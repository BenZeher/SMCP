package smic;

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.servlet.ServletContext;

import smcontrolpanel.SMSystemFunctions;
import smcontrolpanel.SMUtilities;
import SMDataDefinition.SMMasterStyleSheetDefinitions;
import SMDataDefinition.SMTableicitemlocations;
import SMDataDefinition.SMTableicitems;
import ServletUtilities.clsServletUtilities;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsManageBigDecimals;

public class ICQuantitiesOnHandReport extends java.lang.Object{

	private String m_sErrorMessage;
	private boolean bDebugMode = false;
	
	public ICQuantitiesOnHandReport(
			){
		m_sErrorMessage = "";
	}
	public boolean processReport(
			Connection conn,
			String sStartingItem,
			String sEndingItem,
			String sDBID,
			String sUserID,
			PrintWriter out,
			boolean bOutputToCSV,
			ServletContext context,
			String sLicenseModuleLevel
			){
		
		String SQL = "SELECT"
			+ " " + SMTableicitemlocations.TableName + "." + SMTableicitemlocations.sQtyOnHand
			+ ", " + SMTableicitemlocations.TableName + "." + SMTableicitemlocations.sLocation
			+ ", " + SMTableicitemlocations.TableName + "." + SMTableicitemlocations.sItemNumber
			+ ", " + SMTableicitems.TableName + "." + SMTableicitems.sItemDescription
			+ ", " + SMTableicitems.TableName + "." + SMTableicitems.sCostUnitOfMeasure
			+ ", " + SMTableicitems.TableName + "." + SMTableicitems.bdmostrecentcost
			+ ", " + SMTableicitems.TableName + "." + SMTableicitems.sComment1
			+ ", " + SMTableicitems.TableName + "." + SMTableicitems.sComment2
			+ " FROM " + SMTableicitems.TableName + " INNER JOIN "
			+ SMTableicitemlocations.TableName + " ON " 
			+ SMTableicitems.TableName + "." + SMTableicitems.sItemNumber 
			+ " = " + SMTableicitemlocations.TableName + "." + SMTableicitemlocations.sItemNumber
			+ " WHERE ("
				+ "(" + SMTableicitemlocations.TableName + "." + SMTableicitemlocations.sQtyOnHand
				+ " > 0.0000)"
				+ " AND (" + SMTableicitemlocations.TableName + "." + SMTableicitemlocations.sItemNumber
				+ " >= '" + sStartingItem + "')"
				+ " AND (" + SMTableicitemlocations.TableName + "." + SMTableicitemlocations.sItemNumber
				+ " <= '" + sEndingItem + "')"
 			+ ")"
 			+ " ORDER BY " + SMTableicitemlocations.TableName + "." + SMTableicitemlocations.sItemNumber
 				+ ", " + SMTableicitemlocations.TableName + "." + SMTableicitemlocations.sLocation
			;
		
		if (bDebugMode){
			clsServletUtilities.sysprint(this.toString(), sUserID, "Main SQL = " + SQL);
		}
		
		//Check permissions for viewing items:
		boolean bViewItemPermitted = SMSystemFunctions.isFunctionPermitted(
			SMSystemFunctions.ICDisplayItemInformation,
			sUserID,
			conn,
			sLicenseModuleLevel);
		
		if (bOutputToCSV){
			String sHeading = "\"ITEM\""
					+ ",\"LOCATION\""
					+ ",\"QTY\""
					+ ",\"DESCRIPTION\""
					+ ",\"MOSTRECENTCOST\""
					+ ",\"COMMENT1\""
					+ ",\"COMMENT2\""
			;
			out.println(sHeading);
	    	try{
				ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
				while(rs.next()){
					String sComment1 = rs.getString(SMTableicitems.TableName + "." + SMTableicitems.sComment1);
					if (sComment1 == null){
						sComment1 = "";
					}
					sComment1 = sComment1.trim().replace("\"", "\"\"");
					String sComment2 = rs.getString(SMTableicitems.TableName + "." + SMTableicitems.sComment2);
					if (sComment2 == null){
						sComment2 = "";
					}
					sComment2 = sComment2.trim().replace("\"", "\"\"");
					//Print each line:
					String sLine =
						
						//Item:
						"\""
						+ rs.getString(SMTableicitemlocations.TableName + "." 
							+ SMTableicitemlocations.sItemNumber).replace("\"", "\"\"")
						+ "\""
						+ ","
						
						//Location:
						+ "\""
						+ rs.getString(SMTableicitemlocations.TableName + "." 
						+ SMTableicitemlocations.sLocation).replace("\"", "\"\"")
						+ "\""
						+ ","
						
						//Qty
						+ clsManageBigDecimals.BigDecimalToFormattedString(
						"########0.0000", rs.getBigDecimal(
						SMTableicitemlocations.TableName + "." + SMTableicitemlocations.sQtyOnHand))
						+ ","	
						
						//Description:
						+ "\""
						+ rs.getString(SMTableicitems.TableName + "." 
						+ SMTableicitems.sItemDescription).replace("\"", "\"\"")
						+ "\""
						+ ","
						
						//Most recent cost
						+ clsManageBigDecimals.BigDecimalToFormattedString(
						"########0.0000", rs.getBigDecimal(
						SMTableicitems.TableName + "." + SMTableicitems.bdmostrecentcost))
						+ ","
						
						//Comment1
						+ "\""
						+ sComment1
						+ "\""
						+ ","

						//Comment2
						+ "\""
						+ sComment2
						+ "\""

					;
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
			printRowHeader(out);
			try{
				if (bDebugMode){
					System.out.println("In " + this.toString() + " SQL: " + SQL);
				}
				ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
				int iCount = 0;
				while(rs.next()){
					//Print the line:
					if(iCount%2 == 0) {
						out.println("<TR CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_ROW_EVEN + "\" >");
					}else {
						out.println("<TR CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_ROW_ODD + "\" >");
					}

					//Item
					String sItemNumber = rs.getString(SMTableicitemlocations.TableName + "." 
							+ SMTableicitemlocations.sItemNumber);
					String sItemNumberLink = "";
					if (bViewItemPermitted){
						sItemNumberLink = "<A HREF=\"" + SMUtilities.getURLLinkBase(context) + "smic.ICDisplayItemInformation?ItemNumber=" 
					    		+ sItemNumber
					    		+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
					    		+ "\">" + clsServletUtilities.Fill_In_Empty_String_For_HTML_Cell(sItemNumber) + "</A>";
					}else{
						sItemNumberLink = sItemNumber;
					}
					out.println("<TD  CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_ALIGN_TOP + "\" >" + sItemNumberLink + "</TD>");
					
					//Location:
					out.println("<TD  CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_ALIGN_TOP + "\" >" 
							+ rs.getString(SMTableicitemlocations.TableName + "." 
							+ SMTableicitemlocations.sLocation) + "</TD>");
					
					//Qty
					out.println("<TD  CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_ALIGN_TOP + "\" >" 
						+ clsManageBigDecimals.BigDecimalToFormattedString(
							"###,###,##0.0000", rs.getBigDecimal(
							SMTableicitemlocations.TableName + "." + SMTableicitemlocations.sQtyOnHand)) 
							+ "&nbsp;</TD>");
					
					//Description:
					out.println("<TD  CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_ALIGN_TOP + "\" >" 
						+ rs.getString(SMTableicitems.TableName + "." + SMTableicitems.sItemDescription) 
						+ "</TD>");
					
					//Most recent cost
					out.println("<TD  CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_ALIGN_TOP + "\" >" 
						+ clsManageBigDecimals.BigDecimalToFormattedString(
							"###,###,##0.0000", rs.getBigDecimal(
							SMTableicitems.TableName + "." + SMTableicitems.bdmostrecentcost)) 
							+ "&nbsp;</TD>");
					
					String sComment1 = rs.getString(SMTableicitems.TableName + "." + SMTableicitems.sComment1);
					if (sComment1 == null){
						sComment1 = "";
					}
					sComment1 = sComment1.trim().replace("\"", "\"\"").replace("/", " ");
					
					out.println("<TD  CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_ALIGN_TOP + "\" >" + sComment1 + "</TD>");
					
					String sComment2 = rs.getString(SMTableicitems.TableName + "." + SMTableicitems.sComment2);
					if (sComment2 == null){
						sComment2 = "";
					}
					sComment2 = sComment2.trim().replace("\"", "\"\"").replace("/", " ");
					out.println("<TD  CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_ALIGN_TOP + "\" >"  + sComment2 + "</TD>");
					
					out.println("</TR>");
					iCount++;
				}
				rs.close();
	    	}catch (SQLException e){
	    		m_sErrorMessage = "Error reading resultset - " + e.getMessage();
	    		return false;
	    	}
	
	    	out.println("</TABLE>");
		}
    	return true;
	}
	
	private void printRowHeader(
		PrintWriter out
	){
		out.println("<TABLE CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_BASIC_WITHOUT_BORDER + "\">");
		out.println("<TR CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_HEADING + "\" >");
		out.println("<TD  CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD + "\" >Item #</TD>");
		out.println("<TD  CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD + "\" >Loc.</TD>");
		out.println("<TD  CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD + "\" >Qty.</TD>");
		out.println("<TD  CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD + "\" >Description</TD>");
		out.println("<TD  CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD + "\" >Recent Cost </TD>");
		out.println("<TD  CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD + "\" >Comment 1</TD>");
		out.println("<TD  CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD + "\" >Comment 2</TD>");
		out.println("</TR>");
	}
	public String getErrorMessage (){
		return m_sErrorMessage;
	}
}
