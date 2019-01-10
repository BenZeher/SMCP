package smic;

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

import smcontrolpanel.SMAuthenticate;
import smcontrolpanel.SMSystemFunctions;
import smcontrolpanel.SMUtilities;
import SMClasses.SMBatchStatuses;
import SMClasses.SMLogEntry;
import SMDataDefinition.SMTableicinventoryworksheet;
import SMDataDefinition.SMTableicinvoiceexportsequences;
import SMDataDefinition.SMTableicphysicalcountlines;
import SMDataDefinition.SMTableicphysicalcounts;
import SMDataDefinition.SMTableicphysicalinventories;
import SMDataDefinition.SMTableicpoheaders;
import SMDataDefinition.SMTableicpoinvoiceheaders;
import SMDataDefinition.SMTableicpoinvoicelines;
import SMDataDefinition.SMTableicpolines;
import SMDataDefinition.SMTableicporeceiptheaders;
import SMDataDefinition.SMTableicporeceiptlines;
import SMDataDefinition.SMTableictransactiondetails;
import SMDataDefinition.SMTableictransactions;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsDateAndTimeConversions;
import ServletUtilities.clsManageRequestParameters;
import ServletUtilities.clsServletUtilities;

public class ICClearTransactionsAction extends HttpServlet{

	private static final long serialVersionUID = 1L;
	private static String m_sWarning = "";
	private static String sDBID = "";
	private static String sUserID = "";
	private static String sUserFullName = "";
	private static String sSendRedirect = "";
	public void doPost(HttpServletRequest request,
			HttpServletResponse response)
	throws ServletException, IOException {

		if (!SMAuthenticate.authenticateSMCPCredentials(
				request, 
				response, 
				getServletContext(), 
				SMSystemFunctions.ICClearTransactions)){
			return;
		}
		
		String sCallingClass = clsManageRequestParameters.get_Request_Parameter("CallingClass", request);
		String sClearingDate = clsManageRequestParameters.get_Request_Parameter("ClearingDate", request);

		if (!SMAuthenticate.authenticateSMCPCredentials(
				request, 
				response, 
				getServletContext(), 
				SMSystemFunctions.ICClearTransactions))
		{
			return;
		}
		
		//Get the session info:
		HttpSession CurrentSession = request.getSession(true);
		sDBID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_DATABASE_ID);
		sUserID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERID);
		sUserFullName = (String)CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERFIRSTNAME) + " "
						+ (String)CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERLASTNAME);
		//Need a connection for the data transaction:
		Connection conn = clsDatabaseFunctions.getConnection(
				getServletContext(), 
				(String)CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_DATABASE_ID),
				"MySQL",
				this.toString() + ".doPost - User: " 
				+ sUserID
				+ " - "
				+ sUserFullName
				
				);
		if (conn == null){
			m_sWarning = "Unable to get data connection.";
			response.sendRedirect(
					"" + SMUtilities.getURLLinkBase(getServletContext()) + "" + sCallingClass + "?"
					+ "" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
					+ "&Warning=" + m_sWarning
			);			
			return;
		}

		if (!clsDateAndTimeConversions.IsValidDateString("M/d/yyyy", sClearingDate)){
			clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1547080800]");
			m_sWarning = "Invalid clearing date.";
			response.sendRedirect(
					"" + SMUtilities.getURLLinkBase(getServletContext()) + "" + sCallingClass + "?"
					+ "" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
					+ "&Warning=" + m_sWarning
			);
			return;
		}
		//Convert the date to a SQL one:
		java.sql.Date datClearingDate = null;
		try {
			datClearingDate = clsDateAndTimeConversions.StringTojavaSQLDate("M/d/yyyy", sClearingDate);
		} catch (ParseException e) {
			m_sWarning = "Error:[1423767071] Invalid clearing date: '" + sClearingDate + "' - " + e.getMessage();
			response.sendRedirect(
					"" + SMUtilities.getURLLinkBase(getServletContext()) + "" + sCallingClass + "?"
					+ "" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
					+ "&Warning=" + m_sWarning
			);
			return;
		}
		java.sql.Date datNow =  clsDateAndTimeConversions.nowAsSQLDate();
		if (datClearingDate.after(datNow)){
			clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1547080801]");
			m_sWarning = "Invalid clearing date - you cannot choose a date later than today.";
			response.sendRedirect(
					"" + SMUtilities.getURLLinkBase(getServletContext()) + "" + sCallingClass + "?"
					+ "" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
					+ "&Warning=" + m_sWarning
			);
			return;
		}

		if (request.getParameter("ConfirmClear") == null){
			clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1547080802]");
			m_sWarning = "You chose to clear, but did not check the box to confirm.";
			response.sendRedirect(
					"" + SMUtilities.getURLLinkBase(getServletContext()) + "" + sCallingClass + "?"
					+ "" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
					+ "&Warning=" + m_sWarning
			);
			return;
		}

		//Have to make sure there are NO unposted batches:
		String SQL = "SELECT"
			+ " " + ICEntryBatch.lbatchnumber
			+ " FROM " + ICEntryBatch.TableName
			+ " WHERE ("
				+ "(" 
				+ "(" + ICEntryBatch.ibatchstatus + " = " + Integer.toString(SMBatchStatuses.ENTERED) + ")"
					+ " OR (" + ICEntryBatch.ibatchstatus + " = " + Integer.toString(SMBatchStatuses.IMPORTED) + ")"
				+ ")"
			+ ")"
			;
		try {
			ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
			if (rs.next()){
				rs.close();
				clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1547080803]");
				m_sWarning = "There are unposted batches - these must be posted before you can clear transactions.";
				response.sendRedirect(
						"" + SMUtilities.getURLLinkBase(getServletContext()) + "" + sCallingClass + "?"
						+ "" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
						+ "&Warning=" + m_sWarning
				);
				return;
			}
			rs.close();
		} catch (SQLException e1) {
			clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1547080804]");
			m_sWarning = "Error checking for unposted batches - with SQL: " + SQL + " - " + e1.getMessage() + ".";
			response.sendRedirect(
					"" + SMUtilities.getURLLinkBase(getServletContext()) + "" + sCallingClass + "?"
					+ "" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
					+ "&Warning=" + m_sWarning
			);
			return;		
		}

		//Log the process:
		SMLogEntry log = new SMLogEntry(sDBID, getServletContext());
		log.writeEntry(
				sUserID, 
				SMLogEntry.LOG_OPERATION_ICCLEARTRANS, 
				"Attempting to clear paid transactions", 
				"Using clearing date " + clsDateAndTimeConversions.utilDateToString(datClearingDate, "MM/dd/yyyy"),
				"[1376509375]");

		ICOption icopt = new ICOption();
		try {
			icopt.checkAndUpdatePostingFlagWithoutConnection(
				getServletContext(), 
				(String)CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_DATABASE_ID), 
				clsServletUtilities.getFullClassName(this.toString()) + ".doPost", 
				sUserFullName, 
				"CLEARING IC TRANSACTIONS");
		} catch (Exception e1) {
			response.sendRedirect(
				"" + SMUtilities.getURLLinkBase(getServletContext()) + "" + sCallingClass + "?"
				+ "" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
				+ "&Warning=" + e1.getMessage()
			);
			return;
		}
		clearTransactionProcess(conn,sCallingClass, datClearingDate, log);
		try {
			icopt.resetPostingFlagWithoutConnection(getServletContext(), sDBID);
		} catch (Exception e) {
			//We won't stop for this, but the next user will have to clear the IC posting flag
		}
		response.sendRedirect(sSendRedirect);
		return;
	}
	
	public void clearTransactionProcess(Connection conn, String sCallingClass, java.sql.Date datClearingDate, SMLogEntry log){
		if(!clsDatabaseFunctions.start_data_transaction(conn)){
			clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1547080796]");
			m_sWarning = "Could not start data transaction.";
			sSendRedirect = "" + SMUtilities.getURLLinkBase(getServletContext()) + "" + sCallingClass + "?"
					+ "" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
					+ "&Warning=" + m_sWarning;
			return;
		}

		if (!deleteRecords(conn, datClearingDate)){
			clsDatabaseFunctions.rollback_data_transaction(conn);
			clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1547080797]");

			sSendRedirect = "" + SMUtilities.getURLLinkBase(getServletContext()) + "" + sCallingClass + "?"
					+ "" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
					+ "&Warning=" + m_sWarning;
			return;
		}

		if(!clsDatabaseFunctions.commit_data_transaction(conn)){
			clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1547080798]");
			m_sWarning = "Could not commit data transaction.";
			sSendRedirect = "" + SMUtilities.getURLLinkBase(getServletContext()) + "" + sCallingClass + "?"
					+ "" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
					+ "&Warning=" + m_sWarning;
			return;
		}

		log.writeEntry(
				sUserID, 
				SMLogEntry.LOG_OPERATION_ICCLEARTRANS, 
				"Successfully cleared IC transactions", 
				"Using clearing date " + clsDateAndTimeConversions.utilDateToString(datClearingDate, "MM/dd/yyyy"),
				"[1376509376]");

		clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1547080799]");
		String sStatus = "Completed and deleted transactions through " 
			+ clsDateAndTimeConversions.utilDateToString(datClearingDate, "MM-dd-yyyy") + " were cleared.";
		sSendRedirect = "" + SMUtilities.getURLLinkBase(getServletContext()) + "" + sCallingClass + "?"
				+ "" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
				+ "&Status=" + sStatus;
		return;
	}
	private boolean deleteRecords(Connection conn, java.sql.Date datClearingDate){
		
		/*
		 * Designed to clear these tables:
		 * 
		x icinventoryworksheet
		x icinvoiceexportsequences
		x icphysicalcountlines
		x icphysicalcounts
		x icphysicalinventories
		X icpoheaders
		x icpoinvoiceheaders
		x icpoinvoicelines
		x icpolines
		x icporeceiptheaders
		x icporeceiptlines
		x ictransactiondetails
		x ictransactions
		 */
		
		//Execute deletes here:
		//Delete physical inventories:
		String SQL = "DELETE " 
			+ SMTableicphysicalinventories.TableName + " FROM " + SMTableicphysicalinventories.TableName
			+ " where ("
				+ "("
					+ "(" + SMTableicphysicalinventories.istatus + " = " 
						+ SMTableicphysicalinventories.STATUS_DELETED + ")"
					+ " OR (" + SMTableicphysicalinventories.istatus + " = " 
						+ SMTableicphysicalinventories.STATUS_BATCHED + ")"
				+ ")"
				+ " AND (" + SMTableicphysicalinventories.datcreated + " <= '"
				+ clsDateAndTimeConversions.utilDateToString(datClearingDate, "yyyy-MM-dd") + " 23:59:59')"
			+ ")"
			;
		try {
			Statement stmt = conn.createStatement();
			stmt.execute(SQL);
		} catch (SQLException e1) {
			m_sWarning = "Could not delete physical inventories with SQL: " + SQL
			+ " - " + e1.getMessage() + ".";
			return false;
		}

		//Delete inventory worksheets:
		SQL = "DELETE " + SMTableicinventoryworksheet.TableName
		+ " FROM ("
		+ SMTableicinventoryworksheet.TableName
		+ " LEFT JOIN " + SMTableicphysicalinventories.TableName
		+ " ON " + SMTableicinventoryworksheet.TableName + "." + SMTableicinventoryworksheet.lphysicalinventoryid
		+ " = " + SMTableicphysicalinventories.TableName + "." + SMTableicphysicalinventories.lid
		+ ")"
		+ " WHERE ((" + SMTableicphysicalinventories.TableName + "." + SMTableicphysicalinventories.lid 
			+ ") Is Null)"
		;
		try {
			Statement stmt = conn.createStatement();
			stmt.execute(SQL);
		} catch (SQLException e1) {
			m_sWarning = "Could not delete inventory worksheets statement with SQL: " + SQL
			+ " - " + e1.getMessage() + ".";
			return false;
		}

		//Delete icphysicalcounts:
		SQL = "DELETE " + SMTableicphysicalcounts.TableName
		+ " FROM ("
		+ SMTableicphysicalcounts.TableName
		+ " LEFT JOIN " + SMTableicphysicalinventories.TableName
		+ " ON " + SMTableicphysicalcounts.TableName + "." + SMTableicphysicalcounts.lphysicalinventoryid
		+ " = " + SMTableicphysicalinventories.TableName + "." + SMTableicphysicalinventories.lid
		+ ")"
		+ " WHERE ((" + SMTableicphysicalinventories.TableName + "." + SMTableicphysicalinventories.lid 
			+ ") Is Null)"
		;
		try {
			Statement stmt = conn.createStatement();
			stmt.execute(SQL);
		} catch (SQLException e1) {
			m_sWarning = "Could not delete icphysicalcounts with SQL: " + SQL
			+ " - " + e1.getMessage() + ".";
			return false;
		}
		
		//Delete icphysicalcountlines:
		SQL = "DELETE " + SMTableicphysicalcountlines.TableName
		+ " FROM ("
		+ SMTableicphysicalcountlines.TableName
		+ " LEFT JOIN " + SMTableicphysicalinventories.TableName
		+ " ON " + SMTableicphysicalcountlines.TableName + "." + SMTableicphysicalcountlines.lphysicalinventoryid
		+ " = " + SMTableicphysicalinventories.TableName + "." + SMTableicphysicalinventories.lid
		+ ")"
		+ " WHERE ((" + SMTableicphysicalinventories.TableName + "." + SMTableicphysicalinventories.lid 
			+ ") Is Null)"
		;
		try {
			Statement stmt = conn.createStatement();
			stmt.execute(SQL);
		} catch (SQLException e1) {
			m_sWarning = "Could not delete icphysicalcountlines with SQL: " + SQL
			+ " - " + e1.getMessage() + ".";
			return false;
		}
		
		//Delete invoice export sequences:
		SQL = "DELETE " 
			+ SMTableicinvoiceexportsequences.TableName + " FROM " + SMTableicinvoiceexportsequences.TableName
			+ " where ("
				+ "(" + SMTableicinvoiceexportsequences.datexported + " <= '"
				+ clsDateAndTimeConversions.utilDateToString(datClearingDate, "yyyy-MM-dd") + " 23:59:59')"
			+ ")"
			;
		try {
			Statement stmt = conn.createStatement();
			stmt.execute(SQL);
		} catch (SQLException e1) {
			m_sWarning = "Could not delete icinvoiceexportsequences with SQL: " + SQL
			+ " - " + e1.getMessage() + ".";
			return false;
		}

		//Delete PO invoice headers:
		SQL = "DELETE " 
			+ SMTableicpoinvoiceheaders.TableName + " FROM " + SMTableicpoinvoiceheaders.TableName
			+ " where ("
				+ "(" + SMTableicpoinvoiceheaders.datinvoice + " <= '"
				+ clsDateAndTimeConversions.utilDateToString(datClearingDate, "yyyy-MM-dd") + " 23:59:59')"
				+ " AND (" + SMTableicpoinvoiceheaders.lexportsequencenumber + " > 0 )"
			+ ")"
			;
		try {
			Statement stmt = conn.createStatement();
			stmt.execute(SQL);
		} catch (SQLException e1) {
			m_sWarning = "Could not delete icpoinvoiceheaders with SQL: " + SQL
			+ " - " + e1.getMessage() + ".";
			return false;
		}

		//Delete po invoice lines:
		SQL = "DELETE " + SMTableicpoinvoicelines.TableName
		+ " FROM ("
		+ SMTableicpoinvoicelines.TableName
		+ " LEFT JOIN " + SMTableicpoinvoiceheaders.TableName
		+ " ON " + SMTableicpoinvoicelines.TableName + "." + SMTableicpoinvoicelines.lpoinvoiceheaderid
		+ " = " + SMTableicpoinvoiceheaders.TableName + "." + SMTableicpoinvoiceheaders.lid
		+ ")"
		+ " WHERE ((" + SMTableicpoinvoiceheaders.TableName + "." + SMTableicpoinvoiceheaders.lid 
			+ ") Is Null)"
		;
		try {
			Statement stmt = conn.createStatement();
			stmt.execute(SQL);
		} catch (SQLException e1) {
			m_sWarning = "Could not delete icpoinvoicelines with SQL: " + SQL
			+ " - " + e1.getMessage() + ".";
			return false;
		}
		
		//Delete po receipt headers:
		SQL = "DELETE " + SMTableicporeceiptheaders.TableName
		+ " FROM "
		+ SMTableicporeceiptheaders.TableName
		//Delete only receipts with NO uninvoiced lines:
		+ "  LEFT JOIN "
		+ " (SELECT "
		+ "SUM(IF(" + SMTableicporeceiptlines.TableName + "." + SMTableicporeceiptlines.lpoinvoiceid + " = -1, 1, 0)) AS UNINVOICED"
		+ ", " + SMTableicporeceiptlines.TableName + "." + SMTableicporeceiptlines.lreceiptheaderid + " AS RCPTID"
		+ " FROM " + SMTableicporeceiptlines.TableName
		+ " GROUP BY " + SMTableicporeceiptlines.TableName + "." + SMTableicporeceiptlines.lreceiptheaderid
		+ ") AS LINEQUERY ON LINEQUERY.RCPTID = " + SMTableicporeceiptheaders.TableName + "." + SMTableicporeceiptheaders.lid
		
		+ " WHERE ("
			+ "(" + SMTableicporeceiptheaders.TableName + "." + SMTableicporeceiptheaders.datreceived
			+ " <= '" + clsDateAndTimeConversions.utilDateToString(datClearingDate, "yyyy-MM-dd") + " 23:59:59')"
			+ " AND ("
				+ "("
				+ "(" + SMTableicporeceiptheaders.lpostedtoic + " = 1)"
				+ " AND (LINEQUERY.UNINVOICED = 0)"
				+ ")"
				+ " OR (" + SMTableicporeceiptheaders.lstatus + " = " 
					+ Long.toString(SMTableicporeceiptheaders.STATUS_DELETED) + ")"
			+ ")"
		+ ")"
		;
		try {
			Statement stmt = conn.createStatement();
			stmt.execute(SQL);
		} catch (SQLException e1) {
			m_sWarning = "Could not delete icporeceiptheaders with SQL: " + SQL
			+ " - " + e1.getMessage() + ".";
			return false;
		}
		 
		//Delete icporeceiptlines:
		SQL = "DELETE " + SMTableicporeceiptlines.TableName
		+ " FROM ("
		+ SMTableicporeceiptlines.TableName
		+ " LEFT JOIN " + SMTableicporeceiptheaders.TableName
		+ " ON " + SMTableicporeceiptlines.TableName + "." + SMTableicporeceiptlines.lreceiptheaderid
		+ " = " + SMTableicporeceiptheaders.TableName + "." + SMTableicporeceiptheaders.lid
		+ ")"
		+ " WHERE ((" + SMTableicporeceiptheaders.TableName + "." + SMTableicporeceiptheaders.lid 
			+ ") Is Null)"
		;
		try {
			Statement stmt = conn.createStatement();
			stmt.execute(SQL);
		} catch (SQLException e1) {
			m_sWarning = "Could not delete icporeceiptlines with SQL: " + SQL
			+ " - " + e1.getMessage() + ".";
			return false;
		}
		
		//Delete PO headers:
		SQL = "DELETE " 
			+ SMTableicpoheaders.TableName + " FROM " + SMTableicpoheaders.TableName
			+ " LEFT JOIN " + SMTableicporeceiptheaders.TableName
			+ " ON " + SMTableicpoheaders.TableName + "." + SMTableicpoheaders.lid + " = "
			+ SMTableicporeceiptheaders.TableName + "." + SMTableicporeceiptheaders.lpoheaderid
			+ " where ("
				+ "(" + SMTableicpoheaders.datpodate + " <= '"
				+ clsDateAndTimeConversions.utilDateToString(datClearingDate, "yyyy-MM-dd") + " 23:59:59')"
				+ " AND (" 
					+ "(" + SMTableicpoheaders.TableName + "." + SMTableicpoheaders.lstatus + " = " 
						+ Integer.toString(SMTableicpoheaders.STATUS_COMPLETE) + ")"
					+ " OR (" + SMTableicpoheaders.TableName + "." + SMTableicpoheaders.lstatus + " = " 
					+ Integer.toString(SMTableicpoheaders.STATUS_DELETED) + ")"
				+ ")"
				+ " AND ((" + SMTableicporeceiptheaders.TableName + "." + SMTableicporeceiptheaders.lid
					+ ") Is Null)"
			+ ")"
			;
		try {
			Statement stmt = conn.createStatement();
			stmt.execute(SQL);
		} catch (SQLException e1) {
			m_sWarning = "Could not delete icpoheaders with SQL: " + SQL
			+ " - " + e1.getMessage() + ".";
			return false;
		}
		
		//Delete po lines:
		SQL = "DELETE " + SMTableicpolines.TableName
		+ " FROM ("
		+ SMTableicpolines.TableName
		+ " LEFT JOIN " + SMTableicpoheaders.TableName
		+ " ON " + SMTableicpolines.TableName + "." + SMTableicpolines.lpoheaderid
		+ " = " + SMTableicpoheaders.TableName + "." + SMTableicpoheaders.lid
		+ ")"
		+ " WHERE ((" + SMTableicpoheaders.TableName + "." + SMTableicpoheaders.lid 
			+ ") Is Null)"
		;
		try {
			Statement stmt = conn.createStatement();
			stmt.execute(SQL);
		} catch (SQLException e1) {
			m_sWarning = "Could not delete icpolines with SQL: " + SQL
			+ " - " + e1.getMessage() + ".";
			return false;
		}	
		
		//Delete ictransactions:
		SQL = "DELETE " 
			+ SMTableictransactions.TableName + " FROM " + SMTableictransactions.TableName
			+ " where ("
				+ "(" + SMTableictransactions.datpostingdate + " <= '"
				+ clsDateAndTimeConversions.utilDateToString(datClearingDate, "yyyy-MM-dd") + " 23:59:59')"
			+ ")"
			;
		try {
			Statement stmt = conn.createStatement();
			stmt.execute(SQL);
		} catch (SQLException e1) {
			m_sWarning = "Could not delete ictransactions with SQL: " + SQL
			+ " - " + e1.getMessage() + ".";
			return false;
		}
		
		//Delete ictransactionlines:
		SQL = "DELETE " + SMTableictransactiondetails.TableName
		+ " FROM ("
		+ SMTableictransactiondetails.TableName
		+ " LEFT JOIN " + SMTableictransactions.TableName
		+ " ON " + SMTableictransactiondetails.TableName + "." + SMTableictransactiondetails.ltransactionid
		+ " = " + SMTableictransactions.TableName + "." + SMTableictransactions.lid
		+ ")"
		+ " WHERE ((" + SMTableictransactions.TableName + "." + SMTableictransactions.lid 
			+ ") Is Null)"
		;
		try {
			Statement stmt = conn.createStatement();
			stmt.execute(SQL);
		} catch (SQLException e1) {
			m_sWarning = "Could not delete ictransactionlines with SQL: " + SQL
			+ " - " + e1.getMessage() + ".";
			return false;
		}
		
		return true;
	}

	public void doGet(HttpServletRequest request,
			HttpServletResponse response)
	throws ServletException, IOException {

		doPost(request, response);
	}
//	@Override
//	public void run() {
//		String sURL = "localhost";
//		String sDBID = "servmgr1"; //servmgr1 - default
//		String sConnString = "jdbc:mysql://" + sURL + ":3306/" + sDBID + "?noDatetimeStringSync=true&connectTimeout=28800000&interactiveClient=True";
//		String sUser = "root";
//		String sPassword = "password123";
//		Connection conn = null;
//		try{
//			conn = DriverManager.getConnection(sConnString, sUser, sPassword);
//		}catch(Exception e){
//			System.out.println(e);
//		}
//		checkICPostingFlag(conn);
////		System.out.println("DONE");
//	}
}
