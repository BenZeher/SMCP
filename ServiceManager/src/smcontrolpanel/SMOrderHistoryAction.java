package smcontrolpanel;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import ConnectionPool.WebContextParameters;
import ServletUtilities.clsDateAndTimeConversions;
import ServletUtilities.clsManageRequestParameters;

public class SMOrderHistoryAction extends HttpServlet {

	private static final long serialVersionUID = 1L;
	private static SimpleDateFormat USDateformatter = new SimpleDateFormat("MM-dd-yyyy hh:mm:ss a EEE");
	
	public void doGet(HttpServletRequest request,
				HttpServletResponse response)
				throws ServletException, IOException {
		if (!SMAuthenticate.authenticateSMCPCredentials(
				request, 
				response, 
				getServletContext(),
				SMSystemFunctions.SMOrderHistory
			)
		){
			return;
		}
	    response.setContentType("text/html");
		PrintWriter out = response.getWriter();
		
	    //Get the session info:
	    HttpSession CurrentSession = request.getSession(true);
	    String sDBID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_DATABASE_ID);
	    String sUserName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERNAME);
	    String sUserID = (String)CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERID);
	    String sUserFullName = (String)CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERFIRSTNAME) + " "
	    				+ (String)CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERLASTNAME);
	    String sCompanyName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_COMPANYNAME);
	    
	    //sCallingClass will look like: smcontrolpanel.ARAgedTrialBalanceReport
	    String sCallingClass = clsManageRequestParameters.get_Request_Parameter("CallingClass", request);

	    String m_sWarning = "";
	    
	    String sStartingOrderDate = clsManageRequestParameters.get_Request_Parameter(SMOrderHistorySelection.STARTING_ORDER_DATE_PARAM, request); 
	    String sEndingOrderDate = clsManageRequestParameters.get_Request_Parameter(SMOrderHistorySelection.ENDING_ORDER_DATE_PARAM, request); 
	    
    	//Convert the date to a SQL one:
	    java.sql.Date datOrderStartingDate = null;
		try {
			datOrderStartingDate = clsDateAndTimeConversions.StringTojavaSQLDate("M/d/yyyy", sStartingOrderDate);
		} catch (ParseException e) {
			m_sWarning = "Error:[1423578212] Invalid starting date '" + datOrderStartingDate + "' - " + e.getMessage();
			response.sendRedirect(
					"" + SMUtilities.getURLLinkBase(getServletContext()) + "" + sCallingClass + "?"
					+ "" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
					+ "&Warning=" + m_sWarning
			);
			return;
		}
    	java.sql.Date datOrderEndingDate = null;
		try {
			datOrderEndingDate = clsDateAndTimeConversions.StringTojavaSQLDate("M/d/yyyy", sEndingOrderDate);
		} catch (ParseException e) {
			m_sWarning = "Error:[1423578213] Invalid ending date '" + datOrderEndingDate + "' - " + e.getMessage();
			response.sendRedirect(
					"" + SMUtilities.getURLLinkBase(getServletContext()) + "" + sCallingClass + "?"
					+ "" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
					+ "&Warning=" + m_sWarning
			);
			return;
		}
	    
		boolean bDownloadToHTML = false;
		if (request.getParameter(SMOrderHistorySelection.DOWNLOAD_TO_HTML) != null){
			bDownloadToHTML = true;
		}
		
    	String sReportTitle = "Order history";

    	String sCriteria = "Including all orders with an order date STARTING WITH '<B>" + sStartingOrderDate + "</B>' and ENDING WITH '<B>" + sEndingOrderDate + "</B>'.";

		//If the user chose to download it:
		if (bDownloadToHTML){
			String disposition = "attachment; fileName= " + "PURGEORDERLIST " + clsDateAndTimeConversions.now("MM-dd-yyyy hh:mm") + ".html";
			response.setHeader("Content-Disposition", disposition);
		}    	
    	out.println("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.0 " +
		   "Transitional//EN\">" +
	       "<HTML>" +
	       "<HEAD><TITLE>" + sReportTitle + " - " + sCompanyName + "</TITLE></HEAD>\n<BR>" + 
		   "<BODY BGCOLOR=\"#FFFFFF\">" +
		   "<TABLE BORDER=0 WIDTH=100%>" +
		   "<TR><TD ALIGN=LEFT WIDTH=45%><FONT SIZE=2>" 
		   + USDateformatter.format((new Timestamp(System.currentTimeMillis()))) + " Printed by " + sUserName 
		   + "</FONT></TD><TD ALIGN=CENTER WIDTH=55%><FONT SIZE=2><B>" + sCompanyName + "</B></FONT></TD></TR>" +
		   "<TR><TD VALIGN=BOTTOM COLSPAN=2><FONT SIZE=2><B>" + sReportTitle + "</B></FONT></TD></TR>" +
		   
		   "<TR><TD COLSPAN=2><FONT SIZE=2>" + sCriteria + "</FONT></TD></TR>");
				   
    	out.println("<TD><A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMUserLogin?" 
				+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
				+ "\">Return to user login</A><BR>");
	    out.println("<A HREF=\"" + WebContextParameters.getdocumentationpageURL(getServletContext()) + "#" + Long.toString(SMSystemFunctions.SMOrderHistory) 
	    		+ "\">Summary</A><BR><BR>");
		out.println("</TD></TR></TABLE>");
    	
    	//Retrieve information
    	SMOrderHistoryReport por = new SMOrderHistoryReport();
    	if (!por.processReport(
    			sDBID, 
    			datOrderStartingDate,
    			datOrderEndingDate, 
    			sUserName,
    			sUserID,
    			sUserFullName,
    			out,
    			getServletContext())){
    		out.println("Could not print report - " + por.getErrorMessage());
    	}
	    out.println("</BODY></HTML>");
	}
}
