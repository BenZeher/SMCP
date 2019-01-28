package smic;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import smcontrolpanel.SMAuthenticate;
import smcontrolpanel.SMSystemFunctions;
import smcontrolpanel.SMUtilities;
import ConnectionPool.WebContextParameters;
import SMClasses.SMLogEntry;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsDateAndTimeConversions;
import ServletUtilities.clsManageRequestParameters;

public class ICItemsReceivedNotInvoicedGenerate extends HttpServlet {

	private static final long serialVersionUID = 1L;

	//formats
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
				SMSystemFunctions.ICListItemsReceived
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
	    String sUserFirstName = (String)CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERFIRSTNAME);
	    String sUserLastName = (String)CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERLASTNAME);
	    
	    String sCompanyName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_COMPANYNAME);
	    
	    //sCallingClass will look like: smcontrolpanel.ARAgedTrialBalanceReport
	    sCallingClass = clsManageRequestParameters.get_Request_Parameter("CallingClass", request);

	    //Log the report usage:
		SMClasses.SMLogEntry log = new SMClasses.SMLogEntry(sDBID, getServletContext());
		log.writeEntry(sUserID, SMLogEntry.LOG_OPERATION_ICITEMSRECEIVEDNOTINVOICED, "REPORT", "ICItemsReceivedNotInvoiced", "[1376509402]");
		String sVendor = clsManageRequestParameters.get_Request_Parameter(ICItemsReceivedNotInvoicedSelection.PARAM_VENDOR, request).trim();
	   
		String sStartDate = "";
	    String sEndDate = "";
		sStartDate = request.getParameter("StartingDate");
		sEndDate = request.getParameter("EndingDate");
		if(
				(!clsDateAndTimeConversions.IsValidDateString("M/d/yyyy", sStartDate))
				|| (!clsDateAndTimeConversions.IsValidDateString("M/d/yyyy", sEndDate))
		){
			sWarning = "Invalid starting date: '" + sStartDate + "'";
    		response.sendRedirect(
				"" + SMUtilities.getURLLinkBase(getServletContext()) + "" + sCallingClass + "?"
				+ "Warning=" + sWarning
				+ "&StartingDate=" + sStartDate
				+ "&EndingDate=" + sEndDate
				+ "&" + ICItemsReceivedNotInvoicedSelection.PARAM_VENDOR + "=" + sVendor
				+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
    		);			
        	return;
		}
		boolean bIncludeStockInventoryItems = false;
		boolean bIncludeNonStockInventoryItems = false;
		boolean bIncludeNonInventoryItems = false;
		if (request.getParameter(ICItemsReceivedNotInvoicedSelection.PARAM_INCLUDE_STOCK_INVENTORY_ITEMS) != null){
			bIncludeStockInventoryItems = true;
		}
		if (request.getParameter(ICItemsReceivedNotInvoicedSelection.PARAM_INCLUDE_NONSTOCK_INVENTORY_ITEMS) != null){
			bIncludeNonStockInventoryItems = true;
		}
		if (request.getParameter(ICItemsReceivedNotInvoicedSelection.PARAM_INCLUDE_NONINVENTORY_ITEMS) != null){
			bIncludeNonInventoryItems = true;
		}
		
		if (!bIncludeStockInventoryItems && !bIncludeNonInventoryItems && !bIncludeNonStockInventoryItems){
			sWarning = "You must select at least STOCK INVENTORY items, NON-STOCK INVENTORY ITEMS or NON-INVENTORY items.";
    		response.sendRedirect(
				"" + SMUtilities.getURLLinkBase(getServletContext()) + "" + sCallingClass + "?"
				+ "Warning=" + sWarning
				+ "&StartingDate=" + sStartDate
				+ "&EndingDate=" + sEndDate
				+ "&" + ICItemsReceivedNotInvoicedSelection.PARAM_VENDOR + "=" + sVendor
				+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
    		);			
        	return;
		}
		
		boolean bIncludeInvoicedItems = false;
		boolean bIncludeUnInvoicedItems = false;
		if (request.getParameter(ICItemsReceivedNotInvoicedSelection.PARAM_INCLUDE_INVOICED_ITEMS) != null){
			bIncludeInvoicedItems = true;
		}
		if (request.getParameter(ICItemsReceivedNotInvoicedSelection.PARAM_INCLUDE_NONINVOICED_ITEMS) != null){
			bIncludeUnInvoicedItems = true;
		}
		
		if (!bIncludeInvoicedItems && !bIncludeUnInvoicedItems){
			sWarning = "You must select at least either INVOICED items or UN-INVOICED items.";
    		response.sendRedirect(
				"" + SMUtilities.getURLLinkBase(getServletContext()) + "" + sCallingClass + "?"
				+ "Warning=" + sWarning
				+ "&StartingDate=" + sStartDate
				+ "&EndingDate=" + sEndDate
				+ "&" + ICItemsReceivedNotInvoicedSelection.PARAM_VENDOR + "=" + sVendor
				+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
    		);			
        	return;
		}
		
    	//Get the list of selected locations:
    	ArrayList<String> sLocations = new ArrayList<String>(0);
	    Enumeration<String> paramLocationNames = request.getParameterNames();
	    String sParamLocationName = "";
	    String sLocationMarker = "LOCATION";
	    while(paramLocationNames.hasMoreElements()) {
	    	sParamLocationName = paramLocationNames.nextElement();
		  if (sParamLocationName.contains(sLocationMarker)){
			  sLocations.add(sParamLocationName.substring(
					  sParamLocationName.indexOf(sLocationMarker) + sLocationMarker.length()));
		  }
	    }
	    Collections.sort(sLocations);
		
	    if (sLocations.size() == 0){
    		sWarning = "You must select at least one location.";
    		response.sendRedirect(
				"" + SMUtilities.getURLLinkBase(getServletContext()) + "" + sCallingClass + "?"
				+ "Warning=" + sWarning
				+ "&StartingDate=" + sStartDate
				+ "&EndingDate=" + sEndDate
				+ "&" + ICItemsReceivedNotInvoicedSelection.PARAM_VENDOR + "=" + sVendor
				+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
			);
	    }

		String sStartingDate;
		try {
			sStartingDate = clsDateAndTimeConversions.utilDateToString(clsDateAndTimeConversions.StringTojavaSQLDate("M/d/yyyy", sStartDate),"yyyy-MM-dd");
		} catch (ParseException e) {
			sWarning = "Error:[1423771768] Invalid starting date: '" + sStartDate + "' - " + e.getMessage();
    		response.sendRedirect(
				"" + SMUtilities.getURLLinkBase(getServletContext()) + "" + sCallingClass + "?"
				+ "Warning=" + sWarning
				+ "&StartingDate=" + sStartDate
				+ "&EndingDate=" + sEndDate
				+ "&" + ICItemsReceivedNotInvoicedSelection.PARAM_VENDOR + "=" + sVendor
				+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
    		);			
        	return;
		}
		String sEndingDate;
		try {
			sEndingDate = clsDateAndTimeConversions.utilDateToString(clsDateAndTimeConversions.StringTojavaSQLDate("M/d/yyyy", sEndDate),"yyyy-MM-dd");
		} catch (ParseException e) {
			sWarning = "Error:[1423771769] Invalid ending date: '" + sEndDate + "' - " + e.getMessage();
    		response.sendRedirect(
				"" + SMUtilities.getURLLinkBase(getServletContext()) + "" + sCallingClass + "?"
				+ "Warning=" + sWarning
				+ "&StartingDate=" + sStartDate
				+ "&EndingDate=" + sEndDate
				+ "&" + ICItemsReceivedNotInvoicedSelection.PARAM_VENDOR + "=" + sVendor
				+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
    		);			
        	return;
		}
	    
    	String sReportTitle = "IC Items Received";    	
    	String sCriteria = "Starting with received date '<B>" + sStartingDate + "</B>'"
    		+ ", ending with '<B>" + sEndingDate + "</B>'";
   		sCriteria = sCriteria + ", for locations: ";
    	for (int i = 0; i < sLocations.size(); i++){
    		if (i == 0){
    			sCriteria += "<B>" + sLocations.get(i) + "</B>";
    		}else{
    			sCriteria += ", <B>" + sLocations.get(i) + "</B>";
    		}
    	}

    	if (bIncludeStockInventoryItems){
    		sCriteria += ", including <B> STOCK INVENTORY</B> items";
    	}
    	if (bIncludeNonStockInventoryItems){
    		sCriteria += ", including <B> NON-STOCK INVENTORY</B> items";
    	}
    	if (bIncludeNonInventoryItems){
    		sCriteria += ", including <B>NON-INVENTORY</B> items";
    	}
    	if (bIncludeInvoicedItems){
    		sCriteria += ", including <B>INVOICED items</B>";
    	}
    	if (bIncludeUnInvoicedItems){
    		sCriteria += ", including <B>NON-INVOICED</B> items";
    	}
    	if (sVendor.compareToIgnoreCase("") == 0){
    		sCriteria += ", for <B>ALL</B> vendors";
    	}else{
    		sCriteria += ", for vendor '<B>" + sVendor + "'</B>";
    	}
    	sCriteria += ".";
    	
    	out.println("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.0 " +
		   "Transitional//EN\">" +
	       "<HTML>" +
	       "<HEAD><TITLE>" + sReportTitle + " - " + sCompanyName + "</TITLE></HEAD>\n<BR>" + 
		   "<BODY BGCOLOR=\"#FFFFFF\">" +
		   "<TABLE BORDER=0 WIDTH=100%>" +
		   "<TR><TD ALIGN=LEFT WIDTH=45%><FONT SIZE=2>" 
		   + USDateformatter.format((new Timestamp(System.currentTimeMillis()))) 
		   + " Printed by " + SMUtilities.getFullNamebyUserID(sUserID, getServletContext(), sDBID, "ICItemsReceivedNotInvoicedGenerate") 
		   + "</FONT></TD><TD ALIGN=CENTER WIDTH=55%><FONT SIZE=2><B>" + sCompanyName + "</B></FONT></TD></TR>" +
		   "<TR><TD VALIGN=BOTTOM COLSPAN=2><FONT SIZE=2><B>" + sReportTitle + "</B></FONT></TD></TR>" +
		   
		   "<TR><TD COLSPAN=2><FONT SIZE=2>" + sCriteria + "</FONT></TD></TR>");
				   
    	out.println("<TD><A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMUserLogin?" 
				+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
				+ "\">Return to user login</A><BR>");
    	out.println("<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smic.ICMainMenu?" 
				+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
				+ "\">Return to Inventory Control Main Menu</A><BR>");
	    out.println("<A HREF=\"" + WebContextParameters.getdocumentationpageURL(getServletContext()) + "#" + Long.toString(SMSystemFunctions.ICListItemsReceived) 
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
    			+ sUserFirstName
    			+ " "
    			+ sUserLastName
    	);
    	if (conn == null){
    		sWarning = "Unable to get data connection.";
    		response.sendRedirect(
    				"" + SMUtilities.getURLLinkBase(getServletContext()) + "" + sCallingClass + "?"
    				+ "&StartingDate=" + sStartDate
    				+ "&EndingDate=" + sEndDate
    				+ "&Warning=" + sWarning
    				+ "&" + ICItemsReceivedNotInvoicedSelection.PARAM_VENDOR + "=" + sVendor
    				+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID    		
    		);			
        	return;
    	}
    	
    	ICItemsReceivedNotInvoicedReport rpt = new ICItemsReceivedNotInvoicedReport();
    	if (!rpt.processReport(
    			conn, 
    			sLocations, 
    			sStartingDate,
    			sEndingDate,
    			sVendor,
    			bIncludeStockInventoryItems,
    			bIncludeNonStockInventoryItems,
    			bIncludeNonInventoryItems,
    	    	bIncludeInvoicedItems,
    	    	bIncludeUnInvoicedItems,
    			sDBID,
    			sUserID,
    			out,
    			getServletContext(),
    			(String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_LICENSE_MODULE_LEVEL))){
    		out.println("Could not print report - " + rpt.getErrorMessage());
    	}
    	clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1547080866]");
	    out.println("</BODY></HTML>");
	}
}
