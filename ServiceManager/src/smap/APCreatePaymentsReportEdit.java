package smap;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import ConnectionPool.WebContextParameters;
import SMDataDefinition.SMMasterStyleSheetDefinitions;
import SMDataDefinition.SMTableapaccountsets;
import SMDataDefinition.SMTableapvendorgroups;
import SMDataDefinition.SMTablebkbanks;
import SMDataDefinition.SMTableicvendors;
import ServletUtilities.clsDBServerTime;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsManageRequestParameters;
import smcontrolpanel.SMAuthenticate;
import smcontrolpanel.SMSystemFunctions;
import smcontrolpanel.SMUtilities;

public class APCreatePaymentsReportEdit  extends HttpServlet {

	public static final String PARAM_PAYMENT_DATE = "PARAMPAYMENTDATE";
	public static final String PARAM_BATCH_DATE = "PARAMBATCHDATE";
	public static final String PARAM_BANK_ID = "PARAMBANK";
	
	public static final String PARAM_SELECT_DOCUMENTS_BY = "PARAMSELECTDOCUMENTSBY";
	public static final String PARAM_SELECT_DOCUMENTS_BY_DUE_DATE = "PARAMSELECTDOCUMENTSBYDUEDATE";
	public static final String PARAM_SELECT_DOCUMENTS_BY_DISCOUNT_DATE = "PARAMSELECTDOCUMENTSBYDISCOUNTDATE";
	public static final String PARAM_SELECT_DOCUMENTS_BY_DUE_OR_DISCOUNT_DATE = "PARAMSELECTDOCUMENTSBYDUEORDISCOUNTDATE";
	public static final String SELECT_DOCUMENTS_BY_DUE_DATE_LABEL = "Due Date ONLY";
	public static final String SELECT_DOCUMENTS_BY_DISCOUNT_DATE_LABEL = "Discount Date Range ONLY";
	public static final String SELECT_DOCUMENTS_BY_DUE_OR_DISCOUNT_DATE_LABEL = "Due Date OR Discount Date Range";
	
	private static final String DUE_DATE_DIV_NAME = "DUEDATEDIV";
	private static final String DISCOUNT_DATE_STARTING_DIV_NAME = "DISCOUNTDATESTARTINGDATEDIV";
	private static final String DISCOUNT_DATE_ENDING_DIV_NAME = "DISCOUNTDATEENDINGDATEDIV";
	public static final String PARAM_DUE_DATE = "PARAMDUEDATE";
	public static final String PARAM_STARTING_DISCOUNT_DATE = "PARAMSTARTINGDISCOUNTDATE";
	public static final String PARAM_ENDING_DISCOUNT_DATE = "PARAMENDINGDISCOUNTDATE";
	public static final String PARAM_STARTING_VENDOR_GROUP_NAME = "PARAMSTARTINGVENDORGROUP";
	public static final String PARAM_ENDING_VENDOR_GROUP_NAME = "PARAMENDINGVENDORGROUP";
	public static final String PARAM_STARTING_ACCOUNT_SET_NAME = "PARAMSTARTINGACCOUNTSET";
	public static final String PARAM_ENDING_ACCOUNT_SET_NAME = "PARAMENDINGACCOUNTSET";
	public static final String PARAM_STARTING_INVOICE_AMT = "PARAMSTARTINGPAYMENTAMOUNT";
	public static final String PARAM_ENDING_INVOICE_AMT = "PARAMENDINGPAYMENTAMOUNT";
	public static final String PARAM_STARTING_VENDOR = "PARAMSTARTINGVENDOR";
	public static final String PARAM_ENDING_VENDOR = "PARAMENDINGVENDOR";
	
	private static final String SUBMIT_EDIT_BUTTON_NAME = "SUBMITBUTTON";
	private static final String SUBMIT_EDIT_BUTTON_LABEL = "Create report";
	
	private static final long serialVersionUID = 1L;

