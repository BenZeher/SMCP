package TimeCardSystem;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Enumeration;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import SMDataDefinition.SMMasterStyleSheetDefinitions;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsDateAndTimeConversions;
import ServletUtilities.clsManageRequestParameters;
import TCSDataDefinition.Employees;
import TCSDataDefinition.TCSTableEmployeeMilestones;
import TCSDataDefinition.TCSTableEmployeeTypeLinks;
import TCSDataDefinition.TCSTableEmployeeTypes;
import TCSDataDefinition.TCSTableMilestones;

public class MilestonesReportGenerate extends HttpServlet {
	
	private static final long serialVersionUID = 1L;

	public static final String PARAM_EXPORT_REPORT = "SubmitExport";
	public static final String PARAM_DISPLAY_REPORT = "SubmitDisplay";
	public static final String PARAM_SELECTED_EMPLOYEE = "SelectedEmployee";
	public static final String PARAM_RADIO_BUTTON_GROUP_TYPE_OR_EMPLOYEE = "TYPEOREMPLOYEE";
	public static final String PARAM_RADIO_BUTTON_GROUP_VALUE_TYPE = "TYPE";
	public static final String PARAM_RADIO_BUTTON_GROUP_VALUE_EMPLOYEE = "EMPLOYEE";
	public static final String PARAM_EMPLOYEE_TYPE_PREFIX = "TYPEPREFIX";
	public static final String PARAM_ALL_EMPLOYEES_VALUE = "*ALLEMPLOYEES*";
	public static final String PARAM_USE_CURRENT_USER_FOR_EMPLOYEE_ID = "USECURRENTUSERFOREMPLOYEEID";
	public static final String PARAM_ALLOW_CLEARING = "ALLOWCLEARING";
	public static final String PARAM_CLEAR_MILESTONE = "CLEARINGMILESTONE";
	public static final String MILESTONES_REPORT_FORM_NAME = "MILESTONESCRITERIAFORM";
	public static final String PARAM_CHECKBOX_SHOW_DETAILS = "Show Details";

	public static final String INCOMPLETE_MILESTONE_TEXT = "Incomplete";
	private static final int TABLE_COLUMN_SPAN = 3;
	
	public void doPost(HttpServletRequest request,
			HttpServletResponse response)
					throws ServletException, IOException {

		response.setContentType("text/html");
		PrintWriter out = response.getWriter();

		HttpSession CurrentSession = request.getSession();
		String sDBID = (String)CurrentSession.getAttribute(TimeCardUtilities.SESSION_ATTRIBUTE_DB);
		String sUserName = (String)CurrentSession.getAttribute(TimeCardUtilities.SESSION_ATTRIBUTE_EID);
		boolean bExportToFileWasSelected = request.getParameter(PARAM_EXPORT_REPORT) != null;
		boolean bSelectByEmployeeType = clsManageRequestParameters.get_Request_Parameter(PARAM_RADIO_BUTTON_GROUP_TYPE_OR_EMPLOYEE,request).compareToIgnoreCase(PARAM_RADIO_BUTTON_GROUP_VALUE_TYPE) == 0;
		boolean bAllowClearing = clsManageRequestParameters.get_Request_Parameter(PARAM_ALLOW_CLEARING, request).compareToIgnoreCase("") != 0;
		String sSelectedEmployee = clsManageRequestParameters.get_Request_Parameter(PARAM_SELECTED_EMPLOYEE, request);
		if (clsManageRequestParameters.get_Request_Parameter(PARAM_USE_CURRENT_USER_FOR_EMPLOYEE_ID, request).compareToIgnoreCase("") != 0){
			sSelectedEmployee = (String) CurrentSession.getAttribute(TimeCardUtilities.SESSION_ATTRIBUTE_EID);
		}
		//Check to see if a milestone record was requested to be deleted
		if (clsManageRequestParameters.get_Request_Parameter(PARAM_CLEAR_MILESTONE, request).compareToIgnoreCase("") != 0){

			String sMilstoneID = clsManageRequestParameters.get_Request_Parameter(PARAM_CLEAR_MILESTONE, request);
			//System.out.println("Button detected for milstone ID:" + sMilstoneID );
	    	Connection conn = null;
	    	conn = clsDatabaseFunctions.getConnection(getServletContext(), sDBID, "MySQL", this.getClass().getName() + "- user: " + sUserName);
	
			//delete the All users in this type:
			 String SQL = "DELETE FROM " + TCSTableEmployeeMilestones.TableName
				+ " WHERE ("
					+ "(" + TCSTableEmployeeMilestones.lid + " = " + sMilstoneID + ")"
				+ ")"
			;
			//System.out.println(SQL);
			try {
				Statement stmt = conn.createStatement();
				stmt.execute(SQL);
			} catch (Exception e) {
				out.println("Error [1512422601] deleting EmployeeMilestone record with " + " ID '" 
					+ sMilstoneID + "' - " + e.getMessage());
			}
	    	
	    	clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1547060117]");

		}	
		
