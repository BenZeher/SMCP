package smar;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import smcontrolpanel.SMAuthenticate;
import smcontrolpanel.SMSystemFunctions;
import smcontrolpanel.SMUtilities;

public class AREditCustomerGroupsAction extends HttpServlet{
	
	private static final long serialVersionUID = 1L;
	private static String sDBID = "";
	public void doPost(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
	
		if(!SMAuthenticate.authenticateSMCPCredentials(
				request, 
				response, 
				getServletContext(), 
				SMSystemFunctions.AREditCustomerGroups))
			{
				return;
			}

	    //Get the session info:
	    HttpSession CurrentSession = request.getSession(true);
	    sDBID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_DATABASE_ID);
		ARCustomerGroup group = new ARCustomerGroup("");
		group.loadFromHTTPRequest(request);
		if(!group.save(getServletContext(), sDBID)){
			response.sendRedirect(
					"" + SMUtilities.getURLLinkBase(getServletContext()) + "smar.AREditCustomerGroupsEdit"
					+ "?" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
					+ "&" + group.getQueryString()
					+ "&Warning=Could not save: " + group.getErrorMessageString()
					
			);
			return;
		}
		response.sendRedirect(
				"" + SMUtilities.getURLLinkBase(getServletContext()) + "smar.AREditCustomerGroupsEdit"
				+ "?" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
				+ "&" + group.getQueryString()
				
		);
		return;
	}
	public void doGet(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
		
		doPost(request, response);
	}
}