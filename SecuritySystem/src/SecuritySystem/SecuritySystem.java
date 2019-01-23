package SecuritySystem;

import java.text.SimpleDateFormat;
import java.util.Date;

import sscommon.SSConstants;
import sscommon.SSUtilities;

public class SecuritySystem {

	/*
	 * 
	 * To run as a runnable jar:
	 * java -jar securitysystem.jar -css.ini -f -o -m -e -p
		
	Query to pick up logged requests in SMCP:
	SELECT * FROM systemlog where soperation = "SSSYSTEMREQUEST" order by lid DESC limit 100
	
	//***************************************************************************
	 */
	
	/*
	 This program can 'hear' commands from the Listening server, and then it will process them, and then return an
	 acknowledgment code: successful or unsuccessful.
	 
	 It can also 'send' commands to the web app, and it waits to get an OK back after sending them, but that's all.  
	 If it sends a command to the web app that is supposed to return information, the web app will acknowledge the
	 command but send a new command string separately in a new socket as its detailed response.
	 
	 So at most, either side sends a 'command' string (which is either a command or a string of info), and gets a 
	 simple OK back in every case, and then the socket closes.
	 * 
	 */
	
	static boolean bOutDiagnosticsToCommandLine = true;
	
	//For debugging in Eclipse, we don't really call the gpio functions:
	static boolean bFakeAllGPIOCalls = false;
	
	private static boolean bOutputSampleCommands = false;
	
	private static boolean bOutputPinToTerminalMappings = false;
	
	private static boolean bUsePiGpioPins = false;
	
	private static boolean bUsingSystemd = false;
	private static ListeningService socketlistener = null;
	
	//If this gets set to true, it gets picked up in the main loop and restarts the controller program:
	private static boolean bRestartProgram = false;
	
	//Configuration file variables:
	private static String m_sConfFileFullName = "";
	private static String m_sControllerName = "";
	private static String m_sPiControllerPasscode = "";
	private static String m_sListeningPortNumber = "";
	private static String m_sWebAppURL = "";
	private static String m_sWebAppPortNumber = "";
	private static String m_sDatabaseID = "";
	private static int m_iLoggingLevel = 0;
	private static String m_sLogFile = "";
	private static boolean m_bTestModeRunning = false;
	static gpioHandler m_gpio;
	private static boolean m_bInitiateShutdown = false;
	private static String m_sShutdownMessage = "Program exit.";
	private static int m_iExitStatus = 0;
	
	//This variable keeps track of the last time we sent a request to the server, so that we don't send too many too fast and blow up the web app server:
	static long iRequestLastSentTime = 0;

