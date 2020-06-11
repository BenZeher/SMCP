package ServletUtilities;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.StringWriter;
import java.net.URL;
import java.sql.Connection;
import java.sql.Statement;
import java.util.Base64;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.net.ssl.HttpsURLConnection;

import SMDataDefinition.SMTablesystemlog;
import SMDataDefinition.SMTableusers;

public class clsOEAuthFunctions {

	private final static String LOG_OPERATION_OHDIRECTTOKEN = "OHDIRECTTOKEN";
	private final static String LOG_OPERATION_OHDIRECTREQUEST = "OHDIRECTREQUEST";
	private static final Pattern pat = Pattern.compile(".*\"access_token\"\\s*:\\s*\"([^\"]+)\".*");
	
	public static String getOHDirectToken (
		String userName, 
		String password,
		String tokenURL,
		String sClientID,
		String sClientSecret,
		Connection conn,
		String sUserID,
		String sDBID
		) throws Exception{
		
		String sAuth = sClientID + ":" + sClientSecret;
		String authentication = Base64.getEncoder().encodeToString(sAuth.getBytes());
	    String content = "grant_type=password&username=" + userName + "&password=" + password;
	    BufferedReader reader = null;
	    HttpsURLConnection connection = null;
	    String returnValue = "";
	    
	    long lStartingTime = System.currentTimeMillis();
	    
	    try {
	        URL url = new URL(tokenURL);
	        connection = (HttpsURLConnection) url.openConnection();
	        connection.setRequestMethod("POST");
	        connection.setDoOutput(true);
	        connection.setRequestProperty("Authorization", "Basic " + authentication);
	        connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
	        connection.setRequestProperty("Accept", "application/json");
	        PrintStream os = new PrintStream(connection.getOutputStream());
	        os.print(content);
	        os.close();
	        reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
	        String line = null;
	        StringWriter out = new StringWriter(connection.getContentLength() > 0 ? connection.getContentLength() : 2048);
	        while ((line = reader.readLine()) != null) {
	            out.append(line);
	        }
	        String response = out.toString();
	        Matcher matcher = pat.matcher(response);
	        if (matcher.matches() && matcher.groupCount() > 0) {
	            returnValue = matcher.group(1);
	        }
	    } catch (Exception e) {
	        throw new Exception("Error [202004225550] - could not get token - " + e.getMessage());
	    } finally {
	        if (reader != null) {
	            try {
	                reader.close();
	            } catch (IOException e) {
	            	throw new Exception("Error [202004225609] - could not read response - " + e.getMessage());
	            }
	        }
	        connection.disconnect();
	    }
	    
	    long lEndingTime = System.currentTimeMillis();
	    
	    try {
			writeEntry(
				"Getting OHDirect TOKEN for DBID:" + sDBID,
				LOG_OPERATION_OHDIRECTTOKEN,
				"[1588268769]",
				sUserID,
				Long.toString(lEndingTime - lStartingTime),
				conn, 
				sDBID);
		} catch (Exception e) {
			System.out.println("[202004305332] - Error recording token request - " + e.getMessage());
		}
	    
	    /*
		java.util.Date todaysDate = new java.util.Date();
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String formattedDate = formatter.format(todaysDate);
		
		// TJR - 4/27/2020 - leaving this line in deliberately for now to see how token logic is working...
	    System.out.println("[202004271205] Retrieving token at " + formattedDate + ": " 
	    + (System.currentTimeMillis() - lStartingTime) + " ms, token size: " + returnValue.length());
	    */
	    return returnValue;
	}
	
	public static String getOHDirectPlusRequest(
		ServletUtilities.clsOHDirectOEAuth2Token token, 
		String sRequestEndPoint,
		String sDBID,
		String sUserID,
		Connection conn) throws Exception{
	    BufferedReader reader = null;
	    String sResult = "";
	    
	    long lStartingTime = System.currentTimeMillis();
	    System.out.println("[202006111210] - sRequestEndPoint = '" + sRequestEndPoint + "', sDBID = '" + sDBID + "', sUserID = '" + sUserID + "'");
	    try {
	        URL url = new URL(token.getOHDirectRequestURLBase(sDBID) + sRequestEndPoint);
	        HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
	        connection.setRequestProperty("Authorization", "Bearer " + token.getToken(sDBID));
	        connection.setDoOutput(true);
	        connection.setRequestMethod("GET");
	        reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
	        String line = null;
	        while ((line = reader.readLine()) != null) {
	        	sResult += line;
	        }
	    } catch (Exception e) {
	    	throw new Exception("Error [1587585250] reading request - " + e.getMessage());	    
	    }
	    
	    long lEndingTime = System.currentTimeMillis();
	    
	    try {
			writeEntry(
					"Getting OHDirect REQUEST for DBID:" + sDBID + " - '" + sRequestEndPoint + "'",
					LOG_OPERATION_OHDIRECTREQUEST,
					"[1588268770]",
					sUserID,
					Long.toString(lEndingTime - lStartingTime),
					conn, 
					sDBID);
		} catch (Exception e) {
			System.out.println("[202004305332] - Error recording vendor query request - " + e.getMessage());
		}
	    
	    return sResult;
	}
	
