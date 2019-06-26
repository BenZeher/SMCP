package smic;

import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import ConnectionPool.WebContextParameters;
import SMDataDefinition.SMTableaptransactionlines;
import SMDataDefinition.SMTableaptransactions;
import SMDataDefinition.SMTableiccosts;
import SMDataDefinition.SMTableicitemlocations;
import SMDataDefinition.SMTableicitemprices;
import SMDataDefinition.SMTableicitems;
import SMDataDefinition.SMTableicitemstatistics;
import SMDataDefinition.SMTableicoptions;
import SMDataDefinition.SMTableicpoheaders;
import SMDataDefinition.SMTableicpolines;
import SMDataDefinition.SMTableicporeceiptheaders;
import SMDataDefinition.SMTableicporeceiptlines;
import SMDataDefinition.SMTableicvendoritems;
import SMDataDefinition.SMTableicvendors;
import SMDataDefinition.SMTableinvoicedetails;
import SMDataDefinition.SMTableinvoiceheaders;
import SMDataDefinition.SMTablelocations;
import SMDataDefinition.SMTableorderdetails;
import SMDataDefinition.SMTableorderheaders;
import SMDataDefinition.SMTablepricelistcodes;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsDateAndTimeConversions;
import ServletUtilities.clsManageBigDecimals;
import ServletUtilities.clsManageRequestParameters;
import ServletUtilities.clsServletUtilities;
import smcontrolpanel.SMAuthenticate;
import smcontrolpanel.SMSystemFunctions;
import smcontrolpanel.SMUtilities;

public class ICDisplayItemInformation extends HttpServlet {

