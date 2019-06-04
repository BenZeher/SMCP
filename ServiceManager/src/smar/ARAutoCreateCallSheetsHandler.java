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
		String sDBID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_DATABASE_ID);
		String sUserID = (String)CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERID);
		String sUserFullName = (String)CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERFIRSTNAME) + " " +
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
				//+ "&ParameterString=*" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
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
				//+ "&ParameterString=*" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
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
			
			try {
				validate_params(
						sStartingCustomer,
						sEndingCustomer,
						sStartingDate,
						sEndingDate,
						arrOrderTypes
				);
			} catch (Exception e1) {
				sRedirectString += "&Warning=" + e1.getMessage();
				response.sendRedirect(sRedirectString);
				return;
			}
			sRedirectString += "&" + ARAutoCreateCallSheetsSelection.PARAM_RUNREFRESH + "=Y";
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

			try {
				validate_params(
						sStartingCustomer,
						sEndingCustomer,
						sStartingDate,
						sEndingDate,
						arrOrderTypes
				);
			} catch (Exception e1) {
				sRedirectString += "&Warning=" + e1.getMessage();
				response.sendRedirect(sRedirectString);
				return;
			}
			
			int iNumberOfInvoices = 0;
			try{
				iNumberOfInvoices = createCallSheets(
					request,
					sDBID,
					sUserID,
					sUserFullName
				);
			}catch(Exception e1){
				sRedirectString += "&Warning=" + e1.getMessage();
				response.sendRedirect(sRedirectString);
				return;
			}
			sRedirectString += "&Status=Successfully created " + Integer.toString(iNumberOfInvoices) + " new call sheet(s).";
			response.sendRedirect(sRedirectString);
			return;
		}
		return;
	}
	private void validate_params(
			String sStartingCustomer,
			String sEndingCustomer,
			String sStartingDate,
			String sEndingDate,
			ArrayList<String>arrOrdTypes		
	)throws Exception{

		if (sStartingCustomer.compareToIgnoreCase(sEndingCustomer) > 0){
			throw new Exception("Invalid input - starting customer is higher than ending customer.");
		}
		Date datStart;
		try {
			datStart = clsDateAndTimeConversions.StringTojavaSQLDate("MM/dd/yyyy", sStartingDate);
		} catch (ParseException e) {
			throw new Exception("Invalid starting date: '" + sStartingDate + "'.");
		}
		Date datEnd;
		try {
			datEnd = clsDateAndTimeConversions.StringTojavaSQLDate("MM/dd/yyyy", sEndingDate);
		} catch (ParseException e) {
			throw new Exception("Invalid ending date: '" + sEndingDate + "'.");
		}
		if (datStart.compareTo(datEnd) > 0){
			throw new Exception("Invalid input - starting date cannot be later than ending date");
		}

		if (arrOrdTypes.size() < 1){
			throw new Exception("You must choose at least one order type.");
		}
		return;
	}
	private int createCallSheets(
			HttpServletRequest req,
			String sDBID,
			String sUserID,
			String sUserFullName
	)throws Exception{

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
			throw new Exception("You must select at least one invoice.");
		}
		
		//Get a connection then start creating call sheets:
		Connection conn = clsDatabaseFunctions.getConnection(
			getServletContext(), 
			sDBID, 
			"MySQL", 
			this.toString() + ".createCallSheets - user: " 
			+ sUserID
			+ " - "
			+ sUserFullName
				)
		;
		
		if (conn == null){
			throw new Exception("Could not open connection to create call sheets.");
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
				clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1547067483]");
				throw new Exception("Could not get user's initials.");
			}
		} catch (SQLException e1) {
			clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1547067484]");
			throw new Exception("Could not get user's initials - " + e1.getMessage());
		}
		
		//Start a data connection
		if (!clsDatabaseFunctions.start_data_transaction(conn)){
			clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1547067485]");
			throw new Exception("Could not start data transaction");
		}
		
		for (int i = 0; i < arrInvoices.size(); i ++){
			try {
				createIndividualCallSheet(arrInvoices.get(i), sUserInitials, conn);
			} catch (Exception e1) {
				clsDatabaseFunctions.rollback_data_transaction(conn);
				clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1547067486]");
				throw new Exception("Error [1548694924] creating call sheets - " + e1.getMessage());
			}
		}
		if (!clsDatabaseFunctions.commit_data_transaction(conn)){
			clsDatabaseFunctions.rollback_data_transaction(conn);
			clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1547067487]");
			throw new Exception("Error [1548694925] committing data transaction.");
		}

		clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1547067488]");
		return arrInvoices.size();
	}
	private void createIndividualCallSheet(String sInvNumber, String sUserInitials, Connection conn) throws Exception{
		
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
			throw new Exception("Error [1548695350] inserting call sheet with SQL: " + clsServletUtilities.URLEncode(SQL) + " - " + e.getMessage());
		}
		
		return;
	}
	public void doGet(HttpServletRequest request,
			HttpServletResponse response)
	throws ServletException, IOException {

		doPost(request, response);
	}
}