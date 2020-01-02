package TimeCardSystem;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsManageRequestParameters;
import TCSDataDefinition.ACUserGroups;
import TCSDataDefinition.Departments;
import TCSDataDefinition.Employees;
import TCSDataDefinition.TCSTablemadgicevents;
import TCSDataDefinition.TCSTablemadgiceventtypes;
import TCSDataDefinition.TCSTablemadgiceventusers;

public class TCEditMADGICEventsEdit extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final String MAIN_FORM_NAME = "MADGICEventForm";
	private static final String CALLED_CLASS = "TimeCardSystem.TCEditMADGICEventsAction";
	
	public static final String SUBMIT_EDIT_BUTTON_NAME = "SUBMITEDIT";
	public static final String SUBMIT_EDIT_BUTTON_LABEL = "Update";
	public static final String SUBMIT_DELETE_BUTTON_NAME = "SUBMITDELETE";
	public static final String SUBMIT_DELETE_BUTTON_LABEL = "Delete";
	public static final String CONFIRM_DELETE_CHECKBOX_NAME = "CONFIRMDELETE";
	public static final String ACTIVE_EMPLOYEE_CHECKBOX_PREFIX = "ACTIVEEMPLOYEECHECKBOX1";
	public static final String INACTIVE_EMPLOYEE_CHECKBOX_PREFIX = "INACTIVEEMPLOYEECHECKBOX0";
	private static final String UNCHECKALLEMPLOYEESBUTTON = "UnCheckAllEmployees";
	private static final String UNCHECKALLEMPLOYEESLABEL = "UNCHECK All Employees";
	private static final String CHECKALLEMPLOYEESBUTTON = "CheckAllEmployees";
	private static final String CHECKALLEMPLOYEESLABEL = "CHECK All Employees";
	private static final String INACTIVE_EMPLOYEES_TABLE_DIV = "INACTIVEEMPLOYEESDIV";
	private static final String SHOWINACTIVEEMPLOYEESBUTTON = "ShowInactiveEmployees";
	private static final String SHOWINACTIVEEMPLOYEESLABEL = "Display INACTIVE Employees";
	private static final String HIDEINACTIVEEMPLOYEESBUTTON = "HideInactiveEmployees";
	private static final String HIDEINACTIVEEMPLOYEESLABEL = "Hide INACTIVE Employees";
	
	public void doPost(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
		
		response.setContentType("text/html");
		PrintWriter out = response.getWriter();
		
	    HttpSession CurrentSession = request.getSession(true);
	    String sDBID = (String) CurrentSession.getAttribute(TimeCardUtilities.SESSION_ATTRIBUTE_DB);
	    String sUser = (String) CurrentSession.getAttribute(TimeCardUtilities.SESSION_ATTRIBUTE_EID);
		String sCallingClass = clsManageRequestParameters.get_Request_Parameter("CallingClass", request);
	    
		TCMADGICEvent entry = new TCMADGICEvent(request);

		//If the user chose to ADD A NEW EVENT, then we just initialize the entry, and go from there:
		if (clsManageRequestParameters.get_Request_Parameter(TCEditMADGICEventsSelection.BUTTON_ADD_NEW_NAME, request).compareToIgnoreCase(TCEditMADGICEventsSelection.BUTTON_ADD_NEW_LABEL) == 0){
			entry.set_slid("-1");
		}
		
		//If this is a 'resubmit', meaning it's being called by the 'Action' class, then
		//the session will have an entry object in it, and that's what we'll pick up.
		
	    if (CurrentSession.getAttribute(TCMADGICEvent.ParamObjectName) != null){
	    	entry = (TCMADGICEvent) CurrentSession.getAttribute(TCMADGICEvent.ParamObjectName);
	    	CurrentSession.removeAttribute(TCMADGICEvent.ParamObjectName);
	    //But if it's NOT a 'resubmit', meaning this class was called for the first time to 
	    //edit, we'll pick up the ID or key from the request and try to load the entry:
	    }else{
	    	//Unless we are trying to create a NEW type, load the existing type:
	    	if (entry.get_slid().compareToIgnoreCase("-1") != 0){
	    		try {
					entry.load(sDBID, getServletContext(), sUser);
				} catch (Exception e) {
					response.sendRedirect(
							"" + TimeCardUtilities.getURLLinkBase(getServletContext()) + sCallingClass
							+ "?Warning=" + e.getMessage()
						);
						return;
				}
	    	}
	    }
	    
	    String sTitle = "Edit MADGIC Events";
	    String sSubtitle = "";
	    out.println(TimeCardUtilities.TCTitleSubBGColorWithFont(sTitle, sSubtitle, TimeCardUtilities.BACKGROUND_COLOR_FOR_ADMIN_SCREENS, TimeCardUtilities.BASE_FONT_FAMILY));
	    out.println(TimeCardUtilities.getDatePickerIncludeString(getServletContext()));
	    
	    out.println("<BR><A HREF=\"" + TCWebContextParameters.getURLLinkBase(getServletContext()) 
	    	+ "TimeCardSystem.AdminMain\">Return to main menu</A><BR><BR>");
	    
	    out.println("<BR><A HREF=\"" + TCWebContextParameters.getURLLinkBase(getServletContext()) 
    		+ "TimeCardSystem.TCEditMADGICEventsSelection\">Edit another MADGIC event</A><BR><BR>");
	    
    	String sWarning = clsManageRequestParameters.get_Request_Parameter("Warning", request);
    	if (sWarning.compareToIgnoreCase("") != 0){
    		out.println("<BR><B><FONT COLOR=RED>Error editing " + TCMADGICEvent.ParamObjectName + " - " + sWarning + ".</FONT></B><BR>");
    	}
    	String sStatus = clsManageRequestParameters.get_Request_Parameter("Status", request);
    	if (sStatus.compareToIgnoreCase("") != 0){
    		out.println("<BR><B><FONT COLOR=YELLOW>" + sStatus + "</FONT></B><BR>");
    	}
	    
    	try {
    		out.println(sCommandScripts(sDBID));
		} catch (SQLException e1) {
			out.println("<BR><B><FONT COLOR=RED>Error loading event type default points - " + e1.getMessage() + ".</FONT></B></BR>");
		}
    	
	    out.println("<FORM ID='\"" + MAIN_FORM_NAME + "'\" NAME=\"" + MAIN_FORM_NAME + "\" ACTION=\"" 
			+ TimeCardUtilities.getURLLinkBase(getServletContext()) + CALLED_CLASS + "\""
			+ " METHOD=\"POST\">"
		);
			
	    out.println("<INPUT TYPE=HIDDEN NAME=\"" + "CallingClass" + "\" VALUE=\"" + this.getClass().getName() + "\">");

	    out.println("<TABLE BORDER=1>");

	    String sID = "NEW";
	    if (entry.get_slid().compareToIgnoreCase("-1") != 0){
	    	sID = entry.get_slid();
	    }
	    
		out.println("<TR>"
			+ "<TD ALIGN=RIGHT><B>" + "ID:" + " </B></TD>"
			+ "<TD ALIGN=LEFT>" + sID + "</TD>"
			+ "<TD ALIGN=LEFT>" 
			+ "&nbsp;" 
			+ "<INPUT TYPE=HIDDEN NAME=\"" + TCSTablemadgicevents.lid + "\" VALUE=\"" + entry.get_slid() + "\">"
			+ "</TD>"
			+ "</TR>"
		);
	    
	    //Name
		if (entry.get_slid().compareToIgnoreCase("-1") != 0){
			out.println("<TR>"
				+ "<TD ALIGN=RIGHT><B>" + "Event type:" + " </B></TD>"
				+ "<TD ALIGN=LEFT>" + entry.get_seventtypename() 
				+ "<INPUT TYPE=HIDDEN NAME=\"" + TCSTablemadgicevents.seventtypename + "\" VALUE=\"" + entry.get_seventtypename() + "\">"
				+ "</TD>"
				+ "<TD ALIGN=LEFT>" + "The event type" + "</TD>"
				+ "</TR>"
			);
		}else{
			
			String SQL = "SELECT * FROM " + TCSTablemadgiceventtypes.TableName
				+ " ORDER BY " + TCSTablemadgiceventtypes.sname
			;
			ArrayList<String>arrEventTypeNames = new ArrayList<String>(0);
			ArrayList<String>arrEventTypeDescriptions = new ArrayList<String>(0);
			arrEventTypeNames.add("");
			arrEventTypeDescriptions.add("** Select an event type **");
			try {
				ResultSet rsEventTypes = clsDatabaseFunctions.openResultSet(SQL, getServletContext(), sDBID);
				while (rsEventTypes.next()){
					arrEventTypeNames.add(rsEventTypes.getString(TCSTablemadgiceventtypes.sname));
					arrEventTypeDescriptions.add(rsEventTypes.getString(TCSTablemadgiceventtypes.sname));
				}
				rsEventTypes.close();
			} catch (SQLException e) {
				out.println("<BR><BR><B><FONT COLOR=RED>Error [1486657360] listing event types with SQL '" 
					+ SQL + "' - " + e.getMessage() + "</FONT></B>");
			}
			out.println(TimeCardUtilities.Create_Edit_Form_List_Row (
				TCSTablemadgicevents.seventtypename,
				arrEventTypeNames,
				entry.get_seventtypename(),
				arrEventTypeDescriptions,
				"Event type:",
				"Select the appropriate event type",
				"eventTypeChange(this);"
				)
			);
		}

		//Date
		out.println(TimeCardUtilities.Create_Edit_Form_Date_Input_Row(
			TCSTablemadgicevents.datevent, 
			entry.get_sdatevent(), 
			"Event date:", 
			"Enter date you want associated with this event", 
			getServletContext())
		);
		
	    //Number of points
	    out.println(TimeCardUtilities.Create_Edit_Form_Text_Input_Row(
	    	TCSTablemadgicevents.inumberofpoints,
	    	entry.get_snumberofpoints(), 
	    	3, 
	    	"Number of points:", 
	    	"The number of points awarded for this event", 
	    	"8", 
	    	"")
	    );
		
	    //Description
		out.println(TimeCardUtilities.Create_Edit_Form_MultilineText_Input_Row(
			TCSTablemadgicevents.sdescription, 
			entry.get_sdescription(), 
			"Description:", 
			"Enter event details here", 
			4, 
			120, 
			""
			)
		);
	    
		out.println("</TABLE>");
	    
		out.println("<P><INPUT TYPE=SUBMIT NAME='" 
			+ SUBMIT_EDIT_BUTTON_NAME 
			+ "' VALUE='" + SUBMIT_EDIT_BUTTON_LABEL + "' STYLE='height: 0.24in'>"
		);
	    
		out.println("<INPUT TYPE=SUBMIT NAME='" 
			+ SUBMIT_DELETE_BUTTON_NAME 
			+ "' VALUE=\"" + SUBMIT_DELETE_BUTTON_LABEL + "\"" + TCMADGICEvent.ParamObjectName 
			+ "' STYLE='height: 0.24in'>"
			+ "  Check to confirm deletion: <INPUT TYPE=CHECKBOX NAME=\"" 
			+ CONFIRM_DELETE_CHECKBOX_NAME + "\"></P>"
		);
		
		out.println("<BR><input type=\"button\" name=\"" 
      		+ CHECKALLEMPLOYEESBUTTON + "\" value=\"" + CHECKALLEMPLOYEESLABEL 
    		+ "\" onClick=\"checkall()\">"
    		+ "&nbsp;<input type=\"button\" name=\"" 
    		+ UNCHECKALLEMPLOYEESBUTTON + "\" value=\"" + UNCHECKALLEMPLOYEESLABEL 
    		+ "\" onClick=\"uncheckall()\">"
    	);
		
		out.println("&nbsp;&nbsp;&nbsp;&nbsp;<input type=\"button\" name=\"" 
	      	+ SHOWINACTIVEEMPLOYEESBUTTON + "\" value=\"" + SHOWINACTIVEEMPLOYEESLABEL 
	    	+ "\" onClick=\"displayInactiveEmployees()\">"
	    	+ "&nbsp;<input type=\"button\" name=\"" 
	    	+ HIDEINACTIVEEMPLOYEESBUTTON + "\" value=\"" + HIDEINACTIVEEMPLOYEESLABEL 
	    	+ "\" onClick=\"hideInactiveEmployees()\">"
	    	+ "<BR>"
	    );

		//Display the active users, in every case:
		try {
			out.println(displayUsersTable(sDBID, entry.get_slid(), true));
		} catch (Exception e) {
			out.println("<BR><BR><B><FONT COLOR=RED>Error displaying users table - " + e.getMessage() + ".</FONT></B><BR><BR>");
		}
		
		//Create the INactive users table:
		//Display the active users, in every case:
		try {
			out.println("<div id= \"" + INACTIVE_EMPLOYEES_TABLE_DIV + "\" style=\"display:none;\">\n");
			out.println(displayUsersTable(sDBID, entry.get_slid(), false));
			out.println("\n</div>\n");
		} catch (Exception e) {
			out.println("<BR><BR><B><FONT COLOR=RED>Error displaying INACTIVE users table - " + e.getMessage() + ".</FONT></B><BR><BR>");
		}
		
	    return;
	}

	private String displayUsersTable(String sDBID, String sMADGICEventID, boolean bActiveEmployees) throws Exception{
		String s = "";
		
		String sActiveStatus = "0";
		String sActiveLabel = "INACTIVE";
		String sCheckboxPrefix = INACTIVE_EMPLOYEE_CHECKBOX_PREFIX;
		if (bActiveEmployees){
			sActiveStatus = "1";
			sActiveLabel = "ACTIVE";
			sCheckboxPrefix = ACTIVE_EMPLOYEE_CHECKBOX_PREFIX;
		}
		
		ArrayList<String> sUserTable = new ArrayList<String>(0);
			//First get a list of all the users:
	        String sSQL = "SELECT * FROM " + Employees.TableName 
	        	+ " LEFT JOIN " + Departments.TableName 
	        	+ " ON " + Employees.TableName + "." + Employees.iDepartmentID + " = "
	        	+ Departments.TableName + "." + Departments.iDeptID
				+ " WHERE (" 
	        		+ "(" + Employees.TableName + "." + Employees.iActive + " = " + sActiveStatus + ")"
				+ ")  ORDER BY " + Employees.TableName + "." + Employees.sEmployeeLastName + ", " + Employees.sEmployeeFirstName
			;
	        ResultSet rsEmployees;
			try {
				rsEmployees = clsDatabaseFunctions.openResultSet(sSQL, getServletContext(), sDBID, "MySQL", this.toString());
			} catch (Exception e) {
				throw new Exception("Error [1486679822] listing users with SQL '" + sSQL + " - " + e.getMessage());
			}
        	
	        sSQL = "SELECT * FROM " + TCSTablemadgiceventusers.TableName 
	        	+ " WHERE (" 
	        		+ "(" + TCSTablemadgiceventusers.lmadgiceventid + " = " + sMADGICEventID + ")"
	        	+ ") ORDER BY " + ACUserGroups.sEmployeeID;
	        
	        ResultSet rsEventUsers;
			try {
				rsEventUsers = clsDatabaseFunctions.openResultSet(sSQL, getServletContext(), sDBID);
			} catch (Exception e) {
				throw new Exception("Error [1486679823] listing event users with SQL '" + sSQL + " - " + e.getMessage());
			}
	        
	        String sCheckedOrNot = "";
        	try {
				while (rsEmployees.next()){
					sCheckedOrNot = Is_Employee_In_Event(rsEmployees.getString(Employees.TableName + "." + Employees.sEmployeeID), rsEventUsers);
					sUserTable.add((String) "<label><INPUT TYPE=CHECKBOX " + sCheckedOrNot 
						+ " NAME=\"" + sCheckboxPrefix +  rsEmployees.getString(Employees.sEmployeeID) + "\">" 
							+ rsEmployees.getString(Employees.sEmployeeLastName) + ", " + rsEmployees.getString(Employees.sEmployeeFirstName)
							+ "</label>");
				}
				rsEmployees.close();
				rsEventUsers.close();
			} catch (Exception e) {
				throw new Exception("Error [1486679824] checking event users - " + e.getMessage());
			}
        	
        	s += "<BR><I><B>" + sActiveLabel + " Employees assigned to this event:</B></I>";
        	
        	//Print the table:
        	s += TimeCardUtilities.Build_HTML_Table(5, sUserTable,1,false);
		
		return s;
	}
	public String Is_Employee_In_Event(String sEmployee, ResultSet rsEventUsers) throws Exception{
		
		try {
			// Set this recordset to the beginning every time:
			rsEventUsers.beforeFirst();
			while (rsEventUsers.next()){
				if (rsEventUsers.getString(ACUserGroups.sEmployeeID).compareTo(sEmployee) == 0){
					return "checked=\"Yes\"";
				}
			}
			//If we never found a matching record, return:
			return "";
		}catch (SQLException ex){
	    	throw new Exception("Error [1486679821] checking users in event - " + ex.getMessage());
		}
	}
	private String sCommandScripts(String sDBID) throws SQLException{
		String s = "";
		
		s += "<NOSCRIPT>\n"
			+ "    <font color=red>\n"
			+ "    <H3>This page requires that JavaScript be enabled to function properly</H3>\n"
			+ "    </font>\n"
			+ "</NOSCRIPT>\n\n"
		;

		s += "<script type='text/javascript'>\n\n";
		//s += "window.onbeforeunload = prompttosave;\n";
		
		//load event types list
		int iCounter = 0;
		
		String seventtypename = "";
		
		String SQL = "SELECT"
				+ " " + TCSTablemadgiceventtypes.inumberofpoints
				+ ", " + TCSTablemadgiceventtypes.sname
				+ ", " + TCSTablemadgiceventtypes.sdescription
				+ " FROM " + TCSTablemadgiceventtypes.TableName
				+ " ORDER BY " + TCSTablemadgiceventtypes.sname
				;
		
		try {
			ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, 
					getServletContext(), 
					sDBID, 
					"MySQL", 
					this.toString() + " [1486675507] SQL: " + SQL 
			);
			while (rs.next()){
				iCounter++;
				seventtypename += "    seventtypenames[\"" + rs.getString(TCSTablemadgiceventtypes.sname).trim() + "\"] = \"" 
					+ Integer.toString(rs.getInt(TCSTablemadgiceventtypes.inumberofpoints)).trim().replace("\"", "'") + "\";\n";
			}
			rs.close();
		} catch (SQLException e) {
			throw new SQLException("Error [1486675724] reading points awarded for javascript - " + e.getMessage());
		}
		
		//Create the arrays, if there are any:
		if (iCounter > 0){
			s += "    var seventtypenames = new Array(" + Integer.toString(iCounter) + ");\n";
			s += "    " + seventtypename + "\n";
		}
		
		s += "\n";
		

		s += "    function eventTypeChange(selectObj) {\n" 
		// get the index of the selected option 
		+ "        var idx = selectObj.selectedIndex;\n"
		// get the value of the selected option 
		+ "        var which = selectObj.options[idx].value;\n"
		//+ "        alert(selectObj.options[idx].value);\n"
		// use the selected option value to retrieve the ship to fields from the ship to arrays:
		+ "        if (which != ''){\n"
		+ "            document.forms[\"" + MAIN_FORM_NAME + "\"].elements[\"" + TCSTablemadgicevents.inumberofpoints + "\"].value = seventtypenames[which];\n"
		+ "        }\n"
		//+ "    flagDirty();\n"
		+ "    }\n\n"; 
		
		//Functions for turning on and off the checkboxes:
       	s += "    function checkall(){\n"
			+ "        for (i=0; i<document.forms[\"" + MAIN_FORM_NAME + "\"].elements.length; i++){\n"
   			+ "            var testName = document.forms[\"" + MAIN_FORM_NAME + "\"].elements[i].name;\n"
   			+ "            if (testName.substring(0, " + Integer.toString(ACTIVE_EMPLOYEE_CHECKBOX_PREFIX.length()) 
   				+ "	) == \"" + ACTIVE_EMPLOYEE_CHECKBOX_PREFIX + "\"){\n"
   			+ "                document.forms[\"" + MAIN_FORM_NAME + "\"].elements[i].checked = true;\n"
   			+ "            }\n"
   			+ "        }\n"
		  + "    }\n\n";

       	s += "    function uncheckall(){\n"
			+ "        for (i=0; i<document.forms[\"" + MAIN_FORM_NAME + "\"].elements.length; i++){\n"
   			+ "            var testName = document.forms[\"" + MAIN_FORM_NAME + "\"].elements[i].name;\n"
   			+ "            if (testName.substring(0, " + Integer.toString(ACTIVE_EMPLOYEE_CHECKBOX_PREFIX.length()) 
   				+ "	) == \"" + ACTIVE_EMPLOYEE_CHECKBOX_PREFIX + "\"){\n"
   			+ "                document.forms[\"" + MAIN_FORM_NAME + "\"].elements[i].checked = false;\n"
   			+ "            }\n"
   			+ "        }\n"
		  + "    }\n";
		
		//Function for displaying or hiding the INactive users:
		s += "    function displayInactiveEmployees() {\n" 
			+ "        document.getElementById('" + INACTIVE_EMPLOYEES_TABLE_DIV + "').style.display = \"inline\";\n"
			+ "    }\n"
		;
		s += "    function hideInactiveEmployees() {\n" 
			+ "        document.getElementById('" + INACTIVE_EMPLOYEES_TABLE_DIV + "').style.display = \"none\";\n"
			+ "    }\n"
		;

		s += "</script>\n\n";
		
		return s;	
	}
	
	public void doGet(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
		
		doPost(request, response);
	}
}
