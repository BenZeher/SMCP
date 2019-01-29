package smar;

import java.io.IOException;
import java.sql.Connection;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import ServletUtilities.clsDatabaseFunctions;
import smcontrolpanel.SMAuthenticate;
import smcontrolpanel.SMSystemFunctions;
import smcontrolpanel.SMUtilities;

public class ARYearEndProcessingAction extends HttpServlet{
	
	private static final long serialVersionUID = 1L;
	public void doPost(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
	
		if(!SMAuthenticate.authenticateSMCPCredentials(
				request, 
				response, 
				getServletContext(), 
				SMSystemFunctions.ARYearEndProcessing))
			{
				return;
			}

	    //Get the session info:
	    HttpSession CurrentSession = request.getSession(true);
	    String sDBID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_DATABASE_ID);
	    String sUserID = (String)CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERID);
	    String sUserFullName = (String)CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERFIRSTNAME) + " "
	    				+ (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERLASTNAME);
		String sCallingClass = ARUtilities.get_Request_Parameter("CallingClass", request);
		String sWarning = "";
		
		Connection conn = clsDatabaseFunctions.getConnection(
			getServletContext(), 
			sDBID,
			"MySQL",
			this.toString() + ".doGet - User: " 
			+ sUserID
			+ " - "
			+ sUserFullName
				);
		
		if (request.getParameter("ConfirmProcess") == null){
			sWarning = "You chose to process, but did not check the 'Confirm' checkbox";
			response.sendRedirect(
				"" + SMUtilities.getURLLinkBase(getServletContext()) + "" + sCallingClass
				+ "?Warning=" + sWarning
				+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
			);
			return;
		}
		
		if(conn == null){
			sWarning = "Error opening connection";
			response.sendRedirect(
				"" + SMUtilities.getURLLinkBase(getServletContext()) + "" + sCallingClass
				+ "?Warning=" + sWarning
				+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
			);
			return;
		}
		if(!ARYearEndProcess.processYearEnd(conn)){
			sWarning = ARYearEndProcess.getErrorMessage();
			clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1547067575]");
			response.sendRedirect(
				"" + SMUtilities.getURLLinkBase(getServletContext()) + "" + sCallingClass
				+ "?Warning=" + sWarning
				+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
			);
			return;
		}
		clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1547067576]");
		response.sendRedirect(
			"" + SMUtilities.getURLLinkBase(getServletContext()) + "" + sCallingClass
			+ "?Warning=Year end processing completed successfully."
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
