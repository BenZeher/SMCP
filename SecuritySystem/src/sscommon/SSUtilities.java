package sscommon;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

import SecuritySystem.SecuritySystem;

public class SSUtilities {

	public static String getKeyValue(String sQueryString, String sParamName){
		String s = "";
		String sParamArray[] = sQueryString.split(SSConstants.QUERY_STRING_DELIMITER);
		for (int i = 0; i < sParamArray.length; i++){
			String sKeyValuePair = sParamArray[i];
			String sKeyValueArray[] = sKeyValuePair.split(SSConstants.QUERY_STRING_EQUALS_SYMBOL);
			String sKey = sKeyValueArray[0].trim();
			if (sKey.compareToIgnoreCase(sParamName) == 0){
				return sKeyValueArray[1].trim();
			}
		}
		return s;
	}

	public static String buildKeyValuePair(String sKey, String sValue){
		return sKey + SSConstants.QUERY_STRING_EQUALS_SYMBOL + sValue.replace("&", "+").replace("=", SSConstants.QUERY_EQUALS_SIGN_MASK);
	}

	//This just turns the request string into a String array:
	public static String[] getKeyValuePairsList(String sRequestString){
		return sRequestString.split(SSConstants.QUERY_STRING_DELIMITER);
	}
	
	//This just gets each key out of a String Array:
	public static String getKeyFromRequestList(String[] sKeyValuePairsList, int iIndex){
		return sKeyValuePairsList[iIndex].split(SSConstants.QUERY_STRING_EQUALS_SYMBOL)[0];
	}
	
	public static void printCommandSyntax(){
		System.out.println("Command line usage: pass in the config file using the option " + SSConstants.COMMAND_LINE_OPTION_CONFIG_FILE);
		System.out.println("For Example:");
		System.out.println("SecuritySystem " + SSConstants.COMMAND_LINE_OPTION_CONFIG_FILE + " /opt/securitysystem/ss.ini\n\n");
		System.out.println("ADDITIONAL OPTIONS:\n");
		System.out.println("'" + SSConstants.COMMAND_LINE_OPTION_FAKE_GPIO + "' - tells the program to 'fake' the GPIO input and output - this is used"
			+ " when testing on a computer rather than on an actual pi.\n");
		System.out.println("'" + SSConstants.COMMAND_LINE_OPTION_OUTPUT_DIAGNOSTICS + "' - tells the program to output diagnostics to the command line.\n");
		System.out.println("'" + SSConstants.COMMAND_LINE_OPTION_SHOW_SAMPLE_COMMANDS + "' - prints sample socket commands to the command line for testing.\n");
		System.out.println("'" + SSConstants.COMMAND_LINE_OPTION_SHOW_PIN_TO_TERMINAL_MAPPING + "' - prints the pin-to-terminal mappings to the command line.\n");
		System.out.println("'" + SSConstants.COMMAND_LINE_OPTION_USE_SYSTEMD_RESTART + "' - tells the program to use systemd commands when restarting.\n");
	}

	public static void processCommandLine(String sCommandLineArguments[]) throws Exception{
	
		if (sCommandLineArguments.length == 0) {
			printCommandSyntax();
			return;
		}
	
		SecuritySystem.setFakeAllGPIO(false);
		SecuritySystem.setOutputDiagnostics(false);
		try {
			for (int i = 0; i < sCommandLineArguments.length; i++){
				if (sCommandLineArguments[i].startsWith(SSConstants.COMMAND_LINE_OPTION_CONFIG_FILE)){
					SecuritySystem.setConfigFileFullName(sCommandLineArguments[i].substring(SSConstants.COMMAND_LINE_OPTION_CONFIG_FILE.length()));
				}
				if (sCommandLineArguments[i].compareToIgnoreCase(SSConstants.COMMAND_LINE_OPTION_FAKE_GPIO) == 0){
					//'Fake' all the gpio for testing:
					SecuritySystem.setFakeAllGPIO(true);
				}
				if (sCommandLineArguments[i].compareToIgnoreCase(SSConstants.COMMAND_LINE_OPTION_OUTPUT_DIAGNOSTICS) == 0){
					//'Fake' all the gpio for testing:
					SecuritySystem.setOutputDiagnostics(true);
				}
				if (sCommandLineArguments[i].compareToIgnoreCase(SSConstants.COMMAND_LINE_OPTION_SHOW_SAMPLE_COMMANDS) == 0){
					//List sample commands to stdout:
					SecuritySystem.setPrintSampleCommands(true);
				}
				if (sCommandLineArguments[i].compareToIgnoreCase(SSConstants.COMMAND_LINE_OPTION_SHOW_PIN_TO_TERMINAL_MAPPING) == 0){
					//List pin-to-terminal mappings to stdout:
					SecuritySystem.setPrintPinToTerminalMappings(true);
				}
				if (sCommandLineArguments[i].compareToIgnoreCase(SSConstants.COMMAND_LINE_OPTION_USE_PI_GPIO_PINS) == 0){
					//Use PI GPIO Pins (NOT using I2C devices):
					SecuritySystem.setUsePiGpioPins(true);
				}
				if (sCommandLineArguments[i].compareToIgnoreCase(SSConstants.COMMAND_LINE_OPTION_USE_SYSTEMD_RESTART) == 0){
					//Flag to restart using systemd (newer Linux versions)
					SecuritySystem.setUsingSystemd(true);
				}
			}
		} catch (Exception e) {
			throw new Exception("Error [1458178425] reading command line - " + e.getMessage());
		}
	
		//TODO = add any other command line options here:
		//.....
	
	}

