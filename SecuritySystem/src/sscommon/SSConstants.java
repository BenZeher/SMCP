package sscommon;

public class SSConstants {

	/*
	 Some basic definitions:
	 
	 TRIGGER - when an input pin goes to 'HIGH', which would happen if a sensor or door switch gets activated.
	 
	 PIN - this refers to any of the connection pins on the Raspberry Pi.  These are typically connected, directly or through
	 relays, to the 'terminals' in the controller.
	 
	 TERMINAL - this refers to any of the connectors on the controller.  The various devices get wired directly to the terminals.
	 
	 LISTENING - an input pin is 'listening' when it is connected to a 'listener', which will call a trigger handler in the program.  
	 Until a pin is 'listening', the pi will catch it, but it won't make a call to the main program to tell it anything has happened.  
	 'Listening' doesn't apply to output pins.
	 
	 ARM - a zone can be 'armed' or not.  'Armed' means that the devices in that zone are 'listening', and when a device in the zone
	 is 'triggered', it puts the zone in an 'ALARMED' condition.
	 
	 BREACHED - a zone that is 'ARMED' is 'BREACHED' once a device in it has been triggered, and it stays 'BREACHED' until it is
	 'DE-ALARMED' or some timed procedure resets it.
	 
	 ACTIVATE - output pins can be 'activated' to do things like light a light or sound an alarm bell - to 'activate', we just
	 close the contacts of the out pin pair.
	 
	 ACTIVATED - input pins can be 'active' (or 'activated') or not.  A motion detector is 'active' after it has been triggered.
	 A door is 'active' if it is open.  Active means that the input pins are NOT in their 'normal' state.  If the pins (terminals)
	 are NORMALLY CLOSED, then if they are OPEN, they are 'ACTIVE'  If they are normally open, then if they are open, they are 
	 INACTIVE and if they are closed, they are ACTIVE.
	 
	 PROVISION - this is something that the pi4j functions do when the class initializes - it internally sets up the pins, one by one,
	 to make them available for use.  We try to 'unprovision' whenever the program closes.
	 
	 PASSCODE - this is the password, particular to the pi unit, that is passed back and forth between the pi and the web app in
	 all communications to verify that each is talking to the other.
	 
	 UNIT IDENTIFIER - this is the unique ID of the particular pi, which is passed up to the web app so it knows which pi is reporting.
	 It is also stored in the 'units' table in the web app to identify the individual pis.
	 
	 */
	
	/* NOTE about how the controller update files work and how to build a new version of the controller program (securitysystem.jar):
	​So the program (securitysystem.jar) gets renamed to securitysystem.new, and then it gets zipped up with a password, and finally it gets stored in a folder on the tomcat server at: 
	
	/tomcat/webapps/sm/ss/
	
	When the server tells the controller to update, the controller pulls that file down (with wget), puts it in the program folder, then it renames the current securitysystem.jar to securitysystem.old, renames the updated version (securitysystem.new) to securitysystem.jar, and then finally restarts itself by running:
	
	/etc/init.d/securitysystem restart
	
	So you have to make a couple changes so you compile the program with the update now - here they are:
	
	1) Create a local folder on your computer called /opt/securitysystem - this will hold the 'securitysystem.new', when you build it.
	
	sudo mkdir /opt/securitysystem
	sudo chmod 777 /opt/securitysystem
	
	2) Create a folder in your tomcat webapp to hold the update files (you can skip this step if you just install my latest WAR file (attached)):
	
	sudo mkdir /tomcat/webapps/sm/ss
	
	3) Create a new script called /usr/sbin/zip_ss_controller_jar.sh:
	
	sudo vim /usr/sbin/zip_ss_controller_jar.sh
	
	then put this in it:
	
	#!/bin/bash
	cd /opt/securitysystem
	zip -P sspw001 /tomcat/webapps/sm/ss/securitysystem.zip securitysystem.new
	
	Save it and exit.
	
	4) Add that script to your 'compile' script (/usr/sbin/update_all_classes.sh):
	
	sudo vim /usr/sbin/update_all_classes.sh
	
	And add this line just before the shutdown line:
	
	sh /usr/sbin/zip_ss_controller_jar.sh
	
	Save it.
	
	5) Whenever you build the program, if there are changes to the controller software or version, make sure you 'export SecuritySystem to a runnable jar' and save it as /opt/securitysystem/securitysystem.new'.  Then run the 'update_all' script AFTER exporting, and that takes care of all of it.

	 */

