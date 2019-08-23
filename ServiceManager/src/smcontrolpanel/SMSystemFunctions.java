package smcontrolpanel;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import javax.servlet.ServletContext;

import SMClasses.SMLogEntry;
import SMClasses.SMReminders;
import SMDataDefinition.SMModuleListing;
import SMDataDefinition.SMTableglexternalcompanypulls;
import SMDataDefinition.SMTableicitems;
import SMDataDefinition.SMTablesecurityfunctions;
import SMDataDefinition.SMTablesecuritygroupfunctions;
import SMDataDefinition.SMTablesecurityusergroups;
import SMDataDefinition.SMTablessalarmsequences;
import SMDataDefinition.SMTablessalarmsequenceusers;
import SMDataDefinition.SMTablessdeviceevents;
import SMDataDefinition.SMTablessdevices;
import SMDataDefinition.SMTablessdeviceusers;
import SMDataDefinition.SMTablessuserevents;
import SMDataDefinition.SMTablesystemlog;
import SMDataDefinition.SMTableusers;
import ServletUtilities.clsServletUtilities;
import ServletUtilities.clsDatabaseFunctions;
import smic.ICItem;

public class SMSystemFunctions extends java.lang.Object{

	//Security functions:
	public static long SMCreateCreditNotes = 1L;
	public static long SMCreateInvoices = 2L;
	public static long SMPreInvoice = 3L;
	public static long SMInvoiceAuditList = 4L;
	public static long ICPrintLabelScanSheets = 5L;
	public static long SMPrintInvoice = 6L;
	public static long GLEditBatches = 7L;
	public static long SMEditTaxCertificates = 8L;
	public static long ICEditPhysicalInventory = 9L;
	public static long SMCanceledJobsReport = 10L;
	public static long SMMonthlyBilling = 11L;
	public static long SMMonthlySales = 12L;
	public static long SMOrderHistory = 13L;
	public static long SMProductivityReport = 14L;
	public static long SMOpenOrdersReport = 15L;
	public static long SMSignAnySalespersonsProposals = 16L;
	public static long SMCreateGDriveOrderFolders = 17L;
	public static long ICOnHandByDescriptionSelection = 18L;
	public static long APEditVendors = 19L;
	public static long ICPOReceivingReport = 20L;
	public static long ICEditPurchaseOrders = 21L;
	public static long ICPrintPurchaseOrders = 22L;
	public static long SMEditProposalPhraseGroups = 23L;
	public static long SMRecalibrateOrderAndInvoiceCounters = 24L;
	public static long ICItemPricing = 25L;
	public static long SMReceiveOrderCancellationNotifications = 26L;
	public static long SMEditWorkOrders = 27L;
	public static long SMEditServiceTypes = 28L;
	public static long SMUnpostWorkOrders = 29L;
	public static long SMConfigureWorkOrders = 30L;
	public static long SMViewPricingOnWorkOrders = 31L;
	public static long FAEditClasses = 32L;
	public static long SMManagePublicQueries = 33L;
	public static long ICAssignPO = 34L;
	public static long BKEditBanks = 35L;
	public static long SMEditDetailSheets = 36L;
	public static long SMEditOrders = 37L;
	public static long SMAverageMUpertruckday = 38L;
	public static long SMCriticaldatesreport = 40L;
	public static long SMListOrdersForScheduling = 41L;
	public static long ICListUnusedPOs=42L;
	public static long ICListItemsReceived = 43L;
	public static long ICEditReceipts = 44L;
	public static long SMSalesEffortCheck = 45L;
	public static long SMWarrantystatusreport = 46L;
	public static long SMViewOrderDetailInformation = 47L;
	public static long BKEditStatements = 48L;
	public static long ICClearPostedBatches = 49L;
	public static long SMViewMechanicsOwnTruckSchedule = 50L;
	public static long SMPurgeData = 51L;
	public static long ICFindItems = 52L;
	public static long SMRecordGeocode = 53L;
	public static long SMCustomPartsonHandNotonSalesOrders = 54L;
	public static long BKImportBankEntries = 55L;
	public static long SMAutoRefreshSchedule = 56L;
	public static long SMListCustomersOnHoldWithNoOptions = 57L;
	public static long SMDirectItemEntry = 58L;
	public static long BKClearStatements = 59L;
	public static long SMUnbilledOrdersReport = 60L;
	public static long SMCreateQuotes = 407L;
	public static long SMInitiateMaterialReturns = 416L;
	public static long SMManageuserpasswords = 1000L;
	public static long SMChangeyourpassword = 1002L;
	public static long SMManageSecurityGroups = 1003L;
	public static long SMListSecurityLevels = 1004L;
	public static long SMEditSystemOptions = 1005L;
	public static long SMEditUsers = 1006L;
	public static long SMSystemstatistics = 1007L;
	public static long SMEditCompanyProfile = 1008L;
	public static long SMEditMechanics = 1009L;
	public static long SMEditSalespersons = 1010L;
	public static long SMEditSiteLocations = 1011L;
	public static long SMIndividualAverageMUpertruckday = 1012L;
	public static long SMEditConveniencePhrases = 1013L;
	public static long SMEditDefaultItemCategories = 1014L;
	public static long SMEditMaterialReturns = 1015L;
	public static long SMEditLaborTypes = 1016L;
	public static long SMEditLocations = 1017L;
	public static long SMEditOrderSources = 1018L;
	public static long FAReCreateGLSelection = 1019L;
	public static long ICRecreateAPInvoiceExport = 1020L;
	public static long SMEditTaxes = 1021L;
	public static long SMEditWorkPerformedCodes = 1022L;
	public static long SMTaxByCategoryReport = 1023L;
	public static long ARAccountsReceivable = 1024L;
	public static long SMCustomerCallLogEntryForm = 1025L;
	public static long SMCustomerCallList = 1026L;
	public static long SMSalesContactReport = 1027L;
	public static long AREditAccountSets = 1028L;
	public static long AREditPriceListCodes = 1029L;
	public static long AREditTerms = 1030L;
	public static long AREditCustomerGroups = 1031L;
	public static long AREditCustomers = 1032L;
	public static long AREditCustomerShipToLocations = 1033L;
	public static long AREditBatches = 1034L;
	public static long ARClearpostedbatches = 1035L;
	public static long ARClearfullypaidtransactions = 1036L;
	public static long ARYearEndProcessing = 1037L;
	public static long ARRenumberMergeCustomers = 1038L;
	public static long ARCustomerActivity = 1039L;
	public static long ARCustomerStatistics = 1040L;
	public static long ARPostingJournal = 1041L;
	public static long ARAgingReport = 1042L;
	public static long ARActivityReport = 1043L;
	public static long ARMiscCashReport = 1044L;
	public static long ARPrintStatements = 1045L;
	public static long GLEditChartOfAccounts = 1046L;
	public static long SMViewOrderInformation = 1047L;
	public static long SMViewProjectInformation = 1048L;
	public static long ARReceiveNewCustomerNotification = 1049L;	
	public static long SMEditProjectTypes = 1050L;
	public static long SMBidFollowUpReport = 1051L;
	public static long SMPendingBidsReport = 1052L;
	public static long SMEditBids = 1053L;
	public static long SMGlobalConnectionPoolStatus = 1054L;
	public static long SMOrderSourceListing = 1055L;
	public static long SMViewOrderHeaderInformation = 1056L;
	public static long ARListInactiveCustomers = 1057L;
	public static long ARSetInactiveCustomers = 1058L;
	public static long ARDeleteInactiveCustomers = 1059L;
	public static long ARClearMonthlyStatistics = 1060L;
	public static long ARViewChronologicalLog = 1061L;
	public static long ARListCustomersOnHold = 1062L;
	public static long ARResetPostingInProcessFlag = 1063L;
	public static long SMUnbilledOrdersReportForIndividual = 1064L;
	public static long SMUpdateSecurityFunctions = 1065L;
	public static long ICInventoryControl = 1066L;
	public static long ICEditItems = 1067L;
	public static long ICEditAccountSets = 1068L;
	public static long ICEditCategories = 1069L;
	public static long ICDisplayItemInformation = 1070L;
	public static long ICItemValuationReport = 1071L;
	public static long ICResetPostingInProcessFlag = 1072L;
	public static long ICEditBatches = 1073L;
	public static long SMEditSalesContacts = 1074L;
	public static long SMSalesLeadReport = 1075L;
	public static long SMEditSalesGroups = 1076L;
	public static long SMCloneOrder = 1077L;
	public static long SMJobCostDailyReport = 1078L;
	public static long SMJobCostSummaryReport = 1079L;
	public static long ICEditICOptions = 1080L;
	public static long ICPrintUPCLabels = 1081L;
	public static long SMViewSystemConfiguration = 1082L;
	public static long SMViewOrderDocuments = 1083L;
	public static long SMViewBidDocuments = 1084L;
	public static long ICTransactionHistory = 1085L;
	public static long ICEditItemPricing = 1086L;
	public static long SMEditCriticalDate = 1087L;
	public static long SMIndividualMonthlySales = 1088L;
	public static long SMCreateGDriveSalesLeadFolders = 1089L;
	public static long ARDisplayCustomerInformation = 1090L;
	public static long ICEditVendorItems = 1091L;
	public static long APEditVendorTerms = 1092L;
	public static long ICEnterInvoices = 1093L;
	public static long APReceiveNewVendorNotification = 1094L;
	public static long ICQuantitiesOnHandReport = 1095L;
	public static long ICPrintReceivingLabels = 1096L;
	public static long ICSetInactiveItems = 1097L;
	public static long ICDeleteInactiveItems = 1098L;
	public static long AREditAROptions = 1099L;
	public static long SMListQuickLinks = 1100L;
	public static long SMQuerySelector = 1101L;
	public static long SMDisplayDataDefs = 1102L;
	public static long SMEditBidProductTypes = 1103L;
	public static long SMEditLabelPrinters = 1104L;
	public static long ICUpdateItemPrices = 1105L;
	public static long ICClearTransactions = 1106L;
	public static long ICClearStatistics = 1107L;
	public static long SMManageOrders = 1108L;
	public static long SMEditChangeOrders = 1109L;
	public static long SMCreateGDrivePOFolders = 1110L;
	public static long SMPrintInstallationTicket = 1111L;
	public static long SMPrintServiceTicket = 1112L;
	public static long ARDeleteCustomers = 1113L;
	public static long SMPrintJobFolderLabel = 1114L;
	public static long ARAutoCreateCallSheets = 1115L;
	public static long AREditCallSheets = 1116L;
	public static long ARPrintCallSheets = 1117L;
	public static long SMViewTruckSchedules = 1118L;
	public static long ICItemNumberMatchUp = 1119L;
	public static long ICUnderStockedItemReport = 1120L;
	public static long FAFixedAssets = 1121L;
	public static long FAManageAssets = 1122L;
	public static long FAEnterAdjustments = 1123L;
	public static long FASettings = 1124L;
	public static long FAPeriodEndProcessing = 1125L;
	public static long FAClearTransactionHistory = 1126L;
	public static long FAResetYearToDateDepreciation = 1127L;
	public static long FAAssetList = 1128L;
	public static long FATransactionReport = 1129L;
	public static long FAEditDepreciationType = 1130L;
	public static long SMEditLeftPreviousSiteTime = 1131L;
	public static long SMEditArrivedAtCurrentSiteTime = 1132L;
	public static long SMEditLeftCurrentSiteTime = 1133L;
	public static long SMEditArrivedAtNextSiteTime = 1134L;
	public static long SMChangeCustomerOnOrders = 1135L;
	public static long BKBankFunctions = 1136L;
	public static long SMCreateGDriveARFolders = 1137L;
	public static long SMReceiveWorkOrderPostNotification= 1138L;
	public static long SMManageDeliveryTickets = 1139L;
	public static long FAEditLocation = 1140L;
	public static long SMExecuteSQL = 1141L;
	public static long SMImportData = 1142L;
	public static long ICFindItemsSortedByMostUsed = 1143L;
	public static long ICViewPODocuments = 1144L;
	public static long SMCreateItemsFromOrderDetails = 1145L;
	public static long SMCreatePOsFromOrderDetails = 1146L;
	public static long SMEditProposals = 1147L;
	public static long SMApproveProposals = 1148L;
	public static long SMEditProposalTerms = 1149L;
	public static long SMEditProposalPhrases = 1150L;
	public static long SMLoadWageScaleData = 1151L;
	public static long SMWageScaleReport = 1152L;
	public static long ICEditPOShipVias = 1153L;
	public static long SMEditSalespersonSignatures = 1154L;
	public static long ICConfirmPurchaseOrder = 1155L;
	public static long SMEditDeliveryTicketTerms = 1156L;
	public static long APConvertACCPACData = 1157L;
	public static long SMEditLaborBackCharges = 1158L;
	public static long APAccountsPayable = 1159L;
	public static long APEditAPOptions = 1160L;
	public static long APEditAccountSets = 1161L;
	public static long APEditVendorGroups = 1162L;
	public static long APEdit1099CPRSCodes = 1163L;
	public static long APEditDistributionCodes = 1164L;
	public static long APEditVendorRemitToLocations = 1165L;
	public static long GLGeneralLedger = 1166L;
	public static long SMCreateGDriveVendorFolders = 1167L;
	public static long SMEditReminders = 1168L;
	public static long SMEditPersonalReminders = 1169L;
	public static long SMSalesTaxReport = 1170L;
	public static long GLEditCostCenters = 1171L;
	public static long APDisplayVendorInformation = 1172L;
	public static long SMDisplayMostUsedItemsOnWorkOrder = 1173L;
	public static long SMEditLocationOnWorkOrderDetail = 1174L;
	public static long ASAlarmFunctions = 1175L;
	public static long ASDeviceStatus = 1176L;
	public static long ASEditControllers = 1177L;
	public static long ASEditDevices = 1178L;
	public static long ASEditAlarmSequences = 1179L;
	public static long ASAuthorizeDeviceUsers = 1180L;
	public static long ASActivateDevices = 1181L;
	public static long ASActivateAlarmSequences = 1182L;
	public static long ASListUserEventsByDate = 1183L;
	public static long ASAuthorizeAlarmSequenceUsers = 1184L;
	public static long SMCreateGDriveWorkOrderFolders = 1185L;
	public static long SMViewWorkOrderDocuments = 1186L;
	public static long ASConfigureControllers = 1187L;
	public static long ASControllerDiagnostics = 1188L;
	public static long ASListDeviceEventsByDate = 1189L;
	public static long ASListAllSSEventsByDate = 1190L;
	public static long ASEditSSOptions = 1191L;
	public static long ASListAuthorizedDeviceUsers = 1192L;
	public static long ASListAuthorizedAlarmSequenceUsers = 1193L;
	public static long SMViewAppointmentCalendar = 1194L;
	public static long SMEditAppointmentCalendar = 1195L;
	public static long SMEditAppointmentGroups = 1196L;
	public static long ICBulkTransfers = 1197L;
	public static long SMSendInvoices = 1198L;
	public static long ASEditEventSchedules = 1199L;
	public static long ASListOverridenDeviceEvents = 1200L;
	public static long ASActivateSelectedSequences = 1201L;
	public static long APEditBatches = 1202L;
	public static long APClearPostedAndDeletedBatches = 1203L;
	public static long APResetPostingInProcessFlag = 1204L;
	public static long ASListUsersPermissions = 1205L;
	public static long APControlPayments = 1206L;
	public static long APChangeOrMergeVendorAccounts = 1207L;
	public static long APAgedPayables = 1208L;
	public static long APViewTransactionInformation = 1209L;
	public static long APVendorTransactionsReport = 1210L;
	public static long APEditCheckForms = 1211L;
	public static long APPrintChecks = 1212L;
	public static long BKUpdateNextCheckNumber = 1213L;
	public static long GLQueryFinancialInformation = 1214L;
	public static long APClearFullyPaidTransactions = 1215L;
	public static long APDeleteInactiveVendorsQuery = 1216L;
	public static long APQueryCheckRunLog = 1217L;
	public static long APReverseChecks = 1218L;
	public static long SMEditUsersCustomLinks = 1219L;
	public static long APBatchPostingJournal = 1220L;
	public static long SMEditDoingBusinessAsAddresses = 1221L;
	public static long SMZeroWorkOrderItemPrices = 1222L;
	public static long GLEditGLOptions = 1223L;
	public static long GLConvertACCPACData = 1224L;
	public static long GLEditAccountSegments = 1225L;
	public static long GLEditAcctSegmentValues = 1226L;
	public static long GLEditAccountStructures = 1227L;
	public static long GLEditAccountGroups = 1228L;
	public static long GLEditFiscalPeriods = 1229L;
	public static long GLTrialBalance = 1230L;
	public static long APViewInvoicesOnHold = 1231L;
	public static long SMEditServerSettingsFile = 1232L;
	public static long SMDisplayLoggingOperations = 1233L;
	public static long GLTransactionListing = 1234L;
	public static long GLClearPostedAndDeletedBatches = 1235L;
	public static long GLClearTransactions = 1236L;
	public static long GLClearFiscalData = 1237L;
	public static long FAEditOptions = 1238L;
	public static long GLImportBatches = 1239L;
	public static long GLResetPostingInProcessFlag = 1240;
	public static long GLPullExternalDataIntoConsolidation = 1241;
	public static long GLManageExternalCompanies = 1242;
	public static long GLListPreviousExternalCompanyPulls = 1243;
	public static long GLCloseFiscalYear = 1244;
	
	private static ArrayList <String>arrFunctions;
	private static ArrayList <Long>arrFunctionIDs;
	private static ArrayList <String>arrFunctionLinks;
	private static ArrayList <String>arrFunctionDescriptions;
	private static ArrayList <Long>arrFunctionModuleLevel;

