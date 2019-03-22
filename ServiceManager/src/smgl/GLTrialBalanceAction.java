package smgl;

import java.io.IOException;
import java.io.PrintWriter;
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
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsDateAndTimeConversions;
import ServletUtilities.clsManageRequestParameters;
import ServletUtilities.clsValidateFormFields;
import smcontrolpanel.SMAuthenticate;
import smcontrolpanel.SMSystemFunctions;
import smcontrolpanel.SMUtilities;

public class GLTrialBalanceAction extends HttpServlet {

	private static final long serialVersionUID = 1L;

	private static SimpleDateFormat USDateformatter = new SimpleDateFormat("MM-dd-yyyy hh:mm:ss a EEE");

	private long lStartingTime = 0;

	public void doGet(HttpServletRequest request,
			HttpServletResponse response)
	throws ServletException, IOException {

		response.setContentType("text/html");
		PrintWriter out = response.getWriter();
	    if (!SMAuthenticate.authenticateSMCPCredentials(request, response, getServletContext(), SMSystemFunctions.GLTrialBalance)){
	    	return;
	    }
		//Get the session info:
		HttpSession CurrentSession = request.getSession(true);
		String sDBID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_DATABASE_ID);
		String sUserID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERID);
		String sWarning = "";
		

		//Get parameters here:
		//sCallingClass will look like: smar.ARAgedTrialBalanceReport
		String sCallingClass = clsManageRequestParameters.get_Request_Parameter("CallingClass", request);
		String sEndingAccount = request.getParameter(GLTrialBalanceSelect.PARAM_ENDING_ACCOUNT);
		String sStartingAccount = request.getParameter(GLTrialBalanceSelect.PARAM_STARTING_ACCOUNT);
		String sEndingAccountGroup = request.getParameter(GLTrialBalanceSelect.PARAM_ENDING_ACCOUNT_GROUP);
		String sStartingAccountGroup = request.getParameter(GLTrialBalanceSelect.PARAM_STARTING_ACCOUNT_GROUP);
		String sFiscalPeriod = request.getParameter(GLTrialBalanceSelect.PARAM_FISCAL_PERIOD_SELECTION);
		boolean bIncludeAccountsWithNoActivity = request.getParameter(GLTrialBalanceSelect.PARAM_PROCESS_FOR_NO_ACTIVITY) != null;
		String sReportType = request.getParameter(GLTrialBalanceSelect.PARAM_REPORT_TYPE);
		boolean bDownloadAsHTML = (request.getParameter(GLTrialBalanceSelect.PARAM_DOWNLOAD_TO_HTML) != null);

		//Get the starting and ending segment values:
		String sStartingSegmentBase = request.getParameter(GLTrialBalanceSelect.PARAM_ENDING_SEGMENT_BASE);
		String sEndingSegmentBase = request.getParameter(GLTrialBalanceSelect.PARAM_STARTING_SEGMENT_BASE);

		
		
		/*******************************************************/
		
		String sParamString = "";
		sParamString += "*CallingClass=" + sCallingClass;
