package smic;

//import SMDataDefinition.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import smcontrolpanel.SMAuthenticate;
import smcontrolpanel.SMSystemFunctions;
import smcontrolpanel.SMUtilities;
import ConnectionPool.WebContextParameters;
import SMClasses.SMBatchStatuses;
import SMClasses.SMModuleTypes;
import SMDataDefinition.SMTableicbatchentries;
import ServletUtilities.clsServletUtilities;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsDateAndTimeConversions;
import ServletUtilities.clsManageBigDecimals;
import ServletUtilities.clsStringFunctions;

public class ICEditBatches extends HttpServlet {

	public static final String CREATE_PO_INVOICE_BATCH_LABEL = "Create AP batch from PO invoices -->";
	public static final String CREATE_PO_INVOICE_BATCH_BUTTON = "CREATEBATCHFROMINVOICES";
	public static final String PARAM_CREATE_PO_INVOICE_BATCH_DATE = "CREATEINVOICEBATCHDATE";
	
	private static final long serialVersionUID = 1L;
	/*
	 * Parameters:
	 * Limit - e.g. Limit=25 - number of batches to list
	 */

	private static final String sBatchObjectName = "Batch";
	
	public void doPost(HttpServletRequest request,
				HttpServletResponse response)
				throws ServletException, IOException {
		
		if (!SMAuthenticate.authenticateSMCPCredentials(
				request, 
				response, 
				getServletContext(), 
				SMSystemFunctions.ICEditBatches)){
			return;
		}
		PrintWriter out = response.getWriter();
	    //Get the session info:
	    HttpSession CurrentSession = request.getSession(true);
	    String sDBID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_DATABASE_ID);
	    String sUserID = (String)CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERID);
	    String sUserFullName = (String)CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERFIRSTNAME) + " "
	    				+ (String)CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERLASTNAME);
	    String sCompanyName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_COMPANYNAME);
	    
	    //Get the variables for the class:
	    String sNumberOfBatchesToDisplay;
	    if (request.getParameter("Limit") == null){
	    	sNumberOfBatchesToDisplay = "0";
	    }else{
	    	sNumberOfBatchesToDisplay = request.getParameter("Limit");
	    }

	    String title = "Edit " + sBatchObjectName + "es.";
	    String subtitle = "";
	    out.println(SMUtilities.SMCPTitleSubBGColor(title, subtitle, SMUtilities.getInitBackGroundColor(getServletContext(), sDBID), sCompanyName));
	    out.println(SMUtilities.getJQueryIncludeString());
	    out.println(SMUtilities.getDatePickerIncludeString(getServletContext()));
	    //Display any warnings:
	    if (request.getParameter("Warning") != null){
	    	String sWarning = request.getParameter("Warning");
	    	if (!sWarning.equalsIgnoreCase("")){
	    		out.println("<B><FONT COLOR=\"RED\">" + sWarning + "</FONT></B><BR>");
	    	}
	    }
	    if (request.getParameter("Status") != null){
	    	String sStatus = request.getParameter("Status");
	    	if (!sStatus.equalsIgnoreCase("")){
	    		out.println("<B>" + sStatus + "</B><BR>");
	    	}
	    }
	    
	    //Print a link to the first page after login:
	    out.println("<BR><A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMUserLogin?" 
				+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
				+ "\">Return to user login</A><BR>");
	    out.println("<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smic.ICMainMenu?" 
				+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
				+ "\">Return to Inventory Main Menu</A><BR>");
	    out.println("<A HREF=\"" + WebContextParameters.getdocumentationpageURL(getServletContext()) + "#" + Long.toString(SMSystemFunctions.ICEditBatches) 
	    		+ "\">Summary</A><BR><BR>");
	    
	    //New Table for Unposted PO Invoices and Receipts
	    out.println("<TABLE BORDER=1 CELLSPACING=2 style=\"font-size:85%\">");
	    out.println("<TR>");
	    
	    out.println("<TD>");
	    out.println("<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) 
	    		+ "smic.ICPOUnpostedInvoiceGenerate?" 
				+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID + "\">View Unposted PO Invoices</A><BR>");
	    out.println("</TD>");
	    
	    out.println("<TD>");
	    out.println("<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) 
	    		+ "smic.ICPOUnpostedReceiptsGenerate?" 
				+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID + "\">View Unposted PO Receipts</A><BR>");
	    out.println("</TD>");
	    
	    out.println("</TR>");
	    out.println("</TABLE>");
	    
	    out.println("<BR>");
	    
	    //Add links to create new batches:
	    out.println("<TABLE BORDER=1 CELLSPACING=2 style=\"font-size:75%\">");
	    out.println("<TR>");
	    out.println("<TD>");
	    out.println("<A HREF=\""+ SMUtilities.getURLLinkBase(getServletContext()) + "smic.ICEditBatchesEdit?BatchNumber=-1&BatchType=" 
	    		+ Integer.toString(ICBatchTypes.IC_SHIPMENT) 
	    		+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
	    		+"\">New shipment batch</A>");
	    out.println("</TD>");
	    out.println("<TD>");
	    out.println("<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smic.ICEditBatchesEdit?BatchNumber=-1&BatchType="
	    		+ Integer.toString(ICBatchTypes.IC_RECEIPT) 
	    		+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
	    		+ "\">New receipt batch</A>");
	    out.println("</TD>");
	    
	    out.println("<TD>");
	    out.println("<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smic.ICEditBatchesEdit?BatchNumber=-1&BatchType="
	    		+ Integer.toString(ICBatchTypes.IC_ADJUSTMENT) 
	    		+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
	    		+ "\">New adjustment batch</A>");
	    out.println("</TD>");
	    
	    out.println("<TD>");
	    out.println("<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smic.ICEditBatchesEdit?BatchNumber=-1&BatchType="
	    		+ Integer.toString(ICBatchTypes.IC_TRANSFER) 
	    		+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
	    		+ "\">New transfer batch</A>");
	    out.println("</TD>");
	    
	    out.println("<TD>");
	    out.println("<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smic.ICEditBatchesEdit?BatchNumber=-1&BatchType="
	    		+ Integer.toString(ICBatchTypes.IC_PHYSICALCOUNT) 
	    		+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
	    		+ "\">New physical count batch</A>");
	    out.println("</TD>");
	    
	    out.println("<TD>");
	    out.println("<A HREF=\""+ SMUtilities.getURLLinkBase(getServletContext()) 
	    		+ "smic.ICCreateSMInvoiceBatch?" 
	    		+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID+"\" >Import Service Manager invoice batch</A>");
	    out.println("</TD>");

	    out.println("<TD>");
	    out.println("<A HREF=\""+ SMUtilities.getURLLinkBase(getServletContext()) 
	    		+ "smic.ICCreateBatchFromReceipts?" 
	    		+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID+"\" >Create batch from receipts</A>");
	    out.println("</TD>");
	    
	    out.println("</TR>");
	    out.println("</TABLE>");
	    
	    //Create a table for the edit buttons:
	    ServletUtilities.clsDBServerTime clsServerTime = null;
		try {
			clsServerTime = new ServletUtilities.clsDBServerTime(sDBID, sUserFullName,  getServletContext());
		} catch (Exception e) {
			out.println("<BR><FONT COLOR=RED>Error [1554394239] getting current date - " + e.getMessage() + "<BR>");
		}
		
	    out.println("<TABLE BORDER=0 CELLSPACING=2 style=\"font-size:normal; \">" + "\n");
	    
	    //Create invoice batch:
	    out.println("<FORM NAME='CREATEPOINVOICEBATCHFORM' ACTION='" + SMUtilities.getURLLinkBase(getServletContext()) + "smic.ICCreateBatchFromInvoices' METHOD='POST'>\n");
	    out.println("<INPUT TYPE=HIDDEN NAME='" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "' VALUE='" + sDBID + "'>\n");
	    out.println("  <TR>\n");
	    
	    out.println("    <TD>"
	    	+ "<INPUT TYPE=SUBMIT"
	    	+ " NAME = \"" + CREATE_PO_INVOICE_BATCH_BUTTON + "\""
	    	+ " VALUE = \"" + CREATE_PO_INVOICE_BATCH_LABEL + "\""
	    	+ ">"
	    	+ "    </TD>\n"
	    	+ "    <TD>"
	    	+ " using AP batch date:&nbsp;"
	    	+ " <INPUT TYPE=TEXT NAME=\"" + PARAM_CREATE_PO_INVOICE_BATCH_DATE + "\""
			+ " VALUE=\"" + clsServerTime.getCurrentDateTimeInSelectedFormat(clsServletUtilities.DATE_FORMAT_FOR_DISPLAY) + "\""
			+ " SIZE=28"
			+ " MAXLENGTH=10"
			+ " STYLE=\"width: .75in; height: 0.25in\""
			+ ">"
	    	+ SMUtilities.getDatePickerString(PARAM_CREATE_PO_INVOICE_BATCH_DATE, getServletContext())
	    	+ "    </TD>\n"
         );
	    out.println("  </TR>\n");
	    out.println("</FORM>\n");
	    
	    out.println("</TABLE>\n\n");
	    
	    out.println(javaScript());
	    
	    out.println("<FONT SIZE=2><B>NOTE:</B>&nbsp;Background color indicates batch status - "
		    	+ "GREEN = Entered, BLUE = Imported, RED = Deleted, WHITE = Posted"
		    	+ "</FONT>"
		    );
	    
	    //Build List
	    out.println(sStyleScripts());
	    out.println("<TABLE class = \" batchlist \" >");
	    
	    //Headings:
	    out.println("<TR>");
	    out.println("<TH class=\"headingleft\" >Batch #</TH>");
	    out.println("<TH class=\"headingleft\" >Date</TH>");
	    out.println("<TH class=\"headingleft\" >Type</TH>");
	    out.println("<TH class=\"headingleft\" >Status</TH>");
	    out.println("<TH class=\"headingright\" >Entries</TH>");
	    out.println("<TH class=\"headingright\" >Net Cost Total</TH>");
	    out.println("<TH class=\"headingleft\" >Created by</TH>");
	    out.println("<TH class=\"headingleft\" >Description</TH>");
	    out.println("<TH class=\"headingleft\" >Last edited</TH>");
	    out.println("<TH class=\"headingleft\" >Posted</TH>");
	    out.println("</TR>");

	    String SQL = "";
	    try{
	    	SQL = "SELECT " 
			+ ICEntryBatch.TableName + "." + ICEntryBatch.lbatchnumber + ", "
			+ ICEntryBatch.datbatchdate + ", "
			+ ICEntryBatch.ibatchstatus + ", "
			+ ICEntryBatch.ibatchtype + ", "
			+ ICEntryBatch.sbatchdescription + ", "
			+ ICEntryBatch.lbatchlastentry + ", "
			+ ICEntryBatch.datlasteditdate + ", "
			+ ICEntryBatch.datpostdate + ", "
			+ ICEntryBatch.TableName + "." + ICEntryBatch.screatedbyfullname
			
			+ ", SUM(" + 
				SMTableicbatchentries.TableName + "." + SMTableicbatchentries.bdentryamount
				+ ") AS dbatchtotal"
		
			+ " FROM " + ICEntryBatch.TableName + " LEFT JOIN " 
				+ SMTableicbatchentries.TableName + " ON "
				+ ICEntryBatch.TableName + "." + ICEntryBatch.lbatchnumber
				+ " = " + SMTableicbatchentries.TableName + "." + SMTableicbatchentries.lbatchnumber
			+ " WHERE ("
				+ "(" + ICEntryBatch.smoduletype + " = '" + SMModuleTypes.IC + "')"
			+ ")"
	
			+ " GROUP BY " + ICEntryBatch.TableName + "." + ICEntryBatch.lbatchnumber
			
			+ " ORDER BY " 
			+ ICEntryBatch.lbatchnumber + " DESC";
	    	
	    	if (Long.parseLong(sNumberOfBatchesToDisplay) != 0){
	    		SQL = SQL + " LIMIT " + sNumberOfBatchesToDisplay;
	    	}
	    	
	    	//System.out.println("In " + this.toString() + " listing batches SQL = " + SQL);
	        ResultSet rs = clsDatabaseFunctions.openResultSet(
	        	SQL, 
	        	getServletContext(), 
	        	sDBID,
	        	"MySQL",
	        	this.toString() + ".doPost - User: " + sUserID
	        	+ " - "
	        	+ sUserFullName
	        		);
	        
        	while (rs.next()){
        		//Start a row:
        		BigDecimal bdBatchTotal = BigDecimal.ZERO;
        		
        		if (rs.getBigDecimal("dbatchtotal") != null){
        			//Transfers carry a cost, but they net out to zero, since they have a cost in the 'FROM'
        			//location and the opposite amount in the 'TO' location:
        			if (rs.getInt(ICEntryBatch.ibatchtype) == ICEntryTypes.TRANSFER_ENTRY){
        				bdBatchTotal = BigDecimal.ZERO;
        			}else{
        				bdBatchTotal = rs.getBigDecimal("dbatchtotal");
        			}
        		}
        		out.println("<TR>");
        		String sCreatedBy = "";
        		sCreatedBy = "" + rs.getString(ICEntryBatch.TableName + "." + ICEntryBatch.screatedbyfullname) + "";
 
        		out.println(
        			Build_Row(		
	        			out,
	        			Integer.toString(rs.getInt(ICEntryBatch.lbatchnumber)),
	        			clsStringFunctions.PadLeft(Integer.toString(rs.getInt(ICEntryBatch.lbatchnumber)),"0",6),
	        			clsDateAndTimeConversions.TimeStampToString(rs.getTimestamp(ICEntryBatch.datbatchdate), "MM-dd-yyyy"),
	        			rs.getInt(ICEntryBatch.ibatchtype),
	        			ICBatchTypes.Get_Batch_Type(rs.getInt(ICEntryBatch.ibatchtype)),
	        			SMBatchStatuses.Get_Transaction_Status((rs.getInt(ICEntryBatch.ibatchstatus))),
	        			rs.getInt(ICEntryBatch.ibatchstatus),
	        			Long.toString(rs.getLong(ICEntryBatch.lbatchlastentry)),
	        			clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(bdBatchTotal),
	        			sCreatedBy,
	        			rs.getString(ICEntryBatch.sbatchdescription),
	        			rs.getString(ICEntryBatch.datlasteditdate),
	        			rs.getString(ICEntryBatch.datpostdate),
	        			request,
	        			getServletContext(),
	        			sDBID
        			)
        		);
        		//End the row:
        		out.println("</TR>");
        	}
        	rs.close();
	        //out.println ("<BR>");
		}catch (SQLException ex){
			out.println ("<BR><FONT COLOR=RED><B>"
					+ "Error reading batches with SQL: " + clsStringFunctions.filter(SQL + " - " + ex.getMessage())
					+ "</B></FONT>"
			);
		}
		
		out.println("</TABLE>");
		
		//***************************************
		out.println("</BODY></HTML>");
	}

	private static String Build_Row (
			PrintWriter pwout,
			String sBatchNumber,
			String sBatchNumberLabel,
			String sBatchDate,
			int iBatchType,
			String sBatchTypeLabel,
			String sBatchStatus,
			int iBatchStatus,
			String sNoOfLines,
			String sBatchTotal,
			String sCreatedBy, 
			String sDesc,
			String sLastEditedDate,
			String sPostingDate,
			HttpServletRequest req,
			ServletContext context,
			String sDBID
			){

		String sOutPut = "<TD class=\"fieldleftaligned" + SMBatchStatuses.Get_Transaction_Status(iBatchStatus) + "\" >";
		sOutPut += "<A HREF=\"" + SMUtilities.getURLLinkBase(context) + "smic.ICEditBatchesEdit" 
	    		+ "?" + "BatchNumber=" + sBatchNumber
	    		+ "&BatchType=" + Integer.toString(iBatchType)
	    		+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
	    		+ "\">"
	    		+ sBatchNumberLabel
	    		+ "</A>";
		sOutPut += "</TD>";
		
		sOutPut += "<TD class=\"fieldleftaligned" + SMBatchStatuses.Get_Transaction_Status(iBatchStatus) + "\" >";
		sOutPut += sBatchDate;
		sOutPut += "</TD>";

		sOutPut += "<TD class=\"fieldleftaligned" + SMBatchStatuses.Get_Transaction_Status(iBatchStatus) + "\" >";
		sOutPut += sBatchTypeLabel;
		sOutPut += "</TD>";
		
		sOutPut += "<TD class=\"fieldleftaligned" + SMBatchStatuses.Get_Transaction_Status(iBatchStatus) + "\" >";
		sOutPut += sBatchStatus;
		sOutPut += "</TD>";

		sOutPut += "<TD class=\"fieldrightaligned" + SMBatchStatuses.Get_Transaction_Status(iBatchStatus) + "\" >";
		sOutPut += sNoOfLines;
		sOutPut += "</TD>";
		
		sOutPut += "<TD class=\"fieldrightaligned" + SMBatchStatuses.Get_Transaction_Status(iBatchStatus) + "\" >";
		sOutPut += sBatchTotal;
		sOutPut += "</TD>";
		
		sOutPut += "<TD class=\"fieldleftaligned" + SMBatchStatuses.Get_Transaction_Status(iBatchStatus) + "\" >";
		sOutPut += sCreatedBy;
		sOutPut += "</TD>";
		
		sOutPut += "<TD class=\"fieldleftaligned" + SMBatchStatuses.Get_Transaction_Status(iBatchStatus) + "\" >";
		sOutPut += clsServletUtilities.Fill_In_Empty_String_For_HTML_Cell(sDesc);
		sOutPut += "</TD>";
		
		sOutPut += "<TD class=\"fieldleftaligned" + SMBatchStatuses.Get_Transaction_Status(iBatchStatus) + "\" >";
		sOutPut += clsServletUtilities.Fill_In_Empty_String_For_HTML_Cell(clsDateAndTimeConversions.resultsetDateTimeStringToString(sLastEditedDate));
		sOutPut += "</TD>";
		
		sOutPut += "<TD class=\"fieldleftaligned" + SMBatchStatuses.Get_Transaction_Status(iBatchStatus) + "\" >";
		String sFormattedPostingDate = clsDateAndTimeConversions.resultsetDateTimeStringToString(sPostingDate);
		if (sFormattedPostingDate.compareToIgnoreCase("00/00/0000 00:00 AM") == 0){
			sFormattedPostingDate = "(N/A)";
		}
		sOutPut += sFormattedPostingDate;
		sOutPut += "</TD>";
		
		return sOutPut;

	}
	private String sStyleScripts(){
		String s = "";
		//String sBorderSize = "0";
		String sRowHeight = "22px";
		s += "<style type=\"text/css\">\n";

		//Layout table:
		s +=
			"table.batchlist {"
			+ "border-width: " + "1" + "px; "
			+ "border-spacing: 2px; "
			//+ "border-style: outset; "
			+ "border-style: solid; "
			+ "border-color: black; "
			+ "border-collapse: collapse; "
			//+ "width: 100%; "
			+ "font-size: " + "small" + "; "
			+ "font-family : Arial; "
			+ "color: white; "
			+ "background-color: white; "
			+ "}"
			+ "\n"
			;

		//This is the def for the left aligned fields of an ENTERED batch:
		s +=
			"td.fieldleftaligned" + SMBatchStatuses.Get_Transaction_Status(SMBatchStatuses.ENTERED) + " {"
					+ "height: " + sRowHeight + "; "
					//+ "vertical-align: bottom;"
					+ "background-color: " + SMBatchStatuses.Get_Transaction_Status_Color(SMBatchStatuses.ENTERED) + "; "
					+ "border-width: " + "1" + "px; "
					+ "padding: 2px; "
					+ "border-style: solid; "
					+ "border-color: black; "
					+ "font-weight: normal; "
					+ "font-size: small; "
					+ "text-align: left; "
					+ "color: black; "
			+ "}"
			+ "\n"
			;
		//This is the def for the left aligned fields of an IMPORTED batch:
		s +=
			"td.fieldleftaligned" + SMBatchStatuses.Get_Transaction_Status(SMBatchStatuses.IMPORTED) + " {"
					+ "height: " + sRowHeight + "; "
					//+ "vertical-align: bottom;"
					+ "background-color: " + SMBatchStatuses.Get_Transaction_Status_Color(SMBatchStatuses.IMPORTED) + "; "
					+ "border-width: " + "1" + "px; "
					+ "padding: 2px; "
					+ "border-style: solid; "
					+ "border-color: black; "
					+ "font-weight: normal; "
					+ "font-size: small; "
					+ "text-align: left; "
					+ "color: black; "
			+ "}"
			+ "\n"
			;
		//This is the def for the left aligned fields of a DELETED batch:
		s +=
			"td.fieldleftaligned" + SMBatchStatuses.Get_Transaction_Status(SMBatchStatuses.DELETED) + " {"
					+ "height: " + sRowHeight + "; "
					//+ "vertical-align: bottom;"
					+ "background-color: " + SMBatchStatuses.Get_Transaction_Status_Color(SMBatchStatuses.DELETED) + "; "
					+ "border-width: " + "1" + "px; "
					+ "padding: 2px; "
					+ "border-style: solid; "
					+ "border-color: black; "
					+ "font-weight: normal; "
					+ "font-size: small; "
					+ "text-align: left; "
					+ "color: black; "
			+ "}"
			+ "\n"
			;
		//This is the def for the left aligned fields of a POSTED batch:
		s +=
			"td.fieldleftaligned" + SMBatchStatuses.Get_Transaction_Status(SMBatchStatuses.POSTED) + " {"
					+ "height: " + sRowHeight + "; "
					//+ "vertical-align: bottom;"
					+ "background-color: " + SMBatchStatuses.Get_Transaction_Status_Color(SMBatchStatuses.POSTED) + "; "
					+ "border-width: " + "1" + "px; "
					+ "padding: 2px; "
					+ "border-style: solid; "
					+ "border-color: black; "
					+ "font-weight: normal; "
					+ "font-size: small; "
					+ "text-align: left; "
					+ "color: black; "
			+ "}"
			+ "\n"
			;

		
		//This is the def for the right aligned fields of an ENTERED batch:
		s +=
			"td.fieldrightaligned" + SMBatchStatuses.Get_Transaction_Status(SMBatchStatuses.ENTERED) + " {"
					+ "height: " + sRowHeight + "; "
					//+ "vertical-align: bottom;"
					+ "background-color: " + SMBatchStatuses.Get_Transaction_Status_Color(SMBatchStatuses.ENTERED) + "; "
					+ "border-width: " + "1" + "px; "
					+ "padding: 2px; "
					+ "border-style: solid; "
					+ "border-color: black; "
					+ "font-weight: normal; "
					+ "font-size: small; "
					+ "text-align: right; "
					+ "color: black; "
			+ "}"
			+ "\n"
			;
		//This is the def for the right aligned fields of an IMPORTED batch:
		s +=
			"td.fieldrightaligned" + SMBatchStatuses.Get_Transaction_Status(SMBatchStatuses.IMPORTED) + " {"
					+ "height: " + sRowHeight + "; "
					//+ "vertical-align: bottom;"
					+ "background-color: " + SMBatchStatuses.Get_Transaction_Status_Color(SMBatchStatuses.IMPORTED) + "; "
					+ "border-width: " + "1" + "px; "
					+ "padding: 2px; "
					+ "border-style: solid; "
					+ "border-color: black; "
					+ "font-weight: normal; "
					+ "font-size: small; "
					+ "text-align: right; "
					+ "color: black; "
			+ "}"
			+ "\n"
			;
		//This is the def for the right aligned fields of a DELETED batch:
		s +=
			"td.fieldrightaligned" + SMBatchStatuses.Get_Transaction_Status(SMBatchStatuses.DELETED) + " {"
					+ "height: " + sRowHeight + "; "
					//+ "vertical-align: bottom;"
					+ "background-color: " + SMBatchStatuses.Get_Transaction_Status_Color(SMBatchStatuses.DELETED) + "; "
					+ "border-width: " + "1" + "px; "
					+ "padding: 2px; "
					+ "border-style: solid; "
					+ "border-color: black; "
					+ "font-weight: normal; "
					+ "font-size: small; "
					+ "text-align: right; "
					+ "color: black; "
			+ "}"
			+ "\n"
			;
		//This is the def for the right aligned fields of a POSTED batch:
		s +=
			"td.fieldrightaligned" + SMBatchStatuses.Get_Transaction_Status(SMBatchStatuses.POSTED) + " {"
					+ "height: " + sRowHeight + "; "
					//+ "vertical-align: bottom;"
					+ "background-color: " + SMBatchStatuses.Get_Transaction_Status_Color(SMBatchStatuses.POSTED) + "; "
					+ "border-width: " + "1" + "px; "
					+ "padding: 2px; "
					+ "border-style: solid; "
					+ "border-color: black; "
					+ "font-weight: normal; "
					+ "font-size: small; "
					+ "text-align: right; "
					+ "color: black; "
			+ "}"
			+ "\n"
			;
		
		//This is the def for a left-aligned heading:
		s +=
			"th.headingleft {"
			+ "height: " + sRowHeight + "; "
			//+ "border-width: " + sBorderSize + "px; "
			//+ "padding: 2px; "
			//+ "border-style: none; "
			//+ "border-color: white; "
			+ "vertical-align: bottom;"
			+ "background-color: #708090; "
			+ "font-weight: bold; "
			+ "font-size: small; "
			+ "text-align: left; "
			+ "color: white; "
			+ "}"
			+ "\n"
			;
		
		//This is the def for a right-aligned heading:
		s +=
			"th.headingright {"
			+ "height: " + sRowHeight + "; "
			//+ "border-width: " + sBorderSize + "px; "
			//+ "padding: 2px; "
			//+ "border-style: none; "
			//+ "border-color: white; "
			+ "vertical-align: bottom;"
			+ "background-color: #708090; "
			+ "font-weight: bold; "
			+ "font-size: small; "
			+ "text-align: right; "
			+ "color: white; "
			+ "}"
			+ "\n"
			;
		
		s += "</style>"
			+ "\n"
			;

		return s;
	}
	public String javaScript(){
		String s = "";
		s += " <script>\n"
		  +  "   window.addEventListener(\"beforeunload\",function(){\n" 
		  +  "   document.documentElement.style.cursor = \"not-allowed\";\n "
		  +  "   document.documentElement.style.cursor = \"wait\";\n"
		  +"     });\n"
		  +  " </script>\n";
		return s;
	}
	public void doGet(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
		
		doPost(request, response);
	}
}