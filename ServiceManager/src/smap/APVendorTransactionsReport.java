package smap;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.ResultSet;
import javax.servlet.ServletContext;
import SMDataDefinition.SMMasterStyleSheetDefinitions;
import SMDataDefinition.SMTableapbatchentries;
import SMDataDefinition.SMTableapbatches;
import SMDataDefinition.SMTableapmatchinglines;
import SMDataDefinition.SMTableaptransactions;
import SMDataDefinition.SMTableicvendors;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsDateAndTimeConversions;
import ServletUtilities.clsManageBigDecimals;
import smcontrolpanel.SMUtilities;

public class APVendorTransactionsReport {

	private static int NUMBER_OF_COLUMNS = 11;
	private static int NUMBER_OF_RECORDS_TO_BUFFER = 50;
	
	public String processReport(
		Connection conn,
		String sStartingDate,
		String sEndingDate,
		String sStartingVendor,
		String sEndingVendor,
		String sStartingDocNumber,
		boolean bIncludeCreditNotes,
		boolean bIncludeDebitNotes,
		boolean bIncludeInvoices,
		boolean bIncludeApplyToTransactions,
		boolean bIncludeMiscellaneousPayments,
		boolean bIncludePayments,
		boolean bIncludePrePayments,
		boolean bIncludeCheckReversals,
		boolean bIncludeVendorsWithZeroBalance,
		boolean bIncludeAppliedDetails,
		boolean bIncludeFullyPaidTransactions,
		boolean bIncludeLinkToTransactionInformation,
		boolean bIncludeLinkToBatchInformation,
		boolean bIncludeLinkToVendorInfo,
		ServletContext context,
		String sDBID
		) throws Exception{
		String s = "";
	
		s += printTableHeading();
		
		s += printColumnHeadings(bIncludeAppliedDetails);
		
		s += printReport(
			conn,
			sStartingDate,
			sEndingDate,
			sStartingVendor,
			sEndingVendor,
			sStartingDocNumber,
			bIncludeCreditNotes,
			bIncludeDebitNotes,
			bIncludeInvoices,
			bIncludeApplyToTransactions,
			bIncludeMiscellaneousPayments,
			bIncludePayments,
			bIncludePrePayments,
			bIncludeCheckReversals,
			bIncludeVendorsWithZeroBalance,
			bIncludeAppliedDetails,
			bIncludeFullyPaidTransactions,
			bIncludeLinkToTransactionInformation,
			bIncludeLinkToBatchInformation,
			bIncludeLinkToVendorInfo,
			context,
			sDBID
		);
		
		s += printTableFooting();
		return s;
	}
	
	private String printTableHeading(){
		String s = "";
		
		s += "<TABLE class = \"" + SMMasterStyleSheetDefinitions.TABLE_BASIC_WITHOUT_BORDER + "\" >\n";
		
		return s;
	}

