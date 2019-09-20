package smap;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.ResultSet;

import javax.servlet.ServletContext;

import SMDataDefinition.SMMasterStyleSheetDefinitions;
import SMDataDefinition.SMTableapbatchentries;
import SMDataDefinition.SMTableapbatchentrylines;
import SMDataDefinition.SMTableapchecks;
import SMDataDefinition.SMTableaptransactions;
import SMDataDefinition.SMTableicvendors;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsDateAndTimeConversions;
import ServletUtilities.clsManageBigDecimals;
import smcontrolpanel.SMUtilities;

public class APCheckRegisterReport  extends java.lang.Object{
	
	private static int NUMBER_OF_COLUMNS = 11;
	
	BigDecimal bdGrandTotalPaymentAmt;
	
	public String processReport(
		Connection conn,
		String sBatchNumber,
		boolean bIncludeLinkToTransactionInformation,
		boolean bIncludeLinkToVendorInfo,
		String sDBID,
		ServletContext context
	) throws Exception{
		
		bdGrandTotalPaymentAmt = new BigDecimal("0.00");
		
		String s = "";
		
		//Testing:
		//s += "<BR>TEST REPORT...<BR>";

		s += printTableHeading();
		
		s += printColumnHeadings();
		
		s += buildReport(sBatchNumber, conn, bIncludeLinkToTransactionInformation, bIncludeLinkToVendorInfo, sDBID, context);
		
		s += printReportTotals(bdGrandTotalPaymentAmt);
		
		s += printTableFooting();
		
		s += printFootnotes();
		
		return s;
	}

