package smcontrolpanel;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLEncoder;
import java.sql.Connection;
import java.util.concurrent.TimeUnit;

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

import SMClasses.SMLogEntry;
import smar.SMOption;

public class SMGeocoder {
	//All information regarding Google Maps Geocode API 
	//https://developers.google.com/maps/documentation/geocoding/intro
	
	// URL prefix to the geocoder
	public static final String GEOCODER_REQUEST_PREFIX_FOR_XML = "https://maps.google.com/maps/api/geocode/xml";
	
	//Geocode API Status responses
	public static final String OVER_QUERY_LIMIT_ERROR = "OVER_QUERY_LIMIT";
	public static final String REQUEST_DENIED_ERROR = "REQUEST_DENIED";
	public static final String ZERO_RESULTS_ERROR = "ZERO_RESULTS";
	public static final String UNKNOWN_ERROR = "UNKNOWN_ERROR";
	public static final String REQUEST_OK = "OK";
	
	//Variables for retry if errors are detected
	private static int RETRY_WAIT_TIME_MS = 1000;
	public static int NUMBER_OF_TIME_TO_RETRY = 2;
	
	public static final String EMPTY_GEOCODE = "NaN,NaN";

	private static boolean bDebugMode = false;

	public static String codeAddress (String address, Connection Connection, int iAttemptNumber)
	throws IOException,
		XPathExpressionException, 
		ParserConfigurationException, 
		SAXException,
		SocketTimeoutException,
		Exception{
		
		// query address
		// String address = "1600 Amphitheatre Parkway, Mountain View, CA";

		//return empty geocode if no address has been requested
		if(address.trim().compareToIgnoreCase("") == 0) {
			return EMPTY_GEOCODE;
		}
		
		//Get the Google API Key from system options
		SMOption smopt = new SMOption();		
		try {
			smopt.load(Connection);
		} catch (Exception e1) {
			throw new Exception("Error [1513199028] getting SM Options in Geocoder - " + e1.getMessage());
		}
		String API_KEY_PARAMETER = "";
		if(smopt.getsgoogleapikey().trim().compareToIgnoreCase("") != 0) {
			 API_KEY_PARAMETER = "&key=" + smopt.getsgoogleapikey();
		}

		// prepare a URL to the geocoder
		URL url = new URL(GEOCODER_REQUEST_PREFIX_FOR_XML + "?address=" + URLEncoder.encode(address, "UTF-8")+ API_KEY_PARAMETER + "&sensor=false");

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
		XPath xpath;
		try {
			xpath = XPathFactory.newInstance().newXPath();
		} catch (Exception e) {
			throw new XPathExpressionException("Error [1389966099] getting 'xpath' - " + e.getMessage());
		}
		
		String sXMLStatus = "";
		// extract the result
		NodeList resultNodeList = null;
		resultNodeList = (NodeList) xpath.evaluate("/GeocodeResponse/status", geocoderResultDocument, XPathConstants.NODESET);
		for(int i=0; i<resultNodeList.getLength(); ++i) {
			if (bDebugMode){
				System.out.println("In SMGeocoder.codeAddress, getting result - resultNodeList.item(i).getTextContent() = " +  resultNodeList.item(i).getTextContent());
			}
			sXMLStatus += resultNodeList.item(i).getTextContent();
		}
		//If the query limit has been reached wait a second and try again
		if(sXMLStatus.compareToIgnoreCase(OVER_QUERY_LIMIT_ERROR) == 0){
			try {
				if(iAttemptNumber <= NUMBER_OF_TIME_TO_RETRY) {
					TimeUnit.MILLISECONDS.sleep(RETRY_WAIT_TIME_MS);
					
					if (bDebugMode){
						System.out.println("Failed trying for number '" + Integer.toString(iAttemptNumber)
						+ "' in " + Integer.toString(RETRY_WAIT_TIME_MS) + "ms");
					}
					return OVER_QUERY_LIMIT_ERROR;
				}
			} catch (Exception e) {
				if (bDebugMode){
					System.out.println("In SMGeocoder.codeAddress, failed waiting for next attempt -" + e.getMessage());
				}
			}
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
		
		//Log every geocode request
		SMClasses.SMLogEntry log = new SMClasses.SMLogEntry(Connection);
		log.writeEntry(
   				"-1", 
   				SMLogEntry.LOG_OPERATION_SMGEOCODEREQUEST, 
   				"Requested Address: " + address + "\n"
   				+ "Returned GeoCode Request Status: " + sXMLStatus + "\n"
   				+ "Returned Lat: " + Float.toString(lat)+ "\n"
   				+ "Returned Lng: " + Float.toString(lng) + "\n"
   				+  " " + Integer.toString(iAttemptNumber) + " Attempts"
   				,
   				url.toString(),
   				"[1512764683]");
		
		//Detect status of request 
		if(sXMLStatus.compareToIgnoreCase(REQUEST_DENIED_ERROR) == 0) {
			throw new Exception(REQUEST_DENIED_ERROR + " returned from google geocode API. Your geocode API key may be invalid or restricted");
		}
		if(sXMLStatus.compareToIgnoreCase(OVER_QUERY_LIMIT_ERROR) == 0) {
			//TODO check for API key in system options
			throw new Exception(OVER_QUERY_LIMIT_ERROR + " returned from google geocode API. Contact your system adminstrator to obtain an API key.");
		}
		if(sXMLStatus.compareToIgnoreCase(ZERO_RESULTS_ERROR) == 0) {
			throw new Exception(ZERO_RESULTS_ERROR + " returned from google geocode API. The address you entered is invalid for google maps.");
		}
		if(sXMLStatus.compareToIgnoreCase(UNKNOWN_ERROR) == 0) {
			throw new Exception(UNKNOWN_ERROR + " returned from google geocode API. Google servers are having trouble. The request may succeed if you try again..");
		}
		
		return Float.toString(lat) + "," + Float.toString(lng);
		/*
    
    // c) extract the coordinates of the first result
    resultNodeList = (NodeList) xpath.evaluate("/GeocodeResponse/result[1]/address_component[type/text() = 'administrative_area_level_1']/country[short_name/text() = 'US']/*", geocoderResultDocument, XPathConstants.NODESET);
    float lat = Float.NaN;
    float lng = Float.NaN;
    for(int i=0; i<resultNodeList.getLength(); ++i) {
      Node node = resultNodeList.item(i);
      if("lat".equals(node.getNodeName())) lat = Float.parseFloat(node.getTextContent());
      if("lng".equals(node.getNodeName())) lng = Float.parseFloat(node.getTextContent());
    }
    System.out.println("lat/lng=" + lat + "," + lng);
		 */
	}

}
