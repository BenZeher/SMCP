package smar;

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

import smcontrolpanel.SMAuthenticate;
import smcontrolpanel.SMSystemFunctions;
import smcontrolpanel.SMUtilities;
import ConnectionPool.WebContextParameters;
import SMDataDefinition.SMTablesecurityfunctions;
import SMDataDefinition.SMTablesecuritygroupfunctions;
import SMDataDefinition.SMTablesecurityusergroups;
import ServletUtilities.clsDatabaseFunctions;

public class ARMainMenu extends HttpServlet {
	private static final long serialVersionUID = 1L;
	public void doPost(HttpServletRequest request,
			HttpServletResponse response)
	throws ServletException, IOException {

		PrintWriter out = response.getWriter();
		if(!SMAuthenticate.authenticateSMCPCredentials(
				request, 
				response, 
				getServletContext(), 
				SMSystemFunctions.ARAccountsReceivable))
			{
				return;
			}

		//Get the session info:
		HttpSession CurrentSession = request.getSession(true);
		String subtitle = "Accounts Receivable";
		String sDBID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_DATABASE_ID);
		boolean bMobileView = false;
		if (CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_MOBILE) != null){
			String sMobile = (String)CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_MOBILE);
			if ((sMobile.compareToIgnoreCase("Y") == 0)){
				bMobileView = true;
			}
		}

		out.println(SMUtilities.getMenuHead(
				subtitle,
				SMUtilities.getInitBackGroundColor(getServletContext(), sDBID),
				SMUtilities.DEFAULT_FONT_FAMILY,
				bMobileView,
				getServletContext()
		)
		);
		//Print a link to the first page after login:
		if (bMobileView){
			out.println("<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) 
					+ "smcontrolpanel.SMUserLogin?" 
					+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
					+ "\"  style=\"color: darkblue; font-weight:100;\" >"
					+ "Return to user login</A><BR>");
			out.println("<A HREF=\"" + WebContextParameters.getdocumentationpageURL(getServletContext()) + "#" + Long.toString(SMSystemFunctions.ARAccountsReceivable) 
					+ "\"  style=\"color: darkblue; font-weight:100;\" >Summary</A><BR><BR>");
			
		}else{
			out.println("<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) 
					+ "smcontrolpanel.SMUserLogin?" 
					+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
					+ "\" >"
					+ "Return to user login</A><BR>");
			out.println("<A HREF=\"" + WebContextParameters.getdocumentationpageURL(getServletContext()) + "#" + Long.toString(SMSystemFunctions.ARAccountsReceivable) 
					+ "\" >Summary</A><BR><BR>");

		}

		if (!buildMenus(
				bMobileView,
				(String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERNAME),
				(String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERID),
				(String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERFIRSTNAME) + " "
				+ (String)CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERLASTNAME),
				(String)CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_DATABASE_ID),
				out,
				(String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_LICENSE_MODULE_LEVEL))){
			out.println("Error building menus.");
		}
		out.println("</BODY></HTML>");
	}
	private boolean buildMenus(
			boolean bMobileView,
			String sUserName, 
			String sUserID,
			String sUserFullName,
			String sDBID, 
			PrintWriter pwOut,
			String sLicenseModuleLevel){

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
					sDBID,
					"MySQL",
					this.toString() + ".buildMenus - UserID: " + sUserID
					+ " - "
					+ sUserFullName
					);

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

		//Edit AR Options
		arMenu.add(SMSystemFunctions.AREditAROptions);
		//Edit Account Sets
		arMenu.add(SMSystemFunctions.AREditAccountSets);
		//Edit Price List Codes
		arMenu.add(SMSystemFunctions.AREditPriceListCodes);
		//Edit Terms
		arMenu.add(SMSystemFunctions.AREditTerms);
		//Edit Customer Groups
		arMenu.add(SMSystemFunctions.AREditCustomerGroups);
		//Edit Customers
		arMenu.add(SMSystemFunctions.AREditCustomers);
		//Edit Customer Ship To Locations
		arMenu.add(SMSystemFunctions.AREditCustomerShipToLocations);
		//Reset the Posting-in-process flag
		arMenu.add(SMSystemFunctions.ARResetPostingInProcessFlag);

		if (bMobileView){
			SMUtilities.buildMobileMenuTable(
					"Master Tables",
					arPermittedFunctions,
					arPermittedFunctionNames,
					arPermittedFunctionLinks,
					arMenu,
					sDBID,
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
					sDBID,
					pwOut,
					getServletContext()
			);
		}

		//Transactions
		arMenu.clear();
		//Edit Batches
		arMenu.add(SMSystemFunctions.AREditBatches);
		//Clear posted batches
		arMenu.add(SMSystemFunctions.ARClearpostedbatches);
		//Clear fully paid transactions
		arMenu.add(SMSystemFunctions.ARClearfullypaidtransactions);
		//Year End Processing
		arMenu.add(SMSystemFunctions.ARYearEndProcessing);
		//Renumber/Merge Customers
		arMenu.add(SMSystemFunctions.ARRenumberMergeCustomers);
		//Set Inactive Customers
		arMenu.add(SMSystemFunctions.ARSetInactiveCustomers);
		//Delete Inactive Customers
		arMenu.add(SMSystemFunctions.ARDeleteInactiveCustomers);
		//Clear Monthly Statistics
		arMenu.add(SMSystemFunctions.ARClearMonthlyStatistics);
		//Auto-create Call Sheets
		arMenu.add(SMSystemFunctions.ARAutoCreateCallSheets);
		//Edit Call Sheets
		arMenu.add(SMSystemFunctions.AREditCallSheets);
		//Print Call Sheets
		arMenu.add(SMSystemFunctions.ARPrintCallSheets);

		if (bMobileView){
			SMUtilities.buildMobileMenuTable(
					"Transactions",
					arPermittedFunctions,
					arPermittedFunctionNames,
					arPermittedFunctionLinks,
					arMenu,
					sDBID,
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
					sDBID,
					pwOut,
					getServletContext()
			);
		}

		//Inquiries:
		arMenu.clear();
		//Customer Activity
		arMenu.add(SMSystemFunctions.ARCustomerActivity);
		//Customer Statistics
		arMenu.add(SMSystemFunctions.ARCustomerStatistics);
		//View Chron Log
		arMenu.add(SMSystemFunctions.ARViewChronologicalLog);
		//View customer information
		arMenu.add(SMSystemFunctions.ARDisplayCustomerInformation);

		if (bMobileView){
			SMUtilities.buildMobileMenuTable(
					"Inquiries",
					arPermittedFunctions,
					arPermittedFunctionNames,
					arPermittedFunctionLinks,
					arMenu,
					sDBID,
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
					sDBID,
					pwOut,
					getServletContext()
			);
		}


		//Reports
		arMenu.clear();
		//Posting Journal
		arMenu.add(SMSystemFunctions.ARPostingJournal);
		//Aging Report
		arMenu.add(SMSystemFunctions.ARAgingReport);
		//Activity Report
		arMenu.add(SMSystemFunctions.ARActivityReport);
		//Misc Cash Report
		arMenu.add(SMSystemFunctions.ARMiscCashReport);
		//Print Statements
		arMenu.add(SMSystemFunctions.ARPrintStatements);
		//List Inactive Customers
		arMenu.add(SMSystemFunctions.ARListInactiveCustomers);
		//List Customers on hold
		arMenu.add(SMSystemFunctions.ARListCustomersOnHold);

		if (bMobileView){
			SMUtilities.buildMobileMenuTable(
					"Reports",
					arPermittedFunctions,
					arPermittedFunctionNames,
					arPermittedFunctionLinks,
					arMenu,
					sDBID,
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
					sDBID,
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