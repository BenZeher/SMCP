package ConnectionPool;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import javax.servlet.ServletContext;

public class PoolUtilities {
	
	//public static final int MAX_NO_OF_CONNECTIONS = 30;
	private static final String MYSQL_DRIVER = "mysql";
	private static final String MYSQL_DRIVER_CLASS_NAME = "com.mysql.jdbc.Driver";
	private static final String MYSQL_DEFAULT_PORT = "3306";
	private static final String MYSQL_DATABASE_TYPE = "MySQL";
	private static final String MYSQL_CONNECT_STRING = "?noDatetimeStringSync=true&connectTimeout=28800000&interactiveClient=True"; //&zeroDateTimeBehavior=round";
	private static boolean bDebugMode = false;

	protected static LabelledConnection OpenDatabaseConnection(
			String sDataBaseID, 
			String sType, 
			String sCallingClass,
			ServletContext context) throws Exception{

		//If the instance uses a control database, then the database ID is actually a database ID, but if 
		//it's NOT using a control database, then the 'database ID' is actually just the name of the database.
		String sDatabaseURL = "127.0.0.1";
		String sUserName = "UserName";
		String sPassword = "P@ssword";
		String sDatabasePort = MYSQL_DEFAULT_PORT;
		String sDataBaseName = "servmgr";

		if (bDebugMode){
			System.out.println(
					"in PoolUtilities.OpenDatabaseConnection - "
					+ "sDatabaseID = " + sDataBaseID
					+ "sType = " + sType
					+ "sCallingClass = " + sCallingClass
			);
		}

		//If there is a CONTROL database, then get the company database info from there:
		ServerSettingsFileParameters serverSettingsFile = new ServerSettingsFileParameters(ServerSettingsFileParameters.getFullPathToResourceFolder(context) + ServerSettingsFileParameters.SERVER_SETTINGS_FILENAME);
		if (serverSettingsFile.readKeyValue(ServerSettingsFileParameters.SERVER_SETTING_CONTROL_DB_NAME).compareToIgnoreCase("") != 0){
			CompanyDataCredentials cdc = new CompanyDataCredentials();
			try {
				
				cdc.load(
					sDataBaseID, 
					serverSettingsFile.readKeyValue(ServerSettingsFileParameters.SERVER_SETTING_CONTROL_DB_URL),
					serverSettingsFile.readKeyValue(ServerSettingsFileParameters.SERVER_SETTING_CONTROL_DB_PORT),
					serverSettingsFile.readKeyValue(ServerSettingsFileParameters.SERVER_SETTING_CONTROL_DB_NAME),
					serverSettingsFile.readKeyValue(ServerSettingsFileParameters.SERVER_SETTING_CONTROL_DB_USERNAME),
					serverSettingsFile.readKeyValue(ServerSettingsFileParameters.SERVER_SETTING_CONTROL_DB_PASSWORD)
				);
				sDatabaseURL = cdc.get_databaseurl();
				sUserName = cdc.get_databaseuser();
				sPassword = cdc.get_databaseuserpw();
				sDatabasePort = cdc.get_databaseport();
				sDataBaseName = cdc.get_databasename();
			} catch (Exception e) {
				throw new Exception("Error [1540923861] - reading control database - " + e.getMessage());
			}
		}
		String sDB = "";
		String sDbURl = "";
		String sDriver = "";
		String sDriverClassName = "";
		String sPort = "";
		String sDBType = "";
		String sConnectStringParams = "";

		sDB = sDataBaseName;
		sDbURl = sDatabaseURL;
		sDriver = MYSQL_DRIVER;
		sDriverClassName = MYSQL_DRIVER_CLASS_NAME;
		sPort = sDatabasePort;
		sDBType = MYSQL_DATABASE_TYPE;
		sConnectStringParams = MYSQL_CONNECT_STRING;

		//Now try to get the connection:
		//create an instance of JDBC connection if there is none.
		//in case there is no instance of JDBC driver loaded, an exception will be thrown. catch that and load the driver.
		if (bDebugMode){
			System.out.println("in PoolUtilities.OpenDatabaseConnection - " + "trying first connection.");
		}
		String sConnectionString = "jdbc:" + sDriver + "://" + sDbURl + ":" + sPort + "/" + sDB + sConnectStringParams;
		try {
			Connection conn = DriverManager.getConnection(sConnectionString, sUserName, sPassword);
			if (bDebugMode){
				System.out.println("Connection String: " + sConnectionString + " - User: " + sUserName + " Password: '" + sPassword + "'");
			}
			long lConnectionID = getConnectionID(conn, sDBType);
			return (new LabelledConnection(conn, sDataBaseID, sDBType, sCallingClass, sDbURl, new Timestamp(System.currentTimeMillis()), lConnectionID));
		}catch (SQLException e) {
			try {
				if (bDebugMode){
					System.out.println("In PoolUtilities.OpenDatabaseConnection, First connection attempt failed, going to try to register class first.");
				}
				Class.forName(sDriverClassName).newInstance();
			} catch (InstantiationException e2) {
				if (bDebugMode){
					System.out.println("In PoolUtilities.OpenDatabaseConnection, got InstantiationException trying to register driver class.");
				}
				throw new Exception("InstantiationException getting new instance for"
						+ sDBType + " driver with connection string: " + sConnectionString + " - User: " + sUserName + " Password: " 
						+ sPassword + " - " + e2.getMessage()
				);
			} catch (IllegalAccessException e2) {
				if (bDebugMode){
					System.out.println("In PoolUtilities.OpenDatabaseConnection, got IllegalAccessException trying to register driver class.");
				}
				throw new Exception("IllegalAccessException getting new instance for " + sDBType + " driver with connection string:"
					+ sConnectionString + " - User: " + sUserName + " Password: " + sPassword  + " - " + e2.getMessage());
			} catch (ClassNotFoundException e3) {
				if (bDebugMode){
					System.out.println("In PoolUtilities.OpenDatabaseConnection, got ClassNotFoundException trying to register driver class.");
				}
				throw new Exception("ClassNotFoundException getting new instance for"
					+ sDBType + " driver with connection string: " + sConnectionString + " - User: " + sUserName + " Password: " 
					+ sPassword + " - " + e3.getMessage());
			}
			if (bDebugMode){
				System.out.println("In PoolUtilities.OpenDatabaseConnection, second try to get connection.");
			}
			try{
				Connection conn = DriverManager.getConnection(sConnectionString, sUserName, sPassword);
				if (bDebugMode){
					System.out.println("(Second try) Connection String: " + sConnectionString + " - User: " + sUserName + " Password: "  
						+ sPassword);
				}
				if (bDebugMode){
					System.out.println("In PoolUtilities.OpenDatabaseConnection, got connection on second try; returning connection."
					);
				}
				long lConnectionID = getConnectionID(conn, sDBType);
				return (new LabelledConnection(conn, sDataBaseID, sDBType, sCallingClass, sDbURl, new Timestamp(System.currentTimeMillis()), lConnectionID));
			}catch(Exception e1){
				if (bDebugMode){
					System.out.println("Exception on second try getting connection for " + sDBType + " driver with connection string:"
						+ sConnectionString + " - User: " + sUserName + " Password: " + sPassword + " - " + e1.getMessage());
				}
				throw new Exception("Exception getting new instance for " + sDBType + " driver with connection string:"
						+ sConnectionString + " - User: " + sUserName + " Password: " + sPassword + " - " + e1.getMessage());
			}
		}
	  }

