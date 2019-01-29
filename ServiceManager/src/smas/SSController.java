package smas;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import sscommon.SSConstants;
import sscommon.SSUtilities;
import SMDataDefinition.SMTablesscontrollers;
import SMDataDefinition.SMTablessdevices;
import SMDataDefinition.SMTableusers;
import ServletUtilities.clsMasterEntry;
import smcontrolpanel.SMUtilities;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsDateAndTimeConversions;
import ServletUtilities.clsManageRequestParameters;

public class SSController extends clsMasterEntry{
	
	public static final String ParamObjectName = "Security system controller";
	
	private String m_slid;
	private String m_scontrollername;
	private String m_sdescription;
	private String m_spasscode;
	private String m_sdattimelastmaintained;
	private String m_llastmaintainedbyid;
	private String m_slastmaintainedbyfullname;
	private String m_scontrollerurl;
	private String m_slisteningport;
	private String m_sactive;
	
	//Config file values:
	private String m_sconfigfilepasscode = "";
	private String m_sconfigfilecontrollername = "";
	private String m_sconfigfilelisteningport = "";
	private String m_sconfigfilewebbappurl = "";
	private String m_sconfigfilewebappport = "";
	private String m_sconfigfiledatabaseid = "";
	private String m_sconfigfilelogginglevel = "";
	private String m_sconfigfilelogfilename = "";
	private String m_scontrollersoftwareversion = "";
	private String m_scontrollerhostname = "";
	
	private boolean bDebugMode = false;
	
    public SSController() {
		super();
		initControllerVariables();
        }
    
    SSController(HttpServletRequest req){
		super(req);
		initControllerVariables();
		m_slid = clsManageRequestParameters.get_Request_Parameter(
				SMTablesscontrollers.lid, req).trim();
		m_scontrollername = clsManageRequestParameters.get_Request_Parameter(
				SMTablesscontrollers.scontrollername, req).trim();
		m_sdescription  = clsManageRequestParameters.get_Request_Parameter(
				SMTablesscontrollers.sdescription, req).trim().replace("&quot;", "\"");
		m_spasscode = clsManageRequestParameters.get_Request_Parameter(
				SMTablesscontrollers.spasscode, req).trim().replace("&quot;", "\"");
		m_sdattimelastmaintained = clsManageRequestParameters.get_Request_Parameter(
				SMTablesscontrollers.dattimelastmaintained, req).trim().replace("&quot;", "\"");
		if(m_sdattimelastmaintained.compareToIgnoreCase("") == 0){
			m_sdattimelastmaintained = EMPTY_DATETIME_STRING;
		}
		m_llastmaintainedbyid = clsManageRequestParameters.get_Request_Parameter(
				SMTablesscontrollers.llastmaintainedbyid, req).trim().replace("&quot;", "\"");
		if(m_llastmaintainedbyid.compareToIgnoreCase("") == 0){
			m_llastmaintainedbyid = "0";
		}
		m_slastmaintainedbyfullname = clsManageRequestParameters.get_Request_Parameter(
				SMTablesscontrollers.slastmaintainedbyfullname, req).trim().replace("&quot;", "\"");
		m_scontrollerurl = clsManageRequestParameters.get_Request_Parameter(
				SMTablesscontrollers.scontrollerurl, req).trim().replace("&quot;", "\"");
		m_slisteningport = clsManageRequestParameters.get_Request_Parameter(
				SMTablesscontrollers.slisteningport, req).trim().replace("&quot;", "\"");
		if (clsManageRequestParameters.get_Request_Parameter(
				SMTablesscontrollers.iactive, req).trim().replace("&quot;", "\"").compareToIgnoreCase("") != 0){
			m_sactive = "1";
		}else{
			m_sactive = "0";
		}
		
		//Load the controller config file values, in case we are configuring the controller:
		m_sconfigfilepasscode = clsManageRequestParameters.get_Request_Parameter(
				SSConstants.CONFFILE_KEY_PASSCODE, req).trim().replace("&quot;", "\"");
		m_sconfigfilecontrollername = clsManageRequestParameters.get_Request_Parameter(
				SSConstants.CONFFILE_KEY_NAME, req).trim().replace("&quot;", "\"");
		m_sconfigfilelisteningport = clsManageRequestParameters.get_Request_Parameter(
				SSConstants.CONFFILE_LISTENING_PORT, req).trim().replace("&quot;", "\"");
		m_sconfigfilewebbappurl = clsManageRequestParameters.get_Request_Parameter(
				SSConstants.CONFFILE_WEB_APP_URL, req).trim().replace("&quot;", "\"");
		m_sconfigfilewebappport = clsManageRequestParameters.get_Request_Parameter(
				SSConstants.CONFFILE_WEB_APP_PORT, req).trim().replace("&quot;", "\"");
		m_sconfigfiledatabaseid = clsManageRequestParameters.get_Request_Parameter(
				SSConstants.CONFFILE_DATABASE_ID, req).trim().replace("&quot;", "\"");
		m_sconfigfilelogginglevel = clsManageRequestParameters.get_Request_Parameter(
				SSConstants.CONFFILE_LOGGING_LEVEL, req).trim().replace("&quot;", "\"");
		m_sconfigfilelogfilename = clsManageRequestParameters.get_Request_Parameter(
				SSConstants.CONFFILE_LOG_FILE, req).trim().replace("&quot;", "\"");
		m_scontrollersoftwareversion = clsManageRequestParameters.get_Request_Parameter(
				SSConstants.QUERY_KEY_VERSION, req).trim().replace("&quot;", "\"");
		m_scontrollerhostname = clsManageRequestParameters.get_Request_Parameter(
				SSConstants.QUERY_KEY_CONTROLLER_HOSTNAME, req).trim().replace("&quot;", "\"");
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
    		throw new Exception("Error [1458864727] opening data connection to load " + ParamObjectName + ".");
    	}
    	
