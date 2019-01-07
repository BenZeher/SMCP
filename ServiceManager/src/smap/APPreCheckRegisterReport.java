package smap;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.ResultSet;

import javax.servlet.ServletContext;

import SMDataDefinition.SMMasterStyleSheetDefinitions;
import SMDataDefinition.SMTableaptransactions;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsDateAndTimeConversions;
import ServletUtilities.clsManageBigDecimals;
import smcontrolpanel.SMUtilities;

public class APPreCheckRegisterReport  extends java.lang.Object{
	
	BigDecimal bdGrandTotalOriginalAmt;
	BigDecimal bdGrandTotalCurrentAmt;
	BigDecimal bdGrandTotalDiscountAmt;
	BigDecimal bdGrandTotalNetPayable;
	BigDecimal bdGrandTotalPaymentAmt;
	
	public String processReport(
		Connection conn,
		APBatch batch,
		boolean bIncludeLinkToTransactionInformation,
		boolean bIncludeLinkToVendorInfo,
		String sDBID,
		ServletContext context
	) throws Exception{
		
		bdGrandTotalOriginalAmt = new BigDecimal("0.00");
		bdGrandTotalCurrentAmt = new BigDecimal("0.00");
		bdGrandTotalDiscountAmt = new BigDecimal("0.00");
		bdGrandTotalNetPayable = new BigDecimal("0.00");
		bdGrandTotalPaymentAmt = new BigDecimal("0.00");
		
		String s = "";
		
		//Testing:
		//s += "<BR>TEST REPORT...<BR>";

		s += printTableHeading();
		
		s += printColumnHeadings();
		//System.out.println("[1512697501]");
		s += buildReport(batch, conn, bIncludeLinkToTransactionInformation, bIncludeLinkToVendorInfo, sDBID, context);
		//System.out.println("[1512697502]");
		s += printReportTotals(bdGrandTotalOriginalAmt, bdGrandTotalCurrentAmt, bdGrandTotalDiscountAmt, bdGrandTotalNetPayable, bdGrandTotalPaymentAmt);
		//System.out.println("[1512697503]");
		s += printTableFooting();
		
		s += printFootnotes();
		
		return s;
	}
	

	private String buildReport(
		APBatch batch,
		Connection conn,
		boolean bIncludeLinkToTransactionInformation,
		boolean bIncludeLinkToVendorInfo,
		String sDBID,
		ServletContext context) throws Exception{
		
		String s = "";
		
		for (int iEntryNumber = 1; iEntryNumber <= batch.getBatchEntryArray().size(); iEntryNumber++){
			//Print each entry:
			s += printEntry(batch, 
				iEntryNumber, 
				bIncludeLinkToVendorInfo,
				bIncludeLinkToTransactionInformation,
				context, 
				sDBID,
				conn
			);
		}
		
		return s;
	}
	