	private static long getConnectionID(Connection conn, String sDatabaseType) throws Exception{
		long lConnectionID = 0L;

		if (sDatabaseType.compareToIgnoreCase("MySQL") != 0){
			return 0L;
		}
		Statement stmt = conn.createStatement();
		try {
			ResultSet rs = stmt.executeQuery("SELECT CONNECTION_ID()");
			if (rs.next()){
				lConnectionID = rs.getLong(1);
			}
			rs.close();
		} catch (Exception e) {
			throw new Exception("Error [1500321531] getting connection ID - " + e.getMessage());
		}
		return lConnectionID;
	}
	
	public static Connection getConnection(
			ServletContext context, 
			String sDBID, 
			String sType, 
			String sCallingClass
	) throws Exception{
		//this function gets a connection from the connection pool
		String sFullDescription = "L/O: " + nowInStdFormat() + " " + sCallingClass;
		Connection conn;
		ConnectionPool connections = (ConnectionPool) context.getAttribute("DBConnections");
		if (bDebugMode){
			System.out.println("01 Poolutilities.getConnection - sDBID = " + sDBID + ", sType = " + sType + ", sCallingClass = " + sCallingClass);
		}
		
		if (sDBID.compareToIgnoreCase("") == 0){
			throw new Exception("Error [1420561169] - in PoolUtilities.getConnection, sDBID is blank.");
		}

		//	throw new Exception("Error [1420561170] - in PoolUtilities.getConnection, context is null.");
		//If there is no connection pool yet, get one:
		if (connections == null){
			if (bDebugMode){
				System.out.println("PoolUtilities.getConnection " + "there is no available connection pool, create one.");
			}
			int iMaxNumberOfConnections;
			try {
				iMaxNumberOfConnections = Integer.parseInt(WebContextParameters.getMaximumNumberOfConnections(context));
			} catch (NumberFormatException e) {
				throw new Exception("Error [1539616429] reading max number of connections from web context parameters.");
			}
			try{
				connections = new ConnectionPool(
						sDBID, 
						sType, 
						sFullDescription, 
						context, 
						1, 
						iMaxNumberOfConnections, 
						true
				);
			}catch(Exception e1){
				throw new Exception("Error [1539616430] instantiating connection pool - " + e1.getMessage());
			}
			context.setAttribute("DBConnections", connections);
		}
		//get a connection for the correct company

		//Otherwise, get a connection from the connection pool:
		try {
			conn = connections.getConnection(sDBID, sType, sFullDescription);
		} catch (Exception e) {
			throw new Exception("Error [1420561451] in PoolUtilities.getConnection, sDBID = '" + sDBID 
				+ "', context.toString = '" + context.toString() + "' - " + e.getMessage());
		}
		
		//print out the status of our pool
		//connections.PrintStatus(false);	
		return conn;  
	}
	/* TJR - 1/3/2019 - replaced this with the function below to add diagnostics
	public static void freeConnection(ServletContext context, Connection conn) throws Exception{
		//this function returns the connection to the connection pool for next use
		
		if (context == null){
			System.out.println("Error [1516370807] freeing connection - context is null.");
			throw new Exception("Error [1516370817] freeing connection - context is null.");
		}
		try {
			ConnectionPool connections = (ConnectionPool) context.getAttribute("DBConnections");
			if (connections == null){
				throw new Exception("Error [1516370808] - variable 'connections' is null.");
			}
			
			if (conn != null) {
				connections.free(conn);
				//print out the status of our pool
				//connections.PrintStatus(false);	
			}else{
				throw new Exception("Error [1480623791] - connection to be freed is null (context.toString() = '" + context.toString() + "'.");
			}
		} catch (Exception e) {
			throw new Exception("Error [1480623781] - connection = '" + conn.toString() + ", context = '" + context.toString() + "' - " + e.getMessage());
		}
	}
	*/
	public static void freeConnection(ServletContext context, Connection conn, String sDiagnosticMarker) throws Exception{
		//this function returns the connection to the connection pool for next use
		
		if (context == null){
			System.out.println("Error [1516370807] freeing connection - context is null, diagnostic marker = '" + sDiagnosticMarker + "'.");
			throw new Exception("Error [1516370817] freeing connection - context is null, diagnostic marker = '" + sDiagnosticMarker + "'.");
		}
		try {
			ConnectionPool connections = (ConnectionPool) context.getAttribute("DBConnections");
			if (connections == null){
				throw new Exception("Error [1516370808] - variable 'connections' is null.");
			}
			
			if (conn != null) {
				connections.free(conn);
				//print out the status of our pool
				//connections.PrintStatus(false);	
			}else{
				throw new Exception("Error [1480623791] - connection to be freed is null (context.toString() = '" + context.toString() + "'.");
			}
		} catch (Exception e) {
			throw new Exception("Error [1480623781] - connection = '" + conn.toString() + ", context = '" + context.toString() + "' - " + e.getMessage());
		}
	}

