package smap;

import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import smar.SMOption;
import smcontrolpanel.SMAuthenticate;
import smcontrolpanel.SMSystemFunctions;
import smcontrolpanel.SMUtilities;
import ConnectionPool.WebContextParameters;
import SMDataDefinition.SMCreateGoogleDriveFolderParamDefinitions;
import SMDataDefinition.SMTableapaccountsets;
import SMDataDefinition.SMTableaptransactions;
import SMDataDefinition.SMTableapvendorgroups;
import SMDataDefinition.SMTableapvendorstatistics;
import SMDataDefinition.SMTablebkbanks;
import SMDataDefinition.SMTableglaccounts;
import SMDataDefinition.SMTableicvendors;
import SMDataDefinition.SMTableicvendorterms;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsDateAndTimeConversions;
import ServletUtilities.clsManageBigDecimals;
import ServletUtilities.clsManageRequestParameters;
import ServletUtilities.clsServletUtilities;
import SMDataDefinition.SMTableapvendorremittolocations;

public class APDisplayVendorInformation extends HttpServlet {

	private static final long serialVersionUID = 1L;
	
	//private String BACKGROUND_COLOR_LIGHT_YELLOW = "#FFFFBB";
	private String BACKGROUND_COLOR_LIGHT_BLUE = "#C2E0FF";
	private String VIEW_TRANSACTIONS_FORM_NAME = "VIEWTRANSACTIONS";
	private String VIEW_TRANSACTIONS_SUBMIT_BUTTON = "VIEWTRANSACTIONSBUTTON";
	private String VIEW_TRANSACTIONS_BUTTON_LABEL = "View Transactions";
	public static final String PARAM_VENDOR_NUMBER = "VendorNumber";
	public static final String PARAM_ORIGINATING_FROM_VENDOR_INFO_SCREEN = "ORIGINATEDFROMVENDORINFO";
	