	public static String requestOHDirectData(Connection conn, String sEndPointQuery, String sDBID, String sUserID) throws Exception{
		String sResult = "";
		
		clsOHDirectSettings ohd = new clsOHDirectSettings();
		try {
			ohd.load(conn);
		} catch (Exception e2) {
			throw new Exception("Error [202004271238] - could not load OHDirect connection settings - " + e2.getMessage());
		}
		
		clsOHDirectOEAuth2Token token = new clsOHDirectOEAuth2Token();
		try {
			sResult = clsOEAuthFunctions.getOHDirectPlusRequest(token, sEndPointQuery, sDBID, sUserID, conn);
		} catch (Exception e1) {
			//IF it fails the first time, try to refresh the token:
			try {
				token.refreshToken(conn, sDBID, sUserID);
			} catch (Exception e) {
				throw new Exception("Error [202004235737] - " + e.getMessage());
			}
			
			//Try to read OHDirect again:
			System.out.println("[202006111057] - got here....");
			try {
				sResult = clsOEAuthFunctions.getOHDirectPlusRequest(token, sEndPointQuery, sDBID, sUserID, conn);
			} catch (Exception e) {
				throw new Exception("Error [202004235806] - " + e.getMessage());
			}
		}
		return sResult;
	}
	public static String convertOHDirectDateTimeToStd(String sOHDirectDateTime) throws Exception{
		String sFormattedDate = sOHDirectDateTime;
		sFormattedDate = sFormattedDate.replace("T", " ");
		sFormattedDate = sFormattedDate.substring(0, 19);
		try {
			sFormattedDate = ServletUtilities.clsDateAndTimeConversions.convertDateFormat(
					sFormattedDate, 
					clsServletUtilities.DATETIME_24HR_FORMAT_FOR_SQL, 
					clsServletUtilities.DATETIME_FORMAT_FOR_DISPLAY,
					clsServletUtilities.EMPTY_DATETIME_VALUE);
		} catch (Exception e) {
			throw new Exception("Error [202004275337] - could not convert date '" + sOHDirectDateTime + "'");
		}
		return sFormattedDate;
	}
	
	//Write entries to the 'systemlog':
    public static boolean writeEntry (
    		String sDescription,
    		String sOperation,
    		String sReference,
    		String sUserID,
    		String sTimeInMilliseconds,
    		Connection conn,
    		String sDBID
   		) throws Exception{
    	String m_sUserID = "";
    	
    	try {
    		m_sUserID = sUserID.substring(0, SMTablesystemlog.suseridLength - 1);
		} catch (Exception e1) {
			m_sUserID = sUserID;
		}

    	String SQL = "";
		SQL = "INSERT INTO " + SMTablesystemlog.TableName
			+ " ("
	    		+ SMTablesystemlog.datloggingtime
	    		+ ", " + SMTablesystemlog.mdescription
	    		+ ", " + SMTablesystemlog.soperation
	    		+ ", " + SMTablesystemlog.mcomment
	    		+ ", " + SMTablesystemlog.suserid
	    		+ ", " + SMTablesystemlog.suserfullname
	    		+ ", " + SMTablesystemlog.sreferenceid
	    		+ ") ";
   
   		 SQL += "SELECT "
   				+ " NOW()"
   				+ ", '" + clsDatabaseFunctions.FormatSQLStatement(sDescription) + "'"
   				+ ", '" + clsDatabaseFunctions.FormatSQLStatement(sOperation) + "'"
   				+ ", '" + clsDatabaseFunctions.FormatSQLStatement(sTimeInMilliseconds) + "'"
   				+ ", '" + clsDatabaseFunctions.FormatSQLStatement(m_sUserID) + "'"
   				+ ", CONCAT(" + SMTableusers.sUserFirstName + ",\" \"," +  SMTableusers.sUserLastName + ")"
   				+ ", '" + clsDatabaseFunctions.FormatSQLStatement(sReference) + "'"
   				+ " FROM " + SMTableusers.TableName
   				+ " WHERE (";
   		 	if(clsStringFunctions.isInteger(m_sUserID.trim())){
   		 		SQL += "(" + SMTableusers.lid + "=" + m_sUserID + "))";
   		 	}else {
   		 		SQL += "(" + SMTableusers.sUserName + "='" + m_sUserID + "'))";
   		 	} 
		
		//System.out.println(SQL);
		try {
			Statement stmt = conn.createStatement();
			stmt.execute(SQL);
		} catch (Exception e) {
			throw new Exception("Error [1413217833]" + Long.toString(System.currentTimeMillis()) 
					+ " - logging operation '" + LOG_OPERATION_OHDIRECTTOKEN  
					+ " error using connnection: " + e.getMessage() + ".");
		}

    	return true;
    }
}
