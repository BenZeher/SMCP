package smic;

import java.io.PrintWriter;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.servlet.ServletContext;

import smcontrolpanel.SMSystemFunctions;
import smcontrolpanel.SMUtilities;
import SMClasses.SMLogEntry;
import SMDataDefinition.SMMasterStyleSheetDefinitions;
import SMDataDefinition.SMTableicitems;
import SMDataDefinition.SMTableictransactions;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsDateAndTimeConversions;
import ServletUtilities.clsManageBigDecimals;
import ServletUtilities.clsServletUtilities;

public class ICTransactionHistory extends java.lang.Object{

	private String m_sErrorMessage;

	public ICTransactionHistory(
	){
		m_sErrorMessage = "";
	}
	public boolean processReport(
			Connection conn,
			String sStartingItem,
			String sEndingItem,
			String sStartingDate,
			String sEndingDate,
			boolean bShowCostingDetails,
			boolean bIncludeShipments,
			boolean bIncludeReceipts,
			boolean bIncludeAdjustments,
			boolean bIncludeTransfers,
			boolean bIncludePhysicalCounts,
			String sDBID,
			String sUserID,
			String sUserFullName,
			PrintWriter out,
			ServletContext context,
			String sLicenseModuleLevel
	){

		String sTransactionTypeQualifier = "";

		if (bIncludeShipments){
			sTransactionTypeQualifier += 
				"(" + SMTableictransactions.TableName + "." + SMTableictransactions.ientrytype + " = "
				+ Integer.toString(ICEntryTypes.SHIPMENT_ENTRY) + ")";
		}

		if (bIncludeReceipts){
			if (sTransactionTypeQualifier.compareToIgnoreCase("") == 0){
				sTransactionTypeQualifier += 
					"(" + SMTableictransactions.TableName + "." + SMTableictransactions.ientrytype + " = "
					+ Integer.toString(ICEntryTypes.RECEIPT_ENTRY) + ")";
			}else{
				sTransactionTypeQualifier += 
					" OR (" + SMTableictransactions.TableName + "." + SMTableictransactions.ientrytype + " = "
					+ Integer.toString(ICEntryTypes.RECEIPT_ENTRY) + ")";
			}
		}

		if (bIncludeAdjustments){
			if (sTransactionTypeQualifier.compareToIgnoreCase("") == 0){
				sTransactionTypeQualifier += 
					"(" + SMTableictransactions.TableName + "." + SMTableictransactions.ientrytype + " = "
					+ Integer.toString(ICEntryTypes.ADJUSTMENT_ENTRY) + ")";
			}else{
				sTransactionTypeQualifier += 
					" OR (" + SMTableictransactions.TableName + "." + SMTableictransactions.ientrytype + " = "
					+ Integer.toString(ICEntryTypes.ADJUSTMENT_ENTRY) + ")";
			}
		}

		if (bIncludeTransfers){
			if (sTransactionTypeQualifier.compareToIgnoreCase("") == 0){
				sTransactionTypeQualifier += 
					"(" + SMTableictransactions.TableName + "." + SMTableictransactions.ientrytype + " = "
					+ Integer.toString(ICEntryTypes.TRANSFER_ENTRY) + ")";
			}else{
				sTransactionTypeQualifier += 
					" OR (" + SMTableictransactions.TableName + "." + SMTableictransactions.ientrytype + " = "
					+ Integer.toString(ICEntryTypes.TRANSFER_ENTRY) + ")";
			}
		}

		if (bIncludePhysicalCounts){
			if (sTransactionTypeQualifier.compareToIgnoreCase("") == 0){
				sTransactionTypeQualifier += 
					"(" + SMTableictransactions.TableName + "." + SMTableictransactions.ientrytype + " = "
					+ Integer.toString(ICEntryTypes.PHYSICALCOUNT_ENTRY) + ")";
			}else{
				sTransactionTypeQualifier += 
					" OR (" + SMTableictransactions.TableName + "." + SMTableictransactions.ientrytype + " = "
					+ Integer.toString(ICEntryTypes.PHYSICALCOUNT_ENTRY) + ")";
			}
		}

		if (sTransactionTypeQualifier.compareToIgnoreCase("") != 0){
			sTransactionTypeQualifier = " AND (" + sTransactionTypeQualifier + ")";
		}

		String SQL = "SELECT"
			+ " " + SMTableictransactions.TableName + "." + SMTableictransactions.bdcost
			+ ", " + SMTableictransactions.TableName + "." + SMTableictransactions.bdqty
			+ ", " + SMTableictransactions.TableName + "." + SMTableictransactions.datpostingdate
			+ ", " + SMTableictransactions.TableName + "." + SMTableictransactions.ientrytype
			+ ", " + SMTableictransactions.TableName + "." + SMTableictransactions.lid
			+ ", " + SMTableictransactions.TableName + "." + SMTableictransactions.loriginalbatchnumber
			+ ", " + SMTableictransactions.TableName + "." + SMTableictransactions.loriginalentrynumber
			+ ", " + SMTableictransactions.TableName + "." + SMTableictransactions.loriginallinenumber
			+ ", " + SMTableictransactions.TableName + "." + SMTableictransactions.sdocnumber
			+ ", " + SMTableictransactions.TableName + "." + SMTableictransactions.sentrydescription
			+ ", " + SMTableictransactions.TableName + "." + SMTableictransactions.sitemnumber
			+ ", " + SMTableictransactions.TableName + "." + SMTableictransactions.slinedescription
			+ ", " + SMTableictransactions.TableName + "." + SMTableictransactions.slocation
			+ ", " + SMTableictransactions.TableName + "." + SMTableictransactions.spostedbyfullname
			+ ", " + SMTableictransactions.TableName + "." + SMTableictransactions.sunitofmeasure
			+ ", " + SMTableicitems.TableName + "." + SMTableicitems.sItemDescription
			+ " FROM " + SMTableictransactions.TableName
			+ ", " + SMTableicitems.TableName
			+ " WHERE ("
			+ "(" + SMTableictransactions.TableName + "." + SMTableictransactions.sitemnumber 
			+ " >= '" + sStartingItem + "')"
			+ " AND (" + SMTableictransactions.TableName + "." + SMTableictransactions.sitemnumber 
			+ " <= '" + sEndingItem + "')"
			+ " AND (" + SMTableictransactions.TableName + "." + SMTableictransactions.datpostingdate 
			+ " >= '" + sStartingDate + " 00:00:00')"
			+ " AND (" + SMTableictransactions.TableName + "." + SMTableictransactions.datpostingdate 
			+ " <= '" + sEndingDate + " 23:59:59')"
			//Link:
			+ " AND (" + SMTableictransactions.TableName + "." + SMTableictransactions.sitemnumber + " = "
			+ SMTableicitems.TableName + "." + SMTableicitems.sItemNumber + ")"

			//Include which transactions:
			+ sTransactionTypeQualifier	

			+ ")"	//Complete the 'where' clause
			+ " ORDER BY "
			+ SMTableictransactions.TableName + "." + SMTableictransactions.slocation
			+ ", " + SMTableictransactions.TableName + "." + SMTableictransactions.datpostingdate
			+ ", " + SMTableictransactions.TableName + "." + SMTableictransactions.sitemnumber
			+ ", " + SMTableictransactions.TableName + "." + SMTableictransactions.loriginalbatchnumber
			+ ", " + SMTableictransactions.TableName + "." + SMTableictransactions.loriginalentrynumber
			+ ", " + SMTableictransactions.TableName + "." + SMTableictransactions.loriginallinenumber
			;

		//System.out.println("In " + this.toString() + ".processReport - main SQL = " + SQL);

		String sCurrentLocation = "";

		//Check permissions for viewing items:
		boolean bViewItemPermitted = SMSystemFunctions.isFunctionPermitted(
				SMSystemFunctions.ICDisplayItemInformation,
				sUserID,
				conn,
				sLicenseModuleLevel);

		//Check permissions for viewing receipts:
		boolean bViewReceiptPermitted = SMSystemFunctions.isFunctionPermitted(
				SMSystemFunctions.ICEditReceipts,
				sUserID,
				conn,
				sLicenseModuleLevel);

		BigDecimal bdNetChange = new BigDecimal(0);
		try{
			ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
			int iCounter = 0;
			while(rs.next()){
				
				/*if (iCounter == 100){
					out.println("</TABLE><TABLE BORDER=0>");
					iCounter = 0;
				}*/
				String sLocation = rs.getString(SMTableictransactions.TableName 
						+ "." + SMTableictransactions.slocation).trim();

				//If there was a previous line, and the location has changed, print the location footer:
				if (
						(sLocation.compareToIgnoreCase(sCurrentLocation) != 0)
						&& (sCurrentLocation.compareToIgnoreCase("") !=0)
				){
					printLocationFooter(sCurrentLocation, bdNetChange, out);
					bdNetChange = BigDecimal.ZERO;
				}

				if (sLocation.compareToIgnoreCase(sCurrentLocation) != 0){
					printLocationHeader(sLocation, out);
					//Reset the markers:
					sCurrentLocation = sLocation;
				}

				//Print the line:
				if(iCounter%2 == 0) {
					out.println("<TR CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_ROW_EVEN + "\" >");
				}else {
					out.println("<TR CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_ROW_ODD + "\" >");
				}
				//Transaction date:
				out.println("<TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\">" + 
						clsDateAndTimeConversions.sqlDateToString(
								rs.getDate(SMTableictransactions.TableName + "." 
										+ SMTableictransactions.datpostingdate), "M/d/yyyy") + "</TD>");

				//Item number:
				String sItemNumber = rs.getString(SMTableictransactions.TableName + "." 
						+ SMTableictransactions.sitemnumber);
				String sItemNumberLink = "";
				if (bViewItemPermitted){
					sItemNumberLink = "<A HREF=\"" + SMUtilities.getURLLinkBase(context) + "smic.ICDisplayItemInformation?ItemNumber=" 
					+ sItemNumber
					+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
					+ "\">" + clsServletUtilities.Fill_In_Empty_String_For_HTML_Cell(sItemNumber) + "</A>";
				}else{
					sItemNumberLink = sItemNumber;
				}

				out.println("<TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\">"  + sItemNumberLink + "</TD>");
				//Item description:
				out.println("<TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\">"  
						+ rs.getString(SMTableicitems.TableName + "." 
								+ SMTableicitems.sItemDescription) + "</TD>");

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

				//Document number
				//If it's a receipt, add a link here to the receipt:
				String sReceiptNumber = rs.getString(SMTableictransactions.TableName + "." 
						+ SMTableictransactions.sdocnumber);
				if ((sType.compareToIgnoreCase("Receipt") == 0) && bViewReceiptPermitted){
					out.println("<TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\">" 
							+ "<A HREF=\"" + SMUtilities.getURLLinkBase(context) 
							+ "smic.ICEditReceiptEdit?lid=" + sReceiptNumber 
									+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
									+ "\">" + clsServletUtilities.Fill_In_Empty_String_For_HTML_Cell(sReceiptNumber) + "</A>"
							+ "</TD>"
					);
				}else{
					out.println("<TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\">"  + sReceiptNumber	+ "</TD>");
				}
				
				out.println("<TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\">"  + sType + "</FONT></TD>");
				out.println("<TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\">" 
						+ rs.getString(SMTableictransactions.TableName + "." 
								+ SMTableictransactions.sentrydescription) + "</TD>");
				out.println("<TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\">" 
						+ rs.getString(SMTableictransactions.TableName + "." 
								+ SMTableictransactions.slinedescription) + "</TD>");
				out.println("<TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\">" 
						+ Long.toString(rs.getLong(SMTableictransactions.TableName + "." 
								+ SMTableictransactions.loriginalbatchnumber)) 
								+ "</TD>");
				out.println("<TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\">" 
						+ Long.toString(rs.getLong(SMTableictransactions.TableName + "." 
								+ SMTableictransactions.loriginalentrynumber)) 
								+ "</TD>");
				out.println("<TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\">" 
						+ Long.toString(rs.getLong(SMTableictransactions.TableName + "." 
								+ SMTableictransactions.loriginallinenumber)) 
								+ "</TD>");
				out.println("<TD NOWRAP CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\">" 
						+ rs.getString(SMTableictransactions.TableName + "." 
								+ SMTableictransactions.spostedbyfullname) + "</TD>");
				out.println("<TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\">" 
						+ clsManageBigDecimals.BigDecimalToFormattedString("###,###,##0.0000", bdQty) 
						+ "</TD>");
				out.println("<TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\">" 
						+ rs.getString(SMTableictransactions.TableName + "." 
								+ SMTableictransactions.sunitofmeasure) + "</TD>");

				out.println("<TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\">" 
						+ "<A HREF=\"" + SMUtilities.getURLLinkBase(context) + "smic.ICTransactionDetailsDisplay"
						+ "?CallingClass=" + SMUtilities.getFullClassName(this.toString())
						+ "&" + "ICTransactionID" + "=" + rs.getLong(
								SMTableictransactions.TableName + "." + SMTableictransactions.lid)
								+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
								+ "\">"
								+ clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(
										rs.getBigDecimal(SMTableictransactions.TableName + "." 
												+ SMTableictransactions.bdcost)) 
												+ "</A>"
												+ "</FONT></TD>"
				);

				out.println("</TR>");
				//Accumulate the net change:
				bdNetChange = bdNetChange.add(rs.getBigDecimal(SMTableictransactions.TableName + "." 
						+ SMTableictransactions.bdcost));
				iCounter++;
			}
			rs.close();
		}catch (SQLException e){
			m_sErrorMessage = "Error reading resultset - " + e.getMessage();
			return false;
		}

		//If there was anything printed, print the last footer:
		if (sCurrentLocation.compareToIgnoreCase("") !=0)
		{
			printLocationFooter(sCurrentLocation, bdNetChange, out);
		}

		//Print the grand totals:
		printReportFooter(out);

		SMLogEntry log = new SMLogEntry(conn);
		log.writeEntry(sUserID, SMLogEntry.LOG_OPERATION_ICTRANSACTIONHISTORYREPORT, "REPORT", "IC Transaction History Report", "[1376509412]");

		return true;
	}