	private String printReport(
		Connection conn,
		String sStartingDate,
		String sEndingDate,
		String sStartingVendor,
		String sEndingVendor,
		String sStartingDocNumber,
		
		boolean bIncludeCreditNotes,
		boolean bIncludeDebitNotes,
		boolean bIncludeInvoices,
		boolean bIncludeApplyToTransactions,
		boolean bIncludeMiscellaneousPayments,
		boolean bIncludePayments,
		boolean bIncludePrePayments,
		boolean bIncludeCheckReversals,
		boolean bIncludeVendorsWithZeroBalance,
		boolean bIncludeAppliedDetails,
		boolean bIncludeFullyPaidTransactions,
		
		boolean bIncludeLinkToTransactionInformation,
		boolean bIncludeLinkToBatchInformation,
		boolean bIncludeLinkToVendorInfo,
		
		ServletContext context,
		String sDBID
		) throws Exception{
		
		String s= "";
		
		//Get the record set:
		String SQL = 
			"SELECT"
			+ " " + SMTableaptransactions.TableName + "." + SMTableaptransactions.svendor
			+ ", " + SMTableaptransactions.TableName + "." + SMTableaptransactions.idoctype
			+ ", " + SMTableaptransactions.TableName + "." + SMTableaptransactions.bdoriginalamt
			+ ", " + SMTableaptransactions.TableName + "." + SMTableaptransactions.bdcurrentamt
			+ ", " + SMTableaptransactions.TableName + "." + SMTableaptransactions.sdocnumber
			+ ", " + SMTableaptransactions.TableName + "." + SMTableaptransactions.ionhold
			+ ", " + SMTableaptransactions.TableName + "." + SMTableaptransactions.lid
			+ ", " + SMTableaptransactions.TableName + "." + SMTableaptransactions.lapplytopurchaseorderid
			+ ", " + SMTableaptransactions.TableName + "." + SMTableaptransactions.loriginalbatchnumber
			+ ", " + SMTableaptransactions.TableName + "." + SMTableaptransactions.loriginalentrynumber
			+ ", " + SMTableaptransactions.TableName + "." + SMTableaptransactions.datdiscountdate
			+ ", " + SMTableaptransactions.TableName + "." + SMTableaptransactions.datdocdate
			+ ", " + SMTableaptransactions.TableName + "." + SMTableaptransactions.datduedate
			+ ", IF((DATEDIFF(NOW(), " + SMTableaptransactions.TableName + "." + SMTableaptransactions.datduedate + ") > 0)"
				+ " AND (" + SMTableaptransactions.TableName + "." + SMTableaptransactions.bdcurrentamt + " != 0.00),"
				+ "DATEDIFF(NOW(), " + SMTableaptransactions.TableName + "." + SMTableaptransactions.datduedate + "), 0) AS DAYSPASTDUE"
			+ ", " + SMTableaptransactions.TableName + "." + SMTableaptransactions.schecknumber
			+ ", " + SMTableicvendors.TableName + "." + SMTableicvendors.sname
			+ ", " + SMTableapbatchentries.TableName + "." + SMTableapbatchentries.lid
			+ ", VENDORBALANCES.VENDORBALANCE"
			+ " FROM" 
			+ " " + SMTableaptransactions.TableName
			+ " LEFT JOIN " + SMTableicvendors.TableName
			+ " ON " + SMTableaptransactions.TableName + "." + SMTableaptransactions.svendor
			+ " = " + SMTableicvendors.TableName + "." + SMTableicvendors.svendoracct
			+ " LEFT JOIN "
			+ " (SELECT"
			+ " " + SMTableaptransactions.TableName + "." + SMTableaptransactions.svendor + " AS VENDOR"
			+ ", SUM(" + SMTableaptransactions.TableName + "." + SMTableaptransactions.bdcurrentamt + ") AS VENDORBALANCE"
			+ " FROM " + SMTableaptransactions.TableName
			+ " WHERE ("
				+ "(" + SMTableaptransactions.TableName + "." + SMTableaptransactions.svendor + " >= '" + sStartingVendor + "')"
				+ " AND (" + SMTableaptransactions.TableName + "." + SMTableaptransactions.svendor + " <= '" + sEndingVendor + "')"
			+ ")"
			+ " GROUP BY " + SMTableaptransactions.TableName + "." + SMTableaptransactions.svendor
			+ ") AS VENDORBALANCES"
			+ " ON VENDORBALANCES.VENDOR = " + SMTableaptransactions.TableName + "." + SMTableaptransactions.svendor
			
			+ " LEFT JOIN " + SMTableapbatchentries.TableName
			+ " ON " 
			+ "(" + SMTableaptransactions.TableName + "." + SMTableaptransactions.loriginalbatchnumber + " = " + SMTableapbatchentries.TableName + "." + SMTableapbatchentries.lbatchnumber + ")"
			+ " AND (" + SMTableaptransactions.TableName + "." + SMTableaptransactions.loriginalentrynumber + " = " + SMTableapbatchentries.TableName + "." + SMTableapbatchentries.lentrynumber + ")"
		;
		
		SQL	+= " WHERE ("
				//Date range:
				+ "(" + SMTableaptransactions.TableName + "." + SMTableaptransactions.datdocdate + " >= '" + sStartingDate + "')"
				+ " AND (" + SMTableaptransactions.TableName + "." + SMTableaptransactions.datdocdate + " <= '" + sEndingDate + " 23:59:59')"
				
				//Vendor range:
				+ " AND (" + SMTableaptransactions.TableName + "." + SMTableaptransactions.svendor + " >= '" + sStartingVendor + "')"
				+ " AND (" + SMTableaptransactions.TableName + "." + SMTableaptransactions.svendor + " <= '" + sEndingVendor + "')"
				;
		
				//Starting document number:
				SQL += " AND (" + SMTableaptransactions.TableName + "." + SMTableaptransactions.sdocnumber + " >= '" + sStartingDocNumber + "')";
				
				//Transaction types:
				if (!bIncludeCreditNotes){
					SQL += " AND (" + SMTableaptransactions.TableName + "." + SMTableaptransactions.idoctype + " != " + SMTableaptransactions.AP_TRANSACTION_TYPE_INVOICE_CREDITNOTE + ")";
				}
				if (!bIncludeDebitNotes){
					SQL += " AND (" + SMTableaptransactions.TableName + "." + SMTableaptransactions.idoctype + " != " + SMTableaptransactions.AP_TRANSACTION_TYPE_INVOICE_DEBITNOTE + ")";
				}
				if (!bIncludeInvoices){
					SQL += " AND (" + SMTableaptransactions.TableName + "." + SMTableaptransactions.idoctype + " != " + SMTableaptransactions.AP_TRANSACTION_TYPE_INVOICE_INVOICE + ")";
				}
				if (!bIncludeApplyToTransactions){
					SQL += " AND (" + SMTableaptransactions.TableName + "." + SMTableaptransactions.idoctype + " != " + SMTableaptransactions.AP_TRANSACTION_TYPE_PAYMENT_APPLYTO + ")";
				}
				if (!bIncludeMiscellaneousPayments){
					SQL += " AND (" + SMTableaptransactions.TableName + "." + SMTableaptransactions.idoctype + " != " + SMTableaptransactions.AP_TRANSACTION_TYPE_PAYMENT_MISCPAYMENT + ")";
				}
				if (!bIncludePayments){
					SQL += " AND (" + SMTableaptransactions.TableName + "." + SMTableaptransactions.idoctype + " != " + SMTableaptransactions.AP_TRANSACTION_TYPE_PAYMENT_PAYMENT + ")";
				}
				if (!bIncludePrePayments){
					SQL += " AND (" + SMTableaptransactions.TableName + "." + SMTableaptransactions.idoctype + " != " + SMTableaptransactions.AP_TRANSACTION_TYPE_PAYMENT_PREPAYMENT + ")";
				}
				if (!bIncludeCheckReversals){
					SQL += " AND (" + SMTableaptransactions.TableName + "." + SMTableaptransactions.idoctype + " != " + SMTableaptransactions.AP_TRANSACTION_TYPE_REVERSAL + ")";
				}
				
				//Don't print vendors with a zero balance:
				if (!bIncludeVendorsWithZeroBalance){
					SQL += " AND (VENDORBALANCES.VENDORBALANCE != 0.00)";
				}
				
				//Don't print full paid transactions:
				if (!bIncludeFullyPaidTransactions){
					SQL += " AND (" + SMTableaptransactions.TableName + "." + SMTableaptransactions.bdcurrentamt + " != 0.00)";
				}
				
			SQL += ")"
			+ " ORDER BY" 
			+ " " + SMTableaptransactions.TableName + "." + SMTableaptransactions.svendor
			+ ", " + SMTableaptransactions.TableName + "." + SMTableaptransactions.datdocdate
			+ ", " + "LPAD(" + SMTableaptransactions.TableName + "." + SMTableaptransactions.sdocnumber + ", " + Integer.toString(SMTableaptransactions.sdocnumberlength) + ", ' ')"
		;
		//System.out.println("[1523548313] SQL: " + SQL);
		String sLastVendor = "";
		int iRecordCount = 0;
		String sRecordBuffer = "";
		BigDecimal bdTotalVendorOriginalAmt = new BigDecimal("0.00");
		BigDecimal bdTotalVendorCurrentAmt = new BigDecimal("0.00");
		BigDecimal bdGrandTotalOriginalAmt = new BigDecimal("0.00");
		BigDecimal bdGrandTotalCurrentAmt = new BigDecimal("0.00");
		
		try {
			ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
			while (rs.next()){
	        	String sVendorLink = rs.getString(SMTableaptransactions.TableName + "." + SMTableaptransactions.svendor);
	        	if(bIncludeLinkToVendorInfo){
	        		sVendorLink = "<A HREF=\"" + SMUtilities.getURLLinkBase(context) + "smap." 
	    	    		+ "APDisplayVendorInformation" 
	    	    		+ "?" + "VendorNumber" + "=" + rs.getString(SMTableaptransactions.TableName + "." + SMTableaptransactions.svendor) 
	    	    		+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
	    	    		+ "\">"
	    	    		+ rs.getString(SMTableaptransactions.TableName + "." + SMTableaptransactions.svendor)
	    	    		+ "</A>";
	        	}
				
				if (sLastVendor.compareToIgnoreCase(sVendorLink) != 0){
					
					//IF there was a previous vendor, then print the last remaining vendor doc, and then the vendor footing for that vendor:
					if (sLastVendor.compareTo("") != 0){
						sRecordBuffer += printVendorFooting(NUMBER_OF_COLUMNS, bdTotalVendorOriginalAmt, bdTotalVendorCurrentAmt);
						bdTotalVendorOriginalAmt = BigDecimal.ZERO;
						bdTotalVendorCurrentAmt = BigDecimal.ZERO;
					}
					
					//Then print the new vendor heading:
					sRecordBuffer += printVendorHeading(
						NUMBER_OF_COLUMNS, 
						sVendorLink, 
						rs.getString(SMTableicvendors.TableName + "." + SMTableicvendors.sname),
						rs.getBigDecimal("VENDORBALANCES.VENDORBALANCE")
					);
				}
				
				//If the document number/vendor combination has changed, print the document line
				//x - have to decide if the document line was printed already or not:
				//if (
				//	(sLastDocNumberAndVendor.compareToIgnoreCase(rs.getString(SMTableaptransactions.TableName + "." + SMTableaptransactions.sdocnumber)
				//		+ rs.getString(SMTableaptransactions.TableName + "." + SMTableaptransactions.svendor)) != 0)
				//	&& (sLastDocNumberAndVendor.compareTo("") != 0)
				//){
				//}
				
				//Print the document line every time:
				String sBatchEntryID = "";
				if (rs.getLong(SMTableapbatchentries.TableName + "." + SMTableapbatchentries.lid) != 0L){
					sBatchEntryID = Long.toString(rs.getLong(SMTableapbatchentries.TableName + "." + SMTableapbatchentries.lid));
				}
				
				sRecordBuffer += printDocumentLine(
					NUMBER_OF_COLUMNS, 
					rs.getString(SMTableaptransactions.TableName + "." + SMTableaptransactions.sdocnumber), 
					Long.toString(rs.getLong(SMTableaptransactions.TableName + "." + SMTableaptransactions.lid)),
					rs.getInt(SMTableaptransactions.TableName + "." + SMTableaptransactions.idoctype),
					rs.getInt(SMTableaptransactions.TableName + "." + SMTableaptransactions.ionhold),
					Long.toString(rs.getLong(SMTableaptransactions.TableName + "." + SMTableaptransactions.lapplytopurchaseorderid)),
					clsDateAndTimeConversions.resultsetDateStringToFormattedString(
						rs.getString(SMTableaptransactions.TableName + "." + SMTableaptransactions.datdocdate), SMUtilities.DATE_FORMAT_FOR_DISPLAY, SMUtilities.EMPTY_DATE_VALUE),
					clsDateAndTimeConversions.resultsetDateStringToFormattedString(
						rs.getString(SMTableaptransactions.TableName + "." + SMTableaptransactions.datduedate), SMUtilities.DATE_FORMAT_FOR_DISPLAY, SMUtilities.EMPTY_DATE_VALUE),
					clsDateAndTimeConversions.resultsetDateStringToFormattedString(
							rs.getString(SMTableaptransactions.TableName + "." + SMTableaptransactions.datdiscountdate), SMUtilities.DATE_FORMAT_FOR_DISPLAY, ""),
					Integer.toString(rs.getInt("DAYSPASTDUE")),
					rs.getString(SMTableaptransactions.TableName + "." + SMTableaptransactions.schecknumber),
					Long.toString(rs.getLong(SMTableaptransactions.TableName + "." + SMTableaptransactions.loriginalbatchnumber)),
					Long.toString(rs.getLong(SMTableaptransactions.TableName + "." + SMTableaptransactions.loriginalentrynumber)),
					sBatchEntryID,
					rs.getBigDecimal(SMTableaptransactions.TableName + "." + SMTableaptransactions.bdoriginalamt),
					rs.getBigDecimal(SMTableaptransactions.TableName + "." + SMTableaptransactions.bdcurrentamt),
					bIncludeLinkToTransactionInformation,
					bIncludeLinkToBatchInformation,
					sDBID,
					context
				);
				
				//Print the applied details lines, every time, if requested:
				if (bIncludeAppliedDetails){
					sRecordBuffer += printApplyingLines(
						bIncludeLinkToTransactionInformation,
						bIncludeLinkToBatchInformation,
						sDBID, 
						context,
						conn,
						Long.toString(rs.getLong(SMTableaptransactions.TableName + "." + SMTableaptransactions.lid)))
					;
				}
				
				//Use these to 'remember' the last record:
				sLastVendor = sVendorLink;
				bdTotalVendorOriginalAmt = bdTotalVendorOriginalAmt.add(rs.getBigDecimal(SMTableaptransactions.TableName + "." + SMTableaptransactions.bdoriginalamt));
				bdTotalVendorCurrentAmt = bdTotalVendorCurrentAmt.add(rs.getBigDecimal(SMTableaptransactions.TableName + "." + SMTableaptransactions.bdcurrentamt));
				bdGrandTotalOriginalAmt = bdGrandTotalOriginalAmt.add(rs.getBigDecimal(SMTableaptransactions.TableName + "." + SMTableaptransactions.bdoriginalamt));
				bdGrandTotalCurrentAmt = bdGrandTotalCurrentAmt.add(rs.getBigDecimal(SMTableaptransactions.TableName + "." + SMTableaptransactions.bdcurrentamt));
				if ((iRecordCount % NUMBER_OF_RECORDS_TO_BUFFER) == 0){
					s += sRecordBuffer;
					sRecordBuffer = "";
				}
				
				//System.out.println("[1546461948] - iRecordCount = " + iRecordCount);
				//Count the records:
				iRecordCount++;
			}
			rs.close();
		} catch (Exception e) {
			throw new Exception("Error [1502730448] - reading query results with SQL: '" + SQL + "' - " + e.getMessage());
		}
		
		//System.out.println("[1546461947] - finished loop.");
		
		s += sRecordBuffer;
		
		//If there were any transactions listed, then print the last document and the vendor totals:
		if (iRecordCount > 0){
			s += printVendorFooting(NUMBER_OF_COLUMNS, bdTotalVendorOriginalAmt, bdTotalVendorCurrentAmt);
		}
		
		//Report totals:
		s += printReportTotals(NUMBER_OF_COLUMNS, bdGrandTotalOriginalAmt, bdGrandTotalCurrentAmt);
		
		//For testing only:
		//s += "<BR><B>" + SQL + "</B><BR>";
		return s;
	}
	
