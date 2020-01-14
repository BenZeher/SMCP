package SecuritySystem;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import sscommon.SSConstants;
import sscommon.SSUtilities;

public class ControllerResponses {

	public static String processInputFromServer(String sRequestString) throws Exception{
		
		String sResult = "";
		try {
			SSUtilities.authenticateRequest(sRequestString);
		} catch (Exception e1) {
			throw new Exception(e1.getMessage());
		}
		
		//Now get the command:
		String sCommand = SSUtilities.getKeyValue(sRequestString, SSConstants.QUERY_KEY_COMMAND);
		//Now process each command:
		
		//Set input pin listening status:
		if (sCommand.compareToIgnoreCase(SSConstants.QUERY_KEYVALUE_COMMAND_SET_TERMINAL_LISTENING_STATUS) == 0){
			try {
				ControllerResponses.setInputPinListeningStatus(sRequestString);
			} catch (Exception e) {
				//Failed to complete the command - return the error:
				return SSUtilities.buildKeyValuePair(
						SSConstants.QUERY_KEY_ACKNOWLEDGMENT, SSConstants.QUERY_KEYVALUE_ACKNOWLEDGMENT_UNSUCCESSFUL)
					+ SSConstants.QUERY_STRING_DELIMITER
					+ SSUtilities.buildKeyValuePair(
						SSConstants.QUERY_KEY_REQUEST_ERROR, e.getMessage())
				;
			}
			//Completed successfully - acknowledge:
			return SSUtilities.buildKeyValuePair(SSConstants.QUERY_KEY_ACKNOWLEDGMENT, SSConstants.QUERY_KEYVALUE_ACKNOWLEDGMENT_SUCCESSFUL);
		}
		
		//List input terminals listening status - are they listening or not?:
		if (sCommand.compareToIgnoreCase(SSConstants.QUERY_KEYVALUE_COMMAND_GET_LISTENING_STATUS) == 0){
			try {
				sResult = ControllerResponses.getInputTerminalListeningStatus(sRequestString);
			} catch (Exception e) {
				//Failed to complete the command - return the error:
				return SSUtilities.buildKeyValuePair(
						SSConstants.QUERY_KEY_ACKNOWLEDGMENT, SSConstants.QUERY_KEYVALUE_ACKNOWLEDGMENT_UNSUCCESSFUL)
					+ SSConstants.QUERY_STRING_DELIMITER
					+ SSUtilities.buildKeyValuePair(
						SSConstants.QUERY_KEY_REQUEST_ERROR, e.getMessage())
				;
			}
			return SSUtilities.buildKeyValuePair(SSConstants.QUERY_KEY_ACKNOWLEDGMENT, SSConstants.QUERY_KEYVALUE_ACKNOWLEDGMENT_SUCCESSFUL)
				+ SSConstants.QUERY_STRING_DELIMITER
				+ sResult
			;
		}
		
		//Set output pin contact status:
		if (sCommand.compareToIgnoreCase(SSConstants.QUERY_KEYVALUE_COMMAND_SET_TERMINAL_CONTACT_STATUS) == 0){
			try {
				ControllerResponses.setOutputTerminalContactStatus(sRequestString);
			} catch (Exception e) {
				//Failed to complete the command - return the error:
				return SSUtilities.buildKeyValuePair(
						SSConstants.QUERY_KEY_ACKNOWLEDGMENT, SSConstants.QUERY_KEYVALUE_ACKNOWLEDGMENT_UNSUCCESSFUL)
					+ SSConstants.QUERY_STRING_DELIMITER
					+ SSUtilities.buildKeyValuePair(
						SSConstants.QUERY_KEY_REQUEST_ERROR, e.getMessage())
				;
			}
			//Completed successfully - acknowledge:
			return SSUtilities.buildKeyValuePair(SSConstants.QUERY_KEY_ACKNOWLEDGMENT, SSConstants.QUERY_KEYVALUE_ACKNOWLEDGMENT_SUCCESSFUL);
		}
		
		//List terminals contact status - are they closed or open?:
		if (sCommand.compareToIgnoreCase(SSConstants.QUERY_KEYVALUE_COMMAND_LISTING_TERMINAL_CONTACT_STATUS) == 0){
			try {
				sResult = ControllerResponses.getTerminalContactStatus(sRequestString);
			} catch (Exception e) {
				//Failed to complete the command - return the error:
				return SSUtilities.buildKeyValuePair(
						SSConstants.QUERY_KEY_ACKNOWLEDGMENT, SSConstants.QUERY_KEYVALUE_ACKNOWLEDGMENT_UNSUCCESSFUL)
					+ SSConstants.QUERY_STRING_DELIMITER
					+ SSUtilities.buildKeyValuePair(
						SSConstants.QUERY_KEY_REQUEST_ERROR, e.getMessage())
				;
			}
			return SSUtilities.buildKeyValuePair(SSConstants.QUERY_KEY_ACKNOWLEDGMENT, SSConstants.QUERY_KEYVALUE_ACKNOWLEDGMENT_SUCCESSFUL)
				+ SSConstants.QUERY_STRING_DELIMITER
				+ sResult
			;
		}
		
		//Get the output terminals remaining intervals:
		//List terminals contact status - are they closed or open?:
		
		// TJR - 7/13/2016 - stopped using this since we began using the 'pulse' function again:
		/*
		if (sCommand.compareToIgnoreCase(SSConstants.QUERY_KEYVALUE_COMMAND_GET_REMAINING_INTERVAL) == 0){
			try {
				sResult = ControllerResponses.getOutputTerminalRemainingInterval(sRequestString);
			} catch (Exception e) {
				//Failed to complete the command - return the error:
				return SSUtilities.buildKeyValuePair(
						SSConstants.QUERY_KEY_ACKNOWLEDGMENT, SSConstants.QUERY_KEYVALUE_ACKNOWLEDGMENT_UNSUCCESSFUL)
					+ SSConstants.QUERY_STRING_DELIMITER
					+ SSUtilities.buildKeyValuePair(
						SSConstants.QUERY_KEY_REQUEST_ERROR, e.getMessage())
				;
			}
			return SSUtilities.buildKeyValuePair(SSConstants.QUERY_KEY_ACKNOWLEDGMENT, SSConstants.QUERY_KEYVALUE_ACKNOWLEDGMENT_SUCCESSFUL)
				+ SSConstants.QUERY_STRING_DELIMITER
				+ sResult
			;
		}
		*/
		//Delete the log file:
		if (sCommand.compareToIgnoreCase(SSConstants.QUERY_KEYVALUE_COMMAND_DELETE_LOG_FILE) == 0){
			try {
				SSUtilities.deleteLogFile(SecuritySystem.getLogFile());
			} catch (Exception e) {
				return SSUtilities.buildKeyValuePair(
						SSConstants.QUERY_KEY_ACKNOWLEDGMENT, SSConstants.QUERY_KEYVALUE_ACKNOWLEDGMENT_UNSUCCESSFUL)
					+ SSConstants.QUERY_STRING_DELIMITER
					+ SSUtilities.buildKeyValuePair(
						SSConstants.QUERY_KEY_REQUEST_ERROR, e.getMessage())
				;
			}
			return SSUtilities.buildKeyValuePair(SSConstants.QUERY_KEY_ACKNOWLEDGMENT, SSConstants.QUERY_KEYVALUE_ACKNOWLEDGMENT_SUCCESSFUL);
		}
		
		//Report the current system information:
		String sHostName = SSUtilities.executeSystemCommand("hostname");
		if (sCommand.compareToIgnoreCase(SSConstants.QUERY_KEYVALUE_COMMAND_REPORT_SYSTEMINFORMATION) == 0){
			return SSUtilities.buildKeyValuePair(
				SSConstants.QUERY_KEY_ACKNOWLEDGMENT, SSConstants.QUERY_KEYVALUE_ACKNOWLEDGMENT_SUCCESSFUL)
				+ SSConstants.QUERY_STRING_DELIMITER
				+ SSUtilities.buildKeyValuePair(SSConstants.QUERY_KEY_VERSION, SSConstants.CONTROLLER_VERSION)
				+ SSConstants.QUERY_STRING_DELIMITER
				+ SSUtilities.buildKeyValuePair(SSConstants.QUERY_KEY_CONTROLLER_HOSTNAME, sHostName)
			;
		}
		
		//Send the log up to the server:
		if (sCommand.compareToIgnoreCase(SSConstants.QUERY_KEYVALUE_COMMAND_SEND_LOG) == 0){
			try {
				sResult = ControllerResponses.getLogFileContents(sRequestString);
			} catch (Exception e) {
				//Failed to complete the command - return the error:
				return SSUtilities.buildKeyValuePair(
						SSConstants.QUERY_KEY_ACKNOWLEDGMENT, SSConstants.QUERY_KEYVALUE_ACKNOWLEDGMENT_UNSUCCESSFUL)
					+ SSConstants.QUERY_STRING_DELIMITER
					+ SSUtilities.buildKeyValuePair(
						SSConstants.QUERY_KEY_REQUEST_ERROR, e.getMessage())
				;
			}
			return SSUtilities.buildKeyValuePair(SSConstants.QUERY_KEY_ACKNOWLEDGMENT, SSConstants.QUERY_KEYVALUE_ACKNOWLEDGMENT_SUCCESSFUL)
				+ SSConstants.QUERY_STRING_DELIMITER
				//This is a special case - we don't use the 'build key' function because it replaces ampersands and equals signs,
				//and we want the log going back without any filtering:
				+ SSConstants.QUERY_KEY_LOGFILE_CONTENTS + SSConstants.QUERY_STRING_EQUALS_SYMBOL + sResult
			;
		}
		
		//Send the config file up to the server:
		if (sCommand.compareToIgnoreCase(SSConstants.QUERY_KEYVALUE_COMMAND_SEND_CONFIG_FILE) == 0){
			try {
				sResult = ControllerResponses.getConfigFileContents(sRequestString);
			} catch (Exception e) {
				//Failed to complete the command - return the error:
				return SSUtilities.buildKeyValuePair(
						SSConstants.QUERY_KEY_ACKNOWLEDGMENT, SSConstants.QUERY_KEYVALUE_ACKNOWLEDGMENT_UNSUCCESSFUL)
					+ SSConstants.QUERY_STRING_DELIMITER
					+ SSUtilities.buildKeyValuePair(
						SSConstants.QUERY_KEY_REQUEST_ERROR, e.getMessage())
				;
			}
			return SSUtilities.buildKeyValuePair(SSConstants.QUERY_KEY_ACKNOWLEDGMENT, SSConstants.QUERY_KEYVALUE_ACKNOWLEDGMENT_SUCCESSFUL)
				+ SSConstants.QUERY_STRING_DELIMITER
				+ SSUtilities.buildKeyValuePair(SSConstants.QUERY_KEY_CONFIGFILE_CONTENTS, sResult)
			;
		}
		
		//Update the controller's config file:
		if (sCommand.compareToIgnoreCase(SSConstants.QUERY_KEYVALUE_COMMAND_UPDATE_CONFIG_FILE) == 0){
			try {
				ControllerResponses.updateConfigFile(sRequestString);
			} catch (Exception e) {
				//Failed to complete the command - return the error:
				return SSUtilities.buildKeyValuePair(
						SSConstants.QUERY_KEY_ACKNOWLEDGMENT, SSConstants.QUERY_KEYVALUE_ACKNOWLEDGMENT_UNSUCCESSFUL)
					+ SSConstants.QUERY_STRING_DELIMITER
					+ SSUtilities.buildKeyValuePair(
						SSConstants.QUERY_KEY_REQUEST_ERROR, e.getMessage())
				;
			}
			return SSUtilities.buildKeyValuePair(SSConstants.QUERY_KEY_ACKNOWLEDGMENT, SSConstants.QUERY_KEYVALUE_ACKNOWLEDGMENT_SUCCESSFUL);
		}
		
		//Update the controller software:
		if (sCommand.compareToIgnoreCase(SSConstants.QUERY_KEYVALUE_COMMAND_UPDATE_PROGRAM) == 0){
			try {
				ControllerResponses.updateControllerSoftware(sRequestString, SecuritySystem.getWebAppURL(), SecuritySystem.getWebAppPortNumber());
			} catch (Exception e) {
				//Failed to complete the command - return the error:
				return SSUtilities.buildKeyValuePair(
						SSConstants.QUERY_KEY_ACKNOWLEDGMENT, SSConstants.QUERY_KEYVALUE_ACKNOWLEDGMENT_UNSUCCESSFUL)
					+ SSConstants.QUERY_STRING_DELIMITER
					+ SSUtilities.buildKeyValuePair(
						SSConstants.QUERY_KEY_REQUEST_ERROR, e.getMessage())
				;
			}
			SecuritySystem.setRestartFlag(true);
			return SSUtilities.buildKeyValuePair(SSConstants.QUERY_KEY_ACKNOWLEDGMENT, SSConstants.QUERY_KEYVALUE_ACKNOWLEDGMENT_SUCCESSFUL);
		}
		
		//Send the sample commands up to the server:
		if (sCommand.compareToIgnoreCase(SSConstants.QUERY_KEYVALUE_COMMAND_LIST_SAMPLE_TELNET_COMMANDS) == 0){
			try {
				sResult = ControllerResponses.getSampleTelnetCommands(sRequestString);
			} catch (Exception e) {
				//Failed to complete the command - return the error:
				return SSUtilities.buildKeyValuePair(
						SSConstants.QUERY_KEY_ACKNOWLEDGMENT, SSConstants.QUERY_KEYVALUE_ACKNOWLEDGMENT_UNSUCCESSFUL)
					+ SSConstants.QUERY_STRING_DELIMITER
					+ SSUtilities.buildKeyValuePair(
						SSConstants.QUERY_KEY_REQUEST_ERROR, e.getMessage())
				;
			}
			return SSUtilities.buildKeyValuePair(SSConstants.QUERY_KEY_ACKNOWLEDGMENT, SSConstants.QUERY_KEYVALUE_ACKNOWLEDGMENT_SUCCESSFUL)
				+ SSConstants.QUERY_STRING_DELIMITER
				//This is a special case - we don't use the 'build key' function because it replaces ampersands and equals signs,
				//and we want the log going back without any filtering:
				+ SSConstants.QUERY_KEY_SAMPLE_COMMAND_CONTENTS + SSConstants.QUERY_STRING_EQUALS_SYMBOL + sResult
			;
		}
		
		//Send the pin mappings up to the server:
		if (sCommand.compareToIgnoreCase(SSConstants.QUERY_KEYVALUE_COMMAND_LIST_PIN_MAPPINGS) == 0){
			try {
				sResult = ControllerResponses.getPinMappings(sRequestString);
			} catch (Exception e) {
				//Failed to complete the command - return the error:
				return SSUtilities.buildKeyValuePair(
						SSConstants.QUERY_KEY_ACKNOWLEDGMENT, SSConstants.QUERY_KEYVALUE_ACKNOWLEDGMENT_UNSUCCESSFUL)
					+ SSConstants.QUERY_STRING_DELIMITER
					+ SSUtilities.buildKeyValuePair(
						SSConstants.QUERY_KEY_REQUEST_ERROR, e.getMessage())
				;
			}
			return SSUtilities.buildKeyValuePair(SSConstants.QUERY_KEY_ACKNOWLEDGMENT, SSConstants.QUERY_KEYVALUE_ACKNOWLEDGMENT_SUCCESSFUL)
				+ SSConstants.QUERY_STRING_DELIMITER
				//This is a special case - we don't use the 'build key' function because it replaces ampersands and equals signs,
				//and we want the log going back without any filtering:
				+ SSConstants.QUERY_KEY_PIN_MAPPING_LIST + SSConstants.QUERY_STRING_EQUALS_SYMBOL + sResult
			;
		}
		
		//Toggle the controller into or out of 'TEST' mode:
		if (sCommand.compareToIgnoreCase(SSConstants.QUERY_KEYVALUE_COMMAND_SET_TEST_MODE) == 0){
			try {
				ControllerResponses.setTestMode(sRequestString);
			} catch (Exception e) {
				//Failed to complete the command - return the error:
				return SSUtilities.buildKeyValuePair(
						SSConstants.QUERY_KEY_ACKNOWLEDGMENT, SSConstants.QUERY_KEYVALUE_ACKNOWLEDGMENT_UNSUCCESSFUL)
					+ SSConstants.QUERY_STRING_DELIMITER
					+ SSUtilities.buildKeyValuePair(
						SSConstants.QUERY_KEY_REQUEST_ERROR, e.getMessage())
				;
			}
			return SSUtilities.buildKeyValuePair(SSConstants.QUERY_KEY_ACKNOWLEDGMENT, SSConstants.QUERY_KEYVALUE_ACKNOWLEDGMENT_SUCCESSFUL)
				+ SSConstants.QUERY_STRING_DELIMITER
				+ sResult
			;
		}
		
		//Get the current test mode state:
		if (sCommand.compareToIgnoreCase(SSConstants.QUERY_KEYVALUE_COMMAND_GET_TEST_MODE) == 0){
			try {
				sResult = ControllerResponses.getTestMode(sRequestString);
			} catch (Exception e) {
				//Failed to complete the command - return the error:
				return SSUtilities.buildKeyValuePair(
						SSConstants.QUERY_KEY_ACKNOWLEDGMENT, SSConstants.QUERY_KEYVALUE_ACKNOWLEDGMENT_UNSUCCESSFUL)
					+ SSConstants.QUERY_STRING_DELIMITER
					+ SSUtilities.buildKeyValuePair(
						SSConstants.QUERY_KEY_REQUEST_ERROR, e.getMessage())
				;
			}
			return SSUtilities.buildKeyValuePair(SSConstants.QUERY_KEY_ACKNOWLEDGMENT, SSConstants.QUERY_KEYVALUE_ACKNOWLEDGMENT_SUCCESSFUL)
				+ SSConstants.QUERY_STRING_DELIMITER
				+ SSUtilities.buildKeyValuePair(SSConstants.QUERY_KEY_TEST_MODE_STATE, sResult)
			;
		}
		
		//Terminate the program:
		if (sCommand.compareToIgnoreCase(SSConstants.QUERY_KEYVALUE_COMMAND_TERMINATE) == 0){
			SecuritySystem.triggerShutdown(
				"Program terminated program through listener connection by " 
					+ SSUtilities.getKeyValue(sRequestString, SSConstants.QUERY_KEY_USER_ID)
					+ " - "
					+ SSUtilities.getKeyValue(sRequestString, SSConstants.QUERY_KEY_USER_FULL_NAME),
					SSConstants.EXIT_NORMAL);
			return SSUtilities.buildKeyValuePair(SSConstants.QUERY_KEY_ACKNOWLEDGMENT, SSConstants.QUERY_KEYVALUE_ACKNOWLEDGMENT_SUCCESSFUL);
		}
		
		//Restart the program:
		if (sCommand.compareToIgnoreCase(SSConstants.QUERY_KEYVALUE_COMMAND_RESTART) == 0){
			SecuritySystem.setRestartFlag(true);
			return SSUtilities.buildKeyValuePair(SSConstants.QUERY_KEY_ACKNOWLEDGMENT, SSConstants.QUERY_KEYVALUE_ACKNOWLEDGMENT_SUCCESSFUL);
		}
		
		//*******************************************************************************************************************
		//DIAGNOSTIC COMMANDS - USED ONLY FOR TESTING:
		
		//From a telnet prompt, FAKE set the contact status of input terminals:
		if (sCommand.compareToIgnoreCase(SSConstants.QUERY_KEYVALUE_FAKE_SET_INPUT_CONTACT) == 0){
			try {
				ControllerResponses.setFakeInputTerminalContactStatus(sRequestString);
			} catch (Exception e) {
				//Failed to complete the command - return the error:
				return SSUtilities.buildKeyValuePair(
						SSConstants.QUERY_KEY_ACKNOWLEDGMENT, SSConstants.QUERY_KEYVALUE_ACKNOWLEDGMENT_UNSUCCESSFUL)
					+ SSConstants.QUERY_STRING_DELIMITER
					+ SSUtilities.buildKeyValuePair(
						SSConstants.QUERY_KEY_REQUEST_ERROR, e.getMessage())
				;
			}
			//Completed successfully - acknowledge:
			return SSUtilities.buildKeyValuePair(SSConstants.QUERY_KEY_ACKNOWLEDGMENT, SSConstants.QUERY_KEYVALUE_ACKNOWLEDGMENT_SUCCESSFUL);
		}
		//*********************************************************************************************************************************
		
		//Unrecognized command:
		//Failed to complete the command - return the error:
		return SSUtilities.buildKeyValuePair(
				SSConstants.QUERY_KEY_ACKNOWLEDGMENT, SSConstants.QUERY_KEYVALUE_ACKNOWLEDGMENT_UNSUCCESSFUL)
			+ SSConstants.QUERY_STRING_DELIMITER
			+ SSUtilities.buildKeyValuePair(
				SSConstants.QUERY_KEY_REQUEST_ERROR, "Unrecognized command string")
		;
	}

