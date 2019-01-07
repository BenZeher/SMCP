package smar;

import java.io.IOException;
import java.sql.Connection;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Enumeration;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import smcontrolpanel.SMAuthenticate;
import smcontrolpanel.SMSystemFunctions;
import smcontrolpanel.SMUtilities;
import SMDataDefinition.SMTablearcustomer;
import SMDataDefinition.SMTablecallsheets;
import SMDataDefinition.SMTableinvoiceheaders;
import SMDataDefinition.SMTableusers;
import ServletUtilities.clsServletUtilities;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsDateAndTimeConversions;
import ServletUtilities.clsManageRequestParameters;
import ServletUtilities.clsStringFunctions;

public class ARAutoCreateCallSheetsHandler extends HttpServlet{

	private static final long serialVersionUID = 1L;
	private String sDBID = "";
	private String sUserID = "";
	private String sUserFullName = "";
	private String sWarning = "";
	private String sStatus = "";
	public void doPost(HttpServletRequest request,
			HttpServletResponse response)
	throws ServletException, IOException {

		if (!SMAuthenticate.authenticateSMCPCredentials(
				request, 
				response, 
				getServletContext(), 
				SMSystemFunctions.ARAutoCreateCallSheets)
		){
			return;
		}

		//Get the session info:
		HttpSession CurrentSession = request.getSession(true);
		sDBID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_DATABASE_ID);
		sUserID = (String)CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERID);
		sUserFullName = (String)CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERFIRSTNAME) + " " +
						(String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERLASTNAME);

		String sCallingClass = clsManageRequestParameters.get_Request_Parameter("CallingClass", request);
		String sRedirectString = "";

		String sStartingCustomer = clsManageRequestParameters.get_Request_Parameter(
				ARAutoCreateCallSheetsSelection.STARTING_CUSTOMER_FIELD, request).trim();
		String sEndingCustomer = clsManageRequestParameters.get_Request_Parameter(
				ARAutoCreateCallSheetsSelection.ENDING_CUSTOMER_FIELD, request).trim();
		String sStartingDate = clsManageRequestParameters.get_Request_Parameter(
				ARAutoCreateCallSheetsSelection.STARTING_DATE_FIELD, request).trim();
		String sEndingDate = clsManageRequestParameters.get_Request_Parameter(
				ARAutoCreateCallSheetsSelection.ENDING_DATE_FIELD, request).trim();
		ArrayList<String>arrOrderTypes = new ArrayList<String>(0);
		Enumeration <String> e = request.getParameterNames();
		String sParam = "";
		while (e.hasMoreElements()){
			sParam = e.nextElement();
			if (sParam.contains(ARAutoCreateCallSheetsSelection.PARAM_SERVICETYPE)){
				arrOrderTypes.add(sParam.substring(
						ARAutoCreateCallSheetsSelection.PARAM_SERVICETYPE.length(), sParam.length()));
			}
		}

		String sParamString = "";
		if (sCallingClass.compareToIgnoreCase("") != 0){
			sParamString += "*CallingClass=" + sCallingClass;
		}
		if (sStartingCustomer.compareToIgnoreCase("") != 0){
			sParamString += "*" + ARAutoCreateCallSheetsSelection.STARTING_CUSTOMER_FIELD + "=" + sStartingCustomer;
		}
		if (sEndingCustomer.compareToIgnoreCase("") != 0){
			sParamString += "*" + ARAutoCreateCallSheetsSelection.ENDING_CUSTOMER_FIELD + "=" + sEndingCustomer;
		}
		if (sStartingDate.compareToIgnoreCase("") != 0){
			sParamString += "*" + ARAutoCreateCallSheetsSelection.STARTING_DATE_FIELD + "=" + sStartingDate;
		}
		if (sEndingDate.compareToIgnoreCase("") != 0){
			sParamString += "*" + ARAutoCreateCallSheetsSelection.ENDING_DATE_FIELD + "=" + sEndingDate;
		}
		for (int i = 0; i < arrOrderTypes.size(); i++){
			sParamString += "*" + ARAutoCreateCallSheetsSelection.PARAM_SERVICETYPE + arrOrderTypes.get(i) + "=Y";
		}
		//System.out.println("In " + this.toString() + " sParamString = " + sParamString);
		//Special cases - if this class was called by a finder for the 'starting customer' field:
		if (request.getParameter(ARAutoCreateCallSheetsSelection.FIND_STARTING_CUSTOMER_BUTTON_NAME) != null){
			//Then call the finder to search for customers:
			sRedirectString = 
				"" + SMUtilities.getURLLinkBase(getServletContext()) + "smar.ObjectFinder"
				+ "?" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
				+ "&ObjectName=Customer"
				+ "&ResultClass=FinderResults"
				+ "&SearchingClass=smar.ARAutoCreateCallSheetsSelection"
				+ "&ReturnField=" + ARAutoCreateCallSheetsSelection.STARTING_CUSTOMER_FIELD
				+ "&SearchField1=" + SMTablearcustomer.sCustomerName
				+ "&SearchFieldAlias1=Name"
				+ "&SearchField2=" + SMTablearcustomer.sCustomerNumber
				+ "&SearchFieldAlias2=Customer%20Code"
				+ "&SearchField3=" + SMTablearcustomer.sAddressLine1
				+ "&SearchFieldAlias3=Address%20Line%201"
				+ "&SearchField4=" + SMTablearcustomer.sPhoneNumber
				+ "&SearchFieldAlias4=Phone"
				+ "&ResultListField1="  + SMTablearcustomer.sCustomerNumber
				+ "&ResultHeading1=Customer%20Number"
				+ "&ResultListField2="  + SMTablearcustomer.sCustomerName
				+ "&ResultHeading2=Customer%20Name"
				+ "&ResultListField3="  + SMTablearcustomer.sAddressLine1
				+ "&ResultHeading3=Address%20Line%201"
				+ "&ResultListField4="  + SMTablearcustomer.sPhoneNumber
				+ "&ResultHeading4=Phone"
				+ "&ResultListField5="  + SMTablearcustomer.sCustomerGroup
				+ "&ResultHeading5=Customer%20Group"
				+ "&ParameterString=*" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
				//+ "*" + "EndingCustomer=" + SMUtilities.get_Request_Parameter(ARAutoCreateCallSheetsSelection.ENDING_CUSTOMER_FIELD, request)
				+ sParamString
				;
			response.sendRedirect(sRedirectString);
			return;
		}

		if (request.getParameter(ARAutoCreateCallSheetsSelection.FIND_ENDING_CUSTOMER_BUTTON_NAME) != null){
			//Then call the finder to search for customers:
			sRedirectString = 
				"" + SMUtilities.getURLLinkBase(getServletContext()) + "smar.ObjectFinder"
				+ "?" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
				+ "&ObjectName=Customer"
				+ "&ResultClass=FinderResults"
				+ "&SearchingClass=smar.ARAutoCreateCallSheetsSelection"
				+ "&ReturnField=" + ARAutoCreateCallSheetsSelection.ENDING_CUSTOMER_FIELD
				+ "&SearchField1=" + SMTablearcustomer.sCustomerName
				+ "&SearchFieldAlias1=Name"
				+ "&SearchField2=" + SMTablearcustomer.sCustomerNumber
				+ "&SearchFieldAlias2=Customer%20Code"
				+ "&SearchField3=" + SMTablearcustomer.sAddressLine1
				+ "&SearchFieldAlias3=Address%20Line%201"
				+ "&SearchField4=" + SMTablearcustomer.sPhoneNumber
				+ "&SearchFieldAlias4=Phone"
				+ "&ResultListField1="  + SMTablearcustomer.sCustomerNumber
				+ "&ResultHeading1=Customer%20Number"
				+ "&ResultListField2="  + SMTablearcustomer.sCustomerName
				+ "&ResultHeading2=Customer%20Name"
				+ "&ResultListField3="  + SMTablearcustomer.sAddressLine1
				+ "&ResultHeading3=Address%20Line%201"
				+ "&ResultListField4="  + SMTablearcustomer.sPhoneNumber
				+ "&ResultHeading4=Phone"
				+ "&ResultListField5="  + SMTablearcustomer.sCustomerGroup
				+ "&ResultHeading5=Customer%20Group"
				+ "&ParameterString=*" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
				//+ "*" + "EndingCustomer=" + SMUtilities.get_Request_Parameter(ARAutoCreateCallSheetsSelection.ENDING_CUSTOMER_FIELD, request)
				+ sParamString
				;
			response.sendRedirect(sRedirectString);
			return;
		}

		if (request.getParameter(ARAutoCreateCallSheetsSelection.REFRESHLIST_BUTTON_NAME) != null){

			//Then refresh the list on the screen:
			sRedirectString = 
				"" + SMUtilities.getURLLinkBase(getServletContext()) + sCallingClass
				+ "?" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
				+ "&" + ARAutoCreateCallSheetsSelection.STARTING_CUSTOMER_FIELD + "=" 
				+ clsServletUtilities.URLEncode(sStartingCustomer)
				+ "&" + ARAutoCreateCallSheetsSelection.ENDING_CUSTOMER_FIELD + "=" 
				+ clsServletUtilities.URLEncode(sEndingCustomer)
				+ "&" + ARAutoCreateCallSheetsSelection.STARTING_DATE_FIELD + "=" 
				+ clsServletUtilities.URLEncode(sStartingDate)
				+ "&" + ARAutoCreateCallSheetsSelection.ENDING_DATE_FIELD + "=" 
				+ clsServletUtilities.URLEncode(sEndingDate)
				;
			for (int i = 0; i < arrOrderTypes.size(); i++){
				sRedirectString += "&" + clsServletUtilities.URLEncode(ARAutoCreateCallSheetsSelection.PARAM_SERVICETYPE 
						+ arrOrderTypes.get(i)) + "=Y";
			}

			if(!validate_params(
					sStartingCustomer,
					sEndingCustomer,
					sStartingDate,
					sEndingDate,
					arrOrderTypes
			)){
				sRedirectString += "&Warning=" + sWarning;
			}else{
				sRedirectString += "&" + ARAutoCreateCallSheetsSelection.PARAM_RUNREFRESH + "=Y";
			}

			response.sendRedirect(sRedirectString);
			return;
		}

		if (request.getParameter(ARAutoCreateCallSheetsSelection.CREATECALLSHEETS_BUTTON_NAME) != null){

			//Then build a redirect string:
			sRedirectString = 
				"" + SMUtilities.getURLLinkBase(getServletContext()) + sCallingClass
				+ "?" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
				+ "&" + ARAutoCreateCallSheetsSelection.STARTING_CUSTOMER_FIELD + "=" 
				+ clsServletUtilities.URLEncode(sStartingCustomer)
				+ "&" + ARAutoCreateCallSheetsSelection.ENDING_CUSTOMER_FIELD + "=" 
				+ clsServletUtilities.URLEncode(sEndingCustomer)
				+ "&" + ARAutoCreateCallSheetsSelection.STARTING_DATE_FIELD + "=" 
				+ clsServletUtilities.URLEncode(sStartingDate)
				+ "&" + ARAutoCreateCallSheetsSelection.ENDING_DATE_FIELD + "=" 
				+ clsServletUtilities.URLEncode(sEndingDate)
				;
			for (int i = 0; i < arrOrderTypes.size(); i++){
				sRedirectString += "&" + clsServletUtilities.URLEncode(ARAutoCreateCallSheetsSelection.PARAM_SERVICETYPE 
						+ arrOrderTypes.get(i)) + "=Y";
			}

			if(!validate_params(
					sStartingCustomer,
					sEndingCustomer,
					sStartingDate,
					sEndingDate,
					arrOrderTypes
			)){
				sRedirectString += "&Warning=" + sWarning;
			}else{
				if (!createCallSheets(
					request,
					sDBID,
					sUserID,
					sUserFullName
				)){
					sRedirectString += "&Warning=" + sWarning;
				}else{
					sRedirectString += "&Status=" + sStatus;
				}
			}

			response.sendRedirect(sRedirectString);
			return;
		}
		return;
	}
	private boolean validate_params(
			String sStartingCustomer,
			String sEndingCustomer,
			String sStartingDate,
			String sEndingDate,
			ArrayList<String>arrOrdTypes		
	){

		if (sStartingCustomer.compareToIgnoreCase(sEndingCustomer) > 0){
			sWarning = "Invalid input - starting customer is higher than ending customer.";
			return false;
		}
		Date datStart;
		try {
			datStart = clsDateAndTimeConversions.StringTojavaSQLDate("MM/dd/yyyy", sStartingDate);
		} catch (ParseException e) {
			sWarning = "Invalid starting date: '" + sStartingDate + "'.";
			return false;
		}
		Date datEnd;
		try {
			datEnd = clsDateAndTimeConversions.StringTojavaSQLDate("MM/dd/yyyy", sEndingDate);
		} catch (ParseException e) {
			sWarning = "Invalid ending date: '" + sEndingDate + "'.";
			return false;
		}
		if (datStart.compareTo(datEnd) > 0){
			sWarning = "Invalid input - starting date cannot be later than ending date";
			return false;
		}

		if (arrOrdTypes.size() < 1){
			sWarning = "You must choose at least one order type.";
			return false;
		}
		return true;
	}
	private boolean createCallSheets(
			HttpServletRequest req,
			String sConf,
			String sUserID,
			String sUserFullName
	){

		//Pick off the invoice numbers from the request:
		ArrayList<String>arrInvoices = new ArrayList<String>(0);
		Enumeration <String> e = req.getParameterNames();
		String sParam = "";
		while (e.hasMoreElements()){
			sParam = e.nextElement();
			if (sParam.contains(ARAutoCreateCallSheetsSelection.PARAM_INVNUMBER)){
				arrInvoices.add(sParam.substring(
						ARAutoCreateCallSheetsSelection.PARAM_INVNUMBER.length(), sParam.length()));
			}
		}
		
		if (arrInvoices.size() < 1){
			sWarning = "You must select at least one invoice.";
			return false;
		}
		
		//Get a connection then start creating call sheets:
		Connection conn = clsDatabaseFunctions.getConnection(
			getServletContext(), 
			sConf, 
			"MySQL", 
			this.toString() + ".createCallSheets - user: " 
			+ sUserID
			+ " - "
			+ sUserFullName
				)
		;
		
		if (conn == null){
			sWarning = "Could not open connection to create call sheets.";
			return false;
		}
		
		//Get the current user's initials:
		String sUserInitials = "";
		String SQL = "SELECT"
			+ " " + SMTableusers.sIdentifierInitials
			+ " FROM " + SMTableusers.TableName
			+ " WHERE ("
				+ "(" + SMTableusers.lid + " = " + sUserID + ")"
			+ ")"
			;
		try {
			ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
			if (rs.next()){
				sUserInitials = rs.getString(SMTableusers.sIdentifierInitials);
				rs.close();
			}else{
				rs.close();
				clsDatabaseFunctions.freeConnection(getServletContext(), conn);
				sWarning = "Could not get user's initials.";
				return false;
			}
		} catch (SQLException e1) {
			clsDatabaseFunctions.freeConnection(getServletContext(), conn);
			sWarning = "Could not get user's initials - " + e1.getMessage();
			return false;
		}
		
		//Start a data connection
		if (!clsDatabaseFunctions.start_data_transaction(conn)){
			clsDatabaseFunctions.freeConnection(getServletContext(), conn);
			sWarning = "Could not start data transaction";
			return false;
		}
		
		for (int i = 0; i < arrInvoices.size(); i ++){
			if (!createIndividualCallSheet(arrInvoices.get(i), sUserInitials, conn)){
				clsDatabaseFunctions.rollback_data_transaction(conn);
				clsDatabaseFunctions.freeConnection(getServletContext(), conn);
				return false;
			}
		}
		if (!clsDatabaseFunctions.commit_data_transaction(conn)){
			clsDatabaseFunctions.rollback_data_transaction(conn);
			clsDatabaseFunctions.freeConnection(getServletContext(), conn);
			return false;
		}

		clsDatabaseFunctions.freeConnection(getServletContext(), conn);
		sStatus = "Successfully created " + Integer.toString(arrInvoices.size()) + " new call sheet(s).";
		return true;
	}
	private boolean createIndividualCallSheet(String sInvNumber, String sUserInitials, Connection conn){
		
		String SQL = "INSERT INTO " + SMTablecallsheets.TableName
			+ "("
			+ SMTablecallsheets.datLastContact
			+ ", " + SMTablecallsheets.datNextContact
			+ ", " + SMTablecallsheets.mNotes
			+ ", " + SMTablecallsheets.sAlertInits
			+ ", " + SMTablecallsheets.sAccountTerms
			+ ", " + SMTablecallsheets.sAcct
			+ ", " + SMTablecallsheets.sCallSheetName
			+ ", " + SMTablecallsheets.sCollector
			+ ", " + SMTablecallsheets.sCustomerName
			+ ", " + SMTablecallsheets.sJobPhone
			+ ", " + SMTablecallsheets.sOrderNumber
			+ ", " + SMTablecallsheets.sPhone
			//+ ", " + SMTablecallsheets.sResponsibility
			+ ")"
			+ " SELECT "
			+ "NOW()"
			+ ", DATE_ADD(" + SMTableinvoiceheaders.TableName + "." + SMTableinvoiceheaders.datInvoiceDate 
			+ ", INTERVAL 30 DAY)"
			+ ", ''"
			+ ", ''"
			+ ", " + SMTableinvoiceheaders.sTerms
			+ ", " + SMTableinvoiceheaders.sCustomerCode
			+ ", CONCAT(TRIM(" + SMTableinvoiceheaders.sOrderNumber 
				+ "),' - ', TRIM(" + SMTableinvoiceheaders.sShipToName + "))"

			+ ", '" + sUserInitials + "'"
			+ ", LEFT(TRIM(" + SMTableinvoiceheaders.sBillToName +")," 
				+ Integer.toString(SMTablecallsheets.sCustomerNameLength) + ")"
			+ ", " + SMTableinvoiceheaders.sShipToPhone
			+ ", TRIM(" + SMTableinvoiceheaders.sOrderNumber + ")"
			+ ", " + SMTableinvoiceheaders.sBillToPhone
			//+ ", " + SMTableinvoiceheaders.sSalesperson
			
			+ " FROM " + SMTableinvoiceheaders.TableName
			+ " WHERE ("
				+ "(" + SMTableinvoiceheaders.sInvoiceNumber 
					+ " = '" + clsStringFunctions.PadLeft(sInvNumber.trim(), " ", 8) + "')"
			+ ")"
			;
		try {
			Statement stmt = conn.createStatement();
			stmt.executeUpdate(SQL);
		} catch (SQLException e) {
			sWarning = "Error inserting call sheet with SQL: " + clsServletUtilities.URLEncode(SQL) + " - " + e.getMessage();
			return false;
		}
		
		return true;
	}
	public void doGet(HttpServletRequest request,
			HttpServletResponse response)
	throws ServletException, IOException {

		doPost(request, response);
	}
}