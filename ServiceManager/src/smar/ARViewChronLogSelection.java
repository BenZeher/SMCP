package smar;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import smcontrolpanel.SMAuthenticate;
import smcontrolpanel.SMSystemFunctions;
import smcontrolpanel.SMUtilities;
import ConnectionPool.WebContextParameters;
import SMDataDefinition.SMTablearcustomer;
import ServletUtilities.clsCreateHTMLFormFields;
import ServletUtilities.clsDateAndTimeConversions;

public class ARViewChronLogSelection  extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static String sCompanyName = "";
	private static String sDBID = "";
	public void doPost(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
	
		PrintWriter out = response.getWriter();
		if(!SMAuthenticate.authenticateSMCPCredentials(
				request, 
				response, 
				getServletContext(), 
				SMSystemFunctions.ARViewChronologicalLog))
			{
				return;
			}

	    //Get the session info:
	    HttpSession CurrentSession = request.getSession(true);
	    sCompanyName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_COMPANYNAME);
	    sDBID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_DATABASE_ID);
	    String title = "View Chronological Log";
	    String subtitle = "";
	    out.println(SMUtilities.SMCPTitleSubBGColor(title, subtitle, SMUtilities.getInitBackGroundColor(getServletContext(), sDBID), sCompanyName));
	    out.println(SMUtilities.getDatePickerIncludeString(getServletContext()));
	    
	    String sWarning = ARUtilities.get_Request_Parameter("Warning", request);
		if (! sWarning.equalsIgnoreCase("")){
			out.println("<B><FONT COLOR=\"RED\">WARNING: " + sWarning + "</FONT></B><BR>");
		}
	    //Print a link to the first page after login:
	    out.println("<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMUserLogin?" 
				+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
				+ "\">Return to user login</A><BR>");
	    //Print a link to main menu:
	    out.println("<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smar.ARMainMenu?" 
				+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
				+ "\">Return to Accounts Receivable Main Menu</A><BR>");
	    out.println("<A HREF=\"" + WebContextParameters.getdocumentationpageURL(getServletContext()) + "#" + Long.toString(SMSystemFunctions.ARViewChronologicalLog) 
	    		+ "\">Summary</A><BR><BR>");
	    
	    out.println("<BR>This report lists AR events for the selected customer range, and applying to the selected"
	    		+ " range of document numbers.  The report will only include events within the selected date"
	    		+ " range as well.<BR>"
	    );
	    
    	out.println ("<FORM ACTION =\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smar.ARViewChronLogGenerate\">");
    	out.println("<INPUT TYPE=HIDDEN NAME='" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "' VALUE='" + sDBID + "'>");
    	out.println("<INPUT TYPE=HIDDEN NAME=CallingClass VALUE=\"" + this.getClass().getName() + "\">");
    	out.println("<BR>");
    	out.println("<TABLE WIDTH=100% CELLPADDING=10 border=4>");
    	out.println("<TR>");
    	out.println("<TD ALIGN=LEFT><B>For customer:</B></TD>");
    	out.println("<TD>" + clsCreateHTMLFormFields.TDTextBox(
    		"CustomerNumber", 
    		"", 
    		SMTablearcustomer.sCustomerNumberLength, 
    		SMTablearcustomer.sCustomerNumberLength, 
    		"") 
    		+ "</TD>");
    	out.println("<TD>&nbsp;</TD>");
    	out.println("</TR>");
    	
    	//Document range selection:
    	out.println("<TR>");
    	out.println("<TD ALIGN=LEFT><B>Applying to documents:</B></TD>");
    	out.println("<TD>" + "<B>Starting with:</B> " + clsCreateHTMLFormFields.TDTextBox("StartingDocumentNumber", "", 25, 75, "") + "</TD>");
    	out.println("<TD>" + "<B>Ending with:</B> " + clsCreateHTMLFormFields.TDTextBox("EndingDocumentNumber", "ZZZZZZZZZZZZ", 25, 75, "") + "</TD>");
    	out.println("</TR>");
    	
       	//Job Number selection:
    	out.println("<TR>");
    	out.println("<TD ALIGN=LEFT><B>Invoices/Credits only for job number:</B></TD>");
    	out.println("<TD>" + clsCreateHTMLFormFields.TDTextBox("JobNumber", "", 25, 75, "") + "</TD>");
    	out.println("<TD>" + "Leave this blank to get ALL jobs." + "</TD>");
    	out.println("</TR>");
    	
    	//Event Date Range
    	out.println("<TR>");
    	out.println("<TD ALIGN=LEFT><B>Event Dates:</B></TD>");
    	out.println("<TD>" + "<B>Starting with:</B> " + clsCreateHTMLFormFields.TDTextBox("StartingEventDate", "01/01/1900", 10, 10, "") 
    			+ SMUtilities.getDatePickerString("StartingEventDate", getServletContext())
    			+ "</TD>"
    			);
    	out.println("<TD>" + "<B>Ending with:</B> " + clsCreateHTMLFormFields.TDTextBox(
    			"EndingEventDate", clsDateAndTimeConversions.now("MM/dd/yyyy"), 10, 10, "") 
    			+ SMUtilities.getDatePickerString("EndingEventDate", getServletContext())
    			+ "</TD>");
    	out.println("</TR>");
    	out.println("</TABLE>");
    	
    	out.println("<BR><INPUT TYPE=\"SUBMIT\" VALUE=\"----View----\">");
    	out.println("</FORM>");
	   
		out.println("</BODY></HTML>");
	}
	
	public void doGet(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
		
		doPost(request, response);
	}
}
