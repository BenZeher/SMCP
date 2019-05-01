package smgl;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import SMClasses.SMBatchStatuses;
import SMClasses.SMLogEntry;
import SMDataDefinition.SMTableglfinancialstatementdata;
import SMDataDefinition.SMTableglfiscalsets;
import SMDataDefinition.SMTablegloptions;
import SMDataDefinition.SMTablegltransactionbatches;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsManageRequestParameters;
import smcontrolpanel.SMAuthenticate;
import smcontrolpanel.SMSystemFunctions;
import smcontrolpanel.SMUtilities;

public class GLClearFiscalDataAction extends HttpServlet{

	private static final long serialVersionUID = 1L;
	public void doPost(HttpServletRequest request,
			HttpServletResponse response)
	throws ServletException, IOException {

		String sCallingClass = clsManageRequestParameters.get_Request_Parameter("CallingClass", request);
		String sClearingFiscalYear = clsManageRequestParameters.get_Request_Parameter(SMTableglfiscalsets.ifiscalyear, request);

	    if (!SMAuthenticate.authenticateSMCPCredentials(request, response, getServletContext(), SMSystemFunctions.GLClearFiscalData)){
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
			clearFiscalData(sDBID, sClearingFiscalYear, sUserID);
		} catch (Exception e) {
			response.sendRedirect(
				"" + SMUtilities.getURLLinkBase(getServletContext()) + "" + sCallingClass + "?"
				+ "" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
				+ "&Warning=" + e.getMessage()
			);
			return;
		}
		
		String sStatus = "GL fiscal data through fiscal year " 
			+ sClearingFiscalYear + " was cleared.";
		response.sendRedirect(
				"" + SMUtilities.getURLLinkBase(getServletContext()) + "" + sCallingClass + "?"
				+ "" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
				+ "&Status=" + sStatus
		);
		return;
	}
	private void clearFiscalData(String sDBID, String sClearingFiscalYear, String sUserID) throws Exception{
		
		//Need a connection for the data transaction:
		Connection conn;
		try {
			conn = clsDatabaseFunctions.getConnectionWithException(getServletContext(), sDBID, "MySQL", SMUtilities.getFullClassName(this.toString()));
		} catch (Exception e2) {
			throw new Exception("Error [1556734110] getting connection - " + e2.getMessage());
		}
		
		try {
			checkAndSetGLPostingFlag (conn, sUserID);
		} catch (Exception e) {
			clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1556734111]");
			throw new Exception("Error [1556734112] checking GL Posting Flag - " + e.getMessage());
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
				clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1556734113]");
				throw new Exception("There are unposted batches - these must be posted before you can clear transactions.");
			}
			rs.close();
		} catch (SQLException e1) {
			clearPostingFlag(conn);
			clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1556734114]");
			throw new Exception("Error [1556734115] checking for unposted batches - with SQL: " + SQL + " - " + e1.getMessage() + ".");
		}
		
		//Log the process:
		SMLogEntry log = new SMLogEntry(sDBID, getServletContext());
		log.writeEntry(
			sUserID, 
			SMLogEntry.LOG_OPERATION_GLCLEARFISCALDATA, 
			"Attempting to clear GL fiscal data", 
			"Using fiscal year " + sClearingFiscalYear,
			"[1556721366]"
		);
		
		if(!clsDatabaseFunctions.start_data_transaction(conn)){
			clearPostingFlag(conn);
			clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1556734116]");
			throw new Exception("Could not start data transaction.");
		}

		try {
			deleteRecords(conn, sClearingFiscalYear);
		} catch (Exception e) {
			clsDatabaseFunctions.rollback_data_transaction(conn);
			clearPostingFlag(conn);
			clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1556734117]");
			throw new Exception("Error [1556734118] - Could not delete records - " + e.getMessage());
		}

		if(!clsDatabaseFunctions.commit_data_transaction(conn)){
			clsDatabaseFunctions.rollback_data_transaction(conn);
			clearPostingFlag(conn);
			clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1556734119]");
			throw new Exception("Error [1556734120] - Could not commit data transaction.");
		}

		clearPostingFlag(conn);

		log.writeEntry(
			sUserID, 
			SMLogEntry.LOG_OPERATION_GLCLEARFISCALDATA, 
			"Successfully cleared GL fiscal data", 
			"Using fiscal year " + sClearingFiscalYear,
			"[1556734121]");
		
		return;
	}
	private void deleteRecords(Connection conn, String sClearingFiscalYear) throws Exception{
		
		//Execute deletes here:
		String SQL = "DELETE FROM " 
			+ SMTableglfiscalsets.TableName
			+ " WHERE ("
			+ "(" + SMTableglfiscalsets.ifiscalyear + " <= "
			+ sClearingFiscalYear + ")"
			+ ")"
			;
		//System.out.println("[1556734122] - SQL: '" + SQL + "'");
		try {
			Statement stmt = conn.createStatement();
			stmt.execute(SQL);
		} catch (SQLException e1) {
			throw new Exception(" Error [1556734123] - Could not execute delete GL fiscal sets with SQL: " + SQL
				+ " - " + e1.getMessage() + ".");
		}
		
		SQL = "DELETE FROM " 
			+ SMTableglfinancialstatementdata.TableName
			+ " WHERE ("
			+ "(" + SMTableglfinancialstatementdata.ifiscalyear + " <= "
			+ sClearingFiscalYear + ")"
			+ ")"
			;
		//System.out.println("[1556734122] - SQL: '" + SQL + "'");
		try {
			Statement stmt = conn.createStatement();
			stmt.execute(SQL);
		} catch (SQLException e1) {
			throw new Exception(" Error [1556734123] - Could not execute delete GL financial statement data with SQL: " + SQL
				+ " - " + e1.getMessage() + ".");
		}
		
		return;
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
				throw new Exception("Error [1556734124] clearing posting flag in GL options");
			}
		}catch (SQLException e){
			throw new Exception("Error [1556734125] clearing posting flag in GL options - " + e.getMessage());
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
			throw new Exception("Error [1556734126] checking for previous posting - " + e.getMessage());
		}
		//If not, then set the posting flag:
		try{
			String SQL = "UPDATE " + SMTablegloptions.TableName 
			+ " SET " + SMTablegloptions.ibatchpostinginprocess + " = 1"
			+ ", " + SMTablegloptions.datstartdate + " = NOW()"
			+ ", " + SMTablegloptions.sprocess + " = 'CLEARING GL FISCAL DATA'"
			+ ", " + SMTablegloptions.luserid + " = " + sUserID + ""
			;
			Statement stmt = conn.createStatement();
			stmt.execute(SQL);
		}catch (SQLException e){
			throw new Exception("Error [1556734127] setting posting flag in GL options - " + e.getMessage());
		}
	}
	public void doGet(HttpServletRequest request,
			HttpServletResponse response)
	throws ServletException, IOException {

		doPost(request, response);
	}
}
