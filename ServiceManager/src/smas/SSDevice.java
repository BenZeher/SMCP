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
import SMDataDefinition.SMTablessalarmactivationdevices;
import SMDataDefinition.SMTablessalarmtriggerdevices;
import SMDataDefinition.SMTablessdevices;
import SMDataDefinition.SMTablessdeviceusers;
import SMDataDefinition.SMTablesseventscheduledetails;
import ServletUtilities.clsMasterEntry;
import smcontrolpanel.SMUtilities;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsManageRequestParameters;

public class SSDevice extends clsMasterEntry{
	
	public static final String ParamObjectName = "Security system device";
	public static final int NO_ZONE_SELECTED_VALUE = 0;
	
	private String m_slid;
	private String m_sinputcontrollerid;
	private String m_soutputcontrollerid;
	private String m_sdescription;
	private String m_sinputterminal;
	private String m_sinputtype;
	private String m_soutputterminal;
	private String m_sdevicetype;
	private String m_sactivationtype;
	private String m_scontactduration;
	//These values are never stored - they are always set on the fly:
	private String m_sinputcontactstatus;
	private String m_soutputcontactstatus;
	private String m_sinputlisteningstatus;
	private String m_sremarks;
	private String m_sactive;
	private boolean bOutputDiagnostics = false;
	
    public SSDevice() {
		super();
		initRecordVariables();
        }
    
    SSDevice(HttpServletRequest req){
		super(req);
		initRecordVariables();
		m_slid = clsManageRequestParameters.get_Request_Parameter(
				SMTablessdevices.lid, req).trim();
		if (m_slid.compareToIgnoreCase("") == 0){
			m_slid = "-1";
		}
		m_sinputcontrollerid = clsManageRequestParameters.get_Request_Parameter(
				SMTablessdevices.linputcontrollerid, req).trim();
		m_soutputcontrollerid = clsManageRequestParameters.get_Request_Parameter(
				SMTablessdevices.loutputcontrollerid, req).trim();
		m_sdescription = clsManageRequestParameters.get_Request_Parameter(
				SMTablessdevices.sdescription, req).trim().replace("&quot;", "\"");
		m_sinputterminal = clsManageRequestParameters.get_Request_Parameter(
				SMTablessdevices.sinputterminalnumber, req).trim().replace("&quot;", "\"");
		m_soutputterminal = clsManageRequestParameters.get_Request_Parameter(
				SMTablessdevices.soutputterminalnumber, req).trim().replace("&quot;", "\"");
		m_sdevicetype = clsManageRequestParameters.get_Request_Parameter(
				SMTablessdevices.idevicetype, req).trim().replace("&quot;", "\"");
		m_sactivationtype = clsManageRequestParameters.get_Request_Parameter(
				SMTablessdevices.iactivationtype, req).trim().replace("&quot;", "\"");
		m_scontactduration = clsManageRequestParameters.get_Request_Parameter(
				SMTablessdevices.icontactduration, req).trim().replace("&quot;", "\"");
		m_sinputtype = clsManageRequestParameters.get_Request_Parameter(
				SMTablessdevices.iinputtype, req).trim().replace("&quot;", "\"");
		m_sremarks = clsManageRequestParameters.get_Request_Parameter(
				SMTablessdevices.sremarks, req).trim().replace("&quot;", "\"");
		if (clsManageRequestParameters.get_Request_Parameter(
				SMTablessdevices.iactive, req).trim().replace("&quot;", "\"").compareToIgnoreCase("") != 0){
			m_sactive = "1";
		}else{
			m_sactive = "0";
		}
    }
    public void load (ServletContext context, String sDBID, String sUser, String sUserID, String sUserFullName) throws Exception{
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
    		throw new Exception("Error [1458914858] opening data connection to load " + ParamObjectName + ".");
    	}
    	