	SMSystemFunctions(String sDBID){
		arrFunctions = new ArrayList<String>(0);
		arrFunctionIDs = new ArrayList<Long>(0);
		arrFunctionLinks = new ArrayList<String>(0);
		arrFunctionDescriptions = new ArrayList<String>(0);
		arrFunctionModuleLevel = new ArrayList<Long>(0);
		populateSecurityFunctions(sDBID);
	}
	private static void populateSecurityFunctions(String sDBID){

		//Module levels:
		//If the company has ANY of the module levels in his license package, then a function that references ANY of this modules should appear in his program.
		//So if the company has just the BASE and ALARM SYSTEM modules, then ANY function with the BASE module number OR the ALARM SYSTEM module number would appear on his menus (pending permissions.)

		arrFunctions.add("SM Create Credit Notes"); 
		arrFunctionIDs.add(SMCreateCreditNotes); 
		arrFunctionLinks.add("smcontrolpanel.SMCreateCreditNoteCriteriaSelection"); 
		arrFunctionDescriptions.add("Required to create credit notes against invoices.");
		arrFunctionModuleLevel.add(SMModuleListing.MODULE_ORDERENTRY);

		arrFunctions.add("SM Create Invoices"); 
		arrFunctionIDs.add(SMCreateInvoices); 
		arrFunctionLinks.add("smcontrolpanel.SMCreateMultipleInvoicesSelection"); 
		arrFunctionDescriptions.add("Required to create invoices, either individually OR on the 'Create (multiple) invoices screen.");
		arrFunctionModuleLevel.add(SMModuleListing.MODULE_ORDERENTRY);

		arrFunctions.add("SM Print Invoice"); 
		arrFunctionIDs.add(SMPrintInvoice); 
		arrFunctionLinks.add("smcontrolpanel.SMPrintInvoiceCriteriaSelection"); 
		arrFunctionDescriptions.add("Required to display an actual invoice on the screen.");
		arrFunctionModuleLevel.add(SMModuleListing.MODULE_ORDERENTRY);

		arrFunctions.add("IC Physical Inventory"); 
		arrFunctionIDs.add(ICEditPhysicalInventory); 
		arrFunctionLinks.add("smic.ICListPhysicalInventories"); 
		arrFunctionDescriptions.add("Required to do ANY physical inventory processing.");
		arrFunctionModuleLevel.add(SMModuleListing.MODULE_INVENTORYCONTROL);

		arrFunctions.add("SM Canceled Jobs"); 
		arrFunctionIDs.add(SMCanceledJobsReport); 
		arrFunctionLinks.add("smcontrolpanel.SMCanceledJobsReportCriteriaSelection"); 
		arrFunctionDescriptions.add("Required to print the 'Canceled jobs' report.");
		arrFunctionModuleLevel.add(SMModuleListing.MODULE_ORDERENTRY);

		arrFunctions.add("SM Monthly Billing"); 
		arrFunctionIDs.add(SMMonthlyBilling); 
		arrFunctionLinks.add("smcontrolpanel.SMMonthlyBillingReportSelection"); 
		arrFunctionDescriptions.add("Required to print the 'Monthly Billing Report'.");
		arrFunctionModuleLevel.add(SMModuleListing.MODULE_ORDERENTRY);

		arrFunctions.add("SM Monthly Sales"); 
		arrFunctionIDs.add(SMMonthlySales); 
		arrFunctionLinks.add("smcontrolpanel.SMMonthlySalesReportSelection"); 
		arrFunctionDescriptions.add("Required to print the 'Monthly Sales Report'.");
		arrFunctionModuleLevel.add(SMModuleListing.MODULE_ORDERENTRY);

		arrFunctions.add("SM Monthly Sales for Individual"); 
		arrFunctionIDs.add(SMIndividualMonthlySales);
		arrFunctionLinks.add(
				"smcontrolpanel.SMMonthlySalesReportSelection?" 
						+ SMMonthlySalesReportSelection.PRINTINDIVIDUAL_PARAMETER 
						+ "=" + SMMonthlySalesReportSelection.PRINTINDIVIDUAL_VALUE_YES);
		arrFunctionDescriptions.add("Required to print the 'Monthly Sales For Individual' report.");
		arrFunctionModuleLevel.add(SMModuleListing.MODULE_ORDERENTRY);

		arrFunctions.add("SM Productivity Report"); 
		arrFunctionIDs.add(SMProductivityReport); 
		arrFunctionLinks.add("smcontrolpanel.SMProductivityReportCriteriaSelection"); 
		arrFunctionDescriptions.add("Required to print the 'Productivity' report.");
		arrFunctionModuleLevel.add(SMModuleListing.MODULE_ORDERENTRY);

		arrFunctions.add("SM Open Orders Report");
		arrFunctionIDs.add(SMOpenOrdersReport); 
		arrFunctionLinks.add("smcontrolpanel.SMOpenOrdersReportGenerate"); 
		arrFunctionDescriptions.add("Required to print the 'Open Orders' report.");
		arrFunctionModuleLevel.add(SMModuleListing.MODULE_ORDERENTRY);

		arrFunctions.add("AP Edit Vendors"); 
		arrFunctionIDs.add(APEditVendors); 
		arrFunctionLinks.add("smap.APEditVendorSelection"); 
		arrFunctionDescriptions.add("Required to edit any AP vendors (inventory suppliers).");
		arrFunctionModuleLevel.add(SMModuleListing.MODULE_ACCOUNTSPAYABLE + SMModuleListing.MODULE_INVENTORYCONTROL);

		arrFunctions.add("IC PO Receiving Report"); 
		arrFunctionIDs.add(ICPOReceivingReport); 
		arrFunctionLinks.add("smic.ICPOReceivingReportSelection"); 
		arrFunctionDescriptions.add("Required to print the 'PO Receiving Report'.");
		arrFunctionModuleLevel.add(SMModuleListing.MODULE_INVENTORYCONTROL);

		arrFunctions.add("IC Edit Purchase Orders"); 
		arrFunctionIDs.add(ICEditPurchaseOrders); 
		arrFunctionLinks.add("smic.ICEditPOSelection"); 
		arrFunctionDescriptions.add("Required to do any PO editing.");
		arrFunctionModuleLevel.add(SMModuleListing.MODULE_INVENTORYCONTROL);

		arrFunctions.add("IC Print Purchase Orders"); 
		arrFunctionIDs.add(ICPrintPurchaseOrders); 
		arrFunctionLinks.add("smic.ICPrintPOSelection"); 
		arrFunctionDescriptions.add("Required to display any actual Purchase Orders on the screen.");
		arrFunctionModuleLevel.add(SMModuleListing.MODULE_INVENTORYCONTROL);

		arrFunctions.add("IC Edit Item Prices"); 
		arrFunctionIDs.add(ICItemPricing); 
		arrFunctionLinks.add("smic.ICEditItemPriceSelection"); 
		arrFunctionDescriptions.add("Required to edit prices for items in the various price lists.");
		arrFunctionModuleLevel.add(SMModuleListing.MODULE_INVENTORYCONTROL + SMModuleListing.MODULE_ORDERENTRY);

		arrFunctions.add("SM Average MU per Truck Day"); 
		arrFunctionIDs.add(SMAverageMUpertruckday); 
		arrFunctionLinks.add("smcontrolpanel.SMAverageMUReportSelection"); 
		arrFunctionDescriptions.add("Required to run the Average Mark Up Per Truck Day report.");
		arrFunctionModuleLevel.add(SMModuleListing.MODULE_ORDERENTRY);

		arrFunctions.add("SM Average MU per Truck Day for Individual");
		arrFunctionIDs.add(SMIndividualAverageMUpertruckday);
		arrFunctionLinks.add(
				"smcontrolpanel.SMAverageMUReportSelection?" 
						+ SMAverageMUReportSelection.PRINTINDIVIDUAL_PARAMETER 
						+ "=" + SMAverageMUReportSelection.PRINTINDIVIDUAL_VALUE_YES); 
		arrFunctionDescriptions.add("Required to run the Average Mark Up Per Truck Day report for an individual salesperson.");
		arrFunctionModuleLevel.add(SMModuleListing.MODULE_ORDERENTRY);

		arrFunctions.add("SM Critical Dates Report"); 
		arrFunctionIDs.add(SMCriticaldatesreport); 
		arrFunctionLinks.add("smcontrolpanel.SMCriticalDateReportCriteriaSelection"); 
		arrFunctionDescriptions.add("Required to run the Critical Dates report.");
		arrFunctionModuleLevel.add(SMModuleListing.MODULE_ORDERENTRY);

		arrFunctions.add("SM Warranty Status Report"); 
		arrFunctionIDs.add(SMWarrantystatusreport); 
		arrFunctionLinks.add("smcontrolpanel.SMWarrantyStatusReportSelect"); 
		arrFunctionDescriptions.add("Required to run the Warranty Status reports.");
		arrFunctionModuleLevel.add(SMModuleListing.MODULE_ORDERENTRY);

		arrFunctions.add("SM Purge Data"); 
		arrFunctionIDs.add(SMPurgeData); 
		arrFunctionLinks.add("smcontrolpanel.SMPurgeDataSelection"); 
		arrFunctionDescriptions.add("Required to run the Purge Data functions.");
		arrFunctionModuleLevel.add(SMModuleListing.MODULE_BASE);

		arrFunctions.add("SM Custom Parts On Hand Not On Sales Orders"); 
		arrFunctionIDs.add(SMCustomPartsonHandNotonSalesOrders); 
		arrFunctionLinks.add("smcontrolpanel.SMCustomPartsOnHandNotOnSalesOrdersCriteriaSelection"); 
		arrFunctionDescriptions.add("Required to run the Custom Parts On Hand report.");
		arrFunctionModuleLevel.add(SMModuleListing.MODULE_ORDERENTRY + SMModuleListing.MODULE_INVENTORYCONTROL);

		arrFunctions.add("AR Auto Create Call Sheets"); 
		arrFunctionIDs.add(ARAutoCreateCallSheets); 
		arrFunctionLinks.add("smar.ARAutoCreateCallSheetsSelection"); 
		arrFunctionDescriptions.add("Required to automatically create call sheets in Accounts Receivable.");
		arrFunctionModuleLevel.add(SMModuleListing.MODULE_ACCOUNTSRECEIVABLE);

		arrFunctions.add("SM Manage user passwords"); 
		arrFunctionIDs.add(SMManageuserpasswords); 
		arrFunctionLinks.add("smcontrolpanel.SMManagePasswords"); 
		arrFunctionDescriptions.add("Required to manage user passwords");
		arrFunctionModuleLevel.add(SMModuleListing.MODULE_BASE);

		arrFunctions.add("SM Change your password"); 
		arrFunctionIDs.add(SMChangeyourpassword); 
		arrFunctionLinks.add("smcontrolpanel.SMChangeUserPassword"); 
		arrFunctionDescriptions.add("Required for a user to change their own password.");
		arrFunctionModuleLevel.add(SMModuleListing.MODULE_BASE);

		arrFunctions.add("SM Manage Security Groups"); 
		arrFunctionIDs.add(SMManageSecurityGroups); 
		arrFunctionLinks.add("smcontrolpanel.SMManageSecurityGroups"); 
		arrFunctionDescriptions.add("Required to access the Manage Security Groups function, which sets and revokes selected permissions for users/security groups.");
		arrFunctionModuleLevel.add(SMModuleListing.MODULE_BASE);

		arrFunctions.add(" SM List Security Levels"); 
		arrFunctionIDs.add(SMListSecurityLevels); 
		arrFunctionLinks.add("smcontrolpanel.SMListSecurityLevels"); 
		arrFunctionDescriptions.add("Required to list the security levels, which show which users/ have which permissions, and through which groups those permissions were granted.");
		arrFunctionModuleLevel.add(SMModuleListing.MODULE_BASE);

		arrFunctions.add("SM Edit System Options"); 
		arrFunctionIDs.add(SMEditSystemOptions); 
		arrFunctionLinks.add("smcontrolpanel.SMEditSMOptions"); 
		arrFunctionDescriptions.add("Required to access the overall system options to administer the program.");
		arrFunctionModuleLevel.add(SMModuleListing.MODULE_BASE);

		arrFunctions.add("SM Edit Users"); 
		arrFunctionIDs.add(SMEditUsers); 
		arrFunctionLinks.add("smcontrolpanel.SMEditUsersSelection"); 
		arrFunctionDescriptions.add("Required to edit and maintain user information.");
		arrFunctionModuleLevel.add(SMModuleListing.MODULE_BASE);

		arrFunctions.add("SM System Statistics"); 
		arrFunctionIDs.add(SMSystemstatistics); 
		arrFunctionLinks.add("smcontrolpanel.SMStatistics"); 
		arrFunctionDescriptions.add("Required to access the system statistics, which include things like number of orders, number of invoices, etc.");
		arrFunctionModuleLevel.add(SMModuleListing.MODULE_BASE);

		arrFunctions.add("SM Edit Company Profile"); 
		arrFunctionIDs.add(SMEditCompanyProfile); 
		arrFunctionLinks.add("smcontrolpanel.SMEditCompanyProfile"); 
		arrFunctionDescriptions.add("Required to edit the company profile.");
		arrFunctionModuleLevel.add(SMModuleListing.MODULE_BASE);

		arrFunctions.add("SM Edit Mechanics"); 
		arrFunctionIDs.add(SMEditMechanics); 
		arrFunctionLinks.add("smcontrolpanel.SMEditMechanics"); 
		arrFunctionDescriptions.add("Required to edit and maintain the mechanics.");
		arrFunctionModuleLevel.add(SMModuleListing.MODULE_ORDERENTRY);

		arrFunctions.add("SM Edit Salespersons"); 
		arrFunctionIDs.add(SMEditSalespersons); 
		arrFunctionLinks.add("smcontrolpanel.SMEditSalesperson"); 
		arrFunctionDescriptions.add("Required to edit the salespersons.");
		arrFunctionModuleLevel.add(SMModuleListing.MODULE_ORDERENTRY + SMModuleListing.MODULE_ACCOUNTSRECEIVABLE);

		arrFunctions.add("SM Edit Site Locations"); 
		arrFunctionIDs.add(SMEditSiteLocations); 
		arrFunctionLinks.add("smcontrolpanel.SMEditSiteLocations"); 
		arrFunctionDescriptions.add("Required to edit the site locations, which are specific areas within a ship to location where work might be done, for example, a particular dock or door at a large customer site.");
		arrFunctionModuleLevel.add(SMModuleListing.MODULE_ORDERENTRY + SMModuleListing.MODULE_ACCOUNTSRECEIVABLE);

		arrFunctions.add("SM Edit Convenience Phrases"); 
		arrFunctionIDs.add(SMEditConveniencePhrases); 
		arrFunctionLinks.add("smcontrolpanel.SMEditConveniencePhrases"); 
		arrFunctionDescriptions.add("Required to edit the convenience phrases which can be inserted on the order header.");
		arrFunctionModuleLevel.add(SMModuleListing.MODULE_ORDERENTRY);

		arrFunctions.add("SM Edit Default Item Categories"); 
		arrFunctionIDs.add(SMEditDefaultItemCategories); 
		arrFunctionLinks.add("smcontrolpanel.SMEditDefaultItemCategories"); 
		arrFunctionDescriptions.add("Required to edit the default item categories which are determined by the service type and default warehouse location on orders. "
				+ "This function also sets the initial, 'default' item on an order based on the same parameters.");
		arrFunctionModuleLevel.add(SMModuleListing.MODULE_INVENTORYCONTROL + SMModuleListing.MODULE_ORDERENTRY);

		arrFunctions.add("SM Edit Labor Types"); 
		arrFunctionIDs.add(SMEditLaborTypes); 
		arrFunctionLinks.add("smcontrolpanel.SMEditLaborTypes"); 
		arrFunctionDescriptions.add("Required to maintain the 'labor types', which are used in calculating material and labor billing values in the 'Direct Entry' function.");
		arrFunctionModuleLevel.add(SMModuleListing.MODULE_ORDERENTRY);

		arrFunctions.add("SM Edit Inventory Locations"); 
		arrFunctionIDs.add(SMEditLocations); 
		arrFunctionLinks.add("smcontrolpanel.SMEditLocations"); 
		arrFunctionDescriptions.add("Required to edit the warehouse locations.");
		arrFunctionModuleLevel.add(SMModuleListing.MODULE_INVENTORYCONTROL + SMModuleListing.MODULE_ORDERENTRY);

		arrFunctions.add("SM Edit Marketing Sources"); 
		arrFunctionIDs.add(SMEditOrderSources); 
		arrFunctionLinks.add("smcontrolpanel.SMEditOrderSources"); 
		arrFunctionDescriptions.add("Required to edit the order sources, which are the possible 'lead sources' (for example 'Internet advertising', or 'Referral from previous customer',"
				+ " or 'General contractor bid') from which an order was initiated.");
		arrFunctionModuleLevel.add(SMModuleListing.MODULE_ORDERENTRY);

		arrFunctions.add("SM Edit Taxes"); 
		arrFunctionIDs.add(SMEditTaxes); 
		arrFunctionLinks.add("smcontrolpanel.SMEditTaxSelection"); 
		arrFunctionDescriptions.add("Required to edit the tax tables, including jurisdiction names, rates, etc.");
		arrFunctionModuleLevel.add(SMModuleListing.MODULE_ORDERENTRY + SMModuleListing.MODULE_ACCOUNTSRECEIVABLE);

		arrFunctions.add("SM Edit Work Performed Codes"); 
		arrFunctionIDs.add(SMEditWorkPerformedCodes); 
		arrFunctionLinks.add("smcontrolpanel.SMEditWorkPerformedCodes"); 
		arrFunctionDescriptions.add("Required to edit the 'Work performed codes' which appear on work orders and can also be inserted into the invoice detail comments on the 'Edit order detail' screen.");
		arrFunctionModuleLevel.add(SMModuleListing.MODULE_ORDERENTRY);

		arrFunctions.add("SM Tax By Category Report"); 
		arrFunctionIDs.add(SMTaxByCategoryReport); 
		arrFunctionLinks.add("smcontrolpanel.TaxByCategoryReportSelect"); 
		arrFunctionDescriptions.add("Required to run the 'Tax By Category' report.");
		arrFunctionModuleLevel.add(SMModuleListing.MODULE_ORDERENTRY);

		arrFunctions.add("AR Accounts Receivable Menu"); 
		arrFunctionIDs.add(ARAccountsReceivable); 
		arrFunctionLinks.add("smar.ARMainMenu");
		arrFunctionDescriptions.add("Required to access the main Accounts Receivable menu - without this permission, a user will not see that menu on the main menu screen.");
		arrFunctionModuleLevel.add(SMModuleListing.MODULE_ORDERENTRY + SMModuleListing.MODULE_ACCOUNTSRECEIVABLE);

		arrFunctions.add("SM Customer Call Log Entry Form"); 
		arrFunctionIDs.add(SMCustomerCallLogEntryForm); 
		arrFunctionLinks.add("smcontrolpanel.CustomerCallLogEntry"); 
		arrFunctionDescriptions.add("Required to access the Customer Call Log Entry form, which is used to record initial calls from customers.");
		arrFunctionModuleLevel.add(SMModuleListing.MODULE_SALESMANAGEMENT);

		arrFunctions.add("SM Customer Call List"); 
		arrFunctionIDs.add(SMCustomerCallList); 
		arrFunctionLinks.add("smcontrolpanel.CustomerCallLogListCriteriaSelection"); 
		arrFunctionDescriptions.add("Required to run the Customer Call Log report, listing customer calls.");
		arrFunctionModuleLevel.add(SMModuleListing.MODULE_SALESMANAGEMENT);
/*
		arrFunctions.add("SM Sales Contact Report"); 
		arrFunctionIDs.add(SMSalesContactReport); 
		arrFunctionLinks.add("smcontrolpanel.SMSalesContactListCriteriaSelection"); 
		arrFunctionDescriptions.add("Required to run the Sales Contact report.");
		arrFunctionModuleLevel.add(SMModuleListing.MODULE_SALESMANAGEMENT);
*/
		arrFunctions.add("AR Edit Account Sets"); 
		arrFunctionIDs.add(AREditAccountSets); 
		arrFunctionLinks.add("smar.AREditAccountSets"); 
		arrFunctionDescriptions.add("Required to edit the Accounts Receivable 'Account Sets' which are groupings of the control accounts for A/R.");
		arrFunctionModuleLevel.add(SMModuleListing.MODULE_ACCOUNTSRECEIVABLE + SMModuleListing.MODULE_ORDERENTRY);

		arrFunctions.add("AR Edit Price List Codes"); 
		arrFunctionIDs.add(AREditPriceListCodes); 
		arrFunctionLinks.add("smar.AREditPriceListCodes"); 
		arrFunctionDescriptions.add("Required to edit the Price List codes, which are used to set up different price lists for the master inventory.  Each customer can then be associated with a 'custom' price list"
				+ " and so have their own set of prices established.");
		arrFunctionModuleLevel.add(SMModuleListing.MODULE_INVENTORYCONTROL + SMModuleListing.MODULE_ACCOUNTSRECEIVABLE + SMModuleListing.MODULE_ORDERENTRY);

		arrFunctions.add("AR Edit Terms"); 
		arrFunctionIDs.add(AREditTerms); 
		arrFunctionLinks.add("smar.AREditTerms"); 
		arrFunctionDescriptions.add("Required to edit the list of customer terms in Accounts Receivable.");
		arrFunctionModuleLevel.add(SMModuleListing.MODULE_ACCOUNTSRECEIVABLE + SMModuleListing.MODULE_ORDERENTRY);

		arrFunctions.add("AR Edit Customer Groups"); 
		arrFunctionIDs.add(AREditCustomerGroups); 
		arrFunctionLinks.add("smar.AREditCustomerGroups"); 
		arrFunctionDescriptions.add("Required to edit and maintain the 'Customer Groups' which make it possible to group customers into larger sets.");
		arrFunctionModuleLevel.add(SMModuleListing.MODULE_ACCOUNTSRECEIVABLE);

		arrFunctions.add("AR Edit Customers"); 
		arrFunctionIDs.add(AREditCustomers); 
		arrFunctionLinks.add("smar.AREditCustomers"); 
		arrFunctionDescriptions.add("Required to edit and maintain customer master information.");
		arrFunctionModuleLevel.add(SMModuleListing.MODULE_ACCOUNTSRECEIVABLE + SMModuleListing.MODULE_ORDERENTRY);

		arrFunctions.add("AR Edit Customer Ship To Locations"); 
		arrFunctionIDs.add(AREditCustomerShipToLocations); 
		arrFunctionLinks.add("smar.AREditCustomerShipTos"); 
		arrFunctionDescriptions.add("Required to edit and maintain ship-to locations for customers in Accounts Receivable.");
		arrFunctionModuleLevel.add(SMModuleListing.MODULE_ACCOUNTSRECEIVABLE + SMModuleListing.MODULE_ORDERENTRY);

		arrFunctions.add("AR Edit Batches"); 
		arrFunctionIDs.add(AREditBatches); 
		arrFunctionLinks.add("smar.AREditBatches"); 
		arrFunctionDescriptions.add("Required to edit and maintain Accounts Receivable batches.");
		arrFunctionModuleLevel.add(SMModuleListing.MODULE_ACCOUNTSRECEIVABLE);

		arrFunctions.add("AR Clear posted batches"); 
		arrFunctionIDs.add(ARClearpostedbatches); 
		arrFunctionLinks.add("smar.ARClearPostedBatchesSelection"); 
		arrFunctionDescriptions.add("Required to clear Accounts Receivable batches from history.");
		arrFunctionModuleLevel.add(SMModuleListing.MODULE_ACCOUNTSRECEIVABLE);

		arrFunctions.add("AR Clear fully paid transactions"); 
		arrFunctionIDs.add(ARClearfullypaidtransactions); 
		arrFunctionLinks.add("smar.ARClearPaidTransactionsSelection"); 
		arrFunctionDescriptions.add("Required to clear fully paid Accounts Receivable transactions.");
		arrFunctionModuleLevel.add(SMModuleListing.MODULE_ACCOUNTSRECEIVABLE);

		arrFunctions.add("AR Year End Processing"); 
		arrFunctionIDs.add(ARYearEndProcessing); 
		arrFunctionLinks.add("smar.ARYearEndProcessingSelection"); 
		arrFunctionDescriptions.add("Required to run the year end process in Accounts Receivable.");
		arrFunctionModuleLevel.add(SMModuleListing.MODULE_ACCOUNTSRECEIVABLE);

		arrFunctions.add("AR Renumber/Merge Customers"); 
		arrFunctionIDs.add(ARRenumberMergeCustomers); 
		arrFunctionLinks.add("smar.ARSelectForCustomerNumberChange"); 
		arrFunctionDescriptions.add("Required to run the Renumber/Merge function in Accounts Receivable.");
		arrFunctionModuleLevel.add(SMModuleListing.MODULE_ACCOUNTSRECEIVABLE + SMModuleListing.MODULE_ORDERENTRY);

		arrFunctions.add("AR Customer Activity"); 
		arrFunctionIDs.add(ARCustomerActivity); 
		arrFunctionLinks.add("smar.ARActivityInquiry");
		arrFunctionDescriptions.add("Required to view Accounts Receivable customer activity.");
		arrFunctionModuleLevel.add(SMModuleListing.MODULE_ACCOUNTSRECEIVABLE);

		arrFunctions.add("AR Customer Statistics"); 
		arrFunctionIDs.add(ARCustomerStatistics); 
		arrFunctionLinks.add("smar.ARSelectCustomerStatistics"); 
		arrFunctionDescriptions.add("Required to view customer statistics in Accounts Receivable.");
		arrFunctionModuleLevel.add(SMModuleListing.MODULE_ACCOUNTSRECEIVABLE);

		arrFunctions.add("AR Posting Journal"); 
		arrFunctionIDs.add(ARPostingJournal); 
		arrFunctionLinks.add("smar.ARSelectForPostingJournal"); 
		arrFunctionDescriptions.add("Required to run the Accounts Receivable posting journal.");
		arrFunctionModuleLevel.add(SMModuleListing.MODULE_ACCOUNTSRECEIVABLE);

		arrFunctions.add("AR Aging Report"); 
		arrFunctionIDs.add(ARAgingReport); 
		arrFunctionLinks.add("smar.ARAgedTrialBalanceReport"); 
		arrFunctionDescriptions.add("Required to run the Accounts Receivable Aging report.");
		arrFunctionModuleLevel.add(SMModuleListing.MODULE_ACCOUNTSRECEIVABLE);

		arrFunctions.add("AR Activity Report"); 
		arrFunctionIDs.add(ARActivityReport); 
		arrFunctionLinks.add("smar.ARSelectForActivityReport"); 
		arrFunctionDescriptions.add("Required to run the Accounts Receivable Activity report.");
		arrFunctionModuleLevel.add(SMModuleListing.MODULE_ACCOUNTSRECEIVABLE);

		arrFunctions.add("AR Misc Cash Report"); 
		arrFunctionIDs.add(ARMiscCashReport); 
		arrFunctionLinks.add("smar.ARSelectForMiscCashReport"); 
		arrFunctionDescriptions.add("Required to run the Accounts Receivable Miscellaneous Cash report.");
		arrFunctionModuleLevel.add(SMModuleListing.MODULE_ACCOUNTSRECEIVABLE);

		arrFunctions.add("AR Print Statements"); 
		arrFunctionIDs.add(ARPrintStatements); 
		arrFunctionLinks.add("smar.ARPrintStatementsSelection"); 
		arrFunctionDescriptions.add("Required to print customer statements in Accounts Receivable.");
		arrFunctionModuleLevel.add(SMModuleListing.MODULE_ACCOUNTSRECEIVABLE);

		arrFunctions.add("GL Edit Chart Of Accounts"); 
		arrFunctionIDs.add(GLEditChartOfAccounts); 
		arrFunctionLinks.add("smgl.GLEditAccounts"); 
		arrFunctionDescriptions.add("Required to edit and maintain General Ledger accounts.");
		arrFunctionModuleLevel.add(SMModuleListing.MODULE_ACCOUNTSPAYABLE + SMModuleListing.MODULE_ACCOUNTSRECEIVABLE + SMModuleListing.MODULE_INVENTORYCONTROL + SMModuleListing.MODULE_ORDERENTRY);

		arrFunctions.add("SM View Order Information"); 
		arrFunctionIDs.add(SMViewOrderInformation); 
		arrFunctionLinks.add("smcontrolpanel.SMDisplayOrderSelect"); 
		arrFunctionDescriptions.add("Required to view the 'View Order Information' screen."
				+ "This permission is used in dozens of places to create a link where the order number appears on different screens.");
		arrFunctionModuleLevel.add(SMModuleListing.MODULE_ORDERENTRY);

		arrFunctions.add("AR Receive New Customer Notification"); 
		arrFunctionIDs.add(ARReceiveNewCustomerNotification); 
		arrFunctionLinks.add(""); 
		arrFunctionDescriptions.add("Granting this permission will cause users/groups to receive a email notification whenever a new customer is set up in the 'Edit Customers' function."
				+ "  Email settings have to be correct in the 'SM Edit Options' function for this to work properly, and the user has to be set up with an email address in the 'Edit Users' function.");
		arrFunctionModuleLevel.add(SMModuleListing.MODULE_ACCOUNTSRECEIVABLE);

		arrFunctions.add("SM Edit Project Types"); 
		arrFunctionIDs.add(SMEditProjectTypes); 
		arrFunctionLinks.add("smcontrolpanel.SMEditProjectTypes"); 
		arrFunctionDescriptions.add("Required to edit and maintain the list of various 'Project Types'");
		arrFunctionModuleLevel.add(SMModuleListing.MODULE_ORDERENTRY);

		arrFunctions.add("SM " + SMBidEntry.ParamObjectName + " Follow-Up Report"); 
		arrFunctionIDs.add(SMBidFollowUpReport); 
		arrFunctionLinks.add("smcontrolpanel.SMBidFollowUpCriteriaSelection"); 
		arrFunctionDescriptions.add("Required to run the " + SMBidEntry.ParamObjectName + " Follow Up report.");
		arrFunctionModuleLevel.add(SMModuleListing.MODULE_SALESMANAGEMENT);

		arrFunctions.add("SM Pending " + SMBidEntry.ParamObjectName + "s Report"); 
		arrFunctionIDs.add(SMPendingBidsReport); 
		arrFunctionLinks.add("smcontrolpanel.SMBidTODOCriteriaSelection"); 
		arrFunctionDescriptions.add("Required to run the Pending " + SMBidEntry.ParamObjectName + "s report.");
		arrFunctionModuleLevel.add(SMModuleListing.MODULE_SALESMANAGEMENT);

		arrFunctions.add("SM Edit " + SMBidEntry.ParamObjectName + "s"); 
		arrFunctionIDs.add(SMEditBids); 
		arrFunctionLinks.add("smcontrolpanel.SMEditBidSelect"); 
		arrFunctionDescriptions.add("Required to edit and maintain " + SMBidEntry.ParamObjectName + "s.");
		arrFunctionModuleLevel.add(SMModuleListing.MODULE_SALESMANAGEMENT);

		arrFunctions.add("SM Global Connection Pool Status"); 
		arrFunctionIDs.add(SMGlobalConnectionPoolStatus); 
		arrFunctionLinks.add("smcontrolpanel.SMCheckConnectionList"); 
		arrFunctionDescriptions.add("Required to check the Global Connection Pool Status from the main menu, which lists free and busy database connections and allows the user to release"
				+ " connections which may be hung as a result of errors, etc.");
		arrFunctionModuleLevel.add(SMModuleListing.MODULE_BASE);

		arrFunctions.add("SM Marketing Source Listing"); 
		arrFunctionIDs.add(SMOrderSourceListing); 
		arrFunctionLinks.add("smcontrolpanel.SMOrderSourceListingCriteriaSelection"); 
		arrFunctionDescriptions.add("Required to run the list of marketing sources which shows statistics on where sales leads or orders originated.");
		arrFunctionModuleLevel.add(SMModuleListing.MODULE_ORDERENTRY);

		arrFunctions.add("AR List Inactive Customers"); 
		arrFunctionIDs.add(ARListInactiveCustomers); 
		arrFunctionLinks.add("smar.ARListInactiveCustomersSelection"); 
		arrFunctionDescriptions.add("Required to run the list of inactive customers.");
		arrFunctionModuleLevel.add(SMModuleListing.MODULE_ACCOUNTSRECEIVABLE + SMModuleListing.MODULE_ORDERENTRY);

		arrFunctions.add("AR Set Inactive Customers"); 
		arrFunctionIDs.add(ARSetInactiveCustomers); 
		arrFunctionLinks.add("smar.ARSetInactiveCustomersSelection"); 
		arrFunctionDescriptions.add("Required to run the process which automatically sets inactive customers based on last transaction date, etc.");
		arrFunctionModuleLevel.add(SMModuleListing.MODULE_ACCOUNTSRECEIVABLE + SMModuleListing.MODULE_ORDERENTRY);

		arrFunctions.add("AR Delete Inactive Customers"); 
		arrFunctionIDs.add(ARDeleteInactiveCustomers); 
		arrFunctionLinks.add("smar.ARDeleteInactiveCustomersSelection"); 
		arrFunctionDescriptions.add("Required to run the automatic process which deletes inactive customers in Accounts Receivable.");
		arrFunctionModuleLevel.add(SMModuleListing.MODULE_ACCOUNTSRECEIVABLE);

		arrFunctions.add("AR Clear Monthly Statistics"); 
		arrFunctionIDs.add(ARClearMonthlyStatistics); 
		arrFunctionLinks.add("smar.ARClearMonthlyStatisticsSelection"); 
		arrFunctionDescriptions.add("Required to clear monthly statistics in Accounts Receivable, which is typically done periodically.");
		arrFunctionModuleLevel.add(SMModuleListing.MODULE_ACCOUNTSRECEIVABLE);

		arrFunctions.add("AR View Chronological Log"); 
		arrFunctionIDs.add(ARViewChronologicalLog); 
		arrFunctionLinks.add("smar.ARViewChronLogSelection"); 
		arrFunctionDescriptions.add("Required to view the Accounts Receivable 'Chronological Log', which journals the history of each Accounts Receivable transaction.");
		arrFunctionModuleLevel.add(SMModuleListing.MODULE_ACCOUNTSRECEIVABLE);

		arrFunctions.add("AR List Customers On Hold"); 
		arrFunctionIDs.add(ARListCustomersOnHold); 
		arrFunctionLinks.add("smar.ARListCustomersOnHold"); 
		arrFunctionDescriptions.add("Required to run the 'Customers On Hold' listing.");
		arrFunctionModuleLevel.add(SMModuleListing.MODULE_ACCOUNTSRECEIVABLE + SMModuleListing.MODULE_ORDERENTRY);

		arrFunctions.add("AR Reset Posting-In-Process Flag"); 
		arrFunctionIDs.add(ARResetPostingInProcessFlag); 
		arrFunctionLinks.add("smar.ARResetPostingFlag"); 
		arrFunctionDescriptions.add("Required to reset the Accounts Receivable posting flag - if a posting process fails in A/R, the system records the user and the process and will not"
				+ " allow any subsequent postings until this flag is cleared.");
		arrFunctionModuleLevel.add(SMModuleListing.MODULE_ACCOUNTSRECEIVABLE);

		arrFunctions.add("SM Update Security Functions"); 
		arrFunctionIDs.add(SMUpdateSecurityFunctions); 
		arrFunctionLinks.add("smcontrolpanel.SMUpdateSecurityFunctionSelection"); 
		arrFunctionDescriptions.add("Required to run the 'Update Security Functions' process which is normally run after a program update to freshen the list of security functions, menu names,"
				+ " add new functions, and remove obsolete ones.");
		arrFunctionModuleLevel.add(SMModuleListing.MODULE_BASE);

		arrFunctions.add("IC Inventory Control Menu"); 
		arrFunctionIDs.add(ICInventoryControl); 
		arrFunctionLinks.add("smic.ICMainMenu"); 
		arrFunctionDescriptions.add("Required to access the main Inventory Control menu - without this permission, a user will not see that menu on the main menu screen.");
		arrFunctionModuleLevel.add(SMModuleListing.MODULE_INVENTORYCONTROL + SMModuleListing.MODULE_ORDERENTRY);

		arrFunctions.add("IC Edit Items"); 
		arrFunctionIDs.add(ICEditItems); 
		arrFunctionLinks.add("smic.ICEditItemsSelection"); 
		arrFunctionDescriptions.add("Required to edit and maintain items in the master inventory.");
		arrFunctionModuleLevel.add(SMModuleListing.MODULE_INVENTORYCONTROL + SMModuleListing.MODULE_ORDERENTRY);

		arrFunctions.add("IC Edit Account Sets"); 
		arrFunctionIDs.add(ICEditAccountSets); 
		arrFunctionLinks.add("smic.ICEditAccountSetsSelection"); 
		arrFunctionDescriptions.add("Required to maintain Inventory Control account sets, which are used to set the 'control' account in inventory transactions, representing the inventory asset in the General Ledger"
				+ " - typically a warehouse location.");
		arrFunctionModuleLevel.add(SMModuleListing.MODULE_INVENTORYCONTROL + SMModuleListing.MODULE_ORDERENTRY);

		arrFunctions.add("IC Edit Categories");
		arrFunctionIDs.add(ICEditCategories); 
		arrFunctionLinks.add("smic.ICEditCategoriesSelection"); 
		arrFunctionDescriptions.add("Required to maintain Inventory Control categories, which are used to set the COGS account in inventory transactions.  The category is chosen from a transaction"
				+ " screen, for example the Order Entry detail screen, or inventory adjustment screen, and will determine which expense account is affected by the transaction.");
		arrFunctionModuleLevel.add(SMModuleListing.MODULE_INVENTORYCONTROL + SMModuleListing.MODULE_ORDERENTRY);

		arrFunctions.add("IC Display Item Information"); 
		arrFunctionIDs.add(SMSystemFunctions.ICDisplayItemInformation); 
		arrFunctionLinks.add("smic.ICDisplayItemSelection"); 
		arrFunctionDescriptions.add("Required to view the Display Item Information screen.  This permission also typically causes item numbers that appear throughout the program"
				+ " to appear as links to this same screen for users granted the permission.");
		arrFunctionModuleLevel.add(SMModuleListing.MODULE_INVENTORYCONTROL);

		arrFunctions.add("IC Item Valuation Report"); 
		arrFunctionIDs.add(SMSystemFunctions.ICItemValuationReport); 
		arrFunctionLinks.add("smic.ICItemValuationReportSelection"); 
		arrFunctionDescriptions.add("Required to process the Item Valuation report in Inventory Control.");
		arrFunctionModuleLevel.add(SMModuleListing.MODULE_INVENTORYCONTROL);

		arrFunctions.add("IC Reset Posting-In-Process Flag"); 
		arrFunctionIDs.add(SMSystemFunctions.ICResetPostingInProcessFlag); 
		arrFunctionLinks.add("smic.ICResetPostingFlag"); 
		arrFunctionDescriptions.add("Required to reset the Inventory Control posting flag - if a posting process fails in I/C, the system records the user and the process and will not"
				+ " allow any subsequent postings until this flag is cleared.");
		arrFunctionModuleLevel.add(SMModuleListing.MODULE_INVENTORYCONTROL);

		arrFunctions.add("IC Edit Batches"); 
		arrFunctionIDs.add(ICEditBatches); 
		arrFunctionLinks.add("smic.ICEditBatches"); 
		arrFunctionDescriptions.add("Required to edit any Inventory Control batches, including shipments, receipts, adjustments, etc.");
		arrFunctionModuleLevel.add(SMModuleListing.MODULE_INVENTORYCONTROL);

		arrFunctions.add("SM Edit Sales Contacts"); 
		arrFunctionIDs.add(SMEditSalesContacts); 
		arrFunctionLinks.add("smcontrolpanel.SMSalesContactSelect"); 
		arrFunctionDescriptions.add("Required to edit Sales Contacts.");
		arrFunctionModuleLevel.add(SMModuleListing.MODULE_SALESMANAGEMENT);

		arrFunctions.add("SM " + SMBidEntry.ParamObjectName + " Report"); 
		arrFunctionIDs.add(SMSalesLeadReport); 
		arrFunctionLinks.add("smcontrolpanel.SMBidReportCriteriaSelection"); 
		arrFunctionDescriptions.add("Required to run the " + SMBidEntry.ParamObjectName + " report.");
		arrFunctionModuleLevel.add(SMModuleListing.MODULE_SALESMANAGEMENT);

		arrFunctions.add("SM Edit Sales Group"); 
		arrFunctionIDs.add(SMEditSalesGroups); 
		arrFunctionLinks.add("smcontrolpanel.SMEditSalesGroups"); 
		arrFunctionDescriptions.add("Required to edit and maintain Sales Groups which are used to group orders, typically by different sales offices.");
		arrFunctionModuleLevel.add(SMModuleListing.MODULE_ORDERENTRY);

		arrFunctions.add("SM Clone Order"); 
		arrFunctionIDs.add(SMCloneOrder); 
		arrFunctionLinks.add(""); 
		arrFunctionDescriptions.add("Required to clone orders, which creates a new order mostly identical to a previous one, and includes an option to clone the details as well.");
		arrFunctionModuleLevel.add(SMModuleListing.MODULE_ORDERENTRY);

		arrFunctions.add("SM Job Cost Daily Report");
		arrFunctionIDs.add(SMJobCostDailyReport);
		arrFunctionLinks.add("smcontrolpanel.SMJobCostDailyReportSelection"); 
		arrFunctionDescriptions.add("Required to run the Job Cost Daily report which displays information about relative efficiency of orders and mechanics.");
		arrFunctionModuleLevel.add(SMModuleListing.MODULE_ORDERENTRY);

		arrFunctions.add("SM Job Cost Summary Report");
		arrFunctionIDs.add(SMJobCostSummaryReport);
		arrFunctionLinks.add("smcontrolpanel.SMDisplayJobCostSelect"); 
		arrFunctionDescriptions.add("Required to run the Job Cost Summary report which details actual costs versus estimates for orders.");
		arrFunctionModuleLevel.add(SMModuleListing.MODULE_ORDERENTRY);

		arrFunctions.add("IC Edit Inventory Options");
		arrFunctionIDs.add(ICEditICOptions);
		arrFunctionLinks.add("smic.ICEditICOptions"); 
		arrFunctionDescriptions.add("Required to edit the administrative options for the Inventory Control system.");
		arrFunctionModuleLevel.add(SMModuleListing.MODULE_INVENTORYCONTROL);

		arrFunctions.add("IC Print Individual UPC Labels");
		arrFunctionIDs.add(ICPrintUPCLabels);
		arrFunctionLinks.add("smic.ICPrintUPCSelection"); 
		arrFunctionDescriptions.add("Required to print individual UPC item labels in Inventory Control.");
		arrFunctionModuleLevel.add(SMModuleListing.MODULE_INVENTORYCONTROL);

		arrFunctions.add("SM Print Invoice Audit List");
		arrFunctionIDs.add(SMInvoiceAuditList);
		arrFunctionLinks.add("smcontrolpanel.SMPrintInvoiceAuditSelection"); 
		arrFunctionDescriptions.add("Required to run the Invoice Audit List which details costs and prices, as well as other details for a date range of invoices.");
		arrFunctionModuleLevel.add(SMModuleListing.MODULE_ORDERENTRY);

		arrFunctions.add("SM Print Pre-Invoice Report");
		arrFunctionIDs.add(SMPreInvoice);
		arrFunctionLinks.add("smcontrolpanel.SMPrintPreInvoiceSelection"); 
		arrFunctionDescriptions.add("Required to run the Pre-Invoice report which details any orders with items shipped in preparation for invoicing.  This allows management review of billing"
				+ " and projected costs BEFORE the actual invoice postings.");
		arrFunctionModuleLevel.add(SMModuleListing.MODULE_ORDERENTRY);

		arrFunctions.add("SM View Order Documents");
		arrFunctionIDs.add(SMViewOrderDocuments);
		arrFunctionLinks.add(""); 
		arrFunctionDescriptions.add("This permission allows links to appear to the document store (either FTP or Google Drive) for orders - this part of the system's Document Management system.");
		arrFunctionModuleLevel.add(SMModuleListing.MODULE_ORDERENTRY);

		arrFunctions.add("SM View " + SMBidEntry.ParamObjectName + " Documents");
		arrFunctionIDs.add(SMViewBidDocuments);
		arrFunctionLinks.add(""); 
		arrFunctionDescriptions.add("This permission allows links to appear to the document store (either FTP or Google Drive) for sales leads - this part of the system's Document Management system.");
		arrFunctionModuleLevel.add(SMModuleListing.MODULE_SALESMANAGEMENT);

		arrFunctions.add("IC Transaction History");
		arrFunctionIDs.add(ICTransactionHistory);
		arrFunctionLinks.add("smic.ICTransactionHistorySelection"); 
		arrFunctionDescriptions.add("Required to run the Inventory Transaction history report for a seleted item.");
		arrFunctionModuleLevel.add(SMModuleListing.MODULE_INVENTORYCONTROL);

		arrFunctions.add("IC Edit Item Pricing");
		arrFunctionIDs.add(ICEditItemPricing);
		arrFunctionLinks.add("smic.ICViewItemPricingSelection"); 
		arrFunctionDescriptions.add("Required to edit prices for items in the various price lists.");
		arrFunctionModuleLevel.add(SMModuleListing.MODULE_INVENTORYCONTROL + SMModuleListing.MODULE_ORDERENTRY);

		arrFunctions.add("SM Edit Critical Date"); 
		arrFunctionIDs.add(SMEditCriticalDate); 
		arrFunctionLinks.add(""); 
		arrFunctionDescriptions.add("Required to edit critical dates.  Critical date editing is accessed through a link"
				+ "(if this permission is granted) displayed in various places listing critical date entries."); //editing critical date always originated from some critical dates report or project info
		arrFunctionModuleLevel.add(SMModuleListing.MODULE_ORDERENTRY);

		arrFunctions.add("IC Assign PO Number");
		arrFunctionIDs.add(ICAssignPO);
		arrFunctionLinks.add("smic.ICAssignPOSelection"); 
		arrFunctionDescriptions.add("Required to use the simplified 'Assign a PO Number' function which generates a purchase order for immediate use without requiring all the details at the time.");
		arrFunctionModuleLevel.add(SMModuleListing.MODULE_INVENTORYCONTROL);

		arrFunctions.add("IC List Unused PO Numbers");
		arrFunctionIDs.add(ICListUnusedPOs);
		arrFunctionLinks.add("smic.ICListUnusedPOsSelection"); 
		arrFunctionDescriptions.add("Required to run the 'List Unused Purchase Orders' report.");
		arrFunctionModuleLevel.add(SMModuleListing.MODULE_INVENTORYCONTROL);

		arrFunctions.add("IC List Items Received");
		arrFunctionIDs.add(ICListItemsReceived);
		arrFunctionLinks.add("smic.ICItemsReceivedNotInvoicedSelection"); 
		arrFunctionDescriptions.add("Required to run the List Of Items Received in Inventory Control.");
		arrFunctionModuleLevel.add(SMModuleListing.MODULE_INVENTORYCONTROL);

		arrFunctions.add("IC Edit Receipts"); 
		arrFunctionIDs.add(ICEditReceipts); 
		arrFunctionLinks.add("smic.ICEditReceiptsSelection"); 
		arrFunctionDescriptions.add("Required to edit receipts in Inventory Control.");
		arrFunctionModuleLevel.add(SMModuleListing.MODULE_INVENTORYCONTROL);

		arrFunctions.add("IC Clear Posted Batches"); 
		arrFunctionIDs.add(ICClearPostedBatches); 
		arrFunctionLinks.add("smic.ICClearPostedBatchesSelection"); 
		arrFunctionDescriptions.add("Required to clear posted batches in Inventory Control.");
		arrFunctionModuleLevel.add(SMModuleListing.MODULE_INVENTORYCONTROL);

		arrFunctions.add("AR View Customer Information"); 
		arrFunctionIDs.add(ARDisplayCustomerInformation); 
		arrFunctionLinks.add("smar.ARDisplayCustomerSelect"); 
		arrFunctionDescriptions.add("Required to access the 'View Customer Information' screen in Accounts Receivable, which displays details from the customer master.  This"
				+ " permission also causes the customer number to appear as a link to that screen from various other screens in the program.");
		arrFunctionModuleLevel.add(SMModuleListing.MODULE_ACCOUNTSRECEIVABLE + SMModuleListing.MODULE_ORDERENTRY);

		arrFunctions.add("IC Edit Vendor Items"); 
		arrFunctionIDs.add(ICEditVendorItems); 
		arrFunctionLinks.add("smic.ICEditVendorItems"); 
		arrFunctionDescriptions.add("Required to edit vendor information about individual items, such as the vendor's item number, comments, etc.");
		arrFunctionModuleLevel.add(SMModuleListing.MODULE_INVENTORYCONTROL);

		arrFunctions.add("AP Edit Vendor Terms"); 
		arrFunctionIDs.add(APEditVendorTerms); 
		arrFunctionLinks.add("smap.APEditVendorTermsSelect"); 
		arrFunctionDescriptions.add("Required to edit and maintain Inventory/Accounts Payable vendor's terms.");
		arrFunctionModuleLevel.add(SMModuleListing.MODULE_INVENTORYCONTROL + SMModuleListing.MODULE_ACCOUNTSPAYABLE);

		arrFunctions.add("IC Enter Invoices"); 
		arrFunctionIDs.add(ICEnterInvoices); 
		arrFunctionLinks.add("smic.ICEnterInvoiceSelection"); 
		arrFunctionDescriptions.add("Required to enter Accounts Payable invoices against Inventory purchase orders.");
		arrFunctionModuleLevel.add(SMModuleListing.MODULE_INVENTORYCONTROL + SMModuleListing.MODULE_ACCOUNTSPAYABLE);

		arrFunctions.add("AP Receive New Vendor Notification"); 
		arrFunctionIDs.add(APReceiveNewVendorNotification); 
		arrFunctionLinks.add(""); 
		arrFunctionDescriptions.add("Granting this permission to a user causes an email to be sent to that user whenever a new vendor is added.  The email settings in"
				+ " the SM System Options menu must be set and an email address must be associated with the user in the 'Edit Users' function.");
		arrFunctionModuleLevel.add(SMModuleListing.MODULE_ACCOUNTSPAYABLE + SMModuleListing.MODULE_INVENTORYCONTROL);

		arrFunctions.add("IC List Quantities On Hand");
		arrFunctionIDs.add(ICQuantitiesOnHandReport);
		arrFunctionLinks.add("smic.ICQuantitiesOnHandSelection"); 
		arrFunctionDescriptions.add("Required to run the List Quantities On Hand report in Inventory Control.");
		arrFunctionModuleLevel.add(SMModuleListing.MODULE_INVENTORYCONTROL);

		arrFunctions.add("IC Print Receiving Labels"); 
		arrFunctionIDs.add(ICPrintReceivingLabels); 
		arrFunctionLinks.add("smic.ICPrintReceivingLabelsSelection"); 
		arrFunctionDescriptions.add("Required to access the 'IC Print Receiving Labels' function in Inventory Control.");
		arrFunctionModuleLevel.add(SMModuleListing.MODULE_INVENTORYCONTROL);

		arrFunctions.add("IC Set Inactive Items"); 
		arrFunctionIDs.add(ICSetInactiveItems); 
		arrFunctionLinks.add("smic.ICSetInactiveItemsSelection"); 
		arrFunctionDescriptions.add("Required to access the function in Inventory Control that automatically sets inactive items based on last transaction date, etc.");
		arrFunctionModuleLevel.add(SMModuleListing.MODULE_INVENTORYCONTROL);

		arrFunctions.add("IC Delete Inactive Items"); 
		arrFunctionIDs.add(ICDeleteInactiveItems); 
		arrFunctionLinks.add("smic.ICDeleteInactiveItemsSelection"); 
		arrFunctionDescriptions.add("Required to access the function that automatically deleted inactive items in Inventory Control.");
		arrFunctionModuleLevel.add(SMModuleListing.MODULE_INVENTORYCONTROL);

		arrFunctions.add("AR Edit Accounts Receivable Options");
		arrFunctionIDs.add(AREditAROptions);
		arrFunctionLinks.add("smar.AREditAROptions"); 
		arrFunctionDescriptions.add("Required to access the main administrative function for maintaining Accounts Receivable options.");
		arrFunctionModuleLevel.add(SMModuleListing.MODULE_ACCOUNTSRECEIVABLE);

		arrFunctions.add("SM List Quick Links");
		arrFunctionIDs.add(SMListQuickLinks);
		arrFunctionLinks.add("smcontrolpanel.SMListQuickLinksPasswdPrompt"); 
		arrFunctionDescriptions.add("Required to access the list of 'Quick Links' which can be copied to a device's 'desktop' screen for quick access to particular functions in the program.");
		arrFunctionModuleLevel.add(SMModuleListing.MODULE_BASE);

		arrFunctions.add("SM Query Selector");
		arrFunctionIDs.add(SMQuerySelector);
		arrFunctionLinks.add("smcontrolpanel.SMQuerySelect"); 
		arrFunctionDescriptions.add("Required to access the query tool from the main menu.");
		arrFunctionModuleLevel.add(SMModuleListing.MODULE_BASE);

		arrFunctions.add("SM Display Data Definitions");
		arrFunctionIDs.add(SMDisplayDataDefs);
		arrFunctionLinks.add(""); 
		arrFunctionDescriptions.add("Required to access the database data definitions, which is available from either the query tool or the SQL command executor.");
		arrFunctionModuleLevel.add(SMModuleListing.MODULE_BASE);

		arrFunctions.add("SM Edit " + SMBidEntry.ParamObjectName + " Product Types");
		arrFunctionIDs.add(SMEditBidProductTypes);
		arrFunctionLinks.add("smcontrolpanel.SMEditBidProductTypesSelect"); 
		arrFunctionDescriptions.add("Required to maintain " + SMBidEntry.ParamObjectName + " Product Types, which allow sales leads to be subdivided"
				+ " and tracked by the various product lines within an individual sales lead.");
		arrFunctionModuleLevel.add(SMModuleListing.MODULE_SALESMANAGEMENT);

		arrFunctions.add("SM Edit Label Printers");
		arrFunctionIDs.add(SMEditLabelPrinters);
		arrFunctionLinks.add("smcontrolpanel.SMEditLabelPrintersSelect"); 
		arrFunctionDescriptions.add("Required to set the IP address, port, font sizes, and label margins for Zebra-type label printers, used for printing item labels.");
		arrFunctionModuleLevel.add(SMModuleListing.MODULE_INVENTORYCONTROL);

		arrFunctions.add("IC Update Item Prices");
		arrFunctionIDs.add(ICUpdateItemPrices);
		arrFunctionLinks.add("smic.ICUpdateItemPricesSelection"); 
		arrFunctionDescriptions.add("Required to access the function which can automatically change item prices based on percentages, etc.");
		arrFunctionModuleLevel.add(SMModuleListing.MODULE_INVENTORYCONTROL);

		arrFunctions.add("IC Clear Transactions");
		arrFunctionIDs.add(ICClearTransactions);
		arrFunctionLinks.add("smic.ICClearTransactionsSelection"); 
		arrFunctionDescriptions.add("Required to clear Inventory Control transactions.");
		arrFunctionModuleLevel.add(SMModuleListing.MODULE_INVENTORYCONTROL);

		arrFunctions.add("IC Clear Statistics");
		arrFunctionIDs.add(ICClearStatistics);
		arrFunctionLinks.add("smic.ICClearStatisticsSelection"); 
		arrFunctionDescriptions.add("Required to clear Inventory Control statistics.");
		arrFunctionModuleLevel.add(SMModuleListing.MODULE_INVENTORYCONTROL);

		arrFunctions.add("SM Manage Orders");
		arrFunctionIDs.add(SMManageOrders);
		arrFunctionLinks.add("smcontrolpanel.SMEditOrderSelection"); 
		arrFunctionDescriptions.add("Required to edit and maintain orders - this gives access to all of the functions that relate to editing orders and quotes.");
		arrFunctionModuleLevel.add(SMModuleListing.MODULE_ORDERENTRY);

		arrFunctions.add("SM Edit Change Orders");
		arrFunctionIDs.add(SMEditChangeOrders);
		arrFunctionLinks.add("smcontrolpanel.SMEditChangeOrdersEdit"); 
		arrFunctionDescriptions.add("Required to edit and maintain change orders.");
		arrFunctionModuleLevel.add(SMModuleListing.MODULE_ORDERENTRY);

		arrFunctions.add("SM Print Installation Work Order");
		arrFunctionIDs.add(SMPrintInstallationTicket);
		arrFunctionLinks.add(""); 
		arrFunctionDescriptions.add("Required to print installation work orders in printed format (not 'live' work orders for use in modile devices).");
		arrFunctionModuleLevel.add(SMModuleListing.MODULE_ORDERENTRY);

		arrFunctions.add("SM Print Service Work Order");
		arrFunctionIDs.add(SMPrintServiceTicket);
		arrFunctionLinks.add(""); 
		arrFunctionDescriptions.add("Required to print service work orders in printed format (not 'live' work orders for use in modile devices).");
		arrFunctionModuleLevel.add(SMModuleListing.MODULE_ORDERENTRY);

		arrFunctions.add("SM Print Job Folder Label");
		arrFunctionIDs.add(SMPrintJobFolderLabel);
		arrFunctionLinks.add(""); 
		arrFunctionDescriptions.add("Required to print job folder labels.");
		arrFunctionModuleLevel.add(SMModuleListing.MODULE_ORDERENTRY);

		arrFunctions.add("SM View Project Information");
		arrFunctionIDs.add(SMViewProjectInformation);
		arrFunctionLinks.add(""); 
		arrFunctionDescriptions.add("Required to view 'project' related details on the View Order Information screen, specifically, the Billing Summary, Change Orders, and Critical Dates.");
		arrFunctionModuleLevel.add(SMModuleListing.MODULE_ORDERENTRY);

		arrFunctions.add("AR Edit Call Sheets");
		arrFunctionIDs.add(AREditCallSheets);
		arrFunctionLinks.add("smar.AREditCallSheetsSelection"); 
		arrFunctionDescriptions.add("Required to edit and maintain Accounts Receivable 'Call Sheets' for payment follow up and collections.");
		arrFunctionModuleLevel.add(SMModuleListing.MODULE_ACCOUNTSRECEIVABLE);

		arrFunctions.add("AR Print Call Sheets");
		arrFunctionIDs.add(ARPrintCallSheets);
		arrFunctionLinks.add("smar.ARPrintCallSheetsSelection"); 
		arrFunctionDescriptions.add("Required to display list of Accounts Receivable call sheets");
		arrFunctionModuleLevel.add(SMModuleListing.MODULE_ACCOUNTSRECEIVABLE);

		arrFunctions.add("AR Delete Customers");arrFunctionIDs.add(ARDeleteCustomers);arrFunctionLinks.add(""); 
		arrFunctionDescriptions.add("Required to delete customers.");
		arrFunctionModuleLevel.add(SMModuleListing.MODULE_ACCOUNTSRECEIVABLE + SMModuleListing.MODULE_ORDERENTRY);

		arrFunctions.add("SM View Truck Schedules");
		arrFunctionIDs.add(SMViewTruckSchedules);
		arrFunctionLinks.add(
				"smcontrolpanel.SMViewTruckScheduleSelection?" + SMViewTruckScheduleSelection.EDITSCHEDULE_PARAMETER + "=Y"
						+ "&" + SMViewTruckScheduleSelection.DISPLAYMOVEANDCOPYBUTTONS_PARAMETER + "=Y"); 
		arrFunctionDescriptions.add("Required to VIEW (not edit) the truck schedules.");
		arrFunctionModuleLevel.add(SMModuleListing.MODULE_ORDERENTRY);

		arrFunctions.add("IC Vendor Item List");
		arrFunctionIDs.add(ICItemNumberMatchUp);
		arrFunctionLinks.add("smic.ICItemNumberMatchUpCriteriaSelection"); 
		arrFunctionDescriptions.add("Required to view the Inventory Control 'Vendor item' list, which lists all the associated vendor items for each item in the master inventory.");
		arrFunctionModuleLevel.add(SMModuleListing.MODULE_INVENTORYCONTROL);

		arrFunctions.add("IC Under Stocked Item Report");
		arrFunctionIDs.add(ICUnderStockedItemReport);
		arrFunctionLinks.add("smic.ICUnderStockedItemReportSelection"); 
		arrFunctionDescriptions.add("Required to view the Inventory Control 'Understocked' report, which shows all items with quantities that have fallen below their specified minimum levels.");
		arrFunctionModuleLevel.add(SMModuleListing.MODULE_INVENTORYCONTROL);

		arrFunctions.add("IC Items On Hand Searched By Description");
		arrFunctionIDs.add(ICOnHandByDescriptionSelection);
		arrFunctionLinks.add("smic.ICOnHandByDescriptionSelection"); 
		arrFunctionDescriptions.add("Required use search items on hand by description (accessed through the main Inventory Control menu.");
		arrFunctionModuleLevel.add(SMModuleListing.MODULE_INVENTORYCONTROL);

		arrFunctions.add("IC Print Label Sheets For Scanning");
		arrFunctionIDs.add(ICPrintLabelScanSheets);
		arrFunctionLinks.add("smic.ICPrintLabelScanSheetSelect"); 
		arrFunctionDescriptions.add("Required to print sheets of UPC labels for scanning - these are used for scanning when items are too small or not able to carry labels.");
		arrFunctionModuleLevel.add(SMModuleListing.MODULE_INVENTORYCONTROL);

		arrFunctions.add("SM Edit Orders"); 
		arrFunctionIDs.add(SMEditOrders); 
		arrFunctionLinks.add(""); 
		arrFunctionDescriptions.add("Required to edit orders - gives access to the order header, order details, order totals, and order detail list screens for editing.");
		arrFunctionModuleLevel.add(SMModuleListing.MODULE_ORDERENTRY);

/*		3/19/2019 Currently not being used. 
		arrFunctions.add("SM Sales Effort Check"); 
		arrFunctionIDs.add(SMSalesEffortCheck); 
		arrFunctionLinks.add("smcontrolpanel.SMSalesEffortCheckSelection"); 
		arrFunctionDescriptions.add("Required to run the Sales Effort Check report.");
		arrFunctionModuleLevel.add(SMModuleListing.MODULE_SALESMANAGEMENT);
*/
		arrFunctions.add("SM View Order Detail Information"); 
		arrFunctionIDs.add(SMViewOrderDetailInformation); 
		arrFunctionLinks.add(""); 
		arrFunctionDescriptions.add("Required to view order detail information on the 'View Order Information' screen.");
		arrFunctionModuleLevel.add(SMModuleListing.MODULE_ORDERENTRY);

		arrFunctions.add("SM View Mechanics Own Truck Schedule");
		arrFunctionIDs.add(SMViewMechanicsOwnTruckSchedule);
		arrFunctionLinks.add(
				"smcontrolpanel.SMViewTruckScheduleGenerate?" 
						+ SMViewTruckScheduleSelection.EDITSCHEDULE_PARAMETER + "=Y"
						+ "&" + SMViewTruckScheduleSelection.DISPLAYMOVEANDCOPYBUTTONS_PARAMETER + "=Y"
						+ "&" + SMViewTruckScheduleSelection.DATE_RANGE_TODAY + "=Y"
						+ "&" + SMViewTruckScheduleSelection.LOOKUPMECHANIC_PARAMETER + "=Y"
						+ "&" + SMViewTruckScheduleSelection.VIEWALLLOCATIONS_PARAMETER + "=Y"
						+ "&" + SMViewTruckScheduleSelection.VIEWALLSERVICETYPES_PARAMETER + "=Y"
				); arrFunctionDescriptions.add("Required for a mechanic to view his individual truck schedule.");
		arrFunctionModuleLevel.add(SMModuleListing.MODULE_ORDERENTRY);

			arrFunctions.add("IC Find Items With Vendor Item Numbers");
			arrFunctionIDs.add(ICFindItems);
			arrFunctionLinks.add(
					"SMClasses.ObjectFinder?"
							+ "ObjectName=Items%20With%20Vendor%20Item Numbers"
							+ "&ResultClass=FinderResults"
							+ "&SearchingClass=smic.ICDisplayItemSelection"
							+ "&ReturnField=" + ICItem.ParamItemNumber			
							+ "&SearchField1=icitems.sitemdescription"
							+ "&SearchFieldAlias1=Description"
							+ "&SearchField2=icitems.sitemnumber"
							+ "&SearchFieldAlias2=Item%20No."
							+ "&SearchField3=icvendoritems.svendoritemNumber"
							+ "&SearchFieldAlias3=Vendor%20Item%20Number"
							+ "&SearchField4=icvendoritems.scomment"
							+ "&SearchFieldAlias4=Comment"
							+ "&ResultListField1=icitems.sitemnumber"
							+ "&ResultHeading1=Item%20No."
							+ "&ResultListField2=icitems.sitemdescription"
							+ "&ResultHeading2=Description"
							+ "&ResultListField3=icitems.scostunitofmeasure"
							+ "&ResultHeading3=Unit"
							+ "&ResultListField4=icvendoritems.svendor"
							+ "&ResultHeading4=Vendor"
							+ "&ResultListField5=icvendoritems.svendoritemnumber"
							+ "&ResultHeading5=Vendor%20Item"
							+ "&ResultListField6=icvendoritems.scomment"
							+ "&ResultHeading6=Comment"			
							+ "&ParameterString="
					); 
			arrFunctionDescriptions.add("Required to invoke the 'finder' to search for inventory items using the vendor's item number.");
			arrFunctionModuleLevel.add(SMModuleListing.MODULE_INVENTORYCONTROL);

			arrFunctions.add("SM Record Geocodes");
			arrFunctionIDs.add(SMRecordGeocode);
			arrFunctionLinks.add("smcontrolpanel.SMRecordGeocodeClient?" + SMRecordGeocodeClient.START_TIMER_PARAM + "=Y"); 
			arrFunctionDescriptions.add("Required to use the 'Record Geocodes' screen (experimental) which records the user's GPS position (latitude and longitude) in a database table on a polled interval");
			arrFunctionModuleLevel.add(SMModuleListing.MODULE_ORDERENTRY);

			arrFunctions.add("FA Fixed Assets Menu"); 
			arrFunctionIDs.add(FAFixedAssets); 
			arrFunctionLinks.add("smfa.FAMainMenu"); 
			arrFunctionDescriptions.add("Required to access the main Fixed Assets menu - without this permission, a user will not see that menu on the main menu screen.");
			arrFunctionModuleLevel.add(SMModuleListing.MODULE_FIXEDASSETS);

			arrFunctions.add("FA Manage Assets"); 
			arrFunctionIDs.add(FAManageAssets); 
			arrFunctionLinks.add("smfa.FAEditAssetsSelect"); 
			arrFunctionDescriptions.add("Required to edit and maintain individual assets within the Fixed Assets system.");
			arrFunctionModuleLevel.add(SMModuleListing.MODULE_FIXEDASSETS);

			arrFunctions.add("FA Enter Adjustments"); 
			arrFunctionIDs.add(FAEnterAdjustments); 
			arrFunctionLinks.add("smfa.FAMainMenu"); 
			arrFunctionDescriptions.add("Required to make adjustments to depreciation in the Fixed Assets system.");
			arrFunctionModuleLevel.add(SMModuleListing.MODULE_FIXEDASSETS);

			arrFunctions.add("FA Settings"); 
			arrFunctionIDs.add(FASettings); 
			arrFunctionLinks.add("smfa.FAMainMenu"); 
			arrFunctionDescriptions.add("Required to access the administrative settings for the Fixed Assets system.");
			arrFunctionModuleLevel.add(SMModuleListing.MODULE_FIXEDASSETS);

			arrFunctions.add("FA Period End Processing"); 
			arrFunctionIDs.add(FAPeriodEndProcessing); 
			arrFunctionLinks.add("smfa.FAPeriodEndProcessingSelect"); 
			arrFunctionDescriptions.add("Required to run the Fixed Assets period end processing, which generates the depreciation transactions.");
			arrFunctionModuleLevel.add(SMModuleListing.MODULE_FIXEDASSETS);

			arrFunctions.add("FA Clear Transaction History"); 
			arrFunctionIDs.add(FAClearTransactionHistory); 
			arrFunctionLinks.add("smfa.FAClearTransactionHistorySelect"); 
			arrFunctionDescriptions.add("Required to clear the Fixed Assets transaction history.");
			arrFunctionModuleLevel.add(SMModuleListing.MODULE_FIXEDASSETS);

			arrFunctions.add("FA Edit Depreciation Type"); 
			arrFunctionIDs.add(FAEditDepreciationType); 
			arrFunctionLinks.add("smfa.FAEditDepreciationTypeSelect"); 
			arrFunctionDescriptions.add("Required to maintain the list of depreciation types (for example, '5 Year Straight Line Depreciation', etc.");
			arrFunctionModuleLevel.add(SMModuleListing.MODULE_FIXEDASSETS);

			arrFunctions.add("FA Reset Year-To-Date Depreciation"); 
			arrFunctionIDs.add(FAResetYearToDateDepreciation); 
			arrFunctionLinks.add("smfa.FAResetYTDDepreciationSelect"); 
			arrFunctionDescriptions.add("Required to set the year-to-date depreciation totals to begin a new year of depreciation.");
			arrFunctionModuleLevel.add(SMModuleListing.MODULE_FIXEDASSETS);

			arrFunctions.add("FA Asset List"); 
			arrFunctionIDs.add(FAAssetList); 
			arrFunctionLinks.add("smfa.FAAssetListSelect"); 
			arrFunctionDescriptions.add("Required to run the Fixed Assets List report.");
			arrFunctionModuleLevel.add(SMModuleListing.MODULE_FIXEDASSETS);

			arrFunctions.add("FA Transaction Report"); 
			arrFunctionIDs.add(FATransactionReport); 
			arrFunctionLinks.add("smfa.FATransactionListSelect"); 
			arrFunctionDescriptions.add("Required to run the Fixed Assets Transactions report.");
			arrFunctionModuleLevel.add(SMModuleListing.MODULE_FIXEDASSETS);

			arrFunctions.add("SM List Customers On Hold (no options)"); 
			arrFunctionIDs.add(SMListCustomersOnHoldWithNoOptions); 
			arrFunctionLinks.add("smar.ARListCustomersOnHoldGenerate?CallingClass=smcontrolpanel.SMUserLogin"); 
			arrFunctionDescriptions.add("Required to list customers on hold in Accounts Receivable.");
			arrFunctionModuleLevel.add(SMModuleListing.MODULE_ACCOUNTSRECEIVABLE + SMModuleListing.MODULE_ORDERENTRY);

			arrFunctions.add("SM Create Quotes");
			arrFunctionIDs.add(SMCreateQuotes);
			arrFunctionLinks.add(""); 
			arrFunctionDescriptions.add("Required to be able to create new quotes in the order entry system.");
			arrFunctionModuleLevel.add(SMModuleListing.MODULE_ORDERENTRY);

			arrFunctions.add("SM List Orders For Scheduling"); 
			arrFunctionIDs.add(SMListOrdersForScheduling); 
			arrFunctionLinks.add("smcontrolpanel.SMListOrdersForSchedulingSelection"); 
			arrFunctionDescriptions.add("Required to run the 'List Orders For Scheduling' report.");
			arrFunctionModuleLevel.add(SMModuleListing.MODULE_ORDERENTRY);

			arrFunctions.add("SM Edit Site Time - Left previous");
			arrFunctionIDs.add(SMEditLeftPreviousSiteTime);
			arrFunctionLinks.add(""); 
			arrFunctionDescriptions.add("This permission will cause the 'Left previous job' field to appear for the permitted user on the 'Edit Work Order' screen.");
			arrFunctionModuleLevel.add(SMModuleListing.MODULE_ORDERENTRY);

			arrFunctions.add("SM Edit Site Time - Arrived at current");
			arrFunctionIDs.add(SMEditArrivedAtCurrentSiteTime);
			arrFunctionLinks.add(""); 
			arrFunctionDescriptions.add("This permission will cause the 'Arrived at current job' field to appear for the permitted user on the 'Edit Work Order' screen.");
			arrFunctionModuleLevel.add(SMModuleListing.MODULE_ORDERENTRY);

			arrFunctions.add("SM Edit Site Time - Left current");
			arrFunctionIDs.add(SMEditLeftCurrentSiteTime);
			arrFunctionLinks.add(""); 
			arrFunctionDescriptions.add("This permission will cause the 'Left current job' field to appear for the permitted user on the 'Edit Work Order' screen.");
			arrFunctionModuleLevel.add(SMModuleListing.MODULE_ORDERENTRY);

			arrFunctions.add("SM Edit Site Time - Arrived at next");
			arrFunctionIDs.add(SMEditArrivedAtNextSiteTime);
			arrFunctionLinks.add(""); 
			arrFunctionDescriptions.add("This permission will cause the 'Arrived at next job' field to appear for the permitted user on the 'Edit Work Order' screen.");
			arrFunctionModuleLevel.add(SMModuleListing.MODULE_ORDERENTRY);

			arrFunctions.add("SM List Order History"); 
			arrFunctionIDs.add(SMOrderHistory); 
			arrFunctionLinks.add("smcontrolpanel.SMOrderHistorySelection"); 
			arrFunctionDescriptions.add("Required to run the 'List Order History' report, which would normally be run and saved before purging old orders.");
			arrFunctionModuleLevel.add(SMModuleListing.MODULE_ORDERENTRY);

			arrFunctions.add("FA Edit Classes"); 
			arrFunctionIDs.add(FAEditClasses); 
			arrFunctionLinks.add("smfa.FAEditClassSelect"); 
			arrFunctionDescriptions.add("Required to edit and maintain the various Fixed Assets 'Asset Classes', for example 'Auto and Truck', 'Office Furniture', etc.");
			arrFunctionModuleLevel.add(SMModuleListing.MODULE_FIXEDASSETS);

			arrFunctions.add("FA Re-create GL Export File"); 
			arrFunctionIDs.add(FAReCreateGLSelection);
			arrFunctionLinks.add("smfa.FAReCreateGLSelection"); 
			arrFunctionDescriptions.add("Required to re-create a General Ledger export file from a Fixed Assets posting.  This is used if the export file from the original posting is lost;"
					+ " a re-created export file can be created and downloaded using this function.");
			arrFunctionModuleLevel.add(SMModuleListing.MODULE_FIXEDASSETS);

			arrFunctions.add("IC Re-create AP Invoice Export File"); 
			arrFunctionIDs.add(ICRecreateAPInvoiceExport); 
			arrFunctionLinks.add("smic.ICRecreateAPExportSelection"); 
			arrFunctionDescriptions.add("Required to re-create a General Ledger export file from an Accounts Payable Invoice posting.  This is used if the export file from the original posting is lost;"
					+ " a re-created export file can be created and downloaded using this function.");
			arrFunctionModuleLevel.add(SMModuleListing.MODULE_ACCOUNTSPAYABLE + SMModuleListing.MODULE_INVENTORYCONTROL);

			arrFunctions.add("FA Edit Location"); 
			arrFunctionIDs.add(FAEditLocation); 
			arrFunctionLinks.add("smfa.FAEditLocationSelect"); 
			arrFunctionDescriptions.add("Required to edit locations in Fixed Assets.");
			arrFunctionModuleLevel.add(SMModuleListing.MODULE_FIXEDASSETS);

			arrFunctions.add("SM Execute SQL Command"); 
			arrFunctionIDs.add(SMExecuteSQL); 
			arrFunctionLinks.add("smcontrolpanel.SMExecuteSQLSelect"); 
			arrFunctionDescriptions.add("Required to access the 'Execute SQL Command' function, which allows updates and inserts to the inderlying database.  This should be highly restricted"
					+ " to prevent accidental changes to the database.");
			arrFunctionModuleLevel.add(SMModuleListing.MODULE_BASE);
			
			arrFunctions.add("SM Import Data"); 
			arrFunctionIDs.add(SMImportData); 
			arrFunctionLinks.add("smcontrolpanel.SMImportDataSelect"); 
			arrFunctionDescriptions.add("Required to access the 'Import Data Command' function, which uploads the csv file onto the database.  This should be highly restricted"
					+ " to prevent accidental changes to the database.");
			arrFunctionModuleLevel.add(SMModuleListing.MODULE_BASE);
			
			

			arrFunctions.add("IC Find Items Sorted By Most Used");
			arrFunctionIDs.add(ICFindItemsSortedByMostUsed);
			arrFunctionLinks.add(
					"SMClasses.ObjectFinder?"
							+ "ObjectName=" + SMClasses.FinderResults.SEARCH_MOSTUSEDITEMS
							+ "&ResultClass=FinderResults&SearchingClass=smic.ICEditItemsSelection"
							+ "&ReturnField=%22%22"
							+ "&SearchField1=" + SMTableicitems.sItemDescription
							+ "&SearchFieldAlias1=Description"
							+ "&SearchField2=" + SMTableicitems.sItemNumber
							+ "&SearchFieldAlias2=Item%20No."
							+ "&ResultListField1=" + SMTableicitems.sItemNumber
							+ "&ResultHeading1=Item%20No."
							+ "&ResultListField2=SHIPPEDTOTAL"
							+ "&ResultHeading2=Total%20shipped"
							+ "&ResultListField3=" + SMTableicitems.sItemDescription
							+ "&ResultHeading3=Description"
							+ "&ResultListField4=" + SMTableicitems.sCostUnitOfMeasure
							+ "&ResultHeading4=Unit"
							+ "&ResultListField5=" + SMTableicitems.iActive
							+ "&ResultHeading5=Active? (Active = 1, Inactive = 0)"
							+ "&ParameterString="
					); 
			arrFunctionDescriptions.add("Required to invoke the 'finder' to search for items listed by the most commonly used.");
			arrFunctionModuleLevel.add(SMModuleListing.MODULE_INVENTORYCONTROL);

			arrFunctions.add("IC View PO Documents"); 
			arrFunctionIDs.add(ICViewPODocuments); 
			arrFunctionLinks.add(""); 
			arrFunctionDescriptions.add("This permission allows links to appear to the document store (either FTP or Google Drive) for purchase orders - this part of the system's Document Management system.");
			arrFunctionModuleLevel.add(SMModuleListing.MODULE_INVENTORYCONTROL);

			arrFunctions.add("SM Create Items From Order Details"); 
			arrFunctionIDs.add(SMCreateItemsFromOrderDetails); 
			arrFunctionLinks.add(""); 
			arrFunctionDescriptions.add("Required to access the function on the 'Edit Order Details List' screen which creates master inventory items from miscellaneous items on orders.");
			arrFunctionModuleLevel.add(SMModuleListing.MODULE_ORDERENTRY);

			arrFunctions.add("SM Create Purchase Orders From Order Details"); 
			arrFunctionIDs.add(SMCreatePOsFromOrderDetails); 
			arrFunctionLinks.add(""); 
			arrFunctionDescriptions.add("Required to access the function on the 'Edit Order Details List' screen which creates creates purchase orders from selected lines on orders.");
			arrFunctionModuleLevel.add(SMModuleListing.MODULE_ORDERENTRY);

			arrFunctions.add("SM Order Detail Direct Entry"); 
			arrFunctionIDs.add(SMDirectItemEntry); 
			arrFunctionLinks.add(""); 
			arrFunctionDescriptions.add("Required to access the function on the 'Edit Order Details List' screen which automatically adds lines to the order with calculated billing values.");
			arrFunctionModuleLevel.add(SMModuleListing.MODULE_ORDERENTRY);

			arrFunctions.add("SM Edit Proposals"); 
			arrFunctionIDs.add(SMEditProposals); 
			arrFunctionLinks.add(""); 
			arrFunctionDescriptions.add("Required to create and edit proposals in the order entry system.");
			arrFunctionModuleLevel.add(SMModuleListing.MODULE_ORDERENTRY);

			arrFunctions.add("SM Approve Proposals"); 
			arrFunctionIDs.add(SMApproveProposals); 
			arrFunctionLinks.add(""); 
			arrFunctionDescriptions.add("Required to approve proposals - this is a management function that allows permitted users to review and 'approve' proposals.  A finished proposal, with"
					+ " 'signature box' for the customer to sign cannnot be generated until a proposal is first 'Approved'");
			arrFunctionModuleLevel.add(SMModuleListing.MODULE_ORDERENTRY);

			arrFunctions.add("SM Edit Proposal Terms"); 
			arrFunctionIDs.add(SMEditProposalTerms); 
			arrFunctionLinks.add("smcontrolpanel.SMEditProposalTermsSelect"); 
			arrFunctionDescriptions.add("Required to maintain the list of proposal terms.");
			arrFunctionModuleLevel.add(SMModuleListing.MODULE_ORDERENTRY);

			arrFunctions.add("SM Edit Proposal Phrases"); 
			arrFunctionIDs.add(SMEditProposalPhrases); 
			arrFunctionLinks.add("smcontrolpanel.SMProposalPhrasesSelect"); 
			arrFunctionDescriptions.add("Required to maintain the list of proposal phrases that can be automatically inserted by a user into proposals.");
			arrFunctionModuleLevel.add(SMModuleListing.MODULE_ORDERENTRY);

			arrFunctions.add("SM Load Wage Scale Data"); 
			arrFunctionIDs.add(SMLoadWageScaleData); 
			arrFunctionLinks.add("smcontrolpanel.SMLoadWageScaleDataSelect"); 
			arrFunctionDescriptions.add("Required to load wage scale data into system for customized functions (experimental).");
			arrFunctionModuleLevel.add(SMModuleListing.MODULE_PAYROLL);

			arrFunctions.add("SM Wage Scale Report"); 
			arrFunctionIDs.add(SMWageScaleReport); 
			arrFunctionLinks.add("smcontrolpanel.SMWageScaleReportSelect"); 
			arrFunctionDescriptions.add("Required to run the 'Wage Scale Report' using custom data loaded previously.");
			arrFunctionModuleLevel.add(SMModuleListing.MODULE_PAYROLL);

			arrFunctions.add("IC Edit PO Ship Vias"); 
			arrFunctionIDs.add(ICEditPOShipVias); 
			arrFunctionLinks.add("smic.ICEditPOShipViasSelect"); 
			arrFunctionDescriptions.add("Required to maintain the list of 'ship to' addresses that can be displayed on purchase orders as destinations for ordered items.");
			arrFunctionModuleLevel.add(SMModuleListing.MODULE_INVENTORYCONTROL);

			arrFunctions.add("SM Edit Salesperson Signatures"); 
			arrFunctionIDs.add(SMEditSalespersonSignatures); 
			arrFunctionLinks.add("smcontrolpanel.SMEditSalespersonSignature"); 
			arrFunctionDescriptions.add("Required to edit salespersons' signatures, which are displayed on generated proposals.");
			arrFunctionModuleLevel.add(SMModuleListing.MODULE_ORDERENTRY);

			arrFunctions.add("SM Change Customer On Sales Orders"); 
			arrFunctionIDs.add(SMChangeCustomerOnOrders); 
			arrFunctionLinks.add(""); 
			arrFunctionDescriptions.add("Required to be able to change a customer account number on existing sales orders.");
			arrFunctionModuleLevel.add(SMModuleListing.MODULE_ORDERENTRY);

			arrFunctions.add("SM Sign Any Salespersons Proposals"); 
			arrFunctionIDs.add(SMSignAnySalespersonsProposals); 
			arrFunctionLinks.add(""); 
			arrFunctionDescriptions.add("Gives the user permission to create the salesperson's signature on other people's proposals.  This is typically a management function"
					+ " that allows someone to 'sign' a proposal for a salesperson if he isn't available.");
			arrFunctionModuleLevel.add(SMModuleListing.MODULE_ORDERENTRY);

			arrFunctions.add("SM Edit Proposal Phrase Groups"); 
			arrFunctionIDs.add(SMEditProposalPhraseGroups); 
			arrFunctionLinks.add("smcontrolpanel.SMProposalPhraseGroupSelect"); 
			arrFunctionDescriptions.add("Required to maintain the list of 'groups' of proposal phrases.  Proposal phrases can be organized into separate groups to make listing them"
					+ " on the screen easier.");
			arrFunctionModuleLevel.add(SMModuleListing.MODULE_ORDERENTRY);

			arrFunctions.add("SM Recalibrate Order and Invoice Number Counters"); 
			arrFunctionIDs.add(SMRecalibrateOrderAndInvoiceCounters); 
			arrFunctionLinks.add("smcontrolpanel.SMRecalibrateCounters"); 
			arrFunctionDescriptions.add("Required to 'recalibrate' the order and invoice counters.  If because of an error, the 'next order number' or 'next invoice number' has not been incremented"
					+ ", the program will throw an error trying to create a new order or invoice, advising that there is a 'Duplicate' key'.  In that case this function will reset the counter to remove the error.");
			arrFunctionModuleLevel.add(SMModuleListing.MODULE_ORDERENTRY);

			arrFunctions.add("SM Receive Order Cancellation Notifications"); 
			arrFunctionIDs.add(SMReceiveOrderCancellationNotifications); 
			arrFunctionLinks.add(""); 
			arrFunctionDescriptions.add("This causes the permitted user to receive an email whenver someone cancels an order - generally an audit function used by managment.");
			arrFunctionModuleLevel.add(SMModuleListing.MODULE_ORDERENTRY);

			arrFunctions.add("SM Edit Work Orders"); 
			arrFunctionIDs.add(SMEditWorkOrders); 
			arrFunctionLinks.add(""); 
			arrFunctionDescriptions.add("Required to access the 'Edit work order' screen, typically used by mechanics in the field to enter work done and items used.");
			arrFunctionModuleLevel.add(SMModuleListing.MODULE_ORDERENTRY);

			arrFunctions.add("IC Confirm Purchase Orders"); 
			arrFunctionIDs.add(ICConfirmPurchaseOrder); 
			arrFunctionLinks.add(""); 
			arrFunctionDescriptions.add("Required to confirm a Purchase Order - NOT CURRENTLY IN USE.");
			arrFunctionModuleLevel.add(SMModuleListing.MODULE_INVENTORYCONTROL);

			arrFunctions.add("SM Edit Service Type Information"); 
			arrFunctionIDs.add(SMEditServiceTypes); 
			arrFunctionLinks.add("smcontrolpanel.SMEditServiceTypeSelect"); 
			arrFunctionDescriptions.add("Required to edit service type information, which includes the terms for work orders, based on the service type.");
			arrFunctionModuleLevel.add(SMModuleListing.MODULE_ORDERENTRY);

			arrFunctions.add("SM UNPost Work Orders"); 
			arrFunctionIDs.add(SMUnpostWorkOrders); 
			arrFunctionLinks.add(""); 
			arrFunctionDescriptions.add("Required to be able to UNpost a posted work orders - this is only accessible from the 'Configure work orders' screen.");
			arrFunctionModuleLevel.add(SMModuleListing.MODULE_ORDERENTRY);

			arrFunctions.add("SM Configure Work Orders"); 
			arrFunctionIDs.add(SMConfigureWorkOrders); 
			arrFunctionLinks.add(""); 
			arrFunctionDescriptions.add("Required to access the 'Configure work orders' screen, from where items are assigned, instructions and hours used are maintained for work orders.");
			arrFunctionModuleLevel.add(SMModuleListing.MODULE_ORDERENTRY);

			arrFunctions.add("SM View Pricing On Work Orders"); 
			arrFunctionIDs.add(SMViewPricingOnWorkOrders); 
			arrFunctionLinks.add(""); 
			arrFunctionDescriptions.add("Required to view prices on the 'Edit work orders' screen.");
			arrFunctionModuleLevel.add(SMModuleListing.MODULE_ORDERENTRY);

			arrFunctions.add("BK Bank Functions Menu"); 
			arrFunctionIDs.add(BKBankFunctions); 
			arrFunctionLinks.add("smbk.BKMainMenu"); 
			arrFunctionDescriptions.add("Required to access the main Bank Functions menu - without this permission, a user will not see that menu on the main menu screen.");
			arrFunctionModuleLevel.add(SMModuleListing.MODULE_BANKFUNCTIONS);

			arrFunctions.add("BK Edit Banks"); 
			arrFunctionIDs.add(BKEditBanks); 
			arrFunctionLinks.add("smbk.BKEditBanksSelect"); 
			arrFunctionDescriptions.add("Required to edit individual bank information in the 'Bank functions'.");
			arrFunctionModuleLevel.add(SMModuleListing.MODULE_BANKFUNCTIONS);

			arrFunctions.add("SM Edit Work Order Detail Sheets"); 
			arrFunctionIDs.add(SMEditDetailSheets); 
			arrFunctionLinks.add("smcontrolpanel.SMDetailSheetSelect"); 
			arrFunctionDescriptions.add("Required to maintain work order 'detail sheets', which typically are used for customized checklists for mechanics to fill out or check off on work orders.");
			arrFunctionModuleLevel.add(SMModuleListing.MODULE_ORDERENTRY);

			arrFunctions.add("BK Edit Statements"); 
			arrFunctionIDs.add(BKEditStatements); 
			arrFunctionLinks.add("smbk.BKEditStatementSelect"); 
			arrFunctionDescriptions.add("Required to edit bank statements in the Bank functions.");
			arrFunctionModuleLevel.add(SMModuleListing.MODULE_BANKFUNCTIONS);

			arrFunctions.add("BK Import Bank Account Entries"); 
			arrFunctionIDs.add(BKImportBankEntries); 
			arrFunctionLinks.add("smbk.BKEntryImportSelect"); 
			arrFunctionDescriptions.add("Required to import bank account entries from external systems (such as for importing Accounts Payable checks into the bank account statements).");
			arrFunctionModuleLevel.add(SMModuleListing.MODULE_BANKFUNCTIONS);

			arrFunctions.add("SM Auto-Refresh Truck Schedule"); 
			arrFunctionIDs.add(SMAutoRefreshSchedule); 
			arrFunctionLinks.add(""); 
			arrFunctionDescriptions.add("Causes the truck schedule to be redisplayed automatically on a regular interval for the permitted user.  USE WITH CAUTION - activating this for multiple"
					+ " users can cause excessive processing and tie up data connections on the server.");
			arrFunctionModuleLevel.add(SMModuleListing.MODULE_ORDERENTRY);

			arrFunctions.add("BK Clear Posted Bank Statements"); 
			arrFunctionIDs.add(BKClearStatements); 
			arrFunctionLinks.add("smbk.BKClearEntriesSelect");	
			arrFunctionDescriptions.add("Required to clear posted bank statements from history.");
			arrFunctionModuleLevel.add(SMModuleListing.MODULE_BANKFUNCTIONS);

			arrFunctions.add("SM Edit Material Returns"); 
			arrFunctionIDs.add(SMEditMaterialReturns); 
			arrFunctionLinks.add("smcontrolpanel.SMEditMaterialReturnSelect"); 
			arrFunctionDescriptions.add("Required to edit and maintain 'Material Returns', which are records of material that was returned from jobs, awaiting disposition (e.g., to be"
					+ "returned to the vendor, to be replaced, to be scrapped, etc').");
			arrFunctionModuleLevel.add(SMModuleListing.MODULE_ORDERENTRY);

			arrFunctions.add("SM Initiate Material Returns"); 
			arrFunctionIDs.add(SMInitiateMaterialReturns); 
			arrFunctionLinks.add(""); 
			arrFunctionDescriptions.add("Required to originate 'Material Returns', which are records of material that was returned from jobs, awaiting disposition (e.g., to be"
					+ "returned to the vendor, to be replaced, to be scrapped, etc'). This permission is typically granted to mechanics so they can generate material returns from work"
					+ " orders in the field.");
			arrFunctionModuleLevel.add(SMModuleListing.MODULE_ORDERENTRY);

			arrFunctions.add("SM Manage Public Queries");       
			arrFunctionIDs.add(SMManagePublicQueries); 
			arrFunctionLinks.add(""); 
			arrFunctionDescriptions.add("Required to manage 'public' (i.e., common to everyone) queries.  Anyone with access to the query system can create 'private' queries, but"
					+ " only someone with THIS permission can save or delete 'public' queries.");
			arrFunctionModuleLevel.add(SMModuleListing.MODULE_BASE);

			arrFunctions.add("SM Edit Tax Certificates"); 
			arrFunctionIDs.add(SMEditTaxCertificates); 
			arrFunctionLinks.add("smcontrolpanel.SMEditTaxCertificatesSelect"); 
			arrFunctionDescriptions.add("Required to edit and maintain tax certificates.");
			arrFunctionModuleLevel.add(SMModuleListing.MODULE_ACCOUNTSRECEIVABLE+ SMModuleListing.MODULE_ORDERENTRY);

			arrFunctions.add("SM Unbilled Orders Report"); 
			arrFunctionIDs.add(SMUnbilledOrdersReport); 
			arrFunctionLinks.add("smcontrolpanel.SMUnbilledContractReportSelection");
			arrFunctionDescriptions.add("This report lists any order lines which still have a quantity on order.  It is useful for determining if any items need to be shipped/invoiced, and can also be used"
					+ " as a way to approximately project order backlog over a future period of time.");
			arrFunctionModuleLevel.add(SMModuleListing.MODULE_ORDERENTRY);

			arrFunctions.add("SM View Order Header Information"); 
			arrFunctionIDs.add(SMViewOrderHeaderInformation); 
			arrFunctionLinks.add(""); 
			arrFunctionDescriptions.add("Required to view the top section of order information (including the Bill to, Ship to, etc.) on the View Order Information screen.");
			arrFunctionModuleLevel.add(SMModuleListing.MODULE_ORDERENTRY);

			arrFunctions.add("SM Unbilled Orders For Individual Salesperson"); 
			arrFunctionIDs.add(SMUnbilledOrdersReportForIndividual); 
			arrFunctionLinks.add(
					"smcontrolpanel.SMUnbilledContractReportSelection?" + SMUnbilledContractReportSelection.PRINTINDIVIDUAL_PARAMETER + "=" 
							+ SMUnbilledContractReportSelection.PRINTINDIVIDUAL_VALUE_YES); 
			arrFunctionDescriptions.add("Required to automatically print the Unbilled Orders report for the specific user.  The user must be associated with a particular salesperson - this is"
					+ " done on the 'Edit Users' screen.");
			arrFunctionModuleLevel.add(SMModuleListing.MODULE_ORDERENTRY);

			arrFunctions.add("SM View System Configuration"); 
			arrFunctionIDs.add(SMViewSystemConfiguration); 
			arrFunctionLinks.add("smcontrolpanel.SMViewConfiguration"); 
			arrFunctionDescriptions.add("Lists system configuration settings.");
			arrFunctionModuleLevel.add(SMModuleListing.MODULE_BASE);

			arrFunctions.add("SM Create/Upload Order Document Folders"); 
			arrFunctionIDs.add(SMCreateGDriveOrderFolders); 
			arrFunctionLinks.add(""); 
			arrFunctionDescriptions.add("Activates functions for creating a file folder or uploading files to a folder for an ORDER under a specified parent folder in Google Drive.");
			arrFunctionModuleLevel.add(SMModuleListing.MODULE_ORDERENTRY);

			arrFunctions.add("SM Create/Upload Sales Lead Document Folders"); 
			arrFunctionIDs.add(SMCreateGDriveSalesLeadFolders); 
			arrFunctionLinks.add(""); 
			arrFunctionDescriptions.add("Activates functions for creating a file folder or uploading files to a folder for a SALES LEAD under a specified parent folder in Google Drive.");
			arrFunctionModuleLevel.add(SMModuleListing.MODULE_SALESMANAGEMENT);

			arrFunctions.add("SM Create/Upload PO Document Folders"); 
			arrFunctionIDs.add(SMCreateGDrivePOFolders); 
			arrFunctionLinks.add(""); 
			arrFunctionDescriptions.add("Activates functions for creating a file folder or uploading files to a folder for a PURCHASE ORDER under a specified parent folder in Google Drive.");
			arrFunctionModuleLevel.add(SMModuleListing.MODULE_ACCOUNTSPAYABLE + SMModuleListing.MODULE_INVENTORYCONTROL);

			arrFunctions.add("SM Create/Upload AR Document Folders"); 
			arrFunctionIDs.add(SMCreateGDriveARFolders); 
			arrFunctionLinks.add(""); 
			arrFunctionDescriptions.add("Activates functions for creating a file folder or uploading files to a folder for a CUSTOMER ACCOUNT under a specified parent folder in Google Drive.");
			arrFunctionModuleLevel.add(SMModuleListing.MODULE_ACCOUNTSRECEIVABLE);

			arrFunctions.add("SM Receive Work Order Post Notification"); 
			arrFunctionIDs.add(SMReceiveWorkOrderPostNotification); 
			arrFunctionLinks.add(""); 
			arrFunctionDescriptions.add("This causes the permitted salesperson to receive an email whenever a workorder for one of his orders is posted");
			arrFunctionModuleLevel.add(SMModuleListing.MODULE_ORDERENTRY);

			arrFunctions.add("SM Manage Delivery Tickets"); 
			arrFunctionIDs.add(SMManageDeliveryTickets); 
			arrFunctionLinks.add(""); 
			arrFunctionDescriptions.add("Permits the user to add, modify, and post delivery tickets and also to view saved ones");
			arrFunctionModuleLevel.add(SMModuleListing.MODULE_ORDERENTRY);

			arrFunctions.add("SM Edit Delivery Ticket Terms"); 
			arrFunctionIDs.add(SMEditDeliveryTicketTerms); 
			arrFunctionLinks.add("smcontrolpanel.SMEditDeliveryTicketTerms"); 
			arrFunctionDescriptions.add("Activates function to edit the terms and condition displayed to the customer on interactive delivery tickets");
			arrFunctionModuleLevel.add(SMModuleListing.MODULE_ORDERENTRY);

			arrFunctions.add("AP Convert ACCPAC Data"); 
			arrFunctionIDs.add(APConvertACCPACData); 
			arrFunctionLinks.add("smap.APConvertACCPAC"); 
			arrFunctionDescriptions.add("A ONE TIME function to convert ACCPAC AP data to SMCP");
			arrFunctionModuleLevel.add(SMModuleListing.MODULE_ACCOUNTSPAYABLE);

			arrFunctions.add("SM Edit Labor Back Charges"); 
			arrFunctionIDs.add(SMEditLaborBackCharges); 
			arrFunctionLinks.add("smcontrolpanel.SMLaborBackchargeSelection"); 
			arrFunctionDescriptions.add("Activates function to add and edit Labor Back Charges");
			arrFunctionModuleLevel.add(SMModuleListing.MODULE_ORDERENTRY);

			arrFunctions.add("AP Accounts Payable Menu"); 
			arrFunctionIDs.add(APAccountsPayable); 
			arrFunctionLinks.add("smap.APMainMenu"); 
			arrFunctionDescriptions.add("Required to access the main Accounts Payable menu - without this permission, a user will not see that menu on the main menu screen.");
			arrFunctionModuleLevel.add(SMModuleListing.MODULE_ACCOUNTSPAYABLE + SMModuleListing.MODULE_INVENTORYCONTROL);

			arrFunctions.add("AP Edit Accounts Payable Options");
			arrFunctionIDs.add(APEditAPOptions);
			arrFunctionLinks.add("smap.APEditAPOptions"); 
			arrFunctionDescriptions.add("Required to access the main administrative function for maintaining Accounts Payable options.");
			arrFunctionModuleLevel.add(SMModuleListing.MODULE_ACCOUNTSPAYABLE);

			arrFunctions.add("AP Edit Account Sets");
			arrFunctionIDs.add(APEditAccountSets);
			arrFunctionLinks.add("smap.APAccountSetsSelection"); 
			arrFunctionDescriptions.add("Required to maintain Accounts Payable account sets.");
			arrFunctionModuleLevel.add(SMModuleListing.MODULE_ACCOUNTSPAYABLE);

			arrFunctions.add("AP Edit Vendor Groups");
			arrFunctionIDs.add(APEditVendorGroups);
			arrFunctionLinks.add("smap.APVendorGroupSelection"); 
			arrFunctionDescriptions.add("Required to maintain Accounts Payable vendor groups.");
			arrFunctionModuleLevel.add(SMModuleListing.MODULE_ACCOUNTSPAYABLE + SMModuleListing.MODULE_INVENTORYCONTROL);

			arrFunctions.add("AP Edit 1099/CPRS Codes");
			arrFunctionIDs.add(APEdit1099CPRSCodes);
			arrFunctionLinks.add("smap.AP1099CPRSCodesSelection"); 
			arrFunctionDescriptions.add("Required to maintain Accounts Payable 1099/CPRS Codes.");
			arrFunctionModuleLevel.add(SMModuleListing.MODULE_ACCOUNTSPAYABLE);

			arrFunctions.add("AP Edit Distribution Codes");
			arrFunctionIDs.add(APEditDistributionCodes);
			arrFunctionLinks.add("smap.APDistributionCodesSelection"); 
			arrFunctionDescriptions.add("Required to maintain Accounts Payable Distribution Codes.");
			arrFunctionModuleLevel.add(SMModuleListing.MODULE_ACCOUNTSPAYABLE);

			arrFunctions.add("AP Edit Vendor Remit To Locations");
			arrFunctionIDs.add(APEditVendorRemitToLocations);
			arrFunctionLinks.add(""); 
			arrFunctionDescriptions.add("Required to maintain vendor remit to locations.");
			arrFunctionModuleLevel.add(SMModuleListing.MODULE_ACCOUNTSPAYABLE + SMModuleListing.MODULE_INVENTORYCONTROL);

			arrFunctions.add("GL General Ledger Menu");
			arrFunctionIDs.add(GLGeneralLedger);
			arrFunctionLinks.add("smgl.GLMainMenu"); 
			arrFunctionDescriptions.add("Required to access the main General Ledger menu - without this permission, a user will not see that menu on the main menu screen.");
			arrFunctionModuleLevel.add(SMModuleListing.MODULE_GENERALLEDGER + SMModuleListing.MODULE_ACCOUNTSPAYABLE + SMModuleListing.MODULE_ACCOUNTSRECEIVABLE + SMModuleListing.MODULE_ORDERENTRY);

			arrFunctions.add("SM Create/Upload Vendor Document Folders"); 
			arrFunctionIDs.add(SMCreateGDriveVendorFolders); 
			arrFunctionLinks.add(""); 
			arrFunctionDescriptions.add("Activates functions for creating a file folder or uploading files to a folder for a VENDOR under a specified parent folder in Google Drive.");
			arrFunctionModuleLevel.add(SMModuleListing.MODULE_ACCOUNTSPAYABLE + SMModuleListing.MODULE_INVENTORYCONTROL);

			arrFunctions.add("SM Edit Personal Reminders"); 
			arrFunctionIDs.add(SMEditPersonalReminders); 
			arrFunctionLinks.add("smcontrolpanel.SMRemindersSelection"); 
			arrFunctionDescriptions.add("Required to schedule personal reminders.");
			arrFunctionModuleLevel.add(SMModuleListing.MODULE_BASE);

			arrFunctions.add("SM Edit Reminders"); 
			arrFunctionIDs.add(SMEditReminders); 
			arrFunctionLinks.add("smcontrolpanel.SMRemindersSelection?" + SMReminders.Paramiremindermode + "=1"); 
			arrFunctionDescriptions.add("Required to schedule reminders for other users.");
			arrFunctionModuleLevel.add(SMModuleListing.MODULE_BASE);

			arrFunctions.add("SM Sales Invoice Tax Report"); 
			arrFunctionIDs.add(SMSalesTaxReport); 
			arrFunctionLinks.add("smcontrolpanel.SMSalesTaxReportSelection"); 
			arrFunctionDescriptions.add("Lists tax liabilities and amounts collected on sales invoices.");
			arrFunctionModuleLevel.add(SMModuleListing.MODULE_ORDERENTRY);

			arrFunctions.add("GL Edit Cost Centers"); 
			arrFunctionIDs.add(GLEditCostCenters); 
			arrFunctionLinks.add("smgl.GLEditCostCenterSelection"); 
			arrFunctionDescriptions.add("Required to maintain cost centers.");
			arrFunctionModuleLevel.add(SMModuleListing.MODULE_GENERALLEDGER + SMModuleListing.MODULE_ORDERENTRY);

			arrFunctions.add("AP Display Vendor Information"); 
			arrFunctionIDs.add(APDisplayVendorInformation); 
			arrFunctionLinks.add("smap.APDisplayVendorSelection"); 
			arrFunctionDescriptions.add("Required to view vendor information.");
			arrFunctionModuleLevel.add(SMModuleListing.MODULE_ACCOUNTSPAYABLE + SMModuleListing.MODULE_INVENTORYCONTROL);

			arrFunctions.add("SM Display Most Used Items On Work Order"); 
			arrFunctionIDs.add(SMDisplayMostUsedItemsOnWorkOrder); 
			arrFunctionLinks.add(""); 
			arrFunctionDescriptions.add("Enables the function to display the mechanics most commonly used items on the work order edit screen.");
			arrFunctionModuleLevel.add(SMModuleListing.MODULE_ORDERENTRY);

			arrFunctions.add("SM Edit Location On Work Order Detail"); 
			arrFunctionIDs.add(SMEditLocationOnWorkOrderDetail); 
			arrFunctionLinks.add(""); 
			arrFunctionDescriptions.add("Enables the function to edit location on work order items.");
			arrFunctionModuleLevel.add(SMModuleListing.MODULE_ORDERENTRY);

			arrFunctions.add("AS Alarm Systems Menu"); 
			arrFunctionIDs.add(ASAlarmFunctions); 
			arrFunctionLinks.add("smas.ASMainMenu"); 
			arrFunctionDescriptions.add("Required to access the main Alarm Systems menu - without this permission, a user will not see that menu on the main menu screen.");
			arrFunctionModuleLevel.add(SMModuleListing.MODULE_ALARMSYSTEM);

			//arrFunctions.add("AS Alarm Listener Service");arrFunctionIDs.add(ASAlarmListener);arrFunctionLinks.add(""); 
			//arrFunctionDescriptions.add("This permission is only for alarm controllers, not for actual users.  It allows the system to authenticate"
			//	+ " alarm controllers when they try to contact the server, but it's not a permission normally granted to users.");

			arrFunctions.add("AS Edit Controllers"); 
			arrFunctionIDs.add(ASEditControllers); 
			arrFunctionLinks.add("smas.ASEditControllersSelect"); 
			arrFunctionDescriptions.add("Use this to add, modify, and delete individual alarm controllers from the system.");
			arrFunctionModuleLevel.add(SMModuleListing.MODULE_ALARMSYSTEM);

			arrFunctions.add("AS Edit Devices"); 
			arrFunctionIDs.add(ASEditDevices); 
			arrFunctionLinks.add("smas.ASEditDevicesSelect"); 
			arrFunctionDescriptions.add("Use this to add, modify, and delete individual security system devices, "
					+ "like sensors, door open switches, alarm sounding devices, etc..");
			arrFunctionModuleLevel.add(SMModuleListing.MODULE_ALARMSYSTEM);

			arrFunctions.add("AS Edit Alarm Sequences"); 
			arrFunctionIDs.add(ASEditAlarmSequences); 
			arrFunctionLinks.add("smas.ASEditAlarmSequencesSelect"); 
			arrFunctionDescriptions.add("Use this to set up and configure alarm sequences.");
			arrFunctionModuleLevel.add(SMModuleListing.MODULE_ALARMSYSTEM);

			arrFunctions.add("AS Authorize Device Users"); 
			arrFunctionIDs.add(ASAuthorizeDeviceUsers); 
			arrFunctionLinks.add("smas.ASAuthorizeDeviceUsersSelect"); 
			arrFunctionDescriptions.add("Use this to authorize or de-authorize users for activating or monitoring the various connected devices.");
			arrFunctionModuleLevel.add(SMModuleListing.MODULE_ALARMSYSTEM);

			arrFunctions.add("AS Activate Devices"); 
			arrFunctionIDs.add(ASActivateDevices); 
			arrFunctionLinks.add("smas.ASActivateDevicesEdit"); 
			arrFunctionDescriptions.add("This permission gives the user access to the 'Activate Devices' page,"
					+ " which then allows them to activate any devices in the activation zones they are allowed to use.");
			arrFunctionModuleLevel.add(SMModuleListing.MODULE_ALARMSYSTEM);

			arrFunctions.add("AS Activate/Deactivate Alarms"); 
			arrFunctionIDs.add(ASActivateAlarmSequences); 
			arrFunctionLinks.add("smas.ASActivateAlarmsEdit"); 
			arrFunctionDescriptions.add("This permission gives the user access to the 'AS Activate/Deactivate Alarms,"
					+ " which then allows them to arm or disarm any of the alarm sequences they are allowed to use.");
			arrFunctionModuleLevel.add(SMModuleListing.MODULE_ALARMSYSTEM);

			arrFunctions.add("AS List User Events By Date");
			arrFunctionIDs.add(ASListUserEventsByDate);
			arrFunctionLinks.add(
					"smcontrolpanel.SMQueryParameters?" 
							+ "QUERYSTRING="
							+ clsServletUtilities.URLEncode(
									"select"
											+ " DATE_FORMAT(" + SMTablessuserevents.dattimeoccurrence + ", '%c/%e/%Y %l:%i:%s %p') as 'Time'"
											+ ", " + SMTablessuserevents.suserfullname + " as 'Name'"
											+ ", " + SMTablessuserevents.sdevicedescription + " as 'Device'"
											+ ", " + SMTablessuserevents.scomment + " as 'Comment'"
											+ ", CONCAT(" + SMTablessuserevents.suserlatitude + ", ',', " 
											+ SMTablessuserevents.suserlongitude + ") as 'Lat./Lon.'"
											+ ",  CONCAT('<A HREF=https://maps.google.com/maps?q=loc:', " + SMTablessuserevents.suserlatitude 
											+ ", ',', " + SMTablessuserevents.suserlongitude + ", '>Originated</A>') AS 'Map Link'"
											+ " FROM " + SMTablessuserevents.TableName
											+ " WHERE ("
											+ " (" + SMTablessuserevents.dattimeoccurrence + " >= STR_TO_DATE('[[*DATEPICKER*{Starting on:}{TODAY}]] 00:00:00', '%m/%d/%Y %H:%i:%S'))"
											+ ")"
											+ " ORDER BY " + SMTablessuserevents.dattimeoccurrence
									)
									+ "&QUERYTITLE=" + clsServletUtilities.URLEncode("AS List User Events By Date")
									+ "&ALTERNATEROWCOLORS=Y"
									+ "&FONTSIZE=small"
									//+ "&SHOWSQLCOMMAND=Y"
									//				Possible parameters:				
									//				public static String PARAM_EXPORTOPTIONS = "EXPORTOPTIONS";
									//				public static String EXPORT_COMMADELIMITED_VALUE = "COMMADELIMITED";
									//				public static String EXPORT_HTML_VALUE = "HTML";
									//				public static String EXPORT_NOEXPORT_VALUE = "NOEXPORT";
									//				public static String EXPORT_COMMADELIMITED_LABEL = "Comma delimited file";
									//				public static String EXPORT_HTML_LABEL = "HTML (web page) file";
									//				public static String EXPORT_NOEXPORT_LABEL = "Do not export - display on screen";
									//				public static String PARAM_QUERYID = "QUERYID";
									//				public static String PARAM_QUERYTITLE = "QUERYTITLE";
									//				public static String PARAM_QUERYSTRING = "QUERYSTRING";
									//				public static String PARAM_PWFORQUICKLINK = "PWFORQUICKLINK";
									//				public static String PARAM_FONTSIZE = "FONTSIZE";
									//				public static String PARAM_INCLUDEBORDER = "INCLUDEBORDER";
									//				public static String PARAM_ALTERNATEROWCOLORS = "ALTERNATEROWCOLORS";
									//				public static String PARAM_TOTALNUMERICFIELDS = "TOTALNUMERICFIELDS";
									//				public static String PARAM_SHOWSQLCOMMAND = "SHOWSQLCOMMAND";
					); 
			arrFunctionDescriptions.add("Lists user events, like activating a device or setting an alarm, etc.");
			arrFunctionModuleLevel.add(SMModuleListing.MODULE_ALARMSYSTEM);

			arrFunctions.add("AS List Device Events By Date");
			arrFunctionIDs.add(ASListDeviceEventsByDate);
			arrFunctionLinks.add(
					"smcontrolpanel.SMQueryParameters?" 
							+ "QUERYSTRING="
							+ clsServletUtilities.URLEncode(
									"select"
											+ " DATE_FORMAT(" + SMTablessdeviceevents.dattimeoccurrence + ", '%c/%e/%Y %l:%i:%s %p') as 'Time'"
											+ ", CONCAT(" + SMTablessdeviceevents.sdevicedescription + ", ' ',"
											+ "    IF (" + SMTablessdevices.idevicetype + "=0,"
											+ "        IF(" + SMTablessdeviceevents.ieventtype + "=0, 'was opened', IF (" + SMTablessdeviceevents.ieventtype + "=1, 'was OPENED','was CLOSED')),"
											+ "   		      IF (" + SMTablessdevices.idevicetype + "=1, IF(" + SMTablessdeviceevents.ieventtype + "=0, 'was ACTIVATED', IF(" 
											+ SMTablessdeviceevents.ieventtype + "=1, 'STARTED detecting motion', 'STOPPED detecting motion')), IF(" + SMTablessdeviceevents.ieventtype + "=0, 'was ACTIVATED', IF(" + SMTablessdeviceevents.ieventtype + "=1, 'was turned ON', 'was turned OFF') ))"
											+ "    )"
											+ ") AS Event"
											+ ", " + SMTablessdeviceevents.scomment + " AS 'Comment'"
											+ ", IF(" + SMTablessdeviceevents.szonename + " != '', " + SMTablessdeviceevents.szonename + ", '(N/A)') AS 'Alarm Sequence Name'"
											+ " FROM " + SMTablessdeviceevents.TableName + " LEFT JOIN " + SMTablessdevices.TableName
											+ " ON " + SMTablessdeviceevents.TableName + "." + SMTablessdeviceevents.ldeviceid + "=" + SMTablessdevices.TableName + "."  + SMTablessdevices.lid
											+ " WHERE ("
											+ " (" + SMTablessdeviceevents.dattimeoccurrence + " >= STR_TO_DATE('[[*DATEPICKER*{Starting on:}{TODAY}]] 00:00:00', '%m/%d/%Y %H:%i:%S'))"
											//Don't pick up 'check in' events:
											+ " AND (" + SMTablessdeviceevents.sreferenceid + "!='" + "[1465324143]" + "')"
											+ ")"
											+ " ORDER BY " + SMTablessdeviceevents.dattimeoccurrence
									)
									+ "&QUERYTITLE=" + clsServletUtilities.URLEncode("AS List Device Events By Date")
									+ "&ALTERNATEROWCOLORS=Y"
									+ "&FONTSIZE=small"
									//+ "&SHOWSQLCOMMAND=Y"
									//					Possible parameters:				
									//					public static String PARAM_EXPORTOPTIONS = "EXPORTOPTIONS";
									//					public static String EXPORT_COMMADELIMITED_VALUE = "COMMADELIMITED";
									//					public static String EXPORT_HTML_VALUE = "HTML";
									//					public static String EXPORT_NOEXPORT_VALUE = "NOEXPORT";
									//					public static String EXPORT_COMMADELIMITED_LABEL = "Comma delimited file";
									//					public static String EXPORT_HTML_LABEL = "HTML (web page) file";
									//					public static String EXPORT_NOEXPORT_LABEL = "Do not export - display on screen";
									//					public static String PARAM_QUERYID = "QUERYID";
									//					public static String PARAM_QUERYTITLE = "QUERYTITLE";
									//					public static String PARAM_QUERYSTRING = "QUERYSTRING";
									//					public static String PARAM_PWFORQUICKLINK = "PWFORQUICKLINK";
									//					public static String PARAM_FONTSIZE = "FONTSIZE";
									//					public static String PARAM_INCLUDEBORDER = "INCLUDEBORDER";
									//					public static String PARAM_ALTERNATEROWCOLORS = "ALTERNATEROWCOLORS";
									//					public static String PARAM_TOTALNUMERICFIELDS = "TOTALNUMERICFIELDS";
									//					public static String PARAM_SHOWSQLCOMMAND = "SHOWSQLCOMMAND";
					); 
			arrFunctionDescriptions.add("Lists device events, like when a motion detector is triggered, or a door is opened, etc.");
			arrFunctionModuleLevel.add(SMModuleListing.MODULE_ALARMSYSTEM);

			arrFunctions.add("AS List ALL Events By Date");
			arrFunctionIDs.add(ASListAllSSEventsByDate);
			arrFunctionLinks.add(
					"smcontrolpanel.SMQueryParameters?" 
							+ "QUERYSTRING="
							+ clsServletUtilities.URLEncode(
									"SELECT"
											+ " DATE_FORMAT(OCCURRENCETIME, '%c/%e/%Y %l:%i:%s %p') AS 'Time'"
											+ ", `User`"
											+ ", `Event Description`"
											+ ", `Comment`"
											+ ", `Map Link`"
											+ " FROM" 
											+ "("
											+ "select"
											+ " " + SMTablessdeviceevents.dattimeoccurrence + " AS 'OCCURRENCETIME'"
											+ ", CONCAT(" + SMTablessdeviceevents.sdevicedescription + ", ' ', IF (" + SMTablessdevices.idevicetype 
											+ "=0, IF(" + SMTablessdeviceevents.ieventtype + "=0, 'was opened', IF (" + SMTablessdeviceevents.ieventtype 
											+ "=1, 'was OPENED','was CLOSED')), IF (" + SMTablessdevices.idevicetype + "=1, IF(" + SMTablessdeviceevents.ieventtype 
											+ "=0, 'was ACTIVATED', IF(" + SMTablessdeviceevents.ieventtype + "=1, 'STARTED detecting motion', 'STOPPED detecting motion')), IF(" 
											+ SMTablessdeviceevents.ieventtype + "=0, 'was ACTIVATED', IF(" + SMTablessdeviceevents.ieventtype 
											+ "=1, 'was turned ON', 'was turned OFF') )) )) as 'Event Description'"
											+ ", '(UNKNOWN)' as 'User'"
											+ ", " + SMTablessdeviceevents.scomment + " AS 'Comment'"
											+ ", '(N/A)' as 'Map Link'"
											+ " FROM " + SMTablessdeviceevents.TableName + " LEFT JOIN " + SMTablessdevices.TableName + " ON " + SMTablessdeviceevents.TableName + "." 
											+ SMTablessdeviceevents.ldeviceid + "=" + SMTablessdevices.TableName + "." + SMTablessdevices.lid
											+ " WHERE ("
											//Don't pick up 'check in' events:
											+ "(" + SMTablessdeviceevents.sreferenceid + "!='" + "[1465324143]" + "')"
											+ ")"
											+ " UNION ALL"
											+ " select"
											+ " " + SMTablessuserevents.dattimeoccurrence + " AS 'OCCURRENCETIME'"
											+ ", IF((" + SMTablessuserevents.sdevicedescription + " != ''), CONCAT(" + SMTablessuserevents.sdevicedescription + ", ' - ', " 
											+ SMTablessuserevents.scomment + "), " + SMTablessuserevents.scomment + ") as 'Event Description'"
											+ ", " + SMTablessuserevents.suserfullname + " as 'User'"
											+ ", " + SMTablessuserevents.scomment + " AS 'Comment'"
											+ ", IF ((" + SMTablessuserevents.suserlatitude + " != '') AND (" + SMTablessuserevents.suserlongitude 
											+ " != ''), CONCAT('<A HREF=https://maps.google.com/maps?q=loc:', " + SMTablessuserevents.suserlatitude + ", ',', " 
											+ SMTablessuserevents.suserlongitude + ", '>Originated</A>'), '(N/A)') AS 'Map Link'"
											+ "FROM " + SMTablessuserevents.TableName
											+ ") AS UNIONQUERY"
											+ " WHERE ("
											+ "(UNIONQUERY.OCCURRENCETIME >= STR_TO_DATE('[[*DATEPICKER*{Starting on:}{TODAY}]] 00:00:00', '%m/%d/%Y %H:%i:%S'))"
											+ ")"
											+ " ORDER BY UNIONQUERY.OCCURRENCETIME"
									)
									+ "&QUERYTITLE=" + clsServletUtilities.URLEncode("AS List ALL Events By Date")
									+ "&ALTERNATEROWCOLORS=Y"
									+ "&FONTSIZE=small"
									//+ "&SHOWSQLCOMMAND=Y"
					); 
			arrFunctionDescriptions.add("Lists ALL alarm system events, like when a motion detector is triggered, or a door is opened, as well as when someone sets an alarm, or opens a door remotely, etc.");
			arrFunctionModuleLevel.add(SMModuleListing.MODULE_ALARMSYSTEM);
			
			arrFunctions.add("AS List Specific Users Permissions");
			arrFunctionIDs.add(ASListUsersPermissions);
			arrFunctionLinks.add(
					"smcontrolpanel.SMQueryParameters?" 
							+ "QUERYSTRING="
							+ clsServletUtilities.URLEncode(
									"[[*SETVARIABLES*SET @UserName := '[[Enter User Full Name ]]']]"
									+ "(SELECT 'Alarm' AS 'Alarm or Device Type'"
									+ ", " + SMTablessalarmsequences.TableName + "." + SMTablessalarmsequences.lid + " AS 'ID'"
									+ ", " + SMTablessalarmsequences.TableName + "." + SMTablessalarmsequences.sname + " AS 'Alarm or Device Name'"
									+ ", CONCAT(" + SMTableusers.TableName + "." + SMTableusers.sUserFirstName + ", ' ', " + SMTableusers.TableName + "." + SMTableusers.sUserLastName + ") AS 'User'"
									+ " FROM "
									+ SMTablessalarmsequences.TableName
									+ " LEFT JOIN "
									+  SMTablessalarmsequenceusers.TableName 
									+ " ON " + SMTablessalarmsequenceusers.TableName + "." + SMTablessalarmsequenceusers.lalarmsequenceid 
									+ "=" + SMTablessalarmsequences.TableName + "." + SMTablessalarmsequences.lid
									+ " LEFT JOIN " + SMTableusers.TableName 
									+ " ON " + SMTablessalarmsequenceusers.TableName + "." + SMTablessalarmsequenceusers.luserid + "=" + SMTableusers.TableName + "." + SMTableusers.lid
									+ " WHERE ( CONCAT (TRIM(" + SMTableusers.TableName + "." + SMTableusers.sUserFirstName + "), ' ', TRIM(" + SMTableusers.TableName + "." + SMTableusers.sUserLastName + ")) = @UserName))"
									
									+ " UNION"
									+ "(SELECT"
									+ "'Device' AS 'Alarm or Device Type'"
									+ ", " + SMTablessdevices.TableName + "." + SMTablessdevices.lid + " AS 'ID'"
									+ ", " + SMTablessdevices.TableName + "." + SMTablessdevices.sdescription + " AS 'Alarm or Device Name'"
									+ ", CONCAT(" + SMTableusers.TableName + "." + SMTableusers.sUserFirstName + ", ' ', " + SMTableusers.TableName + "." + SMTableusers.sUserLastName + ") AS 'User'"
									+ " FROM "
									+ SMTablessdevices.TableName
									+ " LEFT JOIN "
									+ SMTablessdeviceusers.TableName + " ON " + SMTablessdeviceusers.TableName + "." + SMTablessdeviceusers.ldeviceid + " = " + SMTablessdevices.TableName + "." + SMTablessdevices.lid
									+ " LEFT JOIN " + SMTableusers.TableName 
									+ " ON " + SMTablessdeviceusers.TableName + "." + SMTablessdeviceusers.luserid + "=" + SMTableusers.TableName + "." + SMTableusers.lid
									+ " WHERE"
									+ "( CONCAT (TRIM(" + SMTableusers.TableName + "." + SMTableusers.sUserFirstName + "), ' ', TRIM(" + SMTableusers.TableName + "." + SMTableusers.sUserLastName + ")) = @UserName))"
									)
									+ "&QUERYTITLE=" + clsServletUtilities.URLEncode("AS List Specific Users Permissions")
									+ "&ALTERNATEROWCOLORS=Y"
									+ "&FONTSIZE=medium"
									//+ "&SHOWSQLCOMMAND=Y"
					); 
			arrFunctionDescriptions.add("Lists ALL alarm sequences and devices each user has permission to access.");
			arrFunctionModuleLevel.add(SMModuleListing.MODULE_ALARMSYSTEM);

			arrFunctions.add("AS Device Status"); 
			arrFunctionIDs.add(ASDeviceStatus); 
			arrFunctionLinks.add("smas.ASDeviceStatusEdit"); 
			arrFunctionDescriptions.add("This permission gives the user access to the 'Device status' page,"
					+ " which lists devices' status and allows the contacts to be managed manually for diagnostics.");
			arrFunctionModuleLevel.add(SMModuleListing.MODULE_ALARMSYSTEM);

			arrFunctions.add("AS Authorize Alarm Users"); 
			arrFunctionIDs.add(ASAuthorizeAlarmSequenceUsers); 
			arrFunctionLinks.add("smas.ASAuthorizeAlarmUsersSelect"); 
			arrFunctionDescriptions.add("Use this to authorize or de-authorize users for activating/deactivating alarms.");
			arrFunctionModuleLevel.add(SMModuleListing.MODULE_ALARMSYSTEM);

			arrFunctions.add("SM Create/Upload Work Order Document Folders"); 
			arrFunctionIDs.add(SMCreateGDriveWorkOrderFolders); 
			arrFunctionLinks.add(""); 
			arrFunctionDescriptions.add("Activates functions for creating a file folder or uploading files to a folder for an WORK ORDER under a specified parent folder in Google Drive.");
			arrFunctionModuleLevel.add(SMModuleListing.MODULE_ORDERENTRY);

			arrFunctions.add("SM View Work Order Documents");
			arrFunctionIDs.add(SMViewWorkOrderDocuments);
			arrFunctionLinks.add(""); 
			arrFunctionDescriptions.add("This permission allows links to appear to the document store (either FTP or Google Drive) for work orders - this part of the system's Document Management system.");
			arrFunctionModuleLevel.add(SMModuleListing.MODULE_ORDERENTRY);

			arrFunctions.add("AS Configure Controllers"); 
			arrFunctionIDs.add(ASConfigureControllers); 
			arrFunctionLinks.add("smas.ASConfigureControllerSelect"); 
			arrFunctionDescriptions.add("This function actually connects to the selected controller and allows it to be configured remotely"
					+ " - it also allows the controller's log file to be read and cleared.");
			arrFunctionModuleLevel.add(SMModuleListing.MODULE_ALARMSYSTEM);

			arrFunctions.add("AS Controller Diagnostics"); 
			arrFunctionIDs.add(ASControllerDiagnostics); 
			arrFunctionLinks.add("smas.ASControllerDiagnosticsSelect"); 
			arrFunctionDescriptions.add("Use this to view and/or clear the controller's log, get a list of telenet troubleshooting commands, view the controller pin-to-terminal mappings, etc.");
			arrFunctionModuleLevel.add(SMModuleListing.MODULE_ALARMSYSTEM);

			arrFunctions.add("AS Edit System Options"); 
			arrFunctionIDs.add(ASEditSSOptions); 
			arrFunctionLinks.add("smas.ASEditSSOptionsEdit"); 
			arrFunctionDescriptions.add("Sets global options for the Security System module.");
			arrFunctionModuleLevel.add(SMModuleListing.MODULE_ALARMSYSTEM);

			arrFunctions.add("AS List Authorized Device Users"); 
			arrFunctionIDs.add(ASListAuthorizedDeviceUsers); 
			arrFunctionLinks.add(
					"smcontrolpanel.SMQueryParameters?" 
							+ "QUERYSTRING="
							+ clsServletUtilities.URLEncode(
									"SELECT" 
										+ " CONCAT('<A HREF=*LINKBASE*smas.ASAuthorizeDeviceUsersEdit?lid=', ssdevices.lid, '&" 
												+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "='" + sDBID + "', ssdevices.sdescription, '</A>') as 'Device'"
											+ ", ssdeviceusers.suser AS 'Authorized User'"
											+ ", CONCAT(users.sUserFirstName, ' ', users.sUserLastName) AS 'Full Name'"
											+ " from"
											+ " ssdeviceusers"
											+ " LEFT JOIN ssdevices on ssdevices.lid = ssdeviceusers.ldeviceid"
											+ " LEFT JOIN users on users.sUserName = ssdeviceusers.suser"
											+ " ORDER BY ssdevices.sdescription, ssdeviceusers.suser"
									)
									+ "&QUERYTITLE=" + clsServletUtilities.URLEncode("AS List Authorized Device Users")
									+ "&ALTERNATEROWCOLORS=Y"
									+ "&FONTSIZE=small"
									//+ "&SHOWSQLCOMMAND=Y"
					); 
			arrFunctionDescriptions.add("Lists all the authorized users for a device, and includes a link to the screen for setting permissions to the devices.");
			arrFunctionModuleLevel.add(SMModuleListing.MODULE_ALARMSYSTEM);

			arrFunctions.add("AS List Authorized Alarm Users"); 
			arrFunctionIDs.add(ASListAuthorizedAlarmSequenceUsers); 
			arrFunctionLinks.add(
					"smcontrolpanel.SMQueryParameters?" 
							+ "QUERYSTRING="
							+ clsServletUtilities.URLEncode(
									"SELECT" 
											+ " CONCAT('<A HREF=*LINKBASE*smas.ASAuthorizeAlarmUsersEdit?lid=', ssalarmsequences.lid, '&'" 
												+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "='" + sDBID + "'>', ssalarmsequences.sname, '</A>')"
											+ " as 'Alarm Sequence Name'"
											+ ", ssalarmsequenceusers.suser AS 'Authorized User'"
											+ ", CONCAT(users.sUserFirstName, ' ', users.sUserLastName) AS 'Full Name'"
											+ " from"
											+ " ssalarmsequenceusers"
											+ " LEFT JOIN ssalarmsequences on ssalarmsequences.lid = ssalarmsequenceusers.lalarmsequenceid"
											+ " LEFT JOIN users on users.sUserName = ssalarmsequenceusers.suser"
											+ " order by ssalarmsequences.sname, ssalarmsequenceusers.suser"
									)
									+ "&QUERYTITLE=" + clsServletUtilities.URLEncode("AS List Authorized Device Users")
									+ "&ALTERNATEROWCOLORS=Y"
									+ "&FONTSIZE=small"
									//+ "&SHOWSQLCOMMAND=Y"
					); 
			arrFunctionDescriptions.add("Lists all the authorized users for each alarm sequence, and includes a link to the screen for setting permissions to them.");
			arrFunctionModuleLevel.add(SMModuleListing.MODULE_ALARMSYSTEM);

			arrFunctions.add("SM View Appointment Calendar"); 
			arrFunctionIDs.add(SMViewAppointmentCalendar); 
			arrFunctionLinks.add("smcontrolpanel.SMViewAppointmentCalendarSelection"); 
			arrFunctionDescriptions.add("Required to view schedule entries");
			arrFunctionModuleLevel.add(SMModuleListing.MODULE_BASE);
			
			//arrFunctions.add("SM View All Users Schedules"); 
			//arrFunctionIDs.add(SMViewAllUsersSchedules); 
			//arrFunctionLinks.add(""); 
			//arrFunctionDescriptions.add("Required to view other users schedule entries");
			//arrFunctionModuleLevel.add(SMModuleListing.MODULE_BASE);

			arrFunctions.add("SM Edit Appointment Calendar"); 
			arrFunctionIDs.add(SMEditAppointmentCalendar); 
			arrFunctionLinks.add(""); 
			arrFunctionDescriptions.add("Required to edit the schedule entries");
			arrFunctionModuleLevel.add(SMModuleListing.MODULE_BASE);

			arrFunctions.add("SM Edit Appointment Calendar Groups"); 
			arrFunctionIDs.add(SMEditAppointmentGroups); 
			arrFunctionLinks.add("smcontrolpanel.SMEditAppointmentGroupsSelect"); 
			arrFunctionDescriptions.add("Required to edit the schedule groups");
			arrFunctionModuleLevel.add(SMModuleListing.MODULE_BASE);

			arrFunctions.add("IC Bulk Transfers"); 
			arrFunctionIDs.add(ICBulkTransfers); 
			arrFunctionLinks.add("smic.ICBulkTransferEdit"); 
			arrFunctionDescriptions.add("Used to streamline the process of creating an inventory transfer batch and entering multiple quantities and items");
			arrFunctionModuleLevel.add(SMModuleListing.MODULE_INVENTORYCONTROL);

			arrFunctions.add("SM Send Invoices"); 
			arrFunctionIDs.add(SMSendInvoices); 
			arrFunctionLinks.add("smcontrolpanel.SMSendInvoiceSelection"); 
			arrFunctionDescriptions.add("Used to print and email mutiple invoices.");
			arrFunctionModuleLevel.add(SMModuleListing.MODULE_BASE);

			arrFunctions.add("AS Edit Event Schedules"); 
			arrFunctionIDs.add(ASEditEventSchedules); 
			arrFunctionLinks.add("smas.ASEditEventSchedulesSelect"); 
			arrFunctionDescriptions.add("Use this to add, modify, and delete daily and weekly schedules for the security system: either"
					+ " to automatically set and unset alarm sequences, or to turn on lights, open gates, etc., at a specified time of day or week.");
			arrFunctionModuleLevel.add(SMModuleListing.MODULE_ALARMSYSTEM);
			
			arrFunctions.add("AS List Overridden Device Events"); 
			arrFunctionIDs.add(ASListOverridenDeviceEvents); 
			arrFunctionLinks.add(
					"smcontrolpanel.SMQueryParameters?" 
							+ "QUERYSTRING="
							+ clsServletUtilities.URLEncode(
									"select" 
									+ " " + SMTablessuserevents.TableName + "." + SMTablessuserevents.lid + " AS 'ID'"
									+ ", date_format(" + SMTablessuserevents.TableName + "." + SMTablessuserevents.dattimeoccurrence + ", '%m/%e/%Y %h:%i:%s %p') AS 'Date/Time'" 
									+ ", " + SMTablessuserevents.TableName + "." + SMTablessuserevents.suserfullname + " AS 'User'"
									+ ", " + SMTablessuserevents.TableName + "." + SMTablessuserevents.scomment + " AS 'Comment'"
									+ ", " + SMTablessuserevents.TableName + "." + SMTablessuserevents.lalarmid + " AS 'Sequence ID'"
									+ ", " + SMTablessalarmsequences.TableName + "." + SMTablessalarmsequences.sname + " AS 'Name'"
									+ " from " + SMTablessuserevents.TableName + " LEFT JOIN " + SMTablessalarmsequences.TableName + " ON" 
									+ " " + SMTablessuserevents.TableName + "." + SMTablessuserevents.lalarmid + "=ssalarmsequences.lid"
									+ " where ("
									+ "(" + SMTablessuserevents.TableName + "." + SMTablessuserevents.sreferenceid + " = '[1480452758]')"
									+ ") order by " + SMTablessuserevents.TableName + "." + SMTablessuserevents.dattimeoccurrence + " DESC"
									)
									+ "&QUERYTITLE=" + clsServletUtilities.URLEncode("AS List Overridden Device Events")
									+ "&ALTERNATEROWCOLORS=Y"
									+ "&FONTSIZE=small"
									//+ "&SHOWSQLCOMMAND=Y"
					); 
			arrFunctionDescriptions.add("Use this to list any time anyone set an alarm sequence and had to OVERRIDE a malfunctioning device.");
			arrFunctionModuleLevel.add(SMModuleListing.MODULE_ALARMSYSTEM);
			
			arrFunctions.add("AS Activate/Deactivate Selected Alarm Sequences"); 
			arrFunctionIDs.add(ASActivateSelectedSequences); 
			arrFunctionLinks.add("smas.ASActivateSelectedSequencesEdit"); 
			arrFunctionDescriptions.add("This permission gives the user access to"
					+ " arm or disarm selected alarm sequences.");
			arrFunctionModuleLevel.add(SMModuleListing.MODULE_ALARMSYSTEM);
			
			arrFunctions.add("AP Edit Batches"); 
			arrFunctionIDs.add(APEditBatches); 
			arrFunctionLinks.add("smap.APEditBatchesSelect"); 
			arrFunctionDescriptions.add("This permission gives the user access to"
					+ " create, modify, or delete AP invoice, payment, or adjustment batches.");
			arrFunctionModuleLevel.add(SMModuleListing.MODULE_ACCOUNTSPAYABLE);
			
			arrFunctions.add("AP Clear Posted And Deleted Batches"); 
			arrFunctionIDs.add(APClearPostedAndDeletedBatches); 
			arrFunctionLinks.add("smap.APClearPostedBatchesSelect"); 
			arrFunctionDescriptions.add("This permission gives the user the ability to"
					+ " clear posted or deleted AP batches, based on a cutoff date.");
			arrFunctionModuleLevel.add(SMModuleListing.MODULE_ACCOUNTSPAYABLE);
			
			arrFunctions.add("AP Reset Posting-In-Process Flag"); 
			arrFunctionIDs.add(APResetPostingInProcessFlag); 
			arrFunctionLinks.add("smap.APResetPostingFlagSelect"); 
			arrFunctionDescriptions.add("Required to reset the Accounts Payable posting flag - if a posting process fails in A/P, the system records the user and the process and will not"
					+ " allow any subsequent postings until this flag is cleared.");
			arrFunctionModuleLevel.add(SMModuleListing.MODULE_ACCOUNTSPAYABLE);
			
			arrFunctions.add("AP Control Payments"); 
			arrFunctionIDs.add(APControlPayments); 
			arrFunctionLinks.add("smap.APControlPaymentsSelect"); 
			arrFunctionDescriptions.add("This permission gives the user access to"
					+ " modify the due date, discount date, discount amount, or set invoices on hold.");
			arrFunctionModuleLevel.add(SMModuleListing.MODULE_ACCOUNTSPAYABLE);
			
			arrFunctions.add("AP Aged Payables Report"); 
			arrFunctionIDs.add(APAgedPayables); 
			arrFunctionLinks.add("smap.APAgedPayablesSelect"); 
			arrFunctionDescriptions.add("This permission gives the user access to"
					+ " run the Aged Payables report, showing open vendors' invoices using a range of dates.");
			arrFunctionModuleLevel.add(SMModuleListing.MODULE_ACCOUNTSPAYABLE);
			
			arrFunctions.add("AP View Transaction Information"); 
			arrFunctionIDs.add(APViewTransactionInformation); 
			arrFunctionLinks.add(""); 
			arrFunctionDescriptions.add("This permission gives the user access to"
					+ " view transaction details, usually by means of a link from various places in the system.");
			arrFunctionModuleLevel.add(SMModuleListing.MODULE_ACCOUNTSPAYABLE);
			
			arrFunctions.add("AP Invoices On Hold Query"); 
			arrFunctionIDs.add(APViewInvoicesOnHold); 
			arrFunctionLinks.add("smcontrolpanel.SMQueryParameters?" 
					+ "QUERYSTRING="
					+ clsServletUtilities.URLEncode(
							"SELECT aptransactions.datdocdate AS 'Date'"
							+ " , CONCAT(aptransactions.svendor,'-',icvendors.sname) AS 'Vendor name'"
							+ " , CONCAT("
								+ "'<A HREF=\\*LINKBASE*smap.APViewTransactionInformation?lid=',"
								+ "aptransactions.lid,"
								+ "'&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID + "',"
								+ "'>',"
								+ " aptransactions.sdocnumber,"
								+ " '</A>'"
							+ ")  as 'Invoice #'"
							+ " , aptransactions.bdcurrentamt AS 'Amount'  "
							+ " FROM aptransactions"
							+ " LEFT JOIN icvendors ON aptransactions.svendor = icvendors.svendoracct "
							+ "  WHERE ("
							+ " (aptransactions.ionhold = 1) "
							+ " AND (aptransactions.bdcurrentamt != 0)"
							+ " )"
							+ " ORDER BY aptransactions.datdocdate"
							+ " , icvendors.sname"
							+ " , aptransactions.sdocnumber ")
							+"&QUERYTITLE=" + clsServletUtilities.URLEncode("AP Invoices On Hold")
							+ "&ALTERNATEROWCOLORS=Y"
							+ "&SHOWSQLCOMMAND=Y"
							+ "&FONTSIZE=small"
							);
			arrFunctionDescriptions.add("This show invoices that are on hold");
			arrFunctionModuleLevel.add(SMModuleListing.MODULE_ACCOUNTSPAYABLE);
			
			arrFunctions.add("AP Vendor Transactions Report"); 
			arrFunctionIDs.add(APVendorTransactionsReport); 
			arrFunctionLinks.add("smap.APVendorTransactionsSelect"); 
			arrFunctionDescriptions.add("This permission gives the user access to"
					+ " run the Vendor Transactions report, showing vendors' transactions using a range of dates and other selection criteria.");
			arrFunctionModuleLevel.add(SMModuleListing.MODULE_ACCOUNTSPAYABLE);
			
			arrFunctions.add("AP Edit Check Forms"); 
			arrFunctionIDs.add(APEditCheckForms); 
			arrFunctionLinks.add("smap.APEditCheckFormsSelect"); 
			arrFunctionDescriptions.add("This function is used to design and modify the printed checks that can be used in the system.");
			arrFunctionModuleLevel.add(SMModuleListing.MODULE_ACCOUNTSPAYABLE);
			
			arrFunctions.add("AP Print Checks"); 
			arrFunctionIDs.add(APPrintChecks); 
			arrFunctionLinks.add(""); 
			arrFunctionDescriptions.add("This function is used to print checks from payment batches in Accounts Payable.");
			arrFunctionModuleLevel.add(SMModuleListing.MODULE_ACCOUNTSPAYABLE);

			arrFunctions.add("BK Update Next Check Number"); 
			arrFunctionIDs.add(BKUpdateNextCheckNumber); 
			arrFunctionLinks.add(""); 
			arrFunctionDescriptions.add("This allows a user to set the 'next check number' for a bank account - it may be used, for example, in AP when printing checks.");
			arrFunctionModuleLevel.add(SMModuleListing.MODULE_BANKFUNCTIONS);
			
			arrFunctions.add("AP Clear Fully Paid Transactions"); 
			arrFunctionIDs.add(APClearFullyPaidTransactions); 
			arrFunctionLinks.add("smap.APClearTransactionsSelect"); 
			arrFunctionDescriptions.add("Required to clear fully paid Accounts Payable transactions (vendor transaction history.");
			arrFunctionModuleLevel.add(SMModuleListing.MODULE_ACCOUNTSPAYABLE);
			
			arrFunctions.add("AP Change/Merge Vendor Accounts"); 
			arrFunctionIDs.add(APChangeOrMergeVendorAccounts); 
			arrFunctionLinks.add("smap.APVendorNumberChangeSelect"); 
			arrFunctionDescriptions.add("Allows a user to change a vendor's account number throughout the system - OR to merge two different accounts into one single account.");
			arrFunctionModuleLevel.add(SMModuleListing.MODULE_ACCOUNTSPAYABLE);
			
			arrFunctions.add("AP Delete Inactive Vendors Query"); 
			arrFunctionIDs.add(APDeleteInactiveVendorsQuery); 
			arrFunctionLinks.add(
					"smcontrolpanel.SMQueryParameters?" 
							+ "QUERYSTRING="
							+ clsServletUtilities.URLEncode(
								"select"
								+ " CONCAT("
								+ "   '<A HREF=\\*LINKBASE*smap.APEditVendorAction?svendoracct='"
								+ "   , svendoracct"
								+ "   , '&COMMANDFLAG=DELETEVENDOR'"
								+ "   , '&CONFIRMDELETE=CONFIRMDELETE'"
								+ "   , '&CallingClass=smap.APEditVendorsEdit'"
								+ "   , '&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID + "'"
								
								+ "   , 'Delete'"
								+ "   , '</A>'"
								+ ") as 'DELETE?'"
								
								+ ", CONCAT("
								+ "   '<A HREF=\\*LINKBASE*smap.APDisplayVendorInformation?VendorNumber='"
								+ "   , svendoracct"
								+ "   , '&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID + "'"
								+ "   , svendoracct"
								+ "   , '</A>'"
								+ ") as 'ACCT'"
								
								+ ", sname AS NAME"
								+ ", IF(iactive=0, 'INACTIVE', 'ACTIVE') AS STATUS"
								+ ", TRANSACTIONQUERY.LASTTRANSACTION AS 'LAST TRANSACTION'"
								+ ",CONCAT("
								+ "   '<A HREF=\\*LINKBASE*smap.APViewTransactionInformation'"
								+ "   ,'?lid='"
								+ "   , TRANSACTIONQUERY.lid"
								+ "   , '&sdocnumber='"
								+ "   , TRANSACTIONQUERY.sdocnumber"
								+ "   , '&svendor='"
								+ "   , TRANSACTIONQUERY.svendor"
								+ "   , 'loriginalbatchnumber='"
								+ "   , TRANSACTIONQUERY.loriginalbatchnumber"
								+ "   , 'loriginalentrynumber='"
								+ "   , TRANSACTIONQUERY.loriginalentrynumber"
								+ "   , '&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID + "'"
								+ "   , '>'"
								+ "   , TRANSACTIONQUERY.sdocnumber"
								+ "   , '</A>'"
								+ ") AS 'DOC #'"
								+ " FROM icvendors"
								+ " LEFT JOIN"
								+ " (SELECT"
								+ " MAX(datdocdate) AS LASTTRANSACTION"
								+ ", sdocnumber"
								+ ", svendor"
								+ ", loriginalbatchnumber"
								+ ", loriginalentrynumber"
								+ ", lid"
								+ " FROM aptransactions"
								+ " GROUP BY svendor"
								+ ") AS TRANSACTIONQUERY" 
								+ " ON icvendors.svendoracct=TRANSACTIONQUERY.svendor"
								+ " WHERE ("
								+ "(iactive = 0)"
								+ ") ORDER BY svendoracct"	
							)
								+ "&QUERYTITLE=" + clsServletUtilities.URLEncode("AP Delete Inactive Vendors")
								+ "&ALTERNATEROWCOLORS=Y"
								+ "&FONTSIZE=small"
								//+ "&SHOWSQLCOMMAND=Y"
					); 
			arrFunctionDescriptions.add("This lists the INACTIVE vendors, includes a link to delete each individually, and shows the last transaction for the vendor.");
			arrFunctionModuleLevel.add(SMModuleListing.MODULE_ACCOUNTSPAYABLE);

			arrFunctions.add("AP Check Run Log Query"); 
			arrFunctionIDs.add(APQueryCheckRunLog); 
			arrFunctionLinks.add(
				"smcontrolpanel.SMQueryParameters?" 
					+ "QUERYSTRING="
					+ clsServletUtilities.URLEncode(
						"select" 
						+ " CONCAT("
						+ "     '<B><U>CHECK RUN PROCESSED AT:&nbsp;',"
						+ "     DATE_FORMAT(" + SMTablesystemlog.datloggingtime + ", '%c/%e/%Y %l:%i:%s %p'),"
						+ "     '</U></B><BR>',"
						+ "     REPLACE(" + SMTablesystemlog.mcomment + ", '\\n', '<BR>')"
						+ ") AS 'Check Run Information'"
						+ " from " + SMTablesystemlog.TableName
						+ " WHERE ("
						+ "    (" + SMTablesystemlog.soperation + " = '" + SMLogEntry.LOG_OPERATION_APCHECKRUNPRINTED + "')"
						+ "    AND (" + SMTablesystemlog.datloggingtime + " >= STR_TO_DATE('[[*DATEPICKER*{With check run dates STARTING on:}{TODAY}]] 00:00:00', '%m/%d/%Y %H:%i:%S'))"
						+ "    AND (" + SMTablesystemlog.datloggingtime + "<= STR_TO_DATE('[[*DATEPICKER*{With check run dates ENDING on:}{TODAY}]] 23:59:59', '%m/%d/%Y %H:%i:%S'))"
						+ ")"
						+ " ORDER BY " + SMTablesystemlog.lid
					)	
				+ "&QUERYTITLE=" + clsServletUtilities.URLEncode("AP List Previous Check Runs")
				+ "&ALTERNATEROWCOLORS=Y"
				+ "&FONTSIZE=small"
				//+ "&SHOWSQLCOMMAND=N"
			); 
			arrFunctionDescriptions.add("This lists the attempted check runs, regardless of whether the checks were actually finalized, within a range of selected dates.");
			arrFunctionModuleLevel.add(SMModuleListing.MODULE_ACCOUNTSPAYABLE);
			
			arrFunctions.add("AP Reverse Posted Checks"); 
			arrFunctionIDs.add(APReverseChecks); 
			arrFunctionLinks.add(""); 
			arrFunctionDescriptions.add("This permission allows a user to create and edit batches of check REVERSALS, in case a posted check needs to be voided.");
			arrFunctionModuleLevel.add(SMModuleListing.MODULE_ACCOUNTSPAYABLE);
			
			arrFunctions.add("SM Edit Users Custom Links"); 
			arrFunctionIDs.add(SMEditUsersCustomLinks); 
			arrFunctionLinks.add("smcontrolpanel.SMEditUsersCustomLinksEdit"); 
			arrFunctionDescriptions.add("This permission allows editing of users custom links that display on the main menu.");
			arrFunctionModuleLevel.add(SMModuleListing.MODULE_BASE);
			
			arrFunctions.add("SM Edit Doing Business As Addresses"); 
			arrFunctionIDs.add(SMEditDoingBusinessAsAddresses); 
			arrFunctionLinks.add("smcontrolpanel.SMEditDoingBusinessAsAddressSelection"); 
			arrFunctionDescriptions.add("This permission allows editing of 'Doing Business As Addresses' on the main menu.");
			arrFunctionModuleLevel.add(SMModuleListing.MODULE_BASE);
			
			arrFunctions.add("SM Zero Work Order Item Prices"); 
			arrFunctionIDs.add(SMZeroWorkOrderItemPrices); 
			arrFunctionLinks.add(""); 
			arrFunctionDescriptions.add("This permission allows the option to set item prices to zero on items that are added to a work order. ");
			arrFunctionModuleLevel.add(SMModuleListing.MODULE_BASE);
			
			arrFunctions.add("AP Batch Posting Journal"); 
			arrFunctionIDs.add(APBatchPostingJournal); 
			arrFunctionLinks.add(
				"smcontrolpanel.SMQueryParameters?" 
					+ SMQuerySelect.PARAM_SYSTEMQUERYID + "=" + SMSystemQueries.APQUERY_BATCH_POSTING_JOURNAL_INDEX
				+ "&QUERYTITLE=" + clsServletUtilities.URLEncode("AP Batch Posting Journal")
				//+ "&ALTERNATEROWCOLORS=Y"
				//+ "&FONTSIZE=small"
				//+ "&SHOWSQLCOMMAND=Y"
			); 
			arrFunctionDescriptions.add("This lists a selected range of posted or deleted batches, and also allows the user to select INVOICE, PAYMENT, REVERSAL, or ALL types of batches.");
			arrFunctionModuleLevel.add(SMModuleListing.MODULE_ACCOUNTSPAYABLE);
			
			arrFunctions.add("GL Edit General Ledger Options");
			arrFunctionIDs.add(GLEditGLOptions);
			arrFunctionLinks.add("smgl.GLEditGLOptions"); 
			arrFunctionDescriptions.add("Required to access the main administrative function for maintaining General Ledger options.");
			arrFunctionModuleLevel.add(SMModuleListing.MODULE_GENERALLEDGER);
			
			arrFunctions.add("GL Convert ACCPAC Data"); 
			arrFunctionIDs.add(GLConvertACCPACData); 
			arrFunctionLinks.add("smgl.GLConvertACCPAC"); 
			arrFunctionDescriptions.add("A ONE TIME function to convert ACCPAC GL data to SMCP");
			arrFunctionModuleLevel.add(SMModuleListing.MODULE_GENERALLEDGER);
			
			arrFunctions.add("GL Edit Account Segments");
			arrFunctionIDs.add(GLEditAccountSegments);
			arrFunctionLinks.add("smgl.GLAccountSegmentSelect"); 
			arrFunctionDescriptions.add("Required to maintain General Ledger Account Segments.");
			arrFunctionModuleLevel.add(SMModuleListing.MODULE_GENERALLEDGER);
			
			arrFunctions.add("GL Edit Account Segment Values");
			arrFunctionIDs.add(GLEditAcctSegmentValues);
			arrFunctionLinks.add("smgl.GLAcctSegmentValueSelect"); 
			arrFunctionDescriptions.add("Required to maintain General Ledger Account Segment possible values.");
			arrFunctionModuleLevel.add(SMModuleListing.MODULE_GENERALLEDGER);
			
			arrFunctions.add("GL Edit Account Structures");
			arrFunctionIDs.add(GLEditAccountStructures);
			arrFunctionLinks.add("smgl.GLAccountStructureSelect"); 
			arrFunctionDescriptions.add("Required to maintain General Ledger Account Structures.");
			arrFunctionModuleLevel.add(SMModuleListing.MODULE_GENERALLEDGER);
			
			arrFunctions.add("GL Edit Account Groups");
			arrFunctionIDs.add(GLEditAccountGroups);
			arrFunctionLinks.add("smgl.GLAccountGroupSelect"); 
			arrFunctionDescriptions.add("Required to maintain General Ledger Account Groups.");
			arrFunctionModuleLevel.add(SMModuleListing.MODULE_GENERALLEDGER);
			
			arrFunctions.add("GL Edit Fiscal Periods");
			arrFunctionIDs.add(GLEditFiscalPeriods);
			arrFunctionLinks.add("smgl.GLEditFiscalPeriodsSelect"); 
			arrFunctionDescriptions.add("Used to add new fiscal years and periods befoe beginning a new year.");
			arrFunctionModuleLevel.add(SMModuleListing.MODULE_GENERALLEDGER);
						
			arrFunctions.add("SM Import Data");
			arrFunctionIDs.add(SMImportData);
			arrFunctionLinks.add("smcontrolpanel.SMImportDataSelect"); 
			arrFunctionDescriptions.add("Required to access the Upload SQL CSV Command function, which uploads the csv file onto the database.  This should be highly restricted");
			arrFunctionModuleLevel.add(SMModuleListing.MODULE_BASE);
			
			arrFunctions.add("SM Edit Server Settings");
			arrFunctionIDs.add(SMEditServerSettingsFile);
			arrFunctionLinks.add("smcontrolpanel.SMServerSettingsEdit"); 
			arrFunctionDescriptions.add("Allows user to edit the server settings file on the server. ");
			arrFunctionModuleLevel.add(SMModuleListing.MODULE_BASE);
			
			arrFunctions.add("SM List Logging Operations");
			arrFunctionIDs.add(SMDisplayLoggingOperations);
			arrFunctionLinks.add("smcontrolpanel.SMDisplayLoggingOperations"); 
			arrFunctionDescriptions.add("List all the logging operations; useful for building queries to track who used which function, when, and how. ");
			arrFunctionModuleLevel.add(SMModuleListing.MODULE_BASE);
			
			arrFunctions.add("GL Query GL Financial Information");
			arrFunctionIDs.add(GLQueryFinancialInformation);
			arrFunctionLinks.add(""); 
			arrFunctionDescriptions.add("Allows or denies a user the ability to run queries on GL financial data - this permission is required to run financial statements. ");
			arrFunctionModuleLevel.add(SMModuleListing.MODULE_GENERALLEDGER);
			
			arrFunctions.add("GL Trial Balance");
			arrFunctionIDs.add(GLTrialBalance);
			arrFunctionLinks.add("smgl.GLTrialBalanceSelect"); 
			arrFunctionDescriptions.add("Print the General Ledger Trial Balance. ");
			arrFunctionModuleLevel.add(SMModuleListing.MODULE_GENERALLEDGER);

			arrFunctions.add("GL Transaction Listing");
			arrFunctionIDs.add(GLTransactionListing);
			arrFunctionLinks.add("smgl.GLTransactionListingSelect"); 
			arrFunctionDescriptions.add("Print a selected list of transactions from the General Ledger. ");
			arrFunctionModuleLevel.add(SMModuleListing.MODULE_GENERALLEDGER);
			
			arrFunctions.add("GL Edit Batches");
			arrFunctionIDs.add(GLEditBatches);
			arrFunctionLinks.add("smgl.GLEditBatchesSelect"); 
			arrFunctionDescriptions.add("Add, modify, or delete GL batches. ");
			arrFunctionModuleLevel.add(SMModuleListing.MODULE_GENERALLEDGER);
			
			arrFunctions.add("GL Clear Posted And Deleted Batches"); 
			arrFunctionIDs.add(GLClearPostedAndDeletedBatches); 
			arrFunctionLinks.add("smgl.GLClearPostedBatchesSelect"); 
			arrFunctionDescriptions.add("This permission gives the user the ability to"
					+ " clear posted or deleted GL batches, based on a cutoff date.");
			arrFunctionModuleLevel.add(SMModuleListing.MODULE_GENERALLEDGER);
			
			arrFunctions.add("GL Clear Transactions"); 
			arrFunctionIDs.add(GLClearTransactions); 
			arrFunctionLinks.add("smgl.GLClearTransactionsSelect"); 
			arrFunctionDescriptions.add("This permission gives the user the ability to"
					+ " clear GL transactions, based on a cutoff date.");
			arrFunctionModuleLevel.add(SMModuleListing.MODULE_GENERALLEDGER);
			
			arrFunctions.add("GL Clear Fiscal Data"); 
			arrFunctionIDs.add(GLClearFiscalData); 
			arrFunctionLinks.add("smgl.GLClearFiscalDataSelect"); 
			arrFunctionDescriptions.add("This permission gives the user the ability to"
					+ " clear GL fiscal and financial data, based on a cutoff date.");
			arrFunctionModuleLevel.add(SMModuleListing.MODULE_GENERALLEDGER);
			
			arrFunctions.add("FA Edit System Options"); 
			arrFunctionIDs.add(FAEditOptions); 
			arrFunctionLinks.add("smfa.FAEditOptionsEdit"); 
			arrFunctionDescriptions.add("This permission gives the user the ability to"
					+ " set the Fixed Assets system options.");
			arrFunctionModuleLevel.add(SMModuleListing.MODULE_FIXEDASSETS);
			
			arrFunctions.add("GL Import Batches"); 
			arrFunctionIDs.add(GLImportBatches); 
			arrFunctionLinks.add(""); 
			arrFunctionDescriptions.add("This permission gives the user the ability to"
					+ " import CSV files from other accounting systems into new GL batches.");
			arrFunctionModuleLevel.add(SMModuleListing.MODULE_GENERALLEDGER);
			
			arrFunctions.add("GL Reset Posting-In-Process Flag"); 
			arrFunctionIDs.add(GLResetPostingInProcessFlag); 
			arrFunctionLinks.add("smgl.GLResetPostingFlagSelect"); 
			arrFunctionDescriptions.add("Required to reset the General Ledger posting flag - if a posting process fails in GL, the system records the user and the process and will not"
					+ " allow any subsequent postings until this flag is cleared.");
			arrFunctionModuleLevel.add(SMModuleListing.MODULE_GENERALLEDGER);
			
			arrFunctions.add("GL Pull External Companies Into Consolidation"); 
			arrFunctionIDs.add(GLPullExternalDataIntoConsolidation); 
			arrFunctionLinks.add("smgl.GLPullIntoConsolidationSelect"); 
			arrFunctionDescriptions.add("Pulls GL transactions from a designated external comoany (database)");
			arrFunctionModuleLevel.add(SMModuleListing.MODULE_GENERALLEDGER);
			
			arrFunctions.add("GL Manage External Companies"); 
			arrFunctionIDs.add(GLManageExternalCompanies); 
			arrFunctionLinks.add("smgl.GLEditExternalCompaniesEdit"); 
			arrFunctionDescriptions.add("Used to manage external companies so that they can be 'pulled' into a consolidated company.");
			arrFunctionModuleLevel.add(SMModuleListing.MODULE_GENERALLEDGER);
			
			arrFunctions.add("GL List Previous External Company Pulls");
			arrFunctionIDs.add(GLListPreviousExternalCompanyPulls);
			arrFunctionLinks.add(
					"smcontrolpanel.SMQueryParameters?" 
							+ "QUERYSTRING="
							+ clsServletUtilities.URLEncode(
									"select"
											+ " DATE_FORMAT(" + SMTableglexternalcompanypulls.dattimepulldate + ", '%c/%e/%Y %l:%i:%s %p') as 'Time'"
											+ ", " + SMTableglexternalcompanypulls.sfullusername + " as 'Name'"
											+ ", " + SMTableglexternalcompanypulls.scompanyname + " as 'Pulled From'"
											+ ", " + SMTableglexternalcompanypulls.ifiscalyear + " as 'Fiscal Year'"
											+ ", " + SMTableglexternalcompanypulls.ifiscalperiod + " as 'Fiscal Period'"
											+ " FROM " + SMTableglexternalcompanypulls.TableName
											+ " ORDER BY " + SMTableglexternalcompanypulls.dattimepulldate + " DESC"
									)
									+ "&QUERYTITLE=" + clsServletUtilities.URLEncode("GL List Previous External Company Pulls")
									+ "&ALTERNATEROWCOLORS=Y"
									+ "&FONTSIZE=small"
									//+ "&SHOWSQLCOMMAND=Y"
									//				Possible parameters:				
									//				public static String PARAM_EXPORTOPTIONS = "EXPORTOPTIONS";
									//				public static String EXPORT_COMMADELIMITED_VALUE = "COMMADELIMITED";
									//				public static String EXPORT_HTML_VALUE = "HTML";
									//				public static String EXPORT_NOEXPORT_VALUE = "NOEXPORT";
									//				public static String EXPORT_COMMADELIMITED_LABEL = "Comma delimited file";
									//				public static String EXPORT_HTML_LABEL = "HTML (web page) file";
									//				public static String EXPORT_NOEXPORT_LABEL = "Do not export - display on screen";
									//				public static String PARAM_QUERYID = "QUERYID";
									//				public static String PARAM_QUERYTITLE = "QUERYTITLE";
									//				public static String PARAM_QUERYSTRING = "QUERYSTRING";
									//				public static String PARAM_PWFORQUICKLINK = "PWFORQUICKLINK";
									//				public static String PARAM_FONTSIZE = "FONTSIZE";
									//				public static String PARAM_INCLUDEBORDER = "INCLUDEBORDER";
									//				public static String PARAM_ALTERNATEROWCOLORS = "ALTERNATEROWCOLORS";
									//				public static String PARAM_TOTALNUMERICFIELDS = "TOTALNUMERICFIELDS";
									//				public static String PARAM_SHOWSQLCOMMAND = "SHOWSQLCOMMAND";
					); 
			arrFunctionDescriptions.add("Lists the previous 'pulls' from an external company into this company's GL.");
			arrFunctionModuleLevel.add(SMModuleListing.MODULE_GENERALLEDGER);
			
			arrFunctions.add("GL Close Fiscal Year"); 
			arrFunctionIDs.add(GLCloseFiscalYear); 
			arrFunctionLinks.add("smgl.GLCloseFiscalYearEdit"); 
			arrFunctionDescriptions.add("Automatically creates journal entries to close the fiscal year.");
			arrFunctionModuleLevel.add(SMModuleListing.MODULE_GENERALLEDGER);
			
			
	}

