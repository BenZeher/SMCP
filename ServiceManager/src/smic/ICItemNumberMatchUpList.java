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
				out.println("<TR>");
							
				//Vendor Item Number 
				String sVendorItemNumber = "<TD><FONT SIZE=2>" + rs.getString(SMTableicvendoritems.TableName + "." + SMTableicvendoritems.sVendorItemNumber) + "</FONT></TD>";

				//Item Number 
				String sItemNumber;
				String sItem = rs.getString(SMTableicitems.TableName + "." + SMTableicitems.sItemNumber);
				if (sItem == null){
					sItemNumber = "<TD><FONT SIZE=2>N/A</FONT></TD>";
				}else{
					if (bViewItemPermitted){
						sItemNumber = "<TD><FONT SIZE=2><A HREF=\"" + SMUtilities.getURLLinkBase(context) 
								+ "smic.ICDisplayItemInformation?ItemNumber=" 
								+ sItem
								+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
								+ "\">" + sItem + "</A>";
					}else{
						sItemNumber = "<TD><FONT SIZE=2>" + sItem + "</FONT></TD>";
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
					out.println("<TD><FONT SIZE=2>N/A</FONT></TD>");
				}else{
					out.println("<TD><FONT SIZE=2>" 
						+ s + "</FONT></TD>");
				}
				
				//Desc
				s = rs.getString(SMTableicitems.TableName + "." + SMTableicitems.sItemDescription);
				if (s == null){
					out.println("<TD><FONT SIZE=2>N/A</FONT></TD>");
				}else{
					out.println("<TD><FONT SIZE=2>" 
								+ s + "</FONT></TD>");
				}
				//UOM
				s = rs.getString(SMTableicitems.TableName + "." + SMTableicitems.sCostUnitOfMeasure);
				if (s == null){
					out.println("<TD><FONT SIZE=2>N/A</FONT></TD>");
				}else{
					out.println("<TD><FONT SIZE=2>" 
								+ s + "</FONT></TD>");
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
		
		out.println("<TABLE BORDER=0>");
		out.println("<TR>");
		if (sSortByOurItem.compareTo("0") == 0){
			out.println("<TD><B><FONT SIZE=2>Item Number</FONT></B></TD>");
			out.println("<TD><B><FONT SIZE=2>Vendor Item Number</FONT></B></TD>");
		}else{
			out.println("<TD><B><FONT SIZE=2>Vendor Item Number</FONT></B></TD>");
			out.println("<TD><B><FONT SIZE=2>Item Number</FONT></B></TD>");
		}
		out.println("<TD><B><FONT SIZE=2>Vendor</FONT></B></TD>");
		out.println("<TD><B><FONT SIZE=2>Description</FONT></B></TD>");
		out.println("<TD><B><FONT SIZE=2>UOM</FONT></B></TD>");
		out.println("</TR>");
	}
	public String getErrorMessage (){
		return m_sErrorMessage;
	}
}
