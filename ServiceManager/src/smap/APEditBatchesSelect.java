package smap;

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

import ConnectionPool.WebContextParameters;
import SMClasses.SMBatchStatuses;
import SMDataDefinition.SMTableapbatchentries;
import SMDataDefinition.SMTableapbatches;
import SMDataDefinition.SMTableusers;
import ServletUtilities.clsServletUtilities;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsDateAndTimeConversions;
import ServletUtilities.clsManageBigDecimals;
import ServletUtilities.clsStringFunctions;
import smar.ARUtilities;
import smcontrolpanel.SMAuthenticate;
import smcontrolpanel.SMSystemFunctions;
import smcontrolpanel.SMUtilities;

public class APEditBatchesSelect extends HttpServlet {

	public static final String NUMBER_OF_BATCHES_TO_DISPLAY = "25";
	
	private static final long serialVersionUID = 1L;
	/*
	 * Parameters:
	 * Limit - e.g. Limit=25 - number of batches to list
	 */
	private static final String sObjectName = "AP Batch";
	
	public void doPost(HttpServletRequest request,
				HttpServletResponse response)
				throws ServletException, IOException {
		
		if (!SMAuthenticate.authenticateSMCPCredentials(
				request, 
				response, 
				getServletContext(), 
				SMSystemFunctions.APEditBatches)){
			return;
		}
		PrintWriter out = response.getWriter();
	    //Get the session info:
	    HttpSession CurrentSession = request.getSession(true);
	    String sDBID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_DATABASE_ID);
	    String sUserID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERID);
	    String sUserFullName = (String)CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERFIRSTNAME) + " "
	    		+ (String)CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERLASTNAME);
	    String sCompanyName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_COMPANYNAME);
	    
	    //Get the variables for the class:
	    String sNumberOfBatchesToDisplay;
	    if (request.getParameter(NUMBER_OF_BATCHES_TO_DISPLAY) == null){
	    	sNumberOfBatchesToDisplay = "0";
	    }else{
	    	sNumberOfBatchesToDisplay = request.getParameter(NUMBER_OF_BATCHES_TO_DISPLAY);
	    }

	    String title = "Edit " + sObjectName + "es.";
	    String subtitle = "";
	    out.println(SMUtilities.SMCPTitleSubBGColor(title, subtitle, SMUtilities.getInitBackGroundColor(getServletContext(), sDBID), sCompanyName));

	    //Display any warnings:
	    if (request.getParameter("Warning") != null){
	    	String sWarning = request.getParameter("Warning");
	    	if (!sWarning.equalsIgnoreCase("")){
	    		out.println("\n<B><FONT COLOR=\"RED\">" + sWarning + "</FONT></B><BR>\n");
	    	}
	    }
	    if (request.getParameter("Status") != null){
	    	String sStatus = request.getParameter("Status");
	    	if (!sStatus.equalsIgnoreCase("")){
	    		out.println("\n<B>" + sStatus + "</B><BR>\n");
	    	}
	    }
	    
	    //Print a link to the first page after login:
	    out.println("<BR><A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMUserLogin?" 
				+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
				+ "\">Return to user login</A><BR>\n");
	    out.println("<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smap.APMainMenu?" 
				+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
				+ "\">Return to Accounts Payable Main Menu</A><BR>\n");
	    out.println("<A HREF=\"" + WebContextParameters.getdocumentationpageURL(getServletContext()) + "#" + Long.toString(SMSystemFunctions.APEditBatches) 
	    		+ "\">Summary</A><BR><BR>\n");
	    out.println("<FORM NAME='MAINFORM' ACTION='" + SMUtilities.getURLLinkBase(getServletContext()) + "smap.APEditBatchesEdit' METHOD='POST'>");
	    out.println("<INPUT TYPE=HIDDEN NAME='" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "' VALUE='" + sDBID + "'>\n");
	    //Add links to create new batches:
	    out.println("<TABLE BORDER=1 CELLSPACING=2 style=\"font-size:75%\">\n");
	    out.println("  <TR>\n");
	    out.println("    <TD>");
	    out.println("<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) 
	    	+ "smap.APEditBatchesEdit?" + SMTableapbatches.lbatchnumber + "=-1&" 
	    	+ SMTableapbatches.ibatchtype + "=" 
	    	+ Integer.toString(SMTableapbatches.AP_BATCH_TYPE_INVOICE) 
	    	+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
	    	+ "\">New invoice batch</A>");
	    out.println("</TD>\n");
	    
	    out.println("    <TD>");
	    out.println("<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) 
	    	+ "smap.APEditBatchesEdit?" + SMTableapbatches.lbatchnumber + "=-1&" 
	    	+ SMTableapbatches.ibatchtype + "=" 
	    	+ Integer.toString(SMTableapbatches.AP_BATCH_TYPE_PAYMENT) 
	    	+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
	    	+ "\">Create manual check batch</A>");
	    out.println("</TD>\n");
	    
	    //Create a link to automatically create payment batches:
	    out.println("    <TD>");
	    out.println("<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smap.APCreatePaymentsReportEdit"
	    	+ "?" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
	    	+ "\">Pre-check register</A>");
	    out.println("</TD>\n");

	    //Create a link to create a check reversal batch:
	    if (SMSystemFunctions.isFunctionPermitted(
	    		SMSystemFunctions.APReverseChecks, 
	    		sUserID, 
	    		getServletContext(), 
	    		sDBID, 
	    		(String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_LICENSE_MODULE_LEVEL)
	    	)
	    ){
		    out.println("    <TD>");
		    out.println("<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) 
		    	+ "smap.APEditBatchesEdit?" + SMTableapbatches.lbatchnumber + "=-1&" 
		    	+ SMTableapbatches.ibatchtype + "=" 
		    	+ Integer.toString(SMTableapbatches.AP_BATCH_TYPE_REVERSALS) 
		    	+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
		    	+ "\">Create check reversal batch</A>");
		    out.println("</TD>\n");
	    }
	    
	    out.println("  </TR>\n");
	    out.println("</TABLE>\n");
	    
	    out.println("<FONT SIZE=2><B>NOTE:</B>&nbsp;Background color indicates batch status - "
		    	+ "GREEN = Entered, BLUE = Imported, RED = Deleted, WHITE = Posted"
		    	+ "</FONT>"
		    );
	    
	    //Build List
	    out.println(sStyleScripts());
	    out.println(javaScript());
	    out.println("<TABLE class = \" batchlist \" >\n");
	    
	    //Headings:
	    out.println("  <TR>\n");
	    out.println("    <TH class=\"headingleft\" >Batch #</TH>\n");
	    out.println("    <TH class=\"headingleft\" >Date</TH>\n");
	    out.println("    <TH class=\"headingleft\" >Type</TH>\n");
	    out.println("    <TH class=\"headingleft\" >Status</TH>\n");
	    out.println("    <TH class=\"headingright\" >Entries</TH>\n");
	    out.println("    <TH class=\"headingright\" >Net batch total</TH>\n");
	    out.println("    <TH class=\"headingleft\" >Created by</TH>\n");
	    out.println("    <TH class=\"headingleft\" >Description</TH>\n");
	    out.println("    <TH class=\"headingleft\" >Last edited</TH>\n");
	    out.println("    <TH class=\"headingleft\" >Posted</TH>\n");
	    out.println("  </TR>\n");

	    String SQL = "";
	    try{
	    	SQL = "SELECT" 
			+ " " + SMTableapbatches.TableName + "." + SMTableapbatches.lbatchnumber 
			+ ", " + SMTableapbatches.TableName + "." + SMTableapbatches.datbatchdate 
			+ ", " + SMTableapbatches.TableName + "." + SMTableapbatches.ibatchstatus 
			+ ", " + SMTableapbatches.TableName + "." + SMTableapbatches.ibatchtype 
			+ ", " + SMTableapbatches.TableName + "." + SMTableapbatches.sbatchdescription 
			+ ", " + SMTableapbatches.TableName + "." + SMTableapbatches.lbatchlastentry 
			+ ", " + SMTableapbatches.TableName + "." + SMTableapbatches.datlasteditdate 
			+ ", " + SMTableapbatches.TableName + "." + SMTableapbatches.datpostdate 
			+ ", " + SMTableapbatches.TableName + "." + SMTableapbatches.lcreatedby

			+ ", " + SMTableusers.TableName + "." + SMTableusers.sUserFirstName
			+ ", " + SMTableusers.TableName + "." + SMTableusers.sUserLastName
			
			+ ", SUM(" 
				+ SMTableapbatchentries.TableName + "." + SMTableapbatchentries.bdentryamount
				+ ") AS dbatchtotal"
		
			+ " FROM " + SMTableapbatches.TableName + " LEFT JOIN " 
				+ SMTableapbatchentries.TableName + " ON "
				+ SMTableapbatches.TableName + "." + SMTableapbatches.lbatchnumber
				+ " = " + SMTableapbatchentries.TableName + "." + SMTableapbatchentries.lbatchnumber
				+ " LEFT JOIN " + SMTableusers.TableName + " ON "
				+ SMTableapbatches.TableName + "." + SMTableapbatches.lcreatedby + " = " 
				+ SMTableusers.TableName + "." + SMTableusers.lid
	
			+ " GROUP BY " + SMTableapbatches.TableName + "." + SMTableapbatches.lbatchnumber
			
			+ " ORDER BY " 
			+ SMTableapbatches.lbatchnumber + " DESC";
	    	
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
       				bdBatchTotal = rs.getBigDecimal("dbatchtotal");
        		}
        		out.println("  <TR>\n");
        		String sCreatedBy = "";
        		if (
        			(rs.getString(SMTableusers.TableName + "." + SMTableusers.sUserFirstName) == null)
        			|| (rs.getString(SMTableusers.TableName + "." + SMTableusers.sUserFirstName) == null)
        				
        		){
        			sCreatedBy = "(User ID: '" + rs.getString(SMTableapbatches.TableName + "." + SMTableapbatches.lcreatedby) + "')";
        		} else{
        			sCreatedBy = rs.getString(SMTableusers.TableName + "." + SMTableusers.sUserFirstName)
        				+ " " + rs.getString(SMTableusers.TableName + "." + SMTableusers.sUserLastName);
        		}
        		
        		//System.out.println(
        		//	"[1490198979] "
        		//	+ "rs.getString(SMTableapbatches.TableName.SMTableapbatches.datpostdate) = '" + rs.getString(SMTableapbatches.TableName + "." + SMTableapbatches.datpostdate) + "'"
        		//	+ ", DATETIME_FORMAT_FOR_DISPLAY = '" + SMUtilities.DATETIME_FORMAT_FOR_DISPLAY + "'"
        		//);
        		
        		try {
					out.println(
						Build_Row(		
							out,
							Integer.toString(rs.getInt(SMTableapbatches.TableName + "." + SMTableapbatches.lbatchnumber)),
							clsStringFunctions.PadLeft(Integer.toString(rs.getInt(SMTableapbatches.TableName + "." + SMTableapbatches.lbatchnumber)),"0",6),
							clsDateAndTimeConversions.resultsetDateTimeStringToFormattedString(
									rs.getString(SMTableapbatches.TableName + "." + SMTableapbatches.datbatchdate), SMUtilities.DATE_FORMAT_FOR_DISPLAY, SMUtilities.EMPTY_DATETIME_VALUE),
							rs.getInt(SMTableapbatches.TableName + "." + SMTableapbatches.ibatchtype),
							SMTableapbatches.getBatchTypeLabel(rs.getInt(SMTableapbatches.TableName + "." + SMTableapbatches.ibatchtype)),
							SMBatchStatuses.Get_Transaction_Status((rs.getInt(SMTableapbatches.TableName + "." + SMTableapbatches.ibatchstatus))),
							rs.getInt(SMTableapbatches.TableName + "." + SMTableapbatches.ibatchstatus),
							Long.toString(rs.getLong(SMTableapbatches.TableName + "." + SMTableapbatches.lbatchlastentry)),
							clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(bdBatchTotal),
							sCreatedBy,
							rs.getString(SMTableapbatches.TableName + "." + SMTableapbatches.sbatchdescription),
							clsDateAndTimeConversions.resultsetDateTimeStringToFormattedString(
									rs.getString(SMTableapbatches.TableName + "." + SMTableapbatches.datlasteditdate), SMUtilities.DATETIME_FORMAT_FOR_DISPLAY, SMUtilities.EMPTY_DATETIME_VALUE),
							clsDateAndTimeConversions.resultsetDateTimeStringToFormattedString(
									rs.getString(SMTableapbatches.TableName + "." + SMTableapbatches.datpostdate), SMUtilities.DATETIME_FORMAT_FOR_DISPLAY, SMUtilities.EMPTY_DATETIME_VALUE),
							request,
							getServletContext(),
							sDBID
						)
					);
				} catch (Exception e) {
					out.println("<BR><FONT COLOR=RED><B>Error [1490198033] reading row for batch number " 
						+ Integer.toString(rs.getInt(SMTableapbatches.TableName + "." + SMTableapbatches.lbatchnumber))
						+ " - " + e.getMessage()
						+ ".</B></FONT><BR>"
					);
				}
        		//End the row:
        		out.println("  </TR>\n");
        	}
        	rs.close();
	        //out.println ("<BR>");
		}catch (SQLException ex){
			out.println ("\n<BR><FONT COLOR=RED><B>"
					+ "Error [1488898524] reading batches with SQL: " + clsStringFunctions.filter(SQL + " - " + ex.getMessage())
					+ "</B></FONT>\n"
			);
		}
		
		out.println("</TABLE>\n");
		
		//***************************************
		out.println("\n</BODY></HTML>\n");
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

		String sOutPut = "    <TD class=\"fieldleftaligned" + SMBatchStatuses.Get_Transaction_Status(iBatchStatus) + "\" >";
		sOutPut += "<A HREF=\"" + SMUtilities.getURLLinkBase(context) + "smap.APEditBatchesEdit" 
	    		+ "?" + SMTableapbatches.lbatchnumber + "=" + sBatchNumber
	    		+ "&" + SMTableapbatches.ibatchtype + "=" + Integer.toString(iBatchType)
	    		+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
	    		+ "\">"
	    		+ sBatchNumberLabel
	    		+ "</A>";
		sOutPut += "</TD>\n";
		
		sOutPut += "    <TD class=\"fieldleftaligned" + SMBatchStatuses.Get_Transaction_Status(iBatchStatus) + "\" >";
		sOutPut += sBatchDate;
		sOutPut += "</TD>\n";

		sOutPut += "    <TD class=\"fieldleftaligned" + SMBatchStatuses.Get_Transaction_Status(iBatchStatus) + "\" >";
		sOutPut += sBatchTypeLabel;
		sOutPut += "</TD>\n";
		
		sOutPut += "    <TD class=\"fieldleftaligned" + SMBatchStatuses.Get_Transaction_Status(iBatchStatus) + "\" >";
		sOutPut += sBatchStatus;
		sOutPut += "</TD>\n";

		sOutPut += "    <TD class=\"fieldrightaligned" + SMBatchStatuses.Get_Transaction_Status(iBatchStatus) + "\" >";
		sOutPut += sNoOfLines;
		sOutPut += "</TD>\n";
		
		sOutPut += "    <TD class=\"fieldrightaligned" + SMBatchStatuses.Get_Transaction_Status(iBatchStatus) + "\" >";
		sOutPut += sBatchTotal;
		sOutPut += "</TD>\n";
		
		sOutPut += "    <TD class=\"fieldleftaligned" + SMBatchStatuses.Get_Transaction_Status(iBatchStatus) + "\" >";
		sOutPut += sCreatedBy;
		sOutPut += "</TD>\n";
		
		sOutPut += "    <TD class=\"fieldleftaligned" + SMBatchStatuses.Get_Transaction_Status(iBatchStatus) + "\" >";
		sOutPut += clsServletUtilities.Fill_In_Empty_String_For_HTML_Cell(sDesc);
		sOutPut += "</TD>\n";
		
		sOutPut += "    <TD class=\"fieldleftaligned" + SMBatchStatuses.Get_Transaction_Status(iBatchStatus) + "\" >";
		sOutPut += ARUtilities.Fill_In_Empty_String_For_HTML_Cell(sLastEditedDate);
		sOutPut += "</TD>\n";
		
		sOutPut += "    <TD class=\"fieldleftaligned" + SMBatchStatuses.Get_Transaction_Status(iBatchStatus) + "\" >";
		//String sFormattedPostingDate = SMUtilities.resultsetDateTimeStringToString(sPostingDate);
		//if (sFormattedPostingDate.compareToIgnoreCase("00/00/0000 00:00 AM") == 0){
		//	sFormattedPostingDate = "(N/A)";
		//}
		sOutPut += sPostingDate;
		sOutPut += "</TD>\n";
		
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