	private String printTableFooting(){
		String s = "";
		
		s += "</TABLE>\n";
		
		return s;
	}
	
	private String printVendorHeading(int iNumberOfColumns, String svendoracct, String svendorname, BigDecimal bdVendorBalance){
		String s = "";
		
		s += "  <TR class = \"" + SMMasterStyleSheetDefinitions.TABLE_ROW_BACKGROUNDCOLOR_LIGHTBLUE + " \" >\n";
		s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_FIELDCONTROL_LEFT_JUSTIFIED + " \""
			+ " COLSPAN = " + Integer.toString(iNumberOfColumns) + ">"
			+ "<B>Vendor account:</B>&nbsp;" + svendoracct
			+ "&nbsp;&nbsp;&nbsp;<B>Vendor name:</B>&nbsp;" + svendorname
			+ "&nbsp;&nbsp;&nbsp;<B>Balance:</B>&nbsp;" + clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(bdVendorBalance)
			+ "</TD>\n"
			+ "  </TR>\n"
		;
		
		return s;
	}
	private String printVendorFooting(int iNumberOfColumns, BigDecimal bdVendorTotalOriginalAmt, BigDecimal bdVendorTotalCurrentAmt){
		String s = "";
		
		s += "  <TR class = \"" + SMMasterStyleSheetDefinitions.TABLE_ROW_BACKGROUNDCOLOR_LIGHTBLUE + " \" >\n";
		s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_FIELDCONTROL_RIGHT_JUSTIFIED + " \" COLSPAN=" + Integer.toString(iNumberOfColumns - 2) + " >"
			+ "<B>Vendor totals:</B>"
			+ "</TD>\n"
			
			+ "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_FIELDCONTROL_RIGHT_JUSTIFIED + " \" >"
			+ "<B>" + clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(bdVendorTotalOriginalAmt) + "</B>"
			+ "</TD>\n"
			
			+ "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_FIELDCONTROL_RIGHT_JUSTIFIED + " \" >"
			+ "<B>" + clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(bdVendorTotalCurrentAmt) + "</B>"
			+ "</TD>\n"
			
			+ "  </TR>\n"
		;
		
		s += "  <TR class = \"" + SMMasterStyleSheetDefinitions.TABLE_ROW_BACKGROUNDCOLOR_BLACK + " \" >\n"
			+ "    <TD COLSPAN = " + Integer.toString(iNumberOfColumns) + ">&nbsp;</TD>\n"
			+ "  </TR>\n"
		;
		
		return s;
	}
	
