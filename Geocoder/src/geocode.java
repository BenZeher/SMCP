import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLEncoder;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Date;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class geocode
{
	// URL prefix to the geocoder
	private static final String GEOCODER_REQUEST_PREFIX_FOR_XML = "https://maps.google.com/maps/api/geocode/xml";
	private static final int NUM_TO_PROCESS_PER_PASS = 49;
	private static final long LOOP_PAUSE_IN_MILLISECONDS = 1500;
	
	//Record Types that can be updates
	private static final String RECORD_TYPE_ORDERS = "Orders";
	private static final String RECORD_TYPE_APPOINTMENTS = "Appointments";
	
	//Geocode API Status responses
	public static final String OVER_QUERY_LIMIT_ERROR = "OVER_QUERY_LIMIT";
	public static final String REQUEST_DENIED_ERROR = "REQUEST_DENIED";
	public static final String ZERO_RESULTS_ERROR = "ZERO_RESULTS";
	public static final String UNKNOWN_ERROR = "UNKNOWN_ERROR";
	public static final String REQUEST_OK = "OK";
	
	private static boolean bDebugMode = false;
	public static void main(String[] paramArrayOfString)
	{
	
		String sDatabaseURL = "127.0.0.1"; //str1
		String sDatabaseName = "Database"; //str2
		String sUserName = "UserName";     //str3
		String sPassword = "P@ssword";     //str4
		String sNumberOfOrdersToProcess = "1000"; //str5
		String sRecordType = "Orders"; //str6
		String sAPIKey = "APIKey"; //str7

		if (paramArrayOfString.length == 0) {
			printCommandSyntax();
			return;
		}
		
		//First, get our parameters:
		if (paramArrayOfString[0].compareToIgnoreCase("") == 0){
			printCommandSyntax();
			return;
		}else{
			sDatabaseURL = paramArrayOfString[0];
		}

		if (paramArrayOfString[1].compareToIgnoreCase("") == 0){
			printCommandSyntax();
			return;
		}else{
			sDatabaseName = paramArrayOfString[1];
		}
		
		if (paramArrayOfString[2].compareToIgnoreCase("") == 0){
			printCommandSyntax();
			return;
		}else{
			sUserName = paramArrayOfString[2];
		}
		
		if (paramArrayOfString[3].compareToIgnoreCase("") == 0){
			printCommandSyntax();
			return;
		}else{
			sPassword = paramArrayOfString[3];
		}
		
		if (paramArrayOfString[4].compareToIgnoreCase("") == 0){
			printCommandSyntax();
			return;
		}else{
			sNumberOfOrdersToProcess = paramArrayOfString[4];
		}
		
		if (paramArrayOfString[5].compareToIgnoreCase("") == 0){
			printCommandSyntax();
			return;
		}else{
			sRecordType = paramArrayOfString[5];
		}
		
		if (paramArrayOfString[6].compareToIgnoreCase("") == 0){
			printCommandSyntax();
			return;
		}else{
			sAPIKey = paramArrayOfString[6];
		}

		Connection conn;
		try
		{
			conn = DriverManager.getConnection("jdbc:mysql://" 
					+ sDatabaseURL 
					+ ":3306/" 
					+ sDatabaseName 
					+ "?noDatetimeStringSync=true&connectTimeout=28800000&interactiveClient=True", sUserName, sPassword);
		}
		catch (Exception localException2) {
			try {
				Class.forName("com.mysql.jdbc.Driver").newInstance();
				conn = DriverManager.getConnection("jdbc:mysql://" + sDatabaseURL 
						+ ":3306/" 
						+ sDatabaseName 
						+ "?noDatetimeStringSync=true&connectTimeout=28800000&interactiveClient=True", sUserName, sPassword);
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
		
		if (conn == null){
			System.out.println("Could not get MySQL connection");
			return;
		}
		
		System.out.println("Successfully got MySQL connection");
		
		//Display how many records left to update
    	String SQL = "";
		if(sRecordType.compareToIgnoreCase(RECORD_TYPE_ORDERS) == 0) {
    		SQL = "SELECT " + " " + "COUNT(strimmedordernumber) AS strimmedordernumber"
    			+ " FROM " + "orderheaders"
    			+ "  WHERE ("
    			+ " (sgeocode = 'NaN,NaN')"
    			+ ")"
    			;	
    	}else if(sRecordType.compareToIgnoreCase(RECORD_TYPE_APPOINTMENTS) == 0) {
    		SQL = "SELECT " + "COUNT(lid) AS lid"
        			+ " FROM " + "appointments"
        			+ "  WHERE ("
        			+ "(sgeocode = 'NaN,NaN')"
        			+ ")"
        			;
    	}
		ResultSet rsCount;
		try {
			rsCount = openResultSet(SQL, conn);

			if(rsCount.next()) {
				if(sRecordType.compareToIgnoreCase(RECORD_TYPE_ORDERS) == 0) {
					System.out.println(rsCount.getString("strimmedordernumber") + " " + sRecordType + " left to update on " + sDatabaseName);
				}
				if(sRecordType.compareToIgnoreCase(RECORD_TYPE_APPOINTMENTS) == 0) {
					System.out.println(rsCount.getString("lid") + " " + sRecordType + " left to update on " + sDatabaseName);
				}
			}
			rsCount.close();
		
		} catch (SQLException e2) {
			System.out.println("Error getting record count!!");
		}
		//Here's where we update the geocodes:
		int iNumberProcessedPerPass = -1;
		int iTotalNumberProcessed = 0;
		while ((iNumberProcessedPerPass != 0) && (iTotalNumberProcessed < Integer.parseInt(sNumberOfOrdersToProcess))){
			try {
				iNumberProcessedPerPass = updateGeocodes(conn, sAPIKey, sRecordType, NUM_TO_PROCESS_PER_PASS);
			} catch (Exception e1) {
				System.out.println(e1.getMessage());
				return;
			}
			try {
				Thread.sleep(LOOP_PAUSE_IN_MILLISECONDS);
			} catch (InterruptedException e) {
				System.out.println("Error pausing - " + e.getMessage());
				return;
			}
			iTotalNumberProcessed += iNumberProcessedPerPass;
		}
		try {
			conn.close();
		} catch (SQLException e) {
			System.out.println("Error closing MySQL connection - " + e.getMessage());
			return;
		}
		System.out.println("Updated a total of " + iTotalNumberProcessed + " " + sRecordType+ " geocodes on " + new Date(System.currentTimeMillis()).toString());
		return;
	}
	private static void printCommandSyntax(){
		System.out.println("Command line usage: databaseURL databasename username password maxnumbertoprocess orders/appointments APIKey");
		System.out.println("For example:");
		System.out.println("geocode smdbserver01 ServMgr1 smuser smuser 2000 orders AIzaSyBy1RpeUW6xZCewNPWuu0xb_kN-Nwx5SAw");
	}
    private static int updateGeocodes(
    		Connection conn, 
    		String sAPIKey,
    		String sRecordType,
    		int iChunkSize
    		) throws Exception {
    	long lTimer = System.currentTimeMillis();
    	String SQL = "";
    	
    	if(sRecordType.compareToIgnoreCase(RECORD_TYPE_ORDERS) == 0) {
    		SQL = "SELECT " 
    			+ "sgeocode"
    			+ ", " + "sShipToAddress1"
    			+ ", " + "sShipToAddress2"
    			+ ", " + "sShipToAddress3"
    			+ ", " + "sShipToAddress4"
    			+ ", " + "sShipToCity"
    			+ ", " + "sShipToState"
    			+ ", " + "sShipToZip"
    			+ ", " + "strimmedordernumber"
    			+ " FROM " + "orderheaders"
    			+ "  WHERE ("
    			+ " (sgeocode = 'NaN,NaN')"
    			+ ")"
    			+ " LIMIT " + Integer.toString(iChunkSize)
    			;	
    	}else if(sRecordType.compareToIgnoreCase(RECORD_TYPE_APPOINTMENTS) == 0) {
    		SQL = "SELECT " 
        			+ "sgeocode"
        			+ ", " + "saddress1"
        			+ ", " + "saddress2"
        			+ ", " + "saddress3"
        			+ ", " + "saddress4"
        			+ ", " + "scity"
        			+ ", " + "sstate"
        			+ ", " + "szip"
        			+ ", " + "lid"
        			+ " FROM " + "appointments"
        			+ "  WHERE ("
        			+ "(sgeocode = 'NaN,NaN')"
        			+ ")"
        			+ " LIMIT " + Integer.toString(iChunkSize)
        			;
    	}

		ResultSet rs = openResultSet(SQL, conn);
		String sID = "";
		int iNumberProcessed = 0;
		while (rs.next()){
			//Get the geocode and update it:
			String sMapAddress = "";
			
			if(sRecordType.compareToIgnoreCase(RECORD_TYPE_ORDERS) == 0) {
				sID = rs.getString("strimmedordernumber").trim();
				sMapAddress = rs.getString("sShipToAddress1").trim();
				sMapAddress	= sMapAddress.trim() + " " + rs.getString("sShipToAddress2").trim();
				sMapAddress	= sMapAddress.trim() + " " + rs.getString("sShipToAddress3").trim();
				sMapAddress	= sMapAddress.trim() + " " + rs.getString("sShipToAddress4").trim();
				sMapAddress	= sMapAddress.trim() + " " + rs.getString("sShipToCity").trim();
				sMapAddress	= sMapAddress.trim() + " " + rs.getString("sShipToState").trim();
				sMapAddress	= sMapAddress.trim() + " " + rs.getString("sShipToZip").trim();
				
			}
			
			if(sRecordType.compareToIgnoreCase(RECORD_TYPE_APPOINTMENTS) == 0) {
				sID = rs.getString("lid").trim();
				sMapAddress = rs.getString("saddress1").trim();
				sMapAddress	= sMapAddress.trim() + " " + rs.getString("saddress2").trim();
				sMapAddress	= sMapAddress.trim() + " " + rs.getString("saddress2").trim();
				sMapAddress	= sMapAddress.trim() + " " + rs.getString("saddress2").trim();
				sMapAddress	= sMapAddress.trim() + " " + rs.getString("scity").trim();
				sMapAddress	= sMapAddress.trim() + " " + rs.getString("sstate").trim();
				sMapAddress	= sMapAddress.trim() + " " + rs.getString("szip").trim();
			}
			

			
			String sLatLng = "";
			try {
				//we do not want to send a request for a blank address. 
				if(sMapAddress.trim().compareToIgnoreCase("") != 0) {
					sLatLng = codeAddress(sID, sMapAddress, sAPIKey);	
				}else {
					System.out.println(sID + " address is blank ");
				}
				
			} catch (XPathExpressionException e) {
				//throw new SQLException("Geocoder XPathExpressionException - " + e.getMessage());
			} catch (IOException e) {
				//throw new SQLException("Geocoder IOException - " + e.getMessage());
			} catch (ParserConfigurationException e) {
				//throw new SQLException("Geocoder ParserConfigurationException - " + e.getMessage());
			} catch (SAXException e) {
				//throw new SQLException("Geocoder SAXException - " + e.getMessage());
			}
				String sUpdateSQL = "";
				if(sRecordType.compareToIgnoreCase(RECORD_TYPE_APPOINTMENTS) == 0) {
					sUpdateSQL = "UPDATE " + "appointments"
							+ " SET " + "sgeocode" + " = '" + sLatLng + "'"
							+ " WHERE (lid = '" + rs.getString("lid") + "')" 
						;
				}
				
				if(sRecordType.compareToIgnoreCase(RECORD_TYPE_ORDERS) == 0) {
					sUpdateSQL = "UPDATE " + "orderheaders"
							+ " SET " + "sgeocode" + " = '" + sLatLng + "'"
							+ " WHERE (strimmedordernumber = '" + rs.getString("strimmedordernumber") + "')" 
						;
				}

				try{
					Statement stmt = conn.createStatement();
					stmt.execute(sUpdateSQL);
				}catch (SQLException e){
					rs.close();
					throw new SQLException ("Error updating geocode with SQL: " + sUpdateSQL + " - " + e.getMessage());
				}
			
			iNumberProcessed++;
		}
		rs.close();
		System.out.println("Updated " + Integer.toString(iNumberProcessed) + " " + sRecordType //+ sOrderList 
				+ " in " + (System.currentTimeMillis() - lTimer)/1000 + " seconds.");
		return iNumberProcessed;
    }
	public static ResultSet openResultSet(String SQLStatement, Connection conn) throws SQLException{

		try{
			Statement stmt = conn.createStatement();
			//System.out.println ("SQL = " + SQLStatement); 
			ResultSet rs = stmt.executeQuery(SQLStatement);
			return rs;
		}catch (Exception ex) {
			// handle any errors
			System.out.println("Error opening resultset with SQL: " + SQLStatement + " - " 
				+ ex.toString() + "  *-*  " + ex.getMessage());
			//return null;
			throw new SQLException(ex.getMessage());
		}
	}
	private static String codeAddress (String sRecordID, String address, String sAPIKey)
	throws IOException,
		XPathExpressionException, 
		ParserConfigurationException, 
		SAXException,
		SocketTimeoutException{

		// query address
		// String address = "1600 Amphitheatre Parkway, Mountain View, CA";

		// prepare a URL to the geocoder
		URL url = new URL(GEOCODER_REQUEST_PREFIX_FOR_XML + "?address=" + URLEncoder.encode(address, "UTF-8") + "&key=" + sAPIKey +"&sensor=false");

		// prepare an HTTP connection to the geocoder
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();

		Document geocoderResultDocument = null;
		try {
			// open the connection and get results as InputSource.
			//TODO - set timeout
			conn.setConnectTimeout(5000);
			conn.connect();
			InputSource geocoderResultInputSource = new InputSource(conn.getInputStream());

			// read result and parse into XML Document
			geocoderResultDocument = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(geocoderResultInputSource);
		} finally {
			conn.disconnect();
		}

		// prepare XPath
		XPath xpath = XPathFactory.newInstance().newXPath();

		// extract the result
		NodeList resultNodeList = null;
		
		String sXMLStatus = "";
		resultNodeList = (NodeList) xpath.evaluate("/GeocodeResponse/status", geocoderResultDocument, XPathConstants.NODESET);
		for(int i=0; i<resultNodeList.getLength(); ++i) {
			if (bDebugMode){
				System.out.println("In SMGeocoder.codeAddress, getting result - resultNodeList.item(i).getTextContent() = " +  resultNodeList.item(i).getTextContent());
			}
			sXMLStatus += resultNodeList.item(i).getTextContent();
		}

		// a) obtain the formatted_address field for every result
		resultNodeList = (NodeList) xpath.evaluate("/GeocodeResponse/result/formatted_address", geocoderResultDocument, XPathConstants.NODESET);
		for(int i=0; i<resultNodeList.getLength(); ++i) {
			if (bDebugMode){
				System.out.println("In SMGeocoder.codeAddress, getting formatted address - resultNodeList.item(i).getTextContent() = " +  resultNodeList.item(i).getTextContent());
			}
		}

		// b) extract the locality for the first result
		resultNodeList = (NodeList) xpath.evaluate("/GeocodeResponse/result[1]/address_component[type/text()='locality']/long_name", geocoderResultDocument, XPathConstants.NODESET);
		for(int i=0; i<resultNodeList.getLength(); ++i) {
			if (bDebugMode){
				System.out.println("In SMGeocoder.codeAddress, getting locality - resultNodeList.item(i).getTextContent() = " +  resultNodeList.item(i).getTextContent());
			}
		}

		// c) extract the coordinates of the first result
		resultNodeList = (NodeList) xpath.evaluate("/GeocodeResponse/result[1]/geometry/location/*", geocoderResultDocument, XPathConstants.NODESET);
		float lat = Float.NaN;
		float lng = Float.NaN;
		
		for(int i=0; i<resultNodeList.getLength(); ++i) {
			Node node = resultNodeList.item(i);
			if("lat".equals(node.getNodeName())) lat = Float.parseFloat(node.getTextContent());
			if("lng".equals(node.getNodeName())) lng = Float.parseFloat(node.getTextContent());
		}
		if (bDebugMode){
			System.out.println("In SMGeocoder.codeAddress, lat/lng=" + lat + "," + lng);
		}
		
		if(sXMLStatus.compareToIgnoreCase(REQUEST_OK) != 0) {	
			if(sXMLStatus.compareToIgnoreCase(ZERO_RESULTS_ERROR) == 0) {	
				System.out.println(ZERO_RESULTS_ERROR + " for Record ID '" + sRecordID + "' using address: '" + address + "'");
				return "";
			}else {
				System.out.println("GeoCode Request failed - Status: " + sXMLStatus + " for Record ID '" + sRecordID + "' using query " + url.toString());
				return Float.toString(lat) + "," + Float.toString(lng);	
			}
		}
		System.out.println(sRecordID + " successfully updated to " + Float.toString(lat) + "," + Float.toString(lng));
		return Float.toString(lat) + "," + Float.toString(lng);

	}

}