	public static void processConfigFile(String sFullFileName) throws Exception{
		//Read configuration file saved on PI
		try {
			File file = new File(sFullFileName);
			FileReader fileReader = new FileReader(file);
			BufferedReader bufferedReader = new BufferedReader(fileReader);
			String line;
			while ((line = bufferedReader.readLine()) != null) {
				String[] sKeyValue = line.split(SSConstants.CONFFILE_KEY_VALUE_DELIMITER);
				if (sKeyValue[0].trim().compareToIgnoreCase(SSConstants.CONFFILE_KEY_NAME) == 0){
					SecuritySystem.setControllerName(sKeyValue[1].trim());
				}
				if (sKeyValue[0].trim().compareToIgnoreCase(SSConstants.CONFFILE_KEY_PASSCODE) == 0){
					SecuritySystem.setControllerPasscode(sKeyValue[1].trim());
				}
				if (sKeyValue[0].trim().compareToIgnoreCase(SSConstants.CONFFILE_LISTENING_PORT) == 0){
					SecuritySystem.setListeningPortNumber(sKeyValue[1].trim());
				}
				if (sKeyValue[0].trim().compareToIgnoreCase(SSConstants.CONFFILE_WEB_APP_URL) == 0){
					SecuritySystem.setWebAppURL(sKeyValue[1].trim());
				}
				if (sKeyValue[0].trim().compareToIgnoreCase(SSConstants.CONFFILE_WEB_APP_PORT) == 0){
					SecuritySystem.setWebAppPortNumber(sKeyValue[1].trim());
				}
				if (sKeyValue[0].trim().compareToIgnoreCase(SSConstants.CONFFILE_DATABASE_ID) == 0){
					SecuritySystem.setDatabaseID(sKeyValue[1].trim());
				}
				if (sKeyValue[0].trim().compareToIgnoreCase(SSConstants.CONFFILE_LOGGING_LEVEL) == 0){
					SecuritySystem.setLoggingLevel(sKeyValue[1].trim());
				}
				if (sKeyValue[0].trim().compareToIgnoreCase(SSConstants.CONFFILE_LOG_FILE) == 0){
					SecuritySystem.setLogFile(sKeyValue[1].trim());
				}			
			}
			fileReader.close();
		} catch (IOException e) {
			throw new Exception("Error [1458178367] reading config file '" + sFullFileName + "' - " + e.getMessage());
		}
		//Confirm required values:
		String sErrorMessages = "";
		if (SecuritySystem.getControllerName().compareToIgnoreCase("") == 0){
			sErrorMessages += SSConstants.CONTROLLER_NAME_LABEL + " is not found in config file.\n";
		}
		if (SecuritySystem.getControllerPasscode().compareToIgnoreCase("") == 0){
			sErrorMessages += SSConstants.CONTROLLER_PASSWORD_LABEL + " is not found in config file.\n";
		}
		if (SecuritySystem.getListeningPortNumber().compareToIgnoreCase("") == 0){
			sErrorMessages += SSConstants.LISTENING_PORT_LABEL + " is not found in config file.\n";
		}
		if (SecuritySystem.getWebAppURL().compareToIgnoreCase("") == 0){
			sErrorMessages += SSConstants.CONFFILE_WEB_APP_URL + " is not found in config file.\n";
		}
		if (SecuritySystem.getWebAppPortNumber().compareToIgnoreCase("") == 0){
			sErrorMessages += SSConstants.CONFFILE_WEB_APP_PORT + " is not found in config file.\n";
		}
		if (SecuritySystem.getDatabaseID().compareToIgnoreCase("") == 0){
			sErrorMessages += SSConstants.DATABASE_ID_LABEL + " is not found in config file.\n";
		}
		if (SecuritySystem.getLoggingLevel() > 0){
			if (SecuritySystem.getLogFile().compareToIgnoreCase("") == 0){
				sErrorMessages += SSConstants.LOGGING_LEVEL_LABEL + " is greater than zero, but no log file was specified.\n";				
			}
		}
		
		//Check the port numbers:
		try {
			@SuppressWarnings("unused")
			int iListeningPort = Integer.parseInt(SecuritySystem.getListeningPortNumber());
		} catch (Exception e) {
			sErrorMessages += SSConstants.LISTENING_PORT_LABEL + " '" + SecuritySystem.getListeningPortNumber() + "' is invalid.\n";
		}
		try {
			@SuppressWarnings("unused")
			int iWebUrlPort = Integer.parseInt(SecuritySystem.getWebAppPortNumber());
		} catch (Exception e) {
			sErrorMessages += SSConstants.WEB_APP_PORT_LABEL + " '" + SecuritySystem.getWebAppPortNumber() + "' is invalid.\n";
		}
	
		if (sErrorMessages.compareToIgnoreCase("") != 0){
			throw new Exception(sErrorMessages);
		}
	}

