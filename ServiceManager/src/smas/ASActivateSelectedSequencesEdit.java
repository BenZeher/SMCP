package smas;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import smcontrolpanel.SMMasterEditEntry;
import smcontrolpanel.SMSystemFunctions;
import smcontrolpanel.SMUtilities;
import SMDataDefinition.SMGoogleMapAPIKey;
import SMDataDefinition.SMMasterStyleSheetDefinitions;
import SMDataDefinition.SMTablessalarmsequences;
import SMDataDefinition.SMTablessalarmsequenceusers;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsDateAndTimeConversions;
import ServletUtilities.clsManageRequestParameters;
import ServletUtilities.clsServletUtilities;

public class ASActivateSelectedSequencesEdit  extends HttpServlet {

	private static final long serialVersionUID = 1L;
	private static final String ARM_ZONE_BUTTON_LABEL = "ARM";
	private static final String DISARM_ZONE_BUTTON_LABEL = "DISARM";
	private static final String ARM_DISARM_BUTTON_NAME = "ARMDISARMBUTTON";
	public static final String ALARMSEQUENCE_CHECKBOX_NAME_PREFIX = "CHKALARMSEQUENCE";
	public static final String PARAMETER_USER_LATITUDE = "USERLATITUDE";
	public static final String PARAMETER_USER_LONGITUDE = "USERLONGITUDE";
	public static final String PARAMETER_ARM_STATE_TO_SET = "ARMSTATETOSET";  //is the user 'ARMING' or 'DISARMING'?
	public static final String PARAMETER_LOCATION_RECORDING_ALLOWED = "LOCATIONRECORDINGALLOWED";
	public static final String PARAMETER_LOCATION_RECORDING_VALUE_NOT_ALLOWED = "NOTALLOWED";
	public static final String PARAMETER_LOCATION_RECORDING_VALUE_ALLOWED = "ALLOWED";
	public static final String PARAMETER_OVERRIDE_TRIGGER_DEVICE_MALFUNCTION = "OVERRIDETRIGGERDEVICEMALFUNCTION";
	//This parameter gets set if the user tried to set an alarm, but one of the trigger devices was not in its 'normal' state.
	//This would be set by the 'ASActivateAlarmsAction' class and returned back to the current class:
	public static final String PARAMETER_TRIGGER_DEVICE_MALFUNCTION = "TRIGGERDEVICEMALFUNCTION";
	
	public static final String BUTTON_UPDATE_SEQUENCES_NAME = "UPDATE_SEQUENCES";
	public static final String BUTTON_UPDATE_SEQUENCES_LABEL = "Update selected alarm sequences";
	
