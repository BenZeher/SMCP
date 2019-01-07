package smas;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import SMClasses.SMBackgroundScheduleProcessor;
import SMDataDefinition.SMTablessdevices;
import SMDataDefinition.SMTablessuserevents;
import ServletUtilities.clsManageRequestParameters;
import smcontrolpanel.SMMasterEditAction;
import smcontrolpanel.SMSystemFunctions;
import sscommon.SSConstants;

public class ASActivateDevicesAction extends HttpServlet{
	
	private static final long serialVersionUID = 1L;
	
	//private static String OBJECT_NAME = SSController.ParamObjectName;
	public void doPost(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
	
		SMMasterEditAction smaction = new SMMasterEditAction(request, response);
		if (!smaction.processSession(getServletContext(), SMSystemFunctions.ASActivateDevices)){return;}
	    //Read the entry fields from the request object:
		SSDevice entry = new SSDevice();
		String sDeviceID = clsManageRequestParameters.get_Request_Parameter(SMTablessdevices.lid, request);
		
		//If the user has not allowed the location recording, notify them and get them back to the screen:
		if (clsManageRequestParameters.get_Request_Parameter(ASActivateDevicesEdit.PARAMETER_LOCATION_RECORDING_ALLOWED, request).compareToIgnoreCase(
			ASActivateDevicesEdit.PARAMETER_LOCATION_RECORDING_VALUE_NOT_ALLOWED) == 0){
			smaction.redirectAction(
				"This device is secured, so you need to ALLOW this web page to record the location before you can activate it.", 
				"",
				""
				);
			return;
		}
		
		//Check for a confirm:
		if (clsManageRequestParameters.get_Request_Parameter(
			ASActivateDevicesEdit.CONFIRM_CHECKBOX_NAME_PREFIX + sDeviceID, request).compareToIgnoreCase("") == 0){
			smaction.redirectAction(
				"You need to check 'Confirm' if you want to activate this device.", 
				"",
				""
				);
			return;
		}
		
		//Get the latitude and longitude:
		String sUserLatitude = clsManageRequestParameters.get_Request_Parameter(ASActivateDevicesEdit.PARAMETER_USER_LATITUDE, request);
		String sUserLongitude = clsManageRequestParameters.get_Request_Parameter(ASActivateDevicesEdit.PARAMETER_USER_LONGITUDE, request);
		String sCurrentActivationStatus = clsManageRequestParameters.get_Request_Parameter(sDeviceID + ASActivateDevicesEdit.PARAMETER_CURRENT_INPUT_ACTIVATION_STATE, request);
		//String sCurrentOutputActivationStatus = SMUtilities.get_Request_Parameter(ASActivateDevicesEdit.PARAMETER_CURRENT_OUTPUT_ACTIVATION_STATE, request);
		//String sActiveDeactivateCommand = SMUtilities.get_Request_Parameter(ASActivateDevicesEdit.ACTIVATE_DEVICE_PARAMETER, request);
		entry.setslid(sDeviceID);
		
		try {
			entry.load(getServletContext(), 
					smaction.getsDBID(), 
					smaction.getUserName(), 
					smaction.getUserID(),
					smaction.getFullUserName());
		} catch (Exception e1) {
			smaction.redirectAction(
				"Could not read device ID '" + sDeviceID + "': " + e1.getMessage(), 
				"",
				""
				);
			return;
		}
		
		//Activate/deactivate the device:
		//TODO - allow this to be set to either state, depending on the device type and status:		
		String sSetContactState = SSConstants.QUERY_KEYVALUE_TERMINAL_CONTACT_STATUS_CLOSED;
		
		String sActivationDuration = "0";
		String sContactDurationWarnings = "";
		//If the a device is set to momentary contact get the contact duration for that device.
		if(entry.getsactivationtype().compareToIgnoreCase(Integer.toString(SMTablessdevices.DEVICE_ACTIVATION_TYPE_MOMENTARY_CONTACT)) == 0){
			int iDuration = -1;
			try {
				iDuration = Integer.parseInt(entry.getscontactduration());
			}catch (Exception e) {
				sContactDurationWarnings += "Contact duration for device '" + sDeviceID + "' is not valid.";
			}			
			if(iDuration <= 0){
				sActivationDuration = SSConstants.DEFAULT_MOMENTARY_CONTACT_ACTIVATE_INTERVAL_IN_MS;
				sContactDurationWarnings += "Contact duration for device '" + sDeviceID 
					+ "' can not be used. Defaulting to " 
					+ SSConstants.DEFAULT_MOMENTARY_CONTACT_ACTIVATE_INTERVAL_IN_MS + "ms";
			}else{
				sActivationDuration = entry.getscontactduration();
			}
		//If the device is constant contact set the activation duration indefinitely 
		}else if(entry.getsactivationtype().compareToIgnoreCase(Integer.toString(SMTablessdevices.DEVICE_ACTIVATION_TYPE_CONSTANT_CONTACT)) == 0) {			
			//If the current output state is DEACTIVATED then activate it. 
			if(sCurrentActivationStatus.compareToIgnoreCase(SMTablessdevices.DEVICE_TYPE_STATE_ONOFF_DEACTIVATED) == 0
				|| sCurrentActivationStatus.compareToIgnoreCase(SMTablessdevices.DEVICE_TYPE_STATE_SMOKEDETECTOR_DEACTIVATED) == 0
				|| sCurrentActivationStatus.compareToIgnoreCase(SMTablessdevices.DEVICE_TYPE_STATE_MOTIONSENSOR_DEACTIVATED) == 0
				|| sCurrentActivationStatus.compareToIgnoreCase(SMTablessdevices.DEVICE_TYPE_STATE_DOOR_DEACTIVATED) == 0){
				sSetContactState = SSConstants.QUERY_KEYVALUE_TERMINAL_CONTACT_STATUS_CLOSED;
				sActivationDuration = Long.toString(Long.MAX_VALUE);
			}else{
				sSetContactState = SSConstants.QUERY_KEYVALUE_TERMINAL_CONTACT_STATUS_OPEN;
				sActivationDuration = Long.toString(0);
			}		
		}else{
			sActivationDuration = SSConstants.DEFAULT_MOMENTARY_CONTACT_ACTIVATE_INTERVAL_IN_MS;
		}
		
		try {
			entry.setOutPutContactsState(
				smaction.getsDBID(),
				smaction.getUserName(),
				getServletContext(),
				sSetContactState,
				sActivationDuration,
				SMBackgroundScheduleProcessor.getServerID(getServletContext()));
		}catch (Exception e1) {
			smaction.redirectAction(
				"Could not activate device '" + entry.getsdescription() + "': " + e1.getMessage(), 
				"",
				""
				
				);
			return;
		}
		
		//Record the activation in the user events log:
		ASUserEventLogEntry usereventlog = new ASUserEventLogEntry(smaction.getsDBID(), getServletContext());
		try {
			
			usereventlog.writeEntry(
				SMTablessuserevents.DEVICE_ACTIVATED, 
				smaction.getUserID(),
				sUserLatitude, 
				sUserLongitude, 
				entry.getslid(), 
				entry.getsdescription(), 
				"Activated the device from web page in SMCP when the device was currently " + sCurrentActivationStatus,
				"[459297590]", 
				null,
				"-1");
		} catch (Exception e) {
			smaction.redirectAction(
					"", 
					entry.getsdescription() + " was successfully activated - but there was an error recording it in the user events log"
					+ " - " + e.getMessage() + ".",
					""
				);
				return;
		}
		
		smaction.redirectAction(
			sContactDurationWarnings, 
			entry.getsdescription() + " was successfully activated.",
			""
		);
		return;
	}
	public void doGet(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
		
		doPost(request, response);
	}
}