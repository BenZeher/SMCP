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

import smar.ARUtilities;
import smcontrolpanel.SMAuthenticate;
import smcontrolpanel.SMSystemFunctions;
import smcontrolpanel.SMUtilities;
import ConnectionPool.WebContextParameters;
import SMDataDefinition.SMTablesecurityfunctions;
import SMDataDefinition.SMTablesecuritygroupfunctions;
import SMDataDefinition.SMTablesecurityusergroups;
import ServletUtilities.clsDatabaseFunctions;

public class APMainMenu extends HttpServlet {

	private static final long serialVersionUID = 1L;
	public void doPost(HttpServletRequest request,
			HttpServletResponse response)
	throws ServletException, IOException {

		PrintWriter out = response.getWriter();
		if (!SMAuthenticate.authenticateSMCPCredentials(
				request, 
				response, 
				getServletContext(), 
				SMSystemFunctions.APAccountsPayable))
		{
			return;
		}

		//Get the session info:
		HttpSession CurrentSession = request.getSession(true);
		String subtitle = "Accounts Payable";
		boolean bMobileView = false;
		if (CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_MOBILE) != null){
			String sMobile = (String)CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_MOBILE);
			if ((sMobile.compareToIgnoreCase("Y") == 0)){
				bMobileView = true;
			}
		}
		String sDBID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_DATABASE_ID);

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
				(String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERFIRSTNAME)
				 + " " + (String)CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERLASTNAME),
				(String)CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_DATABASE_ID),
				out,
				(String)CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_LICENSE_MODULE_LEVEL))){
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
					sDBID,
					"MySQL",
					this.toString() + ".buildMenus - UserID: " 
					+ sUserID
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
		arMenu.add(SMSystemFunctions.APEditAPOptions);
		arMenu.add(SMSystemFunctions.APEditVendors);
		arMenu.add(SMSystemFunctions.APEditVendorTerms);
		arMenu.add(SMSystemFunctions.APEditAccountSets);
		arMenu.add(SMSystemFunctions.APEditVendorGroups);
		arMenu.add(SMSystemFunctions.APEdit1099CPRSCodes);
		arMenu.add(SMSystemFunctions.APEditDistributionCodes);
		arMenu.add(SMSystemFunctions.APEditCheckForms);
		arMenu.add(SMSystemFunctions.APResetPostingInProcessFlag);
		
		//....
		
		//Probably should leave this one till last:
		arMenu.add(SMSystemFunctions.APConvertACCPACData);

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
		arMenu.add(SMSystemFunctions.APEditBatches);
		arMenu.add(SMSystemFunctions.APClearPostedAndDeletedBatches);
		arMenu.add(SMSystemFunctions.APControlPayments);
		//arMenu.add(SMSystemFunctions.APYearEndProcessing);
		arMenu.add(SMSystemFunctions.APClearFullyPaidTransactions);
		arMenu.add(SMSystemFunctions.APChangeOrMergeVendorAccounts);
		arMenu.add(SMSystemFunctions.APDeleteInactiveVendorsQuery);

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
		arMenu.add(SMSystemFunctions.APDisplayVendorInformation);
		arMenu.add(SMSystemFunctions.APViewInvoicesOnHold);

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
		arMenu.add(SMSystemFunctions.APAgedPayables);
		arMenu.add(SMSystemFunctions.APVendorTransactionsReport);
		arMenu.add(SMSystemFunctions.APQueryCheckRunLog);
		arMenu.add(SMSystemFunctions.APBatchPostingJournal);

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