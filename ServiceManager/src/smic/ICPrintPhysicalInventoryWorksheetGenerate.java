package smic;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import smcontrolpanel.SMAuthenticate;
import smcontrolpanel.SMSystemFunctions;
import smcontrolpanel.SMUtilities;
import ConnectionPool.WebContextParameters;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsManageRequestParameters;

public class ICPrintPhysicalInventoryWorksheetGenerate extends HttpServlet {

	private static final long serialVersionUID = 1L;

	private static SimpleDateFormat USDateformatter = new SimpleDateFormat("MM-dd-yyyy hh:mm:ss a EEE");
	
	private String sWarning = "";
	private String sCallingClass = "";
	private String sDBID = "";
	private String sUserID = "";
	private String sUserFullName = "";
	private String sCompanyName = "";
	//private static SimpleDateFormat USTimeOnlyformatter = new SimpleDateFormat("hh:mm:ss a");
	
	public void doGet(HttpServletRequest request,
				HttpServletResponse response)
				throws ServletException, IOException {
		if(!SMAuthenticate.authenticateSMCPCredentials(
				request,	
				response,
				getServletContext(),
				SMSystemFunctions.ICEditPhysicalInventory
			)
		){
			return;
		}
	    response.setContentType("text/html");
		PrintWriter out = response.getWriter();
		
	    //Get the session info:
	    HttpSession CurrentSession = request.getSession(true);
	    sDBID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_DATABASE_ID);
	    sUserID = (String)CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERID);
	    sUserFullName = (String)CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERFIRSTNAME) + " "
	    				+ (String)CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERLASTNAME);
	    sCompanyName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_COMPANYNAME);
	    
	    //sCallingClass will look like: smcontrolpanel.ARAgedTrialBalanceReport
	    sCallingClass = clsManageRequestParameters.get_Request_Parameter("CallingClass", request);
	    String sPhysicalInventoryID 
	    	= clsManageRequestParameters.get_Request_Parameter(ICPhysicalInventoryEntry.ParamID, request);
	    String sWorksheetStartingItem = "";
	    String sWorksheetEndingItem = "";
	    String sWorksheetLocation = "";
	    sWorksheetStartingItem = request.getParameter("WorksheetStartingItem");
	    sWorksheetEndingItem = request.getParameter("WorksheetEndingItem");
	    sWorksheetLocation = request.getParameter("WorksheetLocation");

	    boolean bShowQtyOnHand = (request.getParameter("ShowQtyOnHand") != null);
	    boolean bOutputToCSV = (request.getParameter("OutputToCSV") != null);
	    
	    if (bOutputToCSV){
	    	 response.setContentType("text/csv");
             String disposition = "attachment; fileName=physicalcountworksheet.csv";
             response.setHeader("Content-Disposition", disposition);
	    }else{
	    	String sReportTitle = "IC Physical Count Worksheet";
	
	    	String sCriteria = "Starting with item '<B>" + sWorksheetStartingItem + "</B>'"
	    		+ ", ending with item '<B>" + sWorksheetEndingItem + "</B>', for location"
	    		+ " <B>" + sWorksheetLocation + "</B>."
	    		;
	    	
	    	out.println("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.0 " +
			   "Transitional//EN\">" +
		       "<HTML>" +
		       "<HEAD><TITLE>" + sReportTitle + " - " + sCompanyName + "</TITLE></HEAD>\n<BR>" + 
			   "<BODY BGCOLOR=\"#FFFFFF\">" +
			   "<TABLE BORDER=0 WIDTH=100%>" +
			   "<TR><TD ALIGN=LEFT WIDTH=45%><FONT SIZE=2>" 
			   + USDateformatter.format((new Timestamp(System.currentTimeMillis()))) + " Printed by " + sUserFullName
			   + "</FONT></TD><TD ALIGN=CENTER WIDTH=55%><FONT SIZE=2><B>" + sCompanyName + "</B></FONT></TD></TR>" +
			   "<TR><TD VALIGN=BOTTOM COLSPAN=2><FONT SIZE=2><B>" + sReportTitle + "</B></FONT></TD></TR>" +
			   
			   "<TR><TD COLSPAN=2><FONT SIZE=2>" + sCriteria + "</FONT></TD></TR>");
					   
	    	out.println("<TD><A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMUserLogin?" 
				+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
				+ "\">Return to user login</A><BR>");
	    	out.println("<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smic.ICMainMenu?" 
				+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
				+ "\">Return to Inventory Control Main Menu</A><BR>");
		    out.println("<A HREF=\"" + WebContextParameters.getdocumentationpageURL(getServletContext()) + "#" + Long.toString(SMSystemFunctions.ICTransactionHistory) 
		    		+ "\">Summary</A><BR><BR>");
			out.println("</TD></TR></TABLE>");
	    }
    	//Retrieve information
    	Connection conn = clsDatabaseFunctions.getConnection(
    			getServletContext(), 
    			sDBID, 
    			"MySQL", 
    			SMUtilities.getFullClassName(this.toString()) + " - user: " + sUserID
    			+ " - "
    			+ sUserFullName
    			);
    	if (conn == null){
    		sWarning = "Unable to get data connection.";
    		response.sendRedirect(
    				"" + SMUtilities.getURLLinkBase(getServletContext()) + "" + sCallingClass + "?"
    				+ ICPhysicalInventoryEntry.ParamID + "=" + sPhysicalInventoryID
    				+ "&WorksheetStartingItem=" + sWorksheetStartingItem
    				+ "&WorksheetEndingItem=" + sWorksheetEndingItem
    				+ "&Warning=" + sWarning
    				+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID    		);			
        	return;
    	}
    	
    	ICPhysicalInventoryWorksheet ws = new ICPhysicalInventoryWorksheet();
    	if (!ws.processReport(
    			conn, 
    			sPhysicalInventoryID,
    			sWorksheetStartingItem, 
    			sWorksheetEndingItem, 
    			sDBID,
    			sUserID,
    			bShowQtyOnHand,
    			bOutputToCSV,
    			out)){
    		out.println("Could not print report - " + ws.getErrorMessage());
    	}
    	clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1547080948]");
	    out.println("</BODY></HTML>");
	}
}
