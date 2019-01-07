package smcontrolpanel;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.ResultSet;
import java.util.ArrayList;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import ConnectionPool.WebContextParameters;
import SMDataDefinition.SMTablecostcenters;
import SMDataDefinition.SMTabletax;
import ServletUtilities.clsServletUtilities;
import ServletUtilities.clsCreateHTMLFormFields;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsManageRequestParameters;

public class SMSalesTaxReportSelection extends HttpServlet {

	public static final String STARTING_DATE_FIELD = "STARTINGDATEFIELD";
	public static final String ENDING_DATE_FIELD = "ENDINGDATEFIELD";
	public static final String TAX_JURISDICTION_CHECKBOX_STUB = "TAXJURISDICTIONCHK";
	public static final String COST_CENTER_CHECKBOX_STUB = "COSTCENTERCHK";
	
	public static final String ENTER_ITEM_NUMBER_TEXTBOX = "ENTERITEMNUMBERTXT";
	public static final String ENTER_ORDER_NUMBER_TEXTBOX = "ENTERORDERNUMBERTXT";
	
	public static final String USE_COST_CENTERS = "USECOSTCENTERS";
	public static final String USE_COST_CENTERS_TRUE = "USECOSTCENTERSTRUE";
	public static final String USE_COST_CENTERS_FALSE = "USECOSTCENTERSFALSE";
	
	public static final String DETAIL_OPTIONS = "DETAILOPTIONS";
	public static final String DETAIL_OPTIONS_SHOW_INVOICE_LINES = "SHOWINVOICELINES";
	//public static final String DETAIL_OPTIONS_SHOW_INVOICE_HEADERS = "SHOWINVOICEHEADERS";
	public static final String DETAIL_OPTIONS_SHOW_TOTALS_ONLY = "SHOWTOTALSONLY";
	
	public static final String SALES_INVOICE_TAX_REPORT_NAME = "Sales Invoice Tax Report";
	
	private static final long serialVersionUID = 1L;
	//private static String sObjectName = "Asset";
	private static String sCalledClassName = "SMSalesTaxReportGenerate";
	private static String sCompanyName = "";
	private static String sUserName = "";
	private static String sUserID = "";
	private static String sUserFullName = "";
	private String sDBID = "";
	
