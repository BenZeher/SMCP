package smcontrolpanel;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import smcontrolpanel.SMUtilities;

public class SMTestOHDPlusSelect extends HttpServlet {
	
	public static final String PARAM_WARNING = "OHDPLUSCONNECTIONWARNING";
	public static final String PARAM_REQUESTSTRING = "OHDPLUSCONNECTIONREQSTRING";
	private static final long serialVersionUID = 1L;
	
	public void doGet(HttpServletRequest request,
				HttpServletResponse response)
				throws ServletException, IOException {
	    
	    response.setContentType("text/html");
		PrintWriter out = response.getWriter();
		if (!SMAuthenticate.authenticateSMCPCredentials(
				request, 
				response, 
				getServletContext(), 
				SMSystemFunctions.SMTestOHDPlusConnection))
		{
			return;
		}
		
	    //Get the session info:
	    HttpSession CurrentSession = request.getSession(true);
	    String sDBID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_DATABASE_ID);
	    //String sUserID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERID);
	    //String sUserFullName = (String)CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERFIRSTNAME) + " "
	    //	+ (String)CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERLASTNAME);
	    String sCompanyName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_COMPANYNAME);
	    String title = "Test Request To OHD Plus";
	    String subtitle = "";
	    
	    out.println(SMUtilities.SMCPTitleSubBGColor(title, subtitle, SMUtilities.getInitBackGroundColor(getServletContext(), sDBID), sCompanyName));
		
	    out.println(SMUtilities.getMasterStyleSheetLink());
	    out.println(SMUtilities.getDatePickerIncludeString(getServletContext()));
	    
	    //String sWarning = "";
	    String sWarning = ServletUtilities.clsManageRequestParameters.get_Request_Parameter(PARAM_WARNING, request);
		if (sWarning.compareToIgnoreCase("") != 0){
			out.println("<B><FONT COLOR=\"RED\">WARNING: " + sWarning + "</FONT></B><BR>");
		}
	    
    	out.println ("<FORM ACTION =\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMTestOHDPlusAction\">");
    	out.println("<INPUT TYPE=HIDDEN NAME='" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "' VALUE='" + sDBID + "'>\n");
    	out.println("<INPUT TYPE=HIDDEN NAME='CallingClass' VALUE='" + this.getClass().getName() + "'>\n");
    	
    	out.println("Enter request string: <INPUT TYPE=TEXT SIZE=80 NAME='" + PARAM_REQUESTSTRING + "'>");
    	
    	out.println ("<BR><INPUT TYPE=\"SUBMIT\" VALUE=\"----Make query request----\">");
    	//out.println("  Check to confirm process: <INPUT TYPE=CHECKBOX NAME=\"" + CONFIRM_PROCESS + "\"><BR>");
    	out.println ("</FORM>");
	    out.println("</BODY></HTML>");
	}
}
