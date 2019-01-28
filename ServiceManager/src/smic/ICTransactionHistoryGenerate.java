package smic;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.Timestamp;
import java.text.ParseException;
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
import ServletUtilities.clsDateAndTimeConversions;
import ServletUtilities.clsManageRequestParameters;

public class ICTransactionHistoryGenerate extends HttpServlet {

	private static final long serialVersionUID = 1L;

	private static SimpleDateFormat USDateformatter = new SimpleDateFormat("MM-dd-yyyy hh:mm:ss a EEE");
	
	private String sWarning = "";
	private String sCallingClass = "";

	//private static SimpleDateFormat USTimeOnlyformatter = new SimpleDateFormat("hh:mm:ss a");
	
	public void doGet(HttpServletRequest request,
				HttpServletResponse response)
				throws ServletException, IOException {
		if (!SMAuthenticate.authenticateSMCPCredentials(
				request, 
				response, 
				getServletContext(),
				SMSystemFunctions.ICTransactionHistory
			)
		){
			return;
		}
	    response.setContentType("text/html");
		PrintWriter out = response.getWriter();
		
	    //Get the session info:
	    HttpSession CurrentSession = request.getSession(true);
	    String sDBID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_DATABASE_ID);
	    String sUserID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERID);
	    String sUserFullName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERFIRSTNAME) + " "
	    				+ (String)CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERLASTNAME);
	    String sCompanyName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_COMPANYNAME);
	    
	    //sCallingClass will look like: smcontrolpanel.ARAgedTrialBalanceReport
	    sCallingClass = clsManageRequestParameters.get_Request_Parameter("CallingClass", request);

	    String sStartingItem = "";
	    String sEndingItem = "";
	    sStartingItem = request.getParameter("StartingItemNumber");
	    sEndingItem = request.getParameter("EndingItemNumber");
	    
	    String sStartDate = "";
	    String sEndDate = "";
	    
		sStartDate = request.getParameter("StartingDate");
		sEndDate = request.getParameter("EndingDate");
		
		String sStartingDate;
		try {
			sStartingDate = clsDateAndTimeConversions.utilDateToString(clsDateAndTimeConversions.StringTojavaSQLDate("M/d/yyyy", sStartDate),"yyyy-MM-dd");
		} catch (ParseException e) {
			sWarning = "Error:[1423771910] Invalid starting date: '" + sStartDate + "' - " + e.getMessage();
    		response.sendRedirect(
				"" + SMUtilities.getURLLinkBase(getServletContext()) + "" + sCallingClass + "?"
				+ "Warning=" + sWarning
				+ "&StartingItemNumber=" + sStartingItem
				+ "&EndingItemNumber=" + sEndingItem
				+ "&StartingDate=" + sStartDate
				+ "&EndingDate=" + sEndDate
				+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
    		);			
        	return;
		}
		String sEndingDate;
		try {
			sEndingDate = clsDateAndTimeConversions.utilDateToString(clsDateAndTimeConversions.StringTojavaSQLDate("M/d/yyyy", sEndDate),"yyyy-MM-dd");
		} catch (ParseException e) {
			sWarning = "Error:[1423771911] Invalid ending date: '" + sEndDate + "' - " + e.getMessage();
    		response.sendRedirect(
    				"" + SMUtilities.getURLLinkBase(getServletContext()) + "" + sCallingClass + "?"
    				+ "&StartingItemNumber=" + sStartingItem
    				+ "&EndingItemNumber=" + sEndingItem
    				+ "&StartingDate=" + sStartDate
    				+ "&EndingDate=" + sEndDate
    				+ "&Warning=" + sWarning
    				+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
        		);			
            	return;
		}
	    
		boolean bShowCostingDetails = false;
		if (request.getParameter("SHOWCOSTINGDETAILS") != null){
			bShowCostingDetails = true;
		}
		
		boolean bIncludeShipments = request.getParameter(
			"TRANSACTIONTYPE" + Integer.toString(ICEntryTypes.SHIPMENT_ENTRY)) != null;
		boolean bIncludeReceipts = request.getParameter(
			"TRANSACTIONTYPE" + Integer.toString(ICEntryTypes.RECEIPT_ENTRY)) != null;
		boolean bIncludeAdjustments = request.getParameter(
			"TRANSACTIONTYPE" + Integer.toString(ICEntryTypes.ADJUSTMENT_ENTRY)) != null;
		boolean bIncludeTransfers = request.getParameter(
			"TRANSACTIONTYPE" + Integer.toString(ICEntryTypes.TRANSFER_ENTRY)) != null;
		boolean bIncludePhysicalCounts = request.getParameter(
			"TRANSACTIONTYPE" + Integer.toString(ICEntryTypes.PHYSICALCOUNT_ENTRY)) != null;

    	String sReportTitle = "IC Transaction History";

    	String sCriteria = "Starting with item '<B>" + sStartingItem + "</B>'"
    		+ ", ending with item '<B>" + sEndingItem + "</B>'";
    	
   		sCriteria += ", starting on <B>" + sStartDate + "</B> and going through <B>" + sEndDate + "</B>";
   		
   		String sTransactionTypeString = "";
   		if (bIncludeShipments){
   			sTransactionTypeString += ", <B>including</B> Shipments";
   		}else{
   			sTransactionTypeString += ", <B>NOT including</B> Shipments";
   		}
   		if (bIncludeReceipts){
   			sTransactionTypeString += ", <B>including</B> Receipts";
   		}else{
   			sTransactionTypeString += ", <B>NOT including</B> Receipts";
   		}
   		if (bIncludeAdjustments){
   			sTransactionTypeString += ", <B>including</B> Adjustments";
   		}else{
   			sTransactionTypeString += ", <B>NOT including</B> Adjustments";
   		}
   		if (bIncludeTransfers){
   			sTransactionTypeString += ", <B>including</B> Transfers";
   		}else{
   			sTransactionTypeString += ", <B>NOT including</B> Transfers";
   		}
   		if (bIncludePhysicalCounts){
   			sTransactionTypeString += ", <B>including</B> Physical counts.";
   		}else{
   			sTransactionTypeString += ", <B>NOT including</B> Physical counts.";
   		}
   		sCriteria += sTransactionTypeString;
   		//if (bShowCostingDetails){
   		//	sCriteria += ", <B>SHOWING</B> costing details.";
   		//}else{
   		//	sCriteria += ", <B>NOT SHOWING</B> costing details.";
   		//}

    	out.println("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.0 " +
		   "Transitional//EN\">" +
	       "<HTML>" +
	       "<HEAD><TITLE>" + sReportTitle + " - " + sCompanyName + "</TITLE></HEAD>\n<BR>" + 
		   "<BODY BGCOLOR=\"#FFFFFF\">" +
		   "<TABLE BORDER=0 WIDTH=100%>" +
		   "<TR><TD ALIGN=LEFT WIDTH=45%><FONT SIZE=2>" 
		   + USDateformatter.format((new Timestamp(System.currentTimeMillis()))) 
		   + " Printed by " + SMUtilities.getFullNamebyUserID(sUserID, getServletContext(), sDBID, "ICTransactionHistoryGenerate") 
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
    	
    	//Retrieve information
    	Connection conn = clsDatabaseFunctions.getConnection(
    			getServletContext(), 
    			sDBID, 
    			"MySQL", 
    			SMUtilities.getFullClassName(this.toString()) 
    			+ " - user: " 
    			+ sUserID
    			+ " - "
    			+ sUserFullName
    			);
    	if (conn == null){
    		sWarning = "Unable to get data connection.";
    		String sTransactionTypesParameter = "";
    		if (bIncludeShipments){
    			sTransactionTypesParameter += "&TRANSACTIONTYPE" 
    				+ Integer.toString(ICEntryTypes.SHIPMENT_ENTRY) + "=true";
    		}
    		if (bIncludeReceipts){
    			sTransactionTypesParameter += "&TRANSACTIONTYPE" 
    				+ Integer.toString(ICEntryTypes.RECEIPT_ENTRY) + "=true";
    		}
    		if (bIncludeTransfers){
    			sTransactionTypesParameter += "&TRANSACTIONTYPE" 
    				+ Integer.toString(ICEntryTypes.TRANSFER_ENTRY) + "=true";
    		}
    		if (bIncludeAdjustments){
    			sTransactionTypesParameter += "&TRANSACTIONTYPE" 
    				+ Integer.toString(ICEntryTypes.ADJUSTMENT_ENTRY) + "=true";
    		}
    		if (bIncludePhysicalCounts){
    			sTransactionTypesParameter += "&TRANSACTIONTYPE" 
    				+ Integer.toString(ICEntryTypes.PHYSICALCOUNT_ENTRY) + "=true";
    		}

    		response.sendRedirect(
    				"" + SMUtilities.getURLLinkBase(getServletContext()) + "" + sCallingClass + "?"
    				+ "StartingItemNumber=" + sStartingItem
    				+ "&EndingItemNumber=" + sEndingItem
    				+ "&StartingDate=" + sStartDate
    				+ "&EndingDate=" + sEndDate
    				+ sTransactionTypesParameter
    				+ "&Warning=" + sWarning
    				+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID    		);			
        	return;
    	}
    	
    	ICTransactionHistory transhist = new ICTransactionHistory();
    	if (!transhist.processReport(
    			conn, 
    			sStartingItem, 
    			sEndingItem, 
    			sStartingDate,
    			sEndingDate,
    			bShowCostingDetails,
    			bIncludeShipments,
    			bIncludeReceipts,
    			bIncludeAdjustments,
    			bIncludeTransfers,
    			bIncludePhysicalCounts,
    			sDBID,
    			sUserID,
    			sUserFullName,
    			out,
    			getServletContext(),
    			(String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_LICENSE_MODULE_LEVEL))){
    		out.println("Could not print report - " + transhist.getErrorMessage());
    	}
    	clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1547080993]");
	    out.println("</BODY></HTML>");
	}
}