	//*******************************************************************************
	//Current version of this controller software:
	public static final String CONTROLLER_VERSION = "65";
	//*******************************************************************************

	
	//*******************************************************************************
	//Config file constants:
	public static String CONFFILE_KEY_VALUE_DELIMITER = "=";
	public static String CONFFILE_KEY_PASSCODE = "CONTROLLER_PASSCODE";
	public static int CONFFILE_KEY_PASSCODE_MAX_LENGTH = 32;
	public static String CONTROLLER_PASSWORD_LABEL = "Pi controller password";
	public static String CONFFILE_KEY_NAME = "CONTROLLER_NAME";
	public static int CONFFILE_KEY_NAME_MAX_LENGTH = 32;
	public static String CONTROLLER_NAME_LABEL = "Pi controller name";
	public static String CONFFILE_LISTENING_PORT = "LISTENING_PORT";
	public static int CONFFILE_LISTENING_PORT_MAX_LENGTH = 5;
	public static String LISTENING_PORT_LABEL = "Listening port number";
	public static String CONFFILE_WEB_APP_URL = "WEB_APP_URL";
	public static int CONFFILE_WEB_APP_URL_MAX_LENGTH = 72;
	public static String WEB_APP_URL_LABEL = "Web app URL";
	public static String CONFFILE_WEB_APP_PORT = "WEB_APP_PORT";
	public static int CONFFILE_WEB_APP_PORT_MAX_LENGTH = 5;
	public static String WEB_APP_PORT_LABEL = "Web app port";
	public static String CONFFILE_DATABASE_ID = "DBID";
	public static int CONFFILE_DATABASE_ID_MAX_LENGTH = 32;
	public static String DATABASE_ID_LABEL = "Database ID";
	public static String CONFFILE_LOGGING_LEVEL = "LOGGING_LEVEL";
	public static int CONFFILE_LOGGING_LEVEL_MAX_LENGTH = 1;
	public static String LOGGING_LEVEL_LABEL = "Logging level";
	public static String CONFFILE_LOG_FILE = "LOG_FILE";
	public static int CONFFILE_LOG_FILE_MAX_LENGTH = 72;
	public static String LOG_FILE_LABEL = "Log file";
	public static String CONFIG_KEY_LIST[]= {
		CONFFILE_KEY_PASSCODE, 
		CONFFILE_KEY_NAME,
		CONFFILE_LISTENING_PORT,
		CONFFILE_WEB_APP_URL,
		CONFFILE_WEB_APP_PORT,
		CONFFILE_DATABASE_ID,
		CONFFILE_LOGGING_LEVEL,
		CONFFILE_LOG_FILE
	};
	
	//**********************************************************************************
	
	//Command line constants:
	//This option is followed by a space and then the full path name of the config file:
	public static String COMMAND_LINE_OPTION_CONFIG_FILE = "-c";
	//This is used, by itself to tell the program to ignore the actual gpio calls - used for testing in Eclipse, etc.
	public static String COMMAND_LINE_OPTION_FAKE_GPIO = "-f";
	//This is used, by itself, to tell the program to output diagnostic info to stdout:
	public static String COMMAND_LINE_OPTION_OUTPUT_DIAGNOSTICS = "-o";
	//This is used to print examples of commands used to communicate with the controller:
	public static String COMMAND_LINE_OPTION_SHOW_SAMPLE_COMMANDS = "-e";
	//This is used to print the pin-to-terminal mapping of the program:
	public static String COMMAND_LINE_OPTION_SHOW_PIN_TO_TERMINAL_MAPPING = "-m";
	//This is used if I2C i/o expansion chips are NOT being used. 
	public static String COMMAND_LINE_OPTION_USE_PI_GPIO_PINS = "-p";
	
