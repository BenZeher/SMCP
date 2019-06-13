package smic;

import java.sql.Connection;
import java.sql.ResultSet;

import javax.servlet.ServletContext;

import SMDataDefinition.SMMasterStyleSheetDefinitions;
import SMDataDefinition.SMTableapbatchentries;
import SMDataDefinition.SMTableapbatches;
import SMDataDefinition.SMTableapmatchinglines;
import SMDataDefinition.SMTableaptransactions;
import SMDataDefinition.SMTableicpoinvoiceheaders;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsDateAndTimeConversions;
import ServletUtilities.clsManageBigDecimals;
import smcontrolpanel.SMUtilities;

public class ICPOUnpostedReport {
	
	public String processReport(Connection conn,
			ServletContext context,
			String sDBID)
			throws Exception {
		
		String s = "";
		s += printTableHeading();
		s += printColumnHeadings();
		s += printReport(conn, context, sDBID);
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
		
		s += "  <TR class = \"" + SMMasterStyleSheetDefinitions.TABLE_ROW_BACKGROUNDCOLOR_WHITE + " \" >\n";
		s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_LEFT_JUSTIFIED + " \" >"
			+  "PO Invoice #" + sHeadingPadding
			+ "</TD>\n"
			
			+ "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_LEFT_JUSTIFIED + " \" >"
			+  "Vendor Acct" + sHeadingPadding
			+ "</TD>\n"
			
			+"    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_LEFT_JUSTIFIED + " \" >"
			+  "Vendor Name" + sHeadingPadding
			+ "</TD>\n"

			+"    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_LEFT_JUSTIFIED + " \" >"
			+  "Vendor Invoice #" + sHeadingPadding
			+ "</TD>\n"
			
			+"    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_LEFT_JUSTIFIED + " \" >"
			+  "Invoice Entry Date" + sHeadingPadding
			+ "</TD>\n"

			+"    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_LEFT_JUSTIFIED + " \" >"
			+  "Invoice Date" + sHeadingPadding
			+ "</TD>\n"
					
			+"    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_RIGHT_JUSTIFIED + " \" >"
			+ " Invoice Total"
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
	
	private String printReport(Connection conn,
			ServletContext context,
			String sDBID) 
			throws Exception {
		String s = "";
		String sIndent = "&nbsp;&nbsp;&nbsp;&nbsp;";
		String SQL = "SELECT"
				+ " " + SMTableicpoinvoiceheaders.TableName + "." + SMTableicpoinvoiceheaders.lid 
				+ " , "+ SMTableicpoinvoiceheaders.TableName + "." + SMTableicpoinvoiceheaders.svendor 
				+ " , "+ SMTableicpoinvoiceheaders.TableName + "." + SMTableicpoinvoiceheaders.svendorname
				+ " , "+ SMTableicpoinvoiceheaders.TableName + "." + SMTableicpoinvoiceheaders.sinvoicenumber
				+ " , "+ SMTableicpoinvoiceheaders.TableName + "." + SMTableicpoinvoiceheaders.datentered
				+ " , "+ SMTableicpoinvoiceheaders.TableName + "." + SMTableicpoinvoiceheaders.datinvoice
				+ " , "+ SMTableicpoinvoiceheaders.TableName + "." + SMTableicpoinvoiceheaders.bdinvoicetotal
				+ " FROM "
				+SMTableicpoinvoiceheaders.TableName;
				
				SQL+= " WHERE ("
				+ SMTableicpoinvoiceheaders.TableName +"."+SMTableicpoinvoiceheaders.lexportsequencenumber + " <= 0 )"
				;
				
				SQL+=" ORDER BY"
				+ " " +SMTableicpoinvoiceheaders.TableName +"."+SMTableicpoinvoiceheaders.lexportsequencenumber;
				
				String sPOID = "";
				try {
					ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
					while (rs.next()){
						sPOID =Long.toString(rs.getLong(SMTableicpoinvoiceheaders.TableName + "." + SMTableicpoinvoiceheaders.lid));
						String sPOIDLink = getLink(sPOID,context, sDBID);
						s += "  <TR class = \"" + SMMasterStyleSheetDefinitions.TABLE_ROW_BACKGROUNDCOLOR_LIGHTBLUE + " \" >\n";
						s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + " \" >"
							
							+  sIndent
							//PO ID Link
							+ "<I>" + sPOIDLink + "</I>"
							+ "</TD>\n"
							
							//Vendor ID
							+ "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + " \" >"
							+ "<I>" + rs.getString(SMTableicpoinvoiceheaders.TableName + "." + SMTableicpoinvoiceheaders.svendor) + "</I>"
							+ "</TD>\n"
							
							//Vendor Name
							+ "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + " \" >"
							+ "<I>" + rs.getString(SMTableicpoinvoiceheaders.TableName + "." + SMTableicpoinvoiceheaders.svendorname) + "</I>"
							+ "</TD>\n"
							
							//Vendor Invoice Number
							+ "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + " \" >"
							+ "<I>" + rs.getString(SMTableicpoinvoiceheaders.TableName + "." + SMTableicpoinvoiceheaders.sinvoicenumber) + "</I>"
							+ "</TD>\n"
							
							//Date Entered
							+ "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + " \" >"
							+ "<I>" + clsDateAndTimeConversions.resultsetDateStringToFormattedString(
								rs.getString(SMTableicpoinvoiceheaders.TableName + "." + SMTableicpoinvoiceheaders.datentered), SMUtilities.DATE_FORMAT_FOR_DISPLAY, SMUtilities.EMPTY_DATE_VALUE) + "</I>"
							+ "</TD>\n"
							
							//Invoice Date
							+ "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + " \" >"
							+ "<I>" + clsDateAndTimeConversions.resultsetDateStringToFormattedString(
								rs.getString(SMTableicpoinvoiceheaders.TableName + "." + SMTableicpoinvoiceheaders.datinvoice), SMUtilities.DATE_FORMAT_FOR_DISPLAY, SMUtilities.EMPTY_DATE_VALUE) + "</I>"
							+ "</TD>\n"
							
							//Invoice Total
							+ "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + " \" >"
							+ "<I>" + clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(rs.getBigDecimal(SMTableicpoinvoiceheaders.TableName + "." + SMTableicpoinvoiceheaders.bdinvoicetotal)) + "</I>"
							+ "</TD>\n"
							
							+  "&nbsp;"
							+ "</TD>\n"
							
							+ "  </TR>\n"
						;
					}
				}catch (Exception e) {
					throw new Exception("Error [1502730448] - reading query results with SQL: '" + SQL + "' - " + e.getMessage());
				}
		return s;
	}
	private String getLink(String sPOID,
			ServletContext context, 
			String sDBID) {
		String sPOIDLink = "<A HREF=\"" + SMUtilities.getURLLinkBase(context) + "smic.ICEditPOEdit?"
	    		+ SMTableicpoinvoiceheaders.lid + "=" + sPOID
				+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
				+ "\">" + sPOID + "</A>";
		return sPOIDLink;
	}
}