	public static String printSampleCommands() throws Exception{
		String s = "";
		String sUser = "100";
		String sUserFullName = "Ebenezer Scrooge";
		ArrayList<String>arrTerminalNumbers = new ArrayList<String>(0);
		ArrayList<String>arrStates = new ArrayList<String>(0);
		ArrayList<String>arrIntervals = new ArrayList<String>(0);
		
		s += "SAMPLE TEST COMMANDS FOR THE CONTROLLER:\n"
			+ "Telnet to the controller on port " + SecuritySystem.getListeningPortNumber() + " and type these commands"
				+ " to test the controller and program.\n"
		;
		
		//Test opening or closing output contacts:
		arrTerminalNumbers.add("25");
		arrStates.add(SSConstants.QUERY_KEYVALUE_TERMINAL_CONTACT_STATUS_CLOSED);
		arrIntervals.add("3000");
		arrTerminalNumbers.add("26");
		arrStates.add(SSConstants.QUERY_KEYVALUE_TERMINAL_CONTACT_STATUS_OPEN);
		arrIntervals.add("0");

		s += "\n"
			+ "To OPEN/CLOSE output TERMINAL CONTACTS:\n"
			+ "Set Terminal 25 contacts to CLOSED - interval is 3000 ms, terminal 26 contacts to OPEN - interval is 0 ms.\n";
		String sSetTerminalContactsCommand;
		try {
			sSetTerminalContactsCommand = buildRequestFromServerToSetTerminalContacts(
						arrTerminalNumbers,
						arrStates,
						arrIntervals,
						SecuritySystem.getControllerName(),
						SecuritySystem.getControllerPasscode(),
						sUser,
						sUserFullName,
						"TestServer"
						);
			;
		} catch (Exception e) {
			throw new Exception(" Error [1521132683] - " + e.getMessage());
		}
		s += sSetTerminalContactsCommand.substring(0, sSetTerminalContactsCommand.length() - 1);

		
		//Test getting the contact state on all the terminals:
		s += "\n"
			+ "To get the CONTACT STATE on all TERMINALS:\n" 
		;
		String sGetContactStateOfTerminals = buildRequestFromServerToGetTerminalsContactStatus(
			SecuritySystem.getControllerName(),
			SecuritySystem.getControllerPasscode(),
			sUser,
			sUserFullName,
			"TestServer1"
		);
		s += sGetContactStateOfTerminals.substring(0, sGetContactStateOfTerminals.length() - 1);

		
		//Test setting the listening state on some input terminals:
		arrTerminalNumbers.clear();
		arrStates.clear();
		arrIntervals.clear();
		arrTerminalNumbers.add("1");
		arrStates.add(SSConstants.QUERY_KEYVALUE_TERMINAL_LISTENING_STATUS_LISTENING);
		arrTerminalNumbers.add("2");
		arrStates.add(SSConstants.QUERY_KEYVALUE_TERMINAL_LISTENING_STATUS_NOTLISTENING);
		s += "\n"
			+ "To set the listening state on input TERMINALS:\n"
			+ "Terminal 1 IS listening, terminal 2 is NOT listening.\n" 
		;
		String sSetTerminalListeningStatusCommand = buildRequestFromServerToSetTerminalsListeningStatus(
				arrTerminalNumbers,
				arrStates,
				SecuritySystem.getControllerName(),
				SecuritySystem.getControllerPasscode(),
				sUser,
				sUserFullName,
				"TestServer1"
				)
		;
		s += "" + sSetTerminalListeningStatusCommand.substring(0, sSetTerminalListeningStatusCommand.length() - 1);
		
		//Test getting the listening state on all the input terminals:
		s += "\n"
			+ "To get the LISTENING STATE on all INPUT TERMINALS:\n" 
		;
		String sGetListeningStateOfInputTerminals = buildRequestFromServerToGetInputTerminalsListeningStatus(
			SecuritySystem.getControllerName(),
			SecuritySystem.getControllerPasscode(),
			sUser,
			sUserFullName,
			"TestServer1"
		);
		s += "" + sGetListeningStateOfInputTerminals.substring(0, sGetListeningStateOfInputTerminals.length() - 1);

		/*
		 * TJR - 7/13/2016 - removed this function because it no longer works since we started using the 'pulse' function:
		//Get the activation interval remaining times for all the output pins:
		s += "\n"
				+ "To get the ACTIVATION INTERVAL (Duration) ON ALL OUTPUT TERMINALS:\n" 
			;
		String sGetIntervalRemainingOnputTerminals = buildRequestFromServerToGetOutputTerminalIntervalRemaining(
			SecuritySystem.getControllerName(),
			SecuritySystem.getControllerPasscode(),
			sUser,
			sUserFullName
		);
		s += sGetIntervalRemainingOnputTerminals.substring(0, sGetIntervalRemainingOnputTerminals.length() - 1);
	*/
	
		//Test getting the controller software version:
		s += "\n"
			+ "To get the controller software VERSION:\n" 
		;
		String sGetControllerSoftwareVersion = buildRequestFromServerToGetControllerSystemInformation(
			SecuritySystem.getControllerName(),
			SecuritySystem.getControllerPasscode(),
			sUser,
			sUserFullName,
			"TestServer1"
		);
		s += sGetControllerSoftwareVersion.substring(0, sGetControllerSoftwareVersion.length() - 1);
			
			
		//Test telling the controller to delete its log:
		s += "\n"
			+ "To make the controller delete the system log:\n" 
		;
		
		String sDeleteControllerLog = buildRequestFromServerToDeleteControllerLogFile(
			SecuritySystem.getControllerName(),
			SecuritySystem.getControllerPasscode(),
			sUser,
			sUserFullName,
			"TestServer1"
		);
		s += sDeleteControllerLog.substring(0, sDeleteControllerLog.length() - 1);
		

		//Test telling the controller to send its log:
		s += "\n"
			+ "To make the controller SEND the system log:\n" 
		;
		String sSendControllerLog = buildRequestFromServerToGetLog(
			SecuritySystem.getControllerName(),
			SecuritySystem.getControllerPasscode(),
			sUser,
			sUserFullName,
			"TestServer1"
		);
		s += sSendControllerLog.substring(0, sSendControllerLog.length() - 1);
		

		//Test telling the controller to send its config file:
		s += "\n"
			+ "To make the controller SEND the config file:\n" 
		;
		String sSendControllerConfigFile = buildRequestFromServerToGetConfigFile(
			SecuritySystem.getControllerName(),
			SecuritySystem.getControllerPasscode(),
			sUser,
			sUserFullName,
			"TestServer1"
		);
		s += sSendControllerConfigFile.substring(0, sSendControllerConfigFile.length() - 1);

		//Test telling the controller to UPDATE its config file:
		s += "\n"
			+ "To make the controller UPDATE the config file:\n" 
		;
		s += "\n"
				+ "Update the LOGGING LEVEL to '2', and the database ID to 'sm3':\n" 
			;
		
		ArrayList<String> arrConfigKeys = new ArrayList<String>(0);
		arrConfigKeys.add(SSConstants.CONFFILE_LOGGING_LEVEL);
		arrConfigKeys.add(SSConstants.CONFFILE_DATABASE_ID);
		ArrayList<String> arrConfigKeyValues = new ArrayList<String>(0);
		arrConfigKeyValues.add("2");
		arrConfigKeyValues.add("sm3");
		String sUpdateConfigFile = buildRequestFromServerToUpdateConfigFile(
			arrConfigKeys,
			arrConfigKeyValues,
			SecuritySystem.getControllerName(),
			SecuritySystem.getControllerPasscode(),
			sUser,
			sUserFullName,
			"TestServer1"
			)
		;
		s += sUpdateConfigFile.substring(0, sUpdateConfigFile.length() - 1);
		
		//Test telling the controller to UPDATE its PROGRAM:
		s += "\n"
			+ "To make the controller UPDATE its software from the web app server:\n" 
		;
		String sUpdateControllerSoftware = buildRequestFromServerToUpdateProgram(
			SecuritySystem.getControllerName(),
			SecuritySystem.getControllerPasscode(),
			sUser,
			sUserFullName,
			"TestServer1"
			)
		;
		s += sUpdateControllerSoftware.substring(0, sUpdateControllerSoftware.length() - 1);
		
		//Restart the program:
		s += "\n";
		s += "To restart the program from a telnet command:\n";
		String sRestartCommand = buildRequestFromServerToRestartProgram(
				SecuritySystem.getControllerName(),
				SecuritySystem.getControllerPasscode(),
				sUser,
				sUserFullName,
				"TestServer1"
			);
		s += sRestartCommand.substring(0, sRestartCommand.length() - 1);
		
		
		//Test telling the controller to send its sample commands:
		s += "\n"
			+ "To make the controller send a list of sample commands:\n";
		String sSendSampleCommands = buildRequestFromServerToListSampleCommands(
			SecuritySystem.getControllerName(),
			SecuritySystem.getControllerPasscode(),
			sUser,
			sUserFullName,
			"TestServer1"
		);
		s += sSendSampleCommands.substring(0, sSendSampleCommands.length() - 1);
		
		//Test telling the controller to send its pin to terminal mappings:
		s += "\n"
			+ "To make the controller send a list of pin-to-terminal mappings:\n";
		String sSendPinMappings = buildRequestFromServerToListPinMappings(
			SecuritySystem.getControllerName(),
			SecuritySystem.getControllerPasscode(),
			sUser,
			sUserFullName,
			"TestServer1"
		);
		s += sSendPinMappings.substring(0, sSendPinMappings.length() - 1);
		
		//Turn test mode on or off:
		s += "\n"
			+ "To turn test mode on or off:\n"
			+ "This turns test mode ON:\n";
		String sSetTestModeState = SSConstants.QUERY_KEYVALUE_TEST_MODE_ON;
		String sToggleTestMode = buildRequestFromServerToSetTestMode(
			SecuritySystem.getControllerName(),
			SecuritySystem.getControllerPasscode(),
			sUser,
			sUserFullName,
			sSetTestModeState,
			"TestServer1"
		);
		s += sToggleTestMode.substring(0, sToggleTestMode.length() - 1);
		
		
		//Get the current test mode (on or off):
		s += "\n"
			+ "To ask if test mode is on or off:\n";
		String sGetTestMode = buildRequestFromServerToGetTestMode(
			SecuritySystem.getControllerName(),
			SecuritySystem.getControllerPasscode(),
			sUser,
			sUserFullName,
			"TestServer1"
		);
		s += sGetTestMode.substring(0, sGetTestMode.length() - 1);
		
		//Terminate the program:
		s += "\n";
		s += "To terminate the program from a telnet command:\n";
		String sTerminateCommand = buildRequestFromServerToTerminateProgram(
				SecuritySystem.getControllerName(),
				SecuritySystem.getControllerPasscode(),
				sUser,
				sUserFullName,
				"TestServer1"
			);
		s += sTerminateCommand.substring(0, sTerminateCommand.length() - 1);
		s += "\n";
		s += "\n";
		s += "********************************************************************************************************\n";
		s += "DIAGNOSTIC COMMANDS - THESE CAN ONLY BE USED FROM A TELNET PROMPT,\n"
			+ " - the server never actually uses them but they can be used to fake setting input contacts\n"
			+ " open or closed and they can 'fake' a 'trigger' from a listening contact:\n";
		s += "********************************************************************************************************\n";

		//Fake setting contact status on input terminals:
		s += "To fake setting an input terminal contact status, like a door open:\n"
			+ "Set Terminal 2 Contacts to CLOSED, terminal 3 contacts to OPEN:\n";
		
			arrTerminalNumbers.clear();
			arrStates.clear();
			arrIntervals.clear();
			arrTerminalNumbers.add("2");
			arrStates.add(SSConstants.QUERY_KEYVALUE_TERMINAL_CONTACT_STATUS_CLOSED);
			arrTerminalNumbers.add("3");
			arrStates.add(SSConstants.QUERY_KEYVALUE_TERMINAL_CONTACT_STATUS_OPEN);
			String sFakeSetInputContactsCommand = buildRequestFromTelnetToFakeSetInputTerminalContacts(
				arrTerminalNumbers,
				arrStates,
				SecuritySystem.getControllerName(),
				SecuritySystem.getControllerPasscode(),
				sUser,
				sUserFullName,
				"TestServer1"
				)
			;
			s += sFakeSetInputContactsCommand.substring(0, sFakeSetInputContactsCommand.length() - 1);
			s += "\n";
			
		return s;
	}
	public static String printPinToTerminalMappings(boolean bPrintInHTML){
		String s = "";
		s += "\n";
		s += "I2C PIN-TO-TERMINAL MAPPINGS:\n";
		s += "\n";
		s += " INPUT TERMINALS:\n";
		s += " |================================|\n";
		s += " | TERMINAL | I2C PIN  | I2C CHIP |\n";
		s += " |   NUMBER |  NUMBER  |  ADDRESS |\n";
		s += " |================================|\n"
		;
		if (bPrintInHTML){
			s = "<TABLE BORDER=1>";
			s += "<TR><TD COLSPAN=3><B>I2C PIN-TO-TERMINAL MAPPINGS</B>:</TD</TR>";
			s += "<TR><TD COLSPAN=3></TD</TR>";
			s += "<TR><TD COLSPAN=3><B>INPUT TERMINALS</B>:</TD</TR>";
			s += "<TR><TD><B>TERMINAL<BR>NUMBER</B></TD><TD><B>I2C PIN<BR></B></TD><TD><B>I2C CHIP</TD></TR>"
			;
		}
		//Addresses:
		String sChipAddress = "";
		String sI2CPin = "";
		String sTerminalNumber = "";
		for (int i = 0; i < SecuritySystem.getGPIOHandler().getNumberOfInputTerminals(); i++){
			sChipAddress = "0x" + Integer.toString(SecuritySystem.getGPIOHandler().getI2CInputChipAddress(i), 16);
			SecuritySystem.getGPIOHandler();
				sI2CPin = SecuritySystem.getGPIOHandler().getInputPinArray().get(i).getName();
				sTerminalNumber = SecuritySystem.getGPIOHandler().getInputTerminalArray().get(i);
				if (bPrintInHTML){
					s += "<TR>" 
						+ "<TD ALIGN=RIGHT>" +  sTerminalNumber + " </TD>"
						+ "<TD ALIGN=RIGHT>" +  sI2CPin + "</TD>" 
						+ "<TD ALIGN=RIGHT>" + sChipAddress + "</TD>"
						+ "</TR>"
					;
				}else{
					s += " |" 
							+ PadLeft(sTerminalNumber, " ", 9)
							+ " |" 
							+ PadLeft(sI2CPin, " ", 9) 
							+ " |"
							+ PadLeft(sChipAddress, " ", 9) 
							+ " |\n";
				}

		}
		if (!bPrintInHTML){
			s += " |================================|\n";
		}
		
		if (bPrintInHTML){
			s += "<TR><TD COLSPAN=3></TD</TR>";
			s += "<TR><TD COLSPAN=3></TD</TR>";
			s += "<TR><TD COLSPAN=3><B>OUTPUT TERMINALS</B>:</TD</TR>";
			s += "<TR><TD><B>TERMINAL<BR>NUMBER</B></TD><TD><B>I2C PIN<BR></B></TD><TD><B>I2C CHIP</TD></TR>"
			;
		}else{
			s += "\n";
			s += " OUTPUT TERMINALS:\n";
			s += " |================================|\n";
			s += " | TERMINAL | I2C PIN  | I2C CHIP |\n";
			s += " |   NUMBER |  NUMBER  |  ADDRESS |\n";
			s += " |================================|\n"
			;
		}
		

		//Addresses:
		for (int i = 0; i < SecuritySystem.getGPIOHandler().getNumberOfOutputTerminals(); i++){
			sChipAddress = "0x" + Integer.toString(SecuritySystem.getGPIOHandler().getI2COutputChipAddress(i), 16);
			SecuritySystem.getGPIOHandler();
				sI2CPin = SecuritySystem.getGPIOHandler().getOutputPinArray().get(i).getName();
				sTerminalNumber = SecuritySystem.getGPIOHandler().getOutputTerminalArray().get(i);
				
				if (bPrintInHTML){
					s += "<TR>" 
						+ "<TD ALIGN=RIGHT>" +  sTerminalNumber + " </TD>"
						+ "<TD ALIGN=RIGHT>" +  sI2CPin + "</TD>" 
						+ "<TD ALIGN=RIGHT>" + sChipAddress + "</TD>"
						+ "</TR>"
					;
				}else{
					s += " |" 
							+ PadLeft(sTerminalNumber, " ", 9)
							+ " |" 
							+ PadLeft(sI2CPin, " ", 9) 
							+ " |"
							+ PadLeft(sChipAddress, " ", 9) 
							+ " |\n";
				}

		}
		if (bPrintInHTML){
			s += "</TABLE>";
		}else{
			s += " |================================|\n";
		}

		return s;
	}
	public static void validatePassCode(String sPasscode) throws Exception {
		if (sPasscode.compareTo(SecuritySystem.getControllerPasscode()) == 0){
			return;
		}else{
			throw new Exception(SSConstants.RESPONSE_CODE_INVALID_PASSCODE
				+ SSConstants.QUERY_STRING_DELIMITER
				+ "Passcode is invalid"
			);
		}
	}

