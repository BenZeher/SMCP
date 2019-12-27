package smas;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.servlet.ServletContext;

import SMClasses.SMLogEntry;
import SMDataDefinition.SMTablessdeviceevents;
import SMDataDefinition.SMTablessuserevents;
import SMDataDefinition.SMTableusers;
import ServletUtilities.clsDatabaseFunctions;

public class ASUserEventLogEntry {

	private Connection conn;
	private String m_sDBID;
    public ASUserEventLogEntry(Connection cn)
    {
    	conn = cn;
    }
    public ASUserEventLogEntry(String sDBID)
    {
    	m_sDBID = sDBID;
    }

    public void writeEntry (
    		int iEventType,
    		String sUserID,
    		String sUserLatitude,
    		String sUserLongitude,
    		String sDeviceID,
    		String sDeviceDes,
    		String sComment,
    		String sReferenceId,
    		String sDateTime,
    		String sAlarmID,
    		ServletContext context,
    		String sDiagMarker
   		) throws Exception{

    	if (conn == null){
    		conn = clsDatabaseFunctions.getConnectionWithException(
    			context, 
    			m_sDBID, 
    			"MySQL", 
    			this.toString() + ".writeEntry - user: " + sUserID
    		);
    	}
    	
    	String sUserFirstName = "";
    	String sUserLastName = "";
    	//Get the user full name:
    	String SQL = "SELECT"
    		+ " " + SMTableusers.sUserFirstName
    		+ ", " + SMTableusers.sUserLastName
    		+ " FROM " + SMTableusers.TableName
    		+ " WHERE ("
    			+ "(" + SMTableusers.lid + " = " + sUserID + ")"
    		+ ")"
    	;
    	try {
			ResultSet rsUsers = clsDatabaseFunctions.openResultSet(SQL, conn);
			if (rsUsers.next()){
				sUserFirstName = rsUsers.getString(SMTableusers.sUserFirstName);
				sUserLastName = rsUsers.getString(SMTableusers.sUserLastName);
			}
			rsUsers.close();
		} catch (Exception e1) {
			clsDatabaseFunctions.freeConnection(context, conn, "[1547067610]");
			throw new Exception("Error [1459296622] reading users for user event log - " + e1.getMessage());
		}
    	
    	String sDeviceDescription = "";
    	//If we are arming or disarming a zone, there won't be a particular device referenced:
    	if (iEventType == SMTablessuserevents.DEVICE_ACTIVATED){
	    	//Get the device:
	    	SSDevice device = new SSDevice();
	    	device.setslid(sDeviceID);
	    	try {
				device.load(conn);
			} catch (Exception e2) {
				clsDatabaseFunctions.freeConnection(context, conn, "[1547067611]");
				throw new Exception("Error [1459296623] reading device for user event log - " + e2.getMessage());
			}
	    	sDeviceDescription = device.getsdescription();
    	}
    	
    	String sAlarmName = "";
    	String sAlarmDescription = "";
    	//Get the alarm info:
    	//TODO
    	
    	String sEventTypeLabel = SMTablessuserevents.getUserEventTypeLabel(iEventType);
    	if (sEventTypeLabel.length() > SMTablessuserevents.seventtypelabellength){
    		sEventTypeLabel = sEventTypeLabel.substring(0, SMTablessuserevents.seventtypelabellength - 1);
    	}
    	if (sUserID.length() > SMTablessuserevents.luseridlength){
    		sUserID = sUserID.substring(0, SMTablessuserevents.luseridlength - 1);
    	}
    	if (sUserLatitude.length() > SMTablessuserevents.suserlatitudelength){
    		sUserLatitude = sUserLatitude.substring(0, SMTablessuserevents.suserlatitudelength - 1);
    	}
    	if (sUserLongitude.length() > SMTablessuserevents.suserlongitudelength){
    		sUserLongitude = sUserLongitude.substring(0, SMTablessuserevents.suserlongitudelength - 1);
    	}

    	if (sComment.length() > SMTablessdeviceevents.scommentlength){
    		sComment = sComment.substring(0, SMTablessdeviceevents.scommentlength - 1);
    	}
    	
    	String sEventDateTime = sDateTime;
    	if (sDateTime == null){
    		sEventDateTime = "NOW()";
    	}
		SQL = "INSERT INTO " + SMTablessuserevents.TableName
			+ " ("
    			+ SMTablessuserevents.dattimeoccurrence
    			+ ", " + SMTablessuserevents.ieventtype
    			+ ", " + SMTablessuserevents.ldeviceid
    			+ ", " + SMTablessuserevents.lalarmid
    			+ ", " + SMTablessuserevents.scomment
    			+ ", " + SMTablessuserevents.sdevicedescription
    			+ ", " + SMTablessuserevents.seventtypelabel
    			+ ", " + SMTablessuserevents.sreferenceid
    			+ ", " + SMTablessuserevents.luserid
    			+ ", " + SMTablessuserevents.suserfullname
    			+ ", " + SMTablessuserevents.suserlatitude
    			+ ", " + SMTablessuserevents.suserlongitude
    			+ ", " + SMTablessuserevents.salarmdescription
    			+ ", " + SMTablessuserevents.salarmname
			+ ") VALUES ("
				+ " " + sEventDateTime
				+ ", " + Integer.toString(iEventType)
				+ ", " + sDeviceID
				+ ", " + sAlarmID
				+ ", '" + clsDatabaseFunctions.FormatSQLStatement(sComment) + "'"
				+ ", '" + clsDatabaseFunctions.FormatSQLStatement(sDeviceDescription) + "'"
				+ ", '" + clsDatabaseFunctions.FormatSQLStatement(sEventTypeLabel) + "'"
				+ ", '" + clsDatabaseFunctions.FormatSQLStatement(sReferenceId) + "'"
				+ ", " + clsDatabaseFunctions.FormatSQLStatement(sUserID) + ""
				+ ", '" + clsDatabaseFunctions.FormatSQLStatement(sUserFirstName.trim() + " " + sUserLastName.trim()) + "'"
				+ ", '" + clsDatabaseFunctions.FormatSQLStatement(sUserLatitude) + "'"
				+ ", '" + clsDatabaseFunctions.FormatSQLStatement(sUserLongitude) + "'"
				+ ", '" + clsDatabaseFunctions.FormatSQLStatement(sAlarmDescription) + "'"
				+ ", '" + clsDatabaseFunctions.FormatSQLStatement(sAlarmName) + "'"
			+ ")"
			;
		
		//Here we log it in the systemlog as well to ensure we know who access what
		Date date = new Date();
	    String strDateFormat = "hh:mm:ss a";
	    DateFormat dateFormat = new SimpleDateFormat(strDateFormat);
	    String formattedDate= dateFormat.format(date);
	    
		SMClasses.SMLogEntry log = new SMClasses.SMLogEntry(conn);
	    log.writeEntry(sUserID, SMLogEntry.LOG_OPERATION_SSUSEREVENT, sUserFirstName + " " + sUserLastName + " accessed the device " + sDeviceID + ":" + sDeviceDes + " at " + formattedDate  , "ASUserEventLogEntry", "[1577471378]");
		
		try {
			Statement stmt = conn.createStatement();
			stmt.execute(SQL);
		} catch (Exception e) {
			clsDatabaseFunctions.freeConnection(context, conn, "[1547067612]");
			throw new Exception("Error [1459297199] writing user event log  - " + e.getMessage() + ".");
		}
		free_AS_Connection(context, conn, sDiagMarker);
		return;
    }
    
    private boolean free_AS_Connection(ServletContext context, Connection conn, String sMarker){
    	
    	return clsDatabaseFunctions.freeConnection(context, conn, "[1547067613]" + " - " + sMarker);
    }
}
