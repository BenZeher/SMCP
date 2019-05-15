package smcontrolpanel;

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
import SMDataDefinition.SMTablesecurityfunctions;
import SMDataDefinition.SMTablesecuritygroupfunctions;
import SMDataDefinition.SMTableusers;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsStringFunctions;

public class SMManageSecurityGroupsEdit extends HttpServlet {

	private static final long serialVersionUID = 1L;
	@Override
	public void doPost(HttpServletRequest request,
				HttpServletResponse response)
				throws ServletException, IOException {

		if (!SMAuthenticate.authenticateSMCPCredentials(
				request, 
				response, 
				getServletContext(),
				SMSystemFunctions.SMManageSecurityGroups
			)
		){
			return;
		}
		
	    //Get the session info:
	    HttpSession CurrentSession = request.getSession(true);
	    String sDBID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_DATABASE_ID);
	    String sCompanyName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_COMPANYNAME);
	    String sLicenseModuleLevel = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_LICENSE_MODULE_LEVEL);
	    String sGroupName = clsStringFunctions.filter(request.getParameter("Security Groups"));
		PrintWriter out = response.getWriter();
		
		String title = "";
		String subtitle = "";
		
	    if(request.getParameter("SubmitEdit") != null){
	    	//User has chosen to edit an existing group:
			title = "Edit Security Group: " + sGroupName;
		    subtitle = "";
		    out.println(SMUtilities.SMCPTitleSubBGColor(title, subtitle, SMUtilities.getInitBackGroundColor(getServletContext(), sDBID), sCompanyName));

		    //Print a link to the first page after login:
		    out.println("<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMUserLogin?" 
				+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
				+ "\">Return to user login</A><BR><BR>");
			
		    Edit_Group(sGroupName, out, sDBID, false, sLicenseModuleLevel);
	    }
	    if(request.getParameter("SubmitDelete") != null){
	    	//User has chosen to delete an existing group:
			title = "Delete Security Group: " + sGroupName;
		    subtitle = "";
		    out.println(SMUtilities.SMCPTitleSubBGColor(title, subtitle, SMUtilities.getInitBackGroundColor(getServletContext(), sDBID), sCompanyName));

		    //Print a link to the first page after login:
		    out.println("<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMUserLogin?" 
				+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
				+ "\">Return to user login</A><BR><BR>");
			
		    if (request.getParameter("ConfirmDelete") == null){
		    	out.println ("You must check the 'confirming' check box to delete a group.");
		    }
		    else{
			    if (Delete_Group(sGroupName, out, sDBID) == false){
			    	out.println ("Error deleting group " + sGroupName + ".");
			    }
			    else{
			    	out.println ("Successfully deleted group " + sGroupName + ".");
			    }
		    }
	    }
	    if(request.getParameter("SubmitAdd") != null){
	    	
		    String sNewGroupName = clsStringFunctions.filter(request.getParameter("NewGroupName"));
	    	//User has chosen to add a new group:
			title = "Add Security Group: " + sNewGroupName;
		    subtitle = "";
		    out.println(SMUtilities.SMCPTitleSubBGColor(title, subtitle, SMUtilities.getInitBackGroundColor(getServletContext(), sDBID), sCompanyName));

		    //Print a link to the first page after login:
		    out.println("<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMUserLogin?" 
				+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
				+ "\">Return to user login</A><BR><BR>");

		    if (sNewGroupName == ""){
		    	out.println ("You chose to add a new group, but you did not enter a new group name to add.");
		    }
		    else{
		    	Edit_Group(sNewGroupName, out, sDBID, true, sLicenseModuleLevel);
		    }
	    }
		
		out.println("</BODY></HTML>");
	}
	
	public String Is_Function_In_Group(int iFunction, ResultSet rs){
		
		try {
			// Set this recordset to the beginning every time:
			rs.beforeFirst();
			while (rs.next()){
				if (rs.getInt(SMTablesecuritygroupfunctions.ifunctionid) == iFunction){
					return "checked=\"Yes\"";
				}
			}
			//rs.close();
			//If we never found a matching record, return 'NO':
			return "";
		}catch (SQLException ex){
	    	System.out.println("Error in SMManageSecurityGroupsEdit class in Is_Function_In_Group!!");
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
				if (rs.getString("sUserName").compareTo(sUser) == 0){
					return "checked=\"Yes\"";
				}
			}
			//If we never found a matching record, return:
			return "";
		}catch (SQLException ex){
	    	System.out.println("Error in SMManageSecurityGroupsEdit class in Is_User_In_Group!!");
	        System.out.println("SQLException: " + ex.getMessage());
	        System.out.println("SQLState: " + ex.getSQLState());
	        System.out.println("SQL: " + ex.getErrorCode());
			return "";
		}
	}

	private void Edit_Group(
			String sGroup, 
			PrintWriter pwOut, 
			String sDBID,
			boolean bAddGroup,
			String sLicenseModuleLevel){
	    
		//first, add the group if it's an 'Add':
		if (bAddGroup == true){
			if (Add_Group (sGroup, sDBID, pwOut) == false){
				pwOut.println("ERROR - Could not add group " + sGroup + ".<BR>");
				return;
			}
		}
		
		pwOut.println("<FORM NAME='MAINFORM' ACTION='" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMManageSecurityGroupsAction' METHOD='POST'>");
		pwOut.println("<INPUT TYPE=HIDDEN NAME='" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "' VALUE='" + sDBID + "'>");
		pwOut.println("<INPUT TYPE=HIDDEN NAME=\"GroupName\" VALUE=\"" + sGroup + "\">");
		pwOut.println("<INPUT TYPE=HIDDEN NAME=\"CallingClass\" VALUE=\"" + SMUtilities.getFullClassName(this.toString()) + "\">");
	    String sOutPut = "";
	    pwOut.println("<P><INPUT TYPE=SUBMIT NAME='SubmitEdit' VALUE='Update Group' STYLE='height: 0.24in'></P>");
	    pwOut.println("<B>Select Functions To Include In Group '" + sGroup + "':</B>");
	    
	    //Add function list
		try{
			//First get a list of all the security functions:
	        String sSQL = "SELECT * FROM " + SMTablesecurityfunctions.TableName 
	        	+ " WHERE ("
	        		+ "((" + SMTablesecurityfunctions.imodulelevelsum + " & " + sLicenseModuleLevel + ") > 0)"
	        	+ ")"
	        	+ " ORDER BY " + SMTablesecurityfunctions.sFunctionName;
	        ResultSet rsFunctions = clsDatabaseFunctions.openResultSet(sSQL, getServletContext(), sDBID);
        	
	        sSQL = SMMySQLs.Get_Security_Group_Functions_SQL(sGroup);
	        ResultSet rsGroupFunctions = clsDatabaseFunctions.openResultSet(sSQL, getServletContext(), sDBID);
	        
	        String sCheckedOrNot = "";
	        ArrayList<String> sFunctionTable = new ArrayList<String>(0);
	        
	        //System.out.println("iFunctionCount = " + iFunctionCount + ", iRowCount = " + iRowCount + ", iMod = " + iMod);
	        while (rsFunctions.next()){
	        	sCheckedOrNot = Is_Function_In_Group(rsFunctions.getInt(SMTablesecurityfunctions.iFunctionID), rsGroupFunctions);
	        	sFunctionTable.add((String) "<INPUT TYPE=CHECKBOX " + sCheckedOrNot 
	        			+ " NAME=\"Function***Update" 
	        			+ Integer.toString(rsFunctions.getInt(SMTablesecurityfunctions.iFunctionID)) 
	        			+ "\">" 
	        			+ "<span"
	        			+ " title=\"" 
	        			+ rsFunctions.getString(SMTablesecurityfunctions.sDescription) 
	        			+ "\""
	        			+ ">"
	        			+ rsFunctions.getString("sFunctionName")
	        			+ " (" + Integer.toString(rsFunctions.getInt(SMTablesecurityfunctions.iFunctionID)) + ")"
	        			+ "</span>"
	        			);
	        	//System.out.println("Added element: " + "<INPUT TYPE=CHECKBOX " + sCheckedOrNot + "\" NAME=\"Function***Update" +  rsFunctions.getString("sFunctionName") + "\">" + rsFunctions.getString("sFunctionName"));
        	}
	        rsFunctions.close();
	        rsGroupFunctions.close();
	        pwOut.println(SMUtilities.Build_HTML_Table(3, sFunctionTable,1,true));
	        
        	//End the drop down list:
        	//pwOut.println ("</TABLE>");
        	pwOut.println ("<BR>");
		}catch (SQLException ex){
	    	System.out.println("Error in SMManageSecurityGroupsEdit class!!");
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
	        String sSQL = SMMySQLs.Get_User_List_SQL(false);
	        ResultSet rsUsers = clsDatabaseFunctions.openResultSet(sSQL, getServletContext(), sDBID);
        	
	        sSQL = SMMySQLs.Get_Security_Group_Users_SQL(sGroup);
	        ResultSet rsGroupUsers = clsDatabaseFunctions.openResultSet(sSQL, getServletContext(), sDBID);
	        
	        String sCheckedOrNot = "";
        	while (rsUsers.next()){
        		sCheckedOrNot = Is_User_In_Group(rsUsers.getString("sUserName"), rsGroupUsers);
        		sUserTable.add((String) "<LABEL><INPUT TYPE=CHECKBOX " + sCheckedOrNot + " NAME=\"User***Update" 
        			+  rsUsers.getString(SMTableusers.lid) + "\"></LABEL>" 
        			+ rsUsers.getString(SMTableusers.sUserFirstName) 
        			+ " " + rsUsers.getString(SMTableusers.sUserLastName)
        			+ " (" + rsUsers.getString("sUserName") + ")" 
        		);
        	}
        	rsUsers.close();
        	rsGroupUsers.close();
        	//Print the table:
        	pwOut.println(SMUtilities.Build_HTML_Table(3, sUserTable,1,true));
        	
		}catch (SQLException ex){
	    	System.out.println("Error in SMManageSecurityGroupsEdit class!!");
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
			String sDBID){
		
		ArrayList<String> sSQLList = new ArrayList<String>(0);
		
		sSQLList.add((String) SMMySQLs.Delete_Group_Functions_SQL(sGroup));
		sSQLList.add((String) SMMySQLs.Delete_Group_Users_SQL(sGroup));
		sSQLList.add((String) SMMySQLs.Delete_Group_SQL(sGroup));
		try {
			boolean bResult = clsDatabaseFunctions.executeSQLsInTransaction(sSQLList, getServletContext(), sDBID);
			return bResult;
		}catch (SQLException ex){
	    	System.out.println("Error in SMManageSecurityGroupsEdit.Delete_Group class!!");
	        System.out.println("SQLException: " + ex.getMessage());
	        System.out.println("SQLState: " + ex.getSQLState());
	        System.out.println("SQL: " + ex.getErrorCode());
			return false;
		}		
	}
	
	private boolean Add_Group(String sGroup, String sDBID, PrintWriter pwOut){
		
		//First, make sure there isn't a group by this name already:
		String sSQL = SMMySQLs.Get_Security_Group_SQL(sGroup);
		
		try{
			ResultSet rs = clsDatabaseFunctions.openResultSet(sSQL, getServletContext(), sDBID);
			if (rs.next()){
				//This group already exists, so we can't add it:
				pwOut.println("The " + sGroup + " group already exists - it cannot be added.<BR>");
				rs.close();
				return false;
			}
			rs.close();
		}catch(SQLException ex){
	    	System.out.println("Error in SMManageSecurityGroupsEdit.Add_Group class!!");
	        System.out.println("SQLException: " + ex.getMessage());
	        System.out.println("SQLState: " + ex.getSQLState());
	        System.out.println("SQL: " + ex.getErrorCode());
			return false;
		}
		sSQL = SMMySQLs.Add_New_Group_SQL(sGroup);
		try {
			
			boolean bResult = clsDatabaseFunctions.executeSQL(sSQL, getServletContext(), sDBID); 
			return bResult;
		}catch (SQLException ex){
	    	System.out.println("Error in SMManageSecurityGroupsEdit.Add_Group class!!");
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
