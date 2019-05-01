package smgl;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import ConnectionPool.WebContextParameters;
import ServletUtilities.clsManageRequestParameters;
import smcontrolpanel.SMAuthenticate;
import smcontrolpanel.SMSystemFunctions;
import smcontrolpanel.SMUtilities;
public class GLClearTransactionsSelect extends HttpServlet {
	
	public static final String CLEARING_DATE = "ClearingDate";
	public static final String CONFIRM_CLEARING_CHECKBOX = "ConfirmClearing";
	
	private static final long serialVersionUID = 1L;
	public void doPost(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
	
		PrintWriter out = response.getWriter();
	    if (!SMAuthenticate.authenticateSMCPCredentials(request, response, getServletContext(), SMSystemFunctions.GLClearTransactions)){
	    	return;
	    }

	    //Get the session info:
	    HttpSession CurrentSession = request.getSession(true);
	    String sCompanyName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_COMPANYNAME);
	    String sDBID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_DATABASE_ID);
	    String title = "Clear previous GL transactions";
	    String subtitle = "";
	    out.println(SMUtilities.SMCPTitleSubBGColor(title, subtitle, SMUtilities.getInitBackGroundColor(getServletContext(), sDBID), sCompanyName));
	    out.println(SMUtilities.getDatePickerIncludeString(getServletContext()));
	    
	    String sWarning = clsManageRequestParameters.get_Request_Parameter("Warning", request);
		if (! sWarning.equalsIgnoreCase("")){
			out.println("<B><FONT COLOR=\"RED\">WARNING: " + sWarning + "</FONT></B><BR>");
		}
	    String sStatus = clsManageRequestParameters.get_Request_Parameter("Status", request);
		if (! sStatus.equalsIgnoreCase("")){
			out.println("<B>NOTE: " + sStatus + "</B><BR>");
		}
		
	    //Print a link to the first page after login:
	    out.println("<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMUserLogin?" 
				+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
				+ "\">Return to user login</A><BR>");
	    //Print a link to main menu:
	    out.println("<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smgl.GLMainMenu?" 
				+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
				+ "\">Return to General Ledger Main Menu</A><BR>");
	    out.println("<A HREF=\"" + WebContextParameters.getdocumentationpageURL(getServletContext()) + "#" + Long.toString(SMSystemFunctions.GLClearTransactions) 
	    		+ "\">Summary</A><BR>");
	    
		out.println ("<FORM ACTION =\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smgl.GLClearTransactionsAction\">");
		out.println("<INPUT TYPE=HIDDEN NAME='" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "' VALUE='" + sDBID + "'>");
		out.println("<INPUT TYPE=HIDDEN NAME=CallingClass VALUE=\"" + this.getClass().getName() + "\">");
	    out.println("<BR>NOTE: This will permanently clear all the GL transaction history up to and including the date you select.  It uses the POSTING DATE"
	    	+ " of the transaction to determine whether to remove the transaction.<BR>");
	    
	    out.println("<BR>"
	    	+ "<INPUT TYPE=TEXT NAME=\"" + CLEARING_DATE + "\""
	    	+ " VALUE=\"" + SMUtilities.EMPTY_DATE_VALUE + "\""
	    	+ " SIZE=10"
	    	+ " MAXLENGTH = 10"
	    	+ " STYLE=\"width: " + "1.00" + " in; height: 0.25in\""
	    	+ ">&nbsp;"
	    	
	    	+ SMUtilities.getDatePickerString(CLEARING_DATE, getServletContext())
        );
        
		out.println("<BR><BR>");
		
		out.println("<INPUT TYPE=\"SUBMIT\" VALUE=\"----Clear transactions----\">");
		out.println("  Check to confirm clearing: <INPUT TYPE=CHECKBOX NAME=\"" + CONFIRM_CLEARING_CHECKBOX + "\">");
		out.println("</FORM>");
	   
		out.println("</BODY></HTML>");
	}

	public void doGet(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
		
		doPost(request, response);
	}
}
