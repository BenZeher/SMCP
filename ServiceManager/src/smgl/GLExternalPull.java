package smgl;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.servlet.ServletContext;

import SMClasses.SMLogEntry;
import SMDataDefinition.SMTableglaccounts;
import SMDataDefinition.SMTableglexternalcompanies;
import SMDataDefinition.SMTableglexternalcompanypulls;
import SMDataDefinition.SMTablegloptions;
import SMDataDefinition.SMTablegltransactionlines;
import ServletUtilities.clsDatabaseFunctions;
import smcontrolpanel.SMUtilities;

public class GLExternalPull {
	public GLExternalPull(
			) 
	{
	}
	
	public void pullCompany(
			String sDBID,
			String sUserID,
			String sUserFullName,
			String sCompanyID, 
			String sFiscalYear, 
			String sFiscalPeriod, 
			Connection conn,
			ServletContext context
			) throws Exception{
			
			//Create a log object:
			SMLogEntry log = new SMLogEntry(conn);
			
	    	//First confirm that the period in the consolidated company is unlocked:
	    	GLFiscalYear period = new GLFiscalYear();
	    	if (period.isPeriodLocked(sFiscalYear, Integer.parseInt(sFiscalPeriod), conn)){
	    		if (context != null){
	    			ServletUtilities.clsDatabaseFunctions.freeConnection(context, conn, "[1562875355]");
	    		}
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
	    	
	    	try {
				setPostingFlag(conn, sUserID, sCompanyName, "PULLING EXTERNAL COMPANY " + sCompanyName + "'");
			} catch (Exception e2) {
				throw new Exception("Error [20191971411123] " + "setting posting flag - " + e2.getMessage());
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
	    		+ ", " + SMTableglexternalcompanypulls.ipulltype
	    		+ ", " + SMTableglexternalcompanypulls.lcompanyid
	    		+ ", " + SMTableglexternalcompanypulls.luserid
	    		+ ", " + SMTableglexternalcompanypulls.scompanyname
	    		+ ", " + SMTableglexternalcompanypulls.sdbname
	    		+ ", " + SMTableglexternalcompanypulls.sfullusername
	    		+ " ) VALUES ("
	    		+ "NOW()"
	    		+ ", " + sFiscalPeriod
	    		+ ", " + sFiscalYear
	    		+ ", " + Integer.toString(SMTableglexternalcompanypulls.PULL_TYPE_PULL)
	    		+ ", " + sCompanyID
	    		+ ", " + sUserID
	    		+ ", '" + sCompanyName + "'"
	    		+ ", '" + sDBName + "'"
	    		+ ", '" + sUserFullName + "'"
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
	    		+ ", " + SMTablegltransactionlines.sSourceledgertransactionlink
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
	    		+ ", " + SMTablegltransactionlines.sSourceledgertransactionlink
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
	    	try {
				GLTransactionBatch.updateFiscalSets(log, sUserID, "", Long.toString(lExternalCompanyPullID), conn, false, 0);
			} catch (Exception e2) {
				clsDatabaseFunctions.rollback_data_transaction(conn);
				throw new Exception("Error [20193161423264] " + "updating fiscal sets - " + e2.getMessage());
			}

	    	//Commit the data transaction:
	    	try {
				ServletUtilities.clsDatabaseFunctions.commit_data_transaction_with_exception(conn);
			} catch (Exception e1) {
				clsDatabaseFunctions.rollback_data_transaction(conn);
				throw new Exception("Error [20191921620409] " + "Could not commit data transaction - " + e1.getMessage() + ".");
			}
	    	
	    	try {
				unsetPostingFlag(conn);
			} catch (Exception e) {
				clsDatabaseFunctions.rollback_data_transaction(conn);
				throw new Exception("Error [20191971412326] " + e.getMessage());
			}
	    	
			return;
		}
	private void checkForPreviousPull(Connection conn, String sFiscalYear, String sFiscalPeriod, String sCompanyID) throws Exception{
		String SQL = "SELECT"
			+ " " + SMTableglexternalcompanypulls.dattimepulldate
			+ ", " + SMTableglexternalcompanypulls.ipulltype
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
		int iNumberOfPulls = 0;
		int iNumberOfReversals = 0;
		String sCompanyName = "";
		String sFullUserName = "";
		String sPullTime = "";
		while (rs.next()){
			if (rs.getInt(SMTableglexternalcompanypulls.ipulltype) == SMTableglexternalcompanypulls.PULL_TYPE_PULL){
				iNumberOfPulls++;
			}else{
				iNumberOfReversals++;
			}
			sCompanyName = rs.getString(SMTableglexternalcompanypulls.scompanyname);
			sFullUserName = rs.getString(SMTableglexternalcompanypulls.sfullusername);
			sPullTime = ServletUtilities.clsDateAndTimeConversions.resultsetDateTimeStringToFormattedString(
				rs.getString(SMTableglexternalcompanypulls.dattimepulldate),
				ServletUtilities.clsServletUtilities.DATETIME_FORMAT_FOR_DISPLAY,
				ServletUtilities.clsServletUtilities.EMPTY_DATETIME_VALUE)
			;
		}
		rs.close();
		
		if (iNumberOfPulls > iNumberOfReversals){
			throw new Exception("Error [201919384192] " + "Fiscal period " + sFiscalPeriod + " for fiscal year " + sFiscalYear + " has already been pulled"
					+ " for company '" + sCompanyName + "' with company ID " + sCompanyID + " by " + sFullUserName + " - " + sPullTime + "."
				);
		}
		
		return;
	}
    private void setPostingFlag(Connection conn, String sUserID, String sCompanyName, String sProcessDescription) throws Exception{
		//First check to make sure no one else is posting:
		try{
			String SQL = "SELECT * FROM " + SMTablegloptions.TableName;
			ResultSet rsGLOptions = clsDatabaseFunctions.openResultSet(SQL, conn);
			if (!rsGLOptions.next()){
	    		throw new Exception("Error [1563297146] - could not get GL Options record.");
			}else{
				if(rsGLOptions.getLong(SMTablegloptions.ibatchpostinginprocess) == 1){
					throw new Exception("Error [1563297147] - A previous GL posting is not completed - "
						+ SMUtilities.getFullNamebyUserID(rsGLOptions.getString(SMTablegloptions.luserid), conn) + " has been "
						+ rsGLOptions.getString(SMTablegloptions.sprocess) + " "
						+ "since " + rsGLOptions.getString(SMTablegloptions.datstartdate) + "."
					);
				}
			}
			rsGLOptions.close();
		}catch (SQLException e){
			throw new Exception("Error [1563297148] checking for previous posting - " + e.getMessage());
		}
		//If not, then set the posting flag:
		try{
			String SQL = "UPDATE " + SMTablegloptions.TableName 
				+ " SET " + SMTablegloptions.ibatchpostinginprocess + " = 1"
				+ ", " + SMTablegloptions.datstartdate + " = NOW()"
				+ ", " + SMTablegloptions.sprocess 
					+ " = '" + sProcessDescription + "'"
	   			+ ", " + SMTablegloptions.luserid 
					+ " = " + sUserID
			;
			if (!clsDatabaseFunctions.executeSQL(SQL, conn)){
				throw new Exception("Error [1563297149] setting posting flag in GL Options");
	    		
			}
		}catch (SQLException e){
			throw new Exception("Error [1563297160] setting posting flag in GL Options - " + e.getMessage());
			
		}
    }
	private void unsetPostingFlag(Connection conn) throws Exception{
		try{
			String SQL = "UPDATE " + SMTablegloptions.TableName 
			+ " SET " + SMTablegloptions.ibatchpostinginprocess + " = 0"
			+ ", " + SMTablegloptions.datstartdate + " = '0000-00-00 00:00:00'"
			+ ", " + SMTablegloptions.sprocess + " = ''"
				+ ", " + SMTablegloptions.luserid + " = 0"
			;
			if (!clsDatabaseFunctions.executeSQL(SQL, conn)){
				throw new Exception("Error [1563297145] clearing posting flag in GL Options");
			}
		}catch (SQLException e){
			throw new Exception("Error [1563297146] clearing posting flag in GL Options - " + e.getMessage());
		}
	}
	public void reversePreviousPull(Connection conn, String sUserID, String sCompanyName, String sPullID, SMLogEntry log) throws Exception{
		//First, set the posting flag so nothing can change while we do it:
    	try {
			setPostingFlag(conn, sUserID, sCompanyName, "REVERSING PULL ID " + sPullID);
		} catch (Exception e2) {
			throw new Exception("Error [20191971412123] " + "setting posting flag to reverse pull - " + e2.getMessage());
		}
    	
    	//Start a data transaction:
    	try {
			ServletUtilities.clsDatabaseFunctions.start_data_transaction_with_exception(conn);
		} catch (Exception e1) {
			throw new Exception("Error [2019192172080] " + "Could not start data transaction - " + e1.getMessage() + ".");
		}
    	
    	//Do the reversal:
    	try {
			reversePull(conn, sPullID, sUserID, log);
		} catch (Exception e2) {
			ServletUtilities.clsDatabaseFunctions.rollback_data_transaction(conn);
			throw new Exception("Error [20193221520447] " + " reversing Pull - " + e2.getMessage());
		}
    	
    	//Commit the data transaction:
    	try {
			ServletUtilities.clsDatabaseFunctions.commit_data_transaction_with_exception(conn);
		} catch (Exception e1) {
			clsDatabaseFunctions.rollback_data_transaction(conn);
			throw new Exception("Error [20191921620409] " + "Could not commit data transaction - " + e1.getMessage() + ".");
		}
    	
    	try {
			unsetPostingFlag(conn);
		} catch (Exception e) {
			clsDatabaseFunctions.rollback_data_transaction(conn);
			throw new Exception("Error [20191971512326] " + e.getMessage());
		}
		
	}
	private void reversePull(Connection conn, String sPullID, String sUserID, SMLogEntry log) throws Exception {
		
		//First, update the fiscal sets and financial data for the pull:
		
		//We'll need to updated the fiscal sets and financial data, but there's no way
		//to do that after the GL transaction line records are deleted.  So we'll
		// first NEGATE all the transactions for this pull, and tell the fiscal set updater
		// to update the fiscal sets and financial data for them.  This will effectively
		// adjust the fiscal sets and financial data to values as if the pull transactions
		// never happened.
		// After that, we can just remove the GL transaction line records for the pull
		// and we'll be all finished.
		
		String SQL = "UPDATE" + SMTablegltransactionlines.TableName
			+ " SET " + SMTablegltransactionlines.bdamount + " = (-1 * " + SMTablegltransactionlines.bdamount + ")"
			+ " WHERE ("
				+ "(" + SMTablegltransactionlines.lexternalcompanypullid + " = " + sPullID + ")"
			+ ")"
		;
		try {
			ServletUtilities.clsDatabaseFunctions.executeSQLWithException(SQL, conn);
		} catch (Exception e) {
			throw new Exception("Error [20193221546130] " + "negating GL transaction lines for pull ID " + sPullID + " with SQL: '" + SQL + "' - " + e.getMessage());
		}
		
		//Now tell the fiscal/financial updater to process those transactions:
    	try {
			GLTransactionBatch.updateFiscalSets(log, sUserID, "", sPullID, conn, false, 0);
		} catch (Exception e2) {
			clsDatabaseFunctions.rollback_data_transaction(conn);
			throw new Exception("Error [20193161425264] " + "updating fiscal sets - " + e2.getMessage());
		}
		
		//Next, just delete the pull transactions
		SQL = "DELETE FROM " + SMTablegltransactionlines.TableName
			+ " WHERE ("
				+ "(" + SMTablegltransactionlines.lexternalcompanypullid + " = " + sPullID + ")"
			+ ")"
		;
		try {
			ServletUtilities.clsDatabaseFunctions.executeSQLWithException(SQL, conn);
		} catch (Exception e) {
			throw new Exception("Error [20193221546140] " + "removing GL transaction lines for pull ID " + sPullID + " with SQL: '" + SQL + "' - " + e.getMessage());
		}
		
		//Finally, add a 'pull' record for the reversal:
		//TODO
		
		
		
	}
}
