package smic;

import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import smcontrolpanel.SMAuthenticate;
import smcontrolpanel.SMSystemFunctions;
import smcontrolpanel.SMUtilities;
import ConnectionPool.WebContextParameters;
import SMClasses.SMLogEntry;
import SMDataDefinition.SMTableicitems;
import SMDataDefinition.SMTableicpoheaders;
import SMDataDefinition.SMTableicpolines;
import SMDataDefinition.SMTableicporeceiptheaders;
import SMDataDefinition.SMTableicporeceiptlines;
import SMDataDefinition.SMTableicvendoritems;
import SMDataDefinition.SMTablelabelprinters;
import ServletUtilities.clsServletUtilities;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsDateAndTimeConversions;
import ServletUtilities.clsManageBigDecimals;
import ServletUtilities.clsManageRequestParameters;
import ServletUtilities.clsStringFunctions;

public class ICPrintReceivingLabelsGenerate extends HttpServlet {

	private static final long serialVersionUID = 1L;
	public static final String LABELQTY_FIELD = "LBLQTY";
	public static final String BUTTONSAVE_NAME = "SUBMITSAVE";
	public static final String BUTTONPRINT_NAME = "SUBMITPRINT";
	public static final String BUTTONSAVE_LABEL = "Save number of labels";
	public static final String BUTTONPRINT_LABEL = "Print labels";
	private static final int MAX_NUMBER_OF_LINES_ALLOWED = 1000;
	private static final String TOO_MANY_ITEMS_MESSAGE = 
		"Error [1415827798] - You cannot print labels for more than " + Integer.toString(MAX_NUMBER_OF_LINES_ALLOWED) 
		+ ", so this list was terminated at that number of items - limit the range of items or dates"
		+ " to bring the number of items to " + Integer.toString(MAX_NUMBER_OF_LINES_ALLOWED) + " or less."
	;
	//formats
	private static final SimpleDateFormat USDateformatter = new SimpleDateFormat("MM-dd-yyyy hh:mm:ss a EEE");

	
	private boolean bDebugMode = false;
	//private static SimpleDateFormat USTimeOnlyformatter = new SimpleDateFormat("hh:mm:ss a");

