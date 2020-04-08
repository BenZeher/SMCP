package smcontrolpanel;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URLEncoder;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import SMClasses.SMUser;
import SMDataDefinition.SMModuleListing;
import SMDataDefinition.SMTableappointmentgroups;
import SMDataDefinition.SMTableappointmentusergroups;
import SMDataDefinition.SMTablecolortable;
import SMDataDefinition.SMTablecustomlinks;
import SMDataDefinition.SMTablemechanics;
import SMDataDefinition.SMTablesalesperson;
import SMDataDefinition.SMTablesecuritygroups;
import SMDataDefinition.SMTablesecurityusergroups;
import SMDataDefinition.SMTablessalarmsequences;
import SMDataDefinition.SMTablessalarmsequenceusers;
import SMDataDefinition.SMTablessdevices;
import SMDataDefinition.SMTablessdeviceusers;
import SMDataDefinition.SMTableusers;
import SMDataDefinition.SMTableuserscustomlinks;
import ServletUtilities.clsCreateHTMLTableFormFields;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsManageRequestParameters;
import ServletUtilities.clsStringFunctions;

public class SMEditUsersEdit extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
	public static final String DELETE_CUSTOM_LINK_BUTTON_NAME = "DELETECUSTOMLINK";
	public static final String ADD_CUSTOM_LINK_BUTTON_NAME = "ADDCUSTOMLINK";
	
	public static final String UPDATE_SECURITY_GROUPS_PREFIX = "SecurityGroup***Update";
	public static final String UPDATE_AS_DEVICES_PREFIX = "DeviceUser***Update";
	public static final String UPDATE_AS_ALARM_SEQUENCES_PREFIX = "AlarmSequence***Update";
	public static final String UPDATE_APPOINTMENT_GROUP_PREFIX = "AppointmentGroup***Update";
	public static final String UPDATE_CUSTOM_LINK_PREFIX = "CustomLink***Update";
	
	public static final String UPDATE_USER_BUTTON_NAME = "SubmitEdit";
	public static final String UPDATE_USER_BUTTON_VALUE = "Update User";
	
	public static final String DELETE_USER_BUTTON_NAME = "SubmitDelete";
	public static final String DELETE_USER_BUTTON_VALUE = "Delete User";
	
	
	public void doPost(HttpServletRequest request,
				HttpServletResponse response)
				throws ServletException, IOException {

		PrintWriter out = response.getWriter();
		if (!SMAuthenticate.authenticateSMCPCredentials(
				request, 
				response, 
				getServletContext(), 
				SMSystemFunctions.SMEditUsers))
		{
			return;
		}

	    //Get the session info:
	    HttpSession CurrentSession = request.getSession(true);
	    String sDBID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_DATABASE_ID);
	    String sCompanyName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_COMPANYNAME);
	    String sCurrentUser = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERNAME);
	    String sUserID = (String)CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERID);
	    String sUserFullName = (String)CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERFIRSTNAME) + " "
	    				+ (String)CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERLASTNAME);
	     
	    //Get the user ID passed in and see if this is a new record
	    SMUser userentry = new SMUser(request, getServletContext(), sDBID);
	    //Load from the object if it exists
	    if (CurrentSession.getAttribute(SMUser.ParamObjectName) != null){
	    	userentry = (SMUser) CurrentSession.getAttribute(SMUser.ParamObjectName);
	    	CurrentSession.removeAttribute(SMUser.ParamObjectName);
		}else{
			if(!userentry.bIsNewRecord()){
				try {
					userentry.load(getServletContext(), sDBID, sCurrentUser, sUserID, sUserFullName);
				} catch (Exception e1) {
		    		response.sendRedirect(SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMEditUsersSelection?"
			    			+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
			    			+ "&Error=" + URLEncoder.encode(e1.getMessage() , "UTF-8")
			    			);
		    		return;
				}
			}
		}
		
		String sWarning = clsManageRequestParameters.get_Request_Parameter("Warning", request);
		String sStatus = clsManageRequestParameters.get_Request_Parameter("Status", request);;
	    /*TODO NOTE: The start of this class acts as an action class for the SMEditUsersSelection
	     * These actions should be moved to SMEditUsersAction when sUsersName field can be edited.. That is
	     * once the users ID field is being used throughout SMCP
	    */

		//If it is a request to edit a user from the selection class
	    if(request.getParameter(SMEditUsersSelection.EDIT_USER_BUTTON_NAME) != null){
	    	if(userentry.getlid().compareToIgnoreCase("-1") != 0){
	    	    try {
	    	    	Edit_User_HTML_Header(userentry, out, sWarning, sStatus, sCompanyName, sDBID);
	    	    	Edit_User_HTML(userentry, out, sDBID);
					return;
				} catch (Exception e) {
		    		response.sendRedirect(SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMEditUsersSelection?"
			    			+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
			    			+ "&Warning=" + URLEncoder.encode("Loading edit users HTML:  " + e.getMessage() , "UTF-8")
			    			);
		    		return;
				}
	    	}else{
		    	response.sendRedirect(SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMEditUsersSelection?"
		    			+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
		    			+ "&Warning=" + URLEncoder.encode("Can not find user ID to load edit screen.", "UTF-8")
		    			);
		    	return;
	    	}
	    }
	    
	    //If this class is returning from it's action class.
	    try {
	    	Edit_User_HTML_Header(userentry, out, sWarning, sStatus, sCompanyName, sDBID);
        	Edit_User_HTML(userentry, out, sDBID);
		} catch (Exception e) {
			response.sendRedirect(SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMEditUsersSelection?"
	    			+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
	    			+ "&Error=" + URLEncoder.encode("Loading edit users HTML:  " + e.getMessage() , "UTF-8")
	    			);
    		return; 
		}
		return;
	}

	private void Edit_User_HTML_Header(
			SMUser userentry, 
			PrintWriter out, 
			String sWarning,
			String sStatus,
			String sCompanyName,
			String sDBID) throws Exception{
		
		String title = "Edit User - " + userentry.getsUserName();
		String subtitle = "";
    	out.println(SMUtilities.SMCPTitleSubBGColor(title, subtitle, SMUtilities.getInitBackGroundColor(getServletContext(), sDBID), sCompanyName));
    	if(sWarning.compareToIgnoreCase("") != 0){
    		out.println("<FONT COLOR=\"RED\"><B>Warning: " + sWarning + "</B></FONT><BR>");
    	}
    	if(sStatus.compareToIgnoreCase("") != 0){
    		out.println("<FONT><B>Status: " + sStatus + "</B></FONT><BR>");
    	}
	}
	private void Edit_User_HTML(
			SMUser userentry, 
			PrintWriter out, 
			String sDBID) throws Exception{

	    //Print a link to the first page after login:
	    out.println("<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMUserLogin?" 
			+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
			+ "\">Return to user login</A><BR>");
	    out.println("<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMEditUsersSelection?" 
				+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
				+ "\">Return to Edit User</A><BR><BR>");
	    out.println(SMUtilities.getColorPickerIncludeString(getServletContext()));
	    //LTO 20120814 color picker for users.
		out.println("<script type='text/javascript' src='/sm/scripts/jquery-1.8.0.min.js'></script>");
		out.println("<script type='text/javascript' src='/sm/scripts/jquery.simple-color.js'></script>");
		out.println(get_javascript_validation());
		
	    //Get license level to control content displayed
	    long lLicenseModuleLevel = 0;
		try {
			lLicenseModuleLevel = Long.parseLong(SMUtilities.getSMCPModuleLevel(getServletContext(), sDBID));
		} catch (Exception e1) {
			out.println("ERROR - Could not read license file.<BR>");
		}
		
		out.println("<FORM NAME='MAINFORM' ACTION='" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMEditUsersAction' ONSUBMIT=\" return checkInput()\" METHOD='POST'>");
		out.println("<INPUT TYPE=HIDDEN NAME='" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "' VALUE='" + sDBID + "'>");
		out.println("<INPUT TYPE=HIDDEN NAME=\"" + SMUser.Paramlid + "\" VALUE=\"" + userentry.getlid() + "\">");
		
	    String sOutPut = "";
	    String sSQL = "";
        sOutPut = "<TABLE BORDER=12 CELLSPACING=2>";
        
		//ID
		sOutPut += "<TR>";	
		sOutPut += "<TD ALIGN=RIGHT><B>UserID: </B></TD>";  
		sOutPut += "<TD ALIGN=LEFT>";
		if(userentry.bIsNewRecord()) {
			sOutPut +=  "NEW";
		}else {
			sOutPut +=  clsStringFunctions.filter(userentry.getlid());
		}
		
		sOutPut += "</TD>";
		sOutPut += "<TD ALIGN=LEFT>This is a generated unique ID for this user. </TD>";
		sOutPut += "</TR>";	

		//Username:
		sOutPut += "<TR>";
		sOutPut += "<TD ALIGN=RIGHT><B>Username<FONT COLOR=\"RED\">*</FONT>: </B></TD>";
		
		sOutPut += "<TD ALIGN=LEFT>";
		sOutPut += "<INPUT TYPE=TEXT ID = \"" + SMTableusers.sUserName + "\" "
				+ " NAME=\"" + SMUser.ParamsUserName + "\"";
		sOutPut += " VALUE=\"" + clsStringFunctions.filter(userentry.getsUserName()) + "\"";
		sOutPut += "SIZE=28";
		sOutPut += " MAXLENGTH=" + Integer.toString(SMTableusers.sUserUserNameLength);
		sOutPut += " STYLE=\"width: 1.2in; height: 0.25in\" ";
		sOutPut += "></TD>";
		
		sOutPut += "<TD ALIGN=LEFT>Enter a <i>unique</i> username that will be used to log into SMCP.</TD>";
		sOutPut += "</TR>";

		//First name:
		sOutPut += "<TR>";
		sOutPut += "<TD ALIGN=RIGHT><B>First name<FONT COLOR=\"RED\">*</FONT>: </B></TD>";
		
		sOutPut += "<TD ALIGN=LEFT>";
		sOutPut += "<INPUT TYPE=TEXT ID = \"" + SMTableusers.sUserFirstName + "\" "
				+ " NAME=\"" + SMUser.ParamsUserFirstName + "\"";
		sOutPut += " VALUE=\"" + clsStringFunctions.filter(userentry.getsUserFirstName()) + "\"";
		sOutPut += "SIZE=28";
		sOutPut += " MAXLENGTH=" + Integer.toString(SMTableusers.sUserUserFirstNameLength);
		sOutPut += " STYLE=\"width: 2.41in; height: 0.25in\"";
		sOutPut += "></TD>";
		
		sOutPut += "<TD ALIGN=LEFT>Enter the user's first name</TD>";
		sOutPut += "</TR>";
		
		//Last name:
		sOutPut += "<TR>";
		sOutPut += "<TD ALIGN=RIGHT><B>Last name<FONT COLOR=\"RED\">*</FONT>: </B></TD>";
		
		sOutPut += "<TD ALIGN=LEFT>";
		sOutPut += "<INPUT TYPE=TEXT ID = \"" + SMTableusers.sUserLastName + "\" "
				+ "NAME=\"" + SMUser.ParamsUserLastName + "\"";
		sOutPut += " VALUE=\"" + clsStringFunctions.filter(userentry.getsUserLastName()) + "\"";
		sOutPut += "SIZE=28";
		sOutPut += " MAXLENGTH=" + Integer.toString(SMTableusers.sUserUserLastNameLength);
		sOutPut += " STYLE=\"width: 2.41in; height: 0.25in\"";
		sOutPut += "></TD>";
		
		sOutPut += "<TD ALIGN=LEFT>Enter the user's last name</TD>";
		sOutPut += "</TR>";

		//Initials:
		sOutPut += "<TR>";
		sOutPut += "<TD ALIGN=RIGHT><B>User initials: </B></TD>";
		
		sOutPut += "<TD ALIGN=LEFT>";
		sOutPut += "<INPUT TYPE=TEXT NAME=\"" + SMUser.ParamsIdentifierInitials + "\"";
		sOutPut += " VALUE=\"" + clsStringFunctions.filter(userentry.getsIdentifierInitials()) + "\"";
		sOutPut += "SIZE=28";
		sOutPut += " MAXLENGTH=" + Integer.toString(SMTableusers.sUserIdentifierInitialsLength);
		sOutPut += " STYLE=\"width: 2.41in; height: 0.25in\"";
		sOutPut += "></TD>";
		
		sOutPut += "<TD ALIGN=LEFT>Default user initials</TD>";
		sOutPut += "</TR>";    
		

		//Email address:
		sOutPut += "<TR>";
		sOutPut += "<TD ALIGN=RIGHT><B>Email address: </B></TD>";
		
		sOutPut += "<TD ALIGN=LEFT>";
		sOutPut += "<INPUT TYPE=TEXT ID =\""+SMTableusers.semail +"\" NAME=\"" + SMUser.Paramsemail + "\"";
		sOutPut += " VALUE=\"" + clsStringFunctions.filter(userentry.getsemail()) + "\"";
		sOutPut += "SIZE=28";
		sOutPut += " MAXLENGTH=" + Integer.toString(SMTableusers.semailLength);
		sOutPut += " STYLE=\"width: 2.41in; height: 0.25in\"";
		sOutPut += "></TD>";
		
		sOutPut += "<TD ALIGN=LEFT>Address for sending system notices</TD>";
		sOutPut += "</TR>";
		
		//Salesperson code
		sOutPut += "<TR>";
		sOutPut += "<TD ALIGN=RIGHT><B>Default salesperson code: </B></TD>";
		sOutPut += "<TD ALIGN=LEFT>";	        
		try{
		    sSQL = "SELECT"
		    	+ " " + SMTablesalesperson.sSalespersonCode
		    	+ ", " + SMTablesalesperson.sSalespersonFirstName
		    	+ ", " +	SMTablesalesperson.sSalespersonLastName 
		    	+ " FROM " + SMTablesalesperson.TableName
		    	+ " ORDER BY " + SMTablesalesperson.sSalespersonCode 
		    ;
		    ResultSet rsSalespersons = clsDatabaseFunctions.openResultSet(sSQL, getServletContext(), sDBID);
		    sOutPut += "<SELECT NAME=\"" + SMUser.ParamsDefaultSalespersonCode + "\">"; 
			sOutPut += "<OPTION VALUE=\"" + "" + "\">** NONE SELECTED **</OPTION>"; 
			while (rsSalespersons.next()){
				sOutPut += "<OPTION VALUE=\"" + rsSalespersons.getString(SMTablesalesperson.sSalespersonCode) + "\"";
				if (userentry.getsDefaultSalespersonCode().compareToIgnoreCase(rsSalespersons.getString(SMTablesalesperson.sSalespersonCode)) == 0){
					sOutPut += " SELECTED";
				}
				sOutPut += ">";
				sOutPut += rsSalespersons.getString(SMTablesalesperson.sSalespersonCode) 
					+ " - " + rsSalespersons.getString(SMTablesalesperson.sSalespersonFirstName) 
					+ " " + rsSalespersons.getString(SMTablesalesperson.sSalespersonLastName);
			}
			rsSalespersons.close();
		    //End the drop down list:
		    sOutPut += "</SELECT>&nbsp;&nbsp;";
		    sOutPut += "<input type=\"button\" target=\"_blank\" "
		    		+ "onclick=\"window.open(\'" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMEditSalesperson " + "', '_blank')\" "
		    		+ "value=\"Edit Salespersons\" />";
		    sOutPut += "</TD><TD ALIGN=LEFT>Salesperson that appears when starting an order.</TD>";
		    sOutPut += "</TR>";

		}catch (SQLException ex){
			System.out.println("[1586348039] Error in SMEditUsers class - reading salespersons");
		    System.out.println("SQLException: " + ex.getMessage());
		    System.out.println("SQLState: " + ex.getSQLState());
		    System.out.println("SQL: " + ex.getErrorCode());
			//return false;
		}

		//Technician's initials: UPDATED 10-28-14 SCO
		sOutPut += "<TR>";
		sOutPut += "<TD ALIGN=RIGHT><B>Technician's initials: </B></TD>";
		sOutPut += "<TD ALIGN=LEFT>";	        
		try{
		    sSQL = "SELECT"
		    	+ " " + SMTablemechanics.sMechInitial
		    	+ ", " + SMTablemechanics.sMechFullName
		    	+ ", " +	SMTablemechanics.lid 
		    	+ " FROM " + SMTablemechanics.TableName
		    	+ " ORDER BY " + SMTablemechanics.sMechInitial 
		    ;
		    ResultSet rsTechnicians = clsDatabaseFunctions.openResultSet(sSQL, getServletContext(), sDBID);
		    sOutPut += "<SELECT NAME=\"" + SMTableusers.smechanicinitials + "\">"; 
			sOutPut += "<OPTION VALUE=\"" + "" + "\">** NONE SELECTED **</OPTION>"; 
			while (rsTechnicians.next()){
				sOutPut += "<OPTION VALUE=\"" + rsTechnicians.getString(SMTablemechanics.sMechInitial) + "\"";
				if (userentry.getsmechanicinitials().compareToIgnoreCase(rsTechnicians.getString(SMTablemechanics.sMechInitial)) == 0){
					sOutPut += " SELECTED";
				}
				sOutPut += ">";
				sOutPut += rsTechnicians.getString(SMTablemechanics.sMechInitial) + " - " 
					+ rsTechnicians.getString(SMTablemechanics.sMechFullName);
			}
			rsTechnicians.close();
		    //End the drop down list:
		    sOutPut += "</SELECT>&nbsp;&nbsp;";
		    sOutPut += "<input type=\"button\""
		    		+ "onclick=\"window.open('" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMEditMechanics " + "', '_blank')\" "
		    		+ "value=\"Edit Technicians\" />";
		    sOutPut += "</TD><TD ALIGN=LEFT>Choose the technician associated with this user.</TD>";
		    sOutPut += "</TR>";

		}catch (SQLException ex){
			System.out.println("[1579271797] Error in SMEditUsers class - reading mechanics");
		    System.out.println("SQLException: " + ex.getMessage());
		    System.out.println("SQLState: " + ex.getSQLState());
		    System.out.println("SQL: " + ex.getErrorCode());
			//return false;
		}
		
		//Active?
		int iTrueOrFalse = 0;
		if (userentry.getiactive().compareTo("1") == 0){
			iTrueOrFalse = 1;
		}else{
			iTrueOrFalse = 0;
		}
		sOutPut += clsCreateHTMLTableFormFields.Create_Edit_Form_Checkbox_Row(
			SMTableusers.iactive, 
			iTrueOrFalse, 
			"Active user?", 
			"Uncheck to de-activate this user.  Making a user inactive will prevent them from logging in to the system."
			);
		//Users color code
		sOutPut += "<TR><TD ALIGN=RIGHT><B>User's icon color code:</B></TD>";
		sOutPut += "<TD>"; 

		int iRow = 0;
		int iCol = 0;
		try{
		    sSQL = "SELECT"
		    	+ " " + SMTableusers.susercolorcodecol
		    	+ ", " + SMTableusers.susercolorcoderow
		    	+ " FROM " + SMTableusers.TableName
		    	+ " WHERE " + SMTableusers.lid + "=" + userentry.getlid() + "" 
		    ;
		    ResultSet rsColor = clsDatabaseFunctions.openResultSet(sSQL, getServletContext(), sDBID);

			if (rsColor.next()){
				iRow = rsColor.getInt(SMTableusers.susercolorcoderow);
				iCol = rsColor.getInt(SMTableusers.susercolorcodecol);	        		
			}
			rsColor.close();
		}catch (SQLException ex){
			sOutPut += "Error getting users color code - " + ex.getMessage();
		}
		try{
		    sOutPut += print_user_color_selection_map(
		    			iRow,
		    			iCol, 
					    false,
					    getServletContext(),
					    sDBID);
		}catch (Exception e){
			sOutPut += "Failed to print color selection -" + e.getMessage();
		}

		sOutPut += "</TD><TD VALIGN=CENTRE>" + "Select a color for users icon on appointment calendar map. " + "</TD>"
				+ "</TR>";
		
		
		//Users Custom Links:
		sOutPut += "<TR>";
		sOutPut += "<TD ALIGN=RIGHT><B>Users custom links: </B></TD>";
		try{
		    sSQL = "SELECT"
			    	+ " " + SMTablecustomlinks.lid
			    	+ ", " + SMTablecustomlinks.surl
			    	+ ", " + SMTablecustomlinks.surlname
			    	+ " FROM " + SMTablecustomlinks.TableName
		    ;
		    ResultSet rsCustomLinks = clsDatabaseFunctions.openResultSet(sSQL, getServletContext(), sDBID);
		    String sCheckedOrNot = "";
		    sOutPut += "<TD>";
		    ArrayList<String> sUsersCustomLinksTable = new ArrayList<String>(0);
			while (rsCustomLinks.next()){
				String sCurrentURLID = rsCustomLinks.getString(SMTablecustomlinks.lid); 
				String sCurrentURL = rsCustomLinks.getString(SMTablecustomlinks.surl);
				String sCurrentURLName = rsCustomLinks.getString(SMTablecustomlinks.surlname );
				sCheckedOrNot = isUserUsingCustomLink(userentry.getlid(), sCurrentURLID, sDBID);
				
				sUsersCustomLinksTable.add("<INPUT TYPE=\"CHECKBOX\"" + sCheckedOrNot + " NAME=\"" + UPDATE_CUSTOM_LINK_PREFIX + sCurrentURLID + "\">" 
				+ "<A HREF=\"" + sCurrentURL + "\">" +sCurrentURLName + "</A>");	 
			}
			sOutPut += SMUtilities.Build_HTML_Table(1, sUsersCustomLinksTable,0,true);
			rsCustomLinks.close();
		}catch (SQLException ex){
			sOutPut += "Error users custom link - " + ex.getMessage();
		}
		sOutPut += "&nbsp;&nbsp;&nbsp;"
				+ "<input type=\"button\" onclick=\"location.href=\'" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMEditUsersCustomLinksEdit " + "\';\" value=\"Edit Custom Links\" />";
		sOutPut += "</TD>";
		sOutPut += "<TD VALIGN=CENTRE>" + "Select links that will appear on this users main menu. " + "</TD>"
				+ "</TR>";
		
		//Select security a groups
		sOutPut += "<TR><TD ALIGN=RIGHT><B>Security groups:</B></TD>";
		sOutPut += "<DIV ID = \"securitygroups\">";
		try{
		    sSQL = "SELECT"
		    	+ " " + SMTablesecuritygroups.sSecurityGroupName
		    	+ " FROM " + SMTablesecuritygroups.TableName
		    ;
		    ResultSet rsSecurityGroups = clsDatabaseFunctions.openResultSet(sSQL, getServletContext(), sDBID);
		    String sCheckedOrNot = "";
		    sOutPut += "<TD>";
		    ArrayList<String> sSecurityGroupsTable = new ArrayList<String>(0);
			while (rsSecurityGroups.next()){
				String sCurrentSecurityGroupName = rsSecurityGroups.getString(SMTablesecuritygroups.sSecurityGroupName);
				sCheckedOrNot = isUserInSecurityGroup(userentry.getlid(), sCurrentSecurityGroupName, sDBID);
				
				sSecurityGroupsTable.add("<LABEL><INPUT TYPE=\"CHECKBOX\"" + sCheckedOrNot + " NAME=\"" + UPDATE_SECURITY_GROUPS_PREFIX + sCurrentSecurityGroupName +"\">" + sCurrentSecurityGroupName+"</LABEL>");	
			}
			sOutPut += SMUtilities.Build_HTML_Table(2, sSecurityGroupsTable,1,true);
			rsSecurityGroups.close();
		}catch (SQLException ex){
			sOutPut += "Error getting security groups - " + ex.getMessage();
		}
		sOutPut += "</TD>";
		sOutPut += "<TD VALIGN=CENTRE>" + "Select security groups user should belong to. " + "</TD>"
				+ "</DIV>"
				+ "</TR>";
		
		//Select appointment calendar groups
		sOutPut += "<TR><TD ALIGN=RIGHT><B>Appointment calendar groups:</B></TD>";

		try{
		    sSQL = "SELECT"
		    	+ " " + SMTableappointmentgroups.sappointmentgroupname
		    	+ " FROM " + SMTableappointmentgroups.TableName
		    ;
		    ResultSet rsAppointmentGroups = clsDatabaseFunctions.openResultSet(sSQL, getServletContext(), sDBID);
		    String sCheckedOrNot = "";
		    sOutPut += "<TD>";
		    ArrayList<String> sAppointmentGroupsTable = new ArrayList<String>(0);
			while (rsAppointmentGroups.next()){
				String sCurrentAppointmentGroupName = rsAppointmentGroups.getString(SMTableappointmentgroups.sappointmentgroupname);
				sCheckedOrNot = isUserInAppointmentGroup(userentry.getlid(), sCurrentAppointmentGroupName, sDBID);
				
				sAppointmentGroupsTable.add("<LABEL><INPUT TYPE=\"CHECKBOX\"" + sCheckedOrNot + " NAME=\"" + UPDATE_APPOINTMENT_GROUP_PREFIX + sCurrentAppointmentGroupName + "\">" + sCurrentAppointmentGroupName+"</LABEL>");	 
			}
			sOutPut += SMUtilities.Build_HTML_Table(1, sAppointmentGroupsTable, 0,true);
			rsAppointmentGroups.close();
		}catch (SQLException ex){
			sOutPut += "Error getting appointment groups - " + ex.getMessage();
		}
		sOutPut += "</TD>";
		sOutPut += "<TD VALIGN=CENTRE>" + "Select appointment calendar groups user should belong to. " + "</TD>"
				+ "</TR>";
		
		//If the alarm system module is present in the license file
		if((lLicenseModuleLevel & SMModuleListing.MODULE_ALARMSYSTEM) > 0){
			//Select alarm sequences to access
			sOutPut += "<TR><TD ALIGN=RIGHT><B>Alarm sequences:</B></TD>";

			try{
				sSQL = "SELECT"
					+ " " + SMTablessalarmsequences.lid 
					+ ", " + SMTablessalarmsequences.sname
					+ " FROM " + SMTablessalarmsequences.TableName
					;
				ResultSet rsAlarmSequences = clsDatabaseFunctions.openResultSet(sSQL, getServletContext(), sDBID);
				String sCheckedOrNot = "";
				sOutPut += "<TD>";
				ArrayList<String> sAlarmSequencesTable = new ArrayList<String>(0);
				while (rsAlarmSequences.next()){
					String sCurrentAlarmSequenceName = rsAlarmSequences.getString(SMTablessalarmsequences.sname);
					String sCurrentAlarmSequenceID = rsAlarmSequences.getString(SMTablessalarmsequences.lid);
					sCheckedOrNot = isUserInAlarmSequence(userentry.getlid(), sCurrentAlarmSequenceID, sDBID);
				
					sAlarmSequencesTable.add("<LABEL><INPUT TYPE=\"CHECKBOX\"" + sCheckedOrNot + " NAME=\"" + UPDATE_AS_ALARM_SEQUENCES_PREFIX + sCurrentAlarmSequenceID + "\">" + sCurrentAlarmSequenceName+"</LABEL>");	 
				}
				sOutPut += SMUtilities.Build_HTML_Table(1, sAlarmSequencesTable, 0,true);
				rsAlarmSequences.close();
			}catch (SQLException ex){
				sOutPut += "Error getting alarm sequences - " + ex.getMessage();
			}
			sOutPut += "</TD>";
			sOutPut += "<TD VALIGN=CENTRE>" + "User can activate and deactive selected alarm sequences. " + "</TD>"
					+ "</TR>";
		
			//Select devices to access
			sOutPut += "<TR><TD ALIGN=RIGHT><B>Activate Devices:</B></TD>";

			try{
				sSQL = "SELECT"
					+ " " + SMTablessdevices.lid 
					+ ", " + SMTablessdevices.sdescription
					+ " FROM " + SMTablessdevices.TableName
					+ " WHERE( "
					+ "(" + SMTablessdevices.iactive + "= 1 )"
					+ " AND "
					+ "(" + SMTablessdevices.soutputterminalnumber + "!= '')"
		    		+ ")"
		    		;
				ResultSet rsDevices = clsDatabaseFunctions.openResultSet(sSQL, getServletContext(), sDBID);
				String sCheckedOrNot = "";
				sOutPut += "<TD>";
				ArrayList<String> sDevicesTable = new ArrayList<String>(0);
				while (rsDevices.next()){
					String sCurrentDeviceName = rsDevices.getString(SMTablessdevices.sdescription);
					String sCurrentDeviceID = rsDevices.getString(SMTablessdevices.lid);
					sCheckedOrNot = isDeviceUser(userentry.getlid(), sCurrentDeviceID, sDBID);
				
					sDevicesTable.add("<LABEL><INPUT TYPE=\"CHECKBOX\"" + sCheckedOrNot + " NAME=\"" + UPDATE_AS_DEVICES_PREFIX + sCurrentDeviceID + "\">" + sCurrentDeviceName + "</LABEL>");	 
				}
				sOutPut += SMUtilities.Build_HTML_Table(1, sDevicesTable, 0,true);
				rsDevices.close();
			}catch (SQLException ex){
				sOutPut += "Error getting devices - " + ex.getMessage();
			}
			sOutPut += "</TD>";
			sOutPut += "<TD VALIGN=CENTRE>" + "User can activate selected devices. " + "</TD>"
					+ "</TR>";
		
   
		}
	
		
		//********************
        sOutPut += "</TABLE>";
        sOutPut += "<BR>";
		sOutPut += "<P><INPUT TYPE=SUBMIT NAME='" + UPDATE_USER_BUTTON_NAME + "' VALUE='" + UPDATE_USER_BUTTON_VALUE + "' STYLE='height: 0.24in'>&nbsp;&nbsp;";
		sOutPut += "<INPUT TYPE=SUBMIT NAME='" + DELETE_USER_BUTTON_NAME + "' VALUE='" + DELETE_USER_BUTTON_VALUE 
				+ "' ONCLICK=\"return confirm('WARNING: This is a permanent action. "
				+ "This users first and last name will be stored on historical records, but the user record can not be restored. "
				+ "Are you sure you want to delete this user record?');\" STYLE='height: 0.24in'></P>";
		sOutPut += "</FORM>";
		out.println(sOutPut);
		
	}
	
	private String isUserUsingCustomLink(String sUserID, String sCustomLinkID, String sDBID){
		String s = "";
		 try{
		       String sSQL = "SELECT * "
				    	+ " FROM " + SMTableuserscustomlinks.TableName
				    	+ " WHERE("
				    	+ "(" + SMTableuserscustomlinks.luserid + "=" + sUserID + ")"
				    	+ " AND "
				    	+ "(" + SMTableuserscustomlinks.icustomlinkid + "=" + sCustomLinkID + ")"
				    	+ ")"
				    	;
		        ResultSet rsCustomLink = clsDatabaseFunctions.openResultSet(sSQL, getServletContext(), sDBID);
	        	if (rsCustomLink.next()){
	        		s = " checked='yes' ";        		
	        	}
	        	rsCustomLink.close();
		    }catch (SQLException ex){
		    	
		    }

		return s;
	}
	private String isUserInSecurityGroup(String sUserID, String sSecurityGroup, String sDBID){
		String s = "";
		 try{
		       String sSQL = "SELECT"
		        	+ " " + SMTablesecurityusergroups.sSecurityGroupName
		        	+ " FROM " + SMTablesecurityusergroups.TableName
		        	+ " WHERE ("
		        	+ "(" + SMTablesecurityusergroups.luserid + "=" + sUserID + ")"
		        	+ "AND "
		        	+ "(" +  SMTablesecurityusergroups.sSecurityGroupName + "='" + sSecurityGroup + "')"
		        	+ ")" 
		        ;
		        ResultSet rsSecurityGroup = clsDatabaseFunctions.openResultSet(sSQL, getServletContext(), sDBID);
	        	if (rsSecurityGroup.next()){
	        		s = " checked='yes' ";        		
	        	}
	        	rsSecurityGroup.close();
		    }catch (SQLException ex){
		    	
		    }

		return s;
	}
	
	private String isUserInAppointmentGroup(String sUserID, String sAppointmentGroup, String sDBID){
		String s = "";
		 try{
		       String sSQL = "SELECT"
		        	+ " " + SMTableappointmentusergroups.sappointmentgroupname
		        	+ " FROM " + SMTableappointmentusergroups.TableName
		        	+ " WHERE ("
		        	+ "(" + SMTableappointmentusergroups.luserid + "= " + sUserID + ")"
		        	+ "AND "
		        	+ "(" +  SMTableappointmentusergroups.sappointmentgroupname + "='" + sAppointmentGroup + "')"
		        	+ ")" 
		        ;
		        ResultSet rsSecurityGroup = clsDatabaseFunctions.openResultSet(sSQL, getServletContext(), sDBID);
	        	if (rsSecurityGroup.next()){
	        		s = " checked='yes' ";        		
	        	}
	        	rsSecurityGroup.close();
		    }catch (SQLException ex){
		    	
		    }

		return s;
	}
	
	private String isUserInAlarmSequence(String sUserID, String sAlarmSequence, String sDBID){
		String s = "";
		 try{
		       String sSQL = "SELECT"
		        	+ " " + SMTablessalarmsequenceusers.lalarmsequenceid
		        	+ " FROM " + SMTablessalarmsequenceusers.TableName
		        	+ " WHERE ("
		        	+ "(" + SMTablessalarmsequenceusers.luserid + "=" + sUserID + ")"
		        	+ "AND "
		        	+ "(" +  SMTablessalarmsequenceusers.lalarmsequenceid + "=" + sAlarmSequence + ")"
		        	+ ")" 
		        ;
		        ResultSet rsAlarmSequences = clsDatabaseFunctions.openResultSet(sSQL, getServletContext(), sDBID);
	        	if (rsAlarmSequences.next()){
	        		s = " checked='yes' ";        		
	        	}
	        	rsAlarmSequences.close();
		    }catch (SQLException ex){
		    	
		    }
		return s;
	}
	
	private String isDeviceUser(String sUserID, String sDevice, String sDBID){
		String s = "";
		 try{
		       String sSQL = "SELECT"
		        	+ " " + SMTablessdeviceusers.ldeviceid
		        	+ " FROM " + SMTablessdeviceusers.TableName
		        	+ " WHERE ("
		        	+ "(" + SMTablessdeviceusers.luserid + "=" + sUserID + ")"
		        	+ "AND "
		        	+ "(" +  SMTablessdeviceusers.ldeviceid + "='" + sDevice + "')"
		        	+ ")" 
		        ;
		        ResultSet rsDevices = clsDatabaseFunctions.openResultSet(sSQL, getServletContext(), sDBID);
	        	if (rsDevices.next()){
	        		s = " checked='yes' ";        		
	        	}
	        	rsDevices.close();
		    }catch (SQLException ex){
		    	
		    }

		return s;
	}
	
	
	private String print_user_color_selection_map(
			int iRow, 
			int iCol, 
			boolean bRestricted, 
			ServletContext context, 
			String sDBID) throws SQLException{
		String s = "";
		// get mechanics's color
		String sUserColor = "000000";
		String sSQL = "SELECT * FROM" + " " + SMTablecolortable.TableName
				+ " WHERE" + " " + SMTablecolortable.irow + " = " + iRow
				+ " AND" + " " + SMTablecolortable.icol + " = " + iCol;
		// System.out.println("[1345066542] SQL = " + sSQL);
		ResultSet rsColorCode = clsDatabaseFunctions.openResultSet(sSQL, context, sDBID);
		if (rsColorCode.next()) {
			sUserColor = rsColorCode.getString(SMTablecolortable.scolorcode);
		}
		rsColorCode.close();
		s += "<div id='colorselector' style='position:relative;'>\n";
		// here we circle all the used colors so user knows what NOT to pic.
		sSQL = "SELECT DISTINCT" + " " + SMTableusers.susercolorcoderow
				+ "," + " " + SMTableusers.susercolorcodecol + " FROM "
				+ " " + SMTableusers.TableName;


		// System.out.println("[1345229718] SQL = " + sSQL);

		ResultSet rsUsedColors = clsDatabaseFunctions.openResultSet(sSQL, context, sDBID);
		int iSuffix = 0;
		while (rsUsedColors.next()) {

			s += "<div id='mc"
					+ iSuffix
					+ "' "
					+ "style='visibility:visible; "
					+ "position:absolute; "
					+ "top:"
					+ (rsUsedColors.getInt(SMTableusers.susercolorcoderow) * 15 + 30)
					+ "px; "
					+ "left:"
					+ (rsUsedColors.getInt(SMTableusers.susercolorcodecol)
							* 18
							+ Math.abs(rsUsedColors
									.getInt(SMTableusers.susercolorcoderow) - 6)
							* 9 + 4) + "px; " + "width:12px; " + "height:2px; "
					+ "background-image:url(\"images/usedcolor.png\")'" + ">"
					+ "</div>\n";
			iSuffix++;
		}
		rsUsedColors.close();

		s += "<div id='selectedColor' "
				+ "style='visibility:hidden; "
				+ "position:relative; "
				+ "width:20px; "
				+ "height:20px; "
				+ "background-image:url(\"images/selectedcolor.gif\")'"
				+ ">"
				+ "</div>\n"
				+

				"<img style='margin-right:2px;' src=\"images/colormap.png\" usemap='#colormap' alt='colormap' />"
				+ "<map id='colormap' name='colormap' onmouseout='mouseOutMap()'>"
				+ print_area_list();

		s += "</map>\n";
		s += "<script type='text/javascript'>\n"; // intialize color
														// selector with mech
														// info.

		s += "	document.getElementById('selectedColor').style.top='"
				+ (iRow * 15 + 20) + "px'\n;";
		s += "	document.getElementById('selectedColor').style.left='"
				+ (iCol * 18 + Math.abs(iRow - 6) * 9) + "px';\n";
		s += "	document.getElementById('selectedColor').style.visibility='visible';\n";
		
		s += "</script>\n";
		s += "<div style='width:300px;padding-top:33px;padding-left:66px;margin-bottom:30px;'>"
				+ "<div id='divpreview' style='float: left; height: 20px; width: 100px; border-width: 1px 1px medium; border-style: solid solid none; border-color: rgb(212, 212, 212) rgb(212, 212, 212) -moz-use-text-color; -moz-border-top-colors: none; -moz-border-right-colors: none; -moz-border-bottom-colors: none; -moz-border-left-colors: none; -moz-border-image: none; background-color: #"
				+ sUserColor
				+ ";'>&nbsp;</div>"
				+ "<div id='divpreviewtxt' style='float:left; height: 20px; width:50px;padding-left:5px;padding-top:7px;'>"
				+ sUserColor
				+ "</div>"
				+ "<input type=hidden style='height: 20px; width:70px;' name='colorhex' id='colorhex' value='#"
				+ sUserColor + "' />" + "</div>\n";
		// out.println("<BR><BR>Selected color: <input style='width:70px;' name='colorhex' id='colorhex' value='#FF0000' />");

		s += "</div>\n"; // end of color selector div

		s += "<script type='text/javascript'>\n";
		s += "<!--\n";
		s += "var colorhex=\"#" + sUserColor + "\"\n";

		s += "function mouseOverColor(hex){\n";
		s += "	document.getElementById('divpreview').style.backgroundColor=hex;\n";
		s += "	document.getElementById('divpreviewtxt').innerHTML=hex;\n";
		s += "	document.body.style.cursor='pointer';\n";
		s += "}\n";

		s += "function mouseOutMap(){\n";
		s += "	document.getElementById('divpreview').style.backgroundColor=colorhex;\n";
		s += "	document.getElementById('divpreviewtxt').innerHTML=colorhex;\n";
		s += "	document.body.style.cursor='';\n";
		s += "}\n";

		s += "function clickColor(hex,seltop,selleft){\n";
		s += "	var xhttp,c\n";
		s += "	if (hex==0){\n";
		s += "		c=document.getElementById('colorhex').value;\n";
		s += "	}else{\n";
		s += "		c=hex;\n";
		s += "	}\n";
		s += "	if (c.substr(0,1)=='#'){\n";
		s += "		c=c.substr(1);\n";
		s += "	}\n";
		s += "	colorhex='#' + c;\n";

		s += "	document.getElementById('colorhex').value=colorhex;\n";

		s += "	if (seltop>-1 && selleft>-1){\n";
		s += "		document.getElementById('selectedColor').style.top=seltop + 'px';\n";
		s += "		document.getElementById('selectedColor').style.left=selleft + 'px';\n";
		s += "		document.getElementById('selectedColor').style.visibility='visible';\n";
		s += "	}else{\n";
		s += "		document.getElementById('divpreview').style.backgroundColor=colorhex;\n";
		s += "		document.getElementById('divpreviewtxt').innerHTML=colorhex;\n";
		s += "		document.getElementById('selectedColor').style.visibility='hidden';\n";
		s += "	}\n";
		s += "}\n";
		// -->
		s += "</script>\n";

		return s;
}
	
	
	private String get_javascript_validation() {
		
		String s = "<script>\n";
		s += "  function checkInput (){ \n"
				//Validate the users first name
		  +  "    var errors = [];\n"
		  +  "    var alertMessage = \"\";\n"
		  + "     if(document.getElementById('" + SMTableusers.sUserName + "').value == '' ){"
		  +  "      errors.push('"+SMTableusers.sUserName+"');\n"
		  +  "      alertMessage += \"Username field is BLANK\";\n"		  
		  + "     }"
		  +  "    if(document.getElementById('" + SMTableusers.sUserFirstName + "').value == '' && document.getElementById('" + SMTableusers.sUserLastName + "').value == ''){\n"
		  +  "      errors.push('"+SMTableusers.sUserFirstName+"');\n"
		  +  "      errors.push('"+SMTableusers.sUserLastName+"');\n"
		  +  "      alertMessage += \"Firstname and Lastname field is BLANK\";\n"		  
		  +  "       }else if (document.getElementById('"+SMTableusers.sUserFirstName+"').value == ''){\n"
		  +  "      errors.push('"+SMTableusers.sUserFirstName+"');\n"
		  +  "      alertMessage += \"Firstname  is BLANK\";\n"
		  +  "       }else if (document.getElementById('"+SMTableusers.sUserLastName+"').value == ''){\n"
		  +  "         errors.push('"+SMTableusers.sUserLastName+"');\n"
		  +  "         alertMessage += \"Lastname  is BLANK\";\n"
		  +  "      }\n"
		  + ""
		  + ""
		  +  "       if(errors === undefined || errors.length == 0){\n"
		  +  "            return true;\n"
		  +  "       }\n"
		  +  "         alert(alertMessage);\n"
		  +  "         document.getElementById(errors[0]).scrollIntoView();\n"
		  +  "         document.getElementById(errors[0]).focus();\n"
		  +  "         return false;\n"
		  +  "     }\n\n"

		  + "</script>\n";
		
		return s;
	}
private String print_area_list(){
		
		return 
		"<area style='cursor:pointer' shape='poly' coords='63,0,72,4,72,15,63,19,54,15,54,4' onclick='clickColor(\"#000000\",20,54)' onmouseover='mouseOverColor(\"#000000\")' alt='#000000' />" +
		"<area style='cursor:pointer' shape='poly' coords='81,0,90,4,90,15,81,19,72,15,72,4' onclick='clickColor(\"#336699\",20,72)' onmouseover='mouseOverColor(\"#336699\")' alt='#336699' />" +
		"<area style='cursor:pointer' shape='poly' coords='99,0,108,4,108,15,99,19,90,15,90,4' onclick='clickColor(\"#3366CC\",20,90)' onmouseover='mouseOverColor(\"#3366CC\")' alt='#3366CC' />" +
		"<area style='cursor:pointer' shape='poly' coords='117,0,126,4,126,15,117,19,108,15,108,4' onclick='clickColor(\"#003399\",20,108)' onmouseover='mouseOverColor(\"#003399\")' alt='#003399' />" +
		"<area style='cursor:pointer' shape='poly' coords='135,0,144,4,144,15,135,19,126,15,126,4' onclick='clickColor(\"#000099\",20,126)' onmouseover='mouseOverColor(\"#000099\")' alt='#000099' />" +
		"<area style='cursor:pointer' shape='poly' coords='153,0,162,4,162,15,153,19,144,15,144,4' onclick='clickColor(\"#0000CC\",20,144)' onmouseover='mouseOverColor(\"#0000CC\")' alt='#0000CC' />" +
		"<area style='cursor:pointer' shape='poly' coords='171,0,180,4,180,15,171,19,162,15,162,4' onclick='clickColor(\"#000066\",20,162)' onmouseover='mouseOverColor(\"#000066\")' alt='#000066' />" +
		"<area style='cursor:pointer' shape='poly' coords='54,15,63,19,63,30,54,34,45,30,45,19' onclick='clickColor(\"#006666\",35,45)' onmouseover='mouseOverColor(\"#006666\")' alt='#006666' />" +
		"<area style='cursor:pointer' shape='poly' coords='72,15,81,19,81,30,72,34,63,30,63,19' onclick='clickColor(\"#006699\",35,63)' onmouseover='mouseOverColor(\"#006699\")' alt='#006699' />" +
		"<area style='cursor:pointer' shape='poly' coords='90,15,99,19,99,30,90,34,81,30,81,19' onclick='clickColor(\"#0099CC\",35,81)' onmouseover='mouseOverColor(\"#0099CC\")' alt='#0099CC' />" +
		"<area style='cursor:pointer' shape='poly' coords='108,15,117,19,117,30,108,34,99,30,99,19' onclick='clickColor(\"#0066CC\",35,99)' onmouseover='mouseOverColor(\"#0066CC\")' alt='#0066CC' />" +
		"<area style='cursor:pointer' shape='poly' coords='126,15,135,19,135,30,126,34,117,30,117,19' onclick='clickColor(\"#0033CC\",35,117)' onmouseover='mouseOverColor(\"#0033CC\")' alt='#0033CC' />" +
		"<area style='cursor:pointer' shape='poly' coords='144,15,153,19,153,30,144,34,135,30,135,19' onclick='clickColor(\"#0000FF\",35,135)' onmouseover='mouseOverColor(\"#0000FF\")' alt='#0000FF' />" +
		"<area style='cursor:pointer' shape='poly' coords='162,15,171,19,171,30,162,34,153,30,153,19' onclick='clickColor(\"#3333FF\",35,153)' onmouseover='mouseOverColor(\"#3333FF\")' alt='#3333FF' />" +
		"<area style='cursor:pointer' shape='poly' coords='180,15,189,19,189,30,180,34,171,30,171,19' onclick='clickColor(\"#333399\",35,171)' onmouseover='mouseOverColor(\"#333399\")' alt='#333399' />" +
		"<area style='cursor:pointer' shape='poly' coords='45,30,54,34,54,45,45,49,36,45,36,34' onclick='clickColor(\"#669999\",50,36)' onmouseover='mouseOverColor(\"#669999\")' alt='#669999' />" +
		"<area style='cursor:pointer' shape='poly' coords='63,30,72,34,72,45,63,49,54,45,54,34' onclick='clickColor(\"#009999\",50,54)' onmouseover='mouseOverColor(\"#009999\")' alt='#009999' />" +
		"<area style='cursor:pointer' shape='poly' coords='81,30,90,34,90,45,81,49,72,45,72,34' onclick='clickColor(\"#33CCCC\",50,72)' onmouseover='mouseOverColor(\"#33CCCC\")' alt='#33CCCC' />" +
		"<area style='cursor:pointer' shape='poly' coords='99,30,108,34,108,45,99,49,90,45,90,34' onclick='clickColor(\"#00CCFF\",50,90)' onmouseover='mouseOverColor(\"#00CCFF\")' alt='#00CCFF' />" +
		"<area style='cursor:pointer' shape='poly' coords='117,30,126,34,126,45,117,49,108,45,108,34' onclick='clickColor(\"#0099FF\",50,108)' onmouseover='mouseOverColor(\"#0099FF\")' alt='#0099FF' />" +
		"<area style='cursor:pointer' shape='poly' coords='135,30,144,34,144,45,135,49,126,45,126,34' onclick='clickColor(\"#0066FF\",50,126)' onmouseover='mouseOverColor(\"#0066FF\")' alt='#0066FF' />" +
		"<area style='cursor:pointer' shape='poly' coords='153,30,162,34,162,45,153,49,144,45,144,34' onclick='clickColor(\"#3366FF\",50,144)' onmouseover='mouseOverColor(\"#3366FF\")' alt='#3366FF' />" +
		"<area style='cursor:pointer' shape='poly' coords='171,30,180,34,180,45,171,49,162,45,162,34' onclick='clickColor(\"#3333CC\",50,162)' onmouseover='mouseOverColor(\"#3333CC\")' alt='#3333CC' />" +
		"<area style='cursor:pointer' shape='poly' coords='189,30,198,34,198,45,189,49,180,45,180,34' onclick='clickColor(\"#666699\",50,180)' onmouseover='mouseOverColor(\"#666699\")' alt='#666699' />" +
		"<area style='cursor:pointer' shape='poly' coords='36,45,45,49,45,60,36,64,27,60,27,49' onclick='clickColor(\"#339966\",65,27)' onmouseover='mouseOverColor(\"#339966\")' alt='#339966' />" +
		"<area style='cursor:pointer' shape='poly' coords='54,45,63,49,63,60,54,64,45,60,45,49' onclick='clickColor(\"#00CC99\",65,45)' onmouseover='mouseOverColor(\"#00CC99\")' alt='#00CC99' />" +
		"<area style='cursor:pointer' shape='poly' coords='72,45,81,49,81,60,72,64,63,60,63,49' onclick='clickColor(\"#00FFCC\",65,63)' onmouseover='mouseOverColor(\"#00FFCC\")' alt='#00FFCC' />" +
		"<area style='cursor:pointer' shape='poly' coords='90,45,99,49,99,60,90,64,81,60,81,49' onclick='clickColor(\"#00FFFF\",65,81)' onmouseover='mouseOverColor(\"#00FFFF\")' alt='#00FFFF' />" +
		"<area style='cursor:pointer' shape='poly' coords='108,45,117,49,117,60,108,64,99,60,99,49' onclick='clickColor(\"#33CCFF\",65,99)' onmouseover='mouseOverColor(\"#33CCFF\")' alt='#33CCFF' />" +
		"<area style='cursor:pointer' shape='poly' coords='126,45,135,49,135,60,126,64,117,60,117,49' onclick='clickColor(\"#3399FF\",65,117)' onmouseover='mouseOverColor(\"#3399FF\")' alt='#3399FF' />" +
		"<area style='cursor:pointer' shape='poly' coords='144,45,153,49,153,60,144,64,135,60,135,49' onclick='clickColor(\"#6699FF\",65,135)' onmouseover='mouseOverColor(\"#6699FF\")' alt='#6699FF' />" +
		"<area style='cursor:pointer' shape='poly' coords='162,45,171,49,171,60,162,64,153,60,153,49' onclick='clickColor(\"#6666FF\",65,153)' onmouseover='mouseOverColor(\"#6666FF\")' alt='#6666FF' />" +
		"<area style='cursor:pointer' shape='poly' coords='180,45,189,49,189,60,180,64,171,60,171,49' onclick='clickColor(\"#6600FF\",65,171)' onmouseover='mouseOverColor(\"#6600FF\")' alt='#6600FF' />" +
		"<area style='cursor:pointer' shape='poly' coords='198,45,207,49,207,60,198,64,189,60,189,49' onclick='clickColor(\"#6600CC\",65,189)' onmouseover='mouseOverColor(\"#6600CC\")' alt='#6600CC' />" +
		"<area style='cursor:pointer' shape='poly' coords='27,60,36,64,36,75,27,79,18,75,18,64' onclick='clickColor(\"#339933\",80,18)' onmouseover='mouseOverColor(\"#339933\")' alt='#339933' />" +
		"<area style='cursor:pointer' shape='poly' coords='45,60,54,64,54,75,45,79,36,75,36,64' onclick='clickColor(\"#00CC66\",80,36)' onmouseover='mouseOverColor(\"#00CC66\")' alt='#00CC66' />" +
		"<area style='cursor:pointer' shape='poly' coords='63,60,72,64,72,75,63,79,54,75,54,64' onclick='clickColor(\"#00FF99\",80,54)' onmouseover='mouseOverColor(\"#00FF99\")' alt='#00FF99' />" +
		"<area style='cursor:pointer' shape='poly' coords='81,60,90,64,90,75,81,79,72,75,72,64' onclick='clickColor(\"#66FFCC\",80,72)' onmouseover='mouseOverColor(\"#66FFCC\")' alt='#66FFCC' />" +
		"<area style='cursor:pointer' shape='poly' coords='99,60,108,64,108,75,99,79,90,75,90,64' onclick='clickColor(\"#66FFFF\",80,90)' onmouseover='mouseOverColor(\"#66FFFF\")' alt='#66FFFF' />" +
		"<area style='cursor:pointer' shape='poly' coords='117,60,126,64,126,75,117,79,108,75,108,64' onclick='clickColor(\"#66CCFF\",80,108)' onmouseover='mouseOverColor(\"#66CCFF\")' alt='#66CCFF' />" +
		"<area style='cursor:pointer' shape='poly' coords='135,60,144,64,144,75,135,79,126,75,126,64' onclick='clickColor(\"#99CCFF\",80,126)' onmouseover='mouseOverColor(\"#99CCFF\")' alt='#99CCFF' />" +
		"<area style='cursor:pointer' shape='poly' coords='153,60,162,64,162,75,153,79,144,75,144,64' onclick='clickColor(\"#9999FF\",80,144)' onmouseover='mouseOverColor(\"#9999FF\")' alt='#9999FF' />" +
		"<area style='cursor:pointer' shape='poly' coords='171,60,180,64,180,75,171,79,162,75,162,64' onclick='clickColor(\"#9966FF\",80,162)' onmouseover='mouseOverColor(\"#9966FF\")' alt='#9966FF' />" +
		"<area style='cursor:pointer' shape='poly' coords='189,60,198,64,198,75,189,79,180,75,180,64' onclick='clickColor(\"#9933FF\",80,180)' onmouseover='mouseOverColor(\"#9933FF\")' alt='#9933FF' />" +
		"<area style='cursor:pointer' shape='poly' coords='207,60,216,64,216,75,207,79,198,75,198,64' onclick='clickColor(\"#9900FF\",80,198)' onmouseover='mouseOverColor(\"#9900FF\")' alt='#9900FF' />" +
		"<area style='cursor:pointer' shape='poly' coords='18,75,27,79,27,90,18,94,9,90,9,79' onclick='clickColor(\"#006600\",95,9)' onmouseover='mouseOverColor(\"#006600\")' alt='#006600' />" +
		"<area style='cursor:pointer' shape='poly' coords='36,75,45,79,45,90,36,94,27,90,27,79' onclick='clickColor(\"#00CC00\",95,27)' onmouseover='mouseOverColor(\"#00CC00\")' alt='#00CC00' />" +
		"<area style='cursor:pointer' shape='poly' coords='54,75,63,79,63,90,54,94,45,90,45,79' onclick='clickColor(\"#00FF00\",95,45)' onmouseover='mouseOverColor(\"#00FF00\")' alt='#00FF00' />" +
		"<area style='cursor:pointer' shape='poly' coords='72,75,81,79,81,90,72,94,63,90,63,79' onclick='clickColor(\"#66FF99\",95,63)' onmouseover='mouseOverColor(\"#66FF99\")' alt='#66FF99' />" +
		"<area style='cursor:pointer' shape='poly' coords='90,75,99,79,99,90,90,94,81,90,81,79' onclick='clickColor(\"#99FFCC\",95,81)' onmouseover='mouseOverColor(\"#99FFCC\")' alt='#99FFCC' />" +
		"<area style='cursor:pointer' shape='poly' coords='108,75,117,79,117,90,108,94,99,90,99,79' onclick='clickColor(\"#CCFFFF\",95,99)' onmouseover='mouseOverColor(\"#CCFFFF\")' alt='#CCFFFF' />" +
		"<area style='cursor:pointer' shape='poly' coords='126,75,135,79,135,90,126,94,117,90,117,79' onclick='clickColor(\"#CCCCFF\",95,117)' onmouseover='mouseOverColor(\"#CCCCFF\")' alt='#CCCCFF' />" +
		"<area style='cursor:pointer' shape='poly' coords='144,75,153,79,153,90,144,94,135,90,135,79' onclick='clickColor(\"#CC99FF\",95,135)' onmouseover='mouseOverColor(\"#CC99FF\")' alt='#CC99FF' />" +
		"<area style='cursor:pointer' shape='poly' coords='162,75,171,79,171,90,162,94,153,90,153,79' onclick='clickColor(\"#CC66FF\",95,153)' onmouseover='mouseOverColor(\"#CC66FF\")' alt='#CC66FF' />" +
		"<area style='cursor:pointer' shape='poly' coords='180,75,189,79,189,90,180,94,171,90,171,79' onclick='clickColor(\"#CC33FF\",95,171)' onmouseover='mouseOverColor(\"#CC33FF\")' alt='#CC33FF' />" +
		"<area style='cursor:pointer' shape='poly' coords='198,75,207,79,207,90,198,94,189,90,189,79' onclick='clickColor(\"#CC00FF\",95,189)' onmouseover='mouseOverColor(\"#CC00FF\")' alt='#CC00FF' />" +
		"<area style='cursor:pointer' shape='poly' coords='216,75,225,79,225,90,216,94,207,90,207,79' onclick='clickColor(\"#9900CC\",95,207)' onmouseover='mouseOverColor(\"#9900CC\")' alt='#9900CC' />" +
		"<area style='cursor:pointer' shape='poly' coords='9,90,18,94,18,105,9,109,0,105,0,94' onclick='clickColor(\"#003300\",110,0)' onmouseover='mouseOverColor(\"#003300\")' alt='#003300' />" +
		"<area style='cursor:pointer' shape='poly' coords='27,90,36,94,36,105,27,109,18,105,18,94' onclick='clickColor(\"#009933\",110,18)' onmouseover='mouseOverColor(\"#009933\")' alt='#009933' />" +
		"<area style='cursor:pointer' shape='poly' coords='45,90,54,94,54,105,45,109,36,105,36,94' onclick='clickColor(\"#33CC33\",110,36)' onmouseover='mouseOverColor(\"#33CC33\")' alt='#33CC33' />" +
		"<area style='cursor:pointer' shape='poly' coords='63,90,72,94,72,105,63,109,54,105,54,94' onclick='clickColor(\"#66FF66\",110,54)' onmouseover='mouseOverColor(\"#66FF66\")' alt='#66FF66' />" +
		"<area style='cursor:pointer' shape='poly' coords='81,90,90,94,90,105,81,109,72,105,72,94' onclick='clickColor(\"#99FF99\",110,72)' onmouseover='mouseOverColor(\"#99FF99\")' alt='#99FF99' />" +
		"<area style='cursor:pointer' shape='poly' coords='99,90,108,94,108,105,99,109,90,105,90,94' onclick='clickColor(\"#CCFFCC\",110,90)' onmouseover='mouseOverColor(\"#CCFFCC\")' alt='#CCFFCC' />" +
		"<area style='cursor:pointer' shape='poly' coords='117,90,126,94,126,105,117,109,108,105,108,94' onclick='clickColor(\"#FFFFFF\",110,108)' onmouseover='mouseOverColor(\"#FFFFFF\")' alt='#FFFFFF' />" +
		"<area style='cursor:pointer' shape='poly' coords='135,90,144,94,144,105,135,109,126,105,126,94' onclick='clickColor(\"#FFCCFF\",110,126)' onmouseover='mouseOverColor(\"#FFCCFF\")' alt='#FFCCFF' />" +
		"<area style='cursor:pointer' shape='poly' coords='153,90,162,94,162,105,153,109,144,105,144,94' onclick='clickColor(\"#FF99FF\",110,144)' onmouseover='mouseOverColor(\"#FF99FF\")' alt='#FF99FF' />" +
		"<area style='cursor:pointer' shape='poly' coords='171,90,180,94,180,105,171,109,162,105,162,94' onclick='clickColor(\"#FF66FF\",110,162)' onmouseover='mouseOverColor(\"#FF66FF\")' alt='#FF66FF' />" +
		"<area style='cursor:pointer' shape='poly' coords='189,90,198,94,198,105,189,109,180,105,180,94' onclick='clickColor(\"#FF00FF\",110,180)' onmouseover='mouseOverColor(\"#FF00FF\")' alt='#FF00FF' />" +
		"<area style='cursor:pointer' shape='poly' coords='207,90,216,94,216,105,207,109,198,105,198,94' onclick='clickColor(\"#CC00CC\",110,198)' onmouseover='mouseOverColor(\"#CC00CC\")' alt='#CC00CC' />" +
		"<area style='cursor:pointer' shape='poly' coords='225,90,234,94,234,105,225,109,216,105,216,94' onclick='clickColor(\"#660066\",110,216)' onmouseover='mouseOverColor(\"#660066\")' alt='#660066' />" +
		"<area style='cursor:pointer' shape='poly' coords='18,105,27,109,27,120,18,124,9,120,9,109' onclick='clickColor(\"#336600\",125,9)' onmouseover='mouseOverColor(\"#336600\")' alt='#336600' />" +
		"<area style='cursor:pointer' shape='poly' coords='36,105,45,109,45,120,36,124,27,120,27,109' onclick='clickColor(\"#009900\",125,27)' onmouseover='mouseOverColor(\"#009900\")' alt='#009900' />" +
		"<area style='cursor:pointer' shape='poly' coords='54,105,63,109,63,120,54,124,45,120,45,109' onclick='clickColor(\"#66FF33\",125,45)' onmouseover='mouseOverColor(\"#66FF33\")' alt='#66FF33' />" +
		"<area style='cursor:pointer' shape='poly' coords='72,105,81,109,81,120,72,124,63,120,63,109' onclick='clickColor(\"#99FF66\",125,63)' onmouseover='mouseOverColor(\"#99FF66\")' alt='#99FF66' />" +
		"<area style='cursor:pointer' shape='poly' coords='90,105,99,109,99,120,90,124,81,120,81,109' onclick='clickColor(\"#CCFF99\",125,81)' onmouseover='mouseOverColor(\"#CCFF99\")' alt='#CCFF99' />" +
		"<area style='cursor:pointer' shape='poly' coords='108,105,117,109,117,120,108,124,99,120,99,109' onclick='clickColor(\"#FFFFCC\",125,99)' onmouseover='mouseOverColor(\"#FFFFCC\")' alt='#FFFFCC' />" +
		"<area style='cursor:pointer' shape='poly' coords='126,105,135,109,135,120,126,124,117,120,117,109' onclick='clickColor(\"#FFCCCC\",125,117)' onmouseover='mouseOverColor(\"#FFCCCC\")' alt='#FFCCCC' />" +
		"<area style='cursor:pointer' shape='poly' coords='144,105,153,109,153,120,144,124,135,120,135,109' onclick='clickColor(\"#FF99CC\",125,135)' onmouseover='mouseOverColor(\"#FF99CC\")' alt='#FF99CC' />" +
		"<area style='cursor:pointer' shape='poly' coords='162,105,171,109,171,120,162,124,153,120,153,109' onclick='clickColor(\"#FF66CC\",125,153)' onmouseover='mouseOverColor(\"#FF66CC\")' alt='#FF66CC' />" +
		"<area style='cursor:pointer' shape='poly' coords='180,105,189,109,189,120,180,124,171,120,171,109' onclick='clickColor(\"#FF33CC\",125,171)' onmouseover='mouseOverColor(\"#FF33CC\")' alt='#FF33CC' />" +
		"<area style='cursor:pointer' shape='poly' coords='198,105,207,109,207,120,198,124,189,120,189,109' onclick='clickColor(\"#CC0099\",125,189)' onmouseover='mouseOverColor(\"#CC0099\")' alt='#CC0099' />" +
		"<area style='cursor:pointer' shape='poly' coords='216,105,225,109,225,120,216,124,207,120,207,109' onclick='clickColor(\"#993399\",125,207)' onmouseover='mouseOverColor(\"#993399\")' alt='#993399' />" +
		"<area style='cursor:pointer' shape='poly' coords='27,120,36,124,36,135,27,139,18,135,18,124' onclick='clickColor(\"#333300\",140,18)' onmouseover='mouseOverColor(\"#333300\")' alt='#333300' />" +
		"<area style='cursor:pointer' shape='poly' coords='45,120,54,124,54,135,45,139,36,135,36,124' onclick='clickColor(\"#669900\",140,36)' onmouseover='mouseOverColor(\"#669900\")' alt='#669900' />" +
		"<area style='cursor:pointer' shape='poly' coords='63,120,72,124,72,135,63,139,54,135,54,124' onclick='clickColor(\"#99FF33\",140,54)' onmouseover='mouseOverColor(\"#99FF33\")' alt='#99FF33' />" +
		"<area style='cursor:pointer' shape='poly' coords='81,120,90,124,90,135,81,139,72,135,72,124' onclick='clickColor(\"#CCFF66\",140,72)' onmouseover='mouseOverColor(\"#CCFF66\")' alt='#CCFF66' />" +
		"<area style='cursor:pointer' shape='poly' coords='99,120,108,124,108,135,99,139,90,135,90,124' onclick='clickColor(\"#FFFF99\",140,90)' onmouseover='mouseOverColor(\"#FFFF99\")' alt='#FFFF99' />" +
		"<area style='cursor:pointer' shape='poly' coords='117,120,126,124,126,135,117,139,108,135,108,124' onclick='clickColor(\"#FFCC99\",140,108)' onmouseover='mouseOverColor(\"#FFCC99\")' alt='#FFCC99' />" +
		"<area style='cursor:pointer' shape='poly' coords='135,120,144,124,144,135,135,139,126,135,126,124' onclick='clickColor(\"#FF9999\",140,126)' onmouseover='mouseOverColor(\"#FF9999\")' alt='#FF9999' />" +
		"<area style='cursor:pointer' shape='poly' coords='153,120,162,124,162,135,153,139,144,135,144,124' onclick='clickColor(\"#FF6699\",140,144)' onmouseover='mouseOverColor(\"#FF6699\")' alt='#FF6699' />" +
		"<area style='cursor:pointer' shape='poly' coords='171,120,180,124,180,135,171,139,162,135,162,124' onclick='clickColor(\"#FF3399\",140,162)' onmouseover='mouseOverColor(\"#FF3399\")' alt='#FF3399' />" +
		"<area style='cursor:pointer' shape='poly' coords='189,120,198,124,198,135,189,139,180,135,180,124' onclick='clickColor(\"#CC3399\",140,180)' onmouseover='mouseOverColor(\"#CC3399\")' alt='#CC3399' />" +
		"<area style='cursor:pointer' shape='poly' coords='207,120,216,124,216,135,207,139,198,135,198,124' onclick='clickColor(\"#990099\",140,198)' onmouseover='mouseOverColor(\"#990099\")' alt='#990099' />" +
		"<area style='cursor:pointer' shape='poly' coords='36,135,45,139,45,150,36,154,27,150,27,139' onclick='clickColor(\"#666633\",155,27)' onmouseover='mouseOverColor(\"#666633\")' alt='#666633' />" +
		"<area style='cursor:pointer' shape='poly' coords='54,135,63,139,63,150,54,154,45,150,45,139' onclick='clickColor(\"#99CC00\",155,45)' onmouseover='mouseOverColor(\"#99CC00\")' alt='#99CC00' />" +
		"<area style='cursor:pointer' shape='poly' coords='72,135,81,139,81,150,72,154,63,150,63,139' onclick='clickColor(\"#CCFF33\",155,63)' onmouseover='mouseOverColor(\"#CCFF33\")' alt='#CCFF33' />" +
		"<area style='cursor:pointer' shape='poly' coords='90,135,99,139,99,150,90,154,81,150,81,139' onclick='clickColor(\"#FFFF66\",155,81)' onmouseover='mouseOverColor(\"#FFFF66\")' alt='#FFFF66' />" +
		"<area style='cursor:pointer' shape='poly' coords='108,135,117,139,117,150,108,154,99,150,99,139' onclick='clickColor(\"#FFCC66\",155,99)' onmouseover='mouseOverColor(\"#FFCC66\")' alt='#FFCC66' />" +
		"<area style='cursor:pointer' shape='poly' coords='126,135,135,139,135,150,126,154,117,150,117,139' onclick='clickColor(\"#FF9966\",155,117)' onmouseover='mouseOverColor(\"#FF9966\")' alt='#FF9966' />" +
		"<area style='cursor:pointer' shape='poly' coords='144,135,153,139,153,150,144,154,135,150,135,139' onclick='clickColor(\"#FF6666\",155,135)' onmouseover='mouseOverColor(\"#FF6666\")' alt='#FF6666' />" +
		"<area style='cursor:pointer' shape='poly' coords='162,135,171,139,171,150,162,154,153,150,153,139' onclick='clickColor(\"#FF0066\",155,153)' onmouseover='mouseOverColor(\"#FF0066\")' alt='#FF0066' />" +
		"<area style='cursor:pointer' shape='poly' coords='180,135,189,139,189,150,180,154,171,150,171,139' onclick='clickColor(\"#CC6699\",155,171)' onmouseover='mouseOverColor(\"#CC6699\")' alt='#CC6699' />" +
		"<area style='cursor:pointer' shape='poly' coords='198,135,207,139,207,150,198,154,189,150,189,139' onclick='clickColor(\"#993366\",155,189)' onmouseover='mouseOverColor(\"#993366\")' alt='#993366' />" +
		"<area style='cursor:pointer' shape='poly' coords='45,150,54,154,54,165,45,169,36,165,36,154' onclick='clickColor(\"#999966\",170,36)' onmouseover='mouseOverColor(\"#999966\")' alt='#999966' />" +
		"<area style='cursor:pointer' shape='poly' coords='63,150,72,154,72,165,63,169,54,165,54,154' onclick='clickColor(\"#CCCC00\",170,54)' onmouseover='mouseOverColor(\"#CCCC00\")' alt='#CCCC00' />" +
		"<area style='cursor:pointer' shape='poly' coords='81,150,90,154,90,165,81,169,72,165,72,154' onclick='clickColor(\"#FFFF00\",170,72)' onmouseover='mouseOverColor(\"#FFFF00\")' alt='#FFFF00' />" +
		"<area style='cursor:pointer' shape='poly' coords='99,150,108,154,108,165,99,169,90,165,90,154' onclick='clickColor(\"#FFCC00\",170,90)' onmouseover='mouseOverColor(\"#FFCC00\")' alt='#FFCC00' />" +
		"<area style='cursor:pointer' shape='poly' coords='117,150,126,154,126,165,117,169,108,165,108,154' onclick='clickColor(\"#FF9933\",170,108)' onmouseover='mouseOverColor(\"#FF9933\")' alt='#FF9933' />" +
		"<area style='cursor:pointer' shape='poly' coords='135,150,144,154,144,165,135,169,126,165,126,154' onclick='clickColor(\"#FF6600\",170,126)' onmouseover='mouseOverColor(\"#FF6600\")' alt='#FF6600' />" +
		"<area style='cursor:pointer' shape='poly' coords='153,150,162,154,162,165,153,169,144,165,144,154' onclick='clickColor(\"#FF5050\",170,144)' onmouseover='mouseOverColor(\"#FF5050\")' alt='#FF5050' />" +
		"<area style='cursor:pointer' shape='poly' coords='171,150,180,154,180,165,171,169,162,165,162,154' onclick='clickColor(\"#CC0066\",170,162)' onmouseover='mouseOverColor(\"#CC0066\")' alt='#CC0066' />" +
		"<area style='cursor:pointer' shape='poly' coords='189,150,198,154,198,165,189,169,180,165,180,154' onclick='clickColor(\"#660033\",170,180)' onmouseover='mouseOverColor(\"#660033\")' alt='#660033' />" +
		"<area style='cursor:pointer' shape='poly' coords='54,165,63,169,63,180,54,184,45,180,45,169' onclick='clickColor(\"#996633\",185,45)' onmouseover='mouseOverColor(\"#996633\")' alt='#996633' />" +
		"<area style='cursor:pointer' shape='poly' coords='72,165,81,169,81,180,72,184,63,180,63,169' onclick='clickColor(\"#CC9900\",185,63)' onmouseover='mouseOverColor(\"#CC9900\")' alt='#CC9900' />" +
		"<area style='cursor:pointer' shape='poly' coords='90,165,99,169,99,180,90,184,81,180,81,169' onclick='clickColor(\"#FF9900\",185,81)' onmouseover='mouseOverColor(\"#FF9900\")' alt='#FF9900' />" +
		"<area style='cursor:pointer' shape='poly' coords='108,165,117,169,117,180,108,184,99,180,99,169' onclick='clickColor(\"#CC6600\",185,99)' onmouseover='mouseOverColor(\"#CC6600\")' alt='#CC6600' />" +
		"<area style='cursor:pointer' shape='poly' coords='126,165,135,169,135,180,126,184,117,180,117,169' onclick='clickColor(\"#FF3300\",185,117)' onmouseover='mouseOverColor(\"#FF3300\")' alt='#FF3300' />" +
		"<area style='cursor:pointer' shape='poly' coords='144,165,153,169,153,180,144,184,135,180,135,169' onclick='clickColor(\"#FF0000\",185,135)' onmouseover='mouseOverColor(\"#FF0000\")' alt='#FF0000' />" +
		"<area style='cursor:pointer' shape='poly' coords='162,165,171,169,171,180,162,184,153,180,153,169' onclick='clickColor(\"#CC0000\",185,153)' onmouseover='mouseOverColor(\"#CC0000\")' alt='#CC0000' />" +
		"<area style='cursor:pointer' shape='poly' coords='180,165,189,169,189,180,180,184,171,180,171,169' onclick='clickColor(\"#990033\",185,171)' onmouseover='mouseOverColor(\"#990033\")' alt='#990033' />" +
		"<area style='cursor:pointer' shape='poly' coords='63,180,72,184,72,195,63,199,54,195,54,184' onclick='clickColor(\"#663300\",200,54)' onmouseover='mouseOverColor(\"#663300\")' alt='#663300' />" +
		"<area style='cursor:pointer' shape='poly' coords='81,180,90,184,90,195,81,199,72,195,72,184' onclick='clickColor(\"#996600\",200,72)' onmouseover='mouseOverColor(\"#996600\")' alt='#996600' />" +
		"<area style='cursor:pointer' shape='poly' coords='99,180,108,184,108,195,99,199,90,195,90,184' onclick='clickColor(\"#CC3300\",200,90)' onmouseover='mouseOverColor(\"#CC3300\")' alt='#CC3300' />" +
		"<area style='cursor:pointer' shape='poly' coords='117,180,126,184,126,195,117,199,108,195,108,184' onclick='clickColor(\"#993300\",200,108)' onmouseover='mouseOverColor(\"#993300\")' alt='#993300' />" +
		"<area style='cursor:pointer' shape='poly' coords='135,180,144,184,144,195,135,199,126,195,126,184' onclick='clickColor(\"#990000\",200,126)' onmouseover='mouseOverColor(\"#990000\")' alt='#990000' />" +
		"<area style='cursor:pointer' shape='poly' coords='153,180,162,184,162,195,153,199,144,195,144,184' onclick='clickColor(\"#800000\",200,144)' onmouseover='mouseOverColor(\"#800000\")' alt='#800000' />" +
		"<area style='cursor:pointer' shape='poly' coords='171,180,180,184,180,195,171,199,162,195,162,184' onclick='clickColor(\"#660000\",200,162)' onmouseover='mouseOverColor(\"#660000\")' alt='#660000' />";
		
	}
	
	public void doGet(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
		
		doPost(request, response);
	}
}
