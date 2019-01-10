package smcontrolpanel;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import SMClasses.SMLogEntry;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsManageRequestParameters;

public class SMExecuteSQLAction extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static String sDBID = "";
	private static String sUserID = "";
	public void doPost(HttpServletRequest request,
				HttpServletResponse response)
				throws ServletException, IOException {

	    response.setContentType("text/html");
		//PrintWriter out = response.getWriter();
	    //Get the session info:
	    HttpSession CurrentSession = request.getSession(true);
	    sDBID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_DATABASE_ID);
	    sUserID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERID);
	    if (!SMAuthenticate.authenticateSMCPCredentials(request, response, getServletContext(), SMSystemFunctions.SMExecuteSQL)){
	    	return;
	    }
	    
	    //sCallingClass will look like: smar.ARAgedTrialBalanceReport
	    String sCallingClass = clsManageRequestParameters.get_Request_Parameter("CallingClass", request);
	    //String sExecuteString = SMUtilities.URLDecode(SMUtilities.get_Request_Parameter(SMExecuteSQLSelect.PARAM_EXECUTESTRING, request));
	    String sExecuteString = clsManageRequestParameters.get_Request_Parameter(SMExecuteSQLSelect.PARAM_EXECUTESTRING, request);
	    
	    //Try to insert this query into the saved queries table:
	    String sRedirect = "" + SMUtilities.getURLLinkBase(getServletContext()) + "" + sCallingClass
				+ "?" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
				//+ "&" + SMExecuteSQLSelect.PARAM_EXECUTESTRING + "=" + sQueryString
		;
	    
	    int i = -1;
	    try {
			i = executeCommand(sExecuteString, sDBID, sUserID);
		} catch (SQLException e) {
			sRedirect += "&Warning=SQL command failed - " + e.getMessage();
		}

	    if (i > -1){
	    	sRedirect += "&Status=SQL command was executed successfully; " + Integer.toString(i) + " row(s) affected.";
	    }
		response.sendRedirect(response.encodeRedirectURL(sRedirect));
	    return;
	}
	private int executeCommand(String sExecuteString, String sConf, String sUserID) throws SQLException{
		int i = -1;
		Connection conn;
		try {
			conn = clsDatabaseFunctions.getConnection(getServletContext(), sConf, "MySQL", this.toString() + " SQL: " + sExecuteString);
		} catch (Exception e) {
			throw new SQLException(e.getMessage());
		}
		  
		try{		
			Statement stmt = conn.createStatement();
		    i = stmt.executeUpdate(sExecuteString);
		    clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1547080560]");
		}catch (Exception ex) {
			// handle any errors
			SMClasses.SMLogEntry log = new SMClasses.SMLogEntry(sConf, getServletContext());
			log.writeEntry(
					sUserID, 
					SMLogEntry.LOG_OPERATION_SMEXECUTESQL, 
					"[1376503188] EXECUTE SQL FAILED - " + ex.getMessage(),
					"Command: " + sExecuteString,
					"[1376509321]");
			if (conn != null){
				clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1547080559]");
			}
			throw new SQLException(ex.getMessage());
		}

		//Log the execution:
		SMClasses.SMLogEntry log = new SMClasses.SMLogEntry(sConf, getServletContext());
		log.writeEntry(
				sUserID, 
				SMLogEntry.LOG_OPERATION_SMEXECUTESQL, 
				"EXECUTE SQL SUCCEEDED",
				"Command: " + sExecuteString,
				"[1376509322]");
		
		return i;
	}
	public void doGet(HttpServletRequest request,
			HttpServletResponse response)
	throws ServletException, IOException {

		doPost(request, response);
	}
}
