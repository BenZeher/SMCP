package SecuritySystem;

import sscommon.SSConstants;
import sscommon.SSUtilities;

public class ServerRequests {

	static String sendRequestString(
		String sWebAppURL, 
		String sPortNumber, 
		String sRequest, 
		boolean bOutputDiagnostics) throws Exception{
		
		//This makes sure that we don't send a request more than once every so many milliseconds:
		while ((System.currentTimeMillis() - SecuritySystem.iRequestLastSentTime) < SSConstants.MINIMUM_REQUEST_INTERVAL){
			Thread.sleep(500);
		}
		SecuritySystem.iRequestLastSentTime = System.currentTimeMillis();
		try {
			ClientService cs = new ClientService(SecuritySystem.getLogFile(), SecuritySystem.getLoggingLevel());
			return cs.sendRequest(
				sWebAppURL,
				Integer.parseInt(sPortNumber),
				sRequest,
				bOutputDiagnostics
			);
		} catch (Exception e) {
			throw new Exception (e.getMessage());
		}
	}

	//Commands to issue TO the server:
	static void notifyOfStartUp() throws Exception{
		String sNotifyServerOfStartupCommand = 
				SSUtilities.buildKeyValuePair(SSConstants.QUERY_KEY_COMMAND, SSConstants.QUERY_KEYVALUE_COMMAND_CONTROLLER_STARTUP);
		String sGetArmedStatusRequest = SSUtilities.buildControllerRequestToServerString(sNotifyServerOfStartupCommand);
		
		try {
			sendRequestString(SecuritySystem.getWebAppURL(), SecuritySystem.getWebAppPortNumber(), sGetArmedStatusRequest, SecuritySystem.getOutputDiagnostics());
		} catch (Exception e) {
			throw new Exception(e.getMessage());
		}
	}
	
	static void requestPinListeningStatus() throws Exception{
		//Request all the terminal listening statuses:
		String sGetPinArmedStatusCommand = 
			SSUtilities.buildKeyValuePair(SSConstants.QUERY_KEY_COMMAND, SSConstants.QUERY_KEYVALUE_COMMAND_GET_LISTENING_STATUS);
		String sGetArmedStatusRequest = SSUtilities.buildControllerRequestToServerString(sGetPinArmedStatusCommand);
		
		String sResponse = "";
		try {
			//TODO - what to do if this returns false?
			sResponse = sendRequestString(SecuritySystem.getWebAppURL(), SecuritySystem.getWebAppPortNumber(), sGetArmedStatusRequest, SecuritySystem.getOutputDiagnostics());
		} catch (Exception e) {
			throw new Exception(e.getMessage());
		}
		
		//Now set the pin listening statuses:
		try {
			ControllerResponses.setInputPinListeningStatus(sResponse);
		} catch (Exception e) {
			throw new Exception(e.getMessage());
		}
	}

	static void requestPinContactStatus() throws Exception{
		//Request the individual pin-active status:
		String sGetPinContactStatusCommand = 
			SSUtilities.buildKeyValuePair(SSConstants.QUERY_KEY_COMMAND, SSConstants.QUERY_KEYVALUE_COMMAND_GET_CONTACT_STATUS);
		;
		String sGetTerminalContactStatusRequest = SSUtilities.buildControllerRequestToServerString(sGetPinContactStatusCommand);
		
		String sResponse = "";
		try {
			sResponse = sendRequestString(SecuritySystem.getWebAppURL(), SecuritySystem.getWebAppPortNumber(), sGetTerminalContactStatusRequest, SecuritySystem.getOutputDiagnostics());
		} catch (Exception e) {
			throw new Exception(e.getMessage());
		}
		
		//Now set the pin contact statuses:
		try {
			ControllerResponses.setOutputTerminalContactStatus(sResponse);
		} catch (Exception e) {
			throw new Exception(e.getMessage());
		}
	}

