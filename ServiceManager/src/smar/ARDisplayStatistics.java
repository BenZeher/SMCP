package smar;

import SMDataDefinition.*;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsManageBigDecimals;

import java.math.BigDecimal;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import SMClasses.SMLogEntry;
import smcontrolpanel.SMAuthenticate;
import smcontrolpanel.SMSystemFunctions;
import smcontrolpanel.SMUtilities;
public class ARDisplayStatistics extends HttpServlet {

	private static final long serialVersionUID = 1L;
	private static final String sObjectName = "Customer";
	public void doPost(HttpServletRequest request,
				HttpServletResponse response)
				throws ServletException, IOException {

		PrintWriter out = response.getWriter();
		if(!SMAuthenticate.authenticateSMCPCredentials(
			request, 
			response, 
			getServletContext(), 
			SMSystemFunctions.ARCustomerStatistics))
		{
			return;
		}
	    //Get the session info:
	    HttpSession CurrentSession = request.getSession(true);
	    String sDBID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_DATABASE_ID);
	    String sCompanyName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_COMPANYNAME);
	    String sCustomerCode = (String) request.getParameter(sObjectName);
	    String sUserID = (String)CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERID);
		String title = "";
		String subtitle = "";
		title = "View statistics for: " + sCustomerCode;
	    subtitle = "";
	    out.println(SMUtilities.SMCPTitleSubBGColor(title, subtitle, SMUtilities.getInitBackGroundColor(getServletContext(), sDBID), sCompanyName));

	    //Print a link to the first page after login:
	    out.println("<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMUserLogin?" 
				+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
				+ "\">Return to user login</A><BR>");
	    out.println("<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smar.ARMainMenu?" 
				+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
				+ "\">Return to Accounts Receivable Main Menu</A><BR><BR>");

	    displayStatistics(sCustomerCode, out, sDBID, sUserID);
	    
		out.println("</BODY></HTML>");
	}
	private boolean displayStatistics (String sCustomerCode, PrintWriter pwOut, String sDBID, String sUserID){
		
		ARCustomer cust = new ARCustomer(sCustomerCode);
		if (!cust.load(getServletContext(), sDBID)){
			pwOut.println("Error loading customer.");
			return false;
		}
		
		ARCustomerStatistics custstat = new ARCustomerStatistics(sCustomerCode);
		if (!custstat.load(getServletContext(), sDBID)){
			pwOut.println("Error loading customer statistics.");
			return false;
		}
		

		pwOut.println(SMUtilities.getMasterStyleSheetLink());
		pwOut.println("<B>Customer name: </B>" + cust.getM_sCustomerName());
		
		//Table:
		pwOut.println("<TABLE WIDTH=100% CLASS=\"" + SMMasterStyleSheetDefinitions.TABLE_BASIC_WITH_BORDER + "\">");
		pwOut.println("<TR CLASS =\"" + SMMasterStyleSheetDefinitions.TABLE_FOOTER + "\">");
		pwOut.println("<TD CLASS=\"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\"><B>Balance:</B> "
				+ clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(custstat.getM_bdcurrentbalance()) + "</TD>");
		pwOut.println("<TD CLASS=\"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\"><B>Last invoice:</B> "
				+ clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(custstat.getM_bdamountoflastinvoice())
				+ " <B>on</B> " + custstat.getM_datlastinvoice() + "</TD>");
		pwOut.println("<TD CLASS=\"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\"><B>Last credit:</B> " 
				+ clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(custstat.getM_bdamountoflastcredit())
				+ " <B>on</B> " + custstat.getM_datlastcredit() + "</TD>");
		pwOut.println("<TD CLASS=\"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\"><B>Last payment:</B> "
				+ clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(custstat.getM_bdamountoflastpayment())
				+ " <B>on</B> " + custstat.getM_datlastpayment() + "</TD>");

		pwOut.println("</TR>");
		
		//Largest invoice:
		pwOut.println("<TR CLASS =\"" + SMMasterStyleSheetDefinitions.TABLE_FOOTER + "\">");
		pwOut.println("<TD CLASS=\"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\"><B>Largest invoice:</B> " 
				+ clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(custstat.getM_bdamountofhighestinvoice()) + "</TD>");
		//Largest invoice last year:
		pwOut.println("<TD CLASS=\"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\"><B>Largest last year:</B> " 
				+ clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(custstat.getM_bdamountofhighestinvoicelastyear()) + "</TD>");
		
		//largest balance
		pwOut.println("<TD CLASS=\"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\"><B>Highest balance:</B> " 
				+ clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(custstat.getM_bdamountofhighestbalance()) + "</TD>");
		
		//largest balance last year
		pwOut.println("<TD CLASS=\"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\"><B>Highest last year:</B> " 
				+ clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(custstat.getM_bdamountofhighestbalancelastyear()) + "</TD>");
		pwOut.println("</TR>");
		
		pwOut.println("<TR CLASS =\"" + SMMasterStyleSheetDefinitions.TABLE_FOOTER + "\">");
		//total number of paid invoices
		pwOut.println("<TD CLASS=\"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\"><B>Paid invoices:</B> " 
				+ Long.toString(custstat.getM_ltotalnumberofpaidinvoices()) + "</TD>");
		
		//total number of open invoices
		pwOut.println("<TD CLASS=\"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\"><B>Open invoices:</B> " 
				+ Long.toString(custstat.getM_lnumberofopeninvoices()) + "</TD>");
		
		//total number of days to pay
		pwOut.println("<TD CLASS=\"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\"><B>Total days to pay:</B> " 
				+ Long.toString(custstat.getM_ltotalnumberofdaystopay()) + "</TD>");

		//average number of days to pay
		BigDecimal bdTotalDaysToPay = new BigDecimal(Long.toString(custstat.getM_ltotalnumberofdaystopay()));
		BigDecimal bdTotalPaidInvoices = new BigDecimal(Long.toString(custstat.getM_ltotalnumberofpaidinvoices()));
		BigDecimal bdAvgDaysToPay = new BigDecimal(0);
		if(bdTotalPaidInvoices.compareTo(BigDecimal.ZERO) > 0){
			bdAvgDaysToPay = bdTotalDaysToPay.divide(bdTotalPaidInvoices, BigDecimal.ROUND_HALF_UP);
		}
		pwOut.println("<TD CLASS=\"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\"><B>Avg. days to pay:</B> " 
				+ clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(bdAvgDaysToPay) + "</TD>");

		pwOut.println("</TR>");
		pwOut.println("<TR CLASS =\"" + SMMasterStyleSheetDefinitions.TABLE_FOOTER + "\">");
		pwOut.println("<TD CLASS=\"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\"><B>Retainage balance:</B> " 
				+ clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(cust.getCalculatedRetainageBalance(getServletContext(), sDBID)) + "</TD>");
		pwOut.println("<TD CLASS=\"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\"><B>Credit limit:</B> " 
				+ cust.getM_dCreditLimit() + "</TD>");
		pwOut.println("</TR>");
		pwOut.println("</TABLE>");

		try{
			String SQL =  "SELECT * FROM " + SMTablearmonthlystatistics.TableName 
					+ " WHERE (" 
					+ "(" + SMTablearmonthlystatistics.sCustomerNumber + " = '" + sCustomerCode + "')"
				+ ")"
				+ " ORDER BY " + SMTablearmonthlystatistics.sYear + " DESC" 
					+ ", " + SMTablearmonthlystatistics.sMonth + " DESC";
			ResultSet rs = clsDatabaseFunctions.openResultSet(
				SQL, 
				getServletContext(),
				sDBID,
				"MySQL",
				this.toString() + ".displayStatistics");
			//Display the monthly statistics:
			pwOut.println("<B><U>Monthly statistics</U></B>");
			pwOut.println("<TABLE BGCOLOR=\"#FFFFFF\" WIDTH=100% CLASS=\"" + SMMasterStyleSheetDefinitions.TABLE_BASIC_WITH_BORDER + "\">");
			pwOut.println("<TR CLASS =\"" + SMMasterStyleSheetDefinitions.TABLE_HEADING + "\">");
			//year
			pwOut.println("<TD CLASS=\"" + SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_LEFT_JUSTIFIED + "\">Year</TD>");		
			//month
			pwOut.println("<TD CLASS=\"" + SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_LEFT_JUSTIFIED + "\">Month</TD>");
			//invoice total
			pwOut.println("<TD CLASS=\"" + SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_LEFT_JUSTIFIED + "\">Invoice total</TD>");
			//credit total
			pwOut.println("<TD CLASS=\"" + SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_LEFT_JUSTIFIED + "\">Credit total</TD>");
			//payment total
			pwOut.println("<TD CLASS=\"" + SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_LEFT_JUSTIFIED + "\">Payment total</TD>");
			//number of invoices
			pwOut.println("<TD CLASS=\"" + SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_LEFT_JUSTIFIED + "\">Invoices</TD>");
			//number of credits
			pwOut.println("<TD CLASS=\"" + SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_LEFT_JUSTIFIED + "\">Credits</TD>");
			//number of payments
			pwOut.println("<TD CLASS=\"" + SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_LEFT_JUSTIFIED + "\">Payments</TD>");
			//paid invoices
			pwOut.println("<TD CLASS=\"" + SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_LEFT_JUSTIFIED + "\">Paid invoices</TD>");
			//total days to pay
			pwOut.println("<TD CLASS=\"" + SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_LEFT_JUSTIFIED + "\">Total days to pay</TD>");
			//average days to pay
			pwOut.println("<TD CLASS=\"" + SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_LEFT_JUSTIFIED + "\">Avg days to pay</TD>");
			pwOut.println("</TR>");
			int iCount=0;
			while (rs.next()){
				if(iCount % 2 == 0) {
					pwOut.println("<TR CLASS =\"" + SMMasterStyleSheetDefinitions.TABLE_ROW_EVEN + "\">");
				}else {
					pwOut.println("<TR CLASS =\"" + SMMasterStyleSheetDefinitions.TABLE_ROW_ODD + "\">");
				}
				pwOut.println("<TD CLASS=\"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\">" + Long.toString(rs.getLong(SMTablearmonthlystatistics.sYear)) + "</TD>");
				pwOut.println("<TD CLASS=\"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\">" + Long.toString(rs.getLong(SMTablearmonthlystatistics.sMonth)) + "</TD>");
				pwOut.println("<TD CLASS=\"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\">"
						+ clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(rs.getBigDecimal(SMTablearmonthlystatistics.sInvoiceTotal)) + "</TD>");
				pwOut.println("<TD CLASS=\"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\">"
						+ clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(rs.getBigDecimal(SMTablearmonthlystatistics.sCreditTotal)) + "</TD>");
				pwOut.println("<TD CLASS=\"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\">"
						+ clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(rs.getBigDecimal(SMTablearmonthlystatistics.sPaymentTotal)) + "</TD>");
				pwOut.println("<TD CLASS=\"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\">" + Long.toString(rs.getLong(SMTablearmonthlystatistics.sNumberOfInvoices)) + "</TD>");
				pwOut.println("<TD CLASS=\"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\">"+ Long.toString(rs.getLong(SMTablearmonthlystatistics.sNumberOfCredits)) + "</TD>");
				pwOut.println("<TD CLASS=\"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\">" + Long.toString(rs.getLong(SMTablearmonthlystatistics.sNumberOfPayments)) + "</TD>");
				pwOut.println("<TD CLASS=\"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\">" + Long.toString(rs.getLong(SMTablearmonthlystatistics.sNumberOfPaidInvoices)) + "</TD>");
				pwOut.println("<TD CLASS=\"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\">" + Long.toString(rs.getLong(SMTablearmonthlystatistics.sTotalNumberOfDaysToPay)) + "</TD>");
				iCount++;
				bdTotalDaysToPay = new BigDecimal(Long.toString(rs.getLong(SMTablearmonthlystatistics.sTotalNumberOfDaysToPay)));
				bdTotalPaidInvoices = new BigDecimal(Long.toString(rs.getLong(SMTablearmonthlystatistics.sNumberOfPaidInvoices)));
				bdAvgDaysToPay = new BigDecimal(0);
				if(bdTotalPaidInvoices.compareTo(BigDecimal.ZERO) > 0){
					bdAvgDaysToPay = bdTotalDaysToPay.divide(bdTotalPaidInvoices, BigDecimal.ROUND_HALF_UP);
				}
				pwOut.println("<TD ALIGN=RIGHT>" 
						+ clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(bdAvgDaysToPay) + "</TD>");

				
				pwOut.println("</TR>");
			}
			rs.close();
			
			   SMClasses.SMLogEntry log = new SMClasses.SMLogEntry(sDBID, getServletContext());
		 	   log.writeEntry(sUserID, SMLogEntry.LOG_OPERATION_ARDISPLAYSTATISTICS, "REPORT", "ARDisplayStatistics", "[1564762299]");
			pwOut.println("</TABLE>");
		}catch (SQLException e){
			System.out.println("Error reading monthly statistics - " + e.getMessage());
			pwOut.println("Error reading monthly statistics - " + e.getMessage());
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
