package smas;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.util.ArrayList;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import smcontrolpanel.SMBackgroundJobManager;
import smcontrolpanel.SMMasterEditEntry;
import smcontrolpanel.SMSystemFunctions;
import smcontrolpanel.SMUtilities;
import SMDataDefinition.SMTablessalarmsequences;
import SMDataDefinition.SMTablessdevices;
import SMDataDefinition.SMTablesseventscheduledetails;
import SMDataDefinition.SMTablesseventschedules;
import ServletUtilities.clsCreateHTMLTableFormFields;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsServletUtilities;
import ServletUtilities.clsStringFunctions;

public class ASEditEventSchedulesEdit  extends HttpServlet {

	private static final long serialVersionUID = 1L;
	private static final String OBJECT_NAME = SSEventSchedule.ParamObjectName;
	private static final String DARK_ROW_BG_COLOR = "#cceeff"; //Light blue
	private static final String LIGHT_ROW_BG_COLOR = "#FFFFFF";
	
	public static final String DAYOFWEEKLABEL_SUNDAY = "Sunday";
	public static final String DAYOFWEEKLABEL_MONDAY = "Monday";
	public static final String DAYOFWEEKLABEL_TUESDAY = "Tuesday";
	public static final String DAYOFWEEKLABEL_WEDNESDAY = "Wednesday";
	public static final String DAYOFWEEKLABEL_THURSDAY = "Thursday";
	public static final String DAYOFWEEKLABEL_FRIDAY = "Friday";
	public static final String DAYOFWEEKLABEL_SATURDAY = "Saturday";
	
