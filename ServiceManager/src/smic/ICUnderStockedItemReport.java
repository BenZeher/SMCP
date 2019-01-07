package smic;

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import javax.servlet.ServletContext;

import smcontrolpanel.SMSystemFunctions;
import smcontrolpanel.SMUtilities;
import SMDataDefinition.SMTableicitemlocations;
import SMDataDefinition.SMTableicitems;
import SMDataDefinition.SMTableicpoheaders;
import SMDataDefinition.SMTableicpolines;
import SMDataDefinition.SMTableicporeceiptheaders;
import SMDataDefinition.SMTableicporeceiptlines;
import SMDataDefinition.SMTableorderdetails;
import SMDataDefinition.SMTableorderheaders;
import ServletUtilities.clsServletUtilities;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsManageBigDecimals;

public class ICUnderStockedItemReport extends java.lang.Object{

	private String m_sErrorMessage;
	private boolean bDebugMode = false;
	
	public ICUnderStockedItemReport(
			){
		m_sErrorMessage = "";
	}
	public boolean processReport(
			Connection conn,
			ArrayList<String>sLocations,
			String sDBID,
			String sUserID,
			PrintWriter out,
			ServletContext context,
			String sLicenseModuleLevel
			){
	
		//TODO - replace with:
		String SQL = 
			"SELECT"
			+ " 'ICUnderStockedItemReport' as REPORTNAME"
			+ ", '" + sUserID + "' as USERNAME"
			+ ", ITEMLOCQUERY." + SMTableicitemlocations.sItemNumber
			+ ", ITEMLOCQUERY." + SMTableicitemlocations.sLocation
			+ ", icitems.sitemdescription"
			+ ", ITEMLOCQUERY." + SMTableicitemlocations.sQtyOnHand
			+ ", ITEMLOCQUERY." + SMTableicitemlocations.sMinQtyOnHand
			+ ", " + SMTableicitems.TableName + "." + SMTableicitems.sCostUnitOfMeasure
			+ ", IF(POLINEQUERY.QTYPENDING IS NULL, 0.00, POLINEQUERY.QTYPENDING) AS POLINEQTYPENDING"
			+ ", IF(QTYRECNPOSTQUERY.QTYRECNPOST IS NULL, 0.00, QTYRECNPOSTQUERY.QTYRECNPOST) AS QTYRECNPOST"
			+ ", IF(SOLINEQUERY.QTYPENDING IS NULL, 0.00, SOLINEQUERY.QTYPENDING) SOLINEQTYPENDING"
 			+ " FROM" 

			+ " ("
				+ "SELECT"
				+ " * FROM " + SMTableicitemlocations.TableName
				+ " WHERE ("
					+ "(" + SMTableicitemlocations.sMinQtyOnHand + " != 0.00)"
					+ " AND (" + SMTableicitemlocations.sQtyOnHand + " < " + SMTableicitemlocations.sMinQtyOnHand + ")"
					+ " AND ("
					;
					
					for (int i = 0; i < sLocations.size(); i++){
						if(i == 0){
							SQL += " ( "  + SMTableicitemlocations.TableName + "." +  SMTableicitemlocations.sLocation + "=" + "'"+ sLocations.get(i) + "'" + ")" ;
						}else{
							SQL += " OR " +  " ( "  + SMTableicitemlocations.TableName + "." +  SMTableicitemlocations.sLocation + "=" + "'"+ sLocations.get(i) + "'" + ")";
						}
					}	
				SQL += ")"
				+ ")"
			+ ") AS ITEMLOCQUERY"
			+ " LEFT JOIN " + SMTableicitems.TableName + " ON ITEMLOCQUERY." + SMTableicitemlocations.sItemNumber + " = " + SMTableicitems.TableName + "." + SMTableicitems.sItemNumber
			+ " LEFT JOIN"
			+ " ("
				+ "SELECT"
				+ " " + SMTableicpolines.TableName + "." + SMTableicpolines.sitemnumber + " AS ITEM"
				+ ", " + SMTableicpolines.TableName + "." + SMTableicpolines.slocation + " AS LOC"
				+ ", SUM(" + SMTableicpolines.TableName + "." + SMTableicpolines.bdqtyordered + " - " + SMTableicpolines.TableName + "." + SMTableicpolines.bdqtyreceived + ") AS QTYPENDING"
				+ " FROM " + SMTableicpolines.TableName
				+ " LEFT JOIN " + SMTableicpoheaders.TableName + " ON " + SMTableicpolines.TableName + "." + SMTableicpolines.lpoheaderid + " = " + SMTableicpoheaders.TableName + "." + SMTableicpoheaders.lid
				+ " WHERE ("
					+ "(" 
						+ "(" + SMTableicpoheaders.TableName + "." + SMTableicpoheaders.lstatus + " = " + SMTableicpoheaders.STATUS_ENTERED + ")"
						+ " OR (" + SMTableicpoheaders.TableName + "." + SMTableicpoheaders.lstatus + " = " + SMTableicpoheaders.STATUS_PARTIALLY_RECEIVED + ")"
					+ ")"
					+ " AND (" + SMTableicpolines.TableName + "." + SMTableicpolines.bdqtyordered + " != 0)"
				+ ") GROUP BY " + SMTableicpolines.TableName + "." + SMTableicpolines.sitemnumber + ", " + SMTableicpolines.TableName + "." + SMTableicpolines.slocation
			+ ") AS POLINEQUERY"
			+ " ON ((POLINEQUERY.ITEM = ITEMLOCQUERY." + SMTableicitemlocations.sItemNumber + ") AND (POLINEQUERY.LOC = ITEMLOCQUERY." + SMTableicitemlocations.sLocation + "))"
			+ " LEFT JOIN"
			+ "("
				+ "SELECT"
				+ " " + SMTableicporeceiptlines.TableName + "." + SMTableicporeceiptlines.sitemnumber + " AS ITEM"
				+ ", " + SMTableicporeceiptlines.TableName + "." + SMTableicporeceiptlines.slocation + " AS LOC"
				+ ", SUM(" + SMTableicporeceiptlines.TableName + "." + SMTableicporeceiptlines.bdqtyreceived + ") AS QTYRECNPOST"
				+ " FROM " + SMTableicporeceiptlines.TableName + " LEFT JOIN " + SMTableicporeceiptheaders.TableName
				+ " ON " + SMTableicporeceiptlines.TableName + "." + SMTableicporeceiptlines.lreceiptheaderid + " = " + SMTableicporeceiptheaders.TableName + "." + SMTableicporeceiptheaders.lid
				+ " WHERE ("
					+ "(" + SMTableicporeceiptlines.TableName + "." + SMTableicporeceiptlines.bdqtyreceived + " > 0)"
					+ " AND (" + SMTableicporeceiptheaders.TableName + "." + SMTableicporeceiptheaders.lpostedtoic + " = 0)"
					+ " AND (" + SMTableicporeceiptheaders.TableName + "." + SMTableicporeceiptheaders.lstatus + " =  0)"
				+ ") GROUP BY " + SMTableicporeceiptlines.TableName + "." + SMTableicporeceiptlines.sitemnumber + ", " + SMTableicporeceiptlines.TableName + "." + SMTableicporeceiptlines.slocation
			+ ") AS QTYRECNPOSTQUERY"
			+ " ON ((QTYRECNPOSTQUERY.ITEM = ITEMLOCQUERY." + SMTableicitemlocations.sItemNumber + ") AND (QTYRECNPOSTQUERY.LOC = ITEMLOCQUERY." + SMTableicitemlocations.sLocation + "))"
			+ " LEFT JOIN" 
			+ "("
				+ "SELECT"
				+ " " + SMTableorderdetails.TableName + "." + SMTableorderdetails.sItemNumber + " AS ITEM"
				+ ", " + SMTableorderdetails.TableName + "." + SMTableorderdetails.sLocationCode + " AS LOC"
				+ ", SUM(" + SMTableorderdetails.TableName + "." + SMTableorderdetails.dQtyOrdered + ") AS QTYPENDING"
				+ " FROM " + SMTableorderdetails.TableName
				+ " LEFT JOIN " + SMTableorderheaders.TableName + " ON " + SMTableorderdetails.TableName + "." + SMTableorderdetails.strimmedordernumber + " = " + SMTableorderheaders.TableName + "." + SMTableorderheaders.strimmedordernumber
				+ " WHERE ("
					+ "("
						+ "(" + SMTableorderheaders.TableName + "." + SMTableorderheaders.iOrderType + " = 1) OR (" + SMTableorderheaders.TableName + "." + SMTableorderheaders.iOrderType + " = 3)"
					+ ")"
					+ " AND (" + SMTableorderheaders.TableName + "." + SMTableorderheaders.datOrderCanceledDate + " < '1950-01-01')"
					+ " AND (" + SMTableorderdetails.TableName + "." + SMTableorderdetails.dQtyOrdered + " != 0.00)"
				+ ") GROUP BY " + SMTableorderdetails.TableName + "." + SMTableorderdetails.sItemNumber + ", " + SMTableorderdetails.TableName + "." + SMTableorderdetails.sLocationCode
			+ ") AS SOLINEQUERY"
			+ " ON ((SOLINEQUERY.ITEM = ITEMLOCQUERY." + SMTableicitemlocations.sItemNumber + ") AND (SOLINEQUERY.LOC = ITEMLOCQUERY." + SMTableicitemlocations.sLocation + "))"
			+ " ORDER BY ITEMLOCQUERY." + SMTableicitemlocations.sItemNumber
		;

		/* TJR - 7/21/2017- replaced this query to try to optimize speed:
		String SQL = "SELECT"
			+ " 'ICUnderStockedItemReport' as REPORTNAME"
			+ ", '" + sUserName + "' AS USERNAME"
			+ ", " + SMTableicitemlocations.TableName + "." + SMTableicitemlocations.sItemNumber
			+ ", " + SMTableicitemlocations.TableName + "." + SMTableicitemlocations.sLocation
			+ ", " + SMTableicitems.TableName + "." + SMTableicitems.sItemDescription
			+ ", " + SMTableicitemlocations.TableName + "." + SMTableicitemlocations.sQtyOnHand
			+ ", " + SMTableicitemlocations.TableName + "." + SMTableicitemlocations.sMinQtyOnHand
			+ ", " + SMTableicitems.TableName + "." + SMTableicitems.sCostUnitOfMeasure
			+ ", " + "POLINEQUERY.QTYPENDING"
			+ ", " + "QTYRECNPOSTQUERY.QTYRECNPOST"
			+ ", " + "SOLINEQUERY.QTYPENDING"
			
			+ " FROM"
			+ " " + SMTableicitemlocations.TableName + " LEFT JOIN " + " " + SMTableicitems.TableName + " ON "
			+ SMTableicitemlocations.TableName + "." + SMTableicitemlocations.sItemNumber + " = "
			+ SMTableicitems.TableName + "." + SMTableicitems.sItemNumber
			+ " LEFT JOIN " 
			+ " (SELECT "

			+ SMTableicpolines.TableName + "." + SMTableicpolines.sitemnumber + " AS ITEM"
			+ ", " + SMTableicpolines.TableName + "." + SMTableicpolines.slocation + " AS LOC"
			+ ", SUM(" + SMTableicpolines.TableName + "." + SMTableicpolines.bdqtyordered 
			+ " - " + SMTableicpolines.TableName + "." + SMTableicpolines.bdqtyreceived + ") AS QTYPENDING"
			+ " FROM " + SMTableicpolines.TableName
			+ " LEFT JOIN " + SMTableicpoheaders.TableName + " ON "
			+ SMTableicpolines.TableName + "." + SMTableicpolines.lpoheaderid + " = "
			+ SMTableicpoheaders.TableName + "." + SMTableicpoheaders.lid

			+ " WHERE ("
			+ "(" + SMTableicpoheaders.TableName + "." + SMTableicpoheaders.lstatus + " = " + SMTableicpoheaders.STATUS_ENTERED + ")"
			+ " OR (" + SMTableicpoheaders.TableName + "." + SMTableicpoheaders.lstatus + " = " + SMTableicpoheaders.STATUS_PARTIALLY_RECEIVED + ")"
			+ ")"
			
			+ " GROUP BY " + SMTableicpolines.TableName + "." + SMTableicpolines.sitemnumber + "," + SMTableicpolines.TableName + "." + SMTableicpolines.slocation
			
			+ ") AS POLINEQUERY ON ((POLINEQUERY.ITEM = " + SMTableicitemlocations.TableName + "." + SMTableicitemlocations.sItemNumber 
			+ ") AND (POLINEQUERY.LOC = " + SMTableicitemlocations.TableName + "." + SMTableicitemlocations.sLocation 
			+"))"
			//
			+ " LEFT JOIN " 
			+ " (SELECT "

			+ SMTableicporeceiptlines.TableName+ "." + SMTableicporeceiptlines.sitemnumber + " AS ITEM"
			+ ", " + SMTableicporeceiptlines.TableName + "." + SMTableicporeceiptlines.slocation + " AS LOC"
			+ ", SUM(" + SMTableicporeceiptlines.TableName + "." + SMTableicporeceiptlines.bdqtyreceived + ") AS QTYRECNPOST"
			+ " FROM " + SMTableicporeceiptlines.TableName
			+ " LEFT JOIN " + SMTableicporeceiptheaders.TableName + " ON "
			+ SMTableicporeceiptlines.TableName + "." + SMTableicporeceiptlines.lreceiptheaderid + " = "
			+ SMTableicporeceiptheaders.TableName + "." + SMTableicporeceiptheaders.lid

			+ " WHERE ("
			+ "(" + SMTableicporeceiptlines.TableName + "." + SMTableicporeceiptlines.bdqtyreceived + " > " + "0" + ")"
			+ " AND (" + SMTableicporeceiptheaders.TableName + "." + SMTableicporeceiptheaders.lpostedtoic + " = " + "0" + ")"
			+ " AND (" + SMTableicporeceiptheaders.TableName + "." + SMTableicporeceiptheaders.lstatus + " = " + "0" + ")"
			+ ")"
			
			+ " GROUP BY " + SMTableicporeceiptlines.TableName + "." + SMTableicporeceiptlines.sitemnumber + "," + SMTableicporeceiptlines.TableName + "." + SMTableicporeceiptlines.slocation
			
			+ ") AS QTYRECNPOSTQUERY ON ((QTYRECNPOSTQUERY.ITEM = " + SMTableicitemlocations.TableName + "." + SMTableicitemlocations.sItemNumber 
			+ ") AND (QTYRECNPOSTQUERY.LOC = " + SMTableicitemlocations.TableName + "." + SMTableicitemlocations.sLocation 
			+"))"
			
			//
			+ " LEFT JOIN " 
			+ " (SELECT "

			+ SMTableorderdetails.TableName + "." + SMTableorderdetails.sItemNumber + " AS ITEM"
			+ ", " + SMTableorderheaders.TableName + "." + SMTableorderheaders.sLocation + " AS LOC"
			+ ", SUM(" + SMTableorderdetails.TableName + "." + SMTableorderdetails.dQtyOrdered + ") AS QTYPENDING"
			+ " FROM " + SMTableorderdetails.TableName
			+ " LEFT JOIN " + SMTableorderheaders.TableName + " ON "
			+ SMTableorderdetails.TableName + "." + SMTableorderdetails.strimmedordernumber + " = "
			+ SMTableorderheaders.TableName + "." + SMTableorderheaders.strimmedordernumber

			+ " WHERE ("
			+ "("
			+ "(" + SMTableorderheaders.TableName + "." + SMTableorderheaders.iOrderType + " = " + SMTableorderheaders.ORDERTYPE_ACTIVE + ")"
			+ " OR (" + SMTableorderheaders.TableName + "." + SMTableorderheaders.iOrderType + " = " + SMTableorderheaders.ORDERTYPE_STANDING + ")"
			+ ")"
			+ " AND (" + SMTableorderheaders.TableName + "." + SMTableorderheaders.datOrderCanceledDate + " < '1950-01-01')"
			+ ")"
			
			+ " GROUP BY " + SMTableorderdetails.TableName + "." + SMTableorderdetails.sItemNumber + "," + SMTableorderheaders.TableName + "." + SMTableorderheaders.sLocation
			
			+ ") AS SOLINEQUERY ON ((SOLINEQUERY.ITEM = " + SMTableicitemlocations.TableName + "." + SMTableicitemlocations.sItemNumber 
			+ ") AND (SOLINEQUERY.LOC = " + SMTableicitemlocations.TableName + "." + SMTableicitemlocations.sLocation 
			+"))"
			//
			
			+ " WHERE (" 
				+ "(" + SMTableicitemlocations.TableName + "." + SMTableicitemlocations.sItemNumber + " = " 
				+ SMTableicitems.TableName + "." + SMTableicitems.sItemNumber + ")"
				+ " AND (" + SMTableicitemlocations.TableName + "." + SMTableicitemlocations.sMinQtyOnHand + " <> 0)"
				+ " AND (" + SMTableicitemlocations.TableName + "." + SMTableicitemlocations.sQtyOnHand + " < " 
				+ SMTableicitemlocations.TableName + "." + SMTableicitemlocations.sMinQtyOnHand + ")"
				+ " AND (";
				//+ SMTableicitemlocations.TableName + "." +  SMTableicitemlocations.sLocation + "=";
				
			for (int i = 0; i < sLocations.size(); i++){
				if(i == 0){
					SQL += " ( "  + SMTableicitemlocations.TableName + "." +  SMTableicitemlocations.sLocation + "=" + "'"+ sLocations.get(i) + "'" + ")" ;
				}else{
					SQL += " OR " +  " ( "  + SMTableicitemlocations.TableName + "." +  SMTableicitemlocations.sLocation + "=" + "'"+ sLocations.get(i) + "'" + ")";
				}
			}
				//Locations:
				
			SQL += ")) "
			+ " ORDER BY " + SMTableicitemlocations.TableName + "." + SMTableicitemlocations.sItemNumber; 
	*/

		//Check permissions for viewing items:
		boolean bViewItemPermitted = SMSystemFunctions.isFunctionPermitted(
			SMSystemFunctions.ICDisplayItemInformation,
			sUserID,
			conn,
			sLicenseModuleLevel);
			printRowHeader(out);
			
		int iItemCounter = 0;
		long lStartingTime = 0;
		long lEndingTime = 0;
		try{
			//System.out.println("In [1415133482]" + this.toString() + " SQL: " + SQL);
			if (bDebugMode){
				System.out.println("In " + this.toString() + " SQL: " + SQL);
			}
			lStartingTime = System.currentTimeMillis();
			ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
			lEndingTime = System.currentTimeMillis();
			boolean bFlipper = false;
			String sBackgroundColor = "";
			while(rs.next()){
				if (!bFlipper){
					sBackgroundColor = "\"#DCDCDC\"";
				}else{
					sBackgroundColor = "\"#FFFFFF\"";
				}
				//Print the line:
				out.println("<TR bgcolor =" + sBackgroundColor + ">");
				
				//Item number
				String sItemNumber = rs.getString("ITEMLOCQUERY" + "." + SMTableicitemlocations.sItemNumber);
				String sItemNumberLink = "";
				if (bViewItemPermitted){
					sItemNumberLink = "<A HREF=\"" + SMUtilities.getURLLinkBase(context) + "smic.ICDisplayItemInformation?ItemNumber=" 
				    		+ sItemNumber
				    		+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
				    		+ "\">" + clsServletUtilities.Fill_In_Empty_String_For_HTML_Cell(sItemNumber) + "</A>";
				}else{
					sItemNumberLink = sItemNumber;
				}
				out.println("<TD ALIGN=LEFT VALIGN=TOP><FONT SIZE=2>" + sItemNumberLink + "</FONT></TD>");
				
				//Location:
				out.println("<TD ALIGN=LEFT VALIGN=TOP><FONT SIZE=2>" 
						+ rs.getString("ITEMLOCQUERY" + "." + SMTableicitemlocations.sLocation) +
						"</FONT></TD>");
				
				//Description:
				out.println("<TD ALIGN=LEFT VALIGN=TOP><FONT SIZE=2>" 
						+ rs.getString(
								SMTableicitems.TableName + "." + SMTableicitems.sItemDescription) 
								+ "</FONT></TD>");
				
				//Qty On-Hand
				out.println("<TD ALIGN=RIGHT VALIGN=TOP><FONT SIZE=2>" 
						+ clsManageBigDecimals.BigDecimalToFormattedString(
								"###,###,##0.0000", rs.getBigDecimal(
								"ITEMLOCQUERY" + "." + SMTableicitemlocations.sQtyOnHand)) 
								+ "</FONT></TD>");
				
				//Min Qty On-Hand
				out.println("<TD ALIGN=RIGHT VALIGN=TOP><FONT SIZE=2>" 
						+ clsManageBigDecimals.BigDecimalToFormattedString(
								"###,###,##0.0000", rs.getBigDecimal(
								"ITEMLOCQUERY" + "." + SMTableicitemlocations.sMinQtyOnHand)) 
								+ "</FONT></TD>");
				
				//Qty unreceived
				out.println("<TD ALIGN=RIGHT VALIGN=TOP><FONT SIZE=2>" 
						+ clsManageBigDecimals.BigDecimalToFormattedString(
								"###,###,##0.0000", rs.getBigDecimal("POLINEQTYPENDING")) 
								+ "</FONT></TD>");
				//Qty Received, not yet post
				out.println("<TD ALIGN=RIGHT VALIGN=TOP><FONT SIZE=2>" 
						+ clsManageBigDecimals.BigDecimalToFormattedString(
								"###,###,##0.0000", rs.getBigDecimal("QTYRECNPOST")) 
								+ "</FONT></TD>");
				
				//Qty on sales order
				out.println("<TD ALIGN=RIGHT VALIGN=TOP><FONT SIZE=2>" 
						+ clsManageBigDecimals.BigDecimalToFormattedString(
								"###,###,##0.0000", rs.getBigDecimal("SOLINEQTYPENDING")) 
								+ "</FONT></TD>");
				
				//UOM
				out.println("<TD ALIGN=LEFT VALIGN=TOP><FONT SIZE=2>" 
						+ rs.getString(
								SMTableicitems.TableName + "." + SMTableicitems.sCostUnitOfMeasure) 
								+ "</FONT></TD>");
				
				out.println("</TR>");
				
				bFlipper = !bFlipper;
				iItemCounter++;
			}
			rs.close();
    	}catch (SQLException e){
    		m_sErrorMessage = "Error [1500654811] reading resultset with SQL: '" + SQL + "' - " + e.getMessage();
    		return false;
    	}
    	out.println("</TABLE>");
    	
    	out.println("<BR><B>" + Integer.toString(iItemCounter) + " items listed.");
    	out.println("<BR>Query took " + (lEndingTime - lStartingTime)/1000L + " seconds (" + Long.toString(lEndingTime - lStartingTime) + "ms) on database server.");
    	return true;
	}
	
