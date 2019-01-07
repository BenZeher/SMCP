package smic;

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

import smar.ARUtilities;
import smcontrolpanel.SMAuthenticate;
import smcontrolpanel.SMSystemFunctions;
import smcontrolpanel.SMUtilities;
import ConnectionPool.WebContextParameters;
import SMDataDefinition.SMTablesecurityfunctions;
import SMDataDefinition.SMTablesecuritygroupfunctions;
import SMDataDefinition.SMTablesecurityusergroups;
import ServletUtilities.clsDatabaseFunctions;

public class ICMainMenu extends HttpServlet {

	private static final long serialVersionUID = 1L;
	public void doPost(HttpServletRequest request,
			HttpServletResponse response)
	throws ServletException, IOException {

		PrintWriter out = response.getWriter();
		if (!SMAuthenticate.authenticateSMCPCredentials(
				request, 
				response, 
				getServletContext(), 
				SMSystemFunctions.ICInventoryControl))
		{
			return;
		}

		//Get the session info:
		HttpSession CurrentSession = request.getSession(true);
		String subtitle = "Inventory Control";
		String sDBID = (String)CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_DATABASE_ID);
		boolean bMobileView = false;
		if (CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_MOBILE) != null){
			String sMobile = (String)CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_MOBILE);
			if ((sMobile.compareToIgnoreCase("Y") == 0)){
				bMobileView = true;
			}
		}

		out.println(SMUtilities.getMenuHead(
				subtitle,
				SMUtilities.getInitBackGroundColor(getServletContext(), (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_DATABASE_ID)),
				SMUtilities.DEFAULT_FONT_FAMILY,
				bMobileView,
				getServletContext()
		)
		);
		String sWarning = ARUtilities.get_Request_Parameter("Warning", request);
		if (! sWarning.equalsIgnoreCase("")){
			out.println("<B><FONT COLOR=\"RED\">WARNING: " + sWarning + "</FONT></B><BR>");
		}

		//Print a link to the first page after login:
		if (bMobileView){
			out.println("<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) 
					+ "smcontrolpanel.SMUserLogin?" 
				+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
				+ "\"  style=\"color: darkblue; font-weight:100; \">Return to user login</A><BR>");
			out.println("<A HREF=\"" + WebContextParameters.getdocumentationpageURL(getServletContext()) + "#" + Long.toString(SMSystemFunctions.ARAccountsReceivable) 
					+ "\"  style=\"color: darkblue; font-weight:100; \">Summary</A><BR><BR>");

		}else{
			out.println("<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) 
					+ "smcontrolpanel.SMUserLogin?" 
				+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
				+ "\" >Return to user login</A><BR>");
			out.println("<A HREF=\"" + WebContextParameters.getdocumentationpageURL(getServletContext()) + "#" + Long.toString(SMSystemFunctions.ARAccountsReceivable) 
					+ "\" >Summary</A><BR><BR>");
		}

		if (!buildMenus(
				bMobileView,
				(String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERNAME),
				(String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERID),
				(String)CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_DATABASE_ID),
				out,
				(String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_LICENSE_MODULE_LEVEL)
				)){
			out.println("Error building menus.");
		}
		out.println("</BODY></HTML>");
	}
	private boolean buildMenus(
			boolean bMobileView,
			String sUserName, 
			String sUserID,
			String sConf, 
			PrintWriter pwOut,
			String sLicenseModuleLevel
	){

		ArrayList<Long> arPermittedFunctions = new ArrayList<Long>(0);
		ArrayList<String> arPermittedFunctionNames = new ArrayList<String>(0);
		ArrayList<String> arPermittedFunctionLinks = new ArrayList<String>(0);
		ArrayList<Long> arMenu = new ArrayList<Long>(0); 

		//First, get a list of the permitted functions:
		try{
			String SQL = "SELECT DISTINCT "
				+ SMTablesecurityfunctions.TableName  + "." + SMTablesecurityfunctions.iFunctionID
				+ ", " + SMTablesecurityfunctions.TableName + "." + SMTablesecurityfunctions.slink
				+ ", " + SMTablesecurityfunctions.TableName + "." + SMTablesecurityfunctions.sFunctionName
				+ " FROM"
				+ " " + SMTablesecuritygroupfunctions.TableName 
				+ ", " + SMTablesecurityusergroups.TableName
				+ ", " + SMTablesecurityfunctions.TableName
				+ " WHERE (" 
				+ "(" + SMTablesecuritygroupfunctions.TableName + "." 
				+ SMTablesecuritygroupfunctions.sGroupName 
				+ " = " + SMTablesecurityusergroups.TableName + "." 
				+ SMTablesecurityusergroups.sSecurityGroupName + ")" 

				+ " AND (" + SMTablesecuritygroupfunctions.TableName + "." 
				+ SMTablesecuritygroupfunctions.ifunctionid 
				+ " = " + SMTablesecurityfunctions.TableName + "." 
				+ SMTablesecurityfunctions.iFunctionID + ")" 

				+ " AND (" + SMTablesecurityusergroups.TableName + "." 
				+ SMTablesecurityusergroups.luserid + "=" + sUserID + ")"
				
				+ " AND ((" + SMTablesecurityfunctions.imodulelevelsum + " & " + sLicenseModuleLevel + ") > 0)"
				
				+ ")"
				;
			ResultSet rs = clsDatabaseFunctions.openResultSet(
					SQL, 
					getServletContext(), 
					sConf,
					"MySQL",
					this.toString() + ".buildMenus - User: " + sUserName);

			while(rs.next()){
				arPermittedFunctions.add(rs.getLong(SMTablesecurityfunctions.iFunctionID));
				arPermittedFunctionNames.add(rs.getString(SMTablesecurityfunctions.sFunctionName));
				arPermittedFunctionLinks.add(rs.getString(SMTablesecurityfunctions.slink));
			}
			rs.close();
		}catch(SQLException e){
			System.out.println("Error getting list of permitted functions - " + e.getMessage());
			return false;
		}

		//Set up the layout:
		if (bMobileView){
			pwOut.println(SMUtilities.layoutMobileMenu());
		}

		//Build menus depending on the user's security levels:
		//Master Tables:
		arMenu.clear();
		arMenu.add(SMSystemFunctions.ICEditItems);
		arMenu.add(SMSystemFunctions.ICEditItemPricing);
		arMenu.add(SMSystemFunctions.ICUpdateItemPrices);
		arMenu.add(SMSystemFunctions.ICEditAccountSets);
		arMenu.add(SMSystemFunctions.ICEditCategories);
		arMenu.add(SMSystemFunctions.ICEditPOShipVias);
		arMenu.add(SMSystemFunctions.ICEditICOptions);
		arMenu.add(SMSystemFunctions.ICResetPostingInProcessFlag);
		//arMenu.add(SMSystemFunctions.ICConvertFromACCPAC);

		if (bMobileView){
			SMUtilities.buildMobileMenuTable(
					"Master Tables",
					arPermittedFunctions,
					arPermittedFunctionNames,
					arPermittedFunctionLinks,
					arMenu,
					sConf,
					pwOut,
					getServletContext()
			);

		}else{
			SMUtilities.buildMenuTable(
					"Master Tables",
					arPermittedFunctions,
					arPermittedFunctionNames,
					arPermittedFunctionLinks,
					arMenu,
					sConf,
					pwOut,
					getServletContext()
			);
		}
		//Transactions
		arMenu.clear();
		arMenu.add(SMSystemFunctions.ICEditBatches);
		arMenu.add(SMSystemFunctions.ICEditPhysicalInventory);
		arMenu.add(SMSystemFunctions.ICEditPurchaseOrders);
		arMenu.add(SMSystemFunctions.ICEnterInvoices);
		arMenu.add(SMSystemFunctions.ICAssignPO);
		arMenu.add(SMSystemFunctions.ICClearPostedBatches);
		arMenu.add(SMSystemFunctions.ICSetInactiveItems);
		arMenu.add(SMSystemFunctions.ICDeleteInactiveItems);
		arMenu.add(SMSystemFunctions.ICClearTransactions);
		arMenu.add(SMSystemFunctions.ICClearStatistics);
		arMenu.add(SMSystemFunctions.ICRecreateAPInvoiceExport);
		arMenu.add(SMSystemFunctions.ICBulkTransfers);

		if (bMobileView){
			SMUtilities.buildMobileMenuTable(
					"Transactions",
					arPermittedFunctions,
					arPermittedFunctionNames,
					arPermittedFunctionLinks,
					arMenu,
					sConf,
					pwOut,
					getServletContext()
			);

		}else{
			SMUtilities.buildMenuTable(
					"Transactions",
					arPermittedFunctions,
					arPermittedFunctionNames,
					arPermittedFunctionLinks,
					arMenu,
					sConf,
					pwOut,
					getServletContext()
			);
		}

		//Inquiries:
		arMenu.clear();
		arMenu.add(SMSystemFunctions.ICDisplayItemInformation);
		arMenu.add(SMSystemFunctions.ICItemNumberMatchUp);
		arMenu.add(SMSystemFunctions.ICOnHandByDescriptionSelection);
		arMenu.add(SMSystemFunctions.ICFindItems);
		arMenu.add(SMSystemFunctions.ICFindItemsSortedByMostUsed);
		if (bMobileView){
			SMUtilities.buildMobileMenuTable(
					"Inquiries",
					arPermittedFunctions,
					arPermittedFunctionNames,
					arPermittedFunctionLinks,
					arMenu,
					sConf,
					pwOut,
					getServletContext()
			);

		}else{
			SMUtilities.buildMenuTable(
					"Inquiries",
					arPermittedFunctions,
					arPermittedFunctionNames,
					arPermittedFunctionLinks,
					arMenu,
					sConf,
					pwOut,
					getServletContext()
			);
		}

		//Reports
		arMenu.clear();
		arMenu.add(SMSystemFunctions.ICItemValuationReport);
		arMenu.add(SMSystemFunctions.ICTransactionHistory);
		arMenu.add(SMSystemFunctions.ICPrintUPCLabels);
		arMenu.add(SMSystemFunctions.ICPOReceivingReport);
		arMenu.add(SMSystemFunctions.ICPrintPurchaseOrders);
		arMenu.add(SMSystemFunctions.ICListUnusedPOs);
		arMenu.add(SMSystemFunctions.ICListItemsReceived);
		arMenu.add(SMSystemFunctions.ICQuantitiesOnHandReport);
		arMenu.add(SMSystemFunctions.ICUnderStockedItemReport);
		arMenu.add(SMSystemFunctions.ICPrintReceivingLabels);
		arMenu.add(SMSystemFunctions.ICPrintLabelScanSheets);

		if (bMobileView){
			SMUtilities.buildMobileMenuTable(
					"Reports",
					arPermittedFunctions,
					arPermittedFunctionNames,
					arPermittedFunctionLinks,
					arMenu,
					sConf,
					pwOut,
					getServletContext()
			);

		}else{
			SMUtilities.buildMenuTable(
					"Reports",
					arPermittedFunctions,
					arPermittedFunctionNames,
					arPermittedFunctionLinks,
					arMenu,
					sConf,
					pwOut,
					getServletContext()
			);
		}

		return true;
	}

	public void doGet(HttpServletRequest request,
			HttpServletResponse response)
	throws ServletException, IOException {

		doPost(request, response);
	}
}