	public static void main(String args[]) throws InterruptedException {
		//Delay start 
		Thread.sleep(SSConstants.MAIN_STARTING_DELAY_IN_MS);
		
		//Read the command line
		try {
			SSUtilities.processCommandLine(args);
		} catch (Exception e1) {
			System.out.println("Exiting program - " + e1.getMessage());
			System.exit(SSConstants.EXIT_ERROR_COULD_NOT_PROCESS_COMMAND_LINE);
		}

		//Read the config file
		try {
			SSUtilities.processConfigFile(m_sConfFileFullName);
		} catch (Exception e) {
			System.out.println("Exiting program - " + e.getMessage());
			System.exit(SSConstants.EXIT_ERROR_COULD_NOT_READ_CONFIG_FILE);
		}

		if (getLoggingLevel() > 0){
			String sCommandLine = "";
			for (int i = 0; i < args.length; i++){
				sCommandLine += args[i] + " ";
			}
			writeControllerLogEntry(
				"Starting program version " + SSConstants.CONTROLLER_VERSION + " with command line: '" + sCommandLine.trim() + "'",
				1);
			writeControllerLogEntry(
					"Read config file.",
					1);
		}

		if (bOutDiagnosticsToCommandLine){
			System.out.println("<--Security System--> START");
		}

		if (bOutputSampleCommands){
			try {
				System.out.println(SSUtilities.printSampleCommands());
			} catch (Exception e) {
				System.out.println("Error [1521132755] printing sample commands - " + e.getMessage());
			}
		}
		
		//Instantiate a 'GPIO Handler':
		m_gpio = new gpioHandler(bUsePiGpioPins);
		
		if (bOutputPinToTerminalMappings){
			System.out.println(SSUtilities.printPinToTerminalMappings(false));
		}
		
		//Tell it if we are faking calls to it or not:
		m_gpio.setFakingGPIOInput(bFakeAllGPIOCalls);
		
		//Provision the terminals in the pi:
		if (bOutDiagnosticsToCommandLine){
			System.out.println("Provisioning Terminals...");
		}
		try {
			m_gpio.provisionTerminals(bUsePiGpioPins);
		} catch (Exception e) {
			writeControllerLogEntry(
				"Error [1463513469] provisioning terminals - " + e.getMessage() + ".",
				1);
			System.out.println(e.getMessage());
			triggerShutdown("GPIO problem - " + e.getMessage(), SSConstants.EXIT_ERROR_GPIO_FAILURE);
		};
		
		if (bOutDiagnosticsToCommandLine){
			System.out.println(m_gpio.getsProvisionedTerminals());
		}

		//Start the listening server:
		startListeningServer(Integer.parseInt(m_sListeningPortNumber), bOutDiagnosticsToCommandLine);
		writeControllerLogEntry("Successfully started listening server.", 1);

		//Check with the web app to see what output terminals should have open or closed contacts, and which inputs should be 'listening':
		try {
			ServerRequests.initializeTerminalStatus();
		} catch (Exception e1) {
			writeControllerLogEntry(
				"Error [1463513471] initializing terminal status - " + e1.getMessage() + ".",
				1);

			System.out.println(e1.getMessage());
			//shutdown(e1.getMessage(), SSConstants.EXIT_ERROR_COULD_NOT_SEND_COMMAND);
		}
		writeControllerLogEntry(
			"Initialized terminal status.",
			1);
		
		
		//Let the web app know that we just started up, so it can take appropriate response:
		try {
			ServerRequests.notifyOnStartup();
		} catch (Exception e1) {
			writeControllerLogEntry(
				"Error [1486250926] notifying server of startup - " + e1.getMessage() + ".",
				1);

			System.out.println(e1.getMessage());
			//shutdown(e1.getMessage(), SSConstants.EXIT_ERROR_COULD_NOT_SEND_COMMAND);
		}
		writeControllerLogEntry(
			"Notified server of startup.",
			1);
		
		// keep program running until user aborts (CTRL-C)
		//int iTestCounter = 0;
		long lLastCheckInTime = System.currentTimeMillis();
		while(!m_bInitiateShutdown) {
				
			//Update the duration intervals for any output terminals:
			/*
			try {
				m_gpio.updateOutputTerminalDurations();
			} catch (Exception e) {
				//Don't blow it up - just report the error to the command line:
				if (bOutDiagnosticsToCommandLine){
					System.out.println(e.getMessage());
				}
			}
			*/
			if (bOutDiagnosticsToCommandLine){	
				//System.out.println("Looping every " 
				//	+ Long.toString(SSConstants.MAIN_LOOP_EXECUTION_SLEEP_INTERVAL_IN_MS) + " ms....");

			}
			Thread.sleep(SSConstants.MAIN_LOOP_EXECUTION_SLEEP_INTERVAL_IN_MS);

			//Check in with the server on a periodic basis:
			long lMillisecondsSinceCheckIn = System.currentTimeMillis() - lLastCheckInTime;
			if (lMillisecondsSinceCheckIn > (SSConstants.CONTROLLER_CHECK_IN_PERIOD_IN_MINUTES * 60000L)){
				lLastCheckInTime = System.currentTimeMillis();
				try {
					ServerRequests.checkInWithServer();
				} catch (Exception e1) {
					writeControllerLogEntry(
						"Error [1465323078] checking in with server - " + e1.getMessage() + ".",
						1);
				}
			}
			//This code tests the frequency of trigger event requests:
			//if (iTestCounter <= 20){
			//	try {
			//		reportPinTriggerEvent("pc=p123&INTERVAL=" + Integer.toString(SSConstants.MAIN_LOOP_EXECUTION_SLEEP_INTERVAL_IN_MS) + "&TRIGGER=" + Integer.toString(iTestCounter));
			//	} catch (Exception e) {
			//		System.out.println("oops");
			//	}
			//iTestCounter++;
			//}
			if(bRestartProgram){
				try {
					restartProgram(bUsingSystemd);
				} catch (Exception e) {
					writeControllerLogEntry("Error restarting program - " + e.getMessage(), 1);
				}
			}
		}

		shutdown(m_sShutdownMessage, m_iExitStatus);
		
		// stop all GPIO activity/threads by shutting down the GPIO controller
		// (this method will forcefully shutdown all GPIO monitoring threads and scheduled tasks)
		// gpio.shutdown();   <--- implement this method call if you wish to terminate the Pi4J GPIO controller        
	}