	private void printRowHeader(PrintWriter out){
		
		out.println("<TABLE BORDER=0>");
		out.println("<TR>");
		out.println("<TD ALIGN=LEFT><B><FONT SIZE=3>Item&nbsp;#</FONT></B></TD>");
		out.println("<TD ALIGN=LEFT><B><FONT SIZE=3>Location</FONT></B></TD>");
		out.println("<TD ALIGN=LEFT><B><FONT SIZE=3>Description</FONT></B></TD>");
		out.println("<TD ALIGN=RIGHT><B><FONT SIZE=3>Qty&nbsp;On-Hand</FONT></B></TD>");
		out.println("<TD ALIGN=RIGHT><B><FONT SIZE=3>Min&nbsp;Qty&nbsp;On-Hand</FONT></B></TD>");
		out.println("<TD ALIGN=RIGHT><B><FONT SIZE=3>Qty&nbsp;On&nbsp;PO</FONT></B></TD>");
		out.println("<TD ALIGN=RIGHT><B><FONT SIZE=3>Qty&nbsp;Received&nbsp;<BR>Not&nbsp;Posted</FONT></B></TD>");  
		out.println("<TD ALIGN=RIGHT><B><FONT SIZE=3>Qty&nbsp;On<BR>Sales&nbsp;Order</FONT></B></TD>");
		out.println("<TD ALIGN=LEFT><B><FONT SIZE=3>UOM</FONT></B></TD>");
		out.println("</TR>");
		
		/*
		 * out.println("<TABLE BORDER=0 WIDTH=100%>");
		out.println("<TR>");
		out.println("<TD ALIGN=CENTER WIDTH=10%><B><FONT SIZE=3>Item&nbsp;#</FONT></B></TD>");
		out.println("<TD ALIGN=CENTER WIDTH=10%><B><FONT SIZE=3>Location</FONT></B></TD>");
		out.println("<TD ALIGN=LEFT WIDTH=35%><B><FONT SIZE=3>Description</FONT></B></TD>");
		out.println("<TD ALIGN=RIGHT WIDTH=15%><B><FONT SIZE=3>Qty&nbsp;On-Hand</FONT></B></TD>");
		out.println("<TD ALIGN=RIGHT WIDTH=15%><B><FONT SIZE=3>Min&nbsp;Qty&nbsp;On-Hand</FONT></B></TD>");
		out.println("<TD ALIGN=RIGHT WIDTH=15%><B><FONT SIZE=3>Qty&nbsp;On&nbsp;PO</FONT></B></TD>");
		out.println("<TD ALIGN=RIGHT WIDTH=15%><B><FONT SIZE=3>Qty&nbsp;Received&nbsp;<BR>Not&nbsp;Posted</FONT></B></TD>");  
		out.println("<TD ALIGN=RIGHT WIDTH=15%><B><FONT SIZE=3>Qty&nbsp;On<BR>Sales&nbsp;Order</FONT></B></TD>");
		out.println("<TD ALIGN=CENTER WIDTH=15%><B><FONT SIZE=3>UOM</FONT></B></TD>");
		out.println("</TR>");
		 */
	}
	public String getErrorMessage (){
		return m_sErrorMessage;
	}
}
