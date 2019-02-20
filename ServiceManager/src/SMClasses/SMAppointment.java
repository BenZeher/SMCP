package SMClasses;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import smcontrolpanel.SMBidEntry;
import smcontrolpanel.SMGeocoder;
import smcontrolpanel.SMMySQLs;
import SMDataDefinition.SMTableappointments;
import ServletUtilities.clsMasterEntry;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsDateAndTimeConversions;
import ServletUtilities.clsManageRequestParameters;


public class SMAppointment extends clsMasterEntry{
	
	public static final String ParamObjectName = "Appointment";
	
	public static final String Paramlid = "lid";
	public static final String Paramluserid = "luserid";
	public static final String Paramdatentrydate = "datentrydate";
	public static final String Paramiminuteofday = "iminuteofday";
	public static final String Parammcomment = "mcomment";
	public static final String Paramsordernumber = "sordernumber"; 
	public static final String Paramisalescontactid = "isalescontactid";
	public static final String Paramibidid = "ibidid";
	public static final String Paramsaddress1 = "saddress1";
	public static final String Paramsaddress2 = "saddress2";
	public static final String Paramsaddress3 = "saddress3";
	public static final String Paramsaddress4 = "saddress4";
	public static final String Paramscity = "scity";
	public static final String Paramsstate = "sstate";
	public static final String Paramszip = "szip";
	public static final String Paramsgeocode = "sgeocode";
	public static final String Paramscontactname = "scontactname";
	public static final String Paramsshiptoname = "sshiptoname";
	public static final String Paramsbilltoname = "sbilltoname";
	public static final String Paramsphone = "sphone";
	public static final String Paramsemail = "semail";
	public static final String Paramdatcreatedtime = "datcreatedtime";
	public static final String Paramlcreateduserid = "lcreateduserid";
	public static final String Paraminotificationtime = "inotificationtime";
	public static final String Paraminotificationsent = "inotificationsent";
	//Other parameters
	public static final String ParamNewRecord = "NewRecord";
	public static final String ParamNewRecordValue = "1";
	
	private String m_slid;
	private String m_sluserid;
	private String m_datentrydate;
	private String m_iminuteofday;
	private String m_mcomment;
	private String m_sordernumber;
	private String m_isalescontactid;
	private String m_ibidid;
	private String m_saddress1;
	private String m_saddress2;
	private String m_saddress3;
	private String m_saddress4;
	private String m_scity;
	private String m_sstate;
	private String m_szip;
	private String m_sgeocode;
	private String m_scontactname;
	private String m_sshiptoname;
	private String m_sbilltoname;
	private String m_sphone;
	private String m_semail;
	private String m_datcreatedtime;
	private String m_slcreateduserid;
	private String m_inotificationtime;
	private String m_inotificationsent;
	
	private String m_sNewRecord;

	private static boolean bDebugMode = false;
	
    public SMAppointment() {
		super();
		initScheduleEntryVariables();
        }
    