	//**********************************************************************************
	//Main Program constants:
	//Main starting delay time:
	public static int MAIN_STARTING_DELAY_IN_MS = 15000;
	
	//Main execution loop sleep duration:
	public static int MAIN_LOOP_EXECUTION_SLEEP_INTERVAL_IN_MS = 50;
	
	//Number of minutes between periodic controller 'check in's with web app:
	public static long CONTROLLER_CHECK_IN_PERIOD_IN_MINUTES = 10;
	
	//Minimum time between sending requests to the server:
	public static int MINIMUM_REQUEST_INTERVAL = 1000;
	
	//Web App class to call for sending queries, etc:
	public static String WEB_APP_LISTENER_CLASS = "/sm/smas.ASAlarmListenerAction";
	
	//Client service, for making requests back to the web app:
	public static int CLIENT_SOCKET_MAX_TIME_TO_BLOCK_IN_SECONDS = 10;
	
	//Default listening port for controllers:
	public static String DEFAULT_CONTROLLER_LISTENING_PORT = "19435";
	
	//Used for testing and diagnostics in the SMCP program:
	public static boolean OUTPUT_DIAGNOSTIC_MESSAGES = false;
	
	//Default program folder:
	public static final String DEFAULT_PROGRAM_FOLDER_ON_CONTROLLER = "/opt/securitysystem";
	
	//Entries to the log that are longer than this length will be truncated:
	public static final int MAXIMUM_LOG_ENTRY_LENGTH = 1024;
	
	//Path on the web server to the updated controller program files:
	public static final String WEB_APP_PATH_TO_PROGRAM_UPDATE = "/sm/ss/securitysystem.zip";
	
	//Password to unzip the updated controller software program:
	public static final String PROGRAM_UPDATE_UNZIP_PASSWORD = "sspw001";
	
	//Name of controller program:
	public static final String PROGRAM_UPDATE_JAR_FILE = "securitysystem.jar";
	
	//Name of downloaded controller software update:
	public static final String PROGRAM_UPDATE_ZIP_FILE = "securitysystem.zip";
	
	//Name of old controller software update:
	public static final String PROGRAM_UPDATE_ZIP_FILE_OLD = "securitysystem.old";
	
	//Name of new controller software update:
		public static final String PROGRAM_UPDATE_ZIP_FILE_NEW = "securitysystem.new";
		

	
	//Name of script on controller that restarts the controller program:
	public static final String PROGRAM_UPDATE_SCRIPT_ON_CONTROLLER = "update.sh";
	
	//Command to restart program:
	public static final String RESTART_PROGRAM_COMMAND = "/etc/init.d/securitysystem restart";

	
	//***************************************************************************************
	
	//Default output pin interval (how long the alarm will sound without manually stopping it)
	public static String DEFAULT_MOMENTARY_CONTACT_ACTIVATE_INTERVAL_IN_MS = "500"; //ms
	
	//Constant to parse Pin Number from GpioPin Object
	public static String PARSE_GPIO_PIN = "GPIO "; 
	
	//Debounce time(ms) for all states
	public static int INPUT_DEBOUNCE_TIME = 200; 
	
	//*********************************************************************************
	
	//QUERY CONSTANTS:
	//Query string command constants:
	public static String QUERY_STRING_DELIMITER = "&";
	public static String QUERY_STRING_EQUALS_SYMBOL = "=";
	//This is used to embed the 'equals' sign in query strings:
	public static String QUERY_EQUALS_SIGN_MASK = "*EQ*";
	
	//We have query KEYS and query KEY VALUES:
	