	public void doPost(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
		
		SSEventSchedule entry = new SSEventSchedule(request);
		SMMasterEditEntry smedit = new SMMasterEditEntry(
				request,
				response,
				getServletContext(),
				entry.getObjectName(),
				SMUtilities.getFullClassName(this.toString()),
				"smas.ASEditEventSchedulesAction",
				"smcontrolpanel.SMUserLogin",
				"Go back to user login",
				SMSystemFunctions.ASEditEventSchedules
				);
		
		if (!smedit.processSession(getServletContext(), SMSystemFunctions.ASEditEventSchedules)){
			smedit.getPWOut().println("Error in process session: " + smedit.getErrorMessages());
			return;
		}
		//If this is a 'resubmit', meaning it's being called by the 'Action' class, then
		//the session will have an entry object in it, and that's what we'll pick up.
		HttpSession currentSession = smedit.getCurrentSession();
		
	    if (currentSession.getAttribute(OBJECT_NAME) != null){
	    	entry = (SSEventSchedule) currentSession.getAttribute(OBJECT_NAME);
	    	currentSession.removeAttribute(OBJECT_NAME);
	    //But if it's NOT a 'resubmit', meaning this class was called for the first time to 
	    //edit, we'll pick up the ID or key from the request and try to load the entry:
	    }else{
	    	if (!smedit.getAddingNewEntryFlag()){
	    		try {
					entry.load(getServletContext(), smedit.getsDBID(), smedit.getUserID(), smedit.getFullUserName());
				} catch (Exception e) {
					response.sendRedirect(
							"" + SMUtilities.getURLLinkBase(getServletContext()) + "smas.ASEditEventSchedulesSelect"
							+ "?" + SMTablesseventschedules.lid + "=" + entry.getslid()
							+ "&Warning=" + e.getMessage()
							+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + smedit.getsDBID()
						);
						return;
				}
	    	}else{
	    		//Make sure the program knows we are adding a new record:
	    		entry.setslid("-1");
	    	}
	    }
	    smedit.printHeaderTable();
	    //Add a link to return to the original URL:
	    if (smedit.getOriginalURL().trim().compareToIgnoreCase("") !=0 ){
		    smedit.getPWOut().println(
		    		"<A HREF=\"" + smedit.getOriginalURL().replace("*", "&") + "\">" 
		    		+ "Back to report" + "</A>");
	    }
	    
		smedit.getPWOut().println(
				"<BR><A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smas.ASMainMenu?" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" 
					+ smedit.getsDBID() + "\">Return to Alarm Systems Main Menu</A><BR>");
	    
		smedit.getPWOut().println("<BR>");
	    try {
			smedit.createEditPage(getEditHTML(smedit, entry), "");
		} catch (Exception e) {
    		String sError = "Could not create edit page - " + e.getMessage();
			response.sendRedirect(
				"" + SMUtilities.getURLLinkBase(getServletContext()) + "" + "smas.ASEditEventSchedulesSelect"
				+ "?" + SMTablesseventschedules.lid + "=" + entry.getslid()
				+ "&Warning=Could not load " + OBJECT_NAME + " with ID: " + entry.getslid() + " - " + sError
				+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + smedit.getsDBID()
			);
			return;
		}
	    return;
	}
	private String getEditHTML(SMMasterEditEntry sm, SSEventSchedule entry) throws Exception{

		String s = "";
		s += "\n" + sStyleScripts() + "\n";
		Connection conn = null;
		try {
			conn = clsDatabaseFunctions.getConnection(
				getServletContext(), 
				sm.getsDBID(), 
				"MySQL", 
				SMUtilities.getFullClassName(this.toString() + ".getEditHTML - user: " 
				+ sm.getUserID()
				+ " - "
				+sm.getFullUserName()));
		} catch (Exception e1) {
			throw new Exception("Error [1481840378] getting connection - " + e1.getMessage());
		}
		
		//Don't think we even need this - it's ok to edit a schedule, even if the schedule is currently running:
		//if (
		//	entry.isCurrentlyLive(conn)
		//){
		//	s += "<B><FONT COLOR=RED>Event schedule '" + entry.getsname() + "' is currenty live and cannot be edited"
		//		+ " until it is first made inactive.</FONT></B>";
		//	return s;
		//}
		
		s += "<TABLE class = \" basicwithborder \" >";
		String sID = "NEW";
		if ((entry.getslid().compareToIgnoreCase("-1") != 0)){
			sID = entry.getslid();
		}
		s += "<TR><TD ALIGN=RIGHT><B>Event schedule ID</B>:</TD><TD><B>" 
			+ sID 
			+ "<INPUT TYPE=HIDDEN NAME=\"" + SMTablesseventschedules.lid + "\" VALUE=\"" 
			+ entry.getslid() + "\">"
			+ "</B></TD><TD>&nbsp;</TD></TR>";
		
		//If we are trying to add a new entry:
		if (entry.getslid().compareToIgnoreCase("-1") == 0){
			
			s += clsCreateHTMLTableFormFields.Create_Edit_Form_Text_Input_Row(
					SMTablesseventschedules.sname,
					entry.getsname().replace("\"", "&quot;"), 
					SMTablesseventschedules.snamelength, 
					"<B>Schedule name: <FONT COLOR=RED>*Required*</FONT></B>",
					"Give the event schedule a short, identifying name, like 'Daily office alarm', or 'Warehouse Daily', etc..",
					"50"
			);
		}else{

			s += "<TR><TD ALIGN=RIGHT><B>Schedule name: <FONT COLOR=RED>*Required*</FONT></B>:</TD>"
				+ "<TD>" 
				+ "<B>" + entry.getsname() + "</B>" 
				+ "<INPUT TYPE=HIDDEN NAME=\"" + SMTablesseventschedules.sname + "\" VALUE=\"" 
					+ entry.getsname() + "\">"
				+ "</TD>"
				+ "<TD>&nbsp;</TD>"
				+ "</TR>"
				;
		}
		
		//Description
		s += clsCreateHTMLTableFormFields.Create_Edit_Form_Text_Input_Row(
				SMTablesseventschedules.sdescription,
			entry.getsdescription().replace("\"", "&quot;"), 
			SMTablesseventschedules.sdescriptionlength, 
			"<B>Description: <FONT COLOR=RED>*Required*</FONT></B>",
			"Enter a description that clearly identifies the schedule, like 'Fresno office, warehouse and yard'.",
			"75"
		);

		//Active?
		s += clsCreateHTMLTableFormFields.Create_Edit_Form_Checkbox_Row(
				SMTablesseventschedules.iactive, 
				Integer.parseInt(entry.getsactive()), 
				"Active? <FONT COLOR=RED>*Required*</FONT>", 
				"De-activate a schedule with this: if unchecked, this schedule won't run, no matter what time or day it is currently."
			);
		
		//Create a list of the days of the week on which this schedule will start running:
		//TODO:
		s += "<TR>";
		s += "<TD ALIGN=RIGHT VALIGN=TOP><B>" + "Days&nbsp;of&nbsp;week&nbsp;<FONT COLOR=RED>*Required*</FONT>:" + " </B></TD>";
		s += "<TD ALIGN=LEFT>";
		
		ArrayList<String>arrWeekdays = new ArrayList<String>(0);
		arrWeekdays.add(DAYOFWEEKLABEL_SUNDAY);
		arrWeekdays.add(DAYOFWEEKLABEL_MONDAY);
		arrWeekdays.add(DAYOFWEEKLABEL_TUESDAY);
		arrWeekdays.add(DAYOFWEEKLABEL_WEDNESDAY);
		arrWeekdays.add(DAYOFWEEKLABEL_THURSDAY);
		arrWeekdays.add(DAYOFWEEKLABEL_FRIDAY);
		arrWeekdays.add(DAYOFWEEKLABEL_SATURDAY);
		int iWeekdayValue = Integer.parseInt(entry.getsdaysoftheweek());
		for (int i = 1; i < 8; i++){
			s += "<INPUT TYPE=CHECKBOX";
			Double dExponentialPower = Math.pow(Double.parseDouble("2"), Double.parseDouble(Integer.toString(i)));
			int iExponentialPower = dExponentialPower.intValue();
			if ((iWeekdayValue & iExponentialPower) > 0){
				s += clsServletUtilities.CHECKBOX_CHECKED_STRING;
			}
			s += " NAME=\"" + arrWeekdays.get(i - 1) + "\" width=0.25>";
			s += "&nbsp;" + arrWeekdays.get(i - 1);
			s += "<BR>";
			//s += "<B> " + iWeekdayValue + ", " + Math.pow(Double.parseDouble("2"), Double.parseDouble(Integer.toString(i))) + "</B><BR>";
		}
		
		s += "</TD>";
		s += "</TR>";
		
		//Start time:
		ArrayList<String>arrDisplayedValues = new ArrayList<String>(0);
		ArrayList<String>arrActualMinuteValues = new ArrayList<String>(0);
		for (int ampm = 0; ampm <=1; ampm++){
			for (int hr = 0; hr < 12; hr++){
				for (int qtr = 0; qtr <= 3; qtr ++){
					String sSuffix = "AM";
					if (ampm == 1){
						sSuffix = "PM";
					}
					int iHour = hr;
					if (hr == 0){
						iHour = 12;
					}
					arrDisplayedValues.add(Integer.toString(iHour) + ":" + clsStringFunctions.PadLeft(Integer.toString(15 * qtr), "0", 2) + " " + sSuffix);
					arrActualMinuteValues.add(Integer.toString((ampm * (12 * 60)) + (hr * 60) + (qtr * 15)));
				}
			}
		}
		
		s += clsCreateHTMLTableFormFields.Create_Edit_Form_List_Row(
				SMTablesseventschedules.istarttime, 
				arrActualMinuteValues, 
				entry.getsstarttime(), 
				arrDisplayedValues, 
				"Start&nbsp;time&nbsp;<FONT COLOR=RED>*Required*</FONT>:", 
				"Set the time you want the event to start each selected day."
			);
		
		//Duration (in minutes):
		//Description
		s += clsCreateHTMLTableFormFields.Create_Edit_Form_Text_Input_Row(
				SMTablesseventschedules.idurationinminutes,
			entry.getsdurationinminutes().replace("\"", "&quot;"), 
			4, 
			"<B>Duration<SUP><a href=\"#duration\">1</a></SUP>&nbsp;in&nbsp;minutes:&nbsp;<FONT COLOR=RED>*Required*</FONT></B>",
			"How long the event should last after it's started.  For example, to set an event to run for for 12 hours, set the"
				+ " duration to 720 minutes, which is 12 hours times 60 minutes per hour.  To run for 24 hours, set it to 1440.",
			"12"
		);
		
		s += "</TABLE>";
		
		boolean bAllowDeviceEdit = SMSystemFunctions.isFunctionPermitted(
				SMSystemFunctions.ASEditDevices, 
				sm.getUserID(), 
				conn,
				(String) sm.getCurrentSession().getAttribute(SMUtilities.SMCP_SESSION_PARAM_LICENSE_MODULE_LEVEL));
		
		boolean bAllowAlarmSequenceEdit = SMSystemFunctions.isFunctionPermitted(
				SMSystemFunctions.ASEditAlarmSequences, 
				sm.getUserID(), 
				conn,
				(String) sm.getCurrentSession().getAttribute(SMUtilities.SMCP_SESSION_PARAM_LICENSE_MODULE_LEVEL));
		
		//Next display the alarm sequences and devices:
		s += displayDevicesAndSequences(conn, entry, bAllowDeviceEdit, bAllowAlarmSequenceEdit);
		clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1547067609]");
		