    public SMAppointment(HttpServletRequest req){
		super(req);
		initScheduleEntryVariables(); 
		m_slid = clsManageRequestParameters.get_Request_Parameter(
				SMAppointment.Paramlid, req).trim();
		m_sluserid = clsManageRequestParameters.get_Request_Parameter(
				SMAppointment.Paramluserid, req).trim().replace("&quot;", "\"");
		m_datentrydate  = clsManageRequestParameters.get_Request_Parameter(
				SMAppointment.Paramdatentrydate, req).trim().replace("&quot;", "\"");
		if(m_datentrydate.compareToIgnoreCase("") == 0){
			m_datentrydate = EMPTY_DATE_STRING;
		}
		

		if ((clsManageRequestParameters.get_Request_Parameter(
						SMAppointment.Paramiminuteofday + "SelectedHour", req).compareToIgnoreCase("") != 0)){

			m_iminuteofday = clsManageRequestParameters.get_Request_Parameter(
					SMAppointment.Paramiminuteofday + "SelectedHour", req).trim()
					+ ":" 
					+ clsManageRequestParameters.get_Request_Parameter(
					SMAppointment.Paramiminuteofday + "SelectedMinute", req).trim()
					+ " "
					;

			if (clsManageRequestParameters.get_Request_Parameter(
					SMAppointment.Paramiminuteofday + "SelectedAMPM", req).trim().compareToIgnoreCase("1") == 0){
				m_iminuteofday = m_iminuteofday + "PM";
			}else{
				m_iminuteofday = m_iminuteofday + "AM";
			}
		}else{
			m_iminuteofday ="12:00 AM";
		}
		
		m_mcomment = clsManageRequestParameters.get_Request_Parameter(
				SMAppointment.Parammcomment, req).trim().replace("&quot;", "\"");
		m_sordernumber = clsManageRequestParameters.get_Request_Parameter(
				SMAppointment.Paramsordernumber, req).trim().replace("&quot;", "\"");

		m_isalescontactid = clsManageRequestParameters.get_Request_Parameter(
				SMAppointment.Paramisalescontactid, req).trim().replace("&quot;", "\"");
		if(req.getParameter(SMAppointment.Paramisalescontactid) == null){
			m_isalescontactid = "";
		}
		m_ibidid = clsManageRequestParameters.get_Request_Parameter(
				SMAppointment.Paramibidid, req).trim().replace("&quot;", "\"");
		if(req.getParameter(SMAppointment.Paramibidid) == null){
			m_ibidid = "";
		}
		m_saddress1 = clsManageRequestParameters.get_Request_Parameter(
				SMAppointment.Paramsaddress1, req).trim().replace("&quot;", "\"");
		m_saddress2 = clsManageRequestParameters.get_Request_Parameter(
				SMAppointment.Paramsaddress2, req).trim().replace("&quot;", "\"");
		m_saddress3 = clsManageRequestParameters.get_Request_Parameter(
				SMAppointment.Paramsaddress3, req).trim().replace("&quot;", "\"");
		m_saddress4 = clsManageRequestParameters.get_Request_Parameter(
				SMAppointment.Paramsaddress4, req).trim().replace("&quot;", "\"");
		m_scity = clsManageRequestParameters.get_Request_Parameter(
				SMAppointment.Paramscity, req).trim().replace("&quot;", "\"");
		m_sstate = clsManageRequestParameters.get_Request_Parameter(
				SMAppointment.Paramsstate, req).trim().replace("&quot;", "\"");
		m_szip = clsManageRequestParameters.get_Request_Parameter(
				SMAppointment.Paramszip, req).trim().replace("&quot;", "\"");
		m_sgeocode = clsManageRequestParameters.get_Request_Parameter(
				SMAppointment.Paramsgeocode, req).trim().replace("&quot;", "\"");
		m_scontactname = clsManageRequestParameters.get_Request_Parameter(
				SMAppointment.Paramscontactname, req).trim().replace("&quot;", "\"");
		m_sbilltoname = clsManageRequestParameters.get_Request_Parameter(
				SMAppointment.Paramsbilltoname, req).trim().replace("&quot;", "\"");
		m_sshiptoname = clsManageRequestParameters.get_Request_Parameter(
				SMAppointment.Paramsshiptoname, req).trim().replace("&quot;", "\"");
		m_sphone = clsManageRequestParameters.get_Request_Parameter(
				SMAppointment.Paramsphone, req).trim().replace("&quot;", "\"");
		m_semail = clsManageRequestParameters.get_Request_Parameter(
				SMAppointment.Paramsemail, req).trim().replace("&quot;", "\"");
		m_datcreatedtime = clsManageRequestParameters.get_Request_Parameter(
				SMAppointment.Paramdatcreatedtime, req).trim().replace("&quot;", "\"");
		m_slcreateduserid = clsManageRequestParameters.get_Request_Parameter(
				SMAppointment.Paramlcreateduserid, req).trim().replace("&quot;", "\"");
		
		m_inotificationtime = clsManageRequestParameters.get_Request_Parameter(
				SMAppointment.Paraminotificationtime, req).trim().replace("&quot;", "\"");	
		if(req.getParameter(SMAppointment.Paraminotificationtime) == null){
			m_inotificationtime = "-1";
		}
		
		m_inotificationsent = clsManageRequestParameters.get_Request_Parameter(
				SMAppointment.Paraminotificationsent, req).trim().replace("&quot;", "\"");	
		if(req.getParameter(SMAppointment.Paraminotificationsent) == null){
			m_inotificationsent = "0";
		}

		m_sNewRecord = clsManageRequestParameters.get_Request_Parameter(SMAppointment.ParamNewRecord, req).trim().replace("&quot;", "\"");
    }
    public void load (ServletContext context, String sDBIB, String sUserID, String sUserFullName) throws Exception{
    	Connection conn = clsDatabaseFunctions.getConnection(
    			context, 
    			sDBIB, 
    			"MySQL", 
    			this.toString() + " - user: " + sUserID + " - " + sUserFullName
    			);
    	
    	if (conn == null){
    		throw new Exception("Error opening data connection to load schedule entry.");
    	}
    	
    	try {
			load (conn);
		} catch (Exception e) {
			clsDatabaseFunctions.freeConnection(context, conn, "[1547067668]");
			throw new Exception(e.getMessage());
		}
    	clsDatabaseFunctions.freeConnection(context, conn, "[1547067669]");
    }
    public boolean load (Connection conn) throws Exception{
    	return load (m_slid, conn);
    }
    private boolean load (String sID, Connection conn) throws Exception{

    	sID = sID.trim();
    	if (sID.compareToIgnoreCase("") == 0){
    		throw new Exception("ID code cannot be blank when loading schedule entry.");
    	}
		
		String SQL = "SELECT * FROM " + SMTableappointments.TableName
			+ " WHERE ("
				+ SMTableappointments.lid + " = " + sID
			+ ")";
		if (bDebugMode){
			System.out.println("In " + this.toString() + " - load SQL = " + SQL);
		}
		try {
			ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
			if (rs.next()) {
				//Load the variables here:
				m_slid = Long.toString(rs.getLong(SMTableappointments.lid));
				m_sluserid = rs.getString(SMTableappointments.luserid).trim();
				m_datentrydate = clsDateAndTimeConversions.resultsetDateStringToString(
						rs.getString(SMTableappointments.datentrydate).trim());
				m_iminuteofday = timeIntegerToString(rs.getInt(SMTableappointments.iminuteofday));
				m_mcomment = rs.getString(SMTableappointments.mcomment);
				m_sordernumber = rs.getString(SMTableappointments.sordernumber).trim();
				m_isalescontactid = Long.toString(rs.getLong(SMTableappointments.isalescontactid));
				m_ibidid = Long.toString(rs.getLong(SMTableappointments.ibidid));
				m_saddress1 = rs.getString(SMTableappointments.saddress1).trim();
				m_saddress2 = rs.getString(SMTableappointments.saddress2).trim();
				m_saddress3 = rs.getString(SMTableappointments.saddress3).trim();
				m_saddress4 = rs.getString(SMTableappointments.saddress4).trim();
				m_scity = rs.getString(SMTableappointments.scity).trim();
				m_sstate = rs.getString(SMTableappointments.sstate).trim();
				m_szip = rs.getString(SMTableappointments.szip).trim();
				m_sgeocode = rs.getString(SMTableappointments.sgeocode).trim();
				m_scontactname = rs.getString(SMTableappointments.scontactname).trim();
				m_sbilltoname = rs.getString(SMTableappointments.sbilltoname).trim();
				m_sshiptoname = rs.getString(SMTableappointments.sshiptoname).trim();
				m_sphone = rs.getString(SMTableappointments.sphone).trim();
				m_semail = rs.getString(SMTableappointments.semail).trim();
				m_datcreatedtime = rs.getString(SMTableappointments.datcreatedtime).trim();
				m_slcreateduserid = rs.getString(SMTableappointments.lcreateduserid).trim();
				m_inotificationtime = Long.toString(rs.getLong(SMTableappointments.inotificationtime));
				m_inotificationsent = Long.toString(rs.getLong(SMTableappointments.inotificationsent));
				rs.close();
			} else {
				rs.close();
				return false;
			}
		} catch (Exception e) {
			throw new Exception("Error [1400590535] reading " + ParamObjectName + " for lid : '" + sID
					+ "' - " + e.getMessage());
		}
		return true;
    }
    
