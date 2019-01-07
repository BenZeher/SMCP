package smar;

import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Connection;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import smcontrolpanel.SMAuthenticate;
import smcontrolpanel.SMSystemFunctions;
import smcontrolpanel.SMUtilities;
import SMClasses.SMLogEntry;
import SMDataDefinition.*;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsDateAndTimeConversions;
import ServletUtilities.clsManageBigDecimals;
import ServletUtilities.clsManageRequestParameters;

public class ARAgedTrialBalanceReportGenerate extends HttpServlet {

	private static final long serialVersionUID = 1L;

	private static SimpleDateFormat USDateformatter = new SimpleDateFormat("MM-dd-yyyy hh:mm:ss a EEE");

	private String sWarning = "";
	private String sCallingClass = "";
	private static String sDBID = "";
	private static String sUserID = "";
	private static String sUserFirstName = "";
	private static String sUserLastName = "";
	private static Connection conn;
	private long lStartingTime = 0;
	private long lTestTime = 0;
	private boolean bDebugMode = false;
	private String sTempTableName = "";

	public void doGet(HttpServletRequest request,
			HttpServletResponse response)
	throws ServletException, IOException {

		response.setContentType("text/html");
		PrintWriter out = response.getWriter();
	    if (!SMAuthenticate.authenticateSMCPCredentials(request, response, getServletContext(), SMSystemFunctions.ARAgingReport)){
	    	return;
	    }
		//Get the session info:
		HttpSession CurrentSession = request.getSession(true);
		sDBID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_DATABASE_ID);
		sUserID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERID);
		sUserFirstName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERFIRSTNAME);
		sUserLastName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERLASTNAME);

		//Get parameters here:
		//sCallingClass will look like: smar.ARAgedTrialBalanceReport
		sCallingClass = ARUtilities.get_Request_Parameter("CallingClass", request);
		String sAgeAsOf = request.getParameter("AsOfDate");
		String sCutOffDate = request.getParameter("CutOffDate");
		String sStartingCustomer = request.getParameter("StartingCustomer");
		String sEndingCustomer = request.getParameter("EndingCustomer");
		String sAccountSet = request.getParameter(ARAccountSet.ParamsAcctSetCode);
		String sSortBy = request.getParameter("SORTBY");
		String sRetainageFlag = ARUtilities.get_Request_Parameter("AgingType", request);
		boolean bDownloadAsHTML = (request.getParameter(ARAgedTrialBalanceReport.DOWNLOAD_TO_HTML) != null);
		int iPrintTransactionsIn = Integer.parseInt(request.getParameter("PrintTransactionIn").trim());
		int iCurrent = Integer.parseInt(request.getParameter("Current").trim());
		int i1st = Integer.parseInt(request.getParameter("1st").trim());
		int i2nd = Integer.parseInt(request.getParameter("2nd").trim());
		int i3rd = Integer.parseInt(request.getParameter("3rd").trim());
		boolean bIncludeOnlyCustomerOverTheirCreditLimits = false;
		if (request.getParameter("IOCOTCL") != null){
			bIncludeOnlyCustomerOverTheirCreditLimits = true;
		}
		boolean bPrintCustomerswithaZeroBalance = false;
		if (request.getParameter("PCWAZB") != null){
			bPrintCustomerswithaZeroBalance = true;
		}
		boolean bIncludePrePayments = false;
		if (request.getParameter("IP") != null){
			bIncludePrePayments = true;
		}
		boolean bIncludeAppliedDetails = false;
		if (request.getParameter("IAD") != null){
			bIncludeAppliedDetails = true;
		}
		boolean bIncludePaidTransactions = false;
		if (request.getParameter("IPT") != null){
			bIncludePaidTransactions = true;
		}
		/*******************************************************/
		String sParamString = "";
		if (sCallingClass.compareToIgnoreCase("") != 0){
			sParamString += "*CallingClass=" + sCallingClass;
		}
		if (sAgeAsOf.compareToIgnoreCase("") != 0){
			sParamString += "*AsOfDate=" + sAgeAsOf;
		}
		sParamString += "*" + "CutoffBy=" + clsManageRequestParameters.get_Request_Parameter("CutoffBy", request);
		sParamString += "*" + "CutOffDate=" + sCutOffDate;
		sParamString += "*" + ARAccountSet.ParamsAcctSetCode + "=" + sAccountSet;
		sParamString += "*" + "SORTBY=" + sSortBy;
		sParamString += "*" + "AgingType=" + sRetainageFlag;
		sParamString += "*" + "PrintTransactionIn=" + clsManageRequestParameters.get_Request_Parameter("PrintTransactionIn", request);
		sParamString += "*" + "Current=" + clsManageRequestParameters.get_Request_Parameter("Current", request);
		sParamString += "*" + "1st=" + clsManageRequestParameters.get_Request_Parameter("1st", request);
		sParamString += "*" + "2nd=" + clsManageRequestParameters.get_Request_Parameter("2nd",request);
		sParamString += "*" + "3rd=" + clsManageRequestParameters.get_Request_Parameter("3rd",request);
		if (request.getParameter("IOCOTCL") != null){
			sParamString += "*" + "IOCOTCL=Y";
		}else{
			sParamString += "*" + "IOCOTCL=N";
		}
		if (request.getParameter("PCWAZB") != null){
			sParamString += "*" + "PCWAZB=Y";
		}else{
			sParamString += "*" + "PCWAZB=N";
		}
		if (request.getParameter("IP") != null){
			sParamString += "*" + "IP=Y";
		}else{
			sParamString += "*" + "IP=N";
		}
		if (request.getParameter("IAD") != null){
			sParamString += "*" + "IAD=Y";
		}else{
			sParamString += "*" + "IAD=N";
		}
		if (request.getParameter("IPT") != null){
			sParamString += "*" + "IPT=Y";
		}else{
			sParamString += "*" + "IPT=N";
		}
		
		//Special cases - if this class was called by a finder for the 'starting customer' field:
		if (request.getParameter(ARAgedTrialBalanceReport.FIND_STARTING_CUSTOMER_BUTTON_NAME) != null){
			//Then call the finder to search for customers:
			String sRedirectString = 
				"" + SMUtilities.getURLLinkBase(getServletContext()) + "smar.ObjectFinder"
			+ "?" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
			+ "&ObjectName=Customer"
			+ "&ResultClass=FinderResults"
			+ "&SearchingClass=smar.ARAgedTrialBalanceReport"
			+ "&ReturnField=" + "StartingCustomer"
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
			+ "*" + "EndingCustomer=" + clsManageRequestParameters.get_Request_Parameter("EndingCustomer", request)
			+ sParamString
			;
			response.sendRedirect(sRedirectString);
			return;
		}

		//Special cases - if this class was called by a finder for the 'starting customer' field:
		if (request.getParameter(ARAgedTrialBalanceReport.FIND_ENDING_CUSTOMER_BUTTON_NAME) != null){
			//Then call the finder to search for customers:
			String sRedirectString = 
				"" + SMUtilities.getURLLinkBase(getServletContext()) + "smar.ObjectFinder"
			+ "?" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
			+ "&ObjectName=Customer"
			+ "&ResultClass=FinderResults"
			+ "&SearchingClass=smar.ARAgedTrialBalanceReport"
			+ "&ReturnField=" + "EndingCustomer"
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
			+ "*" + "StartingCustomer=" + clsManageRequestParameters.get_Request_Parameter("StartingCustomer", request)
			+ sParamString
			;
			response.sendRedirect(sRedirectString);
			return;
		}
		//End special cases:
		/*******************************************************/
		
		lStartingTime = System.currentTimeMillis();

		java.sql.Date datAgeAsOf;
		java.sql.Date datCutOffDate;
		String SQL = "";
		
		try {
			datAgeAsOf = clsDateAndTimeConversions.StringTojavaSQLDate("MM/dd/yyyy", sAgeAsOf);
		} catch (ParseException e) {
			sWarning = "Invalid 'Age as of' date.";
			response.sendRedirect(
					"" + SMUtilities.getURLLinkBase(getServletContext()) + "" + sCallingClass + "?"
					+ "Warning=" + sWarning
					+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
			);			
			return;
		}
		try {
			datCutOffDate = clsDateAndTimeConversions.StringTojavaSQLDate("MM/dd/yyyy", sCutOffDate);
		} catch (ParseException e) {
			sWarning = "Invalid cut off date.";
			response.sendRedirect(
					"" + SMUtilities.getURLLinkBase(getServletContext()) + "" + sCallingClass + "?"
					+ "Warning=" + sWarning
					+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
			);			
			return;
		}
		
		try {
			//0 for non-retainage, 1 for retainage type
			//Customized title
			String sReportTitle = "A/R Aged Trial Balance by Document Date";
			String sRetainageTypeDesc = "";
			if(sRetainageFlag.compareToIgnoreCase("0") == 0){
				sRetainageTypeDesc = "NON-Retainage Transactions Only";
			}else{
				sRetainageTypeDesc = "Retainage Transactions Only";
			}
			String sSortedByLabel = "account number";
			if (sSortBy.compareToIgnoreCase("NAME") == 0){
				sSortedByLabel = "account name";
			}

			String sAccountSetLabel = ", for <B>ALL</B> account sets";
			if (sAccountSet.compareToIgnoreCase("") != 0){
				sAccountSetLabel = ", for account set <B>" + sAccountSet + "</B>";
			}
			String sCriteria = "Aged as of <B>" + sAgeAsOf + "</B>"
			+ ", cut off as of <B>" + sCutOffDate + "</B>"
			+ ", starting with customer <B>" + sStartingCustomer + "</B>"
			+ ", ending with customer <B>" + sEndingCustomer + "</B>"
			+ ", sorted by " + sSortedByLabel
			+ sAccountSetLabel
			+ " for <B>" + sRetainageTypeDesc  + "</B>"
			;

			//If the user chose to download it:
			if (bDownloadAsHTML){
				String disposition = "attachment; fileName= " + "AGING " + clsDateAndTimeConversions.now("MM-dd-yyyy hh:mm") + ".html";
				response.setHeader("Content-Disposition", disposition);
			}
			out.println("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.0 " +
					"Transitional//EN\">" +
					"<HTML>" +
					"<HEAD><TITLE>" + sReportTitle + " - " + (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_COMPANYNAME) + "</TITLE></HEAD>\n<BR>" + 
					"<BODY BGCOLOR=\"#FFFFFF\">" +
					"<TABLE BORDER=0 WIDTH=100%>" +
					"<TR><TD ALIGN=LEFT WIDTH=45%><FONT SIZE=2>" 
					+ USDateformatter.format((new Timestamp(System.currentTimeMillis()))) 
					+ "</FONT></TD><TD ALIGN=CENTER WIDTH=55%><FONT SIZE=2><B>" + CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_COMPANYNAME).toString() + "</B></FONT></TD></TR>" +
					"<TR><TD VALIGN=BOTTOM COLSPAN=2><FONT SIZE=2><B>" + sReportTitle + "</B></FONT></TD></TR>" +

					"<TR><TD COLSPAN=2><FONT SIZE=2>" + sCriteria + "</FONT></TD></TR>");
			
			out.println("<TR><TD COLSPan=2><FONT SIZE=2>Include ONLY customers over their credit limits (");
			if (bIncludeOnlyCustomerOverTheirCreditLimits){
				out.println("<B>X</B>)</TD></TR>");
			}else{
				out.println("&nbsp;)</TD></TR>");
			}

			if (sRetainageFlag.compareToIgnoreCase("1") == 0){
				out.println("<TR><TD COLSPan=2><FONT SIZE=2>Include customers with a zero RETAINAGE balance (");
			}else{
				out.println("<TR><TD COLSPan=2><FONT SIZE=2>Include customers with a zero CURRENT balance (");
			}
			if (bPrintCustomerswithaZeroBalance){
				out.println("<B>X</B>)</TD></TR>");
			}else{
				out.println("&nbsp;)</TD></TR>");
			}

			out.println("<TR><TD COLSPan=2><FONT SIZE=2>Include prepayments (");
			if (bIncludePrePayments){
				out.println("<B>X</B>)</TD></TR>");
			}else{
				out.println("&nbsp;)</TD></TR>");
			}

			out.println("<TR><TD COLSPan=2><FONT SIZE=2>Include applied details (");
			if (bIncludeAppliedDetails){
				out.println("<B>X</B>)</TD></TR>");
			}else{
				out.println("&nbsp;)</TD></TR>");
			}

			out.println("<TR><TD COLSPan=2><FONT SIZE=2>Include paid transactions (");
			if (bIncludePaidTransactions){
				out.println("<B>X</B>)</TD></TR>");
			}else{
				out.println("&nbsp;)</TD></TR>");
			}

			out.println("<TD><A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMUserLogin?" 
				+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
				+ "\">Return to user login</A><BR>" +
					"<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smar.ARMainMenu?" 
				+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
				+ "\">Return to Accounts Receivable Main Menu</A></TD></TR></TABLE>");

			//Retrieve information
			conn = clsDatabaseFunctions.getConnection(
					getServletContext(), 
					sDBID, 
					"MySQL", 
					this.toString() 
					+ ".doGet - User: " 
					+ sUserID
					+ " - "
					+ sUserFirstName
					+ " "
					+ sUserLastName
			);
			if (conn == null){
				sWarning = "Could not open connection";
				response.sendRedirect(
						"" + SMUtilities.getURLLinkBase(getServletContext()) + "" + sCallingClass + "?"
						+ "Warning=" + sWarning
						+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
				);			
				return;
			}

			long lTempTableTime = System.currentTimeMillis();

			if(!createTemporaryTables(
					conn,
					sStartingCustomer, 
					sEndingCustomer,
					sAccountSet,
					sRetainageFlag,
					clsDateAndTimeConversions.utilDateToString(datAgeAsOf, "yyyy-MM-dd"),
					clsDateAndTimeConversions.utilDateToString(datCutOffDate, "yyyy-MM-dd"),
					Integer.toString(iCurrent),
					Integer.toString(i1st),
					Integer.toString(i2nd),
					Integer.toString(i3rd),
					bIncludePaidTransactions
			)){

				clsDatabaseFunctions.freeConnection(getServletContext(), conn);
				response.sendRedirect(
						"" + SMUtilities.getURLLinkBase(getServletContext()) + "" + sCallingClass + "?"
						+ "Warning=" + sWarning
						+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
				);			
				return;
			}

			if (bDebugMode){
				System.out.println(
					"In " + this.toString() + " - " + "Creating temp tables took " 
						+ (System.currentTimeMillis() - lTempTableTime) + " milliseconds overall.");
			}
			
			//Establish needed permissions:
			boolean bAllowInvoiceView = 
				SMSystemFunctions.isFunctionPermitted(
						SMSystemFunctions.SMPrintInvoice, 
						sUserID, 
						conn,
						(String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_LICENSE_MODULE_LEVEL)
				);
			boolean bAllowCustomerView = 
				SMSystemFunctions.isFunctionPermitted(
						SMSystemFunctions.ARDisplayCustomerInformation, 
						sUserID, 
						conn,
						(String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_LICENSE_MODULE_LEVEL)
				);
			boolean bAllowOrderView = 
				SMSystemFunctions.isFunctionPermitted(
						SMSystemFunctions.SMViewOrderInformation, 
						sUserID, 
						conn,
						(String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_LICENSE_MODULE_LEVEL)
				);

			//TODO
			SQL = 
				"SELECT " + sTempTableName + ".*, "
				+ SMTablearcustomerstatistics.TableName + "." + SMTablearcustomerstatistics.sCurrentBalance 
				+ " FROM " + sTempTableName
				+ " LEFT JOIN " + SMTablearcustomerstatistics.TableName
				+ " ON " + sTempTableName + ".scustomer = " + SMTablearcustomerstatistics.TableName
				+ "." + SMTablearcustomerstatistics.sCustomerNumber
				+ " WHERE (";
			//Need this line in case there are no ANDs in the WHERE clause
			SQL = SQL + "(1=1)";
			if(bIncludeOnlyCustomerOverTheirCreditLimits){
				SQL = SQL + " AND (" 
				+ SMTablearcustomerstatistics.TableName + "." + SMTablearcustomerstatistics.sCurrentBalance 
				+ " > dcreditlimit)";
			}
			if(!bPrintCustomerswithaZeroBalance){
				if (sRetainageFlag.compareToIgnoreCase("1") == 0){
					SQL = SQL + " AND (dretainagebalance != 0.00)";
				}else{
					SQL = SQL + " AND (" 
					+ SMTablearcustomerstatistics.TableName + "." + SMTablearcustomerstatistics.sCurrentBalance 
					+ " != 0.00)";
				}
			}
			if(!bIncludePrePayments){
				SQL = SQL + " AND (idoctype != " + ARDocumentTypes.PREPAYMENT + ")";
			}
			if(!bIncludeAppliedDetails){
				//If it's either a CONTROL line Or it's a DISTRIBUTION line, but it's not applied:
				SQL = SQL + " AND ((ssource = 'CONTROL') OR (lappliedto < 1))";
			}
			if(!bIncludePaidTransactions){
				SQL = SQL + " AND (dapplytodoccurrentamt != 0.00)";
			}

			SQL = SQL + ")";
			if (sSortBy.compareToIgnoreCase("NAME") == 0){
				SQL = SQL + " ORDER BY scustomername, scustomer, lappliedto, ssource, datdocdate";
			}else{
				SQL = SQL + " ORDER BY scustomer, lappliedto, ssource, datdocdate";
			}

			lTestTime = System.currentTimeMillis();
			//System.out.println("[1500489204] SQL 7 = " + SQL);
			ResultSet rsBalanceList = clsDatabaseFunctions.openResultSet(
					SQL, 
					conn);
			if (bDebugMode){
				System.out.println(
						"[1500480918] In " + this.toString() + " - " + "Opening rsBalance resultset took " 
						+ (System.currentTimeMillis() - lTestTime) + " milliseconds.");
			}
			
			//System.out.println("[1471883682] - SQL = " + SQL);
			//variables for customer total calculations
			BigDecimal dTotalCurrent = new BigDecimal(0);
			BigDecimal dTotal1st = new BigDecimal(0);
			BigDecimal dTotal2nd = new BigDecimal(0);
			BigDecimal dTotal3rd = new BigDecimal(0);
			BigDecimal dTotalOver = new BigDecimal(0);

			//variables for grand total calculations
			BigDecimal dGrandTotalCurrent = new BigDecimal(0);
			BigDecimal dGrandTotal1st = new BigDecimal(0);
			BigDecimal dGrandTotal2nd = new BigDecimal(0);
			BigDecimal dGrandTotal3rd = new BigDecimal(0);
			BigDecimal dGrandTotalOver = new BigDecimal(0);
			
			BigDecimal bdRunningTotal = new BigDecimal(0);

			int iCustomersPrinted = 0;
			//BigDecimal dGrandTotal = new BigDecimal(0);

			String sCurrentCustomer = "";
			String sDocAppliedTo = "";
			String sCurrentDocAppliedTo = "";
			int iLinesPrinted = 0;
			
			//Set the table style:
			out.println(setTableStyleString());
			
			//Set the special headings to be used on summary view only:
			String sSummaryCurrentHeading = "";
			String sSummary1stHeading = "";
			String sSummary2ndHeading = "";
			String sSummary3rdHeading = "";
			String sSummaryOverHeading = "";
			String sSummaryTotalHeading = "";
			if (iPrintTransactionsIn != 0){
				sSummaryCurrentHeading = "Current:&nbsp;";
				sSummary1stHeading = (iCurrent + 1) + "&nbsp;to&nbsp;" + (i1st) + "&nbsp;Days:&nbsp;";
				sSummary2ndHeading = (i1st + 1) + "&nbsp;to&nbsp;" + i2nd + "&nbsp;Days:&nbsp;";
				sSummary3rdHeading = (i2nd + 1) + "&nbsp;to&nbsp;" + i3rd + "&nbsp;Days:&nbsp;";
				sSummaryOverHeading = "Over&nbsp;" + i3rd + "&nbsp;Days:&nbsp;";
				sSummaryTotalHeading = "Total:&nbsp;";
			}
			
			while (rsBalanceList.next()){
				//If it's the beginning of a section, print the headings:
				if (iLinesPrinted == 50){
					out.println("</TABLE>");
					iLinesPrinted = 0;
				}
				if (iLinesPrinted == 0){
					//print out the column headers.
					//out.println("<TABLE BORDER=0 WIDTH=100%>");
					out.println("<table class=\"main\">");
					if (iPrintTransactionsIn == 0){
						out.println("<TR>" + 
								"<TD ALIGN=LEFT VALIGN=BOTTOM WIDTH=8%><B><FONT SIZE=2>Applied to</FONT></B></TD>" +
								"<TD ALIGN=LEFT VALIGN=BOTTOM WIDTH=3%><B><FONT SIZE=2>Type</FONT></B></TD>" +
								"<TD ALIGN=LEFT VALIGN=BOTTOM WIDTH=8%><B><FONT SIZE=2>Doc #</FONT></B></TD>" +
								"<TD ALIGN=LEFT VALIGN=BOTTOM WIDTH=7%><B><FONT SIZE=2>Doc ID</FONT></B></TD>" +
								"<TD ALIGN=LEFT VALIGN=BOTTOM WIDTH=7%><B><FONT SIZE=2>Doc. Date</FONT></B></TD>" + 
								"<TD ALIGN=LEFT VALIGN=BOTTOM WIDTH=7%><B><FONT SIZE=2>Due Date</FONT></B></TD>" +
								"<TD ALIGN=LEFT VALIGN=BOTTOM WIDTH=8%><B><FONT SIZE=2>Order #</FONT></B></TD>" +
								"<TD ALIGN=RIGHT VALIGN=BOTTOM WIDTH=8%><B><FONT SIZE=2>Current</FONT></B></TD>" +
								"<TD ALIGN=RIGHT VALIGN=BOTTOM WIDTH=8%><B><FONT SIZE=2>" + (iCurrent + 1) + " to " + (i1st) + "<BR>Days</FONT></B></TD>" +
								"<TD ALIGN=RIGHT VALIGN=BOTTOM WIDTH=8%><B><FONT SIZE=2>" + (i1st + 1) + " to " + i2nd + "<BR>Days</FONT></B></TD>" +
								"<TD ALIGN=RIGHT VALIGN=BOTTOM WIDTH=8%><B><FONT SIZE=2>" + (i2nd + 1) + " to " + i3rd + "<BR>Days</FONT></B></TD>" +
								"<TD ALIGN=RIGHT VALIGN=BOTTOM WIDTH=8%><B><FONT SIZE=2>Over " + i3rd + "<BR>Days</FONT></B></TD>" +
								"<TD ALIGN=RIGHT VALIGN=BOTTOM WIDTH=9%><B><FONT SIZE=2>Total</FONT></B></TD>" +
								"<TD ALIGN=RIGHT VALIGN=BOTTOM WIDTH=8%><B><FONT SIZE=2>" +
								"<a href=\"#RUNNINGTOTAL\">Running<BR>Total*</a></FONT></B></TD>" +
								"</TR>" + 
						"<TR><TDCOLSPAN=10><HR></TD><TR>");
					}
				}

				//Print the header for any new customer:
				if (rsBalanceList.getString("scustomer").compareToIgnoreCase(sCurrentCustomer) != 0){
					//Print the footer, if the record is for a new customer:
					if (sCurrentCustomer.compareToIgnoreCase("") != 0){
						out.println("<TR>");
						out.println("<TD ALIGN=RIGHT colspan=\"7\"><B><FONT SIZE=2>Customer total:</FONT></B></TD>");
						out.println("<td class=\"r\">" + sSummaryCurrentHeading + "<B>" + clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(dTotalCurrent) + "</B></FONT></TD>");
						out.println("<td class=\"r\">" + sSummary1stHeading + "<B>" + clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(dTotal1st) + "</B></FONT></TD>");
						out.println("<td class=\"r\">" + sSummary2ndHeading + "<B>" + clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(dTotal2nd) + "</B></FONT></TD>");
						out.println("<td class=\"r\">" + sSummary3rdHeading + "<B>" + clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(dTotal3rd) + "</B></FONT></TD>");
						out.println("<td class=\"r\">" + sSummaryOverHeading + "<B>" + clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(dTotalOver) + "</B></FONT></TD>");
						out.println("<td class=\"r\">" + sSummaryTotalHeading + "<B>" + clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(dTotalCurrent.add(dTotal1st).add(dTotal2nd).add(dTotal3rd).add(dTotalOver)) + "</B></FONT></TD>");
						out.println("<td class=\"r\"><B>" + "&nbsp;" + "</B></FONT></TD>");
						out.println("</TR>");

						//Reset the customer totals:
						dTotalCurrent = BigDecimal.ZERO;
						dTotal1st = BigDecimal.ZERO;
						dTotal2nd = BigDecimal.ZERO;
						dTotal3rd = BigDecimal.ZERO;
						dTotalOver = BigDecimal.ZERO;

						iCustomersPrinted++;
					}

					//Print the customer header:
					out.println("<TR>");
					String sCustomerNumber = rsBalanceList.getString("scustomer");
					String sTerms = "N/A";
					ARCustomer cust = new ARCustomer(sCustomerNumber);
					if (cust.load(getServletContext(), sDBID)){
						sTerms = cust.getM_sTerms();
					}
					//Add a link to the customer info:
					String sCustomerInfoLink = sCustomerNumber;
					if (bAllowCustomerView){
						sCustomerInfoLink = 
							"<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smar.ARDisplayCustomerInformation?CustomerNumber=" 
							+ sCustomerNumber + "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID + "\">" 
							+ sCustomerNumber + "</A>"
							;
					}

					out.println("<TD><B><FONT SIZE=2>" + sCustomerInfoLink + "</FONT></B></TD>");
					out.println("<TD ALIGN=LEFT colspan=\"12\"><B><FONT SIZE=2>" 
							+ rsBalanceList.getString("scustomername") + "</B>"
							+ "  - Current balance: <B>" + clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(
								rsBalanceList.getBigDecimal(SMTablearcustomerstatistics.TableName + "." 
									+ SMTablearcustomerstatistics.sCurrentBalance)) + "</B>,"
							+ " Retainage balance: <B>" + clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(rsBalanceList.getBigDecimal("dretainagebalance")) + "</B>,"
							+ " Credit limit: <B>" + clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(rsBalanceList.getBigDecimal("dcreditlimit")) + "</B>,"
							+ " Terms: <B>" + sTerms + "</B>"
							+ "</FONT></B></TD>");
					//Reset:
					sCurrentCustomer = rsBalanceList.getString("scustomer");
				}
				if (iPrintTransactionsIn == 0){
					sDocAppliedTo = rsBalanceList.getString("sdocappliedto");
					if (sDocAppliedTo.compareToIgnoreCase(sCurrentDocAppliedTo) != 0){
						bdRunningTotal = BigDecimal.ZERO;
					}
					sCurrentDocAppliedTo = sDocAppliedTo;
					out.println("<TR>");
					out.println("<TD ALIGN=LEFT><FONT SIZE=2>&nbsp;&nbsp;" + sDocAppliedTo + "</FONT></TD>");
					int iDocType = rsBalanceList.getInt("idoctype");
					out.println("<TD ALIGN=LEFT><FONT SIZE=2>" + getDocumentTypeLabel(iDocType) + "</FONT></TD>");
					String sDocNumber = rsBalanceList.getString("sdocnumber");
					if(
							(iDocType == ARDocumentTypes.INVOICE)
							|| (iDocType == ARDocumentTypes.CREDIT)
					){
						//Qualify invoice link with permissions:
						String sViewInvoiceLink = sDocNumber;
						if (bAllowInvoiceView){
							sViewInvoiceLink =
								"<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "" 
								+ SMUtilities.lnViewInvoice(sDBID, sDocNumber )
								+ "\">"
								+ sDocNumber
								+ "</A>"
								;
						}
						out.println("<TD ALIGN=LEFT><FONT SIZE=2>" 
								+ sViewInvoiceLink
								+ "</FONT></TD>");
					}else{
						out.println("<TD ALIGN=LEFT><FONT SIZE=2>" + sDocNumber + "</FONT></TD>");
					}
					out.println("<TD ALIGN=LEFT><FONT SIZE=2>" + Long.toString(rsBalanceList.getLong("ldocid")) + "</FONT></TD>");
					out.println("<TD ALIGN=LEFT><FONT SIZE=2>" + clsDateAndTimeConversions.utilDateToString(rsBalanceList.getDate("datdocdate"),"MM/dd/yyyy") + "</FONT></TD>");
					out.println("<TD ALIGN=LEFT><FONT SIZE=2>" + clsDateAndTimeConversions.utilDateToString(rsBalanceList.getDate("datduedate"),"MM/dd/yyyy") + "</FONT></TD>");

					String sOrderNumber = rsBalanceList.getString("sordernumber");
					String sOrderNumberLink = sOrderNumber;
					//Qualify order link with permissions:
					if (bAllowOrderView){
						sOrderNumberLink = 
							"<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMDisplayOrderInformation?OrderNumber=" 
							+ sOrderNumber 
							+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
							+ "\">" + ARUtilities.Fill_In_Empty_String_For_HTML_Cell(sOrderNumber) + "</A>"
							;
					}
					out.println("<TD ALIGN=LEFT><FONT SIZE=2>"
							+ sOrderNumberLink 
							+ "</FONT></TD>");

					bdRunningTotal = bdRunningTotal.add(rsBalanceList.getBigDecimal("dagingcolumncurrent"));
					bdRunningTotal = bdRunningTotal.add(rsBalanceList.getBigDecimal("dagingcolumnfirst"));
					bdRunningTotal = bdRunningTotal.add(rsBalanceList.getBigDecimal("dagingcolumnsecond"));
					bdRunningTotal = bdRunningTotal.add(rsBalanceList.getBigDecimal("dagingcolumnthird"));
					bdRunningTotal = bdRunningTotal.add(rsBalanceList.getBigDecimal("dagingcolumnover"));
					//TJR
					out.println("<td class=\"r\">" + clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(rsBalanceList.getBigDecimal("dagingcolumncurrent")) + "</FONT></TD>");
					out.println("<td class=\"r\">" + clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(rsBalanceList.getBigDecimal("dagingcolumnfirst")) + "</FONT></TD>");
					out.println("<td class=\"r\">" + clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(rsBalanceList.getBigDecimal("dagingcolumnsecond")) + "</FONT></TD>");
					out.println("<td class=\"r\">" + clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(rsBalanceList.getBigDecimal("dagingcolumnthird")) + "</FONT></TD>");
					out.println("<td class=\"r\">" + clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(rsBalanceList.getBigDecimal("dagingcolumnover")) + "</FONT></TD>");
					out.println("<td class=\"r\">" + clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(rsBalanceList.getBigDecimal("doriginalamt")) + "</FONT></TD>");
					out.println("<td class=\"r\">" + clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(bdRunningTotal) + "</FONT></TD>");
					out.println("</TR>");
				}

				//Set the totals:
				dTotalCurrent = dTotalCurrent.add(rsBalanceList.getBigDecimal("dagingcolumncurrent"));
				dTotal1st = dTotal1st.add(rsBalanceList.getBigDecimal("dagingcolumnfirst"));
				dTotal2nd = dTotal2nd.add(rsBalanceList.getBigDecimal("dagingcolumnsecond"));
				dTotal3rd = dTotal3rd.add(rsBalanceList.getBigDecimal("dagingcolumnthird"));
				dTotalOver = dTotalOver.add(rsBalanceList.getBigDecimal("dagingcolumnover"));

				//Accumulate the grand totals:
				dGrandTotalCurrent = dGrandTotalCurrent.add(rsBalanceList.getBigDecimal("dagingcolumncurrent"));
				dGrandTotal1st = dGrandTotal1st.add(rsBalanceList.getBigDecimal("dagingcolumnfirst"));
				dGrandTotal2nd = dGrandTotal2nd.add(rsBalanceList.getBigDecimal("dagingcolumnsecond"));
				dGrandTotal3rd = dGrandTotal3rd.add(rsBalanceList.getBigDecimal("dagingcolumnthird"));
				dGrandTotalOver = dGrandTotalOver.add(rsBalanceList.getBigDecimal("dagingcolumnover"));

				iLinesPrinted ++;
			}
			rsBalanceList.close();

			//Print the footer for the last customer, if at least one customer was found:
			if (sCurrentCustomer.compareToIgnoreCase("") != 0){
				out.println("<TR>");
				out.println("<TD ALIGN=RIGHT colspan=\"7\"><B><FONT SIZE=2>Customer total:</FONT></B></TD>");
				out.println("<td class=\"r\">" + sSummaryCurrentHeading + "<B>" + clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(dTotalCurrent) + "</B></FONT></TD>");
				out.println("<td class=\"r\">" + sSummary1stHeading + "<B>" + clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(dTotal1st) + "</B></FONT></TD>");
				out.println("<td class=\"r\">" + sSummary2ndHeading + "<B>" + clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(dTotal2nd) + "</B></FONT></TD>");
				out.println("<td class=\"r\">" + sSummary3rdHeading + "<B>" + clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(dTotal3rd) + "</B></FONT></TD>");
				out.println("<td class=\"r\">" + sSummaryOverHeading + "<B>" + clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(dTotalOver) + "</B></FONT></TD>");
				out.println("<td class=\"r\">" + sSummaryTotalHeading + "<B>" + clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(dTotalCurrent.add(dTotal1st).add(dTotal2nd).add(dTotal3rd).add(dTotalOver)) + "</B></FONT></TD>");
				out.println("<td class=\"r\"><B>" + "&nbsp;" + "</B></FONT></TD>");
				out.println("</TR>");
				iCustomersPrinted++;
			}

			//Print the grand totals:
			out.println("<TD colspan=\"12\">&nbsp;</TD>");
			out.println("<TR>");
			out.println("<TD ALIGN=RIGHT colspan=\"7\"><B><FONT SIZE=2>Report totals:</FONT></B></TD>");
			out.println("<td class=\"r\"><B>" + clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(dGrandTotalCurrent) + "</B></FONT></TD>");
			out.println("<td class=\"r\"><B>" + clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(dGrandTotal1st) + "</B></FONT></TD>");
			out.println("<td class=\"r\"><B>" + clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(dGrandTotal2nd) + "</B></FONT></TD>");
			out.println("<td class=\"r\"><B>" + clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(dGrandTotal3rd) + "</B></FONT></TD>");
			out.println("<td class=\"r\"><B>" + clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(dGrandTotalOver) + "</B></FONT></TD>");
			BigDecimal bdGrandTotal = dGrandTotalCurrent.add(dGrandTotal1st).add(dGrandTotal2nd).add(dGrandTotal3rd).add(dGrandTotalOver);
			out.println("<td class=\"r\"><B>" + clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(bdGrandTotal) + "</B></FONT></TD>");
			out.println("<td class=\"r\"><B>" + "&nbsp;" + "</B></FONT></TD>");
			out.println("</TR>");

			//Percentages:
			BigDecimal bdOneHundred = new BigDecimal(100);
			out.println("<TR>");
			out.println("<TD ALIGN=RIGHT colspan=\"7\"><B><FONT SIZE=2>&nbsp;</FONT></B></TD>");
			if(bdGrandTotal.compareTo(BigDecimal.ZERO) != 0){
				out.println("<td class=\"r\"><B>" + clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(dGrandTotalCurrent.multiply(bdOneHundred).divide(bdGrandTotal, BigDecimal.ROUND_HALF_UP)) + "%</B></FONT></TD>");
				out.println("<td class=\"r\"><B>" + clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(dGrandTotal1st.multiply(bdOneHundred).divide(bdGrandTotal, BigDecimal.ROUND_HALF_UP)) + "%</B></FONT></TD>");
				out.println("<td class=\"r\"><B>" + clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(dGrandTotal2nd.multiply(bdOneHundred).divide(bdGrandTotal, BigDecimal.ROUND_HALF_UP)) + "%</B></FONT></TD>");
				out.println("<td class=\"r\"><B>" + clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(dGrandTotal3rd.multiply(bdOneHundred).divide(bdGrandTotal, BigDecimal.ROUND_HALF_UP)) + "%</B></FONT></TD>");
				out.println("<td class=\"r\"><B>" + clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(dGrandTotalOver.multiply(bdOneHundred).divide(bdGrandTotal, BigDecimal.ROUND_HALF_UP)) + "%</B></FONT></TD>");
				out.println("<td class=\"r\"><B>100.00%</B></FONT></TD>");
				out.println("<td class=\"r\"><B>&nbsp;</B></FONT></TD>");
			}else{
				out.println("<td class=\"r\"><B>0.00%</B></FONT></TD>");
				out.println("<td class=\"r\"><B>0.00%</B></FONT></TD>");
				out.println("<td class=\"r\"><B>0.00%</B></FONT></TD>");
				out.println("<td class=\"r\"><B>0.00%</B></FONT></TD>");
				out.println("<td class=\"r\"><B>0.00%</B></FONT></TD>");
				out.println("<td class=\"r\"><B>100.00%</B></FONT></TD>");
				out.println("<td class=\"r\"><B>&nbsp;</B></FONT></TD>");
			}
			out.println("</TR>");
			out.println("</TABLE>");

			//Print the legends:
			out.println("<TABLE BORDER=0 WIDTH=100%>");
			out.println("<TR>");
			for (int i = 0;i <= 9; i++){
				out.println("<TD><FONT SIZE=2><I>" + ARDocumentTypes.Get_Document_Type_Label(i) + " = " + getDocumentTypeLabel(i) + "</I></FONT></TD>");
			}
			out.println("</TR>");
			out.println("</TABLE>");
			out.println("<a name=\"RUNNINGTOTAL\"><B>*Running total:</B>&nbsp;This represents the running total of amounts applied to"
				+ " the corresponding"
				+ " <B>APPLIED-<I>TO</I></B> transaction in the left column.<BR>"	
			);
			out.println("<B>" + iCustomersPrinted + " customers printed</B>");

		} catch (Exception ex) {
			// handle any errors
			out.println("<BR><BR>Error!!<BR>");
			out.println("Exception: " + ex.toString() + "<BR>");
			SMLogEntry errorlog = new SMLogEntry(conn);
			errorlog.writeEntry(sUserID, SMLogEntry.LOG_OPERATION_ARAGING, "Error in aged trial balance - " + ex.getMessage(), SQL, "[1376509253]");
		}

		SMLogEntry log = new SMLogEntry(conn);
		log.writeEntry(sUserID, SMLogEntry.LOG_OPERATION_ARAGING, "REPORT", "ARAgedTrialBalance", "[1376509254]");

		clsDatabaseFunctions.freeConnection(getServletContext(), conn);
		long lEndingTime = System.currentTimeMillis();
		out.println("<BR>Processing took " + (lEndingTime - lStartingTime)/1000L + " seconds (" + (lEndingTime - lStartingTime) + "ms).");
		out.println("</BODY></HTML>");

	}
	private boolean createTemporaryTables(
			Connection conn,
			String sStartingCustomer, 
			String sEndingCustomer,
			String sAccountSet,
			String sRetainageFlag,
			String sAgedAsOfDate,
			String sCutOffDate,
			String sCurrentAgingColumn,
			String sFirstAgingColumn,
			String sSecondAgingColumn,
			String sThirdAgingColumn,
			boolean bIncludePaidTransactions
	){
		lTestTime = System.currentTimeMillis();
		sTempTableName = "ARAGINGTMP" + Long.toString(System.currentTimeMillis());
		
		String SQL;
		SMLogEntry log = new SMLogEntry(conn);

		SQL = ARSQLs.Drop_Temporary_Aging_Table(sTempTableName);
		try {
			if (!clsDatabaseFunctions.executeSQL(SQL, conn)){
			}
		} catch (SQLException e) {
			// Don't choke on this error - we may just not have a table there and that doesn't matter:
		}
		SQL = ARSQLs.Create_Temporary_Aging_Table(sTempTableName, true);
		//System.out.println("[1500489198] SQL 1 = " + SQL);
		//SQL = ARSQLs.Create_Temporary_Aging_Table(sTempTableName, false);
		try{
			Statement stmt = conn.createStatement();
			stmt.executeUpdate(SQL);
		}catch (Exception ex) {
			//log.writeEntry(sUserName, SMLogEntry.LOG_OPERATION_ARAGING, " Error creating temporary aging table - " 
					log.writeEntry(sUserID, SMLogEntry.LOG_OPERATION_ARAGING, " Error creating temporary aging table - " 
					+ ex.getMessage(), SQL, "[1376509252]");
			sWarning = "Error [1540841717] creating temporary aging table with SQL: " + SQL + " - " + ex.getMessage();
			return false;
		}

		//Insert the artransactions:
		//TODO
		
		SQL = "INSERT INTO " + sTempTableName + " ("
			+ "scustomer,"
			+ " ldocid,"
			+ " idoctype,"
			+ " sdocnumber,"
			+ " datdocdate,"
			+ " datduedate,"
			+ " datapplytodate,"
			+ " doriginalamt,"
			+ " dcurrentamt,"
			+ " sordernumber,"
			+ " ssource,"
			+ " lappliedto,"
			+ " sdocappliedto,"
			+ " dapplytodoccurrentamt,"
			+ " lparenttransactionid,"
			+ " scustomername,"
			+ " dcreditlimit"
			+ ") SELECT"
			+ " " + SMTableartransactions.spayeepayor
			+ ", " + SMTableartransactions.lid
			+ ", " + SMTableartransactions.idoctype
			+ ", " + SMTableartransactions.sdocnumber
			+ ", " + SMTableartransactions.datdocdate
			+ ", " + SMTableartransactions.datduedate
			+ ", " + SMTableartransactions.datdocdate
			+ ", " + SMTableartransactions.doriginalamt
			+ ", " + SMTableartransactions.dcurrentamt
			+ ", " + SMTableartransactions.sordernumber
			+ ", 'CONTROL'"
			+ ", " + SMTableartransactions.lid
			+ ", " + SMTableartransactions.sdocnumber
			+ ", " + SMTableartransactions.dcurrentamt
			+ ", " + SMTableartransactions.lid
			+ ", " + SMTablearcustomer.sCustomerName
			+ ", " + SMTablearcustomer.dCreditLimit
			+ " FROM " + SMTableartransactions.TableName + ", " 
			+ SMTablearcustomer.TableName
			+ " WHERE ("
			+ "(" + SMTableartransactions.spayeepayor + ">='" + sStartingCustomer + "')"
			+ " AND (" + SMTableartransactions.spayeepayor + "<='" + sEndingCustomer + "')";

		if(sRetainageFlag.compareToIgnoreCase("") != 0){
			SQL = SQL + " AND (" + SMTableartransactions.iretainage + "=" + sRetainageFlag + ")";
		}
		SQL = SQL + " AND (" + SMTableartransactions.datdocdate + "<='" + sCutOffDate + " 23:59:59')"
		+ " AND (" + SMTableartransactions.spayeepayor + " = " 
		+ SMTablearcustomer.TableName + "." + SMTablearcustomer.sCustomerNumber + ")";

		if (sAccountSet.compareToIgnoreCase("") != 0){
			SQL = SQL + " AND (" + SMTablearcustomer.sAccountSet + " = '" + sAccountSet + "')";
		}
		if(!bIncludePaidTransactions){
			SQL = SQL + " AND (" + SMTableartransactions.dcurrentamt + " != 0.00)";
		}
		SQL = SQL + ")"
		;
		lTestTime = System.currentTimeMillis();
		//System.out.println("[1500489199] SQL 2 = " + SQL);
		try{
			Statement stmt = conn.createStatement();
			stmt.executeUpdate(SQL);
		}catch (Exception ex) {
			log.writeEntry(sUserID, SMLogEntry.LOG_OPERATION_ARAGING, "Error inserting transactions into aging table - " 
					+ ex.getMessage(), SQL, "[1376509511]");
			sWarning = "Error inserting transactions into aging table with SQL: " + SQL + " - " + ex.getMessage();
			return false;
		}
		
		//Turn commit back on in the target table:
		try {
			Statement stmtInsert = conn.createStatement();
			stmtInsert.execute("COMMIT");
		} catch (Exception e) {
			log.writeEntry(sUserID, SMLogEntry.LOG_OPERATION_ARAGING, " Error turning on COMMIT - " 
				+ e.getMessage(), SQL, "[1501162623]");
			sWarning = "Error [1501162623] turning on COMMIT - " + e.getMessage();
			return false;
		}
		
		if (bDebugMode){
			System.out.println(
					"[1500480914] In " + this.toString() + " - " + "First insert took " 
					+ (System.currentTimeMillis() - lTestTime) + " milliseconds.");
		}
		//Insert the matching lines:

		//To optimize the speed of the insert, we'll disable the keys and lock the table:
		//Disabling the keys doesn't seem to make any consistent difference - 2/3/2012 - TJR
		//TODO
		//SQL = "ALTER TABLE " + sTempTableName + " DISABLE KEYS";
		//try{
		//	Statement stmt = conn.createStatement();
		//	stmt.executeUpdate(SQL);
		//}catch (Exception ex) {
		//	log.writeEntry(sUserName, SMLogEntry.LOG_OPERATION_ARAGING, "Error disabling keys to insert matchinglines - " 
		//			+ ex.getMessage(), SQL);
		//	sWarning = "Error disabling keys to insert matchinglines with SQL: " + SQL + " - " + ex.getMessage();
		//	return false;
		//}
		
		/*
		TJR - 5/13/2011 - this isn't needed on a temporary table, but I'm leaving it here as a sample
		in case we need it in a NON-temporary table:
		SQL = "LOCK TABLES " + sTempTableName + " WRITE";
		try{
			Statement stmt = conn.createStatement();
			stmt.executeUpdate(SQL);
		}catch (Exception ex) {
			log.writeEntry(sUserName, SMLogEntry.LOG_OPERATION_ARAGING, "Error locking table to insert matchinglines - " 
					+ ex.getMessage(), SQL);
			sWarning = "Error locking table to insert matchinglines with SQL: " + SQL + " - " + ex.getMessage();
			return false;
		}
		*/
		
		SQL = "INSERT INTO " + sTempTableName + " ("
			+ "scustomer"
			+ ", ldocid"
			+ ", sdocnumber"
			+ ", datdocdate"
			+ ", datduedate"
			+ ", datapplytodate"
			+ ", doriginalamt"
			+ ", dcurrentamt"
			+ ", sordernumber"
			+ ", ssource"
			+ ", lappliedto"
			+ ", sdocappliedto"
			+ ", dapplytodoccurrentamt"
			+ ", lparenttransactionid"
			+ ", scustomername"
			+ ", dcreditlimit"

			+ ") SELECT"
			+ " " + SMTablearmatchingline.TableName + "." + SMTablearmatchingline.spayeepayor
			+ ", " + SMTablearmatchingline.TableName + "." + SMTablearmatchingline.lid
			+ ", " + SMTablearmatchingline.TableName + "." + SMTablearmatchingline.sdocnumber
			+ ", " + SMTablearmatchingline.TableName + "." + SMTablearmatchingline.dattransactiondate
			+ ", " + SMTableartransactions.TableName + "." + SMTableartransactions.datduedate
			+ ", " + SMTableartransactions.TableName + "." + SMTableartransactions.datdocdate
			//Applied amounts have the same sign as the apply-to amount, and so they must be negated:
			+ ", -1 * " + SMTablearmatchingline.TableName + "." + SMTablearmatchingline.damount
			+ ", -1 * " + SMTablearmatchingline.TableName + "." + SMTablearmatchingline.damount
			+ ", ''"
			+ ", 'DIST'"
			+ ", " + SMTablearmatchingline.TableName + "." + SMTablearmatchingline.ldocappliedtoid
			+ ", " + SMTablearmatchingline.TableName + "." + SMTablearmatchingline.sapplytodoc
			+ ", " + SMTableartransactions.TableName + "." + SMTableartransactions.dcurrentamt
			+ ", " + SMTablearmatchingline.TableName + "." + SMTablearmatchingline.lparenttransactionid
			+ ", IF (" + SMTablearcustomer.TableName + "." + SMTablearcustomer.sCustomerName + " IS NULL, '(NOT FOUND)', " 
				+ SMTablearcustomer.TableName + "." + SMTablearcustomer.sCustomerName + ")"
			+ ", IF (" + SMTablearcustomer.TableName + "." + SMTablearcustomer.dCreditLimit + " IS NULL, 0.00, "
				+ SMTablearcustomer.TableName + "." + SMTablearcustomer.dCreditLimit + ")"
			+ " FROM (" + SMTablearmatchingline.TableName + " LEFT JOIN " + SMTableartransactions.TableName
			+ " ON " + SMTablearmatchingline.TableName + "." + SMTablearmatchingline.ldocappliedtoid + " = "
			+ SMTableartransactions.TableName + "." + SMTableartransactions.lid + ")"
			
			+ " LEFT JOIN " + SMTablearcustomer.TableName + " ON " + SMTablearmatchingline.TableName
			+ "." + SMTablearmatchingline.spayeepayor + " = " + SMTablearcustomer.TableName + "."
			+ SMTablearcustomer.sCustomerNumber
			
			+ " WHERE ("
			+ "(" + SMTableartransactions.TableName + "." + SMTableartransactions.spayeepayor + ">='" 
			+ sStartingCustomer + "')"
			+ " AND (" + SMTableartransactions.TableName + "." + SMTableartransactions.spayeepayor + "<='" 
			+ sEndingCustomer + "')";

		if (sAccountSet.compareToIgnoreCase("") != 0){
			SQL = SQL + " AND (" + SMTablearcustomer.sAccountSet + " = '" + sAccountSet + "')";
		}
		if(sRetainageFlag.compareToIgnoreCase("") != 0){
			SQL = SQL + " AND (" + SMTableartransactions.TableName + "." 
			+ SMTableartransactions.iretainage + "=" + sRetainageFlag + ")";
		}

		SQL = SQL + " AND (" + SMTablearmatchingline.dattransactiondate + "<='" + sCutOffDate + " 23:59:59')"
		//End Where clause:
		+ ")"
		;

		//Sort to match the temp table:
		//SQL += " ORDER BY "
		//	+ SMTablearmatchingline.TableName + "." + SMTablearmatchingline.spayeepayor
		//	+ ", " + SMTablearmatchingline.TableName + "." + SMTablearmatchingline.ldocappliedtoid
		//	+ ", " + SMTablearmatchingline.TableName + "." + SMTablearmatchingline.sdocnumber
		//	+ ", " + SMTablearmatchingline.TableName + "." + SMTablearmatchingline.lparenttransactionid
		//;
		
		//if (bDebugMode){
		//	System.out.println("In " + this.toString() + " - Insert matchinglines SQL = " + SQL);
		//}
		
		lTestTime = System.currentTimeMillis();
		//System.out.println("[1500489200] SQL 3 = " + SQL);
		try{
			Statement stmt = conn.createStatement();
			stmt.executeUpdate(SQL);
		}catch (Exception ex) {
			log.writeEntry(sUserID, SMLogEntry.LOG_OPERATION_ARAGING, "Error inserting distribution lines into aging table - " 
					+ ex.getMessage(), SQL, "[1376509519]");
			sWarning = "Error inserting distribution lines into aging table with SQL: " + SQL + " - " + ex.getMessage();
			return false;
		}
		
		//Commit the transaction:
		try {
			Statement stmtInsert = conn.createStatement();
			stmtInsert.execute("COMMIT");
		} catch (Exception e) {
			log.writeEntry(sUserID, SMLogEntry.LOG_OPERATION_ARAGING, " Error COMMITTING transaction - " 
				+ e.getMessage(), SQL, "[1501162625]");
			sWarning = "Error [1501162625] COMMITTING transaction - " + e.getMessage();
			return false;
		}
		
		if (bDebugMode){
			System.out.println(
					"[1500480913] In " + this.toString() + " - " + "Inserting matching lines took " 
					+ (System.currentTimeMillis() - lTestTime) + " milliseconds.");
		}

		/*
		SQL = "UNLOCK TABLES";
		try{
			Statement stmt = conn.createStatement();
			stmt.executeUpdate(SQL);
		}catch (Exception ex) {
			log.writeEntry(sUserName, SMLogEntry.LOG_OPERATION_ARAGING, "Error UNlocking table after inserting matchinglines - " 
					+ ex.getMessage(), SQL);
			sWarning = "Error UNlocking table after inserting matchinglines with SQL: " + SQL + " - " + ex.getMessage();
			return false;
		}
		*/
		//TODO
		//SQL = "ALTER TABLE " + sTempTableName + " ENABLE KEYS";
		//try{
		//	Statement stmt = conn.createStatement();
		//	stmt.executeUpdate(SQL);
		//}catch (Exception ex) {
		//	log.writeEntry(sUserName, SMLogEntry.LOG_OPERATION_ARAGING, "Error enabling keys after inserting matchinglines - " 
		//			+ ex.getMessage(), SQL);
		//	sWarning = "Error enabling keys after inserting matchinglines with SQL: " + SQL + " - " + ex.getMessage();
		//	return false;
		//}
		//Insert_Parent_Document_Type_Into_Aging_Table
		SQL = ARSQLs.Update_Parent_Document_Type_In_Aging_Table(sTempTableName);
		//System.out.println("[1500489201] SQL 4 = " + SQL);
		lTestTime = System.currentTimeMillis();
		try{
			Statement stmt = conn.createStatement();
			stmt.executeUpdate(SQL);
		}catch (Exception ex) {
			log.writeEntry(sUserID, SMLogEntry.LOG_OPERATION_ARAGING, "Error updating parent document type in aging table - " 
					+ ex.getMessage(), SQL, "[1376509541]");
			sWarning = "Error updating parent document type in aging table with SQL: " + SQL + " - " + ex.getMessage();
			return false;
		}
		
		//Commit the transaction:
		try {
			Statement stmtInsert = conn.createStatement();
			stmtInsert.execute("COMMIT");
		} catch (Exception e) {
			log.writeEntry(sUserID, SMLogEntry.LOG_OPERATION_ARAGING, " Error COMMITTING transaction - " 
				+ e.getMessage(), SQL, "[1501162626]");
			sWarning = "Error [1501162626] COMMITTING transaction - " + e.getMessage();
			return false;
		}
		
		if (bDebugMode){
			System.out.println(
					"[1500480915] In " + this.toString() + " - " + "Updating parent document type took " 
					+ (System.currentTimeMillis() - lTestTime) + " milliseconds.");
		}
		//Update the aging columns on all lines, based on their 'due' dates: 
		//applied-to documents:
		SQL = ARSQLs.Update_AgingColumns_In_Aging_Table(
				sTempTableName,
				sAgedAsOfDate,
				sCurrentAgingColumn,
				sFirstAgingColumn, 
				sSecondAgingColumn, 
				sThirdAgingColumn);
		//System.out.println("[1500489202] SQL 5 = " + SQL);
		lTestTime = System.currentTimeMillis();
		try{
			Statement stmt = conn.createStatement();
			stmt.executeUpdate(SQL);
		}catch (Exception ex) {
			log.writeEntry(sUserID, SMLogEntry.LOG_OPERATION_ARAGING, "Error updating aging columns - " 
					+ ex.getMessage(), SQL, "[1376509549]");
			sWarning = "Error updating updating aging columns with SQL: " + SQL + " - " + ex.getMessage();
			return false;
		}
		
		//Commit the transaction:
		try {
			Statement stmtInsert = conn.createStatement();
			stmtInsert.execute("COMMIT");
		} catch (Exception e) {
			log.writeEntry(sUserID, SMLogEntry.LOG_OPERATION_ARAGING, " Error COMMITTING transaction - " 
				+ e.getMessage(), SQL, "[1501162627]");
			sWarning = "Error [1501162627] COMMITTING transaction - " + e.getMessage();
			return false;
		}
		
		if (bDebugMode){
			System.out.println(
					"[1500480916] In " + this.toString() + " - " + "Updating aging columns took " 
					+ (System.currentTimeMillis() - lTestTime) + " milliseconds.");
		}
		//Update current balances:
		//TODO
		/*
		SQL = 
			"UPDATE " + sTempTableName + " INNER JOIN" 
			+ " " + SMTablearcustomerstatistics.TableName
			+ " ON " + sTempTableName + ".scustomer = " + SMTablearcustomerstatistics.TableName 
			+ "." + SMTablearcustomerstatistics.sCustomerNumber
			+ " SET " + sTempTableName + ".dbalance = " + SMTablearcustomerstatistics.TableName + "." + 
			SMTablearcustomerstatistics.sCurrentBalance
			;
		//System.out.println("In " + this.toString() + ".createTemporaryTables, update current balances SQL = " + SQL);
		lTestTime = System.currentTimeMillis();
		try{
			Statement stmt = conn.createStatement();
			stmt.executeUpdate(SQL);
		}catch (Exception ex) {
			log.writeEntry(sUserName, SMLogEntry.LOG_OPERATION_ARAGING, "Error updating current balances - " 
					+ ex.getMessage(), SQL);
			sWarning = "Error updating updating current balances with SQL: " + SQL + " - " + ex.getMessage();
			return false;
		}
		if (bDebugMode){
			System.out.println(
					"In " + this.toString() + " - " + "Updating current balances took " 
					+ (System.currentTimeMillis() - lTestTime)/1000L + " seconds.");
		}
		*/
		//Update retainage balances:
		SQL = 
			/* - TJR - 7/25/2017 - Newer version - has bug that doesn't display all the retainage as it should:
			"UPDATE " 
			+ " (SELECT"
			+ " SUM(" + SMTableartransactions.dcurrentamt + ") AS retainagebalance"
			+ ", " + SMTableartransactions.spayeepayor
			+ " FROM " + SMTableartransactions.TableName
			+ " WHERE ("
				+ "(" + SMTableartransactions.iretainage + " = 1)"
				+ " AND (" + SMTableartransactions.dcurrentamt + " != 0.00)"
			+ ")"
			+ ") AS RETAINAGEQUERY"
			+ " LEFT JOIN " + sTempTableName + " ON " + sTempTableName + ".scustomer=RETAINAGEQUERY." + SMTableartransactions.spayeepayor
			+ " SET " + sTempTableName + ".dretainagebalance = RETAINAGEQUERY.retainagebalance"
			*/
				
			"UPDATE " + sTempTableName + " INNER JOIN" 
			+ " (SELECT SUM(dcurrentamt) AS retainagebalance"
			+ ", artransactions.spayeepayor"
			+ " FROM " + SMTableartransactions.TableName
			+ " WHERE iretainage = 1"
			+ " GROUP BY " + SMTableartransactions.spayeepayor + ") as retainagequery" 
			+ " ON " + sTempTableName + ".scustomer = retainagequery." + SMTableartransactions.spayeepayor
			+ " SET " + sTempTableName + ".dretainagebalance = retainagequery.retainagebalance"
			;
		//System.out.println("In " + this.toString() + ".createTemporaryTables, retainage SQL = " + SQL);
		lTestTime = System.currentTimeMillis();
		//System.out.println("[1500489203] SQL 6 = " + SQL);
		try{
			Statement stmt = conn.createStatement();
			stmt.executeUpdate(SQL);
		}catch (Exception ex) {
			log.writeEntry(sUserID, SMLogEntry.LOG_OPERATION_ARAGING, "Error updating retainage balances - " 
					+ ex.getMessage(), SQL,"[1376509542]");
			sWarning = "Error updating retainage balances with SQL: " + SQL + " - " + ex.getMessage();
			return false;
		}
		
		//Commit the transaction:
		try {
			Statement stmtInsert = conn.createStatement();
			stmtInsert.execute("COMMIT");
		} catch (Exception e) {
			log.writeEntry(sUserID, SMLogEntry.LOG_OPERATION_ARAGING, " Error COMMITTING transaction - " 
				+ e.getMessage(), SQL, "[1501162627]");
			sWarning = "Error [1501162627] COMMITTING transaction - " + e.getMessage();
			return false;
		}
		
		if (bDebugMode){
			System.out.println(
					"[1500480917] In " + this.toString() + " - " + "Updating retainage balances took " 
					+ (System.currentTimeMillis() - lTestTime) + " milliseconds.");
		}
		return true;
	}
	private String getDocumentTypeLabel(int lDocType){

		switch (lDocType){
		//Invoice
		case 0: return "IN";
		//Credit
		case 1: return "CR";
		//Payment
		case 2: return "PY";
		//Prepay
		case 3: return "PI";
		//Reversal
		case 4: return "RV";
		//Invoice adjustment
		case 5: return "IA";
		//Misc Receipt
		case 6: return "MR";
		//Cash adjustment
		case 7: return "CA";
		//Credit adjustment:
		case 8: return "RA";
		//Retainage transaction:
		case 9: return "RT";
		default: return "IN";
		}
	}
	private static String setTableStyleString(){
		String s = "";
		String sBorderSize = "0";
		String sFontSize = "small";
		s += "<style type=\"text/css\">\n";
		
		//TJR - 5/13/2011 - I left all these comments in to use as samples here or elsewhere:
		//Set hyperlink style:
		//s += "a {font-family : Arial; Font-size : 12px; text-decoration : none}\n";
		
		//s += "amenu {font-family : Arial; text-decoration : none; font-weight: 900}\n";
		//s += "amenu:link {color : white}\n";
		//s += "amenu:visited {color : #99FFFF}\n";
		//s += "amenu:active {color : #99FFFF}\n";
		//s += "amenu:hover {color : white}\n";
		
		//s += "a {font-family : Arial; text-decoration : none; font-weight: 900}\n";
		//s += "a:link {color : #99FFFF}\n";
		//s += "a:visited {color : #99FFFF}\n";
		//s += "a:active {color : #99FFFF}\n";
		//s += "a:hover {color : white}\n";
		
		//Layout table:
		s +=
			"table.main {"
			+ "border-width: " + sBorderSize + "px; "
			+ "border-spacing: 2px; "
			//+ "border-style: outset; "
			+ "border-style: none; "
			+ "border-color: white; "
			+ "border-collapse: separate; "
			+ "width: 100%; "
			+ "font-size: " + sFontSize + "; "
			//+ "font-family : Arial; "
			+ "color: black; "
			//+ "background-color: white; "
			+ "}"
			+ "\n"
			;

		//s +=
		//	"table.main th {"
		//	+ "border-width: " + sBorderSize + "px; "
		//	+ "padding: 2px; "
		//	//+ "border-style: inset; "
		//	+ "border-style: none; "
		//	+ "border-color: white; "
		//	+ "background-color: white; "
		//	+ "color: black; "
		//	+ "font-family : Arial; "
		//	+ "vertical-align: text-middle; "
		//	//+ "height: 50px; "
		//	+ "}"
		//	+ "\n"
		//	;

		//s +=
		//	"tr.d0 td {"
		//	+ "background-color: #FFFFFF; "
		//	+"}"
		//	;
		//s +=
		//	"tr.d1 td {"
		//	+ "background-color: #EEEEEE; "
		//	+ "}"
		//	+ "\n"
		//	;

		//This is the def for a left-aligned TD:
		s +=
			"td.l {"
			//+ "border-width: " + sBorderSize + "px; "
			//+ "padding: 2px; "
			//+ "border-style: none; "
			//+ "border-color: white; "
			//+ "vertical-align: text-middle;"
			//+ "background-color: black; "
			+ "text-align: left; "
			//+ "color: black; "
			//+ "height: 50px; "
			+ "}"
			+ "\n"
			;

		//This is the default TD def - it is right aligned
		s +=
			"td.r {"
			//+ "border-width: " + sBorderSize + "px; "
			//+ "padding: 2px; "
			//+ "border-style: none; "
			//+ "border-color: white; "
			//+ "vertical-align: text-middle;"
			//+ "background-color: black; "
			+ "text-align: right; "
			//+ "color: black; "
			//+ "height: 50px; "
			+ "}"
			+ "\n"
			;
		
		s += "</style>"
			+ "\n"
			;

		return s;
	}
}