	private String printReportTotals(int iNumberOfColumns, BigDecimal bdGrandTotalOriginalAmt, BigDecimal bdGrandTotalCurrentAmt){
		String s = "";
		
		s += "  <TR class = \"" + SMMasterStyleSheetDefinitions.TABLE_ROW_BACKGROUNDCOLOR_LIGHTBLUE + " \" >\n";
		s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_FIELDCONTROL_RIGHT_JUSTIFIED + " \" COLSPAN=" + Integer.toString(iNumberOfColumns - 2) + " >"
			+ "<B>GRAND TOTALS:</B>"
			+ "</TD>\n"
			
			+ "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_FIELDCONTROL_RIGHT_JUSTIFIED + " \" >"
			+ "<B>" + clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(bdGrandTotalOriginalAmt) + "</B>"
			+ "</TD>\n"
			
			+ "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_FIELDCONTROL_RIGHT_JUSTIFIED + " \" >"
			+ "<B>" + clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(bdGrandTotalCurrentAmt) + "</B>"
			+ "</TD>\n"
			
			+ "  </TR>\n"
		;
		
		s += "  <TR class = \"" + SMMasterStyleSheetDefinitions.TABLE_ROW_BACKGROUNDCOLOR_BLACK + " \" >\n"
			+ "    <TD COLSPAN = " + Integer.toString(iNumberOfColumns) + ">&nbsp;</TD>\n"
			+ "  </TR>\n"
		;
		
		return s;
	}
	private String printDocumentLine(
		int iNumberOfColumns, 
		String sDocNumber, 
		String sTransactionID,
		int iDocType,
		int iOnHold,
		String sPONumber,
		String sDocDate,
		String sDueDate,
		String sDiscountDate,
		String sDaysPastDue,
		String sCheckNumber,
		String sOriginalBatchNumber,
		String sOriginalEntryNumber,
		String sOriginalEntryID,
		BigDecimal bdOriginalAmt,
		BigDecimal bdCurrentAmt,
		boolean bIncludeLinkToTransactionInformation,
		boolean bIncludeLinkToBatches,
		String sDBID,
		ServletContext context
		) {
		
		String s = "";
		String sIndent = "&nbsp;&nbsp;";
		String sDisplayedPONumber = sPONumber;
		String sDueDateOrCheckNumber = sDueDate;
		
		if (sPONumber.compareToIgnoreCase("0") == 0){
			sDisplayedPONumber = "&nbsp;";
		}
		
		if (
			(iDocType == SMTableaptransactions.AP_TRANSACTION_TYPE_PAYMENT_MISCPAYMENT)
			|| (iDocType == SMTableaptransactions.AP_TRANSACTION_TYPE_PAYMENT_PAYMENT)
			|| (iDocType == SMTableaptransactions.AP_TRANSACTION_TYPE_PAYMENT_PREPAYMENT)
		){
			sDueDateOrCheckNumber = sCheckNumber;
		}
		
		if (bIncludeLinkToTransactionInformation){
			sDocNumber =
				"<A HREF=\"" + SMUtilities.getURLLinkBase(context) + "smap.APViewTransactionInformation?"
				+ SMTableaptransactions.lid + "=" + sTransactionID
				+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
				+ "\">" + sDocNumber + "</A>"
			;
		}
		
		String sOriginalBatchLinks = getBatchAndEntryLinks(
			sOriginalBatchNumber,
			sOriginalEntryNumber,
			sOriginalEntryID,
			iDocType,
			bIncludeLinkToBatches,
			context,
			sDBID
		);

		String sOnHold = "";
		if (iOnHold == 1){
			sOnHold = "<B><FONT COLOR=RED>Y</FONT></B>";
		}
		
		s += "  <TR class = \"" + SMMasterStyleSheetDefinitions.TABLE_ROW_BACKGROUNDCOLOR_WHITE + " \" >\n";
		s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + " \" >"
			+ sIndent
			
			//Doc number
			+  sDocNumber
			+ "</TD>\n"
			
			//Doc type
			+ "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + " \" >"
			+  getDocType(iDocType)
			+ "</TD>\n"
			
			//PO Number
			+ "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + " \" >"
			+  sDisplayedPONumber
			+ "</TD>\n"
			
			//Doc date
			+ "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + " \" >"
			+  sDocDate
			+ "</TD>\n"
			
			//Discount date
			+ "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + " \" >"
			+  sDiscountDate
			+ "</TD>\n"
			
			//Due date OR check number, if it's a payment:
			+ "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + " \" >"
			+  sDueDateOrCheckNumber
			+ "</TD>\n"
			
			//On hold?
			+ "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + " \" >"
			+  sOnHold
			+ "</TD>\n"
			
			//Batch and entry:
			+ "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + " \" >"
			+  sOriginalBatchLinks
			+ "</TD>\n"
			
			//Days past due:
			+ "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + " \" >"
			+  sDaysPastDue
			+ "</TD>\n"
			
			//Original amt:
			+ "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + " \" >"
			+  clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(bdOriginalAmt)
			+ "</TD>\n"
			
			//Current amt:
			+ "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + " \" >"
			+  clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(bdCurrentAmt)
			+ "</TD>\n"
			
			+ "  </TR>\n"
		;
		
		return s;
	}
	private String getBatchAndEntryLinks(
		String sOriginalBatchNumber,
		String sOriginalEntryNumber,
		String sOriginalEntryID,
		int iDocType,
		boolean bIncludeLinkToBatches,
		ServletContext context,
		String sDBID
		){
		
		//We are going to try to get the batch information to it can be accessed from a link.
		//If there is NO sOriginalEntryID, then we don't try to get the link for the bath and entry:
		String sOriginalBatchNumberLink = sOriginalBatchNumber;
		if (sOriginalBatchNumber == null){
			return "(NOT FOUND)";
		}
		String sOriginalEntryNumberLink = sOriginalEntryNumber;
		if (sOriginalEntryNumber == null){
			return "(NOT FOUND)";
		}
		
		if (
			(sOriginalEntryID.compareToIgnoreCase("") != 0)
			&& bIncludeLinkToBatches
		){
			String sBatchType = Integer.toString(SMTableapbatches.AP_BATCH_TYPE_INVOICE);
			if (
				(iDocType == SMTableaptransactions.AP_TRANSACTION_TYPE_PAYMENT_APPLYTO)
				|| (iDocType == SMTableaptransactions.AP_TRANSACTION_TYPE_PAYMENT_MISCPAYMENT)
				|| (iDocType == SMTableaptransactions.AP_TRANSACTION_TYPE_PAYMENT_PAYMENT)
				|| (iDocType == SMTableaptransactions.AP_TRANSACTION_TYPE_PAYMENT_PREPAYMENT)
			){
				sBatchType = Integer.toString(SMTableapbatches.AP_BATCH_TYPE_PAYMENT);
			}
			
			sOriginalBatchNumberLink =
				"<A HREF=\"" + SMUtilities.getURLLinkBase(context) + "smap.APEditBatchesEdit?"
		    		+ SMTableapbatches.lbatchnumber + "=" + sOriginalBatchNumber
		    		+ "&" + SMTableapbatches.ibatchtype + "=" + sBatchType
					+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
					+ "\">" + sOriginalBatchNumber + "</A>"
			;
			
       		if (sBatchType.compareToIgnoreCase(Integer.toString(SMTableapbatches.AP_BATCH_TYPE_INVOICE)) == 0){
   				sOriginalEntryNumberLink = "<A HREF=\"" + SMUtilities.getURLLinkBase(context) + "smap.APEditInvoiceEdit" 
    	    		+ "?" + SMTableapbatches.lbatchnumber + "=" + sOriginalBatchNumber
    	    		+ "&" + SMTableapbatchentries.lentrynumber + "=" + sOriginalEntryNumber
    	    		+ "&" + SMTableapbatchentries.lid + "=" + sOriginalEntryID
    	    		+ "&" + SMTableapbatchentries.ientrytype + "=" + Integer.toString(iDocType)
    	    		+ "&" + "CallingClass=" + SMUtilities.getFullClassName(this.toString())
    	    		+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
    	    		+ "\">"
    	    		+ sOriginalEntryNumber
    	    		+ "</A>"
    	    		+ "\n"
	    	    ;
    		}
       		
       		if (sBatchType.compareToIgnoreCase(Integer.toString(SMTableapbatches.AP_BATCH_TYPE_PAYMENT)) == 0){
   				sOriginalEntryNumberLink = "<A HREF=\"" + SMUtilities.getURLLinkBase(context) + "smap.APEditPaymentEdit" 
    	    		+ "?" + SMTableapbatches.lbatchnumber + "=" + sOriginalBatchNumber
    	    		+ "&" + SMTableapbatchentries.lentrynumber + "=" + sOriginalEntryNumber
    	    		+ "&" + SMTableapbatchentries.lid + "=" + sOriginalEntryID
    	    		+ "&" + SMTableapbatchentries.ientrytype + "=" + Integer.toString(iDocType)
    	    		+ "&" + "CallingClass=" + SMUtilities.getFullClassName(this.toString())
    	    		+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
    	    		+ "\">"
    	    		+ sOriginalEntryNumber 
    	    		+ "</A>"
    	    		+ "\n"
    	    	;
    		}
		}
		
		return sOriginalBatchNumberLink + "&nbsp;-&nbsp;" + sOriginalEntryNumberLink;
	}
	private String printApplyingLines(
		boolean bIncludeLinkToTransaction,
		boolean bIncludeLinkToBatchInformation,
		String sDBID, 
		ServletContext context, 
		Connection conn,
		String sApplyToDocID) throws Exception{
		String s = "";
		
		String sIndent = "&nbsp;&nbsp;&nbsp;&nbsp;";
		String sDocNumberLink = "";
		//Get all the matching transactions, if there ARE any:
		String SQL = "SELECT"
			+ " " + SMTableapmatchinglines.TableName + "." + SMTableapmatchinglines.bdappliedamount
			+ ", " + SMTableapmatchinglines.TableName + "." + SMTableapmatchinglines.dattransactiondate
			+ ", " + SMTableapmatchinglines.TableName + "." + SMTableapmatchinglines.sappliedfromdocnumber
			+ ", " + SMTableaptransactions.TableName + "." + SMTableaptransactions.lid
			+ ", " + SMTableaptransactions.TableName + "." + SMTableaptransactions.sdocnumber
			+ ", " + SMTableaptransactions.TableName + "." + SMTableaptransactions.idoctype
			+ ", " + SMTableaptransactions.TableName + "." + SMTableaptransactions.schecknumber
			+ ", " + SMTableaptransactions.TableName + "." + SMTableaptransactions.loriginalbatchnumber
			+ ", " + SMTableaptransactions.TableName + "." + SMTableaptransactions.loriginalentrynumber
			+ ", " + SMTableapbatchentries.TableName + "." + SMTableapbatchentries.lid
			
			+ " FROM " + SMTableapmatchinglines.TableName
			+ " LEFT JOIN " + SMTableaptransactions.TableName
			+ " ON " + SMTableapmatchinglines.TableName + "." + SMTableapmatchinglines.ltransactionappliedfromid
			+ " = " + SMTableaptransactions.TableName + "." + SMTableaptransactions.lid
			
			+ " LEFT JOIN " + SMTableapbatchentries.TableName
			+ " ON " 
			+ "(" + SMTableaptransactions.TableName + "." + SMTableaptransactions.loriginalbatchnumber + " = " + SMTableapbatchentries.TableName + "." + SMTableapbatchentries.lbatchnumber + ")"
			+ " AND (" + SMTableaptransactions.TableName + "." + SMTableaptransactions.loriginalentrynumber + " = " + SMTableapbatchentries.TableName + "." + SMTableapbatchentries.lentrynumber + ")"
			
			+ " WHERE ("
				+ "(" + SMTableapmatchinglines.TableName + "." + SMTableapmatchinglines.ltransactionappliedtoid + " = " + sApplyToDocID + ")"
			+ ")"
			+ " ORDER BY " + SMTableapmatchinglines.TableName + "." + SMTableapmatchinglines.dattransactiondate
		;
		try {
			ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
			while (rs.next()){
				if (bIncludeLinkToTransaction){
					sDocNumberLink =
						"<A HREF=\"" + SMUtilities.getURLLinkBase(context) + "smap.APViewTransactionInformation?"
						+ SMTableaptransactions.lid + "=" + Long.toString(rs.getLong(SMTableaptransactions.TableName + "." + SMTableaptransactions.lid))
						+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
						+ "\">" + rs.getString(SMTableapmatchinglines.TableName + "." + SMTableapmatchinglines.sappliedfromdocnumber) + "</A>"
					;
				}
		
				String sBatchEntryID = "";
				if (rs.getLong(SMTableapbatchentries.TableName + "." + SMTableapbatchentries.lid) != 0L){
					sBatchEntryID = Long.toString(rs.getLong(SMTableapbatchentries.TableName + "." + SMTableapbatchentries.lid));
				}
				
				String sOriginalBatchAndEntryLink = getBatchAndEntryLinks(
					rs.getString(SMTableaptransactions.TableName + "." + SMTableaptransactions.loriginalbatchnumber),
					rs.getString(SMTableaptransactions.TableName + "." + SMTableaptransactions.loriginalentrynumber),
					sBatchEntryID,
					rs.getInt(SMTableaptransactions.TableName + "." + SMTableaptransactions.idoctype),
					bIncludeLinkToBatchInformation,
					context,
					sDBID
				);

				String sCheckNumber = "";
				if (rs.getString(SMTableaptransactions.TableName + "." + SMTableaptransactions.schecknumber) != null){
					sCheckNumber = rs.getString(SMTableaptransactions.TableName + "." + SMTableaptransactions.schecknumber);
				}

				s += "  <TR class = \"" + SMMasterStyleSheetDefinitions.TABLE_ROW_BACKGROUNDCOLOR_LIGHTGREY + " \" >\n";
				s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + " \" >"
					
					+  sIndent 

					//Doc number
					+ "<I>" + sDocNumberLink + "</I>"
					+ "</TD>\n"
					
					//Doc Type
					+ "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + " \" >"
					+ "<I>" + getDocType(rs.getInt(SMTableaptransactions.TableName + "." + SMTableaptransactions.idoctype)) + "</I>"
					+ "</TD>\n"
					
					//Check number
					+ "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + " \" >"
					+ "<I>" + sCheckNumber + "</I>"
					+ "</TD>\n"
					
					//Doc date
					+ "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + " \" >"
					+ "<I>" + clsDateAndTimeConversions.resultsetDateStringToFormattedString(
						rs.getString(SMTableapmatchinglines.TableName + "." + SMTableapmatchinglines.dattransactiondate), SMUtilities.DATE_FORMAT_FOR_DISPLAY, SMUtilities.EMPTY_DATE_VALUE) + "</I>"
					+ "</TD>\n"
					
					//(Column not used:)
					+ "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + " \" >"
					+  "&nbsp;"
					+ "</TD>\n"
					
					//(Column not used:)
					+ "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + " \" >"
					+  "&nbsp;"
					+ "</TD>\n"
					
					//(Column not used:)
					+ "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + " \" >"
					+  "&nbsp;"
					+ "</TD>\n"
					
					//Batch and entry link:
					+ "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + " \" >"
					+  "<I>" + sOriginalBatchAndEntryLink + "</I>"
					+ "</TD>\n"
					
					//(Column not used:)
					+ "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + " \" >"
					+  "&nbsp;"
					+ "</TD>\n"
					
					//Applied amt - this gets negated, e.g., to show payments as NEGATIVE numbers, since invoices appear as POSITIVE:
					+ "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + " \" >"
					+  "<I>" + clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(rs.getBigDecimal(SMTableapmatchinglines.TableName + "." + SMTableapmatchinglines.bdappliedamount).negate()) + "</I>"
					+ "</TD>\n"
					
					//(Column not used:)
					+ "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + " \" >"
					+  "&nbsp;"
					+ "</TD>\n"
					
					+ "  </TR>\n"
				;
			}
			rs.close();
		} catch (Exception e) {
			throw new Exception("Error [1502812076] reading applying transactions with SQL: '" + SQL + "' - " + e.getMessage());
		}
		
		return s;
	}