	public void doPost(HttpServletRequest request,
				HttpServletResponse response)
				throws ServletException, IOException {
		
		PrintWriter out = response.getWriter();
	    //Get the session info:
	    HttpSession CurrentSession = request.getSession(true);
	    sCompanyName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_COMPANYNAME);
	    sDBID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_DATABASE_ID);
	    sUserName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERNAME);
	    sUserID = (String)CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERID);
	    sUserFullName = (String)CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERFIRSTNAME) + " "
	    				+ (String)CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERLASTNAME);
	    if (!SMAuthenticate.authenticateSMCPCredentials(request, response, getServletContext(), SMSystemFunctions.SMSalesTaxReport)){
	    	return;
	    }
	    String subtitle = "";
	    out.println(SMUtilities.SMCPTitleSubBGColor(SALES_INVOICE_TAX_REPORT_NAME, subtitle, SMUtilities.getInitBackGroundColor(getServletContext(), sDBID), sCompanyName));
	    out.println(SMUtilities.getDatePickerIncludeString(getServletContext()));
	    out.println(SMUtilities.getJQueryIncludeString());
	    out.println(getJavascript());
	    
		//If there is a warning from trying to input previously, print it here:
		String sWarning = clsManageRequestParameters.get_Request_Parameter("Warning", request);
	    if (! sWarning.equalsIgnoreCase("")){
			out.println("<B><FONT COLOR=\"RED\">WARNING: " + sWarning + "</FONT></B><BR>");
		}
		String sStatus = clsManageRequestParameters.get_Request_Parameter("Status", request);
	    if (! sStatus.equalsIgnoreCase("")){
			out.println("<B>STATUS: " + sStatus + "</B><BR>");
		}
	    
	    //Print a link to the first page after login:
	    out.println("<BR><A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMUserLogin?" 
				+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
				+ "\">Return to user login</A><BR>");
	    out.println("<A HREF=\"" + WebContextParameters.getdocumentationpageURL(getServletContext()) + "#" + Long.toString(SMSystemFunctions.SMSalesTaxReport) 
	    		+ "\">Summary</A><BR><BR>");
	    out.println("<FORM NAME='MAINFORM' ACTION='" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel." + sCalledClassName + "' METHOD='POST'>");
	    out.println("<INPUT TYPE=HIDDEN NAME='" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "' VALUE='" + sDBID + "'>");
	    try{
	    	List_Criteria(out, sDBID, sUserName, sUserID, sUserFullName, request);
	    }catch (Exception e){
			response.sendRedirect(
					"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMUserLogin"
					+ "?" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
					+ "&Warning=" + clsServletUtilities.URLEncode("Error displaying sales tax report selection screen : " + e.getMessage())
			);
			return;
	    }
	    
		out.println("</FORM>");
		out.println("</BODY></HTML>");
	}
	
	public void doGet(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
		
		doPost(request, response);
	}
	
	private void List_Criteria(PrintWriter pwOut, 
							   String sConf,
							   String sUser,
							   String sUserID,
							   String sUserFullName,
							   HttpServletRequest req) throws Exception{

	    pwOut.println("<TABLE BORDER=1 CELLSPACING=2>");
        
	    //First the starting and ending dates:
	    pwOut.println("<TR"
	    	+ " style=\"background-color:grey; color:white; \">"
	    	+ "<TD>"
			+ "<B>&nbsp;DATE RANGE</B>"
			+ "</TD>"
			+ "</TR>");
	    pwOut.println("<TR>"
	    	+ "<TD>"
	    	+ "Starting with date:&nbsp;"
	    	+ clsCreateHTMLFormFields.TDTextBox(
	    		STARTING_DATE_FIELD, 
	    		"00/00/0000", 
	    		10, 
	    		10, 
	    		""
	    	)
	    	+ SMUtilities.getDatePickerString(STARTING_DATE_FIELD, getServletContext())
	    	+ "&nbsp;&nbsp;"
	    	+ " and ending with date:&nbsp;"
	    	+ clsCreateHTMLFormFields.TDTextBox(
	    		ENDING_DATE_FIELD, 
	    		"00/00/0000", 
	    		10, 
	    		10, 
	    		""
	    	)
	    	+ SMUtilities.getDatePickerString(ENDING_DATE_FIELD, getServletContext())
	    	+ "</TD>"
	    	+ "</TR>"
	    );
	    
	    //Next OPTIONALLY list the jurisdictions that can be selected as checkboxes:
	    pwOut.println("<TR"
		    	+ " style=\"background-color:grey; color:white; \">"
		    	+ "<TD>"
				+ "<B>&nbsp;TAX JURISDICTIONS</B>"
				+ "</TD>"
				+ "</TR>");
	    ArrayList<String>arrTaxJurisdictionCheckboxes = new ArrayList<String>(0);
	    
	    String SQL = "SELECT"
	    	+ " DISTINCT "
	    	+ SMTabletax.staxjurisdiction
	    	+ " FROM " + SMTabletax.TableName
	    	+ " ORDER BY " + SMTabletax.staxjurisdiction
	    ;
	    try {
			ResultSet rsTaxJurisdictions = clsDatabaseFunctions.openResultSet(
				SQL, 
				getServletContext(), 
				sConf, 
				"MySQL", 
				SMUtilities.getFullClassName(this.toString()) + ".List_Criteria - user: " 
				+ sUserID
				+ " -  "
				+ sUserFullName);
			while (rsTaxJurisdictions.next()){
				String sTaxJurisdiction = rsTaxJurisdictions.getString(SMTabletax.staxjurisdiction);
				String sTaxJurisdictionMarker = TAX_JURISDICTION_CHECKBOX_STUB + sTaxJurisdiction;
				//If there's a query parameter coming back with this jurisdiction checked, then check this one:
				String sChecked = "";
				if (clsManageRequestParameters.get_Request_Parameter(sTaxJurisdictionMarker, req).compareToIgnoreCase("") != 0){
					sChecked = " CHECKED ";
				}

				arrTaxJurisdictionCheckboxes.add(
					(String) "<LABEL NAME='LABEL" + sTaxJurisdictionMarker + "'>"
					+ "<INPUT TYPE=CHECKBOX" 
					+ sChecked
					+ " NAME=\"" + sTaxJurisdictionMarker + "\""
					+ " VALUE=\"" + sTaxJurisdictionMarker + "\"" 
					+ "\">" 
					+ sTaxJurisdiction
					+ "</LABEL>"
					+ "\n"
				);
			}
			rsTaxJurisdictions.close();
		} catch (Exception e1) {
			pwOut.println(
				"<BR><B><FONT COLOR=RED>Error [1454965679] reading tax jurisdictions with SQL: '" 
				+ SQL + "' - " + e1.getMessage() + "</FONT></B><BR>");
		}
	    pwOut.println("<TR><TD>");
	    pwOut.println(SMUtilities.Build_HTML_Table(2, arrTaxJurisdictionCheckboxes, 100, 1, true ,true));
	    pwOut.println("</TD></TR>");
	    
	    //Next OPTIONALLY list the cost centers to be selected:
	    pwOut.println("<TR"
		    	+ " style=\"background-color:grey; color:white; \">"
		    	+ "<TD>"
				+ "<B>&nbsp;COST CENTERS</B>"
				+ "</TD>"
				+ "</TR>");
	    
	    pwOut.println("<TR><TD>");
	    String sUseCostCenterIsChecked = "";
	    String sDontUseCostCenterIsChecked = "";
	    if (clsManageRequestParameters.get_Request_Parameter(USE_COST_CENTERS, req).compareToIgnoreCase(USE_COST_CENTERS_TRUE) == 0){
	    	sUseCostCenterIsChecked = " CHECKED ";
	    	sDontUseCostCenterIsChecked = "";
	    }else{
	    	sUseCostCenterIsChecked = "";
	    	sDontUseCostCenterIsChecked = " CHECKED ";
	    }
	    pwOut.println("&nbsp;"
	    	+ "<LABEL NAME='LABELUSECOSTCENTERS'>"
	    	+ "<input type=\"radio\" name=\"" + USE_COST_CENTERS + "\" value=\"" 
			+ USE_COST_CENTERS_FALSE + "\"" + sDontUseCostCenterIsChecked + ">" 
	    	+ "Do NOT group by cost center (IGNORE cost centers and just include them all)"
			+ "</LABEL>"
	    );
	    pwOut.println("</TD></TR>");
	    
	    ArrayList<String>arrCostCenterCheckboxes = new ArrayList<String>(0);
	    
	    SQL = "SELECT"
	    	+ " * "
	    	+ " FROM " + SMTablecostcenters.TableName
	    	+ " ORDER BY " + SMTablecostcenters.scostcentername
	    ;
	    ResultSet rsCostCenters;
		try {
			rsCostCenters = clsDatabaseFunctions.openResultSet(
				SQL, 
				getServletContext(), 
				sConf, 
				"MySQL", 
				SMUtilities.getFullClassName(this.toString()) + ".List_Criteria - user: " + sUserID
				+ " - "
				+ sUserFullName
					);
			while (rsCostCenters.next()){
				String sCostCenterName = rsCostCenters.getString(SMTablecostcenters.scostcentername);
				String sCostCenterMarker = COST_CENTER_CHECKBOX_STUB + sCostCenterName;
				//If there's a query parameter coming back with this cost center checked, then check this one:
				String sChecked = "";
				if (clsManageRequestParameters.get_Request_Parameter(sCostCenterMarker, req).compareToIgnoreCase("") != 0){
					sChecked = " CHECKED ";
				}
				arrCostCenterCheckboxes.add(
					(String) "<LABEL NAME='USECOSTCENTER" + sCostCenterName + "'>"
					+ "<INPUT TYPE=CHECKBOX" + sChecked
					+ " NAME=\"" + sCostCenterMarker + "\""
					+ " VALUE=\"" + sCostCenterMarker + "\"" 
					+ "\">" 
					+ sCostCenterName
					+ "</LABEL>"
					+ "\n"
				);
			}
			rsCostCenters.close();
		} catch (Exception e1) {
			pwOut.println(
					"<BR><B><FONT COLOR=RED>Error [1454965680] reading cost centers with SQL: '" 
					+ SQL + "' - " + e1.getMessage() + "</FONT></B><BR>");
		}
		arrCostCenterCheckboxes.add(
				(String) "<LABEL NAME='USECOSTCENTER(Unassigned)'>"
				+ "<INPUT TYPE=CHECKBOX NAME=\"COSTCENTERCHK(Unassigned)\""
				+ " VALUE=\"COSTCENTERCHK(Unassigned)\"" 
				+ "\">(Unassigned)</LABEL>"
				+ "\n"
			);
		
	    pwOut.println("<TR><TD>");
	    pwOut.println("&nbsp;"
	    		+ "<LABEL NAME='LABELUSECOSTCENTERS'>"
		    	+ "<input type=\"radio\" name=\"" + USE_COST_CENTERS + "\" value=\"" 
				+ USE_COST_CENTERS_TRUE + "\"" + sUseCostCenterIsChecked + ">" 
		    	+ "Include and group report by the selected cost centers below:"
				+ "</LABEL>"
		    	+ "<BR>"
		    );
	    
	    pwOut.println(SMUtilities.Build_HTML_Table(4, arrCostCenterCheckboxes, 100, 0, true ,true));
	    pwOut.println("</TD></TR>");
	    
	    pwOut.println("<TR"
		    	+ " style=\"background-color:grey; color:white; \">"
		    	+ "<TD>"
				+ "<B>&nbsp;DETAIL OPTIONS</B>"
				+ "</TD>"
				+ "</TR>");
	    
        //Include individual invoice totals?
	    String sDetailInvoiceLineIsChecked = " CHECKED ";
	    String sDetailSummaryOnlyIsChecked = "";
	    if (clsManageRequestParameters.get_Request_Parameter(DETAIL_OPTIONS, req).compareToIgnoreCase(DETAIL_OPTIONS_SHOW_INVOICE_LINES) == 0){
	    	sDetailInvoiceLineIsChecked = " CHECKED ";
	    }
	    //if (SMUtilities.get_Request_Parameter(DETAIL_OPTIONS, req).compareToIgnoreCase(DETAIL_OPTIONS_SHOW_INVOICE_HEADERS) == 0){
	    //	sDetailInvoiceTotalIsChecked = " CHECKED ";
	    //}
	    if (clsManageRequestParameters.get_Request_Parameter(DETAIL_OPTIONS, req).compareToIgnoreCase(DETAIL_OPTIONS_SHOW_TOTALS_ONLY) == 0){
	    	sDetailSummaryOnlyIsChecked = " CHECKED ";
	    }
	    
	    pwOut.println("<TR><TD>");
	    pwOut.println("&nbsp;"
	    	+ "<LABEL NAME='LABELSHOWINVOICELINES'>"
	    	+ "<input ONCHANGE = \"visible();\" type=\"radio\" id = \"invoicechecked\" name=\"" + DETAIL_OPTIONS + "\" value=\"" 
			+ DETAIL_OPTIONS_SHOW_INVOICE_LINES + "\"" + sDetailInvoiceLineIsChecked + ">" 
	    	+ "List each invoice LINE"
			+ "</LABEL>\n"
	    	+ " <BR>\n"
	    	+ "<div  id = \"itemId\" >\n"
	    	+ "&nbsp;&nbsp&nbsp;Enter the item number in the textbox if you want to view a particular item or leave it blank to view ALL items:"
	    	+ "&nbsp;<input type = \"text\" name = \""+ENTER_ITEM_NUMBER_TEXTBOX+"\" id = \"textBoxId\" value = \"\">\n"
	    	+ "<BR>"
	    	+ "&nbsp;&nbsp&nbsp;Enter the order number in the textbox if you want to view a particular order or leave it blank to view ALL orders:"
	    	+ "&nbsp;<input type = \"text\" name = \""+ENTER_ORDER_NUMBER_TEXTBOX+"\" id = \"textBoxId\" value = \"\">\n"
	    	+ "</div><BR>"
	    	+ "&nbsp;"
	    	+ "<LABEL NAME = 'LABELSHOWTOTALSONLY'>"
	    	+ "<input ONCHANGE = \"invisible();\" type=\"radio\" name=\"" + DETAIL_OPTIONS + "\" value=\"" 
			+ DETAIL_OPTIONS_SHOW_TOTALS_ONLY + "\"" + sDetailSummaryOnlyIsChecked + ">" 
	    	+ "Show summary only"
			+ "</LABEL>"
	    	+ "<BR>"
		    );
	    	
	    pwOut.println("</TD></TR>");
	    
        pwOut.println("</TABLE>");
        
        //pwOut.println("<BR>");
        pwOut.println("<P><INPUT TYPE=SUBMIT NAME='SubmitPrint' VALUE='Print' STYLE='height: 0.24in'></P>");
  
	}
	
	String getJavascript(){
		String s = "";
		s = "<script type= 'text/JavaScript'>\n"
				+" function invisible(){\n"
	    		+ "       document.getElementById(\"itemId\").style.display = \"none\";\n"
	    		+ "       document.getElementById(\"textBoxId\").value = \"\";\n"
	    		//+ "       document.getElementById(\"itemId\").children.value = \"\"\n;"
	    		+ " }\n"
	    		+"  $(document).ready(function(){\n"
	    		+"        if(document.getElementById(\"invoicechecked\").checked == false){\n"
	    		+ "       document.getElementById(\"itemId\").style.display = \"none\";\n"
	    		+ "       document.getElementById(\"textBoxId\").value = \"\";\n"
	    		+"         }\n   "
	    		+"       });\n"
	    		+ " function visible () {\n"
	    		+ "       document.getElementById(\"itemId\").style.display = \"block\";\n  "
	    		+ " }\n"
	    		+ " </script>\n";
		return s;
	}
}
