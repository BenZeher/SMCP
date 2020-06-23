package smap;

import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import SMClasses.SMLogEntry;
import SMDataDefinition.SMMasterStyleSheetDefinitions;
import SMDataDefinition.SMTableapbatches;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsDateAndTimeConversions;
import ServletUtilities.clsManageRequestParameters;
import smcontrolpanel.SMAuthenticate;
import smcontrolpanel.SMSystemFunctions;
import smcontrolpanel.SMUtilities;

public class APCreatePaymentsReportGenerate extends HttpServlet {
	private static final long serialVersionUID = 1L;

	private static int MAX_ERROR_LENGTH = 1024;
	private static String SUBMIT_BUTTON_NAME = "CREATEBATCH";
	private static String SUBMIT_BUTTON_VALUE = "Create payment batch";
	
	private static SimpleDateFormat USDateformatter = new SimpleDateFormat("MM-dd-yyyy hh:mm:ss a EEE");

	public void doPost(HttpServletRequest request,
			HttpServletResponse response)
	throws ServletException, IOException {

		response.setContentType("text/html");
		PrintWriter out = response.getWriter();
	    if (!SMAuthenticate.authenticateSMCPCredentials(request, response, getServletContext(), SMSystemFunctions.APEditBatches)){
	    	return;
	    }
		//Get the session info:
		HttpSession CurrentSession = request.getSession(true);
		String sDBID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_DATABASE_ID);
		String sUserID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERID);

		//Get parameters here:
		//sCallingClass will look like: smar.ARAgedTrialBalanceReport
		String sWarning = "";
		String sCallingClass = clsManageRequestParameters.get_Request_Parameter("CallingClass", request);
		String sPaymentDate = request.getParameter(APCreatePaymentsReportEdit.PARAM_PAYMENT_DATE);
		String sBatchDate = request.getParameter(APCreatePaymentsReportEdit.PARAM_BATCH_DATE);
		String sBankID = request.getParameter(APCreatePaymentsReportEdit.PARAM_BANK_ID);
		String sSelectDocumentsBy = request.getParameter(APCreatePaymentsReportEdit.PARAM_SELECT_DOCUMENTS_BY);
		String sDueDate = request.getParameter(APCreatePaymentsReportEdit.PARAM_DUE_DATE);
		String sStartingDiscountDate = request.getParameter(APCreatePaymentsReportEdit.PARAM_STARTING_DISCOUNT_DATE);
		String sEndingDiscountDate = request.getParameter(APCreatePaymentsReportEdit.PARAM_ENDING_DISCOUNT_DATE);
		String sStartingVendorGroupName = request.getParameter(APCreatePaymentsReportEdit.PARAM_STARTING_VENDOR_GROUP_NAME);
		String sEndingVendorGroupName = request.getParameter(APCreatePaymentsReportEdit.PARAM_ENDING_VENDOR_GROUP_NAME);
		String sStartingAcctSetName = request.getParameter(APCreatePaymentsReportEdit.PARAM_STARTING_ACCOUNT_SET_NAME);
		String sEndingAcctSetName = request.getParameter(APCreatePaymentsReportEdit.PARAM_ENDING_ACCOUNT_SET_NAME);
		String sStartingInvoiceAmt = request.getParameter(APCreatePaymentsReportEdit.PARAM_STARTING_INVOICE_AMT);
		String sEndingInvoiceAmt = request.getParameter(APCreatePaymentsReportEdit.PARAM_ENDING_INVOICE_AMT);
		String sStartingVendor = request.getParameter(APCreatePaymentsReportEdit.PARAM_STARTING_VENDOR);
		String sEndingVendor = request.getParameter(APCreatePaymentsReportEdit.PARAM_ENDING_VENDOR);

		/*******************************************************/
		String sParamString = "";
		sParamString += "&CallingClass=" + sCallingClass;
		sParamString += "&" + APCreatePaymentsReportEdit.PARAM_PAYMENT_DATE + "=" + sPaymentDate;
		sParamString += "&" + APCreatePaymentsReportEdit.PARAM_BATCH_DATE + "=" + sBatchDate;
		sParamString += "&" + APCreatePaymentsReportEdit.PARAM_BANK_ID + "=" + sBankID;
		sParamString += "&" + APCreatePaymentsReportEdit.PARAM_SELECT_DOCUMENTS_BY + "=" + sSelectDocumentsBy;
		sParamString += "&" + APCreatePaymentsReportEdit.PARAM_DUE_DATE + "=" + sDueDate;
		sParamString += "&" + APCreatePaymentsReportEdit.PARAM_STARTING_DISCOUNT_DATE + "=" + sStartingDiscountDate;
		sParamString += "&" + APCreatePaymentsReportEdit.PARAM_ENDING_DISCOUNT_DATE + "=" + sEndingDiscountDate;
		sParamString += "&" + APCreatePaymentsReportEdit.PARAM_STARTING_VENDOR_GROUP_NAME + "=" + sStartingVendorGroupName;
		sParamString += "&" + APCreatePaymentsReportEdit.PARAM_ENDING_VENDOR_GROUP_NAME + "=" + sStartingVendorGroupName;
		sParamString += "&" + APCreatePaymentsReportEdit.PARAM_STARTING_ACCOUNT_SET_NAME + "=" + sStartingAcctSetName;
		sParamString += "&" + APCreatePaymentsReportEdit.PARAM_ENDING_ACCOUNT_SET_NAME + "=" + sEndingAcctSetName;
		sParamString += "&" + APCreatePaymentsReportEdit.PARAM_STARTING_INVOICE_AMT + "=" + sStartingInvoiceAmt;
		sParamString += "&" + APCreatePaymentsReportEdit.PARAM_ENDING_INVOICE_AMT + "=" + sEndingInvoiceAmt;
		sParamString += "&" + APCreatePaymentsReportEdit.PARAM_STARTING_VENDOR + "=" + sStartingVendor;
		sParamString += "&" + APCreatePaymentsReportEdit.PARAM_ENDING_VENDOR + "=" + sEndingVendor;
		
		@SuppressWarnings("unused")
		java.sql.Date datTest;
		
		try {
			datTest = clsDateAndTimeConversions.StringTojavaSQLDate("MM/dd/yyyy", sPaymentDate);
		} catch (ParseException e) {
			sWarning = "Invalid payment date.";
			response.sendRedirect(
				"" + SMUtilities.getURLLinkBase(getServletContext()) + "" + sCallingClass + "?"
				+ "Warning=" + sWarning
				+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
				+ sParamString
			);			
			return;
		}
		
		try {
			datTest = clsDateAndTimeConversions.StringTojavaSQLDate("MM/dd/yyyy", sBatchDate);
		} catch (ParseException e) {
			sWarning = "Invalid batch date.";
			response.sendRedirect(
				"" + SMUtilities.getURLLinkBase(getServletContext()) + "" + sCallingClass + "?"
				+ "Warning=" + sWarning
				+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
				+ sParamString
			);			
			return;
		}
		
		if (sSelectDocumentsBy.compareToIgnoreCase(APCreatePaymentsReportEdit.PARAM_SELECT_DOCUMENTS_BY_DUE_DATE) != 0){
			try {
				datTest = clsDateAndTimeConversions.StringTojavaSQLDate("MM/dd/yyyy", sStartingDiscountDate);
			} catch (ParseException e) {
				sWarning = "Invalid starting discount date.";
				response.sendRedirect(
					"" + SMUtilities.getURLLinkBase(getServletContext()) + "" + sCallingClass + "?"
					+ "Warning=" + sWarning
					+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
					+ sParamString
			);			
				return;
			}
			
			try {
				datTest = clsDateAndTimeConversions.StringTojavaSQLDate("MM/dd/yyyy", sEndingDiscountDate);
			} catch (ParseException e) {
				sWarning = "Invalid ending discount date.";
				response.sendRedirect(
					"" + SMUtilities.getURLLinkBase(getServletContext()) + "" + sCallingClass + "?"
					+ "Warning=" + sWarning
					+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
					+ sParamString
			);			
				return;
			}
		}
		
		if (sSelectDocumentsBy.compareToIgnoreCase(APCreatePaymentsReportEdit.PARAM_SELECT_DOCUMENTS_BY_DISCOUNT_DATE) != 0){
			try {
				datTest = clsDateAndTimeConversions.StringTojavaSQLDate("MM/dd/yyyy", sDueDate);
			} catch (ParseException e) {
				sWarning = "Invalid due date.";
				response.sendRedirect(
					"" + SMUtilities.getURLLinkBase(getServletContext()) + "" + sCallingClass + "?"
					+ "Warning=" + sWarning
					+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
					+ sParamString
			);			
				return;
			}
		}

		if (sStartingVendorGroupName.compareToIgnoreCase(sEndingVendorGroupName) > 0){
			sWarning = "The STARTING vendor group is greater than the ENDING vendor group.";
			response.sendRedirect(
				"" + SMUtilities.getURLLinkBase(getServletContext()) + "" + sCallingClass + "?"
				+ "Warning=" + sWarning
				+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
				+ sParamString
		);			
			return;
		}
		
		if (sStartingAcctSetName.compareToIgnoreCase(sEndingAcctSetName) > 0){
			sWarning = "The STARTING account set is greater than the ENDING account set.";
			response.sendRedirect(
				"" + SMUtilities.getURLLinkBase(getServletContext()) + "" + sCallingClass + "?"
				+ "Warning=" + sWarning
				+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
				+ sParamString
		);			
			return;
		}
		
		if (sStartingVendor.compareToIgnoreCase(sEndingVendor) > 0){
			sWarning = "The STARTING vendor is greater than the ENDING vendor.";
			response.sendRedirect(
				"" + SMUtilities.getURLLinkBase(getServletContext()) + "" + sCallingClass + "?"
				+ "Warning=" + sWarning
				+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
				+ sParamString
		);			
			return;
		}
		
		BigDecimal bdStartingAmt = null;
		try {
			bdStartingAmt = new BigDecimal(sStartingInvoiceAmt.replaceAll(",", "").trim());
		} catch (Exception e2) {
			sWarning = "The STARTING amount '" + sStartingInvoiceAmt + "' is invalid.";
			response.sendRedirect(
				"" + SMUtilities.getURLLinkBase(getServletContext()) + "" + sCallingClass + "?"
				+ "Warning=" + sWarning
				+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
				+ sParamString
			);			
			return;
		}
		
		if (bdStartingAmt.compareTo(BigDecimal.ZERO) <= 0){
			sWarning = "The STARTING amount must be more than ZERO.";
			response.sendRedirect(
				"" + SMUtilities.getURLLinkBase(getServletContext()) + "" + sCallingClass + "?"
				+ "Warning=" + sWarning
				+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
				+ sParamString
			);			
			return;
		}
		
		BigDecimal bdEndingAmt = null;
		try {
			bdEndingAmt = new BigDecimal(sEndingInvoiceAmt.replaceAll(",", "").trim());
		} catch (Exception e2) {
			sWarning = "The ENDING amount '" + sEndingInvoiceAmt + "' is invalid.";
			response.sendRedirect(
				"" + SMUtilities.getURLLinkBase(getServletContext()) + "" + sCallingClass + "?"
				+ "Warning=" + sWarning
				+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
				+ sParamString
			);			
			return;
		}
		
		if (bdStartingAmt.compareTo(bdEndingAmt) > 0){
			sWarning = "The STARTING amount '" + sStartingInvoiceAmt + "' is more than the ENDING amount '" + sEndingInvoiceAmt + "'.";
			response.sendRedirect(
				"" + SMUtilities.getURLLinkBase(getServletContext()) + "" + sCallingClass + "?"
				+ "Warning=" + sWarning
				+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
				+ sParamString
			);			
			return;
		}
		
		//Customized title
		String sReportTitle = "A/P Pre-Check Register";
		
		out.println("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.0 " 
			+ "Transitional//EN\">\n" 
			+ "<HTML>\n" 
			+ "  <HEAD>\n"
			+ "    <TITLE>" + sReportTitle + " - " + (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_COMPANYNAME) + "</TITLE>\n"
			+ "  </HEAD>\n"
			+ "<BR>" 
			+ "  <BODY BGCOLOR=\"" + SMMasterStyleSheetDefinitions.BACKGROUND_WHITE + "\">\n" 
			+ "    <TABLE BORDER=0 WIDTH=100%>\n" 
			+ "      <TR>\n"
			+ "        <TD ALIGN=LEFT WIDTH=45%><FONT SIZE=2>" + USDateformatter.format((new Timestamp(System.currentTimeMillis()))) + "</FONT></TD>\n"
			+ "        <TD ALIGN=CENTER WIDTH=55%><FONT SIZE=2><B>" + CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_COMPANYNAME).toString() + "</B></FONT></TD>\n"
			+ "      </TR>\n" 
			+ "      <TR>\n"
			+ "        <TD VALIGN=BOTTOM COLSPAN=2><FONT SIZE=2><B>" + sReportTitle + "</B></FONT></TD>\n"
			+ "      </TR>\n" 
		);
		
		out.println("  <TR>\n"
			+ "    <TD><A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMUserLogin?" 
			+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
			+ "\">Return to user login</A><BR>" 
			+ "<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smap.APMainMenu?" 
			+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
			+ "\">Return to Accounts Payable Main Menu</A></TD>\n"
			+ "  </TR>\n"
			
			+ "  <TR>\n"
			+ "    <TD>"
			+ "<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smap.APEditBatchesSelect?" 
					+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
					+ "\">Return to Batch List</A>"
			+ "</TD\n"
			+ "  </TR>\n"
			
			+ "</TABLE>"
		);
		
		out.println(SMUtilities.getMasterStyleSheetLink());
		out.println("<BR>\n");
		
		//TODO - display user's choices here:
		
		
		
		//Here we need to create a form to hold our variables, and allow the user to create the batch:
		out.println("\n<FORM ACTION =\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smap.APAutoCreatePaymentBatchAction\" METHOD=\"POST\" >\n");
		out.println("<INPUT TYPE=HIDDEN NAME=\"" + "CallingClass" + "\" VALUE=\"" + SMUtilities.getFullClassName(this.toString()) + "\">");
		out.println("<INPUT TYPE=HIDDEN NAME='" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "' VALUE='" + sDBID + "'>");
		out.println("<INPUT TYPE=HIDDEN NAME=\"" + APCreatePaymentsReportEdit.PARAM_PAYMENT_DATE + "\" VALUE=\"" + sPaymentDate + "\">");
		out.println("<INPUT TYPE=HIDDEN NAME=\"" + APCreatePaymentsReportEdit.PARAM_BATCH_DATE + "\" VALUE=\"" + sBatchDate + "\">");
		out.println("<INPUT TYPE=HIDDEN NAME=\"" + APCreatePaymentsReportEdit.PARAM_BANK_ID + "\" VALUE=\"" + sBankID + "\">");
		out.println("<INPUT TYPE=HIDDEN NAME=\"" + APCreatePaymentsReportEdit.PARAM_SELECT_DOCUMENTS_BY + "\" VALUE=\"" + sSelectDocumentsBy + "\">");
		out.println("<INPUT TYPE=HIDDEN NAME=\"" + APCreatePaymentsReportEdit.PARAM_DUE_DATE + "\" VALUE=\"" + sDueDate + "\">");
		out.println("<INPUT TYPE=HIDDEN NAME=\"" + APCreatePaymentsReportEdit.PARAM_STARTING_DISCOUNT_DATE + "\" VALUE=\"" + sStartingDiscountDate + "\">");
		out.println("<INPUT TYPE=HIDDEN NAME=\"" + APCreatePaymentsReportEdit.PARAM_ENDING_DISCOUNT_DATE + "\" VALUE=\"" + sEndingDiscountDate + "\">");
		out.println("<INPUT TYPE=HIDDEN NAME=\"" + APCreatePaymentsReportEdit.PARAM_STARTING_VENDOR_GROUP_NAME + "\" VALUE=\"" + sStartingVendorGroupName + "\">");
		out.println("<INPUT TYPE=HIDDEN NAME=\"" + APCreatePaymentsReportEdit.PARAM_ENDING_VENDOR_GROUP_NAME + "\" VALUE=\"" + sEndingVendorGroupName + "\">");
		out.println("<INPUT TYPE=HIDDEN NAME=\"" + APCreatePaymentsReportEdit.PARAM_STARTING_ACCOUNT_SET_NAME + "\" VALUE=\"" + sStartingAcctSetName + "\">");
		out.println("<INPUT TYPE=HIDDEN NAME=\"" + APCreatePaymentsReportEdit.PARAM_ENDING_ACCOUNT_SET_NAME + "\" VALUE=\"" + sEndingAcctSetName + "\">");
		out.println("<INPUT TYPE=HIDDEN NAME=\"" + APCreatePaymentsReportEdit.PARAM_STARTING_INVOICE_AMT + "\" VALUE=\"" + sStartingInvoiceAmt + "\">");
		out.println("<INPUT TYPE=HIDDEN NAME=\"" + APCreatePaymentsReportEdit.PARAM_ENDING_INVOICE_AMT + "\" VALUE=\"" + sEndingInvoiceAmt + "\">");
		out.println("<INPUT TYPE=HIDDEN NAME=\"" + APCreatePaymentsReportEdit.PARAM_STARTING_VENDOR + "\" VALUE=\"" + sBatchDate + "\">");
		out.println("<INPUT TYPE=HIDDEN NAME=\"" + APCreatePaymentsReportEdit.PARAM_ENDING_VENDOR + "\" VALUE=\"" + sEndingVendor + "\">");
		out.println("<INPUT TYPE=SUBMIT"
			+ " NAME='" + SUBMIT_BUTTON_NAME + "'" 
			+ " VALUE='" + SUBMIT_BUTTON_VALUE + "'" 
			+ ">"
		);
		out.println("</FORM>");
		out.println("<BR>");
		
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
				+ sParamString
			);			
			return;
		}
		
		//System.out.println("[1512696965] - got to here");
		
		APAutoCreatePaymentBatch createbatch = new APAutoCreatePaymentBatch();
		APBatch autopaymentbatch = null;
		try {
			autopaymentbatch = createbatch.createPaymentBatch (
				conn,
				sPaymentDate,
				sBatchDate,
				sBankID,
				sSelectDocumentsBy,
				sDueDate,
				sStartingDiscountDate,
				sEndingDiscountDate,
				sStartingVendorGroupName,
				sEndingVendorGroupName,
				sStartingAcctSetName,
				sEndingAcctSetName,
				sStartingInvoiceAmt,
				sEndingInvoiceAmt,
				sStartingVendor,
				sEndingVendor,
				sUserID,
				sDBID,
				getServletContext()
				)
			;
		} catch (Exception e) {
			clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1547047697]");
			sWarning = "Failed to create payment batch - " + e.getMessage();
			if (sWarning.length() > MAX_ERROR_LENGTH){
				sWarning = sWarning.substring(0, MAX_ERROR_LENGTH) + "...";
			}
			response.sendRedirect(
				"" + SMUtilities.getURLLinkBase(getServletContext()) + "" + sCallingClass + "?"
				+ "Warning=" + sWarning
				+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
				+ sParamString
			);
			return;
		}
		
		//Print the temporary batch to the screen:
		long lStartingTime = System.currentTimeMillis();
		APPreCheckRegisterReport rpt = new APPreCheckRegisterReport();
		//System.out.println("[1512696966] - got to here");
		try {
			out.println(
				rpt.processReport(
					conn,
					autopaymentbatch, 
					SMSystemFunctions.isFunctionPermitted(
						SMSystemFunctions.APViewTransactionInformation, 
						sUserID, 
						conn, 
						(String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_LICENSE_MODULE_LEVEL)),
					SMSystemFunctions.isFunctionPermitted(
						SMSystemFunctions.APDisplayVendorInformation, 
						sUserID, 
						conn, 
						(String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_LICENSE_MODULE_LEVEL)),
					sDBID,
					getServletContext()
				)
			);
		} catch (Exception e) {
			System.out.println("[1512696967] - redirect = '"
					+ SMUtilities.getURLLinkBase(getServletContext()) + "" + sCallingClass + "?"
		    		+ SMTableapbatches.lbatchnumber + "=" + autopaymentbatch.getsbatchnumber()
		    		+ "&" + SMTableapbatches.ibatchtype + "=" + autopaymentbatch.getsbatchtype()
					+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
					+ "&Warning=" + e.getMessage()
			);
			clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1547047698]");
			response.sendRedirect(
				"" + SMUtilities.getURLLinkBase(getServletContext()) + "" + sCallingClass + "?"
		    		+ SMTableapbatches.lbatchnumber + "=" + autopaymentbatch.getsbatchnumber()
		    		+ "&" + SMTableapbatches.ibatchtype + "=" + autopaymentbatch.getsbatchtype()
					+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
					+ "&Warning=" + e.getMessage()
			);			
			return;
		}
		
		clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1547047699]");
		
		SMLogEntry log = new SMLogEntry(conn);
		log.writeEntry(sUserID,SMLogEntry.LOG_OPERATION_APPRECHECK, "REPORT", "AP Pre-Check Register", "[1495576378]");
		
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
