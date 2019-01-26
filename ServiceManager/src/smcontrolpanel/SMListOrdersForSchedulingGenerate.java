package smcontrolpanel;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Enumeration;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import ConnectionPool.WebContextParameters;
import SMDataDefinition.SMTableservicetypes;
import ServletUtilities.clsServletUtilities;
import ServletUtilities.clsDateAndTimeConversions;
import ServletUtilities.clsManageRequestParameters;

public class SMListOrdersForSchedulingGenerate extends HttpServlet {

	private static final long serialVersionUID = 1L;
	
	//private boolean bDebugMode = false;
	
	public void doPost(HttpServletRequest request,
			HttpServletResponse response)
	throws ServletException, IOException {

		if (!SMAuthenticate.authenticateSMCPCredentials(
				request, 
				response, 
				getServletContext(), 
				SMSystemFunctions.SMListOrdersForScheduling)
		){
			return;
		}

		PrintWriter out = response.getWriter();
		//Get the session info:
		HttpSession CurrentSession = request.getSession(true);
		String sDBID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_DATABASE_ID);
		String sUserID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERID);
		String sCompanyName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_COMPANYNAME);
		String sUserFullName = SMUtilities.getFullNamebyUserID(sUserID, getServletContext(), sDBID, this.toString());
		String sWarning = "";
		String sCallingClass = clsManageRequestParameters.get_Request_Parameter("CallingClass", request);
		String sRedirectString = "";
		sRedirectString = SMUtilities.getURLLinkBase(getServletContext()) + sCallingClass;
		
		boolean bDateRangeChosen = false;
		boolean bDateRangeToday = false;
		boolean bDateRangeThisWeek = false;
		boolean bDateRangeNextWeek = false;
		if (clsManageRequestParameters.get_Request_Parameter(
			SMListOrdersForSchedulingSelection.DATE_RANGE_PARAM, request).compareToIgnoreCase(
			SMListOrdersForSchedulingSelection.DATE_RANGE_CHOOSE) == 0){
			bDateRangeChosen = true;
		}
		if (clsManageRequestParameters.get_Request_Parameter(
			SMListOrdersForSchedulingSelection.DATE_RANGE_PARAM, request).compareToIgnoreCase(
			SMListOrdersForSchedulingSelection.DATE_RANGE_TODAY) == 0){
			bDateRangeToday = true;
		}
		if (clsManageRequestParameters.get_Request_Parameter(
			SMListOrdersForSchedulingSelection.DATE_RANGE_PARAM, request).compareToIgnoreCase(
			SMListOrdersForSchedulingSelection.DATE_RANGE_THISWEEK) == 0){
			bDateRangeThisWeek = true;
		}
		if (clsManageRequestParameters.get_Request_Parameter(
			SMListOrdersForSchedulingSelection.DATE_RANGE_PARAM, request).compareToIgnoreCase(
			SMListOrdersForSchedulingSelection.DATE_RANGE_NEXTWEEK) == 0){
			bDateRangeNextWeek = true;
		}
		
		//Get the starting and ending dates:
		//Default both to today:
		String sStartingDate = clsDateAndTimeConversions.now("MM/dd/yyyy");
		String sEndingDate = clsDateAndTimeConversions.now("MM/dd/yyyy");
		if (bDateRangeChosen){
			sStartingDate = clsManageRequestParameters.get_Request_Parameter(
				SMListOrdersForSchedulingSelection.STARTING_DATE_FIELD, request).trim();
			sEndingDate = clsManageRequestParameters.get_Request_Parameter(
				SMListOrdersForSchedulingSelection.ENDING_DATE_FIELD, request).trim();
			//Check to make sure we're not asking for more than a week:
			long lDateDifference = 0;
			try {
				lDateDifference = clsDateAndTimeConversions.getFirstDateMinusSecondDateInDays(sEndingDate, sStartingDate);
			} catch (Exception e) {
				sWarning += "  " + e.getMessage();
			}
			if (lDateDifference < 0){
				sWarning += "  Your ending date is EARLIER than your starting date.";
			}
		}
		if (bDateRangeThisWeek){
			Calendar calStartingDate = Calendar.getInstance();
			Calendar calEndingDate = Calendar.getInstance();
			int day = calStartingDate.get(Calendar.DAY_OF_WEEK);
			calStartingDate.add(Calendar.DATE, 2 - day);
			calEndingDate.add(Calendar.DATE, 8 - day);
			sStartingDate = clsDateAndTimeConversions.CalendarToString(calStartingDate, "MM/dd/yyyy");
			sEndingDate = clsDateAndTimeConversions.CalendarToString(calEndingDate, "MM/dd/yyyy");
		}
		if (bDateRangeNextWeek){
			Calendar calStartingDate = Calendar.getInstance();
			Calendar calEndingDate = Calendar.getInstance();
			int day = calStartingDate.get(Calendar.DAY_OF_WEEK);
			calStartingDate.add(Calendar.DATE, 9 - day);
			calEndingDate.add(Calendar.DATE, 15 - day);
			sStartingDate = clsDateAndTimeConversions.CalendarToString(calStartingDate, "MM/dd/yyyy");
			sEndingDate = clsDateAndTimeConversions.CalendarToString(calEndingDate, "MM/dd/yyyy");
		}
    	sRedirectString +=
    		"?" + SMListOrdersForSchedulingSelection.STARTING_DATE_FIELD + "=" + sStartingDate
    		+ "&" + SMListOrdersForSchedulingSelection.ENDING_DATE_FIELD + "=" + sEndingDate
    		+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
    	;
    	if (bDateRangeChosen){
    		sRedirectString += "&" + SMListOrdersForSchedulingSelection.DATE_RANGE_CHOOSE + "=Y";
    	}
    	if (bDateRangeToday){
    		sRedirectString += "&" + SMListOrdersForSchedulingSelection.DATE_RANGE_TODAY + "=Y";
    	}
    	if (bDateRangeThisWeek){
    		sRedirectString += "&" + SMListOrdersForSchedulingSelection.DATE_RANGE_THISWEEK + "=Y";
    	}
    	if (bDateRangeNextWeek){
    		sRedirectString += "&" + SMListOrdersForSchedulingSelection.DATE_RANGE_NEXTWEEK + "=Y";
    	}
    	
    	//Get the list of selected locations:
    	ArrayList<String> sLocations = new ArrayList<String>(0);
	    Enumeration<String> paramLocationNames = request.getParameterNames();
	    String sParamLocationName = "";
	    String sLocationMarker = SMListOrdersForSchedulingSelection.LOCATION_PARAMETER;
	    while(paramLocationNames.hasMoreElements()) {
	    	sParamLocationName = paramLocationNames.nextElement();
		  if (sParamLocationName.contains(sLocationMarker)){
			  sLocations.add(sParamLocationName.substring(
					  sParamLocationName.indexOf(sLocationMarker) + sLocationMarker.length()));
			  sRedirectString += "&" + sParamLocationName + "=Y";
		  }
	    }
	    Collections.sort(sLocations);
		
	    if (sLocations.size() == 0){
    		sWarning += "  You must select at least one location.";
	    }
    	//Get the list of selected order types:
    	ArrayList<String> sServiceTypes = new ArrayList<String>(0);
		Enumeration<String> paramNames = request.getParameterNames();
	    String sParamName = "";
	    String sMarker = SMListOrdersForSchedulingSelection.SERVICETYPE_PARAMETER;
	    while(paramNames.hasMoreElements()) {
	      sParamName = paramNames.nextElement();
		  if (sParamName.contains(sMarker)){
			  sServiceTypes.add(sParamName.substring(sParamName.indexOf(sMarker) + sMarker.length()));
			  sRedirectString += "&" + sParamName + "=Y";
		  }
	    }
	    Collections.sort(sServiceTypes);
		
	    if (sServiceTypes.size() == 0){
    		sWarning += "  You must select at least one service type.";
	    }
    		
		try {
			sStartingDate = clsDateAndTimeConversions.utilDateToString(clsDateAndTimeConversions.StringTojavaSQLDate("M/d/yyyy", sStartingDate),"yyyy-MM-dd");
		} catch (ParseException e) {
			sWarning += "  Invalid starting date - Error:[1423581092] - '" + sStartingDate + "' - " + e.getMessage();
		}
		try {
			sEndingDate = clsDateAndTimeConversions.utilDateToString(clsDateAndTimeConversions.StringTojavaSQLDate("M/d/yyyy", sEndingDate),"yyyy-MM-dd");
		} catch (ParseException e) {
			sWarning += "  Invalid ending date - Error:[1423581093] - '" + sEndingDate + "' - " + e.getMessage();
		}
		
    	if(sWarning.compareToIgnoreCase("") != 0){
    		response.sendRedirect(sRedirectString + "&Warning=" + sWarning);
    		return;
    	}
	    
    	String sReportTitle = "List of orders for scheduling";   
    	String sCriteria = "Starting with expected ship date '<B>" + sStartingDate + "</B>'"
    		+ ", ending with expected ship date date '<B>" + sEndingDate + "</B>'"
    	;

		sCriteria += ", including ONLY orders with these default header locations: ";
    	for (int i = 0; i < sLocations.size(); i++){
    		if (i == 0){
    			sCriteria += "<B>" + sLocations.get(i) + "</B>";
    		}else{
    			sCriteria += ", <B>" + sLocations.get(i) + "</B>";
    		}
    	}
    	sCriteria += ", and including ONLY orders for these service types: ";
    	for (int i = 0; i < sServiceTypes.size(); i++){
    		if (i == 0){
    			sCriteria += "<B>" + SMTableservicetypes.getServiceTypeLabel(sServiceTypes.get(i)) + "</B>";
    		}else{
    			sCriteria += ", <B>" + SMTableservicetypes.getServiceTypeLabel(sServiceTypes.get(i)) + "</B>";
    		}
    	}
    	
    	sCriteria += ". Orders shown have at least ONE item remaining on the order."; //, and have NEVER been scheduled previously.";
    	sCriteria += "  <B>NOTE:</B>If an order is scheduled for the day of its 'Expected ship date', it will <B>NOT</B> show on the list.";
	    
    	out.println("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.0 "
		   + "Transitional//EN\">"
	       + "<HTML>"
	       + "<HEAD>"

	       + "<TITLE>" + sReportTitle + " - " + sCompanyName + "</TITLE></HEAD>\n<BR>" 
		   + "<BODY BGCOLOR=\"" 
		   + SMUtilities.getInitBackGroundColor(getServletContext(), sDBID) 
		   + "\""
		   + " style=\"font-family: " + SMUtilities.DEFAULT_FONT_FAMILY + "\";"
		   //Jump to the last edit:
		   + " onLoad=\"window.location='#LastEdit'\""
		   + ">"
		   + "<TABLE BORDER=0 WIDTH=100%>"
		   + "<TR><TD ALIGN=LEFT WIDTH=45%><FONT SIZE=2>" 
		   + clsDateAndTimeConversions.nowStdFormat() + " Printed by " + sUserFullName 
		   + "</FONT></TD><TD ALIGN=CENTER WIDTH=55%><FONT SIZE=2><B>" + sCompanyName + "</B></FONT></TD></TR>"
		   + "<TR><TD VALIGN=BOTTOM COLSPAN=2><FONT SIZE=2><B>" + sReportTitle + "</B></FONT></TD></TR>"
		   + "<TR><TD COLSPAN=2><FONT SIZE=2>" + sCriteria + "</FONT></TD></TR>");
				   
    	out.println("<TD><A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMUserLogin?" 
			+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
			+ "\">Return to user login</A><BR>");
	    out.println("<A HREF=\"" + WebContextParameters.getdocumentationpageURL(getServletContext()) + "#" + Long.toString(SMSystemFunctions.SMListOrdersForScheduling) 
	    		+ "\">Summary</A><BR>");

	    out.println("</TD></TR></TABLE>");
	    String sReturnWarning = clsManageRequestParameters.get_Request_Parameter("Warning", request);
		if (sReturnWarning.compareToIgnoreCase("") != 0){
			out.println("<B><FONT COLOR=\"RED\">WARNING: " + sReturnWarning + "</FONT></B><BR>");
		}

	    //log usage of this this report
	    SMClasses.SMLogEntry log = new SMClasses.SMLogEntry(sDBID, getServletContext());
	    log.writeEntry(sUserID, "SMLISTORDERSFORSCHEDULING", "REPORT", "SMListOrdersForScheduling", "[1376509328]");
	    
		SMListOrdersForSchedulingReport rpt = new SMListOrdersForSchedulingReport();
    	if (!rpt.processReport(
    			sDBID, 
    			sLocations, 
    			sServiceTypes, 
    			sStartingDate, 
    			sEndingDate,
    			bDateRangeChosen,
    			bDateRangeToday,
    			bDateRangeThisWeek,
    			bDateRangeNextWeek,
    			sUserID,
    			sUserFullName,
    			out, 
    			getServletContext(),
    			(String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_LICENSE_MODULE_LEVEL))){
    		sRedirectString += "&Warning=" + clsServletUtilities.URLEncode(rpt.getErrorMessage());
			response.sendRedirect(sRedirectString);
			return;
    	}
    	
	    out.println("</BODY></HTML>");
		
		return;
	}
	public void doGet(HttpServletRequest request,
			HttpServletResponse response)
	throws ServletException, IOException {

		doPost(request, response);
	}
}
