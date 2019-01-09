package smap;

import java.io.IOException;
//import java.io.PrintWriter;
import java.sql.Connection;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import SMClasses.SMLogEntry;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsManageRequestParameters;
import smcontrolpanel.SMAuthenticate;
import smcontrolpanel.SMSystemFunctions;
import smcontrolpanel.SMUtilities;

public class APVendorNumberChangeAction extends HttpServlet{

	private static final long serialVersionUID = 1L;
	private static String m_sWarning = "";
	private static String sUserName = "";
	private static String sUserID = "";
	private static String sDBID = "";
	public void doPost(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
	
		String sCallingClass = clsManageRequestParameters.get_Request_Parameter("CallingClass", request);
	
	    if (!SMAuthenticate.authenticateSMCPCredentials(request, response, getServletContext(), SMSystemFunctions.APChangeOrMergeVendorAccounts)){
	    	return;
	    }

	    //Get the session info:
	    HttpSession CurrentSession = request.getSession(true);
	    sUserName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERNAME);
	    sUserID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERID);
	    sDBID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_DATABASE_ID);
	    
	    if ((request.getParameter(APVendorNumberChangeSelect.PROCESS_TYPE) == null)){
	    	m_sWarning = "You must choose either CHANGE or MERGE for this function.";
			response.sendRedirect(
					"" + SMUtilities.getURLLinkBase(getServletContext()) + "" + sCallingClass + "?"
					+ "Warning=" + m_sWarning
					+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
			);
			return;
	    }
		
	    boolean bIsMerge = false;
	    if ((request.getParameter(APVendorNumberChangeSelect.PROCESS_TYPE).compareToIgnoreCase(APVendorNumberChangeSelect.VENDOR_MERGE) == 0)){
	    	bIsMerge = true;
	    }
	    
	    if (request.getParameter(APVendorNumberChangeSelect.CONFIRM_CHECKBOX_NAME) == null){
	    	m_sWarning = "You chose to change the vendor account, but did not check the box to confirm.";
			response.sendRedirect(
					"" + SMUtilities.getURLLinkBase(getServletContext()) + "" + sCallingClass + "?"
					+ "Warning=" + m_sWarning
					+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
			);
			return;
	    }
	    
	    if (request.getParameter(APVendorNumberChangeSelect.FROM_VENDOR) == null){
	    	m_sWarning = "The FROM vendor is blank.";
			response.sendRedirect(
					"" + SMUtilities.getURLLinkBase(getServletContext()) + "" + sCallingClass + "?"
					+ "Warning=" + m_sWarning
					+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
			);
			return;
	    }
	    
	    if (request.getParameter(APVendorNumberChangeSelect.TO_VENDOR) == null){
	    	m_sWarning = "The TO vendor is blank.";
			response.sendRedirect(
					"" + SMUtilities.getURLLinkBase(getServletContext()) + "" + sCallingClass + "?"
					+ "Warning=" + m_sWarning
					+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
			);
			return;
	    }
	    
	    String sFromVendor = clsManageRequestParameters.get_Request_Parameter(APVendorNumberChangeSelect.FROM_VENDOR, request);
	    String sToVendor = clsManageRequestParameters.get_Request_Parameter(APVendorNumberChangeSelect.TO_VENDOR, request);

	    APVendorNumberChange apnc = new APVendorNumberChange();
	    
		Connection conn;
		try {
			conn = clsDatabaseFunctions.getConnectionWithException(
				getServletContext(), 
				(String)CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_DATABASE_ID),
				"MySQL",
				this.toString() + ".doPost - User: " + sUserName);
		} catch (Exception e) {
			m_sWarning = "Error [1545921765] - Unable to get data connection - " + e.getMessage();
			response.sendRedirect(
					"" + SMUtilities.getURLLinkBase(getServletContext()) + "" + sCallingClass + "?"
					+ "Warning=" + m_sWarning
					+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
			);			
	    	return;
		}
		
	    String sDesc = "CHANGE '" + sFromVendor + "' TO '" + sToVendor + "'";
	    if (bIsMerge){
	    	sDesc = "MERGE '" + sFromVendor + "' INTO '" + sToVendor + "'";
	    }
	    
	    try {
			apnc.processChange(conn, sFromVendor, sToVendor, bIsMerge, sUserID);
		} catch (Exception e) {
		    SMClasses.SMLogEntry log = new SMClasses.SMLogEntry(conn);
		    log.writeEntry(sUserID, SMLogEntry.LOG_OPERATION_APRENUMBER, sDesc, "Error: " + e.getMessage(), "[1507559683]");
	    	clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1547059496]");
	    	m_sWarning = e.getMessage();
			response.sendRedirect(
					"" + SMUtilities.getURLLinkBase(getServletContext()) + "" + sCallingClass + "?"
					+ "Warning=" + m_sWarning
					+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
			);
			return;
		}
	    
	    SMClasses.SMLogEntry log = new SMClasses.SMLogEntry(conn);
	    log.writeEntry(sUserID, SMLogEntry.LOG_OPERATION_APRENUMBER, sDesc, "Successful", "[1507559682]");
    	clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1547059497]");
    	String sFunction = "CHANGED TO";
    	if (bIsMerge){
    		sFunction = "MERGED INTO";
    	}
    	m_sWarning = "Vendor account '" + sFromVendor + "' was successfully " + sFunction + " vendor '" + sToVendor + "'.";
		response.sendRedirect(
				"" + SMUtilities.getURLLinkBase(getServletContext()) + "" + sCallingClass + "?"
				+ "Status=" + m_sWarning
				+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
		);
		return;
    }
	public void doGet(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
		
		doPost(request, response);
	}
}
