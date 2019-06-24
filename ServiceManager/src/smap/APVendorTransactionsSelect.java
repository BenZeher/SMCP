package smap;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import ConnectionPool.WebContextParameters;
import SMDataDefinition.SMTableapbatchentries;
import SMDataDefinition.SMTableaptransactions;
import SMDataDefinition.SMTableapvendorgroups;
import SMDataDefinition.SMTableicvendors;
import ServletUtilities.clsCreateHTMLFormFields;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsDateAndTimeConversions;
import ServletUtilities.clsManageRequestParameters;
import smcontrolpanel.SMAuthenticate;
import smcontrolpanel.SMSystemFunctions;
import smcontrolpanel.SMUtilities;

public class APVendorTransactionsSelect extends HttpServlet {
	private static final long serialVersionUID = 1L;
	public static String FIND_STARTING_VENDOR_BUTTON_NAME = "FINDSTARTINGVENDORBUTTON";
	public static String FIND_STARTING_VENDOR_BUTTON_LABEL = "Find";
	public static String FIND_ENDING_VENDOR_BUTTON_NAME = "FINDENDINGVENDORBUTTON";
	public static String FIND_ENDING_VENDOR_BUTTON_LABEL = "Find";
	public static String PARAM_TRANSACTION_TYPE_PREFIX = "TRANSACTION_TYPE";
	public static String PARAM_DOWNLOAD_TO_HTML = "DOWNLOADTOHTML";
	public static String PARAM_STARTING_VENDOR = "StartingVendor";
	public static String PARAM_ENDING_VENDOR = "EndingVendor";
	public static String PARAM_STARTING_GROUP = "StartingVendorGroup";
	public static String PARAM_ENDING_GROUP = "EndingVendorGroup";
	public static String PARAM_STARTING_DOCUMENT_NUMBER = "StartingDocumentNumber";
	public static String PARAM_STARTING_DOCUMENT_DATE = "StartingDocumentDate";
	public static String PARAM_ENDING_DOCUMENT_DATE = "EndingDocumentDate";
	public static String PARAM_PRINT_VENDORS_WITH_A_ZERO_BALANCE = "PrintVendorsWithAZeroBalance";
	public static String PARAM_INCLUDE_APPLIED_DETAILS = "IncludeAppliedDetails";
	public static String PARAM_INCLUDE_FULLY_PAID_TRANSACTIONS = "IncludeFullyPaidTransactions";
	
