package smar;

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

public class ARCustomerNumberChangeGenerate extends HttpServlet{

	private static final long serialVersionUID = 1L;
	public void doPost(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
	
		String sCallingClass = clsManageRequestParameters.get_Request_Parameter("CallingClass", request);
	
	    if (!SMAuthenticate.authenticateSMCPCredentials(request, response, getServletContext(), SMSystemFunctions.ARRenumberMergeCustomers)){
	    	return;
	    }

	    //Get the session info:
	    HttpSession CurrentSession = request.getSession(true);
	    String sUserID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERID);
	    String sUserFullName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERFIRSTNAME) + " "
	    				+ (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERLASTNAME);
	    String sDBID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_DATABASE_ID);
	    String m_sWarning = "";
	    if ((request.getParameter("GROUPTYPE") == null)){
	    	m_sWarning = "You must choose either CHANGE or MERGE for the type.";
			response.sendRedirect(
					"" + SMUtilities.getURLLinkBase(getServletContext()) + "" + sCallingClass + "?"
					+ "Warning=" + m_sWarning
					+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
			);
			return;
	    }
		
	    boolean bIsMerge = false;
	    if ((request.getParameter("GROUPTYPE").compareToIgnoreCase("Merge") == 0)){
	    	bIsMerge = true;
	    }
	    
	    if (request.getParameter("ConfirmChange") == null){
	    	m_sWarning = "You chose to change the customer code, but did not check the box to confirm.";
			response.sendRedirect(
					"" + SMUtilities.getURLLinkBase(getServletContext()) + "" + sCallingClass + "?"
					+ "Warning=" + m_sWarning
					+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
			);
			return;
	    }
	    
	    if (request.getParameter("FromCustomer") == null){
	    	m_sWarning = "The FROM customer code is blank.";
			response.sendRedirect(
					"" + SMUtilities.getURLLinkBase(getServletContext()) + "" + sCallingClass + "?"
					+ "Warning=" + m_sWarning
					+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
			);
			return;
	    }
	    
	    if (request.getParameter("ToCustomer") == null){
	    	m_sWarning = "The TO customer code is blank.";
			response.sendRedirect(
					"" + SMUtilities.getURLLinkBase(getServletContext()) + "" + sCallingClass + "?"
					+ "Warning=" + m_sWarning
					+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
			);
			return;
	    }
	    
	    String sFromCustomer = clsManageRequestParameters.get_Request_Parameter("FromCustomer", request);
	    String sToCustomer = clsManageRequestParameters.get_Request_Parameter("ToCustomer", request);

	    ARCustomerNumberChange acnc = new ARCustomerNumberChange();
	    
		Connection conn = clsDatabaseFunctions.getConnection(
			getServletContext(), 
			sDBID,
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
					+ "Warning=" + m_sWarning
					+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
			);			
	    	return;
		}
	    
	    String sDesc = "CHANGE '" + sFromCustomer + "' TO '" + sToCustomer + "'";
	    if (bIsMerge){
	    	sDesc = "MERGE '" + sFromCustomer + "' INTO '" + sToCustomer + "'";
	    }
	    if(!acnc.processChange(conn, sFromCustomer, sToCustomer, bIsMerge)){
		    SMClasses.SMLogEntry log = new SMClasses.SMLogEntry(conn);
		    log.writeEntry(sUserID, SMLogEntry.LOG_OPERATION_ARRENUMBER, sDesc, "Error: " + acnc.getErrorMessage(), "[1376509273]");
	    	clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1547067524]");
	    	m_sWarning = acnc.getErrorMessage();
			response.sendRedirect(
					"" + SMUtilities.getURLLinkBase(getServletContext()) + "" + sCallingClass + "?"
					+ "Warning=" + m_sWarning
					+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
			);
			return;
	    }else{
		    SMClasses.SMLogEntry log = new SMClasses.SMLogEntry(conn);
		    log.writeEntry(sUserID, SMLogEntry.LOG_OPERATION_ARRENUMBER, sDesc, "Successful", "[1376509274]");
	    	clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1547067525]");
	    	m_sWarning = "Customer code " + sFromCustomer + " successfully merged into customer " + sToCustomer + ".";
			response.sendRedirect(
					"" + SMUtilities.getURLLinkBase(getServletContext()) + "" + sCallingClass + "?"
					+ "Status=" + m_sWarning
					+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
			);
			return;
	    }
    }
	public void doGet(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
		
		doPost(request, response);
	}
}
