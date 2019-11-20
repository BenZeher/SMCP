package TimeCardSystem;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Enumeration;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsDateAndTimeConversions;
import ServletUtilities.clsManageRequestParameters;
import TCSDataDefinition.Departments;
import TCSDataDefinition.Employees;
import TCSDataDefinition.LeaveAdjustmentTypes;
import TCSDataDefinition.LeaveAdjustments;
import TCSDataDefinition.SpecialEntryTypes;
import TCSDataDefinition.TCSTablemadgicevents;
import TCSDataDefinition.TCSTablemadgiceventtypes;
import TCSDataDefinition.TCSTablemadgiceventusers;
import TCSDataDefinition.TimeEntries;

public class MADGICReportGenerate extends HttpServlet {
	
	private static final long serialVersionUID = 1L;

	public static final String PARAM_EXPORT_REPORT = "SubmitExport";
	public static final String PARAM_DISPLAY_REPORT = "SubmitDisplay";
	public static final String PARAM_SELECTED_EMPLOYEE = "SelectedEmployee";
	public static final String PARAM_RADIO_BUTTON_GROUP_DEPT_OR_EMPLOYEE = "DEPTOREMPLOYEE";
	public static final String PARAM_RADIO_BUTTON_GROUP_VALUE_DEPT = "DEPT";
	public static final String PARAM_RADIO_BUTTON_GROUP_VALUE_EMPLOYEE = "EMPLOYEE";
	public static final String PARAM_DEPARTMENT_PREFIX = "DEPTPREFIX";
	public static final String PARAM_ALL_EMPLOYEES_VALUE = "*ALLEMPLOYEES*";
	public static final String PARAM_USE_CURRENT_USER_FOR_EMPLOYEE_ID = "USECURRENTUSERFOREMPLOYEEID";
	public static final String PARAM_CHECKBOX_SHOW_DETAILS = "Show Details";
	public static final String MADGIC_REPORT_FORM_NAME = "MADGICCRITERIAFORM";
	private static final int POINT_VALUE_FOR_REGULAR_DAY_WORKED = 8;
	private static final int POINT_VALUE_FOR_BEEPER_CALL_WORKED = 10;
	private static final int POINT_VALUE_FOR_NIGHT_JOB_WORKED = 10;
	private static final int POINT_VALUE_FOR_WEEKEND_WORKED = 10;
	private static final int POINT_VALUE_FOR_HOLIDAY_WORKED = 8;
	private static final int POINT_VALUE_FOR_DAY_PAID_PER_MANAGER = 8;
	private static final int POINT_VALUE_FOR_JURY_DUTY_DAY = 8;
	private static final int POINT_VALUE_FOR_PERSONAL_BUSINESS_EXCUSED = 8;
	private static final int POINT_VALUE_FOR_ALLOWED_LATE_DAY = 4;
	private static final int POINT_VALUE_FOR_LEAVE_TYPE_VACATION = 8;
	private static final int POINT_VALUE_FOR_LEAVE_TYPE_PERSONAL_TIME_OFF_EXCUSED = 8;
	private static final int POINT_VALUE_FOR_LEAVE_TYPE_PERSONAL_TIME_OFF_NOT_EXCUSED = 0;
	private static final int STARTING_POINT_VALUE_CREDIT = 100;

	private static final int ALLOWED_NUMBER_OF_LATES = 3;
	
	//Entry types - these are data dependent.  That means the values could change any time!!!:
	public static final int ENTRY_TYPE_REGULAR_WORKED_DAY = 0;
	public static final int ENTRY_TYPE_ID_BEEPER_CALL = 6;
	public static final int ENTRY_TYPE_ID_NIGHT_JOB = 5;
	public static final int ENTRY_TYPE_ID_WEEKEND = 7;
	public static final int ENTRY_TYPE_ID_HOLIDAY = 8;
	public static final int ENTRY_TYPE_ID_PAID_PER_MANAGER = 9;
	public static final int ENTRY_TYPE_ID_JURY_DUTY = 10;
	public static final int ENTRY_TYPE_ID_PERSONAL_BUSINESS_EXCUSED = 11;
	
	//Leave types - these are data dependent.  That means the values could change any time!!!:
	public static final int LEAVE_TYPE_VACATION_DAY = 1;
	public static final int LEAVE_TYPE_PERSONAL_TIME_OFF_EXCUSED = 2;
	public static final int LEAVE_TYPE_PERSONAL_TIME_OFF_UNEXCUSED = 3;
	
	private static int TABLE_COLUMN_SPAN = 8;
	
	private static final String COLOR_LIGHT_BLUE = "#4d94ff"; //"#e6ffff";
	private static final String COLOR_LIGHT_LIGHT_BLUE = "#99c2ff"; //"#ccffff";
	//private static final String COLOR_LIGHT_GREEN = "#e6fff2";
	private static final String COLOR_LIGHT_GREEN_2 = "#b3ffb3";
	//private static final String COLOR_MEDIUM_BLUE = "#99c2ff";
	private static final String COLOR_LIGHT_SLATE = "#e0e0eb";
	private static final String COLOR_OF_EVENT_ENTRY_NAME = "#ff66ff"; //"#ff99ff";
	
