package smic;

import java.io.PrintWriter;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import javax.servlet.ServletContext;
import smar.ARUtilities;
import smcontrolpanel.SMUtilities;
import SMClasses.SMLogEntry;
import SMDataDefinition.SMTableicinventoryworksheet;
import SMDataDefinition.SMTableicitems;
import SMDataDefinition.SMTableicphysicalcountlines;
import SMDataDefinition.SMTableicphysicalcounts;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsManageBigDecimals;

public class ICPhysicalInventoryVarianceReport extends java.lang.Object{

	private String m_sErrorMessage;
	private boolean bDebugMode = false;
	
	public ICPhysicalInventoryVarianceReport(
			){
		m_sErrorMessage = "";
	}
	public boolean processReport(
			Connection conn,
			String sPhysicalInventoryID,
			String sUserID,
			String sDBID,
			String sUserFullName,
			boolean bShowOnlyVariances,
			boolean bSummaryOnly,
			boolean bInactiveOnly,
			PrintWriter out,
			ServletContext context
			){
	
		String SQL = 
			"SELECT"
			+ " " + SMTableicinventoryworksheet.TableName + "." + SMTableicinventoryworksheet.lphysicalinventoryid
			+ ", " + SMTableicinventoryworksheet.TableName + "." + SMTableicinventoryworksheet.sitemnumber
			+ ", " + SMTableicinventoryworksheet.TableName + "." + SMTableicinventoryworksheet.bdqtyonhand
			+ ", " + SMTableicinventoryworksheet.TableName + "." + SMTableicinventoryworksheet.bdmostrecentcost
			+ ", " + SMTableicinventoryworksheet.TableName + "." + SMTableicinventoryworksheet.sinvacct
			+ ", " + SMTableicinventoryworksheet.TableName + "." + SMTableicinventoryworksheet.swriteoffacct
			+ ", " + SMTableicitems.TableName + "." + SMTableicitems.sItemDescription
			+ ", " + SMTableicitems.TableName + "." + SMTableicitems.bdmostrecentcost
			+ ", " + SMTableicitems.TableName + "." + SMTableicitems.sCostUnitOfMeasure
			+ ", " + SMTableicphysicalcountlines.TableName + "." + SMTableicphysicalcountlines.bdqty
			+ ", " + SMTableicphysicalcounts.TableName + "." + SMTableicphysicalcounts.lid
			+ ", " + SMTableicphysicalcounts.TableName + "." + SMTableicphysicalcounts.sdesc
			+ " FROM ("
			+ "(" + SMTableicinventoryworksheet.TableName 
			+ " LEFT JOIN " + SMTableicitems.TableName + " ON " + SMTableicinventoryworksheet.TableName 
			+ "." + SMTableicinventoryworksheet.sitemnumber + " = " 
			+ SMTableicitems.TableName + "." + SMTableicitems.sItemNumber + ")"
			+ " LEFT JOIN " + SMTableicphysicalcountlines.TableName + " ON ("
				+ SMTableicinventoryworksheet.TableName + "." + SMTableicinventoryworksheet.sitemnumber 
				+ " = " + SMTableicphysicalcountlines.TableName + "." 
				+ SMTableicphysicalcountlines.sitemnumber + ")"
				+ " AND (" + SMTableicinventoryworksheet.TableName + "." + SMTableicinventoryworksheet.lphysicalinventoryid
				+ " = " + SMTableicphysicalcountlines.TableName + "." + SMTableicphysicalcountlines.lphysicalinventoryid + ")"
			+ ")"
			+ " LEFT JOIN " + SMTableicphysicalcounts.TableName + " ON " 
			+ SMTableicphysicalcountlines.TableName + "." + SMTableicphysicalcountlines.lcountid
			+ " = " + SMTableicphysicalcounts.TableName + "." + SMTableicphysicalcounts.lid
			+ " WHERE ("
				+ "(" + SMTableicinventoryworksheet.TableName + "." 
				+ SMTableicinventoryworksheet.lphysicalinventoryid + " = " + sPhysicalInventoryID
				+ ")";
				if(bInactiveOnly){
					SQL +=  " AND (" + SMTableicitems.TableName + "." +  SMTableicitems.iActive + " = 0)";
				}			
		SQL += ")"
			+ " ORDER BY"
			+ " " + SMTableicinventoryworksheet.TableName + "." + SMTableicinventoryworksheet.sitemnumber
			
			;
		
		if (bDebugMode){
			System.out.println("In " + this.toString() + ".processReport - main SQL = " + SQL);
		}
    	String sPreviousLoopItem = "";
    	
    	//Fields used in the report:
    	String sReportItemNumber = "";
    	String sReportItemDesc = "";
    	String sInvGLAcct = "";
		String sWriteOffGLAcct = "";
    	String sReportUOM = "";
    	BigDecimal bdReportQtyOnHand = new BigDecimal(0);
    	BigDecimal bdReportQtyCounted = new BigDecimal(0);
    	BigDecimal bdReportMostRecentCost = new BigDecimal(0);
    	BigDecimal bdTotalCostVariance = new BigDecimal(0);
    	ArrayList<String> arrCountLines = new ArrayList<String>(0);
    			
		printLineHeader(out);
		
    	try{
			ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
			while(rs.next()){
				String sItem = rs.getString(SMTableicinventoryworksheet.TableName 
						+ "." + SMTableicinventoryworksheet.sitemnumber).trim()
				;
				
				//If there was a previous line, and the locationitem marker has changed, print the totals:
				if (
						(sItem.compareToIgnoreCase(sPreviousLoopItem) != 0)
						&& (sPreviousLoopItem.compareToIgnoreCase("") !=0)
				){
					printItemFooter(
						sReportItemNumber,
						sReportItemDesc,
						sInvGLAcct,
						sWriteOffGLAcct,
						sReportUOM,
						sPhysicalInventoryID,
						bdReportQtyOnHand,
						bdReportQtyCounted,
						bdReportMostRecentCost,
						arrCountLines,
						bShowOnlyVariances,
						bSummaryOnly,
						sDBID,
						out,
						context
					);
					
					//Accumulate the cost variance for this item:
					bdTotalCostVariance 
						= bdTotalCostVariance.add(
							bdReportQtyCounted.subtract(bdReportQtyOnHand).multiply(bdReportMostRecentCost));
					bdReportQtyCounted = BigDecimal.ZERO;
					arrCountLines.clear();
				}
				
				//Reset the marker:
				sPreviousLoopItem = sItem;

				//Set all the report variables:
				sReportItemNumber = rs.getString(SMTableicinventoryworksheet.TableName + "." 
						+ SMTableicinventoryworksheet.sitemnumber);
				sReportItemDesc = rs.getString(
					SMTableicitems.TableName + "." + SMTableicitems.sItemDescription);
		    	sInvGLAcct = rs.getString(
		    			SMTableicinventoryworksheet.TableName + "." + SMTableicinventoryworksheet.sinvacct);
				sWriteOffGLAcct = rs.getString(
						SMTableicinventoryworksheet.TableName + "." + SMTableicinventoryworksheet.swriteoffacct);
				sReportUOM = rs.getString(SMTableicitems.TableName + "." + SMTableicitems.sCostUnitOfMeasure);
				bdReportQtyOnHand = rs.getBigDecimal(SMTableicinventoryworksheet.TableName + "." 
					+ SMTableicinventoryworksheet.bdqtyonhand);
				bdReportMostRecentCost = rs.getBigDecimal(SMTableicitems.TableName + "." 
					+ SMTableicitems.bdmostrecentcost);

				//If there are any counts corresponding to this item, accumulate those in the countline
				//array:
				long lCountID = rs.getLong(SMTableicphysicalcounts.TableName + "." + SMTableicphysicalcounts.lid);
				if (lCountID != 0L){
					//Accumulate the qtys counted from each count line:
					BigDecimal bdLineCount = rs.getBigDecimal(
							SMTableicphysicalcountlines.TableName + "." + SMTableicphysicalcountlines.bdqty);
					
					arrCountLines.add(
						"<TR>"
						+ "<TD COLSPAN=7 ALIGN=RIGHT><FONT SIZE=2><I>"
						+ rs.getString(SMTableicphysicalcounts.TableName + "." 
								+ SMTableicphysicalcounts.sdesc)
						+ "&nbsp;(Count ID&nbsp;" + Long.toString(lCountID) + "):"
						+ "</I></FONT></TD>"
						+ "<TD ALIGN=RIGHT><FONT SIZE=2><I>"
						+ clsManageBigDecimals.BigDecimalToFormattedString("########0.0000", bdLineCount)
						+ "</I></FONT></TD>"
						+ "<TD COLSPAN=3"
						+ "&nbsp;"
						+ "</TD>"
						+ "</TR>"
					);

					if (bdLineCount != null){
						bdReportQtyCounted = bdReportQtyCounted.add(bdLineCount);
					}
				}
			}
			rs.close();
    	}catch (SQLException e){
    		m_sErrorMessage = "Error reading resultset - " + e.getMessage();
    		return false;
    	}
    	
    	//If there was anything printed, print the last location/item footer:
		if (sPreviousLoopItem.compareToIgnoreCase("") !=0)
		{
			printItemFooter(
					sReportItemNumber,
					sReportItemDesc,
					sInvGLAcct,
					sWriteOffGLAcct,
					sReportUOM,
					sPhysicalInventoryID,
					bdReportQtyOnHand,
					bdReportQtyCounted,
					bdReportMostRecentCost,
					arrCountLines,
					bShowOnlyVariances,
					bSummaryOnly,
					sDBID,
					out,
					context
			);
		}
		
		//Print the grand totals:
		printTotals(bdTotalCostVariance,out);
		printReportFooter(out);
		
	    SMLogEntry log = new SMLogEntry(conn);
	    log.writeEntry(sUserID, SMLogEntry.LOG_OPERATION_ICPHYSICALINVENTORYVARIANCEREPORT, "REPORT", "IC Physical Inventory Variance Report", "[1376509406]");

		return true;
	}
	private void printLineHeader(PrintWriter out){
		out.println("<TABLE BORDER=0>");
		out.println("<TR>");
		out.println("<TD><B><FONT SIZE=2>Item</FONT></B></TD>");
		out.println("<TD><B><FONT SIZE=2>Item Desc.</FONT></B></TD>");
		out.println("<TD><B><FONT SIZE=2>Inv. acct.</FONT></B></TD>");
		out.println("<TD><B><FONT SIZE=2>Write-off acct.</FONT></B></TD>");
		out.println("<TD><B><FONT SIZE=2>UOM</FONT></B></TD>");
		out.println("<TD ALIGN=RIGHT><B><FONT SIZE=2>Qty on hand</FONT></B></TD>");
		out.println("<TD ALIGN=RIGHT><B><FONT SIZE=2>Qty counted</FONT></B></TD>");
		out.println("<TD ALIGN=RIGHT><B><FONT SIZE=2>Most recent cost<BR>(Click to edit)</FONT></B></TD>");
		out.println("<TD ALIGN=RIGHT><B><FONT SIZE=2>Qty variance</FONT></B></TD>");
		out.println("<TD ALIGN=RIGHT><B><FONT SIZE=2>Cost variance</FONT></B></TD>");
		out.println("</TR>");
	}
	private void printItemFooter(
			String sItem,
			String sItemDesc,
			String sInvGLAcct,
			String sWriteOffGLAcct,
			String sUOM,
			String sPhysicalInventoryID,
			BigDecimal bdQtyOnHand,
			BigDecimal bdQtyCounted,
			BigDecimal bdMostRecentCost,
			ArrayList <String> arrCountInfoString,
			boolean bShowOnlyVariances,
			boolean bSummaryOnly,
			String sDBID,
			PrintWriter pwOut,
			ServletContext context
	){
		//First, calculate the variance:
		if (bdQtyCounted.compareTo(bdQtyOnHand) == 0){
			if (bShowOnlyVariances){
				return;
			}
		}
		
		pwOut.println("<TR>");
		String sReportItemNumber = "<A HREF=\"" + SMUtilities.getURLLinkBase(context) + "smic.ICDisplayItemInformation?ItemNumber=" 
    		+ sItem
    		+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
    		+ "\">" 
    		+ ARUtilities.Fill_In_Empty_String_For_HTML_Cell(sItem) + "</A>";
		
		pwOut.println("<TD><FONT SIZE=2>" + sReportItemNumber + "</FONT></TD>");
		pwOut.println("<TD><FONT SIZE=2>" + sItemDesc + "</FONT></TD>");
		pwOut.println("<TD><FONT SIZE=2>" + sInvGLAcct + "</FONT></TD>");
		pwOut.println("<TD><FONT SIZE=2>" + sWriteOffGLAcct + "</FONT></TD>");
		pwOut.println("<TD><FONT SIZE=2>" + sUOM + "</FONT></TD>");
		pwOut.println("<TD ALIGN=RIGHT><FONT SIZE=2>" 
			+ clsManageBigDecimals.BigDecimalToFormattedString("########0.0000", bdQtyOnHand) + "</FONT></B></TD>");
		pwOut.println("<TD ALIGN=RIGHT><FONT SIZE=2>");
		if (arrCountInfoString.size() > 0){
			pwOut.println("<U>");
		}
		pwOut.println(clsManageBigDecimals.BigDecimalToFormattedString("########0.0000", bdQtyCounted));
		if (arrCountInfoString.size() > 0){
			pwOut.println("</U>");
		}
		pwOut.println("</FONT></B></TD>");
		
		String sMostRecentCostLink = "<A HREF=\"" + SMUtilities.getURLLinkBase(context) + "smic." + "ICUpdateMostRecentCostEdit"
			+ "?" + ICItem.ParamItemNumber + "=" + sItem
			+ "&" + ICItem.ParamMostRecentCost + "=" 
				+ clsManageBigDecimals.BigDecimalToFormattedString("########0.0000", bdMostRecentCost)
			+ "&" + ICPhysicalInventoryEntry.ParamID + "=" + sPhysicalInventoryID
    		+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
    		+ "\">" + clsManageBigDecimals.BigDecimalToFormattedString("########0.0000", bdMostRecentCost) + "</A>";
		if (bDebugMode){
			System.out.println("In " + this.toString() + ".printLocationItemFooter - mostrecentcostlink = "
					+ sMostRecentCostLink
			);
		}
		pwOut.println("<TD ALIGN=RIGHT><FONT SIZE=2>" 
			+ sMostRecentCostLink + "</FONT></B></TD>");
		
		//Print the variances:
		//Qty variance
		pwOut.println("<TD ALIGN=RIGHT><FONT SIZE=2>" 
				+ clsManageBigDecimals.BigDecimalToFormattedString("########0.0000",
						bdQtyCounted.subtract(bdQtyOnHand)
					) + "</FONT></B></TD>");
		//Cost variance:
		pwOut.println("<TD ALIGN=RIGHT><FONT SIZE=2>" 
				+ clsManageBigDecimals.BigDecimalToFormattedString("########0.0000",
					(bdQtyCounted.subtract(bdQtyOnHand)).multiply(bdMostRecentCost)
					) + "</FONT></B></TD>");
		
		pwOut.println("</TR>");
		
		//Don't print the individual counts if the user has chosen to show the 'Summary Only':
		if (!bSummaryOnly){
			if (arrCountInfoString.size() > 0){
				if (arrCountInfoString.size() > 0){
					pwOut.println("</U>");
				}
				for (int i = 0; i < arrCountInfoString.size(); i++){
					pwOut.println(arrCountInfoString.get(i));
				}
			}
		}
	}
	private void printTotals(
			BigDecimal bdNetChange,
			PrintWriter out
		){
			out.println(
				"<TR><TD ALIGN=RIGHT COLSPAN=9><FONT SIZE=2><B>Net cost change total:" 
					+ "</B></FONT></TD>"
				+ "<TD ALIGN=RIGHT><FONT SIZE=2>" 
				+ clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(bdNetChange)
				+ "</FONT>"
			);
			out.println("</TR>");
			out.println("</TABLE>");
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
