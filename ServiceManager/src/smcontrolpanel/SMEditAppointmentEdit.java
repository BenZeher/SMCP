package smcontrolpanel;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Enumeration;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import smcontrolpanel.SMSystemFunctions;
import SMClasses.SMAppointment;
import SMDataDefinition.SMTableappointments;
import SMDataDefinition.SMTableappointmentusergroups;
import SMDataDefinition.SMTableusers;
import ServletUtilities.clsServletUtilities;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsDateAndTimeConversions;
import ServletUtilities.clsManageRequestParameters;
import ServletUtilities.clsStringFunctions;

public class SMEditAppointmentEdit extends HttpServlet {

	private static final long serialVersionUID = 1L;
	
	public static final String RECORDWASCHANGED_FLAG = "RECORDWASCHANGEDFLAG";
	public static final String RECORDWASCHANGED_FLAG_VALUE = "RECORDWASCHANGED";
	public static final String ADDRESSCHANGE_FLAG = "ADDRESSCHANGEFLAG";
	public static final String ADDRESSCHANGED_VALUE = "ADDRESSCHANGED";
	public static final String COMMAND_FLAG = "COMMANDFLAG";
	public static final String COMMAND_VALUE_SAVE = "SaveEntry";
	public static final String BUTTON_LABEL_SAVE = " Update ";
	public static final String COMMAND_VALUE_DELETE = "DeleteEntry";
	public static final String BUTTON_LABEL_DELETE = " Delete ";
	
