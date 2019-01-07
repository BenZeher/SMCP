package smic;
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

public class ICOptionUpdate extends HttpServlet{

	private static final long serialVersionUID = 1L;
	private ICOption m_icoption;
	private ICOptionInput m_icoptioninput;
	private String sDBID = "";
	private String sUserName = "";
	private String sCompanyName = "";
	//HttpServletRequest parameters:
	
	public void doPost(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {

		PrintWriter out = response.getWriter();
		if (!SMAuthenticate.authenticateSMCPCredentials(
				request, 
				response, 
				getServletContext(), 
				SMSystemFunctions.ICEditICOptions))
		{
			return;
		}

	    //Get the session info:
	    HttpSession CurrentSession = request.getSession(true);
	    sDBID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_DATABASE_ID);
	    sUserName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERNAME);
	    sCompanyName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_COMPANYNAME);
    
	    //Collect all the request parameters:
	    getRequestParameters(request);
	    //Instantiate a new entry:
	    m_icoption = new ICOption();
	    
	    //Load the input parameters into the new system option record:
	    if (!m_icoptioninput.loadToICOptionClass(m_icoption)){
			response.sendRedirect(
					"" + SMUtilities.getURLLinkBase(getServletContext()) + "smic." + "ICEditICOptions" + "?"
					+ "OptionInput=true"
					+ "&" + m_icoptioninput.getQueryString()
					+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
					+ "&Warning=" + m_icoptioninput.getErrorMessageString()
			);			
	    	return;
	    }
	    
		String subtitle = "";
		String title = "";
	
		title = "Updating Inventory options:";
		out.println(SMUtilities.SMCPTitleSubBGColor(title, subtitle, SMUtilities.getInitBackGroundColor(getServletContext(), sDBID), sCompanyName));
		
		if (!m_icoption.saveEditableFields(getServletContext(), sDBID, sUserName)){
			//Setting OptionInput to false will force the calling form to reload the system options record:
			response.sendRedirect(
						"" + SMUtilities.getURLLinkBase(getServletContext()) + "smic." + "ICEditICOptions" + "?"
						+ "OptionInput=True"
						+ "&" + m_icoptioninput.getQueryString()
						+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
						+ "&Warning=" + "Could not save Inventory options: "+ m_icoption.getErrorMessage()
				);
	        return;
	    //If the entry CAN be processed, load the successful entry back into the EntryInput class, and 
	    //allow the entry editor to display it:
		}else{
			//Redirect:
			//Setting OptionInput to false will force the calling form to reload the system options record:
			response.sendRedirect(
					"" + SMUtilities.getURLLinkBase(getServletContext()) + "smic." + "ICEditICOptions" + "?"
					+ "OptionInput=false"
					+ "&" + m_icoptioninput.getQueryString()
					+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
					+ "&Warning="
			);
	        return;
		}
	}
	private void getRequestParameters(
    	HttpServletRequest req){

		m_icoptioninput = new ICOptionInput(req);
	}
	public void doGet(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
		
		doPost(request, response);
	}
}