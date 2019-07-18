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
import SMClasses.SMLogEntry;
import SMDataDefinition.SMMasterStyleSheetDefinitions;
import SMDataDefinition.SMTableiccosts;
import SMDataDefinition.SMTableicitemlocations;
import SMDataDefinition.SMTableicitems;
import SMDataDefinition.SMTablelocations;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsDateAndTimeConversions;
import ServletUtilities.clsManageBigDecimals;
import ServletUtilities.clsServletUtilities;
import ServletUtilities.clsStringFunctions;

public class ICItemValuationReport extends java.lang.Object{

	private String m_sErrorMessage;
	private boolean bCostTableIsOpen = false;
	
	public ICItemValuationReport(
			){
		m_sErrorMessage = "";
	}
	public boolean processReport(
			Connection conn,
			String sStartingItem,
			String sEndingItem,
			boolean bShowIndividualBuckets,
			boolean bShowIndividualLocations,
			int iIncludingQuantities,
			int iIncludingCosts,
			ArrayList<String> sLocations,
			String sDBID,
			String sUserID,
			String sUserFullName,
			PrintWriter out,
			ServletContext context,
			String sLicenseModuleLevel
			){
		
		boolean bCostIncluded = false;
		boolean bQuantitiesIncluded = false;
		
		String SQL = "SELECT"
			+ " " + SMTableicitemlocations.TableName + "." + SMTableicitemlocations.sItemNumber
			+ ", " + SMTableicitemlocations.TableName + "." + SMTableicitemlocations.sLocation
			+ ", " + SMTableicitemlocations.TableName + "." + SMTableicitemlocations.sQtyOnHand
			+ ", " + SMTableicitemlocations.TableName + "." + SMTableicitemlocations.sTotalCost
			+ ", " + SMTableicitems.TableName + "." + SMTableicitems.sItemDescription
			+ ", " + SMTableicitems.TableName + "." + SMTableicitems.sCostUnitOfMeasure
			+ ", " + SMTableiccosts.TableName + "." + SMTableiccosts.iId
			+ ", " + SMTableiccosts.TableName + "." + SMTableiccosts.bdQty
			+ ", " + SMTableiccosts.TableName + "." + SMTableiccosts.iSource
			+ ", " + SMTableiccosts.TableName + "." + SMTableiccosts.sRemark
			+ ", " + SMTableiccosts.TableName + "." + SMTableiccosts.datCreationDate
			+ ", " + SMTableiccosts.TableName + "." + SMTableiccosts.bdCost
			+ ", " + SMTableiccosts.TableName + "." + SMTableiccosts.bdCostShipped
			+ ", " + SMTableiccosts.TableName + "." + SMTableiccosts.bdQtyShipped
			+ " FROM ("
			+ SMTableicitemlocations.TableName + " INNER JOIN "
			+ SMTableicitems.TableName + " ON " + SMTableicitemlocations.TableName + "." + SMTableicitemlocations.sItemNumber
			+ "= " + SMTableicitems.TableName + "." + SMTableicitems.sItemNumber 
			+ ") LEFT JOIN " + SMTableiccosts.TableName + " ON ("
			+ SMTableicitemlocations.TableName + "." + SMTableicitemlocations.sLocation 
			+ " = " + SMTableiccosts.TableName + "." + SMTableiccosts.sLocation + ")"
			+ " AND (" + SMTableicitemlocations.TableName + "." + SMTableicitemlocations.sItemNumber
			+ " = " + SMTableiccosts.TableName + "." + SMTableiccosts.sItemNumber + ")"
			+ " WHERE ("
				+ "(" + SMTableicitemlocations.TableName + "." + SMTableicitemlocations.sItemNumber 
					+ " >= '" + sStartingItem + "')"
				+ " AND (" + SMTableicitemlocations.TableName + "." + SMTableicitemlocations.sItemNumber 
					+ " <= '" + sEndingItem + "')";
				//Add a clause to limit qtys:
				switch (iIncludingQuantities){
					case 0:
						break;
					case 1:
						SQL = SQL + " AND  ( (" + SMTableicitemlocations.TableName + "." 
							+ SMTableicitemlocations.sQtyOnHand + " > 0.0000" + ")";
						bQuantitiesIncluded = true;
						break;
					case 2:
						SQL = SQL + " AND ( (" + SMTableicitemlocations.TableName + "." 
							+ SMTableicitemlocations.sQtyOnHand + " = 0.0000" + ")";
						bQuantitiesIncluded = true;
						break;
					case 3:
						SQL = SQL + " AND ( (" + SMTableicitemlocations.TableName + "." 
							+ SMTableicitemlocations.sQtyOnHand + " < 0.0000" + ")";
						bQuantitiesIncluded = true;
						break;
					case 4:
						SQL = SQL + " AND ( (" + SMTableicitemlocations.TableName + "." 
							+ SMTableicitemlocations.sQtyOnHand + " != 0.0000" + ")";
						bQuantitiesIncluded = true;
						break;
				}
		
				//Add a clause to limit costs:
				switch (iIncludingCosts){
				case 0:
					break;
				case 1:
					SQL = SQL + " AND (" + SMTableicitemlocations.TableName + "." 
							+ SMTableicitemlocations.sTotalCost + " > 0.00" + ") ";
					if(bQuantitiesIncluded){
						SQL = SQL + " )";
					}
					bCostIncluded = true;
					break;
				case 2:
					SQL = SQL + " AND (" + SMTableicitemlocations.TableName + "." 
							+ SMTableicitemlocations.sTotalCost + " = 0.00" + ") ";
					if(bQuantitiesIncluded){
						SQL = SQL + " )";
					}
					bCostIncluded = true;
					break;
				case 3:
					SQL = SQL + " AND (" + SMTableicitemlocations.TableName + "." 
							+ SMTableicitemlocations.sTotalCost + " < 0.00" + ") ";
					if(bQuantitiesIncluded){
						SQL = SQL + " )";
					}
					bCostIncluded = true;
					break;
				case 4:
					SQL = SQL + " AND (" + SMTableicitemlocations.TableName + "." 
							+ SMTableicitemlocations.sTotalCost + " != 0.00 )";
					if(bQuantitiesIncluded){
						SQL = SQL + " )";
					}
					bCostIncluded = true;
					break;
			}
				if(!bQuantitiesIncluded && !bCostIncluded){
					SQL += "  ";
				}else if(!bCostIncluded){
					SQL += " ) ";
				}
		
				SQL = SQL + " AND (";
				//Locations:
				for (int i = 0; i < sLocations.size(); i++){
					if (i > 0){
						SQL = SQL + " OR ";
					}
					SQL = SQL + "(" + SMTableicitemlocations.TableName + "." + SMTableicitemlocations.sLocation 
						+ " = '" + sLocations.get(i) + "')";
				}
			SQL = SQL + ")"	//Complete the 'AND' clause
			+ ")"	//Complete the 'where' clause
			+ "ORDER BY "
				+ SMTableicitemlocations.TableName + "." + SMTableicitemlocations.sItemNumber
				+ ", " + SMTableicitemlocations.TableName + "." + SMTableicitemlocations.sLocation
				+ ", " + SMTableiccosts.TableName + "." + SMTableiccosts.iSource
				+ ", " + SMTableiccosts.TableName + "." + SMTableiccosts.datCreationDate
			
			;
		
		//System.out.println("[1525795198] In " + this.toString() + ".processReport - main SQL = " + SQL);
		
    	String sCurrentItemNumber = "";
    	String sCurrentLocation = "";
    	int iLineNumberForItem = 0;
    	int iLineNumberForLocation = 0;
    	long lNumberOfItems = 0;
    			
		//Variables for report totals:
    	ArrayList<BigDecimal> arrLocationTotals = new ArrayList<BigDecimal>(0);
    	for (int i = 0; i < sLocations.size(); i ++){
    		arrLocationTotals.add(BigDecimal.ZERO);
    	}
		BigDecimal bdTotalItemsAmount = BigDecimal.ZERO;
    	
		//Check permissions for viewing items:
		boolean bViewItemPermitted = SMSystemFunctions.isFunctionPermitted(
			SMSystemFunctions.ICDisplayItemInformation,
			sUserID,
			conn,
			sLicenseModuleLevel);
		
    	try{
			ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
			while(rs.next()){
				String sItemNumber = rs.getString(SMTableicitemlocations.TableName 
						+ "." + SMTableicitemlocations.sItemNumber).trim();
				String sLocation = rs.getString(SMTableicitemlocations.TableName 
						+ "." + SMTableicitemlocations.sLocation).trim();
				BigDecimal bdItemValue = rs.getBigDecimal(
						SMTableicitemlocations.TableName + "." + SMTableicitemlocations.sTotalCost);
				if (sItemNumber.compareToIgnoreCase(sCurrentItemNumber) != 0){
					//If the table listing the costs has been started, but not finished,
					//end it now:
					if (bCostTableIsOpen){
						out.println("</TABLE>");
						bCostTableIsOpen = false;
					}
					
					if(bShowIndividualBuckets || bShowIndividualLocations){
						if (lNumberOfItems > 0){
							out.println("<BR>");
						}
						printItemHeader(
							sItemNumber,
							rs.getString(SMTableicitems.TableName + "." + SMTableicitems.sItemDescription).trim(),
							bViewItemPermitted,
							sDBID,
							out,
							context
							);
					}
					//Increment the item counter:
					lNumberOfItems++;
					
					if (bShowIndividualLocations){
						printLocationValues(
							sLocation,
							rs.getBigDecimal(SMTableicitemlocations.TableName + "." 
									+ SMTableicitemlocations.sQtyOnHand),
							bdItemValue,
							rs.getString(SMTableicitems.TableName 
									+ "." + SMTableicitems.sCostUnitOfMeasure).trim(),
							out
							);
					}
					//Accumulate the location total:
					arrLocationTotals.set(
						sLocations.indexOf(sLocation), 
						arrLocationTotals.get(sLocations.indexOf(sLocation)).add(bdItemValue));
					
					//Accumulate the total:
					bdTotalItemsAmount = bdTotalItemsAmount.add(bdItemValue);
					
					iLineNumberForItem = 0;
					iLineNumberForLocation = 0;
				}
				
				//If it's NOT the first record for the item, but the location number has changed,
				//print another location header:
				if (
					(iLineNumberForItem != 0)
					&& (sLocation.compareToIgnoreCase(sCurrentLocation) != 0)
				){
					//If the table listing the costs has been started, but not finished,
					//end it now:
					if (bCostTableIsOpen){
						out.println("</TABLE>");
						bCostTableIsOpen = false;
					}
					
					if (bShowIndividualLocations){
						printLocationValues(
								sLocation,
								rs.getBigDecimal(SMTableicitemlocations.TableName + "." 
										+ SMTableicitemlocations.sQtyOnHand),
								bdItemValue,
								rs.getString(SMTableicitems.TableName 
										+ "." + SMTableicitems.sCostUnitOfMeasure).trim(),
								out
								);
					}
					//Accumulate the location total:
					arrLocationTotals.set(
						sLocations.indexOf(sLocation), 
						arrLocationTotals.get(sLocations.indexOf(sLocation)).add(bdItemValue));

					//Accumulate the total:
					bdTotalItemsAmount = bdTotalItemsAmount.add(bdItemValue);
					
					iLineNumberForLocation = 0;
				}
				//Print the cost info:
				if (bShowIndividualBuckets){
					if (rs.getBigDecimal(SMTableiccosts.TableName + "." + SMTableiccosts.bdQty) != null){
					printCostLine(
							rs.getLong(SMTableiccosts.TableName + "." + SMTableiccosts.iId), 
							rs.getBigDecimal(SMTableiccosts.TableName + "." + SMTableiccosts.bdQty),
							rs.getBigDecimal(SMTableiccosts.TableName + "." + SMTableiccosts.bdCost),
							rs.getBigDecimal(SMTableiccosts.TableName + "." + SMTableiccosts.bdQtyShipped),
							rs.getBigDecimal(SMTableiccosts.TableName + "." + SMTableiccosts.bdCostShipped),
							rs.getString(SMTableicitems.TableName + "." + SMTableicitems.sCostUnitOfMeasure),
							rs.getInt(SMTableiccosts.TableName + "." + SMTableiccosts.iSource),
							rs.getString(SMTableiccosts.TableName + "." + SMTableiccosts.sRemark),
							rs.getDate(SMTableiccosts.TableName + "." + SMTableiccosts.datCreationDate),
							iLineNumberForLocation,
							sItemNumber,
							sLocation,
							out
							);
					}
				}
				//Reset the markers:
				sCurrentItemNumber = sItemNumber;
				sCurrentLocation = sLocation;
				iLineNumberForItem++;
				iLineNumberForLocation++;

			}
			rs.close();
    	}catch (SQLException e){
    		m_sErrorMessage = "Error reading resultset - " + e.getMessage();
    		return false;
    	}
    	
		//If the table listing the costs has been started, but not finished,
		//end it now:
		if (bCostTableIsOpen){
			out.println("</TABLE>");
			bCostTableIsOpen = false;
		}

		//Print the grand totals:
		printReportFooter(
				lNumberOfItems,
				bdTotalItemsAmount,
				sLocations,
				arrLocationTotals,
				out,
				conn
				);
		
		out.println("<BR>");
		
		//Print the SQL statement that created the list:
		out.println("<TABLE style = \" table-layout:fixed; width:100%; \" >"
			+ "<TR STYLE = \"  background-color: #E1D276;\">"
			+ "<TD style= \" word-wrap:break-word; \" >"
			+ "<P STYLE = \"font-family:arial\"> SQL Statement: '<B>" + clsStringFunctions.filter(SQL) + "'</B></P>"
			+ "</TD>"
			+ "</TR>"
			+ "</TABLE>"
		);
		
	    SMLogEntry log = new SMLogEntry(conn);
	    log.writeEntry(sUserID, SMLogEntry.LOG_OPERATION_ICITEMVALUATIONREPORT, "REPORT", "IC Item Valuation Report", "[1376509403]");
		return true;
	}
	
