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

public class ICPOReceivingReportGenerate extends HttpServlet {

	private static final long serialVersionUID = 1L;

	//formats
	private static SimpleDateFormat USDateformatter = new SimpleDateFormat("MM-dd-yyyy hh:mm:ss a EEE");

	
	//private static SimpleDateFormat USTimeOnlyformatter = new SimpleDateFormat("hh:mm:ss a");
	
	public void doGet(HttpServletRequest request,
				HttpServletResponse response)
				throws ServletException, IOException {
		if (!SMAuthenticate.authenticateSMCPCredentials(
				request, 
				response, 
				getServletContext(),
				SMSystemFunctions.ICPOReceivingReport
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
	    
		
		String sWarning = "";
		String sCallingClass = "";
	    //sCallingClass will look like: smcontrolpanel.ARAgedTrialBalanceReport
	    sCallingClass = clsManageRequestParameters.get_Request_Parameter("CallingClass", request);

	    //Log the report usage:
		SMClasses.SMLogEntry log = new SMClasses.SMLogEntry(sDBID, getServletContext());
		log.writeEntry(sUserID, SMLogEntry.LOG_OPERATION_ICPORECEIVINGREPORT, "REPORT", "ICPOReceivingReport", "[1376509410]");
	   
		String sStartingVendor = clsManageRequestParameters.get_Request_Parameter("StartingVendor", request);
		String sEndingVendor = clsManageRequestParameters.get_Request_Parameter("EndingVendor", request);
		
	    String sStartPODate = clsManageRequestParameters.get_Request_Parameter("StartingPODate", request);
	    String sEndPODate = clsManageRequestParameters.get_Request_Parameter("EndingPODate", request);
	    String sStartArrivalDate = clsManageRequestParameters.get_Request_Parameter("StartingDate", request);
	    String sEndArrivalDate = clsManageRequestParameters.get_Request_Parameter("EndingDate", request);

	    String sStartingPODate = null;
	    
		try {
			sStartingPODate = clsDateAndTimeConversions.utilDateToString(clsDateAndTimeConversions.StringTojavaSQLDate("M/d/yyyy", sStartPODate),"yyyy-MM-dd");
		} catch (ParseException e) {
			sWarning = "Error:[1423581191] Invalid starting PO date: '" + sStartPODate + "' - " + e.getMessage();
    		response.sendRedirect(
				"" + SMUtilities.getURLLinkBase(getServletContext()) + "" + sCallingClass + "?"
				+ "Warning=" + sWarning
				+ "&StartingVendor=" + sStartingVendor
				+ "&EndingVendor=" + sEndingVendor
				+ "&StartingPODate=" + sStartPODate
				+ "&EndingPODate=" + sEndPODate
				+ "&StartingDate=" + sStartArrivalDate
				+ "&EndingDate=" + sEndArrivalDate
				+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
    		);			
        	return;
		}
		String sEndingPODate = null;
		try {
			sEndingPODate = clsDateAndTimeConversions.utilDateToString(clsDateAndTimeConversions.StringTojavaSQLDate("M/d/yyyy", sEndPODate),"yyyy-MM-dd");
		} catch (ParseException e) {
			sWarning = "Error:[1423581192] Invalid ending PO date: '" + sEndPODate + "' - " + e.getMessage();
    		response.sendRedirect(
				"" + SMUtilities.getURLLinkBase(getServletContext()) + "" + sCallingClass + "?"
				+ "Warning=" + sWarning
				+ "&StartingVendor=" + sStartingVendor
				+ "&EndingVendor=" + sEndingVendor
				+ "&StartingPODate=" + sStartPODate
				+ "&EndingPODate=" + sEndPODate
				+ "&StartingDate=" + sStartArrivalDate
				+ "&EndingDate=" + sEndArrivalDate
				+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
    		);			
        	return;
		}
		String sStartingArrivalDate = null;
		try {
			sStartingArrivalDate = clsDateAndTimeConversions.utilDateToString(clsDateAndTimeConversions.StringTojavaSQLDate("M/d/yyyy", sStartArrivalDate),"yyyy-MM-dd");
		} catch (ParseException e) {
			sWarning = "Error:[1423581193] Invalid starting arrival date: '" + sStartArrivalDate + "' - " + e.getMessage();
    		response.sendRedirect(
				"" + SMUtilities.getURLLinkBase(getServletContext()) + "" + sCallingClass + "?"
				+ "Warning=" + sWarning
				+ "&StartingVendor=" + sStartingVendor
				+ "&EndingVendor=" + sEndingVendor
				+ "&StartingPODate=" + sStartPODate
				+ "&EndingPODate=" + sEndPODate
				+ "&StartingDate=" + sStartArrivalDate
				+ "&EndingDate=" + sEndArrivalDate
				+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
    		);			
        	return;
		}
		String sEndingArrivalDate = null;
		try {
			sEndingArrivalDate = clsDateAndTimeConversions.utilDateToString(clsDateAndTimeConversions.StringTojavaSQLDate("M/d/yyyy", sEndArrivalDate),"yyyy-MM-dd");
		} catch (ParseException e) {
			sWarning = "Error:[1423581194] Invalid ending arrival date: '" + sEndArrivalDate + "' - " + e.getMessage();
    		response.sendRedirect(
				"" + SMUtilities.getURLLinkBase(getServletContext()) + "" + sCallingClass + "?"
				+ "Warning=" + sWarning
				+ "&StartingVendor=" + sStartingVendor
				+ "&EndingVendor=" + sEndingVendor
				+ "&StartingPODate=" + sStartPODate
				+ "&EndingPODate=" + sEndPODate
				+ "&StartingDate=" + sStartArrivalDate
				+ "&EndingDate=" + sEndArrivalDate
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
				+ "&StartingDate=" + sStartArrivalDate
				+ "&EndingDate=" + sEndArrivalDate
				+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
			);
	    }

    	String sReportTitle = "IC Items To Be Received Listed By PO";   
    	String sCriteria = "Starting with PO date '<B>" + sStartingPODate + "</B>'"
    		+ ", ending with '<B>" + sEndingPODate + "</B>'";
    	sCriteria += ", starting with arrival date '<B>" + sStartingArrivalDate + "</B>'"
    		+ ", ending with '<B>" + sEndingArrivalDate + "</B>'";
    	sCriteria = sCriteria + ", for vendors: '" + sStartingVendor + "' through '" + sEndingVendor + "'";
   		sCriteria = sCriteria + ", for locations: ";
    	for (int i = 0; i < sLocations.size(); i++){
    		if (i == 0){
    			sCriteria += "<B>" + sLocations.get(i) + "</B>";
    		}else{
    			sCriteria += ", <B>" + sLocations.get(i) + "</B>";
    		}
    	}
    	sCriteria += " - NOT INCLUDING PO'S ON HOLD.";
	    boolean bOutputToCSV = (request.getParameter("OutputToCSV") != null);
	    String sColor = SMUtilities.getInitBackGroundColor(getServletContext(), sDBID);
	    
	    String sPageHeader = "<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.0 " +
			   "Transitional//EN\">" +
		       "<HTML>" +
		       "<HEAD><TITLE>" + sReportTitle + " - " + sCompanyName + "</TITLE></HEAD>\n<BR>" + 
			   "<BODY BGCOLOR=\"#FFFFFF\">" +
			   "<TABLE BORDER=0 WIDTH=100% BGCOLOR = \"" + sColor + "\">" +
			   "<TR><TD ALIGN=LEFT WIDTH=45%><FONT SIZE=2>" 
			   + USDateformatter.format((new Timestamp(System.currentTimeMillis()))) 
			   + " Printed by " + SMUtilities.getFullNamebyUserID(sUserID, getServletContext(), sDBID, "ICPOReceivingReportGenerate") 
			   + "</FONT></TD><TD ALIGN=CENTER WIDTH=55%><FONT SIZE=2><B>" + sCompanyName + "</B></FONT></TD></TR>" +
			   "<TR><TD VALIGN=BOTTOM COLSPAN=2><FONT SIZE=2><B>" + sReportTitle + "</B></FONT></TD></TR>" +
			   
			   "<TR><TD COLSPAN=2><FONT SIZE=2>" + sCriteria + "</FONT></TD></TR>"
					   
			   + "<TD><A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMUserLogin?" 
			   + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
			   + "\">Return to user login</A><BR>"
			   + "<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smic.ICMainMenu?" 
			   + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
			   + "\">Return to Inventory Control Main Menu</A><BR>"
			   + "<A HREF=\"" + WebContextParameters.getdocumentationpageURL(getServletContext()) + "#" + Long.toString(SMSystemFunctions.ICPOReceivingReport) 
			   + "\">Summary</A><BR><BR>"
			   + "</TD></TR></TABLE>"
	    ;
	    

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
    				+ "&StartingDate=" + sStartArrivalDate
    				+ "&EndingDate=" + sEndArrivalDate
    				+ "&Warning=" + sWarning
    				+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID    		);			
        	return;
    	}
    	if(!bOutputToCSV) {
        	out.println(sPageHeader);
    	}
    	out.println(SMUtilities.getMasterStyleSheetLink());
    	
    	ICPOReceivingReport rpt = new ICPOReceivingReport();
    	String sReportBody = "";
    	try {
    		sReportBody = rpt.processReport(
					conn, 
					sLocations,
					sStartingPODate,
					sEndingPODate,
					sStartingArrivalDate,
					sEndingArrivalDate,
					sStartingVendor,
					sEndingVendor,
					sDBID,
					sUserID,
					bOutputToCSV,
					getServletContext(),
					(String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_LICENSE_MODULE_LEVEL))
			;
		} catch (Exception e) {
			
			out.println("Error [1545430039] - Could not print report - " + e.getMessage());
			out.println("</BODY></HTML>");
			return;

		}
    	
	    if (bOutputToCSV){
	    	response.setContentType("text/csv");
            String disposition = "attachment; fileName=poreceiving.csv";
            response.setHeader("Content-Disposition", disposition);
            out.println(sReportBody);
	    }else{
	    	out.println(sReportBody);
		    out.println("</BODY></HTML>");
	    }
    		
    	clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1547080944]");
    	return;
	}
}
