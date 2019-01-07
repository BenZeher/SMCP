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

public class AREditPriceListCodesAction extends HttpServlet{
	
	private static final long serialVersionUID = 1L;
	private static String sDBID = "";
	public void doPost(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
	
		if(!SMAuthenticate.authenticateSMCPCredentials(
				request, 
				response, 
				getServletContext(), 
				SMSystemFunctions.AREditPriceListCodes))
			{
				return;
			}

	    //Get the session info:
	    HttpSession CurrentSession = request.getSession(true);
	    sDBID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_DATABASE_ID);
		
		SMPriceListCode plc = new SMPriceListCode("");
		plc.loadFromHTTPRequest(request);
		if(!plc.save(getServletContext(), sDBID)){
			response.sendRedirect(
				"" + SMUtilities.getURLLinkBase(getServletContext()) + "smar.AREditPriceListCodesEdit"
				+ "?" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
				+ "&" + plc.getQueryString()
				+ "&Warning=Could not save: " + plc.getErrorMessageString()
			);
			return;
		}
		response.sendRedirect(
			"" + SMUtilities.getURLLinkBase(getServletContext()) + "smar.AREditPriceListCodes"
			+ "?" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
			+ "&" + plc.getQueryString()
			+ "&Status=Successfully saved price list code '" + plc.getM_sPriceListCode() + "'"
		);
		return;
	}
	public void doGet(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
		
		doPost(request, response);
	}
}