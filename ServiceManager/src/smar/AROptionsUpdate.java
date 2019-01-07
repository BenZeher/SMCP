package smar;
import smcontrolpanel.SMAuthenticate;
import smcontrolpanel.SMSystemFunctions;
import smcontrolpanel.SMUtilities;

import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

public class AROptionsUpdate extends HttpServlet{

	private static final long serialVersionUID = 1L;
	private AROptions m_aroptions;
	private String sDBID = "";
	private String sUserName = "";
	private String sUserID = "";
	private String sUserFullName = "";
	private String sCompanyName = "";
	//HttpServletRequest parameters:
	
	public void doPost(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {

		PrintWriter out = response.getWriter();
		
	    //Get the session info:
	    HttpSession CurrentSession = request.getSession(true);
	    sDBID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_DATABASE_ID);
	    sUserName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERNAME);
	    sUserFullName = (String)CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERFIRSTNAME) + " "
	    				+ (String)CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERLASTNAME);
	    sCompanyName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_COMPANYNAME);
		if (!SMAuthenticate.authenticateSMCPCredentials(
				request, 
				response, 
				getServletContext(),
				SMSystemFunctions.AREditAROptions)){
			return;
		}
    
	    //Collect all the request parameters:
	    getRequestParameters(request);
	    
		String subtitle = "";
		String title = "";
	
		title = "Updating Accounts Receivable options:";
		out.println(SMUtilities.SMCPTitleSubBGColor(title, subtitle, SMUtilities.getInitBackGroundColor(getServletContext(), sDBID), sCompanyName));
		
		if (!m_aroptions.saveEditableFields(getServletContext(), sDBID, sUserName, sUserID, sUserFullName)){
			//Setting OptionInput to false will force the calling form to reload the system options record:
			response.sendRedirect(
						"" + SMUtilities.getURLLinkBase(getServletContext()) + "smar." + "AREditAROptions" + "?"
						+ "OptionInput=True"
						+ "&" + m_aroptions.getQueryString()
						+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
						+ "&Warning=" + "Could not save Accounts Receivable options: "
						+ m_aroptions.getErrorMessageString()
				);
	        return;
	    //If the entry CAN be processed, load the successful entry back into the EntryInput class, and 
	    //allow the entry editor to display it:
		}else{
			//Redirect:
			//Setting OptionInput to false will force the calling form to reload the system options record:
			response.sendRedirect(
					"" + SMUtilities.getURLLinkBase(getServletContext()) + "smar." + "AREditAROptions" + "?"
					+ "OptionInput=false"
					+ "&" + m_aroptions.getQueryString()
					+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
					+ "&Warning="
			);
	        return;
		}
	}
	private void getRequestParameters(
    	HttpServletRequest req){

		m_aroptions = new AROptions(req);
	}
	public void doGet(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
		
		doPost(request, response);
	}
}