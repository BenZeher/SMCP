package smcontrolpanel;

import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import smar.ARPrintCallSheetsSelection;
import smar.SMOption;
import SMClasses.SMAppointment;
import SMClasses.SMDeliveryTicket;
import SMClasses.SMLaborBackCharge;
import SMClasses.SMLogEntry;
import SMClasses.SMMaterialReturn;
import SMClasses.SMOrderDetail;
import SMClasses.SMOrderHeader;
import SMClasses.SMWorkOrderHeader;
import SMDataDefinition.SMCreateGoogleDriveFolderParamDefinitions;
import SMDataDefinition.SMTableappointments;
import SMDataDefinition.SMTablearcustomer;
import SMDataDefinition.SMTablebids;
import SMDataDefinition.SMTablechangeorders;
import SMDataDefinition.SMTablecriticaldates;
import SMDataDefinition.SMTabledeliverytickets;
import SMDataDefinition.SMTabledoingbusinessasaddresses;
import SMDataDefinition.SMTableicitems;
import SMDataDefinition.SMTableinvoicedetails;
import SMDataDefinition.SMTableinvoiceheaders;
import SMDataDefinition.SMTablelaborbackcharges;
import SMDataDefinition.SMTablematerialreturns;
import SMDataDefinition.SMTableorderdetails;
import SMDataDefinition.SMTableorderheaders;
import SMDataDefinition.SMTableproposals;
import SMDataDefinition.SMTablesalesgroups;
import SMDataDefinition.SMTablesalesperson;
import SMDataDefinition.SMTabletax;
import SMDataDefinition.SMTableworkorders;
import ServletUtilities.clsServletUtilities;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsDateAndTimeConversions;
import ServletUtilities.clsManageBigDecimals;
import ServletUtilities.clsManageRequestParameters;
import ServletUtilities.clsStringFunctions;

public class SMDisplayOrderInformation extends HttpServlet {
	private static final String PROPOSAL_TABLE_BG_COLOR = "#CCFFB2";
	private static final String ORDER_HEADER_TABLE_BG_COLOR = "#FFFFFF"; //"#FFBCA2"; 
	private static final String ORDER_DETAILS_BG_COLOR = "#FFB8B8";
	private static final String LEFT_ON_ORDER_BG_COLOR = "#58FA58";
	private static final String WORK_ORDERS_TABLE_BG_COLOR = "#FFFF66";
	private static final String CHANGEORDERS_TABLE_BG_COLOR = "#A3D1FF";
	private static final String SUBHEADER_TABLE_BG_COLOR = "#FFFFFF"; //"#F2C3FA";
	private static final String MATERIAL_RETURNS_TABLE_BG_COLOR = "#CCFFB2";
	private static final String BILLING_SUMMARY_TABLE_BG_COLOR = "#F2C3FA";
	private static final String CRITICAL_DATE_TABLE_BG_COLOR = "#FFBCA2";
	private static final String TAX_CALCULATION_TABLE_BG_COLOR = "#A3D1FF";
	private static final String CELL_BORDER_COLOR = "#808080";
	
	private static final long serialVersionUID = 1L;
	//private static final String LIGHT_BG_COLOR = "EEEEEE";
	private static final String LIGHT_GREY_BG_COLOR = "#f3f3f3";
	private static final String DARK_ROW_BG_COLOR = "#DCDCDC";
	private static final String LIGHT_ROW_BG_COLOR = "#FFFFFF";
	private static final String DELIVERY_TICKET_TABLE_BG_COLOR = "#EE9A4D";
	private static final String LABOR_BACKCHARGES_TABLE_BG_COLOR = "#FFFFBB";
	private static final String FOLLOWUP_SALES_LEAD_TABLE_BG_COLOR = "#FFBCA2";
	private static final String APPOINTMENTS_TABLE_BG_COLOR = "#e8c355";
	private static SimpleDateFormat USDateOnlyformatter = new SimpleDateFormat("MM-dd-yyyy");
	
	//TODO Flag to test google drive picker to create upload files.
	private static final boolean bTestGoogleDrivePicker = false;
	
