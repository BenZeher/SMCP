package smic;

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.servlet.ServletContext;

import smcontrolpanel.SMSystemFunctions;
import smcontrolpanel.SMUtilities;
import SMDataDefinition.SMMasterStyleSheetDefinitions;
import SMDataDefinition.SMTableicitemprices;
import SMDataDefinition.SMTableicitems;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsManageBigDecimals;
import ServletUtilities.clsServletUtilities;

public class ICUpdateItemPricesPreview extends java.lang.Object{

	private String m_sErrorMessage;
	private boolean bDebugMode = false;
	
	public ICUpdateItemPricesPreview(
			){
		m_sErrorMessage = "";
	}
	public boolean processReport(
			Connection conn,
			String sStartingItem,
			String sEndingItem,
			String sStartingPriceList,
			String sEndingPriceList,
			String sStartingRptGrp1,
			String sEndingRptGrp1,
			String sStartingRptGrp2,
			String sEndingRptGrp2,
			String sStartingRptGrp3,
			String sEndingRptGrp3,
			String sStartingRptGrp4,
			String sEndingRptGrp4,
			String sStartingRptGrp5,
			String sEndingRptGrp5,
			boolean bUpdatePriceLevel0,
			boolean bUpdatePriceLevel1,
			boolean bUpdatePriceLevel2,
			boolean bUpdatePriceLevel3,
			boolean bUpdatePriceLevel4,
			boolean bUpdatePriceLevel5,
			boolean bUpdateByPercent,
			String sUpdateAmount,
			String sDBID,
			String sUserID,
			PrintWriter out,
			ServletContext context,
			String sLicenseModuleLevel
			){
	
		sUpdateAmount = sUpdateAmount.trim().replace(",", "");
		
		//TODO - left off here:
		String SQL = "SELECT"
			+ " " + SMTableicitemprices.TableName + "." + SMTableicitemprices.bdBasePrice
			+ ", " + SMTableicitemprices.TableName + "." + SMTableicitemprices.bdLevel1Price
			+ ", " + SMTableicitemprices.TableName + "." + SMTableicitemprices.bdLevel2Price
			+ ", " + SMTableicitemprices.TableName + "." + SMTableicitemprices.bdLevel3Price
			+ ", " + SMTableicitemprices.TableName + "." + SMTableicitemprices.bdLevel4Price
			+ ", " + SMTableicitemprices.TableName + "." + SMTableicitemprices.bdLevel5Price
			+ ", " + SMTableicitemprices.TableName + "." + SMTableicitemprices.sPriceListCode
			+ ", " + SMTableicitems.TableName + "." + SMTableicitems.sItemNumber
			+ ", " + SMTableicitems.TableName + "." + SMTableicitems.sItemDescription
			+ ", " + SMTableicitems.TableName + "." + SMTableicitems.sCostUnitOfMeasure
			+ ", " + SMTableicitems.TableName + "." + SMTableicitems.bdmostrecentcost
			;

			//Calculate the update prices:
			if (bUpdateByPercent){
				SQL += ", " + SMTableicitemprices.bdBasePrice + " * (1 + (" + sUpdateAmount + "/100)) AS UPDATEDLEVEL0"
				+ ", " + SMTableicitemprices.bdLevel1Price + " * (1 + (" + sUpdateAmount + "/100)) AS UPDATEDLEVEL1"
				+ ", " + SMTableicitemprices.bdLevel2Price + " * (1 + (" + sUpdateAmount + "/100)) AS UPDATEDLEVEL2"
				+ ", " + SMTableicitemprices.bdLevel3Price + " * (1 + (" + sUpdateAmount + "/100)) AS UPDATEDLEVEL3"
				+ ", " + SMTableicitemprices.bdLevel4Price + " * (1 + (" + sUpdateAmount + "/100)) AS UPDATEDLEVEL4"
				+ ", " + SMTableicitemprices.bdLevel5Price + " * (1 + (" + sUpdateAmount + "/100)) AS UPDATEDLEVEL5"
				;
			}else{
				SQL += ", " + SMTableicitemprices.bdBasePrice + " + " + sUpdateAmount + " AS UPDATEDLEVEL0"
				+ ", " + SMTableicitemprices.bdLevel1Price + " + " + sUpdateAmount + " AS UPDATEDLEVEL1"
				+ ", " + SMTableicitemprices.bdLevel2Price + " + " + sUpdateAmount + " AS UPDATEDLEVEL2"
				+ ", " + SMTableicitemprices.bdLevel3Price + " + " + sUpdateAmount + " AS UPDATEDLEVEL3"
				+ ", " + SMTableicitemprices.bdLevel4Price + " + " + sUpdateAmount + " AS UPDATEDLEVEL4"
				+ ", " + SMTableicitemprices.bdLevel5Price + " + " + sUpdateAmount + " AS UPDATEDLEVEL5"
				;
			}

			SQL += " FROM ("
			+ SMTableicitemprices.TableName + " LEFT JOIN " 
			+ SMTableicitems.TableName + " ON " 
			+ SMTableicitemprices.TableName + "." + SMTableicitemprices.sItemNumber
			+ " = " + SMTableicitems.TableName + "." + SMTableicitems.sItemNumber
			+ ")"
			+ " WHERE ("
				+ "(" + SMTableicitems.TableName + "." + SMTableicitems.sItemNumber 
					+ " >= '" + sStartingItem + "')" 
				+ " AND (" + SMTableicitems.TableName + "." + SMTableicitems.sItemNumber 
					+ " <= '" + sEndingItem + "')"					

				+ " AND (" + SMTableicitemprices.TableName + "." + SMTableicitemprices.sPriceListCode + " >= '" + 
					sStartingPriceList + "')"
				+ " AND (" + SMTableicitemprices.TableName + "." + SMTableicitemprices.sPriceListCode + " <= '" + 
					sEndingPriceList + "')"

				+ " AND (" + SMTableicitems.TableName + "." + SMTableicitems.sreportgroup1
					+ " >= '" + sStartingRptGrp1 + "')" 
				+ " AND (" + SMTableicitems.TableName + "." + SMTableicitems.sreportgroup1 
					+ " <= '" + sEndingRptGrp1 + "')"

				+ " AND (" + SMTableicitems.TableName + "." + SMTableicitems.sreportgroup2
					+ " >= '" + sStartingRptGrp2 + "')" 
				+ " AND (" + SMTableicitems.TableName + "." + SMTableicitems.sreportgroup2 
					+ " <= '" + sEndingRptGrp2 + "')"

				+ " AND (" + SMTableicitems.TableName + "." + SMTableicitems.sreportgroup3
					+ " >= '" + sStartingRptGrp3 + "')" 
				+ " AND (" + SMTableicitems.TableName + "." + SMTableicitems.sreportgroup3 
					+ " <= '" + sEndingRptGrp3 + "')"

				+ " AND (" + SMTableicitems.TableName + "." + SMTableicitems.sreportgroup4
					+ " >= '" + sStartingRptGrp4 + "')" 
				+ " AND (" + SMTableicitems.TableName + "." + SMTableicitems.sreportgroup4 
					+ " <= '" + sEndingRptGrp4 + "')"
					
				+ " AND (" + SMTableicitems.TableName + "." + SMTableicitems.sreportgroup5
					+ " >= '" + sStartingRptGrp5 + "')" 
				+ " AND (" + SMTableicitems.TableName + "." + SMTableicitems.sreportgroup5 
					+ " <= '" + sEndingRptGrp5 + "')"

			+ ")"	//Complete the 'where' clause
			+ " ORDER BY"
				+ " " + SMTableicitemprices.TableName + "." + SMTableicitemprices.sPriceListCode
				+ ", " + SMTableicitemprices.TableName + "." + SMTableicitemprices.sItemNumber
			;
		
		//Check permissions for viewing items:
		boolean bViewItemPermitted = SMSystemFunctions.isFunctionPermitted(
			SMSystemFunctions.ICDisplayItemInformation,
			sUserID,
			conn,
			sLicenseModuleLevel);
		
		printRowHeader(
			out,
			bUpdatePriceLevel0,
			bUpdatePriceLevel1,
			bUpdatePriceLevel2,
			bUpdatePriceLevel3,
			bUpdatePriceLevel4,
			bUpdatePriceLevel5
		);
		try{
			if (bDebugMode){
				System.out.println("[1579204455] In " + this.toString() + " SQL: " + SQL);
			}
			ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
			int iCount = 0;
			while(rs.next()){
				
				if(iCount % 2 == 0) {
					out.println("<TR CLASS=\"" + SMMasterStyleSheetDefinitions.TABLE_ROW_EVEN + "\">");
				}else {
					out.println("<TR CLASS=\"" + SMMasterStyleSheetDefinitions.TABLE_ROW_ODD + "\">");
				}
				//Print the line:

							
				//Price list
				out.println("<TD CLASS=\"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\">" 
					+ rs.getString(SMTableicitemprices.TableName + "." 
					+ SMTableicitemprices.sPriceListCode) + "");

				//Item
				String sItem = rs.getString(SMTableicitems.TableName + "." 
						+ SMTableicitems.sItemNumber);
				if (bViewItemPermitted){
					out.println("<TD CLASS=\"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\"><A HREF=\"" + SMUtilities.getURLLinkBase(context) 
						+ "smic.ICDisplayItemInformation?ItemNumber=" 
			    		+ sItem
			    		+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
			    		+ "\">" + clsServletUtilities.Fill_In_Empty_String_For_HTML_Cell(sItem) + "</A>");
				}else{
					out.println("<TD CLASS=\"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\">" + sItem + "");
				}

				//Desc
				out.println("<TD CLASS=\"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\">" 
					+ rs.getString(SMTableicitems.TableName + "." 
					+ SMTableicitems.sItemDescription) + "");
				
				//UOM
				out.println("<TD CLASS=\"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\">" 
						+ rs.getString(SMTableicitems.TableName + "." 
						+ SMTableicitems.sCostUnitOfMeasure) + "");
				
				//MRC
				out.println("<TD CLASS=\"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\">" 
						+ clsManageBigDecimals.BigDecimalToScaledFormattedString(
						2, rs.getBigDecimal(SMTableicitems.TableName + "." 
						+ SMTableicitems.bdmostrecentcost)) 
						+ "");
				
				//Level0
				if (bUpdatePriceLevel0){
					out.println("<TD CLASS=\"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\">" 
						+ clsManageBigDecimals.BigDecimalToScaledFormattedString(
						2, rs.getBigDecimal(SMTableicitemprices.TableName + "." 
						+ SMTableicitemprices.bdBasePrice)) 
						+ "");
					out.println("<TD CLASS=\"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\">" 
						+ clsManageBigDecimals.BigDecimalToScaledFormattedString(
						2, rs.getBigDecimal("UPDATEDLEVEL0")) 
						+ "");
				}
				
				//Level1
				if (bUpdatePriceLevel1){
					out.println("<TD CLASS=\"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\">" 
						+ clsManageBigDecimals.BigDecimalToScaledFormattedString(
						2, rs.getBigDecimal(SMTableicitemprices.TableName + "." 
						+ SMTableicitemprices.bdLevel1Price)) 
						+ "");
					out.println("<TD CLASS=\"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\">" 
						+ clsManageBigDecimals.BigDecimalToScaledFormattedString(
						2, rs.getBigDecimal("UPDATEDLEVEL1")) 
						+ "");
				}

				//Level2
				if (bUpdatePriceLevel2){
					out.println("<TD CLASS=\"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\">" 
						+ clsManageBigDecimals.BigDecimalToScaledFormattedString(
						2, rs.getBigDecimal(SMTableicitemprices.TableName + "." 
						+ SMTableicitemprices.bdLevel2Price)) 
						+ "");
					out.println("<TD CLASS=\"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\">" 
						+ clsManageBigDecimals.BigDecimalToScaledFormattedString(
						2, rs.getBigDecimal("UPDATEDLEVEL2")) 
						+ "");
				}

				//Level3
				if (bUpdatePriceLevel3){
					out.println("<TD CLASS=\"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\">" 
						+ clsManageBigDecimals.BigDecimalToScaledFormattedString(
						2, rs.getBigDecimal(SMTableicitemprices.TableName + "." 
						+ SMTableicitemprices.bdLevel3Price)) 
						+ "");
					out.println("<TD CLASS=\"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\">" 
						+ clsManageBigDecimals.BigDecimalToScaledFormattedString(
						2, rs.getBigDecimal("UPDATEDLEVEL3")) 
						+ "");
				}

				//Level4
				if (bUpdatePriceLevel4){
					out.println("<TD CLASS=\"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\">" 
						+ clsManageBigDecimals.BigDecimalToScaledFormattedString(
						2, rs.getBigDecimal(SMTableicitemprices.TableName + "." 
						+ SMTableicitemprices.bdLevel4Price)) 
						+ "");
					out.println("<TD CLASS=\"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\">" 
						+ clsManageBigDecimals.BigDecimalToScaledFormattedString(
						2, rs.getBigDecimal("UPDATEDLEVEL4")) 
						+ "");
				}

				//Level5
				if (bUpdatePriceLevel5){
					out.println("<TD CLASS=\"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\">" 
						+ clsManageBigDecimals.BigDecimalToScaledFormattedString(
						2, rs.getBigDecimal(SMTableicitemprices.TableName + "." 
						+ SMTableicitemprices.bdLevel5Price)) 
						+ "");
					out.println("<TD CLASS=\"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\">" 
						+ clsManageBigDecimals.BigDecimalToScaledFormattedString(
						2, rs.getBigDecimal("UPDATEDLEVEL5")) 
						+ "");
				}
				
				out.println("</TR>");
				iCount++;
			}
			rs.close();
    	}catch (SQLException e){
    		m_sErrorMessage = "Error reading resultset - " + e.getMessage();
    		return false;
    	}
    	
    	out.println("</TABLE>");
		return true;
	}
	
