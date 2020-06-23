package smap;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import SMClasses.SMLogEntry;
import SMDataDefinition.SMMasterStyleSheetDefinitions;
import SMDataDefinition.SMTableaptransactions;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsDateAndTimeConversions;
import ServletUtilities.clsManageRequestParameters;
import smcontrolpanel.SMAuthenticate;
import smcontrolpanel.SMSystemFunctions;
import smcontrolpanel.SMUtilities;

public class APVendorTransactionsGenerate extends HttpServlet {

	private static final long serialVersionUID = 1L;

	private static SimpleDateFormat USDateformatter = new SimpleDateFormat("MM-dd-yyyy hh:mm:ss a EEE");

	public void doPost(HttpServletRequest request,
			HttpServletResponse response)
	throws ServletException, IOException {

		long lStartingTime = 0;
		response.setContentType("text/html");
		PrintWriter out = response.getWriter();
	    if (!SMAuthenticate.authenticateSMCPCredentials(request, response, getServletContext(), SMSystemFunctions.APVendorTransactionsReport)){
	    	return;
	    }
		//Get the session info:
		HttpSession CurrentSession = request.getSession(true);
		String sDBID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_DATABASE_ID);
		String sUserID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERID);

		//Get parameters here:
		//sCallingClass will look like: smar.ARAgedTrialBalanceReport
		String sCallingClass = clsManageRequestParameters.get_Request_Parameter("CallingClass", request);
		String sWarning = "";
		
		String sStartingDocumentDate = request.getParameter(APVendorTransactionsSelect.PARAM_STARTING_DOCUMENT_DATE);
		String sEndingDocumentDate = request.getParameter(APVendorTransactionsSelect.PARAM_ENDING_DOCUMENT_DATE);
		String sStartingVendor = request.getParameter(APVendorTransactionsSelect.PARAM_STARTING_VENDOR);
		String sEndingVendor = request.getParameter(APVendorTransactionsSelect.PARAM_ENDING_VENDOR);
		String sStartingVendorGroup = request.getParameter(APVendorTransactionsSelect.PARAM_STARTING_GROUP);
		String sEndingVendorGroup = request.getParameter(APVendorTransactionsSelect.PARAM_ENDING_GROUP);
		String sStartingDocNumber = request.getParameter(APVendorTransactionsSelect.PARAM_STARTING_DOCUMENT_NUMBER);
		String sOriginatingFromViewVendorInfoScreen = clsManageRequestParameters.get_Request_Parameter(APDisplayVendorInformation.PARAM_ORIGINATING_FROM_VENDOR_INFO_SCREEN, request);
		boolean bPrintVendorsWithAZeroBalance = clsManageRequestParameters.get_Request_Parameter(APVendorTransactionsSelect.PARAM_PRINT_VENDORS_WITH_A_ZERO_BALANCE, request).compareToIgnoreCase("") !=0;
		boolean bIncludeAppliedDetails = clsManageRequestParameters.get_Request_Parameter(APVendorTransactionsSelect.PARAM_INCLUDE_APPLIED_DETAILS, request).compareToIgnoreCase("") !=0;
		boolean bIncludeFullyPaidTransactions = clsManageRequestParameters.get_Request_Parameter(APVendorTransactionsSelect.PARAM_INCLUDE_FULLY_PAID_TRANSACTIONS, request).compareToIgnoreCase("") !=0;
		
		boolean bTransactionTypeCreditNote = clsManageRequestParameters.get_Request_Parameter(
			APVendorTransactionsSelect.PARAM_TRANSACTION_TYPE_PREFIX + Integer.toString(SMTableaptransactions.AP_TRANSACTION_TYPE_INVOICE_CREDITNOTE), request).compareToIgnoreCase("") != 0;
		boolean bTransactionTypeDebitNote = clsManageRequestParameters.get_Request_Parameter(
			APVendorTransactionsSelect.PARAM_TRANSACTION_TYPE_PREFIX + Integer.toString(SMTableaptransactions.AP_TRANSACTION_TYPE_INVOICE_DEBITNOTE), request).compareToIgnoreCase("") != 0;
		boolean bTransactionTypeInvoice = clsManageRequestParameters.get_Request_Parameter(
			APVendorTransactionsSelect.PARAM_TRANSACTION_TYPE_PREFIX + Integer.toString(SMTableaptransactions.AP_TRANSACTION_TYPE_INVOICE_INVOICE), request).compareToIgnoreCase("") != 0;
		boolean bTransactionTypeApplyTo = clsManageRequestParameters.get_Request_Parameter(
			APVendorTransactionsSelect.PARAM_TRANSACTION_TYPE_PREFIX + Integer.toString(SMTableaptransactions.AP_TRANSACTION_TYPE_PAYMENT_APPLYTO), request).compareToIgnoreCase("") != 0;
		boolean bTransactionTypeMiscPayment = clsManageRequestParameters.get_Request_Parameter(
			APVendorTransactionsSelect.PARAM_TRANSACTION_TYPE_PREFIX + Integer.toString(SMTableaptransactions.AP_TRANSACTION_TYPE_PAYMENT_MISCPAYMENT), request).compareToIgnoreCase("") != 0;
		boolean bTransactionTypePayment = clsManageRequestParameters.get_Request_Parameter(
			APVendorTransactionsSelect.PARAM_TRANSACTION_TYPE_PREFIX + Integer.toString(SMTableaptransactions.AP_TRANSACTION_TYPE_PAYMENT_PAYMENT), request).compareToIgnoreCase("") != 0;
		boolean bTransactionTypePrePayment = clsManageRequestParameters.get_Request_Parameter(
			APVendorTransactionsSelect.PARAM_TRANSACTION_TYPE_PREFIX + Integer.toString(SMTableaptransactions.AP_TRANSACTION_TYPE_PAYMENT_PREPAYMENT), request).compareToIgnoreCase("") != 0;
		boolean bTransactionTypeCheckReversal = clsManageRequestParameters.get_Request_Parameter(
				APVendorTransactionsSelect.PARAM_TRANSACTION_TYPE_PREFIX + Integer.toString(SMTableaptransactions.AP_TRANSACTION_TYPE_REVERSAL), request).compareToIgnoreCase("") != 0;

		/*******************************************************/
		String sParamString = "";
		sParamString += "*CallingClass=" + sCallingClass;
		sParamString += "*" + APVendorTransactionsSelect.PARAM_STARTING_DOCUMENT_DATE + "=" + sStartingDocumentDate;
		sParamString += "*" + APVendorTransactionsSelect.PARAM_ENDING_DOCUMENT_DATE + "=" + sEndingDocumentDate;
		sParamString += "*" + APVendorTransactionsSelect.PARAM_STARTING_VENDOR + "=" + sStartingVendor;
		sParamString += "*" + APVendorTransactionsSelect.PARAM_ENDING_VENDOR + "=" + sEndingVendor;
		sParamString += "*" + APVendorTransactionsSelect.PARAM_STARTING_GROUP + "=" + sStartingVendorGroup;
		sParamString += "*" + APVendorTransactionsSelect.PARAM_ENDING_GROUP + "=" + sEndingVendorGroup;
		sParamString += "*" + APVendorTransactionsSelect.PARAM_STARTING_DOCUMENT_NUMBER + "=" + sStartingDocNumber;
		sParamString += "*" + APVendorTransactionsSelect.PARAM_INCLUDE_FULLY_PAID_TRANSACTIONS + "=" + clsManageRequestParameters.get_Request_Parameter(APVendorTransactionsSelect.PARAM_INCLUDE_FULLY_PAID_TRANSACTIONS, request);
		sParamString += "*" + APVendorTransactionsSelect.PARAM_PRINT_VENDORS_WITH_A_ZERO_BALANCE + "=" + clsManageRequestParameters.get_Request_Parameter(APVendorTransactionsSelect.PARAM_PRINT_VENDORS_WITH_A_ZERO_BALANCE, request);
		sParamString += "*" + APVendorTransactionsSelect.PARAM_INCLUDE_APPLIED_DETAILS + "=" + clsManageRequestParameters.get_Request_Parameter(APVendorTransactionsSelect.PARAM_INCLUDE_APPLIED_DETAILS, request);
		sParamString += "*" + APDisplayVendorInformation.PARAM_ORIGINATING_FROM_VENDOR_INFO_SCREEN + "=" + clsManageRequestParameters.get_Request_Parameter(APDisplayVendorInformation.PARAM_ORIGINATING_FROM_VENDOR_INFO_SCREEN, request);
		
		
		sParamString += "*" + APVendorTransactionsSelect.PARAM_TRANSACTION_TYPE_PREFIX + Integer.toString(SMTableaptransactions.AP_TRANSACTION_TYPE_INVOICE_CREDITNOTE) + "=" 
			+ clsManageRequestParameters.get_Request_Parameter(APVendorTransactionsSelect.PARAM_TRANSACTION_TYPE_PREFIX + Integer.toString(SMTableaptransactions.AP_TRANSACTION_TYPE_INVOICE_CREDITNOTE), request);
		sParamString += "*" + APVendorTransactionsSelect.PARAM_TRANSACTION_TYPE_PREFIX + Integer.toString(SMTableaptransactions.AP_TRANSACTION_TYPE_INVOICE_DEBITNOTE) + "=" 
			+ clsManageRequestParameters.get_Request_Parameter(APVendorTransactionsSelect.PARAM_TRANSACTION_TYPE_PREFIX + Integer.toString(SMTableaptransactions.AP_TRANSACTION_TYPE_INVOICE_DEBITNOTE), request);
		sParamString += "*" + APVendorTransactionsSelect.PARAM_TRANSACTION_TYPE_PREFIX + Integer.toString(SMTableaptransactions.AP_TRANSACTION_TYPE_INVOICE_INVOICE) + "=" 
			+ clsManageRequestParameters.get_Request_Parameter(APVendorTransactionsSelect.PARAM_TRANSACTION_TYPE_PREFIX + Integer.toString(SMTableaptransactions.AP_TRANSACTION_TYPE_INVOICE_INVOICE), request);
		sParamString += "*" + APVendorTransactionsSelect.PARAM_TRANSACTION_TYPE_PREFIX + Integer.toString(SMTableaptransactions.AP_TRANSACTION_TYPE_PAYMENT_APPLYTO) + "=" 
			+ clsManageRequestParameters.get_Request_Parameter(APVendorTransactionsSelect.PARAM_TRANSACTION_TYPE_PREFIX + Integer.toString(SMTableaptransactions.AP_TRANSACTION_TYPE_PAYMENT_APPLYTO), request);
		sParamString += "*" + APVendorTransactionsSelect.PARAM_TRANSACTION_TYPE_PREFIX + Integer.toString(SMTableaptransactions.AP_TRANSACTION_TYPE_PAYMENT_MISCPAYMENT) + "=" 
			+ clsManageRequestParameters.get_Request_Parameter(APVendorTransactionsSelect.PARAM_TRANSACTION_TYPE_PREFIX + Integer.toString(SMTableaptransactions.AP_TRANSACTION_TYPE_PAYMENT_MISCPAYMENT), request);
		sParamString += "*" + APVendorTransactionsSelect.PARAM_TRANSACTION_TYPE_PREFIX + Integer.toString(SMTableaptransactions.AP_TRANSACTION_TYPE_PAYMENT_PAYMENT) + "=" 
			+ clsManageRequestParameters.get_Request_Parameter(APVendorTransactionsSelect.PARAM_TRANSACTION_TYPE_PREFIX + Integer.toString(SMTableaptransactions.AP_TRANSACTION_TYPE_PAYMENT_PAYMENT), request);
		sParamString += "*" + APVendorTransactionsSelect.PARAM_TRANSACTION_TYPE_PREFIX + Integer.toString(SMTableaptransactions.AP_TRANSACTION_TYPE_PAYMENT_PREPAYMENT) + "=" 
			+ clsManageRequestParameters.get_Request_Parameter(APVendorTransactionsSelect.PARAM_TRANSACTION_TYPE_PREFIX + Integer.toString(SMTableaptransactions.AP_TRANSACTION_TYPE_PAYMENT_PREPAYMENT), request);
		sParamString += "*" + APVendorTransactionsSelect.PARAM_TRANSACTION_TYPE_PREFIX + Integer.toString(SMTableaptransactions.AP_TRANSACTION_TYPE_REVERSAL) + "=" 
			+ clsManageRequestParameters.get_Request_Parameter(APVendorTransactionsSelect.PARAM_TRANSACTION_TYPE_PREFIX + Integer.toString(SMTableaptransactions.AP_TRANSACTION_TYPE_REVERSAL), request);
		
		
		//sParamString += "*" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID;
		
		//Special cases - if this class was called by a finder for the 'starting vendor' field:
		if (request.getParameter(APVendorTransactionsSelect.FIND_STARTING_VENDOR_BUTTON_NAME) != null){
			//Then call the finder to search for vendors:
			String sRedirectString = 
				APVendor.getFindVendorLink(
					"smap.APVendorTransactionsSelect", 
					APVendorTransactionsSelect.PARAM_STARTING_VENDOR, 
					sParamString, 
					getServletContext(),
					sDBID
				)	
			;
			response.sendRedirect(sRedirectString);
			return;
		}

		//Special cases - if this class was called by a finder for the 'ending vendor' field:
		if (request.getParameter(APVendorTransactionsSelect.FIND_ENDING_VENDOR_BUTTON_NAME) != null){
			//Then call the finder to search for vendors:
			String sRedirectString = 
				APVendor.getFindVendorLink(
					"smap.APVendorTransactionsSelect", 
					APVendorTransactionsSelect.PARAM_ENDING_VENDOR, 
					sParamString, 
					getServletContext(),
					sDBID
				)	
			;
			response.sendRedirect(sRedirectString);
			return;
		}
		//End special cases:
		/*******************************************************/
		String sStartingDate = "";
		@SuppressWarnings("unused")
		java.sql.Date datTestDate = null;
		try {
			datTestDate = clsDateAndTimeConversions.StringTojavaSQLDate(SMUtilities.DATE_FORMAT_FOR_DISPLAY, sStartingDocumentDate);
			sStartingDate = clsDateAndTimeConversions.convertDateFormat(sStartingDocumentDate, SMUtilities.DATE_FORMAT_FOR_DISPLAY, SMUtilities.DATE_FORMAT_FOR_SQL, SMUtilities.EMPTY_SQL_DATE_VALUE);
		} catch (Exception e) {
			sWarning = "Invalid starting date.";
			response.sendRedirect(
					"" + SMUtilities.getURLLinkBase(getServletContext()) + "" + sCallingClass + "?"
					+ "Warning=" + sWarning
					+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
					+ sParamString.replace("*", "&")
			);			
			return;
		}
		String sEndingDate = "";
		try {
			datTestDate = clsDateAndTimeConversions.StringTojavaSQLDate(SMUtilities.DATE_FORMAT_FOR_DISPLAY, sEndingDocumentDate);
			sEndingDate = clsDateAndTimeConversions.convertDateFormat(sEndingDocumentDate, SMUtilities.DATE_FORMAT_FOR_DISPLAY, SMUtilities.DATE_FORMAT_FOR_SQL, SMUtilities.EMPTY_SQL_DATE_VALUE);
		} catch (Exception e) {
			sWarning = "Invalid ending date.";
			response.sendRedirect(
					"" + SMUtilities.getURLLinkBase(getServletContext()) + "" + sCallingClass + "?"
					+ "Warning=" + sWarning
					+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
					+ sParamString.replace("*", "&")
			);			
			return;
		}
		
		//Customized title
		String sReportTitle = "A/P Vendor Transactions";
		 String sColor = SMUtilities.getInitBackGroundColor(getServletContext(), sDBID);

		
		out.println("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.0 " 
			+ "Transitional//EN\">\n" 
			+ "<HTML>\n" 
			+ "  <HEAD>\n"
			+ "    <TITLE>" + sReportTitle + " - " + (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_COMPANYNAME) + "</TITLE>\n"
			+ "  </HEAD>\n"
			+ "<BR>" 
			+ "  <BODY BGCOLOR=\"" + SMMasterStyleSheetDefinitions.BACKGROUND_WHITE + "\">\n" 
			+ "    <TABLE BORDER=0 WIDTH=100% BGCOLOR = \"" + sColor + "\">\n" 
			+ "      <TR>\n"
			+ "        <TD ALIGN=LEFT WIDTH=45%><FONT SIZE=2>" + USDateformatter.format((new Timestamp(System.currentTimeMillis()))) + "</FONT></TD>\n"
			+ "        <TD ALIGN=CENTER WIDTH=55%><FONT SIZE=2><B>" + CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_COMPANYNAME).toString() + "</B></FONT></TD>\n"
			+ "      </TR>\n" 
			+ "      <TR>\n"
			+ "        <TD VALIGN=BOTTOM COLSPAN=2><FONT SIZE=2><B>" + sReportTitle + "</B></FONT></TD>\n"
			+ "      </TR>\n" 
		);
		
		String sLinks = "  <TR>\n"
			+ "    <TD><A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMUserLogin?" 
			+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
			+ "\">Return to user login</A><BR>" 
			+ "<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smap.APMainMenu?" 
			+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
			+ "\">Return to Accounts Payable Main Menu</A></TD>\n"
			+ "  </TR>\n"
		;
		if (sOriginatingFromViewVendorInfoScreen.compareToIgnoreCase("") != 0){
			sLinks += "  <TR>\n" 
				+ "    <TD><A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smap.APDisplayVendorInformation?" 
				+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
				+ "&" + APDisplayVendorInformation.PARAM_VENDOR_NUMBER + "=" + sStartingVendor
				+ "&" + "CallingClass=" + "APDisplayVendorSelection"  //Tell the Vendor Info screen we came from the Vendor info 'select' screen
				+ "\">Return to view vendor information for vendor '" + sStartingVendor + "'</A><BR>"
				+ "</TD>\n"
				+ "  </TR>\n"
			;
		}
		sLinks += "</TABLE>";
		
		out.println(sLinks);
			
		out.println(SMUtilities.getMasterStyleSheetLink());
		out.println("<TABLE WIDTH = 100% BORDER=0 BGCOLOR = \"" + sColor +  "\">\n");
		
		String s = "";
		
		s += "  <TR>\n"
			+ "    <TD WIDTH = 15% class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\" >"
			+ "Starting document date:&nbsp;"
			+ "    </TD>\n"
			+ "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\" >"
			+ "<B>" + sStartingDocumentDate + "</B>"
			+ "    </TD>\n"
			+ "  </TR>\n"
		;
			
		s += "  <TR>\n"
			+ "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\" >"
			+ "Ending document date:&nbsp;"
			+ "    </TD>\n"
			+ "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\" >"
			+ "<B>" + sEndingDocumentDate + "</B>"
			+ "    </TD>\n"
			+ "  </TR>\n"
		;
		
		s += "  <TR>\n"
			+ "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\" >"
			+ "Starting with vendor:&nbsp;"
			+ "    </TD>\n"
			+ "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\" >"
			+ "<B>" + sStartingVendor + "</B>"
			+ "    </TD>\n"
			+ "  </TR>\n"
		;
			
		s += "  <TR>\n"
				+ "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\" >"
				+ "Ending with vendor:&nbsp;"
				+ "    </TD>\n"
				+ "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\" >"
				+ "<B>" + sEndingVendor + "</B>"
				+ "    </TD>\n"
				+ "  </TR>\n"
				;

		s += "  <TR>\n"
				+ "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\" >"
				+ "Starting with vendor group:&nbsp;"
				+ "    </TD>\n"
				+ "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\" >"
				+ "<B>" + sStartingVendorGroup + "</B>"
				+ "    </TD>\n"
				+ "  </TR>\n"
				;

		s += "  <TR>\n"
				+ "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\" >"
				+ "Ending with vendor group:&nbsp;"
				+ "    </TD>\n"
				+ "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\" >"
				+ "<B>" + sEndingVendorGroup + "</B>"
				+ "    </TD>\n"
				+ "  </TR>\n"
				;

		s += "  <TR>\n"
			+ "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\" >"
			+ "Starting with document number:&nbsp;"
			+ "    </TD>\n"
			+ "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\" >"
			+ "<B>" + sStartingDocNumber + "</B>"
			+ "    </TD>\n"
			+ "  </TR>\n"
		;
		
		String sYesOrNo = "";
		
		if(bTransactionTypeCreditNote){
			sYesOrNo = "Y";
		}else{
			sYesOrNo = "N";
		}
		s += "  <TR>\n"
				+ "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\" >"
				+ "Include credit notes?&nbsp;"
				+ "    </TD>\n"
				+ "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\" >"
				+ "<B>" + sYesOrNo + "</B>"
				+ "    </TD>\n"
				+ "  </TR>\n"
			;		
		
		if(bTransactionTypeDebitNote){
			sYesOrNo = "Y";
		}else{
			sYesOrNo = "N";
		}
		s += "  <TR>\n"
				+ "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\" >"
				+ "Include debit notes?&nbsp;"
				+ "    </TD>\n"
				+ "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\" >"
				+ "<B>" + sYesOrNo + "</B>"
				+ "    </TD>\n"
				+ "  </TR>\n"
			;
		
		if(bTransactionTypeInvoice){
			sYesOrNo = "Y";
		}else{
			sYesOrNo = "N";
		}
		s += "  <TR>\n"
				+ "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\" >"
				+ "Include invoices?&nbsp;"
				+ "    </TD>\n"
				+ "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\" >"
				+ "<B>" + sYesOrNo + "</B>"
				+ "    </TD>\n"
				+ "  </TR>\n"
			;
		
		if(bTransactionTypeApplyTo){
			sYesOrNo = "Y";
		}else{
			sYesOrNo = "N";
		}
		s += "  <TR>\n"
				+ "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\" >"
				+ "Include apply-to transactions?&nbsp;"
				+ "    </TD>\n"
				+ "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\" >"
				+ "<B>" + sYesOrNo + "</B>"
				+ "    </TD>\n"
				+ "  </TR>\n"
			;
		
		if(bTransactionTypeMiscPayment){
			sYesOrNo = "Y";
		}else{
			sYesOrNo = "N";
		}
		s += "  <TR>\n"
				+ "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\" >"
				+ "Include miscellaneous payments?&nbsp;"
				+ "    </TD>\n"
				+ "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\" >"
				+ "<B>" + sYesOrNo + "</B>"
				+ "    </TD>\n"
				+ "  </TR>\n"
			;
		
		if(bTransactionTypePayment){
			sYesOrNo = "Y";
		}else{
			sYesOrNo = "N";
		}
		s += "  <TR>\n"
				+ "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\" >"
				+ "Include payments?&nbsp;"
				+ "    </TD>\n"
				+ "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\" >"
				+ "<B>" + sYesOrNo + "</B>"
				+ "    </TD>\n"
				+ "  </TR>\n"
			;
		
		if(bTransactionTypePrePayment){
			sYesOrNo = "Y";
		}else{
			sYesOrNo = "N";
		}
		s += "  <TR>\n"
				+ "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\" >"
				+ "Include pre-payments?&nbsp;"
				+ "    </TD>\n"
				+ "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\" >"
				+ "<B>" + sYesOrNo + "</B>"
				+ "    </TD>\n"
				+ "  </TR>\n"
			;
		
		if(bTransactionTypeCheckReversal){
			sYesOrNo = "Y";
		}else{
			sYesOrNo = "N";
		}
		s += "  <TR>\n"
				+ "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\" >"
				+ "Include check reversals?&nbsp;"
				+ "    </TD>\n"
				+ "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\" >"
				+ "<B>" + sYesOrNo + "</B>"
				+ "    </TD>\n"
				+ "  </TR>\n"
			;
		
		if(bPrintVendorsWithAZeroBalance){
			sYesOrNo = "Y";
		}else{
			sYesOrNo = "N";
		}
		s += "  <TR>\n"
				+ "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\" >"
				+ "Print vendors with a zero balance?&nbsp;"
				+ "    </TD>\n"
				+ "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\" >"
				+ "<B>" + sYesOrNo + "</B>"
				+ "    </TD>\n"
				+ "  </TR>\n"
			;
		
		if(bIncludeAppliedDetails){
			sYesOrNo = "Y";
		}else{
			sYesOrNo = "N";
		}
		s += "  <TR>\n"
				+ "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\" >"
				+ "Include applied details?&nbsp;"
				+ "    </TD>\n"
				+ "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\" >"
				+ "<B>" + sYesOrNo + "</B>"
				+ "    </TD>\n"
				+ "  </TR>\n"
			;
		
		if(bIncludeFullyPaidTransactions){
			sYesOrNo = "Y";
		}else{
			sYesOrNo = "N";
		}
		s += "  <TR>\n"
				+ "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\" >"
				+ "Include fully paid transactions?&nbsp;"
				+ "    </TD>\n"
				+ "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\" >"
				+ "<B>" + sYesOrNo + "</B>"
				+ "    </TD>\n"
				+ "  </TR>\n"
			;
		
		out.println(s);
		out.println("</TABLE>\n");
		out.println("<BR>\n");
		
		//Retrieve information
		Connection conn = null;
		try {
			conn = clsDatabaseFunctions.getConnectionWithException(
					getServletContext(), 
					sDBID, 
					"MySQL", 
					this.toString() + ".doGet - UserID: " + sUserID
			);
		} catch (Exception e1) {
			response.sendRedirect(
				"" + SMUtilities.getURLLinkBase(getServletContext()) + "" + sCallingClass + "?"
				+ "Warning=" + e1.getMessage()
				+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
				+ sParamString.replace("*", "&")
			);			
			return;
		}
		
		lStartingTime = System.currentTimeMillis();
		APVendorTransactionsReport rpt = new APVendorTransactionsReport();
		try {
			out.println(
				rpt.processReport(
					conn,
					sStartingDate,
					sEndingDate,
					sStartingVendor,
					sEndingVendor,
					sStartingVendorGroup,
					sEndingVendorGroup,
					sStartingDocNumber,
					bTransactionTypeCreditNote,
					bTransactionTypeDebitNote,
					bTransactionTypeInvoice,
					bTransactionTypeApplyTo,
					bTransactionTypeMiscPayment,
					bTransactionTypePayment,
					bTransactionTypePrePayment,
					bTransactionTypeCheckReversal,
					bPrintVendorsWithAZeroBalance,
					bIncludeAppliedDetails,
					bIncludeFullyPaidTransactions,
					
					SMSystemFunctions.isFunctionPermitted(
						SMSystemFunctions.APViewTransactionInformation, 
						sUserID, 
						conn, 
						(String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_LICENSE_MODULE_LEVEL)
					),
					SMSystemFunctions.isFunctionPermitted(
						SMSystemFunctions.APEditBatches, 
						sUserID, 
						conn, 
						(String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_LICENSE_MODULE_LEVEL)
					),
					SMSystemFunctions.isFunctionPermitted(
						SMSystemFunctions.APDisplayVendorInformation, 
						sUserID, 
						conn, 
						(String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_LICENSE_MODULE_LEVEL)
					),
					
					getServletContext(),
					sDBID
				)
			);
		} catch (Exception e) {
			clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1547059511]");
			response.sendRedirect(
				"" + SMUtilities.getURLLinkBase(getServletContext()) + "" + sCallingClass + "?"
				+ "Warning=" + e.getMessage()
				+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
				+ sParamString.replace("*", "&")
			);			
			return;
		}
		
		clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1547059512]");
		
		SMLogEntry log = new SMLogEntry(conn);
		log.writeEntry(sUserID,SMLogEntry.LOG_OPERATION_APVENDORTRANSACTIONS, "REPORT", "AP Vendor Transactions", "[1502477919]");
		
		long lEndingTime = System.currentTimeMillis();
		out.println("<BR>Processing took " + (lEndingTime - lStartingTime)/1000L + " seconds.\n");
		out.println("  </BODY>\n"
			+ "    </HTML>\n");
		return;	
	}
	public void doGet(HttpServletRequest request,
			HttpServletResponse response)
	throws ServletException, IOException {

		doPost(request, response);
	}
}
