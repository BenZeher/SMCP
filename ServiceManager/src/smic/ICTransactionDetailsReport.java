package smic;

import java.io.PrintWriter;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.servlet.ServletContext;

import smcontrolpanel.SMSystemFunctions;
import smcontrolpanel.SMUtilities;
import SMDataDefinition.SMMasterStyleSheetDefinitions;
import SMDataDefinition.SMTableicitems;
import SMDataDefinition.SMTableictransactiondetails;
import SMDataDefinition.SMTableictransactions;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsDateAndTimeConversions;
import ServletUtilities.clsManageBigDecimals;
import ServletUtilities.clsServletUtilities;

public class ICTransactionDetailsReport extends java.lang.Object{

	private boolean bDebugMode = false;
	
	public ICTransactionDetailsReport(
			){
	}
	public void processReport(
			Connection conn,
			String sTransactionID,
			String sOriginalBatchNumber,
			String sOriginalEntryNumber,
			String sDBID,
			String sUserID,
			PrintWriter out,
			ServletContext context,
			String sLicenseModuleLevel
			) throws Exception{
	
		String SQL = 
			"SELECT " + SMTableictransactions.TableName + ".*"
			+ ", " + SMTableictransactiondetails.TableName + ".*"
			+ ", " + SMTableicitems.TableName + "." + SMTableicitems.sItemDescription
			+ " FROM (" + SMTableictransactions.TableName 
			+ " LEFT JOIN " + SMTableicitems.TableName + " ON "
			+ SMTableictransactions.TableName + "." + SMTableictransactions.sitemnumber
			+ " = " + SMTableicitems.TableName + "." + SMTableicitems.sItemNumber + ")"
			+ " LEFT JOIN " + SMTableictransactiondetails.TableName
			+ " ON " + SMTableictransactions.TableName + "." + SMTableictransactions.lid
			+ " = " + SMTableictransactiondetails.TableName + "." + SMTableictransactiondetails.ltransactionid
			+ " WHERE ("
			;
		
			if (
				(sOriginalBatchNumber.compareToIgnoreCase("") != 0) && (sOriginalEntryNumber.compareToIgnoreCase("") != 0)
			){
				SQL += "(" + SMTableictransactions.TableName + "." + SMTableictransactions.loriginalbatchnumber + " = " + sOriginalBatchNumber + ")"
					+ " AND (" + SMTableictransactions.TableName + "." + SMTableictransactions.loriginalentrynumber + " = " + sOriginalEntryNumber + ")"
				;
			}else{
				SQL += "(" + SMTableictransactions.TableName + "." 
					+ SMTableictransactions.lid + " = " + sTransactionID + ")"
				;
			}
			SQL += ")"
			+ " ORDER BY"
			+ " " + SMTableictransactiondetails.TableName + "." + SMTableictransactiondetails.ltransactionid
			+ ", " + SMTableictransactiondetails.TableName + "." + SMTableictransactiondetails.ldetailnumber
			;
		if (bDebugMode){
			System.out.println("[1579204053] In " + this.toString() + ".processReport - main SQL = " + SQL);
		}
    	
		//Check permissions for viewing items:
		boolean bViewItemPermitted = SMSystemFunctions.isFunctionPermitted(
			SMSystemFunctions.ICDisplayItemInformation,
			sUserID,
			conn,
			sLicenseModuleLevel);
		
		//Check permissions for viewing batches:
		boolean bBatchViewingPermitted = SMSystemFunctions.isFunctionPermitted(
			SMSystemFunctions.ICEditBatches,
			sUserID,
			conn,
			sLicenseModuleLevel);
		
		BigDecimal bdTotalCost = new BigDecimal(0);
		BigDecimal bdTotalQty = new BigDecimal(0);
		BigDecimal bdQty = new BigDecimal(0);
		BigDecimal bdCost = new BigDecimal(0);
		BigDecimal bdTotalTransactionNetCost = new BigDecimal(0);
		BigDecimal bdTotalTransactionNetQty = new BigDecimal(0);
		long lLastTransactionID = 0L;
		boolean bOddRow = true;
		
    	try{
			ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
			while(rs.next()){
				
				if (rs.getLong(SMTableictransactiondetails.TableName + "." + SMTableictransactiondetails.ltransactionid) != lLastTransactionID){
					//Print the transaction (header) information:
					//If there was a previous transaction...
					if (lLastTransactionID != 0L){
						// ...then print a 'totals' line for the transaction:
						printTransactionTotals(out, bdTotalTransactionNetCost, bdTotalTransactionNetQty, bOddRow);
						bdTotalTransactionNetCost = BigDecimal.ZERO;
						bdTotalTransactionNetQty = BigDecimal.ZERO;
						// ...then end the transaction details table:
						out.println(ServletUtilities.clsServletUtilities.createHTMLComment("End transaction info table") + "\n" + "</TABLE>" + "\n");
						// ..and then end the 'enclosing' table for the transaction:
						out.println(ServletUtilities.clsServletUtilities.createHTMLComment("End enclosing table") + "\n" + "</TABLE>" + "\n");
					}
					
					//Change the background color for the transaction and its details:
					bOddRow = !bOddRow;
					
					//Begin the enclosing table:
					out.println("<TABLE NAME = 'ENCLOSINGTABLE' WIDTH = 100% CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_BASIC_WITH_BORDER + "\">" + "\n");
					
					try {
						printTransactionSection(rs, out, bViewItemPermitted, context, sDBID, bOddRow, bBatchViewingPermitted);
					} catch (SQLException e) {
						throw new Exception("Error [1568988489] reading transaction info - " + e.getMessage());
					}
					printDetailsHeader(out, bOddRow);
				}
					
				//Now print each of the transaction details:
				bdCost = rs.getBigDecimal(SMTableictransactiondetails.TableName + "." + SMTableictransactiondetails.bdcostchange);
				bdQty = rs.getBigDecimal(SMTableictransactiondetails.TableName + "." + SMTableictransactiondetails.bdqtychange);
				try {
					printTransactionDetail(out, rs, bdCost, bdQty, bOddRow);
				} catch (Exception e) {
					throw new Exception("Error [20192631047599] " + "");
				}
				
				//Accumulate the cost and qty for this transaction:
				bdTotalTransactionNetCost = bdTotalTransactionNetCost.add(bdCost);
				bdTotalTransactionNetQty = bdTotalTransactionNetQty.add(bdQty);
				bdTotalCost = bdTotalCost.add(bdCost);
				bdTotalQty = bdTotalQty.add(bdQty);
				
				lLastTransactionID = rs.getLong(SMTableictransactions.TableName + "." + SMTableictransactions.lid);
			}
			rs.close();
    	}catch (SQLException e){
    		throw new Exception("Error [2019263105022] " + "reading resultset with SQL: '" + SQL + "' - " + e.getMessage());
    	}

    	//If there were any records, close the last transaction details table:
    	if (lLastTransactionID != 0L){
    		// ...print the last transaction detail totals:
    		printTransactionTotals(out, bdTotalTransactionNetCost, bdTotalTransactionNetQty, bOddRow);
    		//Close the transaction table:
    		out.println("</TABLE>" + "\n");
    	}
    	
    	//Now print the totals line:
    	printTotalsSection(out, bdTotalCost);
    	
    	//End the table:
    	out.println("</TABLE>" + "\n");
		return;
	}
	private void printTransactionSection(
			ResultSet rs, 
			PrintWriter out, 
			boolean bViewItemPermitted,
			ServletContext context,
			String sDBID,
			boolean bOddRow,
			boolean bBatchViewingPermitted) throws SQLException{
				
		printTransactionHeader(out, bOddRow);
		
		//Print the transaction header line:
		if (bOddRow){
			out.println("  <TR  CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_ROW_ODD + "\">" + "\n");
		}else{
			out.println("  <TR  CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_ROW_EVEN + "\">" + "\n");
		}
		
		try{
		//Transaction date:
		out.println("    <TD CLASS=\"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\">" 
			+ clsDateAndTimeConversions.sqlDateToString(rs.getDate(SMTableictransactions.TableName + "." + SMTableictransactions.datpostingdate), "M/d/yyyy") 
			+ "</TD>" + "\n");
		
		//Location:
		out.println("    <TD CLASS=\"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\">" 
			+ rs.getString(SMTableictransactions.TableName + "." + SMTableictransactions.slocation).trim() 
			+ "</TD>" + "\n");
		
		//Item number:
		String sItemNumber = rs.getString(SMTableictransactions.TableName + "." 
			+ SMTableictransactions.sitemnumber);
		String sItemNumberLink = "";
		if (bViewItemPermitted){
			sItemNumberLink = "<A HREF=\"" + SMUtilities.getURLLinkBase(context) 
			+ "smic.ICDisplayItemInformation?ItemNumber=" 
	    		+ sItemNumber
	    		+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
	    		+ "\">" + clsServletUtilities.Fill_In_Empty_String_For_HTML_Cell(sItemNumber) + "</A>";
		}else{
			sItemNumberLink = sItemNumber;
		}
		out.println("    <TD CLASS=\"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\">" 
			+ sItemNumberLink 
			+ "</TD>" + "\n");
		
		//Item description:
		out.println("    <TD CLASS=\"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\">" 
			+ rs.getString(SMTableicitems.TableName + "." + SMTableicitems.sItemDescription) 
			+ "</TD>" + "\n");
		
		//Doc number:
		out.println("    <TD CLASS=\"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\">" 
			+ rs.getString(SMTableictransactions.TableName + "." + SMTableictransactions.sdocnumber)
			+ "</TD>" + "\n");

		//Type:
		String sType = "";
		BigDecimal bdQty = rs.getBigDecimal(SMTableictransactions.TableName + "." 
			+ SMTableictransactions.bdqty);
		int iType = rs.getInt(SMTableictransactions.TableName + "." 
			+ SMTableictransactions.ientrytype);
		if (iType == ICEntryTypes.ADJUSTMENT_ENTRY){
			sType = "Adjustment";
		}
		if (iType == ICEntryTypes.RECEIPT_ENTRY){
			if (bdQty.compareTo(BigDecimal.ZERO) < 0){
				sType = "Receipt Return";
			}else{
				sType = "Receipt";
			}
		}
		if (iType == ICEntryTypes.SHIPMENT_ENTRY){
			if (bdQty.compareTo(BigDecimal.ZERO) < 0){
				sType = "Shipment";
			}else{
				sType = "Shipment Return";
			}
		}
		if (iType == ICEntryTypes.TRANSFER_ENTRY){
			if (bdQty.compareTo(BigDecimal.ZERO) < 0){
				sType = "Transfer from";
			}else{
				sType = "Transfer to";
			}
		}
		if (iType == ICEntryTypes.PHYSICALCOUNT_ENTRY){
			sType = "Physical count";
		}
		
		out.println("    <TD CLASS=\"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\">" 
			+ sType
			+ "</TD>" + "\n");
		
		//Entry description:
		out.println("    <TD CLASS=\"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\">" 
			+ rs.getString(SMTableictransactions.TableName + "." + SMTableictransactions.sentrydescription)
			+ "</TD>" + "\n");
		
		//Line description:
		out.println("    <TD CLASS=\"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\">" 
			+ rs.getString(SMTableictransactions.TableName + "." + SMTableictransactions.slinedescription)
			+ "</TD>" + "\n");

		//Original batch and entry:
		String sBatchEntryLink = 
				Long.toString(rs.getLong(SMTableictransactions.TableName + "." + SMTableictransactions.loriginalbatchnumber))
				+ " - "
				+ Long.toString(rs.getLong(SMTableictransactions.TableName + "." + SMTableictransactions.loriginalentrynumber));
		if (bBatchViewingPermitted){
    	    //Set the name of the class which will handle the processing of the entry, depending on the doc type:
    	    String sEntryViewingClass = "";
    	    if (rs.getLong(SMTableictransactions.TableName + "." + SMTableictransactions.ientrytype) 
    	    		== ICEntryTypes.ADJUSTMENT_ENTRY){
    	    	sEntryViewingClass = "ICEditAdjustmentEntry";
    	    }
    	    if (rs.getLong(SMTableictransactions.TableName + "." + SMTableictransactions.ientrytype) 
    	    		== ICEntryTypes.RECEIPT_ENTRY){
    	    	sEntryViewingClass = "ICEditReceiptEntry";
    	    }
    	    if (rs.getLong(SMTableictransactions.TableName + "." + SMTableictransactions.ientrytype) 
    	    		== ICEntryTypes.SHIPMENT_ENTRY){
    	    	sEntryViewingClass = "ICEditShipmentEntry";
    	    }
    	    if (rs.getLong(SMTableictransactions.TableName + "." + SMTableictransactions.ientrytype) 
    	    		== ICEntryTypes.TRANSFER_ENTRY){
    	    	sEntryViewingClass = "ICEditTransferEntry";
    	    }
    	    if (rs.getLong(SMTableictransactions.TableName + "." + SMTableictransactions.ientrytype) 
    	    		== ICEntryTypes.PHYSICALCOUNT_ENTRY){
    	    	sEntryViewingClass = "ICEditPhysicalCountEntry";
    	    }
			sBatchEntryLink = "<A HREF=\"" + SMUtilities.getURLLinkBase(context) + "smic." + sEntryViewingClass + "?" 
				+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
				+ "&BatchNumber=" + Long.toString(rs.getLong(SMTableictransactions.TableName + "." + SMTableictransactions.loriginalbatchnumber))
				+ "&EntryNumber=" + Long.toString(rs.getLong(SMTableictransactions.TableName + "." + SMTableictransactions.loriginalentrynumber))
				+ "&Editable=No"
				+ "&BatchType=" + Integer.toString(ICEntryTypes.getBatchType(rs.getInt(SMTableictransactions.TableName + "." + SMTableictransactions.ientrytype)))
				+ "&EntryType=" + Integer.toString(rs.getInt(SMTableictransactions.TableName + "." + SMTableictransactions.ientrytype))
				+ "\">" + sBatchEntryLink + "</A>\n"
			;
		}
		out.println("    <TD CLASS=\"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\">" 
			+ sBatchEntryLink
			+ "</TD>" + "\n");
		
		//Original line number:
		out.println("    <TD CLASS=\"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\">" 
			+ Long.toString(rs.getLong(SMTableictransactions.TableName + "." + SMTableictransactions.loriginallinenumber))
			+ "</TD>" + "\n");
		
		//Posted by:
		out.println("    <TD CLASS=\"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\">" 
			+ rs.getString(SMTableictransactions.TableName + "." + SMTableictransactions.spostedbyfullname)
			+ "</TD>" + "\n");
				
		//UOM:
		out.println("    <TD CLASS=\"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\">" 
			+ rs.getString(SMTableictransactions.TableName + "." + SMTableictransactions.sunitofmeasure)
			+ "</TD>" + "\n");
		
		//Qty:
		out.println("    <TD CLASS=\"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\">" 
			+ clsManageBigDecimals.BigDecimalToFormattedString("###,###,##0.0000", bdQty)
			+ "</TD>" + "\n");

		//Cost:
		out.println("    <TD CLASS=\"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\">" 
			+ clsManageBigDecimals.BigDecimalToFormattedString("###,###,##0.0000", rs.getBigDecimal(SMTableictransactions.TableName + "." + SMTableictransactions.bdcost))
			+ "</TD>" + "\n");
		
		}catch(SQLException e){
			throw e;
		}
		out.println("  </TR>");
		out.println("</TABLE>");

	}
	private void printTransactionDetail(
			PrintWriter out,
			ResultSet rs,
			BigDecimal bdCost,
			BigDecimal bdQty,
			boolean bOddRow
			) throws Exception{
		
		if (bOddRow){
			out.println("  <TR  CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_ROW_ODD + "\">" + "\n");
		}else{
			out.println("  <TR  CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_ROW_EVEN + "\">" + "\n");
		}
		
		//Cost bucket ID:
		out.println("    <TD CLASS=\"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\">" 
			+ Long.toString(rs.getLong(SMTableictransactiondetails.TableName + "." + SMTableictransactiondetails.lcostbucketid))
			+ "</TD>" + "\n");
		
		//Creation date:
		out.println("    <TD CLASS=\"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\">" 
			+ clsDateAndTimeConversions.resultsetDateStringToString(
				rs.getString(SMTableictransactiondetails.TableName + "." + SMTableictransactiondetails.dattimecostbucketcreation))
			+ "</TD>" + "\n");
		
		//Remark:
		out.println("    <TD CLASS=\"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\">" 
			+ rs.getString(SMTableictransactiondetails.TableName + "." + SMTableictransactiondetails.scostbucketremark) + "</TD>" + "\n");
		
		BigDecimal bdQtyBeforeTransaction = new BigDecimal(0);
		BigDecimal bdCostBeforeTransaction = new BigDecimal(0);
		BigDecimal bdQtyAfterTransaction = new BigDecimal(0);
		BigDecimal bdCostAfterTransaction = new BigDecimal(0);
		
		bdQtyBeforeTransaction = rs.getBigDecimal(SMTableictransactiondetails.TableName + "." 
				+ SMTableictransactiondetails.bdcostbucketqtybeforetrans);
		if (bdQtyBeforeTransaction == null){
			bdQtyBeforeTransaction = BigDecimal.ZERO;
		}
		bdCostBeforeTransaction = rs.getBigDecimal(SMTableictransactiondetails.TableName + "." 
				+ SMTableictransactiondetails.bdcostbucketcostbeforetrans);
		if (bdCostBeforeTransaction == null){
			bdCostBeforeTransaction = BigDecimal.ZERO;
		}

		if (bdQty == null){
			bdQty = BigDecimal.ZERO;
		}
		if (bdCost == null){
			bdCost = BigDecimal.ZERO;
		}
		bdQtyAfterTransaction = bdQtyBeforeTransaction.add(bdQty);
		bdCostAfterTransaction = bdCostBeforeTransaction.add(bdCost);
		
		//Qty before transaction:
		out.println("    <TD CLASS=\"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\">" 
			+ clsManageBigDecimals.BigDecimalToScaledFormattedString(SMTableictransactiondetails.bdcostbucketqtybeforetransScale, bdQtyBeforeTransaction) 
			+ "</TD>" + "\n");
		
		//Cost before transaction:
		out.println("    <TD CLASS=\"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\">" 
			+ clsManageBigDecimals.BigDecimalToScaledFormattedString(SMTableictransactiondetails.bdcostbucketcostbeforetransScale, bdCostBeforeTransaction)
			+ "</TD>" + "\n");
		
		//Qty after transaction:
		out.println("    <TD CLASS=\"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\">" 
			+ clsManageBigDecimals.BigDecimalToScaledFormattedString(SMTableictransactiondetails.bdcostbucketqtybeforetransScale, bdQtyAfterTransaction)
			+ "</TD>" + "\n");

		//Cost after transaction:
		out.println("    <TD CLASS=\"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\">" 
			+ clsManageBigDecimals.BigDecimalToScaledFormattedString(SMTableictransactiondetails.bdcostbucketcostbeforetransScale, bdCostAfterTransaction)
			+ "</TD>" + "\n");

		//Qty change:
		out.println("    <TD CLASS=\"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\">" 
			+ clsManageBigDecimals.BigDecimalToScaledFormattedString(SMTableictransactiondetails.bdqtychangeScale, bdQty)
			+ "</TD>" + "\n");

		//Cost change:
		out.println("    <TD CLASS=\"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\">" 
			+ clsManageBigDecimals.BigDecimalToScaledFormattedString(SMTableictransactiondetails.bdcostchangeScale, bdCost)
			+ "</TD>" + "\n");

		out.println("</TR>");
	}
	private void printTransactionTotals(
		PrintWriter out,
		BigDecimal bdTotalTransactionNetCost,
		BigDecimal bdTotalTransactionNetQty,
		boolean bOddRow
		) throws Exception{
	
	if (bOddRow){
		out.println("  <TR  CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_ROW_ODD + "\">" + "\n");
	}else{
		out.println("  <TR  CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_ROW_EVEN + "\">" + "\n");
	}
	

	//Label:
	out.println("    <TD CLASS=\"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD + "\" COLSPAN=7>" 
		+ "Transaction Totals:"
		+ "</TD>" + "\n");

	//Qty change:
	out.println("    <TD CLASS=\"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD + "\">" 
		+ clsManageBigDecimals.BigDecimalToScaledFormattedString(SMTableictransactiondetails.bdqtychangeScale, bdTotalTransactionNetQty)
		+ "</TD>" + "\n");

	//Cost change:
	out.println("    <TD CLASS=\"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD + "\">" 
		+ clsManageBigDecimals.BigDecimalToScaledFormattedString(SMTableictransactiondetails.bdcostchangeScale, bdTotalTransactionNetCost)
		+ "</TD>" + "\n");

	out.println("</TR>");
	}
	private void printTransactionHeader(PrintWriter out, boolean bOddRow){
		out.println(ServletUtilities.clsServletUtilities.createHTMLComment("Begin table for transaction info"));
		out.println("<TABLE WIDTH = 100% CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_BASIC_WITH_BORDER_COLLAPSE + "\">" + "\n");
		if (bOddRow){
			out.println("  <TR  CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_ROW_ODD + "\">" + "\n");
		}else{
			out.println("  <TR  CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_ROW_EVEN + "\">" + "\n");
		}
		
		out.println("    <TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD + "\">Date</TD>" + "\n");
		out.println("    <TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD + "\">Location</TD>" + "\n");
		out.println("    <TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD + "\">Item</TD>" + "\n");
		out.println("    <TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD + "\">Item Desc.</TD>" + "\n");
		out.println("    <TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD + "\">Doc. #</TD>" + "\n");
		out.println("    <TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD + "\">Type</TD>" + "\n");
		out.println("    <TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD + "\">Entry Desc.</TD>" + "\n");
		out.println("    <TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD + "\">Line Desc.</TD>" + "\n");
		out.println("    <TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD + "\">Batch/Entry</TD>" + "\n");
		out.println("    <TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD + "\">Line</TD>" + "\n");
		out.println("    <TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD + "\">Posted by</TD>" + "\n");
		out.println("    <TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD + "\">Unit</TD>" + "\n");
		out.println("    <TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD + "\">Qty</TD>" + "\n");
		out.println("    <TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD + "\">Cost</TD>" + "\n");
		out.println("  </TR>");
	}
	private void printDetailsHeader(PrintWriter out, boolean bOddRow){
		out.println(ServletUtilities.clsServletUtilities.createHTMLComment("Begin table for transaction details"));
		out.println("<TABLE WIDTH = 100% CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_BASIC_WITH_BORDER_COLLAPSE + "\">" + "\n");
		if (bOddRow){
			out.println("  <TR  CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_ROW_ODD + "\">" + "\n");
		}else{
			out.println("  <TR  CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_ROW_EVEN + "\">" + "\n");
		}
		out.println("    <TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD + "\">Cost&nbsp;bucket&nbsp;ID</TD>" + "\n");
		out.println("    <TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD + "\">Created</TD>" + "\n");
		out.println("    <TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD + "\">Remark</TD>" + "\n");
		out.println("    <TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD + "\">Qty&nbsp;before&nbsp;transaction</TD>" + "\n");
		out.println("    <TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD + "\">Cost&nbsp;before&nbsp;transaction</TD>" + "\n");
		out.println("    <TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD + "\">Qty&nbsp;after&nbsp;transaction</TD>" + "\n");
		out.println("    <TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD + "\">Cost&nbsp;after&nbsp;transaction</TD>" + "\n");
		out.println("    <TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD + "\">Qty&nbsp;change</TD>" + "\n");
		out.println("    <TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD + "\">Cost&nbsp;change</TD>" + "\n");
		out.println("  </TR>");
	}
	private void printTotalsSection(PrintWriter out, BigDecimal bdTotalCost){
		out.println("<TABLE WIDTH = 100% align=right CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_BASIC_WITH_BORDER_COLLAPSE + "\">" + "\n");
		out.println("  <TR class = \"" + SMMasterStyleSheetDefinitions.TABLE_TOTAL + " \" >\n");
		out.println("    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD + " \" >"
			+ " TOTAL CHANGE IN COST:&nbsp;"
			+ clsManageBigDecimals.BigDecimalToScaledFormattedString(SMTableictransactiondetails.bdcostchangeScale, bdTotalCost)
			+ "</TD>\n"
		);
		out.println("  </TR>");
		out.println("</TABLE>");
	}
}