	  public static boolean executeSQL(String SQLStatement, Connection conn) throws SQLException{
		  
			try{
				
			    Statement stmt = conn.createStatement();
			    stmt.executeUpdate(SQLStatement);
			    return true;
			}catch (SQLException ex) {
				// handle any errors
				if (bDebugMode){
					System.out.println("[1398284682] Error executing SQL: " + ex.toString() + "  *-*  " + ex.getMessage());
				}
				throw new SQLException(ex.getMessage());
			}
	  }
	  public static boolean executeSQL(
			  String SQLStatement, 
			  ServletContext context,
			  String sDBID) throws SQLException{
		  
		Connection conn;
		try {
			//LTO - changed 4/23/2014:
			//conn = getConnection(context, sDBID, "MySQL", "N/A");
			conn = getConnection(context, sDBID, "MySQL", "SQL: " + SQLStatement);
		} catch (Exception e) {
			throw new SQLException(e.getMessage());
		}
		  
		try{
		    Statement stmt = conn.createStatement();
		    stmt.executeUpdate(SQLStatement);
		    freeConnection(
		    	context, 
		    	conn,
		    	"[1546534862] SQL = '" + SQLStatement + "', sDBID = '" + sDBID + "'."
		    );
		    return true;
		}catch (Exception ex) {
			// handle any errors
			
			//If it's just a 'context is null' error, then we can return true and move on:
			if (ex.getMessage().contains("[1516370817]")){
				System.out.println("[1543535556] - SQLStatement = '" + SQLStatement + "', sDBID = '" + sDBID + "'.");
				return true;
			}
			
			if (conn != null){
				try{
					freeConnection(
						context, 
						conn,
						"[1546534861] SQL = '" + SQLStatement + "', sDBID = '" + sDBID + "'" );
				}catch (Exception e){
					//System.out.println("[1398284671] Failed to free connection");
					throw new SQLException("[1398284671] Failed to free connection - " + ex.getMessage());
				}
			}
			throw new SQLException("[1398285378]" + ex.getMessage());
		 }
	  }

