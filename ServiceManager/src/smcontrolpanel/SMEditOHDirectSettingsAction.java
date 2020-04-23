package smcontrolpanel;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import ServletUtilities.clsOHDirectSettings;

public class SMEditOHDirectSettingsAction extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
	public void doPost(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {

		PrintWriter out = response.getWriter();
		if (!SMAuthenticate.authenticateSMCPCredentials(
				request, 
				response, 
				getServletContext(), 
				SMSystemFunctions.SMEditOHDirectSettings))
		{
			return;
		}
	    //Get the session info:
	    HttpSession CurrentSession = request.getSession(true);
	    String sDBID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_DATABASE_ID);
	    String sUserID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERID);
	    String sUserFullName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERFIRSTNAME)
	    	+ " " + (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERLASTNAME);
	    String sCompanyName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_COMPANYNAME);
	    //Collect all the request parameters:
	    clsOHDirectSettings objsettings = new clsOHDirectSettings(request);

		String subtitle = "";
		String title = "";
	
		title = "Updating OHDirect Connection Settings:";
		out.println(SMUtilities.SMCPTitleSubBGColor(title, subtitle, SMUtilities.getInitBackGroundColor(getServletContext(), sDBID), sCompanyName));
		try {
			objsettings.save_without_data_transaction(getServletContext(), sDBID, sUserID, sUserFullName);;
		} catch (Exception e) {
	    	CurrentSession.setAttribute(clsOHDirectSettings.ParamObjectName, objsettings);
			response.sendRedirect(
				"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel." + "SMEditOHDirectSettingsEdit"
				+ "?" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
				+ "&Warning=" + e.getMessage()
			);			
	    	return;
		}
		
		response.sendRedirect(
			"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel." + "SMEditOHDirectSettingsEdit" + "?"
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