	private String buildReport(
		String sBatchNumber,
		Connection conn,
		boolean bIncludeLinkToTransactionInformation,
		boolean bIncludeLinkToVendorInfo,
		String sDBID,
		ServletContext context) throws Exception{
		
		String s = "";
		
		String SQL = "SELECT" + "\n"
			+ " " + SMTableapbatchentries.TableName + "." + SMTableapbatchentries.lentrynumber + "\n"
			+ ", " + SMTableapbatchentries.TableName + "." + SMTableapbatchentries.bdentryamount + "\n"
			+ ", " + SMTableapbatchentries.TableName + "." + SMTableapbatchentries.svendoracct + "\n"
			+ ", " + SMTableapbatchentries.TableName + "." + SMTableapbatchentries.svendorname + "\n"
			+ ", " + SMTableapbatchentries.TableName + "." + SMTableapbatchentries.schecknumber + "\n"
			
			+ ", " + SMTableapbatchentrylines.TableName + "." + SMTableapbatchentrylines.bdamount + "\n"
			+ ", " + SMTableapbatchentrylines.TableName + "." + SMTableapbatchentrylines.bdapplieddiscountamt + "\n"
			+ ", " + SMTableapbatchentrylines.TableName + "." + SMTableapbatchentrylines.bdpayableamount + "\n"
			
			+ ", " + SMTableapchecks.TableName + "." + SMTableapchecks.datcheckdate + "\n"
			
			+ ", " + SMTableicvendors.TableName + "." + SMTableicvendors.iactive + "\n"
			
			+ ", " + SMTableaptransactions.TableName + "." +SMTableaptransactions.idoctype + "\n"
			+ ", " + SMTableaptransactions.TableName + "." +SMTableaptransactions.bdcurrentamt + "\n"
			+ ", " + SMTableaptransactions.TableName + "." +SMTableaptransactions.datdiscountdate + "\n"
			+ ", " + SMTableaptransactions.TableName + "." +SMTableaptransactions.datdocdate + "\n"
			+ ", " + SMTableaptransactions.TableName + "." +SMTableaptransactions.datduedate + "\n"
			+ ", " + SMTableaptransactions.TableName + "." +SMTableaptransactions.lid + "\n"
			+ ", " + SMTableaptransactions.TableName + "." +SMTableaptransactions.sdocnumber + "\n"
			
			+ " FROM " + SMTableapbatchentrylines.TableName + "\n"
			+ " LEFT JOIN " + SMTableapbatchentries.TableName + " ON (" + SMTableapbatchentrylines.TableName + "." + SMTableapbatchentrylines.lbatchnumber
				+ "=" + SMTableapbatchentries.TableName + "." + SMTableapbatchentries.lbatchnumber + ")\n"
				+ " AND (" + SMTableapbatchentrylines.TableName + "." + SMTableapbatchentrylines.lentrynumber
				+ "=" + SMTableapbatchentries.TableName + "." + SMTableapbatchentries.lentrynumber + ")\n"
			
			+ " LEFT JOIN " + SMTableapchecks.TableName + "\n"
				+ " ON (" + SMTableapchecks.TableName + "." + SMTableapchecks.lbatchnumber + " = " + SMTableapbatchentrylines.TableName + "." + SMTableapbatchentrylines.lbatchnumber + ")\n"
				+ " AND (" + SMTableapchecks.TableName + "." + SMTableapchecks.lentrynumber + " = " + SMTableapbatchentrylines.TableName + "." + SMTableapbatchentrylines.lentrynumber + ")\n"
			+ " LEFT JOIN " + SMTableicvendors.TableName + "\n"
			+ " ON " + SMTableapbatchentries.TableName + "." + SMTableapbatchentries.svendoracct + " = " + SMTableicvendors.TableName + "." + SMTableicvendors.svendoracct + "\n"
			+ " LEFT JOIN " + SMTableaptransactions.TableName + "\n"
			+ " ON (" + SMTableapbatchentrylines.TableName + "." + SMTableapbatchentrylines.sapplytodocnumber + " = " + SMTableaptransactions.TableName + "." + SMTableaptransactions.sdocnumber + ")" + "\n"
			+ " AND (" + SMTableapbatchentries.TableName + "." + SMTableapbatchentries.svendoracct + " = " + SMTableaptransactions.TableName + "." + SMTableaptransactions.svendor + ")" + "\n"
			
			+ " WHERE (" + "\n"
				+ "(" + SMTableapbatchentrylines.TableName + "." + SMTableapbatchentrylines.lbatchnumber + " = " + sBatchNumber + ")" + "\n"
			+ ")" + "\n"
			+ " ORDER BY " + SMTableapbatchentries.TableName + "." + SMTableapbatchentries.svendoracct + "\n"
				+ ", " + SMTableapbatchentries.TableName + "." + SMTableapbatchentries.lentrynumber + "\n"
				+ ", " + SMTableapbatchentrylines.TableName + "." + SMTableapbatchentrylines.llinenumber + "\n"
		;
		
		//System.out.println("[1501085690] - " + SQL);
		long lCurrentEntryNumber = 0L;
		BigDecimal bdEntryPayableTotal = new BigDecimal("0.00");
		BigDecimal bdEntryDiscountTotal = new BigDecimal("0.00");
		BigDecimal bdEntryNetPaymentTotal = new BigDecimal("0.00");
		BigDecimal bdEntryAdjustmentTotal = new BigDecimal("0.00");
		BigDecimal bdLastEntryPaymentAmt = new BigDecimal("0.00");
		try {
			ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
			while (rs.next()){
				
	        	String sVendorLink = rs.getString(SMTableapbatchentries.TableName + "." + SMTableapbatchentries.svendoracct);
	        	if(bIncludeLinkToVendorInfo){
	        		sVendorLink = "<A HREF=\"" + SMUtilities.getURLLinkBase(context) + "smap." 
	    	    		+ "APDisplayVendorInformation" 
	    	    		+ "?" + "VendorNumber" + "=" + rs.getString(SMTableapbatchentries.TableName + "." + SMTableapbatchentries.svendoracct) 
	    	    		+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
	    	    		+ "\">"
	    	    		+ rs.getString(SMTableapbatchentries.TableName + "." + SMTableapbatchentries.svendoracct)
	    	    		+ "</A>";
	        	}
				
				if (rs.getLong(SMTableapbatchentries.TableName + "." + SMTableapbatchentries.lentrynumber) != lCurrentEntryNumber){
					//If there is a current entry, then print the entry totals here:
					if (lCurrentEntryNumber != 0L){
						s += printEntryTotals(
							bdEntryPayableTotal,
							bdEntryDiscountTotal,
							bdEntryNetPaymentTotal,
							bdEntryAdjustmentTotal,
							bdLastEntryPaymentAmt
						);
						bdEntryPayableTotal = BigDecimal.ZERO;
						bdEntryDiscountTotal = BigDecimal.ZERO;
						bdEntryNetPaymentTotal = BigDecimal.ZERO;
						bdEntryAdjustmentTotal = BigDecimal.ZERO;
						bdLastEntryPaymentAmt = BigDecimal.ZERO;
					}
					
					//Print the vendor heading line:
					s += printEntryHeadingLine(
						sVendorLink,
						rs.getString(SMTableapbatchentries.TableName + "." + SMTableapbatchentries.svendorname),
						rs.getInt(SMTableicvendors.TableName + "." + SMTableicvendors.iactive)
					);
					
					//Accumulate the entry totals:
					bdGrandTotalPaymentAmt = bdGrandTotalPaymentAmt.add(rs.getBigDecimal(SMTableapbatchentries.TableName + "." + SMTableapbatchentries.bdentryamount));
				}

				//Print the detail line:
				String sCheckDate = "N/A";
				if (
					(rs.getString(SMTableapchecks.TableName + "." + SMTableapchecks.datcheckdate) != null)
					&& (rs.getString(SMTableapchecks.TableName + "." + SMTableapchecks.datcheckdate).compareToIgnoreCase("") != 0)	
				){
					sCheckDate = clsDateAndTimeConversions.resultsetDateStringToFormattedString(
						rs.getString(SMTableapchecks.TableName + "." + SMTableapchecks.datcheckdate),
						SMUtilities.DATE_FORMAT_FOR_DISPLAY, 
						SMUtilities.EMPTY_DATE_VALUE)
					;
				}
				
				//Get the dollar amts from the check line:
				BigDecimal bdInvoicePayableAmt = rs.getBigDecimal(SMTableapbatchentrylines.TableName + "." + SMTableapbatchentrylines.bdpayableamount);
				if (bdInvoicePayableAmt == null){
					bdInvoicePayableAmt = BigDecimal.ZERO;
				}
				BigDecimal bdDiscountTakenAmt = rs.getBigDecimal(SMTableapbatchentrylines.TableName + "." + SMTableapbatchentrylines.bdapplieddiscountamt);
				if (bdDiscountTakenAmt == null){
					bdDiscountTakenAmt = BigDecimal.ZERO;
				}
				BigDecimal bdNetPaidAmt = rs.getBigDecimal(SMTableapbatchentrylines.TableName + "." + SMTableapbatchentrylines.bdamount);
				if (bdNetPaidAmt == null){
					bdNetPaidAmt = BigDecimal.ZERO;
				}
				BigDecimal bdAdjustmentAmt = bdInvoicePayableAmt.add(bdDiscountTakenAmt.add(bdNetPaidAmt));
				
				s += printCheckLine(
					rs.getInt(SMTableaptransactions.TableName + "." + SMTableaptransactions.idoctype),
					rs.getString(SMTableaptransactions.TableName + "." + SMTableaptransactions.sdocnumber),
					rs.getLong(SMTableaptransactions.TableName + "." + SMTableaptransactions.lid),
					clsDateAndTimeConversions.resultsetDateStringToFormattedString(
						rs.getString(SMTableaptransactions.TableName + "." + SMTableaptransactions.datdiscountdate),
						SMUtilities.DATE_FORMAT_FOR_DISPLAY, 
						SMUtilities.EMPTY_DATE_VALUE),
					clsDateAndTimeConversions.resultsetDateStringToFormattedString(
						rs.getString(SMTableaptransactions.TableName + "." + SMTableaptransactions.datduedate),
						SMUtilities.DATE_FORMAT_FOR_DISPLAY, 
						SMUtilities.EMPTY_DATE_VALUE),
					bdInvoicePayableAmt,
					bdDiscountTakenAmt,
					bdNetPaidAmt,
					bdAdjustmentAmt,
					sCheckDate,
					rs.getString(SMTableapbatchentries.TableName + "." + SMTableapbatchentries.schecknumber),
					rs.getBigDecimal(SMTableapbatchentries.TableName + "." + SMTableapbatchentries.bdentryamount),
					bIncludeLinkToTransactionInformation,
					sDBID,
					context
				);
				
				lCurrentEntryNumber = rs.getLong(SMTableapbatchentries.TableName + "." + SMTableapbatchentries.lentrynumber);
				bdEntryPayableTotal = bdEntryPayableTotal.add(bdInvoicePayableAmt);
				bdEntryDiscountTotal = bdEntryDiscountTotal.add(bdDiscountTakenAmt);
				bdEntryNetPaymentTotal = bdEntryNetPaymentTotal.add(bdNetPaidAmt);
				bdEntryAdjustmentTotal = bdEntryAdjustmentTotal.add(bdAdjustmentAmt);
				bdLastEntryPaymentAmt = rs.getBigDecimal(SMTableapbatchentries.TableName + "." + SMTableapbatchentries.bdentryamount);
			}
			rs.close();
		} catch (Exception e) {
			throw new Exception("Error [1501082693] reading batch entries - " + e.getMessage());
		}
		
		//Print the last entry totals line:
		s += printEntryTotals(
			bdEntryPayableTotal,
			bdEntryDiscountTotal,
			bdEntryNetPaymentTotal,
			bdEntryAdjustmentTotal,
			bdLastEntryPaymentAmt
		);
		
		return s;
	}

