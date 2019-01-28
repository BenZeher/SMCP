package smbk;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import ServletUtilities.clsCreateHTMLFormFields;
import ServletUtilities.clsManageRequestParameters;
import smcontrolpanel.SMAuthenticate;
import smcontrolpanel.SMSystemFunctions;
import smcontrolpanel.SMUtilities;

public class BKClearEntriesSelect extends HttpServlet {

	private static final long serialVersionUID = 1L;
	
	public static String CLEAR_BK_BUTTON_NAME = "GENERATE_REPORT";
	public static String CLEAR_BKSTATEMENT_BUTTON_LABEL = "----Clear Statements----";
	public static String CLEARING_BKENTRY_DATE_FIELD = "Clearing Date";
	public static String CONFIRM_CLEAR_BKENTRY_CHECKBOX_NAME = "CONFIRMCLEARING";

	public void doPost(HttpServletRequest request,
				HttpServletResponse response)
				throws ServletException, IOException {
		
		String sCalledClassName = "BKClearEntriesAction";
		
		PrintWriter out = response.getWriter();
	    //Get the session info:
	    HttpSession CurrentSession = request.getSession(true);
	    String sCompanyName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_COMPANYNAME);
	    String sDBID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_DATABASE_ID);
	    if (!SMAuthenticate.authenticateSMCPCredentials(request, response, getServletContext(), SMSystemFunctions.BKClearStatements)){
	    	return;
	    }

	    String title = "Clear Posted Bank Statements.";
	    String subtitle = "";
	    out.println(SMUtilities.SMCPTitleSubBGColor(title, subtitle, SMUtilities.getInitBackGroundColor(getServletContext(), sDBID), sCompanyName));
	    out.println(SMUtilities.getDatePickerIncludeString(getServletContext()));

	    //If there is a warning from trying to input previously, print it here:
		String sWarning = clsManageRequestParameters.get_Request_Parameter("Warning", request);
	    if (! sWarning.equalsIgnoreCase("")){
			out.println("<B><FONT COLOR=\"RED\">WARNING: " + sWarning + "</FONT></B><BR>");
		}
		String sStatus = clsManageRequestParameters.get_Request_Parameter("Status", request);
	    if (! sStatus.equalsIgnoreCase("")){
			out.println("<B>STATUS: " + sStatus + "</B><BR>");
		}
	    
	    //Print a link to the first page after login:
	    out.println("<BR><A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMUserLogin?" 
				+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
				+ "\">Return to user login</A><BR>");
	    out.println("<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smbk.BKMainMenu?" 
				+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
				+ "\">Return To Bank Functions Menu</A><BR>");
	   // out.println("<A HREF=\"" + WebContextParameters.getdocumentationpageURL(getServletContext()) + "#" + SMSystemFunctions.FAClearTransactionHistory + "\">Summary</A><BR><BR>");
	    out.println("<FORM NAME='MAINFORM' ACTION='" + SMUtilities.getURLLinkBase(getServletContext()) + "smbk." + sCalledClassName + "' METHOD='POST'>");
	    out.println("<INPUT TYPE=HIDDEN NAME='" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "' VALUE='" + sDBID + "'>");

	    out.println("<BR>Clear All Posted Bank Statements Up To:&nbsp;"
	
		+ clsCreateHTMLFormFields.TDTextBox(
				CLEARING_BKENTRY_DATE_FIELD, 
				"00/00/0000", 
				10, 
				10, 
				""
		) 
		+ SMUtilities.getDatePickerString(CLEARING_BKENTRY_DATE_FIELD, getServletContext()));
	    
	    out.println("<BR><INPUT TYPE=\"SUBMIT\" NAME=" 
				+ CLEAR_BK_BUTTON_NAME 
				+ " VALUE=\"" + CLEAR_BKSTATEMENT_BUTTON_LABEL+ "\">&nbsp;&nbsp;");
		out.println("Check to confirm deletion: <INPUT TYPE=CHECKBOX NAME=\"" + CONFIRM_CLEAR_BKENTRY_CHECKBOX_NAME + "\">");
		out.println("<INPUT TYPE=HIDDEN NAME=\"" + "CallingClass" + "\" VALUE='" + SMUtilities.getFullClassName(this.toString()) + "'>");
		out.println("</FORM>");
		out.println("</BODY></HTML>");
	}
	
	public void doGet(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
		
		doPost(request, response);
	}
}
	
