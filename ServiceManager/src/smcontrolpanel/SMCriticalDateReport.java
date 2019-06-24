package smcontrolpanel;

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import javax.servlet.ServletContext;

import SMDataDefinition.SMMasterStyleSheetDefinitions;
import SMDataDefinition.SMTableartransactions;
import SMDataDefinition.SMTablebids;
import SMDataDefinition.SMTablecriticaldates;
import SMDataDefinition.SMTableicpoheaders;
import SMDataDefinition.SMTableorderheaders;
import SMDataDefinition.SMTablesalescontacts;
import SMDataDefinition.SMTablesalesgroups;
import SMDataDefinition.SMTablesalesperson;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsDateAndTimeConversions;

public class SMCriticalDateReport extends java.lang.Object{

	private String m_sErrorMessage;
	private boolean bDebugMode = false;

	public SMCriticalDateReport(
	){
		m_sErrorMessage = "";
	}
	public boolean processReport(
			Connection conn,
			String sStartingDate,
			String sEndingDate,
			ArrayList <String> alSelectedUsers,
			ArrayList <String> alTypes,
			ArrayList <String> alStatus,
			ArrayList <String>arrSalesGroupCodes,
			String sOrderBy,
			String sAssignedBy,
			String sCurrentURL,
			String sDBID,
			String sUserID,
			PrintWriter out,
			ServletContext context
	){

		//SQL Statement:
		String sSQL = Get_Critical_Date_Report_SQL(sStartingDate, 
				sEndingDate, 
				alSelectedUsers,
				alTypes,
				alStatus,
				arrSalesGroupCodes,
				sAssignedBy,
				sOrderBy);
		//end SQL statement
		/*
		//Check permissions for viewing invoices and orders:
		boolean bViewOrderPermitted = SMSystemFunctions.isFunctionPermitted(
			SMSystemFunctions.ViewOrderInformation,
			sUserName,
			conn);

		boolean bViewJobCostSummaryPermitted = SMSystemFunctions.isFunctionPermitted(
				SMSystemFunctions.JobCostSummaryReport,
				sUserName,
				conn);
		 */
			
			try {
				printMultipleTypes(out, sSQL, conn, context, sDBID);
			} catch (Exception e) {
				m_sErrorMessage = "Error reading resultset - " + e.getMessage();
				return false;
			}
		
		return true;
	}
/*	
	private void printOnlyOrders(PrintWriter out, String sSQL, Connection conn, ServletContext context) throws Exception {
		//printout column header
				out.println("<TABLE WIDTH=100% cellspacing=0 cellpadding=1>");
				out.println("<TR>");
				//out.println("<TD ALIGN=CENTER bordercolor=\"000\" style=\"border: 1px solid\" VALIGN=TOP WIDTH=5%><FONT SIZE=2><B>ID</B></FONT></TD>");
				//out.println("<TD ALIGN=CENTER bordercolor=\"000\" style=\"border: 1px solid\" VALIGN=TOP WIDTH=3%><FONT SIZE=2><B>SP#</B></FONT></TD>");
				out.println("<TD ALIGN=CENTER bordercolor=\"000\" style=\"border: 1px solid\" VALIGN=TOP WIDTH=3%><FONT SIZE=2><B>Responsible</B></FONT></TD>");
				out.println("<TD ALIGN=CENTER bordercolor=\"000\" style=\"border: 1px solid\" VALIGN=TOP WIDTH=3%><FONT SIZE=2><B>Assigned</B></FONT></TD>");
				out.println("<TD ALIGN=CENTER bordercolor=\"000\" style=\"border: 1px solid\" VALIGN=TOP WIDTH=8%><FONT SIZE=2><B>Critical Date</B></FONT></TD>");
				//out.println("<TD ALIGN=CENTER bordercolor=\"000\" style=\"border: 1px solid\" VALIGN=TOP WIDTH=3%><FONT SIZE=2><B>Type</B></FONT></TD>");
				out.println("<TD ALIGN=LEFT bordercolor=\"000\" style=\"border: 1px solid\" VALIGN=TOP WIDTH=29%><FONT SIZE=2><B>Comment</B></FONT></TD>");
				out.println("<TD ALIGN=LEFT bordercolor=\"000\" style=\"border: 1px solid\" VALIGN=TOP WIDTH=14%><FONT SIZE=2><B>Ship to</B></FONT></TD>");
				out.println("<TD ALIGN=LEFT bordercolor=\"000\" style=\"border: 1px solid\" VALIGN=TOP WIDTH=14%><FONT SIZE=2><B>Bill to Name</B></FONT></TD>");
				out.println("<TD ALIGN=LEFT bordercolor=\"000\" style=\"border: 1px solid\" VALIGN=TOP WIDTH=10%><FONT SIZE=2><B>Project Manager</B></FONT></TD>");
				out.println("<TD ALIGN=CENTER bordercolor=\"000\" style=\"border: 1px solid\" VALIGN=TOP WIDTH=9%><FONT SIZE=2><B>Phone Number</B></FONT></TD>");
				out.println("<TD ALIGN=CENTER bordercolor=\"000\" style=\"border: 1px solid\" VALIGN=TOP WIDTH=7%><FONT SIZE=2><B>Order Number</B></FONT></TD>");
				out.println("</TR>");
				boolean bHasDetail = false;    //2

				try{

					ResultSet rs = clsDatabaseFunctions.openResultSet(sSQL, conn);
					while(rs.next()){
						bHasDetail = true;

						out.println("<TR>");
						//out.println("<TD ALIGN=CENTER VALIGN=TOP><FONT SIZE=2>"  + "</FONT></TD>");
						out.println("<TD ALIGN=CENTER VALIGN=TOP nowrap><FONT SIZE=2>" + rs.getString((SMTablecriticaldates.sresponsibleuserfullname).replace("`", "")) + "</FONT></TD>");
						out.println("<TD ALIGN=CENTER VALIGN=TOP nowrap><FONT SIZE=2>" + rs.getString((SMTablecriticaldates.sassignedbyuserfullname).replace("`", "")) + "</FONT></TD>");
						out.println("<TD ALIGN=CENTER VALIGN=TOP><FONT SIZE=2>" + clsDateAndTimeConversions.utilDateToString(rs.getDate((SMTablecriticaldates.TableName + "." + SMTablecriticaldates.sCriticalDate).replace("`", "")),"M/d/yyyy") + "</FONT></TD>");
						out.println("<TD ALIGN=LEFT VALIGN=TOP><FONT SIZE=2>" + rs.getString((SMTablecriticaldates.TableName + "." + SMTablecriticaldates.sComments).replace("`", "")) + "</FONT></TD>");
						out.println("<TD ALIGN=LEFT VALIGN=TOP><FONT SIZE=2>" + rs.getString((SMTableorderheaders.TableName + "." + SMTableorderheaders.sShipToName).replace("`", "")) + "</FONT></TD>"); //project.projectname
						out.println("<TD ALIGN=LEFT VALIGN=TOP><FONT SIZE=2>" + rs.getString((SMTableorderheaders.TableName + "." + SMTableorderheaders.sBillToName).replace("`", "")) + "</FONT></TD>"); //project.customername
						out.println("<TD ALIGN=LEFT VALIGN=TOP><FONT SIZE=2>" + rs.getString((SMTableorderheaders.TableName + "." + SMTableorderheaders.sBillToContact).replace("`", "")) + "</FONT></TD>"); //project.projectmanager
						out.println("<TD ALIGN=CENTER VALIGN=TOP><FONT SIZE=2>" + rs.getString((SMTableorderheaders.TableName + "." + SMTableorderheaders.sBillToPhone).replace("`", "")) + "</FONT></TD>"); //project.phonenumber
						out.println("<TD ALIGN=CENTER VALIGN=TOP><FONT SIZE=2>" 
							+ "										<A HREF=\"" 
							+ SMUtilities.getURLLinkBase(context) 
							+ "smcontrolpanel.SMDisplayOrderInformation?OrderNumber=" 
							+ rs.getString((SMTableorderheaders.TableName + "." 
							+ SMTableorderheaders.sOrderNumber).replace("`", "")) 
							+ "#CriticalDatesFooter\">" 
							+ rs.getString((SMTableorderheaders.TableName + "." 
							+ SMTableorderheaders.sOrderNumber).replace("`", "")) 
							+ "</A></FONT></TD>"); //clicking on job number leads to project information
						out.println("</TR>");
					}
					rs.close();

				}catch (SQLException e){
					throw new Exception("Error print only orders - " + e.getMessage());
				}
				if (!bHasDetail){
					out.println("<TR><TD ALIGN=CENTER COLSPAN=9>&nbsp;</TD></TR>");
					out.println("<TR><TD ALIGN=CENTER COLSPAN=9><FONT SIZE=3><B>No Critical Date Information to Display</B></FONT></TD></TR>");
				}
				out.println("</TABLE>");
	}
*/
/*
	private void printOnlyPurchaseOrders(PrintWriter out, String sSQL, Connection conn, ServletContext context) throws Exception {
		//printout column header
				out.println("<TABLE WIDTH=100% cellspacing=0 cellpadding=1>");
				out.println("<TR>");
				//out.println("<TD ALIGN=CENTER bordercolor=\"000\" style=\"border: 1px solid\" VALIGN=TOP WIDTH=5%><FONT SIZE=2><B>ID</B></FONT></TD>");
				//out.println("<TD ALIGN=CENTER bordercolor=\"000\" style=\"border: 1px solid\" VALIGN=TOP WIDTH=3%><FONT SIZE=2><B>SP#</B></FONT></TD>");
				out.println("<TD ALIGN=CENTER bordercolor=\"000\" style=\"border: 1px solid\" VALIGN=TOP WIDTH=3%><FONT SIZE=2><B>Responsible</B></FONT></TD>");
				out.println("<TD ALIGN=CENTER bordercolor=\"000\" style=\"border: 1px solid\" VALIGN=TOP WIDTH=3%><FONT SIZE=2><B>Assigned</B></FONT></TD>");
				out.println("<TD ALIGN=CENTER bordercolor=\"000\" style=\"border: 1px solid\" VALIGN=TOP WIDTH=8%><FONT SIZE=2><B>Critical Date</B></FONT></TD>");
				//out.println("<TD ALIGN=CENTER bordercolor=\"000\" style=\"border: 1px solid\" VALIGN=TOP WIDTH=3%><FONT SIZE=2><B>Type</B></FONT></TD>");
				out.println("<TD ALIGN=LEFT bordercolor=\"000\" style=\"border: 1px solid\" VALIGN=TOP WIDTH=29%><FONT SIZE=2><B>Comment</B></FONT></TD>");
				out.println("<TD ALIGN=LEFT bordercolor=\"000\" style=\"border: 1px solid\" VALIGN=TOP WIDTH=14%><FONT SIZE=2><B>Vendor</B></FONT></TD>");
				out.println("<TD ALIGN=LEFT bordercolor=\"000\" style=\"border: 1px solid\" VALIGN=TOP WIDTH=14%><FONT SIZE=2><B>Satus</B></FONT></TD>");
				out.println("<TD ALIGN=LEFT bordercolor=\"000\" style=\"border: 1px solid\" VALIGN=TOP WIDTH=10%><FONT SIZE=2><B>Expected Date</B></FONT></TD>");
				out.println("<TD ALIGN=LEFT bordercolor=\"000\" style=\"border: 1px solid\" VALIGN=TOP WIDTH=10%><FONT SIZE=2><B>PO Number</B></FONT></TD>");
				out.println("</TR>");
				boolean bHasDetail = false;    //2

				try{

					ResultSet rs = clsDatabaseFunctions.openResultSet(sSQL, conn);
					while(rs.next()){
						bHasDetail = true;

						out.println("<TR>");
						//out.println("<TD ALIGN=CENTER VALIGN=TOP><FONT SIZE=2>"  + "</FONT></TD>");
						out.println("<TD ALIGN=CENTER VALIGN=TOP nowrap><FONT SIZE=2>" + rs.getString((SMTablecriticaldates.sresponsibleuserfullname).replace("`", "")) + "</FONT></TD>");
						out.println("<TD ALIGN=CENTER VALIGN=TOP nowrap><FONT SIZE=2>" + rs.getString((SMTablecriticaldates.sassignedbyuserfullname).replace("`", "")) + "</FONT></TD>");
						out.println("<TD ALIGN=CENTER VALIGN=TOP><FONT SIZE=2>" + clsDateAndTimeConversions.utilDateToString(rs.getDate((SMTablecriticaldates.TableName + "." + SMTablecriticaldates.sCriticalDate)),"M/d/yyyy") + "</FONT></TD>");
						out.println("<TD ALIGN=LEFT VALIGN=TOP><FONT SIZE=2>" + rs.getString((SMTablecriticaldates.TableName + "." + SMTablecriticaldates.sComments).replace("`", "")) + "</FONT></TD>");
						out.println("<TD ALIGN=LEFT VALIGN=TOP><FONT SIZE=2>" + rs.getString((SMTableicpoheaders.TableName + "." + SMTableicpoheaders.svendor).replace("`", "")) + "</FONT></TD>"); 
						out.println("<TD ALIGN=LEFT VALIGN=TOP><FONT SIZE=2>" + SMTableicpoheaders.getStatusDescription(rs.getInt((SMTableicpoheaders.TableName + "." + SMTableicpoheaders.lstatus))) + "</FONT></TD>"); 
						out.println("<TD ALIGN=LEFT VALIGN=TOP><FONT SIZE=2>" + clsDateAndTimeConversions.utilDateToString(rs.getDate((SMTableicpoheaders.TableName + "." + SMTableicpoheaders.datexpecteddate)), "M/d/yyyy") + "</FONT></TD>"); 
						out.println("<TD ALIGN=CENTER VALIGN=TOP><FONT SIZE=2>" 
							+ "										<A HREF=\"" 
							+ SMUtilities.getURLLinkBase(context) 
							+ "smic.ICEditPOEdit?lid=" 
							+ Long.toString(rs.getLong((SMTableicpoheaders.TableName + "." + SMTableicpoheaders.lid))) 
							+ "\">" 
							+Long.toString(rs.getLong((SMTableicpoheaders.TableName + "." + SMTableicpoheaders.lid)))
							+ "</A></FONT></TD>"); 
						out.println("</TR>");
					}
					rs.close();

				}catch (SQLException e){
					throw new Exception("Error print only purchase orders - " + e.getMessage());
				}
				if (!bHasDetail){
					out.println("<TR><TD ALIGN=CENTER COLSPAN=9>&nbsp;</TD></TR>");
					out.println("<TR><TD ALIGN=CENTER COLSPAN=9><FONT SIZE=3><B>No Critical Date Information to Display</B></FONT></TD></TR>");
				}
				out.println("</TABLE>");
	}
*/
/*
	private void printOnlySalesLeads(PrintWriter out, String sSQL, Connection conn, ServletContext context) throws Exception {
		//printout column header
				out.println("<TABLE WIDTH=100% cellspacing=0 cellpadding=1>");
				out.println("<TR>");
				//out.println("<TD ALIGN=CENTER bordercolor=\"000\" style=\"border: 1px solid\" VALIGN=TOP WIDTH=5%><FONT SIZE=2><B>ID</B></FONT></TD>");
				//out.println("<TD ALIGN=CENTER bordercolor=\"000\" style=\"border: 1px solid\" VALIGN=TOP WIDTH=3%><FONT SIZE=2><B>SP#</B></FONT></TD>");
				out.println("<TD ALIGN=CENTER bordercolor=\"000\" style=\"border: 1px solid\" VALIGN=TOP WIDTH=3%><FONT SIZE=2><B>Responsible</B></FONT></TD>");
				out.println("<TD ALIGN=CENTER bordercolor=\"000\" style=\"border: 1px solid\" VALIGN=TOP WIDTH=3%><FONT SIZE=2><B>Assigned</B></FONT></TD>");
				out.println("<TD ALIGN=CENTER bordercolor=\"000\" style=\"border: 1px solid\" VALIGN=TOP WIDTH=8%><FONT SIZE=2><B>Critical Date</B></FONT></TD>");
				//out.println("<TD ALIGN=CENTER bordercolor=\"000\" style=\"border: 1px solid\" VALIGN=TOP WIDTH=3%><FONT SIZE=2><B>Type</B></FONT></TD>");
				out.println("<TD ALIGN=LEFT bordercolor=\"000\" style=\"border: 1px solid\" VALIGN=TOP WIDTH=29%><FONT SIZE=2><B>Comment</B></FONT></TD>");
				out.println("<TD ALIGN=LEFT bordercolor=\"000\" style=\"border: 1px solid\" VALIGN=TOP WIDTH=14%><FONT SIZE=2><B>Ship to</B></FONT></TD>");
				out.println("<TD ALIGN=LEFT bordercolor=\"000\" style=\"border: 1px solid\" VALIGN=TOP WIDTH=14%><FONT SIZE=2><B>Bill to Name</B></FONT></TD>");
				out.println("<TD ALIGN=LEFT bordercolor=\"000\" style=\"border: 1px solid\" VALIGN=TOP WIDTH=10%><FONT SIZE=2><B>Project Manager</B></FONT></TD>");
				out.println("<TD ALIGN=CENTER bordercolor=\"000\" style=\"border: 1px solid\" VALIGN=TOP WIDTH=9%><FONT SIZE=2><B>Phone Number</B></FONT></TD>");
				out.println("<TD ALIGN=CENTER bordercolor=\"000\" style=\"border: 1px solid\" VALIGN=TOP WIDTH=7%><FONT SIZE=2><B>Sales Lead </B></FONT></TD>");
				out.println("</TR>");
				boolean bHasDetail = false;    //2

				try{

					ResultSet rs = clsDatabaseFunctions.openResultSet(sSQL, conn);
					while(rs.next()){
						bHasDetail = true;

						out.println("<TR>");
						//out.println("<TD ALIGN=CENTER VALIGN=TOP><FONT SIZE=2>"  + "</FONT></TD>");
						out.println("<TD ALIGN=CENTER VALIGN=TOP nowrap><FONT SIZE=2>" + rs.getString((SMTablecriticaldates.sresponsibleuserfullname).replace("`", "")) + "</FONT></TD>");
						out.println("<TD ALIGN=CENTER VALIGN=TOP nowrap><FONT SIZE=2>" + rs.getString((SMTablecriticaldates.sassignedbyuserfullname).replace("`", "")) + "</FONT></TD>");
						out.println("<TD ALIGN=CENTER VALIGN=TOP><FONT SIZE=2>" + clsDateAndTimeConversions.utilDateToString(rs.getDate((SMTablecriticaldates.TableName + "." + SMTablecriticaldates.sCriticalDate).replace("`", "")),"M/d/yyyy") + "</FONT></TD>");
						out.println("<TD ALIGN=LEFT VALIGN=TOP><FONT SIZE=2>" + rs.getString((SMTablecriticaldates.TableName + "." + SMTablecriticaldates.sComments).replace("`", "")) + "</FONT></TD>");
						out.println("<TD ALIGN=LEFT VALIGN=TOP><FONT SIZE=2>" + rs.getString((SMTablebids.TableName + "." + SMTablebids.scustomername).replace("`", "")) + "</FONT></TD>"); 
						out.println("<TD ALIGN=LEFT VALIGN=TOP><FONT SIZE=2>" + rs.getString((SMTablebids.TableName + "." + SMTablebids.sprojectname).replace("`", "")) + "</FONT></TD>"); 
						out.println("<TD ALIGN=LEFT VALIGN=TOP><FONT SIZE=2>" + rs.getString((SMTablebids.TableName + "." + SMTablebids.scontactname).replace("`", "")) + "</FONT></TD>"); 
						out.println("<TD ALIGN=CENTER VALIGN=TOP><FONT SIZE=2>" + rs.getString((SMTablebids.TableName + "." + SMTablebids.sphonenumber).replace("`", "")) + "</FONT></TD>"); 
						out.println("<TD ALIGN=CENTER VALIGN=TOP><FONT SIZE=2>" 
							+ "										<A HREF=\"" 
							+ SMUtilities.getURLLinkBase(context) 
							+ "smcontrolpanel.SMEditBidEntry?lid=" 
							+ Integer.toString(rs.getInt((SMTablebids.TableName + "." 
							+ SMTablebids.lid).replace("`", ""))) 
							+ "\">" 
							+ Integer.toString(rs.getInt((SMTablebids.TableName + "." 
							+ SMTablebids.lid).replace("`", "")))  
							+ "</A></FONT></TD>"); //clicking on sales lead to edit lead
						out.println("</TR>");
					}
					rs.close();

				}catch (SQLException e){
					throw new Exception("Error print only sales leads - " + e.getMessage());
				}
				if (!bHasDetail){
					out.println("<TR><TD ALIGN=CENTER COLSPAN=9>&nbsp;</TD></TR>");
					out.println("<TR><TD ALIGN=CENTER COLSPAN=9><FONT SIZE=3><B>No Critical Date Information to Display</B></FONT></TD></TR>");
				}
				out.println("</TABLE>");
	}
*/	
	private void printMultipleTypes(PrintWriter out, String sSQL, Connection conn, ServletContext context, String sDBID) throws Exception {
		//printout column header
				out.println("<TABLE WIDTH = 100% CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_BASIC_WITH_BORDER_COLLAPSE + "\" >\n");
				out.println("<TR CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_HEADING + "\">\n ");
				//out.println("<TD ALIGN=CENTER bordercolor=\"000\" style=\"border: 1px solid\" VALIGN=TOP WIDTH=5%><FONT SIZE=2><B>ID</B></FONT></TD>");
				out.println("<TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL + "\"><B>Type</B></TD>");
				out.println("<TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL + "\"><B>Responsible</B></TD>");
				out.println("<TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_CENTER_JUSTIFIED_ARIAL_SMALL + "\"><B>Assigned</B></TD>");
				out.println("<TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_CENTER_JUSTIFIED_ARIAL_SMALL + "\"><B>ID</B></TD>");
				out.println("<TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_CENTER_JUSTIFIED_ARIAL_SMALL + "\"><B>Date</B></TD>");
				out.println("<TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL + "\"><B>Comment</B></TD>");
				out.println("<TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL + "\"><B>Other Information</B></TD>");
				out.println("</TR>");
				boolean bHasDetail = false;//2
				out.println(SMUtilities.getMasterStyleSheetLink());
				 int iCritCount = 0;
				try{

					ResultSet rs = clsDatabaseFunctions.openResultSet(sSQL, conn);
					while(rs.next()){
						bHasDetail = true;
						iCritCount++;
						if(iCritCount%2 == 0) {
							out.println("<TR CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_ROW_EVEN + "\">");
						}else {
							out.println("<TR CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_ROW_ODD + "\">");
						}

						out.println("<TD CLASS = \" " + SMMasterStyleSheetDefinitions.TABLE_CELL_CENTER_JUSTIFIED_ARIAL_SMALL_WO_BORDER_ALIGN_TOP + " \"> "  + SMTablecriticaldates.getTypeDescriptions(rs.getInt(SMTablecriticaldates.itype)) + "</TD>");
						out.println("<TD CLASS = \" " + SMMasterStyleSheetDefinitions.TABLE_CELL_CENTER_JUSTIFIED_ARIAL_SMALL_WO_BORDER_ALIGN_TOP + " \"> " + rs.getString((SMTablecriticaldates.sresponsibleuserfullname).replace("`", "")) + "</TD>");
						out.println("<TD CLASS = \" " + SMMasterStyleSheetDefinitions.TABLE_CELL_CENTER_JUSTIFIED_ARIAL_SMALL_WO_BORDER_ALIGN_TOP + " \"> " + rs.getString((SMTablecriticaldates.sassignedbyuserfullname).replace("`", "")) + "</TD>");


						out.println("<TD CLASS = \" " + SMMasterStyleSheetDefinitions.TABLE_CELL_CENTER_JUSTIFIED_ARIAL_SMALL_WO_BORDER_ALIGN_TOP + " \"><A HREF=\"" 
								+ SMUtilities.getURLLinkBase(context) 
								+ "smcontrolpanel.SMCriticalDateEdit?" + SMTablecriticaldates.sId + "=" 
								+ rs.getString((SMTablecriticaldates.sId).replace("`", ""))
								+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
								+ "#CriticalDatesFooter\">" 
								+ rs.getString((SMTablecriticaldates.sId).replace("`", ""))
							+ "</A>" + "</TD>");
						out.println("<TD CLASS = \" " + SMMasterStyleSheetDefinitions.TABLE_CELL_CENTER_JUSTIFIED_ARIAL_SMALL_WO_BORDER_ALIGN_TOP + " \">" + clsDateAndTimeConversions.utilDateToString(rs.getDate((SMTablecriticaldates.TableName + "." + SMTablecriticaldates.sCriticalDate).replace("`", "")),"M/d/yyyy") + "</TD>");
						out.println("<TD CLASS = \" " + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_ALIGN_TOP + " \">" + rs.getString((SMTablecriticaldates.TableName + "." + SMTablecriticaldates.sComments).replace("`", "")) + "</TD>");
						
						out.println("<TD NOWRAP CLASS = \" " + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_ALIGN_TOP+" \">");
						
						if( rs.getInt(SMTablecriticaldates.TableName + "." + SMTablecriticaldates.itype) == SMTablecriticaldates.SALES_ORDER_RECORD_TYPE) { 
							out.println("<b>Salesperson Name: </b>" + rs.getString((SMTablesalesperson.TableName + "." + SMTablesalesperson.sSalespersonFirstName).replace("`", "")).trim() + " " +rs.getString((SMTablesalesperson.TableName + "." + SMTablesalesperson.sSalespersonLastName).replace("`", "")).trim() + "");
							out.println("<br><b>Bill To Name: </b>" + rs.getString((SMTableorderheaders.TableName + "." + SMTableorderheaders.sBillToName).replace("`", "")).trim() + "");
							out.println("<br><b>Ship To Name: </b>" + rs.getString((SMTableorderheaders.TableName + "." + SMTableorderheaders.sShipToName).replace("`", "")).trim() + "");
							out.println("<br><b>Contact: </b>" + rs.getString((SMTableorderheaders.TableName + "." + SMTableorderheaders.sBillToContact).replace("`", "")).trim() + ""); 
							out.println("<br><b>Phone: </b>" + rs.getString((SMTableorderheaders.TableName + "." + SMTableorderheaders.sBillToPhone).replace("`", "")).trim() + ""); 
							out.println("<br><b>Order Number:</b>" + "<A HREF=\"" 
									+ SMUtilities.getURLLinkBase(context) 
									+ "smcontrolpanel.SMDisplayOrderInformation?OrderNumber=" 
									+ rs.getString((SMTableorderheaders.TableName + "." 
									+ SMTableorderheaders.sOrderNumber).replace("`", "")) 
									+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
									+ "#CriticalDatesFooter\">" 
									+ rs.getString((SMTableorderheaders.TableName + "." 
									+ SMTableorderheaders.sOrderNumber).replace("`", "")) 
								+ "</A>"); 
						}
						
						if( rs.getInt(SMTablecriticaldates.TableName + "." + SMTablecriticaldates.itype) == SMTablecriticaldates.SALES_CONTACT_RECORD_TYPE) {
							out.println("<b>Customer Account: </b>" + "<A HREF=\"" 
									+ SMUtilities.getURLLinkBase(context) 
									+ "smar.ARDisplayCustomerInformation?CustomerNumber=" 
									+ rs.getString((SMTablesalescontacts.TableName + "." + SMTablesalescontacts.scustomernumber).replace("&", "%26")).trim() 
									+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
									+ "\">" 
									+ rs.getString((SMTablesalescontacts.TableName + "." + SMTablesalescontacts.scustomernumber).replace("`", "")).trim()  
									+ "</A>");
							out.println("<br><b>Customer Name: </b>" + rs.getString((SMTablesalescontacts.TableName + "." + SMTablesalescontacts.scustomername).replace("`", "")).trim() + ""); 
							out.println("<br><b>Contact Name: </b>" + rs.getString((SMTablesalescontacts.TableName + "." + SMTablesalescontacts.scontactname).replace("`", "")).trim() + ""); 
							out.println("<br><b>Phone: </b>" + rs.getString((SMTablesalescontacts.TableName + "." + SMTablesalescontacts.sphonenumber).replace("`", "")).trim() + ""); 
							out.println("<br><b>Last Invoice Date: </b>" + clsDateAndTimeConversions.resultsetDateStringToString(rs.getString("LASTINVOICEDATE")) + ""); 
							out.println("<br><b>Sales Contact ID: </b>" + "<A HREF=\"" 
								+ SMUtilities.getURLLinkBase(context) 
								+ "smcontrolpanel.SMSalesContactEdit?id=" 
								+ Integer.toString(rs.getInt((SMTablesalescontacts.TableName + "." 
								+ SMTablebids.lid).replace("`", ""))) 
								+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
								+ "\">" 
								+ Integer.toString(rs.getInt((SMTablesalescontacts.TableName + "." 
								+ SMTablebids.lid).replace("`", "")))  
								+ "</A>");	
						}
						
						if( rs.getInt(SMTablecriticaldates.TableName + "." + SMTablecriticaldates.itype) == SMTablecriticaldates.SALES_LEAD_RECORD_TYPE) {
							out.println("<b>Ship To Name: </b>" + rs.getString((SMTablebids.TableName + "." + SMTablebids.scustomername).replace("`", "")).trim() + ""); 
							out.println("<br><b>Bill To Name: </b>" + rs.getString((SMTablebids.TableName + "." + SMTablebids.sprojectname).replace("`", "")).trim() + ""); 
							out.println("<br><b>Contact: </b>" + rs.getString((SMTablebids.TableName + "." + SMTablebids.scontactname).replace("`", "")).trim() + ""); 
							out.println("<br><b>Phone: </b>" + rs.getString((SMTablebids.TableName + "." + SMTablebids.sphonenumber).replace("`", "")).trim() + ""); 
							out.println("<br><b>Sales Lead: </b>" + "<A HREF=\"" 
								+ SMUtilities.getURLLinkBase(context) 
								+ "smcontrolpanel.SMEditBidEntry?lid=" 
								+ Integer.toString(rs.getInt((SMTablebids.TableName + "." 
								+ SMTablebids.lid).replace("`", ""))) 
								+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
								+ "\">" 
								+ Integer.toString(rs.getInt((SMTablebids.TableName + "." 
								+ SMTablebids.lid).replace("`", "")))  
								+ "</A>");	
						}
						
						if( rs.getInt(SMTablecriticaldates.TableName + "." + SMTablecriticaldates.itype) == SMTablecriticaldates.PURCHASE_ORDER_RECORD_TYPE) {
							out.println("<b>Vendor Name: </b> " + rs.getString((SMTableicpoheaders.TableName + "." + SMTableicpoheaders.svendorname).replace("`", "")).trim() + ""); 
							out.println("<br><b>Vendor Number: </b> " + rs.getString((SMTableicpoheaders.TableName + "." + SMTableicpoheaders.svendor).replace("`", "")).trim() + ""); 
							out.println("<br><b>Status: </b>" + SMTableicpoheaders.getStatusDescription(rs.getInt((SMTableicpoheaders.TableName + "." + SMTableicpoheaders.lstatus))) + ""); 
							out.println("<br><b>Expected: </b>" + clsDateAndTimeConversions.resultsetDateStringToString(rs.getString(SMTableicpoheaders.datexpecteddate)) + ""); 
							out.println("<br><b>PO Number: </b>" + "<A HREF=\"" 
								+ SMUtilities.getURLLinkBase(context) 
								+ "smic.ICEditPOEdit?lid=" 
								+ Long.toString(rs.getLong((SMTableicpoheaders.TableName + "." + SMTableicpoheaders.lid))) 
								+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
								+ "#CriticalDatesFooter\">" 
								+ Long.toString(rs.getLong((SMTableicpoheaders.TableName + "." + SMTableicpoheaders.lid))) 
								+ "</A>");	
						}
						
						out.println("</TD></TR>");
					}
					rs.close();

				}catch (SQLException e){
					throw new Exception("Error printinting mutiple types - " + e.getMessage());
				}
				if (!bHasDetail){
					out.println("<TR><TD ALIGN=CENTER COLSPAN=9>&nbsp;</TD></TR>");
					out.println("<TR><TD ALIGN=CENTER COLSPAN=9><FONT SIZE=3><B>No Critical Date Information to Display</B></FONT></TD></TR>");
				}
				  out.println("<TR  CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_TOTAL + "\" ><TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL +"\" COLSPAN=7 ><B>Total  " + SMCriticalDateEntry.ParamObjectName + "s: " + iCritCount + "</B></TD></TR>");
				out.println("</TABLE>\n");

	}
	