	//********************************************************************************************************************************
	//Commands understood by the controller:
	static void setInputPinListeningStatus(String sRequestString) throws Exception{
	
		String sDiagnosticMessage = "[1579025544] Setting input terminal listening status: ";
		
		String sKeyValuePairs[] = SSUtilities.getKeyValuePairsList(sRequestString);
		
		SecuritySystem.writeControllerLogEntry(
			"Request to set listening status sent by "
			+ SSUtilities.getKeyValue(sRequestString, SSConstants.QUERY_KEY_USER_ID)
			+ " - "
			+ SSUtilities.getKeyValue(sRequestString, SSConstants.QUERY_KEY_USER_FULL_NAME),
			1);
		
		for (int i = 0; i < sKeyValuePairs.length; i++){
			//Pick off each pin and set its listening state:
			String sKey = SSUtilities.getKeyFromRequestList(sKeyValuePairs, i);
			if (sKey.startsWith(SSConstants.QUERY_KEY_TERMINALLISTENINGSTATE_PREFIX)){
				String sTerminalNumber = sKey.substring(SSConstants.QUERY_KEY_TERMINALLISTENINGSTATE_PREFIX.length(), sKey.length());
				String sListeningState = SSUtilities.getKeyValue(sRequestString, sKey);
				boolean bListening = false;
				if (sListeningState.compareToIgnoreCase(SSConstants.QUERY_KEYVALUE_TERMINAL_LISTENING_STATUS_LISTENING) == 0){
					bListening = true;
				}
				try {
					SecuritySystem.getGPIOHandler().setInputTerminalListeningStatus(sTerminalNumber, bListening);
				} catch (Exception e) {
					throw new Exception("Error [1459799989] - could not set terminal " + sTerminalNumber 
						+ " to listening status " + sListeningState + " - " + e.getMessage());
				}
				sDiagnosticMessage+= " Set terminal " + sTerminalNumber + " to " + sListeningState + ",";
			}
		}
		
		if(SecuritySystem.bFakeAllGPIOCalls || SecuritySystem.bOutDiagnosticsToCommandLine){
			System.out.println(sDiagnosticMessage);
		}
	}

