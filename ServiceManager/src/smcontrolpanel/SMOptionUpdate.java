package smcontrolpanel;
import smar.*;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

public class SMOptionUpdate extends HttpServlet{

	private static final long serialVersionUID = 1L;
	
	public void doPost(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {

		PrintWriter out = response.getWriter();
		if (!SMAuthenticate.authenticateSMCPCredentials(
				request, 
				response, 
				getServletContext(), 
				SMSystemFunctions.SMEditSystemOptions))
		{
			return;
		}
	    //Get the session info:
	    HttpSession CurrentSession = request.getSession(true);
	    String sDBID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_DATABASE_ID);
	    String sUserName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERNAME);
	    String sCompanyName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_COMPANYNAME);
	    //Collect all the request parameters:
	    SMOptionInput m_optioninput = new SMOptionInput(request);
	    //Instantiate a new entry:
	    SMOption m_option = new SMOption();
	    //Load the input parameters into the new system option record:
	    if (!m_optioninput.loadToSMOptionClass(m_option)){
	    	CurrentSession.setAttribute(SMOptionInput.ParamObjectName, m_optioninput);
			response.sendRedirect(
				"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel." + "SMEditSMOptions"
				+ "?" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
				+ "&Warning=" + m_optioninput.getErrorMessageString()
			);			
	    	return;
	    }
		String subtitle = "";
		String title = "";
	
		title = "Updating system options:";
		out.println(SMUtilities.SMCPTitleSubBGColor(title, subtitle, SMUtilities.getInitBackGroundColor(getServletContext(), sDBID), sCompanyName));
		try {
			m_option.saveEditableFields(getServletContext(), sDBID, sUserName);
		} catch (Exception e) {
	    	CurrentSession.setAttribute(SMOptionInput.ParamObjectName, m_optioninput);
			response.sendRedirect(
				"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel." + "SMEditSMOptions"
				+ "?" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
				+ "&Warning=" + e.getMessage()
			);			
	    	return;
		}
		
	    //If the entry CAN be processed, load the successful entry back into the EntryInput class, and 
	    //allow the entry editor to display it:
		//Setting OptionInput to false will force the calling form to reload the system options record:
		response.sendRedirect(
			"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel." + "SMEditSMOptions" + "?"
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