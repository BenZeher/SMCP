
package smcontrolpanel;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Enumeration;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import SMClasses.SMLogEntry;
import SMDataDefinition.SMTableusers;
import ServletUtilities.clsServletUtilities;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsDateAndTimeConversions;
import ServletUtilities.clsManageRequestParameters;
import ConnectionPool.WebContextParameters;

public class SMViewAppointmentCalendarGenerate extends HttpServlet {

	private static final long serialVersionUID = 1L;
	public static final int DEFAULT_NUMBER_OF_DAYS_TO_SHOW = 7;
	
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
		String sCompanyName = "";
		
		//Get the session info:
		HttpSession CurrentSession;
		try {
			CurrentSession = request.getSession(true);
			sDBID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_DATABASE_ID);
			//sUserName = (String) CurrentSession.getAttribute(SMUtilities.SESSION_PARAM_USERNAME);
			sUserID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERID);
			sCompanyName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_COMPANYNAME);
		} catch (Exception e3) {
			out.println("<HTML>WARNING: Error [1494599197] - " + e3.getMessage() + ".</BODY></HTML>");
			return;
		}
		  //log usage of this report
	    SMClasses.SMLogEntry log = new SMClasses.SMLogEntry(sDBID, getServletContext());
	    log.writeEntry(sUserID, SMLogEntry.LOG_OPERATION_VIEWAPPOINTMENTCALENDAR, "REPORT", "SMViewAppointmentCalendar", "[1528217212]");

		boolean bAllowedToViewCalendar = SMSystemFunctions.isFunctionPermitted(
			SMSystemFunctions.SMViewAppointmentCalendar,
			sUserID,
			getServletContext(),
			sDBID,
			(String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_LICENSE_MODULE_LEVEL));

		//If the user has NO rights to view calendar, bump them out here:
		if (!bAllowedToViewCalendar){
			out.println("<HTML>WARNING: You do not currently have access to view the appointment calendar.</BODY></HTML>");
			return;
		}

		String sWarning = "";
		String sCallingClass = clsManageRequestParameters.get_Request_Parameter("CallingClass", request);
		String sRedirectString = "";
		sRedirectString = SMUtilities.getURLLinkBase(getServletContext()) + sCallingClass;

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


		try {
			CurrentSession.removeAttribute(SMViewAppointmentCalendarReport.APPOINTMENTQUERYSTRING);
		} catch (Exception e2) {
			//Could not remove attribute - but this should not cause any problem.
		}
		try {
			CurrentSession.setAttribute(SMViewAppointmentCalendarReport.APPOINTMENTQUERYSTRING, sRequestQueryString);
		} catch (Exception e2) {
			out.println("<HTML>WARNING: Error setting session attribute - " + e2.getMessage() + ".</BODY></HTML>");
			return;	
		}
		
		boolean bDateRangeChosen = false;
		boolean bDateRangeToday = false;
		boolean bDateRangeThisWeek = false;
		boolean bDateRangeNextWeek = false;
		boolean bDisplayOneDay = false;
		if (clsManageRequestParameters.get_Request_Parameter(
			SMViewAppointmentCalendarSelection.DATE_RANGE_PARAM, request).compareToIgnoreCase(
			SMViewAppointmentCalendarSelection.DATE_RANGE_CHOOSE) == 0){
			bDateRangeChosen = true;
		}
		if (clsManageRequestParameters.get_Request_Parameter(
			SMViewAppointmentCalendarSelection.DATE_RANGE_PARAM, request).compareToIgnoreCase(
			SMViewAppointmentCalendarSelection.DATE_RANGE_TODAY) == 0){
			bDateRangeToday = true;
			bDisplayOneDay = true;
		}
		if (clsManageRequestParameters.get_Request_Parameter(
			SMViewAppointmentCalendarSelection.DATE_RANGE_PARAM, request).compareToIgnoreCase(
			SMViewAppointmentCalendarSelection.DATE_RANGE_THISWEEK) == 0){
			bDateRangeThisWeek = true;
		}
		if (clsManageRequestParameters.get_Request_Parameter(
			SMViewAppointmentCalendarSelection.DATE_RANGE_PARAM, request).compareToIgnoreCase(
			SMViewAppointmentCalendarSelection.DATE_RANGE_NEXTWEEK) == 0){
			bDateRangeNextWeek = true;
		}
		
		//Get the starting and ending dates:
		//Default both to today:
		String sStartingDate = clsDateAndTimeConversions.now("MM/dd/yyyy");
		String sEndingDate = clsDateAndTimeConversions.now("MM/dd/yyyy");
		
		try {
			if (bDateRangeChosen){
				sStartingDate = clsManageRequestParameters.get_Request_Parameter(
					SMViewAppointmentCalendarSelection.STARTING_DATE_FIELD, request).trim();
				sEndingDate = clsManageRequestParameters.get_Request_Parameter(
					SMViewAppointmentCalendarSelection.ENDING_DATE_FIELD, request).trim();
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
					sWarning += "  You can only print up to seven days on the calendar.";
				}
				if(lDateDifference == 0){
					bDisplayOneDay = true;
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
		ArrayList<String> aUserIDs = new ArrayList<String>();
		
		//Collect the user names
		Enumeration<String> parameterNames = request.getParameterNames();
		while (parameterNames.hasMoreElements()){
			String paramName = (String) parameterNames.nextElement();
			 //String sParamValue = SMUtilities.get_Request_Parameter(paramName, request);
			 if(paramName.contains(SMViewAppointmentCalendarSelection.USER_PREFIX)){
				 aUserIDs.add(paramName.substring(SMViewAppointmentCalendarSelection.USER_PREFIX.length()));
			 }
		}
		//Order them by first and last name 
		String SQL = "SELECT " + SMTableusers.sUserName 
				+ "," + SMTableusers.sUserFirstName 
				+ ", " + SMTableusers.sUserLastName
				+ ", " + SMTableusers.lid
				+ " FROM " + SMTableusers.TableName
				+ " WHERE (";
				for (int i = 0; i < aUserIDs.size(); i++){
					if(i == 0){
						SQL += "(" + SMTableusers.lid + " = " + aUserIDs.get(i) + ")";
					}else{
						SQL += " OR (" + SMTableusers.lid + " = " + aUserIDs.get(i) + ")";
					}
				}
				SQL += ")"
					+ " ORDER BY " + SMTableusers.sUserFirstName + ", " + SMTableusers.sUserLastName
				;
		ResultSet rs = null;
		aUserIDs.clear();
		try {
			rs = clsDatabaseFunctions.openResultSet(SQL, getServletContext(), sDBID);		
			while(rs.next()){
				aUserIDs.add(rs.getString(SMTableusers.lid));
			}
			rs.close();
		} catch (SQLException e) {
			out.println("<HTML>WARNING: Error processing users names - " + e.getMessage() + ".</BODY></HTML>");
			return;	
		}

		//TODO sort by fullname instead of username.
	/*	//Sort the user names (NOTE: this will display the user in order by username NOT full name)
		Collections.sort(aUserNames, new Comparator<String>() {
		    @Override
		    public int compare(String s1, String s2) {
		        return s1.compareToIgnoreCase(s2);
		    }
		});
	*/	
		//Load the redirect string to keep selection parameters.
    	sRedirectString +=
    		"?" + SMViewAppointmentCalendarSelection.STARTING_DATE_FIELD + "=" + sStartingDate
    		+ "&" + SMViewAppointmentCalendarSelection.ENDING_DATE_FIELD + "=" + sEndingDate
    		+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
    	;
    	if (bDateRangeChosen){
    		sRedirectString += "&" + SMViewAppointmentCalendarSelection.DATE_RANGE_CHOOSE + "=Y";
    	}
    	if (bDateRangeToday){
    		sRedirectString += "&" + SMViewAppointmentCalendarSelection.DATE_RANGE_TODAY + "=Y";
    	}
    	if (bDateRangeThisWeek){
    		sRedirectString += "&" + SMViewAppointmentCalendarSelection.DATE_RANGE_THISWEEK + "=Y";
    	}
    	if (bDateRangeNextWeek){
    		sRedirectString += "&" + SMViewAppointmentCalendarSelection.DATE_RANGE_NEXTWEEK + "=Y";
    	}
    	
    	//Get the 'Edit' flag:
    	boolean bAllowCalendarEditing = request.getParameter(
    		SMViewAppointmentCalendarSelection.EDITAPPOINTMENT_PARAMETER) != null;
    	
    	//Get the 'Display move/copy buttons' flag:
    	//boolean bAllowMoveOrCopyButtons = request.getParameter(
    	//	SMViewTruckScheduleSelection.DISPLAYMOVEANDCOPYBUTTONS_PARAMETER) != null;
    	
    	
    	if (!clsDateAndTimeConversions.IsValidDateString("M/d/yyyy", sStartingDate)){
    		sWarning += "  Invalid starting date - '" + sStartingDate + "'";
    	}
	    
    	if (!clsDateAndTimeConversions.IsValidDateString("M/d/yyyy", sEndingDate)){
    		sWarning += "  Invalid end date - '" + sEndingDate + "'";
    	}

    	if(sWarning.compareToIgnoreCase("") != 0){
    		response.sendRedirect(sRedirectString + "&Warning=" + clsServletUtilities.URLEncode(sWarning));
    		return;
    	}
	    
    	String sReportTitle = "Appointment Calendar";   
    	String sCriteria = "Starting with date '<B>" + sStartingDate + "</B>'"
    		+ ", ending with date '<B>" + sEndingDate + "</B>'"
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
		       + "<FONT COLOR=RED><B>" + clsDateAndTimeConversions.nowStdFormatWithSeconds() + "</B></FONT>" 
		       + " Printed by " + SMUtilities.getFullNamebyUserID(sUserID, getServletContext(), sDBID, "SMViewAppointmentCalendarGenerate") 
		       + " using DBID " + sDBID + " timestamp: " + sMSMarker
		       + "</FONT></TD><TD ALIGN=CENTER WIDTH=55%><FONT SIZE=2><B>" + sCompanyName + "</B></FONT></TD></TR>"
		       + "<TR><TD VALIGN=BOTTOM COLSPAN=2><FONT SIZE=2><B>" + sReportTitle + "</B></FONT></TD></TR>"
		       + "<TR><TD COLSPAN=2><FONT SIZE=2>" + sCriteria + "</FONT></TD></TR>")
		       ;

					   
	    	out.println("<TD><A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMUserLogin?" 
				+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
				+ "\">Return to user login</A><BR>");
		    out.println("<A HREF=\"" + WebContextParameters.getdocumentationpageURL(getServletContext()) + "#" + Long.toString(SMSystemFunctions.SMViewTruckSchedules) 
		    		+ "\">Summary</A><BR>");
	    	out.println("<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMViewAppointmentCalendarSelection?" 
				+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
				+ "\">Return to Appointment Selection</A><BR>");
		    out.println("<BR><A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMEditBidEntry"
					+ "?SubmitAdd=Y"
					+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
					+ "&CallingClass=smcontrolpanel.SMEditBidEntry"
					+ "&OriginalCallingClass=smcontrolpanel.SMEditBidSelect"
 
		    		+ "\">Create Sales Lead</A><BR>");
		    
		    out.println("</TD></TR></TABLE>");
		    		    
			if (sReturnWarning.compareToIgnoreCase("") != 0){
				out.println("<B><FONT COLOR=\"RED\">WARNING: " + sReturnWarning + "</FONT></B><BR>");
			}
			if (sReturnStatus.compareToIgnoreCase("") != 0){
				out.println("<B><FONT COLOR=\"RED\">STATUS: " + sReturnStatus + "</FONT></B><BR>");
			}
	    }
	    
		SMViewAppointmentCalendarReport rpt = new SMViewAppointmentCalendarReport();
		
    	try {
			rpt.initializeReport(
					sStartingDate, 
					sEndingDate,
					aUserIDs,
					bDisplayOneDay,
					bDateRangeChosen,
					bDateRangeToday,
					bDateRangeThisWeek,
					bDateRangeNextWeek,
					bAllowCalendarEditing, 
					sUserID, 
					sDBID,
					sMSMarker,
					out, 
					bOutputToCSV,
					getServletContext(),
					(String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_LICENSE_MODULE_LEVEL))
			;
		} catch (Exception e) {
			System.out.println("FAIL HERE: " + e.getMessage());
			sRedirectString += "&Warning=" + clsServletUtilities.URLEncode(e.getMessage());
			try {
				response.sendRedirect(sRedirectString);
			} catch (Exception e1) {
				out.println("Error [1380728145] redirecting: " + e1.getMessage());
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
	
	public void doGet(HttpServletRequest request,
			HttpServletResponse response)
	throws ServletException, IOException {

		doPost(request, response);
	}
}