	private static String getInputTerminalListeningStatus(String sRequestString) throws Exception{
		
		//We just need to create the string of pin active/inactive values and pass it back.
		//The calling function adds the acknowledgment to the response for us.
		String sDiagnosticMessage = "[1579025610] Getting input terminal listening status: ";
		SecuritySystem.writeControllerLogEntry(
				"Request to get listening status sent by "
				+ SSUtilities.getKeyValue(sRequestString, SSConstants.QUERY_KEY_USER_ID)
				+ " - "
				+ SSUtilities.getKeyValue(sRequestString, SSConstants.QUERY_KEY_USER_FULL_NAME),
				1);
		String sResult = "";
		System.out.println("[1463577439] - SecuritySystem.getGPIOHandler().getNumberOfInputTerminals() = " + SecuritySystem.getGPIOHandler().getNumberOfInputTerminals());
		for(int i = 0; i < SecuritySystem.getGPIOHandler().getNumberOfInputTerminals(); i++){
			String sInputTerminalListeningState = SSConstants.QUERY_KEYVALUE_TERMINAL_LISTENING_STATUS_NOTLISTENING;
			SecuritySystem.getGPIOHandler();
			if (gpioHandler.getInputTerminalListeningStatus(SecuritySystem.getGPIOHandler().getInputTerminalNumber(i))){
				sInputTerminalListeningState = SSConstants.QUERY_KEYVALUE_TERMINAL_LISTENING_STATUS_LISTENING;
			}
			if (sResult.compareToIgnoreCase("") != 0){
				sResult += SSConstants.QUERY_STRING_DELIMITER;
			}
				sResult += SSUtilities.buildKeyValuePair(SSConstants.QUERY_KEY_TERMINALLISTENINGSTATE_PREFIX 
					+ SecuritySystem.getGPIOHandler().getInputTerminalNumber(i),
					sInputTerminalListeningState
				);
		}
						
		if(SecuritySystem.bFakeAllGPIOCalls || SecuritySystem.bOutDiagnosticsToCommandLine){
			System.out.println(sDiagnosticMessage);
		}
		return sResult;
		
	}
	
