package smic;

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.servlet.ServletContext;
import smcontrolpanel.SMSystemFunctions;
import smcontrolpanel.SMUtilities;
import SMDataDefinition.SMTableicvendoritems;
import ServletUtilities.clsDatabaseFunctions;
import SMDataDefinition.SMMasterStyleSheetDefinitions;
import SMDataDefinition.SMTableicitems;

public class ICItemNumberMatchUpList extends java.lang.Object{

	private String m_sErrorMessage;
	private boolean bDebugMode = false;
	
	public ICItemNumberMatchUpList(
			){
		m_sErrorMessage = "";
	}
	public boolean processReport(
			Connection conn,
			String sStartingItem,
			String sEndingItem,
			String sSortbyOurItem,
			String sDBID,
			String sUserID,
			PrintWriter out,
			ServletContext context,
			String sLicenseModuleLevel
			){
	
		String SQL = "SELECT"
			+ " " + SMTableicitems.TableName + "." + SMTableicvendoritems.sItemNumber
			+ ", " + SMTableicvendoritems.TableName + "." + SMTableicvendoritems.sVendorItemNumber
			+ ", " + SMTableicvendoritems.TableName + "." + SMTableicvendoritems.sVendor
			+ ", " + SMTableicitems.TableName + "." + SMTableicitems.sCostUnitOfMeasure
			+ ", " + SMTableicitems.TableName + "." + SMTableicitems.sItemDescription
			;

			SQL += " FROM ("
			+ SMTableicvendoritems.TableName + " LEFT JOIN " 
			+ SMTableicitems.TableName + " ON " 
			+ SMTableicvendoritems.TableName + "." + SMTableicvendoritems.sItemNumber
			+ " = " + SMTableicitems.TableName + "." + SMTableicitems.sItemNumber
			+ ")";
			
			if (sSortbyOurItem.compareTo("1") == 0){
				SQL += " WHERE" + 
					   " " + SMTableicvendoritems.TableName + "." + SMTableicvendoritems.sVendorItemNumber + " <> ''" +
					   " AND" + 
					   " " + SMTableicvendoritems.TableName + "." + SMTableicvendoritems.sVendorItemNumber + " >= '" + sStartingItem + "'" +
					   " AND" + 
					   " " + SMTableicvendoritems.TableName + "." + SMTableicvendoritems.sVendorItemNumber + " <= '" + sEndingItem + "'" +
					   " ORDER BY" + 
					   	" " + SMTableicvendoritems.TableName + "." + SMTableicvendoritems.sVendorItemNumber;
			}else{
				SQL += " WHERE" + 
					   " " + SMTableicitems.TableName + "." + SMTableicitems.sItemNumber + " >= '" + sStartingItem + "'" +
					   " AND" + 
					   " " + SMTableicitems.TableName + "." + SMTableicitems.sItemNumber + " <= '" + sEndingItem + "'" +
					   " ORDER BY" + 
					   	" " + SMTableicitems.TableName + "." + SMTableicvendoritems.sItemNumber;
			}
			
			//System.out.println("SQL = " + SQL);
			
		//Check permissions for viewing items:
		boolean bViewItemPermitted = SMSystemFunctions.isFunctionPermitted(
			SMSystemFunctions.ICDisplayItemInformation,
			sUserID,
			conn,
			sLicenseModuleLevel);
		
		printRowHeader(sSortbyOurItem, out);
		int iCounter = 0;
		
		try{
			if (bDebugMode){
				System.out.println("In " + this.toString() + " SQL: " + SQL);
			}
			ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
			while(rs.next()){
				
				//Print the line:
				if(iCounter%2 == 0) {
					out.println("<TR CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_ROW_EVEN + "\" >");
				}else {
					out.println("<TR CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_ROW_ODD + "\" >");
				}
							
				//Vendor Item Number 
				String sVendorItemNumber ="<TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\" >" + rs.getString(SMTableicvendoritems.TableName + "." + SMTableicvendoritems.sVendorItemNumber) + "</TD>";

				//Item Number 
				String sItemNumber;
				String sItem = rs.getString(SMTableicitems.TableName + "." + SMTableicitems.sItemNumber);
				if (sItem == null){
					sItemNumber = "<TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\" >N/A</TD>";
				}else{
					if (bViewItemPermitted){
						sItemNumber = "<TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\" ><A HREF=\"" + SMUtilities.getURLLinkBase(context) 
								+ "smic.ICDisplayItemInformation?ItemNumber=" 
								+ sItem
								+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
								+ "\">" + sItem + "</A>";
					}else{
						sItemNumber = "<TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\" >"  + sItem + "</TD>";
					}
				}
				
				//now determine the order of the 2 item number columns.
				if (sSortbyOurItem.compareTo("0") == 0){
					out.println(sItemNumber + sVendorItemNumber);
				}else{
					out.println(sVendorItemNumber + sItemNumber);
				}

				//Vendor
				String s = rs.getString(SMTableicvendoritems.TableName + "." + SMTableicvendoritems.sVendor);
				if (s == null){
					out.println("<TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\" >N/A</TD>");
				}else{
					out.println("<TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\" >" 
						+ s + "</TD>");
				}
				
				//Desc
				s = rs.getString(SMTableicitems.TableName + "." + SMTableicitems.sItemDescription);
				if (s == null){
					out.println("<TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\" >N/A</TD>");
				}else{
					out.println("<TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\" >"  
								+ s + "</TD>");
				}
				//UOM
				s = rs.getString(SMTableicitems.TableName + "." + SMTableicitems.sCostUnitOfMeasure);
				if (s == null){
					out.println("<TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\" >N/A</TD>");
				}else{
					out.println("<TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\" >" 
								+ s + "</TD>");
				}
				
				out.println("</TR>");
				iCounter ++; 
			}
			rs.close();
    	}catch (SQLException e){
    		m_sErrorMessage = "Error reading resultset - " + e.getMessage();
    		return false;
    	}
    	
    	out.println("<TR><TD ALIGN=RIGHT COLSPAN=4><B>Total item counts: " + iCounter + "</B></TD></TR></TABLE>");
    	
		return true;
	}
	
	private void printRowHeader(String sSortByOurItem,
								PrintWriter out){
		
		out.println("<TABLE WIDTH = 100% CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_BASIC_WITHOUT_BORDER + "\">");
		out.println("<TR CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_HEADING + "\" >");
		if (sSortByOurItem.compareTo("0") == 0){
			out.println("<TD  CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD + "\" >Item Number</TD>");
			out.println("<TD  CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD + "\" >Vendor Item Number</TD>");
		}else{
			out.println("<TD  CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD + "\" >Vendor Item Number</TD>");
			out.println("<TD  CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD + "\" >Item Number</TD>");
		}
		out.println("<TD  CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD + "\" >Vendor</TD>");
		out.println("<TD  CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD + "\" >Description</TD>");
		out.println("<TD  CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD + "\" >UOM</TD>");
		out.println("</TR>");
	}
	public String getErrorMessage (){
		return m_sErrorMessage;
	}
}
