package smcontrolpanel;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import SMClasses.MySQLs;
import SMDataDefinition.*;
import ServletUtilities.clsCreateHTMLFormFields;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsManageRequestParameters;
import ServletUtilities.clsServletUtilities;

public class SMViewTruckScheduleSelection  extends HttpServlet {
	private static final long serialVersionUID = 1L;
	public static final String LOCATION_PARAMETER = "LOCATION";
	public static final String MECHANIC_PARAMETER = "MECHANIC";
	public static final String SERVICETYPE_PARAMETER = "SERVICETYPE";
	public static final String GENERATE_REPORT_PARAMETER = "GENERATE_REPORT";
	public static final String GENERATE_REPORT_LABEL = "----View----";
	public static final String PRINTINDIVIDUAL_VALUE_NO = "NO";
	public static final String INDIVIDUALSALESPERSON_PARAMETER = "INDIVIDUALSALESPERSON";
	public static final String STARTING_DATE_FIELD = "StartingDate";
	public static final String ENDING_DATE_FIELD = "EndingDate";
	public static final String DATE_RANGE_PARAM = "DateRange";
	public static final String DATE_RANGE_CHOOSE = "DateRangeChoose";
	public static final String DATE_RANGE_TODAY = "DateRangeToday";
	public static final String DATE_RANGE_THISWEEK = "DateRangeThisWeek";
	public static final String DATE_RANGE_NEXTWEEK = "DateRangeNextWeek";
	public static final String EDITSCHEDULE_PARAMETER = "AllowScheduleEditing";
	public static final String ONLYSHOWZEROHOURS_PARAMETER = "OnlyShowZeroHours";
	public static final String AUTOREFRESH_PARAMETER = "AutoRefresh";
	public static final String LOOKUPMECHANIC_PARAMETER = "LOOKUPMECHANIC";
	public static final String VIEWALLLOCATIONS_PARAMETER = "VIEWALLLOCATIONS";
	public static final String VIEWALLSERVICETYPES_PARAMETER = "VIEWALLSERVICETYPES";
	public static final String DISPLAYMOVEANDCOPYBUTTONS_PARAMETER = "DISPLAYMOVEANDCOPYBUTTONS";
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