	public void doPost(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
		
		SMAppointment entry = new SMAppointment(request);
		
		SMMasterEditEntry smedit = new SMMasterEditEntry(
				request,
				response,
				getServletContext(),
				entry.getObjectName(),
				SMUtilities.getFullClassName(this.toString()),
				"smcontrolpanel.SMEditAppointmentAction",
				"smcontrolpanel.SMUserLogin",
				"Go back to user login",
				SMSystemFunctions.SMEditAppointmentCalendar
				);
		
		if (!smedit.processSession(getServletContext(), SMSystemFunctions.SMEditAppointmentCalendar)){
			smedit.getPWOut().println("Error in process session: " + smedit.getErrorMessages());
			return;
		}

		//See if this is a new record or not from the appointment entry id
		if (entry.getslid().compareToIgnoreCase("-1") == 0){
			entry.setsNewRecord(SMAppointment.ParamNewRecordValue);
		}else{
			entry.setsNewRecord("0");
		}
		
		//TODO Load object passed from appointment. 
		//If this is a 'resubmit', meaning it's being called by , then
		//the session will have a appointment entry entry object in it, and that's what we'll pick up.
		HttpSession currentSession = smedit.getCurrentSession();

		if (currentSession.getAttribute(SMAppointment.ParamObjectName) != null){
			//System.out.println("got an object from session");
			entry = (SMAppointment) currentSession.getAttribute(SMAppointment.ParamObjectName);
			currentSession.removeAttribute(SMAppointment.ParamObjectName);
			//But if it's NOT a 'resubmit', meaning this class was called for the first time to 
			//edit, we'll pick up the ID or key from the request and try to load the entry:
		}else{
			if (!entry.bIsNewRecord()){
				try{
				entry.load(getServletContext(), smedit.getsDBID(), smedit.getUserID(), smedit.getFullUserName());
				//System.out.println("entry loaded successfully.");	
				}catch (Exception e){
					response.sendRedirect(
							"" + SMUtilities.getURLLinkBase(getServletContext()) + "" + smedit.getCallingClass()
							+ "?" + SMAppointment.Paramlid + "=" + entry.getslid()
							+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + smedit.getsDBID()
							+ "&Warning=" + e.getMessage()
							);
				}
			}
		}
		
		//build navigation string to go back to appointment.
		String sNavigationStringParams = "";
		
		sNavigationStringParams += "&" + SMViewAppointmentCalendarSelection.DATE_RANGE_PARAM 
				+ "=" + SMViewAppointmentCalendarSelection.DATE_RANGE_CHOOSE;
		
		if (clsManageRequestParameters.get_Request_Parameter(
				SMViewAppointmentCalendarSelection.STARTING_DATE_FIELD, request) != null){
			sNavigationStringParams += "&" + SMViewAppointmentCalendarSelection.STARTING_DATE_FIELD + "=" + clsManageRequestParameters.get_Request_Parameter(
					SMViewAppointmentCalendarSelection.STARTING_DATE_FIELD, request);
			}
		if (clsManageRequestParameters.get_Request_Parameter(
				SMViewAppointmentCalendarSelection.ENDING_DATE_FIELD, request) != null){
			sNavigationStringParams += "&" + SMViewAppointmentCalendarSelection.ENDING_DATE_FIELD + "=" + clsManageRequestParameters.get_Request_Parameter(
					SMViewAppointmentCalendarSelection.ENDING_DATE_FIELD, request);
			} 
    	
		boolean bAllowCalendarEditing = request.getParameter(
        		SMViewAppointmentCalendarSelection.EDITAPPOINTMENT_PARAMETER) != null;
		
		if (bAllowCalendarEditing){
			sNavigationStringParams += "&" + SMViewAppointmentCalendarSelection.EDITAPPOINTMENT_PARAMETER + "=Y"; 
		}
		
		ArrayList<String> aUserNames = new ArrayList<String>();
		Enumeration<String> parameterNames = request.getParameterNames();
		while (parameterNames.hasMoreElements()){
			String paramName = (String) parameterNames.nextElement();
			 //String sParamValue = SMUtilities.get_Request_Parameter(paramName, request);
			 if(paramName.contains(SMViewAppointmentCalendarSelection.USER_PREFIX)){
				 aUserNames.add(paramName.substring(SMViewAppointmentCalendarSelection.USER_PREFIX.length()));
			 }
		}

		for(int i = 0; aUserNames.size() > i; i++){
			sNavigationStringParams += "&" + SMViewAppointmentCalendarSelection.USER_PREFIX + aUserNames.get(i);
		}
		
		
	    //Include all javascript and css
		smedit.printHeaderTable();
	    smedit.getPWOut().println(SMUtilities.getSMCPJSIncludeString(getServletContext()));
	    smedit.getPWOut().println(SMUtilities.getDatePickerIncludeString(getServletContext()));
	    smedit.getPWOut().println(clsServletUtilities.getJQueryIncludeString());
	    smedit.getPWOut().println(SMUtilities.getMasterStyleSheetLink());

	    //If this screen in being loaded from another screen no users will come with it. 
	    if(aUserNames.size() > 0){
			smedit.getPWOut().println(
					"<BR><A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMViewAppointmentCalendarGenerate?" 
						+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" 
						+ smedit.getsDBID() + sNavigationStringParams + "\">Back to Appointment Calendar</A><BR>");
	    }else{
	    	smedit.getPWOut().println(
					"<BR><A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMViewAppointmentCalendarSelection?" 
						+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" 
						+ smedit.getsDBID() + "\">View Appointment Calendar</A><BR>");
	    }
	    
		smedit.getPWOut().println("<BR>");
		smedit.setbIncludeDeleteButton(false);
		smedit.setbIncludeUpdateButton(false);
	    try {
			smedit.createEditPage(getEditHTML(smedit, entry, aUserNames), "");
		} catch (SQLException e) {
    		String sError = "Could not create edit page - " + e.getMessage();
			response.sendRedirect(
				"" + SMUtilities.getURLLinkBase(getServletContext()) + "" + smedit.getCallingClass()
				+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + smedit.getsDBID()
				+ "?Warning=" + sError
			);
				return;
		}
	}
	private String getEditHTML(SMMasterEditEntry sm, SMAppointment entry, ArrayList<String> arrUserNames) throws SQLException{

		String s = "";
		s += sCommandScript() + "\n";
		s += "<INPUT TYPE=HIDDEN NAME=\"" + RECORDWASCHANGED_FLAG + "\" VALUE=\"" 
			+ clsManageRequestParameters.get_Request_Parameter(RECORDWASCHANGED_FLAG, sm.getRequest()) + "\""
			+ " id=\"" + RECORDWASCHANGED_FLAG + "\"" + ">";
		
		//Store which command button the user has chosen:
		s += "<INPUT TYPE=HIDDEN NAME=\"" + COMMAND_FLAG + "\" VALUE=\"" + "" + "\""
			+ " id=\"" + COMMAND_FLAG + "\""
			+ "\">";
		
		//If new record store for the action class to pick up.
		if(entry.bIsNewRecord()){
			s += "<INPUT TYPE=HIDDEN NAME=\"" + SMAppointment.ParamNewRecord + "\" VALUE=\"" + entry.getsNewRecord() + "\""
				+ " id=\"" + SMAppointment.ParamNewRecord + "\""
				+ "\">";
		}
		
		if (entry.bIsNewRecord()){
			s += "<INPUT TYPE=HIDDEN NAME=\"" + ADDRESSCHANGE_FLAG + "\" VALUE=\"" 
				+ ADDRESSCHANGED_VALUE + "\""
				+ " id=\"" + ADDRESSCHANGE_FLAG + "\""
				+ "\">";
		//Otherwise, we only get it if it's re-submitted back to this class or changed with javascript:
		}else{
			s += "<INPUT TYPE=HIDDEN NAME=\"" + ADDRESSCHANGE_FLAG + "\" VALUE=\"" 
				+ clsManageRequestParameters.get_Request_Parameter(ADDRESSCHANGE_FLAG, sm.getRequest()) + "\""
				+ " id=\"" + ADDRESSCHANGE_FLAG + "\""
				+ "\">";
		}
		//Sore all the original appointment calendar report information to return to appointment after saving
		s += "<INPUT TYPE=HIDDEN NAME=\"" + SMViewAppointmentCalendarSelection.STARTING_DATE_FIELD + "\" VALUE=\"" 
				+ clsManageRequestParameters.get_Request_Parameter(SMViewAppointmentCalendarSelection.STARTING_DATE_FIELD, sm.getRequest()) + "\""
				 + ">";
		s += "<INPUT TYPE=HIDDEN NAME=\"" + SMViewAppointmentCalendarSelection.ENDING_DATE_FIELD + "\" VALUE=\"" 
				+ clsManageRequestParameters.get_Request_Parameter(SMViewAppointmentCalendarSelection.ENDING_DATE_FIELD, sm.getRequest()) + "\""
				 + ">";
		s += "<INPUT TYPE=HIDDEN NAME=\"" + SMViewAppointmentCalendarSelection.EDITAPPOINTMENT_PARAMETER + "\" VALUE=\"" 
				+ clsManageRequestParameters.get_Request_Parameter(SMViewAppointmentCalendarSelection.EDITAPPOINTMENT_PARAMETER, sm.getRequest()) + "\""
				 + ">";
		
		
		for(int i = 0; arrUserNames.size() > i; i++){
			
			s += "<INPUT TYPE=HIDDEN NAME=\"" + SMViewAppointmentCalendarSelection.USER_PREFIX + arrUserNames.get(i) + "\"" 
					+ " VALUE=\"" + arrUserNames.get(i) + "\""
				 + ">";
		}
//TODO
		s += "<TABLE BORDER=1>\n";
 		
		//ID
		s += "  <TR>\n"
			+ "    <TD ALIGN=RIGHT><B>ID</B>:</TD>\n"
			+ "    <TD><B>" ;
			if(entry.bIsNewRecord()){
				s	+= "<B>NEW</B>";	
			}else{
				s += entry.getslid();
			}
			s += " <INPUT TYPE=HIDDEN"
			+ " NAME = \"" + SMTableappointments.lid + "\""
			+ " ID = \"" + SMTableappointments.lid + "\""
			+ " VALUE = \"" + entry.getslid() + "\""
			+ ">"
			+ "</B></TD>\n"
			+ "    <TD>&nbsp;</TD>\n"
			+ "  </TR>\n"
				;
 
		//Created by
		if(!entry.bIsNewRecord()){
			
		s += "  <TR>\n"
			+ "    <TD ALIGN=RIGHT><B>Created by</B>:</TD>\n"
			+ "    <TD>" ;
		s += SMUtilities.getFullNamebyUserID(entry.getlcreateduserid(), getServletContext(), sm.getsDBID(), "SmEditAppointmentEdit.getEditHTML")
					+ " on " + clsDateAndTimeConversions.resultsetDateTimeToTheMinuteStringToString(entry.getdatcreatedtime());
		s += " <INPUT TYPE=HIDDEN"
			+ " NAME = \"" + SMTableappointments.lcreateduserid + "\""
			+ " ID = \"" + SMTableappointments.lcreateduserid + "\""
			+ " VALUE = \"" + entry.getlcreateduserid() + "\""
			+ ">";
		s += " <INPUT TYPE=HIDDEN"
			+ " NAME = \"" + SMTableappointments.datcreatedtime + "\""
			+ " ID = \"" + SMTableappointments.datcreatedtime + "\""
			+ " VALUE = \"" + entry.getdatcreatedtime() + "\""
			+ ">";
		s += "</TD>\n"
			+ "    <TD>&nbsp;</TD>\n"
			+ "  </TR>\n"
				;
		}
		
		  s	+= "<TR>\n"
			+ "<TD ALIGN=RIGHT><B>Scheduled for</B>:</TD>\n"
			+ "<TD>";
			
			
			s += "<SELECT NAME=\"" + SMAppointment.Paramluserid + "\">"
			    	+ "<OPTION VALUE=\"" + "" + "\">*** Select User ***";
			    
			//Drop down the list:
			String SQL = "SELECT DISTINCT " + SMTableusers.TableName + "." + SMTableusers.sUserFirstName
		        	+ "," + SMTableusers.TableName + "." + SMTableusers.sUserLastName
		        	+ "," + SMTableusers.TableName + "." + SMTableusers.sUserName
		        	+ "," + SMTableusers.TableName + "." + SMTableusers.lid
		        	+ " FROM " + SMTableappointmentusergroups.TableName
		        	+ " LEFT JOIN " + SMTableusers.TableName 
		        	+ " ON " + SMTableusers.TableName + "." + SMTableusers.lid 
		        	+ "=" + SMTableappointmentusergroups.TableName + "." + SMTableappointmentusergroups.luserid 
		        	+ " ORDER BY " + SMTableusers.TableName + "." + SMTableusers.sUserFirstName;
			    try {
					ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, getServletContext(),
									sm.getsDBID(), "MySQL", SMUtilities
									.getFullClassName(this.toString())
									+ ".getEditHTML - user: " 
									+ sm.getUserID()
									+ " - "
									+ sm.getFullUserName()
							);

					while (rs.next()) {

						String sUserID = clsDatabaseFunctions.getRecordsetStringValue(rs,SMTableusers.TableName + "." + SMTableusers.lid);
						if(sUserID.compareToIgnoreCase("") != 0) {
						s += "<OPTION";
						if (sUserID.compareToIgnoreCase(entry.getluserid()) == 0) {
							s += " SELECTED=yes";
						}
						s += " VALUE=\"" + sUserID + "\">" 
							+ clsDatabaseFunctions.getRecordsetStringValue(rs,SMTableusers.TableName + "." + SMTableusers.sUserFirstName) + " "
							+ clsDatabaseFunctions.getRecordsetStringValue(rs,SMTableusers.TableName + "." + SMTableusers.sUserLastName)
							;
						}
					}
					rs.close();
				} catch (Exception e) {
					s += "</SELECT><BR><B>Error reading " + "Appointment Calendar users" + " - " + e.getMessage();
				}
				s+= "</SELECT>";
			
			
			s += "</TD>\n"
			+ "    <TD> Schedule this appointment for a different user. </TD>\n"
			+ "  </TR>\n"
			;
//TODO
		//Date and Time
		String sDate = SMUtilities.EMPTY_DATE_VALUE;
		SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
		Calendar calCurrentDate = Calendar.getInstance();	

