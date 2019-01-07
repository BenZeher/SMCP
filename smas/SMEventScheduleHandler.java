package smas;
/**
 * Flow diagram for this process schedules can be viewed here:
 * https://creately.com/diagram/j0jkoft92/HPE4HfLHEYNU0uew0xS8uGMcTw%3D
 */
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import javax.servlet.ServletContext;

import ConnectionPool.CompanyDataCredentials;
import SMClasses.SMLogEntry;
import SMDataDefinition.SMTablessalarmsequences;
import SMDataDefinition.SMTablessdeviceevents;
import SMDataDefinition.SMTablesseventscheduledetails;
import SMDataDefinition.SMTablesseventschedules;
import ServletUtilities.clsMasterEntry;
import ServletUtilities.clsServletUtilities;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsDateAndTimeConversions;
import smcontrolpanel.SMBackgroundJobManager;
import smcontrolpanel.SMUtilities;
import sscommon.SSConstants;

public class SMEventScheduleHandler  extends clsMasterEntry{

	public static final String SCHEDULE_MANAGER_USER = "SCHEDULER";
	public static final String SCHEDULE_MANAGER_USERID = "0";
	private static final boolean bDebugMode = false;
	
	public String processSchedules(
		ServletContext context,
		String sDatabaseID,
		String sServerID,
		String sTestDeviceID,
		int iDiagnosticLoggingLevel
		) throws Exception{
		String s = "";
		Connection conn = null;
		ArrayList<String> arrDeviceIDs = new ArrayList<String>(0);
		ArrayList<String> arrAlarmSequenceIDs = new ArrayList<String>(0);

		if(iDiagnosticLoggingLevel >= 1){
			System.out.println("[1516307934] " + clsDateAndTimeConversions.nowStdFormatWithSeconds() + " - in processSchedules, sServerID = '" + sServerID + "', sDatabaseID = '" + sDatabaseID + "'.");
		}
		
		try {
			conn = clsDatabaseFunctions.getConnectionWithException(
				context, 
				sDatabaseID, 
				"MySQL", 
				SMUtilities.getFullClassName(this.toString() + ".processSchedules"));
		} catch (Exception e1) {
			//If we can't get a connection here, just exit quietly - it's probably not a legitimate database:
			System.out.println("[1483976558] - can't connect to company database ID '" 
					+ sDatabaseID + "' which is listed in company data credentials - it probably doesn't exist.");
				return s;
		}
		
		if(iDiagnosticLoggingLevel >= 1){
			System.out.println("[1516307935] " + clsDateAndTimeConversions.nowStdFormatWithSeconds() + " - in processSchedules, got connection.");
		}
		
		//Get a list of all the devices and alarm sequences that are included in any active schedules:
		String SQL = "SELECT"
			+ " * FROM " + SMTablesseventscheduledetails.TableName
			+ " LEFT JOIN " + SMTablesseventschedules.TableName + " ON "
			+ SMTablesseventscheduledetails.TableName + "." + SMTablesseventscheduledetails.lsseventscheduleid
			+ " = " + SMTablesseventschedules.TableName + "." + SMTablesseventschedules.lid
			+ " WHERE ("
				+ "(" + SMTablesseventschedules.iactive + " = 1)"
			+ ") ORDER BY " + SMTablesseventscheduledetails.TableName + "." + SMTablesseventscheduledetails.ideviceorsequence
			+ ", " + SMTablesseventscheduledetails.TableName + "." + SMTablesseventscheduledetails.ldeviceorsequenceid
		;
		
		if(bDebugMode){
			System.out.println("[1489409465] - " + SQL);
		}
		
		try {
			ResultSet rs = null;
			try {
				rs = clsDatabaseFunctions.openResultSet(SQL, conn);
			} catch (SQLException e) {
				//If it's just because the database doesn't exist, don't choke over it:
				if (e.getMessage().contains("doesn't exist")){
					System.out.println("Error [1483738051] - company database ID '" + sDatabaseID + "'"
						+ ", which is listed in " + CompanyDataCredentials.TableName 
						+ ", is missing table '" + SMTablesseventscheduledetails.TableName + "'."
					);
					clsDatabaseFunctions.freeConnection(context, conn);
					return s;
				}
			}
			catch (Exception e) {
				clsDatabaseFunctions.freeConnection(context, conn);
				throw new Exception("Error [1483738484] - " + e.getMessage());
			}
			//System.out.println("[1483975843] - eventschedule rs SQL = '" + SQL + "'.");
			//System.out.println("[1483975843-1.1] - rs = " + rs);
			
			//If the resultset is null, that probably means there's nothing to process, probably not even a table or a database
			//so we return 'quietly':
			if (rs == null){
				clsDatabaseFunctions.freeConnection(context, conn);
				System.out.println("[1483976557] - in company database ID '" 
					+ sDatabaseID + "', there is no event schedule table for processing.");
				return s;
			}
			
			//System.out.println("[1483975843-1.2]");
			
			if(iDiagnosticLoggingLevel >= 1){
				System.out.println("[1516307936] " + clsDateAndTimeConversions.nowStdFormatWithSeconds() + " - in processSchedules, going into while loop for resultset.");
			}
			
			while (rs.next()){
				//System.out.println("[1483975843-1]");
				String sDeviceOrSequenceID = Long.toString(rs.getLong(SMTablesseventscheduledetails.TableName + "." + SMTablesseventscheduledetails.ldeviceorsequenceid));
				
				//If it's a device, handle it:
				if (rs.getInt(SMTablesseventscheduledetails.TableName + "." + SMTablesseventscheduledetails.ideviceorsequence)
					== SMTablesseventscheduledetails.DEVICEORSEQUENCE_DEVICE){
					//System.out.println("[1483975843-2]");
					boolean bDeviceIsAlreadyListed = false;
					for (int i = 0; i < arrDeviceIDs.size(); i++){
						if (arrDeviceIDs.get(i).compareToIgnoreCase(sDeviceOrSequenceID) == 0){
							bDeviceIsAlreadyListed = true;
						}
					}
					//System.out.println("[1483975843-3]");
					//If the device wasn't already in the list, then add it:
					if (!bDeviceIsAlreadyListed){
						arrDeviceIDs.add(sDeviceOrSequenceID);
					}
				}else{
					
					//But if it's an alarm sequence:
					boolean bAlarmSequenceIsAlreadyListed = false;
					//System.out.println("[1483975843-4]");
					for (int i = 0; i < arrAlarmSequenceIDs.size(); i++){
						if (arrAlarmSequenceIDs.get(i).compareToIgnoreCase(sDeviceOrSequenceID) == 0){
							bAlarmSequenceIsAlreadyListed = true;
						}
					}
					//If the device wasn't already in the list, then add it:
					//System.out.println("[1483975843-5]");
					if (!bAlarmSequenceIsAlreadyListed){
						arrAlarmSequenceIDs.add(sDeviceOrSequenceID);
					}
				}
			}
			rs.close();
		} catch (Exception e) {
			clsDatabaseFunctions.freeConnection(context, conn);
			throw new Exception("Error [1482263715] reading event schedule records using database ID '" + sDatabaseID + "' - " + e.getMessage());
		}
		
		//System.out.println("[1483983785]");
		
		//Now process each of the devices:
		for (int i = 0; i < arrDeviceIDs.size(); i++){
			try {
				s += processDevice(arrDeviceIDs.get(i), conn, context, sServerID, sTestDeviceID);
			} catch (Exception e) {
				clsDatabaseFunctions.freeConnection(context, conn);
				throw new Exception("Error [1482267396] - " + e.getMessage());
			}
		}
		
		//Now process each of the alarm sequences:
		if(iDiagnosticLoggingLevel >= 1){
			System.out.println("[1516307937] " + clsDateAndTimeConversions.nowStdFormatWithSeconds() + " - in processSchedules, going into process alarm sequences.");
		}
		for (int i = 0; i < arrAlarmSequenceIDs.size(); i++){
			if(iDiagnosticLoggingLevel >= 1){
				System.out.println("[1516307938] " + clsDateAndTimeConversions.nowStdFormatWithSeconds() + " - in processSchedules, processing alarm sequence '" + arrAlarmSequenceIDs.get(i) + "'.");
			}
			try {
				s += processSequence(arrAlarmSequenceIDs.get(i), conn, context, sServerID);
			} catch (Exception e) {
				if(iDiagnosticLoggingLevel >= 1){
					System.out.println("[1516307939] " + clsDateAndTimeConversions.nowStdFormatWithSeconds() + " - in processSchedules, error processing alarm sequence ID '" + arrAlarmSequenceIDs.get(i) + "' - " + e.getMessage() + ".");
				}
				clsDatabaseFunctions.freeConnection(context, conn);
				throw new Exception("Error [1484088922] - " + e.getMessage());
			}
		}
		clsDatabaseFunctions.freeConnection(context, conn);
		return s;
	}
	
