package smar;

//import SMDataDefinition.*;
import java.io.IOException;
import java.io.PrintWriter;
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
import SMClasses.SMBatchTypes;
import SMClasses.SMEntryBatch;
import SMClasses.SMModuleTypes;
import SMDataDefinition.SMMasterStyleSheetDefinitions;
import SMDataDefinition.SMTableentries;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsDateAndTimeConversions;
import ServletUtilities.clsManageBigDecimals;
import ServletUtilities.clsServletUtilities;
import ServletUtilities.clsStringFunctions;

public class AREditBatches extends HttpServlet {

	private static final long serialVersionUID = 1L;
	/*
	 * Parameters:
	 * Limit - e.g. Limit=25 - number of batches to list
	 */
	private static final String sObjectName = "Batch";
	
	public void doPost(HttpServletRequest request,
				HttpServletResponse response)
				throws ServletException, IOException {
		
		if (!SMAuthenticate.authenticateSMCPCredentials(
				request, 
				response, 
				getServletContext(),
				SMSystemFunctions.AREditBatches
			)
		){
			return;
		}
	    PrintWriter out = response.getWriter();
		
	    //Get the session info:
	    HttpSession CurrentSession = request.getSession(true);
	    String sDBID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_DATABASE_ID);
	    String sCompanyName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_COMPANYNAME);
	    String sUserID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERID);
	    String sUserFullName = (String)CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERFIRSTNAME) + " "
	    		 		+ (String)CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERLASTNAME);
			    
	    //Get the variables for the class:
	    String sNumberOfBatchesToDisplay;
	    if (request.getParameter("Limit") == null){
	    	sNumberOfBatchesToDisplay = "0";
	    }else{
	    	sNumberOfBatchesToDisplay = request.getParameter("Limit");
	    }

	    String title = "Edit " + sObjectName + "es.";
	    String subtitle = "";
	    out.println(SMUtilities.SMCPTitleSubBGColor(title, subtitle, SMUtilities.getInitBackGroundColor(getServletContext(), sDBID), sCompanyName));

	    //Display any warnings:
	    if (request.getParameter("Warning") != null){
	    	String sWarning = request.getParameter("Warning");
	    	if (!sWarning.equalsIgnoreCase("")){
	    		out.println("<B><FONT COLOR=\"RED\">" + sWarning + "</FONT></B><BR>");
	    	}
	    }
	    
	    String sWarning = (String)CurrentSession.getAttribute(AREditBatchesEdit.AR_BATCH_POSTING_SESSION_WARNING_OBJECT);
	    CurrentSession.removeAttribute(AREditBatchesEdit.AR_BATCH_POSTING_SESSION_WARNING_OBJECT);
		if (sWarning != null){
			out.println("<B><FONT COLOR=\"RED\">: " + sWarning + "</FONT></B><BR>");
		}
	    
	    //Print a link to the first page after login:
	    out.println("<BR><A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMUserLogin?" 
				+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
				+ "\">Return to user login</A><BR>");
	    out.println("<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smar.ARMainMenu?" 
				+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
				+ "\">Return to Accounts Receivable Main Menu</A><BR>");
	    out.println("<A HREF=\"" + WebContextParameters.getdocumentationpageURL(getServletContext()) + "#" + Long.toString(SMSystemFunctions.AREditBatches) 
	    		+ "\">Summary</A><BR><BR>");
	    out.println("<FORM NAME='MAINFORM' ACTION='" + SMUtilities.getURLLinkBase(getServletContext()) + "smar.AREditBatchesEdit' METHOD='POST'>");
	    out.println("<INPUT TYPE=HIDDEN NAME='" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "' VALUE='" + sDBID + "'>");
	    //Add links to create new batches:
	    out.println("<TABLE BORDER=1 CELLSPACING=2 style=\"font-size:75%\">");
	    out.println("<TR>");
	    out.println("<TD>");
	    out.println("<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smar.AREditBatchesEdit?BatchNumber=-1&BatchType=" 
	    		+ Integer.toString(SMBatchTypes.AR_INVOICE) 
	    		+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
	    		+ "\">Create a new invoice batch</A>");
	    out.println("</TD>");
	    out.println("<TD>");
	    out.println("<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smar.AREditBatchesEdit?BatchNumber=-1&BatchType="
	    		+ Integer.toString(SMBatchTypes.AR_CASH) 
	    		+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
	    		+ "\">Create a new cash batch</A>");
	    out.println("</TD>");
	    out.println("<TD>");
	    out.println("<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smar.ARCreateSMInvoiceBatch?" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" 
	    		+ sDBID + "\">Import Service Manager invoice batch</A>");
	    out.println("</TD>");
	    out.println("</TR>");
	    out.println("</TABLE>");
	    out.println(SMUtilities.getMasterStyleSheetLink());
	    out.println("<FONT SIZE=2><B>NOTE:</B>&nbsp;Background color indicates batch status - "
	    	+ "GREEN = Entered, BLUE = Imported, RED = Deleted, WHITE = Posted"
	    	+ "</FONT>"
	    );
	    
	    //Build List
	    out.println(sStyleScripts());
	    out.println(javaScript());
	    out.println("<TABLE WIDTH = 100% class = \" " + SMMasterStyleSheetDefinitions.TABLE_BASIC_WITH_BORDER + " \" >");
	    //out.println("<TABLE BORDER=1 CELLSPACING=2 style=\"font-size:75%\">");
	    out.println("<TR CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_HEADING + "\">");
	    out.println("<TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_LEFT_JUSTIFIED + "\">Batch #</TD>");
	    out.println("<TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_LEFT_JUSTIFIED + "\">Date</TD>");
	    out.println("<TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_LEFT_JUSTIFIED + "\">Type</TD>");
	    out.println("<TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_LEFT_JUSTIFIED + "\">Status</TD>");
	    out.println("<TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_LEFT_JUSTIFIED + "\">Entries</TD>");
	    out.println("<TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_LEFT_JUSTIFIED + "\">Total</TD>");
	    out.println("<TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_LEFT_JUSTIFIED + "\">Created by</TD>");
	    out.println("<TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_LEFT_JUSTIFIED + "\">Description</TD>");
	    out.println("<TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_LEFT_JUSTIFIED + "\">Last edited</TD>");
	    out.println("<TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_LEFT_JUSTIFIED + "\">Posted</TD>");
	    out.println("</TR>");
	    
	    String SQL = "";
	    try{
	    	SQL = "SELECT " 
			+ SMEntryBatch.TableName + "." + SMEntryBatch.ibatchnumber + ", "
			+ SMEntryBatch.datbatchdate + ", "
			+ SMEntryBatch.ibatchstatus + ", "
			+ SMEntryBatch.ibatchtype + ", "
			+ SMEntryBatch.sbatchdescription + ", "
			+ SMEntryBatch.ibatchlastentry + ", "
			+ SMEntryBatch.datlasteditdate + ", "
			+ SMEntryBatch.datpostdate + ", "
			+ SMEntryBatch.TableName + "." + SMEntryBatch.screatedbyfullname 
			
			+ ", SUM(" + 
				SMTableentries.TableName + "." + SMTableentries.doriginalamount
				+ ") AS dbatchtotal"
		
			+ " FROM " + SMEntryBatch.TableName + " LEFT JOIN " 
				+ SMTableentries.TableName + " ON "
				+ SMEntryBatch.TableName + "." + SMEntryBatch.ibatchnumber
				+ " = " + SMTableentries.TableName + "." + SMTableentries.ibatchnumber
			+ " WHERE ("
				+ "(" + SMEntryBatch.smoduletype + " = '" + SMModuleTypes.AR + "')"
			+ ")"
	
			+ " GROUP BY " + SMEntryBatch.TableName + "." + SMEntryBatch.ibatchnumber
			
			+ " ORDER BY " 
			+ SMEntryBatch.ibatchnumber + " DESC";
	    	
	    	if (Long.parseLong(sNumberOfBatchesToDisplay) != 0){
	    		SQL = SQL + " LIMIT " + sNumberOfBatchesToDisplay;
	    	}
	    	//System.out.println("In AREditBatches - SQL: " + SQL);
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
        		out.println("<TR>");
        		String sCreatedBy = "";
        			//sCreatedBy = "(" + rs.getString(SMEntryBatch.TableName + "." + SMEntryBatch.lcreatedbyid) + ")";
        			sCreatedBy = rs.getString(SMEntryBatch.TableName + "." + SMEntryBatch.screatedbyfullname);
        		
        		out.println(
        			Build_Row(		
	        			out,
	        			Integer.toString(rs.getInt(SMEntryBatch.ibatchnumber)),
	        			clsStringFunctions.PadLeft(Integer.toString(rs.getInt(SMEntryBatch.ibatchnumber)),"0",6),
	        			clsDateAndTimeConversions.TimeStampToString(rs.getTimestamp(SMEntryBatch.datbatchdate), "MM-dd-yyyy"),
	        			rs.getInt(SMEntryBatch.ibatchtype),
	        			SMBatchTypes.Get_Batch_Type(rs.getInt(SMEntryBatch.ibatchtype)),
	        			SMBatchStatuses.Get_Transaction_Status((rs.getInt(SMEntryBatch.ibatchstatus))),
	        			rs.getInt(SMEntryBatch.ibatchstatus),
	        			Long.toString(rs.getLong(SMEntryBatch.ibatchlastentry)),
	        			clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(rs.getBigDecimal("dbatchtotal")),
	        			sCreatedBy,
	        			rs.getString(SMEntryBatch.sbatchdescription),
	        			rs.getString(SMEntryBatch.datlasteditdate),
	        			rs.getString(SMEntryBatch.datpostdate),
	        			request,
	        			getServletContext(),
	        			sDBID
        			)
        		);
        		//End the row:
        		out.println("</TR>");
        	}
        	rs.close();
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
		sOutPut += "<A HREF=\"" + SMUtilities.getURLLinkBase(context) + "smar.AREditBatchesEdit" 
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
		sOutPut += clsServletUtilities.Fill_In_Empty_String_For_HTML_Cell(clsDateAndTimeConversions.resultsetDateTimeToTheMinuteStringToString(sLastEditedDate));
		sOutPut += "</TD>";
		
		sOutPut += "<TD class=\"fieldleftaligned" + SMBatchStatuses.Get_Transaction_Status(iBatchStatus) + "\" >";
		String sFormattedPostingDate = clsDateAndTimeConversions.resultsetDateTimeToTheMinuteStringToString(sPostingDate);
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
			+ "background-color: " + SMMasterStyleSheetDefinitions.BACKGROUND_DARK_GREY + "; "
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
			+ "background-color: " + SMMasterStyleSheetDefinitions.BACKGROUND_DARK_GREY + "; "
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
		  +  "     document.body.setAttribute(\"style\",\"pointer-events: none; cursor: not-allowed; \");\n"
		  +  "     document.documentElement.style.cursor = \"wait\";\n"
		  +"      });\n"
		  +  " </script>\n";
		return s;
	}
	public void doGet(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
		
		doPost(request, response);
	}
}