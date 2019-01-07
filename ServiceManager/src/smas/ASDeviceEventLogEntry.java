package smas;

import java.sql.Connection;
import java.sql.Statement;

import javax.servlet.ServletContext;

import SMDataDefinition.SMTablessdeviceevents;
import ServletUtilities.clsDatabaseFunctions;
import smcontrolpanel.SMUtilities;

public class ASDeviceEventLogEntry {

	private Connection conn;
	private ServletContext context;
	private String sConf;
    public ASDeviceEventLogEntry(Connection cn)
    {
    	conn = cn;
    }
    public ASDeviceEventLogEntry(String sDBID, ServletContext cont)
    {
    	context = cont;
    	sConf = sDBID;
    }

    public void writeEntry (
    		int iTerminalType,
    		int iEventType,
    		long lDeviceID,
    		String sDeviceDescription,
    		long lControllerID,
    		String sControllerName,
    		String sControllerDescription,
    		long lZoneID,
    		String sZoneName,
    		String sZoneDescription,
    		String sTerminalNumber,
    		String sComment,
    		String sReferenceId,
    		String sDateTime
   		) throws Exception{
    	
    	if (sDeviceDescription.length() > SMTablessdeviceevents.sdevicedescriptionlength){
    		sDeviceDescription = sDeviceDescription.substring(0, SMTablessdeviceevents.sdevicedescriptionlength - 1);
    	}
    	if (sControllerName.length() > SMTablessdeviceevents.scontrollernamelength){
    		sControllerName = sControllerName.substring(0, SMTablessdeviceevents.scontrollernamelength - 1);
    	}
    	if (sControllerDescription.length() > SMTablessdeviceevents.scontrollerdescriptionlength){
    		sControllerDescription = sControllerDescription.substring(0, SMTablessdeviceevents.scontrollerdescriptionlength - 1);
    	}
    	if (sZoneName.length() > SMTablessdeviceevents.szonenamelength){
    		sZoneName = sZoneName.substring(0, SMTablessdeviceevents.szonenamelength - 1);
    	}
    	if (sZoneDescription.length() > SMTablessdeviceevents.szonedescriptionlength){
    		sZoneDescription = sZoneDescription.substring(0, SMTablessdeviceevents.szonedescriptionlength - 1);
    	}
    	if (sComment.length() > SMTablessdeviceevents.scommentlength){
    		sComment = sComment.substring(0, SMTablessdeviceevents.scommentlength - 1);
    	}
    	
    	String sEventDateTime = sDateTime;
    	if (sDateTime == null){
    		sEventDateTime = "NOW()";
    	}
		String SQL = "INSERT INTO " + SMTablessdeviceevents.TableName
			+ " ("
    			+ SMTablessdeviceevents.dattimeoccurrence
    			+ ", " + SMTablessdeviceevents.ieventtype
    			+ ", " + SMTablessdeviceevents.iterminaltype
    			+ ", " + SMTablessdeviceevents.lcontrollerid
    			+ ", " + SMTablessdeviceevents.ldeviceid
    			+ ", " + SMTablessdeviceevents.lzoneid
    			+ ", " + SMTablessdeviceevents.scomment
    			+ ", " + SMTablessdeviceevents.scontrollerdescription
    			+ ", " + SMTablessdeviceevents.scontrollername
    			+ ", " + SMTablessdeviceevents.sdevicedescription
    			+ ", " + SMTablessdeviceevents.sterminalnumber
    			+ ", " + SMTablessdeviceevents.sreferenceid
    			+ ", " + SMTablessdeviceevents.szonedescription
    			+ ", " + SMTablessdeviceevents.szonename
			+ ") VALUES ("
				+ " " + sEventDateTime
				+ ", " + Integer.toString(iEventType)
				+ ", " + Integer.toString(iTerminalType)
				+ ", " + Long.toString(lControllerID)
				+ ", " + Long.toString(lDeviceID)
				+ ", " + Long.toString(lZoneID)
				+ ", '" + clsDatabaseFunctions.FormatSQLStatement(sComment) + "'"
				+ ", '" + clsDatabaseFunctions.FormatSQLStatement(sControllerDescription) + "'"
				+ ", '" + clsDatabaseFunctions.FormatSQLStatement(sControllerName) + "'"
				+ ", '" + clsDatabaseFunctions.FormatSQLStatement(sDeviceDescription) + "'"
				+ ", '" + clsDatabaseFunctions.FormatSQLStatement(sTerminalNumber) + "'"
				+ ", '" + clsDatabaseFunctions.FormatSQLStatement(sReferenceId) + "'"
				+ ", '" + clsDatabaseFunctions.FormatSQLStatement(sZoneDescription) + "'"
				+ ", '" + clsDatabaseFunctions.FormatSQLStatement(sZoneName) + "'"
			+ ")"
			;
		
		if (conn != null){
			try {
				Statement stmt = conn.createStatement();
				stmt.execute(SQL);
			} catch (Exception e) {
				throw new Exception("Error [1459211320] writing device event log  - " + e.getMessage() + ".");
			}

		}else{
			try {
				clsDatabaseFunctions.executeSQL(SQL, context, sConf, "MySQL", SMUtilities.getFullClassName(this.toString()));
			} catch (Exception e) {
				throw new Exception("Error [1459211321] writing device event log  - " + e.getMessage() + ".");
			}
		}
    }
}