	public String getErrorMessage (){
		return m_sErrorMessage;
	}
	
	private String Get_Critical_Date_Report_SQL(String sStartingDate, 
			String sEndingDate, 
			ArrayList<String> alSelectedUsers,
			ArrayList <String> alTypes,
			ArrayList <String> alStatus,
			ArrayList <String> arrSalesGroupCodes,
			String sAssignedBy,
			String sOrderBy){

		String SQL = "SELECT" +
		" " + SMTablecriticaldates.TableName + "." + SMTablecriticaldates.sId + "," +
		" " + SMTablecriticaldates.TableName + "." + SMTablecriticaldates.sCriticalDate + "," +
		" " + SMTablecriticaldates.TableName + "." + SMTablecriticaldates.lresponsibleuserid + "," +
		" " + SMTablecriticaldates.TableName + "." + SMTablecriticaldates.lassignedbyuserid + "," +
		" " + SMTablecriticaldates.TableName + "." + SMTablecriticaldates.sResolvedFlag + "," +
		" " + SMTablecriticaldates.TableName + "." + SMTablecriticaldates.sComments + "," + 
		" " + SMTablecriticaldates.TableName + "." + SMTablecriticaldates.sCriticalDate + "," + //remember to trim this guy
		" " + SMTablecriticaldates.TableName + "." + SMTablecriticaldates.itype//process this later in code, CD or PN(everything else)
	    + ", IF(TRIM(" + SMTablecriticaldates.TableName + "." + SMTablecriticaldates.sresponsibleuserfullname + ") = '', "
		+ "'" + "(NOT FOUND)" + "', " 
		+ SMTablecriticaldates.TableName + "." + SMTablecriticaldates.sresponsibleuserfullname
		+ ") AS " + SMTablecriticaldates.sresponsibleuserfullname	
		+ ", IF(TRIM(" + SMTablecriticaldates.TableName + "." + SMTablecriticaldates.sassignedbyuserfullname + ") = '', "
		+ "'" + "(NOT FOUND)" + "', " 
		+ SMTablecriticaldates.TableName + "." + SMTablecriticaldates.sassignedbyuserfullname
		+ ") AS " + SMTablecriticaldates.sassignedbyuserfullname;

		for (int i = 0; i < alTypes.size(); i++){
			if (Integer.parseInt(alTypes.get(i)) == SMTablecriticaldates.SALES_ORDER_RECORD_TYPE) {
					SQL += ", " + SMTableorderheaders.TableName + "." + SMTableorderheaders.sBillToName  
						+ ", " + SMTableorderheaders.TableName + "." + SMTableorderheaders.sShipToName 
						+ ", " + SMTableorderheaders.TableName + "." + SMTableorderheaders.sBillToContact
						+ ", " + SMTableorderheaders.TableName + "." + SMTableorderheaders.sShipToContact
						+ ", " + SMTableorderheaders.TableName + "." + SMTableorderheaders.sBillToPhone 
						+ ", " + SMTableorderheaders.TableName + "." + SMTableorderheaders.sShipToPhone
						+ ", " + SMTableorderheaders.TableName + "." + SMTableorderheaders.sOrderNumber
						+ ", " + SMTableorderheaders.TableName + "." + SMTableorderheaders.sSalesperson
						+ ", " + SMTablesalesperson.TableName + "." + SMTablesalesperson.sSalespersonFirstName
						+ ", " + SMTablesalesperson.TableName + "." + SMTablesalesperson.sSalespersonLastName;
			}
			if (Integer.parseInt(alTypes.get(i)) == SMTablecriticaldates.SALES_CONTACT_RECORD_TYPE) {
				SQL += ", " + SMTablesalescontacts.TableName + "." + SMTablesalescontacts.scontactname
					+ ", " + SMTablesalescontacts.TableName + "." + SMTablesalescontacts.sphonenumber  
					+ ", " + SMTablesalescontacts.TableName + "." + SMTablesalescontacts.id  
					+ ", " + SMTablesalescontacts.TableName + "." + SMTablesalescontacts.scustomernumber
					+ ", MAXINVOICEDATETABLE.MAXDATE AS LASTINVOICEDATE"
					+ ", " + SMTablesalescontacts.TableName + "." + SMTablesalescontacts.scustomername;
			}	
			
			if (Integer.parseInt(alTypes.get(i)) == SMTablecriticaldates.SALES_LEAD_RECORD_TYPE) {
					SQL += ", " + SMTablebids.TableName + "." + SMTablebids.scontactname
						+ ", " + SMTablebids.TableName + "." + SMTablebids.sphonenumber  
						+ ", " + SMTablebids.TableName + "." + SMTablebids.lid  
						+ ", " + SMTablebids.TableName + "." + SMTablebids.sprojectname
						+ ", " + SMTablebids.TableName + "." + SMTablebids.scustomername;
				}	
			
			if (Integer.parseInt(alTypes.get(i)) == SMTablecriticaldates.PURCHASE_ORDER_RECORD_TYPE) {
				SQL += ", " + SMTableicpoheaders.TableName + "." + SMTableicpoheaders.svendor
					+ ", " + SMTableicpoheaders.TableName + "." + SMTableicpoheaders.lid
					+ ", " + SMTableicpoheaders.TableName + "." + SMTableicpoheaders.svendorname
					+ ", " + SMTableicpoheaders.TableName + "." + SMTableicpoheaders.datexpecteddate
					+ ", " + SMTableicpoheaders.TableName + "." + SMTableicpoheaders.lstatus;
			}
		}

		SQL += " FROM "+ SMTablecriticaldates.TableName;
		
		for (int i = 0; i < alTypes.size(); i++){
			if (Integer.parseInt(alTypes.get(i)) ==  SMTablecriticaldates.SALES_ORDER_RECORD_TYPE) {
					SQL += " LEFT JOIN " + SMTableorderheaders.TableName 
					+ " ON "  + SMTablecriticaldates.TableName + "." + SMTablecriticaldates.sdocnumber + " = " 
					+ SMTableorderheaders.TableName + "." + SMTableorderheaders.strimmedordernumber
					+ " LEFT JOIN " + SMTablesalesgroups.TableName
					+ " ON " + SMTableorderheaders.TableName + "." + SMTableorderheaders.iSalesGroup
					+ " = " + SMTablesalesgroups.iSalesGroupId
					+ " LEFT JOIN " + SMTablesalesperson.TableName
					+ " ON " + SMTableorderheaders.TableName + "." + SMTableorderheaders.sSalesperson
					+ " = " + SMTablesalesperson.sSalespersonCode
					;
			}	
			if (Integer.parseInt(alTypes.get(i)) == SMTablecriticaldates.SALES_CONTACT_RECORD_TYPE) {
				SQL += " LEFT JOIN " + SMTablesalescontacts.TableName
				+ " ON "  + SMTablecriticaldates.TableName + "." + SMTablecriticaldates.sdocnumber + " = " 
				+ "" + SMTablesalescontacts.TableName + "." + SMTablesalescontacts.id + ""
					+ " LEFT JOIN "
					+ "( SELECT MAX(" + SMTableartransactions.TableName + "." + SMTableartransactions.datdocdate + ") as MAXDATE,"
						+ SMTableartransactions.TableName + "." + SMTableartransactions.spayeepayor + " as CUSTOMER"
						+ " FROM " + SMTableartransactions.TableName 
						+ " WHERE " + SMTableartransactions.TableName + "." +SMTableartransactions.idoctype + " = 0"
						+ " GROUP BY " + SMTableartransactions.TableName + "." + SMTableartransactions.spayeepayor + ") as MAXINVOICEDATETABLE"
				+ " ON MAXINVOICEDATETABLE.CUSTOMER = " + SMTablesalescontacts.TableName + "." + SMTablesalescontacts.scustomernumber;
		}
			if (Integer.parseInt(alTypes.get(i)) == SMTablecriticaldates.SALES_LEAD_RECORD_TYPE) {
					SQL += " LEFT JOIN " + SMTablebids.TableName
					+ " ON "  + SMTablecriticaldates.TableName + "." + SMTablecriticaldates.sdocnumber + " = " 
					+ "CAST(" + SMTablebids.TableName + "." + SMTablebids.lid + " as char(11))";
			}
			if (Integer.parseInt(alTypes.get(i)) == SMTablecriticaldates.PURCHASE_ORDER_RECORD_TYPE) {
				SQL += " LEFT JOIN " + SMTableicpoheaders.TableName
				+ " ON "  + SMTablecriticaldates.TableName + "." + SMTablecriticaldates.sdocnumber + " = " 
				+ "CAST(" + SMTableicpoheaders.TableName + "." + SMTableicpoheaders.lid + " as char(11))";
		}
		}				
		SQL += " WHERE ("
		//starting date
		+ "(" + SMTablecriticaldates.TableName + "." + SMTablecriticaldates.sCriticalDate + " >= '" 
			+ sStartingDate + " 00:00:00')"
		//ending date
		+ " AND (" + SMTablecriticaldates.TableName + "." + SMTablecriticaldates.sCriticalDate + " <= '" 
			+ sEndingDate + " 23:59:59')";
			
		if(alStatus.size() == 1) {
			SQL += " AND (" + SMTablecriticaldates.TableName + "." + SMTablecriticaldates.sResolvedFlag + " =" + alStatus.get(0)+ ")";
		}		
		
		SQL += " AND (";
				
		for (int i = 0; i < alTypes.size(); i++){
			if (i != 0){
				SQL += " OR";
			}
				SQL += "(" + SMTablecriticaldates.TableName + "." + SMTablecriticaldates.itype + " = " + alTypes.get(i)+ ")";
		}
		SQL += ")";
		
		SQL += " AND (";		
		if (alSelectedUsers.size() > 0){
			SQL += " (";
			for (int i = 0; i < alSelectedUsers.size(); i++){
				if (i != 0){
					SQL += " OR";
				}
				SQL += " " + SMTablecriticaldates.TableName + "." + SMTablecriticaldates.lresponsibleuserid 
				+ " = "+ alSelectedUsers.get(i) + "";
			}
			SQL += ")";
		}else{
			//show nothing
			SQL += " 1 = 0 ";
		}
		SQL += ")"; // end the last 'AND'
		
		if (sAssignedBy.compareToIgnoreCase("") != 0){
			SQL += " AND (" + SMTablecriticaldates.TableName + "." + SMTablecriticaldates.lassignedbyuserid + " = "
			+ sAssignedBy + ")";
		}
		
		//If the user chose to list critical dates for ORDERS, then qualify the SQL statement using the selected sales groups:
		for (int i = 0; i < alTypes.size(); i++){
			if (Integer.parseInt(alTypes.get(i)) == SMTablecriticaldates.SALES_ORDER_RECORD_TYPE) {
				SQL += " AND ("
					//We have to add this because if the user selects orders AND purchase orders, for example, then any purchase order records won't have a sales group code - it will be null instead:
					+ " (" +  SMTablesalesgroups.TableName + "." + SMTablesalesgroups.sSalesGroupCode + " IS NULL)";
				for (int j = 0; j < arrSalesGroupCodes.size(); j++){
					SQL += " OR (" + SMTablesalesgroups.TableName + "." + SMTablesalesgroups.sSalesGroupCode + " = '" + arrSalesGroupCodes.get(j) + "')";
				}
				SQL += ")";  //End the 'AND' clause
				break;
			}
		}
		
		SQL += ")"; //End the 'WHERE' clause
		
		SQL += " ORDER BY ";
		if (sOrderBy.compareToIgnoreCase(SMCriticalDateReportCriteriaSelection.ParamSortByDate) == 0){
			SQL += SMTablecriticaldates.TableName + "." + SMTablecriticaldates.sresponsibleuserfullname  + ", " +
					SMTablecriticaldates.TableName + "." + SMTablecriticaldates.sCriticalDate + ", " +			
					SMTablecriticaldates.TableName + "." + SMTablecriticaldates.itype;
		}else if (sOrderBy.compareToIgnoreCase(SMCriticalDateReportCriteriaSelection.ParamSortByType) == 0){
			SQL += SMTablecriticaldates.TableName + "." + SMTablecriticaldates.sresponsibleuserfullname + ", " + 
					SMTablecriticaldates.TableName + "." + SMTablecriticaldates.itype + ", " +
					SMTablecriticaldates.TableName + "." + SMTablecriticaldates.sCriticalDate ;
					
		}
		
		//System.out.println("[1552014955] - SQL = '" + SQL + "'");
		
		if (bDebugMode){
			System.out.println("In " + this.toString() + " SQL: " + SQL);
		}
		return SQL;

	}
}