	public void doGet(HttpServletRequest request,
			HttpServletResponse response)
	throws ServletException, IOException {
		if (!SMAuthenticate.authenticateSMCPCredentials(
				request, 
				response, 
				getServletContext(),
				SMSystemFunctions.ICPrintReceivingLabels
		)
		){
			return;
		}
		response.setContentType("text/html");
		PrintWriter out = response.getWriter();

		//Get the session info:
		HttpSession CurrentSession = request.getSession(true);
		String sDBID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_DATABASE_ID);
		String sUserID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERID);
		String sUserFirstName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERFIRSTNAME);
		String sUserLastName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERLASTNAME);
		
		String sCompanyName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_COMPANYNAME);


		String sWarning = "";
		String sCallingClass = "";
		//sCallingClass will look like: smcontrolpanel.ARAgedTrialBalanceReport
		sCallingClass = clsManageRequestParameters.get_Request_Parameter("CallingClass", request);
		if (sCallingClass.compareToIgnoreCase("") == 0){
			sCallingClass = "ICPrintReceivingLabelsSelection";
		}

		//Log the report usage:
	
		SMClasses.SMLogEntry log = new SMClasses.SMLogEntry(sDBID, getServletContext());
		log.writeEntry(sUserID, SMLogEntry.LOG_OPERATION_ICPRINTRECEIVINGLABELS, "REPORT", "ICPrintReceivingLabels", "[1376509411]");

		String sStartPODate = clsManageRequestParameters.get_Request_Parameter("StartingPODate", request);
		String sEndPODate = clsManageRequestParameters.get_Request_Parameter("EndingPODate", request);
		
		String sStartExpectedReceiptDate = clsManageRequestParameters.get_Request_Parameter("StartingDate", request);
		String sEndExpectedReceiptDate = clsManageRequestParameters.get_Request_Parameter("EndingDate", request);
		
		String sStartReceiptDate = clsManageRequestParameters.get_Request_Parameter(ICPrintReceivingLabelsSelection.STARTING_RECEIPT_DATE_PARAMETER, request);
		String sEndReceiptDate = clsManageRequestParameters.get_Request_Parameter(ICPrintReceivingLabelsSelection.ENDING_RECEIPT_DATE_PARAMETER, request);
		
		String sStartingVendor = clsManageRequestParameters.get_Request_Parameter("StartingVendor", request);
		String sEndingVendor = clsManageRequestParameters.get_Request_Parameter("EndingVendor", request);
	
		String sPrintUnreceivedOrReceived = clsManageRequestParameters.get_Request_Parameter(ICPrintReceivingLabelsSelection.PRINT_RECEIVED_OR_UNRECEIVED_ITEMS, request);
		String sReceivedByID = clsManageRequestParameters.get_Request_Parameter(ICPrintReceivingLabelsSelection.RECEIVED_BY_PARAMETER, request);
		
		String sPONumber = clsManageRequestParameters.get_Request_Parameter(ICPrintReceivingLabelsAction.PARAM_PONUMBER, request);
		
		String sQueryString = 
			"&StartingPODate=" + sStartPODate
			+ "&EndingPODate=" + sEndPODate
			+ "&StartingDate=" + sStartExpectedReceiptDate
			+ "&EndingDate=" + sEndExpectedReceiptDate
			+ "&" + ICPrintReceivingLabelsAction.PARAM_PONUMBER + "=" + sPONumber
			+ "&" + ICPrintReceivingLabelsSelection.STARTING_RECEIPT_DATE_PARAMETER + "=" + sStartReceiptDate
			+ "&" + ICPrintReceivingLabelsSelection.ENDING_RECEIPT_DATE_PARAMETER + "=" + sEndReceiptDate
			+ "&" + ICPrintReceivingLabelsSelection.RECEIVED_BY_PARAMETER + "=" + sReceivedByID
			+ "&" + ICPrintReceivingLabelsSelection.PRINT_RECEIVED_OR_UNRECEIVED_ITEMS + "=" + sPrintUnreceivedOrReceived
		;

		//Get the list of selected locations:
		ArrayList<String> sLocations = new ArrayList<String>(0);
		Enumeration<String> paramLocationNames = request.getParameterNames();
		String sParamLocationName = "";
		String sLocationMarker = "LOCATION";
		while(paramLocationNames.hasMoreElements()) {
			sParamLocationName = paramLocationNames.nextElement();
			if (sParamLocationName.contains(sLocationMarker)){
				sLocations.add(sParamLocationName.substring(
						sParamLocationName.indexOf(sLocationMarker) + sLocationMarker.length()));
			}
		}
		Collections.sort(sLocations);

		String sStartingPODate = null;
		String sEndingPODate = null;
		String sStartingExpectedReceiptDate = null;
		String sEndingExpectedReceiptDate = null;
		String sStartingReceiptDate = null;
		String sEndingReceiptDate = null;
		//Validate the parameters:
		//IF the user is simply choosing to print labels for a single PO, then we don't need to validate the
		//other parameters:
		if (sPONumber.compareToIgnoreCase("") == 0){
			if (sLocations.size() == 0){
				sWarning = "You must select at least one location.";
				response.sendRedirect(
						"" + SMUtilities.getURLLinkBase(getServletContext()) + "" + sCallingClass + "?"
						+ "Warning=" + sWarning
						+ sQueryString
						+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
				);
			}
	
			try {
				sStartingPODate = clsDateAndTimeConversions.utilDateToString(clsDateAndTimeConversions.StringTojavaSQLDate("M/d/yyyy", sStartPODate),"yyyy-MM-dd");
			} catch (ParseException e) {
				sWarning = "Error:[1423581191] Invalid starting PO date: '" + sStartPODate + "' - " + e.getMessage();
				response.sendRedirect(
						"" + SMUtilities.getURLLinkBase(getServletContext()) + "" + sCallingClass + "?"
						+ "Warning=" + sWarning
						+ sQueryString
						+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
				);			
				return;
			}
			
			try {
				sEndingPODate = clsDateAndTimeConversions.utilDateToString(clsDateAndTimeConversions.StringTojavaSQLDate("M/d/yyyy", sEndPODate),"yyyy-MM-dd");
			} catch (ParseException e) {
				sWarning = "Error:[1423752778] Invalid ending PO date: '" + sEndPODate + "' - " + e.getMessage();
				response.sendRedirect(
						"" + SMUtilities.getURLLinkBase(getServletContext()) + "" + sCallingClass + "?"
						+ "Warning=" + sWarning
						+ sQueryString
						+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
				);			
				return;
			}
			
			try {
				sStartingExpectedReceiptDate = clsDateAndTimeConversions.utilDateToString(clsDateAndTimeConversions.StringTojavaSQLDate("M/d/yyyy", sStartExpectedReceiptDate),"yyyy-MM-dd");
			} catch (ParseException e) {
				sWarning = "Error:[1423752777] Invalid starting date: '" + sStartExpectedReceiptDate + "' - " + e.getMessage();
				response.sendRedirect(
						"" + SMUtilities.getURLLinkBase(getServletContext()) + "" + sCallingClass + "?"
						+ "Warning=" + sWarning
						+ sQueryString
						+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
				);			
				return;
			}
			
			try {
				sEndingExpectedReceiptDate = clsDateAndTimeConversions.utilDateToString(clsDateAndTimeConversions.StringTojavaSQLDate("M/d/yyyy", sEndExpectedReceiptDate),"yyyy-MM-dd");
			} catch (ParseException e) {
				sWarning = "Error:[1423752776] Invalid ending date: '" + sEndExpectedReceiptDate + "' - " + e.getMessage();
				response.sendRedirect(
						"" + SMUtilities.getURLLinkBase(getServletContext()) + "" + sCallingClass + "?"
						+ "Warning=" + sWarning
						+ sQueryString
						+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
				);			
				return;
			}
			
			try {
				sStartingReceiptDate = clsDateAndTimeConversions.utilDateToString(clsDateAndTimeConversions.StringTojavaSQLDate("M/d/yyyy", sStartReceiptDate),"yyyy-MM-dd");
			} catch (ParseException e) {
				sWarning = "Error:[1423752775] Invalid starting receipt date: '" + sStartReceiptDate + "' - " + e.getMessage();
				response.sendRedirect(
						"" + SMUtilities.getURLLinkBase(getServletContext()) + "" + sCallingClass + "?"
						+ "Warning=" + sWarning
						+ sQueryString
						+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
				);			
				return;
			}
			
			try {
				sEndingReceiptDate = clsDateAndTimeConversions.utilDateToString(clsDateAndTimeConversions.StringTojavaSQLDate("M/d/yyyy", sEndReceiptDate),"yyyy-MM-dd");
			} catch (ParseException e) {
				sWarning = "Error:[1423752774] Invalid ending receipt date: '" + sEndReceiptDate + "' - " + e.getMessage();
				response.sendRedirect(
						"" + SMUtilities.getURLLinkBase(getServletContext()) + "" + sCallingClass + "?"
						+ "Warning=" + sWarning
						+ sQueryString
						+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
				);			
				return;
			}
		}
		String sReportTitle = "IC Print Receiving Labels";
		String sCriteria = "Starting with PO date '<B>" + sStartingPODate + "</B>'"
		+ ", ending with '<B>" + sEndingPODate + "</B>'";

		sCriteria = sCriteria + ", for vendors: '" + sStartingVendor + "' through '" + sEndingVendor + "'";
		sCriteria = sCriteria + ", for locations: ";
		for (int i = 0; i < sLocations.size(); i++){
			if (i == 0){
				sCriteria += "<B>" + sLocations.get(i) + "</B>";
			}else{
				sCriteria += ", <B>" + sLocations.get(i) + "</B>";
			}
		}
		
		if (sPrintUnreceivedOrReceived.compareToIgnoreCase(ICPrintReceivingLabelsSelection.PRINT_RECEIVED_ITEMS_VALUE) == 0){
			sCriteria += ", including only <B>RECEIVED</B> items";
			sCriteria += ", starting with actual receipt date '<B>" + sStartingReceiptDate + "</B>'"
					+ ", ending with '<B>" + sEndingReceiptDate + "</B>'";
			sCriteria += ", including only items received by ";
			if(sReceivedByID.compareToIgnoreCase("0") != 0) {
				sCriteria +=	"<B>" + SMUtilities.getFullNamebyUserID(sReceivedByID, getServletContext(), sDBID, "") + "</B>";	
			}else {
				sCriteria += "<B>" + ICPrintReceivingLabelsSelection.RECEIVED_BY_ANYONE + "</B>";
			}
		}
		if (sPrintUnreceivedOrReceived.compareToIgnoreCase(ICPrintReceivingLabelsSelection.PRINT_UNRECEIVED_ITEMS_VALUE) == 0){
			sCriteria += ", including only <B>UNRECEIVED</B> items";
			sCriteria += ", starting with expected receipt date '<B>" + sStartingExpectedReceiptDate + "</B>'"
					+ ", ending with '<B>" + sEndingExpectedReceiptDate + "</B>'";
		}
		
		sCriteria += ", listing <B>ONLY</B> inventory items.";
		
		//IF the user has simply chosen to print receiving labels for a particular PO, then set the 'Criteria' string to that:
		if (sPONumber.compareToIgnoreCase("") != 0){
			sCriteria = "Print labels for PO # " + sPONumber;
		}
		
		out.println("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.0 " +
				"Transitional//EN\">" +
				"<HTML>" +
				"<HEAD><TITLE>" + sReportTitle + " - " + sCompanyName + "</TITLE></HEAD>\n<BR>" + 
				"<BODY BGCOLOR=\"#FFFFFF\">" +
				"<TABLE BORDER=0 WIDTH=100%>" +
				"<TR><TD ALIGN=LEFT WIDTH=45%><FONT SIZE=2>" 
				+ USDateformatter.format((new Timestamp(System.currentTimeMillis()))) 
				+ " Printed by " + sUserFirstName + " " + sUserLastName
				+ "</FONT></TD><TD ALIGN=CENTER WIDTH=55%><FONT SIZE=2><B>" + sCompanyName + "</B></FONT></TD></TR>" +
				"<TR><TD VALIGN=BOTTOM COLSPAN=2><FONT SIZE=2><B>" + sReportTitle + "</B></FONT></TD></TR>" +

				"<TR><TD COLSPAN=2><FONT SIZE=2>" + sCriteria + "</FONT></TD></TR>");

		out.println("<TD><A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMUserLogin?" 
				+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
				+ "\">Return to user login</A><BR>");
		out.println("<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smic.ICMainMenu?" 
				+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
				+ "\">Return to Inventory Control Main Menu</A><BR>");
		out.println("<A HREF=\"" + WebContextParameters.getdocumentationpageURL(getServletContext()) + "#" + Long.toString(SMSystemFunctions.ICPOReceivingReport) 
				+ "\">Summary</A><BR>");
		out.println("</TD></TR></TABLE>");

		String sWarnings = clsManageRequestParameters.get_Request_Parameter("Warning", request);
		if (! sWarnings.equalsIgnoreCase("")){
			out.println("<B><FONT COLOR=\"RED\">WARNING: " + sWarnings + "</FONT></B><BR>");
		}
		String sStatus = clsManageRequestParameters.get_Request_Parameter("Status", request);
		if (! sStatus.equalsIgnoreCase("")){
			out.println("<B>" + sStatus + "</B><BR>");
		}
		//Retrieve information
		Connection conn = clsDatabaseFunctions.getConnection(
				getServletContext(), 
				sDBID, 
				"MySQL", 
				SMUtilities.getFullClassName(this.toString()) 
				+ " - userID: " 
				+ sUserID
				+ " - "
				+ sUserFirstName
				+ " "
				+ sUserLastName
		);
		if (conn == null){
			sWarnings = "Unable to get data connection.";
			response.sendRedirect(
					"" + SMUtilities.getURLLinkBase(getServletContext()) + "" + sCallingClass
					+ "?Warning=" + sWarnings
					+ sQueryString
					+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID    		);			
			return;
		}

		if (!processReport(
				conn, 
				sLocations,
				sStartingPODate,
				sEndingPODate,
				sStartingExpectedReceiptDate,
				sEndingExpectedReceiptDate,
				sStartingReceiptDate,
				sEndingReceiptDate,
				sStartingVendor,
				sEndingVendor,
				sReceivedByID,
				sPrintUnreceivedOrReceived,
				sPONumber,
				sDBID,
				sUserID,
				out,
				request,
				(String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_LICENSE_MODULE_LEVEL))
		){
		}
		clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1547080956]");
		out.println("</BODY></HTML>");
	}

	public boolean processReport(
			Connection conn,
			ArrayList<String>sLocations,
			String sStartingPODate,
			String sEndingPODate,
			String sStartingDate,
			String sEndingDate,
			String sStartingReceiptDate,
			String sEndingReceiptDate,
			String sStartingVendor,
			String sEndingVendor,
			String sReceivedByID,
			String sPrintUnreceivedOrReceived,
			String sPONumber,
			String sDBID,
			String sUserID,
			PrintWriter out,
			HttpServletRequest req,
			String sLicenseModuleLevel
	){

		//Create string of locations:
		String sLocationsString = "";
		for (int i = 0; i < sLocations.size(); i++){
			sLocationsString += "," + sLocations.get(i);
		}

		//Check permissions for viewing items:
		boolean bViewItemPermitted = SMSystemFunctions.isFunctionPermitted(
				SMSystemFunctions.ICDisplayItemInformation,
				sUserID,
				conn,
				sLicenseModuleLevel);

		//Determine if this user has rights to edit a PO:
		boolean bAllowPOEditing = 
			SMSystemFunctions.isFunctionPermitted(
					SMSystemFunctions.ICEditPurchaseOrders, 
					sUserID, 
					conn,
					sLicenseModuleLevel
			);

		//Determine if this user has rights to view a PO:
		boolean bAllowPOPrinting = 
			SMSystemFunctions.isFunctionPermitted(
					SMSystemFunctions.ICPrintPurchaseOrders, 
					sUserID, 
					conn,
					sLicenseModuleLevel
			);

		//Start the form:
		out.println ("<FORM METHOD=\"POST\" ACTION =\"" + SMUtilities.getURLLinkBase(getServletContext()) 
				+ "smic.ICPrintReceivingLabelsAction\">");
		out.println("<INPUT TYPE=HIDDEN NAME='" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "' VALUE='" + sDBID + "'>");
		out.println("<INPUT TYPE=HIDDEN NAME=CallingClass VALUE=\"" 
				+ SMUtilities.getFullClassName(this.toString()) + "\">");
		out.println("<INPUT TYPE=HIDDEN NAME='StartingVendor' VALUE='" 
				+ clsManageRequestParameters.get_Request_Parameter("StartingVendor", req) + "'>");
		out.println("<INPUT TYPE=HIDDEN NAME='EndingVendor' VALUE='" 
				+ clsManageRequestParameters.get_Request_Parameter("EndingVendor", req) + "'>");
		out.println("<INPUT TYPE=HIDDEN NAME='StartingDate' VALUE='" 
				+ clsManageRequestParameters.get_Request_Parameter("StartingDate", req) + "'>");
		out.println("<INPUT TYPE=HIDDEN NAME='EndingDate' VALUE='" 
				+ clsManageRequestParameters.get_Request_Parameter("EndingDate", req) + "'>");
		out.println("<INPUT TYPE=HIDDEN NAME='StartingPODate' VALUE='" 
				+ clsManageRequestParameters.get_Request_Parameter("StartingPODate", req) + "'>");
		out.println("<INPUT TYPE=HIDDEN NAME='EndingPODate' VALUE='" 
				+ clsManageRequestParameters.get_Request_Parameter("EndingPODate", req) + "'>");
		out.println("<INPUT TYPE=HIDDEN NAME='" + ICPrintReceivingLabelsAction.PARAM_PONUMBER + "' VALUE='" 
				+ clsManageRequestParameters.get_Request_Parameter(ICPrintReceivingLabelsAction.PARAM_PONUMBER, req) + "'>");
		out.println("<INPUT TYPE=HIDDEN NAME='" + ICPrintReceivingLabelsSelection.STARTING_RECEIPT_DATE_PARAMETER + "' VALUE='" 
				+ clsManageRequestParameters.get_Request_Parameter(ICPrintReceivingLabelsSelection.STARTING_RECEIPT_DATE_PARAMETER, req) + "'>");
		out.println("<INPUT TYPE=HIDDEN NAME='" + ICPrintReceivingLabelsSelection.ENDING_RECEIPT_DATE_PARAMETER + "' VALUE='" 
				+ clsManageRequestParameters.get_Request_Parameter(ICPrintReceivingLabelsSelection.ENDING_RECEIPT_DATE_PARAMETER, req) + "'>");
		out.println("<INPUT TYPE=HIDDEN NAME='" + ICPrintReceivingLabelsSelection.RECEIVED_BY_PARAMETER + "' VALUE='" 
				+ clsManageRequestParameters.get_Request_Parameter(ICPrintReceivingLabelsSelection.RECEIVED_BY_PARAMETER, req) + "'>");
		out.println("<INPUT TYPE=HIDDEN NAME='" + ICPrintReceivingLabelsSelection.PRINT_RECEIVED_OR_UNRECEIVED_ITEMS + "' VALUE='"
				+ sPrintUnreceivedOrReceived + "'>");

		for (int i = 0; i < sLocations.size(); i++){
			out.println("<INPUT TYPE=HIDDEN NAME='LOCATION" + sLocations.get(i) + "' VALUE='yes'"); 
		}

		//Add a drop down for the destinations:
		out.println("<BR>Print destination: ");
		out.println("<SELECT NAME = \"" + ICPrintUPCItemLabel.LABELPRINTER_LIST + "\">");
		out.println("<OPTION selected=yes VALUE=\"" + "0" + "\">" + "Print to screen</OPTION>");
		String sLabelPrinterSQL = "SELECT * FROM " + SMTablelabelprinters.TableName + " ORDER BY " + SMTablelabelprinters.sName;
		try {
			ResultSet rs = clsDatabaseFunctions.openResultSet(
					sLabelPrinterSQL, 
					getServletContext(), sDBID, "MySQL", SMUtilities.getFullClassName(this.toString()));
			while (rs.next()){
				out.println("<OPTION VALUE=\"" + Long.toString(rs.getLong(SMTablelabelprinters.lid)) + "\">" 
						+ rs.getString(SMTablelabelprinters.sName) + " - " 
						+ rs.getString(SMTablelabelprinters.sDescription) + "</OPTION>");
			}
			rs.close();
		} catch (SQLException e) {
			out.println("Error reading label printers data - " + e.getMessage());
		}
		out.println("</SELECT><BR>");

		//TJR - 9/23/2011 - removed this so that we couldn't save the number of labels to the item record:
		//out.println("<INPUT TYPE=\"SUBMIT\" NAME=\"" 
		//		+ BUTTONSAVE_NAME + "\" VALUE=\"" + BUTTONSAVE_LABEL + "\">");
		out.println("&nbsp;&nbsp;<INPUT TYPE=\"SUBMIT\" NAME=\"" 
				+ BUTTONPRINT_NAME + "\" VALUE=\"" + BUTTONPRINT_LABEL + "\">");

		//Here we branch, depending on whether the user chose to print RECEIVED or UNRECEIVED items:
		if (sPrintUnreceivedOrReceived.compareToIgnoreCase(ICPrintReceivingLabelsSelection.PRINT_RECEIVED_ITEMS_VALUE) == 0){
			try {
				printReceivedReport(
						conn, 
						out,
						sStartingReceiptDate,
						sEndingReceiptDate,
						sStartingPODate,
						sEndingPODate,
						sLocationsString,
						sStartingVendor,
						sEndingVendor,
						sReceivedByID,
						bViewItemPermitted,
						bAllowPOEditing,
						bAllowPOPrinting,
						sDBID
				);
			} catch (Exception e) {
				out.println("<BR>Error processing report - " + e.getMessage() + ".");
			}
		}else{
			try {
				printUnreceivedReport(
						conn, 
						out,
						sStartingDate,
						sEndingDate,
						sStartingPODate,
						sEndingPODate,
						sLocationsString,
						sStartingVendor,
						sEndingVendor,
						sPONumber,
						bViewItemPermitted,
						bAllowPOEditing,
						bAllowPOPrinting,
						sDBID
				);
			} catch (Exception e) {
				out.println("<BR>Error processing report - " + e.getMessage() + ".");
			}
		}

		out.println("</TABLE>");
		out.println("<BR>");

		//TJR - 9/23/2011 - removed this so that we couldn't save the number of labels to the item record:
		//out.println("<INPUT TYPE=\"SUBMIT\" NAME=\"" 
		//		+ BUTTONSAVE_NAME + "\" VALUE=\"" + BUTTONSAVE_LABEL + "\">");
		out.println("&nbsp;&nbsp;<INPUT TYPE=\"SUBMIT\" NAME=\"" 
				+ BUTTONPRINT_NAME + "\" VALUE=\"" + BUTTONPRINT_LABEL + "\">");
		out.println("</FORM>");
		return true;
	}

	private void printUnreceivedReport(
			Connection conn, 
			PrintWriter out,
			String sStartingDate,
			String sEndingDate,
			String sStartingPODate,
			String sEndingPODate,
			String sLocationsString,
			String sStartingVendor,
			String sEndingVendor,
			String sPONumber,
			boolean bViewItemPermitted,
			boolean bAllowPOEditing,
			boolean bAllowPOPrinting,
			String sDBID
			) throws Exception {
		
		int iLineCounter = 0;
		printUnreceivedRowHeader(out);
		//TJR - modify this statement:
		String SQL = "SELECT"
			+ " " + SMTableicpoheaders.TableName + "." + SMTableicpoheaders.lid
			+ ", " + SMTableicpoheaders.TableName + "." + SMTableicpoheaders.svendor
			+ ", " + SMTableicpoheaders.TableName + "." + SMTableicpoheaders.lstatus
			+ ", " + SMTableicpoheaders.TableName + "." + SMTableicpoheaders.sreference
			+ ", " + SMTableicpoheaders.TableName + "." + SMTableicpoheaders.svendorname
			+ ", " + SMTableicpoheaders.TableName + "." + SMTableicpoheaders.sponumber
			+ ", " + SMTableicpoheaders.TableName + "." + SMTableicpoheaders.datexpecteddate
			+ ", " + SMTableicpoheaders.TableName + "." + SMTableicpoheaders.datpodate
			+ ", " + SMTableicpolines.TableName + "." + SMTableicpolines.sitemnumber
			+ ", " + SMTableicpolines.TableName + "." + SMTableicpolines.bdqtyordered
			+ ", " + SMTableicpolines.TableName + "." + SMTableicpolines.datexpected
			+ ", " + SMTableicpolines.TableName + "." + SMTableicpolines.llinenumber
			+ ", " + SMTableicpolines.TableName + "." + SMTableicpolines.sitemdescription
			+ ", " + SMTableicpolines.TableName + "." + SMTableicpolines.slocation
			+ ", " + SMTableicitems.TableName + "." + SMTableicitems.sComment1
			+ ", " + SMTableicitems.TableName + "." + SMTableicitems.bdnumberoflabels
			+ ", " + SMTableicvendoritems.TableName + "." + SMTableicvendoritems.sVendorItemNumber
			+ " FROM (" + SMTableicpoheaders.TableName + " INNER JOIN " + SMTableicpolines.TableName
			+ " ON " + SMTableicpoheaders.TableName + "." + SMTableicpoheaders.lid + " = " 
			+ SMTableicpolines.TableName + "." + SMTableicpolines.lpoheaderid + ") LEFT JOIN " 
			+ SMTableicitems.TableName + " ON " + SMTableicpolines.TableName + "." 
			+ SMTableicpolines.sitemnumber + " = " + SMTableicitems.TableName + "." 
			+ SMTableicitems.sItemNumber
			+ " LEFT JOIN " + SMTableicvendoritems.TableName + " ON (" 
			+ "(" + SMTableicpolines.TableName + "." + SMTableicpolines.sitemnumber + " = " 
			+ SMTableicvendoritems.TableName + "." + SMTableicvendoritems.sItemNumber + ")"
			+ " AND (" + SMTableicpoheaders.TableName + "." + SMTableicpoheaders.svendor + " = " 
			+ SMTableicvendoritems.TableName + "." + SMTableicvendoritems.sVendor + ")"
			+ ")"
			+ " WHERE ("
			//Is NOT deleted:
			+ "(" 
			+ "(" + SMTableicpoheaders.TableName + "." + SMTableicpoheaders.lstatus
			+ " = " + SMTableicpoheaders.STATUS_ENTERED + ")"
			+ " OR (" + SMTableicpoheaders.TableName + "." + SMTableicpoheaders.lstatus
			+ " = " + SMTableicpoheaders.STATUS_PARTIALLY_RECEIVED + ")"
			+ " OR (" + SMTableicpoheaders.TableName + "." + SMTableicpoheaders.lstatus
			+ " = " + SMTableicpoheaders.STATUS_COMPLETE + ")"
			+ ")"

			//And only get inventory items:
			+ " AND (" + SMTableicpolines.TableName + "." + SMTableicpolines.lnoninventoryitem + " = 0)";

			//IF we are NOT just printing labels for a particular PO, then add other qualifiers:
			if (sPONumber.compareToIgnoreCase("") != 0){
				SQL += " AND (" + SMTableicpoheaders.TableName + "." + SMTableicpoheaders.lid + " = " + sPONumber + ")";
			}else{
			//Expected receipt Dates
			SQL += " AND (" + SMTableicpoheaders.TableName + "." + SMTableicpoheaders.datexpecteddate 
				+ " >= '" + sStartingDate + " 00:00:00')"
				+ " AND (" + SMTableicpoheaders.TableName + "." + SMTableicpoheaders.datexpecteddate
				+ " <= '" + sEndingDate + " 23:59:59')"
	
				//PO dates
				+ " AND (" + SMTableicpoheaders.TableName + "." + SMTableicpoheaders.datpodate
				+ " >= '" + sStartingPODate + " 00:00:00')"
				+ " AND (" + SMTableicpoheaders.TableName + "." + SMTableicpoheaders.datpodate
				+ " <= '" + sEndingPODate + " 23:59:59')"
	
				//Locations:
				+ " AND (INSTR('" + sLocationsString + "', " + SMTableicpolines.TableName + "." 
				+ SMTableicpolines.slocation + ") > 0)"
	
				//Vendors:
				+ " AND (" + SMTableicpoheaders.TableName + "." + SMTableicpoheaders.svendor + " >=	'"
				+ sStartingVendor + "')"
				+ " AND (" + SMTableicpoheaders.TableName + "." + SMTableicpoheaders.svendor + " <=	'"
				+ sEndingVendor + "')"
				
				//And only get items that are not completely received:
				+ " AND (" + SMTableicpolines.TableName + "." + SMTableicpolines.bdqtyordered 
					+ " > " + SMTableicpolines.TableName + "." + SMTableicpolines.bdqtyreceived + ")"
				;
			}

			SQL += ")"	//Complete the 'where' clause
			+ " ORDER BY "
			+ SMTableicpoheaders.TableName + "." + SMTableicpoheaders.svendor
			+ ", " + SMTableicpoheaders.TableName + "." + SMTableicpoheaders.lid
			+ ", " + SMTableicpolines.TableName + "." + SMTableicpolines.llinenumber
			;

		try{
			if (bDebugMode){
				System.out.println("[1477078327] In " + this.toString() + " SQL: " + SQL);
			}
			ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
			while(rs.next()){
				iLineCounter++;
				String sItemNumber = rs.getString(SMTableicpolines.TableName + "." 
						+ SMTableicpolines.sitemnumber);

				//Print the line:
				out.println("<TR>");
				//TJR - 9/23/2011 - changed this so that the default number of labels is always zero:
				//BigDecimal bdNumberOfLabels = rs.getBigDecimal(SMTableicitems.TableName + "." + SMTableicitems.bdnumberoflabels);
				BigDecimal bdNumberOfLabels = BigDecimal.ZERO;
				
				//Qty
				BigDecimal bdQty = rs.getBigDecimal(SMTableicpolines.TableName + "." + SMTableicpolines.bdqtyordered);
				
				BigDecimal bdLabelTotal = new BigDecimal(0);
				
				if ((bdQty == null) || (bdNumberOfLabels == null) ){
				}else{
					bdLabelTotal = bdQty.multiply(bdNumberOfLabels).setScale (0, BigDecimal.ROUND_UP);
				}
/*
				out.println("<TD VALIGN=TOP ALIGN=RIGHT><FONT SIZE=2>" 
						+ SMUtilities.BigDecimalToFormattedString(
								"###,###,##0.0000", bdQty) 
								+ "</FONT>"
								+ "<INPUT TYPE=HIDDEN NAME='" + ICPrintReceivingLabelsAction.PARAM_QTYMARKER
								+ Integer.toString(iLineCounter)
								+ "' VALUE='" 
								+ SMUtilities.BigDecimalToFormattedString(
										"########0.0000", bdQty) + "'>"
										+ "</TD>");
*/
				
				String sName = ICPrintReceivingLabelsAction.PARAM_QTYMARKER + Integer.toString(iLineCounter);
				out.println("<TD VALIGN=TOP ALIGN=RIGHT><FONT SIZE=2>"
						+ "<INPUT TYPE=TEXT NAME=\"" + sName 
						+ "\" VALUE=\"" + clsManageBigDecimals.BigDecimalToFormattedString(
								"########0.0000", bdQty) 
								+ "\" SIZE = " + "8" 
								+ " MAXLENGTH = " + "10" 
								+ ">"
								+ "</FONT></TD>"
				);
				
				//Number of labels per unit:
				sName = ICPrintReceivingLabelsAction.PARAM_NUMPIECESMARKER 
				+ clsStringFunctions.PadLeft(Integer.toString(iLineCounter), "0", 6)
				+ sItemNumber;
				out.println("<TD VALIGN=TOP ALIGN=RIGHT><FONT SIZE=2>"
						+ "<INPUT TYPE=TEXT NAME=\"" + sName 
						+ "\" VALUE=\"" + clsManageBigDecimals.BigDecimalToFormattedString(
								"########0.0000", bdNumberOfLabels) 
								+ "\" SIZE = " + "8" 
								+ " MAXLENGTH = " + "10" 
								+ ">"
								+ "</FONT></TD>"
				);

				//Total number of labels:
				out.println("<TD VALIGN=TOP ALIGN=RIGHT><FONT SIZE=2>" 
						+ clsManageBigDecimals.BigDecimalToFormattedString(
								"###,###,##0.0000", bdLabelTotal) 
								+ "</FONT></TD>");
				
				//Vendor item number
				String sVendorItemNumber = rs.getString(SMTableicvendoritems.TableName + "." + SMTableicvendoritems.sVendorItemNumber);
				if (sVendorItemNumber == null){
					sVendorItemNumber = "";
				}
				out.println("<TD VALIGN=TOP><FONT SIZE=2>" + sVendorItemNumber + "</FONT></TD>");

				//Item
				String sItemNumberLink = "";
				if (bViewItemPermitted){
					sItemNumberLink = "<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smic.ICDisplayItemInformation?ItemNumber=" 
					+ sItemNumber
					+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
					+ "\">" + clsServletUtilities.Fill_In_Empty_String_For_HTML_Cell(sItemNumber) + "</A>";
				}else{
					sItemNumberLink = sItemNumber;
				}
				out.println(
						"<TD VALIGN=TOP><FONT SIZE=2>" 
						+ sItemNumberLink 
						+ "</FONT>"
						+ "<INPUT TYPE=HIDDEN NAME='" + ICPrintReceivingLabelsAction.PARAM_ITEMNUMMARKER
						+ Integer.toString(iLineCounter)
						+ "' VALUE='" 
						+ sItemNumber + "'>"
						+ "</TD>");

				//Description:
				out.println("<TD VALIGN=TOP><FONT SIZE=2>" 
						+ rs.getString(
								SMTableicpolines.TableName + "." + SMTableicpolines.sitemdescription) 
								+ "</FONT></TD>");

				//PO:
				String sPOID = Long.toString(rs.getLong(SMTableicpoheaders.TableName + "." 
						+ SMTableicpoheaders.lid));
				String sPOLink = sPOID;
				if (bAllowPOEditing){
					sPOLink = 
						"<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smic.ICEditPOEdit"
						+ "?" + ICPOHeader.Paramlid + "=" + sPOID
						+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID + "\">" + sPOID + "</A>";
				}
				out.println("<TD VALIGN=TOP><FONT SIZE=2>" + sPOLink + "</FONT></TD>");

				//View?
				String sPOViewLink = "N/A";
				if (bAllowPOPrinting){
					sPOViewLink = 
						"<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smic.ICPrintPOGenerate"
						+ "?" + "StartingPOID" + "=" + sPOID
						+ "&" + "EndingPOID" + "="
						+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID + "\">" + "View" + "</A>";
				}
				out.println("<TD VALIGN=TOP><FONT SIZE=2>" + sPOViewLink + "</FONT></TD>");

				//Location:
				out.println("<TD VALIGN=TOP><FONT SIZE=2>" 
						+ rs.getString(SMTableicpolines.TableName + "." 
								+ SMTableicpolines.slocation) + "</FONT></TD>");

				//Expected receipt date
				out.println("<TD VALIGN=TOP><FONT SIZE=2>" + 
						clsDateAndTimeConversions.resultsetDateStringToString(rs.getString(SMTableicpoheaders.TableName + "." 
								+ SMTableicpoheaders.datexpecteddate)) + "</FONT></TD>");

				//Reference
				out.println("<TD VALIGN=TOP><FONT SIZE=2>" 
						+ rs.getString(
								SMTableicpoheaders.TableName + "." + SMTableicpoheaders.sreference) 
								+ "</FONT></TD>");

				//Vendor
				out.println("<TD VALIGN=TOP><FONT SIZE=2>" 
						+ rs.getString(
								SMTableicpoheaders.TableName + "." + SMTableicpoheaders.svendorname) 
								+ "</FONT></TD>");

				//Comment 1
				String sComment1 = rs.getString(SMTableicitems.TableName + "." + SMTableicitems.sComment1);
				if (sComment1 == null){
					sComment1 = "";
				}
				sComment1 = sComment1.trim();
				out.println("<TD VALIGN=TOP><FONT SIZE=2>" + sComment1 + "</FONT></TD>");

				out.println("</TR>");
				if (iLineCounter > MAX_NUMBER_OF_LINES_ALLOWED){
					out.println("<TR><TD COLSPAN=13><BR><FONT COLOR=RED><B><I>"
						+ TOO_MANY_ITEMS_MESSAGE
						+ "</I></B></TD></TR>");
					break;
				}
			}
			rs.close();
		}catch (SQLException e){
			out.println("Error reading resultset - " + e.getMessage());
		}
		out.println("<INPUT TYPE=HIDDEN NAME='" 
				+ ICPrintReceivingLabelsAction.PARAM_NUMBEROFDIFFERENTLABELS 
				+ "' VALUE='" + Integer.toString(iLineCounter) + "'>");
	}
	private void printUnreceivedRowHeader(
			PrintWriter out
	){
		out.println("<TABLE BORDER=0>");
		out.println("<TR>");
		out.println("<TD ALIGN=RIGHT VALIGN=BOTTOM><B><FONT SIZE=2>Qty</FONT></B></TD>");
		out.println("<TD ALIGN=RIGHT VALIGN=BOTTOM><B><FONT SIZE=2>Labels<BR>Per&nbsp;Item</FONT></B></TD>");
		out.println("<TD ALIGN=RIGHT VALIGN=BOTTOM><B><FONT SIZE=2>Total<BR>Labels</FONT></B></TD>");
		out.println("<TD VALIGN=BOTTOM><B><FONT SIZE=2>Vendor&nbsp;item&nbsp;#</FONT></B></TD>");
		out.println("<TD VALIGN=BOTTOM><B><FONT SIZE=2>Item&nbsp;#</FONT></B></TD>");
		out.println("<TD VALIGN=BOTTOM><B><FONT SIZE=2>Description</FONT></B></TD>");
		out.println("<TD VALIGN=BOTTOM><B><FONT SIZE=2>PO&nbsp;#</FONT></B></TD>");
		out.println("<TD VALIGN=BOTTOM><B><FONT SIZE=2>View&nbsp;?</FONT></B></TD>");
		out.println("<TD VALIGN=BOTTOM><B><FONT SIZE=2>Loc.</FONT></B></TD>");
		out.println("<TD VALIGN=BOTTOM><B><FONT SIZE=2>Expected<BR>rcpt&nbsp;Date</FONT></B></TD>");
		out.println("<TD VALIGN=BOTTOM><B><FONT SIZE=2>Reference</FONT></B></TD>");
		out.println("<TD VALIGN=BOTTOM><B><FONT SIZE=2>Vendor</FONT></B></TD>");
		out.println("<TD VALIGN=BOTTOM><B><FONT SIZE=2>Comment1</FONT></B></TD>");
		out.println("</TR>");
	}
	private void printReceivedReport(
			Connection conn, 
			PrintWriter out,
			String sStartingReceiptDate,
			String sEndingReceiptDate,
			String sStartingPODate,
			String sEndingPODate,
			String sLocationsString,
			String sStartingVendor,
			String sEndingVendor,
			String sReceivedByID,
			boolean bViewItemPermitted,
			boolean bAllowPOEditing,
			boolean bAllowPOPrinting,
			String sDBID
			) throws Exception {
		
		int iLineCounter = 0;
		printReceivedRowHeader(out);
		//TJR - modify this statement:
		String SQL = "SELECT"
			+ " " + SMTableicporeceiptlines.TableName + "." + SMTableicporeceiptlines.lreceiptheaderid
			+ ", " + SMTableicporeceiptlines.TableName + "." + SMTableicporeceiptlines.sitemdescription
			+ ", " + SMTableicporeceiptlines.TableName + "." + SMTableicporeceiptlines.sitemnumber
			+ ", " + SMTableicporeceiptlines.TableName + "." + SMTableicporeceiptlines.slocation
			+ ", " + SMTableicporeceiptlines.TableName + "." + SMTableicporeceiptlines.bdqtyreceived
			+ ", " + SMTableicporeceiptheaders.TableName + "." + SMTableicporeceiptheaders.lpoheaderid
			+ ", " + SMTableicporeceiptheaders.TableName + "." + SMTableicporeceiptheaders.lid
			+ ", " + SMTableicporeceiptheaders.TableName + "." + SMTableicporeceiptheaders.datreceived
			+ ", " + SMTableicporeceiptheaders.TableName + "." + SMTableicporeceiptheaders.screatedbyfullname
			+ ", " + SMTableicpoheaders.TableName + "." + SMTableicpoheaders.sreference
			+ ", " + SMTableicpoheaders.TableName + "." + SMTableicpoheaders.svendorname
			+ ", " + SMTableicitems.TableName + "." + SMTableicitems.sComment1
			+ ", " + SMTableicvendoritems.TableName + "." + SMTableicvendoritems.sVendorItemNumber
			+ " FROM (" + SMTableicporeceiptheaders.TableName + " INNER JOIN " + SMTableicporeceiptlines.TableName
			+ " ON " + SMTableicporeceiptheaders.TableName + "." + SMTableicporeceiptheaders.lid + " = " 
			+ SMTableicporeceiptlines.TableName + "." + SMTableicporeceiptlines.lreceiptheaderid + ") LEFT JOIN " 
			+ SMTableicitems.TableName + " ON " + SMTableicporeceiptlines.TableName + "." 
			+ SMTableicporeceiptlines.sitemnumber + " = " + SMTableicitems.TableName + "." 
			+ SMTableicitems.sItemNumber
			+ " LEFT JOIN " + SMTableicpoheaders.TableName + " ON " + SMTableicporeceiptheaders.TableName + "." 
			+ SMTableicporeceiptheaders.lpoheaderid
			+ " = " + SMTableicpoheaders.TableName + "." + SMTableicpoheaders.lid
			+ " LEFT JOIN " + SMTableicvendoritems.TableName + " ON (" 
			+ "(" + SMTableicporeceiptlines.TableName + "." + SMTableicporeceiptlines.sitemnumber + " = " 
			+ SMTableicvendoritems.TableName + "." + SMTableicvendoritems.sItemNumber + ")"
			+ " AND (" + SMTableicpoheaders.TableName + "." + SMTableicpoheaders.svendor + " = " 
			+ SMTableicvendoritems.TableName + "." + SMTableicvendoritems.sVendor + ")"
			+ ")"
			+ " WHERE ("
			//Actual receipt Dates
			+ "(" + SMTableicporeceiptheaders.TableName + "." + SMTableicporeceiptheaders.datreceived
			+ " >= '" + sStartingReceiptDate + " 00:00:00')"
			+ " AND (" + SMTableicporeceiptheaders.TableName + "." + SMTableicporeceiptheaders.datreceived
			+ " <= '" + sEndingReceiptDate + " 23:59:59')"
			//PO dates
			+ " AND (" + SMTableicpoheaders.TableName + "." + SMTableicpoheaders.datpodate
			+ " >= '" + sStartingPODate + " 00:00:00')"
			+ " AND (" + SMTableicpoheaders.TableName + "." + SMTableicpoheaders.datpodate
			+ " <= '" + sEndingPODate + " 23:59:59')"

			//Locations:
			+ " AND (INSTR('" + sLocationsString + "', " + SMTableicporeceiptlines.TableName + "." 
			+ SMTableicporeceiptlines.slocation + ") > 0)"

			+ " AND (" + SMTableicpoheaders.TableName + "." + SMTableicpoheaders.svendor + " >=	'"
			+ sStartingVendor + "')"
			+ " AND (" + SMTableicpoheaders.TableName + "." + SMTableicpoheaders.svendor + " <=	'"
			+ sEndingVendor + "')"

			//Is NOT deleted:
			+ " AND (" + SMTableicporeceiptheaders.TableName + "." + SMTableicporeceiptheaders.lstatus
			+ " = " + SMTableicporeceiptheaders.STATUS_ENTERED + ")"

			//And only get inventory items:
			+ " AND (" + SMTableicporeceiptlines.TableName + "." + SMTableicporeceiptlines.lnoninventoryitem + " = 0)"
			;
			//If there is a 'received by' selected, qualify with that:
			if (sReceivedByID.compareToIgnoreCase("0") != 0){
				SQL += " AND (" + SMTableicporeceiptheaders.TableName + "." + SMTableicporeceiptheaders.lcreatedbyid 
				+ " = " + sReceivedByID + ")";
			}
			
			SQL += ")"	
			//Complete the 'where' clause
			+ " ORDER BY "
			+ SMTableicpoheaders.TableName + "." + SMTableicpoheaders.svendor
			+ ", " + SMTableicporeceiptheaders.TableName + "." + SMTableicporeceiptheaders.lid
			+ ", " + SMTableicporeceiptlines.TableName + "." + SMTableicporeceiptlines.llinenumber
			;

		try{
			if (bDebugMode){
				System.out.println("[1579203887] In " + this.toString() + " SQL: " + SQL);
			}
			ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
			while(rs.next()){

				iLineCounter++;
				String sItemNumber = rs.getString(SMTableicporeceiptlines.TableName + "." 
						+ SMTableicporeceiptlines.sitemnumber);

				//Print the line:
				out.println("<TR>");
				//TJR - 9/23/2011 - changed this so that the default number of labels is always zero:
				//BigDecimal bdNumberOfLabels = rs.getBigDecimal(SMTableicitems.TableName + "." + SMTableicitems.bdnumberoflabels);
				BigDecimal bdNumberOfLabels = BigDecimal.ZERO;
				
				//Qty
				BigDecimal bdQty = rs.getBigDecimal(SMTableicporeceiptlines.TableName + "." + SMTableicporeceiptlines.bdqtyreceived);
				
				BigDecimal bdLabelTotal = new BigDecimal(0);
				
				if ((bdQty == null) || (bdNumberOfLabels == null) ){
				}else{
					bdLabelTotal = bdQty.multiply(bdNumberOfLabels).setScale (0, BigDecimal.ROUND_UP);
				}
/*
				out.println("<TD VALIGN=TOP ALIGN=RIGHT><FONT SIZE=2>" 
						+ SMUtilities.BigDecimalToFormattedString(
								"###,###,##0.0000", bdQty) 
								+ "</FONT>"
								+ "<INPUT TYPE=HIDDEN NAME='" + ICPrintReceivingLabelsAction.PARAM_QTYMARKER
								+ Integer.toString(iLineCounter)
								+ "' VALUE='" 
								+ SMUtilities.BigDecimalToFormattedString(
										"########0.0000", bdQty) + "'>"
										+ "</TD>");
*/
				
				String sName = ICPrintReceivingLabelsAction.PARAM_QTYMARKER + Integer.toString(iLineCounter);
				out.println("<TD VALIGN=TOP ALIGN=RIGHT><FONT SIZE=2>"
						+ "<INPUT TYPE=TEXT NAME=\"" + sName 
						+ "\" VALUE=\"" + clsManageBigDecimals.BigDecimalToFormattedString(
								"########0.0000", bdQty) 
								+ "\" SIZE = " + "8" 
								+ " MAXLENGTH = " + "10" 
								+ ">"
								+ "</FONT></TD>"
				);
				
				//Number of labels per unit:
				sName = ICPrintReceivingLabelsAction.PARAM_NUMPIECESMARKER 
				+ clsStringFunctions.PadLeft(Integer.toString(iLineCounter), "0", 6)
				+ sItemNumber;
				out.println("<TD VALIGN=TOP ALIGN=RIGHT><FONT SIZE=2>"
						+ "<INPUT TYPE=TEXT NAME=\"" + sName 
						+ "\" VALUE=\"" + clsManageBigDecimals.BigDecimalToFormattedString(
								"########0.0000", bdNumberOfLabels) 
								+ "\" SIZE = " + "8" 
								+ " MAXLENGTH = " + "10" 
								+ ">"
								+ "</FONT></TD>"
				);

				//Total number of labels:
				out.println("<TD VALIGN=TOP ALIGN=RIGHT><FONT SIZE=2>" 
						+ clsManageBigDecimals.BigDecimalToFormattedString(
								"###,###,##0.0000", bdLabelTotal) 
								+ "</FONT></TD>");
				
				//Vendor item number
				String sVendorItemNumber = rs.getString(SMTableicvendoritems.TableName + "." + SMTableicvendoritems.sVendorItemNumber);
				if (sVendorItemNumber == null){
					sVendorItemNumber = "";
				}
				out.println("<TD VALIGN=TOP><FONT SIZE=2>" + sVendorItemNumber + "</FONT></TD>");

				//Item
				String sItemNumberLink = "";
				if (bViewItemPermitted){
					sItemNumberLink = "<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smic.ICDisplayItemInformation?ItemNumber=" 
					+ sItemNumber
					+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
					+ "\">" + clsServletUtilities.Fill_In_Empty_String_For_HTML_Cell(sItemNumber) + "</A>";
				}else{
					sItemNumberLink = sItemNumber;
				}
				out.println(
						"<TD VALIGN=TOP><FONT SIZE=2>" 
						+ sItemNumberLink 
						+ "</FONT>"
						+ "<INPUT TYPE=HIDDEN NAME='" + ICPrintReceivingLabelsAction.PARAM_ITEMNUMMARKER
						+ Integer.toString(iLineCounter)
						+ "' VALUE='" 
						+ sItemNumber + "'>"
						+ "</TD>");

				//Description:
				out.println("<TD VALIGN=TOP><FONT SIZE=2>" 
						+ rs.getString(
								SMTableicporeceiptlines.TableName + "." + SMTableicporeceiptlines.sitemdescription) 
								+ "</FONT></TD>");

				//PO:
				String sPOID = Long.toString(rs.getLong(SMTableicporeceiptheaders.TableName + "." 
						+ SMTableicporeceiptheaders.lpoheaderid));
				String sPOLink = sPOID;
				if (bAllowPOEditing){
					sPOLink = 
						"<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smic.ICEditPOEdit"
						+ "?" + ICPOHeader.Paramlid + "=" + sPOID
						+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID + "\">" + sPOID + "</A>";
				}
				out.println("<TD VALIGN=TOP><FONT SIZE=2>" + sPOLink + "</FONT></TD>");

				//Receipt #
				String sReceiptID = Long.toString(rs.getLong(SMTableicporeceiptheaders.lid));
				String sReceiptViewLink = "N/A";
				if (bAllowPOEditing){
					sReceiptViewLink = 
						"<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) 
						+ "smic.ICEditReceiptEdit?lid=" + sReceiptID 
						+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
						+ "\">" + sReceiptID + "</A>";
				}
				out.println("<TD VALIGN=TOP><FONT SIZE=2>" + sReceiptViewLink + "</FONT></TD>");

				//Location:
				out.println("<TD VALIGN=TOP><FONT SIZE=2>" 
						+ rs.getString(SMTableicporeceiptlines.TableName + "." 
								+ SMTableicporeceiptlines.slocation) + "</FONT></TD>");

				//Actual receipt date
				out.println("<TD VALIGN=TOP><FONT SIZE=2>" + 
						clsDateAndTimeConversions.resultsetDateStringToString(rs.getString(SMTableicporeceiptheaders.TableName + "." 
								+ SMTableicporeceiptheaders.datreceived)) + "</FONT></TD>");

				//Received by
				out.println("<TD VALIGN=TOP><FONT SIZE=2>" 
						+ rs.getString(
								SMTableicporeceiptheaders.TableName + "." + SMTableicporeceiptheaders.screatedbyfullname).trim().replace(" ", "&nbsp;") 
								+ "</FONT></TD>");
				
				//Reference
				out.println("<TD VALIGN=TOP><FONT SIZE=2>" 
						+ rs.getString(
								SMTableicpoheaders.TableName + "." + SMTableicpoheaders.sreference) 
								+ "</FONT></TD>");

				//Vendor
				out.println("<TD VALIGN=TOP><FONT SIZE=2>" 
						+ rs.getString(
								SMTableicpoheaders.TableName + "." + SMTableicpoheaders.svendorname) 
								+ "</FONT></TD>");

				//Comment 1
				String sComment1 = rs.getString(SMTableicitems.TableName + "." + SMTableicitems.sComment1);
				if (sComment1 == null){
					sComment1 = "";
				}
				sComment1 = sComment1.trim();
				out.println("<TD VALIGN=TOP><FONT SIZE=2>" + sComment1 + "</FONT></TD>");

				out.println("</TR>");
				
				if (iLineCounter > MAX_NUMBER_OF_LINES_ALLOWED){
					out.println("<TR><TD COLSPAN=13><BR><FONT COLOR=RED><B><I>"
						+ TOO_MANY_ITEMS_MESSAGE
						+ "</I></B></TD></TR>");
					break;
				}
			}
			rs.close();
		}catch (SQLException e){
			out.println("Error reading resultset - " + e.getMessage());
		}
		out.println("<INPUT TYPE=HIDDEN NAME='" 
			+ ICPrintReceivingLabelsAction.PARAM_NUMBEROFDIFFERENTLABELS 
			+ "' VALUE='" + Integer.toString(iLineCounter) + "'>");
	}
	private void printReceivedRowHeader(
			PrintWriter out
	){
		out.println("<TABLE BORDER=0>");
		out.println("<TR>");
		out.println("<TD ALIGN=RIGHT VALIGN=BOTTOM><B><FONT SIZE=2>Qty&nbsp;Rcvd</FONT></B></TD>");
		out.println("<TD ALIGN=RIGHT VALIGN=BOTTOM><B><FONT SIZE=2>Labels<BR>Per&nbsp;Item</FONT></B></TD>");
		out.println("<TD ALIGN=RIGHT VALIGN=BOTTOM><B><FONT SIZE=2>Total<BR>Labels</FONT></B></TD>");
		out.println("<TD VALIGN=BOTTOM><B><FONT SIZE=2>Vendor&nbsp;item&nbsp;#</FONT></B></TD>");
		out.println("<TD VALIGN=BOTTOM><B><FONT SIZE=2>Item&nbsp;#</FONT></B></TD>");
		out.println("<TD VALIGN=BOTTOM><B><FONT SIZE=2>Description</FONT></B></TD>");
		out.println("<TD VALIGN=BOTTOM><B><FONT SIZE=2>PO&nbsp;#</FONT></B></TD>");
		out.println("<TD VALIGN=BOTTOM><B><FONT SIZE=2>Rcpt&nbsp;#</FONT></B></TD>");
		out.println("<TD VALIGN=BOTTOM><B><FONT SIZE=2>Loc.</FONT></B></TD>");
		out.println("<TD VALIGN=BOTTOM><B><FONT SIZE=2>Actual<BR>Rcpt&nbsp;Date</FONT></B></TD>");
		out.println("<TD VALIGN=BOTTOM><B><FONT SIZE=2>Received&nbsp;By</FONT></B></TD>");
		out.println("<TD VALIGN=BOTTOM><B><FONT SIZE=2>Reference</FONT></B></TD>");
		out.println("<TD VALIGN=BOTTOM><B><FONT SIZE=2>Vendor</FONT></B></TD>");
		out.println("<TD VALIGN=BOTTOM><B><FONT SIZE=2>Comment1</FONT></B></TD>");
		out.println("</TR>");
	}
}
