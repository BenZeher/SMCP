package smic;

import java.sql.Connection;
import java.sql.ResultSet;
import java.util.ArrayList;

import javax.servlet.ServletContext;

import smcontrolpanel.SMSystemFunctions;
import smcontrolpanel.SMUtilities;
import SMDataDefinition.SMMasterStyleSheetDefinitions;
import SMDataDefinition.SMTableicitems;
import SMDataDefinition.SMTableicpoheaders;
import SMDataDefinition.SMTableicpolines;
import ServletUtilities.clsServletUtilities;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsDateAndTimeConversions;
import ServletUtilities.clsManageBigDecimals;

public class ICPOReceivingReport extends java.lang.Object{

	private boolean bDebugMode = false;
	
	public ICPOReceivingReport(
			){
	}
	public String processReport(
			Connection conn,
			boolean bShowItemsFullyReceived,
			ArrayList<String>sLocations,
			String sStartingPODate,
			String sEndingPODate,
			String sStartingDate,
			String sEndingDate,
			String sStartingVendor,
			String sEndingVendor,
			String sDBID,
			String sUserID,
			boolean bOutputToCSV,
			ServletContext context,
			String sLicenseModuleLevel
			) throws Exception{
	
		//Create string of locations:
		String sLocationsString = "";
		for (int i = 0; i < sLocations.size(); i++){
			sLocationsString += "," + sLocations.get(i);
		}
		
		String SQL = "SELECT"
			+ " " + SMTableicpoheaders.TableName + "." + SMTableicpoheaders.lid
			+ ", " + SMTableicpoheaders.TableName + "." + SMTableicpoheaders.scomment
			+ ", " + SMTableicpoheaders.TableName + "." + SMTableicpoheaders.svendor
			+ ", " + SMTableicpoheaders.TableName + "." + SMTableicpoheaders.lstatus
			+ ", " + SMTableicpoheaders.TableName + "." + SMTableicpoheaders.sreference
			+ ", " + SMTableicpoheaders.TableName + "." + SMTableicpoheaders.svendorname
			+ ", " + SMTableicpoheaders.TableName + "." + SMTableicpoheaders.sponumber
			+ ", " + SMTableicpoheaders.TableName + "." + SMTableicpoheaders.datexpecteddate
			+ ", " + SMTableicpoheaders.TableName + "." + SMTableicpoheaders.datpodate
			+ ", " + SMTableicpolines.TableName + "." + SMTableicpolines.sitemnumber
			+ ", " + SMTableicpolines.TableName + "." + SMTableicpolines.bdqtyordered
			+ ", " + SMTableicpolines.TableName + "." + SMTableicpolines.datexpected
			+ ", " + SMTableicpolines.TableName + "." + SMTableicpolines.llinenumber
			+ ", " + SMTableicpolines.TableName + "." + SMTableicpolines.sitemdescription
			+ ", " + SMTableicpolines.TableName + "." + SMTableicpolines.svendorsitemnumber
			+ ", " + SMTableicpolines.TableName + "." + SMTableicpolines.slocation
			+ ", " + SMTableicpolines.TableName + "." + SMTableicpolines.bdqtyreceived
			+ ", " + SMTableicitems.TableName + "." + SMTableicitems.sComment1
			+ ", " + SMTableicitems.TableName + "." + SMTableicitems.sworkordercomment
			+ " FROM (" + SMTableicpoheaders.TableName + " INNER JOIN " + SMTableicpolines.TableName
			+ " ON " + SMTableicpoheaders.TableName + "." + SMTableicpoheaders.lid + " = " 
			+ SMTableicpolines.TableName + "." + SMTableicpolines.lpoheaderid + ") LEFT JOIN " 
			+ SMTableicitems.TableName + " ON " + SMTableicpolines.TableName + "." 
			+ SMTableicpolines.sitemnumber + " = " + SMTableicitems.TableName + "." 
			+ SMTableicitems.sItemNumber
				+ " WHERE ("
				//Arrival Dates
				+ "(" + SMTableicpoheaders.TableName + "." + SMTableicpoheaders.datexpecteddate 
					+ " >= '" + sStartingDate + " 00:00:00')"
				+ " AND (" + SMTableicpoheaders.TableName + "." + SMTableicpoheaders.datexpecteddate
					+ " <= '" + sEndingDate + " 23:59:59')"
				
				//PO dates
				+ " AND (" + SMTableicpoheaders.TableName + "." + SMTableicpoheaders.datpodate
					+ " >= '" + sStartingPODate + " 00:00:00')"
				+ " AND (" + SMTableicpoheaders.TableName + "." + SMTableicpoheaders.datpodate
					+ " <= '" + sEndingPODate + " 23:59:59')";
		
//				}else{
//					SQL += " AND ( "+SMTableicpoheaders.TableName + "." + SMTableicpoheaders.lstatus
//							+ " != "+SMTableicpoheaders.STATUS_COMPLETE+ " )";
//				}
					
				//Locations:
	            SQL += " AND (INSTR('" + sLocationsString + "', " + SMTableicpolines.TableName + "." 
            		+ SMTableicpolines.slocation + ") > 0)"
				
            	+ " AND (" + SMTableicpoheaders.TableName + "." + SMTableicpoheaders.svendor + " >=	'"
            		+ sStartingVendor + "')"
               	+ " AND (" + SMTableicpoheaders.TableName + "." + SMTableicpoheaders.svendor + " <=	'"
            		+ sEndingVendor + "')";
	            
	        	if(bShowItemsFullyReceived){
					SQL += 	 " AND( "
							+""+SMTableicpolines.TableName + "." + SMTableicpolines.bdqtyordered
							+ " = "+SMTableicpolines.TableName +"."+SMTableicpolines.bdqtyreceived
							+ " )";
				}else{
					SQL += 	 " AND( "
							+""+SMTableicpolines.TableName + "." + SMTableicpolines.bdqtyordered
							+ " != "+SMTableicpolines.TableName +"."+SMTableicpolines.bdqtyreceived
							+ " )";
				}
	 
				//Is NOT complete:
				SQL += " AND (" 
					+ "(" + SMTableicpoheaders.TableName + "." + SMTableicpoheaders.lstatus
					+ " = " + SMTableicpoheaders.STATUS_ENTERED + ")"
					+ " OR (" + SMTableicpoheaders.TableName + "." + SMTableicpoheaders.lstatus
					+ " = " + SMTableicpoheaders.STATUS_PARTIALLY_RECEIVED + ")"
				+ ")"

			+ ")"	//Complete the 'where' clause
			+ " ORDER BY "
				+ SMTableicpoheaders.TableName + "." + SMTableicpoheaders.svendor
				+ ", " + SMTableicpoheaders.TableName + "." + SMTableicpoheaders.lid
				+ ", " + SMTableicpolines.TableName + "." + SMTableicpolines.llinenumber
			;
			
		//Check permissions for viewing items:
		boolean bViewItemPermitted = SMSystemFunctions.isFunctionPermitted(
			SMSystemFunctions.ICDisplayItemInformation,
			sUserID,
			conn,
			sLicenseModuleLevel);
		
		//Determine if this user has rights to edit a PO:
		boolean bAllowPOEditing = 
			SMSystemFunctions.isFunctionPermitted(
					SMSystemFunctions.ICEditPurchaseOrders, 
					sUserID, 
					conn,
					sLicenseModuleLevel
			);
		
		//Determine if this user has rights to view a PO:
		boolean bAllowPOPrinting = 
			SMSystemFunctions.isFunctionPermitted(
					SMSystemFunctions.ICPrintPurchaseOrders, 
					sUserID, 
					conn,
					sLicenseModuleLevel
			);
		
		String sOutPut = "";
		String sLine = "";
		int iCount = 0;
		if (bOutputToCSV){
			
			String sHeading = "\"LOCATION\""
					+ ",\"PO\""
					+ ",\"PODATE\""
					+ ",\"QTY\""
					+ ",\"VENDORITEM\""
					+ ",\"ITEM\""
					+ ",\"DESCRIPTION\""
					+ ",\"WORKORDERCOMMENT\""
					+ ",\"ARRIVALDATE\""
					+ ",\"REFERENCE\""
					+ ",\"COMMENT\""
					+ ",\"VENDOR\""
					+ ",\"ITEMCOMMENT1\""
			;
			sOutPut += sHeading + "\n";
	    	try{
				ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
				while(rs.next()){
					
					//Print each line:
					//Location:
					String sLocation = rs.getString(SMTableicpolines.TableName + "." 
						+ SMTableicpolines.slocation);
					if (sLocation == null){
						sLocation = "";
					}
					
					sLine = 
					"\""
					+ sLocation.replace("\"", "\"\"")
					+ "\""
					+ ","
					;
					
					//PO
					String sPO = Long.toString(rs.getLong(SMTableicpoheaders.TableName + "." 
					+ SMTableicpoheaders.lid));
							
					if (sPO == null){
						sPO = "";
					}
							
					sLine += sPO.replace("\"", "\"\"") + ",";

					//PO date
					String sPODate = clsDateAndTimeConversions.resultsetDateStringToString(rs.getString(SMTableicpoheaders.TableName + "." 
						+ SMTableicpoheaders.datpodate))
					;
					if (sPODate == null){
						sPODate = "";
					}
					sLine += sPODate + ",";
					
					//Qty
					String sQty = clsManageBigDecimals.BigDecimalToFormattedString(
							"########0.0000", rs.getBigDecimal(
									SMTableicpolines.TableName + "." + SMTableicpolines.bdqtyordered))
					;
					if (sQty == null){
						sQty = "";
					}
					sLine += sQty + ",";
				
					//If there's a vendor item number on the PO line, use that:
					String sVendorItemNumber = rs.getString(SMTableicpolines.TableName + "." + SMTableicpolines.svendorsitemnumber);
					
					// TJR - 6/14 - took this out so that now ONLY the vendor's item number stored on the PO line will appear:
					//IF there's NO vendor item number on the line, grab one stored in the vendor's item table, if possible:
					//if (sVendorItemNumber.compareToIgnoreCase("") == 0){
					//	if (rs.getString(SMTableicvendoritems.TableName + "." + SMTableicvendoritems.sVendorItemNumber) != null){
					//		sVendorItemNumber = rs.getString(SMTableicvendoritems.TableName + "." + SMTableicvendoritems.sVendorItemNumber);
					//	}
					//}
				
					//Vendor item number:
					if (sVendorItemNumber == null){
						sVendorItemNumber = "";
					}
					
					sLine += "\""
					+ sVendorItemNumber.replace("\"", "\"\"")
					+ "\""
					+ ","
					;
				
					//Item:
					String sItem = rs.getString(SMTableicpolines.TableName + "." 
							+ SMTableicpolines.sitemnumber)
					;
					if (sItem == null){
						sItem = "";
					}
					sLine += "\"" + sItem.replace("\"", "\"\"") + "\"" + ",";
					
					//Description:
					String sDescription = rs.getString(SMTableicpolines.TableName + "." 
							+ SMTableicpolines.sitemdescription);
					if (sDescription == null){
						sDescription = "";
					}
					sLine += "\"" + sDescription.replace("\"", "\"\"") + "\"" + ",";
					
					//Work Order Comment:
					System.out.println("[1545431842]");
					String sWorkOrderComment = rs.getString(SMTableicitems.TableName + "." 
							+ SMTableicitems.sworkordercomment);
					System.out.println("[1545431843]");
					if (sWorkOrderComment == null){
						sWorkOrderComment = "";
					}
					
					sLine += "\"" + sWorkOrderComment.replace("\"", "\"\"") + "\"" + ",";
					
					//Arrival date
					String sArrivalDate = clsDateAndTimeConversions.resultsetDateStringToString(rs.getString(SMTableicpoheaders.TableName + "." 
							+ SMTableicpoheaders.datexpecteddate));
					if (sArrivalDate == null){
						sArrivalDate = "";
					}
					sLine += sArrivalDate + ",";
					
					//Reference:
					String sReference = rs.getString(SMTableicpoheaders.TableName + "." 
							+ SMTableicpoheaders.sreference);
					if (sReference == null){
						sReference = "";
					}
					sLine += "\"" + sReference.replace("\"", "\"\"") + "\"" + ",";

					//Comment:
					String sComment = rs.getString(SMTableicpoheaders.TableName + "." 
							+ SMTableicpoheaders.scomment);
					if (sComment == null){
						sComment = "";
					}
					sLine += "\"" + sComment.replace("\"", "\"\"") + "\"" + ",";

					//Vendor
					String sVendor = rs.getString(SMTableicpoheaders.TableName + "." 
							+ SMTableicpoheaders.svendorname);
					if (sVendor == null){
						sVendor = "";
					}
					sLine += "\"" + sVendor.replace("\"", "\"\"") + "\"" + ",";
					
					//Comment1
					String sComment1 = rs.getString(SMTableicitems.TableName + "." + SMTableicitems.sComment1);
					if (sComment1 == null){
						sComment1 = "";
					}
					sComment1 = sComment1.trim().replace("\"", "\"\"");
					sLine += "\"" + sComment1 + "\"";
					
					sOutPut += sLine + "\n";
				}
				rs.close();
	    	}catch (Exception e){
	    		throw new Exception("Error [1545428707] reading resultset - after line: '" + sLine + "' - " + e.getMessage());
	    	}
		}else{
			sOutPut += printRowHeader();
			try{
				if (bDebugMode){
					System.out.println("In " + this.toString() + " SQL: " + SQL);
				}
				ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
				while(rs.next()){
					//Print the line:
					if(iCount%2 == 0) {
						sOutPut += "<TR CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_ROW_EVEN + "\" >";
					}else {
						sOutPut += "<TR CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_ROW_ODD + "\" >";
					}
					
					//PO:
					String sPOID = Long.toString(rs.getLong(SMTableicpoheaders.TableName + "." 
							+ SMTableicpoheaders.lid));
					String sPOLink = sPOID;
					if (bAllowPOEditing){
						sPOLink = 
							"<A HREF=\"" + SMUtilities.getURLLinkBase(context) + "smic.ICEditPOEdit"
							+ "?" + ICPOHeader.Paramlid + "=" + sPOID
							+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID + "\">" + sPOID + "</A>";
					}
					sOutPut += "<TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_ALIGN_TOP + "\" >" + sPOLink + "</TD>";
					
					//PO date
					sOutPut += "<TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_ALIGN_TOP + "\" >" + 
						clsDateAndTimeConversions.resultsetDateStringToString(rs.getString(SMTableicpoheaders.TableName + "." 
						+ SMTableicpoheaders.datpodate)) + "</TD>";
					
					//View?
					String sPOViewLink = "N/A";
					if (bAllowPOPrinting){
						sPOViewLink = 
							"<A HREF=\"" + SMUtilities.getURLLinkBase(context) + "smic.ICPrintPOGenerate"
							+ "?" + "StartingPOID" + "=" + sPOID
							+ "&" + "EndingPOID" + "="
							+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID + "\">" + "View" + "</A>";
					}
					sOutPut += "<TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_ALIGN_TOP + "\" >" + sPOViewLink + "</TD>";
					
					//Location:
					sOutPut += "<TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_ALIGN_TOP + "\" >"
							+ rs.getString(SMTableicpolines.TableName + "." 
								+ SMTableicpolines.slocation) + "</TD>";
					
					//Qty
					sOutPut += "<TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_ALIGN_TOP + "\" >"
							+ clsManageBigDecimals.BigDecimalToFormattedString(
									"###,###,##0.0000", rs.getBigDecimal(
									SMTableicpolines.TableName + "." + SMTableicpolines.bdqtyordered)) 
									+ "</FONT></TD>";
					
					sOutPut +=  "<TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_ALIGN_TOP + "\" >"
						+ rs.getString(SMTableicpolines.TableName + "." + SMTableicpolines.svendorsitemnumber) 
						+ "</TD>";
					
					//Item
					String sItemNumber = rs.getString(SMTableicpolines.TableName + "." 
							+ SMTableicpolines.sitemnumber);
					String sItemNumberLink = "";
					if (bViewItemPermitted){
						sItemNumberLink = "<A HREF=\"" + SMUtilities.getURLLinkBase(context) + "smic.ICDisplayItemInformation?ItemNumber=" 
					    		+ sItemNumber
					    		+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
					    		+ "\">" + clsServletUtilities.Fill_In_Empty_String_For_HTML_Cell(sItemNumber) + "</A>";
					}else{
						sItemNumberLink = sItemNumber;
					}
					sOutPut +=  "<TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_ALIGN_TOP + "\" >"+ sItemNumberLink + "</TD>";
					
					//Description:
					sOutPut += "<TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_ALIGN_TOP + "\" >"
							+ rs.getString(
									SMTableicpolines.TableName + "." + SMTableicpolines.sitemdescription) 
									+ "</TD>";
					//Work Order Comment
					String sTemp = rs.getString(
							SMTableicitems.TableName + "." + SMTableicitems.sworkordercomment);
					
					String sWorkOrderComment = sTemp == null ? " " : sTemp;
					sOutPut +=  "<TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_ALIGN_TOP + "\" >"
							+sWorkOrderComment+"</TD>";
					
					//Arrival date
					sOutPut +=  "<TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_ALIGN_TOP + "\" >" +
							clsDateAndTimeConversions.resultsetDateStringToString(rs.getString(SMTableicpoheaders.TableName + "." 
									+ SMTableicpoheaders.datexpecteddate)) + "</TD>";
					
					//Reference
					sOutPut +=  "<TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_ALIGN_TOP + "\" >"
							+ rs.getString(
									SMTableicpoheaders.TableName + "." + SMTableicpoheaders.sreference) 
									+ "</TD>";
					
					//Comment
					sOutPut += "<TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_ALIGN_TOP + "\" >"
							+ rs.getString(
									SMTableicpoheaders.TableName + "." + SMTableicpoheaders.scomment) 
									+ "</TD>";
					
					//Vendor
					sOutPut +=  "<TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_ALIGN_TOP + "\" >"
							+ rs.getString(
									SMTableicpoheaders.TableName + "." + SMTableicpoheaders.svendorname) 
									+ "</TD>";
					
					//Comment 1
					String sComment1 = rs.getString(SMTableicitems.TableName + "." + SMTableicitems.sComment1);
					if (sComment1 == null){
						sComment1 = "";
					}
					sComment1 = sComment1.trim();
					sOutPut +=  "<TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_ALIGN_TOP + "\" >"+ sComment1 + "</TD>";
					
					sOutPut += "</TR>";
					iCount++;
				}
				rs.close();
	    	}catch (Exception e){
	    		throw new Exception("Error [1545428708] reading resultset - " + e.getMessage());
	    	}
	 
			sOutPut += "</TABLE>";

		}
    	return sOutPut;
	}
	