	public static void authenticateRequest(String sRequestString) throws Exception {
		
		String sPassCode = getKeyValue(sRequestString, SSConstants.QUERY_KEY_PASSCODE);
		
		if (sPassCode.compareToIgnoreCase("") == 0){
			throw new Exception(SSConstants.RESPONSE_CODE_PASSCODE_NOT_FOUND
				+ SSConstants.QUERY_STRING_DELIMITER
				+ "Passcode not found"
			);
		}
		
		try {
			validatePassCode(sPassCode);
		} catch (Exception e) {
			throw new Exception(e.getMessage());
		}
	
	}

	public static String buildControllerRequestToServerString(String sCommandString){
		//GET /sm/smas.ASAlarmListenerAction?user=airo&pw=xxxxxx&db=sm1 HTTP/1.1
		//host: localhost
		String s = "";
		
		s += "GET " + SSConstants.WEB_APP_LISTENER_CLASS 
			+ "?" 
			+ SSUtilities.buildKeyValuePair(SSConstants.QUERY_KEY_CONTROLLERNAME, SecuritySystem.getControllerName())
			+ SSConstants.QUERY_STRING_DELIMITER
			+ SSUtilities.buildKeyValuePair(SSConstants.QUERY_KEY_PASSCODE, SecuritySystem.getControllerPasscode())
			+ SSConstants.QUERY_STRING_DELIMITER
			+ SSUtilities.buildKeyValuePair(SSConstants.QUERY_KEY_DATABASEID, SecuritySystem.getDatabaseID())
			+ SSConstants.QUERY_STRING_DELIMITER + sCommandString
		;
		
		//Add the host name:
		//s += "host: " + sWebAppURL + "\n";
		return s;
	}
	
