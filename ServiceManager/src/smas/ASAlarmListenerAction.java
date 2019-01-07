package smas;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import SMClasses.SMBackgroundScheduleProcessor;
import SMClasses.SMLogEntry;
import SMDataDefinition.SMTablessalarmsequences;
import SMDataDefinition.SMTablessalarmtriggerdevices;
import SMDataDefinition.SMTablesscontrollers;
import SMDataDefinition.SMTablessdeviceevents;
import SMDataDefinition.SMTablessdevices;
import SMDataDefinition.SMTablesseventscheduledetails;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsManageRequestParameters;
import smcontrolpanel.SMUtilities;
import sscommon.SSConstants;
import sscommon.SSUtilities;

public class ASAlarmListenerAction extends HttpServlet{
	
	//Sample connection string:
	/*
	  First, connect on the web server port like this:
	  
	  telnet localhost 8080
	  
	  After connecting, enter this string first:
	  
	  GET /sm/smas.ASAlarmListenerAction?user=Benz&pw=Passw0rd&db=ServMgr1 HTTP/1.1
	  
	  Then hit Enter, and type this:
	  
	  host: localhost
	  
	  Then hit enter twice, and the class will respond and close the connection
	  
	 */
	
	private static final String ALARM_LISTENER_USER = "ALARMLISTENER";
	private static final String ALARM_LISTENER_USERID = "-1";
	
	private static final long serialVersionUID = 1L;
	//private boolean bDebugMode = true;
	@Override
	public void doPost(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
	
		
		PrintWriter out = response.getWriter();
	    
	    //Read the request:
		String sDBID = clsManageRequestParameters.get_Request_Parameter(SSConstants.QUERY_KEY_DATABASEID, request);
		String sPasscode = clsManageRequestParameters.get_Request_Parameter(SSConstants.QUERY_KEY_PASSCODE, request);
		String sControllerName = clsManageRequestParameters.get_Request_Parameter(SSConstants.QUERY_KEY_CONTROLLERNAME, request);
		
		//Need to build some checking in here, but for now, we'll just use the DBID to get a connection and make an entry in the log:
		Connection conn = null;
		try {
			conn = clsDatabaseFunctions.getConnectionWithException(
				getServletContext(), 
				sDBID, 
				"MySQL", 
				SMUtilities.getFullClassName(this.toString()) + ".doPost - unit '" + sControllerName + "'."
			);
		} catch (Exception e1) {
			out.println("Error getting connection.");
			return;
		}
		
		SMLogEntry log = new SMLogEntry(conn);
		log.writeEntry(
			ALARM_LISTENER_USERID, 
			SMLogEntry.LOG_OPERATION_SSSYSTEMREQUEST, 
			"Received connection from SS controller", 
			 "Query string from controller " + sControllerName + ": '" + request.getQueryString() + "'", 
			"[1458694548]");
		
		try {
			validate_request(sPasscode, sControllerName, conn);
		} catch (Exception e1) {
			log.writeEntry(
				"'" + sControllerName + "'", 
				SMLogEntry.LOG_OPERATION_SSSYSTEMERROR,
				"Error validating passcode '" + sPasscode + "'",
				e1.getMessage(),
				"[1463507195]");
			clsDatabaseFunctions.freeConnection(getServletContext(), conn);
			out.println(SSUtilities.buildKeyValuePair(SSConstants.QUERY_KEY_ACKNOWLEDGMENT, SSConstants.QUERY_KEYVALUE_ACKNOWLEDGMENT_UNSUCCESSFUL)
				+ SSConstants.QUERY_STRING_DELIMITER
				+ SSUtilities.buildKeyValuePair(SSConstants.QUERY_KEY_REQUEST_ERROR, e1.getMessage())
			);
			return;
		}
		
		//Process the request:
		String sResult = "";
		String sServerID = SMBackgroundScheduleProcessor.getServerID(getServletContext());
		try {
			sResult = process_request(request.getQueryString(), conn, out, sServerID);
		} catch (Exception e) {
			log.writeEntry(
					ALARM_LISTENER_USERID, 
					SMLogEntry.LOG_OPERATION_SSSYSTEMERROR,
					"Query string from controller " +sControllerName + ": Error validating query string '" + request.getQueryString() + "'",
					e.getMessage(),
					"[1463507196]");
			out.println(SSUtilities.buildKeyValuePair(SSConstants.QUERY_KEY_ACKNOWLEDGMENT, SSConstants.QUERY_KEYVALUE_ACKNOWLEDGMENT_UNSUCCESSFUL)
				+ SSConstants.QUERY_STRING_DELIMITER
				+ SSUtilities.buildKeyValuePair(SSConstants.QUERY_KEY_REQUEST_ERROR, e.getMessage())
				);
		}
		out.println(SSUtilities.buildKeyValuePair(SSConstants.QUERY_KEY_ACKNOWLEDGMENT, SSConstants.QUERY_KEYVALUE_ACKNOWLEDGMENT_SUCCESSFUL)
			+ SSConstants.QUERY_STRING_DELIMITER
			+ sResult
		);
		
		//Temporary - for diagnostics:
		/*
	    out.println(
	    	SSConstants.REQUEST_ACKNOWLEDGMENT_SUCCESSFUL + SSConstants.QUERY_STRING_DELIMITER
	    	+ " This is the web app speaking: I heard you:\n"
	    	+ "Passcode: " + sPasscode + "\n"
	    	+ "DBID: " + sDBID + "\n"
	    	+ "sUnitID: " + sControllerName + "\n"
	    	+ "Command: " + sCommand + "\n"
	    	+ "Pin values: " + sPinValues
	    );
	    */
		
		//Free the connection:
		clsDatabaseFunctions.freeConnection(getServletContext(), conn);
	    return;
	}
	