	private void printLocationValues(
		String sLocation,
		BigDecimal bdQtyOH,
		BigDecimal bdTotalCost,
		String sUnitOfMeasure,
		PrintWriter out
	){
		out.println("<BR>");
		out.println("<TABLE WIDTH = 100% CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_BASIC_WITH_BORDER + "\">");
		out.println("<TR  CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_ROW_HIGHLIGHT + "\">");
		out.println(
			"<TD WIDTH=15% CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL + "\"><B>Location:</B>&nbsp;" + sLocation + "</TD>"
			+"<TD WIDTH=15% CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL + "\"><B>Qty on hand:</B>&nbsp;" + clsManageBigDecimals.BigDecimalToFormattedString("########0.0000", bdQtyOH) + "</TD>"
			+"<TD WIDTH=15% CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL + "\"><B>Total cost:&nbsp:</B>&nbsp;" + clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(bdTotalCost) + "</TD>"
			+"<TD WIDTH=15% CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL + "\"><B>Unit of measure:</B>&nbsp;" + sUnitOfMeasure + "</TD>"
		);
		out.println("</TR>");
		out.println("</TABLE>");
		
	}
	private void printItemHeader(
		String sItemNumber, 
		String sDesc,
		boolean bViewItemPermitted,
		String sDBID,
		PrintWriter out,
		ServletContext context
		){
		
		String sItemNumberLink = sItemNumber;
		
		if (bViewItemPermitted){
			sItemNumberLink = "<A HREF=\"" + SMUtilities.getURLLinkBase(context) + "smic.ICDisplayItemInformation?ItemNumber=" 
		    		+ sItemNumber 
		    		+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
		    		+ "\">" + clsServletUtilities.Fill_In_Empty_String_For_HTML_Cell(sItemNumber) + "</A>";
		}
		
		out.println(
				"<TABLE WIDTH = 100% CLASS= \"" + SMMasterStyleSheetDefinitions.TABLE_BASIC_WITHOUT_BORDER + "\">"
				+ "<TR CLASS = \""+ SMMasterStyleSheetDefinitions.TABLE_HEADING +"\">"
				+ "<TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL + "\">"
				+ "<B>Item number:</B>&nbsp;" + sItemNumberLink 
				+ "&nbsp;&nbsp;<B>Description:</B>&nbsp;" + sDesc
				+ "</TD></TR></TABLE>"
				);
	}
	private void printCostLine(
			long lId, 
			BigDecimal bdQty,
			BigDecimal bdCost,
			BigDecimal bdQtyShipped,
			BigDecimal bdCostShipped,
			String sUnitOfMeasure,
			int iSource,
			String sRemark,
			java.sql.Date datCreationDate,
			int iLineNumberForLocation,
			String sItemNumber,
			String sLocation,
			PrintWriter out
			){
		
		if (sRemark.compareToIgnoreCase("") == 0){
			sRemark = "&nbsp;";
		}
		
		if (iLineNumberForLocation == 0){
			//Print header:
			out.println("<BR>");
			out.println("<TABLE WIDTH = 100% CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_BASIC_WITH_BORDER  +" \">");
			out.println( "<TR  CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_FOOTER + "\">");
			out.println("<TD COLSPAN = \"8\" CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL + "\"><U>Cost buckets for&nbsp;item&nbsp<B>" 
					+ sItemNumber + "</B>, location&nbsp;<B>" 
					+ sLocation + "</B>:</U></TD>");
			out.println("</TR>");
			out.println( "<TR  CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_FOOTER + "\">");
			out.println("<TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL + "\"><B>Type</B></TD>"
					+ "<TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL + "\"><B>Remark</B></TD>"
					+ "<TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL + "\"><B>UOM</B></TD>"
					+ "<TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL + "\"><B>Date</B></TD>"
					+ "<TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL + "\"><B>Qty shipped</B></TD>"
					+ "<TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL + "\"><B>Cost shipped</B></TD>"
					+ "<TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL + "\"><B>Qty left</B></TD>"
					+ "<TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL + "\"><B>Cost left</B></TD>"
					+ "</TR>"
			);
		}
		

				if(iLineNumberForLocation%2 == 0) {
					out.println( "<TR  CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_ROW_EVEN + "\">");
				}else {
					out.println( "<TR  CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_ROW_ODD + "\">");
				}
				out.println(
				 "<TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL + "\">" + SMTableiccosts.getCostSourceLabel(iSource) + "</TD>"
				+  "<TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL + "\">" + sRemark + "</TD>"
				+  "<TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL + "\">" + sUnitOfMeasure + "</TD>"
				+  "<TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL + "\">" + clsDateAndTimeConversions.utilDateToString(datCreationDate, "MM/dd/yyyy") + "</TD>"
				+  "<TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL + "\">" + clsManageBigDecimals.BigDecimalToFormattedString("########0.0000", bdQtyShipped) + "</TD>"
				+  "<TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL + "\">" + clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(bdCostShipped) + "</TD>"
				+  "<TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL + "\">" + clsManageBigDecimals.BigDecimalToFormattedString("########0.0000", bdQty) + "</TD>"
				+  "<TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL + "\">" + clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(bdCost) + "</TD>"
				+ "</TR>"
		);

		bCostTableIsOpen = true;
	}
	private void printReportFooter(
			long lNumberOfItems,
			BigDecimal bdTotalItemsAmount,
			ArrayList<String> sLocations,
			ArrayList<BigDecimal> bdLocationTotals,
			PrintWriter out,
			Connection conn
			){
		out.println("<BR>");
		out.println("<TABLE WIDTH = 100% CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_BASIC_WITH_BORDER + "\">");
			
		String sLocationDescription = "";

		for (int i = 0; i < sLocations.size(); i++){
			try{
				String SQL = "SELECT * FROM " + SMTablelocations.TableName
				+ " WHERE (" + SMTablelocations.sLocation + " = '" + sLocations.get(i) + "')";
				
				ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
				if (rs.next()){
					sLocationDescription = rs.getString(SMTablelocations.sLocationDescription).trim();
				}
				rs.close();
			}catch (SQLException e){
				System.out.println("Error reading location description for location: " + sLocations.get(i));
			}
			
			out.println(
				"<TR CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_TOTAL + "\">"
				+ "<TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\"><B>Total cost on hand for location&nbsp;<B>"
					+ sLocations.get(i) + "</B> - " + sLocationDescription + ":</TD>"
				+ "<TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\"><B>" + clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(
						bdLocationTotals.get(i)) + "</B></TD>"
				+ "</TR>"		
			);
		}
		
		out.println(
				"<TR CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_TOTAL + "\">"
						+ "<TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\"><B>Number Of Items:&nbsp" + Long.toString(lNumberOfItems) + ", Total Cost On Hand:</B></TD>"
						+ "<TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\"><B>" + clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(bdTotalItemsAmount) + "</B></TD>"
						+ "</TR>"
		);
		
		out.println("</TABLE>");
	}
	public String getErrorMessage (){
		return m_sErrorMessage;
	}
}