	public static String buildServerRequestToControllerString(
		String sCommandString,
		String sControllerName,
		String sControllerPasscode,
		String sUser,
		String sUserFullName,
		String sServerID
		) throws Exception{
		
		return SSUtilities.buildKeyValuePair(SSConstants.QUERY_KEY_CONTROLLERNAME, sControllerName)
			+ SSConstants.QUERY_STRING_DELIMITER
			+ SSUtilities.buildKeyValuePair(SSConstants.QUERY_KEY_PASSCODE, sControllerPasscode)
			+ SSConstants.QUERY_STRING_DELIMITER
			+ SSUtilities.buildKeyValuePair(SSConstants.QUERY_KEY_USER_ID, sUser)
			+ SSConstants.QUERY_STRING_DELIMITER
			+ SSUtilities.buildKeyValuePair(SSConstants.QUERY_KEY_USER_FULL_NAME, sUserFullName)
			+ SSConstants.QUERY_STRING_DELIMITER
			+ SSUtilities.buildKeyValuePair(SSConstants.QUERY_KEY_SERVER_ID, sServerID)
			+ SSConstants.QUERY_STRING_DELIMITER
			+ sCommandString
		;
	}

	public static String buildRequestFromServerToSetTerminalContacts(
			ArrayList<String>arrTerminalNumbers,
			ArrayList<String>arrContactStates,
			ArrayList<String>arrDurationsInMS,
			String sControllerName,
			String sControllerPasscode,
			String sUserName,
			String sUserFullName,
			String sServerID
			) throws Exception{
		
		String sCommandString = SSUtilities.buildKeyValuePair(
			SSConstants.QUERY_KEY_COMMAND, SSConstants.QUERY_KEYVALUE_COMMAND_SET_TERMINAL_CONTACT_STATUS);

		for (int i = 0; i < arrTerminalNumbers.size(); i++){
			sCommandString +=
    		//Active state
			SSConstants.QUERY_STRING_DELIMITER
    		+ SSUtilities.buildKeyValuePair(SSConstants.QUERY_KEY_TERMINALCONTACTSTATE_PREFIX + arrTerminalNumbers.get(i), arrContactStates.get(i))
    		
    		//Interval
    		+ SSConstants.QUERY_STRING_DELIMITER
    		+ SSUtilities.buildKeyValuePair(SSConstants.QUERY_KEY_TERMINALDURATIONINMS_PREFIX + arrTerminalNumbers.get(i), arrDurationsInMS.get(i));
		}
		
		return buildServerRequestToControllerString(
			sCommandString,
			sControllerName,
			sControllerPasscode,
			sUserName,
			sUserFullName,
			sServerID
			)
		;
	}
	
