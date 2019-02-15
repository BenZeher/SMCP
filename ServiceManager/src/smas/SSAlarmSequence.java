package smas;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import sscommon.SSConstants;
import SMClasses.SMLogEntry;
import SMDataDefinition.SMTablecompanyprofile;
import SMDataDefinition.SMTablesmoptions;
import SMDataDefinition.SMTablessalarmactivationdevices;
import SMDataDefinition.SMTablessalarmsequences;
import SMDataDefinition.SMTablessalarmtriggerdevices;
import SMDataDefinition.SMTablesscontrollers;
import SMDataDefinition.SMTablessdevices;
import SMDataDefinition.SMTablessuserevents;
import SMDataDefinition.SMTableusers;
import ServletUtilities.clsMasterEntry;
import smcontrolpanel.SMUtilities;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsDateAndTimeConversions;
import ServletUtilities.clsManageRequestParameters;

public class SSAlarmSequence extends clsMasterEntry{

	public static final String ParamObjectName = "Alarm Sequence";
	public static final String PARAM_REMOVE_TRIGGER_DEVICE_ID_PREFIX = "REMOVETRIGGERDEVICEID";
	public static final String PARAM_ADD_NEW_TRIGGER_DEVICE_BUTTON = "ADDTRIGGERDEVICE";
	public static final String PARAM_ADD_NEW_TRIGGER_DEVICE_LABEL = "Add this device";
	public static final String PARAM_NEW_TRIGGER_DEVICE_LIST = "NEWTRIGGER";
	public static final String PARAM_REMOVE_ACTIVATION_DEVICE_ID_PREFIX = "REMOVEACTIVATIONDEVICEID";
	public static final String PARAM_ADD_NEW_ACTIVATION_DEVICE_BUTTON = "ADDACTIVATIONDEVICE";
	public static final String PARAM_ADD_NEW_ACTIVATION_DEVICE_LABEL = "Add this device";
	public static final String PARAM_NEW_ACTIVATION_DEVICE_LIST = "NEWACTIVATION";
	public static final String PARAM_ACTIVATION_DURATION_IN_SECONDS = "ACTIVATIONDURATIONINSECONDS";
	public static final String TRIGGER_DEVICE_SET_ERROR = "DEVICE PROBLEM: ";
	
	private String m_slid;
	private String m_sname;
	private String m_sdescription;
	private String m_salarmstate;
	private String m_snotificationemails;
	private String m_salarmsetcountdown;
	private String m_sdatlastarmed;
	private String m_sdatlastdisarmed;
	private String m_llastarmedbyid;
	private String m_slastarmedbyfullname;
	private String m_llastdisarmedbyid;
	private String m_slastdisarmedbyfullname;
	private long m_ldatlastarmedunixtimestamp;
	private long m_ldatlastdisarmedunixtimestamp; //This will carry the 'UNIX' time in seconds
	private static ArrayList<SSDevice>arrTriggerDevices;
	private static ArrayList<SSDevice>arrActivationDevices;
	private static ArrayList<String>arrActivationDeviceDurationsInMS;
	private boolean bDebugMode = false;

	public SSAlarmSequence() {
		super();
		initRecordVariables();
	}

