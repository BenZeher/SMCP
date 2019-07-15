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

import SMDataDefinition.*;
import ServletUtilities.clsCreateHTMLFormFields;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsManageRequestParameters;
import ServletUtilities.clsServletUtilities;

public class SMViewAppointmentCalendarSelection  extends HttpServlet {
	private static final long serialVersionUID = 1L;
	public static final String APPOINTMENT_GROUP_PARAMETER = "APPOINTMENTGROUP";
	public static final String USERNAME_PARAMETER = "USERNAME";
	public static final String GENERATE_REPORT_PARAMETER = "GENERATE_REPORT";
	public static final String GENERATE_REPORT_LABEL = "----View----";
	public static final String PRINTINDIVIDUAL_VALUE_NO = "NO";
	public static final String INDIVIDUALUSER_PARAMETER = "INDIVIDUALUSER";
	public static final String STARTING_DATE_FIELD = "StartingDate";
	public static final String ENDING_DATE_FIELD = "EndingDate";
	public static final String DATE_RANGE_PARAM = "DateRange";
	public static final String DATE_RANGE_CHOOSE = "DateRangeChoose";
	public static final String DATE_RANGE_TODAY = "DateRangeToday";
	public static final String DATE_RANGE_THISWEEK = "DateRangeThisWeek";
	public static final String DATE_RANGE_NEXTWEEK = "DateRangeNextWeek";
	public static final String EDITAPPOINTMENT_PARAMETER = "AllowAppointmentEditing";
	public static final String VIEWALL_PARAMETER = "VIEWALL";
	public static final String DISPLAYMOVEANDCOPYBUTTONS_PARAMETER = "DISPLAYMOVEANDCOPYBUTTONS";
	public static final String USER_PREFIX = "USER*";
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
		//sUserName = (String) CurrentSession.getAttribute(SMUtilities.SESSION_PARAM_USERNAME);
		String sUserID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERID);

		boolean bAllowedToViewAllAppointments = SMSystemFunctions.isFunctionPermitted(
				SMSystemFunctions.SMViewAppointmentCalendar,
				sUserID,
				getServletContext(),
				sDBID,
				(String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_LICENSE_MODULE_LEVEL));

		//If the user has NO rights to view appointments, bump them out here:
		if (!bAllowedToViewAllAppointments){
			out.println("<HTML>WARNING: You do not currently have access to view the appointment calendar.</BODY></HTML>");
			return;
		}
		
		//Get the parameters:
		String sStartingDate = clsManageRequestParameters.get_Request_Parameter(STARTING_DATE_FIELD, request);
		String sEndingDate = clsManageRequestParameters.get_Request_Parameter(ENDING_DATE_FIELD, request);
		boolean bDateRangeChosen = request.getParameter(DATE_RANGE_CHOOSE) != null;
		boolean bDateRangeToday = request.getParameter(DATE_RANGE_TODAY) != null;
		boolean bDateRangeThisWeek = request.getParameter(DATE_RANGE_THISWEEK) != null;
		boolean bDateRangeNextWeek = request.getParameter(DATE_RANGE_NEXTWEEK) != null;
    	boolean bAllowEditAppointments = SMSystemFunctions.isFunctionPermitted(
    			SMSystemFunctions.SMEditAppointmentCalendar, 
    			sUserID, 
    			getServletContext(), 
    			sDBID,
    			(String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_LICENSE_MODULE_LEVEL));
		boolean bCheckAllAppointmentGroups = request.getParameter(
			SMViewAppointmentCalendarSelection.VIEWALL_PARAMETER) != null; 
		
		String title = "";
		title = "View Appointment Calendar";
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
				+ "smcontrolpanel.SMViewAppointmentCalendarGenerate\">");
		out.println("<INPUT TYPE=HIDDEN NAME='" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "' VALUE='" + sDBID + "'>");
		out.println("<INPUT TYPE=HIDDEN NAME=CallingClass VALUE=\"" + this.getClass().getName() + "\">");
		out.println(sCommandScripts());
		out.println("<TABLE WIDTH=100% CELLPADDING=1 border=1>");

		
		//If none of the date choices are set, we'll decide the default by whether or not
		//this is a mobile session: if it is we'll default to show today only, otherwise,
		//we'll default to show the whole week:
		String sMobile = "N";
		if (!bDateRangeChosen && !bDateRangeToday && !bDateRangeThisWeek){
			sMobile = (String)CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_MOBILE);
			if (sMobile.compareToIgnoreCase("Y") == 0){
				bDateRangeToday = false;
				bDateRangeThisWeek = true;
			}else{
				bDateRangeThisWeek = true;
			}
		}
		String sChecked = "";
		out.println("<TR style=\"background-color:grey; color:white; \"><TD><B>"
					+ "&nbsp;DATE RANGE</B></TD></TR>");
		out.println("<TR><TD ALIGN=LEFT>");
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
		
		if (bAllowEditAppointments){
		out.println("<TR style=\"background-color:grey; color:white; \"><TD>"
				+ "<B>&nbsp;OPTIONS</B></TD></TR>");
		}
		//Add a checkbox for editing:
		if (bAllowEditAppointments){
			out.println("<TR><TD>");
			out.println("<LABEL><INPUT TYPE=CHECKBOX NAME=\"" + EDITAPPOINTMENT_PARAMETER  + "\"");
			if ((request.getParameter(SMViewAppointmentCalendarSelection.EDITAPPOINTMENT_PARAMETER) != null)){
				sChecked = clsServletUtilities.CHECKBOX_CHECKED_STRING;
			}else{
				sChecked = clsServletUtilities.CHECKBOX_CHECKED_STRING;
			}
			out.println(" " + sChecked + " " + " width=0.25>" + "Allow appointment editing." + "<BR><LABEL>");
			out.println("</TD></TR>");
		}
		
		try{
			//select appointment group
			String sSQL = "SELECT"
				+ " " + SMTableappointmentgroups.sappointmentgroupname
				+ ", " + SMTableappointmentgroups.sappointmentgroupdesc
				+ ", " + SMTableappointmentgroups.igroupid
				+ " FROM " + SMTableappointmentgroups.TableName
			;
			ResultSet rsAppointmentGroups = clsDatabaseFunctions.openResultSet(
					sSQL, 
					getServletContext(), 
					sDBID, 
					"MySQL", 
					"smcontrolpanel.SMViewAppointmentCalendarSelection");
			out.println("<TR style=\"background-color:grey; color:white; \"><TD>"
					+ "<B>&nbsp;APPOINTMENT GROUPS</B></TD></TR><TR><TD>");
			sChecked = "";
			while(rsAppointmentGroups.next()){
				String sAppointmentGroup = rsAppointmentGroups.getString(SMTableappointmentgroups.TableName + "." 
					+ SMTableappointmentgroups.sappointmentgroupname).trim();
				String sAppointmentGroupID = rsAppointmentGroups.getString(SMTableappointmentgroups.TableName + "." 
						+ SMTableappointmentgroups.igroupid).trim();
				out.println("<LABEL><INPUT TYPE=CHECKBOX NAME=\"" + APPOINTMENT_GROUP_PARAMETER + "\""
					+ " ID=\"GID*" + sAppointmentGroupID + "\""
					+ " onclick=\"selectusers()\""
				);
				if (
					(request.getParameter(APPOINTMENT_GROUP_PARAMETER + sAppointmentGroup) != null) || bCheckAllAppointmentGroups
				){
					sChecked = clsServletUtilities.CHECKBOX_CHECKED_STRING;
				}else{
					sChecked = "";
				}
				out.println(" " + sChecked + " "
					+ " width=0.25>" 
					+ sAppointmentGroup + " - "
					+ rsAppointmentGroups.getString(SMTableappointmentgroups.TableName + "." + SMTableappointmentgroups.sappointmentgroupdesc) + "<BR></LABEL>");
			}
			rsAppointmentGroups.close();
			out.println("</TD></TR>");
			out.println("</TABLE>");
			out.println("<BR><LABEL><INPUT TYPE=\"SUBMIT\" NAME=" 
					+ GENERATE_REPORT_PARAMETER 
					+ " VALUE=\"" + GENERATE_REPORT_LABEL + "\">&nbsp;&nbsp;<BR><BR></LABEL>");
		}catch(SQLException ex){
			//handle any errors
			out.println("Error reading appointment groups - " + ex.getMessage() + "<BR>");
		}
		
		ArrayList<String> sUserTable = new ArrayList<String>(0);
		try{
			//First get a list of all the users:
	        String sSQL = "SELECT DISTINCT " + SMTableappointmentusergroups.TableName + "." + SMTableappointmentusergroups.luserid 
	        		+ "," + SMTableusers.TableName + "." + SMTableusers.sUserFirstName
	        		+ "," + SMTableusers.TableName + "." + SMTableusers.sUserLastName
	        		+ "," + SMTableusers.TableName + "." + SMTableusers.sUserName
	        		+ " FROM " + SMTableappointmentusergroups.TableName
	        		+ " LEFT JOIN " + SMTableusers.TableName 
	        		+ " ON " + SMTableusers.TableName + "." + SMTableusers.lid 
	        		+ "=" + SMTableappointmentusergroups.TableName + "." + SMTableappointmentusergroups.luserid 
	        		+ " ORDER BY " + SMTableusers.TableName + "." + SMTableusers.sUserFirstName;

	        ResultSet rsUsers = clsDatabaseFunctions.openResultSet(sSQL, getServletContext(), sDBID);
	       
	        String sCurrentUserID = "";
	        String sCurrentUserFullName = "";
	        String sGroupID = "";
        	while (rsUsers.next()){
        		sCurrentUserID = rsUsers.getString(SMTableappointmentusergroups.TableName + "." + SMTableappointmentusergroups.luserid );
        		sCurrentUserFullName = rsUsers.getString(SMTableusers.sUserFirstName) + " " + rsUsers.getString(SMTableusers.sUserLastName);
        		sSQL = "SELECT " + SMTableappointmentgroups.igroupid 
    	        		+ " FROM " + SMTableappointmentgroups.TableName
    	        		+ " LEFT JOIN " + SMTableappointmentusergroups.TableName
    	        		+ " ON " + SMTableappointmentgroups.TableName + "." + SMTableappointmentgroups.sappointmentgroupname 
    	        		+ "=" + SMTableappointmentusergroups.TableName + "." +  SMTableappointmentusergroups.sappointmentgroupname
    	        		+ " WHERE " + SMTableappointmentusergroups.TableName + "." + SMTableappointmentusergroups.luserid 
    	        		+ "=" + sCurrentUserID + "";
        		
        		ResultSet rsAppointmentGroups = clsDatabaseFunctions.openResultSet(sSQL, getServletContext(), sDBID);
        		
        		while (rsAppointmentGroups.next()){
        			sGroupID += "Group" +  rsAppointmentGroups.getString(SMTableappointmentgroups.igroupid) ;
        		}      	
            		sUserTable.add((String) "<LABEL><INPUT TYPE=CHECKBOX" 
            			+ " ID=\"" + sGroupID + "\""  
            			+ " NAME=\"" + SMViewAppointmentCalendarSelection.USER_PREFIX +  sCurrentUserID + "\""
            			+ " CLASS=\"users\">" 
                		+ sCurrentUserFullName
                		+ "</LABEL>"
                	);
        			sGroupID = "";
        			rsAppointmentGroups.close();
        	} 	
        	rsUsers.close();

        	//Print the table:
        	out.println(SMUtilities.Build_HTML_Table(3, sUserTable,1,true));
        	
		}catch (SQLException ex){
			out.println("Error reading from users table - " + ex.getMessage() + "<BR>");
			//return false;
		}	
		

		out.println("<BR>");
	
		out.println("<INPUT TYPE=\"SUBMIT\" NAME=" 
				+ GENERATE_REPORT_PARAMETER 
				+ " VALUE=\"" + GENERATE_REPORT_LABEL + "\">&nbsp;&nbsp;");
		out.println("</FORM>");

		out.println("</BODY></HTML>");
	}
	
	private String sCommandScripts(){
			String s = "";
			
			s += "<NOSCRIPT>\n"
				+ "    <font color=red>\n"
				+ "    <H3>This page requires that JavaScript be enabled to function properly</H3>\n"
				+ "    </font>\n"
				+ "</NOSCRIPT>\n"
			;
 
			s += "<script type='text/javascript'>\n";				
			
			s += "window.onload = selectusers;\n";

			s += "function selectusers(){\n"
				+ "var appointmentgroups = document.getElementsByName(\"" + APPOINTMENT_GROUP_PARAMETER + "\");\n"
				     + "for (var i = 0; i < appointmentgroups.length; i++) {\n"
				//If a group is checked, check all users in that group
				     	+ " if(appointmentgroups[i].checked){\n"
				     			+ "var fullid = appointmentgroups[i].getAttribute('id');\n"
				     			+ "var id = fullid.split('*');\n"
				     			+ "var allusers = document.getElementsByClassName('users');\n"
				     				+ "for (var j = 0; j < allusers.length; j++) {\n"
				     				+ "var userID = \"Group\".concat(id[1]);\n"
				     					+ "if(allusers[j].getAttribute('id').indexOf(userID) != null "
				     					+ "&& allusers[j].getAttribute('id').indexOf(userID) > -1){\n"
				     						+ "allusers[j].checked = true;\n"
				     					+ "}\n"
				     				+ "}\n"
				 //If a group is not, uncheck all the users in that group
				     	+ "} else {\n"
				     	+ "var fullid = appointmentgroups[i].getAttribute('id');\n"
		     			+ "var id = fullid.split('*');\n"
		     			+ "var allusers = document.getElementsByClassName('users');\n"
		     				+ "for (var j = 0; j < allusers.length; j++) {\n"
		     				+ "var userID = \"Group\".concat(id[1]);\n"
		     					+ "if(allusers[j].getAttribute('id').indexOf(userID) != null "
		     					+ "&& allusers[j].getAttribute('id').indexOf(userID) > -1){\n"
		     					    + "allusers[j].checked = false;\n"
		     					+ "}\n"
		     				+ "}\n"
				     	+ "}\n"
			     	+ "}\n"
				   //Go back over all groups and to make sure a user was not unchecked that was in multiple groups
			     	+ "for (var i = 0; i < appointmentgroups.length; i++) {\n"
				     	+ " if(appointmentgroups[i].checked){\n"
				     			+ "var fullid = appointmentgroups[i].getAttribute('id');\n"
				     			+ "var id = fullid.split('*');\n"
				     			+ "var allusers = document.getElementsByClassName('users');\n"
				     				+ "for (var j = 0; j < allusers.length; j++) {\n"
				     				+ "var userID = \"Group\".concat(id[1]);\n"
				     					+ "if(allusers[j].getAttribute('id').indexOf(userID) != null "
				     					+ "&& allusers[j].getAttribute('id').indexOf(userID) > -1){\n"
				     						+ "allusers[j].checked = true;\n"
				     					+ "}\n"
				     				+ "}\n"	
				     	     + "}\n"
				      + "}\n"
				+ "}\n"
			;
			s += "</script>\n";
			return s;
		}
	public void doGet(HttpServletRequest request,
			HttpServletResponse response)
	throws ServletException, IOException {

		doPost(request, response);
	}

}