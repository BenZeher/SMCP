package smcontrolpanel;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import SMClasses.SMLogEntry;

public class SMChangeUserPassword extends HttpServlet {
	private static final long serialVersionUID = 1L;
	@Override
	public void doPost(HttpServletRequest request,
				HttpServletResponse response)
				throws ServletException, IOException {
		
		PrintWriter out = response.getWriter();
		if(!SMAuthenticate.authenticateSMCPCredentials(
				request, 
				response, 
				getServletContext(), 
				SMSystemFunctions.SMChangeyourpassword))
			{
				return;
			}

	    //Get the session info:
	    HttpSession CurrentSession = request.getSession(true);
	    String sUserID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERID);
	    String sCompanyName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_COMPANYNAME);
	    String sDBID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_DATABASE_ID);
	    String title = "Change current password";
	    String subtitle = "";
	    
	    SMClasses.SMLogEntry log = new SMClasses.SMLogEntry(sDBID, getServletContext());
	 	log.writeEntry(sUserID, SMLogEntry.LOG_OPERATION_SMCHANGEUSERPASSWORD, "REPORT", "SMChangeYourPassword", "[1535653641]");

	    
	    out.println(SMUtilities.SMCPTitleSubBGColor(title, subtitle, SMUtilities.getInitBackGroundColor(getServletContext(), sDBID), sCompanyName));
	    
	    out.println("<BR><A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMUserLogin?" 
				+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
				+ "\">Return to user login</A><BR><BR>");
	    
	    out.println("<BR>" + SMUtilities.PASSWORD_REQUIREMENTS + "<BR>" );
	    
	    out.println("<FORM NAME='MAINFORM' ACTION='" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMChangeUserPasswordAction' METHOD='POST'>");
	    out.println("<INPUT TYPE=HIDDEN NAME='" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "' VALUE='" + sDBID + "'>");
	    //Display text boxes for the current password, the new
		//one, and a confirmation:
		String sOutPut;
		
		sOutPut = "<P>Enter your current password:<BR>";
		sOutPut = sOutPut + "<INPUT TYPE=PASSWORD NAME='CurrentPassword' SIZE=28 MAXLENGTH=50 STYLE='width: 2.41in; height: 0.25in;'></P>";
		sOutPut = sOutPut + "<P>Enter a new password:<BR>";
		sOutPut = sOutPut + "<INPUT TYPE=PASSWORD NAME='NewPassword' SIZE=28 MAXLENGTH=50 STYLE='width: 2.41in; height: 0.25in;'></P>";
		sOutPut = sOutPut + "<P>Retype your new password:<BR>";
		sOutPut = sOutPut + "<INPUT TYPE=PASSWORD NAME='ConfirmedPassword' SIZE=28 MAXLENGTH=50 STYLE='width: 2.41in; height: 0.25in;'></P>";
		sOutPut = sOutPut + "<P><INPUT TYPE=SUBMIT NAME='Submit' VALUE='Change Password' STYLE='height: 0.24in'></P>";
		sOutPut = sOutPut + "</FORM>";
		out.println(sOutPut);
		
		out.println("</BODY></HTML>");
	}
	
	@Override
	public void doGet(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
		
		doPost(request, response);
	}
}