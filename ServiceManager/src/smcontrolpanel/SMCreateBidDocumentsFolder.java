package smcontrolpanel;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import ServletUtilities.clsManageRequestParameters;

public class SMCreateBidDocumentsFolder extends HttpServlet{

	private static final long serialVersionUID = 1L;
	//OBSOLETE? - We no longer use actual folders for sales lead documents
	//HttpServletRequest parameters:
	private String m_sWarning;
	
	private static String sDBID = "";
	public void doPost(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {

		if(!SMAuthenticate.authenticateSMCPCredentials(
				request, 
				response, 
				getServletContext(), 
				-1L))
			{
				return;
			}

	    //Get the session info:
	    HttpSession CurrentSession = request.getSession(true);
	    sDBID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_DATABASE_ID);
	    m_sWarning = "";
	    
	    String sBidNumber = clsManageRequestParameters.get_Request_Parameter("BidNumber", request).trim();
	    String sCallingClass = clsManageRequestParameters.get_Request_Parameter("CallingClass", request).trim();
	    String sOriginalCallingClass = clsManageRequestParameters.get_Request_Parameter("OriginalCallingClass", request).trim();
	    
    	//try {
		//	if (!createDocumentsFolder(request, sBidNumber)){
		//	}
		//} catch (SQLException e) {
		//	m_sWarning = e.getMessage();
		//}
		
		response.sendRedirect(
			"" + SMUtilities.getURLLinkBase(getServletContext()) + "" + sCallingClass
			+ "?id=" + sBidNumber
			+ "&CallingClass=" + sOriginalCallingClass
			+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
			+ "&Warning=" + m_sWarning
		);
    	return;
	}

	public void doGet(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
		
		doPost(request, response);
	}
}