package smgl;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.ResultSet;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import SMClasses.SMBatchStatuses;
import SMDataDefinition.SMTableglexternalcompanies;
import SMDataDefinition.SMTablegltransactionbatches;
import SMDataDefinition.SMTablegltransactionlines;
import ServletUtilities.clsManageRequestParameters;
import smcontrolpanel.SMMasterEditAction;
import smcontrolpanel.SMSystemFunctions;

public class GLPullIntoConsolidationAction extends HttpServlet{
	
	private static final long serialVersionUID = 1L;
	public void doPost(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
	
		SMMasterEditAction smaction = new SMMasterEditAction(request, response);
		if (!smaction.processSession(getServletContext(), SMSystemFunctions.GLPullExternalDataIntoConsolidation)){return;}
	    //Read the entry fields from the request object:
	    String sExternalCompanyID = ServletUtilities.clsManageRequestParameters.get_Request_Parameter(GLPullIntoConsolidationSelect.RADIO_BUTTONS_NAME, request);
	    boolean bAddNewGLs = ServletUtilities.clsManageRequestParameters.get_Request_Parameter(
	    	GLPullIntoConsolidationSelect.ADD_GL_ACCOUNTS, request).compareToIgnoreCase("") != 0;
	    String sFiscalYearAndPeriodPeriod = request.getParameter(GLPullIntoConsolidationSelect.PARAM_FISCAL_PERIOD_SELECTION);
		String sFiscalYear = sFiscalYearAndPeriodPeriod.substring(0, sFiscalYearAndPeriodPeriod.indexOf(GLPullIntoConsolidationSelect.PARAM_VALUE_DELIMITER));
		String sFiscalPeriod = sFiscalYearAndPeriodPeriod.replace(sFiscalYear + GLTrialBalanceSelect.PARAM_VALUE_DELIMITER, "");
		
		try {
			pullCompany(smaction, request, sExternalCompanyID, sFiscalYear, sFiscalPeriod, bAddNewGLs);
		} catch (Exception e) {
			smaction.redirectAction(
				e.getMessage(), 
				"", 
	    		""
			);
			return;
		}
		smaction.redirectAction(
			"", 
			"Company with ID '" + sExternalCompanyID + "' was successfully pulled into the consolidated company.",
    		""
		);
		return;
	}
	private void pullCompany(
		SMMasterEditAction sm, 
		HttpServletRequest req, 
		String sCompanyID, 
		String sFiscalYear, 
		String sFiscalPeriod, 
		boolean bAddNewGLs
		) throws Exception{
		
		//Read the external company lines:
		String sErrorString = "";
    	
    	Connection conn = null;
    	try {
			conn = ServletUtilities.clsDatabaseFunctions.getConnectionWithException(
				getServletContext(), 
				sm.getsDBID(), 
				"MySQL", 
				this.toString() + ".updateCompanies - user: " + sm.getFullUserName()
			);
		} catch (Exception e1) {
			throw new Exception("Error [1562702050] " + "Could not get data connection - " + e1.getMessage());
		}
    	
    	//Pull the transactions into a single GL batch:
    	
    	//First confirm that the period in the consolidated company is unlocked:
    	GLFiscalYear period = new GLFiscalYear();
    	if (period.isPeriodLocked(sFiscalYear, Integer.parseInt(sFiscalPeriod), conn)){
			throw new Exception(
					"Error [20191901557102] " + "fiscal period '" + sFiscalYear + " - " + sFiscalPeriod + "' is locked.");
    	}
    	
    	//Next, confirm that the period hasn't been 'pulled' before:
    	//TODO
    	
    	//Get the company information:
    	String sDBName = "";
    	String sCompanyName = "";
    	String SQL = "SELECT * FROM " + SMTableglexternalcompanies.TableName
    		+ " WHERE ("
    			+ "(" + SMTableglexternalcompanies.lid + " = " + sCompanyID + ")"
    		+ ")"
    	;
    	try {
			ResultSet rsCompany = ServletUtilities.clsDatabaseFunctions.openResultSet(SQL, conn);
			if (rsCompany.next()){
				sDBName = rsCompany.getString(SMTableglexternalcompanies.sdbname);
				sCompanyName = rsCompany.getString(SMTableglexternalcompanies.scompanyname);
			}else{
				rsCompany.close();
				throw new Exception("Error [20191911552508] " + "Could not read external company information for comapny ID '" + sCompanyID + "'.");
			}
			rsCompany.close();
		} catch (Exception e) {
			throw new Exception("Error [20191911553508] " + "reading external company information with SQL: '" + SQL + "' - " + e.getMessage());
		}
    	
    	//Now create a new batch:
    	GLTransactionBatch glbatch = new GLTransactionBatch("-1");
    	glbatch.setlcreatedby(sm.getUserID());
    	glbatch.setllasteditedby(sm.getUserID());
    	glbatch.setsbatchdate(clsManageRequestParameters.get_Request_Parameter(
    			GLPullIntoConsolidationSelect.PARAM_BATCH_DATE, req).replace("&quot;", "\""));
    	glbatch.setsbatchdescription("Pulled from company '" + sCompanyName + "'");
    	glbatch.setsbatchstatus(Integer.toString(SMBatchStatuses.IMPORTED));

    	//Create a single entry for all the transactions:
    	GLTransactionBatchEntry glentry = new GLTransactionBatchEntry();
    	glentry.setsdatdocdate(glbatch.getsbatchdate());
    	glentry.setsdatentrydate(glbatch.getsbatchdate());
    	glentry.setsentrydescription("Consolidated pull");
    	glentry.setsfiscalperiod(sFiscalPeriod);
    	glentry.setsfiscalyear(sFiscalYear);
    	glentry.setssourceledger(GLSourceLedgers.getSourceLedgerDescription(GLSourceLedgers.SOURCE_LEDGER_JOURNAL_ENTRY));

    	//Now load all the transactions into the entry:
    	SQL = "SELECT * FROM " + sDBName + "." + SMTablegltransactionlines.TableName
    		+ " WHERE ("
    			+ "(" + SMTablegltransactionlines.ifiscalperiod + " = " + sFiscalPeriod + ")"
    			+ " AND (" + SMTablegltransactionlines.ifiscalyear + " = " + sFiscalYear + ")"
    		+ ")"
    	;
    	try {
			ResultSet rs = ServletUtilities.clsDatabaseFunctions.openResultSet(SQL, conn);
			while (rs.next()){
				GLTransactionBatchLine line = new GLTransactionBatchLine();
				line.setsacctid(rs.getString(SMTablegltransactionlines.sacctid));
				line.setscomment("");
				if (rs.getBigDecimal(SMTablegltransactionlines.bdamount).compareTo(BigDecimal.ZERO) < 0.00){
					line.setscreditamt(ServletUtilities.clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat
						(rs.getBigDecimal(SMTablegltransactionlines.bdamount).abs())
					);
					line.setsdebitamt("0.00");
				}else{
					line.setsdebitamt(ServletUtilities.clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(rs.getBigDecimal(SMTablegltransactionlines.bdamount).abs()));
					line.setscreditamt("0.00");
				}
				line.setsdescription(rs.getString(SMTablegltransactionlines.sdescription));
				line.setsreference(rs.getString(SMTablegltransactionlines.sreference));
				line.setssourceledger(rs.getString(SMTablegltransactionlines.ssourceledger));
				line.setssourcetype(rs.getString(SMTablegltransactionlines.ssourcetype));
				line.setstransactiondate(ServletUtilities.clsDateAndTimeConversions.resultsetDateStringToFormattedString(
					rs.getString(SMTablegltransactionlines.dattransactiondate), 
					ServletUtilities.clsServletUtilities.DATE_FORMAT_FOR_DISPLAY, 
					ServletUtilities.clsServletUtilities.EMPTY_DATE_VALUE)
				);
				glentry.addLine(line);
			}
			rs.close();
		} catch (Exception e) {
			throw new Exception("Error [2019191165588] " + "could not get records of GL transactions using SQL '"
				+ SQL + "' - " + e.getMessage()
			);
		}
    	
    	//Save the batch:
    	glbatch.addBatchEntry(glentry);
    	glbatch.save_without_data_transaction(conn, sm.getUserID(), sm.getFullUserName(), false);
    	
    	
    	if (sErrorString.compareToIgnoreCase("") != 0){
    		ServletUtilities.clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1562702052]");
    		throw new Exception("Error [1562702051] pulling external company's transactions - " + sErrorString);
    	}
    	ServletUtilities.clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1562702053]");
		return;
	}

	public void doGet(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
		
		doPost(request, response);
	}
}