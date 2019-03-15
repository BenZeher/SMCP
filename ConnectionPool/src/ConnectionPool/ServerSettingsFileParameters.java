package ConnectionPool;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.PrintWriter;
import java.util.ArrayList;

import javax.servlet.ServletContext;

import ConnectionPool.WebContextParameters;



public class ServerSettingsFileParameters {

	public static final String SERVER_SETTINGS_FILENAME = "serversettings.ini";
	//Control database server settings file constants
	public static String SERVER_SETTING_SERVER_HOST_NAME = "CONTROL_SERVER_HOST_NAME";
	public static String SERVER_SETTING_CONTROL_DB_NAME = "CONTROL_DB_NAME";
	public static String SERVER_SETTING_CONTROL_DB_URL = "CONTROL_DB_URL";
	public static String SERVER_SETTING_CONTROL_DB_PORT = "CONTROL_DB_PORT";
	public static String SERVER_SETTING_CONTROL_DB_USERNAME = "CONTROL_DB_USERNAME";
	public static String SERVER_SETTING_CONTROL_DB_PASSWORD = "CONTROL_DB_PASSWORD";
	//Alarm System server settings file constant
	public static String SERVER_SETTING_RUN_SCHEDULER = "AS_RUN_SCHEDULER";
	public static String SERVER_SETTING_RUN_SCHEDULER_TRUE = "1";
	public static String SERVER_SETTING_SERVER_ID = "AS_SERVER_ID";
	public static String SERVER_SETTING_TEST_DEVICE_ID = "AS_TEST_DEVICE_ID";
	public static String SERVER_SETTING_DEBUG_LEVEL = "SERVER_DEBUGGING_LEVEL";
	//SM Other settings
	public static String SERVER_SETTING_RUN_APPOINTMENT_NOTIFICATIONS = "SM_RUN_APPOINTMENT_NOTIFICATIONS";
	public static String SERVER_SETTING_RUN_APPOINTMENT_NOTIFICATIONS_TRUE = "1";
	public static String SERVER_SETTING_INITIATING_URL = "INITIATING_URL";
	
	
	public final static String CONFFILE_KEY_VALUE_DELIMITER = "=";
	private static String m_sFullFilePath = "";
	
    public ServerSettingsFileParameters(String sFullFilePath)
    {
    	m_sFullFilePath = sFullFilePath;
    	
    }

    public String readKeyValue (String sKey) throws Exception {
    	String s = "";
		//Read custom settings file on server
		try {
			
			File file = new File(m_sFullFilePath);
			if(file.createNewFile()) {
				writeDefaultValues(file);
			}
			FileReader fileReader = new FileReader(file);
			BufferedReader bufferedReader = new BufferedReader(fileReader);
			String line;
			while ((line = bufferedReader.readLine()) != null) {
				
				String[] sKeyValue = line.split(CONFFILE_KEY_VALUE_DELIMITER);
				
				if (sKeyValue[0].trim().compareToIgnoreCase(sKey) == 0){
					if(sKeyValue.length < 2) {
						s = "";
					}else {
						s = sKeyValue[1].trim().replace(";", "");
					}
				}
			}
			fileReader.close();
		} catch (Exception e) {
			throw new Exception("Error [1493390486] reading config file '" + m_sFullFilePath + "' - " + e.getMessage());
		}
    	
    	return s;
    }
    
    public void writeKeyValues (ArrayList<String> arrKeys, ArrayList<String> arrKeyValues, boolean bAppend) throws Exception {
    	PrintWriter fileWriter = null;
    	try {
    	fileWriter = new PrintWriter(new FileOutputStream(m_sFullFilePath, bAppend));  
    	fileWriter.print("");
    	for(int i = 0; arrKeys.size() > i; i++) {
			fileWriter.println(arrKeys.get(i) + CONFFILE_KEY_VALUE_DELIMITER + arrKeyValues.get(i));
		}
    	} catch(Exception e){
    		fileWriter.close();
    		throw new Exception("Error [1540224943] reading config file '" + m_sFullFilePath + "' - " + e.getMessage());	
    	}
    	fileWriter.close();
    	
    }
    
    public void writeDefaultValues(File file)
    {
    	PrintWriter fileWriter = null;
    	try {
    	fileWriter = new PrintWriter(file);  
    	fileWriter.println(SERVER_SETTING_SERVER_HOST_NAME + CONFFILE_KEY_VALUE_DELIMITER + "localhost");
    	fileWriter.println(SERVER_SETTING_CONTROL_DB_NAME + CONFFILE_KEY_VALUE_DELIMITER + "smcpcontrols");
    	fileWriter.println(SERVER_SETTING_CONTROL_DB_URL + CONFFILE_KEY_VALUE_DELIMITER + "localhost");
    	fileWriter.println(SERVER_SETTING_CONTROL_DB_PORT + CONFFILE_KEY_VALUE_DELIMITER + "3306");
    	fileWriter.println(SERVER_SETTING_CONTROL_DB_USERNAME + CONFFILE_KEY_VALUE_DELIMITER + "smadmin");
    	fileWriter.println(SERVER_SETTING_CONTROL_DB_PASSWORD + CONFFILE_KEY_VALUE_DELIMITER + "atoSMCP3322");
    	fileWriter.println(SERVER_SETTING_SERVER_ID + CONFFILE_KEY_VALUE_DELIMITER + "SERVER1");
    	fileWriter.println(SERVER_SETTING_RUN_SCHEDULER + CONFFILE_KEY_VALUE_DELIMITER + "0");
    	fileWriter.println(SERVER_SETTING_TEST_DEVICE_ID + CONFFILE_KEY_VALUE_DELIMITER + "0");
    	fileWriter.println(SERVER_SETTING_DEBUG_LEVEL + CONFFILE_KEY_VALUE_DELIMITER + "0");
    	fileWriter.println(SERVER_SETTING_RUN_APPOINTMENT_NOTIFICATIONS + CONFFILE_KEY_VALUE_DELIMITER + "0");
    	fileWriter.println(SERVER_SETTING_INITIATING_URL + CONFFILE_KEY_VALUE_DELIMITER + "");
    	fileWriter.close();
    	}catch(Exception e) {
    		
    	}
    	
    }
    
	public static String getFullPathToResourceFolder(ServletContext context) throws Exception{
		
		//Get catalina base
		String sFullFilePath = System.getProperty( "catalina.base" );
		
		//Add webapps folder to the path
		sFullFilePath += System.getProperty("file.separator") + "webapps";
		
		//Add local resources folder to the path
		sFullFilePath = sFullFilePath + System.getProperty("file.separator");
		//System.out.println("[1552416111] sFullFilePath = " + sFullFilePath);
		if (WebContextParameters.getLocalResourcesPath(context).startsWith(System.getProperty("file.separator"))){
			sFullFilePath += WebContextParameters.getLocalResourcesPath(context).substring(1);
		}else{
			sFullFilePath += WebContextParameters.getLocalResourcesPath(context);
		}
		//End with file separator
		while (sFullFilePath.endsWith(System.getProperty("file.separator"))){
			sFullFilePath = sFullFilePath.substring(0, sFullFilePath.length() - 1);
		}
		sFullFilePath += System.getProperty("file.separator");
		
		//return full path to resource folder (e.g. '/var/lib/tomcat7/webapps/smlocalresources/')
		return sFullFilePath;
	}
}