package smic;

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.servlet.ServletContext;

import smcontrolpanel.SMSystemFunctions;
import smcontrolpanel.SMUtilities;
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
			ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
			while(rs.next()){
				//Print the line:
				out.println("<TR>");
				
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
				out.println("<TD><FONT SIZE=2>" + sPONumberLink + "</FONT></TD>");
				
				//View
				String sPOViewLink = "N/A";
				if (bAllowPOPrinting){
					sPOViewLink = 
						"<A HREF=\"" + SMUtilities.getURLLinkBase(context) + "smic.ICPrintPOGenerate"
						+ "?" + "StartingPOID" + "=" + sPONumber
						+ "&" + "EndingPOID" + "="
						+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID+ "=" + sDBID + "\">" + "View" + "</A>";
				}
				out.println("<TD><FONT SIZE=2>" + sPOViewLink + "</FONT></TD>");
				
				//Status:
				out.println("<TD><FONT SIZE=2>" 
						+ SMTableicpoheaders.getStatusDescription(rs.getInt(
								SMTableicpoheaders.TableName + "." + SMTableicpoheaders.lstatus)) 
								+ "</FONT></TD>");
				
				//Assigned date:
				out.println("<TD><FONT SIZE=2>" 
						+ clsDateAndTimeConversions.resultsetDateTimeStringToString(
							rs.getString(SMTableicpoheaders.TableName + "." 
							+ SMTableicpoheaders.datassigned)) + "</FONT></TD>");
				
				//Assigned to:
				out.println("<TD><FONT SIZE=2>" 
						+ rs.getString(
								SMTableicpoheaders.TableName + "." + SMTableicpoheaders.sassignedtofullname) 
								+ "</FONT></TD>");

				//Reference:
				out.println("<TD><FONT SIZE=2>" 
						+ rs.getString(
								SMTableicpoheaders.TableName + "." + SMTableicpoheaders.sreference).trim() 
								+ "</FONT></TD>");

				//Comment:
				out.println("<TD><FONT SIZE=2>" 
						+ rs.getString(
								SMTableicpoheaders.TableName + "." + SMTableicpoheaders.scomment).trim() 
								+ "</FONT></TD>");

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
		out.println("<TABLE style=\" border-style:none; padding:1px 4px 1px 4px;\">");
		out.println("<TR>");
		out.println("<TD style=\" padding-left:10px; padding-right:10px; \">"
				+ "<B><FONT SIZE=2>PO&nbsp;#</FONT></B></TD>");
		out.println("<TD style=\" padding-left:10px; padding-right:10px; \">"
				+ "<B><FONT SIZE=2>View&nbsp;?</FONT></B></TD>");
		out.println("<TD style=\" padding-left:10px; padding-right:10px; \">"
				+ "<B><FONT SIZE=2>Status</FONT></B></TD>");
		out.println("<TD style=\" padding-left:10px; padding-right:10px; \">"
				+ "<B><FONT SIZE=2>Date&nbsp;assigned</FONT></B></TD>");
		out.println("<TD style=\" padding-left:10px; padding-right:10px; \">"
				+ "<B><FONT SIZE=2>Assigned&nbsp;to</FONT></B></TD>");
		out.println("<TD style=\" padding-left:10px; padding-right:10px; \">"
				+ "<B><FONT SIZE=2>Reference</FONT></B></TD>");
		out.println("<TD style=\" padding-left:10px; padding-right:10px; \">"
				+ "<B><FONT SIZE=2>Comment</FONT></B></TD>");
		/*
		out.println("<TD style=\" padding-left:10px; padding-right:10px; \">"
				+ "<B><FONT SIZE=2>Deleted by</FONT></B></TD>");
		out.println("<TD style=\" padding-left:10px; padding-right:10px; \">"
				+ "<B><FONT SIZE=2>Deleted on</FONT></B></TD>");
		*/
		out.println("</TR>");
	}
	public String getErrorMessage (){
		return m_sErrorMessage;
	}
}
