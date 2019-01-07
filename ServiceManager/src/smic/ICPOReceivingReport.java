package smic;

import java.sql.Connection;
import java.sql.ResultSet;
import java.util.ArrayList;

import javax.servlet.ServletContext;

import smcontrolpanel.SMSystemFunctions;
import smcontrolpanel.SMUtilities;
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
			+ ", " + SMTableicpoheaders.TableName + "." + SMTableicpoheaders.lphase
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
    						+ ")";
				//Is NOT on hold:
				SQL += " AND (" 
					+ "(" + SMTableicpoheaders.TableName + "." + SMTableicpoheaders.lphase
					+ " != " + SMTableicpoheaders.PHASE_ON_HOLD + ")"
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
					sOutPut += "<TR>";
					
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
					sOutPut += "<TD VALIGN=TOP><FONT SIZE=2>" + sPOLink + "</FONT></TD>";
					
					//PO date
					sOutPut += "<TD VALIGN=TOP><FONT SIZE=2>" + 
						clsDateAndTimeConversions.resultsetDateStringToString(rs.getString(SMTableicpoheaders.TableName + "." 
						+ SMTableicpoheaders.datpodate)) + "</FONT></TD>";
					
					//View?
					String sPOViewLink = "N/A";
					if (bAllowPOPrinting){
						sPOViewLink = 
							"<A HREF=\"" + SMUtilities.getURLLinkBase(context) + "smic.ICPrintPOGenerate"
							+ "?" + "StartingPOID" + "=" + sPOID
							+ "&" + "EndingPOID" + "="
							+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID + "\">" + "View" + "</A>";
					}
					sOutPut += "<TD VALIGN=TOP><FONT SIZE=2>" + sPOViewLink + "</FONT></TD>";
					
					//Location:
					sOutPut += "<TD VALIGN=TOP><FONT SIZE=2>" 
							+ rs.getString(SMTableicpolines.TableName + "." 
								+ SMTableicpolines.slocation) + "</FONT></TD>";
					
					//Qty
					sOutPut += "<TD VALIGN=TOP ALIGN=RIGHT><FONT SIZE=2>" 
							+ clsManageBigDecimals.BigDecimalToFormattedString(
									"###,###,##0.0000", rs.getBigDecimal(
									SMTableicpolines.TableName + "." + SMTableicpolines.bdqtyordered)) 
									+ "</FONT></TD>";
					
					sOutPut += "<TD VALIGN=TOP><FONT SIZE=2>" 
						+ rs.getString(SMTableicpolines.TableName + "." + SMTableicpolines.svendorsitemnumber) 
						+ "</FONT></TD>";
					
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
					sOutPut += "<TD VALIGN=TOP><FONT SIZE=2>" + sItemNumberLink + "</FONT></TD>";
					
					//Description:
					sOutPut += "<TD VALIGN=TOP><FONT SIZE=2>" 
							+ rs.getString(
									SMTableicpolines.TableName + "." + SMTableicpolines.sitemdescription) 
									+ "</FONT></TD>";
					//Work Order Comment
					String sTemp = rs.getString(
							SMTableicitems.TableName + "." + SMTableicitems.sworkordercomment);
					
					String sWorkOrderComment = sTemp == null ? " " : sTemp;
					sOutPut += "<TD VALIGN=TOP><FONT SIZE=2>" 
							+sWorkOrderComment+"</FONT></TD>";
					
					//Arrival date
					sOutPut += "<TD VALIGN=TOP><FONT SIZE=2>" + 
							clsDateAndTimeConversions.resultsetDateStringToString(rs.getString(SMTableicpoheaders.TableName + "." 
									+ SMTableicpoheaders.datexpecteddate)) + "</FONT></TD>";
					
					//Reference
					sOutPut += "<TD VALIGN=TOP><FONT SIZE=2>" 
							+ rs.getString(
									SMTableicpoheaders.TableName + "." + SMTableicpoheaders.sreference) 
									+ "</FONT></TD>";
					
					//Comment
					sOutPut += "<TD VALIGN=TOP><FONT SIZE=2>" 
							+ rs.getString(
									SMTableicpoheaders.TableName + "." + SMTableicpoheaders.scomment) 
									+ "</FONT></TD>";
					
					//Vendor
					sOutPut += "<TD VALIGN=TOP><FONT SIZE=2>" 
							+ rs.getString(
									SMTableicpoheaders.TableName + "." + SMTableicpoheaders.svendorname) 
									+ "</FONT></TD>";
					
					//Comment 1
					String sComment1 = rs.getString(SMTableicitems.TableName + "." + SMTableicitems.sComment1);
					if (sComment1 == null){
						sComment1 = "";
					}
					sComment1 = sComment1.trim();
					sOutPut += "<TD VALIGN=TOP><FONT SIZE=2>" + sComment1 + "</FONT></TD>";
					
					sOutPut += "</TR>";
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
		
		sOutPut += "<TABLE BORDER=0>";
		sOutPut += "<TR>";
		sOutPut += "<TD><B><FONT SIZE=2>PO&nbsp;#</FONT></B></TD>";
		sOutPut += "<TD><B><FONT SIZE=2>Date</FONT></B></TD>";
		sOutPut += "<TD><B><FONT SIZE=2>View&nbsp;?</FONT></B></TD>";
		sOutPut += "<TD><B><FONT SIZE=2>Loc.</FONT></B></TD>";
		sOutPut += "<TD ALIGN=RIGHT><B><FONT SIZE=2>Qty</FONT></B></TD>";
		sOutPut += "<TD><B><FONT SIZE=2>Vendor&nbsp;item&nbsp;#</FONT></B></TD>";
		sOutPut += "<TD><B><FONT SIZE=2>Item&nbsp;#</FONT></B></TD>";
		sOutPut += "<TD><B><FONT SIZE=2>Description</FONT></B></TD>";
		sOutPut += "<TD><B><FONT SIZE=2>Work&nbsp;Order&nbsp;Comment</FONT></B></TD>";
		sOutPut += "<TD><B><FONT SIZE=2>Arrival<BR>Date</FONT></B></TD>";
		sOutPut += "<TD><B><FONT SIZE=2>Reference</FONT></B></TD>";
		sOutPut += "<TD><B><FONT SIZE=2>Comment</FONT></B></TD>";
		sOutPut += "<TD><B><FONT SIZE=2>Vendor</FONT></B></TD>";
		sOutPut += "<TD><B><FONT SIZE=2>Item&nbsp;Comment1</FONT></B></TD>";
		sOutPut += "</TR>";
		
		return sOutPut;
	}
}