	SSAlarmSequence(HttpServletRequest req){
		super(req);
		initRecordVariables();
		m_slid = clsManageRequestParameters.get_Request_Parameter(
				SMTablessalarmsequences.lid, req).trim();
		if (m_slid.compareToIgnoreCase("") == 0){
			m_slid = "-1";
		}
		m_sname = clsManageRequestParameters.get_Request_Parameter(
				SMTablessalarmsequences.sname, req).trim();
		m_sdescription  = clsManageRequestParameters.get_Request_Parameter(
				SMTablessalarmsequences.sdescription, req).trim().replace("&quot;", "\"");
		m_salarmstate = clsManageRequestParameters.get_Request_Parameter(
				SMTablessalarmsequences.ialarmstate, req).trim().replace("&quot;", "\"");
		if (m_salarmstate.compareToIgnoreCase("") == 0){
			m_salarmstate = Integer.toString(SMTablessalarmsequences.ALARM_STATE_UNARMED);
		}
		m_snotificationemails  = clsManageRequestParameters.get_Request_Parameter(
			SMTablessalarmsequences.semailnotifications, req).trim().replace("&quot;", "\"");
		m_salarmsetcountdown = clsManageRequestParameters.get_Request_Parameter(
				SMTablessalarmsequences.lalarmsetdelaycountdown, req).trim().replace("&quot;", "\"");
		m_sdatlastarmed = clsManageRequestParameters.get_Request_Parameter(
				SMTablessalarmsequences.datlastarmed, req).trim().replace("&quot;", "\"");
		if(m_sdatlastarmed.compareToIgnoreCase("") == 0){
			m_sdatlastarmed = EMPTY_DATETIME_STRING;
		}
		m_sdatlastdisarmed = clsManageRequestParameters.get_Request_Parameter(
				SMTablessalarmsequences.datlastdisarmed, req).trim().replace("&quot;", "\"");
		if(m_sdatlastdisarmed.compareToIgnoreCase("") == 0){
			m_sdatlastdisarmed = EMPTY_DATETIME_STRING;
		}
		m_llastarmedbyid = clsManageRequestParameters.get_Request_Parameter(
				SMTablessalarmsequences.llastarmedbyid, req).trim().replace("&quot;", "\"");
		m_slastarmedbyfullname  = clsManageRequestParameters.get_Request_Parameter(
				SMTablessalarmsequences.slastarmedbyfullname, req).trim().replace("&quot;", "\"");
		m_llastdisarmedbyid = clsManageRequestParameters.get_Request_Parameter(
				SMTablessalarmsequences.llastdisarmedbyid, req).trim().replace("&quot;", "\"");
		m_slastdisarmedbyfullname  = clsManageRequestParameters.get_Request_Parameter(
				SMTablessalarmsequences.slastdisarmedbyfullname, req).trim().replace("&quot;", "\"");
	}
	public void load (ServletContext context, String sDBID, String sUserID, String sUserFullName) throws Exception{
		Connection conn = clsDatabaseFunctions.getConnection(
				context, 
				sDBID, 
				"MySQL", 
				this.toString() + " - user: " 
				+ sUserID
				+ " - "
				+ sUserFullName
				);

		if (conn == null){
			throw new Exception("Error [1459440499] opening data connection to load " + ParamObjectName + ".");
		}

		try {
			load (conn);
		} catch (Exception e) {
			clsDatabaseFunctions.freeConnection(context, conn, "[1547067623]");
			throw new Exception(e.getMessage());
		}
		clsDatabaseFunctions.freeConnection(context, conn, "[1547067624]");
	}
	public void load (Connection conn) throws Exception{
		load (m_slid, conn);
	}
	private void load (String sID, Connection conn) throws Exception{

		sID = sID.trim();
		if (sID.compareToIgnoreCase("") == 0){
			throw new Exception("ID code cannot be blank when loading " + ParamObjectName + ".");
		}

		String SQL = "SELECT *"
				+ ", UNIX_TIMESTAMP(" + SMTablessalarmsequences.datlastarmed + ") AS LASTARMEDUNIXFORMAT"
				+ ", UNIX_TIMESTAMP(" + SMTablessalarmsequences.datlastdisarmed + ") AS LASTDISARMEDUNIXFORMAT"
				+ " FROM " + SMTablessalarmsequences.TableName 
				+ " WHERE ("
					+ SMTablessalarmsequences.lid + " = " + sID
				+ ")";
		if (bDebugMode){
			System.out.println("In " + this.toString() + " - load SQL = " + SQL);
		}
		try {
			ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
			if (rs.next()) {
				//Load the variables here:
				m_slid = Long.toString(rs.getLong(SMTablessalarmsequences.lid));
				m_sname = rs.getString(SMTablessalarmsequences.sname);
				m_sdescription = rs.getString(SMTablessalarmsequences.sdescription).trim();
				m_salarmstate = Long.toString(rs.getLong(SMTablessalarmsequences.ialarmstate));
				m_snotificationemails = rs.getString(SMTablessalarmsequences.semailnotifications).trim();
				m_salarmsetcountdown = Long.toString(rs.getLong(SMTablessalarmsequences.lalarmsetdelaycountdown));
				m_sdatlastarmed = clsDateAndTimeConversions.resultsetDateTimeStringToString(rs.getString(SMTablessalarmsequences.datlastarmed));
				m_sdatlastdisarmed = clsDateAndTimeConversions.resultsetDateTimeStringToString(rs.getString(SMTablessalarmsequences.datlastdisarmed));
				m_ldatlastarmedunixtimestamp = rs.getLong("LASTARMEDUNIXFORMAT");
				m_ldatlastdisarmedunixtimestamp = rs.getLong("LASTDISARMEDUNIXFORMAT");
				m_llastarmedbyid = Long.toString(rs.getLong(SMTablessalarmsequences.llastarmedbyid));
				m_slastarmedbyfullname = rs.getString(SMTablessalarmsequences.slastarmedbyfullname);
				m_llastdisarmedbyid =  Long.toString(rs.getLong(SMTablessalarmsequences.llastdisarmedbyid));
				m_slastdisarmedbyfullname = rs.getString(SMTablessalarmsequences.slastdisarmedbyfullname);
				rs.close();
			} else {
				rs.close();
				throw new Exception(ParamObjectName + " does not exist.");
			}
		} catch (Exception e) {
			throw new Exception("Error [1459440500] reading " + ParamObjectName + " for lid : '" + sID
					+ "' - " + e.getMessage());
		}
		
		//Load the trigger devices:
		try {
			loadTriggerDevices(conn);
		} catch (Exception e) {
			throw new Exception(e.getMessage());
		}
		
		//Load the activation devices:
		try {
			loadActivationDevices(conn);
		} catch (Exception e) {
			throw new Exception(e.getMessage());
		}
		
		return;
	}
	public void loadTriggerDevices(Connection conn) throws Exception{
		//Load the trigger devices:
		arrTriggerDevices.clear();
		String SQL = "SELECT * FROM " + SMTablessalarmtriggerdevices.TableName
			+ " WHERE ("
				+ "(" + SMTablessalarmtriggerdevices.lalarmsequenceid + " = " + getslid() + ")"
			+ ") ORDER BY " + SMTablessalarmtriggerdevices.ldeviceid
		;
		try {
			ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
			while (rs.next()){
				String sDeviceID = Long.toString(rs.getLong(SMTablessalarmtriggerdevices.ldeviceid));
				SSDevice device = new SSDevice();
				device.setslid(sDeviceID);
				try {
					device.load(conn);
				} catch (Exception e) {
					throw new Exception("Error [1460504946] - could not load device with ID '" + sDeviceID + " - " + e.getMessage());
				}
				arrTriggerDevices.add(device);
			}
			rs.close();
		} catch (Exception e) {
			throw new Exception("Error [1460504947] - could not load device - " + e.getMessage());
		}
	}
	public void loadActivationDevices(Connection conn) throws Exception{
		//Load the trigger devices:
		arrActivationDevices.clear();
		arrActivationDeviceDurationsInMS.clear();
		String SQL = "SELECT * FROM " + SMTablessalarmactivationdevices.TableName
			+ " WHERE ("
				+ "(" + SMTablessalarmactivationdevices.lalarmsequenceid + " = " + getslid() + ")"
			+ ") ORDER BY " + SMTablessalarmactivationdevices.ldeviceid
		;
		try {
			ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
			while (rs.next()){
				String sDeviceID = Long.toString(rs.getLong(SMTablessalarmactivationdevices.ldeviceid));
				SSDevice device = new SSDevice();
				device.setslid(sDeviceID);
				try {
					device.load(conn);
				} catch (Exception e) {
					throw new Exception("Error [1460560308] - could not load activation device with ID '" + sDeviceID + " - " + e.getMessage());
				}
				arrActivationDevices.add(device);
				arrActivationDeviceDurationsInMS.add(Long.toString(rs.getLong(SMTablessalarmactivationdevices.lactivationduration)));
			}
			rs.close();
		} catch (Exception e) {
			throw new Exception("Error [1460560309] - could not load activation device - " + e.getMessage());
		}
	}