		if(entry.getdatentrydate().compareToIgnoreCase(SMUtilities.EMPTY_DATE_VALUE) != 0){
			try {
				calCurrentDate.setTime(dateFormat.parse(entry.getdatentrydate()));
				sDate = clsDateAndTimeConversions.CalendarToString(calCurrentDate, "MM/dd/yyyy");
			} catch (Exception e) {
				System.out.println("[1579268821] Error in time function - " + e.getMessage());
			}
		}

		s += "  <TR>\n"
			+ "<TD ALIGN=RIGHT><B>Date</B>:</TD>\n"
			+ "<TD>"
			+ "<INPUT TYPE=TEXT NAME=\"" + SMAppointment.Paramdatentrydate + "\""
		    + " VALUE=\"" + sDate + "\""
		    + " MAXLENGTH=" + "10"
		   	+ " SIZE = " + "8"
		   	+ " onchange=\"flagDirty();\""
		   	+ ">"
		   	+ "&nbsp;" + SMUtilities.getDatePickerString(SMTableappointments.datentrydate, getServletContext())
	    	+ "</TD>\n"
		   	+ "<TD> Change the date of the entry.</TD>\n"
			+ "  </TR>\n"
		   	;

		  s	+= "<TR>\n"
		   	+ "<TD ALIGN=RIGHT><B>Time</B>:</TD>\n"
		   	+ "<TD>"
		   	+ Create_Edit_Form_Time_Input_Field(SMAppointment.Paramiminuteofday, entry.getiminuteofday(), getServletContext())
		   	+ "    <TD> Change the time of the entry. </TD>\n"
			+ "  </TR>\n"
		    ;
	
