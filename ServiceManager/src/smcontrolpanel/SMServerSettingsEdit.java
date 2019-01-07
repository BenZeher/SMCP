package smcontrolpanel;

import java.io.IOException;

import java.util.ArrayList;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import ConnectionPool.ServerSettingsFileParameters;
import ServletUtilities.clsCreateHTMLTableFormFields;
import smcontrolpanel.SMMasterEditEntry;
import smcontrolpanel.SMSystemFunctions;
import smcontrolpanel.SMUtilities;


public class SMServerSettingsEdit  extends HttpServlet {

	public static final String UPDATE_BUTTON_NAME = "UPDATE";
	public static final String UPDATE_BUTTON_LABEL = "Update";

	private static final long serialVersionUID = 1L;
	public void doPost(HttpServletRequest request,
			HttpServletResponse response)
					throws ServletException, IOException {

		SMMasterEditEntry smedit = new SMMasterEditEntry(
				request,
				response,
				getServletContext(),
				"Server Settings File",
				SMUtilities.getFullClassName(this.toString()),
				"smcontrolpanel.SMServerSettingsAction",
				"smcontrolpanel.SMUserLogin",
				"Go back to user login",
				SMSystemFunctions.SMEditServerSettingsFile
				);

		if (!smedit.processSession(getServletContext(), SMSystemFunctions.SMEditServerSettingsFile)){
			smedit.getPWOut().println("Error in process session: " + smedit.getErrorMessages());
			return;
		}
		
		smedit.printHeaderTable();

		smedit.getPWOut().println("<BR>");
		smedit.setbIncludeDeleteButton(false);

		try {
			smedit.createEditPage(getEditHTML(smedit), "");
		} catch (Exception e) {
			smedit.getPWOut().println("The Custom settings file is missing or not configured properly on your web server. Error: " + e.getMessage());				
			return;
		}
		return;
	}
	private String getEditHTML(SMMasterEditEntry sm) throws Exception{
		
		//Set the setting file path
		ServerSettingsFileParameters readFile = new ServerSettingsFileParameters(SMUtilities.getFullPathToResourceFolder(getServletContext()) + ServerSettingsFileParameters.SERVER_SETTINGS_FILENAME);
		
		String s = "<TABLE BORDER=1>";
		//Host server name
		String sControlsServerHostNameValue = readFile.readKeyValue(ServerSettingsFileParameters.SERVER_SETTING_SERVER_HOST_NAME);
		s += clsCreateHTMLTableFormFields.Create_Edit_Form_Text_Input_Row(
				ServerSettingsFileParameters.SERVER_SETTING_SERVER_HOST_NAME,
				sControlsServerHostNameValue, 
				40, 
				"<B>Server Host Name</B>",
				"The qualified URL (including 'http' or 'https', and the port number, if appropriate) of the server which hosts this web app.<BR>"
				+ "For example: <B>https://smcpserver.com</B> or <B>123.456.789:8080</B> or <B>localhost</B>.",
				"40"
				);

		//Controls database name
		String sControlsDatabaseNameValue = readFile.readKeyValue(ServerSettingsFileParameters.SERVER_SETTING_CONTROL_DB_NAME);
		s += clsCreateHTMLTableFormFields.Create_Edit_Form_Text_Input_Row(
				ServerSettingsFileParameters.SERVER_SETTING_CONTROL_DB_NAME,
				sControlsDatabaseNameValue, 
				40, 
				"<B>Controls Database Name</B>",
				"The name of the control database which contains credentials for all companies using this app",
				"40"
				);
		
		//Controls database URL
		String sControlsDatabaseURL = readFile.readKeyValue(ServerSettingsFileParameters.SERVER_SETTING_CONTROL_DB_URL);
		s += clsCreateHTMLTableFormFields.Create_Edit_Form_Text_Input_Row(
				ServerSettingsFileParameters.SERVER_SETTING_CONTROL_DB_URL,
				sControlsDatabaseURL, 
				40, 
				"<B>Controls Database URL</B>",
				"The location of the MySQL server that holds the control database",
				"40"
				);
		
		//Controls database port
		String sControlsDatabasePortValue = readFile.readKeyValue(ServerSettingsFileParameters.SERVER_SETTING_CONTROL_DB_PORT);
		s += clsCreateHTMLTableFormFields.Create_Edit_Form_Text_Input_Row(
				ServerSettingsFileParameters.SERVER_SETTING_CONTROL_DB_PORT,
				sControlsDatabasePortValue, 
				40, 
				"<B>Controls Database Port</B>",
				"The port number of the MySQL server that holds the control database",
				"40"
				);
		
		//Controls database username.
		String sControlsDatabaseUsernameValue = readFile.readKeyValue(ServerSettingsFileParameters.SERVER_SETTING_CONTROL_DB_USERNAME);
		s += clsCreateHTMLTableFormFields.Create_Edit_Form_Text_Input_Row(
				ServerSettingsFileParameters.SERVER_SETTING_CONTROL_DB_USERNAME,
				sControlsDatabaseUsernameValue, 
				40, 
				"<B>Controls Database Username</B>",
				"The authorized MySQL user for the control database",
				"40"
				);
		
		//Controls database password
		String sControlsDatabasePasswordValue = readFile.readKeyValue(ServerSettingsFileParameters.SERVER_SETTING_CONTROL_DB_PASSWORD);
		s += clsCreateHTMLTableFormFields.Create_Edit_Form_Text_Input_Row(
				ServerSettingsFileParameters.SERVER_SETTING_CONTROL_DB_PASSWORD,
				sControlsDatabasePasswordValue, 
				40, 
				"<B>Controls Database Password</B>",
				"The authorized MySQL user's password for the control database",
				"40"
				);
		
		//Server ID
		String sServerIDValue = readFile.readKeyValue(ServerSettingsFileParameters.SERVER_SETTING_SERVER_ID);
		s += clsCreateHTMLTableFormFields.Create_Edit_Form_Text_Input_Row(
				ServerSettingsFileParameters.SERVER_SETTING_SERVER_ID,
				sServerIDValue, 
				40, 
				"<B>Server ID</B>",
				"Unique server ID for alarm system authentication.",
				"40"
				);
		
		
		//Test Device ID
		String sTestDeviceID = readFile.readKeyValue(ServerSettingsFileParameters.SERVER_SETTING_TEST_DEVICE_ID);
		s += clsCreateHTMLTableFormFields.Create_Edit_Form_Text_Input_Row(
				ServerSettingsFileParameters.SERVER_SETTING_TEST_DEVICE_ID,
				sTestDeviceID, 
				2, 
				"<B>Alarm System Test Device ID</B>",
				"This allows us to turn on debugging on the fly for a particular alarm system device.",
				"4"
				);
		
		//Server Debugging Level
		String sServerDebuggingValue = readFile.readKeyValue(ServerSettingsFileParameters.SERVER_SETTING_DEBUG_LEVEL);
		ArrayList<String> sValues =  new ArrayList<String>();
		sValues.add("0");
		sValues.add("1");
		sValues.add("2");
		sValues.add("3");	
		ArrayList<String> arrLabels = new ArrayList<String>(0);
		arrLabels.add("0 - No logging");
		arrLabels.add("1 - Minimal logging");
		arrLabels.add("2 - Standard logging");
		arrLabels.add("3 - Maximum logging");
		s += clsCreateHTMLTableFormFields.Create_Edit_Form_List_Row(
				ServerSettingsFileParameters.SERVER_SETTING_DEBUG_LEVEL,
				sValues, 
				sServerDebuggingValue, 
				arrLabels, 
				"<B>Alarm System Debugging Level </B>",
				"Logging verbosity for alarm system."
				);
		
		//Alarm System Scheduler
		String sRunSchedulerValue = readFile.readKeyValue(ServerSettingsFileParameters.SERVER_SETTING_RUN_SCHEDULER);
		int iChecked = 0;
		if(sRunSchedulerValue.trim().compareToIgnoreCase("1") == 0) {
			iChecked = 1;
		}
		s += clsCreateHTMLTableFormFields.Create_Edit_Form_Checkbox_Row(
				ServerSettingsFileParameters.SERVER_SETTING_RUN_SCHEDULER,
				iChecked, 
				"<B>Run Alarm System Scheduler?</B>",
				"Server will poll alarm system schedules every minute. "
				);
		
		
		//Run Appointment Notifications
		String sRunAppointmentNotificationsValue = readFile.readKeyValue(ServerSettingsFileParameters.SERVER_SETTING_RUN_APPOINTMENT_NOTIFICATIONS);
			iChecked = 0;
		if(sRunAppointmentNotificationsValue.trim().compareToIgnoreCase("1") == 0) {
			iChecked = 1;
		}
		s += clsCreateHTMLTableFormFields.Create_Edit_Form_Checkbox_Row(
				ServerSettingsFileParameters.SERVER_SETTING_RUN_APPOINTMENT_NOTIFICATIONS,
				iChecked, 
				"<B>Run Appointment Notifications?</B>",
				"Server will poll appointments for email reminders every minute. "
				);
		
		//Initiating URL
		String sInitiatingURL = readFile.readKeyValue(ServerSettingsFileParameters.SERVER_SETTING_INITIATING_URL);
		s += clsCreateHTMLTableFormFields.Create_Edit_Form_Text_Input_Row(
				ServerSettingsFileParameters.SERVER_SETTING_INITIATING_URL,
				sInitiatingURL, 
				40, 
				"<B>Inititaiting URL</B>",
				"The link to the 'initiating' server, which is used to create quicklinks that can be independent of the 'target' server for the program",
				"40"
				);


		
		s += "</TABLE>";

		return s;
	}

	public void doGet(HttpServletRequest request,
			HttpServletResponse response)
					throws ServletException, IOException {

		doPost(request, response);
	}
}