	public void save_without_data_transaction (ServletContext context, String sDBID, String sUser, String sUserID, String sUserFullName) throws Exception{

		Connection conn = clsDatabaseFunctions.getConnection(
				context, 
				sDBID, 
				"MySQL", 
				this.toString() + " - user: " 
				+ sUserID
				+ " - "
				+ sUserFullName
				);

		if (conn == null){
			throw new Exception("Error [1459440501] opening data connection.");
		}
		
		//Start a data transaction, so we can save to the triggers and alarm devices tables as well:
		try {
			clsDatabaseFunctions.start_data_transaction(conn);
		} catch (Exception e1) {
			clsDatabaseFunctions.freeConnection(context, conn, "[1547067625]");
			throw new Exception("Error [1460477856] starting data transaction - " + e1.getMessage());
		}

		try {
			save_without_data_transaction (conn, sUser);
		} catch (Exception e) {
			clsDatabaseFunctions.rollback_data_transaction(conn);
			clsDatabaseFunctions.freeConnection(context, conn, "[1547067626]");
			throw new Exception(e.getMessage());
		}
		clsDatabaseFunctions.commit_data_transaction(conn);
		clsDatabaseFunctions.freeConnection(context, conn, "[1547067627]");

	}
	public void save_without_data_transaction (Connection conn, String sUser) throws Exception{

		try {
			validate_entry_fields(conn);
		} catch (Exception e1) {
			throw new Exception (e1.getMessage());
		}
		String SQL = "";
		
		//If it's a new record, do an insert:
		SQL = "INSERT INTO " + SMTablessalarmsequences.TableName + " ("
				+ SMTablessalarmsequences.datlastarmed
				+ ", " + SMTablessalarmsequences.datlastdisarmed
				+ ", " + SMTablessalarmsequences.ialarmstate
				+ ", " + SMTablessalarmsequences.lalarmsetdelaycountdown
				+ ", " + SMTablessalarmsequences.sdescription
				+ ", " + SMTablessalarmsequences.semailnotifications
				+ ", " + SMTablessalarmsequences.sname
				+ ") VALUES ("
				+ "'0000-00-00 00:00:00'"
				+ ", '0000-00-00 00:00:00'"
				+ ", " + getsalarmstate()
				+ ", " + getsalarmsetcountdown()
				+ ", '" + clsDatabaseFunctions.FormatSQLStatement(getsdescription().trim()) + "'"
				+ ", '" + clsDatabaseFunctions.FormatSQLStatement(getsnotificationemails().trim()) + "'"
				+ ", '" + clsDatabaseFunctions.FormatSQLStatement(getsname().trim()) + "'"
				+ ")"
				//If the record has the same zone name, then it's a duplicate, because
				//those are both part of a unique key:
				+ " ON DUPLICATE KEY UPDATE"
				//Don't update the alarmed state or time last armed here - we'll do those in a separate function:
				//+ " " + SMTablessalarmsequences.datlastarmed + " = '" + getsdattimelastarmed()  + "'"
				+ " " + SMTablessalarmsequences.lalarmsetdelaycountdown + " = " + getsalarmsetcountdown()
				//+ ", " + SMTablessalarmsequences.ialarmstate + " = " + getsalarmstate()
				+ ", " + SMTablessalarmsequences.sdescription + " = '" + clsDatabaseFunctions.FormatSQLStatement(getsdescription().trim()) + "'"
				+ ", " + SMTablessalarmsequences.semailnotifications + " = '" + clsDatabaseFunctions.FormatSQLStatement(getsnotificationemails().trim()) + "'"
				;

		if (bDebugMode){
			System.out.println("In " + this.toString() + " - save SQL = " + SQL);
		}
		try{
			Statement stmt = conn.createStatement();
			stmt.executeUpdate(SQL);
		}catch (Exception ex) {
			throw new Exception ("Error [1459440502] in insert/update with SQL: " + SQL + " - " + ex.getMessage());
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
			saveTriggerDevices(conn);
		} catch (Exception e) {
			throw new Exception(e.getMessage());
		}
		
		//Now save the activation devices:
		try {
			saveActivationDevices(conn);
		} catch (Exception e) {
			throw new Exception(e.getMessage());
		}
	}

