package ServletUtilities;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.StringWriter;
import java.net.URL;
import java.util.Base64;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.net.ssl.HttpsURLConnection;

public class clsOEAuthFunctions {

	private static final Pattern pat = Pattern.compile(".*\"access_token\"\\s*:\\s*\"([^\"]+)\".*");
	
	public static String getResourceCredentials(
		String userName, 
		String password,
		String tokenURL,
		String sClientID,
		String sClientSecret
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
	    System.out.println("Time to get token: " + (System.currentTimeMillis() - lStartingTime) + "ms");
	    return returnValue;
	}
	
	public static String getOHDirectPlusRequest(
		String sTokenString, 
		String sFullRequestString) throws Exception{
	    BufferedReader reader = null;
	    String sResult = "";
	    try {
	        URL url = new URL(sFullRequestString);
	        HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
	        connection.setRequestProperty("Authorization", "Bearer " + sTokenString);
	        connection.setDoOutput(true);
	        connection.setRequestMethod("GET");
	        reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
	        String line = null;
	        //StringWriter out = new StringWriter(connection.getContentLength() > 0 ? connection.getContentLength() : 2048);
	        while ((line = reader.readLine()) != null) {
	            //out.append(line);
	        	sResult += line;
	        }
	        //String response = out.toString();
	        //System.out.println(response);
	    } catch (Exception e) {
	    	throw new Exception("Error [1587585250] reading request - " + e.getMessage());	    
	    }
	    return sResult;
	}
}