	private void validate_request(String sPassCode, String sControllerName, Connection conn) throws Exception{

		String sError = "";
		String SQL = "SELECT * FROM " + SMTablesscontrollers.TableName
			+ " WHERE ("
				+ "(" + SMTablesscontrollers.scontrollername + " = '" + sControllerName + "')"
			+ ")"
		;
		try {
			ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
			if (rs.first()){
				if (rs.getString(SMTablesscontrollers.spasscode).compareTo(sPassCode) != 0){
					sError = "Passcode does not match for controller ID " + sControllerName + ".";
				}
			}else{
				sError = "Controller ID " + sControllerName + " was not found.";
			}
			rs.close();
		} catch (Exception e) {
			throw new Exception("Error [1458784151] reading controllers - " + e.getMessage());
		}
		if (sError.compareToIgnoreCase("") != 0){
			throw new Exception(sError);
		}
		return;
	}
	
	private String process_request(String sQueryString, Connection conn, PrintWriter out, String sServerID) throws Exception{
		String sResult = "";
		//First, get the command:
		String sCommand = SSUtilities.getKeyValue(sQueryString, SSConstants.QUERY_KEY_COMMAND);
		String sControllerName = SSUtilities.getKeyValue(sQueryString, SSConstants.QUERY_KEY_CONTROLLERNAME);

		//SMUtilities.sysprint(this.toString(), "ALARMLISTENER", "[1459884550] sQueryString = '" + sQueryString + "'");
		
		//Process the various commands:
		/*
		public static String QUERY_KEYVALUE_PROCESS_TRIGGER = "PROCESSTRIGGER";
		//This command is passed from the controller to the web app, so the web app can tell the controller what listening state
		//the pins SHOULD be in, and the controller will then modify them if necessary - it passes the pin number and the listening state the pin should be in:
		public static String QUERY_KEYVALUE_GET_LISTENING_STATUS = "GETLISTENINGSTATUS"; //asks the web app for the correct listening status so the controller can set
		//This command is passed from the controller to the web app, so the web app can tell the controller what active state
		//the pins SHOULD be in, and the controller will then modify them if necessary - it passes the pin number and the active state the pin should be in:
		public static String QUERY_KEYVALUE_GET_ACTIVE_STATUS = "GETACTIVESTATUS";
		//This command precedes a list of the listening statuses of the pins.  It's sent from the controller to the web app in response
		//to the web apps 'QUERY_KEYVALUE_CONFIRM_PIN_LISTENING_STATUS' request:
		public static String QUERY_KEYVALUE_LISTING_PIN_LISTENING_STATUS = "LISTINGLISTENINGSTATUS";
		//This command precedes a list of the active statuses of the pins.  It's sent from the controller to the web app in response
		//to the web apps 'QUERY_KEYVALUE_CONFIRM_PIN_ACTIVE_STATUS' request:
		public static String QUERY_KEYVALUE_LISTING_PIN_ACTIVE_STATUS = "LISTINGACTIVESTATUS";
		*/
		
		//*******************************************************************************************
		// Server is being told to process a 'trigger' from an input terminal:
		if (sCommand.compareToIgnoreCase(SSConstants.QUERY_KEYVALUE_COMMAND_PROCESS_TRIGGER) == 0){
			String sKeyValuePairs[] = SSUtilities.getKeyValuePairsList(sQueryString);
			String sTerminalNumber = "";
			String sTriggerState = "";
			for (int i = 0; i < sKeyValuePairs.length; i++){
				//This command only sends ONE trigger report at a time - if we have multiple triggers, each is sent separately:
				String sKey = SSUtilities.getKeyFromRequestList(sKeyValuePairs, i);
				if (sKey.startsWith(SSConstants.QUERY_KEY_TERMINALCONTACTSTATECHANGE_PREFIX)){
					sTerminalNumber = sKey.substring(SSConstants.QUERY_KEY_TERMINALCONTACTSTATECHANGE_PREFIX.length(), sKey.length());
					sTriggerState = SSUtilities.getKeyValue(sQueryString, sKey);
					break;
				}
			}
			
			try {
				processTriggerEvent(sTerminalNumber, sTriggerState, sControllerName, conn, getServletContext(), sServerID);
			} catch (Exception e) {
				//Report the error:
				throw new Exception(e.getMessage());
			}
			//Acknowledge the command:
			return "";
		}
		//********************************************************************************************
		
		//********************************************************************************************
		//Server is being asked the listening status of pins:
		if (sCommand.compareToIgnoreCase(SSConstants.QUERY_KEYVALUE_COMMAND_GET_LISTENING_STATUS) == 0){
			try {
				sResult = processGetControllerInitialListeningStatus(sControllerName, conn);
			} catch (Exception e) {
				//Report the error:
				throw new Exception(e.getMessage());
			}
			return sResult;
		}
		//*********************************************************************************************
		
		//*********************************************************************************************
		//Server is being asked the contact status of pins:
		if (sCommand.compareToIgnoreCase(SSConstants.QUERY_KEYVALUE_COMMAND_GET_CONTACT_STATUS) == 0){
			try {
				sResult = processGetControllerInitialContactStatus(sControllerName, conn);
			} catch (Exception e) {
				//Report the error:
				throw new Exception(e.getMessage());
			}
			return sResult;
		}
		//*********************************************************************************************
		//Controller is 'checking in' on a periodic basis:
		if (sCommand.compareToIgnoreCase(SSConstants.QUERY_KEYVALUE_COMMAND_CONTROLLER_CHECK_IN) == 0){
			try {
				sResult = processControllerCheckIn(sControllerName, conn);
			} catch (Exception e) {
				//Report the error:
				throw new Exception(e.getMessage());
			}
			return sResult;
		}
		//*********************************************************************************************
		//Controller is notifying the server that it just started up:
		if (sCommand.compareToIgnoreCase(SSConstants.QUERY_KEYVALUE_COMMAND_CONTROLLER_STARTUP) == 0){
			try {
				sResult = processControllerStartUp(sControllerName, conn);
			} catch (Exception e) {
				//Report the error:
				throw new Exception(e.getMessage());
			}
			return sResult;
		}
		//*********************************************************************************************
		
		
		
		//If the command wasn't processed, send an UNacknowledgment:
		throw new Exception("Command was not recognized.");
	}
	//Command processing:
	private void processTriggerEvent(
		String sTerminalNumber, 
		String sTriggerState, 
		String sControllerName,
		Connection conn,
		ServletContext context,
		String sServerID) throws Exception{
		
		//First, get the controller:
		String sControllerID = "";
		String sControllerDescription = "";
		String sDeviceID = "";
		int iDeviceInputNormalState = 0;
		boolean bControllerIsInactive = false;
		String sDeviceDescription = "";
		String SQL = "SELECT * FROM " + SMTablesscontrollers.TableName
			+ " WHERE ("
				+ "(" + SMTablesscontrollers.scontrollername + " = '" + sControllerName + "')"
			+ ")"
		;
		ResultSet rsControllers = clsDatabaseFunctions.openResultSet(SQL, conn);
		if (rsControllers.next()){
			
			//Is this an 'active' controller?
			bControllerIsInactive = rsControllers.getInt(SMTablesscontrollers.iactive) == 0;
			sControllerID = Long.toString(rsControllers.getLong(SMTablesscontrollers.lid));
			sControllerDescription = rsControllers.getString(SMTablesscontrollers.sdescription);
			//Now get the device:
			SQL = "SELECT * FROM " + SMTablessdevices.TableName
				+ " WHERE ("
					+ "(" + SMTablessdevices.sinputterminalnumber + " = '" + sTerminalNumber + "')"
					+ " AND (" + SMTablessdevices.linputcontrollerid + " = " + sControllerID + ")"
				+ ")"
			;
			ResultSet rsDevices = clsDatabaseFunctions.openResultSet(SQL, conn);
			if (rsDevices.next()){
				sDeviceID = Long.toString(rsDevices.getLong(SMTablessdevices.lid));
				iDeviceInputNormalState = rsDevices.getInt(SMTablessdevices.iinputtype);
				sDeviceDescription = rsDevices.getString(SMTablessdevices.sdescription);
			}else{
				throw new Exception("Error [1462983530] - could not load device with controller name '" + sControllerName + "'"
					+ " and input terminal '" + sTerminalNumber + "'");
			}
			rsDevices.close();
		}else{
			rsControllers.close();
			throw new Exception("Error [1462983529] - could not load controller '" + sControllerName + "'");
		}

		if(bControllerIsInactive){
			//Just exit quietly:
			return;
		}
		
		//Determine the trigger event 'type':
		int iEventType = -1;
		//If the terminal is normally CLOSED:
		if (iDeviceInputNormalState == SMTablessdevices.DEVICE_INPUT_TYPE_NORMALLY_CLOSED){
			//If the trigger is a contact OPENING, it's a trigger 'start':
			if (sTriggerState.compareToIgnoreCase(SSConstants.QUERY_KEYVALUE_TERMINAL_EVENT_CONTACTS_OPENED) == 0){
				iEventType = SMTablessdeviceevents.DEVICE_EVENT_TYPE_TRIGGER_STARTED;
			}
			//If the trigger is a contact CLOSING, it's a trigger 'stop':
			if (sTriggerState.compareToIgnoreCase(SSConstants.QUERY_KEYVALUE_TERMINAL_EVENT_CONTACTS_CLOSED) == 0){
				iEventType = SMTablessdeviceevents.DEVICE_EVENT_TYPE_TRIGGER_STOPPED;
			}
		}
		
		//If the terminal is normally OPEN:
		if (iDeviceInputNormalState == SMTablessdevices.DEVICE_INPUT_TYPE_NORMALLY_OPEN){
			//If the trigger is a contact CLOSING, it's a trigger 'start':
			if (sTriggerState.compareToIgnoreCase(SSConstants.QUERY_KEYVALUE_TERMINAL_EVENT_CONTACTS_CLOSED) == 0){
				iEventType = SMTablessdeviceevents.DEVICE_EVENT_TYPE_TRIGGER_STARTED;
			}
			//If the trigger is a contact OPENING, it's a trigger 'stop':
			if (sTriggerState.compareToIgnoreCase(SSConstants.QUERY_KEYVALUE_TERMINAL_EVENT_CONTACTS_OPENED) == 0){
				iEventType = SMTablessdeviceevents.DEVICE_EVENT_TYPE_TRIGGER_STOPPED;
			}
		}

		if (iEventType == -1){
			throw new Exception("Error [1459215785] - invalid trigger state '" + sTriggerState + "'.");
		}
		
		//Now if there are any alarm sequences armed for which this device is a 'trigger', trigger those:
		SQL = "SELECT"
			+ " " + SMTablessalarmtriggerdevices.TableName + "." + SMTablessalarmtriggerdevices.ldeviceid
			+ ", " + SMTablessalarmtriggerdevices.TableName + "." + SMTablessalarmtriggerdevices.lalarmsequenceid
			+ ", " + SMTablessalarmsequences.TableName + "." + SMTablessalarmsequences.lid
			+ " FROM " + SMTablessalarmtriggerdevices.TableName 
			+ " LEFT JOIN " + SMTablessalarmsequences.TableName + " ON "
			+ SMTablessalarmtriggerdevices.TableName + "." + SMTablessalarmtriggerdevices.lalarmsequenceid + " = "
			+ SMTablessalarmsequences.TableName + "." + SMTablessalarmsequences.lid
			+ " WHERE ("
				//Only for this device:
				+ "(" + SMTablessalarmtriggerdevices.TableName + "." + SMTablessalarmtriggerdevices.ldeviceid
					+ " = " + sDeviceID + ")"
				//AND
				+ " AND ("
					//The alarm state is either ARMED or TRIGGERED
					+ "(" + SMTablessalarmsequences.TableName + "." + SMTablessalarmsequences.ialarmstate + " = " 
						+ Integer.toString(SMTablessalarmsequences.ALARM_STATE_ARMED) + ")"
					+ " OR (" + SMTablessalarmsequences.TableName + "." + SMTablessalarmsequences.ialarmstate + " = " 
						+ Integer.toString(SMTablessalarmsequences.ALARM_STATE_TRIGGERED) + ")"
				+ ")"
				//And there's actually a matching alarm sequence:	
				+ " AND (" + SMTablessalarmsequences.TableName + "." + SMTablessalarmsequences.lid + " IS NOT NULL)"
			+ ")"
		;
		//System.out.println("[1462994266] - SQL= '" + SQL + "'");
		//Log the event:
		ASDeviceEventLogEntry log = new ASDeviceEventLogEntry(conn);
		try {
			ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
			while (rs.next()){
				SSAlarmSequence seq = new SSAlarmSequence();
				seq.setslid(Long.toString(rs.getLong(SMTablessalarmsequences.TableName + "." + SMTablessalarmsequences.lid)));
				seq.load(conn);
				seq.initiateAlarmSequence(conn,
					ALARM_LISTENER_USER,
					ALARM_LISTENER_USERID,
					sDeviceDescription,
					false,
					context,
					sServerID
				);
				//Record the event triggering the alarm sequence:
				//Log the event:
				try {
					log.writeEntry(
						iDeviceInputNormalState, 
						iEventType, 
						Long.parseLong(sDeviceID), 
						sDeviceDescription, 
						Long.parseLong(sControllerID), 
						sControllerName, 
						sControllerDescription, 
						Integer.parseInt(seq.getslid()), 
						seq.getsname(), 
						seq.getsdescription(), 
						sTerminalNumber, 
						"Trigger event sent from controller", 
						"[1459215980]", 
						null
					);
				} catch (Exception e) {
					throw new Exception("Error [1459215981] writing to device event log - " + e.getMessage());
				}
			}
			rs.close();
		} catch (Exception e1) {
			throw new Exception("Error [1462994265] getting armed alarm sequences - " + e1.getMessage());
		}
		return;
	}