	public void doGet(HttpServletRequest request,
			HttpServletResponse response)throws ServletException, IOException {

		response.setContentType("text/html");
		PrintWriter out = response.getWriter();

		HttpSession CurrentSession = request.getSession();
		String sDBID = (String)CurrentSession.getAttribute(TimeCardUtilities.SESSION_ATTRIBUTE_DB);
		boolean bExportToFileWasSelected = request.getParameter(PARAM_EXPORT_REPORT) != null;
		boolean bSelectByDepartment = clsManageRequestParameters.get_Request_Parameter(PARAM_RADIO_BUTTON_GROUP_DEPT_OR_EMPLOYEE,request).compareToIgnoreCase(PARAM_RADIO_BUTTON_GROUP_VALUE_DEPT) == 0;
		boolean bShowDetails = clsManageRequestParameters.get_Request_Parameter(PARAM_CHECKBOX_SHOW_DETAILS, request).compareToIgnoreCase("") != 0;
		String sSelectedEmployee = clsManageRequestParameters.get_Request_Parameter(PARAM_SELECTED_EMPLOYEE, request);
		if (clsManageRequestParameters.get_Request_Parameter(PARAM_USE_CURRENT_USER_FOR_EMPLOYEE_ID, request).compareToIgnoreCase("") != 0){
			sSelectedEmployee = (String) CurrentSession.getAttribute(TimeCardUtilities.SESSION_ATTRIBUTE_EID);
		}
		
		int iPeriod = 0;
		int iStartingYear = 0;
		try {
			iPeriod = Integer.parseInt(request.getParameter(TimeCardUtilities.PARAM_REPORTING_PERIOD).substring(4, 5));
			iStartingYear = Integer.parseInt(request.getParameter(TimeCardUtilities.PARAM_REPORTING_PERIOD).substring(0, "2016".length()));
		} catch (Exception e1) {
			out.println("<BR><BR><B>Error [1490192024] - You must select a reporting period.</B><BR>");
			out.println("</BODY></HTML>");
			return;
		}
		
		String sStartingDate = TCMADGICEvent.getStartingMADGICReportPeriodDate(iPeriod, iStartingYear);
		String sEndingDate = TCMADGICEvent.getEndingMADGICReportPeriodDate(iPeriod, iStartingYear);
		
		String m_sExportFileName = "MADGICReport"
				+ "-" + clsDateAndTimeConversions.now("yyyy-MM-dd")
				+ "-" + clsDateAndTimeConversions.now("hh-mm-ss")
				+ ".CSV";
		
		//IF the user chose to select by Dept, then get the departments selected:
		ArrayList<String> alDepartments = new ArrayList<String>(0);
		if (bSelectByDepartment){
			Enumeration<String> paramNames = request.getParameterNames();
			while(paramNames.hasMoreElements()) {
				String s = paramNames.nextElement().toString();
				if (s.length() >= MADGICReportGenerate.PARAM_DEPARTMENT_PREFIX.length()){
					if (s.substring(0, MADGICReportGenerate.PARAM_DEPARTMENT_PREFIX.length()).compareTo(MADGICReportGenerate.PARAM_DEPARTMENT_PREFIX) == 0){
						alDepartments.add(s.substring(MADGICReportGenerate.PARAM_DEPARTMENT_PREFIX.length(), s.length()));
					}
				}
			}
		}
		String sSelectionTypeDesc = "";
		if (bSelectByDepartment){
			sSelectionTypeDesc = "Using <B>THESE DEPARTMENTS</B> to select employees: ";
			
			String SQL = "SELECT"
				+ " " + Departments.sDeptDesc
				+ " FROM " + Departments.TableName
				+ " WHERE ("
				;
				for (int i = 0; i < alDepartments.size(); i++){
					if (i == 0){
						SQL += "(" + Departments.iDeptID + " = " + alDepartments.get(i) + ")";
					}else{
						SQL += " OR (" + Departments.iDeptID + " = " + alDepartments.get(i) + ")";
					}
				}
				SQL += ")"
				+ " ORDER BY " + Departments.sDeptDesc
			;
			try {
				ResultSet rsDepts = clsDatabaseFunctions.openResultSet(SQL, getServletContext(), sDBID);
				int iRecordCount = 0;
				while (rsDepts.next()){
					if (iRecordCount == 0){
						sSelectionTypeDesc += rsDepts.getString(Departments.sDeptDesc);
					}else{
						sSelectionTypeDesc += ", " + rsDepts.getString(Departments.sDeptDesc);
					}
					
					iRecordCount++;
				}
				rsDepts.close();
			} catch (SQLException e) {
				out.println("<BR><BR>Error [1485729380] reading departments with SQL: " + SQL + ". - " + e.getMessage() + ".<BR><BR>");
			}
				
			sSelectionTypeDesc += ".";
		}else{
			sSelectionTypeDesc = "Employee selected: " + sSelectedEmployee + ".";
		}
		if (bExportToFileWasSelected){
			response.setContentType("text/csv");
			String disposition = "attachment; fileName= " + m_sExportFileName;
			response.setHeader("Content-Disposition", disposition);
			out.println("\"MADGIC Report\"" + "\"");
		}else{
			String s = "<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.0 " 
				+ "Transitional//EN\">" 
				+ "<HTML>" 
				+ "<HEAD><TITLE>MADGIC Report</TITLE></HEAD>\n<BR>" 
				+ "<BODY BGCOLOR=\"" + TimeCardUtilities.BACKGROUND_COLOR_FOR_ADMIN_SCREENS + "\">" 
				+ "\n<TABLE BORDER=0 WIDTH=90%  style = \" font-family: arial; \"  >\n"
				+ "  <TR>\n"
				+ "    <TD VALIGN=BOTTOM><FONT SIZE=4><B>MADGIC Report</B></FONT></TD>\n" 

				+ "    <TD VALIGN=BOTTOM><B>&nbsp;&nbsp;&nbsp;&nbsp;" 
				+ "<FONT SIZE=2><B>Start Date:</B> " + sStartingDate
				+ "&nbsp;&nbsp;-&nbsp;&nbsp;" 
				+ "<B>End Date:</B> " + sEndingDate
				+ "&nbsp;&nbsp;" + sSelectionTypeDesc
				+ "</FONT></TD>\n"
				+ "  </TR>\n"
			;
			try {
				if (TimeCardUtilities.isFunctionPermitted(
					(String)CurrentSession.getAttribute(TimeCardUtilities.SESSION_ATTRIBUTE_EID), 
					sDBID, 
					AccessControlFunctionList.ViewAdministrationMenu, getServletContext())
					){
					s += "  <TR>\n"
					+ "    <TD><Font SIZE=3>"
					+ "<A HREF=\"" + ConnectionPool.WebContextParameters.getURLLinkBase(getServletContext()) + "TimeCardSystem.AdminMain\">Return to main menu</A>"
					+ "</FONT></TD>\n"
					+ "  </TR>\n"
					;
				}
			} catch (Exception e) {
				out.println("<BR><BR><B><FONT COLOR=RED>Error [1487601788] checking permissions - " + e.getMessage() + ".");
			}
			s += "</TABLE>\n";
			out.println(s);
		}
		
		//Make sure that the user made a valid choice:
		if (bSelectByDepartment && alDepartments.size() == 0){
			out.println("<BR><BR><B>Error [1485269069] - You chose to list by departments, but you didn't pick any departments.</B><BR>");
			out.println("</BODY></HTML>");
			return;
		}
		
		String sSQL = "SELECT * FROM " + Employees.TableName
			+ " LEFT JOIN " + Departments.TableName + " ON " + Employees.TableName + "." + Employees.iDepartmentID + "=" + Departments.TableName + "." + Departments.iDeptID
			+ " WHERE ("
				+ "(" + Employees.TableName + "." + Employees.iActive + " = 1)"
			;
		if (bSelectByDepartment) {
			sSQL += " AND (";
			for (int i=0;i<alDepartments.size();i++){
				if (i == 0){
					sSQL = sSQL + "(" + Employees.iDepartmentID + " = " + alDepartments.get(i).toString() + ")";
				}else{
					sSQL = sSQL + " OR (" + Employees.iDepartmentID + " = " + alDepartments.get(i).toString() + ")";
				}
			}
			sSQL += ")";
		}else{
			//The user has chosen to select by individual employee:
			if (sSelectedEmployee.compareToIgnoreCase(PARAM_ALL_EMPLOYEES_VALUE) != 0){
				sSQL += " AND (" + Employees.TableName + "." + Employees.sEmployeeID + " = '" + sSelectedEmployee + "')";
			}
		}
		
		//End 'WHERE' clause:
		sSQL += ")"
			+ " ORDER By " + Departments.TableName + "." + Departments.sDeptDesc + ", " + Employees.TableName + "." + Employees.sEmployeeLastName + ", " + Employees.sEmployeeFirstName;
			
		//out.println("[1485191341] sSQL = '" + sSQL + "'.");

		if (!bExportToFileWasSelected){
			out.println("\n<TABLE BORDER=0 WIDTH=100% cellpadding = \"6\" style = \" font-family: arial; \" >\n");
		}
		int iCurrentDepartment = -1;

		try {
			ResultSet rsEmployeeList = clsDatabaseFunctions.openResultSet(sSQL, getServletContext(), sDBID);
			while (rsEmployeeList.next()){
				EmployeeTotals totals = new EmployeeTotals(
					rsEmployeeList.getString(Employees.sEmployeeID),
					sStartingDate,
					sEndingDate,
					sDBID,
					rsEmployeeList.getString(Employees.sEmployeeLastName) + ", " 
						+ rsEmployeeList.getString(Employees.sEmployeeFirstName) + " " 
						+ rsEmployeeList.getString(Employees.sEmployeeMiddleName),
						rsEmployeeList.getString(Departments.TableName + "." + Departments.sDeptDesc),
					rsEmployeeList.getTimestamp(Employees.tStartTime)
				);

				totals.loadeventnames(sDBID);
				
				totals.processTotals();
				
				if (totals.employeeHasTimeInThisPeriod()){
					if (iCurrentDepartment != rsEmployeeList.getInt(Employees.TableName +"." + Employees.iDepartmentID)){
						if (!bShowDetails){
							printHeadings(
								bExportToFileWasSelected, 
								rsEmployeeList.getString(Departments.TableName + "." + Departments.sDeptDesc), 
								out, 
								totals.getListOfEventNames()
							);
						}
						
					}
					
					if (bShowDetails){
						out.println(totals.printEmployeeHeading(totals.getListOfEventNames()));
						out.println(totals.printEmployeeDetails(totals.getListOfEventNames(), CurrentSession));
					}
					out.println(totals.printEmployeeTotals(bShowDetails, totals.getListOfEventNames()));
				}
				
				iCurrentDepartment = rsEmployeeList.getInt(Employees.TableName +"." + Employees.iDepartmentID);
			}
			rsEmployeeList.close();
		} catch (Exception ex) {
			// handle any errors
			out.println("<BR><BR>Error [1485191340] - " + ex.getMessage() + "<BR>");
		}
		
		out.println("</TABLE>\n");
		
		if (!bExportToFileWasSelected){
			printRules(out);
		}

		out.flush();
		out.close();

		out.println("</BODY></HTML>");
		
		return;
	} //end of Get()

	private class EmployeeTotals extends Object {
		
		private String m_sEmployeeID;
		private int m_iNumberOfLatesForEmployee;
		private String m_sSQLFormattedStartingDate;
		private String m_sSQLFormattedEndingDate;
		private ArrayList<MADGICWorkedDay>m_arrDaysInSelectionRange;
		private boolean m_bEmployeeHasTimeInThisPeriod;
		private String m_sDBID;
		private String m_sEmployeeName;
		private String m_sDeptDescription;
		private Timestamp m_tsStartTime;
		private ArrayList<String>m_arrEventTypeNames;
		
