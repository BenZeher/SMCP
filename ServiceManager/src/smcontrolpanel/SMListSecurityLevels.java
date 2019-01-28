package smcontrolpanel;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

public class SMListSecurityLevels extends HttpServlet {
	private static final long serialVersionUID = 1L;
	@Override
	public void doPost(HttpServletRequest request,
				HttpServletResponse response)
				throws ServletException, IOException {
		
		response.setContentType("text/html");
		PrintWriter out = response.getWriter();
		if (!SMAuthenticate.authenticateSMCPCredentials(
				request, 
				response, 
				getServletContext(), 
				SMSystemFunctions.SMListSecurityLevels))
		{
			return;
		}

	    //Get the session info:
	    HttpSession CurrentSession = request.getSession(true);
	    String sCompanyName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_COMPANYNAME);
	    String sDBID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_DATABASE_ID);
	    
	    String title = "List Security Users, Groups, and Functions";
	    String subtitle = "";
	    out.println(SMUtilities.SMCPTitleSubBGColor(title, subtitle, SMUtilities.getInitBackGroundColor(getServletContext(), sDBID), sCompanyName));

	    out.println("<BR><A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) 
	    		+ "smcontrolpanel.SMUserLogin?" 
				+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
				+ "\">Return to user login</A><BR><BR>");
		
		// List by user
    	out.println("<A HREF='" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMListSecurityBy?ListBy=User&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID + "' NAME='ListSecurityByUserLink'><U><FONT SIZE=3 STYLE='font-size: 11pt'><FONT COLOR='#000080'>List Security Levels By User</FONT></FONT></U></A><BR>");
		
    	// List by group
		out.println("<A HREF='" + SMUtilities.getURLLinkBase(getServletContext())  + "smcontrolpanel.SMListSecurityBy?ListBy=Group&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID + "' NAME='ListSecurityByGroupLink'><U><FONT SIZE=3 STYLE='font-size: 11pt'><FONT COLOR='#000080'>List Security Levels By Group</FONT></FONT></U></A><BR>");
    	
    	// List by function
		out.println("<A HREF='" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMListSecurityBy?ListBy=Function&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID + "' NAME='ListSecurityByFunctionLink'><U><FONT SIZE=3 STYLE='font-size: 11pt'><FONT COLOR='#000080'>List Security Levels By Function </FONT></FONT></U></A><BR>");

		// List groups 
		out.println("<A HREF='" + SMUtilities.getURLLinkBase(getServletContext())  + "smcontrolpanel.SMListSecurityBy?ListBy=GroupList&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID + "' NAME='ListSecurityByGroupListLink'><U><FONT SIZE=3 STYLE='font-size: 11pt'><FONT COLOR='#000080'>List Security Groups </FONT></FONT></U></A><BR>");

		// List functions 
		out.println("<A HREF='" + SMUtilities.getURLLinkBase(getServletContext())  + "smcontrolpanel.SMListSecurityBy?ListBy=FunctionList&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID + "' NAME='ListSecurityByFunctionListLink'><U><FONT SIZE=3 STYLE='font-size: 11pt'><FONT COLOR='#000080'>List Security Functions </FONT></FONT></U></A><BR>");
		out.println("</BODY></HTML>");
	}
	
	@Override
	public void doGet(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
		
		doPost(request, response);
	}
}