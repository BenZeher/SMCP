package smar;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.Date;
import java.text.ParseException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import smcontrolpanel.SMAuthenticate;
import smcontrolpanel.SMMasterEditSelect;
import smcontrolpanel.SMSystemFunctions;
import smcontrolpanel.SMUtilities;
import ConnectionPool.WebContextParameters;
import SMClasses.SMLogEntry;
import SMDataDefinition.SMTablearcustomer;
import SMDataDefinition.SMTablecallsheets;
import ServletUtilities.clsServletUtilities;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsDateAndTimeConversions;
import ServletUtilities.clsManageRequestParameters;

public class ARPrintCallSheetsGenerate extends HttpServlet{

	private static final long serialVersionUID = 1L;
	public void doPost(HttpServletRequest request,
			HttpServletResponse response)
	throws ServletException, IOException {

		if (!SMAuthenticate.authenticateSMCPCredentials(
				request, 
				response, 
				getServletContext(), 
				SMSystemFunctions.ARPrintCallSheets)
		){
			return;
		}

		PrintWriter out = response.getWriter();
		//Get the session info:
		HttpSession CurrentSession = request.getSession(true);
		String sDBID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_DATABASE_ID);
		String sUserID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERID);
		String sUserFullName = (String)CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERFIRSTNAME) + " "
				+ (String)CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERLASTNAME);
		String sCompanyName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_COMPANYNAME);

		String sCallingClass = clsManageRequestParameters.get_Request_Parameter("CallingClass", request);
		//String sWarning = "";
		String sRedirectString = "";

		String sCallSheetID = clsManageRequestParameters.get_Request_Parameter(
				ARPrintCallSheetsSelection.CALLSHEET_ID_FIELD, request).trim();
		String sStartingCustomer = clsManageRequestParameters.get_Request_Parameter(
				ARPrintCallSheetsSelection.STARTING_CUSTOMER_FIELD, request).trim();
		String sEndingCustomer = clsManageRequestParameters.get_Request_Parameter(
				ARPrintCallSheetsSelection.ENDING_CUSTOMER_FIELD, request).trim();
		String sCollector = clsManageRequestParameters.get_Request_Parameter(
				ARPrintCallSheetsSelection.COLLECTOR_FIELD, request).trim();
		String sResponsibility = clsManageRequestParameters.get_Request_Parameter(
				ARPrintCallSheetsSelection.RESPONSIBILITY_FIELD, request).trim();
		String sStartingNextContactDate = clsManageRequestParameters.get_Request_Parameter(
				ARPrintCallSheetsSelection.STARTING_NEXT_CONTACT_DATE_FIELD, request).trim();
		String sEndingNextContactDate = clsManageRequestParameters.get_Request_Parameter(
				ARPrintCallSheetsSelection.ENDING_NEXT_CONTACT_DATE_FIELD, request).trim();
		String sStartingLastContactDate = clsManageRequestParameters.get_Request_Parameter(
				ARPrintCallSheetsSelection.STARTING_LAST_CONTACT_DATE_FIELD, request).trim();
		String sEndingLastContactDate = clsManageRequestParameters.get_Request_Parameter(
				ARPrintCallSheetsSelection.ENDING_LAST_CONTACT_DATE_FIELD, request).trim();
		String sOrderNumber = clsManageRequestParameters.get_Request_Parameter(
				ARPrintCallSheetsSelection.ORDERNUMBER_FIELD, request).trim();
		boolean bPrintWithNotes = request.getParameter(ARPrintCallSheetsSelection.PRINTWITHNOTES_FIELD) != null;
		boolean bOnlyPrintAlerts = request.getParameter(ARPrintCallSheetsSelection.PRINTONLYALERTS_FIELD) != null;
		boolean bPrintZeroBalanceCustomers = request.getParameter(ARPrintCallSheetsSelection.PRINTZEROBALANCECUSTOMERS_FIELD) != null;
		boolean bPrintWithResponsibilityOnly = request.getParameter(ARPrintCallSheetsSelection.PRINTWITHRESPONSIBILITYONLY_FIELD) != null;

		String sParamString = "";
		if (sCallingClass.compareToIgnoreCase("") != 0){
			sParamString += "*CallingClass=" + sCallingClass;
		}
		if (sCallSheetID.compareToIgnoreCase("") != 0){
			sParamString += "*" + ARPrintCallSheetsSelection.CALLSHEET_ID_FIELD + "=" + sCallSheetID;
		}
		if (sStartingCustomer.compareToIgnoreCase("") != 0){
			sParamString += "*" + ARPrintCallSheetsSelection.STARTING_CUSTOMER_FIELD + "=" + sStartingCustomer;
		}
		if (sEndingCustomer.compareToIgnoreCase("") != 0){
			sParamString += "*" + ARPrintCallSheetsSelection.ENDING_CUSTOMER_FIELD + "=" + sEndingCustomer;
		}
		if (sCollector.compareToIgnoreCase("") != 0){
			sParamString += "*" + ARPrintCallSheetsSelection.COLLECTOR_FIELD + "=" + sCollector;
		}
		if (sResponsibility.compareToIgnoreCase("") != 0){
			sParamString += "*" + ARPrintCallSheetsSelection.RESPONSIBILITY_FIELD + "=" + sResponsibility;
		}
		if (sStartingNextContactDate.compareToIgnoreCase("") != 0){
			sParamString += "*" + ARPrintCallSheetsSelection.STARTING_NEXT_CONTACT_DATE_FIELD + "=" 
			+ sStartingNextContactDate;
		}
		if (sEndingNextContactDate.compareToIgnoreCase("") != 0){
			sParamString += "*" + ARPrintCallSheetsSelection.ENDING_NEXT_CONTACT_DATE_FIELD + "=" 
			+ sEndingNextContactDate;
		}
		if (sStartingLastContactDate.compareToIgnoreCase("") != 0){
			sParamString += "*" + ARPrintCallSheetsSelection.STARTING_LAST_CONTACT_DATE_FIELD + "=" 
			+ sStartingLastContactDate;
		}
		if (sEndingLastContactDate.compareToIgnoreCase("") != 0){
			sParamString += "*" + ARPrintCallSheetsSelection.ENDING_LAST_CONTACT_DATE_FIELD + "=" 
			+ sEndingLastContactDate;
		}
		if (sOrderNumber.compareToIgnoreCase("") != 0){
			sParamString += "*" + ARPrintCallSheetsSelection.ORDERNUMBER_FIELD + "=" 
			+ sOrderNumber;
		}
		//for (int i = 0; i < arrOrderTypes.size(); i++){
		//	sParamString += "*" + ARPrintCallSheetsSelection.PARAM_SERVICETYPE + arrOrderTypes.get(i) + "=Y";
		//}
		//System.out.println("In " + this.toString() + " sParamString = " + sParamString);
		//Special cases - if this class was called by a finder for the 'starting customer' field:
		if (request.getParameter(ARPrintCallSheetsSelection.FIND_STARTING_CUSTOMER_BUTTON_NAME) != null){
			//Then call the finder to search for customers:
			sRedirectString = 
				"" + SMUtilities.getURLLinkBase(getServletContext()) + "smar.ObjectFinder"
				+ "?" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
				+ "&ObjectName=Customer"
				+ "&ResultClass=FinderResults"
				+ "&SearchingClass=smar.ARPrintCallSheetsSelection"
				+ "&ReturnField=" + ARPrintCallSheetsSelection.STARTING_CUSTOMER_FIELD
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
				//+ "*" + "EndingCustomer=" + SMUtilities.get_Request_Parameter(ARPrintCallSheetsSelection.ENDING_CUSTOMER_FIELD, request)
				+ sParamString
				;
			response.sendRedirect(sRedirectString);
			return;
		}

		if (request.getParameter(ARPrintCallSheetsSelection.FIND_ENDING_CUSTOMER_BUTTON_NAME) != null){
			//Then call the finder to search for customers:
			sRedirectString = 
				"" + SMUtilities.getURLLinkBase(getServletContext()) + "smar.ObjectFinder"
				+ "?" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
				+ "&ObjectName=Customer"
				+ "&ResultClass=FinderResults"
				+ "&SearchingClass=smar.ARPrintCallSheetsSelection"
				+ "&ReturnField=" + ARPrintCallSheetsSelection.ENDING_CUSTOMER_FIELD
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
				//+ "*" + "EndingCustomer=" + SMUtilities.get_Request_Parameter(ARPrintCallSheetsSelection.ENDING_CUSTOMER_FIELD, request)
				+ sParamString
				;
			response.sendRedirect(sRedirectString);
			return;
		}

		if (request.getParameter(ARPrintCallSheetsSelection.FIND_ID_BUTTON_NAME) != null){
			//Then call the finder to search for customers:
			sRedirectString = 
				"" + SMUtilities.getURLLinkBase(getServletContext()) + "smar.ObjectFinder"
				+ "?" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
				+ "&ObjectName=" + ARCallSheet.ParamObjectName
				+ "&ResultClass=FinderResults"
				+ "&SearchingClass=smar.ARPrintCallSheetsSelection"
				+ "&ReturnField=" + ARPrintCallSheetsSelection.CALLSHEET_ID_FIELD
				+ "&SearchField1=" + SMTablecallsheets.sCustomerName
				+ "&SearchFieldAlias1=Customer%20Name"
				+ "&SearchField2=" + SMTablecallsheets.sCallSheetName
				+ "&SearchFieldAlias2=Call%20Sheet%20Name"
				+ "&SearchField3=" + SMTablecallsheets.sOrderNumber
				+ "&SearchFieldAlias3=Order%20Number"
				+ "&SearchField4=" + SMTablecallsheets.sJobPhone
				+ "&SearchFieldAlias4=Ship-to%20Phone"
				+ "&SearchField5=" + SMTablecallsheets.sPhone
				+ "&SearchFieldAlias5=Phone"
				+ "&ResultListField1=" + SMTablecallsheets.sID
				+ "&ResultHeading1=ID"
				+ "&ResultListField2=" + SMTablecallsheets.sCollector
				+ "&ResultHeading2=Collector"
				+ "&ResultListField3="  + SMTablecallsheets.sAcct
				+ "&ResultHeading3=Acct."
				+ "&ResultListField4=" + SMTablecallsheets.sCallSheetName
				+ "&ResultHeading4=Call%20Sheet%20Name"
				+ "&ResultListField5="  + SMTablecallsheets.sCustomerName
				+ "&ResultHeading5=Customer%20Name"
				+ "&ResultListField6="  + SMTablecallsheets.sOrderNumber
				+ "&ResultHeading6=Order%20"
				+ "&ResultListField7="  + SMTablecallsheets.sPhone
				+ "&ResultHeading7=Phone"
				+ "&ResultListField8="  + SMTablecallsheets.sJobPhone
				+ "&ResultHeading8=Ship-to%20Phone"
				+ "&ParameterString=*" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
				//+ "*" + "EndingCustomer=" + SMUtilities.get_Request_Parameter(ARPrintCallSheetsSelection.ENDING_CUSTOMER_FIELD, request)
				+ sParamString
				;
			response.sendRedirect(sRedirectString);
			return;
		}

		if (request.getParameter(ARPrintCallSheetsSelection.PRINT_BUTTON_NAME) != null){

			//Then refresh the list on the screen:
			sRedirectString = 
				"" + SMUtilities.getURLLinkBase(getServletContext()) + sCallingClass
				+ "?" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
				+ "&" + ARPrintCallSheetsSelection.STARTING_CUSTOMER_FIELD + "=" 
				+ clsServletUtilities.URLEncode(sStartingCustomer)
				+ "&" + ARPrintCallSheetsSelection.ENDING_CUSTOMER_FIELD + "=" 
				+ clsServletUtilities.URLEncode(sEndingCustomer)
				
				+ "&" + ARPrintCallSheetsSelection.COLLECTOR_FIELD + "=" 
				+ clsServletUtilities.URLEncode(sCollector)
				+ "&" + ARPrintCallSheetsSelection.RESPONSIBILITY_FIELD + "=" 
				+ clsServletUtilities.URLEncode(sResponsibility)
				
				+ "&" + ARPrintCallSheetsSelection.STARTING_LAST_CONTACT_DATE_FIELD + "=" 
				+ clsServletUtilities.URLEncode(sStartingLastContactDate)
				+ "&" + ARPrintCallSheetsSelection.ENDING_LAST_CONTACT_DATE_FIELD + "=" 
				+ clsServletUtilities.URLEncode(sEndingLastContactDate)

				+ "&" + ARPrintCallSheetsSelection.STARTING_NEXT_CONTACT_DATE_FIELD + "=" 
				+ clsServletUtilities.URLEncode(sStartingNextContactDate)
				+ "&" + ARPrintCallSheetsSelection.ENDING_NEXT_CONTACT_DATE_FIELD + "=" 
				+ clsServletUtilities.URLEncode(sEndingNextContactDate)
				
				+ "&" + ARPrintCallSheetsSelection.ORDERNUMBER_FIELD + "=" 
				+ clsServletUtilities.URLEncode(sOrderNumber)
			;
				
			if (bPrintWithNotes){
				sRedirectString += "&" + ARPrintCallSheetsSelection.PRINTWITHNOTES_FIELD + "=Y";
			}
			if (bOnlyPrintAlerts){
				sRedirectString += "&" + ARPrintCallSheetsSelection.PRINTONLYALERTS_FIELD + "=Y";
			}
			if (bPrintZeroBalanceCustomers){
				sRedirectString += "&" + ARPrintCallSheetsSelection.PRINTZEROBALANCECUSTOMERS_FIELD + "=Y";
			}
			if (bPrintWithResponsibilityOnly){
				sRedirectString += "&" + ARPrintCallSheetsSelection.PRINTWITHRESPONSIBILITYONLY_FIELD + "=Y";
			}
			
			
			try {
				validate_params(
					sCallSheetID,
					sStartingCustomer,
					sEndingCustomer,
					sCollector,
					sResponsibility,
					sStartingLastContactDate,
					sEndingLastContactDate,
					sStartingNextContactDate,
					sEndingNextContactDate
				);
			} catch (Exception e) {
				sRedirectString += "&Warning=" + clsServletUtilities.URLEncode(e.getMessage());
				response.sendRedirect(sRedirectString);
				return;
			}
		}

    	String sReportTitle = "Call Sheet Listing";
    	String sCriteria = "";
    	if (sCallSheetID.trim().compareToIgnoreCase("") == 0){
    		sCriteria += "Starting";
    	}else{
    		sCriteria += "For Call Sheet ID: <B>" + sCallSheetID + "</B>, starting";
    	}
    	sCriteria += " with customer '<B>" + sStartingCustomer + "</B>'"
    		+ ", ending with '<B>" + sEndingCustomer + "</B>'";
    	if (sCollector.compareToIgnoreCase("") == 0){
    		sCriteria += ", for <B>ALL</B> collectors";
    	}else{
    		sCriteria += ", for collector '<B>" + sCollector + "'</B>";
    	}
    	if (sResponsibility.compareToIgnoreCase("") == 0){
    		sCriteria += ", for <B>ALL</B> responsible persons";
    	}else{
    		sCriteria += ", for responsible person '<B>" + sResponsibility + "'</B>";
    	}
    	sCriteria += ", starting with collector '<B>" + sCollector + "'</B>"
    		+ ", ending with '<B>" + sResponsibility + "'</B>";
    	sCriteria += ", starting with last contact date '<B>" + sStartingLastContactDate + "</B>'"
    		+ ", ending with '<B>" + sEndingLastContactDate + "</B>'";
    	sCriteria += ", starting with next contact date '<B>" + sStartingNextContactDate + "</B>'"
    		+ ", ending with '<B>" + sEndingNextContactDate + "</B>'";
    	
    	if (sOrderNumber.compareToIgnoreCase("") == 0){
        	sCriteria += ", for <B>ALL</B>" + " order numbers";
    	}else{
        	sCriteria += ", <B>ONLY</B> for order number <B>" + sOrderNumber + "</B>";
    	}

		if(bPrintWithNotes){
			sCriteria += ", <B>INCLUDING notes</B>";
		}else{
			sCriteria += ", <B>NOT INCLUDING notes</B>";
		}

		if(bOnlyPrintAlerts){
			sCriteria += ", <B>including call sheets with alerts ONLY</B>";
		}

		if(bPrintZeroBalanceCustomers){
			sCriteria += ", <B>INCLUDING zero-balance customers</B>";
		}else{
			sCriteria += ", <B>NOT INCLUDING zero-balance customers</B>";
		}

		if(bPrintWithResponsibilityOnly){
			sCriteria += ", <B>including ONLY call sheets with a responsibility assigned</B>";
		}
		
	    boolean bOutputToCSV = (request.getParameter("OutputToCSV") != null);
	    
	  //log usage of this this report
	 	   SMClasses.SMLogEntry log = new SMClasses.SMLogEntry(sDBID, getServletContext());
	 	   log.writeEntry(sUserID, SMLogEntry.LOG_OPERATION_ARPRINTCALLSHEETS, "REPORT", "ARPrintCallSheets", "[1528205002]");
	    
    	//Retrieve information
		//Get a connection, and try to print:
		Connection conn = clsDatabaseFunctions.getConnection(
			getServletContext(), 
			sDBID, 
			"MySQL", 
			this.toString() + ".doPost - userID: " 
			+ sUserID
			+ " - "
			+ sUserFullName
				);
		
		if (conn == null){
			sRedirectString += "&Warning=" + "Could not get data connection";
			response.sendRedirect(sRedirectString);
			return;
		}
	    if (bOutputToCSV){
	    	 response.setContentType("text/csv");
             String disposition = "attachment; fileName=poreceiving.csv";
             response.setHeader("Content-Disposition", disposition);
	    }else{
	    	out.println("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.0 "
			   + "Transitional//EN\">"
		       + "<HTML>"
		       + "<HEAD>"
		       + "<TITLE>" + sReportTitle + " - " + sCompanyName + "</TITLE></HEAD>\n<BR>" 
			   + "<BODY BGCOLOR=\"#FFFFFF\""
			   + " style=\"font-family: " + SMUtilities.DEFAULT_FONT_FAMILY + "\";"
			   + ">"
			   + "<TABLE BORDER=0 WIDTH=100%>"
			   + "<TR><TD ALIGN=LEFT WIDTH=45%><FONT SIZE=2>" 
			   + clsDateAndTimeConversions.nowStdFormat() + " Printed by " + SMUtilities.getFullNamebyUserID(sUserID, conn) 
			   + "</FONT></TD><TD ALIGN=CENTER WIDTH=55%><FONT SIZE=2><B>" + sCompanyName + "</B></FONT></TD></TR>"
			   + "<TR><TD VALIGN=BOTTOM COLSPAN=2><FONT SIZE=2><B>" + sReportTitle + "</B></FONT></TD></TR>"
			   + "<TR><TD COLSPAN=2><FONT SIZE=2>" + sCriteria + "</FONT></TD></TR>");
					   
	    	out.println("<TD><A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMUserLogin?" 
				+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
				+ "\">Return to user login</A><BR>");
	    	out.println("<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smar.ARMainMenu?" 
				+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
				+ "\">Return to Accounts Receivable Main Menu</A><BR>");
		    out.println("<A HREF=\"" + WebContextParameters.getdocumentationpageURL(getServletContext()) + "#" + Long.toString(SMSystemFunctions.ARPrintCallSheets) 
		    		+ "\">Summary</A><BR>");
		    out.println("<INPUT TYPE=HIDDEN NAME='" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "' VALUE='" + sDBID + "'>");
		    
		    if (SMSystemFunctions.isFunctionPermitted(
		    		SMSystemFunctions.AREditCallSheets, 
		    		sUserID, 
		    		conn,
		    		(String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_LICENSE_MODULE_LEVEL))){
		    out.println("<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) 
		    		+ "smar.AREditCallSheetsEdit"
		    		+ "?"
		    		+ SMMasterEditSelect.SUBMIT_ADD_BUTTON_NAME + "=Y"
		    		+ "&CallingClass=" + SMUtilities.getFullClassName(this.toString())
		    		+ "&" + ARCallSheet.ParamsID + "=-1"
		    		+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
		    		+ "\">Add a new call sheet</A>"
		    		);
		    }
			out.println("</TD></TR></TABLE>");
	    }

		ARPrintCallSheetsReport rpt = new ARPrintCallSheetsReport();
    	if (!rpt.processReport(
    			conn, 
    			sCallSheetID,
    			sStartingCustomer, 
    			sEndingCustomer, 
    			sCollector, 
    			sResponsibility, 
    			sStartingLastContactDate, 
    			sEndingLastContactDate, 
    			sStartingNextContactDate, 
    			sEndingNextContactDate, 
    			sOrderNumber,
    			bPrintWithNotes, 
    			bOnlyPrintAlerts, 
    			bPrintZeroBalanceCustomers, 
    			bPrintWithResponsibilityOnly, 
    			sDBID, 
    			sUserID, 
    			out, 
    			bOutputToCSV,
    			getServletContext(),
    			(String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_LICENSE_MODULE_LEVEL))){
    		clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1547067567]");
    		sRedirectString += "&Warning=" + rpt.getErrorMessage();
			response.sendRedirect(sRedirectString);
			return;
    	}
    	
    	clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1547067566]");
	    out.println("</BODY></HTML>");
		
		return;
	}
	private void validate_params(
			String sCallSheetID,
			String sStartingCustomer,
			String sEndingCustomer,
			String sStartingCollector,
			String sResponsibility,
			String sStartingLastContactDate,
			String sEndingLastContactDate,
			String sStartingNextContactDate,
			String sEndingNextContactDate
	) throws Exception{

		sCallSheetID = sCallSheetID.trim();
		if (sCallSheetID.compareToIgnoreCase("") != 0){
			try {
				long lTest = Long.parseLong(sCallSheetID);
				if (lTest < 0){
					throw new Exception("Invalid input - Call Sheet ID cannot be less than zero.");
				}
			} catch (NumberFormatException e) {
				throw new Exception("Invalid input - Call Sheet ID '" + sCallSheetID + "' is invalid.");
			}
		}
		if (sStartingCustomer.compareToIgnoreCase(sEndingCustomer) > 0){
			throw new Exception("Invalid input - starting customer is higher than ending customer.");
		}

		Date datStartingLastContactDate= null;
		try {
			datStartingLastContactDate = clsDateAndTimeConversions.StringTojavaSQLDate("MM/dd/yyyy", sStartingLastContactDate);
		} catch (ParseException e) {
			throw new Exception("Invalid input - Date must be in format MM/dd/yyy, ex: 02/05/2015");
		}
		Date datEndingLastContactDate = null;
		try {
			datEndingLastContactDate = clsDateAndTimeConversions.StringTojavaSQLDate("MM/dd/yyyy", sEndingLastContactDate);
		} catch (ParseException e) {
			throw new Exception("Invalid input - Date must be in format MM/dd/yyy, ex: 02/05/2015");
		}
		if (datStartingLastContactDate.compareTo(datEndingLastContactDate) > 0){
			throw new Exception("Invalid input - starting last contact date cannot be later than ending last contact date");
		}

		Date datStartingNextContactDate = null;
		try {
			datStartingNextContactDate = clsDateAndTimeConversions.StringTojavaSQLDate("MM/dd/yyyy", sStartingNextContactDate);
		} catch (ParseException e) {
			throw new Exception("Invalid input - Date must be in format MM/dd/yyy, ex: 02/05/2015");
		}
		Date datEndingNextContactDate = null;
		try {
			datEndingNextContactDate = clsDateAndTimeConversions.StringTojavaSQLDate("MM/dd/yyyy", sEndingNextContactDate);
		} catch (ParseException e) {
			throw new Exception("Invalid input - Date must be in format MM/dd/yyy, ex: 02/05/2015");
		}
		if (datStartingNextContactDate.compareTo(datEndingNextContactDate) > 0){
			throw new Exception("Invalid input - starting next contact date cannot be later than ending next contact date");
		}

		return;
	}
	public void doGet(HttpServletRequest request,
			HttpServletResponse response)
	throws ServletException, IOException {

		doPost(request, response);
	}
}