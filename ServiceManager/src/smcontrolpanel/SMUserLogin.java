package smcontrolpanel;

import java.io.IOException;
import java.io.PrintWriter;
//import java.net.*;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import SMDataDefinition.SMTablecustomlinks;
import SMDataDefinition.SMTablesecurityfunctions;
import SMDataDefinition.SMTablesecuritygroupfunctions;
import SMDataDefinition.SMTablesecurityusergroups;
import SMDataDefinition.SMTableuserscustomlinks;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsManageRequestParameters;

/** Servlet that presents the main menu.*/
public class SMUserLogin extends HttpServlet {

	private static final long serialVersionUID = 1L;
	//private final int MAX_PING_TIMEOUT_IN_MILLISECONDS = 400;
	//private final int GREEN_PING_RESPONSE_MAX = 100;
	//private final int ORANGE_PING_RESPONSE_MAX = 300;
	@Override

	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.setContentType("text/html");
		PrintWriter out = response.getWriter();

		//Try to authenticate credentials:
		if (!SMAuthenticate.authenticateSMCPCredentials(request, response, getServletContext(), -1L)){
			if(clsManageRequestParameters.get_Request_Parameter(SMAuthenticate.PARAM_UPDATE_DATABASE, request).compareToIgnoreCase("") != 0) {
				return;
			}
			out.println("<HTML>WARNING: Unable to authenticate - please log in again.</BODY></HTML>");
			return;
		//If we fail to authenticate credentials, then get a current session:
		}else{
			//System.out.println("[202021315550] " + " failed to authenticate credentials");
			HttpSession CurrentSession = request.getSession(false);
			if (CurrentSession == null){
				out.println("<HTML>WARNING: Unable to get session information - please log in again.</BODY></HTML>");
				return;
			}

			//Check for scheduled reminders
			String sScheduleCheck = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_CHECK_SCHEDULE);
			if(sScheduleCheck != null){
				try {
					response.sendRedirect(
							"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMDisplayRemiders"
						);
					return;
				} catch (IOException e) {	
					//System.out.println("Error [1495246259] redirecting to SMDisplayReminders" + e.getMessage());
				}
			}
			out.println(printHeading(
				request, 
				CurrentSession, 
				getServletContext() 
				)
			);
			try {
				processMenu(CurrentSession, out);
			} catch (Exception e) {
				out.println("<HTML>WARNING: Unable to process menu - " + e.getMessage() + " - please log in again.</BODY></HTML>");
				return;
			}
		}
		return;
	}

	private String printHeading(
			HttpServletRequest req, 
			HttpSession session,
			ServletContext context){
		String s = "";
		String sSubTitle = "<FONT SIZE=3 COLOR='RED'><I><--- Click this image for more information</I></FONT>";
		String sMobile = "";
		try {
			sMobile = (String) session.getAttribute(SMUtilities.SMCP_SESSION_PARAM_MOBILE);
		} catch (Exception e2) {
			//Can't read session info - session may be invalidated:
			//System.out.println("Error [1415824523] reading 'mobile' session attribute - " + e2.getMessage());
			sMobile = "N";
		}
		if (sMobile.compareToIgnoreCase("Y") != 0){
			sMobile = "N";
		}
		try {
			s += SMUtilities.getMenuHead(
					sSubTitle,
					SMUtilities.getInitBackGroundColor(getServletContext(), 
						(String) session.getAttribute(SMUtilities.SMCP_SESSION_PARAM_DATABASE_ID)),
					SMUtilities.DEFAULT_FONT_FAMILY,
					sMobile.compareToIgnoreCase("Y") == 0,
					getServletContext()
					)
			;
		} catch (Exception e1) {
			s += "<BR>Error [1395236505] printing menu header - " + e1.getMessage();
		}

		//Get the database server name here:
		String sDatabaseServer = "";
		try {
			sDatabaseServer = SMUtilities.getDatabaseServerURL(req, session, getServletContext());
		} catch (Exception e) {
			s += "<BR>Error [1413907259] reading database server - " + e.getMessage();
		}
		
		if (sMobile.compareToIgnoreCase("Y") != 0){
			
			if (SMUtilities.getHostName().compareToIgnoreCase("") != 0){
				String userID = (String)session.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERID);
				String sUserFullName = (String)session.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERFIRSTNAME) + " "
										+ (String)session.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERLASTNAME);
				s += "Program version " + SMUpdateData.getProgramVersion() + ", " + SMUpdateData.getCopyright() 
				+ " last revised <B>" + SMUpdateData.getLastRevisionDate() + "</B>"
				+ " database revision number <B>" + SMUtilities.getDatabaseRevisionNumber(
						context, 
						(String)session.getAttribute(SMUtilities.SMCP_SESSION_PARAM_DATABASE_ID), 
						SMUtilities.getFullClassName(this.toString()), 
						userID,
						sUserFullName
					) + "</B>"
				+ " running on '<B>" + SMUtilities.getHostName() + "</B>'"
				+ " using database server '<B>" + sDatabaseServer + "</B>'."
				;
			}
		}else{
			s += SMUpdateData.getCopyright() + "<BR>";
		}

		return s;
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

		String SQL = "";
		//First, get a list of the permitted functions:
		try{
			SQL = "SELECT DISTINCT "
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
				
				+ ")";
			//System.out.println("SQL = " + SQL);
			ResultSet rs = clsDatabaseFunctions.openResultSet(
					SQL, 
					getServletContext(), 
					sDBID, 
					"MySQL", 
					this.toString() + ".buildMenus" + " UserID: " + sUserID);

			while(rs.next()){
				arPermittedFunctions.add(rs.getLong(SMTablesecurityfunctions.iFunctionID));
				arPermittedFunctionNames.add(rs.getString(SMTablesecurityfunctions.sFunctionName));
				arPermittedFunctionLinks.add(rs.getString(SMTablesecurityfunctions.slink));
			}

			rs.close();
		}catch(SQLException e){
			System.out.println("Error [1425391219] getting list of permitted functions with SQL '" + SQL + "' - for user '" + sUserFullName 
				+ "', sUserID: '" + sUserID + "' - " + e.getMessage());
			return false;
		}

		//Set up the layout:
		if (bMobileView){
			pwOut.println(SMUtilities.layoutMobileMenu());

			//First, build a list of shortcuts to the menus themselves:
			//Service Manager Maintenance
			pwOut.println("<table class=\"main\">");
			pwOut.println("<tr><td>" 
					+ "<a HREF='#SM'>SYSTEM ADMINISTRATION MENU</a>" 
					+ "</td></tr>");
			pwOut.println("<tr><td>" 
					+ "<a HREF='#SMMASTERMENU'>SERVICE MANAGER MASTER TABLES MENU</a>" 
					+ "</td></tr>");
			pwOut.println("<tr><td>" 
					+ "<a HREF='#SMREPORTSMENU'>SERVICE MANAGER REPORTS MENU</a>" 
					+ "</td></tr>");
			pwOut.println("<tr><td>" 
					+ "<a HREF='#SMMGMREPORTSMENU'>MANAGEMENT REPORTS MENU</a>" 
					+ "</td></tr>");
			pwOut.println("<tr><td>" 
					+ "<a HREF='#SMSALESMGMMENU'>SALES MANAGEMENT MENU</a>" 
					+ "</td></tr>");
			pwOut.println("<tr><td>" 
					+ "<a HREF='#ARMENU'>ACCOUNTS RECEIVABLE MENU</a>" 
					+ "</td></tr>");
			pwOut.println("<tr><td>" 
					+ "<a HREF='#ICMENU'>INVENTORY CONTROL MENU</a>" 
					+ "</td></tr>");
			pwOut.println("<tr><td>" 
					+ "<a HREF='#APMENU'>ACCOUNTS PAYABLE MENU</a>" 
					+ "</td></tr>");
			pwOut.println("<tr><td>" 
					+ "<a HREF='#FAMENU'>FIXED ASSETS MENU</a>" 
					+ "</td></tr>");
			pwOut.println("<tr><td>" 
					+ "<a HREF='#BKMENU'>BANK FUNCTIONS MENU</a>" 
					+ "</td></tr>");
			//Close the table:
			pwOut.println("<table class=\"main\">");

			pwOut.println("<BR><BR>");
		}

		//Build menus depending on the user's security levels:
		//System Administration Table:
		arMenu.clear();
		//Manage user passwords:
		arMenu.add(SMSystemFunctions.SMManageuserpasswords);

		//Change your password:
		arMenu.add(SMSystemFunctions.SMChangeyourpassword);

		//Manage Security Groups
		arMenu.add(SMSystemFunctions.SMManageSecurityGroups);

		//List Security Levels
		arMenu.add(SMSystemFunctions.SMListSecurityLevels);

		//Edit System Options
		arMenu.add(SMSystemFunctions.SMEditSystemOptions);

		//Edit Users
		arMenu.add(SMSystemFunctions.SMEditUsers);
		
		//Edit Users Custom Links
		arMenu.add(SMSystemFunctions.SMEditUsersCustomLinks);

		//System statistics
		arMenu.add(SMSystemFunctions.SMSystemstatistics);

		//Global Connection Pool Status
		arMenu.add(SMSystemFunctions.SMGlobalConnectionPoolStatus);

		//Update security functions
		arMenu.add(SMSystemFunctions.SMUpdateSecurityFunctions);

		arMenu.add(SMSystemFunctions.SMPurgeData);
		
		arMenu.add(SMSystemFunctions.SMRecalibrateOrderAndInvoiceCounters);

		arMenu.add(SMSystemFunctions.SMListQuickLinks);

		arMenu.add(SMSystemFunctions.SMQuerySelector);
		
		arMenu.add(SMSystemFunctions.SMExecuteSQL);
		
		arMenu.add(SMSystemFunctions.SMDisplayLoggingOperations);
		
		// TJR - 1/29/2019 - removed this until we can clean it up:
		//arMenu.add(SMSystemFunctions.SMImportData);
		
		arMenu.add(SMSystemFunctions.SMViewSystemConfiguration);
		
		arMenu.add(SMSystemFunctions.SMEditServerSettingsFile);
		
		arMenu.add(SMSystemFunctions.SMTestOHDPlusConnection);
	
		
		if (bMobileView){
			SMUtilities.buildMobileMenuTable(
					"<a name=\"SM\">SYSTEM ADMINISTRATION</a>",
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
					"System Administration",
					arPermittedFunctions,
					arPermittedFunctionNames,
					arPermittedFunctionLinks,
					arMenu,
					sDBID,
					pwOut,
					getServletContext()
			);
		}

		//Service Manager Master File Maintenance:
		arMenu.clear();

		//Edit Company Profile
		arMenu.add(SMSystemFunctions.SMEditCompanyProfile);
		
		//Edit DBAs
		arMenu.add(SMSystemFunctions.SMEditDoingBusinessAsAddresses);

		//Edit Mechanics
		arMenu.add(SMSystemFunctions.SMEditMechanics);

		//Edit Salespersons
		arMenu.add(SMSystemFunctions.SMEditSalespersons);
		
		//Edit Salespersons Signatures
		arMenu.add(SMSystemFunctions.SMEditSalespersonSignatures);

		//Edit Site Locations
		arMenu.add(SMSystemFunctions.SMEditSiteLocations);

		//Edit Convenience Phrases
		arMenu.add(SMSystemFunctions.SMEditConveniencePhrases);

		//Edit Default Item Categories
		arMenu.add(SMSystemFunctions.SMEditDefaultItemCategories);

		//Edit Labor Types
		arMenu.add(SMSystemFunctions.SMEditLaborTypes);
		
		//Edit or Add Labor Backcharges:
		arMenu.add(SMSystemFunctions.SMEditLaborBackCharges);

		//Edit Locations
		arMenu.add(SMSystemFunctions.SMEditLocations);

		//Edit Order Sources
		arMenu.add(SMSystemFunctions.SMEditOrderSources);

		//Edit Taxes
		arMenu.add(SMSystemFunctions.SMEditTaxes);

		//Edit Work Performed Codes
		arMenu.add(SMSystemFunctions.SMEditWorkPerformedCodes);

		//Edit Project Types
		arMenu.add(SMSystemFunctions.SMEditProjectTypes);

		//Edit Sales Groups
		arMenu.add(SMSystemFunctions.SMEditSalesGroups);

		//Edit Product Types:
		arMenu.add(SMSystemFunctions.SMEditBidProductTypes);

		//Edit Label Printers:
		arMenu.add(SMSystemFunctions.SMEditLabelPrinters);

		//Edit orders:
		arMenu.add(SMSystemFunctions.SMManageOrders);

		//Create multiple invoices:
		arMenu.add(SMSystemFunctions.SMCreateInvoices);
		
		//Send invoices:
		arMenu.add(SMSystemFunctions.SMSendInvoices);

		//Create credit note:
		arMenu.add(SMSystemFunctions.SMCreateCreditNotes);

		//Edit Proposal Terms
		arMenu.add(SMSystemFunctions.SMEditProposalTerms);
		
		//Edit Delivery Ticket Terms
		arMenu.add(SMSystemFunctions.SMEditDeliveryTicketTerms);
		
		//Edit proposal phrase groups
		arMenu.add(SMSystemFunctions.SMEditProposalPhraseGroups);
		
		//Edit Proposal Phrases
		arMenu.add(SMSystemFunctions.SMEditProposalPhrases);
		
		//Edit Service Types
		arMenu.add(SMSystemFunctions.SMEditServiceTypes);

		//Edit work order detail sheets:
		arMenu.add(SMSystemFunctions.SMEditDetailSheets);
		
		//Edit material returns:
		arMenu.add(SMSystemFunctions.SMEditMaterialReturns);
		
		//Edit vendor returns:
		arMenu.add(SMSystemFunctions.SMEditVendorReturns);
		
		//Edit tax certificates:
		arMenu.add(SMSystemFunctions.SMEditTaxCertificates);
		
		//Edit reminders:
		arMenu.add(SMSystemFunctions.SMEditReminders);
		
		//Edit personal reminders:
		arMenu.add(SMSystemFunctions.SMEditPersonalReminders);
		
		//Edit appointment Calendar groups:
		arMenu.add(SMSystemFunctions.SMEditAppointmentGroups);
		
		//Edit price level labels:
		arMenu.add(SMSystemFunctions.SMEditPriceLevelLabels);

		
		if (bMobileView){
			SMUtilities.buildMobileMenuTable(
					"<a name=\"SMMASTERMENU\">SERVICE MANAGER MASTER TABLES MENU</a>",
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
					"Service Manager Master File Maintenance",
					arPermittedFunctions,
					arPermittedFunctionNames,
					arPermittedFunctionLinks,
					arMenu,
					sDBID,
					pwOut,
					getServletContext()
			);
		}

		//Service Manager Reports:
		arMenu.clear();

		//Tax By Category Report
		arMenu.add(SMSystemFunctions.SMTaxByCategoryReport);
		
		//Sales Invoice Tax Report
		arMenu.add(SMSystemFunctions.SMSalesTaxReport);

		//Canceled Jobs Report:
		arMenu.add(SMSystemFunctions.SMCanceledJobsReport);

		//Open Orders Report:
		arMenu.add(SMSystemFunctions.SMOpenOrdersReport);

		//Custom Parts On Hand Not On Sales Order
		arMenu.add(SMSystemFunctions.SMCustomPartsonHandNotonSalesOrders);

		//View order information:
		arMenu.add(SMSystemFunctions.SMViewOrderInformation);

		//Job Cost Daily Report
		arMenu.add(SMSystemFunctions.SMJobCostDailyReport);

		//Pre Invoice:
		arMenu.add(SMSystemFunctions.SMPreInvoice);

		//Invoice Audit List:
		arMenu.add(SMSystemFunctions.SMInvoiceAuditList);

		//Critical Dates Report:
		arMenu.add(SMSystemFunctions.SMCriticaldatesreport);

		//View truck schedules
		arMenu.add(SMSystemFunctions.SMViewTruckSchedules);
		
		//View mechanic's own truck schedule:
		arMenu.add(SMSystemFunctions.SMViewMechanicsOwnTruckSchedule);
		
		//View customers on hold:
		arMenu.add(SMSystemFunctions.SMListCustomersOnHoldWithNoOptions);
		
		//Sales Effort check
		arMenu.add(SMSystemFunctions.SMSalesEffortCheck);
		
		//Record geocodes
		arMenu.add(SMSystemFunctions.SMRecordGeocode);
		
		//Order history
		arMenu.add(SMSystemFunctions.SMOrderHistory);
		
		if (bMobileView){
			SMUtilities.buildMobileMenuTable(
					"<a name=\"SMREPORTSMENU\">SERVICE MANAGER REPORTS MENU</a>",
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
					"Service Manager Reports",
					arPermittedFunctions,
					arPermittedFunctionNames,
					arPermittedFunctionLinks,
					arMenu,
					sDBID,
					pwOut,
					getServletContext()
			);
		}

		//Management Reports:
		arMenu.clear();

		//Average MU per truck day:
		arMenu.add(SMSystemFunctions.SMAverageMUpertruckday);

		//Average MU per truck day for individual:
		arMenu.add(SMSystemFunctions.SMIndividualAverageMUpertruckday);

		//Customer Call List
		//arMenu.add(SMSystemFunctions.CustomerCallList);

		//Monthly billing:
		arMenu.add(SMSystemFunctions.SMMonthlyBilling);

		//Monthly sales report:
		arMenu.add(SMSystemFunctions.SMMonthlySales);

		//Monthly sales report for individual:
		arMenu.add(SMSystemFunctions.SMIndividualMonthlySales);

		//Order Source Listing:
		arMenu.add(SMSystemFunctions.SMOrderSourceListing);

		//Productivity Report:
		arMenu.add(SMSystemFunctions.SMProductivityReport);

		//Unbilled Contract report:
		//arMenu.add(SMSystemFunctions.SMUnbilledContract);

		//Unbilled Contract report for an individual salesperson:
		//arMenu.add(SMSystemFunctions.SMIndividualUnbilledContractReport);

		//NEW Unbilled Contract Report:
		arMenu.add(SMSystemFunctions.SMUnbilledOrdersReport);
		
		//NEW Individual Unbilled Contract Report:
		arMenu.add(SMSystemFunctions.SMUnbilledOrdersReportForIndividual);
		
		//Warranty Status Report:
		arMenu.add(SMSystemFunctions.SMWarrantystatusreport);
		
		arMenu.add(SMSystemFunctions.SMListOrdersForScheduling);
		
		arMenu.add(SMSystemFunctions.SMViewAppointmentCalendar);
		


		if (bMobileView){
			SMUtilities.buildMobileMenuTable(
					"<a name=\"SMMGMREPORTSMENU\">MANAGEMENT REPORTS MENU</a>",
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
					"Management Reports",
					arPermittedFunctions,
					arPermittedFunctionNames,
					arPermittedFunctionLinks,
					arMenu,
					sDBID,
					pwOut,
					getServletContext()
			);
		}

		//Sales Management:
		arMenu.clear();

		//Create/Edit Sales Contact
		arMenu.add(SMSystemFunctions.SMEditSalesContacts);

		//Sales Contact Report
		arMenu.add(SMSystemFunctions.SMSalesContactReport);

		//Create/Edit Sales Lead
		arMenu.add(SMSystemFunctions.SMEditBids);

		//Sales Lead Report
		arMenu.add(SMSystemFunctions.SMSalesLeadReport);

		//Sales Lead To-Do List
		arMenu.add(SMSystemFunctions.SMPendingBidsReport);

		//Sales lead Follow-Up List
		arMenu.add(SMSystemFunctions.SMBidFollowUpReport);

		if (bMobileView){
			SMUtilities.buildMobileMenuTable(
					"<a name=\"SMSALESMGMMENU\">SALES MANAGEMENT MENU</a>",
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
					"Sales Management",
					arPermittedFunctions,
					arPermittedFunctionNames,
					arPermittedFunctionLinks,
					arMenu,
					sDBID,
					pwOut,
					getServletContext()
			);
		}


		//Accounts Receivable
		arMenu.clear();

		//Accounts receivable
		arMenu.add(SMSystemFunctions.ARAccountsReceivable);

		//Print invoice
		arMenu.add(SMSystemFunctions.SMPrintInvoice);

		if (bMobileView){
			SMUtilities.buildMobileMenuTable(
					"<a name=\"ARMENU\">ACCOUNTS RECEIVABLE</a>",
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
					"Accounts Receivable",
					arPermittedFunctions,
					arPermittedFunctionNames,
					arPermittedFunctionLinks,
					arMenu,
					sDBID,
					pwOut,
					getServletContext()
			);
		}

		//Inventory Control
		arMenu.clear();
		arMenu.add(SMSystemFunctions.ICInventoryControl);

		if (bMobileView){
			SMUtilities.buildMobileMenuTable(
					"<a name=\"ICMENU\">INVENTORY CONTROL</a>",
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
					"Inventory Control",
					arPermittedFunctions,
					arPermittedFunctionNames,
					arPermittedFunctionLinks,
					arMenu,
					sDBID,
					pwOut,
					getServletContext()
			);
		}

		//Accounts Payable
		arMenu.clear();
		arMenu.add(SMSystemFunctions.APAccountsPayable);

		if (bMobileView){
			SMUtilities.buildMobileMenuTable(
					"<a name=\"APMENU\">ACCOUNTS PAYABLE</a>",
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
					"Accounts Payable",
					arPermittedFunctions,
					arPermittedFunctionNames,
					arPermittedFunctionLinks,
					arMenu,
					sDBID,
					pwOut,
					getServletContext()
			);
		}
		
		//Fixed Assets
		arMenu.clear();
		arMenu.add(SMSystemFunctions.FAFixedAssets);

		if (bMobileView){
			SMUtilities.buildMobileMenuTable(
					"<a name=\"FAMENU\">FIXED ASSETS</a>",
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
					"Fixed Assets",
					arPermittedFunctions,
					arPermittedFunctionNames,
					arPermittedFunctionLinks,
					arMenu,
					sDBID,
					pwOut,
					getServletContext()
			);
		}

		//Bank functions
		arMenu.clear();
		arMenu.add(SMSystemFunctions.BKBankFunctions);

		if (bMobileView){
			SMUtilities.buildMobileMenuTable(
					"<a name=\"BKMENU\">BANK FUNCTIONS</a>",
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
					"Bank Functions",
					arPermittedFunctions,
					arPermittedFunctionNames,
					arPermittedFunctionLinks,
					arMenu,
					sDBID,
					pwOut,
					getServletContext()
			);
		}
		
		//General Ledger
		arMenu.clear();
		//Edit GL Accounts
		arMenu.add(SMSystemFunctions.GLGeneralLedger);

		if (bMobileView){
			SMUtilities.buildMobileMenuTable(
					"<a name=\"PRMENU\">GENERALLEDGER</a>",
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
					"General Ledger",
					arPermittedFunctions,
					arPermittedFunctionNames,
					arPermittedFunctionLinks,
					arMenu,
					sDBID,
					pwOut,
					getServletContext()
			);
		}

		//Alarm systems
		arMenu.clear();
		
		//Main menu
		arMenu.add(SMSystemFunctions.ASAlarmFunctions);
		
		if (bMobileView){
			SMUtilities.buildMobileMenuTable(
					"<a name=\"ASMENU\">ALARMSYSTEM</a>",
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
					"Alarm system",
					arPermittedFunctions,
					arPermittedFunctionNames,
					arPermittedFunctionLinks,
					arMenu,
					sDBID,
					pwOut,
					getServletContext()
			);
		}
		
		//Payroll
		arMenu.clear();
		arMenu.add(SMSystemFunctions.SMLoadWageScaleData);
		arMenu.add(SMSystemFunctions.SMWageScaleReport);

		if (bMobileView){
			SMUtilities.buildMobileMenuTable(
					"<a name=\"PRMENU\">PAYROLL</a>",
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
					"Payroll",
					arPermittedFunctions,
					arPermittedFunctionNames,
					arPermittedFunctionLinks,
					arMenu,
					sDBID,
					pwOut,
					getServletContext()
			);
		}
		
		//Display users custom links if they have any
		try{
			SQL = "SELECT  "
				+ 		SMTablecustomlinks.surlname
				+ ", " + SMTablecustomlinks.surl
				+ " FROM "
				+ SMTableuserscustomlinks.TableName 
				+ " LEFT JOIN "+ SMTablecustomlinks.TableName + " ON " 
				+ SMTableuserscustomlinks.TableName + "." + SMTableuserscustomlinks.icustomlinkid
				+ " = " + SMTablecustomlinks.TableName + "." + SMTablecustomlinks.lid
				+ " WHERE (" 
				+ 		SMTableuserscustomlinks.luserid + " = " + sUserID + ""
				+ ")" 
				;

			ResultSet rsCustomLinks = clsDatabaseFunctions.openResultSet(
					SQL, 
					getServletContext(), 
					sDBID, 
					"MySQL", 
					this.toString() + ".buildMenus" + " User: " + sUserName);
			
			ArrayList<String> arCustomLinks = new ArrayList<String>(0);
			String sLink = "";
			while(rsCustomLinks.next()){
				sLink ="<A HREF='" + rsCustomLinks.getString( SMTablecustomlinks.surl)+ "'>"
						+ "<U><FONT SIZE=3 STYLE='font-size: 11pt'><FONT COLOR='#000080'>"
						+ rsCustomLinks.getString( SMTablecustomlinks.surlname)
						+ "</FONT></FONT></U></A><BR>"
						;
				
				arCustomLinks.add(sLink);
			}
			rsCustomLinks.close();
			
			if(arCustomLinks.size() > 0){
				pwOut.println("<B>" + "Custom Links" + "</B>");
				pwOut.println(SMUtilities.Build_HTML_Table(4, arCustomLinks,1,true));
				pwOut.println("<BR>");
			}
			
		}catch(SQLException e){
			System.out.println("Error [144391219] getting list of custom links - " + e.getMessage());
			return false;
		}


		return true;
	}
	private void processMenu(HttpSession session, PrintWriter pwOut) throws Exception{
		try{
			//Read from the recordset into parameters:
			String sMobile = (String) session.getAttribute(SMUtilities.SMCP_SESSION_PARAM_MOBILE);
			String sOutPut = "";
			if (sMobile.compareToIgnoreCase("Y") == 0){
				sOutPut += "User: <B>" + session.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERFIRSTNAME) + " " 
									   + session.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERLASTNAME) + "</B><BR>"
								 	   + "Company: <B>" + session.getAttribute(SMUtilities.SMCP_SESSION_PARAM_COMPANYNAME) + "</B>";
			}else{
				sOutPut = "<BR>Hello, " + session.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERFIRSTNAME) + " " 
										+ session.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERLASTNAME) + ": "
				+ "you are currently logged into the " + SMUtilities.getInitProgramTitle(getServletContext())
				+ " for company: <B>"
				+ session.getAttribute(SMUtilities.SMCP_SESSION_PARAM_COMPANYNAME) + "</B>";
			}
			pwOut.println(sOutPut + "<BR>");

			//Link to switch to mobile view or full web page:
			String sDBID = (String) session.getAttribute(SMUtilities.SMCP_SESSION_PARAM_DATABASE_ID);
			
			//Print a link to the user session information:
			pwOut.println("<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMViewUserSessionInformation?" 
					+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
					+ "\">View session information</A>&nbsp;&nbsp;");
			
			boolean bMobileView = false;
			if (sMobile.compareToIgnoreCase("Y") == 0){
				pwOut.println("<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) 
						+ "smcontrolpanel.SMUserLogin?" 
						+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
						+ "&mobile=N\" style=\"color: darkblue; font-weight:100; \" >Click for Standard Web View</A><BR><BR>");
				bMobileView = true;
			}else{
				pwOut.println("<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) 
						+ "smcontrolpanel.SMUserLogin?" 
						+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
						+ "&mobile=Y\">Currently in Standard Web View - click for Mobile Version</A>");
				pwOut.println("&nbsp;&nbsp;<A HREF=\"" 
						+ "https://sites.google.com/site/airotechservicemanager/calendar" 
						+ "\">Review SMCP Program Updates</A><BR>");
			}
			//Calculate reminders and display link if there are any reminders
			String sUserID = (String) session.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERID);
			try{
			SMDisplayReminders scheduleEntry = new SMDisplayReminders();
			scheduleEntry.calculateReminders(getServletContext(), sDBID, sUserID);

			if(scheduleEntry.getRowCounter() > 0){
				String s = "s";
				if(scheduleEntry.getRowCounter() == 1){
					s = "";
				}
				pwOut.println("<div style=\"border-bottom-left-radius: 10px;border-bottom-right-radius: 10px;"
						+ "padding: 3px;background-color:white;border:1px solid black;position:fixed;top:0;right:5%;\">"
						+ "&nbsp;&nbsp;&nbsp;&nbsp;<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) 
						+ "smcontrolpanel.SMDisplayReminders?" 
						+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
						+ "\">" + "You have " + Integer.toString(scheduleEntry.getRowCounter()) + " reminder" + s + "</A>&nbsp;&nbsp;&nbsp;&nbsp;</div>");
			}
			}catch (Exception e){
				System.out.println("[1456324674] - Error checking reminders-" + e.getMessage() );
			}
			//Build menus here:
			if (!buildMenus(
					bMobileView,
					(String) session.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERNAME),
					(String) session.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERID),
					(String) session.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERFIRSTNAME) + " " + (String)session.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERLASTNAME),
					(String)session.getAttribute(SMUtilities.SMCP_SESSION_PARAM_DATABASE_ID),
					pwOut,
					(String)session.getAttribute(SMUtilities.SMCP_SESSION_PARAM_LICENSE_MODULE_LEVEL))
			){
				pwOut.println("Error building menus.");
			}
			pwOut.println("</BODY></HTML>");

		} catch (Exception ex){
			pwOut.println("Error [1393625864] in " + SMUtilities.getFullClassName(this.toString()) + " - " + ex.getMessage());
			pwOut.println("</BODY></HTML>");
		}
	}
	/*
	private String Get_Ping_Value(String sIP,
								  int iPort,
								  int iTrialNumber) throws IOException{

		String sColor = "RED";
		int iTried = 0;
		long lTotalTime = 0;
		int iSuccessful = 0;
		Socket socket = null;
		SocketAddress sockaddr = new InetSocketAddress(sIP, iPort);
		String s = "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<FONT SIZE=1>Connection status: </FONT><FONT SIZE=1 COLOR='";
		while (iTried < iTrialNumber){
			iTried++;
			long lStartTime = System.currentTimeMillis();
			try {
				socket = new Socket();
				socket.connect(sockaddr, MAX_PING_TIMEOUT_IN_MILLISECONDS);
			} catch(SocketTimeoutException e) {
				//System.out.println("Socket trial[" + iTried + "]: Connection timed out.");
				continue;
			}catch(UnknownHostException e){
				socket.close();
				//System.out.println("[1393612283] Unknown host.");
				return s + sColor + "'><B>Unknown host exception</B> pinging " + sIP + "</FONT>";
			} catch(IOException e) {
				socket.close();
				//System.out.println("[1393612284] Error when pinging " + sIP + ":" + iPort);
				return s + sColor + "'><B>IO Exception</B> pinging " + sIP + "</FONT>";
			}catch(Exception e){
				socket.close();
				//System.out.println("[1418853709] Unknown host.");
				return s + sColor + "'><B>Generic socket exception [1418853709]</B> pinging " + sIP + "</FONT>";
			}
			iSuccessful++;
			long lEndTime = System.currentTimeMillis();
			//System.out.println("Pinging " + sIP + ":" + iPort + " trial [" + iTried + "]: " + (lEndTime - lStartTime));
			lTotalTime += (lEndTime - lStartTime);
		}
		socket.close();
		long lAvgPing = 0;
		if (iSuccessful > 0){
			lAvgPing = (lTotalTime) / iSuccessful;
			if (lAvgPing < GREEN_PING_RESPONSE_MAX){
				sColor = "GREEN";
			}else if (lAvgPing < ORANGE_PING_RESPONSE_MAX){
				sColor = "ORANGE";
			}else{
				sColor = "RED";
			}
			return s + sColor + "'>" + "<B>" + lAvgPing + " ms</B> avg. from " + sIP + " in " + iTrialNumber + " attempts.</FONT>";
		}else{
			//If all attempts failed:
			return s + "RED" + "'><B>" + "no ping response within " 
				+ Integer.toString(this.MAX_PING_TIMEOUT_IN_MILLISECONDS) 
				+ " milliseconds from " + sIP + " after " + Integer.toBinaryString(iTrialNumber) + " attempts.</FONT>";
		}
	}
	*/
	@Override
	public void doGet(HttpServletRequest request,
			HttpServletResponse response)
	throws ServletException, IOException {

		doPost(request, response);
	}
}
