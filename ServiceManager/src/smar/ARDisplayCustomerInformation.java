package smar;

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

import smcontrolpanel.SMAuthenticate;
import smcontrolpanel.SMCreateGDriveFolder;
import smcontrolpanel.SMSystemFunctions;
import smcontrolpanel.SMUtilities;
import ConnectionPool.WebContextParameters;
import SMClasses.SMLogEntry;
import SMClasses.SMOrderHeader;
import SMDataDefinition.SMCreateGoogleDriveFolderParamDefinitions;
import SMDataDefinition.SMTablearcustomer;
import SMDataDefinition.SMTabledefaultsalesgroupsalesperson;
import SMDataDefinition.SMTableorderdetails;
import SMDataDefinition.SMTableorderheaders;
import SMDataDefinition.SMTablesalescontacts;
import SMDataDefinition.SMTablesalesgroups;
import SMDataDefinition.SMTablesalesperson;
import SMDataDefinition.SMTabletax;
import SMDataDefinition.SMTabletaxcertificates;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsDateAndTimeConversions;
import ServletUtilities.clsManageBigDecimals;
import ServletUtilities.clsManageRequestParameters;
import ServletUtilities.clsStringFunctions;

public class ARDisplayCustomerInformation extends HttpServlet {

	private static final long serialVersionUID = 1L;
	private String sWarning = "";
	private String sCallingClass = "";
	private String sDBID = "";
	private String sUserID = "";
	private String sUserFullName = "";
	private String sCompanyName = "";
	