	  public static boolean executeSQL(
			  String SQLStatement, 
			  ServletContext context, 
			  String sDBID, 
			  String DBType, 
			  String CallingClass) throws SQLException{
		  
			Connection conn;
			try {
				//TJR - changed 3/19/2012:
				//conn = getConnection(context, sDBID, DBType, CallingClass);
				conn = getConnection(context, sDBID, DBType, CallingClass + " SQL: " + SQLStatement);
			} catch (Exception e) {
				throw new SQLException(e.getMessage());
			}
			  
			try{		
				Statement stmt = conn.createStatement();
			    stmt.executeUpdate(SQLStatement);
			    freeConnection(
			    	context,
			    	conn,
			    	"[1546534860] SQL = '" + SQLStatement + "', sDBID = '" + sDBID + "'" 
			    );
			    return true;
			}catch (Exception ex) {
				// handle any errors
				
				//If it's just a 'context is null' error, then we can return true and move on:
				if (ex.getMessage().contains("[1516370817]")){
					System.out.println("[154353555] - SQLStatement = '" + SQLStatement + "', sDBID = '" + sDBID + "'" 
						+ " CallingClass = '" + CallingClass + "'"	
						+ ".");
					return true;
				}
				if (conn != null){
					try{
						freeConnection(
							context, 
							conn,
							"[1546535616] SQLStatement = '" + SQLStatement + "', sDBID = '" + sDBID + "'."
						);
					}catch(Exception e){
						//System.out.println("[1398285175] Failed to free connection");
						throw new SQLException("[1398285175] Failed to free connection - " + ex.getMessage());
					}
				}
				throw new SQLException("[1398285362]" + ex.getMessage());
			 }
		  }
	  
