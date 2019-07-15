package smic;

import java.io.PrintWriter;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import javax.servlet.ServletContext;

import smcontrolpanel.SMSystemFunctions;
import smcontrolpanel.SMUtilities;
import SMDataDefinition.SMMasterStyleSheetDefinitions;
import SMDataDefinition.SMTableicitems;
import SMDataDefinition.SMTableicpoheaders;
import SMDataDefinition.SMTableicporeceiptheaders;
import SMDataDefinition.SMTableicporeceiptlines;
import ServletUtilities.clsServletUtilities;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsDateAndTimeConversions;
import ServletUtilities.clsManageBigDecimals;

public class ICItemsReceivedNotInvoicedReport extends java.lang.Object{

	private String m_sErrorMessage;
	private boolean bDebugMode = false;
	
	private static final String ITEM_TYPE_STOCKINVENTORY = "STOCK INVENTORY";
	private static final String ITEM_TYPE_NONSTOCKINVENTORY = "NON-STOCK INVENTORY";
	private static final String ITEM_TYPE_NONINVENTORY = "NON-INVENTORY";
	
	public ICItemsReceivedNotInvoicedReport(
			){
		m_sErrorMessage = "";
	}
	public boolean processReport(
			Connection conn,
			ArrayList<String>sLocations,
			String sStartingDate,
			String sEndingDate,
			String sVendor,
			boolean bIncludeStockInventoryItems,
			boolean bIncludeNonStockInventoryItems,
			boolean bIncludeNonInventoryItems,
			boolean bIncludeInvoicedItems,
	    	boolean bIncludeUnInvoicedItems,
	    	String sDBID,
			String sUserID,
			PrintWriter out,
			ServletContext context,
			String sLicenseModuleLevel
			){
	
		//Create string of locations:
		String sLocationsString = "";
		for (int i = 0; i < sLocations.size(); i++){
			sLocationsString += "," + sLocations.get(i);
		}
		
		String SQL = "SELECT"
			+ " " + SMTableicporeceiptheaders.TableName + "." + SMTableicporeceiptheaders.lpoheaderid
			+ ", " + SMTableicporeceiptheaders.TableName + "." + SMTableicporeceiptheaders.lid
			+ ", " + SMTableicporeceiptheaders.TableName + "." + SMTableicporeceiptheaders.datreceived
			+ ", " + SMTableicporeceiptheaders.TableName + "." + SMTableicporeceiptheaders.screatedbyfullname
			+ ", " + SMTableicporeceiptheaders.TableName + "." + SMTableicporeceiptheaders.lpostedtoic
			+ ", " + SMTableicporeceiptlines.TableName + "." + SMTableicporeceiptlines.bdqtyreceived
			+ ", " + SMTableicporeceiptlines.TableName + "." + SMTableicporeceiptlines.bdextendedcost
			+ ", " + SMTableicporeceiptlines.TableName + "." + SMTableicporeceiptlines.sitemnumber
			+ ", " + SMTableicporeceiptlines.TableName + "." + SMTableicporeceiptlines.sitemdescription
			+ ", " + SMTableicporeceiptlines.TableName + "." + SMTableicporeceiptlines.sglexpenseacct
			+ ", " + SMTableicporeceiptlines.TableName + "." + SMTableicporeceiptlines.sunitofmeasure
			+ ", " + SMTableicporeceiptlines.TableName + "." + SMTableicporeceiptlines.lnoninventoryitem
			+ ", " + SMTableicporeceiptlines.TableName + "." + SMTableicporeceiptlines.slocation
			+ ", " + SMTableicpoheaders.TableName + "." + SMTableicpoheaders.svendor
			+ ", " + SMTableicpoheaders.TableName + "." + SMTableicpoheaders.svendorname
			
			+ ", IF (" + SMTableicporeceiptlines.TableName + "." + SMTableicporeceiptlines.lnoninventoryitem + " = 1, '" + ITEM_TYPE_NONINVENTORY + "',"
				+ " IF (" + SMTableicitems.TableName + "." + SMTableicitems.inonstockitem + " = 1, '" + ITEM_TYPE_NONSTOCKINVENTORY + "', '" + ITEM_TYPE_STOCKINVENTORY + "')"
			+ ") AS 'ITEMTYPE'"
			
			+ " FROM ("
			+ SMTableicporeceiptlines.TableName + " LEFT JOIN " 
			+ SMTableicporeceiptheaders.TableName + " ON " 
			+ SMTableicporeceiptlines.TableName + "." + SMTableicporeceiptlines.lreceiptheaderid 
			+ " = " + SMTableicporeceiptheaders.TableName + "." + SMTableicporeceiptheaders.lid

			+ " LEFT JOIN " + SMTableicpoheaders.TableName + " ON " + SMTableicpoheaders.TableName + "."
			+ SMTableicpoheaders.lid + " = " + SMTableicporeceiptheaders.TableName + "." + SMTableicporeceiptheaders.lpoheaderid
			
			+ " LEFT JOIN " + SMTableicitems.TableName + " ON " + SMTableicporeceiptlines.TableName + "." + SMTableicporeceiptlines.sitemnumber
			+ " = " + SMTableicitems.TableName + "." + SMTableicitems.sItemNumber
			
			+ ")"
			+ " WHERE ("
				//Dates
				+ "(" + SMTableicporeceiptheaders.TableName + "." + SMTableicporeceiptheaders.datreceived 
					+ " >= '" + sStartingDate + " 00:00:00')"
				+ " AND (" + SMTableicporeceiptheaders.TableName + "." + SMTableicporeceiptheaders.datreceived
					+ " <= '" + sEndingDate + " 23:59:59')"
					
				//Locations:
	            + " AND (INSTR('" + sLocationsString + "', " + SMTableicporeceiptlines.TableName + "." 
            		+ SMTableicporeceiptlines.slocation + ") > 0)"
				
            	//Qty not zero:
            	+ " AND (" + SMTableicporeceiptlines.TableName + "." + SMTableicporeceiptlines.bdqtyreceived
            		+ " <> 0.0000)"
		
				//The receipt is not deleted:
				+ " AND (" + SMTableicporeceiptheaders.TableName + "." + SMTableicporeceiptheaders.lstatus + " != " 
					+ Integer.toString(SMTableicporeceiptheaders.STATUS_DELETED) + ")"
				;
		
   				//It IS posted to IC:
            		//TJR - 1/10/2013 - we don't care if it's posted:
   				//+ " AND (" + SMTableicporeceiptheaders.TableName + "." 
   				//	+ SMTableicporeceiptheaders.lpostedtoic + " = 1)"
   				//;
				//Invoiced/UnInvoiced:
				//Can't include neither, but it could include both
				if (bIncludeInvoicedItems && bIncludeUnInvoicedItems){
					//Just include everything
				}
				if (!bIncludeInvoicedItems && bIncludeUnInvoicedItems){
					//Uninvoiced items only:
					SQL += " AND (" + SMTableicporeceiptlines.TableName + "." 
					+ SMTableicporeceiptlines.lpoinvoiceid + " = " + Integer.toString(SMTableicporeceiptlines.PO_INVOICE_STATUS_NOT_INVOICED_YET) + ")";
				}
				if (bIncludeInvoicedItems && !bIncludeUnInvoicedItems){
					//Invoiced items only:
					SQL += " AND (" + SMTableicporeceiptlines.TableName + "." 
					+ SMTableicporeceiptlines.lpoinvoiceid + " != " + Integer.toString(SMTableicporeceiptlines.PO_INVOICE_STATUS_NOT_INVOICED_YET) + ")";
				}

				String sItemTypeQualifier = "";
				if (bIncludeStockInventoryItems){
					if (sItemTypeQualifier.compareToIgnoreCase("") == 0){
						sItemTypeQualifier += "("
								+ "IF (" + SMTableicporeceiptlines.TableName + "." + SMTableicporeceiptlines.lnoninventoryitem + " = 1, '" + ITEM_TYPE_NONINVENTORY + "',"
									+ " IF (" + SMTableicitems.TableName + "." + SMTableicitems.inonstockitem + " = 1, '" + ITEM_TYPE_NONSTOCKINVENTORY + "', '" + ITEM_TYPE_STOCKINVENTORY + "')"
								+ ")"
							+ " = '" + ITEM_TYPE_STOCKINVENTORY + "')";
					}else{
						sItemTypeQualifier += " OR ("
								+ "IF (" + SMTableicporeceiptlines.TableName + "." + SMTableicporeceiptlines.lnoninventoryitem + " = 1, '" + ITEM_TYPE_NONINVENTORY + "',"
									+ " IF (" + SMTableicitems.TableName + "." + SMTableicitems.inonstockitem + " = 1, '" + ITEM_TYPE_NONSTOCKINVENTORY + "', '" + ITEM_TYPE_STOCKINVENTORY + "')"
								+ ")"
							+ " = '" + ITEM_TYPE_STOCKINVENTORY + "')";
					}
				}
				
				if (bIncludeNonStockInventoryItems){
					if (sItemTypeQualifier.compareToIgnoreCase("") == 0){
						sItemTypeQualifier += "("
								+ "IF (" + SMTableicporeceiptlines.TableName + "." + SMTableicporeceiptlines.lnoninventoryitem + " = 1, '" + ITEM_TYPE_NONINVENTORY + "',"
									+ " IF (" + SMTableicitems.TableName + "." + SMTableicitems.inonstockitem + " = 1, '" + ITEM_TYPE_NONSTOCKINVENTORY + "', '" + ITEM_TYPE_STOCKINVENTORY + "')"
								+ ")"
							+ " = '" + ITEM_TYPE_NONSTOCKINVENTORY + "')";
					}else{
						sItemTypeQualifier += " OR ("
								+ "IF (" + SMTableicporeceiptlines.TableName + "." + SMTableicporeceiptlines.lnoninventoryitem + " = 1, '" + ITEM_TYPE_NONINVENTORY + "',"
									+ " IF (" + SMTableicitems.TableName + "." + SMTableicitems.inonstockitem + " = 1, '" + ITEM_TYPE_NONSTOCKINVENTORY + "', '" + ITEM_TYPE_STOCKINVENTORY + "')"
								+ ")"
							+ " = '" + ITEM_TYPE_NONSTOCKINVENTORY + "')";
					}
				}
				
				if (bIncludeNonInventoryItems){
					if (sItemTypeQualifier.compareToIgnoreCase("") == 0){
						sItemTypeQualifier += "("
								+ "IF (" + SMTableicporeceiptlines.TableName + "." + SMTableicporeceiptlines.lnoninventoryitem + " = 1, '" + ITEM_TYPE_NONINVENTORY + "',"
									+ " IF (" + SMTableicitems.TableName + "." + SMTableicitems.inonstockitem + " = 1, '" + ITEM_TYPE_NONSTOCKINVENTORY + "', '" + ITEM_TYPE_STOCKINVENTORY + "')"
								+ ")"
							+ " = '" + ITEM_TYPE_NONINVENTORY + "')";
					}else{
						sItemTypeQualifier += " OR ("
								+ "IF (" + SMTableicporeceiptlines.TableName + "." + SMTableicporeceiptlines.lnoninventoryitem + " = 1, '" + ITEM_TYPE_NONINVENTORY + "',"
									+ " IF (" + SMTableicitems.TableName + "." + SMTableicitems.inonstockitem + " = 1, '" + ITEM_TYPE_NONSTOCKINVENTORY + "', '" + ITEM_TYPE_STOCKINVENTORY + "')"
								+ ")"
							+ " = '" + ITEM_TYPE_NONINVENTORY + "')";
					}
				}
						
				SQL += " AND (" + sItemTypeQualifier + ")";
				
				
				if (sVendor.trim().compareToIgnoreCase("") != 0){
					SQL += " AND (" + SMTableicpoheaders.TableName + "." + SMTableicpoheaders.svendor + " = '" + sVendor.trim() + "')";
				}
				
				SQL += ")"	//Complete the 'where' clause
			+ " ORDER BY"
				+ " " + SMTableicporeceiptlines.TableName + "." + SMTableicporeceiptlines.sglexpenseacct
				+ ", " + SMTableicporeceiptheaders.TableName + "." + SMTableicporeceiptheaders.datreceived
				+ ", " + SMTableicporeceiptheaders.TableName + "." + SMTableicporeceiptheaders.lpoheaderid
				+ ", " + SMTableicporeceiptlines.TableName + "." + SMTableicporeceiptlines.sitemnumber
			;
		//Check permissions for editing receipts:
		boolean bEditReceiptsPermitted = SMSystemFunctions.isFunctionPermitted(
			SMSystemFunctions.ICEditReceipts,
			sUserID,
			conn,
			sLicenseModuleLevel);
				
		//Check permissions for viewing items:
		boolean bViewItemPermitted = SMSystemFunctions.isFunctionPermitted(
			SMSystemFunctions.ICDisplayItemInformation,
			sUserID,
			conn,
			sLicenseModuleLevel);
		
		//Determine if this user has rights to edit a PO:
		boolean bAllowPOEditing = 
			SMSystemFunctions.isFunctionPermitted(
					SMSystemFunctions.ICEditPurchaseOrders, 
					sUserID, 
					conn,
					sLicenseModuleLevel
			);
		
		//Determine if this user has rights to view a PO:
		boolean bAllowPOPrinting = 
			SMSystemFunctions.isFunctionPermitted(
					SMSystemFunctions.ICPrintPurchaseOrders, 
					sUserID, 
					conn,
					sLicenseModuleLevel
			);
		
		printRowHeader(out);
		long lPOID = 0;
		long lLastPOID = 0;
		BigDecimal bdPOTotal = new BigDecimal(0);
		BigDecimal bdAccountTotal = new BigDecimal(0);
		BigDecimal bdGrandTotal = new BigDecimal(0);
		String sExpenseAcct = "";
		String sLastExpenseAcct = "";
		String sLastVendorString = "";
		try{
			if (bDebugMode){
				System.out.println("[1520280197] In " + this.toString() + " SQL: " + SQL);
			}
			//System.out.println("[1551108887] - SQL = '" + SQL + "'.");
			int iCount = 0;
			ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
			while(rs.next()){
				
				lPOID = rs.getLong(SMTableicporeceiptheaders.TableName 
						+ "." + SMTableicporeceiptheaders.lpoheaderid);
				sExpenseAcct = rs.getString(SMTableicporeceiptlines.TableName + "." 
						+ SMTableicporeceiptlines.sglexpenseacct);

				//If we're up to a new PO, OR if we getting into a different GL, print the last PO footer:
				if (
						((lLastPOID != 0) && (lPOID != lLastPOID))
						||
						((sExpenseAcct.compareToIgnoreCase(sLastExpenseAcct) != 0)
						&& (sLastExpenseAcct.compareToIgnoreCase("") != 0))
				){
					printPOFooter(
							lLastPOID,
							bdPOTotal,
							sLastVendorString,
							out
					);
					bdPOTotal = BigDecimal.ZERO;
					iCount = 0;
				}

				if (
						(sExpenseAcct.compareToIgnoreCase(sLastExpenseAcct) != 0)
						&& (sLastExpenseAcct.compareToIgnoreCase("") != 0)
						
				){
					printAccountFooter(
							sLastExpenseAcct,
							bdAccountTotal,
							out
					);
					bdAccountTotal = BigDecimal.ZERO;
					iCount = 0;
				}
				
				//Print the line:
				if(iCount%2 == 0) {
					out.println("<TR CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_ROW_EVEN + "\" >");
				}else {
					out.println("<TR CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_ROW_ODD + "\" >");
				}
								
				//PO:
				String sPOID = Long.toString(lPOID);
				String sPOLink = sPOID;
				if (bAllowPOEditing){
					sPOLink = 
						"<A HREF=\"" + SMUtilities.getURLLinkBase(context) + "smic.ICEditPOEdit"
						+ "?" + ICPOHeader.Paramlid + "=" + sPOID
						+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID + "\">" + sPOID + "</A>";
				}
				out.println("<TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_ALIGN_TOP + "\">" + sPOLink + "</TD>");
				
				//View?
				String sPOViewLink = "N/A";
				if (bAllowPOPrinting){
					sPOViewLink = 
						"<A HREF=\"" + SMUtilities.getURLLinkBase(context) + "smic.ICPrintPOGenerate"
						+ "?" + "StartingPOID" + "=" + sPOID
						+ "&" + "EndingPOID" + "="
						+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID + "\">" + "View" + "</A>";
				}
				out.println("<TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_ALIGN_TOP + "\">" + sPOViewLink + "</TD>");
				
				//Receipt #:
				String sEditReceiptLink = Long.toString(rs.getLong(
						SMTableicporeceiptheaders.TableName + "." + SMTableicporeceiptheaders.lid));
				if (bEditReceiptsPermitted){
					sEditReceiptLink = 
						"<A HREF=\"" + SMUtilities.getURLLinkBase(context) + "smic.ICEditReceiptEdit"
						+ "?" + ICPOHeader.Paramlid + "=" + sEditReceiptLink
						+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID + "\">" + sEditReceiptLink + "</A>";
				}
				out.println("<TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_ALIGN_TOP + "\">" + sEditReceiptLink + "</TD>");
				
				//Posted?:
				String sPosted = "N";
				if (rs.getInt(SMTableicporeceiptheaders.TableName + "." + SMTableicporeceiptheaders.lpostedtoic) != 0){
					sPosted = "Y";
				}
				out.println("<TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_ALIGN_TOP + "\">" + sPosted + "</TD>");
				
				//Created by:
				out.println("<TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_ALIGN_TOP + "\">"
						+ rs.getString(
								SMTableicporeceiptheaders.TableName + "." + SMTableicporeceiptheaders.screatedbyfullname) 
								+ "</TD>");
				
				//Location:
				out.println("<TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_ALIGN_TOP + "\">"
						+ rs.getString(SMTableicporeceiptlines.TableName + "." 
							+ SMTableicporeceiptlines.slocation) + "</TD>");

				//Item
				String sItemNumber = rs.getString(SMTableicporeceiptlines.TableName + "." 
						+ SMTableicporeceiptlines.sitemnumber);
				String sItemNumberLink = "";
				if (bViewItemPermitted && (rs.getLong(
						SMTableicporeceiptlines.TableName + "." + SMTableicporeceiptlines.lnoninventoryitem) == 0)){
					sItemNumberLink = "<A HREF=\"" + SMUtilities.getURLLinkBase(context) + "smic.ICDisplayItemInformation?ItemNumber=" 
				    		+ sItemNumber
				    		+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
				    		+ "\">" + clsServletUtilities.Fill_In_Empty_String_For_HTML_Cell(sItemNumber) + "</A>";
				}else{
					sItemNumberLink = sItemNumber;
				}
				out.println("<TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_ALIGN_TOP + "\">"+ sItemNumberLink + "</TD>");

				//Description:
				out.println("<TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_ALIGN_TOP + "\">"
					+ rs.getString(
					SMTableicporeceiptlines.TableName + "." + SMTableicporeceiptlines.sitemdescription) 
					+ "</TD>");
				
				//Item type: ITEMTYPE
				String sColor = "RED";
				if (rs.getString("ITEMTYPE").compareToIgnoreCase(ITEM_TYPE_STOCKINVENTORY) == 0){
					sColor="GREEN";
				}
				if (rs.getString("ITEMTYPE").compareToIgnoreCase(ITEM_TYPE_NONSTOCKINVENTORY) == 0){
					sColor="PURPLE";
				}
				
				out.println("<TD STYLE = \"font-family:arial;\"><FONT SIZE=2 COLOR=" + sColor + "><B>" 
					+ rs.getString("ITEMTYPE") 
					+ "</B></FONT></TD>");
				
				//Expense Acct:
				out.println("<TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_ALIGN_TOP + "\">" 
						+ rs.getString(
								SMTableicporeceiptlines.TableName + "." + SMTableicporeceiptlines.sglexpenseacct) 
								+ "</TD>");

				//Arrival date
				out.println("<TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_ALIGN_TOP + "\">"  +
						clsDateAndTimeConversions.sqlDateToString(
							rs.getDate(SMTableicporeceiptheaders.TableName + "." 
								+ SMTableicporeceiptheaders.datreceived), "M/d/yyyy") + "</FONT></TD>");
				
				//Qty
				out.println("<TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_ALIGN_TOP + "\">" 
						+ clsManageBigDecimals.BigDecimalToScaledFormattedString(
								4, rs.getBigDecimal(SMTableicporeceiptlines.TableName + "." 
								+ SMTableicporeceiptlines.bdqtyreceived)) 
								+ "</TD>");

				//U/M:
				out.println("<TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_ALIGN_TOP + "\">" 
					+ rs.getString(
					SMTableicporeceiptlines.TableName + "." + SMTableicporeceiptlines.sunitofmeasure) 
					+ "</TD>");
				
				out.println("<TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_ALIGN_TOP + "\">" 
					+ clsManageBigDecimals.BigDecimalToScaledFormattedString(
					2, rs.getBigDecimal(SMTableicporeceiptlines.TableName + "." 
					+ SMTableicporeceiptlines.bdextendedcost)) 
					+ "</TD>");
				
				out.println("</TR>");
				
				bdPOTotal = bdPOTotal.add(rs.getBigDecimal(SMTableicporeceiptlines.TableName + "." 
					+ SMTableicporeceiptlines.bdextendedcost));
				
				bdAccountTotal = bdAccountTotal.add(rs.getBigDecimal(SMTableicporeceiptlines.TableName + "." 
						+ SMTableicporeceiptlines.bdextendedcost));
				
				bdGrandTotal = bdGrandTotal.add(rs.getBigDecimal(SMTableicporeceiptlines.TableName + "." 
						+ SMTableicporeceiptlines.bdextendedcost));
				
				lLastPOID = lPOID;
				sLastExpenseAcct = sExpenseAcct;
				sLastVendorString = rs.getString(SMTableicpoheaders.svendor) + " - " + rs.getString(SMTableicpoheaders.svendorname); 
				iCount++;
			}
			rs.close();
    	}catch (SQLException e){
    		m_sErrorMessage = "Error [1520280773] reading resultset with SQL '" + SQL + "' - " + e.getMessage();
    		return false;
    	}

    	//Print the last PO footer:
		if ((lLastPOID != 0)){
			printPOFooter(
					lLastPOID,
					bdPOTotal,
					sLastVendorString,
					out
			);
		}
		if ((sLastExpenseAcct.compareToIgnoreCase("") != 0)){
			printAccountFooter(
					sLastExpenseAcct,
					bdAccountTotal,
					out
			);
		}
    	
		printReportFooter(bdGrandTotal, out);
		
    	out.println("</TABLE>");
		return true;
	}
	