	//QUERY KEYS:
	public static String QUERY_KEY_TERMINALCONTACTSTATE_PREFIX = "TERMINALCONTACTSTATE"; //the terminal number gets added to the key, e.g. '&TERMINALACTIVESTATE09=1'
	public static String QUERY_KEY_TERMINALDURATIONINMS_PREFIX = "TERMINALINTERVAL";//the terminal number gets added to the key, and the key value is the duration the key should be active
	public static String QUERY_KEY_TERMINALLISTENINGSTATE_PREFIX = "TERMINALLISTENINGSTATE"; //the terminal number gets added to the key, e.g. '&TERMINALLISTENINGSTATE09=1'
	public static String QUERY_KEY_TERMINALCONTACTSTATECHANGE_PREFIX = "TERMINALCONTACTCHANGE"; //the terminal number gets added to the key, e.g. '&TERMINALTRIGGERSTATE09=1'
	//Carries the interval time remaining in ms for any activated output terminals:
	public static String QUERY_KEY_TERMINALREMAININGINTERVAL_PREFIX = "TERMINALINTERVALREMAINING";
//																				
	public static String QUERY_KEY_PASSCODE = "PC"; //passes the passcode
	public static String QUERY_KEY_COMMAND = "COMMAND"; //passes the actual command to be processed
	public static String QUERY_KEY_CONTROLLERNAME = "CONTROLLERNAME"; //passes the controller name
	public static String QUERY_KEY_DATABASEID = "DBID"; //passes the database ID, i.e., which company database to use on the server
	//Reports the controller software version:
	public static String QUERY_KEY_VERSION = "VERSION";
	//Carries the contents of the controller's log file:
	public static String QUERY_KEY_LOGFILE_CONTENTS = "LOGFILECONTENTS";
	//Carries the contents of the controller's config file:
	public static String QUERY_KEY_CONFIGFILE_CONTENTS = "CONFIGFILECONTENTS";
	//Carries the list of sample commands:
	public static String QUERY_KEY_SAMPLE_COMMAND_CONTENTS = "SAMPLECOMMANDCONTENTS";
	//Carries the list of pin mappings:
	public static String QUERY_KEY_PIN_MAPPING_LIST = "LISTOFPINMAPPINGS";
	//Indicates the version of the program that a PROGRAM UPDATE command will update to:
	public static String QUERY_KEY_UPDATE_PROGRAM_VERSION_AVAILABLE = "AVAILABLEPROGRAMVERSION";
	//Carries the path to the updated version of the program that the controller will retrieve:
	public static String QUERY_KEY_PATH_TO_PROGRAM_UPDATE = "PATHTOPROGRAMUPDATE";
	//Carries the password to unzip the updated program file:
	public static String QUERY_KEY_PROGRAM_UPDATE_UNZIP_PASSWORD = "PROGRAMUPDATEUNZIPPASSWORD";
	//Carries the usernid of the person who initiated the request:
	public static String QUERY_KEY_USER_ID = "USERID";
	//Carries the full name of the person who initiated the request:
	public static String QUERY_KEY_USER_FULL_NAME = "USERFULLNAME";
	//Carries the name of the server that initiated the request:
	public static String QUERY_KEY_SERVER_ID = "SERVERID";
	//Carries the test mode toggle (i.e. ON or OFF):
	public static String QUERY_KEY_TEST_MODE_STATE = "TESTMODESTATE";
	//Carries the hostname of the pi:
	public static String QUERY_KEY_CONTROLLER_HOSTNAME = "CONTROLLERHOSTNAME";

	//Response parameters:
	public static String QUERY_KEY_ACKNOWLEDGMENT = "ACK"; //This key is in a response string and its value tells if the communication
															// was successful or not
	public static String QUERY_KEY_REQUEST_ERROR = "REQUESTERROR";  //The value of this key carries the error when the listener can't execute the requested command
	
	//Responses to the requesting client:
	public static String QUERY_KEYVALUE_ACKNOWLEDGMENT_SUCCESSFUL = "OK";
	public static String QUERY_KEYVALUE_ACKNOWLEDGMENT_UNSUCCESSFUL = "FAIL";
	
	//QUERY KEY VALUES:
	
	//COMMANDS:
	