	public static String buildRequestFromServerToSetTerminalsListeningStatus(
			ArrayList<String>arrTerminalNumbers,
			ArrayList<String>arrListeningStates,
			String sControllerName,
			String sControllerPasscode,
			String sUser,
			String sUserFullName,
			String sServerID
			) throws Exception{
		String sCommandString = SSUtilities.buildKeyValuePair(
			SSConstants.QUERY_KEY_COMMAND, SSConstants.QUERY_KEYVALUE_COMMAND_SET_TERMINAL_LISTENING_STATUS);

		for (int i = 0; i < arrTerminalNumbers.size(); i++){
			sCommandString += 
				SSConstants.QUERY_STRING_DELIMITER
    		//Listening state
				+ SSUtilities.buildKeyValuePair(
				SSConstants.QUERY_KEY_TERMINALLISTENINGSTATE_PREFIX + arrTerminalNumbers.get(i), arrListeningStates.get(i));
		}
		
		try {
			return buildServerRequestToControllerString(
				sCommandString,
				sControllerName,
				sControllerPasscode,
				sUser,
				sUserFullName,
				sServerID)
			;
		} catch (Exception e) {
			throw new Exception("Error [1521133700] - " + e.getMessage());
		}
	}
	public static String buildRequestFromServerToGetInputTerminalsListeningStatus(
			String sControllerName,
			String sControllerPasscode,
			String sUser,
			String sUserFullName,
			String sServerID
			) throws Exception{
		String sCommandString = SSUtilities.buildKeyValuePair(
			SSConstants.QUERY_KEY_COMMAND, SSConstants.QUERY_KEYVALUE_COMMAND_GET_LISTENING_STATUS);

		try {
			return buildServerRequestToControllerString(
				sCommandString,
				sControllerName,
				sControllerPasscode,
				sUser,
				sUserFullName,
				sServerID)
			;
		} catch (Exception e) {
			throw new Exception("Error [1521133701] - " + e.getMessage());
		}
	}
	public static String buildRequestFromServerToGetOutputTerminalIntervalRemaining(
			String sControllerName,
			String sControllerPasscode,
			String sUser,
			String sUserFullName,
			String sServerID
			) throws Exception{
		String sCommandString = SSUtilities.buildKeyValuePair(
			SSConstants.QUERY_KEY_COMMAND, SSConstants.QUERY_KEYVALUE_COMMAND_GET_REMAINING_INTERVAL);

		try {
			return buildServerRequestToControllerString(
				sCommandString,
				sControllerName,
				sControllerPasscode,
				sUser,
				sUserFullName,
				sServerID)
			;
		} catch (Exception e) {
			throw new Exception("Error [1521133702] - " + e.getMessage());
		}
	}
	
	public static String buildRequestFromServerToGetTerminalsContactStatus(
			String sControllerName,
			String sControllerPasscode,
			String sUser,
			String sUserFullName,
			String sServerID
			) throws Exception{
		String sCommandString = SSUtilities.buildKeyValuePair(
			SSConstants.QUERY_KEY_COMMAND, SSConstants.QUERY_KEYVALUE_COMMAND_LISTING_TERMINAL_CONTACT_STATUS);

		try {
			return buildServerRequestToControllerString(
				sCommandString,
				sControllerName,
				sControllerPasscode,
				sUser,
				sUserFullName,
				sServerID)
			;
		} catch (Exception e) {
			throw new Exception("Error [1521133703] - " + e.getMessage());
		}
	}
	
	public static String buildRequestFromTelnetToFakeSetInputTerminalContacts(
			ArrayList<String>arrTerminalNumbers,
			ArrayList<String>arrContactStates,
			String sControllerName,
			String sControllerPasscode,
			String sUser,
			String sUserFullName,
			String sServerID
			) throws Exception{
		String sCommandString = SSUtilities.buildKeyValuePair(
			SSConstants.QUERY_KEY_COMMAND, SSConstants.QUERY_KEYVALUE_FAKE_SET_INPUT_CONTACT);

		for (int i = 0; i < arrTerminalNumbers.size(); i++){
			sCommandString += 
				SSConstants.QUERY_STRING_DELIMITER
    		//Listening state
				+ SSUtilities.buildKeyValuePair(
				SSConstants.QUERY_KEY_TERMINALCONTACTSTATE_PREFIX + arrTerminalNumbers.get(i), arrContactStates.get(i));
		}
		
		try {
			return buildServerRequestToControllerString(
				sCommandString,
				sControllerName,
				sControllerPasscode,
				sUser,
				sUserFullName,
				sServerID)
			;
		} catch (Exception e) {
			throw new Exception("Error [1521133704] - " + e.getMessage());
		}
	}

	public static String buildRequestFromServerToTerminateProgram(
			String sControllerName,
			String sControllerPasscode,
			String sUser,
			String sUserFullName,
			String sServerID
			) throws Exception{
		String sCommandString = SSUtilities.buildKeyValuePair(
			SSConstants.QUERY_KEY_COMMAND, SSConstants.QUERY_KEYVALUE_COMMAND_TERMINATE);

		try {
			return buildServerRequestToControllerString(
				sCommandString,
				sControllerName,
				sControllerPasscode,
				sUser,
				sUserFullName,
				sServerID)
			;
		} catch (Exception e) {
			throw new Exception("Error [1521133705] - " + e.getMessage());
		}
	}
	