    	try {
			load (conn);
		} catch (Exception e) {
			clsDatabaseFunctions.freeConnection(context, conn, "[1547067632]");
			throw new Exception(e.getMessage());
		}
    	clsDatabaseFunctions.freeConnection(context, conn, "[1547067633]");
    }
    public void load (Connection conn) throws Exception{
    	load (m_slid, conn);
    }
    private void load (String sID, Connection conn) throws Exception{

    	sID = sID.trim();
    	if (sID.compareToIgnoreCase("") == 0){
    		throw new Exception("ID code cannot be blank when loading " + ParamObjectName + ".");
    	}
		
		String SQL = "SELECT * FROM " + SMTablesscontrollers.TableName
			+ " WHERE ("
				+ SMTablesscontrollers.lid + " = " + sID
			+ ")";
		if (bDebugMode){
			System.out.println("In " + this.toString() + " - load SQL = " + SQL);
		}
		try {
			ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
			if (rs.next()) {
				//Load the variables here:
				m_slid = Long.toString(rs.getLong(SMTablesscontrollers.lid));
				m_scontrollername = rs.getString(SMTablesscontrollers.scontrollername).trim();
				m_sdescription = rs.getString(SMTablesscontrollers.sdescription).trim();
				m_spasscode = rs.getString(SMTablesscontrollers.spasscode).trim();
				m_sdattimelastmaintained = clsDateAndTimeConversions.resultsetDateTimeStringToString(
					rs.getString(SMTablesscontrollers.dattimelastmaintained));
				m_llastmaintainedbyid = Long.toString(rs.getLong(SMTablesscontrollers.llastmaintainedbyid));
				m_slastmaintainedbyfullname = rs.getString(SMTablesscontrollers.slastmaintainedbyfullname).trim();
				m_scontrollerurl = rs.getString(SMTablesscontrollers.scontrollerurl).trim();
				m_slisteningport = rs.getString(SMTablesscontrollers.slisteningport).trim();
				m_sactive = Integer.toString(rs.getInt(SMTablesscontrollers.iactive));
				rs.close();
			} else {
				rs.close();
				throw new Exception(ParamObjectName + " with ID '" + sID + "' was not found.");
			}
		} catch (Exception e) {
			throw new Exception("Error [1458864728] reading " + ParamObjectName + " for lid : '" + sID
					+ "' - " + e.getMessage());
		}
		return;
    }
    
    public void save_without_data_transaction (ServletContext context, String sConf, String sUser, String sUserID, String sUserFullName) throws Exception{
    	
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
    		throw new Exception("Error [1458864729] opening data connection.");
    	}
    	
    	try {
			save_without_data_transaction (conn, sUser, sUserID);
		} catch (Exception e) {
			clsDatabaseFunctions.freeConnection(context, conn, "[1547067634]");
			throw new Exception(e.getMessage());
		}
    	clsDatabaseFunctions.freeConnection(context, conn, "[1547067635]");
    	
    }
    public void save_without_data_transaction (Connection conn, String sUser, String sUserID) throws Exception{

    	try {
			validate_entry_fields(conn);
		} catch (Exception e1) {
			throw new Exception (e1.getMessage());
		}
    	String SQL = "";
    	String sLastMaintainedByFullName = "N/A";
    	sLastMaintainedByFullName = SMUtilities.getFullNamebyUserID(sUserID, conn);
    	setllastmaintainedbyid(sUserID);
    	setslastmaintainedbyfullname(sLastMaintainedByFullName);
		//If it's a new record, do an insert:
		SQL = "INSERT INTO " + SMTablesscontrollers.TableName + " ("
			+ SMTablesscontrollers.dattimelastmaintained
			+ ", " + SMTablesscontrollers.iactive
			+ ", " + SMTablesscontrollers.scontrollerurl
			+ ", " + SMTablesscontrollers.sdescription
			+ ", " + SMTablesscontrollers.llastmaintainedbyid
			+ ", " + SMTablesscontrollers.slastmaintainedbyfullname
			+ ", " + SMTablesscontrollers.slisteningport
			+ ", " + SMTablesscontrollers.spasscode
			+ ", " + SMTablesscontrollers.scontrollername
			+ ") VALUES ("
			+ " NOW()"
			+ ", " + getsactive()
			+ ", '" + clsDatabaseFunctions.FormatSQLStatement(getscontrollerurl().trim()) + "'"
			+ ", '" + clsDatabaseFunctions.FormatSQLStatement(getsdescription().trim()) + "'"
			+ ", " + clsDatabaseFunctions.FormatSQLStatement(getllastmaintainedbyid().trim()) + ""
			+ ", '" + clsDatabaseFunctions.FormatSQLStatement(getslastmaintainedbyfullname().trim()) + "'"
			+ ", '" + clsDatabaseFunctions.FormatSQLStatement(getslisteningport().trim()) + "'"
			+ ", '" + clsDatabaseFunctions.FormatSQLStatement(getspasscode().trim()) + "'"
			+ ", '" + clsDatabaseFunctions.FormatSQLStatement(getscontrollername().trim()) + "'"
			+ ")"
			+ " ON DUPLICATE KEY UPDATE"
			+ " " + SMTablesscontrollers.dattimelastmaintained + " = NOW()"
			+ ", " + SMTablesscontrollers.iactive + " = " + getsactive()
			+ ", " + SMTablesscontrollers.scontrollerurl + " = '" + clsDatabaseFunctions.FormatSQLStatement(getscontrollerurl().trim()) + "'"
			+ ", " + SMTablesscontrollers.sdescription  + " = '" + clsDatabaseFunctions.FormatSQLStatement(getsdescription().trim()) + "'"
			+ ", " + SMTablesscontrollers.llastmaintainedbyid + " = " + clsDatabaseFunctions.FormatSQLStatement(getllastmaintainedbyid().trim()) + ""
			+ ", " + SMTablesscontrollers.slastmaintainedbyfullname + " = '" + clsDatabaseFunctions.FormatSQLStatement(getslastmaintainedbyfullname().trim()) + "'"
			+ ", " + SMTablesscontrollers.slisteningport + " = '" + clsDatabaseFunctions.FormatSQLStatement(getslisteningport().trim()) + "'"
			+ ", " + SMTablesscontrollers.spasscode + " = '" + clsDatabaseFunctions.FormatSQLStatement(getspasscode().trim()) + "'"
			+ ", " + SMTablesscontrollers.scontrollername + " = '" + clsDatabaseFunctions.FormatSQLStatement(getscontrollername().trim()) + "'"
		;

		if (bDebugMode){
			System.out.println("In " + this.toString() + " - save SQL = " + SQL);
		}
		try{
		    Statement stmt = conn.createStatement();
		    stmt.executeUpdate(SQL);
		}catch (Exception ex) {
			throw new Exception ("Error [1458868611] in insert/update with SQL: " + SQL + " - " + ex.getMessage());
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
    }

    public void delete (ServletContext context, String sConf, String sUserID, String sUserFullName) throws Exception{
    	
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
    		throw new Exception ("Error [1458868612] opening data connection.");
    	}
    	
    	try {
			delete (conn);
		} catch (Exception e) {
			clsDatabaseFunctions.freeConnection(context, conn, "[1547067630]");
			throw new Exception(e.getMessage());
		}
    	clsDatabaseFunctions.freeConnection(context, conn, "[1547067631]");
    }
    public void delete (Connection conn) throws Exception{
    	
    	//Validate deletions
    	String SQL = "";
    	//If there are any devices using it, we can't delete it:
    	SQL = "SELECT "
    		+ " " + SMTablessdevices.lid
    		+ " FROM " + SMTablessdevices.TableName
    		+ " WHERE ("
    			+ "(" + SMTablessdevices.linputcontrollerid + " = " + getslid() + ")"
    		+ ")"
    		;
    	try {
			ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
			if (rs.next()) {
				rs.close();
				throw new Exception("Cannot delete this controller because there are some devices set up using it.");
			}else{
				rs.close();
			}
		} catch (SQLException e) {
			throw new Exception("Error [1458868613] reading devices to delete controller - " + e.getMessage());
		}
    	//Delete:
    	if (!clsDatabaseFunctions.start_data_transaction(conn)){
    		throw new Exception("Error [1458868614] - Could not start transaction when deleting " + ParamObjectName + ".");
    	}
    	SQL = "DELETE FROM " + SMTablesscontrollers.TableName
    		+ " WHERE ("
    			+ SMTablesscontrollers.lid + " = " + getslid()
    		+ ")"
    		;
		try{
		    Statement stmt = conn.createStatement();
		    stmt.executeUpdate(SQL);
		}catch (Exception ex) {
			clsDatabaseFunctions.rollback_data_transaction(conn);
			throw new Exception("Error [1458868615] - Could not delete controller " + getscontrollername() + " with SQL: " + SQL + " - " + ex.getMessage());
		}
		
		if (!clsDatabaseFunctions.commit_data_transaction(conn)){
			clsDatabaseFunctions.rollback_data_transaction(conn);
			throw new Exception("Error [1458868616] - Could not commit data transaction while deleting " + ParamObjectName + ".");
		}
		//Empty the values:
		initControllerVariables();
    }
    
    public void deleteControllerLog (ServletContext context, String sConf, String sUser, String sUserID, String sUserFullName,  String sServerID) throws Exception{
    	
    	load(context, sConf, sUserID, sUserFullName);
    	
    	String sRequestString = SSUtilities.buildRequestFromServerToDeleteControllerLogFile(
    		getscontrollername(),
    		getspasscode(),
    		sUser, 
    		SMUtilities.getUserFullName(sUser, context, sConf, "SSController.deleteControllerLog"),
    		sServerID
    	);
    	
    	ASClientService client = new ASClientService();
    	try {
			client.sendRequest(
				getscontrollerurl(), 
				Integer.parseInt(getslisteningport()), 
				sRequestString, 
				false);
		} catch (Exception e1) {
			throw new Exception("Error [1463759269] sending command to delete controller log - " + e1.getMessage());
		}
    			
    	return;
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
        if (m_sdescription.length() > SMTablesscontrollers.sdescriptionlength){
        	sErrors += "Description cannot be more than " + Integer.toString(SMTablesscontrollers.sdescriptionlength) + " characters.  ";
        }
        if (m_sdescription.compareToIgnoreCase("") == 0){
        	sErrors += "Description cannot be blank.  ";
        }
        m_spasscode = m_spasscode.trim();
        if (m_spasscode.length() > SMTablesscontrollers.spasscodelength){
        	sErrors += "Passcode cannot be more than " + Integer.toString(SMTablesscontrollers.spasscodelength) + " characters.  ";
        }
        if (m_spasscode.compareToIgnoreCase("") == 0){
        	sErrors += "Pass code cannot be blank.  ";
        }
        m_sdattimelastmaintained = m_sdattimelastmaintained.trim();
        if (m_sdattimelastmaintained.compareToIgnoreCase("") == 0){
        	m_sdattimelastmaintained = EMPTY_DATETIME_STRING;
        }
        if (m_sdattimelastmaintained.compareToIgnoreCase(EMPTY_DATETIME_STRING) != 0){
        	if (!clsDateAndTimeConversions.IsValidDateString("M/d/yyyy hh:ss a", m_sdattimelastmaintained)){
        		sErrors += "Date last maintained is invalid: '" + m_sdattimelastmaintained + "'.";
        	}
        }

        m_llastmaintainedbyid = m_llastmaintainedbyid.trim();
        if (m_llastmaintainedbyid.length() > SMTablesscontrollers.llastmaintainedbyidlength){
        	sErrors += "Last maintained by ID cannot be more than " + Integer.toString(SMTablesscontrollers.llastmaintainedbyidlength) + " characters.  ";
        }
        m_slastmaintainedbyfullname = m_slastmaintainedbyfullname.trim();
        if (m_slastmaintainedbyfullname.length() > SMTablesscontrollers.slastmaintainedbyfullnamelength){
        	sErrors += "Last maintained by full name cannot be more than " + Integer.toString(SMTablesscontrollers.slastmaintainedbyfullnamelength) + " characters.  ";
        }
        m_scontrollername = m_scontrollername.trim();
        if (m_scontrollername.compareToIgnoreCase("") == 0){
        	sErrors += "Controller ID cannot be blank.  ";
        }
        if (m_scontrollername.length() > SMTablesscontrollers.scontrollernamelength){
        	sErrors += "Controller ID cannot be more than " + Integer.toString(SMTablesscontrollers.scontrollernamelength) + " characters.  ";
        }
        m_scontrollerurl = m_scontrollerurl.trim();
        if (m_scontrollerurl.length() > SMTablesscontrollers.scontrollerurllength){
        	sErrors += "Controller URL cannot be more than " + Integer.toString(SMTablesscontrollers.scontrollerurllength) + " characters.  ";
        }
        if (m_scontrollerurl.compareToIgnoreCase("") == 0){
        	sErrors += "Controller URL cannot be blank.  ";
        }
        m_slisteningport = m_slisteningport.trim();
        if (m_slisteningport.length() > SMTablesscontrollers.slisteningportlength){
        	sErrors += "Listening port cannot be more than " + Integer.toString(SMTablesscontrollers.scontrollerurllength) + " characters.  ";
        }
        if (m_slisteningport.compareToIgnoreCase("") == 0){
        	sErrors += "Listening port cannot be blank.  ";
        }
        //Check if port is a valid integer:
        int iPort = 0;
        try {
			iPort = Integer.parseInt(m_slisteningport);
		} catch (Exception e) {
			sErrors += "Listening port is not a valid integer value.  ";
		}
        
        if ((iPort < 1024) || (iPort >= 65535)){
        	sErrors += "Listening port must be higher than 1024 and less than 65535.  ";
        }
        
        if (sErrors.compareToIgnoreCase("") != 0){
        	throw new Exception(sErrors);
        }
    }
    public void validate_configuration_fields (Connection conn) throws Exception{
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
    	
    	m_sconfigfilepasscode = m_sconfigfilepasscode.trim();
        if (m_sconfigfilepasscode.length() > SSConstants.CONFFILE_KEY_PASSCODE_MAX_LENGTH){
        	sErrors += "Controller passcode cannot be more than " + Integer.toString(SSConstants.CONFFILE_KEY_PASSCODE_MAX_LENGTH) + " characters.  ";
        }
        if (m_sconfigfilepasscode.compareToIgnoreCase("") == 0){
        	sErrors += "Pass code cannot be blank.  ";
        }

        m_sconfigfilecontrollername = m_sconfigfilecontrollername.trim();
        if (m_sconfigfilecontrollername.compareToIgnoreCase("") == 0){
        	sErrors += "Controller name cannot be blank.  ";
        }
        if (m_sconfigfilecontrollername.length() > SSConstants.CONFFILE_KEY_NAME_MAX_LENGTH){
        	sErrors += "Controller name cannot be more than " + Integer.toString(SSConstants.CONFFILE_KEY_NAME_MAX_LENGTH) + " characters.  ";
        }
        //Check to make sure we don't already have a controller with this name:
        String SQL = "SELECT * FROM " + SMTablesscontrollers.TableName
        	+ " WHERE ("
        		+ "(" + SMTablesscontrollers.scontrollername + " = '" + m_sconfigfilecontrollername + "')"
        		+ " AND (" + SMTablesscontrollers.lid + " != " + getslid() + ")"
        	+ ")"
        ;
        try {
			ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
			if (rs.next()){
				sErrors += "Controller name '" + m_sconfigfilecontrollername + "' is already in use on another controller.  ";
			}
			rs.close();
		} catch (Exception e1) {
			throw new Exception("Error [1463775326] - checking for duplicate controller names - " + e1.getMessage());
		}
        
        m_sconfigfilelisteningport = m_sconfigfilelisteningport.trim();
        if (m_sconfigfilelisteningport.length() > SSConstants.CONFFILE_LISTENING_PORT_MAX_LENGTH){
        	sErrors += "Listening port cannot be more than " + Integer.toString(SSConstants.CONFFILE_LISTENING_PORT_MAX_LENGTH) + " characters.  ";
        }
        if (m_sconfigfilelisteningport.compareToIgnoreCase("") == 0){
        	sErrors += "Listening port cannot be blank.  ";
        }
        //Check if port is a valid integer:
        int iPort = 0;
        try {
			iPort = Integer.parseInt(m_sconfigfilelisteningport);
			if (
				(iPort <= 0)
				|| (iPort >= 65535)
			){
				sErrors += "Controller's listening port must be between 0 and 65535.  ";
			}
		} catch (Exception e) {
			sErrors += "Controller's listening port is not a valid integer value.  ";
		}
        
        m_sconfigfilewebbappurl = m_sconfigfilewebbappurl.trim();
        if (m_sconfigfilewebbappurl.length() > SSConstants.CONFFILE_WEB_APP_URL_MAX_LENGTH){
        	sErrors += "Web app URL cannot be more than " + Integer.toString(SSConstants.CONFFILE_WEB_APP_URL_MAX_LENGTH) + " characters.  ";
        }
        if (m_sconfigfilewebbappurl.compareToIgnoreCase("") == 0){
        	sErrors += "Web app URL cannot be blank.  ";
        }

        if (m_sconfigfilewebappport.compareToIgnoreCase("") == 0){
        	sErrors += "Web app port cannot be blank.  ";
        }
        //Check if port is a valid integer:
        iPort = 0;
        try {
			iPort = Integer.parseInt(m_sconfigfilewebappport);
			if (
				(iPort <= 0)
				|| (iPort >= 65535)
			){
				sErrors += "Web app port must be between 0 and 65535.  ";
			}
		} catch (Exception e) {
			sErrors += "Web app port is not a valid integer value.  ";
		}
        
        m_sconfigfiledatabaseid = m_sconfigfiledatabaseid.trim();
        if (m_sconfigfiledatabaseid.compareToIgnoreCase("") == 0){
        	sErrors += "Database ID cannot be blank.  ";
        }
        if (m_sconfigfiledatabaseid.length() > SSConstants.CONFFILE_DATABASE_ID_MAX_LENGTH){
        	sErrors += "Database ID cannot be more than " + Integer.toString(SSConstants.CONFFILE_DATABASE_ID_MAX_LENGTH) + " characters.  ";
        }
        
        if (m_sconfigfilelogginglevel.compareToIgnoreCase("") == 0){
        	sErrors += "Logging level cannot be blank.  ";
        }
        //Check if logging level is a valid integer:
        if (
        		(m_sconfigfilelogginglevel.compareTo("0") != 0)
        		&& (m_sconfigfilelogginglevel.compareTo("1") != 0)
        		&& (m_sconfigfilelogginglevel.compareTo("2") != 0)
        		&& (m_sconfigfilelogginglevel.compareTo("3") != 0)
        		){
        	sErrors += "Logging level is not a valid integer value.  ";
		}
        
        m_sconfigfilelogfilename = m_sconfigfilelogfilename.trim();
        if (m_sconfigfilelogfilename.compareToIgnoreCase("") == 0){
        	sErrors += "Log file name cannot be blank.  ";
        }
        if (m_sconfigfilelogfilename.length() > SSConstants.CONFFILE_LOG_FILE_MAX_LENGTH){
        	sErrors += "Log file name cannot be more than " + Integer.toString(SSConstants.CONFFILE_LOG_FILE_MAX_LENGTH) + " characters.  ";
        }
        
        if (sErrors.compareToIgnoreCase("") != 0){
        	throw new Exception(sErrors);
        }
    }
    public void configure_without_data_transaction (ServletContext context, String sConf, String sUser, String sUserID, String sUserFullName, String sServerID) throws Exception{
    	
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
    		throw new Exception("Error [1463687145] opening data connection.");
    	}
    	
    	try {
			configure_without_data_transaction (conn, sUser, sUserID, context, sServerID);
		} catch (Exception e) {
			clsDatabaseFunctions.freeConnection(context, conn, "[1547067628]");
			throw new Exception(e.getMessage());
		}
    	clsDatabaseFunctions.freeConnection(context, conn, "[1547067629]");
    	
    }
    
    public void configure_without_data_transaction (Connection conn, String sUser, String sUserID, ServletContext context, String sServerID) throws Exception{

    	try {
    		validate_configuration_fields(conn);
		} catch (Exception e1) {
			throw new Exception (e1.getMessage());
		}
    	
    	//Load the controller so that it's up to date:
    	load(conn);
    	
    	//Build command string:
    	ArrayList<String>arrConfigFileKeys = new ArrayList<String>(0);
    	ArrayList<String>arrConfigFileKeyValues = new ArrayList<String>(0);
    	arrConfigFileKeys.add(SSConstants.CONFFILE_DATABASE_ID);
    	arrConfigFileKeyValues.add(getsconfigfiledatabaseid());
    	arrConfigFileKeys.add(SSConstants.CONFFILE_KEY_NAME);
    	arrConfigFileKeyValues.add(this.getsconfigfilecontrollername());
    	arrConfigFileKeys.add(SSConstants.CONFFILE_KEY_PASSCODE);
    	arrConfigFileKeyValues.add(getsconfigfilepasscode());
    	arrConfigFileKeys.add(SSConstants.CONFFILE_LISTENING_PORT);
    	arrConfigFileKeyValues.add(getsconfigfilelisteningport());
    	arrConfigFileKeys.add(SSConstants.CONFFILE_LOG_FILE);
    	arrConfigFileKeyValues.add(getsconfigfilelogfilename());
    	arrConfigFileKeys.add(SSConstants.CONFFILE_LOGGING_LEVEL);
    	arrConfigFileKeyValues.add(getsconfigfilelogginglevel());
    	arrConfigFileKeys.add(SSConstants.CONFFILE_WEB_APP_PORT);
    	arrConfigFileKeyValues.add(getsconfigfilewebappport());
    	arrConfigFileKeys.add(SSConstants.CONFFILE_WEB_APP_URL);
    	arrConfigFileKeyValues.add(getsconfigfilewebappurl());
    	
    	String sRequestString = SSUtilities.buildRequestFromServerToUpdateConfigFile(
    		arrConfigFileKeys, 
    		arrConfigFileKeyValues, 
    		getscontrollername(), 
    		getspasscode(),
    		sUser,
    		SMUtilities.getFullNamebyUserID(sUserID, conn),
    		sServerID
    	);
    	
    	ASClientService client = new ASClientService();
    	try {
			client.sendRequest(
				getscontrollerurl(), 
				Integer.parseInt(getslisteningport()), 
				sRequestString, 
				false);
		} catch (Exception e1) {
			throw new Exception("Error [1463689221] sending command to configure controller - " + e1.getMessage());
		}
    	
    	//Now update the controller values in the current program:
    	//NOTE: DO NOT SET THE LISTING PORT, because if port forwarding is being used, the port the controller is listening on
    	//may be different than the port that the SMCP program has to connect on.
    	//setslisteningport(getsconfigfilelisteningport());
    	setspasscode(getsconfigfilepasscode());
    	setscontrollername(getsconfigfilecontrollername());
    	
    	String SQL = "";
    	String sLastMaintainedByFullName = "N/A";
    	SQL = "SELECT"
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
				sLastMaintainedByFullName = rs.getString(SMTableusers.sUserFirstName).trim() 
						+ " " + rs.getString(SMTableusers.sUserLastName).trim();
			}
			rs.close();
		} catch (Exception e) {
			throw new Exception("Error [1458864730] reading user full name to save record - " + e.getMessage());
		}
    	setllastmaintainedbyid(sUserID);
    	setslastmaintainedbyfullname(sLastMaintainedByFullName);
		SQL = "UPDATE " + SMTablesscontrollers.TableName + " SET "
			+ " " + SMTablesscontrollers.dattimelastmaintained + " = NOW()"
			+ ", " + SMTablesscontrollers.llastmaintainedbyid + " = " + clsDatabaseFunctions.FormatSQLStatement(getllastmaintainedbyid().trim()) + ""
			+ ", " + SMTablesscontrollers.slastmaintainedbyfullname + " = '" + clsDatabaseFunctions.FormatSQLStatement(getslastmaintainedbyfullname().trim()) + "'"
			// TJR - removed this 6/23/2016 because if port forwarding is being used, the controller may be listening on
			// a different port than is used to actually connect to it
			//+ ", " + SMTablesscontrollers.slisteningport + " = '" + SMUtilities.FormatSQLStatement(getslisteningport().trim()) + "'"
			+ ", " + SMTablesscontrollers.spasscode + " = '" + clsDatabaseFunctions.FormatSQLStatement(getspasscode().trim()) + "'"
			+ ", " + SMTablesscontrollers.scontrollername + " = '" + clsDatabaseFunctions.FormatSQLStatement(getscontrollername().trim()) + "'"
			+ " WHERE ("
				+ "(" + SMTablesscontrollers.lid + " = " + getslid() + ")"
			+ ")"
		;

		if (bDebugMode){
			System.out.println("In " + this.toString() + " - save SQL = " + SQL);
		}
		try{
		    Statement stmt = conn.createStatement();
		    stmt.executeUpdate(SQL);
		}catch (Exception ex) {
			throw new Exception ("Error [1463689222] in insert/update with SQL: " + SQL + " - " + ex.getMessage());
		}
    }
    public String getLogFile (ServletContext context, String sConf, String sUser, String sUserID, String sUserFullName,  String sServerID) throws Exception{

    	//First, load the controller:
    	try {
			load(context, sConf, sUserID, sUserFullName);
		} catch (Exception e2) {
			throw new Exception("Error [1463704324] loading controller = " + e2.getMessage());
		}
    	
    	//Next, get the log file into a buffer:
    	String sResponse = "";
    	
    	//Now try to read the log:
    	String sRequestString = SSUtilities.buildRequestFromServerToGetLog(
    		getscontrollername(), 
    		getspasscode(),
    		sUser,
    		SMUtilities.getUserFullName(sUser, context, sConf, "SSController.getLogFile"),
    		sServerID
    	);
    	ASClientService client = new ASClientService();
    	try {
    		sResponse = client.sendRequest(
				getscontrollerurl(), 
				Integer.parseInt(getslisteningport()), 
				sRequestString, 
				false);
		} catch (Exception e1) {
			throw new Exception("Error [1463689221] sending command to get controller log - " + e1.getMessage());
		}
        	
    	//Pick off the first query key pairs:
    	//This will be the first part of the response, which we'll throw away.
    	//After that, everything will be purely the log file:
    	String sPrefix = SSUtilities.buildKeyValuePair(SSConstants.QUERY_KEY_ACKNOWLEDGMENT, SSConstants.QUERY_KEYVALUE_ACKNOWLEDGMENT_SUCCESSFUL)
				+ SSConstants.QUERY_STRING_DELIMITER
				+ SSUtilities.buildKeyValuePair(SSConstants.QUERY_KEY_LOGFILE_CONTENTS, "");
    	//We throw away the command prefix, and also replace all the 'newlines' with '<BR>'s:
    	String sLogFileBuffer = 
    		sResponse.replace(sPrefix, "").replace("\n", "<BR>").replace(
    			SSConstants.QUERY_EQUALS_SIGN_MASK, SSConstants.QUERY_STRING_EQUALS_SYMBOL);
    	return sLogFileBuffer;
    }
    public String getSampleCommands (ServletContext context, String sConf, String sUser, String sUserID, String sUserFullName, String sServerID) throws Exception{

    	//First, load the controller:
    	try {
			load(context, sConf, sUserID, sUserFullName);
		} catch (Exception e2) {
			throw new Exception("Error [1464294098] loading controller = " + e2.getMessage());
		}
    	
    	//Next, get the log file into a buffer:
    	String sResponse = "";
    	
    	//Now try to read the log:
    	String sRequestString = SSUtilities.buildRequestFromServerToListSampleCommands(
    		getscontrollername(), 
    		getspasscode(),
    		sUser,
    		SMUtilities.getUserFullName(sUser, context, sConf, "SSController.getSampleCommands"),
    		sServerID
    	);
    	ASClientService client = new ASClientService();
    	try {
    		sResponse = client.sendRequest(
				getscontrollerurl(), 
				Integer.parseInt(getslisteningport()), 
				sRequestString, 
				false);
		} catch (Exception e1) {
			throw new Exception("Error [1464294099] sending command to get sample commands - " + e1.getMessage());
		}
        	
    	//Pick off the first query key pairs:
    	//This will be the first part of the response, which we'll throw away.
    	//After that, everything will be purely the log file:
    	String sPrefix = SSUtilities.buildKeyValuePair(SSConstants.QUERY_KEY_ACKNOWLEDGMENT, SSConstants.QUERY_KEYVALUE_ACKNOWLEDGMENT_SUCCESSFUL)
				+ SSConstants.QUERY_STRING_DELIMITER
				+ SSUtilities.buildKeyValuePair(SSConstants.QUERY_KEY_SAMPLE_COMMAND_CONTENTS, "");
    	//We throw away the command prefix, and also replace all the 'newlines' with '<BR>'s:
    	String sResponseBuffer = 
    		sResponse.replace(sPrefix, "").replace("\n", "<BR>").replace(
    			SSConstants.QUERY_EQUALS_SIGN_MASK, SSConstants.QUERY_STRING_EQUALS_SYMBOL);
    	return sResponseBuffer;
    }
    public String getTerminalMappings (ServletContext context, String sConf, String sUser, String sUserID, String sUserFullName, String sServerID) throws Exception{

    	//First, load the controller:
    	try {
			load(context, sConf, sUserID, sUserFullName);
		} catch (Exception e2) {
			throw new Exception("Error [1464294100] loading controller = " + e2.getMessage());
		}
    	
    	//Next, get the log file into a buffer:
    	String sResponse = "";
    	
    	//Now try to read the log:
    	String sRequestString = SSUtilities.buildRequestFromServerToListPinMappings(
    		getscontrollername(), 
    		getspasscode(),
    		sUser,
    		SMUtilities.getUserFullName(sUser, context, sConf, "SSController.getTerminalMappings"),
    		sServerID
    	);
    	ASClientService client = new ASClientService();
    	try {
    		sResponse = client.sendRequest(
				getscontrollerurl(), 
				Integer.parseInt(getslisteningport()), 
				sRequestString, 
				false);
		} catch (Exception e1) {
			throw new Exception("Error [1464294101] sending command to get terminal mappings - " + e1.getMessage());
		}
        	
    	//Pick off the first query key pairs:
    	//This will be the first part of the response, which we'll throw away.
    	//After that, everything will be purely the log file:
    	String sPrefix = SSUtilities.buildKeyValuePair(SSConstants.QUERY_KEY_ACKNOWLEDGMENT, SSConstants.QUERY_KEYVALUE_ACKNOWLEDGMENT_SUCCESSFUL)
				+ SSConstants.QUERY_STRING_DELIMITER
				+ SSUtilities.buildKeyValuePair(SSConstants.QUERY_KEY_PIN_MAPPING_LIST, "");
    	//We throw away the command prefix, and also replace all the 'newlines' with '<BR>'s:
    	String sResponseBuffer = 
    		sResponse.replace(sPrefix, "").replace("\n", "<BR>").replace(
    			SSConstants.QUERY_EQUALS_SIGN_MASK, SSConstants.QUERY_STRING_EQUALS_SYMBOL);
    	return sResponseBuffer;
    }
    public void updateControllerVersion (ServletContext context, String sConf, String sUser, String sUserID, String sUserFullName,  String sServerID) throws Exception{

    	//First, load the controller:
    	try {
			load(context, sConf, sUserID, sUserFullName);
		} catch (Exception e2) {
			throw new Exception("Error [1464030225] loading controller = " + e2.getMessage());
		}
    	
    	//Now try to read the log:
    	String sRequestString = SSUtilities.buildRequestFromServerToUpdateProgram(
    		getscontrollername(), 
    		getspasscode(),
    		sUser,
    		SMUtilities.getUserFullName(sUser, context, sConf, "SSController.updateControllerVersion"),
    		sServerID);
    	ASClientService client = new ASClientService();
    	try {
    		client.sendRequest(
				getscontrollerurl(), 
				Integer.parseInt(getslisteningport()), 
				sRequestString, 
				false);
		} catch (Exception e1) {
			throw new Exception("Error [1464030226] sending command to configure controller - " + e1.getMessage());
		}

    	return;
    }
    public void restartController (ServletContext context, String sConf, String sUser, String sUserID, String sUserFullName,  String sServerID) throws Exception{

    	//First, load the controller:
    	try {
			load(context, sConf, sUserID, sUserFullName);
		} catch (Exception e2) {
			throw new Exception("Error [1466039397] loading controller = " + e2.getMessage());
		}
    	
    	//Now try to read the log:
    	String sRequestString = SSUtilities.buildRequestFromServerToRestartProgram(
    		getscontrollername(), 
    		getspasscode(),
    		sUser,
    		SMUtilities.getUserFullName(sUser, context, sConf, "SSController.restartController"),
    		sServerID);
    	ASClientService client = new ASClientService();
    	try {
    		client.sendRequest(
				getscontrollerurl(), 
				Integer.parseInt(getslisteningport()), 
				sRequestString, 
				false);
		} catch (Exception e1) {
			throw new Exception("Error [1466039398] sending command to restart controller - " + e1.getMessage());
		}

    	return;
    }
    public boolean getTestModeState (ServletContext context, String sConf, String sUser, String sUserID, String sUserFullName,  String sServerID) throws Exception{

    	//First, load the controller:
    	try {
			load(context, sConf, sUserID, sUserFullName);
		} catch (Exception e2) {
			throw new Exception("Error [1464745368] loading controller = " + e2.getMessage());
		}
    	
    	//Next, get the log file into a buffer:
    	String sResponse = "";
    	
    	//Now try to read the log:
    	String sRequestString = SSUtilities.buildRequestFromServerToGetTestMode(
    		getscontrollername(), 
    		getspasscode(),
    		sUser,
    		SMUtilities.getUserFullName(sUser, context, sConf, "SSController.getTestModeState"),
    		sServerID
    	);
    	ASClientService client = new ASClientService();
    	try {
    		sResponse = client.sendRequest(
				getscontrollerurl(), 
				Integer.parseInt(getslisteningport()), 
				sRequestString, 
				false);
		} catch (Exception e1) {
			throw new Exception("Error [1464745369] sending command to get terminal mappings - " + e1.getMessage());
		}
        	
    	//Get the current test mode state (on or off):
    	if (SSUtilities.getKeyValue(
    		sResponse, SSConstants.QUERY_KEY_TEST_MODE_STATE).compareToIgnoreCase(SSConstants.QUERY_KEYVALUE_TEST_MODE_ON) == 0){
    		return true;
    	}else{
    		return false;
    	}
    }
    public void setTestModeState (ServletContext context, String sConf, String sUser, String sUserID, String sUserFullName,  String sTestMode, String sServerID) throws Exception{

    	//First, load the controller:
    	try {
			load(context, sConf, sUserID, sUserFullName);
		} catch (Exception e2) {
			throw new Exception("Error [1464885925] loading controller = " + e2.getMessage());
		}
    	
    	//Now try to set the test state:
    	String sRequestString = SSUtilities.buildRequestFromServerToSetTestMode(
    		getscontrollername(), 
    		getspasscode(),
    		sUser,
    		SMUtilities.getUserFullName(sUser, context, sConf, "SSController.getTestModeState"),
    		sTestMode,
    		sServerID
    	);
    	ASClientService client = new ASClientService();
    	try {
    		client.sendRequest(
				getscontrollerurl(), 
				Integer.parseInt(getslisteningport()), 
				sRequestString, 
				false);
		} catch (Exception e1) {
			throw new Exception("Error [1464885926] sending command to set test mode - " + e1.getMessage());
		}
    }
    
	public String getslid() {
		return m_slid;
	}

	public void setslid(String slid) {
		m_slid = slid;
	}

	public String getscontrollername() {
		return m_scontrollername;
	}

	public void setscontrollername(String scontrollername) {
		m_scontrollername = scontrollername;
	}

	public String getsdescription() {
		return m_sdescription;
	}

	public void setsdescription(String sdescription) {
		m_sdescription = sdescription;
	}

	public String getspasscode() {
		return m_spasscode;
	}

	public void setspasscode(String spasscode) {
		m_spasscode = spasscode;
	}

	public String getsdattimelastmaintained() {
		return m_sdattimelastmaintained;
	}

	public void setsdattimelastmaintained(String sdattimelastmaintained) {
		m_sdattimelastmaintained = sdattimelastmaintained;
	}

	public String getllastmaintainedbyid() {
		return m_llastmaintainedbyid;
	}

	public void setllastmaintainedbyid(String slastmaintainedbyid) {
		m_llastmaintainedbyid = slastmaintainedbyid;
	}

	public String getslastmaintainedbyfullname() {
		return m_slastmaintainedbyfullname;
	}

	public void setslastmaintainedbyfullname(String slastmaintainedbyfullname) {
		m_slastmaintainedbyfullname = slastmaintainedbyfullname;
	}

	public String getscontrollerurl() {
		return m_scontrollerurl;
	}

	public void setscontrollerurl(String scontrollerurl) {
		m_scontrollerurl = scontrollerurl;
	}

	public String getslisteningport() {
		return m_slisteningport;
	}

	public void setslisteningport(String slisteningport) {
		m_slisteningport = slisteningport;
	}
	
	public String getsactive() {
		return m_sactive;
	}

	public void setsactive(String sactive) {
		m_sactive = sactive;
	}

	public String getObjectName(){
		return ParamObjectName;
	}
	
	//These are the 'config file' values that are in the actual controller itself:
	public String getsconfigfilepasscode() {
		return m_sconfigfilepasscode;
	}
	public void setconfigfilepasscode(String sconfigfilepasscode) {
		m_sconfigfilepasscode = sconfigfilepasscode;
	}
	public String getsconfigfilecontrollername() {
		return m_sconfigfilecontrollername;
	}
	public void setsconfigfilecontrollername(String sconfigfilecontrollername) {
		m_sconfigfilecontrollername = sconfigfilecontrollername;
	}
	public String getsconfigfilelisteningport() {
		return m_sconfigfilelisteningport;
	}
	public void setsconfigfilelisteningport(String sconfigfilelisteningport) {
		m_sconfigfilelisteningport = sconfigfilelisteningport;
	}
	public String getsconfigfilewebappurl() {
		return m_sconfigfilewebbappurl;
	}
	public void setsconfigfilewebappurl(String sconfigfilewebappurl) {
		m_sconfigfilewebbappurl = sconfigfilewebappurl;
	}
	public String getsconfigfilewebappport() {
		return m_sconfigfilewebappport;
	}
	public void setsconfigfilewebappport(String sconfigfilewebappport) {
		m_sconfigfilewebappport = sconfigfilewebappport;
	}
	public String getsconfigfiledatabaseid() {
		return m_sconfigfiledatabaseid;
	}
	public void setsconfigfiledatabaseid(String sconfigfiledatabaseid) {
		m_sconfigfiledatabaseid = sconfigfiledatabaseid;
	}
	public String getsconfigfilelogginglevel() {
		return m_sconfigfilelogginglevel;
	}
	public void setsconfigfilelogginglevel(String sconfigfilelogginglevel) {
		m_sconfigfilelogginglevel = sconfigfilelogginglevel;
	}
	public String getsconfigfilelogfilename() {
		return m_sconfigfilelogfilename;
	}
	public void setsconfigfilelogfilename(String sconfigfilelogfilename) {
		m_sconfigfilelogfilename = sconfigfilelogfilename;
	}
	public String getcontrollersoftwareversion(){
		return m_scontrollersoftwareversion;
	}
	public void setcontrollersoftwareversion(String scontrollersoftwareversion){
		m_scontrollersoftwareversion = scontrollersoftwareversion;
	}
	public String getcontrollerhostname(){
		return m_scontrollerhostname;
	}
	public void setcontrollerhostname(String scontrollerhostname){
		m_scontrollerhostname = scontrollerhostname;
	}
	
	public ArrayList<SSDevice> getDeviceList(
		Connection conn, 
		boolean bUpdateContactStatus, 
		boolean bOutputDiagnostics,
		String sUser,
		ServletContext context,
		String sServerID) throws Exception{
		
		//Make sure this class is loaded before we even try this:
		//try {
		//	load(conn);
		//} catch (Exception e1) {
		//	throw new Exception("Error [1459949356] could not load controller - " + e1.getMessage());
		//}
		
		ArrayList<SSDevice>arrDevices = new ArrayList<SSDevice>(0);
		String SQL = "SELECT"
			+ " * FROM " + SMTablessdevices.TableName
			+ " WHERE ("
				+ "(" + SMTablessdevices.linputcontrollerid + " = " + getslid() + ")"
				+ " OR (" + SMTablessdevices.loutputcontrollerid + " = " + getslid() + ")"
			+ ")"
		;
		try {
			ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
			while (rs.next()){
				SSDevice device = new SSDevice();
				device.setslid(Long.toString(rs.getLong(SMTablessdevices.lid)));
				try {
					device.load(conn);
				} catch (Exception e) {
					throw new Exception("Error [1459948716] loading device with ID '" + rs.getLong(SMTablessdevices.lid) + " - " + e.getMessage());
				}
				arrDevices.add(device);
			}
			rs.close();
		} catch (Exception e) {
			throw new Exception("Error [1459948717] reading devices for controller ID '" + getslid() + " - " + e.getMessage());
		}
		
		if (bUpdateContactStatus){
			//Now get the active status of all the devices and add that to the array:
			String sGetActivationStatusRequest = SSUtilities.buildRequestFromServerToGetTerminalsContactStatus(
				getscontrollername(),
				getspasscode(),
				sUser,
				SMUtilities.getUserFullName(sUser, conn),
				sServerID
			);
			if (bOutputDiagnostics){
				System.out.println("[1459951576] sGetActivationStatusRequest = '" + sGetActivationStatusRequest + "'\n"
					+ "getscontrollerurl() = '" + getscontrollerurl() + "'\n"
					+ "getslisteningport() = '" + getslisteningport() + "'\n"
				);
			}
			//Now send the request to the controller to get a list of the activation states of all the devices:
			String sResponse = "";
			boolean bReadingControllerFailed = false;
			try {
				ASClientService cs = new ASClientService();
				sResponse = cs.sendRequest(
					getscontrollerurl(),
					Integer.parseInt(getslisteningport()),
					sGetActivationStatusRequest,
					bOutputDiagnostics)
				;
			} catch (Exception e) {
				//throw new Exception ("Error [1459275825] - request to activate device ID " + this.getslid() 
				//	+ " was not successfully sent " + e.getMessage());
				bReadingControllerFailed = true;
			}
			
			//Parse off the activation states from the response for each device and terminal:
			for (int i = 0; i < arrDevices.size(); i++){
				if (bReadingControllerFailed){
					arrDevices.get(i).setinputcontactstatus("N/A");
					arrDevices.get(i).setoutputcontactstatus("N/A");
				}else{
					arrDevices.get(i).setinputcontactstatus(
						SSUtilities.getKeyValue(sResponse, SSConstants.QUERY_KEY_TERMINALCONTACTSTATE_PREFIX + arrDevices.get(i).getsinputterminalnumber()));
					arrDevices.get(i).setoutputcontactstatus(
						SSUtilities.getKeyValue(sResponse, SSConstants.QUERY_KEY_TERMINALCONTACTSTATE_PREFIX + arrDevices.get(i).getsoutputterminalnumber()));
				}
			}

			//Now get the listening status of all the devices and add that to the array:
			String sGetListeningStatusRequest = SSUtilities.buildRequestFromServerToGetInputTerminalsListeningStatus(
				getscontrollername(),
				getspasscode(),
				sUser,
				SMUtilities.getUserFullName(sUser, conn),
				sServerID
			);
			if (bOutputDiagnostics){
				System.out.println("[1463525462] sGetListeningStatusRequest = '" + sGetActivationStatusRequest + "'\n"
					+ "getscontrollerurl() = '" + getscontrollerurl() + "'\n"
					+ "getslisteningport() = '" + getslisteningport() + "'\n"
				);
			}
			//Now send the request to the controller to get a list of the listening states of all the devices:
			sResponse = "";
			bReadingControllerFailed = false;
			try {
				ASClientService cs = new ASClientService();
				sResponse = cs.sendRequest(
					getscontrollerurl(),
					Integer.parseInt(getslisteningport()),
					sGetListeningStatusRequest,
					bOutputDiagnostics)
				;
			} catch (Exception e) {
				bReadingControllerFailed = true;
			}
			
			//Parse off the listening states from the response for each device and terminal:
			for (int i = 0; i < arrDevices.size(); i++){
				if (bReadingControllerFailed){
					arrDevices.get(i).setinputlisteningstatus("N/A");
				}else{
					arrDevices.get(i).setinputlisteningstatus(
						SSUtilities.getKeyValue(sResponse, SSConstants.QUERY_KEY_TERMINALLISTENINGSTATE_PREFIX + arrDevices.get(i).getsinputterminalnumber()));
				}
			}

		}
		//Now all the devices are included with their current activation status:
		return arrDevices;
	}
	public void setListeningStateOfInputTerminals(
			Connection conn, 
			ArrayList<String>arrDeviceIDs, 
			String sListeningState,
			String sUser,
			ServletContext context,
			String sServerID) throws Exception{

	String sFullCommandString = 
		//Passcode
		SSUtilities.buildKeyValuePair(SSConstants.QUERY_KEY_PASSCODE, getspasscode())
		
		//Controller name
		+ SSConstants.QUERY_STRING_DELIMITER
		+ SSUtilities.buildKeyValuePair(SSConstants.QUERY_KEY_CONTROLLERNAME, getscontrollername())
		
		//Command
		+ SSConstants.QUERY_STRING_DELIMITER
		+ SSUtilities.buildKeyValuePair(
			SSConstants.QUERY_KEY_COMMAND, SSConstants.QUERY_KEYVALUE_COMMAND_SET_TERMINAL_LISTENING_STATUS);
			
	ArrayList<SSDevice>arrDevices = getDeviceList(conn, false, SSConstants.OUTPUT_DIAGNOSTIC_MESSAGES, sUser, context, sServerID);
	for (int i = 0; i < arrDevices.size(); i++){
		for (int j = 0; j < arrDeviceIDs.size(); j++){
			if (arrDevices.get(i).getslid().compareToIgnoreCase(arrDeviceIDs.get(j)) == 0){
				sFullCommandString += SSConstants.QUERY_STRING_DELIMITER
					+ SSUtilities.buildKeyValuePair(
						SSConstants.QUERY_KEY_TERMINALLISTENINGSTATE_PREFIX + arrDevices.get(i).getsinputterminalnumber(), sListeningState);
			}
		}
	}
		
	//Send the command to the controller:
	if (SSConstants.OUTPUT_DIAGNOSTIC_MESSAGES){
    	System.out.println("[1459468087] - request string being sent to controller: " + sFullCommandString);
    }
	try {
		ASClientService cs = new ASClientService();
		cs.sendRequest(
			getscontrollerurl(),
			Integer.parseInt(getslisteningport()),
			sFullCommandString,
			SSConstants.OUTPUT_DIAGNOSTIC_MESSAGES
		);
	} catch (Exception e) {
		throw new Exception (e.getMessage());
	}
}
	//This function checks to make sure that all the trigger devices are in their 'NORMAL' contact state,
	//so that if not, the user can be notified.  In that case, the user gets a chance to override the malfunctioning
	//device(s).