	  public static void executeSQL(
			  String SQL, 
			  String sDBID, 
			  String DBType, 
			  String CallingClass,
			  ServletContext context) throws Exception{

		  Connection conn;
		  try {
			  //TJR - changed 3/19/2012:
			  //conn = getConnection(context, sDBID, DBType, CallingClass);
			  conn = getConnection(context, sDBID, DBType, CallingClass + " SQL: " + SQL);
		  } catch (Exception e) {
			  throw new Exception(e.getMessage());
		  }

		  try{		
			  Statement stmt = conn.createStatement();
			  stmt.executeUpdate(SQL);
			  freeConnection(
					  context, 
					  conn,
					  "[1546534863] SQL = '" + SQL + ", sDBID = '" + sDBID + "'"
					  );
		  }catch (Exception ex) {
			  // handle any errors
			  
				//If it's just a 'context is null' error, then we can return true and move on:
				if (ex.getMessage().contains("[1516370817]")){
					System.out.println("[154353557] - SQLStatement = '" + SQL + "', sDBID = '" + sDBID + "'" 
						+ " CallingClass = '" + CallingClass + "'"	
						+ ".");
					return;
				}
			  
			  if (conn != null){
				  try{
					  freeConnection(
						context, 
						conn,
						"[1546534864] SQL = '" + SQL + ", sDBID = '" + sDBID + "'"
					);
				  }catch(Exception e){
					  //System.out.println("[1398285175] Failed to free connection");
					  throw new Exception("[1493057803] Failed to free connection - " + ex.getMessage());
				  }
			  }
			  throw new SQLException("[1493057804]" + ex.getMessage());
		  }
		  return;
	  }
	  
	  public static boolean executeSQLsInTransaction(
			  ArrayList<String> SQLStatements, 
			  ServletContext context, 
			  String sDBID) throws SQLException{

		  //First, make sure there is at least one statement in the list:
		  if (SQLStatements.size() <= 0){
			  return false;
		  }  

		  Connection conn = null;
		  Statement stmt = null;
	    String sAllSQLs = "";
	    for(int i = 0; i < SQLStatements.size(); i++){
	    	sAllSQLs += SQLStatements.get(i) + "\n";
	    }
		  try{
			  conn = getConnection(context, sDBID, "MySQL", "SQL: (Multiple SQLs in transaction.)");
			  stmt = conn.createStatement();
		    stmt.execute("START TRANSACTION");
		    for (int i=0;i<SQLStatements.size();i++){
		    	try {
					stmt.executeUpdate(SQLStatements.get(i).toString());
				} catch (Exception e) {
					throw new Exception("Error [1539616661] executing SQL command #" + Integer.toString(i + 1) + " - SQL = '" + SQLStatements.get(i) + "' - " + e.getMessage());
				}
		    }
		    stmt.execute("COMMIT");
		    
		    freeConnection(
		    	context, 
		    	conn,
		    	"[1546534867] executeSQLsInTransaction - AllSQLs = '" + sAllSQLs + "', sDBID = '" + sDBID + "'."
		    );
		    
		    return true;
		}catch (Exception ex) {
			
			//If it's just a 'context is null' error, then we can return true and move on:
			if (ex.getMessage().contains("[1516370817]")){
				System.out.println("[154353559] - SQLStatements.get(0) = '" + SQLStatements.get(0) + "', sDBID = '" + sDBID + "'" 
					+ ".");
				return true;
			}
			
			// roll back
			stmt.execute("ROLLBACK");
			if (conn != null){
				try{
					freeConnection(
						context, 
						conn,
						"[1546534868] executeSQLsInTransaction - AllSQLs = '" + sAllSQLs + "', sDBID = '" + sDBID + "'."
					);
				}catch(Exception e){
					//System.out.println("[1398285305] Failed to free connection");
					throw new SQLException("Error [1398285305] Failed to free connection - " + ex.getMessage());
				}
			}
			throw new SQLException("Error [1398285346]" + ex.getMessage());
		}
	  }
	  
