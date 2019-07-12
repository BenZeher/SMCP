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

public class SMListOrdersForSchedulingSelection  extends HttpServlet {
	private static final long serialVersionUID = 1L;
	public static String LOCATION_PARAMETER = "LOCATION";
	public static String SERVICETYPE_PARAMETER = "SERVICETYPE";
	public static String GENERATE_REPORT_PARAMETER = "GENERATE_REPORT";
	public static String GENERATE_REPORT_LABEL = "----View----";
	public static String STARTING_DATE_FIELD = "StartingDate";
	public static String ENDING_DATE_FIELD = "EndingDate";
	public static String DATE_RANGE_PARAM = "DateRange";
	public static String DATE_RANGE_CHOOSE = "DateRangeChoose";
	public static String DATE_RANGE_TODAY = "DateRangeToday";
	public static String DATE_RANGE_THISWEEK = "DateRangeThisWeek";
	public static String DATE_RANGE_NEXTWEEK = "DateRangeNextWeek";
	public static String VIEWALLLOCATIONS_PARAMETER = "VIEWALLLOCATIONS";
	public static String VIEWALLSERVICETYPES_PARAMETER = "VIEWALLSERVICETYPES";
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
		String sCompanyName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_COMPANYNAME);

		//Get the parameters:
		String sStartingDate = clsManageRequestParameters.get_Request_Parameter(STARTING_DATE_FIELD, request);
		String sEndingDate = clsManageRequestParameters.get_Request_Parameter(ENDING_DATE_FIELD, request);
		boolean bDateRangeChosen = request.getParameter(DATE_RANGE_CHOOSE) != null;
		boolean bDateRangeToday = request.getParameter(DATE_RANGE_TODAY) != null;
		boolean bDateRangeThisWeek = request.getParameter(DATE_RANGE_THISWEEK) != null;
		boolean bDateRangeNextWeek = request.getParameter(DATE_RANGE_NEXTWEEK) != null;
		boolean bCheckAllLocationsAndTypes = request.getParameter(
			SMViewTruckScheduleSelection.DATE_RANGE_PARAM) == null; 
		
		String title = "";
		title = "List orders for scheduling";
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
				+ "smcontrolpanel.SMListOrdersForSchedulingGenerate\">");
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
					"smcontrolpanel.SMListOrdersForSchedulingSelection [1332275466]");
			out.println("<TR><TD ALIGN=RIGHT><H4>Include orders with these default header locations:&nbsp;</H4></TD><TD>");
			String sChecked = "";
			while(rsLocations.next()){
				String sLocation = rsLocations.getString(SMTablelocations.TableName + "." 
					+ SMTablelocations.sLocation).trim();
				out.println("<INPUT TYPE=CHECKBOX NAME=\"" + LOCATION_PARAMETER 
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
					+ rsLocations.getString(SMTablelocations.TableName + "." + SMTablelocations.sLocationDescription) + "<BR>");
			}
			rsLocations.close();
			out.println("</TR>");

			//select service type
			sSQL = MySQLs.Get_Distinct_Servicetypes_SQL();
			//System.out.println("Service Type SQL: " + sSQL);
			ResultSet rsServiceTypes = clsDatabaseFunctions.openResultSet(
				sSQL, 
				getServletContext(), 
				sDBID, 
				"MySQL", 
				"smcontrolpanel.SMUnbilledContractReportCriteriaSelection [1332275592]"
			);
			out.println("<TR><TD ALIGN=RIGHT><H4>Include orders with these service types:&nbsp;</H4></TD><TD>");
			while(rsServiceTypes.next()){
				String sServiceType = rsServiceTypes.getString(SMTableorderheaders.TableName + "." + SMTableorderheaders.sServiceTypeCode).trim();
				if(rsServiceTypes.getString(SMTableservicetypes.TableName + "." + SMTableservicetypes.id) != null) {
					out.println("<INPUT TYPE=CHECKBOX NAME=\"" + SERVICETYPE_PARAMETER 
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
					+ "<BR>");
				}	
			}
			rsServiceTypes.close();
			out.println("</TD>");
			out.println("</TR>");

		}catch(SQLException ex){
			//handle any errors
			out.println("Error reading locations and service types - " + ex.getMessage() + "<BR>");
		}
		
		bDateRangeToday = true;
		String sChecked = "";
		out.println("<TR><TD ALIGN=RIGHT><H4>And ONLY show orders with expected ship dates within this date range:&nbsp;</H4></TD>");
		out.println("<TD>");
		if (bDateRangeThisWeek){
			sChecked = clsServletUtilities.CHECKBOX_CHECKED_STRING;
		}else{
			sChecked = "";
		}
		out.println ("<INPUT TYPE=\"RADIO\" NAME=\"" + DATE_RANGE_PARAM 
				+ "\" VALUE=\"" + DATE_RANGE_THISWEEK + "\" " + sChecked + ">This week only (Mon-Sun)<BR>");

		if (bDateRangeNextWeek){
			sChecked = clsServletUtilities.CHECKBOX_CHECKED_STRING;
		}else{
			sChecked = "";
		}
		out.println ("<INPUT TYPE=\"RADIO\" NAME=\"" + DATE_RANGE_PARAM 
				+ "\" VALUE=\"" + DATE_RANGE_NEXTWEEK + "\" " + sChecked + ">Next week only (Mon-Sun)<BR>");
		
		if (bDateRangeToday){
			sChecked = clsServletUtilities.CHECKBOX_CHECKED_STRING;
		}else{
			sChecked = "";
		}
				
		out.println ("<INPUT TYPE=\"RADIO\" NAME=\"" + DATE_RANGE_PARAM 
				+ "\" VALUE=\"" + DATE_RANGE_TODAY + "\" " + sChecked + ">Today only<BR>");

		if (bDateRangeChosen){
			sChecked = clsServletUtilities.CHECKBOX_CHECKED_STRING;
		}else{
			sChecked = "";
		}
		out.println ("<INPUT TYPE=\"RADIO\" NAME=\"" + DATE_RANGE_PARAM 
				+ "\" VALUE=\"" + DATE_RANGE_CHOOSE + "\" " + sChecked + ">OR Choose dates:<BR>");
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
		
		out.println("</TABLE>");
		out.println("<BR>");
	
		out.println("<INPUT TYPE=\"SUBMIT\" NAME=" 
				+ GENERATE_REPORT_PARAMETER 
				+ " VALUE=\"" + GENERATE_REPORT_LABEL + "\">&nbsp;&nbsp;");
		out.println("</FORM>");

		out.println("This report will list any orders in the selection range which have <B>NOT BEEN SCHEDULED</B> on their 'expected ship dates'.");
		
		out.println("</BODY></HTML>");
	}
	public void doGet(HttpServletRequest request,
			HttpServletResponse response)
	throws ServletException, IOException {

		doPost(request, response);
	}

}