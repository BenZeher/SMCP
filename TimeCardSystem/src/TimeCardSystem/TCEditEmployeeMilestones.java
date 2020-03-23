package TimeCardSystem;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsDateAndTimeConversions;
import ServletUtilities.clsManageRequestParameters;
import ServletUtilities.clsServletUtilities;
import TCSDataDefinition.Employees;
import TCSDataDefinition.TCSTableEmployeeMilestones;
import TCSDataDefinition.TCSTableEmployeeTypeAccess;
import TCSDataDefinition.TCSTableEmployeeTypeLinks;
import TCSDataDefinition.TCSTableMilestones;
import TCSDataDefinition.TCSTablecompanyprofile;
import TimeCardSystem.MilestonesReportGenerate.Employee;


public class TCEditEmployeeMilestones extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final String MAIN_FORM_NAME = "MilestoneForm";
	private static final String CALLED_CLASS = "TimeCardSystem.TCEditEmployeeMilestones";
	
	public static final String SUBMIT_EDIT_BUTTON_NAME = "SUBMITEDIT";
	public static final String SUBMIT_EDIT_BUTTON_LABEL = "Add Completed Milestone";

	public void doPost(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
		
		response.setContentType("text/html");
		PrintWriter out = response.getWriter();
	    
		//Get a session for this user.
	    HttpSession CurrentSession = request.getSession(true);

	  	//session will expire in one hour.
	  	CurrentSession.setMaxInactiveInterval(TimeCardUtilities.MAX_SESSION_INTERVAL);

	  	//check for valid database name
	  	try {
	  		if (request.getParameter(TimeCardUtilities.REQUEST_PARAMETER_DB) == null){
	  			//if there is no conf name passed in, check session for stored passwd
	  			if ((String)CurrentSession.getAttribute(TimeCardUtilities.SESSION_ATTRIBUTE_DB) == null){
	  				//there is no conf name, go back to login screen
	  				CurrentSession.invalidate();
	  				out.println("<BR>The current session is void. Please login again.");
	  				return;
	  			}else{
	  				//a conf name is already stored in session, do nothing.
	  			}
	  		}else{
	  			//store this conf name into session. overwrite any old one.
	  			CurrentSession.setAttribute(TimeCardUtilities.SESSION_ATTRIBUTE_DB, request.getParameter(TimeCardUtilities.REQUEST_PARAMETER_DB));
	  		}
	  	} catch (Exception e2) {
	  		out.println("<BR>Error with session attribute - " + e2.getMessage());
	  		return;
	  	}
	  		
	  	//Get the company information:
	  	String sSQL = "SELECT * FROM " + TCSTablecompanyprofile.TableName;
	  	String sDBID;
	  	try {
	  		sDBID = (String)CurrentSession.getAttribute(TimeCardUtilities.SESSION_ATTRIBUTE_DB);
	  	} catch (Exception e1) {
	  		sDBID = "";
	  	}
	  	if (sDBID == null){
	  		sDBID = "";
	  	}
	  	try {
	  		ResultSet rs = clsDatabaseFunctions.openResultSet(
	  				sSQL, 
	  				getServletContext(), 
	  				sDBID, 
	  				"MySQL",
	  				this.toString() + ".reading company name"
	  				);
	  		if (rs.next()){
	  			CurrentSession.setAttribute(TimeCardUtilities.SESSION_ATTRIBUTE_COMPANYNAME, rs.getString(TCSTablecompanyprofile.sCompanyName));
	  			rs.close();
	  		}else{
	  			out.println("<html><body><BR>Could not read company name.</body></html>");
	  			rs.close();
	  			return;
	  		}
	  	} catch (SQLException e) {
	  		out.println("<html><body><BR>Error reading company name: " + e.getMessage() + "</body></html>");
	  		return;
	  	}
	  
	  	//Valid the pin number and user
		String sPinNumber = clsManageRequestParameters.get_Request_Parameter(TimeCardUtilities.REQUEST_PARAMETER_PINNUMBER, request);
	    String sUserID = "";
		sSQL = TimeCardSQLs.Get_Employee_Info_By_Pin_SQL(sPinNumber);
		try {
			ResultSet rs = clsDatabaseFunctions.openResultSet(sSQL, getServletContext(), (String)CurrentSession.getAttribute(TimeCardUtilities.SESSION_ATTRIBUTE_DB));
			if(rs.next() && sPinNumber.compareToIgnoreCase("") != 0){
				if (rs.getInt(Employees.TableName + "." + Employees.iActive) == 0){
					throw new Exception("Error [2019214162329] " + "This user is marked as inactive - contact an administrator for help.");
				}
			    sUserID = rs.getString(Employees.sEmployeeID);	
			}else{
				out.println("The pin code is not valid. Please try again.");	
				out.println("<META http-equiv='Refresh' content='2;URL=" 
					+ TCWebContextParameters.getURLLinkBase(getServletContext())
					+ MainLogin.CLASS_NAME
					+ "?db=" + (String)CurrentSession.getAttribute(TimeCardUtilities.SESSION_ATTRIBUTE_DB)
					+ "&" + MainLogin.MILESTONES_LOGIN_PARAM + "=Y" 
					+ "'>");
				return;
				}
		} catch (Exception e1) {
			out.println("Failed to log in - " + e1.getMessage());	
			out.println("<META http-equiv='Refresh' content='2;URL=" 
				+ TCWebContextParameters.getURLLinkBase(getServletContext())
				+ MainLogin.CLASS_NAME
				+ "?db=" + (String)CurrentSession.getAttribute(TimeCardUtilities.SESSION_ATTRIBUTE_DB)
				+ "&" + MainLogin.MILESTONES_LOGIN_PARAM + "=Y" 
				+ "'>");
			return;
		}
		
	    String sStatus = "";
		String sWarning = "";
			
		//If this class 'SAVE' button has just been clicked save the employee milestone record from the request.
		if (clsManageRequestParameters.get_Request_Parameter(SUBMIT_EDIT_BUTTON_NAME, request).compareToIgnoreCase(SUBMIT_EDIT_BUTTON_LABEL) == 0){
			try {
				SaveEmployeeMilestone(request, getServletContext(), sDBID, sUserID);
				sStatus = "Employee milestone has been saved.";
			} catch (Exception e) {
				sWarning = e.getMessage();
			}
		}
		
		//Print page headings
	    String sTitle = "Edit Employee Milestones";
	    String sSubtitle = "";
	    out.println(TimeCardUtilities.TCTitleSubBGColorWithFont(sTitle, sSubtitle, TimeCardUtilities.BACKGROUND_COLOR_FOR_ADMIN_SCREENS, TimeCardUtilities.BASE_FONT_FAMILY));
	    out.println(clsServletUtilities.getDatePickerIncludeString(getServletContext()));
	    out.println(clsServletUtilities.getJQueryIncludeString());
	    out.println(getCSSStyles());
	    
    	sWarning += clsManageRequestParameters.get_Request_Parameter("Warning", request);
    	if (sWarning.compareToIgnoreCase("") != 0){
    		out.println("<BR><B><FONT COLOR=RED>Error editing " + "employees milestone" + " - " + sWarning + ".</FONT></B><BR>");
    	}
		out.println("<BR>");
		sStatus += clsManageRequestParameters.get_Request_Parameter("Status", request);
		if (sStatus.compareToIgnoreCase("") != 0) {
			out.println("<B><FONT COLOR=YELLOW>" + sStatus + "</FONT></B><BR>");
		}
		
		
		//Print screen
		if (sPinNumber.compareToIgnoreCase("") != 0) {
			//Print milestone edit form
			try {
				out.println(printMilestoneEditForm(sPinNumber, sUserID, sDBID, request));
			} catch (Exception ex) {
				out.println("<html><body>Error in EditEmployeeMilestone class - " + ex.getMessage() + "</body></html>");
			}
			
			//Print selected employees milestone
			try {
				out.println("\n<TABLE ID=\"MAINTABLE\" BORDER=0 WIDTH=100% cellpadding = \"6\" style = \" display:block; font-family: arial; \" >\n");
				out.println(printEmployeesMilestones(sPinNumber, sUserID, sDBID));
				out.println("\n</TABLE>\n");
			} catch (Exception ex) {
				out.println("<html><body>Error in EditEmployeeMilestone class - " + ex.getMessage() + "</body></html>");
			}
		} else {
			// Redirect back to the main login
			out.println("The pin code is not valid. Please try again.");
			out.println("<META http-equiv='Refresh' content='2;URL="
					+ TCWebContextParameters.getURLLinkBase(getServletContext()) + MainLogin.CLASS_NAME
					+ "?db=" + (String) CurrentSession.getAttribute(TimeCardUtilities.SESSION_ATTRIBUTE_DB) + "&"
					+ MainLogin.MILESTONES_LOGIN_PARAM + "=Y" + "'>");
			return;
		}
		
		try {
			out.println(sGetJavascript());
		} catch (SQLException e) {
			out.println("[1518118355] Javascript error. ");
		}

	}




	private String printMilestoneEditForm(String sPinNumber, String sUserID, String sDBID, HttpServletRequest req) throws Exception {
		String s = "";
		s += "<FORM ID='\"" + MAIN_FORM_NAME + "'\" NAME=\"" + MAIN_FORM_NAME + "\" ACTION=\""
				+ clsServletUtilities.getURLLinkBase(getServletContext()) + CALLED_CLASS + "\"" + " METHOD=\"POST\">";

		s += "<INPUT TYPE=HIDDEN NAME=\"" + "CallingClass" + "\" VALUE=\"" + this.getClass().getName() + "\">";
		s += "<INPUT TYPE=HIDDEN NAME=\"" + TimeCardUtilities.REQUEST_PARAMETER_PINNUMBER + "\" VALUE=\"" + sPinNumber
				+ "\">";

		s += "<TABLE BORDER=1>";

		// Employee ID
		ArrayList<String> sValues = new ArrayList<String>();
		ArrayList<String> sDescriptions = new ArrayList<String>();
		sValues.add("0");
		sDescriptions.add("***SELECT EMPLOYEE ***");
		String sSQL = "SELECT " 
				+ Employees.TableName + "." + Employees.sEmployeeID 
				+ "," + Employees.TableName + "." + Employees.sEmployeeFirstName 
				+ "," + Employees.TableName + "." + Employees.sEmployeeLastName
				+ " FROM " + Employees.TableName 
				+ " LEFT JOIN " + TCSTableEmployeeTypeLinks.TableName 
				+ " ON " + TCSTableEmployeeTypeLinks.TableName + "." + TCSTableEmployeeTypeLinks.sEmployeeID 
				+ " = " + Employees.TableName + "." + Employees.sEmployeeID 
				+ " LEFT JOIN " + TCSTableEmployeeTypeAccess.TableName 
				+ " ON " + TCSTableEmployeeTypeAccess.TableName + "." + TCSTableEmployeeTypeAccess.sEmployeeTypeID 
				+ " = " + TCSTableEmployeeTypeLinks.TableName + "." + TCSTableEmployeeTypeLinks.sEmployeeTypeID 
				+ " WHERE( " + TCSTableEmployeeTypeAccess.TableName + "." + TCSTableEmployeeTypeAccess.sEmployeeID + " = '" + sUserID + "')" 
				+ " GROUP BY " + Employees.TableName + "." + Employees.sEmployeeID;
		ResultSet rsEmployees;
		try {
			rsEmployees = clsDatabaseFunctions.openResultSet(sSQL, getServletContext(), sDBID);
			while (rsEmployees.next()) {
				sValues.add(rsEmployees.getString(Employees.TableName + "." + Employees.sEmployeeID));
				sDescriptions.add(rsEmployees.getString(Employees.TableName + "." + Employees.sEmployeeFirstName) + "  "
						+ rsEmployees.getString(Employees.TableName + "." + Employees.sEmployeeLastName));
			}
			rsEmployees.close();
		} catch (Exception e) {
			throw new Exception("Error getting Employee IDs with SQL: " + sSQL + " Message: " + e.getMessage());
		}
		//Get the default value of the employee already selected if coming back to this screen.
		String sDefaultValue = clsManageRequestParameters.get_Request_Parameter(TCSTableEmployeeMilestones.sEmployeeID, req);
		if(sDefaultValue.compareToIgnoreCase("") == 0) {
			sDefaultValue = sValues.get(0);
		}
		s += TimeCardUtilities.Create_Edit_Form_List_Row(TCSTableEmployeeMilestones.sEmployeeID, sValues, sDefaultValue,
				sDescriptions, "Employee:", "Select the employee this milestone is for", "displayMilestones(this)");

		// Milestone ID
		sValues.clear();
		sDescriptions.clear();
		sValues.add("0");
		sDescriptions.add("***SELECT MILESTONE***");

		sSQL = "SELECT " + TCSTableMilestones.lid 
				+ "," + TCSTableMilestones.sName + ","
				+ TCSTableMilestones.sDescription
				+ " FROM " + TCSTableMilestones.TableName 
				+ " LEFT JOIN " + TCSTableEmployeeTypeAccess.TableName 
				+ " ON " + TCSTableEmployeeTypeAccess.TableName + "."+ TCSTableEmployeeTypeAccess.sEmployeeTypeID 
				+ " = " + TCSTableMilestones.TableName + "." + TCSTableMilestones.sEmployeeTypeID 
				+ " WHERE (" 
				+ TCSTableEmployeeTypeAccess.TableName + "." + TCSTableEmployeeTypeAccess.sEmployeeID + "='" + sUserID + "'"
						+ ")" 
				+ " ORDER BY "
				+ TCSTableMilestones.TableName + "." + TCSTableMilestones.sEmployeeTypeID 
				+ ", " + TCSTableMilestones.TableName + "." + TCSTableMilestones.sName;
		ResultSet rsMilestones;
		try {
			rsMilestones = clsDatabaseFunctions.openResultSet(sSQL, getServletContext(), sDBID);
			while (rsMilestones.next()) {
				sValues.add(rsMilestones.getString(TCSTableMilestones.lid));
				sDescriptions.add(rsMilestones.getString(TCSTableMilestones.sName));
			}
			rsMilestones.close();
		} catch (Exception e) {
			throw new Exception("Error getting Milestones IDs: " + sSQL + " Message: " + e.getMessage());
		}

		s += TimeCardUtilities.Create_Edit_Form_List_Row(TCSTableEmployeeMilestones.sMilestoneID, sValues, "0",
				sDescriptions, "Milestone Completed:", "Select the milestone that was completed.", "");

		// Milestone comments
		s += TimeCardUtilities.Create_Edit_Form_MultilineText_Input_Row(TCSTableEmployeeMilestones.mComment, "",
				"Comments:",
				"Add any notes regarding this milestone. Comments can only be seen by employee and managment.", 3, 30,
				"");
		// Date Completed
		s += TimeCardUtilities.Create_Edit_Form_Date_Input_Row(TCSTableEmployeeMilestones.datDateCompleted,
				clsDateAndTimeConversions.nowStdMdyyyFormat(), "Date Completed:",
				"Enter date this  milestone was completed", getServletContext());
		s += "</TABLE>";

		//Submit Add New Miletone
		s += "<P><INPUT TYPE=SUBMIT NAME='" + SUBMIT_EDIT_BUTTON_NAME + "' VALUE='" + SUBMIT_EDIT_BUTTON_LABEL
				+ "' STYLE='height: 0.24in'>";

		s += "</FORM>";

		return s;
	}
	
	private String printEmployeesMilestones(String sPinNumber, String sUserID, String sDBID) throws Exception {
		String s = "";
		String sSQL = "";
		//Get all employee types current user has access to
		ArrayList<String> arEmployeeTypes = new ArrayList<String>(0);
		sSQL = "SELECT DISTINCT " + TCSTableEmployeeTypeAccess.sEmployeeTypeID + " FROM "
				+ TCSTableEmployeeTypeAccess.TableName + " WHERE (" + TCSTableEmployeeTypeAccess.sEmployeeID + "='"+ sUserID + "')";
		try {
			ResultSet rsEmployeesTypeAccess = clsDatabaseFunctions.openResultSet(sSQL, getServletContext(), sDBID);
			while (rsEmployeesTypeAccess.next()) {
				arEmployeeTypes.add(rsEmployeesTypeAccess.getString(TCSTableEmployeeTypeAccess.sEmployeeTypeID));
			}
			rsEmployeesTypeAccess.close();
		} catch (Exception ex) {
			throw new Exception("Error [14725191339] - " + ex.getMessage());
		}
		
		//print all employee milestones
		sSQL = "SELECT  DISTINCT "
				+ Employees.TableName + "." + Employees.sEmployeeID
				+ ", " + Employees.TableName + "." + Employees.sEmployeeFirstName
				+ ", " + Employees.TableName + "." + Employees.sEmployeeLastName
				+ " FROM " + TCSTableEmployeeTypeLinks.TableName
				+ " LEFT JOIN " + Employees.TableName 
				+ " ON " + Employees.TableName + "." + Employees.sEmployeeID
				+ "=" + TCSTableEmployeeTypeLinks.TableName + "." + TCSTableEmployeeTypeLinks.sEmployeeID
				+ " WHERE ("
				;
		for (int i = 0; i < arEmployeeTypes.size(); i++) {
			if(i > 0) {
				sSQL += " OR ";
			}
			sSQL += " (" + TCSTableEmployeeTypeLinks.TableName + "." + TCSTableEmployeeTypeLinks.sEmployeeTypeID + "='"+ arEmployeeTypes.get(i) + "')";
		}
		sSQL += ")";
			
		try {
			ResultSet rsEmployeeInfo = clsDatabaseFunctions.openResultSet(sSQL, getServletContext(), sDBID);
			while (rsEmployeeInfo.next()) {
				MilestonesReportGenerate.Employee employee = new Employee(
						rsEmployeeInfo.getString(Employees.sEmployeeID),
						rsEmployeeInfo.getString(Employees.sEmployeeFirstName),
						rsEmployeeInfo.getString(Employees.sEmployeeLastName),
						sDBID,
						getServletContext());
				
				employee.loadMilestoneEntries(arEmployeeTypes);

				// If the employee has entries print them
				if (employee.getM_arrMilestoneEntries().size() > 0) {
					s += employee.printEmployeeMilestones(arEmployeeTypes, false, true);
				}
			}
			rsEmployeeInfo.close();
		} catch (Exception ex) {
			throw new Exception("Error [14725291349] - " + ex.getMessage());
		}
		return s;
	}
	
	
	private void SaveEmployeeMilestone(HttpServletRequest request, ServletContext context, String sDBID, String sUserName) throws Exception {
		//Define all variables to be saved
		String sEmployeeTypeID = ""; 
		String sEmployeeID = clsManageRequestParameters.get_Request_Parameter(TCSTableEmployeeMilestones.sEmployeeID, request);;
		String sEmployeeFirstName = ""; 
		String sEmployeeMiddleName = "";
		String sEmployeeLastName = "";
		String sMilestoneID = clsManageRequestParameters.get_Request_Parameter(TCSTableEmployeeMilestones.sMilestoneID, request);
		String sMilestoneName = "";
		String sMilestoneDescription = "";
		String datDateCompleted = clsManageRequestParameters.get_Request_Parameter(TCSTableEmployeeMilestones.datDateCompleted, request);
		String datEntryDate = clsDateAndTimeConversions.nowSqlFormat();
		String sRecordedByID = sUserName;
		String sRecordedByFirstName = "";
		String sRecordedByMiddleName = "";
		String sRecordedByLastName = "";
		String mComment = clsManageRequestParameters.get_Request_Parameter(TCSTableEmployeeMilestones.mComment, request);
		
		//Validate entries
		String sInvalidEntryMsg = "";
		if(sEmployeeID.compareToIgnoreCase("0") == 0 || sEmployeeID.compareToIgnoreCase("") == 0){
			sInvalidEntryMsg += "No employee was selected. ";
		}
		if(sMilestoneID.compareToIgnoreCase("0") == 0 || sMilestoneID.compareToIgnoreCase("") == 0){
			sInvalidEntryMsg += "No milestone was selected. ";
		}
		datDateCompleted = TimeCardUtilities.stdDateStringToSQLDateString(datDateCompleted);
		SimpleDateFormat sdf = new SimpleDateFormat("YYYY-MM-dd");
		try {
			sdf.parse(datDateCompleted);
		} catch (Exception e) {
			sInvalidEntryMsg += "Date completed is invalid. ";
		}
	
		if(sInvalidEntryMsg.compareToIgnoreCase("") != 0){
			throw new Exception(sInvalidEntryMsg);
		}
		
	   	//Get connection
		Connection conn;
		try {
			conn = clsDatabaseFunctions.getConnectionWithException(
				context, 
				sDBID, 
				"MySQL", 
				this.toString() + ":save - user: " + sUserName
			);
		} catch (Exception e1) {
			throw new Exception("Error [1486653017] - could not get connection to save.");
		}
		
		//Get the employee full name for this milestone.
		String sSQL = TimeCardSQLs.Get_Employee_Info_SQL(sEmployeeID);
		ResultSet rsEmployee;	
		try {
			rsEmployee = clsDatabaseFunctions.openResultSet(sSQL, context, sDBID);
		}catch (Exception e){
			throw new Exception(e.getMessage());
		}	
		
		if(rsEmployee.next()){
			sEmployeeFirstName = rsEmployee.getString(Employees.sEmployeeFirstName);
			sEmployeeMiddleName = rsEmployee.getString(Employees.sEmployeeMiddleName);
			sEmployeeLastName = rsEmployee.getString(Employees.sEmployeeLastName);
		}
		rsEmployee.close();
		
		//Get the users full name for this milestone.
		 sSQL = TimeCardSQLs.Get_Employee_Info_SQL(sRecordedByID) 
				;
		ResultSet rsRecordedByEmployee;
		
		try {
			rsRecordedByEmployee = clsDatabaseFunctions.openResultSet(sSQL, context, sDBID);
		}catch (Exception e){
			throw new Exception(e.getMessage());
		}	
		
		if(rsRecordedByEmployee.next()){
			sRecordedByFirstName = rsRecordedByEmployee.getString(Employees.sEmployeeFirstName);
			sRecordedByMiddleName = rsRecordedByEmployee.getString(Employees.sEmployeeMiddleName);
			sRecordedByLastName = rsRecordedByEmployee.getString(Employees.sEmployeeLastName);
		}
		rsRecordedByEmployee.close();
		
		//Get Milestone information
		 sSQL = TimeCardSQLs.Get_Milestone_SQL(sMilestoneID) 
				;
		ResultSet rsMilestone;
		
		try {
			rsMilestone = clsDatabaseFunctions.openResultSet(sSQL, context, sDBID);
		}catch (Exception e){
			throw new Exception(e.getMessage());
		}	
		
		if(rsMilestone.next()){
			sMilestoneName = rsMilestone.getString(TCSTableMilestones.sName);
			sMilestoneDescription = rsMilestone.getString(TCSTableMilestones.sDescription);
			sEmployeeTypeID = rsMilestone.getString(TCSTableMilestones.sEmployeeTypeID);
		}
		rsMilestone.close();
		
		//Insert employee milestone record.
		sSQL = "INSERT INTO " + TCSTableEmployeeMilestones.TableName + "("
			+ TCSTableEmployeeMilestones.sEmployeeTypeID
			+ ", " + TCSTableEmployeeMilestones.sEmployeeID
			+ ", " + TCSTableEmployeeMilestones.sEmployeeFirstName
			+ ", " + TCSTableEmployeeMilestones.sEmployeeMiddleName
			+ ", " + TCSTableEmployeeMilestones.sEmployeeLastName //5
			+ ", " + TCSTableEmployeeMilestones.sMilestoneID
			+ ", " + TCSTableEmployeeMilestones.sMilestoneName
			+ ", " + TCSTableEmployeeMilestones.sMilestoneDescription
			+ ", " + TCSTableEmployeeMilestones.datDateCompleted
			+ ", " + TCSTableEmployeeMilestones.datEntryDate //10
			+ ", " + TCSTableEmployeeMilestones.sRecordedByID
			+ ", " + TCSTableEmployeeMilestones.sRecordedByFirstName
			+ ", " + TCSTableEmployeeMilestones.sRecordedByMiddleName
			+ ", " + TCSTableEmployeeMilestones.sRecordedByLastName
			+ ", " + TCSTableEmployeeMilestones.mComment //15
			+ ") VALUES ("
			+ "'" + sEmployeeTypeID + "'"
			+ ", '" + sEmployeeID + "'"
			+ ", '" + sEmployeeFirstName + "'"
			+ ", '" + sEmployeeMiddleName + "'"
			+ ", '" + sEmployeeLastName + "'" //5
			+ ", '" + sMilestoneID + "'"
			+ ", '" + sMilestoneName + "'"
			+ ", '" + sMilestoneDescription + "'"
			+ ", '" + datDateCompleted + "'"
			+ ", '" + datEntryDate + "'" //10
			+ ", '" + sRecordedByID + "'"
			+ ", '" + sRecordedByFirstName + "'"
			+ ", '" + sRecordedByMiddleName + "'"
			+ ", '" + sRecordedByLastName + "'"
			+ ", '" + mComment + "'" //15
			+ ")"
			;
		//Start a transaction:
		try {
			clsDatabaseFunctions.start_data_transaction(conn);
		} catch (Exception e1) {
			throw new Exception("Error [156741282] starting data transaction - " + e1.getMessage());
		}
				
		try {
			 Statement stmt = conn.createStatement();
			 stmt.executeUpdate(sSQL);
			}catch (SQLException e){
				clsDatabaseFunctions.rollback_data_transaction(conn);
			 	clsDatabaseFunctions.freeConnection(context, conn, "[1547060120]");
			 	throw new Exception("Error [4489536150] saving " + "Employee Milestone" + " record with SQL: '" + sSQL + "' - " + e.getMessage());
			 }
				
		clsDatabaseFunctions.commit_data_transaction(conn);
		clsDatabaseFunctions.freeConnection(context, conn, "[1547060121]");
	}

	private String sGetJavascript() throws SQLException{
			String s = "";
			s += "<style>.employeemilestones{display: none;}</style>";
			s += "<NOSCRIPT>\n"
				+ "    <font color=red>\n"
				+ "    <H3>This page requires that JavaScript be enabled to function properly</H3>\n"
				+ "    </font>\n"
				+ "</NOSCRIPT>\n"
			;
 
			s += "<script type='text/javascript'>\n";
			
			s += "$( document ).ready(function() {\n"
					+ "  var sEmpID =  $('#sEmployeeID').val();\n"
					+ "$('#' + sEmpID).show();\n"
					+"});";
			
			s += "function displayMilestones(EmployeeSelection){\n"
				 + "var sEmpID = EmployeeSelection.value;\n"
				 + "    $('.employeemilestones').hide();\n"
				 + "$('#' + sEmpID).show();\n"
				+ "}\n"
			;

			s += "</script>\n";
			return s;
		}
	
	
	private String getCSSStyles() {
		String s = "";
		
		s += "<style>\n\n";			
		s += "	.employeemilestones{\n"
					+ "display: none;\n"
				+ "}\n";		
		s += "</style>\n\n";
		
		
		return s;
	}
	
	public void doGet(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
		
		doPost(request, response);
	}
}