	private String printEntry(
		APBatch batch, 
		int iEntryNumber, 
		boolean bIncludeLinkToVendorInfo,
		boolean bIncludeLinkToTransactionInformation,
		ServletContext context, 
		String sDBID,
		Connection conn) throws Exception{
		
		APBatchEntry entry = batch.getEntryByEntryNumber(Integer.toString(iEntryNumber));
		String s = "";
		
    	String sVendorLink = entry.getsvendoracct();
    	if(bIncludeLinkToVendorInfo && (context != null)){
    		sVendorLink = "<A HREF=\"" + SMUtilities.getURLLinkBase(context) + "smap." 
	    		+ "APDisplayVendorInformation" 
	    		+ "?" + "VendorNumber" + "=" + entry.getsvendoracct() 
	    		+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
	    		+ "\">"
	    		+ entry.getsvendoracct()
	    		+ "</A>";
    	}
		
		//Print the entry heading line:
		APVendor ven = new APVendor();
		ven.setsvendoracct(entry.getsvendoracct());
		try {
			if(!ven.load(conn)){
				throw new Exception(ven.getErrorMessages());
			}
		} catch (Exception e) {
			throw new Exception("Error [1512667448] - could not load vendor information for vendor '" + entry.getsvendoracct() + "' - " + entry.getsvendorname() + " - " + e.getMessage());
		}
		
		s += printEntryHeadingLine(
			iEntryNumber,
			sVendorLink,
			entry.getsvendorname(),
			ven.getsactive()
		);
		
		//Print the detail lines:
		for (int iLineNumber = 1; iLineNumber <= entry.getLineArray().size(); iLineNumber++){
			APBatchEntryLine line = entry.getLineArray().get(iLineNumber - 1); //Array is zero based
			
			//Have to get info about the 'apply-to' doc here, from aptransactions:
			String SQL = "SELECT"
				+ " " + SMTableaptransactions.datdiscountdate
				+ ", " + SMTableaptransactions.datduedate
				+ ", " + SMTableaptransactions.bdoriginalamt
				+ ", " + SMTableaptransactions.bdcurrentamt
				+ ", " + SMTableaptransactions.bdcurrentdiscountavailable
				+ " FROM " + SMTableaptransactions.TableName
				+ " WHERE ("
					+ "(" + SMTableaptransactions.lid + " = " + line.getslapplytodocid() + ")"
				+ ")"
			;
			try {
				ResultSet rsApplyToTransaction = clsDatabaseFunctions.openResultSet(SQL, conn);
				if (rsApplyToTransaction.next()){
					s += printDetailLine(
						Integer.parseInt(line.getsiapplytodoctype()),
						line.getsapplytodocnumber(),
						line.getslapplytodocid(),
						clsDateAndTimeConversions.resultsetDateStringToFormattedString(rsApplyToTransaction.getString(SMTableaptransactions.TableName + "." + SMTableaptransactions.datdiscountdate)
							, SMUtilities.DATE_FORMAT_FOR_DISPLAY, SMUtilities.EMPTY_DATE_VALUE),
						clsDateAndTimeConversions.resultsetDateStringToFormattedString(rsApplyToTransaction.getString(SMTableaptransactions.TableName + "." + SMTableaptransactions.datduedate)
							, SMUtilities.DATE_FORMAT_FOR_DISPLAY, SMUtilities.EMPTY_DATE_VALUE),
						rsApplyToTransaction.getBigDecimal(SMTableaptransactions.TableName + "." + SMTableaptransactions.bdoriginalamt),
						new BigDecimal(line.getsbdpayableamt().replaceAll(",", "")),
						new BigDecimal(line.getsbddiscountappliedamt().replaceAll(",", "")),
						rsApplyToTransaction.getBigDecimal(SMTableaptransactions.TableName + "." + SMTableaptransactions.bdcurrentamt).subtract(
								rsApplyToTransaction.getBigDecimal(SMTableaptransactions.TableName + "." + SMTableaptransactions.bdcurrentdiscountavailable)),  //Net Payable Amt
						new BigDecimal(line.getsbdamount().replaceAll(",", "")),
						bIncludeLinkToTransactionInformation,
						sDBID,
						context
					);
					
				}else{
					rsApplyToTransaction.close();
					throw new Exception("Could not load record");
				}
				rsApplyToTransaction.close();
			} catch (Exception e) {
				throw new Exception("Error [1512668704] reading AP transaction with lid: '" + line.getslapplytodocid() + "' for vendor '" + entry.getsvendoracct() + " with SQL '" + SQL + "' - " + e.getMessage());
			}
		}

		//Accumulate the entry totals:
		bdGrandTotalPaymentAmt = bdGrandTotalPaymentAmt.add(new BigDecimal( entry.getsentryamount().replaceAll(",", "")));

		s += printEntryTotals(new BigDecimal(entry.getsentryamount().replaceAll(",", "")));
		
		return s;
	}
	
