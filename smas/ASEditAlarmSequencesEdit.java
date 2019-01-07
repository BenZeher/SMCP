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

import smcontrolpanel.SMMasterEditEntry;
import smcontrolpanel.SMSystemFunctions;
import smcontrolpanel.SMUtilities;
import SMDataDefinition.SMTablessalarmsequences;
import SMDataDefinition.SMTablessdevices;
import ServletUtilities.clsCreateHTMLTableFormFields;
import ServletUtilities.clsDatabaseFunctions;

public class ASEditAlarmSequencesEdit  extends HttpServlet {

	private static final long serialVersionUID = 1L;
	private static final String OBJECT_NAME = SSAlarmSequence.ParamObjectName;
	private static final String DARK_ROW_BG_COLOR = "#cceeff"; //Light blue
	private static final String LIGHT_ROW_BG_COLOR = "#FFFFFF";
	
	private static final String ALARM_SET_DELAY_0_SECONDS = "0";
	private static final String ALARM_SET_DELAY_30_SECONDS = "30";
	private static final String ALARM_SET_DELAY_60_SECONDS = "60";
	private static final String ALARM_SET_DELAY_90_SECONDS = "90";
	private static final String ALARM_SET_DELAY_120_SECONDS = "120";
	
	public void doPost(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
		
		SSAlarmSequence entry = new SSAlarmSequence(request);
		SMMasterEditEntry smedit = new SMMasterEditEntry(
				request,
				response,
				getServletContext(),
				entry.getObjectName(),
				SMUtilities.getFullClassName(this.toString()),
				"smas.ASEditAlarmSequencesAction",
				"smcontrolpanel.SMUserLogin",
				"Go back to user login",
				SMSystemFunctions.ASEditAlarmSequences
				);
		
		if (!smedit.processSession(getServletContext(), SMSystemFunctions.ASEditAlarmSequences)){
			smedit.getPWOut().println("Error in process session: " + smedit.getErrorMessages());
			return;
		}
		
		//If this is a 'resubmit', meaning it's being called by the 'Action' class, then
		//the session will have a job cost entry object in it, and that's what we'll pick up.
		HttpSession currentSession = smedit.getCurrentSession();
		
	    if (currentSession.getAttribute(OBJECT_NAME) != null){
	    	entry = (SSAlarmSequence) currentSession.getAttribute(OBJECT_NAME);
	    	currentSession.removeAttribute(OBJECT_NAME);
	    //But if it's NOT a 'resubmit', meaning this class was called for the first time to 
	    //edit, we'll pick up the ID or key from the request and try to load the entry:
	    }else{
	    	if (!smedit.getAddingNewEntryFlag()){
	    		try {
					entry.load(getServletContext(), 
							smedit.getConfFile(), 
							smedit.getUserID(),
							smedit.getFullUserName()
							);
				} catch (Exception e) {
					response.sendRedirect(
							"" + SMUtilities.getURLLinkBase(getServletContext()) + "smas.ASEditAlarmSequencesSelect"
							+ "?" + SMTablessalarmsequences.lid + "=" + entry.getslid()
							+ "&Warning=" + e.getMessage()
							+ "&" + SMUtilities.REQUEST_PARAM_SESSIONTAG + "=" + smedit.getSessionTag()
						);
						return;
				}
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
				"<BR><A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smas.ASMainMenu?" + SMUtilities.REQUEST_PARAM_SESSIONTAG + "=" 
					+ smedit.getSessionTag() + "\">Return to Alarm Systems Main Menu</A><BR>");
	    
		smedit.getPWOut().println("<BR>");
		
	    try {
			smedit.createEditPage(getEditHTML(smedit, entry), "");
		} catch (Exception e) {
    		String sError = "Could not create edit page - " + e.getMessage();
			response.sendRedirect(
				"" + SMUtilities.getURLLinkBase(getServletContext()) + "" + "smas.ASEditAlarmSequencesSelect"
				+ "?" + SMTablessalarmsequences.lid + "=" + entry.getslid()
				+ "&Warning=Could not load " + OBJECT_NAME + " with ID: " + entry.getslid() + " - " + sError
				+ "&" + SMUtilities.REQUEST_PARAM_SESSIONTAG + "=" + smedit.getSessionTag()
			);
			return;
		}
	    return;
	}
	private String getEditHTML(SMMasterEditEntry sm, SSAlarmSequence entry) throws Exception{

		String s = "";
		s += "\n" + sStyleScripts() + "\n";
		
		if (
			(entry.getsalarmstate().compareToIgnoreCase(Integer.toString(SMTablessalarmsequences.ALARM_STATE_ARMED)) == 0)
			|| (entry.getsalarmstate().compareToIgnoreCase(Integer.toString(SMTablessalarmsequences.ALARM_STATE_TRIGGERED)) == 0)
		){
			s += "<B><FONT COLOR=RED>Alarm sequence '" + entry.getsname() + "' is currently armed and cannot be edited until it is disarmed.</FONT></B>";
			return s;
		}
		
		s += "<TABLE class = \" basicwithborder \" >";
		String sID = "NEW";
		if ((entry.getslid().compareToIgnoreCase("-1") != 0)){
			sID = entry.getslid();
		}
		s += "<TR><TD ALIGN=RIGHT><B>Alarm sequence ID</B>:</TD><TD><B>" 
			+ sID 
			+ "<INPUT TYPE=HIDDEN NAME=\"" + SMTablessalarmsequences.lid + "\" VALUE=\"" 
			+ entry.getslid() + "\">"
			+ "</B></TD><TD>&nbsp;</TD></TR>";
		
		Connection conn;
		try {
			conn = clsDatabaseFunctions.getConnectionWithException(
				getServletContext(), 
				sm.getConfFile(), 
				"MySQL", 
				this.toString() + ".getEditHTML - user: " + sm.getUserName()
			);
		} catch (Exception e) {
			throw new Exception("Error [1458920376] getting connection - " + e.getMessage());
		}
		
		//If we are trying to add a new entry:
		if (entry.getslid().compareToIgnoreCase("-1") == 0){
			
			s += clsCreateHTMLTableFormFields.Create_Edit_Form_Text_Input_Row(
					SMTablessalarmsequences.sname,
					entry.getsname().replace("\"", "&quot;"), 
					SMTablessalarmsequences.snamelength, 
					"<B>Sequence name: <FONT COLOR=RED>*Required*</FONT></B>",
					"Give the alarm sequence a short, identifying name, like 'Main Office', or 'Warehouse', etc..",
					"30"
			);
		}else{

			s += "<TR><TD ALIGN=RIGHT><B>Sequence name: <FONT COLOR=RED>*Required*</FONT></B>:</TD>"
				+ "<TD>" 
				+ "<B>" + entry.getsname() + "</B>" 
				+ "<INPUT TYPE=HIDDEN NAME=\"" + SMTablessalarmsequences.sname + "\" VALUE=\"" 
					+ entry.getsname() + "\">"
				+ "</TD>"
				+ "<TD>&nbsp;</TD>"
				+ "</TR>"
				;
		}

		//Display the current alarm state:
		s += "<TR><TD ALIGN=RIGHT><B>Current alarm state:</TD>"
			+ "<TD>" 
			+ "<B>" + SMTablessalarmsequences.getAlarmStateLabel(Integer.parseInt(entry.getsalarmstate())) + "</B>" 
			+ "<INPUT TYPE=HIDDEN NAME=\"" + SMTablessalarmsequences.ialarmstate + "\" VALUE=\"" 
				+ entry.getsalarmstate() + "\">"
			+ "</TD>"
			+ "<TD>&nbsp;</TD>"
			+ "</TR>"
		;
		
		//Display the last date armed:
		//Carry the last alarmed date/time, but hidden:
		s += "<TR><TD ALIGN=RIGHT><B>Last armed:</TD>"
			+ "<TD>" 
			+ "<B>" + entry.getsdattimelastarmed() + "</B>" 
			+ "<INPUT TYPE=HIDDEN NAME=\"" + entry.getsdattimelastarmed() + "\" VALUE=\"" 
				+ entry.getsdattimelastarmed() + "\">"
			+ "</TD>"
			+ "<TD>&nbsp;</TD>"
			+ "</TR>"
		;
		
		//Display the last date disarmed:
		//Carry the last disalarmed date/time, but hidden:
		s += "<TR><TD ALIGN=RIGHT><B>Last disarmed:</TD>"
			+ "<TD>" 
			+ "<B>" + entry.getsdattimelastdisarmed() + "</B>" 
			+ "<INPUT TYPE=HIDDEN NAME=\"" + entry.getsdattimelastdisarmed() + "\" VALUE=\"" 
				+ entry.getsdattimelastdisarmed() + "\">"
			+ "</TD>"
			+ "<TD>&nbsp;</TD>"
			+ "</TR>"
		;

		//Description
		s += clsCreateHTMLTableFormFields.Create_Edit_Form_Text_Input_Row(
			SMTablessalarmsequences.sdescription,
			entry.getsdescription().replace("\"", "&quot;"), 
			SMTablessalarmsequences.sdescriptionlength, 
			"<B>Description: <FONT COLOR=RED>*Required*</FONT></B>",
			"Enter a description that clearly identifies the alarm zone, like 'Fresno office, warehouse and yard'.",
			"75"
		);

		//Alarm set delay interval:
		ArrayList<String>arrAlarmDelayValues = new ArrayList<String>(0);
		ArrayList<String>arrAlarmDelayLabels = new ArrayList<String>(0);
		arrAlarmDelayValues.add(ALARM_SET_DELAY_0_SECONDS);
		arrAlarmDelayLabels.add("None (arm without delay)");
		arrAlarmDelayValues.add(ALARM_SET_DELAY_30_SECONDS);
		arrAlarmDelayLabels.add(ALARM_SET_DELAY_30_SECONDS + "&nbsp;seconds");
		arrAlarmDelayValues.add(ALARM_SET_DELAY_60_SECONDS);
		arrAlarmDelayLabels.add(ALARM_SET_DELAY_60_SECONDS + "&nbsp;seconds");
		arrAlarmDelayValues.add(ALARM_SET_DELAY_90_SECONDS);
		arrAlarmDelayLabels.add(ALARM_SET_DELAY_90_SECONDS + "&nbsp;seconds");
		arrAlarmDelayValues.add(ALARM_SET_DELAY_120_SECONDS);
		arrAlarmDelayLabels.add(ALARM_SET_DELAY_120_SECONDS + "&nbsp;seconds");

		s += clsCreateHTMLTableFormFields.Create_Edit_Form_List_Row(
			SMTablessalarmsequences.lalarmsetdelaycountdown, 
			arrAlarmDelayValues, 
			entry.getsalarmsetcountdown(), 
			arrAlarmDelayLabels, 
			"Alarm delay countdown<BR><FONT COLOR=RED>*Required*</FONT>:", 
			"Set how long you want the alarm delayed after arming to allow people to get out of the building, for example."
		);
		
		//Notification emails
		s += clsCreateHTMLTableFormFields.Create_Edit_Form_MultilineText_Input_Row(
			SMTablessalarmsequences.semailnotifications, 
			entry.getsnotificationemails().replace("\"", "&quot;"), 
			"Notification emails<BR><FONT COLOR=RED></FONT>:", 
			"Enter here a list of email addresses for people you want notified when this alarm is triggered.  Separate the addresses with commas.", 
			3, 
			75
		);
		
		s += "</TABLE>";
		
		boolean bAllowDeviceEdit = SMSystemFunctions.isFunctionPermitted(
				SMSystemFunctions.ASEditDevices, 
				sm.getUserID(), 
				conn,
				(String) sm.getCurrentSession().getAttribute(SMUtilities.SMCP_SESSION_PARAM_LICENSE_MODULE_LEVEL));
		
		//Next display the trigger devices:
		s += displayTriggerDevices(conn, entry, bAllowDeviceEdit);
		
		//Next display the activation devices:
		s += displayActivationDevices(conn, entry, bAllowDeviceEdit);
		
		clsDatabaseFunctions.freeConnection(getServletContext(), conn);
		
		return s;
	}

	private String displayTriggerDevices(Connection conn, SSAlarmSequence entry, boolean bAllowDeviceEdit) throws Exception{
		String s = "";
		String sBackgroundColor = "";
		boolean bOddRow = true;

		s += "<BR><I><B><U>Configure trigger devices (e.g. motion sensors) for the alarm sequence here:</U></B></I>";

		s += "<TABLE class = \" basicwithborder \" >";

		//Display the headings:
		s += "<TR style = \" background-color: black; color: white; \" >";

		s += "<TD class = \" centerjustifiedheading \" >" + "Device ID<BR>Device Status" + "</TD>"
				+ "<TD class = \" leftjustifiedheading \" >" + "Description" + "</TD>"
				+ "<TD class = \" leftjustifiedheading \" >" + "Device type" + "</TD>"
				+ "<TD class = \" centerjustifiedheading \" >" + "Remove?" + "</TD>"
				//+ "<TD class = \" leftjustifiedheading \" >" + "Activation zone" + "</TD>"
				+ "</TR>"
				;

		//Display all the current trigger devices:
		for (int i = 0; i < entry.getTriggerDeviceList().size(); i++){
			if (bOddRow){
				sBackgroundColor = LIGHT_ROW_BG_COLOR;
			}else{
				sBackgroundColor = DARK_ROW_BG_COLOR;
			}

			s += "<TR style = \" background-color: " + sBackgroundColor +  "; line-height: 30px; \">";
			String sDeviceIDLink = "Device ID:&nbsp;" + entry.getTriggerDeviceList().get(i).getslid();
			
			if (entry.getTriggerDeviceList().get(i).getsactive().compareToIgnoreCase("0") == 0){
				sDeviceIDLink += "&nbsp;(INACTIVE)";
			}else{
				sDeviceIDLink += "&nbsp;(ACTIVE)";
			}
			if (bAllowDeviceEdit){
				sDeviceIDLink = "<A HREF=\"" 
					+ SMUtilities.getURLLinkBase(getServletContext()) 
					+ "smas.ASEditDevicesEdit?" 
					+ SMTablessdevices.lid + "=" + entry.getTriggerDeviceList().get(i).getslid()
					+ "\">" + sDeviceIDLink + "</A>"
				;
			}
			s+= "<TD class = \" centerjustifiedcell \">"
					+ sDeviceIDLink
					+ "&nbsp;"
					+ "</TD>"
					;
			s+= "<TD class = \" leftjustifiedcell \" >"
					+ "&nbsp;"
					+ entry.getTriggerDeviceList().get(i).getsdescription()
					+ "&nbsp;"
					+ "</TD>"
					;
			s+= "<TD class = \" leftjustifiedcell \">"
					+ "&nbsp;"
					+ SMTablessdevices.getDeviceTypeLabel(Integer.parseInt(entry.getTriggerDeviceList().get(i).getsdeviccetype()))
					+ "&nbsp;"
					+ "</TD>"
					;
			s+= "<TD class = \" centerjustifiedcell \">"
					+ "&nbsp;"
					+ "<INPUT TYPE=SUBMIT NAME='" + SSAlarmSequence.PARAM_REMOVE_TRIGGER_DEVICE_ID_PREFIX
					+ entry.getTriggerDeviceList().get(i).getslid()
					+ "' VALUE='" + "REMOVE" + "' STYLE='height: 0.24in'>"
					+ "&nbsp;"
					+ "</TD>"
					;

			s += "</TR>";

			bOddRow = !bOddRow;
		}

		//Now add a row for adding additional trigger devices:
		if (bOddRow){
			sBackgroundColor = LIGHT_ROW_BG_COLOR;
		}else{
			sBackgroundColor = DARK_ROW_BG_COLOR;
		}
		s += "<TR style = \" background-color: " + sBackgroundColor +  "; line-height: 30px; \">";

		//If this is a new alarm sequence, not saved, then don't allow any devices to be added:
		if ((entry.getslid().compareToIgnoreCase("-1") == 0)){
			s+= "<TD class = \" leftjustifiedcell \" COLSPAN = 4>"
					+ "<B>(You cannot add trigger devices until you first save this alarm sequence)</B>"
					+ "</TD>"
					;

		}else{

			//Get a list of the available 'trigger' devices:
			s+= "<TD class = \" leftjustifiedcell \" COLSPAN = 4>"
					+ "<B>Select a trigger device to add:&nbsp;</B>"
					+ "<SELECT NAME = '" + SSAlarmSequence.PARAM_NEW_TRIGGER_DEVICE_LIST + "'>"
					+ "<OPTION VALUE=\"" + "\"\"" + ">" + "** Select a TRIGGERING Device **</OPTION>"
					;

			String SQL = "SELECT"
					+ " " + SMTablessdevices.lid
					+ ", " + SMTablessdevices.sdescription
					+ ", " + SMTablessdevices.iactive
					+ " FROM " + SMTablessdevices.TableName
					+ " WHERE ("
					+ "(" + SMTablessdevices.sinputterminalnumber + " != '')"
					+ ") ORDER BY " + SMTablessdevices.lid
					;
			try {
				ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
				while (rs.next()){
					//Don't add the device if it's already a trigger:
					boolean bDeviceIsAlreadyListed = false;
					for(int i = 0; i < entry.getTriggerDeviceList().size(); i++){
						if (entry.getTriggerDeviceList().get(i).getslid().compareToIgnoreCase(Long.toString(rs.getLong(SMTablessdevices.lid))) == 0){
							bDeviceIsAlreadyListed = true;
							break;
						}
					}
					String sInactive = "";
					if (rs.getInt(SMTablessdevices.iactive) == 0){
						sInactive = " (INACTIVE)";
					}
					if (!bDeviceIsAlreadyListed){
						s += "<OPTION VALUE=\"" + Long.toString(rs.getLong(SMTablessdevices.lid)) + "\">";
						s += rs.getString(SMTablessdevices.sdescription) + " - " 
							+ Long.toString(rs.getLong(SMTablessdevices.lid))
							+ sInactive
						;
						s += "</OPTION>";
					}
				}
				rs.close();
			} catch (Exception e) {
				throw new Exception("Error [1460492744] listing trigger devices - " + e.getMessage());
			}
			s += "</SELECT>"
					+ "</TD>"
					;
		}
		s += "</TR>";

		s += "</TABLE>";
		return s;
	}

	private String displayActivationDevices(Connection conn, SSAlarmSequence entry, boolean bAllowDeviceEdit) throws Exception{
		String s = "";
		String sBackgroundColor = "";
		boolean bOddRow = true;

		s += "<BR><I><B><U>Configure the device(s) (e.g. alarm bells, etc.) you want activated when the alarm is triggered:</U></B></I>";

		s += "<TABLE class = \" basicwithborder \" >";

		//Display the headings:
		s += "<TR style = \" background-color: black; color: white; \" >";

		s += "<TD class = \" centerjustifiedheading \" >" + "Device ID<BR>Device status" + "</TD>"
				+ "<TD class = \" leftjustifiedheading \" >" + "Description" + "</TD>"
				+ "<TD class = \" leftjustifiedheading \" >" + "Device type" + "</TD>"
				+ "<TD class = \" rightjustifiedheading \" >" + "Duration (seconds)" + "</TD>"
				+ "<TD class = \" centerjustifiedheading \" >" + "Remove?" + "</TD>"
				//+ "<TD class = \" leftjustifiedheading \" >" + "Activation zone" + "</TD>"
				+ "</TR>"
				;

		//Display all the current activation devices:
		for (int i = 0; i < entry.getActivationDeviceList().size(); i++){
			if (bOddRow){
				sBackgroundColor = LIGHT_ROW_BG_COLOR;
			}else{
				sBackgroundColor = DARK_ROW_BG_COLOR;
			}

			s += "<TR style = \" background-color: " + sBackgroundColor +  "; line-height: 30px; \">";
			String sDeviceIDLink = "Device ID:&nbsp;" + entry.getActivationDeviceList().get(i).getslid();
			if (entry.getActivationDeviceList().get(i).getsactive().compareToIgnoreCase("0") == 0){
				sDeviceIDLink += "&nbsp;(INACTIVE)";
			}else{
				sDeviceIDLink += "&nbsp;(ACTIVE)";
			}
			if (bAllowDeviceEdit){
				sDeviceIDLink = "<A HREF=\"" 
					+ SMUtilities.getURLLinkBase(getServletContext()) 
					+ "smas.ASEditDevicesEdit?" 
					+ SMTablessdevices.lid + "=" + entry.getActivationDeviceList().get(i).getslid()
					+ "\">" + sDeviceIDLink + "</A>"
				;
			}
			s+= "<TD class = \" centerjustifiedcell \">"
					+ sDeviceIDLink
					+ "&nbsp;"
					+ "</TD>"
					;
			s+= "<TD class = \" leftjustifiedcell \" >"
					+ "&nbsp;"
					+ entry.getActivationDeviceList().get(i).getsdescription()
					+ "&nbsp;"
					+ "</TD>"
					;
			s+= "<TD class = \" leftjustifiedcell \">"
					+ "&nbsp;"
					+ SMTablessdevices.getDeviceTypeLabel(Integer.parseInt(entry.getActivationDeviceList().get(i).getsdeviccetype()))
					+ "&nbsp;"
					+ "</TD>"
					;
			//Convert the activation duration ms into seconds for the user to see:
			long lActivationDurationInSeconds;
			try {
				lActivationDurationInSeconds = Long.parseLong(entry.getActivationDeviceDurationInMS().get(i)) / 1000L;
			} catch (Exception e) {
				throw new Exception("Error [1463585156] converting activation duration to seconds - " + e.getMessage());
			}
			s+= "<TD class = \" rightjustifiedcell \">"
					+ "&nbsp;"
					+ Long.toString(lActivationDurationInSeconds)
					+ "&nbsp;"
					+ "</TD>"
					;
			s+= "<TD class = \" centerjustifiedcell \">"
					+ "&nbsp;"
					+ "<INPUT TYPE=SUBMIT NAME='" + SSAlarmSequence.PARAM_REMOVE_ACTIVATION_DEVICE_ID_PREFIX
					+ entry.getActivationDeviceList().get(i).getslid()
					+ "' VALUE='" + "REMOVE" + "' STYLE='height: 0.24in'>"
					+ "&nbsp;"
					+ "</TD>"
					;

			s += "</TR>";

			bOddRow = !bOddRow;
		}

		//Now add a row for adding additional trigger devices:
		if (bOddRow){
			sBackgroundColor = LIGHT_ROW_BG_COLOR;
		}else{
			sBackgroundColor = DARK_ROW_BG_COLOR;
		}
		s += "<TR style = \" background-color: " + sBackgroundColor +  "; line-height: 30px; \">";

		//If this is a new alarm sequence, not saved, then don't allow any devices to be added:
		if ((entry.getslid().compareToIgnoreCase("-1") == 0)){
			s+= "<TD class = \" leftjustifiedcell \" COLSPAN = 4>"
					+ "<B>(You cannot add activation devices until you first save this alarm sequence)</B>"
					+ "</TD>"
					;

		}else{

			//Get a list of the available 'activation' devices:
			s+= "<TD class = \" leftjustifiedcell \" COLSPAN = 5>"
					+ "<B>Select an activation device to add:&nbsp;</B>"
					+ "<SELECT NAME = '" + SSAlarmSequence.PARAM_NEW_ACTIVATION_DEVICE_LIST + "'>"
					+ "<OPTION VALUE=\"" + "\"\"" + ">" + "** Select an ACTIVATION Device (alarm) **</OPTION>"
					;

			String SQL = "SELECT"
					+ " " + SMTablessdevices.lid
					+ ", " + SMTablessdevices.sdescription
					+ ", " + SMTablessdevices.iactive
					+ " FROM " + SMTablessdevices.TableName
					+ " WHERE ("
					+ "(" + SMTablessdevices.soutputterminalnumber + " != '')"
					+ ") ORDER BY " + SMTablessdevices.lid
					;
			try {
				ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
				while (rs.next()){
					//Don't add the device if it's already in the activation devices list:
					boolean bDeviceIsAlreadyListed = false;
					for(int i = 0; i < entry.getActivationDeviceList().size(); i++){
						if (entry.getActivationDeviceList().get(i).getslid().compareToIgnoreCase(Long.toString(rs.getLong(SMTablessdevices.lid))) == 0){
							bDeviceIsAlreadyListed = true;
							break;
						}
					}
					String sInactive = "";
					if (rs.getInt(SMTablessdevices.iactive) == 0){
						sInactive = " (INACTIVE)";
					}
					if (!bDeviceIsAlreadyListed){
						s += "<OPTION VALUE=\"" + Long.toString(rs.getLong(SMTablessdevices.lid)) + "\">";
						s += rs.getString(SMTablessdevices.sdescription)
							+ " - " + Long.toString(rs.getLong(SMTablessdevices.lid)) 
							+ sInactive;
						s += "</OPTION>";
					}
				}
				rs.close();
			} catch (Exception e) {
				throw new Exception("Error [1462305274] listing activation devices - " + e.getMessage());
			}
			s += "</SELECT>";
			s += "&nbsp;<B>Duration (enter '0' (zero) for continuous duration):</B>&nbsp;"
				+ "<INPUT TYPE=TEXT NAME=\"" + SSAlarmSequence.PARAM_ACTIVATION_DURATION_IN_SECONDS + "\""
				+ " VALUE=\"" + "0" + "\""
				+ " SIZE=" + "15"
				+ " MAXLENGTH=" + "8"
				+ "> <B>seconds</B>"
			;
			s += "</TD>";
		}
		s += "</TR>";

		s += "</TABLE>";
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