		//Get the session info:
		HttpSession CurrentSession = request.getSession(true);
		String sDBID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_DATABASE_ID);
		String sCompanyName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_COMPANYNAME);
		String sUserID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERID);

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
		//Get the parameters:
		String sStartingDate = clsManageRequestParameters.get_Request_Parameter(STARTING_DATE_FIELD, request);
		String sEndingDate = clsManageRequestParameters.get_Request_Parameter(ENDING_DATE_FIELD, request);
		boolean bDateRangeChosen = request.getParameter(DATE_RANGE_CHOOSE) != null;
		boolean bDateRangeToday = request.getParameter(DATE_RANGE_TODAY) != null;
		boolean bDateRangeThisWeek = request.getParameter(DATE_RANGE_THISWEEK) != null;
		boolean bDateRangeNextWeek = request.getParameter(DATE_RANGE_NEXTWEEK) != null;
    	boolean bAllowWorkOrderConfiguring = SMSystemFunctions.isFunctionPermitted(
    			SMSystemFunctions.SMConfigureWorkOrders, 
    			sUserID, 
    			getServletContext(), 
    			sDBID,
    			(String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_LICENSE_MODULE_LEVEL));
    	boolean bOnlyShowZeroHours = request.getParameter(ONLYSHOWZEROHOURS_PARAMETER) != null;
		boolean bCheckAllLocationsAndTypes = request.getParameter(
			SMViewTruckScheduleSelection.DATE_RANGE_PARAM) == null; 
		
		String title = "";
		title = "View  truck schedules";
		String subtitle = "";
		out.println(SMUtilities.SMCPTitleSubBGColor(title, subtitle, SMUtilities.getInitBackGroundColor(getServletContext(), sDBID), sCompanyName));
		out.println(SMUtilities.getDatePickerIncludeString(getServletContext()));
	    String sWarning = clsManageRequestParameters.get_Request_Parameter("Warning", request);
		if (! sWarning.equalsIgnoreCase("")){
			out.println("<B><FONT COLOR=\"RED\">WARNING: " + sWarning + "</FONT></B><BR>");
		}
		//Print a link to the first page after login:
		out.println("<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMUserLogin?" 
				+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
				+ "\">Return to user login</A><BR>");

		out.println ("<FORM ACTION =\"" + SMUtilities.getURLLinkBase(getServletContext()) 
				+ "smcontrolpanel.SMViewTruckScheduleGenerate\">");
		out.println("<INPUT TYPE=HIDDEN NAME='" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "' VALUE='" + sDBID + "'>");
		out.println("<INPUT TYPE=HIDDEN NAME=CallingClass VALUE=\"" + this.getClass().getName() + "\">");
		out.println("<TABLE WIDTH=100% CELLPADDING=1 border=1>");

		try{
			//select location
			String sSQL = "SELECT"
				+ " " + SMTablelocations.sLocation
				+ ", " + SMTablelocations.sLocationDescription
				+ " FROM " + SMTablelocations.TableName
				+ " WHERE ("
					+ "(" + SMTablelocations.ishowintruckschedule + " = 1)"
				+ ")"
				+ " ORDER BY "  + SMTablelocations.sLocation
			;
			ResultSet rsLocations = clsDatabaseFunctions.openResultSet(
					sSQL, 
					getServletContext(), 
					sDBID, 
					"MySQL", 
					"smcontrolpanel.SMViewTruckScheduleSelection");
			out.println("<TR><TD ALIGN=RIGHT><H4>ONLY show technicians assigned to these locations:&nbsp;</H4></TD><TD>");
			String sChecked = "";
			while(rsLocations.next()){
				String sLocation = rsLocations.getString(SMTablelocations.TableName + "." 
					+ SMTablelocations.sLocation).trim();
				out.println("<LABEL><INPUT TYPE=CHECKBOX NAME=\"" + LOCATION_PARAMETER 
					+ sLocation + "\""
				);
				if (
					(request.getParameter(LOCATION_PARAMETER + sLocation) != null) || bCheckAllLocationsAndTypes
				){
					sChecked = clsServletUtilities.CHECKBOX_CHECKED_STRING;
				}else{
					sChecked = "";
				}
				out.println(" " + sChecked + " "
					+ " width=0.25>" 
					+ sLocation + " - "
					+ rsLocations.getString(SMTablelocations.TableName + "." + SMTablelocations.sLocationDescription) + "<BR></LABEL>");
			}
			rsLocations.close();
			out.println("</TR>");

			//select service type
			sSQL = MySQLs.Get_Distinct_Servicetypes_SQL();
			//System.out.println("Service Type SQL: " + sSQL);
			ResultSet rsServiceTypes = clsDatabaseFunctions.openResultSet(sSQL, getServletContext(), sDBID, "MySQL", "smcontrolpanel.SMUnbilledContractReportCriteriaSelection");
			out.println("<TR><TD ALIGN=RIGHT><H4>If the job OR technician is associated with these service types:&nbsp;</H4></TD><TD>");
			/*
	    	"<SELECT NAME=\"SelectedServiceType\">");
	    	out.println("<OPTION VALUE=ALLST SELECTED>All Service Types");
			 */
			while(rsServiceTypes.next()){
			 if(rsServiceTypes.getString(SMTableservicetypes.TableName + "." + SMTableservicetypes.id) != null) {
				String sServiceType = rsServiceTypes.getString(SMTableorderheaders.TableName + "." + SMTableorderheaders.sServiceTypeCode).trim();
				out.println("<LABEL><INPUT TYPE=CHECKBOX NAME=\"" + SERVICETYPE_PARAMETER 
					+ sServiceType + "\""); 
				if (
					(request.getParameter(SERVICETYPE_PARAMETER + sServiceType) != null) 
					|| bCheckAllLocationsAndTypes
				){
						sChecked = clsServletUtilities.CHECKBOX_CHECKED_STRING;
					}else{
						sChecked = "";
					}
				out.println(" " + sChecked + " "
					+ " width=0.25>" 
					+ rsServiceTypes.getString(SMTableservicetypes.TableName + "." + SMTableservicetypes.sName)
					+ "<BR></LABEL>");
			 }
			}
			rsServiceTypes.close();
			out.println("</TD>");
			out.println("</TR>");

			//select sort order
			//out.println("<TR><TD ALIGN=CENTER><H3>Sort By </H3></TD>");
			//out.println("<TD><SELECT NAME=\"SortOrder\">");
			//out.println("<OPTION VALUE=\"Salesperson First\">Salesperson First");
			//out.println("<OPTION VALUE=\"Service Type First\">Service Type First");
			//out.println("</SELECT><FONT COLOR=RED> *Not effective when generating emails.</FONT></TD></TR>");

			//show details or not
			//out.println ("<TR><TD ALIGN=CENTER><H3>Schedule format </H3></TD><TD>");
			//out.println ("<INPUT TYPE=\"RADIO\" NAME=\"ShowDetail\" VALUE=1 checked=\"yes\">Yes<BR>");
			//out.println ("<INPUT TYPE=\"RADIO\" NAME=\"ShowDetail\" VALUE=0>No<BR>");
			//out.println ("</TD></TR>");

		}catch(SQLException ex){
			//handle any errors
			out.println("Error reading locations and service types - " + ex.getMessage() + "<BR>");
		}
		
		//If none of the date choices are set, we'll decide the default by whether or not
		//this is a mobile session: if it is we'll default to show today only, otherwise,
		//we'll default to show the whole week:
		String sMobile = "N";
		if (!bDateRangeChosen && !bDateRangeToday && !bDateRangeThisWeek){
			sMobile = (String)CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_MOBILE);
			if (sMobile.compareToIgnoreCase("Y") == 0){
				//TJR - 10/3/2011 - changed this so that even on mobile devices, the default is 'by week':
				bDateRangeToday = false;
				bDateRangeThisWeek = true;
			}else{
				bDateRangeThisWeek = true;
			}
		}
		String sChecked = "";
		out.println("<TR><TD ALIGN=RIGHT><H4>And ONLY show entries within this date range:&nbsp;</H4></TD>");
		out.println("<TD>");
		if (bDateRangeThisWeek){
			sChecked = clsServletUtilities.CHECKBOX_CHECKED_STRING;
		}else{
			sChecked = "";
		}
		out.println ("<LABEL><INPUT TYPE=\"RADIO\" NAME=\"" + DATE_RANGE_PARAM 
				+ "\" VALUE=\"" + DATE_RANGE_THISWEEK + "\" " + sChecked + ">This week only (Mon-Sun)<BR></LABEL>");

		if (bDateRangeNextWeek){
			sChecked = clsServletUtilities.CHECKBOX_CHECKED_STRING;
		}else{
			sChecked = "";
		}
		out.println ("<LABEL><INPUT TYPE=\"RADIO\" NAME=\"" + DATE_RANGE_PARAM 
				+ "\" VALUE=\"" + DATE_RANGE_NEXTWEEK + "\" " + sChecked + ">Next week only (Mon-Sun)<BR></LABEL>");
		
		if (bDateRangeToday){
			sChecked = clsServletUtilities.CHECKBOX_CHECKED_STRING;
		}else{
			sChecked = "";
		}
				
		out.println ("<LABEL><INPUT TYPE=\"RADIO\" NAME=\"" + DATE_RANGE_PARAM 
				+ "\" VALUE=\"" + DATE_RANGE_TODAY + "\" " + sChecked + ">Today only<BR></LABEL>");

		if (bDateRangeChosen){
			sChecked = clsServletUtilities.CHECKBOX_CHECKED_STRING;
		}else{
			sChecked = "";
		}
		out.println ("<LABEL><INPUT TYPE=\"RADIO\" NAME=\"" + DATE_RANGE_PARAM 
				+ "\" VALUE=\"" + DATE_RANGE_CHOOSE + "\" " + sChecked + ">OR Choose dates:<BR></LABEL>");
		out.println ("&nbsp;&nbsp;&nbsp;&nbsp;Starting date:&nbsp;"
				+ clsCreateHTMLFormFields.TDTextBox(
						STARTING_DATE_FIELD, 
						sStartingDate, 
						10, 
						10, 
						""
				) 
				+ SMUtilities.getDatePickerString(STARTING_DATE_FIELD, getServletContext())
				+ "&nbsp;&nbsp;Ending date:&nbsp;"
				+ clsCreateHTMLFormFields.TDTextBox(
						ENDING_DATE_FIELD, 
						sEndingDate, 
						10, 
						10, 
						""
				) 
				+ SMUtilities.getDatePickerString(ENDING_DATE_FIELD, getServletContext())
		);

		out.println("</TD>");
		out.println("</TR>");
		
		//Choose ALL mechanics, or only one:
		
		try{
			//select location
			String sSQL = "SELECT"
				+ " " +SMTablemechanics.lid
				+ ", " + SMTablemechanics.sMechInitial
				+ ", " + SMTablemechanics.sMechFullName
				+ " FROM " + SMTablemechanics.TableName
				+ " ORDER BY "  + SMTablemechanics.sMechFullName
			;
			ResultSet rsMechanics = clsDatabaseFunctions.openResultSet(
					sSQL, 
					getServletContext(), 
					sDBID, 
					"MySQL", 
					"smcontrolpanel.SMViewTruckScheduleSelection");
			out.println("<TR><TD ALIGN=RIGHT><H4>Choose a single technician to show ALL entries for THAT technician:&nbsp;</H4></TD><TD>");
			out.println("<SELECT NAME = \"" + MECHANIC_PARAMETER + "\">");
			out.println("<OPTION VALUE = \"" + "" + "\">** ALL Technicians **</OPTION>");
			String s = "";
			while(rsMechanics.next()){
				String sMechInitial = rsMechanics.getString(SMTablemechanics.TableName + "." 
					+ SMTablemechanics.sMechInitial).trim();
				s = "<OPTION";
				if (clsManageRequestParameters.get_Request_Parameter(MECHANIC_PARAMETER, request).compareToIgnoreCase(sMechInitial) == 0){
					s += " selected=YES ";
				}
				
				//TODO - TJR - change this to use the mechanic's ID:
				//s += " VALUE=\"" + rsMechanics.getString(SMTablemechanics.lid) + "\">"
						
				s += " VALUE=\"" + rsMechanics.getString(SMTablemechanics.sMechInitial) + "\">"
				+ rsMechanics.getString(SMTablemechanics.sMechInitial) + " - " 
				+ rsMechanics.getString(SMTablemechanics.TableName + "." + SMTablemechanics.sMechFullName)
				+ "</OPTION>";
				out.println(s);
			}
			
			rsMechanics.close();
			out.println("</TR>");
		}catch(SQLException ex){
			//handle any errors
			out.println("Error reading technicians - " + ex.getMessage() + "<BR>");
		}
			
		//Add a checkbox for editing:
		if (bAllowWorkOrderConfiguring){
			out.println("<TR><TD ALIGN=RIGHT><H4>Edit schedule?&nbsp;</H4></TD><TD>");
			out.println("<LABEL><INPUT TYPE=CHECKBOX NAME=\"" + EDITSCHEDULE_PARAMETER  + "\"");
			if ((request.getParameter(SMViewTruckScheduleSelection.EDITSCHEDULE_PARAMETER) != null)){
				sChecked = clsServletUtilities.CHECKBOX_CHECKED_STRING;
			}else{
				sChecked = "";
			}
			out.println(" " + sChecked + " " + " width=0.25>" + "Allow schedule editing" + "<BR></LABEL>");
			out.println("</TD>");
			out.println("</TR>");
		}
		
		//Add a checkbox to display the move/copy buttons:
		if (bAllowWorkOrderConfiguring){
			out.println("<TR><TD ALIGN=RIGHT><H4>Display quick edit buttons?&nbsp;</H4></TD><TD>");
			out.println("<LABEL><INPUT TYPE=CHECKBOX NAME=\"" + DISPLAYMOVEANDCOPYBUTTONS_PARAMETER  + "\"");
			if ((request.getParameter(SMViewTruckScheduleSelection.DISPLAYMOVEANDCOPYBUTTONS_PARAMETER) != null)){
				sChecked = clsServletUtilities.CHECKBOX_CHECKED_STRING;
			}else{
				sChecked = "";
			}
			out.println(" " + sChecked + " " + " width=0.25>" + "Display buttons for moving/copying entries" + "<BR></LABEL>");
			out.println("</TD>");
			out.println("</TR>");
		}
		
		//Add a checkbox to only show zero hours:
		out.println("<TR><TD ALIGN=RIGHT><H4>Only show entries with NO hours&nbsp;</H4></TD><TD>");
		out.println("<LABEL><INPUT TYPE=CHECKBOX NAME=\"" + ONLYSHOWZEROHOURS_PARAMETER  + "\"");
		
		if (bOnlyShowZeroHours){
			sChecked = clsServletUtilities.CHECKBOX_CHECKED_STRING;
		}else{
			sChecked = "";
		}
		out.println(" " + sChecked + " " + " width=0.25>" + "Don't show entries with any job cost time entered." + "<BR></LABEL>");
		out.println("</TD>");
		out.println("</TR>");
		
		//Add a checkbox to turn on refresh: SCO if it works, TJR if it doesnt
		
		if(SMSystemFunctions.isFunctionPermitted(
				SMSystemFunctions.SMAutoRefreshSchedule, 
				sUserID, 
				getServletContext(), 
				sDBID,
				(String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_LICENSE_MODULE_LEVEL))){
			out.println("<TR><TD ALIGN=RIGHT><H4>Enable auto-refresh&nbsp;</H4></TD><TD>");
			out.println("<LABEL><INPUT TYPE=CHECKBOX NAME=\"" + AUTOREFRESH_PARAMETER  + "\"");
			out.println(" " + "" + " " + " width=0.25>" + "Check to make the schedule refresh every " 
				+ Integer.toString(SMViewTruckScheduleGenerate.REFRESH_TIME_SECONDS/60) + " min." + "<BR></LABEL>");
			out.println("</TD>");
			out.println("</TR>");
		}
		
		out.println("</TABLE>");
		out.println("<BR>");
	
		out.println("<INPUT TYPE=\"SUBMIT\" NAME=" 
				+ GENERATE_REPORT_PARAMETER 
				+ " VALUE=\"" + GENERATE_REPORT_LABEL + "\">&nbsp;&nbsp;");
		out.println("</FORM>");
		

	    
	    /*
	    
	    out.println("<TABLE style = \"vertical-align:center;"
				+ " height:20px;"
				+ " padding:0px;"
				+ " align-text:center;"
				+ " border-width: 1px;"
				+ " border-color: black;"
				+ " border-style: solid solid solid solid;"
				+ " border-collapse: collapse;"
				+ "\">");
		long lIterator = 0;
		out.println("<TR><TD></TD>");
		for (int i=0;i<32;i++){
			out.println("<TD style = \"vertical-align:center;"
									+ " height:20px;"
									+ " padding:1px;"
									+ " align-text:center;"
									+ " border-width: 1px;"
									+ " border-color: black;"
									+ " border-style: solid;"
									+ "\">" + i + "</TD>");
		}
		out.println("</TR>");
		for (int r=0;r<256;r=r+24){
			//out.println("<TR>");
			for(int g=0;g<256;g=g+26){
				//out.println("<TR>");
				for(int b=0;b<256;b=b+26){
					if (lIterator % 32 == 0){
						out.println("<TR><TD style = \"vertical-align:center;"
									+ " height:20px;"
									+ " padding:1px;"
									+ " align-text:center;"
									+ " border-width: 1px;"
									+ " border-color: black;"
									+ " border-style: solid;"
									+ "\">" 
									+ (lIterator - (lIterator % 32)) / 32 + "</TD>");
					}
					String sColor = SMUtilities.PadLeft(Integer.toHexString(r), "0", 2) + 
									SMUtilities.PadLeft(Integer.toHexString(g), "0", 2) + 
									SMUtilities.PadLeft(Integer.toHexString(b), "0", 2);
					out.println("<TD style = \"vertical-align:center;"
									+ " height:20px;"
									+ " padding:1px;"
									+ " align-text:center;"
									+ " border-width: 1px;"
									+ " border-color: black;"
									+ " border-style: solid;"
									+ "\">" 
									+ "<span style=\"background:#" + sColor + ";\">&nbsp;&nbsp;&nbsp;&nbsp;</span>"// + " " + sColor 
								+ "</TD>");
					
					lIterator++;
					
					if (lIterator % 32 == 0){
						out.println("</TR>");
					}
				}
				//out.println("</TR>");
			} 
			//out.println("</TR>");
		}
		
		out.println("</TABLE>");
	    */

		out.println("</BODY></HTML>");
	}
	public void doGet(HttpServletRequest request,
			HttpServletResponse response)
	throws ServletException, IOException {

		doPost(request, response);
	}

}