	private String printEntryHeadingLine(
		String sVendorAcct,
		String sVendorName,
		int iVendorActive
		) throws Exception{
		String s = "";
		String sVendorIsActive = "Y";
		if (iVendorActive == 0){
			sVendorIsActive = "N";
		}
		s += "  <TR class = \"" + SMMasterStyleSheetDefinitions.TABLE_ROW_BACKGROUNDCOLOR_WHITE + " \" >\n";
			
		s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD + " \" >"
			+  "<I>" + sVendorAcct + "</I>"
			+ "</TD>\n"
			
			+"    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD + " \" >"
			+  "<I>" + sVendorName + "</I>"
			+ "</TD>\n"

			+"    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD + " \" >"
			+  "&nbsp;"  //Discount date
			+ "</TD>\n"

			+"    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD + " \" >"
			+  "<I>" + sVendorIsActive + "</I>"  //Due date
			+ "</TD>\n"
			
			+"    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD + " \" >"
			+  "&nbsp;"  //Vendor payable
			+ "</TD>\n"

			+"    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD + " \" >"
			+  "&nbsp;"  //Vendor discount
			+ "</TD>\n"
			
			+"    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD + " \" >"
			+  "&nbsp;"  //Vendor adjustment
			+ "</TD>\n"

			+"    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD + " \" >"
			+  "&nbsp;"  //Vendor net payment
			+ "</TD>\n"

			+"    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD + " \" >"
			+  "&nbsp;"  //Check date
			+ "</TD>\n"
			
			+"    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD + " \" >"
			+  "&nbsp;"  //Check number
			+ "</TD>\n"
			
			+"    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD + " \" >"
			+  "&nbsp;"  //Check amt
			+ "</TD>\n"
		;
		
		s += "  </TR>\n";
		
		return s;
	}
	
