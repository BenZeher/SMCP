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
import SMDataDefinition.SMTableapaccountsets;
import SMDataDefinition.SMTableicvendors;
import ServletUtilities.clsCreateHTMLFormFields;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsDateAndTimeConversions;
import ServletUtilities.clsManageRequestParameters;
import smcontrolpanel.SMAuthenticate;
import smcontrolpanel.SMSystemFunctions;
import smcontrolpanel.SMUtilities;

public class APAgedPayablesSelect extends HttpServlet {
	private static final long serialVersionUID = 1L;
	public static String FIND_STARTING_VENDOR_BUTTON_NAME = "FINDSTARTINGVENDORBUTTON";
	public static String FIND_STARTING_VENDOR_BUTTON_LABEL = "Find";
	public static String FIND_ENDING_VENDOR_BUTTON_NAME = "FINDENDINGVENDORBUTTON";
	public static String FIND_ENDING_VENDOR_BUTTON_LABEL = "Find";
	public static String PARAM_DOWNLOAD_TO_HTML = "DOWNLOADTOHTML";
	public static String PARAM_STARTING_VENDOR = "StartingVendor";
	public static String PARAM_ENDING_VENDOR = "EndingVendor";
	public static String PARAM_AS_OF_DATE = "AsOfDate";
	public static String PARAM_CUT_OFF_BY = "CutOffBy";
	public static String PARAM_CUT_OFF_BY_DOCUMENT_DATE = "Document Date";
	public static String PARAM_CUT_OFF_DATE = "CutOffDate";
	public static String PARAM_PRINT_TRANSACTION_IN_DETAIL_OR_SUMMARY = "PrintTransactionIn";
	public static String PARAM_PRINT_TRANSACTION_IN_DETAIL_LABEL = "Detail";
	public static String PARAM_PRINT_TRANSACTION_IN_SUMMARY_LABEL = "Summary";
	public static String PARAM_ACCOUNT_SET = "AccountSet";
	public static String PARAM_ACCOOUNT_SET_ALL_ACCOUNT_SETS = "ALL Account Sets";
	public static String PARAM_AGING_CATEGORY_FIRST = "1st";
	public static String PARAM_AGING_CATEGORY_SECOND = "2nd";
	public static String PARAM_AGING_CATEGORY_THIRD = "3rd";
	public static String PARAM_SORT_BY = "SortBy";
	public static String PARAM_SORT_BY_ACCOUNT = "Vendor Account";
	public static String PARAM_SORT_BY_NAME = "Vendor Name";
	public static String PARAM_SORT_DETAIL_BY_TRANSACTION_TYPE = "SortByTransactionType";
	public static String PARAM_PRINT_VENDORS_WITH_A_ZERO_BALANCE = "PrintVendorsWithAZeroBalance";
	public static String PARAM_INCLUDE_APPLIED_DETAILS = "IncludeAppliedDetails";
	public static String PARAM_INCLUDE_FULLY_PAID_TRANSACTIONS = "IncludeFullyPaidTransactions";
	public static String PARAM_INCLUDE_INACTIVE_VENDORS = "IncludeInactiveVendors";
	public static String PARAM_INCLUDE_TRANSACTIONS_ON_HOLD = "IncludeTransactionsOnHold";
	
