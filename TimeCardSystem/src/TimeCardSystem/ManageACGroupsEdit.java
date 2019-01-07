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
import ServletUtilities.clsStringFunctions;
import TCSDataDefinition.*;

public class ManageACGroupsEdit extends HttpServlet {

	private static final long serialVersionUID = 1L;

	public void doPost(HttpServletRequest request,
				HttpServletResponse response)
				throws ServletException, IOException {

		response.setContentType("text/html");
		
	    //Get the session info:
	    HttpSession CurrentSession = request.getSession(true);
	    String sConfFile = (String) CurrentSession.getAttribute(TimeCardUtilities.SESSION_ATTRIBUTE_DB);
	    //String sUserName = (String) CurrentSession.getAttribute(TimeCardUtilities.SESSION_ATTRIBUTE_USERNAME);
	    String sGroupName = clsStringFunctions.filter(request.getParameter("Security Groups"));

		PrintWriter out = response.getWriter();

		String title = "";
		String subtitle = "";
		
	    if(request.getParameter("SubmitEdit") != null){
	    	//User has chosen to edit an existing group:
			title = "Edit Access Control Group: " + sGroupName;
		    subtitle = "";
		    out.println(TimeCardUtilities.TCTitleSubBGColor(title, subtitle, TimeCardUtilities.BACKGROUND_COLOR_FOR_ADMIN_SCREENS));

		    //Print a link to the first page after login:
		    out.println("<BR><A HREF=\"" + ConnectionPool.WebContextParameters.getURLLinkBase(getServletContext()) + "TimeCardSystem.AdminMain\">Return to main menu</A><BR><BR>");
			
		    Edit_Group(sGroupName, out, sConfFile, false);
	    }
	    if(request.getParameter("SubmitDelete") != null){
	    	//User has chosen to delete an existing group:
			title = "Delete Access Control Group: " + sGroupName;
		    subtitle = "";
		    out.println(TimeCardUtilities.TCTitleSubBGColor(title, subtitle, TimeCardUtilities.BACKGROUND_COLOR_FOR_ADMIN_SCREENS));

		    //Print a link to the first page after login:
		    out.println("<BR><A HREF=\"" + ConnectionPool.WebContextParameters.getURLLinkBase(getServletContext()) + "TimeCardSystem.AdminMain\">Return to main menu</A><BR><BR>");
			
		    if (request.getParameter("ConfirmDelete") == null){
		    	out.println ("You must check the 'confirming' check box to delete a group.");
		    }
		    else{
		    	System.out.println("Group Name#1 = " + sGroupName);
			    if (Delete_Group(sGroupName, out, sConfFile) == false){
			    	out.println ("Error deleting group " + sGroupName + ".");
					out.println ("<A href=" + ConnectionPool.WebContextParameters.getURLLinkBase(getServletContext()) + "TimeCardSystem.ManageACGroups>Click here to return to access control group list.</A>");
			    }
			    else{
			    	out.println ("Successfully deleted group " + sGroupName + ".");
			    	out.println("<META http-equiv='Refresh' content='2;URL=" + ConnectionPool.WebContextParameters.getURLLinkBase(getServletContext()) + "TimeCardSystem.ManageACGroups'>");
			    }
		    }
	    }
	    if(request.getParameter("SubmitAdd") != null){
	    	
		    String sNewGroupName = clsStringFunctions.filter(request.getParameter("NewGroupName"));
	    	//User has chosen to add a new group:
			title = "Add Access Control Group: " + sNewGroupName;
		    subtitle = "";
		    out.println(TimeCardUtilities.TCTitleSubBGColor(title, subtitle, TimeCardUtilities.BACKGROUND_COLOR_FOR_ADMIN_SCREENS));

		    //Print a link to the first page after login:
		    out.println("<BR><A HREF=\"" + ConnectionPool.WebContextParameters.getURLLinkBase(getServletContext()) + "TimeCardSystem.AdminMain\">Return to main menu</A><BR><BR>");

		    if (sNewGroupName == ""){
		    	out.println ("You chose to add a new group, but you did not enter a new group name to add.");
		    }
		    else{
		    	Edit_Group(sNewGroupName, out, sConfFile, true);
		    }
	    }
		
		out.println("</BODY></HTML>");
	}
	
