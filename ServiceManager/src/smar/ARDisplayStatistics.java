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

	    displayStatistics(sCustomerCode, out, sDBID);
	    
		out.println("</BODY></HTML>");
	}
	private boolean displayStatistics (String sCustomerCode, PrintWriter pwOut, String sConf){
		
		ARCustomer cust = new ARCustomer(sCustomerCode);
		if (!cust.load(getServletContext(), sConf)){
			pwOut.println("Error loading customer.");
			return false;
		}
		
		ARCustomerStatistics custstat = new ARCustomerStatistics(sCustomerCode);
		if (!custstat.load(getServletContext(), sConf)){
			pwOut.println("Error loading customer statistics.");
			return false;
		}
		
		pwOut.println("<B>Customer name: </B>" + cust.getM_sCustomerName());
		
		//Table:
		pwOut.println("<TABLE BORDER=1 CELLSPACING=2 style=\"font-size:75%\">");
		pwOut.println("<TR>");
		pwOut.println("<TD><B>Balance:</B> "
				+ clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(custstat.getM_bdcurrentbalance()) + "<BR>");
		pwOut.println("<TD><B>Last invoice:</B> "
				+ clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(custstat.getM_bdamountoflastinvoice())
				+ " <B>on</B> " + custstat.getM_datlastinvoice() + "</TD>");
		pwOut.println("<TD><B>Last credit:</B> " 
				+ clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(custstat.getM_bdamountoflastcredit())
				+ " <B>on</B> " + custstat.getM_datlastcredit() + "</TD>");
		pwOut.println("<TD><B>Last payment:</B> "
				+ clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(custstat.getM_bdamountoflastpayment())
				+ " <B>on</B> " + custstat.getM_datlastpayment() + "</TD>");

		pwOut.println("</TR>");
		
		//Largest invoice:
		pwOut.println("<TR>");
		pwOut.println("<TD><B>Largest invoice:</B> " 
				+ clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(custstat.getM_bdamountofhighestinvoice()) + "</TD>");
		//Largest invoice last year:
		pwOut.println("<TD><B>Largest last year:</B> " 
				+ clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(custstat.getM_bdamountofhighestinvoicelastyear()) + "</TD>");
		
		//largest balance
		pwOut.println("<TD><B>Highest balance:</B> " 
				+ clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(custstat.getM_bdamountofhighestbalance()) + "</TD>");
		
		//largest balance last year
		pwOut.println("<TD><B>Highest last year:</B> " 
				+ clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(custstat.getM_bdamountofhighestbalancelastyear()) + "</TD>");
		pwOut.println("</TR>");
		
		pwOut.println("<TR>");
		//total number of paid invoices
		pwOut.println("<TD><B>Paid invoices:</B> " 
				+ Long.toString(custstat.getM_ltotalnumberofpaidinvoices()) + "</TD>");
		
		//total number of open invoices
		pwOut.println("<TD><B>Open invoices:</B> " 
				+ Long.toString(custstat.getM_lnumberofopeninvoices()) + "</TD>");
		
		//total number of days to pay
		pwOut.println("<TD><B>Total days to pay:</B> " 
				+ Long.toString(custstat.getM_ltotalnumberofdaystopay()) + "</TD>");

		//average number of days to pay
		BigDecimal bdTotalDaysToPay = new BigDecimal(Long.toString(custstat.getM_ltotalnumberofdaystopay()));
		BigDecimal bdTotalPaidInvoices = new BigDecimal(Long.toString(custstat.getM_ltotalnumberofpaidinvoices()));
		BigDecimal bdAvgDaysToPay = new BigDecimal(0);
		if(bdTotalPaidInvoices.compareTo(BigDecimal.ZERO) > 0){
			bdAvgDaysToPay = bdTotalDaysToPay.divide(bdTotalPaidInvoices, BigDecimal.ROUND_HALF_UP);
		}
		pwOut.println("<TD><B>Avg. days to pay:</B> " 
				+ clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(bdAvgDaysToPay) + "</TD>");

		pwOut.println("</TR>");
		pwOut.println("<TD><B>Retainage balance:</B> " 
				+ clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(cust.getCalculatedRetainageBalance(getServletContext(), sConf)) + "</TD>");
		pwOut.println("<TD><B>Credit limit:</B> " 
				+ cust.getM_dCreditLimit() + "</TD>");
		
		pwOut.println("</TABLE>");

		try{
			String SQL = ARSQLs.Get_Monthly_Statistics_For_Customer(sCustomerCode);
			ResultSet rs = clsDatabaseFunctions.openResultSet(
				SQL, 
				getServletContext(),
				sConf,
				"MySQL",
				this.toString() + ".displayStatistics");
			//Display the monthly statistics:
			pwOut.println("<B><U>Monthly statistics</U></B>");
			pwOut.println("<TABLE BORDER=1 CELLSPACING=2 style=\"font-size:75%\">");
			pwOut.println("<TR>");
			//year
			pwOut.println("<TD><B><U>Year</U></B></TD>");		
			//month
			pwOut.println("<TD><B><U>Month</U></B></TD>");
			//invoice total
			pwOut.println("<TD><B><U>Invoice total</U></B></TD>");
			//credit total
			pwOut.println("<TD><B><U>Credit total</U></B></TD>");
			//payment total
			pwOut.println("<TD><B><U>Payment total</U></B></TD>");
			//number of invoices
			pwOut.println("<TD><B><U>Invoices</U></B></TD>");
			//number of credits
			pwOut.println("<TD><B><U>Credits</U></B></TD>");
			//number of payments
			pwOut.println("<TD><B><U>Payments</U></B></TD>");
			//paid invoices
			pwOut.println("<TD><B><U>Paid invoices</U></B></TD>");
			//total days to pay
			pwOut.println("<TD><B><U>Total days to pay</U></B></TD>");
			//average days to pay
			pwOut.println("<TD><B><U>Avg days to pay</U></B></TD>");
			pwOut.println("</TR>");
			while (rs.next()){
				pwOut.println("<TR>");
				pwOut.println("<TD>" + Long.toString(rs.getLong(SMTablearmonthlystatistics.sYear)) + "</TD>");
				pwOut.println("<TD ALIGN=RIGHT>" + Long.toString(rs.getLong(SMTablearmonthlystatistics.sMonth)) + "</TD>");
				pwOut.println("<TD ALIGN=RIGHT>" 
						+ clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(rs.getBigDecimal(SMTablearmonthlystatistics.sInvoiceTotal)) + "</TD>");
				pwOut.println("<TD ALIGN=RIGHT>" 
						+ clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(rs.getBigDecimal(SMTablearmonthlystatistics.sCreditTotal)) + "</TD>");
				pwOut.println("<TD ALIGN=RIGHT>" 
						+ clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(rs.getBigDecimal(SMTablearmonthlystatistics.sPaymentTotal)) + "</TD>");
				pwOut.println("<TD ALIGN=RIGHT>" + Long.toString(rs.getLong(SMTablearmonthlystatistics.sNumberOfInvoices)) + "</TD>");
				pwOut.println("<TD ALIGN=RIGHT>" + Long.toString(rs.getLong(SMTablearmonthlystatistics.sNumberOfCredits)) + "</TD>");
				pwOut.println("<TD ALIGN=RIGHT>" + Long.toString(rs.getLong(SMTablearmonthlystatistics.sNumberOfPayments)) + "</TD>");
				pwOut.println("<TD ALIGN=RIGHT>" + Long.toString(rs.getLong(SMTablearmonthlystatistics.sNumberOfPaidInvoices)) + "</TD>");
				pwOut.println("<TD ALIGN=RIGHT>" + Long.toString(rs.getLong(SMTablearmonthlystatistics.sTotalNumberOfDaysToPay)) + "</TD>");

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
