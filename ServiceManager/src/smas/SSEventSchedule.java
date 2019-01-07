package smas;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Calendar;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import SMClasses.SMBackgroundScheduleProcessor;
import SMClasses.SMLogEntry;
import SMDataDefinition.SMTablessalarmsequences;
import SMDataDefinition.SMTablessdeviceevents;
import SMDataDefinition.SMTablesseventscheduledetails;
import SMDataDefinition.SMTablesseventschedules;
import ServletUtilities.clsMasterEntry;
import ServletUtilities.clsDBServerTime;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsDateAndTimeConversions;
import ServletUtilities.clsManageRequestParameters;
import smcontrolpanel.SMBackgroundJobManager;
import smcontrolpanel.SMUtilities;
import sscommon.SSConstants;

public class SSEventSchedule extends clsMasterEntry{

	public static final String ParamObjectName = "Event Schedule";
	public static final String PARAM_REMOVE_EVENT_DETAIL_ID_PREFIX = "REMOVEDEVICEORSEQUENCEID";
	public static final String PARAM_NEW_DEVICEORSEQUENCE_LIST = "NEWDEVICEORSEQUENCELIST";
	public static final String PARAM_ELIGIBLE_DEVICEORALARMSEQUENCE_LIST = "ELIGIBLEDEVICESANDALARMSEQUENCES";
	public static final String PARAM_EVENT_DETAIL_RESET_DELAY_IN_MINUTES = "RESETDELAYINMINUTES";

	private String m_slid;
	private String m_sname;
	private String m_sdescription;
	private String m_sactive;
	private String m_sstarttime;
	private String m_sdurationinminutes;
	private String m_sdaysoftheweek;
	private ArrayList<SSEventScheduleDetail>arrEventScheduleDetails;
	private boolean bDebugMode = false;

	public SSEventSchedule() {
		super();
		initRecordVariables();
	}

	SSEventSchedule(HttpServletRequest req){
		super(req);
		initRecordVariables();
		m_slid = clsManageRequestParameters.get_Request_Parameter(
				SMTablesseventschedules.lid, req).trim();
		if (m_slid.compareToIgnoreCase("") == 0){
			m_slid = "-1";
		}
		m_sname = clsManageRequestParameters.get_Request_Parameter(
				SMTablesseventschedules.sname, req).trim();
		m_sdescription  = clsManageRequestParameters.get_Request_Parameter(
				SMTablesseventschedules.sdescription, req).trim().replace("&quot;", "\"");
		
		m_sactive = clsManageRequestParameters.get_Request_Parameter(
				SMTablesseventschedules.iactive, req).trim().replace("&quot;", "\"");
		if (m_sactive.compareToIgnoreCase("") == 0){
			m_sactive = "0";
		}else{
			m_sactive = "1";
		}
		
		m_sstarttime = clsManageRequestParameters.get_Request_Parameter(
				SMTablesseventschedules.istarttime, req).trim().replace("&quot;", "\"");
		if (m_sstarttime.compareToIgnoreCase("") == 0){
			m_sstarttime = "0";
		}

		m_sdurationinminutes = clsManageRequestParameters.get_Request_Parameter(
				SMTablesseventschedules.idurationinminutes, req).trim().replace("&quot;", "\"");
		if (m_sdurationinminutes.compareToIgnoreCase("") == 0){
			m_sdurationinminutes = "0";
		}
		
		int iDaysOfWeekTotal = 0;
		if (clsManageRequestParameters.get_Request_Parameter(ASEditEventSchedulesEdit.DAYOFWEEKLABEL_SUNDAY, req).compareToIgnoreCase("") != 0){
			iDaysOfWeekTotal += 2;
		}
		if (clsManageRequestParameters.get_Request_Parameter(ASEditEventSchedulesEdit.DAYOFWEEKLABEL_MONDAY, req).compareToIgnoreCase("") != 0){
			iDaysOfWeekTotal += 4;
		}
		if (clsManageRequestParameters.get_Request_Parameter(ASEditEventSchedulesEdit.DAYOFWEEKLABEL_TUESDAY, req).compareToIgnoreCase("") != 0){
			iDaysOfWeekTotal += 8;
		}
		if (clsManageRequestParameters.get_Request_Parameter(ASEditEventSchedulesEdit.DAYOFWEEKLABEL_WEDNESDAY, req).compareToIgnoreCase("") != 0){
			iDaysOfWeekTotal += 16;
		}
		if (clsManageRequestParameters.get_Request_Parameter(ASEditEventSchedulesEdit.DAYOFWEEKLABEL_THURSDAY, req).compareToIgnoreCase("") != 0){
			iDaysOfWeekTotal += 32;
		}
		if (clsManageRequestParameters.get_Request_Parameter(ASEditEventSchedulesEdit.DAYOFWEEKLABEL_FRIDAY, req).compareToIgnoreCase("") != 0){
			iDaysOfWeekTotal += 64;
		}
		if (clsManageRequestParameters.get_Request_Parameter(ASEditEventSchedulesEdit.DAYOFWEEKLABEL_SATURDAY, req).compareToIgnoreCase("") != 0){
			iDaysOfWeekTotal += 128;
		}
		m_sdaysoftheweek = Integer.toString(iDaysOfWeekTotal);
		
	}
	public void load (ServletContext context, String sConf, String sUserID, String sUserFullName) throws Exception{
		Connection conn = clsDatabaseFunctions.getConnection(
				context, 
				sConf, 
				"MySQL", 
				this.toString() + " - user: " 
				+ sUserID
				+ " - "
				+ sUserFullName
				);

		if (conn == null){
			throw new Exception("Error [1481732470] opening data connection to load " + ParamObjectName + ".");
		}
		try {
			load (conn);
		} catch (Exception e) {
			clsDatabaseFunctions.freeConnection(context, conn);
			throw new Exception(e.getMessage());
		}
		clsDatabaseFunctions.freeConnection(context, conn);
	}
	public void load (Connection conn) throws Exception{
		load (m_slid, conn);
	}
	private void load (String sID, Connection conn) throws Exception{
		sID = sID.trim();
		if (sID.compareToIgnoreCase("") == 0){
			throw new Exception("Error [1481732471] - ID code cannot be blank when loading " + ParamObjectName + ".");
		}

		String SQL = "SELECT *"
				+ " FROM " + SMTablesseventschedules.TableName 
				+ " WHERE ("
					+ SMTablesseventschedules.lid + " = " + sID
				+ ")";
		if (bDebugMode){
			System.out.println("[1484083627] In " + this.toString() + " - load SQL = " + SQL);
		}
		try {
			ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
			if (rs.next()) {
				//Load the variables here:
				m_slid = Long.toString(rs.getLong(SMTablesseventschedules.lid));
				m_sname = rs.getString(SMTablesseventschedules.sname);
				m_sdescription = rs.getString(SMTablesseventschedules.sdescription).trim();
				m_sactive = Long.toString(rs.getLong(SMTablesseventschedules.iactive));
				m_sstarttime = Long.toString(rs.getLong(SMTablesseventschedules.istarttime));
				m_sdurationinminutes = Long.toString(rs.getLong(SMTablesseventschedules.idurationinminutes));
				m_sdaysoftheweek = Long.toString(rs.getLong(SMTablesseventschedules.idaysoftheweek));
				rs.close();
			} else {
				rs.close();
				throw new Exception(ParamObjectName + " does not exist.");
			}
		} catch (Exception e) {
			throw new Exception("Error [1481732808] reading " + ParamObjectName + " for lid : '" + sID
					+ "' - " + e.getMessage());
		}
		//Load the event schedule details:
		try {
			loadEventScheduleDevicesAndSequences(conn);
		} catch (Exception e) {
			throw new Exception(e.getMessage());
		}
		return;
	}
	public void loadEventScheduleDevicesAndSequences(ServletContext context, String sDBID, String sUser) throws Exception{
		Connection conn;
		try {
			conn = clsDatabaseFunctions.getConnectionWithException(
				context, 
				sDBID, 
				"MySQL", 
				SMUtilities.getFullClassName(this.toString()) + ".loadEventScheduleDevicesAndSequences - user: " + sUser
			);
		} catch (Exception e1) {
			throw new Exception("Error [1485970276] - could not get connections to load devices or sequences - " + e1.getMessage());
		}
		
		try {
			loadEventScheduleDevicesAndSequences(conn);
		} catch (Exception e) {
			clsDatabaseFunctions.freeConnection(context, conn);
			throw new Exception("Error [1485970274] - could not load devices or sequences - " + e.getMessage());
		}
		
		clsDatabaseFunctions.freeConnection(context, conn);
		
	}
	private void loadEventScheduleDevicesAndSequences(Connection conn) throws Exception{
		//Load the event schedule details:
		arrEventScheduleDetails.clear();
		String SQL = "SELECT * FROM " + SMTablesseventscheduledetails.TableName
			+ " WHERE ("
				+ "(" + SMTablesseventscheduledetails.lsseventscheduleid + " = " + getslid() + ")"
			+ ") ORDER BY " + SMTablesseventscheduledetails.lid
		;
		try {
			ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
			while (rs.next()){
				SSEventScheduleDetail detail = new SSEventScheduleDetail();
				detail.setsiactiontype(Long.toString(rs.getLong(SMTablesseventscheduledetails.iactiontype)));
				detail.setsiactivated(Long.toString(rs.getLong(SMTablesseventscheduledetails.iactivated)));
				detail.setsideviceoralarmsequence(Long.toString(rs.getLong(SMTablesseventscheduledetails.ideviceorsequence)));
				detail.setsiresetdelay(Long.toString(rs.getLong(SMTablesseventscheduledetails.iresetdelay)));
				detail.setsldeviceorsequenceid(Long.toString(rs.getLong(SMTablesseventscheduledetails.ldeviceorsequenceid)));
				detail.setslid(Long.toString(rs.getLong(SMTablesseventscheduledetails.lid)));
				detail.setslsseventscheduleid(Long.toString(rs.getLong(SMTablesseventscheduledetails.lsseventscheduleid)));
				arrEventScheduleDetails.add(detail);
			}
			rs.close();
		} catch (Exception e) {
			throw new Exception("Error [1481735391] - could not load event schedule detail - " + e.getMessage());
		}
	}