	private String printRowHeader(){
		
		String sOutPut = "";
		
		sOutPut += "<TABLE WIDTH = 100% CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_BASIC_WITH_BORDER_COLLAPSE + "\">";
		sOutPut += "<TR CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_HEADING + "\" >";
		sOutPut +="<TD  CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_ALIGN_TOP + "\" ><B>PO #</B<</TD>";
		sOutPut +="<TD  CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_ALIGN_TOP + "\" ><B>Date</B></TD>";
		sOutPut +="<TD NOWRAP  CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_ALIGN_TOP + "\" ><B>View ?</B></TD>";
		sOutPut +="<TD  CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_ALIGN_TOP + "\" ><B>Loc.</B></TD>";
		sOutPut +="<TD  CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_ALIGN_TOP + "\" ><B>Qty</B></TD>";
		sOutPut +="<TD  CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_ALIGN_TOP + "\" ><B>Vendor item #</B></TD>";
		sOutPut +="<TD  CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_ALIGN_TOP + "\" ><B>Item #</B></TD>";
		sOutPut +="<TD  CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_ALIGN_TOP + "\" ><B>Description</B></TD>";
		sOutPut +="<TD  CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_ALIGN_TOP + "\" ><B>Work Order Comment<B>/</TD>";
		sOutPut +="<TD  CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_ALIGN_TOP + "\" ><B>Arrival Date</B></TD>";
		sOutPut +="<TD  CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_ALIGN_TOP + "\" ><B>Reference</B></TD>";
		sOutPut +="<TD  CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_ALIGN_TOP + "\" ><B>Comment</B></TD>";
		sOutPut +="<TD  CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_ALIGN_TOP + "\" ><B>Vendor</B></TD>";
		sOutPut +="<TD  CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_ALIGN_TOP + "\" ><B>Item Comment 1</B></TD>";
		sOutPut += "</TR>";
		
		return sOutPut;
	}
}