    	try {
			load (conn);
		} catch (Exception e) {
			clsDatabaseFunctions.freeConnection(context, conn, "[1547067638]");
			throw new Exception(e.getMessage());
		}
    	clsDatabaseFunctions.freeConnection(context, conn, "[1547067639]");
    }
    public void load (Connection conn) throws Exception{
    	load (m_slid, conn);
    }
    private void load (String sID, Connection conn) throws Exception{

    	sID = sID.trim();
    	if (sID.compareToIgnoreCase("") == 0){
    		throw new Exception("ID code cannot be blank when loading " + ParamObjectName + ".");
    	}
		
		String SQL = "SELECT * FROM " + SMTablessdevices.TableName
			+ " WHERE ("
				+ SMTablessdevices.lid + " = " + sID
			+ ")";
		if (bOutputDiagnostics){
			System.out.println("[1459275625] In " + this.toString() + " - load SQL = " + SQL);
		}
		try {
			ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
			if (rs.next()) {
				//Load the variables here:
				m_slid = Long.toString(rs.getLong(SMTablessdevices.lid));
				m_sinputcontrollerid = Long.toString(rs.getLong(SMTablessdevices.linputcontrollerid));
				m_soutputcontrollerid = Long.toString(rs.getLong(SMTablessdevices.loutputcontrollerid));
				m_sdescription = rs.getString(SMTablessdevices.sdescription).trim();
				m_sinputterminal = rs.getString(SMTablessdevices.sinputterminalnumber).trim();
				m_soutputterminal = rs.getString(SMTablessdevices.soutputterminalnumber).trim();
				m_sdevicetype = Long.toString(rs.getLong(SMTablessdevices.idevicetype));
				m_sactivationtype = Long.toString(rs.getLong(SMTablessdevices.iactivationtype));
				m_scontactduration = Long.toString(rs.getLong(SMTablessdevices.icontactduration));
				m_sinputtype = Long.toString(rs.getLong(SMTablessdevices.iinputtype));
				m_sremarks = rs.getString(SMTablessdevices.sremarks);
				m_sactive = Long.toString(rs.getLong(SMTablessdevices.iactive));
				rs.close();
			} else {
				rs.close();
				throw new Exception(ParamObjectName + " does not exist.");
			}
		} catch (Exception e) {
			throw new Exception("Error [1458914859] reading " + ParamObjectName + " for lid : '" + sID
					+ "' - " + e.getMessage());
		}
		return;
    }
    
    public void save_without_data_transaction (ServletContext context, String sDBID, String sUser, String sUserID, String sUserFullName) throws Exception{
    	
       	Connection conn = clsDatabaseFunctions.getConnection(
    			context, 
    			sDBID, 
    			"MySQL", 
    			this.toString() + " - user: " 
    			+ sUserID
    			+ "- "
    			+ sUserFullName
    			);
    	
    	if (conn == null){
    		throw new Exception("Error [1458914860] opening data connection.");
    	}
    	
    	try {
			save_without_data_transaction (conn, sUser);
		} catch (Exception e) {
			clsDatabaseFunctions.freeConnection(context, conn, "[1547067640]");
			throw new Exception(e.getMessage());
		}
    	clsDatabaseFunctions.freeConnection(context, conn, "[1547067641]");
    	
    }
    public void save_without_data_transaction (Connection conn, String sUser) throws Exception{

    	try {
			validate_entry_fields(conn);
		} catch (Exception e1) {
			throw new Exception (e1.getMessage());
		}
    	String SQL = "";
 
		//If it's a new record, do an insert:
    	if (this.getslid().compareToIgnoreCase("-1") == 0){
    		SQL = "INSERT INTO " + SMTablessdevices.TableName + " ("
    			+ SMTablessdevices.iactivationtype
    			+ ", " + SMTablessdevices.icontactduration
    			+ ", " + SMTablessdevices.iactive
				+ ", " + SMTablessdevices.idevicetype
				+ ", " + SMTablessdevices.iinputtype
				+ ", " + SMTablessdevices.linputcontrollerid
				+ ", " + SMTablessdevices.loutputcontrollerid
				+ ", " + SMTablessdevices.sdescription
				+ ", " + SMTablessdevices.sinputterminalnumber
				+ ", " + SMTablessdevices.soutputterminalnumber
				+ ", " + SMTablessdevices.sremarks
				+ ") VALUES ("
				+ " " + getsactivationtype()
				+ ", " + getscontactduration()
				+ ", " + getsactive()
				+ ", " + getsdeviccetype()
				+ ", " + getsinputtype()
				+ ", " + getsinputcontrollerid()
				+ ", " + getsoutputcontrollerid()
				+ ", '" + clsDatabaseFunctions.FormatSQLStatement(getsdescription().trim()) + "'"
				+ ", '" + clsDatabaseFunctions.FormatSQLStatement(getsinputterminalnumber().trim()) + "'"
				+ ", '" + clsDatabaseFunctions.FormatSQLStatement(getsoutputterminalnumber().trim()) + "'"
				+ ", '" + clsDatabaseFunctions.FormatSQLStatement(getsremarks().trim()) + "'"
				+ ")"
    		;
    	}else{
    		//Update:
			SQL = " UPDATE " + SMTablessdevices.TableName + " SET"
			+ " " + SMTablessdevices.iactivationtype + " = " + getsactivationtype()
			+ ", " + SMTablessdevices.icontactduration + " = " + getscontactduration()
			+ ", " + SMTablessdevices.iactive + " = " + getsactive()
			+ ", " + SMTablessdevices.idevicetype + " = " + getsdeviccetype()
			+ ", " + SMTablessdevices.iinputtype + " = " + getsinputtype()
			+ ", " + SMTablessdevices.linputcontrollerid + " = " + getsinputcontrollerid()
			+ ", " + SMTablessdevices.loutputcontrollerid + " = " + getsoutputcontrollerid()
			+ ", " + SMTablessdevices.sdescription + " = '" + clsDatabaseFunctions.FormatSQLStatement(getsdescription().trim()) + "'"
			+ ", " + SMTablessdevices.sinputterminalnumber + " = '" + clsDatabaseFunctions.FormatSQLStatement(getsinputterminalnumber().trim()) + "'"
			+ ", " + SMTablessdevices.soutputterminalnumber + " = '" + clsDatabaseFunctions.FormatSQLStatement(getsoutputterminalnumber().trim()) + "'"
			+ ", " + SMTablessdevices.sremarks + " = '" + clsDatabaseFunctions.FormatSQLStatement(getsremarks().trim()) + "'"
			+ " WHERE ("
				+ "(" + SMTablessdevices.lid + " = " + getslid() + ")"
			+ ")"
		;
    	}
			
		if (bOutputDiagnostics){
			System.out.println("[1459275626] In " + this.toString() + " - save SQL = " + SQL);
		}
		try{
		    Statement stmt = conn.createStatement();
		    stmt.executeUpdate(SQL);
		}catch (Exception ex) {
			throw new Exception ("Error [1458914862] in insert/update with SQL: " + SQL + " - " + ex.getMessage());
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


	public void delete (ServletContext context, String sDBID, String sUser, String sUserID, String sUserFullName) throws Exception{
    	
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
    		throw new Exception ("Error [1458914863] opening data connection.");
    	}
    	
    	//TODO - delete any deviceusers and alarmsequence devices also:
    	
    	try {
			delete (conn);
		} catch (Exception e) {
			clsDatabaseFunctions.freeConnection(context, conn, "[1547067636]");
			throw new Exception(e.getMessage());
		}
    	clsDatabaseFunctions.freeConnection(context, conn, "[1547067637]");
    }
    public void delete (Connection conn) throws Exception{
    	
    	//Validate deletions
    	String SQL = "";
 
    	//Delete:
    	if (!clsDatabaseFunctions.start_data_transaction(conn)){
    		throw new Exception("Error [1458914865] - Could not start transaction when deleting " + ParamObjectName + ".");
    	}
    	
    	//Delete the device from any alarm sequences:
    	SQL = "DELETE FROM " + SMTablessalarmactivationdevices.TableName
    		+ " WHERE ("
    			+ SMTablessalarmactivationdevices.ldeviceid + " = " + getslid()
    		+ ")"
    		;
		try{
		    Statement stmt = conn.createStatement();
		    stmt.executeUpdate(SQL);
		}catch (Exception ex) {
			clsDatabaseFunctions.rollback_data_transaction(conn);
			throw new Exception("Error [1462304115] - Could not delete alarm activation device ID '" + getslid()  
				+ "' with SQL: " + SQL + " - " + ex.getMessage());
		}
    		
    	SQL = "DELETE FROM " + SMTablessalarmtriggerdevices.TableName
    		+ " WHERE ("
    			+ SMTablessalarmtriggerdevices.ldeviceid + " = " + getslid()
    		+ ")"
    		;
		try{
		    Statement stmt = conn.createStatement();
		    stmt.executeUpdate(SQL);
		}catch (Exception ex) {
			clsDatabaseFunctions.rollback_data_transaction(conn);
			throw new Exception("Error [1462304116] - Could not delete alarm trigger device ID '" + getslid()  
				+ "' with SQL: " + SQL + " - " + ex.getMessage());
		}
    	
    	//Delete the device from any permissions:
    	SQL = "DELETE FROM " + SMTablessdeviceusers.TableName
    		+ " WHERE ("
    			+ SMTablessdeviceusers.ldeviceid + " = " + getslid()
    		+ ")"
    		;
		try{
		    Statement stmt = conn.createStatement();
		    stmt.executeUpdate(SQL);
		}catch (Exception ex) {
			clsDatabaseFunctions.rollback_data_transaction(conn);
			throw new Exception("Error [1462304117] - Could not delete device user permission for device ID '" + getslid()  
				+ "' with SQL: " + SQL + " - " + ex.getMessage());
		}
    	
    	SQL = "DELETE FROM " + SMTablessdevices.TableName
    		+ " WHERE ("
    			+ SMTablessdevices.lid + " = " + getslid()
    		+ ")"
    		;
		try{
		    Statement stmt = conn.createStatement();
		    stmt.executeUpdate(SQL);
		}catch (Exception ex) {
			clsDatabaseFunctions.rollback_data_transaction(conn);
			throw new Exception("Error [1458914866] - Could not delete " + ParamObjectName + getsinputcontrollerid() 
				+ " with SQL: " + SQL + " - " + ex.getMessage());
		}
		
		if (!clsDatabaseFunctions.commit_data_transaction(conn)){
			clsDatabaseFunctions.rollback_data_transaction(conn);
			throw new Exception("Error [1458914867] - Could not commit data transaction while deleting " + ParamObjectName + ".");
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
        if (m_sdescription.length() > SMTablessdevices.sdescriptionlength){
        	sErrors += "Description cannot be more than " + Integer.toString(SMTablessdevices.sdescriptionlength) + " characters.  ";
        }
        if (m_sdescription.compareToIgnoreCase("") == 0){
        	sErrors += "Description cannot be blank.  ";
        }
        m_sinputterminal = m_sinputterminal.trim();
        if (m_sinputterminal.length() > SMTablessdevices.sinputterminalnumberlength){
        	sErrors += "Input terminal cannot be more than " + Integer.toString(SMTablessdevices.sinputterminalnumberlength) + " characters.  ";
        }
        m_soutputterminal = m_soutputterminal.trim();
        if (m_soutputterminal.length() > SMTablessdevices.soutputterminalnumberlength){
        	sErrors += "Output terminal cannot be more than " + Integer.toString(SMTablessdevices.soutputterminalnumberlength) + " characters.  ";
        }
        if ((m_sinputterminal.compareTo("") == 0) && (m_soutputterminal.compareTo("") == 0)){
        	sErrors += "The device must have at least one input or output terminal.  ";
        }
        //Check if terminal number is a valid integer:
        @SuppressWarnings("unused")
		int iTerminal = 0;
        if (m_sinputterminal.compareTo("") != 0){
	        try {
				iTerminal = Integer.parseInt(m_sinputterminal);
			} catch (Exception e) {
				sErrors += "Input terminal '" + m_sinputterminal + "' is not a valid integer value.  ";
			}
        }
        if(m_soutputterminal.compareTo("") != 0){
	        try {
				iTerminal = Integer.parseInt(m_soutputterminal);
			} catch (Exception e) {
				sErrors += "Output terminal '" + m_sinputterminal + "' is not a valid integer value.  ";
			}
        }
        m_sinputcontrollerid = m_sinputcontrollerid.trim();
        if (m_sinputcontrollerid.compareToIgnoreCase("") == 0){
        	m_sinputcontrollerid = "-1";
        }
        m_soutputcontrollerid = m_soutputcontrollerid.trim();
        if (m_soutputcontrollerid.compareToIgnoreCase("") == 0){
        	m_soutputcontrollerid = "-1";
        }

        @SuppressWarnings("unused")
		long lControllerID = 0;
        try {
			lControllerID = Long.parseLong(m_sinputcontrollerid);
		} catch (Exception e1) {
			sErrors += "Input controller ID is not valid.  ";
		}
        try {
			lControllerID = Long.parseLong(m_soutputcontrollerid);
		} catch (Exception e1) {
			sErrors += "Output controller ID is not valid.  ";
		}

        SSController con = new SSController();
        if (m_sinputcontrollerid.compareTo("-1") != 0){
	        con.setslid(m_sinputcontrollerid);
	        try {
				con.load(conn);
			} catch (Exception e) {
				sErrors += "Input Controller ID is not valid.  ";
			}
        }
        if (m_soutputcontrollerid.compareTo("-1") != 0){
	        con.setslid(m_soutputcontrollerid);
	        try {
				con.load(conn);
			} catch (Exception e) {
				sErrors += "Output Controller ID is not valid.  ";
			}
        }

        //If there's an input terminal number, there has to be a controller for it:
        if (m_sinputterminal.compareToIgnoreCase("") != 0){
        	if (m_sinputcontrollerid.compareToIgnoreCase("-1") == 0){
        		sErrors += "If you've entered an input terminal, it must have an input controller.  ";
        	}
        }
        if (m_sinputcontrollerid.compareToIgnoreCase("-1") != 0){
        	if (m_sinputterminal.compareToIgnoreCase("") == 0){
        		sErrors += "If you've entered an input controller, it must have an input terminal.  ";
        	}
        }
        

        //If there's an output terminal number, there has to be a controller for it:
        if (m_soutputterminal.compareToIgnoreCase("") != 0){
        	if (m_soutputcontrollerid.compareToIgnoreCase("-1") == 0){
        		sErrors += "If you've entered an output terminal, it must have an output controller.  ";
        	}
        }
        if (m_soutputcontrollerid.compareToIgnoreCase("-1") != 0){
        	if (m_soutputterminal.compareToIgnoreCase("") == 0){
        		sErrors += "If you've entered an output controller, it must have an output terminal.  ";
        	}
        }

        //If we are adding a new device, make sure that there's not already a record with this input terminal and controllerid:
        if ((m_slid.compareToIgnoreCase("-1") == 0) && (m_sinputterminal.compareToIgnoreCase("") != 0)){
	        String SQL = "SELECT"
	        	+ " " + SMTablessdevices.lid
	        	+ " FROM " + SMTablessdevices.TableName
	        	+ " WHERE ("
	        		+ "(" + SMTablessdevices.linputcontrollerid + " = " + m_sinputcontrollerid + ")"
	        		+ " AND (" + SMTablessdevices.sinputterminalnumber + " = " + m_sinputterminal + ")"
	        	+ ")"
	        ;
	        try {
				ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
				if (rs.next()){
					sErrors += "This INPUT terminal assignment is already taken for another device on this controller.  Use a different terminal.  ";
				}
			} catch (Exception e) {
				throw new Exception("Error [1458934468] checking for previously used input terminal with SQL: " + SQL + " - " + e.getMessage());
			}
        }

        //If we are adding a new device, make sure that there's not already a record with this output terminal and controllerid:
        if ((m_slid.compareToIgnoreCase("-1") == 0) && (m_soutputterminal.compareToIgnoreCase("") != 0)){
	        String SQL = "SELECT"
	        	+ " " + SMTablessdevices.lid
	        	+ " FROM " + SMTablessdevices.TableName
	        	+ " WHERE ("
	        		+ "(" + SMTablessdevices.loutputcontrollerid + " = " + m_soutputcontrollerid + ")"
	        		+ " AND (" + SMTablessdevices.soutputterminalnumber + " = '" + m_soutputterminal + "')"
	        	+ ")"
	        ;
	        try {
				ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
				if (rs.next()){
					sErrors += "This OUTPUT terminal assignment is already taken for another device on this controller.  Use a different terminal.  ";
				}
			} catch (Exception e) {
				throw new Exception("Error [1458934568] checking for previously used input terminal with SQL: " + SQL + " - " + e.getMessage());
			}
        }

        //Device type:
        m_sdevicetype = m_sdevicetype.trim();
        if (m_sdevicetype.compareToIgnoreCase("") == 0){
        	m_sdevicetype = "-1";
        }
        if (
        	(m_sdevicetype.compareToIgnoreCase(Integer.toString(SMTablessdevices.DEVICE_TYPE_DOOR)) != 0)
        	&& (m_sdevicetype.compareToIgnoreCase(Integer.toString(SMTablessdevices.DEVICE_TYPE_MOTIONSENSOR)) != 0)
        	&& (m_sdevicetype.compareToIgnoreCase(Integer.toString(SMTablessdevices.DEVICE_TYPE_ONOFF)) != 0)
        	&& (m_sdevicetype.compareToIgnoreCase(Integer.toString(SMTablessdevices.DEVICE_TYPE_SMOKEDETECTOR)) != 0)
        ){
        	sErrors += "Device type '" + m_sdevicetype + " is invalid.  ";
        }
        
        //Activation type:
        m_sactivationtype = m_sactivationtype.trim();
        if (m_sactivationtype.compareToIgnoreCase("") == 0){
        	m_sactivationtype = Integer.toString(SMTablessdevices.DEVICE_ACTIVATION_TYPE_MOMENTARY_CONTACT);
        }
        if (
        	(m_sactivationtype.compareToIgnoreCase(Integer.toString(SMTablessdevices.DEVICE_ACTIVATION_TYPE_CONSTANT_CONTACT)) != 0)
        	&& (m_sactivationtype.compareToIgnoreCase(Integer.toString(SMTablessdevices.DEVICE_ACTIVATION_TYPE_MOMENTARY_CONTACT)) != 0)
        ){
        	sErrors += "Activation type '" + m_sactivationtype + " is invalid.  ";
        }
        
        //Check to see if contact/activation duration is a positive integer
		int iDuration = 0;
        if (m_soutputterminal.compareTo("") != 0){
	        try {
	        	iDuration = Integer.parseInt(m_scontactduration);
			} catch (Exception e) {
				sErrors += "Contact duration '" + m_scontactduration + "' is not a valid integer value.  ";
			}
	        if(iDuration < 0){
	        	sErrors += "Contact duration '" + m_scontactduration + "' can not be negative'";
	        }
	        if(iDuration > Long.MAX_VALUE){
	        	sErrors += "Contact duration '" + m_scontactduration + "' is too large'";
	        }
        }
        //Input type:
        //If there's no valid input type, set it to the default, which is normally closed.  If there's no input terminal,
        //then it doesn't matter....
        m_sinputtype = m_sinputtype.trim();
        if (m_sinputtype.compareToIgnoreCase("") == 0){
        	m_sinputtype = Integer.toString(SMTablessdevices.DEVICE_INPUT_TYPE_NORMALLY_CLOSED);
        }
        if (
        	(m_sinputtype.compareToIgnoreCase(Integer.toString(SMTablessdevices.DEVICE_INPUT_TYPE_NORMALLY_CLOSED)) != 0)
        	&& (m_sinputtype.compareToIgnoreCase(Integer.toString(SMTablessdevices.DEVICE_INPUT_TYPE_NORMALLY_OPEN)) != 0)
        ){
        	sErrors += "Input type '" + m_sactivationtype + " is invalid.  ";
        }
        
        if (sErrors.compareToIgnoreCase("") != 0){
        	throw new Exception(sErrors);
        }
    }

    public void setOutputContactsState (
    		Connection conn, 
    		String sSetContactState, 
    		String sDurationInMS, 
    		String sUser, 
    		ServletContext context, 
    		String sServerID) throws Exception{
    	
       	if (getsoutputcontrollerid().compareToIgnoreCase("") == 0){
    		throw new Exception("Error [1459275087] - could not read output controller ID");
    	}
       	//If the device is active on a schedule and the user setting this output contact is not the Scheduler exit.
       	if(getIsActivatedOnSchedule(conn) && (sUser != SMEventScheduleHandler.SCHEDULE_MANAGER_USER)){
       		throw new Exception("This device is currently being controlled by a schedule. ");
       	}
           	
    	SSController controller = new SSController();
    	controller.setslid(getsoutputcontrollerid());
    	try {
			controller.load(conn);
		} catch (Exception e) {
			throw new Exception("Error [1459275088] - could not load output controller with ID '" + getsinputcontrollerid());
		}
    	
    	ArrayList<String>arrTerminalNumbers = new ArrayList<String>(0);
    	ArrayList<String>arrActivationStates = new ArrayList<String>(0);
    	ArrayList<String>arrDurationsInMS = new ArrayList<String>(0);
    	arrTerminalNumbers.add(getsoutputterminalnumber());
    	arrActivationStates.add(sSetContactState);
    	//If there's no duration, then it's a momentary contact and we just use the default activate interval:
    	if (sDurationInMS.compareToIgnoreCase("") == 0){
    		arrDurationsInMS.add(SSConstants.DEFAULT_MOMENTARY_CONTACT_ACTIVATE_INTERVAL_IN_MS);
    	}else{
    		arrDurationsInMS.add(sDurationInMS);
    	}
    	
    	String sActivateCommand = SSUtilities.buildRequestFromServerToSetTerminalContacts(
    		arrTerminalNumbers, 
    		arrActivationStates, 
    		arrDurationsInMS, 
    		controller.getscontrollername(), 
    		controller.getspasscode(),
    		sUser,
    		SMUtilities.getUserFullName(sUser, conn),
    		sServerID);
    	
    	if (bOutputDiagnostics){
    		System.out.println("[1459276541] - request string being sent to output controller: " + sActivateCommand);
    	}
    	
		try {
			ASClientService cs = new ASClientService();
			cs.sendRequest(
				controller.getscontrollerurl(),
				Integer.parseInt(controller.getslisteningport()),
				sActivateCommand,
				bOutputDiagnostics)
			;
		} catch (Exception e) {
			throw new Exception ("Error [1459275825] - request to activate device ID " + this.getslid() 
					+ " was not successfully sent.");
		}
		
    	if (bOutputDiagnostics){
    		System.out.println("[1459276542] - returning from client service: " + sActivateCommand);
    	}
		return;

    }
    public void setOutPutContactsState(
    	String sDBID, 
    	String sUser, 
    	ServletContext context, 
    	String sSetContactState,
    	String sActivationDuration,
    	String sServerID) throws Exception{

    	Connection conn = clsDatabaseFunctions.getConnectionWithException(
    		context, 
    		sDBID, 
    		"MySQL", 
    		this.toString() + ".activate - user: " + sUser);
    	
    	try {
			setOutputContactsState(conn, sSetContactState, sActivationDuration, sUser, context, sServerID);
		} catch (Exception e1) {
			clsDatabaseFunctions.freeConnection(context, conn, "[1547067650]");
			throw new Exception(e1.getMessage());
		}
    	clsDatabaseFunctions.freeConnection(context, conn, "[1547067651]");
    	return;
    	
     }
    
	public String getslid() {
		return m_slid;
	}

	public void setslid(String slid) {
		m_slid = slid;
	}

	public String getsinputcontrollerid() {
		return m_sinputcontrollerid;
	}

	public void setsinputcontrollerid(String sinputcontrollerid) {
		m_sinputcontrollerid = sinputcontrollerid;
	}

	public String getsoutputcontrollerid() {
		return m_soutputcontrollerid;
	}

	public void setsoutputcontrollerid(String soutputcontrollerid) {
		m_soutputcontrollerid = soutputcontrollerid;
	}

	public String getsdescription() {
		return m_sdescription;
	}

	public void setsdescription(String sdescription) {
		m_sdescription = sdescription;
	}

	public String getsinputterminalnumber() {
		return m_sinputterminal;
	}

	public void setsinputterminalnumber(String sinputterminalnumber) {
		m_sinputterminal = sinputterminalnumber;
	}

	public String getsoutputterminalnumber() {
		return m_soutputterminal;
	}

	public void setsoutputterminalnumber(String soutputterminalnumber) {
		m_soutputterminal = soutputterminalnumber;
	}

	public String getsdeviccetype() {
		return m_sdevicetype;
	}

	public void setsdevicetype(String sdevicetype) {
		m_sdevicetype = sdevicetype;
	}

	public String getsactivationtype() {
		return m_sactivationtype;
	}

	public void setsactivationtype(String sactivationtype) {
		m_sactivationtype = sactivationtype;
	}

	public String getsinputtype() {
		return m_sinputtype;
	}

	public void setsinputtype(String sinputtype) {
		m_sinputtype = sinputtype;
	}
	
	public String getsremarks(){
		return m_sremarks;
	}
	
	public void setsremarks(String sRemarks){
		m_sremarks = sRemarks;
	}
	
	public String getsactive(){
		return m_sactive;
	}
	
	public void setsactive(String sactive){
		m_sactive = sactive;
	}
	
	public String getscontactduration() {
		return m_scontactduration;
	}
	
	public void setscontactduration(String scontactduration) {
    	m_scontactduration = scontactduration;
	}

	//This is never written to the database, it is ALWAYS set on the fly:
	public void setinputcontactstatus(String sContactStatus){
		m_sinputcontactstatus = sContactStatus;
	}
	public String getinputcontactstatus(){
		return m_sinputcontactstatus;
	}
	public void setinputlisteningstatus(String sListeningStatus){
		m_sinputlisteningstatus = sListeningStatus;
	}
	public String getinputlisteningstatus(){
		return m_sinputlisteningstatus;
	}

	
	//This is never written to the database, it is ALWAYS set on the fly:
	public void setoutputcontactstatus(String sContactStatus){
		m_soutputcontactstatus = sContactStatus;
	}
	public String getoutputcontactstatus(){
		return m_soutputcontactstatus;
	}

	public boolean getIsActivated() throws Exception{
		if (getsinputterminalnumber().compareToIgnoreCase("") != 0){
			if (getsinputtype().compareToIgnoreCase(Integer.toString(SMTablessdevices.DEVICE_INPUT_TYPE_NORMALLY_CLOSED)) == 0){
				if (getinputcontactstatus().compareToIgnoreCase(SSConstants.QUERY_KEYVALUE_TERMINAL_CONTACT_STATUS_OPEN) == 0){
					return true;
				}else if (getinputcontactstatus().compareToIgnoreCase(SSConstants.QUERY_KEYVALUE_TERMINAL_CONTACT_STATUS_CLOSED) == 0){
					return false;
				}else{
					throw new Exception("Cannot determine input contact status");
				}
			}else{
				if (getinputcontactstatus().compareToIgnoreCase(SSConstants.QUERY_KEYVALUE_TERMINAL_CONTACT_STATUS_CLOSED) == 0){
					return true;
				}else if (getinputcontactstatus().compareToIgnoreCase(SSConstants.QUERY_KEYVALUE_TERMINAL_CONTACT_STATUS_OPEN) == 0){
					return false;
				}else{
					throw new Exception("Cannot determine input contact status");
				}
			}
		}else{
			//If we are only checking the output terminals, the device is active if the contacts are closed:
			if (getoutputcontactstatus().compareToIgnoreCase(SSConstants.QUERY_KEYVALUE_TERMINAL_CONTACT_STATUS_CLOSED) == 0){
				return true;
			}else if (getoutputcontactstatus().compareToIgnoreCase(SSConstants.QUERY_KEYVALUE_TERMINAL_CONTACT_STATUS_OPEN) == 0){
				return false;
			}else{
				throw new Exception("Cannot determine output contact status");
			}
		}
	}
	
	public boolean getIsActivatedOnSchedule(Connection conn) throws Exception{
		//Do not let a user (other than the scheduler) set an output state of a device if it is on an active schedule
       	String SQL = "SELECT " + SMTablesseventscheduledetails.lid
       			+ " FROM " + SMTablesseventscheduledetails.TableName
       			+ " WHERE ("
       			+ "(" + SMTablesseventscheduledetails.ldeviceorsequenceid + "=" + getslid() + ")"
       			+ " AND "
       			+ "(" + SMTablesseventscheduledetails.ideviceorsequence + "=" + SMTablesseventscheduledetails.DEVICEORSEQUENCE_DEVICE + ")"
       			+ " AND "
       			+ " (" + SMTablesseventscheduledetails.iactivated + "=1" + ")"
       			+ " )";
       	ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
       	if(rs.next() ){
           	rs.close();
       		return true;
       	}else{
           	rs.close();	
    		return false;
       	}

	}
	public String getCommandLabelForActivatingAndDeactivating(){
		if (getsinputterminalnumber().compareToIgnoreCase("") != 0){
			return SMTablessdevices.getDeviceActivateCommandLabel(
				getsdeviccetype(), 
				getinputcontactstatus(), 
				getsinputtype()
			);
		}else{
			//If we have to, we check the output terminal status to see if it's active or not:
			return SMTablessdevices.getDeviceActivateCommandLabel(
				getsdeviccetype(), 
				getoutputcontactstatus(), 
				Integer.toString(SMTablessdevices.DEVICE_INPUT_TYPE_NORMALLY_OPEN)
			);
		}
	}
	
	public SSController getinputcontroller(Connection conn) throws Exception{

		SSController controller = new SSController();
		if (Long.parseLong(getsinputcontrollerid()) > 0){
			controller.setslid(getsinputcontrollerid());
			try {
				controller.load(conn);
			} catch (Exception e) {
				throw new Exception("Error [1461099077] - could not load controller with ID '" 
						+ getsinputcontrollerid() + "' - " + e.getMessage());
			}
		}
		return controller;
	}
	public SSController getoutputcontroller(Connection conn) throws Exception{
		
		SSController controller = new SSController();
		if (Long.parseLong(getsoutputcontrollerid()) > 0){
			controller.setslid(getsoutputcontrollerid());
			try {
				controller.load(conn);
			} catch (Exception e) {
				throw new Exception("Error [1461099078] - could not load output controller with ID '" 
					+ getsoutputcontrollerid() + "' - " + e.getMessage());
			}
		}
		
		return controller;
	}
	public String getObjectName(){
		return ParamObjectName;
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
    private void initRecordVariables(){
    	m_slid = "-1";
    	m_sinputcontrollerid = "-1";
    	m_soutputcontrollerid = "-1";
    	m_sdescription = "";
    	m_sinputterminal = "-1";
    	m_soutputterminal = "-1";
    	m_sdevicetype = "-1";
    	m_sactivationtype = "-1";
    	m_sinputtype = Integer.toString(SMTablessdevices.DEVICE_INPUT_TYPE_NORMALLY_CLOSED);
    	m_sinputcontactstatus = "";
    	m_soutputcontactstatus = "";
    	m_sinputlisteningstatus = "";
    	m_sremarks = "";
    	m_sactive = "1";
    	m_scontactduration = "0";
	}
}