	public String getSecurityFunction(int iIndex){
		return arrFunctions.get(iIndex);
	}
	public Long getSecurityFunctionID(int iIndex){
		return arrFunctionIDs.get(iIndex);
	}
	public String getSecurityFunctionLink(int iIndex){
		return arrFunctionLinks.get(iIndex);
	}
	public String getSecurityFunctionDescription(int iIndex){
		return arrFunctionDescriptions.get(iIndex);
	}
	public String getSecurityFunctionModuleLevel(int iIndex){
		return Long.toString(arrFunctionModuleLevel.get(iIndex));
	}
	public int getSecurityFunctionCount(){
		return arrFunctions.size();
	}

	public static boolean isFunctionPermitted(
			long lFunctionID, 
			String sUserID, 
			ServletContext context,
			String sDBID,
			String sLicenseModuleLevel
			){

		//If the function ID is -1, pass it through as true:
		if (lFunctionID == -1L){
			return true;
		}
		if (sUserID == null){
			return false;
		}
		 
		String SQL = "";
		try{
			SQL = "SELECT" 
					+ " " + SMTablesecuritygroupfunctions.TableName + "." + SMTablesecuritygroupfunctions.ifunctionid
					+ " FROM " + SMTablesecuritygroupfunctions.TableName
					+ " LEFT JOIN " + SMTablesecurityusergroups.TableName + " ON"
					+ " " + SMTablesecuritygroupfunctions.TableName + "." + SMTablesecuritygroupfunctions.sGroupName + " = "
					+ SMTablesecurityusergroups.TableName + "." + SMTablesecurityusergroups.sSecurityGroupName
					+ " LEFT JOIN " + SMTablesecurityfunctions.TableName
					+ " ON " + SMTablesecurityfunctions.TableName + "." + SMTablesecurityfunctions.iFunctionID
					+ " = " + SMTablesecuritygroupfunctions.TableName + "." + SMTablesecuritygroupfunctions.ifunctionid
					+ " WHERE ("
					+ "(" + SMTablesecuritygroupfunctions.TableName + "." 
					+ SMTablesecuritygroupfunctions.ifunctionid + "=" + Long.toString(lFunctionID) + ")"

					+ " AND (" + SMTablesecurityusergroups.TableName + "." + SMTablesecurityusergroups.luserid
					+ " = " + sUserID + ")"
					;
			if (sLicenseModuleLevel.compareToIgnoreCase("") != 0){
				SQL += " AND ((" + SMTablesecurityfunctions.imodulelevelsum + " & " + sLicenseModuleLevel + ") > 0)";
			}
			SQL += ")"
					;

			ResultSet rs = clsDatabaseFunctions.openResultSet(
					SQL, 
					context, 
					sDBID, 
					"MySQL", 
					"SMSystemFunctions.isFunctionPermitted [1332425427] SQL: " + SQL
					);

			if (rs.next()){
				rs.close();
				return true;
			}
			rs.close();

		}catch (SQLException e){
			System.out.println("Error [1387902086] reading function permissions in SMSystemFunctions.isFunctionPermitted with SQL: " 
					+ SQL + " - " + e.getMessage());
		}

		return false;
	}