/*
		
		sParamString += "*" + APAgedPayablesSelect.PARAM_AS_OF_DATE + "=" + sAgeAsOf;
		sParamString += "*" + APAgedPayablesSelect.PARAM_CUT_OFF_BY + "=" + sCutoffBy;
		sParamString += "*" + APAgedPayablesSelect.PARAM_CUT_OFF_DATE + "=" + sCutOffDate;
		sParamString += "*" + APAgedPayablesSelect.PARAM_STARTING_VENDOR + "=" + sStartingVendor;
		sParamString += "*" + APAgedPayablesSelect.PARAM_ENDING_VENDOR + "=" + sEndingVendor;
		sParamString += "*" + APAgedPayablesSelect.PARAM_ACCOUNT_SET + "=" + sAccountSet;
		sParamString += "*" + APAgedPayablesSelect.PARAM_SORT_BY + "=" + sSortBy;
		sParamString += "*" + APAgedPayablesSelect.PARAM_PRINT_TRANSACTION_IN_DETAIL_OR_SUMMARY + "=" + sPrintTransactionsInDetailOrSummary;
		if (bDownloadAsHTML){
			sParamString += "*" + APAgedPayablesSelect.PARAM_DOWNLOAD_TO_HTML + "=" + "Y";
		}
		
		sParamString += "*" + APAgedPayablesSelect.PARAM_AGING_CATEGORY_FIRST + "=" + s1stAgingCategory;
		sParamString += "*" + APAgedPayablesSelect.PARAM_AGING_CATEGORY_SECOND + "=" + s2ndAgingCategory;
		sParamString += "*" + APAgedPayablesSelect.PARAM_AGING_CATEGORY_THIRD + "=" + s3rdAgingCategory;
		
		if (bPrintVendorswithaZeroBalance){
			sParamString += "*" + APAgedPayablesSelect.PARAM_PRINT_VENDORS_WITH_A_ZERO_BALANCE + "=" + "Y";
		}
		if (bIncludeAppliedDetails){
			sParamString += "*" + APAgedPayablesSelect.PARAM_INCLUDE_APPLIED_DETAILS + "=" + "Y";
		}
		if (bIncludeFullyPaidTransactions){
			sParamString += "*" + APAgedPayablesSelect.PARAM_INCLUDE_FULLY_PAID_TRANSACTIONS + "=" + "Y";
		}
		if (bSortByTransactionType){
			sParamString += "*" + APAgedPayablesSelect.PARAM_SORT_DETAIL_BY_TRANSACTION_TYPE + "=" + "Y";
		}
		if (bIncludeInactiveVendors){
			sParamString += "*" + APAgedPayablesSelect.PARAM_INCLUDE_INACTIVE_VENDORS + "=" + "Y";
		}
		if (bIncludeTransactionsOnHold){
			sParamString += "*" + APAgedPayablesSelect.PARAM_INCLUDE_TRANSACTIONS_ON_HOLD + "=" + "Y";
		}
		
		sParamString += "*" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID;
		
		//Customized title
		String sReportTitle = "GL Trial Balance";
		
		//If the user chose to download it:
		if (bDownloadAsHTML){
			String disposition = "attachment; fileName= " + " AP AGED PAYABLES " + clsDateAndTimeConversions.now("MM-dd-yyyy hh:mm") + ".html";
			response.setHeader("Content-Disposition", disposition);
		}
		out.println("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.0 " 
			+ "Transitional//EN\">\n" 
			+ "<HTML>\n" 
			+ "  <HEAD>\n"
			+ "    <TITLE>" + sReportTitle + " - " + (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_COMPANYNAME) + "</TITLE>\n"
			+ "  </HEAD>\n"
			+ "<BR>" 
			+ "  <BODY BGCOLOR=\"#FFFFFF\">\n" 
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
			+ "<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smgl.GLMainMenu?" 
			+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
			+ "\">Return to General Ledger Main Menu</A></TD>\n"
			+ "  </TR>\n"
			+ "</TABLE>"
		);
		
		out.println(SMUtilities.getMasterStyleSheetLink());
		out.println("<BR>\n");
		out.println("<TABLE BORDER=0>\n");
		
		String s = "";
		
		s += "  <TR>\n"
			+ "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\" >"
			+ "Age as of:&nbsp;"
			+ "    </TD>\n"
			+ "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\" >"
			+ "<B>" + sAgeAsOf + "</B>"
			+ "    </TD>\n"
			+ "  </TR>\n"
		;
			
		s += "  <TR>\n"
			+ "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\" >"
			+ "Cut off by:&nbsp;"
			+ "    </TD>\n"
			+ "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\" >"
			+ "<B>" + sCutoffBy + "</B>"
			+ "    </TD>\n"
			+ "  </TR>\n"
		;
		
		s += "  <TR>\n"
			+ "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\" >"
			+ "Cut off date:&nbsp;"
			+ "    </TD>\n"
			+ "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\" >"
			+ "<B>" + sCutOffDate + "</B>"
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
			+ "For AP Account set:&nbsp;"
			+ "    </TD>\n"
			+ "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\" >"
			+ "<B>" + sAccountSet + "</B>"
			+ "    </TD>\n"
			+ "  </TR>\n"
		;
		
		s += "  <TR>\n"
			+ "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\" >"
			+ "Sort by:&nbsp;"
			+ "    </TD>\n"
			+ "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\" >"
			+ "<B>" + sSortBy + "</B>"
			+ "    </TD>\n"
			+ "  </TR>\n"
		;
		
		s += "  <TR>\n"
			+ "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\" >"
			+ "1st aging category:&nbsp;"
			+ "    </TD>\n"
			+ "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\" >"
			+ "<B>" + s1stAgingCategory + " days</B>"
			+ "    </TD>\n"
			+ "  </TR>\n"
		;
		
		s += "  <TR>\n"
			+ "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\" >"
			+ "2nd aging category:&nbsp;"
			+ "    </TD>\n"
			+ "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\" >"
			+ "<B>" + s2ndAgingCategory + " days</B>"
			+ "    </TD>\n"
			+ "  </TR>\n"
		;
		
		s += "  <TR>\n"
			+ "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\" >"
			+ "3rd aging category:&nbsp;"
			+ "    </TD>\n"
			+ "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\" >"
			+ "<B>" + s3rdAgingCategory + " days</B>"
			+ "    </TD>\n"
			+ "  </TR>\n"
		;	
		
		s += "  <TR>\n"
			+ "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\" >"
			+ "Print transactions in detail or summary:&nbsp;"
			+ "    </TD>\n"
			+ "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\" >"
			+ "<B>" + sPrintTransactionsInDetailOrSummary + "</B>"
			+ "    </TD>\n"
			+ "  </TR>\n"
		;
			
		s += "  <TR>\n"
			+ "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\" >"
			+ "Include vendors with a zero balance:&nbsp;"
			+ "    </TD>\n"
			+ "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\" >"
			+ "<B>" + (bPrintVendorswithaZeroBalance ? "Y" : "N") + "</B>"
			+ "    </TD>\n"
			+ "  </TR>\n"
		;
		
		s += "  <TR>\n"
			+ "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\" >"
			+ "Include applied details:&nbsp;"
			+ "    </TD>\n"
			+ "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\" >"
			+ "<B>" + (bIncludeAppliedDetails ? "Y" : "N") + "</B>"
			+ "    </TD>\n"
			+ "  </TR>\n"
		;
		
		s += "  <TR>\n"
			+ "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\" >"
			+ "Include fully paid transactions:&nbsp;"
			+ "    </TD>\n"
			+ "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\" >"
			+ "<B>" + (bIncludeFullyPaidTransactions ? "Y" : "N") + "</B>"
			+ "    </TD>\n"
			+ "  </TR>\n"
		;
		
		s += "  <TR>\n"
			+ "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\" >"
			+ "Include inactive vendors:&nbsp;"
			+ "    </TD>\n"
			+ "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\" >"
			+ "<B>" + (bIncludeInactiveVendors ? "Y" : "N") + "</B>"
			+ "    </TD>\n"
			+ "  </TR>\n"
		;

		s += "  <TR>\n"
				+ "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\" >"
				+ "Include transactions on hold:&nbsp;"
				+ "    </TD>\n"
				+ "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\" >"
				+ "<B>" + (bIncludeTransactionsOnHold ? "Y" : "N") + "</B>"
				+ "    </TD>\n"
				+ "  </TR>\n"
			;
		
		s += "  <TR>\n"
			+ "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\" >"
			+ "Sort transactions by type:&nbsp;"
			+ "    </TD>\n"
			+ "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\" >"
			+ "<B>" + (bSortByTransactionType ? "Y" : "N") + "</B>"
			+ "    </TD>\n"
			+ "  </TR>\n"
		;
		
		
		out.println(s);
		out.println("</TABLE>\n");
		out.println("<BR>\n");
		*/
		
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
		/*
		APAgedPayablesReport rpt = new APAgedPayablesReport(s1stAgingCategory, s2ndAgingCategory, s3rdAgingCategory);
		try {
			out.println(
				rpt.processReport(
					conn,
					sAgeAsOf, 
					sCutoffBy, 
					sCutOffDate, 
					sStartingVendor, 
					sEndingVendor, 
					sAccountSet, 
					sSortBy, 
					sPrintTransactionsInDetailOrSummary, 
					bPrintVendorswithaZeroBalance, 
					bIncludeAppliedDetails, 
					bIncludeFullyPaidTransactions, 
					bSortByTransactionType, 
					bIncludeInactiveVendors,
					bIncludeTransactionsOnHold,
					bDownloadAsHTML,
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
			clsDatabaseFunctions.freeConnection(getServletContext(), conn,"[1546998936]");
			response.sendRedirect(
				"" + SMUtilities.getURLLinkBase(getServletContext()) + "" + sCallingClass + "?"
				+ "Warning=" + e.getMessage()
				+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
				+ sParamString.replace("*", "&")
			);			
			return;
		}
		*/
		clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1546998937]");
		
		SMLogEntry log = new SMLogEntry(conn);
		log.writeEntry(sUserID ,SMLogEntry.LOG_OPERATION_APAGING, "REPORT", "AP Aged Payables", "[1495576478]");
		
		long lEndingTime = System.currentTimeMillis();
		out.println("<BR>Processing took " + (lEndingTime - lStartingTime)/1000L + " seconds.\n");
		out.println("  </BODY>\n"
			+ "    </HTML>\n");
		return;	
	}
}