		String m_sExportFileName = "MilestonesReport"
				+ "-" + clsDateAndTimeConversions.now("yyyy-MM-dd")
				+ "-" + clsDateAndTimeConversions.now("hh-mm-ss")
				+ ".CSV";
		
		//IF the user chose to select by Employee type, then get the employee types selected:
		ArrayList<String> arEmployeeTypes = new ArrayList<String>(0);
		if (bSelectByEmployeeType){
			Enumeration<String> paramNames = request.getParameterNames();
			while(paramNames.hasMoreElements()) {
				String s = paramNames.nextElement().toString();
				if (s.substring(0, MilestonesReportGenerate.PARAM_EMPLOYEE_TYPE_PREFIX.length()).compareTo(MilestonesReportGenerate.PARAM_EMPLOYEE_TYPE_PREFIX) == 0){
					arEmployeeTypes.add(s.substring(MilestonesReportGenerate.PARAM_EMPLOYEE_TYPE_PREFIX.length(), s.length()));
				}
			}
		}
		String sSelectionTypeDesc = "";
		if (bSelectByEmployeeType){
			sSelectionTypeDesc = "Using <B>THESE EMPLOYEE TYPES</B> to select employees: ";
			
			String SQL = "SELECT DISTINCT "
				+ " " + TCSTableEmployeeTypes.TableName + "." + TCSTableEmployeeTypes.sName
				+ ", " + TCSTableEmployeeTypes.TableName + "." + TCSTableEmployeeTypes.sDescription
				+ " FROM " + TCSTableEmployeeTypeLinks.TableName
				+ " LEFT JOIN " + TCSTableEmployeeTypes.TableName
				+ " ON " + TCSTableEmployeeTypeLinks.TableName + "." + TCSTableEmployeeTypeLinks.sEmployeeTypeID 
				+ " = " + TCSTableEmployeeTypes.TableName + "." + TCSTableEmployeeTypes.lid
				+ " WHERE ("
				;
				for (int i = 0; i < arEmployeeTypes.size(); i++){
					if (i == 0){
						SQL += "(" + TCSTableEmployeeTypeLinks.TableName + "." + TCSTableEmployeeTypeLinks.sEmployeeTypeID + " = '" + arEmployeeTypes.get(i) + "')";
					}else{
						SQL += " OR (" + TCSTableEmployeeTypeLinks.TableName + "." + TCSTableEmployeeTypeLinks.sEmployeeTypeID + " = '" + arEmployeeTypes.get(i) + "')";
					}
				}
				SQL += ")"
				+ " ORDER BY " + TCSTableEmployeeTypes.TableName + "." + TCSTableEmployeeTypes.sName
			;
			try {
				ResultSet rsTypes = clsDatabaseFunctions.openResultSet(SQL, getServletContext(), sDBID);
				int iRecordCount = 0;
				while (rsTypes.next()){
					if (iRecordCount == 0){
						sSelectionTypeDesc += rsTypes.getString(TCSTableEmployeeTypes.TableName + "." + TCSTableEmployeeTypes.sName);
					}else{
						sSelectionTypeDesc += ", " + rsTypes.getString(TCSTableEmployeeTypes.TableName + "." + TCSTableEmployeeTypes.sName);
					}
					
					iRecordCount++;
				}
				rsTypes.close();
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
			out.println("\"Milestone Report\"" + "\"");
		}else{
			String s = "<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.0 " 
				+ "Transitional//EN\">" 
				+ "<HTML>" 
				+ "<HEAD><TITLE>Milestones Report</TITLE></HEAD>\n<BR>" 
				+ "<BODY BGCOLOR=\"" + TimeCardUtilities.BACKGROUND_COLOR_FOR_ADMIN_SCREENS + "\">" 
				+ "\n<TABLE BORDER=0 WIDTH=90%  style = \" font-family: arial; \"  >\n"
				+ "  <TR>\n"
				+ "    <TD VALIGN=BOTTOM nowrap><FONT SIZE=4><B>Milestones Report</B></FONT></TD>\n" 

				+ "    <TD VALIGN=BOTTOM>"

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
					+ "    <TD nowrap><Font SIZE=3>"
					+ "<A HREF=\"" + TCWebContextParameters.getURLLinkBase(getServletContext()) + "TimeCardSystem.AdminMain\">Return to main menu</A>"
					+ "</FONT></TD>\n"
					+ "  </TR>\n"
					;
				}
			} catch (Exception e) {
				out.println("<BR><BR><B><FONT COLOR=RED>Error [1487601788] checking permissions - " + e.getMessage() + ".");
			}
			s += "<TR><TD COLSPAN=2 ALIGN=\"CENTER\"><BR>" + sSelectionTypeDesc + "</TD></TR>";
			s += "</TABLE>\n";
			out.println(s);
		}
		
		//Make sure that the user made a valid choice:
		if (bSelectByEmployeeType && arEmployeeTypes.size() == 0){
			out.println("<BR><BR><B>Error [14852849069] - You chose to list by employee type, but you didn't pick any employee type.</B><BR>");
			out.println("</BODY></HTML>");
			return;
		}
		
		String sSQL = "SELECT * FROM " + Employees.TableName;
			if (bSelectByEmployeeType) {
				sSQL+= " LEFT JOIN " + TCSTableEmployeeTypeLinks.TableName + " ON " + Employees.TableName + "." + Employees.sEmployeeID + "=" + TCSTableEmployeeTypeLinks.TableName + "." + TCSTableEmployeeTypeLinks.sEmployeeID
					 + " LEFT JOIN " + TCSTableEmployeeTypes.TableName + " ON " + TCSTableEmployeeTypeLinks.TableName + "." + TCSTableEmployeeTypeLinks.sEmployeeTypeID + "=" + TCSTableEmployeeTypes.TableName + "." + TCSTableEmployeeTypes.lid;
			}
		sSQL += " WHERE ("
				+ "(" + Employees.TableName + "." + Employees.iActive + " = 1)"
			;
		if (bSelectByEmployeeType) {
			sSQL += " AND (";
			for (int i=0;i<arEmployeeTypes.size();i++){
				if (i == 0){
					sSQL = sSQL + "(" + TCSTableEmployeeTypeLinks.sEmployeeTypeID + " = '" + arEmployeeTypes.get(i).toString() + "')";
				}else{
					sSQL = sSQL + " OR (" + TCSTableEmployeeTypeLinks.sEmployeeTypeID + " = '" + arEmployeeTypes.get(i).toString() + "')";
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
			+ " GROUP BY " + Employees.TableName + "." + Employees.sEmployeeID
			+ " ORDER BY " + Employees.TableName + "." + Employees.sEmployeeLastName + ", " + Employees.TableName + "." + Employees.sEmployeeFirstName
			;
			
		if (!bExportToFileWasSelected){
			out.println(TimeCardUtilities.getJQueryIncludeString());
			out.println(getStyles());
			out.println(sCommandScripts());
			
			out.println("<FORM NAME=\"MAINFORM\""
			+ " ACTION=\"" + TimeCardUtilities.getURLLinkBase(getServletContext()) + "TimeCardSystem.MilestonesReportGenerate?" + request.getQueryString()
			+ "\" METHOD=\"POST\">");
			out.println("\n<TABLE ID=\"MAINTABLE\" BORDER=0 WIDTH=100% cellpadding = \"6\" style = \" font-family: arial; \" >\n");
		}
	//	int iCurrentEmployeeType = -1;

		try {
			ResultSet rsEmployeeList = clsDatabaseFunctions.openResultSet(sSQL, getServletContext(), sDBID);
			while (rsEmployeeList.next()){
				Employee employee = new Employee(
					rsEmployeeList.getString(Employees.sEmployeeID),
					rsEmployeeList.getString(Employees.sEmployeeFirstName),
					rsEmployeeList.getString(Employees.sEmployeeLastName), 
					sDBID,
					getServletContext()
				);
				employee.loadMilestoneEntries(arEmployeeTypes);
				
				//If the employee has entries print them
				if(employee.getM_arrMilestoneEntries().size() > 0){
					out.println(employee.printEmployeeMilestones( arEmployeeTypes, bAllowClearing, false));	
				}
			}			
		} catch (Exception ex) {
			// handle any errors
			out.println("<BR><BR>Error [14725191340] - " + ex.getMessage() + "<BR>");
		}
		
		out.println("</FORM></TABLE>\n");
		
		if (!bExportToFileWasSelected){
			//printRules(out);
		}

		out.flush();
		out.close();

		out.println("</BODY></HTML>");
		
		return;
	} 
	public void doGet(HttpServletRequest request,
			HttpServletResponse response)
					throws ServletException, IOException {

		doPost(request, response);
	}
	

	public static class Employee extends Object {
		
		private String m_sEmployeeID;
		private String m_sEmployeeFirstName;
		private String m_sEmployeeLastName;
		private ArrayList<MilestoneEntry> m_arrMilestoneEntries;
		private String m_sDBID;
		private ServletContext m_ServletContext;
		
		public Employee(
				String sEmployeeID,
				String sEmployeeFistName,
				String sEmployeeLastName,
				String sDatabaseID,
				ServletContext ServletContext
				) throws Exception {

			m_sEmployeeID = sEmployeeID;
			m_sEmployeeFirstName = sEmployeeFistName;
			m_sEmployeeLastName = sEmployeeLastName;
			m_sDBID = sDatabaseID;
			m_ServletContext = ServletContext;
			setM_arrMilestoneEntries(new ArrayList<MilestoneEntry>(0));
			}	

		
		public String printEmployeeMilestones( ArrayList<String> arrEmployeeTypes, boolean bAllowClearing, boolean bCreateNestedTables) throws Exception{
			//Display employees first and last name
			String s = "";
			if(bCreateNestedTables) {
				s += "<table id=\"" + m_sEmployeeID + "\" class=\"employeemilestones\">";
			}
			
			s += "<TR><TD COLSPAN=" + Integer.toString(TABLE_COLUMN_SPAN) + "></TD></TR>";
			s += "<TR style=\"background-color:" + SMMasterStyleSheetDefinitions.BACKGROUND_GREY + ";\">";	
			s += "<TD COLSPAN=" + Integer.toString(TABLE_COLUMN_SPAN) + "><B>" + m_sEmployeeFirstName + " " + m_sEmployeeLastName + "</B></TD>\n";
			s += "</TR>\n\n";
			
			String sCurrentEmployeeType = "";
			String sLastEmployeeType = "";
			String sRowColor = "";
			boolean bIsComplete = false;
			//Loop through all the milestone entries.
			for (int i = 0; i < getM_arrMilestoneEntries().size(); i++){	
				bIsComplete = getM_arrMilestoneEntries().get(i).m_datDateCompleted.compareToIgnoreCase(INCOMPLETE_MILESTONE_TEXT) != 0;
				sCurrentEmployeeType = getM_arrMilestoneEntries().get(i).m_Milestone.get_sEmployeeTypeID();
				//Change employee type heading when type changes
				if(sLastEmployeeType.compareToIgnoreCase(sCurrentEmployeeType) != 0){
					TCEmployeeType employeeType = new TCEmployeeType();
					employeeType.set_slid(sCurrentEmployeeType);
					employeeType.load(m_sDBID, m_ServletContext, "");
					s += "<TR style=\"background-color:black;color:white;\">";
					s += "<TD COLSPAN=" + Integer.toString(TABLE_COLUMN_SPAN) + ">&nbsp;&nbsp;&nbsp;"
					  + "<I>" + employeeType.get_sName() + " - " + employeeType.get_sDescription() +"</I>"
					  + "</TD>\n";
					s += "</TR>\n\n";
				}
				
				if(bIsComplete){
					sRowColor = SMMasterStyleSheetDefinitions.BACKGROUND_LIGHT_GREEN_ALT;
				}else{
					sRowColor = SMMasterStyleSheetDefinitions.BACKGROUND_BLUE;
				}
				//Display every Milestone for this employee
				s += "<TR class=\"border_top\" style=\"background-color:" + sRowColor+ ";\">";
				
				s += "<TD WIDTH=\"10%\">" + getM_arrMilestoneEntries().get(i).m_datDateCompleted + "</TD>\n";
				s += "<TD WIDTH=\"70%\">" + getM_arrMilestoneEntries().get(i).m_Milestone.get_sName() + " - "
				+ getM_arrMilestoneEntries().get(i).m_Milestone.get_sDescription();
				if( getM_arrMilestoneEntries().get(i).m_mComment.compareToIgnoreCase("") != 0){
					s += "<BR>&nbsp;&nbsp;&nbsp;<small><FONT COLOR=\"RED\"> -" + getM_arrMilestoneEntries().get(i).m_mComment + "</FONT></small>";	
				}
				s += "</TD>\n";
				if(bIsComplete){
					s += "<TD WIDTH=\"20%\">" + "" + getM_arrMilestoneEntries().get(i).m_sRecordedByFirstName + " " + getM_arrMilestoneEntries().get(i).m_sRecordedByLastName 
							+ " on " + getM_arrMilestoneEntries().get(i).m_datEntryDate + "\n";
					if(bAllowClearing) {
						s += "<br><button type=\"submit\" name=\"" + PARAM_CLEAR_MILESTONE + "\" value=\"" + getM_arrMilestoneEntries().get(i).m_entryID + "\"> Delete </button>";
					}
				}else{
					s += "<TD WIDTH=\"20%\">" + "N/A"+ "\n";
				}
				
				s += "</TD></TR>\n\n";	
				
				sLastEmployeeType = sCurrentEmployeeType;
			}
			if(bCreateNestedTables) {
				s += "</table>";
			}
			return s;
		}
			

		public void loadMilestoneEntries(ArrayList<String> arrEmployeeTypes) throws Exception{
			//This query gets ALL the Milestone names and descriptions for each employee type/employee selected.
			//The employee milestones fields will contain NULL values if the milestone as not been completed, 
			//otherwise the MOST RECENT (determined by auto incrementing MilstoneID) employee milestones record will be used.
			String sSQL = "SELECT " + TCSTableMilestones.TableName + "." + TCSTableMilestones.lid
					+ ", " + TCSTableMilestones.TableName + "." + TCSTableMilestones.sName
					+ ", " + TCSTableMilestones.TableName + "." + TCSTableMilestones.sDescription
					+ ", " + TCSTableMilestones.TableName + "." + TCSTableMilestones.sEmployeeTypeID
					+ ", " + "EMPLOYEESMILESTONES" + "." + TCSTableEmployeeMilestones.lid + " AS `EmployeeMilestones.lid`"
					+ ", " + "EMPLOYEESMILESTONES" + "." + TCSTableEmployeeMilestones.datDateCompleted + " AS `EmployeeMilestones.datDateCompleted`"
					+ ", " + "EMPLOYEESMILESTONES" + "." + TCSTableEmployeeMilestones.datEntryDate + " AS `EmployeeMilestones.datEntryDate`"
					+ ", " + "EMPLOYEESMILESTONES" + "." + TCSTableEmployeeMilestones.sRecordedByID + " AS `EmployeeMilestones.sRecordedByID`"
					+ ", " + "EMPLOYEESMILESTONES" + "." + TCSTableEmployeeMilestones.sRecordedByFirstName + " AS `EmployeeMilestones.sRecordedByFirstName`"
					+ ", " + "EMPLOYEESMILESTONES" + "." + TCSTableEmployeeMilestones.sRecordedByLastName + " AS `EmployeeMilestones.sRecordedByLastName`"
					+ ", " + "EMPLOYEESMILESTONES" + "." + TCSTableEmployeeMilestones.mComment + " AS `EmployeeMilestones.mComment`"
					+ " FROM " + TCSTableMilestones.TableName 
					+ " LEFT JOIN " + TCSTableEmployeeTypeLinks.TableName 
					+ " ON " + TCSTableMilestones.TableName + "." + TCSTableMilestones.sEmployeeTypeID 
					+ " = " + TCSTableEmployeeTypeLinks.TableName + "." + TCSTableEmployeeTypeLinks.sEmployeeTypeID
				
					+ " LEFT JOIN "
						+ " (SELECT " + TCSTableEmployeeMilestones.TableName + "." + TCSTableEmployeeMilestones.sMilestoneID
						+ ", " + TCSTableEmployeeMilestones.TableName + "." +TCSTableEmployeeMilestones.lid
						+ ", " + TCSTableEmployeeMilestones.TableName + "." +TCSTableEmployeeMilestones.sMilestoneName
						+ ", " + TCSTableEmployeeMilestones.TableName + "." +TCSTableEmployeeMilestones.datDateCompleted 
						+ ", " + TCSTableEmployeeMilestones.TableName + "." +TCSTableEmployeeMilestones.sEmployeeTypeID  
						+ ", " + TCSTableEmployeeMilestones.TableName + "." +TCSTableEmployeeMilestones.datEntryDate 
						+ ", " + TCSTableEmployeeMilestones.TableName + "." +TCSTableEmployeeMilestones.sRecordedByID
						+ ", " + TCSTableEmployeeMilestones.TableName + "." +TCSTableEmployeeMilestones.sRecordedByFirstName
						+ ", " + TCSTableEmployeeMilestones.TableName + "." +TCSTableEmployeeMilestones.sRecordedByLastName
						+ ", " + TCSTableEmployeeMilestones.TableName + "." +TCSTableEmployeeMilestones.mComment
						+ " FROM " + TCSTableEmployeeMilestones.TableName
						 	+ " LEFT JOIN "
						 			+ "( SELECT "  +TCSTableEmployeeMilestones.lid
						 			+   ", MAX(" +TCSTableEmployeeMilestones.lid + ") AS ID"
						 				+ " FROM " + TCSTableEmployeeMilestones.TableName
						 				+ " WHERE (" + TCSTableEmployeeMilestones.sEmployeeID + " = '" + m_sEmployeeID + "')"
						 				+ " GROUP BY " + TCSTableEmployeeMilestones.sMilestoneID
						 				+ ") AS MAXID"
						 				+ " ON " + TCSTableEmployeeMilestones.TableName + "." +TCSTableEmployeeMilestones.lid
						 				+ " = " + " MAXID.ID"
						 + " WHERE (" + TCSTableEmployeeMilestones.TableName + "." + TCSTableEmployeeMilestones.sEmployeeID + " = '" + m_sEmployeeID + "')"
						+ " GROUP BY " + TCSTableEmployeeMilestones.TableName + "." +TCSTableEmployeeMilestones.sMilestoneID
						+ ") AS EMPLOYEESMILESTONES"
				
				+ " ON " + TCSTableMilestones.TableName + "." + TCSTableMilestones.lid+ " = " + "EMPLOYEESMILESTONES" + "." + TCSTableEmployeeMilestones.sMilestoneID
				
				+ " WHERE (" 
					+ "(" + TCSTableEmployeeTypeLinks.TableName + "." + TCSTableEmployeeTypeLinks.sEmployeeID + " = '" + m_sEmployeeID + "')" ;
					//Add Employee types selected from selection screen.
				if(arrEmployeeTypes.size() > 0){
					sSQL += " AND (";
					for(int i = 0; i < arrEmployeeTypes.size(); i++){
						if(i > 0){
							sSQL += " OR ";
						}
						sSQL += " (" + TCSTableMilestones.TableName + "." + TCSTableMilestones.sEmployeeTypeID + "='" + arrEmployeeTypes.get(i) + "')";
						}
					sSQL += ")";
					}
			sSQL += ")"  
				+ " ORDER BY " + TCSTableMilestones.TableName + "." + TCSTableMilestones.sEmployeeTypeID + ", " + TCSTableMilestones.TableName + "." + TCSTableMilestones.sName 
			; 

			try {
				ResultSet rsMilestoneRecords = clsDatabaseFunctions.openResultSet(sSQL, m_ServletContext, m_sDBID);
				getM_arrMilestoneEntries().clear();
				while (rsMilestoneRecords.next()){

					//Add a new Milestone entry for this employee
					getM_arrMilestoneEntries().add( new MilestoneEntry(
							rsMilestoneRecords.getString(TCSTableEmployeeMilestones.TableName + "." + TCSTableEmployeeMilestones.lid),
							rsMilestoneRecords.getString(TCSTableEmployeeMilestones.TableName + "." + TCSTableEmployeeMilestones.datDateCompleted),
							rsMilestoneRecords.getString(TCSTableEmployeeMilestones.TableName + "." + TCSTableEmployeeMilestones.datEntryDate),
							rsMilestoneRecords.getString(TCSTableEmployeeMilestones.TableName + "." + TCSTableEmployeeMilestones.sRecordedByID),
							rsMilestoneRecords.getString(TCSTableEmployeeMilestones.TableName + "." + TCSTableEmployeeMilestones.sRecordedByFirstName),
							rsMilestoneRecords.getString(TCSTableEmployeeMilestones.TableName + "." + TCSTableEmployeeMilestones.sRecordedByLastName),
							rsMilestoneRecords.getString(TCSTableMilestones.lid),
							rsMilestoneRecords.getString(TCSTableEmployeeMilestones.TableName + "." + TCSTableEmployeeMilestones.mComment)
							)
					);					
				}
				rsMilestoneRecords.close();
			} catch (Exception e) {
				throw new Exception("Error [8674795154] - reading milestone entries with SQL: " + sSQL + " - " + e.getMessage());
			}
		}
		
		public ArrayList<MilestoneEntry> getM_arrMilestoneEntries() {
			return m_arrMilestoneEntries;
		}

		public void setM_arrMilestoneEntries(ArrayList<MilestoneEntry> m_arrMilestoneEntries) {
			this.m_arrMilestoneEntries = m_arrMilestoneEntries;
		}

		private class MilestoneEntry {
			public String m_entryID;
			public String m_datDateCompleted;
			public String m_datEntryDate;
			public String m_sRecordedByFirstName;
			public String m_sRecordedByLastName;
			public String m_mComment;
			public TCMilestones m_Milestone;
			
			public MilestoneEntry(
					String entryID,
					String datDateCompleted,
					String datEntryDate,
					String sRecordedByID,
					String sRecordedByFirstName,
					String sRecordedByLastName,
					String sMilestoneID,
					String mComment
					) throws Exception {
				//If this milestone has not been completed fill with empty values
				if(datDateCompleted == null){
					 m_entryID = "";
					 m_datDateCompleted = INCOMPLETE_MILESTONE_TEXT;
					 m_datEntryDate = "N/A";
					 m_sRecordedByFirstName = "N/A";
					 m_sRecordedByLastName = "";
					 m_mComment = "";
				}else{
					m_entryID = entryID;
					 m_datDateCompleted = TimeCardUtilities.resultsetDateStringToString(datDateCompleted);
					 m_datEntryDate = TimeCardUtilities.resultsetDateStringToString(datEntryDate);
					 m_sRecordedByFirstName = sRecordedByFirstName;
					 m_sRecordedByLastName = sRecordedByLastName;
					 m_mComment = mComment;
				}
					 m_Milestone = new TCMilestones();
					 if(sMilestoneID == null) {
						 sMilestoneID = "-1";
					 }
					 m_Milestone.set_slid(sMilestoneID);
					 try {
						 m_Milestone.load(m_sDBID, m_ServletContext, "");
					 } catch (Exception e) {
						 throw new Exception("Error loading milestone entry: " + e.getMessage());
					 }
			}
		}		
}
	
	private String getStyles() {
		return "<style>\n" + 
				"\n" + 
				"  \n" + 
				"  tr.border_top td {border-bottom:1pt solid black;}\n" +
			    "  #MAINTABLE {border-collapse: collapse;}\n" + 
				"  \n" +
				"</style>";
	
	}
	private String sCommandScripts() {
			String s = "";
			
			s += "<NOSCRIPT>\n"
				+ "    <font color=red>\n"
				+ "    <H3>This page requires that JavaScript be enabled to function properly</H3>\n"
				+ "    </font>\n"
				+ "</NOSCRIPT>\n"
			;
 
			s += "<script type='text/javascript'>\n";
			
			//Enter javascript/jquery here

			s += "</script>\n";
			
			return s;
		}
}