	public void delete (ServletContext context, String sDBID, String sUserID, String sUserFullName) throws Exception{

		Connection conn = clsDatabaseFunctions.getConnection(
				context, 
				sDBID, 
				"MySQL", 
				this.toString() + " - user: " 
				+ sUserID
				+ " - "
				+ sUserFullName
				);

		if (conn == null){
			throw new Exception ("Error [1459440503] opening data connection.");
		}

		try {
			delete (conn);
		} catch (Exception e) {
			clsDatabaseFunctions.freeConnection(context, conn, "[1547067621]");
			throw new Exception(e.getMessage());
		}
		clsDatabaseFunctions.freeConnection(context, conn, "[1547067622]");
	}
	public void delete (Connection conn) throws Exception{

		//Validate deletions
		String SQL = "";

		if (
			(getsalarmstate().compareToIgnoreCase(Integer.toString(SMTablessalarmsequences.ALARM_STATE_ARMED)) == 0)
			|| (getsalarmstate().compareToIgnoreCase(Integer.toString(SMTablessalarmsequences.ALARM_STATE_TRIGGERED)) == 0)
		){
			throw new Exception("You cannot delete an alarm sequence while it is " + SMTablessalarmsequences.getAlarmStateLabel(Integer.parseInt(getsalarmstate())) + ".");
		}

		//Delete:
		if (!clsDatabaseFunctions.start_data_transaction(conn)){
			throw new Exception("Error [1459440504] - Could not start transaction when deleting " + ParamObjectName + ".");
		}

		/*
		//Remove zone from all the device records:
		SQL = "UPDATE " + SMTablessdevices.TableName
				+ " SET " + SMTablessdevices.lzoneid + " = " + Integer.toString(SSDevice.NO_ZONE_SELECTED_VALUE)
				+ " WHERE ("
				+ SMTablessdevices.lzoneid + " = " + getslid()
				+ ")"
				;
		try{
			Statement stmt = conn.createStatement();
			stmt.executeUpdate(SQL);
		}catch (Exception ex) {
			SMUtilities.rollback_data_transaction(conn);
			throw new Exception("Error [1459440505] - Could not update " + SSDevice.ParamObjectName + " records" 
					+ " with SQL: " + SQL + " - " + ex.getMessage());
		}
*/
		//Remove this zone from all the zone/user records:
		/* TODO
		SQL = "DELETE FROM " + SMTablessdeviceusers.TableName
				+ " WHERE ("
				+ SMTablessdeviceusers.ldeviceid + " = " + getslid()
				+ ")"
				;
		try{
			Statement stmt = conn.createStatement();
			stmt.executeUpdate(SQL);
		}catch (Exception ex) {
			SMUtilities.rollback_data_transaction(conn);
			throw new Exception("Error [1459440506] - Could not remove " + ParamObjectName + getszonename() 
					+ " from the authorized zone user records with SQL: " + SQL + " - " + ex.getMessage());
		}
		*/
		SQL = "DELETE FROM " + SMTablessalarmsequences.TableName
				+ " WHERE ("
				+ SMTablessalarmsequences.lid + " = " + getslid()
				+ ")"
				;
		try{
			Statement stmt = conn.createStatement();
			stmt.executeUpdate(SQL);
		}catch (Exception ex) {
			clsDatabaseFunctions.rollback_data_transaction(conn);
			throw new Exception("Error [1459440507] - Could not delete " + ParamObjectName + getsname() 
					+ " with SQL: " + SQL + " - " + ex.getMessage());
		}

		if (!clsDatabaseFunctions.commit_data_transaction(conn)){
			clsDatabaseFunctions.rollback_data_transaction(conn);
			throw new Exception("Error [1459440508] - Could not commit data transaction while deleting " + ParamObjectName + ".");
		}
		//Empty the values:
		initRecordVariables();
	}