	private String printEntryHeadingLine(
		long lEntryNumber,
		String sVendorAcct,
		String sVendorName,
		String sVendorActive
		) throws Exception{
		String s = "";
		String sVendorIsActive = "Y";
		if (sVendorActive.compareToIgnoreCase("1") == 0){
			sVendorIsActive = "N";
		}
		s += "  <TR class = \"" + SMMasterStyleSheetDefinitions.TABLE_ROW_BACKGROUNDCOLOR_LIGHTGREY + " \" >\n";
		s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD + " \" >"
			+  Long.toString(lEntryNumber)
			+ "</TD>\n"
			
			+ "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD + " \" >"
			+  sVendorAcct
			+ "</TD>\n"
			
			+"    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD + " \" >"
			+  sVendorName
			+ "</TD>\n"

			+"    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD + " \" >"
			+  "&nbsp;"  //Discount date
			+ "</TD>\n"

			+"    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD + " \" >"
			+  sVendorIsActive
			+ "</TD>\n"
			
			+"    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD + " \" >"
			+  "&nbsp;"  //Original Amt
			+ "</TD>\n"

			+"    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD + " \" >"
			+  "&nbsp;"  //Current payable
			+ "</TD>\n"
			
			+"    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD + " \" >"
			+  "&nbsp;"  //Discount
			+ "</TD>\n"

			+"    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD + " \" >"
			+  "&nbsp;"  //Net Payable
			+ "</TD>\n"

			+"    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD + " \" >"
			+  "&nbsp;"  //Payment Limit
			+ "</TD>\n"
			
			+"    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD + " \" >"
			+  "&nbsp;"  //Payment Amt
			+ "</TD>\n"
		;
		s += "  </TR>\n";
		
		return s;
	}
	
	private String printReportTotals(
		BigDecimal bdGrandTotalOriginalAmt,
		BigDecimal bdGrandTotalCurrentAmt,
		BigDecimal bdGrandTotalDiscountAmt,
		BigDecimal bdGrandTotalNetPayable,
		BigDecimal bdGrandTotalPaymentAmt
		
		){
		String s = "";
		String sColSpan = "10";
		
		s += "  <TR class = \"" + SMMasterStyleSheetDefinitions.TABLE_ROW_BACKGROUNDCOLOR_WHITE + " \" >\n";
		s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_FIELDCONTROL_RIGHT_JUSTIFIED + " \" COLSPAN=" + sColSpan + ">"
			+ "<B>Report totals:</B>"
			+ "</TD>\n"
			
			//+ "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + " \" >"
			//+  "<B>" + SMUtilities.BigDecimalTo2DecimalSTDFormat(bdGrandTotalOriginalAmt) + "</B>"
			//+ "</TD>\n"

			//+ "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + " \" >"
			//+  "<B>" + SMUtilities.BigDecimalTo2DecimalSTDFormat(bdGrandTotalCurrentAmt) + "</B>"
			//+ "</TD>\n"
			
			//+ "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + " \" >"
			//+  "<B>" + SMUtilities.BigDecimalTo2DecimalSTDFormat(bdGrandTotalDiscountAmt) + "</B>"
			//+ "</TD>\n"

			//+ "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + " \" >"
			//+  "<B>" + SMUtilities.BigDecimalTo2DecimalSTDFormat(bdGrandTotalNetPayable) + "</B>"
			//+ "</TD>\n"

			//+ "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + " \" >"
			//+  "&nbsp;"
			//+ "</TD>\n"

			+ "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + " \" >"
			+  "<B>" 
			+ clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(bdGrandTotalPaymentAmt)
			+ "</B>"
			+ "</TD>\n"
			
			+ "  </TR>\n"
		;
		
		return s;
	}
	
	private String printColumnHeadings(){
		String s = "";

		s += "  <TR class = \"" + SMMasterStyleSheetDefinitions.TABLE_ROW_BACKGROUNDCOLOR_LIGHTBLUE + " \" >\n";
		s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_LEFT_JUSTIFIED + " \" >"
			+  "Entry<BR>No."
			+ "</TD>\n"
			
			+ "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_LEFT_JUSTIFIED + " \" >"
			+  "Vendor&nbsp;No./<BR>&nbsp;&nbsp;Doc.&nbsp;Type"
			+ "</TD>\n"
			
			+ "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_LEFT_JUSTIFIED + " \" >"
			+  "Payee&nbsp;Name/<BR>&nbsp;&nbsp;Document&nbsp;No."
			+ "</TD>\n"

			+ "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_LEFT_JUSTIFIED + " \" >"
			+  "Discount&nbsp;Date"
			+ "</TD>\n"

			+ "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_LEFT_JUSTIFIED + " \" >"
			+  "Active/<BR>Due&nbsp;Date"
			+ "</TD>\n"
			
			+ "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_RIGHT_JUSTIFIED + " \" >"
			+  "<a href=\"#originalamt\">Original<BR>Amt.<SUP>1</SUP></a>"
			+ "</TD>\n"

			+ "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_RIGHT_JUSTIFIED + " \" >"
			+  "<a href=\"#currentpayable\">Current<BR>Payable<SUP>2</SUP></a>"
			+ "</TD>\n"
			
			+ "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_RIGHT_JUSTIFIED + " \" >"
			+  "<a href=\"#discountamt\">Discount<SUP>3</SUP></a>"
			+ "</TD>\n"

			+ "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_RIGHT_JUSTIFIED + " \" >"
			+  "<a href=\"#netpayable\">Net<BR>Payable<SUP>4</SUP></a>"
			+ "</TD>\n"

			+ "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_RIGHT_JUSTIFIED + " \" >"
			+  "<a href=\"#paymentlimit\">Payment<BR>Limit<SUP>5</SUP></a>"
			
			+ "</TD>\n"
			
			+ "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_RIGHT_JUSTIFIED + " \" >"
			+  "<a href=\"#paymentamt\">Payment<BR>Amt.<SUP>5</SUP></a>"
			+ "</TD>\n"
		;
		s += "  </TR>\n";
		
		return s;
	}
	
