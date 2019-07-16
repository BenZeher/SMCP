package smgl;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import SMClasses.SMBatchStatuses;
import SMDataDefinition.SMTableglaccounts;
import SMDataDefinition.SMTableglexternalcompanies;
import SMDataDefinition.SMTableglexternalcompanypulls;
import SMDataDefinition.SMTablegltransactionlines;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsManageRequestParameters;
import smcontrolpanel.SMMasterEditAction;
import smcontrolpanel.SMSystemFunctions;

public class GLPullIntoConsolidationAction extends HttpServlet{
	
	private static final long serialVersionUID = 1L;
	public void doPost(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
	
		SMMasterEditAction smaction = new SMMasterEditAction(request, response);
		try {
			smaction.getCurrentSession().removeAttribute(GLPullIntoConsolidationSelect.SESSION_WARNING_OBJECT);
		} catch (Exception e2) {
			//If this attribute isn't in the session, just go on without disruption....
		}
		if (!smaction.processSession(getServletContext(), SMSystemFunctions.GLPullExternalDataIntoConsolidation)){return;}
	    //Read the entry fields from the request object:
	    String sExternalCompanyID = ServletUtilities.clsManageRequestParameters.get_Request_Parameter(GLPullIntoConsolidationSelect.RADIO_BUTTONS_NAME, request);
	    boolean bAddNewGLs = ServletUtilities.clsManageRequestParameters.get_Request_Parameter(
	    	GLPullIntoConsolidationSelect.ADD_GL_ACCOUNTS, request).compareToIgnoreCase("") != 0;
	    String sFiscalYearAndPeriodPeriod = request.getParameter(GLPullIntoConsolidationSelect.PARAM_FISCAL_PERIOD_SELECTION);
		String sFiscalYear = sFiscalYearAndPeriodPeriod.substring(0, sFiscalYearAndPeriodPeriod.indexOf(GLPullIntoConsolidationSelect.PARAM_VALUE_DELIMITER));
		String sFiscalPeriod = sFiscalYearAndPeriodPeriod.replace(sFiscalYear + GLTrialBalanceSelect.PARAM_VALUE_DELIMITER, "");
		
		if (request.getParameter(GLPullIntoConsolidationSelect.CONFIRM_PROCESS) == null){
			smaction.getCurrentSession().setAttribute(GLPullIntoConsolidationSelect.SESSION_WARNING_OBJECT, "You must check the 'Confirm' checkbox to continue.");
			smaction.redirectAction(
					"", 
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
			smaction.getCurrentSession().setAttribute(GLPullIntoConsolidationSelect.SESSION_WARNING_OBJECT, e1.getMessage());
			smaction.redirectAction(
					"", 
					"", 
		    		""
				);
				return;
		}
    	
		try {
			pullCompany(smaction, request, sExternalCompanyID, sFiscalYear, sFiscalPeriod, bAddNewGLs, conn);
		} catch (Exception e) {
			ServletUtilities.clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1562875614]");
			smaction.getCurrentSession().setAttribute(GLPullIntoConsolidationSelect.SESSION_WARNING_OBJECT, e.getMessage());
			smaction.redirectAction(
				"", 
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
    	
    	//Make sure that all the GLs in the 'source' data are also in the consolidated company:
    	SQL = "SELECT DISTINCT SOURCETABLE." + SMTablegltransactionlines.sacctid
    		+ " FROM " + sDBName + "." + SMTablegltransactionlines.TableName + " AS SOURCETABLE"
    		+ " LEFT JOIN " + SMTableglaccounts.TableName + " ON "
    		+ "SOURCETABLE." + SMTablegltransactionlines.sacctid + " = "
    		+ SMTableglaccounts.TableName + "." + SMTablegltransactionlines.sacctid
    		+ " WHERE ("
			+ "(SOURCETABLE." + SMTablegltransactionlines.ifiscalperiod + " = " + sFiscalPeriod + ")"
			+ " AND (SOURCETABLE." + SMTablegltransactionlines.ifiscalyear + " = " + sFiscalYear + ")"
    		+ " AND (" + SMTableglaccounts.TableName + "." + SMTablegltransactionlines.sacctid + " IS NULL)"
    		+ ")"
    	;
    	//System.out.println("[20191961339222] " + "SQL = " + SQL);
    	String sMissingAccts = "";
    	try {
			ResultSet rs = ServletUtilities.clsDatabaseFunctions.openResultSet(SQL, conn);
			while (rs.next()){
				sMissingAccts += rs.getString("SOURCETABLE." + SMTablegltransactionlines.sacctid) + ".  ";
			}
			rs.close();
		} catch (Exception e2) {
			throw new Exception("Error [2019196139556] " + "Error checking for missing GL Accounts in the consolidate company with SQL: '" + SQL + "' - " + e2.getMessage());
		}
    	
    	if (sMissingAccts.compareToIgnoreCase("") != 0){
    		throw new Exception("Error [2019196138589] " + "These accounts are missing in the consolidated company GL:  " + sMissingAccts);
    	}
    	
    	//Start a data transaction:
    	try {
			ServletUtilities.clsDatabaseFunctions.start_data_transaction_with_exception(conn);
		} catch (Exception e1) {
			throw new Exception("Error [2019192162080] " + "Could not start data transaction - " + e1.getMessage() + ".");
		}
    	
    	long lExternalCompanyPullID = 0L;
    	
    	//Add a record to the list of 'pulls':
    	SQL = "INSERT INTO " + SMTableglexternalcompanypulls.TableName
    		+ " ("
    		+ SMTableglexternalcompanypulls.dattimepulldate
    		+ ", " + SMTableglexternalcompanypulls.ifiscalperiod
    		+ ", " + SMTableglexternalcompanypulls.ifiscalyear
    		+ ", " + SMTableglexternalcompanypulls.lcompanyid
    		+ ", " + SMTableglexternalcompanypulls.luserid
    		+ ", " + SMTableglexternalcompanypulls.scompanyname
    		+ ", " + SMTableglexternalcompanypulls.sdbname
    		+ ", " + SMTableglexternalcompanypulls.sfullusername
    		+ " ) VALUES ("
    		+ "NOW()"
    		+ ", " + sFiscalPeriod
    		+ ", " + sFiscalYear
    		+ ", " + sCompanyID
    		+ ", " + sm.getUserID()
    		+ ", '" + sCompanyName + "'"
    		+ ", '" + sDBName + "'"
    		+ ", '" + sm.getFullUserName() + "'"
    		+ ")"
    	;
    	try {
			Statement stmt = conn.createStatement();
			stmt.execute(SQL);
		} catch (Exception e) {
			clsDatabaseFunctions.rollback_data_transaction(conn);
			throw new Exception("Error [20191921611298] " 
				+ "Could not insert 'pull' record with SQL: '" + SQL + "'" + e.getMessage() + ".");
		}
    	
		SQL = "SELECT LAST_INSERT_ID()";
		try {
			ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
			if (!rs.next()){
		    	clsDatabaseFunctions.rollback_data_transaction(conn);
		    	rs.close();
		    	throw new Exception("Error [20191971220397] " + "no last insert ID record with SQL '" + SQL + ".");
			}else{
				lExternalCompanyPullID = rs.getLong(1);
				rs.close();
			}
		} catch (SQLException e) {
	    	clsDatabaseFunctions.rollback_data_transaction(conn);
	    	throw new Exception("Error [20191971222108] " + "getting last insert ID with SQL '" + SQL + "' - " + e.getMessage());
		}
    	
    	//Now insert all the transactions:
    	SQL = "INSERT INTO " + SMTablegltransactionlines.TableName
    		+ "("
    		+ SMTablegltransactionlines.bdamount
    		+ ", " + SMTablegltransactionlines.datpostingdate
    		+ ", " + SMTablegltransactionlines.dattransactiondate
    		+ ", " + SMTablegltransactionlines.ifiscalperiod
    		+ ", " + SMTablegltransactionlines.ifiscalyear
    		+ ", " + SMTablegltransactionlines.lexternalcompanypullid
    		+ ", " + SMTablegltransactionlines.loriginalbatchnumber
    		+ ", " + SMTablegltransactionlines.loriginalentrynumber
    		+ ", " + SMTablegltransactionlines.loriginallinenumber
    		+ ", " + SMTablegltransactionlines.lsourceledgertransactionlineid
    		+ ", " + SMTablegltransactionlines.sacctid
    		+ ", " + SMTablegltransactionlines.sdescription
    		+ ", " + SMTablegltransactionlines.sreference
    		+ ", " + SMTablegltransactionlines.ssourceledger
    		+ ", " + SMTablegltransactionlines.ssourcetype
    		+ ", " + SMTablegltransactionlines.stransactiontype

    		+ ") "
    		
    		+ " SELECT"
    		+ " " + SMTablegltransactionlines.bdamount
    		+ ", " + SMTablegltransactionlines.datpostingdate
    		+ ", " + SMTablegltransactionlines.dattransactiondate
    		+ ", " + SMTablegltransactionlines.ifiscalperiod
    		+ ", " + SMTablegltransactionlines.ifiscalyear
    		+ ", " + Long.toString(lExternalCompanyPullID)
    		+ ", " + SMTablegltransactionlines.loriginalbatchnumber
    		+ ", " + SMTablegltransactionlines.loriginalentrynumber
    		+ ", " + SMTablegltransactionlines.loriginallinenumber
    		+ ", " + SMTablegltransactionlines.lsourceledgertransactionlineid
    		+ ", " + SMTablegltransactionlines.sacctid
    		+ ", " + SMTablegltransactionlines.sdescription
    		+ ", " + SMTablegltransactionlines.sreference
    		+ ", " + SMTablegltransactionlines.ssourceledger
    		+ ", " + SMTablegltransactionlines.ssourcetype
    		+ ", " + SMTablegltransactionlines.stransactiontype
    		+ " FROM " + sDBName + "." + SMTablegltransactionlines.TableName
    		+ " WHERE ("
    			+ "(" + SMTablegltransactionlines.ifiscalperiod + " = " + sFiscalPeriod + ")"
    			+ " AND (" + SMTablegltransactionlines.ifiscalyear + " = " + sFiscalYear + ")"
    			+ " AND (" + SMTablegltransactionlines.bdamount + " != 0.00)"
    		+ ")"
    	;
    	try {
    		Statement stmt = conn.createStatement();
    		stmt.execute(SQL);
		} catch (Exception e) {
			clsDatabaseFunctions.rollback_data_transaction(conn);
			throw new Exception("Error [2019191165588] " + "could not pull GL transactions using SQL '"
				+ SQL + "' - " + e.getMessage()
			);
		}
    	
    	//Now update the fiscal set data:
    	//TODO
    	
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
			+ " " + SMTableglexternalcompanypulls.dattimepulldate
			+ ", " + SMTableglexternalcompanypulls.lid
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