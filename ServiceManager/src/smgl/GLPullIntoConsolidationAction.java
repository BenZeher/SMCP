package smgl;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import SMClasses.SMBatchStatuses;
import SMDataDefinition.SMTableglexternalcompanies;
import SMDataDefinition.SMTableglexternalcompanypulls;
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
		
		if (request.getParameter(GLPullIntoConsolidationSelect.CONFIRM_PROCESS) == null){
			smaction.redirectAction(
					"You must check the 'Confirm' checkbox to continue.", 
					"", 
		    		""
				);
				return;
		}
		
    	Connection conn = null;
    	try {
			conn = ServletUtilities.clsDatabaseFunctions.getConnectionWithException(
				getServletContext(), 
				smaction.getsDBID(), 
				"MySQL", 
				this.toString() + ".updateCompanies - user: " + smaction.getFullUserName()
			);
		} catch (Exception e1) {
			smaction.redirectAction(
					e1.getMessage(), 
					"", 
		    		""
				);
				return;
		}
    	
		try {
			pullCompany(smaction, request, sExternalCompanyID, sFiscalYear, sFiscalPeriod, bAddNewGLs, conn);
		} catch (Exception e) {
			ServletUtilities.clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1562875614]");
			smaction.redirectAction(
				e.getMessage(), 
				"", 
	    		""
			);
			return;
		}
		ServletUtilities.clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1562875615]");
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
		boolean bAddNewGLs,
		Connection conn
		) throws Exception{
		
    	//Pull the transactions into a single GL batch:
    	
    	//First confirm that the period in the consolidated company is unlocked:
    	GLFiscalYear period = new GLFiscalYear();
    	if (period.isPeriodLocked(sFiscalYear, Integer.parseInt(sFiscalPeriod), conn)){
    		ServletUtilities.clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1562875355]");
			throw new Exception(
					"Error [20191901557102] " + "fiscal period '" + sFiscalYear + " - " + sFiscalPeriod + "' is locked.");
    	}
    	
    	//Next, confirm that the period hasn't been 'pulled' before:
    	try {
			checkForPreviousPull(conn, sFiscalYear, sFiscalPeriod, sCompanyID);
		} catch (Exception e2) {
			throw new Exception("Error [2019193811396] " + e2.getMessage());
		}
    	
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
    	
    	//Start a data transaction:
    	try {
			ServletUtilities.clsDatabaseFunctions.start_data_transaction_with_exception(conn);
		} catch (Exception e1) {
			throw new Exception("Error [2019192162080] " + "Could not start data transaction - " + e1.getMessage() + ".");
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
    	try {
			glbatch.save_without_data_transaction(conn, sm.getUserID(), sm.getFullUserName(), false);
		} catch (Exception e) {
			throw new Exception("Error [2019192164281] " + "Could not save GL Transaction batch - " + e.getMessage());
		}
    	
    	//Add a record to the list of 'pulls':
    	SQL = "INSERT INTO " + SMTableglexternalcompanypulls.TableName
    		+ " ("
    		+ SMTableglexternalcompanypulls.dattimepulldate
    		+ ", " + SMTableglexternalcompanypulls.ifiscalperiod
    		+ ", " + SMTableglexternalcompanypulls.ifiscalyear
    		+ ", " + SMTableglexternalcompanypulls.lbatchnumber
    		+ ", " + SMTableglexternalcompanypulls.lcompanyid
    		+ ", " + SMTableglexternalcompanypulls.luserid
    		+ ", " + SMTableglexternalcompanypulls.scompanyname
    		+ ", " + SMTableglexternalcompanypulls.sdbname
    		+ ", " + SMTableglexternalcompanypulls.sfullusername
    		+ " ) VALUES ("
    		+ "NOW()"
    		+ ", " + sFiscalPeriod
    		+ ", " + sFiscalYear
    		+ ", " + glbatch.getsbatchnumber()
    		+ ", " + sCompanyID
    		+ ", " + sm.getUserID()
    		+ ", '" + sCompanyName + "'"
    		+ ", '" + sDBName + "'"
    		+ ", '" + sm.getFullUserName()
    		+ ")"
    	;
    	try {
			Statement stmt = conn.createStatement();
			stmt.execute(SQL);
		} catch (Exception e) {
			throw new Exception("Error [20191921611298] " 
				+ "Could not insert 'pull' record with SQL: '" + SQL + "'" + e.getMessage() + ".");
		}
    	
    	//Commit the data transaction:
    	try {
			ServletUtilities.clsDatabaseFunctions.commit_data_transaction_with_exception(conn);
		} catch (Exception e1) {
			throw new Exception("Error [20191921620409] " + "Could not commit data transaction - " + e1.getMessage() + ".");
		}
    	
		return;
	}
	private void checkForPreviousPull(Connection conn, String sFiscalYear, String sFiscalPeriod, String sCompanyID) throws Exception{
		String SQL = "SELECT"
			+ " " + SMTableglexternalcompanypulls.lid
			+ ", " + SMTableglexternalcompanypulls.scompanyname
			+ ", " + SMTableglexternalcompanypulls.sfullusername
			+ " FROM " + SMTableglexternalcompanypulls.TableName
			+ " WHERE ("
				+ "(" + SMTableglexternalcompanypulls.lcompanyid + " = " + sCompanyID + ")"
				+ " AND (" + SMTableglexternalcompanypulls.ifiscalperiod + " = " + sFiscalPeriod + ")"
				+ " AND (" + SMTableglexternalcompanypulls.ifiscalyear + " = " + sFiscalYear + ")"
			+ ")"
		;
		ResultSet rs = ServletUtilities.clsDatabaseFunctions.openResultSet(SQL, conn);
		if (rs.next()){
			String sCompanyName = rs.getString(SMTableglexternalcompanypulls.scompanyname);
			String sFullUserName = rs.getString(SMTableglexternalcompanypulls.sfullusername);
			String sPullTime = ServletUtilities.clsDateAndTimeConversions.resultsetDateTimeStringToFormattedString(
				rs.getString(SMTableglexternalcompanypulls.dattimepulldate),
				ServletUtilities.clsServletUtilities.DATETIME_FORMAT_FOR_DISPLAY,
				ServletUtilities.clsServletUtilities.EMPTY_DATETIME_VALUE)
			;
			rs.close();
			throw new Exception("Error [201919384192] " + "Fiscal period " + sFiscalPeriod + " for fiscal year " + sFiscalYear + " has already been pulled"
				+ " for company '" + sCompanyName + "' with company ID " + sCompanyID + " by " + sFullUserName + " - " + sPullTime + "."
			);
		}
		rs.close();
		
		return;
	}
	/* TJR - 7/12/2019 - probably can't use this because the account structures may not be the same in the SOURCE and TARGET companies....
	private void addNewGLAccounts(Connection conn, String sDBName, String sFiscalYear, String sFiscalPeriod) throws Exception{
		
		String SQL = "SELECT DISTINCT " + SMTablegltransactionlines.TableName + "." + SMTablegltransactionlines.sacctid
			+ ",  " + SMTableglaccounts.TableName + "." + SMTableglaccounts.bdannualbudget
			+ ",  " + SMTableglaccounts.TableName + "." + SMTableglaccounts.iallowaspoexpense
			+ ",  " + SMTableglaccounts.TableName + "." + SMTableglaccounts.iCostCenterID
			+ ",  " + SMTableglaccounts.TableName + "." + SMTableglaccounts.inormalbalancetype
			+ ",  " + SMTableglaccounts.TableName + "." + SMTableglaccounts.laccountgroupid
			+ ",  " + SMTableglaccounts.TableName + "." + SMTableglaccounts.lActive
			+ ",  " + SMTableglaccounts.TableName + "." + SMTableglaccounts.lstructureid
			+ ",  " + SMTableglaccounts.TableName + "." + SMTableglaccounts.sAcctID
			+ ",  " + SMTableglaccounts.TableName + "." + SMTableglaccounts.sAcctType
			+ ",  " + SMTableglaccounts.TableName + "." + SMTableglaccounts.sDesc
			+ ",  " + SMTableglaccounts.TableName + "." + SMTableglaccounts.sFormattedAcct
			+ " FROM " + sDBName + "." + SMTablegltransactionlines.TableName + " LEFT JOIN " + sDBName + "." + SMTableglaccounts.TableName + " ON "
			+ sDBName + "." + SMTablegltransactionlines.TableName + "." + SMTablegltransactionlines.sacctid + " = " 
			+ sDBName + "." + SMTableglaccounts.TableName + "." + SMTableglaccounts.sAcctID
			+ " WHERE ("
			    + "(" + SMTablegltransactionlines.ifiscalperiod + " = " + sFiscalPeriod + ")"
    			+ " AND (" + SMTablegltransactionlines.ifiscalyear + " = " + sFiscalYear + ")"
			+ ")"
		;
		
		//TODO - go through the accounts and add them if needed:
		
		
		return;
	}
	*/
	public void doGet(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
		
		doPost(request, response);
	}
}