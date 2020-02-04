package smic;

import java.io.PrintWriter;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.servlet.ServletContext;

import SMClasses.SMLogEntry;
import SMDataDefinition.SMMasterStyleSheetDefinitions;
import SMDataDefinition.SMTableicitemprices;
import SMDataDefinition.SMTableicitems;
import SMDataDefinition.SMTablepricelistcodes;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsManageBigDecimals;
import ServletUtilities.clsServletUtilities;
import smcontrolpanel.SMPriceLevelLabels;
import smcontrolpanel.SMSystemFunctions;
import smcontrolpanel.SMUtilities;

public class ICViewItemPricingReport extends java.lang.Object{

	private String m_sErrorMessage;
	
	public ICViewItemPricingReport(
			){
		m_sErrorMessage = "";
	}
	public boolean processReport(
			Connection conn,
			String sStartingItem,
			String sEndingItem,
			String sStartingPriceCode,
			String sEndingPriceCode,
			String sDBID,
			String sUserID,
			String sUserFullName,
			PrintWriter out,
			ServletContext context,
			String sLicenseModuleLevel
			){
	
		if (!createTempTable (
				sStartingItem, 
				sEndingItem, 
				sStartingPriceCode, 
				sEndingPriceCode, 
				conn)){
		}
				
		//Check permissions for viewing items:
		boolean bItemViewingPermitted = SMSystemFunctions.isFunctionPermitted(
			SMSystemFunctions.ICDisplayItemInformation,
			sUserID,
			conn,
			sLicenseModuleLevel);
		
		//Check permissions for editing prices:
		boolean bEditPricingPermitted = SMSystemFunctions.isFunctionPermitted(
			SMSystemFunctions.ICEditItemPricing,
			sUserID,
			conn,
			sLicenseModuleLevel);
		String sCurrentItem = "";
		String SQL = "SELECT * FROM ICPRICES"
			+ " ORDER BY sitemnumber, spricelistcode"
			;

		int iCount = 0;
		out.println("<TABLE WIDTH=100% CLASS= \"" + SMMasterStyleSheetDefinitions.TABLE_BASIC_WITH_BORDER + "\">");
    	try{
			ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
			while(rs.next()){
				String sItem = rs.getString("sitemnumber").trim();


				
				//If it's a new item, print the item header:
				if (sCurrentItem.compareToIgnoreCase(sItem) !=0){
					printItemHeader(
							sItem, 
							rs.getString("sdesc"), 
							rs.getString("suom"),
							sDBID,
							bItemViewingPermitted,
							out,
							context,
							conn);
					iCount=0;
				}
				
				printItemPrices(
						sItem,
						rs.getString("spricelistcode"),
						rs.getString("spricelistcodedesc"),
						rs.getBigDecimal("bdbaseprice"),
						rs.getBigDecimal("bdlevel1price"),
						rs.getBigDecimal("bdlevel2price"),
						rs.getBigDecimal("bdlevel3price"),
						rs.getBigDecimal("bdlevel4price"),
						rs.getBigDecimal("bdlevel5price"),
						sDBID,
						bEditPricingPermitted, 
						out, 
						conn,
						context,
						iCount);
				
				sCurrentItem = sItem;
				iCount++;
			}
			rs.close();
    	}catch (Exception e){
    		m_sErrorMessage = "Error [1580855424] reading resultset - " + e.getMessage();
    		return false;
    	}
    	
    	//If there was at least one item, print a footer:
		if (sCurrentItem.compareToIgnoreCase("") !=0){
			printItemFooter(out);
		}
    	
		//Print the report footer:
		printReportFooter(out);
		
	    SMLogEntry log = new SMLogEntry(conn);
	    log.writeEntry(sUserID, SMLogEntry.LOG_OPERATION_ICVIEWITEMPRICINGREPORT, "REPORT", "IC View Item Pricing Report", "[1376509416]");
	    
		SQL = "DROP TEMPORARY TABLE IF EXISTS ICPRICES"
			;
		try {
			if (!clsDatabaseFunctions.executeSQL(SQL, conn)){
				//Let it go . . . 
			}
		} catch (SQLException e) {
			//Let it go . . .
		}

		return true;
	}
	private boolean createTempTable(
			String sStartingItem, 
			String sEndingItem, 
			String sStartingCode, 
			String sEndingCode, 
			Connection con
			){
	
		String SQL = "DROP TEMPORARY TABLE IF EXISTS ICPRICES";
		try {
			if (!clsDatabaseFunctions.executeSQL(SQL, con)){
				//Don't do anything
			}
		} catch (SQLException e) {
			//Don't do anything		
		}
		
		SQL = "CREATE TEMPORARY TABLE ICPRICES ("
			+ " sitemnumber varchar(24) NOT NULL DEFAULT ''"
			+ ", spricelistcode varchar(6) NOT NULL DEFAULT ''"
			+ ", spricelistcodedesc varchar(60) NOT NULL DEFAULT ''"
			+ ", suom varchar(10) NOT NULL DEFAULT ''"
			+ ", sdesc varchar(75) NOT NULL DEFAULT ''"
			+ ", bdbaseprice decimal(17,2) NOT NULL DEFAULT '0.00'"
			+ ", bdlevel1price decimal(17,2) NOT NULL DEFAULT '0.00'"
			+ ", bdlevel2price decimal(17,2) NOT NULL DEFAULT '0.00'"
			+ ", bdlevel3price decimal(17,2) NOT NULL DEFAULT '0.00'"
			+ ", bdlevel4price decimal(17,2) NOT NULL DEFAULT '0.00'"
			+ ", bdlevel5price decimal(17,2) NOT NULL DEFAULT '0.00'"
			+ ", PRIMARY KEY (spricelistcode, sitemnumber)"
			+ ")" // Engine=MyISAM"
		;

		try {
			if (!clsDatabaseFunctions.executeSQL(SQL, con)){
				m_sErrorMessage = "Could not create temporary table.";
				return false;
			}
		} catch (SQLException e) {
			m_sErrorMessage = "Could not create temporary table - " + e.getMessage()+ ".";
			return false;		
		}
		
		SQL = "INSERT INTO ICPRICES"
			+ " ("
			+ "sitemnumber"
			+ ", spricelistcode"
			+ ", spricelistcodedesc"
			+ ", sdesc"
			+ ", suom"
			+ ")"
			+ " SELECT"
			+ " " + SMTableicitems.TableName + "." + SMTableicitems.sItemNumber + " AS sitemnumber"
			+ ", " + SMTablepricelistcodes.TableName + "." + SMTablepricelistcodes.spricelistcode 
				+ " AS spricelistcode"
			+ ", " + SMTablepricelistcodes.TableName + "." + SMTablepricelistcodes.sdescription
				+ " AS spricelistcodedesc"
			+ ", " + SMTableicitems.TableName + "." + SMTableicitems.sItemDescription + " AS sdesc"
			+ ", " + SMTableicitems.TableName + "." + SMTableicitems.sCostUnitOfMeasure + " AS suom"
			+ " FROM " + SMTablepricelistcodes.TableName + ", " + SMTableicitems.TableName
			+ " WHERE ("
				+ "(" + SMTableicitems.TableName + "." + SMTableicitems.sItemNumber + " >= '" 
					+ sStartingItem + "')"
				+ " AND (" + SMTableicitems.TableName + "." + SMTableicitems.sItemNumber + " <= '" 
					+ sEndingItem + "')"
				+ " AND (" + SMTablepricelistcodes.TableName + "." + SMTablepricelistcodes.spricelistcode 
						+ " >= '" + sStartingCode + "')"
				+ " AND (" + SMTablepricelistcodes.TableName + "." + SMTablepricelistcodes.spricelistcode 
						+ " <= '" + sEndingCode + "')"
			+ ")"
			+ " ORDER BY " + SMTableicitems.TableName + "." + SMTableicitems.sItemNumber
			+ ", " + SMTablepricelistcodes.TableName + "." + SMTablepricelistcodes.spricelistcode
			;

		try {
			if (!clsDatabaseFunctions.executeSQL(SQL, con)){
				m_sErrorMessage = "Could not insert into temporary table.";
				return false;
			}
		} catch (SQLException e) {
			m_sErrorMessage = "Could not insert into temporary table - " + e.getMessage()+ ".";
			return false;		
		}

		SQL = "UPDATE ICPRICES, " + SMTableicitemprices.TableName
			+ " SET"
			+ " ICPRICES.bdbaseprice = " + SMTableicitemprices.TableName + "." 
				+ SMTableicitemprices.bdBasePrice
			+ ", ICPRICES.bdlevel1price = " + SMTableicitemprices.TableName + "." 
				+ SMTableicitemprices.bdLevel1Price
			+ ", ICPRICES.bdlevel2price = " + SMTableicitemprices.TableName + "." 
				+ SMTableicitemprices.bdLevel2Price
			+ ", ICPRICES.bdlevel3price = " + SMTableicitemprices.TableName + "." 
				+ SMTableicitemprices.bdLevel3Price
			+ ", ICPRICES.bdlevel4price = " + SMTableicitemprices.TableName + "." 
				+ SMTableicitemprices.bdLevel4Price
			+ ", ICPRICES.bdlevel5price = " + SMTableicitemprices.TableName + "." 
				+ SMTableicitemprices.bdLevel5Price
			+ " WHERE ("
				+ "(ICPRICES.sitemnumber = " + SMTableicitemprices.TableName + "." 
					+ SMTableicitemprices.sItemNumber + ")"
				+ " AND (ICPRICES.spricelistcode = " + SMTableicitemprices.TableName + "." 
					+ SMTableicitemprices.sPriceListCode + ")"
			+ ")"
			;

		try {
			if (!clsDatabaseFunctions.executeSQL(SQL, con)){
				m_sErrorMessage = "Could not update temporary table.";
				return false;
			}
		} catch (SQLException e) {
			m_sErrorMessage = "Could not update temporary table - " + e.getMessage()+ ".";
			return false;		
		}

		return true;
	}
	private boolean printItemPrices(
			String sItemNumber,
			String sPriceCode,
			String sPriceCodeDesc,
			BigDecimal bdBasePrice,
			BigDecimal bdPrice1,
			BigDecimal bdPrice2,
			BigDecimal bdPrice3,
			BigDecimal bdPrice4,
			BigDecimal bdPrice5,
			String sDBID,
			boolean bEditPricingPermitted, 
			PrintWriter out, 
			Connection con,
			ServletContext context,
			int iCount){
		
		//Print the line:
		if(iCount % 2 == 0) {
			out.println("<TR CLASS=\"" + SMMasterStyleSheetDefinitions.TABLE_ROW_EVEN + "\">");
		}else {
			out.println("<TR CLASS=\"" + SMMasterStyleSheetDefinitions.TABLE_ROW_ODD + "\">");
		}
		//Price code:
		String sPriceCodeLink = sPriceCode;
		if (bEditPricingPermitted){
			sPriceCodeLink = "<A HREF=\"" + SMUtilities.getURLLinkBase(context) + "smic.ICItemPriceEdit"
				+ "?ItemNumber=" + sItemNumber
				+ "&PriceListCode=" + sPriceCode
				+ "&PriceListCodeDesc=" + sPriceCodeDesc 
	    		+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
	    		+ "\">" + clsServletUtilities.Fill_In_Empty_String_For_HTML_Cell(sPriceCode) + "</A>";
		}
		out.println("<TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\">"+sPriceCodeLink +"</TD>\n");
		//Description:
		out.println("<TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\">"+sPriceCodeDesc +"</TD>\n");
		//Base price
		out.println("<TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\">"+clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(bdBasePrice) +"</TD>\n");
		//Level 1 price
		out.println("<TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\">"+clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(bdPrice1) +"</TD>\n");
		//Level 2 price
		out.println("<TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\">"+clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(bdPrice2) +"</TD>\n");
		//Level 3 price
		out.println("<TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\">"+clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(bdPrice3) +"</TD>\n");
		//Level 4 price
		out.println("<TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\">"+clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(bdPrice4) +"</TD>\n");
		//Level 5 price
		out.println("<TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\">"+clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(bdPrice5) +"</TD>\n");
		out.println("</TR>");

		return true;
	}
	private void printItemHeader(
		String sItem,
		String sDescription,
		String sUOM,
		String sDBID,
		boolean bViewItemPermitted,
		PrintWriter out,
		ServletContext context,
		Connection conn
	) throws Exception{
		out.println("<TR CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_HEADING + "\"><TD COLSPAN=\"8\" CLASS=\"" + SMMasterStyleSheetDefinitions.TABLE_ROW_BREAK + "\">&nbsp;</TD></TR>");
		String sOutPut = "<TR CLASS=\"" + SMMasterStyleSheetDefinitions.TABLE_HEADING + "\">"
				+ "<TD COLSPAN = 8 CLASS=\"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD + "\">";
		sOutPut += "Item:&nbsp;";
		
		if (bViewItemPermitted){
			sOutPut += "<A HREF=\"" + SMUtilities.getURLLinkBase(context) + "smic.ICDisplayItemInformation?ItemNumber=" 
	    		+ sItem
	    		+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
	    		+ "\">" + clsServletUtilities.Fill_In_Empty_String_For_HTML_Cell(sItem) + "</A>";
		}else{
			sOutPut += sItem;
		}
		out.println(sOutPut + "&nbsp;-&nbsp;" + sDescription.trim() 
			+ "&nbsp;Unit of measure:&nbsp;" + sUOM + "</TD></TR>");
		out.println("<TR CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_HEADING + "\"><TD COLSPAN=\"8\" CLASS=\"" + SMMasterStyleSheetDefinitions.TABLE_ROW_BREAK + "\">&nbsp;</TD></TR>");
		
		SMPriceLevelLabels pricelevellabels = new SMPriceLevelLabels();
		try {
			pricelevellabels.load(conn);
		} catch (Exception e1) {
			throw new Exception("Error [1580855317] reading price level labels: " + e1.getMessage());
		}
		
		out.println("<TR CLASS=\"" + SMMasterStyleSheetDefinitions.TABLE_HEADING + "\">");
		out.println("<TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_LEFT_JUSTIFIED + "\">Price List Code</TD>\n");
		out.println("<TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_LEFT_JUSTIFIED + "\">Description</TD>\n");
		out.println("<TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_RIGHT_JUSTIFIED + "\">" + pricelevellabels.get_sbaselabel() + "</TD>\n");
		out.println("<TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_RIGHT_JUSTIFIED + "\">" + pricelevellabels.get_slevel1label() + "</TD>\n");
		out.println("<TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_RIGHT_JUSTIFIED + "\">" + pricelevellabels.get_slevel2label() + "</TD>\n");
		out.println("<TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_RIGHT_JUSTIFIED + "\">" + pricelevellabels.get_slevel3label() + "</TD>\n");
		out.println("<TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_RIGHT_JUSTIFIED + "\">" + pricelevellabels.get_slevel4label() + "</TD>\n");
		out.println("<TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_RIGHT_JUSTIFIED + "\">" + pricelevellabels.get_slevel5label() + "</TD>\n");
		out.println("</TR>");
	}
	private void printItemFooter(
			PrintWriter out
			){
		out.println("<TR></TR>");
	}
	private void printReportFooter(
			PrintWriter out
			){
		out.println("</TABLE><BR>");
	}
	public String getErrorMessage (){
		return m_sErrorMessage;
	}
}