		  //Email notification time selection
		  s	+= "<TR>\n"
			+ "<TD ALIGN=RIGHT><B>Email Notification Time</B>:</TD>\n"
			+ "<TD>"
			;
		  //if an appointment notification for this appointment has not been sent, choose a time
		  //TODO OR datentrydate < today
		  if(entry.getinotificationsent().compareToIgnoreCase("0") == 0 ){
			s += "<SELECT NAME=\"" + SMAppointment.Paraminotificationtime + "\">"
					;
			for(int i = 0; i < SMTableappointments.arrNotificationTimeIntervals.length; i++){
				s += "<OPTION";
				if (SMTableappointments.arrNotificationTimeIntervals[i][0].compareToIgnoreCase(entry.getinotificationtime()) == 0) {
					s += " SELECTED=yes";
				}
				s += " VALUE=\"" + SMTableappointments.arrNotificationTimeIntervals[i][0] + "\">" 
					+ SMTableappointments.arrNotificationTimeIntervals[i][1] + ""
					;
				}
		  }else{
			  //TODO calculate time notification was sent.
		/*	
			  SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/YYYY");
			  Date appointmentDate = null;
			try {
				appointmentDate = formatter.parse(entry.getdatentrydate());
			} catch (ParseException e) {
				System.out.println(e.getMessage());
			}
			  long appointmentDateUnixMinutes = (appointmentDate.getTime()/1000)/60;
			  long reminderTimeUnixMinutes = (appointmentDateUnixMinutes + Long.parseLong(entry.getiminuteofday()))
					  - Long.parseLong(entry.getinotificationtime());
			  Date reminderDate = new Date ();
			  reminderDate.setTime((long)(reminderTimeUnixMinutes*1000)*60);
		 */
			  s += " <i> *notification sent</i>";
		  }
			s += "</TD>";
			s += "    <TD> Select a time to receive an email reminder for this appointment. </TD>\n"
			+ "  </TR>\n"
			;
		  //Comment
		  s += "  <TR>\n"
			+ "<TD ALIGN=RIGHT><B>Comment</B>:</TD>\n"
			+ "<TD>" 				
			+ "<TEXTAREA NAME=\"" + SMAppointment.Parammcomment + "\""
			+ " rows=3"
			+ " cols=39"
			+ ">"
			+ entry.getmcomment()
			+ "</TEXTAREA>"
			+ "</TD>\n"
			+ "<TD> Comment will be displayed on the calendar. </TD>\n"
			+ "</TR>\n"
			;
		//Order number
		s += "  <TR>\n"
			+ "<TD ALIGN=RIGHT><B>Generated by Order Number</B>:</TD>\n"
			+ "<TD><B>" 
			+ " <INPUT TYPE=TEXT"
			+ " NAME = \"" + SMAppointment.Paramsordernumber + "\""
			+ " ID = \"" + SMAppointment.Paramsordernumber + "\""
			+ " VALUE = \"" + entry.getsordernumber() + "\""
			+ " MAXLENGTH=" + SMTableappointments.sordernumberlength
		    + " SIZE = " + "11"
			+ ">"
			+ "</B></TD>\n"
			+ "    <TD> Order number that this appointment was created from. </TD>\n"
			+ "  </TR>\n"
			;
		//Bid ID
		String sBidID = entry.getibidid();
		if(sBidID.compareToIgnoreCase("0") == 0){
			sBidID = "";
		}
		s += "  <TR>\n"
			+ "    <TD ALIGN=RIGHT><B>Generated by Sales Lead</B>:</TD>\n"
			+ "    <TD><B>" 
			+ " <INPUT TYPE=TEXT"
			+ " NAME = \"" + SMAppointment.Paramibidid + "\""
			+ " ID = \"" + SMAppointment.Paramibidid + "\""
			+ " VALUE = \"" + sBidID + "\""
			+ " MAXLENGTH=" + SMTableappointments.ibididlength
		    + " SIZE = " + "11"
			+ ">"
			+ "</B></TD>\n"
			+ "    <TD> Sales lead that this appointment was created from.. </TD>\n"
			+ "  </TR>\n"
			;
		//Sales contact ID:
		String sSalesContactID = entry.getisalescontactid();
		if(sSalesContactID.compareToIgnoreCase("0") == 0){
			sSalesContactID = "";
		}
		s += "  <TR>\n"
			+ "    <TD ALIGN=RIGHT><B>Generated by Sales Contact ID</B>:</TD>\n"
			+ "    <TD><B>" 
			+ " <INPUT TYPE=TEXT"
			+ " NAME = \"" + SMAppointment.Paramisalescontactid + "\""
			+ " ID = \"" + SMAppointment.Paramisalescontactid + "\""
			+ " VALUE = \"" + sSalesContactID + "\""
			+ " MAXLENGTH=" + SMTableappointments.isalescontactidlength
		    + " SIZE = " + "11"
			+ ">"
			+ "</B></TD>\n"
			+ "    <TD> Sales contact that this appointment was created from. </TD>\n"
			+ "  </TR>\n"
				;
		