	public void doGet(HttpServletRequest request,
			HttpServletResponse response)
	throws ServletException, IOException {

		if (!SMAuthenticate.authenticateSMCPCredentials(
				request, 
				response, 
				getServletContext(),
				SMSystemFunctions.SMViewOrderInformation
		)
		){
			return;
		}

		response.setContentType("text/html");
		PrintWriter out = response.getWriter();

		//Get the session info:
		HttpSession CurrentSession = request.getSession(true);
		String sDBID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_DATABASE_ID);
		String sUserID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERID);
		String sCompanyName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_COMPANYNAME);
		if (sDBID.compareToIgnoreCase("") == 0){
			SMUtilities.sysprint(this.toString(), 
			sUserID, 
			"[1551711576] request params = '" + ServletUtilities.clsManageRequestParameters.getAllRequestParameters(request)
				+ "', session attributes = '" + SMUtilities.getSessionAttributes(CurrentSession)
			);
		}

		String sOrderNumber = clsManageRequestParameters.get_Request_Parameter("OrderNumber", request).replace(" ", "");

		//Customized title
		String sReportTitle = "Display Order/Quote Information";
		out.println(SMUtilities.SMCPTitleSubBGColor(sReportTitle, "", "#FFFFFF", sCompanyName));

		out.println("<TD><A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMUserLogin?" 
				+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
				+ "\">Return to user login</A><BR>");

		out.println("<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMURLHistoryList?" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" 
				+ sDBID + "\">Return to...</A><BR>");
		
		if (
			(sDBID == null) || (sDBID.compareToIgnoreCase("") == 0)
		){
			out.println("<BR><FONT COLOR=RED>Error [1423004585] - Database id '" + sDBID + "' is invalid, userID: '" + sUserID + "'.<BR></FONT>");
		}else{
			//log usage of this this report
			SMClasses.SMLogEntry log = new SMClasses.SMLogEntry(sDBID, getServletContext());
			log.writeEntry(sUserID, SMLogEntry.LOG_OPERATION_SMDISPLAYORDERINFORMATION, "REPORT", "SMDisplayOrderInformation", "[1376509320]");
			
			if(bTestGoogleDrivePicker) {
				try {
					out.println(clsServletUtilities.getDrivePickerJSIncludeString(
							getServletContext(),
							"",
							"",
							"",
							SMCreateGDriveFolder.ORDER_RECORD_TYPE_PARAM_VALUE,
							sOrderNumber,
							sOrderNumber)
							);
				} catch (Exception e) {
					System.out.println("[1554818420] - Failed to load drivepicker.js - " + e.getMessage());
				}
			}
			out.println(sStyleScripts());
			
			if (!displayOrder(sDBID, sUserID, sOrderNumber, out, request, CurrentSession)){
			}
		}
		out.println("</BODY></HTML>");
	}
	private boolean displayOrder(
			String sDBID,
			String sUserID,
			String sOrderNum, 
			PrintWriter pwOut, 
			HttpServletRequest req,
			HttpSession session){

		String sDefaultSP = "";
		String sLinks = "";

		Connection conn;
		try {
			conn = clsDatabaseFunctions.getConnectionWithException(
				getServletContext(), 
				sDBID, 
				"MySQL", 
				SMUtilities.getFullClassName(this.toString()) + ".displayOrder - userID: " + sUserID
			);
		} catch (Exception e) {
			pwOut.println ("Error [1411071471] getting connection - " + e.getMessage());
			return false;
		}
		String sLicenseModuleLevel = (String) session.getAttribute(SMUtilities.SMCP_SESSION_PARAM_LICENSE_MODULE_LEVEL);
		boolean bAllowOrderHeaderView = SMSystemFunctions.isFunctionPermitted(SMSystemFunctions.SMViewOrderHeaderInformation, sUserID, conn, sLicenseModuleLevel);
		boolean bAllowProjectView = SMSystemFunctions.isFunctionPermitted(SMSystemFunctions.SMViewProjectInformation, sUserID, conn, sLicenseModuleLevel);
		boolean bAllowJobCostView = SMSystemFunctions.isFunctionPermitted(SMSystemFunctions.SMJobCostSummaryReport, sUserID, conn, sLicenseModuleLevel);
		boolean bAllowDocumentView = SMSystemFunctions.isFunctionPermitted(SMSystemFunctions.SMViewOrderDocuments, sUserID, conn, sLicenseModuleLevel); 
		boolean bAllowInvoiceView = SMSystemFunctions.isFunctionPermitted(SMSystemFunctions.SMPrintInvoice, sUserID, conn, sLicenseModuleLevel); 
		boolean bAllowCustomerView = SMSystemFunctions.isFunctionPermitted(SMSystemFunctions.ARDisplayCustomerInformation, sUserID, conn, sLicenseModuleLevel); 
		boolean bAllowInstallationTicketPrinting = SMSystemFunctions.isFunctionPermitted(SMSystemFunctions.SMPrintInstallationTicket, sUserID, conn, sLicenseModuleLevel); 
		boolean bAllowServiceTicketPrinting = SMSystemFunctions.isFunctionPermitted(SMSystemFunctions.SMPrintServiceTicket, sUserID, conn, sLicenseModuleLevel); 
		boolean bAllowOrderEditing = SMSystemFunctions.isFunctionPermitted(SMSystemFunctions.SMManageOrders, sUserID, conn, sLicenseModuleLevel); 
		boolean bAllowChangeOrderEditing = SMSystemFunctions.isFunctionPermitted(SMSystemFunctions.SMEditChangeOrders, sUserID, conn, sLicenseModuleLevel); 
		boolean bAllowItemViewing = SMSystemFunctions.isFunctionPermitted(SMSystemFunctions.ICDisplayItemInformation, sUserID, conn, sLicenseModuleLevel); 
		boolean bAllowCallSheetViewing = SMSystemFunctions.isFunctionPermitted(SMSystemFunctions.ARPrintCallSheets, sUserID, conn, sLicenseModuleLevel); 
		boolean bAllowOrderDetailViewing = SMSystemFunctions.isFunctionPermitted(SMSystemFunctions.SMViewOrderDetailInformation, sUserID, conn, sLicenseModuleLevel); 
		boolean bAllowScheduling = SMSystemFunctions.isFunctionPermitted(SMSystemFunctions.SMConfigureWorkOrders, sUserID, conn, sLicenseModuleLevel); 
		boolean bAllowBidEditing = SMSystemFunctions.isFunctionPermitted(SMSystemFunctions.SMEditBids, sUserID, conn, sLicenseModuleLevel); 
		boolean bAllowProposalViewing = SMSystemFunctions.isFunctionPermitted(SMSystemFunctions.SMEditProposals, sUserID, conn, sLicenseModuleLevel); 
		boolean bAllowEditMaterialReturns = SMSystemFunctions.isFunctionPermitted(SMSystemFunctions.SMEditMaterialReturns, sUserID, conn, sLicenseModuleLevel); 
		boolean bAllowEditWorkOrders = SMSystemFunctions.isFunctionPermitted(SMSystemFunctions.SMEditWorkOrders, sUserID, conn, sLicenseModuleLevel);
		boolean bAllowCreateGDriveOrderFolders = SMSystemFunctions.isFunctionPermitted(SMSystemFunctions.SMCreateGDriveOrderFolders, sUserID, conn, sLicenseModuleLevel);
		boolean bAllowManageDeliveryTickets = SMSystemFunctions.isFunctionPermitted(SMSystemFunctions.SMManageDeliveryTickets, sUserID, conn, sLicenseModuleLevel);
		boolean bAllowEditLaborBackCharge = SMSystemFunctions.isFunctionPermitted(SMSystemFunctions.SMEditLaborBackCharges, sUserID, conn, sLicenseModuleLevel);
		boolean bAllowViewAppointments = SMSystemFunctions.isFunctionPermitted(SMSystemFunctions.SMViewAppointmentCalendar, sUserID, conn, sLicenseModuleLevel);
		boolean bAllowEditAppointments = SMSystemFunctions.isFunctionPermitted(SMSystemFunctions.SMEditAppointmentCalendar, sUserID, conn, sLicenseModuleLevel);
		
		BigDecimal bdTotalBilled = new BigDecimal(0);
		String sDepositAmount = "0.00";
		String sWarrantyExpirationDate = "00/00/0000";
		String sWageScaleNotes = "";
		String sGeoCode = "";
		String sOrderDate = "";
		int iOrderType = 0;

		String SQL = "SELECT"
			+ " " + SMTableorderheaders.TableName + ".*"
			+ ", " + SMTablesalesgroups.TableName + ".*"
			+ ", " + SMTablesalesperson.TableName + ".*"
			+ ", " + SMTablearcustomer.TableName + "." + SMTablearcustomer.iOnHold
			+ ", " + SMTabletax.TableName + "." + SMTabletax.sdescription
			
			+ " FROM "
			+ SMTableorderheaders.TableName + " LEFT JOIN " 
			+ SMTablesalesgroups.TableName + " ON " 
			+ SMTableorderheaders.TableName + "." + SMTableorderheaders.iSalesGroup + " = "
			+ SMTablesalesgroups.TableName + "." + SMTablesalesgroups.iSalesGroupId
			//join to salespersons table:
			+ " LEFT JOIN " 
			+ SMTablesalesperson.TableName + " ON " 
			+ SMTableorderheaders.TableName + "." + SMTableorderheaders.sSalesperson + " = "
			+ SMTablesalesperson.TableName + "." + SMTablesalesperson.sSalespersonCode
			
			+ " LEFT JOIN " 
			+ SMTablearcustomer.TableName + " ON " 
			+ SMTableorderheaders.TableName + "." + SMTableorderheaders.sCustomerCode + " = "
			+ SMTablearcustomer.TableName + "." + SMTablearcustomer.sCustomerNumber
			
			+ " LEFT JOIN " 
			+ SMTabletax.TableName + " ON " 
			+ SMTableorderheaders.TableName + "." + SMTableorderheaders.itaxid + " = "
			+ SMTabletax.TableName + "." + SMTabletax.lid			
			
			+ " WHERE "
			+ SMTableorderheaders.strimmedordernumber + " = '" + sOrderNum + "'";
		try{
			ResultSet rsOrder = clsDatabaseFunctions.openResultSet(SQL, conn);

			if(rsOrder.next()){
				//Get the project information here:
				sDepositAmount = clsManageBigDecimals.BigDecimalToScaledFormattedString(
						SMTableorderheaders.bddepositamountScale, rsOrder.getBigDecimal(
								SMTableorderheaders.bddepositamount));
				sWarrantyExpirationDate = clsDateAndTimeConversions.resultsetDateStringToString(
						rsOrder.getString(SMTableorderheaders.datwarrantyexpiration));
				sWageScaleNotes = rsOrder.getString(SMTableorderheaders.swagescalenotes);
				sGeoCode = rsOrder.getString(SMTableorderheaders.sgeocode);
				if(sGeoCode.trim().compareToIgnoreCase("") == 0) {
					sGeoCode = SMGeocoder.EMPTY_GEOCODE;
				}
				iOrderType = rsOrder.getInt(SMTableorderheaders.iOrderType);
				
				//Manage/Edit order links
				String sManageOrdersLink = "";
				String sEditeOrderLink = "";
				String sEditOrderDetailsLink = "";			
				if (bAllowOrderEditing){
					sManageOrdersLink = "<A HREF=\"" 
						+ SMUtilities.getURLLinkBase(getServletContext()) 
						+ "smcontrolpanel.SMEditOrderSelection?" + SMOrderHeader.ParamsOrderNumber + "=" 
						+ sOrderNum 
						+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
						+ "&CallingClass=" + this.getClass().getName()
						+ "\">Manage&nbsp;orders</A>";
					pwOut.println(sManageOrdersLink + "<BR>");
					
					sEditeOrderLink = "<A HREF=\"" 
						+ SMUtilities.getURLLinkBase(getServletContext()) 
						+ "smcontrolpanel.SMEditOrderEdit?" + SMOrderHeader.Paramstrimmedordernumber + "=" 
						+ sOrderNum 
						+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
						+ "&CallingClass=" + this.getClass().getName()
						+ "\">Edit&nbsp;order</A>";
					pwOut.println(sEditeOrderLink + "<BR>");
					
					if(bAllowOrderDetailViewing){
						sEditOrderDetailsLink = "<A HREF=\"" 
							+ SMUtilities.getURLLinkBase(getServletContext()) 
							+ "smcontrolpanel.SMOrderDetailList?" + SMOrderHeader.Paramstrimmedordernumber + "=" 
							+ sOrderNum 
							+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
							+ "&CallingClass=" + this.getClass().getName()
							+ "\">Edit&nbsp;order&nbsp;details</A>";
						pwOut.println(sEditOrderDetailsLink + "<BR>");
					}
				}

				//Build the long string of links for all the functions here:
				//Link to order header:
				sLinks = "<FONT SIZE=2><a href=\"#OrderHeader\">Order&nbsp;header</a>&nbsp;&nbsp;";
				
				//Link to order details:
				sLinks += "<FONT SIZE=2><a href=\"#OrderDetails\">Order&nbsp;details</a>&nbsp;&nbsp;</FONT>";

				//Link to change orders:
				if(bAllowProjectView  && (iOrderType != SMTableorderheaders.ORDERTYPE_QUOTE)){
					sLinks = sLinks + "<FONT SIZE=2><a href=\"#ChangeOrders\">Change&nbsp;orders</a>&nbsp;&nbsp;</FONT>";
				}
				
				//Link to billing summary:
				if (bAllowProjectView && (iOrderType != SMTableorderheaders.ORDERTYPE_QUOTE)){
					sLinks = sLinks + "<FONT SIZE=2><a href=\"#BILLINGSUMMARY\">Billing&nbsp;summary</a>&nbsp;&nbsp;</FONT>"; 
				}
				
				//Link to critical dates:
				if(bAllowProjectView  && (iOrderType != SMTableorderheaders.ORDERTYPE_QUOTE)){
					sLinks = sLinks + "<FONT SIZE=2><a href=\"#CriticalDates\">Critical&nbsp;dates</a>&nbsp;&nbsp;</FONT>"; 
				}
				
				//Link to work orders:
				if (iOrderType != SMTableorderheaders.ORDERTYPE_QUOTE){
					sLinks += "<FONT SIZE=2><a href=\"#WorkOrders\">Work&nbsp;orders</a>&nbsp;&nbsp;</FONT>";
				}

				//Link to appointments
				if (bAllowViewAppointments && iOrderType != SMTableorderheaders.ORDERTYPE_QUOTE){
					sLinks += "<FONT SIZE=2><a href=\"#Appointments\">Appointments</a>&nbsp;&nbsp;</FONT>";
				}

				//Link to delivery tickets:
				if(bAllowManageDeliveryTickets && (iOrderType != SMTableorderheaders.ORDERTYPE_QUOTE)){
					sLinks += "<FONT SIZE=2><a href=\"#DeliveryTickets\">Delivery&nbsp;Tickets</a>&nbsp;&nbsp;</FONT>";
				}
				//Link to material returns:
				sLinks += "<FONT SIZE=2><a href=\"#MaterialReturns\">Material Returns</a>&nbsp;&nbsp;</FONT>";
				
				//Link to labor backcharges:
				if(bAllowEditLaborBackCharge && (iOrderType != SMTableorderheaders.ORDERTYPE_QUOTE)){
					sLinks += "<FONT SIZE=2><a href=\"#LaborBackcharges\">Labor&nbsp;Backcharges</a>&nbsp;&nbsp;</FONT>";
				}
				//Link to proposal:
				if (bAllowProposalViewing){
					sLinks += "<FONT SIZE=2><a href=\"#Proposal\">Proposal</a>&nbsp;&nbsp;</FONT>";
				}
				
				//Link to Items Left On Order
				sLinks += "<FONT SIZE=2><a href=\"#ItemsLeftOnOrder\">Item(s) Left On Order</a>&nbsp;&nbsp;</FONT>";
				
				//Link to tax calculation
				if (iOrderType != SMTableorderheaders.ORDERTYPE_QUOTE){
					sLinks += "<FONT SIZE=2><a href=\"#TaxCalculation\">Sales&nbsp;Tax&nbsp;calculation</a>&nbsp;&nbsp;</FONT>";
				}
				//Link Follow Up Sales Leads:
				if (bAllowBidEditing){
					sLinks += "<FONT SIZE=2><a href=\"#FollowUpSalesLead\">Follow up sales leads</a>&nbsp;&nbsp;</FONT>";
				}
				//Link to job cost data:
				if(bAllowJobCostView && (iOrderType != SMTableorderheaders.ORDERTYPE_QUOTE)){
					//Check to see if there is any job cost data available:
					String jcSQL = "SELECT"
						+ " " + SMTableworkorders.lid
						+ " FROM " + SMTableworkorders.TableName
						+ " WHERE ("
						+ "(" + SMTableworkorders.strimmedordernumber + " = '" + sOrderNum + "')"
						+ ")"
						;
					boolean bJobCostExists = false;
					try {
						ResultSet rsJobCost = clsDatabaseFunctions.openResultSet(jcSQL, conn);
						if (rsJobCost.next()){
							bJobCostExists = true;
						}
						rsJobCost.close();
						if (bJobCostExists){
							sLinks = sLinks 
							+ "<FONT SIZE=2><A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMDisplayJobCostInformation?OrderNumber=" 
							+ sOrderNum + "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID + "\">Job&nbsp;cost</A>&nbsp;&nbsp;</FONT>"; 
						}else{
							sLinks = sLinks + "<FONT SIZE=2>Job&nbsp;cost: N/A&nbsp;&nbsp;</FONT>"; 
						}
					} catch (SQLException e) {
						//sWarning = "Could not check for Job Cost data - " + e.getMessage();
					}
				}
				
				//Link to schedule this order:
				if (bAllowScheduling && (iOrderType != SMTableorderheaders.ORDERTYPE_QUOTE)){
					sLinks = sLinks
						+ "<FONT SIZE=2><A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) 
						+ "smcontrolpanel.SMConfigWorkOrderEdit?" + SMWorkOrderHeader.Paramlid + "=-1"
						+ "&" + SMWorkOrderHeader.Paramscheduleddate + "=" + clsDateAndTimeConversions.now("M/d/yyyy")
						+ "&" + SMWorkOrderHeader.Paramstrimmedordernumber + "=" + sOrderNum
						+ "&" + SMConfigWorkOrderEdit.REMOVE_WORK_ORDER_ATTRIBUTE_FROM_SESSION + "=Y"
						+ "&CallingClass=smcontrolpanel.SMDisplayOrderInformation"
						+ "&ReturnToTruckSchedule=N"
						+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
						+ "\">Schedule&nbsp;this&nbsp;order</A>&nbsp;&nbsp;</FONT>";
					;
				}

				//Link to list nearby orders:
				if (iOrderType != SMTableorderheaders.ORDERTYPE_QUOTE){
					sLinks = sLinks
						+ "<FONT SIZE=2><A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) 
						+ "smcontrolpanel.SMListNearbyOrdersGenerate?ORDERNUMBER=" + sOrderNum
						+ "&GEOCODE=" + sGeoCode 
						+ "&CallingClass=smcontrolpanel.SMDisplayOrderInformation"
						+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
						+ "\">List&nbsp;nearby&nbsp;orders</A>&nbsp;&nbsp;</FONT>";
					;
				}
				
				//Link to view call sheets:
				if (bAllowCallSheetViewing && (iOrderType != SMTableorderheaders.ORDERTYPE_QUOTE)){
					sLinks = sLinks
					+ "<FONT SIZE=2><A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) 
					+ "smar.ARPrintCallSheetsGenerate?" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
					+ "&CallingClass=smcontrolpanel.SMDisplayOrderInformation"
					+ "&" + ARPrintCallSheetsSelection.ORDERNUMBER_FIELD + "=" + sOrderNum
					+ "&" + ARPrintCallSheetsSelection.ENDING_CUSTOMER_FIELD + "=ZZZZZZZZZZZZ"
					+ "&" + ARPrintCallSheetsSelection.ENDING_LAST_CONTACT_DATE_FIELD + "=12/31/2100"
					+ "&" + ARPrintCallSheetsSelection.ENDING_NEXT_CONTACT_DATE_FIELD + "=12/31/2100"
					+ "&" + ARPrintCallSheetsSelection.PRINTWITHNOTES_FIELD + "=Y"
					+ "&" + ARPrintCallSheetsSelection.PRINTZEROBALANCECUSTOMERS_FIELD + "=Y"
					+ "&" + ARPrintCallSheetsSelection.COLLECTOR_FIELD + "="
					+ "&" + ARPrintCallSheetsSelection.RESPONSIBILITY_FIELD + "="
					+ "&" + ARPrintCallSheetsSelection.STARTING_CUSTOMER_FIELD + "=0"
					+ "&" + ARPrintCallSheetsSelection.STARTING_LAST_CONTACT_DATE_FIELD + "=1/1/1990"
					+ "&" + ARPrintCallSheetsSelection.STARTING_NEXT_CONTACT_DATE_FIELD + "=1/1/1990"
					+ "&" + ARPrintCallSheetsSelection.PRINT_BUTTON_NAME + "Y"
					+ "\">List&nbsp;call&nbsp;sheets</A>&nbsp;&nbsp;</FONT>";
				}
				
				
				//Link to view Google Drive folder:
				SMOption smopt = new SMOption();
				try {
					smopt.load(conn);
				} catch (Exception e1) {
					pwOut.println("<BR>Error getting SMOptions [1385390626] - " + e1.getMessage() + "<BR>");
				}
					
				if (bAllowDocumentView){
					if (smopt.getOrderDocsFTPUrl().compareToIgnoreCase("") != 0){
						sLinks = sLinks +  "<FONT SIZE=2><A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMListOrderDocuments?OrderNumber=" 
						+ sOrderNum.trim() + "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID + "\">Order&nbsp;documents</A>&nbsp;&nbsp;</FONT>";
					}
					//Try to build a link to the Google Docs folder:
					String sGDocLink = rsOrder.getString(SMTableorderheaders.TableName + "." + SMTableorderheaders.sgdoclink);
					if (sGDocLink == null){
						sGDocLink = "";
					}
					if (sGDocLink.compareToIgnoreCase("") != 0){
						sLinks = sLinks +  "<FONT SIZE=2><A HREF=\"" + sGDocLink 
						+ "\">Google&nbsp;Drive&nbsp;folder</A>&nbsp;&nbsp;</FONT>";
					}
				}
				
				//Link to create folder and/or upload file to Google Drive:	
				if (bAllowCreateGDriveOrderFolders){
					String sCreateUploadFileLink = "";
					String sFolderName =  smopt.getgdriveorderfolderprefix() 
						+ rsOrder.getString(SMTableorderheaders.strimmedordernumber) 
						+ smopt.getgdriveorderfoldersuffix();
		        	//Parameters for upload folder web-app
		        	//parentfolderid
		        	//foldername
		        	//returnURL
		        	//recordtype
		        	//keyvalue
					try {
						sCreateUploadFileLink = smopt.getgdriveuploadfileurl() + "?"
								+ SMCreateGoogleDriveFolderParamDefinitions.parentfolderid + "=" + smopt.getgdriveorderparentfolderid()
								+ "&" + SMCreateGoogleDriveFolderParamDefinitions.foldername + "=" + sFolderName
								+ "&" + SMCreateGoogleDriveFolderParamDefinitions.backgroundcolor + "=" + smopt.getBackGroundColor()
								+ "&" + SMCreateGoogleDriveFolderParamDefinitions.returnURL + "=" + SMUtilities.getCreateGDriveReturnURL(req, getServletContext())
								+ "&" + SMCreateGoogleDriveFolderParamDefinitions.recordtype + "=" + SMCreateGDriveFolder.DISPLAYED_ORDER_TYPE_PARAM_VALUE
								+ "&" + SMCreateGoogleDriveFolderParamDefinitions.keyvalue + "=" + sOrderNum.trim()
								;
					}catch(Exception e) {
						pwOut.println("Error [1542748927] " + e.getMessage());
						return false;
					}
				
					
					if(bTestGoogleDrivePicker) {
						sLinks += "<FONT SIZE=2><a onclick=\"loadPicker()\" href=\"#\">Upload to google drive</a>&nbsp;&nbsp;</FONT>";	
					}else {
						sLinks += "<FONT SIZE=2><a href=\"" + sCreateUploadFileLink + "\" target=\"_blank\">Create folder/Upload File(s)</a>&nbsp;&nbsp;</FONT>";
					}
					
				}
				
				//Add a link to print the ticket:
				if (bAllowInstallationTicketPrinting && (iOrderType != SMTableorderheaders.ORDERTYPE_QUOTE)){
					sLinks = sLinks
					+ "<FONT SIZE=2><A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) 
					+ "smcontrolpanel.SMPrintInstallationTicketGenerate?" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
					+ "&CallingClass=smcontrolpanel.SMDisplayOrderInformation"
					+ "&StartingOrderNumber=" + sOrderNum
					+ "&EndingOrderNumber=" + sOrderNum
					+ "&" + SMEditOrderSelection.NUMBEROFINSTALLATIONWORKORDERCOPIES + "=1"
					+ "\">Print&nbsp;'installation&nbsp;style'&nbsp;work&nbsp;order</A>&nbsp;&nbsp;</FONT>";
				}
				if (bAllowServiceTicketPrinting && (iOrderType != SMTableorderheaders.ORDERTYPE_QUOTE)){
					sLinks = sLinks
					+ "<FONT SIZE=2><A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) 
					+ "smcontrolpanel.SMPrintServiceTicketGenerate?" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
					+ "&CallingClass=smcontrolpanel.SMDisplayOrderInformation"
					+ "&StartingOrderNumber=" + sOrderNum
					+ "&EndingOrderNumber=" + sOrderNum
					+ "&" + SMEditOrderSelection.NUMBEROFSERVICEWORKORDERCOPIES + "=1"
					+ "\">Print&nbsp;'service&nbsp;style'&nbsp;work&nbsp;order</A>&nbsp;&nbsp;</FONT>";
				}

				//Add link to create delivery ticket
				if(bAllowManageDeliveryTickets && (iOrderType != SMTableorderheaders.ORDERTYPE_QUOTE)){
					sLinks += "&nbsp<FONT SIZE=2><A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) 
						+ "smcontrolpanel.SMEditDeliveryTicketEdit?" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
						+ "&" + SMTabledeliverytickets.strimmedordernumber + "=" + sOrderNum
						+ "&" + SMTabledeliverytickets.lid + "=-1"
						+ "\">Add&nbsp;interactive&nbsp;delivery&nbsp;ticket</A>&nbsp;</FONT>";
				}
				//Add link to create labor back charge
				if(bAllowEditLaborBackCharge && (iOrderType != SMTableorderheaders.ORDERTYPE_QUOTE)){
					sLinks += "&nbsp<FONT SIZE=2><A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) 
						+ "smcontrolpanel.SMLaborBackChargeEdit?" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
						+ "&" + SMTablelaborbackcharges.strimmedordernumber + "=" + sOrderNum
						+ "&" + SMTablelaborbackcharges.lid + "=-1" 
						+ "\">Add&nbsp;labor&nbsp;back&nbsp;charge</A>&nbsp;</FONT>";
				}
				//Add link to create appointment
				if(bAllowEditAppointments && (iOrderType != SMTableorderheaders.ORDERTYPE_QUOTE)){
					sLinks += "&nbsp<FONT SIZE=2><A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) 
						+ "smcontrolpanel.SMEditAppointmentEdit?" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
						+ "&" + SMAppointment.Paramsordernumber + "=" + sOrderNum
						+ "&" + SMAppointment.Paramlid + "=-1" 
						+ "&" + SMAppointment.Paramscontactname + "=" + clsServletUtilities.URLEncode(rsOrder.getString(SMTableorderheaders.sShipToContact))
						+ "&" + SMAppointment.Paramsbilltoname + "=" + clsServletUtilities.URLEncode(rsOrder.getString(SMTableorderheaders.sBillToName))
						+ "&" + SMAppointment.Paramsshiptoname + "=" + clsServletUtilities.URLEncode(rsOrder.getString(SMTableorderheaders.sShipToName))
						+ "&" + SMAppointment.Paramsaddress1 + "=" + clsServletUtilities.URLEncode(rsOrder.getString(SMTableorderheaders.sShipToAddress1))
						+ "&" + SMAppointment.Paramsaddress2 + "=" + clsServletUtilities.URLEncode(rsOrder.getString(SMTableorderheaders.sShipToAddress2))
						+ "&" + SMAppointment.Paramsaddress3 + "=" + clsServletUtilities.URLEncode(rsOrder.getString(SMTableorderheaders.sShipToAddress3))
						+ "&" + SMAppointment.Paramsaddress4 + "=" + clsServletUtilities.URLEncode(rsOrder.getString(SMTableorderheaders.sShipToAddress4))
						+ "&" + SMAppointment.Paramscity + "=" + clsServletUtilities.URLEncode(rsOrder.getString(SMTableorderheaders.sShipToCity))
						+ "&" + SMAppointment.Paramsstate + "=" + clsServletUtilities.URLEncode(rsOrder.getString(SMTableorderheaders.sShipToState))
						+ "&" + SMAppointment.Paramszip + "=" + clsServletUtilities.URLEncode(rsOrder.getString(SMTableorderheaders.sShipToZip))
						+ "&" + SMAppointment.Paramsphone + "=" + clsServletUtilities.URLEncode(rsOrder.getString(SMTableorderheaders.sShipToPhone))
						+ "&" + SMAppointment.Paramsemail + "=" + clsServletUtilities.URLEncode(rsOrder.getString(SMTableorderheaders.sshiptoemail))
						+ "&" + SMAppointment.Paramdatentrydate + "=" +SMUtilities.EMPTY_DATE_VALUE
						+ "\">Create&nbsp;appointment</A>&nbsp;</FONT>";
				}
				if(bAllowBidEditing){
					sLinks += "&nbsp<FONT SIZE=2><A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) 
						+ "smcontrolpanel.SMEditBidEntry?" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
						+ "&" + "SubmitAdd=Y"
						+ "&" + SMBidEntry.Paramdatoriginationdate + "=" + clsServletUtilities.URLEncode(clsDateAndTimeConversions.now("M/d/yyyy"))
						+ "&" + SMBidEntry.Paramscreatedfromordernumber + "=" + clsServletUtilities.URLEncode(rsOrder.getString(SMTableorderheaders.strimmedordernumber))
						+ "&" + SMBidEntry.Paramemailaddress + "=" + clsServletUtilities.URLEncode(rsOrder.getString(SMTableorderheaders.sEmailAddress))
						+ "&" + SMBidEntry.Paramsaltphonenumber + "=" + clsServletUtilities.URLEncode(rsOrder.getString(SMTableorderheaders.ssecondarybilltophone))
						+ "&" + SMBidEntry.Paramscontactname + "=" + clsServletUtilities.URLEncode(rsOrder.getString(SMTableorderheaders.sBillToContact))
						+ "&" + SMBidEntry.Paramscustomername + "=" + clsServletUtilities.URLEncode(rsOrder.getString(SMTableorderheaders.sBillToName))
						+ "&" + SMBidEntry.Paramsfaxnumber + "=" + clsServletUtilities.URLEncode(rsOrder.getString(SMTableorderheaders.sBillToFax))
						+ "&" + SMBidEntry.Paramsphonenumber + "=" + clsServletUtilities.URLEncode(rsOrder.getString(SMTableorderheaders.sBillToPhone))
						+ "&" + SMBidEntry.Paramssalespersoncode + "=" + clsServletUtilities.URLEncode(rsOrder.getString(SMTableorderheaders.sSalesperson))
						+ "&" + SMBidEntry.ParamiOrderSourceID + "=" + clsServletUtilities.URLEncode(rsOrder.getString(SMTableorderheaders.iOrderSourceID))
						+ "&" + SMBidEntry.Paramsprojectname + "=" + clsServletUtilities.URLEncode(rsOrder.getString(SMTableorderheaders.sShipToName))
						+ "&" + SMBidEntry.Paramsshiptoaddress1 + "=" + clsServletUtilities.URLEncode(rsOrder.getString(SMTableorderheaders.sShipToAddress1))
						+ "&" + SMBidEntry.Paramsshiptoaddress2 + "=" + clsServletUtilities.URLEncode(rsOrder.getString(SMTableorderheaders.sShipToAddress2))
						+ "&" + SMBidEntry.Paramsshiptoaddress3 + "=" + clsServletUtilities.URLEncode((rsOrder.getString(SMTableorderheaders.sShipToAddress3) + " " + rsOrder.getString(SMTableorderheaders.sShipToAddress4).trim()))
						+ "&" + SMBidEntry.Paramsshiptocity + "=" + clsServletUtilities.URLEncode(rsOrder.getString(SMTableorderheaders.sShipToCity))
						+ "&" + SMBidEntry.Paramsshiptostate + "=" + clsServletUtilities.URLEncode(rsOrder.getString(SMTableorderheaders.sShipToState))
						+ "&" + SMBidEntry.Paramsshiptozip + "=" + clsServletUtilities.URLEncode(rsOrder.getString(SMTableorderheaders.sShipToZip))
						+ "&CallingClass=smcontrolpanel.SMEditBidEntry"
						+ "&OriginalCallingClass=smcontrolpanel.SMEditBidSelect"
						+ "\">Create&nbsp;follow&nbsp;up&nbsp;sales&nbsp;lead</A>&nbsp;</FONT>";
				}

				pwOut.println(sLinks + "<BR>");
				
				//End of string of links
				
				String sCustomerCode = rsOrder.getString(SMTableorderheaders.sCustomerCode).trim();
				String sCustomerCodeLink = sCustomerCode;
				if (bAllowCustomerView){
					sCustomerCodeLink = 
						"<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smar.ARDisplayCustomerInformation?CustomerNumber=" 
						+ sCustomerCode + "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID + "\">" + sCustomerCode + "</A>"
						;
				}
				sDefaultSP = rsOrder.getString(SMTableorderheaders.sSalesperson).trim();
				String sSalespersonName = rsOrder.getString(SMTablesalesperson.sSalespersonFirstName);
				if (sSalespersonName == null){sSalespersonName = "";}
				if (rsOrder.getString(SMTablesalesperson.sSalespersonLastName) != null){
					sSalespersonName += " " + rsOrder.getString(SMTablesalesperson.sSalespersonLastName);
				}
				String sCustomerOnHold = "N";
				if (rsOrder.getLong(SMTablearcustomer.TableName + "." + SMTablearcustomer.iOnHold) != 0){
					sCustomerOnHold = "Y";
				}
				sOrderDate = clsDateAndTimeConversions.utilDateToString(rsOrder.getDate(SMTableorderheaders.datOrderDate),"M/d/yyyy");
				
				
				String sDBAName = getDoingBuisnessAsDescription(rsOrder, conn);
				
				
				//Order header information:
				pwOut.println("<BR><a name=\"OrderHeader\"><B><U>Order Header</U></B>");
				String sOrderType = SMTableorderheaders.getOrderTypeDescriptions(iOrderType);
				pwOut.print("<FONT SIZE=2><BR><B>" + "Order #" + ":</B> </FONT><FONT SIZE=4><I>" 
						+ rsOrder.getString(SMTableorderheaders.sOrderNumber).trim() + "</I></FONT><FONT SIZE=2>&nbsp;&nbsp;"
						+ "<B>Type:</B> " + sOrderType + "&nbsp;&nbsp;"
						+ "<B>Customer:</B> " + sCustomerCodeLink + "&nbsp;&nbsp;"
						+ "<B>On hold?:</B> " + sCustomerOnHold + "&nbsp;&nbsp;"
						+ "<B>Order date:</B> " + sOrderDate + "&nbsp;&nbsp;"
						+ "<B>Salesperson:</B> " + sDefaultSP + "&nbsp;(" + sSalespersonName + ")&nbsp;"
						+ "<B>Doing Business As:</B> " + sDBAName +"&nbsp;"
						+ "<B>Service type:</B> " + rsOrder.getString(SMTableorderheaders.sServiceTypeCodeDescription).trim() + "&nbsp;&nbsp;"
						+ "<B>Sales Group:</B> ");
				if (rsOrder.getInt(SMTableorderheaders.iSalesGroup) == 0){
					pwOut.print("N/A");
				}else{
					pwOut.print(rsOrder.getString(SMTablesalesgroups.sSalesGroupDesc).trim() + "&nbsp;&nbsp;");
				}
				pwOut.println("</FONT><BR>");

				if(bAllowOrderHeaderView){
					pwOut.println("<TABLE BORDER=0 WIDTH=100%  cellspacing=0 cellpadding=1 style= \" background-color: " + ORDER_HEADER_TABLE_BG_COLOR + "; \">");
					pwOut.println("<TR><TD colspan=2><hr></TD></TR>");
					pwOut.println("<TR>");
					String sBillToName = rsOrder.getString(SMTableorderheaders.sBillToName);
					pwOut.println("<TD><FONT SIZE=2><B>Bill to: </B>" + sBillToName + "</FONT></TD>");
					String sShipToName = rsOrder.getString(SMTableorderheaders.sShipToName);
					pwOut.println("<TD><FONT SIZE=2><B>Ship to code - name: </B>" 
							+ rsOrder.getString(SMTableorderheaders.sShipToCode)
							+ " - " + sShipToName + "</FONT></TD>");
	
					String sURLTitle = "Viewing order information for order  " 
						+ sOrderNum + " " + sBillToName + " - " + sShipToName;
					try {
						SMUtilities.addURLToHistory(sURLTitle, session, req);
					} catch (Exception e) {
					}
					pwOut.println("</TR>");
					pwOut.println("<TR>");
					pwOut.println("<TD><FONT SIZE=2><B>Address 1: </B>" + rsOrder.getString(SMTableorderheaders.sBillToAddressLine1) + "</FONT></TD>");
					pwOut.println("<TD><FONT SIZE=2><B>Address 1: </B>" + rsOrder.getString(SMTableorderheaders.sShipToAddress1) + "</FONT></TD>");
					pwOut.println("</TR>");
					pwOut.println("<TR>");
					pwOut.println("<TD><FONT SIZE=2><B>Address 2: </B>" + rsOrder.getString(SMTableorderheaders.sBillToAddressLine2) + "</FONT></TD>");
					pwOut.println("<TD><FONT SIZE=2><B>Address 2: </B>" + rsOrder.getString(SMTableorderheaders.sShipToAddress2) + "</FONT></TD>");
					pwOut.println("</TR>");
					pwOut.println("<TR>");
					pwOut.println("<TD><FONT SIZE=2><B>Address 3: </B>" + rsOrder.getString(SMTableorderheaders.sBillToAddressLine3) + "</FONT></TD>");
					pwOut.println("<TD><FONT SIZE=2><B>Address 3: </B>" + rsOrder.getString(SMTableorderheaders.sShipToAddress3) + "</FONT></TD>");
					pwOut.println("</TR>");
					pwOut.println("<TR>");
					pwOut.println("<TD><FONT SIZE=2><B>Address 4: </B>" + rsOrder.getString(SMTableorderheaders.sBillToAddressLine4) + "</FONT></TD>");
					pwOut.println("<TD><FONT SIZE=2><B>Address 4: </B>" + rsOrder.getString(SMTableorderheaders.sShipToAddress4) + "</FONT></TD>");
					pwOut.println("</TR>");
					pwOut.println("<TR>");
					pwOut.println("<TD><FONT SIZE=2><B>City: </B>" + rsOrder.getString(SMTableorderheaders.sBillToCity) + "</FONT></TD>");
					pwOut.println("<TD><FONT SIZE=2><B>City: </B>" + rsOrder.getString(SMTableorderheaders.sShipToCity) + "</FONT></TD>");
					pwOut.println("</TR>");
					pwOut.println("<TR>");
					pwOut.println("<TD><FONT SIZE=2><B>State: </B>" + rsOrder.getString(SMTableorderheaders.sBillToState) + "</FONT></TD>");
					pwOut.println("<TD><FONT SIZE=2><B>State: </B>" + rsOrder.getString(SMTableorderheaders.sShipToState) + "</FONT></TD>");
					pwOut.println("</TR>");				
					pwOut.println("<TR>");
					pwOut.println("<TD><FONT SIZE=2><B>Country: </B>" + rsOrder.getString(SMTableorderheaders.sBillToCountry) + "</FONT></TD>");
					pwOut.println("<TD><FONT SIZE=2><B>Country: </B>" + rsOrder.getString(SMTableorderheaders.sShipToCountry) + "</FONT></TD>");
					pwOut.println("</TR>");
					pwOut.println("<TR>");
					pwOut.println("<TD><FONT SIZE=2><B>Postal code: </B>" + rsOrder.getString(SMTableorderheaders.sBillToZip) + "</FONT></TD>");
					pwOut.println("<TD><FONT SIZE=2><B>Postal code: </B>" + rsOrder.getString(SMTableorderheaders.sShipToZip) + "</FONT></TD>");
					pwOut.println("</TR>");
					pwOut.println("<TR>");
					pwOut.println("<TD><FONT SIZE=2><B>Contact: </B>" + rsOrder.getString(SMTableorderheaders.sBillToContact) + "</FONT></TD>");
					pwOut.println("<TD><FONT SIZE=2><B>Contact: </B>" + rsOrder.getString(SMTableorderheaders.sShipToContact) + "</FONT></TD>");
					pwOut.println("</TR>");
					pwOut.println("<TR>");
					pwOut.println("<TD><FONT SIZE=2><B>Phone: </B>" + rsOrder.getString(SMTableorderheaders.sBillToPhone) + "</FONT></TD>");
					pwOut.println("<TD><FONT SIZE=2><B>Phone: </B>" + rsOrder.getString(SMTableorderheaders.sShipToPhone) + "</FONT></TD>");
					pwOut.println("</TR>");
					pwOut.println("<TR>");
					pwOut.println("<TD><FONT SIZE=2><B>2nd phone: </B>" + rsOrder.getString(SMTableorderheaders.ssecondarybilltophone) 
							+ "</FONT></TD>");
					pwOut.println("<TD><FONT SIZE=2><B>2nd phone: </B>" + rsOrder.getString(SMTableorderheaders.ssecondaryshiptophone) 
							+ "</FONT></TD>");
					pwOut.println("</TR>");
					pwOut.println("<TR>");
					pwOut.println("<TD><FONT SIZE=2><B>Fax: </B>" + rsOrder.getString(SMTableorderheaders.sBillToFax) + "</FONT></TD>");
					pwOut.println("<TD><FONT SIZE=2><B>Fax: </B>" + rsOrder.getString(SMTableorderheaders.sShipToFax) + "</FONT></TD>");
					pwOut.println("</TR>");
					pwOut.println("<TR>");
					pwOut.println("<TD><FONT SIZE=2><B>Email: </B>" + rsOrder.getString(SMTableorderheaders.sEmailAddress) + "</FONT></TD>");
					String sMapAddress = rsOrder.getString(SMTableorderheaders.sShipToAddress1).trim();
					sMapAddress	= sMapAddress.trim() + " " + rsOrder.getString(SMTableorderheaders.sShipToAddress2).trim();
					sMapAddress	= sMapAddress.trim() + " " + rsOrder.getString(SMTableorderheaders.sShipToAddress3).trim();
					sMapAddress	= sMapAddress.trim() + " " + rsOrder.getString(SMTableorderheaders.sShipToAddress4).trim();
					sMapAddress	= sMapAddress.trim() + " " + rsOrder.getString(SMTableorderheaders.sShipToCity).trim();
					sMapAddress	= sMapAddress.trim() + " " + rsOrder.getString(SMTableorderheaders.sShipToState).trim();
					sMapAddress	= sMapAddress.trim() + " " + rsOrder.getString(SMTableorderheaders.sShipToCountry).trim();
					sMapAddress	= sMapAddress.trim() + " " + rsOrder.getString(SMTableorderheaders.sShipToZip).trim();
					pwOut.println("<TD><FONT SIZE=2><B>Ship to email: </B>" + rsOrder.getString(SMTableorderheaders.sshiptoemail) + "</FONT></TD>");
					pwOut.println("</TR>");
					pwOut.println("<TD><FONT SIZE=2><B>Invoicing contact: </B>"+ rsOrder.getString(SMTableorderheaders.sinvoicingcontact)+"</FONT></TD>");
					pwOut.println("<TD><FONT SIZE=2><B>Map: </B><A HREF=\"" 
							+ clsServletUtilities.createGoogleMapLink(sMapAddress)
							+ "\">"
							+ sMapAddress
							+ "</A>" 
							+ "</FONT></TD>");
					pwOut.println("<TR>");
					pwOut.println("<TD><FONT SIZE=2><B>Invoicing email: </B>"+ rsOrder.getString(SMTableorderheaders.sinvoicingemail)+"</FONT></TD>");
					String sGeocode = rsOrder.getString(SMTableorderheaders.sgeocode);
					if((sGeocode.compareToIgnoreCase("") == 0) || (sGeocode.compareToIgnoreCase("NaN,NaN") == 0)) {
						sGeocode = "Invalid";
					}
					pwOut.println("<TD><FONT SIZE=2><B>Geocode (latitude/longitude): </B>" 
							+ sGeocode + "</FONT></TD>");
					pwOut.println("</TR>");
					
					pwOut.println("</TABLE>");
	
					pwOut.println("<TABLE BORDER=0 WIDTH=100%  style= \" background-color: " + SUBHEADER_TABLE_BG_COLOR + "; \" >");
					pwOut.println("<tr><TD colspan=4><hr></TD></tr>");
					pwOut.println("<TR>");
					pwOut.println("<TD><FONT SIZE=2><B>Price level: </B>" + Integer.toString(rsOrder.getInt(SMTableorderheaders.iCustomerDiscountLevel)) + "</FONT></TD>");
					pwOut.println("<TD><FONT SIZE=2><B>Price list: </B>" + rsOrder.getString(SMTableorderheaders.sDefaultPriceListCode) + "</FONT></TD>");
					pwOut.println("<TD><FONT SIZE=2><B>PO Number: </B>" + rsOrder.getString(SMTableorderheaders.sPONumber) + "</FONT></TD>");
					pwOut.println("<TD><FONT SIZE=2><B>Special wage rate: </B>" + rsOrder.getString(SMTableorderheaders.sSpecialWageRate) + "</FONT></TD>");
					pwOut.println("</TR>");
					pwOut.println("<TR>");
					if(rsOrder.getInt(SMTableorderheaders.iOrderType) == SMTableorderheaders.ORDERTYPE_ACTIVE){
						pwOut.println("<TD><FONT SIZE=2><B>Order type: </B>Active</FONT></TD>");
					}
					if(rsOrder.getInt(SMTableorderheaders.iOrderType) == SMTableorderheaders.ORDERTYPE_QUOTE){
						pwOut.println("<TD><FONT SIZE=2><B>Order type: </B>Quote</FONT></TD>");
					}
					if(rsOrder.getInt(SMTableorderheaders.iOrderType) == SMTableorderheaders.ORDERTYPE_STANDING){
						pwOut.println("<TD><FONT SIZE=2><B>Order type: </B>Standing</FONT></TD>");
					}
					pwOut.println("<TD><FONT SIZE=2><B>Expected ship date: </B>" + clsDateAndTimeConversions.resultsetDateStringToString(rsOrder.getString(SMTableorderheaders.datExpectedShipDate)) + "</FONT></TD>");
					pwOut.println("<TD><FONT SIZE=2><B>Creation date: </B>" 
						+ clsDateAndTimeConversions.resultsetDateTimeStringToString(rsOrder.getString(SMTableorderheaders.datOrderCreationDate))
						+ "</FONT></TD>");
					//pwOut.println("<TD><FONT SIZE=2><B>Creation date: </B>" + SMUtilities.utilDateToString(rsOrder.getDate(SMTableorderheaders.datOrderCreationDate),"MM/d/yyyy") + "</FONT></TD>");
					pwOut.println("<TD><FONT SIZE=2><B>Terms: </B>" + rsOrder.getString(SMTableorderheaders.sTerms) + "</FONT></TD>");
					pwOut.println("</TR>");
					pwOut.println("<TR>");
					pwOut.println("<TD><FONT SIZE=2><B>Last invoice #: </B>" + rsOrder.getString(SMTableorderheaders.sLastInvoiceNumber) + "</FONT></TD>");
					pwOut.println("<TD><FONT SIZE=2><B>Number of invoices: </B>" + Integer.toString(rsOrder.getInt(SMTableorderheaders.iNumberOfInvoices)) + "</FONT></TD>");
					pwOut.println("<TD><FONT SIZE=2><B>Warranty expiration: </B>" +	sWarrantyExpirationDate	+ "</FONT></TD>");
					pwOut.println("<TD><FONT SIZE=2><B>Default item Location: </B>" + rsOrder.getString(SMTableorderheaders.sLocation) + "</FONT></TD>");
					pwOut.println("</TR>");
					pwOut.println("<TR>");				
					if(rsOrder.getInt(SMTableorderheaders.iOnHold) != 0){
						pwOut.println("<TD><FONT SIZE=2><B>Order On hold? </B>YES</FONT></TD>");
					}else{
						pwOut.println("<TD><FONT SIZE=2><B>Order On hold? </B>NO</FONT></TD>");
					}
					pwOut.println("<TD><FONT SIZE=2><B>Default item category: </B>" + rsOrder.getString(SMTableorderheaders.sDefaultItemCategory) + "</FONT></TD>");
					String sCreatedByFullName = rsOrder.getString(SMTableorderheaders.sOrderCreatedByFullName);
					if (sCreatedByFullName == null){
						sCreatedByFullName = "";
					}
					pwOut.println("<TD><FONT SIZE=2><B>Created by: </B>" + sCreatedByFullName + "</FONT></TD>");
					pwOut.println("<TD><FONT SIZE=2><B>Deposit amount: </B>" + sDepositAmount + "</FONT></TD>");
					pwOut.println("<TR>");
					pwOut.println("<TD><FONT SIZE=2><B>Tax base: </B>" + clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(rsOrder.getBigDecimal(SMTableorderheaders.bdtaxbase)) + "</FONT></TD>");
					pwOut.println("<TD><FONT SIZE=2><B>Sales Tax amount: </B>" + clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(rsOrder.getBigDecimal(SMTableorderheaders.bdordertaxamount)) + "</FONT></TD>");				
					pwOut.println("<TD COLSPAN=2><FONT SIZE=2><B>Tax: </B>" 
						+ rsOrder.getString(SMTableorderheaders.staxjurisdiction) 
						+ " - "
						+ rsOrder.getString(SMTableorderheaders.staxtype)
						+ " - "
						+ rsOrder.getString(SMTabletax.TableName + "." + SMTabletax.sdescription)
						+ "</FONT></TD>");
					//pwOut.println("<TD><FONT SIZE=2><B>Tax type: </B>" + rsOrder.getString(SMTableorderheaders.staxtype) + "</FONT></TD>");
					pwOut.println("</TR>");
					
					pwOut.println("<TR>");
					pwOut.println("<TD><FONT SIZE=2><B>Order source: </B>" + rsOrder.getString(SMTableorderheaders.sOrderSourceDesc) + "</FONT></TD>");
					pwOut.println("<TD><FONT SIZE=2><B>Pre-posting discount desc: </B>" + rsOrder.getString(SMTableorderheaders.sPrePostingInvoiceDiscountDesc) + "</FONT></TD>");
					pwOut.println("<TD><FONT SIZE=2><B>Pre-posting discount %: </B>" + clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(rsOrder.getBigDecimal(SMTableorderheaders.dPrePostingInvoiceDiscountPercentage)) + "</FONT></TD>");
					pwOut.println("<TD><FONT SIZE=2><B>Pre-posting discount amt: </B>" + clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(rsOrder.getBigDecimal(SMTableorderheaders.dPrePostingInvoiceDiscountAmount)) + "</FONT></TD>");
					pwOut.println("</TR>");
					pwOut.println("<TR>");
					pwOut.println("<TD><FONT SIZE=2><B>Estimated hours: </B>" + clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(rsOrder.getBigDecimal(SMTableorderheaders.dEstimatedHour)) + "</FONT></TD>");
					String sLastEditedByFullName = rsOrder.getString(SMTableorderheaders.LASTEDITUSERFULLNAME);
					if (sLastEditedByFullName == null){
						sLastEditedByFullName = "";
					}
					String sLastEditDate = "";
					String sLastEditDateAsLong = "";
					int lLastEditTimeAsLong = 0;
					String sLastEditTimeAsLong = "";
					try {
						sLastEditDateAsLong = Long.toString(rsOrder.getLong(SMTableorderheaders.LASTEDITDATE));
						lLastEditTimeAsLong = rsOrder.getInt(SMTableorderheaders.LASTEDITTIME); 
						if (lLastEditTimeAsLong == 0){
							sLastEditTimeAsLong = "000000";
						}else{
							sLastEditTimeAsLong = Long.toString(lLastEditTimeAsLong);						
						}
						sLastEditDate = Integer.toString(Integer.parseInt(sLastEditDateAsLong.substring(4, 6))) + "/";
						sLastEditDate += Integer.toString(Integer.parseInt(sLastEditDateAsLong.substring(6, 8))) + "/";
						sLastEditDate += sLastEditDateAsLong.substring(0, 4) + "&nbsp;";								
					} catch (Exception e) {
						pwOut.println("<BR><FONT COLOR=RED>Error formatting LAST EDIT DATE - " + e.getMessage() + "<BR>");
					}
					int iHours = lLastEditTimeAsLong / 10000;
					String sAMPM = "AM";
					if (iHours > 11){
						sAMPM = "PM";
					}
					if (iHours > 12){
						iHours = iHours - 12;
					}
					if (iHours == 0){
						iHours = 12;
					}
					sLastEditDate += Integer.toString(iHours) + ":";
					
					try {
						sLastEditDate += sLastEditTimeAsLong.substring(sLastEditTimeAsLong.length() - 4, sLastEditTimeAsLong.length() - 2) + ":";
						sLastEditDate += sLastEditTimeAsLong.substring(sLastEditTimeAsLong.length() - 2, sLastEditTimeAsLong.length()) + "&nbsp;" 
							+ sAMPM;
					} catch (Exception e) {
						sLastEditDate = "*ERROR [1419870770]*";
						System.out.println("[1419870771] - user ID: " + sUserID + ", sDBID = '" + sDBID 
							+ "', strimmedordernumber = '" + sOrderNum 
							+ "', sLastEditTimeAsLong = '" + sLastEditTimeAsLong + "'.");
				}
					//sLastEditDate += " (" + lLastEditTimeAsLong + ")";
					pwOut.println("<TD><FONT SIZE=2><B>Last edited by: </B>" + sLastEditedByFullName + "</FONT></TD>");
					pwOut.println("<TD><FONT SIZE=2><B>Last edit process: </B>"
						+ rsOrder.getString(SMTableorderheaders.LASTEDITPROCESS) + "</FONT></TD>");
					pwOut.println("<TD><FONT SIZE=2><B>Last edit time: </B>"
						+ sLastEditDate + "</FONT></TD>");
					pwOut.println("</TR>");
					pwOut.println("<TR>");
					pwOut.println("<TD><FONT SIZE=2><B>Control acct set: </B>" 
						+ rsOrder.getString(SMTableorderheaders.sCustomerControlAcctSet) + "</FONT></TD>");
					String sCanceledDate;
					if (rsOrder.getString(SMTableorderheaders.datOrderCanceledDate) == null
							|| rsOrder.getString(SMTableorderheaders.datOrderCanceledDate).compareTo("1899-12-31 00:00:00") <= 0
					){
						sCanceledDate = "N/A";
					}else{
						sCanceledDate = "<B><FONT COLOR=RED>" 
							+ USDateOnlyformatter.format(rsOrder.getDate(SMTableorderheaders.datOrderCanceledDate))
							+ "</FONT></B>";
					}
					pwOut.println("<TD><FONT SIZE=2><B>Date canceled: </B>" + sCanceledDate + "</FONT></TD>");
					//Last posting date:
					pwOut.println("<TD><FONT SIZE=2><B>Header last saved: </B>" + clsDateAndTimeConversions.resultsetDateTimeStringToString(
						rsOrder.getString(SMTableorderheaders.datLastPostingDate)) + "</FONT></TD>");
					//Cloned from:
					pwOut.println("<TD><FONT SIZE=2><B>Cloned from: </B>" + rsOrder.getString(SMTableorderheaders.sclonedfrom) + "</FONT></TD>");
					pwOut.println("</TR>");
	
					pwOut.println("<TR>");
					pwOut.println("<tr><TD colspan=4><hr></TD></tr>");
					pwOut.println("<TD colspan=\"4\"><FONT SIZE=2><B>Internal notes: </B>" + rsOrder.getString(SMTableorderheaders.mInternalComments) + "</FONT></TD>");
					pwOut.println("</TR>");
					pwOut.println("<TR>");
					pwOut.println("<TD colspan=\"4\"><FONT SIZE=2><B>Invoice notes: </B>" + rsOrder.getString(SMTableorderheaders.mInvoiceComments) + "</FONT></TD>");
					pwOut.println("</TR>");
					pwOut.println("<TR>");
					pwOut.println("<TD colspan=\"4\"><FONT SIZE=2><B>Invoicing instructions: </B>" + rsOrder.getString(SMTableorderheaders.sinvoicingnotes) + "</FONT></TD>");
					pwOut.println("</TR>");
					pwOut.println("<TR>");
					pwOut.println("<TD colspan=\"4\"><FONT SIZE=2><B>Directions: </B>" + rsOrder.getString(SMTableorderheaders.mDirections) + "</FONT></TD>");
					pwOut.println("</TR>");
					pwOut.println("<TR>");
					pwOut.println("<TD colspan=\"4\"><FONT SIZE=2><B>Work order notes: </B>" + rsOrder.getString(SMTableorderheaders.mTicketComments).replace("\n",  "<BR>") + "</FONT></TD>");
					pwOut.println("</TR>");
					
					// TJR - Removed 10/2/2014:
					/*
					pwOut.println("<TR>");
					String sFieldNotes = rsOrder.getString(SMTableorderheaders.mFieldNotes);
					if (sFieldNotes == null){
						sFieldNotes = "";
					}
					pwOut.println("<TD colspan=\"4\"><FONT SIZE=2><B>Field notes: </B>" + sFieldNotes + "</FONT></TD>");
					pwOut.println("</TR>");
					*/
	
					pwOut.println("<TR>");
					pwOut.println("<TD colspan=\"4\"><FONT SIZE=2><B>Wage scale notes: </B>" 
						+ sWageScaleNotes + "</FONT></TD>");
					pwOut.println("</TR>");
					pwOut.println("<TR>");
					pwOut.println("<TD colspan=\"4\"><FONT SIZE=2><B>Carpenter rate: </B>" 
							+ rsOrder.getString(SMTableorderheaders.scarpenterrate) + "</FONT></TD>");
					pwOut.println("</TR>");
					pwOut.println("<TR>");
					pwOut.println("<TD colspan=\"4\"><FONT SIZE=2><B>Laborer rate: </B>" 
							+ rsOrder.getString(SMTableorderheaders.slaborerrate) + "</FONT></TD>");
					pwOut.println("</TR>");
					pwOut.println("<TR>");
					pwOut.println("<TD colspan=\"4\"><FONT SIZE=2><B>Electrician rate: </B>" 
							+ rsOrder.getString(SMTableorderheaders.selectricianrate) + "</FONT></TD>");
					pwOut.println("</TR>");
					pwOut.println("<TR>");
					
					long lBidID = rsOrder.getLong(SMTableorderheaders.lbidid);
					String sBidID = Long.toString(lBidID);
					if (bAllowBidEditing && lBidID > 0L){
						sBidID = "<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMEditBidEntry"
					    	+ "?" + SMBidEntry.ParamID + "=" + sBidID 
					    	+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
					    	+ "\">" 
					    	+ sBidID + "</A>"
						;
					}else{
						sBidID = "N/A";
					}
					
					pwOut.println("<TD colspan=\"4\"><FONT SIZE=2><B>Generated from " + SMBidEntry.ParamObjectName + " ID: </B>" 
							+ sBidID
							+ "&nbsp;<B>Quote description</B>:&nbsp;"
							+ rsOrder.getString(SMTableorderheaders.squotedescription)
							+ "</FONT></TD>");
					pwOut.println("</TR>");
					
					pwOut.println("<tr><TD colspan=4><hr></TD></tr>");
					pwOut.println("</TABLE>");
				}
			}else{
				pwOut.println("Order not found.");
				clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1547080461]");
				return false;
			}
			rsOrder.close();
		}catch (SQLException e){
			clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1547080462]");
			pwOut.println("Error opening order query: " + e.getMessage());
			return false;
		}

		//Load the order:
		SMOrderHeader order = new SMOrderHeader();
		order.setM_strimmedordernumber(sOrderNum);
		if (!order.load(conn)){
			clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1547080463]");
			pwOut.println ("Error [1411490347] loading order - " + order.getErrorMessages());
			return false;
		}
		
		//Print order details:
		try {
			clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1547080464]");
			pwOut.println(printOrderDetails(order, conn, sLinks, sUserID, bAllowOrderDetailViewing, bAllowItemViewing, sDBID));
		} catch (Exception e) {
			pwOut.println("<BR><FONT COLOR=RED>" + e.getMessage() + "</FONT><BR>");
		}

		//Print change order information:
		try {
			pwOut.println(printChangeOrders(order, conn, sLinks, sUserID, bAllowProjectView, bAllowChangeOrderEditing, sDBID));
		} catch (Exception e) {
			pwOut.println("<BR><FONT COLOR=RED>" + e.getMessage() + "</FONT><BR>");
		}
		
		//Print a list of invoices for this order:
		try {
			pwOut.println(printBillingSummary(
				bAllowProjectView, 
				bAllowInvoiceView,
				order,
				sOrderNum, 
				sLinks, 
				bdTotalBilled, 
				conn,
				sDBID
			));
		} catch (Exception e) {
			pwOut.println("<BR><FONT COLOR=RED>" + e.getMessage() + "</FONT><BR>");
		}
		
		//Critical dates
		try {
			pwOut.println(printCriticalDates(sOrderNum, sUserID, sLinks, conn, bAllowProjectView, sDBID));
		} catch (Exception e) {
			pwOut.println("<BR><FONT COLOR=RED>" + e.getMessage() + "</FONT><BR>");
		}
		
		//Print work order information:
		try {
			pwOut.println(printWorkOrders(sOrderNum, conn, sLinks, sUserID, bAllowEditWorkOrders, sDBID));
		} catch (Exception e) {
			pwOut.println("<BR><FONT COLOR=RED>" + e.getMessage() + "</FONT><BR>");
		}
		
		//Print appointment information:
		if(bAllowViewAppointments){
			try {
				pwOut.println(printAppointments(sOrderNum, conn, sLinks, sUserID, bAllowEditAppointments, sDBID));
			} catch (Exception e) {
				pwOut.println("<BR><FONT COLOR=RED>" + e.getMessage() + "</FONT><BR>");
			}
		}
		
		//Print delivery ticket information:
		//*** Restrict view until Delivery tickets are finished
		if(bAllowManageDeliveryTickets){ 
		try {
			pwOut.println(printDeliveryTickets(sOrderNum, conn, sLinks, sUserID, bAllowManageDeliveryTickets, sDBID));
		} catch (Exception e) {
			pwOut.println("<BR><FONT COLOR=RED>" + e.getMessage() + "</FONT><BR>");
		}
		}
		
		//Print material returns:
		try {
			pwOut.println(printMaterialReturns(sOrderNum, conn, sLinks, sUserID, bAllowEditMaterialReturns, sDBID));
		} catch (Exception e) {
			pwOut.println("<BR><FONT COLOR=RED>" + e.getMessage() + "</FONT><BR>");
		}
		
		//Print labor back charges:
		try {
			pwOut.println(printLaborBackCharges(sOrderNum, conn, sLinks, sUserID, bAllowEditLaborBackCharge, sDBID));
		} catch (Exception e) {
			pwOut.println("<BR><FONT COLOR=RED>" + e.getMessage() + "</FONT><BR>");
		}
		
		//Proposal goes here:
		try {
			pwOut.println(printProposal(
				conn,
				sDBID,
				sUserID,
				sLinks,
				sOrderNum.trim(),
				bAllowProposalViewing
			));
		} catch (Exception e) {
			pwOut.println("<BR><FONT COLOR=RED>" + e.getMessage() + "</FONT><BR>");
		}

		//Print Items left on order:
		try {
			clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1547080465]");
			pwOut.println(ItemsLeftOnOrder(order, conn, sLinks,sUserID,bAllowOrderDetailViewing, bAllowItemViewing,  sDBID));
		} catch (Exception e) {
			pwOut.println("<BR><FONT COLOR=RED>" + e.getMessage() + "</FONT><BR>");
		}
		
		//Tax calculation:
		try {
			pwOut.println(printTaxCalculation(sOrderNum, order, conn, sLinks, bAllowOrderDetailViewing));
		} catch (Exception e) {
			pwOut.println("<BR><FONT COLOR=RED>" + e.getMessage() + "</FONT><BR>");
		}
				
		//Follow Up Sales Leads	
		try {
			pwOut.println(printFolowUpSalesLeads(sOrderNum, conn, sLinks, sUserID, bAllowBidEditing, sDBID));
		} catch (Exception e) {
			pwOut.println("<BR><FONT COLOR=RED>" + e.getMessage() + "</FONT><BR>");
		}
		clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1547080466]");
		return true;
	}
	private String printOrderDetails(
			SMOrderHeader order, 
			Connection conn, 
			String sLinks, 
			String sUserID,
			boolean bAllowOrderDetailViewing,
			boolean bAllowItemViewing,
			String sDBID) throws Exception {
		String s = "";
		boolean bOddRow = true;
		String sBackgroundColor = "";
		int iRowSpan = 0;
		String sInvoiceComments = "";
		String sInternalComments = "";
		String sTicketComments = "";
		String sWorkOrderComment = "";
		
		if (!bAllowOrderDetailViewing){
			return s;
		}
		try{
			String SQL = "SELECT " + SMTableorderdetails.TableName + ".*, " + SMTableicitems.TableName + "." 
			+ SMTableicitems.sworkordercomment
			+ ", '" + sUserID + "' AS QUERYINGUSERID"
			+ " FROM " + SMTableorderdetails.TableName + " LEFT JOIN " + SMTableicitems.TableName + " ON " + SMTableorderdetails.TableName 
			+ "." + SMTableorderdetails.sItemNumber + " = " + SMTableicitems.TableName + "." + SMTableicitems.sItemNumber
			+ " WHERE ("
			+ SMTableorderdetails.strimmedordernumber + " = '" + order.getM_strimmedordernumber() + "'"
			+ ")"
			+ " ORDER BY " + SMTableorderdetails.iLineNumber
			;
			ResultSet rsDetails = clsDatabaseFunctions.openResultSet(SQL, conn);
			s += "<BR><a name=\"OrderDetails\"><B><U>Order Details</U></B><BR>";
			s += sLinks + "<BR>";
			s += "<TABLE BORDER=0 WIDTH=100%  cellspacing=0 cellpadding=1 style= \" background-color: " 
				+ ORDER_DETAILS_BG_COLOR + "; \" >";
			s += "<TR>";
			s += "<TD ALIGN=RIGHT style=\"border: 1px solid\" bordercolor=\"000\"><FONT SIZE=2><B><BR>Line #</B></TD>";
			s += "<TD ALIGN=RIGHT style=\"border: 1px solid\" bordercolor=\"000\"><FONT SIZE=2><B>Qty<BR>ordered</TD>";
			s += "<TD ALIGN=RIGHT style=\"border: 1px solid\" bordercolor=\"000\"><FONT SIZE=2><B>Qty shipped<BR>to date</B></TD>";
			s += "<TD style=\"border: 1px solid\" bordercolor=\"000\"><FONT SIZE=2><B>Item #</B></TD>";
			s += "<TD style=\"border: 1px solid\" bordercolor=\"000\"><FONT SIZE=2><B>Description</B></TD>";
			s += "<TD style=\"border: 1px solid\" bordercolor=\"000\"><FONT SIZE=2><B>U/M</B></TD>";
			s += "<TD ALIGN=RIGHT style=\"border: 1px solid\" bordercolor=\"000\"><FONT SIZE=2><B>Unit<BR>price</B></TD>";
			s += "<TD ALIGN=RIGHT style=\"border: 1px solid\" bordercolor=\"000\"><FONT SIZE=2><B>Extended<BR>price</B></TD>";
			s += "<TD ALIGN=CENTER style=\"border: 1px solid\" bordercolor=\"000\"><FONT SIZE=2><B>Line<BR>booked</B></TD>";
			s += "<TD ALIGN=CENTER style=\"border: 1px solid\" bordercolor=\"000\"><FONT SIZE=2><B>Line<BR>Cat.</B></TD>";
			s += "<TD ALIGN=CENTER style=\"border: 1px solid\" bordercolor=\"000\"><FONT SIZE=2><B>Line<BR>Loc.</B></TD>";
			s += "</TR>";
			bOddRow = true;

			while(rsDetails.next()){
				/* TJR - 12/5/2011 - changed this logic to ONLY consider qty ordered X unit price after discussion
				 * with Justin Witter
				
				if (rsDetails.getBigDecimal(SMTableorderdetails.dExtendedOrderPrice).compareTo(BigDecimal.ZERO) != 0){
					dRemainingOrderTotal = dRemainingOrderTotal.add(
						rsDetails.getBigDecimal(SMTableorderdetails.dExtendedOrderPrice));
				}else{
					dRemainingOrderTotal = dRemainingOrderTotal.add(
						rsDetails.getBigDecimal(
							SMTableorderdetails.dQtyOrdered).multiply(
							rsDetails.getBigDecimal(SMTableorderdetails.dOrderUnitPrice))
					);
				}
				
				//Updated logic - TJR - 12/5/2011:
				dRemainingOrderTotal = dRemainingOrderTotal.add(
						rsDetails.getBigDecimal(
							SMTableorderdetails.dQtyOrdered).multiply(
							rsDetails.getBigDecimal(SMTableorderdetails.dOrderUnitPrice))
					);
				*/
				//Updated to match rounding on extended prices on individual lines - TJR 2/16/2012:
				if(bOddRow){
					sBackgroundColor = "\"" + DARK_ROW_BG_COLOR + "\"";
				}else{
					sBackgroundColor = "\"" + LIGHT_ROW_BG_COLOR + "\"";
				}
				iRowSpan = 2;

				sInvoiceComments = rsDetails.getString(SMTableorderdetails.mInvoiceComments);
				//System.out.println("invoice comment = " + sInvoiceComments);
				if(sInvoiceComments != null){
					if (sInvoiceComments.compareToIgnoreCase("") !=0){
						iRowSpan++;
					}
				}
				sInternalComments = rsDetails.getString(SMTableorderdetails.mInternalComments);
				//System.out.println("invoice comment = " + sInternalComments);
				if(sInternalComments != null){
					if (sInternalComments.compareToIgnoreCase("") !=0){
						iRowSpan++;
					}
				}
				sTicketComments = rsDetails.getString(SMTableorderdetails.mTicketComments);
				if(sTicketComments != null){
					if (sTicketComments.compareToIgnoreCase("") !=0){
						iRowSpan++;
					}
				}

				sWorkOrderComment = rsDetails.getString(SMTableicitems.TableName + "." + SMTableicitems.sworkordercomment);
				if(sWorkOrderComment != null){
					if (sWorkOrderComment.compareToIgnoreCase("") !=0){
						iRowSpan++;
					}
				}
				String sItemNumber = rsDetails.getString(SMTableorderdetails.sItemNumber);
				String sItemNumberLink = sItemNumber;
				if (bAllowItemViewing){
					sItemNumberLink = "<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) 
					+ "smic.ICDisplayItemInformation?ItemNumber=" 
					+ sItemNumber
					+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
					+ "\">" + sItemNumber + "</A>";
				}	
				
				s += "<TR bgcolor =" + sBackgroundColor + ">";
				s += "<TD class = \"rightjustifiedcell\" style = \" vertical-align: text-top; \" rowspan=" + Integer.toString(iRowSpan) + "><B>" 
					+ clsStringFunctions.PadLeft(Integer.toString(
					rsDetails.getInt(SMTableorderdetails.iLineNumber)),"0", 4) + "</B></TD>";
				s += "<TD class = \"rightjustifiedcell\">" 
					+ clsManageBigDecimals.BigDecimalToScaledFormattedString(SMTableorderdetails.dQtyOrderedScale, 
						rsDetails.getBigDecimal(SMTableorderdetails.dQtyOrdered)) + "</TD>";
				s += "<TD class = \"rightjustifiedcell\">" 
					+ clsManageBigDecimals.BigDecimalToScaledFormattedString(SMTableorderdetails.dQtyShippedScale,
						rsDetails.getBigDecimal(SMTableorderdetails.dQtyShippedToDate)) + "</TD>";
				s += "<TD class = \"leftjustifiedcell\">" + sItemNumberLink + "</TD>";
				s += "<TD class = \"leftjustifiedcell\">" + rsDetails.getString(SMTableorderdetails.sItemDesc) + "</TD>";
				s += "<TD class = \"leftjustifiedcell\">" + rsDetails.getString(SMTableorderdetails.sOrderUnitOfMeasure) + "</TD>";
				s += "<TD class = \"rightjustifiedcell\">" + clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(rsDetails.getBigDecimal(SMTableorderdetails.dOrderUnitPrice)) + "</TD>";
				s += "<TD class = \"rightjustifiedcell\">" + clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(rsDetails.getBigDecimal(SMTableorderdetails.dExtendedOrderPrice)) + "</TD>";
				s += "<TD class = \"centerjustifiedcell\">" + clsDateAndTimeConversions.resultsetDateStringToString(rsDetails.getString(SMTableorderdetails.datLineBookedDate)) + "</TD>";
				s += "<TD class = \"centerjustifiedcell\">" + rsDetails.getString(SMTableorderdetails.sItemCategory) + "</TD>";
				s += "<TD class = \"centerjustifiedcell\">" + rsDetails.getString(SMTableorderdetails.sLocationCode) + "</TD>";
				s += "</TR>";

				s += "<TR bgcolor =" + sBackgroundColor + ">";
				s += "<TD colspan = 6 class = \"leftjustifiedcell\">Mechanic: " 
						+ rsDetails.getString(SMTableorderdetails.sMechInitial) 
						+ " - "
						+ rsDetails.getString(SMTableorderdetails.sMechFullName)
						+ "</TD>";
				s += "<TD colspan = 6  class = \"leftjustifiedcell\" >Door label: " 
						+ rsDetails.getString(SMTableorderdetails.sLabel) 
						+ "</TD>";
				s += "</TR>";

				if(sInvoiceComments != null){
					if(sInvoiceComments.compareToIgnoreCase("") !=0){
						s += "<TR bgcolor =" + sBackgroundColor + ">";
						s += "<TD colspan=12  class = \"leftjustifiedcell\"><B>Invoice detail comment:</B> " 
							+ "<I>" + rsDetails.getString(SMTableorderdetails.mInvoiceComments) + "</I></TD>";
						s += "</TR>";
					}
				}
				if(sInternalComments != null){
					if(sInternalComments.compareToIgnoreCase("") !=0){
						s += "<TR bgcolor =" + sBackgroundColor + ">";
						s += "<TD colspan=12  class = \"leftjustifiedcell\"><B>Internal detail comment:</B> " 
							+ "<I>" + rsDetails.getString(SMTableorderdetails.mInternalComments) + "</I></TD>";
						s += "</TR>";
					}
				}

				if(sTicketComments != null){
					if(sTicketComments.compareToIgnoreCase("") !=0){
						s += "<TR bgcolor =" + sBackgroundColor+ sBackgroundColor + ">";
						s += "<TD colspan=12  class = \"leftjustifiedcell\"><B>Work order detail comment:</B> " 
							+ "<I>" + rsDetails.getString(SMTableorderdetails.mTicketComments) + "</I></TD>";
						s += "</TR>";
					}
				}
				s += "</TR>";

				if(sWorkOrderComment != null){
					if(sWorkOrderComment.compareToIgnoreCase("") !=0){
						s += "<TR bgcolor =" + sBackgroundColor+ sBackgroundColor + ">";
						s += "<TD colspan=12  class = \"leftjustifiedcell\" ><B>Work order item comment:</B> " 
							+ "<I>" + rsDetails.getString(SMTableicitems.sworkordercomment) + "</I></TD>";
						s += "</TR>";
					}
				}
				s += "</TR>";

				bOddRow = ! bOddRow;
			}
			rsDetails.close();
			s += "</TABLE>";
		}catch(SQLException e){
			throw new Exception("Error [1411498997] opening details query: " + e.getMessage());
		}
		return s;
	}

	
	private String ItemsLeftOnOrder(
			SMOrderHeader order, 
			Connection conn, 
			String sLinks, 
			String sUserID,
			boolean bAllowOrderDetailViewing,
			boolean bAllowItemViewing,
			String sDBID) throws Exception {
		String s = "";
		boolean bOddRow = true;
		String sBackgroundColor = "";
		int iRowSpan = 0;
		String sInvoiceComments = "";
		String sInternalComments = "";
		String sTicketComments = "";
		String sWorkOrderComment = "";
		
		
		try{
			String SQL = "SELECT " + SMTableorderdetails.TableName + ".*, " + SMTableicitems.TableName + "." 
			+ SMTableicitems.sworkordercomment
			+ ", '" + sUserID + "' AS QUERYINGUSERID"
			+ " FROM " + SMTableorderdetails.TableName + " LEFT JOIN " + SMTableicitems.TableName + " ON " + SMTableorderdetails.TableName 
			+ "." + SMTableorderdetails.sItemNumber + " = " + SMTableicitems.TableName + "." + SMTableicitems.sItemNumber
			+ " WHERE ("
				+ " (" + SMTableorderdetails.strimmedordernumber + " = '" + order.getM_strimmedordernumber() + "')"
				+ " AND (" + SMTableorderdetails.dQtyOrdered + " > 0 )"
			+ ")"
			+ " ORDER BY " + SMTableorderdetails.iLineNumber
			;
			ResultSet rsDetails = clsDatabaseFunctions.openResultSet(SQL, conn);
			s += "<BR><a name=\"ItemsLeftOnOrder\"><B><U>Item(s) Left On Order</U></B><BR>";
			s += sLinks + "<BR>";
			s += "<TABLE BORDER=0 WIDTH=100%  cellspacing=0 cellpadding=1 style= \" background-color: " 
				+ LEFT_ON_ORDER_BG_COLOR + "; \" >";
			s += "<TR>";
			s += "<TD ALIGN=RIGHT style=\"border: 1px solid\" bordercolor=\"000\"><FONT SIZE=2><B><BR>Line #</B></TD>";
			s += "<TD ALIGN=RIGHT style=\"border: 1px solid\" bordercolor=\"000\"><FONT SIZE=2><B>Qty<BR>Remaining</TD>";
			s += "<TD ALIGN=RIGHT style=\"border: 1px solid\" bordercolor=\"000\"><FONT SIZE=2><B>Qty shipped<BR>to date</B></TD>";
			s += "<TD ALIGN=RIGHT style=\"border: 1px solid\" bordercolor=\"000\"><FONT SIZE=2><B>Original<BR>Qty</B></TD>";
			s += "<TD style=\"border: 1px solid\" bordercolor=\"000\"><FONT SIZE=2><B>Item #</B></TD>";
			s += "<TD style=\"border: 1px solid\" bordercolor=\"000\"><FONT SIZE=2><B>Description</B></TD>";
			s += "<TD style=\"border: 1px solid\" bordercolor=\"000\"><FONT SIZE=2><B>U/M</B></TD>";
			s += "</TR>";
			bOddRow = true;

			while(rsDetails.next()){
				
				if(bOddRow){
					sBackgroundColor = "\"" + DARK_ROW_BG_COLOR + "\"";
				}else{
					sBackgroundColor = "\"" + LIGHT_ROW_BG_COLOR + "\"";
				}
				iRowSpan = 2;

				sInvoiceComments = rsDetails.getString(SMTableorderdetails.mInvoiceComments);
				//System.out.println("invoice comment = " + sInvoiceComments);
				if(sInvoiceComments != null){
					if (sInvoiceComments.compareToIgnoreCase("") !=0){
						iRowSpan++;
					}
				}
				sInternalComments = rsDetails.getString(SMTableorderdetails.mInternalComments);
				//System.out.println("invoice comment = " + sInternalComments);
				if(sInternalComments != null){
					if (sInternalComments.compareToIgnoreCase("") !=0){
						iRowSpan++;
					}
				}
				sTicketComments = rsDetails.getString(SMTableorderdetails.mTicketComments);
				if(sTicketComments != null){
					if (sTicketComments.compareToIgnoreCase("") !=0){
						iRowSpan++;
					}
				}

				sWorkOrderComment = rsDetails.getString(SMTableicitems.TableName + "." + SMTableicitems.sworkordercomment);
				if(sWorkOrderComment != null){
					if (sWorkOrderComment.compareToIgnoreCase("") !=0){
						iRowSpan++;
					}
				}
				String sItemNumber = rsDetails.getString(SMTableorderdetails.sItemNumber);
				String sItemNumberLink = sItemNumber;
				if (bAllowItemViewing){
					sItemNumberLink = "<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) 
					+ "smic.ICDisplayItemInformation?ItemNumber=" 
					+ sItemNumber
					+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
					+ "\">" + sItemNumber + "</A>";
				}
				
				s += "<TR bgcolor =" + sBackgroundColor + ">";
				s += "<TD class = \"rightjustifiedcell\" style = \" vertical-align: text-top; \" rowspan=" + Integer.toString(iRowSpan) + "><B>" 
					+ clsStringFunctions.PadLeft(Integer.toString(
					rsDetails.getInt(SMTableorderdetails.iLineNumber)),"0", 4) + "</B></TD>";
				s += "<TD class = \"rightjustifiedcell\">" 
					+ clsManageBigDecimals.BigDecimalToScaledFormattedString(SMTableorderdetails.dQtyOrderedScale, 
					rsDetails.getBigDecimal(SMTableorderdetails.dQtyOrdered)) + "</TD>";
				s += "<TD class = \"rightjustifiedcell\">" 
					+ clsManageBigDecimals.BigDecimalToScaledFormattedString(SMTableorderdetails.dQtyShippedToDateScale,
						rsDetails.getBigDecimal(SMTableorderdetails.dQtyShippedToDate)) + "</TD>";
				s += "<TD class = \"rightjustifiedcell\">" + 
						clsManageBigDecimals.BigDecimalToScaledFormattedString(SMTableorderdetails.dOriginalQtyScale,
						rsDetails.getBigDecimal(SMTableorderdetails.dOriginalQty)) + "</TD>";;
				s += "<TD class = \"leftjustifiedcell\">" + sItemNumberLink + "</TD>";
				s += "<TD class = \"leftjustifiedcell\">" + rsDetails.getString(SMTableorderdetails.sItemDesc) + "</TD>";
				s += "<TD class = \"leftjustifiedcell\">" + rsDetails.getString(SMTableorderdetails.sOrderUnitOfMeasure) + "</TD>";
				
				s += "</TR>";

				s += "<TR bgcolor =" + sBackgroundColor + ">";
				s += "<TD colspan = 6 class = \"leftjustifiedcell\">Mechanic: " 
						+ rsDetails.getString(SMTableorderdetails.sMechInitial) 
						+ " - "
						+ rsDetails.getString(SMTableorderdetails.sMechFullName)
						+ "</TD>";
				s += "</TR>";

				if(sInvoiceComments != null){
					if(sInvoiceComments.compareToIgnoreCase("") !=0){
						s += "<TR bgcolor =" + sBackgroundColor + ">";
						s += "<TD colspan=12  class = \"leftjustifiedcell\"><B>Invoice detail comment:</B> " 
							+ "<I>" + rsDetails.getString(SMTableorderdetails.mInvoiceComments) + "</I></TD>";
						s += "</TR>";
					}
				}
				if(sInternalComments != null){
					if(sInternalComments.compareToIgnoreCase("") !=0){
						s += "<TR bgcolor =" + sBackgroundColor + ">";
						s += "<TD colspan=12  class = \"leftjustifiedcell\"><B>Internal detail comment:</B> " 
							+ "<I>" + rsDetails.getString(SMTableorderdetails.mInternalComments) + "</I></TD>";
						s += "</TR>";
					}
				}

				if(sTicketComments != null){
					if(sTicketComments.compareToIgnoreCase("") !=0){
						s += "<TR bgcolor =" + sBackgroundColor+ sBackgroundColor + ">";
						s += "<TD colspan=12  class = \"leftjustifiedcell\"><B>Work order detail comment:</B> " 
							+ "<I>" + rsDetails.getString(SMTableorderdetails.mTicketComments) + "</I></TD>";
						s += "</TR>";
					}
				}
				s += "</TR>";

				if(sWorkOrderComment != null){
					if(sWorkOrderComment.compareToIgnoreCase("") !=0){
						s += "<TR bgcolor =" + sBackgroundColor+ sBackgroundColor + ">";
						s += "<TD colspan=12  class = \"leftjustifiedcell\" ><B>Work order item comment:</B> " 
							+ "<I>" + rsDetails.getString(SMTableicitems.sworkordercomment) + "</I></TD>";
						s += "</TR>";
					}
				}
				s += "</TR>";

				bOddRow = ! bOddRow;
			}
			rsDetails.close();
			s += "</TABLE>";
		}catch(SQLException e){
			throw new Exception("Error [1411498998] opening details query: " + e.getMessage());
		}
		return s;
	}
	
	private String printChangeOrders(
			SMOrderHeader order, 
			Connection conn, 
			String sLinks, 
			String sUserID,
			boolean bAllowProjectView,
			boolean bAllowChangeOrderEditing,
			String sDBID) throws Exception {
		String s = "";
		boolean bOddRow = true;
		String sBackgroundColor = "";
		//Change order information goes here:
		BigDecimal bdChangeOrderTotal = new BigDecimal(0);
		if (!bAllowProjectView){
			return s;
		}
		try{
			String SQL = "SELECT * FROM " + SMTablechangeorders.TableName
			+ " WHERE ("
			+ SMTablechangeorders.sJobNumber + " = '" + order.getM_strimmedordernumber() + "'" 
			+ ")"
			+ " ORDER BY " + SMTablechangeorders.datChangeOrderDate
			;
			ResultSet rsChangeOrders = clsDatabaseFunctions.openResultSet(SQL, conn);

			String sLink = "";
			if (bAllowChangeOrderEditing){
				sLink = "&nbsp;&nbsp;<A HREF=\"" 
					+ SMUtilities.getURLLinkBase(getServletContext()) 
					+ "smcontrolpanel.SMEditChangeOrdersEdit?" + SMOrderHeader.ParamsOrderNumber + "=" 
					+ order.getM_strimmedordernumber() 
					+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
					+ "&CallingClass=" + this.getClass().getName()
					+ "\">Edit change orders</A>";
			}

			s += "<BR><B><a name=\"ChangeOrders\"><B><U>Change Orders</U></B></a>"+ sLink + "<BR>";
			s += sLinks + "<BR>";
			s += "<TABLE BORDER=0 WIDTH=100%  cellspacing=0 cellpadding=1 style= \" background-color: " 
					+ CHANGEORDERS_TABLE_BG_COLOR + "; \" >";
			s += "<TR>";
			s += "<TD class = \" leftjustifiedheading \" >Date</TD>";
			s += "<TD class = \" rightjustifiedheading \" >C.O. #</TD>";
			s += "<TD class = \" leftjustifiedheading \" >Description</TD>";
			s += "<TD class = \" rightjustifiedheading \" >Truck Days</TD>";
			s += "<TD class = \" rightjustifiedheading \" >Total MU</TD>";
			s += "<TD class = \" rightjustifiedheading \" >Amount</TD>";
			s += "</TR>";
			bOddRow = true;
			if(bOddRow){
				sBackgroundColor = "\"" + DARK_ROW_BG_COLOR + "\"";
			}else{
				sBackgroundColor = "\"" + LIGHT_ROW_BG_COLOR + "\"";
			}
			s += "<TR bgcolor =" + sBackgroundColor + ">";
			s += "<TD class = \" leftjustifiedcell \" ><B>" + order.getM_datOrderDate() + "</B></TD>";
			s += "<TD class = \" leftjustifiedcell \" >&nbsp;</FONT></TD>";
			s += "<TD class = \" rightjustifiedcell \" ><B>ORIGINAL CONTRACT AMOUNT</B></TD>";
			s += "<TD class = \" rightjustifiedcell \" ><B>" + order.getM_bdtruckdays() + "</B></TD>";
			s += "<TD class = \" rightjustifiedcell \" ><B>" + order.getM_bdtotalmarkup() + "</B></TD>";
			s += "<TD class = \" rightjustifiedcell \" ><B>" + order.getM_bdtotalcontractamount() + "</B></TD>";
			s += "</TR>";
			bOddRow = ! bOddRow;
			while(rsChangeOrders.next()){
				bdChangeOrderTotal = bdChangeOrderTotal.add(rsChangeOrders.getBigDecimal(SMTablechangeorders.dAmount.replace("`", "")));
				if(bOddRow){
					sBackgroundColor = "\"" + DARK_ROW_BG_COLOR + "\"";
				}else{
					sBackgroundColor = "\"" + LIGHT_ROW_BG_COLOR + "\"";
				}
				//sCellStyle = "style=\"border: 0px solid\" bordercolor=" + sBackgroundColor;
				s += "<TR bgcolor =" + sBackgroundColor + ">";
				s += "<TD class = \" leftjustifiedcell \" >" + clsDateAndTimeConversions.utilDateToString(rsChangeOrders.getDate(
					SMTablechangeorders.datChangeOrderDate.replace("`", "")),"M/d/yyyy") + "</TD>";
				s += "<TD class = \" rightjustifiedcell \" >" + clsManageBigDecimals.doubleToDecimalFormat(rsChangeOrders.getDouble(
					SMTablechangeorders.dChangeOrderNumber.replace("`", "")),0) + "</TD>";
				s += "<TD class = \" leftjustifiedcell \" >" + rsChangeOrders.getString(SMTablechangeorders.sDesc.replace("`", "")) 
					+ "</TD>";
				s += "<TD class = \" rightjustifiedcell \" >" + clsManageBigDecimals.doubleTo2DecimalSTDFormat(rsChangeOrders.getDouble(
					SMTablechangeorders.dTruckDays.replace("`", ""))) + "</TD>";
				s += "<TD class = \" rightjustifiedcell \" >" + clsManageBigDecimals.doubleTo2DecimalSTDFormat(rsChangeOrders.getDouble(
					SMTablechangeorders.dTotalMarkUp.replace("`", ""))) + "</TD>";
				s += "<TD class = \" rightjustifiedcell \" >" + clsManageBigDecimals.doubleTo2DecimalSTDFormat(rsChangeOrders.getDouble(
					SMTablechangeorders.dAmount.replace("`", ""))) + "</TD>";
				s += "</TR>";
				bOddRow = ! bOddRow;
			}
			rsChangeOrders.close();
			
			//Print the change order total:
			if(bOddRow){
				sBackgroundColor = "\"" + DARK_ROW_BG_COLOR + "\"";
			}else{
				sBackgroundColor = "\"" + LIGHT_ROW_BG_COLOR + "\"";
			}
			s += "<TR bgcolor =" + sBackgroundColor + ">";
			s += "<TD class = \" rightjustifiedcell \" COLSPAN = 5><B>CHANGE ORDER TOTAL:</B></TD>"
				+ "<TD class = \" rightjustifiedcell \" ><B>" 
				+ clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(bdChangeOrderTotal) + "</B></TD>"
				+ "</TR>"
			;
			bOddRow = ! bOddRow;
			if(bOddRow){
				sBackgroundColor = "\"" + DARK_ROW_BG_COLOR + "\"";
			}else{
				sBackgroundColor = "\"" + LIGHT_ROW_BG_COLOR + "\"";
			}
			//Print the total of the contract amount AND the change orders:
			s += "<TR bgcolor =" + sBackgroundColor + ">";
			s += "<TD class = \" rightjustifiedcell \" COLSPAN = 5><B>"
				+ "TOTAL CONTRACT AMOUNT (ORIGINAL CONTRACT AMOUNT <I>PLUS</I> CHANGE ORDER TOTAL):"
				+ "</B></TD>"
				+ "<TD class = \" rightjustifiedcell \" ><B>" 
				+ clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(
					bdChangeOrderTotal.add(new BigDecimal(order.getM_bdtotalcontractamount().replace(",", "")))
					) 
				+ "</B></TD>"
				+ "</TR>"
			;
			s += "</TABLE>";
		}catch(SQLException e){
			throw new Exception("Error [1411490198] opening change orders query: " + e.getMessage());
		}
		return s;
	}

	private String printWorkOrders(
			String sOrderNumber, 
			Connection conn, 
			String sLinks, 
			String sUserID,
			boolean bAllowEditWorkOrders,
			String sDBID) throws Exception {
		String s = "";
		boolean bOddRow = true;
		String sBackgroundColor = "";
		try{
			String SQL = "SELECT * FROM " + SMTableworkorders.TableName
			+ " WHERE ("
			+ SMTableworkorders.strimmedordernumber + " = '" + sOrderNumber.trim() + "'" 
			+ ")"
			+ " ORDER BY " + SMTableworkorders.datscheduleddate
			;
			ResultSet rsWorkOrders = clsDatabaseFunctions.openResultSet(SQL, conn);

			s += "<BR><a name=\"WorkOrders\"><B><U>Work Orders</U></B></a><BR>";
			s += sLinks + "<BR>";
			s += "<TABLE BORDER=0 WIDTH=100%  cellspacing=0 cellpadding=1 style= \" background-color: " 
					+ WORK_ORDERS_TABLE_BG_COLOR + "; \" >";
			s += "<TR>";
			s += "<TD class = \" leftjustifiedheading \" >Schedule date</TD>";
			s += "<TD class = \" leftjustifiedheading \" >Done date</TD>";
			s += "<TD class = \" leftjustifiedheading \" >W.O. #</TD>";
			s += "<TD class = \" leftjustifiedheading \" >View?</TD>";
			s += "<TD class = \" leftjustifiedheading \" ><B>Mechanic</TD>";
			s += "<TD class = \" leftjustifiedheading \" >Posted?</TD>";
			s += "<TD class = \" leftjustifiedheading \" ><B>Imported?</TD>";
			s += "<TD class = \" leftjustifiedheading \" ><B>Documents</TD>";
			s += "</TR>";

			bOddRow = true;
			while(rsWorkOrders.next()){
				if(bOddRow){
					sBackgroundColor = "\"" + DARK_ROW_BG_COLOR + "\"";
				}else{
					sBackgroundColor = "\"" + LIGHT_ROW_BG_COLOR + "\"";
				}
				s += "<TR bgcolor =" + sBackgroundColor + ">";
				s += "<TD class = \" leftjustifiedcell \" >" 
						+ clsDateAndTimeConversions.resultsetDateStringToString(rsWorkOrders.getString(SMTableworkorders.datscheduleddate)) + "</TD>";
				s += "<TD class = \" leftjustifiedcell \" >" 
					+ clsDateAndTimeConversions.resultsetDateStringToString(rsWorkOrders.getString(SMTableworkorders.dattimedone)) + "</TD>";
					//+ SMUtilities.utilDateToString(rsWorkOrders.getDate(SMTableworkorders.dattimedone),"M/d/yyyy") + "</TD>";

				//First, a link to the ID for editing:
				String sWorkOrderID = Long.toString(rsWorkOrders.getLong(SMTableworkorders.lid));
				String sWOLink = sWorkOrderID;
				if (bAllowEditWorkOrders){
					sWOLink = "<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMWorkOrderEdit?"
						+ SMWorkOrderHeader.Paramlid + "=" + sWorkOrderID
						+ "&" + SMWorkOrderHeader.Paramstrimmedordernumber + "=" + sOrderNumber.trim()
						+ "&CallingClass=" + SMUtilities.getFullClassName(this.toString())
						+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
						+ "\">" + sWorkOrderID 
						+ "</A>"
					;
				}
				s += "<TD class = \" leftjustifiedcell \" >" + sWOLink + "</TD>";
				//Next a link to VIEW the work order:
				String sViewWOLink = "(Not posted)";
				if (rsWorkOrders.getInt(SMTableworkorders.iposted) == 1){
					sViewWOLink = "<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMWorkOrderAction?"
						+ SMWorkOrderEdit.COMMAND_FLAG + "=" + SMWorkOrderEdit.PRINTRECEIPTCOMMAND_VALUE
						+ "&" + SMWorkOrderHeader.Paramlid + "=" + sWorkOrderID
						+ "&" + SMWorkOrderHeader.Paramstrimmedordernumber + "=" + sOrderNumber.trim()
						+ "&CallingClass=" + SMUtilities.getFullClassName(this.toString())
						+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
						+ "\">" + "View" 
						+ "</A>"
					;
				}
				s += "<TD class = \" leftjustifiedcell \" >" + sViewWOLink + "</TD>";
				s += "<TD class = \" leftjustifiedcell \" >" + rsWorkOrders.getString(SMTableworkorders.smechanicname) + "</TD>";
				String sPosted = "N";
				if (rsWorkOrders.getInt(SMTableworkorders.iposted)==1){
					sPosted = "Y";
				}
				s += "<TD class = \" leftjustifiedcell \" >" + sPosted + "</TD>";
				String sImported = "N";
				if (rsWorkOrders.getInt(SMTableworkorders.iimported)==1){
					sImported = "Y";
				}
				s += "<TD class = \" leftjustifiedcell \" >" + sImported + "</TD>";
				
				String sGDocLink = rsWorkOrders.getString(SMTableworkorders.sgdoclink);
				if(sGDocLink.compareToIgnoreCase("") != 0){
					s+= "<TD class = \" leftjustifiedcell \"><A HREF=\"" + sGDocLink + "\">" + "View Folder" + "</A></TD>";
				}else{
					s+= "<TD class = \" leftjustifiedcell \">None</TD>";
				}
				s += "</TR>";
				bOddRow = ! bOddRow;
			}
			rsWorkOrders.close();
			s += "</TABLE>";
		}catch(SQLException e){
			s += "Error opening work orders query: " + e.getMessage();
		}
		return s;
	}
	
	private String printAppointments(
			String sOrderNumber, 
			Connection conn, 
			String sLinks, 
			String sUserID,
			boolean bAllowEditAppointments,
			String sDBID) throws Exception {
		String s = "";
		boolean bOddRow = true;
		String sBackgroundColor = "";
		try{
			String SQL = "SELECT * FROM " + SMTableappointments.TableName
			+ " WHERE ("
			+ SMTableappointments.sordernumber + " = '" + sOrderNumber.trim() + "'" 
			+ ")"
			+ " ORDER BY " + SMTableappointments.datentrydate 
			+ ", " + SMTableappointments.iminuteofday
			+ ", " + SMTableappointments.luserid
			;
			ResultSet rsAppointments = clsDatabaseFunctions.openResultSet(SQL, conn);

			s += "<BR><a name=\"Appointments\"><B><U>Appointments</U></B></a><BR>";
			s += sLinks + "<BR>";
			s += "<TABLE BORDER=0 WIDTH=100%  cellspacing=0 cellpadding=1 style= \" background-color: " 
					+ APPOINTMENTS_TABLE_BG_COLOR + "; \" >";
			s += "<TR>";
			s += "<TD class = \" leftjustifiedheading \" >ID #</TD>";
			s += "<TD class = \" leftjustifiedheading \" >Date/Time</TD>";
			s += "<TD class = \" leftjustifiedheading \" >Scheduled for</TD>";
			s += "<TD class = \" leftjustifiedheading \" >Comment</TD>";
			s += "</TR>";

			bOddRow = true;
			while(rsAppointments.next()){
				if(bOddRow){
					sBackgroundColor = "\"" + DARK_ROW_BG_COLOR + "\"";
				}else{
					sBackgroundColor = "\"" + LIGHT_ROW_BG_COLOR + "\"";
				}
				s += "<TR bgcolor =" + sBackgroundColor + ">";
				s += "<TD class = \" leftjustifiedcell \" >";
				if(bAllowEditAppointments){		
				s += "<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMEditAppointmentEdit?" 
						 + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID+"&"
						+ SMAppointment.Paramlid + "=" + rsAppointments.getString(SMTableappointments.lid) + "\">"
						+ rsAppointments.getString(SMTableappointments.lid) + "</A>";
				}else{
					s += rsAppointments.getString(SMTableappointments.lid);
				}
				s += "</TD>";
				s += "<TD class = \" leftjustifiedcell \" >" 
					+ clsDateAndTimeConversions.resultsetDateStringToString(rsAppointments.getString(SMTableappointments.datentrydate)) 
					+ " " + SMAppointment.timeIntegerToString(rsAppointments.getInt(SMTableappointments.iminuteofday))
					+ "</TD>";
				s += "<TD class = \" leftjustifiedcell \" >" 
					+ SMUtilities.getFullNamebyUserID(rsAppointments.getString(SMTableappointments.luserid), conn)
					+ "</TD>";
				s += "<TD class = \" leftjustifiedcell \" >" 
					+ rsAppointments.getString(SMTableappointments.mcomment)
					+ "</TD>";
				s += "</TR>";
				bOddRow = ! bOddRow;
			}
			rsAppointments.close();
			s += "</TABLE>";
		}catch(SQLException e){
			s += "Error opening appointment query: " + e.getMessage();
		}
		return s;
	}
	
	private String printDeliveryTickets(
			String sOrderNumber, 
			Connection conn, 
			String sLinks, 
			String sUserID,
			boolean bAllowCreateDeliveryTicket,
			String sDBID) throws Exception {
		String s = "";
		boolean bOddRow = true;
		String sBackgroundColor = "";
		try{
			String SQL = "SELECT * FROM " + SMTabledeliverytickets.TableName
			+ " WHERE ("
			+ SMTabledeliverytickets.strimmedordernumber + " = '" + sOrderNumber.trim() + "'" 
			+ ")"
			+ " ORDER BY " + SMTabledeliverytickets.datinitiated
			;
			ResultSet rsDeliveryTicket = clsDatabaseFunctions.openResultSet(SQL, conn);

			s += "<BR><a name=\"DeliveryTickets\"><B><U>Delivery Ticket</U></B></a><BR>";
			s += sLinks + "<BR>";
			s += "<TABLE BORDER=0 WIDTH=100%  cellspacing=0 cellpadding=1 style= \" background-color: " 
					+ DELIVERY_TICKET_TABLE_BG_COLOR + "; \" >";
			s += "<TR>";
			s += "<TD class = \" leftjustifiedheading \" >Initiated date</TD>";		
			s += "<TD class = \" leftjustifiedheading \" >Delivery Ticket #</TD>";
			s += "<TD class = \" leftjustifiedheading \" >View?</TD>";
			s += "<TD class = \" leftjustifiedheading \" >Posted?</TD>";
			s += "<TD class = \" leftjustifiedheading \" >W.O. #</TD>";
			s += "</TR>";

			bOddRow = true;
			while(rsDeliveryTicket.next()){
				if(bOddRow){
					sBackgroundColor = "\"" + DARK_ROW_BG_COLOR + "\"";
				}else{
					sBackgroundColor = "\"" + LIGHT_ROW_BG_COLOR + "\"";
				}
				s += "<TR bgcolor =" + sBackgroundColor + ">";
				s += "<TD class = \" leftjustifiedcell \" >" 
						+ clsDateAndTimeConversions.resultsetDateStringToString(rsDeliveryTicket.getString(SMTabledeliverytickets.datinitiated)) + "</TD>";
				
				//First, a link to the ID for editing:
				String sDeliveryTicketID = Long.toString(rsDeliveryTicket.getLong(SMTabledeliverytickets.lid));
				String sDTLink = sDeliveryTicketID;
				if (bAllowCreateDeliveryTicket){
					sDTLink = "<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMEditDeliveryTicketEdit?"
						+ SMDeliveryTicket.Paramlid + "=" + sDeliveryTicketID
						+ "&" + SMDeliveryTicket.Paramstrimmedordernumber + "=" + sOrderNumber.trim()
						+ "&CallingClass=" + SMUtilities.getFullClassName(this.toString())
						+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
						+ "\">" + sDeliveryTicketID 
						+ "</A>"
					;
				}
				s += "<TD class = \" leftjustifiedcell \" >" + sDTLink + "</TD>";
				//Next a link to VIEW the delivery ticket:
				String sViewDTLink = "(Not posted)";
				if (rsDeliveryTicket.getInt(SMTableworkorders.iposted) == 1){
					sViewDTLink = "<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMEditDeliveryTicketAction?"
						+ SMDeliveryTicket.Paramlid + "=" + sDeliveryTicketID
						+ "&" + SMEditDeliveryTicketEdit.COMMAND_FLAG + "=" + SMEditDeliveryTicketEdit.PRINT_COMMAND_VALUE
						+ "&CallingClass=" + SMUtilities.getFullClassName(this.toString())
						+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
						+ "\">" + "View" 
						+ "</A>"
					;
				}
		
				s += "<TD class = \" leftjustifiedcell \" >" + sViewDTLink + "</TD>";
			
				String sPosted = "N";
				if (rsDeliveryTicket.getInt(SMTabledeliverytickets.iposted)==1){
					sPosted = "Y";
				}
				s += "<TD class = \" leftjustifiedcell \" >" + sPosted + "</TD>";
				
				String sWONumber = "(None)";
				if(rsDeliveryTicket.getString(SMTabledeliverytickets.iworkorderid).compareToIgnoreCase("0") != 0){
					sWONumber = rsDeliveryTicket.getString(SMTabledeliverytickets.iworkorderid);
				}
				s += "<TD class = \" leftjustifiedcell \" >" 
					+ sWONumber + "</TD>";
					//+ SMUtilities.utilDateToString(rsWorkOrders.getDate(SMTableworkorders.dattimedone),"M/d/yyyy") + "</TD>";
				
				s += "</TR>";
				bOddRow = ! bOddRow;
			}
			rsDeliveryTicket.close();
			s += "</TABLE>";
		}catch(SQLException e){
			s += "Error opening delivery ticket query: " + e.getMessage();
		}
		return s;
	}
	
	private String printMaterialReturns(
			String sOrderNumber, 
			Connection conn, 
			String sLinks, 
			String sUserID,
			boolean bAllowEditMaterialReturns,
			String sDBID) throws Exception {
		String s = "";
		boolean bOddRow = true;
		String sBackgroundColor = "";
		try{
			String SQL = "SELECT * FROM " + SMTablematerialreturns.TableName
			+ " WHERE ("
			+ SMTablematerialreturns.strimmedordernumber + " = '" + sOrderNumber.trim() + "'" 
			+ ")"
			+ " ORDER BY " + SMTablematerialreturns.datinitiated
			;
			ResultSet rsMaterialReturns = clsDatabaseFunctions.openResultSet(SQL, conn);
			s += "<BR><a name=\"MaterialReturns\"><B><U>MaterialReturns</U></B></a><BR>";
			s += sLinks + "<BR>";
			s += "<TABLE BORDER=0 WIDTH=100%  cellspacing=0 cellpadding=1 style= \" background-color: " 
				+ MATERIAL_RETURNS_TABLE_BG_COLOR + "; \" >";
			s += "<TR>";
			s += "<TD class = \" leftjustifiedheading \" >ID #</TD>";
			s += "<TD class = \" leftjustifiedheading \" >Initiated</TD>";
			s += "<TD class = \" leftjustifiedheading \" >By</TD>";
			s += "<TD class = \" leftjustifiedheading \" >W.O. #</TD>";
			s += "<TD class = \" leftjustifiedheading \" >To be returned?</TD>";
			s += "<TD class = \" leftjustifiedheading \" >Vendor #</TD>";
			s += "<TD class = \" leftjustifiedheading \" >Resolved?</TD>";
			s += "<TD class = \" leftjustifiedheading \" >Description</TD>";
			s += "</TR>";

			while(rsMaterialReturns.next()){
				if(bOddRow){
					sBackgroundColor = "\"" + DARK_ROW_BG_COLOR + "\"";
				}else{
					sBackgroundColor = "\"" + LIGHT_ROW_BG_COLOR + "\"";
				}
				s += "<TR bgcolor =" + sBackgroundColor + ">";
				
				//First, a link to the ID for editing:
				String sMaterialReturnID = Long.toString(rsMaterialReturns.getLong(SMTablematerialreturns.lid));
				String sMRLink = sMaterialReturnID;
				if (bAllowEditMaterialReturns){
					sMRLink = "<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMEditMaterialReturnEdit?"
						+ SMMaterialReturn.Paramlid + "=" + sMaterialReturnID
						+ "&CallingClass=" + SMUtilities.getFullClassName(this.toString())
						+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
						+ "\">" + sMaterialReturnID 
						+ "</A>"
					;
				}
				s += "<TD class = \" leftjustifiedcell \" >" + sMRLink + "</TD>";
				s += "<TD class = \" leftjustifiedcell \" >" + clsDateAndTimeConversions.resultsetDateTimeStringToString(
						rsMaterialReturns.getString(SMTablematerialreturns.datinitiated) + "</TD>");
				s += "<TD class = \" leftjustifiedcell \" >" + rsMaterialReturns.getString(SMTablematerialreturns.sinitiatedbyfullname) 
					+ "</TD>";
				
				//Next, a link to the work order ID for viewing:
				String sWorkOrderID = Long.toString(rsMaterialReturns.getLong(SMTablematerialreturns.iworkorderid));
				if (sWorkOrderID.compareToIgnoreCase("") != 0){
					SMWorkOrderHeader wo = new SMWorkOrderHeader();
					wo.setlid(sWorkOrderID);
					try {
						if (!wo.load(conn)){
							s += "Error loading work order #" + sWorkOrderID + " for material return - " + wo.getErrorMessages();
						}
					} catch (Exception e) {
						throw new Exception ("Error [1411479516] reading work order #" + sWorkOrderID + " for material return - " 
							+ wo.getErrorMessages());
					}
					String sViewWOLink = sWorkOrderID;
					if (wo.isWorkOrderPosted()){
						sViewWOLink = "<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMWorkOrderAction?"
							+ SMWorkOrderEdit.COMMAND_FLAG + "=" + SMWorkOrderEdit.PRINTRECEIPTCOMMAND_VALUE
							+ "&" + SMWorkOrderHeader.Paramlid + "=" + sWorkOrderID
							+ "&" + SMWorkOrderHeader.Paramstrimmedordernumber + "=" + sOrderNumber.trim()
							+ "&CallingClass=" + SMUtilities.getFullClassName(this.toString())
							+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
							+ "\">" + sWorkOrderID 
							+ "</A>"
						;
					}
					s += "<TD class = \" leftjustifiedcell \" >" + sViewWOLink + "</TD>";
				}else{
					s += "<TD class = \" leftjustifiedcell \" >" + "N/A" + "</TD>";
				}
				
				//To Be Returned?
				String sToBeReturned = "N";
				if (rsMaterialReturns.getInt(SMTablematerialreturns.itobereturned)==1){
					sToBeReturned = "Y";
				}
				s += "<TD class = \" leftjustifiedcell \" >" + sToBeReturned + "</TD>";
				
				//Vendor acct:
				s += "<TD class = \" leftjustifiedcell \" >" + rsMaterialReturns.getString(SMTablematerialreturns.svendoracct) + "</TD>";
				
				//Resolved:
				String sResolved = "N";
				if (rsMaterialReturns.getInt(SMTablematerialreturns.iresolved)==1){
					sResolved = "Y";
				}
				s += "<TD class = \" leftjustifiedcell \" >" + sResolved + "</TD>";
				s += "<TD class = \" leftjustifiedcell \" >" + rsMaterialReturns.getString(SMTablematerialreturns.sdescription) + "</TD>";
				
				s += "</TR>";
				bOddRow = ! bOddRow;
			}
			rsMaterialReturns.close();
			s += "</TABLE>";
		}catch(SQLException e){
			throw new Exception("Error [1411479517] opening material returns query: " + e.getMessage());
		}
		return s;
	}
	public String getDoingBuisnessAsDescription(ResultSet rsOrder, Connection conn){
		String sDBAName = "";
		try{
			String dbaSQL = "SELECT "+SMTabledoingbusinessasaddresses.sDescription+" FROM "
					+SMTabledoingbusinessasaddresses.TableName
					+" WHERE "+SMTabledoingbusinessasaddresses.lid+" = "+rsOrder.getInt(SMTableorderheaders.idoingbusinessasaddressid)+"";
			ResultSet rsDBA = clsDatabaseFunctions.openResultSet(dbaSQL, conn);
			while(rsDBA.next()){
				sDBAName = rsDBA.getString(SMTabledoingbusinessasaddresses.sDescription);
			}
			
		}catch(Exception e){
			
		}
		return sDBAName;
	}

	private String printFolowUpSalesLeads(
			String sOrderNumber, 
			Connection conn, 
			String sLinks, 
			String sUserID,
			boolean bAllowEditBids,
			String sDBID) throws Exception {
		String s = "";
		boolean bOddRow = true;
		String sBackgroundColor = "";
		try{
			String SQL = "SELECT * FROM " + SMTablebids.TableName
			+ " WHERE ("
			+ SMTablebids.screatedfromordernumber + " = '" + sOrderNumber.trim() + "'" 
			+ ")"
			+ " ORDER BY " + SMTablebids.datcreatedtime
			;
			ResultSet rsFollowUpSalesLead = clsDatabaseFunctions.openResultSet(SQL, conn);
			if(!rsFollowUpSalesLead.isBeforeFirst()){
				//throw new Exception("(No follow up sales leads created)");
			}
			s += "<BR><a name=\"FollowUpSalesLead\"><B><U>Follow Up Sales Leads</U></B></a><BR>";
			s += sLinks + "<BR>";
			s += "<TABLE BORDER=0 WIDTH=100%  cellspacing=0 cellpadding=1 style= \" background-color: " 
				+ FOLLOWUP_SALES_LEAD_TABLE_BG_COLOR + "; \" >";
			s += "<TR>";
			s += "<TD class = \" leftjustifiedheading \" >ID #</TD>";
			s += "<TD class = \" leftjustifiedheading \" >Date</TD>";
			s += "<TD class = \" leftjustifiedheading \" >Sales Person</TD>";
			s += "<TD class = \" leftjustifiedheading \" >Bill To Name</TD>";
			s += "<TD class = \" leftjustifiedheading \" >Ship To Name</TD>";
			s += "<TD class = \" leftjustifiedheading \" >View Folder</TD>";
			s += "</TR>";

			while(rsFollowUpSalesLead.next()){
				if(bOddRow){
					sBackgroundColor = "\"" + DARK_ROW_BG_COLOR + "\"";
				}else{
					sBackgroundColor = "\"" + LIGHT_ROW_BG_COLOR + "\"";
				}
				s += "<TR bgcolor =" + sBackgroundColor + ">";
				
				//First, a link to the ID for editing:
				String sFollowUpSalesLeadID = Long.toString(rsFollowUpSalesLead.getLong(SMTablebids.lid));
				String sSalesLeadLink = sFollowUpSalesLeadID;
				if (bAllowEditBids){
					sSalesLeadLink = "<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMEditBidEntry?"
					+ SMBidEntry.ParamID + "=" + sFollowUpSalesLeadID
					+ "&CallingClass=" + SMUtilities.getFullClassName(this.toString())
					+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
					+ "\">" + sFollowUpSalesLeadID 
					+ "</A>"
				;
				}
				//Sales Lead ID
				s += "<TD class = \" leftjustifiedcell \" >" + sSalesLeadLink + "</TD>";
				//Date initiated:
				s += "<TD class = \" leftjustifiedcell \" >" + clsDateAndTimeConversions.resultsetDateTimeStringToString(
						rsFollowUpSalesLead.getString(SMTablebids.datcreatedtime) + "</TD>");
				//Sales Person:
				s += "<TD class = \" leftjustifiedcell \" >" + rsFollowUpSalesLead.getString(SMTablebids.ssalespersoncode) 
					+ "</TD>";
				//Bill To Name
				s += "<TD class = \" leftjustifiedcell \" >" + rsFollowUpSalesLead.getString(SMTablebids.scustomername).toString() 
						+ "</TD>";
				//Ship To Name
				s += "<TD class = \" leftjustifiedcell \" >" + rsFollowUpSalesLead.getString(SMTablebids.sprojectname) 
						+ "</TD>";		
				//Folder
				String sGdoclink = rsFollowUpSalesLead.getString(SMTablebids.sgdoclink).trim();
				if(sGdoclink.compareToIgnoreCase("") != 0){
					sGdoclink = "<A href=\"" + sGdoclink + "\"> View Folder </A>";
				}else{
					sGdoclink = "None";
				}
				s += "<TD class = \" leftjustifiedcell \" >" + sGdoclink
						+ "</TD>";		
				s += "</TR>";
				bOddRow = ! bOddRow;
			}
			rsFollowUpSalesLead.close();
			s += "</TABLE>";
		}catch(SQLException e){
			throw new Exception("Error [1507228993] opening follow up sales lead query: " + e.getMessage());
		}
		return s;
	}
	private String printLaborBackCharges(
			String sOrderNumber, 
			Connection conn, 
			String sLinks, 
			String sUserID,
			boolean bAllowEditLaborBackCharge,
			String sDBID) throws Exception {
		String s = "";
		boolean bOddRow = true;
		String sBackgroundColor = "";
		try{
			String SQL = "SELECT * FROM " + SMTablelaborbackcharges.TableName
			+ " WHERE ("
			+ SMTablelaborbackcharges.strimmedordernumber + " = '" + sOrderNumber.trim() + "'" 
			+ ")"
			+ " ORDER BY " + SMTablelaborbackcharges.datinitiated
			;
			ResultSet rsLaborBackCharge = clsDatabaseFunctions.openResultSet(SQL, conn);
			s += "<BR><a name=\"LaborBackCharges\"><B><U>Labor Back Charges</U></B></a><BR>";
			s += sLinks + "<BR>";
			s += "<TABLE BORDER=0 WIDTH=100%  cellspacing=0 cellpadding=1 style= \" background-color: " 
				+ LABOR_BACKCHARGES_TABLE_BG_COLOR + "; \" >";
			s += "<TR>";
			s += "<TD class = \" leftjustifiedheading \" >ID #</TD>";
			s += "<TD class = \" leftjustifiedheading \" >Initiated</TD>";
			s += "<TD class = \" leftjustifiedheading \" >By</TD>";
			s += "<TD class = \" leftjustifiedheading \" >Credit Requested</TD>";
			s += "<TD class = \" leftjustifiedheading \" >Description</TD>";
			s += "</TR>";

			while(rsLaborBackCharge.next()){
				if(bOddRow){
					sBackgroundColor = "\"" + DARK_ROW_BG_COLOR + "\"";
				}else{
					sBackgroundColor = "\"" + LIGHT_ROW_BG_COLOR + "\"";
				}
				s += "<TR bgcolor =" + sBackgroundColor + ">";
				
				//First, a link to the ID for editing:
				String sLaborBackChargeID = Long.toString(rsLaborBackCharge.getLong(SMTablelaborbackcharges.lid));
				String sLBCLink = sLaborBackChargeID;
				if (bAllowEditLaborBackCharge){
				sLBCLink = "<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMLaborBackChargeEdit?"
					+ SMLaborBackCharge.Paramlid + "=" + sLaborBackChargeID
					+ "&CallingClass=" + SMUtilities.getFullClassName(this.toString())
					+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
					+ "\">" + sLaborBackChargeID 
					+ "</A>"
				;
				}
				s += "<TD class = \" leftjustifiedcell \" >" + sLBCLink + "</TD>";
				//Date initiated:
				s += "<TD class = \" leftjustifiedcell \" >" + clsDateAndTimeConversions.resultsetDateStringToString(
						rsLaborBackCharge.getString(SMTablelaborbackcharges.datinitiated) + "</TD>");
				//Initiated by:
				s += "<TD class = \" leftjustifiedcell \" >" + rsLaborBackCharge.getString(SMTablelaborbackcharges.sinitiatedbyfullname) 
					+ "</TD>";
				//Back Charge total
				s += "<TD class = \" leftjustifiedcell \" >" + rsLaborBackCharge.getBigDecimal(SMTablelaborbackcharges.bdcreditrequested).toString() 
						+ "</TD>";
				//Description
				s += "<TD class = \" leftjustifiedcell \" >" + rsLaborBackCharge.getString(SMTablelaborbackcharges.sdescription) + "</TD>";
				
				s += "</TR>";
				bOddRow = ! bOddRow;
			}
			rsLaborBackCharge.close();
			s += "</TABLE>";
		}catch(SQLException e){
			throw new Exception("Error [1411479517] opening labor back charge query: " + e.getMessage());
		}
		return s;
	}
	
	private String printProposal(
			Connection conn,
			String sDBID,
			String sUserID,
			String sLinks,
			String sProposalNumber,
			boolean bViewProposalsAllowed
		) throws Exception{
		String s = "";
		if (!bViewProposalsAllowed){
			return s;
		}
		s += "<BR><a name=\"Proposal\"><B><U>Proposal</U></B></a><BR>";
		s += sLinks + "<BR>";
		s += "<TABLE BORDER=1 cellspacing=0 cellpadding=1 style=\" title:ProposalTable; background-color: "
			+ PROPOSAL_TABLE_BG_COLOR + "; \" width=100% >\n";
		
		String SQL = "SELECT"
			+ " " + SMTableproposals.strimmedordernumber
			+ " FROM " + SMTableproposals.TableName
			+ " WHERE ("
				+ "(" + SMTableproposals.strimmedordernumber + " = '" + sProposalNumber + "')"
			+ ")"
		;
		boolean bProposalExists = false;
		try {
			ResultSet rsProposal = clsDatabaseFunctions.openResultSet(SQL, conn);
			if (rsProposal.next()){
				bProposalExists = true;
			}
			rsProposal.close();
		} catch (Exception e) {
			s += "</TABLE>";
			throw new Exception("Error [1402606399] checking for proposal - " + e.getMessage() + ".");
		}
		if (!bProposalExists){
			s += "</TABLE>";
			throw new Exception("(No proposal created)");
		}
		SMProposalForm proposal = new SMProposalForm();
		s += proposal.processReport(
			conn, 
			sProposalNumber, 
			sDBID, 
			sUserID, 
			1, 
			SMProposalForm.REQUEST_TYPE_PRINT, 
			getServletContext(), 
			false)
		;
		s += "</TABLE>";
		return s;
	}

	private String printBillingSummary(
		boolean bAllowProjectView, 
		boolean bAllowInvoiceView,
		SMOrderHeader order,
		String sOrderNum, 
		String sLinks, 
		BigDecimal bdTotalBilled, 
		Connection conn,
		String sDBID
		) throws Exception{
		String s = "";
		boolean bOddRow = true;
		String SQL = "";
		String sBackgroundColor = "";
		if (bAllowProjectView){
			try{
				SQL = "SELECT"
					+ " " + SMTableinvoicedetails.TableName + "." + SMTableinvoicedetails.sInvoiceNumber
					+ ", " + SMTableinvoiceheaders.TableName + "." + SMTableinvoiceheaders.datInvoiceDate
					+ ", " + SMTableinvoiceheaders.TableName + "." + SMTableinvoiceheaders.iTransactionType
					+ ", " + SMTableinvoiceheaders.TableName + "." + SMTableinvoiceheaders.sCreatedByFullName
					+ ", SUM(" + SMTableinvoicedetails.TableName + "." 
						+  SMTableinvoicedetails.dExtendedPriceAfterDiscount + ") AS EXTPRICE"
					+ " FROM " + SMTableinvoicedetails.TableName + " INNER JOIN "
					+ SMTableinvoiceheaders.TableName
					+ " ON " + SMTableinvoicedetails.TableName + "." + SMTableinvoicedetails.sInvoiceNumber
					+ " = " + SMTableinvoiceheaders.TableName + "." + SMTableinvoiceheaders.sInvoiceNumber
					+ " WHERE ("
						+ SMTableinvoiceheaders.TableName + "." + SMTableinvoiceheaders.strimmedordernumber
						+ " = '" + sOrderNum + "'"
					+ ")"
					+ " GROUP BY (" + SMTableinvoicedetails.TableName + "." + SMTableinvoicedetails.sInvoiceNumber 
					+ ")"
					;
	
				//System.out.println("In " + this.toString() + " Invoice SQL = " + SQL);
	
				ResultSet rsInvoices = clsDatabaseFunctions.openResultSet(SQL, conn);
				s += "<BR><a name=\"BILLINGSUMMARY\"><B><U>Billing Summary</U></B><BR>";
				s += sLinks + "<BR>";
				s += "<TABLE BORDER=0 WIDTH=100%  cellspacing=0 cellpadding=1 style= \" background-color: " 
						+ BILLING_SUMMARY_TABLE_BG_COLOR + "; \" >";
				s += "<TR>";
				s += "<TD class = \" leftjustifiedheading \" >Invoice date</TD>";
				s += "<TD class = \" leftjustifiedheading \" >Invoice number</TD>";
				s += "<TD class = \" leftjustifiedheading \" >Created by</TD>";
				s += "<TD class = \" leftjustifiedheading \" >Type</TD>";
				s += "<TD class = \" rightjustifiedheading \" >Amount</TD>";
				s += "</TR>";
	
				while(rsInvoices.next()){
					bdTotalBilled = bdTotalBilled.add(rsInvoices.getBigDecimal("EXTPRICE"));
					if(bOddRow){
						sBackgroundColor = "\"" + DARK_ROW_BG_COLOR + "\"";
					}else{
						sBackgroundColor = "\"" + LIGHT_ROW_BG_COLOR + "\"";
					}
					s += "<TR bgcolor =" + sBackgroundColor + ">";
					s += "<TD class = \" leftjustifiedcell \" >" 
						+ clsDateAndTimeConversions.utilDateToString(
							rsInvoices.getDate(
								SMTableinvoiceheaders.TableName + "." 
								+ SMTableinvoiceheaders.datInvoiceDate), "M/d/yyyy") + "</TD>";
					
					String sInvoiceNumber = rsInvoices.getString(SMTableinvoicedetails.TableName + "." 
						+ SMTableinvoicedetails.sInvoiceNumber).trim();
	
					if (bAllowInvoiceView){
						s += "<TD class = \" leftjustifiedcell \" >"
							+ "<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "" 
							+ SMUtilities.lnViewInvoice(sDBID, sInvoiceNumber)
							+ "\">"
							+ sInvoiceNumber
							+ "</A>"
							+ "</TD>";
					}else{
						s += "<TD class = \" leftjustifiedcell \" >" 
							+ sInvoiceNumber + "</TD>";
					}

					//Created by
					String sCreatedByFullName = rsInvoices.getString(SMTableinvoiceheaders.sCreatedByFullName);
					if (sCreatedByFullName == null){
						sCreatedByFullName = "";
					}
					s += "<TD class = \" leftjustifiedcell \" >" + sCreatedByFullName + "</TD>";
					
					//Type
					if (rsInvoices.getInt(SMTableinvoiceheaders.TableName + "." 
							+ SMTableinvoiceheaders.iTransactionType) == SMTableinvoiceheaders.TYPE_INVOICE){
						s += "<TD class = \" leftjustifiedcell \" >INVOICE</TD>";
					}else{
						s += "<TD class = \" leftjustifiedcell \" >CREDIT</TD>";
					}
					
					//EXTPRICE
					s += "<TD class = \" rightjustifiedcell \" >" 
							+ clsManageBigDecimals.doubleTo2DecimalSTDFormat(rsInvoices.getDouble("EXTPRICE")) 
							+ "</TD>";
	
					s += "</TR>";
					bOddRow = ! bOddRow;
				}
				rsInvoices.close();
				//Show the total amount billed:
				if (bOddRow){
					sBackgroundColor = "\"" + DARK_ROW_BG_COLOR + "\"";
				}else{
					sBackgroundColor = "\"" + LIGHT_ROW_BG_COLOR + "\"";
				}
				s += "<TR bgcolor =" + sBackgroundColor + ">";
				s += "<TD COLSPAN=4 class = \" rightjustifiedcell \" ><B>AMOUNT INVOICED TO DATE:</B></TD>"
					+ "<TD class = \" rightjustifiedcell \" >" 
						+ clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(bdTotalBilled)
					+ "</TD>"
					+ "</TR>"
				;
				bOddRow = ! bOddRow;
			}catch(SQLException e){
				throw new Exception("Error [1411072077] opening invoice query: " + e.getMessage());
			}
			boolean bCalculationFailed = false;
			try {
				order.calculateBillingTotals(conn, sOrderNum);
			} catch (Exception e) {
				s += "<TR>";
				s += "<TD ALIGN=RIGHT ><FONT SIZE=2; COLOR=RED>" 
						+ "<B>ERROR CALCULATING ORDER TOTALS - " + e.getMessage() + "</B>" + "</FONT></TD>";
				s += "</TR>";
				bCalculationFailed = true;
			}

			if (bCalculationFailed == false){
				if (bOddRow){
					sBackgroundColor = "\"" + DARK_ROW_BG_COLOR + "\"";
				}else{
					sBackgroundColor = "\"" + LIGHT_ROW_BG_COLOR + "\"";
				}
				//Show the total amount remaining to be billed
				s += "<TR bgcolor =" + sBackgroundColor + ">";
				s += 
					"<TD COLSPAN=4 class = \" rightjustifiedcell \" >"
					+ "<B>AMOUNT ON ORDER DETAILS CURRENTLY BEING SHIPPED:</B></TD>"
					+ "<TD class = \" rightjustifiedcell \" >"
					+ clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(order.getCalculatedOrderTotals_TotalAmtCurrentlyShipped())
					+ "</TD>"
					+ "</TR>"
				;
				bOddRow = !bOddRow;
				if (bOddRow){
					sBackgroundColor = "\"" + DARK_ROW_BG_COLOR + "\"";
				}else{
					sBackgroundColor = "\"" + LIGHT_ROW_BG_COLOR + "\"";
				}
				s += "<TR bgcolor =" + sBackgroundColor + ">";
				s += 
					"<TD COLSPAN=4  class = \" rightjustifiedcell \" >"
					+ "<B>AMOUNT ON ORDER DETAILS REMAINING UNSHIPPED:</B></TD>"
					+ "<TD class = \" rightjustifiedcell \" ><B>"
					+ clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(order.getCalculatedOrderTotals_TotalAmtStillOnOrder())
					+ "</B></TD>"
					+ "</TR>"
				;
				bOddRow = !bOddRow;
				//TOTAL ORDER AMOUNT (AMOUNT INVOICED TO DATE PLUS AMOUNT ON ORDER DETAILS REMAINING TO BE INVOICED):
				if (bOddRow){
					sBackgroundColor = "\"" + DARK_ROW_BG_COLOR + "\"";
				}else{
					sBackgroundColor = "\"" + LIGHT_ROW_BG_COLOR + "\"";
				}
				s += "<TR bgcolor =" + sBackgroundColor + ">";
				s += "<TD class = \" rightjustifiedcell \"  COLSPAN=4>" 
						+ "<B>TOTAL ORDER AMT (amt invoiced to date PLUS amt shipped PLUS amt left on order):</B>" + "</TD>";
				s += "<TD class = \" rightjustifiedcell \" <B>"
					+ clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(
						order.getCalculatedOrderTotals_TotalBilled().add(
						order.getCalculatedOrderTotals_TotalAmtCurrentlyShipped()).add(
						order.getCalculatedOrderTotals_TotalAmtStillOnOrder()))
					+ "</B></TD>";
				s += "</TR>";
				s += "</TABLE>";
				if (order.getCalculatedOrderTotals_OriginalContractAmount().compareTo(BigDecimal.ZERO) != 0){
					if (order.getCalculatedOrderTotals_RemainingAmtDifference().compareTo(BigDecimal.ZERO) > 0){
						s += "<FONT COLOR=RED><B><I>NOTE:</I> The Total Order Amount in the Billing Summary is "
							+ clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(order.getCalculatedOrderTotals_RemainingAmtDifference().abs())
							+ " <I>LESS</I> than the Total Contract Amount in the Change Order log.</B></FONT><BR>"
						;
					}
					if (order.getCalculatedOrderTotals_RemainingAmtDifference().compareTo(BigDecimal.ZERO) < 0){
						s += "<FONT COLOR=RED><B><I>NOTE:</I> The Total Order Amount in the Billing Summary is "
							+ clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(order.getCalculatedOrderTotals_RemainingAmtDifference().abs())
							+ " <I>MORE</I> than the Total Contract Amount in the Change Order log.</B></FONT><BR>"
						;
						//pwOut.println("</TR>");
					}
				}
			}
		}
		return s;
	}
	private String printCriticalDates(
			String sOrderNum, 
			String sUserID, 
			String sLinks,
			Connection conn,
			boolean bAllowProjectView,
			String sDBID) throws Exception{
		String s = "";
		String SQL = "";
		boolean bOddRow = true;
		String sBackgroundColor = "";
		if (!bAllowProjectView){
			return s;
		}
		try{
			SQL = "SELECT * FROM " + SMTablecriticaldates.TableName
			+ " WHERE ("
			+ SMTablecriticaldates.sdocnumber + " = '" + sOrderNum.trim() + "'" 
			+ ")"
			+ " ORDER BY " + SMTablecriticaldates.sCriticalDate
			;
			ResultSet rsCriticalDates = clsDatabaseFunctions.openResultSet(SQL, conn);
			s += "<BR><a name=\"CriticalDates\"><B><U>Critical Dates</U></B></a>"
				+ "&nbsp;&nbsp;"
				+ "<FONT SIZE=2><a href=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMCriticalDateEdit?" +
				SMCriticalDateEntry.ParamID + "=-1" +
				"&" + SMCriticalDateEntry.ParamCriticalDate + "=" + clsDateAndTimeConversions.utilDateToString(new Date(System.currentTimeMillis()), "M/d/yyyy") +
				"&" + SMCriticalDateEntry.ParamDocNumber + "=" + sOrderNum.trim() +
				"&" + SMCriticalDateEntry.ParamResponsibleUserID + "=" + sUserID +
				"&" + SMCriticalDateEntry.ParamAssignedbyUserID + "=" + sUserID +
				"&" + SMCriticalDateEntry.ParamTimeStampAudit + "=" + clsDateAndTimeConversions.utilDateToString(new Date(System.currentTimeMillis()), "yyyy-MM-dd hh:mm:ss") +
				"&" + SMCriticalDateEntry.ParamiType + "=" + SMTablecriticaldates.SALES_ORDER_RECORD_TYPE +
				//"&OriginalURL=" + sCurrentURL +
				"&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID + 
				"\">" + "Add new critical date" + "</A><BR>"
			;
			s += sLinks + "<BR>";
			s += "<TABLE BORDER=0 WIDTH=100%  cellspacing=0 cellpadding=1 style= \" background-color: " 
					+ CRITICAL_DATE_TABLE_BG_COLOR + "; \" >";
			s += "<TR>";
			s += "<TD class = \" centerjustifiedheading \" >ID</TD>";
			s += "<TD class = \" leftjustifiedheading \" >Date</TD>";
			s += "<TD class = \" leftjustifiedheading \" >Resolved?</TD>";
			s += "<TD class = \" leftjustifiedheading \" >Responsible</TD>";
			s += "<TD class = \" leftjustifiedheading \" >Assigned&nbsp;by</TD>";
			s += "<TD class = \" leftjustifiedheading \" >Comments</TD>";
			s += "</TR>";

			bOddRow = true;
			while(rsCriticalDates.next()){
				if(bOddRow){
					sBackgroundColor = "\"" + LIGHT_GREY_BG_COLOR + "\"";
				}else{
					sBackgroundColor = "\"" + LIGHT_ROW_BG_COLOR + "\"";
				}
				s += "<TR   bgcolor =" + sBackgroundColor + ">";
				s += "<TD  VALIGN=TOP class = \" centerjustifiedcell \"style = \" vertical-align: text-top; \" ><A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) 
					+ "smcontrolpanel.SMCriticalDateEdit?id=" 
					+ rsCriticalDates.getInt((SMTablecriticaldates.TableName + "." + SMTablecriticaldates.sId).replace("`", "")) +
					"&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID + "\">" 
					+ rsCriticalDates.getInt((SMTablecriticaldates.TableName + "." + SMTablecriticaldates.sId).replace("`", "")) 
					+ "</A></TD>";

				s += "<TD VALIGN=TOP class = \" leftjustifiedcell \"style = \" vertical-align: text-top; \" >" + clsDateAndTimeConversions.utilDateToString(
						rsCriticalDates.getDate(SMTablecriticaldates.sCriticalDate.replace("`", "")),"M/d/yyyy") + "</TD>";
				if(rsCriticalDates.getInt(SMTablecriticaldates.sResolvedFlag.replace("`", "")) == 0){
					s += "<TD class = \" leftjustifiedcell \"style = \" vertical-align: text-top; \" >" + "No" + "</TD>";
				}else{
					s += "<TD class = \" leftjustifiedcell \"style = \" vertical-align: text-top; \" >" + "Yes" + "</TD>";
				}
				s += "<TD VALIGN=TOP class = \" leftjustifiedcell \"style = \" vertical-align: text-top; \" >" + rsCriticalDates.getString(
						SMTablecriticaldates.sresponsibleuserfullname.replace("`", "")) + "</TD>";
				
				s += "<TD VALIGN=TOP class = \" leftjustifiedcell \"style = \" vertical-align: text-top; \" >" + rsCriticalDates.getString(
					SMTablecriticaldates.sassignedbyuserfullname.replace("`", "")) + "</TD>";

				//if(sCriticalDateComments.compareToIgnoreCase("") !=0){
				s += "<TD VALIGN=TOP class = \" leftjustifiedcell \"style = \" vertical-align: text-top; \" >" + rsCriticalDates.getString(SMTablecriticaldates.sComments.replace("`", "")) 
					+ "</TD>";
				s += "</TR>";
				bOddRow = ! bOddRow;
			}
			rsCriticalDates.close();
			s += "</TABLE>";

			s += "<BR><FONT SIZE=2><a href=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMCriticalDateEdit?" +
				SMCriticalDateEntry.ParamID + "=-1" +
				"&" + SMCriticalDateEntry.ParamCriticalDate + "=" + clsDateAndTimeConversions.utilDateToString(new Date(System.currentTimeMillis()), "M/d/yyyy") +
				"&" + SMCriticalDateEntry.ParamDocNumber + "=" + sOrderNum.trim() +
				"&" + SMCriticalDateEntry.ParamResponsibleUserID + "=" + sUserID +
				"&" + SMCriticalDateEntry.ParamAssignedbyUserID + "=" + sUserID +
				"&" + SMCriticalDateEntry.ParamTimeStampAudit + "=" + clsDateAndTimeConversions.utilDateToString(new Date(System.currentTimeMillis()), "yyyy-MM-dd hh:mm:ss") +
				"&" + SMCriticalDateEntry.ParamiType + "=" + SMTablecriticaldates.SALES_ORDER_RECORD_TYPE +
				//"&OriginalURL=" + sCurrentURL +
				"&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID + 
				"\">" + "Add new critical date" + "</A>"; 
		}catch(SQLException e){
			throw new Exception("Error [1411067899] opening critical dates query: " + e.getMessage());
		}
		//This link is here to allow links back to the critical dates to go to the bottom of the list instead of the top
		s += "<BR><a name=\"CriticalDatesFooter\"><B><U>(End Critical Dates)</U></B></a><BR>";
		return s;
	}
	private String printTaxCalculation(
		String sOrderNumber, 
		SMOrderHeader order, 
		Connection conn,
		String sLinks,
		boolean bAllowOrderDetailViewing) throws Exception{
		boolean bOddRow = true;
		String sBackgroundColor = "";
		String s = "";
		
		if (!bAllowOrderDetailViewing){
			return s;
		}
		//Tax information here:
		SMOrderHeader ord = new SMOrderHeader();
		ord.setM_strimmedordernumber(sOrderNumber.trim());
		if (!ord.load(conn)){
			throw new Exception("Error [1411066628] loading order to calculate taxes - " + order.getErrorMessages());
		}
		s += "<BR><a name=\"TaxCalculation\"><B><U>Sales&nbsp;Tax&nbsp;Calculation</U></B></a>&nbsp;";
		s += "<FONT SIZE=2><B>NOTE:</B> Sales Tax is only calculated on items that currently have a 'Qty Shipped'.<BR>";
		s += sLinks + "<BR>";
		s += "<TABLE BORDER=1 WIDTH=100%  cellspacing=0 cellpadding=1 style= \" background-color: " 
				+ TAX_CALCULATION_TABLE_BG_COLOR + "; \" >";
		s += "<TR>";
		s += "<TD class = \" centerjustifiedheading \" >Line</TD>";
		s += "<TD class = \" leftjustifiedheading \" >Item</TD>";
		s += "<TD class = \" rightjustifiedheading \" >Qty&nbsp;Shipped</TD>";
		s += "<TD class = \" centerjustifiedheading \" >Taxable?</TD>";
		s += "<TD class = \" rightjustifiedheading \" >Extended&nbsp;Price Before&nbsp;Discount</TD>";
		s += "<TD class = \" rightjustifiedheading \" >Extended&nbsp;Price After&nbsp;Discount</TD>";
		s += "<TD class = \" rightjustifiedheading \" >Sales&nbsp;Tax&nbsp;Amount</TD>";
		s += "</TR>";
		SMSalesOrderTaxCalculator sotc;
		try {
			sotc = new SMSalesOrderTaxCalculator(
				ord.salesTaxRate(conn), 
				new BigDecimal(ord.getM_dPrePostingInvoiceDiscountAmount().replace(",","")));
			;
		} catch (SQLException e) {
			throw new Exception ("Error [1411066627] calculating taxes - " + e.getMessage());
		}
		for (int i = 0; i < ord.get_iOrderDetailCount(); i++){
			SMOrderDetail detail = ord.getOrderDetail(i);
			try {
				sotc.addLine(
						new BigDecimal(detail.getM_dExtendedOrderPrice().replace(",", "")), 
						Integer.parseInt(detail.getM_iTaxable()), 
						new BigDecimal(detail.getM_dQtyShipped().replace(",", "")),
						detail.getM_sItemNumber());
			} catch (NumberFormatException e) {
				throw new Exception("Number format error [1411066629] loading line to calculate taxes - " + detail.getM_sItemNumber() 
					+ " - detail.getM_iTaxable() = " + detail.getM_iTaxable() + " - " + e.getMessage());
			} catch (Exception e) {
				throw new Exception("General error [1411066630] getting loading line to calculate taxes - " + e.getMessage());
			}
		}
		try {
			sotc.calculateSalesTax();
		} catch (Exception e) {
			throw new Exception("Error [1411066631] calculating taxes - " + e.getMessage());

		}
		
		for (int i = 0; i < sotc.getLineCount(); i++){
			String sTaxable = "Y";
			if (sotc.getIsLineTaxable(i) == 0){
				sTaxable = "N";
			}
			if(bOddRow){
				sBackgroundColor = "\"" + DARK_ROW_BG_COLOR + "\"";
			}else{
				sBackgroundColor = "\"" + LIGHT_ROW_BG_COLOR + "\"";
			}
			s += "<TR bgcolor =" + sBackgroundColor + ">";
			s += "<TD class = \" rightjustifiedcell \">" + Integer.toString(i + 1) + "</TD>";
			s += "<TD class = \" leftjustifiedcell \">" + sotc.getItem(i) + "</TD>";
			s += "<TD class = \" rightjustifiedcell \">" + sotc.getQtyShipped(i).toString() + "</TD>";
			s += "<TD class = \" centerjustifiedcell \">" + sTaxable + "</TD>";
			s += "<TD class = \" rightjustifiedcell \">" + clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(sotc.getLineExtendedPriceBeforeDiscount(i)) + "</TD>";
			s += "<TD class = \" rightjustifiedcell \">" + clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(sotc.getLineExtendedPriceAfterDiscount(i)) + "</TD>";
			s += "<TD class = \" rightjustifiedcell \">" + clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(sotc.getSalesTaxAmountPerLine(i)) + "</TD>";
			s += "</TR>";
			bOddRow = !bOddRow;
		}
		if(bOddRow){
			sBackgroundColor = "\"" + DARK_ROW_BG_COLOR + "\"";
		}else{
			sBackgroundColor = "\"" + LIGHT_ROW_BG_COLOR + "\"";
		}
		s += "<TR bgcolor =" + sBackgroundColor + ">"
			+ "<TD COLSPAN = 6 class = \" rightjustifiedcell \" ><B>TOTAL SALES TAX:</B></TD>"
			+ "<TD class = \" rightjustifiedcell \" >" + sotc.getTotalSalesTax().toString() + "</TD>"
			+ "</TR>"
		;
		s += "</TABLE>";
		return s;

	}

	private String sStyleScripts(){
		String s = "";
		String sBorderSize = "0";
		String sRowHeight = "22px";
		s += "<style type=\"text/css\">\n";
		
		//Layout table:
		s +=
			"table.basic {"
			+ "border-width: " + sBorderSize + "px; "
			+ "border-spacing: 2px; "
			//+ "border-style: outset; "
			+ "border-style: none; "
			+ "border-color: white; "
			+ "border-collapse: separate; "
			+ "width: 100%; "
			+ "font-size: " + "small" + "; "
			+ "font-family : Arial; "
			+ "color: black; "
			//+ "background-color: white; "
			+ "}"
			+ "\n"
			;
		
		/*
		//This is the def for a label field:
		s +=
			"td.fieldlabel {"
			+ "height: " + sRowHeight + "; "
			//+ "border-width: " + sBorderSize + "px; "
			//+ "padding: 2px; "
			//+ "border-style: none; "
			//+ "border-color: white; "
			//+ "vertical-align: text-middle;"
			//+ "background-color: black; "
			+ "font-weight: bold; "
			+ "text-align: right; "
			//+ "color: black; "
			//+ "height: 50px; "
			+ "}"
			+ "\n"
			;
		*/
		//This is the def for a table cell, left justified:
		s +=
			"td.leftjustifiedcell {"
			+ "height: " + sRowHeight + "; "
			+ "border: 0px solid; "
			+ "padding: 2px; "
			+ "border-color: " + CELL_BORDER_COLOR + "; "
			+ "vertical-align: bottom;"
			+ "font-family : Arial; "
			+ "font-weight: normal; "
			+ "font-size: small; "
			+ "text-align: left; "
			+ "color: black; "
			//+ "height: 50px; "
			+ "}"
			+ "\n"
			;

		//This is the def for a table cell, right justified:
		s +=
			"td.rightjustifiedcell {"
			+ "height: " + sRowHeight + "; "
			+ "border: 0px solid; "
			+ "padding: 2px; "
			+ "border-color: " + CELL_BORDER_COLOR + "; "
			+ "vertical-align: bottom;"
			+ "font-family : Arial; "
			+ "font-weight: normal; "
			+ "font-size: small; "
			+ "text-align: right; "
			+ "color: black; "
			//+ "height: 50px; "
			+ "}"
			+ "\n"
			;
		
		//This is the def for a table cell, center justified:
		s +=
			"td.centerjustifiedcell {"
			+ "height: " + sRowHeight + "; "
			+ "border: 0px solid; "
			+ "padding: 2px; "
			+ "border-color: " + CELL_BORDER_COLOR + "; "
			+ "vertical-align: bottom;"
			+ "font-family : Arial; "
			+ "font-weight: normal; "
			+ "font-size: small; "
			+ "text-align: center; "
			+ "color: black; "
			//+ "height: 50px; "
			+ "}"
			+ "\n"
			;
		
		//This is the def for a left-aligned heading on a table:
		s +=
			"td.leftjustifiedheading {"
			+ "border: 1px solid; "
			+ "bordercolor: 000; "
			+ "padding: 2px; "
			+ "font-family : Arial; "
			+ "font-size: small; "
			+ "font-weight: bold; "
			+ "text-align: left; "
			+ "vertical-align:bottom; "
			+ "}"
			+ "\n"
			;
		
		//This is the def for a right-aligned heading on a table:
		s +=
			"td.rightjustifiedheading {"
			+ "border: 1px solid; "
			+ "bordercolor: 000; "
			+ "padding: 2px; "
			+ "font-family : Arial; "
			+ "font-size: small; "
			+ "font-weight: bold; "
			+ "text-align: right; "
			+ "vertical-align:bottom; "
			+ "}"
			+ "\n"
			;

		//This is the def for a center-aligned heading on a table:
		s +=
			"td.centerjustifiedheading {"
			+ "border: 1px solid; "
			+ "bordercolor: 000; "
			+ "padding: 2px; "
			+ "font-family : Arial; "
			+ "font-size: small; "
			+ "font-weight: bold; "
			+ "text-align: center; "
			+ "vertical-align:bottom; "
			+ "}"
			+ "\n"
			;
		
		s += "</style>"
			+ "\n"
			;

		return s;
	}
	
}
