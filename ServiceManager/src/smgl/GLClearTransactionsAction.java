package smgl;

import java.io.IOException;
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
import SMDataDefinition.SMTablegloptions;
import SMDataDefinition.SMTablegltransactionbatches;
import SMDataDefinition.SMTablegltransactionlines;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsDateAndTimeConversions;
import ServletUtilities.clsManageRequestParameters;
import smcontrolpanel.SMAuthenticate;
import smcontrolpanel.SMSystemFunctions;
import smcontrolpanel.SMUtilities;

public class GLClearTransactionsAction extends HttpServlet{

	private static final long serialVersionUID = 1L;
	public void doPost(HttpServletRequest request,
			HttpServletResponse response)
	throws ServletException, IOException {

		String sCallingClass = clsManageRequestParameters.get_Request_Parameter("CallingClass", request);
		String sClearingDate = clsManageRequestParameters.get_Request_Parameter(GLClearTransactionsSelect.CLEARING_DATE, request);

	    if (!SMAuthenticate.authenticateSMCPCredentials(request, response, getServletContext(), SMSystemFunctions.GLClearTransactions)){
	    	return;
	    }
		//Get the session info:
		HttpSession CurrentSession = request.getSession(true);
		String sDBID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_DATABASE_ID);
		String sUserID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERID);

		if (clsManageRequestParameters.get_Request_Parameter(GLClearTransactionsSelect.CONFIRM_CLEARING_CHECKBOX, request).compareToIgnoreCase("") == 0){
			String sWarning = "You chose to clear, but did not check the box to confirm.";
			response.sendRedirect(
					"" + SMUtilities.getURLLinkBase(getServletContext()) + "" + sCallingClass + "?"
					+ "" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
					+ "&Warning=" + sWarning
			);
			return;
		}
		
		try {
			clearTransactions(sDBID, sClearingDate, sUserID);
		} catch (Exception e) {
			response.sendRedirect(
				"" + SMUtilities.getURLLinkBase(getServletContext()) + "" + sCallingClass + "?"
				+ "" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
				+ "&Warning=" + e.getMessage()
			);
			return;
		}
		
		String sStatus = "GL transactions through " 
			+ sClearingDate + " were cleared.";
		response.sendRedirect(
				"" + SMUtilities.getURLLinkBase(getServletContext()) + "" + sCallingClass + "?"
				+ "" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
				+ "&Status=" + sStatus
		);
		return;
	}
	private void clearTransactions(String sDBID, String sClearingDate, String sUserID) throws Exception{
		
		if (!clsDateAndTimeConversions.IsValidDateString("M/d/yyyy", sClearingDate)){
			throw new Exception("Invalid clearing date - '" + sClearingDate + "'.");
		}
		//Convert the date to a SQL one:
		java.sql.Date datClearingDate = null;
		try {
			datClearingDate = clsDateAndTimeConversions.StringTojavaSQLDate("M/d/yyyy", sClearingDate);
		} catch (ParseException e) {
			throw new Exception("Error:[1556721359] Invalid clearing date '" + sClearingDate + "' - " + e.getMessage());
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
			throw new Exception("Error [1556721360] getting connection - " + e2.getMessage());
		}
		
		try {
			checkAndSetGLPostingFlag (conn, sUserID);
		} catch (Exception e) {
			clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1556721361]");
			throw new Exception("Error [1556721362] checking GL Posting Flag - " + e.getMessage());
		}
		
		//Have to make sure there are NO unposted batches:
		String SQL = "SELECT"
			+ " " + SMTablegltransactionbatches.lbatchnumber
			+ " FROM " + SMTablegltransactionbatches.TableName
			+ " WHERE ("
				+ "(" 
				+ "(" + SMTablegltransactionbatches.ibatchstatus + " = " + Integer.toString(SMBatchStatuses.ENTERED) + ")"
					+ " OR (" + SMTablegltransactionbatches.ibatchstatus + " = " + Integer.toString(SMBatchStatuses.IMPORTED) + ")"
				+ ")"
			+ ")"
			;
		try {
			ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
			if (rs.next()){
				rs.close();
				clearPostingFlag(conn);
				clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1556721363]");
				throw new Exception("There are unposted batches - these must be posted before you can clear transactions.");
			}
			rs.close();
		} catch (SQLException e1) {
			clearPostingFlag(conn);
			clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1556721364]");
			throw new Exception("Error [1556721365] checking for unposted batches - with SQL: " + SQL + " - " + e1.getMessage() + ".");
		}
		
		//Log the process:
		SMLogEntry log = new SMLogEntry(sDBID, getServletContext());
		log.writeEntry(
			sUserID, 
			SMLogEntry.LOG_OPERATION_GLCLEARTRANS, 
			"Attempting to clear GL transactions", 
			"Using clearing date " + sClearingDate,
			"[1556721366]"
		);
		
		if(!clsDatabaseFunctions.start_data_transaction(conn)){
			clearPostingFlag(conn);
			clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1556721367]");
			throw new Exception("Could not start data transaction.");
		}

		try {
			deleteRecords(conn, sClearingDate);
		} catch (Exception e) {
			clsDatabaseFunctions.rollback_data_transaction(conn);
			clearPostingFlag(conn);
			clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1556721368]");
			throw new Exception("Error [1506472208] - Could not delete records - " + e.getMessage());
		}

		if(!clsDatabaseFunctions.commit_data_transaction(conn)){
			clsDatabaseFunctions.rollback_data_transaction(conn);
			clearPostingFlag(conn);
			clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1556721369]");
			throw new Exception("Error [1556721370] - Could not commit data transaction.");
		}

		clearPostingFlag(conn);

		log.writeEntry(
			sUserID, 
			SMLogEntry.LOG_OPERATION_GLCLEARTRANS, 
			"Successfully cleared GL transactions", 
			"Using clearing date " + clsDateAndTimeConversions.utilDateToString(datClearingDate, "MM/dd/yyyy"),
			"[1556721371]");
		
		return;
	}
	private void deleteRecords(Connection conn, String sClearingDate) throws Exception{
		
		//Execute deletes here:
		String SQL = "DELETE FROM " 
			+ SMTablegltransactionlines.TableName
			+ " WHERE ("
			+ "(" + SMTablegltransactionlines.datpostingdate + " <= '"
			+ clsDateAndTimeConversions.convertDateFormat(sClearingDate, SMUtilities.DATE_FORMAT_FOR_DISPLAY, SMUtilities.DATE_FORMAT_FOR_SQL, SMUtilities.EMPTY_DATE_VALUE) + " 23:59:59')"
			+ ")"
			;
		System.out.println("[1556721372] - SQL: '" + SQL + "'");
		try {
			Statement stmt = conn.createStatement();
			stmt.execute(SQL);
		} catch (SQLException e1) {
			throw new Exception(" Error [1556721373] - Could not execute delete GL transactions statement with SQL: " + SQL
				+ " - " + e1.getMessage() + ".");
		}

	}
	private void clearPostingFlag(Connection conn) throws Exception{
		try{
			String SQL = "UPDATE " + SMTablegloptions.TableName 
			+ " SET " + SMTablegloptions.ibatchpostinginprocess + " = 0"
			+ ", " + SMTablegloptions.datstartdate + " = '0000-00-00 00:00:00'"
			+ ", " + SMTablegloptions.sprocess + " = ''"
			+ ", " + SMTablegloptions.luserid + " = 0"
			;
			if (!clsDatabaseFunctions.executeSQL(SQL, conn)){
				throw new Exception("Error [1556721382] clearing posting flag in GL options");
			}
		}catch (SQLException e){
			throw new Exception("Error [1556721383] clearing posting flag in GL options - " + e.getMessage());
		}
	}
	private void checkAndSetGLPostingFlag(Connection conn, String sUserID) throws Exception{
		//First check to make sure no one else is posting:
		try{
			String SQL = "SELECT * FROM " + SMTablegloptions.TableName;
			ResultSet rsGLOptions = clsDatabaseFunctions.openResultSet(SQL, conn);
			if (!rsGLOptions.next()){
				throw new Exception("Error getting GL options record - no record found.");
			}else{
				if(rsGLOptions.getLong(SMTablegloptions.ibatchpostinginprocess) == 1){
					throw new Exception("A previous posting is not completed - "
						+ SMUtilities.getFullNamebyUserID(rsGLOptions.getString(SMTablegloptions.luserid), conn) + " has been "
						+ rsGLOptions.getString(SMTablegloptions.sprocess) + " "
						+ "since " + rsGLOptions.getString(SMTablegloptions.datstartdate) + "."
					);
				}
			}
			rsGLOptions.close();
		}catch (Exception e){
			throw new Exception("Error [1556721384] checking for previous posting - " + e.getMessage());
		}
		//If not, then set the posting flag:
		try{
			String SQL = "UPDATE " + SMTablegloptions.TableName 
			+ " SET " + SMTablegloptions.ibatchpostinginprocess + " = 1"
			+ ", " + SMTablegloptions.datstartdate + " = NOW()"
			+ ", " + SMTablegloptions.sprocess + " = 'CLEARING GL TRANSACTIONS'"
			+ ", " + SMTablegloptions.luserid + " = " + sUserID + ""
			;
			Statement stmt = conn.createStatement();
			stmt.execute(SQL);
		}catch (SQLException e){
			throw new Exception("Error [1556721385] setting posting flag in GL options - " + e.getMessage());
		}
	}
	public void doGet(HttpServletRequest request,
			HttpServletResponse response)
	throws ServletException, IOException {

		doPost(request, response);
	}
}