	public static String buildRequestFromServerToRestartProgram(
			String sControllerName,
			String sControllerPasscode,
			String sUser,
			String sUserFullName,
			String sServerID
			) throws Exception{
		String sCommandString = SSUtilities.buildKeyValuePair(
			SSConstants.QUERY_KEY_COMMAND, SSConstants.QUERY_KEYVALUE_COMMAND_RESTART);

		try {
			return buildServerRequestToControllerString(
				sCommandString,
				sControllerName,
				sControllerPasscode,
				sUser,
				sUserFullName,
				sServerID)
			;
		} catch (Exception e) {
			throw new Exception("Error [1521133706] - " + e.getMessage());
		}
	}
	
	public static String buildRequestFromServerToGetControllerSystemInformation(
			String sControllerName,
			String sControllerPasscode,
			String sUser,
			String sUserFullName,
			String sServerID
			) throws Exception{
		String sCommandString = SSUtilities.buildKeyValuePair(
			SSConstants.QUERY_KEY_COMMAND, SSConstants.QUERY_KEYVALUE_COMMAND_REPORT_SYSTEMINFORMATION);

		try {
			return buildServerRequestToControllerString(
				sCommandString,
				sControllerName,
				sControllerPasscode,
				sUser,
				sUserFullName,
				sServerID)
			;
		} catch (Exception e) {
			throw new Exception("Error [1521133707] - " + e.getMessage());
		}
	}
	
	public static String buildRequestFromServerToGetLog(
			String sControllerName,
			String sControllerPasscode,
			String sUser,
			String sUserFullName,
			String sServerID
			) throws Exception{
		String sCommandString = SSUtilities.buildKeyValuePair(
			SSConstants.QUERY_KEY_COMMAND, SSConstants.QUERY_KEYVALUE_COMMAND_SEND_LOG);

		try {
			return buildServerRequestToControllerString(
				sCommandString,
				sControllerName,
				sControllerPasscode,
				sUser,
				sUserFullName,
				sServerID)
			;
		} catch (Exception e) {
			throw new Exception("Error [1521133708] - " + e.getMessage());
		}
	}
	
	public static String buildRequestFromServerToGetConfigFile(
			String sControllerName,
			String sControllerPasscode,
			String sUser,
			String sUserFullName,
			String sServerID
			) throws Exception{
		String sCommandString = SSUtilities.buildKeyValuePair(
			SSConstants.QUERY_KEY_COMMAND, SSConstants.QUERY_KEYVALUE_COMMAND_SEND_CONFIG_FILE);

		try {
			return buildServerRequestToControllerString(
				sCommandString,
				sControllerName,
				sControllerPasscode,
				sUser,
				sUserFullName,
				sServerID)
			;
		} catch (Exception e) {
			throw new Exception("Error [1521133709] - " + e.getMessage());
		}
	}
	
	public static String buildRequestFromServerToUpdateConfigFile(
			ArrayList<String>arrKeys,
			ArrayList<String>arrKeyValues,
			String sControllerName,
			String sControllerPasscode,
			String sUser,
			String sUserFullName,
			String sServerID
			) throws Exception{
		String sCommandString = SSUtilities.buildKeyValuePair(
			SSConstants.QUERY_KEY_COMMAND, SSConstants.QUERY_KEYVALUE_COMMAND_UPDATE_CONFIG_FILE);

		for (int i = 0; i < arrKeys.size(); i++){
			sCommandString += 
				SSConstants.QUERY_STRING_DELIMITER
    		//Set config file keys:
				+ SSUtilities.buildKeyValuePair(arrKeys.get(i), arrKeyValues.get(i));
		}
		
		try {
			return buildServerRequestToControllerString(
				sCommandString,
				sControllerName,
				sControllerPasscode,
				sUser,
				sUserFullName,
				sServerID)
			;
		} catch (Exception e) {
			throw new Exception("Error [1521133710] - " + e.getMessage());
		}
	}
	
	public static String buildRequestFromServerToDeleteControllerLogFile(
			String sControllerName,
			String sControllerPasscode,
			String sUser,
			String sUserFullName,
			String sServerID
			) throws Exception{
		String sCommandString = SSUtilities.buildKeyValuePair(
			SSConstants.QUERY_KEY_COMMAND, SSConstants.QUERY_KEYVALUE_COMMAND_DELETE_LOG_FILE);

		try {
			return buildServerRequestToControllerString(
				sCommandString,
				sControllerName,
				sControllerPasscode,
				sUser,
				sUserFullName,
				sServerID)
			;
		} catch (Exception e) {
			throw new Exception("Error [1521133711] - " + e.getMessage());
		}
	}
	
	public static String buildRequestFromServerToUpdateProgram(
			String sControllerName,
			String sControllerPasscode,
			String sUser,
			String sUserFullName,
			String sServerID
			) throws Exception{
		
		String sCommandString = SSUtilities.buildKeyValuePair(
			SSConstants.QUERY_KEY_COMMAND, SSConstants.QUERY_KEYVALUE_COMMAND_UPDATE_PROGRAM);

		sCommandString += 
			SSConstants.QUERY_STRING_DELIMITER
			+ SSUtilities.buildKeyValuePair(SSConstants.QUERY_KEY_UPDATE_PROGRAM_VERSION_AVAILABLE, SSConstants.CONTROLLER_VERSION)

			+ SSConstants.QUERY_STRING_DELIMITER
			+ SSUtilities.buildKeyValuePair(SSConstants.QUERY_KEY_PATH_TO_PROGRAM_UPDATE, SSConstants.WEB_APP_PATH_TO_PROGRAM_UPDATE)
			
			+ SSConstants.QUERY_STRING_DELIMITER
			+ SSUtilities.buildKeyValuePair(SSConstants.QUERY_KEY_PROGRAM_UPDATE_UNZIP_PASSWORD, SSConstants.PROGRAM_UPDATE_UNZIP_PASSWORD)
			;

		try {
			return buildServerRequestToControllerString(
				sCommandString,
				sControllerName,
				sControllerPasscode,
				sUser,
				sUserFullName,
				sServerID)
			;
		} catch (Exception e) {
			throw new Exception("Error [1521133712] - " + e.getMessage());
		}
	}
	