    public void save_without_data_transaction (ServletContext context, String sDBIB, String sUserID, String sUserFullName, boolean bRequireGeocode) throws Exception{
    	
       	Connection conn = clsDatabaseFunctions.getConnection(
    			context, 
    			sDBIB, 
    			"MySQL", 
    			this.toString() + " - user: " + sUserID + " - " + sUserFullName
    			);
    	
    	if (conn == null){
    		throw new Exception("Error [1469196637] opening data connection.");
    	}
    	
    	try {
			save_without_data_transaction (conn,sUserID, bRequireGeocode);
		} catch (Exception e) {
			clsDatabaseFunctions.freeConnection(context, conn, "[1547067670]");
			throw new Exception(e.getMessage());
		}
    	clsDatabaseFunctions.freeConnection(context, conn, "[1547067671]");
    	
    }
    public void save_without_data_transaction (Connection conn, String sUserID, boolean bRequireGeocode) throws Exception{

    	try {
			validate_entry_fields(conn);
		} catch (Exception e1) {
			throw new Exception (e1.getMessage());
		}
    	
    	String SQL = "";
    	
    	//update the geocode before saving the appointment to see if the address as changed.
    	if(bRequireGeocode) {
    		try{
    			updateGeoCode(conn, sUserID);
    		}catch (Exception e){
    			super.addStatusMessage("Could not update geocode - " + e.getMessage());
    		}	
    	}

    	
		if(getsNewRecord().compareToIgnoreCase(SMAppointment.ParamNewRecordValue) == 0){
			SQL = "INSERT INTO " + SMTableappointments.TableName + " ("
				+ SMTableappointments.luserid
				+ ", " + SMTableappointments.datentrydate
				+ ", " + SMTableappointments.iminuteofday
					+ ", " + SMTableappointments.mcomment
					+ ", " + SMTableappointments.sordernumber
					+ ", " + SMTableappointments.isalescontactid
					+ ", " + SMTableappointments.ibidid
					+ ", " + SMTableappointments.saddress1
					+ ", " + SMTableappointments.saddress2
					+ ", " + SMTableappointments.saddress3
					+ ", " + SMTableappointments.saddress4
					+ ", " + SMTableappointments.scity
					+ ", " + SMTableappointments.sstate
					+ ", " + SMTableappointments.szip
					+ ", " + SMTableappointments.sgeocode
					+ ", " + SMTableappointments.scontactname
					+ ", " + SMTableappointments.sbilltoname
					+ ", " + SMTableappointments.sshiptoname
					+ ", " + SMTableappointments.sphone
					+ ", " + SMTableappointments.semail
					+ ", " + SMTableappointments.datcreatedtime
					+ ", " + SMTableappointments.lcreateduserid
					+ ", " + SMTableappointments.inotificationtime
					+ ", " + SMTableappointments.inotificationsent
					+ ") VALUES ("
					+ "" + clsDatabaseFunctions.FormatSQLStatement(getluserid().trim()) + ""
					+ ", '" + clsDateAndTimeConversions.stdDateStringToSQLDateString(getdatentrydate()) + "'"
					+ ", " + getiminuteofday()
					+ ", '" + clsDatabaseFunctions.FormatSQLStatement(getmcomment()) + "'"
					+ ", '" + clsDatabaseFunctions.FormatSQLStatement(getsordernumber().trim()) + "'"
					+ ", " + getisalescontactid()
					+ ", " + getibidid()
					+ ", '" + clsDatabaseFunctions.FormatSQLStatement(getsaddress1().trim()) + "'"
					+ ", '" + clsDatabaseFunctions.FormatSQLStatement(getsaddress2().trim()) + "'"
					+ ", '" + clsDatabaseFunctions.FormatSQLStatement(getsaddress3().trim()) + "'"
					+ ", '" + clsDatabaseFunctions.FormatSQLStatement(getsaddress4().trim()) + "'"
					+ ", '" + clsDatabaseFunctions.FormatSQLStatement(getscity().trim()) + "'"
					+ ", '" + clsDatabaseFunctions.FormatSQLStatement(getsstate().trim()) + "'"
					+ ", '" + clsDatabaseFunctions.FormatSQLStatement(getszip().trim()) + "'"
					+ ", '" + clsDatabaseFunctions.FormatSQLStatement(getsgeocode().trim()) + "'"
					+ ", '" + clsDatabaseFunctions.FormatSQLStatement(getscontactname().trim()) + "'"
					+ ", '" + clsDatabaseFunctions.FormatSQLStatement(getsbilltoname().trim()) + "'"
					+ ", '" + clsDatabaseFunctions.FormatSQLStatement(getsshiptoname().trim()) + "'"
					+ ", '" + clsDatabaseFunctions.FormatSQLStatement(getsphone().trim()) + "'"
					+ ", '" + clsDatabaseFunctions.FormatSQLStatement(getsemail().trim()) + "'"
					+ ", NOW()"
					+ ", " + sUserID + ""
					+ ", " + getinotificationtime()
					+ ", " + getinotificationsent() 
					+ ")";
		}else{
			SQL = "UPDATE " +  SMTableappointments.TableName
				+ " SET " + SMTableappointments.luserid + " = " + clsDatabaseFunctions.FormatSQLStatement(getluserid().trim()) + ""
				+ ", " + SMTableappointments.datentrydate + " = '" + clsDateAndTimeConversions.stdDateStringToSQLDateString(getdatentrydate()) + "'"
			+ ", " + SMTableappointments.iminuteofday + " = " + getiminuteofday() + ""
			+ ", " + SMTableappointments.mcomment + " = '" + clsDatabaseFunctions.FormatSQLStatement(getmcomment()) + "'"
			+ ", " + SMTableappointments.sordernumber + " = '" + clsDatabaseFunctions.FormatSQLStatement(getsordernumber().trim()) + "'"
			+ ", " + SMTableappointments.isalescontactid + " = " + getisalescontactid()
			+ ", " + SMTableappointments.ibidid + " = " + getibidid()
			+ ", " + SMTableappointments.saddress1 + " = '" + clsDatabaseFunctions.FormatSQLStatement(getsaddress1().trim()) + "'"
		    + ", " + SMTableappointments.saddress2 + " = '" + clsDatabaseFunctions.FormatSQLStatement(getsaddress2().trim()) + "'"
			+ ", " + SMTableappointments.saddress3 + " = '" + clsDatabaseFunctions.FormatSQLStatement(getsaddress3().trim()) + "'"
			+ ", " + SMTableappointments.saddress4 + " = '" + clsDatabaseFunctions.FormatSQLStatement(getsaddress4().trim()) + "'"
			+ ", " + SMTableappointments.scity + " = '" + clsDatabaseFunctions.FormatSQLStatement(getscity().trim()) + "'"
			+ ", " + SMTableappointments.sstate + " = '" + clsDatabaseFunctions.FormatSQLStatement(getsstate().trim()) + "'"
			+ ", " + SMTableappointments.szip + " = '" + clsDatabaseFunctions.FormatSQLStatement(getszip().trim()) + "'"
			+ ", " + SMTableappointments.sgeocode + " = '" + clsDatabaseFunctions.FormatSQLStatement(getsgeocode().trim()) + "'"
			+ ", " + SMTableappointments.scontactname + " = '" + clsDatabaseFunctions.FormatSQLStatement(getscontactname().trim()) + "'"
			+ ", " + SMTableappointments.sbilltoname + " = '" + clsDatabaseFunctions.FormatSQLStatement(getsbilltoname().trim()) + "'"
			+ ", " + SMTableappointments.sshiptoname + " = '" + clsDatabaseFunctions.FormatSQLStatement(getsshiptoname().trim()) + "'"
			+ ", " + SMTableappointments.sphone + " = '" + clsDatabaseFunctions.FormatSQLStatement(getsphone().trim()) + "'"
			+ ", " + SMTableappointments.semail + " = '" + clsDatabaseFunctions.FormatSQLStatement(getsemail().trim()) + "'"
			+ ", " + SMTableappointments.inotificationtime + " = " + getinotificationtime()
			+ ", " + SMTableappointments.inotificationsent + " = " + getinotificationsent()
			+ " WHERE (" + SMTableappointments.lid + "=" + getslid() + ")"
		;
		}
		if (bDebugMode){
			System.out.println("In " + this.toString() + " - save SQL = " + SQL);
		}
		try{
		    Statement stmt = conn.createStatement();
		    stmt.executeUpdate(SQL);
		}catch (Exception ex) {
			throw new Exception ("Error [1469199627] in insert/update with SQL: " + SQL + " - " + ex.getMessage());
		}
		//Update the ID if it's an insert:
		if (getsNewRecord().compareToIgnoreCase(ParamNewRecordValue) == 0){
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
    }
    
    public void updateNotificationSentFlag(Connection conn, String sValue) throws Exception{

    	
    	String SQL = "";
			SQL = "UPDATE " +  SMTableappointments.TableName
				+ " SET " + SMTableappointments.inotificationsent + " = " + sValue
			+ " WHERE (" + SMTableappointments.lid + "=" + getslid() + ")"
		;
		
		if (bDebugMode){
			System.out.println("In " + this.toString() + " - save SQL = " + SQL);
		}
		try{
		    Statement stmt = conn.createStatement();
		    stmt.executeUpdate(SQL);
		}catch (Exception ex) {
			throw new Exception ("Error [1469199627] in insert/update with SQL: " + SQL + " - " + ex.getMessage());
		}

    }

	public void delete (ServletContext context, String sDBIB, String sUserID, String sUserFullName) throws Exception{
    	
    	Connection conn = clsDatabaseFunctions.getConnection(
    			context, 
    			sDBIB, 
    			"MySQL", 
    			this.toString() + " - user: " + sUserID + " - " + sUserFullName
    			);
    	
    	if (conn == null){
    		throw new Exception ("Error [1495804150] opening data connection.");
    	}
    	
    	try {
			delete (conn);
		} catch (Exception e) {
			clsDatabaseFunctions.freeConnection(context, conn, "[1547067666]");
			throw new Exception(e.getMessage());
		}
    	clsDatabaseFunctions.freeConnection(context, conn, "[1547067667]");
    }
    public void delete (Connection conn) throws Exception{
    	
    	//Validate deletions
    	String SQL = "";
   
    	SQL = "DELETE FROM " + SMTableappointments.TableName
    		+ " WHERE ("
    			+ SMTableappointments.lid + " = " + this.getslid()
    		+ ")"
    		;
		try{
		    Statement stmt = conn.createStatement();
		    stmt.executeUpdate(SQL);
		}catch (Exception ex) {
			throw new Exception("Error [1469199859] - Could not delete schedule entry with ID " + getslid() + " with SQL: " + SQL + " - " + ex.getMessage());
		}
		
		//Empty the values:
		initScheduleEntryVariables();
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
        
    	m_iminuteofday = m_iminuteofday.trim();
    	try{
    		int isequencenumber = timeStringToInteger(m_iminuteofday);
    		setiminuteofday(Integer.toString(isequencenumber));
    	}catch (Exception e){
    		if (m_iminuteofday.compareToIgnoreCase("") == 0){
    			sErrors += "Time can not be blank.  ";
    		}else{
    			sErrors += "Time is not valid.  " + e.getMessage();
    		}
    	}
        m_datentrydate = m_datentrydate.trim();
        if (m_datentrydate.compareToIgnoreCase("") == 0 || m_datentrydate.compareToIgnoreCase(EMPTY_DATE_STRING) == 0){
        	sErrors += "Entry date is not valid. ";
        }
        //validate sales contact
        if(m_isalescontactid.compareToIgnoreCase("") == 0){
        	m_isalescontactid = "0";
        }
        if(m_isalescontactid.compareToIgnoreCase("") != 0 
        	&& m_isalescontactid.compareToIgnoreCase("0") != 0 ){
        	try{
        		int icontactid = Integer.parseInt(m_isalescontactid);

        		String sSQL = SMMySQLs.Get_Sales_Contact_By_ID_SQL(icontactid);
        		ResultSet rs = clsDatabaseFunctions.openResultSet(sSQL, conn);
	    		if(!rs.next()){
	    			sErrors += "Sales contact ID '" + m_isalescontactid + "' does not exist. ";
	    		}
	    		rs.close();
        	}catch (Exception e){
        		sErrors += "Sales contect ID must be a number. ";
        	}
        }
        //Validate sales lead
        if(m_ibidid.compareToIgnoreCase("") == 0){
        	m_ibidid = "0";
        }
        if(m_ibidid.compareToIgnoreCase("") != 0 && m_ibidid.compareToIgnoreCase("0") != 0 ){
        	SMBidEntry bid = new SMBidEntry();
        	bid.setlid(m_ibidid);
        	
        	if(!bid.load(conn)){
        		sErrors += "Sales lead '" + m_ibidid + "' does not exist. ";
        	}
        }
        
        //validate order number
        if(m_sordernumber.compareToIgnoreCase("") != 0){
        	SMOrderHeader order = new SMOrderHeader();
        	order.setM_strimmedordernumber(m_sordernumber);
        	if(!order.load(conn)){
        		sErrors += "Order '" + m_sordernumber + "' does not exist. ";
        	}
        }
        
        
        if((m_sordernumber.trim().compareToIgnoreCase("") != 0) && (m_ibidid.trim().compareToIgnoreCase("0") != 0)){
        	sErrors += "You have tried to link this appointment to more than ONE item. Please select only one: order, saled lead OR contact ID.";  	
        }
        if((m_isalescontactid.trim().compareToIgnoreCase("0") != 0) && (m_ibidid.trim().compareToIgnoreCase("0") != 0)){
           	sErrors += "You have tried to link this appointment to more than ONE item. Please select only one: order, saled lead OR contact ID.";  	  	
        }
        if((m_sordernumber.trim().compareToIgnoreCase("") != 0) && (m_isalescontactid.trim().compareToIgnoreCase("0") != 0)){
           	sErrors += "You have tried to link this appointment to more than ONE item. Please select only one: order, saled lead OR contact ID.";  		
        }
       
        m_sluserid = m_sluserid.trim();
        if(m_sluserid.compareToIgnoreCase("") == 0){
        	sErrors += "You must select a user to schedule this appointment for. ";
        }

        if (sErrors.compareToIgnoreCase("") != 0){
        	throw new Exception(sErrors);
        }
    }
    
    public void updateGeoCode(Connection conn, String sUserID) throws Exception {
    	
		//Get the current map address to update:
		String sMapAddressToUpdate = getsaddress1().trim();
		sMapAddressToUpdate	= sMapAddressToUpdate.trim() + " " + getsaddress2().trim();
		sMapAddressToUpdate	= sMapAddressToUpdate.trim() + " " + getsaddress3().trim();
		sMapAddressToUpdate	= sMapAddressToUpdate.trim() + " " + getsaddress4().trim();
		sMapAddressToUpdate	= sMapAddressToUpdate.trim() + " " + getscity().trim();
		sMapAddressToUpdate	= sMapAddressToUpdate.trim() + " " + getsstate().trim();
		sMapAddressToUpdate	= sMapAddressToUpdate.trim() + " " + getszip().trim();

		String sLatLng = SMGeocoder.EMPTY_GEOCODE;
		//We are NOT going to stop the save if we code a geocode error:
		
		int iAttemptNo = 0;
		try {
			do {
				sLatLng = SMGeocoder.codeAddress(sMapAddressToUpdate, conn, iAttemptNo);
				iAttemptNo++;
			}while(sLatLng.compareToIgnoreCase(SMGeocoder.OVER_QUERY_LIMIT_ERROR) == 0);
			
			//Log geocode request
			SMClasses.SMLogEntry log = new SMClasses.SMLogEntry(conn);
			log.writeEntry(
				sUserID, 
				SMLogEntry.LOG_OPERATION_SMGEOCODEREQUEST, 
					"Appointment ID: " + getslid() +  "\n"
					+ "Requested Address: " + sMapAddressToUpdate + ""
					+ "Returned Lat/Lng: " + sLatLng + "\n"
					,
					"SMappointment.updateGeoCode",
					"[1512765699]");
			
		} catch (Exception e) {
			setsgeocode(SMGeocoder.EMPTY_GEOCODE);	
			throw new Exception(e.getMessage());
		} 
		;	
		
		if(iAttemptNo > 1) {
			//TODO check smoptions for api key
			super.addStatusMessage("Mutiple geocode requests had to be sent. A geocode API Key is recomended ");
		}
		
		setsgeocode(sLatLng);							
    }

    public static int timeStringToInteger(String sEnteredTime) throws Exception{
    	int iMinutesAfterMidnight = 0000;
   
    	if(sEnteredTime.contains("PM")){
    		sEnteredTime = sEnteredTime.replace("PM", "");
    		sEnteredTime = sEnteredTime.replace(":", "");
    		sEnteredTime = sEnteredTime.trim();

    		try{
    			iMinutesAfterMidnight = Integer.parseInt(sEnteredTime);
    			if(sEnteredTime.substring(0, 2).compareToIgnoreCase("12") != 0){
        			iMinutesAfterMidnight += 1200;
    			}

    		}catch (Exception e){
    			throw new Exception(e.getMessage());
    		}
    		
    	}else{
    		sEnteredTime = sEnteredTime.replace("AM", "");
    		if(sEnteredTime.substring(0, sEnteredTime.lastIndexOf(":")).compareToIgnoreCase("12") == 0){
    			sEnteredTime = sEnteredTime.replaceFirst("12", "00");
    		}
    		sEnteredTime = sEnteredTime.replace(":", "");
    		sEnteredTime = sEnteredTime.trim();

    		try{
    			iMinutesAfterMidnight = Integer.parseInt(sEnteredTime);
    		}catch (Exception e){
    			throw new Exception(e.getMessage());
    		}
    		
    	}
    	//Convert to minutes after midnight
    	iMinutesAfterMidnight = (iMinutesAfterMidnight/100) * 60 + (iMinutesAfterMidnight%100);

    	if(bDebugMode){
    		System.out.println("timeStringToInteger() "
    				+ "Minutes after midnight calculation: " + Integer.toString(iMinutesAfterMidnight));
    	}
    	return iMinutesAfterMidnight;
    }
    
    public static String timeIntegerToString(int iSavedTime) {
	
    		Calendar cal = Calendar.getInstance();
    		cal.set(Calendar.HOUR_OF_DAY, iSavedTime/60);
    		cal.set(Calendar.MINUTE, iSavedTime % 60);
    		cal.set(Calendar.SECOND, 0);
    		cal.set(Calendar.MILLISECOND, 0);
    		SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
    		String sHourMinute = sdf.format(cal.getTime());
    		
    		String sHour = sHourMinute.substring(0, sHourMinute.indexOf(":"));
    		int iHour = Integer.parseInt(sHour);
    		
    		String sMinute = sHourMinute.substring(sHourMinute.indexOf(":") + 1);
    		
    		String sMeridiem = "AM";
    		
    		if(iHour >= 12){
    			if(iHour != 12){
    				iHour = iHour-12;
    			}
    			sHour = Integer.toString(iHour);
    			sMeridiem = "PM";
    		}else{
    			if(iHour == 0)
    			iHour = 12;
    			sHour = Integer.toString(iHour);
    		}
    		
    		String sTime = sHour + ":" + sMinute + " " + sMeridiem;
    	
    	if(bDebugMode){
    		System.out.println("timeIntegerToString() "
    						+ "Hour: " + sHour 
    						+ " Minute: " + sMinute
    						+ " Meridiem: " + sMeridiem);
    	} 	
    	
    	return sTime;
    }

	public String getslid() {
		return m_slid;
	}

	public void setslid(String slid) {
		m_slid = slid;
	}

	public String getluserid() {
		return m_sluserid;
	}

	public void setluserid(String sluserid) {
		m_sluserid = sluserid;
	}

	public String getdatentrydate() {
		return m_datentrydate;
	}

	public void setdatentrydate(String datentrydate) {
		m_datentrydate = datentrydate;
	}

	public String getiminuteofday() {
		return m_iminuteofday;
	}

	public void setiminuteofday(String iminuteofday) {
		m_iminuteofday = iminuteofday;
	}

	public String getmcomment() {
		return m_mcomment;
	}

	public void setmcomment(String mcomment) {
		m_mcomment = mcomment;
	}

	public String getsordernumber() {
		return m_sordernumber;
	}

	public void setsordernumber(String sordernumber) {
		m_sordernumber = sordernumber;
	}

	public String getisalescontactid() {
		return m_isalescontactid;
	}

	public void setisalescontactid(String isalescontactid) {
		m_isalescontactid = isalescontactid;
	}

	public String getibidid() {
		return m_ibidid;
	}

	public void setibidid(String ibidid) {
		m_ibidid = ibidid;
	}

	public String getsaddress1() {
		return m_saddress1;
	}

	public void setsaddress1(String saddress1) {
		m_saddress1 = saddress1;
	}
	
	public String getsaddress2() {
		return m_saddress2;
	}

	public void setsaddress2(String saddress2) {
		m_saddress2 = saddress2;
	}
	
	public String getsaddress3() {
		return m_saddress3;
	}

	public void setsaddress3(String saddress3) {
		m_saddress3 = saddress3;
	}
	
	public String getsaddress4() {
		return m_saddress4;
	}

	public void setsaddress4(String saddress4) {
		m_saddress4 = saddress4;
	}

	public String getscity() {
		return m_scity;
	}

	public void setscity(String scity) {
		m_scity = scity;
	}

	public String getsstate() {
		return m_sstate;
	}

	public void setsstate(String sstate) {
		m_sstate = sstate;
	}
	
	public String getszip() {
		return m_szip;
	}

	public void setszip(String szip) {
		m_szip = szip;
	}
	
	public String getsgeocode() {
		return m_sgeocode;
	}

	public void setsgeocode(String sgeocode) {
		m_sgeocode = sgeocode;
	}
	
	public String getscontactname() {
		return m_scontactname;
	}

	public void setscontactname(String scontactname) {
		m_scontactname = scontactname;
	}
	
	public String getsshiptoname() {
		return m_sshiptoname;
	}

	public void setsshiptoname(String sshiptoname) {
		m_sshiptoname = sshiptoname;
	}
	
	public String getsbilltoname() {
		return m_sbilltoname;
	}

	public void setsbilltoname(String sbilltoname) {
		m_sbilltoname = sbilltoname;
	}
	
	public String getsphone() {
		return m_sphone;
	}

	public void setsphone(String sphone) {
		m_sphone = sphone;
	}
	
	public String getsemail() {
		return m_semail;
	}

	public void setsemail(String semail) {
		m_semail = semail;
	}
	
	public String getdatcreatedtime() {
		return m_datcreatedtime;
	}

	public void setdatcreatedtime(String datcreatedtime) {
		m_datcreatedtime = datcreatedtime;
	}
	
	public String getlcreateduserid() {
		return m_slcreateduserid;
	}

	public void setlcreateduserid(String slcreateduserid) {
		m_slcreateduserid = slcreateduserid;
	}
	
	public String getinotificationtime() {
		return m_inotificationtime;
	}
	
	public void setinotificationtime(String inotificationtime) {
		m_inotificationtime = inotificationtime;
	}
	
	public String getinotificationsent() {
		return m_inotificationsent;
	}
	
	public void setinotificationsent(String inotificationsent) {
		m_inotificationsent = inotificationsent;
	}
	
	public String getsNewRecord() {
		return m_sNewRecord;
	}

	public void setsNewRecord(String mSNewRecord) {
		m_sNewRecord = mSNewRecord;
	}
	
	public boolean bIsNewRecord(){
		if(getsNewRecord().compareToIgnoreCase(SMAppointment.ParamNewRecordValue) == 0){
			return true;
		}else{
			return false;
		}
		
	}
	public String getObjectName(){
		return ParamObjectName;
	}
	
    private void initScheduleEntryVariables(){
    	m_slid = "-1";
    	m_sluserid = "";
    	m_datentrydate = EMPTY_DATE_STRING;
    	m_iminuteofday = "12:00 AM";
    	m_mcomment = "";
    	m_sordernumber = "";
    	m_isalescontactid = "";
    	m_ibidid = "";
    	m_saddress1 = "";
       	m_saddress2 = "";
       	m_saddress3 = "";
       	m_saddress4 = "";
    	m_scity = "";
    	m_sstate = "";
    	m_szip = "";
    	m_sgeocode = SMGeocoder.EMPTY_GEOCODE;
    	m_scontactname = "";
    	m_sbilltoname = "";
    	m_sshiptoname = "";
    	m_sphone = "";
    	m_semail = "";
    	m_inotificationtime = "-1";
    	m_inotificationsent = "0";
    	m_sNewRecord = "1";
	}


}
