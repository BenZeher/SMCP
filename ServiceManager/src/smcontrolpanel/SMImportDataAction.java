package smcontrolpanel;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

import SMClasses.SMLogEntry;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsDateAndTimeConversions;
import ServletUtilities.clsServletUtilities;

public class SMImportDataAction extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final String IMPORT_FILE_SIGNATURE = "smcpSQLImport";
	private static final int MAX_FILE_SIZE = 4196;
	private static final int MAX_MEM_SIZE = 2000000;
	private static final String TEMP_TABLE = "TEMPTABLE";
	private static String sDBID = "";
	private static String sUserID = "";
	private String filePath = "";
	private String sCallingClass = "";
	private String m_sExecuteString = "";
	
	public void doPost(HttpServletRequest request,
				HttpServletResponse response)
				throws ServletException, IOException {

	    response.setContentType("text/html");
	    
	    //Get the session info:
	    HttpSession CurrentSession = request.getSession(true);
	    sDBID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_DATABASE_ID);
	    sUserID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERID);
	    if (!SMAuthenticate.authenticateSMCPCredentials(request, response, getServletContext(), SMSystemFunctions.SMImportData)){
	    	return;
	    }
	    
	    //Calling Class
	    sCallingClass = "smcontrolpanel.SMImportDataSelect";
	    
	    //The DatabaseID
	    String DATABASEID = (String)CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_DATABASE_ID);
	    
	    
	    //The maxMemSize and FileSize 
	    int maxFileSize = MAX_FILE_SIZE;
	    long maxMemSize = MAX_MEM_SIZE;
	    int iNumberOfImportedRecords = -1;
	    
	    //Gets the file path and executes the process
	    String sLoadCommand = "LOAD DATA LOCAL INFILE '"+filePath+"{FILE}'";
	    try{
	    	filePath = SMUtilities.getAbsoluteRootPath(request, getServletContext())+ System.getProperty("file.separator")+ "smimportdata";
	    	iNumberOfImportedRecords = process(filePath,sLoadCommand,CurrentSession,request,maxFileSize,maxMemSize,DATABASEID);
		
			//Log the execution:
			SMClasses.SMLogEntry log = new SMClasses.SMLogEntry(sDBID, getServletContext());
			log.writeEntry(
					sUserID, 
					SMLogEntry.LOG_OPERATION_IMPORTDATA, 
					"Import with SQL SUCCEEDED",
					"Command: " + sLoadCommand + "\n" + m_sExecuteString,
					"[1376509322]");
			
	    }catch(Exception e){
	    	SMClasses.SMLogEntry log = new SMClasses.SMLogEntry(sDBID, getServletContext());
			log.writeEntry(
					sUserID, 
					SMLogEntry.LOG_OPERATION_IMPORTDATA, 
					"Import with SQL FAILED",
					"Command: " + sLoadCommand + "\n" + m_sExecuteString,
					"[1376509323]");
	    	response.sendRedirect(
					"" + SMUtilities.getURLLinkBase(getServletContext()) + "" + sCallingClass
					+ "?Warning=" + clsServletUtilities.URLEncode(e.getMessage())
	   	    		+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
				);
				return;
					
	    }
	    
	    response.sendRedirect(
				"" + SMUtilities.getURLLinkBase(getServletContext()) + "" + sCallingClass
				+ "?Status=" +""+iNumberOfImportedRecords+" records have been imported into the SMCP table."
   	    		+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
			);
	}
	
	/**
	 * process method
	 * runs the createTempImportFileFolder and writeFileExecuteCommandAndDeleteFile methods
	 * @param filePath
	 * @param sLoadCommand
	 * @param CurrentSession
	 * @param request
	 * @param maxFileSize
	 * @param maxMemSize
	 * @param sDatabaseID
	 * @throws Exception
	 */
	private int  process (String filePath,
						  String loadCommand,
						  HttpSession CurrentSession, 
						  HttpServletRequest request,
						  int maxFileSize,
						  long maxMemSize,
						  String sDatabaseID
						  ) throws Exception {
		
		int iNumberOfRecordsImported = -1;
		try{
			//Create Temp Folder
			createTempImportFileFolder(filePath);
		}catch(Exception e1){
			throw new Exception (e1.getMessage());
		}
		
		try{
			//Write the file, Execute the SQL Command and Delete the File After Executing the command
			iNumberOfRecordsImported = writeFileExecuteCommandAndDeleteFile(filePath,loadCommand,CurrentSession,request,maxFileSize,maxMemSize,sDatabaseID);
		}catch(Exception e2){
			throw new Exception(e2.getMessage());
		}
	    try {
			deleteCurrentTempImportFiles(filePath);		
		} catch (Exception e3) {
				throw new Exception(e3.getMessage());
		}
	    return iNumberOfRecordsImported;
		
	}
	
	/**
	 * createTemImportFileFolder method
	 * @param sTempFileFolder
	 * @throws Exception
	 */
	private void createTempImportFileFolder(String sTempFileFolder) throws Exception{
	    File dir = new File(sTempFileFolder);
	    //If the directory exists do nothing 
	    if (dir.exists()) {
	      return;
	    }
	    
	    //Need to create the path:
	    try{
	        // Create one directory
	        if (!new File(sTempFileFolder).mkdir()) {
	        	throw new Exception("Error [1535397364] creating temp upload folder.");
	        }    
        }catch (Exception e){//Catch exception if any
        	throw new Exception("Error [1535397375] creating temp upload folder - " + e.getMessage() + ".");
	    }
	}
	
	@SuppressWarnings("unchecked")
	private int writeFileExecuteCommandAndDeleteFile(
			String filePath,
			String loadCommand,
			HttpSession ses, 
			HttpServletRequest req,
			int maxFileSize,
			long maxMemSize,
			String DATABASEID
			
	) throws Exception{
		
		//Make sure to initialize the global variable:
		m_sExecuteString = "";
		
		ArrayList<SQLWarning> warning = new ArrayList<SQLWarning>();
		//Number of Records Imported
		int iNumberOfRecordsImported = -1;
		
		boolean update = false;
		boolean insert_update = false;
		boolean insert = false;
		boolean bIncludeHeader = false;
		
	    DiskFileItemFactory factory = new DiskFileItemFactory();
	    factory.setSizeThreshold(maxFileSize);
	    factory.setRepository(new File(filePath));
	    ServletFileUpload upload = new ServletFileUpload(factory);
	    upload.setSizeMax(maxMemSize);
	    List<FileItem> fileItems = null;
		try {
			fileItems = upload.parseRequest(req);
		} catch (FileUploadException e1) {
				System.out.println("[1535481746] In " + this.toString() + " error on upload.parseRequest: " 
					+ e1.getMessage());
			throw new Exception("Error [1535375153] on upload.parseRequest: " + e1.getMessage());
		}
	    	Iterator<FileItem> iFileItemIterator = fileItems.iterator();
		    String m_sExecuteString = "";
		    String fileName = IMPORT_FILE_SIGNATURE + clsDateAndTimeConversions.now("yyyyMMdd_HHmmss") + ".csv";
	    	while(iFileItemIterator.hasNext()){
	    		FileItem item = iFileItemIterator.next();
	    		if(!item.isFormField()){
	    			if("file".equals(item.getFieldName())){
		    			if(item.getName() == null || item.getName() == ""){
		    				throw new Exception("Error [1534187716] CSV File Not uploaded");
		    			}else if (!item.getContentType().equals("text/csv")){
		    				throw new Exception("Error [1534870366] Import file has to be in CSV Format");
		    			}else{
		    				FileItem fi = item;
			    			try{
			    				fi.write(new File(filePath,fileName));
			    			}catch(Exception e){
			    				throw new Exception ("ERROR [1533908623] CANNOT WRITE FILE "+e.getMessage());
		    			}
	    			}
	    		}
	    		}else{
	    			String fieldName = item.getFieldName();
	    			String fieldNameValue = item.getString();
	    			System.out.println("Field Name - "+item.getFieldName()+" Value - "+item.getString());
	    			if(fieldName.equals("EXECUTESTRING")){
	    				if(fieldNameValue.equals(""))
	    					throw new Exception("ERROR [1535374271] SQL COMMAND IS BLANK ");
	    				else
	    					m_sExecuteString = fieldNameValue;//Gets the SQL Command
	    			}else if(fieldName.equals("operation")){
	    				if(fieldNameValue.equals("INSERT/UPDATE"))
	    						insert_update = true;
	    				else if(fieldNameValue.equals("INSERT"))
	    						insert = true;
	    				else if(fieldNameValue.equals("UPDATE"))
	    						update = true;
	    			}else if (fieldName.equals("header")){
	    				if(fieldNameValue.equals("yes"))
	    					bIncludeHeader = true;
	    				else if (fieldNameValue.equals("no"))
	    					bIncludeHeader = false;
	    			}
	    		}
	    			
	    	}
	    	//int iRows = 0;
	    	//int iColumns = 0;
	    	//File file = new File (fileName);
	    	try{
	    	ArrayList<String []> fileData = readFile(fileName);
//	    	String readCharacter = "Edwin'";
//	    	readCharacter = addSingleQoute(readCharacter);
//	    	for(int i = 0; i < readCharacter.length(); i++){
//	    		int value = readCharacter.charAt(i);
//	    		System.out.println(readCharacter.charAt(i)+" the value is "+value);
//	    	}
    		String sTableName = getTableName(m_sExecuteString);
    		ArrayList<String> sTableFields = tableFields(m_sExecuteString,sDBID,DATABASEID,sUserID);
    		checkTemporaryTableExist (sDBID, sUserID);
    		createTemporaryTable(warning,sDBID,sTableName,DATABASEID,sUserID);
    		ArrayList<String> aPrimaryandUniqueKey = getPrimaryAndUniqueKey(sDBID,sTableName);
    		ArrayList<String> primaryKeys = getPrimaryKeys(sDBID,sTableName);
    		addRecordsfromArray(bIncludeHeader,fileData,aPrimaryandUniqueKey,sTableFields,sDBID,sUserID);
	    	if(insert_update == true){
	    		iNumberOfRecordsImported = insertFromTemporaryTablewithUpdate (warning, aPrimaryandUniqueKey, sDBID, sTableName, sTableFields,DATABASEID, sUserID);
	    	}else if (insert == true){
	    		iNumberOfRecordsImported = insertFromTemporaryTableWithOutUpdate (warning,sDBID, sTableName, sTableFields,DATABASEID, sUserID);
	    	}else if (update == true){
	    		iNumberOfRecordsImported = updateFromTemporaryTable (warning,aPrimaryandUniqueKey,primaryKeys,sDBID,sTableName, sTableFields,DATABASEID, sUserID);
	    	}
	    	removeTemporaryTable(warning,sDBID,DATABASEID,  sUserID);
	    	String sWarning = "";
	    	if(!warning.isEmpty()){
	    		for(int i = 0; i < warning.size(); i++){
	    			sWarning += " "+warning.get(i).getMessage();
	    		}
	    		throw new Exception(sWarning);
	    	}
				} catch (SQLException e) {
					throw new Exception("SQL command failed ERROR [1534444137] - "+ e.getMessage());
				}
	    	
			    return iNumberOfRecordsImported;
}
	
