package smic;

import java.io.PrintWriter;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.servlet.ServletContext;

import smar.ARUtilities;
import smcontrolpanel.SMSystemFunctions;
import smcontrolpanel.SMUtilities;
import SMClasses.SMLogEntry;
import SMDataDefinition.SMTableicitems;
import SMDataDefinition.SMTableictransactions;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsDateAndTimeConversions;
import ServletUtilities.clsManageBigDecimals;

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
				
				if (iCounter == 100){
					out.println("</TABLE><TABLE BORDER=0>");
					iCounter = 0;
				}
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
				out.println("<TR>");
				//Transaction date:
				out.println("<TD><FONT SIZE=2>" + 
						clsDateAndTimeConversions.sqlDateToString(
								rs.getDate(SMTableictransactions.TableName + "." 
										+ SMTableictransactions.datpostingdate), "M/d/yyyy") + "</FONT></TD>");

				//Item number:
				String sItemNumber = rs.getString(SMTableictransactions.TableName + "." 
						+ SMTableictransactions.sitemnumber);
				String sItemNumberLink = "";
				if (bViewItemPermitted){
					sItemNumberLink = "<A HREF=\"" + SMUtilities.getURLLinkBase(context) + "smic.ICDisplayItemInformation?ItemNumber=" 
					+ sItemNumber
					+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
					+ "\">" + ARUtilities.Fill_In_Empty_String_For_HTML_Cell(sItemNumber) + "</A>";
				}else{
					sItemNumberLink = sItemNumber;
				}

				out.println("<TD><FONT SIZE=2>" + sItemNumberLink + "</FONT></TD>");
				//Item description:
				out.println("<TD><FONT SIZE=2>" 
						+ rs.getString(SMTableicitems.TableName + "." 
								+ SMTableicitems.sItemDescription) + "</FONT></TD>");

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
					out.println("<TD><FONT SIZE=2>"
							+ "<A HREF=\"" + SMUtilities.getURLLinkBase(context) 
							+ "smic.ICEditReceiptEdit?lid=" + sReceiptNumber 
									+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
									+ "\">" + ARUtilities.Fill_In_Empty_String_For_HTML_Cell(sReceiptNumber) + "</A>"
							+ "</FONT></TD>"
					);
				}else{
					out.println("<TD><FONT SIZE=2>" + sReceiptNumber	+ "</FONT></TD>");
				}
				
				out.println("<TD><FONT SIZE=2>" + sType + "</FONT></TD>");
				out.println("<TD><FONT SIZE=2>" 
						+ rs.getString(SMTableictransactions.TableName + "." 
								+ SMTableictransactions.sentrydescription) + "</FONT></TD>");
				out.println("<TD><FONT SIZE=2>" 
						+ rs.getString(SMTableictransactions.TableName + "." 
								+ SMTableictransactions.slinedescription) + "</FONT></TD>");
				out.println("<TD ALIGN=RIGHT><FONT SIZE=2>" 
						+ Long.toString(rs.getLong(SMTableictransactions.TableName + "." 
								+ SMTableictransactions.loriginalbatchnumber)) 
								+ "</FONT></TD>");
				out.println("<TD ALIGN=RIGHT><FONT SIZE=2>" 
						+ Long.toString(rs.getLong(SMTableictransactions.TableName + "." 
								+ SMTableictransactions.loriginalentrynumber)) 
								+ "</FONT></TD>");
				out.println("<TD ALIGN=RIGHT><FONT SIZE=2>" 
						+ Long.toString(rs.getLong(SMTableictransactions.TableName + "." 
								+ SMTableictransactions.loriginallinenumber)) 
								+ "</FONT></TD>");
				out.println("<TD ALIGN=RIGHT><FONT SIZE=2>" 
						+ rs.getString(SMTableictransactions.TableName + "." 
								+ SMTableictransactions.spostedbyfullname) + "</FONT></TD>");
				out.println("<TD ALIGN=RIGHT><FONT SIZE=2>" 
						+ clsManageBigDecimals.BigDecimalToFormattedString("###,###,##0.0000", bdQty) 
						+ "</FONT></TD>");
				out.println("<TD><FONT SIZE=2>" 
						+ rs.getString(SMTableictransactions.TableName + "." 
								+ SMTableictransactions.sunitofmeasure) + "</FONT></TD>");

				out.println("<TD ALIGN=RIGHT><FONT SIZE=2>"
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
		out.println("<B><U>Location: " + sLocation + "</U></B><BR>");
		out.println("<TABLE BORDER=0>");
		out.println("<TR>");
		out.println("<TD><B><FONT SIZE=2>Date</FONT></B></TD>");
		out.println("<TD><B><FONT SIZE=2>Item</FONT></B></TD>");
		out.println("<TD><B><FONT SIZE=2>Item Desc.</FONT></B></TD>");
		out.println("<TD><B><FONT SIZE=2>Doc. #</FONT></B></TD>");
		out.println("<TD><B><FONT SIZE=2>Type</FONT></B></TD>");
		out.println("<TD><B><FONT SIZE=2>Entry desc.</FONT></B></TD>");
		out.println("<TD><B><FONT SIZE=2>Line desc.</FONT></B></TD>");
		out.println("<TD ALIGN=RIGHT><B><FONT SIZE=2>Batch</FONT></B></TD>");
		out.println("<TD ALIGN=RIGHT><B><FONT SIZE=2>Entry</FONT></B></TD>");
		out.println("<TD ALIGN=RIGHT><B><FONT SIZE=2>Line</FONT></B></TD>");
		out.println("<TD ALIGN=RIGHT><B><FONT SIZE=2>Posted by</FONT></B></TD>");
		out.println("<TD ALIGN=RIGHT><B><FONT SIZE=2>Qty.</FONT></B></TD>");
		out.println("<TD><B><FONT SIZE=2>Unit</FONT></B></TD>");
		out.println("<TD ALIGN=RIGHT><B><FONT SIZE=2>Cost</FONT></B></TD>");
		out.println("</TR>");
	}
	private void printLocationFooter(
			String sLocation,
			BigDecimal bdNetChange,
			PrintWriter out
	){
		out.println(
				"<TR><TD ALIGN=RIGHT COLSPAN=13><FONT SIZE=2>"
				+ "<B>Net cost change for location " + sLocation 
				+ ":</B></FONT></TD>"
				+ "<TD ALIGN=RIGHT><FONT SIZE=2>" 
				+ clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(bdNetChange)
				+ "</FONT></TD>"
		);
		out.println("</TR></TABLE>");
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