	static void reportPinTriggerEvent(String sTerminalNumber, String sContactChangeState) throws Exception{
		//Signal to the web app that an input pin was triggered:
		
		String sCommandString = 
			//Command to issue:
			SSUtilities.buildKeyValuePair(SSConstants.QUERY_KEY_COMMAND, SSConstants.QUERY_KEYVALUE_COMMAND_PROCESS_TRIGGER)
			//Pin trigger state:
			+ SSConstants.QUERY_STRING_DELIMITER
			+ SSUtilities.buildKeyValuePair(SSConstants.QUERY_KEY_TERMINALCONTACTSTATECHANGE_PREFIX + sTerminalNumber, sContactChangeState)
		;
				
		String sServerRequest = SSUtilities.buildControllerRequestToServerString(sCommandString);
		
		//If we're outputting diagnostic messages, send this one along:
		if (SecuritySystem.bOutDiagnosticsToCommandLine){
			System.out.println("Received contact change report on terminal " + sTerminalNumber
				+ " - sending string: \n"
				+ sServerRequest
			);
		}
		
		try {
			sendRequestString(SecuritySystem.getWebAppURL(), SecuritySystem.getWebAppPortNumber(), sServerRequest, SecuritySystem.getOutputDiagnostics());
		} catch (Exception e) {
			throw new Exception(e.getMessage());
		}
	}
	
	static void initializeTerminalStatus() throws Exception{
		
		//If there's no Web App URL, then don't try to talk to the web app:
		if (SecuritySystem.getWebAppURL().compareToIgnoreCase("NONE") == 0){
			return;
		}
		
		//Initial input pin status query
		try {
			requestPinListeningStatus();
		} catch (Exception e) {
			throw new Exception(e.getMessage());
		}
		
		//Initial output pin status query:
		try {
			requestPinContactStatus();
		} catch (Exception e) {
			throw new Exception(e.getMessage());
		}
	}

	static void notifyOnStartup() throws Exception{
		
		//If there's no Web App URL, then don't try to talk to the web app:
		if (SecuritySystem.getWebAppURL().compareToIgnoreCase("NONE") == 0){
			return;
		}
		//Initial input pin status query
		try {
			notifyOfStartUp();
		} catch (Exception e) {
			throw new Exception(e.getMessage());
		}
	}

	
	static void checkInWithServer() throws Exception{
		//Periodic check in with the web app to perform timed procedures, etc.
		String sCommandString = 
			//Command to issue:
			SSUtilities.buildKeyValuePair(SSConstants.QUERY_KEY_COMMAND, SSConstants.QUERY_KEYVALUE_COMMAND_CONTROLLER_CHECK_IN)
		;
				
		String sServerRequest = SSUtilities.buildControllerRequestToServerString(sCommandString);
		
		//If we're outputting diagnostic messages, send this one along:
		if (SecuritySystem.bOutDiagnosticsToCommandLine){
			System.out.println("Checking into server"
				+ " - sending string: \n"
				+ sServerRequest
			);
		}
		
		try {
			sendRequestString(SecuritySystem.getWebAppURL(), SecuritySystem.getWebAppPortNumber(), sServerRequest, SecuritySystem.getOutputDiagnostics());
		} catch (Exception e) {
			throw new Exception(e.getMessage());
		}
	}
	
	//Trigger event handler:
	public static void triggerEventHandler(String sTerminalNumber, String sTriggerType){
		if (SecuritySystem.bOutDiagnosticsToCommandLine){
			System.out.println("Input sensor terminal number " + sTerminalNumber + " was triggered.");
		}
		
		//If we are in 'TEST' mode, just activate the output contacts of a specified output..
		if (SecuritySystem.getTestModeRunning()){
			//Get the terminal number of the 'associated' output to know which output terminal to trigger:
			String sOutputTerminal = Integer.toString(Integer.parseInt(sTerminalNumber) 
				+ SecuritySystem.getGPIOHandler().getNumberOfInputTerminals());
			try {
				SecuritySystem.getGPIOHandler().setOutputTerminalContactStatus(
					sOutputTerminal, true, Long.parseLong(SSConstants.DEFAULT_MOMENTARY_CONTACT_ACTIVATE_INTERVAL_IN_MS));
			} catch (NumberFormatException e) {
				if (SecuritySystem.bOutDiagnosticsToCommandLine){
					System.out.println("Number format exception [1464357240] setting output contact status in test mode - " + e.getMessage());
				}
			} catch (Exception e) {
				if (SecuritySystem.bOutDiagnosticsToCommandLine){
					System.out.println("Error [1464357241] setting output contact status in test mode - " + e.getMessage());
				}
			}
		}else{
		//If we are NOT in test mode, then send the trigger on to the web app:
			try {
				reportPinTriggerEvent(sTerminalNumber, sTriggerType);
			} catch (Exception e) {
				//TODO - decide how we need to handle a failed trigger report to the server:
			}
		}
	}

}