public void readInputTerminalsContactNormalStatus(
		Connection conn, 
		ArrayList<String>arrDeviceIDs, 
		String sUser, 
		ServletContext context,
		String sServerID) throws Exception{
	
	String sNotNormalContactList = "";
	ArrayList<SSDevice>arrAllDevicesOnController = getDeviceList(
		conn, 
		true, 
		SSConstants.OUTPUT_DIAGNOSTIC_MESSAGES, 
		sUser, 
		context,
		sServerID);
	for (int i = 0; i < arrAllDevicesOnController.size(); i++){
		for (int j = 0; j < arrDeviceIDs.size(); j++){
			SSDevice device = arrAllDevicesOnController.get(i);
			//If this device is one of the ones we are supposed to be checking, then check it:
			if (device.getslid().compareToIgnoreCase(arrDeviceIDs.get(j)) == 0){
				//If the input contacts are normally closed, then if they're currently 'OPEN', report it:
				if (device.getsinputtype().compareToIgnoreCase(Integer.toString(SMTablessdevices.DEVICE_INPUT_TYPE_NORMALLY_CLOSED)) == 0){
					if (device.getinputcontactstatus().compareToIgnoreCase(SSConstants.QUERY_KEYVALUE_TERMINAL_CONTACT_STATUS_OPEN) == 0){
						sNotNormalContactList += " " + device.getsdescription() + " is not set.  ";	
					}
				}
				//If the input contacts are normally open, then if they're currently 'CLOSED', report it:
				if (device.getsinputtype().compareToIgnoreCase(Integer.toString(SMTablessdevices.DEVICE_INPUT_TYPE_NORMALLY_OPEN)) == 0){
					if (device.getinputcontactstatus().compareToIgnoreCase(SSConstants.QUERY_KEYVALUE_TERMINAL_CONTACT_STATUS_CLOSED) == 0){
						sNotNormalContactList += " " + device.getsdescription() + " is not set.  ";	
					}
				}
			}
		}
	}
	if (sNotNormalContactList.compareToIgnoreCase("") != 0){
		throw new Exception(sNotNormalContactList);
	}
}
public void readControllerConfiguration(String sUser, String sUserFullName, ServletContext context, String sServerID) throws Exception{

	//Now get the config file from the controller:
	String sGetConfigFileRequest = SSUtilities.buildRequestFromServerToGetConfigFile(
			getscontrollername(),
			getspasscode(),
			sUser,
			sUserFullName,
			sServerID
			);
	//Now send the request to the controller to get a list of the activation states of all the devices:
	String sResponse = "";
	try {
		ASClientService cs = new ASClientService();
		sResponse = cs.sendRequest(
				getscontrollerurl(),
				Integer.parseInt(getslisteningport()),
				sGetConfigFileRequest,
				false)
				;
	} catch (Exception e) {
		throw new Exception("Error [1463605229] - could not read config file from controller - " + e.getMessage());
	}

	//Pick off the config file value:
	String sConfigFile = SSUtilities.getKeyValue(
			sResponse, SSConstants.QUERY_KEY_CONFIGFILE_CONTENTS);

	sConfigFile = sConfigFile.replace(SSConstants.QUERY_EQUALS_SIGN_MASK, SSConstants.QUERY_STRING_EQUALS_SYMBOL);

	//Now read each line into a String array:
	String sConfigLines[] = sConfigFile.split("\n");

	//Initialize all the controller's variables here:
	initControllerRemoteVariables();

	//Parse off each of the config file values and place them in their corresponding variable:
	for (int i = 0; i < sConfigLines.length; i++){
		if (SSUtilities.getKeyValue(sConfigLines[i], SSConstants.CONFFILE_KEY_NAME).compareToIgnoreCase("") != 0){
			setsconfigfilecontrollername(SSUtilities.getKeyValue(sConfigLines[i], SSConstants.CONFFILE_KEY_NAME));
		}
		if (SSUtilities.getKeyValue(sConfigLines[i], SSConstants.CONFFILE_DATABASE_ID).compareToIgnoreCase("") != 0){
			setsconfigfiledatabaseid(SSUtilities.getKeyValue(sConfigLines[i], SSConstants.CONFFILE_DATABASE_ID));
		}
		if (SSUtilities.getKeyValue(sConfigLines[i], SSConstants.CONFFILE_KEY_PASSCODE).compareToIgnoreCase("") != 0){
			setconfigfilepasscode(SSUtilities.getKeyValue(sConfigLines[i], SSConstants.CONFFILE_KEY_PASSCODE));
		}
		if (SSUtilities.getKeyValue(sConfigLines[i], SSConstants.CONFFILE_LISTENING_PORT).compareToIgnoreCase("") != 0){
			setsconfigfilelisteningport(SSUtilities.getKeyValue(sConfigLines[i], SSConstants.CONFFILE_LISTENING_PORT));
		}
		if (SSUtilities.getKeyValue(sConfigLines[i], SSConstants.CONFFILE_LOG_FILE).compareToIgnoreCase("") != 0){
			setsconfigfilelogfilename(SSUtilities.getKeyValue(sConfigLines[i], SSConstants.CONFFILE_LOG_FILE));
		}
		if (SSUtilities.getKeyValue(sConfigLines[i], SSConstants.CONFFILE_LOGGING_LEVEL).compareToIgnoreCase("") != 0){
			setsconfigfilelogginglevel(SSUtilities.getKeyValue(sConfigLines[i], SSConstants.CONFFILE_LOGGING_LEVEL));
		}
		if (SSUtilities.getKeyValue(sConfigLines[i], SSConstants.CONFFILE_WEB_APP_PORT).compareToIgnoreCase("") != 0){
			setsconfigfilewebappport(SSUtilities.getKeyValue(sConfigLines[i], SSConstants.CONFFILE_WEB_APP_PORT));
		}
		if (SSUtilities.getKeyValue(sConfigLines[i], SSConstants.CONFFILE_WEB_APP_URL).compareToIgnoreCase("") != 0){
			setsconfigfilewebappurl(SSUtilities.getKeyValue(sConfigLines[i], SSConstants.CONFFILE_WEB_APP_URL));
		}
	}

	//Next get the version from the controller:
	String sGetControllerSoftwareVersionRequest = SSUtilities.buildRequestFromServerToGetControllerSystemInformation(
			getscontrollername(),
			getspasscode(),
			sUser,
			sUserFullName,
			sServerID
			);
	//Now send the request to the controller to get system information:
	sResponse = "";
	try {
		ASClientService cs = new ASClientService();
		sResponse = cs.sendRequest(
				getscontrollerurl(),
				Integer.parseInt(getslisteningport()),
				sGetControllerSoftwareVersionRequest,
				false)
				;
	} catch (Exception e) {
		throw new Exception("Error [1463605230] - could not read software version from controller - " + e.getMessage());
	}

	setcontrollersoftwareversion(SSUtilities.getKeyValue(sResponse, SSConstants.QUERY_KEY_VERSION));
	setcontrollerhostname(SSUtilities.getKeyValue(sResponse, SSConstants.QUERY_KEY_CONTROLLER_HOSTNAME));
}

