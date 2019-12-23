package smcontrolpanel;

import java.io.PrintWriter;
import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;

import javax.servlet.ServletContext;

import SMClasses.SMLogEntry;
import SMDataDefinition.SMMasterStyleSheetDefinitions;
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
			"<a name=\"OrderDetails\"><P STYLE = \"font-family:arial;\"><B><U>Order Details</U></B></P>");
			pwOut.println(
			"<TABLE WIDTH=100% CLASS=\"" + SMMasterStyleSheetDefinitions.TABLE_BASIC_WITH_BORDER + "\">");

			//pwOut.println("<a name=\"OrderDetails\"><TABLE BORDER=0 WIDTH=100% cellspacing=0 cellpadding=1>");
			pwOut.println("<TR  CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_HEADING + "\">");
			pwOut.println("<TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD + "\">Line #</TD>");
			pwOut.println("<TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD + "\">Qty Ordered</TD>");
			pwOut.println("<TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD + "\">Qty shipped to date</TD>");
			pwOut.println("<TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD + "\">Item #</TD>");
			pwOut.println("<TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD + "\">Description</TD>");
			pwOut.println("<TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD + "\">U/M</TD>");
			pwOut.println("<TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD + "\">Unit Price</TD>");
			pwOut.println("<TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD + "\">Extended Price</TD>");
			pwOut.println("<TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_CENTER_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD + "\">Line Booked</TD>");
			pwOut.println("<TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_CENTER_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD + "\">Line Cat.</TD>");
			pwOut.println("<TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_CENTER_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD + "\">Line Loc.</TD>");
			pwOut.println("</TR>"); 
			pwOut.println("<TR  CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_HEADING + "\">");
			pwOut.println("<TD COLSPAN=\"11\" CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_ROW_BREAK + "\">&nbsp;</TD>");
			pwOut.println("</TR>"); 


			boolean bOddRow = false;

			while(rsDetails.next()){
				//Calculate the remaining order total:
				BigDecimal bdRemainingLineTotal = rsDetails.getBigDecimal(
					SMTableorderdetails.TableName + "." + SMTableorderdetails.dQtyOrdered).multiply(
					rsDetails.getBigDecimal(SMTableorderdetails.TableName + "." 
					+ SMTableorderdetails.dOrderUnitPrice)).setScale(2, BigDecimal.ROUND_HALF_UP);
				dRemainingOrderTotal = dRemainingOrderTotal.add(bdRemainingLineTotal);
				if(bOddRow){
					pwOut.println("<TR  CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_ROW_ODD + "\">");
				}else{
					pwOut.println("<TR  CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_ROW_EVEN + "\">");
				}

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

				pwOut.println("<TD rowspan=" + Integer.toString(iRowSpan) + " " + sCellStyle 
					+ " CLASS = \" " + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_ALIGN_TOP +  " \" "
					+ "><FONT SIZE=2><B>" + clsStringFunctions.PadLeft(Integer.toString(
					rsDetails.getInt(SMTableorderdetails.iLineNumber)),"0", 4) + "</B></TD>");
				pwOut.println("<TD CLASS= \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\">" + clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(rsDetails.getBigDecimal(SMTableorderdetails.dQtyOrdered)) + "</TD>");
				pwOut.println("<TD CLASS= \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\">" + clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(rsDetails.getBigDecimal(SMTableorderdetails.dQtyShippedToDate)) + "</TD>");
				
				String sItemNumber = rsDetails.getString(SMTableorderdetails.sItemNumber);
				String sItemNumberLink = sItemNumber;
			
				pwOut.println("<TD CLASS= \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\">" + sItemNumberLink + "</TD>");
				pwOut.println("<TD CLASS= \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\">" + rsDetails.getString(SMTableorderdetails.sItemDesc) + "</TD>");
				pwOut.println("<TD CLASS= \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\">" + rsDetails.getString(SMTableorderdetails.sOrderUnitOfMeasure) + "</TD>");
				pwOut.println("<TD CLASS= " + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + ">" +clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(rsDetails.getBigDecimal(SMTableorderdetails.dOrderUnitPrice)) + "</TD>");
				pwOut.println("<TD CLASS= " + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + ">" +clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(rsDetails.getBigDecimal(SMTableorderdetails.dExtendedOrderPrice)) + "</TD>");
				pwOut.println("<TD CLASS= " + SMMasterStyleSheetDefinitions.TABLE_CELL_CENTER_JUSTIFIED_ARIAL_SMALL_WO_BORDER + ">" + clsDateAndTimeConversions.resultsetDateStringToString(rsDetails.getString(SMTableorderdetails.datLineBookedDate)) + "</TD>");
				pwOut.println("<TD CLASS= " + SMMasterStyleSheetDefinitions.TABLE_CELL_CENTER_JUSTIFIED_ARIAL_SMALL_WO_BORDER + ">" + rsDetails.getString(SMTableorderdetails.sItemCategory) + "&nbsp;</TD>");
				pwOut.println("<TD CLASS= " + SMMasterStyleSheetDefinitions.TABLE_CELL_CENTER_JUSTIFIED_ARIAL_SMALL_WO_BORDER + ">" + rsDetails.getString(SMTableorderdetails.sLocationCode) + "&nbsp;</TD>");
				pwOut.println("</TR>");

				if(bOddRow){
					pwOut.println("<TR  CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_ROW_ODD + "\">");
				}else{
					pwOut.println("<TR  CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_ROW_EVEN + "\">");
				}
				pwOut.println("<TD COLSPAN = 6 CLASS=\" " + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\">Technician: " 
						+ rsDetails.getString(SMTableorderdetails.sMechInitial) 
						+ " - "
						+ rsDetails.getString(SMTableorderdetails.sMechFullName)
						+ "</TD>");
				pwOut.println("<TD COLSPAN = 6 CLASS=\" " + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\">Door label: " 
						+ rsDetails.getString(SMTableorderdetails.sLabel) 
						+ "</TD>");
				pwOut.println("</TR>");

				if(sInvoiceComments != null){
					if(sInvoiceComments.compareToIgnoreCase("") !=0){
						if(bOddRow){
							pwOut.println("<TR  CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_ROW_ODD + "\">");
						}else{
							pwOut.println("<TR  CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_ROW_EVEN + "\">");
						}
						pwOut.println("<TD colspan=12 CLASS =\" " + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\">Invoice detail comments: " + rsDetails.getString(SMTableorderdetails.mInvoiceComments) + "</TD>");
						pwOut.println("</TR>");
					}
				}

				if(sTicketComments != null){
					if(sTicketComments.compareToIgnoreCase("") !=0){
						if(bOddRow){
							pwOut.println("<TR  CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_ROW_ODD + "\">");
						}else{
							pwOut.println("<TR  CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_ROW_EVEN + "\">");
						}
						pwOut.println("<TD colspan=12 CLASS = \" " + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\">Work order detail comments: " + rsDetails.getString(SMTableorderdetails.mTicketComments) + "</TD>");
						pwOut.println("</TR>");
					}
				}
				pwOut.println("</TR>");
				bOddRow = ! bOddRow;
			}
			rsDetails.close();
			pwOut.println("</TABLE>");
			pwOut.println("<BR>");
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
			pwOut.println(
			"<a name=\"ChangeOrders\"><P STYLE = \"font-family:arial;\"><B><U>Change Orders</U></B></P>");
			pwOut.println(
			"<TABLE WIDTH=100% CLASS=\"" + SMMasterStyleSheetDefinitions.TABLE_BASIC_WITH_BORDER + "\">");
			
			pwOut.println("<TR  CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_HEADING + "\">");
			pwOut.println("<TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD + "\">Date</TD>");
			pwOut.println("<TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD + "\">C.O. #</TD>");
			pwOut.println("<TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD + "\">Description</TD>");
			pwOut.println("<TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD + "\">Truck Days</TD>");
			pwOut.println("<TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD + "\">Total MU</TD>");
			pwOut.println("<TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD + "\">Amount</TD>");
			pwOut.println("</TR>"); 
			pwOut.println("<TR  CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_HEADING + "\">");
			pwOut.println("<TD COLSPAN=\"6\" CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_ROW_BREAK + "\">&nbsp;</TD>");
			pwOut.println("</TR>"); 
			

			boolean bOddRow = false;
			if(bOddRow){
				pwOut.println("<TR  CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_ROW_ODD + "\">");
			}else{
				pwOut.println("<TR  CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_ROW_EVEN + "\">");
			}

			//sCellStyle = "style=\"border: 0px solid\" bordercolor=" + sBackgroundColor;
			pwOut.println("<TD CLASS= \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\"><B>" + sOrderDate + "</B></TD>");
			pwOut.println("<TD CLASS= \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\">&nbsp;</TD>");
			pwOut.println("<TD CLASS= \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\"><B>ORIGINAL CONTRACT AMOUNT</B></TD>");
			pwOut.println("<TD CLASS= \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\"><B>" + sTruckDays + "</B></TD>");
			pwOut.println("<TD CLASS= \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\"><B>" + sTotalMU + "</B></TD>");
			pwOut.println("<TD CLASS= \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\"><B>" 
					+ clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(bdOriginalContractAmount) + "</B></TD>");
			pwOut.println("</TR>");
			bOddRow = ! bOddRow;
			while(rsChangeOrders.next()){
				bdChangeOrderTotal = bdChangeOrderTotal.add(rsChangeOrders.getBigDecimal(SMTablechangeorders.dAmount.replace("`", "")));
				if(bOddRow){
					pwOut.println("<TR  CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_ROW_ODD + "\">");
				}else{
					pwOut.println("<TR  CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_ROW_EVEN + "\">");
				}
				//sCellStyle = "style=\"border: 0px solid\" bordercolor=" + sBackgroundColor;
				pwOut.println("<TD CLASS= \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\">" + clsDateAndTimeConversions.utilDateToString(rsChangeOrders.getDate(SMTablechangeorders.datChangeOrderDate.replace("`", "")),"M/d/yyyy") + "</TD>");
				pwOut.println("<TD CLASS= \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\">" + clsManageBigDecimals.doubleTo2DecimalSTDFormat(rsChangeOrders.getDouble(SMTablechangeorders.dChangeOrderNumber.replace("`", ""))) + "</TD>");
				pwOut.println("<TD CLASS= \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\">" + rsChangeOrders.getString(SMTablechangeorders.sDesc.replace("`", "")) + "</TD>");
				pwOut.println("<TD CLASS= \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\">" + clsManageBigDecimals.doubleTo2DecimalSTDFormat(rsChangeOrders.getDouble(SMTablechangeorders.dTruckDays.replace("`", ""))) + "</TD>");
				pwOut.println("<TD CLASS= \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\">" + clsManageBigDecimals.doubleTo2DecimalSTDFormat(rsChangeOrders.getDouble(SMTablechangeorders.dTotalMarkUp.replace("`", ""))) + "</TD>");
				pwOut.println("<TD CLASS= \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\">" + clsManageBigDecimals.doubleTo2DecimalSTDFormat(rsChangeOrders.getDouble(SMTablechangeorders.dAmount.replace("`", ""))) + "</TD>");
				pwOut.println("</TR>");
				bOddRow = ! bOddRow;
			}
			rsChangeOrders.close();
			

			pwOut.println("<TR  CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_FOOTER + "\">");
			pwOut.println(
				"<TD CLASS= \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\" COLSPAN = 5><B>CHANGE ORDER TOTAL:</B></TD>"
				+ "<TD CLASS= \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\"><B>" 
				+ clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(bdChangeOrderTotal) + "</B></TD>"
				+ "</TR>"
			);
			
			pwOut.println("<TR  CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_FOOTER + "\">");
			pwOut.println(
				"<TD CLASS= \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\" COLSPAN = 5><B>"
				+ "TOTAL CONTRACT AMOUNT (ORIGINAL CONTRACT AMOUNT <I>PLUS</I> CHANGE ORDER TOTAL):"
				+ "</B></TD>"
				+ "<TD CLASS= \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\"><B><FONT SIZE=2>" 
				+ clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(bdChangeOrderTotal.add(bdOriginalContractAmount)) 
				+ "</B></TD>"
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
		pwOut.println(
		"<P STYLE = \"font-family:arial;\"><B><U>Critical Dates</U></B></P>");
		
		try{
			SQL = "SELECT * FROM " + SMTablecriticaldates.TableName
			+ " WHERE ("
			+ SMTablecriticaldates.sdocnumber + " = '" + sTrimmedOrderNumber + "'" 
			+ ")"
			+ " ORDER BY " + SMTablecriticaldates.sCriticalDate
			;
			ResultSet rsCriticalDates = clsDatabaseFunctions.openResultSet(SQL, context, sDBID, "MySQL", this.toString() + ".printCriticalDates [1332178466]");

			pwOut.println(
			"<TABLE WIDTH=100% CLASS=\"" + SMMasterStyleSheetDefinitions.TABLE_BASIC_WITH_BORDER + "\">");
			
			pwOut.println("<TR  CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_HEADING + "\">");
			pwOut.println("<TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD + "\">ID</TD>");
			pwOut.println("<TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD + "\">Date</TD>");
			pwOut.println("<TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD + "\">Resolved?</TD>");
			pwOut.println("<TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD + "\">Responsible</TD>");
			pwOut.println("<TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD + "\">Assigned&nbsp;by</TD>");
			pwOut.println("<TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD + "\">Comments</TD>");
			pwOut.println("</TR>"); 
			pwOut.println("<TR  CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_HEADING + "\">");
			pwOut.println("<TD COLSPAN=\"6\" CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_ROW_BREAK + "\">&nbsp;</TD>");
			pwOut.println("</TR>"); 


			boolean bOddRow = false;

			while(rsCriticalDates.next()){
				if(bOddRow){
					pwOut.println("<TR  CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_ROW_ODD + "\">");
				}else{
					pwOut.println("<TR  CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_ROW_EVEN + "\">");
				}

				pwOut.println("<TD CLASS=\"" +  SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\"><FONT SIZE=2>" 
						+ rsCriticalDates.getInt((SMTablecriticaldates.TableName + "." + SMTablecriticaldates.sId).replace("`", "")) 
						+ "</TD>");

				pwOut.println("<TD CLASS=\"" +  SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\"><FONT SIZE=2><B>" + clsDateAndTimeConversions.utilDateToString(
						rsCriticalDates.getDate(SMTablecriticaldates.sCriticalDate.replace("`", "")),"M/d/yyyy") + "</B></TD>");
				if(rsCriticalDates.getInt(SMTablecriticaldates.sResolvedFlag.replace("`", "")) == 0){
					pwOut.println("<TD CLASS= \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\">" + "No" + "</TD>");
				}else{
					pwOut.println("<TD CLASS= \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\">" + "Yes" + "</TD>");
				}
				pwOut.println("<TD CLASS= \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\">" + rsCriticalDates.getString(
						SMTablecriticaldates.sresponsibleuserfullname.replace("`", "")) + "</TD>");
				pwOut.println("<TD CLASS= \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\">" + rsCriticalDates.getString(
					SMTablecriticaldates.sassignedbyuserfullname.replace("`", "")) + "</TD>");
				pwOut.println("<TD CLASS= \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\">" + rsCriticalDates.getString(SMTablecriticaldates.sComments.replace("`", "")) + "</TD>");
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
		int iNumberofColumns = 0;
		pwOut.println(
		"<P STYLE = \"font-family:arial;\"><B><U>Job cost/schedule</U></B></P>");
		
		try{
			SQL = "SELECT * FROM " + SMTableworkorders.TableName
			+ " WHERE ("
			+ SMTableworkorders.strimmedordernumber + " = '" + sTrimmedOrderNumber + "'" 
			+ ")"
			+ " ORDER BY " + SMTableworkorders.datscheduleddate + ", " + SMTableworkorders.ijoborder
			;
			ResultSet rsJobCost = clsDatabaseFunctions.openResultSet(SQL, context, sDBID, "MySQL", this.toString() + ".printJobCost [1332178467]");

			pwOut.println(
			"<TABLE WIDTH=100% CLASS=\"" + SMMasterStyleSheetDefinitions.TABLE_BASIC_WITH_BORDER + "\">");
			
			pwOut.println("<TR  CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_HEADING + "\">");
			pwOut.println("<TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD + "\">Date/time</TD>");
			pwOut.println("<TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD + "\">Job order</TD>");
			pwOut.println("<TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD + "\">Type</TD>");
			pwOut.println("<TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD + "\">T</TD>");
			pwOut.println("<TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD + "\">Assistant</TD>");
			pwOut.println("<TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD + "\">Start time</TD>");
			pwOut.println("<TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD + "\">Hrs.</TD>");
			pwOut.println("<TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD + "\">Travel</TD>");
			pwOut.println("<TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD + "\">Backcharge</TD>");
			pwOut.println("<TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD + "\">Left previous</TD>");
			pwOut.println("<TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD + "\">Arrived current</TD>");
			pwOut.println("<TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD + "\">Left current</TD>");
			pwOut.println("<TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD + "\">Arrived next</TD>");
			pwOut.println("</TR>"); 
			pwOut.println("<TR  CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_HEADING + "\">");
			pwOut.println("<TD COLSPAN=\"13\" CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_ROW_BREAK + "\">&nbsp;</TD>");
			pwOut.println("</TR>"); 
			iNumberofColumns = 13;

			boolean bOddRow = false;

			while(rsJobCost.next()){
				if(bOddRow){
					pwOut.println("<TR  CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_ROW_ODD + "\">");
				}else{
					pwOut.println("<TR  CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_ROW_EVEN + "\">");
				}
				pwOut.println("<TD CLASS=\"" +  SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\"><FONT SIZE=2>" 
						+ clsDateAndTimeConversions.resultsetDateTimeStringToString(rsJobCost.getString(SMTableworkorders.TableName + "." + SMTableworkorders.datscheduleddate)) 
						+ "</TD>");
				pwOut.println("<TD CLASS=\"" +  SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\" ALIGN=RIGHT><FONT SIZE=2>" + 
						Integer.toString(rsJobCost.getInt(SMTableworkorders.TableName + "." + SMTableworkorders.ijoborder)) + "</TD>");
				pwOut.println("<TD CLASS=\"" +  SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\"><FONT SIZE=2>" 
						+ "&nbsp;" 
						+ "</TD>");
				pwOut.println("<TD CLASS= \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\">" + 
						rsJobCost.getString(SMTableworkorders.TableName + "." + SMTableworkorders.smechanicinitials) + "</TD>");
				pwOut.println("<TD CLASS= \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\">" + 
						rsJobCost.getString(SMTableworkorders.TableName + "." + SMTableworkorders.sassistant) + "</TD>");
				pwOut.println("<TD CLASS= \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\">" + 
						rsJobCost.getString(SMTableworkorders.TableName + "." + SMTableworkorders.sstartingtime) + "</TD>");
				pwOut.println("<TD CLASS=\"" +  SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\" ALIGN=RIGHT><FONT SIZE=2>" + clsManageBigDecimals.BigDecimalToScaledFormattedString(
						SMTableworkorders.bdqtyofhoursScale, rsJobCost.getBigDecimal(SMTableworkorders.TableName + "." + SMTableworkorders.bdqtyofhours)) + "</TD>");
				pwOut.println("<TD CLASS=\"" +  SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\" ALIGN=RIGHT><FONT SIZE=2>" + clsManageBigDecimals.BigDecimalToScaledFormattedString(
						SMTableworkorders.bdtravelhoursScale, rsJobCost.getBigDecimal(SMTableworkorders.TableName + "." + SMTableworkorders.bdtravelhours)) + "</TD>");
				pwOut.println("<TD CLASS=\"" +  SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\" ALIGN=RIGHT><FONT SIZE=2>" + clsManageBigDecimals.BigDecimalToScaledFormattedString(
						SMTableworkorders.bdbackchargehoursScale, rsJobCost.getBigDecimal(SMTableworkorders.TableName + "." + SMTableworkorders.bdbackchargehours)) + "</TD>");
				pwOut.println("<TD CLASS=\"" +  SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\"><FONT SIZE=2>" 
						+ clsDateAndTimeConversions.resultsetDateTimeStringToString(rsJobCost.getString(SMTableworkorders.TableName + "." + SMTableworkorders.dattimeleftprevious)) 
						+ "</TD>");
				pwOut.println("<TD CLASS=\"" +  SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\"><FONT SIZE=2>" 
						+ clsDateAndTimeConversions.resultsetDateTimeStringToString(rsJobCost.getString(SMTableworkorders.TableName + "." + SMTableworkorders.dattimearrivedatcurrent)) 
						+ "</TD>");
				pwOut.println("<TD CLASS=\"" +  SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\"><FONT SIZE=2>" 
						+ clsDateAndTimeConversions.resultsetDateTimeStringToString(rsJobCost.getString(SMTableworkorders.TableName + "." + SMTableworkorders.dattimeleftcurrent)) 
						+ "</TD>");
				pwOut.println("<TD CLASS=\"" +  SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\"><FONT SIZE=2>" 
						+ clsDateAndTimeConversions.resultsetDateTimeStringToString(rsJobCost.getString(SMTableworkorders.TableName + "." + SMTableworkorders.dattimearrivedatnext)) 
						+ "</TD>");

				pwOut.println("</TR>");

				if(bOddRow){
					pwOut.println("<TR  CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_ROW_ODD + "\">");
				}else{
					pwOut.println("<TR  CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_ROW_EVEN + "\">");
				}
				pwOut.println("<TD CLASS=\"" +  SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\" COLSPAN=" + Integer.toString(iNumberofColumns) + "><FONT SIZE=2><B>" +
					"Comment:&nbsp;</B>" + rsJobCost.getString(SMTableworkorders.TableName + "." + SMTableworkorders.sschedulecomment) + "</TD>");
				pwOut.println("</TR>");
				
				if(bOddRow){
					pwOut.println("<TR  CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_ROW_ODD + "\">");
				}else{
					pwOut.println("<TR  CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_ROW_EVEN + "\">");
				}
				pwOut.println("<TD CLASS=\"" +  SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\" COLSPAN=" + Integer.toString(iNumberofColumns) + "><FONT SIZE=2><B>" +
					"Description:&nbsp;</B>" + rsJobCost.getString(SMTableworkorders.TableName + "." + SMTableworkorders.mworkdescription) + "</TD>");
				pwOut.println("</TR>");
				
				//Here we'll print work order info, if there is any:
				long lWorkOrderID = rsJobCost.getLong(SMTableworkorders.TableName + "." + SMTableworkorders.lid);
				if (lWorkOrderID > 0){
					pwOut.println("<TR><TD COLSPAN=" + Integer.toString(iNumberofColumns) + ">");
					pwOut.println(
			"<TABLE WIDTH=100% CLASS=\"" + SMMasterStyleSheetDefinitions.TABLE_BASIC_WITH_BORDER + "\">");

					if(bOddRow){
						pwOut.println("<TR  CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_ROW_ODD + "\">");
					}else{
						pwOut.println("<TR  CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_ROW_EVEN + "\">");
					}
					pwOut.println("<TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD + "\">Date/time</TD>");
					pwOut.println("<TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD + "\">WO #</TD>");
					pwOut.println("<TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD + "\">Imported?</TD>");
					pwOut.println("<TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD + "\">Posted?</TD>");
					pwOut.println("<TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD + "\">Signed by</TD>");
					pwOut.println("<TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD + "\">Date signed</TD>");
					pwOut.println("<TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD + "\">Addl work authorized?</TD>");
					pwOut.println("</TR>"); 

					
					if(bOddRow){
						pwOut.println("<TR  CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_ROW_ODD + "\">");
					}else{
						pwOut.println("<TR  CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_ROW_EVEN + "\">");
					}
					pwOut.println("<TD CLASS=\"" +  SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\"><FONT SIZE=2>" 
							+ clsDateAndTimeConversions.resultsetDateStringToString(rsJobCost.getString(SMTableworkorders.TableName + "." + SMTableworkorders.dattimedone)) 
							+ "</TD>");
					pwOut.println("<TD CLASS= \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\">" + 
						Long.toString(rsJobCost.getLong(SMTableworkorders.TableName + "." + SMTableworkorders.lid)) + "</TD>");
					String sImported = "Y";
					if (rsJobCost.getLong(SMTableworkorders.TableName + "." + SMTableworkorders.iimported) == 0){
						sImported = "N";
					}
					pwOut.println("<TD CLASS= \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\">" + 
						sImported + "</TD>");
					String sPosted = "Y";
					if (rsJobCost.getLong(SMTableworkorders.TableName + "." + SMTableworkorders.iposted) == 0){
						sPosted = "N";
					}
					pwOut.println("<TD CLASS= \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\">" + 
						sPosted + "</TD>");
					pwOut.println("<TD CLASS= \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\">" + 
						rsJobCost.getString(SMTableworkorders.TableName + "." + SMTableworkorders.ssignedbyname) + "</TD>");
					pwOut.println("<TD CLASS=\"" +  SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\"><FONT SIZE=2>" 
						+ clsDateAndTimeConversions.resultsetDateStringToString(rsJobCost.getString(SMTableworkorders.TableName + "." + SMTableworkorders.dattimesigned)) 
						+ "</TD>");
					String sAddlWorkAuthorized = "Y";
					if (rsJobCost.getLong(SMTableworkorders.TableName + "." + SMTableworkorders.iadditionalworkauthorized) == 0){
						sAddlWorkAuthorized = "N";
					}
					pwOut.println("<TD CLASS= \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\">" + 
						sAddlWorkAuthorized + "</TD>");
					pwOut.println("</TR>");
					
					if(bOddRow){
						pwOut.println("<TR  CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_ROW_ODD + "\">");
					}else{
						pwOut.println("<TR  CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_ROW_EVEN + "\">");
					}
					pwOut.println("<TD CLASS=\"" +  SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\" COLSPAN=" + Integer.toString(iNumberofColumns) + "><FONT SIZE=2><B>" +
							"Comments:&nbsp;</B>" + rsJobCost.getString(SMTableworkorders.TableName + "." + SMTableworkorders.mcomments) + "</TD>");
					pwOut.println("</TR>");
					String sAdditionalWork = rsJobCost.getString(SMTableworkorders.TableName + "." + SMTableworkorders.madditionalworkcomments);
					if (sAdditionalWork.compareToIgnoreCase("") != 0){
						pwOut.println("<TR bgcolor =" + sBackgroundColor + ">");
						pwOut.println("<TD CLASS=\"" +  SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\" COLSPAN=" + Integer.toString(iNumberofColumns) + "><FONT SIZE=2><B>" +
								"Add'l work comments:&nbsp;</B>" + rsJobCost.getString(SMTableworkorders.TableName + "." + SMTableworkorders.madditionalworkcomments) + "</TD>");
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
			"<A NAME=\"BILLINGSUMMARY\"><P STYLE = \"font-family:arial;\"><B><U>Billing Summary</U></B></P></A>");
			
			pwOut.println(
			"<TABLE WIDTH=100% CLASS=\"" + SMMasterStyleSheetDefinitions.TABLE_BASIC_WITH_BORDER + "\">");
			
			pwOut.println("<TR  CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_HEADING + "\">");
			pwOut.println("<TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD + "\">Invoice date</TD>");
			pwOut.println("<TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD + "\">Invoice number</TD>");
			pwOut.println("<TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD + "\">Type</TD>");
			pwOut.println("<TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD + "\">Amount</TD>");
			pwOut.println("</TR>"); 
			pwOut.println("<TR  CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_HEADING + "\">");
			pwOut.println("<TD COLSPAN=\"13\" CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_ROW_BREAK + "\">&nbsp;</TD>");
			pwOut.println("</TR>"); 
			


			boolean bOddRow = false;
			while(rsInvoices.next()){
				bdTotalBilled = bdTotalBilled.add(rsInvoices.getBigDecimal("EXTPRICE"));
				if(bOddRow){
					pwOut.println("<TR  CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_ROW_ODD + "\">");
				}else{
					pwOut.println("<TR  CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_ROW_EVEN + "\">");
				}

				pwOut.println("<TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\">" 
						+ clsDateAndTimeConversions.utilDateToString(
								rsInvoices.getDate(
										SMTableinvoiceheaders.TableName + "." 
										+ SMTableinvoiceheaders.datInvoiceDate), "M/d/yyyy") + "</TD>");
				
				String sInvoiceNumber = rsInvoices.getString(SMTableinvoicedetails.TableName + "." 
						+ SMTableinvoicedetails.sInvoiceNumber).trim();
				pwOut.println("<TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\">" 
					+ sInvoiceNumber + "</TD>");

				if (rsInvoices.getInt(SMTableinvoiceheaders.TableName + "." 
						+ SMTableinvoiceheaders.iTransactionType) == SMTableinvoiceheaders.TYPE_INVOICE){
					pwOut.println("<TD CLASS= \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\">INVOICE</TD>");
				}else{
					pwOut.println("<TD CLASS= \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\">CREDIT</TD>");
				}
				
				//EXTPRICE
				pwOut.println("<TD CLASS= \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\"><FONT SIZE=2>" 
						+ clsManageBigDecimals.doubleTo2DecimalSTDFormat(rsInvoices.getDouble("EXTPRICE")) 
						+ "</TD>");

				pwOut.println("</TR>");

				bOddRow = ! bOddRow;
			}
			rsInvoices.close();
			
			pwOut.println("<TR  CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_FOOTER + "\">");
			pwOut.println(
				"<TD CLASS= \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\" COLSPAN=3" + sCellStyle 
				+ "><B><FONT SIZE=2>AMOUNT INVOICED TO DATE:</B></TD>"
				+ "<TD CLASS= \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\"" + sCellStyle + "><B><FONT SIZE=2>" 
					+ clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(bdTotalBilled)
				+ "</B></TD>"
				+ "</TR>"
			);
			
			pwOut.println("<TR  CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_FOOTER + "\">");

			if (bOrderIsCanceled){
				dRemainingOrderTotal = BigDecimal.ZERO;
			}			
			pwOut.println(
					"<TD CLASS= \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\" COLSPAN=3" + sCellStyle 
					+ "><B><FONT SIZE=2>AMOUNT ON ORDER DETAILS REMAINING TO BE INVOICED:</B></TD>"
					+ "<TD CLASS= \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\"" + sCellStyle + "><B><FONT SIZE=2>"
					+ clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(dRemainingOrderTotal)
					+ "</B></TD>"
					+ "</TR>"
				);
			
			//Show the total of invoiced and to-be-invoiced:
			pwOut.println("<TR  CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_FOOTER + "\">");
			pwOut.println(
					"<TD CLASS= \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\" COLSPAN=3" + sCellStyle 
					+ "><B><FONT SIZE=2>TOTAL ORDER AMOUNT (AMOUNT INVOICED TO DATE <I>PLUS</I> "
					+ "AMOUNT ON ORDER DETAILS REMAINING TO BE INVOICED):</B></TD>"
					+ "<TD CLASS= \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\"><B><FONT SIZE=2>" 
						+ clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(bdTotalBilled.add(dRemainingOrderTotal))
					+ "</B></TD>"
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
		pwOut.println("<TABLE BORDER=0 WIDTH=100%  cellspacing=0 cellpadding=1>");
		pwOut.println("<TR>");
		pwOut.print("<TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\"><B>" + "Order number" + ":</B> " + rsOrder.getString(SMTableorderheaders.sOrderNumber).trim() + "&nbsp;&nbsp;"
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
		pwOut.println("</TABLE>");

		pwOut.println("<TABLE BORDER=0 WIDTH=100%  cellspacing=0 cellpadding=1>");
		pwOut.println("<tr><TD colspan=2><hr></TD></tr>");
		pwOut.println("<TR>");
		String sBillToName = rsOrder.getString(SMTableorderheaders.sBillToName);
		pwOut.println("<TD CLASS= \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\"><B>Bill to: </B>" + sBillToName + "</TD>");
		String sShipToName = rsOrder.getString(SMTableorderheaders.sShipToName);
		pwOut.println("<TD CLASS= \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\"><B>Ship to code - name: </B>" 
				+ rsOrder.getString(SMTableorderheaders.sShipToCode)
				+ " - " + sShipToName + "</TD>");

		pwOut.println("</TR>");
		pwOut.println("<TR>");
		pwOut.println("<TD CLASS= \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\"><B>Address 1: </B>" + rsOrder.getString(SMTableorderheaders.sBillToAddressLine1) + "</TD>");
		pwOut.println("<TD CLASS= \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\"><B>Address 1: </B>" + rsOrder.getString(SMTableorderheaders.sShipToAddress1) + "</TD>");
		pwOut.println("</TR>");
		pwOut.println("<TR>");
		pwOut.println("<TD CLASS= \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\"><B>Address 2: </B>" + rsOrder.getString(SMTableorderheaders.sBillToAddressLine2) + "</TD>");
		pwOut.println("<TD CLASS= \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\"><B>Address 2: </B>" + rsOrder.getString(SMTableorderheaders.sShipToAddress2) + "</TD>");
		pwOut.println("</TR>");
		pwOut.println("<TR>");
		pwOut.println("<TD CLASS= \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\"><B>Address 3: </B>" + rsOrder.getString(SMTableorderheaders.sBillToAddressLine3) + "</TD>");
		pwOut.println("<TD CLASS= \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\"><B>Address 3: </B>" + rsOrder.getString(SMTableorderheaders.sShipToAddress3) + "</TD>");
		pwOut.println("</TR>");
		pwOut.println("<TR>");
		pwOut.println("<TD CLASS= \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\"><B>Address 4: </B>" + rsOrder.getString(SMTableorderheaders.sBillToAddressLine4) + "</TD>");
		pwOut.println("<TD CLASS= \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\"><B>Address 4: </B>" + rsOrder.getString(SMTableorderheaders.sShipToAddress4) + "</TD>");
		pwOut.println("</TR>");
		pwOut.println("<TR>");
		pwOut.println("<TD CLASS= \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\"><B>City: </B>" + rsOrder.getString(SMTableorderheaders.sBillToCity) + "</TD>");
		pwOut.println("<TD CLASS= \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\"><B>City: </B>" + rsOrder.getString(SMTableorderheaders.sShipToCity) + "</TD>");
		pwOut.println("</TR>");
		pwOut.println("<TR>");
		pwOut.println("<TD CLASS= \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\"><B>State: </B>" + rsOrder.getString(SMTableorderheaders.sBillToState) + "</TD>");
		pwOut.println("<TD CLASS= \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\"><B>State: </B>" + rsOrder.getString(SMTableorderheaders.sShipToState) + "</TD>");
		pwOut.println("</TR>");				
		pwOut.println("<TR>");
		pwOut.println("<TD CLASS= \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\"><B>Country: </B>" + rsOrder.getString(SMTableorderheaders.sBillToCountry) + "</TD>");
		pwOut.println("<TD CLASS= \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\"><B>Country: </B>" + rsOrder.getString(SMTableorderheaders.sShipToCountry) + "</TD>");
		pwOut.println("</TR>");
		pwOut.println("<TR>");
		pwOut.println("<TD CLASS= \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\"><B>Postal code: </B>" + rsOrder.getString(SMTableorderheaders.sBillToZip) + "</TD>");
		pwOut.println("<TD CLASS= \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\"><B>Postal code: </B>" + rsOrder.getString(SMTableorderheaders.sShipToZip) + "</TD>");
		pwOut.println("</TR>");
		pwOut.println("<TR>");
		pwOut.println("<TD CLASS= \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\"><B>Contact: </B>" + rsOrder.getString(SMTableorderheaders.sBillToContact) + "</TD>");
		pwOut.println("<TD CLASS= \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\"><B>Contact: </B>" + rsOrder.getString(SMTableorderheaders.sShipToContact) + "</TD>");
		pwOut.println("</TR>");
		pwOut.println("<TR>");
		pwOut.println("<TD CLASS= \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\"><B>Phone: </B>" + rsOrder.getString(SMTableorderheaders.sBillToPhone) + "</TD>");
		pwOut.println("<TD CLASS= \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\"><B>Phone: </B>" + rsOrder.getString(SMTableorderheaders.sShipToPhone) + "</TD>");
		pwOut.println("</TR>");
		pwOut.println("<TR>");
		pwOut.println("<TD CLASS= \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\"><B>2nd phone: </B>" + rsOrder.getString(SMTableorderheaders.ssecondarybilltophone) 
				+ "</TD>");
		pwOut.println("<TD CLASS= \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\"><B>2nd phone: </B>" + rsOrder.getString(SMTableorderheaders.ssecondaryshiptophone) 
				+ "</TD>");
		pwOut.println("</TR>");
		pwOut.println("<TR>");
		pwOut.println("<TD CLASS= \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\"><B>Fax: </B>" + rsOrder.getString(SMTableorderheaders.sBillToFax) + "</TD>");
		pwOut.println("<TD CLASS= \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\"><B>Fax: </B>" + rsOrder.getString(SMTableorderheaders.sShipToFax) + "</TD>");
		pwOut.println("</TR>");
		pwOut.println("<TR>");
		pwOut.println("<TD CLASS= \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\"><B>Email: </B>" + rsOrder.getString(SMTableorderheaders.sEmailAddress) + "</TD>");
		pwOut.println("<TD CLASS= \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\"><B>Ship to email: </B>" + rsOrder.getString(SMTableorderheaders.sshiptoemail) + "</TD>");
		pwOut.println("</TR>");
		pwOut.println("<TD>&nbsp;</TD>");
		String sMapAddress = rsOrder.getString(SMTableorderheaders.sShipToAddress1).trim();
		sMapAddress	= sMapAddress.trim() + " " + rsOrder.getString(SMTableorderheaders.sShipToAddress2).trim();
		sMapAddress	= sMapAddress.trim() + " " + rsOrder.getString(SMTableorderheaders.sShipToAddress3).trim();
		sMapAddress	= sMapAddress.trim() + " " + rsOrder.getString(SMTableorderheaders.sShipToAddress4).trim();
		sMapAddress	= sMapAddress.trim() + " " + rsOrder.getString(SMTableorderheaders.sShipToCity).trim();
		sMapAddress	= sMapAddress.trim() + " " + rsOrder.getString(SMTableorderheaders.sShipToState).trim();
		sMapAddress	= sMapAddress.trim() + " " + rsOrder.getString(SMTableorderheaders.sShipToCountry).trim();
		
		pwOut.println("<TD CLASS= \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\"><B>Map: </B>" + sMapAddress	+ "</TD>");
		pwOut.println("</TR>");
		pwOut.println("<TR>");
		pwOut.println("<TD>&nbsp;</TD>");
		pwOut.println("<TD CLASS= \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\"><B>Geocode (latitude/longitude): </B>" 
				+ rsOrder.getString(SMTableorderheaders.sgeocode) + "</TD>");
		pwOut.println("</TR>");
		pwOut.println("</TABLE>");

		pwOut.println("<TABLE BORDER=0 WIDTH=100%>");
		pwOut.println("<tr><TD colspan=4><hr></TD></tr>");
		pwOut.println("<TR>");
		pwOut.println("<TD CLASS= \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\"><B>Price level: </B>" + Integer.toString(rsOrder.getInt(SMTableorderheaders.iCustomerDiscountLevel)) + "</TD>");
		pwOut.println("<TD CLASS= \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\"><B>Price list: </B>" + rsOrder.getString(SMTableorderheaders.sDefaultPriceListCode) + "</TD>");
		pwOut.println("<TD CLASS= \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\"><B>PO Number: </B>" + rsOrder.getString(SMTableorderheaders.sPONumber) + "</TD>");
		pwOut.println("<TD CLASS= \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\"><B>Special wage rate: </B>" + rsOrder.getString(SMTableorderheaders.sSpecialWageRate) + "</TD>");
		pwOut.println("</TR>");
		pwOut.println("<TR>");
		if(rsOrder.getInt(SMTableorderheaders.iOrderType) == 1){
			pwOut.println("<TD CLASS= \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\"><B>Order type: </B>Active</TD>");
		}else{
			pwOut.println("<TD CLASS= \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\"><B>Order type: </B>Standing</TD>");
		}
		pwOut.println("<TD CLASS= \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\"><B>Expected ship date: </B>" + clsDateAndTimeConversions.resultsetDateStringToString(rsOrder.getString(SMTableorderheaders.datExpectedShipDate)) + "</TD>");
		pwOut.println("<TD CLASS= \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\"><B>Creation date: </B>" + clsDateAndTimeConversions.utilDateToString(rsOrder.getDate(SMTableorderheaders.datOrderCreationDate),"MM/d/yyyy") + "</TD>");
		pwOut.println("<TD CLASS= \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\"><B>Terms: </B>" + rsOrder.getString(SMTableorderheaders.sTerms) + "</TD>");
		pwOut.println("</TR>");
		pwOut.println("<TR>");
		pwOut.println("<TD CLASS= \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\"><B>Last invoice #: </B>" + rsOrder.getString(SMTableorderheaders.sLastInvoiceNumber) + "</TD>");
		pwOut.println("<TD CLASS= \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\"><B>Number of invoices: </B>" + Integer.toString(rsOrder.getInt(SMTableorderheaders.iNumberOfInvoices)) + "</TD>");
		String sWarrantyExpirationDate = clsDateAndTimeConversions.resultsetDateStringToString(
				rsOrder.getString(SMTableorderheaders.datwarrantyexpiration));
		pwOut.println("<TD CLASS= \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\"><B>Warranty expiration: </B>" +	sWarrantyExpirationDate	+ "</TD>");
		pwOut.println("<TD CLASS= \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\"><B>Location: </B>" + rsOrder.getString(SMTableorderheaders.sLocation) + "</TD>");
		pwOut.println("</TR>");
		pwOut.println("<TR>");
		if(rsOrder.getInt(SMTableorderheaders.iOnHold) != 0){
			pwOut.println("<TD CLASS= \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\"><B>Order On hold? </B>YES</TD>");
		}else{
			pwOut.println("<TD CLASS= \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\"><B>Order On hold? </B>NO</TD>");
		}
		
//		String sCompletedDate = "REMOVED";
//		if (rsOrder.getString(SMTableorderheaders.datCompletedDate) == null || 
//				rsOrder.getString(SMTableorderheaders.datCompletedDate).compareTo("1899-12-31 00:00:00") <= 0
//		){
//			sCompletedDate = "N/A";
//		}else{
//			sCompletedDate = USDateOnlyformatter.format(rsOrder.getDate(SMTableorderheaders.datCompletedDate));
//		}
		pwOut.println("<TD CLASS= \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\"><B>Tax Jurisdiction: </B>" + rsOrder.getString(SMTableorderheaders.staxjurisdiction) + "</TD>");
//		pwOut.println("<TD CLASS= \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\"><B>Requisition due day: </B>" 
//				+ Integer.toString(rsOrder.getInt(SMTableorderheaders.iRequisitionDueDay)) + "</TD>");
		pwOut.println("<TD CLASS= \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\"><B>Default item category: </B>" + rsOrder.getString(SMTableorderheaders.sDefaultItemCategory) + "</TD>");
		pwOut.println("</TR>");
		pwOut.println("<TR>");
		pwOut.println("<TD CLASS= \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\"><B>Created by: </B>" + rsOrder.getString(SMTableorderheaders.sOrderCreatedByFullName) + "</TD>");
		pwOut.println("<TD CLASS= \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\"><B>Tax amount: </B>" + clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(rsOrder.getBigDecimal(SMTableorderheaders.bdordertaxamount)) + "</TD>");
		pwOut.println("<TD CLASS= \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\"><B>Tax type: </B>" + rsOrder.getString(SMTableorderheaders.staxtype) + "</TD>");
		pwOut.println("<TD CLASS= \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\"><B>&nbsp;</B></TD>");
		pwOut.println("</TR>");
		pwOut.println("<TR>");
		pwOut.println("<TD CLASS= \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\"><B>Tax base: </B>" + clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(rsOrder.getBigDecimal(SMTableorderheaders.bdtaxbase)) + "</TD>");
		pwOut.println("<TD CLASS= \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\"><B>Order source: </B>" + rsOrder.getString(SMTableorderheaders.sOrderSourceDesc) + "</TD>");
		pwOut.println("<TD CLASS= \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\"><B>Pre-posting discount desc: </B>" + rsOrder.getString(SMTableorderheaders.sPrePostingInvoiceDiscountDesc) + "</TD>");
		pwOut.println("<TD CLASS= \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\"><B>Pre-posting discount %: </B>" + clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(rsOrder.getBigDecimal(SMTableorderheaders.dPrePostingInvoiceDiscountPercentage)) + "</TD>");
		pwOut.println("</TR>");
		pwOut.println("<TR>");
		pwOut.println("<TD CLASS= \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\"><B>Pre-posting discount amt: </B>" + clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(rsOrder.getBigDecimal(SMTableorderheaders.dPrePostingInvoiceDiscountAmount)) + "</TD>");
		pwOut.println("<TD CLASS= \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\"><B>Last edited by: </B>" + rsOrder.getString(SMTableorderheaders.LASTEDITUSERFULLNAME) + "</TD>");
		pwOut.println("<TD CLASS= \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\"><B>Last edited: </B>" + Long.toString(rsOrder.getLong(SMTableorderheaders.LASTEDITDATE)) + "</TD>");
		pwOut.println("<TD CLASS= \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\"><B>Control acct set: </B>" + rsOrder.getString(SMTableorderheaders.sCustomerControlAcctSet) + "</TD>");
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
		pwOut.println("<TD CLASS= \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\"><B>Date canceled: </B>" + sCanceledDate + "</TD>");
		
		pwOut.println("<TD CLASS= \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\"><B>Estimated hours: </B>" + clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(rsOrder.getBigDecimal(SMTableorderheaders.dEstimatedHour)) + "</TD>");
		pwOut.println("</TR>");

		pwOut.println("<TR>");
		pwOut.println("<tr><TD colspan=4><hr></TD></tr>");
		pwOut.println("<TD COLSPAN = \"4\" CLASS= \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\"><B>Internal notes: </B>" + rsOrder.getString(SMTableorderheaders.mInternalComments) + "</FONT></TD>");
		pwOut.println("</TR>");
		pwOut.println("<TR>");
		pwOut.println("<TD COLSPAN = \"4\" CLASS= \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\"><B>Invoice notes: </B>" + rsOrder.getString(SMTableorderheaders.mInvoiceComments) + "</FONT></TD>");
		pwOut.println("</TR>");
		pwOut.println("<TR>");
		pwOut.println("<TD COLSPAN = \"4\" CLASS= \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\"><B>Directions: </B>" + rsOrder.getString(SMTableorderheaders.mDirections) + "</FONT></TD>");
		pwOut.println("</TR>");
		pwOut.println("<TR>");
		pwOut.println("<TD COLSPAN = \"4\" CLASS= \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\"><B>Work order notes: </B>" + rsOrder.getString(SMTableorderheaders.mTicketComments) + "</FONT></TD>");
		pwOut.println("</TR>");
		
		/* TJR - 10/2/2014 - removed:
		pwOut.println("<TR>");
		String sFieldNotes = rsOrder.getString(SMTableorderheaders.mFieldNotes);
		if (sFieldNotes == null){
			sFieldNotes = "";
		}
		pwOut.println("<TD COLSPAN = \"4\" CLASS= \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\"><B>Field notes: </B>" + sFieldNotes + "</FONT></TD>");
		pwOut.println("</TR>");
		*/
		pwOut.println("<TR>");
		String sWageScaleNotes = rsOrder.getString(SMTableorderheaders.swagescalenotes);
		pwOut.println("<TD COLSPAN = \"4\" CLASS= \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\"><B>Wage scale notes: </B>" 
			+ sWageScaleNotes + "</FONT></TD>");
		pwOut.println("</TR>");
		pwOut.println("<TR>");
		pwOut.println("<TD COLSPAN = \"4\" CLASS= \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\"><B>Carpenter rate: </B>" 
				+ rsOrder.getString(SMTableorderheaders.scarpenterrate) + "</FONT></TD>");
		pwOut.println("</TR>");
		pwOut.println("<TR>");
		pwOut.println("<TD COLSPAN = \"4\" CLASS= \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\"><B>Laborer rate: </B>" 
				+ rsOrder.getString(SMTableorderheaders.slaborerrate) + "</FONT></TD>");
		pwOut.println("</TR>");
		pwOut.println("<TR>");
		pwOut.println("<TD COLSPAN = \"4\" CLASS= \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\"><B>Electrician rate: </B>" 
				+ rsOrder.getString(SMTableorderheaders.selectricianrate) + "</FONT></TD>");
		pwOut.println("</TR>");
		pwOut.println("<TR>");
		pwOut.println("<TD COLSPAN = \"4\" CLASS= \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\"><B>" + SMBidEntry.ParamObjectName + " ID: </B>"
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