	private String printTableHeading(){
		String s = "";
		
		s += "<TABLE class = \"" + SMMasterStyleSheetDefinitions.TABLE_BASIC_WITHOUT_BORDER + "\" >\n";
		
		return s;
	}

	private String printTableFooting(){
		String s = "";
		
		s += "</TABLE>\n";
		
		return s;
	}
	
	private String printDetailLine(
			int iDocType,
			String sDocNumber,
			String sApplyToDocID,
			String sDiscountDate,
			String sDueDate,
			BigDecimal bdOriginalAmt,
			BigDecimal bdCurrentPayable,
			BigDecimal bdDiscountAmt,
			BigDecimal bdNetPayableAmt,
			BigDecimal bdPaymentAmt,
			boolean bIncludeLinkToTransactionInformation,
			String sDBID, 
			ServletContext context
		){
				
		String s = "";
		String sDocNumberLink = sDocNumber;
		if (bIncludeLinkToTransactionInformation && (context != null)){
			sDocNumberLink =
				"<A HREF=\"" + SMUtilities.getURLLinkBase(context) + "smap.APViewTransactionInformation?"
				+ SMTableaptransactions.lid + "=" + sApplyToDocID
				+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
				+ "\">" + sDocNumber + "</A>"
			;
		}
		
		s += "  <TR class = \"" + SMMasterStyleSheetDefinitions.TABLE_ROW_BACKGROUNDCOLOR_LIGHTGREY + " \" >\n";
		
		s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + " \" >"
			+  "&nbsp;"
			+ "</TD>\n"
			
			+ "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + " \" >"
			+  getDocType(iDocType)
			+ "</TD>\n"
			
			+ "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + " \" >"
			+  sDocNumberLink
			+ "</TD>\n"
			
			+ "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + " \" >"
			+  sDiscountDate
			+ "</TD>\n"
			
			+ "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + " \" >"
			+  sDueDate
			+ "</TD>\n"
			
			// 'Original AMT'
			+ "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + " \" >"
			+  clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(bdOriginalAmt)
			+ "</TD>\n"
			
			// 'Current Payable'
			+ "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + " \" >"
			+  clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(bdCurrentPayable)
			+ "</TD>\n"
			
			// 'Discount'
			+ "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + " \" >"
			+  clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(bdDiscountAmt)
			+ "</TD>\n"
			
			// 'Net payable'
			+ "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + " \" >"
			+  clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(bdNetPayableAmt)
			+ "</TD>\n"
			
			+ "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + " \" >"
			+  "&nbsp;"
			+ "</TD>\n"
			
			// 'Payment Amt'
			+ "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + " \" >"
			+  clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(bdPaymentAmt)
			+ "</TD>\n"

			+ "  </TR>\n"
			;
			return s;
		}
		
