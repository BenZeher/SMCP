package smar;

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

import smcontrolpanel.SMAuthenticate;
import smcontrolpanel.SMSystemFunctions;
import smcontrolpanel.SMUtilities;
import SMClasses.SMBatchStatuses;
import SMClasses.SMEntryBatch;
import SMClasses.SMLogEntry;
import SMClasses.SMModuleTypes;
import SMDataDefinition.SMTablearchronlog;
import SMDataDefinition.SMTablearmatchingline;
import SMDataDefinition.SMTablearoptions;
import SMDataDefinition.SMTableartransactions;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsDateAndTimeConversions;
import ServletUtilities.clsManageRequestParameters;

public class ARClearPaidTransactionsAction extends HttpServlet{

	private static final long serialVersionUID = 1L;
	public void doPost(HttpServletRequest request,
			HttpServletResponse response)
	throws ServletException, IOException {

		String sCallingClass = clsManageRequestParameters.get_Request_Parameter("CallingClass", request);
		String sClearingDate = clsManageRequestParameters.get_Request_Parameter("ClearingDate", request);

	    if (!SMAuthenticate.authenticateSMCPCredentials(request, response, getServletContext(), SMSystemFunctions.ARClearfullypaidtransactions)){
	    	return;
	    }
		//Get the session info:
		HttpSession CurrentSession = request.getSession(true);
		String sDBID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_DATABASE_ID);
		String sUserID = (String)CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERID);
		String sUserFullName = (String)CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERFIRSTNAME)
				+ " " + (String)CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERLASTNAME);
		String m_sWarning = "";
		//Need a connection for the data transaction:
		Connection conn = clsDatabaseFunctions.getConnection(
				getServletContext(), 
				(String)CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_DATABASE_ID),
				"MySQL",
				this.toString() 
				+ ".doPost - User: " 
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
			clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1547067503]");
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
			m_sWarning = "Error:[1423836375] Invalid clearing date '" + sClearingDate + "' - " + e.getMessage();
			response.sendRedirect(
					"" + SMUtilities.getURLLinkBase(getServletContext()) + "" + sCallingClass + "?"
					+ "" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
					+ "&Warning=" + m_sWarning
			);
			return;
		}
		java.sql.Date datNow =  clsDateAndTimeConversions.nowAsSQLDate();
		if (datClearingDate.after(datNow)){
			clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1547067504]");
			m_sWarning = "Invalid clearing date - you cannot choose a date later than today.";
			response.sendRedirect(
					"" + SMUtilities.getURLLinkBase(getServletContext()) + "" + sCallingClass + "?"
					+ "" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
					+ "&Warning=" + m_sWarning
			);
			return;
		}

		if (request.getParameter("ConfirmClear") == null){
			clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1547067505]");
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
			+ " " + SMEntryBatch.ibatchnumber
			+ " FROM " + SMEntryBatch.TableName
			+ " WHERE ("
				+ "(" 
				+ "(" + SMEntryBatch.ibatchstatus + " = " + Integer.toString(SMBatchStatuses.ENTERED) + ")"
					+ " OR (" + SMEntryBatch.ibatchstatus + " = " + Integer.toString(SMBatchStatuses.IMPORTED) + ")"
				+ ")"
			
				+ " AND (" + SMEntryBatch.ibatchtype + " = '" + SMModuleTypes.AR + "')"
			+ ")"
			;
		try {
			ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
			if (rs.next()){
				rs.close();
				clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1547067506]");
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
			clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1547067507]");
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
				SMLogEntry.LOG_OPERATION_ARCLEARPAIDTRANS, 
				"Attempting to clear paid transactions", 
				"Using clearing date " + clsDateAndTimeConversions.utilDateToString(datClearingDate, "MM/dd/yyyy"),
				"[1376509269]");

		try {
			checkARPostingFlag(conn, sUserFullName);
		} catch (Exception e2) {
			clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1547067508]");
			response.sendRedirect(
					"" + SMUtilities.getURLLinkBase(getServletContext()) + "" + sCallingClass + "?"
					+ "" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
					+ "&Warning=" + e2.getMessage()
			);
			return;
		}
		
		if(!clsDatabaseFunctions.start_data_transaction(conn)){
			try {
				clearPostingFlag(conn);
			} catch (Exception e) {
				//Have to just throw this away
			}
			clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1547067509]");
			m_sWarning = "Could not start data transaction.";
			response.sendRedirect(
					"" + SMUtilities.getURLLinkBase(getServletContext()) + "" + sCallingClass + "?"
					+ "" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
					+ "&Warning=" + m_sWarning
			);
			return;
		}

		try {
			deleteRecords(conn, datClearingDate);
		} catch (Exception e) {
			try {
				clearPostingFlag(conn);
			} catch (Exception e1) {
				//Have to just throw this away
			}
			clsDatabaseFunctions.rollback_data_transaction(conn);
			try {
				clearPostingFlag(conn);
			} catch (Exception e1) {
				//Have to just throw this away
			}
			clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1547067510]");
			response.sendRedirect(
					"" + SMUtilities.getURLLinkBase(getServletContext()) + "" + sCallingClass + "?"
					+ "" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
					+ "&Warning=" + e.getMessage()
			);
			return;

		}
		
		if(!clsDatabaseFunctions.commit_data_transaction(conn)){
			try {
				clearPostingFlag(conn);
			} catch (Exception e) {
				//Have to just throw this away
			}
			clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1547067511]");
			m_sWarning = "Could not commit data transaction.";
			response.sendRedirect(
					"" + SMUtilities.getURLLinkBase(getServletContext()) + "" + sCallingClass + "?"
					+ "" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
					+ "&Warning=" + m_sWarning
			);
			return;
		}

		try {
			clearPostingFlag(conn);
		} catch (Exception e) {
			//Have to just throw this away
		}

		log.writeEntry(
				sUserID, 
				SMLogEntry.LOG_OPERATION_ARCLEARPAIDTRANS, 
				"Successfully cleared paid transactions", 
				"Using clearing date " + clsDateAndTimeConversions.utilDateToString(datClearingDate, "MM/dd/yyyy"),
				"[1376509515]");

		clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1547067512]");
		m_sWarning = "Fully paid transactions through " 
			+ clsDateAndTimeConversions.utilDateToString(datClearingDate, "MM-dd-yyyy") + " were cleared.";
		response.sendRedirect(
				"" + SMUtilities.getURLLinkBase(getServletContext()) + "" + sCallingClass + "?"
				+ "" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
				+ "&Warning=" + m_sWarning
		);
		return;
	}
	private void deleteRecords(Connection conn, java.sql.Date datClearingDate) throws Exception{
		//Execute deletes here:
		String SQL = "delete " 
			+ SMTableartransactions.TableName + " FROM " + SMTableartransactions.TableName
			+ " where ("
			+ "(" + SMTableartransactions.dcurrentamt + " = 0.00)"
			+ " AND (" + SMTableartransactions.datdocdate + " <= '"
			+ clsDateAndTimeConversions.utilDateToString(datClearingDate, "yyyy-MM-dd") + " 23:59:59')"
			+ ")"
			;
		try {
			Statement stmt = conn.createStatement();
			stmt.execute(SQL);
		} catch (SQLException e1) {
			throw new Exception("Error [1548693700] - Could not execute delete artransactions statement with SQL: " + SQL
			+ " - " + e1.getMessage() + ".");
		}

		//This statement deletes matching lines AS LONG AS their apply-to transaction is gone
		// It doesn't care if their parent transaction is gone or not:
		SQL = "DELETE " + SMTablearmatchingline.TableName 
		+ " FROM ("
		+ SMTablearmatchingline.TableName
		+ " LEFT JOIN " + SMTableartransactions.TableName + " AS appliedto"
		+ " ON " + SMTablearmatchingline.TableName + "." + SMTablearmatchingline.ldocappliedtoid
		+ " =  appliedto." + SMTableartransactions.lid
		+ ")"
		+ " WHERE ((appliedto." + SMTableartransactions.lid + ") Is Null)"
		;

		try {
			Statement stmt = conn.createStatement();
			stmt.execute(SQL);
		} catch (SQLException e1) {
			throw new Exception("Error [1548693701] - Could not execute delete armatchinglines statement with SQL: " + SQL
			+ " - " + e1.getMessage() + ".");
		}

		//Remove all archronlog entries for deleted transactions here:
		SQL = "DELETE " + SMTablearchronlog.TableName
		+ " FROM ("
		+ SMTablearchronlog.TableName
		+ " LEFT JOIN " + SMTableartransactions.TableName + " AS appliedto"
		+ " ON (" 
		+ "(" + SMTablearchronlog.TableName + "." + SMTablearchronlog.spayeepayor
		+ " =  appliedto." + SMTableartransactions.spayeepayor + ")"
		+ " AND ("+ SMTablearchronlog.TableName + "." + SMTablearchronlog.sapplytodoc
		+ " =  appliedto." + SMTableartransactions.sdocnumber + ")"
		+ ")"
		+ ")"
		+ " WHERE ((appliedto." + SMTableartransactions.lid + ") Is Null)"
		;
		
		try {
			Statement stmt = conn.createStatement();
			stmt.execute(SQL);
		} catch (SQLException e1) {
			throw new Exception("Error [1548693702] - Could not execute delete archronlog statement with SQL: " + SQL
			+ " - " + e1.getMessage() + ".");
		}		
		return;
	}
	private void clearPostingFlag(Connection conn) throws Exception{
		try{
			String SQL = "UPDATE " + SMTablearoptions.TableName 
			+ " SET " + SMTablearoptions.ibatchpostinginprocess + " = 0"
			+ ", " + SMTablearoptions.datstartdate + " = '0000-00-00 00:00:00'"
			+ ", " + SMTablearoptions.sprocess + " = ''"
			+ ", " + SMTablearoptions.suserfullname + " = ''"
			;
			if (!clsDatabaseFunctions.executeSQL(SQL, conn)){
				throw new Exception("Error [1548694031] could not execute SQL command: '" + SQL + "'.");
			}
		}catch (SQLException e){
			throw new Exception("Error [1548694032] clearing posting flag in aroptions - " + e.getMessage());
		}
	}
	private void checkARPostingFlag(Connection conn, String sUserFullName) throws Exception{
		//First check to make sure no one else is posting:
		try{
			String SQL = "SELECT * FROM " + SMTablearoptions.TableName;
			ResultSet rsAROptions = clsDatabaseFunctions.openResultSet(SQL, conn);
			if (!rsAROptions.next()){
				throw new Exception("No aroptions record found");
			}else{
				if(rsAROptions.getLong(SMTablearoptions.ibatchpostinginprocess) == 1){
					throw new Exception("A previous posting is not completed - "
						+ rsAROptions.getString(SMTablearoptions.suserfullname) + " has been "
						+ rsAROptions.getString(SMTablearoptions.sprocess) + " "
						+ "since " + rsAROptions.getString(SMTablearoptions.datstartdate) + "."
						);
				}
			}
			rsAROptions.close();
		}catch (SQLException e){
			throw new Exception("Error [1548694290] checking for previous posting - " + e.getMessage());
		}
		//If not, then set the posting flag:
		try{
			String SQL = "UPDATE " + SMTablearoptions.TableName 
			+ " SET " + SMTablearoptions.ibatchpostinginprocess + " = 1"
			+ ", " + SMTablearoptions.datstartdate + " = NOW()"
			+ ", " + SMTablearoptions.sprocess 
			+ " = 'CLEARING AR TRANSACTIONS'"
			+ ", " + SMTablearoptions.suserfullname + " = '" + sUserFullName + "'"
			;
			Statement stmt = conn.createStatement();
			stmt.execute(SQL);
		}catch (SQLException e){
			throw new Exception("Error [1548694291] setting posting flag in aroptions - " + e.getMessage());
		}
		return;
	}
	public void doGet(HttpServletRequest request,
			HttpServletResponse response)
	throws ServletException, IOException {

		doPost(request, response);
	}
}