	private String printReportTotals(
		BigDecimal bdGrandTotalPaymentAmt
		
		){
		String s = "";
		
		s += "  <TR class = \"" + SMMasterStyleSheetDefinitions.TABLE_ROW_BACKGROUNDCOLOR_WHITE + " \" >\n";
		s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_FIELDCONTROL_RIGHT_JUSTIFIED + " \" COLSPAN=" + Integer.toString(NUMBER_OF_COLUMNS - 1) + ">"
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
			+  "Vendor&nbsp;No./"
			+ "</TD>\n"
			
			+"    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_LEFT_JUSTIFIED + " \" >"
			+  "Doc"
			+ "</TD>\n"
			
			+"    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_LEFT_JUSTIFIED + " \" >"
			+  "Discount"
			+ "</TD>\n"

			+"    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_LEFT_JUSTIFIED + " \" >"
			+  "Active/"
			+ "</TD>\n"
			
			+"    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_CENTER_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD + " \" COLSPAN=4 >"
			+  "---------------- VENDOR ----------------"
			+ "</TD>\n"
			/*
			+"    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_RIGHT_JUSTIFIED + " \" >"
			+  "Discount"
			+ "</TD>\n"
			
			+"    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_RIGHT_JUSTIFIED + " \" >"
			+  "Adjustment"
			+ "</TD>\n"
			
			+"    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_RIGHT_JUSTIFIED + " \" >"
			+  "Net&nbsp;payment"
			+ "</TD>\n"
			*/
			+"    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_CENTER_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD + " \" COLSPAN=3 >"
			+  "---------------- BANK ----------------"
			+ "</TD>\n"
			
			/*
			+"    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_LEFT_JUSTIFIED + " \" >"
			+  "Check&nbsp;number"
			+ "</TD>\n"
			
			+"    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_RIGHT_JUSTIFIED + " \" >"
			+  "Payment&nbsp;amt"
			+ "</TD>\n"
			*/
		;
		
		s += "  </TR>\n";
		
		s += "  <TR class = \"" + SMMasterStyleSheetDefinitions.TABLE_ROW_BACKGROUNDCOLOR_LIGHTBLUE + " \" >\n";
		s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_LEFT_JUSTIFIED + " \" >"
			+  "Doc.&nbsp;Number"
			+ "</TD>\n"
			
			+"    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_LEFT_JUSTIFIED + " \" >"
			+  "Type"
			+ "</TD>\n"
			
			+"    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_LEFT_JUSTIFIED + " \" >"
			+  "Date"
			+ "</TD>\n"

			+"    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_LEFT_JUSTIFIED + " \" >"
			+  "Due&nbsp;Date"
			+ "</TD>\n"
			
			+"    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_RIGHT_JUSTIFIED + " \" >"
			+  "<a href=\"#payable\">Payable<SUP>1</SUP></a>"
			+ "</TD>\n"
			
			+"    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_RIGHT_JUSTIFIED + " \" >"
			+  "<a href=\"#discount\">Discount<SUP>2</SUP></a>"
			+ "</TD>\n"
			
			+"    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_RIGHT_JUSTIFIED + " \" >"
			+  "<a href=\"#adjustment\">Adjustment<SUP>3</SUP></a>"
			+ "</TD>\n"
			
			+"    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_RIGHT_JUSTIFIED + " \" >"
			+  "<a href=\"#netpayable\">Net&nbsp;payment<SUP>4</SUP></a>"
			+ "</TD>\n"
			
			+"    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_LEFT_JUSTIFIED + " \" >"
			+  "Check&nbsp;date"
			+ "</TD>\n"
			
			+"    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_LEFT_JUSTIFIED + " \" >"
			+  "Check&nbsp;number"
			+ "</TD>\n"
			
			+"    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_RIGHT_JUSTIFIED + " \" >"
			+  "<a href=\"#paymentamt\">Payment&nbsp;amt<SUP>5</SUP></a>"
			+ "</TD>\n"
			
		;
		
		s += "  </TR>\n";
		
		
		return s;
	}
	
