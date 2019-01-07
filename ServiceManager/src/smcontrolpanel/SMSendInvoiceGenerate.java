package smcontrolpanel;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URLEncoder;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import ConnectionPool.WebContextParameters;
import SMClasses.SMInvoicePrinter;
import SMClasses.SMLogEntry;
import SMClasses.SMOrderHeader;
import SMDataDefinition.SMTablecompanyprofile;
import SMDataDefinition.SMTabledoingbusinessasaddresses;
import SMDataDefinition.SMTableinvoiceheaders;
import SMDataDefinition.SMTableorderheaders;
import SMDataDefinition.SMTablesalesgroups;
import SMDataDefinition.SMTableservicetypes;
import ServletUtilities.clsEmailInlineHTML;
import ServletUtilities.clsCreateHTMLFormFields;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsDateAndTimeConversions;
import ServletUtilities.clsManageRequestParameters;
import ServletUtilities.clsServletUtilities;
import ServletUtilities.clsPDFFileCreator;
import smar.SMOption;

public class SMSendInvoiceGenerate extends HttpServlet {

	private static final long serialVersionUID = 1L;

	private boolean bDebugMode = false;

	private String INVOICE_EMAIL_SUBJECT = "Invoice";
	private String sSendingEmailResultString = "";
	private String sCallingClass = "";
	private String sDBID = "";
	private String sUserName = "";
	private String sUserID = "";
	private String sUserFullName = "";
	private String sCompanyName = "";
	//Button names
	public static final String sParamPrintButtonName = "PRINTINVOICE";
	public static final String sParamPrintButtonValue = "Print selected invoices";
	public static final String sParamEmailButtonName = "EMAILINVOICE";
	public static final String sParamEmailButtonValue = "Email selected invoices";
	public static final String sParamUpdateButtonName = "UPDATEINVOICES";
	public static final String sParamUpdateButtonValue = "Update status of selected invoices";
	//Markers
	public static final String sInvoiceCheckboxMarker = "INVOICENO";
	public static final String sEmailMarker = "EMAILTO";
	public static final String sInvoicingStateMarker = "INVOICINGSTATUS";
	
	private static final String INVOICE_PDF_FILE_PREFIX = "Invoice-";