		//Contact Name:
		s += "  <TR>\n"
			+ "    <TD ALIGN=RIGHT><B>Contact Name</B>:</TD>\n"
			+ "    <TD><B>" 
			+ " <INPUT TYPE=TEXT"
			+ " NAME = \"" + SMAppointment.Paramscontactname + "\""
			+ " ID = \"" + SMAppointment.Paramscontactname + "\""
			+ " VALUE = \"" + entry.getscontactname() + "\""
			+ " MAXLENGTH=" + SMTableappointments.icontactnameLength
		    + " SIZE = " + "40"
			+ ">"
			+ "</B></TD>\n"
			+ "    <TD> Person of contact for this appointment. </TD>\n"
			+ "  </TR>\n"
			;
		
		//Bill to Name:
		s += "  <TR>\n"
			+ "    <TD ALIGN=RIGHT><B>Bill to Name</B>:</TD>\n"
			+ "    <TD><B>" 
			+ " <INPUT TYPE=TEXT"
			+ " NAME = \"" + SMAppointment.Paramsbilltoname + "\""
			+ " ID = \"" + SMAppointment.Paramsbilltoname + "\""
			+ " VALUE = \"" + entry.getsbilltoname() + "\""
			+ " MAXLENGTH=" + SMTableappointments.ibilltonameLength
		    + " SIZE = " + "40"
			+ ">"
			+ "</B></TD>\n"
			+ "    <TD>&nbsp;</TD>\n"
			+ "  </TR>\n"
			;
		
		//Ship to Name:
		s += "  <TR>\n"
			+ "    <TD ALIGN=RIGHT><B>Ship to Name</B>:</TD>\n"
			+ "    <TD><B>" 
			+ " <INPUT TYPE=TEXT"
			+ " NAME = \"" + SMAppointment.Paramsshiptoname + "\""
			+ " ID = \"" + SMAppointment.Paramsshiptoname + "\""
			+ " VALUE = \"" + entry.getsshiptoname() + "\""
			+ " MAXLENGTH=" + SMTableappointments.ishiptonameLength
		    + " SIZE = " + "40"
			+ ">"
			+ "</B></TD>\n"
			+ "    <TD>&nbsp;</TD>\n"
			+ "  </TR>\n"
			;
		