		public EmployeeTotals(
				String sEmployeeID,
				String sStartingDate,
				String sEndingDate,
				String sDatabaseID,
				String sEmployeeName,
				String sDeptDesc,
				Timestamp tsStartTime
				) throws Exception {
			m_iNumberOfLatesForEmployee = 0;
			m_bEmployeeHasTimeInThisPeriod = false;
			m_sEmployeeID = sEmployeeID;
			m_sDBID = sDatabaseID;
			m_sEmployeeName = sEmployeeName;
			m_sDeptDescription = sDeptDesc;
			m_tsStartTime = tsStartTime;
			m_arrDaysInSelectionRange = new ArrayList<MADGICWorkedDay>(0);
			m_arrEventTypeNames = new ArrayList<String>(0);
			
			//Create an array of all the dates within the selected range:
			SimpleDateFormat sdf = new SimpleDateFormat("M/d/yyyy");
	        Calendar calStartingDate = Calendar.getInstance();
			try {
				calStartingDate.setTime(sdf.parse(sStartingDate));
			} catch (ParseException e2) {
				throw new Exception("Error [1485294631] - invalid starting date: '" + sStartingDate + "'.");
			}
	        Calendar calEndingDate = Calendar.getInstance();
			try {
				calEndingDate.setTime(sdf.parse(sEndingDate));
			} catch (ParseException e2) {
				throw new Exception("Error [1485294631] - invalid ending date: '" + sEndingDate + "'.");
			}
			m_arrDaysInSelectionRange.add(new MADGICWorkedDay(calStartingDate));
			while (m_arrDaysInSelectionRange.get(m_arrDaysInSelectionRange.size() - 1).m_calendarDay.before(calEndingDate)){
				Calendar calNextDay = Calendar.getInstance();
				try {
					calNextDay.setTime(
						sdf.parse(
							sdf.format(m_arrDaysInSelectionRange.get(m_arrDaysInSelectionRange.size() - 1).m_calendarDay.getTime())
						)
					);
				} catch (ParseException e) {
					//Shouldn't be able to happen...
				}
				calNextDay.add(Calendar.DAY_OF_YEAR, 1);
				m_arrDaysInSelectionRange.add(new MADGICWorkedDay(calNextDay));
			}
			SimpleDateFormat sqlSDFFormat = new SimpleDateFormat("yyyy-MM-dd");
			m_sSQLFormattedStartingDate = sqlSDFFormat.format(m_arrDaysInSelectionRange.get(0).m_calendarDay.getTime());
			m_sSQLFormattedEndingDate = sqlSDFFormat.format(m_arrDaysInSelectionRange.get(m_arrDaysInSelectionRange.size() - 1).m_calendarDay.getTime());
		}
		
		public ArrayList<String> getListOfEventNames(){
			return m_arrEventTypeNames;
		}
		
		public void processTotals() throws Exception{
			
			//First we process time entries:
			loadTimeEntries();
			
			//Next we process the leave entries:
			loadLeaveEntries();
			
			//Process the MADGIC Event entries:
			loadEventEntries();
			
			//Calculate points for time entries:
			calculatePointsOnTimeEntries();
			
			//Calculate points for leave entries:
			calculatePointsOnLeaveEntries();
			
		}
		
		public void loadeventnames(String sDBID) throws Exception{
			
			String SQL = "SELECT " + TCSTablemadgiceventtypes.sname
				+ " FROM " + TCSTablemadgiceventtypes.TableName
				+ " ORDER BY " + TCSTablemadgiceventtypes.sname
			;
			try {
				ResultSet rsEventTypeNames = clsDatabaseFunctions.openResultSet(
					SQL, 
					getServletContext(), 
					sDBID, 
					"MySQL", 
					this.toString() + ".loadeventnames"
				);
				while (rsEventTypeNames.next()){
					m_arrEventTypeNames.add(rsEventTypeNames.getString(TCSTablemadgiceventtypes.sname));
				}
				rsEventTypeNames.close();
			} catch (Exception e) {
				throw new Exception("Error [1486749264] loading event type names with SQL '" + SQL + "' - " + e.getMessage());
			}
		}
		
		private void calculatePointsOnTimeEntries(){
			
			for (int iIndividualDateInSelectionRange = 0; iIndividualDateInSelectionRange < m_arrDaysInSelectionRange.size(); iIndividualDateInSelectionRange++){
				for (int iTimeEntryOnIndividualDay = 0; iTimeEntryOnIndividualDay < m_arrDaysInSelectionRange.get(iIndividualDateInSelectionRange).arrTimeEntries.size(); iTimeEntryOnIndividualDay++){
					//We ONLY give points for a regular worked day is this is the FIRST 'REGULAR' time entry on the day, so we need to determine that:
					boolean bDayAlreadyHasREGULARTimeEntries = false;
					for (int i = 0; i < m_arrDaysInSelectionRange.get(iIndividualDateInSelectionRange).arrTimeEntries.size(); i++){
						if (m_arrDaysInSelectionRange.get(iIndividualDateInSelectionRange).arrTimeEntries.get(i).m_iEntryType == ENTRY_TYPE_REGULAR_WORKED_DAY){
							//If we've found a time entry on this day which IS a 'regular' day, then if it's a time entry BEFORE the current one,
							// indicate that there was previous REGULAR time on this day already:
							if (i < iTimeEntryOnIndividualDay){
								bDayAlreadyHasREGULARTimeEntries = true;
								break;
							}
						}
					}
					
					//Calculate points:
					MADGICTimeEntry te = m_arrDaysInSelectionRange.get(iIndividualDateInSelectionRange).arrTimeEntries.get(iTimeEntryOnIndividualDay);
					if (te.m_iEntryType == ENTRY_TYPE_REGULAR_WORKED_DAY){
						if (te.m_bLate){
							m_iNumberOfLatesForEmployee ++;
							if (m_iNumberOfLatesForEmployee <= ALLOWED_NUMBER_OF_LATES){
								if (!bDayAlreadyHasREGULARTimeEntries){
									te.m_iWeeklyPoints += POINT_VALUE_FOR_ALLOWED_LATE_DAY;
								}
							}
						}else{
							//IF it's not a 'LATE' entry:
							//AND if the day has not already been counted:
							if (!bDayAlreadyHasREGULARTimeEntries){
								te.m_iWeeklyPoints = POINT_VALUE_FOR_REGULAR_DAY_WORKED;
							}
						}//end if
					}else{
						//special entries get added in every time, even there WAS a previous 'REGULAR' time entry on this day:
						if (te.m_iEntryType == ENTRY_TYPE_ID_BEEPER_CALL){
							te.m_iBeeperPoints = POINT_VALUE_FOR_BEEPER_CALL_WORKED;
						}else if (te.m_iEntryType == ENTRY_TYPE_ID_NIGHT_JOB){
							te.m_iNightWorkPoints = POINT_VALUE_FOR_NIGHT_JOB_WORKED;
						}else if (te.m_iEntryType == ENTRY_TYPE_ID_WEEKEND){
							te.m_iWeekendPoints = POINT_VALUE_FOR_WEEKEND_WORKED;
						}else if (te.m_iEntryType == ENTRY_TYPE_ID_HOLIDAY){
							te.m_iWeeklyPoints = POINT_VALUE_FOR_HOLIDAY_WORKED;
						}else if (te.m_iEntryType == ENTRY_TYPE_ID_PAID_PER_MANAGER){
							te.m_iWeeklyPoints = POINT_VALUE_FOR_DAY_PAID_PER_MANAGER;
						}else if (te.m_iEntryType == ENTRY_TYPE_ID_JURY_DUTY){
							te.m_iWeeklyPoints = POINT_VALUE_FOR_JURY_DUTY_DAY;
						}else if (te.m_iEntryType == ENTRY_TYPE_ID_PERSONAL_BUSINESS_EXCUSED){
							te.m_iWeeklyPoints = POINT_VALUE_FOR_PERSONAL_BUSINESS_EXCUSED;
						}
					}//end if
				}
			}
		}
		
