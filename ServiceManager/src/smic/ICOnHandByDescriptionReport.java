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
import SMDataDefinition.SMTableicitemlocations;
import SMDataDefinition.SMTableicitems;
import SMDataDefinition.SMTableicoptions;
import SMDataDefinition.SMTableorderdetails;
import SMDataDefinition.SMTableorderheaders;
import ServletUtilities.clsServletUtilities;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsManageBigDecimals;

public class ICOnHandByDescriptionReport extends java.lang.Object{

	private String m_sErrorMessage;
	private boolean bDebugMode = false;
	private static String COMPANY_NAME_FIELD = "COMPANYNAME";
	private static String LOCALCOMPANYMARKER = "(LOCAL)";
	public ICOnHandByDescriptionReport(
	){
		m_sErrorMessage = "";
	}
	public boolean processReport(
			Connection conn,
			String sSearchText,
			String sDBID,
			String sUserID,
			PrintWriter out,
			ServletContext context,
			String sLicenseModuleLevel
	){

		ArrayList<String>arrCompanyNames = new ArrayList<String>(0);
		ArrayList<String>arrCompanyDatabases = new ArrayList<String>(0);

		long lTimer = System.currentTimeMillis();
		
		try {
			loadSisterCompanyData (conn, arrCompanyNames, arrCompanyDatabases);
		} catch (SQLException e) {
			m_sErrorMessage = "Error reading sister company data - " + e.getMessage();
			return false;
		}

		String SQL = "SELECT"
			+ " 'ICOnHandByDescriptionReport SQL' as REPORTNAME"
			+ ", '" + sUserID + "' AS USERNAME"
			+ ", " + SMTableicitemlocations.TableName + "." + SMTableicitemlocations.sQtyOnHand + " AS QTYOH"
			+ ", " + SMTableicitemlocations.TableName + "." + SMTableicitemlocations.sLocation + " AS LOC"
			+ ", " + SMTableicitemlocations.TableName + "." + SMTableicitemlocations.sItemNumber + " AS ITEM"
			+ ", " + SMTableicitemlocations.TableName + "." + SMTableicitemlocations.sTotalCost + " AS TOTALCOST"
			+ ", " + SMTableicitems.TableName + "." + SMTableicitems.sItemDescription + " AS ITEMDESC"
			+ ", " + SMTableicitems.TableName + "." + SMTableicitems.sCostUnitOfMeasure + " AS UOM"
			+ ", " + SMTableicitems.TableName + "." + SMTableicitems.bdmostrecentcost + " AS MRC"
			+ ", " + SMTableicitems.TableName + "." + SMTableicitems.sComment1 + " AS COMMENT1"
			+ ", " + SMTableicitems.TableName + "." + SMTableicitems.sComment2 + " AS COMMENT2"
			+ ", '" + LOCALCOMPANYMARKER + "' AS " + COMPANY_NAME_FIELD
			+ ", SALESORDERQUERY.SumOfQtyOrdered"
			+ " FROM " + SMTableicitems.TableName + " INNER JOIN "
			+ SMTableicitemlocations.TableName + " ON " 
			+ SMTableicitems.TableName + "." + SMTableicitems.sItemNumber 
			+ " = " + SMTableicitemlocations.TableName + "." + SMTableicitemlocations.sItemNumber
			
			//Add code for the sales order qty here:
			+ " LEFT JOIN"
			+ " (SELECT"
			+ " " + SMTableorderdetails.TableName + "." + SMTableorderdetails.sItemNumber + " AS ORDERDETAILSITEM"
			+ ", " + SMTableorderdetails.TableName + "." + SMTableorderdetails.sLocationCode + " AS ORDERDETAILSLOCATION"
			+ ", " + SMTableorderheaders.TableName + "." + SMTableorderheaders.iOrderType
			+ ", Sum(" + SMTableorderdetails.TableName + "." + SMTableorderdetails.dQtyOrdered + ") AS SumOfQtyOrdered"
			+ " FROM " + SMTableorderdetails.TableName + " INNER JOIN " + SMTableorderheaders.TableName
			+ " ON " + SMTableorderdetails.TableName + "." + SMTableorderdetails.strimmedordernumber
			+ " = " + SMTableorderheaders.TableName + "." + SMTableorderheaders.strimmedordernumber
			+ " WHERE ("
			+ "(" + SMTableorderheaders.TableName + "." + SMTableorderheaders.datOrderCanceledDate
			+ " < '1990-01-01')"
			+ " AND (" + SMTableorderheaders.iOrderType + " != " + SMTableorderheaders.ORDERTYPE_QUOTE + ")"
			+ " AND (" + SMTableorderdetails.TableName + "." + SMTableorderdetails.dQtyOrdered + " > 0.00)"
			+ ") GROUP BY " + SMTableorderdetails.TableName + "." + SMTableorderdetails.sItemNumber + ","
			+ " " + SMTableorderdetails.TableName + "." + SMTableorderdetails.sLocationCode
			+ ") AS SALESORDERQUERY ON" 
			+ "((" + SMTableicitemlocations.TableName + "." + SMTableicitemlocations.sItemNumber
			+ " = ORDERDETAILSITEM)"
			+ " AND (" + SMTableicitemlocations.TableName + "." + SMTableicitemlocations.sLocation
			+ " = ORDERDETAILSLOCATION))"
			
			
			+ " WHERE ("
			+ "(" + SMTableicitemlocations.TableName + "." + SMTableicitemlocations.sQtyOnHand
			+ " > 0.0000)"
			+ " AND (" + SMTableicitems.TableName + "." + SMTableicitems.sItemDescription
			+ " LIKE '%" + clsDatabaseFunctions.FormatSQLStatement(sSearchText) + "%')"
			+ ")"
			;

		for (int i = 0; i < arrCompanyDatabases.size(); i++){
			SQL += " UNION ALL SELECT"
				+ " 'ICOnHandByDescriptionReport SQL' as REPORTNAME"
				+ ", '" + sUserID + "' AS USERNAME"
				+ ", " + arrCompanyDatabases.get(i) + "." + SMTableicitemlocations.TableName + "." + SMTableicitemlocations.sQtyOnHand + " AS QTYOH"
				+ ", " + arrCompanyDatabases.get(i) + "." + SMTableicitemlocations.TableName + "." + SMTableicitemlocations.sLocation + " AS LOC"
				+ ", " + arrCompanyDatabases.get(i) + "." + SMTableicitemlocations.TableName + "." + SMTableicitemlocations.sItemNumber + " AS ITEM"
				+ ", " + arrCompanyDatabases.get(i) + "." + SMTableicitemlocations.TableName + "." + SMTableicitemlocations.sTotalCost + " AS TOTALCOST"
				+ ", " + arrCompanyDatabases.get(i) + "." + SMTableicitems.TableName + "." + SMTableicitems.sItemDescription + " AS ITEMDESC"
				+ ", " + arrCompanyDatabases.get(i) + "." + SMTableicitems.TableName + "." + SMTableicitems.sCostUnitOfMeasure + " AS UOM"
				+ ", " + arrCompanyDatabases.get(i) + "." + SMTableicitems.TableName + "." + SMTableicitems.bdmostrecentcost + " AS MRC"
				+ ", " + arrCompanyDatabases.get(i) + "." + SMTableicitems.TableName + "." + SMTableicitems.sComment1 + " AS COMMENT1"
				+ ", " + arrCompanyDatabases.get(i) + "." + SMTableicitems.TableName + "." + SMTableicitems.sComment2 + " AS COMMENT2"
				+ ", '" + arrCompanyNames.get(i) + "' AS " + COMPANY_NAME_FIELD
				+ ", SALESORDERQUERY.SumOfQtyOrdered"
				+ " FROM " + arrCompanyDatabases.get(i) + "." + SMTableicitems.TableName + " INNER JOIN "
				+ arrCompanyDatabases.get(i) + "." + SMTableicitemlocations.TableName + " ON " 
				+ arrCompanyDatabases.get(i) + "." + SMTableicitems.TableName + "." + SMTableicitems.sItemNumber 
				+ " = " + arrCompanyDatabases.get(i) + "." + SMTableicitemlocations.TableName + "." + SMTableicitemlocations.sItemNumber
				
				//Add code for the sales order qty here:
				+ " LEFT JOIN"
				+ " (SELECT"
				+ " " + arrCompanyDatabases.get(i) + "." + SMTableorderdetails.TableName + "." + SMTableorderdetails.sItemNumber + " AS ORDERDETAILSITEM"
				+ ", " + arrCompanyDatabases.get(i) + "." + SMTableorderdetails.TableName + "." + SMTableorderdetails.sLocationCode + " AS ORDERDETAILSLOCATION"
				+ ", " + arrCompanyDatabases.get(i) + "." + SMTableorderheaders.TableName + "." + SMTableorderheaders.iOrderType
				+ ", Sum(" + arrCompanyDatabases.get(i) + "." + SMTableorderdetails.TableName + "." + SMTableorderdetails.dQtyOrdered + ") AS SumOfQtyOrdered"
				+ " FROM " + arrCompanyDatabases.get(i) + "." + SMTableorderdetails.TableName + " INNER JOIN " + arrCompanyDatabases.get(i) + "." + SMTableorderheaders.TableName
				+ " ON " + arrCompanyDatabases.get(i) + "." + SMTableorderdetails.TableName + "." + SMTableorderdetails.strimmedordernumber
				+ " = " + arrCompanyDatabases.get(i) + "." + SMTableorderheaders.TableName + "." + SMTableorderheaders.strimmedordernumber
				+ " WHERE ("
				+ "(" + arrCompanyDatabases.get(i) + "." + SMTableorderheaders.TableName + "." + SMTableorderheaders.datOrderCanceledDate
				+ " < '1990-01-01')"
				+ " AND (" + arrCompanyDatabases.get(i) + "." + SMTableorderheaders.TableName + "." + SMTableorderheaders.iOrderType + " != " 
				+ SMTableorderheaders.ORDERTYPE_QUOTE + ")"
				+ " AND (" + SMTableorderdetails.TableName + "." + SMTableorderdetails.dQtyOrdered + " > 0.00)"
				+ ") GROUP BY " + arrCompanyDatabases.get(i) + "." + SMTableorderdetails.TableName + "." + SMTableorderdetails.sItemNumber + ","
				+ " " + arrCompanyDatabases.get(i) + "." + SMTableorderdetails.TableName + "." + SMTableorderdetails.sLocationCode
				+ ") AS SALESORDERQUERY ON" 
				+ "((" + arrCompanyDatabases.get(i) + "." + SMTableicitemlocations.TableName + "." + SMTableicitemlocations.sItemNumber
				+ " = ORDERDETAILSITEM)"
				+ " AND (" + arrCompanyDatabases.get(i) + "." + SMTableicitemlocations.TableName + "." + SMTableicitemlocations.sLocation
				+ " = ORDERDETAILSLOCATION))"
				
				+ " WHERE ("
				+ "(" + arrCompanyDatabases.get(i) + "." + SMTableicitemlocations.TableName + "." + SMTableicitemlocations.sQtyOnHand
				+ " > 0.0000)"
				+ " AND (" + arrCompanyDatabases.get(i) + "." + SMTableicitems.TableName + "." + SMTableicitems.sItemDescription
				+ " LIKE '%" + clsDatabaseFunctions.FormatSQLStatement(sSearchText) + "%')"
				+ ")"
				;
		}

		SQL += " ORDER BY ITEM";

		if (bDebugMode){
			clsServletUtilities.sysprint(this.toString(), sUserID, "[1500577278] Main SQL = " + SQL);
			out.println("<BR>Main SQL = " + SQL + "<BR>");
		}
		//Check permissions for viewing items:
		boolean bViewItemPermitted = SMSystemFunctions.isFunctionPermitted(
				SMSystemFunctions.ICDisplayItemInformation,
				sUserID,
				conn,
				sLicenseModuleLevel);

		//TODO:
		//add alternating row colors

		printLayout(out);
		out.println("<table class=\"main\">");
		printRowHeader(out);
		try{
			if (bDebugMode){
				System.out.println("In " + this.toString() + " SQL: " + SQL);
			}
			ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
			boolean bWhiteRow = true;
			while(rs.next()){
				bWhiteRow = !bWhiteRow;
				//Print the line:
				out.println("<TR>");

				String sLine = "";
				String sCompanyName = rs.getString(COMPANY_NAME_FIELD);
				//We will ONLY show a link to the item if it's in the local company:
				boolean bIsLocalItem = sCompanyName.compareToIgnoreCase(LOCALCOMPANYMARKER) == 0;

				//Item
				String sItemNumber = rs.getString("ITEM");
				String sItemNumberLink = "";
				if (bViewItemPermitted && bIsLocalItem){
					sItemNumberLink = "<A HREF=\"" + SMUtilities.getURLLinkBase(context) + "smic.ICDisplayItemInformation?ItemNumber=" 
					+ sItemNumber
					+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
					+ "\">" + clsServletUtilities.Fill_In_Empty_String_For_HTML_Cell(sItemNumber) + "</A>";
				}else{
					sItemNumberLink = sItemNumber;
				}
				sLine += "<TD><FONT SIZE=2>" + sItemNumberLink + "</FONT></TD>";

				//Company:
				sLine += "<TD><FONT SIZE=2>" 
					+ sCompanyName + "</FONT></TD>";

				//Location:
				sLine += "<TD><FONT SIZE=2>" 
					+ rs.getString("LOC") + "</FONT></TD>";

				//Qty
				sLine += "<TD ALIGN=RIGHT><FONT SIZE=2>" 
					+ clsManageBigDecimals.BigDecimalToFormattedString(
							"###,###,##0.0000", rs.getBigDecimal("QTYOH")) 
							+ "</FONT></TD>";

				//Qty on sales order:
				BigDecimal bdQtyOnSalesOrder = rs.getBigDecimal("SumOfQtyOrdered");
				if (bdQtyOnSalesOrder == null){
					bdQtyOnSalesOrder = BigDecimal.ZERO;
				}
				sLine += "<TD ALIGN=RIGHT><FONT SIZE=2>" 
					+ clsManageBigDecimals.BigDecimalToFormattedString(
							"###,###,##0.0000", bdQtyOnSalesOrder) 
							+ "</FONT></TD>";
				
				//Description:
				sLine += "<TD><FONT SIZE=2>" 
					+ rs.getString("ITEMDESC") 
					+ "</FONT></TD>";

				//Avg. unit cost:
				sLine += "<TD ALIGN=RIGHT><FONT SIZE=2>" 
					+ clsManageBigDecimals.BigDecimalToFormattedString(
							"###,###,##0.00",
							(rs.getBigDecimal("TOTALCOST")).divide(
									rs.getBigDecimal("QTYOH"),BigDecimal.ROUND_HALF_UP
							)
					) 
					+ "</FONT></TD>";

				//Unit of measure:
				sLine += "<TD><FONT SIZE=2>" 
					+ rs.getString("UOM") + "</FONT></TD>";

				//Total cost
				sLine += "<TD ALIGN=RIGHT><FONT SIZE=2>" 
					+ clsManageBigDecimals.BigDecimalToFormattedString(
							"###,###,##0.0000", rs.getBigDecimal("TOTALCOST")) 
							+ "</FONT></TD>";

				String sComment1 = rs.getString("COMMENT1");
				if (sComment1 == null){
					sComment1 = "";
				}
				sComment1 = sComment1.trim().replace("\"", "\"\"");

				sLine += "<TD><FONT SIZE=2>" + sComment1 + "</FONT></TD>";

				String sComment2 = rs.getString("COMMENT2");
				if (sComment2 == null){
					sComment2 = "";
				}
				sComment2 = sComment2.trim().replace("\"", "\"\"");
				sLine += "<TD><FONT SIZE=2>" + sComment2 + "</FONT></TD>";

				if (bWhiteRow){
					out.println("<TR class=\"d0\">" + sLine + "</TR>");
				}else{
					out.println("<TR class=\"d1\">" + sLine + "</TR>");
				}

				out.println("</TR>");
			}
			rs.close();
		}catch (SQLException e){
			m_sErrorMessage = "Error reading resultset - " + e.getMessage();
			return false;
		}

		out.println("</TABLE>");
		
		out.println("Processing time: " + Long.toString((System.currentTimeMillis() - lTimer) / 1000) + " seconds.");
		return true;
	}
	private void printLayout(PrintWriter out){
		String sBorderSize = "0";
		String sFontSize = "small";
		out.println("<style type=\"text/css\">");
		out.println(
				"table.main {"
				+ "border-width: " + sBorderSize + "px; "
				+ "border-spacing: 2px; "
				+ "border-style: outset; "
				+ "border-color: gray; "
				+ "border-collapse: separate; "
				+ "font-size: " + sFontSize + "; "
				//+ "background-color: white; "
				+ "}"
		);

		out.println(
				"table.main th {"
				+ "border-width: " + sBorderSize + "px; "
				+ "padding: 2px; "
				+ "border-style: inset; "
				+ "border-color: gray; "
				//+ "background-color: white; "
				+ "}"
		);

		out.println(
				"tr.d0 td {"
				+ "background-color: #FFFFFF; "
				+"}"
		);
		out.println(
				"tr.d1 td {"
				+ "background-color: #EEEEEE; "
				+ "}"
		);

		out.println(
				"table.main td {"
				+ "border-width: " + sBorderSize + "px; "
				+ "padding: 2px; "
				+ "border-style: inset; "
				+ "border-color: gray; "
				+ "vertical-align: text-top;"
				//+ "background-color: #EEEEEE; "
				+ "}"
		);

		out.println("</style>");
	}
	private void loadSisterCompanyData (
			Connection conn, 
			ArrayList<String>arrCompanyNames, 
			ArrayList<String>arrCompanyDatabases) throws SQLException
			{

		String SQL = "SELECT"
			+ " " + SMTableicoptions.ssistercompanydb1
			+ ", " + SMTableicoptions.ssistercompanydb2
			+ ", " + SMTableicoptions.ssistercompanyname1
			+ ", " + SMTableicoptions.ssistercompanyname2
			+ " FROM " + SMTableicoptions.TableName
			;
		ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
		if (rs.next()){
			if (rs.getString(SMTableicoptions.ssistercompanyname1).trim().compareToIgnoreCase("") != 0){
				arrCompanyNames.add(rs.getString(SMTableicoptions.ssistercompanyname1));
				arrCompanyDatabases.add(rs.getString(SMTableicoptions.ssistercompanydb1));
			}
			if (rs.getString(SMTableicoptions.ssistercompanyname2).trim().compareToIgnoreCase("") != 0){
				arrCompanyNames.add(rs.getString(SMTableicoptions.ssistercompanyname2));
				arrCompanyDatabases.add(rs.getString(SMTableicoptions.ssistercompanydb2));
			}
			rs.close();
		}else{
			rs.close();
			throw new SQLException("Could not get the single ICOptions record.");
		}
			}
	private void printRowHeader(
			PrintWriter out
	){
		//out.println("<TABLE BORDER=0>");
		out.println("<TR>");
		out.println("<TH VALIGN=BOTTOM><B><FONT SIZE=2>Item</FONT></B></TH>");
		out.println("<TH VALIGN=BOTTOM><B><FONT SIZE=2>Company</FONT></B></TH>");
		out.println("<TH VALIGN=BOTTOM><B><FONT SIZE=2>Loc</FONT></B></TH>");
		out.println("<TH ALIGN=RIGHT VALIGN=BOTTOM><B><FONT SIZE=2>Qty&nbsp;OH</FONT></B></TH>");
		out.println("<TH ALIGN=RIGHT VALIGN=BOTTOM><B><FONT SIZE=2>Qty&nbsp;on<BR>Sales&nbsp;Order</FONT></B></TH>");
		out.println("<TH VALIGN=BOTTOM><B><FONT SIZE=2>Description</FONT></B></TH>");
		out.println("<TH VALIGN=BOTTOM><B><FONT SIZE=2>Avg. unit cost</FONT></B></TH>");
		out.println("<TH VALIGN=BOTTOM><B><FONT SIZE=2>UOM</FONT></B></TH>");
		out.println("<TH ALIGN=RIGHT VALIGN=BOTTOM><B><FONT SIZE=2>Total cost<BR>on hand</FONT></B></TH>");
		out.println("<TH VALIGN=BOTTOM><B><FONT SIZE=2>Comment 1</FONT></B></TH>");
		out.println("<TH VALIGN=BOTTOM><B><FONT SIZE=2>Comment 2</FONT></B></TH>");
		out.println("</TR>");
	}
	public String getErrorMessage (){
		return m_sErrorMessage;
	}
}