	private static String getTerminalContactStatus(String sRequestString) throws Exception{
	
		//We just need to create the string of pin active/inactive values and pass it back.
		//The calling function adds the acknowledgment to the response for us.
		String sDiagnosticMessage = "[1579025610] Getting terminal activated status: ";
		SecuritySystem.writeControllerLogEntry(
				"Request to get contact status sent by "
				+ SSUtilities.getKeyValue(sRequestString, SSConstants.QUERY_KEY_USER_ID)
				+ " - "
				+ SSUtilities.getKeyValue(sRequestString, SSConstants.QUERY_KEY_USER_FULL_NAME),
				1);
		String sResult = "";
		for(int i = 0; i < SecuritySystem.getGPIOHandler().getNumberOfInputTerminals(); i++){
			
			String sInputTerminalContactState = SSConstants.QUERY_KEYVALUE_TERMINAL_CONTACT_STATUS_OPEN;
			if (!SecuritySystem.getGPIOHandler().areInputTerminalContactsOpen(SecuritySystem.getGPIOHandler().getInputTerminalNumber(i))){
				sInputTerminalContactState = SSConstants.QUERY_KEYVALUE_TERMINAL_CONTACT_STATUS_CLOSED;
			}
			if (sResult.compareToIgnoreCase("") != 0){
				sResult += SSConstants.QUERY_STRING_DELIMITER;
			}
				sResult += SSUtilities.buildKeyValuePair(SSConstants.QUERY_KEY_TERMINALCONTACTSTATE_PREFIX 
					+ SecuritySystem.getGPIOHandler().getInputTerminalNumber(i),
					sInputTerminalContactState
				);
		}
				
		//Get the status of the OUTPUT terminals:
		for(int i = 0; i < SecuritySystem.getGPIOHandler().getNumberOfOutputTerminals(); i++){
			
			String sOutputTerminalContactState = SSConstants.QUERY_KEYVALUE_TERMINAL_CONTACT_STATUS_OPEN;
			if (!SecuritySystem.getGPIOHandler().areOutputTerminalContactsOpen(SecuritySystem.getGPIOHandler().getOutputTerminalNumber(i))){
				sOutputTerminalContactState = SSConstants.QUERY_KEYVALUE_TERMINAL_CONTACT_STATUS_CLOSED;
			}
			if (sResult.compareToIgnoreCase("") != 0){
				sResult += SSConstants.QUERY_STRING_DELIMITER;
			}
				sResult += SSUtilities.buildKeyValuePair(SSConstants.QUERY_KEY_TERMINALCONTACTSTATE_PREFIX 
					+ SecuritySystem.getGPIOHandler().getOutputTerminalNumber(i),
					sOutputTerminalContactState
				);
		}
		
		if(SecuritySystem.bFakeAllGPIOCalls || SecuritySystem.bOutDiagnosticsToCommandLine){
			System.out.println(sDiagnosticMessage);
		}
		return sResult;
		
	}

	static void setOutputTerminalContactStatus(String sRequestString) throws Exception{
		String sDiagnosticMessage = "[1579025610] Setting output pin active status: ";
		SecuritySystem.writeControllerLogEntry(
				"Request to output terminal active status sent by "
				+ SSUtilities.getKeyValue(sRequestString, SSConstants.QUERY_KEY_USER_ID)
				+ " - "
				+ SSUtilities.getKeyValue(sRequestString, SSConstants.QUERY_KEY_USER_FULL_NAME),
				1);
		String sKeyValuePairs[] = SSUtilities.getKeyValuePairsList(sRequestString);
		
		for (int i = 0; i < sKeyValuePairs.length; i++){
			//Pick off each pin and set its active state:
			String sKey = SSUtilities.getKeyFromRequestList(sKeyValuePairs, i);
			if (sKey.startsWith(SSConstants.QUERY_KEY_TERMINALCONTACTSTATE_PREFIX)){
				String sTerminalNumber = sKey.substring(SSConstants.QUERY_KEY_TERMINALCONTACTSTATE_PREFIX.length(), sKey.length());
				String sContactState = SSUtilities.getKeyValue(sRequestString, sKey);
				boolean bContactsClosed = false;
				if (sContactState.compareToIgnoreCase(SSConstants.QUERY_KEYVALUE_TERMINAL_CONTACT_STATUS_CLOSED) == 0){
					bContactsClosed = true;
				}
				//Now get the interval for that pin:
				String sInterval = SSUtilities.getKeyValue(sRequestString, SSConstants.QUERY_KEY_TERMINALDURATIONINMS_PREFIX + sTerminalNumber);
				long lInterval;
				try {
					lInterval = Long.parseLong(sInterval);
				} catch (Exception e1) {
					throw new Exception("Error [1459801182] could not parse integer - " + e1.getMessage());
				}
				try {
					SecuritySystem.getGPIOHandler().setOutputTerminalContactStatus(sTerminalNumber, bContactsClosed, lInterval);
				} catch (Exception e) {
					throw new Exception("Error [1459799089] - could not set terminal output " + sTerminalNumber 
						+ " to active status " + sContactState + " - " + e.getMessage());
				}
				sDiagnosticMessage+= " Set terminal " + sTerminalNumber + " to " + sContactState + " - interval = " + sInterval + ",";
			}
		}
		if(SecuritySystem.bFakeAllGPIOCalls || SecuritySystem.bOutDiagnosticsToCommandLine){
			System.out.println(sDiagnosticMessage);
		}
		return;
	}
	
