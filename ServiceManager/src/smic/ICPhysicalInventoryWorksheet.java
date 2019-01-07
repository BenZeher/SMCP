package smic;

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

import SMClasses.SMLogEntry;
import SMDataDefinition.SMTableicinventoryworksheet;
import SMDataDefinition.SMTableicitems;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsManageBigDecimals;

public class ICPhysicalInventoryWorksheet extends java.lang.Object{

	private String m_sErrorMessage;
	private boolean bDebugMode = false;
	
	public ICPhysicalInventoryWorksheet(
			){
		m_sErrorMessage = "";
	}
	public boolean processReport(
			Connection conn,
			String sPhysicalInventoryID,
			String sStartingItem,
			String sEndingItem,
			String sDBID,
			String sUserID,
			boolean bShowQtyOnHand,
			boolean bOutputToCSV,
			PrintWriter out
			){
	
		String SQL = 
			"SELECT"
			+ " " + SMTableicinventoryworksheet.TableName + "." + SMTableicinventoryworksheet.lphysicalinventoryid
			+ ", " + SMTableicinventoryworksheet.TableName + "." + SMTableicinventoryworksheet.sitemnumber
			+ ", " + SMTableicinventoryworksheet.TableName + "." + SMTableicinventoryworksheet.bdqtyonhand
			+ ", " + SMTableicinventoryworksheet.TableName + "." + SMTableicinventoryworksheet.bdmostrecentcost
			+ ", " + SMTableicitems.TableName + "." + SMTableicitems.sItemDescription
			+ ", " + SMTableicitems.TableName + "." + SMTableicitems.sCostUnitOfMeasure
			+ " FROM "
			+ "(" + SMTableicinventoryworksheet.TableName 
			+ " LEFT JOIN " + SMTableicitems.TableName + " ON " + SMTableicinventoryworksheet.TableName 
			+ "." + SMTableicinventoryworksheet.sitemnumber + " = " 
			+ SMTableicitems.TableName + "." + SMTableicitems.sItemNumber + ")"
			+ " WHERE ("
				+ "(" + SMTableicinventoryworksheet.TableName + "." 
					+ SMTableicinventoryworksheet.lphysicalinventoryid + " = " + sPhysicalInventoryID + ")"
				+ " AND (" + SMTableicinventoryworksheet.TableName + "." 
					+ SMTableicinventoryworksheet.sitemnumber + " >= '" + sStartingItem + "')"
				+ " AND (" + SMTableicinventoryworksheet.TableName + "." 
					+ SMTableicinventoryworksheet.sitemnumber + " <= '" + sEndingItem + "')"
			+ ")"
			+ " ORDER BY"
			+ " " + SMTableicinventoryworksheet.TableName + "." + SMTableicinventoryworksheet.sitemnumber
			;
		
		if (bDebugMode){
			System.out.println("In " + this.toString() + ".processReport - main SQL = " + SQL);
		}
		
		if (bOutputToCSV){
			String sHeading = "\"Item\",\"Description\",\"UOM\"";
			if (bShowQtyOnHand){
				sHeading += ",\"Qty on hand\"";
			}
			out.println(sHeading);
	    	try{
				ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
				while(rs.next()){
					//Reset the marker:
					//Print each line:
					String sLine = 
						"\""
						+ rs.getString(SMTableicinventoryworksheet.TableName + "." 
						+ SMTableicinventoryworksheet.sitemnumber).replace("\"", "\"\"")
						+ "\""
						+ ","
						+ "\""
						+ rs.getString(SMTableicitems.TableName + "." 
							+ SMTableicitems.sItemDescription).replace("\"", "\"\"")
						+ "\""
						+ ","
						+ "\""
						+ rs.getString(SMTableicitems.TableName + "." 
							+ SMTableicitems.sCostUnitOfMeasure).replace("\"", "\"\"")
						+ "\""
					;
					
					if (bShowQtyOnHand){
						sLine += ","
							+ clsManageBigDecimals.BigDecimalToFormattedString(
							"########0.0000", rs.getBigDecimal(
							SMTableicinventoryworksheet.TableName + "." 
							+ SMTableicinventoryworksheet.bdqtyonhand)) 
						;
					}
					out.println(sLine);
				}
				rs.close();
				out.flush();
	            out.close();
	    	}catch (SQLException e){
	    		m_sErrorMessage = "Error reading resultset - " + e.getMessage();
	    		return false;
	    	}
			
		}else{
			printHeader(bShowQtyOnHand, out);
	    	try{
				ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
				while(rs.next()){
					//Reset the marker:
					//Print each line:
					out.println("<TR>");
					out.println("<TD><FONT SIZE=2>" + rs.getString(
						SMTableicinventoryworksheet.TableName + "." + SMTableicinventoryworksheet.sitemnumber) 
						+ "</FONT></TD>");
					out.println("<TD><FONT SIZE=2>" + rs.getString(
						SMTableicitems.TableName + "." + SMTableicitems.sItemDescription) 
						+ "</FONT></TD>");
					out.println("<TD><FONT SIZE=2>" + rs.getString(
						SMTableicitems.TableName + "." + SMTableicitems.sCostUnitOfMeasure) 
						+ "</FONT></TD>");
					if (bShowQtyOnHand){
						out.println(
							"<TD ALIGN=RIGHT><FONT SIZE=2>" 
							+ clsManageBigDecimals.BigDecimalToFormattedString(
							"###,###,##0.0000", rs.getBigDecimal(
							SMTableicinventoryworksheet.TableName + "." + SMTableicinventoryworksheet.bdqtyonhand)) 
							+ "</FONT></TD>"
						);
					}
					out.println("<TD><B><U><FONT SIZE=2>"
							+ "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;"
							+ "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;"
							+ "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;"
							+ "</FONT></U></B></TD>");
					out.println("</TR>");
				}
				rs.close();
	    	}catch (SQLException e){
	    		m_sErrorMessage = "Error reading resultset - " + e.getMessage();
	    		return false;
	    	}
	    			
			//Print the footer:
			printReportFooter(out);
		}
		
	    SMLogEntry log = new SMLogEntry(conn);
	    log.writeEntry(sUserID, SMLogEntry.LOG_OPERATION_ICPHYSICALINVENTORYWORKSHEET, "REPORT", "IC Physical Inventory Worksheet", "[1376509407]");

		return true;
	}
	private void printHeader(
			boolean bShowQtyOnHand,
			PrintWriter out
		){
			
			out.println("<TABLE BORDER=0>");
			out.println("<TR>");
			out.println("<TD><B><FONT SIZE=2>Item</FONT></B></TD>");
			out.println("<TD><B><FONT SIZE=2>Item Desc.</FONT></B></TD>");
			out.println("<TD><B><FONT SIZE=2>UOM</FONT></B></TD>");
			if (bShowQtyOnHand){
				out.println("<TD ALIGN=RIGHT><B><FONT SIZE=2>Qty on hand</FONT></B></TD>");
			}
			out.println("<TD ALIGN=RIGHT><B><FONT SIZE=2>Qty counted</FONT></B></TD>");
			out.println("</TR>");
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