	public void save_without_data_transaction (ServletContext context, String sConf, String sUser, String sUserID , String sUserFullName) throws Exception{

		Connection conn = clsDatabaseFunctions.getConnection(
				context, 
				sConf, 
				"MySQL", 
				this.toString() + " - user: " 
				+ sUserID
				+ " - "
				+ sUserFullName
				);

		if (conn == null){
			throw new Exception("Error [1481732809] opening data connection.");
		}
		
		//Start a data transaction, so we can save to the triggers and alarm devices tables as well:
		try {
			clsDatabaseFunctions.start_data_transaction(conn);
		} catch (Exception e1) {
			clsDatabaseFunctions.freeConnection(context, conn);
			throw new Exception("Error [1481732810] starting data transaction - " + e1.getMessage());
		}
		try {
			save_without_data_transaction (conn, sUser, context);
		} catch (Exception e) {
			clsDatabaseFunctions.rollback_data_transaction(conn);
			clsDatabaseFunctions.freeConnection(context, conn);
			throw new Exception(e.getMessage());
		}
		clsDatabaseFunctions.commit_data_transaction(conn);
		clsDatabaseFunctions.freeConnection(context, conn);

	}
	public void save_without_data_transaction (Connection conn, String sUser, ServletContext context) throws Exception{

		try {
			validate_entry_fields(conn, context);
		} catch (Exception e1) {
			throw new Exception (e1.getMessage());
		}
		String SQL = "";
		
		//If it's a new record, do an insert:
		SQL = "INSERT INTO " + SMTablesseventschedules.TableName + " ("
				+ SMTablesseventschedules.iactive
				+ ", " + SMTablesseventschedules.idaysoftheweek
				+ ", " + SMTablesseventschedules.idurationinminutes
				+ ", " + SMTablesseventschedules.istarttime
				+ ", " + SMTablesseventschedules.sdescription
				+ ", " + SMTablesseventschedules.sname
				+ ") VALUES ("
				+ getsactive()
				+ ", " + getsdaysoftheweek()
				+ ", " + getsdurationinminutes()
				+ ", " + getsstarttime()
				+ ", '" + clsDatabaseFunctions.FormatSQLStatement(getsdescription().trim()) + "'"
				+ ", '" + clsDatabaseFunctions.FormatSQLStatement(getsname().trim()) + "'"
				+ ")"
				+ " ON DUPLICATE KEY UPDATE"
				+ " " + SMTablesseventschedules.iactive + " = " + getsactive()
				+ ", " + SMTablesseventschedules.idaysoftheweek + " = " + getsdaysoftheweek()
				+ ", " + SMTablesseventschedules.idurationinminutes + " = " + getsdurationinminutes()
				+ ", " + SMTablesseventschedules.istarttime + " = " + getsstarttime()
				+ ", " + SMTablesseventschedules.sdescription + " = '" + clsDatabaseFunctions.FormatSQLStatement(getsdescription().trim()) + "'"
				+ ", " + SMTablesseventschedules.sname + " = '" + clsDatabaseFunctions.FormatSQLStatement(getsname().trim()) + "'"
				;

		if (bDebugMode){
			System.out.println("In " + this.toString() + " - save SQL = " + SQL);
		}
		try{
			Statement stmt = conn.createStatement();
			stmt.executeUpdate(SQL);
		}catch (Exception ex) {
			throw new Exception ("Error [1481737505] in insert/update with SQL: " + SQL + " - " + ex.getMessage());
		}
		
		//Update the ID if it's an insert:
		if (m_slid.compareToIgnoreCase("-1") == 0){
			SQL = "SELECT last_insert_id()";
			try {
				ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
				if (rs.next()) {
					m_slid = Long.toString(rs.getLong(1));
				}else {
					m_slid = "0";
				}
				rs.close();
			} catch (SQLException e) {
				throw new Exception("Could not get last ID number - " + e.getMessage());
			}
			//If something went wrong, we can't get the last ID:
			if (m_slid.compareToIgnoreCase("0") == 0){
				throw new Exception("Could not get last ID number.");
			}
		}
		//Now save the trigger devices:
		try {
			saveEventScheduledetails(conn);
		} catch (Exception e) {
			throw new Exception("Error [1482170623] saving event schedule details - " + e.getMessage());
		}
	}
	