	//COMMANDS FROM CONTROLLER TO SERVER:
	//Tells the web app to process a just received 'trigger' event (a sensor was activated) - also passes the pin number and trigger state:
	public static String QUERY_KEYVALUE_COMMAND_PROCESS_TRIGGER = "PROCESSTRIGGER";
	//This command is passed from the controller to the web app, so the web app can tell the controller what listening state
	//the pins SHOULD be in, and the controller will then modify them if necessary - it passes the pin number and the listening state the pin should be in:
	public static String QUERY_KEYVALUE_COMMAND_GET_LISTENING_STATUS = "GETLISTENINGSTATUS";
	//This command is passed from the controller to the web app, so the web app can tell the controller what active state
	//the pins SHOULD be in, and the controller will then modify them if necessary - it passes the pin number and the active state the pin should be in:
	public static String QUERY_KEYVALUE_COMMAND_GET_CONTACT_STATUS = "GETCONTACTSTATUS";
	//This command precedes a list of the listening statuses of the pins.  It's sent from the controller to the web app in response
	//to the web apps 'QUERY_KEYVALUE_CONFIRM_TERMINAL_LISTENING_STATUS' request:
	public static String QUERY_KEYVALUE_COMMAND_LISTING_TERMINAL_LISTENING_STATUS = "LISTINGLISTENINGSTATUS";
	//This command precedes a list of the active statuses of the pins.  It's sent from the controller to the web app in response
	//to the web apps 'QUERY_KEYVALUE_CONFIRM_TERMINAL_ACTIVE_STATUS' request:
	public static String QUERY_KEYVALUE_COMMAND_LISTING_TERMINAL_CONTACT_STATUS = "LISTINGCONTACTSTATUS";
	//This command asks to get the interval durations remaining for all the output pins.  IF any output terminal is active, then the contacts are either permanently CLOSED
	// or it's on an 'interval'.  If it's on an interval, the controller will tell the current amount of time (ms) remaining on that interval for each terminal:
	public static String QUERY_KEYVALUE_COMMAND_GET_REMAINING_INTERVAL = "GETREMAININGINTERVAL";
	//This command is just a 'check in' - the controller regularly checks in with the server.  The web app on the server
	//can use this to run any kind of regular procedures that might be timed, etc.
	public static String QUERY_KEYVALUE_COMMAND_CONTROLLER_CHECK_IN = "CONTROLLERCHECKIN";
	//This command tells the server that the controller has just started up, so the server can take any action, such as forcing it to close contacts based on a running schedule, etc.:
	public static String QUERY_KEYVALUE_COMMAND_CONTROLLER_STARTUP = "CONTROLLERSTARTUP";
	
	
	//COMMANDS FROM SERVER TO CONTROLLER:
	//Tells the controller to set a pin's listening status to listening or not listening - also passes the requested listening state:
	public static String QUERY_KEYVALUE_COMMAND_SET_TERMINAL_LISTENING_STATUS = "SETTERMINALLISTENINGSTATUS";
	//Tells the controller to activate or deactivate a pin (e.g., turning on the alarm bell) - also passes the requested activation state
	// and interval if state is 1 (high):
	public static String QUERY_KEYVALUE_COMMAND_SET_TERMINAL_CONTACT_STATUS = "SETTERMINALCONTACTSTATUS"; 
	//Tells the controller to delete its current log file:
	public static String QUERY_KEYVALUE_COMMAND_DELETE_LOG_FILE = "DELETELOGFILE";
	//Tells the controller to kill the program:
	public static String QUERY_KEYVALUE_COMMAND_TERMINATE = "TERMINATE";
	//Tells the controller to restart the program:
	public static String QUERY_KEYVALUE_COMMAND_RESTART = "RESTARTPROGRAM";
	//Tells the controller to report its version number:
	public static String QUERY_KEYVALUE_COMMAND_REPORT_SYSTEMINFORMATION = "REPORTVERSION";
	//Tells the controller to return the text of the log file:
	public static String QUERY_KEYVALUE_COMMAND_SEND_LOG = "SENDLOG";
	//Tells the controller to return the text of the log file:
	public static String QUERY_KEYVALUE_COMMAND_SEND_CONFIG_FILE = "SENDCONFIGFILE";
	//Tells the controller to update its config file with the values passed in the query string:
	public static String QUERY_KEYVALUE_COMMAND_UPDATE_CONFIG_FILE = "UPDATECONFIGFILE";
	//Tells the controller to update its program with the filename passed in the query string:
	public static String QUERY_KEYVALUE_COMMAND_UPDATE_PROGRAM = "UPDATEPROGRAM";
	//Tells the controller to list out the sample telnet commands:
	public static String QUERY_KEYVALUE_COMMAND_LIST_SAMPLE_TELNET_COMMANDS = "LISTSAMPLETELNETCOMMANDS";
	//Tells the controller to print out the pin/terminal mappings:
	public static String QUERY_KEYVALUE_COMMAND_LIST_PIN_MAPPINGS = "LISTPINMAPPINGS";
	//Tells the controller to toggle itself in and out of 'TEST' mode:
	public static String QUERY_KEYVALUE_COMMAND_SET_TEST_MODE = "SETTESTMODE";
	//Tells the controller to report its current 'test mode' (is the test mode ON or OFF now):
	public static String QUERY_KEYVALUE_COMMAND_GET_TEST_MODE = "GETTESTMODE";