	private String printColumnHeadings(boolean bIncludeAppliedDetails){
		String s = "";
		String sHeadingPadding = "&nbsp;&nbsp;";
		
		s += "  <TR class = \"" + SMMasterStyleSheetDefinitions.TABLE_ROW_BACKGROUNDCOLOR_WHITE + " \" >\n";
		s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_LEFT_JUSTIFIED + " \" >"
			+  "Doc #" + sHeadingPadding
			+ "</TD>\n"
			
			+ "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_LEFT_JUSTIFIED + " \" >"
			+  "Type" + sHeadingPadding
			+ "</TD>\n"
			
			+"    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_LEFT_JUSTIFIED + " \" >"
			+  "PO&nbsp;#" + sHeadingPadding
			+ "</TD>\n"

			+"    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_LEFT_JUSTIFIED + " \" >"
			+  "Doc date" + sHeadingPadding
			+ "</TD>\n"
			
			+"    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_LEFT_JUSTIFIED + " \" >"
			+  "Disc date" + sHeadingPadding
			+ "</TD>\n"

			+"    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_LEFT_JUSTIFIED + " \" >"
			+  "Due date OR check number" + sHeadingPadding
			+ "</TD>\n"
					
			+"    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_LEFT_JUSTIFIED + " \" >"
			+  "On hold?" + sHeadingPadding
			+ "</TD>\n"
			
			+"    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_LEFT_JUSTIFIED + " \" >"
			+  "Batch - Entry" + sHeadingPadding
			+ "</TD>\n"

			+"    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_LEFT_JUSTIFIED + " \" >"
			+  "Days over" + sHeadingPadding
			+ "</TD>\n"
			
			+"    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_RIGHT_JUSTIFIED + " \" >"
			+  "Original amt" + sHeadingPadding
			+ "</TD>\n"

			+"    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_RIGHT_JUSTIFIED + " \" >"
			+ " Current amt"
			+ "</TD>\n"
/*
			+"    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_RIGHT_JUSTIFIED + " \" >"
			+  "Over " + Integer.toString(m_i3rdAgingCategoryNumberOfDays)
			+ "</TD>\n"
			
			+"    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_RIGHT_JUSTIFIED + " \" >"
			+  "Total<BR>Overdue"
			+ "</TD>\n"

			+"    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_RIGHT_JUSTIFIED + " \" >"
			+  "Total<BR>Payable"
			+ "</TD>\n"
*/
		;
		s += "  </TR>\n";
		
		if (bIncludeAppliedDetails){
			String sIndent = "&nbsp;&nbsp;";
			s += "  <TR class = \"" + SMMasterStyleSheetDefinitions.TABLE_ROW_BACKGROUNDCOLOR_LIGHTGREY + " \" >\n";
			s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_LEFT_JUSTIFIED + " \" >"
				+  sIndent + "<I>" + "Applying<BR>Doc #" + "</I>"
				+ "</TD>\n"
				
				+ "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_LEFT_JUSTIFIED + " \" >"
				+  "<I>" + "Type" + "</I>"
				+ "</TD>\n"
				
				+"    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_LEFT_JUSTIFIED + " \" >"
				+  "<I>" + "Check #" + "</I>"
				+ "</TD>\n"
				
				+"    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_LEFT_JUSTIFIED + " \" >"
				+  "<I>" + "Trans Date" + "</I>"
				+ "</TD>\n"

				+"    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_LEFT_JUSTIFIED + " \" >"
				+  "<I>" + "" + "</I>"
				+ "</TD>\n"
				
				+"    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_LEFT_JUSTIFIED + " \" >"
				+  "<I>" + "" + "</I>"
				+ "</TD>\n"
				
				+"    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_LEFT_JUSTIFIED + " \" >"
				+  "<I>" + "" + "</I>"
				+ "</TD>\n"

				+"    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_LEFT_JUSTIFIED + " \" >"
				+  "<I>" + "Batch - Entry" + "</I>"
				+ "</TD>\n"

				+"    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_LEFT_JUSTIFIED + " \" >"
				+  ""
				+ "</TD>\n"
				
				+"    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_RIGHT_JUSTIFIED + " \" >"
				+  "<I>" + "Applied amt" + "</I>"
				+ "</TD>\n"

				+"    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_RIGHT_JUSTIFIED + " \" >"
				+  "<I>" + "" + "</I>"
				+ "</TD>\n"
/*
				+"    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_RIGHT_JUSTIFIED + " \" >"
				+  ""
				+ "</TD>\n"
				
				+"    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_RIGHT_JUSTIFIED + " \" >"
				+  ""
				+ "</TD>\n"

				+"    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_RIGHT_JUSTIFIED + " \" >"
				+  "Net<BR>Applied"
				+ "</TD>\n"
*/
			;
			s += "  </TR>\n";
		}
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
		case SMTableaptransactions.AP_TRANSACTION_TYPE_REVERSAL:
			return "RV";
		//case EARNED_DISCOUNT_TYPE:
		//	return "ED";
		default:
			return "NA";
		}
	}
}