	private void printRowHeader(
		PrintWriter out,
		boolean IncludePriceLevel0,
		boolean IncludePriceLevel1,
		boolean IncludePriceLevel2,
		boolean IncludePriceLevel3,
		boolean IncludePriceLevel4,
		boolean IncludePriceLevel5
	){
		out.println("<TABLE WIDTH=100% CLASS=\"" + SMMasterStyleSheetDefinitions.TABLE_BASIC_WITH_BORDER + "\">");
		out.println("<TR CLASS=\"" + SMMasterStyleSheetDefinitions.TABLE_HEADING + "\">");
		out.println("<TD CLASS=\"" + SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_LEFT_JUSTIFIED + "\">Price list</TD>");
		out.println("<TD CLASS=\"" + SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_LEFT_JUSTIFIED + "\">Item</TD>");
		out.println("<TD CLASS=\"" + SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_LEFT_JUSTIFIED + "\">Description</TD>");
		out.println("<TD CLASS=\"" + SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_LEFT_JUSTIFIED + "\">UOM</TD>");
		out.println("<TD CLASS=\"" + SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_RIGHT_JUSTIFIED + "\">Most<BR>Recent&nbsp;Cost</TD>");
		if (IncludePriceLevel0){
			out.println("<TD CLASS=\"" + SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_RIGHT_JUSTIFIED + "\">Current<BR>Base</TD>");
			out.println("<TD CLASS=\"" + SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_RIGHT_JUSTIFIED + "\">Proposed<BR>Base</TD>");
		}
		if (IncludePriceLevel1){
			out.println("<TD CLASS=\"" + SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_RIGHT_JUSTIFIED + "\">Current<BR>Level 1</TD>");
			out.println("<TD CLASS=\"" + SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_RIGHT_JUSTIFIED + "\">Proposed<BR>Level 1</TD>");
		}
		if (IncludePriceLevel2){
			out.println("<TD CLASS=\"" + SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_RIGHT_JUSTIFIED + "\">Current<BR>Level 2</TD>");
			out.println("<TD CLASS=\"" + SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_RIGHT_JUSTIFIED + "\">Proposed<BR>Level 2</TD>");
		}
		if (IncludePriceLevel3){
			out.println("<TD CLASS=\"" + SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_RIGHT_JUSTIFIED + "\">Current<BR>Level 3</TD>");
			out.println("<TD CLASS=\"" + SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_RIGHT_JUSTIFIED + "\">Proposed<BR>Level 3</TD>");
		}
		if (IncludePriceLevel4){
			out.println("<TD CLASS=\"" + SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_RIGHT_JUSTIFIED + "\">Current<BR>Level 4</TD>");
			out.println("<TD CLASS=\"" + SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_RIGHT_JUSTIFIED + "\">Proposed<BR>Level 4</TD>");
		}
		if (IncludePriceLevel5){
			out.println("<TD CLASS=\"" + SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_RIGHT_JUSTIFIED + "\">Current<BR>Level 5</TD>");
			out.println("<TD CLASS=\"" + SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_RIGHT_JUSTIFIED + "\">Proposed<BR>Level 5</TD>");
		}
		out.println("</TR>");
	}
	public String getErrorMessage (){
		return m_sErrorMessage;
	}
}