		private void calculatePointsOnLeaveEntries(){
			for (int iIndividualDateInSelectionRange = 0; iIndividualDateInSelectionRange < m_arrDaysInSelectionRange.size(); iIndividualDateInSelectionRange++){
				for (int iLeaveEntryOnIndividualDay = 0; iLeaveEntryOnIndividualDay < m_arrDaysInSelectionRange.get(iIndividualDateInSelectionRange).arrLeaveEntries.size(); iLeaveEntryOnIndividualDay++){
					//We have to determine if this day already has any REGULAR time entries on it before we try to calculate points:
					boolean bDayAlreadyHasREGULARTimeEntries = false;
					for (int i = 0; i < m_arrDaysInSelectionRange.get(iIndividualDateInSelectionRange).arrTimeEntries.size(); i++){
						if (m_arrDaysInSelectionRange.get(iIndividualDateInSelectionRange).arrTimeEntries.get(i).m_iEntryType == ENTRY_TYPE_REGULAR_WORKED_DAY){
							//If we've found a time entry on this day which IS a 'regular' time entry, then
							// indicate that there was previous REGULAR time on this day already:
							bDayAlreadyHasREGULARTimeEntries = true;
							break;
						}
					}

					MADGICLeaveEntry le = m_arrDaysInSelectionRange.get(iIndividualDateInSelectionRange).arrLeaveEntries.get(iLeaveEntryOnIndividualDay);
					
					//Add points if the leave entry qualifies:
					if (!bDayAlreadyHasREGULARTimeEntries){
						if (le.m_iLeaveType == LEAVE_TYPE_VACATION_DAY){
							le.m_iPointsAwarded = POINT_VALUE_FOR_LEAVE_TYPE_VACATION;
						} else if (le.m_iLeaveType == LEAVE_TYPE_PERSONAL_TIME_OFF_EXCUSED) {
							le.m_iPointsAwarded = POINT_VALUE_FOR_LEAVE_TYPE_PERSONAL_TIME_OFF_EXCUSED;
						} else if (le.m_iLeaveType == LEAVE_TYPE_PERSONAL_TIME_OFF_UNEXCUSED) {
							le.m_iPointsAwarded = POINT_VALUE_FOR_LEAVE_TYPE_PERSONAL_TIME_OFF_NOT_EXCUSED;
						}
					}//end if
				}
			}
		}
		
		public String printEmployeeHeading(ArrayList<String>arrEventNames){
			String s = "";
			SimpleDateFormat starttimeformat = new SimpleDateFormat("hh:mm a");
			s = "  <TR bgcolor=\"" + COLOR_LIGHT_SLATE + "\">\n"
				+ "    <TD><B>" + m_sDeptDescription + "</B></TD>\n"
				+ "    <TD COLSPAN=2><B>" + m_sEmployeeName + "</B> <I>Start: " + starttimeformat.format(m_tsStartTime.getTime()) + "</I></B></TD>\n"
				+ "    <TD ALIGN=RIGHT>Weekly</TD>\n"
				+ "    <TD ALIGN=RIGHT>Beeper</TD>\n"
				+ "    <TD ALIGN=RIGHT>Night</TD>\n"
				+ "    <TD ALIGN=RIGHT>Weekend</TD>\n"
			;
			
			for (int i = 0; i < arrEventNames.size(); i++){
				s += "    <TD ALIGN=RIGHT>" + arrEventNames.get(i) + "</TD>\n";
			}
			s += "    <TD ALIGN=RIGHT>Total</TD>\n"
				+ "  </TR>\n";
			return s;
		}
		public String printEmployeeTotals(boolean bPrintDetails, ArrayList<String>arrEventNames) throws Exception{
			String s = "";
			int m_iRegularPointTotalsForEmployee = 0;
			int m_iBeeperCallPointTotalsForEmployee = 0;
			int m_iNightJobPointTotalsForEmployee = 0;
			int m_iWeekEndPointTotalsForEmployee = 0;
			ArrayList<Integer>arrEventPointTotals = new ArrayList<Integer>(0);
			
			//Initialize the event points array:
			for (int i = 0; i < arrEventNames.size(); i++){
				arrEventPointTotals.add(0);
			}
			
			int iNumberOfWeekdaysInRangeToDate = 0;
			for (int iIndividualDayInSelectionRange = 0; iIndividualDayInSelectionRange < m_arrDaysInSelectionRange.size(); iIndividualDayInSelectionRange++){
				MADGICWorkedDay day = m_arrDaysInSelectionRange.get(iIndividualDayInSelectionRange);
				
				//If it's a weekday, add it to the weekday count for the range:
				if (
					(day.m_calendarDay.get(Calendar.DAY_OF_WEEK) > Calendar.SUNDAY)
					&& (day.m_calendarDay.get(Calendar.DAY_OF_WEEK) < Calendar.SATURDAY)
						
				){
					//Don't add any days past TODAY:
					Calendar calToday = Calendar.getInstance();
					//calToday.add(Calendar.DAY_OF_YEAR, 1);
					calToday.set(Calendar.HOUR_OF_DAY, 0);
					calToday.set(Calendar.MINUTE, 0);
					calToday.set(Calendar.SECOND, 0);
					//Now calTomorrow is set to tomorrow at 12:00AM
					if (!day.m_calendarDay.after(calToday)){
						iNumberOfWeekdaysInRangeToDate++;
					}
				}
				
				//Add up the time entry points:
				for (int iIndividualTimeEntry = 0; iIndividualTimeEntry < day.arrTimeEntries.size(); iIndividualTimeEntry++){
					MADGICTimeEntry timeentry = day.arrTimeEntries.get(iIndividualTimeEntry);
					m_iRegularPointTotalsForEmployee += timeentry.m_iWeeklyPoints;
					m_iBeeperCallPointTotalsForEmployee += timeentry.m_iBeeperPoints;
					m_iNightJobPointTotalsForEmployee += timeentry.m_iNightWorkPoints;
					m_iWeekEndPointTotalsForEmployee += timeentry.m_iWeekendPoints;
				}
				
				//Add up the leave entry points:
				for (int iIndividualLeaveEntry = 0; iIndividualLeaveEntry < day.arrLeaveEntries.size(); iIndividualLeaveEntry++){
					MADGICLeaveEntry leaventry = day.arrLeaveEntries.get(iIndividualLeaveEntry);
					m_iRegularPointTotalsForEmployee += leaventry.m_iPointsAwarded;
				}
				
				//Add up the event entry points:
				for (int iIndividualEventEntry = 0; iIndividualEventEntry < day.arrEventEntries.size(); iIndividualEventEntry++){
					MADGICEventEntry evententry = day.arrEventEntries.get(iIndividualEventEntry);
					for (int iIndividualEventTypeName = 0; iIndividualEventTypeName < arrEventNames.size(); iIndividualEventTypeName++){
						String sEventTypeName = arrEventNames.get(iIndividualEventTypeName);
						if (evententry.m_sEventTypeName.compareToIgnoreCase(sEventTypeName) == 0){
							arrEventPointTotals.set(iIndividualEventTypeName, arrEventPointTotals.get(iIndividualEventTypeName) + evententry.m_iPointsAwarded); 
						}
					}
				}
			}
			
			//Re-print the heading, if we are printing a bunch of details;
			if (bPrintDetails){
				s += printEmployeeHeading(arrEventNames);
			}
			
			//Print totals line:
			s += "  <TR bgcolor=\"white\" >\n";
			
			if (bPrintDetails){
				s += "    <TD ALIGN=LEFT>&nbsp;</TD>\n"
					+ "    <TD ALIGN=RIGHT COLSPAN=2>"
					+ "<B>GOAL: "
					+ Integer.toString((iNumberOfWeekdaysInRangeToDate * POINT_VALUE_FOR_REGULAR_DAY_WORKED) + STARTING_POINT_VALUE_CREDIT)
					+ "</B>"
					+ "&nbsp;<I>(" + Integer.toString(iNumberOfWeekdaysInRangeToDate) + " WEEKDAY(S) (THRU TODAY) X " 
					+ Integer.toString(POINT_VALUE_FOR_REGULAR_DAY_WORKED) + " DAILY POINTS"
					+ " = " + Integer.toString(iNumberOfWeekdaysInRangeToDate * POINT_VALUE_FOR_REGULAR_DAY_WORKED)
					+ ", + " + Integer.toString(STARTING_POINT_VALUE_CREDIT)
					+ " = " + Integer.toString((iNumberOfWeekdaysInRangeToDate * POINT_VALUE_FOR_REGULAR_DAY_WORKED) + STARTING_POINT_VALUE_CREDIT)
					+ ")</I>" 
					+ "&nbsp;&nbsp;&nbsp;&nbsp;<B>TOTALS:</B>"
					+ "</TD>\n"; 
			}else{
				s += "    <TD ALIGN=LEFT>" + m_sDeptDescription + "</TD>\n"
					+ "    <TD><B>" + m_sEmployeeName + "</B></TD>\n"; 
			}
				
			s += "    <TD ALIGN=RIGHT>" + Integer.toString(m_iRegularPointTotalsForEmployee) + "</TD>\n" 
				+ "    <TD ALIGN=RIGHT>" + Integer.toString(m_iBeeperCallPointTotalsForEmployee) + "</TD>\n" 
				+ "    <TD ALIGN=RIGHT>" + Integer.toString(m_iNightJobPointTotalsForEmployee) + "</TD>\n" 
				+ "    <TD ALIGN=RIGHT>" + Integer.toString(m_iWeekEndPointTotalsForEmployee) + "</TD>\n"
			;

			int iPointTotalForEmployee = m_iRegularPointTotalsForEmployee + m_iBeeperCallPointTotalsForEmployee + m_iNightJobPointTotalsForEmployee + m_iWeekEndPointTotalsForEmployee;
			
			//Print the columns for the event points:
			for (int i = 0; i < arrEventNames.size(); i++){
				s += "    <TD ALIGN=RIGHT>" + Integer.toString(arrEventPointTotals.get(i)) + "</TD>\n";
				iPointTotalForEmployee += arrEventPointTotals.get(i);
			}
			
			s += "    <TD ALIGN=RIGHT>" + Integer.toString((iPointTotalForEmployee)) + "</TD>\n" 
				+ "  </TR>\n"
			;
			if (bPrintDetails){
				//Print an extra row to separate each employee:
				s += "  <TR>\n"
					+ "    <TD COLSPAN=" + Integer.toString(TABLE_COLUMN_SPAN + arrEventNames.size()) + ">&nbsp</TD>\n"
					+ "  </TR>\n"
				;
			}
			
			return s;
		}
		