/*
	public String read_out_debug_data(){
    	String sResult = "  ** " + SMUtilities.getFullClassName(this.toString()) + " read out: ";
    	sResult += "\nTerms code: " + this.getsTermsCode();
    	sResult += "\nTerms desc: " + this.getsTermsDescription();    	
    	sResult += "\nLast maintained: " + this.getsLastMaintainedDate();
    	sResult += "\nActive: " + this.getsActive();
    	sResult += "\nDiscount percentage: " + this.getsDiscountPercentage();
    	sResult += "\nDiscount number of days: " + this.getsDiscountNumberOfDays();
    	sResult += "\nDiscount day of the month: " + this.getsDiscountDayOfTheMonth();
    	sResult += "\nDue number of days: " + this.getsDueNumberOfDays();
    	sResult += "\nDue day of the month: " + this.getsDueDayOfTheMonth();    	
    	sResult += "\nObject name: " + this.getObjectName();
    	return sResult;
    }
*/
    private void initControllerVariables(){
    	m_slid = "-1";
    	m_scontrollername = "";
    	m_sdescription = "";
    	m_spasscode = "";
    	m_sdattimelastmaintained = EMPTY_DATETIME_STRING;
    	m_llastmaintainedbyid = "0";
    	m_slastmaintainedbyfullname = "";
    	m_scontrollerurl = "";
    	m_slisteningport = "";
    	m_sactive = "1";
    	initControllerRemoteVariables();

	}
    private void initControllerRemoteVariables(){
    	//Config file values:
    	m_sconfigfilepasscode = "";
    	m_sconfigfilecontrollername = "";
    	m_sconfigfilelisteningport = "";
    	m_sconfigfilewebbappurl = "";
    	m_sconfigfilewebappport = "";
    	m_sconfigfiledatabaseid = "";
    	m_sconfigfilelogginglevel = "";
    	m_sconfigfilelogfilename = "";
    	m_scontrollersoftwareversion = "";
    	m_scontrollerhostname = "";
    }
}