	private static String sDBID = "";
	private static String sUserID = "";
	private static String sUserFullName = "";
	private static String sCompanyName = "";
	public void doPost(HttpServletRequest request,
			HttpServletResponse response)
	throws ServletException, IOException {

		PrintWriter out = response.getWriter();
	    if (!SMAuthenticate.authenticateSMCPCredentials(request, response, getServletContext(), SMSystemFunctions.APAgedPayables)){
	    	return;
	    }
		//Get the session info:
		HttpSession CurrentSession = request.getSession(true);
		sDBID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_DATABASE_ID);
		sUserID = (String)CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERID);
		sUserFullName = (String)CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERFIRSTNAME) + " "
						+ (String)CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERLASTNAME);
		sCompanyName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_COMPANYNAME);
		String title = "Aged Payables Report";
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
		out.println("<A HREF=\"" + WebContextParameters.getdocumentationpageURL(getServletContext()) + "#" + Long.toString(SMSystemFunctions.APAgedPayables)
				+ "\">Summary</A><BR>");

		try {
			ArrayList<String> alValues = new ArrayList<String>(0);
			ArrayList<String> alOptions = new ArrayList<String>(0);
			out.println ("<FORM ACTION =\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smap.APAgedPayablesGenerate\">");
			out.println("<INPUT TYPE=HIDDEN NAME='" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "' VALUE='" + sDBID + "'>");
			out.println("<INPUT TYPE=HIDDEN NAME=CallingClass VALUE=\"" + this.getClass().getName() + "\">");
			out.println("<TABLE WIDTH=100% CELLPADDING=10 border=4>\n");
			//Starting date will be 1990-01-01
			out.println("<INPUT TYPE=HIDDEN NAME=SelectedStartYear VALUE=1990>");
			out.println("<INPUT TYPE=HIDDEN NAME=SelectedStartMonth VALUE=1>");
			out.println("<INPUT TYPE=HIDDEN NAME=SelectedStartDay VALUE=1>");

			out.println("  <TR>\n"
				+ "    <TD ALIGN=CENTER WIDTH=100%>\n"
				+ "<TABLE BORDER=0 WIDTH=100%>\n");

			//As of date:
			String sAsOfDate = clsManageRequestParameters.get_Request_Parameter(PARAM_AS_OF_DATE, request);
			if (sAsOfDate.compareToIgnoreCase("") == 0){
				sAsOfDate = clsDateAndTimeConversions.now("MM/dd/yyyy");
			}
			out.println("  <TR>\n"
				+ "    <TD ALIGN=LEFT WIDTH=30%><B>Age As Of:&nbsp;</B>"
				+ "<INPUT TYPE=TEXT NAME=\"" + PARAM_AS_OF_DATE + "\""
				+ " VALUE=\"" + sAsOfDate + "\""
				+ " SIZE=12"
				+ " MAXLENGTH=10"
				+ ">"
				+ SMUtilities.getDatePickerString(PARAM_AS_OF_DATE, getServletContext())
				+ "</TD>\n"
			);
			
			//Cut off by TYPE of date (no needed?)
			alValues.clear(); alOptions.clear();
			alValues.add(PARAM_CUT_OFF_BY_DOCUMENT_DATE); alOptions.add(PARAM_CUT_OFF_BY_DOCUMENT_DATE);
			String sDefaultCutoffBy = clsManageRequestParameters.get_Request_Parameter(PARAM_CUT_OFF_BY, request);
			out.println("    <TD ALIGN=LEFT><B>Cut off by:&nbsp;</B>" + "<SELECT NAME=\"" + PARAM_CUT_OFF_BY + "\">");
			for (int i=0;i<alValues.size();i++){
				if (alValues.get(i).compareToIgnoreCase(sDefaultCutoffBy) == 0){
					out.println("<OPTION VALUE=\"" + alValues.get(i) + "\" CHECKED > " + alOptions.get(i));
				}else{
					out.println("<OPTION VALUE=\"" + alValues.get(i) + "\"> " + alOptions.get(i));
				}
			}
			out.println("</SELECT>");
			out.println("</TD>\n");
			
			//Cut off date:
			String sCutOffDate = clsManageRequestParameters.get_Request_Parameter(PARAM_CUT_OFF_DATE, request);
			if (sCutOffDate.compareToIgnoreCase("") == 0){
				sCutOffDate = clsDateAndTimeConversions.now("MM/dd/yyyy");
			}
			out.println("    <TD ALIGN=LEFT WIDTH=35%><B>Cut off date:&nbsp;</B>"
				+ "<INPUT TYPE=TEXT NAME=\"" + PARAM_CUT_OFF_DATE + "\""
				+ " VALUE=\"" + sCutOffDate + "\""
				+ " SIZE=12"
				+ " MAXLENGTH=10"
				+ ">"
				+ SMUtilities.getDatePickerString(PARAM_CUT_OFF_DATE, getServletContext())
				+ "</TD>\n"
			);
			
			out.println("  </TR>\n");

			//Print transactions in
			String sPrintTransactionsIn = clsManageRequestParameters.get_Request_Parameter(PARAM_PRINT_TRANSACTION_IN_DETAIL_OR_SUMMARY, request);
			alValues.clear(); alOptions.clear();
			alValues.add(PARAM_PRINT_TRANSACTION_IN_DETAIL_LABEL); alOptions.add(PARAM_PRINT_TRANSACTION_IN_DETAIL_LABEL);
			alValues.add(PARAM_PRINT_TRANSACTION_IN_SUMMARY_LABEL); alOptions.add(PARAM_PRINT_TRANSACTION_IN_SUMMARY_LABEL);

			out.println("  <TR>\n"
				+ "    <TD ALIGN=LEFT WIDTH=20%><B>Print Transactions In&nbsp;</B>"
				+ clsCreateHTMLFormFields.Create_Edit_Form_List_Field(
					PARAM_PRINT_TRANSACTION_IN_DETAIL_OR_SUMMARY, 
					alValues, 
					sPrintTransactionsIn, 
					alOptions)
				+ "</TD>\n");
			
			String sSQL = "";
			String sStartingVendorNumber = clsManageRequestParameters.get_Request_Parameter(PARAM_STARTING_VENDOR, request);
			String sEndingVendorNumber = clsManageRequestParameters.get_Request_Parameter(PARAM_ENDING_VENDOR, request);
			ResultSet rsVendors = null;
			//get customer list from database if it's not passed in:
			if (sStartingVendorNumber.compareToIgnoreCase("") == 0){
				sSQL = "SELECT " 
					+ SMTableicvendors.svendoracct 
					+ ", " + SMTableicvendors.sname
					+ " FROM " + SMTableicvendors.TableName
					+ " ORDER BY " + SMTableicvendors.svendoracct + " ASC LIMIT 1";
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
			}
			if (sEndingVendorNumber.compareToIgnoreCase("") == 0){
				sSQL = "SELECT " 
					+ SMTableicvendors.svendoracct 
					+ ", " + SMTableicvendors.sname
					+ " FROM " + SMTableicvendors.TableName
					+ " ORDER BY " + SMTableicvendors.svendoracct + " DESC LIMIT 1";
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
			}
			out.println("    <TD WIDTH=30%>" + "<B>Starting with vendor:</B> " 
				+ clsCreateHTMLFormFields.TDTextBox(
					PARAM_STARTING_VENDOR, 
					sStartingVendorNumber, 
					10, 
					SMTableicvendors.svendoracctLength, 
				"")
				+ "<INPUT TYPE=" + "\"SUBMIT\"" 
				+ " NAME=\"" + FIND_STARTING_VENDOR_BUTTON_NAME + "\""
				+ " VALUE=\"" + FIND_STARTING_VENDOR_BUTTON_LABEL + "\">"
				+ "</TD>\n"
			);

			out.println("    <TD WIDTH=30%>" + "<B>Ending with vendor:</B> " 
			+ clsCreateHTMLFormFields.TDTextBox(
					PARAM_ENDING_VENDOR, 
					sEndingVendorNumber, 
					10, 
					SMTableicvendors.svendoracctLength,
				"") 
				+ "<INPUT TYPE=" + "\"SUBMIT\"" 
				+ " NAME=\"" + FIND_ENDING_VENDOR_BUTTON_NAME + "\""
				+ " VALUE=\"" + FIND_ENDING_VENDOR_BUTTON_LABEL + "\">"
				+ "</TD>\n");
			out.println("</TABLE>\n"
				+ "</TD>\n"
				+ "  </TR>\n");

			out.println("  <TR>\n"
				+ "    <TD ALIGN=CENTER WIDTH=100%>\n");
			out.println("<TABLE BORDER=0 WIDTH=100%>\n");
			out.println("  <TR>\n");
			

			//Aging categories:
			out.println("  <TR>\n");
			out.println("    <TD ALIGN=LEFT><B>Aging categories:</B></TD>\n");
			
			//deadline for 1st
			String s1st = clsManageRequestParameters.get_Request_Parameter(PARAM_AGING_CATEGORY_FIRST, request);
			if (s1st.compareToIgnoreCase("") == 0){
				s1st = "30";
			}
			out.println("    <TD ALIGN=LEFT WIDTH=20%><B>1st column:&nbsp;</B>Up to&nbsp;</B>"
				+ clsCreateHTMLFormFields.TDTextBox(PARAM_AGING_CATEGORY_FIRST, s1st, 6, 10, "") + "&nbsp;days</TD>\n");
			
			//deadline for 2nd
			String s2nd = clsManageRequestParameters.get_Request_Parameter(PARAM_AGING_CATEGORY_SECOND, request);
			if (s2nd.compareToIgnoreCase("") == 0){
				s2nd = "60";
			}
			out.println("    <TD ALIGN=LEFT WIDTH=20%><B>2nd column:&nbsp;</B>Up to&nbsp;</B>"
				+ clsCreateHTMLFormFields.TDTextBox(PARAM_AGING_CATEGORY_SECOND, s2nd, 6, 10, "") + "&nbsp;days</TD>\n");
			
			//deadline for 3rd
			String s3rd = clsManageRequestParameters.get_Request_Parameter(PARAM_AGING_CATEGORY_THIRD, request);
			if (s3rd.compareToIgnoreCase("") == 0){
				s3rd = "90";
			}
			out.println("    <TD ALIGN=LEFT WIDTH=20%><B>3rd column:&nbsp;</B>Up to&nbsp;</B>"
				+ clsCreateHTMLFormFields.TDTextBox(PARAM_AGING_CATEGORY_THIRD, s3rd, 6, 10, "") + "&nbsp;days</TD>\n");
			
			out.println("  </TR>\n");
			out.println("</TABLE>\n");
			out.println("    </TD>\n"
				+ "  </TR>\n");

			//Get the account sets:
			//Account sets:
			out.println("  <TR>\n"
				+ "    <TD ALIGN=CENTER WIDTH=100%>\n");
			out.println("<TABLE BORDER=0 WIDTH=100%>\n");
			out.println("  <TR>\n"
				+ "    <TD ALIGN=LEFT WIDTH=100%>" 
					+ "<B>For Control Account Set:&nbsp;");

			String sAccountSet = clsManageRequestParameters.get_Request_Parameter(PARAM_ACCOUNT_SET, request);
			try{
				String SQL = "SELECT * FROM " + SMTableapaccountsets.TableName;
				ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, getServletContext(), sDBID, "MySQL", this.toString() + " - User: " + sUserID
				+ " - " + sUserFullName		
						);

				out.println ("<SELECT NAME=\"" + PARAM_ACCOUNT_SET + "\">" );
				out.println ("<OPTION VALUE=\"" + PARAM_ACCOOUNT_SET_ALL_ACCOUNT_SETS + "\">");
				out.println (PARAM_ACCOOUNT_SET_ALL_ACCOUNT_SETS);
				while (rs.next()){
					if (rs.getString(SMTableapaccountsets.sacctsetname).compareToIgnoreCase(sAccountSet) == 0){
						out.println ("<OPTION SELECTED=yes VALUE=\"" + rs.getString(SMTableapaccountsets.sacctsetname) + "\">");
					}else{
						out.println ("<OPTION VALUE=\"" + rs.getString(SMTableapaccountsets.sacctsetname) + "\">");
					}
					out.println (rs.getString(SMTableapaccountsets.sacctsetname) + " - " + rs.getString(SMTableapaccountsets.sdescription));
				}
				rs.close();
				//End the drop down list:
				out.println ("</SELECT>");

			}catch (SQLException e){
				out.println("Error loading account sets - " + e.getMessage());
			}
			out.println ("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;");

			String sSortBy = clsManageRequestParameters.get_Request_Parameter(PARAM_SORT_BY, request);
			out.println("<B>Sort by:</B>&nbsp;");
			out.println ("<SELECT NAME=\"" + PARAM_SORT_BY + "\">" );
			if (sSortBy.compareToIgnoreCase(PARAM_SORT_BY_NAME) == 0){
				out.println ("<OPTION VALUE=\"" + PARAM_SORT_BY_NAME + "\">" + PARAM_SORT_BY_NAME);
				out.println ("<OPTION SELECTED=yes VALUE=\"" + PARAM_SORT_BY_NAME + "\">" + PARAM_SORT_BY_NAME);
			}else{
				out.println ("<OPTION SELECTED=yes VALUE=\"" + PARAM_SORT_BY_ACCOUNT + "\">" + PARAM_SORT_BY_ACCOUNT);
				out.println ("<OPTION VALUE=\"" + PARAM_SORT_BY_ACCOUNT + "\">" + PARAM_SORT_BY_ACCOUNT);
			}
			out.println ("</SELECT>");
			out.println("</TD>\n");
			out.println("  </TR>\n");
			out.println("</TABLE>\n"
				+ "</TD>\n"
				+ "  </TR>");

			out.println("  <TR>\n"
				+ "    <TD ALIGN=CENTER WIDTH=100%>\n");
			out.println("<TABLE BORDER=0 WIDTH=100%>\n");
			
			boolean bPrintVendorsWithAZeroBalance = false;
			if (clsManageRequestParameters.get_Request_Parameter(PARAM_PRINT_VENDORS_WITH_A_ZERO_BALANCE,request).compareToIgnoreCase("Y") == 0){
				bPrintVendorsWithAZeroBalance = true;
			}

			boolean bIncludeAppliedDetails = true;
			if (clsManageRequestParameters.get_Request_Parameter(PARAM_INCLUDE_APPLIED_DETAILS,request).compareToIgnoreCase("N") == 0){
				bIncludeAppliedDetails = false;
			}
			boolean bIncludePullyPaidTransactions = false;
			if (clsManageRequestParameters.get_Request_Parameter(PARAM_INCLUDE_FULLY_PAID_TRANSACTIONS,request).compareToIgnoreCase("Y") == 0){
				bIncludePullyPaidTransactions = true;
			}
			
			//on hold vendors
			boolean bIncludeInactiveVendors = false;
			if (clsManageRequestParameters.get_Request_Parameter(PARAM_INCLUDE_INACTIVE_VENDORS,request).compareToIgnoreCase("Y") == 0){
				bIncludeInactiveVendors = true;
			}
			
			//on hold transactions
			boolean bIncludeTransactionsOnHold = false;
			if (clsManageRequestParameters.get_Request_Parameter(PARAM_INCLUDE_TRANSACTIONS_ON_HOLD,request).compareToIgnoreCase("Y") == 0){
				bIncludeTransactionsOnHold = true;
			}

			//sort by transaction type
			boolean bSortDetailByTransactionType = false;
			if (clsManageRequestParameters.get_Request_Parameter(PARAM_SORT_DETAIL_BY_TRANSACTION_TYPE,request).compareToIgnoreCase("Y") == 0){
				bSortDetailByTransactionType = true;
			}

			//select transaction types
			
			out.println("  <TR>\n" 
				+ "    <TD ALIGN=LEFT WIDTH=55%>" 
				+ clsCreateHTMLFormFields.TDCheckBox(PARAM_PRINT_VENDORS_WITH_A_ZERO_BALANCE, bPrintVendorsWithAZeroBalance, "Print vendors with a zero balance") 
				+ clsCreateHTMLFormFields.TDCheckBox(PARAM_INCLUDE_APPLIED_DETAILS, bIncludeAppliedDetails, "Include applied details") //default to true
				+ clsCreateHTMLFormFields.TDCheckBox(PARAM_INCLUDE_FULLY_PAID_TRANSACTIONS, bIncludePullyPaidTransactions, "Include fully paid transactions")
				+ clsCreateHTMLFormFields.TDCheckBox(PARAM_INCLUDE_INACTIVE_VENDORS, bIncludeInactiveVendors, "Include inactive vendors")
				+ clsCreateHTMLFormFields.TDCheckBox(PARAM_INCLUDE_TRANSACTIONS_ON_HOLD, bIncludeTransactionsOnHold, "Include transactions on hold")
				+ clsCreateHTMLFormFields.TDCheckBox(PARAM_SORT_DETAIL_BY_TRANSACTION_TYPE, bSortDetailByTransactionType, "Sort detail by transaction type")
				+ "</TD>\n" 
				+ "  </TR>\n"
			);
			out.println("</TABLE>\n"
				+ "    </TD>\n"
				+ "  </TR>\n");

			out.println("  <TR>\n"
				+ "    <TD ALIGN=CENTER WIDTH=100%>\n");
			out.println("<TABLE BORDER=0 WIDTH=100%>\n");
			boolean bDownLoadToHTML = (request.getParameter(PARAM_DOWNLOAD_TO_HTML) != null);
			out.println("  <TR>" 
				+ "    <TD ALIGN=LEFT WIDTH=55%>" 
				+ clsCreateHTMLFormFields.TDCheckBox(PARAM_DOWNLOAD_TO_HTML, bDownLoadToHTML, "Download to HTML file") 
				+ "</TD>\n" 
				+ "  </TR>\n"
			);
			out.println("</TABLE>\n");
			
			out.println("</TABLE>\n");
			out.println("<INPUT TYPE=\"SUBMIT\" VALUE=\"----View----\">");
			out.println("</FORM>");

		} catch (SQLException ex) {
			// handle any errors
			out.println("<B>Error: " + ex.getMessage() + "</B>");
		}

		out.println("</BODY></HTML>");
	}

	public void doGet(HttpServletRequest request,
			HttpServletResponse response)
	throws ServletException, IOException {

		doPost(request, response);
	}
}