	private String processGetControllerInitialListeningStatus(
			String sControllerName, 
			Connection conn) throws Exception{
			
			String sResult = "";
			String sControllerID = "";
			//Throw exception if it fails, otherwise put the listening status in the sResult variable:
			String SQL = "SELECT"
				+ " " + SMTablesscontrollers.lid
				+ " FROM " + SMTablesscontrollers.TableName
				+ " WHERE ("
					+ "(" + SMTablesscontrollers.scontrollername + " = '" + sControllerName + "')"
				+ ")"
			;
			try {
				ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
				if (rs.next()){
					sControllerID = Long.toString(rs.getLong(SMTablesscontrollers.lid));
				}else{
					rs.close();
					throw new Exception("Error [1462479162] - controller '" + sControllerName + "' was not found.");
				}
				rs.close();
			} catch (Exception e) {
				throw new Exception("Error [1462479163] - " + e.getMessage());
			}
			
			//Now determine what the listening status of the devices on the controller should be, based on what alarms are set:
			SQL = "SELECT"
				+ " " + SMTablessdevices.TableName + "." + SMTablessdevices.sinputterminalnumber
				+ ", " + SMTablessalarmsequences.TableName + "." + SMTablessalarmsequences.ialarmstate
				+ ", " + SMTablessdevices.TableName + "." + SMTablessdevices.iactive
				+ " FROM " + SMTablessdevices.TableName
				+ " LEFT JOIN " + SMTablessalarmtriggerdevices.TableName + " ON "
				+ SMTablessalarmtriggerdevices.TableName + "." + SMTablessalarmtriggerdevices.ldeviceid
				+ " = " + SMTablessdevices.TableName + "." + SMTablessdevices.lid
				+ " LEFT JOIN " + SMTablessalarmsequences.TableName + " ON "
				+ SMTablessalarmsequences.TableName + "." + SMTablessalarmsequences.lid + " = "
				+ SMTablessalarmtriggerdevices.TableName + "." + SMTablessalarmtriggerdevices.lalarmsequenceid
				+ " WHERE ("
					+ "(" + SMTablessdevices.TableName + "." + SMTablessdevices.sinputterminalnumber + " != '')"
					+ " AND (" + SMTablessdevices.TableName + "." + SMTablessdevices.linputcontrollerid + " = " + sControllerID + ")"
				+ ")"
				+ " ORDER BY " + SMTablessdevices.TableName + "." + SMTablessdevices.sinputterminalnumber
			;
			try {
				ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
				while (rs.next()){
					//First, set the value to default to NOT LISTENING:
					String sListeningValue = SSConstants.QUERY_KEYVALUE_TERMINAL_LISTENING_STATUS_NOTLISTENING;
					int iCurrentAlarmState = rs.getInt(SMTablessalarmsequences.TableName + "." + SMTablessalarmsequences.ialarmstate);
					
					//If the device is not inactive, AND if the alarm is either armed or triggered,
					//then set it to listening:
					if (
						(rs.getInt(SMTablessdevices.TableName + "." + SMTablessdevices.iactive) == 1)
						&&
						((iCurrentAlarmState == SMTablessalarmsequences.ALARM_STATE_ARMED)
						|| (iCurrentAlarmState == SMTablessalarmsequences.ALARM_STATE_TRIGGERED))
							
					){
						sListeningValue = SSConstants.QUERY_KEYVALUE_TERMINAL_LISTENING_STATUS_LISTENING;
					}
					if (sResult.compareToIgnoreCase("") == 0){
						sResult += SSUtilities.buildKeyValuePair(
							SSConstants.QUERY_KEY_TERMINALLISTENINGSTATE_PREFIX 
							+ rs.getString(SMTablessdevices.TableName + "." + SMTablessdevices.sinputterminalnumber)
							, sListeningValue);
					}else{
						sResult += SSConstants.QUERY_STRING_DELIMITER 
							+ SSUtilities.buildKeyValuePair(
							SSConstants.QUERY_KEY_TERMINALLISTENINGSTATE_PREFIX 
							+ rs.getString(SMTablessdevices.TableName + "." + SMTablessdevices.sinputterminalnumber)
							, sListeningValue);
					}
				}
				rs.close();
			} catch (Exception e) {
				throw new Exception("Error [1462479163] - error getting device listening statuses with SQL: " + SQL 
					+ " - " + e.getMessage());
			}
			return sResult;
	}