	public String Is_Function_In_Group(String sFunction, ResultSet rs){
		
		try {
			// Set this recordset to the beginning every time:
			rs.beforeFirst();
			while (rs.next()){
				if (rs.getString(ACGroupFunctions.sFunction).compareTo(sFunction) == 0){
					return "checked=\"Yes\"";
				}
			}
			//rs.close();
			//If we never found a matching record, return 'NO':
			return "";
		}catch (SQLException ex){
	    	System.out.println("Error in ManageACGroupsEdit class in Is_Function_In_Group!!");
	        System.out.println("SQLException: " + ex.getMessage());
	        System.out.println("SQLState: " + ex.getSQLState());
	        System.out.println("SQL: " + ex.getErrorCode());
			return "";
		}
	}
	
	public String Is_User_In_Group(String sUser, ResultSet rs){
		
		try {
			// Set this recordset to the beginning every time:
			rs.beforeFirst();
			while (rs.next()){
				if (rs.getString(ACUserGroups.sEmployeeID).compareTo(sUser) == 0){
					return "checked=\"Yes\"";
				}
			}
			//If we never found a matching record, return:
			return "";
		}catch (SQLException ex){
	    	System.out.println("Error in ManageACGroupsEdit class in Is_User_In_Group!!");
	        System.out.println("SQLException: " + ex.getMessage());
	        System.out.println("SQLState: " + ex.getSQLState());
	        System.out.println("SQL: " + ex.getErrorCode());
			return "";
		}
	}

	private void Edit_Group(
			String sGroup, 
			PrintWriter pwOut, 
			String sConf,
			boolean bAddGroup){
	    
		//first, add the group if it's an 'Add':
		if (bAddGroup == true){
			if (Add_Group (sGroup, sConf, pwOut) == false){
				pwOut.println("ERROR - Could not add group " + sGroup + ".<BR>");
				return;
			}
		}
		
		pwOut.println("<FORM NAME='MAINFORM' ACTION='" + ConnectionPool.WebContextParameters.getURLLinkBase(getServletContext()) + "TimeCardSystem.ManageACGroupsAction' METHOD='POST'>");
		//pwOut.println("<FORM NAME='MAINFORM' ACTION='" + ConnectionPool.WebContextParameters.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMManageSecurityGroupsAction' METHOD='POST'>");
	    pwOut.println("<INPUT TYPE=HIDDEN NAME=\"GroupName\" VALUE=\"" + sGroup + "\">");
	    String sOutPut = "";
	  
	    pwOut.println("<B>Select Functions To Include In Group '" + sGroup + "':</B>");
	    //sOutPut = "<TABLE><TR>";
    	
    	//pwOut.println(sOutPut);
	    
	    //Add function list
		try{
			//First get a list of all the security functions:
	        String sSQL = TimeCardSQLs.Get_Access_Control_Function_List();
	        ResultSet rsFunctions = clsDatabaseFunctions.openResultSet(sSQL, getServletContext(), sConf);
        	
	        sSQL = TimeCardSQLs.Get_Access_Control_Group_Functions_SQL(sGroup);
	        ResultSet rsGroupFunctions = clsDatabaseFunctions.openResultSet(sSQL, getServletContext(), sConf);
	        
	        String sCheckedOrNot = "";
	        ArrayList<String> sFunctionTable = new ArrayList<String>(0);
	        
	        //System.out.println("iFunctionCount = " + iFunctionCount + ", iRowCount = " + iRowCount + ", iMod = " + iMod);
	        while (rsFunctions.next()){
	        	sCheckedOrNot = Is_Function_In_Group(rsFunctions.getString(ACFunctions.sFunctionName), rsGroupFunctions);
	        	sFunctionTable.add((String) "<INPUT TYPE=CHECKBOX " + sCheckedOrNot + " NAME=\"Function***Update" +  rsFunctions.getString(ACFunctions.sFunctionName) + "\">" + rsFunctions.getString(ACFunctions.sFunctionDesc));
	        	//System.out.println("Added element: " + "<INPUT TYPE=CHECKBOX " + sCheckedOrNot + "\" NAME=\"Function***Update" +  rsFunctions.getString("sFunctionName") + "\">" + rsFunctions.getString("sFunctionName"));
        	}
	        rsFunctions.close();
	        rsGroupFunctions.close();
	        //pwOut.println(SMUtilities.Build_HTML_Table(3, sFunctionTable));
	        pwOut.println(TimeCardUtilities.Build_HTML_Table(3, sFunctionTable,1,false));
	        
        	//End the drop down list:
        	//pwOut.println ("</TABLE>");
        	pwOut.println ("<BR>");
		}catch (SQLException ex){
	    	System.out.println("Error in ManageAccessControlGroupsEdit class!!");
	        System.out.println("SQLException: " + ex.getMessage());
	        System.out.println("SQLState: " + ex.getSQLState());
	        System.out.println("SQL: " + ex.getErrorCode());
			//return false;
		}

		// Add the user list:
		//********************
		pwOut.println("<B>Select Users To Include In Group '" + sGroup + "':</B>");
	    
	    //Add user list
		ArrayList<String> sUserTable = new ArrayList<String>(0);
		try{
			//First get a list of all the users:
	        String sSQL = TimeCardSQLs.Get_Employee_List_SQL(false);
	        ResultSet rsEmployees = clsDatabaseFunctions.openResultSet(sSQL, getServletContext(), sConf);
        	
	        sSQL = TimeCardSQLs.Get_Access_Control_Group_Users_SQL(sGroup);
	        ResultSet rsGroupUsers = clsDatabaseFunctions.openResultSet(sSQL, getServletContext(), sConf);
	        
	        String sCheckedOrNot = "";
        	while (rsEmployees.next()){
        		sCheckedOrNot = Is_User_In_Group(rsEmployees.getString(Employees.sEmployeeID), rsGroupUsers);
        		sUserTable.add((String) "<INPUT TYPE=CHECKBOX " + sCheckedOrNot + " NAME=\"User***Update" +  rsEmployees.getString(Employees.sEmployeeID) + "\">" + rsEmployees.getString(Employees.sEmployeeLastName) + ", " + rsEmployees.getString(Employees.sEmployeeFirstName));
        	}
        	rsEmployees.close();
        	rsGroupUsers.close();
        	//Print the table:
        	pwOut.println(TimeCardUtilities.Build_HTML_Table(5, sUserTable,1,false));
        	
		}catch (SQLException ex){
	    	System.out.println("Error in ManageACGroupsEdit class!!");
	        System.out.println("SQLException: " + ex.getMessage());
	        System.out.println("SQLState: " + ex.getSQLState());
	        System.out.println("SQL: " + ex.getErrorCode());
			//return false;
		}
		
		//********************
		
		sOutPut = "<P><INPUT TYPE=SUBMIT NAME='SubmitEdit' VALUE='Update Group' STYLE='height: 0.24in'></P>";
		sOutPut = sOutPut + "</FORM>";
		pwOut.println(sOutPut);
		
	}
	