	public static String processDevice(String sDeviceID, Connection conn, ServletContext context, String sServerID, String sTestDeviceID) throws Exception{
		String s = "";
		
		//FOR TESTING ONLY:
		boolean bDeviceIsTestDevice = sDeviceID.compareToIgnoreCase(sTestDeviceID) == 0;
		
		//System.out.println("[1483983786] Processing schedule for device ID '" + sDeviceID + "', test device is: '" + sTestDeviceID + "'.");
		
		//We have to accomplish THREE things here:
		// 1 - we have to make sure that the device is in the state it SHOULD be in
		// 2 - we have to make sure that the 'activated' flag for every schedule detail is set correctly.  For any device,
		//     there should only be ONE 'activated' schedule detail at a time - all the others should be 'de-activated'.
		//     That's because there should only be ONE active schedule at any given time.
		// 3 - if it's already been activated or not activated, as it should be, then we don't want to send it a redundant signal to activate/de-activate again.
		
		//Get all the active schedules for this device:
		String SQL = "SELECT"
			+ " * "
			+ " FROM " + SMTablesseventscheduledetails.TableName
			+ " LEFT JOIN " + SMTablesseventschedules.TableName + " ON "
			+ SMTablesseventscheduledetails.TableName + "." + SMTablesseventscheduledetails.lsseventscheduleid + " = " + SMTablesseventschedules.TableName + "." + SMTablesseventschedules.lid
			+ " WHERE ("
				+ "(" + SMTablesseventscheduledetails.TableName + "." + SMTablesseventscheduledetails.ideviceorsequence + " = " + Integer.toString(SMTablesseventscheduledetails.DEVICEORSEQUENCE_DEVICE) + ")"
				+ " AND (" + SMTablesseventscheduledetails.TableName + "." + SMTablesseventscheduledetails.ldeviceorsequenceid + "= " + sDeviceID + ")"
				+ " AND (" + SMTablesseventschedules.TableName + "." + SMTablesseventschedules.iactive + " = 1)"
			+ ")"
			;
		if (bDebugMode){
			System.out.println("[1484081728] SQL = '" + SQL + "'");
		}
		boolean bDeviceShouldBeActive = false;
		boolean bDeviceIsFlaggedAsActivated = false;
		long lSSEventScheduleDetailID = 0;
		
		//This tells us which schedule set the device to active, if it is:
		String sEventScheduleWhichSetDeviceToActive = "0";
		ResultSet rsSchedules = clsDatabaseFunctions.openResultSet(SQL, conn);
		String sActivatingScheduleID = "0";
		String sActivatingSchedulename = "";
		String sActivatingScheduleDescription = "";
		while (rsSchedules.next()){
			SSEventSchedule sched = new SSEventSchedule();
			sched.setslid(Long.toString(rsSchedules.getLong(SMTablesseventscheduledetails.TableName + "." + SMTablesseventscheduledetails.lsseventscheduleid)));
			
			//If it's flagged as 'activated' on ANY schedule detail record, then we record that.
			//There should ONLY be one schedule at a time, if any, that has caused the device to be activated.
			if (rsSchedules.getLong(SMTablesseventscheduledetails.TableName + "." + SMTablesseventscheduledetails.iactivated) == 1){
				bDeviceIsFlaggedAsActivated = true;
				sEventScheduleWhichSetDeviceToActive = Long.toString(rsSchedules.getLong(SMTablesseventscheduledetails.TableName + "." + SMTablesseventscheduledetails.lsseventscheduleid));
			}
			//Even if there are several schedules telling this device to be OFF, as long as ONE
			//is telling it to be ON, then we turn it on:
			
			//There can only be ONE 'live' schedule at any given time, because we don't allow schedules to be built that
			// 'overlap' = no device can be scheduled for any overlapping time period in multiple schedules:
			
			if (sched.isCurrentlyLive(conn, bDeviceIsTestDevice)){
			//if (sched.isCurrentlyLive(conn, false)){
				bDeviceShouldBeActive = true;
				//Record the event schedule ID for the one detail which SHOULD be active now (if there is one); 
				lSSEventScheduleDetailID = rsSchedules.getLong(SMTablesseventscheduledetails.TableName + "." + SMTablesseventscheduledetails.lid);
				//Record the schedule ID for the schedule that's activating the device:
				sActivatingScheduleID = sched.getslid();
				sActivatingSchedulename = sched.getsname();
				sActivatingScheduleDescription = sched.getsdescription();
			}
		}
		rsSchedules.close();
		
		SSDevice dev = new SSDevice();
		dev.setslid(sDeviceID);
		try {
			dev.load(conn);
		} catch (Exception e) {
			throw new Exception("Error [1482269679] - Could not load device with ID '" + sDeviceID + "' - " + e.getMessage());
		}
		
		SMLogEntry log = new SMLogEntry(conn);
		
		//TESTING:
		if(bDeviceIsTestDevice){
			clsServletUtilities.sysprint(
				"[1493949846] SMEventScheduleHandler", 
				"TESTUSER", 
				"\n"
					+ "bDeviceShouldBeActive = " + bDeviceShouldBeActive + "\n"
					+ "bDeviceIsFlaggedAsActivated = " + bDeviceIsFlaggedAsActivated + "\n"
					+ "lSSEventScheduleDetailID = " + lSSEventScheduleDetailID + "\n"
					+ "sEventScheduleWhichSetDeviceToActive = " + sEventScheduleWhichSetDeviceToActive + "\n"
					+ "sActivatingScheduleID = " + sActivatingScheduleID + "\n"
					+ "sActivatingSchedulename = '" + sActivatingSchedulename + "'\n"
					+ "sActivatingScheduleDescription = '" + sActivatingScheduleDescription + "'\n"
					+ ""
			);
		}
		//If the device should be active now...
		if (bDeviceShouldBeActive){
			//IF it IS already activated, determine if it was already activated by another schedule OR if it's just being activated for the first time on this schedule now.
			boolean bDeviceWasActivatedByPreviousSchedule = false;
			if (
				//If the schedule which set this device to active is NOT 'empty':
				(sEventScheduleWhichSetDeviceToActive.compareToIgnoreCase("0") != 0)
				//AND if the schedule which set this device to active is NOT the schedule that SHOULD be activating it:
				&& (sEventScheduleWhichSetDeviceToActive.compareToIgnoreCase(sActivatingScheduleID) != 0)
			){
				//Then this device as activated by some PREVIOUS schedule, not the one that's supposed to be activating it now
				bDeviceWasActivatedByPreviousSchedule = true;
			}
			
			if (
				//IF it's not flagged as active yet...
				(!bDeviceIsFlaggedAsActivated)
				//OR if it IS flagged as active, but it was activated by a different schedule previously:
				|| (bDeviceWasActivatedByPreviousSchedule)
			){
				//We assume that the device will be activated by CLOSING the output terminals:
				String sSetContactState = SSConstants.QUERY_KEYVALUE_TERMINAL_CONTACT_STATUS_CLOSED;
				String sActivationDurationInMS = Long.toString(24 * 60 * 60 * 1000);
				
				//Comment out for testing:
				dev.setOutputContactsState(conn, sSetContactState, sActivationDurationInMS, SCHEDULE_MANAGER_USER, context, sServerID);
				
				log.writeEntry(
					SMBackgroundJobManager.SCHEDULING_USER, 
					SMLogEntry.LOG_OPERATION_SSDEVICESETBYSCHEDULE, 
					"Device ID " + dev.getslid() + " set by automatic scheduling", 
					"Device: " + dev.getsdescription() + " was SET by schedule ID: " +  sActivatingScheduleID, 
					"[1484012076]");
				
				ASDeviceEventLogEntry devicelog = new ASDeviceEventLogEntry(conn);
				SSController outputcontroller = dev.getoutputcontroller(conn);
				try {
					devicelog.writeEntry(
						-1, 
						SMTablessdeviceevents.DEVICE_EVENT_TYPE_ACTIVATED, 
						Long.parseLong(sDeviceID), 
						dev.getsdescription(), 
						Long.parseLong(dev.getsoutputcontrollerid()), 
						outputcontroller.getscontrollername(), 
						outputcontroller.getsdescription(), 
						Long.parseLong(sActivatingScheduleID), 
						sActivatingSchedulename, 
						sActivatingScheduleDescription, 
						dev.getsoutputterminalnumber(), 
						"Device SET by automatic schedule", 
						"[1485971735]", 
						null
					);
				} catch (Exception e) {
					throw new Exception("Error [1459215981] writing to device event log - " + e.getMessage());
				}
			}
			if(bDeviceIsTestDevice){
				clsServletUtilities.sysprint(
					"[1493949847] SMEventScheduleHandler", 
					"TESTUSER", 
					"\n"
						+ "bDeviceShouldBeActive = " + bDeviceShouldBeActive + "\n"
						+ "bDeviceIsFlaggedAsActivated = " + bDeviceIsFlaggedAsActivated + "\n"
						+ "lSSEventScheduleDetailID = " + lSSEventScheduleDetailID + "\n"
						+ "sEventScheduleWhichSetDeviceToActive = " + sEventScheduleWhichSetDeviceToActive + "\n"
						+ "sActivatingScheduleID = " + sActivatingScheduleID + "\n"
						+ "sActivatingSchedulename = '" + sActivatingSchedulename + "'\n"
						+ "sActivatingScheduleDescription = '" + sActivatingScheduleDescription + "'\n"
						+ ""
				);
			}
		}else{
			//But if it should NOT be active and is FLAGGED as active, turn it off:
			if (bDeviceIsFlaggedAsActivated){
				//If the device should NOT be active now, then turn it off:
					
				//Comment out for testing:
				dev.setOutputContactsState(conn, SSConstants.QUERY_KEYVALUE_TERMINAL_CONTACT_STATUS_OPEN, "0", SCHEDULE_MANAGER_USER, context, sServerID);
				log.writeEntry(
					SMBackgroundJobManager.SCHEDULING_USER, 
					SMLogEntry.LOG_OPERATION_SSDEVICEUNSETBYSCHEDULE, 
					"Device ID " + dev.getslid() + " UNset by automatic scheduling", 
					"Device: " + dev.getsdescription() , 
					"[1484012077]");
				
				ASDeviceEventLogEntry devicelog = new ASDeviceEventLogEntry(conn);
				SSController outputcontroller = dev.getoutputcontroller(conn);
				try {
					devicelog.writeEntry(
						-1, 
						SMTablessdeviceevents.DEVICE_EVENT_TYPE_DEACTIVATED, 
						Long.parseLong(sDeviceID), 
						dev.getsdescription(), 
						Long.parseLong(dev.getsoutputcontrollerid()), 
						outputcontroller.getscontrollername(), 
						outputcontroller.getsdescription(), 
						Long.parseLong(sActivatingScheduleID), 
						sActivatingSchedulename, 
						sActivatingScheduleDescription, 
						dev.getsoutputterminalnumber(), 
						"Device UNSET by automatic schedule", 
						"[1485971736]", 
						null
					);
				} catch (Exception e) {
					throw new Exception("Error [1459215991] writing to device event log - " + e.getMessage());
				}
			}
			if(bDeviceIsTestDevice){
				clsServletUtilities.sysprint(
					"[1493949848] SMEventScheduleHandler", 
					"TESTUSER", 
					"\n"
						+ "bDeviceShouldBeActive = " + bDeviceShouldBeActive + "\n"
						+ "bDeviceIsFlaggedAsActivated = " + bDeviceIsFlaggedAsActivated + "\n"
						+ "lSSEventScheduleDetailID = " + lSSEventScheduleDetailID + "\n"
						+ "sEventScheduleWhichSetDeviceToActive = " + sEventScheduleWhichSetDeviceToActive + "\n"
						+ "sActivatingScheduleID = " + sActivatingScheduleID + "\n"
						+ "sActivatingSchedulename = '" + sActivatingSchedulename + "'\n"
						+ "sActivatingScheduleDescription = '" + sActivatingScheduleDescription + "'\n"
						+ ""
				);
			}
		}
		
		//Now we have to update the schedule details 'activated' flag so that the system knows which details have been activated and which have been 
		//'de-activated':
		// TOREVIEW - Review this line to see if it can't be optimized:
		SQL = "UPDATE " + SMTablesseventscheduledetails.TableName
			+ " SET " + SMTablesseventscheduledetails.iactivated + " = "
			+ " IF(" + SMTablesseventscheduledetails.lid + " = " + Long.toString(lSSEventScheduleDetailID) + ", 1, 0)"
			+ " WHERE ("
				+ "(" + SMTablesseventscheduledetails.ideviceorsequence + " = " + SMTablesseventscheduledetails.DEVICEORSEQUENCE_DEVICE + ")"
				+ " AND (" + SMTablesseventscheduledetails.ldeviceorsequenceid + " = " + sDeviceID + ")"
			+ ")"
		;
		try {
			Statement stmt = conn.createStatement();
			stmt.execute(SQL);
		} catch (Exception e) {
			throw new Exception("Error [1484253469] updating activation state of schedule details with SQL: " + SQL + " - " + e.getMessage());
		}
		
		return s;
	}
	