	private String processGetControllerInitialContactStatus(
			String sControllerName, 
			Connection conn) throws Exception{
		
		String sResult = "";

		/*
		//For testing - send this string back:
		sResult = SSUtilities.buildKeyValuePair(SSConstants.QUERY_KEY_TERMINALACTIVESTATE_PREFIX + "1", SSConstants.QUERY_KEYVALUE_TERMINAL_ACTIVE_STATUS_ACTIVATED)
			+ SSConstants.QUERY_STRING_DELIMITER
			+ SSUtilities.buildKeyValuePair(SSConstants.QUERY_KEY_TERMINALINTERVAL_PREFIX + "1", "3000")	
			+ SSConstants.QUERY_STRING_DELIMITER
			+ SSUtilities.buildKeyValuePair(SSConstants.QUERY_KEY_TERMINALACTIVESTATE_PREFIX + "2", SSConstants.QUERY_KEYVALUE_TERMINAL_ACTIVE_STATUS_DEACTIVATED)
			+ SSConstants.QUERY_STRING_DELIMITER
			+ SSUtilities.buildKeyValuePair(SSConstants.QUERY_KEY_TERMINALINTERVAL_PREFIX + "2", "0")
			+ SSConstants.QUERY_STRING_DELIMITER
			+ SSUtilities.buildKeyValuePair(SSConstants.QUERY_KEY_TERMINALACTIVESTATE_PREFIX + "3", SSConstants.QUERY_KEYVALUE_TERMINAL_ACTIVE_STATUS_ACTIVATED)
			+ SSConstants.QUERY_STRING_DELIMITER
			+ SSUtilities.buildKeyValuePair(SSConstants.QUERY_KEY_TERMINALINTERVAL_PREFIX + "3", "1000")
		;
		*/
		return sResult;
	}
	
