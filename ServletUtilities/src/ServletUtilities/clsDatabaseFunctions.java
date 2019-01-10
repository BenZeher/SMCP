package ServletUtilities;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

import javax.servlet.ServletContext;

import ConnectionPool.PoolUtilities;

public class clsDatabaseFunctions {

	public static String FormatSQLStatement(String s) {
		
		if (s != null){
			s = s.replace("'", "''");
			s = s.replace("\\", "\\\\");
			s = s.replace("“", "\"");
			//ASCII Values
			int iAsciiLeftSingleQuotationMark = 145;
			int iAsciiRightSingleQuotationMark = 146;
			int iAsciiLeftDoubleQuotationMark = 147;
			int iAsciiRightDoubleQoutationMark = 148;
			
			StringBuilder newString = new StringBuilder();
			//Loop to Check if there is any Double or Single Quotes
			for(int i = 0; i < s.length(); i++){
				int iCharacterAsciivalue = s.charAt(i);
				if(iAsciiLeftDoubleQuotationMark == iCharacterAsciivalue){
					newString.append('\"');
				}else if (iAsciiRightDoubleQoutationMark == iCharacterAsciivalue){
					newString.append('\"');
				}
				else if (iAsciiLeftSingleQuotationMark == iCharacterAsciivalue){
					newString.append("\'\'");
				}else if(iAsciiRightSingleQuotationMark == iCharacterAsciivalue){
					newString.append("\'\'");
				}else{
					newString.append(s.charAt(i));
				}
			}
			if(newString.length() != 0)
     			s = newString.toString();
		}
	
		return s;
	}

	public static Connection getConnection(ServletContext context, String sConf, String sDBType, String sCallingClass){
		try {
			return PoolUtilities.getConnection(context, sConf, sDBType, sCallingClass);
		}catch (Exception e){
			System.out.println("Error [1389965763] In ServletUtilities.getConnection - " + e.getMessage());
			return null;
		}
	}

	public static Connection getConnectionWithException(ServletContext context, String sConf, String sDBType, String sCallingClass) throws Exception{
		try {
			return PoolUtilities.getConnection(context, sConf, sDBType, sCallingClass);
		}catch (SQLException e){
			throw new Exception("Error [1389965764] getting connection - " + e.getMessage());
		}
	}

	/* TJR - replaced this with the function underneath, which calls for an additional parameters, the diagnostic marker:
	public static boolean freeConnection(ServletContext context, Connection conn){
		
		String sDiagnosticMarker = "[1546534875] clsDatabaseFunctions.freeConnection";
		
		try {
			PoolUtilities.freeConnection(context, conn, sDiagnosticMarker);
			return true;
		}catch (Exception e){
			java.util.Date todaysDate = new java.util.Date();
			SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			String formattedDate = formatter.format(todaysDate);
			System.out.println("[1479228065] Error in freeConnection - " 
				+ formattedDate + " - error: " 
				+ e.getMessage()
			);
			//If it's just a 'context is null' error, then we can return true and move on:
			if (e.getMessage().contains("[1516370817]")){
				return true;
			}
			return false;
		}
	}
	*/
	public static boolean freeConnection(ServletContext context, Connection conn, String sDiagnosticMarker){
		
		try {
			PoolUtilities.freeConnection(context, conn, sDiagnosticMarker);
			return true;
		}catch (Exception e){
			java.util.Date todaysDate = new java.util.Date();
			SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			String formattedDate = formatter.format(todaysDate);
			System.out.println("[1479228065] Error in freeConnection - " 
				+ formattedDate + " - error: " 
				+ e.getMessage()
			);
			//If it's just a 'context is null' error, then we can return true and move on:
			if (e.getMessage().contains("[1516370817]")){
				return true;
			}
			return false;
		}
	}

	public static boolean executeSQL(String SQLStatement, Connection conn) throws SQLException{
		return PoolUtilities.executeSQL(SQLStatement, conn);
	}

	public static boolean executeSQL(String SQLStatement, ServletContext context, String confName) throws SQLException{
		return PoolUtilities.executeSQL(SQLStatement, context, confName);
	}

	public static boolean executeSQL(String SQLStatement, ServletContext context, String confName, String DBType, String CallingClass) throws SQLException{
		return PoolUtilities.executeSQL(SQLStatement, context, confName, DBType, CallingClass);
	}

	public static void executeSQLWithException(
		String sSQL, 
		String sDBID, 
		String sDBType, 
		String sCallingClass, 
		ServletContext context
		) throws Exception{
		
		PoolUtilities.executeSQL(
			  sSQL, 
			  sDBID, 
			  sDBType, 
			  sCallingClass,
			  context)
		;
	}
	public static void executeSQL(String SQLStatement, String confName, String DBType, String CallingClass, ServletContext context) throws Exception{
		PoolUtilities.executeSQL(SQLStatement, confName, DBType, CallingClass, context);
	}