	public void doPost(HttpServletRequest request,
			HttpServletResponse response)
	throws ServletException, IOException {
		
		PrintWriter out = response.getWriter();
	    if (!SMAuthenticate.authenticateSMCPCredentials(request, response, getServletContext(), SMSystemFunctions.APEditBatches)){
	    	return;
	    }
		//Get the session info:
		HttpSession CurrentSession = request.getSession(true);
		String sDBID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_DATABASE_ID);
		String sUserName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERNAME);
		String sUserID = (String)CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERID);
		String sUserFullName = (String)CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERFIRSTNAME) + " "
								+ (String)CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERLASTNAME);
		String sCompanyName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_COMPANYNAME);
		String title = "Auto-create Payment Batch";
		String subtitle = "";
		out.println(SMUtilities.SMCPTitleSubBGColor(title, subtitle, SMUtilities.getInitBackGroundColor(getServletContext(), sDBID), sCompanyName));
		out.println(SMUtilities.getDatePickerIncludeString(getServletContext()));
		out.println(SMUtilities.getSMCPJSIncludeString(getServletContext()));
		out.println(SMUtilities.getMasterStyleSheetLink());
		out.println(sCommandScript());

		if (clsManageRequestParameters.get_Request_Parameter("Warning", request).compareToIgnoreCase("") != 0){
			out.println("<B><FONT COLOR=\"RED\">WARNING: " + clsManageRequestParameters.get_Request_Parameter("Warning", request) + "</FONT></B><BR>");
		}
		if (clsManageRequestParameters.get_Request_Parameter("Status", request).compareToIgnoreCase("") != 0){
			out.println("<BR><B>NOTE: " + clsManageRequestParameters.get_Request_Parameter("Status", request) + "</B><BR>");
		}
		//Print a link to the first page after login:
		out.println("<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMUserLogin?" 
				+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
				+ "\">Return to user login</A><BR>");
		//Print a link to main menu:
		out.println("<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smap.APMainMenu?" 
				+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
				+ "\">Return to Accounts Payable Main Menu</A><BR>");
		out.println("<A HREF=\"" + WebContextParameters.getdocumentationpageURL(getServletContext()) + "#" + Long.toString(SMSystemFunctions.APAgedPayables)
				+ "\">Summary</A><BR>");

		//Start the input form:
		out.println ("\n<FORM ACTION =\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smap.APCreatePaymentsReportGenerate\" METHOD=\"POST\" >\n");
		
		out.println("<INPUT TYPE=HIDDEN NAME=\"" + "CallingClass" + "\" VALUE=\"" 
			+ SMUtilities.getFullClassName(this.toString()) + "\">");
		
		out.println("<INPUT TYPE=HIDDEN NAME='" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "' VALUE='" + sDBID + "'>");
		
		String s = "";
		
		//Start the table:
		s += "<BR>\n<TABLE style=\" border-style:solid; border-color:black; font-size:small; \">\n";
		
		//Add the fields:
		s += "  <TR>\n";
		
		// ****** Payment date
		clsDBServerTime servertime = null;
		try {
			servertime = new clsDBServerTime(sDBID,sUserName, getServletContext());
		} catch (Exception e) {
			s += "<BR><FONT COLOR=RED>Error [1498660858] getting current server time - " + e.getMessage() + "</FONT><BR>";
		}
		String sPaymentDate = servertime.getCurrentDateTimeInSelectedFormat(SMUtilities.DATE_FORMAT_FOR_DISPLAY);
		s +="    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD   + "\" >Payment&nbsp;date:&nbsp;</TD>\n";
		s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER   + "\" >";
		s += "<INPUT TYPE=TEXT NAME=\"" + PARAM_PAYMENT_DATE + "\""
    	    + " VALUE=\"" + sPaymentDate + "\""
    	    + " MAXLENGTH=" + "10"
    	    + " SIZE = " + "8"
    	    + " onchange=\"flagDirty();\""
    	    + ">"
    	    + "&nbsp;" + SMUtilities.getDatePickerString(PARAM_PAYMENT_DATE, getServletContext())
    	;
		s += "    </TD>\n";
		
		
		// ****** Batch date
		String sBatchDate = servertime.getCurrentDateTimeInSelectedFormat(SMUtilities.DATE_FORMAT_FOR_DISPLAY);
		s +="    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD   + "\" >Batch&nbsp;date:&nbsp;</TD>\n";
		s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER   + "\" >";
		s += "<INPUT TYPE=TEXT NAME=\"" + PARAM_BATCH_DATE + "\""
    	    + " VALUE=\"" + sBatchDate + "\""
    	    + " MAXLENGTH=" + "10"
    	    + " SIZE = " + "8"
    	    + " onchange=\"flagDirty();\""
    	    + ">"
    	    + "&nbsp;" + SMUtilities.getDatePickerString(PARAM_BATCH_DATE, getServletContext())
    	;
		s += "    </TD>\n";
		
		s += "  </TR>\n";
		s += "  <TR>\n";
		
		// ****** Pay from bank
		String SQL = "";
		ArrayList<String>arrBankIDs = new ArrayList<String>(0);
		ArrayList<String>arrBankDescriptions = new ArrayList<String>(0);
		 SQL = "SELECT"
			+ " " + SMTablebkbanks.lid
			+ ", " + SMTablebkbanks.saccountname
			+ " FROM " + SMTablebkbanks.TableName
			+ " ORDER BY " + SMTablebkbanks.lid 
		;
		//First, add a bank account so we can be sure the user chose one:
		 //arrBankIDs.add("");
		 //arrBankDescriptions.add("*** Select bank account ***");
				
		try {
			ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, getServletContext(),
				sDBID, "MySQL", SMUtilities.getFullClassName(this.toString())
					+ ".getEditHTML - user: " + sUserID
					+ " - "
					+ sUserFullName
					);
			while (rs.next()) {
				arrBankIDs.add(rs.getString(SMTablebkbanks.lid));
				arrBankDescriptions.add(rs.getString(SMTablebkbanks.saccountname)
				);
			}
			rs.close();
		} catch (SQLException e) {
			s += "<BR><B><FONT COLOR=RED>Error [1498661579] reading bank account codes - " + e.getMessage() + ".</FONT></B><BR>";
		}
		s +="    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD   + "\" >Bank:&nbsp;</TD>\n";
		s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER   + "\" COLSPAN=3>";
		s += "<SELECT"
			+ " NAME = \"" + PARAM_BANK_ID + "\""
			+ " ID = \"" + PARAM_BANK_ID + "\""
			+ " >\n"
		;
		for (int i = 0; i < arrBankIDs.size(); i++){
			s += "<OPTION";
			if (arrBankIDs.get(i).toString().compareTo(clsManageRequestParameters.get_Request_Parameter(PARAM_BANK_ID, request)) == 0){
				s += " selected=yes";
			}
			s += " VALUE=\"" + arrBankIDs.get(i).toString() + "\">"
			+ arrBankDescriptions.get(i).toString() + "\n";
		}
		s += "</SELECT>";
		s += "&nbsp;<B><I><FONT COLOR=RED>NOTE:</FONT> ONLY vendors using this bank will be included.</I></B>";
		s += "    </TD>\n";
		
		s += "  </TR>\n";
		s += "  <TR>\n";
		
		// ****** Select documents by: 1) Due Date, 2) Discount Date, 3) Due date OR discount date
		s +="    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD   + "\" >Select documents by:</TD>\n";
		s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER   + "\" >";
		s += "<SELECT"
			+ " NAME = \"" + PARAM_SELECT_DOCUMENTS_BY + "\""
			+ " ID = \"" + PARAM_SELECT_DOCUMENTS_BY + "\""	
			+ " onchange=\"displayDateFields();\""
			+ " >\n"
		;
		s += "<OPTION VALUE=\"" + PARAM_SELECT_DOCUMENTS_BY_DUE_DATE + "\">" + SELECT_DOCUMENTS_BY_DUE_DATE_LABEL + "\n";
		s += "<OPTION VALUE=\"" + PARAM_SELECT_DOCUMENTS_BY_DISCOUNT_DATE + "\">" + SELECT_DOCUMENTS_BY_DISCOUNT_DATE_LABEL + "\n";
		s += "<OPTION VALUE=\"" + PARAM_SELECT_DOCUMENTS_BY_DUE_OR_DISCOUNT_DATE + "\" selected=yes >" + SELECT_DOCUMENTS_BY_DUE_OR_DISCOUNT_DATE_LABEL + "\n";
		s += "</SELECT>";
		s += "    </TD>\n";
		
		//Blank columns:
		s += "    <TD>&nbsp;    </TD>\n";
		s += "    <TD>&nbsp;    </TD>\n";
		
		s += "  </TR>\n";
		
		// ****** DUE on or before
		s += "  <TR>\n";
		s +="    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD   + "\" >Due on or before:&nbsp;</TD>\n";
		s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER   + "\" >";
		s += "<DIV NAME = \"" + DUE_DATE_DIV_NAME + "\" ID = \"" + DUE_DATE_DIV_NAME + "\" >\n";
		s += "<INPUT TYPE=TEXT NAME=\"" + PARAM_DUE_DATE + "\""
		        + " VALUE=\"" + servertime.getCurrentDateTimeInSelectedFormat(SMUtilities.DATE_FORMAT_FOR_DISPLAY) + "\""
		        + " MAXLENGTH=" + "10"
		        + " SIZE = " + "8"
		        + ">"
		    + "&nbsp;" + SMUtilities.getDatePickerString(PARAM_DUE_DATE, getServletContext());
		s += "\n</DIV>\n";
		s += "    </TD>\n";
		s += "  </TR>\n";
		
		s += "  <TR>\n";
		
		// ****** Discounts available FROM (starting discount date) TO (ending discount date)
		s +="    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD   + "\" >Discount dates from:&nbsp;</TD>\n";
		s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER   + "\" >";
		s += "<DIV NAME = \"" + DISCOUNT_DATE_STARTING_DIV_NAME + "\" ID = \"" + DISCOUNT_DATE_STARTING_DIV_NAME + "\" >\n";
		s += "<INPUT TYPE=TEXT NAME=\"" + PARAM_STARTING_DISCOUNT_DATE + "\""
	        + " VALUE=\"" + servertime.getCurrentDateTimeInSelectedFormat(SMUtilities.DATE_FORMAT_FOR_DISPLAY) + "\""
	        + " MAXLENGTH=" + "10"
	        + " SIZE = " + "8"
	        + ">\n"
	    + "&nbsp;" + SMUtilities.getDatePickerString(PARAM_STARTING_DISCOUNT_DATE, getServletContext());
	    s += "\n</DIV>\n";
		;
		
		s +="    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD   + "\" >To:&nbsp;</TD>\n";
		s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER   + "\" >";
		s += "<DIV NAME = \"" + DISCOUNT_DATE_ENDING_DIV_NAME + "\" ID = \"" + DISCOUNT_DATE_ENDING_DIV_NAME + "\" >\n";
		s += "<INPUT TYPE=TEXT NAME=\"" + PARAM_ENDING_DISCOUNT_DATE + "\""
	        + " VALUE=\"" + servertime.getCurrentDateTimeInSelectedFormat(SMUtilities.DATE_FORMAT_FOR_DISPLAY) + "\""
	        + " MAXLENGTH=" + "10"
	        + " SIZE = " + "8"
	        + ">\n"
	        + "&nbsp;" + SMUtilities.getDatePickerString(PARAM_ENDING_DISCOUNT_DATE, getServletContext())
		;
		s += "\n</DIV>\n";
		s += "  </TR>\n";
		
		s += "  <TR>\n";
		
		// ------ Vendor selection range ------
		s += "  <TR style=\"background-color:grey; color:white; \" ><TD COLSPAN=4><B>&nbsp;VENDOR SELECTION RANGE:</B></TD></TR>\n";
		
		// ****** Vendor GROUP (from and to)
		s += "  <TR>\n";
		SQL = "";
		ArrayList<String>arrVendorGroupNames = new ArrayList<String>(0);
		ArrayList<String>arrVendorGroupDescriptions = new ArrayList<String>(0);
		 SQL = "SELECT"
			+ " " + SMTableapvendorgroups.lid
			+ ", " + SMTableapvendorgroups.sgroupid
			+ ", " + SMTableapvendorgroups.sdescription
			+ " FROM " + SMTableapvendorgroups.TableName
			+ " ORDER BY " + SMTableapvendorgroups.sgroupid
		;
		try {
			ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, getServletContext(),
				sDBID, "MySQL", SMUtilities.getFullClassName(this.toString())
					+ ".getEditHTML - getting vendor groups - user: " + sUserID
					+ " - "
					+ sUserFullName
					);
			while (rs.next()) {
				arrVendorGroupNames.add(rs.getString(SMTableapvendorgroups.sgroupid));
				arrVendorGroupDescriptions.add(rs.getString(SMTableapvendorgroups.sgroupid) + " - " + rs.getString(SMTableapvendorgroups.sdescription));
			}
			rs.close();
		} catch (SQLException e) {
			s += "<BR><B><FONT COLOR=RED>Error [1498763704] reading vendor groups - " + e.getMessage() + ".</FONT></B><BR>";
		}
		s +="    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD   + "\" >Starting with vendor group:&nbsp;</TD>\n";
		s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER   + "\" >";
		s += "<SELECT"
			+ " NAME = \"" + PARAM_STARTING_VENDOR_GROUP_NAME + "\""
			+ " ID = \"" + PARAM_STARTING_VENDOR_GROUP_NAME + "\""
			+ " >\n"
		; 
		for (int i = 0; i < arrVendorGroupNames.size(); i++){
			s += "<OPTION";
			if (arrVendorGroupNames.get(i).toString().compareTo(clsManageRequestParameters.get_Request_Parameter(PARAM_STARTING_VENDOR_GROUP_NAME, request)) == 0){
				s += " selected=yes";
			}
			s += " VALUE=\"" + arrVendorGroupNames.get(i).toString() + "\">" 
			+ arrVendorGroupDescriptions.get(i).toString() + "\n";
		}
		s += "</SELECT>";
		s += "    </TD>\n";
		
		//TO vendor groups:
		s +="    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD   + "\" >Ending with vendor group:&nbsp;</TD>\n";
		s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER   + "\" >";
		s += "<SELECT"
			+ " NAME = \"" + PARAM_ENDING_VENDOR_GROUP_NAME + "\""
			+ " ID = \"" + PARAM_ENDING_VENDOR_GROUP_NAME + "\""
			+ " >\n"
		; 
		for (int i = 0; i < arrVendorGroupNames.size(); i++){
			s += "<OPTION";
			if (i == arrVendorGroupNames.size() - 1){
				s += " selected=yes";
			}
			s += " VALUE=\"" + arrVendorGroupNames.get(i).toString() + "\">" 
			+ arrVendorGroupDescriptions.get(i).toString() + "\n";
		}
		s += "</SELECT>";
		s += "    </TD>\n";
		
		s += "  </TR>\n";
		
		// ****** Vendor NUMBER (from and to)
		s += "  <TR>\n";
		s +="    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD   + "\" >Starting with vendor:&nbsp;</TD>\n";
		s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER   + "\" >";
		s += "<INPUT TYPE=TEXT NAME=\"" + PARAM_STARTING_VENDOR + "\""
	        + " VALUE=\"" + "" + "\""
	        + " MAXLENGTH=" + Integer.toString(SMTableicvendors.svendoracctLength)
	        + " SIZE = " + "15"
	        + ">\n"
		;
		s += "    </TD>\n";

		s +="    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD   + "\" >Ending with vendor:&nbsp;</TD>\n";
		s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER   + "\" >";
		s += "<INPUT TYPE=TEXT NAME=\"" + PARAM_ENDING_VENDOR + "\""
	        + " VALUE=\"" + "ZZZZZZZZZZZZ" + "\""
	        + " MAXLENGTH=" + Integer.toString(SMTableicvendors.svendoracctLength)
	        + " SIZE = " + "15"
	        + ">\n"
		;
		s += "    </TD>\n";

		s += "  </TR>\n";
		
		
		// ****** Account set (from and to)
		s += "  <TR>\n";
		SQL = "";
		ArrayList<String>arrAccountSetNames = new ArrayList<String>(0);
		ArrayList<String>arrAccountSetDescriptions = new ArrayList<String>(0);
		 SQL = "SELECT"
			+ " " + SMTableapaccountsets.lid
			+ ", " + SMTableapaccountsets.sacctsetname
			+ ", " + SMTableapaccountsets.sdescription
			+ " FROM " + SMTableapaccountsets.TableName
			+ " ORDER BY " + SMTableapaccountsets.sacctsetname
		;
		try {
			ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, getServletContext(),
				sDBID, "MySQL", SMUtilities.getFullClassName(this.toString())
					+ ".getEditHTML - getting account sets - user: " + sUserID
					+ " - "
					+ sUserFullName
					);
			while (rs.next()) {
				arrAccountSetNames.add(rs.getString(SMTableapaccountsets.sacctsetname));
				arrAccountSetDescriptions.add(rs.getString(SMTableapaccountsets.sacctsetname) + " - " + rs.getString(SMTableapaccountsets.sdescription));
			}
			rs.close();
		} catch (SQLException e) {
			s += "<BR><B><FONT COLOR=RED>Error [1498763705] reading AP Account Sets - " + e.getMessage() + ".</FONT></B><BR>";
		}
		s +="    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD   + "\" >Starting with account set:&nbsp;</TD>\n";
		s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER   + "\" >";
		s += "<SELECT"
			+ " NAME = \"" + PARAM_STARTING_ACCOUNT_SET_NAME + "\""
			+ " ID = \"" + PARAM_STARTING_ACCOUNT_SET_NAME + "\""
			+ " >\n"
		; 
		for (int i = 0; i < arrAccountSetNames.size(); i++){
			s += "<OPTION";
			if (arrAccountSetNames.get(i).toString().compareTo(clsManageRequestParameters.get_Request_Parameter(PARAM_STARTING_ACCOUNT_SET_NAME, request)) == 0){
				s += " selected=yes";
			}
			s += " VALUE=\"" + arrAccountSetNames.get(i).toString() + "\">" 
			+ arrAccountSetDescriptions.get(i).toString() + "\n";
		}
		s += "</SELECT>";
		s += "    </TD>\n";
		
		//TO account sets:
		s +="    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD   + "\" >Ending with account set:&nbsp;</TD>\n";
		s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER   + "\" >";
		s += "<SELECT"
			+ " NAME = \"" + PARAM_ENDING_ACCOUNT_SET_NAME + "\""
			+ " ID = \"" + PARAM_ENDING_ACCOUNT_SET_NAME + "\""
			+ " >\n"
		; 
		for (int i = 0; i < arrAccountSetNames.size(); i++){
			s += "<OPTION";
			if (i == arrAccountSetNames.size() - 1){
				s += " selected=yes";
			}
			s += " VALUE=\"" + arrAccountSetNames.get(i).toString() + "\">" 
			+ arrAccountSetDescriptions.get(i).toString() + "\n";
		}
		s += "</SELECT>";
		s += "    </TD>\n";
		
		s += "  </TR>\n";
		
		
		// ****** Vendor AMOUNTS (from and to)
		s += "  <TR>\n";
		s +="    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD   + "\" >Starting with current invoice amount:&nbsp;</TD>\n";
		s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER   + "\" >";
		s += "<INPUT TYPE=TEXT NAME=\"" + PARAM_STARTING_INVOICE_AMT + "\""
	        + " VALUE=\"" + "0.01" + "\""
	        + " MAXLENGTH=" + "16"
	        + " SIZE = " + "12"
	        + ">\n"
		;
		s += "    </TD>\n";

		s +="    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD   + "\" >Up to current invoice amount:&nbsp;</TD>\n";
		s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER   + "\" >";
		s += "<INPUT TYPE=TEXT NAME=\"" + PARAM_ENDING_INVOICE_AMT + "\""
	        + " VALUE=\"" + "9,999,999,999.00" + "\""
	        + " MAXLENGTH=" + "16"
	        + " SIZE = " + "12"
	        + ">\n"
		;
		s += "    </TD>\n";

		s += "  </TR>\n";
		
		
		// ****** Payment code (from and to) - not used???
		
		
		
		//End the table:
		s += "</TABLE>\n";
		
		s += "<INPUT TYPE=SUBMIT NAME='" 
			+ SUBMIT_EDIT_BUTTON_NAME 
			+ "' VALUE='" + SUBMIT_EDIT_BUTTON_LABEL 
			+ "' STYLE='height: 0.24in'>"
		;
		
		out.println(s);
		
		//End the input form:
		out.println ("\n</FORM>\n");
		
		//End the HTML:
		out.println("</BODY></HTML>");
	}
	private String sCommandScript(){
		String s = "";
		s += "<NOSCRIPT>\n"
				+ "    <font color=red>\n"
				+ "    <H3>This page requires that JavaScript be enabled to function properly</H3>\n"
				+ "    </font>\n"
				+ "</NOSCRIPT>\n"
			;
		s += "<script type='text/javascript'>\n";
		
		s += "window.onload = function() {\n"
			//+ "    calculatelinetotal();\n"
			+ "}\n"
		;
		
   		s += "function displayDateFields(){\n"
   			+ "    var objSelectByList = document.getElementById(\"" + PARAM_SELECT_DOCUMENTS_BY + "\");\n"  //block, //inline
   			+ "    if (objSelectByList.options[objSelectByList.selectedIndex].text == '" + SELECT_DOCUMENTS_BY_DUE_DATE_LABEL + "' ){\n"
   			+ "        document.getElementById(\"" + DISCOUNT_DATE_STARTING_DIV_NAME + "\").style.display = \"none\";\n"
   			+ "        document.getElementById(\"" + DISCOUNT_DATE_ENDING_DIV_NAME + "\").style.display = \"none\";\n"
   			+ "        document.getElementById(\"" + DUE_DATE_DIV_NAME + "\").style.display = \"inline\";\n"
   			+ "    }\n"

   			+ "    if (objSelectByList.options[objSelectByList.selectedIndex].text == '" + SELECT_DOCUMENTS_BY_DISCOUNT_DATE_LABEL + "' ){\n"
   			+ "        document.getElementById(\"" + DUE_DATE_DIV_NAME + "\").style.display = \"none\";\n"
   			+ "        document.getElementById(\"" + DISCOUNT_DATE_STARTING_DIV_NAME + "\").style.display = \"inline\";\n"
   			+ "        document.getElementById(\"" + DISCOUNT_DATE_ENDING_DIV_NAME + "\").style.display = \"inline\";\n"
   			+ "    }\n"
   			
   			+ "    if (objSelectByList.options[objSelectByList.selectedIndex].text == '" + SELECT_DOCUMENTS_BY_DUE_OR_DISCOUNT_DATE_LABEL + "' ){\n"
   			+ "        document.getElementById(\"" + DUE_DATE_DIV_NAME + "\").style.display = \"inline\";\n"
   			+ "        document.getElementById(\"" + DISCOUNT_DATE_STARTING_DIV_NAME + "\").style.display = \"inline\";\n"
   			+ "        document.getElementById(\"" + DISCOUNT_DATE_ENDING_DIV_NAME + "\").style.display = \"inline\";\n"
   			+ "    }\n" 

   			+ "}\n"
   		;
		
   		s += "</script>\n";
   		
   		return s;
	}
	public void doGet(HttpServletRequest request,
			HttpServletResponse response)
	throws ServletException, IOException {

		doPost(request, response);
	}
}