		s += "<BR><BR><SUP><a name=\"duration\">1</a></SUP><B>Duration</B>: For devices, the duration determines how long the contacts will remain closed.  A 'duration' of 8 hours means that the schedule will hold"
			+ " the terminal contacts CLOSED continuously for 8 hours.  After hour hours, the schedule will OPEN the contacts.  For alarm sequences, the alarm sequence will be set at the beginning"
			+ " of the scheduled period, and will periodically (every " + Integer.toString(SMBackgroundJobManager.POLLING_FREQUENCY_IN_MINUTES) + " minute(s)) update and re-set the alarm if necessary.  At the end of the scheduled period, the alarm sequence will be disarmed"
			+ " and disarmed every schedule cycle (every " + Integer.toString(SMBackgroundJobManager.POLLING_FREQUENCY_IN_MINUTES) + " minute(s))."
			;
		s += "<BR><SUP><a name=\"resetdelay\">2</a></SUP><B>Reset delay</B>: This ONLY applies to alarm sequences.  If an alarm sequence has been manually disarmed, the schedule will delay the 'reset'"
				+ " of the alarm for a specified number of minutes.  That number of minutes is the 'reset delay'."
			;
		s += "<BR>";
		
		return s;
	}

	private String displayDevicesAndSequences(
		Connection conn, 
		SSEventSchedule entry, 
		boolean bAllowDeviceEdit,
		boolean bAllowAlarmSequenceEdit) throws Exception{
		String s = "";
		String sBackgroundColor = "";
		boolean bOddRow = true;

		s += "<BR><I><B><U>Configure alarm sequences and individual devices to be scheduled here:</U></B></I>";

		s += "<TABLE class = \" basicwithborder \" >";

		//Display the headings:
		s += "<TR style = \" background-color: black; color: white; \" >";

		s += "<TD class = \" leftjustifiedheading \" >" + "Device/Alarm Sequence" + "</TD>"
				+ "<TD class = \" centerjustifiedheading \" >" + "ID" + "</TD>"
				+ "<TD class = \" leftjustifiedheading \" >" + "Description" + "</TD>"
				+ "<TD class = \" leftjustifiedheading \" >" + "Reset delay" + "</TD>"
				+ "<TD class = \" centerjustifiedheading \" >" + "Remove?" + "</TD>"
				+ "</TR>"
				;

		//Display all the current trigger devices:
		for (int i = 0; i < entry.getEventScheduleDetails().size(); i++){
			if (bOddRow){
				sBackgroundColor = LIGHT_ROW_BG_COLOR;
			}else{
				sBackgroundColor = DARK_ROW_BG_COLOR;
			}

			String sEventDescription = 
				SMTablesseventscheduledetails.getDetailTypeLabel(
					Integer.parseInt(entry.getEventScheduleDetails().get(i).getsideviceoralarmsequence()));
			s += "<TR style = \" background-color: " + sBackgroundColor +  "; line-height: 30px; \">";
			s+= "<TD class = \" leftjustifiedcell \" >"
					+ "&nbsp;"
					+ sEventDescription
					+ "&nbsp;"
					+ "</TD>"
					;
			
			String sDeviceIDLink = sEventDescription + " ID:&nbsp;" + entry.getEventScheduleDetails().get(i).getsldeviceorsequenceid();
			String sDescription = "";
			String sName = "";
			if (entry.getEventScheduleDetails().get(i).getsideviceoralarmsequence().compareToIgnoreCase(Integer.toString(SMTablesseventscheduledetails.DEVICEORSEQUENCE_DEVICE)) == 0){
				if (bAllowDeviceEdit){
					sDeviceIDLink = "<A HREF=\"" 
						+ SMUtilities.getURLLinkBase(getServletContext()) 
						+ "smas.ASEditDevicesEdit?" 
						+ SMTablessdevices.lid + "=" + entry.getEventScheduleDetails().get(i).getsldeviceorsequenceid()
						+ "\">" + sDeviceIDLink + "</A>"
					;
				}
				SSDevice device = new SSDevice();
				device.setslid(entry.getEventScheduleDetails().get(i).getsldeviceorsequenceid());
				try {
					device.load(conn);
				} catch (Exception e) {
					throw new Exception("Error [1481922554] loading device ID '" + entry.getEventScheduleDetails().get(i).getslid()
						+ " - " + e.getMessage());
				}
				sDescription = device.getsdescription();
			}else{
				if (bAllowAlarmSequenceEdit){
					sDeviceIDLink = "<A HREF=\"" 
						+ SMUtilities.getURLLinkBase(getServletContext()) 
						+ "smas.ASEditAlarmSequencesEdit?" 
						+ SMTablessalarmsequences.lid + "=" + entry.getEventScheduleDetails().get(i).getsldeviceorsequenceid()
						+ "\">" + sDeviceIDLink + "</A>"
					;
				}
				SSAlarmSequence seq = new SSAlarmSequence();
				seq.setslid(entry.getEventScheduleDetails().get(i).getsldeviceorsequenceid());
				try {
					seq.load(conn);
				} catch (Exception e) {
					throw new Exception("Error [1481922555] loading alarm sequence ID '" + entry.getEventScheduleDetails().get(i).getslid()
						+ " - " + e.getMessage());
				}
				sDescription = seq.getsdescription();
				sName = seq.getsname();
			}

			s+= "<TD class = \" centerjustifiedcell \">"
					+ sDeviceIDLink
					+ "&nbsp;"
					+ "</TD>"
					;
			s+= "<TD class = \" leftjustifiedcell \" >"
					+ "&nbsp;"
					+ sName + " - " + sDescription
					+ "&nbsp;"
					+ "</TD>"
					;
			s+= "<TD class = \" leftjustifiedcell \">"
					+ "&nbsp;"
					+ entry.getEventScheduleDetails().get(i).getsiresetdelay()
					+ "&nbsp;"
					+ "</TD>"
					;
			s+= "<TD class = \" centerjustifiedcell \">"
					+ "&nbsp;"
					+ "<INPUT TYPE=SUBMIT NAME='" + SSEventSchedule.PARAM_REMOVE_EVENT_DETAIL_ID_PREFIX
					+ entry.getEventScheduleDetails().get(i).getslid()
					+ "' VALUE='" + "REMOVE" + "' STYLE='height: 0.24in'>"
					+ "&nbsp;"
					+ "</TD>"
					;

			s += "</TR>";

			bOddRow = !bOddRow;
		}
		s += "</TABLE>";
		try {
			s += displayEligibleDevicesAndAlarmSequences(conn, entry);
		} catch (Exception e) {
			s += "<BR><B><FONT COLOR=RED>" + e.getMessage() + "</FONT></B><BR>";
		}
		return s;
	}

	private String displayEligibleDevicesAndAlarmSequences(
			Connection conn, 
			SSEventSchedule entry) throws Exception{
		String s = "";

		s += "<BR><I><B><U>Add a device or alarm sequence to this schedule:</U></B></I><BR>";

		//If this is a new alarm sequence, not saved, then don't allow any devices to be added:
		if ((entry.getslid().compareToIgnoreCase("-1") == 0)){
			s+= "<TD class = \" leftjustifiedcell \" COLSPAN = 4>"
					+ "<B>(You cannot add devices/alarm sequences until you first save this event schedule)</B>"
					+ "</TD>"
					;
		}else{
			//Get a list of the available devices/alarm sequences:
			s+= "<SELECT NAME = '" + SSEventSchedule.PARAM_ELIGIBLE_DEVICEORALARMSEQUENCE_LIST + "'>"
					+ "<OPTION VALUE=\"" + "\"\"" + ">" + "** Select a device/alarm sequence **</OPTION>"
					;

			//First, list the alarm sequences:
			String SQL = "SELECT"
					+ " " + SMTablessalarmsequences.lid
					+ ", " + SMTablessalarmsequences.sname
					+ " FROM " + SMTablessalarmsequences.TableName
					+ " ORDER BY " + SMTablessalarmsequences.sname
					;
			try {
				ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
				while (rs.next()){
					//Don't add the sequence if it's already in the event details list:
					boolean bAlarmSequenceIsAlreadyListed = false;
					for(int i = 0; i < entry.getEventScheduleDetails().size(); i++){
						if (
							(entry.getEventScheduleDetails().get(i).getsldeviceorsequenceid().compareToIgnoreCase(Long.toString(rs.getLong(SMTablessalarmsequences.lid))) == 0)
							//AND if the event schedule detail is an alarm sequence:
							&& (entry.getEventScheduleDetails().get(i).getsideviceoralarmsequence().compareToIgnoreCase(Integer.toString(SMTablesseventscheduledetails.DEVICEORSEQUENCE_SEQUENCE)) == 0)
						){
							bAlarmSequenceIsAlreadyListed = true;
							break;
						}
					}
					if (!bAlarmSequenceIsAlreadyListed){
						//The value will have a '0' or a '1' prefix, to tell whether it's a device or an alarm sequence,
						//and then the ID of the device or alarm sequence:
						s += "<OPTION VALUE=\"" 
							+ Integer.toString(SMTablesseventscheduledetails.DEVICEORSEQUENCE_SEQUENCE) 
							+ Long.toString(rs.getLong(SMTablessalarmsequences.lid)) + "\">";
						s += "ALARM SEQUENCE: " + rs.getString(SMTablessalarmsequences.sname)
							+ " - ID: " + Long.toString(rs.getLong(SMTablessalarmsequences.lid))
						;
						s += "</OPTION>";
					}
				}
				rs.close();
			} catch (Exception e) {
				throw new Exception("Error [1482247449] listing eligible alarm sequences - " + e.getMessage());
			}
			
			SQL = "SELECT"
					+ " " + SMTablessdevices.lid
					+ ", " + SMTablessdevices.sdescription
					+ ", " + SMTablessdevices.iactive
					+ " FROM " + SMTablessdevices.TableName
					+ " WHERE ("
					+ "(" + SMTablessdevices.soutputterminalnumber + " != '')"
					+ ") ORDER BY " + SMTablessdevices.sdescription
					;
			try {
				ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
				while (rs.next()){
					//Don't add the device if it's already in the activation devices list:
					boolean bDeviceIsAlreadyListed = false;
					for(int i = 0; i < entry.getEventScheduleDetails().size(); i++){
						if (
							(entry.getEventScheduleDetails().get(i).getsldeviceorsequenceid().compareToIgnoreCase(Long.toString(rs.getLong(SMTablessdevices.lid))) == 0)
							//AND if the event schedule detail is a device:
							&& (entry.getEventScheduleDetails().get(i).getsideviceoralarmsequence().compareToIgnoreCase(Integer.toString(SMTablesseventscheduledetails.DEVICEORSEQUENCE_DEVICE)) == 0)
						){
							bDeviceIsAlreadyListed = true;
							break;
						}
					}
					if (!bDeviceIsAlreadyListed){
						//The value will have a '0' or a '1' prefix, to tell whether it's a device or an alarm sequence,
						//and then the ID of the device or alarm sequence:
						s += "<OPTION VALUE=\"" 
							+ Integer.toString(SMTablesseventscheduledetails.DEVICEORSEQUENCE_DEVICE) 
							+ Long.toString(rs.getLong(SMTablessdevices.lid)) + "\">";
						s += "DEVICE: " + rs.getString(SMTablessdevices.sdescription)
							+ " - ID: " + Long.toString(rs.getLong(SMTablessdevices.lid))
						;
						s += "</OPTION>";
					}
				}
				rs.close();
			} catch (Exception e) {
				throw new Exception("Error [1482183747] listing eligible devices - " + e.getMessage());
			}
			s += "</SELECT>";
			s += "&nbsp;<B>Reset delay<SUP><a href=\"#resetdelay\">2</a></SUP> (how long to re-activate after the last manual de-activation):</B>&nbsp;"
				+ "<INPUT TYPE=TEXT NAME=\"" + SSEventSchedule.PARAM_EVENT_DETAIL_RESET_DELAY_IN_MINUTES + "\""
				+ " VALUE=\"" + "0" + "\""
				+ " SIZE=" + "15"
				+ " MAXLENGTH=" + "8"
				+ "> <B>minutes</B>"
			;
		}
		return s;
	}

	private String sStyleScripts(){
		String s = "";
		String sRowHeight = "22px";
		s += "<style type=\"text/css\">\n";
		
		//Layout table:
		s +=
			"table.basic {"
			//+ "border-width: 0px; "
			//+ "border-spacing: 2px; "
			//+ "border-style: outset; "
			//+ "border-style: solid; "
			//+ "border-style: none; "
			//+ "border-color: black; "
			+ "border-collapse: collapse; "
			+ "width: 100%; "
			+ "font-size: " + "small" + "; "
			+ "font-family : Arial; "
			+ "color: black; "
			//+ "background-color: white; "
			+ "}"
			+ "\n"
			;
		
		s +=
			"table.basicwithborder {"
			+ "border-width: 1px; "
			+ "border-spacing: 2px; "
			+ "border-style: outset; "
			+ "border-style: solid; "
			//+ "border-style: none; "
			+ "border-color: black; "
			+ "border-collapse: separate; "
			+ "width: 100%; "
			+ "font-size: " + "medium" + "; "
			+ "font-family : Arial; "
			+ "color: black; "
			//+ "background-color: white; "
			+ "}"
			+ "\n"
			;
		/*
		//This is the def for a label field:
		s +=
			"td.fieldlabel {"
			+ "height: " + sRowHeight + "; "
			//+ "border-width: " + sBorderSize + "px; "
			//+ "padding: 2px; "
			//+ "border-style: none; "
			//+ "border-color: white; "
			//+ "vertical-align: text-middle;"
			//+ "background-color: black; "
			+ "font-weight: bold; "
			+ "text-align: right; "
			//+ "color: black; "
			//+ "height: 50px; "
			+ "}"
			+ "\n"
			;
		*/
		//This is the def for a table cell, left justified:
		s +=
			"td.leftjustifiedcell {"
			+ "height: " + sRowHeight + "; "
			//+ "border: 0px solid; "
			//+ "border-style: none; "
			+ "padding: 2px; "
			//+ "border-color: " + CELL_BORDER_COLOR + "; "
			+ "vertical-align: center;"
			+ "font-family : Arial; "
			//+ "font-weight: bold; "
			+ "font-size: medium; "
			+ "text-align: left; "
			+ "color: black; "
			//+ "height: 50px; "
			+ "}"
			+ "\n"
			;

	    //style= \" word-wrap:break-word; \"
	    //style= \" word-wrap:normal; white-space:pre-wrap; \" 
		s +=
			"td.leftjustifiedcellforcewrap {"
			+ "height: " + sRowHeight + "; "
			//+ "border: 0px solid; "
			//+ "border-style: none; "
			+ "padding: 2px; "
			//+ "border-color: " + CELL_BORDER_COLOR + "; "
			+ "vertical-align: center;"
			+ "font-family : Arial; "
			//+ "font-weight: bold; "
			+ "font-size: medium; "
			+ "text-align: left; "
			+ "color: black; "
			+ "word-wrap:break-word; "
			//+ "height: 50px; "
			+ "}"
			+ "\n"
			;
		//This is the def for a table cell, right justified:
		s +=
			"td.rightjustifiedcell {"
			+ "height: " + sRowHeight + "; "
			//+ "border: 0px solid; "
			//+ "border-style: none; "
			+ "padding: 2px; "
			//+ "border-color: " + CELL_BORDER_COLOR + "; "
			+ "vertical-align: center;"
			+ "font-family : Arial; "
			//+ "font-weight: bold; "
			+ "font-size: medium; "
			+ "text-align: right; "
			+ "color: black; "
			//+ "height: 50px; "
			+ "}"
			+ "\n"
			;
		
		//This is the def for a table cell, center justified:
		s +=
			"td.centerjustifiedcell {"
			+ "height: " + sRowHeight + "; "
			//+ "border: 0px solid; "
			//+ "border-style: none; "
			+ "padding: 2px; "
			//+ "border-color: " + CELL_BORDER_COLOR + "; "
			+ "vertical-align: center;"
			+ "font-family : Arial; "
			//+ "font-weight: bold; "
			+ "font-size: medium; "
			+ "text-align: center; "
			+ "color: black; "
			//+ "height: 50px; "
			+ "}"
			+ "\n"
			;
		
		//This is the def for a left-aligned heading on a table:
		s +=
			"td.leftjustifiedheading {"
			//+ "border: 0px solid; "
			+ "border-style: none; "
			//+ "bordercolor: 000; "
			+ "padding: 2px; "
			+ "font-family : Arial; "
			+ "font-size: medium; "
			+ "font-weight: bold; "
			+ "text-align: left; "
			+ "vertical-align:bottom; "
			+ "}"
			+ "\n"
			;
		
		//This is the def for a right-aligned heading on a table:
		s +=
			"td.rightjustifiedheading {"
			//+ "border: 0px solid; "
			+ "border-style: none; "
			//+ "bordercolor: 000; "
			+ "padding: 2px; "
			+ "font-family : Arial; "
			+ "font-size: medium; "
			+ "font-weight: bold; "
			+ "text-align: right; "
			+ "vertical-align:bottom; "
			+ "}"
			+ "\n"
			;

		//This is the def for a center-aligned heading on a table:
		s +=
			"td.centerjustifiedheading {"
			//+ "border: 0px solid; "
			+ "border-style: none; "
			//+ "bordercolor: 000; "
			+ "padding: 2px; "
			+ "font-family : Arial; "
			+ "font-size: medium; "
			+ "font-weight: bold; "
			+ "text-align: center; "
			+ "vertical-align:bottom; "
			+ "}"
			+ "\n"
			;
		
		s += "</style>"
			+ "\n"
			;

		return s;
	}

	public void doGet(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
		
		doPost(request, response);
	}
}
