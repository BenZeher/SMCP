package smcontrolpanel;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

import ConnectionPool.ServerSettingsFileParameters;
import SMClasses.SMBackgroundScheduleProcessor;
import ServletUtilities.clsDateAndTimeConversions;

@WebListener
public class SMBackgroundJobManager implements ServletContextListener {

	private ScheduledExecutorService scheduler;

	private static String sControlDatabaseURL = "";
	private static String sControlDatabaseName = "";
	private static String sControlDatabasePort = "";
	private static String sControlDatabaseUser = "";
	private static String sControlDatabasePw = "";
	private static ServletContext sc = null;
	
	public static final String SCHEDULING_USER = "SYSTEM SCHEDULING";
	public static final String SCHEDULING_USERID = "0";
	public static final int POLLING_FREQUENCY_IN_MINUTES = 1;

	@Override
	public void contextInitialized(ServletContextEvent event) {
		//System.out.println("[1482261718] started listener");
		scheduler = Executors.newSingleThreadScheduledExecutor();
		scheduler.scheduleAtFixedRate(new runEveryMinute(), 0, POLLING_FREQUENCY_IN_MINUTES, TimeUnit.MINUTES);
		scheduler.scheduleAtFixedRate(new runEveryHour(), 0, 1, TimeUnit.HOURS);
		//scheduler.scheduleAtFixedRate(new runEveryQuarterHour(), 0, 15, TimeUnit.MINUTES);

		//Now get the database info:
		sc = event.getServletContext();
		//String user_name = sc.getInitParameter("user_name");
		try {
			
		ServerSettingsFileParameters serverSettingsFile = new ServerSettingsFileParameters(ServerSettingsFileParameters.getFullPathToResourceFolder(sc) + ServerSettingsFileParameters.SERVER_SETTINGS_FILENAME);

		sControlDatabaseURL = serverSettingsFile.readKeyValue(ServerSettingsFileParameters.SERVER_SETTING_CONTROL_DB_URL);
		sControlDatabaseName = serverSettingsFile.readKeyValue(ServerSettingsFileParameters.SERVER_SETTING_CONTROL_DB_NAME);
		sControlDatabasePort = serverSettingsFile.readKeyValue(ServerSettingsFileParameters.SERVER_SETTING_CONTROL_DB_PORT);
		sControlDatabaseUser = serverSettingsFile.readKeyValue(ServerSettingsFileParameters.SERVER_SETTING_CONTROL_DB_USERNAME);
		sControlDatabasePw = serverSettingsFile.readKeyValue(ServerSettingsFileParameters.SERVER_SETTING_CONTROL_DB_PASSWORD);
		} catch (Exception e) {
			System.out.println("[1540910904] - Error reading control database settings from server settings file ");
		}
		System.out.println("[1483717973] SMCP Background Manager context initialized: " + clsDateAndTimeConversions.nowStdFormatWithSeconds() 
			+ " - Control DB URL = '" + sControlDatabaseURL + "'"
			+ ", Control DB name = '" + sControlDatabaseName + "'"
			+ ", Control DB port = '" + sControlDatabasePort + "'"
			
		);
	}

	@Override
	public void contextDestroyed(ServletContextEvent event) {
		scheduler.shutdownNow();
	}

	public class runEveryMinute implements Runnable {

		@Override
		public void run() {
			// Run every minute:
			//System.out.println("[1483717969] SMCP Background Manager reporting time: " + SMUtilities.nowStdFormatWithSeconds() 
			//+ " - Control DB URL = " + sControlDatabaseURL
			//+ ", Control DB name = '" + sControlDatabaseName + "'"
			//);
			
			//SMUtilities.sysprint(this.toString(), "SCHEDULE_USER", "[1483717969] in runEveryMinute " 
			//+ " - Control DB URL = '" + sControlDatabaseURL + "'"
			//+ ", Control DB name = '" + sControlDatabaseName + "'"
			//+ ", Control DB port = '" + sControlDatabasePort + "'"
			//);
			
			@SuppressWarnings("unused")
			SMBackgroundScheduleProcessor proc = null;
			try {
				proc = new SMBackgroundScheduleProcessor(
						SMBackgroundScheduleProcessor.MINUTE_FREQUENCY,
						sControlDatabaseURL,
						sControlDatabaseName,
						sControlDatabasePort,
						sControlDatabaseUser,
						sControlDatabasePw,
						sc
						);
			} catch (Exception e) {
				System.out.println("[1483715802] Error in runEveryMinute - " + e.getMessage() + ", "
					+ "sControlDatabaseURL = '" + sControlDatabaseURL + "', "
					+ "sControlDatabaseName = '" + sControlDatabaseName + "', "
					+ "sControlDatabasePort = '" + sControlDatabasePort + "', "
					+ "sControlDatabaseUser = '" + sControlDatabaseUser + "'."
				);
				
			}
			proc = null;
		}
	}
	
	public class runEveryHour implements Runnable {

		@Override
		public void run() {
			// Run every hour:
			System.out.println("[1483717979]SMCP Background Manager HOURLY time check: " + clsDateAndTimeConversions.nowStdFormatWithSeconds() 
			+ " - Control DB URL = " + sControlDatabaseURL);

		}
	}
}