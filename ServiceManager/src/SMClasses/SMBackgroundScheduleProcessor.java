package SMClasses;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import javax.servlet.ServletContext;

import ConnectionPool.CompanyDataCredentials;
import ConnectionPool.ServerSettingsFileParameters;
import SMDataDefinition.SMModuleListing;
import ServletUtilities.clsDateAndTimeConversions;
import smas.SMEventScheduleHandler;
import smcontrolpanel.SMAppointmentNotificationHandler;
import smcontrolpanel.SMUtilities;

public class SMBackgroundScheduleProcessor extends java.lang.Object{

	public static final int MINUTE_FREQUENCY = 0;
	public static final int HOUR_FREQUENCY = 1;
	public static final int DAY_FREQUENCY = 2;
	public static final int QUARTERHOUR_FREQUENCY = 4;
			
	
	public SMBackgroundScheduleProcessor(
		int iFrequency,
		String sControlDatabaseURL,
		String sControlDatabaseName,
		String sControlDatabasePort,
		String sControlDatabaseUser,
		String sControlDatabaseUserPW,
		ServletContext context
	        ) throws Exception{
		//Test:
		//System.out.println(SMUtilities.nowStdFormatWithSeconds() + " - " 
		//	+ WebContextParameters.controldatabasename + " = " + sControlDatabaseName
		//	+ " - frequency = " + Integer.toString(iFrequency)
		//);
		
		//SMUtilities.sysprint(this.toString(), "SCHEDULE_USER", "[1483717970] in SMBackgroundScheduleProcessor " 
		//+ " - Control DB URL = '" + sControlDatabaseURL + "'"
		//+ ", Control DB name = '" + sControlDatabaseName + "'"
		//+ ", Control DB port = '" + sControlDatabasePort + "'"
		//);
		
		int iDiagnosticLoggingLevel = 0;
		
		try {
			ServerSettingsFileParameters readFile = new ServerSettingsFileParameters(SMUtilities.getFullPathToResourceFolder(context) + ServerSettingsFileParameters.SERVER_SETTINGS_FILENAME);
			if (readFile.readKeyValue(ServerSettingsFileParameters.SERVER_SETTING_DEBUG_LEVEL).compareToIgnoreCase("") != 0){
				iDiagnosticLoggingLevel = Integer.parseInt(readFile.readKeyValue(ServerSettingsFileParameters.SERVER_SETTING_DEBUG_LEVEL));
			}
		}catch (Exception e9) {
			System.out.println("Error [1516307712] getting diagnostic logging level - " + e9.getMessage());
		}
		
		if(iDiagnosticLoggingLevel >= 1){
			System.out.println("[1516307927] " + clsDateAndTimeConversions.nowStdFormatWithSeconds() + " - in SMBackgroundScheduleProcessor, getting connection.");
		}
		
		//Get a connection:
		Connection conn = null;
		String sConnectionString = 
			"jdbc:mysql://" 
				+ sControlDatabaseURL 
				+ ":" + sControlDatabasePort + "/" 
				+ sControlDatabaseName 
				+ "?noDatetimeStringSync=true&connectTimeout=28800000&interactiveClient=True"
		;
		try {
			conn = DriverManager.getConnection(
				sConnectionString, 
				sControlDatabaseUser, 
				sControlDatabaseUserPW);
		}catch (Exception E) { 
			try{
				Class.forName("com.mysql.jdbc.Driver").newInstance();
				conn = DriverManager.getConnection(
						sConnectionString, 
						sControlDatabaseUser, 
						sControlDatabaseUserPW);
			}catch(Exception F){
				throw new Exception("Error [1483714533] - Could not get connection with connection string '" 
					+ sConnectionString + "' - " + F.getMessage());
			}
		}
		
		if(iDiagnosticLoggingLevel >= 1){
			System.out.println("[1516307928] " + clsDateAndTimeConversions.nowStdFormatWithSeconds() + " - in SMBackgroundScheduleProcessor, got connection.");
		}
		
		try {
			if(iFrequency == MINUTE_FREQUENCY){
				processMinuteFrequency(conn, context, iDiagnosticLoggingLevel);
			}
			if(iFrequency == QUARTERHOUR_FREQUENCY){
				//Create function for hourly frequency
			}
			if(iFrequency == HOUR_FREQUENCY){
				//Create function for hourly frequency
			}
			if(iFrequency == DAY_FREQUENCY){
				//Create function for day frequency
			}
			
		} catch (Exception e) {
			conn.close();
			throw new Exception("Error [1483714534] - " + e.getMessage());
		}
		conn.close();
	}
	
