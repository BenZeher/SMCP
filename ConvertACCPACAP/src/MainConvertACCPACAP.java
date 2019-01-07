import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import SMDataDefinition.SMTableapoptions;
import smap.APACCPACConversion;

public class MainConvertACCPACAP
{
	public static void main(String[] paramArrayOfString)
	{
		
		String sConversionUser = "TESTCONVERT";
		
		int iAPDatabaseType = 0; // or Pervasive is 0, MS SQL = 1
		int iACCPACAPVersion = 0;
		String sDatabaseURL = "127.0.0.1"; //str1
		String sDatabaseName = "Database"; //str2
		String sUserName = "UserName";     //str3
		String sPassword = "Password";     //str4
		String sAPDatabaseURL = "127.0.0.1"; //str1
		String sAPDatabaseName = "Database"; //str2
		String sAPUserName = "UserName";     //str3
		String sAPPassword = "P@ssword";     //str4
		
/*
			if (paramArrayOfString.length < 8) {
				System.out.println("Command line usage: SMCPDatabaseURL SMCPDatabaseName SMCPUserName SMCPPw APDatabaseURL APDatabase APUserName APPw");
				//System.out.println("For example:");
				//System.out.println("To run connection test: java ACCPACICConvert ACCPACIC.conf TEST");
				//System.out.println("To run IC data conversion: java ACCPACICConvert ACCPACIC.conf IC");
				//System.out.println("To run PO Header data conversion: java ACCPACICConvert ACCPACIC.conf POHEAD");
				//System.out.println("To run PO Line data conversion: java ACCPACICConvert ACCPACIC.conf POLINE");
				//System.out.println("To run PO Receipt data conversion: java ACCPACICConvert ACCPACIC.conf POREC");
				//System.out.println("To run any combination, add options: java ACCPACICConvert ACCPACIC.conf POHEAD POLINE");
				//System.out.println("To convert ALL data in one run: java ACCPACICConvertACCPACIC.conf  ALL");
				return;
			}
*/		
		
		//TEST run:
		if (paramArrayOfString.length == 0){
			
			//Loca database on laptop:
			sDatabaseURL = "localhost";
			sDatabaseName = "servmgr1";
			sUserName = "smuser7sT559";
			sPassword = "kJ26D3G9bvK8";
			
			/*
			//Washington companies:
			iAPDatabaseType = SMTableapoptions.ACCPAC_DATABASE_VERSION_TYPE_MSSQL;
			sAPDatabaseURL = "madg01.com";
			sAPDatabaseName = "comp1";
			//sAPDatabaseName = "comp2";
			//sAPDatabaseName = "comp3";
			//sAPDatabaseName = "comp4"; //GDR
			//sAPDatabaseName = "airo";
			//sAPDatabaseName = "admin"; //MADG
			//sAPDatabaseName = "HROffice";
			sAPUserName = "jdbc";
			sAPPassword = "TScb15^%!";
			*/
		
			/*
			//OHD Tampa Bay
			iAPDatabaseType = SMTableapoptions.ACCPAC_DATABASE_VERSION_TYPE_PERVASIVE;
			sAPDatabaseURL = "199.119.100.130";
			sAPDatabaseName = "OHDINC"; //Tampa
			sAPUserName = "AERO";
			sAPPassword = "aero15";
			*/
			
			/*
			//OHD of the Capitol City (Tallahassee)
			iAPDatabaseType = SMTableapoptions.ACCPAC_DATABASE_VERSION_TYPE_PERVASIVE;
			sAPDatabaseURL = "199.119.100.130";
			sAPDatabaseName = "OHDCPL"; //Capitol city
			sAPUserName = "AERO";
			sAPPassword = "aero15";
			*/
			
			
			//OHD Daytona:
			iAPDatabaseType = SMTableapoptions.ACCPAC_DATABASE_VERSION_TYPE_PERVASIVE;
			sAPDatabaseURL = "74.50.124.130";
			sAPDatabaseName = "OHDDAY";
			sAPUserName = "Airo";
			sAPPassword = "tomcat";
			
			
		}else{
			//Load the parameters from the command line:
			sDatabaseURL = paramArrayOfString[0];
			sDatabaseName = paramArrayOfString[1];
			sUserName = paramArrayOfString[2];
			sPassword = paramArrayOfString[3];
			sAPDatabaseURL = paramArrayOfString[4];
			sAPDatabaseName = paramArrayOfString[5];
			sAPUserName = paramArrayOfString[6];
			sAPPassword = paramArrayOfString[7];
		}
		
		Connection cnSMCP = null;
		try
		{
			cnSMCP = DriverManager.getConnection("jdbc:mysql://" + sDatabaseURL + ":3306/" + sDatabaseName + "?noDatetimeStringSync=true&connectTimeout=28800000&interactiveClient=True", sUserName, sPassword);
		}
		catch (Exception localException2) {
			try {
				Class.forName("com.mysql.jdbc.Driver").newInstance();
				cnSMCP = DriverManager.getConnection("jdbc:mysql://" + sDatabaseURL + ":3306/" + sDatabaseName + "?noDatetimeStringSync=true&connectTimeout=28800000&interactiveClient=True", sUserName, sPassword);
			} catch (InstantiationException e) {
				System.out.println("InstantiationException getting MySQL connection - " + e.getMessage());
				return;
			} catch (IllegalAccessException e) {
				System.out.println("IllegalAccessException getting MySQL connection - " + e.getMessage());
				return;
			} catch (ClassNotFoundException e) {
				System.out.println("ClassNotFoundException getting MySQL connection - " + e.getMessage());
				return;
			} catch (SQLException e) {
				System.out.println("SQLException getting MySQL connection - " + e.getMessage());
				return;
			}
		}
		
		if (cnSMCP == null){
			System.out.println("Could not get MySQL connection");
			return;
		}
		
		System.out.println("Successfully got MySQL connection");
		
		Connection cnAP = null;
		
		//If we're reading a Pervasive DB:
		if (iAPDatabaseType == SMTableapoptions.ACCPAC_DATABASE_VERSION_TYPE_PERVASIVE){
		//Pervasive connection
			try
				{
					cnAP = DriverManager.getConnection("jdbc:pervasive://" + sAPDatabaseURL + ":1583/" + sAPDatabaseName + "", sAPUserName, sAPPassword);
			}catch (Exception localException2) {
				try {
					Class.forName("com.pervasive.jdbc.v2.Driver").newInstance();
					cnAP = DriverManager.getConnection("jdbc:pervasive://" + sAPDatabaseURL + ":1583/" + sAPDatabaseName + "", sAPUserName, sAPPassword);
				} catch (InstantiationException e) {
					System.out.println("InstantiationException getting ACCPAC connection - " + e.getMessage());
					return;
				} catch (IllegalAccessException e) {
					System.out.println("IllegalAccessException getting ACCPAC connection - " + e.getMessage());
					return;
				} catch (ClassNotFoundException e) {
					System.out.println("ClassNotFoundException getting ACCPAC connection - " + e.getMessage());
					return;
				} catch (SQLException e) {
					System.out.println("SQLException getting ACCPAC connection - " + e.getMessage());
					return;
				}
			}
			
			if (cnAP == null){
				System.out.println("Could not get Pervasive connection");
				return;
			}
			
			System.out.println("Successfully got Pervasive connection");
		//If we're reading an MS SQL DB:
		}else{
			try
			{
				cnAP = DriverManager.getConnection("jdbc:microsoft:sqlserver://" + sAPDatabaseURL + ":1433;DatabaseName=" + sAPDatabaseName, sAPUserName, sAPPassword);
			}
			catch (Exception localException2) {
				try {
					//Class.forName("com.microsoft.jdbc.sqlserver.SQLServerDriver").newInstance();
					//Class.forName("com.microsoft.jdbc.sqlserver.sqlserverdriver").newInstance();
					Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver").newInstance();
					//cnAP = DriverManager.getConnection("jdbc:microsoft:sqlserver://" + sAPDatabaseURL + ":1433;DatabaseName=" + sAPDatabaseName, sAPUserName, sAPPassword);
					cnAP = DriverManager.getConnection("jdbc:sqlserver://" + sAPDatabaseURL + ":1433;DatabaseName=" + sAPDatabaseName, sAPUserName, sAPPassword);
					//String Url = "jdbc:sqlserver://localhost:1433;databaseName=movies";
			        //Connection connection = DriverManager.getConnection(Url,"sa", "xxxxxxx);
				} catch (InstantiationException e) {
					System.out.println("InstantiationException getting ACCPAC connection - " + e.getMessage());
					return;
				} catch (IllegalAccessException e) {
					System.out.println("IllegalAccessException getting ACCPAC connection - " + e.getMessage());
					return;
				} catch (ClassNotFoundException e) {
					System.out.println("ClassNotFoundException getting ACCPAC connection - " + e.getMessage());
					return;
				} catch (SQLException e) {
					System.out.println("SQLException getting ACCPAC connection - " + e.getMessage());
					return;
				}
			}
			
			if (cnAP == null){
				System.out.println("Could not get MSSQL connection");
				return;
			}
			
			System.out.println("Successfully got MSSQL connection");
		}
		
		//readPervasiveData(cnAP);
		//readPVTableFields(cnAP, "APVEN");
		//comparePervasiveTables(cnMSSQL, cnAP);
		readScheduleIntervals(cnAP);
		
		PrintWriter out = new PrintWriter(System.out, true);
		
		APACCPACConversion convACCPAC = new APACCPACConversion();
		try {
			out.println(convACCPAC.convertData(cnSMCP, cnAP, iAPDatabaseType, iACCPACAPVersion, sConversionUser, false).replace("<BR>", "\n"));
		} catch (Exception e) {
			out.println(e.getMessage());
		}

		closeConnections(cnSMCP, cnAP);
		return;
	}
	private static void closeConnections(Connection conn, Connection cnACCPAC){
		try {
			conn.close();
		} catch (SQLException e) {
			//Can't do much here
		}
		conn = null;
		try {
			cnACCPAC.close();
		} catch (SQLException e) {
			//Can't do much here
		}
		cnACCPAC = null;
		return;
	}
	@SuppressWarnings("unused")
	private static void readPervasiveData(Connection cnPV){
		//Pervasive tables:
		String SQL = "Select * from X$File order by xf$name";
		String SQL1 = "";
		try{
			Statement stmt = cnPV.createStatement();
			ResultSet rs = stmt.executeQuery(SQL);
			while (rs.next()){
				String sTableName = rs.getString(2);
				if (sTableName.startsWith("AP")){
					try{
						SQL1 = "SELECT COUNT(*) AS TOTALRECORDS FROM " + sTableName;
						Statement stmtcount = cnPV.createStatement();
						ResultSet rscount = stmtcount.executeQuery(SQL1);
						while(rscount.next()){
							if (rscount.getLong("TOTALRECORDS") > 0L){
								System.out.println(sTableName.trim() + ", " + Long.toString(rscount.getLong("TOTALRECORDS")));
							}
						}
					}catch(Exception ex1){
						System.out.println("Error opening resultset with SQL1: " + SQL1 + " - " 
								+ ex1.toString() + "  *-*  " + ex1.getMessage());
					}
				}
			}
			rs.close();
		}catch (Exception ex) {
			// handle any errors
			System.out.println("Error opening resultset with SQL: " + SQL + " - " 
				+ ex.toString() + "  *-*  " + ex.getMessage());
			//return null;
			//throw new SQLException(ex.getMessage());
		}
	}
	@SuppressWarnings("unused")
	private static void readPVTableFields(Connection cnPV, String sTableName){
		String SQL = "select X$Field.* from X$Field, X$File where xe$File = xf$id"
			+ " and xf$name = '" + sTableName + "' and xe$DataType < 200 order by xe$offset";
		System.out.println("Table " + sTableName);
		String sFields = "";
		ArrayList<String>arrFieldNames = new ArrayList<String>(0);
		try{
			Statement stmt = cnPV.createStatement();
			ResultSet rs = stmt.executeQuery(SQL);
			while (rs.next()){
				//Get the field name:
				String sFieldName = rs.getString(3).trim();
				arrFieldNames.add(sFieldName);
				System.out.println(sFieldName);
				sFields += sFieldName + ", ";
			}
			rs.close();
		}catch (Exception ex) {
			// handle any errors
			System.out.println("Error opening resultset with SQL: " + SQL + " - " 
				+ ex.toString() + "  *-*  " + ex.getMessage());
			//return null;
			//throw new SQLException(ex.getMessage());
		}
		System.out.println(sFields);
		System.out.println("");
		
		//Get the data:
		SQL = "SELECT * FROM " + sTableName;
		System.out.println("Sample data:");
		try{
			Statement stmt = cnPV.createStatement();
			ResultSet rs = stmt.executeQuery(SQL);
			while (rs.next()){
				int i = 1;
				boolean bReachedLastColumn = false;
				try {
					while ((rs.getString(i) != null) && (bReachedLastColumn == false)){
						System.out.println(arrFieldNames.get(i - 1) + ": " + rs.getString(i));
						i++;
					}
				} catch (Exception e) {
					bReachedLastColumn = true;
				}
				System.out.println("***NEXT RECORD***");
			}
			rs.close();
		}catch (Exception ex) {
			// handle any errors
			System.out.println("Error opening resultset with SQL: " + SQL + " - " 
				+ ex.toString() + "  *-*  " + ex.getMessage());
			//return null;
			//throw new SQLException(ex.getMessage());
		}
		System.out.println("");
	}
	