	private String printEntryTotals(BigDecimal bdPaymentTotalForEntry){
		String s = "";
		int iColSpan = 11;
		s += "  <TR class = \"" + SMMasterStyleSheetDefinitions.TABLE_ROW_BACKGROUNDCOLOR_LIGHTBLUE + " \" >\n";
		s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_FIELDCONTROL_RIGHT_JUSTIFIED + " \" COLSPAN=" + Integer.toString(iColSpan - 1) + ">"
			+ "<B>Payment totals:</B>"
			+ "</TD>\n"
			
			+ "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_FIELDCONTROL_RIGHT_JUSTIFIED + " \" >"
					+ clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(bdPaymentTotalForEntry)
					+ "</TD>\n"
			
			+ "  </TR>\n"
		;
		
		s += "  <TR class = \"" + SMMasterStyleSheetDefinitions.TABLE_ROW_BACKGROUNDCOLOR_BLACK + " \" >\n"
			+ "    <TD COLSPAN = " + Integer.toString(iColSpan) + ">&nbsp;</TD>\n"
			+ "  </TR>\n"
		;
		
		return s;
	}
		
	private String getDocType(int iDocType){
		switch(iDocType){
		case SMTableaptransactions.AP_TRANSACTION_TYPE_INVOICE_CREDITNOTE:
			return "CN";
		case SMTableaptransactions.AP_TRANSACTION_TYPE_INVOICE_DEBITNOTE:
			return "DN";
		case SMTableaptransactions.AP_TRANSACTION_TYPE_INVOICE_INVOICE:
			return "IN";
		case SMTableaptransactions.AP_TRANSACTION_TYPE_PAYMENT_APPLYTO:
			return "AT";
		case SMTableaptransactions.AP_TRANSACTION_TYPE_PAYMENT_MISCPAYMENT:
			return "MI";
		case SMTableaptransactions.AP_TRANSACTION_TYPE_PAYMENT_PAYMENT:
			return "PY";
		case SMTableaptransactions.AP_TRANSACTION_TYPE_PAYMENT_PREPAYMENT:
			return "PP";
		default:
			return "NA";
		}
	}
	
	private String printFootnotes(){
		String s= "";
		
		s += 
				
			"<BR>"
			+ "<a name=\"originalamt\"><B><SUP>1</SUP>&nbsp;Original Amt.</a>:</B>&nbsp;"
			+ "This is the original amount of the transaction; for example, the 'original amt' of an invoice is the total amount on the vendor's invoice." + "\n"
			
			+ "<BR>"
			+ "<a name=\"currentpayable\"><B><SUP>2</SUP>&nbsp;Current Payable</a>:</B>&nbsp;"
			+ "The total amount remaining on the transaction; for example, the 'current amount payable' of an invoice is the total amount left to be paid on the invoice, regardless of due or discount dates." + "\n"
				
			+ "<BR>"
			+ "<a name=\"discountamt\"><B><SUP>3</SUP>&nbsp;Discount</a>:</B>&nbsp;"
			+ "The total amount of discount being taken on this payment (usually against an invoice.)" + "\n"
			
			+ "<BR>"
			+ "<a name=\"netpayable\"><B><SUP>4</SUP>&nbsp;Net Payable</a>:</B>&nbsp;"
			+ "The current amount payable LESS the current discount available (usually the discount amount available on the invoice)" + "\n"
			
			+ "<BR>"
			+ "<a name=\"paymentlimit\"><B><SUP>5</SUP>&nbsp;Payment Limit</a>:</B>&nbsp;"
			+ "The maximum allowed to be paid (not typically used)." + "\n"
			
			+ "<BR>"
			+ "<a name=\"paymentamt\"><B><SUP>6</SUP>&nbsp;Payment Amt.</a>:</B>&nbsp;"
			+ "Amount actually being paid with this payment (the check amount)." + "\n"
			
			+ "<BR>"
		;
		return s;
	}
	
	//Print the legends:
	/*
	private String printDocTypeLegends(){
		String s = "";
		
		s += "<BR><TABLE BORDER=0>\n";
		s += "  <TR>\n";
		for (int i = SMTableaptransactions.AP_TRANSACTION_TYPE_INVOICE_INVOICE;i < SMTableaptransactions.NUMBER_OF_AP_TRANSACTION_TYPES; i++){
			if (getDocType(i).compareToIgnoreCase("NA") != 0){
				s += "    <TD><FONT SIZE=2><I>" + SMTableapbatchentries.getDocumentTypeLabel(i) + " = " + getDocType(i) + "</I></FONT></TD>\n";
			}
		}
		s += "  </TR>\n";
		s += "</TABLE>\n";
		
		return s;
	}
	*/

}