	private void processMinuteFrequency(
			Connection conn,
			ServletContext context,
			int iDiagnosticLoggingLevel
		) throws Exception{
		
		//System.out.println("[1483719020] - " + String.format(
	    //        "getCatalog() returns: %s", 
	    //        conn.getCatalog()));
		
		if(iDiagnosticLoggingLevel >= 1){
			System.out.println("[1516307929] " + clsDateAndTimeConversions.nowStdFormatWithSeconds() + " - in processMinuteFrequency, starting out...");
		}
		
		//Try each database and see if there's anything to be run:
		String SQL = "SELECT * FROM " + CompanyDataCredentials.TableName;
		try {
			PreparedStatement statement = conn.prepareStatement(SQL);
			ResultSet rs = statement.executeQuery();
			while (rs.next()){
				CompanyDataCredentials cdc = new CompanyDataCredentials();
				try {
					cdc.load(
						rs.getString(CompanyDataCredentials.sdatabaseid),
						conn		
					);
				} catch (Exception e1) {
					throw new Exception("Error [1483722571] loading company data credentials - " + e1.getMessage());
				}
				
				try {
					processMinuteFrequencyForEachCompany(rs.getString(CompanyDataCredentials.sdatabaseid), context, iDiagnosticLoggingLevel);
				} catch (Exception e) {
					throw new Exception("Error [1483714536] in " 
							+ SMUtilities.getFullClassName(this.toString())
							+ ".processMinuteFrequency - "
							+ e.getMessage());
				}
			}
			rs.close();
		} catch (Exception e) {
			throw new Exception("Error [1483714535] - "
				+ e.getMessage());
		}
	}
	