	public void validate_entry_fields (Connection conn) throws Exception{
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

		m_sdescription = m_sdescription.trim();
		if (m_sdescription.length() > SMTablessalarmsequences.sdescriptionlength){
			sErrors += "Description cannot be more than " + Integer.toString(SMTablessalarmsequences.sdescriptionlength) + " characters.  ";
		}
		if (m_sdescription.compareToIgnoreCase("") == 0){
			sErrors += "Description cannot be blank.  ";
		}
		
		m_salarmstate = m_salarmstate.trim();
		if (
			(m_salarmstate.compareToIgnoreCase(Integer.toString(SMTablessalarmsequences.ALARM_STATE_ARMED)) != 0) 
			&& (m_salarmstate.compareToIgnoreCase(Integer.toString(SMTablessalarmsequences.ALARM_STATE_UNARMED)) != 0)
			&& (m_salarmstate.compareToIgnoreCase(Integer.toString(SMTablessalarmsequences.ALARM_STATE_TRIGGERED)) != 0)
		){
			sErrors += "Invalid value for 'alarmed state'.  ";
		}

		m_sname = m_sname.trim();
		if (m_sname.length() > SMTablessalarmsequences.snamelength){
			sErrors += "Name cannot be more than " + Integer.toString(SMTablessalarmsequences.snamelength) + " characters.  ";
		}
		if (m_sname.compareToIgnoreCase("") == 0){
			sErrors += "zone name cannot be blank.  ";
		}

		m_snotificationemails = m_snotificationemails.trim();

		m_salarmsetcountdown = m_salarmsetcountdown.trim();
		
		if (sErrors.compareToIgnoreCase("") != 0){
			throw new Exception(sErrors);
		}
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

	public String getsnotificationemails() {
		return m_snotificationemails;
	}

	public void setsnotificationemails(String snotificationemails) {
		m_snotificationemails = snotificationemails;
	}

	public String getsalarmstate() {
		return m_salarmstate;
	}

	public void setsalarmstate(String salarmstate) {
		m_salarmstate = salarmstate;
	}
	
	public String getsalarmsetcountdown() {
		return m_salarmsetcountdown;
	}

	public void setsalarmsetcountdown(String salarmsetcountdown) {
		m_salarmsetcountdown = salarmsetcountdown;
	}
	
	public String getsdattimelastarmed() {
		return m_sdatlastarmed;
	}
	public void setsdattimelastarmed(String sdatlastarmed) {
		m_sdatlastarmed = sdatlastarmed;
	}
	public long getdattimelastarmedasunixtimestamp(){
		return m_ldatlastarmedunixtimestamp;
	}
	public String getsdattimelastdisarmed() {
		return m_sdatlastdisarmed;
	}
	public void setsdattimelastdisarmed(String sdatlastdisarmed) {
		m_sdatlastdisarmed = sdatlastdisarmed;
	}
	public long getdattimelastdisarmedasunixtimestamp(){
		return m_ldatlastdisarmedunixtimestamp;
	}
	public String getllastarmedbyid(){
		return m_llastarmedbyid;
	}
	public void setlalarmedbyid (String sLastArmedByID){
		m_llastarmedbyid = sLastArmedByID;
	}
	public String getslastdisarmedby(){
		return m_llastdisarmedbyid;
	}
	public void setsdisalarmedby (String sLastDisArmedByID){
		m_llastdisarmedbyid = sLastDisArmedByID;
	}
	public String getslastarmedbyfullname(){
		return m_slastarmedbyfullname;
	}
	public void setsalarmedbyfullname (String sLastArmedByFullName){
		m_slastarmedbyfullname = sLastArmedByFullName;
	}
	public String getslastdisarmedbyfullname(){
		return m_slastdisarmedbyfullname;
	}
	public void setsdisalarmedbyfullname (String sLastDisArmedByFullName){
		m_slastdisarmedbyfullname = sLastDisArmedByFullName;
	}
	public ArrayList<SSDevice> getTriggerDeviceList(){
		return arrTriggerDevices;
	}
	public ArrayList<SSDevice> getActivationDeviceList(){
		return arrActivationDevices;
	}
	public ArrayList<String> getActivationDeviceDurationInMS(){
		return arrActivationDeviceDurationsInMS;
	}
	
	public String getObjectName(){
		return ParamObjectName;
	}

	public static void addTriggerDevice(String sDeviceID, String sDBID, String sUser, String sUserID, String sUserFullName, ServletContext context) throws Exception{
		SSDevice device = new SSDevice();
		device.setslid(sDeviceID);
		try {
			device.load(context, sDBID, sUser, sUserID, sUserFullName);
		} catch (Exception e) {
			throw new Exception("Error loading trigger device with ID '" + sDeviceID + "' - " + e.getMessage());
		}
		arrTriggerDevices.add(device);
	}
	public static void addActivationDevice(
			String sDeviceID, 
			String sActivationDurationInSeconds,
			String sDBID, 
			String sUser, 
			String sUserID,
			String sUserFullName,
			ServletContext context) throws Exception{
		SSDevice device = new SSDevice();
		device.setslid(sDeviceID);
		try {
			device.load(context, sDBID, sUser, sUserID, sUserFullName);
		} catch (Exception e) {
			throw new Exception("Error loading trigger device with ID '" + sDeviceID + "' - " + e.getMessage());
		}
		//Validate the integer:
		sActivationDurationInSeconds = sActivationDurationInSeconds.replace(",", "");
		long lActivationDurationInSeconds = 0;
		try {
			lActivationDurationInSeconds = Long.parseLong(sActivationDurationInSeconds) * 1000L;
		} catch (NumberFormatException e1) {
			throw new Exception("Invalid activation duration: '" + sActivationDurationInSeconds + "'.");
		}

		arrActivationDevices.add(device);
		arrActivationDeviceDurationsInMS.add(Long.toString(lActivationDurationInSeconds));
	}

	public void removeTriggerDevice(String sDeviceID){
		for (int i = 0; i < arrTriggerDevices.size(); i++){
			if (arrTriggerDevices.get(i).getslid().compareToIgnoreCase(sDeviceID) == 0){
				arrTriggerDevices.remove(i);
				break;
			}
		}
	}
	
	public void removeActivationDevice(String sDeviceID){
		for (int i = 0; i < arrActivationDevices.size(); i++){
			if (arrActivationDevices.get(i).getslid().compareToIgnoreCase(sDeviceID) == 0){
				arrActivationDevices.remove(i);
				arrActivationDeviceDurationsInMS.remove(i);
				break;
			}
		}
	}

	private void saveTriggerDevices(Connection conn) throws Exception{
		//First remove all the current trigger devices:
		String SQL = "DELETE FROM " + SMTablessalarmtriggerdevices.TableName
			+ " WHERE ("
				+ "(" + SMTablessalarmtriggerdevices.lalarmsequenceid + " = " + getslid() + ")"
			+ ")"
		;
		try {
			Statement stmt = conn.createStatement();
			stmt.execute(SQL);
		} catch (Exception e) {
			throw new Exception("Error [1460477455] removing current trigger devices - " + e.getMessage());
		}
		
		//Now insert all the current trigger devices:
		for (int i = 0; i < arrTriggerDevices.size(); i++){
			SQL = "INSERT INTO " + SMTablessalarmtriggerdevices.TableName + "("
				+ SMTablessalarmtriggerdevices.lalarmsequenceid
				+ ", " + SMTablessalarmtriggerdevices.ldeviceid
				+ ") VALUES ("
				+ getslid()
				+ ", " + arrTriggerDevices.get(i).getslid()
				+ ")"
			;
			try {
				Statement stmt = conn.createStatement();
				stmt.execute(SQL);
			} catch (Exception e) {
				throw new Exception ("Error [1460477855] adding trigger device with ID '" + arrTriggerDevices.get(i).getslid() + " - " + e.getMessage());
			}
		}
	}

	private void saveActivationDevices(Connection conn) throws Exception{
		//First remove all the current trigger devices:
		String SQL = "DELETE FROM " + SMTablessalarmactivationdevices.TableName
			+ " WHERE ("
				+ "(" + SMTablessalarmactivationdevices.lalarmsequenceid + " = " + getslid() + ")"
			+ ")"
		;
		try {
			Statement stmt = conn.createStatement();
			stmt.execute(SQL);
		} catch (Exception e) {
			throw new Exception("Error [1460566452] removing current activation devices - " + e.getMessage());
		}
		
		//Now insert all the current activation devices:
		for (int i = 0; i < arrActivationDevices.size(); i++){
			SQL = "INSERT INTO " + SMTablessalarmactivationdevices.TableName + "("
				+ SMTablessalarmactivationdevices.lalarmsequenceid
				+ ", " + SMTablessalarmactivationdevices.ldeviceid
				+ ", " + SMTablessalarmactivationdevices.lactivationduration
				+ ") VALUES ("
				+ getslid()
				+ ", " + arrActivationDevices.get(i).getslid()
				+ ", " + arrActivationDeviceDurationsInMS.get(i)
				+ ")"
			;
			try {
				Statement stmt = conn.createStatement();
				stmt.execute(SQL);
			} catch (Exception e) {
				throw new Exception ("Error [1460560795] adding activation device with ID '" + arrActivationDevices.get(i).getslid() + " - " + e.getMessage());
			}
		}
	}
	
	private void initRecordVariables(){
		m_slid = "-1";
		m_sname = "";
		m_sdescription = "";
		m_salarmstate = "0";
		m_snotificationemails = "";
		m_salarmsetcountdown = "0";
		m_sdatlastarmed = "0";
		m_sdatlastdisarmed = "0";
		m_llastarmedbyid = "0";
		m_slastarmedbyfullname = "";
		m_llastdisarmedbyid = "0";
		m_slastdisarmedbyfullname = "";
		arrTriggerDevices = new ArrayList<SSDevice>(0);
		arrActivationDevices = new ArrayList<SSDevice>(0);
		arrActivationDeviceDurationsInMS = new ArrayList<String>(0);
	}

	public void setArmedState(
			Connection conn, 
			String sUserName,
			String sUserID,
			int iArmingState,
			boolean bOverrideMalfunctioningDevices,
			ServletContext context,
			String sServerID
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
				//If the activation device is active on a schedule do not change the output contact state
				if(!device.getIsActivatedOnSchedule(conn)){
					device.setOutputContactsState(conn, sSetContactState, sActivationDuration, sUserName, context, sServerID);
				}
			}
		}
		