		//Address 1:
		s += "  <TR>\n"
			+ "    <TD ALIGN=RIGHT><B>Address 1</B>:</TD>\n"
			+ "    <TD><B>" 
			+ " <INPUT TYPE=TEXT"
			+ " NAME = \"" + SMAppointment.Paramsaddress1 + "\""
			+ " ID = \"" + SMAppointment.Paramsaddress1 + "\""
			+ " VALUE = \"" + entry.getsaddress1() + "\""
			+ " MAXLENGTH=" + SMTableappointments.saddress1Length
		    + " SIZE = " + "40"
		    + " onchange=\"flagShipToDirty();\""
			+ ">"
			+ "</B></TD>\n"
			+ "    <TD> Enter the address of the appointment. </TD>\n"
			+ "  </TR>\n"
			;
		//Address 2:
		s += "  <TR>\n"
			+ "    <TD ALIGN=RIGHT><B>Address 2</B>:</TD>\n"
			+ "    <TD><B>" 
			+ " <INPUT TYPE=TEXT"
			+ " NAME = \"" + SMAppointment.Paramsaddress2 + "\""
			+ " ID = \"" + SMAppointment.Paramsaddress2 + "\""
			+ " VALUE = \"" + entry.getsaddress2() + "\""
			+ " MAXLENGTH=" + SMTableappointments.saddress2Length
		    + " SIZE = " + "40"
		    + " onchange=\"flagShipToDirty();\""
			+ ">"
			+ "</B></TD>\n"
			+ "    <TD>&nbsp;</TD>\n"
			+ "  </TR>\n"
			;
		//Address 3:
		s += "  <TR>\n"
			+ "    <TD ALIGN=RIGHT><B>Address 3</B>:</TD>\n"
			+ "    <TD><B>" 
			+ " <INPUT TYPE=TEXT"
			+ " NAME = \"" + SMAppointment.Paramsaddress3 + "\""
			+ " ID = \"" + SMAppointment.Paramsaddress3 + "\""
			+ " VALUE = \"" + entry.getsaddress3() + "\""
			+ " MAXLENGTH=" + SMTableappointments.saddress3Length
		    + " SIZE = " + "40"
		    + " onchange=\"flagShipToDirty();\""
			+ ">"
			+ "</B></TD>\n"
			+ "<TD>&nbsp;</TD>\n"
			+ "</TR>\n"
			;
		//Address 4:
		s += "  <TR>\n"
			+ "<TD ALIGN=RIGHT><B>Address 4</B>:</TD>\n"
			+ "<TD><B>" 
			+ " <INPUT TYPE=TEXT"
			+ " NAME = \"" + SMAppointment.Paramsaddress4 + "\""
			+ " ID = \"" + SMAppointment.Paramsaddress4 + "\""
			+ " VALUE = \"" + entry.getsaddress4() + "\""
			+ " MAXLENGTH=" + SMTableappointments.saddress4Length
		    + " SIZE = " + "40"
		    + " onchange=\"flagShipToDirty();\""
			+ ">"
			+ "</B></TD>\n"
			+ "<TD>&nbsp;</TD>\n"
			+ "</TR>\n"
			;
		//City:
		s += "  <TR>\n"
			+ "<TD ALIGN=RIGHT><B>City</B>:</TD>\n"
			+ "<TD><B>" 
			+ "<INPUT TYPE=TEXT"
			+ " NAME = \"" + SMAppointment.Paramscity + "\""
			+ " ID = \"" + SMAppointment.Paramscity + "\""
			+ " VALUE = \"" + entry.getscity() + "\""
			+ " MAXLENGTH=" + SMTableappointments.saddress1Length
		    + " SIZE = " + "20"
		    + " onchange=\"flagShipToDirty();\""
			+ ">"
			+ "</B></TD>\n"
			+ "    <TD>&nbsp;</TD>\n"
			+ "  </TR>\n"
			;
		//State:
		s += "  <TR>\n"
			+ "<TD ALIGN=RIGHT><B>State</B>:</TD>\n"
			+ "<TD><B>" 
			+ " <INPUT TYPE=TEXT"
			+ " NAME = \"" + SMAppointment.Paramsstate + "\""
			+ " ID = \"" + SMAppointment.Paramsstate + "\""
			+ " VALUE = \"" + entry.getsstate() + "\""
			+ " MAXLENGTH=" + SMTableappointments.sstateLength
		    + " SIZE = " + "20"
		    + " onchange=\"flagShipToDirty();\""
			+ ">"
			+ "</B></TD>\n"
			+ "<TD>&nbsp;</TD>\n"
			+ "</TR>\n"
			;
		//Zip code:
		s += "  <TR>\n"
			+ "<TD ALIGN=RIGHT><B>Zip Code</B>:</TD>\n"
			+ " <TD><B>" 
			+ " <INPUT TYPE=TEXT"
			+ " NAME = \"" + SMAppointment.Paramszip + "\""
			+ " ID = \"" + SMAppointment.Paramszip + "\""
			+ " VALUE = \"" + entry.getszip() + "\""
			+ " MAXLENGTH=" + SMTableappointments.szipLength
		    + " SIZE = " + "20"
		    + " onchange=\"flagShipToDirty();\""
			+ ">"
			+ "</B></TD>\n"
			+ "<TD>&nbsp;</TD>\n"
			+ "</TR>\n"
			;
		//Geocode:
		String sGeocode = entry.getsgeocode();
		//If this is not a new record, geocode did not return an address, or it is blank display invalid 
		if(!entry.bIsNewRecord() && 
			(sGeocode.compareToIgnoreCase(SMGeocoder.EMPTY_GEOCODE) == 0 || sGeocode.compareToIgnoreCase("") == 0)){
			sGeocode = " <font color=\"red\"> Invalid</font>";
		}
		s += "  <TR>\n"
			+ "<TD ALIGN=RIGHT><B>Geocode (latitude/longitude)</B>:</TD>\n"
			+ " <TD>" 
			+ "" + sGeocode + ""
			+ "</TD>\n"
			+ "<TD> If the geocode is invalid this appointment will not appear on the Map.</TD>\n"
			+ "</TR>\n"
			;
		s += "<INPUT TYPE=HIDDEN NAME=\"" + SMAppointment.Paramsgeocode + "\" VALUE=\"" + entry.getsgeocode() + "\"" + ">";
		
