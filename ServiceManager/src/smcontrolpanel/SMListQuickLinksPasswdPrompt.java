package smcontrolpanel;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import ServletUtilities.clsManageRequestParameters;

public class SMListQuickLinksPasswdPrompt extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static String sCompanyName = "";
	private String sDBID = "";
	public void doGet(HttpServletRequest request,
				HttpServletResponse response)
				throws ServletException, IOException {
	    
		if (!SMAuthenticate.authenticateSMCPCredentials(
				request, 
				response, 
				getServletContext(), 
				SMSystemFunctions.SMListQuickLinks
			)
		){
			return;
		}
	    response.setContentType("text/html");
	    
		PrintWriter out = response.getWriter();
	    //Get the session info:
	    HttpSession CurrentSession = request.getSession(true);
	    sCompanyName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_COMPANYNAME);
	    sDBID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_DATABASE_ID);
	    String title = "List Customized Quick Links";
	    String subtitle = "";
	    
	    out.println(SMUtilities.SMCPTitleSubBGColor(title, subtitle, SMUtilities.getInitBackGroundColor(getServletContext(), sDBID), sCompanyName));

	    out.println("<BR><A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMUserLogin?" 
				+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
				+ "\">Return to user login</A><BR><BR>");

	    String sWarning = clsManageRequestParameters.get_Request_Parameter("Warning", request);
		if (! sWarning.equalsIgnoreCase("")){
			out.println("<B><FONT COLOR=\"RED\">WARNING: " + sWarning + "</FONT></B><BR>");
		}
    	out.println ("<FORM ACTION =\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMListQuickLinks\">");
    	out.println("<INPUT TYPE=HIDDEN NAME='" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "' VALUE='" + sDBID + "'>");
		out.println("<INPUT TYPE=HIDDEN NAME='CallingClass' VALUE=\"" + SMUtilities.getFullClassName(this.toString()) + "\">");

    	out.println(
				"<P>Please type in your password to list quick links:&nbsp;&nbsp;<INPUT TYPE=PASSWORD NAME=\"PASSWORD\" " // SIZE=28 "
				+ "MAXLENGTH=50"
				//+ "\" STYLE=\"width: 2.41in; height: 0.25in\">"
				+ ">"
				//+ "&nbsp;&nbsp;<BR><BR><FONT COLOR=RED>(If you put invalid password in, the quick links generated will not work. You may also need to login again.)</FONT></P>"
		);
    	
    	out.println ("<INPUT TYPE=\"SUBMIT\" VALUE=\"----List----\">");
    	out.println ("</FORM>");
 
	    out.println("</BODY></HTML>");
	}
	
	public void doPost(HttpServletRequest request,
				HttpServletResponse response)
				throws ServletException, IOException {
		
		doGet(request, response);
	}
	
}