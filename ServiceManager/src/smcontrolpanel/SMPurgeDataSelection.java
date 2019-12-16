package smcontrolpanel;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import ConnectionPool.WebContextParameters;
import ServletUtilities.clsCreateHTMLFormFields;
import ServletUtilities.clsManageRequestParameters;

public class SMPurgeDataSelection extends HttpServlet {
	private static final long serialVersionUID = 1L;
	public static final String PURGE_ORDERS = "PURGEORDERS";
	public static final String PURGE_CUSTOMERCALLLOGS = "PURGECUSTOMERCALLLOGS";
	public static final String PURGE_BIDS = "PURGEBIDS";
	public static final String PURGE_SALESCONTACTS = "PURGESALESCONTACTS";
	public static final String PURGE_SYSTEMLOG = "PURGESYSTEMLOG";
	public static final String PURGE_MATERIALRETURNS = "PURGEMATERIALRETURNS";
	public static final String PURGE_GLDATA = "PURGEGLDATA";
	public static final String PURGE_SECURITYSYSTEMLOGS = "PURGESECURITYSYSTEMLOGS";
	public void doGet(HttpServletRequest request,
				HttpServletResponse response)
				throws ServletException, IOException {
	    
	    response.setContentType("text/html");
		PrintWriter out = response.getWriter();
		if (!SMAuthenticate.authenticateSMCPCredentials(
				request, 
				response, 
				getServletContext(), 
				SMSystemFunctions.SMPurgeData))
		{
			return;
		}

	    //Get the session info:
	    HttpSession CurrentSession = request.getSession(true);
	    String sCompanyName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_COMPANYNAME);
	    String sDBID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_DATABASE_ID);
	    String title = "Purge Data";
	    String subtitle = "";
	    
	    out.println(SMUtilities.SMCPTitleSubBGColor(title, subtitle, SMUtilities.getInitBackGroundColor(getServletContext(), sDBID), sCompanyName));
		out.print(SMUtilities.getDatePickerIncludeString(getServletContext()));
	    String sWarning = clsManageRequestParameters.get_Request_Parameter("Warning", request);
		if (! sWarning.equalsIgnoreCase("")){
			out.println("<B><FONT COLOR=\"RED\">" + sWarning + "</FONT></B><BR>");
		}
	    String sStatus = clsManageRequestParameters.get_Request_Parameter("Status", request);
		if (! sStatus.equalsIgnoreCase("")){
			out.println("<B>" + sStatus + "</B><BR>");
		}
		//Print a link to the first page after login:
	    out.println("<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMUserLogin?" 
				+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
				+ "\">Return to user login</A><BR>");
	    out.println("<A HREF=\"" + WebContextParameters.getdocumentationpageURL(getServletContext()) + "#" + Long.toString(SMSystemFunctions.SMPurgeData) 
	    		+ "\">Summary</A><BR><BR>");
	    
	    out.println("If you choose to PURGE <B>ORDERS</B>, "
	    		+ "this function will purge all of the "
	    		+ " current order data, ONLY if<BR>"
	    		+ "1) All of the lines on the order have an order date PREVIOUS to the purge "
	    		+ "deadline date AND<BR>"
	    		+ "2) There are no unshipped lines on the order (i.e., if the quantity on order is "
	    		+ "zero AND the amount shipped to date is NOT zero).  If the order is canceled, and the "
	    		+ "order date is previous to the purge deadline, then they will be removed, "
	    		+ "even if they have unshipped lines.<BR>"
	    		+ "After those orders are purged, any records linked to the purged orders are also "
	    		+ "purged, including: order details, invoice audit list comments, invoices/credit notes<SUP>1</SUP>, invoice details, "
	    		+ "pre-invoice comments, critical dates, change orders, job cost/truck schedule records, and work orders.<BR><BR>"
	    );

	    out.println("If you choose to PURGE <B>CUSTOMER CALL LOGS</B>, this function will purge all customer call records"
	    		+ " with a call date previous to the purge deadline date.<BR>"
	    );

	    out.println("If you choose to PURGE <B>" + SMBidEntry.ParamObjectName + "</B>, this function will purge all " + SMBidEntry.ParamObjectName.toLowerCase() + "s with a status of Successful,"
	    		+ " Unsuccessful, or Inactive, and with a " + SMBidEntry.ParamObjectName + " origination date previous to the purge deadline date.<BR>"
	    );
	    
	    out.println("If you choose to PURGE <B>SALES CONTACTS</B>, this function will purge all sales contact records which are"
	    		+ " NOT active AND have a last contact date previous "
	    		+ "to the purge deadline date.<BR>"
	    );
	    
	    out.println("If you choose to PURGE the <B>SYSTEM LOG</B>, this function will purge all system log records which"
	    		+ " have a log date previous to the purge deadline date.<BR>"
	    );

	    out.println("If you choose to PURGE the <B>MATERIAL RETURNS</B>, this function will purge all material return records which"
	    		+ " are flagged as resolved, and which have a resolved date previous to the purge deadline date.<BR>"
	    );
	    
	    out.println("If you choose to PURGE the <B>GENERAL LEDGER data</B>, this function will purge all" 
	    	+ " GL export records (from subledger batch postings) and 'external pull' records previous to the purge deadline date. Additionally it will delete"
	    	+ " all financial statement data, fiscal sets, fiscal years, and GL transactions for all fiscal years UP TO BUT NOT INCLUDING the fiscal year"
	    		+ " of the purge deadline date.<BR>"
	    );
	    
	    out.println("If you choose to PURGE the <B>SECURITY SYSTEM LOGS</B>, this function will purge all device event and user event records which"
	    		+ " have a log date previous to the purge deadline date.<BR>"
	    );
	    
    	out.println ("<FORM ACTION =\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMPurgeDataAction\">");
    	out.println("<INPUT TYPE=HIDDEN NAME='" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "' VALUE='" + sDBID + "'>");
    	out.println("<INPUT TYPE=HIDDEN NAME='CallingClass' VALUE='" 
    			+ SMUtilities.getFullClassName(this.toString()) + "'>");

	    out.println("<BR><B>Purge orders?:</B> <INPUT TYPE=CHECKBOX NAME=\"" + PURGE_ORDERS + "\">");
	    out.println("<BR><B>Purge customer call logs?:</B> <INPUT TYPE=CHECKBOX NAME=\"" + PURGE_CUSTOMERCALLLOGS + "\">");
	    out.println("<BR><B>Purge " + SMBidEntry.ParamObjectName.toLowerCase() + "s?:</B> <INPUT TYPE=CHECKBOX NAME=\"" + PURGE_BIDS + "\">");
	    out.println("<BR><B>Purge sales contacts?:</B> <INPUT TYPE=CHECKBOX NAME=\"" + PURGE_SALESCONTACTS + "\">");
	    out.println("<BR><B>Purge system log?:</B> <INPUT TYPE=CHECKBOX NAME=\"" + PURGE_SYSTEMLOG + "\">");
	    out.println("<BR><B>Purge material returns?:</B> <INPUT TYPE=CHECKBOX NAME=\"" + PURGE_MATERIALRETURNS + "\">");
	    out.println("<BR><B>Purge General Ledger data?:</B> <INPUT TYPE=CHECKBOX NAME=\"" + PURGE_GLDATA + "\">");
	    out.println("<BR><B>Purge security system logs?:</B> <INPUT TYPE=CHECKBOX NAME=\"" + PURGE_SECURITYSYSTEMLOGS + "\">");
	    
    	out.println("<TABLE BORDER=1>");
    	
		out.println("<TR>");
		out.println("<TD>" + "<B>Purge deadline date (mm/dd/yyyy):</B></TD><TD>" 
				+ clsCreateHTMLFormFields.TDTextBox("PurgeDeadline", "1/1/2000", 10, 10, "") 
				+ SMUtilities.getDatePickerString("PurgeDeadline", getServletContext())
				+ "</TD>");
		out.println("</TR>");
    	
	    out.println("</TABLE>");

	    out.println ("<INPUT TYPE=\"SUBMIT\" VALUE=\"----Purge Data----\">");
    	out.println("  Check to confirm purge: <INPUT TYPE=CHECKBOX NAME=\"ConfirmPurge\">");
    	out.println ("</FORM>");
	    out.println("<BR><B>NOTE: <I>This may take several minutes; do not stop it until it advises that it finished"
	    		+ " successfully.</I></B><BR>");

    	out.println("<B><SUP>1</SUP></B> If any of the deleted invoices/credit notes are still open in Accounts Receivable,"
    		+ " those A/R transactions will NOT be deleted, but "
			+ "the invoice itself will not be available to be re-printed.");
	    out.println("</BODY></HTML>");
	}
	
}