	private void deactivateDevicesAndSequences(Connection conn, ServletContext context, String sServerID) throws Exception {
		
		SMLogEntry log = new SMLogEntry(conn);
		for (int i = 0; i < arrEventScheduleDetails.size(); i++){
			//De-activate each device and alarm sequence:
			if (arrEventScheduleDetails.get(i).getsideviceoralarmsequence().compareToIgnoreCase(Integer.toString(SMTablesseventscheduledetails.DEVICEORSEQUENCE_DEVICE)) == 0){
				
				SSDevice dev = new SSDevice();
				dev.setslid(arrEventScheduleDetails.get(i).getsldeviceorsequenceid());
				dev.load(conn);
				
				dev.setOutputContactsState(conn, SSConstants.QUERY_KEYVALUE_TERMINAL_CONTACT_STATUS_OPEN, "0", SMEventScheduleHandler.SCHEDULE_MANAGER_USER, context, sServerID);
				
				log.writeEntry(
						SMBackgroundJobManager.SCHEDULING_USERID, 
						SMLogEntry.LOG_OPERATION_SSDEVICEUNSETBYSCHEDULE, 
						"Device ID " + dev.getslid() + " UNset by user updating schedule", 
						"Device: " + dev.getsdescription() + " (schedule was made inactive)" , 
						"[1484689707]");
				
				ASDeviceEventLogEntry devicelog = new ASDeviceEventLogEntry(conn);
				SSController outputcontroller = dev.getoutputcontroller(conn);
				try {
					devicelog.writeEntry(
						-1, 
						SMTablessdeviceevents.DEVICE_EVENT_TYPE_DEACTIVATED, 
						Long.parseLong(dev.getslid()), 
						dev.getsdescription(), 
						Long.parseLong(dev.getsoutputcontrollerid()), 
						outputcontroller.getscontrollername(), 
						outputcontroller.getsdescription(), 
						Long.parseLong(getslid()), 
						getsname(), 
						getsdescription(), 
						dev.getsoutputterminalnumber(), 
						"Device UNSET by user updating schedule", 
						"[1485971746]", 
						null
					);
				} catch (Exception e) {
					throw new Exception("Error [1459215971] writing to device event log - " + e.getMessage());
				}
				
			}else{
				SSAlarmSequence seq = new SSAlarmSequence();
				seq.setslid(arrEventScheduleDetails.get(i).getsldeviceorsequenceid());
				seq.setArmedState(
						conn, 
						SMEventScheduleHandler.SCHEDULE_MANAGER_USER, 
						SMEventScheduleHandler.SCHEDULE_MANAGER_USERID, 
						SMTablessalarmsequences.ALARM_STATE_UNARMED, 
						true, 
						context, 
						sServerID);
				
				log.writeEntry(
					SMBackgroundJobManager.SCHEDULING_USER, 
					SMLogEntry.LOG_OPERATION_SSSEQUENCEUNSETBYSCHEDULE, 
					"Alarm sequence ID " + seq.getslid() + " UNset by user updating schedule", 
					"Sequence: " + seq.getsdescription() + " (schedule was made inactive)", 
					"[1484689971]");
				
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
						Long.parseLong(getslid()), 
						getsname(), 
						getsdescription(), 
						"-1", 
						"Alarm sequence UNSET by user updating schedule", 
						"[1485971626]", 
						null
					);
				} catch (Exception e) {
					throw new Exception("Error [1459215381] writing to device event log - " + e.getMessage());
				}

			}
		}
		//Now we have to update the schedule details 'activated' flag so that the system knows these details have been de-activated
		String SQL = "UPDATE " + SMTablesseventscheduledetails.TableName
			+ " SET " + SMTablesseventscheduledetails.iactivated + " = 0"
			+ " WHERE ("
				+ "(" + SMTablesseventscheduledetails.lsseventscheduleid + " = " + getslid() + ")"
			+ ")"
		;
		try {
			Statement stmt = conn.createStatement();
			stmt.execute(SQL);
		} catch (Exception e) {
			throw new Exception("Error [1484689970] updating activation state of schedule details with SQL: " + SQL + " - " + e.getMessage());
		}
	}
	
	public void delete (ServletContext context, String sConf, String sUser, String sUserID, String sUserFullName) throws Exception{

		Connection conn = clsDatabaseFunctions.getConnection(
				context, 
				sConf, 
				"MySQL", 
				this.toString() + " - user: " 
				+ sUserID
				+ " - "
				+ sUserFullName
				);

		if (conn == null){
			throw new Exception ("Error [1481732939] opening data connection.");
		}

		try {
			delete (conn);
		} catch (Exception e) {
			clsDatabaseFunctions.freeConnection(context, conn);
			throw new Exception(e.getMessage());
		}
		clsDatabaseFunctions.freeConnection(context, conn);
	}
	public void delete (Connection conn) throws Exception{

		//Validate deletions
		String SQL = "";
		//TODO - figure out what rules we need to implement before deleting:
		//For example, we can't delete a schedule if it's currently in use
		//if (
		//	(getsalarmstate().compareToIgnoreCase(Integer.toString(SMTablessalarmsequences.ALARM_STATE_ARMED)) == 0)
		//	|| (getsalarmstate().compareToIgnoreCase(Integer.toString(SMTablessalarmsequences.ALARM_STATE_TRIGGERED)) == 0)
		//){
		//	throw new Exception("You cannot delete an alarm sequence while it is " + SMTablessalarmsequences.getAlarmStateLabel(Integer.parseInt(getsalarmstate())) + ".");
		//}

		//Delete:
		//Delete all the details:
		if (!clsDatabaseFunctions.start_data_transaction(conn)){
			throw new Exception("Error [1481814168] - Could not start transaction when deleting " + ParamObjectName + ".");
		}

		SQL = "DELETE FROM " + SMTablesseventscheduledetails.TableName
				+ " WHERE ("
				+ SMTablesseventscheduledetails.lsseventscheduleid + " = " + getslid()
				+ ")"
				;
		try{
			Statement stmt = conn.createStatement();
			stmt.executeUpdate(SQL);
		}catch (Exception ex) {
			clsDatabaseFunctions.rollback_data_transaction(conn);
			throw new Exception("Error [1481814165] - Could not delete " + ParamObjectName + getsname() 
					+ " DETAILS with SQL: " + SQL + " - " + ex.getMessage());
		}
		
		SQL = "DELETE FROM " + SMTablesseventschedules.TableName
				+ " WHERE ("
				+ SMTablesseventschedules.lid + " = " + getslid()
				+ ")"
				;
		try{
			Statement stmt = conn.createStatement();
			stmt.executeUpdate(SQL);
		}catch (Exception ex) {
			clsDatabaseFunctions.rollback_data_transaction(conn);
			throw new Exception("Error [1481814166] - Could not delete " + ParamObjectName + getsname() 
					+ " with SQL: " + SQL + " - " + ex.getMessage());
		}

		if (!clsDatabaseFunctions.commit_data_transaction(conn)){
			clsDatabaseFunctions.rollback_data_transaction(conn);
			throw new Exception("Error [1481814167] - Could not commit data transaction while deleting " + ParamObjectName + ".");
		}
		//Empty the values:
		initRecordVariables();
	}

	public void validate_entry_fields (Connection conn, ServletContext context) throws Exception{
		//Validate the entries here:
		String sErrors = "";
		m_slid = m_slid.trim();
		if (m_slid.compareToIgnoreCase("") == 0){
			m_slid = "-1";
		}
		try {
			@SuppressWarnings("unused")
			long lID = Long.parseLong(m_slid);
		} catch (Exception e) {
			throw new Exception("Invalid ID: '" + m_slid + "'.");
		}

		m_sname = m_sname.trim();
		if (m_sname.length() > SMTablesseventschedules.snamelength){
			sErrors += "Name cannot be more than " + Integer.toString(SMTablesseventschedules.snamelength) + " characters.  ";
		}
		if (m_sname.compareToIgnoreCase("") == 0){
			sErrors += "Name cannot be blank.  ";
		}
		
		m_sdescription = m_sdescription.trim();
		if (m_sdescription.length() > SMTablesseventschedules.sdescriptionlength){
			sErrors += "Description cannot be more than " + Integer.toString(SMTablesseventschedules.sdescriptionlength) + " characters.  ";
		}
		if (m_sdescription.compareToIgnoreCase("") == 0){
			sErrors += "Description cannot be blank.  ";
		}
		
		m_sactive = m_sactive.trim();
		if (
			(m_sactive.compareToIgnoreCase("0") != 0) 
			&& (m_sactive.compareToIgnoreCase("1") != 0)
		){
			sErrors += "Invalid value for 'active' - '" + m_sactive + "'.  ";
		}
		
		m_sstarttime = m_sstarttime.trim();
		if (m_sstarttime.compareToIgnoreCase("") == 0){
			m_sstarttime = "0";
		}
		try {
			long lstarttime = Long.parseLong(m_sstarttime);
			if (lstarttime > (24L * 60L)){
				sErrors += "Start time is invalid.  ";
			}
		} catch (Exception e) {
			throw new Exception("Invalid start time: '" + m_sstarttime + "'.");
		}
		
		m_sdurationinminutes = m_sdurationinminutes.trim();
		if (m_sdurationinminutes.compareToIgnoreCase("") == 0){
			m_sdurationinminutes = "0";
		}
		try {
			long ldurationinminutes = Long.parseLong(m_sdurationinminutes);
			if ((ldurationinminutes > (24L * 60L)) || (ldurationinminutes == 0)){
				sErrors += "Duration '" +  m_sdurationinminutes + "' is invalid.  ";
			}
		} catch (Exception e) {
			throw new Exception("Invalid duration: '" + m_sdurationinminutes + "'.");
		}
		
		m_sdaysoftheweek = m_sdaysoftheweek.trim();
		if (m_sdaysoftheweek.compareToIgnoreCase("") == 0){
			m_sdaysoftheweek = "0";
		}
		try {
			long ldaysofweek = Long.parseLong(m_sdaysoftheweek);
			if (ldaysofweek == 0){
				sErrors += "You must select at least one day of the week.  ";
			}
		} catch (Exception e) {
			throw new Exception("Invalid days of the week chosen.");
		}
		
		//Do not allow schedule details to overlap - no device or sequence can be scheduled through the same time period
		//in two different schedules:

		sErrors += checkForTimeOverlaps(conn);

		if (sErrors.compareToIgnoreCase("") != 0){
			throw new Exception(sErrors);
		}
		
		//If we are making this schedule inactive, we have to turn off any devices and sequences:
		if (getslid().compareToIgnoreCase("-1") != 0){
			SSEventSchedule testsched = new SSEventSchedule();
			testsched.setslid(getslid());
			if(testsched.isCurrentlyLive(conn, false)){
				if (getsactive().compareToIgnoreCase("0") == 0){
					//Make sure all the devices and sequences are turned off:
					deactivateDevicesAndSequences(conn, context, SMBackgroundScheduleProcessor.getServerID(context));
				}
			}
		}
	}

	public String checkForTimeOverlaps(Connection conn) throws Exception{
		
		String sComparisonResults = "";
		ArrayList<Long>arrStartingTimes = new ArrayList<Long>(0);
		ArrayList<Long>arrEndingTimes = new ArrayList<Long>(0);
		ArrayList<Long>arrExistingStartingTimes = new ArrayList<Long>(0);
		ArrayList<Long>arrExistingEndingTimes = new ArrayList<Long>(0);
		
		loadDailySchedulesIntoArrays (
			arrStartingTimes,
			arrEndingTimes,
			Long.parseLong(getsdaysoftheweek()),
			Long.parseLong(getsstarttime()),
			Long.parseLong(getsdurationinminutes())
		);

		//DEBUG:
		//for (int i = 0; i < arrStartingTimes.size(); i++){
			//System.out.println("[1484677033] Day " + i + " starting time: " + arrStartingTimes.get(i) + ", ending time: " + arrEndingTimes.get(i));
		//}
		
		//Now compare these starting and ending times to the existing records, just to be sure we haven't created an
		// 'overlapping' record:
		String SQL = "SELECT"
			+ " * FROM " + SMTablesseventscheduledetails.TableName
			+ " LEFT JOIN " + SMTablesseventschedules.TableName + " ON "
			+ SMTablesseventscheduledetails.TableName + "." + SMTablesseventscheduledetails.lsseventscheduleid
			+ " = " + SMTablesseventschedules.TableName + "." + SMTablesseventschedules.lid
			+ " WHERE ("
				+ "(" + SMTablesseventscheduledetails.lsseventscheduleid + " != " + this.getslid() + ")"
			+ ")"
			;
		
		ResultSet rsOtherSchedules = clsDatabaseFunctions.openResultSet(SQL, conn);
		
		try {
			while (rsOtherSchedules.next()){
				boolean bConflictWasFoundWithThisRecord = false;
				arrExistingStartingTimes.clear();
				arrExistingEndingTimes.clear();
				//Does this record include a device or sequence that the current schedule includes?  If not, we don't have to check for a conflict.
				for (int i = 0; i < arrEventScheduleDetails.size(); i++){
					if (bConflictWasFoundWithThisRecord){
						break;
					}
					if (Long.toString(rsOtherSchedules.getLong(SMTablesseventscheduledetails.TableName + "." 
						+ SMTablesseventscheduledetails.ldeviceorsequenceid)).compareTo(arrEventScheduleDetails.get(i).getsldeviceorsequenceid()) == 0){
						//Then we have to check for a conflict:
						loadDailySchedulesIntoArrays (
							arrExistingStartingTimes,
							arrExistingEndingTimes,
							rsOtherSchedules.getLong(SMTablesseventschedules.TableName + "." + SMTablesseventschedules.idaysoftheweek),
							rsOtherSchedules.getLong(SMTablesseventschedules.TableName + "." + SMTablesseventschedules.istarttime),
							rsOtherSchedules.getLong(SMTablesseventschedules.TableName + "." + SMTablesseventschedules.idurationinminutes)
						);

						//DEBUG:
						//for (int z = 0; z < arrExistingStartingTimes.size(); z++){
						//	System.out.println("[1484677034] Day " + z + " EXISTING starting time: " + arrExistingStartingTimes.get(z) 
						//	+ ", EXISTING ending time: " + arrExistingEndingTimes.get(z));
						//}
						
						//Now compare this schedule against the existing schedule to make sure that there are no conflicts:
						String sResults = compareIndividualTimes(
								rsOtherSchedules.getLong(SMTablesseventscheduledetails.TableName + "." + SMTablesseventscheduledetails.lsseventscheduleid),
								arrStartingTimes, 
								arrEndingTimes,
								arrExistingStartingTimes,
								arrExistingEndingTimes 
							);
						
						//If there was any conflict, there's no need to check this schedule any more:
						if (sResults.compareTo("") != 0){
							sComparisonResults += sResults;
							bConflictWasFoundWithThisRecord = true;
							break;
						}
					}
				}
			}
			rsOtherSchedules.close();
		} catch (Exception e) {
			throw new Exception("Error [1484572359] checking for schedule overlaps - " + e.getMessage());
		}
		return sComparisonResults;
	}
	
	private String compareIndividualTimes(
			long lScheduleID,
			ArrayList<Long>arrStartTimes1, 
			ArrayList<Long>arrEndTimes1,
			ArrayList<Long>arrStartTimes2,
			ArrayList<Long>arrEndTimes2 
			){

		for (int k = 0; k < arrStartTimes1.size(); k++){
			//Unless either the starting or ending time is NOT zero, there can't be an conflict:
			if ((arrStartTimes1.get(k) != 0) || (arrEndTimes1.get(k) != 0)) {
				for (int l = 0; l < arrStartTimes2.size(); l++){
					//Unless either the starting or ending time is NOT zero, there can't be an conflict:
					if ((arrStartTimes2.get(k) != 0) || (arrEndTimes2.get(k) != 0)) {
						if (
								//If a daily starting time for THIS schedule starts between the starting and ending times of an existing schedule, then this is a conflict:
								(arrStartTimes1.get(k) >= arrStartTimes2.get(l))
								&& (arrStartTimes1.get(k) <= arrEndTimes2.get(l))
								){
							//Record conflict
							return "Schedule ID " + Long.toString(lScheduleID)
							+ " has overlapping times for some of these devices or sequences.  ";
						}

						//Now we have to check the opposite:
						if (
								//If a daily starting time for the existing schedule starts between the starting and ending times of THIS schedule, then this is a conflict:
								(arrStartTimes2.get(k) >= arrStartTimes1.get(l))
								&& (arrStartTimes2.get(k) <= arrEndTimes1.get(l))
								){
							//Record conflict
							return "Schedule ID " + Long.toString(lScheduleID)
							+ " has overlapping times for some of these devices or sequences.  ";
						}

						if (
								//If a daily ending time for THIS schedule ends between the starting and ending times of an existing schedule, then this is a conflict:
								(arrEndTimes1.get(k) >= arrStartTimes2.get(l))
								&& (arrEndTimes1.get(k) <= arrEndTimes2.get(l))
								){
							//Record conflict
							return "Schedule ID " + Long.toString(lScheduleID)
							+ " has overlapping times for some of these devices or sequences.  ";
						}

						if (
								//If a daily ending time for the EXISTING schedule ends between the starting and ending times of THIS schedule, then this is a conflict:
								(arrEndTimes2.get(k) >= arrStartTimes1.get(l))
								&& (arrEndTimes2.get(k) <= arrEndTimes1.get(l))
								){
							//Record conflict
							return "Schedule ID " + Long.toString(lScheduleID)
							+ " has overlapping times for some of these devices or sequences.  ";
						}
					}
				}
			}
		}
		return "";
	}
	
	private void loadDailySchedulesIntoArrays (
		ArrayList<Long>StartingTimeArray,
		ArrayList<Long>EndingTimeArray,
		long lDaysOfTheWeek,
		long lDailyStartTime,
		long lDuration
			){
		
		//First, load the Starting and ending times into the 'current record' array:
		//Sunday:
		long lMinutesInOneDay = 24L * 60L;  //This is the time in minutes from 12:00AM Sunday in the week - it starts with zero.
		
		for (int iDayOfWeek = Calendar.SUNDAY; iDayOfWeek <= Calendar.SATURDAY; iDayOfWeek++){
			double dDayOfWeek = new Double(iDayOfWeek);
			if (
				(Integer.parseInt(
					Double.toString(
						Math.pow((2.0), (dDayOfWeek))
							).replace(".0", "")
				) 
				& lDaysOfTheWeek) > 0){
				//Calculate the beginning and ending time for the Sunday schedule:
				StartingTimeArray.add(((iDayOfWeek - 1) * lMinutesInOneDay) + lDailyStartTime);
				EndingTimeArray.add(StartingTimeArray.get(StartingTimeArray.size() - 1) + lDuration);
			}else{
				StartingTimeArray.add(0L);
				EndingTimeArray.add(0L);
			}
		}
	}
	
	public void addDetail(SSEventScheduleDetail detail){
		arrEventScheduleDetails.add(detail);
	}
	
	public String getslid() {
		return m_slid;
	}

	public void setslid(String slid) {
		m_slid = slid;
	}

	public String getsname() {
		return m_sname;
	}

	public void setsname(String sname) {
		m_sname = sname;
	}

	public String getsdescription() {
		return m_sdescription;
	}

	public void setsdescription(String sdescription) {
		m_sdescription = sdescription;
	}
	
	public String getsactive() {
		return m_sactive;
	}

	public void setsactive(String sactive) {
		m_sactive = sactive;
	}
	
	public String getsstarttime() {
		return m_sstarttime;
	}

	public void setsstarttime(String sstarttime) {
		m_sstarttime = sstarttime;
	}
	
	public String getsdurationinminutes() {
		return m_sdurationinminutes;
	}
	public void setsdurationinminutes(String sdurationinminutes) {
		m_sdurationinminutes = sdurationinminutes;
	}
	public String getsdaysoftheweek(){
		return m_sdaysoftheweek;
	}
	public void setsdaysoftheweek(String sdaysoftheweek) {
		m_sdaysoftheweek = sdaysoftheweek;
	}
	
	public String getObjectName(){
		return ParamObjectName;
	}

	public ArrayList<SSEventScheduleDetail> getEventScheduleDetails() {
		return arrEventScheduleDetails;
	}
	
	public SSEventScheduleDetail getEventScheduleDetail(String ideviceorsequence, String ldeviceorsequenceid){
		for(int i = 0; i < arrEventScheduleDetails.size(); i++){
			if(arrEventScheduleDetails.get(i).getsideviceoralarmsequence().compareToIgnoreCase(ideviceorsequence) == 0
				&& arrEventScheduleDetails.get(i).getsldeviceorsequenceid().compareToIgnoreCase(ldeviceorsequenceid) == 0){
				return arrEventScheduleDetails.get(i);
			}
		}		
		return null;
	}
	
	public void addEventScheduleDetail(
		String sEventScheduleID,
		String sDeviceOrSequenceID, 
		String sActionType, 
		String sDeviceOrAlarmSequence,
		String sResetDelay
		) throws Exception{
		SSEventScheduleDetail detail = new SSEventScheduleDetail();
		detail.setsiactiontype(sActionType);
		detail.setsideviceoralarmsequence(sDeviceOrAlarmSequence);
		detail.setsiresetdelay(sResetDelay);
		detail.setsldeviceorsequenceid(sDeviceOrSequenceID);
		detail.setslsseventscheduleid(sEventScheduleID);
		arrEventScheduleDetails.add(detail);
	}

	public void removeEventScheduleDetail(String sDetailID, ServletContext context, String sConf, String sUser, String sUserID, String sUserFullName) throws Exception{
		
		//Must make the schedule inactive before removing devices:
		if (getsactive().compareToIgnoreCase("1") == 0){
			throw new Exception("Error [1484685862] - you must make a schedule INactive before removing any devices.");
		}
		
		for (int i = 0; i < arrEventScheduleDetails.size(); i++){
			if (arrEventScheduleDetails.get(i).getslid().compareToIgnoreCase(sDetailID) == 0){
				Connection conn = clsDatabaseFunctions.getConnection(
						context, 
						sConf, 
						"MySQL", 
						this.toString() + " - user: " 
						+ sUserID
						+ " - "
						+ sUserFullName
						);

				if (conn == null){
					throw new Exception("Error [1484250249] opening data connection.");
				}
				
				try {
					Statement stmt = conn.createStatement();
					String SQL = "DELETE FROM " + SMTablesseventscheduledetails.TableName
						+ " WHERE ("
							+ "(" + SMTablesseventscheduledetails.lid + " = " + sDetailID + ")"
						+ ")"
					;
					stmt.execute(SQL);
					clsDatabaseFunctions.freeConnection(context, conn);
				} catch (Exception e) {
					throw new Exception ("Error [1487719040] deleting schedule detail - " + e.getMessage() + ".");
				}
				break;
			}
		}
	}

	private void saveEventScheduledetails(Connection conn) throws Exception{
		
		//Now insert all the current devices and/or sequences:
		for (int i = 0; i < arrEventScheduleDetails.size(); i++){
			String SQL = "INSERT INTO " + SMTablesseventscheduledetails.TableName + "("
				+ SMTablesseventscheduledetails.iactiontype
				+ ", " + SMTablesseventscheduledetails.iactivated
				+ ", " + SMTablesseventscheduledetails.ideviceorsequence
				+ ", " + SMTablesseventscheduledetails.iresetdelay
				+ ", " + SMTablesseventscheduledetails.ldeviceorsequenceid
				+ ", " + SMTablesseventscheduledetails.lsseventscheduleid
				+ ") VALUES ("
				+ arrEventScheduleDetails.get(i).getsiactiontype()
				+ ", " + arrEventScheduleDetails.get(i).getsiactivated()
				+ ", " + arrEventScheduleDetails.get(i).getsideviceoralarmsequence()
				+ ", " + arrEventScheduleDetails.get(i).getsiresetdelay()
				+ ", " + arrEventScheduleDetails.get(i).getsldeviceorsequenceid()
				+ ", " + getslid()
				+ ")"
				+ " ON DUPLICATE KEY UPDATE"
				+ " " + SMTablesseventscheduledetails.iactiontype + " = " + arrEventScheduleDetails.get(i).getsiactiontype()
				+ ", " + SMTablesseventscheduledetails.iactivated + " = " + arrEventScheduleDetails.get(i).getsiactivated()
				+ ", " + SMTablesseventscheduledetails.iresetdelay + " = " + arrEventScheduleDetails.get(i).getsiresetdelay()
			;
			try {
				Statement stmt = conn.createStatement();
				stmt.execute(SQL);
			} catch (Exception e) {
				throw new Exception ("Error [1481838065] adding device/sequence with ID '" 
					+ arrEventScheduleDetails.get(i).getslid() + " - " + e.getMessage());
			}
		}
	}

	public boolean isCurrentlyLive(Connection conn, boolean bOutPutDiagnostics) throws Exception{
		
		//First, make sure it's a saved schedule:
		if (getslid().compareToIgnoreCase("-1") != 0){
			//Next, try to load the schedule:
			try {
				load(conn);
			} catch (Exception e) {
				throw new Exception("Error [1481838064] - couldn't load event schedule with ID '" + getslid() + "' - " + e.getMessage());
			}
			//If we can load the schedule, then if it's an ACTIVE schedule:
			if (getsactive().compareToIgnoreCase("1") == 0){
				//Now, check to see if the current time is within the scheduled time to run:
				
				clsDBServerTime st;
				try {
					st = new clsDBServerTime(conn);
				} catch (Exception e1) {
					throw new Exception("Error [1494252358] - in " + this.toString() + " - " + e1.getMessage());
				}
				
				//Current day of week:
				//Calendar calendar = Calendar.getInstance();
				//int iToday = calendar.get(Calendar.DAY_OF_WEEK);  //Sunday is '1'
				int iToday = st.getCurrentDayOfWeek();
				
				//Now get the current time in minutes:
				//Calendar now = Calendar.getInstance();
				long lCurrentTimeInMinutes = st.getCurrentTimeInMinutes();				
				
				
				//Get the integer for yesterday:
				int iYesterday = iToday - 1;
				if (iYesterday <= 0){
					iYesterday = Calendar.SATURDAY;
				}
				
				int iDaysOfWeekToRun = 0;
				try {
					iDaysOfWeekToRun = Integer.parseInt(getsdaysoftheweek());
				} catch (Exception e) {
					throw new Exception ("Error [1481838063] couldn't convert days of week '" + getsdaysoftheweek() + "' to integer");
				}
				
				long lStartingTimeInMinutes = 0;
				try {
					lStartingTimeInMinutes = Long.parseLong(getsstarttime());
				} catch (Exception e) {
					throw new Exception ("Error [1481838073] couldn't convert starting time '" + getsstarttime() + "' to long integer");
				}
				
				long lDurationInMinutes = 0;
				try {
					lDurationInMinutes = Long.parseLong(getsdurationinminutes());
				} catch (Exception e) {
					throw new Exception ("Error [1484015060] couldn't convert duration in minutes '" + getsdurationinminutes() + "' to long integer");
				}
				
				//First, get the value for midnight in minutes:
				//Calendar midnight = Calendar.getInstance();
				//midnight.set(Calendar.HOUR_OF_DAY, 0);
				//midnight.set(Calendar.MINUTE, 0);
				//midnight.set(Calendar.SECOND, 0);
				//midnight.set(Calendar.MILLISECOND, 0);
				//long lMidnightTodayInMinutes = midnight.getTimeInMillis()/(60L * 1000L);
				long lMidnightTodayInMinutes = st.getLastMidnightInMinutes();
				
				//Get today's scheduled starting and ending times:
				long lTodaysStartingTimeInMinutes = lMidnightTodayInMinutes + lStartingTimeInMinutes;
				long lTodaysEndingTimeInMinutes = lTodaysStartingTimeInMinutes + lDurationInMinutes;
				
				//Get yesterday's scheduled starting and ending times:
				long lYesterdaysStartingTimeInMinutes = lTodaysStartingTimeInMinutes - (24L * 60L);
				long lYesterdaysEndingTimeInMinutes = lTodaysEndingTimeInMinutes - (24L * 60L);
				
				//See if the schedule was supposed to be started yesterday:
				//if (((2^iYesterday) & (iDaysOfWeekToRun)) > 0){
				//Get the power of 2 raised to the day of the week:
				int iPowerOf2RaisedToYesterdayOfWeek = Integer.parseInt(Double.toString(Math.pow((double) 2, (double) iYesterday)).replace(".0", ""));
				int iPowerOf2RaisedToCurrentDayOfWeek = Integer.parseInt(Double.toString(Math.pow((double) 2, (double) iToday)).replace(".0", ""));
				
				//Print debug info:
				if (bDebugMode || bOutPutDiagnostics){
					System.out.println("[1484069702] TIME: " + clsDateAndTimeConversions.nowStdFormatWithSeconds());
					System.out.println("[1484069703] iToday = '" + iToday + "'");
					System.out.println("[1484069704] iYesterday = '" + iYesterday + "'");
					System.out.println("[1484069705] iDaysOfWeekToRun = '" + iDaysOfWeekToRun + "'");
					System.out.println("[1484069706] iDaysOfWeekToRun = '" + iDaysOfWeekToRun + "'");
					System.out.println("[1484069707] lStartingTimeInMinutes = '" + lStartingTimeInMinutes + "'");
					System.out.println("[1484069708] lDurationInMinutes = '" + lDurationInMinutes + "'");
					System.out.println("[1484069709] lCurrentTimeInMinutes = '" + lCurrentTimeInMinutes + "'");
					System.out.println("[1484069710] lMidnightTodayInMinutes = '" + lMidnightTodayInMinutes + "'");
					System.out.println("[1484069711] lTodaysStartingTimeInMinutes = '" + lTodaysStartingTimeInMinutes + "'");
					System.out.println("[1484069712] lTodaysEndingTimeInMinutes = '" + lTodaysEndingTimeInMinutes + "'");
					System.out.println("[1484069713] lYesterdaysStartingTimeInMinutes = '" + lYesterdaysStartingTimeInMinutes + "'");
					System.out.println("[1484069714] lYesterdaysEndingTimeInMinutes = '" + lYesterdaysEndingTimeInMinutes + "'");
					System.out.println("[1484069715] iPowerOf2RaisedToYesterdayOfWeek = '" + iPowerOf2RaisedToYesterdayOfWeek + "'");
					System.out.println("[1484069716] iPowerOf2RaisedToCurrentDayOfWeek = '" + iPowerOf2RaisedToCurrentDayOfWeek + "'");
				}
				
				if (((iPowerOf2RaisedToYesterdayOfWeek)&(iDaysOfWeekToRun)) > 0){
					//If the current time is between yesterday's scheduled start and stop time, then this schedule is 'live':
					if (
						(lCurrentTimeInMinutes >= lYesterdaysStartingTimeInMinutes)
						&& (lCurrentTimeInMinutes <= lYesterdaysEndingTimeInMinutes)
							
					){
						return true;
					}
					
				}
				
				//Now check today's schedule:
				if (((iPowerOf2RaisedToCurrentDayOfWeek)&(iDaysOfWeekToRun)) > 0){
					//If the current time is between today's scheduled start and stop time, then this schedule is 'live':
					if (
						(lCurrentTimeInMinutes >= lTodaysStartingTimeInMinutes)
						&& (lCurrentTimeInMinutes <= lTodaysEndingTimeInMinutes)
							
					){
						return true;
					}
					
				}
			}
		}

		return false;
	}
	
	private void initRecordVariables(){
		m_slid = "-1";
		m_sname = "";
		m_sdescription = "";
		m_sactive = "0";
		m_sstarttime = "0";
		m_sdurationinminutes = "0";
		m_sdaysoftheweek = "0";
		arrEventScheduleDetails = new ArrayList<SSEventScheduleDetail>(0);
	}