	public static boolean executeSQLsInTransaction(ArrayList<String> SQLStatements, ServletContext context, String confName) throws SQLException{
		return PoolUtilities.executeSQLsInTransaction(SQLStatements, context, confName);
	}

	public static void executeSQLsInTrans(ArrayList<String> SQLStatements, ServletContext context, String confName) throws SQLException{
		try {
			PoolUtilities.executeSQLsInTransaction(SQLStatements, context, confName);
		} catch (Exception e) {
			throw new SQLException("Error [1539615603] executing SQL statements in transaction - " + e.getMessage() + ".");
		}
	}

	public static ResultSet openResultSet(String SQLStatement, Connection conn) throws SQLException{
		return PoolUtilities.openResultSet(SQLStatement, conn);
	}

	public static ResultSet openResultSet(String SQLStatement, ServletContext context, String confName) throws SQLException{
		return PoolUtilities.openResultSet(SQLStatement, context, confName);
	}

	public static ResultSet openResultSet(String SQLStatement, ServletContext context, String confName, String DBType, String CallingClass) throws SQLException{
		return PoolUtilities.openResultSet(SQLStatement, context, confName, DBType, CallingClass);
	}

	public static ArrayList<String> getConnectionStatus(ServletContext context, String sExecutioner){
		return PoolUtilities.Get_Connection_List(context, sExecutioner);
	}

	public static boolean ExecuteConnection(ServletContext context, int i, String sConnectionState){
		return PoolUtilities.ExecuteConnection(context, i, sConnectionState);
	}

	public static int getAvailableConnectionNumber(ServletContext context){
		return PoolUtilities.getAvailableConnectionNumber(context);
	}

	//Data transactions:
	public static boolean start_data_transaction (Connection conn){
	
		String SQL = "START TRANSACTION";
		try{
			if (executeSQL(SQL, conn) == false){
				return false;
			}
		}catch (SQLException ex){
			System.out.println("Error in SMUtilities - start transaction!!");
			System.out.println("SQLException: " + ex.getMessage());
			System.out.println("SQLState: " + ex.getSQLState());
			System.out.println("SQL: " + ex.getErrorCode());
			return false;
		}
		return true;
	}

	public static boolean rollback_data_transaction (Connection conn){
	
		String SQL = "ROLLBACK";
		try{
			if (executeSQL(SQL, conn) == false){
				return false;
			}
		}catch (SQLException ex){
			System.out.println("Error in SMUtilities - rollback transaction!!");
			System.out.println("SQLException: " + ex.getMessage());
			System.out.println("SQLState: " + ex.getSQLState());
			System.out.println("SQL: " + ex.getErrorCode());
			return false;
		}
		return true;
	}

	public static boolean commit_data_transaction (Connection conn){
	
		String SQL = "COMMIT";
		try{
			if (executeSQL(SQL, conn) == false){
				return false;
			}
		}catch (SQLException ex){
			System.out.println("Error in SMUtilities - commit transaction!!");
			System.out.println("SQLException: " + ex.getMessage());
			System.out.println("SQLState: " + ex.getSQLState());
			System.out.println("SQL: " + ex.getErrorCode());
			return false;
		}
		return true;
	}

	public static long getLastInsertID(Connection conn) throws SQLException {
		long lID = -1; 
		try {
			//Get the ID:
			ResultSet rsID = openResultSet(
					"SELECT LAST_INSERT_ID()", conn);
			if (rsID.next()) {
				lID = rsID.getLong(1);
				rsID.close();
			} else {
				rsID.close();
				throw new SQLException("Could not get last insert ID");
			}
		} catch (SQLException e) {
			throw e;
		}
		return lID;
	}

	public static String getRecordsetStringValue (ResultSet rs, String sFieldName){
	
		try {
			if (rs.getString(sFieldName) == null){
				return "";
			}else{
				return rs.getString(sFieldName);
			}
		} catch (SQLException e) {
			return "";
		}
	}
	public static String updateFieldValueFromNullToEmptyString (String sFieldName, String sTableName) {
		return "UPDATE `" + sTableName + "` SET `" + sFieldName + "` = '' WHERE (`" + sFieldName + "` IS NULL)";
	}
	
	public static String updateDBFieldDefaultValueToEmptyString (String sFieldName, String sTableName, int iFieldLength) {
		return "ALTER TABLE `" + sTableName + "` CHANGE `" + sFieldName + "` `" + sFieldName + "` varchar(" + Integer.toString(iFieldLength) + ") NOT NULL DEFAULT ''";
	}
}