	private void printLocationHeader(
			String sLocation,
			PrintWriter out
	){
		out.println("<TABLE WIDTH = 100% CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_BASIC_WITHOUT_BORDER + "\">");
		out.println("<TR CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_HEADING + "\" >");
		out.println("<TD COLSPAN=\"14\" CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD + "\" ><U>Location: " + sLocation + "</U></TD>");
		out.println("</TR>");
		
		out.println("<TR CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_HEADING + "\" >");
		out.println("<TD  CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD + "\" >Date</TD>");
		out.println("<TD  CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD + "\" >Item</TD>");
		out.println("<TD  CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD + "\" >Item Desc.</TD>");
		out.println("<TD  CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD + "\" >Doc. #</TD>");
		out.println("<TD  CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD + "\" >Type</TD>");
		out.println("<TD  CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD + "\" >Entry desc.</TD>");
		out.println("<TD  CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD + "\" >Line desc.</TD>");
		out.println("<TD  CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD + "\" >Batch</TD>");
		out.println("<TD  CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD + "\" >Entry</TD>");
		out.println("<TD  CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD + "\" >Line</TD>");
		out.println("<TD NOWRAP  CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD + "\" >Posted by</TD>");
		out.println("<TD  CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD + "\" >Qty.</TD>");
		out.println("<TD  CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD + "\" >Unit</TD>");
		out.println("<TD  CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD + "\" >Cost</TD>");
		out.println("</TR>");
	}
	private void printLocationFooter(
			String sLocation,
			BigDecimal bdNetChange,
			PrintWriter out
	){
		out.println(
				"<TR CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_TOTAL + "\" >"
				+"<TD COLSPAN=\"13\" CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD + "\" >"
				+ "Net cost change for location " + sLocation 
				+ ":</TD>"
				+ "<TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\">" 
				+ clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(bdNetChange)
				+ "</TD>"
		);
		out.println("</TR></TABLE><BR>");
	}
	private void printReportFooter(
			PrintWriter out
	){
		out.println("<BR>");
	}
	public String getErrorMessage (){
		return m_sErrorMessage;
	}
}
