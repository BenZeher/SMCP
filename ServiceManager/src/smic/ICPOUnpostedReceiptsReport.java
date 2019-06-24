package smic;

import java.sql.ResultSet;

import javax.servlet.ServletContext;

import SMDataDefinition.SMMasterStyleSheetDefinitions;
import SMDataDefinition.SMTableicpoinvoiceheaders;
import SMDataDefinition.SMTableicporeceiptheaders;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsDateAndTimeConversions;
import smcontrolpanel.SMUtilities;

public class ICPOUnpostedReceiptsReport {
	
	public String processReport(
			ServletContext context,
			String sDBID,
			String sCallingClass)
			throws Exception {
		
		String s = "";
		s += printTableHeading();
		s += printColumnHeadings();
		s += printReport(context, sDBID,sCallingClass);
		s += printTableFooting();
		return s;
	}
	private String printTableHeading(){
		String s = "";
		
		s += "<TABLE class = \"" + SMMasterStyleSheetDefinitions.TABLE_BASIC_WITHOUT_BORDER + "\" >\n";
		
		return s;
	}
	private String printColumnHeadings(){
		String s = "";
		String sHeadingPadding = "&nbsp;&nbsp;";
		
		s += "  <TR class = \"" + SMMasterStyleSheetDefinitions.TABLE_HEADING + " \" >\n";
		s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_LEFT_JUSTIFIED + " \" >"
			+  "Receipt #" + sHeadingPadding
			+ "</TD>\n"
			
			+ "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_LEFT_JUSTIFIED + " \" >"
			+  "PO #" + sHeadingPadding
			+ "</TD>\n"
			
			+"    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_LEFT_JUSTIFIED + " \" >"
			+  "Date Received" + sHeadingPadding
			+ "</TD>\n"

			+"    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_LEFT_JUSTIFIED + " \" >"
			+  "Created by" + sHeadingPadding
			+ "</TD>\n"

		;
		s += "  </TR>\n";
		return s;
	}
	private String printTableFooting(){
		String s = "";
		
		s += "</TABLE>\n";
		
		return s;
	}
	
	private String printReport(
			ServletContext context,
			String sDBID,
			String sCallingClass) 
			throws Exception {
		String s = "";
		String sIndent = "&nbsp;&nbsp;&nbsp;&nbsp;";
		String SQL = "SELECT"
				+ " " + SMTableicporeceiptheaders.TableName + "." + SMTableicporeceiptheaders.lid 
				+ " , "+ SMTableicporeceiptheaders.TableName + "." + SMTableicporeceiptheaders.lpoheaderid 
				+ " , "+ SMTableicporeceiptheaders.TableName + "." + SMTableicporeceiptheaders.datreceived
				+ " , "+ SMTableicporeceiptheaders.TableName + "." + SMTableicporeceiptheaders.screatedbyfullname
				+ " FROM "
				+SMTableicporeceiptheaders.TableName;
				
				SQL+= " WHERE ("
				+ SMTableicporeceiptheaders.TableName + "." + SMTableicporeceiptheaders.lpostedtoic + " <= 0 )"
				+ "AND (" + SMTableicporeceiptheaders.TableName + "." + SMTableicporeceiptheaders.datdeleted + " = 0)"
				;
				
				SQL+=" ORDER BY"
				+ " " +SMTableicporeceiptheaders.TableName + "." + SMTableicporeceiptheaders.datreceived 
				+ ", "+SMTableicporeceiptheaders.TableName + "." + SMTableicporeceiptheaders.lid
				;

				String sPOID = "";
				String sReceiptID = "";
				int alt = 1;
				try {
					ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, context, sDBID, "MySQL", sCallingClass);
					while (rs.next()) {
						sPOID = Long.toString(
								rs.getLong(SMTableicporeceiptheaders.TableName + "." + SMTableicporeceiptheaders.lpoheaderid));
						sReceiptID = Long.toString(
								rs.getLong(SMTableicporeceiptheaders.TableName + "." + SMTableicporeceiptheaders.lid));
						String sPOIDLink = getLinkPO(sPOID, context, sDBID, sCallingClass);
						String sReceiptIDLink = getLinkReceipt(sReceiptID, sPOID, context, sDBID, sCallingClass);

						if(alt%2 == 1) {
							s += "  <TR class = \"" + SMMasterStyleSheetDefinitions.TABLE_ROW_ODD + " \" >\n";
							alt++;
						}else {
							s += "  <TR class = \"" + SMMasterStyleSheetDefinitions.TABLE_ROW_EVEN + " \" >\n";
							alt++;
						}
						s += "    <TD class = \""
								+ SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + " \" >"

						+ sIndent
						// Receipt Link
						+ "<I>" + sReceiptIDLink + "</I>" + "</TD>\n"
						// PO Link
						+ "    <TD class = \""
						+ SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + " \" >"
						+ "<I>" + sPOIDLink + "</I>" + "</TD>\n"

						// Date Received
						+ "    <TD class = \""
						+ SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + " \" >"
						+ "<I>"
						+ clsDateAndTimeConversions.resultsetDateStringToFormattedString(
								rs.getString(SMTableicporeceiptheaders.TableName + "."
										+ SMTableicporeceiptheaders.datreceived),
								SMUtilities.DATE_FORMAT_FOR_DISPLAY, SMUtilities.EMPTY_DATE_VALUE)
						+ "</I>" + "</TD>\n"

						// Created By
						+ "    <TD class = \""
						+ SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + " \" >"
						+ "<I>" + rs.getString(SMTableicporeceiptheaders.TableName + "."
								+ SMTableicporeceiptheaders.screatedbyfullname)
						+ "</I>" + "</TD>\n";

						s += "&nbsp;" + "</TD>\n"

						+ "  </TR>\n";
					}
					rs.close();
				} catch (Exception e) {
					throw new Exception(
							"Error [1560448136] - reading query results with SQL: '" + SQL + "' - " + e.getMessage());
				}
				return s;
	}

	private String getLinkReceipt(String sReceiptID, 
			String sPOID, 
			ServletContext context, 
			String sDBID,
			String sCallingClass) {
			String sReceiptIDLink = "<A HREF=\"" + SMUtilities.getURLLinkBase(context) + "smic.ICEditReceiptEdit?"
				+ SMTableicpoinvoiceheaders.lpoheaderid + "=" + sPOID + "&" + SMTableicpoinvoiceheaders.lid + "="
				+ sReceiptID + "&Callingclass=" + sCallingClass + "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "="
				+ sDBID + "\">" + sReceiptID + "</A>";
		return sReceiptIDLink;
	}

	private String getLinkPO(String sPOID, 
			ServletContext context, 
			String sDBID, 
			String sCallingClass) {
			String sPOIDLink = "<A HREF=\"" + SMUtilities.getURLLinkBase(context) + "smic.ICEditPOEdit?"
				+ SMTableicpoinvoiceheaders.lid + "=" + sPOID + "&Callingclass=" + sCallingClass + "&"
				+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID + "\">" + sPOID + "</A>";
		return sPOIDLink;
	}
}
