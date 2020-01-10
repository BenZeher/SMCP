package smcontrolpanel;

import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import SMClasses.SMOrderHeader;
import SMClasses.SMWorkOrderHeader;
import SMDataDefinition.SMGoogleMapAPIKey;
import SMDataDefinition.SMTablecolortable;
import SMDataDefinition.SMTablecompanyprofile;
import SMDataDefinition.SMTablelocations;
import SMDataDefinition.SMTablemechanics;
import SMDataDefinition.SMTableorderheaders;
import SMDataDefinition.SMTableworkorders;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsManageRequestParameters;

public class SMMapDisplay extends HttpServlet {

	private static final long serialVersionUID = 1L;
	private boolean bDebugMode = false;
	
	public void doPost(HttpServletRequest request,
			HttpServletResponse response)
	throws ServletException, IOException {

		if (!SMAuthenticate.authenticateSMCPCredentials(
				request, 
				response, 
				getServletContext(), 
				SMSystemFunctions.SMViewTruckSchedules)
		){
			return;
		}

		PrintWriter out = response.getWriter();
		HttpSession CurrentSession = request.getSession(true);
		String sDBID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_DATABASE_ID);
		String sUserID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERID);
		String sUserFullName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERFIRSTNAME) + " "
						+ (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERLASTNAME);
		
		String sCenterAddress = "";
		
		try {
			sCenterAddress = getMapCenterAddress(sDBID, sUserID, sUserFullName, getServletContext());
		} catch (SQLException e) {
			out.println("<BR>Error reading company profile - " + e.getMessage());
		}
		
		String sScheduleDate = clsManageRequestParameters.get_Request_Parameter(
				SMViewTruckScheduleReport.MAPDATEPARAMETER, request);
		
		String sMechanic = clsManageRequestParameters.get_Request_Parameter(
				SMViewTruckScheduleSelection.MECHANIC_PARAMETER, request);
		//Load the remaining request parameters:
    	//Get the list of selected locations:
    	ArrayList<String> sLocations = new ArrayList<String>(0);
	    Enumeration<String> paramLocationNames = request.getParameterNames();
	    String sParamLocationName = "";
	    String sLocationMarker = SMViewTruckScheduleSelection.LOCATION_PARAMETER;
	    while(paramLocationNames.hasMoreElements()) {
	    	sParamLocationName = paramLocationNames.nextElement();
		  if (sParamLocationName.contains(sLocationMarker)){
			  sLocations.add(sParamLocationName.substring(
					  sParamLocationName.indexOf(sLocationMarker) + sLocationMarker.length()));
		  }
	    }
	    Collections.sort(sLocations);
	    
	    ArrayList<String> arrLocationAddress = new ArrayList<String>(0);
	    try {
			arrLocationAddress = getLocationAddresses(sLocations, sDBID, sUserID, sUserFullName, getServletContext());
		} catch (SQLException e1) {
			out.println("<BR>Error reading location addresses - " + e1.getMessage());
		}
		
    	//Get the list of selected order types:
    	ArrayList<String> sServiceTypes = new ArrayList<String>(0);
		Enumeration<String> paramNames = request.getParameterNames();
	    String sParamName = "";
	    String sMarker = SMViewTruckScheduleSelection.SERVICETYPE_PARAMETER;
	    while(paramNames.hasMoreElements()) {
	      sParamName = paramNames.nextElement();
		  if (sParamName.contains(sMarker)){
			  sServiceTypes.add(sParamName.substring(sParamName.indexOf(sMarker) + sMarker.length()));
		  }
	    }
	    Collections.sort(sServiceTypes);
		
		//Load the array of addresses, titles, open/closed flags, and info window strings:
		ArrayList<String>arrMechanics = new ArrayList<String> (0);
		ArrayList<String>arrGeoCodes = new ArrayList<String> (0);
		ArrayList<String>arrMarkerTitles = new ArrayList<String> (0);
		ArrayList<String>arrOpenClosedFlags = new ArrayList<String> (0);
		ArrayList<String>arrInfoStrings = new ArrayList<String> (0);
		Connection conn = clsDatabaseFunctions.getConnection(
				getServletContext(), 
				sDBID, 
				"MySQL", 
				this.toString() 
				+ ".loadScheduleEntries - userID: " 
				+ sUserID
				+ " - "
				+ sUserFullName);
		if (conn == null){
			out.println("<BR>Error getting data connection.");
			return;
		}
		
		try {
			loadScheduleEntries(
				conn,
				sDBID, 
				sUserID, 
				getServletContext(),
				sScheduleDate,
				sMechanic,
				sLocations,
				sServiceTypes,
				arrMechanics,
				arrGeoCodes,
				arrMarkerTitles,
				arrOpenClosedFlags,
				arrInfoStrings,
				(String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_LICENSE_MODULE_LEVEL)
				);
		} catch (SQLException e) {
			out.println("<BR>Error reading schedule entries - " + e.getMessage());
		}
		out.println(getMapDisplayScript(
				arrLocationAddress,
				sCenterAddress, 
				arrMechanics,
				arrGeoCodes, 
				arrMarkerTitles,
				arrOpenClosedFlags,
				arrInfoStrings,
				getServletContext(),
				conn)
		);
		clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1547080588]");
		return;
	}
	private String getMapCenterAddress(String sDBID, String sUserID, String sUserFullName, ServletContext context) throws SQLException{
		String sCenterAddress = "";
		String SQL = "SELECT * FROM " + SMTablecompanyprofile.TableName;
		ResultSet rs = clsDatabaseFunctions.openResultSet(
				SQL, 
				context, 
				sDBID, 
				"MySQL", 
				this.toString() + ".getMapCenterAddress - user: " 
				+ sUserID
				+ " - "
				+ sUserFullName
				);
		if (rs.next()){
			sCenterAddress += rs.getString(SMTablecompanyprofile.sAddress01).trim()
				+ " " + rs.getString(SMTablecompanyprofile.sAddress02).trim()
				+ " " + rs.getString(SMTablecompanyprofile.sAddress03).trim()
				+ " " + rs.getString(SMTablecompanyprofile.sAddress04).trim()
				+ " " + rs.getString(SMTablecompanyprofile.sCity).trim()
				+ " " + rs.getString(SMTablecompanyprofile.sState).trim()
				+ " " + rs.getString(SMTablecompanyprofile.sZipCode).trim()
				+ " " + rs.getString(SMTablecompanyprofile.sCountry).trim()
			;
		}else{
			throw new SQLException("No record when reading company profile");
		}
		rs.close();
		return sCenterAddress;
	}
	
	
	private ArrayList<String> getLocationAddresses(ArrayList <String> aLocationNames, String sDBID, String sUserID, String sUserFullName, ServletContext context) throws SQLException{
		
		 ArrayList<String> sLocationAddress = new ArrayList<String>(0);
		String SQL = "SELECT * FROM " + SMTablelocations.TableName
				+ " WHERE (";
		for (int i = 0; i < aLocationNames.size(); i++) {
			if(i != 0) {
				SQL += " AND ";
			}
			SQL += "(" + SMTablelocations.sLocation + "=" + "'" + aLocationNames.get(i) + "')";
		}
		SQL += ")";
		
		//System.out.println(SQL);
		ResultSet rs = clsDatabaseFunctions.openResultSet(
				SQL, 
				context, 
				sDBID, 
				"MySQL", 
				this.toString() + ".getLocationAddress - user: " 
				+ sUserID
				+ " - "
				+ sUserFullName
				);
		while (rs.next()){
			sLocationAddress.add(rs.getString(SMTablelocations.sAddress1).trim()
				+ " " + rs.getString(SMTablelocations.sAddress2).trim()
				+ " " + rs.getString(SMTablelocations.sAddress3).trim()
				+ " " + rs.getString(SMTablelocations.sAddress4).trim()
				+ " " + rs.getString(SMTablelocations.sCity).trim()
				+ " " + rs.getString(SMTablelocations.sState).trim()
				+ " " + rs.getString(SMTablelocations.sZip).trim()
				+ " " + rs.getString(SMTablelocations.sCountry).trim());
			
		}
		rs.close();
		return sLocationAddress;
	}
	
	private void loadScheduleEntries(
		Connection conn,
		String sDBID, 
		String sUserID, 
		ServletContext context,
		String sScheduleDate,
		String sMechanic,
		ArrayList <String> arrLocations,
		ArrayList <String> arrServiceTypes,
		ArrayList <String> arrMechanics,
		ArrayList <String> arrGeoCodes,
		ArrayList <String> arrMarkerTitles,
		ArrayList <String> arrOpenClosedFlags,
		ArrayList <String> arrInfoWindowStrings,
		String sLicenseModuleLevel
		) throws SQLException{
		
		String sTempDayTable = SMViewTruckScheduleReport.TEMPTABLE_BASE + Long.toString(System.currentTimeMillis());
		SMViewTruckScheduleReport tsr = new SMViewTruckScheduleReport();
		try {
			tsr.createTemporaryTruckScheduleTable(
					conn,
					arrLocations,
					arrServiceTypes,
					sScheduleDate,
					sScheduleDate,
					false,
					sMechanic,
					sTempDayTable,
					context)
			;
		} catch (Exception e1) {
			try {
				Statement stmt = conn.createStatement();
				stmt.execute("DROP TABLE IF EXISTS " + sTempDayTable);
			} catch (SQLException e) {
				//Don't stop for this:
			}
			throw new SQLException("Error [1428686479] could not create truck schedule - " + e1.getMessage());
		}
		
		//Get permissions:
		//Check permissions for viewing orders:
		boolean bViewOrderPermitted = SMSystemFunctions.isFunctionPermitted(
				SMSystemFunctions.SMViewOrderInformation,
				sUserID,
				conn,
				sLicenseModuleLevel);
		
		//Check permissions for editing schedule:
		boolean bConfigureWorkOrdersPermitted = SMSystemFunctions.isFunctionPermitted(
				SMSystemFunctions.SMConfigureWorkOrders,
				sUserID,
				conn,
				sLicenseModuleLevel);
		
		String SQL = "SELECT "
			+ sTempDayTable + ".*"
			+ ", " + SMTableworkorders.TableName + "." + SMTableworkorders.ijoborder
			+ ", " + SMTableworkorders.TableName + "." + SMTableworkorders.sassistant
			+ ", " + SMTableworkorders.TableName + "." + SMTableworkorders.strimmedordernumber
			+ ", " + SMTableworkorders.TableName + "." + SMTableworkorders.smechanicinitials
			+ ", " + SMTableworkorders.TableName + "." + SMTableworkorders.sschedulecomment
			+ ", " + SMTableworkorders.TableName + "." + SMTableworkorders.sscheduledbyfullname
			+ ", " + SMTableworkorders.TableName + "." + SMTableworkorders.lscheduledbyuserid
			+ ", " + SMTableworkorders.TableName + "." + SMTableworkorders.sstartingtime
			+ ", " + SMTableorderheaders.TableName + "." + SMTableorderheaders.sBillToName
			+ ", " + SMTableorderheaders.TableName + "." + SMTableorderheaders.sShipToName
			+ ", " + SMTableorderheaders.TableName + "." + SMTableorderheaders.sShipToAddress1
			+ ", " + SMTableorderheaders.TableName + "." + SMTableorderheaders.sShipToAddress2
			+ ", " + SMTableorderheaders.TableName + "." + SMTableorderheaders.sShipToAddress3
			+ ", " + SMTableorderheaders.TableName + "." + SMTableorderheaders.sShipToAddress4
			+ ", " + SMTableorderheaders.TableName + "." + SMTableorderheaders.sShipToCity
			+ ", " + SMTableorderheaders.TableName + "." + SMTableorderheaders.sShipToState
			+ ", " + SMTableorderheaders.TableName + "." + SMTableorderheaders.sShipToCountry
			+ ", " + SMTableorderheaders.TableName + "." + SMTableorderheaders.sShipToZip
			+ ", " + SMTableorderheaders.TableName + "." + SMTableorderheaders.sSalesperson
			+ ", " + SMTableorderheaders.TableName + "." + SMTableorderheaders.sServiceTypeCodeDescription
			+ ", " + SMTableorderheaders.TableName + "." + SMTableorderheaders.sgeocode
			+ ", " + SMTablemechanics.TableName + "." + SMTablemechanics.sMechFullName
			+ ", " + SMTablemechanics.TableName + "." + SMTablemechanics.sMechColorCodeRow
			+ ", " + SMTablemechanics.TableName + "." + SMTablemechanics.sMechColorCodeCol
			+ " FROM " + sTempDayTable + " LEFT JOIN " + SMTableworkorders.TableName + " ON "
			+ sTempDayTable + "." + SMViewTruckScheduleReport.TEMPTABLE_FIELD_WORKORDERID
			+ " = " + SMTableworkorders.TableName + "." + SMTableworkorders.lid
			+ " LEFT JOIN " + SMTableorderheaders.TableName + " ON "
			+ sTempDayTable + "." + SMViewTruckScheduleReport.TEMPTABLE_FIELD_ORDERNUMBER
			+ " = " + SMTableorderheaders.TableName + "." + SMTableorderheaders.strimmedordernumber
			+ " LEFT JOIN " + SMTablemechanics.TableName + " ON "
			+ sTempDayTable + "." + SMViewTruckScheduleReport.TEMPTABLE_FIELD_MECHID
			+ " = " + SMTablemechanics.TableName + "." + SMTablemechanics.lid
			
			//ONLY get records with real jobs
			+ " WHERE ("
				+ "(" + SMTableorderheaders.TableName + "." + SMTableorderheaders.strimmedordernumber
				+ " IS NOT NULL)"
			+ ")"
			+ " ORDER BY "
			+ SMTableorderheaders.TableName + "." + SMTableorderheaders.sgeocode
			+ ", " + SMTableorderheaders.TableName + "." + SMTableorderheaders.sShipToAddress1
			+ ", " + SMTableorderheaders.TableName + "." + SMTableorderheaders.sShipToAddress2
			+ ", " + SMTableorderheaders.TableName + "." + SMTableorderheaders.sShipToAddress3
			+ ", " + SMTableorderheaders.TableName + "." + SMTableorderheaders.sShipToAddress4
			+ ", " + SMTableorderheaders.TableName + "." + SMTableorderheaders.sShipToCity
			+ ", " + SMTableorderheaders.TableName + "." + SMTableorderheaders.sShipToState
			+ ", " + SMTableorderheaders.TableName + "." + SMTableorderheaders.sShipToZip
			;
		//if (bDebugMode){
		//System.out.println("In " + this.toString() + ".loadScheduleEntries - SQL = '" + SQL + "'");
		//}
		try {
			ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
			while (rs.next()){
				boolean bGeocodeIsRepeated = false;
				//TJR - only for debugging:
				//if (iCounter > 29){break;}
				String sMapAddress = rs.getString(SMTableorderheaders.TableName + "." 
						+ SMTableorderheaders.sShipToAddress1).trim();
				sMapAddress	= sMapAddress.trim() + " " + rs.getString(SMTableorderheaders.TableName + "." 
						+ SMTableorderheaders.sShipToAddress2).trim();
				sMapAddress	= sMapAddress.trim() + " " + rs.getString(SMTableorderheaders.TableName + "." 
						+ SMTableorderheaders.sShipToAddress3).trim();
				sMapAddress	= sMapAddress.trim() + " " + rs.getString(SMTableorderheaders.TableName + "." 
						+ SMTableorderheaders.sShipToAddress4).trim();
				sMapAddress	= sMapAddress.trim() + " " + rs.getString(SMTableorderheaders.TableName + "." 
						+ SMTableorderheaders.sShipToCity).trim();
				sMapAddress	= sMapAddress.trim() + " " + rs.getString(SMTableorderheaders.TableName + "." 
						+ SMTableorderheaders.sShipToState).trim();
				sMapAddress	= sMapAddress.trim() + " " + rs.getString(SMTableorderheaders.TableName + "." 
						+ SMTableorderheaders.sShipToZip).trim();
				//sMapAddress	= sMapAddress.trim() + " " + rs.getString(SMTableorderheaders.TableName + "." 
				//		+ SMTableorderheaders.sShipToCountry).trim();
				
				//Keep a list of mechanics for legend display on the map.
				String sMechanicDesc = rs.getString(SMTableworkorders.TableName + "." + SMTableworkorders.smechanicinitials) + " - "
									 + rs.getString(SMTablemechanics.TableName + "." + SMTablemechanics.sMechFullName).replace("'","\\\'");
				if (arrMechanics.indexOf(sMechanicDesc) < 0){
					arrMechanics.add(sMechanicDesc);
				}
				
				//IF there is a geocode in the order header use it, if not, this work order will not be displayed on the map:
				String sGeoCode = rs.getString(SMTableorderheaders.TableName + "." + SMTableorderheaders.sgeocode).trim();
				
				if(sGeoCode.trim().compareToIgnoreCase("") == 0) {
					sGeoCode = SMGeocoder.EMPTY_GEOCODE;
				}
				/*Removed 12/11/17 in order to reduce geocode requests
				String sLatLng = "";
				if (sGeoCode.compareToIgnoreCase("") == 0){
					try {
						sLatLng = SMGeocoder.codeAddress(sMapAddress, conn);
						
						SMClasses.SMLogEntry log = new SMClasses.SMLogEntry(conn);
						log.writeEntry(
							sUserName, 
							SMLogEntry.LOG_OPERATION_GEOCODEREQUEST, 
							" Work Order Map Display" + "\n"
							+ "Requested Address: " + sMapAddress + "\n"
							+ "Returned Lat/Lng: " + sLatLng + "\n"
							,
							"SMMapDisplay.loadScheduleEntries",
							"[15127664332]");
						
						//System.out.println("[1345133388] new GeoCode = " + sLatLng);
						String sUpdateSQL = "UPDATE " + SMTableorderheaders.TableName
							+ " SET " + SMTableorderheaders.sgeocode + " = '" + sLatLng + "'"
							+ " WHERE ("
								+ "(" + SMTableorderheaders.strimmedordernumber + " = '" 
								+ rs.getString(SMTableworkorders.TableName + "." + SMTableworkorders.strimmedordernumber) + "')"
							+ ")"
							;
						Statement stmt = conn.createStatement();
						stmt.execute(sUpdateSQL);

					} catch (XPathExpressionException e) {
						System.out.println("[1345133388] XPathExpressionException");
						Statement stmt = conn.createStatement();
						stmt.execute("DROP TABLE IF EXISTS " + sTempDayTable);
						throw new SQLException("Geocoder XPathExpressionException - " + e.getMessage());
					} catch (IOException e) {
						System.out.println("[1345133388] IOException");
						Statement stmt = conn.createStatement();
						stmt.execute("DROP TABLE IF EXISTS " + sTempDayTable);
						throw new SQLException("Geocoder IOException - " + e.getMessage());
					} catch (ParserConfigurationException e) {
						System.out.println("[1345133388] ParserConfigurationException");
						Statement stmt = conn.createStatement();
						stmt.execute("DROP TABLE IF EXISTS " + sTempDayTable);
						throw new SQLException("Geocoder ParserConfigurationException - " + e.getMessage());
					} catch (SAXException e) {
						System.out.println("[1345133388] SAXException");
						Statement stmt = conn.createStatement();
						stmt.execute("DROP TABLE IF EXISTS " + sTempDayTable);
						throw new SQLException("Geocoder SAXException - " + e.getMessage());
					}
					sGeoCode = sLatLng;
				}
				*/	
				//TODO: add mechanic's color code to it. 
				//After this point, gecode will look like: (Lng, Lat, Color)
				String sMechColorCode = rs.getString(SMTablemechanics.TableName + "." + SMTablemechanics.sMechColorCodeRow) + ", " + 
										rs.getString(SMTablemechanics.TableName + "." + SMTablemechanics.sMechColorCodeCol);
				if (sMechColorCode.length() == 0){
					sGeoCode += ", 0, 0";
				}else{
					sGeoCode += ", " + sMechColorCode;
				}
				
				//See if this geocode has already been added:
				if (arrGeoCodes.size() > 0){
					if (bDebugMode){
						System.out.println(
							"In " + this.toString() + ".loadScheduleEntries - "
							+ "previous geocode was: '" + arrGeoCodes.get(arrGeoCodes.size() - 1) + "'"
							+ ", current geocode is: '" + sGeoCode + "'."
						);
					}
					if (sGeoCode.compareToIgnoreCase(arrGeoCodes.get(arrGeoCodes.size() - 1)) == 0){
						bGeocodeIsRepeated = true;
					}else{
						arrGeoCodes.add(sGeoCode);
					}
					if (bDebugMode){
						System.out.println(
							"In " + this.toString() + ".loadScheduleEntries - "
							+ "bGeoCodeRepeated = " + bGeocodeIsRepeated + "."
						);
					}
				}else{
					arrGeoCodes.add(sGeoCode);
				}
				
				if (bGeocodeIsRepeated){
					arrMarkerTitles.set(arrMarkerTitles.size() - 1, arrMarkerTitles.get(arrMarkerTitles.size() - 1)
						+ "; "
						+ rs.getString(SMTablemechanics.TableName + "." + SMTablemechanics.sMechFullName) 
						+ " - Job #" + Long.toString(rs.getLong(SMTableworkorders.TableName + "." 
								+ SMTableworkorders.ijoborder))
					); 
				}else{
					arrMarkerTitles.add(
							rs.getString(SMTablemechanics.TableName + "." + SMTablemechanics.sMechFullName) 
						+ " - Job #" + Long.toString(rs.getLong(SMTableworkorders.TableName + "." + SMTableworkorders.ijoborder)));
					if (rs.getBigDecimal(sTempDayTable + "." 
						+ SMViewTruckScheduleReport.TEMPTABLE_FIELD_TOTALHOURS).compareTo(BigDecimal.ZERO) == 0){
						arrOpenClosedFlags.add("O");
					}else{
						arrOpenClosedFlags.add("C");
					}
				}
				String sEditJobCostLink = rs.getString(SMTablemechanics.TableName + "." 
						+ SMTablemechanics.sMechFullName)+ "&nbsp;Job:&nbsp;#"
						+ Long.toString(rs.getLong(SMTableworkorders.TableName + "." + SMTableworkorders.ijoborder));
				
				if (bConfigureWorkOrdersPermitted){
					sEditJobCostLink = "<A HREF=\""
						+ SMUtilities.getURLLinkBase(context) 
						+ "smcontrolpanel.SMConfigWorkOrderEdit"
						+ "?" + SMWorkOrderHeader.Paramlid + "=" 
						+ rs.getString(sTempDayTable + "." + SMViewTruckScheduleReport.TEMPTABLE_FIELD_WORKORDERID)
						+ "&" + SMConfigWorkOrderEdit.REMOVE_WORK_ORDER_ATTRIBUTE_FROM_SESSION + "=Y"
						+ "&CallingClass=smcontrolpanel.SMMapDisplay"

						//This parameter will tell the 'action' class to bring us back here
						//after the editing:
						+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
						+ "\">"
						+ sEditJobCostLink 
						+ "</A>"
						;
				}
				String sOrderNumber = rs.getString(SMTableworkorders.TableName + "." + SMTableworkorders.strimmedordernumber);
				String sOrderNumberLink = "";
				if (bViewOrderPermitted){
					sOrderNumberLink = 
						"<A HREF=\"" + SMUtilities.getURLLinkBase(context) 
						+ "smcontrolpanel.SMDisplayOrderInformation"
						+ "?" + SMOrderHeader.ParamsOrderNumber + "=" + sOrderNumber 
						+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID + "\">" + sOrderNumber + "</A>"
						;
				}else{
					sOrderNumberLink = sOrderNumber;
				}
				
				String sServiceType = rs.getString(SMTableorderheaders.TableName + "." 
						+ SMTableorderheaders.sServiceTypeCodeDescription);
				if (sServiceType == null){
					sServiceType = "";
				} 
				
				String sInfoWindow = ""
					+ sEditJobCostLink
					+ "&nbsp;-&nbsp;Order&nbsp;#&nbsp;" 
					+ sOrderNumberLink + "&nbsp;" 
					+ "<B><FONT COLOR=" + SMViewTruckScheduleReport.SERVICETYPE_COLOR + ">"
					+ sServiceType + "&nbsp;</FONT></B>"
					+ "<BR>LOCATION:&nbsp;" + sMapAddress
					+ "<BR>CUSTOMER:&nbsp;" + rs.getString(SMTableorderheaders.TableName + "." + SMTableorderheaders.sBillToName)
					+ "<BR>SALESPERSON:&nbsp;" + rs.getString(SMTableorderheaders.TableName + "." + SMTableorderheaders.sSalesperson)
					+ "<BR>COMMENT: " 
						+ rs.getString(SMTableworkorders.TableName + "." 
						+ SMTableworkorders.sschedulecomment).replace("\n", "<BR>").replace("\r", "<BR>").replace("\t", "&nbsp;")
				;

				if (bGeocodeIsRepeated){
					arrInfoWindowStrings.set(arrInfoWindowStrings.size() - 1, 
						arrInfoWindowStrings.get(arrInfoWindowStrings.size() - 1)
						+ "<BR>"
						+ sInfoWindow.replace(",", "").replace("\"", "'")
					);
				}else{
					arrInfoWindowStrings.add(sInfoWindow.replace(",", "").replace("\"", "'"));
				}
			}
			rs.close();
		} catch (SQLException e) {
			try {
				Statement stmt = conn.createStatement();
				stmt.execute("DROP TABLE IF EXISTS " + sTempDayTable);
			} catch (SQLException e1) {
				//Don't stop for this:
			}
			throw new SQLException("SQL = '" + SQL + "' - error: " + e.getMessage());
		}
		//Make sure we drop the table:
		try {
			Statement stmt = conn.createStatement();
			stmt.execute("DROP TABLE IF EXISTS " + sTempDayTable);
		} catch (SQLException e) {
			//Don't stop for this:
		}
	}
	private String getMapDisplayScript(
			ArrayList<String>arrLocationAddress,
			String sCenterAddress, 
			ArrayList<String>arrMechanics,
			ArrayList<String>arrGeocodes,
			ArrayList<String>arrMarkerTitles,
			ArrayList<String>arrOpenClosedFlags,
			ArrayList<String>arrInfoStrings,
			ServletContext context,
			Connection conn
		){
		//String sMarkerInterval = "100";
		if (true){
			//sMarkerInterval = "10";
		}
		
		//Calculate a marker interval based on the size of the array that won't choke Google's query limit:
		/*//
		switch (arrGeocodes.size()/10){
		case 0:
			sMarkerInterval = "200";
		case 1:
			sMarkerInterval = "200";
		case 2:
			sMarkerInterval = "200";
		case 3:
			sMarkerInterval = "200";
		case 4:
			sMarkerInterval = "600";
		default:
			sMarkerInterval = "800";
		}
		*/
		String s = "";
		s+= 
			"<!DOCTYPE html>\n"
			+ "<html>\n"
			+ "<head>\n"
			+ "  <meta name=\"viewport\" content=\"initial-scale=1.0, user-scalable=no\" />\n"
			+ "  <style type=\"text/css\">\n"
			+ "    html { height: 100% }\n"
			+ "    body { height: 100%; margin: 0; padding: 0 }\n"
			+ "    #map_canvas { height: 100% }\n"
			
			+ "    #legend {\n"
			+ "    background: #FFF;\n"
			+ "    padding: 10px;\n"
			+ "    margin: 5px;\n"
			+ "    font-size: 10px;\n"
			+ "    font-family: Arial, sans-serif;\n"
			+ "    }\n"

			+ "    #legend p {\n"
			+ "    font-size: 15px;\n"
			+ "    font-weight: bold;\n"
			+ "    }\n"

			+ "    #legend div {\n"
			+ "    padding-bottom: 5px;\n"
			+ "    }\n"

			+ "    .color {\n"
			+ "    border: 1px solid;\n"
			+ "    height: 12px;\n"
			+ "    width: 12px;\n"
			+ "    margin-right: 3px;\n"
			+ "    float: left;\n"
			+ "    }\n"

			+ "  </style>\n"
			+ "  <script type=\"text/javascript\"  async defer\n"
			//+ "    src=\"https://maps.googleapis.com/maps/api/js?sensor=false\">\n"
		    + "    src=\"https://maps.googleapis.com/maps/api/js?key=" + SMGoogleMapAPIKey.SMCP_GMAPS_API_KEY1 + "\">\n"
		    + "  </script>\n"
		    
		    + "  <script type=\"text/javascript\">\n"
		    + "  var geocoder;\n"
		    + "  var map;\n"
		    + "  var iterator = 0;\n"
		    + "  var openimage;\n"
		    + "  var closedimage;\n"
		    + "  var shadow;\n"
		    
		    //This is the array holds mechanics name for legends
		    /*
		    + "var mechanics = [\n"
	    	  + "{'mechname': 'Li Tong','color': '#ff0000'}"
	    	  + ",{'mechname': 'Tom Ronayne','color': '#00ff00'}" 
	    	  + ",{'mechname': 'Brian Kuhn','color': '#0000ff'}"
	        + "];\n"
		    */
			
			//This array holds the locations we want to 'drop' on to the map:
		    + "  var geocodes = [\n"
		    ;
			
			//Load the array with the addresses:
			for (int i = 0; i < arrGeocodes.size(); i++){
				if (i == 0){
					s += "    \"" + arrGeocodes.get(i) + "\"\n";
				}else{
					s += "    , \"" + arrGeocodes.get(i) + "\"\n";	
				}
			}
		    //s + "    new google.maps.LatLng(39.37, -76.967),\n"
		    //+ "    new google.maps.LatLng(39.379, -76.8),\n"
		    //+ "    new google.maps.LatLng(39.3678, -76.7),\n"
		    //+ "    new google.maps.LatLng(39.3689, -76.6)\n"
		    s += "  ];\n"
		    
		    //This array holds the TITLES we want to 'drop' on to the map:
		    + "  var markertitles = [\n"
		    ;
		    
			//Load the array with the marker titles:
			for (int i = 0; i < arrMarkerTitles.size(); i++){
				if (i == 0){
					s += "    \"" + arrMarkerTitles.get(i).replace(",", "") + "\"\n";
				}else{
					s += "    , \"" + arrMarkerTitles.get(i).replace(",", "") + "\"\n";	
				}
			}
		    s += "  ];\n"
		    	
		    //This array holds the 'Open/closed flags for the marker icons:
		    + "  var openclosedflags = [\n"
		    ;
		    
			//Load the array with the marker titles:
			for (int i = 0; i < arrOpenClosedFlags.size(); i++){
				if (i == 0){
					s += "    \"" + arrOpenClosedFlags.get(i) + "\"\n";
				}else{
					s += "    , \"" + arrOpenClosedFlags.get(i) + "\"\n";	
				}
			}
		    s += "  ];\n"
		   
		    //This array holds the 'info window' strings for the marker icons:
		    + "  var infowindows = [\n"
		    ;
		    
			//Load the array with the marker titles:
			for (int i = 0; i < arrInfoStrings.size(); i++){
				if (i == 0){
					s += "    \"" + arrInfoStrings.get(i) + "\"\n";
				}else{
					s += "    , \"" + arrInfoStrings.get(i) + "\"\n";	
				}
			}
		    s += "  ];\n"
		    	
		    //This code sets up the buttons to turn on/off the traffic view:
		    	// Define a property to hold the Home state
		    	+ "  TrafficControl.prototype.home_ = null;\n"

		    	// Define setters and getters for this property
		    	+ "  TrafficControl.prototype.getHome = function() {\n"
		    	+ "    return this.home_;\n"
		    	+ "  }\n"

		    	+ "  TrafficControl.prototype.setHome = function(home) {\n"
		    	+ "    this.home_ = home;\n"
		    	+ "  }\n"

		    	+ "  function TrafficControl(map, div, home) {\n"

		    	  // Get the control DIV. We'll attach our control UI to this DIV.
		    	+ "    var controlDiv = div;\n"

		    	  // We set up a variable for the 'this' keyword since we're adding event
		    	  // listeners later and 'this' will be out of scope.
		    	+ "    var control = this;\n"

		    	  // Set the home property upon construction
		    	+ "    control.home_ = home;\n"

		    	  // Set CSS styles for the DIV containing the control. Setting padding to
		    	  // 5 px will offset the control from the edge of the map
		    	+ "    controlDiv.style.padding = '5px';\n"
		    	
		    	  // Set CSS for the control border
		    	+ "    var showTrafficUI = document.createElement('DIV');\n"
		    	+ "    showTrafficUI.style.backgroundColor = 'white';\n"
		    	+ "    showTrafficUI.style.borderStyle = 'solid';\n"
		    	+ "    showTrafficUI.style.borderWidth = '1px';\n"
		    	+ "    showTrafficUI.style.cursor = 'pointer';\n"
		    	+ "    showTrafficUI.style.textAlign = 'center';\n"
		    	+ "    showTrafficUI.title = 'Click to show traffic layer';\n"
		    	+ "    controlDiv.appendChild(showTrafficUI);\n"

		    	  // Set CSS for the control interior
		    	+ "    var showTrafficText = document.createElement('DIV');\n"
		    	+ "    showTrafficText.innerHTML = '&nbsp;Show traffic&nbsp;';\n"
		    	+ "    showTrafficText.style.fontFamily = 'Arial';\n"
		    	+ "    showTrafficText.style.fontSize = 'small';\n"
		    	+ "    showTrafficUI.appendChild(showTrafficText);\n"
		    	  
		    	  // Set CSS for the setHome control border
		    	//+ "    var setHomeUI = document.createElement('DIV');\n"
		    	//+ "    setHomeUI.style.backgroundColor = 'white';\n"
		    	//+ "    setHomeUI.style.borderStyle = 'solid';\n"
		    	//+ "    setHomeUI.style.borderWidth = '2px';\n"
		    	//+ "    setHomeUI.style.cursor = 'pointer';\n"
		    	//+ "    setHomeUI.style.textAlign = 'center';\n"
		    	//+ "    setHomeUI.title = 'Click to hide traffic layer';\n"
		    	//+ "    controlDiv.appendChild(setHomeUI);\n"

		    	  // Set CSS for the control interior
		    	//+ "    var setHomeText = document.createElement('DIV');\n"
		    	//+ "    setHomeText.innerHTML = 'Hide traffic';\n"
		    	//+ "    setHomeUI.appendChild(setHomeText);\n"

		    	  // Setup the click event listener for Home:
		    	  // simply set the map to the control's current home property.
		    	+ "    google.maps.event.addDomListener(showTrafficUI, 'click', function() {\n"
			    + "      var trafficLayer = new google.maps.TrafficLayer();\n"
			    + "      trafficLayer.setMap(map);\n"
		    	+ "    });\n"

		    	  // Setup the click event listener for Set Home:
		    	  // Set the control's home to the current Map center.
		    	//+ "    google.maps.event.addDomListener(setHomeUI, 'click', function() {\n"
			    //+ "      var trafficLayer = new google.maps.TrafficLayer();\n"
			    //+ "      trafficLayer.setMap(map);\n"
		    	//+ "    });\n"
		    	+ "  }\n"
		    	//TODO:	initialization
		    	+ "  function initialize() {\n"
		    	
		    	+ "  var legendTitle = 'Mechanics';\n";

			    boolean bNewMarkers = true;
		    	
		    	if (!bNewMarkers){
		    		
		    		s += "    openimage = new google.maps.MarkerImage(\n"
					    + "      \"" + SMUtilities.getOpenTruckMarker(context) + "\",\n"
					    + "      new google.maps.Size(32.0, 32.0),\n"
					    + "      new google.maps.Point(0, 0),\n"
					    + "      new google.maps.Point(16.0, 16.0)\n"
					    + "    );\n"
					    + "    closedimage = new google.maps.MarkerImage(\n"
					    + "      \"" + SMUtilities.getClosedTruckMarker(context) + "\",\n"
					    + "      new google.maps.Size(32.0, 32.0),\n"
					    + "      new google.maps.Point(0, 0),\n"
					    + "      new google.maps.Point(16.0, 16.0)\n"
					    + "    );\n"
					    + "    shadow = new google.maps.MarkerImage(\n"
					    + "      \"" + SMUtilities.getTruckMarkerShadow(context) + "\",\n"
					    + "      new google.maps.Size(32.0, 32.0),\n"
					    + "      new google.maps.Point(0, 0),\n"
					    + "      new google.maps.Point(16.0, 10.0)\n"
					    + "    );\n"
					    ;
		    	}else{
		    		s += "    shadow = new google.maps.MarkerImage(\n"
					    + "      \"" + SMUtilities.getMechanicMarkerShadow(context) + "\",\n"
					    + "      new google.maps.Size(22.0, 32.0),\n"
					    + "      new google.maps.Point(0, 0),\n"
					    + "      new google.maps.Point(11.0, 16.0)\n"
					    + "    );\n"
					    ;
		    	
		    	}
				
			    //If only one location is selected center the map to that location.
			    //TODO get center point of location addresses in the array. 
			    if (arrLocationAddress.size() == 1) {
			    	sCenterAddress= arrLocationAddress.get(0);
			    }
			    //set map center
			    s += "    geocoder = new google.maps.Geocoder();\n"
				    + "    var latlng = new google.maps.LatLng(38.895496,-77.03008);\n"
			        + "    var sAddress = \"" + sCenterAddress + "\";\n"
			    	+ "    geocoder.geocode( { 'address': sAddress}, function(results, status) {\n"
				    + "      if (status == google.maps.GeocoderStatus.OK) {"
				    + "         map.setCenter(results[0].geometry.location);\n"
				    + "      } else {\n"
				    + "        alert(\"Geocoding location was not successful for the following reason: \" + status);\n"
				    + "      }\n"
				    + "    });\n";
			    
				   s += "\n"
				   
				    //set map options
				    + "     var myOptions = {\n"
				    + "        zoom: 10,\n"
				  //  + "        center: latlng,\n"
				    + "        mapTypeId: google.maps.MapTypeId.ROADMAP,\n"
				    + "        mapTypeControlOptions: { \n" 
				    + "        		style: google.maps.MapTypeControlStyle.DROPDOWN_MENU\n"  
				    + "        }\n"
				    + "      };\n"
				    
				    //create map
				    + "      var mapDiv = document.getElementById('map_canvas');\n"
				    + "      map = new google.maps.Map(mapDiv,myOptions);\n"
				    // Create the DIV to hold the control and call the TrafficControl()
				    // constructor passing in this DIV.
				    + "      var trafficControlDiv = document.createElement('DIV');\n"
				    + "      var trafficControl = new TrafficControl(map, trafficControlDiv, map.getCenter());\n"
	
				    + "      trafficControlDiv.index = 1;\n"
				    + "      map.controls[google.maps.ControlPosition.TOP_RIGHT].push(trafficControlDiv);\n"
				    ;
			    
		    if (bNewMarkers){
		    	/*
		    //This draws jobs on the map with Polygons:
		     s += "      for (var i = 0; i < geocodes.length; i++) {\n"
			     + "        var myLatlng = new google.maps.LatLng(geocodes[iterator]);\n"
			     + " 		var truckdot;"
			     + "        var temp = new Array();\n"
			     + "        temp = geocodes[iterator].split(',');\n"
			     + " 		var truckloc = [new google.maps.LatLng(parseFloat(temp[0]), parseFloat(temp[1]))];\n"
			     + " 		var colorcode = temp[2];\n"
			     + " 		if (openclosedflags[iterator] == \"O\"){\n"
		         + "   			truckdot = new google.maps.Polygon({\n"
		         + "  				paths: truckloc,\n"
		         + "  				strokeColor: colorcode,\n"	//color for the mechanic
		         + "  				strokeOpacity: 1.0,\n"		//transparency of the dot
		         + "  				strokeWeight: 12,\n"		//size of the dot 
		         + "  			fillColor: colorcode,\n"
		         + "  			fillOpacity: 0.35\n"
		         + "  			});\n"
		         + " 		}else{\n"
		         + "   			truckdot = new google.maps.Polygon({\n"
		         + "  				paths: truckloc,\n"
		         + "  				strokeColor: colorcode,\n"	//color for the mechanic
		         + "  				strokeOpacity: 0.55,\n"		//transparency of the dot
		         + "  				strokeWeight: 10,\n"		//size of the dot
		         + "  			fillColor: colorcode,\n"
		         + "  			fillOpacity: 0.35\n"
		         + "  			});\n"
		         + " 		}"
		         + "   		truckdot.setMap(map);\n"
		  
		         // Add a listener for the click event
		         + " 		google.maps.event.addListener(truckdot, 'click', showJobInfo);\n"
			     + "         iterator++;\n"
			     + "      }\n"
			    ; 
		     */
		    	//end of drawing jobs with Polygons
		    	
		    	//This draws jobs on the map with Markers:
			    s += "      for (var i = 0; i < geocodes.length; i++) {\n"
				    + "        setTimeout(function() {\n"
				    + "          addMarker();\n"
				    + "        }, i * 100);\n"
				    + "      }\n"
				    ;
		    	
		     
		    }else{
		    	
			    //This does the 'drops' of each schedule entry:
			    s += "      for (var i = 0; i < geocodes.length; i++) {\n"
				    + "        setTimeout(function() {\n"
				    + "          addMarker();\n"
				    + "        }, i * 100);\n"
				    + "      }\n"
				    ;
		    }
		    if (bNewMarkers){
	        // Create the legend and display on the map
		    s += "var html1 = " + get_Mechanic_Color_Code_Legend_HTML(arrMechanics, conn) + "\n;";

		    //Get the column count:
		    int m_iLegendColumnCount = getLegendColumnCount(arrMechanics);
		    
		    s += "var legend = new Legend('Legend', '80px', html1, '" + (200 * (m_iLegendColumnCount + 1)) + "px');\n"
		    	+ "map.controls[google.maps.ControlPosition.TOP_RIGHT].push(legend.div);\n"
		    	;
		    }
		    s += "infowindow = new google.maps.InfoWindow();\n"
		    	;
		    s += "google.maps.event.addDomListener(window, 'load', initialize);\n"
		    	+ "}\n"	//end of map initialization	    
		    	+ "\n";

		    s += "function showJobInfo(event){\n"

		    	  + " var contentString = getContentString(event.latLng);\n"

		    	  // Replace our Info Window's content and position
		    	  + " infowindow.setContent(contentString);\n"
		    	  + " infowindow.setPosition(event.latLng);\n"
		    	  + " infowindow.open(map);\n"
		    + " }\n"
		    ;
		    
		    s += "function getContentString(JobLatLng) {\n"		
		    	
		    	  + " 	infowindow.setContent('No job information available');\n"
		    	  + "  	for (var i=0; i<geocodes.length; i++) {\n"
		    	  + "		var temp = new Array();\n"
			      + "       temp = geocodes[i].split(',');\n"
			      + " 		var temploc = new google.maps.LatLng(parseFloat(temp[0]),parseFloat(temp[1]));\n"
			      + " 		if (JobLatLng.toUrlValue() == temploc.toUrlValue()){\n"
			      + "			infowindow.setContent(infowindows[i]);\n"
			      + " 		}\n"
			      + "   }\n"
			      + " }\n"
			      ;
		    
	    if (!bNewMarkers){
	    	s += "  function addMarker(){\n"
			    + "     var temp = new Array();\n"
			    + "     temp = geocodes[iterator].split(',');\n"
			    + "     var myLatlng = new google.maps.LatLng(parseFloat(temp[0]),parseFloat(temp[1]));\n"
			    + "     if (openclosedflags[iterator] == \"O\"){\n"
	 		    + "          var marker = new google.maps.Marker({\n"
	 		    + "          	position: myLatlng,\n"
			    + "          	map: map,\n" 
			    + "          	icon: openimage,\n"
			    + "          	shadow: shadow,\n"
			    + "          	draggable: false,\n"
			    + "          	animation: google.maps.Animation.DROP,\n"
			    + "          	title: markertitles[iterator]\n"
			    + "          	});\n"
			    + "     } else {\n"
			    + "          var marker = new google.maps.Marker({\n"
			    + "             position: myLatlng,\n"
			    + "             map: map,\n" 
			    + "             icon: closedimage,\n"
			    + "             shadow: shadow,\n"
			    + "             draggable: false,\n"
			   + "             animation: google.maps.Animation.DROP,\n"
			    + "             title: markertitles[iterator]\n"
			    + "             });\n"
			    + "     }\n"
			    
	            //Add an info window for this marker:
			    + "     var infowindow = new google.maps.InfoWindow({\n"
			    + "         content: infowindows[iterator]\n"
			    + "          });\n"
	            //Add a click event for this marker:
			    + "     google.maps.event.addListener(marker, 'click', function() {\n"
			    + "     	infowindow.open(map,marker);\n"
			    + "     });\n"
			    + "     iterator++;\n"
			    + "}\n"
			    ;
	    }else{
	    	//Example of icon from sprite
	    	 
	    	s += "  function addMarker(){\n"
			    + "     var temp = new Array();\n"
			    + "     temp = geocodes[iterator].split(',');\n"
			    + "     var myLatlng = new google.maps.LatLng(parseFloat(temp[0]),parseFloat(temp[1]));\n"
			    + "     if (openclosedflags[iterator] == \"O\"){\n"
			    //+ " 		alert ('temp[2] = ' + temp[2] + ', temp[3] = ' + temp[3] + '.');\n"
	    		+ "	 		truckicon = new google.maps.MarkerImage(\n"
				+ "      		\"" + SMUtilities.getMechanicMarkerSprite(context) + "\",\n" 
				+ "      		new google.maps.Size(22.0, 32.0),\n" 										// display size
				+ "      		new google.maps.Point(parseInt(temp[3])*22, parseInt(temp[2])*32),\n"		// origin point within sprite
				+ "      		new google.maps.Point(11.0, 16.0)\n" 										// anchor point
				+ "      		);\n"
				    
	 		    + "          var marker = new google.maps.Marker({\n"
	 		    + "          	position: myLatlng,\n"
			    + "          	map: map,\n" 
			    + "          	icon: truckicon,\n"
			    + "          	shadow: shadow,\n"
			    + "          	draggable: false,\n"
			    + "          	animation: google.maps.Animation.DROP,\n"
			    + "          	title: markertitles[iterator]\n"
			    + "          	});\n"
			    + "     } else {\n"
				
	    		+ "	 		truckicon = new google.maps.MarkerImage(\n"
				+ "      		\"" + SMUtilities.getMechanicMarkerSprite(context) + "\",\n" 
				+ "      		new google.maps.Size(15.0, 32.0),\n" 										// display size
				+ "      		new google.maps.Point(parseInt(temp[3])*22, parseInt(temp[2])*32),\n"		// origin point within sprite
				+ "      		new google.maps.Point(8.0, 16.0)\n" 										// anchor point
				+ "      		);\n"
				
	 		    + "          var marker = new google.maps.Marker({\n"
	 		    + "          	position: myLatlng,\n"
			    + "          	map: map,\n" 
			    + "          	icon: truckicon,\n"
			    + "          	shadow: shadow,\n"
			    + "          	draggable: false,\n"
			    + "          	animation: google.maps.Animation.DROP,\n"
			    + "          	title: markertitles[iterator]\n"
			    + "          	});\n"
			    + "     }\n"
			    
	            //Add an info window for this marker:
			    + "     var infowindow = new google.maps.InfoWindow({\n"
			    + "         content: infowindows[iterator]\n"
			    + "          });\n"
	            //Add a click event for this marker:
			    + "     google.maps.event.addListener(marker, 'click', function() {\n"
			    + "     	infowindow.open(map,marker);\n"
			    + "     });\n"
			    + "     iterator++;\n"
			    + "}\n"
			    ;
	    	
	    }

	    s += "function Legend(name, controlWidth, content, contentWidth) {\n"
	    	+ "this.name = name;\n"
	    	+ "this.content = content;\n"

	    	+ "var container = document.createElement('div');\n"
	    	+ "container.style.position = 'relative';\n"
	    	+ "container.style.margin = '5px';\n"

	    	+ "var html = '<div style=\"background-color: white; width: ' + controlWidth + '; position: relative; overflow: hidden; border: 1px solid black; text-align: left; cursor: pointer;\">' +\n"
	    					+ "'<div style=\"color: black; font-family: Arial,sans-serif; font-size: 12px; padding: 0px 5px; border-width: 1px; border-style: solid; border-color: rgb(112, 112, 112) rgb(208, 208, 208) rgb(208, 208, 208) rgb(112, 112, 112); font-weight: bold; -khtml-user-select:none; -moz-user-select:none;\" unselectable=\"on\">' +\n"
				    		+ "		name +\n"
				    		+ "'</div>' +\n"
				    		+ "'<div style=\"position: absolute; right: 4px; top: 4px; -khtml-user-select:none;\">' +\n"
				    		+ "'	<img src=\"https://maps.gstatic.com/intl/en_gb/mapfiles/down-arrow.gif\" style=\"display: block;\">' +\n"
				    		+ "'</div>' +\n"
			    		+ "'</div>' +\n"
			    		+ "'<div style=\"display: none; padding: 5px; color: black; font-family: Arial,sans-serif; font-size: 12px; background-color: white; position: absolute; right: 0px; border: 1px solid black; width: ' + contentWidth + ';\">' +\n"
				    		+ "		content +\n"
				    	+ "'</div>';\n"
				    	;
	      s += "container.innerHTML = html;\n"
	    	  + "this.div = container;\n"

	    	  + "var control = container.childNodes[0];\n"
	    	  + "var legend  = container.childNodes[1];\n"
	      
	    	  + "control.title = 'Show legend';\n"

	    	  + "control.onclick = toggle;\n"
	    	  + "control.ontouchstart = touch;\n"
	    	  ;

	      s += "function toggle() {\n"
		    	  + "if (legend.style.display == 'none') {\n"
		    	  + "	legend.style.display = 'block';\n"
		    	  + "	control.title = 'Hide legend';\n"
		    	  + "} else {\n"
		    	  + "	legend.style.display = 'none';\n"
		    	  + "	control.title = 'Show legend';\n"
		    	  + "}\n"
	    	  + "}\n"//end of toggle
	    	  ;

	      s += "function touch() {\n"
		    	  + "if (arguments.length > 0 && arguments[0].preventDefault) {\n"
	    	  	  + "	arguments[0].preventDefault();\n"
		    	  + "}\n"
		    	  + "toggle();\n"
	    	  + "}\n" //end of touch
	    	  
	    + "}\n";//end of Legend()

		    s += "  </script>\n"
			  	+ "</head>\n"
			  	+ "<body onload=\"initialize()\">\n"
			  	+ "<div id=\"map_canvas\" style=\"width:100%; height:100%\"></div>\n"
			  	+ "</body>\n"
			  	+ "</html>\n";
		  	
		return s;
	}
	private String get_Mechanic_Color_Code(String sInit, Connection conn){
	
		String SQL = "SELECT * FROM" + 
						" " + SMTablemechanics.TableName + ", " + SMTablecolortable.TableName +
						" WHERE" + 
							" " + SMTablemechanics.TableName + "." + SMTablemechanics.sMechColorCodeRow + " =" +
							" " + SMTablecolortable.TableName + "." + SMTablecolortable.irow +
							" AND" +
							" " + SMTablemechanics.TableName + "." + SMTablemechanics.sMechColorCodeCol + " =" +
							" " + SMTablecolortable.TableName + "." + SMTablecolortable.icol +
							" AND" +
							" " + SMTablemechanics.TableName + "." + SMTablemechanics.sMechInitial + " = '" + sInit + "'";
		if (bDebugMode){System.out.println("[1344549881] SQL = " + SQL);}
		try{
			ResultSet rsMechColor = clsDatabaseFunctions.openResultSet(SQL, conn);
			if (rsMechColor.next()){
				//if there is a valid code, return it
				return rsMechColor.getString(SMTablecolortable.TableName + "." + SMTablecolortable.scolorcode);
			}else{
				//if there is no record for this mechanic, which is not likely, return black
				return "000000";
			}
		}catch(SQLException e){
			//if Anything went wrong, return black too.
			if (bDebugMode){
				System.out.println("[1344549882] Failed to retrieve color code for " + sInit);
				System.out.println("[1344549883] SQL = " + SQL);
			}
			return "000000";
		}
	}
	
	private String get_Mechanic_Color_Code_Legend_HTML(ArrayList<String> arrMechanics, Connection conn){
		
		int iAllowedNumberOfRows = 25;
		String s = "'<TABLE WIDTH=100%>' +\n";
		
		//Load the array for mechanic legends
		//sort Mechanics by first Name/Initial
		//first, generate corresponding color Array
		//ArrayList<String> arrColors = get_Legend_Colors(arrMechanics.size());
		Collections.sort(arrMechanics);
		
		//now arrange mechanics in this way:
		//	1.	Order by first name in one column, then next
		//	2.	Each column will be 25 names or less, but all columns will be the same length +/- 1 row.
		//first, figure out how many columns and how many rows
		ArrayList<String> alRows = new ArrayList<String> (iAllowedNumberOfRows);
		int iterator = 0;
		int iRowCount = 0;
		int iLegendColumnCount = -1;
		
		while (iterator < arrMechanics.size()){ 
			if (iRowCount == 0){
				iLegendColumnCount++;
			}
			if (iLegendColumnCount == 0){
				alRows.add("'<TR>' +\n" + " '<TD><span style=\"background:#" + 
										get_Mechanic_Color_Code(arrMechanics.get(iterator).substring(0, 3), conn) + ";\">&nbsp;&nbsp;&nbsp;&nbsp;</span>" +
										" " + arrMechanics.get(iterator) + 
					            		"</TD>' +\n");
			}else{
				alRows.set(iRowCount, alRows.get(iRowCount) + 
									" '<TD><span style=\"background:#" + 
									get_Mechanic_Color_Code(arrMechanics.get(iterator).substring(0, 3), conn) + ";\">&nbsp;&nbsp;&nbsp;&nbsp;</span>" +
									" " + arrMechanics.get(iterator) + 
				            		"</TD>' +\n");
			}

			iRowCount ++;
			if (iRowCount % iAllowedNumberOfRows == 0){
				iRowCount = 0;
			}
			iterator++;
		}
		
		for (int i=0;i<alRows.size();i++){
			s += alRows.get(i) + "'</TR>' +\n";
		}
		
		s += "'</TABLE>'\n";
		
		return s;
					
	}
	private int getLegendColumnCount(ArrayList<String> arrMechanics){
		int iLegendColumnCount = -1;
		int iAllowedNumberOfRows = 25;
		
		//Load the array for mechanic legends
		//sort Mechanics by first Name/Initial
		//first, generate corresponding color Array
		//ArrayList<String> arrColors = get_Legend_Colors(arrMechanics.size());
		Collections.sort(arrMechanics);
		
		//now arrange mechanics in this way:
		//	1.	Order by first name in one column, then next
		//	2.	Each column will be 25 names or less, but all columns will be the same length +/- 1 row.
		//first, figure out how many columns and how many rows
		int iterator = 0;
		int iRowCount = 0;
		
		while (iterator < arrMechanics.size()){ 
			if (iRowCount == 0){
				iLegendColumnCount++;
			}
			iRowCount ++;
			if (iRowCount % iAllowedNumberOfRows == 0){
				iRowCount = 0;
			}
			iterator++;
		}
		
		return iLegendColumnCount;
	}
	
	public void doGet(HttpServletRequest request,
			HttpServletResponse response)
	throws ServletException, IOException {

		doPost(request, response);
	}
}
