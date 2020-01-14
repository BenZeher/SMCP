import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ACCPACICConvert
{
	public static void main(String[] paramArrayOfString)
	{
			String sDatabaseURL = "127.0.0.1"; //str1
			String sDatabaseName = "Database"; //str2
			String sUserName = "UserName";     //str3
			String sPassword = "Password";     //str4
			String sPVDatabaseURL = "127.0.0.1"; //str1
			String sPVDatabaseName = "Database"; //str2
			String sPVUserName = "UserName";     //str3
			String sPVPassword = "P@ssword";     //str4

			if (paramArrayOfString.length == 0) {
				System.out.println("[1579024711] Command line usage: ACCPACICConvert conffile command1 command2 . . . .");
				System.out.println("[1579024712] For example:");
				System.out.println("[1579024713] To run connection test: java ACCPACICConvert ACCPACIC.conf TEST");
				System.out.println("[1579024714] To run IC data conversion: java ACCPACICConvert ACCPACIC.conf IC");
				System.out.println("[1579024715] To run PO Header data conversion: java ACCPACICConvert ACCPACIC.conf POHEAD");
				System.out.println("[1579024763] To run PO Line data conversion: java ACCPACICConvert ACCPACIC.conf POLINE");
				System.out.println("[1579024767] To run PO Receipt data conversion: java ACCPACICConvert ACCPACIC.conf POREC");
				System.out.println("[1579024771] To run any combination, add options: java ACCPACICConvert ACCPACIC.conf POHEAD POLINE");
				System.out.println("[1579024773] To convert ALL data in one run: java ACCPACICConvertACCPACIC.conf  ALL");
				return;
			}
			try {
				BufferedReader localBufferedReader = new BufferedReader(new FileReader(paramArrayOfString[0]));

				String sLine;
				while ((sLine = localBufferedReader.readLine()) != null)
				{
					if (sLine.startsWith("DatabaseURL")) {
						sDatabaseURL = sLine.substring(sLine.indexOf("=") + 1);
					}
					if (sLine.startsWith("DatabaseName")) {
						sDatabaseName = sLine.substring(sLine.indexOf("=") + 1);
					}
					if (sLine.startsWith("UserName")) {
						sUserName = sLine.substring(sLine.indexOf("=") + 1);
					}
					if (sLine.startsWith("Password")) {
						sPassword = sLine.substring(sLine.indexOf("=") + 1);
					}
					//Pervasive:
					if (sLine.startsWith("PVDatabaseURL")) {
						sPVDatabaseURL = sLine.substring(sLine.indexOf("=") + 1);
					}
					if (sLine.startsWith("PVDatabaseName")) {
						sPVDatabaseName = sLine.substring(sLine.indexOf("=") + 1);
					}
					if (sLine.startsWith("PVUserName")) {
						sPVUserName = sLine.substring(sLine.indexOf("=") + 1);
					}
					if (sLine.startsWith("PVPassword")) {
						sPVPassword = sLine.substring(sLine.indexOf("=") + 1);
					}
				}

				localBufferedReader.close();
			} catch (FileNotFoundException e) {
				System.out.println("[1579024775] File not found - " + e.getMessage());
				return;
			} catch (IOException e) {
				System.out.println("[1579024780] File IO exception - " + e.getMessage());
				return;
			}

			Connection conn;
			try
			{
				conn = DriverManager.getConnection("jdbc:mysql://" + sDatabaseURL + ":3306/" + sDatabaseName + "?noDatetimeStringSync=true&connectTimeout=28800000&interactiveClient=True", sUserName, sPassword);
			}
			catch (Exception localException2) {
				try {
					Class.forName("com.mysql.jdbc.Driver").newInstance();
					conn = DriverManager.getConnection("jdbc:mysql://" + sDatabaseURL + ":3306/" + sDatabaseName + "?noDatetimeStringSync=true&connectTimeout=28800000&interactiveClient=True", sUserName, sPassword);
				} catch (InstantiationException e) {
					System.out.println("[1579024784] InstantiationException getting MySQL connection - " + e.getMessage());
					return;
				} catch (IllegalAccessException e) {
					System.out.println("[1579024788] IllegalAccessException getting MySQL connection - " + e.getMessage());
					return;
				} catch (ClassNotFoundException e) {
					System.out.println("[1579024790] ClassNotFoundException getting MySQL connection - " + e.getMessage());
					return;
				} catch (SQLException e) {
					System.out.println("[1579024793] SQLException getting MySQL connection - " + e.getMessage());
					return;
				}
			}
			
			if (conn == null){
				System.out.println("[1579024797] Could not get MySQL connection");
				return;
			}
			
			System.out.println("[1579024800] Successfully got MySQL connection");
			//Pervasive connection
			Connection cnACCPAC;
			try
			{
				cnACCPAC = DriverManager.getConnection("jdbc:pervasive://" + sPVDatabaseURL + ":1583/" + sPVDatabaseName + "", sPVUserName, sPVPassword);
			}
			catch (Exception localException2) {
				try {
					Class.forName("com.pervasive.jdbc.v2.Driver").newInstance();
					cnACCPAC = DriverManager.getConnection("jdbc:pervasive://" + sPVDatabaseURL + ":1583/" + sPVDatabaseName + "", sPVUserName, sPVPassword);
				} catch (InstantiationException e) {
					System.out.println("[1579024794] InstantiationException getting ACCPAC connection - " + e.getMessage());
					return;
				} catch (IllegalAccessException e) {
					System.out.println("[1579024804] IllegalAccessException getting ACCPAC connection - " + e.getMessage());
					return;
				} catch (ClassNotFoundException e) {
					System.out.println("[1579024807] ClassNotFoundException getting ACCPAC connection - " + e.getMessage());
					return;
				} catch (SQLException e) {
					System.out.println("[1579024811] SQLException getting ACCPAC connection - " + e.getMessage());
					return;
				}
			}
			
			if (cnACCPAC == null){
				System.out.println("[1579024813] Could not get Pervasive connection");
				return;
			}
			
			System.out.println("[1579024817] Successfully got Pervasive connection");
			
			insertRecords conv = new insertRecords(new PrintWriter(System.out));
			
			//Look for a TEST parameter:
			for (int i = 0; i < paramArrayOfString.length; i++){
				if (paramArrayOfString[i].compareToIgnoreCase("TEST") == 0){
					System.out.println("[1579024820] Program started in test mode and is exiting now");
					closeConnections(conn, cnACCPAC, conv);
					return;
				}
				if (paramArrayOfString[i].compareToIgnoreCase("ALL") == 0){
					if (!convertAll(conv, conn, cnACCPAC, "", "AIRO")){
						System.out.println("[1579024824] Could not convert data");
						System.out.println(conv.getErrorMessage());
					}
					closeConnections(conn, cnACCPAC, conv);
					return;
				}
			}
			
			//Otherwise, we'll do the conversions one at a time:
			for (int i = 0; i < paramArrayOfString.length; i++){
				if (paramArrayOfString[i].compareToIgnoreCase("IC") == 0){
					if (!conv.convertICData(conn, cnACCPAC, "", sUserName)){
						System.out.println("[1579024827] Could not convert IC data");
						System.out.println(conv.getErrorMessage());
					}
				}
				if (paramArrayOfString[i].compareToIgnoreCase("POHEAD") == 0){
					if (!conv.convertPOHeaderData(conn, cnACCPAC, "", sUserName)){
						System.out.println("[1579024829] Could not convert PO Header data");
						System.out.println(conv.getErrorMessage());
					}
				}
				if (paramArrayOfString[i].compareToIgnoreCase("POLINE") == 0){
					if (!conv.convertPOHeaderData(conn, cnACCPAC, "", sUserName)){
						System.out.println("[1579024832] Could not convert PO Line data");
						System.out.println(conv.getErrorMessage());
					}
				}
				if (paramArrayOfString[i].compareToIgnoreCase("POREC") == 0){
					if (!conv.convertPOHeaderData(conn, cnACCPAC, "", sUserName)){
						System.out.println("[1579024836] Could not convert PO Receipt data");
						System.out.println(conv.getErrorMessage());
					}
				}
			}
			closeConnections(conn, cnACCPAC, conv);
			return;
	}
	private static void closeConnections(Connection conn, Connection cnACCPAC, insertRecords conv){
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
		conv = null;
		return;
	}
	private static boolean convertAll(
			insertRecords conv,
			Connection conn, 
			Connection conACCPAC, 
			String sSessionTag, 
			String sUserName
			){
		
		if (!conv.convertICData(conn, conACCPAC, sSessionTag, sUserName)){
    		return false;
    	}
		
		if (!conv.convertPOHeaderData(conn, conACCPAC, sSessionTag, sUserName)){
    		return false;
    	}
		
		if (!conv.convertPOLineData(conn, conACCPAC, sSessionTag, sUserName)){
    		return false;
    	}

		if (!conv.convertPOReceiptData(conn, conACCPAC, sSessionTag, sUserName)){
    		return false;
    	}

		return true;
	}
}