	public static boolean isFunctionPermitted(
			long lFunctionID, 
			String sUserID, 
			Connection conn,
			String sLicenseModuleLevel
			){
		
		String SQL = "";
		try{
			SQL = "SELECT" 
					+ " " + SMTablesecuritygroupfunctions.TableName + "." + SMTablesecuritygroupfunctions.ifunctionid
					+ " FROM " + SMTablesecuritygroupfunctions.TableName
					+ " LEFT JOIN " + SMTablesecurityusergroups.TableName + " ON"
					+ " " + SMTablesecuritygroupfunctions.TableName + "." + SMTablesecuritygroupfunctions.sGroupName + " = "
					+ SMTablesecurityusergroups.TableName + "." + SMTablesecurityusergroups.sSecurityGroupName
					+ " LEFT JOIN " + SMTablesecurityfunctions.TableName
					+ " ON " + SMTablesecurityfunctions.TableName + "." + SMTablesecurityfunctions.iFunctionID
					+ " = " + SMTablesecuritygroupfunctions.TableName + "." + SMTablesecuritygroupfunctions.ifunctionid
					+ " WHERE ("
					+ "(" + SMTablesecuritygroupfunctions.TableName + "." 
					+ SMTablesecuritygroupfunctions.ifunctionid + "=" + Long.toString(lFunctionID) + ")"

				+ " AND (" + SMTablesecurityusergroups.TableName + "." + SMTablesecurityusergroups.luserid
				+ " = " + sUserID + ")"
				;
			if (sLicenseModuleLevel.compareToIgnoreCase("") != 0){
				SQL += " AND ((" + SMTablesecurityfunctions.imodulelevelsum + " & " + sLicenseModuleLevel + ") > 0)";
			}
			SQL += ")"
					;
			//System.out.println("[1467383015] - SQL: " + SQL);
			ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);

			if (rs.next()){
				rs.close();
				return true;
			}
			rs.close();

		}catch (SQLException e){
			System.out.println("Error [1387902087] reading function permissions in SMSystemFunctions.isFunctionPermitted with SQL: " + SQL + " - " + e.getMessage());
		}

		return false;
	}
}