	//DIAGNOSTICS - can be typed into the controller from a telnet prompt:
	//This command is just for testing and 'fakes' a trigger event on a sensor to tell the controller that a pin was triggered
	//To use it, telnet into the pi and enter the command.
	public static String QUERY_KEYVALUE_REPORT_CONTACT_CHANGE = "REPORTCONTACTCHANGE";
	//This command can be typed into the controller from a telnet prompt to 'fake' setting an input terminal's contact status (to closed or open):
	public static String QUERY_KEYVALUE_FAKE_SET_INPUT_CONTACT = "FAKESETINPUTCONTACT";
	
	//OTHER KEY VALUES:
	public static String QUERY_KEYVALUE_TERMINAL_LISTENING_STATUS_NOTLISTENING = "NOTLISTENING";
	public static String QUERY_KEYVALUE_TERMINAL_LISTENING_STATUS_LISTENING = "LISTENING";
	public static String QUERY_KEYVALUE_TERMINAL_CONTACT_STATUS_CLOSED = "CLOSED";
	public static String QUERY_KEYVALUE_TERMINAL_CONTACT_STATUS_OPEN = "OPEN";
	public static String QUERY_KEYVALUE_TERMINAL_EVENT_CONTACTS_OPENED = "OPENED";
	public static String QUERY_KEYVALUE_TERMINAL_EVENT_CONTACTS_CLOSED = "CLOSED";
	public static String QUERY_KEYVALUE_TEST_MODE_ON = "ON";
	public static String QUERY_KEYVALUE_TEST_MODE_OFF = "OFF";

	//************************************************************************************************************************
	//Response constants

	//Response error codes:
	public static int RESPONSE_CODE_NONE = 0;
	public static int RESPONSE_CODE_UNRECOGNIZED_COMMAND = 1;
	public static int RESPONSE_CODE_COULD_NOT_SET_LISTENING_STATUS = 2;
	public static int RESPONSE_CODE_COULD_NOT_SET_CONTACT_STATUS = 3;
	public static int RESPONSE_CODE_COULD_NOT_GET_LISTENING_STATUS = 4;
	public static int RESPONSE_CODE_PASSCODE_NOT_FOUND = 5;
	public static int RESPONSE_CODE_INVALID_PASSCODE = 6;
	public static int RESPONSE_CODE_UNPARSEABLE_COMMAND = 7;
	
	//***************************************************************************************************************************
	
	//Exit status codes:
	public static int EXIT_NORMAL = 0;
	public static int EXIT_ERROR_COULD_NOT_SEND_COMMAND = 1;
	public static int EXIT_ERROR_COULD_NOT_PROCESS_COMMAND_LINE = 2;
	public static int EXIT_ERROR_COULD_NOT_READ_CONFIG_FILE = 3;
	public static int EXIT_ERROR_LISTENING_SERVICE_FAILED = 4;
	public static int EXIT_ERROR_GPIO_FAILURE = 5;
	public static int EXIT_ERROR_OTHER = 999;
	
	//****************************************************************************************************************************
	
	//****************************************************************************************************************************
	
	//****************************************************************************************************************************
}