	private void printRowHeader(
		PrintWriter out
	){
		out.println("<TABLE WIDTH = 100% CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_BASIC_WITHOUT_BORDER + "\">");
		out.println("<TR CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_HEADING + "\" >");
		out.println("<TD   CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD + "\" >PO #</TD>");
		out.println("<TD NOWRAP  CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD + "\" >View ?</TD>");
		out.println("<TD  NOWRAP CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD + "\" >Recipt #</TD>");
		out.println("<TD  CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD + "\" >Posted?</TD>");
		out.println("<TD  CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD + "\" >Received By</TD>");
		out.println("<TD  CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD + "\" >Loc.</TD>");
		out.println("<TD  CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD + "\" >Item #</TD>");
		out.println("<TD  CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD + "\" >Description</TD>");
		out.println("<TD  CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD + "\" >Item Type</TD>");
		out.println("<TD  CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD + "\" >Expense acct.</TD>");
		out.println("<TD  CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD + "\" >Received</TD>");
		out.println("<TD  CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD + "\" >Qty. recv'd</TD>");
		out.println("<TD  CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD + "\" >U/M</TD>");
		out.println("<TD  CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD + "\" >Recv'd cost</TD>");
		out.println("</TR>");

	}
	private void printPOFooter(
			Long sPOID,
			BigDecimal bdPOTotal,
			String sVendorNameAndAcct,
			PrintWriter out
		){
			out.println("<TR>");
			out.println("<TD COLSPAN = \"13\" CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD + "\" >"
				+ "Total&nbsp;for&nbsp;PO&nbsp;#&nbsp;" 
				+ Long.toString(sPOID) + " - (Vendor: " + sVendorNameAndAcct + ")</TD>");
			out.println("<TD ALIGN=RIGHT style=\"font-family:arial; border-top-style:solid;"
				+ " border-top-color:black; border-top-width:thin;\"><B><FONT SIZE=2>" 
				+ clsManageBigDecimals.BigDecimalToScaledFormattedString(
					SMTableicporeceiptlines.bdextendedcostScale, bdPOTotal)
				+ "</FONT></B></TD>");
			out.println("</TR>");
			out.println("<TR CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_SUB_HEADING+"\">");
			out.println("<TD COLSPAN = \"14\" CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_BREAK +"\">&nbsp;</TD>");
			out.println("</TR>");
		}
	private void printAccountFooter(
			String sGLAcct,
			BigDecimal bdAccountTotal,
			PrintWriter out
		){
		out.println("<TR CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_TOTAL +"\">");
		out.println("<TD COLSPAN = \"13\" CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD + "\" >"
			+ "Total&nbsp;for&nbsp;GL&nbsp;account&nbsp;" + sGLAcct + "</TD>");
		out.println("<TD ALIGN=RIGHT style=\"font-family:arial; border-top-style:solid;"
			+ " border-top-color:black; border-top-width:thin;\"><B><FONT SIZE=2>" 
			+ clsManageBigDecimals.BigDecimalToScaledFormattedString(
				SMTableicporeceiptlines.bdextendedcostScale, bdAccountTotal)
			+ "</FONT></B></TD>");
		out.println("</TR>");
		out.println("<TR CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_TOTAL +"\">");
		out.println("<TD COLSPAN=\"14\" CLASS=\"" + SMMasterStyleSheetDefinitions.TABLE_ROW_BREAK + "\">&nbsp;</TD>");
		out.println("</TR>");

		}
	private void printReportFooter(
			BigDecimal bdGrandTotal,
			PrintWriter out
		){
		out.println("<TR CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_TOTAL +"\">");
		out.println("<TD COLSPAN = \"13\" CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD + "\" >"
				+ "<FONT SIZE=2>Grand&nbsp;total:</TD>");
			out.println("<TD ALIGN=RIGHT style=\" border-top-style:solid;"
				+ " border-top-color:black; border-top-width:medium;\"><B><FONT SIZE=2>" 
				+ clsManageBigDecimals.BigDecimalToScaledFormattedString(
					SMTableicporeceiptlines.bdextendedcostScale, bdGrandTotal)
				+ "</FONT></B></TD>");
			out.println("</TR>");
		}
	public String getErrorMessage (){
		return m_sErrorMessage;
	}
}
