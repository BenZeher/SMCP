package smcontrolpanel;

import java.io.*;
import java.util.ArrayList;

import javax.servlet.*;
import javax.servlet.http.*;

import ServletUtilities.*;

public class SMURLHistoryList extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private String sCompanyName = "";
	private String sDBID = "";
	public void doGet(HttpServletRequest request,
				HttpServletResponse response)
				throws ServletException, IOException {
	    
		if(!SMAuthenticate.authenticateSMCPCredentials(request,	response, getServletContext(), -1L)){
			return;
		}
		
	    response.setContentType("text/html");
		PrintWriter out = response.getWriter();
		
	    //Get the session info:
	    HttpSession CurrentSession = request.getSession(true);
	    sCompanyName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_COMPANYNAME);
	    sDBID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_DATABASE_ID);
	    String title = "URL History:";
	    String subtitle = "";
	    
	    out.println(SMUtilities.SMCPTitleSubBGColor(title, subtitle, SMUtilities.getInitBackGroundColor(getServletContext(), sDBID), sCompanyName));
			    
		//Print a link to the first page after login:
	    out.println("<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMUserLogin?" 
				+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
				+ "\">User login</A><BR><BR>");
	    //Print out URL history
	    @SuppressWarnings("unchecked")
		ArrayList<clsURLRecord> alHistory = (ArrayList<clsURLRecord>) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_URLHISTORY);
	    try {
			for (int i=0;i<alHistory.size();i++){
				out.println("<A HREF=\"" + clsServletUtilities.URLDecode(((clsURLRecord) alHistory.get(i)).Address()) + "\">" + (i + 1) + " :   " + ((clsURLRecord) alHistory.get(i)).Title() + "</A><BR>");
			}
		} catch (Exception e) {
			out.println("Error [1419955188] reading URL history - " + e.getMessage());
		}
	    out.println("</BODY></HTML>");
	}
}