	public static String buildRequestFromServerToListSampleCommands(
			String sControllerName,
			String sControllerPasscode,
			String sUser,
			String sUserFullName,
			String sServerID
			) throws Exception{
		String sCommandString = SSUtilities.buildKeyValuePair(
			SSConstants.QUERY_KEY_COMMAND, SSConstants.QUERY_KEYVALUE_COMMAND_LIST_SAMPLE_TELNET_COMMANDS);

		try {
			return buildServerRequestToControllerString(
				sCommandString,
				sControllerName,
				sControllerPasscode,
				sUser,
				sUserFullName,
				sServerID)
			;
		} catch (Exception e) {
			throw new Exception("Error [1521133713] - " + e.getMessage());
		}
	}
	
	public static String buildRequestFromServerToListPinMappings(
			String sControllerName,
			String sControllerPasscode,
			String sUser,
			String sUserFullName,
			String sServerID
			) throws Exception{
		String sCommandString = SSUtilities.buildKeyValuePair(
			SSConstants.QUERY_KEY_COMMAND, SSConstants.QUERY_KEYVALUE_COMMAND_LIST_PIN_MAPPINGS);

		try {
			return buildServerRequestToControllerString(
				sCommandString,
				sControllerName,
				sControllerPasscode,
				sUser,
				sUserFullName,
				sServerID)
			;
		} catch (Exception e) {
			throw new Exception("Error [1521133714] - " + e.getMessage());
		}
	}
	
	public static String buildRequestFromServerToSetTestMode(
			String sControllerName,
			String sControllerPasscode,
			String sUser,
			String sUserFullName,
			String sRequestedTestMode,
			String sServerID
			) throws Exception{
		String sCommandString = SSUtilities.buildKeyValuePair(
			SSConstants.QUERY_KEY_COMMAND, SSConstants.QUERY_KEYVALUE_COMMAND_SET_TEST_MODE)
			+ SSConstants.QUERY_STRING_DELIMITER
			+ SSUtilities.buildKeyValuePair(SSConstants.QUERY_KEY_TEST_MODE_STATE, sRequestedTestMode)
		;

		try {
			return buildServerRequestToControllerString(
				sCommandString,
				sControllerName,
				sControllerPasscode,
				sUser,
				sUserFullName,
				sServerID)
			;
		} catch (Exception e) {
			throw new Exception("Error [1521133714] - " + e.getMessage());
		}
	}
	public static String buildRequestFromServerToGetTestMode(
			String sControllerName,
			String sControllerPasscode,
			String sUser,
			String sUserFullName,
			String sServerID
			) throws Exception{
		String sCommandString = SSUtilities.buildKeyValuePair(
			SSConstants.QUERY_KEY_COMMAND, SSConstants.QUERY_KEYVALUE_COMMAND_GET_TEST_MODE)
		;

		try {
			return buildServerRequestToControllerString(
				sCommandString,
				sControllerName,
				sControllerPasscode,
				sUser,
				sUserFullName,
				sServerID)
			;
		} catch (Exception e) {
			throw new Exception("Error [1521133715] - " + e.getMessage());
		}
	}
	public static String PadLeft(String sStr, String sPadChar, int iTotalStringLength){
		if (sStr.length()> iTotalStringLength){
			return StringLeft(sStr,iTotalStringLength);
		}

		String sResult = "";
		//System.out.println("SPADCHAR = " + sPadChar);
		for (int i = 0; i < iTotalStringLength - sStr.length(); i++){
			sResult += sPadChar;
			//System.out.println("sResult = " + sResult);
		}
		sResult += sStr;

		return sResult;
	}
	private static String StringLeft(String sSource, int iLength){

		if (iLength < 0){
			return sSource;
		}

		if (sSource.length() > iLength){
			return sSource.substring(0, iLength);
		}
		else{
			return sSource;
		}
	}
	public static void writeToFile(String sFileName, String sLineToWrite, boolean bAppend) throws Exception{

		BufferedWriter bw = null;
		try {
			// APPEND MODE SET HERE
			bw = new BufferedWriter(new FileWriter(sFileName, true));
			bw.write(sLineToWrite);
			bw.newLine();
			bw.flush();
		} catch (IOException e) {
			throw new Exception("Error [1463511330] writing to file '" + sFileName + "' - " + e.getMessage());
		} finally {                       // always close the file
			if (bw != null) try {
				bw.close();
			} catch (IOException ioe2) {
				// just ignore it
			}
		}
	}

	public static void deleteLogFile(String sLogFileName) throws Exception{
		try{
			File file = new File(sLogFileName);
			if(!file.delete()){
				throw new Exception("Error [1463515717] could not delete log file '" + sLogFileName);
			}
		}catch(Exception e){
			throw new Exception("Error [1463515718] deleting log file '" + sLogFileName);
		}
		//Add an entry to the new log indicating that the log file was removed:
		SecuritySystem.writeControllerLogEntry("Deleted diagnostic log", 1);
	}
	public static String executeSystemCommand(String sCommandWithOptions) throws Exception{
		String sResult = "";
		String sErrorString = "";
		int iExitValue = 0;
		try {

			Process process = Runtime.getRuntime().exec(sCommandWithOptions);
			iExitValue = process.waitFor();
			//if (iExitValue != 0) {
			//	throw new Exception("Error [1464012751] program exited abnormally with exit status: '" + Integer.toString(exitValue) + "'.");
			//}
			BufferedReader reader = new BufferedReader(
					new InputStreamReader(process.getInputStream()));
			BufferedReader stdError = new BufferedReader(new
					InputStreamReader(process.getErrorStream()));
			String line;
			String sError;
			while ((line = reader.readLine()) != null) {
				sResult += line + "\n";
			}
			while ((sError = stdError.readLine()) != null) {
				sErrorString += sError + "\n";
			}
			reader.close();
			stdError.close();
		}catch(Exception e){
			throw new Exception("Error [1465225327] executing command '" + sCommandWithOptions + "' - " + e.getMessage());
		}
		if (iExitValue != 0){
			throw new Exception("Error [1465225328] executing command '" + sCommandWithOptions + "' - " + sErrorString);
		}
		return sResult;
	}
}