	  public static void executeSQLsInTrans(
			  ArrayList<String> SQLStatements, 
			  ServletContext context, 
			  String sDBID) throws SQLException{

		  //First, make sure there is at least one statement in the list:
		  if (SQLStatements.size() <= 0){
			  throw new SQLException("No SQL statements to execute.");
		  }  

		  Connection conn = null;
		  Statement stmt = null;
		  
	    String sAllSQLs = "";
	    for(int i = 0; i < SQLStatements.size(); i++){
	    	sAllSQLs += SQLStatements.get(i) + "\n";
	    }
		  
		  try{
			  conn = getConnection(context, sDBID, "MySQL", "SQL: (Multiple SQLs in transaction.)");
			  stmt = conn.createStatement();
		    stmt.execute("START TRANSACTION");
		    for (int i=0;i<SQLStatements.size();i++){
		    	stmt.executeUpdate(SQLStatements.get(i).toString());
		    }
		    stmt.execute("COMMIT");
		    
		    freeConnection(
		    	context, 
		    	conn,
		    	"[1546534865] executeSQLsInTrans - AllSQLs = '" + sAllSQLs + "', sDBID = '" + sDBID + "'."
		    );
		}catch (Exception ex) {
			
			//If it's just a 'context is null' error, then we can return true and move on:
			if (ex.getMessage().contains("[1516370817]")){
				System.out.println("[154353558] - SQLStatements.get(0) = '" + SQLStatements.get(0) + "', sDBID = '" + sDBID + "'" 
					+ ".");
				return;
			}
			
			// roll back
			stmt.execute("ROLLBACK");
			if (conn != null){
				try{
					freeConnection(
						context, 
						conn,
						"[1546534866] executeSQLsInTrans - AllSQLs = '" + sAllSQLs + "', sDBID = '" + sDBID + "'."
					);
				}catch(Exception e){
					//System.out.println("[1398285423] Failed to free connection");
					throw new SQLException("[1398285423] Failed to free connection - " + ex.getMessage());
				}
			}
			throw new SQLException("[1398285424]" + ex.getMessage());
		}
		return;
	  }

	  public static ResultSet openResultSet(String SQLStatement, Connection conn) throws SQLException{
		  
			Statement stmt = conn.createStatement();
	        ResultSet rs = stmt.executeQuery(SQLStatement);
		    return rs;
		  }
	  public static ResultSet openResultSet(String SQLStatement, ServletContext context, String sDBID) throws SQLException{
		  
		  Connection conn;
		try {
			//TJR - changed 3/19/2012:
			//conn = getConnection(context, sDBID, "MySQL", "N/A");
			conn = getConnection(context, sDBID, "MySQL", "SQL: " + SQLStatement);
		} catch (Exception e) {
			throw new SQLException(e.getMessage());
		}
		
		ResultSet rs = null;
		try{
		    Statement stmt = conn.createStatement();
		    rs = stmt.executeQuery(SQLStatement);
	        
		   freeConnection(
				 context, 
				 conn,
				 "[1546534873] openResultSet - SQLStatement = '" + SQLStatement + "', DBID = '" + sDBID + "'."
			);
		    
		    return rs;
		}catch (Exception ex) {
			// handle any errors
			
			//If it's just a 'context is null' error, then we can return true and move on:
			if (ex.getMessage().contains("[1516370817]")){
				System.out.println("[154353562] - SQLStatement = '" + SQLStatement + "', sDBID = '" + sDBID + "'" 
					+ ".");
				return rs;
			}
			
			if (conn != null){
				try{
					freeConnection(
						context, 
						conn,
						"[1546534874] openResultSet - SQLStatement = '" + SQLStatement + "', DBID = '" + sDBID + "'."
					);
				}catch(Exception e){
					//System.out.println("[1398285485] Failed to free connection");
					throw new SQLException("[1398285487] Failed to free connection - " + ex.getMessage());
				}
			}
			throw new SQLException("Error [1398285486] with SQL = '" + SQLStatement + "' - " + ex.getMessage());
		}
	  }
	  public static ResultSet openResultSet(
			  String SQLStatement, 
			  ServletContext context, 
			  String sDBID, 
			  String DBType) throws SQLException{
		  
		  Connection conn;
		try {
			//TJR - changed 3/19/2012:
			//conn = getConnection(context, sDBID, DBType, "N/A");
			conn = getConnection(context, sDBID, DBType, "SQL: " + SQLStatement);
		} catch (Exception e) {
			throw new SQLException(e.getMessage());
		}
		
		ResultSet rs = null;
		try{
		    Statement stmt = conn.createStatement();
		    rs = stmt.executeQuery(SQLStatement);
	        
		   freeConnection(
				  context, 
				  conn,
				  "[1546534871] openResultSet - SQLStatement = '" + SQLStatement + "', DBID = '" + sDBID + "'."
				   );
		    
		    return rs;
		}catch (Exception ex) {
			// handle any errors
			
			//If it's just a 'context is null' error, then we can return true and move on:
			if (ex.getMessage().contains("[1516370817]")){
				System.out.println("[154353561] - SQLStatements = '" + SQLStatement + "', sDBID = '" + sDBID + "'" 
					+ ".");
				return rs;
			}
			
			if (conn != null){
				try{
					freeConnection(
						context, 
						conn,
						"[1546534872] openResultSet - SQLStatement = '" + SQLStatement + "', DBID = '" + sDBID + "'."
				);
				}catch(Exception e){
					//System.out.println("[1398285531] Failed to free connection");
					throw new SQLException("[1398285532] Failed to free connection - " + ex.getMessage());
				}
			}
			throw new SQLException("[1398285533]" + ex.getMessage());
		}
	  }
	  public static ResultSet openResultSet(
			  String SQLStatement, 
			  ServletContext context, 
			  String DBID, 
			  String DBType, 
			  String CallingClass) throws SQLException{
		  
		  Connection conn;
		try {
			//TJR - changed 3/19/2012:
			//conn = getConnection(context, sDBID, DBType, CallingClass);
			conn = getConnection(context, DBID, DBType, CallingClass + " SQL: " + SQLStatement);
		} catch (Exception e) {
			throw new SQLException("[1499348041] '" + CallingClass + "' " + e.getMessage());
		}
		 
		ResultSet rs = null;
		try{
		    Statement stmt = conn.createStatement();
		    rs = stmt.executeQuery(SQLStatement);
	        
		   freeConnection(
				   context, 
				   conn,
				   "[1546534869] openResultSet - SQLStatement = '" + SQLStatement + "', DBID = '" + DBID + "'."
			);
		    
		    return rs;
		}catch (Exception ex) {
			// handle any errors
			
			//If it's just a 'context is null' error, then we can return true and move on:
			if (ex.getMessage().contains("[1516370817]")){
				System.out.println("[154353560] - SQLStatement = '" + SQLStatement + "', DBID = '" + DBID + "'" 
					+ ".");
				return rs;
			}
			
			if (conn != null){
				try{
					freeConnection(
							context, 
							conn,
							"[1546534870] openResultSet - SQLStatement = '" + SQLStatement + "', DBID = '" + DBID + "'."
					);
				}catch(Exception e){
					//System.out.println("[1398285731] Failed to free connection");
					throw new SQLException("[1398285732] '" + CallingClass + "' Failed to free connection - " + ex.getMessage());
				}
			}
			throw new SQLException("[1398285733] '" + CallingClass + "'" + ex.getMessage());
		}
	  }
	  