	// TJR - 7/13/1026 - removed this since we started using the 'pulse' function again:
	/*
	private static String getOutputTerminalRemainingInterval(String sRequestString) throws Exception{
		
		String sDiagnosticMessage = "[1579025610] Getting output terminal remaining interval: ";
		SecuritySystem.writeControllerLogEntry(
				"Request to get output terminal remaining duration sent by "
				+ SSUtilities.getKeyValue(sRequestString, SSConstants.QUERY_KEY_USER_NAME)
				+ " - "
				+ SSUtilities.getKeyValue(sRequestString, SSConstants.QUERY_KEY_USER_FULL_NAME),
				1);
		String sResult = "";
				
		//Get the status of the OUTPUT terminals:
		for(int i = 0; i < SecuritySystem.getGPIOHandler().getNumberOfOutputTerminals(); i++){
			if (sResult.compareToIgnoreCase("") != 0){
				sResult += SSConstants.QUERY_STRING_DELIMITER;
			}
				sResult += SSUtilities.buildKeyValuePair(SSConstants.QUERY_KEY_TERMINALREMAININGINTERVAL_PREFIX 
					+ SecuritySystem.getGPIOHandler().getOutputTerminalNumber(i),
					Long.toString(SecuritySystem.getGPIOHandler().getOutputTerminalIntervalRemaining(
						SecuritySystem.getGPIOHandler().getOutputTerminalNumber(i)))
				);
		}
		
		if(SecuritySystem.bFakeAllGPIOCalls || SecuritySystem.bOutDiagnosticsToCommandLine){
			System.out.println(sDiagnosticMessage);
		}
		return sResult;
	}
	*/
	private static String getLogFileContents(String sRequestString) throws Exception{
		
		String sDiagnosticMessage = "[1579025610] Getting contents of log file: ";
		SecuritySystem.writeControllerLogEntry(
				"Request to get log file contents sent by "
				+ SSUtilities.getKeyValue(sRequestString, SSConstants.QUERY_KEY_USER_ID)
				+ " - "
				+ SSUtilities.getKeyValue(sRequestString, SSConstants.QUERY_KEY_USER_FULL_NAME),
				1);
		String sResult = "";
		
		//Read the log file into a buffer and return it:
		String sLogFileName = SecuritySystem.getLogFile();
		if (sLogFileName.compareToIgnoreCase("") == 0){
			return sResult;
		}
		try {
			File file = new File(sLogFileName);
			FileReader fileReader = new FileReader(file);
			BufferedReader bufferedReader = new BufferedReader(fileReader);
			String line;
			//Using a buffer line limit makes the appends faster with a long file:
			int iBufferLineLimit = 100;
			int iLineCounter = 0;
			String sBuffer = "";
			while ((line = bufferedReader.readLine()) != null) {
				sBuffer +=  line + "\n";
				if (iLineCounter == iBufferLineLimit){
					sResult += sBuffer;
					sBuffer = "";
				}
				iLineCounter++;
			}
			fileReader.close();
			//Get the remaining lines:
			sResult += sBuffer;
		} catch (IOException e) {
			throw new Exception("Error [1463586661] reading log file '" + sLogFileName + "' - " + e.getMessage());
		}		
		if(SecuritySystem.bFakeAllGPIOCalls || SecuritySystem.bOutDiagnosticsToCommandLine){
			System.out.println(sDiagnosticMessage);
		}
		return sResult;
	}

	private static String getSampleTelnetCommands(String sRequestString) throws Exception{
		
		String sDiagnosticMessage = "[1579025610] Getting list of sample telnet commands: ";
		SecuritySystem.writeControllerLogEntry(
				"Request to get list of sample telnet commands sent by "
				+ SSUtilities.getKeyValue(sRequestString, SSConstants.QUERY_KEY_USER_ID)
				+ " - "
				+ SSUtilities.getKeyValue(sRequestString, SSConstants.QUERY_KEY_USER_FULL_NAME),
				1);
		String sResult = SSUtilities.printSampleCommands();
		if(SecuritySystem.bFakeAllGPIOCalls || SecuritySystem.bOutDiagnosticsToCommandLine){
			System.out.println(sDiagnosticMessage);
		}
		return sResult;
	}
	
	private static String getPinMappings(String sRequestString) throws Exception{
		
		String sDiagnosticMessage = "[1579025663] Getting list of Pi pin to terminal mappings: ";
		SecuritySystem.writeControllerLogEntry(
				"Request to get list of pin to terminal mappings "
				+ SSUtilities.getKeyValue(sRequestString, SSConstants.QUERY_KEY_USER_ID)
				+ " - "
				+ SSUtilities.getKeyValue(sRequestString, SSConstants.QUERY_KEY_USER_FULL_NAME),
				1);
		String sResult = SSUtilities.printPinToTerminalMappings(true);
		if(SecuritySystem.bFakeAllGPIOCalls || SecuritySystem.bOutDiagnosticsToCommandLine){
			System.out.println(sDiagnosticMessage);
		}
		return sResult;
	}

	private static String getConfigFileContents(String sRequestString) throws Exception{
		
		String sDiagnosticMessage = "[1579025666] Getting contents of config file: ";
		SecuritySystem.writeControllerLogEntry(
				"Request to get config file sent by "
				+ SSUtilities.getKeyValue(sRequestString, SSConstants.QUERY_KEY_USER_ID)
				+ " - "
				+ SSUtilities.getKeyValue(sRequestString, SSConstants.QUERY_KEY_USER_FULL_NAME),
				1);
		String sResult = "";
		
		//Read the config file into a buffer and return it:
		String sConfigFileName = SecuritySystem.getConfigFileFullName();
		if (sConfigFileName.compareToIgnoreCase("") == 0){
			return sResult;
		}
		try {
			File file = new File(sConfigFileName);
			FileReader fileReader = new FileReader(file);
			BufferedReader bufferedReader = new BufferedReader(fileReader);
			String line;
			//Using a buffer line limit makes the appends faster with a long file:
			int iBufferLineLimit = 100;
			int iLineCounter = 0;
			String sBuffer = "";
			while ((line = bufferedReader.readLine()) != null) {
				sBuffer +=  line + "\n";
				if (iLineCounter == iBufferLineLimit){
					sResult += sBuffer;
					sBuffer = "";
				}
				iLineCounter++;
			}
			fileReader.close();
			//Get the remaining lines:
			sResult += sBuffer;
		} catch (IOException e) {
			throw new Exception("Error [1463602888] reading config file '" + sConfigFileName + "' - " + e.getMessage());
		}		
		if(SecuritySystem.bFakeAllGPIOCalls || SecuritySystem.bOutDiagnosticsToCommandLine){
			System.out.println(sDiagnosticMessage);
		}
		
		return sResult;
	}