/*	private String addSingleQoute(String letter){
		System.out.println("The word is "+letter);
		StringBuilder builder = new StringBuilder();
		char singleQoute = 39;
		for(int i = 0; i < letter.length(); i++){
			if((i == 0) || (i == letter.length() - 1)){
				if(letter.charAt(i) != singleQoute){
					if(i == 0 ){
						builder.append("'");
						builder.append(letter.charAt(i));
					}else if (i == letter.length() - 1){
						builder.append(letter.charAt(i));
						builder.append("'");
					}
					
					
				}else{
					builder.append(letter.charAt(i));
				}
			}else{
				builder.append(letter.charAt(i));
			}
		}
		
		return builder.toString();
	}
	*/
	private ArrayList<String []> readFile (String fileName)throws Exception{
		BufferedReader scanner = new BufferedReader (new FileReader(filePath + System.getProperty("file.separator") + fileName));
		ArrayList<String []> al = new ArrayList<String[]>();
		String line = "";
	//	char doubleQoute = 34;
	//	char singleQoute = 39;
		try{
		while((line = scanner.readLine()) != null){
	//		int nullcount = 0;
		//	String a = line.trim().replace("\"", ""); 
			//System.out.println(a);
			String [] data = line.split(",", -1);
				for(int i = 0; i < data.length; i++){
					data[i] = data[i].trim().replace("\"", "");;
			}
			al.add(data);
				//System.out.println();
		}
		}catch(Exception e){
		//	throw new Exception("[1542033631] "+e.getMessage());
		}
		scanner.close();
		return al;
	}

	    
	private String getColumns (String sExecuteString){
		String s = sExecuteString.substring(sExecuteString.indexOf("(")+1);
		StringBuilder sb = new StringBuilder();
		for(int i = 0; i < s.length(); i++){
			if(s.charAt(i) == ')'){
				break;
			}else{
				sb.append(s.charAt(i));
			}
		}
		
		return sb.toString();
	}
	

	
	