		//Phone:
		if(entry.getslid().compareToIgnoreCase("-1") == 0) {
			s += "  <TR>\n"
					+ "<TD ALIGN=RIGHT><B>Phone</B>:</TD>\n"
					+ " <TD><B>" 
					+ " <INPUT TYPE=TEXT"
					+ " NAME = \"" + SMAppointment.Paramsphone + "\""
					+ " ID = \"" + SMAppointment.Paramsphone + "\""
					+ " VALUE = \"" + entry.getsphone() + "\""
					+ " MAXLENGTH=" + SMTableappointments.iphoneLength
					+ " SIZE = " + "20"
					+ ">"
					+ "</B></TD>\n"
					+ "<TD> Phone number of contact person. </TD>\n"
					+ "</TR>\n"
					;
		}else {
			s += "  <TR>\n"
					+ "<TD ALIGN=RIGHT><A HREF=\"tel:" +entry.getsphone()  + "\"><B>Phone</B></A>:</TD>\n"
					+ " <TD><B>" 
					+ " <INPUT TYPE=TEXT"
					+ " NAME = \"" + SMAppointment.Paramsphone + "\""
					+ " ID = \"" + SMAppointment.Paramsphone + "\""
					+ " VALUE = \"" + entry.getsphone() + "\""
					+ " MAXLENGTH=" + SMTableappointments.iphoneLength
					+ " SIZE = " + "20"
					+ ">"
					+ "</B></TD>\n"
					+ "<TD> Phone number of contact person. </TD>\n"
					+ "</TR>\n"
					;
		}
		
		//Email:
		s += "  <TR>\n"
			+ "<TD ALIGN=RIGHT><B>Email</B>:</TD>\n"
			+ " <TD><B>" 
			+ " <INPUT TYPE=TEXT"
			+ " NAME = \"" + SMAppointment.Paramsemail + "\""
			+ " ID = \"" + SMAppointment.Paramsemail + "\""
			+ " VALUE = \"" + entry.getsemail() + "\""
			+ " MAXLENGTH=" + SMTableappointments.iemailLength
		    + " SIZE = " + "40"
			+ ">"
			+ "</B></TD>\n"
			+ "<TD> Email address of contact person. </TD>\n"
			+ "</TR>\n"
			;
		s += "</TABLE>";
		
		s += createSaveButton();
		s += "&nbsp;"; 
		s += createDeleteButton();
		