	private static final long serialVersionUID = 1L;

	
	public void doGet(HttpServletRequest request,
				HttpServletResponse response)
				throws ServletException, IOException {

	    response.setContentType("text/html");
		PrintWriter out = response.getWriter();
		if (!SMAuthenticate.authenticateSMCPCredentials(
				request, 
				response, 
				getServletContext(), 
				SMSystemFunctions.ICDisplayItemInformation))
		{
			return;
		}
		String sICDisplayItemInformationWarning = "";

	    //Get the session info:
	    HttpSession CurrentSession = request.getSession(true);
	    String sDBID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_DATABASE_ID);
	    String sUserID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERID);
	    String sUserFirstName = (String)CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERFIRSTNAME);
	    String sUserLastName = (String)CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERLASTNAME);
	    
	    String sCompanyName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_COMPANYNAME);
	    String sLicenseModuleLevel = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_LICENSE_MODULE_LEVEL);
	    
	    //sCallingClass will look like: smcontrolpanel.ARAgedTrialBalanceReport
	    String sCallingClass = clsManageRequestParameters.get_Request_Parameter("CallingClass", request);

    	String sItemNumber = clsManageRequestParameters.get_Request_Parameter("ItemNumber", request).replace(" ", "");

    	boolean bIncludeItemOnOrderInformation = 
    		request.getParameter("IncludeItemOnOrderInformation") != null;

    	boolean bIncludeItemOnInvoiceInformation = 
    		request.getParameter("IncludeItemOnInvoiceInformation") != null;
    	
    	boolean bIncludeItemOnAPInvoiceInformation = 
       		request.getParameter("IncludeItemOnAPInvoiceInformation") != null;
    	boolean bTriedLink = request.getParameter("NoSisters") != null;
    	
    	//boolean bIncludeItemOnSalesOrdersInformation = 
    	//	request.getParameter("IncludeItemOnSalesOrdersInformation") != null;
    	//Always show items on orders:
    	boolean bIncludeItemOnSalesOrdersInformation = true;
    	
    	//boolean bIncludeItemOnPOsInformation = 
    	//	request.getParameter("IncludeItemOnPOsInformation") != null;
    	//Always show items on PO's:
    	boolean bIncludeItemOnPOsInformation = true;
    	
    	//If this is call to a 'sister' company, get that info now:
    	String sSisterCompany = clsManageRequestParameters.get_Request_Parameter("SisterCompany", request);
    	String sSisterCompanyDb = clsManageRequestParameters.get_Request_Parameter("SisterCompanyDb", request);
    	
    	//Customized title
    	String sReportTitle = "Display Item Information";
    	out.println(SMUtilities.SMCPTitleSubBGColor(sReportTitle, "", "#FFFFFF", sCompanyName));
    	
    	if (sSisterCompany.compareToIgnoreCase("") != 0){
    		out.println("<B>NOTE: SHOWING CORRESPONDING ITEM INFORMATION FOR " + sSisterCompany + "</B><BR>");
    	}else if(bTriedLink) {
    		out.println("<B>NOTE: NO COMMON PART NUMBER IN COMPANY </B><BR>");
    	}
    	
    	out.println("<TD><A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMUserLogin?" 
				+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
				+ "\">Return to user login</A><BR>");
	    out.println("<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smic.ICMainMenu?" 
				+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
				+ "\">Return to Inventory Control Main Menu</A><BR>");
	    out.println("<A HREF=\"" + WebContextParameters.getdocumentationpageURL(getServletContext()) + "#" + Long.toString(SMSystemFunctions.ICDisplayItemInformation) 
	    		+ "\">Summary</A><BR>");
	    out.println("<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMURLHistoryList?" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" 
				+ sDBID + "\">Return to...</A><BR>");
	    
    	//Retrieve information
    	Connection conn = clsDatabaseFunctions.getConnection(
    			getServletContext(), 
    			sDBID, 
    			"MySQL", 
    			this.toString() 
    			+ " - UserID: " 
    			+ sUserID
    			+ " - "
    			+ sUserFirstName
    			+ " "
    			+ sUserLastName
    			);
    	if (conn == null){
    		sICDisplayItemInformationWarning = "Unable to get data connection.";
    		response.sendRedirect(
				"" + SMUtilities.getURLLinkBase(getServletContext()) + "" + sCallingClass + "?"
				+ "Warning=" + sICDisplayItemInformationWarning
				+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
    		);			
        	return;
    	}
	    
    	//If there are 'sister companies' with the same part, display links to them here, but ONLY IF
    	//this is NOT a link to a 'sister' company itself:
    	if (sSisterCompany.compareToIgnoreCase("") == 0){
    		createSisterCompanyLinks(sItemNumber, conn, out, sDBID);
    	}
	    //isFunctionPermitted
    	out.println("<B><U>OPTIONS</U></B><BR>");
    	boolean bAllowViewingOrders = false;
    	if (SMSystemFunctions.isFunctionPermitted(SMSystemFunctions.SMViewOrderInformation, sUserID, conn, sLicenseModuleLevel)){
    		bAllowViewingOrders = true;
		    out.println("<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smic.ICDisplayItemInformation?" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
		    		+ "&ItemNumber=" + sItemNumber
		    		+ "&IncludeItemOnOrderInformation=true"
		    		+ "&SisterCompany=" + sSisterCompany
		    		+ "&SisterCompanyDb=" + sSisterCompanyDb
		    		+ "\">List orders including this item</A>&nbsp;&nbsp;");
	    	}
    	
    	if (SMSystemFunctions.isFunctionPermitted(SMSystemFunctions.SMPrintInvoice, sUserID, conn, sLicenseModuleLevel)){
		    out.println("<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smic.ICDisplayItemInformation?" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
		    		+ "&ItemNumber=" + sItemNumber
		    		+ "&IncludeItemOnInvoiceInformation=true"
		    		+ "&SisterCompany=" + sSisterCompany
		    		+ "&SisterCompanyDb=" + sSisterCompanyDb
		    		+ "\">List sales invoices including this item</A>&nbsp;&nbsp;");
    	}
    	
    	if (SMSystemFunctions.isFunctionPermitted(SMSystemFunctions.SMPrintInvoice, sUserID, conn, sLicenseModuleLevel)){
		    out.println("<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smic.ICDisplayItemInformation?" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
		   		+ "&ItemNumber=" + sItemNumber
		   		+ "&IncludeItemOnAPInvoiceInformation=true"
		   		+ "&SisterCompany=" + sSisterCompany
		   		+ "&SisterCompanyDb=" + sSisterCompanyDb
		   		+ "\">List AP invoices including this item</A>&nbsp;&nbsp;");
    	}
	    
    	if (SMSystemFunctions.isFunctionPermitted(SMSystemFunctions.ICPrintUPCLabels, sUserID, conn, sLicenseModuleLevel)){
		    out.println("<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) 
		    		+ "smic.ICPrintUPCSelection?" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
		    		+ "&" + ICPrintUPCAction.PARAM_ITEMNUMMARKER + "1=" + sItemNumber
		    		+ "&" + ICPrintUPCAction.PARAM_QTYMARKER + "1=1"
		    		+ "&" + ICPrintUPCAction.PARAM_NUMPIECESMARKER + "1=1"
		    		+ "\">Print UPC labels</A>&nbsp;&nbsp;");
    	}
    	
       	if (SMSystemFunctions.isFunctionPermitted(SMSystemFunctions.ICTransactionHistory, sUserID, conn, sLicenseModuleLevel)){
		    out.println("<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) 
		    		+ "smic.ICTransactionHistoryGenerate?" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
		    		+ "&StartingItemNumber=" + sItemNumber
		    		+ "&EndingItemNumber=" + sItemNumber
		    		+ "&StartingDate=1/1/1900"
		    		+ "&EndingDate=" + clsDateAndTimeConversions.now("M/d/yyyy")
		    		+ "&TRANSACTIONTYPE0=on"
		    		+ "&TRANSACTIONTYPE1=on"
		    		+ "&TRANSACTIONTYPE2=on"
		    		+ "&TRANSACTIONTYPE3=on"
		    		+ "&TRANSACTIONTYPE4=on"
		    		+ "\">Show transaction history</A>&nbsp;&nbsp;");
    	}

       	if (SMSystemFunctions.isFunctionPermitted(SMSystemFunctions.ICEditItems, sUserID, conn, sLicenseModuleLevel)){
		    out.println("<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) 
		    		+ "smic.ICEditItemsEdit?" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
		    		+ "&" + ICItem.ParamItemNumber + "=" + sItemNumber
		    		+ "&" + "SubmitEdit=Y"
		    		+ "&CallingClass=" + SMUtilities.getFullClassName(this.toString()) 
		    		+ "\">Edit item</A>&nbsp;&nbsp;");
    	}
       	
    	if (!displayItem(
    		conn,
    		sSisterCompanyDb,
    		sItemNumber, 
    		bIncludeItemOnOrderInformation,
    		bIncludeItemOnInvoiceInformation,
    		bIncludeItemOnAPInvoiceInformation,
    		bIncludeItemOnSalesOrdersInformation,
    		bIncludeItemOnPOsInformation,
    		bAllowViewingOrders,
    		out,
    		sLicenseModuleLevel,
    		getServletContext(),
    		sUserID,
    		sDBID)){
    	}
    	
    	clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1547080814]");
    	
	    out.println("</BODY></HTML>");
	}
	private void createSisterCompanyLinks(String sItem, Connection con, PrintWriter pwOut, String sDBID){
		String sSisterCompanyName1 = "";
		String sSisterCompanyName2 = "";
		String sSisterCompanyDb1 = "";
		String sSisterCompanyDb2 = "";
		String sCommonPartNumber = "";
		String sCorrespondingItem = "";
		String sSisterCompany1Link = "";
		String sSisterCompany2Link = "";
		String SQL = "SELECT * FROM " + SMTableicoptions.TableName;
		try {
			ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, con);
			if (rs.next()){
				sSisterCompanyName1 = rs.getString(SMTableicoptions.ssistercompanyname1).trim();
				sSisterCompanyName2 = rs.getString(SMTableicoptions.ssistercompanyname2).trim();
				sSisterCompanyDb1 = rs.getString(SMTableicoptions.ssistercompanydb1).trim();
				sSisterCompanyDb2 = rs.getString(SMTableicoptions.ssistercompanydb2).trim();
			}
			rs.close();
		} catch (SQLException e) {
			pwOut.println("<BR><B>Error reading icoptions - " + e.getMessage() + ".</B><BR>");
		}
		if (sSisterCompanyName1.compareToIgnoreCase("") !=0){
			SQL = "SELECT "
				+ SMTableicitems.sCommonPartNumber
				+ " FROM " + SMTableicitems.TableName
				+ " WHERE ("
					+ SMTableicitems.sItemNumber + " = '" + sItem + "'"
				+ ")"
				;
			try {
				ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, con);
				if (rs.next()){
					sCommonPartNumber = rs.getString(SMTableicitems.sCommonPartNumber);
				}
				rs.close();
			} catch (SQLException e) {
				pwOut.println("<BR><B>Error getting common part number - " + e.getMessage() + ".</B><BR>");
			}
			
			if (sCommonPartNumber.compareToIgnoreCase("") != 0){
				//See if this part has a corresponding item in the sister company:
				SQL = "SELECT " 
					+ SMTableicitems.sItemNumber
					+ " FROM " + sSisterCompanyDb1 + "." + SMTableicitems.TableName
					+ " WHERE ("
						+ "(" + SMTableicitems.sCommonPartNumber
							+ " = '" + sCommonPartNumber + "')"
					+ ")"
					;
				//System.out.println("In " + this.toString() + " corresponding item SQL = " + SQL);
				try {
					ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, con);
					if (rs.next()){
						sCorrespondingItem = rs.getString(SMTableicitems.sItemNumber);
					}
					rs.close();
				} catch (SQLException e) {
					pwOut.println("<BR><B>Error reading sister database 1 - " + e.getMessage() + ".</B><BR>");
				}
			}
			//Finally, if there's a corresponding item, display a link to it:
			if (sCorrespondingItem.compareToIgnoreCase("") != 0){
				sSisterCompany1Link = "<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smic.ICDisplayItemInformation?" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" 
						+ sDBID 
			    		+ "&ItemNumber=" + sCorrespondingItem
			    		+ "&SisterCompany=" + sSisterCompanyName1
			    		+ "&SisterCompanyDb=" + sSisterCompanyDb1
			    		+ "\">Show corresponding item in " + sSisterCompanyName1 + "</A>&nbsp;&nbsp;";
			}else {
				sSisterCompany1Link = "<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smic.ICDisplayItemInformation?" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" 
						+ sDBID 
			    		+ "&ItemNumber=" + sItem
			    		+ "&NoSisters=" + true
			    		+ "\">Show corresponding item in " + sSisterCompanyName1 + "</A>&nbsp;&nbsp;";
			}
		}
		//Check the second company:
		if (sSisterCompanyName2.compareToIgnoreCase("") !=0){
			SQL = "SELECT "
				+ SMTableicitems.sCommonPartNumber
				+ " FROM " + SMTableicitems.TableName
				+ " WHERE ("
					+ SMTableicitems.sItemNumber + " = '" + sItem + "'"
				+ ")"
				;
			try {
				ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, con);
				if (rs.next()){
					sCommonPartNumber = rs.getString(SMTableicitems.sCommonPartNumber);
				}
				rs.close();
			} catch (SQLException e) {
				pwOut.println("<BR><B>Error getting common part number - " + e.getMessage() + ".</B><BR>");
			}
			
			if (sCommonPartNumber.compareToIgnoreCase("") != 0){
				//See if this part has a corresponding item in the sister company:
				SQL = "SELECT " 
					+ SMTableicitems.sItemNumber
					+ " FROM " + sSisterCompanyDb2 + "." + SMTableicitems.TableName
					+ " WHERE ("
						+ "(" + SMTableicitems.sCommonPartNumber
							+ " = '" + sCommonPartNumber + "')"
					+ ")"
					;
				//System.out.println("In " + this.toString() + " corresponding item SQL = " + SQL);
				try {
					ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, con);
					if (rs.next()){
						sCorrespondingItem = rs.getString(SMTableicitems.sItemNumber);
					}
					rs.close();
				} catch (SQLException e) {
					pwOut.println("<BR><B>Error reading sister database 2 - " + e.getMessage() + ".</B><BR>");
				}
			}
			//Finally, if there's a corresponding item, display a link to it:
			if (sCorrespondingItem.compareToIgnoreCase("") != 0){
				sSisterCompany2Link = "<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smic.ICDisplayItemInformation?" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" 
						+ sDBID 
			    		+ "&ItemNumber=" + sCorrespondingItem
			    		+ "&SisterCompany=" + sSisterCompanyName2
			    		+ "&SisterCompanyDb=" + sSisterCompanyDb2
			    		+ "\">Show corresponding item in " + sSisterCompanyName2 + "</A>&nbsp;&nbsp;";
			}else {
				sSisterCompany2Link = "<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smic.ICDisplayItemInformation?" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" 
						+ sDBID 
			    		+ "&ItemNumber=" + sItem
			    		+ "&NoSisters=" + true
			    		+ "\">Show corresponding item in " + sSisterCompanyName2 + "</A>&nbsp;&nbsp;";
			}
		}
		
		String sCombinedLinks = sSisterCompany1Link + sSisterCompany2Link;
		if (sCombinedLinks.trim().compareToIgnoreCase("") !=0){
			pwOut.println(sCombinedLinks + "<BR>");
		}
		
		return;
	}
	private boolean displayItem(
		Connection conn,
		String sDbName,
		String sItemNum,
		boolean bIncludeOrderInformation,
		boolean bIncludeInvoiceInformation,
		boolean bIncludeAPInvoiceInformation,
		boolean bIncludeItemOnSalesOrdersInformation,
		boolean bIncludeItemOnPOsInformation,
		boolean bAllowViewingOrders,
		PrintWriter pwOut,
		String sLicenseModLevel,
		ServletContext context,
		String sUserID,
		String sDBID){
	
		String sFullDatabaseName = "";
		if (sDbName.compareToIgnoreCase("") != 0){
			sFullDatabaseName = sDbName + ".";
		}
		
		String SQL = "SELECT * FROM "
			+ sFullDatabaseName + SMTableicitems.TableName
			+ " WHERE ("
				+ "(" + SMTableicitems.sItemNumber + ") = '" + sItemNum + "'"
			+ ")"
			;

		String sLinks = "<FONT SIZE=2><a href=\"#ItemMaster\">Item Master</a>"
			+ "<FONT SIZE=2>&nbsp;&nbsp;<a href=\"#Statistics\">Statistics</a></FONT>"
			+ "<FONT SIZE=2>&nbsp;&nbsp;<a href=\"#LocationDetails\">Location Details</a></FONT>"
			+ "<FONT SIZE=2>&nbsp;&nbsp;<a href=\"#CostBuckets\">Cost Buckets</a></FONT>"
			+ "<FONT SIZE=2>&nbsp;&nbsp;<a href=\"#ItemPricing\">Item Pricing</a></FONT>"
			+ "<FONT SIZE=2>&nbsp;&nbsp;<a href=\"#VendorItems\">Vendor Items</a></FONT>"
		;
		
		if (bIncludeOrderInformation){
			sLinks = sLinks
			+ "<FONT SIZE=2>&nbsp;&nbsp;<a href=\"#ItemsOnOrders\">Items On Orders</a></FONT>";
		}

		if (bIncludeInvoiceInformation){
			sLinks = sLinks
			+ "<FONT SIZE=2>&nbsp;&nbsp;<a href=\"#ItemsOnInvoices\">Items On Invoices</a></FONT>";
		}
		
		if (bIncludeAPInvoiceInformation){
			sLinks = sLinks
			+ "<FONT SIZE=2>&nbsp;&nbsp;<a href=\"#ItemsOnAPInvoices\">Items On Accounts Payable Invoices</a></FONT>";
		}

		if (bIncludeItemOnPOsInformation){
			sLinks = sLinks
			+ "<FONT SIZE=2>&nbsp;&nbsp;<a href=\"#ItemsOnPurchaseOrders\">Items On Purchase Orders</a></FONT>";
		}
		
		try{
			ResultSet rsItem = clsDatabaseFunctions.openResultSet(SQL, conn);
			
			if(rsItem.next()){
				pwOut.println("<BR><a name=\"ItemMaster\"><TABLE WIDTH=100% BORDER=0><TR><TD ALIGN=LEFT><B><U>Item Master</U></B></TD></TR></TABLE>");
				pwOut.println(sLinks); 
				pwOut.println(
					"<BR>"
					+ "<span style = \" font-weight: bold; font-size: medium; color: black; \" >"
					+ "<BR><B>Item #:</B> " 
					+ "<I>" + rsItem.getString(SMTableicitems.sItemNumber).trim() + "&nbsp;&nbsp;</I>"
					+ "<B>Description:</B> " + "<I>" + rsItem.getString(SMTableicitems.sItemDescription).trim() + "&nbsp;&nbsp</I>"
					+ "</span><BR>"
				);
				
				pwOut.println("<TABLE BORDER=0 WIDTH=100%  cellspacing=0 cellpadding=1>");
				pwOut.println("<tr><TD colspan=2><hr></TD></tr>");
				pwOut.println("<TR>");
				pwOut.println("<TD><FONT SIZE=2><B>Default category: </B>" 
						+ rsItem.getString(SMTableicitems.sCategoryCode) + "</FONT></TD>");
				pwOut.println("<TD>&nbsp;</TD>");
				pwOut.println("</TR>");
				pwOut.println("<TR>");
				pwOut.println("<TD><FONT SIZE=2><B>Default price list: </B>" 
						+ rsItem.getString(SMTableicitems.sDefaultPriceListCode) + "</FONT></TD>");
				pwOut.println("<TD><FONT SIZE=2><B>Picking sequence: </B>" 
						+ rsItem.getString(SMTableicitems.sPickingSequence) + "</FONT></TD>");
				pwOut.println("</TR>");
				pwOut.println("<TR>");
				pwOut.println("<TD><FONT SIZE=2><B>Cost unit of measure: </B>" 
						+ rsItem.getString(SMTableicitems.sCostUnitOfMeasure) + "</FONT></TD>");
				if (rsItem.getInt(SMTableicitems.iTaxable) == 0){
					pwOut.println("<TD><FONT SIZE=2><B>Taxable: </B>NO</FONT></TD>");
				}else{
					pwOut.println("<TD><FONT SIZE=2><B>Taxable: </B>YES</FONT></TD>");
				}
				pwOut.println("</TR>");
				pwOut.println("<TR>");
				if (rsItem.getInt(SMTableicitems.iActive) == 0){
					pwOut.println("<TD><FONT SIZE=2><B>Active: </B>NO</FONT></TD>");
				}else{
					pwOut.println("<TD><FONT SIZE=2><B>Active: </B>YES</FONT></TD>");
				}
	        	String sDate = rsItem.getString(SMTableicitems.datInactive);
	        	String sInactiveDate = sDate.substring(5, 7) + "/" + sDate.substring(8, 10) + "/" + sDate.substring(0, 4);
				pwOut.println("<TD><FONT SIZE=2><B>Inactive date: </B>" 
						+ sInactiveDate + "</FONT></TD>");
				pwOut.println("</TR>");

				//If the user has permission to see orders, add a link to the order here:
				String sDedicatedToOrder = rsItem.getString(SMTableicitems.sDedicatedToOrderNumber);
				String sDedicatedToOrderLink = sDedicatedToOrder;
				if (bAllowViewingOrders){
					//Create a link to the order information:
					sDedicatedToOrderLink = 
						"<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMDisplayOrderInformation?OrderNumber=" 
						+ sDedicatedToOrder + "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
					+ "\">" + sDedicatedToOrder + "</A>"; 
				}
				
				pwOut.println("<TR>");
				pwOut.println("<TD><FONT SIZE=2><B>Dedicated to order: </B>" 
						+ sDedicatedToOrderLink + "</FONT></TD>");
				
				if (rsItem.getInt(SMTableicitems.ilaboritem) == 0){
					pwOut.println("<TD><FONT SIZE=2><B>Labor item? </B>NO</FONT></TD>");
				}else{
					pwOut.println("<TD><FONT SIZE=2><B>Labor item? </B>YES</FONT></TD>");
				}
				pwOut.println("</TR>");
				
				pwOut.println("<TR>");
				pwOut.println("<TD><FONT SIZE=2><B>Most recent cost: </B>" 
						+ clsManageBigDecimals.BigDecimalToScaledFormattedString(
							SMTableicitems.bdmostrecentcostScale, rsItem.getBigDecimal(
								SMTableicitems.bdmostrecentcost)) + "</FONT></TD>");
				if (rsItem.getInt(SMTableicitems.inonstockitem) == 0){
					pwOut.println("<TD><FONT SIZE=2><B>Non stock item? </B>NO</FONT></TD>");
				}else{
					pwOut.println("<TD><FONT SIZE=2><B>Non stock item? </B>YES</FONT></TD>");
				}
				pwOut.println("</TR>");

				pwOut.println("<TR>");
				pwOut.println("<TD><FONT SIZE=2><B>Number of labels: </B>" 
						+ clsManageBigDecimals.BigDecimalToScaledFormattedString(
							SMTableicitems.bdnumberoflabelsScale, rsItem.getBigDecimal(
								SMTableicitems.bdnumberoflabels)) + "</FONT></TD>");
				
				if (rsItem.getInt(SMTableicitems.isuppressitemqtylookup) == 0){
					pwOut.println("<TD><FONT SIZE=2><B>Suppress item qty lookup? </B>NO</FONT></TD>");
				}else{
					pwOut.println("<TD><FONT SIZE=2><B>Suppress item qty lookup? </B>YES</FONT></TD>");
				}
				
				pwOut.println("</TR>");
				
				pwOut.println("<TR>");
				pwOut.println("<TD><FONT SIZE=2><B>Last maintained by: </B>" 
						+ rsItem.getString(SMTableicitems.sLastEditUserFullName) + "</FONT></TD>");
				
	        	sDate = rsItem.getString(SMTableicitems.datLastMaintained);
	        	String sLastMaintainedDate = sDate.substring(5, 7) + "/" + sDate.substring(8, 10) + "/" + sDate.substring(0, 4);
				pwOut.println("<TD><FONT SIZE=2><B>Last maintained on: </B>" 
						+ sLastMaintainedDate + "</FONT></TD>");				
				pwOut.println("</TR>");
				pwOut.println("<TR>");
				pwOut.println("<TD><FONT SIZE = 2><B>Can be purchased through the PO system? </B>");
				String sCanBePurchased = rsItem.getInt(SMTableicitems.icannotbepurchased) == 0 ? "YES" : "NO";
				pwOut.println(" "+sCanBePurchased);
				pwOut.println("</FONT></TD>");
				pwOut.println("<TD><FONT SIZE = 2><B>Can be sold and invoiced through the sales order system? </B>");
				String sCanBeSold = rsItem.getInt(SMTableicitems.icannotbesold) == 0 ? "YES" : "NO";
				pwOut.println(" "+sCanBeSold);
				pwOut.println("</FONT></TD>");
				pwOut.println("</TR>");
				pwOut.println("<TR>");
				pwOut.println("<TD COLSPAN=2><FONT SIZE=2><B>Common Part Number: </B>" 
						+ rsItem.getString(SMTableicitems.sCommonPartNumber) + "</FONT></TD>");
				pwOut.println("</TR>");
				pwOut.println("<TR>");
				pwOut.println("<TD COLSPAN=2><FONT SIZE=2><B>Comment 1: </B>" 
						+ rsItem.getString(SMTableicitems.sComment1) + "</FONT></TD>");
				pwOut.println("</TR>");
				pwOut.println("<TR>");
				pwOut.println("<TD COLSPAN=2><FONT SIZE=2><B>Comment 2: </B>" 
						+ rsItem.getString(SMTableicitems.sComment2) + "</FONT></TD>");
				pwOut.println("</TR>");
				pwOut.println("<TR>");
				pwOut.println("<TD COLSPAN=2><FONT SIZE=2><B>Comment 3: </B>" 
						+ rsItem.getString(SMTableicitems.sComment3) + "</FONT></TD>");
				pwOut.println("</TR>");
				pwOut.println("<TR>");
				pwOut.println("<TD COLSPAN=2><FONT SIZE=2><B>Comment 4: </B>" 
						+ rsItem.getString(SMTableicitems.sComment4) + "</FONT></TD>");
				pwOut.println("</TR>");
				pwOut.println("<TR>");
				pwOut.println("<TD COLSPAN=2><FONT SIZE=2><B>Report group 1: </B>" 
						+ rsItem.getString(SMTableicitems.sreportgroup1) + "</FONT></TD>");
				pwOut.println("</TR>");
				pwOut.println("<TR>");
				pwOut.println("<TD COLSPAN=2><FONT SIZE=2><B>Report group 2: </B>" 
						+ rsItem.getString(SMTableicitems.sreportgroup2) + "</FONT></TD>");
				pwOut.println("</TR>");
				pwOut.println("<TR>");
				pwOut.println("<TD COLSPAN=2><FONT SIZE=2><B>Report group 3: </B>" 
						+ rsItem.getString(SMTableicitems.sreportgroup3) + "</FONT></TD>");
				pwOut.println("</TR>");
				pwOut.println("<TR>");
				pwOut.println("<TD COLSPAN=2><FONT SIZE=2><B>Report group 4: </B>" 
						+ rsItem.getString(SMTableicitems.sreportgroup4) + "</FONT></TD>");
				pwOut.println("</TR>");
				pwOut.println("<TR>");
				pwOut.println("<TD COLSPAN=2><FONT SIZE=2><B>Report group 5: </B>" 
						+ rsItem.getString(SMTableicitems.sreportgroup5) + "</FONT></TD>");
				pwOut.println("</TR>");
				pwOut.println("<TR>");
				pwOut.println("<TD COLSPAN=2><FONT SIZE=2><B>Work order item comment: </B>" 
						+ rsItem.getString(SMTableicitems.sworkordercomment) + "</FONT></TD>");
				pwOut.println("</TR>");

				pwOut.println("</TABLE>");
				
			}else{
				pwOut.println("Item not found.");
				return false;
			}
			rsItem.close();
		}catch (SQLException e){
			pwOut.println("Error opening item query: " + e.getMessage());
			return false;
		}
		
		try{
			SQL = "SELECT * FROM " + sFullDatabaseName + SMTableicitemstatistics.TableName + ", " 
				+ sFullDatabaseName + SMTableicitems.TableName
				+ " WHERE ("
					+ "(" + SMTableicitemstatistics.TableName + "." + SMTableicitemstatistics.sItemNumber 
						+ " = '" + sItemNum + "')"
					+ " AND (" + SMTableicitemstatistics.TableName + "." + SMTableicitemstatistics.sItemNumber + " = "
						+ SMTableicitems.TableName + "." + SMTableicitems.sItemNumber + ")" 
				+ ")"
				
				+ " ORDER BY " + SMTableicitemstatistics.sLocation + ", " 
					+ SMTableicitemstatistics.lYear + " DESC, " 
					+ SMTableicitemstatistics.lMonth + " DESC"
				;
			ResultSet rsItemStatistics = clsDatabaseFunctions.openResultSet(SQL, conn);
			
			pwOut.println("<BR><a name=\"Statistics\"><TABLE WIDTH=100% BORDER=0><TR>"
					+ "<TD ALIGN=LEFT><B><U>Statistics</U></B></TD></TR></TABLE>");
			pwOut.println(sLinks); 
			
			pwOut.println("<TABLE BORDER=1 WIDTH=100%  cellspacing=0 cellpadding=1>");

			//Table heading:
			pwOut.println(
				"<TR>"
				+ "<TD><FONT SIZE=2><B><U>Location</B></U></FONT></TD>"
				+ "<TD><FONT SIZE=2><B><U>Year</B></U></FONT></TD>"
				+ "<TD><FONT SIZE=2><B><U>Month</B></U></FONT></TD>"
				+ "<TD ALIGN=RIGHT><FONT SIZE=2><B><U>Qty Sold</B></U></FONT></TD>"
				+ "<TD ALIGN=RIGHT><FONT SIZE=2><B><U>Amt. Sold</B></U></FONT></TD>"
				+ "<TD ALIGN=RIGHT><FONT SIZE=2><B><U>Cost Of Items Sold</B></U></FONT></TD>"
				+ "<TD ALIGN=RIGHT><FONT SIZE=2><B><U>Qty Returned</B></U></FONT></TD>"
				+ "<TD ALIGN=RIGHT><FONT SIZE=2><B><U>Amt. Returned</B></U></FONT></TD>"
				+ "<TD ALIGN=RIGHT><FONT SIZE=2><B><U>Cost Of Returns</B></U></FONT></TD>"
				+ "</TR>"
			);
			
			while (rsItemStatistics.next()){
				pwOut.println("<TR>");
				pwOut.println("<TD><FONT SIZE=2>"
						+ rsItemStatistics.getString(SMTableicitemstatistics.sLocation)
						+ "</FONT></TD>");
				pwOut.println("<TD><FONT SIZE=2>" 
						+ Long.toString(rsItemStatistics.getLong(SMTableicitemstatistics.lYear))
						+ "</FONT></TD>");
				pwOut.println("<TD><FONT SIZE=2>" 
						+ Long.toString(rsItemStatistics.getLong(SMTableicitemstatistics.lMonth))
						+ "</FONT></TD>");
				pwOut.println("<TD ALIGN=RIGHT><FONT SIZE=2>" + clsManageBigDecimals.BigDecimalToFormattedString(
						"########0.0000", rsItemStatistics.getBigDecimal(SMTableicitemstatistics.bdQtySold))
						+ "</FONT></TD>");
				pwOut.println("<TD ALIGN=RIGHT><FONT SIZE=2>" + clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(
						rsItemStatistics.getBigDecimal(SMTableicitemstatistics.bdAmountSold))
						+ "</FONT></TD>");
				pwOut.println("<TD ALIGN=RIGHT><FONT SIZE=2>" + clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(
						rsItemStatistics.getBigDecimal(SMTableicitemstatistics.bdCostOfItemsSold))
						+ "</FONT></TD>");
				pwOut.println("<TD ALIGN=RIGHT><FONT SIZE=2>" + clsManageBigDecimals.BigDecimalToFormattedString(
						"########0.0000", rsItemStatistics.getBigDecimal(SMTableicitemstatistics.bdQtyReturned))
						+ "</FONT></TD>");
				pwOut.println("<TD ALIGN=RIGHT><FONT SIZE=2>" + clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(
						rsItemStatistics.getBigDecimal(SMTableicitemstatistics.bdAmountReturned))
						+ "</FONT></TD>");
				pwOut.println("<TD ALIGN=RIGHT><FONT SIZE=2>" + clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(
						rsItemStatistics.getBigDecimal(SMTableicitemstatistics.bdCostOfItemsReturned))
						+ "</FONT></TD>");
				
				pwOut.println("</TR>");
			}
			rsItemStatistics.close();
		}catch (SQLException e){
			pwOut.println("Error opening item costs query: " + e.getMessage());
			return false;
		}
		pwOut.println("</TABLE>");
		
		// - Get location details
		try{
			SQL = "SELECT "
				+ " 'Location details for ICDisplayItemInformation' as REPORTNAME"
				+ ", " + SMTableicitemlocations.TableName + "." + SMTableicitemlocations.sItemNumber
				+ ", " + SMTableicitemlocations.TableName + "." + SMTableicitemlocations.sLocation
				+ ", " + SMTableicitemlocations.TableName + "." + SMTableicitemlocations.sQtyOnHand
				+ ", " + SMTableicitemlocations.TableName + "." + SMTableicitemlocations.sTotalCost
				+ ", " + SMTableicitemlocations.TableName + "." + SMTableicitemlocations.sMinQtyOnHand
				+ ", " + SMTableicitems.TableName + "." + SMTableicitems.sCostUnitOfMeasure
				+ ", " + SMTablelocations.TableName + "." + SMTablelocations.sLocationDescription
				+ ", QTYONSALESORDERQUERY.SumOfQtyOrdered"
				+ " FROM " + sFullDatabaseName + SMTableicitemlocations.TableName
				+ " LEFT JOIN ("
				+ "SELECT"
				+ " " + SMTableorderdetails.sItemNumber + " AS ITEMNUM"
				+ ", " + SMTableorderdetails.sLocationCode + " AS DETAILLOC"
				+ ", Sum(" + SMTableorderdetails.TableName + "." + SMTableorderdetails.dQtyOrdered
				+ ") AS SumOfQtyOrdered"
				+ " FROM " + sFullDatabaseName + SMTableorderdetails.TableName + " LEFT JOIN " 
				+ sFullDatabaseName + SMTableorderheaders.TableName + " ON"
				+ " " + sFullDatabaseName + SMTableorderdetails.TableName + "." + SMTableorderdetails.dUniqueOrderID
				+ " = " + sFullDatabaseName + SMTableorderheaders.TableName + "." + SMTableorderheaders.dOrderUniqueifier
				+ " WHERE ("
					+ "(" + SMTableorderdetails.sItemNumber + " = '" + sItemNum + "')"
					+ " AND (" + SMTableorderheaders.datOrderCanceledDate + " < '1990-01-01')"
					+ " AND (" + SMTableorderheaders.TableName + "." + SMTableorderheaders.iOrderType 
					+ " != " + SMTableorderheaders.ORDERTYPE_QUOTE + ")"
				+ ")"
				+ " GROUP BY " + SMTableorderdetails.sItemNumber + ", " + SMTableorderdetails.sLocationCode 
				+ ") AS QTYONSALESORDERQUERY"
				+ " ON " + SMTableicitemlocations.TableName + "." + SMTableicitemlocations.sItemNumber + " = "
				+ " QTYONSALESORDERQUERY.ITEMNUM"
				+ " AND " + SMTableicitemlocations.TableName + "." + SMTableicitemlocations.sLocation + " = "
				+ " QTYONSALESORDERQUERY.DETAILLOC"
				+ " LEFT JOIN " + sFullDatabaseName + SMTablelocations.TableName + " ON "
				+ SMTableicitemlocations.TableName + "." + SMTableicitemlocations.sLocation + " = "
				+ SMTablelocations.TableName + "." + SMTableicitemlocations.sLocation
				+ " LEFT JOIN " + sFullDatabaseName + SMTableicitems.TableName + " ON "
				+ SMTableicitemlocations.TableName + "." + SMTableicitemlocations.sItemNumber + " = "
				+ SMTableicitems.TableName + "." + SMTableicitems.sItemNumber
				+ " WHERE ("
					+ "(" + SMTableicitemlocations.TableName + "." + SMTableicitemlocations.sItemNumber 
						+ " = '" + sItemNum + "')"
				+ ")";
			ResultSet rsItemLocation = clsDatabaseFunctions.openResultSet(SQL, conn);
			
			pwOut.println("<BR><a name=\"LocationDetails\"><TABLE WIDTH=100% BORDER=0><TR>"
					+ "<TD ALIGN=LEFT><B><U>Location details</U></B></TD></TR></TABLE>");
			pwOut.println(sLinks); 
			
			pwOut.println("<TABLE BORDER=1 WIDTH=100%  cellspacing=0 cellpadding=1>");

			//Table heading:
			pwOut.println(
				"<TR>"
				+ "<TD><FONT SIZE=2><B><U>Location</B></U></TD>"
				+ "<TD><FONT SIZE=2><B><U>UOM</B></U></TD>");
			
			if(bIncludeItemOnSalesOrdersInformation){
				pwOut.println("<TD ALIGN=RIGHT><FONT SIZE=2><B><U>Qty On Sales Order</B></U></TD>");
			}
			
			//if(bIncludeItemOnPOsInformation){
			//	pwOut.println("<TD ALIGN=RIGHT><FONT SIZE=2><B><U>Qty On PO</B></U></TD>");
			//}

			pwOut.println(
				"<TD ALIGN=RIGHT><FONT SIZE=2><B><U>Qty On Hand</B></U></TD>"
				+ "<TD ALIGN=RIGHT><FONT SIZE=2><B><U>Cost On Hand</B></U></TD>"
				+ "<TD ALIGN=RIGHT><FONT SIZE=2><B><U>Min Qty On Hand</B></U></TD>"
				+ "</TR>"
			);
			
			while (rsItemLocation.next()){
				pwOut.println("<TR>");
				pwOut.println("<TD><FONT SIZE=2>"
						+ rsItemLocation.getString(SMTableicitemlocations.TableName + "." 
						+ SMTableicitemlocations.sLocation) 
						+ "&nbsp;" + rsItemLocation.getString(SMTablelocations.TableName + "." 
						+ SMTablelocations.sLocationDescription)
						+ "</FONT></TD>");
				pwOut.println("<TD><FONT SIZE=2>" 
						+ rsItemLocation.getString(SMTableicitems.TableName + "." 
						+ SMTableicitems.sCostUnitOfMeasure) + "</FONT></TD>");
				
				/*
				if(bIncludeItemOnSalesOrdersInformation){
					//Get the Qty on Sales Orders:
					SQL = "SELECT"
						+ " " + SMTableorderdetails.sItemNumber
						+ ", Sum(" + SMTableorderdetails.TableName + "." + SMTableorderdetails.dQtyOrdered
						+ ") AS SumOfQtyOrdered"
						+ " FROM " + sFullDatabaseName + SMTableorderdetails.TableName + " INNER JOIN " 
						+ sFullDatabaseName + SMTableorderheaders.TableName + " ON"
						+ " " + sFullDatabaseName + SMTableorderdetails.TableName + "." + SMTableorderdetails.dUniqueOrderID
						+ " = " + sFullDatabaseName + SMTableorderheaders.TableName + "." + SMTableorderheaders.dOrderUniqueifier
						+ " WHERE ("
							+ "(" + SMTableorderdetails.sItemNumber + " = '" + sItemNum + "')"
							+ " AND (" + SMTableorderheaders.datOrderCanceledDate + " < '1990-01-01')"
							+ " AND (" + SMTableorderheaders.TableName + "." + SMTableorderheaders.iOrderType 
							+ " != " + SMTableorderheaders.ORDERTYPE_QUOTE + ")"
							+ " AND (" + SMTableorderdetails.sLocationCode + " = '" 
								+ rsItemLocation.getString(SMTableicitemlocations.TableName + "." 
								+ SMTableicitemlocations.sLocation) + "')"
						+ ")"
						+ " GROUP BY " + SMTableorderdetails.sItemNumber
					;
					
					//System.out.println("In " + this.toString() + " QtyOnSalesORders SQL = " + SQL);
					ResultSet rsQtyOnSalesOrders = SMUtilities.openResultSet(SQL, conn);
					BigDecimal bdQtyOnSalesOrders = BigDecimal.ZERO;
					if (rsQtyOnSalesOrders.next()){
						bdQtyOnSalesOrders = rsQtyOnSalesOrders.getBigDecimal("SumOfQtyOrdered");
					}
					rsQtyOnSalesOrders.close();
					
					pwOut.println("<TD ALIGN=RIGHT><FONT SIZE=2>" + SMUtilities.BigDecimalToFormattedString(
							"########0.0000", bdQtyOnSalesOrders) + "</FONT></TD>");
				}
				*/
				pwOut.println("<TD ALIGN=RIGHT><FONT SIZE=2>" + clsManageBigDecimals.BigDecimalToFormattedString(
						"########0.0000", rsItemLocation.getBigDecimal("SumOfQtyOrdered")) + "</FONT></TD>");

				/*
				if(bIncludeItemOnPOsInformation){
					//Get the Qty on POs:
					SQL = "SELECT"
						+ " " + SMTableicpolines.sitemnumber
						+ ", Sum(" 
							+ SMTableicpolines.bdqtyordered
							+ " - "
							+ SMTableicpolines.bdqtyreceived
						+ ") AS SumOfQtyOnPOs"
						+ " FROM " + sFullDatabaseName + SMTableicpolines.TableName + " INNER JOIN " 
						+ sFullDatabaseName + SMTableicpoheaders.TableName + " ON"
						+ " " + sFullDatabaseName + SMTableicpolines.TableName + "." + SMTableicpolines.lpoheaderid
						+ " = " + sFullDatabaseName + SMTableicpoheaders.TableName + "." + SMTableicpoheaders.lid
						+ " WHERE ("
							+ "(" + SMTableicpolines.sitemnumber + " = '" + sItemNum + "')"
							+ " AND (" + SMTableicpoheaders.lstatus + " != " 
								+ SMTableicpoheaders.STATUS_DELETED + ")"
							+ " AND (" + SMTableicpolines.slocation + " = '" 
								+ rsItemLocation.getString(SMTableicitemlocations.TableName + "." 
								+ SMTableicitemlocations.sLocation) + "')"
						+ ")"
						+ " GROUP BY " + SMTableicpolines.sitemnumber
					;
					
					//System.out.println("In " + this.toString() + " QtyOnSalesORders SQL = " + SQL);
					ResultSet rsQtyOnPOs = SMUtilities.openResultSet(SQL, conn);
					BigDecimal bdQtyOnPOs = BigDecimal.ZERO;
					if (rsQtyOnPOs.next()){
						bdQtyOnPOs = rsQtyOnPOs.getBigDecimal("SumOfQtyOnPOs");
					}
					rsQtyOnPOs.close();
					
					pwOut.println("<TD ALIGN=RIGHT><FONT SIZE=2>" + SMUtilities.BigDecimalToFormattedString(
							"########0.0000", bdQtyOnPOs) + "</FONT></TD>");
				}
				*/
				pwOut.println("<TD ALIGN=RIGHT><FONT SIZE=2>" + clsManageBigDecimals.BigDecimalToFormattedString(
						"########0.0000", rsItemLocation.getBigDecimal(SMTableicitemlocations.TableName + "." 
						+ SMTableicitemlocations.sQtyOnHand)) + "</FONT></TD>");
				pwOut.println("<TD ALIGN=RIGHT><FONT SIZE=2>" + clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(
						rsItemLocation.getBigDecimal(SMTableicitemlocations.TableName + "." 
						+ SMTableicitemlocations.sTotalCost)) + "</FONT></TD>");
				BigDecimal minQtyOnHand = rsItemLocation.getBigDecimal(SMTableicitemlocations.TableName + "." 
						+ SMTableicitemlocations.sMinQtyOnHand);
				minQtyOnHand = minQtyOnHand.equals(null) ? BigDecimal.ZERO : minQtyOnHand;
				pwOut.println("<TD ALIGN=RIGHT><FONT SIZE=2>" + clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(
						minQtyOnHand) + "</FONT></TD>");
				pwOut.println("</TR>");
			}
			rsItemLocation.close();
		}catch (SQLException e){
			pwOut.println("Error opening item locations query: " + e.getMessage());
			return false;
		}
		pwOut.println("</TABLE>");
		
		//Get cost buckets:
		try{
			SQL = "SELECT"
				+ " " + sFullDatabaseName + SMTableiccosts.TableName + ".*"
				+ ", " + sFullDatabaseName + SMTableicitems.TableName + "." + SMTableicitems.sCostUnitOfMeasure
				+ " FROM " + sFullDatabaseName + SMTableiccosts.TableName
				+ " LEFT JOIN " 
				+ sFullDatabaseName + SMTableicitems.TableName
				+ " ON " + sFullDatabaseName + SMTableiccosts.TableName + "." + SMTableiccosts.sItemNumber + " = "
				+ sFullDatabaseName + SMTableicitems.TableName + "." + SMTableicitems.sItemNumber
				+ " LEFT JOIN " + sFullDatabaseName + SMTableicporeceiptlines.TableName 
				+ " ON " +  sFullDatabaseName + SMTableiccosts.TableName + "." + SMTableiccosts.lReceiptLineID + " = "
				+ sFullDatabaseName + SMTableicporeceiptlines.TableName + "." + SMTableicporeceiptlines.lid
				+ " WHERE ("
					+ "(" + SMTableiccosts.TableName + "." + SMTableiccosts.sItemNumber 
						+ " = '" + sItemNum + "')"
				+ ")"
				
				+ " ORDER BY " + SMTableiccosts.sLocation + ", " 
					+ SMTableiccosts.iSource + ", " + SMTableiccosts.datCreationDate
				;
			ResultSet rsItemCosts = clsDatabaseFunctions.openResultSet(SQL, conn);
			
			pwOut.println("<BR><a name=\"CostBuckets\"><TABLE WIDTH=100% BORDER=0><TR>"
					+ "<TD ALIGN=LEFT><B><U>Cost Buckets</U></B></TD></TR></TABLE>");
			pwOut.println(sLinks); 
			
			pwOut.println("<TABLE BORDER=1 WIDTH=100%  cellspacing=0 cellpadding=1>");

			//Table heading:
			pwOut.println(
				"<TR>"
				+ "<TD><FONT SIZE=2><B><U>Location</B></U></FONT></TD>"
				+ "<TD><FONT SIZE=2><B><U>ID</B></U></FONT></TD>"
				+ "<TD><FONT SIZE=2><B><U>Type</B></U></FONT></TD>"
				+ "<TD><FONT SIZE=2><B><U>Date</B></U></FONT></TD>"
				+ "<TD><FONT SIZE=2><B><U>Receipt #</B></U></FONT></TD>"
				+ "<TD><FONT SIZE=2><B><U>UOM</B></U></FONT></TD>"
				+ "<TD ALIGN=RIGHT><FONT SIZE=2><B><U>Remaining Qty</B></U></FONT></TD>"
				+ "<TD ALIGN=RIGHT><FONT SIZE=2><B><U>Remaining Cost</B></U></FONT></TD>"
				//+ "<TD ALIGN=RIGHT><FONT SIZE=2><B><U>Qty shipped</B></U></FONT></TD>"
				//+ "<TD ALIGN=RIGHT><FONT SIZE=2><B><U>Cost shipped</B></U></FONT></TD>"
				+ "</TR>"
			);
			
			while (rsItemCosts.next()){
				pwOut.println("<TR>");
				pwOut.println("<TD><FONT SIZE=2>"
						+ rsItemCosts.getString(SMTableiccosts.sLocation)
						+ "</FONT></TD>");
				pwOut.println("<TD><FONT SIZE=2>"
						+ Long.toString(rsItemCosts.getLong(SMTableiccosts.iId))
						+ "</FONT></TD>");
				pwOut.println("<TD><FONT SIZE=2>" 
						+ SMTableiccosts.getCostSourceLabel(rsItemCosts.getInt(SMTableiccosts.iSource))
						+ "</FONT></TD>");
				pwOut.println("<TD><FONT SIZE=2>" + clsDateAndTimeConversions.utilDateToString(
						rsItemCosts.getDate(
						SMTableiccosts.datCreationDate), "MM/dd/yyyy") + "</FONT></TD>");
				pwOut.println("<TD><FONT SIZE=2>"
						+ clsServletUtilities.Fill_In_Empty_String_For_HTML_Cell(
								rsItemCosts.getString(SMTableiccosts.sReceiptNumber))
						+ "</FONT></TD>");
				pwOut.println("<TD><FONT SIZE=2>"
						+ rsItemCosts.getString(SMTableicitems.sCostUnitOfMeasure)
						+ "</FONT></TD>");
				pwOut.println("<TD ALIGN=RIGHT><FONT SIZE=2>" + clsManageBigDecimals.BigDecimalToFormattedString(
						"########0.0000", rsItemCosts.getBigDecimal(SMTableiccosts.bdQty))
						+ "</FONT></TD>");
				pwOut.println("<TD ALIGN=RIGHT><FONT SIZE=2>" + clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(
						rsItemCosts.getBigDecimal(SMTableiccosts.bdCost))
						+ "</FONT></TD>");
				//pwOut.println("<TD ALIGN=RIGHT><FONT SIZE=2>" + SMUtilities.BigDecimalToFormattedString(
				//		"########0.0000", rsItemCosts.getBigDecimal(SMTableiccosts.bdQtyShipped))
				//		+ "</FONT></TD>");
				//pwOut.println("<TD ALIGN=RIGHT><FONT SIZE=2>" + SMUtilities.BigDecimalTo2DecimalSTDFormat(
				//		rsItemCosts.getBigDecimal(SMTableiccosts.bdCostShipped))
				//		+ "</FONT></TD>");
				pwOut.println("</TR>");
			}
			rsItemCosts.close();
		}catch (SQLException e){
			pwOut.println("Error opening item costs query: " + e.getMessage());
			return false;
		}
		pwOut.println("</TABLE>");
		
		//Get price information:
		try{
			SQL = "SELECT * FROM " + sFullDatabaseName + SMTableicitemprices.TableName + ", " 
				+ sFullDatabaseName + SMTablepricelistcodes.TableName + ","
				+ sFullDatabaseName + SMTableicitems.TableName
				+ " WHERE ("
					+ "(" + SMTableicitemprices.TableName + "." + SMTableicitemprices.sItemNumber 
						+ " = '" + sItemNum + "')"
					+ " AND (" + SMTableicitemprices.TableName + "." + SMTableicitemprices.sItemNumber + " = "
					+ SMTableicitems.TableName + "." + SMTableicitems.sItemNumber + ")" 
					+ " AND (" + SMTableicitemprices.TableName + "." + SMTableicitemprices.sPriceListCode + " = "
					+ SMTablepricelistcodes.TableName + "." + SMTablepricelistcodes.spricelistcode + ")" 
				+ ")"
				+ " ORDER BY " + SMTableicitemprices.TableName + "." + SMTableicitemprices.sPriceListCode 
				;
			ResultSet rsItemPrices = clsDatabaseFunctions.openResultSet(SQL, conn);
			
			pwOut.println("<BR><a name=\"ItemPricing\"><TABLE WIDTH=100% BORDER=0><TR>"
					+ "<TD ALIGN=LEFT><B><U>Item Pricing</U></B></TD></TR></TABLE>");
			pwOut.println(sLinks); 
			
			pwOut.println("<TABLE BORDER=1 WIDTH=100%  cellspacing=0 cellpadding=1>");

			//Table heading:
			pwOut.println(
				"<TR>"
				+ "<TD><FONT SIZE=2><B><U>Price list</B></U></FONT></TD>"
				+ "<TD><FONT SIZE=2><B><U>Description</B></U></FONT></TD>"
				+ "<TD><FONT SIZE=2><B><U>Last maintained</B></U></FONT></TD>"
				+ "<TD><FONT SIZE=2><B><U>By</B></U></FONT></TD>"
				+ "<TD><FONT SIZE=2><B><U>UOM</B></U></FONT></TD>"
				+ "<TD ALIGN=RIGHT><FONT SIZE=2><B><U>Base price</B></U></FONT></TD>"
				+ "<TD ALIGN=RIGHT><FONT SIZE=2><B><U>Price level 1</B></U></FONT></TD>"
				+ "<TD ALIGN=RIGHT><FONT SIZE=2><B><U>Price level 2</B></U></FONT></TD>"
				+ "<TD ALIGN=RIGHT><FONT SIZE=2><B><U>Price level 3</B></U></FONT></TD>"
				+ "<TD ALIGN=RIGHT><FONT SIZE=2><B><U>Price level 4</B></U></FONT></TD>"
				+ "<TD ALIGN=RIGHT><FONT SIZE=2><B><U>Price level 5</B></U></FONT></TD>"
				+ "</TR>"
			);
			
			while (rsItemPrices.next()){
				pwOut.println("<TR>");
				pwOut.println("<TD><FONT SIZE=2>"
						+ rsItemPrices.getString(SMTableicitemprices.sPriceListCode)
						+ "</FONT></TD>");
				pwOut.println("<TD><FONT SIZE=2>"
						+ rsItemPrices.getString(SMTablepricelistcodes.sdescription)
						+ "</FONT></TD>");
				pwOut.println("<TD><FONT SIZE=2>" + clsDateAndTimeConversions.utilDateToString(
						rsItemPrices.getDate(
							SMTableicitemprices.datLastMaintained), "MM/dd/yyyy") + "</FONT></TD>");
				pwOut.println("<TD><FONT SIZE=2>"
						+ rsItemPrices.getString(SMTableicitemprices.sLastEditUserFullName)
						+ "</FONT></TD>");
				pwOut.println("<TD><FONT SIZE=2>"
						+ rsItemPrices.getString(SMTableicitems.sCostUnitOfMeasure)
						+ "</FONT></TD>");
				pwOut.println("<TD ALIGN=RIGHT><FONT SIZE=2>" + clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(
						rsItemPrices.getBigDecimal(SMTableicitemprices.bdBasePrice))
						+ "</FONT></TD>");
				pwOut.println("<TD ALIGN=RIGHT><FONT SIZE=2>" + clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(
						rsItemPrices.getBigDecimal(SMTableicitemprices.bdLevel1Price))
						+ "</FONT></TD>");
				pwOut.println("<TD ALIGN=RIGHT><FONT SIZE=2>" + clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(
						rsItemPrices.getBigDecimal(SMTableicitemprices.bdLevel2Price))
						+ "</FONT></TD>");
				pwOut.println("<TD ALIGN=RIGHT><FONT SIZE=2>" + clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(
						rsItemPrices.getBigDecimal(SMTableicitemprices.bdLevel3Price))
						+ "</FONT></TD>");
				pwOut.println("<TD ALIGN=RIGHT><FONT SIZE=2>" + clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(
						rsItemPrices.getBigDecimal(SMTableicitemprices.bdLevel4Price))
						+ "</FONT></TD>");
				pwOut.println("<TD ALIGN=RIGHT><FONT SIZE=2>" + clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(
						rsItemPrices.getBigDecimal(SMTableicitemprices.bdLevel5Price))
						+ "</FONT></TD>");

				pwOut.println("</TR>");
			}
			rsItemPrices.close();
		}catch (SQLException e){
			pwOut.println("Error opening item prices query: " + e.getMessage());
			return false;
		}
		pwOut.println("</TABLE>");
		//******************
		try{
			SQL = "SELECT * FROM " + sFullDatabaseName + SMTableicvendoritems.TableName 
					+ " LEFT JOIN " + sFullDatabaseName + SMTableicvendors.TableName
					+ " ON " + sFullDatabaseName + SMTableicvendoritems.TableName + "." + SMTableicvendoritems.sVendor
					+ " = " + sFullDatabaseName + SMTableicvendors.TableName + "." + SMTableicvendors.svendoracct
				+ " WHERE ("
					+ "(" + SMTableicvendoritems.sItemNumber 
						+ " = '" + sItemNum + "')"
				+ ")"
				
				+ " ORDER BY " + SMTableicvendoritems.sVendor + ", " 
					+ SMTableicvendoritems.sVendorItemNumber
				;
			//System.out.println("In " + this.toString() + " SQL = " + SQL);
			ResultSet rsVendorItems = clsDatabaseFunctions.openResultSet(SQL, conn);
			
			pwOut.println("<BR><a name=\"VendorItems\"><TABLE WIDTH=100% BORDER=0><TR>"
					+ "<TD ALIGN=LEFT><B><U>Vendor Items</U></B></TD></TR></TABLE>");
			pwOut.println(sLinks); 
			
			pwOut.println("<TABLE BORDER=1 WIDTH=100%  cellspacing=0 cellpadding=1>");

			//Table heading:
			pwOut.println(
				"<TR>"
				+ "<TD><FONT SIZE=2><B><U>Vendor #</B></U></FONT></TD>"
				+ "<TD><FONT SIZE=2><B><U>Vendor name</B></U></FONT></TD>"
				+ "<TD><FONT SIZE=2><B><U>Vendor item #</B></U></FONT></TD>"
				+ "<TD ALIGN=RIGHT><FONT SIZE=2><B><U>Cost</B></U></FONT></TD>"
				+ "<TD ALIGN=RIGHT><FONT SIZE=2><B><U>Comment</B></U></FONT></TD>"
				+ "</TR>"
			);
			
			while (rsVendorItems.next()){
				pwOut.println("<TR>");
				pwOut.println("<TD><FONT SIZE=2>"
						+ rsVendorItems.getString(SMTableicvendoritems.TableName + "." + SMTableicvendoritems.sVendor)
						+ "</FONT></TD>");
				pwOut.println("<TD><FONT SIZE=2>"
						+ rsVendorItems.getString(SMTableicvendors.TableName + "." + SMTableicvendors.sname)
						+ "</FONT></TD>");
				pwOut.println("<TD><FONT SIZE=2>"
						+ rsVendorItems.getString(SMTableicvendoritems.TableName + "." + SMTableicvendoritems.sVendorItemNumber)
						+ "</FONT></TD>");
				pwOut.println("<TD ALIGN=RIGHT><FONT SIZE=2>" + clsManageBigDecimals.BigDecimalToFormattedString(
						"########0.0000", rsVendorItems.getBigDecimal(SMTableicvendoritems.TableName + "." + SMTableicvendoritems.sCost))
						+ "</FONT></TD>");
				pwOut.println("<TD><FONT SIZE=2>"
						+ rsVendorItems.getString(SMTableicvendoritems.TableName + "." + SMTableicvendoritems.sComment)
						+ "</FONT></TD>");
				pwOut.println("</TR>");
			}
			rsVendorItems.close();
		}catch (SQLException e){
			pwOut.println("Error opening vendor items query: " + e.getMessage());
			return false;
		}
		pwOut.println("</TABLE>");
		//******************
		
		//Get order info:
		if (bIncludeOrderInformation){
			try{
				SQL = "SELECT "
					+ SMTableorderheaders.sOrderNumber
					+ ", " + SMTableorderheaders.iOrderType
					+ ", " + SMTableorderheaders.sBillToName
					+ ", " + SMTableorderheaders.sShipToName
					+ ", " + SMTableorderdetails.iLineNumber
					+ ", " + SMTableorderdetails.dQtyOrdered
					+ ", " + SMTableorderdetails.dQtyShipped
					+ ", " + SMTableorderdetails.dQtyShippedToDate
					+ ", " + SMTableorderdetails.sOrderUnitOfMeasure
					+ ", " + SMTableorderdetails.datDetailExpectedShipDate
					+ " FROM " + sFullDatabaseName + SMTableorderdetails.TableName + ", " 
					+ sFullDatabaseName + SMTableorderheaders.TableName
					+ " WHERE ("
						+ "(" + SMTableorderdetails.sItemNumber + " = '" + sItemNum + "')"
						+ " AND (" + SMTableorderdetails.TableName + "." +  SMTableorderdetails.dUniqueOrderID
						+ " = " + SMTableorderheaders.TableName + "." 
						+ SMTableorderheaders.dOrderUniqueifier + ")"
						/* LTO 20140313 WE'd like to show Quotes too
						+ " AND (" + SMTableorderheaders.TableName + "." + SMTableorderheaders.iOrderType + " != " 
						+ SMTableorderheaders.ORDERTYPE_QUOTE + ")"
						*/
					+ ")"
					
					+ " ORDER BY " + SMTableorderheaders.sOrderNumber + ", " 
						+ SMTableorderdetails.iLineNumber
					;
				ResultSet rsOrderLines = clsDatabaseFunctions.openResultSet(SQL, conn);
				
				pwOut.println("<BR><a name=\"ItemsOnOrders\"><TABLE WIDTH=100% BORDER=0><TR>"
						+ "<TD ALIGN=LEFT><B><U>Items on orders</U></B></TD></TR></TABLE>");
				pwOut.println(sLinks); 
				
				pwOut.println("<TABLE BORDER=1 WIDTH=100%  cellspacing=0 cellpadding=1>");
	
				//Table heading:
				pwOut.println(
					"<TR>"
					+ "<TD><FONT SIZE=2><B><U>Order #</B></U></FONT></TD>"
					+ "<TD><FONT SIZE=2><B><U>Order type</B></U></FONT></TD>"
					+ "<TD><FONT SIZE=2><B><U>Bill to</B></U></FONT></TD>"
					+ "<TD><FONT SIZE=2><B><U>Ship to</B></U></FONT></TD>"
					+ "<TD ALIGN=RIGHT><FONT SIZE=2><B><U>Line#</B></U></FONT></TD>"
					+ "<TD><FONT SIZE=2><B><U>UOM</B></U></FONT></TD>"
					+ "<TD><FONT SIZE=2><B><U>Expected<BR>ship date</B></U></FONT></TD>"
					+ "<TD ALIGN=RIGHT><FONT SIZE=2><B><U>Qty ordered</B></U></FONT></TD>"
					+ "<TD ALIGN=RIGHT><FONT SIZE=2><B><U>Qty shipped</B></U></FONT></TD>"
					+ "<TD ALIGN=RIGHT><FONT SIZE=2><B><U>Qty shipped<BR>to date</B></U></FONT></TD>"
					+ "</TR>"
				);
				
				while (rsOrderLines.next()){
					pwOut.println("<TR>");
					pwOut.println("<TD><FONT SIZE=2>"
							+ rsOrderLines.getString(SMTableorderheaders.sOrderNumber)
							+ "</FONT></TD>");
					pwOut.println("<TD><FONT SIZE=2>"
							+ SMTableorderheaders.getOrderTypeDescriptions(rsOrderLines.getInt(SMTableorderheaders.iOrderType))
							+ "</FONT></TD>");
					pwOut.println("<TD><FONT SIZE=2>"
							+ rsOrderLines.getString(SMTableorderheaders.sBillToName)
							+ "</FONT></TD>");
					pwOut.println("<TD><FONT SIZE=2>"
							+ rsOrderLines.getString(SMTableorderheaders.sShipToName)
							+ "</FONT></TD>");
					pwOut.println("<TD ALIGN=RIGHT><FONT SIZE=2>"
							+ Integer.toString(rsOrderLines.getInt(SMTableorderdetails.iLineNumber))
							+ "</FONT></TD>");
					pwOut.println("<TD><FONT SIZE=2>"
							+ rsOrderLines.getString(SMTableorderdetails.sOrderUnitOfMeasure)
							+ "</FONT></TD>");
					pwOut.println(
						"<TD><FONT SIZE=2>" 
						+ clsDateAndTimeConversions.resultsetDateStringToString(
							rsOrderLines.getString(
								SMTableorderdetails.datDetailExpectedShipDate)
							)
					);
					
					//pwOut.println("<TD><FONT SIZE=2>"
					//		+ SMUtilities.utilDateToString(
					//			rsOrderLines.getDate(
					//				SMTableorderdetails.datDetailExpectedShipDate), "MM/dd/yyyy")
					//		+ "</FONT></TD>");
					pwOut.println("<TD ALIGN=RIGHT><FONT SIZE=2>" + clsManageBigDecimals.BigDecimalToFormattedString(
							"########0.0000", rsOrderLines.getBigDecimal(SMTableorderdetails.dQtyOrdered))
							+ "</FONT></TD>");
					pwOut.println("<TD ALIGN=RIGHT><FONT SIZE=2>" + clsManageBigDecimals.BigDecimalToFormattedString(
							"########0.0000", rsOrderLines.getBigDecimal(SMTableorderdetails.dQtyShipped))
							+ "</FONT></TD>");
					pwOut.println("<TD ALIGN=RIGHT><FONT SIZE=2>" + clsManageBigDecimals.BigDecimalToFormattedString(
							"########0.0000", rsOrderLines.getBigDecimal(SMTableorderdetails.dQtyShippedToDate))
							+ "</FONT></TD>");
				}
				rsOrderLines.close();
			}catch (SQLException e){
				pwOut.println("Error opening order details query: " + e.getMessage());
				return false;
			}
			pwOut.println("</TABLE>");
		}
		
		//Get invoice info:
		if (bIncludeInvoiceInformation){
			try{
				SQL = "SELECT "
					+ SMTableinvoiceheaders.TableName + "." 
					+ SMTableinvoiceheaders.sInvoiceNumber
					+ ", " + SMTableinvoiceheaders.sOrderNumber
					+ ", " + SMTableinvoiceheaders.sBillToName
					+ ", " + SMTableinvoiceheaders.sShipToName
					+ ", " + SMTableinvoiceheaders.datInvoiceDate
					+ ", " + SMTableinvoicedetails.iLineNumber
					+ ", " + SMTableinvoicedetails.dQtyShipped
					+ ", " + SMTableinvoicedetails.sUnitOfMeasure
					+ ", " + SMTableinvoicedetails.dExtendedPrice
					+ " FROM " + sFullDatabaseName + SMTableinvoicedetails.TableName + ", " 
						+ sFullDatabaseName + SMTableinvoiceheaders.TableName
					+ " WHERE ("
						+ "(" + SMTableinvoicedetails.sItemNumber + " = '" + sItemNum + "')"
						+ " AND (" + SMTableinvoicedetails.TableName + "." 
						+ SMTableinvoicedetails.sInvoiceNumber
						+ " = " + SMTableinvoiceheaders.TableName + "." 
						+ SMTableinvoiceheaders.sInvoiceNumber + ")"
					+ ")"
					
					+ " ORDER BY " + SMTableinvoiceheaders.TableName + "." 
						+ SMTableinvoiceheaders.sInvoiceNumber + ", " 
						+ SMTableinvoicedetails.iLineNumber
					;
				
				ResultSet rsInvoiceLines = clsDatabaseFunctions.openResultSet(SQL, conn);
				
				pwOut.println("<BR><a name=\"ItemsOnInvoices\"><TABLE WIDTH=100% BORDER=0><TR>"
						+ "<TD ALIGN=LEFT><B><U>Items on invoices</U></B></TD></TR></TABLE>");
				pwOut.println(sLinks); 
				
				pwOut.println("<TABLE BORDER=1 WIDTH=100%  cellspacing=0 cellpadding=1>");
	
				//Table heading:
				pwOut.println(
					"<TR>"
					+ "<TD><FONT SIZE=2><B><U>Invoice #</B></U></FONT></TD>"
					+ "<TD><FONT SIZE=2><B><U>Date</B></U></FONT></TD>"
					+ "<TD><FONT SIZE=2><B><U>Order #</B></U></FONT></TD>"
					+ "<TD><FONT SIZE=2><B><U>Bill to</B></U></FONT></TD>"
					+ "<TD><FONT SIZE=2><B><U>Ship to</B></U></FONT></TD>"
					+ "<TD ALIGN=RIGHT><FONT SIZE=2><B><U>Line #</B></U></FONT></TD>"
					+ "<TD><FONT SIZE=2><B><U>UOM</B></U></FONT></TD>"
					+ "<TD ALIGN=RIGHT><FONT SIZE=2><B><U>Qty shipped</B></U></FONT></TD>"
					+ "<TD ALIGN=RIGHT><FONT SIZE=2><B><U>Extended price</B></U></FONT></TD>"
					+ "</TR>"
				);
				
				while (rsInvoiceLines.next()){
					pwOut.println("<TR>");
					pwOut.println("<TD><FONT SIZE=2>"
							+ rsInvoiceLines.getString(SMTableinvoiceheaders.sInvoiceNumber)
							+ "</FONT></TD>");
					pwOut.println("<TD><FONT SIZE=2>"
							+ clsDateAndTimeConversions.utilDateToString(
								rsInvoiceLines.getDate(
									SMTableinvoiceheaders.datInvoiceDate), "MM/dd/yyyy")
							+ "</FONT></TD>");
					pwOut.println("<TD><FONT SIZE=2>"
							+ rsInvoiceLines.getString(SMTableinvoiceheaders.sOrderNumber)
							+ "</FONT></TD>");
					pwOut.println("<TD><FONT SIZE=2>"
							+ rsInvoiceLines.getString(SMTableinvoiceheaders.sBillToName)
							+ "</FONT></TD>");
					pwOut.println("<TD><FONT SIZE=2>"
							+ rsInvoiceLines.getString(SMTableinvoiceheaders.sShipToName)
							+ "</FONT></TD>");
					pwOut.println("<TD ALIGN=RIGHT><FONT SIZE=2>"
							+ rsInvoiceLines.getInt(SMTableinvoicedetails.iLineNumber)
							+ "</FONT></TD>");
					pwOut.println("<TD><FONT SIZE=2>"
							+ rsInvoiceLines.getString(SMTableinvoicedetails.sUnitOfMeasure)
							+ "</FONT></TD>");
					pwOut.println("<TD ALIGN=RIGHT><FONT SIZE=2>" + clsManageBigDecimals.BigDecimalToFormattedString(
							"########0.0000", rsInvoiceLines.getBigDecimal(SMTableinvoicedetails.dQtyShipped))
							+ "</FONT></TD>");
					pwOut.println("<TD ALIGN=RIGHT><FONT SIZE=2>" + clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(
							rsInvoiceLines.getBigDecimal(SMTableinvoicedetails.dExtendedPrice))
							+ "</FONT></TD>");
				}
				rsInvoiceLines.close();
			}catch (SQLException e){
				pwOut.println("Error opening invoice details query: " + e.getMessage());
				return false;
			}
			pwOut.println("</TABLE>");
		}
		
		//Get AP invoice information:
		if (bIncludeAPInvoiceInformation){
			//Get permissions for the links:
			
			boolean bViewInvoiceLinks = SMSystemFunctions.isFunctionPermitted(SMSystemFunctions.APViewTransactionInformation, sUserID, conn, sLicenseModLevel);
			boolean bViewPOLinks = SMSystemFunctions.isFunctionPermitted(SMSystemFunctions.ICEditPurchaseOrders, sUserID, conn, sLicenseModLevel);
			boolean bViewReceiptLinks = SMSystemFunctions.isFunctionPermitted(SMSystemFunctions.ICEditReceipts, sUserID, conn, sLicenseModLevel);

			try{
				SQL = "SELECT "
					+ SMTableaptransactionlines.TableName + "." + SMTableaptransactionlines.bdamount
					+ ", " + SMTableaptransactionlines.TableName + "." + SMTableaptransactionlines.bdqtyreceived
					+ ", " + SMTableaptransactionlines.TableName + "." + SMTableaptransactionlines.lpoheaderid
					+ ", " + SMTableaptransactionlines.TableName + "." + SMTableaptransactionlines.lporeceiptlineid
					+ ", " + SMTableaptransactionlines.TableName + "." + SMTableaptransactionlines.lreceiptheaderid
					+ ", " + SMTableaptransactionlines.TableName + "." + SMTableaptransactionlines.sdistributionacct
					+ ", " + SMTableaptransactionlines.TableName + "." + SMTableaptransactionlines.sitemdescription
					+ ", " + SMTableaptransactionlines.TableName + "." + SMTableaptransactionlines.sunitofmeasure
					+ ", " + SMTableaptransactions.TableName + "." + SMTableaptransactions.datdocdate
					+ ", " + SMTableaptransactions.TableName + "." + SMTableaptransactions.idoctype
					+ ", " + SMTableaptransactions.TableName + "." + SMTableaptransactions.lid
					+ ", " + SMTableaptransactions.TableName + "." + SMTableaptransactions.sdocnumber
					+ ", " + SMTableaptransactions.TableName + "." + SMTableaptransactions.staxjurisdiction
					+ ", " + SMTableaptransactions.TableName + "." + SMTableaptransactions.staxtype
					+ ", " + SMTableaptransactions.TableName + "." + SMTableaptransactions.svendor
					+ ", " + SMTableicvendors.TableName + "." + SMTableicvendors.sname
					
					+ " FROM " + sFullDatabaseName + SMTableaptransactionlines.TableName 
					+ " LEFT JOIN " + sFullDatabaseName + SMTableaptransactions.TableName + " ON "
					+ sFullDatabaseName + SMTableaptransactionlines.TableName + "." + SMTableaptransactionlines.ltransactionheaderid 
					+ " = " + sFullDatabaseName + SMTableaptransactions.TableName + "." + SMTableaptransactions.lid
					
					+ " LEFT JOIN " + sFullDatabaseName + SMTableicvendors.TableName + " ON "
					+ sFullDatabaseName + SMTableaptransactions.TableName + "." + SMTableaptransactions.svendor + " = " 
					+ sFullDatabaseName + SMTableicvendors.TableName + "." + SMTableicvendors.svendoracct
					
					+ " WHERE ("
						+ "(" + SMTableaptransactionlines.TableName + "." + SMTableaptransactionlines.sitemnumber + " = '" + sItemNum + "')"
						+ " AND (" + SMTableaptransactions.TableName + "." + SMTableaptransactions.idoctype + " = " + Integer.toString(SMTableaptransactions.AP_TRANSACTION_TYPE_INVOICE_INVOICE) + ")"
					+ ")"
					+ " ORDER BY " + SMTableaptransactions.TableName + "." + SMTableaptransactions.datdocdate + ", " 
						+ SMTableaptransactions.TableName + "." + SMTableaptransactions.sdocnumber + ", "
						+ SMTableaptransactionlines.TableName + "." + SMTableaptransactionlines.lid
					;
				
				//System.out.println("[1521932700] - SQL: " + SQL);
				
				ResultSet rsAPInvoiceLines = clsDatabaseFunctions.openResultSet(SQL, conn);
				
				pwOut.println("<BR><a name=\"ItemsOnAPInvoices\"><TABLE WIDTH=100% BORDER=0><TR>"
						+ "<TD ALIGN=LEFT><B><U>Items on Accounts Payable invoices</U></B></TD></TR></TABLE>");
				pwOut.println(sLinks); 
				
				pwOut.println("<TABLE BORDER=1 WIDTH=100%  cellspacing=0 cellpadding=1>");
	
				//Table heading:
				pwOut.println(
					"<TR>"
					+ "<TD><FONT SIZE=2><B><U>Invoice #</B></U></FONT></TD>"
					+ "<TD><FONT SIZE=2><B><U>Date</B></U></FONT></TD>"
					+ "<TD><FONT SIZE=2><B><U>Tax jurisdiction</B></U></FONT></TD>"
					+ "<TD><FONT SIZE=2><B><U>Vendor</B></U></FONT></TD>"
					+ "<TD><FONT SIZE=2><B><U>PO #</B></U></FONT></TD>"
					+ "<TD><FONT SIZE=2><B><U>Receipt #</B></U></FONT></TD>"
					+ "<TD ALIGN=RIGHT><FONT SIZE=2><B><U>Qty</B></U></FONT></TD>"
					+ "<TD><FONT SIZE=2><B><U>Description</B></U></FONT></TD>"
					+ "<TD><FONT SIZE=2><B><U>UOM</B></U></FONT></TD>"
					+ "<TD ALIGN=RIGHT><FONT SIZE=2><B><U>Extended cost</B></U></FONT></TD>"
					+ "</TR>"
				);
				
				while (rsAPInvoiceLines.next()){
					
					String sInvNumber = rsAPInvoiceLines.getString(SMTableaptransactions.TableName + "." + SMTableaptransactions.sdocnumber);
					if (bViewInvoiceLinks){
						sInvNumber =
							"<A HREF=\"" + SMUtilities.getURLLinkBase(context) + "smap.APViewTransactionInformation?"
							+ SMTableaptransactions.lid + "=" + Long.toString(rsAPInvoiceLines.getLong(SMTableaptransactions.TableName + "." + SMTableaptransactions.lid))
							+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
							+ "\">" + sInvNumber + "</A>"
						;
					}
					
					String sPONumber = Long.toString(rsAPInvoiceLines.getLong(SMTableaptransactionlines.TableName + "." + SMTableaptransactionlines.lpoheaderid));
					if (bViewPOLinks){
						sPONumber =
							"<A HREF=\"" + SMUtilities.getURLLinkBase(context) + "smic.ICEditPOEdit?"
							+ SMTableicpoheaders.lid + "=" + sPONumber
							+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
							+ "\">" + sPONumber + "</A>"
						;
					}
					
					String sReceiptNumber = Long.toString(rsAPInvoiceLines.getLong(SMTableaptransactionlines.TableName + "." + SMTableaptransactionlines.lreceiptheaderid));
					if (bViewReceiptLinks){
						sReceiptNumber =
							"<A HREF=\"" + SMUtilities.getURLLinkBase(context) + "smic.ICEditReceiptEdit?"
							+ SMTableicporeceiptheaders.lid + "=" + sReceiptNumber
							+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
							+ "\">" + sReceiptNumber + "</A>"
						;
					}
					
					pwOut.println("<TR>");
					pwOut.println("<TD><FONT SIZE=2>"
							+ sInvNumber
							+ "</FONT></TD>");
					pwOut.println("<TD><FONT SIZE=2>"
							+ clsDateAndTimeConversions.utilDateToString(
								rsAPInvoiceLines.getDate(SMTableaptransactions.TableName + "." + SMTableaptransactions.datdocdate),
								"MM/dd/yyyy"
							)
							+ "</FONT></TD>");
					pwOut.println("<TD><FONT SIZE=2>"
							+ rsAPInvoiceLines.getString(SMTableaptransactions.TableName + "." + SMTableaptransactions.staxjurisdiction)
							+ " - " + rsAPInvoiceLines.getString(SMTableaptransactions.TableName + "." + SMTableaptransactions.staxtype)
							+ "</FONT></TD>");
					pwOut.println("<TD><FONT SIZE=2>"
							+ rsAPInvoiceLines.getString(SMTableaptransactions.TableName + "." + SMTableaptransactions.svendor)
							+ " - " + rsAPInvoiceLines.getString(SMTableicvendors.TableName + "." + SMTableicvendors.sname)
							+ "</FONT></TD>");
					pwOut.println("<TD><FONT SIZE=2>"
							+ sPONumber
							+ "</FONT></TD>");
					pwOut.println("<TD><FONT SIZE=2>"
							+ sReceiptNumber
							+ "</FONT></TD>");
					pwOut.println("<TD ALIGN=RIGHT><FONT SIZE=2>" + clsManageBigDecimals.BigDecimalToFormattedString(
							"########0.0000", rsAPInvoiceLines.getBigDecimal(SMTableaptransactionlines.TableName + "." + SMTableaptransactionlines.bdqtyreceived))
							+ "</FONT></TD>");
					pwOut.println("<TD><FONT SIZE=2>"
							+ rsAPInvoiceLines.getString(SMTableaptransactionlines.TableName + "." + SMTableaptransactionlines.sitemdescription)
							+ "</FONT></TD>");
					pwOut.println("<TD><FONT SIZE=2>"
							+ rsAPInvoiceLines.getString(SMTableaptransactionlines.TableName + "." + SMTableaptransactionlines.sunitofmeasure)
							+ "</FONT></TD>");

					pwOut.println("<TD ALIGN=RIGHT><FONT SIZE=2>" + clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(
							rsAPInvoiceLines.getBigDecimal(SMTableaptransactionlines.TableName + "." + SMTableaptransactionlines.bdamount))
							+ "</FONT></TD>");
				}
				rsAPInvoiceLines.close();
			}catch (SQLException e){
				pwOut.println("Error opening AP invoice details query: " + e.getMessage());
				return false;
			}
			pwOut.println("</TABLE>");
		}

		//Get purchase order info:
		if (bIncludeItemOnPOsInformation){
			try{
				SQL = "SELECT "
					+ SMTableicpoheaders.TableName + "." + SMTableicpoheaders.datexpecteddate
					+ ", " + SMTableicpoheaders.TableName + "." + SMTableicpoheaders.datpodate
					+ ", " + SMTableicpoheaders.TableName + "." + SMTableicpoheaders.lid
					+ ", " + SMTableicpoheaders.TableName + "." + SMTableicpoheaders.lstatus
					+ ", " + SMTableicpoheaders.TableName + "." + SMTableicpoheaders.sgdoclink
					+ ", " + SMTableicpolines.TableName + "." + SMTableicpolines.bdqtyordered
					+ ", " + SMTableicpolines.TableName + "." + SMTableicpolines.bdqtyreceived
					+ ", " + SMTableicpolines.TableName + "." + SMTableicpolines.slocation
					+ ", " + SMTableicpolines.TableName + "." + SMTableicpolines.sunitofmeasure
					+ ", QTYPENDING"
					+ " FROM " + sFullDatabaseName + SMTableicpolines.TableName + " LEFT JOIN " 
					+ sFullDatabaseName + SMTableicpoheaders.TableName + " ON "
					+ sFullDatabaseName + SMTableicpoheaders.TableName + "." + SMTableicpoheaders.lid + " = " 
					+ sFullDatabaseName + SMTableicpolines.TableName + "." + SMTableicpolines.lpoheaderid
					
					+ " LEFT JOIN ("
					+ "SELECT"
					+ " " + SMTableicporeceiptlines.lpolineid + " AS POLINEID"
					+ ", SUM(" + SMTableicporeceiptlines.bdqtyreceived + ") AS QTYPENDING"
					+ " FROM " + sFullDatabaseName + SMTableicporeceiptlines.TableName + " LEFT JOIN "
					+ sFullDatabaseName + SMTableicporeceiptheaders.TableName + " ON "
					+ sFullDatabaseName + SMTableicporeceiptlines.TableName + "." + SMTableicporeceiptlines.lreceiptheaderid + " = "
					+ sFullDatabaseName + SMTableicporeceiptheaders.TableName + "." + SMTableicporeceiptheaders.lid
					+ " WHERE ("
						+ "(" + SMTableicporeceiptheaders.TableName + "." + SMTableicporeceiptheaders.lpostedtoic + " = 0)"
						+ " AND (" + SMTableicporeceiptheaders.TableName + "." + SMTableicporeceiptheaders.lstatus + " = 0)"
					+ ")"
					+ " GROUP BY " + SMTableicporeceiptlines.lpolineid
					+ ") AS PENDINGRCPTS "
					+ " ON " + sFullDatabaseName + SMTableicpolines.TableName + "." + SMTableicpolines.lid + " = PENDINGRCPTS.POLINEID"
					
					+ " WHERE ("
						+ "(" + SMTableicpolines.TableName + "." + SMTableicpolines.sitemnumber + " = '" + sItemNum + "')"
						//+ " AND (" + SMTableicpolines.TableName + "." + SMTableicpolines.bdqtyordered + " > "
						//+ SMTableicpolines.TableName + "." + SMTableicpolines.bdqtyreceived + ")"
						//+ " AND (" + SMTableicpoheaders.lstatus + " != " + Integer.toString(SMTableicpoheaders.STATUS_COMPLETE) + ")"
						+ " AND (" + SMTableicpoheaders.lstatus + " != " + Integer.toString(SMTableicpoheaders.STATUS_DELETED) + ")"
					+ ")"
					
					+ " ORDER BY " + SMTableicpoheaders.datpodate + " DESC"
					;
				ResultSet rsPurchaseOrderLines = clsDatabaseFunctions.openResultSet(SQL, conn);
				
				pwOut.println("<BR><a name=\"ItemsOnPurchaseOrders\"><TABLE WIDTH=100% BORDER=0><TR>"
						+ "<TD ALIGN=LEFT><B><U>Items on purchase orders</U></B></TD></TR></TABLE>");
				pwOut.println(sLinks); 
				
				pwOut.println("<TABLE BORDER=1 WIDTH=100%  cellspacing=0 cellpadding=1>");
	
				//Table heading:
				pwOut.println(
					"<TR>"
					+ "<TD><FONT SIZE=2><B><U>PO #</B></U></FONT></TD>"
					+ "<TD><FONT SIZE=2><B><U>PO date</B></U></FONT></TD>"
					+ "<TD><FONT SIZE=2><B><U>View ?</B></U></FONT></TD>"
					+ "<TD><FONT SIZE=2><B><U>Doc folder</B></U></FONT></TD>"
					+ "<TD><FONT SIZE=2><B><U>PO Status</B></U></FONT></TD>"
					+ "<TD><FONT SIZE=2><B><U>Expected date</B></U></FONT></TD>"
					+ "<TD><FONT SIZE=2><B><U>Location</B></U></FONT></TD>"
					+ "<TD ALIGN=RIGHT><FONT SIZE=2><B><U>Qty ordered</B></U></FONT></TD>"
					+ "<TD ALIGN=RIGHT><FONT SIZE=2><B><U>Qty received<BR>(posted)</B></U></FONT></TD>"
					+ "<TD ALIGN=RIGHT><FONT SIZE=2><B><U>Qty received<BR>(NOT posted)</B></U></FONT></TD>"
					+ "<TD><FONT SIZE=2><B><U>Unit of measure</B></U></FONT></TD>"
					+ "</TR>"
				);
				
				boolean bAllowPOEditing = SMSystemFunctions.isFunctionPermitted(
					SMSystemFunctions.ICEditPurchaseOrders, 
					sUserID, 
					conn,
					sLicenseModLevel);
				boolean bAllowPODocViewing = SMSystemFunctions.isFunctionPermitted(
					SMSystemFunctions.ICViewPODocuments, 
					sUserID, 
					conn,
					sLicenseModLevel);
				boolean bAllowPOPrinting = SMSystemFunctions.isFunctionPermitted(
					SMSystemFunctions.ICPrintPurchaseOrders, 
					sUserID, 
					conn,
					sLicenseModLevel);
				
				while (rsPurchaseOrderLines.next()){
					pwOut.println("<TR>");
					
					String sPOId = Long.toString(rsPurchaseOrderLines.getLong(SMTableicpoheaders.lid));
					String sPOLink = sPOId;
					if (bAllowPOEditing){
						sPOLink = "<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smic.ICEditPOEdit"
								+ "?" + ICPOHeader.Paramlid + "=" + sPOId
								+ "&CallingClass=" + "smic.ICEditPOSelection"
					    		+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
					    		+ "\">" + clsServletUtilities.Fill_In_Empty_String_For_HTML_Cell(sPOId) + "</A>";
					}
					pwOut.println("<TD><FONT SIZE=2>"
							+ sPOLink
							+ "</FONT></TD>");
					pwOut.println(
							"<TD><FONT SIZE=2>" 
							+ clsDateAndTimeConversions.resultsetDateStringToString(
								rsPurchaseOrderLines.getString(
										SMTableicpoheaders.datpodate)
								)
							)
					;
					
					//Link to view PO:
					String sPrintPOLink = "N/A";
					if (bAllowPOPrinting){
						sPrintPOLink = "<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smic.ICPrintPOGenerate"
						+ "?" + "StartingPOID" + "=" + sPOId
						+ "&" + "EndingPOID" + "="
						+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID + "\">" + "View" + "</A>";
					}
					pwOut.println("<TD><FONT SIZE=2>"
							+ sPrintPOLink
							+ "</FONT></TD>");
					
					//Doc folder link
					String sGDocLink = "N/A";
					String sGDoc = rsPurchaseOrderLines.getString(SMTableicpoheaders.TableName + "." + SMTableicpoheaders.sgdoclink);
					if (sGDoc == null){
						sGDoc = "";
					}
					if (
						(bAllowPODocViewing)
						&& (sGDoc.compareToIgnoreCase("") != 0)
					){
						sGDocLink = "<A HREF=\"" 
							+ sGDoc
							+ "\">"
							+ "Documents"
							+ "</A>"
						;
					}
					pwOut.println("<TD><FONT SIZE=2>" + sGDocLink + "</FONT></TD>"); 
					//PO Status
					pwOut.println("<TD><FONT SIZE=2>"
							+ SMTableicpoheaders.getStatusDescription(rsPurchaseOrderLines.getInt(
								SMTableicpoheaders.TableName + "." + SMTableicpoheaders.lstatus))
							+ "</FONT></TD>");

					pwOut.println(
							"<TD><FONT SIZE=2>" 
							+ clsDateAndTimeConversions.resultsetDateStringToString(
								rsPurchaseOrderLines.getString(
										SMTableicpoheaders.datexpecteddate)
								)
							);							
					
					pwOut.println("<TD><FONT SIZE=2>"
							+ rsPurchaseOrderLines.getString(SMTableicpolines.slocation)
							+ "</FONT></TD>");
					pwOut.println("<TD ALIGN=RIGHT><FONT SIZE=2>" + clsManageBigDecimals.BigDecimalToFormattedString(
							"########0.0000", rsPurchaseOrderLines.getBigDecimal(SMTableicpolines.bdqtyordered))
							+ "</FONT></TD>");
					BigDecimal bdQtyReceivedNotPosted = new BigDecimal("0.00");
					bdQtyReceivedNotPosted = rsPurchaseOrderLines.getBigDecimal("QTYPENDING");
					if (bdQtyReceivedNotPosted == null){
						bdQtyReceivedNotPosted = BigDecimal.ZERO;
					}
					pwOut.println("<TD ALIGN=RIGHT><FONT SIZE=2>" + clsManageBigDecimals.BigDecimalToFormattedString(
							"########0.0000", 
							rsPurchaseOrderLines.getBigDecimal(SMTableicpolines.bdqtyreceived).subtract(bdQtyReceivedNotPosted)
							)
							+ "</FONT></TD>");
					pwOut.println("<TD ALIGN=RIGHT><FONT SIZE=2>" + clsManageBigDecimals.BigDecimalToFormattedString(
							"########0.0000", bdQtyReceivedNotPosted)
							+ "</FONT></TD>");
					
					pwOut.println("<TD><FONT SIZE=2>"
							+ rsPurchaseOrderLines.getString(SMTableicpolines.sunitofmeasure)
							+ "</FONT></TD>");
				}
				rsPurchaseOrderLines.close();
			}catch (SQLException e){
				pwOut.println("Error opening purchase order details query: " + e.getMessage());
				return false;
			}
			pwOut.println("</TABLE>");
		}
		return true;
	}
}
