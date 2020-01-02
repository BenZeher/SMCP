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
import TCSDataDefinition.Employees;
import TCSDataDefinition.TCSTableEmployeeTypeAccess;
import TCSDataDefinition.TCSTableEmployeeTypeLinks;
import TCSDataDefinition.TCSTableEmployeeTypes;


public class TCEditEmployeeTypesEdit extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final String MAIN_FORM_NAME = "MilestoneForm";
	private static final String CALLED_CLASS = "TimeCardSystem.TCEditEmployeeTypesAction";
	
	public static final String SUBMIT_EDIT_BUTTON_NAME = "SUBMITEDIT";
	public static final String SUBMIT_EDIT_BUTTON_LABEL = "Update";
	public static final String SUBMIT_DELETE_BUTTON_NAME = "SUBMITDELETE";
	public static final String SUBMIT_DELETE_BUTTON_LABEL = "Delete";
	public static final String CONFIRM_DELETE_CHECKBOX_NAME = "CONFIRMDELETE";
	
	public static final String UPDATE_USER_MARKER = "User***Update";
	public static final String UPDATE_ACCESS_MARKER = "Access***Update";

	public void doPost(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
		
		response.setContentType("text/html");
		PrintWriter out = response.getWriter();
		
	    HttpSession CurrentSession = request.getSession(true);
	    String sDBID = (String) CurrentSession.getAttribute(TimeCardUtilities.SESSION_ATTRIBUTE_DB);
	    String sUser = (String) CurrentSession.getAttribute(TimeCardUtilities.SESSION_ATTRIBUTE_EID);
		String sCallingClass = clsManageRequestParameters.get_Request_Parameter("CallingClass", request);
	    
		TCEmployeeType entry = new TCEmployeeType(request);

		//If the user chose to ADD A NEW MILESTONE, then we just initialize the entry, and go from there:
		if (clsManageRequestParameters.get_Request_Parameter(TCEditEmployeeTypeSelection.BUTTON_ADD_NEW_NAME, request).compareToIgnoreCase(TCEditEmployeeTypeSelection.BUTTON_ADD_NEW_LABEL) == 0){
			entry.set_slid("-1");
		}

		//If this is a 'resubmit', meaning it's being called by the 'Action' class, then
		//the session will have an entry object in it, and that's what we'll pick up.
		
	    if (CurrentSession.getAttribute(TCEmployeeType.ParamObjectName) != null){
	    	entry = (TCEmployeeType) CurrentSession.getAttribute(TCEmployeeType.ParamObjectName);
	    	CurrentSession.removeAttribute(TCEmployeeType.ParamObjectName);
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
	    
	    String sTitle = "Edit Employee Types";
	    String sSubtitle = "";
	    out.println(TimeCardUtilities.TCTitleSubBGColorWithFont(sTitle, sSubtitle, TimeCardUtilities.BACKGROUND_COLOR_FOR_ADMIN_SCREENS, TimeCardUtilities.BASE_FONT_FAMILY));
	    out.println(TimeCardUtilities.getDatePickerIncludeString(getServletContext()));
	    
	    out.println("<BR><A HREF=\"" + TCWebContextParameters.getURLLinkBase(getServletContext()) 
	    	+ "TimeCardSystem.AdminMain\">Return to main menu</A><BR>");
	    
	    out.println("<BR><A HREF=\"" + TCWebContextParameters.getURLLinkBase(getServletContext()) 
    		+ "TimeCardSystem.TCEditEmployeeTypeSelection\">Edit another employee type</A><BR>");
	    
    	String sWarning = clsManageRequestParameters.get_Request_Parameter("Warning", request);
    	if (sWarning.compareToIgnoreCase("") != 0){
    		out.println("<BR><B><FONT COLOR=RED>Error editing " + TCEmployeeType.ParamObjectName + " - " + sWarning + ".</FONT></B><BR>");
    	}
    	out.println("<BR>");
    	String sStatus = clsManageRequestParameters.get_Request_Parameter("Status", request);
    	if (sStatus.compareToIgnoreCase("") != 0){
    		out.println("<B><FONT COLOR=YELLOW>" + sStatus + "</FONT></B><BR>");
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
			+ "<INPUT TYPE=HIDDEN NAME=\"" + TCSTableEmployeeTypes.lid + "\" VALUE=\"" + entry.get_slid() + "\">"
			+ "</TD>"
			+ "</TR>"
		);
		
	    //Name
	    out.println(TimeCardUtilities.Create_Edit_Form_Text_Input_Row(
	    	TCSTableEmployeeTypes.sName,
	    	entry.get_sName(), 
	    	TCSTableEmployeeTypes.sNameLength, 
	    	"Name:", 
	    	"Milestone Name", 
	    	"40", 
	    	"")
	    );
	    
	    //Description
	    out.println(TimeCardUtilities.Create_Edit_Form_MultilineText_Input_Row(
	    		TCSTableEmployeeTypes.sDescription,
	    	entry.get_sDescription(), 
	    	"Description:", 
	    	"Enter milestone description", 
	    	3,
	    	40,
	    	"")
	    );

		out.println("</TABLE>");
		
	    //Print users table for Employee Type Group
	    out.println("<BR><B>Select users to include in employee type " + "</B>");
	    
	    //Add user list
		ArrayList<String> sUserTable = new ArrayList<String>(0);
		try{
			//First get a list of all the users:
	        String sSQL = TimeCardSQLs.Get_Employee_List_SQL(false);
	        ResultSet rsEmployees = clsDatabaseFunctions.openResultSet(sSQL, getServletContext(), sDBID);
        	
	        sSQL = TimeCardSQLs.Get_Employee_Type_Users_SQL(sID);
	        ResultSet rsTypeUsers = clsDatabaseFunctions.openResultSet(sSQL, getServletContext(), sDBID);
	        
	        String sCheckedOrNot = "";
        	while (rsEmployees.next()){
        		if(Is_User_In_Employee_Type(rsEmployees.getString(Employees.sEmployeeID), rsTypeUsers)){
        			sCheckedOrNot = "checked";
        		}else{
        			sCheckedOrNot = "";
        		}
        		sUserTable.add((String) "<INPUT TYPE=CHECKBOX NAME=\"" + UPDATE_USER_MARKER +  rsEmployees.getString(Employees.sEmployeeID) + "\" " + sCheckedOrNot + ">" + rsEmployees.getString(Employees.sEmployeeLastName) + ", " + rsEmployees.getString(Employees.sEmployeeFirstName));
        	}
        	rsEmployees.close();
        	rsTypeUsers.close();
        	//Print the table:
        	out.println(TimeCardUtilities.Build_HTML_Table(5, sUserTable,1,false));
        	
		}catch (SQLException ex){
	    	out.println("Error in EditEmployeeTypeEdit class!!");
	        out.println("SQLException: " + ex.getMessage());
	        out.println("SQLState: " + ex.getSQLState());
	        out.println("SQL: " + ex.getErrorCode());
			//return false;
		}
		
	    //Print users table for Employee type Access
	    out.println("<BR><BR><B>Select users to access employee type " + "</B>");
	    
	    //Add user list
	    ArrayList<String> sAccessUserTable = new ArrayList<String>(0);
		try{
			//First get a list of all the users:
	        String sSQL = TimeCardSQLs.Get_Employee_List_SQL(false);
	        ResultSet rsAccessEmployees = clsDatabaseFunctions.openResultSet(sSQL, getServletContext(), sDBID);
	      
	        sSQL = TimeCardSQLs.Get_Employee_Type_Access_Users_SQL(sID);
	        ResultSet rsAccesssTypeUsers = clsDatabaseFunctions.openResultSet(sSQL, getServletContext(), sDBID);
	       
	        String sCheckedOrNot = "";
        	while (rsAccessEmployees.next()){
        		if(Is_User_In_Employee_Type_Acesss(rsAccessEmployees.getString(Employees.sEmployeeID), rsAccesssTypeUsers)){
        			sCheckedOrNot = "checked";
        		}else{
        			sCheckedOrNot = "";
        		}
        		sAccessUserTable.add((String) "<INPUT TYPE=CHECKBOX NAME=\"" + UPDATE_ACCESS_MARKER  +  rsAccessEmployees.getString(Employees.sEmployeeID) 
        				+ "\" " + sCheckedOrNot + ">" 
        				+ rsAccessEmployees.getString(Employees.sEmployeeLastName) + ", " + rsAccessEmployees.getString(Employees.sEmployeeFirstName));
        	}
        	rsAccessEmployees.close();
        	rsAccesssTypeUsers.close();
        	//Print the table:
        	out.println(TimeCardUtilities.Build_HTML_Table(5, sAccessUserTable,1,false));
        	
		}catch (SQLException ex){
	    	out.println("Error in EditEmployeeTypeEdit class!!");
	        out.println("SQLException: " + ex.getMessage());
	        out.println("SQLState: " + ex.getSQLState());
	        out.println("SQL: " + ex.getErrorCode());
			//return false;
		}

	    
		out.println("<P><INPUT TYPE=SUBMIT NAME='" 
			+ SUBMIT_EDIT_BUTTON_NAME 
			+ "' VALUE='" + SUBMIT_EDIT_BUTTON_LABEL + "' STYLE='height: 0.24in'>"
		);
	    
		out.println("<INPUT TYPE=SUBMIT NAME='" 
			+ SUBMIT_DELETE_BUTTON_NAME 
			+ "' VALUE=\"" + SUBMIT_DELETE_BUTTON_LABEL + "\"" + TCEmployeeType.ParamObjectName 
			+ "' STYLE='height: 0.24in'>"
			+ "  Check to confirm deletion: <INPUT TYPE=CHECKBOX NAME=\"" 
			+ CONFIRM_DELETE_CHECKBOX_NAME + "\"></P>"
		);
			
	    return;
	}

	private boolean Is_User_In_Employee_Type(String sCurrentUserID, ResultSet rsTypeUsers) throws SQLException {
		rsTypeUsers.beforeFirst();
		while(rsTypeUsers.next()){
			if(sCurrentUserID.compareToIgnoreCase(rsTypeUsers.getString(TCSTableEmployeeTypeLinks.TableName + "." + TCSTableEmployeeTypeLinks.sEmployeeID)) == 0){
				return true;
			}
		}
		return false;
	}
	
	private boolean Is_User_In_Employee_Type_Acesss(String sCurrentUserID, ResultSet rsAccesssTypeUsers) throws SQLException {
		rsAccesssTypeUsers.beforeFirst();
		while(rsAccesssTypeUsers.next()){
			if(sCurrentUserID.compareToIgnoreCase(rsAccesssTypeUsers.getString(TCSTableEmployeeTypeAccess.TableName + "." + TCSTableEmployeeTypeAccess.sEmployeeID)) == 0){
				return true;
			}
		}
		return false;
	}

	public void doGet(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
		
		doPost(request, response);
	}
}