	private static void updateConfigFile(String sRequestString) throws Exception{
		
		String sDiagnosticMessage = "[1579025670] Updating contents of config file: ";
		SecuritySystem.writeControllerLogEntry(
				"Request to update config file sent by "
				+ SSUtilities.getKeyValue(sRequestString, SSConstants.QUERY_KEY_USER_ID)
				+ " - "
				+ SSUtilities.getKeyValue(sRequestString, SSConstants.QUERY_KEY_USER_FULL_NAME),
				1);
		//Read the config file into a buffer and return it:
		String sConfigFileName = SecuritySystem.getConfigFileFullName();
		if (sConfigFileName.compareToIgnoreCase("") == 0){
			throw new Exception("Error [1463665650] - no config file found.");
		}
		
		//Get the request string into an array of key value pairs:
		String sKeyValuePairs[] = SSUtilities.getKeyValuePairsList(sRequestString);
		
		//Now copy those keys into a new array IF the key is actually a valid config file key - we don't want to pick up
		//other commands from the command string:
		//This array will store the config file keys from the update command:
		ArrayList<String>arrUpdateConfFileCommandKeyValuePairs = new ArrayList<String>(0);
		for (int i = 0; i < sKeyValuePairs.length; i++){
			String sKeyInCommandString = SSUtilities.getKeyFromRequestList(sKeyValuePairs, i);
			for(int k = 0; k < SSConstants.CONFIG_KEY_LIST.length; k++){
				if (sKeyInCommandString.compareToIgnoreCase(SSConstants.CONFIG_KEY_LIST[k]) == 0){
					arrUpdateConfFileCommandKeyValuePairs.add(sKeyValuePairs[i]);
				}
			}
		}

		//This array is for storing the CURRENT config file lines:
		ArrayList<String>arrLineBuffer = new ArrayList<String>(0);
		String sCurrentConfigFileLine;
		try {
			File file = new File(sConfigFileName);
			FileReader fileReader = new FileReader(file);
			BufferedReader bufferedReader = new BufferedReader(fileReader);
			while ((sCurrentConfigFileLine = bufferedReader.readLine()) != null) {
				//First, we'll add every line to the buffer:
				arrLineBuffer.add(sCurrentConfigFileLine);
			}
			fileReader.close();
		} catch (IOException e) {
			throw new Exception("Error [1463665651] reading config file '" + sConfigFileName + "' - " + e.getMessage());
		}
			
		//Now go through the list of keys from the update command, one at a time.  If the key is found in the config file,
		//replace that line.  If it's NOT found, at it to the buffer array to be written back to the config file:
		for (int i = 0; i < arrUpdateConfFileCommandKeyValuePairs.size(); i++){
			//Get the key from the update command:
			String sKeyInUpdateCommand = arrUpdateConfFileCommandKeyValuePairs.get(i).substring(
				0, arrUpdateConfFileCommandKeyValuePairs.get(i).indexOf(SSConstants.QUERY_STRING_EQUALS_SYMBOL)).trim();

			//We use this to tell if a matching line was found:
			boolean bAMatchingLineWasFound = false;
			for (int j = 0; j < arrLineBuffer.size(); j++){
				//Get the key from each line in the current config file:
				String sConfigFileKey = "";
				//If it contains an 'EQUALS' sign, then it's a 'key' line:
				if (arrLineBuffer.get(j).contains(SSConstants.QUERY_STRING_EQUALS_SYMBOL)){
					//Get the key from the current config file:
					sConfigFileKey = arrLineBuffer.get(j).substring(0, arrLineBuffer.get(j).indexOf(SSConstants.QUERY_STRING_EQUALS_SYMBOL)).trim();
					//If that key matches a key in the update command, replace the old line with the key value pair
					//in the update command:
					if (sConfigFileKey.compareToIgnoreCase(sKeyInUpdateCommand) == 0){
						arrLineBuffer.set(j, arrUpdateConfFileCommandKeyValuePairs.get(i));
						//Indicate that the line was found so we don't add the key to the file as a new one:
						bAMatchingLineWasFound = true;
					}
				}
			}
			//If the line was not found in the current config file, add this one from the command to it:
			if (!bAMatchingLineWasFound){
				arrLineBuffer.add(arrUpdateConfFileCommandKeyValuePairs.get(i));
			}
		}
			
		//Now the buffer should contain all the updated lines, and any new ones we wanted to add.
		//Write it back to the original config file:
		BufferedWriter bw = null;
		try {
			// OVERWRITE MODE SET HERE
			bw = new BufferedWriter(new FileWriter(sConfigFileName, false));
			for (int i = 0; i < arrLineBuffer.size(); i++){
				bw.write(arrLineBuffer.get(i));
				bw.newLine();
				bw.flush();
			}
		} catch (IOException e) {
			throw new Exception("Error [1463665651] writing to file '" + sConfigFileName + "' - " + e.getMessage());
		} finally {                       // always close the file
			if (bw != null) try {
				bw.close();
			} catch (IOException ioe2) {
				// just ignore it
			}
		}
		
		//If we got this far, then update the actual running values of any config file keys:
		for (int i = 0; i < arrUpdateConfFileCommandKeyValuePairs.size(); i++){
			String sKeyInUpdateCommand = arrUpdateConfFileCommandKeyValuePairs.get(i).substring(
					0, arrUpdateConfFileCommandKeyValuePairs.get(i).indexOf(SSConstants.QUERY_STRING_EQUALS_SYMBOL)).trim();
			if (sKeyInUpdateCommand.compareToIgnoreCase(SSConstants.CONFFILE_DATABASE_ID) == 0){
				SecuritySystem.setDatabaseID(arrUpdateConfFileCommandKeyValuePairs.get(i).substring(
					arrUpdateConfFileCommandKeyValuePairs.get(i).indexOf(SSConstants.QUERY_STRING_EQUALS_SYMBOL) + 1,
					arrUpdateConfFileCommandKeyValuePairs.get(i).length()).trim());
			}
			if (sKeyInUpdateCommand.compareToIgnoreCase(SSConstants.CONFFILE_KEY_NAME) == 0){
				SecuritySystem.setControllerName(arrUpdateConfFileCommandKeyValuePairs.get(i).substring(
					arrUpdateConfFileCommandKeyValuePairs.get(i).indexOf(SSConstants.QUERY_STRING_EQUALS_SYMBOL) + 1,
					arrUpdateConfFileCommandKeyValuePairs.get(i).length()).trim());
			}
			if (sKeyInUpdateCommand.compareToIgnoreCase(SSConstants.CONFFILE_KEY_PASSCODE) == 0){
				SecuritySystem.setControllerPasscode(arrUpdateConfFileCommandKeyValuePairs.get(i).substring(
					arrUpdateConfFileCommandKeyValuePairs.get(i).indexOf(SSConstants.QUERY_STRING_EQUALS_SYMBOL) + 1,
					arrUpdateConfFileCommandKeyValuePairs.get(i).length()).trim());
			}
			if (sKeyInUpdateCommand.compareToIgnoreCase(SSConstants.CONFFILE_LISTENING_PORT) == 0){
				String sCurrentListeningPort = SecuritySystem.getListeningPortNumber();
				String sUpdatedListeningPort = arrUpdateConfFileCommandKeyValuePairs.get(i).substring(
						arrUpdateConfFileCommandKeyValuePairs.get(i).indexOf(SSConstants.QUERY_STRING_EQUALS_SYMBOL) + 1,
						arrUpdateConfFileCommandKeyValuePairs.get(i).length()).trim();
				SecuritySystem.setListeningPortNumber(sUpdatedListeningPort);
				//If we've updated the port number, we'll need to restart the listening server:
				if (sCurrentListeningPort.compareToIgnoreCase(sUpdatedListeningPort) != 0){
					SecuritySystem.restartListeningServer();
				}
				
			}
			if (sKeyInUpdateCommand.compareToIgnoreCase(SSConstants.CONFFILE_LOG_FILE) == 0){
				SecuritySystem.setLogFile(arrUpdateConfFileCommandKeyValuePairs.get(i).substring(
					arrUpdateConfFileCommandKeyValuePairs.get(i).indexOf(SSConstants.QUERY_STRING_EQUALS_SYMBOL) + 1,
					arrUpdateConfFileCommandKeyValuePairs.get(i).length()).trim());
			}
			if (sKeyInUpdateCommand.compareToIgnoreCase(SSConstants.CONFFILE_LOGGING_LEVEL) == 0){
				SecuritySystem.setLoggingLevel(arrUpdateConfFileCommandKeyValuePairs.get(i).substring(
					arrUpdateConfFileCommandKeyValuePairs.get(i).indexOf(SSConstants.QUERY_STRING_EQUALS_SYMBOL) + 1,
					arrUpdateConfFileCommandKeyValuePairs.get(i).length()).trim());
			}
			if (sKeyInUpdateCommand.compareToIgnoreCase(SSConstants.CONFFILE_WEB_APP_PORT) == 0){
				SecuritySystem.setWebAppPortNumber(arrUpdateConfFileCommandKeyValuePairs.get(i).substring(
					arrUpdateConfFileCommandKeyValuePairs.get(i).indexOf(SSConstants.QUERY_STRING_EQUALS_SYMBOL) + 1,
					arrUpdateConfFileCommandKeyValuePairs.get(i).length()).trim());
			}
			if (sKeyInUpdateCommand.compareToIgnoreCase(SSConstants.CONFFILE_WEB_APP_URL) == 0){
				SecuritySystem.setWebAppURL(arrUpdateConfFileCommandKeyValuePairs.get(i).substring(
					arrUpdateConfFileCommandKeyValuePairs.get(i).indexOf(SSConstants.QUERY_STRING_EQUALS_SYMBOL) + 1,
					arrUpdateConfFileCommandKeyValuePairs.get(i).length()).trim());
			}
		}
		
		if(SecuritySystem.bFakeAllGPIOCalls || SecuritySystem.bOutDiagnosticsToCommandLine){
			System.out.println(sDiagnosticMessage);
		}
		return;
	}
	private static void updateControllerSoftware(String sRequestString, String sWebAppURL, String sWebAppPort) throws Exception{
		SecuritySystem.writeControllerLogEntry(
				"Request to update controller software sent by "
				+ SSUtilities.getKeyValue(sRequestString, SSConstants.QUERY_KEY_USER_ID)
				+ " - "
				+ SSUtilities.getKeyValue(sRequestString, SSConstants.QUERY_KEY_USER_FULL_NAME),
				1);
		//First confirm that the program version available is actually newer:
		String sAvailableVersion = SSUtilities.getKeyValue(sRequestString, SSConstants.QUERY_KEY_UPDATE_PROGRAM_VERSION_AVAILABLE);
		int iAvailableVersion = 0;
		try {
			iAvailableVersion = Integer.parseInt(sAvailableVersion);
		} catch (Exception e) {
			throw new Exception("Error [1464011549] available version number '" + sAvailableVersion + "' is not a valid integer.");
		}
		if (Integer.parseInt(SSConstants.CONTROLLER_VERSION) >= iAvailableVersion){
			throw new Exception("Error [1464011550] current controller software version number (" + SSConstants.CONTROLLER_VERSION + ") is "
					+ "up to date.");
		}

		SecuritySystem.writeControllerLogEntry(
				"Received request to update program to version " + sAvailableVersion + " from current version " + SSConstants.CONTROLLER_VERSION, 
				1
				);

		//Otherwise, start pulling the newer version:
		//Get the path name on the server to the new version:
		String sUpdateFilePath = SSUtilities.getKeyValue(sRequestString, SSConstants.QUERY_KEY_PATH_TO_PROGRAM_UPDATE);

		//Remove the program update file if it's already there:
		String sCommandResult = "";
		String sCommand = "rm -f " + SSConstants.DEFAULT_PROGRAM_FOLDER_ON_CONTROLLER + "/" + SSConstants.PROGRAM_UPDATE_ZIP_FILE;
		try {
			sCommandResult = SSUtilities.executeSystemCommand(sCommand);
			SecuritySystem.writeControllerLogEntry(
					"Update controller command: '" + sCommand + "': result: '" + sCommandResult + "'",
					3);
		} catch (Exception e) {
			SecuritySystem.writeControllerLogEntry(
					"Error [1464011554] executing '" + sCommand + "' - " + e.getMessage(), 
					1
					);
			throw new Exception("Error [1464011554] executing '" + sCommand + "' - " + e.getMessage());
		}

		//Retrieve that file:
		if (sWebAppPort.compareToIgnoreCase("") == 0){
			sCommand = "wget " + sWebAppURL + sUpdateFilePath + " -P " + SSConstants.DEFAULT_PROGRAM_FOLDER_ON_CONTROLLER;	
		}else{
			sCommand = "wget " + sWebAppURL + ":" + sWebAppPort + sUpdateFilePath + " -P " + SSConstants.DEFAULT_PROGRAM_FOLDER_ON_CONTROLLER;
		}
		try {
			sCommandResult = SSUtilities.executeSystemCommand(sCommand);
			SecuritySystem.writeControllerLogEntry(
					"Update controller command: '" + sCommand + "': result: '" + sCommandResult + "'",
					3);
		} catch (Exception e) {
			SecuritySystem.writeControllerLogEntry(
					"Error [1464011551] executing '" + sCommand + "' - " + e.getMessage(), 
					1
					);
			throw new Exception("Error [1464011551] executing '" + sCommand + "' - " + e.getMessage());
		}

		//Unzip that file:
		String sUnzipPassword = SSUtilities.getKeyValue(sRequestString, SSConstants.QUERY_KEY_PROGRAM_UPDATE_UNZIP_PASSWORD);
		sCommand = "unzip -P " + sUnzipPassword + " -o " 
				+ SSConstants.DEFAULT_PROGRAM_FOLDER_ON_CONTROLLER + "/" + SSConstants.PROGRAM_UPDATE_ZIP_FILE 
				+ " -d " + SSConstants.DEFAULT_PROGRAM_FOLDER_ON_CONTROLLER;
		try {
			sCommandResult = SSUtilities.executeSystemCommand(sCommand);
			SecuritySystem.writeControllerLogEntry(
					"Update controller command: '" + sCommand + "': result: '" + sCommandResult + "'",
					3);
		} catch (Exception e) {
			SecuritySystem.writeControllerLogEntry(
					"Error [1464011552] executing '" + sCommand + "' - " + e.getMessage(), 
					1
					);
			throw new Exception("Error [1464011552] executing '" + sCommand + "' - " + e.getMessage());
		}

		//Move the current program to 'old':
		sCommand = "mv " + SSConstants.DEFAULT_PROGRAM_FOLDER_ON_CONTROLLER + "/" + SSConstants.PROGRAM_UPDATE_JAR_FILE
			+ " " + SSConstants.DEFAULT_PROGRAM_FOLDER_ON_CONTROLLER + "/" + SSConstants.PROGRAM_UPDATE_ZIP_FILE_OLD ;
		try {
			sCommandResult = SSUtilities.executeSystemCommand(sCommand);
			SecuritySystem.writeControllerLogEntry(
					"Update controller command: '" + sCommand + "': result: '" + sCommandResult + "'",
					3);
		} catch (Exception e) {
			SecuritySystem.writeControllerLogEntry(
					"Error [1464011556] executing '" + sCommand + "' - " + e.getMessage(), 
					1
					);
			throw new Exception("Error [1464011556] executing '" + sCommand + "' - " + e.getMessage());
		}

		//Move the new, downloaded and unzipped program to be the live one:
		sCommand = "mv " + SSConstants.DEFAULT_PROGRAM_FOLDER_ON_CONTROLLER + "/" + SSConstants.PROGRAM_UPDATE_ZIP_FILE_NEW
			+ " " + SSConstants.DEFAULT_PROGRAM_FOLDER_ON_CONTROLLER + "/" + SSConstants.PROGRAM_UPDATE_JAR_FILE;
		try {
			sCommandResult = SSUtilities.executeSystemCommand(sCommand);
			SecuritySystem.writeControllerLogEntry(
					"Update controller command: '" + sCommand + "': result: '" + sCommandResult + "'",
					3);
		} catch (Exception e) {
			SecuritySystem.writeControllerLogEntry(
					"Error [1464011557] executing '" + sCommand + "' - " + e.getMessage(), 
					1
					);
			throw new Exception("Error [1464011557] executing '" + sCommand + "' - " + e.getMessage());
		}

		return;

	}
	static void setFakeInputTerminalContactStatus(String sRequestString) throws Exception{
		
		String sDiagnosticMessage = "[1579025674] Setting FAKE input terminal contact status: ";
		
		String sKeyValuePairs[] = SSUtilities.getKeyValuePairsList(sRequestString);
		
		for (int i = 0; i < sKeyValuePairs.length; i++){
			//Pick off each pin and set its listening state:
			String sKey = SSUtilities.getKeyFromRequestList(sKeyValuePairs, i);
			if (sKey.startsWith(SSConstants.QUERY_KEY_TERMINALCONTACTSTATE_PREFIX)){
				String sTerminalNumber = sKey.substring(SSConstants.QUERY_KEY_TERMINALCONTACTSTATE_PREFIX.length(), sKey.length());
				String sContactState = SSUtilities.getKeyValue(sRequestString, sKey);
				boolean bContactsOpen = false;
				if (sContactState.compareToIgnoreCase(SSConstants.QUERY_KEYVALUE_TERMINAL_CONTACT_STATUS_OPEN) == 0){
					bContactsOpen = true;
				}
				try {
					SecuritySystem.getGPIOHandler().setFakeTerminalContactStatus(sTerminalNumber, bContactsOpen);
				} catch (Exception e) {
					throw new Exception("Error [1460735415] - could not set terminal " + sTerminalNumber 
						+ " to FAKE contact status " + sContactState + " - " + e.getMessage());
				}
				sDiagnosticMessage+= " Set terminal " + sTerminalNumber + " contact state to " + sContactState + ",";
			}
		}
		
		if(SecuritySystem.bFakeAllGPIOCalls || SecuritySystem.bOutDiagnosticsToCommandLine){
			System.out.println(sDiagnosticMessage);
		}
	}
	static void setTestMode(String sRequestString) throws Exception{
		
		String sDiagnosticMessage = "[1579025676] Setting controller test mode to '" 
			+ SSUtilities.getKeyValue(sRequestString, SSConstants.QUERY_KEY_TEST_MODE_STATE) + "': ";
		
		SecuritySystem.writeControllerLogEntry(
			"Request to set controller test mode to '" 
			+ SSUtilities.getKeyValue(sRequestString, SSConstants.QUERY_KEY_TEST_MODE_STATE) + "' sent by "
			+ SSUtilities.getKeyValue(sRequestString, SSConstants.QUERY_KEY_USER_ID)
			+ " - "
			+ SSUtilities.getKeyValue(sRequestString, SSConstants.QUERY_KEY_USER_FULL_NAME),
			1);
		
		boolean bSetToListening;
		if (SSUtilities.getKeyValue(sRequestString, SSConstants.QUERY_KEY_TEST_MODE_STATE).compareToIgnoreCase(SSConstants.QUERY_KEYVALUE_TEST_MODE_ON) == 0){
			bSetToListening = true;
			//If any terminals are in listening mode, or any outputs are closed, we can't set test mode:
			String sListeningTerminals = "";
			for(int i = 0; i < SecuritySystem.getGPIOHandler().getNumberOfInputTerminals(); i++){
				SecuritySystem.getGPIOHandler();
				if(gpioHandler.getInputTerminalListeningStatus(
						SecuritySystem.getGPIOHandler().getInputTerminalNumber(i))){
					if (sListeningTerminals.compareToIgnoreCase("") == 0){
						sListeningTerminals += SecuritySystem.getGPIOHandler().getInputTerminalNumber(i);
					}else{
						sListeningTerminals += ", " + SecuritySystem.getGPIOHandler().getInputTerminalNumber(i);
					}
				} 
			}
			
			String sClosedOutputs = "";
			for(int i = 0; i < SecuritySystem.getGPIOHandler().getNumberOfOutputTerminals(); i++){
				if (!SecuritySystem.getGPIOHandler().areOutputTerminalContactsOpen(
					SecuritySystem.getGPIOHandler().getOutputTerminalNumber(i))){
					if (sClosedOutputs.compareToIgnoreCase("") == 0){
						sClosedOutputs += SecuritySystem.getGPIOHandler().getOutputTerminalNumber(i);
					}else{
						sClosedOutputs += ", " + SecuritySystem.getGPIOHandler().getOutputTerminalNumber(i);
					}
				}
			}
			
			String sInvalidTerminals = "";
			if (sListeningTerminals.compareTo("") != 0){
				sInvalidTerminals += "Input Terminals " + sListeningTerminals + " are currently listening.  ";
			}
			if (sClosedOutputs.compareTo("") != 0){
				sInvalidTerminals += "Output Terminals " + sClosedOutputs + " are currently closed.";
			}
			if (sInvalidTerminals.compareToIgnoreCase("") != 0){
				throw new Exception("Error [1464885453] - could not set test mode: " + sInvalidTerminals); 
			}
		}else{
			bSetToListening = false;
		}
		
		for(int i = 0; i < SecuritySystem.getGPIOHandler().getNumberOfInputTerminals(); i++){
			try {
				SecuritySystem.getGPIOHandler().setInputTerminalListeningStatus(
					SecuritySystem.getGPIOHandler().getInputTerminalNumber(i), bSetToListening);
			} catch (Exception e) {
				throw new Exception("Error [1464356744] - could not set terminal " 
					+ SecuritySystem.getGPIOHandler().getInputTerminalNumber(i) 
					+ " to listening status - " + e.getMessage());
			}
		}
		
		//Set the flag:
		SecuritySystem.setTestModeRunning(bSetToListening);
		
		if(SecuritySystem.bFakeAllGPIOCalls || SecuritySystem.bOutDiagnosticsToCommandLine){
			System.out.println(sDiagnosticMessage);
		}
	}
	static String getTestMode(String sRequestString) throws Exception{
		
		String sDiagnosticMessage = "[1579025678] Getting controller test mode state '" 
			+ SSUtilities.getKeyValue(sRequestString, SSConstants.QUERY_KEY_TEST_MODE_STATE) + "': ";
		
		SecuritySystem.writeControllerLogEntry(
			"Request to get controller test mode sent by "
			+ SSUtilities.getKeyValue(sRequestString, SSConstants.QUERY_KEY_USER_ID)
			+ " - "
			+ SSUtilities.getKeyValue(sRequestString, SSConstants.QUERY_KEY_USER_FULL_NAME),
			1);

		String sResult = "";
		if(SecuritySystem.getTestModeRunning()){
			sResult = SSConstants.QUERY_KEYVALUE_TEST_MODE_ON;
		}else{
			sResult = SSConstants.QUERY_KEYVALUE_TEST_MODE_OFF;
		}
		
		if(SecuritySystem.bFakeAllGPIOCalls || SecuritySystem.bOutDiagnosticsToCommandLine){
			System.out.println(sDiagnosticMessage);
		}
		return sResult;
	}
}
