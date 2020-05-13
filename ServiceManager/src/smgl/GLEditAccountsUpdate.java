package smgl;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import smcontrolpanel.SMAuthenticate;
import smcontrolpanel.SMSystemFunctions;
import smcontrolpanel.SMUtilities;

public class GLEditAccountsUpdate extends HttpServlet{
	
	private static final long serialVersionUID = 1L;
	public void doPost(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
	
		if(!SMAuthenticate.authenticateSMCPCredentials(
				request, 
				response, 
				getServletContext(), 
				SMSystemFunctions.GLEditChartOfAccounts))
			{
				return;
			}

	    //Get the session info:
	    HttpSession CurrentSession = request.getSession(true);
	    String sDBID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_DATABASE_ID);
	    String sUserFullName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERFIRSTNAME) 
	    	+ " " + (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERLASTNAME);
		
		GLAccount glacct = new GLAccount("");
		glacct.loadFromHTTPRequest(request);

		if(!glacct.save(getServletContext(), sDBID, sUserFullName)){
			response.sendRedirect(
					"" + SMUtilities.getURLLinkBase(getServletContext()) + "smgl.GLEditAccountsEdit"
					+ "?" + glacct.getQueryString()
					+ "&Warning=Could not save: " + ServletUtilities.clsServletUtilities.URLEncode(glacct.getErrorMessageString())
					+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
			);
			return;
		}
		response.sendRedirect(
				"" + SMUtilities.getURLLinkBase(getServletContext()) + "smgl.GLEditAccountsEdit"
				+ "?" + glacct.getQueryString()
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