/*
	public void setArmedState(
			Connection conn, 
			String sUserName,
			int iArmingState,
			boolean bOverrideMalfunctioningDevices
			) throws Exception{
		
		//Set the alarm state:
		//If the object is not loaded, load it now:
		//try {
		//	load(conn);
		//} catch (Exception e) {
		//	throw new Exception("Error [1459444138] loading the alarm sequence with ID '" + getslid() + "' - " + e.getMessage());
		//}
		
		//First, if the sequence is being 'unarmed', then de-activate any activation devices:
		if (iArmingState == SMTablessalarmsequences.ALARM_STATE_UNARMED){
			for (int i = 0; i < arrActivationDevices.size(); i++){
				SSDevice device = arrActivationDevices.get(i);
				device.load(conn);
				//We are assuming that any 'activation' device gets de-activated by CLOSING THE CONTACTS:
				String sSetContactState = SSConstants.QUERY_KEYVALUE_TERMINAL_CONTACT_STATUS_OPEN;
				String sActivationDuration = "0";
				device.setOutputContactsState(conn, sSetContactState, sActivationDuration, sUserName);
			}
		}
		
		String sListeningState = "";
		if (iArmingState == SMTablessalarmsequences.ALARM_STATE_ARMED){
			sListeningState = SSConstants.QUERY_KEYVALUE_TERMINAL_LISTENING_STATUS_LISTENING;
			String sCheckingTriggerDeviceResults = checkInputContactsBeforeArming(conn, sUserName);
			if (sCheckingTriggerDeviceResults.contains(TRIGGER_DEVICE_SET_ERROR)){
				//If some of the trigger devices could not be set, then we need to decide whether to go ahead and arm or not:
				//If the use chose to OVERRIDE the malfunctioning trigger devices, then record that and let them go ahead:
				if (bOverrideMalfunctioningDevices){
					//Log the override and let it to on through:
					ASUserEventLogEntry usereventlog = new ASUserEventLogEntry(conn);
					try {
						usereventlog.writeEntry(
								SMTablessuserevents.ALARM_ZONE_ARMED, 
								sUserName, 
								"N/A", 
								"N/A",
								"0",
								"", 
								sCheckingTriggerDeviceResults, 
								"[1480452758]", 
								null,
								getslid());
					} catch (Exception e) {
						throw new Exception(
								"Error [1480452757] recording the alarm set override in the user events log - " + e.getMessage() + "."
								);
					}
				}else{
					throw new Exception (sCheckingTriggerDeviceResults);
				}
			}
		}
		if (iArmingState == SMTablessalarmsequences.ALARM_STATE_UNARMED){
			sListeningState = SSConstants.QUERY_KEYVALUE_TERMINAL_LISTENING_STATUS_NOTLISTENING;
		}
		
		//Set all the pins in the alarm sequence to 'listen' or 'not listen':
		//If we are setting the 'arming state' to 'triggered', then we don't change the listening status:
		if (iArmingState != SMTablessalarmsequences.ALARM_STATE_TRIGGERED){
			try {
				setPinsToListeningState(conn, sListeningState, sUserName);
			} catch (Exception e1) {
				throw new Exception(e1.getMessage());
			}
		}
		//Save the armed state:
		//Get the user's full name:
		String sUserFullName = "(NOT FOUND)";
		String SQL = "SELECT"
			+ " " + SMTableusers.sUserFirstName
			+ ", " + SMTableusers.sUserLastName
			+ " FROM " + SMTableusers.TableName
			+ " WHERE ("
				+ "(" + SMTableusers.sUserName + " = '" + sUserName + "')"
			+ ")"
		;
		try {
			ResultSet rs = SMUtilities.openResultSet(SQL, conn);
			
			if (rs.next()){
				sUserFullName = rs.getString(SMTableusers.sUserFirstName) + " " + rs.getString(SMTableusers.sUserLastName);
			}
			rs.close();
		} catch (Exception e1) {
			//Don't stop for this - keep going....
		}
		
		SQL = "UPDATE " + SMTablessalarmsequences.TableName
			+ " SET " + SMTablessalarmsequences.ialarmstate + " = " + Integer.toString(iArmingState)
		;
		if (iArmingState == SMTablessalarmsequences.ALARM_STATE_ARMED){
			SQL += ", " + SMTablessalarmsequences.datlastarmed + " = NOW()"
				+ ", " + SMTablessalarmsequences.datlastdisarmed + " = '0000-00-00 00:00:00'"
				+ ", " + SMTablessalarmsequences.slastarmedby + " = '" + sUserName + "'"
				+ ", " + SMTablessalarmsequences.slastarmedbyfullname + " = '" + sUserFullName + "'"
			;
		}else if (iArmingState == SMTablessalarmsequences.ALARM_STATE_UNARMED){
			SQL += ", " + SMTablessalarmsequences.datlastarmed + " = '0000-00-00 00:00:00'"
				+ ", " + SMTablessalarmsequences.datlastdisarmed + " = NOW()" 
				+ ", " + SMTablessalarmsequences.slastarmedby + " = ''"
				+ ", " + SMTablessalarmsequences.slastarmedbyfullname + " = ''"
			;
		}else if (iArmingState == SMTablessalarmsequences.ALARM_STATE_TRIGGERED){
			//Don't update the 'last armed time, etc., if it's only being triggered...
		}
		SQL += " WHERE ("
				+ "(" + SMTablessalarmsequences.lid + " = " + getslid() + ")"
			+ ")"
		;
		try {
			Statement stmt = conn.createStatement();
			stmt.execute(SQL);
		} catch (Exception e) {
			throw new Exception("Error [1462409718] updating alarm sequence with SQL: " + SQL + " - " + e.getMessage());
		}
		
		//Then reload the alarm sequence so the class is up to date:
		try {
			load(conn);
		} catch (Exception e) {
			throw new Exception("Error [1462409719] re-loading the alarm sequence with ID '" + getslid() + "' - " + e.getMessage());
		}
	}
*/
/*
	public void initiateAlarmSequence(Connection conn, String sUserName, String sDeviceDescription, boolean bOverrideMalfunctioningDevice) throws Exception{
		
		//First, check the countdown:
		//If the alarm sequence is still in the 'countdown' phase,
		// just return and don't do anything:
		boolean bCountdownIsFinished = false;
		String SQL = "SELECT"
			+ " IF("
				+ "(UNIX_TIMESTAMP(NOW()) - UNIX_TIMESTAMP(" + SMTablessalarmsequences.datlastarmed + ")) > "
				+ SMTablessalarmsequences.lalarmsetdelaycountdown + ", 'Y', 'N') AS COUNTDOWNFINISHED"
			+ " FROM " + SMTablessalarmsequences.TableName
			+ " WHERE ("
				+ "(" + SMTablessalarmsequences.lid + " = " + getslid() + ")"
			+ ")"
		;
		try {
			ResultSet rs = SMUtilities.openResultSet(SQL, conn);
			if (rs.next()){
				if (rs.getString("COUNTDOWNFINISHED").compareToIgnoreCase("Y") == 0){
					bCountdownIsFinished = true;
				}
			}
			rs.close();
		} catch (Exception e) {
			throw new Exception ("Error [1462999433] checking alarm sequence countdown completion - " + e.getMessage());
		}
		//If the countdown isn't finished, then just return and don't do anything:
		if (!bCountdownIsFinished){
			return;
		}

		//But if the countdown is finished, start doing things:
		//If the alarm sequence is not already triggered, then send notifications and set the status to triggered:
		if (getsalarmstate().compareToIgnoreCase(Integer.toString(SMTablessalarmsequences.ALARM_STATE_TRIGGERED)) != 0){
			//Send a notification:
			try {
				emailTriggerNotifications(conn, sDeviceDescription);
			} catch (Exception e) {
				// If the email fails for any reason, record the error, but don't stop the process of triggering
				//and setting the alarm:
				SMLogEntry log = new SMLogEntry(conn);
				log.writeEntry(
					sUserName, 
					SMLogEntry.LOG_OPERATION_ASEMAILSENDERROR, 
					"Alarm " + this.getsname() + " was triggered; error sending email", 
					"Error: " + e.getMessage(), 
					"[1473957806]"
				);
			}
			//Set the state to 'triggered':
			setArmedState(conn, sUserName, SMTablessalarmsequences.ALARM_STATE_TRIGGERED, bOverrideMalfunctioningDevice);
		}
		
		//Activate the alarm devices every time there's a trigger:
		activateAlarmDevices(conn, sUserName);
		
	}
*/
/*
	private void activateAlarmDevices(Connection conn, String sUser) throws Exception{
		for (int i = 0; i < arrActivationDevices.size(); i++){
			SSDevice device = arrActivationDevices.get(i);
			//Don't activate inactive devices:
			if (device.getsactive().compareToIgnoreCase("1") == 0){
				device.load(conn);
				//We are assuming that any 'activation' device gets activated by CLOSING THE CONTACTS:
				String sSetContactState = SSConstants.QUERY_KEYVALUE_TERMINAL_CONTACT_STATUS_CLOSED;
				String sActivationDuration = "";
				//If the device is 'constant contact', then we need to know how long to activate it:
				if(device.getsactivationtype().compareToIgnoreCase(
						Integer.toString(SMTablessdevices.DEVICE_ACTIVATION_TYPE_CONSTANT_CONTACT)) == 0){
					sActivationDuration = arrActivationDeviceDurationsInMS.get(i);
				//But if it's 'momentary contact', then we need to just activate it for the default momentary contact time:
				}else{
					sActivationDuration = SSConstants.DEFAULT_MOMENTARY_CONTACT_ACTIVATE_INTERVAL_IN_MS;
				}
				device.setOutputContactsState(conn, sSetContactState, sActivationDuration, sUser);
			}
		}
	}
*/
}