	private boolean Delete_Group(
			String sGroup,
			PrintWriter pwOut,
			String sConf){
		
		ArrayList<String> sSQLList = new ArrayList<String>(0);
		
		sSQLList.add((String) TimeCardSQLs.Delete_Group_Functions_SQL(sGroup));
		sSQLList.add((String) TimeCardSQLs.Delete_Group_Users_SQL(sGroup));
		sSQLList.add((String) TimeCardSQLs.Delete_Group_SQL(sGroup));
		try {
			boolean bResult = clsDatabaseFunctions.executeSQLsInTransaction(sSQLList, getServletContext(), sConf);
			return bResult;
		}catch (SQLException ex){
	    	System.out.println("Error in SMManageSecurityGroupsEdit.Delete_Group class!!");
	        System.out.println("SQLException: " + ex.getMessage());
	        System.out.println("SQLState: " + ex.getSQLState());
	        System.out.println("SQL: " + ex.getErrorCode());
			return false;
		}		
	}
	
	private boolean Add_Group(String sGroup, String sConf, PrintWriter pwOut){
		
		//First, make sure there isn't a group by this name already:
		String sSQL = TimeCardSQLs.Get_Access_Control_Group_SQL(sGroup);
		
		try{
			ResultSet rs = clsDatabaseFunctions.openResultSet(sSQL, getServletContext(), sConf);
			if (rs.next()){
				//This group already exists, so we can't add it:
				pwOut.println("The " + sGroup + " group already exists - it cannot be added.<BR>");
				return false;
			}
			rs.close();
		}catch(SQLException ex){
	    	System.out.println("Error in ManageACGroupsEdit.Add_Group class!!");
	        System.out.println("SQLException: " + ex.getMessage());
	        System.out.println("SQLState: " + ex.getSQLState());
	        System.out.println("SQL: " + ex.getErrorCode());
			return false;
		}
		sSQL = TimeCardSQLs.Add_New_Group_SQL(sGroup);
		try {
			
			boolean bResult = clsDatabaseFunctions.executeSQL(sSQL, getServletContext(), sConf); 
			return bResult;
		}catch (SQLException ex){
	    	System.out.println("Error in ManageACGroupsEdit.Add_Group class!!");
	        System.out.println("SQLException: " + ex.getMessage());
	        System.out.println("SQLState: " + ex.getSQLState());
	        System.out.println("SQL: " + ex.getErrorCode());
			return false;
		}
		
	}
	
	public void doGet(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
		
		doPost(request, response);
	}
}
