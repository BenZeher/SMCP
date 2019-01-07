package smic;

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
import ServletUtilities.clsCreateHTMLFormFields;
import ServletUtilities.clsManageRequestParameters;

/*
 * Designed to clear these tables:
 * 
icinventoryworksheet
icinvoiceexportsequences
icitemstatistics
icphysicalcountlines
icphysicalcounts
icphysicalinventories
icpoheaders
icpoinvoiceheaders
icpoinvoicelines
icpolines
icporeceiptheaders
icporeceiptlines
ictransactiondetails
ictransactions
 */

public class ICClearTransactionsSelection extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static String sCompanyName = "";
	private String sDBID = "";
	public void doPost(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
	
		if (!SMAuthenticate.authenticateSMCPCredentials(
				request, 
				response, 
				getServletContext(), 
				SMSystemFunctions.ICClearTransactions)){
			return;
		}
		
		PrintWriter out = response.getWriter();
	    //Get the session info:
	    HttpSession CurrentSession = request.getSession(true);
	    sCompanyName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_COMPANYNAME);
	    sDBID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_DATABASE_ID);
	    String title = "Clear IC transactions";
	    String subtitle = "";
	    out.println(SMUtilities.SMCPTitleSubBGColor(
	    		title, subtitle, SMUtilities.getInitBackGroundColor(getServletContext(), sDBID), sCompanyName));
	    out.println(SMUtilities.getDatePickerIncludeString(getServletContext()));
	    
	    String sWarning = clsManageRequestParameters.get_Request_Parameter("Warning", request);
		if (! sWarning.equalsIgnoreCase("")){
			out.println("<B><FONT COLOR=\"RED\">WARNING: " + sWarning + "</FONT></B><BR>");
		}
	    String sStatus = clsManageRequestParameters.get_Request_Parameter("Status", request);
		if (! sStatus.equalsIgnoreCase("")){
			out.println("<B>STATUS: " + sStatus + "</B><BR>");
		}
	    //Print a link to the first page after login:
	    out.println("<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) 
	    		+ "smcontrolpanel.SMUserLogin?" 
				+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
				+ "\">Return to user login</A><BR>");
	    //Print a link to main menu:
	    out.println("<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smic.ICMainMenu?" 
				+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
				+ "\">Return to Inventory Main Menu</A><BR>");
	    out.println("<A HREF=\"" + WebContextParameters.getdocumentationpageURL(getServletContext()) + "#" + Long.toString(SMSystemFunctions.ICClearTransactions) 
	    		+ "\">Summary</A><BR>");
	    
		out.println ("<FORM ACTION =\"" + SMUtilities.getURLLinkBase(getServletContext()) 
				+ "smic.ICClearTransactionsAction\">");
		out.println("<INPUT TYPE=HIDDEN NAME='" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "' VALUE='" + sDBID + "'>");
		out.println("<INPUT TYPE=HIDDEN NAME=CallingClass VALUE=\"" + this.getClass().getName() + "\">");
	    out.println("<BR>NOTE: This will permanently clear all of the POSTED or DELETED Inventory Control records for "
	    	+ "inventory worksheets, physical inventories, physical counts, PO's, PO receipts, PO invoices,"
	    	+ " and inventory transactions created or posted up to the clearing date you select.<BR>"
	    	+ "This is the logic for the clearing:<BR>"
	    	+ "1) All DELETED or already BATCHED physical inventories up to the clearing date are cleared,"
	    	+ " then all related physical inventory worksheets and counts are cleared.<BR>"
	    	+ "2) All IC invoice export sequences up to the clearing date are cleared.<BR>"
	    	+ "3) All PO invoices that have been processed and which have an invoice date up to the clearing date "
	    		+ " are cleared.<BR>"
	    	+ "4) All PO receipts are cleared which: no longer have a corresponding invoice, which have been posted "
	    		+ " to batches, and which have a receipt date up to the clearing date.<BR>"
	    	+ "5) All PO's are cleared which: no longer have a corresponding receipt, which are either completed or "
	    		+ " deleted, and which have a PO date up to the clearing date.<BR>"
	    	+ "6) All IC transactions are cleared which have a transaction date up to the clearing date.<BR>"
	    );
	    
	    String sClearingDate = clsManageRequestParameters.get_Request_Parameter("sClearingDate", request);
		if (sClearingDate.compareToIgnoreCase("") == 0){
			sClearingDate = "1/1/2000";
		}
	    out.println("<TABLE border=1>");
		out.println("<TR>");
		out.println("<TD ALIGN=RIGHT><B>Clearing date</B>:</TD>");
		out.println("<TD>"
				+ clsCreateHTMLFormFields.TDTextBox(
						"ClearingDate", 
						sClearingDate, 
						10, 
						10, 
						""
					) 
				+ SMUtilities.getDatePickerString("StartingDate", getServletContext())
				+ "&nbsp;&nbsp;(Enter as mm/dd/yyyy)"
				+ "</TD>");
		out.println("</TR>");
		
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