		public String printEmployeeDetails(ArrayList<String>arrEventTypeNames, HttpSession session) throws Exception{
			String s = "";
			SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
			SimpleDateFormat weekdayformat = new SimpleDateFormat("EEE");
			SimpleDateFormat punchformat = new SimpleDateFormat("MM/dd hh:mm a");
			int iNumberOfLates = 0;
			for (int i = 0; i < m_arrDaysInSelectionRange.size(); i++){
				MADGICWorkedDay day = m_arrDaysInSelectionRange.get(i);
				
				String sBackgroundColor = COLOR_LIGHT_BLUE;
				if ((i % 2) > 0){
					sBackgroundColor = COLOR_LIGHT_LIGHT_BLUE;
				}
				
				String sPrefix = sdf.format(day.m_calendarDay.getTime())
						+ " - "
						+ weekdayformat.format(day.m_calendarDay.getTime());
				
				for (int j = 0; j < day.arrTimeEntries.size(); j++){
					MADGICTimeEntry timeentry = day.arrTimeEntries.get(j);
					String sLate = "";
					if (timeentry.m_bLate){
						iNumberOfLates++;
						sLate = "<B><FONT COLOR=RED>LATE (" + Integer.toString(iNumberOfLates) + ")</FONT></B>";
					}
					
					String sChangeLogComment = "";
					if (timeentry.m_sChangeLogComment.trim().compareToIgnoreCase("") != 0){
						sChangeLogComment = "<I><FONT COLOR=YELLOW>(" + timeentry.m_sChangeLogComment.trim() + ")</FONT></I>";
					}
					s += "  <TR bgcolor=\"" + sBackgroundColor + "\" style = \" color: black; \" >\n"
						+ "    <TD>"
						+ sPrefix
						+ "    <TD><B><FONT COLOR=YELLOW>" + timeentry.m_sEntryTypeDesc + "</FONT></B>" + "</TD>\n"
						+ "    <TD><B>IN:</B>&nbsp;" + punchformat.format(timeentry.m_tsPunchedIn)
							+ " - <B>OUT:</B>&nbsp;" + punchformat.format(timeentry.m_tsPunchedOut)
							+ "&nbsp;&nbsp;&nbsp;&nbsp;" + sLate 
							+ " " + sChangeLogComment
						+ "</TD>"
						+ "    <TD ALIGN=RIGHT>" + Integer.toString(timeentry.m_iWeeklyPoints) + "</TD>\n"
						+ "    <TD ALIGN=RIGHT>" + Integer.toString(timeentry.m_iBeeperPoints) + "</TD>\n"
						+ "    <TD ALIGN=RIGHT>" + Integer.toString(timeentry.m_iNightWorkPoints) + "</TD>\n"
						+ "    <TD ALIGN=RIGHT>" + Integer.toString(timeentry.m_iWeekendPoints) + "</TD>\n"
					;
					for (int k = 0; k < arrEventTypeNames.size(); k++){
						s += "    <TD ALIGN=RIGHT>" + "0" + "</TD>\n";
					}
					s += "    <TD ALIGN=RIGHT>" + Integer.toString(
							timeentry.m_iWeeklyPoints
							+ timeentry.m_iBeeperPoints
							+ timeentry.m_iNightWorkPoints
							+ timeentry.m_iWeekendPoints
						) + "</TD>\n"
						+ "  </TR>\n"
					;
				}
				
				//Print any LEAVE entries:
				for (int j = 0; j < day.arrLeaveEntries.size(); j++){
					MADGICLeaveEntry leaveentry = day.arrLeaveEntries.get(j);
					s += "  <TR bgcolor=\"" + sBackgroundColor + "\" style = \" color: black; \" >\n"
						+ "    <TD>"
						+ sPrefix
						+ "    <TD><B><FONT COLOR=" + COLOR_LIGHT_GREEN_2 + ">" + leaveentry.m_sLeaveTypeDesc + "</FONT></B>" + "</TD>\n"
						+ "    <TD><B>IN:</B>&nbsp;" + punchformat.format(leaveentry.m_tsInTime)
							+ " - <B>OUT:</B>&nbsp;" + punchformat.format(leaveentry.m_tsOutTime)
						+ "</TD>\n"
						+ "    <TD ALIGN=RIGHT>" + Integer.toString(leaveentry.m_iPointsAwarded) + "</TD>\n"
						+ "    <TD ALIGN=RIGHT>" + "0" + "</TD>\n"
						+ "    <TD ALIGN=RIGHT>" + "0" + "</TD>\n"
						+ "    <TD ALIGN=RIGHT>" + "0" + "</TD>\n"
					;
					for (int k = 0; k < arrEventTypeNames.size(); k++){
						s += "    <TD ALIGN=RIGHT>" + "0" + "</TD>\n";
					}
					
					s += "    <TD ALIGN=RIGHT>" + Integer.toString(leaveentry.m_iPointsAwarded) + "</TD>\n"
						+ "  </TR>\n"
					;
				}

				//Print any EVENT entries:
				for (int j = 0; j < day.arrEventEntries.size(); j++){
					MADGICEventEntry evententry = day.arrEventEntries.get(j);
					
					String sLinkToMADGICEvent = "ID#&nbsp;" + Long.toString(evententry.m_lEventID);
					
					if ((TimeCardUtilities.IsAccessible(
						(ResultSet) session.getAttribute(TimeCardUtilities.SESSION_ATTRIBUTE_ACCCONTROLINFO),
						AccessControlFunctionList.EditMADGICEvents)
							) 
					){ 
						sLinkToMADGICEvent = "<A HREF=\"" 
							+ ConnectionPool.WebContextParameters.getURLLinkBase(getServletContext()) 
							+ "TimeCardSystem.TCEditMADGICEventsEdit?lid=" + Long.toString(evententry.m_lEventID) 
							+ "\">" + sLinkToMADGICEvent + "</A>"
						;
					}
					
					s += "  <TR bgcolor=\"" + sBackgroundColor + "\" style = \" color: black; \" >\n"
						+ "    <TD>"
						+ sPrefix
						+ "    <TD><B><FONT COLOR=" + COLOR_OF_EVENT_ENTRY_NAME + ">" + evententry.m_sEventTypeName + "</FONT></B>" + "</TD>\n"
						+ "    <TD><I>" + sLinkToMADGICEvent + "&nbsp;-&nbsp;" + evententry.m_sEventDesc + "</I></TD>\n"
					;
					
					//Print four columns of zeroes:
					s += 
						"    <TD ALIGN=RIGHT>" + "0" + "</TD>\n"
						+ "    <TD ALIGN=RIGHT>" + "0" + "</TD>\n"
						+ "    <TD ALIGN=RIGHT>" + "0" + "</TD>\n"
						+ "    <TD ALIGN=RIGHT>" + "0" + "</TD>\n"
					;
					
					for (int k = 0; k < arrEventTypeNames.size(); k++){
						if (arrEventTypeNames.get(k).compareToIgnoreCase(evententry.m_sEventTypeName) == 0){
							s += "    <TD ALIGN=RIGHT>" + Integer.toString(evententry.m_iPointsAwarded) + "</TD>\n";
						}else{
							s += "    <TD ALIGN=RIGHT>" + "0" + "</TD>\n";
						}
					}
					
					s += "    <TD ALIGN=RIGHT>" + Integer.toString(evententry.m_iPointsAwarded) + "</TD>\n"
						+ "  </TR>\n"
					;
				}
				
				//If there are NO entries, then make sure to just print the 'prefix' for the empty day:
				if (
					(day.arrTimeEntries.size() == 0)
					&& (day.arrLeaveEntries.size() == 0)
					&& (day.arrEventEntries.size() == 0)
				){
					s += "  <TR bgcolor=\"" + sBackgroundColor + "\" style = \" color: black; \" >\n"
						+ "    <TD>" + sPrefix + "</TD>\n"
						+ "    <TD>" + "-" + "</TD>\n"
						+ "    <TD>" + "-" + "</TD>\n"
						+ "    <TD ALIGN=RIGHT>" + "-" + "</TD>\n"
						+ "    <TD ALIGN=RIGHT>" + "-" + "</TD>\n"
						+ "    <TD ALIGN=RIGHT>" + "-" + "</TD>\n"
						+ "    <TD ALIGN=RIGHT>" + "-" + "</TD>\n"
					;
					for (int k = 0; k < arrEventTypeNames.size(); k++){
						s += "    <TD ALIGN=RIGHT>" + "-" + "</TD>\n";
					}
					
					s += "    <TD ALIGN=RIGHT>" + "-" + "</TD>\n"
						+ "  </TR>\n"
					;
				}
			}
			
			return s;
		}
		
