package smas;

import java.io.IOException;
import java.sql.Connection;

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

public class ASActivateAlarmsAction extends HttpServlet{
	
	private static final long serialVersionUID = 1L;
	
	//private static String OBJECT_NAME = SSController.ParamObjectName;
	public void doPost(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
	
		SMMasterEditAction smaction = new SMMasterEditAction(request, response);
		if (!smaction.processSession(getServletContext(), SMSystemFunctions.ASActivateAlarmSequences)){return;}
	    //Read the entry fields from the request object:
		//SSAlarmSequence entry = new SSAlarmSequence();
		String sAlarmSequenceID = clsManageRequestParameters.get_Request_Parameter(SMTablessalarmsequences.lid, request);
		
		//System.out.println("[1465244287] sAlarmSequenceID = " + sAlarmSequenceID);
		
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
		
		//Check for a confirm:
		if (clsManageRequestParameters.get_Request_Parameter(
			ASActivateAlarmsEdit.CONFIRM_CHECKBOX_NAME_PREFIX + sAlarmSequenceID, request).compareToIgnoreCase("") == 0){
			smaction.redirectAction(
				"You need to check 'Confirm' if you want to arm or disarm this alarm sequence.", 
				"",
				""
				);
			return;
		}
		
		//Get the latitude and longitude:
		String sUserLatitude = clsManageRequestParameters.get_Request_Parameter(ASActivateAlarmsEdit.PARAMETER_USER_LATITUDE, request);
		String sUserLongitude = clsManageRequestParameters.get_Request_Parameter(ASActivateAlarmsEdit.PARAMETER_USER_LONGITUDE, request);
		String sArmStateToSet = clsManageRequestParameters.get_Request_Parameter(ASActivateAlarmsEdit.PARAMETER_ARM_STATE_TO_SET, request);
		boolean bOverrideMalfunctioningDevice = clsManageRequestParameters.get_Request_Parameter(
			ASActivateAlarmsEdit.PARAMETER_OVERRIDE_TRIGGER_DEVICE_MALFUNCTION, request).compareToIgnoreCase("") != 0;
		
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
				"Could not get data connection - " + e2.getMessage(), 
				"",
				""
				);
			return;
		}
		
		int iEventType = 0;
		String sArmedPhrase = "";
		if (sArmStateToSet.compareToIgnoreCase(Integer.toString(SMTablessalarmsequences.ALARM_STATE_ARMED)) == 0){
			iEventType = SMTablessuserevents.ALARM_ZONE_ARMED;
			sArmedPhrase = "ARMED";
		}
		if (sArmStateToSet.compareToIgnoreCase(Integer.toString(SMTablessalarmsequences.ALARM_STATE_UNARMED)) == 0){
			iEventType = SMTablessuserevents.ALARM_ZONE_DISARMED;
			sArmedPhrase = "DISARMED";
		}
		String sResult = "";
		try {
			sResult = setAlarmSequenceState(
				conn, 
				sAlarmSequenceID, 
				iEventType, 
				sUserLatitude, 
				sUserLongitude, 
				sArmStateToSet,
				sArmedPhrase,
				smaction.getUserName(),
				smaction.getUserID(),
				bOverrideMalfunctioningDevice,
				SMBackgroundScheduleProcessor.getServerID(getServletContext()));
		} catch (Exception e2) {
			clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1547067582]");
			String sErrorMessage = e2.getMessage();
			String sOtherParameters = "";
			//If the error indicates a malfunctioning trigger device, let the user know and then he can decide
			//whether or not to override it:
			if (sErrorMessage.contains(SSAlarmSequence.TRIGGER_DEVICE_SET_ERROR)){
				sOtherParameters = "&" + ASActivateAlarmsEdit.PARAMETER_TRIGGER_DEVICE_MALFUNCTION + "=Y";
			}
			smaction.redirectAction(
					"Could not set alarm sequence(s) - " + e2.getMessage(), 
					"",
					sOtherParameters
					);
				return;
		}
		
		clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1547067583]");
		smaction.redirectAction(
			"", 
			sResult,
			""
		);
		return;
	}
	private String setAlarmSequenceState(
			Connection conn, 
			String sAlarmSequenceID, 
			int iEventType, 
			String sUserLatitude, 
			String sUserLongitude, 
			String sArmStateToSet,
			String sArmedPhrase,
			String sUser,
			String sUserID,
			boolean bOverrideMalfunctioningDevice,
			String sServerID) throws Exception{

		String sResults = "";
		//If it's a command to set ALL the alarm sequences, then we have to get all the 
		//alarm sequences that this user is permitted to set:
		String sAlarmSequences[] = sAlarmSequenceID.split(",");
		SSAlarmSequence entry = null;
		for (int i = 0; i < sAlarmSequences.length; i++){
			try {
				entry = new SSAlarmSequence();
				entry.setslid(sAlarmSequences[i]);
				entry.load(conn);
			} catch (Exception e1) {
				throw new Exception("Could not read alarm sequence ID '" + sAlarmSequences[i] + "': " + e1.getMessage()); 
			}

			//Arm/Disarm the zone:
			String sStateSetResult = "Alarm sequence '" + entry.getsname() + "' was successfully " + sArmedPhrase + ".<BR>";
			if (sArmStateToSet.compareToIgnoreCase(Integer.toString(SMTablessalarmsequences.ALARM_STATE_ARMED)) == 0){
				sStateSetResult += 
					"   - You have <B><FONT COLOR = RED>" + entry.getsalarmsetcountdown() + " seconds </FONT></B> from the time"
					+ " the alarm was set to "
					+ " secure the alarm zone.<BR>"
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
					sServerID
					);
			} catch (Exception e1) {
				bAlarmSequenceWasSet = false;
				//If we are only trying to set one sequence, then simply return an error:
				if (sAlarmSequences.length == 1){
					throw new Exception("Alarm sequence '" + entry.getsname() + "' could not be " + sArmedPhrase + ": " + e1.getMessage());
				}else{
					sStateSetResult = "<B><FONT COLOR=RED>Alarm sequence '" + entry.getsname() + "' could not be " 
							+ sArmedPhrase + ": " + e1.getMessage() + "</FONT></B><BR>";
				}
			}
			sResults += sStateSetResult;

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
							"[1459297590]", 
							null,
							entry.getslid(),
							getServletContext(),
							"[1547226136]"
					);
				} catch (Exception e) {
					sResults += 
							entry.getsdescription() + " was successfully " + sArmedPhrase + " - but there was an error recording it in the user events log"
									+ " - " + e.getMessage() + "." + "<BR>";
				}
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