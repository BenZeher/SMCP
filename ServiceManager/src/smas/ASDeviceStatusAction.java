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

public class ASDeviceStatusAction extends HttpServlet{
	
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
		String sUserLatitude = clsManageRequestParameters.get_Request_Parameter(ASDeviceStatusEdit.PARAMETER_USER_LATITUDE, request);
		String sUserLongitude = clsManageRequestParameters.get_Request_Parameter(ASDeviceStatusEdit.PARAMETER_USER_LONGITUDE, request);
		String sActivationDuration = clsManageRequestParameters.get_Request_Parameter(ASDeviceStatusEdit.PARAMETER_DURATION_PREFIX + sDeviceID, request).replace(",", "");

		//If the user has not allowed the location recording, notify them and get them back to the screen:
		if (clsManageRequestParameters.get_Request_Parameter(ASDeviceStatusEdit.PARAMETER_LOCATION_RECORDING_ALLOWED, request).compareToIgnoreCase(
				ASDeviceStatusEdit.PARAMETER_LOCATION_RECORDING_VALUE_NOT_ALLOWED) == 0){
			smaction.redirectAction(
				"This alarm is secured, so you need to ALLOW this web page to record the location before you can activate it.", 
				"",
				""
				);
			return;
		}
		
		entry.setslid(sDeviceID);
		try {
			entry.load(getServletContext(), smaction.getsDBID(), smaction.getUserName(), smaction.getUserID(), smaction.getFullUserName());
		} catch (Exception e1) {
			smaction.redirectAction(
				"Could not read device ID '" + sDeviceID + "': " + e1.getMessage(), 
				"",
				""
				);
			return;
		}
		
		//Test for a valid duration:
		String sDurationValidationError = "";
		if (sActivationDuration.compareToIgnoreCase("") != 0){
			long lTest;
			try {
				lTest = Long.parseLong(sActivationDuration);
				if(lTest < 0){
					sDurationValidationError = "Duration must be zero or greater.";
				}
			} catch (NumberFormatException e) {
				sDurationValidationError = "Invalid duration ('" + sActivationDuration + "')";
			}
			if (sDurationValidationError.compareToIgnoreCase("") != 0){
				smaction.redirectAction(
					sDurationValidationError, 
					"",
					""
					);
				return;
			}
		}else{
			int iDuration = -1;
			try {
				iDuration = Integer.parseInt(entry.getscontactduration());
			}catch (Exception e) {
				sDurationValidationError = "Error [1493506949] - Error validating contact duration: " + e.getMessage();
			}			
			if(iDuration <= 0){
				sActivationDuration = SSConstants.DEFAULT_MOMENTARY_CONTACT_ACTIVATE_INTERVAL_IN_MS;			
			}else{
				sActivationDuration = entry.getscontactduration();
			}
		}
		
		//Activate/deactivate the device:
		String sSetContactState = clsManageRequestParameters.get_Request_Parameter(ASDeviceStatusEdit.SET_OUTPUT_CONTACTS_PARAMETER, request);
		try {
			entry.setOutPutContactsState(
				smaction.getsDBID(), 
				smaction.getUserName(), 
				getServletContext(), 
				sSetContactState,
				sActivationDuration,
				SMBackgroundScheduleProcessor.getServerID(getServletContext()));
		} catch (Exception e1) {
			smaction.redirectAction(
				"Could not toggle device '" + entry.getsdescription() + "': " + e1.getMessage(), 
				"",
				""
				);
			return;
		}
		
		//Record the activation in the user events log:
		ASUserEventLogEntry usereventlog = new ASUserEventLogEntry(smaction.getsDBID());
		try {
			
			usereventlog.writeEntry(
				SMTablessuserevents.DEVICE_ACTIVATED, 
				smaction.getUserID(),
				sUserLatitude, 
				sUserLongitude, 
				entry.getslid(), 
				entry.getsdescription(), 
				"Set the output contacts to " + sSetContactState + " for " + sActivationDuration 
					+ " ms from the DEVICE STATUS web page in SMCP",
				"[459297590]", 
				null,
				"-1",
				getServletContext(),
				"[1547226139]"
			);
		} catch (Exception e) {
			smaction.redirectAction(
					"", 
					entry.getsdescription() + " was successfully toggled - but there was an error recording it in the user events log"
					+ " - " + e.getMessage() + ".",
					""
				);
				return;
		}
		
		smaction.redirectAction(
			"", 
			entry.getsdescription() + " was successfully toggled.",
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