		public boolean employeeHasTimeInThisPeriod(){
			return m_bEmployeeHasTimeInThisPeriod;
		}
		private void loadLeaveEntries() throws Exception{

			String sSQL = "SELECT * FROM " + LeaveAdjustments.TableName + ", " + LeaveAdjustmentTypes.TableName 
				+ " WHERE (" 
					+ "(" + LeaveAdjustments.TableName + "." + LeaveAdjustments.iLeaveTypeID + " = " + LeaveAdjustmentTypes.TableName + "." + LeaveAdjustmentTypes.iTypeID + ")"
					+ " AND (" + LeaveAdjustments.TableName + "." + LeaveAdjustments.sEmployeeID + " = '" + m_sEmployeeID + "')" 
					+ " AND (" 
						
						+ "("
							+ "(" + LeaveAdjustments.TableName + "." + LeaveAdjustments.dtInTime + " >= '" + m_sSQLFormattedStartingDate + "') AND (" 
								+ LeaveAdjustments.TableName + "." + LeaveAdjustments.dtInTime + " <= '" + m_sSQLFormattedEndingDate + "')"
						+ ")"
						+ " OR "
						+ "("
							+ "(" + LeaveAdjustments.TableName + "." + LeaveAdjustments.dtOutTime + " >= '" + m_sSQLFormattedStartingDate + "') AND (" 
								+ LeaveAdjustments.TableName + "." + LeaveAdjustments.dtOutTime + " <= '" + m_sSQLFormattedEndingDate + "')"
						+ ")"
						
					+ ")"  //End the outer 'AND" clause
				+ ")"  //End the outer WHERE clause
				+ " ORDER BY " + LeaveAdjustments.TableName + "." + LeaveAdjustments.dtInTime
			; 
		
			try {
				ResultSet rsLeaveRecords = clsDatabaseFunctions.openResultSet(sSQL, getServletContext(), m_sDBID);
				while (rsLeaveRecords.next()){
					//First record that the employee has at least SOME records in this date range:
					m_bEmployeeHasTimeInThisPeriod = true;
					
					//Break this entry into multiple entries for different days, if it starts on one day and finishes on a different one:
					ArrayList<Timestamp> tsStartingTimes = new ArrayList<Timestamp>(0);
					ArrayList<Timestamp> tsEndingTimes = new ArrayList<Timestamp>(0);
					processTimeSpanIntoSeparateDays(
						rsLeaveRecords.getTimestamp(LeaveAdjustments.TableName + "." + LeaveAdjustments.dtInTime),
						rsLeaveRecords.getTimestamp(LeaveAdjustments.TableName + "." + LeaveAdjustments.dtOutTime),
						tsStartingTimes, 
						tsEndingTimes
					);
					
					//For each of the individual leave entries:
					for (int i = 0; i < tsStartingTimes.size(); i++){
						//Find the day in the overall date selection range that matches this day:
						for (int iIndividualDateInSelectedRange = 0; iIndividualDateInSelectedRange < m_arrDaysInSelectionRange.size(); iIndividualDateInSelectedRange++){
							MADGICWorkedDay day = m_arrDaysInSelectionRange.get(iIndividualDateInSelectedRange);
							
							//If this leave day matches this individual date in the selection range, then add it:
							if (compareDayOnCalendarAndTimestamp(day.m_calendarDay, tsStartingTimes.get(i))){
								//If the starting and ending times for this leave entry span a weekend, we don't want to add the weekend days.
								//So here we ONLY add the leave time if it's for a weekday:
								if ((day.m_calendarDay.get(Calendar.DAY_OF_WEEK) > Calendar.SUNDAY)
										&& (day.m_calendarDay.get(Calendar.DAY_OF_WEEK) < Calendar.SATURDAY)){
									//Add the leave entries to the day:
									MADGICLeaveEntry le = new MADGICLeaveEntry(
										tsStartingTimes.get(i),
										tsEndingTimes.get(i),
										rsLeaveRecords.getInt(LeaveAdjustments.TableName + "." + LeaveAdjustments.iLeaveTypeID),
										rsLeaveRecords.getString(LeaveAdjustmentTypes.TableName + "." + LeaveAdjustmentTypes.sTypeTitle)
									);
									//Now add the leave entry:
									day.arrLeaveEntries.add(le);
								}
								break;  //break and go to the next time entry
							}
						}
					}
				}
				rsLeaveRecords.close();
			} catch (Exception e) {
				throw new Exception("Error [1485189902] - reading leave adjustments with SQL: " + sSQL + " - " + e.getMessage());
			}
		}

		private void loadEventEntries() throws Exception{

			String sSQL = "SELECT * FROM " + TCSTablemadgiceventusers.TableName 
				+ " LEFT JOIN " + TCSTablemadgicevents.TableName
				+ " ON " + TCSTablemadgiceventusers.TableName + "." + TCSTablemadgiceventusers.lmadgiceventid 
				+ " = " + TCSTablemadgicevents.TableName + "." + TCSTablemadgicevents.lid
				+ " WHERE (" 
					+ "(" + TCSTablemadgiceventusers.TableName + "." + TCSTablemadgiceventusers.semployeeid + " = '" + m_sEmployeeID + "')" 
					+ " AND (" + TCSTablemadgicevents.TableName + "." + TCSTablemadgicevents.datevent  + " >= '" + m_sSQLFormattedStartingDate + "')"
					+ " AND (" + TCSTablemadgicevents.TableName + "." + TCSTablemadgicevents.datevent  + " <= '" + m_sSQLFormattedEndingDate + "')"
				+ ")"  //End the outer WHERE clause
				+ " ORDER BY " + TCSTablemadgicevents.TableName + "." + TCSTablemadgicevents.datevent + ", " + TCSTablemadgicevents.TableName + "." + TCSTablemadgicevents.lid
			; 
			try {
				ResultSet rsEventRecords = clsDatabaseFunctions.openResultSet(sSQL, getServletContext(), m_sDBID);
				while (rsEventRecords.next()){
					//First record that the employee has at least SOME records in this date range:
					m_bEmployeeHasTimeInThisPeriod = true;

					//Find the day in the overall date selection range that matches this day:
					for (int iIndividualDateInSelectedRange = 0; iIndividualDateInSelectedRange < m_arrDaysInSelectionRange.size(); iIndividualDateInSelectedRange++){
						MADGICWorkedDay day = m_arrDaysInSelectionRange.get(iIndividualDateInSelectedRange);
						
						//If this event day matches this individual date in the selection range, then add it:
						if (compareDayOnCalendarAndTimestamp(day.m_calendarDay, rsEventRecords.getTimestamp(TCSTablemadgicevents.TableName + "." + TCSTablemadgicevents.datevent))){
							//Add the leave entries to the day:
							MADGICEventEntry evententry = new MADGICEventEntry(
								rsEventRecords.getTimestamp(TCSTablemadgicevents.TableName + "." + TCSTablemadgicevents.datevent),
								rsEventRecords.getInt(TCSTablemadgicevents.TableName + "." + TCSTablemadgicevents.inumberofpoints),
								rsEventRecords.getString(TCSTablemadgicevents.TableName + "." + TCSTablemadgicevents.seventtypename),
								rsEventRecords.getString(TCSTablemadgicevents.TableName + "." + TCSTablemadgicevents.sdescription),
								rsEventRecords.getLong(TCSTablemadgicevents.TableName + "." + TCSTablemadgicevents.lid)
							);
							
							//Now add the event entry:
							day.arrEventEntries.add(evententry);
							break;  //break and go to the next event entry
						}
					}
				}
				rsEventRecords.close();
			} catch (Exception e) {
				throw new Exception("Error [1486747951] - reading event entries with SQL: " + sSQL + " - " + e.getMessage());
			}
		}
		
