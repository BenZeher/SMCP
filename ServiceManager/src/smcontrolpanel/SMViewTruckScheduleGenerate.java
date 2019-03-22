package smcontrolpanel;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.ResultSet;
import java.sql.SQLException;
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
import SMClasses.SMLogEntry;
import SMClasses.SMWorkOrderHeader;
import SMDataDefinition.SMTablelocations;
import SMDataDefinition.SMTableservicetypes;
import SMDataDefinition.SMTableusers;
import ServletUtilities.clsServletUtilities;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsDateAndTimeConversions;
import ServletUtilities.clsManageRequestParameters;

public class SMViewTruckScheduleGenerate extends HttpServlet {

	private static final long serialVersionUID = 1L;
	public static int REFRESH_TIME_SECONDS = 600;
	public static int DEFAULT_NUMBER_OF_DAYS_TO_SHOW = 7;
	
	public void doPost(HttpServletRequest request,
			HttpServletResponse response)
	throws ServletException, IOException {

		if (!SMAuthenticate.authenticateSMCPCredentials(
				request, 
				response, 
				getServletContext(), 
				-1L)
		){
			return;
		}

		PrintWriter out = response.getWriter();
		
		//Make sure these are initialized:
		String sDBID = "";
		//String sUserName = "";
		String sUserID = "";
		String sUserFullName = "";
		String sCompanyName = "";
		
		//Get the session info:
		HttpSession CurrentSession;
		try {
			CurrentSession = request.getSession(true);
			sDBID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_DATABASE_ID);
			//sUserName = (String) CurrentSession.getAttribute(SMUtilities.SESSION_PARAM_USERNAME);
			sUserID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERID);
			sUserFullName = (String)CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERFIRSTNAME) + " "
							+ (String)CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERLASTNAME);
			sCompanyName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_COMPANYNAME);
		} catch (Exception e3) {
			out.println("<HTML>WARNING: Error [1416603288] - " + e3.getMessage() + ".</BODY></HTML>");
			return;
		}

		boolean bAllowedToViewAllSchedules = SMSystemFunctions.isFunctionPermitted(
			SMSystemFunctions.SMViewTruckSchedules,
			sUserID,
			getServletContext(),
			sDBID,
			(String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_LICENSE_MODULE_LEVEL));

		boolean bAllowedToViewMechanicsOwnSchedule = SMSystemFunctions.isFunctionPermitted(
				SMSystemFunctions.SMViewMechanicsOwnTruckSchedule,
				sUserID,
				getServletContext(),
				sDBID,
				(String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_LICENSE_MODULE_LEVEL));

		//If the user has NO rights to view schedules, bump them out here:
		if (!bAllowedToViewAllSchedules && !bAllowedToViewMechanicsOwnSchedule){
			out.println("<HTML>WARNING: You do not currently have access to view any schedules.</BODY></HTML>");
			return;
		}
		
		//If this is a request to look up the mechanic to be viewed by reading the Users table, 
		//do that now:
		String sMechanicInitials = "";
		boolean bLookupMechanic = false;
		if (clsManageRequestParameters.get_Request_Parameter(
			SMViewTruckScheduleSelection.LOOKUPMECHANIC_PARAMETER, request).compareToIgnoreCase("") != 0){
			bLookupMechanic = true;
			//Lookup this mechanic:
			String SQL = "SELECT"
				+ " " + SMTableusers.smechanicinitials
				+ " FROM " + SMTableusers.TableName
				+ " WHERE ("
					+ "(" + SMTableusers.lid + " = " + sUserID + ")"
				+ ")"
			;
			//System.out.println("In " + this.toString() + " SQL = " + SQL);
			try {
				ResultSet rs = clsDatabaseFunctions.openResultSet(
						SQL, 
						getServletContext(), 
						sDBID, 
						"MySQL", 
						this.toString() + ".getting mechanic initials - user: " 
						+ sUserID
						+ " - "
						+ sUserFullName
						);
				if (rs.next()){
					sMechanicInitials = rs.getString(SMTableusers.smechanicinitials);
					if (sMechanicInitials.trim() == ""){
						//System.out.println("[1385406351] There is no mechanic initials setup for this user.");
						sMechanicInitials = "InitNotSetup";
					}
				}
				rs.close();
			} catch (SQLException e) {
				out.println("<HTML>WARNING: Error reading user to determine mechanic ID - " + e.getMessage()
					+ ".</BODY></HTML>");
				return;	
			}
		}else{
			sMechanicInitials = clsManageRequestParameters.get_Request_Parameter(
				SMViewTruckScheduleSelection.MECHANIC_PARAMETER, request).trim();
		}
		
		//At this point the user has either the right to view ALL schedules, or his own:
		
		//If he DOESN'T have the right to view ALL schedules, he must only have the right to view his own:
		if (!bAllowedToViewAllSchedules){
			//If he didn't CHOOSE to view only one mechanic's schedules, bump him out:
			if (sMechanicInitials.compareToIgnoreCase("") == 0){
				out.println("<HTML>WARNING: You do not currently have access to view ALL the schedules.</BODY></HTML>");
				return;	
			}
			
			//Next see if the mechanic he chose is the one associated with him:
			String SQL = "SELECT"
				+ " " + SMTableusers.smechanicinitials
				+ " FROM " + SMTableusers.TableName
				+ " WHERE ("
					+ "(" + SMTableusers.lid + " = " + sUserID + ")"
					+ " AND (" + SMTableusers.smechanicinitials + " = '" + sMechanicInitials + "')"
				+ ")"
				;
			boolean bMechanicMatchesUser = false;
			try {
				ResultSet rs = clsDatabaseFunctions.openResultSet(
						SQL, 
						getServletContext(), 
						sDBID, 
						"MySQL", 
						this.toString() + ".checking mechanic initials match - user: " 
						+ sUserID
						+ " - "
						+ sUserFullName
						);
				if (rs.next()){
					bMechanicMatchesUser = true;
				}
				rs.close();
			} catch (SQLException e) {
				out.println("<HTML>WARNING: Error reading user information with SQL: " 
					+ SQL + " - " + e.getMessage() + " </BODY></HTML>");
				return;	
			}
			
			//If this user is NOT associated with the selected mechanic, bump him out:
			if (!bMechanicMatchesUser){
				out.println("<HTML>WARNING: You do not currently have access to view schedules for mechanics other than yourself.</BODY></HTML>");
				return;	
			}
		}
		
		//At this point the user EITHER has rights to view ALL schedules, or the right to view the
		//mechanic's schedule he's chosen because he IS that mechanic:
		String sWarning = "";
		String sCallingClass = clsManageRequestParameters.get_Request_Parameter("CallingClass", request);
		String sRedirectString = "";
		sRedirectString = SMUtilities.getURLLinkBase(getServletContext()) + sCallingClass;

		//We have to get the 'LastEntryEdited' parameter and set it to the current job cost ID
		//so we don't keep carrying around an old one here:

    	//Get the 'last entry edited' - this is the job cost ID of the last entry edited, in case
    	//the user updated an entry and the schedule is being re-displayed after that edit:
		String sLastEntryEdited = clsManageRequestParameters.get_Request_Parameter(
			SMWorkOrderHeader.LASTENTRYEDITED_PARAM, request);

		//This gets the query string that was used to call this class.  We need to remember this (we'll store
		//it in the session below) but we first want to remove any warning that was passed in with it
		//and we also want to remove the 'LastEntryEdited' parameter so we don't get it passed
		//back to this class next time:
		String sRequestQueryString = request.getQueryString();
		
		//Get any warnings here, so we can clear the warning from the query string that gets saved in the 
		//session:
		
		String sReturnWarning = clsManageRequestParameters.get_Request_Parameter("Warning", request);
		//We have to use URLEncode in this replacement because that's the actual string we are trying to replace:
		if (sReturnWarning.compareToIgnoreCase("") != 0){
			sRequestQueryString = sRequestQueryString.replace("&Warning=" + clsServletUtilities.URLEncode(sReturnWarning), "");
		}
		//Get any status messages here:
		String sReturnStatus = clsManageRequestParameters.get_Request_Parameter("STATUS", request);
		//We have to use URLEncode in this replacement because that's the actual string we are trying to replace:
		if (sReturnStatus.compareToIgnoreCase("") != 0){
			sRequestQueryString = sRequestQueryString.replace("&STATUS=" + clsServletUtilities.URLEncode(sReturnStatus).replace("+", "%20"), "");
		}
		//System.out.println("[1428422427] - sRequestQueryString AFTER: '" + sRequestQueryString + "'");
		if (sLastEntryEdited.compareToIgnoreCase("") != 0){
			sRequestQueryString = sRequestQueryString.replace(
				SMWorkOrderHeader.LASTENTRYEDITED_PARAM + "=" + sLastEntryEdited, 
				"");
		}

		try {
			CurrentSession.removeAttribute(SMViewTruckScheduleReport.TRUCKSCHEDULEQUERYSTRING);
		} catch (Exception e2) {
			//Could not remove attribute - but this should not cause any problem.
		}
		try {
			CurrentSession.setAttribute(SMViewTruckScheduleReport.TRUCKSCHEDULEQUERYSTRING, sRequestQueryString);
		} catch (Exception e2) {
			out.println("<HTML>WARNING: Error setting session attribute - " + e2.getMessage() + ".</BODY></HTML>");
			return;	
		}
		
		boolean bDateRangeChosen = false;
		boolean bDateRangeToday = false;
		boolean bDateRangeThisWeek = false;
		boolean bDateRangeNextWeek = false;
		if (clsManageRequestParameters.get_Request_Parameter(
			SMViewTruckScheduleSelection.DATE_RANGE_PARAM, request).compareToIgnoreCase(
			SMViewTruckScheduleSelection.DATE_RANGE_CHOOSE) == 0){
			bDateRangeChosen = true;
		}
		if (clsManageRequestParameters.get_Request_Parameter(
			SMViewTruckScheduleSelection.DATE_RANGE_PARAM, request).compareToIgnoreCase(
			SMViewTruckScheduleSelection.DATE_RANGE_TODAY) == 0){
			bDateRangeToday = true;
		}
		if (clsManageRequestParameters.get_Request_Parameter(
			SMViewTruckScheduleSelection.DATE_RANGE_PARAM, request).compareToIgnoreCase(
			SMViewTruckScheduleSelection.DATE_RANGE_THISWEEK) == 0){
			bDateRangeThisWeek = true;
		}
		if (clsManageRequestParameters.get_Request_Parameter(
			SMViewTruckScheduleSelection.DATE_RANGE_PARAM, request).compareToIgnoreCase(
			SMViewTruckScheduleSelection.DATE_RANGE_NEXTWEEK) == 0){
			bDateRangeNextWeek = true;
		}
		
		//Get the starting and ending dates:
		//Default both to today:
		String sStartingDate = clsDateAndTimeConversions.now("MM/dd/yyyy");
		String sEndingDate = clsDateAndTimeConversions.now("MM/dd/yyyy");
		
		try {
			if (bDateRangeChosen){
				sStartingDate = clsManageRequestParameters.get_Request_Parameter(
					SMViewTruckScheduleSelection.STARTING_DATE_FIELD, request).trim();
				sEndingDate = clsManageRequestParameters.get_Request_Parameter(
					SMViewTruckScheduleSelection.ENDING_DATE_FIELD, request).trim();
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
				if (lDateDifference > 7){
					sWarning += "  You can only print up to seven days on the schedule.";
				}
			}
			if (bDateRangeThisWeek){
				Calendar calStartingDate = Calendar.getInstance();
				//This formula gives us the starting day of the calendar, based on the current day of the week:
				calStartingDate.add(Calendar.DATE, ((8 - calStartingDate.get(Calendar.DAY_OF_WEEK)) % 7) - 6);
				//Convert to strings:
				sStartingDate = clsDateAndTimeConversions.CalendarToString(calStartingDate, "MM/dd/yyyy");
				calStartingDate.add(Calendar.DATE, DEFAULT_NUMBER_OF_DAYS_TO_SHOW - 1);
				sEndingDate = clsDateAndTimeConversions.CalendarToString(calStartingDate, "MM/dd/yyyy");
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
		} catch (Exception e1) {
			out.println("<HTML>WARNING: Error processing dates - " + e1.getMessage() + ".</BODY></HTML>");
			return;	
		}
    	sRedirectString +=
    		"?" + SMViewTruckScheduleSelection.STARTING_DATE_FIELD + "=" + sStartingDate
    		+ "&" + SMViewTruckScheduleSelection.ENDING_DATE_FIELD + "=" + sEndingDate
    		+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
    	;
    	if (bDateRangeChosen){
    		sRedirectString += "&" + SMViewTruckScheduleSelection.DATE_RANGE_CHOOSE + "=Y";
    	}
    	if (bDateRangeToday){
    		sRedirectString += "&" + SMViewTruckScheduleSelection.DATE_RANGE_TODAY + "=Y";
    	}
    	if (bDateRangeThisWeek){
    		sRedirectString += "&" + SMViewTruckScheduleSelection.DATE_RANGE_THISWEEK + "=Y";
    	}
    	if (bDateRangeNextWeek){
    		sRedirectString += "&" + SMViewTruckScheduleSelection.DATE_RANGE_NEXTWEEK + "=Y";
    	}
    	
    	//Get the 'Edit' flag:
    	boolean bAllowScheduleEditing = request.getParameter(
    		SMViewTruckScheduleSelection.EDITSCHEDULE_PARAMETER) != null;
    	
    	//Get the 'Display move/copy buttons' flag:
    	boolean bAllowMoveOrCopyButtons = request.getParameter(
    		SMViewTruckScheduleSelection.DISPLAYMOVEANDCOPYBUTTONS_PARAMETER) != null;
    	
    	//Get the 'Only show zero hours' flag:
    	boolean bOnlyShowZeroHours = request.getParameter(
        		SMViewTruckScheduleSelection.ONLYSHOWZEROHOURS_PARAMETER) != null;
    	
    	boolean bShowAllLocations = request.getParameter(
        		SMViewTruckScheduleSelection.VIEWALLLOCATIONS_PARAMETER) != null;
    	
    	boolean bShowAllServiceTypes = request.getParameter(
        		SMViewTruckScheduleSelection.VIEWALLSERVICETYPES_PARAMETER) != null;
    	
    	//Get the list of selected locations:
    	ArrayList<String> sLocations = new ArrayList<String>(0);
    	//If we were requested to show ALL locations, get them from the database:
    	if (bShowAllLocations){
    		String SQL = "SELECT"
    			+ " " + SMTablelocations.sLocation
    			+ " FROM " + SMTablelocations.TableName
    			+ " ORDER BY " + SMTablelocations.sLocation
    		;
    		try {
				ResultSet rs = clsDatabaseFunctions.openResultSet(
						SQL, 
						getServletContext(), 
						sDBID, 
						"MySQL", 
						this.toString() + ".getting locations - user: " 
						+ sUserID
						+ " - "
						+ sUserFullName);
				while (rs.next()){
					sLocations.add(rs.getString(SMTablelocations.sLocation));
				}
				rs.close();
			} catch (SQLException e) {
				out.println("<HTML>WARNING: Error reading locations table - " + e.getMessage() + ".</BODY></HTML>");
				return;				
			}
    	}else{
		    Enumeration<String> paramLocationNames = request.getParameterNames();
		    String sParamLocationName = "";
		    String sLocationMarker = SMViewTruckScheduleSelection.LOCATION_PARAMETER;
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
    	}
    	//Get the list of selected order types:
    	ArrayList<String> sServiceTypes = new ArrayList<String>(0);
    	//If we were requested to show ALL service types, get them from the database:
    	if (bShowAllServiceTypes){
    		String SQL = "SELECT"
    			+ " " + SMTableservicetypes.sCode
    			+ " " + SMTableservicetypes.sName
    			+ " FROM " + SMTableservicetypes.TableName
    			+ " ORDER BY " + SMTableservicetypes.sCode
    		;
    		try {
				ResultSet rs = clsDatabaseFunctions.openResultSet(
						SQL, 
						getServletContext(), 
						sDBID, 
						"MySQL", 
						this.toString() + ".getting service types - user: " 
						+ sUserID
						+ " - "
						+ sUserFullName
						);
				while (rs.next()){
					sServiceTypes.add(rs.getString(SMTableservicetypes.sCode));
				}
				rs.close();
			} catch (SQLException e) {
				out.println("<HTML>WARNING: Error reading service types - " + e.getMessage() + ".</BODY></HTML>");
				return;				
			}
    	}else{
			Enumeration<String> paramNames = request.getParameterNames();
		    String sParamName = "";
		    String sMarker = SMViewTruckScheduleSelection.SERVICETYPE_PARAMETER;
		    while(paramNames.hasMoreElements()) {
		      sParamName = paramNames.nextElement();
			  if (sParamName.contains(sMarker)){
				  sServiceTypes.add(sParamName.substring(sParamName.indexOf(sMarker) + sMarker.length()));
				  sRedirectString += "&" + sParamName + "=Y";
			  }
		    }
		    Collections.sort(sServiceTypes);
			
		    if ((sServiceTypes.size() == 0) && !bShowAllServiceTypes){
	    		sWarning += "  You must select at least one service type.";
		    }
    	}
    	if (!clsDateAndTimeConversions.IsValidDateString("M/d/yyyy", sStartingDate)){
    		sWarning += "  Invalid starting date - '" + sStartingDate + "'";
    	}
	    
    	if (!clsDateAndTimeConversions.IsValidDateString("M/d/yyyy", sEndingDate)){
    		sWarning += "  Invalid end date - '" + sEndingDate + "'";
    	}

    	if(sWarning.compareToIgnoreCase("") != 0){
    		response.sendRedirect(sRedirectString + "&Warning=" + sWarning);
    		return;
    	}
	    
    	String sReportTitle = "Truck schedule";   
    	String sCriteria = "Starting with date '<B>" + sStartingDate + "</B>'"
    		+ ", ending with date '<B>" + sEndingDate + "</B>'"
    	;
    	if (sMechanicInitials.compareToIgnoreCase("") != 0){
    		sCriteria += " ONLY for mechanic: '<B>" + sMechanicInitials + "</B>'.";
    	}else{
    		sCriteria += ", including ONLY mechanics for these locations: ";
	    	for (int i = 0; i < sLocations.size(); i++){
	    		if (i == 0){
	    			sCriteria += "<B>" + sLocations.get(i) + "</B>";
	    		}else{
	    			sCriteria += ", <B>" + sLocations.get(i) + "</B>";
	    		}
	    	}
	    	sCriteria += ", including mechanics AND orders associated with these service types: ";
	    	for (int i = 0; i < sServiceTypes.size(); i++){
	    		if (i == 0){
	    			sCriteria += "<B>" + sServiceTypes.get(i) + "</B>";
	    		}else{
	    			sCriteria += ", <B>" + sServiceTypes.get(i) + "</B>";
	    		}
	    	}
    	}
    	
    	//Print the legend explaining the work order status:
    	String sLegend = "Work order statuses: "
			+ "<B><FONT COLOR = " + SMViewTruckScheduleReport.WORK_ORDER_STATUS_COLOR + ">WO-E</FONT></B>"
				+ " - work order exists or items have been added; "
			+ "<B><FONT COLOR = " + SMViewTruckScheduleReport.WORK_ORDER_STATUS_COLOR + ">WO-I</FONT></B>"
				+ " - work order exists and was imported; "
			+ "<B><FONT COLOR = " + SMViewTruckScheduleReport.WORK_ORDER_STATUS_COLOR + ">WO-P</FONT></B>"
				+ " - work order exists and was posted; "
			+ "<B><FONT COLOR = " + SMViewTruckScheduleReport.WORK_ORDER_STATUS_COLOR + ">WO-IP</FONT></B>"
				+ " - work order exists and was imported AND posted."
    	;
    	
	    boolean bOutputToCSV = (request.getParameter("OutputToCSV") != null);
	    
	    //Get a temporary marker to identify this run:
	    String sMSMarker = Long.toString(System.currentTimeMillis());
	    
	    //Retrieve information
	    if (bOutputToCSV){
	    	response.setContentType("text/csv");
	    	String disposition = "attachment; fileName=poreceiving.csv";
	    	response.setHeader("Content-Disposition", disposition);
	    }else{
	    	String sRefreshString = "";
	    	if (request.getParameter(SMViewTruckScheduleSelection.AUTOREFRESH_PARAMETER) != null){
	    		sRefreshString = "<META HTTP-EQUIV=\"REFRESH\" CONTENT=\"" + Integer.toString(REFRESH_TIME_SECONDS) + "\">";
	    	}
	    	out.println("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.0 "
	    			+ "Transitional//EN\">"
	    			+ "<HTML>"
	    			+ "<HEAD>"

		       //TODO - possible incorporate this tag to do an auto refresh?
		       + sRefreshString

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
		       + "<FONT COLOR=RED><B>" + clsDateAndTimeConversions.nowStdFormatWithSeconds() + "</B></FONT>" 
		       + " Printed by " + SMUtilities.getFullNamebyUserID(sUserID, getServletContext(), sDBID, "SMViewTruckScheduleGenerate") 
		       + "</FONT></TD><TD ALIGN=CENTER WIDTH=55%><FONT SIZE=2><B>" + sCompanyName + "</B></FONT></TD></TR>"
		       + "<TR><TD VALIGN=BOTTOM COLSPAN=2><FONT SIZE=2><B>" + sReportTitle + "</B></FONT></TD></TR>"
		       + "<TR><TD COLSPAN=2><FONT SIZE=2>" + sCriteria + "</FONT></TD></TR>"
		       + "<TR><TD COLSPAN=2><FONT SIZE=2>" + sLegend + "</FONT></TD></TR>");
					   
	    	out.println("<TD><A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMUserLogin?" 
				+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
				+ "\">Return to user login</A><BR>");
		    out.println("<A HREF=\"" + WebContextParameters.getdocumentationpageURL(getServletContext()) + "#" + Long.toString(SMSystemFunctions.SMViewTruckSchedules) 
		    		+ "\">Summary</A><BR>");

		    out.println("</TD></TR></TABLE>");
		    		    
			if (sReturnWarning.compareToIgnoreCase("") != 0){
				out.println("<B><FONT COLOR=\"RED\">WARNING: " + sReturnWarning + "</FONT></B><BR>");
			}
			if (sReturnStatus.compareToIgnoreCase("") != 0){
				out.println("<B><FONT COLOR=\"RED\">STATUS: " + sReturnStatus + "</FONT></B><BR>");
			}
	    }

	    //log usage of this report
	    SMClasses.SMLogEntry log = new SMClasses.SMLogEntry(sDBID, getServletContext());
	    log.writeEntry(
	    	sUserID, 
	    	SMLogEntry.LOG_OPERATION_SMVIEWTRUCKSCHEDULE, 
	    	"REPORT", 
	    	"SMViewTruckSchedule - marker = " + sMSMarker 
	    		+ ", current time: " + System.currentTimeMillis()
	    		+ ", sDBID = '" + sDBID + "'",
	    	"[1376509366]"
	    );
	    
		SMViewTruckScheduleReport rpt = new SMViewTruckScheduleReport();
		
    	try {
			rpt.initializeReport(
					sLocations, 
					sServiceTypes, 
					sStartingDate, 
					sEndingDate,
					bDateRangeChosen,
					bDateRangeToday,
					bDateRangeThisWeek,
					bDateRangeNextWeek,
					bAllowScheduleEditing,
					bAllowMoveOrCopyButtons,
					bOnlyShowZeroHours,
					bLookupMechanic,
					sLastEntryEdited,
					sMechanicInitials,
					sUserID, 
					sDBID,
					sMSMarker,
					out, 
					bOutputToCSV,
					getServletContext(),
					(String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_LICENSE_MODULE_LEVEL))
			;
		} catch (Exception e) {
			sRedirectString += "&Warning=" + clsServletUtilities.URLEncode(e.getMessage());
			try {
				response.sendRedirect(sRedirectString);
			} catch (Exception e1) {
				out.println("Error [1389727145] redirecting: " + e1.getMessage());
			}
			return;
		}
    	out.println("<BR><A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMUserLogin?" 
				+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
				+ "\">Return to user login</A><BR>");
    	out.println("<A HREF = #>Back to Top </A>");
	    out.println("</BODY></HTML>");
		return;
	}
	/*
	private String sCommandScripts(SMMasterEditEntry smmaster) throws SQLException{
		String s = "";
		
		s += "<NOSCRIPT>\n"
			+ "    <font color=red>\n"
			+ "    <H3>This page requires that JavaScript be enabled to function properly</H3>\n"
			+ "    </font>\n"
			+ "</NOSCRIPT>\n"
		;

		s += "<script type='text/javascript'>\n";

		s += "window.onload = function() {\n"
			+ "    displaytime();\n"
			+ "}\n"
		;
		
		//Display the current time:
		s += "function displaytime(){\n"
			+ "    var d = new Date();\n"
			+ "    document.getElementById(\"" + TIME_DISPLAY_FIELD + "\").innerHTML = d;\n"
			+ "}\n"
		;
		s += "</script>\n";
		return s;
	}
	*/
	public void doGet(HttpServletRequest request,
			HttpServletResponse response)
	throws ServletException, IOException {

		doPost(request, response);
	}
}