	private String printTableHeading(){
		String s = "";
		
		s += "<TABLE class = \"" + SMMasterStyleSheetDefinitions.TABLE_BASIC_WITH_BORDER + "\" >\n";
		
		return s;
	}

	private String printTableFooting(){
		String s = "";
		
		s += "</TABLE>\n";
		
		return s;
	}
	
	private String printCheckLine(
			int iApplyToDocType,
			String sInvoiceDocNumber,
			long lApplyToDocID,
			String sDiscountDate,
			String sDueDate,
			BigDecimal bdInvoicePayableAmt,
			BigDecimal bdInvoiceDiscountAmt,
			BigDecimal bdInvoiceNetPaymentAmt,
			BigDecimal bdAdjustmentAmt,
			String sCheckDate,
			String sCheckNumber,
			BigDecimal bdCheckAmt,
			boolean bIncludeLinkToTransactionInformation,
			String sDBID, 
			ServletContext context
		){
				
		String s = "";
		String sInvoiceNumberLink = sInvoiceDocNumber;
		if (bIncludeLinkToTransactionInformation){
			sInvoiceNumberLink =
				"<A HREF=\"" + SMUtilities.getURLLinkBase(context) + "smap.APViewTransactionInformation?"
				+ SMTableaptransactions.lid + "=" + Long.toString(lApplyToDocID)
				+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
				+ "\">" + sInvoiceDocNumber + "</A>"
			;
		}
		
		s += "  <TR class = \"" + SMMasterStyleSheetDefinitions.TABLE_ROW_BACKGROUNDCOLOR_LIGHTGREY + " \" >\n";
		
		s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + " \" >"
			+  sInvoiceNumberLink
			+ "</TD>\n"
			
			+ "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + " \" >"
			+  getDocType(iApplyToDocType)
			+ "</TD>\n"
			
			+ "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + " \" >"
			+  sDiscountDate
			+ "</TD>\n"
			
			+ "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + " \" >"
			+  sDueDate
			+ "</TD>\n"
			
			+ "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + " \" >"
			+  clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(bdInvoicePayableAmt)
			+ "</TD>\n"
			
			+ "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + " \" >"
			+  clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(bdInvoiceDiscountAmt)
			+ "</TD>\n"
			
			+ "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + " \" >"
			+  clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(bdAdjustmentAmt)
			+ "</TD>\n"

			+ "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + " \" >"
			+  clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(bdInvoiceNetPaymentAmt)
			+ "</TD>\n"

			+ "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + " \" >"
			+ sCheckDate
			+ "</TD>\n"
			
			+ "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + " \" >"
			+ sCheckNumber
			+ "</TD>\n"

			+ "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + " \" >"
			+  clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(bdCheckAmt)
			+ "</TD>\n"

			+ "  </TR>\n"
			;
			return s;
		}
		