	private static void startListeningServer(int port, boolean bOutputLoggingIsOn) {
		// Try to open a server socket on the given port
		socketlistener = new ListeningService(port, bOutDiagnosticsToCommandLine);
		new Thread(socketlistener).start();
	}
	
	public static void listeningServerExceptionHandler(String sException){
		shutdown(sException, SSConstants.EXIT_ERROR_LISTENING_SERVICE_FAILED);
	}
	
	public static void restartListeningServer() throws Exception{
		socketlistener.stopListening();
		startListeningServer(Integer.parseInt(m_sListeningPortNumber), bOutDiagnosticsToCommandLine);
	}
	
	private static void shutdown(String sShutdownMessage, int iExitStatus){
		//Call gpio unprovisioning here...
		m_gpio.shutdownGPIO();
		System.out.println("Exiting program - " + sShutdownMessage);
		writeControllerLogEntry(
			"Exiting program - " + sShutdownMessage + ".",
			1);

		System.exit(iExitStatus);
	}
	public static void writeControllerLogEntry(
			String sEntry, 
			int iEntryLogLevel){
			
			if ((getLogFile() == null) || (getLogFile().compareToIgnoreCase("") == 0)){
				return;
			}
			if (getLoggingLevel() < iEntryLogLevel){
				return;
			}
			
			//Truncate very long entries:
			if (sEntry.length() > SSConstants.MAXIMUM_LOG_ENTRY_LENGTH){
				sEntry = sEntry.substring(0, SSConstants.MAXIMUM_LOG_ENTRY_LENGTH) + "...";
			}
			
			String sLineToWrite = 
				System.currentTimeMillis()
				+ " "
				+ new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new Date())
				+ ": "
				+ sEntry.replace("\n", "\\n")
			;
			try {
				SSUtilities.writeToFile(getLogFile(), sLineToWrite, true);
			} catch (Exception e) {
				System.out.println("Error [1463512984] could not write to log - " + e.getMessage());
			}
		}
	public static void triggerShutdown(String sExitMessage, int iExitStatus){
		m_sShutdownMessage = sExitMessage;
		m_iExitStatus = iExitStatus;
		m_bInitiateShutdown = true;
	}
	
	public static void restartProgram(boolean bSystemdRestart) throws Exception{
		//In case this fails, we don't want to keep trying to do it:
		setRestartFlag(false);
		
		String sCommand = "";
		if(bSystemdRestart) {
			sCommand = SSConstants.RESTART_PROGRAM_COMMAND_SYSTEMD;
		}else {
			sCommand = SSConstants.RESTART_PROGRAM_COMMAND_INITD;
		}

		writeControllerLogEntry(
				"[1547063484] - Attempting to restart securitysystem with command: '" + sCommand + "'.",
				1);
		try {
			SSUtilities.executeSystemCommand(sCommand);
		} catch (Exception e) {
			SecuritySystem.writeControllerLogEntry(
					"Error [1464099626] executing '" + sCommand + "' - " + e.getMessage(), 
					1
					);
			throw new Exception("Error [1464099626] executing '" + sCommand + "' - " + e.getMessage());
		}
	}

	//Gets and Sets:
	public static void setConfigFileFullName(String sFileName){
		m_sConfFileFullName = sFileName;
	}
	public static String getConfigFileFullName(){
		return m_sConfFileFullName;
	}
	public static void setControllerName(String sName){
		m_sControllerName = sName;
	}
	public static String getControllerName(){
		return m_sControllerName;
	}
	public static void setControllerPasscode(String sPC){
		m_sPiControllerPasscode = sPC;
	}
	public static String getControllerPasscode(){
		return m_sPiControllerPasscode;
	}
	public static void setListeningPortNumber(String sPort){
		m_sListeningPortNumber = sPort;
	}
	public static String getListeningPortNumber(){
		return m_sListeningPortNumber;
	}
	public static void setWebAppURL(String sURL){
		m_sWebAppURL = sURL;
	}
	public static String getWebAppURL(){
		return m_sWebAppURL;
	}
	public static void setWebAppPortNumber(String sWebAppPort){
		m_sWebAppPortNumber = sWebAppPort;
	}
	public static String getWebAppPortNumber(){
		return m_sWebAppPortNumber;
	}
	public static void setDatabaseID(String sDBID){
		m_sDatabaseID = sDBID;
	}
	public static String getDatabaseID(){
		return m_sDatabaseID;
	}
	public static void setLoggingLevel(String sLoggingLevel) throws Exception{
		try {
			m_iLoggingLevel = Integer.parseInt(sLoggingLevel);
		} catch (Exception e) {
			throw new Exception("Error [1463511662] - invalid logging level: '" + sLoggingLevel + "'.");
		}
	}
	public static int getLoggingLevel(){
		return m_iLoggingLevel;
	}
	public static void setLogFile(String sLogFile){
		m_sLogFile = sLogFile;
	}
	public static String getLogFile(){
		return m_sLogFile;
	}
	public static void setOutputDiagnostics(boolean bOutputDiagnostics){
		bOutDiagnosticsToCommandLine = bOutputDiagnostics;
	}
	public static boolean getOutputDiagnostics(){
		return bOutDiagnosticsToCommandLine;
	}
	public static void setUsingSystemd(boolean bUseSystemd){
		bUsingSystemd = bUseSystemd;
	}
	public static boolean getSytsyemInitType(){
		return bUsingSystemd;
	}
	public static void setPrintSampleCommands(boolean bPrintSampleCommands){
		bOutputSampleCommands = bPrintSampleCommands;
	}
	public static void setPrintPinToTerminalMappings(boolean bPrintPinToTerminalMappings){
		bOutputPinToTerminalMappings = bPrintPinToTerminalMappings;
	}
	public static void setFakeAllGPIO(boolean bFakeAllGPIO){
		bFakeAllGPIOCalls = bFakeAllGPIO;
	}
	public static void setUsePiGpioPins(boolean bUsingPiGpioPins){
		bUsePiGpioPins = bUsingPiGpioPins;
	}
	public static gpioHandler getGPIOHandler(){
		return m_gpio;
	}
	public static void setRestartFlag(boolean bRestart){
		bRestartProgram = bRestart;
	}
	public static boolean getRestartFlag(){
		return bRestartProgram;
	}
	public static void setTestModeRunning(boolean bTestModeRunning){
		m_bTestModeRunning = bTestModeRunning;
	}
	public static boolean getTestModeRunning(){
		return m_bTestModeRunning;
	}
}
