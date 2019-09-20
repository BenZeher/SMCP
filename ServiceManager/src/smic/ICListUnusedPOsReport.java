package smic;

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.servlet.ServletContext;

import smcontrolpanel.SMSystemFunctions;
import smcontrolpanel.SMUtilities;
import SMDataDefinition.SMMasterStyleSheetDefinitions;
import SMDataDefinition.SMTableicpoheaders;
import SMDataDefinition.SMTableicpolines;
import ServletUtilities.clsServletUtilities;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsDateAndTimeConversions;

public class ICListUnusedPOsReport extends java.lang.Object{

	private String m_sErrorMessage;
	private boolean bDebugMode = false;
	
	public ICListUnusedPOsReport(
			){
		m_sErrorMessage = "";
	}
	public boolean processReport(
			Connection conn,
			String sDBID,
			String sUserID,
			PrintWriter out,
			ServletContext context,
			String sLicenseModuleLevel
			){
	
		String SQL = "SELECT"
			+ " " + SMTableicpoheaders.TableName + "." + SMTableicpoheaders.lid
			+ ", " + SMTableicpoheaders.TableName + "." + SMTableicpoheaders.datassigned
			+ ", " + SMTableicpoheaders.TableName + "." + SMTableicpoheaders.sassignedtofullname
			+ ", " + SMTableicpoheaders.TableName + "." + SMTableicpoheaders.sreference
			+ ", " + SMTableicpoheaders.TableName + "." + SMTableicpoheaders.lstatus
			+ ", " + SMTableicpoheaders.TableName + "." + SMTableicpoheaders.scomment
			//+ ", " + SMTableicpoheaders.TableName + "." + SMTableicpoheaders.datdeleted
			//+ ", " + SMTableicpoheaders.TableName + "." + SMTableicpoheaders.sdeletedby
			+ " FROM " + SMTableicpoheaders.TableName
			+ " LEFT JOIN " + SMTableicpolines.TableName + " ON " + SMTableicpoheaders.TableName 
			+ "." + SMTableicpoheaders.lid + " = " + SMTableicpolines.TableName + "." 
			+ SMTableicpolines.lpoheaderid
			+ " WHERE (" 
				+ "(ISNULL(" + SMTableicpolines.TableName + "." + SMTableicpolines.lpoheaderid + ") = TRUE)"
				+ " AND (" + SMTableicpoheaders.TableName + "." + SMTableicpoheaders.lstatus + " != " 
					+ Integer.toString(SMTableicpoheaders.STATUS_DELETED) + ")"
			+ ")"
			+ " ORDER BY " + SMTableicpoheaders.TableName + "." + SMTableicpoheaders.lid
			;
		
		//Check permissions for viewing items:
		boolean bEditPurchaseOrdersPermitted = SMSystemFunctions.isFunctionPermitted(
			SMSystemFunctions.ICEditPurchaseOrders,
			sUserID,
			conn,
			sLicenseModuleLevel);
		
		//Determine if this user has rights to view a PO:
		boolean bAllowPOPrinting = 
			SMSystemFunctions.isFunctionPermitted(
					SMSystemFunctions.ICPrintPurchaseOrders, 
					sUserID, 
					conn,
					sLicenseModuleLevel
			);
		printRowHeader(out);
		try{
			if (bDebugMode){
				System.out.println("In " + this.toString() + " SQL: " + SQL);
			}
			int iCount = 0;
			ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
			while(rs.next()){
				//Print the line:
				if(iCount%2 ==0) {
					out.println("<TR CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_ROW_EVEN + "\" >");
				}else {
					out.println("<TR CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_ROW_ODD + "\" >");
				}
				
				//PO:
				String sPONumber = Long.toString(rs.getLong(SMTableicpoheaders.TableName + "." 
						+ SMTableicpoheaders.lid));
				String sPONumberLink = "";
				if (bEditPurchaseOrdersPermitted){
					sPONumberLink = "<A HREF=\"" + SMUtilities.getURLLinkBase(context) + "smic.ICEditPOEdit"
							+ "?" + ICPOHeader.Paramlid + "=" + sPONumber
							+ "&CallingClass=" + "smic.ICEditPOSelection"
				    		+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
				    		+ "\">" + clsServletUtilities.Fill_In_Empty_String_For_HTML_Cell(sPONumber) + "</A>";
				}else{
					sPONumberLink = sPONumber;
				}
				out.println("<TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\" >" + sPONumberLink + "</TD>");
				
				//View
				String sPOViewLink = "N/A";
				if (bAllowPOPrinting){
					sPOViewLink = 
						"<A HREF=\"" + SMUtilities.getURLLinkBase(context) + "smic.ICPrintPOGenerate"
						+ "?" + "StartingPOID" + "=" + sPONumber
						+ "&" + "EndingPOID" + "="
						+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID+ "=" + sDBID + "\">" + "View" + "</A>";
				}
				out.println("<TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\" >"  + sPOViewLink + "</TD>");
				
				//Status:
				out.println("<TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\" >" 
						+ SMTableicpoheaders.getStatusDescription(rs.getInt(
								SMTableicpoheaders.TableName + "." + SMTableicpoheaders.lstatus)) 
								+ "</TD>");
				
				//Assigned date:
				out.println("<TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\" >" 
						+ clsDateAndTimeConversions.resultsetDateTimeStringToString(
							rs.getString(SMTableicpoheaders.TableName + "." 
							+ SMTableicpoheaders.datassigned)) + "</TD>");
				
				//Assigned to:
				out.println("<TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\" >" 
						+ rs.getString(
								SMTableicpoheaders.TableName + "." + SMTableicpoheaders.sassignedtofullname) 
								+ "</TD>");

				//Reference:
				out.println("<TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\" >" 
						+ rs.getString(
								SMTableicpoheaders.TableName + "." + SMTableicpoheaders.sreference).trim() 
								+ "</TD>");

				//Comment:
				out.println("<TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\" >" 
						+ rs.getString(
								SMTableicpoheaders.TableName + "." + SMTableicpoheaders.scomment).trim() 
								+ "</TD>");

				/*
				String sDeletedBy = "N/A";
				String sDeletedDate = "N/A";
				if (rs.getInt(
					SMTableicpoheaders.TableName + "." + SMTableicpoheaders.lstatus)
						== SMTableicpoheaders.STATUS_DELETED){
					sDeletedBy = rs.getString(
						SMTableicpoheaders.TableName + "." + SMTableicpoheaders.sdeletedby).trim();
					sDeletedDate = SMUtilities.resultsetDateTimeStringToString(rs.getString(
							SMTableicpoheaders.TableName + "." + SMTableicpoheaders.datdeleted));
				}
				//Deleted by:
				out.println("<TD><FONT SIZE=2>" + sDeletedBy + "</FONT></TD>");
				out.println("<TD><FONT SIZE=2>" + sDeletedDate + "</FONT></TD>");
				*/
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
		PrintWriter out
	){
		out.println("<TABLE WIDTH = 100% CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_BASIC_WITH_BORDER + "\">");
		out.println("<TR CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_HEADING + "\" >");
		out.println("<TD  CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD + "\" >PO #</TD>");
		out.println("<TD  CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD + "\" >View ?</TD>");
		out.println("<TD  CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD + "\" >Status</TD>");
		out.println("<TD  CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD + "\" >Date Assigned</TD>");
		out.println("<TD  CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD + "\" >Assigned to</TD>");
		out.println("<TD  CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD + "\" >Reference</TD>");
		out.println("<TD  CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD + "\" >Comment</TD>");
		out.println("</TR>");
	}
	public String getErrorMessage (){
		return m_sErrorMessage;
	}
}