		return s;
	}
	
	private String Create_Edit_Form_Time_Input_Field (
			String sDateFieldName,
			String sValue,
			ServletContext context
	){
		//sValue in format HH:mm AM
		try{
			int i = Integer.parseInt(sValue);
			sValue = SMAppointment.timeIntegerToString(i);
		}catch (Exception e){
			//Do nothing. The time is in the correct format
		}
		
		String s = "";

		int iMinute = Integer.parseInt(
				sValue.substring(sValue.indexOf(":") + 1, sValue.indexOf(":") + 3));
		int iAMPM = 0;
		if (clsStringFunctions.StringRight(sValue, 2).compareToIgnoreCase("AM") == 0){
			iAMPM = 0;
		}else{
			iAMPM = 1;
		}
		String sHour = sValue.substring(0, sValue.indexOf(":"));
		int iHour = Integer.parseInt(sHour);
		//if (iHour == 0 && iAMPM == 1){
		if (iHour == 0){
			iHour = 12;
		}
		s += "&nbsp;<SELECT NAME=\"" + sDateFieldName + "SelectedHour\"";
		s += " onchange=\"flagDirty();\"";
		s += ">";
		for (int i=1; i<=12;i++){
			if (i == iHour){
				s += "<OPTION SELECTED VALUE = " + i + ">" + i;
			}else{
				s += "<OPTION VALUE = " + i + ">" + i;
			}
		}
		s += "</SELECT>";
		s += "<B>:</B>&nbsp;<SELECT NAME=\"" + sDateFieldName + "SelectedMinute\"";
		s += " onchange=\"flagDirty();\"";		
		s += ">";
		for (int i=0; i<=59;i++){
			String sMinute = clsStringFunctions.PadLeft(Integer.toString(i), "0", 2);
			if (i == iMinute){
				s += "<OPTION SELECTED VALUE = " 
					+ sMinute + ">" + sMinute;
			}else{
				s += "<OPTION VALUE = " + sMinute + ">" + sMinute;
			}
		}
		
		s += "</SELECT>";	
		s += "&nbsp;<SELECT NAME=\"" + sDateFieldName + "SelectedAMPM\"";
		s += " onchange=\"flagDirty();\"";
		s += ">";
		for (int i=Calendar.AM; i<=Calendar.PM;i++){
			if (i == iAMPM){
				if (i == Calendar.AM){
					s+= "<OPTION SELECTED VALUE = " + Calendar.AM + ">" + "AM";
				}else{
					s += "<OPTION SELECTED VALUE = " + Calendar.PM + ">" + "PM";
				}		
			}else{
				if (i == Calendar.AM){
					s += "<OPTION VALUE = " + Calendar.AM + ">" + "AM";
				}else{
					s += "<OPTION VALUE = " + Calendar.PM + ">" + "PM";
				}
			}
		}
		s += "</SELECT>";

		return s;
	}
	private String sCommandScript(){
		String s = "";
		s += "<NOSCRIPT>\n"
			+ "		<font color=red>\n"
			+ "		<H3>This page requires that JavaScript be enabled to function properly</H3>\n"
			+ "		</font>\n"
			+ "</NOSCRIPT>\n"
			;
		
		s += "<script type='text/javascript'>\n";
		
		//Prompt to save:
		s += "window.onbeforeunload = promptToSave;\n";
		
		s += "function promptToSave(){\n"		
				//If the record WAS changed, then
				+ "	if(document.getElementById(\"" + COMMAND_FLAG + "\").value == \"" 
					+ COMMAND_VALUE_SAVE + "\" ){ \n"
							+ "return;\n"
					+ "}\n"
				+ "	if(document.getElementById(\"" + COMMAND_FLAG + "\").value == \"" 
					+ COMMAND_VALUE_DELETE + "\" ){ \n"
							+ "return;\n"
					+ "}\n"
				+ "    if (document.getElementById(\"" + RECORDWASCHANGED_FLAG + "\").value == \"" 
					+ RECORDWASCHANGED_FLAG_VALUE + "\" ){\n"

				+ "        return 'You have unsaved changes.';\n"
				+ "    }\n"
				+ "}\n\n"
			;
		
		s += "function flagDirty() {\n"
				+ "    document.getElementById(\"" + RECORDWASCHANGED_FLAG + "\").value = \"" 
				+ RECORDWASCHANGED_FLAG_VALUE + "\";\n"
			+ "}\n";
		
		s += "function save(){\n"
				+ "    document.getElementById(\"" + COMMAND_FLAG + "\").value = \"" + COMMAND_VALUE_SAVE + "\";\n"
				+ "    document.forms[\"" + SMMasterEditEntry.MAIN_FORM_NAME + "\"].submit();\n"
				+ "}\n"
			;
		s += "function deleteEntry(){\n"
				+ "var bresult = confirm(\"This appointment will be deleted. \");\n"
				+ "if (bresult) {\n"
				+ "    document.getElementById(\"" + COMMAND_FLAG + "\").value = \"" + COMMAND_VALUE_DELETE + "\";\n"
				+ "    document.forms[\"" + SMMasterEditEntry.MAIN_FORM_NAME + "\"].submit();\n"
				+ "}\n"
				
				+ "}\n"
			;
		s += "function flagShipToDirty() {\n"
				+ "    document.getElementById(\"" + ADDRESSCHANGE_FLAG + "\").value = \"" 
					 + ADDRESSCHANGED_VALUE + "\";\n"
				+ "    flagDirty();\n"
				+ "}\n"
			;
		s += " </script>\n";
		
		return s;
	}
	private String createSaveButton(){
		return "<button type=\"button\""
				+ " value=\"" + BUTTON_LABEL_SAVE + "\""
				+ " name=\"" + BUTTON_LABEL_SAVE + "\""
				+ " onClick=\"save();\">"
				+ BUTTON_LABEL_SAVE
				+ "</button>\n"
				;
	}
	
	private String createDeleteButton(){
		return "<button type=\"button\""
				+ " value=\"" + BUTTON_LABEL_DELETE + "\""
				+ " name=\"" + BUTTON_LABEL_DELETE + "\""
				+ " onClick=\"deleteEntry();\">"
				+ BUTTON_LABEL_DELETE
				+ "</button>\n"
				;
	}
	public void doGet(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
		
		doPost(request, response);
	}
}