	  public static String SystemTime(){

		  Calendar c = Calendar.getInstance();
		  c.setTimeInMillis(System.currentTimeMillis());
		  return c.getTime() + "  ";
	  }
	  
	  public static ArrayList<String> Get_Connection_List(ServletContext context, String sExecutioner){

		  ArrayList<String> alConnectionStatus = new ArrayList<String>(0);
		  //get connection pool
		  ConnectionPool connections = (ConnectionPool) context.getAttribute("DBConnections");
	
		  //output general information
		  alConnectionStatus.add(connections.Get_Connection_Pool_Connection_Summary());
		  alConnectionStatus.add("<HR>");
		  //output connection list
		  ArrayList<String> sConnectionList = new ArrayList<String>(0);
		  try {
			sConnectionList = connections.Get_Connection_Pool_Connection_List(sExecutioner);
			} catch (Exception e) {
				sConnectionList.add(e.getMessage());
			}
		  alConnectionStatus.addAll(sConnectionList);
		  
		  return alConnectionStatus;
	  }
	  
	  public static boolean ExecuteConnection(ServletContext context, int i, String sConnectionState){
		  
		  try {
			  //get connection pool
			  ConnectionPool connections = (ConnectionPool) context.getAttribute("DBConnections");
		
			  connections.free(i, sConnectionState);
			  return true;
		  }catch (Exception ex){
			  //catch exceptions
			  return false;
		  }
		  
	  }
	  
	  public static int getAvailableConnectionNumber(ServletContext context){

		  //get connection pool
		  ConnectionPool connections = (ConnectionPool) context.getAttribute("DBConnections");
		  return connections.availableConnectionNumber();
	  }
	private static String nowInStdFormat() {
		Calendar cal = Calendar.getInstance();
		SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss:SSSS");
		return sdf.format(cal.getTime());
	}
}
