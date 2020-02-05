package smic;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import ConnectionPool.WebContextParameters;
import SMDataDefinition.SMTableicitems;
import SMDataDefinition.SMTablepricelistcodes;
import ServletUtilities.clsCreateHTMLFormFields;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsManageRequestParameters;
import smcontrolpanel.SMAuthenticate;
import smcontrolpanel.SMPriceLevelLabels;
import smcontrolpanel.SMSystemFunctions;
import smcontrolpanel.SMUtilities;

public class ICUpdateItemPricesSelection  extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
	public static final String STARTINGITEM_PARAM = "StartingItemNumber";
	public static final String ENDINGITEM_PARAM = "EndingItemNumber";
	public static final String STARTINGPRICELIST_PARAM = "StartingPriceList";
	public static final String ENDINGPRICELIST_PARAM = "EndingPriceList";
	public static final String RPTGRP1STARTING_PARAM = "ReportGroup1Starting";
	public static final String RPTGRP1ENDING_PARAM = "ReportGroup1Ending";
	public static final String RPTGRP2STARTING_PARAM = "ReportGroup2Starting";
	public static final String RPTGRP2ENDING_PARAM = "ReportGroup2Ending";
	public static final String RPTGRP3STARTING_PARAM = "ReportGroup3Starting";
	public static final String RPTGRP3ENDING_PARAM = "ReportGroup3Ending";
	public static final String RPTGRP4STARTING_PARAM = "ReportGroup4Starting";
	public static final String RPTGRP4ENDING_PARAM = "ReportGroup4Ending";
	public static final String RPTGRP5STARTING_PARAM = "ReportGroup5Starting";
	public static final String RPTGRP5ENDING_PARAM = "ReportGroup5Ending";
	public static final String PRICELEVEL0_PARAM = "PriceLevel0";
	public static final String PRICELEVEL1_PARAM = "PriceLevel1";
	public static final String PRICELEVEL2_PARAM = "PriceLevel2";
	public static final String PRICELEVEL3_PARAM = "PriceLevel3";
	public static final String PRICELEVEL4_PARAM = "PriceLevel4";
	public static final String PRICELEVEL5_PARAM = "PriceLevel5";
	public static final String UPDATETYPE_PARAM = "UpdatePriceType";
	public static final String UPDATEAMOUNT_PARAM = "Update Amount";
	public static final String PREVIEWBUTTONNAME_PARAM = "PREVIEW";
	public static final String PREVIEWSUBMIT_PARAM = "Preview changes";
	public static final String UPDATEBUTTONNAME_PARAM = "UPDATE";
	public static final String UPDATESUBMIT_PARAM = "Update prices";
	public static final String CONFIRMUPDATE_PARAM = "ConfirmUpdate";

	public void doPost(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
	
		if (!SMAuthenticate.authenticateSMCPCredentials(
				request, 
				response, 
				getServletContext(),
				SMSystemFunctions.ICUpdateItemPrices
			)
		){
			return;
		}
		PrintWriter out = response.getWriter();
		
	    //Get the session info:
	    HttpSession CurrentSession = request.getSession(true);
	    String sDBID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_DATABASE_ID);
	    String sCompanyName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_COMPANYNAME);
	    String sUser = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERNAME);
	    String title = "IC Update Item Prices";
	    String subtitle = "";
	    out.println(SMUtilities.SMCPTitleSubBGColor(title, subtitle, SMUtilities.getInitBackGroundColor(getServletContext(), sDBID), sCompanyName));
	    out.println(SMUtilities.getDatePickerIncludeString(getServletContext()));
	    String sWarning = clsManageRequestParameters.get_Request_Parameter("Warning", request);
		if (!sWarning.equalsIgnoreCase("")){
			out.println("<B><FONT COLOR=\"RED\">WARNING: " + sWarning + "</FONT></B><BR>");
		}
	    String sStatus = clsManageRequestParameters.get_Request_Parameter("Status", request);
		if (!sStatus.equalsIgnoreCase("")){
			out.println("<B>NOTE: " + sStatus + "</B><BR>");
		}
		
	    //Print a link to the first page after login:
	    out.println("<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMUserLogin?" 
				+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
				+ "\">Return to user login</A><BR>");
	    out.println("<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smic.ICMainMenu?" 
				+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
				+ "\">Return to Inventory Control Main Menu</A><BR>");
	    out.println("<A HREF=\"" + WebContextParameters.getdocumentationpageURL(getServletContext()) + "#" + Long.toString(SMSystemFunctions.ICUpdateItemPrices) 
	    	+ "\">Summary</A><BR><BR>");
		out.println ("<FORM ACTION =\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smic.ICUpdateItemPricesGenerate\">");
		out.println("<INPUT TYPE=HIDDEN NAME='" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "' VALUE='" + sDBID + "'>");
		out.println("<INPUT TYPE=HIDDEN NAME=CallingClass VALUE=\"" 
			+ SMUtilities.getFullClassName(this.toString()) + "\">");
		out.println("<TABLE WIDTH=100% CELLPADDING=10 border=4>");
		
		//NEED:
		
		//Item range
		//Starting Item number:
		out.println("<TD ALIGN=RIGHT>" + "<B>Update prices for items:</B></TD>"
				+ "<TD>"
				+ "Starting with: "
				+ clsCreateHTMLFormFields.TDTextBox(
					STARTINGITEM_PARAM, 
					clsManageRequestParameters.get_Request_Parameter(STARTINGITEM_PARAM, request), 
					10, 
					SMTableicitems.sItemNumberLength, 
					""
					)
		);
		
		//Ending Item number:
		String sEndingItem = clsManageRequestParameters.get_Request_Parameter(ENDINGITEM_PARAM, request);
		if (sEndingItem.compareToIgnoreCase("") == 0){
			sEndingItem = "ZZZZZZZZZZZZZZZZ";
		}
		out.println("&nbsp;&nbsp;And ending with:"
				+ clsCreateHTMLFormFields.TDTextBox(
					ENDINGITEM_PARAM, 
					sEndingItem, 
					10, 
					SMTableicitems.sItemNumberLength, 
					""
					));
		
		out.println("</TD></TR>");
		
		//Price code range
		String sStartingPriceCode = clsManageRequestParameters.get_Request_Parameter(STARTINGPRICELIST_PARAM, request);
		String sEndingPriceCode = clsManageRequestParameters.get_Request_Parameter(ENDINGPRICELIST_PARAM, request);
		
	    //Add drop down list
		out.println("<TD ALIGN=RIGHT>" + "<B>For price list codes:</B></TD>");
		
		out.println("<TD>Starting with price list:&nbsp;");
		ResultSet rs = null;
		try{
	        String sSQL = "SELECT *" 
	    		+ " FROM " + SMTablepricelistcodes.TableName
	    		+ " ORDER BY " + SMTablepricelistcodes.spricelistcode;
	        rs = clsDatabaseFunctions.openResultSet(
	        	sSQL, 
	        	getServletContext(), 
	        	sDBID,
	        	"MySQL",
	        	this.toString() + ".doPost - User: " + sUser);
	     	out.println ("<SELECT NAME=\"" + STARTINGPRICELIST_PARAM + "\">" );
        	
	     	String sLastPriceCode = "";
	     	String sOutPut = "";
	     	while (rs.next()){
        		sOutPut = "<OPTION VALUE=\"" + rs.getString(SMTablepricelistcodes.spricelistcode).trim() + "\"";
        		if (sStartingPriceCode.compareToIgnoreCase(rs.getString(SMTablepricelistcodes.spricelistcode).trim()) == 0){
        			sOutPut += " selected ";
        		}
        		out.println(sOutPut
        			+ ">"
        			+ rs.getString(SMTablepricelistcodes.spricelistcode).trim() + " - " 
        			+ rs.getString(SMTablepricelistcodes.sdescription)
        		);        		
        		sLastPriceCode = rs.getString(SMTablepricelistcodes.spricelistcode).trim();
        	}

	        //End the drop down list:
	        out.println ("</SELECT>");
	        
	    	out.println("&nbsp;&nbsp;And ending with price list: </B>");
	     	out.println ("<SELECT NAME=\"" + ENDINGPRICELIST_PARAM + "\">" );
	     	rs.beforeFirst();
	     	
	     	if (sEndingPriceCode.compareToIgnoreCase("") == 0){
	     		sEndingPriceCode = sLastPriceCode;
	     	}
        	while (rs.next()){
        		sOutPut = "<OPTION VALUE=\"" + rs.getString(SMTablepricelistcodes.spricelistcode).trim() + "\"";
        		if (sEndingPriceCode.compareToIgnoreCase(rs.getString(SMTablepricelistcodes.spricelistcode).trim()) == 0){
        			sOutPut += " selected ";
        		}
        		out.println(sOutPut
        			+ ">"
        			+ rs.getString(SMTablepricelistcodes.spricelistcode).trim() + " - " 
        			+ rs.getString(SMTablepricelistcodes.sdescription)
        		);
        	}
		}catch (SQLException e){
			out.println("Could not read price lists table - " + e.getMessage());
		}
	        //End the drop down list:
	        out.println ("</SELECT>");
	        out.println ("</TD></TR>");
		
		//5 report group ranges
		//Starting Report Group 1:
		String sStartingReportGroup1 = clsManageRequestParameters.get_Request_Parameter(RPTGRP1STARTING_PARAM, request);
		String sEndingReportGroup1 = clsManageRequestParameters.get_Request_Parameter(RPTGRP1ENDING_PARAM, request);
		if (sEndingReportGroup1.compareToIgnoreCase("") == 0){
			sEndingReportGroup1 = "ZZZZZZZZZZZZZZZZZZZZZZZZ";
		}
		out.println("<TD ALIGN=RIGHT>" + "<B>With report group 1:</B></TD>"
				+ "<TD>"
				+ "Starting with: "
				+ clsCreateHTMLFormFields.TDTextBox(
					RPTGRP1STARTING_PARAM, 
					sStartingReportGroup1, 
					20, 
					SMTableicitems.sreportgroup1Length, 
					""
					)
		);
		
		//Ending Report group 1:
		out.println("&nbsp;&nbsp;And ending with:&nbsp;"
				+ clsCreateHTMLFormFields.TDTextBox(
					RPTGRP1ENDING_PARAM, 
					sEndingReportGroup1, 
					20, 
					SMTableicitems.sreportgroup1Length, 
					""
					));
		
		out.println("</TD></TR>");

		//Starting Report Group 2:
		String sStartingReportGroup2 = clsManageRequestParameters.get_Request_Parameter(RPTGRP2STARTING_PARAM, request);
		String sEndingReportGroup2 = clsManageRequestParameters.get_Request_Parameter(RPTGRP2ENDING_PARAM, request);
		if (sEndingReportGroup2.compareToIgnoreCase("") == 0){
			sEndingReportGroup2 = "ZZZZZZZZZZZZZZZZZZZZZZZZ";
		}
		out.println("<TD ALIGN=RIGHT>" + "<B>And report group 2:</B></TD>"
				+ "<TD>"
				+ "Starting with: "
				+ clsCreateHTMLFormFields.TDTextBox(
					RPTGRP2STARTING_PARAM, 
					sStartingReportGroup2, 
					20, 
					SMTableicitems.sreportgroup2Length, 
					""
					)
		);
		
		//Ending Report group 2:
		out.println("&nbsp;&nbsp;And ending with:&nbsp;"
				+ clsCreateHTMLFormFields.TDTextBox(
					RPTGRP2ENDING_PARAM, 
					sEndingReportGroup2, 
					20, 
					SMTableicitems.sreportgroup2Length, 
					""
					));
		
		out.println("</TD></TR>");
		
		//Starting Report Group 3:
		String sStartingReportGroup3 = clsManageRequestParameters.get_Request_Parameter(RPTGRP3STARTING_PARAM, request);
		String sEndingReportGroup3 = clsManageRequestParameters.get_Request_Parameter(RPTGRP3ENDING_PARAM, request);
		if (sEndingReportGroup3.compareToIgnoreCase("") == 0){
			sEndingReportGroup3 = "ZZZZZZZZZZZZZZZZZZZZZZZZ";
		}
		out.println("<TD ALIGN=RIGHT>" + "<B>And report group 3:</B></TD>"
				+ "<TD>"
				+ "Starting with: "
				+ clsCreateHTMLFormFields.TDTextBox(
					RPTGRP3STARTING_PARAM, 
					sStartingReportGroup3, 
					20, 
					SMTableicitems.sreportgroup3Length, 
					""
					)
		);
		
		//Ending Report group 3:
		out.println("&nbsp;&nbsp;And ending with:&nbsp;"
				+ clsCreateHTMLFormFields.TDTextBox(
					RPTGRP3ENDING_PARAM, 
					sEndingReportGroup3, 
					20, 
					SMTableicitems.sreportgroup3Length, 
					""
					));
		
		out.println("</TD></TR>");
		
		//Starting Report Group 4:
		String sStartingReportGroup4 = clsManageRequestParameters.get_Request_Parameter(RPTGRP4STARTING_PARAM, request);
		String sEndingReportGroup4 = clsManageRequestParameters.get_Request_Parameter(RPTGRP4ENDING_PARAM, request);
		if (sEndingReportGroup4.compareToIgnoreCase("") == 0){
			sEndingReportGroup4 = "ZZZZZZZZZZZZZZZZZZZZZZZZ";
		}
		out.println("<TD ALIGN=RIGHT>" + "<B>And report group 4:</B></TD>"
				+ "<TD>"
				+ "Starting with: "
				+ clsCreateHTMLFormFields.TDTextBox(
					RPTGRP4STARTING_PARAM, 
					sStartingReportGroup4, 
					20, 
					SMTableicitems.sreportgroup4Length, 
					""
					)
		);
		
		//Ending Report group 4:
		out.println("&nbsp;&nbsp;And ending with:&nbsp;"
				+ clsCreateHTMLFormFields.TDTextBox(
					RPTGRP4ENDING_PARAM, 
					sEndingReportGroup4, 
					20, 
					SMTableicitems.sreportgroup4Length, 
					""
					));
		
		out.println("</TD></TR>");
		
		//Starting Report Group 5:
		String sStartingReportGroup5 = clsManageRequestParameters.get_Request_Parameter(RPTGRP5STARTING_PARAM, request);
		String sEndingReportGroup5 = clsManageRequestParameters.get_Request_Parameter(RPTGRP5ENDING_PARAM, request);
		if (sEndingReportGroup5.compareToIgnoreCase("") == 0){
			sEndingReportGroup5 = "ZZZZZZZZZZZZZZZZZZZZZZZZ";
		}
		out.println("<TD ALIGN=RIGHT>" + "<B>And report group 5:</B></TD>"
				+ "<TD>"
				+ "Starting with: "
				+ clsCreateHTMLFormFields.TDTextBox(
					RPTGRP5STARTING_PARAM, 
					sStartingReportGroup5, 
					20, 
					SMTableicitems.sreportgroup5Length, 
					""
					)
		);
		
		//Ending Report group 5:
		out.println("&nbsp;&nbsp;And ending with:&nbsp;"
				+ clsCreateHTMLFormFields.TDTextBox(
					RPTGRP5ENDING_PARAM, 
					sEndingReportGroup5, 
					20, 
					SMTableicitems.sreportgroup5Length, 
					""
					));
		
		out.println("</TD></TR>");
    	
		//checkboxes for pricelevels:
		//Get a connection:
		Connection conn = null;
		try {
			conn = ServletUtilities.clsDatabaseFunctions.getConnectionWithException(
				getServletContext(), 
				sDBID, 
				"MySQL", 
				this.toString() + ".doPost - user: " + sUser);
		} catch (Exception e) {
			out.println("<BR><B><FONT COLOR=RED> Error [1580854741] getting connection - " + e.getMessage() + "</FONT></B><BR>");
			out.println("</BODY></HTML>");
			return;
		}
		
		SMPriceLevelLabels pricelevellabels = new SMPriceLevelLabels();
		try {
			pricelevellabels.load(conn);
		} catch (Exception e1) {
			ServletUtilities.clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1580910623]");
			out.println("<BR><B><FONT COLOR=RED> Error [1580854742] getting price level labels - " + e1.getMessage() + "</FONT></B><BR>");
			out.println("</BODY></HTML>");
			return;
		}
		ServletUtilities.clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1580910624]");
		
		out.println("<TR>");
		out.println("<TD ALIGN=RIGHT><B>Update price levels:</B></TD>");
		out.println("<TD>");
		String sCheckBoxValue = "";
		if (clsManageRequestParameters.get_Request_Parameter(PRICELEVEL0_PARAM, request).compareToIgnoreCase("") != 0){
			sCheckBoxValue = " checked=\"yes\"";
		}else{
			sCheckBoxValue = "";
		}
		out.println("<LABEL>" + pricelevellabels.get_sbaselabel() + ":&nbsp;<INPUT TYPE=CHECKBOX NAME= \"" + PRICELEVEL0_PARAM 
			+ "\"" + sCheckBoxValue + "></LABEL>");
		if (clsManageRequestParameters.get_Request_Parameter(PRICELEVEL1_PARAM, request).compareToIgnoreCase("") != 0){
			sCheckBoxValue = " checked=\"yes\"";
		}else{
			sCheckBoxValue = "";
		}
		out.println("<LABEL>&nbsp;&nbsp;" + pricelevellabels.get_slevel1label() + ":&nbsp;<INPUT TYPE=CHECKBOX NAME= \"" + PRICELEVEL1_PARAM 
				+ "\"" + sCheckBoxValue + "></LABEL>");
		if (clsManageRequestParameters.get_Request_Parameter(PRICELEVEL2_PARAM, request).compareToIgnoreCase("") != 0){
			sCheckBoxValue = " checked=\"yes\"";
		}else{
			sCheckBoxValue = "";
		}
		out.println("<LABEL>&nbsp;&nbsp;" + pricelevellabels.get_slevel2label() + ":&nbsp;<INPUT TYPE=CHECKBOX NAME= \"" + PRICELEVEL2_PARAM 
				+ "\"" + sCheckBoxValue + "></LABEL>");
		if (clsManageRequestParameters.get_Request_Parameter(PRICELEVEL3_PARAM, request).compareToIgnoreCase("") != 0){
			sCheckBoxValue = " checked=\"yes\"";
		}else{
			sCheckBoxValue = "";
		}
		out.println("<LABEL>&nbsp;&nbsp;" + pricelevellabels.get_slevel3label() + ":&nbsp;<INPUT TYPE=CHECKBOX NAME= \"" + PRICELEVEL3_PARAM 
				+ "\"" + sCheckBoxValue + "></LABEL>");
		if (clsManageRequestParameters.get_Request_Parameter(PRICELEVEL4_PARAM, request).compareToIgnoreCase("") != 0){
			sCheckBoxValue = " checked=\"yes\"";
		}else{
			sCheckBoxValue = "";
		}
		out.println("<LABEL>&nbsp;&nbsp;" + pricelevellabels.get_slevel4label() + ":&nbsp;<INPUT TYPE=CHECKBOX NAME= \"" + PRICELEVEL4_PARAM 
				+ "\"" + sCheckBoxValue + "></LABEL>");
		if (clsManageRequestParameters.get_Request_Parameter(PRICELEVEL5_PARAM, request).compareToIgnoreCase("") != 0){
			sCheckBoxValue = " checked=\"yes\"";
		}else{
			sCheckBoxValue = "";
		}
		out.println("<LABEL>&nbsp;&nbsp;" + pricelevellabels.get_slevel5label() + ":&nbsp;<INPUT TYPE=CHECKBOX NAME= \"" + PRICELEVEL5_PARAM 
				+ "\"" + sCheckBoxValue + "></LABEL>");

		out.println("</TD>");
		out.println("</TR>");
		
		//Update price type:
		//Percent or fixed amount
    	out.println("<TR><TD ALIGN=RIGHT><B>Update price by:</B></TD>");
    	if(clsManageRequestParameters.get_Request_Parameter(UPDATETYPE_PARAM, request).compareToIgnoreCase("0") == 0){
        	out.println("<TD>" + 
    				"<LABLE><INPUT TYPE=\"RADIO\" NAME=\"" + UPDATETYPE_PARAM + "\" VALUE=1 >Percentage</LABEL>" + 
					"&nbsp;&nbsp;<LABEL><INPUT TYPE=\"RADIO\" NAME=\"" + UPDATETYPE_PARAM + "\" VALUE=0 CHECKED=\"yes\">Fixed amount</LABEL>");    		
    	}else{
        	out.println("<TD>" + 
    				"<LABEL><INPUT TYPE=\"RADIO\" NAME=\"" + UPDATETYPE_PARAM + "\" VALUE=1 CHECKED=\"yes\">Percentage</LABEL>" + 
					"&nbsp;&nbsp;<LABEL><INPUT TYPE=\"RADIO\" NAME=\"" + UPDATETYPE_PARAM + "\" VALUE=0 >Fixed amount</LABEL>");    		
    	}
		
		String sUpdateAmount = clsManageRequestParameters.get_Request_Parameter(UPDATEAMOUNT_PARAM, request);

		if (sUpdateAmount.compareToIgnoreCase("") == 0){
			sUpdateAmount = "0.00";
		}
		out.println("&nbsp;&nbsp;&nbsp;Percentage/amount:&nbsp;"
			+ clsCreateHTMLFormFields.TDTextBox(
				UPDATEAMOUNT_PARAM, 
				sUpdateAmount, 
				10, 
				13, 
				""
				) 
		);
		
		out.println("</TD></TR>");
		
		out.println("</TABLE>");
		out.println("<BR>");
		out.println("<INPUT TYPE=\"SUBMIT\" NAME=\"" + PREVIEWBUTTONNAME_PARAM + "\"  VALUE=\"" + PREVIEWSUBMIT_PARAM + "\">");
		out.println("&nbsp;&nbsp;<INPUT TYPE=\"SUBMIT\" NAME=\"" + UPDATEBUTTONNAME_PARAM + "\" VALUE=\"" + UPDATESUBMIT_PARAM + "\">");
		out.println("&nbsp;Confirm update:&nbsp;<INPUT TYPE=CHECKBOX NAME= \"" + CONFIRMUPDATE_PARAM + "\">");
		out.println("</FORM>");
	    	
		out.println("</BODY></HTML>");
	}
	
	public void doGet(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
		
		doPost(request, response);
	}
}