	public void doPost(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
		
		SMMasterEditEntry smedit = new SMMasterEditEntry(
				request,
				response,
				getServletContext(),
				"Alarm Activations",
				SMUtilities.getFullClassName(this.toString()),
				"smas.ASActivateSelectedSequencesAction",
				"smcontrolpanel.SMUserLogin",
				"Go back to user login",
				SMSystemFunctions.ASActivateSelectedSequences
				);
		
		if (!smedit.processSession(getServletContext(), SMSystemFunctions.ASActivateSelectedSequences)){
			smedit.getPWOut().println("Error [1487945380] in process session: " + smedit.getErrorMessages());
			return;
		}
		
		smedit.setbIncludeDeleteButton(false);
		smedit.setbIncludeUpdateButton(false);
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
			smedit.createEditPage(getEditHTML(smedit), "");
		} catch (Exception e) {
    		String sError = "Could not create edit page - " + e.getMessage();
    		smedit.getPWOut().println("<HTML><BR><FONT COLOR=RED><B>WARNING - " + sError + "</B><FONT COLOR = RED><BR></HTML>");
			return;
		}
	    return;
	}
	
	private String getEditHTML(SMMasterEditEntry sm) throws Exception{
		String s = "";
	
		//Get the SSOptions:
		SSOptions ssopt = new SSOptions();
		ssopt.load(getServletContext(), sm.getsDBID(), sm.getUserName());
		
		s += "\n" + sStyleScripts() + "\n";
		s += "\n" + sCommandScripts(ssopt.getstrackuserlocation().compareToIgnoreCase("1") == 0) + "\n";

		//Values for geocodes:
		s += "\n<INPUT TYPE=HIDDEN NAME=\"" + PARAMETER_USER_LATITUDE + "\" VALUE=\"" + "" + "\""
			+ " id=\"" + PARAMETER_USER_LATITUDE + "\""
			+ "\">\n";

		s += "\n<INPUT TYPE=HIDDEN NAME=\"" + PARAMETER_USER_LONGITUDE + "\" VALUE=\"" + "" + "\""
				+ " id=\"" + PARAMETER_USER_LONGITUDE + "\""
				+ "\">\n";

		s += "\n<INPUT TYPE=HIDDEN NAME=\"" + PARAMETER_LOCATION_RECORDING_ALLOWED + "\" VALUE=\"" + "" + "\""
				+ " id=\"" + PARAMETER_LOCATION_RECORDING_ALLOWED + "\""
				+ "\">\n";
		
		Connection conn;
		try {
			conn = clsDatabaseFunctions.getConnectionWithException(
				getServletContext(), 
				sm.getsDBID(), 
				"MySQL", 
				this.toString() + ".getEditHTML - user: " + sm.getUserName()
			);
		} catch (Exception e) {
			throw new Exception("Error [1487945381] getting connection - " + e.getMessage());
		}
		
		//IF the user tried to set the alarm, but a trigger device was malfunctioning, give them the chance to override it here:
		if (clsManageRequestParameters.get_Request_Parameter(PARAMETER_TRIGGER_DEVICE_MALFUNCTION, sm.getRequest()).compareToIgnoreCase("") != 0){
			//Display a checkbox to override the malfunction:
			s += "<FONT COLOR=RED><B><I>NOTE: TO OVERRIDE THE MALFUNCTIONING DEVICES AND SET THE ALARM ANYWAY, FIRST CHECK THE BOX DIRECTLY BELOW:</I></B></FONT><BR>";
			s += "<INPUT TYPE=CHECKBOX "
				+ " NAME=\"" + PARAMETER_OVERRIDE_TRIGGER_DEVICE_MALFUNCTION + "\""
				+ " id = \"" + PARAMETER_OVERRIDE_TRIGGER_DEVICE_MALFUNCTION + "\""
				+ ">"
				+ "<B>Check to override malfunctioning devices</B>"
				+ "<BR>"
			;
		}
		
		s += "<TABLE class = \" basicwithborder \" >\n";
		
		//Display the header row:
		s += createHeaderRow();
		
		s += createDeviceRows(sm);
		
		s += "</TABLE>\n";
		
		s += createToggleButton();
		
		clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1547067591]");
		return s;
	}
	private String createHeaderRow(){
		String s = "";

		s += "  <TR style = \" background-color: black; color: white; \" >\n";
		
		s += "    <TD class = \" centerjustifiedheading \" >" + "Name" + "</TD>\n"
			+ "    <TD class = \" leftjustifiedheading \" >" + "Arm/Disarm" + "</TD>\n"
			+ "    <TD class = \" centerjustifiedheading \" >" + "Current status" + "</TD>\n"
			+ "    <TD class = \" leftjustifiedheading \" >" + "Alarm sequence description" + "</TD>\n"
			
			//+ "<TD class = \" centerjustifiedheading \" >" + "Confirm?" + "</TD>"
			+ "  </TR>"
		;
		return s;
	}
	private String createToggleButton(){
		return "<BR>"
			+ "<button type=\"button\""
			+ " style = \"" 
			+ "background-color: " + "BLACK" + ";"
			+ "border: solid;"
			+ "color: white;"
			+ "padding: 6px 6px;"
			+ "text-align: center;"
			+ "text-decoration: none;"
			+ "display: inline-block;"
			+ "font-size: 16px; "
			+ "box-shadow: 0 12px 16px 0 rgba(0,0,0,0.24),0 17px 50px 0 rgba(0,0,0,0.19);"
			+ "\""
			+ " value=\"" + BUTTON_UPDATE_SEQUENCES_LABEL + "\""
			+ " name=\"" + BUTTON_UPDATE_SEQUENCES_NAME + "\""
			+ " onClick=\"updateAlarmSequences();\""
			+ ">"
			+  BUTTON_UPDATE_SEQUENCES_LABEL 
			+ "</button>\n"
		;
	}
	private String createDeviceRows(SMMasterEditEntry sm) throws Exception{
		String s = "";
		boolean bOddRow = true;
		boolean bAllowAlarmSequenceEdit;
		try {
			bAllowAlarmSequenceEdit = SMSystemFunctions.isFunctionPermitted(
				SMSystemFunctions.ASEditAlarmSequences, 
				sm.getUserID(), 
				getServletContext(), 
				sm.getsDBID(),
				(String) sm.getCurrentSession().getAttribute(SMUtilities.SMCP_SESSION_PARAM_LICENSE_MODULE_LEVEL));
		} catch (Exception e) {
			throw new Exception("Error [1487945382] checking alarm sequence editing permissions - " + e.getMessage());
		}
		String SQL = "SELECT"
			+ " " + SMTablessalarmsequences.TableName + "." + SMTablessalarmsequences.lid
			+ ", " + SMTablessalarmsequences.TableName + "." + SMTablessalarmsequences.sdescription
			+ ", " + SMTablessalarmsequences.TableName + "." + SMTablessalarmsequences.ialarmstate
			+ ", " + SMTablessalarmsequences.TableName + "." + SMTablessalarmsequences.lalarmsetdelaycountdown
			+ ", " + SMTablessalarmsequences.TableName + "." + SMTablessalarmsequences.datlastarmed
			+ ", " + SMTablessalarmsequences.TableName + "." + SMTablessalarmsequences.datlastdisarmed
			+ ", " + SMTablessalarmsequences.TableName + "." + SMTablessalarmsequences.sname
			+ ", " + SMTablessalarmsequences.TableName + "." + SMTablessalarmsequences.llastarmedbyid
			+ ", " + SMTablessalarmsequences.TableName + "." + SMTablessalarmsequences.slastarmedbyfullname
			+ ", " + SMTablessalarmsequences.TableName + "." + SMTablessalarmsequences.llastdisarmedbyid
			+ ", " + SMTablessalarmsequences.TableName + "." + SMTablessalarmsequences.slastdisarmedbyfullname
			+ " FROM " + SMTablessalarmsequences.TableName
			+ " LEFT JOIN " + SMTablessalarmsequenceusers.TableName 
			+ " ON " + SMTablessalarmsequences.TableName + "." + SMTablessalarmsequences.lid
			+ " = " + SMTablessalarmsequenceusers.TableName + "." + SMTablessalarmsequenceusers.lalarmsequenceid
			+ " WHERE ("
				+ "(" + SMTablessalarmsequenceusers.TableName + "." + SMTablessalarmsequenceusers.luserid
					+ " = " + sm.getUserID() + ")"
			+ ")"
			+ " ORDER BY " + SMTablessalarmsequences.TableName + "." + SMTablessalarmsequences.sname
		;
		try {
			ResultSet rs = clsDatabaseFunctions.openResultSet(
				SQL, 
				getServletContext(), 
				sm.getsDBID(), 
				"MySQL", 
				this.toString() + ".createAlarmRows - user: " 
				+ sm.getUserID()
				+ " - "
				+ sm.getFullUserName()
				
			);
			while (rs.next()){
				String sBackgroundColor = "";

				if (bOddRow){
					sBackgroundColor = SMMasterStyleSheetDefinitions.BACKGROUND_WHITE;
				}else{
					sBackgroundColor = SMMasterStyleSheetDefinitions.BACKGROUND_PALE_BLUE;
				}
				
				int iAlarmState = rs.getInt(SMTablessalarmsequences.TableName + "." + SMTablessalarmsequences.ialarmstate);
				String sAlarmStateToSet = Integer.toString(SMTablessalarmsequences.ALARM_STATE_ARMED);
				if (iAlarmState == SMTablessalarmsequences.ALARM_STATE_ARMED || iAlarmState == SMTablessalarmsequences.ALARM_STATE_TRIGGERED){
					sAlarmStateToSet = Integer.toString(SMTablessalarmsequences.ALARM_STATE_UNARMED);
				}
				
				s += "  <TR style = \" background-color: " + sBackgroundColor +  "\" > \n"; //line-height: 60px; padding: 10px;
				//Name:
				s+= "    <TD class = \" centerjustifiedcell \">"
					+ rs.getString(SMTablessalarmsequences.TableName + "." + SMTablessalarmsequences.sname)
					+ "</TD>\n"
				;
				
				String sChecked = "";
				if(clsManageRequestParameters.get_Request_Parameter(ALARMSEQUENCE_CHECKBOX_NAME_PREFIX	
						+ Long.toString(rs.getLong(SMTablessalarmsequences.TableName + "." + SMTablessalarmsequences.lid)), sm.getRequest()).compareToIgnoreCase("") != 0){
					sChecked = clsServletUtilities.CHECKBOX_CHECKED_STRING;
				}
				
				s+= "    <TD class = \" leftjustifiedcell \">\n"
					+ "      <label>"
					+ "Check&nbsp;\n"
					+ "      <INPUT TYPE=CHECKBOX "
					+ " NAME=\"" + ALARMSEQUENCE_CHECKBOX_NAME_PREFIX	
						+ Long.toString(rs.getLong(SMTablessalarmsequences.TableName + "." + SMTablessalarmsequences.lid)) + "\""
					+ " id = \"" + ALARMSEQUENCE_CHECKBOX_NAME_PREFIX	
						+ Long.toString(rs.getLong(SMTablessalarmsequences.TableName + "." + SMTablessalarmsequences.lid)) + "\""
					+ sChecked
					//+ " width=0.25"
					+ ">\n"
					+ "      &nbsp;"
					+ "to"
					
					//build a button to activate:
					+ "&nbsp;\n"
					+ createArmButton(
						Long.toString(rs.getLong(SMTablessalarmsequences.TableName + "." + SMTablessalarmsequences.lid)),
						rs.getInt(SMTablessalarmsequences.TableName + "." + SMTablessalarmsequences.ialarmstate))
					+ "      &nbsp;"
					+ "</label>\n"
					//Store the alarm state of each alarm sequence here:
					+ "      <INPUT TYPE=HIDDEN"
						+ " NAME=\"" + PARAMETER_ARM_STATE_TO_SET + Long.toString(rs.getLong(SMTablessalarmsequences.TableName + "." + SMTablessalarmsequences.lid)) + "\""
						+ " VALUE=\"" + sAlarmStateToSet + "\""
						+ " id=\"" + PARAMETER_ARM_STATE_TO_SET + Long.toString(rs.getLong(SMTablessalarmsequences.TableName + "." + SMTablessalarmsequences.lid)) + "\""
						+ " >\n"
					+ "    </TD>\n"
				;
				
				String sAlarmStatus = SMTablessalarmsequences.getAlarmStateLabel(iAlarmState);
				//If the alarm is set, show when:
				String sAlarmInfo = "";
				if (iAlarmState != SMTablessalarmsequences.ALARM_STATE_UNARMED){
					sAlarmInfo = "<span style = \" font-size: small; color: grey; \"><BR>(Previously set at <B><I>" 
						+ clsDateAndTimeConversions.resultsetDateTimeToTheMinuteStringToString(rs.getString(SMTablessalarmsequences.TableName + "." + SMTablessalarmsequences.datlastarmed))
						+ " by " + rs.getString(SMTablessalarmsequences.llastarmedbyid) + " ("
						+ rs.getString(SMTablessalarmsequences.slastarmedbyfullname) + ")"
						+ "</I></B></span>";
				}else{
					sAlarmInfo = "<span style = \" font-size: small; color: grey; \"><BR>(Previously UNset at <B><I>" 
						+ clsDateAndTimeConversions.resultsetDateTimeToTheMinuteStringToString(rs.getString(SMTablessalarmsequences.TableName + "." + SMTablessalarmsequences.datlastdisarmed))
						+ " by " + rs.getString(SMTablessalarmsequences.slastdisarmedbyfullname) + ")"
						+ "</I></B></span>";
				}
				s+= "    <TD class = \" centerjustifiedcell \">"
						+ "<B>"
						+ sAlarmStatus
						+ sAlarmInfo
						+ "</B>"
						+ "</TD>\n"
					;
				
				String sAlarmDelayNote = "";
				if (iAlarmState == SMTablessalarmsequences.ALARM_STATE_UNARMED){
					sAlarmDelayNote = "<span style = \" font-size: small; color: grey; \"><BR>\n"
						+ "      (This alarm will be activated <B><I>" 
						+ Long.toString(rs.getLong(SMTablessalarmsequences.TableName + "." + SMTablessalarmsequences.lalarmsetdelaycountdown))
						+ " seconds</I></B> after you set it.)</span>";
				}
				String sAlarmSequenceIDLink = "Alarm ID:&nbsp;" + Long.toString(rs.getLong(SMTablessalarmsequences.TableName + "." + SMTablessalarmsequences.lid));
				if (bAllowAlarmSequenceEdit){
					sAlarmSequenceIDLink = "<A HREF=\"" 
						+ SMUtilities.getURLLinkBase(getServletContext()) 
						+ "smas.ASEditAlarmSequencesEdit?" 
						+ SMTablessalarmsequences.lid + "=" 
						+ Long.toString(rs.getLong(SMTablessalarmsequences.TableName + "." + SMTablessalarmsequences.lid))
						+ "\">" + sAlarmSequenceIDLink + "</A>"
					;
				}

				s+= "    <TD class = \" leftjustifiedcell \">"
					+ sAlarmSequenceIDLink
					+ "&nbsp;"
					+ "<I>"
					+ rs.getString(SMTablessalarmsequences.TableName + "." + SMTablessalarmsequences.sdescription)
					+ "</I>"
					//Show the delay time:
					+ sAlarmDelayNote
					+ "</TD>\n"
				;
				s += "  </TR>\n";

				bOddRow = !bOddRow;
			}
			rs.close();
		} catch (Exception e) {
			throw new Exception ("Error [1487945383] reading alarm sequences with SQL: " + SQL + " - " + e.getMessage());
		}

		return s;
	}

	private String createArmButton(String sZoneID, int iCurrentArmingState){
		//Green for armed zones, red for disarmed zones:
		String sBackgroundColor = SMMasterStyleSheetDefinitions.GOOGLE_GREEN; //Green
		String sCommandValue = ARM_ZONE_BUTTON_LABEL;
		if (iCurrentArmingState == SMTablessalarmsequences.ALARM_STATE_UNARMED){
			sBackgroundColor = SMMasterStyleSheetDefinitions.GOOGLE_GREEN; //Green
			sCommandValue = ARM_ZONE_BUTTON_LABEL;
		}else{
			sBackgroundColor = SMMasterStyleSheetDefinitions.GOOGLE_RED; //Red
			sCommandValue = DISARM_ZONE_BUTTON_LABEL;
		}
		return "      <button type=\"button\"\n"
			+ "        style = \"\n" 
			+ "          background-color: " + sBackgroundColor + ";\n"
			+ "          border: none;\n"
			+ "          color: white;\n"
			+ "          padding: 6px 6px;\n"
			+ "          text-align: center;\n"
			+ "          text-decoration: none;\n"
			+ "          display: inline-block;\n"
			+ "          font-size: 16px;\n"
			+ "          box-shadow: 0 12px 16px 0 rgba(0,0,0,0.24),0 17px 50px 0 rgba(0,0,0,0.19);\n"
			+ "        \"\n"
			+ "        value=\"" + sCommandValue + "\"\n"
			+ "        name=\"" + ARM_DISARM_BUTTON_NAME + "\"\n"
			//+ " onClick=\"armzone('" + sZoneID + "', '" + sArmStateToSet + "');\""
			+ "      >\n"
			+ "        " + sCommandValue + "\n" 
			+ "      </button>\n"
			;
	}

	private String sCommandScripts(boolean bRecordUserLocation){
		String s = "";
		
		s += "<NOSCRIPT>\n"
			+ "    <font color=red>\n"
			+ "    <H3>This page requires that JavaScript be enabled to function properly</H3>\n"
			+ "    </font>\n"
			+ "</NOSCRIPT>\n"
		;
		
		//Geocode functions:
		s += "  <script type=\"text/javascript\"\n"
		    //+ "    src=\"https://maps.googleapis.com/maps/api/js?sensor=false\">\n"
		    + "    src=\"https://maps.googleapis.com/maps/api/js?key=" + SMGoogleMapAPIKey.SMCP_GMAPS_API_KEY1 + "\">\n"
		    + "  </script>\n"
			;
			s += "<script type=\"text/javascript\">\n";
			s += "window.onload = triggerInitialGeocode();\n";
			
			s += "var watchID;\n";
			s += "var geoLoc;\n";
			
			s += "var t;\n";
			s += "var timer_is_on=0;\n";

			s += "\n";
			s += "function errorHandler(err) {\n";
			s += "    if(err.code == 1) {\n";
			//s += "        alert('Error: Access is denied!');\n";
			s += "        document.getElementById(\"" + PARAMETER_LOCATION_RECORDING_ALLOWED + "\").value = '" 
					+ PARAMETER_LOCATION_RECORDING_VALUE_NOT_ALLOWED + "';\n";
			s += "    }else if( err.code == 2) {\n";
			s += "        alert('Error: Position is unavailable!');\n";
			s += "    }\n";
			s += "}\n";
			s += "\n";

			//Activate device:
			s += "function updateAlarmSequences(){\n"
				//+ "    document.getElementById(\"" + SMTablessalarmsequences.lid + "\").value = sZoneID;\n"
				//+ "    document.getElementById(\"" + PARAMETER_ARM_STATE_TO_SET + "\").value = iArmStateToSet;\n"
				+ "    getGeocode();\n"
				+ "    document.forms[\"MAINFORM\"].submit();\n"
				+ "}\n"
			;
			
			s += "function triggerInitialGeocode(){\n";
			if (bRecordUserLocation){
				s += "    if(navigator.geolocation){\n";
				          // timeout at 60000 milliseconds (60 seconds)
				s += "        var options = {enableHighAccuracy:true, maximumAge:120000, timeout:45000};\n";
				//s += "        geoLoc = navigator.geolocation;\n";
				;
				s += "        navigator.geolocation.getCurrentPosition(initialGeocodeTrigger, errorHandler, options);\n";
				//s += "        t=setTimeout(\"getGeocode()\",60000);\n";
				s += "    }else{\n";
				s += "        alert('Browser does not support geolocation!');\n";
				s += "    }\n";
			}
			s += "}\n";

			//This function is just a dummy to allow us to force the geocoding to happen when the screen first loads - 
			//this allows the user to choose, that first time, whether to allow the site to record his location:
			s += "function initialGeocodeTrigger(position){\n"
					+ "    document.getElementById(\"" + PARAMETER_USER_LATITUDE + "\").value = " 
						+ "position.coords.latitude;\n"
					+ "    document.getElementById(\"" + PARAMETER_USER_LONGITUDE + "\").value = " 
						+ "position.coords.longitude;\n"
					+ "}\n"
				;
			
			s += "function getGeocode(){\n";
			if (bRecordUserLocation){
				s += "    if(navigator.geolocation){\n";
				          // timeout at 60000 milliseconds (60 seconds)
				s += "        var options = {enableHighAccuracy:true, maximumAge:120000, timeout:45000};\n";
				//s += "        geoLoc = navigator.geolocation;\n";
				s += "        navigator.geolocation.getCurrentPosition(submitValues, errorHandler, options);\n";
				//s += "        t=setTimeout(\"getGeocode()\",60000);\n";
				s += "    }else{\n";
				s += "        alert('Browser does not support geolocation!');\n";
				s += "    }\n";
			}else{
				s += "    document.forms[\"MAINFORM\"].submit();\n";
			}
			s += "}\n";
			
			s += "function submitValues(position){\n"
				+ "    document.getElementById(\"" + PARAMETER_USER_LATITUDE + "\").value = " 
					+ "position.coords.latitude;\n"
				+ "    document.getElementById(\"" + PARAMETER_USER_LONGITUDE + "\").value = " 
					+ "position.coords.longitude;\n"
				//+ "    document.getElementById(\"" + SMRecordGeocodeAction.SPEED_PARAMETER + "\").value = " 
				//	+ "position.coords.speed;\n"
				//+ "    document.getElementById(\"" + SMRecordGeocodeAction.ALTITUDE_PARAMETER + "\").value = " 
				//	+ "position.coords.altitude;\n"
				//+ "    document.getElementById(\"" + SMRecordGeocodeAction.ACCURACY_PARAMETER + "\").value = " 
				//	+ "position.coords.accuracy;\n"
				//+ "    document.getElementById(\"" + SMRecordGeocodeAction.ALTITUDEACCURACY_PARAMETER + "\").value = " 
				//	+ "position.coords.altitudeaccuracy;\n"
				//+ "    document.getElementById(\"" + SMRecordGeocodeAction.TIMESTAMP_PARAMETER + "\").value = " 
				//	+ "position.timestamp;\n"
				+ "    document.forms[\"MAINFORM\"].submit();\n"
				+ "}\n"
			;
			
		//*****************************************************

		s += "</script>\n";
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
			+ "font-weight: bold; "
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
			+ "padding: 10px; "
			//+ "border-color: " + CELL_BORDER_COLOR + "; "
			+ "vertical-align: center;"
			+ "font-family : Arial; "
			+ "font-weight: bold; "
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
			+ "padding: 10px; "
			//+ "border-color: " + CELL_BORDER_COLOR + "; "
			+ "vertical-align: center;"
			+ "font-family : Arial; "
			+ "font-weight: bold; "
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
			+ "padding: 10px; "
			//+ "border-color: " + CELL_BORDER_COLOR + "; "
			+ "vertical-align: center;"
			+ "font-family : Arial; "
			+ "font-weight: bold; "
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