		private void loadTimeEntries() throws Exception{
			String sSQL = 	"SELECT * FROM " + TimeEntries.TableName
				+ " LEFT JOIN " + SpecialEntryTypes.TableName
				+ " ON " + TimeEntries.TableName + "." + TimeEntries.iEntryTypeID + " = " + SpecialEntryTypes.TableName + "." + SpecialEntryTypes.iTypeID
				+ " WHERE (" 
					+ "(" + TimeEntries.TableName + "." + TimeEntries.sEmployeeID + " = '" + m_sEmployeeID + "')"
					+ " AND ("
						//Either the IN time or the OUT time is within the period selected:
						+ "((" + TimeEntries.TableName + "." + TimeEntries.dtInTime + " >= '" + m_sSQLFormattedStartingDate + "') AND (" 
							+ TimeEntries.TableName + "." + TimeEntries.dtInTime + " <= '" + m_sSQLFormattedEndingDate + " 23:59:59'))"
							+ "OR ((" + TimeEntries.TableName + "." + TimeEntries.dtOutTime + " >= '" + m_sSQLFormattedStartingDate + "')"
								+ " AND (" + TimeEntries.TableName + "." + TimeEntries.dtOutTime + " <= '" + m_sSQLFormattedEndingDate + " 23:59:59'))"
					+ ")"
					//Make sure we ONLY get entries with actual in AND out times:
					+ " AND (" + TimeEntries.TableName + "." + TimeEntries.dtInTime + " <> '0000-00-00')"
					+ " AND (" + TimeEntries.TableName + "." + TimeEntries.dtOutTime + " <> '0000-00-00')"
					
					//Limit to ONLY time entries that have NO period date, or the 'temporary' posting date:
					//+ " AND (" 
					//	+ "(" + TimeEntries.TableName + "." + TimeEntries.sPeriodDate + " = '0000-00-00')"
					//	+ " OR (" + TimeEntries.TableName + "." + TimeEntries.sPeriodDate + " = '" + TimeCardUtilities.TEMPORARY_POSTING_DATE + "')"
					//+ ")"
				+ ")" //end WHERE
				+ " ORDER BY " + TimeEntries.TableName + "." + TimeEntries.dtInTime
			;

			//System.out.println("[1485386602] " + sSQL);
			
			try {
				ResultSet rsTimeEntryList = clsDatabaseFunctions.openResultSet(sSQL, getServletContext(), m_sDBID);
				while(rsTimeEntryList.next()){
					m_bEmployeeHasTimeInThisPeriod = true;
					
					//Break this entry into multiple entries for different days, if it starts on one day and finishes on a different one:
					ArrayList<Timestamp> tsStartingTimes = new ArrayList<Timestamp>(0);
					ArrayList<Timestamp> tsEndingTimes = new ArrayList<Timestamp>(0);
					
					// TJR - 6/8/2017 - let the 'overrunning' entries (i.e., the ones that go past midnight) stay as single entries instead of splitting them up into multiple days:
					//processTimeSpanIntoSeparateDays(
					//	rsTimeEntryList.getTimestamp(TimeEntries.TableName + "." + TimeEntries.dtInTime), 
					//	rsTimeEntryList.getTimestamp(TimeEntries.TableName + "." + TimeEntries.dtOutTime), 
					//	tsStartingTimes, 
					//	tsEndingTimes
					//);
					
					//Just add the time entries:
					tsStartingTimes.add(rsTimeEntryList.getTimestamp(TimeEntries.TableName + "." + TimeEntries.dtInTime));
					tsEndingTimes.add(rsTimeEntryList.getTimestamp(TimeEntries.TableName + "." + TimeEntries.dtOutTime));
					
					//For each of the individual time entries in this record, look for a matching day in the selection range:
					for (int i = 0; i < tsStartingTimes.size(); i++){
						for (int iIndividualDateInSelectedRange = 0; iIndividualDateInSelectedRange < m_arrDaysInSelectionRange.size(); iIndividualDateInSelectedRange++){
							MADGICWorkedDay day = m_arrDaysInSelectionRange.get(iIndividualDateInSelectedRange);
							
							//If the day in the selection range MATCHES the day of the 'start time' for the time entry, then add it to the list:
							if (compareDayOnCalendarAndTimestamp(day.m_calendarDay, tsStartingTimes.get(i))){
								//Add the time entry to the day:
								MADGICTimeEntry te = new MADGICTimeEntry(
									tsStartingTimes.get(i),
									tsEndingTimes.get(i),
									rsTimeEntryList.getInt(TimeEntries.TableName + "." + TimeEntries.iEntryTypeID),
									rsTimeEntryList.getString(SpecialEntryTypes.TableName + "." + SpecialEntryTypes.sTypeTitle),
									rsTimeEntryList.getInt(TimeEntries.TableName + "." + TimeEntries.iLate) == 1,
									rsTimeEntryList.getString(TimeEntries.mChangeLog)
								);
								day.arrTimeEntries.add(te);
								break; //Move on to the next time entry
							}
						}
					}
				}
				rsTimeEntryList.close();
			} catch (Exception e) {
				throw new Exception("Error [1485465393] - reading Time entries - " + e.getMessage());
			}
		}
		
		private boolean compareDayOnCalendarAndTimestamp(Calendar calFirstDate, Timestamp tsSecondDate){
			
			Calendar calSecond = Calendar.getInstance();
			calSecond.setTimeInMillis(tsSecondDate.getTime());
			if (
				calFirstDate.get(Calendar.YEAR) == calSecond.get(Calendar.YEAR)
				&& calFirstDate.get(Calendar.MONTH) == calSecond.get(Calendar.MONTH)
				&& calFirstDate.get(Calendar.DAY_OF_YEAR) == calSecond.get(Calendar.DAY_OF_YEAR)
			){
				return true;
			}else{
				return false;
			}
		}
		
		private class MADGICTimeEntry {
			public Timestamp m_tsPunchedIn;
			public Timestamp m_tsPunchedOut;
			public String m_sEntryTypeDesc;
			public int m_iEntryType;
			public boolean m_bLate;
			public int m_iWeeklyPoints;
			public int m_iBeeperPoints;
			public int m_iNightWorkPoints;
			public int m_iWeekendPoints;
			public String m_sChangeLogComment;
			
			public MADGICTimeEntry(
					Timestamp tsPunchedIn,
					Timestamp tsPunchedOut,
					int iEntryType,
					String sEntryTypeDesc,
					boolean bLate,
					String sChangeLogComment
					) {
				m_tsPunchedIn = tsPunchedIn;
				m_tsPunchedOut = tsPunchedOut;
				m_iEntryType = iEntryType;
				m_sEntryTypeDesc = sEntryTypeDesc;
				m_bLate = bLate;
				m_sChangeLogComment = sChangeLogComment;
			}
		}
		
		private class MADGICLeaveEntry extends Object{

			public Timestamp m_tsInTime;
			public Timestamp m_tsOutTime;
			public int m_iPointsAwarded;
			public int m_iLeaveType;
			public String m_sLeaveTypeDesc;
			