	public void doPost(HttpServletRequest request,
			HttpServletResponse response)
					throws ServletException, IOException {

		response.setContentType("text/html");

		PrintWriter out = response.getWriter();

		if (!SMAuthenticate.authenticateSMCPCredentials(
				request, 
				response, 
				getServletContext(), 
				SMSystemFunctions.SMSendInvoices))
		{
			return;
		}
		sSendingEmailResultString = "";
		//Get the session info:
		HttpSession CurrentSession = request.getSession(true);
		sDBID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_DATABASE_ID);
		sUserName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERNAME);
		sUserID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERID);
		sCompanyName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_COMPANYNAME);

		//sCallingClass will look like: smcontrolpanel.ARAgedTrialBalanceReport
		sCallingClass = clsManageRequestParameters.get_Request_Parameter("CallingClass", request);

		//Get original query criteria from selection screen.
		String sQueryString = getQueryString(request, false);

		//Retrieve information
		Connection conn = null;
		try {
			conn = clsDatabaseFunctions.getConnectionWithException(
				getServletContext(), 
				sDBID, 
				"MySQL", 
				"smcontrolpanel.SMSendInvoiceGenerate - user: " + sUserName
			);
		} catch (Exception e){
			response.sendRedirect(
				"" + SMUtilities.getURLLinkBase(getServletContext()) + "" + sCallingClass + "?"
					+ "Warning=" + URLEncoder.encode("Error [1487711268] - " + e.getMessage(), "UTF-8")
					+ "&" + sQueryString
					+ "" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
			);			
			return;
		}

		//Get the last invoice edited, if there was one, and save an 'edited' comment, if there was one:
		String sLastInvoiceEdited = "";

		/*
	    //DIAGNOSTIC:
		Enumeration<String> paramtestNames = request.getParameterNames();

		while(paramtestNames.hasMoreElements()) {
			String stestParamName = paramtestNames.nextElement();
			System.out.println("stestParamName = " + stestParamName + ", value = '" + request.getParameter(stestParamName) + "'");
		}
		 */

		//First, determine what command was issued: are we 1) printing the invoices, or 2) e-mailing
		//the invoices, or 3) updating the the sent status of the invoice.


		//Create a list of invoice numbers and email addresses that have been selected.
		ArrayList<String> arrInvoices;
		ArrayList<String> arrEmailAdresses;
		Enumeration <String> e = request.getParameterNames();
		arrInvoices = new ArrayList<String> (0);
		arrEmailAdresses = new ArrayList<String> (0);
		String sParam = "";
		arrInvoices.clear();
		arrEmailAdresses.clear();

		while (e.hasMoreElements()){
			sParam = (String) e.nextElement();
			String sInvoiceToSend = "";
			String sEmailTo = "";
			if (sParam.contains(sInvoiceCheckboxMarker)){
				sInvoiceToSend = sParam.substring(sInvoiceCheckboxMarker.length(), sParam.indexOf("?"));
				sEmailTo = sParam.substring(sParam.indexOf("?") + 1, sParam.length());
				if (sInvoiceToSend.compareToIgnoreCase("") != 0){
					arrInvoices.add(sInvoiceToSend);
					arrEmailAdresses.add(sEmailTo);
				}	
			}
		}

		//Check to see if we are printing or e-mailing the invoices
		//If we are going to print the invoices	 
		if(clsManageRequestParameters.get_Request_Parameter(sParamPrintButtonName, request)
				.compareToIgnoreCase(sParamPrintButtonValue) == 0){
			//Print the invoices to screen
			try {
				out.println(getHTMLInvoiceForm(arrInvoices, request, conn, false));
			} catch (Exception e1) {
				clsDatabaseFunctions.freeConnection(getServletContext(), conn);
				response.sendRedirect(
						"" + SMUtilities.getURLLinkBase(getServletContext()) + "" + sCallingClass + "?"
								+ "Warning=" + URLEncoder.encode(e1.getMessage(), "UTF-8")
								+ "&" + sQueryString
								+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
						);			
				return;
			}
			clsDatabaseFunctions.freeConnection(getServletContext(), conn);
			return;
		}	  
		//If we are going to email the invoices
		if (clsManageRequestParameters.get_Request_Parameter(sParamEmailButtonName, request)
				.compareToIgnoreCase(sParamEmailButtonValue) == 0){
			try {
				emailInvoices(
					arrInvoices,
					arrEmailAdresses, 
					conn, 
					request, 
					getServletContext(),
					sUserName);
			} catch (Exception e1) {
				clsDatabaseFunctions.freeConnection(getServletContext(), conn);
				response.sendRedirect(
						"" + SMUtilities.getURLLinkBase(getServletContext()) + "" + "smcontrolpanel.SMSendInvoiceGenerate" + "?"
								+ "Warning=" + URLEncoder.encode(e1.getMessage(), "UTF-8")
								+ "&" + sQueryString
								+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
						);
				return;
			}
			clsDatabaseFunctions.freeConnection(getServletContext(), conn);
			if (sSendingEmailResultString.compareToIgnoreCase("") != 0){
				response.sendRedirect(
						"" + SMUtilities.getURLLinkBase(getServletContext()) + "" + "smcontrolpanel.SMSendInvoiceGenerate" + "?"
								+ "Status=" + URLEncoder.encode(sSendingEmailResultString, "UTF-8")
								+ "&" + sQueryString
								+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
						);
				return;
			}else{
				response.sendRedirect(
					"" + SMUtilities.getURLLinkBase(getServletContext()) + "" + sCallingClass + "?"
							+ "Status=" + "Invoices successfully emailed."
							+ "&" + sQueryString
							+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
					);			
				return;
			}
		}
			
		//If we are going to save the invoicing states
		if (clsManageRequestParameters.get_Request_Parameter(sParamUpdateButtonName, request)
				.compareToIgnoreCase(sParamUpdateButtonValue) == 0){
			try {
				updateAllInvoiceStates(request,conn);
			} catch (Exception e1){
				clsDatabaseFunctions.freeConnection(getServletContext(), conn);
				response.sendRedirect(
						"" + SMUtilities.getURLLinkBase(getServletContext()) + "" + sCallingClass + "?"
								+ "Warning=" + URLEncoder.encode(e1.getMessage(), "UTF-8")
								+ "&" + sQueryString
								+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
						);			
				return;
			}
			clsDatabaseFunctions.freeConnection(getServletContext(), conn);
			response.sendRedirect(
					"" + SMUtilities.getURLLinkBase(getServletContext()) + "" + sCallingClass + "?"
							+ "Status=" + "Invoice statuses successfully updated."
							+ "&" + sQueryString
							+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
					);			
			return;
		}

		//Get the list of selected order types:
		ArrayList<String> sOrderTypes = new ArrayList<String>(0);
		Enumeration<String> sParamNames = request.getParameterNames();
		String sParamName = "";
		String sMarker = SMSendInvoiceSelection.SERVICE_TYPE_PARAM;
		while(sParamNames.hasMoreElements()) {
			sParamName = sParamNames.nextElement();
			if (sParamName.contains(sMarker)){
				sOrderTypes.add(sParamName.substring(sParamName.indexOf(sMarker) + sMarker.length()));
			}
		}
		Collections.sort(sOrderTypes);

		if (sOrderTypes.size() == 0){
			clsDatabaseFunctions.freeConnection(getServletContext(), conn);
			response.sendRedirect(
					"" + SMUtilities.getURLLinkBase(getServletContext()) + "" + sCallingClass + "?"
							+ "Warning=" + URLEncoder.encode("You must select at least one order type.", "UTF-8")
							+ "&" + sQueryString
							+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
					);	
			return;
		}

		//Get the list of selected sales groups:
		ArrayList<String> sSalesGroups = new ArrayList<String>(0);
		sParamNames = request.getParameterNames();
		sParamName = "";
		sMarker = SMSendInvoiceSelection.SALESGROUP_PARAM;
		while(sParamNames.hasMoreElements()) {
			sParamName = sParamNames.nextElement();
			if (sParamName.contains(sMarker)){
				sSalesGroups.add(sParamName.substring(sParamName.indexOf(sMarker) + sMarker.length()));
			}
		}
		Collections.sort(sSalesGroups);

		if (sSalesGroups.size() == 0){
			clsDatabaseFunctions.freeConnection(getServletContext(), conn);
			response.sendRedirect(
					"" + SMUtilities.getURLLinkBase(getServletContext()) + "" + sCallingClass + "?"
							+ "Warning=" + URLEncoder.encode("You must select at least one sales group.", "UTF-8")
							+ "&" + sQueryString
							+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
					);		
			return;
		}

		//Determine whether this is a 're-processing' of this report, or if the user is running it
		//for the first time:
		boolean bReprocessing = false;
		if (request.getParameter("Reprocess") != null){
			bReprocessing = true;
		}

		//Customized title
		String sReportTitle = "Send Invoice List";
		String sReportSubtitle = "";
		String sCriteria = "including order types: ";

		for (int i = 0; i < sOrderTypes.size(); i++){
			if (i == 0){
				sCriteria += "<B>" + SMTableservicetypes.getServiceTypeLabel(sOrderTypes.get(i)) + "</B>";
			}else{
				sCriteria += ", <B>" + SMTableservicetypes.getServiceTypeLabel(sOrderTypes.get(i)) + "</B>";
			}
		}
		sCriteria = sCriteria + ", including sales groups: ";
		for (int i = 0; i < sSalesGroups.size(); i++){
			if (i == 0){
				sCriteria += "<B>" + sSalesGroups.get(i).substring(
						0, sSalesGroups.get(i).indexOf(SMPrintInvoiceAuditSelection.SALESGROUP_PARAM_SEPARATOR)) + "</B>";
			}else{
				sCriteria += ", <B>" + sSalesGroups.get(i).substring(
						0, sSalesGroups.get(i).indexOf(SMPrintInvoiceAuditSelection.SALESGROUP_PARAM_SEPARATOR)) + "</B>";
			}
		}

		out.println(SMUtilities.SMCPTitleSubBGColor(sReportTitle, 
				sReportSubtitle, 
				SMUtilities.getInitBackGroundColor(getServletContext(), sDBID), 
				SMUtilities.DEFAULT_FONT_FAMILY, 
				sCompanyName, 
				false,
				"window.location='#LastEdit'")
				);

		//If there is a warning from trying to input previously, print it here:
		String sWarning = clsManageRequestParameters.get_Request_Parameter("Warning", request);
		if (sWarning.compareToIgnoreCase("") != 0){
			out.println("<B><FONT COLOR=\"RED\">WARNING: " + sWarning + "</FONT></B><BR>");
		}
		//If there is a status from trying to input previously, print it here:
		String sStatus = clsManageRequestParameters.get_Request_Parameter("Status", request);
		if (sStatus.compareToIgnoreCase("") != 0){
			out.println("<B>" + sStatus + "</B><BR>");
		}

		out.println("<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMUserLogin?" 
				+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
				+ "\">Return to user login</A><BR>" 
				);
		out.println("<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMSendInvoiceSelection?" 
				+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
				+ "\">Return to criteria </A><BR><BR>" 
				);

		String sURLLinkBase = SMUtilities.getURLLinkBase(getServletContext());

		out.println(getStyles());
		out.println ("<FORM NAME=MAINFORM ACTION =\"" + sURLLinkBase + "smcontrolpanel.SMSendInvoiceGenerate\">");

		//Store the 'calling class' here:
		out.println("<INPUT TYPE=HIDDEN NAME=\"CallingClass\" VALUE=\"" + this.getClass().getName() + "\">");
		out.println("<INPUT TYPE=HIDDEN NAME=\"" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "\" VALUE=\"" + sDBID + "\">");
		out.println (getQueryString(request, true));

		boolean bPreSelectInvoicesWithEmailAddresses = clsManageRequestParameters.get_Request_Parameter(
			SMSendInvoiceSelection.SELECT_INVS_WITH_EMAIL_ADDRESSES_PARAM, request).compareToIgnoreCase("") != 0;
		try{
			processReport(
				conn, 
				sOrderTypes,
				sSalesGroups,
				sUserName,
				true,
				sLastInvoiceEdited,
				bReprocessing,
				sURLLinkBase,
				out,
				getServletContext(),
				(String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_LICENSE_MODULE_LEVEL),
				bPreSelectInvoicesWithEmailAddresses
			);
		} catch (Exception e1){
			clsDatabaseFunctions.freeConnection(getServletContext(), conn);
			response.sendRedirect(
					"" + SMUtilities.getURLLinkBase(getServletContext()) + "" + sCallingClass + "?"
							+ "Warning=" + URLEncoder.encode(e1.getMessage(), "UTF-8")
							+ "&" + sQueryString
							+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
					);		
			return;
		}
		clsDatabaseFunctions.freeConnection(getServletContext(), conn);
		out.println("</FORM>");
		out.println("</BODY></HTML>");
	}

	private String getQueryString(HttpServletRequest req, boolean bAddHiddenParams) {
		Enumeration<String> sQueryParamNames = req.getParameterNames();
		String sParamName = "";
		String sServiceTypeMarker = SMSendInvoiceSelection.SERVICE_TYPE_PARAM;
		String sSalesGroupMarker = SMSendInvoiceSelection.SALESGROUP_PARAM;
		String sExtendedPriceMarker = SMSendInvoiceSelection.EXTENDED_PRICE_PARAM;
		String sLaborMaterialsMarker = SMSendInvoiceSelection.LABOR_MATERIALS_PARAM;
		String sTaxBreakdownMarker = SMSendInvoiceSelection.TAX_BREAKDOWN_PARAM;
		String sAllItemsMarker = SMSendInvoiceSelection.ALL_ITEMS_PARAM;
		String sSupressPagebreakMarker =  SMSendInvoiceSelection.SUPRESS_PAGEBREAK_PARAM;    
		String sQueryString = "";
		while(sQueryParamNames.hasMoreElements()) {
			sParamName = sQueryParamNames.nextElement();

			if (sParamName.contains(sServiceTypeMarker) || sParamName.contains(sSalesGroupMarker) 
					|| sParamName.contains(sExtendedPriceMarker) || sParamName.contains(sLaborMaterialsMarker)
					|| sParamName.contains(sTaxBreakdownMarker) || sParamName.contains(sAllItemsMarker)
					|| sParamName.contains(sSupressPagebreakMarker)){
				if(bAddHiddenParams){
					sQueryString += "<INPUT TYPE=HIDDEN NAME=\"" + sParamName 
							+ "\" VALUE=\"" + clsManageRequestParameters.get_Request_Parameter(sParamName, req) + "\" >";
				}else{
					sQueryString += sParamName + "=" + clsManageRequestParameters.get_Request_Parameter(sParamName, req) + "&";
				}
			}
		}	
		return sQueryString;
	}

	private void processReport(
			Connection conn,
			ArrayList <String> sOrderTypes,
			ArrayList <String> sSalesGroups,
			String sUserName,
			boolean bSuppressDetail,
			String sLastInvoiceEdited,
			boolean bReprocessing,
			String sURLLinkBase,
			PrintWriter out,
			ServletContext context,
			String sLicenseModuleLevel,
			boolean bPreSelectInvoicesWithEmailAddresses)  throws Exception{

		//String sCurrentServiceType = "";
		String sCurrentInvoiceNumber = "";
		String sInvoiceDate = "";
		String sInvoiceState = "";
		String sCustomerNumber = "";
		String sOrderNumber = "";
		String sShipToName = "";
		String sBillToName = "";
		String sInvoicingEmail = "";
		String sInvoicingContact = "";
		String sInvoicingNotes = "";
		//Create string of order types:
		String sOrderTypesString = "";
		for (int i = 0; i < sOrderTypes.size(); i++){
			sOrderTypesString += "," + sOrderTypes.get(i);
		}

		//SQL Statement:
		String SQL = "SELECT "
				+ SMTableinvoiceheaders.TableName + "." + SMTableinvoiceheaders.iTransactionType
				+ ", " + SMTableinvoiceheaders.TableName + "." + SMTableinvoiceheaders.sServiceTypeCode
				+ ", " + SMTableinvoiceheaders.TableName + "." + SMTableinvoiceheaders.sInvoiceNumber 
				+ ", " + SMTableinvoiceheaders.TableName + "." + SMTableinvoiceheaders.datInvoiceCreationDate
				+ ", " + SMTableinvoiceheaders.TableName + "." + SMTableinvoiceheaders.datInvoiceDate 
				+ ", " + SMTableinvoiceheaders.TableName + "." + SMTableinvoiceheaders.sCreatedByFullName
				+ ", " + SMTableinvoiceheaders.TableName + "." + SMTableinvoiceheaders.lCreatedByID
				+ ", " + SMTableinvoiceheaders.TableName + "." + SMTableinvoiceheaders.sOrderNumber 
				+ ", " + SMTableinvoiceheaders.TableName + "." + SMTableinvoiceheaders.sCustomerCode 
				+ ", " + SMTableinvoiceheaders.TableName + "." + SMTableinvoiceheaders.sBillToName
				+ ", " + SMTableinvoiceheaders.TableName + "." + SMTableinvoiceheaders.iSalesGroup
				+ ", " + SMTableinvoiceheaders.TableName + "." + SMTableinvoiceheaders.iinvoicingstate 
				+ ", " + SMTableinvoiceheaders.TableName + "." + SMTableinvoiceheaders.iTransactionType
				+ ", IF(" + SMTableinvoiceheaders.TableName + "." + SMTableinvoiceheaders.sSalesperson + " = '', "
				+ "'" + SMOrderHeader.UNLISTEDSALESPERSON_MARKER + "', " 
				+ SMTableinvoiceheaders.TableName + "." + SMTableinvoiceheaders.sSalesperson
				+ ") AS " + SMTableinvoiceheaders.sSalesperson

				+ ", " + SMTablesalesgroups.TableName + "." + SMTablesalesgroups.sSalesGroupDesc

				//+ ", " + SMTableinvoiceheaders.TableName + "." + SMTableinvoiceheaders.sSalesperson
				+ ", " + SMTableinvoiceheaders.TableName + "." + SMTableinvoiceheaders.mInvoiceComments
				+ ", " + SMTableinvoiceheaders.TableName + "." + SMTableinvoiceheaders.sShipToName
				+ ", " + SMTableinvoiceheaders.TableName + "." + SMTableinvoiceheaders.sOrderSourceDesc
				+ ", " + SMTableinvoiceheaders.TableName + "." + SMTableinvoiceheaders.iCustomerDiscountLevel
				+ ", " + SMTableinvoiceheaders.TableName + "." + SMTableinvoiceheaders.sDefaultPriceListCode
				+ ", " + SMTableinvoiceheaders.TableName + "." + SMTableinvoiceheaders.sPONumber
				+ ", " + SMTableinvoiceheaders.TableName + "." + SMTableinvoiceheaders.sTerms
				+ ", " + SMTableinvoiceheaders.TableName + "." + SMTableinvoiceheaders.datDueDate      	

				+ ", " + SMTableorderheaders.TableName + "." + SMTableorderheaders.datOrderCreationDate
				+ ", " + SMTableorderheaders.TableName + "." + SMTableorderheaders.sOrderCreatedByFullName
				+ ", " + SMTableorderheaders.TableName + "." + SMTableorderheaders.lOrderCreatedByID
				+ ", " + SMTableorderheaders.TableName + "." + SMTableorderheaders.datOrderDate
				+ ", " + SMTableorderheaders.TableName + "." + SMTableorderheaders.sShipToName
				+ ", " + SMTableorderheaders.TableName + "." + SMTableorderheaders.sBillToName
				+ ", " + SMTableorderheaders.TableName + "." + SMTableorderheaders.sinvoicingemail
				+ ", " + SMTableorderheaders.TableName + "." + SMTableorderheaders.sinvoicingcontact
				+ ", " + SMTableorderheaders.TableName + "." + SMTableorderheaders.sinvoicingnotes
				+ ", " + SMTableorderheaders.TableName + "." + SMTableorderheaders.strimmedordernumber

				+ " FROM "
				+ SMTableinvoiceheaders.TableName 
				+ " LEFT JOIN " + SMTableorderheaders.TableName 
				+ " ON " + SMTableinvoiceheaders.TableName + "." + SMTableinvoiceheaders.sOrderNumber
				+ " = " + SMTableorderheaders.TableName + "." + SMTableorderheaders.sOrderNumber
				+ " LEFT JOIN " + SMTablesalesgroups.TableName + " ON " + SMTableinvoiceheaders.TableName + "." + SMTableinvoiceheaders.iSalesGroup
				+ " = " + SMTablesalesgroups.TableName + "." + SMTablesalesgroups.iSalesGroupId

				+ " WHERE ("
				//Select by invoice dates
				/*        		+ "(" + SMTableinvoiceheaders.TableName + "." + SMTableinvoiceheaders.datInvoiceDate + " >= '" 
        			+ sStartingDate + "')"
        		+ " AND (" + SMTableinvoiceheaders.TableName + "." + SMTableinvoiceheaders.datInvoiceDate + " <= '" 
        			+ sEndingDate + "')"

        		//Select by invoice creation dates:
           		+ " AND (" + SMTableinvoiceheaders.TableName + "." + SMTableinvoiceheaders.datInvoiceCreationDate + " >= '" 
        			+ sStartingCreationDate + "')"
        		+ " AND (" + SMTableinvoiceheaders.TableName + "." + SMTableinvoiceheaders.datInvoiceCreationDate + " <= '" 
        			+ sEndingCreationDate + " 23:59:59')"
				 */
				//Get the order types:
				+ " (INSTR('" + sOrderTypesString + "', " + SMTableinvoiceheaders.TableName + "." 
				+ SMTableinvoiceheaders.sServiceTypeCode + ") > 0)"

				//Only display invoices that are ready to be sent
				+ " AND (" + SMTableinvoiceheaders.TableName + "." + SMTableinvoiceheaders.iinvoicingstate 
				+ "=" + SMTableinvoiceheaders.INVOICING_STATE_ELIGIBLE + ")"

				//Get the sales groups:
				+ " AND (";
		for (int i = 0; i < sSalesGroups.size(); i++){
			if (i == 0){
				SQL += "(" + SMTableorderheaders.TableName + "." + SMTableorderheaders.iSalesGroup + " = " 
						+ sSalesGroups.get(i).substring(sSalesGroups.get(i).indexOf(SMPrintInvoiceAuditSelection.SALESGROUP_PARAM_SEPARATOR) + 1) + ")";
			}else{
				SQL += " OR (" + SMTableorderheaders.TableName + "." + SMTableorderheaders.iSalesGroup + " = " 
						+ sSalesGroups.get(i).substring(sSalesGroups.get(i).indexOf(SMPrintInvoiceAuditSelection.SALESGROUP_PARAM_SEPARATOR) + 1) + ")";
			}
		}
		SQL += ")"
				+ ")"
				+ " ORDER BY " + SMTableinvoiceheaders.TableName + "." + SMTableinvoiceheaders.datInvoiceDate
				+ ", " + SMTableinvoiceheaders.TableName + "." + SMTableinvoiceheaders.sInvoiceNumber
				;

		if (bDebugMode){
			System.out.println("In " + this.toString() + ".processReport SQL = " + SQL);
		}

		//Display the TOP row of command buttons:
		out.println(printCommandButtons());
		
		String sInvoiceTypeFlag = "<B><FONT COLOR=GREEN>I</FONT></B>";
		String sCreditNoteTypeFlag = "<B><FONT COLOR=RED>C</FONT></B>";
		
		//Generate HTML:
		out.println("<TABLE WIDTH=\"100%\" style=\"border-collapse:collapse;\">\n");
		out.println("  <TR class=\"heading\">\n"
				+ "    <TD class=\"heading\"></TD>\n"
				+ "    <TD class=\"heading\">Invoice Date</TD>\n"
				+ "    <TD class=\"heading\">Doc. #<BR>"
					+ " Inv = " + sInvoiceTypeFlag 
					+ "<BR>Crd = " + sCreditNoteTypeFlag + "</TD>\n"
				+ "    <TD class=\"heading\">Status</TD>\n"
				+ "    <TD class=\"heading\">Customer</TD>\n"
				+ "    <TD class=\"heading\">Order#</TD>\n"
				+ "    <TD class=\"heading\">Bill To</TD>\n"
				+ "    <TD class=\"heading\">Ship To</TD>\n" 
				+ "    <TD class=\"heading\">Invoice Email</TD>\n"
				+ "    <TD class=\"heading\">Invoice Contact</TD>\n"
				+ "    <TD class=\"heading\">Invoice Instructions</TD>\n"
				+ "  </TR>\n"
				);
		try{
			ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
			boolean bIsRowOdd = false;
			String sRowColor = "#DCDCDC";
			while(rs.next()){

				//sCurrentServiceType
				//	= rs.getString(SMTableinvoiceheaders.TableName + "." 
				//	+ SMTableinvoiceheaders.sServiceTypeCode);

				sCurrentInvoiceNumber
				= rs.getString(SMTableinvoiceheaders.TableName + "." 
						+ SMTableinvoiceheaders.sInvoiceNumber).trim();

				sInvoiceDate
				= clsDateAndTimeConversions.resultsetDateStringToString(rs.getString(SMTableinvoiceheaders.TableName + "." 
						+ SMTableinvoiceheaders.datInvoiceDate));

				sInvoiceState
				= Integer.toString(rs.getInt(SMTableinvoiceheaders.TableName + "." 
						+ SMTableinvoiceheaders.iinvoicingstate));

				sCustomerNumber
				= rs.getString(SMTableinvoiceheaders.TableName + "." 
						+ SMTableinvoiceheaders.sCustomerCode);

				sOrderNumber
				= rs.getString(SMTableorderheaders.TableName + "." 
						+ SMTableorderheaders.strimmedordernumber);

				sBillToName
				= rs.getString(SMTableorderheaders.TableName + "." 
						+ SMTableorderheaders.sBillToName);
				
				sShipToName
				= rs.getString(SMTableorderheaders.TableName + "." 
						+ SMTableorderheaders.sShipToName);

				sInvoicingEmail
				= rs.getString(SMTableorderheaders.TableName + "." 
						+ SMTableorderheaders.sinvoicingemail);

				sInvoicingContact
				= rs.getString(SMTableorderheaders.TableName + "." 
						+ SMTableorderheaders.sinvoicingcontact);

				sInvoicingNotes
				= rs.getString(SMTableorderheaders.TableName + "." 
						+ SMTableorderheaders.sinvoicingnotes);

				ArrayList<String> arrStateValues = new ArrayList<String>();
				ArrayList<String> arrStateDescriptions = new ArrayList<String>();

				for(int i = 0; i < SMTableinvoiceheaders.NUMBER_OF_INVOICING_STATES; i++){
					arrStateDescriptions.add(SMTableinvoiceheaders.getInvoicingStateDescription(i));
					arrStateValues.add(Integer.toString(i));
				}

				if(bIsRowOdd){
					sRowColor = " bgcolor=\"#DCDCDC\" ";
				}else{
					sRowColor = "";
				}
				
				String sChecked = "";
				if (sInvoicingEmail.compareToIgnoreCase("") == 0){
					sInvoicingEmail = "(NONE)";
				}else{
					if(bPreSelectInvoicesWithEmailAddresses){
						sChecked = clsServletUtilities.CHECKBOX_CHECKED_STRING;
					}
				}
				String sInvoicingEmailLink = 
					"<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMEditOrderEdit"
					+ "?OrderNumber=" + sOrderNumber
					+ "&CallingClass=smcontrolpanel.SMEditOrderSelection"
					+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
					+ " \">" + sInvoicingEmail + "</A>";
				
				String sDocTypeFlag = sInvoiceTypeFlag;
				if (rs.getInt(SMTableinvoiceheaders.TableName + "." + SMTableinvoiceheaders.iTransactionType) == SMTableinvoiceheaders.TYPE_CREDIT){
					sDocTypeFlag = sCreditNoteTypeFlag;
				}
				out.println( "\n  <TR" + sRowColor + ">\n"
						+ "    <TD class=\"tablecell\">" 
							+ "<INPUT TYPE=\"CHECKBOX\""
								+ " NAME=\"" + sInvoiceCheckboxMarker + sCurrentInvoiceNumber + "?" + sInvoicingEmail + "\""
								+ sChecked
								+ " VALUE=\"\""
							+ ">"
						+ "</TD>\n"
							
						+ "    <TD class=\"tablecell\">" 
						+ sInvoiceDate + "</TD>\n"
						
						+ "    <TD class=\"tablecell\"><A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) 
							+ "smcontrolpanel.SMPrintInvoice?InvoiceNumberFrom=" 
							+ sCurrentInvoiceNumber 
							+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
							+ " \">" + sCurrentInvoiceNumber + "</A>" + "&nbsp;" + sDocTypeFlag + "</TD>\n"
						+ "    <TD class=\"tablecell\">" 
						
							+clsCreateHTMLFormFields.Create_Edit_Form_List_Field(
								sInvoicingStateMarker + sCurrentInvoiceNumber, 
								arrStateValues, 
								sInvoiceState, 
								arrStateDescriptions)  
						+ "</TD>\n"
						+ "    <TD class=\"tablecell\"><A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) 
							+ "smar.ARDisplayCustomerInformation?CustomerNumber=" 
							+ URLEncoder.encode(sCustomerNumber, "UTF-8") 
							+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
							+ " \">" + sCustomerNumber + "</A></TD>\n"
						+ "    <TD class=\"tablecell\"><A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) 
							+ "smcontrolpanel.SMDisplayOrderInformation?OrderNumber=" + sOrderNumber 
							+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
							+ " \">"  + sOrderNumber + "</A></TD>\n"
						+ "    <TD class=\"tablecell\">" + sBillToName + "</TD>\n"
						+ "    <TD class=\"tablecell\">" + sShipToName + "</TD>\n"
						+ "    <TD class=\"tablecell\">" + sInvoicingEmailLink + "</TD>\n"
						+ "    <TD class=\"tablecell\">" + sInvoicingContact + "</TD>\n"
						+ "    <TD class=\"tablecell\">" + sInvoicingNotes + "</TD>\n"
						+ "  </TR>\n");

				bIsRowOdd = !bIsRowOdd;		     			
			}

			out.println(printCommandButtons());
			out.println("</TABLE>\n\n");
			rs.close();
		}catch (Exception e){
			throw new Exception("Error [1487712311] reading resultset with SQL '" + SQL + "' - " + e.getMessage());
		}
	}
	private String printCommandButtons(){
		return "<TABLE><TR><TD>" + "<INPUT TYPE=\"SUBMIT\"" 
				+ " NAME=\"" + sParamPrintButtonName + "\""
				+ " VALUE=\"" + sParamPrintButtonValue + "\">"
				+ "</TD>"
				+ "<TD>" + "<INPUT TYPE=\"SUBMIT\"" 
				+ " NAME=\"" + sParamEmailButtonName + "\""
				+ " VALUE=\"" + sParamEmailButtonValue + "\">"
				+ "</TD>"
				+ "<TD>" + "<INPUT TYPE=\"SUBMIT\"" 
				+ " NAME=\"" + sParamUpdateButtonName + "\""
				+ " VALUE=\"" + sParamUpdateButtonValue + "\">"
				+ "</TD>"
				+ "</TR></TABLE>"
				;
	}
	private void updateAllInvoiceStates(
			HttpServletRequest request, 
			Connection conn) throws Exception {

		ArrayList<String> arrInvoicesToUpdate = new ArrayList<String>(0);
		Enumeration<String> sParamNames = request.getParameterNames();
		String sParamName = "";
		String sMarker = sInvoicingStateMarker;
		while(sParamNames.hasMoreElements()) {
			sParamName = sParamNames.nextElement();
			if (sParamName.contains(sMarker)){
				arrInvoicesToUpdate.add(sParamName.substring(sParamName.indexOf(sMarker) + sMarker.length()));
			}
		}	

		for(int i = 0; i < arrInvoicesToUpdate.size(); i++){
			try {
				updateInvoiceState(arrInvoicesToUpdate.get(i), request, conn);
			} catch (Exception e) {
				throw new Exception("Error [1487712416] updating invoice states - " + e.getMessage());
			}    	
		}
		return;
	}

	private void updateInvoiceState(
			String sInvoiceToUpdate, 
			HttpServletRequest request, 
			Connection conn) throws Exception {

		String sInvoicingState = clsManageRequestParameters.get_Request_Parameter(sInvoicingStateMarker + sInvoiceToUpdate, request);
		String SQL = "";
		SQL = "UPDATE " + SMTableinvoiceheaders.TableName 
				+ " SET " + SMTableinvoiceheaders.iinvoicingstate + "=" + sInvoicingState
				+ " WHERE("
				+ "TRIM(" + SMTableinvoiceheaders.sInvoiceNumber + ") = '" + sInvoiceToUpdate + "'"
				+ ")"
				;
		try{
			clsDatabaseFunctions.executeSQL(SQL, conn);
		}catch (SQLException e){
			throw new Exception("Error [1487712463] updating invoicing status with SQL '" + SQL + "' - " + e.getMessage());
		}
		return;
	}
	private void emailInvoices(
			ArrayList<String> arrInvoice,
			ArrayList<String> arrEmailAddress,
			Connection conn,
			HttpServletRequest request,
			ServletContext context,
			String sUser) throws Exception {

		SMOption opt = new SMOption();
		try {
			opt.load(sDBID, getServletContext(), sUserName);
		} catch (Exception e) {
			throw new Exception("Error [1487709615] loading SM Options data to email work order receipt - " + e.getMessage() + ".");
		}

		String sCompanyName = "";
		String SQL = "SELECT * FROM " + SMTablecompanyprofile.TableName;
		try {
			ResultSet rs = clsDatabaseFunctions.openResultSet(
					SQL, 
					getServletContext(), 
					sDBID, 
					"MySQL", 
					SMUtilities.getFullClassName(this.toString()) + ".sendEmail - " + sUserID
					+ " - "
					+ sUserFullName
					);
			if (rs.next()){
				sCompanyName = rs.getString(SMTablecompanyprofile.sCompanyName).trim();
			}
			rs.close();
		} catch (Exception e) {
			throw new Exception("Error [1487709654] reading company profile - " + e.getMessage() + ".");
		}
		
		for (int i = 0; i < arrInvoice.size(); i++){
			try {
				emailSingleInvoice(
					arrInvoice.get(i), 
					arrEmailAddress.get(i), 
					sCompanyName  + " - " + INVOICE_EMAIL_SUBJECT + " #" + arrInvoice.get(i).trim(), 
					opt, 
					context,
					request,
					conn,
					sUser);
			} catch (Exception e) {
				sSendingEmailResultString += "\n" + e.getMessage();
			}
		}
		return;
	}
	
	public String getFilePath(String sFileName, HttpServletRequest request, ServletContext context) {
		String sFullLogoImageFilePath = SMUtilities.getAbsoluteRootPath(request, context);
		
		sFullLogoImageFilePath = sFullLogoImageFilePath.replace(WebContextParameters.getInitWebAppName(context), "");
		while (sFullLogoImageFilePath.endsWith(System.getProperty("file.separator"))){
			sFullLogoImageFilePath = sFullLogoImageFilePath.substring(0, sFullLogoImageFilePath.length() - 1);
		}
		
		sFullLogoImageFilePath = sFullLogoImageFilePath + System.getProperty("file.separator");
		
		
		if (WebContextParameters.getLocalResourcesPath(context).startsWith(System.getProperty("file.separator"))){
			sFullLogoImageFilePath += WebContextParameters.getLocalResourcesPath(context).substring(1);
		}else{
			sFullLogoImageFilePath += WebContextParameters.getLocalResourcesPath(context);
		}

		while (sFullLogoImageFilePath.endsWith(System.getProperty("file.separator"))){
			sFullLogoImageFilePath = sFullLogoImageFilePath.substring(0, sFullLogoImageFilePath.length() - 1);
		}

		sFullLogoImageFilePath = sFullLogoImageFilePath + System.getProperty("file.separator");

		if (sFileName.startsWith(System.getProperty("file.separator"))){
			sFullLogoImageFilePath += sFileName.substring(0);
		
		}else{
			sFullLogoImageFilePath += sFileName;
			
		}
		return sFullLogoImageFilePath;
	}
	
	public String getInvoiceLogoFileFromDBA(String sInvoiceNumber,
									HttpServletRequest request,
									ServletContext context,
									Connection conn) throws Exception{
		String SQL = "";
		String sDescription = "";
		String sLogoFileName = "";
		SQL = "SELECT "+SMTableinvoiceheaders.TableName+"."+SMTableinvoiceheaders.sdbadescription+" FROM "+SMTableinvoiceheaders.TableName
				+" WHERE "+SMTableinvoiceheaders.TableName+"."+SMTableinvoiceheaders.sInvoiceNumber+" = "+sInvoiceNumber;
		try {
			ResultSet rs = clsDatabaseFunctions.openResultSet(
					SQL, 
					getServletContext(), 
					sDBID, 
					"MySQL", 
					SMUtilities.getFullClassName(this.toString()) + ".sendEmail - " + sUserID
					+ " - "
					+ sUserFullName
					);
			if (rs.next()){
				sDescription = rs.getString(SMTableinvoiceheaders.sdbadescription).trim();
			}
			rs.close();
			SQL = "SELECT "+SMTabledoingbusinessasaddresses.TableName+"."+SMTabledoingbusinessasaddresses.sInvoiceLogo+" FROM "
					+" "+SMTabledoingbusinessasaddresses.TableName+" WHERE "
					+" "+SMTabledoingbusinessasaddresses.TableName+"."+SMTabledoingbusinessasaddresses.sDescription
					+ "= '"+sDescription+"'";
			
			rs = clsDatabaseFunctions.openResultSet(
					SQL, 
					getServletContext(), 
					sDBID, 
					"MySQL", 
					SMUtilities.getFullClassName(this.toString()) + ".sendEmail - " + sUserID
					+ " - "
					+ sUserFullName
					);
			if (rs.next()){
				sLogoFileName = rs.getString(SMTabledoingbusinessasaddresses.sInvoiceLogo).trim();
			}
			rs.close();
		} catch (Exception e) {
			throw new Exception("Error [1487709654] getting File Name - " + e.getMessage() + ".");
		}
		
		sLogoFileName = getFilePath(sLogoFileName,request,context);
		
		return sLogoFileName;
		
		
		
		
	}

	private void emailSingleInvoice(
		String sInvoiceNumber,
		String sEmailAddressee,
		String sEmailSubject,
		SMOption opt,
		ServletContext context,
		HttpServletRequest req,
		Connection conn,
		String sUser
		) throws Exception{
		
		//First, create the invoice file to attach:
		
		
		SMInvoicePrinter invprinter;
		try {
			invprinter = new SMInvoicePrinter(
				conn, 
				sInvoiceNumber,
				true,
				clsManageRequestParameters.get_Request_Parameter(SMSendInvoiceSelection.TAX_BREAKDOWN_PARAM, req).compareToIgnoreCase("") != 0,
				clsManageRequestParameters.get_Request_Parameter(SMSendInvoiceSelection.EXTENDED_PRICE_PARAM, req).compareToIgnoreCase("") != 0,
				clsManageRequestParameters.get_Request_Parameter(SMSendInvoiceSelection.ALL_ITEMS_PARAM, req).compareToIgnoreCase("") != 0,
				clsManageRequestParameters.get_Request_Parameter(SMSendInvoiceSelection.LABOR_MATERIALS_PARAM, req).compareToIgnoreCase("") != 0,
				clsManageRequestParameters.get_Request_Parameter(SMSendInvoiceSelection.SUPRESS_PAGEBREAK_PARAM, req).compareToIgnoreCase("") != 0,
				true,
				1,
				req,
				context);
		} catch (Exception e1) {
			throw new Exception("Error [1487716929] initializing invoice printer for invoice number '" + sInvoiceNumber + "' - " + e1.getMessage());
		}
		String sInvoiceHTML = "<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.0 Transitional//EN\">"
			+ "<HTML>"
			+ "<HEAD>" 
			+ "<STYLE TYPE=\"text/css\" media=\"print\">\n"
			+ "</STYLE><TITLE>" + "Invoice/Credit Note" + "</TITLE>"
			+ "</HEAD>" 
			+ "<BODY BGCOLOR=\"" + "#FFFFFF" + "\">"
		; 
				
		sInvoiceHTML += invprinter.printOneCopyOfInvoice(conn);
		
		//Get the temporary file path for saving files:
		String sPDFInvoiceFileName = 
			SMUtilities.getTempFileDirectory(context, req)
			+ System.getProperty("file.separator")
			+ INVOICE_PDF_FILE_PREFIX 
			+ sInvoiceNumber.trim()
			+ ".pdf";
		ArrayList<String>arrPDFInvoiceFileNames = new ArrayList<String>(0);
		arrPDFInvoiceFileNames.add(sPDFInvoiceFileName);
		clsPDFFileCreator pdfengine = new clsPDFFileCreator(sPDFInvoiceFileName);
		try {
			pdfengine.createPDFFromString(sInvoiceHTML);
		} catch (Exception e1) {
			throw new Exception("Error [1487716930] creating PDF file for invoice number '" + sInvoiceNumber + "' - " + e1.getMessage());
		}
		
		SMLogEntry log = new SMLogEntry(conn);
		
		//Email one copy to each person in the email address list:
		try {
			clsEmailInlineHTML.sendEmailWithEmbeddedHTML(
				opt.getSMTPServer(), 
				opt.getSMTPUserName(), 
				opt.getSMTPPassword(), 
				sEmailAddressee.replace(";", ","),
				opt.getSMTPReplyToAddress(),
				sEmailSubject, 
				createEmailBody(), 
				null,
				arrPDFInvoiceFileNames);
		} catch (Exception e2) {
			log.writeEntry(
				sUserID, 
				SMLogEntry.LOG_OPERATION_INVOICENOTMAILED, 
				"Inv# " + sInvoiceNumber.trim() + " to: " + sEmailAddressee.replace(";", ","), 
				"Error: " + e2.getMessage(), 
				"[1487781876]"
			);
			throw new Exception("Error [1487716932] emailing invoice number '" + sInvoiceNumber + "' - " + e2.getMessage());
		}
		
		log.writeEntry(
				sUserID, 
				SMLogEntry.LOG_OPERATION_INVOICEEMAILED, 
				"Inv# " + sInvoiceNumber.trim() + " to: " + sEmailAddressee.replace(";", ","), 
				"", 
				"[1487781877]"
			);
		
		//Now remove the PDF file:
		try {
			pdfengine.removePDFFile();
		} catch (Exception e1) {
			//Don't want to choke on this, but we'll keep it in the log for now to see if there's a consistent problem deleting:
			System.out.println("Error [1487716931] removing PDF file '" + sPDFInvoiceFileName + "' - " + e1.getMessage());
		}
		
		//Update the status of the emailed invoice to 'sent':
		String SQL = "";
		SQL = "UPDATE " + SMTableinvoiceheaders.TableName 
			+ " SET " + SMTableinvoiceheaders.iinvoicingstate + "=" + SMTableinvoiceheaders.INVOICING_STATE_SENT
			+ " WHERE("
			+ "TRIM(" + SMTableinvoiceheaders.sInvoiceNumber + ") = '" + sInvoiceNumber.trim() + "'"
			+ ")"
		;
		try{
			clsDatabaseFunctions.executeSQL(SQL, conn);
		}catch (SQLException e){
			throw new Exception("Error [1487712463] updating invoicing status "  + " - " + e.getMessage());
		}
	}
	private String createEmailBody() throws Exception{
		String s = "";
		s += "Please process the attached invoice for payment."
			+ "  \n\nNOTE: This is an automatically generated email, please do not respond to this email address."
			+ "  Please use the contact information on the attached invoice to respond."
			+ "  \n\nThank you"
		;
		return s;
	}
	
	public HashMap<String,String> getInvoiceLogo(ArrayList<String> arrInvoices,
												 HttpServletRequest request,
												 ServletContext context,
												 Connection conn)throws Exception{
		HashMap<String, String> map = new HashMap<String,String>();
		
		for(int i = 0; i < arrInvoices.size(); i++) {
			String InvoiceNumber = arrInvoices.get(i);
			String imagePath = "";
			try {
			imagePath = getInvoiceLogoFileFromDBA(InvoiceNumber,request,context,conn);
			}catch(Exception e) {
				throw new Exception("ERROR [1544104980] "+e.getMessage());
			}
			map.put(InvoiceNumber, imagePath);
		}
		return map;
	}
	
	
	private String getHTMLInvoiceForm(
			ArrayList<String> arrInvoices, 
			HttpServletRequest request,
			Connection conn,
			boolean bEmailingInvoices) throws Exception{

		//Get the options for displaying the invoices
		boolean bShowExtendedPriceForEachItem;
		if (request.getParameter("ShowExtendedPriceForEachItem") != null){
			if (Integer.parseInt(request.getParameter("ShowExtendedPriceForEachItem")) == 1){
				bShowExtendedPriceForEachItem = true;
			}else{
				bShowExtendedPriceForEachItem = false;
			}
		}else{
			bShowExtendedPriceForEachItem = false;
		}

		boolean bShowLaborAndMaterialSubtotals;
		if (request.getParameter("ShowLaborAndMaterialSubtotals") != null){
			if (Integer.parseInt(request.getParameter("ShowLaborAndMaterialSubtotals")) == 1){
				bShowLaborAndMaterialSubtotals = true;
			}else{
				bShowLaborAndMaterialSubtotals = false;
			}
		}else{
			bShowLaborAndMaterialSubtotals = false;
		}

		boolean bSuppressDetailsPageBreak;
		if (request.getParameter("SuppressDetailsPageBreak") != null){
			if (Integer.parseInt(request.getParameter("SuppressDetailsPageBreak")) == 1){
				bSuppressDetailsPageBreak = true;
			}else{
				bSuppressDetailsPageBreak = false;
			}
		}else{
			bSuppressDetailsPageBreak = false;
		}

		boolean bShowALLItemsOnInvoiceIncludingDNP;
		if (request.getParameter("ShowALLItemsOnInvoiceIncludingDNP") != null){
			if (Integer.parseInt(request.getParameter("ShowALLItemsOnInvoiceIncludingDNP")) == 1){
				bShowALLItemsOnInvoiceIncludingDNP = true;
			}else{
				bShowALLItemsOnInvoiceIncludingDNP = false;
			}
		}else{
			bShowALLItemsOnInvoiceIncludingDNP = false;
		}

		boolean bShowTaxBreakdown;
		if (request.getParameter("ShowTaxBreakdown") != null){
			if (Integer.parseInt(request.getParameter("ShowTaxBreakdown")) == 1){
				bShowTaxBreakdown = true;
			}else{
				bShowTaxBreakdown = false;
			}
		}else{
			bShowTaxBreakdown = false;
		}


		String sOut = "";
		String sBodyFontString = " style= \"font-family: Arial, sans-serif; \" ";
		sOut +=
				SMUtilities.DOCTYPE
				+ "<HTML>"
				+ "<HEAD>" 
				+ "<STYLE TYPE=\"text/css\" media=\"print\">\n" //+ P.breakhere {page-break-before: always;}\n"
				+ "H1.western { font-family: \"Arial\", sans-serif; font-size: 16pt; }\n"
				+ "H2.western { font-family: \"Arial\", sans-serif; font-size: 14pt; }\n"
				+ "H3.western { font-family: \"Arial\", sans-serif; font-size: 12pt; }\n"
				+ "H4.western { font-family: \"Arial\", sans-serif; font-size: 10pt; }\n"
				+ "</STYLE><TITLE>" + "Report" + "</TITLE></HEAD>\n<BR>" + 
				"<BODY"
					+ " BGCOLOR=\"" + "#FFFFFF" + "\""
					+ sBodyFontString
				+ "\">";
		//HashMap<String,String> hFullLogoImageFilePath = getInvoiceLogo(arrInvoices,request,getServletContext(),conn);
		
		SMPrintInvoice prninvoice = new SMPrintInvoice();
		for (int i = 0; i < arrInvoices.size(); i++){	

			if(i != 0){
				sOut += SMInvoicePrinter.getPageBreak();
			}

			try{
				//String sFullLogoImageFilePath = hFullLogoImageFilePath.get(arrInvoices.get(i));
				sOut += prninvoice.PrintMultipleInvoices(
						arrInvoices.get(i), 
						arrInvoices.get(i), 
						bShowExtendedPriceForEachItem, 
						bShowLaborAndMaterialSubtotals,
						bSuppressDetailsPageBreak, 
						bShowALLItemsOnInvoiceIncludingDNP, 
						bShowTaxBreakdown, 
						1, 
						conn, 
						request,
						getServletContext(),
						bEmailingInvoices
						);

				sOut += "</BODY></HTML>";
			}catch (Exception ex){	
				throw new Exception("Error [1487709415] printing invoices - " + ex.getMessage());
			}
		}
		return sOut;
	}

	private String getStyles() {
		String s = "";
		s += "<style>\n"
				+ "td.heading {\n"
				+ "font-size: small;\n"
				+ "font-weight: bold;\n"
				+ "align: left;\n"
				+ "vertical-align: bottom;\n"
				+ "height:32px;\n"
				+ "}\n";

		s += "tr.heading {\n"
				+ "border-bottom:1pt solid black;\n"
				+ "}\n";

		s += "td.tablecell {\n"
				+ "font-size: small;\n"
				+ "aligh: left;\n"
				+ "height:32px;\n"
				+ "}\n";

		s += "table {\n"
				+ "border-spacing: 15px;\n"
				+ "}\n";

		s += "</style>\n";


		return s;
	}
	public void doGet(HttpServletRequest request,
			HttpServletResponse response)
					throws ServletException, IOException {

		doPost(request, response);
	}
}
