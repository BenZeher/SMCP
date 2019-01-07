package smcontrolpanel;

import java.io.IOException;
import java.util.ArrayList;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import ConnectionPool.ServerSettingsFileParameters;
import ServletUtilities.clsManageRequestParameters;
import smcontrolpanel.SMMasterEditAction;
import smcontrolpanel.SMSystemFunctions;

public class SMServerSettingsAction extends HttpServlet{
	
	private static final long serialVersionUID = 1L;
	public void doPost(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
	
		String sErrorMessage = "";
		SMMasterEditAction smaction = new SMMasterEditAction(request, response);
		if (!smaction.processSession(getServletContext(), SMSystemFunctions.SMEditServerSettingsFile)){return;}
		
		//Validate form parameters
		String sServerHostNameValue = clsManageRequestParameters.get_Request_Parameter(ServerSettingsFileParameters.SERVER_SETTING_SERVER_HOST_NAME, request);
		if(sServerHostNameValue.compareToIgnoreCase("") == 0) {
			sErrorMessage += "Server host name can not be blank.";
		}
		
		if (sServerHostNameValue.endsWith("/")){
			sServerHostNameValue = sServerHostNameValue.substring(0, sServerHostNameValue.length() - 1);
		}
		
		String sControlsDatabaseNameValue = clsManageRequestParameters.get_Request_Parameter(ServerSettingsFileParameters.SERVER_SETTING_CONTROL_DB_NAME, request);
		if(sControlsDatabaseNameValue.compareToIgnoreCase("") == 0) {
			sErrorMessage += "Controls database name can not be blank.";
		}

		String sControlsDatabaseURL = clsManageRequestParameters.get_Request_Parameter(ServerSettingsFileParameters.SERVER_SETTING_CONTROL_DB_URL, request);
		if(sControlsDatabaseURL.compareToIgnoreCase("") == 0) {
			sErrorMessage += "Controls database can not be blank.";
		}	
		String sControlsDatabasePortValue = clsManageRequestParameters.get_Request_Parameter(ServerSettingsFileParameters.SERVER_SETTING_CONTROL_DB_PORT, request);
		if(sControlsDatabasePortValue.compareToIgnoreCase("") == 0) {
			sErrorMessage += "Controls database can not be blank.";
		}	
		String sControlsDatabaseUsernameValue = clsManageRequestParameters.get_Request_Parameter(ServerSettingsFileParameters.SERVER_SETTING_CONTROL_DB_USERNAME, request);
		if(sControlsDatabaseUsernameValue.compareToIgnoreCase("") == 0) {
			sErrorMessage += "Controls database can not be blank.";
		}	
		String sControlsDatabasePasswordValue = clsManageRequestParameters.get_Request_Parameter(ServerSettingsFileParameters.SERVER_SETTING_CONTROL_DB_PASSWORD, request);
		if(sControlsDatabasePasswordValue.compareToIgnoreCase("") == 0) {
			sErrorMessage += "Controls database can not be blank.";
		}	
		String sServerIDValue = clsManageRequestParameters.get_Request_Parameter(ServerSettingsFileParameters.SERVER_SETTING_SERVER_ID, request);
		if(sServerIDValue.compareToIgnoreCase("") == 0) {
			sErrorMessage += "Server ID can not be blank.";
		}		
		String sRunSchedulerValue = clsManageRequestParameters.get_Request_Parameter(ServerSettingsFileParameters.SERVER_SETTING_RUN_SCHEDULER, request);
		if(sRunSchedulerValue.compareToIgnoreCase("") == 0) {
			sRunSchedulerValue = "0";
		}else {
			sRunSchedulerValue = "1";
		}		
		String sRunAppointmentNotificationsValue = clsManageRequestParameters.get_Request_Parameter(ServerSettingsFileParameters.SERVER_SETTING_RUN_APPOINTMENT_NOTIFICATIONS, request);;
		if(sRunAppointmentNotificationsValue.compareToIgnoreCase("") == 0) {
			sRunAppointmentNotificationsValue = "0";
		}else {
			sRunAppointmentNotificationsValue = "1";
		}	
		String sServerDebuggingValueValue = clsManageRequestParameters.get_Request_Parameter(ServerSettingsFileParameters.SERVER_SETTING_DEBUG_LEVEL, request);;
		String sTestDeviceID = clsManageRequestParameters.get_Request_Parameter(ServerSettingsFileParameters.SERVER_SETTING_TEST_DEVICE_ID, request);
		if(sTestDeviceID.compareToIgnoreCase("") == 0) {
			sTestDeviceID += "0";
		}	
		
		//Update the Server settings file by overwriting the file with the form values.
	    if(smaction.isEditRequested()){
	    	try {
	    		if(sErrorMessage.compareToIgnoreCase("") != 0) {
	    			throw new Exception(sErrorMessage);
	    		}
	    		ArrayList<String> arrKeys =  new ArrayList<String>();
	    		arrKeys.add(ServerSettingsFileParameters.SERVER_SETTING_SERVER_HOST_NAME);
	    		arrKeys.add(ServerSettingsFileParameters.SERVER_SETTING_CONTROL_DB_NAME);
	    		arrKeys.add(ServerSettingsFileParameters.SERVER_SETTING_CONTROL_DB_URL);
	    		arrKeys.add(ServerSettingsFileParameters.SERVER_SETTING_CONTROL_DB_PORT);
	    		arrKeys.add(ServerSettingsFileParameters.SERVER_SETTING_CONTROL_DB_USERNAME);
	    		arrKeys.add(ServerSettingsFileParameters.SERVER_SETTING_CONTROL_DB_PASSWORD);
	    		arrKeys.add(ServerSettingsFileParameters.SERVER_SETTING_SERVER_ID);
	    		arrKeys.add(ServerSettingsFileParameters.SERVER_SETTING_RUN_SCHEDULER);
	    		arrKeys.add(ServerSettingsFileParameters.SERVER_SETTING_RUN_APPOINTMENT_NOTIFICATIONS);
	    		arrKeys.add(ServerSettingsFileParameters.SERVER_SETTING_DEBUG_LEVEL);
	    		arrKeys.add(ServerSettingsFileParameters.SERVER_SETTING_TEST_DEVICE_ID);
	    		ArrayList<String> arrKeyValues = new ArrayList<String>();
	    		arrKeyValues.add(sServerHostNameValue);
	    		arrKeyValues.add(sControlsDatabaseNameValue);
	    		arrKeyValues.add(sControlsDatabaseURL);
	    		arrKeyValues.add(sControlsDatabasePortValue);
	    		arrKeyValues.add(sControlsDatabaseUsernameValue);
	    		arrKeyValues.add(sControlsDatabasePasswordValue);
	    		arrKeyValues.add(sServerIDValue);
	    		arrKeyValues.add(sRunSchedulerValue);
	    		arrKeyValues.add(sRunAppointmentNotificationsValue);
	    		arrKeyValues.add(sServerDebuggingValueValue);
	    		arrKeyValues.add(sTestDeviceID);
	    		ServerSettingsFileParameters file = new ServerSettingsFileParameters(SMUtilities.getFullPathToResourceFolder(getServletContext()) + ServerSettingsFileParameters.SERVER_SETTINGS_FILENAME);
	    		file.writeKeyValues(arrKeys, arrKeyValues, false);
			
	    	} catch (Exception e) {
				smaction.redirectAction(
					"Could not configure settings file: " + e.getMessage(), 
					"", 
					""
				);
				return;
			}

	    	
			if (smaction.getOriginalURL().trim().compareToIgnoreCase("") != 0){
				smaction.returnToOriginalURL();
			}else{
				smaction.redirectAction(
					"", 
					"Settings file was successfully configured.",
					""
				);
			}
	    }
		return;
	}
	public void doGet(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
		
		doPost(request, response);
	}
}