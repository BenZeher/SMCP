package smas;

import java.io.IOException;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Enumeration;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import SMClasses.SMBackgroundScheduleProcessor;
import SMDataDefinition.SMTablessalarmsequences;
import SMDataDefinition.SMTablessuserevents;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsManageRequestParameters;
import smcontrolpanel.SMMasterEditAction;
import smcontrolpanel.SMSystemFunctions;

public class ASActivateSelectedSequencesAction extends HttpServlet{
	
	private static final long serialVersionUID = 1L;
	
	//private static String OBJECT_NAME = SSController.ParamObjectName;
	public void doPost(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
	
		SMMasterEditAction smaction = new SMMasterEditAction(request, response);
		if (!smaction.processSession(getServletContext(), SMSystemFunctions.ASActivateSelectedSequences)){return;}
	    //Read the entry fields from the request object:
		
		//If the user has not allowed the location recording, notify them and get them back to the screen:
		if (clsManageRequestParameters.get_Request_Parameter(ASActivateAlarmsEdit.PARAMETER_LOCATION_RECORDING_ALLOWED, request).compareToIgnoreCase(
				ASActivateAlarmsEdit.PARAMETER_LOCATION_RECORDING_VALUE_NOT_ALLOWED) == 0){
			smaction.redirectAction(
				"This alarm is secured, so you need to ALLOW this web page to record the location before you can activate it.", 
				"",
				""
				);
			return;
		}
		
		//Get the latitude and longitude:
		String sUserLatitude = clsManageRequestParameters.get_Request_Parameter(ASActivateAlarmsEdit.PARAMETER_USER_LATITUDE, request);
		String sUserLongitude = clsManageRequestParameters.get_Request_Parameter(ASActivateAlarmsEdit.PARAMETER_USER_LONGITUDE, request);
		boolean bOverrideMalfunctioningDevice = clsManageRequestParameters.get_Request_Parameter(
			ASActivateAlarmsEdit.PARAMETER_OVERRIDE_TRIGGER_DEVICE_MALFUNCTION, request).compareToIgnoreCase("") != 0;

		ArrayList<String>arrSequencesToToggle = new ArrayList<String>(0);
		ArrayList<String>arrAlarmStatesToSet = new ArrayList<String>(0);
		//Now read all the selected alarm sequences and the states to set:
		Enumeration <String> e = request.getParameterNames();
		String sParam = "";

		while (e.hasMoreElements()){
			sParam = (String) e.nextElement();
			String sAlarmSequenceID = "";
			String sAlarmSequenceStateToSet = "";
			
			if (sParam.contains(ASActivateSelectedSequencesEdit.ALARMSEQUENCE_CHECKBOX_NAME_PREFIX)){
				sAlarmSequenceID = sParam.substring(ASActivateSelectedSequencesEdit.ALARMSEQUENCE_CHECKBOX_NAME_PREFIX.length(), sParam.length());
				sAlarmSequenceStateToSet = clsManageRequestParameters.get_Request_Parameter(
						ASActivateSelectedSequencesEdit.PARAMETER_ARM_STATE_TO_SET + sAlarmSequenceID, 
					request);
				//System.out.println("[1488145310] sAlarmSequenceID = '" + sAlarmSequenceID + "'"
				//	+ ", sAlarmSequenceStateToSet = SMUtilities.get_Request_Parameter(ASActivateSelectedSequencesEdit.PARAMETER_ARM_STATE_TO_SET + sAlarmSequenceID, request) = '"
				//	+ SMUtilities.get_Request_Parameter(
				//		ASActivateSelectedSequencesEdit.PARAMETER_ARM_STATE_TO_SET + sAlarmSequenceID, 
				//		request)
				//	+ "'"
				//);
				arrSequencesToToggle.add(sAlarmSequenceID);
				arrAlarmStatesToSet.add(sAlarmSequenceStateToSet);
			}
		}
		
		//Set the alarm sequence(s):
		Connection conn = null;
		try {
			conn = clsDatabaseFunctions.getConnectionWithException(
				getServletContext(), 
				smaction.getsDBID(),
				"MySQL", 
				this.toString() + ".doPost - user: " + smaction.getUserName());
		} catch (Exception e2) {
			smaction.redirectAction(
				"Error [1488142806] - Could not get data connection - " + e2.getMessage(), 
				"",
				""
				);
			return;
		}
		
		String sResult = "";
		String sQueryParameters = getQueryParameters(request);
		for (int i = 0; i < arrSequencesToToggle.size(); i++){
			//If there is a critical error - not just an inability to set a particular alarm sequence - then we get a exception and
			// just report back to the user.
			//But otherwise, we collect all the results in a string, and report that string back to the user:
			try {
				sResult += "<BR>" 
					+ setAlarmSequenceState(
						conn, 
						arrSequencesToToggle.get(i), 
						sUserLatitude, 
						sUserLongitude, 
						arrAlarmStatesToSet.get(i),
						smaction.getUserName(),
						smaction.getUserID(),
						bOverrideMalfunctioningDevice,
						SMBackgroundScheduleProcessor.getServerID(getServletContext())
					);
			} catch (Exception e2) {
				clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1547067589]");
				smaction.redirectAction(
					"Could not set alarm sequence(s) - " + e2.getMessage(), 
					"",
					sQueryParameters
				);
				return;
			}
		}
		//If the result string indicates a malfunctioning trigger device, let the user know and then he can decide
		//whether or not to override it:
		//System.out.println("[1488146442] - sResult = '" + sResult + "'");
		if (sResult.contains(SSAlarmSequence.TRIGGER_DEVICE_SET_ERROR)){
			//Put the message in the 'other parameters' string so the screen can advise the user and give them the checkbox option to OVERRIDE:
			sResult.replace(ASActivateAlarmsEdit.PARAMETER_TRIGGER_DEVICE_MALFUNCTION + "=Y", "");
			sQueryParameters += "&" + ASActivateAlarmsEdit.PARAMETER_TRIGGER_DEVICE_MALFUNCTION + "=Y";
		}
		
		clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1547067590]");
		smaction.redirectAction(
			"", 
			sResult,
			sQueryParameters
		);
		return;
	}
	private String getQueryParameters(HttpServletRequest req){
		String s = "";
		Enumeration <String> e = req.getParameterNames();
		String sParam = "";

		boolean bIsFirstParameter = true;
		while (e.hasMoreElements()){
			sParam = (String) e.nextElement();
			if (bIsFirstParameter){
				s += sParam + "=" + clsManageRequestParameters.get_Request_Parameter(sParam, req);
			}else{
				s += "&" + sParam + "=" + clsManageRequestParameters.get_Request_Parameter(sParam, req);
			}
			bIsFirstParameter = false;
		}
		return s;
	}
	private String setAlarmSequenceState(
			Connection conn, 
			String sAlarmSequenceID, 
			String sUserLatitude, 
			String sUserLongitude, 
			String sArmStateToSet,
			String sUser,
			String sUserID,
			boolean bOverrideMalfunctioningDevice,
			String sServerID) throws Exception{

		String sResults = "";
		int iEventType = 0;
		String sArmedPhrase = "";
		
		//System.out.println("[1488144818] sAlarmSequenceID = '" + sAlarmSequenceID + "', sArmStateToSet = '" + sArmStateToSet + "'.");
		if (sArmStateToSet.compareToIgnoreCase(Integer.toString(SMTablessalarmsequences.ALARM_STATE_ARMED)) == 0){
			iEventType = SMTablessuserevents.ALARM_ZONE_ARMED;
			sArmedPhrase = "ARMED";
		}
		if (sArmStateToSet.compareToIgnoreCase(Integer.toString(SMTablessalarmsequences.ALARM_STATE_UNARMED)) == 0){
			iEventType = SMTablessuserevents.ALARM_ZONE_DISARMED;
			sArmedPhrase = "DISARMED";
		}
		SSAlarmSequence entry = null;
		try {
			entry = new SSAlarmSequence();
			entry.setslid(sAlarmSequenceID);
			entry.load(conn);
		} catch (Exception e1) {
			throw new Exception("Could not read alarm sequence ID '" + sAlarmSequenceID + "': " + e1.getMessage()); 
		}

		//Arm/Disarm the zone:
		sResults = "Alarm sequence '" + entry.getsname() + "' was successfully " + sArmedPhrase + ".";
		if (sArmStateToSet.compareToIgnoreCase(Integer.toString(SMTablessalarmsequences.ALARM_STATE_ARMED)) == 0){
			sResults += 
				"   - You have <B><FONT COLOR = GREEN>" + entry.getsalarmsetcountdown() + " seconds </FONT></B> from the time"
				+ " the alarm was set to "
				+ " secure the alarm zone."
			;
		}
		boolean bAlarmSequenceWasSet = true;
		try {
			entry.setArmedState(
				conn, 
				sUser, 
				sUserID,
				Integer.parseInt(sArmStateToSet), 
				bOverrideMalfunctioningDevice, 
				getServletContext(),
				sServerID);
		} catch (Exception e1) {
			bAlarmSequenceWasSet = false;
			sResults = "<FONT COLOR=RED>Alarm sequence " 
				+ sAlarmSequenceID + " '" + entry.getsname() + "' could not be " + sArmedPhrase + ": " + e1.getMessage()
				+ "</FONT>";
		}

		//Record the activation in the user events log:
		if (bAlarmSequenceWasSet){
			ASUserEventLogEntry usereventlog = new ASUserEventLogEntry(conn);
			try {
				usereventlog.writeEntry(
						iEventType, 
						sUserID,
						sUserLatitude, 
						sUserLongitude,
						"0",
						"", 
						sArmedPhrase + " " + entry.getsname() + " alarm sequence from web page in SMCP", 
						"[1488143001]", 
						null,
						entry.getslid(),
						getServletContext()
				);
			} catch (Exception e) {
				sResults += " - "
					+ entry.getsdescription() + " was successfully " + sArmedPhrase + " - but there was an error recording it in the user events log"
					+ " - " + e.getMessage() + ".";
			}
		}
		return sResults;
	}
	public void doGet(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
		
		doPost(request, response);
	}
}