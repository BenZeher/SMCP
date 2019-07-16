package smcontrolpanel;

import java.io.PrintWriter;
import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;

import javax.servlet.ServletContext;

import SMClasses.SMLogEntry;
import SMDataDefinition.SMTablearcustomer;
import SMDataDefinition.SMTablechangeorders;
import SMDataDefinition.SMTablecriticaldates;
import SMDataDefinition.SMTableinvoicedetails;
import SMDataDefinition.SMTableinvoiceheaders;
import SMDataDefinition.SMTableorderdetails;
import SMDataDefinition.SMTableorderheaders;
import SMDataDefinition.SMTablesalesgroups;
import SMDataDefinition.SMTablesalesperson;
import SMDataDefinition.SMTableworkorders;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsDateAndTimeConversions;
import ServletUtilities.clsManageBigDecimals;
import ServletUtilities.clsStringFunctions;

public class SMOrderHistoryReport extends java.lang.Object{

	private String m_sErrorMessage;
	private static SimpleDateFormat USDateOnlyformatter = new SimpleDateFormat("MM-dd-yyyy");
	private static final String DARK_BG_COLOR = "DCDCDC";
	private BigDecimal dRemainingOrderTotal = new BigDecimal(0.00);
	private BigDecimal bdChangeOrderTotal = new BigDecimal(0);
	public SMOrderHistoryReport(
	){
		m_sErrorMessage = "";
	}
	public boolean processReport(
			String sDBID,
			java.sql.Date datOrderStartingDate,
			java.sql.Date datOrderEndingDate,
			String sUserName,
			String sUserID,
			String sUserFullName,
			PrintWriter out,
			ServletContext context
	){
		
		String SQL = "SELECT"
				+ " " + SMTableorderheaders.TableName + ".*"
				+ ", " + SMTablesalesgroups.TableName + ".*"
				+ ", " + SMTablesalesperson.TableName + ".*"
				+ ", " + SMTablearcustomer.TableName + "." + SMTablearcustomer.iOnHold
				+ " FROM "
				+ SMTableorderheaders.TableName + " LEFT JOIN " 
				+ SMTablesalesgroups.TableName + " ON " 
				+ SMTableorderheaders.TableName + "." + SMTableorderheaders.iSalesGroup + " = "
				+ SMTablesalesgroups.TableName + "." + SMTablesalesgroups.iSalesGroupId
				//join to salespersons table:
				+ " LEFT JOIN " 
				+ SMTablesalesperson.TableName + " ON " 
				+ SMTableorderheaders.TableName + "." + SMTableorderheaders.sSalesperson + " = "
				+ SMTablesalesperson.TableName + "." + SMTablesalesperson.sSalespersonCode
				
				+ " LEFT JOIN " 
				+ SMTablearcustomer.TableName + " ON " 
				+ SMTableorderheaders.TableName + "." + SMTableorderheaders.sCustomerCode + " = "
				+ SMTablearcustomer.TableName + "." + SMTablearcustomer.sCustomerNumber
				
		    	+ " WHERE ("
    			+ "(" + SMTableorderheaders.TableName + "." + SMTableorderheaders.datOrderDate + " <= " + "'" 
    			+ clsDateAndTimeConversions.sqlDateToString(datOrderEndingDate, "yyyy-MM-dd") + " 23:59:59')"
    			+ " AND (" + SMTableorderheaders.TableName + "." + SMTableorderheaders.datOrderDate + " >= " + "'" 
    			+ clsDateAndTimeConversions.sqlDateToString(datOrderStartingDate, "yyyy-MM-dd") + "')"
    		+ ")"
			+ " ORDER BY "
			+ SMTableorderheaders.TableName + "." + SMTableorderheaders.sOrderNumber
		;
		int iCounter = 0;
		long lStartTime = System.currentTimeMillis();
		//System.out.println("[1425658964] - SQL = " + SQL);
		try{
			ResultSet rs = clsDatabaseFunctions.openResultSet(
				SQL, 
				context,	
				sDBID, 
				"MySQL",
				"SMOrderHistoryReport.processReport - user: " + sUserID
				+ " - "
				+ sUserFullName
			);
			
			while(rs.next()){
				dRemainingOrderTotal = BigDecimal.ZERO;
				bdChangeOrderTotal = BigDecimal.ZERO;
				String sTrimmedOrderNumber = rs.getString(SMTableorderheaders.TableName + "." + SMTableorderheaders.strimmedordernumber);
				BigDecimal bdOriginalContractAmount = rs.getBigDecimal(SMTableorderheaders.bdtotalcontractamount);
				String sTruckDays = clsManageBigDecimals.BigDecimalToScaledFormattedString(
						SMTableorderheaders.bdtruckdaysScale, rs.getBigDecimal(
								SMTableorderheaders.bdtruckdays));
				String sTotalMU = clsManageBigDecimals.BigDecimalToScaledFormattedString(
						SMTableorderheaders.bdtotalmarkupScale, rs.getBigDecimal(
								SMTableorderheaders.bdtotalmarkup));
				String sOrderDate = clsDateAndTimeConversions.utilDateToString(rs.getDate(SMTableorderheaders.datOrderDate),"M/d/yyyy");
				bdOriginalContractAmount = rs.getBigDecimal(SMTableorderheaders.bdtotalcontractamount);
				boolean bOrderIsCanceled = false;
				if (rs.getString(SMTableorderheaders.datOrderCanceledDate) == null
						|| rs.getString(SMTableorderheaders.datOrderCanceledDate).compareTo("1899-12-31 00:00:00") <= 0
				){
				}else{
					bOrderIsCanceled = true;
				}
				//Print the order header info:
				printOrderHeader(rs, out);
				printOrderDetails(sTrimmedOrderNumber, sDBID, context, sUserID, sUserFullName, out);
				printChangeOrders(
					sTrimmedOrderNumber, 
					sDBID,
					context,
					sUserID,
					sUserFullName,
					out, 
					bdOriginalContractAmount, 
					sOrderDate,
					sTruckDays,
					sTotalMU
				);
				printInvoices(
					sTrimmedOrderNumber, 
					sDBID, 
					context, 
					sUserName, 
					out,
					bOrderIsCanceled,
					bdOriginalContractAmount,
					bdChangeOrderTotal);
				printCriticalDates(
					sTrimmedOrderNumber, 
					sDBID,
					context,
					out);
						
				printJobCostInformation(
					sTrimmedOrderNumber,
					sDBID,
					context,
					out);
						
				out.println("<BR>");
				iCounter++;
			}
			rs.close();
		}catch (SQLException e){
			m_sErrorMessage = "Error reading resultset - " + e.getMessage();
			return false;
		}

		//Print the report footer:
		out.println("<BR>Printed <B>" + Integer.toString(iCounter) + "</B> order(s) in <B>" 
			+ Long.toString((System.currentTimeMillis() - lStartTime)/1000) + "</B> seconds.");

		SMLogEntry log = new SMLogEntry(sDBID, context);
		log.writeEntry(sUserID, SMLogEntry.LOG_OPERATION_SMPURGEORDERSREPORT, "REPORT", "SM Purge Orders Report", "[1376509332]");

		return true;
	}
	private void printOrderDetails(String sTrimmedOrderNumber, String sDBID, ServletContext context, String sUserID, String sUserFullName, PrintWriter pwOut) throws SQLException {
		
		String sInvoiceComments = "";
		String sTicketComments = "";
		String sBackgroundColor = "";
		String sCellStyle = "";
		int iRowSpan = 0;
		String SQL = "";
		try{
			SQL = "SELECT * FROM " + SMTableorderdetails.TableName
			+ " WHERE ("
			+ SMTableorderdetails.strimmedordernumber + " = " + sTrimmedOrderNumber
			+ ")"
			+ " ORDER BY " + SMTableorderdetails.iLineNumber
			;
			ResultSet rsDetails = clsDatabaseFunctions.openResultSet(
					SQL, 
					context,	
					sDBID, 
					"MySQL",
					"SMOrderHistoryReport.printOrderDetails - user: " + sUserID
					+ " - " + sUserFullName
				);
			
			pwOut.println(
			"<BR><a name=\"OrderDetails\"><B><U>Order Details</U></B><BR>");
			pwOut.println(
			"<TABLE WIDTH=100% BORDER=0 cellspacing=0 cellpadding=1>");

			//pwOut.println("<a name=\"OrderDetails\"><TABLE BORDER=0 WIDTH=100% cellspacing=0 cellpadding=1>");
			pwOut.println("<TR>");
			pwOut.println("<TD ALIGN=RIGHT style=\"border: 1px solid\" bordercolor=\"000\"><FONT SIZE=2><B><BR>Line #</B></TD>");
			pwOut.println("<TD ALIGN=RIGHT style=\"border: 1px solid\" bordercolor=\"000\"><FONT SIZE=2><B>Qty<BR>ordered</TD>");
			pwOut.println("<TD ALIGN=RIGHT style=\"border: 1px solid\" bordercolor=\"000\"><FONT SIZE=2><B>Qty shipped<BR>to date</B></TD>");
			pwOut.println("<TD style=\"border: 1px solid\" bordercolor=\"000\"><FONT SIZE=2><B>Item #</B></TD>");
			pwOut.println("<TD style=\"border: 1px solid\" bordercolor=\"000\"><FONT SIZE=2><B>Description</B></TD>");
			pwOut.println("<TD style=\"border: 1px solid\" bordercolor=\"000\"><FONT SIZE=2><B>U/M</B></TD>");
			pwOut.println("<TD ALIGN=RIGHT style=\"border: 1px solid\" bordercolor=\"000\"><FONT SIZE=2><B>Unit<BR>price</B></TD>");
			pwOut.println("<TD ALIGN=RIGHT style=\"border: 1px solid\" bordercolor=\"000\"><FONT SIZE=2><B>Extended<BR>price</B></TD>");
			pwOut.println("<TD ALIGN=RIGHT style=\"border: 1px solid\" bordercolor=\"000\"><FONT SIZE=2><B>Price<BR>after disc.</B></TD>");
			pwOut.println("<TD ALIGN=RIGHT style=\"border: 1px solid\" bordercolor=\"000\"><FONT SIZE=2><B>Tax</B></TD>");
			pwOut.println("<TD ALIGN=CENTER style=\"border: 1px solid\" bordercolor=\"000\"><FONT SIZE=2><B>Line<BR>booked</B></TD>");
			pwOut.println("<TD ALIGN=CENTER style=\"border: 1px solid\" bordercolor=\"000\"><FONT SIZE=2><B>Line<BR>Cat.</B></TD>");
			pwOut.println("<TD ALIGN=CENTER style=\"border: 1px solid\" bordercolor=\"000\"><FONT SIZE=2><B>Line<BR>Loc.</B></TD>");
			pwOut.println("</TR>");
			boolean bOddRow = true;

			while(rsDetails.next()){
				//Calculate the remaining order total:
				BigDecimal bdRemainingLineTotal = rsDetails.getBigDecimal(
					SMTableorderdetails.TableName + "." + SMTableorderdetails.dQtyOrdered).multiply(
					rsDetails.getBigDecimal(SMTableorderdetails.TableName + "." 
					+ SMTableorderdetails.dOrderUnitPrice)).setScale(2, BigDecimal.ROUND_HALF_UP);
				dRemainingOrderTotal = dRemainingOrderTotal.add(bdRemainingLineTotal);
				if(bOddRow){
					sBackgroundColor = "\"#" + DARK_BG_COLOR + "\"";
				}else{
					sBackgroundColor = "\"#FFFFFF\"";
				}
				//sCellStyle = "style=\"border: 0px solid\" bordercolor=" + sBackgroundColor;
				pwOut.println("<TR bgcolor =" + sBackgroundColor + ">");

				iRowSpan = 2;

				sInvoiceComments = rsDetails.getString(SMTableorderdetails.mInvoiceComments);
				if(sInvoiceComments != null){
					if (sInvoiceComments.compareToIgnoreCase("") !=0){
						iRowSpan++;
					}
				}
				sTicketComments = rsDetails.getString(SMTableorderdetails.mTicketComments);
				if(sTicketComments != null){
					if (sTicketComments.compareToIgnoreCase("") !=0){
						iRowSpan++;
					}
				}

				pwOut.println("<TD ALIGN=RIGHT rowspan=" + Integer.toString(iRowSpan) + " " + sCellStyle 
					+ " style = \"vertical-align:top;\" "
					+ "><FONT SIZE=2><B>" + clsStringFunctions.PadLeft(Integer.toString(
					rsDetails.getInt(SMTableorderdetails.iLineNumber)),"0", 4) + "</B></FONT></TD>");
				pwOut.println("<TD ALIGN=RIGHT " + sCellStyle + "><FONT SIZE=2>" + clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(rsDetails.getBigDecimal(SMTableorderdetails.dQtyOrdered)) + "</FONT></TD>");
				pwOut.println("<TD ALIGN=RIGHT " + sCellStyle + "><FONT SIZE=2>" + clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(rsDetails.getBigDecimal(SMTableorderdetails.dQtyShippedToDate)) + "</FONT></TD>");
				
				String sItemNumber = rsDetails.getString(SMTableorderdetails.sItemNumber);
				String sItemNumberLink = sItemNumber;
			
				pwOut.println("<TD " + sCellStyle + "><FONT SIZE=2>" + sItemNumberLink + "</FONT></TD>");
				pwOut.println("<TD " + sCellStyle + "><FONT SIZE=2>" + rsDetails.getString(SMTableorderdetails.sItemDesc) + "</FONT></TD>");
				pwOut.println("<TD " + sCellStyle + "><FONT SIZE=2>" + rsDetails.getString(SMTableorderdetails.sOrderUnitOfMeasure) + "</FONT></TD>");
				pwOut.println("<TD ALIGN=RIGHT" + sCellStyle + "><FONT SIZE=2>" + clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(rsDetails.getBigDecimal(SMTableorderdetails.dOrderUnitPrice)) + "</FONT></TD>");
				pwOut.println("<TD ALIGN=RIGHT" + sCellStyle + "><FONT SIZE=2>" + clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(rsDetails.getBigDecimal(SMTableorderdetails.dExtendedOrderPrice)) + "</FONT></TD>");
				pwOut.println("<TD ALIGN=CENTER" + sCellStyle + "><FONT SIZE=2>" + clsDateAndTimeConversions.resultsetDateStringToString(rsDetails.getString(SMTableorderdetails.datLineBookedDate)) + "</FONT></TD>");
				pwOut.println("<TD ALIGN=CENTER" + sCellStyle + "><FONT SIZE=2>" + rsDetails.getString(SMTableorderdetails.sItemCategory) + "</FONT></TD>");
				pwOut.println("<TD ALIGN=CENTER" + sCellStyle + "><FONT SIZE=2>" + rsDetails.getString(SMTableorderdetails.sLocationCode) + "</FONT></TD>");
				pwOut.println("</TR>");

				//sBackgroundColor = "bgcolor=\"#EEEEEE\"";
				pwOut.println("<TR bgcolor =" + sBackgroundColor + ">");
				pwOut.println("<TD colspan = 6 " + sCellStyle + "><FONT SIZE=2>Mechanic: " 
						+ rsDetails.getString(SMTableorderdetails.sMechInitial) 
						+ " - "
						+ rsDetails.getString(SMTableorderdetails.sMechFullName)
						+ "</FONT></TD>");
				pwOut.println("<TD colspan = 6 " + sCellStyle + "><FONT SIZE=2>Door label: " 
						+ rsDetails.getString(SMTableorderdetails.sLabel) 
						+ "</FONT></TD>");
				pwOut.println("</TR>");

				if(sInvoiceComments != null){
					if(sInvoiceComments.compareToIgnoreCase("") !=0){
						pwOut.println("<TR bgcolor =" + sBackgroundColor + ">");
						pwOut.println("<TD colspan=12 " + sCellStyle + "><FONT SIZE=2>Invoice detail comments: " + rsDetails.getString(SMTableorderdetails.mInvoiceComments) + "</FONT></TD>");
						pwOut.println("</TR>");
					}
				}

				if(sTicketComments != null){
					if(sTicketComments.compareToIgnoreCase("") !=0){
						pwOut.println("<TR bgcolor =" + sBackgroundColor+ sBackgroundColor + ">");
						pwOut.println("<TD colspan=12 " + sCellStyle + "><FONT SIZE=2>Work order detail comments: " + rsDetails.getString(SMTableorderdetails.mTicketComments) + "</FONT></TD>");
						pwOut.println("</TR>");
					}
				}
				pwOut.println("</TR>");
				bOddRow = ! bOddRow;
			}
			rsDetails.close();
			pwOut.println("</TABLE>");
		}catch(SQLException e){
			throw new SQLException("Error opening details query: " + e.getMessage());
		}
	}
	private void printChangeOrders(
			String sTrimmedOrderNumber, 
			String sDBID,
			ServletContext context,
			String sUserID,
			String sUserFullName,
			PrintWriter pwOut, 
			BigDecimal bdOriginalContractAmount, 
			String sOrderDate,
			String sTruckDays,
			String sTotalMU
		) throws SQLException{
		String sBackgroundColor = "";
		String SQL = "";
		try{
			SQL = "SELECT * FROM " + SMTablechangeorders.TableName
			+ " WHERE ("
			+ SMTablechangeorders.sJobNumber + " = '" + sTrimmedOrderNumber + "'" 
			+ ")"
			+ " ORDER BY " + SMTablechangeorders.datChangeOrderDate
			;
			ResultSet rsChangeOrders = clsDatabaseFunctions.openResultSet(
					SQL, 
					context,	
					sDBID, 
					"MySQL",
					"SMOrderHistoryReport.printChangeOrders - user: " + sUserID
					+ " - "
					+ sUserFullName
				);
			pwOut.println("<a name=\"ChangeOrders\"><B><U>Change Orders</U></B></a>"+ "<BR>");
			pwOut.println("<TABLE BORDER=1 WIDTH=100% cellspacing=0 cellpadding=1>");
			pwOut.println("<TR>");
			pwOut.println("<TD><FONT SIZE=2><B>Date</B></TD>");
			pwOut.println("<TD><FONT SIZE=2><B>C.O. #</TD>");
			pwOut.println("<TD><FONT SIZE=2><B>Description</B></TD>");
			pwOut.println("<TD ALIGN=RIGHT><FONT SIZE=2><B>Truck Days</B></TD>");
			pwOut.println("<TD ALIGN=RIGHT><FONT SIZE=2><B>Total MU</B></TD>");
			pwOut.println("<TD ALIGN=RIGHT><FONT SIZE=2><B>Amount</B></TD>");
			
			pwOut.println("</TR>");

			boolean bOddRow = true;
			if(bOddRow){
				sBackgroundColor = "\"#" + DARK_BG_COLOR + "\"";
			}else{
				sBackgroundColor = "\"#FFFFFF\"";
			}
			//sCellStyle = "style=\"border: 0px solid\" bordercolor=" + sBackgroundColor;
			pwOut.println("<TR bgcolor =" + sBackgroundColor + ">");
			pwOut.println("<TD><FONT SIZE=2><B>" + sOrderDate + "</B></FONT></TD>");
			pwOut.println("<TD><FONT SIZE=2>&nbsp;</FONT></TD>");
			pwOut.println("<TD ALIGN><FONT SIZE=2><B>ORIGINAL CONTRACT AMOUNT</B></FONT></TD>");
			pwOut.println("<TD ALIGN=RIGHT><FONT SIZE=2><B>" + sTruckDays + "</B></FONT></TD>");
			pwOut.println("<TD ALIGN=RIGHT><FONT SIZE=2><B>" + sTotalMU + "</B></FONT></TD>");
			pwOut.println("<TD ALIGN=RIGHT><FONT SIZE=2><B>" 
					+ clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(bdOriginalContractAmount) + "</B></FONT></TD>");
			pwOut.println("</TR>");
			bOddRow = ! bOddRow;
			while(rsChangeOrders.next()){
				bdChangeOrderTotal = bdChangeOrderTotal.add(rsChangeOrders.getBigDecimal(SMTablechangeorders.dAmount.replace("`", "")));
				if(bOddRow){
					sBackgroundColor = "\"#" + DARK_BG_COLOR + "\"";
				}else{
					sBackgroundColor = "\"#FFFFFF\"";
				}
				//sCellStyle = "style=\"border: 0px solid\" bordercolor=" + sBackgroundColor;
				pwOut.println("<TR bgcolor =" + sBackgroundColor + ">");
				pwOut.println("<TD><FONT SIZE=2>" + clsDateAndTimeConversions.utilDateToString(rsChangeOrders.getDate(SMTablechangeorders.datChangeOrderDate.replace("`", "")),"M/d/yyyy") + "</FONT></TD>");
				pwOut.println("<TD><FONT SIZE=2>" + clsManageBigDecimals.doubleTo2DecimalSTDFormat(rsChangeOrders.getDouble(SMTablechangeorders.dChangeOrderNumber.replace("`", ""))) + "</FONT></TD>");
				pwOut.println("<TD ALIGN><FONT SIZE=2>" + rsChangeOrders.getString(SMTablechangeorders.sDesc.replace("`", "")) + "</FONT></TD>");
				pwOut.println("<TD ALIGN=RIGHT><FONT SIZE=2>" + clsManageBigDecimals.doubleTo2DecimalSTDFormat(rsChangeOrders.getDouble(SMTablechangeorders.dTruckDays.replace("`", ""))) + "</FONT></TD>");
				pwOut.println("<TD ALIGN=RIGHT><FONT SIZE=2>" + clsManageBigDecimals.doubleTo2DecimalSTDFormat(rsChangeOrders.getDouble(SMTablechangeorders.dTotalMarkUp.replace("`", ""))) + "</FONT></TD>");
				pwOut.println("<TD ALIGN=RIGHT><FONT SIZE=2>" + clsManageBigDecimals.doubleTo2DecimalSTDFormat(rsChangeOrders.getDouble(SMTablechangeorders.dAmount.replace("`", ""))) + "</FONT></TD>");
				pwOut.println("</TR>");
				bOddRow = ! bOddRow;
			}
			rsChangeOrders.close();
			
			//Print the change order total:
			sBackgroundColor = "\"#FFFFFF\"";
			//sCellStyle = "style=\"border: 0px solid\" bordercolor=" + sBackgroundColor;
			pwOut.println("<TR bgcolor =" + sBackgroundColor + ">");
			pwOut.println(
				"<TD ALIGN=RIGHT COLSPAN = 5><B><FONT SIZE=2>CHANGE ORDER TOTAL:</FONT></B></TD>"
				+ "<TD ALIGN=RIGHT><B><FONT SIZE=2>" 
				+ clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(bdChangeOrderTotal) + "</FONT></B></TD>"
				+ "</TR>"
			);
			
			//Print the total of the contract amount AND the change orders:
			pwOut.println("<TR bgcolor =" + sBackgroundColor + ">");
			pwOut.println(
				"<TD ALIGN=RIGHT COLSPAN = 5><B><FONT SIZE=2>"
				+ "TOTAL CONTRACT AMOUNT (ORIGINAL CONTRACT AMOUNT <I>PLUS</I> CHANGE ORDER TOTAL):"
				+ "</FONT></B></TD>"
				+ "<TD ALIGN=RIGHT><B><FONT SIZE=2>" 
				+ clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(bdChangeOrderTotal.add(bdOriginalContractAmount)) 
				+ "</FONT></B></TD>"
				+ "</TR>"
			);
			pwOut.println("</TABLE>");
		}catch(SQLException e){
			throw new SQLException("Error opening change orders query: " + e.getMessage());
		}
	}
	private void printCriticalDates(
			String sTrimmedOrderNumber, 
			String sDBID,
			ServletContext context,
			PrintWriter pwOut) throws SQLException{
		String SQL = "";
		String sBackgroundColor = "";
		String sCellStyle = "";
		pwOut.println("<BR><B><U>Critical Dates</U></B><BR>");
		
		try{
			SQL = "SELECT * FROM " + SMTablecriticaldates.TableName
			+ " WHERE ("
			+ SMTablecriticaldates.sdocnumber + " = '" + sTrimmedOrderNumber + "'" 
			+ ")"
			+ " ORDER BY " + SMTablecriticaldates.sCriticalDate
			;
			ResultSet rsCriticalDates = clsDatabaseFunctions.openResultSet(SQL, context, sDBID, "MySQL", this.toString() + ".printCriticalDates [1332178466]");

			pwOut.println("<TABLE BORDER=0 WIDTH=100% cellspacing=0 cellpadding=1>");
			pwOut.println("<TR>");
			pwOut.println("<TD style=\"border: 1px solid\" bordercolor=\"000\"><FONT SIZE=2><B>ID</B></TD>");
			pwOut.println("<TD style=\"border: 1px solid\" bordercolor=\"000\"><FONT SIZE=2><B>Date</B></TD>");
			pwOut.println("<TD style=\"border: 1px solid\" bordercolor=\"000\"><FONT SIZE=2><B>Resolved?</TD>");
			pwOut.println("<TD style=\"border: 1px solid\" bordercolor=\"000\"><FONT SIZE=2><B>Responsible</B></TD>");
			pwOut.println("<TD style=\"border: 1px solid\" bordercolor=\"000\"><FONT SIZE=2><B>Assigned&nbsp;by</B></TD>");
			pwOut.println("<TD style=\"border: 1px solid\" bordercolor=\"000\"><FONT SIZE=2><B>Comments</B></TD>");
			pwOut.println("</TR>");

			boolean bOddRow = true;

			while(rsCriticalDates.next()){
				if(bOddRow){
					sBackgroundColor = "\"#" + DARK_BG_COLOR + "\"";
					
				}else{
					sBackgroundColor = "\"#FFFFFF\"";
				}
				//sCellStyle = "style=\"border: 0px solid\" bordercolor=" + sBackgroundColor;
				pwOut.println("<TR bgcolor =" + sBackgroundColor + ">");
				pwOut.println("<TD " + sCellStyle + "><FONT SIZE=2>" 
						+ rsCriticalDates.getInt((SMTablecriticaldates.TableName + "." + SMTablecriticaldates.sId).replace("`", "")) 
						+ "</FONT></TD>");

				pwOut.println("<TD " + sCellStyle + "><FONT SIZE=2><B>" + clsDateAndTimeConversions.utilDateToString(
						rsCriticalDates.getDate(SMTablecriticaldates.sCriticalDate.replace("`", "")),"M/d/yyyy") + "</B></FONT></TD>");
				if(rsCriticalDates.getInt(SMTablecriticaldates.sResolvedFlag.replace("`", "")) == 0){
					pwOut.println("<TD " + sCellStyle + "><FONT SIZE=2>" + "No" + "</FONT></TD>");
				}else{
					pwOut.println("<TD " + sCellStyle + "><FONT SIZE=2>" + "Yes" + "</FONT></TD>");
				}
				pwOut.println("<TD " + sCellStyle + "><FONT SIZE=2>" + rsCriticalDates.getString(
						SMTablecriticaldates.sresponsibleuserfullname.replace("`", "")) + "</FONT></TD>");
				pwOut.println("<TD " + sCellStyle + "><FONT SIZE=2>" + rsCriticalDates.getString(
					SMTablecriticaldates.sassignedbyuserfullname.replace("`", "")) + "</FONT></TD>");
				pwOut.println("<TD " + sCellStyle + "><FONT SIZE=2>" + rsCriticalDates.getString(SMTablecriticaldates.sComments.replace("`", "")) + "</FONT></TD>");
				pwOut.println("</TR>");
				bOddRow = ! bOddRow;
			}
			rsCriticalDates.close();
			pwOut.println("</TABLE>");

		}catch(SQLException e){
			throw new SQLException("Error opening critical dates query: " + e.getMessage());
		}
	}
	private void printJobCostInformation(String sTrimmedOrderNumber, String sDBID, ServletContext context, PrintWriter pwOut) throws SQLException{
		String SQL = "";
		String sBackgroundColor = "";
		String sCellStyle = "";
		int iNumberofColumns = 0;
		pwOut.println("<BR><B><U>Job cost/schedule</U></B><BR>");
		
		try{
			SQL = "SELECT * FROM " + SMTableworkorders.TableName
			+ " WHERE ("
			+ SMTableworkorders.strimmedordernumber + " = '" + sTrimmedOrderNumber + "'" 
			+ ")"
			+ " ORDER BY " + SMTableworkorders.datscheduleddate + ", " + SMTableworkorders.ijoborder
			;
			ResultSet rsJobCost = clsDatabaseFunctions.openResultSet(SQL, context, sDBID, "MySQL", this.toString() + ".printJobCost [1332178467]");

			pwOut.println("<TABLE BORDER=0 WIDTH=100% cellspacing=0 cellpadding=1>");
			pwOut.println("<TR>");
			pwOut.println("<TD style=\"border: 1px solid\" bordercolor=\"000\"><FONT SIZE=2><B>Date/time</B></TD>");
			iNumberofColumns++;
			pwOut.println("<TD style=\"border: 1px solid\" bordercolor=\"000\" ALIGN=RIGHT><FONT SIZE=2><B>Job order</B></TD>");
			iNumberofColumns++;
			pwOut.println("<TD style=\"border: 1px solid\" bordercolor=\"000\"><FONT SIZE=2><B>Type</B></TD>");
			iNumberofColumns++;
			pwOut.println("<TD style=\"border: 1px solid\" bordercolor=\"000\"><FONT SIZE=2><B>Mechanic</TD>");
			iNumberofColumns++;
			pwOut.println("<TD style=\"border: 1px solid\" bordercolor=\"000\"><FONT SIZE=2><B>Assistant</TD>");
			iNumberofColumns++;
			pwOut.println("<TD style=\"border: 1px solid\" bordercolor=\"000\"><FONT SIZE=2><B>Start time</TD>");
			iNumberofColumns++;
			pwOut.println("<TD style=\"border: 1px solid\" bordercolor=\"000\" ALIGN=RIGHT><FONT SIZE=2><B>Hrs.</B></TD>");
			iNumberofColumns++;
			pwOut.println("<TD style=\"border: 1px solid\" bordercolor=\"000\" ALIGN=RIGHT><FONT SIZE=2><B>Travel</B></TD>");
			iNumberofColumns++;
			pwOut.println("<TD style=\"border: 1px solid\" bordercolor=\"000\" ALIGN=RIGHT><FONT SIZE=2><B>Backcharge</B></TD>");
			iNumberofColumns++;
			pwOut.println("<TD style=\"border: 1px solid\" bordercolor=\"000\"><FONT SIZE=2><B>Left previous</B></TD>");
			iNumberofColumns++;
			pwOut.println("<TD style=\"border: 1px solid\" bordercolor=\"000\"><FONT SIZE=2><B>Arrived current</B></TD>");
			iNumberofColumns++;
			pwOut.println("<TD style=\"border: 1px solid\" bordercolor=\"000\"><FONT SIZE=2><B>Left current</B></TD>");
			iNumberofColumns++;
			pwOut.println("<TD style=\"border: 1px solid\" bordercolor=\"000\"><FONT SIZE=2><B>Arrived next</B></TD>");
			iNumberofColumns++;

			pwOut.println("</TR>");

			boolean bOddRow = true;

			while(rsJobCost.next()){
				if(bOddRow){
					sBackgroundColor = "\"#" + DARK_BG_COLOR + "\"";
					
				}else{
					sBackgroundColor = "\"#FFFFFF\"";
				}
				//sCellStyle = "style=\"border: 0px solid\" bordercolor=" + sBackgroundColor;
				pwOut.println("<TR bgcolor =" + sBackgroundColor + ">");
				pwOut.println("<TD " + sCellStyle + "><FONT SIZE=2>" 
						+ clsDateAndTimeConversions.resultsetDateTimeStringToString(rsJobCost.getString(SMTableworkorders.TableName + "." + SMTableworkorders.datscheduleddate)) 
						+ "</FONT></TD>");
				pwOut.println("<TD " + sCellStyle + " ALIGN=RIGHT><FONT SIZE=2>" + 
						Integer.toString(rsJobCost.getInt(SMTableworkorders.TableName + "." + SMTableworkorders.ijoborder)) + "</FONT></TD>");
				pwOut.println("<TD " + sCellStyle + "><FONT SIZE=2>" 
						+ "&nbsp;" 
						+ "</FONT></TD>");
				pwOut.println("<TD " + sCellStyle + "><FONT SIZE=2>" + 
						rsJobCost.getString(SMTableworkorders.TableName + "." + SMTableworkorders.smechanicinitials) + "</FONT></TD>");
				pwOut.println("<TD " + sCellStyle + "><FONT SIZE=2>" + 
						rsJobCost.getString(SMTableworkorders.TableName + "." + SMTableworkorders.sassistant) + "</FONT></TD>");
				pwOut.println("<TD " + sCellStyle + "><FONT SIZE=2>" + 
						rsJobCost.getString(SMTableworkorders.TableName + "." + SMTableworkorders.sstartingtime) + "</FONT></TD>");
				pwOut.println("<TD " + sCellStyle + " ALIGN=RIGHT><FONT SIZE=2>" + clsManageBigDecimals.BigDecimalToScaledFormattedString(
						SMTableworkorders.bdqtyofhoursScale, rsJobCost.getBigDecimal(SMTableworkorders.TableName + "." + SMTableworkorders.bdqtyofhours)) + "</FONT></TD>");
				pwOut.println("<TD " + sCellStyle + " ALIGN=RIGHT><FONT SIZE=2>" + clsManageBigDecimals.BigDecimalToScaledFormattedString(
						SMTableworkorders.bdtravelhoursScale, rsJobCost.getBigDecimal(SMTableworkorders.TableName + "." + SMTableworkorders.bdtravelhours)) + "</FONT></TD>");
				pwOut.println("<TD " + sCellStyle + " ALIGN=RIGHT><FONT SIZE=2>" + clsManageBigDecimals.BigDecimalToScaledFormattedString(
						SMTableworkorders.bdbackchargehoursScale, rsJobCost.getBigDecimal(SMTableworkorders.TableName + "." + SMTableworkorders.bdbackchargehours)) + "</FONT></TD>");
				pwOut.println("<TD " + sCellStyle + "><FONT SIZE=2>" 
						+ clsDateAndTimeConversions.resultsetDateTimeStringToString(rsJobCost.getString(SMTableworkorders.TableName + "." + SMTableworkorders.dattimeleftprevious)) 
						+ "</FONT></TD>");
				pwOut.println("<TD " + sCellStyle + "><FONT SIZE=2>" 
						+ clsDateAndTimeConversions.resultsetDateTimeStringToString(rsJobCost.getString(SMTableworkorders.TableName + "." + SMTableworkorders.dattimearrivedatcurrent)) 
						+ "</FONT></TD>");
				pwOut.println("<TD " + sCellStyle + "><FONT SIZE=2>" 
						+ clsDateAndTimeConversions.resultsetDateTimeStringToString(rsJobCost.getString(SMTableworkorders.TableName + "." + SMTableworkorders.dattimeleftcurrent)) 
						+ "</FONT></TD>");
				pwOut.println("<TD " + sCellStyle + "><FONT SIZE=2>" 
						+ clsDateAndTimeConversions.resultsetDateTimeStringToString(rsJobCost.getString(SMTableworkorders.TableName + "." + SMTableworkorders.dattimearrivedatnext)) 
						+ "</FONT></TD>");

				pwOut.println("</TR>");

				pwOut.println("<TR bgcolor =" + sBackgroundColor + ">");
				pwOut.println("<TD " + sCellStyle + " COLSPAN=" + Integer.toString(iNumberofColumns) + "><FONT SIZE=2><B>" +
					"Comment:&nbsp;</B>" + rsJobCost.getString(SMTableworkorders.TableName + "." + SMTableworkorders.sschedulecomment) + "</FONT></TD>");
				pwOut.println("</TR>");
				
				pwOut.println("<TR bgcolor =" + sBackgroundColor + ">");
				pwOut.println("<TD " + sCellStyle + " COLSPAN=" + Integer.toString(iNumberofColumns) + "><FONT SIZE=2><B>" +
					"Description:&nbsp;</B>" + rsJobCost.getString(SMTableworkorders.TableName + "." + SMTableworkorders.mworkdescription) + "</FONT></TD>");
				pwOut.println("</TR>");
				
				//Here we'll print work order info, if there is any:
				long lWorkOrderID = rsJobCost.getLong(SMTableworkorders.TableName + "." + SMTableworkorders.lid);
				if (lWorkOrderID > 0){
					pwOut.println("<TR><TD COLSPAN=" + Integer.toString(iNumberofColumns) + ">");
					pwOut.println("<TABLE BORDER=0 WIDTH=100% cellspacing=0 cellpadding=1>");

					//Headings:
					pwOut.println("<TR>");
					pwOut.println("<TD style=\"border: 1px solid\" bordercolor=\"000\"><FONT SIZE=2><B>Work order date</B></TD>");
					pwOut.println("<TD style=\"border: 1px solid\" bordercolor=\"000\"><FONT SIZE=2><B>WO #</B></TD>");
					pwOut.println("<TD style=\"border: 1px solid\" bordercolor=\"000\"><FONT SIZE=2><B>Imported?</B></TD>");
					pwOut.println("<TD style=\"border: 1px solid\" bordercolor=\"000\"><FONT SIZE=2><B>Posted?</TD>");
					pwOut.println("<TD style=\"border: 1px solid\" bordercolor=\"000\"><FONT SIZE=2><B>Signed by</TD>");
					pwOut.println("<TD style=\"border: 1px solid\" bordercolor=\"000\"><FONT SIZE=2><B>Date signed</TD>");
					pwOut.println("<TD style=\"border: 1px solid\" bordercolor=\"000\"><FONT SIZE=2><B>Addl work authorized?</TD>");

					pwOut.println("</TR>");
					
					pwOut.println("<TR bgcolor =" + sBackgroundColor + ">");
					pwOut.println("<TD " + sCellStyle + "><FONT SIZE=2>" 
							+ clsDateAndTimeConversions.resultsetDateStringToString(rsJobCost.getString(SMTableworkorders.TableName + "." + SMTableworkorders.dattimedone)) 
							+ "</FONT></TD>");
					pwOut.println("<TD " + sCellStyle + "><FONT SIZE=2>" + 
						Long.toString(rsJobCost.getLong(SMTableworkorders.TableName + "." + SMTableworkorders.lid)) + "</FONT></TD>");
					String sImported = "Y";
					if (rsJobCost.getLong(SMTableworkorders.TableName + "." + SMTableworkorders.iimported) == 0){
						sImported = "N";
					}
					pwOut.println("<TD " + sCellStyle + "><FONT SIZE=2>" + 
						sImported + "</FONT></TD>");
					String sPosted = "Y";
					if (rsJobCost.getLong(SMTableworkorders.TableName + "." + SMTableworkorders.iposted) == 0){
						sPosted = "N";
					}
					pwOut.println("<TD " + sCellStyle + "><FONT SIZE=2>" + 
						sPosted + "</FONT></TD>");
					pwOut.println("<TD " + sCellStyle + "><FONT SIZE=2>" + 
						rsJobCost.getString(SMTableworkorders.TableName + "." + SMTableworkorders.ssignedbyname) + "</FONT></TD>");
					pwOut.println("<TD " + sCellStyle + "><FONT SIZE=2>" 
						+ clsDateAndTimeConversions.resultsetDateStringToString(rsJobCost.getString(SMTableworkorders.TableName + "." + SMTableworkorders.dattimesigned)) 
						+ "</FONT></TD>");
					String sAddlWorkAuthorized = "Y";
					if (rsJobCost.getLong(SMTableworkorders.TableName + "." + SMTableworkorders.iadditionalworkauthorized) == 0){
						sAddlWorkAuthorized = "N";
					}
					pwOut.println("<TD " + sCellStyle + "><FONT SIZE=2>" + 
						sAddlWorkAuthorized + "</FONT></TD>");
					pwOut.println("</TR>");
					
					pwOut.println("<TR bgcolor =" + sBackgroundColor + ">");
					pwOut.println("<TD " + sCellStyle + " COLSPAN=" + Integer.toString(iNumberofColumns) + "><FONT SIZE=2><B>" +
							"Comments:&nbsp;</B>" + rsJobCost.getString(SMTableworkorders.TableName + "." + SMTableworkorders.mcomments) + "</FONT></TD>");
					pwOut.println("</TR>");
					String sAdditionalWork = rsJobCost.getString(SMTableworkorders.TableName + "." + SMTableworkorders.madditionalworkcomments);
					if (sAdditionalWork.compareToIgnoreCase("") != 0){
						pwOut.println("<TR bgcolor =" + sBackgroundColor + ">");
						pwOut.println("<TD " + sCellStyle + " COLSPAN=" + Integer.toString(iNumberofColumns) + "><FONT SIZE=2><B>" +
								"Add'l work comments:&nbsp;</B>" + rsJobCost.getString(SMTableworkorders.TableName + "." + SMTableworkorders.madditionalworkcomments) + "</FONT></TD>");
						pwOut.println("</TR>");
					}
					pwOut.println("</TABLE></TD></TR>");
				}
				
				bOddRow = ! bOddRow;
			}
			rsJobCost.close();
			pwOut.println("</TABLE>");

		}catch(SQLException e){
			throw new SQLException("Error [1425659280] opening job cost query: " + e.getMessage());
		}
	}
	private void printInvoices(
			String sTrimmedOrderNumber, 
			String sDBID, 
			ServletContext context, 
			String sUser, 
			PrintWriter pwOut,
			boolean bOrderIsCanceled,
			BigDecimal bdOriginalContractAmount,
			BigDecimal bdChangeOrderTotal
			) throws SQLException{
		String sBackgroundColor = "";
		String sCellStyle = "";
		BigDecimal bdTotalBilled = new BigDecimal(0.00);
		String SQL = "";
		try{
			SQL = "SELECT"
				+ " " + SMTableinvoicedetails.TableName + "." + SMTableinvoicedetails.sInvoiceNumber
				+ ", " + SMTableinvoiceheaders.TableName + "." + SMTableinvoiceheaders.datInvoiceDate
				+ ", " + SMTableinvoiceheaders.TableName + "." + SMTableinvoiceheaders.iTransactionType
				+ ", SUM(" + SMTableinvoicedetails.TableName + "." 
					+  SMTableinvoicedetails.dExtendedPriceAfterDiscount + ") AS EXTPRICE"
				+ " FROM " + SMTableinvoicedetails.TableName + " INNER JOIN "
				+ SMTableinvoiceheaders.TableName
				+ " ON " + SMTableinvoicedetails.TableName + "." + SMTableinvoicedetails.sInvoiceNumber
				+ " = " + SMTableinvoiceheaders.TableName + "." + SMTableinvoiceheaders.sInvoiceNumber
				+ " WHERE ("
					+ SMTableinvoiceheaders.TableName + "." + SMTableinvoiceheaders.strimmedordernumber
					+ " = '" + sTrimmedOrderNumber + "'"
				+ ")"
				+ " GROUP BY (" + SMTableinvoicedetails.TableName + "." + SMTableinvoicedetails.sInvoiceNumber 
				+ ")"
				;

			ResultSet rsInvoices = clsDatabaseFunctions.openResultSet(SQL, context, sDBID, "MySQL", this.toString() + ".printInvoices [1332178419]");
			pwOut.println(
			"<BR><a name=\"BILLINGSUMMARY\"><B><U>Billing Summary</U></B><BR>");
			pwOut.println("<TABLE WIDTH=100% BORDER=1 cellspacing=0 cellpadding=1>");
			pwOut.println("<TR>");
			pwOut.println("<TD style=\"border: 1px solid; bordercolor: 000; vertical-align:bottom; \"><FONT SIZE=2><B>Invoice date</B></TD>");
			pwOut.println("<TD style=\"border: 1px solid; bordercolor: 000; vertical-align:bottom; \"><FONT SIZE=2><B>Invoice number</B></TD>");
			pwOut.println("<TD style=\"border: 1px solid; bordercolor: 000; vertical-align:bottom; \"><FONT SIZE=2><B>Type</B></TD>");
			pwOut.println("<TD style=\"border: 1px solid; bordercolor: 000; vertical-align:bottom; text-align:right; \"><FONT SIZE=2><B>Amount</B></TD>");
			pwOut.println("</TR>");

			boolean bOddRow = true;
			while(rsInvoices.next()){
				bdTotalBilled = bdTotalBilled.add(rsInvoices.getBigDecimal("EXTPRICE"));
				if(bOddRow){
					sBackgroundColor = "\"#" + DARK_BG_COLOR + "\"";
				}else{
					sBackgroundColor = "\"#FFFFFF\"";
				}
				//sCellStyle = "style=\"border: 0px solid\" bordercolor=" + sBackgroundColor;
				pwOut.println("<TR bgcolor =" + sBackgroundColor + ">");

				pwOut.println("<TD ALIGN=LEFT " + sCellStyle + "><FONT SIZE=2>" 
						+ clsDateAndTimeConversions.utilDateToString(
								rsInvoices.getDate(
										SMTableinvoiceheaders.TableName + "." 
										+ SMTableinvoiceheaders.datInvoiceDate), "M/d/yyyy") + "</FONT></TD>");
				
				String sInvoiceNumber = rsInvoices.getString(SMTableinvoicedetails.TableName + "." 
						+ SMTableinvoicedetails.sInvoiceNumber).trim();
				pwOut.println("<TD ALIGN=LEFT " + sCellStyle + "><FONT SIZE=2>" 
					+ sInvoiceNumber + "</FONT></TD>");

				if (rsInvoices.getInt(SMTableinvoiceheaders.TableName + "." 
						+ SMTableinvoiceheaders.iTransactionType) == SMTableinvoiceheaders.TYPE_INVOICE){
					pwOut.println("<TD><FONT SIZE=2>INVOICE</FONT></TD>");
				}else{
					pwOut.println("<TD><FONT SIZE=2>CREDIT</FONT></TD>");
				}
				
				//EXTPRICE
				pwOut.println("<TD ALIGN=RIGHT " + sCellStyle + "><FONT SIZE=2>" 
						+ clsManageBigDecimals.doubleTo2DecimalSTDFormat(rsInvoices.getDouble("EXTPRICE")) 
						+ "</FONT></TD>");

				pwOut.println("</TR>");

				bOddRow = ! bOddRow;
			}
			rsInvoices.close();
			
			//Show the total amount billed:
			sBackgroundColor = "\"#FFFFFF\"";
			pwOut.println("<TR bgcolor =" + sBackgroundColor + ">");
			pwOut.println(
				"<TD ALIGN=RIGHT COLSPAN=3" + sCellStyle 
				+ "><B><FONT SIZE=2>AMOUNT INVOICED TO DATE:</FONT></B></TD>"
				+ "<TD ALIGN=RIGHT" + sCellStyle + "><B><FONT SIZE=2>" 
					+ clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(bdTotalBilled)
				+ "</B></FONT></TD>"
				+ "</TR>"
			);
			
			//Show the total amount remaining to be billed
			pwOut.println("<TR bgcolor =" + sBackgroundColor + ">");

			if (bOrderIsCanceled){
				dRemainingOrderTotal = BigDecimal.ZERO;
			}			
			pwOut.println(
					"<TD ALIGN=RIGHT COLSPAN=3" + sCellStyle 
					+ "><B><FONT SIZE=2>AMOUNT ON ORDER DETAILS REMAINING TO BE INVOICED:</FONT></B></TD>"
					+ "<TD ALIGN=RIGHT" + sCellStyle + "><B><FONT SIZE=2>"
					+ clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(dRemainingOrderTotal)
					+ "</B></FONT></TD>"
					+ "</TR>"
				);
			
			//Show the total of invoiced and to-be-invoiced:
			pwOut.println("<TR bgcolor =" + sBackgroundColor + ">");
			pwOut.println(
					"<TD ALIGN=RIGHT COLSPAN=3" + sCellStyle 
					+ "><B><FONT SIZE=2>TOTAL ORDER AMOUNT (AMOUNT INVOICED TO DATE <I>PLUS</I> "
					+ "AMOUNT ON ORDER DETAILS REMAINING TO BE INVOICED):</FONT></B></TD>"
					+ "<TD ALIGN=RIGHT" + sCellStyle + "><B><FONT SIZE=2>" 
						+ clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(bdTotalBilled.add(dRemainingOrderTotal))
					+ "</B></FONT></TD>"
					+ "</TR>"
				);
			
			pwOut.println("</TABLE>");
		}catch(SQLException e){
			throw new SQLException("Error opening invoice query: " + e.getMessage());
		}
		//Show any difference between the amount remaining to be billed AND the amount left on the contract here:
		BigDecimal bdTotalContractAmtRemaining = 
			bdOriginalContractAmount.add(bdChangeOrderTotal).subtract(bdTotalBilled) ;
		BigDecimal bdRemainingAmtDifference = bdTotalContractAmtRemaining.subtract(dRemainingOrderTotal);
		if (bdOriginalContractAmount.compareTo(BigDecimal.ZERO) != 0){
			if (bdRemainingAmtDifference.compareTo(BigDecimal.ZERO) > 0){
				pwOut.println("<FONT COLOR=RED><B><I>NOTE:</I> The Total Order Amount in the Billing Summary is "
					+ clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(bdRemainingAmtDifference.abs())
					+ " <I>LESS</I> than the Total Contract Amount in the Change Order log.</B></FONT><BR>"
				);
			}
			if (bdRemainingAmtDifference.compareTo(BigDecimal.ZERO) < 0){
				pwOut.println("<FONT COLOR=RED><B><I>NOTE:</I> The Total Order Amount in the Billing Summary is "
					+ clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(bdRemainingAmtDifference.abs())
					+ " <I>MORE</I> than the Total Contract Amount in the Change Order log.</B></FONT><BR>"
				);
			}
		}

	}
	private void printOrderHeader(ResultSet rsOrder, PrintWriter pwOut) throws SQLException{
		String sDefaultSP = "";

		//Get the project information here:
		try{
		
		String sCustomerCode = rsOrder.getString(SMTableorderheaders.sCustomerCode).trim();
		sDefaultSP = rsOrder.getString(SMTableorderheaders.sSalesperson).trim();
		String sSalespersonName = rsOrder.getString(SMTablesalesperson.sSalespersonFirstName);
		if (sSalespersonName == null){sSalespersonName = "";}
		if (rsOrder.getString(SMTablesalesperson.sSalespersonLastName) != null){
			sSalespersonName += " " + rsOrder.getString(SMTablesalesperson.sSalespersonLastName);
		}
		String sCustomerOnHold = "N";
		if (rsOrder.getLong(SMTablearcustomer.TableName + "." + SMTablearcustomer.iOnHold) != 0){
			sCustomerOnHold = "Y";
		}
		String sOrderDate = clsDateAndTimeConversions.utilDateToString(rsOrder.getDate(SMTableorderheaders.datOrderDate),"M/d/yyyy");
		pwOut.print("<FONT SIZE=2><BR><B>" + "Order number" + ":</B> " + rsOrder.getString(SMTableorderheaders.sOrderNumber).trim() + "&nbsp;&nbsp;"
				+ "<B>Customer code:</B> " + sCustomerCode + "&nbsp;&nbsp;"
				+ "<B>Customer on hold?:</B> " + sCustomerOnHold + "&nbsp;&nbsp;"
				+ "<B>Order date:</B> " + sOrderDate + "&nbsp;&nbsp;"
				+ "<B>Salesperson:</B> " + sDefaultSP + "&nbsp;(" + sSalespersonName + ")&nbsp;"
				+ "<B>Service type:</B> " + rsOrder.getString(SMTableorderheaders.sServiceTypeCodeDescription).trim() + "&nbsp;&nbsp;"
				+ "<B>Sales Group:</B> ");
		if (rsOrder.getInt(SMTableorderheaders.iSalesGroup) == 0){
			pwOut.print("N/A");
		}else{
			pwOut.print(rsOrder.getString(SMTablesalesgroups.sSalesGroupDesc).trim() + "&nbsp;&nbsp;");
		}
		pwOut.println("</FONT><BR>");

		pwOut.println("<TABLE BORDER=0 WIDTH=100%  cellspacing=0 cellpadding=1>");
		pwOut.println("<tr><TD colspan=2><hr></TD></tr>");
		pwOut.println("<TR>");
		String sBillToName = rsOrder.getString(SMTableorderheaders.sBillToName);
		pwOut.println("<TD><FONT SIZE=2><B>Bill to: </B>" + sBillToName + "</FONT></TD>");
		String sShipToName = rsOrder.getString(SMTableorderheaders.sShipToName);
		pwOut.println("<TD><FONT SIZE=2><B>Ship to code - name: </B>" 
				+ rsOrder.getString(SMTableorderheaders.sShipToCode)
				+ " - " + sShipToName + "</FONT></TD>");

		pwOut.println("</TR>");
		pwOut.println("<TR>");
		pwOut.println("<TD><FONT SIZE=2><B>Address 1: </B>" + rsOrder.getString(SMTableorderheaders.sBillToAddressLine1) + "</FONT></TD>");
		pwOut.println("<TD><FONT SIZE=2><B>Address 1: </B>" + rsOrder.getString(SMTableorderheaders.sShipToAddress1) + "</FONT></TD>");
		pwOut.println("</TR>");
		pwOut.println("<TR>");
		pwOut.println("<TD><FONT SIZE=2><B>Address 2: </B>" + rsOrder.getString(SMTableorderheaders.sBillToAddressLine2) + "</FONT></TD>");
		pwOut.println("<TD><FONT SIZE=2><B>Address 2: </B>" + rsOrder.getString(SMTableorderheaders.sShipToAddress2) + "</FONT></TD>");
		pwOut.println("</TR>");
		pwOut.println("<TR>");
		pwOut.println("<TD><FONT SIZE=2><B>Address 3: </B>" + rsOrder.getString(SMTableorderheaders.sBillToAddressLine3) + "</FONT></TD>");
		pwOut.println("<TD><FONT SIZE=2><B>Address 3: </B>" + rsOrder.getString(SMTableorderheaders.sShipToAddress3) + "</FONT></TD>");
		pwOut.println("</TR>");
		pwOut.println("<TR>");
		pwOut.println("<TD><FONT SIZE=2><B>Address 4: </B>" + rsOrder.getString(SMTableorderheaders.sBillToAddressLine4) + "</FONT></TD>");
		pwOut.println("<TD><FONT SIZE=2><B>Address 4: </B>" + rsOrder.getString(SMTableorderheaders.sShipToAddress4) + "</FONT></TD>");
		pwOut.println("</TR>");
		pwOut.println("<TR>");
		pwOut.println("<TD><FONT SIZE=2><B>City: </B>" + rsOrder.getString(SMTableorderheaders.sBillToCity) + "</FONT></TD>");
		pwOut.println("<TD><FONT SIZE=2><B>City: </B>" + rsOrder.getString(SMTableorderheaders.sShipToCity) + "</FONT></TD>");
		pwOut.println("</TR>");
		pwOut.println("<TR>");
		pwOut.println("<TD><FONT SIZE=2><B>State: </B>" + rsOrder.getString(SMTableorderheaders.sBillToState) + "</FONT></TD>");
		pwOut.println("<TD><FONT SIZE=2><B>State: </B>" + rsOrder.getString(SMTableorderheaders.sShipToState) + "</FONT></TD>");
		pwOut.println("</TR>");				
		pwOut.println("<TR>");
		pwOut.println("<TD><FONT SIZE=2><B>Country: </B>" + rsOrder.getString(SMTableorderheaders.sBillToCountry) + "</FONT></TD>");
		pwOut.println("<TD><FONT SIZE=2><B>Country: </B>" + rsOrder.getString(SMTableorderheaders.sShipToCountry) + "</FONT></TD>");
		pwOut.println("</TR>");
		pwOut.println("<TR>");
		pwOut.println("<TD><FONT SIZE=2><B>Postal code: </B>" + rsOrder.getString(SMTableorderheaders.sBillToZip) + "</FONT></TD>");
		pwOut.println("<TD><FONT SIZE=2><B>Postal code: </B>" + rsOrder.getString(SMTableorderheaders.sShipToZip) + "</FONT></TD>");
		pwOut.println("</TR>");
		pwOut.println("<TR>");
		pwOut.println("<TD><FONT SIZE=2><B>Contact: </B>" + rsOrder.getString(SMTableorderheaders.sBillToContact) + "</FONT></TD>");
		pwOut.println("<TD><FONT SIZE=2><B>Contact: </B>" + rsOrder.getString(SMTableorderheaders.sShipToContact) + "</FONT></TD>");
		pwOut.println("</TR>");
		pwOut.println("<TR>");
		pwOut.println("<TD><FONT SIZE=2><B>Phone: </B>" + rsOrder.getString(SMTableorderheaders.sBillToPhone) + "</FONT></TD>");
		pwOut.println("<TD><FONT SIZE=2><B>Phone: </B>" + rsOrder.getString(SMTableorderheaders.sShipToPhone) + "</FONT></TD>");
		pwOut.println("</TR>");
		pwOut.println("<TR>");
		pwOut.println("<TD><FONT SIZE=2><B>2nd phone: </B>" + rsOrder.getString(SMTableorderheaders.ssecondarybilltophone) 
				+ "</FONT></TD>");
		pwOut.println("<TD><FONT SIZE=2><B>2nd phone: </B>" + rsOrder.getString(SMTableorderheaders.ssecondaryshiptophone) 
				+ "</FONT></TD>");
		pwOut.println("</TR>");
		pwOut.println("<TR>");
		pwOut.println("<TD><FONT SIZE=2><B>Fax: </B>" + rsOrder.getString(SMTableorderheaders.sBillToFax) + "</FONT></TD>");
		pwOut.println("<TD><FONT SIZE=2><B>Fax: </B>" + rsOrder.getString(SMTableorderheaders.sShipToFax) + "</FONT></TD>");
		pwOut.println("</TR>");
		pwOut.println("<TR>");
		pwOut.println("<TD><FONT SIZE=2><B>Email: </B>" + rsOrder.getString(SMTableorderheaders.sEmailAddress) + "</FONT></TD>");
		pwOut.println("<TD><FONT SIZE=2><B>Ship to email: </B>" + rsOrder.getString(SMTableorderheaders.sshiptoemail) + "</FONT></TD>");
		pwOut.println("</TR>");
		pwOut.println("<TD>&nbsp;</TD>");
		String sMapAddress = rsOrder.getString(SMTableorderheaders.sShipToAddress1).trim();
		sMapAddress	= sMapAddress.trim() + " " + rsOrder.getString(SMTableorderheaders.sShipToAddress2).trim();
		sMapAddress	= sMapAddress.trim() + " " + rsOrder.getString(SMTableorderheaders.sShipToAddress3).trim();
		sMapAddress	= sMapAddress.trim() + " " + rsOrder.getString(SMTableorderheaders.sShipToAddress4).trim();
		sMapAddress	= sMapAddress.trim() + " " + rsOrder.getString(SMTableorderheaders.sShipToCity).trim();
		sMapAddress	= sMapAddress.trim() + " " + rsOrder.getString(SMTableorderheaders.sShipToState).trim();
		sMapAddress	= sMapAddress.trim() + " " + rsOrder.getString(SMTableorderheaders.sShipToCountry).trim();
		
		pwOut.println("<TD><FONT SIZE=2><B>Map: </B>" + sMapAddress	+ "</FONT></TD>");
		pwOut.println("</TR>");
		pwOut.println("<TR>");
		pwOut.println("<TD>&nbsp;</TD>");
		pwOut.println("<TD><FONT SIZE=2><B>Geocode (latitude/longitude): </B>" 
				+ rsOrder.getString(SMTableorderheaders.sgeocode) + "</FONT></TD>");
		pwOut.println("</TR>");
		pwOut.println("</TABLE>");

		pwOut.println("<TABLE BORDER=0 WIDTH=100%>");
		pwOut.println("<tr><TD colspan=4><hr></TD></tr>");
		pwOut.println("<TR>");
		pwOut.println("<TD><FONT SIZE=2><B>Price level: </B>" + Integer.toString(rsOrder.getInt(SMTableorderheaders.iCustomerDiscountLevel)) + "</FONT></TD>");
		pwOut.println("<TD><FONT SIZE=2><B>Price list: </B>" + rsOrder.getString(SMTableorderheaders.sDefaultPriceListCode) + "</FONT></TD>");
		pwOut.println("<TD><FONT SIZE=2><B>PO Number: </B>" + rsOrder.getString(SMTableorderheaders.sPONumber) + "</FONT></TD>");
		pwOut.println("<TD><FONT SIZE=2><B>Special wage rate: </B>" + rsOrder.getString(SMTableorderheaders.sSpecialWageRate) + "</FONT></TD>");
		pwOut.println("</TR>");
		pwOut.println("<TR>");
		if(rsOrder.getInt(SMTableorderheaders.iOrderType) == 1){
			pwOut.println("<TD><FONT SIZE=2><B>Order type: </B>Active</FONT></TD>");
		}else{
			pwOut.println("<TD><FONT SIZE=2><B>Order type: </B>Standing</FONT></TD>");
		}
		pwOut.println("<TD><FONT SIZE=2><B>Expected ship date: </B>" + clsDateAndTimeConversions.resultsetDateStringToString(rsOrder.getString(SMTableorderheaders.datExpectedShipDate)) + "</FONT></TD>");
		pwOut.println("<TD><FONT SIZE=2><B>Creation date: </B>" + clsDateAndTimeConversions.utilDateToString(rsOrder.getDate(SMTableorderheaders.datOrderCreationDate),"MM/d/yyyy") + "</FONT></TD>");
		pwOut.println("<TD><FONT SIZE=2><B>Terms: </B>" + rsOrder.getString(SMTableorderheaders.sTerms) + "</FONT></TD>");
		pwOut.println("</TR>");
		pwOut.println("<TR>");
		pwOut.println("<TD><FONT SIZE=2><B>Last invoice #: </B>" + rsOrder.getString(SMTableorderheaders.sLastInvoiceNumber) + "</FONT></TD>");
		pwOut.println("<TD><FONT SIZE=2><B>Number of invoices: </B>" + Integer.toString(rsOrder.getInt(SMTableorderheaders.iNumberOfInvoices)) + "</FONT></TD>");
		String sWarrantyExpirationDate = clsDateAndTimeConversions.resultsetDateStringToString(
				rsOrder.getString(SMTableorderheaders.datwarrantyexpiration));
		pwOut.println("<TD><FONT SIZE=2><B>Warranty expiration: </B>" +	sWarrantyExpirationDate	+ "</FONT></TD>");
		pwOut.println("<TD><FONT SIZE=2><B>Location: </B>" + rsOrder.getString(SMTableorderheaders.sLocation) + "</FONT></TD>");
		pwOut.println("</TR>");
		pwOut.println("<TR>");
		if(rsOrder.getInt(SMTableorderheaders.iOnHold) != 0){
			pwOut.println("<TD><FONT SIZE=2><B>Order On hold? </B>YES</FONT></TD>");
		}else{
			pwOut.println("<TD><FONT SIZE=2><B>Order On hold? </B>NO</FONT></TD>");
		}
		
//		String sCompletedDate = "REMOVED";
//		if (rsOrder.getString(SMTableorderheaders.datCompletedDate) == null || 
//				rsOrder.getString(SMTableorderheaders.datCompletedDate).compareTo("1899-12-31 00:00:00") <= 0
//		){
//			sCompletedDate = "N/A";
//		}else{
//			sCompletedDate = USDateOnlyformatter.format(rsOrder.getDate(SMTableorderheaders.datCompletedDate));
//		}
		pwOut.println("<TD><FONT SIZE=2><B>Tax Jurisdiction: </B>" + rsOrder.getString(SMTableorderheaders.staxjurisdiction) + "</FONT></TD>");
//		pwOut.println("<TD><FONT SIZE=2><B>Requisition due day: </B>" 
//				+ Integer.toString(rsOrder.getInt(SMTableorderheaders.iRequisitionDueDay)) + "</FONT></TD>");
		pwOut.println("<TD><FONT SIZE=2><B>Default item category: </B>" + rsOrder.getString(SMTableorderheaders.sDefaultItemCategory) + "</FONT></TD>");
		pwOut.println("</TR>");
		pwOut.println("<TR>");
		pwOut.println("<TD><FONT SIZE=2><B>Created by: </B>" + rsOrder.getString(SMTableorderheaders.sOrderCreatedByFullName) + "</FONT></TD>");
		pwOut.println("<TD><FONT SIZE=2><B>Tax amount: </B>" + clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(rsOrder.getBigDecimal(SMTableorderheaders.bdordertaxamount)) + "</FONT></TD>");
		pwOut.println("<TD><FONT SIZE=2><B>Tax type: </B>" + rsOrder.getString(SMTableorderheaders.staxtype) + "</FONT></TD>");
		pwOut.println("<TD><FONT SIZE=2><B>&nbsp;</B></FONT></TD>");
		pwOut.println("</TR>");
		pwOut.println("<TR>");
		pwOut.println("<TD><FONT SIZE=2><B>Tax base: </B>" + clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(rsOrder.getBigDecimal(SMTableorderheaders.bdtaxbase)) + "</FONT></TD>");
		pwOut.println("<TD><FONT SIZE=2><B>Order source: </B>" + rsOrder.getString(SMTableorderheaders.sOrderSourceDesc) + "</FONT></TD>");
		pwOut.println("<TD><FONT SIZE=2><B>Pre-posting discount desc: </B>" + rsOrder.getString(SMTableorderheaders.sPrePostingInvoiceDiscountDesc) + "</FONT></TD>");
		pwOut.println("<TD><FONT SIZE=2><B>Pre-posting discount %: </B>" + clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(rsOrder.getBigDecimal(SMTableorderheaders.dPrePostingInvoiceDiscountPercentage)) + "</FONT></TD>");
		pwOut.println("</TR>");
		pwOut.println("<TR>");
		pwOut.println("<TD><FONT SIZE=2><B>Pre-posting discount amt: </B>" + clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(rsOrder.getBigDecimal(SMTableorderheaders.dPrePostingInvoiceDiscountAmount)) + "</FONT></TD>");
		pwOut.println("<TD><FONT SIZE=2><B>Last edited by: </B>" + rsOrder.getString(SMTableorderheaders.LASTEDITUSERFULLNAME) + "</FONT></TD>");
		pwOut.println("<TD><FONT SIZE=2><B>Last edited: </B>" + Long.toString(rsOrder.getLong(SMTableorderheaders.LASTEDITDATE)) + "</FONT></TD>");
		pwOut.println("<TD><FONT SIZE=2><B>Control acct set: </B>" + rsOrder.getString(SMTableorderheaders.sCustomerControlAcctSet) + "</FONT></TD>");
		pwOut.println("</TR>");
		pwOut.println("<TR>");
		String sCanceledDate;
		if (rsOrder.getString(SMTableorderheaders.datOrderCanceledDate) == null
				|| rsOrder.getString(SMTableorderheaders.datOrderCanceledDate).compareTo("1899-12-31 00:00:00") <= 0
		){
			sCanceledDate = "N/A";
		}else{
			sCanceledDate = USDateOnlyformatter.format(rsOrder.getDate(SMTableorderheaders.datOrderCanceledDate));
		}
		pwOut.println("<TD><FONT SIZE=2><B>Date canceled: </B>" + sCanceledDate + "</FONT></TD>");
		
		pwOut.println("<TD><FONT SIZE=2><B>Estimated hours: </B>" + clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(rsOrder.getBigDecimal(SMTableorderheaders.dEstimatedHour)) + "</FONT></TD>");
		pwOut.println("</TR>");

		pwOut.println("<TR>");
		pwOut.println("<tr><TD colspan=4><hr></TD></tr>");
		pwOut.println("<TD colspan=\"4\"><FONT SIZE=2><B>Internal notes: </B>" + rsOrder.getString(SMTableorderheaders.mInternalComments) + "</FONT></TD>");
		pwOut.println("</TR>");
		pwOut.println("<TR>");
		pwOut.println("<TD colspan=\"4\"><FONT SIZE=2><B>Invoice notes: </B>" + rsOrder.getString(SMTableorderheaders.mInvoiceComments) + "</FONT></TD>");
		pwOut.println("</TR>");
		pwOut.println("<TR>");
		pwOut.println("<TD colspan=\"4\"><FONT SIZE=2><B>Directions: </B>" + rsOrder.getString(SMTableorderheaders.mDirections) + "</FONT></TD>");
		pwOut.println("</TR>");
		pwOut.println("<TR>");
		pwOut.println("<TD colspan=\"4\"><FONT SIZE=2><B>Work order notes: </B>" + rsOrder.getString(SMTableorderheaders.mTicketComments) + "</FONT></TD>");
		pwOut.println("</TR>");
		
		/* TJR - 10/2/2014 - removed:
		pwOut.println("<TR>");
		String sFieldNotes = rsOrder.getString(SMTableorderheaders.mFieldNotes);
		if (sFieldNotes == null){
			sFieldNotes = "";
		}
		pwOut.println("<TD colspan=\"4\"><FONT SIZE=2><B>Field notes: </B>" + sFieldNotes + "</FONT></TD>");
		pwOut.println("</TR>");
		*/
		pwOut.println("<TR>");
		String sWageScaleNotes = rsOrder.getString(SMTableorderheaders.swagescalenotes);
		pwOut.println("<TD colspan=\"4\"><FONT SIZE=2><B>Wage scale notes: </B>" 
			+ sWageScaleNotes + "</FONT></TD>");
		pwOut.println("</TR>");
		pwOut.println("<TR>");
		pwOut.println("<TD colspan=\"4\"><FONT SIZE=2><B>Carpenter rate: </B>" 
				+ rsOrder.getString(SMTableorderheaders.scarpenterrate) + "</FONT></TD>");
		pwOut.println("</TR>");
		pwOut.println("<TR>");
		pwOut.println("<TD colspan=\"4\"><FONT SIZE=2><B>Laborer rate: </B>" 
				+ rsOrder.getString(SMTableorderheaders.slaborerrate) + "</FONT></TD>");
		pwOut.println("</TR>");
		pwOut.println("<TR>");
		pwOut.println("<TD colspan=\"4\"><FONT SIZE=2><B>Electrician rate: </B>" 
				+ rsOrder.getString(SMTableorderheaders.selectricianrate) + "</FONT></TD>");
		pwOut.println("</TR>");
		pwOut.println("<TR>");
		pwOut.println("<TD colspan=\"4\"><FONT SIZE=2><B>" + SMBidEntry.ParamObjectName + " ID: </B>"
				+ Long.toString(rsOrder.getLong(SMTableorderheaders.lbidid))
				+ "&nbsp;Quote description:&nbsp;"
				+ rsOrder.getString(SMTableorderheaders.squotedescription) + "</FONT></TD>");
		pwOut.println("</TR>");
		
		pwOut.println("<tr><TD colspan=4><hr></TD></tr>");
		pwOut.println("</TABLE>");
		}catch (SQLException e){
			throw new SQLException("Error displaying order header - " + e.getMessage());
		}
	}
	public String getErrorMessage(){
		return m_sErrorMessage;
	}
}