	public void doGet(HttpServletRequest request,
				HttpServletResponse response)
				throws ServletException, IOException {
		if (!SMAuthenticate.authenticateSMCPCredentials(
				request, 
				response, 
				getServletContext(),
				SMSystemFunctions.APDisplayVendorInformation
			)
		){
			return;
		}
	    response.setContentType("text/html");
		PrintWriter out = response.getWriter();
		
	    //Get the session info:
	    HttpSession CurrentSession = request.getSession(true);
	    String sDBID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_DATABASE_ID);
	    String sUserID  = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERID);
	    String sCompanyName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_COMPANYNAME);
	    String sWarning = "";
	    String sCallingClass = clsManageRequestParameters.get_Request_Parameter("CallingClass", request);

    	String sVendorNumber = clsManageRequestParameters.get_Request_Parameter(PARAM_VENDOR_NUMBER, request);

    	//Customized title
    	String sReportTitle = "View Vendor Information";
    	out.println(SMUtilities.SMCPTitleSubBGColor(sReportTitle, "", "#FFFFFF", sCompanyName));
    	out.println("<link rel=\"stylesheet\" type=\"text/css\" href=\"styles/MasterStyleSheet.css\" media=\"screen\" />");
    	
    	out.println(clsServletUtilities.getDatePickerIncludeString(getServletContext()));
    	
    	out.println("<TD><A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMUserLogin?" 
				+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
				+ "\">Return to user login</A><BR>");
	    out.println("<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smap.APMainMenu?" 
				+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
				+ "\">Return to Accounts Payable Main Menu</A><BR>");
	    out.println("<A HREF=\"" + WebContextParameters.getdocumentationpageURL(getServletContext()) + "#" + Long.toString(SMSystemFunctions.APDisplayVendorInformation) 
	    	+ "\">Summary</A><BR>");
	    out.println("<INPUT TYPE=HIDDEN NAME='" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "' VALUE='" + sDBID + "'>");
    	
	    //save current url in url history
	    String sURLTitle = "Viewing vendor information for customer number " + sVendorNumber;
	    try {
			SMUtilities.addURLToHistory(sURLTitle, CurrentSession, request);
		} catch (Exception e) {
		}
	    out.println("<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMURLHistoryList?" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" 
				+ sDBID + "\">Return to...</A><BR>");
    	//Retrieve information
    	Connection conn = clsDatabaseFunctions.getConnection(getServletContext(), sDBID, "MySQL", "smcontrolpanel.SMDisplayOrderInformation");
    	if (conn == null){
    		sWarning = "Unable to get data connection.";
    		response.sendRedirect(
				"" + SMUtilities.getURLLinkBase(getServletContext()) + "" + sCallingClass + "?"
				+ "Warning=" + sWarning
				+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
    		);			
        	return;
    	}

    	if (!displayVendor(
    			conn, 
    			sVendorNumber, 
    			out, 
    			request,
    			(String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_LICENSE_MODULE_LEVEL),
    			sUserID,
    			sDBID)){
    		
    	}
    	
    	clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1547047700]");
    	
	    out.println("</BODY></HTML>");
	}
	private boolean displayVendor(
		Connection conn, 
		String sVendorNum, 
		PrintWriter pwOut, 
		HttpServletRequest req, 
		String sLicenseModuleLevel, 
		String sUserID, 
		String sDBID){
	
		String SQL = "SELECT * FROM "
			+ SMTableicvendors.TableName
			+ " LEFT JOIN " + SMTableapvendorgroups.TableName
			+ " ON " + SMTableicvendors.TableName + "." + SMTableicvendors.ivendorgroupid + " = " + SMTableapvendorgroups.TableName + "." + SMTableapvendorgroups.lid
			+ " WHERE ("
    			+ "(" + SMTableicvendors.TableName + "." + SMTableicvendors.svendoracct + " = '" + sVendorNum + "')"
    		+ ")"
    	;
		//System.out.println("[1509044038] SQL = " + SQL);
		try{
			ResultSet rsVendor = clsDatabaseFunctions.openResultSet(SQL, conn);
			
			if(rsVendor.next()){
				pwOut.print("<FONT SIZE=5><BR> " 
					+ rsVendor.getString(SMTableicvendors.TableName + "." + SMTableicvendors.svendoracct).trim()
					+ " - "
					+ rsVendor.getString(SMTableicvendors.TableName + "." + SMTableicvendors.sname).trim() 
					+ "&nbsp;</FONT>"
					+ "<BR>");
				
				//Links:
				boolean bAllowCreateGDriveVendorFolders = 
						SMSystemFunctions.isFunctionPermitted(
								SMSystemFunctions.SMCreateGDriveVendorFolders, 
								sUserID, 
								conn,
								sLicenseModuleLevel
						);
				
				//Start the first table:
				pwOut.println("<TABLE BORDER=0 WIDTH=100%  cellspacing=0 cellpadding=0><TR>");
				

		/*		
				pwOut.println("<TD>");
				//Default Expense account
				pwOut.print("<FONT SIZE=2><B>Default expense account:</B> " 
						+ rsVendor.getString(SMTableicvendors.sdefaultexpenseacct).trim());
				pwOut.println("</TD>");
			*/	
				//End the first row:
				pwOut.println("</TR>");

				//End the line with a blank:
				pwOut.println("<TD>");
				pwOut.print("&nbsp;");
				pwOut.println("</TD>");
				
				//End the first table:
				pwOut.println("</TABLE>");
				
				//Start the parent table:
				pwOut.println("<TABLE BORDER=1 WIDTH=100%  bgcolor=" + BACKGROUND_COLOR_LIGHT_BLUE + " cellspacing=0 cellpadding=2><TR>");

				//Start the left hand cell:
				pwOut.println("<TD>");
				//. . . .
				pwOut.println("<FONT SIZE=2><B>Address Line 1:</B> " 
						+ rsVendor.getString(SMTableicvendors.TableName + "." + SMTableicvendors.saddressline1).trim() + "<BR>");
				pwOut.println("<FONT SIZE=2><B>Address Line 2:</B> " 
						+ rsVendor.getString(SMTableicvendors.TableName + "." + SMTableicvendors.saddressline2).trim() + "<BR>");
				pwOut.println("<FONT SIZE=2><B>Address Line 3:</B> " 
						+ rsVendor.getString(SMTableicvendors.TableName + "." + SMTableicvendors.saddressline3).trim() + "<BR>");
				pwOut.println("<FONT SIZE=2><B>Address Line 4:</B> " 
						+ rsVendor.getString(SMTableicvendors.TableName + "." + SMTableicvendors.saddressline4).trim() + "<BR>");
				pwOut.println("<FONT SIZE=2><B>City:</B> " 
						+ rsVendor.getString(SMTableicvendors.TableName + "." + SMTableicvendors.scity).trim() + "<BR>");
				pwOut.println("<FONT SIZE=2><B>State:</B> " 
						+ rsVendor.getString(SMTableicvendors.TableName + "." + SMTableicvendors.sstate).trim() + "<BR>");
				pwOut.println("<FONT SIZE=2><B>Postal code:</B> " 
						+ rsVendor.getString(SMTableicvendors.TableName + "." + SMTableicvendors.spostalcode).trim() + "<BR>");
				pwOut.println("<FONT SIZE=2><B>Country:</B> " 
						+ rsVendor.getString(SMTableicvendors.TableName + "." + SMTableicvendors.scountry).trim() + "<BR>");
				pwOut.println("<FONT SIZE=2><B>Contact name:</B> " 
						+ rsVendor.getString(SMTableicvendors.TableName + "." + SMTableicvendors.scontactname).trim() + "<BR>");
				pwOut.println("<FONT SIZE=2><B>Phone:</B> " 
						+ rsVendor.getString(SMTableicvendors.TableName + "." + SMTableicvendors.sphonenumber).trim() + "<BR>");
				pwOut.println("<FONT SIZE=2><B>Fax:</B> " 
						+ rsVendor.getString(SMTableicvendors.TableName + "." + SMTableicvendors.sfaxnumber).trim() + "<BR>");
				pwOut.println("<FONT SIZE=2><B>Web address:</B> " 
						+ rsVendor.getString(SMTableicvendors.TableName + "." + SMTableicvendors.swebaddress).trim() + "<BR>");
				String sVendorGroupString = "<FONT SIZE=2><B>Vendor group:</B> ";
				if (rsVendor.getString(SMTableapvendorgroups.TableName + "." + SMTableapvendorgroups.sgroupid) != null){
					sVendorGroupString += rsVendor.getString(SMTableapvendorgroups.TableName + "." + SMTableapvendorgroups.sgroupid).trim() 
						+ " - "
						+ rsVendor.getString(SMTableapvendorgroups.TableName + "." + SMTableapvendorgroups.sdescription).trim()
					;
				}
				pwOut.println(sVendorGroupString + "<BR>");
				
				//End the left hand cell:
				pwOut.println("</TD>");

				//Start the right hand cell:
				pwOut.println("<TD>");
				//. . . .
				pwOut.println("<FONT SIZE=2><B>Vendor Acct:</B> " 
						+ rsVendor.getString(SMTableicvendors.TableName + "." + SMTableicvendors.svendoracct).trim() + "<BR>");
				pwOut.println("<FONT SIZE=2><B>Last maintained:</B> "
				+ "&nbsp;by&nbsp;" + rsVendor.getString(SMTableicvendors.TableName + "." + SMTableicvendors.slasteditedbyfullname)
				+ "&nbsp;on&nbsp;");				
				if(rsVendor.getString(SMTableicvendors.TableName + "." + SMTableicvendors.datlastmaintained).compareToIgnoreCase("0000-00-00 00:00:00")==0){
					pwOut.println("N/A<BR>");
				}else{
					pwOut.println(clsDateAndTimeConversions.sqlDateToString(rsVendor.getDate(SMTableicvendors.TableName + "." + SMTableicvendors.datlastmaintained), "MM/dd/YYYY") + "<BR>");
				}
				pwOut.println("<FONT SIZE=2><B>Company account code:</B> " 
						+ rsVendor.getString(SMTableicvendors.TableName + "." + SMTableicvendors.scompanyacctcode).trim() + "<BR>");
				pwOut.println("<FONT SIZE=2><B>Confirmation required:</B> ");
				if(rsVendor.getInt(SMTableicvendors.TableName + "." + SMTableicvendors.ipoconfirmationrequired) == 1){
					pwOut.println("YES<BR>");
				}else{
					pwOut.println("NO<BR>");
				}
					SQL = "SELECT"
						+ " " + SMTableapaccountsets.lid
						+ ", " + SMTableapaccountsets.sacctsetname
						+ ", " + SMTableapaccountsets.sdescription				
						+ " FROM " + SMTableapaccountsets.TableName
						+ " WHERE (" + SMTableapaccountsets.lid + "=" 
							+ rsVendor.getInt(SMTableicvendors.TableName + "." + SMTableicvendors.iapaccountset) 
								+ ")"
						+ " ORDER BY " + SMTableapaccountsets.lid ;
				
				ResultSet rsVendorAccountSet = clsDatabaseFunctions.openResultSet(SQL, conn);
				
				pwOut.println("<FONT SIZE=2><B>Account set:</B> ");
				if(rsVendorAccountSet.next()){
					pwOut.println(rsVendorAccountSet.getString(SMTableapaccountsets.sacctsetname).trim() + " - " +  rsVendorAccountSet.getString(SMTableapaccountsets.sdescription).trim() + "<BR>");	
				}else{
					pwOut.println("N/A<BR>");
				}			
				rsVendorAccountSet.close();

				//active
				if (rsVendor.getInt(SMTableicvendors.TableName + "." + SMTableicvendors.iactive) == 0){
					pwOut.print("<FONT SIZE=2><B>Active?</B> NO<BR>");
				}else{
					pwOut.print("<FONT SIZE=2><B>Active?</B> YES<BR>");
				}
				
				SQL = "SELECT"
						+ " " + SMTableicvendorterms.sTermsCode
						+ ", " + SMTableicvendorterms.sDescription
						+ " FROM " + SMTableicvendorterms.TableName
						+ " WHERE (" + SMTableicvendorterms.sTermsCode + "='" 
								+ rsVendor.getString(SMTableicvendors.sterms).trim() 
								+ "')"
							;
				
				ResultSet rsVendorTerms = clsDatabaseFunctions.openResultSet(SQL, conn);
				pwOut.print("<FONT SIZE=2><B>Terms:</B> " );
				if(rsVendorTerms.next()){
					pwOut.print(rsVendorTerms.getString(SMTableicvendorterms.sDescription).trim() + "<BR>");
				}else{
					pwOut.print("N/A<BR>");
				}
				rsVendorTerms.close();
				
				 SQL = "SELECT"
							+ " " + SMTablebkbanks.lid
							+ ", " + SMTablebkbanks.saccountname
							+ " FROM " + SMTablebkbanks.TableName
							+ " WHERE (" + SMTablebkbanks.lid + "=" + rsVendor.getInt(SMTableicvendors.TableName + "." + SMTableicvendors.ibankcode) + ")"
						;
				 ResultSet rsVendorBank = clsDatabaseFunctions.openResultSet(SQL, conn);
				 pwOut.print("<FONT SIZE=2><B>Bank:</B> " );
					if(rsVendorBank.next()){
						pwOut.print(rsVendorBank.getString(SMTablebkbanks.saccountname).trim() + "<BR>");
					}else{
						pwOut.print("N/A<BR>");
					}
					rsVendorBank.close();
					
				SQL = "SELECT * "
						+ " FROM " + SMTableglaccounts.TableName
						+ " WHERE (" + SMTableglaccounts.sAcctID + "='" + rsVendor.getString(SMTableicvendors.TableName + "." + SMTableicvendors.sdefaultexpenseacct) + "')"
							;
				ResultSet rsVendorExpAcct = clsDatabaseFunctions.openResultSet(SQL, conn);
				
				pwOut.println("<FONT SIZE=2><B>Default expense account:</B> ");
				if(rsVendorExpAcct.next()){
					pwOut.println(rsVendor.getString(SMTableicvendors.TableName + "." + SMTableicvendors.sdefaultexpenseacct)
						+ " - " + rsVendorExpAcct.getString(SMTableglaccounts.sDesc).trim() + "<BR>");	
				}else{
					pwOut.println("<BR>");
				}
				rsVendorExpAcct.close();
				
				pwOut.println("<FONT SIZE=2><B>Default distribution code:</B> ");
				// we use a '-1' to indicate that the user chose NOT to use a default distribution code:
				if (rsVendor.getString(SMTableicvendors.TableName + "." + SMTableicvendors.sdefaultdistributioncode).trim().compareToIgnoreCase("-1") == 0){
					pwOut.println("(NONE)" + "<BR>");
				}else{
					pwOut.println(rsVendor.getString(SMTableicvendors.TableName + "." + SMTableicvendors.sdefaultdistributioncode).trim() + "<BR>");
				}

				pwOut.println("<FONT SIZE=2><B>Default invoice line description:</B> ");
				pwOut.println(rsVendor.getString(SMTableicvendors.TableName + "." + SMTableicvendors.sdefaultinvoicelinedesc).trim() + "<BR>");	
				
				SQL = "SELECT * "
						+ " FROM " + SMTableapvendorremittolocations.TableName
						+ " WHERE (" 
						+ "(" + SMTableapvendorremittolocations.svendoracct + "='" 
						+ rsVendor.getString(SMTableicvendors.svendoracct) + "')"
						+ " AND (" + SMTableapvendorremittolocations.sremittocode + "='" 
						+ rsVendor.getString(SMTableicvendors.sprimaryremittocode) + "')"
						+ ")"
							;
				ResultSet rsVendorRemitTo = clsDatabaseFunctions.openResultSet(SQL, conn);
				
				pwOut.println("<FONT SIZE=2><B>Default Remit To:</B> ");
				if(rsVendorRemitTo.next()){
					pwOut.println(rsVendorRemitTo.getString(SMTableapvendorremittolocations.sremittoname).trim() + "<BR>");	
				}else{
					pwOut.println("N/A<BR>");
				}
				rsVendorRemitTo.close();
				
				pwOut.println("<FONT SIZE=2><B>Tax reporting type:</B> ");
				pwOut.println(SMTableicvendors.getTaxReportingTypeDescriptions(rsVendor.getInt(SMTableicvendors.TableName + "." + SMTableicvendors.itaxreportingtype)));

				//End the right hand cell:
				pwOut.println("</TD>");

				//End the parent table:
				pwOut.println("</TR>");
				pwOut.println("</TABLE>");
				
				//Starting date:
				String sStartingDocumentDate = clsManageRequestParameters.get_Request_Parameter(APVendorTransactionsSelect.PARAM_STARTING_DOCUMENT_DATE, req);
				if (sStartingDocumentDate.compareToIgnoreCase("") == 0){
					sStartingDocumentDate = "01/01/2000";
				}
				//Ending date:
				String sEndingDocumentDate = clsManageRequestParameters.get_Request_Parameter(APVendorTransactionsSelect.PARAM_ENDING_DOCUMENT_DATE, req);
				if (sEndingDocumentDate.compareToIgnoreCase("") == 0){
					sEndingDocumentDate = clsDateAndTimeConversions.now("MM/dd/yyyy");
				}
				//Starting document number
				String sStartingDocumentNumber = clsManageRequestParameters.get_Request_Parameter(APVendorTransactionsSelect.PARAM_STARTING_DOCUMENT_NUMBER, req);
				boolean bShowAppliedDetails = clsManageRequestParameters.get_Request_Parameter(APVendorTransactionsSelect.PARAM_INCLUDE_APPLIED_DETAILS, req).compareToIgnoreCase("") != 0;
				boolean bShowFullyPaidTransactions = clsManageRequestParameters.get_Request_Parameter(APVendorTransactionsSelect.PARAM_INCLUDE_FULLY_PAID_TRANSACTIONS, req).compareToIgnoreCase("") != 0;
				
				String sViewTransactionsForm = "";
				sViewTransactionsForm = 
					"<BR>"
					+ "<FORM"
						+ " NAME= \"" + VIEW_TRANSACTIONS_FORM_NAME + "\""
						+ " METHOD= \"" + "POST" + "\""
						+ " ACTION= \"" + SMUtilities.getURLLinkBase(getServletContext()) + "smap." + "APVendorTransactionsGenerate" + "\""
						+ ">" + "\n"
						
					+ "<INPUT TYPE=HIDDEN NAME='" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "' VALUE='" + sDBID + "'>" + "\n"
					+ "<INPUT TYPE=HIDDEN NAME='CallingClass' VALUE=\"" + this.getClass().getName() + "\" >" + "\n"
					
					//We'll let the View Transactions report know that we're coming from this screen, so it can display an option
					//to come back here afterwards:
					+ "<INPUT TYPE=HIDDEN NAME=\"" + PARAM_ORIGINATING_FROM_VENDOR_INFO_SCREEN + "\"" + " VALUE=\"" + "Y" + "\" >" + "\n"
					
					+ "<INPUT TYPE=HIDDEN NAME=\"" + APVendorTransactionsSelect.PARAM_STARTING_VENDOR + "\"" + " VALUE=\"" + sVendorNum + "\" >" + "\n"
					+ "<INPUT TYPE=HIDDEN NAME=\"" + APVendorTransactionsSelect.PARAM_ENDING_VENDOR + "\"" + " VALUE=\"" + sVendorNum + "\" >" + "\n"
					+ "<INPUT TYPE=HIDDEN NAME=\"" + PARAM_VENDOR_NUMBER + "\"" + " VALUE=\"" + sVendorNum + "\" >" + "\n"
					+ "<INPUT TYPE=HIDDEN NAME=\"" + APVendorTransactionsSelect.PARAM_PRINT_VENDORS_WITH_A_ZERO_BALANCE + "\"" + " VALUE=\"" + "Y" + "\" >" + "\n"
					
					+ "<INPUT TYPE=HIDDEN NAME=\"" + APVendorTransactionsSelect.PARAM_TRANSACTION_TYPE_PREFIX + Integer.toString(
								SMTableaptransactions.AP_TRANSACTION_TYPE_INVOICE_CREDITNOTE
							) + "\"" + " VALUE=\"" + "Y" + "\" >" + "\n"
					
					+ "<INPUT TYPE=HIDDEN NAME=\"" + APVendorTransactionsSelect.PARAM_TRANSACTION_TYPE_PREFIX + Integer.toString(
							SMTableaptransactions.AP_TRANSACTION_TYPE_INVOICE_DEBITNOTE
						) + "\"" + " VALUE=\"" + "Y" + "\" >" + "\n"
		
					+ "<INPUT TYPE=HIDDEN NAME=\"" + APVendorTransactionsSelect.PARAM_TRANSACTION_TYPE_PREFIX + Integer.toString(
							SMTableaptransactions.AP_TRANSACTION_TYPE_INVOICE_INVOICE
						) + "\"" + " VALUE=\"" + "Y" + "\" >" + "\n"
					
					+ "<INPUT TYPE=HIDDEN NAME=\"" + APVendorTransactionsSelect.PARAM_TRANSACTION_TYPE_PREFIX + Integer.toString(
							SMTableaptransactions.AP_TRANSACTION_TYPE_PAYMENT_APPLYTO
						) + "\"" + " VALUE=\"" + "Y" + "\" >" + "\n"
							
					+ "<INPUT TYPE=HIDDEN NAME=\"" + APVendorTransactionsSelect.PARAM_TRANSACTION_TYPE_PREFIX + Integer.toString(
							SMTableaptransactions.AP_TRANSACTION_TYPE_PAYMENT_MISCPAYMENT
						) + "\"" + " VALUE=\"" + "Y" + "\" >" + "\n"
							
					+ "<INPUT TYPE=HIDDEN NAME=\"" + APVendorTransactionsSelect.PARAM_TRANSACTION_TYPE_PREFIX + Integer.toString(
							SMTableaptransactions.AP_TRANSACTION_TYPE_PAYMENT_PAYMENT
						) + "\"" + " VALUE=\"" + "Y" + "\" >" + "\n"
		
					+ "<INPUT TYPE=HIDDEN NAME=\"" + APVendorTransactionsSelect.PARAM_TRANSACTION_TYPE_PREFIX + Integer.toString(
							SMTableaptransactions.AP_TRANSACTION_TYPE_PAYMENT_PREPAYMENT
						) + "\"" + " VALUE=\"" + "Y" + "\" >" + "\n"
							
					+ "<INPUT TYPE=HIDDEN NAME=\"" + APVendorTransactionsSelect.PARAM_TRANSACTION_TYPE_PREFIX + Integer.toString(
							SMTableaptransactions.AP_TRANSACTION_TYPE_REVERSAL
						) + "\"" + " VALUE=\"" + "Y" + "\" >" + "\n"
						
					+ "<DIV style = \" font-size: small; font-weight: bold; background-color:" + BACKGROUND_COLOR_LIGHT_BLUE + "; \" >"
					
					+ "<BR>" + "\n"
					+ "&nbsp;&nbsp;"
					+ "<INPUT TYPE=SUBMIT"
					+ " NAME = \"" + VIEW_TRANSACTIONS_SUBMIT_BUTTON + "\""
					+ " VALUE = \"" + VIEW_TRANSACTIONS_BUTTON_LABEL + "\""
					+ ">"
					+ "<BR>" + "\n"
					
					+ "Starting with document date:&nbsp;"
					+ "<INPUT TYPE=TEXT NAME=\"" + APVendorTransactionsSelect.PARAM_STARTING_DOCUMENT_DATE + "\""
					+ " VALUE=\"" + sStartingDocumentDate + "\""
					+ " SIZE=12"
					+ " MAXLENGTH=10"
					+ ">"
					+ "&nbsp;"
					+ SMUtilities.getDatePickerString(APVendorTransactionsSelect.PARAM_STARTING_DOCUMENT_DATE, getServletContext())
					+ "\n"
					
					+ "&nbsp;&nbsp;"
					+ "Ending with document date:&nbsp;"
					+ "<INPUT TYPE=TEXT NAME=\"" + APVendorTransactionsSelect.PARAM_ENDING_DOCUMENT_DATE + "\""
					+ " VALUE=\"" + sEndingDocumentDate + "\""
					+ " SIZE=12"
					+ " MAXLENGTH=10"
					+ ">"
					+ "&nbsp;"
					+ SMUtilities.getDatePickerString(APVendorTransactionsSelect.PARAM_ENDING_DOCUMENT_DATE, getServletContext())
					+ "\n"
					
					+ "&nbsp;&nbsp;"
					+ "Starting with document number:&nbsp;"
					+ "<INPUT TYPE=TEXT NAME=\"" + APVendorTransactionsSelect.PARAM_STARTING_DOCUMENT_NUMBER + "\""
					+ " VALUE=\"" + sStartingDocumentNumber + "\""
					+ " SIZE=12"
					+ " MAXLENGTH=" + Integer.toString(SMTableaptransactions.sdocnumberlength)
					+ ">"
					//+ "<BR>"
					+ "\n"+ "\n"
				;
				
				//Show applied details
				sViewTransactionsForm += 
					"&nbsp;&nbsp;&nbsp;"
					+ "<LABEL NAME=CHKBOX>"
					+ "<INPUT TYPE=\"CHECKBOX\" "
						+ "NAME=\"" + APVendorTransactionsSelect.PARAM_INCLUDE_APPLIED_DETAILS + "\"" 
				;
				if (bShowAppliedDetails){
					sViewTransactionsForm += "VALUE=1 CHECKED> ";
				}else{
					sViewTransactionsForm += "VALUE=0 > "; 
				}
			
				sViewTransactionsForm += "Show applied details";
				
				//Show applied details
				sViewTransactionsForm += 
					"&nbsp;&nbsp;&nbsp;"
					+ "<LABEL NAME=CHKBOX>"
					+ "<INPUT TYPE=\"CHECKBOX\" "
						+ "NAME=\"" + APVendorTransactionsSelect.PARAM_INCLUDE_FULLY_PAID_TRANSACTIONS + "\"" 
				;
				if (bShowFullyPaidTransactions){
					sViewTransactionsForm += "VALUE=1 CHECKED> ";
				}else{
					sViewTransactionsForm += "VALUE=0 > "; 
				}
			
				sViewTransactionsForm += "Show fully paid transactions";
				
				sViewTransactionsForm +=
					"<BR>&nbsp;"
					+ "</DIV>" + "\n"
						
					+ "</FORM>" + "\n"
				;
				
				//If the user has permission to display vendor transactions, then display the form for selecting those:
				if (SMSystemFunctions.isFunctionPermitted(
						SMSystemFunctions.APViewTransactionInformation, 
						sUserID, 
						conn,
						sLicenseModuleLevel
				)){
					pwOut.println(sViewTransactionsForm);
				}
				
				pwOut.println("<FONT SIZE=2><B>Google folder link:</B> " 
						+ "<A HREF=\"" + clsDatabaseFunctions.getRecordsetStringValue(rsVendor, SMTableicvendors.sgdoclink) 
						+ "\">" + clsDatabaseFunctions.getRecordsetStringValue(rsVendor, SMTableicvendors.sgdoclink) + "</A>"
				);
				
				String sLinks = "";
				if (bAllowCreateGDriveVendorFolders){
					String sUploadLink = "";
					try {
						sUploadLink = getGDocUploadLink(rsVendor.getString(SMTableicvendors.TableName + "." + SMTableicvendors.svendoracct), conn, req);
					} catch (Exception e) {
						pwOut.println("<FONT COLOR=RED><B>" + e.getMessage() + "</B></FONT>");
					}
					sLinks += sUploadLink;
				}
				pwOut.println("&nbsp;" + sLinks.trim() + "<BR>");
				
				//Get Vendor Statistics
				try{
					SQL = "SELECT * FROM " + SMTableapvendorstatistics.TableName 
						+ " WHERE ("
							+ "(" + SMTableapvendorstatistics.TableName + "." + SMTableapvendorstatistics.svendoracct 
								+ " = '" + sVendorNum + "')"
							+ ")"					
						+ " ORDER BY " + SMTableapvendorstatistics.lyear + " DESC, " 
							+ SMTableapvendorstatistics.lmonth + " DESC"
						;
					ResultSet rsVendorStatistics = clsDatabaseFunctions.openResultSet(SQL, conn);
					
					pwOut.println("<BR><a name=\"Statistics\"><TABLE WIDTH=100% BORDER=0><TR>"
							+ "<TD ALIGN=LEFT><B><U>Statistics</U></B></TD></TR></TABLE>");
					
					pwOut.println("<TABLE BORDER=1 WIDTH=100%  bgcolor=\"" + BACKGROUND_COLOR_LIGHT_BLUE + "\" cellspacing=0 cellpadding=1>");

					//Table heading:
					pwOut.println(
						"<TR>"
						+ "<TD CLASS=\"fieldrightheading\"><FONT SIZE=2>Year</FONT></TD>"
						+ "<TD CLASS=\"fieldrightheading\"><FONT SIZE=2>Month</FONT></TD>"
						+ "<TD CLASS=\"fieldrightheading\"><FONT SIZE=2># of Invoices</FONT></TD>"
						+ "<TD CLASS=\"fieldrightheading\"><FONT SIZE=2># of Credits</FONT></TD>"
						+ "<TD CLASS=\"fieldrightheading\"><FONT SIZE=2># of Debits</FONT></TD>"
						+ "<TD CLASS=\"fieldrightheading\"><FONT SIZE=2># of Payments</FONT></TD>"
						+ "<TD CLASS=\"fieldrightheading\"><FONT SIZE=2># of Discounts Taken</FONT></TD>"
						+ "<TD CLASS=\"fieldrightheading\"><FONT SIZE=2># of Discounts Lost</FONT></TD>"
						+ "<TD CLASS=\"fieldrightheading\"><FONT SIZE=2># of Adjustments</FONT></TD>"
						+ "<TD CLASS=\"fieldrightheading\"><FONT SIZE=2># of Invoices Paid</FONT></TD>"
						+ "<TD CLASS=\"fieldrightheading\"><FONT SIZE=2>Days to Pay</FONT></TD>"
						+ "<TD CLASS=\"fieldrightheading\"><FONT SIZE=2>Avg Days to Pay</FONT></TD>"
						+ "<TD CLASS=\"fieldrightheading\"><FONT SIZE=2>Invoice Amount</FONT></TD>"
						+ "<TD CLASS=\"fieldrightheading\"><FONT SIZE=2>Credit Amount</FONT></TD>"
						+ "<TD CLASS=\"fieldrightheading\"><FONT SIZE=2>Debit Amount</FONT></TD>"
						+ "<TD CLASS=\"fieldrightheading\"><FONT SIZE=2>Payment Amount</FONT></TD>"
						+ "<TD CLASS=\"fieldrightheading\"><FONT SIZE=2>Discounts Amount</FONT></TD>"
						+ "<TD CLASS=\"fieldrightheading\"><FONT SIZE=2>Discounts Lost Amount</FONT></TD>"
						+ "<TD CLASS=\"fieldrightheading\"><FONT SIZE=2>Adjustments Amount</FONT></TD>"
						+ "</TR>"
					);
					int rowCounter = 0;
					Long lTotalNoInvoinces = 0L;
					Long lTotalNoCredits = 0L;
					Long lTotalNoDebits = 0L;
					Long lTotalNoPayments = 0L;
					Long lTotalNoDiscounts = 0L;
					Long lTotalNoDiscountsLost = 0L;
					Long lTotalNoAdjustments = 0L;
					Long lTotalNoInvoicesPaid = 0L;
					Long lTotalNoDaysToPay = 0L;
					BigDecimal bdInvoiceTotal = new BigDecimal("0.00");
					BigDecimal bdCreditTotal = new BigDecimal("0.00");
					BigDecimal bdDebitTotal = new BigDecimal("0.00");
					BigDecimal bdPaymentTotal = new BigDecimal("0.00");
					BigDecimal bdDiscountTotal = new BigDecimal("0.00");
					BigDecimal bdDiscountLostTotal = new BigDecimal("0.00");
					BigDecimal bdAdjustmentTotal = new BigDecimal("0.00");
					String sColorClass = "";
					while (rsVendorStatistics.next()){					
					
						if (rowCounter % 2 == 0){
							sColorClass = "evennumberedtablerow";
						}else{
							sColorClass = "oddnumberedtablerow";
						}
						pwOut.println("<TR class = \"" + sColorClass + "\">");
						pwOut.println("<TD CLASS=\"fieldcontrolright\"><FONT SIZE=2>"
								+ rsVendorStatistics.getLong(SMTableapvendorstatistics.lyear)
								+ "</FONT></TD>");
						pwOut.println("<TD CLASS=\"fieldcontrolright\"><FONT SIZE=2>" 
								+ Long.toString(rsVendorStatistics.getLong(SMTableapvendorstatistics.lmonth))
								+ "</FONT></TD>");
						pwOut.println("<TD CLASS=\"fieldcontrolright\"><FONT SIZE=2>" 
								+ Long.toString(rsVendorStatistics.getLong(SMTableapvendorstatistics.lnumberofinvoices))
								+ "</FONT></TD>");
						lTotalNoInvoinces += rsVendorStatistics.getLong(SMTableapvendorstatistics.lnumberofinvoices);
						pwOut.println("<TD CLASS=\"fieldcontrolright\"><FONT SIZE=2>" 
								+ Long.toString(rsVendorStatistics.getLong(SMTableapvendorstatistics.lnumberofcredits))
								+ "</FONT></TD>");
						lTotalNoCredits += rsVendorStatistics.getLong(SMTableapvendorstatistics.lnumberofcredits);
						pwOut.println("<TD CLASS=\"fieldcontrolright\"><FONT SIZE=2>" 
								+ Long.toString(rsVendorStatistics.getLong(SMTableapvendorstatistics.lnumberofdebits))
								+ "</FONT></TD>");
						lTotalNoDebits += rsVendorStatistics.getLong(SMTableapvendorstatistics.lnumberofdebits);
						pwOut.println("<TD CLASS=\"fieldcontrolright\"><FONT SIZE=2>" 
								+ Long.toString(rsVendorStatistics.getLong(SMTableapvendorstatistics.lnumberofpayments))
								+ "</FONT></TD>");
						lTotalNoPayments += rsVendorStatistics.getLong(SMTableapvendorstatistics.lnumberofpayments);
						pwOut.println("<TD CLASS=\"fieldcontrolright\"><FONT SIZE=2>" 
								+ Long.toString(rsVendorStatistics.getLong(SMTableapvendorstatistics.lnumberofdiscountstaken))
								+ "</FONT></TD>");
						lTotalNoDiscounts += rsVendorStatistics.getLong(SMTableapvendorstatistics.lnumberofdiscountstaken);
						pwOut.println("<TD CLASS=\"fieldcontrolright\"><FONT SIZE=2>" 
								+ Long.toString(rsVendorStatistics.getLong(SMTableapvendorstatistics.lnumberofdiscountslost))
								+ "</FONT></TD>");
						lTotalNoDiscountsLost += rsVendorStatistics.getLong(SMTableapvendorstatistics.lnumberofdiscountslost);
						pwOut.println("<TD CLASS=\"fieldcontrolright\"><FONT SIZE=2>" 
								+ Long.toString(rsVendorStatistics.getLong(SMTableapvendorstatistics.lnumberofadjustments))
								+ "</FONT></TD>");
						lTotalNoAdjustments += rsVendorStatistics.getLong(SMTableapvendorstatistics.lnumberofadjustments);
						pwOut.println("<TD CLASS=\"fieldcontrolright\"><FONT SIZE=2>" 
								+ Long.toString(rsVendorStatistics.getLong(SMTableapvendorstatistics.lnumberofinvoicespaid))
								+ "</FONT></TD>");
						lTotalNoInvoicesPaid += rsVendorStatistics.getLong(SMTableapvendorstatistics.lnumberofinvoicespaid);
						pwOut.println("<TD CLASS=\"fieldcontrolright\"><FONT SIZE=2>" 
								+ Long.toString(rsVendorStatistics.getLong(SMTableapvendorstatistics.lnumberofdaystopay))
								+ "</FONT></TD>");
						lTotalNoDaysToPay += rsVendorStatistics.getLong(SMTableapvendorstatistics.lnumberofdaystopay);
						
						//Calculate the avg number of days to pay:
						BigDecimal bdAvgDaysToPay = new BigDecimal("0.00");
						String sAvgDaysToPay = "N/A";
						if (rsVendorStatistics.getLong(SMTableapvendorstatistics.lnumberofinvoicespaid) > 0){
							BigDecimal bdNumberOfDaysToPay = new BigDecimal(rsVendorStatistics.getLong(SMTableapvendorstatistics.lnumberofdaystopay));
							BigDecimal bdNumberOfInvoicesPaid = new BigDecimal(rsVendorStatistics.getLong(SMTableapvendorstatistics.lnumberofinvoicespaid));
							bdAvgDaysToPay = bdNumberOfDaysToPay.divide(bdNumberOfInvoicesPaid);
							sAvgDaysToPay = clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(bdAvgDaysToPay);
						}
						
						pwOut.println("<TD CLASS=\"fieldcontrolright\"><FONT SIZE=2>" 
								+ sAvgDaysToPay
								+ "</FONT></TD>");
						
						pwOut.println("<TD CLASS=\"fieldcontrolright\"><FONT SIZE=2>" + clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(
								rsVendorStatistics.getBigDecimal(SMTableapvendorstatistics.bdamountofinvoices))
								+ "</FONT></TD>");
						bdInvoiceTotal = bdInvoiceTotal.add(rsVendorStatistics.getBigDecimal(SMTableapvendorstatistics.bdamountofinvoices));
						pwOut.println("<TD CLASS=\"fieldcontrolright\"><FONT SIZE=2>" + clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(
								rsVendorStatistics.getBigDecimal(SMTableapvendorstatistics.bdamountofcreditnotes))
								+ "</FONT></TD>");
						bdCreditTotal = bdCreditTotal.add(rsVendorStatistics.getBigDecimal(SMTableapvendorstatistics.bdamountofcreditnotes));
						pwOut.println("<TD CLASS=\"fieldcontrolright\"><FONT SIZE=2>" + clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(
								rsVendorStatistics.getBigDecimal(SMTableapvendorstatistics.bdamountofdebitnotes))
								+ "</FONT></TD>");
						bdDebitTotal = bdDebitTotal.add(rsVendorStatistics.getBigDecimal(SMTableapvendorstatistics.bdamountofdebitnotes));
						pwOut.println("<TD CLASS=\"fieldcontrolright\"><FONT SIZE=2>" + clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(
								rsVendorStatistics.getBigDecimal(SMTableapvendorstatistics.bdamountofpayments))
								+ "</FONT></TD>");
						bdPaymentTotal = bdPaymentTotal.add(rsVendorStatistics.getBigDecimal(SMTableapvendorstatistics.bdamountofpayments));
						pwOut.println("<TD CLASS=\"fieldcontrolright\"><FONT SIZE=2>" + clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(
								rsVendorStatistics.getBigDecimal(SMTableapvendorstatistics.bdamountofdiscounts))
								+ "</FONT></TD>");
						bdDiscountTotal = bdDiscountTotal.add(rsVendorStatistics.getBigDecimal(SMTableapvendorstatistics.bdamountofdiscounts));
						pwOut.println("<TD CLASS=\"fieldcontrolright\"><FONT SIZE=2>" + clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(
								rsVendorStatistics.getBigDecimal(SMTableapvendorstatistics.bdamountofdiscountslost))
								+ "</FONT></TD>");
						bdDiscountLostTotal = bdDiscountLostTotal.add(rsVendorStatistics.getBigDecimal(SMTableapvendorstatistics.bdamountofdiscountslost));
						pwOut.println("<TD CLASS=\"fieldcontrolright\"><FONT SIZE=2>" + clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(
								rsVendorStatistics.getBigDecimal(SMTableapvendorstatistics.bdamountofadjustments))
								+ "</FONT></TD>");
						bdAdjustmentTotal = bdAdjustmentTotal.add(rsVendorStatistics.getBigDecimal(SMTableapvendorstatistics.bdamountofadjustments));
						
						pwOut.println("</TR>");
						rowCounter++;
					}
					rsVendorStatistics.close();
					if (rowCounter % 2 == 0){
						sColorClass = "evennumberedtablerow";
					}else{
						sColorClass = "oddnumberedtablerow";
					}
					
					//Calculate the average number of days to pay based on ALL the periods:
					BigDecimal bdAvgDaysToPayForVendor = new BigDecimal("0.00");
					String sAvgDaysToPayForVendor = "N/A";
					if (lTotalNoInvoicesPaid > 0){
						BigDecimal bdNumberOfDaysToPayForVendor = new BigDecimal(lTotalNoDaysToPay);
						BigDecimal bdNumberOfInvoicesPaidForVendor = new BigDecimal(lTotalNoInvoicesPaid);
						bdAvgDaysToPayForVendor = bdNumberOfDaysToPayForVendor.divide(bdNumberOfInvoicesPaidForVendor);
						sAvgDaysToPayForVendor = clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(bdAvgDaysToPayForVendor);
					}
					
					pwOut.println(
							"<TR bgcolor=\"" + BACKGROUND_COLOR_LIGHT_BLUE + "\" >"
							+ "<TD CLASS=\"fieldcontrolright\"COLSPAN=2><FONT SIZE=2><B>TOTAL:</B></FONT></TD>"
							+ "<TD CLASS=\"fieldcontrolright\"><FONT SIZE=2><B>" + Long.toString(lTotalNoInvoinces)+"</B></FONT></TD>"
							+ "<TD CLASS=\"fieldcontrolright\"><FONT SIZE=2><B>" + Long.toString(lTotalNoCredits)+"</B></FONT></TD>"
							+ "<TD CLASS=\"fieldcontrolright\"><FONT SIZE=2><B>" + Long.toString(lTotalNoDebits)+"</B></FONT></TD>"
							+ "<TD CLASS=\"fieldcontrolright\"><FONT SIZE=2><B>" + Long.toString(lTotalNoPayments)+"</B></FONT></TD>"
							+ "<TD CLASS=\"fieldcontrolright\"><FONT SIZE=2><B>" + Long.toString(lTotalNoDiscounts)+"</B></FONT></TD>"
							+ "<TD CLASS=\"fieldcontrolright\"><FONT SIZE=2><B>" + Long.toString(lTotalNoDiscountsLost)+"</B></FONT></TD>"
							+ "<TD CLASS=\"fieldcontrolright\"><FONT SIZE=2><B>" + Long.toString(lTotalNoAdjustments)+"</B></FONT></TD>"
							+ "<TD CLASS=\"fieldcontrolright\"><FONT SIZE=2><B>" + Long.toString(lTotalNoInvoicesPaid)+"</B></FONT></TD>"
							+ "<TD CLASS=\"fieldcontrolright\"><FONT SIZE=2><B>" + Long.toString(lTotalNoDaysToPay)+"</B></FONT></TD>"
							+ "<TD CLASS=\"fieldcontrolright\"><FONT SIZE=2><B>" + sAvgDaysToPayForVendor+"</B></FONT></TD>"
							+ "<TD CLASS=\"fieldcontrolright\"><FONT SIZE=2><B>" + bdInvoiceTotal.toString() + "</B></FONT></TD>"
							+ "<TD CLASS=\"fieldcontrolright\"><FONT SIZE=2><B>" + bdCreditTotal.toString() + "</B></FONT></TD>"
							+ "<TD CLASS=\"fieldcontrolright\"><FONT SIZE=2><B>" + bdDebitTotal.toString() + "</B></FONT></TD>"
							+ "<TD CLASS=\"fieldcontrolright\"><FONT SIZE=2><B>" + bdPaymentTotal.toString() + "</B></FONT></TD>"
							+ "<TD CLASS=\"fieldcontrolright\"><FONT SIZE=2><B>" + bdDiscountTotal.toString() + "</B></FONT></TD>"
							+ "<TD CLASS=\"fieldcontrolright\"><FONT SIZE=2><B>" + bdDiscountLostTotal.toString() + "</B></FONT></TD>"
							+ "<TD CLASS=\"fieldcontrolright\"><FONT SIZE=2><B>" + bdAdjustmentTotal.toString() + "</B></FONT></TD>"
							+ "</TR>"
						);
				}catch (Exception e){
					pwOut.println("Error opening vendor statistics query: " + e.getMessage());
					return false;
				}
				pwOut.println("</TABLE>");
				

				//pwOut.println("</TABLE>");
			}else{
				pwOut.println("Vendor not found.");
				return false;
			}
			rsVendor.close();
		}catch (SQLException e){
			pwOut.println("Error opening vendor query: " + e.getMessage());
			return false;
		}
		
		return true;
	}
	private String getGDocUploadLink(String sVendorNumber, Connection conn, HttpServletRequest req) throws Exception{
		String s = "";
		
		APOptions apopt = new APOptions();
		SMOption smopt = new SMOption();		
		try {
			smopt.load(conn);
		} catch (Exception e1) {
			throw new Exception("Error [1452003995] getting SM Options - " + e1.getMessage());
		}
		try {
			apopt.load(conn);
		} catch (Exception e1) {
			throw new Exception("Error [1452003996] getting AR Options - " + e1.getMessage());
		}
		String sFolderName =  apopt.getgdrivevendorfolderprefix() 
				+ sVendorNumber 
				+ apopt.getgdrivevendorfoldersuffix();
		String sUploadFileLink = "";
		
		try {
			sUploadFileLink = smopt.getgdriveuploadfileurl() + "?"
					+ SMCreateGoogleDriveFolderParamDefinitions.parentfolderid + "=" + apopt.getgdrivevendorparentfolderid()
					+ "&" + SMCreateGoogleDriveFolderParamDefinitions.foldername + "=" + sFolderName
					+ "&" + SMCreateGoogleDriveFolderParamDefinitions.backgroundcolor + "=" + smopt.getBackGroundColor()
					+ "&" + SMCreateGoogleDriveFolderParamDefinitions.returnURL + "=" + SMUtilities.getCreateGDriveReturnURL(req, getServletContext())
					+ "&" + SMCreateGoogleDriveFolderParamDefinitions.recordtype + "=" + SMCreateGoogleDriveFolderParamDefinitions.AP_DISPLAY_VENDOR_TYPE_PARAM_VALUE
					+ "&" + SMCreateGoogleDriveFolderParamDefinitions.keyvalue + "=" + sVendorNumber
					;
		}catch(Exception e) {
			throw new Exception("Error [1542748312] "+e.getMessage());
		}
		 
		s += "&nbsp;&nbsp;<FONT SIZE=2><a href=\"" + sUploadFileLink + "\" target=\"_blank\">Upload File(s) to Google Drive</a></FONT>";
		return s;
	}
}
