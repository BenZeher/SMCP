package smap;

import java.io.IOException;
//import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import SMClasses.SMBatchStatuses;
import SMClasses.SMLogEntry;
import SMDataDefinition.SMTableapbatches;
import SMDataDefinition.SMTableapchecklines;
import SMDataDefinition.SMTableapchecks;
import SMDataDefinition.SMTableapmatchinglines;
import SMDataDefinition.SMTableapoptions;
import SMDataDefinition.SMTableaptransactionlines;
import SMDataDefinition.SMTableaptransactions;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsDateAndTimeConversions;
import ServletUtilities.clsManageRequestParameters;
import smcontrolpanel.SMAuthenticate;
import smcontrolpanel.SMSystemFunctions;
import smcontrolpanel.SMUtilities;

public class APClearTransactionsAction extends HttpServlet{

	private static final long serialVersionUID = 1L;
	private static String sDBID = "";
	private static String sUserID = "";
	public void doPost(HttpServletRequest request,
			HttpServletResponse response)
	throws ServletException, IOException {

		String sCallingClass = clsManageRequestParameters.get_Request_Parameter("CallingClass", request);
		String sClearingDate = clsManageRequestParameters.get_Request_Parameter(APClearTransactionsSelect.CLEARING_DATE, request);

	    if (!SMAuthenticate.authenticateSMCPCredentials(request, response, getServletContext(), SMSystemFunctions.APClearFullyPaidTransactions)){
	    	return;
	    }
		//Get the session info:
		HttpSession CurrentSession = request.getSession(true);
		sDBID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_DATABASE_ID);
		sUserID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERID);

		if (clsManageRequestParameters.get_Request_Parameter(APClearTransactionsSelect.CONFIRM_CLEARING_CHECKBOX, request).compareToIgnoreCase("") == 0){
			String sWarning = "You chose to clear, but did not check the box to confirm.";
			response.sendRedirect(
					"" + SMUtilities.getURLLinkBase(getServletContext()) + "" + sCallingClass + "?"
					+ "" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
					+ "&Warning=" + sWarning
			);
			return;
		}
		
		try {
			clearTransactions(sDBID, sClearingDate);
		} catch (Exception e) {
			response.sendRedirect(
				"" + SMUtilities.getURLLinkBase(getServletContext()) + "" + sCallingClass + "?"
				+ "" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
				+ "&Warning=" + e.getMessage()
			);
			return;
		}
		
		String sStatus = "Fully paid transactions through " 
			+ sClearingDate + " were cleared.";
		response.sendRedirect(
				"" + SMUtilities.getURLLinkBase(getServletContext()) + "" + sCallingClass + "?"
				+ "" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
				+ "&Status=" + sStatus
		);
		return;
	}
	private void clearTransactions(String sDBID, String sClearingDate) throws Exception{
		
		if (!clsDateAndTimeConversions.IsValidDateString("M/d/yyyy", sClearingDate)){
			throw new Exception("Invalid clearing date - '" + sClearingDate + "'.");
		}
		//Convert the date to a SQL one:
		java.sql.Date datClearingDate = null;
		try {
			datClearingDate = clsDateAndTimeConversions.StringTojavaSQLDate("M/d/yyyy", sClearingDate);
		} catch (ParseException e) {
			throw new Exception("Error:[1506451229] Invalid clearing date '" + sClearingDate + "' - " + e.getMessage());
		}
		java.sql.Date datNow =  clsDateAndTimeConversions.nowAsSQLDate();
		if (datClearingDate.after(datNow)){
			throw new Exception("Invalid clearing date - you cannot choose a date later than today.");
		}
		
		//Need a connection for the data transaction:
		Connection conn;
		try {
			conn = clsDatabaseFunctions.getConnectionWithException(getServletContext(), sDBID, "MySQL", SMUtilities.getFullClassName(this.toString()));
		} catch (Exception e2) {
			throw new Exception("Error [1506471930] getting connection - " + e2.getMessage());
		}
		
		try {
			checkAndSetAPPostingFlag (conn);
		} catch (Exception e) {
			clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[]1547047432");
			throw new Exception("Error [1506471592] checking AP Posting Flag - " + e.getMessage());
		}
		//Have to make sure there are NO unposted batches:
		String SQL = "SELECT"
			+ " " + SMTableapbatches.lbatchnumber
			+ " FROM " + SMTableapbatches.TableName
			+ " WHERE ("
				+ "(" 
				+ "(" + SMTableapbatches.ibatchstatus + " = " + Integer.toString(SMBatchStatuses.ENTERED) + ")"
					+ " OR (" + SMTableapbatches.ibatchstatus + " = " + Integer.toString(SMBatchStatuses.IMPORTED) + ")"
				+ ")"
			+ ")"
			;
		try {
			ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
			if (rs.next()){
				rs.close();
				clearPostingFlag(conn);
				clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[]1547047433");
				throw new Exception("There are unposted batches - these must be posted before you can clear transactions.");
			}
			rs.close();
		} catch (SQLException e1) {
			clearPostingFlag(conn);
			clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[]1547047434");
			throw new Exception("Error [1506452538] checking for unposted batches - with SQL: " + SQL + " - " + e1.getMessage() + ".");
		}
		
		//Log the process:
		SMLogEntry log = new SMLogEntry(sDBID, getServletContext());
		log.writeEntry(
			sUserID, 
			SMLogEntry.LOG_OPERATION_APCLEARPAIDTRANS, 
			"Attempting to clear paid transactions", 
			"Using clearing date " + sClearingDate,
			"[1506452664]"
		);
		
		if(!clsDatabaseFunctions.start_data_transaction(conn)){
			clearPostingFlag(conn);
			clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[]1547047435");
			throw new Exception("Could not start data transaction.");
		}

		try {
			deleteRecords(conn, sClearingDate);
		} catch (Exception e) {
			clsDatabaseFunctions.rollback_data_transaction(conn);
			clearPostingFlag(conn);
			clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[]1547047436");
			throw new Exception("Error [1506472208] - Could not delete records - " + e.getMessage());

		}

		if(!clsDatabaseFunctions.commit_data_transaction(conn)){
			clsDatabaseFunctions.rollback_data_transaction(conn);
			clearPostingFlag(conn);
			clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[]1547047437");
			throw new Exception("Error [1506472298] - Could not commit data transaction.");
		}

		clearPostingFlag(conn);

		log.writeEntry(
			sUserID, 
			SMLogEntry.LOG_OPERATION_APCLEARPAIDTRANS, 
			"Successfully cleared paid AP transactions", 
			"Using clearing date " + clsDateAndTimeConversions.utilDateToString(datClearingDate, "MM/dd/yyyy"),
			"[1506472299]");
		
		return;
	}
	private void deleteRecords(Connection conn, String sClearingDate) throws Exception{
		
		//These are the proposed rules for clearing transactions:
		/*

         Once we delete the appropriate transactions, then we can easily remove the corresponding apmatchingline records. 

		 First, we ONLY delete transactions with a zero current amount.

		 Second, every document must have some applying lines to get it from an original amt to a current amt of zero.
		 
		 We want to check the MOST RECENT applying line for each transaction.  Then we check the DOC DATE for the PARENT
		 transaction for that line.  IF that parent transaction's doc date is in the 'clearing' range,
		 then we can remove the APPLYED-TO transaction.
		 
		 After those transactions are removed, then we can remove the orphaned transaction lines.
		 
		 Next, we have to remove apmatchinglines: the rule we'll use is that IF the apply-to transaction is gone, then all the lines APPLYING TO it must be removed as well.
	
		 Alternative logic?:
		 When deleting aptransactions:
		 
		 IF the transaction date is before the deadline,
		 AND if the transaction dates of any APPLYING lines are before the deadline
		 AND if all the transactions that this transaction APPLIES TO are ALSO before the deadline
		 THEN and only then, we delete the aptransaction line
		 
		 */
		
		//Execute deletes here:
		String SQL = "DELETE FROM " 
			+ SMTableaptransactions.TableName
			+ " WHERE ("
			+ "(" + SMTableaptransactions.bdcurrentamt + " = 0.00)"
			+ " AND (" + SMTableaptransactions.datdocdate + " <= '"
			+ clsDateAndTimeConversions.convertDateFormat(sClearingDate, SMUtilities.DATE_FORMAT_FOR_DISPLAY, SMUtilities.DATE_FORMAT_FOR_SQL, SMUtilities.EMPTY_DATE_VALUE) + " 23:59:59')"
			+ ")"
			;
		System.out.println("[1507060164] - SQL: '" + SQL + "'");
		try {
			Statement stmt = conn.createStatement();
			stmt.execute(SQL);
		} catch (SQLException e1) {
			throw new Exception(" Error [1506472717] - Could not execute delete aptransactions statement with SQL: " + SQL
				+ " - " + e1.getMessage() + ".");
		}

		//This statement deletes aptransactionlines if their 'parent' has been removed:
		SQL = "DELETE " + SMTableaptransactionlines.TableName 
		+ " FROM "
		+ SMTableaptransactionlines.TableName
		+ " LEFT JOIN " + SMTableaptransactions.TableName + " AS TRANSACTIONS"
		+ " ON " + SMTableaptransactionlines.TableName + "." + SMTableaptransactionlines.ltransactionheaderid
		+ " =  TRANSACTIONS." + SMTableaptransactions.lid
		+ " WHERE (TRANSACTIONS." + SMTableaptransactions.lid + " IS NULL)"
		;
		System.out.println("[1507060166] - SQL: '" + SQL + "'");
		try {
			Statement stmt = conn.createStatement();
			stmt.execute(SQL);
		} catch (SQLException e1) {
			throw new Exception("Error [1506472719] - Could not execute delete aptransactionlines statement with SQL: " + SQL
				+ " - " + e1.getMessage() + ".");
		}

		//This statement deletes matching lines AS LONG AS their apply-to transaction is gone
		// It doesn't care if their parent transaction is gone or not:
		SQL = "DELETE " + SMTableapmatchinglines.TableName 
		+ " FROM "
		+ SMTableapmatchinglines.TableName
		+ " LEFT JOIN " + SMTableaptransactions.TableName + " AS APPLIEDTO"
		+ " ON " + SMTableapmatchinglines.TableName + "." + SMTableapmatchinglines.ltransactionappliedtoid
		+ " =  APPLIEDTO." + SMTableaptransactions.lid
		+ " WHERE (APPLIEDTO." + SMTableaptransactions.lid + " IS NULL)"
		;
		System.out.println("[1507060165] - SQL: '" + SQL + "'");
		try {
			Statement stmt = conn.createStatement();
			stmt.execute(SQL);
		} catch (SQLException e1) {
			throw new Exception("Error [1506472718] - Could not execute delete apmatchinglines statement with SQL: " + SQL
				+ " - " + e1.getMessage() + ".");
		}
		
		//This statement deletes apchecks if their corresponding transaction has been removed:
		SQL = "DELETE " + SMTableapchecks.TableName 
		+ " FROM "
		+ SMTableapchecks.TableName
		+ " LEFT JOIN " + SMTableaptransactions.TableName + " AS TRANSACTIONS"
		+ " ON " + SMTableapchecks.TableName + "." + SMTableapchecks.ltransactionid
		+ " =  TRANSACTIONS." + SMTableaptransactions.lid
		+ " WHERE (TRANSACTIONS." + SMTableaptransactions.lid + " IS NULL)"
		;
		System.out.println("[1507060167] - SQL: '" + SQL + "'");
		try {
			Statement stmt = conn.createStatement();
			stmt.execute(SQL);
		} catch (SQLException e1) {
			throw new Exception("Error [1506472720] - Could not execute delete apchecks statement with SQL: " + SQL
				+ " - " + e1.getMessage() + ".");
		}
		
		//This statement then deletes 'orphaned' AP check lines:
		SQL = "DELETE " + SMTableapchecklines.TableName 
		+ " FROM "
		+ SMTableapchecklines.TableName
		+ " LEFT JOIN " + SMTableapchecks.TableName + " AS CHECKS"
		+ " ON " + SMTableapchecklines.TableName + "." + SMTableapchecklines.lcheckid
		+ " =  CHECKS." + SMTableapchecks.lid
		+ " WHERE (CHECKS." + SMTableapchecks.lid + " IS NULL)"
		;
		System.out.println("[1507060167] - SQL: '" + SQL + "'");
		try {
			Statement stmt = conn.createStatement();
			stmt.execute(SQL);
		} catch (SQLException e1) {
			throw new Exception("Error [1506977835] - Could not execute delete apchecklines statement with SQL: " + SQL
				+ " - " + e1.getMessage() + ".");
		}
	}
	private void clearPostingFlag(Connection conn) throws Exception{
		try{
			String SQL = "UPDATE " + SMTableapoptions.TableName 
			+ " SET " + SMTableapoptions.ibatchpostinginprocess + " = 0"
			+ ", " + SMTableapoptions.datstartdate + " = '0000-00-00 00:00:00'"
			+ ", " + SMTableapoptions.sprocess + " = ''"
			+ ", " + SMTableapoptions.luserid + " = 0"
			;
			if (!clsDatabaseFunctions.executeSQL(SQL, conn)){
				throw new Exception("Error [1506451870] clearing posting flag in apoptions");
			}
		}catch (SQLException e){
			throw new Exception("Error [1506451871] clearing posting flag in apoptions - " + e.getMessage());
		}
	}
	private void checkAndSetAPPostingFlag(Connection conn) throws Exception{
		//First check to make sure no one else is posting:
		try{
			String SQL = "SELECT * FROM " + SMTableapoptions.TableName;
			ResultSet rsAPOptions = clsDatabaseFunctions.openResultSet(SQL, conn);
			if (!rsAPOptions.next()){
				throw new Exception("Error getting aroptions record - no record found.");
			}else{
				if(rsAPOptions.getLong(SMTableapoptions.ibatchpostinginprocess) == 1){
					throw new Exception("A previous posting is not completed - "
						+ SMUtilities.getFullNamebyUserID(rsAPOptions.getString(SMTableapoptions.luserid), conn) + " has been "
						+ rsAPOptions.getString(SMTableapoptions.sprocess) + " "
						+ "since " + rsAPOptions.getString(SMTableapoptions.datstartdate) + "."
					);
				}
			}
			rsAPOptions.close();
		}catch (Exception e){
			throw new Exception("Error [1506451720] checking for previous posting - " + e.getMessage());
		}
		//If not, then set the posting flag:
		try{
			String SQL = "UPDATE " + SMTableapoptions.TableName 
			+ " SET " + SMTableapoptions.ibatchpostinginprocess + " = 1"
			+ ", " + SMTableapoptions.datstartdate + " = NOW()"
			+ ", " + SMTableapoptions.sprocess + " = 'CLEARING AP TRANSACTIONS'"
			+ ", " + SMTableapoptions.luserid + " = " + sUserID + ""
			;
			Statement stmt = conn.createStatement();
			stmt.execute(SQL);
		}catch (SQLException e){
			throw new Exception("Error [1506451720] setting posting flag in apoptions - " + e.getMessage());
		}
	}
	public void doGet(HttpServletRequest request,
			HttpServletResponse response)
	throws ServletException, IOException {

		doPost(request, response);
	}
}