/*	private void turnOffLocalInfile (String sDBID, String DATABASEID) throws Exception{
		String sSQL = "SET GLOBAL local_infile = 0";
		Connection conn = null;
		try {
			conn = clsDatabaseFunctions.getConnection(getServletContext(), sDBID, "MySQL", this.toString() + " SQL: " + sSQL);
			Statement stmt = conn.createStatement();
			stmt.execute(sSQL);
			clsDatabaseFunctions.freeConnection(getServletContext(), conn);	
		} catch (Exception e) {
			throw new Exception(e.getMessage());
		}
		
	
	}
*/	
	private String getTableName (String sExecuteString){
		String s = sExecuteString.substring(sExecuteString.indexOf("TABLE") + 6);
		StringBuilder sb = new StringBuilder();
		for(int i = 0; i < s.length(); i++){
			if(s.charAt(i) == ' ' || s.charAt(i) == '\n'){
				break;
			}else{
				sb.append(s.charAt(i));
			}
		}
		
		return sb.toString().replaceAll("\\s", "");
	}
	
	private void checkTemporaryTableExist (String sConf, String sUserID)throws SQLException{
		String sSQL = "DROP TABLE IF EXISTS "+TEMP_TABLE+" ";
		Connection conn = null;
		try {
			conn = clsDatabaseFunctions.getConnection(getServletContext(), sConf, "MySQL", this.toString() + " SQL: " + sSQL);
		} catch (Exception e) {
			throw new SQLException(e.getMessage());
		}
		try{
			Statement stmt = conn.createStatement();
			stmt.execute(sSQL);
			clsDatabaseFunctions.freeConnection(getServletContext(), conn);
		}catch(Exception e){
			SMClasses.SMLogEntry log = new SMClasses.SMLogEntry(sConf, getServletContext());
			log.writeEntry(
					sUserID, 
					SMLogEntry.LOG_OPERATION_SMEXECUTESQL, 
					"[1376503188] EXECUTE SQL FAILED - " + e.getMessage(),
					"Command: " + sSQL,
					"[1376509321]");
			if (conn != null){
				clsDatabaseFunctions.freeConnection(getServletContext(), conn);
			}
			throw new SQLException("ERROR [1538072982] "+e.getMessage());
		}
	}
	
	private void createTemporaryTable (ArrayList<SQLWarning> warning, String sConf, String sTableName, String DATABASEID, String sUserID)throws SQLException{
		String sSQL = "CREATE TABLE "+TEMP_TABLE+" LIKE "+sTableName;
		Connection conn = null;
		try {
			conn = clsDatabaseFunctions.getConnection(getServletContext(), sConf, "MySQL", this.toString() + " SQL: " + sSQL);
		} catch (Exception e) {
			throw new SQLException(e.getMessage());
		}
		try{
			Statement stmt = conn.createStatement();
			stmt.execute(sSQL);
			SQLWarning w = conn.getWarnings();
			if(w != null)
				warning.add(w);
			clsDatabaseFunctions.freeConnection(getServletContext(), conn);
		}catch(Exception e){
			SMClasses.SMLogEntry log = new SMClasses.SMLogEntry(sConf, getServletContext());
			log.writeEntry(
					sUserID, 
					SMLogEntry.LOG_OPERATION_SMEXECUTESQL, 
					"[1376503188] EXECUTE SQL FAILED - " + e.getMessage(),
					"Command: " + sSQL,
					"[1376509321]");
			if (conn != null){
				clsDatabaseFunctions.freeConnection(getServletContext(), conn);
			}
			throw new SQLException("ERROR [1538072982] "+e.getMessage());
		}
	}
	
		
	private void removeTemporaryTable (ArrayList<SQLWarning> warning, String sConf, String DATABASEID, String sUserID)throws SQLException{
		String sSQL = "DROP TABLE "+TEMP_TABLE+"";
		Connection conn = null;
		try {
			conn = clsDatabaseFunctions.getConnection(getServletContext(), sConf, "MySQL", this.toString() + " SQL: " + sSQL);
		} catch (Exception e) {
			throw new SQLException(e.getMessage());
		}
		try{
			Statement stmt = conn.createStatement();
			stmt.execute(sSQL);
			SQLWarning w = conn.getWarnings();
			if(w != null)
				warning.add(w);
			clsDatabaseFunctions.freeConnection(getServletContext(), conn);
		}catch(Exception e){
			SMClasses.SMLogEntry log = new SMClasses.SMLogEntry(sConf, getServletContext());
			log.writeEntry(
					sUserID, 
					SMLogEntry.LOG_OPERATION_SMEXECUTESQL, 
					"[1376503188] EXECUTE SQL FAILED - " + e.getMessage(),
					"Command: " + sSQL,
					"[1376509321]");
			if (conn != null){
				clsDatabaseFunctions.freeConnection(getServletContext(), conn);
			}
			throw new SQLException("ERROR [1538072982] "+e.getMessage());
		}
	}
	
	private ArrayList<String> tableFields(String sExecuteString, String sConf, String DATABASEID, String sUserID)throws Exception{
		String [] column = getColumns(sExecuteString).split(",");
		ArrayList <String> tableAndColumns = new ArrayList<String>();
		for(int i = 0; i < column.length; i++){
			tableAndColumns.add(column[i].replaceAll("\\s", ""));
		}
		return tableAndColumns;
		
	}
	
	private int insertFromTemporaryTableWithOutUpdate (ArrayList<SQLWarning> warning,String sConf, String sTableName, ArrayList<String> columns, String DATABASEID, String sUserID) throws SQLException{
		int numberOfRecords = 0;
		String sSQL = "INSERT IGNORE INTO "+sTableName+" (";
		for(int i = 0; i < columns.size(); i++){
			sSQL += columns.get(i)+"";
			if(i == columns.size() - 1)
				sSQL += "";
			else
				sSQL += ",";
		}
		sSQL += ") SELECT ";
		for(int i = 0; i < columns.size(); i++){
			sSQL += columns.get(i)+"";
			if(i == columns.size() - 1)
				sSQL += "";
			else
				sSQL += ",";
		}
		sSQL += " FROM "+TEMP_TABLE+"";
		System.out.println(sSQL);
		Connection conn = null;
		try {
			conn = clsDatabaseFunctions.getConnection(getServletContext(), sConf, "MySQL", this.toString() + " SQL: " + sSQL);
		} catch (Exception e) {
			throw new SQLException(e.getMessage());
		}
		try{
			Statement stmt = conn.createStatement();
			numberOfRecords = stmt.executeUpdate(sSQL);
			SQLWarning w = conn.getWarnings();
			if(w != null)
				warning.add(w);
			clsDatabaseFunctions.freeConnection(getServletContext(), conn);
		}catch(Exception e){
			SMClasses.SMLogEntry log = new SMClasses.SMLogEntry(sConf, getServletContext());
			log.writeEntry(
					sUserID, 
					SMLogEntry.LOG_OPERATION_SMEXECUTESQL, 
					"[1376503188] EXECUTE SQL FAILED - " + e.getMessage(),
					"Command: " + sSQL,
					"[1376509321]");
			if (conn != null){
				clsDatabaseFunctions.freeConnection(getServletContext(), conn);
			}
			throw new SQLException("ERROR [1538073042] "+e.getMessage());
		}
		return numberOfRecords;
	}
	
	private int updateFromTemporaryTable (ArrayList<SQLWarning> warning, ArrayList<String> sPrimaryUniqueKey, ArrayList<String> sPrimaryKey, String sConf, String sTableName, ArrayList<String> columns, String DATABASEID, String sUserID) throws SQLException{
		int numberOfRecords = 0;
		ArrayList<String> columnWithoutPrimaryAndUnique = columnsWithOutPrimaryAndUniqueKeys(sPrimaryKey,columns);
		String sSQL = "UPDATE "+sTableName+" INNER JOIN "+TEMP_TABLE+" ON ";
		for(int i = 0; i < sPrimaryKey.size(); i++){
			sSQL += sTableName+"."+sPrimaryKey.get(i)+" = "+TEMP_TABLE+"."+sPrimaryKey.get(i);
			if(i == sPrimaryKey.size() - 1)
				sSQL += "";
			else
				sSQL += " AND ";
		}
		sSQL += " SET ";
		for(int i = 0; i < columnWithoutPrimaryAndUnique.size(); i++){
			sSQL += sTableName+"."+columnWithoutPrimaryAndUnique.get(i)+" = "+TEMP_TABLE+"."+columnWithoutPrimaryAndUnique.get(i);
			if(i == columnWithoutPrimaryAndUnique.size() - 1)
				sSQL += "";
			else
				sSQL += ",";
		}
		sSQL += " WHERE ";
		for(int i = 0; i < sPrimaryKey.size(); i++){
			sSQL += sTableName+"."+sPrimaryKey.get(i)+" = "+TEMP_TABLE+"."+sPrimaryKey.get(i);
			if(i == sPrimaryKey.size() - 1)
				sSQL += "";
			else
				sSQL += ",";
		}
		System.out.println(sSQL);
		Connection conn = null;
		try {
			conn = clsDatabaseFunctions.getConnection(getServletContext(), sConf, "MySQL", this.toString() + " SQL: " + sSQL);
		} catch (Exception e) {
			throw new SQLException(e.getMessage());
		}
		try{
			Statement stmt = conn.createStatement();
			numberOfRecords = stmt.executeUpdate(sSQL);
			SQLWarning w = conn.getWarnings();
			if(w != null)
				warning.add(w);
			clsDatabaseFunctions.freeConnection(getServletContext(), conn);
		}catch(Exception e){
			SMClasses.SMLogEntry log = new SMClasses.SMLogEntry(sConf, getServletContext());
			log.writeEntry(
					sUserID, 
					SMLogEntry.LOG_OPERATION_SMEXECUTESQL, 
					"[1376503188] EXECUTE SQL FAILED - " + e.getMessage(),
					"Command: " + sSQL,
					"[1376509321]");
			if (conn != null){
				clsDatabaseFunctions.freeConnection(getServletContext(), conn);
			}
			throw new SQLException("ERROR [1538073042] "+e.getMessage());
		}
		return numberOfRecords;
	}
	
	private ArrayList<String> getPrimaryAndUniqueKey(String sConf, String sTableName) throws Exception{
		ArrayList<String> keys = new ArrayList<String>();
		String sPrimaryKeyField = "";
		String sSQL = "DESCRIBE "+sTableName+"";
		Connection conn = null;
		try {
			conn = clsDatabaseFunctions.getConnection(getServletContext(), sConf, "MySQL", this.toString() + " SQL: " + sSQL);
			ResultSet result = clsDatabaseFunctions.openResultSet(sSQL, conn);
			while(result.next()){
				if(result.getString("Key").equals("PRI") || result.getString("Key").equals("UNI")){
					sPrimaryKeyField = result.getString("Field");
					System.out.println(sPrimaryKeyField+" might be unique or primary");
					keys.add(sPrimaryKeyField);
				}
					
			}
		} catch (Exception e) {
			throw new Exception(e.getMessage());
		}
		
		return keys;
		
	}
	private ArrayList<String> getPrimaryKeys(String sConf, String sTableName) throws Exception{
		ArrayList<String> keys = new ArrayList<String>();
		String sPrimaryKeyField = "";
		String sSQL = "DESCRIBE "+sTableName+"";
		Connection conn = null;
		try {
			conn = clsDatabaseFunctions.getConnection(getServletContext(), sConf, "MySQL", this.toString() + " SQL: " + sSQL);
			ResultSet result = clsDatabaseFunctions.openResultSet(sSQL, conn);
			while(result.next()){
				if(result.getString("Key").equals("PRI")){
					sPrimaryKeyField = result.getString("Field");
					keys.add(sPrimaryKeyField);
				}
					
			}
		} catch (Exception e) {
			throw new Exception(e.getMessage());
		}
		
		return keys;
		
	}
	
	private ArrayList<String> columnsWithOutPrimaryAndUniqueKeys (ArrayList<String> sPrimaryKey, ArrayList<String> columns){
		ArrayList<String> newColumn = new ArrayList<String>();
		for(int i = 0; i < columns.size(); i++){
			boolean isPrimaryOrUnique = false;
			for(int j = 0; j < sPrimaryKey.size(); j++){
				if(columns.get(i).equals(sPrimaryKey.get(j)))
					isPrimaryOrUnique = true;
			}
			if(!isPrimaryOrUnique){
				newColumn.add(columns.get(i));
			}
		}
		return newColumn;
	}
	
	private String generateInsertFromTemporaryTableQuery(ArrayList<String> sPrimaryKey, ArrayList<String> columns, String sTableName){
		StringBuilder sb = new StringBuilder();
		sb.append("INSERT INTO "+sTableName+" (");
		for(int i = 0; i < columns.size(); i++){
			sb.append(columns.get(i)+"");
			if(i == columns.size() - 1)
				sb.append("");
			else
				sb.append(",");
		}
		sb.append(") SELECT ");
		for(int i = 0; i < columns.size(); i++){
			sb.append(columns.get(i)+"");
			if(i == columns.size() - 1)
				sb.append("");
			else
				sb.append(",");
		}
		sb.append(" FROM "+TEMP_TABLE+""
				  + " ON DUPLICATE KEY UPDATE ");
		
		ArrayList<String> columnWithoutPrimaryAndUnique = columnsWithOutPrimaryAndUniqueKeys (sPrimaryKey,columns);
		for(int i = 0; i < columnWithoutPrimaryAndUnique.size(); i++){
				sb.append(columnWithoutPrimaryAndUnique.get(i)+" = VALUES("+columnWithoutPrimaryAndUnique.get(i)+")");
				if(i == columnWithoutPrimaryAndUnique.size() - 1)
					sb.append(" ");
				else
					sb.append(", ");
			}
		return sb.toString();
	}
	
	private int insertFromTemporaryTablewithUpdate (ArrayList<SQLWarning> warning, ArrayList<String> sPrimaryKey, String sConf, String sTableName, ArrayList<String> columns, String DATABASEID, String sUserID) throws SQLException{
		int iNumberOfRecords = 0;
		String sSQL = "";
		System.out.println(sSQL);
		Connection conn = null;
		try {
			conn = clsDatabaseFunctions.getConnection(getServletContext(), sConf, "MySQL", this.toString() + " SQL: " + sSQL);
		} catch (Exception e) {
			throw new SQLException(e.getMessage());
		}
		try{
			sSQL = generateInsertFromTemporaryTableQuery(sPrimaryKey,columns, sTableName);
			Statement stmt = conn.createStatement();
			iNumberOfRecords = stmt.executeUpdate(sSQL);
			SQLWarning w = conn.getWarnings();
			if(w != null)
				warning.add(w);
			clsDatabaseFunctions.freeConnection(getServletContext(), conn);
		}catch(Exception e){
			SMClasses.SMLogEntry log = new SMClasses.SMLogEntry(sConf, getServletContext());
			log.writeEntry(
					sUserID, 
					SMLogEntry.LOG_OPERATION_SMEXECUTESQL, 
					"[1376503188] EXECUTE SQL FAILED - " + e.getMessage(),
					"Command: " + sSQL,
					"[1376509321]");
			if (conn != null){
				clsDatabaseFunctions.freeConnection(getServletContext(), conn);
			}
			throw new SQLException("ERROR [1538072905]"+e.getMessage());
		}
		return iNumberOfRecords;
	}
	
	private String addRecordsQuery(boolean bIncludeHeader,ArrayList<String []> array, ArrayList<String> sPrimaryKey, ArrayList<String> columns){
		int iStartPoint = 0;
		if(bIncludeHeader == true)
			iStartPoint = 0;
		else if (bIncludeHeader == false)
			iStartPoint = 1;
		String sSQL = "INSERT INTO "+TEMP_TABLE+" (";
		for(int i = 0; i < columns.size(); i++){
			sSQL += " "+columns.get(i);
			if(i == columns.size() -1 )
				sSQL += "";
			else
				sSQL += ",";
		}
		sSQL += " ) VALUES ";
		for(int i = iStartPoint; i < array.size(); i++){
			sSQL += " ( ";
			System.out.println();
			String [] record = array.get(i);
			for(int j = 0; j < record.length; j++){					
				sSQL += "'"+record[j]+"'";
				if(j == record.length - 1)
					sSQL += "";
				else
					sSQL += ",";
			}
			sSQL += " ) ";
			if(i == array.size() - 1)
				sSQL += "";
			else
				sSQL += ",";
		}
		
		sSQL += " ON DUPLICATE KEY UPDATE ";
		ArrayList<String> columnWithoutPrimaryAndUnique = columnsWithOutPrimaryAndUniqueKeys (sPrimaryKey,columns);
		for(int i = 0; i < columnWithoutPrimaryAndUnique.size(); i++){
				sSQL += columnWithoutPrimaryAndUnique.get(i)+" = "+columnWithoutPrimaryAndUnique.get(i)+"";
				if(i == columnWithoutPrimaryAndUnique.size() - 1)
					sSQL += " ";
				else
					sSQL += ", ";
			}
		return sSQL;
	}
	

	
	
	private int addRecordsfromArray(boolean bIncludeHeader, ArrayList<String []>array , ArrayList<String> sPrimaryKey, ArrayList<String> columns, String sConf, String sUserID) throws SQLException{
		Connection conn = null;
		String sSQL = "";
		int iNumberOfRecords = 0;
		try {
			sSQL = addRecordsQuery(bIncludeHeader, array, sPrimaryKey, columns);
			conn = clsDatabaseFunctions.getConnection(getServletContext(), sConf, "MySQL", this.toString() + " SQL: " + sSQL);
		} catch (Exception e) {
			throw new SQLException(e.getMessage());
		}
		//System.out.println(newString);
		try{
			Statement stmt = conn.createStatement();
			System.out.println(sSQL);
			iNumberOfRecords = stmt.executeUpdate(sSQL);
		    clsDatabaseFunctions.freeConnection(getServletContext(), conn);
		}catch (Exception ex) {
			// handle any errors
			SMClasses.SMLogEntry log = new SMClasses.SMLogEntry(sConf, getServletContext());
			log.writeEntry(
					sUserID, 
					SMLogEntry.LOG_OPERATION_SMEXECUTESQL, 
					"[1376503188] EXECUTE SQL FAILED - " + ex.getMessage(),
					"Command: " + sSQL,
					"[1376509321]");
			if (conn != null){
				clsDatabaseFunctions.freeConnection(getServletContext(), conn);
			}
			throw new SQLException("ERROR [1538073115] "+ex.getMessage());
		}
		return iNumberOfRecords;

	}
	
private void deleteCurrentTempImportFiles(String sTempImportFilePath) throws Exception{
		
	    File dir = new File(sTempImportFilePath);
	    if (!dir.exists()) {
	    	throw new Exception("Error [1396369366] - directory " + sTempImportFilePath + " does not exist.");
	    }
	    String[] info = dir.list();
	    for (int i = 0; i < info.length; i++) {
	      File n = new File(sTempImportFilePath + System.getProperty("file.separator") + info[i]);
	      if (!n.isFile()) { // skip ., .., other directories, etc.
	        continue;
	      }
	      if (!n.delete()){
	    	  throw new Exception("Error [1396369367] - error deleting " 
	    		+ sTempImportFilePath + System.getProperty("file.separator") + info[i] + ".");
	      }
	    }

	}
	
	public void doGet(HttpServletRequest request,
			HttpServletResponse response)
	throws ServletException, IOException {

		doPost(request, response);
	}
}