	public void doGet(HttpServletRequest request,
				HttpServletResponse response)
				throws ServletException, IOException {
		if (!SMAuthenticate.authenticateSMCPCredentials(
				request, 
				response, 
				getServletContext(),
				SMSystemFunctions.ARDisplayCustomerInformation
			)
		){
			return;
		}
	    response.setContentType("text/html");
		PrintWriter out = response.getWriter();
		
	    //Get the session info:
	    HttpSession CurrentSession = request.getSession(true);
	    sDBID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_DATABASE_ID);
	    sUserID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERID);
	    sUserFullName = (String)CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERFIRSTNAME) + " "
	    				+ (String)CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERLASTNAME);
	    sCompanyName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_COMPANYNAME);
	    
	    //sCallingClass will look like: smcontrolpanel.ARAgedTrialBalanceReport
	    sCallingClass = clsManageRequestParameters.get_Request_Parameter("CallingClass", request);

    	String sCustomerNumber = clsManageRequestParameters.get_Request_Parameter("CustomerNumber", request);

    	//Customized title
    	String sReportTitle = "View Customer Information";
    	out.println(SMUtilities.SMCPTitleSubBGColor(sReportTitle, "", "#FFFFFF", sCompanyName));
    	
    	out.println("<TD><A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMUserLogin?" 
				+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
				+ "\">Return to user login</A><BR>");
	    out.println("<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smar.ARMainMenu?" 
				+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
				+ "\">Return to Accounts Receivable Main Menu</A><BR>");
	    out.println("<A HREF=\"" + WebContextParameters.getdocumentationpageURL(getServletContext()) + "#" + Long.toString(SMSystemFunctions.ARDisplayCustomerInformation) 
	    	+ "\">Summary</A><BR>");
    	
	    //save current url in url history
	    String sURLTitle = "Viewing customer information for customer number " + sCustomerNumber;
	    try {
			SMUtilities.addURLToHistory(sURLTitle, CurrentSession, request);
		} catch (Exception e) {
		}
	    out.println("<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMURLHistoryList?" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" 
				+ sDBID + "\">Return to...</A><BR>");
 	    //log usage of this this report
 	    SMClasses.SMLogEntry log = new SMClasses.SMLogEntry(sDBID, getServletContext());
 	    log.writeEntry(sUserID, SMLogEntry.LOG_OPERATION_DISPLAYCUSTOMERINFO, "REPORT", "SMDisplayCustomerInformation", "[1376509275]");
    	
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

    	if (!displayCustomer(
    			conn, 
    			sCustomerNumber, 
    			out, 
    			request,
    			(String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_LICENSE_MODULE_LEVEL))){
    		
    	}
    	
    	clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1547067527]");
    	
	    out.println("</BODY></HTML>");
	}
	private boolean displayCustomer(Connection conn, String sCustomerNum, PrintWriter pwOut, HttpServletRequest req, String sLicenseModuleLevel){
	
		String SQL = "SELECT * FROM "
			+ SMTablearcustomer.TableName
			+ " WHERE "
    		+ SMTablearcustomer.sCustomerNumber + " = '" + sCustomerNum + "'";
			
		try{
			ResultSet rsCustomer = clsDatabaseFunctions.openResultSet(SQL, conn);
			
			if(rsCustomer.next()){
				//pwOut.println("<BR><a name=\"CustomerHeader\"><TABLE WIDTH=100% BORDER=0><TR><TD ALIGN=LEFT><B><U>Order Header</U></B></TD></TR></TABLE>");

				//Qualify links with permissions:
				boolean bAllowCustomerStatisticsView = 
					SMSystemFunctions.isFunctionPermitted(
							SMSystemFunctions.ARCustomerStatistics, 
							sUserID, 
							conn,
							sLicenseModuleLevel
					);
				boolean bAllowCustomerActivityView = 
					SMSystemFunctions.isFunctionPermitted(
							SMSystemFunctions.ARCustomerActivity, 
							sUserID, 
							conn,
							sLicenseModuleLevel
					);
				boolean bAllowCreateGDriveARFolders = 
						SMSystemFunctions.isFunctionPermitted(
								SMSystemFunctions.SMCreateGDriveARFolders, 
								sUserID, 
								conn,
								sLicenseModuleLevel
						);
				String sLinks = "";
				if (bAllowCustomerActivityView){
					sLinks = 
						"<FONT SIZE=2><A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smar.ARActivityDisplay?CustomerNumber=" 
						+ sCustomerNum
						+ "&OpenTransactionsOnly=true"
						+ "&StartingDate=1/1/1900"
						+ "&EndingDate=" + clsDateAndTimeConversions.now("M/d/yyyy")
						+ "&OrderBy=idoctype"
						+ "&Invoice=yes"
						+ "&Credit=yes"
						+ "&Receipt=yes"
						+ "&Prepayment=yes"
						+ "&Reversal=yes"
						+ "&Invoice Adjustment=yes"
						+ "&Misc Receipt=yes"
						+ "&Cash Adjustment=yes"
						+ "&Credit Adjustment=yes"
						+ "&Retainage=yes"
						+ "&Apply-To=yes"
						+ "&Invoice=yes"
						+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
						+ "\">Customer activity</A></FONT>&nbsp;&nbsp;"
						;
				}
				if (bAllowCustomerStatisticsView){
					sLinks +=
						"<FONT SIZE=2>&nbsp;&nbsp;<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smar.ARDisplayStatistics?Customer=" 
						+ sCustomerNum 
						+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
						+ "\">Customer statistics</A></FONT>";
				}
				
				if (bAllowCreateGDriveARFolders){
					String sUploadLink = "";
					try {
						sUploadLink = getGDocUploadLink(rsCustomer.getString(SMTablearcustomer.sCustomerNumber), conn, req);
					} catch (Exception e) {
						pwOut.println("<FONT COLOR=RED><B>" + e.getMessage() + "</B></FONT>");
					}
					sLinks += sUploadLink;
				}
	
				pwOut.println("<BR>" + sLinks + "<BR>"); 
				
				pwOut.print("<FONT SIZE=2><BR><B>Customer code:</B> " 
						+ rsCustomer.getString(SMTablearcustomer.sCustomerNumber).trim());
				
				pwOut.print("<FONT SIZE=2><BR><B>Customer name:</B> " 
					+ rsCustomer.getString(SMTablearcustomer.sCustomerName).trim() 
					+ "&nbsp;-&nbsp; last maintained by user:&nbsp;" 
					+ rsCustomer.getString(SMTablearcustomer.sLastEditUserFullName)
					+ "&nbsp;on&nbsp;"
					+ clsDateAndTimeConversions.sqlDateToString(rsCustomer.getDate(
							SMTablearcustomer.datLastMaintained), "M/d/yyyy", "N/A")
					+ "<BR>");
				
				//Start the first table:
				pwOut.println("<TABLE BORDER=0 WIDTH=100%  cellspacing=0 cellpadding=0><TR>");
				
				pwOut.println("<TD>");
				//active
				if (rsCustomer.getInt(SMTablearcustomer.iActive) == 0){
					pwOut.print("<FONT SIZE=2><B>Active?</B> NO");
				}else{
					pwOut.print("<FONT SIZE=2><B>Active?</B> YES");
				}
				pwOut.println("</TD>");
				
				pwOut.println("<TD>");
				//on hold
				if (rsCustomer.getInt(SMTablearcustomer.iOnHold) == 0){
					pwOut.print("<FONT SIZE=2><B>On Hold?</B> NO");
				}else{
					pwOut.print("<FONT SIZE=2><B>On Hold?</B> YES");
				}
				pwOut.println("</TD>");
				
				pwOut.println("<TD>");
				//terms
				pwOut.print("<FONT SIZE=2><B>Terms:</B> " 
						+ rsCustomer.getString(SMTablearcustomer.sTerms).trim());
				pwOut.println("</TD>");
				
				pwOut.println("<TD>");
				//customer group
				pwOut.print("<FONT SIZE=2><B>Customer group:</B> " 
						+ rsCustomer.getString(SMTablearcustomer.sCustomerGroup).trim());
				pwOut.println("</TD>");
				
				pwOut.println("<TD>");
				//account set
				pwOut.print("<FONT SIZE=2><B>Account set:</B> " 
						+ rsCustomer.getString(SMTablearcustomer.sAccountSet).trim());
				pwOut.println("</TD>");
				
				//End the first row:
				pwOut.println("</TR>");
				
				//Start the second row:
				pwOut.println("<TR>");
				
				pwOut.println("<TD>");
				//price list code
				pwOut.print("<FONT SIZE=2><B>Price list code:</B> " 
						+ rsCustomer.getString(SMTablearcustomer.sPriceListCode).trim());
				pwOut.println("</TD>");
				
				pwOut.println("<TD>");
				//price level
				pwOut.print("<FONT SIZE=2><B>Price level:</B> " 
						+ Long.toString(rsCustomer.getLong(SMTablearcustomer.ipricelevel)));
				pwOut.println("</TD>");
				
				pwOut.println("<TD>");
				//credit limit
				pwOut.print("<FONT SIZE=2><B>Credit limit:</B> " 
						+ clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(
							rsCustomer.getBigDecimal(SMTablearcustomer.dCreditLimit)));
				pwOut.println("</TD>");
				
				pwOut.println("<TD>");
				//start date
				pwOut.print("<FONT SIZE=2><B>Start date:</B> " 
						+ clsDateAndTimeConversions.utilDateToString(rsCustomer.getDate(
						SMTablearcustomer.datStartDate),"M/d/yyyy"));
				pwOut.println("</TD>");

				//End the line with a blank:
				pwOut.println("<TD>");
				pwOut.print("&nbsp;");
				pwOut.println("</TD>");
				
				//End the second row:
				pwOut.println("</TR>");
				
				//End the first table:
				pwOut.println("</TABLE>");
				
				//Start the parent table:
				pwOut.println("<TABLE BORDER=1 WIDTH=100%  cellspacing=0 cellpadding=2><TR>");

				//Start the left hand cell:
				pwOut.println("<TD>");
				//. . . .
				pwOut.println("<FONT SIZE=2><B>Address Line 1:</B> " 
						+ rsCustomer.getString(SMTablearcustomer.sAddressLine1).trim() + "<BR>");
				pwOut.println("<FONT SIZE=2><B>Address Line 2:</B> " 
						+ rsCustomer.getString(SMTablearcustomer.sAddressLine2).trim() + "<BR>");
				pwOut.println("<FONT SIZE=2><B>Address Line 3:</B> " 
						+ rsCustomer.getString(SMTablearcustomer.sAddressLine3).trim() + "<BR>");
				pwOut.println("<FONT SIZE=2><B>Address Line 4:</B> " 
						+ rsCustomer.getString(SMTablearcustomer.sAddressLine4).trim() + "<BR>");
				pwOut.println("<FONT SIZE=2><B>City:</B> " 
						+ rsCustomer.getString(SMTablearcustomer.sCity).trim() + "<BR>");
				pwOut.println("<FONT SIZE=2><B>State:</B> " 
						+ rsCustomer.getString(SMTablearcustomer.sState).trim() + "<BR>");
				pwOut.println("<FONT SIZE=2><B>Postal code:</B> " 
						+ rsCustomer.getString(SMTablearcustomer.sPostalCode).trim() + "<BR>");
				pwOut.println("<FONT SIZE=2><B>Country:</B> " 
						+ rsCustomer.getString(SMTablearcustomer.sCountry).trim() + "<BR>");
				
				//End the left hand cell:
				pwOut.println("</TD>");

				//Start the right hand cell:
				pwOut.println("<TD>");
				//. . . .

				pwOut.println("<FONT SIZE=2><B>Contact name:</B> " 
						+ rsCustomer.getString(SMTablearcustomer.sContactName).trim() + "<BR>");
				pwOut.println("<FONT SIZE=2><B>Phone:</B> " 
						+ rsCustomer.getString(SMTablearcustomer.sPhoneNumber).trim() + "<BR>");
				pwOut.println("<FONT SIZE=2><B>Fax:</B> " 
						+ rsCustomer.getString(SMTablearcustomer.sFaxNumber).trim() + "<BR>");
				pwOut.println("<FONT SIZE=2><B>Email:</B> " 
						+ rsCustomer.getString(SMTablearcustomer.sEmailAddress).trim() + "<BR>");
				pwOut.println("<FONT SIZE=2><B>Web address:</B> " 
						+ rsCustomer.getString(SMTablearcustomer.sWebAddress).trim() + "<BR>");
				
				//Get the tax type:
				String sTaxJurisdiction = "(NOT FOUND)";
				String sTaxType = "(NOT FOUND)";
				String lTaxID = Long.toString(rsCustomer.getLong(SMTablearcustomer.itaxid));
				SQL ="SELECT"
					+ " " + SMTabletax.staxjurisdiction
					+ ", " + SMTabletax.staxtype
					+ " FROM "
					+ SMTabletax.TableName
					+ " WHERE ("
						+ SMTabletax.lid + " = " + lTaxID
					+ ")"
					;
				try {
					ResultSet rs = clsDatabaseFunctions.openResultSet(SQL,
							getServletContext(), sDBID, "MySQL",
							SMUtilities.getFullClassName(this.toString())
									+ ".displayCustomer, get tax type - user: "
									+ sUserID
									+ " - "
									+ sUserFullName
							);
					if (rs.next()) {
						sTaxJurisdiction = rs.getString(SMTabletax.staxjurisdiction);
						sTaxType = rs.getString(SMTabletax.staxtype);
					}
					rs.close();
				} catch (SQLException e) {
					// Nothing here, just go on . . . .
				}
				
				pwOut.println("<FONT SIZE=2><B>Tax jurisdiction:</B> " 
						+ sTaxJurisdiction + "<BR>");

				pwOut.println("<FONT SIZE=2><B>Tax type:</B> " 
						+ sTaxType + "<BR>");

				if (rsCustomer.getInt(SMTablearcustomer.iuseselectronicdeposit) == 0){
					pwOut.print("<FONT SIZE=2><B>Uses electronic deposit?</B> NO<BR>");
				}else{
					pwOut.print("<FONT SIZE=2><B>Uses electronic deposit?</B> YES<BR>");
				}

				if (rsCustomer.getInt(SMTablearcustomer.irequiresstatements) == 0){
					pwOut.print("<FONT SIZE=2><B>Requires statements?</B> NO<BR>");
				}else{
					pwOut.print("<FONT SIZE=2><B>Requires statements?</B> YES<BR>");
				}
				
				if (rsCustomer.getInt(SMTablearcustomer.irequirespo) == 0){
					pwOut.print("<FONT SIZE=2><B>Requires purchase order?</B> NO<BR>");
				}else{
					pwOut.print("<FONT SIZE=2><B>Requires purchase order?</B> YES<BR>");
				}
				
				pwOut.println("<FONT SIZE=2><B>Invoicing contact:</B> " 
						+ rsCustomer.getString(SMTablearcustomer.sinvoicingcontact).trim() + "<BR>");
				pwOut.println("<FONT SIZE=2><B>Invoicing email:</B> " 
						+ rsCustomer.getString(SMTablearcustomer.sinvoicingemail).trim() + "<BR>");
				//End the right hand cell:
				pwOut.println("</TD>");

				//End the parent table:
				pwOut.println("</TR>");
				pwOut.println("</TABLE>");
				
				//Display the default salespersons:
				pwOut.println("<FONT SIZE=2><BR><B>Default salespersons by sales group:</B><BR>"); 
				SQL = "SELECT"
						+ " " + SMTablesalesgroups.TableName + "." + SMTablesalesgroups.sSalesGroupDesc
						+ ", " + SMTablesalesperson.TableName + "." + SMTablesalesperson.sSalespersonFirstName
						+ ", " + SMTablesalesperson.TableName + "." + SMTablesalesperson.sSalespersonLastName
						+ ", " + SMTabledefaultsalesgroupsalesperson.TableName + "." + SMTabledefaultsalesgroupsalesperson.ssalespersoncode
						+ " FROM " + SMTabledefaultsalesgroupsalesperson.TableName + " LEFT JOIN " + SMTablesalesgroups.TableName
						+ " ON " + SMTabledefaultsalesgroupsalesperson.TableName + "." + SMTabledefaultsalesgroupsalesperson.lsalesgroupid
						+ " = " + SMTablesalesgroups.TableName + "." + SMTablesalesgroups.iSalesGroupId
						+ " LEFT JOIN " + SMTablesalesperson.TableName + " ON "
						+ SMTabledefaultsalesgroupsalesperson.TableName + "." + SMTabledefaultsalesgroupsalesperson.ssalespersoncode
						+ " = " + SMTablesalesperson.TableName + "." + SMTablesalesperson.sSalespersonCode
					+ " WHERE ("
						+ "(" + SMTabledefaultsalesgroupsalesperson.scustomercode + " = '" + sCustomerNum + "')"
					+ ")"
				;
				ResultSet rsSalespersons = clsDatabaseFunctions.openResultSet(SQL, conn);
				while (rsSalespersons.next()){
					pwOut.println("Salesperson for group <B>"
						+ rsSalespersons.getString(SMTablesalesgroups.TableName + "." + SMTablesalesgroups.sSalesGroupDesc)
						+ "</B>: "
						+ clsStringFunctions.checkStringForNull(rsSalespersons.getString(SMTabledefaultsalesgroupsalesperson.TableName 
							+ "." + SMTabledefaultsalesgroupsalesperson.ssalespersoncode))
						+ " - "
						+ clsStringFunctions.checkStringForNull(rsSalespersons.getString(SMTablesalesperson.TableName + "." 
								+ SMTablesalesperson.sSalespersonFirstName))
						+ " "
						+ clsStringFunctions.checkStringForNull(rsSalespersons.getString(SMTablesalesperson.TableName + "." 
								+ SMTablesalesperson.sSalespersonLastName))
						+ "<BR>"
					);
				}
				
				pwOut.println("<FONT SIZE=2><BR><B>Customer comments:</B> " 
						+ clsDatabaseFunctions.getRecordsetStringValue(
								rsCustomer, SMTablearcustomer.mCustomerComments).replace("\n", "<BR>")
						+ "<BR>");
				pwOut.println("<FONT SIZE=2><B>Accounting notes:</B> " 
						+ clsDatabaseFunctions.getRecordsetStringValue(
								rsCustomer, SMTablearcustomer.mAccountingNotes).replace("\n", "<BR>")
						+ "<BR>");
				pwOut.println("<FONT SIZE=2><B>Invoicing instructions:</B> " 
						+ clsDatabaseFunctions.getRecordsetStringValue(
								rsCustomer, SMTablearcustomer.sinvoicingnotes).replace("\n", "<BR>")
						+ "<BR>");
				pwOut.println("<FONT SIZE=2><B>Google folder link:</B> " 
						+ "<A HREF=\"" + clsDatabaseFunctions.getRecordsetStringValue(rsCustomer, SMTablearcustomer.sgdoclink) 
						+ "\">" + clsDatabaseFunctions.getRecordsetStringValue(rsCustomer, SMTablearcustomer.sgdoclink) + "</A><BR>"
				);

				//View open orders
				pwOut.println("<BR><BR><FONT SIZE=2><B><U>Open Orders</U>:</B><BR><BR>" );
				SQL = "SELECT"
					+ " 'ARDisplayCustomerInformationSQL' AS REPORTNAME"
					+ ", '" + sUserID + "' AS USERID"
					+ ", " + SMTableorderheaders.TableName + "." + SMTableorderheaders.iOrderType 
					+ "," + SMTableorderheaders.TableName + "." + SMTableorderheaders.datOrderCreationDate
					+ "," + SMTableorderheaders.TableName + "." + SMTableorderheaders.strimmedordernumber
					+ "," + SMTableorderheaders.TableName + "." + SMTableorderheaders.sBillToName
					+ "," + SMTableorderheaders.TableName + "." + SMTableorderheaders.sShipToName
					+ " FROM " 
					+ " (SELECT"
					+ " " + SMTableorderdetails.strimmedordernumber + " AS ORDERNUM"
					+ " FROM orderdetails"
					+ " WHERE ("
						+ "(orderdetails.dQtyOrdered != 0.00)"
					+ ")"
					+ " GROUP BY " + SMTableorderdetails.strimmedordernumber
					+ ") AS DETAILQUERY"
					+ " LEFT JOIN orderheaders ON DETAILQUERY.ORDERNUM=" + SMTableorderheaders.TableName + "." + SMTableorderheaders.strimmedordernumber
					+ " WHERE (" 	
					+ "(" + SMTableorderheaders.TableName + "." + SMTableorderheaders.sCustomerCode + " = '" + sCustomerNum + "')"
					//no quotes
					+ " AND (" + SMTableorderheaders.TableName + "." + SMTableorderheaders.iOrderType + " != "
						+ SMTableorderheaders.ORDERTYPE_QUOTE + ")"
					+ " AND (" 
			 			+ "(" + SMTableorderheaders.TableName + "." + SMTableorderheaders.datOrderCanceledDate + " = '1899/12/31')"
			 			+ " OR (" + SMTableorderheaders.TableName + "." + SMTableorderheaders.datOrderCanceledDate + " = '0000-00-00 00:00:00')"
			 		+ ")) GROUP BY " + SMTableorderheaders.TableName + "." + SMTableorderheaders.strimmedordernumber 
			 		 + " ORDER BY " + SMTableorderheaders.TableName + "." + SMTableorderheaders.iOrderType + " DESC, " 
			 			+ SMTableorderheaders.TableName + "." + SMTableorderheaders.datOrderCreationDate
		 		;
				
				ResultSet rsOpenOrders = clsDatabaseFunctions.openResultSet(SQL, conn);
				int iOrderCount = 0;
				int iRowCount = 1;
				String sRowColor = "#FFF";
				
					
				if (iOrderCount < 1){
					pwOut.println("<TABLE width=\"100%\" style= \"border-collapse:collapse;\">");
					pwOut.println("<TR bgcolor=\"#A3D1FF\">"
						+ "<TD style=\" border: 1px solid;font-size:small; font-weight:bold; text-align:left;\">Status</TD>"
						+ "<TD style=\" border: 1px solid;font-size:small; font-weight:bold; text-align:left;\">Date</TD>"
						+ "<TD style=\" border: 1px solid;font-size:small; font-weight:bold; text-align:left;\">Order Number</TD>"
						+ "<TD style=\" border: 1px solid;font-size:small; font-weight:bold; text-align:left;\">Bill To</TD>"
						+ "<TD style=\" border: 1px solid;font-size:small; font-weight:bold; text-align:left;\">Ship To</TD>"
						+ "</TR>");	
				}
				while(rsOpenOrders.next()){
					iOrderCount++;
					iRowCount++;
					if(iRowCount % 2 == 0){
						sRowColor = "#FFF";
					}else{
						sRowColor = "#DCDCDC";
					}
						pwOut.println( "<TR bgcolor=\"" + sRowColor + "\" ><TD style=\"padding-right: 20px\"><FONT SIZE=2>" 
						+ SMTableorderheaders.getOrderTypeDescriptions(rsOpenOrders.getInt(SMTableorderheaders.iOrderType)) 
						+ "</TD>"
						+ "<TD style=\"padding-right: 20px\"><FONT SIZE=2> " + clsDateAndTimeConversions.resultsetDateStringToString(rsOpenOrders.getString(SMTableorderheaders.datOrderCreationDate)) + "</FONT></TD>"
						+ "<TD style=\"padding-right: 20px\"><FONT SIZE=2> " + "<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMDisplayOrderInformation?" 
						+ "OrderNumber" + "=" + rsOpenOrders.getString(SMTableorderheaders.strimmedordernumber)
						+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
						+ "\">" + rsOpenOrders.getString(SMTableorderheaders.strimmedordernumber) + "</A></FONT></TD>");						 
						
						pwOut.println("<TD style=\"padding-right: 20px\"><FONT SIZE=2> " + rsOpenOrders.getString(SMTableorderheaders.sBillToName) + "</FONT></TD>"		
						+ "<TD style=\"padding-right: 20px\"><FONT SIZE=2> " + rsOpenOrders.getString(SMTableorderheaders.sShipToName) + "</FONT></TD>");

						pwOut.println( "</TR>");
						iOrderCount++;				
				}
				pwOut.println("</TABLE>");
				rsOpenOrders.close();
				
				//View tax certificates
				pwOut.println("<BR><BR><FONT SIZE=2><B><U>Tax Certificates</U>:</B><BR><BR>" );
				SQL = "SELECT * "
					+ " FROM " + SMTabletaxcertificates.TableName 
					+ " LEFT JOIN " + SMTableorderheaders.TableName
					+ " ON " + SMTabletaxcertificates.TableName + "." + SMTabletaxcertificates.sjobnumber
					+ " = " + SMTableorderheaders.TableName + "." + SMTableorderheaders.strimmedordernumber
					+ " WHERE ("
						+ "(" + SMTabletaxcertificates.TableName + "." + SMTabletaxcertificates.scustomernumber + " = '" + sCustomerNum + "')"
					+ ") ORDER BY " + SMTabletaxcertificates.TableName + "." + SMTabletaxcertificates.datreceived
				;

				ResultSet rsTaxCert = clsDatabaseFunctions.openResultSet(SQL, conn);
				int iCertCount = 0;
				iRowCount = 1;
				sRowColor = "#FFF";
								
				if (iCertCount < 1){
					pwOut.println("<TABLE width=\"100%\" style= \"border-collapse:collapse;\">");
					pwOut.println("<TR bgcolor=\"#FFB8B8\">"
						+ "<TD style=\" border: 1px solid;font-size:small; font-weight:bold; text-align:left;\">View Certificate</TD>"
						+ "<TD style=\" border: 1px solid;font-size:small; font-weight:bold; text-align:left;\">ID</TD>"
						+ "<TD style=\" border: 1px solid;font-size:small; font-weight:bold; text-align:left;\">Job #</TD>"
						+ "<TD style=\" border: 1px solid;font-size:small; font-weight:bold; text-align:left;\">Ship To Name</TD>"
						+ "<TD style=\" border: 1px solid;font-size:small; font-weight:bold; text-align:left;\">Project(s) Location</TD>"
						+ "<TD style=\" border: 1px solid;font-size:small; font-weight:bold; text-align:left;\">Exempt Number</TD>"
						+ "<TD style=\" border: 1px solid;font-size:small; font-weight:bold; text-align:left;\">Jurisdiction</TD>"
						+ "<TD style=\" border: 1px solid;font-size:small; font-weight:bold; text-align:left;\">Expiration Date</TD>"
						+ "</TR>");	
				}
				while(rsTaxCert.next()){
				iCertCount++;
					iRowCount++;
					if(iRowCount % 2 == 0){
						sRowColor = "#FFF";
					}else{
						sRowColor = "#DCDCDC";
					}
					if(rsTaxCert.getString(SMTabletaxcertificates.sgdoclink).compareToIgnoreCase("") != 0){

						pwOut.println( "<TR bgcolor=\"" + sRowColor + "\" ><TD style=\"padding-right: 20px\"><FONT SIZE=2><A HREF=\"" + clsDatabaseFunctions.getRecordsetStringValue(rsTaxCert, SMTabletaxcertificates.sgdoclink) 
						+ "\">" + "View" + "</A> </TD>"
						+ "<TD style=\"padding-right: 20px\"> <FONT SIZE=2> " 
							+ "<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMEditTaxCertificatesEdit?" 
							+ SMTabletaxcertificates.lid + "=" + Integer.toString(rsTaxCert.getInt(SMTabletaxcertificates.lid))
							+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
							+ "\">" + Integer.toString(rsTaxCert.getInt(SMTabletaxcertificates.lid)) + "</A></FONT></TD>");
											
						 if(clsDatabaseFunctions.getRecordsetStringValue(rsTaxCert, SMTableorderheaders.strimmedordernumber).compareToIgnoreCase("") != 0){
							pwOut.println( "<TD style=\"padding-right: 20px\"><FONT SIZE=2> " +"<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMDisplayOrderInformation?" 
							+ SMOrderHeader.ParamsOrderNumber + "=" + clsDatabaseFunctions.getRecordsetStringValue(rsTaxCert, SMTableorderheaders.strimmedordernumber)
							+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
							+ "\">" + clsDatabaseFunctions.getRecordsetStringValue(rsTaxCert, SMTableorderheaders.strimmedordernumber) + "</A></FONT></TD>" );			
						 } else{
							 pwOut.println("<TD style=\"padding-right: 20px\"><FONT SIZE=2>&emsp;&emsp;&emsp;</FONT></TD>");
						 }
						 
						pwOut.println("<TD style=\"padding-right: 20px\"><FONT SIZE=2> " + clsDatabaseFunctions.getRecordsetStringValue(rsTaxCert, SMTableorderheaders.sShipToName) + "</FONT></TD>"		
						+ "<TD style=\"padding-right: 20px\"><FONT SIZE=2> " + clsDatabaseFunctions.getRecordsetStringValue(rsTaxCert, SMTabletaxcertificates.sprojectlocation) + "</FONT></TD>"
						+ "<TD style=\"padding-right: 20px\"><FONT SIZE=2> " + clsDatabaseFunctions.getRecordsetStringValue(rsTaxCert, SMTabletaxcertificates.sexemptnumber) + "</FONT></TD>"	
						+ "<TD style=\"padding-right: 20px\"><FONT SIZE=2> " + clsDatabaseFunctions.getRecordsetStringValue(rsTaxCert, SMTabletaxcertificates.staxjurisdiction) + "</FONT></TD>"
						+ "<TD style=\"padding-right: 20px\"><FONT SIZE=2> " + clsDateAndTimeConversions.resultsetDateStringToString(rsTaxCert.getString(SMTabletaxcertificates.datexpired)) + "</FONT></TD>");

						pwOut.println( "</TR>");
						iCertCount++;
					}
				}
				pwOut.println("</TABLE>");
				rsTaxCert.close();
				
				
				
				//View Active Sales Contacts
				pwOut.println("<BR><BR><FONT SIZE=2><B><U>Active Sales Contacts</U>:</B><BR><BR>" );
				
				SQL = "SELECT * "
						+ " FROM " + SMTablesalescontacts.TableName
						+ " WHERE "
							+ "(" + SMTablesalescontacts.binactive + " = 0 ) AND (" + SMTabletaxcertificates.scustomernumber + " = '" + sCustomerNum + "')"
			
					;
				ResultSet rsSalesContact = clsDatabaseFunctions.openResultSet(SQL, conn);
				
				int iSaleContactCount = 0;
				iRowCount = 1;
				sRowColor = "#FFF";
				
				if (iSaleContactCount < 1){
					pwOut.println("<TABLE width=\"100%\" style= \"border-collapse:collapse;\">");
					pwOut.println("<TR bgcolor=\"#FFB8B8\">"
						+ "<TD style=\" border: 1px solid;font-size:small; font-weight:bold; text-align:left;\">ID&nbsp;</TD>"
						+ "<TD style=\" border: 1px solid;font-size:small; font-weight:bold; text-align:left;\">Salesperson&nbsp;Code</TD>"
						+ "<TD style=\" border: 1px solid;font-size:small; font-weight:bold; text-align:left;\">Contact&nbsp;Name</TD>"
						+ "<TD style=\" border: 1px solid;font-size:small; font-weight:bold; text-align:left;\">Phone&nbsp;Number</TD>"
						+ "<TD style=\" border: 1px solid;font-size:small; font-weight:bold; text-align:left;\">Email&nbsp;Address</TD>"
						+ "<TD style=\" border: 1px solid;font-size:small; font-weight:bold; text-align:left;\">Contact&nbsp;Date</TD>"
						+ "<TD style=\" border: 1px solid;font-size:small; font-weight:bold; text-align:left;\">Next&nbsp;Contact&nbsp;Date</TD>"
						+ "<TD style=\" border: 1px solid;font-size:small; font-weight:bold; text-align:left; width:50%;\">Notes</TD>"
						+ "</TR>");	
					while(rsSalesContact.next()){
						iSaleContactCount++;
							iRowCount++;
							if(iRowCount % 2 == 0){
								sRowColor = "#FFF";
							}else{
								sRowColor = "#DCDCDC";
							}

								pwOut.println( "<TR bgcolor=\"" + sRowColor + "\" >\n" 
										+ " <TD style=\"padding-right: 0px\"> <FONT SIZE=2> " + "<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMSalesContactEdit?" + SMTablesalescontacts.id + "=" + Integer.toString(rsSalesContact.getInt(SMTablesalescontacts.id))+ "\">" + Integer.toString(rsSalesContact.getInt(SMTablesalescontacts.id)) + "</A></FONT></TD>\n"
								+ " <TD style=\"padding-right: 20px\"> <FONT SIZE=2> "+ rsSalesContact.getString(SMTablesalescontacts.salespersoncode)+"</FONT></TD>\n"
								+ " <TD style=\"padding-right: 20px\"> <FONT SIZE=2> "+ rsSalesContact.getString(SMTablesalescontacts.scontactname)+"</FONT></TD>\n"
								+ " <TD style=\"padding-right: 20px\"> <FONT SIZE=2> "+ rsSalesContact.getString(SMTablesalescontacts.sphonenumber)+"</FONT></TD>\n"
								+ " <TD style=\"padding-right: 20px\"> <FONT SIZE=2> "+ rsSalesContact.getString(SMTablesalescontacts.semailaddress)+"</FONT></TD>\n"
								+ " <TD style=\"padding-right: 20px\"> <FONT SIZE=2> "+ clsDateAndTimeConversions.resultsetDateStringToString(rsSalesContact.getString(SMTablesalescontacts.datlastcontactdate))+"</FONT></TD>\n"
								+ " <TD style=\"padding-right: 20px\"> <FONT SIZE=2> "+ clsDateAndTimeConversions.resultsetDateStringToString(rsSalesContact.getString(SMTablesalescontacts.datnextcontactdate))+"</FONT></TD>\n"
								+ " <TD style=\"padding-right: 10px\"> <FONT SIZE=2> "+ rsSalesContact.getString(SMTablesalescontacts.mnotes)+"</FONT></TD>\n");
								pwOut.println( "</TR>");
								iCertCount++;
							
						}
						pwOut.println("</TABLE>");
						rsTaxCert.close();
						
				}
				
				
			}else{
				pwOut.println("Customer not found.");
				return false;
			}
			rsCustomer.close();
		}catch (SQLException e){
			pwOut.println("Error opening customer query: " + e.getMessage());
			return false;
		}
		pwOut.println("<BR><B><A HREF = \""+
		SMUtilities.getURLLinkBase(getServletContext()) +"smcontrolpanel.SMSalesContactEdit?" +
		SMTablesalescontacts.id + "=-1" +
		"&SelectedCustomer=" + sCustomerNum
		+"\">Create new Sales Contact</A><B>");
		
		return true;
	}
	private String getGDocUploadLink( String sCustomerNumber, Connection conn, HttpServletRequest req) throws Exception{
		String s = "";

		AROptions aropt = new AROptions();
		SMOption smopt = new SMOption();		
		try {
			smopt.load(conn);
		} catch (Exception e1) {
			throw new Exception("Error [1452003995] getting SM Options - " + e1.getMessage());
		}
		try {
			aropt.load(conn);
		} catch (Exception e1) {
			throw new Exception("Error [1452003996] getting AR Options - " + e1.getMessage());
		}
		String sFolderName =  aropt.getgdrivecustomerfolderprefix() 
				+ sCustomerNumber 
				+ aropt.getgdrivecustomerfoldersuffix();
		
		String sUploadFileLink = smopt.getgdriveuploadfileurl() + "?"
				+ SMCreateGoogleDriveFolderParamDefinitions.parentfolderid + "=" + aropt.getgdrivecustomerparentfolderid()
				+ "&" + SMCreateGoogleDriveFolderParamDefinitions.foldername + "=" + sFolderName
				+ "&" + SMCreateGoogleDriveFolderParamDefinitions.backgroundcolor + "=" + smopt.getBackGroundColor()
				+ "&" + SMCreateGoogleDriveFolderParamDefinitions.returnURL + "=" + SMUtilities.getCreateGDriveReturnURL(req, getServletContext())
				+ "&" + SMCreateGoogleDriveFolderParamDefinitions.recordtype + "=" + SMCreateGDriveFolder.AR_DISPLAYED_CUSTOMER_TYPE_PARAM_VALUE
				+ "&" + SMCreateGoogleDriveFolderParamDefinitions.keyvalue + "=" + sCustomerNumber;
		s += "&nbsp;&nbsp;<FONT SIZE=2><a href=\"" + sUploadFileLink + "\" target=\"_blank\">Upload File(s) to Google Drive</a></FONT>";
		return s;
	}
}