	private String processControllerStartUp(
			String sControllerName,
			Connection conn) throws Exception{
		
		String sResult = "";

		//Get the controller ID:
		String SQL = "SELECT * FROM " + SMTablesscontrollers.TableName
			+ " WHERE ("
				+ "(" + SMTablesscontrollers.scontrollername + " = '" + sControllerName + "')"
			+ ")"
		;
		String sControllerDescription = "(Unknown)";
		long lControllerID = -1;
		try {
			ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
			if (rs.next()){
				sControllerDescription = rs.getString(SMTablesscontrollers.sdescription);
				lControllerID = rs.getLong(SMTablesscontrollers.lid);
			}
			rs.close();
		} catch (Exception e1) {
			throw new Exception("Error [1486252129] reading controller information for controller named '" + sControllerName + "' - " + e1.getMessage());
		}
		
		//Now we assume that because this controller has just started up, all of its output contacts are open.
		//But if there's a schedule running, possibly some of those contacts are supposed to be CLOSED.
		//So we force the server to send 'close' signals to the controller by setting ANY event schedule details
		//   for any devices on this controller to 'UNactivated'.  Then when the automatic scheduler does its
		//   periodic check, it will see that those devices SHOULD be activated, but aren't, so it will
		//   activate them.
		
		SQL = "UPDATE " + SMTablesseventscheduledetails.TableName
			+ " LEFT JOIN " + SMTablessdevices.TableName + " ON "
			+ SMTablesseventscheduledetails.TableName + "." + SMTablesseventscheduledetails.ldeviceorsequenceid + " = "
			+ SMTablessdevices.TableName + "." + SMTablessdevices.lid
			+ " SET " + SMTablesseventscheduledetails.TableName + "." + SMTablesseventscheduledetails.iactivated + " = 0"
			+ " WHERE ("
				+ "(" + SMTablesseventscheduledetails.TableName + "." + SMTablesseventscheduledetails.ideviceorsequence + " = " + SMTablesseventscheduledetails.DEVICEORSEQUENCE_DEVICE + ")"
				+ " AND (" + SMTablessdevices.TableName + "." + SMTablessdevices.loutputcontrollerid + " = " + Long.toString(lControllerID) + ")"
		+ ")"
		;
		try {
			Statement stmt = conn.createStatement();
			stmt.execute(SQL);
		} catch (Exception e1) {
			throw new Exception("Error [1486252128] updating event schedule details - " + e1.getMessage());
		}
		
		//Log the event:
		ASDeviceEventLogEntry log = new ASDeviceEventLogEntry(conn);		
		try {
			log.writeEntry(
				-1, 
				-1, 
				-1, 
				"(N/A)", 
				lControllerID, 
				sControllerName, 
				sControllerDescription, 
				-1, 
				"(N/A)", 
				"(N/A)", 
				"(N/A)", 
				"Controller started up", 
				"[1486252126]", 
				null
			);
		} catch (Exception e) {
			throw new Exception("Error [1486252127] writing to device event log - " + e.getMessage());
		}
		
		return sResult;
	}
	