		String sListeningState = "";
		if (iArmingState == SMTablessalarmsequences.ALARM_STATE_ARMED){
			sListeningState = SSConstants.QUERY_KEYVALUE_TERMINAL_LISTENING_STATUS_LISTENING;
			String sCheckingTriggerDeviceResults = checkInputContactsBeforeArming(conn, sUserName, context, sServerID);
			if (sCheckingTriggerDeviceResults.contains(TRIGGER_DEVICE_SET_ERROR)){
				//If some of the trigger devices could not be set, then we need to decide whether to go ahead and arm or not:
				//If the use chose to OVERRIDE the malfunctioning trigger devices, then record that and let them go ahead:
				if (bOverrideMalfunctioningDevices){
					//Log the override and let it to on through:
					ASUserEventLogEntry usereventlog = new ASUserEventLogEntry(conn);
					try {
						usereventlog.writeEntry(
								SMTablessuserevents.ALARM_ZONE_ARMED, 
								sUserID,
								"N/A", 
								"N/A",
								"0",
								"ALARM SEQ: " + getsname(), 
								sCheckingTriggerDeviceResults, 
								"[1480452758]", 
								null,
								getslid(),
								context,
								"[1547226140]"
						);
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
				setPinsToListeningState(conn, sListeningState, sUserName, context, sServerID);
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
				+ "(" + SMTableusers.lid + " = " + sUserID + ")"
			+ ")"
		;
		try {
			ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
			
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
				+ ", " + SMTablessalarmsequences.llastarmedbyid + " = " + sUserID + ""
				+ ", " + SMTablessalarmsequences.slastarmedbyfullname + " = '" + clsDatabaseFunctions.FormatSQLStatement(sUserFullName) + "'"
				+ ", " + SMTablessalarmsequences.llastdisarmedbyid + " = 0"
				+ ", " + SMTablessalarmsequences.slastdisarmedbyfullname + " = ''"
				
			;
		}else if (iArmingState == SMTablessalarmsequences.ALARM_STATE_UNARMED){
			SQL += ", " + SMTablessalarmsequences.datlastarmed + " = '0000-00-00 00:00:00'"
				+ ", " + SMTablessalarmsequences.datlastdisarmed + " = NOW()" 
				+ ", " + SMTablessalarmsequences.llastarmedbyid + " = 0"
				+ ", " + SMTablessalarmsequences.slastarmedbyfullname + " = ''"
				+ ", " + SMTablessalarmsequences.llastdisarmedbyid + " = " + sUserID + ""
				+ ", " + SMTablessalarmsequences.slastdisarmedbyfullname + " = '" + clsDatabaseFunctions.FormatSQLStatement(sUserFullName) + "'"
				
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
	private String checkInputContactsBeforeArming(Connection conn, String sUser, ServletContext context, String sServerID) throws Exception{
		//Get the trigger device ID and the controller ID of each input terminal on active devices for each controller that's linked to this alarm sequence:
		String SQL = "SELECT"
			+ " " + SMTablessalarmtriggerdevices.TableName + "." + SMTablessalarmtriggerdevices.ldeviceid
			+ ", " + SMTablesscontrollers.TableName + "." + SMTablesscontrollers.lid
			+ " FROM " + SMTablessalarmtriggerdevices.TableName
			+ " LEFT JOIN " + SMTablessdevices.TableName + " ON "
			+ SMTablessdevices.TableName + "." + SMTablessdevices.lid + "=" 
			+ SMTablessalarmtriggerdevices.TableName + "." + SMTablessalarmtriggerdevices.ldeviceid
			+ " LEFT JOIN " + SMTablesscontrollers.TableName + " ON "
			+ SMTablessdevices.TableName + "." + SMTablessdevices.linputcontrollerid + "=" 
			+ SMTablesscontrollers.TableName + "." + SMTablesscontrollers.lid
			+ " WHERE ("
				+ "(" + SMTablessalarmtriggerdevices.TableName + "." + SMTablessalarmtriggerdevices.lalarmsequenceid + " = " + getslid() + ")"
				+ " AND (" + SMTablessdevices.TableName + "." + SMTablessdevices.iactive + " = 1)"
			+ ") ORDER BY " + SMTablesscontrollers.TableName + "." + SMTablesscontrollers.lid
			+ ", " + SMTablessalarmtriggerdevices.TableName + "." + SMTablessalarmtriggerdevices.ldeviceid
			;
		
		//Open the recordset:
		ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
		long lControllerID = 0L;
		//We'll collect all the device IDs in this list:
		ArrayList<String>arrControllerDeviceIDs = new ArrayList<String>(0);
		String sInputContactsStatusResults = "";
		try {
			while (rs.next()){
				//If we are NOT on the first record:
				if (lControllerID != 0L){
					//And IF we are reading a new controller:
					if (lControllerID != rs.getLong(SMTablesscontrollers.TableName + "." + SMTablesscontrollers.lid)){
						//Read the contact states for the previous controller:
						SSController controller = new SSController();
						controller.setslid(Long.toString(lControllerID));
						controller.load(conn);
						try {
							controller.readInputTerminalsContactNormalStatus(
								conn, 
								arrControllerDeviceIDs, 
								sUser,
								context,
								sServerID);
						} catch (Exception e) {
							sInputContactsStatusResults += e.getMessage();
						}
						arrControllerDeviceIDs.clear();
					}
				}
				//Re-set the variables that 'remember' the controller values:
				lControllerID = rs.getLong(SMTablesscontrollers.TableName + "." + SMTablesscontrollers.lid);
				//Add the trigger device to the array list:
				arrControllerDeviceIDs.add(Long.toString(rs.getLong(SMTablessalarmtriggerdevices.TableName + "." + SMTablessalarmtriggerdevices.ldeviceid)));
			}
			rs.close();
		} catch (Exception e) {
			throw new Exception("Error [1462462413] - could not set listening status on all terminals - " + e.getMessage());
		}
		
		//If there are input devices left to be checked after the loop finished, take care of that now:
		if (arrControllerDeviceIDs.size() > 0){
			//Read the contact states to the previous controller:
			SSController controller = new SSController();
			controller.setslid(Long.toString(lControllerID));
			controller.load(conn);
			try {
				controller.readInputTerminalsContactNormalStatus(conn, arrControllerDeviceIDs, sUser, context, sServerID);
			} catch (Exception e) {
				sInputContactsStatusResults += e.getMessage();
			}
		}
		//If there are any results, that means something isn't set, and return that message to the user:
		if (sInputContactsStatusResults.compareToIgnoreCase("") != 0){
			return TRIGGER_DEVICE_SET_ERROR + sInputContactsStatusResults;
		}else{
			return "";
		}
	}
	private void setPinsToListeningState(
			Connection conn,
			String sListeningState,
			String sUser,
			ServletContext context,
			String sServerID
			) throws Exception{
		
		//Send the 'listening' status to each controller that's linked to this alarm sequence:
		String SQL = "SELECT"
			+ " " + SMTablessalarmtriggerdevices.TableName + "." + SMTablessalarmtriggerdevices.ldeviceid
			+ ", " + SMTablesscontrollers.TableName + "." + SMTablesscontrollers.lid
			+ " FROM " + SMTablessalarmtriggerdevices.TableName
			+ " LEFT JOIN " + SMTablessdevices.TableName + " ON "
			+ SMTablessdevices.TableName + "." + SMTablessdevices.lid + "=" 
			+ SMTablessalarmtriggerdevices.TableName + "." + SMTablessalarmtriggerdevices.ldeviceid
			+ " LEFT JOIN " + SMTablesscontrollers.TableName + " ON "
			+ SMTablessdevices.TableName + "." + SMTablessdevices.linputcontrollerid + "=" 
			+ SMTablesscontrollers.TableName + "." + SMTablesscontrollers.lid
			+ " WHERE ("
				+ "(" + SMTablessalarmtriggerdevices.TableName + "." + SMTablessalarmtriggerdevices.lalarmsequenceid + " = " + getslid() + ")"
				+ " AND (" + SMTablessdevices.TableName + "." + SMTablessdevices.iactive + " = 1)"
			+ ") ORDER BY " + SMTablesscontrollers.TableName + "." + SMTablesscontrollers.lid
			+ ", " + SMTablessalarmtriggerdevices.TableName + "." + SMTablessalarmtriggerdevices.ldeviceid
			;
		
		//Open the recordset:
		ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
		long lControllerID = 0L;
		ArrayList<String>arrControllerDeviceIDs = new ArrayList<String>(0);
		try {
			while (rs.next()){
				//If we are NOT on the first record:
				if (lControllerID != 0L){
					//And IF we are reading a new controller:
					if (lControllerID != rs.getLong(SMTablesscontrollers.TableName + "." + SMTablesscontrollers.lid)){
						//Send the command to set the listening states to the previous controller:
						SSController controller = new SSController();
						controller.setslid(Long.toString(lControllerID));
						controller.load(conn);
						controller.setListeningStateOfInputTerminals(conn, arrControllerDeviceIDs, sListeningState, sUser, context, sServerID);
						arrControllerDeviceIDs.clear();
					}
				}
				//Re-set the variables that 'remember' the controller values:
				lControllerID = rs.getLong(SMTablesscontrollers.TableName + "." + SMTablesscontrollers.lid);
				arrControllerDeviceIDs.add(Long.toString(rs.getLong(SMTablessalarmtriggerdevices.TableName + "." + SMTablessalarmtriggerdevices.ldeviceid)));
			}
			rs.close();
		} catch (Exception e) {
			throw new Exception("Error [1462462413] - could not set listening status on all terminals - " + e.getMessage());
		}
		
		//If there are input devices left to be set after the loop finished, take care of that now:
		if (arrControllerDeviceIDs.size() > 0){
			SSController controller = new SSController();
			controller.setslid(Long.toString(lControllerID));
			controller.load(conn);
			controller.setListeningStateOfInputTerminals(conn, arrControllerDeviceIDs, sListeningState, sUser, context, sServerID);
		}
	}
	public void initiateAlarmSequence(Connection conn, 
			String sUserName,
			String sUserID,
			String sDeviceDescription, 
			boolean bOverrideMalfunctioningDevice, 
			ServletContext context,
			String sServerID) throws Exception{
		
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
			ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
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
					sUserID, 
					SMLogEntry.LOG_OPERATION_ASEMAILSENDERROR, 
					"Alarm " + this.getsname() + " was triggered; error sending email", 
					"Error: " + e.getMessage(), 
					"[1473957806]"
				);
			}
			//Set the state to 'triggered':
			setArmedState(
					conn, 
					sUserName,
					sUserID,
					SMTablessalarmsequences.ALARM_STATE_TRIGGERED, 
					bOverrideMalfunctioningDevice, 
					context,
					sServerID);
		}
		
		//Activate the alarm devices every time there's a trigger:
		activateAlarmDevices(conn, sUserName, context, sServerID);
		
	}

	private void activateAlarmDevices(Connection conn, String sUser, ServletContext context, String sServerID) throws Exception{
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
				//But if it's 'momentary contact', then we need to get the momentary contact duration for that device
				}else{
					int iDuration = -1;
					try {
						iDuration = Integer.parseInt(device.getscontactduration());
					}catch (Exception e) {
						throw new Exception("Error [1763506949] - Error validating contact duration: " + e.getMessage());
					}			
					if(iDuration <= 0){
						sActivationDuration = SSConstants.DEFAULT_MOMENTARY_CONTACT_ACTIVATE_INTERVAL_IN_MS;			
					}else{
						sActivationDuration = device.getscontactduration();
					}
				}
				device.setOutputContactsState(conn, sSetContactState, sActivationDuration, sUser, context, sServerID);
			}
		}
	}
	private void emailTriggerNotifications(Connection conn, String sDeviceDescription) throws Exception{

		ArrayList<String>arrNotifyEmails = new ArrayList<String>(0);
		String sNotificationAddresses[] = this.getsnotificationemails().split(",");
		for (int i = 0; i < sNotificationAddresses.length; i++){
			arrNotifyEmails.add(sNotificationAddresses[i].trim());
		}

		if (arrNotifyEmails.size() == 0){
			return;
		}
		
		//Now go get the info we need to send an email:
		String sCurrentTime = "";
		String sSMTPServer = "";
		String sSMTPPort = "";
		String sSMTPSourceServerName = "";
		String sUserName = ""; 
		String sPassword = ""; 
		String sReplyToAddress = "";
		boolean bUsesSMTPAuthentication = false;

		String SQL = "";
		
		//Get company name:
		String sCompanyName = "";
		SQL = "SELECT * FROM " + SMTablecompanyprofile.TableName;
		try {
			ResultSet rsCompanyProfile = clsDatabaseFunctions.openResultSet(SQL, conn);
			if (rsCompanyProfile.next()){
				sCompanyName = rsCompanyProfile.getString(SMTablecompanyprofile.sCompanyName);
			}
			rsCompanyProfile.close();
		} catch (Exception e1) {
			throw new Exception("Error [1463406945] getting company name.");
		}
		
		try{
			SQL = "SELECT " + SMTablesmoptions.TableName + ".*"
			+ ", DATE_FORMAT(NOW(),'%c/%e/%Y %h:%i:%s %p')"
				+ " AS CURRENTTIME FROM " 
				+ SMTablesmoptions.TableName;
			ResultSet rsOptions = clsDatabaseFunctions.openResultSet(SQL, conn);
			if(rsOptions.next()){
				sCurrentTime = rsOptions.getString("CURRENTTIME");
				sSMTPServer = rsOptions.getString(SMTablesmoptions.ssmtpserver).trim();
				sSMTPPort = rsOptions.getString(SMTablesmoptions.ssmtpport).trim();
				sSMTPSourceServerName = rsOptions.getString(SMTablesmoptions.ssmtpsourceservername).trim();
				sUserName = rsOptions.getString(SMTablesmoptions.ssmtpusername).trim();
				sPassword = rsOptions.getString(SMTablesmoptions.ssmtppassword).trim(); 
				sReplyToAddress = rsOptions.getString(SMTablesmoptions.ssmtpreplytoname).trim();
				bUsesSMTPAuthentication = (rsOptions.getInt(SMTablesmoptions.iusesauthentication) == 1);
				rsOptions.close();
			}else{
				rsOptions.close();
				throw new Exception("Error [1463000599] getting smoptions record to get email information.");
			}
		}catch(SQLException e){
			throw new Exception("Error [1463000600] getting email information from smoptions with SQL: " + SQL + "  - " + e.getMessage());
		}
		
		String sBody = sCompanyName + " - " + sCurrentTime
			+ " - " + sDeviceDescription
		;

		int iSMTPPort;
		try {
			iSMTPPort = Integer.parseInt(sSMTPPort);
		} catch (NumberFormatException e) {
			throw new Exception("Error parsing email port '" + sSMTPPort + "' [1463000601] - " + e.getMessage());
		}
		try {
			SMUtilities.sendEmail(
					sSMTPServer, 
					sUserName, 
					sPassword, 
					sReplyToAddress,
					Integer.toString(iSMTPPort),
					"ALARM: " + getsname(),
					sBody,
					"SMCP@" + sSMTPSourceServerName,
					sSMTPSourceServerName, 
					arrNotifyEmails, 
					bUsesSMTPAuthentication,
					false
			);
		} catch (Exception e) {
			throw new Exception("Error sending email [1463000602] " + e.getMessage());
		}
	}
}
	