	private static void readScheduleIntervals(Connection ConnectioncnACCPAC){
		String SQL = "SELECT INTERVAL FROM CSSKTB";
		try{
			Statement stmt = ConnectioncnACCPAC.createStatement();
			ResultSet rs = stmt.executeQuery(SQL);
			while (rs.next()){
				System.out.println("Schedule interval '" + rs.getInt("INTERVAL"));
			}
			rs.close();
		}catch (Exception ex) {
			// handle any errors
			System.out.println("Error opening resultset to read schedule interval with SQL: " + SQL + " - " 
				+ ex.toString() + "  *-*  " + ex.getMessage());
			//return null;
			//throw new SQLException(ex.getMessage());
		}
		
	}
	/*
	private static void comparePervasiveTables(Connection cnMSSQL61L, Connection cnPV54){
		
		ArrayList<String> arr61Tables = new ArrayList<String>(0);
		ArrayList<String> arr61Fields = new ArrayList<String>(0);
		ArrayList<String> arr54Fields = new ArrayList<String>(0);
		
		//First get the list of table we want to compare by listing the AP tables in the MS SQL (6.1) database:
		String SQL = "SELECT TABLE_NAME"
			+ " FROM INFORMATION_SCHEMA.TABLES"
			+ " WHERE TABLE_TYPE = 'BASE TABLE'"  //AND TABLE_CATALOG='dbName'"
			+ " ORDER BY TABLE_NAME"	
		;
		String SQLCount = "";
		String SQL61Fields = "";
		String SQL54Fields = "";
		try{
			Statement stmt = cnMSSQL61L.createStatement();
			ResultSet rs = stmt.executeQuery(SQL);
			while (rs.next()){
				if (rs.getString("TABLE_NAME").startsWith("AP")){
					try{
						SQLCount = "SELECT COUNT(*) AS TOTALRECORDS FROM " + rs.getString("TABLE_NAME"); 
						Statement stmtcount = cnMSSQL61L.createStatement();
						ResultSet rscount = stmtcount.executeQuery(SQLCount);
						while(rscount.next()){
							if (rscount.getLong("TOTALRECORDS") > 0L){
								//System.out.println(rs.getString("TABLE_NAME") + " has " + Long.toString(rscount.getLong("TOTALRECORDS")));
								//Add this table to the list of tables to check:
								arr61Tables.add(rs.getString("TABLE_NAME").trim());
							}
						}
					}catch(Exception ex1){
						System.out.println("Error opening resultset with SQL1: " + SQLCount + " - " 
								+ ex1.toString() + "  *-*  " + ex1.getMessage());
					}
				}
			}
			rs.close();
		}catch (Exception ex) {
			// handle any errors
			System.out.println("Error opening resultset with SQL: " + SQL + " - " 
				+ ex.toString() + "  *-*  " + ex.getMessage());
			//return null;
			//throw new SQLException(ex.getMessage());
		}
		
		//Now load the fields from each table into the 'fields list':
		for (int i = 0; i < arr61Tables.size(); i++){
			//First load the 6.1 fields from each table:
			SQL61Fields = "SELECT COLUMN_NAME,*" 
				+ " FROM INFORMATION_SCHEMA.COLUMNS"
				+ " WHERE TABLE_NAME = '" + arr61Tables.get(i) + "' AND TABLE_SCHEMA='dbo'"
			;
			arr61Fields.clear();
			try {
				Statement stmtfields = cnMSSQL61L.createStatement();
				ResultSet rsfields = stmtfields.executeQuery(SQL61Fields);
				while (rsfields.next()){
					arr61Fields.add(rsfields.getString("COLUMN_NAME").trim());
				}
				rsfields.close();
			} catch (SQLException e) {
				System.out.println("Error reading 6.1 fields from table " + arr61Tables.get(i) + " - " + e.getMessage());
			}
			
			//Now load the 5.4 fields from each table:
			SQL54Fields = "select X$Field.* from X$Field, X$File where xe$File = xf$id"
					+ " and xf$name = '" + arr61Tables.get(i) + "' and xe$DataType < 200 order by xe$offset";
			;
			arr54Fields.clear();
			try {
				Statement stmtfields = cnPV54.createStatement();
				ResultSet rsfields = stmtfields.executeQuery(SQL54Fields);
				while (rsfields.next()){
					arr54Fields.add(rsfields.getString(3).trim());
				}
				rsfields.close();
			} catch (SQLException e) {
				System.out.println("Error reading 5.4 fields from table " + arr61Tables.get(i) + " - " + e.getMessage());
			}
			
			//Sort both lists of fields
			Collections.sort(arr61Fields);
			Collections.sort(arr54Fields);
			
			//List the fields:
			//for (int j = 0; j < arr61Fields.size(); j++){
			//	System.out.println("6.1 - " + arr61Tables.get(i) + " - " + arr61Fields.get(j));
			//}
			//
			//for (int j = 0; j < arr54Fields.size(); j++){
			//	System.out.println("5.4 - " + arr61Tables.get(i) + " - " + arr54Fields.get(j));
			//}

			//Now compare the tables:
			//First see if the tables have the same number of fields:
			if (arr61Fields.size() != arr54Fields.size()){
				System.out.println("Table " + arr61Tables.get(i) + " has " + arr61Fields.size() + " fields in 6.1 and " + arr54Fields.size() + " fields in 5.4");
			}
			
			//First, go through the list of fields in 6.1 and see if they are all in 5.4
			for (int k = 0; k < arr61Fields.size(); k++){
				boolean bFieldFound = false;
				for (int l = 0; l < arr54Fields.size(); l++){
					if (arr61Fields.get(k).compareToIgnoreCase(arr54Fields.get(l)) == 0){
						bFieldFound = true;
					}
				}
				if (!bFieldFound){
					System.out.println(arr61Tables.get(i) + "." + arr61Fields.get(k) + " in 6.1 is not in 5.4");
				}
				
			}
			
			//Next, go through the list of fields in 5.4, and see if they are all in 6.1
			for (int k = 0; k < arr54Fields.size(); k++){
				boolean bFieldFound = false;
				for (int l = 0; l < arr61Fields.size(); l++){
					if (arr54Fields.get(k).compareToIgnoreCase(arr61Fields.get(l)) == 0){
						bFieldFound = true;
					}
				}
				if (!bFieldFound){
					System.out.println(arr61Tables.get(i) + "." + arr54Fields.get(k) + " in 5.4 is not in 6.1");
				}
				
			}
			
		}
		
		
	}
	*/
}