	private String processControllerCheckIn(
			String sControllerName, 
			Connection conn) throws Exception{
		
		String sResult = "";
		
		//TODO:
		
		ASDeviceEventLogEntry log = new ASDeviceEventLogEntry(conn);

		//Log the event:
		String SQL = "SELECT * FROM " + SMTablesscontrollers.TableName
			+ " WHERE ("
				+ "(" + SMTablesscontrollers.scontrollername + " = '" + sControllerName + "')"
			+ ")"
		;
		String sControllerDescription = "(Unknown)";
		long lControllerID = -1;
		try {
			ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
			if (rs.next()){
				sControllerDescription = rs.getString(SMTablesscontrollers.sdescription);
				lControllerID = rs.getLong(SMTablesscontrollers.lid);
			}
			rs.close();
		} catch (Exception e1) {
			throw new Exception("Error [1465324145] reading controller information for controller named '" + sControllerName + "' - " + e1.getMessage());
		}
		try {
			log.writeEntry(
				-1, 
				-1, 
				-1, 
				"(N/A)", 
				lControllerID, 
				sControllerName, 
				sControllerDescription, 
				-1, 
				"(N/A)", 
				"(N/A)", 
				"(N/A)", 
				"Check in sent from controller", 
				"[1465324143]", 
				null
			);
		} catch (Exception e) {
			throw new Exception("Error [1465324144] writing to device event log - " + e.getMessage());
		}
		return sResult;
	}

	@Override
	public void doGet(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
		
		doPost(request, response);
	}
}