	private String printEntryTotals(
		BigDecimal bdCheckPayableTotal,
		BigDecimal bdCheckDiscountTotal,
		BigDecimal bdCheckNetPaymentTotal,
		BigDecimal bdCheckAdjustmentTotal,
		BigDecimal bdCheckPaymentAmt
		){
		
		String s = "";
		s += "  <TR class = \"" + SMMasterStyleSheetDefinitions.TABLE_ROW_BACKGROUNDCOLOR_LIGHTBLUE + " \" >\n"
		
			//Doc number column
			+ "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_FIELDCONTROL_RIGHT_JUSTIFIED + " \" >"
			+ "&nbsp;"
			+ "</TD>\n"
			
			//Doc type column
			+ "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_FIELDCONTROL_RIGHT_JUSTIFIED + " \" >"
			+ "&nbsp;"
			+ "</TD>\n"
			
			//Disc date column
			+ "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_FIELDCONTROL_RIGHT_JUSTIFIED + " \" >"
			+ "&nbsp;"
			+ "</TD>\n"
			
			//Due date column
			+ "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_FIELDCONTROL_RIGHT_JUSTIFIED + " \" >"
			+ "&nbsp;"
			+ "</TD>\n"
			
			//Vendor payable amt:
			+ "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_FIELDCONTROL_RIGHT_JUSTIFIED + " \" style = \" border-top: thin solid; \" >"
			+ clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(bdCheckPayableTotal)
			+ "</TD>\n"
			
			//Vendor discount amt:
			+ "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_FIELDCONTROL_RIGHT_JUSTIFIED + " \" style = \" border-top: thin solid; \" >"
			+ clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(bdCheckDiscountTotal)
			+ "</TD>\n"

			//Vendor adjustment amt:
			+ "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_FIELDCONTROL_RIGHT_JUSTIFIED + " \" style = \" border-top: thin solid; \" >"
			+ clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(bdCheckAdjustmentTotal)
			+ "</TD>\n"
			
			//Vendor net payment amt:
			+ "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_FIELDCONTROL_RIGHT_JUSTIFIED + " \" style = \" border-top: thin solid; \" >"
			+ clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(bdCheckNetPaymentTotal)
			+ "</TD>\n"
			
			//Check date
			+ "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_FIELDCONTROL_RIGHT_JUSTIFIED + " \" >"
			+ "&nbsp;"
			+ "</TD>\n"
			
			//Check number
			+ "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_FIELDCONTROL_RIGHT_JUSTIFIED + " \" >"
			+ "&nbsp;"
			+ "</TD>\n"
			
			//Check payment amt
			+ "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_FIELDCONTROL_RIGHT_JUSTIFIED + " \" style = \" border-top: thin solid; \" >"
			+ clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(bdCheckPaymentAmt)
			+ "</TD>\n"
			
			+ "  </TR>\n"
		;
		
		s += "  <TR class = \"" + SMMasterStyleSheetDefinitions.TABLE_ROW_BACKGROUNDCOLOR_BLACK + " \" >\n"
			+ "    <TD COLSPAN = " + Integer.toString(NUMBER_OF_COLUMNS) + ">&nbsp;</TD>\n"
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
			+ "<a name=\"payable\"><B><SUP>1</SUP>&nbsp;VENDOR Payable</a>:</B>&nbsp;"
			+ "The 'payable' amount is the total amount payable to the vendor, regardless of available discounts and discount dates, at the time the check was issued." + "\n"
			
			+ "<BR>"
			+ "<a name=\"discount\"><B><SUP>2</SUP>&nbsp;VENDOR Discount</a>:</B>&nbsp;"
			+ "The total amount of discount being taken on this payment (usually against an invoice.)" + "\n"
				
			+ "<BR>"
			+ "<a name=\"adjustment\"><B><SUP>3</SUP>&nbsp;VENDOR Adjustment</a>:</B>&nbsp;"
			+ "Any adjustment to the amount paid, aside from discount taken, that results in less than the 'payable' amount being paid." + "\n"
			
			+ "<BR>"
			+ "<a name=\"netpayable\"><B><SUP>4</SUP>&nbsp;VENDOR Net payment</a>:</B>&nbsp;"
			+ "The total amount paid on this check (the PAYABLE amount less any DISCOUNT and any ADJUSTMENT)." + "\n"
			
			+ "<BR>"
			+ "<a name=\"paymentamt\"><B><SUP>5</SUP>&nbsp;BANK Payment amt</a>:</B>&nbsp;"
			+ "Amount actually being paid with this payment (the check amount), and what will appear in the bank statement.  This equals the total of the 'Net Payment' lines." + "\n"
			
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
