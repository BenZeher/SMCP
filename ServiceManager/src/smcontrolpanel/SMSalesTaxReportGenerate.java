package smcontrolpanel;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import ServletUtilities.clsManageRequestParameters;
import ServletUtilities.clsStringFunctions;

public class SMSalesTaxReportGenerate extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final String sReportTitle = SMSalesTaxReportSelection.SALES_INVOICE_TAX_REPORT_NAME;
    
	@Override
	public void doPost(HttpServletRequest request,
					   HttpServletResponse response)throws ServletException, IOException {
		
		PrintWriter out = response.getWriter();
		if (!SMAuthenticate.authenticateSMCPCredentials(
				request, 
				response, 
				getServletContext(), 
				SMSystemFunctions.SMSalesTaxReport))
		{
			return;
		}

	    //Get the session info:
	    HttpSession CurrentSession = request.getSession(true);
	    String sDBID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_DATABASE_ID);
	    String sUserID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERID);
	    String sCompanyName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_COMPANYNAME);
    	String title = sReportTitle;
    	String subtitle = "";

    	//Display all selected criteria
    	ArrayList<String> alCriteria = new ArrayList<String>(0);
    	
    	//Starting and ending dates:
    	String sStartingDate = clsManageRequestParameters.get_Request_Parameter(SMSalesTaxReportSelection.STARTING_DATE_FIELD, request);
    	String sEndingDate = clsManageRequestParameters.get_Request_Parameter(SMSalesTaxReportSelection.ENDING_DATE_FIELD, request);
    	alCriteria.add("<TABLE BORDER=0><TR>" +
    		"<TD ALIGN=LEFT VALIGN=TOP><FONT SIZE=2><B>Starting on: " + sStartingDate + "</B></FONT></TD>" 
    		+ "</FONT>" 
    		+ "</TD>" 
    		+ "</TR></TABLE>"
    	);
       	alCriteria.add("<TABLE BORDER=0><TR>" +
    		"<TD ALIGN=LEFT VALIGN=TOP><FONT SIZE=2><B>Ending on: " + sEndingDate + "</B></FONT></TD>" 
    		+ "</FONT>" 
    		+ "</TD>" 
    		+ "</TR></TABLE>"
    	);
       	
    	
	    //Selected sales groups and service types
    	ArrayList<String> alSelectedTaxJurisdictions = new ArrayList<String>(0);
    	ArrayList<String> alSelectedCostCenters = new ArrayList<String>(0);
		Enumeration<?> e = request.getParameterNames();
    	String sParam = "";
    	String sCostCenter = "";
    	String sTaxJurisdiction = "";
    	while (e.hasMoreElements()){
    		sParam = (String) e.nextElement();
    		if(clsStringFunctions.StringLeft(
    				sParam, 
    				SMSalesTaxReportSelection.COST_CENTER_CHECKBOX_STUB.length()).compareToIgnoreCase(
    						SMSalesTaxReportSelection.COST_CENTER_CHECKBOX_STUB) == 0){
    			//selected tax jurisdiction
    			if (request.getParameter(sParam) != null){
    				sCostCenter = clsStringFunctions.StringRight(
    					sParam, sParam.length() - SMSalesTaxReportSelection.COST_CENTER_CHECKBOX_STUB.length());
    				alSelectedCostCenters.add(sCostCenter);
    			}
    		}else if (clsStringFunctions.StringLeft(
    				sParam, 
    				SMSalesTaxReportSelection.TAX_JURISDICTION_CHECKBOX_STUB.length()).compareToIgnoreCase(
    						SMSalesTaxReportSelection.TAX_JURISDICTION_CHECKBOX_STUB) == 0){
    			//select sales group
    			if (request.getParameter(sParam) != null){
    				sTaxJurisdiction = clsStringFunctions.StringRight(
    					sParam, sParam.length() - SMSalesTaxReportSelection.TAX_JURISDICTION_CHECKBOX_STUB.length());
    				alSelectedTaxJurisdictions.add(sTaxJurisdiction);
    			}
			}
    	}
    	
    	Collections.sort(alSelectedCostCenters);
    	Collections.sort(alSelectedTaxJurisdictions);
    	String s;
    	
    	s = "<TABLE BORDER=0><TR>" +
			"<TD ALIGN=LEFT VALIGN=TOP><FONT SIZE=2><B>Tax Jurisdictions(s):</B></FONT></TD>" 
    		+ "<TD ALIGN=LEFT VALIGN=TOP><FONT SIZE=2>"
		;
		for (int i = 0; i < alSelectedTaxJurisdictions.size(); i++){
			s += alSelectedTaxJurisdictions.get(i) + "<BR>";
		}
		s += "</FONT>" 
			+ "</TD>" 
			+ "</TR></TABLE>";
    	alCriteria.add(s);
    	
    	s = "<TABLE BORDER=0><TR>" 
    		+ "<TD ALIGN=LEFT VALIGN=TOP><FONT SIZE=2><B>Cost Centers(s):</B></FONT></TD>" 
    		+ "<TD ALIGN=LEFT VALIGN=TOP><FONT SIZE=2>"
    	;
    	
    	boolean bGroupByCostCenters = clsManageRequestParameters.get_Request_Parameter(SMSalesTaxReportSelection.USE_COST_CENTERS, request).compareToIgnoreCase(SMSalesTaxReportSelection.USE_COST_CENTERS_TRUE) == 0;
    	if (!bGroupByCostCenters){
    		s += "(NOT USING COST CENTERS)";
    	}else{
			for (int i = 0; i < alSelectedCostCenters.size(); i++){
				s += alSelectedCostCenters.get(i) + "<BR>";
			}
    	}
      	s += "</FONT>"
	    		+ "</TD>" 
	    		+ "</TR></TABLE>";
    	alCriteria.add(s);
    	
        boolean bShowIndividualInvoiceLines = false;
        
	    if (request.getParameter(SMSalesTaxReportSelection.DETAIL_OPTIONS).compareToIgnoreCase(SMSalesTaxReportSelection.DETAIL_OPTIONS_SHOW_INVOICE_LINES) == 0){
	    	bShowIndividualInvoiceLines = true;
    	}
	    
	    
	    String sItemNumber = "";
	    if(request.getParameter(SMSalesTaxReportSelection.ENTER_ITEM_NUMBER_TEXTBOX) != null){
	    	sItemNumber = clsManageRequestParameters.get_Request_Parameter(SMSalesTaxReportSelection.ENTER_ITEM_NUMBER_TEXTBOX, request);
	    }
	    String sOrderNumber = "";
	    if(request.getParameter(SMSalesTaxReportSelection.ENTER_ORDER_NUMBER_TEXTBOX) != null){
	    	sOrderNumber = clsManageRequestParameters.get_Request_Parameter(SMSalesTaxReportSelection.ENTER_ORDER_NUMBER_TEXTBOX, request);
	    }
	    
	    String sDetailOptions = "";
	    if (bShowIndividualInvoiceLines){
	    	sDetailOptions = "<FONT SIZE=2><B>Showing INDIVIDUAL INVOICE LINES:</B></FONT>&nbsp;";
	    }else{
    		sDetailOptions = "<FONT SIZE=2><B>Showing SUMMARY TOTALS ONLY:</B></FONT>&nbsp;";
	    }
	    alCriteria.add(sDetailOptions);

	    sDetailOptions = "<FONT SIZE=2><B>";
	    if (sItemNumber.compareToIgnoreCase("") != 0){
	    	sDetailOptions += "ONLY listing invoice lines for item number '" + sItemNumber + "',";
	    }else{
    		sDetailOptions += "Listing invoice lines for ANY item numbers,";
	    }
	    if (sOrderNumber.compareToIgnoreCase("") != 0){
	    	sDetailOptions += " ONLY listing invoice lines for sales order '" + sOrderNumber + "'.";
	    }else{
    		sDetailOptions += " listing invoice lines for ANY sales orders.";
	    }

	    sDetailOptions += "</B></FONT>";
	    alCriteria.add(sDetailOptions);
	    String sColor = SMUtilities.getInitBackGroundColor(getServletContext(), sDBID);
	    /*************END of PARAMETER list***************/

    	//print out report heading and selected criteria
	    out.println(SMUtilities.SMCPTitleSubBGColor(title, subtitle, "#FFFFFF", sCompanyName));
	    out.println("<TABLE BORDER=0 WIDTH=100% BGCOLOR = \""+ sColor + "\"><TR>");
	    out.println("<TD ALIGN=LEFT VALIGN=TOP><A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMUserLogin?" 
		+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
		+ "\">Return to user login</A>&nbsp;&nbsp;&nbsp;&nbsp;</TD>");
	    out.println("</TR></TABLE>");
	    out.println(SMUtilities.Build_HTML_Table(
	    	2, 				//Number of Columns
	    	alCriteria,		//Criteria Array
			100,			//Width
			0,				//Border
			false,			//Equal Width?
			false,
			sColor)			//Vertical?
		);
	    out.println("<BR>");
	    SMSalesTaxReport rpt = new SMSalesTaxReport();
	    try {
			rpt.processReport(
				sStartingDate,
				sEndingDate,
				bGroupByCostCenters,
				alSelectedCostCenters,
				alSelectedTaxJurisdictions,
				bShowIndividualInvoiceLines,
				sItemNumber,
				sOrderNumber,
				getServletContext(),
				sDBID,
				sUserID,
				out,
				(String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_LICENSE_MODULE_LEVEL)
			);

		} catch (Exception e1) {
			out.println("<B><FONT COLOR=RED><BR>Error printing report - " + e1.getMessage() + "<BR></FONT></B>");
		}

	    //out.println(s);
		out.println("</BODY></HTML>");
		return;
	}
	
	@Override
	public void doGet(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
		
		doPost(request, response);
	}
}