			public MADGICLeaveEntry(
					Timestamp tsInTime,
					Timestamp tsOutTime,
					int iLeaveEntryType,
					String sLeaveTypeDesc
				) {
				m_tsInTime = tsInTime;
				m_tsOutTime = tsOutTime;
				m_iLeaveType = iLeaveEntryType;
				m_sLeaveTypeDesc = sLeaveTypeDesc;
			}
		}

		private class MADGICEventEntry extends Object{

			public int m_iPointsAwarded;
			public String m_sEventTypeName;
			public String m_sEventDesc;
			public long m_lEventID;
			
			public MADGICEventEntry(
					Timestamp tsEventDate,
					int iPointsAwarded,
					String sEventTypeName,
					String sEventDesc,
					long lEventID
				) {
				m_sEventTypeName = sEventTypeName;
				m_sEventDesc = sEventDesc;
				m_iPointsAwarded = iPointsAwarded;
				m_lEventID = lEventID;
			}
		}

		private class MADGICWorkedDay extends Object{

			public Calendar m_calendarDay;
			public ArrayList<MADGICTimeEntry>arrTimeEntries = new ArrayList<MADGICTimeEntry>(0);
			public ArrayList<MADGICLeaveEntry>arrLeaveEntries = new ArrayList<MADGICLeaveEntry>(0);
			public ArrayList<MADGICEventEntry>arrEventEntries = new ArrayList<MADGICEventEntry>(0);

			public MADGICWorkedDay(
					Calendar cCalendarDate
					) {
				m_calendarDay = cCalendarDate;
			}
		}
	}
	
	private void printHeadings(boolean bExport, String sCurrentDeptDesc, PrintWriter pwOut, ArrayList<String>arrEventTypeNames){
		String s = "  <TR>\n"
			+ "    <TD SPAN=" + Integer.toString(TABLE_COLUMN_SPAN + arrEventTypeNames.size()) + ">&nbsp;</TD>\n"
			+ "  </TR>\n"
			+ "  <TR bgcolor=\"black\" style = \" color: white; \"  >\n"
				+ "    <TD ALIGN=LEFT><B>Dept</B></TD>\n"
				+ "    <TD ALIGN=LEFT><B>Employee</B></TD>\n" 
				+ "    <TD ALIGN=RIGHT><B>Weekly</B></TD>\n" 
				+ "    <TD ALIGN=RIGHT><B>Beeper</B></TD>\n" 
				+ "    <TD ALIGN=RIGHT><B>Night</B></TD>\n" 
				+ "    <TD ALIGN=RIGHT><B>Weekend</B></TD>\n"
			;
			
			for (int i = 0; i < arrEventTypeNames.size(); i++){
				s += "    <TD ALIGN=LEFT><B>" + arrEventTypeNames.get(i) + "</B></TD>\n";
			}
			//Finish the headings:
			s += "    <TD ALIGN=RIGHT><B>Total</B></TD>\n" 
				+ "  </TR>\n" 
			;
		pwOut.println(s);
	}
	
	private void printRules(PrintWriter pwOut){
		pwOut.println("</TABLE><BR>");
		pwOut.println("<DIV style = \" font-family: arial; \" >");
		pwOut.println("<B><U>POINT VALUES AND RULES:</U></B><BR>");
		pwOut.println("<BR>");
		pwOut.println(" - REGULAR TIME ENTRY - " + Integer.toString(POINT_VALUE_FOR_REGULAR_DAY_WORKED) + " points per day.<BR>");
		pwOut.println(" - BEEPER CALL ENTRY - " + Integer.toString(POINT_VALUE_FOR_BEEPER_CALL_WORKED) + " points per day. (no date restriction)<BR>");
		pwOut.println(" - NIGHT JOB ENTRY - " + Integer.toString(POINT_VALUE_FOR_NIGHT_JOB_WORKED) + " points per day. (no date restriction)<BR>");
		pwOut.println(" - WEEKEND JOB ENTRY - " + Integer.toString(POINT_VALUE_FOR_WEEKEND_WORKED) + " points per day. (no date restriction)<BR>");
		pwOut.println(" - HOLIDAY ENTRY - " + Integer.toString(POINT_VALUE_FOR_HOLIDAY_WORKED) + " points per day.<BR>");
		pwOut.println(" - 'PAID PER MANAGER' ENTRY - " + Integer.toString(POINT_VALUE_FOR_DAY_PAID_PER_MANAGER) + " points per day.<BR>");
		pwOut.println(" - JURY DUTY ENTRY - " + Integer.toString(POINT_VALUE_FOR_JURY_DUTY_DAY) + " points per day.<BR>");
		pwOut.println(" - 'PERSONAL BUSINESS EXCUSED' ENTRY - " + Integer.toString(POINT_VALUE_FOR_PERSONAL_BUSINESS_EXCUSED) + " points per day.<BR>");
		pwOut.println("<BR>");
		pwOut.println("<B>LEAVE TIMES:</B><BR>");
		pwOut.println(" - VACATION - " + Integer.toString(POINT_VALUE_FOR_LEAVE_TYPE_VACATION) + " points per day.<BR>");
		pwOut.println(" - PERSONAL TIME OFF - EXCUSED - " + Integer.toString(POINT_VALUE_FOR_LEAVE_TYPE_PERSONAL_TIME_OFF_EXCUSED) + " points per day.<BR>");
		pwOut.println(" - PERSONAL TIME OFF - <B>NOT</B> EXCUSED - " + Integer.toString(POINT_VALUE_FOR_LEAVE_TYPE_PERSONAL_TIME_OFF_NOT_EXCUSED) + " points per day.<BR>");
		pwOut.println("<BR>");
		pwOut.println("A LATE day counts for " + Integer.toString(POINT_VALUE_FOR_ALLOWED_LATE_DAY) + " points.<BR>");
		pwOut.println("If there are more than " + Integer.toString(ALLOWED_NUMBER_OF_LATES) + " lates, then there are NO points for a late day.<BR>");
		pwOut.println("<BR>");
		pwOut.println("1. Multiple regular time entries on one day count as one day as far as points are concerned.<BR>");
		pwOut.println("2. Leave entries are treated like regular time entries with the exception that the number of days in one leave entry is taken into consideration.<BR>");
		pwOut.println("3. Any other entry type is worth ZERO points.<BR>");
		pwOut.println("</DIV>");
	}
	
	private void processTimeSpanIntoSeparateDays(
		Timestamp tsInTime, 
		Timestamp tsOutTime, 
		ArrayList<Timestamp> tsStartingTimes, 
		ArrayList<Timestamp> tsEndingTimes){
		
		Calendar calStartTime = Calendar.getInstance();
		calStartTime.setTimeInMillis(tsInTime.getTime());
		Calendar calOutTime = Calendar.getInstance();
		calOutTime.setTimeInMillis(tsOutTime.getTime());
		
		//Keep adding a day to the 'start' time, and keep looping until it's equal or greater than the 'end' time:
		while (calStartTime.getTimeInMillis() <= calOutTime.getTimeInMillis()){
			tsStartingTimes.add(new Timestamp (calStartTime.getTimeInMillis()));

			//Get the END of the day:
			Calendar calEndOfCurrentDay = Calendar.getInstance();
			calEndOfCurrentDay.setTime(calStartTime.getTime());
			calEndOfCurrentDay.set(Calendar.HOUR_OF_DAY, 23);
			calEndOfCurrentDay.set(Calendar.MINUTE, 59);
			calEndOfCurrentDay.set(Calendar.SECOND, 59);
			
			//If the end of the day would be AFTER the 'OUT' time, then add the 'OUT' time and just and exit:
			if (calEndOfCurrentDay.getTimeInMillis() >= calOutTime.getTimeInMillis()){
				tsEndingTimes.add(new Timestamp (calOutTime.getTimeInMillis()));
				break;
			//But if the end of the day would still be EARLIER than the 'OUT' time, re-iterate the loop:
			}else{
				//Set the next 'start time' to the beginning of the next day:
				calStartTime.add(Calendar.DAY_OF_YEAR, 1);
				calStartTime.set(Calendar.HOUR_OF_DAY, 0);
				calStartTime.set(Calendar.MINUTE, 0);
				calStartTime.set(Calendar.SECOND, 0);
				
				//Set the ending time
				tsEndingTimes.add(new Timestamp (calEndOfCurrentDay.getTimeInMillis()));
			}
		}
	}
}