	private void processMinuteFrequencyForEachCompany(String sDatabaseID, ServletContext context, int iDiagnosticLoggingLevel) throws Exception{
		//Returns false and prints a system out if a connection can't be made to the database, but
		//it doesn't throw an error.  This is to allow for the possibility that there are nonexistent
		//databases listed in the companydatacredentials.  This is possible if we removed databases
		//and didn't remove their entries in companydatacredentials.
		
		
		if(iDiagnosticLoggingLevel >= 1){
			System.out.println("[1516307930] " + clsDateAndTimeConversions.nowStdFormatWithSeconds() + " - in processMinuteFrequencyForEachCompany, sDatabaseID = '" + sDatabaseID + "'.");
		}
		
		//Get the companies license module number to make sure we don't run processes for modules they do not have:
		String sLicenseModuleLevel = "";
		try {
			sLicenseModuleLevel = SMUtilities.getSMCPModuleLevel(context, sDatabaseID);
		} catch (Exception e) {
			//No need to report - this just means we didn't even find a valid license for this company and we'll just exit quietly:
			//System.out.println("Error [1483981868] - License file cannot be found - " + e.getMessage() + " for DB ID '" + sDatabaseID + "'.");
			return;
		}
		
		long lLicenseModuleLevel = 0;
		try {
			lLicenseModuleLevel = Long.parseLong(sLicenseModuleLevel);
		} catch (NumberFormatException e1) {
			//Don't catch anything here....
		}

		if(iDiagnosticLoggingLevel >= 1){
			System.out.println("[1516307931] " + clsDateAndTimeConversions.nowStdFormatWithSeconds() + " - in processMinuteFrequencyForEachCompany, sLicenseModuleLevel = '" + sLicenseModuleLevel + "'.");
		}
		
		//Run Alarm System Schedule
		boolean bRunSchedule = processINIFileKeyValuePair(context, 
				ServerSettingsFileParameters.SERVER_SETTING_RUN_SCHEDULER, 
				ServerSettingsFileParameters.SERVER_SETTING_RUN_SCHEDULER_TRUE)
				;
		
		if(iDiagnosticLoggingLevel >= 1){
			System.out.println("[1516307932] " + clsDateAndTimeConversions.nowStdFormatWithSeconds() + " - in processMinuteFrequencyForEachCompany, bRunSchedule = " + bRunSchedule + ".");
		}
		
		if (((lLicenseModuleLevel & SMModuleListing.MODULE_ALARMSYSTEM) > 0) && bRunSchedule){
			SMEventScheduleHandler handler = new SMEventScheduleHandler();
			try {
				String sServerID = getServerID(context);
				String sTestDeviceID = "";
				try {
					ServerSettingsFileParameters readFile = new ServerSettingsFileParameters(SMUtilities.getFullPathToResourceFolder(context) + ServerSettingsFileParameters.SERVER_SETTINGS_FILENAME);
					sTestDeviceID = readFile.readKeyValue(ServerSettingsFileParameters.SERVER_SETTING_TEST_DEVICE_ID);
				}catch (Exception e9) {
					//Has empty test device if file cannot be read.
				}
				if(iDiagnosticLoggingLevel >= 1){
					System.out.println("[1516307933] " + clsDateAndTimeConversions.nowStdFormatWithSeconds() + " - in processMinuteFrequencyForEachCompany, sServerID = '" + sServerID + "'.");
				}
				handler.processSchedules(context, sDatabaseID, sServerID, sTestDeviceID, iDiagnosticLoggingLevel);
			} catch (Exception e) {
				throw new Exception("Error [1483737648] - " + e.getMessage());
			}
		}
		
		//Run Appointment Notifications
		boolean bRunAppointmentCalendarReminders = processINIFileKeyValuePair(context, 
				ServerSettingsFileParameters.SERVER_SETTING_RUN_APPOINTMENT_NOTIFICATIONS, 
				ServerSettingsFileParameters.SERVER_SETTING_RUN_APPOINTMENT_NOTIFICATIONS_TRUE)
				;
		if(((lLicenseModuleLevel & SMModuleListing.MODULE_BASE) > 0) && bRunAppointmentCalendarReminders){
			SMAppointmentNotificationHandler handler = new SMAppointmentNotificationHandler();
			try {
				handler.processAppointmentReminders(context, sDatabaseID);
			} catch (Exception e) {
				throw new Exception("Error [1505421823] - " + e.getMessage());
			}		
		}
		
	}
	
	private boolean processINIFileKeyValuePair(
			ServletContext context,
			String sKey,
			String sValue
		) throws Exception{
		
		boolean bRunProcess = false;
		ServerSettingsFileParameters readFile = new ServerSettingsFileParameters(SMUtilities.getFullPathToResourceFolder(context) + ServerSettingsFileParameters.SERVER_SETTINGS_FILENAME);
		String sKeyValue = "";
		try {
			sKeyValue = readFile.readKeyValue(sKey);
		}catch (Exception e9) {
			bRunProcess = false;
			//System.out.println(e9.getMessage());
		}
		
		if(sKeyValue.compareToIgnoreCase(sValue) == 0){
			bRunProcess = true;
		}
		
		return bRunProcess;
	}
	
	public static String getServerID(ServletContext context) {
		
		String sServerID = "UNKNOWN";
		try {
			//SMReadINIFile readFile = new SMReadINIFile(sFullPathToResourcesFolder + SMUtilities.CUSTOM_SETTINGS_FILENAME);
			ServerSettingsFileParameters readFile = new ServerSettingsFileParameters(SMUtilities.getFullPathToResourceFolder(context) + ServerSettingsFileParameters.SERVER_SETTINGS_FILENAME);
			sServerID = readFile.readKeyValue(ServerSettingsFileParameters.SERVER_SETTING_SERVER_ID);
		}catch (Exception e9) {
			//Us the UNKNOWN server name if the file can not be read or does not exist
		}
		return sServerID;
	}
}
