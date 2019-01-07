package ServletUtilities;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.PrintWriter;
import java.util.ArrayList;


public class clsReadWriteINIFile {

	public final static String CONFFILE_KEY_VALUE_DELIMITER = "=";
	private static String m_sFullFilePath = "";
	
    public clsReadWriteINIFile(String sFullFilePath)
    {
    	m_sFullFilePath = sFullFilePath;
    	
    }

    public static String readFileKeyValue (String sKey) throws Exception {
    	String s = "";
		//Read custom settings file on server
		try {
			
			File file = new File(m_sFullFilePath);
			if(file.createNewFile()) {
				writeFileDefaultValues(file);
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
    
    public static void writeFileKeyValues (ArrayList<String> arrKeys, ArrayList<String> arrKeyValues, boolean bAppend) throws Exception {
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
    
    public static void writeFileDefaultValues(File file)
    {
    	PrintWriter fileWriter = null;
    	try {
    	fileWriter = new PrintWriter(file);  
    	fileWriter.println("New file was created at " + System.currentTimeMillis());
    	fileWriter.close();
    	}catch(Exception e) {
    		
    	}  	
    }
   
}