	public void doPost(HttpServletRequest request,
			HttpServletResponse response)
	throws ServletException, IOException {

		PrintWriter out = response.getWriter();
	    if (!SMAuthenticate.authenticateSMCPCredentials(request, response, getServletContext(), SMSystemFunctions.APVendorTransactionsReport)){
	    	return;
	    }
		//Get the session info:
		HttpSession CurrentSession = request.getSession(true);
		String sDBID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_DATABASE_ID);
		String sUserID = (String)CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERID);
		String sUserFullName = (String)CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERFIRSTNAME) + " "
				       + (String)CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERLASTNAME);
		String sCompanyName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_COMPANYNAME);
		String title = "Vendor Transactions Report";
		String subtitle = "";
		out.println(SMUtilities.SMCPTitleSubBGColor(title, subtitle, SMUtilities.getInitBackGroundColor(getServletContext(), sDBID), sCompanyName));
		out.println(SMUtilities.getDatePickerIncludeString(getServletContext()));

		if (clsManageRequestParameters.get_Request_Parameter("Warning", request).compareToIgnoreCase("") != 0){
			out.println("<BR><FONT COLOR=RED><B>WARNING: " + clsManageRequestParameters.get_Request_Parameter("Warning", request) + "</B></FONT><BR>");
		}
		if (clsManageRequestParameters.get_Request_Parameter("Status", request).compareToIgnoreCase("") != 0){
			out.println("<BR><B>NOTE: " + clsManageRequestParameters.get_Request_Parameter("Status", request) + "</B><BR>");
		}
		//Print a link to the first page after login:
		out.println("<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMUserLogin?" 
				+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
				+ "\">Return to user login</A><BR>");
		//Print a link to main menu:
		out.println("<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smap.APMainMenu?" 
				+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
				+ "\">Return to Accounts Payable Main Menu</A><BR>");
		out.println("<A HREF=\"" + WebContextParameters.getdocumentationpageURL(getServletContext()) + "#" + Long.toString(SMSystemFunctions.APVendorTransactionsReport)
				+ "\">Summary</A><BR>");

		out.println ("<FORM ACTION =\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smap.APVendorTransactionsGenerate\">");
		out.println("<INPUT TYPE=HIDDEN NAME='" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "' VALUE='" + sDBID + "'>");
		out.println("<INPUT TYPE=HIDDEN NAME=CallingClass VALUE=\"" + this.getClass().getName() + "\">");
		//out.println("<TABLE WIDTH=100% CELLPADDING=10 border=4>\n");
		//Starting date will be 1990-01-01
		out.println("<TABLE BORDER=0>\n");

		//Starting date:
		String sStartingDocumentDate = clsManageRequestParameters.get_Request_Parameter(PARAM_STARTING_DOCUMENT_DATE, request);
		if (sStartingDocumentDate.compareToIgnoreCase("") == 0){
			sStartingDocumentDate = "01/01/2000";
		}
		out.println("  <TR>\n");
		
		out.println(
			"    <TD ALIGN=RIGHT><B>Starting document date:&nbsp;</B></TD>\n"
			+ "    <TD ALIGN=LEFT>\n"
			+ "<INPUT TYPE=TEXT NAME=\"" + PARAM_STARTING_DOCUMENT_DATE + "\""
			+ " VALUE=\"" + sStartingDocumentDate + "\""
			+ " SIZE=12"
			+ " MAXLENGTH=10"
			+ ">"
			+ "&nbsp;"
			+ SMUtilities.getDatePickerString(PARAM_STARTING_DOCUMENT_DATE, getServletContext())
			+ "</TD>\n"
		);
		
		out.println("  </TR>\n");

		//Ending date:
		String sEndingDocumentDate = clsManageRequestParameters.get_Request_Parameter(PARAM_ENDING_DOCUMENT_DATE, request);
		if (sEndingDocumentDate.compareToIgnoreCase("") == 0){
			sEndingDocumentDate = clsDateAndTimeConversions.now("MM/dd/yyyy");
		}
		out.println("  <TR>\n");
		
		out.println(
				"    <TD ALIGN=RIGHT><B>Ending document date:&nbsp;</B></TD>\n"
				+ "    <TD ALIGN=LEFT>\n"
				+ "<INPUT TYPE=TEXT NAME=\"" + PARAM_ENDING_DOCUMENT_DATE + "\""
				+ " VALUE=\"" + sEndingDocumentDate + "\""
				+ " SIZE=12"
				+ " MAXLENGTH=10"
				+ ">"
				+ "&nbsp;"
				+ SMUtilities.getDatePickerString(PARAM_ENDING_DOCUMENT_DATE, getServletContext())
				+ "</TD>\n"
			);
		
		out.println("  </TR>\n");
		
		//Vendor selection range:
		String sSQL = "";
		String sStartingVendorNumber = clsManageRequestParameters.get_Request_Parameter(PARAM_STARTING_VENDOR, request);
		String sEndingVendorNumber = clsManageRequestParameters.get_Request_Parameter(PARAM_ENDING_VENDOR, request);
		String sStartingVendorGroup = clsManageRequestParameters.get_Request_Parameter(PARAM_STARTING_GROUP, request);
		String sEndingVendorGroup = clsManageRequestParameters.get_Request_Parameter(PARAM_ENDING_GROUP, request);
		ResultSet rsVendors = null;
		if (sStartingVendorNumber.compareToIgnoreCase("") == 0){
			sSQL = "SELECT " 
				+ SMTableicvendors.svendoracct 
				+ ", " + SMTableicvendors.sname
				+ " FROM " + SMTableicvendors.TableName
				+ " ORDER BY " + SMTableicvendors.svendoracct + " ASC LIMIT 1";
			try {
				rsVendors = clsDatabaseFunctions.openResultSet(
					sSQL, 
					getServletContext(), 
					sDBID,
					"MySQL",
					this.toString() + ".doPost (1) - User: " + sUserID
					+ " - "
					+ sUserFullName
						);

				if (rsVendors.next()){
					sStartingVendorNumber = rsVendors.getString(SMTableicvendors.svendoracct);
				}
				rsVendors.close();
			} catch (SQLException e) {
				out.println("Error [1502469178] loading starting vendor - " + e.getMessage());
			}
		}
		if (sEndingVendorNumber.compareToIgnoreCase("") == 0){
			sSQL = "SELECT " 
				+ SMTableicvendors.svendoracct 
				+ ", " + SMTableicvendors.sname
				+ " FROM " + SMTableicvendors.TableName
				+ " ORDER BY " + SMTableicvendors.svendoracct + " DESC LIMIT 1";
			try {
				rsVendors = clsDatabaseFunctions.openResultSet(
					sSQL, 
					getServletContext(), 
					sDBID,
					"MySQL",
					this.toString() + ".doPost (2) - User: " + sUserID
					+ " - "
					+ sUserFullName
						);
				if (rsVendors.next()){
					sEndingVendorNumber = rsVendors.getString(SMTableicvendors.svendoracct);
				}
				rsVendors.close();
			} catch (SQLException e) {
				out.println("Error [1502469179] loading ending vendor - " + e.getMessage());
			}
		}
		
		out.println("  <TR>\n");
		
		out.println(
			"    <TD ALIGN=RIGHT>" + "<B>Starting with vendor:</B>&nbsp;</TD>\n"
			+ "    <TD ALIGN=LEFT>"
			+ clsCreateHTMLFormFields.TDTextBox(
				PARAM_STARTING_VENDOR, 
				sStartingVendorNumber, 
				10, 
				SMTableicvendors.svendoracctLength, 
			"")
			+ "&nbsp;"
			+ "<INPUT TYPE=" + "\"SUBMIT\"" 
			+ " NAME=\"" + FIND_STARTING_VENDOR_BUTTON_NAME + "\""
			+ " VALUE=\"" + FIND_STARTING_VENDOR_BUTTON_LABEL + "\">"
			+ "</TD>\n"
		);
		
		out.println("  </TR>\n");
		
		out.println("  <TR>\n");

		out.println(
			"    <TD ALIGN=RIGHT>" + "<B>Ending with vendor:</B>&nbsp;</TD>\n"
			+ "    <TD ALIGN=LEFT>"
			+ clsCreateHTMLFormFields.TDTextBox(
				PARAM_ENDING_VENDOR, 
				sEndingVendorNumber, 
				10, 
				SMTableicvendors.svendoracctLength,
			"") 
			+ "&nbsp;"
			+ "<INPUT TYPE=" + "\"SUBMIT\"" 
			+ " NAME=\"" + FIND_ENDING_VENDOR_BUTTON_NAME + "\""
			+ " VALUE=\"" + FIND_ENDING_VENDOR_BUTTON_LABEL + "\">"
			+ "</TD>\n");
		
		out.println("  </TR>\n");
		
		ArrayList<String> sVendorGroups = new ArrayList<String>(0);
		ArrayList<String> sVendorGroupNumbers = new ArrayList<String>(0);
		
		if ((sStartingVendorGroup.compareToIgnoreCase("") == 0) || (sEndingVendorGroup.compareToIgnoreCase("") == 0 )){
			sSQL = "SELECT DISTINCT " 
				+ SMTableapvendorgroups.TableName  + "." + SMTableapvendorgroups.sdescription
				+ ", " + SMTableapvendorgroups.TableName + "." + SMTableapvendorgroups.lid
				+ " FROM " + SMTableicvendors.TableName
				+ " LEFT JOIN " +SMTableapvendorgroups.TableName + " ON " 
				+  SMTableicvendors.TableName + "." + SMTableicvendors.ivendorgroupid + " = " 
				+  SMTableapvendorgroups.TableName + "." + SMTableapvendorgroups.lid				
				+ " ORDER BY " + SMTableapvendorgroups.lid + " ASC ";
			try {
				rsVendors = clsDatabaseFunctions.openResultSet(
					sSQL, 
					getServletContext(), 
					sDBID,
					"MySQL",
					this.toString() + ".doPost (2) - User: " + sUserID
					+ " - "
					+ sUserFullName
						);
				while (rsVendors.next()){
					sVendorGroups.add(rsVendors.getString(SMTableapvendorgroups.TableName  + "." + SMTableapvendorgroups.sdescription));
					sVendorGroupNumbers.add(rsVendors.getString(SMTableapvendorgroups.TableName + "." + SMTableapvendorgroups.lid));
				}
				rsVendors.close();
			} catch (SQLException e) {
				out.println("Error [1561378774] loading ending vendor - " + e.getMessage());
			}
		}
		
		
		String sStartingGroupSelected = sVendorGroups.get(0);
		String sEndingGroupSelected = sVendorGroupNumbers.get(sVendorGroupNumbers.size()-1);
		
		out.println("<TR>\n");
		out.println("<TD ALIGN=RIGHT>" + "<B>Starting with Vendor Group:</B>&nbsp;</TD>\n"
				+ "<TD ALIGN=LEFT>");
		out.println(clsCreateHTMLFormFields.TDDropDownBox(
				PARAM_STARTING_GROUP, 
				sVendorGroupNumbers,
				sVendorGroups,
        		sStartingGroupSelected)
        );
		out.println("</TD></TR>\n");
		out.println("<TR>\n");
		out.println("<TD ALIGN=RIGHT>" + "<B>Ending with Vendor Group:</B>&nbsp;</TD>\n"
				+ "<TD ALIGN=LEFT>");
		out.println(clsCreateHTMLFormFields.TDDropDownBox(
				PARAM_ENDING_GROUP, 
				sVendorGroupNumbers,
        		sVendorGroups,
        		sEndingGroupSelected)
        );
		out.println("</TD></TR>\n");
		
		
		out.println("<TR>\n");
		out.println("</TR>\n");
		
		
		//Starting document number
		String sStartingDocumentNumber = clsManageRequestParameters.get_Request_Parameter(PARAM_STARTING_DOCUMENT_NUMBER, request);
		out.println("  <TR>\n");

		out.println(
			"    <TD ALIGN=RIGHT>" + "<B>Starting with document number:</B>&nbsp;</TD>\n"
			+ "    <TD ALIGN=LEFT>"
			+ clsCreateHTMLFormFields.TDTextBox(
				PARAM_STARTING_DOCUMENT_NUMBER, 
				sStartingDocumentNumber, 
				20, 
				SMTableaptransactions.sdocnumberlength,
			"") 
			+ "</TD>\n"
		);
		
		out.println("  </TR>\n");
		
		//Transaction types:
		out.println("  <TR>\n");
		
		out.println(
			"    <TD ALIGN=RIGHT VALIGN=TOP>" + "<B>Include transaction types:</B>&nbsp;</TD>\n"
			+ "    <TD ALIGN=LEFT>"
		);
		
		boolean bTransactionTypeCreditNote = clsManageRequestParameters.get_Request_Parameter(
			PARAM_TRANSACTION_TYPE_PREFIX + Integer.toString(SMTableaptransactions.AP_TRANSACTION_TYPE_INVOICE_CREDITNOTE), request).compareToIgnoreCase("") != 0;
		boolean bTransactionTypeDebitNote = clsManageRequestParameters.get_Request_Parameter(
			PARAM_TRANSACTION_TYPE_PREFIX + Integer.toString(SMTableaptransactions.AP_TRANSACTION_TYPE_INVOICE_DEBITNOTE), request).compareToIgnoreCase("") != 0;
		boolean bTransactionTypeInvoice = clsManageRequestParameters.get_Request_Parameter(
			PARAM_TRANSACTION_TYPE_PREFIX + Integer.toString(SMTableaptransactions.AP_TRANSACTION_TYPE_INVOICE_INVOICE), request).compareToIgnoreCase("") != 0;
		boolean bTransactionTypeApplyTo = clsManageRequestParameters.get_Request_Parameter(
			PARAM_TRANSACTION_TYPE_PREFIX + Integer.toString(SMTableaptransactions.AP_TRANSACTION_TYPE_PAYMENT_APPLYTO), request).compareToIgnoreCase("") != 0;
		boolean bTransactionTypeMiscPayment = clsManageRequestParameters.get_Request_Parameter(
			PARAM_TRANSACTION_TYPE_PREFIX + Integer.toString(SMTableaptransactions.AP_TRANSACTION_TYPE_PAYMENT_MISCPAYMENT), request).compareToIgnoreCase("") != 0;
		boolean bTransactionTypePayment = clsManageRequestParameters.get_Request_Parameter(
			PARAM_TRANSACTION_TYPE_PREFIX + Integer.toString(SMTableaptransactions.AP_TRANSACTION_TYPE_PAYMENT_PAYMENT), request).compareToIgnoreCase("") != 0;
		boolean bTransactionTypePrePayment = clsManageRequestParameters.get_Request_Parameter(
			PARAM_TRANSACTION_TYPE_PREFIX + Integer.toString(SMTableaptransactions.AP_TRANSACTION_TYPE_PAYMENT_PREPAYMENT), request).compareToIgnoreCase("") != 0;
		boolean bTransactionTypeCheckReversal = clsManageRequestParameters.get_Request_Parameter(
			PARAM_TRANSACTION_TYPE_PREFIX + Integer.toString(SMTableaptransactions.AP_TRANSACTION_TYPE_REVERSAL), request).compareToIgnoreCase("") != 0;
		
		
		out.println(
			clsCreateHTMLFormFields.TDCheckBoxWithoutReturns(
				PARAM_TRANSACTION_TYPE_PREFIX + Integer.toString(SMTableaptransactions.AP_TRANSACTION_TYPE_INVOICE_CREDITNOTE), 
				(bTransactionTypeCreditNote || (request.getParameter(PARAM_TRANSACTION_TYPE_PREFIX + Integer.toString(SMTableaptransactions.AP_TRANSACTION_TYPE_INVOICE_CREDITNOTE)) == null)), 
				SMTableapbatchentries.getDocumentTypeLabel(SMTableaptransactions.AP_TRANSACTION_TYPE_INVOICE_CREDITNOTE)
			)
			+ "&nbsp;"
			+ clsCreateHTMLFormFields.TDCheckBoxWithoutReturns(
				PARAM_TRANSACTION_TYPE_PREFIX + Integer.toString(SMTableaptransactions.AP_TRANSACTION_TYPE_INVOICE_DEBITNOTE), 
				(bTransactionTypeDebitNote || (request.getParameter(PARAM_TRANSACTION_TYPE_PREFIX + Integer.toString(SMTableaptransactions.AP_TRANSACTION_TYPE_INVOICE_DEBITNOTE)) == null)), 
				SMTableapbatchentries.getDocumentTypeLabel(SMTableaptransactions.AP_TRANSACTION_TYPE_INVOICE_DEBITNOTE)
			)
			+ "&nbsp;"
			+ clsCreateHTMLFormFields.TDCheckBoxWithoutReturns(
				PARAM_TRANSACTION_TYPE_PREFIX + Integer.toString(SMTableaptransactions.AP_TRANSACTION_TYPE_INVOICE_INVOICE), 
				(bTransactionTypeInvoice || (request.getParameter(PARAM_TRANSACTION_TYPE_PREFIX + Integer.toString(SMTableaptransactions.AP_TRANSACTION_TYPE_INVOICE_INVOICE)) == null)), 
				SMTableapbatchentries.getDocumentTypeLabel(SMTableaptransactions.AP_TRANSACTION_TYPE_INVOICE_INVOICE)
			)
			+ "&nbsp;"
			+ clsCreateHTMLFormFields.TDCheckBoxWithoutReturns(
				PARAM_TRANSACTION_TYPE_PREFIX + Integer.toString(SMTableaptransactions.AP_TRANSACTION_TYPE_PAYMENT_APPLYTO), 
				(bTransactionTypeApplyTo || (request.getParameter(PARAM_TRANSACTION_TYPE_PREFIX + Integer.toString(SMTableaptransactions.AP_TRANSACTION_TYPE_PAYMENT_APPLYTO)) == null)), 
				SMTableapbatchentries.getDocumentTypeLabel(SMTableaptransactions.AP_TRANSACTION_TYPE_PAYMENT_APPLYTO)
			)
			+ "&nbsp;"
			+ clsCreateHTMLFormFields.TDCheckBoxWithoutReturns(
				PARAM_TRANSACTION_TYPE_PREFIX + Integer.toString(SMTableaptransactions.AP_TRANSACTION_TYPE_PAYMENT_MISCPAYMENT), 
				(bTransactionTypeMiscPayment || (request.getParameter(PARAM_TRANSACTION_TYPE_PREFIX + Integer.toString(SMTableaptransactions.AP_TRANSACTION_TYPE_PAYMENT_MISCPAYMENT)) == null)), 
				SMTableapbatchentries.getDocumentTypeLabel(SMTableaptransactions.AP_TRANSACTION_TYPE_PAYMENT_MISCPAYMENT)
			)
			+ "&nbsp;"
			+ clsCreateHTMLFormFields.TDCheckBoxWithoutReturns(
				PARAM_TRANSACTION_TYPE_PREFIX + Integer.toString(SMTableaptransactions.AP_TRANSACTION_TYPE_PAYMENT_PAYMENT), 
				(bTransactionTypePayment || (request.getParameter(PARAM_TRANSACTION_TYPE_PREFIX + Integer.toString(SMTableaptransactions.AP_TRANSACTION_TYPE_PAYMENT_PAYMENT)) == null)), 
				SMTableapbatchentries.getDocumentTypeLabel(SMTableaptransactions.AP_TRANSACTION_TYPE_PAYMENT_PAYMENT)
			)
			+ "&nbsp;"
			+ clsCreateHTMLFormFields.TDCheckBoxWithoutReturns(
				PARAM_TRANSACTION_TYPE_PREFIX + Integer.toString(SMTableaptransactions.AP_TRANSACTION_TYPE_PAYMENT_PREPAYMENT), 
				(bTransactionTypePrePayment || (request.getParameter(PARAM_TRANSACTION_TYPE_PREFIX + Integer.toString(SMTableaptransactions.AP_TRANSACTION_TYPE_PAYMENT_PREPAYMENT)) == null)), 
				SMTableapbatchentries.getDocumentTypeLabel(SMTableaptransactions.AP_TRANSACTION_TYPE_PAYMENT_PREPAYMENT)
			)
			+ "&nbsp;"
			+ clsCreateHTMLFormFields.TDCheckBoxWithoutReturns(
				PARAM_TRANSACTION_TYPE_PREFIX + Integer.toString(SMTableaptransactions.AP_TRANSACTION_TYPE_REVERSAL), 
				(bTransactionTypeCheckReversal || (request.getParameter(PARAM_TRANSACTION_TYPE_PREFIX + Integer.toString(SMTableaptransactions.AP_TRANSACTION_TYPE_REVERSAL)) == null)), 
				SMTableapbatchentries.getDocumentTypeLabel(SMTableaptransactions.AP_TRANSACTION_TYPE_REVERSAL)
			)
		); 
		
		out.println("    </TD>\n");
		out.println("  </TR>\n");
		
		//Options to show:
		//Transaction types:
		out.println("  <TR>\n");
		
		out.println(
			"    <TD ALIGN=RIGHT VALIGN=TOP>" + "<B>Show:</B>&nbsp;</TD>\n"
			+ "    <TD ALIGN=LEFT>"
		);
		
		boolean bShowVendorsWithAZeroBalance = clsManageRequestParameters.get_Request_Parameter(PARAM_PRINT_VENDORS_WITH_A_ZERO_BALANCE, request).compareToIgnoreCase("") != 0;
		boolean bShowAppliedDetails = clsManageRequestParameters.get_Request_Parameter(PARAM_INCLUDE_APPLIED_DETAILS, request).compareToIgnoreCase("") != 0;
		boolean bShowFullyPaidTransactions = clsManageRequestParameters.get_Request_Parameter(PARAM_INCLUDE_FULLY_PAID_TRANSACTIONS, request).compareToIgnoreCase("") != 0;
		
		out.println(
			clsCreateHTMLFormFields.TDCheckBoxWithoutReturns(
				PARAM_PRINT_VENDORS_WITH_A_ZERO_BALANCE, 
				bShowVendorsWithAZeroBalance, 
				"Vendors with a zero balance"
			)
			+ "&nbsp;"
			+ clsCreateHTMLFormFields.TDCheckBoxWithoutReturns(
				PARAM_INCLUDE_APPLIED_DETAILS, 
				bShowAppliedDetails, 
				"Applied details"
			)
			+ "&nbsp;"
			+ clsCreateHTMLFormFields.TDCheckBoxWithoutReturns(
				PARAM_INCLUDE_FULLY_PAID_TRANSACTIONS, 
				bShowFullyPaidTransactions, 
				"Fully paid transactions"
			)
		); 
		
		out.println("    </TD>\n");
		out.println("  </TR>\n");
			
		out.println("</TABLE>\n\n");

		out.println("<INPUT TYPE=\"SUBMIT\" VALUE=\"----View----\">");
		out.println("</FORM>");

		out.println("</BODY></HTML>");
	}

	public void doGet(HttpServletRequest request,
			HttpServletResponse response)
	throws ServletException, IOException {

		doPost(request, response);
	}
}