	public String processSequence(String sSequenceID, Connection conn, ServletContext context, String sServerID) throws Exception{

		String s = "";
		
		//System.out.println("[1484088923] Processing schedule for alarm sequence ID '" + sSequenceID + "'.");
		
		//Get all the schedules for this sequence:
		String SQL = "SELECT"
			+ " DISTINCT " + SMTablesseventscheduledetails.lsseventscheduleid
			+ " FROM " + SMTablesseventscheduledetails.TableName
			+ " WHERE ("
				+ "(" + SMTablesseventscheduledetails.ideviceorsequence + " = " + Integer.toString(SMTablesseventscheduledetails.DEVICEORSEQUENCE_SEQUENCE) + ")"
				+ " AND (" + SMTablesseventscheduledetails.ldeviceorsequenceid + "= " + sSequenceID + ")"
			+ ")"
			;
		if (bDebugMode){
			System.out.println("[1484088924] SQL = '" + SQL + "'");
		}
		boolean bSequenceIsOnLiveSchedule = false;
		ResultSet rsSchedules = clsDatabaseFunctions.openResultSet(SQL, conn);
		SSEventSchedule sched = null;
		while (rsSchedules.next()){
			sched = new SSEventSchedule();
			sched.setslid(Long.toString(rsSchedules.getLong(SMTablesseventscheduledetails.lsseventscheduleid)));
			//Even if there are several schedules telling this sequence to be OFF, as long as ONE
			//is telling it to be ON, then we turn it on:
			if (sched.isCurrentlyLive(conn, false)){
				bSequenceIsOnLiveSchedule = true;
				break;
			}
		}
		rsSchedules.close();
		
		//if this sequence is not in any schedules AT ALL, then exit
		if(sched == null){
			return "";
		}
		
		//Now load this sequence:
		SSAlarmSequence seq = new SSAlarmSequence();
		seq.setslid(sSequenceID);
		try {
			seq.load(conn);
		} catch (Exception e) {
			throw new Exception("Error [1484088925] - Could not load alarm sequence with ID '" + sSequenceID + "' - " + e.getMessage());
		}
		
		SMLogEntry log = new SMLogEntry(conn);
		
		//If the sequence should be active now, turn it on:
		if (bSequenceIsOnLiveSchedule){
			//Check to see when it was last turned off, so we allow a 'reset' time before it's set again:
			long lLastTimeDisarmedInSeconds = seq.getdattimelastdisarmedasunixtimestamp();
			long lResetDelayInSeconds = 0;
			long lCurrentTimeInSeconds = System.currentTimeMillis() / 1000L;
			for (int i = 0; i < sched.getEventScheduleDetails().size(); i++){
				if (sched.getEventScheduleDetails().get(i).getsideviceoralarmsequence().compareToIgnoreCase(Integer.toString(SMTablesseventscheduledetails.DEVICEORSEQUENCE_SEQUENCE)) == 0){
					if (sched.getEventScheduleDetails().get(i).getsldeviceorsequenceid().compareToIgnoreCase(sSequenceID) == 0){
						lResetDelayInSeconds = Long.parseLong(sched.getEventScheduleDetails().get(i).getsiresetdelay()) * 60L;
					}
				}
			}
			//TODO - 
			//If the last user to disarm the sequence was the system SCHEDULE USER Or if the reset delay has expired
			//since the alarm was last disarmed, then we can reset it:
			
			//We only have to set it IF it's not already set:
			
			//IF it's supposed to be SET ('armed') but it's NOT armed:
			if (seq.getsalarmstate().compareToIgnoreCase(Integer.toString(SMTablessalarmsequences.ALARM_STATE_UNARMED)) == 0){
				//If the schedule was last to UNset (disarm) the sequence
/*				if(seq.getslastdisarmedby().compareToIgnoreCase(SCHEDULE_MANAGER_USER) == 0){
					//Then if it has NOT been set by the schedule
					if(sched.getEventScheduleDetail(Integer.toString(
							SMTablesseventscheduledetails.DEVICEORSEQUENCE_SEQUENCE), sSequenceID)
							.getsiactivated().compareToIgnoreCase("0") == 0){
						//Arm the sequence:
						seq.setArmedState(conn, SCHEDULE_MANAGER_USER, SMTablessalarmsequences.ALARM_STATE_ARMED, true);
				
						//Now we have to update the schedule details 'activated' flag so that the system knows which details have been activated and which have been 
						//'de-activated' by a schedule
						//This flag doesn't actually tell us if the sequence is ARMED or not, but tells us if it was last ACTIVATED or DEACTIVATED by a schedule.
						sched.getEventScheduleDetail(Integer.toString(
						SMTablesseventscheduledetails.DEVICEORSEQUENCE_SEQUENCE), sSequenceID).setsiactivated("1");
						sched.save_without_data_transaction(conn, SCHEDULE_MANAGER_USER);
				
						log.writeEntry(
							SMBackgroundJobManager.SCHEDULING_USER, 
							SMLogEntry.LOG_OPERATION_SSSEQUENCESETBYSCHEDULE, 
							"Alarm sequence ID " + seq.getslid() + " set by automatic scheduling (" + sched.getsname() + ")", 
							"Sequence: " + seq.getsdescription() , 
							"[1484088926]");
					
						ASDeviceEventLogEntry devicelog = new ASDeviceEventLogEntry(conn);
						try {
							devicelog.writeEntry(
								-1, 
								SMTablessdeviceevents.DEVICE_EVENT_TYPE_ACTIVATED, 
								Long.parseLong(seq.getslid()), 
								seq.getsdescription(), 
								-1, 
								"", 
								"", 
								Long.parseLong(sched.getslid()), 
								sched.getsname(), 
								sched.getsdescription(), 
								"-1", 
								"Alarm sequence SET by automatic schedule '" + sched.getsname() + "'", 
								"[1485971635]", 
								null
								);
						} catch (Exception e) {
							throw new Exception("Error [1459215981] writing to device event log - " + e.getMessage());
						}
					}
				//If it was previously disarmed by a anyone BUT the schedule, i.e., but a live person:
			}else{
*/	
					//If reset delay has expired:
					if((lCurrentTimeInSeconds - lLastTimeDisarmedInSeconds) >= lResetDelayInSeconds){
						//Then arm the sequence
						seq.setArmedState(
							conn, 
							SCHEDULE_MANAGER_USER, 
							SCHEDULE_MANAGER_USERID,
							SMTablessalarmsequences.ALARM_STATE_ARMED, 
							true, 
							context,
							sServerID);
				
						//Now we have to update the schedule details 'activated' flag so that the system knows which details have been activated and which have been 
						//'de-activated' by a schedule 
					//	sched.getEventScheduleDetail(Integer.toString(
					//	SMTablesseventscheduledetails.DEVICEORSEQUENCE_SEQUENCE), sSequenceID).setsiactivated("1");
						sched.save_without_data_transaction(conn, SCHEDULE_MANAGER_USER, context);
				
						log.writeEntry(
							SMBackgroundJobManager.SCHEDULING_USER, 
							SMLogEntry.LOG_OPERATION_SSSEQUENCESETBYSCHEDULE, 
							"Alarm sequence ID " + seq.getslid() + " set by automatic scheduling (" + sched.getsname() + ")", 
							"Sequence: " + seq.getsdescription() , 
							"[1489613599]");
					
						ASDeviceEventLogEntry devicelog = new ASDeviceEventLogEntry(conn);
						try {
							devicelog.writeEntry(
								-1, 
								SMTablessdeviceevents.DEVICE_EVENT_TYPE_ACTIVATED, 
								Long.parseLong(seq.getslid()), 
								seq.getsdescription(), 
								-1, 
								"", 
								"", 
								Long.parseLong(sched.getslid()), 
								sched.getsname(), 
								sched.getsdescription(), 
								"-1", 
								"Alarm sequence SET by automatic schedule '" + sched.getsname() + "'", 
								"[1489613600]", 
								null
								);
						} catch (Exception e) {
							throw new Exception("Error [1459215981] writing to device event log - " + e.getMessage());
						}
					}
				}

			
		//If the the schedule is not supposed to be LIVE:
//****Commented out on 4/20/17 by BJZ
/*		}else{
			//If the alarm sequence should NOT be set now, then turn it off:


			//We only have to UNset it if it's already set:
 
			if (seq.getsalarmstate().compareToIgnoreCase(Integer.toString(SMTablessalarmsequences.ALARM_STATE_ARMED)) == 0){
				//And ONLY IF the alarm sequence was last activated by this schedule:
				if(sched.getEventScheduleDetail(Integer.toString(
						SMTablesseventscheduledetails.DEVICEORSEQUENCE_SEQUENCE), sSequenceID)
						.getsiactivated().compareToIgnoreCase("1") == 0){
					// then UNarm the alarm sequence
					seq.setArmedState(conn, SCHEDULE_MANAGER_USER, SMTablessalarmsequences.ALARM_STATE_UNARMED, true);
					//Flag sequence as UNset by the schedule
					sched.getEventScheduleDetail(Integer.toString(
							SMTablesseventscheduledetails.DEVICEORSEQUENCE_SEQUENCE), sSequenceID).setsiactivated("0");
					sched.save_without_data_transaction(conn, SCHEDULE_MANAGER_USER);
					
					log.writeEntry(
						SMBackgroundJobManager.SCHEDULING_USER, 
						SMLogEntry.LOG_OPERATION_SSSEQUENCEUNSETBYSCHEDULE, 
						"Alarm sequence ID " + seq.getslid() + " UNset by automatic scheduling", 
						"Sequence: " + seq.getsdescription() , 
						"[1484088927]");
					
					ASDeviceEventLogEntry devicelog = new ASDeviceEventLogEntry(conn);
					try {
						devicelog.writeEntry(
							-1, 
							SMTablessdeviceevents.DEVICE_EVENT_TYPE_DEACTIVATED, 
							Long.parseLong(seq.getslid()), 
							seq.getsdescription(), 
							-1, 
							"", 
							"", 
							Long.parseLong(sched.getslid()), 
							sched.getsname(), 
							sched.getsdescription(), 
							"-1", 
							"Alarm sequence UNSET by automatic schedule", 
							"[1485971636]", 
							null
						);
					} catch (Exception e) {
						throw new Exception("Error [1459215281] writing to device event log - " + e.getMessage());
					}
				}
			}*/
		}		
		return s;
	}
}
