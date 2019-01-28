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
public class ARClearPaidTransactionsSelection extends HttpServlet {
	private static final long serialVersionUID = 1L;
	public void doPost(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
	
		PrintWriter out = response.getWriter();
	    if (!SMAuthenticate.authenticateSMCPCredentials(request, response, getServletContext(), SMSystemFunctions.ARClearfullypaidtransactions)){
	    	return;
	    }

	    //Get the session info:
	    HttpSession CurrentSession = request.getSession(true);
	    String sCompanyName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_COMPANYNAME);
	    String sDBID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_DATABASE_ID);
	    String title = "Clear fully paid transactions";
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
	    out.println("<A HREF=\"" + WebContextParameters.getdocumentationpageURL(getServletContext()) + "#" + Long.toString(SMSystemFunctions.ARClearfullypaidtransactions) 
	    		+ "\">Summary</A><BR>");
	    
		out.println ("<FORM ACTION =\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smar.ARClearPaidTransactionsAction\">");
		out.println("<INPUT TYPE=HIDDEN NAME='" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "' VALUE='" + sDBID + "'>");
		out.println("<INPUT TYPE=HIDDEN NAME=CallingClass VALUE=\"" + this.getClass().getName() + "\">");
	    out.println("<BR>NOTE: This will permanently clear all the fully satisfied Accounts Receivable transactions"
	    		+ " (transactions) with document dates up to and including the date you select."
	    		+ "  Any matching lines that APPLY TO those transactions, as well as any AR Chronological Log records"
	    		+" for them will be deleted as well.<BR>");
	    
		out.println("<TABLE WIDTH=100% CELLPADDING=10 border=4>");
		out.println("<TR>");
	    out.println(ARUtilities.Create_Edit_Form_DateText_Input_Row(
	    		"ClearingDate", 
	    		"1/1/1900", 
	    		10, 
	    		"Clear up to document date", 
	    		"Input as (mm/dd/yyyy)", 
	    		"1.00",
	    		getServletContext()
	    		)
	    		);
		
		out.println("</TABLE>");
		out.println("<BR>");
		
		out.println("<INPUT TYPE=\"SUBMIT\" VALUE=\"----Clear transactions----\">");
		out.println("  Check to confirm clearing: <INPUT TYPE=CHECKBOX NAME=\"ConfirmClear\">");
		out.println("</FORM>");
	   
		out.println("</BODY></HTML>");
	}

	public void doGet(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
		
		doPost(request, response);
	}
}
