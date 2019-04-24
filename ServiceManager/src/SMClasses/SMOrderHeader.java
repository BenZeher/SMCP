package SMClasses;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import smar.ARCustomer;
import smar.AROptions;
import smar.SMOption;
import smcontrolpanel.SMBidEntry;
import smcontrolpanel.SMEditOrderEdit;
import smcontrolpanel.SMGeocoder;
import smcontrolpanel.SMImportWorkOrdersEdit;
import smcontrolpanel.SMOrderDetailList;
import smcontrolpanel.SMSalesOrderTaxCalculator;
import smcontrolpanel.SMSystemFunctions;
import smcontrolpanel.SMUtilities;
import smic.ICItem;
import SMDataDefinition.SMTablearcustomer;
import SMDataDefinition.SMTablearcustomershiptos;
import SMDataDefinition.SMTableartransactions;
import SMDataDefinition.SMTablechangeorders;
import SMDataDefinition.SMTablecompanyprofile;
import SMDataDefinition.SMTabledefaultitemcategories;
import SMDataDefinition.SMTabledoingbusinessasaddresses;
import SMDataDefinition.SMTableiccategories;
import SMDataDefinition.SMTableicitemprices;
import SMDataDefinition.SMTableicitems;
import SMDataDefinition.SMTableinvoicedetails;
import SMDataDefinition.SMTableinvoiceheaders;
import SMDataDefinition.SMTablelocations;
import SMDataDefinition.SMTablemechanics;
import SMDataDefinition.SMTableorderdetails;
import SMDataDefinition.SMTableorderheaders;
import SMDataDefinition.SMTableordersources;
import SMDataDefinition.SMTablepricelistcodes;
import SMDataDefinition.SMTablesalesperson;
import SMDataDefinition.SMTablesecuritygroupfunctions;
import SMDataDefinition.SMTablesecurityusergroups;
import SMDataDefinition.SMTableservicetypes;
import SMDataDefinition.SMTablesmoptions;
import SMDataDefinition.SMTablessorderheaders;
import SMDataDefinition.SMTabletax;
import SMDataDefinition.SMTableusers;
import ServletUtilities.clsMasterEntry;
import ServletUtilities.clsServletUtilities;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsDateAndTimeConversions;
import ServletUtilities.clsManageBigDecimals;
import ServletUtilities.clsManageRequestParameters;
import ServletUtilities.clsStringFunctions;
import ServletUtilities.clsValidateFormFields;

public class SMOrderHeader extends clsMasterEntry{

	public static final String ParamObjectName = "Order";
	public static final String ORDERFIELDINFO_ENTRY = "ORDERFIELDINFO";

	//This is the code used when an order is retrieved for a salesperson that is no longer in the list of salespeople:
	public static final String UNLISTEDSALESPERSON_MARKER = "N/A";
	
	//Particular to the specific class
	public static final String ParamsiID = "id";
	public static final String ParamsOrderNumber = "OrderNumber";
	public static final String ParamsBillToName = "BillToName";
	public static final String ParamsShipToName = "ShipToName";
	public static final String ParamsServiceTypeCode = "ServiceTypeCode";
	public static final String ParammDirections = "mDirections";
	public static final String ParammTicketComments = "mTicketComments";
	public static final String ParammFieldNotes = "mFieldNotes";
	public static final String ParamsBillToContact = "BillToContact";
	public static final String ParamsShipToContact = "ShipToContact";
	public static final String ParamsBillToPhone = "BillToPhone";
	public static final String ParamsShipToPhone = "ShipToPhone";
	public static final String Parambdtruckdays = "bdtruckdays";
	public static final String Parambddepositamount = "bddepositamount";
	public static final String Paramscarpenterrate = "scarpenterrate";
	public static final String Paramslaborerrate = "slaborerrate";
	public static final String Paramselectricianrate = "selectricianrate";
	public static final String Parambdtotalmarkup = "bdtotalmarkup";
	public static final String Parambdtotalcontractamount = "bdtotalcontractamount";
	public static final String Paramdatwarrantyexpiration = "datwarrantyexpiration";
	public static final String Paramswagescalenotes = "swagescalenotes";
	public static final String Paramssecondarybilltophone = "ssecondarybilltophone";
	public static final String Paramssecondaryshiptophone = "ssecondaryshiptophone";
	public static final String Paramstrimmedordernumber = "strimmedordernumber";
	
	//The rest of the order header fields are listed here:
	public static final String ParamsCustomerCode = "sCustomerCode";
	public static final String ParamsBillToAddressLine1 = "sBillToAddressLine1";
	public static final String ParamsBillToAddressLine2 = "sBillToAddressLine2";
	public static final String ParamsBillToAddressLine3 = "sBillToAddressLine3";
	public static final String ParamsBillToAddressLine4 = "sBillToAddressLine4";
	public static final String ParamsBillToCity = "sBillToCity";
	public static final String ParamsBillToState = "sBillToState";
	public static final String ParamsBillToZip = "sBillToZip";
	public static final String ParamsBillToCountry = "sBillToCountry";
	public static final String ParamsBillToFax = "sBillToFax";
	public static final String ParamsShipToCode = "sShipToCode";
	public static final String ParamsShipToAddress1 = "sShipToAddress1";
	public static final String ParamsShipToAddress2 = "sShipToAddress2";
	public static final String ParamsShipToAddress3 = "sShipToAddress3";
	public static final String ParamsShipToAddress4 = "sShipToAddress4";
	public static final String ParamsShipToCity = "sShipToCity";
	public static final String ParamsShipToState = "sShipToState";
	public static final String ParamsShipToZip = "sShipToZip";
	public static final String ParamsShipToCountry = "sShipToCountry";
	public static final String ParamsShipToFax = "sShipToFax";
	public static final String ParamiCustomerDiscountLevel = "iCustomerDiscountLevel";
	public static final String ParamsDefaultPriceListCode = "sDefaultPriceListCode";
	public static final String ParamsPONumber = "sPONumber";
	public static final String ParamsSpecialWageRate = "sSpecialWageRate";
	public static final String ParamsTerms = "sTerms";
	public static final String ParamiOrderType = "iOrderType";
	public static final String ParamdatOrderDate = "datOrderDate";
	public static final String ParamdatExpectedShipDate = "datExpectedShipDate";
	public static final String ParamdatOrderCreationDate = "datOrderCreationDate";
	public static final String ParamsServiceTypeCodeDescription = "sServiceTypeCodeDescription";
	public static final String ParamsLastInvoiceNumber = "sLastInvoiceNumber";
	public static final String ParamiNumberOfInvoices = "iNumberOfInvoices";
	public static final String ParamsLocation = "sLocation";
	public static final String ParamiOnHold = "iOnHold";
	public static final String ParamdatLastPostingDate = "datLastPostingDate";
	public static final String ParamdTotalAmountItems = "dTotalAmountItems";
	public static final String ParamiNumberOfLinesOnOrder = "iNumberOfLinesOnOrder";
	public static final String ParamsSalesperson = "sSalesperson";
	public static final String ParamsOrderCreatedByFullName = "sOrderCreatedByFullName";
	public static final String ParamlOrderCreatedByID = "lOrderCreatedByID";
	public static final String ParamsTaxJurisdiction = "sTaxJurisdiction";
	public static final String ParamsDefaultItemCategory = "sDefaultItemCategory";
	public static final String ParamdTaxBase = "dTaxBase";
	public static final String ParamdOrderTaxAmount = "dOrderTaxAmount";
	public static final String ParamiNextDetailNumber = "iNextDetailNumber";
	public static final String ParammInternalComments = "mInternalComments";
	public static final String ParammInvoiceComments = "mInvoiceComments";
	public static final String ParamdPrePostingInvoiceDiscountPercentage = "dPrePostingInvoiceDiscountPercentage";
	public static final String ParamdPrePostingInvoiceDiscountAmount = "dPrePostingInvoiceDiscountAmount";
	public static final String ParamLASTEDITUSERFULLNAME = "LASTEDITUSERFULLNAME";
	public static final String ParamLASTEDITUSERID = "LASTEDITUSERID";
	public static final String ParamLASTEDITPROCESS = "LASTEDITPROCESS";
	public static final String ParamLASTEDITDATE = "LASTEDITDATE";
	public static final String ParamLASTEDITTIME = "LASTEDITTIME";
	public static final String ParamsCustomerControlAcctSet = "sCustomerControlAcctSet";
	public static final String ParamsPrePostingInvoiceDiscountDesc = "sPrePostingInvoiceDiscountDesc";
	public static final String ParamdatOrderCanceledDate = "datOrderCanceledDate";
	public static final String ParamiOrderSourceID = "iOrderSourceID";
	public static final String ParamsOrderSourceDesc = "sOrderSourceDesc";
	public static final String ParamdEstimatedHour = "dEstimatedHour";
	public static final String ParamsEmailAddress = "sEmailAddress"; 
	public static final String ParamiSalesGroup = "iSalesGroup";
	public static final String ParamsClonedFrom = "sClonedFrom";
	public static final String Paramsgeocode = "sgeocode";
	public static final String Paramsshiptoemail = "sshiptoemail";
	public static final String Paramsgdoclink = "sgdoclink";
	public static final String Paramlbidid = "lbidid";
	public static final String Paramsquotedescription = "squotedescription";
	public static final String Paramitaxid = "itaxid";
	public static final String Paramstaxtype = "staxtype";
	public static final String ParamsInvoicingEmailAddress = "sInvoicingEmailAddress"; 
	public static final String ParamsInvoicingContact = "sInvoicingContact"; 
	public static final String ParamsInvoicingNotes = "sInvoicingNotes"; 
	public static final String Paramidoingbusinessasaddress = "idoingbusinessasaddress";
	
	private String m_siID;
	private String m_sOrderNumber;
	private String m_sBillToName;
	private String m_sShipToName;
	private String m_sDirections;
	private String m_sTicketComments;
	private String m_sFieldNotes;
	private String m_sServiceTypeCode;
	private String m_bdtruckdays;
	private String m_bddepositamount;
	private String m_scarpenterrate;
	private String m_slaborerrate;
	private String m_selectricianrate;
	private String m_bdtotalmarkup;
	private String m_bdtotalcontractamount;
	private String m_datwarrantyexpiration;
	private String m_swagescalenotes;
	private String m_ssecondarybilltophone;
	private String m_ssecondaryshiptophone;
	private String m_sshiptophone;
	private String m_sbilltophone;
	private String m_sbilltocontact;
	private String m_sshiptocontact;
	private String m_strimmedordernumber;
	
	//The rest of the order header fields:
	private String m_sCustomerCode;
	private String m_sBillToAddressLine1;
	private String m_sBillToAddressLine2;
	private String m_sBillToAddressLine3;
	private String m_sBillToAddressLine4;
	private String m_sBillToCity;
	private String m_sBillToState;
	private String m_sBillToZip;
	private String m_sBillToCountry;
	private String m_sBillToFax;
	private String m_sShipToCode;
	private String m_sShipToAddress1;
	private String m_sShipToAddress2;
	private String m_sShipToAddress3;
	private String m_sShipToAddress4;
	private String m_sShipToCity;
	private String m_sShipToState;
	private String m_sShipToZip;
	private String m_sShipToCountry;
	private String m_sShipToFax;
	private String m_iCustomerDiscountLevel;
	private String m_sDefaultPriceListCode;
	private String m_sPONumber;
	private String m_sSpecialWageRate;
	private String m_sTerms;
	private String m_iOrderType;
	private String m_datOrderDate;
	private String m_datExpectedShipDate;
	private String m_datOrderCreationDate;
	private String m_sServiceTypeCodeDescription;
	private String m_sLastInvoiceNumber;
	private String m_iNumberOfInvoices;
	private String m_sLocation;
	private String m_iOnHold;
	private String m_datLastPostingDate;
	private String m_dTotalAmountItems;
	private String m_iNumberOfLinesOnOrder;
	private String m_sSalesperson;
	private String m_sOrderCreatedByFullName;
	private String m_lOrderCreatedByID;
	private String m_staxjurisdiction;
	private String m_sDefaultItemCategory;
	//private String m_iTaxClass;
	private String m_dTaxBase;
	private String m_sordersalestaxamount;
	private String m_iNextDetailNumber;
	private String m_mInternalComments;
	private String m_mInvoiceComments;
	private String m_dPrePostingInvoiceDiscountPercentage;
	private String m_dPrePostingInvoiceDiscountAmount;
	private String m_LASTEDITUSERFULLNAME;
	private String m_LASTEDITUSERID;
	private String m_LASTEDITPROCESS;
	private String m_LASTEDITDATE;
	private String m_LASTEDITTIME;
	private String m_sCustomerControlAcctSet;
	private String m_sPrePostingInvoiceDiscountDesc;
	private String m_datOrderCanceledDate;
	private String m_iOrderSourceID;
	private String m_sOrderSourceDesc;
	private String m_dEstimatedHour;
	private String m_sEmailAddress; 
	private String m_iSalesGroup;
	private String m_sClonedFrom;
	private String m_sgeocode;
	private String m_sshiptoemail;
	private String m_sgdoclink;
	private String m_sbidid;
	private String m_squotedescription;
	private String m_itaxid;
	private String m_staxtype;
	private String m_sinvoicingemailaddress;
	private String m_sinvoicingcontact;
	private String m_sinvoicingnotes;
	private String m_idoingbusinessasaddressid;
	
	//Variables for determining order billing values:
	private BigDecimal m_bdTotalBilled;
	private BigDecimal m_bdChangeOrderTotal;
	private BigDecimal m_bdOriginalContractAmount;
	private BigDecimal m_bdTotalContractAmtRemaining;
	private BigDecimal m_bdRemainingAmtDifference;
	private BigDecimal m_bdRemainingOrderedLineTotal;
	private BigDecimal m_bdRemainingShippedLineTotal;
	
	private boolean bDebugMode = false;
	
	private ArrayList<SMOrderDetail> m_arrDetails = new ArrayList<SMOrderDetail> (0);
	
	public SMOrderHeader() {
		super();
		initOrderVariables();
	}

	public SMOrderHeader (HttpServletRequest req){
		super(req);
		initOrderVariables();

		m_siID = clsManageRequestParameters.get_Request_Parameter(SMOrderHeader.ParamsiID, req).trim();
		m_bdtruckdays = clsManageRequestParameters.get_Request_Parameter(SMOrderHeader.Parambdtruckdays, req).trim();
		m_bddepositamount = clsManageRequestParameters.get_Request_Parameter(SMOrderHeader.Parambddepositamount, req).trim();
		m_scarpenterrate = clsManageRequestParameters.get_Request_Parameter(SMOrderHeader.Paramscarpenterrate, req).trim();
		m_slaborerrate = clsManageRequestParameters.get_Request_Parameter(SMOrderHeader.Paramslaborerrate, req).trim();
		m_selectricianrate = clsManageRequestParameters.get_Request_Parameter(SMOrderHeader.Paramselectricianrate, req).trim();
		m_bdtotalmarkup = clsManageRequestParameters.get_Request_Parameter(SMOrderHeader.Parambdtotalmarkup, req).trim();
		m_bdtotalcontractamount = clsManageRequestParameters.get_Request_Parameter(SMOrderHeader.Parambdtotalcontractamount, req).trim();
		m_datwarrantyexpiration = clsManageRequestParameters.get_Request_Parameter(SMOrderHeader.Paramdatwarrantyexpiration, req).trim();
		m_swagescalenotes = clsManageRequestParameters.get_Request_Parameter(SMOrderHeader.Paramswagescalenotes, req).trim();
		m_ssecondarybilltophone = clsManageRequestParameters.get_Request_Parameter(SMOrderHeader.Paramssecondarybilltophone, req).trim();
		m_ssecondaryshiptophone = clsManageRequestParameters.get_Request_Parameter(SMOrderHeader.Paramssecondaryshiptophone, req).trim();
		m_sshiptophone = clsManageRequestParameters.get_Request_Parameter(SMOrderHeader.ParamsShipToPhone, req).trim();
		m_sbilltophone = clsManageRequestParameters.get_Request_Parameter(SMOrderHeader.ParamsBillToPhone, req).trim();
		m_sbilltocontact = clsManageRequestParameters.get_Request_Parameter(SMOrderHeader.ParamsBillToContact, req).trim();
		m_sshiptocontact = clsManageRequestParameters.get_Request_Parameter(SMOrderHeader.ParamsShipToContact, req).trim();
		m_sServiceTypeCode = clsManageRequestParameters.get_Request_Parameter(SMOrderHeader.ParamsServiceTypeCode, req).trim();
		m_strimmedordernumber = clsManageRequestParameters.get_Request_Parameter(SMOrderHeader.Paramstrimmedordernumber, req);
		m_sOrderNumber = clsManageRequestParameters.get_Request_Parameter(SMOrderHeader.ParamsOrderNumber, req).trim();
		m_sBillToName = clsManageRequestParameters.get_Request_Parameter(SMOrderHeader.ParamsBillToName, req).trim();
		m_sShipToName = clsManageRequestParameters.get_Request_Parameter(SMOrderHeader.ParamsShipToName, req).trim();
		m_sDirections = clsManageRequestParameters.get_Request_Parameter(SMOrderHeader.ParammDirections, req).trim();
		m_sTicketComments = clsManageRequestParameters.get_Request_Parameter(SMOrderHeader.ParammTicketComments, req).trim();
		m_sFieldNotes = clsManageRequestParameters.get_Request_Parameter(SMOrderHeader.ParammFieldNotes, req).trim();
		
		//The rest of the order header fields:
		m_sCustomerCode = clsManageRequestParameters.get_Request_Parameter(SMOrderHeader.ParamsCustomerCode, req).trim();
		m_sBillToAddressLine1 = clsManageRequestParameters.get_Request_Parameter(SMOrderHeader.ParamsBillToAddressLine1, req).trim();
		m_sBillToAddressLine2 = clsManageRequestParameters.get_Request_Parameter(SMOrderHeader.ParamsBillToAddressLine2, req).trim();
		m_sBillToAddressLine3 = clsManageRequestParameters.get_Request_Parameter(SMOrderHeader.ParamsBillToAddressLine3, req).trim();
		m_sBillToAddressLine4 = clsManageRequestParameters.get_Request_Parameter(SMOrderHeader.ParamsBillToAddressLine4, req).trim();
		m_sBillToCity = clsManageRequestParameters.get_Request_Parameter(SMOrderHeader.ParamsBillToCity, req).trim();
		m_sBillToState = clsManageRequestParameters.get_Request_Parameter(SMOrderHeader.ParamsBillToState, req).trim();
		m_sBillToZip = clsManageRequestParameters.get_Request_Parameter(SMOrderHeader.ParamsBillToZip, req).trim();
		m_sBillToCountry = clsManageRequestParameters.get_Request_Parameter(SMOrderHeader.ParamsBillToCountry, req).trim();
		m_sBillToFax = clsManageRequestParameters.get_Request_Parameter(SMOrderHeader.ParamsBillToFax, req).trim();
		m_sShipToCode = clsManageRequestParameters.get_Request_Parameter(SMOrderHeader.ParamsShipToCode, req).trim();
		m_sShipToAddress1 = clsManageRequestParameters.get_Request_Parameter(SMOrderHeader.ParamsShipToAddress1, req).trim();
		m_sShipToAddress2 = clsManageRequestParameters.get_Request_Parameter(SMOrderHeader.ParamsShipToAddress2, req).trim();
		m_sShipToAddress3 = clsManageRequestParameters.get_Request_Parameter(SMOrderHeader.ParamsShipToAddress3, req).trim();
		m_sShipToAddress4 = clsManageRequestParameters.get_Request_Parameter(SMOrderHeader.ParamsShipToAddress4, req).trim();
		m_sShipToCity = clsManageRequestParameters.get_Request_Parameter(SMOrderHeader.ParamsShipToCity, req).trim();
		m_sShipToState = clsManageRequestParameters.get_Request_Parameter(SMOrderHeader.ParamsShipToState, req).trim();
		m_sShipToZip = clsManageRequestParameters.get_Request_Parameter(SMOrderHeader.ParamsShipToZip, req).trim();
		m_sShipToCountry = clsManageRequestParameters.get_Request_Parameter(SMOrderHeader.ParamsShipToCountry, req).trim();
		m_sShipToFax = clsManageRequestParameters.get_Request_Parameter(SMOrderHeader.ParamsShipToFax, req).trim();
		m_iCustomerDiscountLevel = clsManageRequestParameters.get_Request_Parameter(SMOrderHeader.ParamiCustomerDiscountLevel, req).trim();
		m_sDefaultPriceListCode = clsManageRequestParameters.get_Request_Parameter(SMOrderHeader.ParamsDefaultPriceListCode, req).trim();
		m_sPONumber = clsManageRequestParameters.get_Request_Parameter(SMOrderHeader.ParamsPONumber, req).trim();
		if (req.getParameter(SMOrderHeader.ParamsSpecialWageRate) == null){
			m_sSpecialWageRate = "F";
		}else{
			m_sSpecialWageRate = "T";
		}
		m_sTerms = clsManageRequestParameters.get_Request_Parameter(SMOrderHeader.ParamsTerms, req).trim();
		m_iOrderType = clsManageRequestParameters.get_Request_Parameter(SMOrderHeader.ParamiOrderType, req).trim();
		m_datOrderDate = clsManageRequestParameters.get_Request_Parameter(SMOrderHeader.ParamdatOrderDate, req).trim();
		m_datExpectedShipDate = clsManageRequestParameters.get_Request_Parameter(SMOrderHeader.ParamdatExpectedShipDate, req).trim();
		m_datOrderCreationDate = clsManageRequestParameters.get_Request_Parameter(SMOrderHeader.ParamdatOrderCreationDate, req).trim();
		m_sServiceTypeCodeDescription = clsManageRequestParameters.get_Request_Parameter(SMOrderHeader.ParamsServiceTypeCodeDescription, req).trim();
		m_sLastInvoiceNumber = clsManageRequestParameters.get_Request_Parameter(SMOrderHeader.ParamsLastInvoiceNumber, req).trim();
		m_iNumberOfInvoices = clsManageRequestParameters.get_Request_Parameter(SMOrderHeader.ParamiNumberOfInvoices, req).trim();
		m_sLocation = clsManageRequestParameters.get_Request_Parameter(SMOrderHeader.ParamsLocation, req).trim();
		m_iOnHold = clsManageRequestParameters.get_Request_Parameter(SMOrderHeader.ParamiOnHold, req).trim();
		m_datLastPostingDate = clsManageRequestParameters.get_Request_Parameter(SMOrderHeader.ParamdatLastPostingDate, req).trim();
		m_dTotalAmountItems = clsManageRequestParameters.get_Request_Parameter(SMOrderHeader.ParamdTotalAmountItems, req).trim();
		m_iNumberOfLinesOnOrder = clsManageRequestParameters.get_Request_Parameter(SMOrderHeader.ParamiNumberOfLinesOnOrder, req).trim();
		m_sSalesperson = clsManageRequestParameters.get_Request_Parameter(SMOrderHeader.ParamsSalesperson, req).trim();
		m_sOrderCreatedByFullName = clsManageRequestParameters.get_Request_Parameter(SMOrderHeader.ParamsOrderCreatedByFullName, req).trim();
		m_lOrderCreatedByID = clsManageRequestParameters.get_Request_Parameter(SMOrderHeader.ParamlOrderCreatedByID, req).trim();
		m_sDefaultItemCategory = clsManageRequestParameters.get_Request_Parameter(SMOrderHeader.ParamsDefaultItemCategory, req).trim();
		m_dTaxBase = clsManageRequestParameters.get_Request_Parameter(SMOrderHeader.ParamdTaxBase, req).trim();
		m_sordersalestaxamount = clsManageRequestParameters.get_Request_Parameter(SMOrderHeader.ParamdOrderTaxAmount, req).trim();
		m_iNextDetailNumber = clsManageRequestParameters.get_Request_Parameter(SMOrderHeader.ParamiNextDetailNumber, req).trim();
		m_mInternalComments = clsManageRequestParameters.get_Request_Parameter(SMOrderHeader.ParammInternalComments, req).trim();
		m_mInvoiceComments = clsManageRequestParameters.get_Request_Parameter(SMOrderHeader.ParammInvoiceComments, req).trim();
		m_dPrePostingInvoiceDiscountPercentage = clsManageRequestParameters.get_Request_Parameter(SMOrderHeader.ParamdPrePostingInvoiceDiscountPercentage, req).trim();
		m_dPrePostingInvoiceDiscountAmount = clsManageRequestParameters.get_Request_Parameter(SMOrderHeader.ParamdPrePostingInvoiceDiscountAmount, req).trim();
		m_LASTEDITUSERFULLNAME = clsManageRequestParameters.get_Request_Parameter(SMOrderHeader.ParamLASTEDITUSERFULLNAME, req).trim();
		m_LASTEDITUSERID = clsManageRequestParameters.get_Request_Parameter(SMOrderHeader.ParamLASTEDITUSERID, req).trim();
		m_LASTEDITPROCESS = clsManageRequestParameters.get_Request_Parameter(SMOrderHeader.ParamLASTEDITPROCESS, req).trim();
		m_LASTEDITDATE = clsManageRequestParameters.get_Request_Parameter(SMOrderHeader.ParamLASTEDITDATE, req).trim();
		m_LASTEDITTIME = clsManageRequestParameters.get_Request_Parameter(SMOrderHeader.ParamLASTEDITTIME, req).trim();
		m_sCustomerControlAcctSet = clsManageRequestParameters.get_Request_Parameter(SMOrderHeader.ParamsCustomerControlAcctSet, req).trim();
		m_sPrePostingInvoiceDiscountDesc = clsManageRequestParameters.get_Request_Parameter(SMOrderHeader.ParamsPrePostingInvoiceDiscountDesc, req).trim();
		m_datOrderCanceledDate = clsManageRequestParameters.get_Request_Parameter(SMOrderHeader.ParamdatOrderCanceledDate, req).trim();
		m_iOrderSourceID = clsManageRequestParameters.get_Request_Parameter(SMOrderHeader.ParamiOrderSourceID, req).trim();
		m_sOrderSourceDesc = clsManageRequestParameters.get_Request_Parameter(SMOrderHeader.ParamsOrderSourceDesc, req).trim();
		m_dEstimatedHour = clsManageRequestParameters.get_Request_Parameter(SMOrderHeader.ParamdEstimatedHour, req).trim();
		m_sEmailAddress = clsManageRequestParameters.get_Request_Parameter(SMOrderHeader.ParamsEmailAddress, req).trim(); 
		m_iSalesGroup = clsManageRequestParameters.get_Request_Parameter(SMOrderHeader.ParamiSalesGroup, req).trim();
		m_sClonedFrom = clsManageRequestParameters.get_Request_Parameter(SMOrderHeader.ParamsClonedFrom, req).trim();
		m_sgeocode = clsManageRequestParameters.get_Request_Parameter(SMOrderHeader.Paramsgeocode, req).trim();
		m_sshiptoemail = clsManageRequestParameters.get_Request_Parameter(SMOrderHeader.Paramsshiptoemail, req).trim();
		m_sgdoclink = clsManageRequestParameters.get_Request_Parameter(SMOrderHeader.Paramsgdoclink, req).trim();
		m_sbidid = clsManageRequestParameters.get_Request_Parameter(SMOrderHeader.Paramlbidid, req).trim();
		if (m_sbidid.compareToIgnoreCase("") == 0){m_sbidid = "0";}
		m_squotedescription = clsManageRequestParameters.get_Request_Parameter(SMOrderHeader.Paramsquotedescription, req).trim();
		m_itaxid = clsManageRequestParameters.get_Request_Parameter(SMOrderHeader.Paramitaxid, req).trim();
		m_staxjurisdiction = clsManageRequestParameters.get_Request_Parameter(SMOrderHeader.ParamsTaxJurisdiction, req).trim();
		m_staxtype = clsManageRequestParameters.get_Request_Parameter(SMOrderHeader.Paramstaxtype, req).trim();
		m_sinvoicingemailaddress = clsManageRequestParameters.get_Request_Parameter(SMOrderHeader.ParamsInvoicingEmailAddress, req).trim();
		m_sinvoicingcontact = clsManageRequestParameters.get_Request_Parameter(SMOrderHeader.ParamsInvoicingContact, req).trim();
		m_sinvoicingnotes = clsManageRequestParameters.get_Request_Parameter(SMOrderHeader.ParamsInvoicingNotes, req).trim();
		m_idoingbusinessasaddressid = clsManageRequestParameters.get_Request_Parameter(SMOrderHeader.Paramidoingbusinessasaddress, req).trim();
	}
	
	public void readOrderFieldInfoFromRequest (HttpServletRequest req){
		m_siID = "-1";
		m_sOrderNumber = "";
		m_sBillToName = "";
		m_sShipToName = "";
		m_sDirections = "";
		m_sTicketComments = "";
		m_sFieldNotes = "";

		m_siID = clsManageRequestParameters.get_Request_Parameter(SMOrderHeader.ParamsiID, req).trim();
		m_sOrderNumber = clsManageRequestParameters.get_Request_Parameter(SMOrderHeader.ParamsOrderNumber, req).trim();
		m_sBillToName = clsManageRequestParameters.get_Request_Parameter(SMOrderHeader.ParamsBillToName, req).trim();
		m_sShipToName = clsManageRequestParameters.get_Request_Parameter(SMOrderHeader.ParamsShipToName, req).trim();
		m_sDirections = clsManageRequestParameters.get_Request_Parameter(SMOrderHeader.ParammDirections, req).trim();
		m_sTicketComments = clsManageRequestParameters.get_Request_Parameter(SMOrderHeader.ParammTicketComments, req).trim();
		m_sFieldNotes = clsManageRequestParameters.get_Request_Parameter(SMOrderHeader.ParammFieldNotes, req).trim();

	}
	public boolean loadDefaultCustomerInformation(ServletContext context, String sDBID, String sUser){
		
		boolean bResult = true;
		String SQL = "SELECT * FROM " + SMTablearcustomer.TableName
			+ " WHERE ("
				+ "(" + SMTablearcustomer.sCustomerNumber + " = '" + getM_sCustomerCode() + "')"
			+ ")"
			;
		try {
			ResultSet rs = clsDatabaseFunctions.openResultSet(
				SQL, 
				context, 
				sDBID, 
				"MySQL", 
				this.toString() + " [1332424850] - SQL: " + clsDateAndTimeConversions.now("yyyyMMdd HH:mm:ss:SSS") + SQL);
			if (rs.next()){
				if (rs.getLong(SMTablearcustomer.iActive) == 0){
					super.addErrorMessage("Customer '" + getM_sCustomerCode() + " is inactive.");
					bResult = false;
				}
				if (rs.getLong(SMTablearcustomer.iOnHold) == 1){
					super.addErrorMessage("Customer '" + getM_sCustomerCode() + " is on hold. <BR>" + 
										  "Customer Comments: " + rs.getString(SMTablearcustomer.mCustomerComments));
					bResult = false;
				}
				if (bResult){
					//TODO - remove some defaults
					//setM_iCustomerDiscountLevel(Long.toString(rs.getLong(SMTablearcustomer.ipricelevel)));
					//setM_sSalesperson(rs.getString(SMTablearcustomer.sSalesperson));
					setM_iOnHold(Long.toString(rs.getLong(SMTablearcustomer.iOnHold)));
					//setM_iTaxClass(Long.toString(rs.getLong(SMTablearcustomer.itaxtype)));
					setM_sBillToAddressLine1(rs.getString(SMTablearcustomer.sAddressLine1));
					setM_sBillToAddressLine2(rs.getString(SMTablearcustomer.sAddressLine2));
					setM_sBillToAddressLine3(rs.getString(SMTablearcustomer.sAddressLine3));
					setM_sBillToAddressLine4(rs.getString(SMTablearcustomer.sAddressLine4));
					setM_sBillToCity(rs.getString(SMTablearcustomer.sCity));
					setM_sBilltoContact(rs.getString(SMTablearcustomer.sContactName));
					setM_sBillToCountry(rs.getString(SMTablearcustomer.sCountry));
					setM_sBillToFax(rs.getString(SMTablearcustomer.sFaxNumber));
					setM_sBillToName(rs.getString(SMTablearcustomer.sCustomerName));
					setM_sBilltoPhone(rs.getString(SMTablearcustomer.sPhoneNumber));
					setM_sBillToState(rs.getString(SMTablearcustomer.sState));
					setM_sBillToZip(rs.getString(SMTablearcustomer.sPostalCode));
					setM_sCustomerControlAcctSet(rs.getString(SMTablearcustomer.sAccountSet));
					setM_sDefaultPriceListCode(rs.getString(SMTablearcustomer.sPriceListCode));
					setM_sEmailAddress(rs.getString(SMTablearcustomer.sEmailAddress));
					//setM_sTaxGroup(rs.getString(SMTablearcustomer.staxjurisdiction));
					setM_sTerms(rs.getString(SMTablearcustomer.sTerms));
					setM_sInvoicingEmail(rs.getString(SMTablearcustomer.sinvoicingemail));
					setM_sInvoicingContact(rs.getString(SMTablearcustomer.sinvoicingcontact));
					setM_sInvoicingNotes(rs.getString(SMTablearcustomer.sinvoicingnotes));
				}
			}else{
				super.addErrorMessage("Customer '" + getM_sCustomerCode() + "' could not be found.");
				bResult = false;
			}
			rs.close();
		} catch (SQLException e) {
			super.addErrorMessage("Unable to load Customer '" + getM_sCustomerCode() + "' - " + e.getMessage() + ".");
			bResult = false;
		}
		
		return bResult;
	}
	public boolean setShipToInformation(ServletContext context, String sDBID, String sUserID, String sUserFullName, String sShipToCode){
		//If the ship to code is blank, don't update the ship to info:
		if (sShipToCode.compareToIgnoreCase("") == 0){
			setM_sShipToName("");
			setM_sShipToAddress1("");
			setM_sShipToAddress2("");
			setM_sShipToAddress3("");
			setM_sShipToAddress4("");
			setM_sShipToCity("");
			setM_sShiptoContact("");
			setM_sShipToCountry("");
			setM_sShipToFax("");
			setM_sShiptoPhone("");
			setM_sShipToState("");
			setM_sShipToZip("");
			setM_sShipToCode("");
			return true;
		}
		
		String SQL = "SELECT * FROM " + SMTablearcustomershiptos.TableName
			+ " WHERE ("
				+ "(" + SMTablearcustomershiptos.sShipToCode + " = '" + sShipToCode + "')"
			+ ")"
			;
		
		try {
			ResultSet rs = clsDatabaseFunctions.openResultSet(
					SQL, 
					context, 
					sDBID, 
					"MySQL", 
					SMUtilities.getFullClassName(this.toString()) 
						+ ".setShipToInformation - user: " 
							+ sUserID
							+ " - "
							+ sUserFullName
						+ " [1331736914] "+ clsDateAndTimeConversions.now("yyyyMMdd HH:mm:ss:SSS")
			);
			if (rs.next()){
				setM_sShipToName(rs.getString(SMTablearcustomershiptos.sDescription).trim());
				setM_sShipToAddress1(rs.getString(SMTablearcustomershiptos.sAddressLine1).trim());
				setM_sShipToAddress2(rs.getString(SMTablearcustomershiptos.sAddressLine2).trim());
				setM_sShipToAddress3(rs.getString(SMTablearcustomershiptos.sAddressLine3).trim());
				setM_sShipToAddress4(rs.getString(SMTablearcustomershiptos.sAddressLine4).trim());
				setM_sShipToCity(rs.getString(SMTablearcustomershiptos.sCity).trim());
				setM_sShiptoContact(rs.getString(SMTablearcustomershiptos.sContactName).trim());
				setM_sShipToCountry(rs.getString(SMTablearcustomershiptos.sCountry).trim());
				setM_sShipToFax(rs.getString(SMTablearcustomershiptos.sFaxNumber).trim());
				setM_sShiptoPhone(rs.getString(SMTablearcustomershiptos.sPhoneNumber).trim());
				setM_sShipToState(rs.getString(SMTablearcustomershiptos.sState).trim());
				setM_sShipToZip(rs.getString(SMTablearcustomershiptos.sPostalCode).trim());
				rs.close();
			}else{
				rs.close();
				super.addErrorMessage("No ship to record for ship to code '" + sShipToCode + "'");
				return false;
			}
		} catch (SQLException e) {
			super.addErrorMessage("Error reading ship to record for ship to code '" + sShipToCode + "' = "
				+ e.getMessage());
		}
		
		setM_sShipToCode(sShipToCode);
		return true;
	}
	public boolean setDefaultItemCategory(
			ServletContext context, 
			String sDBID, 
			String sUserID,
			String sUserFullName,
			String sDefaultItemLocation,
			String sServiceTypeCode){
		//If the ship to code is blank, don't update the ship to info:
		if (sDefaultItemLocation.compareToIgnoreCase("") == 0){
			return true;
		}
		if (sServiceTypeCode.compareToIgnoreCase("") == 0){
			return true;
		}
		
		String SQL = "SELECT * FROM " + SMTabledefaultitemcategories.TableName
			+ " WHERE ("
				+ "(" + SMTabledefaultitemcategories.LocationCode + " = '" + sDefaultItemLocation + "')"
				+ " AND (" + SMTabledefaultitemcategories.ServiceTypeCode + " = '" + sServiceTypeCode + "')"
			+ ")"
			;
		
		try {
			ResultSet rs = clsDatabaseFunctions.openResultSet(
					SQL, 
					context, 
					sDBID, 
					"MySQL", 
					SMUtilities.getFullClassName(this.toString()) 
						+ ".setDefaultItemCategory - user: " 
							+ sUserID
							+ " - "
							+ sUserFullName
						+ " [1331736889] "+ clsDateAndTimeConversions.now("yyyyMMdd HH:mm:ss:SSS")
			);
			if (rs.next()){
				setM_sDefaultItemCategory(rs.getString(SMTabledefaultitemcategories.DefaultItemCategory));
				rs.close();
			}else{
				rs.close();
				super.addErrorMessage("No default item category record for location '" 
					+ sDefaultItemLocation + "' and service type '" + sServiceTypeCode + "'.");
				return false;
			}
		} catch (SQLException e) {
			super.addErrorMessage("Error reading default item category record with SQL: " 
				+ SQL + " - " + e.getMessage() + ".");
			return false;
		}
		
		setM_sLocation(sDefaultItemLocation);
		setM_sServiceTypeCode(sServiceTypeCode);
		
		return true;
	}
	private BigDecimal totalAmountOfItems() throws Exception{
	    BigDecimal dTotalAmtItems = new BigDecimal(0);
	    
	    try {
			for (int i = 0; i < m_arrDetails.size(); i++){
			    dTotalAmtItems = dTotalAmtItems.add(new BigDecimal(m_arrDetails.get(i).getM_dExtendedOrderPrice().replace(",", "")));
			}
		} catch (Exception e) {
			throw new Exception("Error getting total amount of items - " + e.getMessage());
		}
	    return dTotalAmtItems;
	}
	private void addInitialItem(Connection conn) throws Exception{
		String SQL = "";
		SMOrderDetail line = new SMOrderDetail();
		
		//First, get the initial item for this order:
		SQL = "SELECT"
			+ " " + SMTabledefaultitemcategories.InitialItem
			+ " FROM " + SMTabledefaultitemcategories.TableName
			+ " WHERE ("
				+ "(" + SMTabledefaultitemcategories.LocationCode + " = '" + getM_sLocation() + "')"
				+ " AND (" + SMTabledefaultitemcategories.ServiceTypeCode + " = '" + getM_sServiceTypeCode() + "')"
			+ ")"
		;
		if (bDebugMode){
			System.out.println("[1332265846]:" + clsDateAndTimeConversions.now("yyyyMMdd HH:mm:ss:SSS") + SQL);
		}
		try {
			ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
			if (rs.next()){
					line.setM_sItemNumber(rs.getString(SMTabledefaultitemcategories.InitialItem).trim());
				rs.close();
			}else{
				rs.close();
				throw new Exception(
					"No default item category record found for location '" + getM_sLocation() 
					+ "', service type '" + getM_sServiceTypeCode() + "'.");
			}
		} catch (SQLException e) {
			throw new Exception(
				"Error reading default item category to add new line with SQL: " 
					+ SQL + " - " + e.getMessage() + ".");
		}
		
		try {
			updateLineWithItemData(
					line,
					line.getM_sItemNumber(), 
					getM_sDefaultPriceListCode(), 
					getM_iCustomerDiscountLevel(),
					true,
					conn);
		} catch (SQLException e1) {
			throw new Exception(e1.getMessage());
		}
		
		line.setM_datDetailExpectedShipDate(getM_datExpectedShipDate());
		line.setM_datLineBookedDate(getM_datOrderDate());
		//line.setM_dLineTaxAmount("0.00");
		line.setM_dOrderUnitCost("0.00");
		line.setM_dOriginalQty("1.0000");
		line.setM_dQtyOrdered("1.0000");
		line.setM_dQtyShipped("0.0000");
		line.setM_dQtyShippedToDate("0.0000");
		line.setM_dUniqueOrderID(getM_siID());
		line.setM_iDetailNumber(getM_iNextDetailNumber());
		line.setM_iLineNumber("1");
		line.setM_sLocationCode(getM_sLocation());
		line.setM_strimmedordernumber(getM_strimmedordernumber());
		line.setM_sItemCategory(getM_sDefaultItemCategory());
		m_arrDetails.clear();
		m_arrDetails.add(line);
		setM_iNextDetailNumber("2");
		return;
	}
	public void updateLineWithItemData(
		SMOrderDetail line,
		String sItemNumber, 
		String sPriceListCode, 
		String sCustomerPriceLevel,
		boolean bRecalculateUnitPrices,
		Connection conn) throws SQLException{
		
		String SQL = "SELECT"
			+ " " + SMTableicitems.TableName + "." + SMTableicitems.bdmostrecentcost
			+ ", " + SMTableicitems.TableName + "." + SMTableicitems.inonstockitem
			+ ", " + SMTableicitems.TableName + "." + SMTableicitems.iTaxable
			+ ", " + SMTableicitems.TableName + "." + SMTableicitems.sComment1
			+ ", " + SMTableicitems.TableName + "." + SMTableicitems.sComment2
			+ ", " + SMTableicitems.TableName + "." + SMTableicitems.sItemNumber
			+ ", " + SMTableicitems.TableName + "." + SMTableicitems.sItemDescription
			+ ", " + SMTableicitems.TableName + "." + SMTableicitems.sCostUnitOfMeasure
			+ ", " + SMTableicitems.TableName + "." + SMTableicitems.iActive
			+ ", " + SMTableicitems.TableName + "." + SMTableicitems.ihideoninvoicedefault
			+ " FROM " + SMTableicitems.TableName
		+ " WHERE ("
			+ "(" + SMTableicitems.TableName + "." + SMTableicitems.sItemNumber + " = '" + sItemNumber + "')"
		+ ")"
		;
		if (bDebugMode){
			System.out.println("[1332265847]:" + clsDateAndTimeConversions.now("yyyyMMdd HH:mm:ss:SSS") + SQL);
		}
		boolean bActive = true;
		try {
			ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
			if (rs.next()){
				//IF it's inactive, we can't use it:
				if (rs.getLong(SMTableicitems.TableName + "." + SMTableicitems.iActive) == 0){
					bActive = false;
				}
				BigDecimal bdMostRecentCost = rs.getBigDecimal(
					SMTableicitems.TableName + "." + SMTableicitems.bdmostrecentcost);
				line.setM_bdEstimatedUnitCost(clsManageBigDecimals.BigDecimalToScaledFormattedString(
					SMTableicitems.bdmostrecentcostScale, bdMostRecentCost));
				if (rs.getLong(SMTableicitems.TableName + "." + SMTableicitems.inonstockitem) == 1){
					line.setM_iIsStockItem("0");
				}else{
					line.setM_iIsStockItem("1");
				}
				line.setM_iTaxable(Long.toString(rs.getLong(
					SMTableicitems.TableName + "." + SMTableicitems.iTaxable)));
				line.setM_isuppressdetailoninvoice(Long.toString(rs.getLong(
						SMTableicitems.TableName + "." + SMTableicitems.ihideoninvoicedefault)));
				line.setM_sItemNumber(rs.getString(
						SMTableicitems.TableName + "." + SMTableicitems.sItemNumber).trim());
				line.setM_sItemDesc(rs.getString(
					SMTableicitems.TableName + "." + SMTableicitems.sItemDescription).trim());
				line.setM_sOrderUnitOfMeasure(rs.getString(
					SMTableicitems.TableName + "." + SMTableicitems.sCostUnitOfMeasure).trim());
				rs.close();
			}else{
				rs.close();
				super.addErrorMessage(
					"Item '" + line.getM_sItemNumber() 
					+ "' could not be found.");
				throw new SQLException("Item '" + sItemNumber 
						+ " could not be found.");
			}
		} catch (SQLException e) {
			throw new SQLException("Unable to read item data for item '" + sItemNumber + "'.");
		}
		
		if (!bActive){
			line.setM_sItemNumber("");
			line.setM_iIsStockItem("0");
			line.setM_iTaxable("1");
			line.setM_sItemNumber("");
			line.setM_sItemDesc("");
			line.setM_sOrderUnitOfMeasure("");
			super.addErrorMessage(
				"Item '" + sItemNumber 
					+ "' is inactive.");
			throw new SQLException("Item '" + sItemNumber 
				+ " is inactive.");
		}
		
		if (bRecalculateUnitPrices){
			try{
				updateLinePrice(line, conn);
			}catch (SQLException e){
				throw new SQLException ("Could not update line prices - " + e.getMessage());
			}
		}
	}

	public boolean load (ServletContext context, String sDBIB, String sUserID, String sUserFullName){
		Connection conn = clsDatabaseFunctions.getConnection(
				context, 
				sDBIB, 
				"MySQL", 
				this.toString() + ".load - user: " + sUserID + " - " + sUserFullName + "   [1332178334] "
		);

		if (conn == null){
			super.addErrorMessage("Error opening data connection.");
			return false;
		}

		boolean bResult = load (conn);
		clsDatabaseFunctions.freeConnection(context, conn, "[1547067727]");
		return bResult;

	}
	public boolean load (Connection conn){
		if (
			(m_strimmedordernumber.compareToIgnoreCase("") == 0)
			&& (m_sOrderNumber.compareToIgnoreCase("-1") != 0)
		){
			m_strimmedordernumber = m_sOrderNumber.trim();
		}
		return load (m_strimmedordernumber, conn);
	}
	private boolean load (String sTrimmedOrderNumber, Connection conn){

		sTrimmedOrderNumber = sTrimmedOrderNumber.trim();
		if (sTrimmedOrderNumber.compareToIgnoreCase("") == 0){
			super.addErrorMessage("Order Number cannot be blank.");
			return false;
		}
		if (sTrimmedOrderNumber.length() > SMTableorderheaders.sOrderNumberLength){
			super.addErrorMessage("Order Number (" + sTrimmedOrderNumber 
					+ ") is too long.");
			return false;
		}

		String SQL = " SELECT * FROM " + SMTableorderheaders.TableName
		+ " WHERE ("
		+ SMTableorderheaders.strimmedordernumber + " = '" + sTrimmedOrderNumber + "'"
		+ ")";
		
		if (bDebugMode){
			System.out.println("[1332265558]:" + clsDateAndTimeConversions.now("yyyyMMdd HH:mm:ss:SSS") + SQL);
		}

		try {
			ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
			if (rs.next()) {
				//Load the variables here:
				m_siID = Long.toString(rs.getLong(SMTableorderheaders.dOrderUniqueifier));
				m_sOrderNumber = rs.getString(SMTableorderheaders.sOrderNumber);
				m_sBillToName = rs.getString(SMTableorderheaders.sBillToName);
				m_sShipToName = rs.getString(SMTableorderheaders.sShipToName);
				m_sDirections = rs.getString(SMTableorderheaders.mDirections);
				if (m_sDirections == null){m_sDirections = "";}
				m_sTicketComments  = rs.getString(SMTableorderheaders.mTicketComments);
				if (m_sTicketComments == null){m_sTicketComments = "";}
				m_sFieldNotes  = rs.getString(SMTableorderheaders.mFieldNotes);
				if (m_sFieldNotes == null){m_sFieldNotes = "";}
				m_sServiceTypeCode = rs.getString(SMTableorderheaders.sServiceTypeCode);
				m_bdtruckdays = clsManageBigDecimals.BigDecimalToScaledFormattedString(
						SMTableorderheaders.bdtruckdaysScale, rs.getBigDecimal(SMTableorderheaders.bdtruckdays));
				m_bddepositamount = clsManageBigDecimals.BigDecimalToScaledFormattedString(
						SMTableorderheaders.bddepositamountScale, rs.getBigDecimal(SMTableorderheaders.bddepositamount));
				m_scarpenterrate = rs.getString(SMTableorderheaders.scarpenterrate);
				m_slaborerrate = rs.getString(SMTableorderheaders.slaborerrate);
				m_selectricianrate = rs.getString(SMTableorderheaders.selectricianrate);
				m_bdtotalmarkup = clsManageBigDecimals.BigDecimalToScaledFormattedString(
						SMTableorderheaders.bdtotalmarkupScale, rs.getBigDecimal(SMTableorderheaders.bdtotalmarkup));
				m_bdtotalcontractamount = clsManageBigDecimals.BigDecimalToScaledFormattedString(
						SMTableorderheaders.bdtotalcontractamountScale, rs.getBigDecimal(SMTableorderheaders.bdtotalcontractamount));
				m_datwarrantyexpiration = clsDateAndTimeConversions.resultsetDateStringToString(
						rs.getString(SMTableorderheaders.datwarrantyexpiration));
				m_swagescalenotes = rs.getString(SMTableorderheaders.swagescalenotes);
				m_ssecondarybilltophone = rs.getString(SMTableorderheaders.ssecondarybilltophone);
				m_ssecondaryshiptophone = rs.getString(SMTableorderheaders.ssecondaryshiptophone);
				m_sshiptophone = rs.getString(SMTableorderheaders.sShipToPhone);
				m_sbilltophone = rs.getString(SMTableorderheaders.sBillToPhone);
				m_sbilltocontact = rs.getString(SMTableorderheaders.sBillToContact);
				m_sshiptocontact = rs.getString(SMTableorderheaders.sShipToContact);
				m_sServiceTypeCode = rs.getString(SMTableorderheaders.sServiceTypeCode);
				m_strimmedordernumber = rs.getString(SMTableorderheaders.strimmedordernumber);
				
				//The rest of the order header fields:
				m_sCustomerCode = rs.getString(SMTableorderheaders.sCustomerCode);
				m_sBillToAddressLine1 = rs.getString(SMTableorderheaders.sBillToAddressLine1);
				m_sBillToAddressLine2 = rs.getString(SMTableorderheaders.sBillToAddressLine2);
				m_sBillToAddressLine3 = rs.getString(SMTableorderheaders.sBillToAddressLine3);
				m_sBillToAddressLine4 = rs.getString(SMTableorderheaders.sBillToAddressLine4);
				m_sBillToCity = rs.getString(SMTableorderheaders.sBillToCity);
				m_sBillToState = rs.getString(SMTableorderheaders.sBillToState);
				m_sBillToZip = rs.getString(SMTableorderheaders.sBillToZip);
				m_sBillToCountry = rs.getString(SMTableorderheaders.sBillToCountry);
				m_sBillToFax = rs.getString(SMTableorderheaders.sBillToFax);
				m_sShipToCode = rs.getString(SMTableorderheaders.sShipToCode);
				m_sShipToAddress1 = rs.getString(SMTableorderheaders.sShipToAddress1);
				m_sShipToAddress2 = rs.getString(SMTableorderheaders.sShipToAddress2);
				m_sShipToAddress3 = rs.getString(SMTableorderheaders.sShipToAddress3);
				m_sShipToAddress4 = rs.getString(SMTableorderheaders.sShipToAddress4);
				m_sShipToCity = rs.getString(SMTableorderheaders.sShipToCity);
				m_sShipToState = rs.getString(SMTableorderheaders.sShipToState);
				m_sShipToZip = rs.getString(SMTableorderheaders.sShipToZip);
				m_sShipToCountry = rs.getString(SMTableorderheaders.sShipToCountry);
				m_sShipToFax = rs.getString(SMTableorderheaders.sShipToFax);
				m_iCustomerDiscountLevel = Long.toString(rs.getLong(SMTableorderheaders.iCustomerDiscountLevel));
				m_sDefaultPriceListCode = rs.getString(SMTableorderheaders.sDefaultPriceListCode);
				m_sPONumber = rs.getString(SMTableorderheaders.sPONumber);
				m_sSpecialWageRate = rs.getString(SMTableorderheaders.sSpecialWageRate);
				m_sTerms = rs.getString(SMTableorderheaders.sTerms);
				m_iOrderType = Long.toString(rs.getLong(SMTableorderheaders.iOrderType));
				m_datOrderDate = clsDateAndTimeConversions.resultsetDateStringToString(
						rs.getString(SMTableorderheaders.datOrderDate));
				m_datExpectedShipDate = clsDateAndTimeConversions.resultsetDateStringToString(
						rs.getString(SMTableorderheaders.datExpectedShipDate));
				m_datOrderCreationDate = clsDateAndTimeConversions.sqlDateToString(rs.getDate(SMTableorderheaders.datOrderCreationDate),
						"MM/dd/yyyy HH:mm:ss"); 
				m_sServiceTypeCodeDescription = rs.getString(SMTableorderheaders.sServiceTypeCodeDescription);
				m_sLastInvoiceNumber = rs.getString(SMTableorderheaders.sLastInvoiceNumber);
				m_iNumberOfInvoices = Long.toString(rs.getLong(SMTableorderheaders.iNumberOfInvoices));
				m_sLocation = rs.getString(SMTableorderheaders.sLocation);
				m_iOnHold = Long.toString(rs.getLong(SMTableorderheaders.iOnHold));
				m_datLastPostingDate = clsDateAndTimeConversions.resultsetDateStringToString(
						rs.getString(SMTableorderheaders.datLastPostingDate));
				m_dTotalAmountItems = clsManageBigDecimals.BigDecimalToScaledFormattedString(
						2, new BigDecimal(rs.getDouble(SMTableorderheaders.dTotalAmountItems)));
				m_iNumberOfLinesOnOrder = Long.toString(rs.getLong(SMTableorderheaders.iNumberOfLinesOnOrder));
				m_sSalesperson = rs.getString(SMTableorderheaders.sSalesperson);
				if (m_sSalesperson.trim().compareToIgnoreCase("N/A") == 0){
					m_sSalesperson = "";
				}
				m_sOrderCreatedByFullName = rs.getString(SMTableorderheaders.sOrderCreatedByFullName);
				m_lOrderCreatedByID = Long.toString(rs.getLong(SMTableorderheaders.lOrderCreatedByID));
				m_staxjurisdiction = rs.getString(SMTableorderheaders.staxjurisdiction);
				m_sDefaultItemCategory = rs.getString(SMTableorderheaders.sDefaultItemCategory);
				m_dTaxBase = clsManageBigDecimals.BigDecimalToScaledFormattedString(
						SMTableorderheaders.bdTaxBaseScale, rs.getBigDecimal(SMTableorderheaders.bdtaxbase));
				m_sordersalestaxamount = clsManageBigDecimals.BigDecimalToScaledFormattedString(
						SMTableorderheaders.bdOrderTaxAmountScale, rs.getBigDecimal(SMTableorderheaders.bdordertaxamount));
				m_iNextDetailNumber = Long.toString(rs.getLong(SMTableorderheaders.iNextDetailNumber));
				m_mInternalComments = clsDatabaseFunctions.getRecordsetStringValue(rs, SMTableorderheaders.mInternalComments);
				m_mInvoiceComments = clsDatabaseFunctions.getRecordsetStringValue(rs, SMTableorderheaders.mInvoiceComments);
				m_dPrePostingInvoiceDiscountPercentage = clsManageBigDecimals.BigDecimalToScaledFormattedString(
						2, new BigDecimal(rs.getDouble(SMTableorderheaders.dPrePostingInvoiceDiscountPercentage)));
				m_dPrePostingInvoiceDiscountAmount = clsManageBigDecimals.BigDecimalToScaledFormattedString(
						2, new BigDecimal(rs.getDouble(SMTableorderheaders.dPrePostingInvoiceDiscountAmount)));
				m_LASTEDITUSERFULLNAME = rs.getString(SMTableorderheaders.LASTEDITUSERFULLNAME);
				m_LASTEDITUSERID = Long.toString(rs.getLong(SMTableorderheaders.LASTEDITUSERID));
				m_LASTEDITPROCESS = rs.getString(SMTableorderheaders.LASTEDITPROCESS);
				m_LASTEDITDATE = Long.toString(rs.getLong(SMTableorderheaders.LASTEDITDATE));
				m_LASTEDITTIME = Long.toString(rs.getLong(SMTableorderheaders.LASTEDITTIME));
				m_sCustomerControlAcctSet = rs.getString(SMTableorderheaders.sCustomerControlAcctSet);
				m_sPrePostingInvoiceDiscountDesc = rs.getString(SMTableorderheaders.sPrePostingInvoiceDiscountDesc);
				m_datOrderCanceledDate = clsDateAndTimeConversions.resultsetDateStringToString(
						rs.getString(SMTableorderheaders.datOrderCanceledDate));
				m_iOrderSourceID = Long.toString(rs.getLong(SMTableorderheaders.iOrderSourceID));
				m_sOrderSourceDesc = rs.getString(SMTableorderheaders.sOrderSourceDesc);
				m_dEstimatedHour = clsManageBigDecimals.BigDecimalToScaledFormattedString(
						2, new BigDecimal(rs.getDouble(SMTableorderheaders.dEstimatedHour)));
//				m_datCompletedDate = SMUtilities.resultsetDateStringToString(
//						rs.getString(SMTableorderheaders.datCompletedDate));
				m_sEmailAddress = rs.getString(SMTableorderheaders.sEmailAddress); 
				m_iSalesGroup = Long.toString(rs.getLong(SMTableorderheaders.iSalesGroup));
				m_sClonedFrom = rs.getString(SMTableorderheaders.sclonedfrom);
				m_sgeocode = rs.getString(SMTableorderheaders.sgeocode);
				m_sshiptoemail = rs.getString(SMTableorderheaders.sshiptoemail);
				m_sgdoclink = rs.getString(SMTableorderheaders.sgdoclink) + "";
				m_sbidid = Long.toString(rs.getLong(SMTableorderheaders.lbidid));
				m_squotedescription = rs.getString(SMTableorderheaders.squotedescription) + "";
				m_itaxid = Long.toString(rs.getLong(SMTableorderheaders.itaxid));
				m_staxtype = rs.getString(SMTableorderheaders.staxtype) + "";
				m_sinvoicingemailaddress = rs.getString(SMTableorderheaders.sinvoicingemail) + "";
				m_sinvoicingcontact = rs.getString(SMTableorderheaders.sinvoicingcontact) + "";
				m_sinvoicingnotes = rs.getString(SMTableorderheaders.sinvoicingnotes) + "";
				m_idoingbusinessasaddressid = Long.toString(rs.getLong(SMTableorderheaders.idoingbusinessasaddressid));
				rs.close();
			} else {
				super.addErrorMessage("No " + ParamObjectName + " found for : '" + sTrimmedOrderNumber
						+ "'");
				rs.close();
				return false;
			}
		} catch (SQLException e) {
			super.addErrorMessage("Error reading " + ParamObjectName + " for : '" + sTrimmedOrderNumber
					+ "' - " + e.getMessage());
			return false;
		}
		
		if (!loadDetailLines(conn)){
			return false;
		}
		return true;
	}
	public boolean cancelOrder (String sTrimmedOrderNumber, ServletContext context, String sDBIB, String sUserID, String sUserFullName){
		boolean bResult = true;

		String SQL = "SELECT"
			+ " " + SMTableorderheaders.datOrderCanceledDate
			+ " FROM " + SMTableorderheaders.TableName
			+ " WHERE ("
				+ "(" + SMTableorderheaders.strimmedordernumber + " = '" + sTrimmedOrderNumber + "')"
			+ ")"
		;
		try {
			ResultSet rs = clsDatabaseFunctions.openResultSet(
				SQL, 
				context, 
				sDBIB, 
				"MySQL", 
				this.toString() + ".cancelOrder - user: " 
				+ sUserID
				+ " - "
				+ sUserFullName
				+ " [1331736858] "+ clsDateAndTimeConversions.now("yyyyMMdd HH:mm:ss:SSS"));
			if (rs.next()){
				if (rs.getString(SMTableorderheaders.datOrderCanceledDate) == null
						|| rs.getString(SMTableorderheaders.datOrderCanceledDate).compareTo("1899-12-31 00:00:00") <= 0
				){
				}else{
					super.addErrorMessage("Order number " + getM_strimmedordernumber() + " was ALREADY canceled.");
					bResult = false;
				}
			}else{
				super.addErrorMessage("Order number " + getM_strimmedordernumber() + " could not be found.");
				bResult = false;
			}
			rs.close();
		} catch (SQLException e) {
			super.addErrorMessage("Error reading canceled date - with SQL: " + SQL + e.getMessage());
			bResult = false;		
		}
		if (bResult){
			//Now update the canceled date:
			SQL = "UPDATE"
				+ " " + SMTableorderheaders.TableName
				+ " SET " + SMTableorderheaders.datOrderCanceledDate + " = NOW()"
				+ " WHERE ("
					+ "(" + SMTableorderheaders.strimmedordernumber + " = '" + sTrimmedOrderNumber + "')"
				+ ")"
			;
			try {
				clsDatabaseFunctions.executeSQL(
					SQL, 
					context, 
					sDBIB, 
					"MySQL", 
					this.toString() + ".cancelOrder - user: " + sUserID + " - " + sUserFullName);
			} catch (SQLException e) {
				super.addErrorMessage("Error setting canceled date - with SQL: " + SQL + e.getMessage());
				bResult = false;
			}
			SMLogEntry log = new SMLogEntry(sDBIB, context);
			log.writeEntry(sUserID, SMLogEntry.LOG_OPERATION_SMCANCELORDER, "Order was canceled", SQL, "[1376509290]");
		}
		
		if (bResult){
			try {
				mailCancellationNotification(
					sTrimmedOrderNumber,
					sUserID,
					sUserFullName,
					sDBIB, 
					context, 
					false
				);
			} catch (Exception e) {
				super.addErrorMessage("Error [1390330922] sending email - " + e.getMessage());
				bResult = false;
			}
		}
		
		return bResult;
	}
	public boolean uncancelOrder (String sTrimmedOrderNumber, ServletContext context, String sDBIB, String sUserID, String sUserFullName){
		boolean bResult = true;

		String SQL = "SELECT"
			+ " " + SMTableorderheaders.datOrderCanceledDate
			+ " FROM " + SMTableorderheaders.TableName
			+ " WHERE ("
				+ "(" + SMTableorderheaders.strimmedordernumber + " = '" + sTrimmedOrderNumber + "')"
			+ ")"
		;
		try {
			ResultSet rs = clsDatabaseFunctions.openResultSet(
				SQL, 
				context, 
				sDBIB, 
				"MySQL", 
				this.toString() + ".uncancelOrder - user: " 
				+ sUserID
				+ " - "
				+ sUserFullName
				+ " [1331736938] " + clsDateAndTimeConversions.now("yyyyMMdd HH:mm:ss:SSS"));
			if (rs.next()){
				if (rs.getString(SMTableorderheaders.datOrderCanceledDate) == null
						|| rs.getString(SMTableorderheaders.datOrderCanceledDate).compareTo("1899-12-31 00:00:00") <= 0
				){
					super.addErrorMessage("Order number " + getM_strimmedordernumber() + " was NOT canceled.");
					bResult = false;
				}
			}else{
				super.addErrorMessage("Order number " + getM_strimmedordernumber() + " could not be found.");
				bResult = false;
			}
			rs.close();
		} catch (SQLException e) {
			super.addErrorMessage("Error checking canceled date - with SQL: " + SQL + e.getMessage());
			bResult = false;		
		}
		if (bResult){
			//Now REMOVE the canceled date:
			SQL = "UPDATE"
				+ " " + SMTableorderheaders.TableName
				+ " SET " + SMTableorderheaders.datOrderCanceledDate + " = '0000-00-00 00:00:00'"
				+ " WHERE ("
					+ "(" + SMTableorderheaders.strimmedordernumber + " = '" + sTrimmedOrderNumber + "')"
				+ ")"
			;
			try {
				clsDatabaseFunctions.executeSQL(
					SQL, 
					context, 
					sDBIB, 
					"MySQL", 
					this.toString() + ".cancelOrder - user: " + sUserID + " - " + sUserFullName);
			} catch (SQLException e) {
				super.addErrorMessage("Error clearing 'canceled' flag - with SQL: " + SQL + e.getMessage());
				bResult = false;
			}
		}
		SMLogEntry log = new SMLogEntry(sDBIB, context);
		log.writeEntry(sUserID, SMLogEntry.LOG_OPERATION_SMUNCANCELORDER, "Order was UNcanceled", SQL, "[1376509291]");
		if (bResult){
			try {
				mailCancellationNotification(
					sTrimmedOrderNumber,
					sUserID, 
					sUserFullName,
					sDBIB, 
					context, 
					true
				);
			} catch (Exception e) {
				super.addErrorMessage("Error [1390330921] sending email - " + e.getMessage());
				bResult = false;
			}
		}
		return bResult;
	}
	
	private void mailCancellationNotification(
			String sTrimmedOrderNumber,
			String sUserID,
			String sUserFullName,
			String sDBIB,
			ServletContext context,
			boolean bUncanceled) throws Exception{
		setM_strimmedordernumber(sTrimmedOrderNumber);
		if (!load(context, sDBIB, sUserID, sUserFullName)){
			throw new Exception(getErrorMessages());
		}
		//Notify any specified users that a new customer was built:
		String SQL = "SELECT " + SMTableusers.semail 
			+ " FROM " 
				+ SMTableusers.TableName + ", "
				+ SMTablesecuritygroupfunctions.TableName + ", "
				+ SMTablesecurityusergroups.TableName
			+ " WHERE ("
				+ "(" + SMTablesecuritygroupfunctions.TableName + "." + SMTablesecuritygroupfunctions.ifunctionid
					+ " = " + SMSystemFunctions.SMReceiveOrderCancellationNotifications + ")"
				+ " AND (" + SMTablesecuritygroupfunctions.TableName + "." + SMTablesecuritygroupfunctions.sGroupName
					+ " = " + SMTablesecurityusergroups.TableName + "." + SMTablesecurityusergroups.sSecurityGroupName + ")"
				+ " AND (" + SMTableusers.TableName + "." + SMTableusers.lid + " = "
					+ SMTablesecurityusergroups.TableName + "." + SMTablesecurityusergroups.luserid + ")"
			+ ")"
			;
		String sCurrentTime = "";
		String sSMTPServer = "";
		String sSMTPPort = "";
		String sSMTPSourceServerName = "";
		String sSMTPUserName = "";
		String sSMTPPassword = "";
		String sReplyToAddress = "";
		String sCompany = "";
		String sSubject = "";
		String sSalesperson = "";
		boolean bUsesSMTPAuthentication = false;
		ArrayList<String>arrNotifyEmails = new ArrayList<String>(0);
		try{
			ResultSet rsNotify = clsDatabaseFunctions.openResultSet(
				SQL, 
				context, 
				sDBIB, 
				"MySQL", 
				this.toString() + ".mailCancellationNotification - rsNotify - user: " + sUserID + " - " + sUserFullName);
			while(rsNotify.next()){
				arrNotifyEmails.add(rsNotify.getString(SMTableusers.semail));
			}
			rsNotify.close();
			//If there is no one to notify, just exit out:
			if(arrNotifyEmails.size() == 0){
				return;
			}
			

			//Get the salesperson's full name:
			SQL = 
				"SELECT"
				+ " " + SMTablesalesperson.sSalespersonFirstName
				+ ", " + SMTablesalesperson.sSalespersonLastName
				+ " FROM " + SMTablesalesperson.TableName
				+ " WHERE ("
					+ "(" + SMTablesalesperson.sSalespersonCode + " = '" + getM_sSalesperson() + "')"
				+ ")"
			;
			ResultSet rsSalesperson = clsDatabaseFunctions.openResultSet(
					SQL, 
					context, 
					sDBIB, 
					"MySQL", 
					this.toString() + ".mailCancellationNotification - rsSalesperson - user: " +  sUserID + " - " + sUserFullName);
			if (rsSalesperson.next()){
				sSalesperson = rsSalesperson.getString(SMTablesalesperson.sSalespersonFirstName) + " "
					+ rsSalesperson.getString(SMTablesalesperson.sSalespersonLastName);
			}
			rsSalesperson.close();
			
			//Now we need to get the company name:
			SQL = "SELECT"
				+ " " + SMTablecompanyprofile.sCompanyName
				+ " FROM " + SMTablecompanyprofile.TableName
			;
			ResultSet rsCompany = clsDatabaseFunctions.openResultSet(
					SQL, 
					context, 
					sDBIB, 
					"MySQL", 
					this.toString() + ".mailCancellationNotification - rsCompany - user: " +  sUserID + " - " + sUserFullName);
			if (rsCompany.next()){
				sCompany = rsCompany.getString(SMTablecompanyprofile.sCompanyName);
			}
			rsCompany.close();
			
			SQL = "SELECT " + SMTablesmoptions.TableName + ".*"
			+ ", DATE_FORMAT(NOW(),'%c/%e/%Y %h:%i:%s %p')"
				+ " AS CURRENTTIME FROM " 
				+ SMTablesmoptions.TableName;
			ResultSet rsOptions = clsDatabaseFunctions.openResultSet(
					SQL, 
					context, 
					sDBIB, 
					"MySQL", 
					this.toString() + ".mailCancellationNotification - rsOptions - user: " +  sUserID + " - " + sUserFullName);
			if(rsOptions.next()){
				sCurrentTime = rsOptions.getString("CURRENTTIME");
				sSMTPServer = rsOptions.getString(SMTablesmoptions.ssmtpserver).trim();
				sSMTPPort = rsOptions.getString(SMTablesmoptions.ssmtpport).trim();
				sSMTPSourceServerName = rsOptions.getString(SMTablesmoptions.ssmtpsourceservername).trim();
				sSMTPUserName = rsOptions.getString(SMTablesmoptions.ssmtpusername).trim();
				sSMTPPassword = rsOptions.getString(SMTablesmoptions.ssmtppassword).trim();
				sReplyToAddress = rsOptions.getString(SMTablesmoptions.ssmtpreplytoname).trim();
				bUsesSMTPAuthentication = (rsOptions.getInt(SMTablesmoptions.iusesauthentication) == 1);
				rsOptions.close();
			}else{
				rsOptions.close();
				throw new Exception("Error [1390329709] - Could not get record with email info.");
			}
		}catch(SQLException e){
			throw new Exception("Error [1390329710] - Could not get email info - " + e.getMessage());
		}

		//Now construct the email:
		String sProcess = "CANCELED";
		if (bUncanceled){
			sProcess = "UNCANCELED";
		}
		sSubject = "Order " + sProcess + " #" 
			+ getM_strimmedordernumber() 
			+ ", " + getM_sDefaultItemCategory()
			+ ", Salesperson: "
			+ getM_sSalesperson()
			+ " - " + sSalesperson
		;
		String sBody = "Order # " + getM_strimmedordernumber() + "' was " + sProcess + " " + sCurrentTime
			+ " by " + sUserFullName 
			+ " in company " + sCompany
			+ "\n\n"
			+ "BILL-TO-NAME: " + getM_sBillToName() + "\n"
			+ "SHIP-TO-NAME: " + getM_sShipToName() + "\n"
			+ "INTERNAL COMMENTS: " + getM_mInternalComments() + "\n"
			;

		int iSMTPPort;
		try {
			iSMTPPort = Integer.parseInt(sSMTPPort);
		} catch (NumberFormatException e) {
			throw new Exception("Error [1390329711] Invalid email port - " + sSMTPPort + ".");
		}
		
		try {
			SMUtilities.sendEmail(
					sSMTPServer, 
					sSMTPUserName, 
					sSMTPPassword, 
					sReplyToAddress,
					Integer.toString(iSMTPPort), 
					sSubject,
					sBody, 
					"SMCP@" + sSMTPSourceServerName,
					sSMTPSourceServerName, 
					arrNotifyEmails, 
					bUsesSMTPAuthentication,
					false);
		} catch (Exception e1) {
			throw new Exception("Error [1390329712] sending email - " + e1.getMessage() + ".");
		}
		return;
	}
	
	public boolean loadDetailLines(Connection conn){
		
		m_arrDetails.clear();
		String SQL = "SELECT * FROM " + SMTableorderdetails.TableName
			+ " WHERE ("
				+ "(" + SMTableorderdetails.strimmedordernumber + " = '" + getM_strimmedordernumber() + "')"
			+ ") ORDER BY " + SMTableorderdetails.iLineNumber
		;
		try {
			ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
			while (rs.next()){
				SMOrderDetail line = new SMOrderDetail();
				line.setM_datDetailExpectedShipDate(clsDateAndTimeConversions.resultsetDateStringToString(
					rs.getString(SMTableorderdetails.datDetailExpectedShipDate)));
				line.setM_datLineBookedDate(clsDateAndTimeConversions.resultsetDateStringToString(
					rs.getString(SMTableorderdetails.datLineBookedDate)));
				line.setM_bdEstimatedUnitCost(clsManageBigDecimals.BigDecimalToScaledFormattedString(
						SMTableorderdetails.bdEstimatedUnitCostScale, rs.getBigDecimal(SMTableorderdetails.bdEstimatedUnitCost)));
				line.setM_dExtendedOrderCost(clsManageBigDecimals.BigDecimalToScaledFormattedString(
					2, new BigDecimal(rs.getDouble(SMTableorderdetails.dExtendedOrderCost))));
				line.setM_dExtendedOrderPrice(clsManageBigDecimals.BigDecimalToScaledFormattedString(
					2, new BigDecimal(rs.getDouble(SMTableorderdetails.dExtendedOrderPrice))));
				line.setM_dOrderUnitCost(clsManageBigDecimals.BigDecimalToScaledFormattedString(
					2, new BigDecimal(rs.getDouble(SMTableorderdetails.dOrderUnitCost))));
				line.setM_dOrderUnitPrice(clsManageBigDecimals.BigDecimalToScaledFormattedString(
					2, new BigDecimal(rs.getDouble(SMTableorderdetails.dOrderUnitPrice))));
				line.setM_dOriginalQty(clsManageBigDecimals.BigDecimalToScaledFormattedString(
					4, new BigDecimal(rs.getDouble(SMTableorderdetails.dOriginalQty))));
				line.setM_dQtyOrdered(clsManageBigDecimals.BigDecimalToScaledFormattedString(
					4, new BigDecimal(rs.getDouble(SMTableorderdetails.dQtyOrdered))));
				line.setM_dQtyShipped(clsManageBigDecimals.BigDecimalToScaledFormattedString(
					4, new BigDecimal(rs.getDouble(SMTableorderdetails.dQtyShipped))));
				line.setM_dQtyShippedToDate(clsManageBigDecimals.BigDecimalToScaledFormattedString(
					4, new BigDecimal(rs.getDouble(SMTableorderdetails.dQtyShippedToDate))));
				line.setM_dUniqueOrderID(Long.toString(rs.getLong(SMTableorderdetails.dUniqueOrderID)));
				line.setM_iDetailNumber(Long.toString(rs.getLong(SMTableorderdetails.iDetailNumber)));
				line.setM_iIsStockItem(Long.toString(rs.getLong(SMTableorderdetails.iIsStockItem)));
				line.setM_iLineNumber(Long.toString(rs.getLong(SMTableorderdetails.iLineNumber)));
				line.setM_isuppressdetailoninvoice(Long.toString(rs.getLong(SMTableorderdetails.isuppressdetailoninvoice)));
				line.setM_iprintondeliveryticket(Long.toString(rs.getLong(SMTableorderdetails.iprintondeliveryticket)));
				line.setM_iTaxable(Long.toString(rs.getLong(SMTableorderdetails.iTaxable)));
				line.setM_mInvoiceComments(rs.getString(SMTableorderdetails.mInvoiceComments));
				line.setM_mInternalComments(rs.getString(SMTableorderdetails.mInternalComments));
				line.setM_mTicketComments(rs.getString(SMTableorderdetails.mTicketComments));
				line.setM_sItemCategory(rs.getString(SMTableorderdetails.sItemCategory));
				line.setM_sItemDesc(rs.getString(SMTableorderdetails.sItemDesc));
				line.setM_sItemNumber(rs.getString(SMTableorderdetails.sItemNumber));
				line.setM_sLabel(rs.getString(SMTableorderdetails.sLabel));
				line.setM_sLocationCode(rs.getString(SMTableorderdetails.sLocationCode));
				line.setM_sMechFullName(rs.getString(SMTableorderdetails.sMechFullName));
				line.setM_sMechInitial(rs.getString(SMTableorderdetails.sMechInitial));
				line.setM_sMechID(Long.toString(rs.getLong(SMTableorderdetails.imechid)));
				line.setM_sOrderUnitOfMeasure(rs.getString(SMTableorderdetails.sOrderUnitOfMeasure));
				line.setM_strimmedordernumber(rs.getString(SMTableorderdetails.strimmedordernumber));
				
				m_arrDetails.add(line);
			}
			rs.close();
		} catch (SQLException e) {
			super.addErrorMessage("Error loading order details with SQL: " + SQL + " - " + e.getMessage());
			return false;
		}
		return true;
	}
	public boolean loadFieldInfo (ServletContext context, String sDBIB, String sUser, String sUserID, String sUserFullName){
		Connection conn = clsDatabaseFunctions.getConnection(
				context, 
				sDBIB, 
				"MySQL", 
				this.toString() + ".loadFieldInfo - user: " + sUserID 
				 + " - "
				 + sUserFullName
		);

		if (conn == null){
			super.addErrorMessage("Error opening data connection.");
			return false;
		}

		boolean bResult = loadFieldInfo (m_sOrderNumber, conn);
		clsDatabaseFunctions.freeConnection(context, conn, "[1547067728]");
		return bResult;

	}
	private boolean loadFieldInfo (String sOrderNumber, Connection conn){

		sOrderNumber = sOrderNumber.trim();
		if (sOrderNumber.compareToIgnoreCase("") == 0){
			super.addErrorMessage("Order Number cannot be blank.");
			return false;
		}
		if (sOrderNumber.length() > SMTableorderheaders.sOrderNumberLength){
			super.addErrorMessage("Order Number (" + sOrderNumber 
					+ ") is too long.");
			return false;
		}

		String SQL = " SELECT"
			+ " " + SMTableorderheaders.dOrderUniqueifier
			+ ", " + SMTableorderheaders.sOrderNumber
			+ ", " + SMTableorderheaders.sBillToName
			+ ", " + SMTableorderheaders.sShipToName
			+ ", " + SMTableorderheaders.mDirections
			+ ", " + SMTableorderheaders.mTicketComments
			+ ", " + SMTableorderheaders.mFieldNotes
			+ " FROM " + SMTableorderheaders.TableName
			+ " WHERE ("
			+ SMTableorderheaders.sOrderNumber + " = '" + clsStringFunctions.PadLeft(sOrderNumber, " ", 8) + "'"
			+ ")";

		try {
			ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
			if (rs.next()) {
				//Load the variables here:
				m_siID = Long.toString(rs.getLong(SMTableorderheaders.dOrderUniqueifier));
				m_sOrderNumber  = rs.getString(SMTableorderheaders.sOrderNumber);
				m_sBillToName  = rs.getString(SMTableorderheaders.sBillToName);
				m_sShipToName  = rs.getString(SMTableorderheaders.sShipToName);
				m_sDirections  = rs.getString(SMTableorderheaders.mDirections);
				if (m_sDirections == null){m_sDirections = "";}
				m_sTicketComments  = rs.getString(SMTableorderheaders.mTicketComments);
				if (m_sTicketComments == null){m_sTicketComments = "";}
				m_sFieldNotes  = rs.getString(SMTableorderheaders.mFieldNotes);
				if (m_sFieldNotes == null){m_sFieldNotes = "";}
				rs.close();
			} else {
				super.addErrorMessage("No " + ParamObjectName + " found for : '" + sOrderNumber
						+ "'");
				rs.close();
				return false;
			}
		} catch (SQLException e) {
			super.addErrorMessage("Error reading " + ParamObjectName + " for : '" + sOrderNumber
					+ "' - " + e.getMessage());
			return false;
		}
		return true;
	}
	
	public boolean save_cloned_order(Connection conn, 
							  String sUserID, 
							  String sUserFullName,
							  String sEditProcess){
		if (!validate_order_fields(conn)){
			return false;
		}
		boolean bNewOrder = false;
		if (m_sOrderNumber.compareTo("-1") == 0){
			bNewOrder = true;
		}
		try {
			processSaveTransaction(conn, sUserID, sUserFullName, bNewOrder, sEditProcess);
		} catch (SQLException e1) {
			super.addErrorMessage("Error saving order - " + e1.getMessage());
			return false;
		}
		return true;
	}
	
	public boolean save_order_totals_without_data_transaction (
			ServletContext context, 
			String sDBIB, 
			String sUser,
			String sUserID,
			String sUserFullName,
			String sCompany){

		Connection conn = clsDatabaseFunctions.getConnection(
				context, 
				sDBIB, 
				"MySQL", 
				this.toString() + ".save_order_totals_without_data_transaction - user: " 
				+ sUserID
				+ " - "
				+ sUserFullName
		);

		if (conn == null){
			super.addErrorMessage("Error opening data connection.");
			return false;
		}

		boolean bResult = save_order_totals_without_data_transaction (conn, sDBIB, context, sUserID, sUserFullName, sCompany);
		clsDatabaseFunctions.freeConnection(context, conn, "[1547067743]");
		return bResult;	

	}
	public boolean save_order_without_data_transaction (
			Connection conn,
			String sDBIB,
			ServletContext context,
			String sUserID, 
			String sUserFullName, 
			boolean bRecalculateUnitPrices,
			boolean bGetGeocode,
			String sEditProcess){
		
		if (!validate_order_fields(conn)){
			return false;
		}
		
		boolean bIsNewOrder = true;
		//Set several fields here (order creation, etc.)
		if (Long.parseLong(getM_siID()) > 0){
			bIsNewOrder = false;
		}
		
		//If it IS a new order, add the initial line to it:
		if (bIsNewOrder){
			this.m_arrDetails.clear();
			try {
				addInitialItem(conn);
			} catch (Exception e) {
				super.addErrorMessage(e.getMessage());
				return false;
			}
		}
		
		if (bDebugMode){
			System.out.println("In " + this.toString() + " in save 1");
		}
		
		//Grab a new geocode if the address field as changed
		if (bGetGeocode){
			try {
				geocodeAddress(conn, sUserID);
			} catch (Exception e) {
				super.addStatusMessage(e.getMessage());
			}
		}
		
		String sCurrentID = getM_siID();
		String sCurrentOrderNumber = getM_sOrderNumber();
		String sCurrentTrimmedOrderNumber = getM_strimmedordernumber();

		if (bDebugMode){
			System.out.println("In " + this.toString() + " in save 2");
		}
		
		//Now recalculate the entire order:
		try {
			recalculateEntireOrder(conn, bIsNewOrder, bRecalculateUnitPrices);
		} catch (Exception e2) {
			super.addErrorMessage("Error recalculating order - " + e2.getMessage());
			return false;
		}
		
		try {
			Statement stmt = conn.createStatement();
			stmt.execute("START TRANSACTION");
		} catch (SQLException e) {
			super.addErrorMessage("Error starting data transaction - " + e.getMessage());
			return false;
		}
		
		if (bDebugMode){
			System.out.println("In " + this.toString() + " in save 3");
		}

		try {
			processSaveTransaction(conn, sUserID, sUserFullName, bIsNewOrder, sEditProcess);
		} catch (SQLException e1) {
			clsDatabaseFunctions.rollback_data_transaction(conn);
			//If it's a new order, we have to roll the ID and order number back, since we haven't actually taken
			//them yet
			if (bIsNewOrder){
				resetOrderNumber(sCurrentID, sCurrentOrderNumber, sCurrentTrimmedOrderNumber);
			}
			super.addErrorMessage("Error saving order - " + e1.getMessage());
			return false;
		}
		
		if (!bIsNewOrder){
			try {
				checkConcurrency(sDBIB, context);
			} catch (SQLException e1) {
				clsDatabaseFunctions.rollback_data_transaction(conn);
				super.addErrorMessage("Could not save - " + e1.getMessage());
				return false;
			}
		}
		try {
			Statement stmt = conn.createStatement();
			stmt.execute("COMMIT");
		} catch (SQLException e) {
			clsDatabaseFunctions.rollback_data_transaction(conn);
			if (bIsNewOrder){
				resetOrderNumber(sCurrentID, sCurrentOrderNumber, sCurrentTrimmedOrderNumber);
			}
			super.addErrorMessage("Error committing data transaction - " + e.getMessage());
			return false;
		}
		
		return true;
	}
	public boolean save_order_unprotected_by_transaction (Connection conn, 
														  String sDBIB,
														  ServletContext context,
														  String sUserID, 
														  String sUserFullName,
														  boolean bRecalculateUnitPrices,
														  String sEditProcess){
		
		if (!validate_order_fields(conn)){
			return false;
		}
		
		//Now recalculate the entire order:
		try {
			recalculateEntireOrder(conn, false, bRecalculateUnitPrices);
		} catch (Exception e2) {
			super.addErrorMessage("Error recalculating order - " + e2.getMessage());
			return false;
		}

		try {
			processSaveTransaction(conn, sUserID, sUserFullName, false, sEditProcess);
		} catch (SQLException e1) {
			super.addErrorMessage("Error saving order - " + e1.getMessage());
			return false;
		}
		
		try {
			checkConcurrency(sDBIB, context);
		} catch (SQLException e) {
			super.addErrorMessage("Error saving order - " + e.getMessage());
			return false;
		}
		
		return true;
	}
	
	public boolean save_order_totals_without_data_transaction (
			Connection conn,
			String sDBIB,
			ServletContext context,
			String sUserID, 
			String sUserFullName, 
			String sCompany){
		
		//First, store the values the user input for the totals fields:
		String sDiscountDescription = getM_sPrePostingInvoiceDiscountDesc();
		String sDiscountPercentage = getM_dPrePostingInvoiceDiscountPercentage();
		String sDiscountAmount = getM_dPrePostingInvoiceDiscountAmount();
		String sDepositAmount = getM_bddepositamount();
		
		//Next, load this order:
		if (!load(getM_strimmedordernumber(), conn)){
			return false;
		}

		//Set the order fields back to what the user input:
		setM_dPrePostingInvoiceDiscountAmount(sDiscountAmount);
		setM_dPrePostingInvoiceDiscountPercentage(sDiscountPercentage);
		setM_sPrePostingInvoiceDiscountDesc(sDiscountDescription);
		setM_bddepositamount(sDepositAmount);
		
		if (!validate_order_fields(conn)){
			return false;
		}
		
		//Now recalculate the entire order:
		try {
			recalculateEntireOrder(conn, false, false);
		} catch (Exception e2) {
			super.addErrorMessage("Error recalculating order - " + e2.getMessage());
			return false;
		}
		
		try {
			Statement stmt = conn.createStatement();
			stmt.execute("START TRANSACTION");
		} catch (SQLException e) {
			super.addErrorMessage("Error starting data transaction - " + e.getMessage());
			return false;
		}
		
		if (bDebugMode){
			System.out.println("In " + this.toString() + " in save 3");
		}

		try {
			updateHeader(conn, sUserID, sUserFullName, "UPDATEDTOTALS");
		} catch (SQLException e1) {
			clsDatabaseFunctions.rollback_data_transaction(conn);
			super.addErrorMessage("Error saving order - " + e1.getMessage());
			return false;
		}
		
		//Check the 'LASTEDIT' in the stored record against this one:
		try {
			checkConcurrency(sDBIB, context);
		} catch (SQLException e1) {
			clsDatabaseFunctions.rollback_data_transaction(conn);
			super.addErrorMessage("Error saving - " + e1.getMessage());
			return false;
		}
		
		try {
			Statement stmt = conn.createStatement();
			stmt.execute("COMMIT");
		} catch (SQLException e) {
			clsDatabaseFunctions.rollback_data_transaction(conn);
			super.addErrorMessage("Error committing data transaction - " + e.getMessage());
			return false;
		}
		
		return true;
	}

	private void geocodeAddress(Connection conn, String sUserID) throws Exception{
		String sMapAddress = this.getM_sShipToAddress1().trim();
		sMapAddress	= sMapAddress.trim() + " " + this.getM_sShipToAddress2().trim();
		sMapAddress	= sMapAddress.trim() + " " + this.getM_sShipToAddress3().trim();
		sMapAddress	= sMapAddress.trim() + " " + this.getM_sShipToAddress4().trim();
		sMapAddress	= sMapAddress.trim() + " " + this.getM_sShipToCity().trim();
		sMapAddress	= sMapAddress.trim() + " " + this.getM_sShipToState().trim();
		sMapAddress	= sMapAddress.trim() + " " + this.getM_sShipToZip().trim();
		
		String sLatLng = SMGeocoder.EMPTY_GEOCODE;
		//We are NOT going to stop the save if we code a geocode error:
		int iAttemptNo = 0;
		try {
			do {
				sLatLng = SMGeocoder.codeAddress(sMapAddress, conn, iAttemptNo);
				iAttemptNo++;
			}while(sLatLng.compareToIgnoreCase(SMGeocoder.OVER_QUERY_LIMIT_ERROR) == 0);
			
			SMClasses.SMLogEntry log = new SMClasses.SMLogEntry(conn);
			log.writeEntry(
				sUserID, 
				SMLogEntry.LOG_OPERATION_SMGEOCODEREQUEST, 
				"Order Number: " + getM_strimmedordernumber() +  "\n"
				+ "Requested Address: " + sMapAddress +  "\n"
				+ "Returned Lat/Lng: " + sLatLng +  "\n"
				,
				"SMOrderHeader.geocodeAddress",
				"[1512766204]");
			
		} catch (Exception e) {
			this.setM_sgeocode(SMGeocoder.EMPTY_GEOCODE);	
			throw new Exception(e.getMessage());
		} 
			

		if(iAttemptNo > 1) {
			//TODO check smoptions for api key
			super.addStatusMessage("Mutiple geocode requests had to be sent. A geocode API Key is recomended ");
		}
		
		if (sLatLng.compareToIgnoreCase("") !=0){
			this.setM_sgeocode(sLatLng);
		}
	}
	private void resetOrderNumber(String sCurrentID, String sCurrentOrderNumber, String sCurrentTrimmedOrderNumber){
		setM_siID(sCurrentID);
		setM_sOrderNumber(sCurrentOrderNumber);
		setM_strimmedordernumber(sCurrentTrimmedOrderNumber);
		for (int i = 0; i < m_arrDetails.size(); i++){
			m_arrDetails.get(i).setM_dUniqueOrderID(sCurrentID);
			m_arrDetails.get(i).setM_strimmedordernumber(sCurrentTrimmedOrderNumber);
		}
	}
	private void processSaveTransaction(
			Connection conn, 
			String sUserID, 
			String sUserFullName, 
			boolean bIsNewOrder,
			String sEditProcess) throws SQLException{
		
		SMOption smopt = new SMOption();
		
		if (bIsNewOrder){
			//Get the new order number and ID:
			if (bDebugMode){
				System.out.println("[1332266509]: getting next order number.");
			}
			smopt = readNextOrderNumber(conn);
			
			//If it's a new order, update the order lines with the uniqueifier and the order number:
			for (int i = 0; i < m_arrDetails.size(); i++){
				m_arrDetails.get(i).setM_dUniqueOrderID(getM_siID());
				m_arrDetails.get(i).setM_strimmedordernumber(getM_strimmedordernumber());
			}
			//Insert the record:
			insertHeader(conn, sUserID, sUserFullName);
		}else{
			//Update the record:
			updateHeader(conn, sUserID, sUserFullName, sEditProcess);
		}
		
		//We don't have to delete all the lines here - 
		//We can just update the lines here because we don't allow lines to be deleted or their detail number
		//to be changed in this class.  So that means that all the lines have their correct detail numbers
		//and there are no gaps between the lines, as there would be if one was deleted
		//deleteAllLines(conn);
		
		//Now actually save the lines:
		for (int i = 0; i < m_arrDetails.size(); i++){
			try {
				//If this is a quote, then all the line booked dates should be set to 00/00/0000 and the 
				//qty shipped should always equal the qty ordered:
				if (getM_iOrderType().compareToIgnoreCase(Integer.toString(SMTableorderheaders.ORDERTYPE_QUOTE)) == 0){
					m_arrDetails.get(i).setM_datLineBookedDate("00/00/0000");
					m_arrDetails.get(i).setM_dQtyShipped(m_arrDetails.get(i).getM_dQtyOrdered());
				}
				m_arrDetails.get(i).save_line(conn);
			} catch (Exception e) {
				throw new SQLException("Line number " + m_arrDetails.get(i).getM_iLineNumber() 
					+ " for item '" + m_arrDetails.get(i).getM_sItemNumber() + "' - " 
					+ e.getMessage());
			}
		}
		
		//If a new order was inserted, Update next order number and uniquefier:
		if (bIsNewOrder){
			try {
				incrementOrderNumber(smopt, conn, sUserID, sUserFullName);
			} catch (Exception e) {
				throw new SQLException("Error incrementing order number - " + e.getMessage());
			}
		}
	
		//Update the Speed Search records:
		updateSpeedSearch(conn);
	}

	private void updateSpeedSearch(Connection conn) throws SQLException{
		boolean bSSRecordAlreadyExists = false;
		String SQL = "SELECT"
			+ " " + SMTablessorderheaders.ORDNUMBER
			+ " FROM " + SMTablessorderheaders.TableName
			+ " WHERE ("
				+ "(" + SMTablessorderheaders.ORDNUMBER + " = '" 
					+ clsStringFunctions.PadLeft(getM_sOrderNumber().trim(), " ",
						SMTableorderheaders.sOrderNumberPaddedLength) + "')"
			+ ")"
			;
		if (bDebugMode){
			System.out.println("[1332271000]: " + clsDateAndTimeConversions.now("yyyyMMdd HH:mm:ss:SSS") + SQL);
		}
		try {
			ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
			if (rs.next()){
				bSSRecordAlreadyExists = true;
			}
			rs.close();
		} catch (Exception e) {
			throw new SQLException("Error checking for existing Speed Search record with SQL: " + SQL 
				+ " - " + e.getMessage());
		}
		
		if (bSSRecordAlreadyExists){
			SQL = " UPDATE " + SMTablessorderheaders.TableName
				+ " SET " + SMTablessorderheaders.BILADDR1 + " = '"	
					+ clsDatabaseFunctions.FormatSQLStatement(truncateString(getM_sBillToAddressLine1(), 
					SMTablessorderheaders.BILADDR1Length)) + "'"
				+ ", " + SMTablessorderheaders.BILADDR2 + " = '" 
					+ clsDatabaseFunctions.FormatSQLStatement(truncateString(getM_sBillToAddressLine2(),
					SMTablessorderheaders.BILADDR2Length)) + "'"
				+ ", " + SMTablessorderheaders.BILADDR3 + " = '" 
					+ clsDatabaseFunctions.FormatSQLStatement(truncateString(getM_sBillToAddressLine3(),
					SMTablessorderheaders.BILADDR3Length)) + "'"
				+ ", " + SMTablessorderheaders.BILADDR4 + " = '"
					+ clsDatabaseFunctions.FormatSQLStatement(truncateString(getM_sBillToAddressLine4(),
					SMTablessorderheaders.BILADDR4Length)) + "'"
				+ ", " + SMTablessorderheaders.BILALLADDRESSES + " = '"
					+ clsDatabaseFunctions.FormatSQLStatement(getM_sBillToAddressLine1().trim() + " "
					+ getM_sBillToAddressLine2().trim() + " "
					+ getM_sBillToAddressLine3().trim() + " "
					+ getM_sBillToAddressLine4().trim()) + "'"
				+ ", " + SMTablessorderheaders.BILCITY + " = '"	
					+ clsDatabaseFunctions.FormatSQLStatement(truncateString(getM_sBillToCity(),
					SMTablessorderheaders.BILCITYLength)) + "'"
				+ ", " + SMTablessorderheaders.BILCONTACT + " = '"	
					+ clsDatabaseFunctions.FormatSQLStatement(truncateString(getM_sBilltoContact(),
					SMTablessorderheaders.BILCONTACTLength)) + "'"
				+ ", " + SMTablessorderheaders.BILFAX + " = '" 
					+ clsDatabaseFunctions.FormatSQLStatement(truncateString(getM_sBillToFax(),
					SMTablessorderheaders.BILFAXLength)) + "'"
				+ ", " + SMTablessorderheaders.BILNAME + " = '" 
					+ clsDatabaseFunctions.FormatSQLStatement(truncateString(getM_sBillToName(),
					SMTablessorderheaders.BILNAMELength)) + "'"
				+ ", " + SMTablessorderheaders.BILPHONE + " = '" 
					+ clsDatabaseFunctions.FormatSQLStatement(truncateString(getM_sBilltoPhone(),
					SMTablessorderheaders.BILPHONELength)) + "'"
				+ ", " + SMTablessorderheaders.BILSTATE + " = '" 
					+ clsDatabaseFunctions.FormatSQLStatement(truncateString(getM_sBillToState(),
					SMTablessorderheaders.BILSTATELength)) + "'"
				+ ", " + SMTablessorderheaders.BILZIP + " = '" 
					+ clsDatabaseFunctions.FormatSQLStatement(truncateString(getM_sBillToZip(),
					SMTablessorderheaders.BILZIPLength)) + "'"
				+ ", " + SMTablessorderheaders.COMMENT + " = '" 
					+ clsDatabaseFunctions.FormatSQLStatement(truncateString(getM_sTicketComments(),
					SMTablessorderheaders.COMMENTLength)) + "'"
				+ ", " + SMTablessorderheaders.CUSTOMER + " = '" 
					+ clsDatabaseFunctions.FormatSQLStatement(truncateString(getM_sCustomerCode(),
					SMTablessorderheaders.CUSTOMERLength)) + "'"
				+ ", " + SMTablessorderheaders.LOCATION + " = '" 
					+ clsDatabaseFunctions.FormatSQLStatement(truncateString(getM_sLocation(), 
					SMTablessorderheaders.LOCATIONLength)) + "'"						
				//Can't update this field because it is part of the primary key:
				//+ ", " + SMTablessorderheaders.ORDDATE + " = " 
				//	+ SMUtilities.stdDateStringToSQLDateString(getM_datOrderDate()).replace("-", "")						
				+ ", " + SMTablessorderheaders.ORDNUMBER + " = '" 
					+ clsDatabaseFunctions.FormatSQLStatement(clsStringFunctions.PadLeft(getM_sOrderNumber().trim(), " ", 
							SMTableorderheaders.sOrderNumberPaddedLength)) + "'"						
				+ ", " + SMTablessorderheaders.PONUMBER + " = '" 
					+ clsDatabaseFunctions.FormatSQLStatement(truncateString(getM_sPONumber(),
					SMTablessorderheaders.PONUMBERLength)) + "'"
				+ ", " + SMTablessorderheaders.SALESPER1 + " = '" 
					+ clsDatabaseFunctions.FormatSQLStatement(truncateString(getM_sSalesperson(), 
					SMTablessorderheaders.SALESPER1Length)) + "'"
				+ ", " + SMTablessorderheaders.SHIPTO + " = '" 
					+ clsDatabaseFunctions.FormatSQLStatement(truncateString(getM_sShipToCode(),
					SMTablessorderheaders.SHIPTOLength)) + "'"						
				+ ", " + SMTablessorderheaders.SHIPVIA + " = '" 
					+ clsDatabaseFunctions.FormatSQLStatement(truncateString(getM_sServiceTypeCode(),
					SMTablessorderheaders.SHIPVIALength)) + "'"						
				+ ", " + SMTablessorderheaders.SHPADDR1 + " = '" 
					+ clsDatabaseFunctions.FormatSQLStatement(truncateString(getM_sShipToAddress1(),
					SMTablessorderheaders.SHPADDR1Length)) + "'"						
				+ ", " + SMTablessorderheaders.SHPADDR2 + " = '" 
					+ clsDatabaseFunctions.FormatSQLStatement(truncateString(getM_sShipToAddress2(),
					SMTablessorderheaders.SHPADDR2Length)) + "'"						
				+ ", " + SMTablessorderheaders.SHPADDR3 + " = '" 
					+ clsDatabaseFunctions.FormatSQLStatement(truncateString(getM_sShipToAddress3(),
					SMTablessorderheaders.SHPADDR3Length)) + "'"						
				+ ", " + SMTablessorderheaders.SHPADDR4 + " = '" 
					+ clsDatabaseFunctions.FormatSQLStatement(truncateString(getM_sShipToAddress4(),
					SMTablessorderheaders.SHPADDR4Length)) + "'"						
					+ ", " + SMTablessorderheaders.SHPALLADDRESSES + " = '"
					+ clsDatabaseFunctions.FormatSQLStatement(getM_sShipToAddress1().trim() + " "
					+ getM_sShipToAddress2().trim() + " "
					+ getM_sShipToAddress3().trim() + " "
					+ getM_sShipToAddress4().trim()) + "'"				
				+ ", " + SMTablessorderheaders.SHPCITY + " = '" 
					+ clsDatabaseFunctions.FormatSQLStatement(truncateString(getM_sShipToCity(),
					SMTablessorderheaders.SHPCITYLength)) + "'"						
				+ ", " + SMTablessorderheaders.SHPCONTACT + " = '" 
					+ clsDatabaseFunctions.FormatSQLStatement(truncateString(getM_sShiptoContact(),
					SMTablessorderheaders.SHPCONTACTLength)) + "'"						
				+ ", " + SMTablessorderheaders.SHPFAX + " = '" 
					+ clsDatabaseFunctions.FormatSQLStatement(truncateString(getM_sShipToFax(),
					SMTablessorderheaders.SHPFAXLength)) + "'"						
				+ ", " + SMTablessorderheaders.SHPNAME + " = '" 
					+ clsDatabaseFunctions.FormatSQLStatement(truncateString(getM_sShipToName(),
					SMTablessorderheaders.SHPNAMELength)) + "'"						
				+ ", " + SMTablessorderheaders.SHPPHONE + " = '" 
					+ clsDatabaseFunctions.FormatSQLStatement(truncateString(getM_sShiptoPhone(),
					SMTablessorderheaders.SHPPHONELength)) + "'"						
				+ ", " + SMTablessorderheaders.SHPSTATE + " = '" 
					+ clsDatabaseFunctions.FormatSQLStatement(truncateString(getM_sShipToState(),
					SMTablessorderheaders.SHPSTATELength)) + "'"						
				+ ", " + SMTablessorderheaders.SHPZIP + " = '" 
					+ clsDatabaseFunctions.FormatSQLStatement(truncateString(getM_sShipToZip(),
					SMTablessorderheaders.SHPZIPLength)) + "'"						
				+ ", " + SMTablessorderheaders.TAUTH1 + " = ''"						
				+ ", " + SMTablessorderheaders.TAXGROUP + " = '" 
					+ clsDatabaseFunctions.FormatSQLStatement(truncateString(getstaxjurisdiction(),
					SMTablessorderheaders.TAXGROUPLength)) + "'"						
				+ ", " + SMTablessorderheaders.TCLASS1 + " = 0"
				//+ ", " + SMTablessorderheaders.TEXEMPT2 + " = ''"
				+ " WHERE ("
					+ "(" + SMTablessorderheaders.ORDNUMBER + " = '" 
						+ clsStringFunctions.PadLeft(getM_sOrderNumber().trim(), " ", 
							SMTableorderheaders.sOrderNumberPaddedLength) + "')"
				+ ")"
			;
		}else{
			SQL = "INSERT INTO " + SMTablessorderheaders.TableName + "("
			+ SMTablessorderheaders.BILADDR1
			+ ", " + SMTablessorderheaders.BILADDR2
			+ ", " + SMTablessorderheaders.BILADDR3
			+ ", " + SMTablessorderheaders.BILADDR4
			+ ", " + SMTablessorderheaders.BILALLADDRESSES
			+ ", " + SMTablessorderheaders.BILCITY
			+ ", " + SMTablessorderheaders.BILCONTACT
			+ ", " + SMTablessorderheaders.BILFAX
			+ ", " + SMTablessorderheaders.BILNAME
			+ ", " + SMTablessorderheaders.BILPHONE
			// 10
			+ ", " + SMTablessorderheaders.BILSTATE
			+ ", " + SMTablessorderheaders.BILZIP
			+ ", " + SMTablessorderheaders.COMMENT
			+ ", " + SMTablessorderheaders.CUSTOMER
			+ ", " + SMTablessorderheaders.LOCATION
			+ ", " + SMTablessorderheaders.ORDDATE
			+ ", " + SMTablessorderheaders.ORDNUMBER
			+ ", " + SMTablessorderheaders.PONUMBER
			+ ", " + SMTablessorderheaders.SALESPER1
			+ ", " + SMTablessorderheaders.SHIPTO
			//20
			+ ", " + SMTablessorderheaders.SHIPVIA
			+ ", " + SMTablessorderheaders.SHPADDR1
			+ ", " + SMTablessorderheaders.SHPADDR2
			+ ", " + SMTablessorderheaders.SHPADDR3
			+ ", " + SMTablessorderheaders.SHPADDR4
			+ ", " + SMTablessorderheaders.SHPALLADDRESSES
			+ ", " + SMTablessorderheaders.SHPCITY
			+ ", " + SMTablessorderheaders.SHPCONTACT
			+ ", " + SMTablessorderheaders.SHPFAX
			+ ", " + SMTablessorderheaders.SHPNAME
			// 30
			+ ", " + SMTablessorderheaders.SHPPHONE
			+ ", " + SMTablessorderheaders.SHPSTATE
			+ ", " + SMTablessorderheaders.SHPZIP
			+ ", " + SMTablessorderheaders.TAUTH1
			+ ", " + SMTablessorderheaders.TAXGROUP
			+ ", " + SMTablessorderheaders.TCLASS1
			//+ ", " + SMTablessorderheaders.TEXEMPT1
			//+ ", " + SMTablessorderheaders.TEXEMPT2
			//36
			+ ") VALUES ("
			+ "'" + clsDatabaseFunctions.FormatSQLStatement(truncateString(getM_sBillToAddressLine1().trim(), SMTablessorderheaders.BILADDR1Length)) + "'"
			+ ", '" + clsDatabaseFunctions.FormatSQLStatement(truncateString(getM_sBillToAddressLine2().trim(), SMTablessorderheaders.BILADDR2Length)) + "'"
			+ ", '" + clsDatabaseFunctions.FormatSQLStatement(truncateString(getM_sBillToAddressLine3().trim(), SMTablessorderheaders.BILADDR3Length)) + "'"
			+ ", '" + clsDatabaseFunctions.FormatSQLStatement(truncateString(getM_sBillToAddressLine4().trim(), SMTablessorderheaders.BILADDR4Length)) + "'"
			+ ", '" + clsDatabaseFunctions.FormatSQLStatement(getM_sBillToAddressLine1().trim() + " " + getM_sBillToAddressLine2().trim() + " "
				+ getM_sBillToAddressLine3().trim() + " " + getM_sBillToAddressLine4().trim()) + "'"
			+ ", '" + clsDatabaseFunctions.FormatSQLStatement(truncateString(getM_sBillToCity().trim(), SMTablessorderheaders.BILCITYLength)) + "'"
			+ ", '" + clsDatabaseFunctions.FormatSQLStatement(truncateString(getM_sBilltoContact().trim(), SMTablessorderheaders.BILCONTACTLength)) + "'"
			+ ", '" + clsDatabaseFunctions.FormatSQLStatement(truncateString(getM_sBillToFax().trim(), SMTablessorderheaders.BILFAXLength)) + "'"
			+ ", '" + clsDatabaseFunctions.FormatSQLStatement(truncateString(getM_sBillToName().trim(), SMTablessorderheaders.BILNAMELength)) + "'"
			+ ", '" + clsDatabaseFunctions.FormatSQLStatement(truncateString(getM_sBilltoPhone().trim(), SMTablessorderheaders.BILPHONELength)) + "'"
			// 10
			+ ", '" + clsDatabaseFunctions.FormatSQLStatement(truncateString(getM_sBillToState().trim(), SMTablessorderheaders.BILSTATELength)) + "'"
			+ ", '" + clsDatabaseFunctions.FormatSQLStatement(truncateString(getM_sBillToZip().trim(), SMTablessorderheaders.BILZIPLength)) + "'"
			+ ", '" + clsDatabaseFunctions.FormatSQLStatement(truncateString(getM_sTicketComments().trim(), SMTablessorderheaders.COMMENTLength)) + "'"
			+ ", '" + clsDatabaseFunctions.FormatSQLStatement(truncateString(getM_sCustomerCode().trim(), SMTablessorderheaders.CUSTOMERLength)) + "'"
			+ ", '" + clsDatabaseFunctions.FormatSQLStatement(truncateString(getM_sLocation().trim(), SMTablessorderheaders.LOCATIONLength)) + "'"
			+ ", " + clsDateAndTimeConversions.stdDateStringToSQLDateString(getM_datOrderDate()).replace("-", "")						
			+ ", '" + clsStringFunctions.PadLeft(getM_sOrderNumber().trim(), " ", 
					SMTableorderheaders.sOrderNumberPaddedLength) + "'"
			+ ", '" + clsDatabaseFunctions.FormatSQLStatement(truncateString(getM_sPONumber().trim(), SMTablessorderheaders.PONUMBERLength)) + "'"
			+ ", '" + clsDatabaseFunctions.FormatSQLStatement(truncateString(getM_sSalesperson().trim(), SMTablessorderheaders.SALESPER1Length)) + "'"
			+ ", '" + clsDatabaseFunctions.FormatSQLStatement(truncateString(getM_sShipToCode().trim(), SMTablessorderheaders.SHIPTOLength)) + "'"
			// 20
			+ ", '" + clsDatabaseFunctions.FormatSQLStatement(truncateString(getM_sServiceTypeCode().trim(), SMTablessorderheaders.SHIPVIALength)) + "'"
			+ ", '" + clsDatabaseFunctions.FormatSQLStatement(truncateString(getM_sShipToAddress1().trim(), SMTablessorderheaders.SHPADDR1Length)) + "'"
			+ ", '" + clsDatabaseFunctions.FormatSQLStatement(truncateString(getM_sShipToAddress2().trim(), SMTablessorderheaders.SHPADDR2Length)) + "'"
			+ ", '" + clsDatabaseFunctions.FormatSQLStatement(truncateString(getM_sShipToAddress3().trim(), SMTablessorderheaders.SHPADDR3Length)) + "'"
			+ ", '" + clsDatabaseFunctions.FormatSQLStatement(truncateString(getM_sShipToAddress4().trim(), SMTablessorderheaders.SHPADDR4Length)) + "'"
			+ ", '" + clsDatabaseFunctions.FormatSQLStatement(getM_sShipToAddress1().trim() + " " + getM_sShipToAddress2().trim() + " "
				+ getM_sShipToAddress3().trim() + " " + getM_sShipToAddress4().trim()) + "'"
			+ ", '" + clsDatabaseFunctions.FormatSQLStatement(truncateString(getM_sShipToCity().trim(), SMTablessorderheaders.SHPCITYLength)) + "'"
			+ ", '" + clsDatabaseFunctions.FormatSQLStatement(truncateString(getM_sShiptoContact().trim(), SMTablessorderheaders.SHPCONTACTLength)) + "'"
			+ ", '" + clsDatabaseFunctions.FormatSQLStatement(truncateString(getM_sShipToFax().trim(), SMTablessorderheaders.SHPFAXLength)) + "'"
			+ ", '" + clsDatabaseFunctions.FormatSQLStatement(truncateString(getM_sShipToName().trim(), SMTablessorderheaders.SHPNAMELength)) + "'"
			// 30
			+ ", '" + clsDatabaseFunctions.FormatSQLStatement(truncateString(getM_sShiptoPhone().trim(), SMTablessorderheaders.SHPPHONELength)) + "'"
			+ ", '" + clsDatabaseFunctions.FormatSQLStatement(truncateString(getM_sShipToState().trim(), SMTablessorderheaders.SHPSTATELength)) + "'"
			+ ", '" + clsDatabaseFunctions.FormatSQLStatement(truncateString(getM_sShipToZip().trim(), SMTablessorderheaders.SHPZIPLength)) + "'"
			+ ", ''"
			+ ", '" + clsDatabaseFunctions.FormatSQLStatement(truncateString(getstaxjurisdiction().trim(), SMTablessorderheaders.TAXGROUPLength)) + "'"
			+ ", " + "0"
			//+ ", ''"
			//+ ", ''"
			// 36
			+ ")"
			;
		}
		if (bDebugMode){
			System.out.println("[1332271001]:" + clsDateAndTimeConversions.now("yyyyMMdd HH:mm:ss:SSS") + SQL);
		}

		try {
			Statement stmt = conn.createStatement();
			stmt.execute(SQL);
		} catch (Exception e) {
			throw new SQLException("Error writing Speed Search record with SQL: " + SQL 
				+ " - " + e.getMessage());
		}
	}
	private String truncateString(String sTest, int iMaxLength){
		
		if (sTest == null){
			return "";
		}
		
		if (sTest.length() > iMaxLength){
			return sTest.substring(0, iMaxLength - 1);
		}else{
			return sTest;
		}
	}
	private void incrementOrderNumber(SMOption smopt, Connection conn, String sUserID, String sUserFullName) throws SQLException{
		
		SMOption currentsmopt = new SMOption();
		if (bDebugMode){
			System.out.println("[1332266459]: incrementing order number in SMOption");
		}
		if (!currentsmopt.load(conn)){
			throw new SQLException("Could not load SMOptions to increment order number.");
		}
		
		if (
			(currentsmopt.getLastEditDate().compareToIgnoreCase(smopt.getLastEditDate()) != 0)
			|| (currentsmopt.getLastEditTime().compareToIgnoreCase(smopt.getLastEditTime()) != 0)
		){
			throw new SQLException(
				"User '" + currentsmopt.getLastEditUserFullName() 
				+ "' was accessing the system data at the exact same time." 
				+ "  Please try to save again."
			);
		}
		currentsmopt.setLastEditProcess("ADDING ORDER");
		currentsmopt.setLastEditUserFullName(sUserFullName);
		currentsmopt.setLastEditUserID(sUserID);
		currentsmopt.setNextOrderNumber(Long.toString(Long.parseLong(getM_strimmedordernumber().trim()) + 1));
		currentsmopt.setNextOrderUniquifier(Long.parseLong(getM_siID().trim()) + 1);
		try {
			currentsmopt.save(conn);
		} catch (Exception e) {
			throw new SQLException("Error saving SMOptions - " + e.getMessage());
		}
	}
	private void checkConcurrency(String sDBIB, ServletContext context)
		throws SQLException{
		
		String sConcurrencyError = "";
		String SQL = "SELECT"
			+ " " + SMTableorderheaders.LASTEDITDATE
			+ ", " + SMTableorderheaders.LASTEDITPROCESS
			+ ", " + SMTableorderheaders.LASTEDITTIME
			+ ", " + SMTableorderheaders.LASTEDITUSERFULLNAME
			+ ", " + SMTableorderheaders.LASTEDITUSERID
			+ " FROM " + SMTableorderheaders.TableName
			+ " WHERE ("
				+ "(" + SMTableorderheaders.strimmedordernumber + " = '" + getM_strimmedordernumber() + "')"
			+ ")"
			;
		if (bDebugMode){
			System.out.println("[1332266299]:" + clsDateAndTimeConversions.now("yyyyMMdd HH:mm:ss:SSS") + SQL);
		}
		try{
			ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, context, sDBIB, "MySQL", this.toString() + ".checkConcurrency(1)");
			if (rs.next()){
				//If the STORED values for the last edit date and the last edit time don't match what we've loaded
				//in this class, then throw an exception:
				if (
					(getM_LASTEDITDATE().compareToIgnoreCase(rs.getString(SMTableorderheaders.LASTEDITDATE)) != 0)
					|| (getM_LASTEDITTIME().compareToIgnoreCase(rs.getString(SMTableorderheaders.LASTEDITTIME)) != 0)
				){
					sConcurrencyError = rs.getString(SMTableorderheaders.LASTEDITUSERFULLNAME)
						+ " has updated this order on "
						+ rs.getString(SMTableorderheaders.LASTEDITDATE)
						+ " at "
						+ rs.getString(SMTableorderheaders.LASTEDITTIME)
						+ " with the process '"
						+ rs.getString(SMTableorderheaders.LASTEDITPROCESS)
						+ "' since you started editing it"
						+ ", but the date stamp on your copy of the order is " + getM_LASTEDITDATE()
						+ " and the time stamp on your copy of the order is " + getM_LASTEDITTIME()
						+ ".  You will have to reload the "
						+ "order before you can save your changes."
					;
				}
			}else{
				throw new Exception("No record found for this order number when checking concurrency.");
			}
			rs.close();
		}catch (Exception e){
			throw new SQLException("Error [1535737119] checking concurrency with SQL: " + SQL + " - " + e.getMessage());
		}
		if (sConcurrencyError.compareToIgnoreCase("") != 0){
			throw new SQLException("Error [1535737120] " + sConcurrencyError);
		}
	}
	private SMOption readNextOrderNumber(Connection conn) throws SQLException{
		
		SMOption smopt = new SMOption();
		if (!smopt.load(conn)){
			throw new SQLException("Could not load SMOptions record - " + smopt.getErrorMessage());
		}
		setM_siID(smopt.getNextOrderUniquifier().trim());
		setM_sOrderNumber(clsStringFunctions.PadLeft(
			smopt.getNextOrderNumber(), " ", SMTableorderheaders.sOrderNumberPaddedLength));
		setM_strimmedordernumber(smopt.getNextOrderNumber().trim());
		
		return smopt;
	}
	private void insertHeader(Connection conn, String sUserID, String sUserFullName) throws SQLException{
		String SQL = "INSERT INTO " + SMTableorderheaders.TableName + "("
			+ " " + SMTableorderheaders.bdtotalcontractamount
			+ ", " + SMTableorderheaders.bdtotalmarkup
			+ ", " + SMTableorderheaders.bdtruckdays
			+ ", " + SMTableorderheaders.bddepositamount
			+ ", " + SMTableorderheaders.datExpectedShipDate
			+ ", " + SMTableorderheaders.datLastPostingDate
			+ ", " + SMTableorderheaders.datOrderCanceledDate
			+ ", " + SMTableorderheaders.datOrderCreationDate
			+ ", " + SMTableorderheaders.datOrderDate
			+ ", " + SMTableorderheaders.datwarrantyexpiration
			//10
			+ ", " + SMTableorderheaders.dEstimatedHour
			+ ", " + SMTableorderheaders.bdordertaxamount
			+ ", " + SMTableorderheaders.dOrderUniqueifier
			+ ", " + SMTableorderheaders.dPrePostingInvoiceDiscountAmount
			+ ", " + SMTableorderheaders.dPrePostingInvoiceDiscountPercentage
			+ ", " + SMTableorderheaders.bdtaxbase
			+ ", " + SMTableorderheaders.dTotalAmountItems
			+ ", " + SMTableorderheaders.iCustomerDiscountLevel
			+ ", " + SMTableorderheaders.iNextDetailNumber
			+ ", " + SMTableorderheaders.iNumberOfInvoices
			//20
			+ ", " + SMTableorderheaders.iNumberOfLinesOnOrder
			+ ", " + SMTableorderheaders.iOnHold
			+ ", " + SMTableorderheaders.iOrderSourceID
			+ ", " + SMTableorderheaders.iOrderType
			+ ", " + SMTableorderheaders.iSalesGroup
			+ ", " + SMTableorderheaders.LASTEDITDATE
			+ ", " + SMTableorderheaders.LASTEDITPROCESS
			+ ", " + SMTableorderheaders.LASTEDITTIME
			+ ", " + SMTableorderheaders.LASTEDITUSERFULLNAME
			+ ", " + SMTableorderheaders.LASTEDITUSERID
			+ ", " + SMTableorderheaders.mDirections
			//30
			+ ", " + SMTableorderheaders.mFieldNotes
			+ ", " + SMTableorderheaders.mInternalComments
			+ ", " + SMTableorderheaders.mInvoiceComments
			+ ", " + SMTableorderheaders.mTicketComments
			+ ", " + SMTableorderheaders.sBillToAddressLine1
			+ ", " + SMTableorderheaders.sBillToAddressLine2
			+ ", " + SMTableorderheaders.sBillToAddressLine3
			+ ", " + SMTableorderheaders.sBillToAddressLine4
			+ ", " + SMTableorderheaders.sBillToCity
			+ ", " + SMTableorderheaders.sBillToContact
			//40
			+ ", " + SMTableorderheaders.sBillToCountry
			+ ", " + SMTableorderheaders.sBillToFax
			+ ", " + SMTableorderheaders.sBillToName
			+ ", " + SMTableorderheaders.sBillToPhone
			+ ", " + SMTableorderheaders.sBillToState
			+ ", " + SMTableorderheaders.sBillToZip
			+ ", " + SMTableorderheaders.scarpenterrate
			+ ", " + SMTableorderheaders.sclonedfrom
			+ ", " + SMTableorderheaders.sCustomerCode
			+ ", " + SMTableorderheaders.sCustomerControlAcctSet
			//50
			+ ", " + SMTableorderheaders.sDefaultItemCategory
			+ ", " + SMTableorderheaders.sDefaultPriceListCode
			+ ", " + SMTableorderheaders.selectricianrate
			+ ", " + SMTableorderheaders.sEmailAddress
			+ ", " + SMTableorderheaders.sgeocode
			+ ", " + SMTableorderheaders.slaborerrate
			+ ", " + SMTableorderheaders.sLastInvoiceNumber
			+ ", " + SMTableorderheaders.sLocation
			+ ", " + SMTableorderheaders.sOrderCreatedByFullName
			+ ", " + SMTableorderheaders.lOrderCreatedByID
			+ ", " + SMTableorderheaders.sOrderNumber
			//60
			+ ", " + SMTableorderheaders.sOrderSourceDesc
			+ ", " + SMTableorderheaders.sPONumber
			+ ", " + SMTableorderheaders.sPrePostingInvoiceDiscountDesc
			+ ", " + SMTableorderheaders.sSalesperson
			+ ", " + SMTableorderheaders.ssecondarybilltophone
			+ ", " + SMTableorderheaders.ssecondaryshiptophone
			+ ", " + SMTableorderheaders.sServiceTypeCode
			+ ", " + SMTableorderheaders.sServiceTypeCodeDescription
			+ ", " + SMTableorderheaders.sShipToAddress1
			+ ", " + SMTableorderheaders.sShipToAddress2
			//70
			+ ", " + SMTableorderheaders.sShipToAddress3
			+ ", " + SMTableorderheaders.sShipToAddress4
			+ ", " + SMTableorderheaders.sShipToCity
			+ ", " + SMTableorderheaders.sShipToCode
			+ ", " + SMTableorderheaders.sShipToContact
			+ ", " + SMTableorderheaders.sShipToCountry
			+ ", " + SMTableorderheaders.sShipToFax
			+ ", " + SMTableorderheaders.sShipToName
			+ ", " + SMTableorderheaders.sShipToPhone
			+ ", " + SMTableorderheaders.sShipToState
			//80
			+ ", " + SMTableorderheaders.sShipToZip
			+ ", " + SMTableorderheaders.sSpecialWageRate
			+ ", " + SMTableorderheaders.staxjurisdiction
			+ ", " + SMTableorderheaders.sTerms
			+ ", " + SMTableorderheaders.strimmedordernumber
			+ ", " + SMTableorderheaders.swagescalenotes
			+ ", " + SMTableorderheaders.sshiptoemail
			+ ", " + SMTableorderheaders.sgdoclink
			+ ", " + SMTableorderheaders.lbidid
			+ ", " + SMTableorderheaders.squotedescription
			//90
			+ ", " + SMTableorderheaders.itaxid
			+ ", " + SMTableorderheaders.staxtype
			+ ", " + SMTableorderheaders.sinvoicingcontact
			+ ", " + SMTableorderheaders.sinvoicingemail
			+ ", " + SMTableorderheaders.sinvoicingnotes
			+ ", " + SMTableorderheaders.idoingbusinessasaddressid
			+ ") VALUES ("
			+ " " + getM_bdtotalcontractamount().replace(",", "")
			+ ", " + getM_bdtotalmarkup().replace(",", "")
			+ ", " + getM_bdtruckdays().replace(",", "")
			+ ", " + getM_bddepositamount().replace(",", "")
//			+ ", '" + SMUtilities.stdDateStringToSQLDateString(getM_datCompletedDate()) + "'"
			+ ", '" + clsDateAndTimeConversions.stdDateStringToSQLDateString(getM_datExpectedShipDate()) + "'"
			+ ", NOW()" //Last posting date
			+ ", '" + clsDateAndTimeConversions.stdDateStringToSQLDateString(getM_datOrderCanceledDate()) + "'"
			+ ", NOW()" //Order creation date
			+ ", '" + clsDateAndTimeConversions.stdDateStringToSQLDateString(getM_datOrderDate()) + "'"
			+ ", '" + clsDateAndTimeConversions.stdDateStringToSQLDateString(getM_datwarrantyexpiration()) + "'"
			//10
			+ ", " + getM_dEstimatedHour().replace(",", "")
			+ ", " + getsordersalestaxamount().replace(",", "")
			+ ", " + getM_siID()
			+ ", " + getM_dPrePostingInvoiceDiscountAmount().replace(",", "")
			+ ", " + getM_dPrePostingInvoiceDiscountPercentage().replace(",", "")
			+ ", " + getM_dTaxBase().replace(",", "")
			+ ", " + getM_dTotalAmountItems().replace(",", "")
			+ ", " + getM_iCustomerDiscountLevel()
			+ ", " + getM_iNextDetailNumber()
			+ ", " + getM_iNumberOfInvoices()
			//20
			+ ", " + getM_iNumberOfLinesOnOrder()
			+ ", " + getM_iOnHold()
			+ ", " + getM_iOrderSourceID()
			+ ", " + getM_iOrderType()
			+ ", " + getM_iSalesGroup()
			//+ ", " + getM_iTaxClass()
			+ ", DATE_FORMAT(NOW(), '%Y%m%d')" //LAST EDIT DATE
			+ ", 'CREATED ORDER'" //LAST EDIT PROCESS
			+ ", DATE_FORMAT(NOW(), '%k%i%s')" // LAST EDIT TIME
			+ ", '" + clsDatabaseFunctions.FormatSQLStatement(sUserFullName) + "'" //LAST EDIT USER FULL NAME
			+ ", " + sUserID  //LAST EDIT USER ID
			+ ", '" + clsDatabaseFunctions.FormatSQLStatement(getM_sDirections()) + "'"
			//30
			+ ", '" + clsDatabaseFunctions.FormatSQLStatement(getM_sFieldNotes()) + "'"
			+ ", '" + clsDatabaseFunctions.FormatSQLStatement(getM_mInternalComments()) + "'"
			+ ", '" + clsDatabaseFunctions.FormatSQLStatement(getM_mInvoiceComments()) + "'"
			+ ", '" + clsDatabaseFunctions.FormatSQLStatement(getM_sTicketComments()) + "'"
			+ ", '" + clsDatabaseFunctions.FormatSQLStatement(getM_sBillToAddressLine1()) + "'"
			+ ", '" + clsDatabaseFunctions.FormatSQLStatement(getM_sBillToAddressLine2()) + "'"
			+ ", '" + clsDatabaseFunctions.FormatSQLStatement(getM_sBillToAddressLine3()) + "'"
			+ ", '" + clsDatabaseFunctions.FormatSQLStatement(getM_sBillToAddressLine4()) + "'"
			+ ", '" + clsDatabaseFunctions.FormatSQLStatement(getM_sBillToCity()) + "'"
			+ ", '" + clsDatabaseFunctions.FormatSQLStatement(getM_sBilltoContact()) + "'"
			//40
			+ ", '" + clsDatabaseFunctions.FormatSQLStatement(getM_sBillToCountry()) + "'"
			+ ", '" + clsDatabaseFunctions.FormatSQLStatement(getM_sBillToFax()) + "'"
			+ ", '" + clsDatabaseFunctions.FormatSQLStatement(getM_sBillToName()) + "'"
			+ ", '" + clsDatabaseFunctions.FormatSQLStatement(getM_sBilltoPhone()) + "'"
			+ ", '" + clsDatabaseFunctions.FormatSQLStatement(getM_sBillToState()) + "'"
			+ ", '" + clsDatabaseFunctions.FormatSQLStatement(getM_sBillToZip()) + "'"
			+ ", '" + clsDatabaseFunctions.FormatSQLStatement(getM_scarpenterrate()) + "'"
			+ ", '" + clsDatabaseFunctions.FormatSQLStatement(getM_sClonedFrom()) + "'"
			+ ", '" + clsDatabaseFunctions.FormatSQLStatement(getM_sCustomerCode()) + "'"
			+ ", '" + clsDatabaseFunctions.FormatSQLStatement(getM_sCustomerControlAcctSet()) + "'"
			//50
			+ ", '" + clsDatabaseFunctions.FormatSQLStatement(getM_sDefaultItemCategory()) + "'"
			+ ", '" + clsDatabaseFunctions.FormatSQLStatement(getM_sDefaultPriceListCode()) + "'"
			+ ", '" + clsDatabaseFunctions.FormatSQLStatement(getM_selectricianrate()) + "'"
			+ ", '" + clsDatabaseFunctions.FormatSQLStatement(getM_sEmailAddress()) + "'"
			+ ", '" + clsDatabaseFunctions.FormatSQLStatement(getM_sgeocode()) + "'"
			+ ", '" + clsDatabaseFunctions.FormatSQLStatement(getM_slaborerrate()) + "'"
			+ ", '" + clsDatabaseFunctions.FormatSQLStatement(getM_sLastInvoiceNumber()) + "'"
			+ ", '" + clsDatabaseFunctions.FormatSQLStatement(getM_sLocation()) + "'"
			+ ", '" + clsDatabaseFunctions.FormatSQLStatement(sUserFullName) + "'" //Order created by full name
			+ ", " + sUserID 													   //Order created by ID
			+ ", '" + clsDatabaseFunctions.FormatSQLStatement(clsStringFunctions.PadLeft(
				getM_sOrderNumber().trim(), " ", SMTableorderheaders.sOrderNumberPaddedLength)) + "'"
			//60
			+ ", '" + clsDatabaseFunctions.FormatSQLStatement(getM_sOrderSourceDesc()) + "'"
			+ ", '" + clsDatabaseFunctions.FormatSQLStatement(getM_sPONumber()) + "'"
			+ ", '" + clsDatabaseFunctions.FormatSQLStatement(getM_sPrePostingInvoiceDiscountDesc()) + "'"
			+ ", '" + clsDatabaseFunctions.FormatSQLStatement(getM_sSalesperson()) + "'"
			+ ", '" + clsDatabaseFunctions.FormatSQLStatement(getM_ssecondarybilltophone()) + "'"
			+ ", '" + clsDatabaseFunctions.FormatSQLStatement(getM_ssecondaryshiptophone()) + "'"
			+ ", '" + clsDatabaseFunctions.FormatSQLStatement(getM_sServiceTypeCode()) + "'"
			+ ", '" + clsDatabaseFunctions.FormatSQLStatement(getM_sServiceTypeCodeDescription()) + "'"
			+ ", '" + clsDatabaseFunctions.FormatSQLStatement(getM_sShipToAddress1()) + "'"
			+ ", '" + clsDatabaseFunctions.FormatSQLStatement(getM_sShipToAddress2()) + "'"
			//70
			+ ", '" + clsDatabaseFunctions.FormatSQLStatement(getM_sShipToAddress3()) + "'"
			+ ", '" + clsDatabaseFunctions.FormatSQLStatement(getM_sShipToAddress4()) + "'"
			+ ", '" + clsDatabaseFunctions.FormatSQLStatement(getM_sShipToCity()) + "'"
			+ ", '" + clsDatabaseFunctions.FormatSQLStatement(getM_sShipToCode()) + "'"
			+ ", '" + clsDatabaseFunctions.FormatSQLStatement(getM_sShiptoContact()) + "'"
			+ ", '" + clsDatabaseFunctions.FormatSQLStatement(getM_sShipToCountry()) + "'"
			+ ", '" + clsDatabaseFunctions.FormatSQLStatement(getM_sShipToFax()) + "'"
			+ ", '" + clsDatabaseFunctions.FormatSQLStatement(getM_sShipToName()) + "'"
			+ ", '" + clsDatabaseFunctions.FormatSQLStatement(getM_sShiptoPhone()) + "'"
			+ ", '" + clsDatabaseFunctions.FormatSQLStatement(getM_sShipToState()) + "'"
			//80
			+ ", '" + clsDatabaseFunctions.FormatSQLStatement(getM_sShipToZip()) + "'"
			+ ", '" + clsDatabaseFunctions.FormatSQLStatement(getM_sSpecialWageRate()) + "'"
			+ ", '" + clsDatabaseFunctions.FormatSQLStatement(getstaxjurisdiction()) + "'"
			+ ", '" + clsDatabaseFunctions.FormatSQLStatement(getM_sTerms()) + "'"
			+ ", '" + clsDatabaseFunctions.FormatSQLStatement(getM_strimmedordernumber()) + "'"
			+ ", '" + clsDatabaseFunctions.FormatSQLStatement(getM_swagescalenotes()) + "'"
			+ ", '" + clsDatabaseFunctions.FormatSQLStatement(getM_sShipToEmail()) + "'"
			+ ", '" + clsDatabaseFunctions.FormatSQLStatement(getM_sGDocLink()) + "'"
			+ ", " + m_sbidid
			+ ", '" + clsDatabaseFunctions.FormatSQLStatement(getsQuoteDescription()) + "'"
			//90
			+ ", " + m_itaxid
			+ ", '" + clsDatabaseFunctions.FormatSQLStatement(getstaxtype()) + "'"
			+ ", '" + clsDatabaseFunctions.FormatSQLStatement(getM_sInvoicingContact()) + "'"
			+ ", '" + clsDatabaseFunctions.FormatSQLStatement(getM_sInvoicingEmail()) + "'"
			+ ", '" + clsDatabaseFunctions.FormatSQLStatement(getM_sInvoicingNotes()) + "'"
			+ ", " + m_idoingbusinessasaddressid

			+ ")"
			;
		if (bDebugMode){
			System.out.println("[1332266298]:" + clsDateAndTimeConversions.now("yyyyMMdd HH:mm:ss:SSS") + SQL);
		}
		try {
			Statement stmt = conn.createStatement();
			stmt.execute(SQL);
		} catch (Exception e) {
			throw new SQLException("Error inserting order header with SQL: " + SQL + " - " + e.getMessage() + ".");
		}
	}
	private void updateHeader(Connection conn, String sUserID, String sUserFullName, String sUpdateProcess) throws SQLException{

		String SQL = "UPDATE " + SMTableorderheaders.TableName
			+ " SET " + SMTableorderheaders.bdtotalcontractamount + " = " + getM_bdtotalcontractamount().replace(",", "")
			+ ", " + SMTableorderheaders.bdtotalmarkup + " = " + getM_bdtotalmarkup().replace(",", "")
			+ ", " + SMTableorderheaders.bdtruckdays + " = " + getM_bdtruckdays().replace(",", "")
			+ ", " + SMTableorderheaders.bddepositamount + " = " + getM_bddepositamount().replace(",", "")
//			+ ", " + SMTableorderheaders.datCompletedDate + " = '" + SMUtilities.stdDateStringToSQLDateString(getM_datCompletedDate()) + "'"
			+ ", " + SMTableorderheaders.datExpectedShipDate + " = '" + clsDateAndTimeConversions.stdDateStringToSQLDateString(getM_datExpectedShipDate()) + "'"
			+ ", " + SMTableorderheaders.datLastPostingDate + " = NOW()"
			+ ", " + SMTableorderheaders.datOrderCanceledDate + " = '" + clsDateAndTimeConversions.stdDateStringToSQLDateString(getM_datOrderCanceledDate()) + "'"
			//We don't update this field:
			//+ ", " + SMTableorderheaders.datOrderCreationDate + " = '" 
			+ ", " + SMTableorderheaders.datOrderDate + " = '" + clsDateAndTimeConversions.stdDateStringToSQLDateString(getM_datOrderDate()) + "'"
			+ ", " + SMTableorderheaders.datwarrantyexpiration + " = '" + clsDateAndTimeConversions.stdDateStringToSQLDateString(getM_datwarrantyexpiration()) + "'"
			+ ", " + SMTableorderheaders.dEstimatedHour + " = " + getM_dEstimatedHour().replace(",", "")
			+ ", " + SMTableorderheaders.bdordertaxamount + " = " + getsordersalestaxamount().replace(",", "")
			//+ ", " + SMTableorderheaders.dOrderUniqueifier  //Don't update this
			+ ", " + SMTableorderheaders.dPrePostingInvoiceDiscountAmount + " = " + getM_dPrePostingInvoiceDiscountAmount().replace(",", "")
			+ ", " + SMTableorderheaders.dPrePostingInvoiceDiscountPercentage + " = " + getM_dPrePostingInvoiceDiscountPercentage().replace(",", "")
			+ ", " + SMTableorderheaders.bdtaxbase + " = " + getM_dTaxBase().replace(",", "")
			+ ", " + SMTableorderheaders.dTotalAmountItems + " = " + getM_dTotalAmountItems().replace(",", "")
			+ ", " + SMTableorderheaders.iCustomerDiscountLevel + " = " + getM_iCustomerDiscountLevel()
			+ ", " + SMTableorderheaders.iNextDetailNumber + " = " + getM_iNextDetailNumber()
			+ ", " + SMTableorderheaders.iNumberOfInvoices + " = " + getM_iNumberOfInvoices()
			+ ", " + SMTableorderheaders.iNumberOfLinesOnOrder + " = " + getM_iNumberOfLinesOnOrder()
			+ ", " + SMTableorderheaders.iOnHold + " = " + getM_iOnHold()
			+ ", " + SMTableorderheaders.iOrderSourceID + " = " + getM_iOrderSourceID()
			+ ", " + SMTableorderheaders.iOrderType + " = " + getM_iOrderType()
			+ ", " + SMTableorderheaders.iSalesGroup + " = " + getM_iSalesGroup()
			+ ", " + SMTableorderheaders.LASTEDITDATE + " = DATE_FORMAT(NOW(), '%Y%m%d')"
			+ ", " + SMTableorderheaders.LASTEDITPROCESS + " = '" + sUpdateProcess + "'"
			+ ", " + SMTableorderheaders.LASTEDITTIME + " = DATE_FORMAT(NOW(), '%k%i%s')"
			+ ", " + SMTableorderheaders.LASTEDITUSERFULLNAME + " = '" + clsDatabaseFunctions.FormatSQLStatement(sUserFullName) + "'"
			+ ", " + SMTableorderheaders.LASTEDITUSERID + " = " + sUserID 
			+ ", " + SMTableorderheaders.mDirections + " = '" + clsDatabaseFunctions.FormatSQLStatement(getM_sDirections()) + "'"
			+ ", " + SMTableorderheaders.mFieldNotes + " = '" + clsDatabaseFunctions.FormatSQLStatement(getM_sFieldNotes()) + "'"
			+ ", " + SMTableorderheaders.mInternalComments + " = '" + clsDatabaseFunctions.FormatSQLStatement(getM_mInternalComments()) + "'"
			+ ", " + SMTableorderheaders.mInvoiceComments + " = '" + clsDatabaseFunctions.FormatSQLStatement(getM_mInvoiceComments()) + "'"
			+ ", " + SMTableorderheaders.mTicketComments + " = '" + clsDatabaseFunctions.FormatSQLStatement(getM_sTicketComments()) + "'"
			+ ", " + SMTableorderheaders.sBillToAddressLine1 + " = '" + clsDatabaseFunctions.FormatSQLStatement(getM_sBillToAddressLine1()) + "'"
			+ ", " + SMTableorderheaders.sBillToAddressLine2 + " = '" + clsDatabaseFunctions.FormatSQLStatement(getM_sBillToAddressLine2()) + "'"
			+ ", " + SMTableorderheaders.sBillToAddressLine3 + " = '" + clsDatabaseFunctions.FormatSQLStatement(getM_sBillToAddressLine3()) + "'"
			+ ", " + SMTableorderheaders.sBillToAddressLine4 + " = '" + clsDatabaseFunctions.FormatSQLStatement(getM_sBillToAddressLine4()) + "'"
			+ ", " + SMTableorderheaders.sBillToCity + " = '" + clsDatabaseFunctions.FormatSQLStatement(getM_sBillToCity()) + "'"
			+ ", " + SMTableorderheaders.sBillToContact + " = '" + clsDatabaseFunctions.FormatSQLStatement(getM_sBilltoContact()) + "'"
			+ ", " + SMTableorderheaders.sBillToCountry + " = '" + clsDatabaseFunctions.FormatSQLStatement(getM_sBillToCountry()) + "'"
			+ ", " + SMTableorderheaders.sBillToFax + " = '" + clsDatabaseFunctions.FormatSQLStatement(getM_sBillToFax()) + "'"
			+ ", " + SMTableorderheaders.sBillToName + " = '" + clsDatabaseFunctions.FormatSQLStatement(getM_sBillToName()) + "'"
			+ ", " + SMTableorderheaders.sBillToPhone + " = '" + clsDatabaseFunctions.FormatSQLStatement(getM_sBilltoPhone()) + "'"
			+ ", " + SMTableorderheaders.sBillToState + " = '" + clsDatabaseFunctions.FormatSQLStatement(getM_sBillToState()) + "'"
			+ ", " + SMTableorderheaders.sBillToZip + " = '" + clsDatabaseFunctions.FormatSQLStatement(getM_sBillToZip()) + "'"
			+ ", " + SMTableorderheaders.scarpenterrate + " = '" + clsDatabaseFunctions.FormatSQLStatement(getM_scarpenterrate()) + "'"
			+ ", " + SMTableorderheaders.sclonedfrom + " = '" + clsDatabaseFunctions.FormatSQLStatement(getM_sClonedFrom()) + "'"
			+ ", " + SMTableorderheaders.sCustomerCode + " = '" + clsDatabaseFunctions.FormatSQLStatement(getM_sCustomerCode()) + "'"
			+ ", " + SMTableorderheaders.sCustomerControlAcctSet + " = '" + clsDatabaseFunctions.FormatSQLStatement(getM_sCustomerControlAcctSet()) + "'"
			+ ", " + SMTableorderheaders.sDefaultItemCategory + " = '" + clsDatabaseFunctions.FormatSQLStatement(getM_sDefaultItemCategory()) + "'"
			+ ", " + SMTableorderheaders.sDefaultPriceListCode + " = '" + clsDatabaseFunctions.FormatSQLStatement(getM_sDefaultPriceListCode()) + "'"
			+ ", " + SMTableorderheaders.selectricianrate + " = '" + clsDatabaseFunctions.FormatSQLStatement(getM_selectricianrate()) + "'"
			+ ", " + SMTableorderheaders.sEmailAddress + " = '" + clsDatabaseFunctions.FormatSQLStatement(getM_sEmailAddress()) + "'"
			+ ", " + SMTableorderheaders.sgeocode + " = '" + clsDatabaseFunctions.FormatSQLStatement(getM_sgeocode()) + "'"
			+ ", " + SMTableorderheaders.slaborerrate + " = '" + clsDatabaseFunctions.FormatSQLStatement(getM_slaborerrate()) + "'"
			+ ", " + SMTableorderheaders.sLastInvoiceNumber + " = '" + clsDatabaseFunctions.FormatSQLStatement(getM_sLastInvoiceNumber()) + "'"
			+ ", " + SMTableorderheaders.sLocation + " = '" + clsDatabaseFunctions.FormatSQLStatement(getM_sLocation()) + "'"
			//We don't update this field:
			//+ ", " + SMTableorderheaders.sOrderCreatedBy  + " = '" + SMUtilities.FormatSQLStatement(getM_sOrderCreatedBy()) + "'"
			//We don't update this field:
			//+ ", " + SMTableorderheaders.sOrderNumber + " = '" + SMUtilities.FormatSQLStatement(
			//		SMUtilities.PadLeft(getM_sOrderNumber().trim(), " ", 
			//			SMTableorderheaders.sOrderNumberPaddedLength)) + "'"
			+ ", " + SMTableorderheaders.sOrderSourceDesc + " = '" + clsDatabaseFunctions.FormatSQLStatement(getM_sOrderSourceDesc()) + "'"
			+ ", " + SMTableorderheaders.sPONumber + " = '" + clsDatabaseFunctions.FormatSQLStatement(getM_sPONumber()) + "'"
			+ ", " + SMTableorderheaders.sPrePostingInvoiceDiscountDesc + " = '" + clsDatabaseFunctions.FormatSQLStatement(getM_sPrePostingInvoiceDiscountDesc()) + "'"
			+ ", " + SMTableorderheaders.sSalesperson + " = '" + clsDatabaseFunctions.FormatSQLStatement(getM_sSalesperson()) + "'"
			+ ", " + SMTableorderheaders.ssecondarybilltophone + " = '" + clsDatabaseFunctions.FormatSQLStatement(getM_ssecondarybilltophone()) + "'"
			+ ", " + SMTableorderheaders.ssecondaryshiptophone + " = '" + clsDatabaseFunctions.FormatSQLStatement(getM_ssecondaryshiptophone()) + "'"
			+ ", " + SMTableorderheaders.sServiceTypeCode + " = '" + clsDatabaseFunctions.FormatSQLStatement(getM_sServiceTypeCode()) + "'"
			+ ", " + SMTableorderheaders.sServiceTypeCodeDescription + " = '" + clsDatabaseFunctions.FormatSQLStatement(getM_sServiceTypeCodeDescription()) + "'"
			+ ", " + SMTableorderheaders.sShipToAddress1 + " = '" + clsDatabaseFunctions.FormatSQLStatement(getM_sShipToAddress1()) + "'"
			+ ", " + SMTableorderheaders.sShipToAddress2 + " = '" + clsDatabaseFunctions.FormatSQLStatement(getM_sShipToAddress2()) + "'"
			+ ", " + SMTableorderheaders.sShipToAddress3 + " = '" + clsDatabaseFunctions.FormatSQLStatement(getM_sShipToAddress3()) + "'"
			+ ", " + SMTableorderheaders.sShipToAddress4 + " = '" + clsDatabaseFunctions.FormatSQLStatement(getM_sShipToAddress4()) + "'"
			+ ", " + SMTableorderheaders.sShipToCity + " = '" + clsDatabaseFunctions.FormatSQLStatement(getM_sShipToCity()) + "'"
			+ ", " + SMTableorderheaders.sShipToCode + " = '" + clsDatabaseFunctions.FormatSQLStatement(getM_sShipToCode()) + "'"
			+ ", " + SMTableorderheaders.sShipToContact + " = '" + clsDatabaseFunctions.FormatSQLStatement(getM_sShiptoContact()) + "'"
			+ ", " + SMTableorderheaders.sShipToCountry + " = '" + clsDatabaseFunctions.FormatSQLStatement(getM_sShipToCountry()) + "'"
			+ ", " + SMTableorderheaders.sShipToFax + " = '" + clsDatabaseFunctions.FormatSQLStatement(getM_sShipToFax()) + "'"
			+ ", " + SMTableorderheaders.sShipToName + " = '" + clsDatabaseFunctions.FormatSQLStatement(getM_sShipToName()) + "'"
			+ ", " + SMTableorderheaders.sShipToPhone + " = '" + clsDatabaseFunctions.FormatSQLStatement(getM_sShiptoPhone()) + "'"
			+ ", " + SMTableorderheaders.sShipToState + " = '" + clsDatabaseFunctions.FormatSQLStatement(getM_sShipToState()) + "'"
			+ ", " + SMTableorderheaders.sShipToZip + " = '" + clsDatabaseFunctions.FormatSQLStatement(getM_sShipToZip()) + "'"
			+ ", " + SMTableorderheaders.sSpecialWageRate + " = '" + clsDatabaseFunctions.FormatSQLStatement(getM_sSpecialWageRate()) + "'"
			+ ", " + SMTableorderheaders.staxjurisdiction + " = '" + clsDatabaseFunctions.FormatSQLStatement(getstaxjurisdiction()) + "'"
			+ ", " + SMTableorderheaders.sTerms + " = '" + clsDatabaseFunctions.FormatSQLStatement(getM_sTerms()) + "'"
			//We don't update this field:
			//+ ", " + SMTableorderheaders.strimmedordernumber + " = '" + SMUtilities.FormatSQLStatement(getM_strimmedordernumber()) + "'"
			+ ", " + SMTableorderheaders.swagescalenotes + " = '" + clsDatabaseFunctions.FormatSQLStatement(getM_swagescalenotes()) + "'"
			+ ", " + SMTableorderheaders.sshiptoemail + " = '" + clsDatabaseFunctions.FormatSQLStatement(getM_sShipToEmail()) + "'"
			+ ", " + SMTableorderheaders.sgdoclink + " = '" + clsDatabaseFunctions.FormatSQLStatement(getM_sGDocLink()) + "'"
			+ ", " + SMTableorderheaders.squotedescription + " = '" + clsDatabaseFunctions.FormatSQLStatement(getsQuoteDescription()) + "'"
			+ ", " + SMTableorderheaders.lbidid + " = " + m_sbidid
			+ ", " + SMTableorderheaders.itaxid + " = " + m_itaxid
			+ ", " + SMTableorderheaders.staxtype + " = '" + clsDatabaseFunctions.FormatSQLStatement(getstaxtype()) + "'"
			+ ", " + SMTableorderheaders.sinvoicingcontact + " = '" + clsDatabaseFunctions.FormatSQLStatement(getM_sInvoicingContact()) + "'"
			+ ", " + SMTableorderheaders.sinvoicingemail + " = '" + clsDatabaseFunctions.FormatSQLStatement(getM_sInvoicingEmail()) + "'"
			+ ", " + SMTableorderheaders.sinvoicingnotes + " = '" + clsDatabaseFunctions.FormatSQLStatement(getM_sInvoicingNotes()) + "'"
			+ ", " + SMTableorderheaders.idoingbusinessasaddressid + " = " + m_idoingbusinessasaddressid
			+ " WHERE ("
				+ "(" + SMTableorderheaders.strimmedordernumber + " = '" + getM_strimmedordernumber() + "')"
			+ ")"
			;
		if (bDebugMode){
			System.out.println("[1332266325]:" + clsDateAndTimeConversions.now("yyyyMMdd HH:mm:ss:SSS") + " - " +  SQL);
		}
		try {
			Statement stmt = conn.createStatement();
			stmt.execute(SQL);
		} catch (Exception e) {
			throw new SQLException("Error updating order header with SQL: " + SQL + " - " + e.getMessage() + ".");
		}
	}
	public boolean saveFieldInfo_without_data_transaction (
			ServletContext context, 
			String sDBIB, 
			String sUser, 
			String sUserID,
			String sUserFullName,
			String sCompany){

		Connection conn = clsDatabaseFunctions.getConnection(
				context, 
				sDBIB, 
				"MySQL", 
				this.toString() + ".saveFieldInfo_without_data_transaction - user: " 
				+ sUserID
				+ " - "
				+ sUserFullName
		);

		if (conn == null){
			super.addErrorMessage("Error opening data connection.");
			return false;
		}

		boolean bResult = saveFieldInfo_without_data_transaction (conn, sUser, sCompany);
		clsDatabaseFunctions.freeConnection(context, conn, "[1547067744]");
		return bResult;	

	}
	public boolean saveFieldInfo_without_data_transaction (Connection conn, String sUser, String sCompany){

		if (!validate_fieldinfo_fields(conn)){
			return false;
		}

		String SQL = "";

		SQL = "UPDATE " + SMTableorderheaders.TableName
		+ " SET " + SMTableorderheaders.mDirections + " = '" 
		+ clsDatabaseFunctions.FormatSQLStatement(m_sDirections.trim()) + "'"
		+ ", " + SMTableorderheaders.mTicketComments + " = '"
		+ clsDatabaseFunctions.FormatSQLStatement(m_sTicketComments.trim()) + "'"
		+ ", " + SMTableorderheaders.mFieldNotes + " = '"
		+ clsDatabaseFunctions.FormatSQLStatement(m_sFieldNotes.trim()) + "'"
		+ " WHERE ("
		+ SMTableorderheaders.sOrderNumber + " = '" + clsStringFunctions.PadLeft(m_sOrderNumber.trim(), " ", 8) + "'"
		+ ")"
		;
		try{
			Statement stmt = conn.createStatement();
			stmt.execute(SQL);
		}catch(SQLException ex){
			super.addErrorMessage("Error updating field info with SQL: " + SQL + " - " + ex.getMessage());
			return false;
		}

		return true;
	}

	public boolean validate_order_fields (Connection conn){
		//Validate the entries here:
		String SQL = "";
		boolean bEntriesAreValid = true;
		boolean bIsQuote = getM_iOrderType().compareToIgnoreCase(Integer.toString(SMTableorderheaders.ORDERTYPE_QUOTE)) == 0;
		//Get the currently saved order, and use it to validate changes:
		SMOrderHeader currentorder = null;
		
		//ID
		if (m_siID.compareToIgnoreCase("-1") != 0){
			if (!isLongValid("Order ID", m_siID, true)){
				bEntriesAreValid = false;
			}else{
				currentorder = new SMOrderHeader();
				currentorder.setM_strimmedordernumber(getM_strimmedordernumber());
				if (!currentorder.load(conn)){
					super.addErrorMessage("Could not load existing order " + getM_strimmedordernumber()
						+ " to check current order values - " + currentorder.getErrorMessages()
					);
					bEntriesAreValid = false;
				}
			}
		}
		//Order number
		if (!isStringValid("Order number", m_sOrderNumber, SMTableorderheaders.sOrderNumberLength,false)){bEntriesAreValid = false;}
		if (!isDecimalValid("Truck days", m_bdtruckdays, SMTableorderheaders.bdtruckdaysScale)){bEntriesAreValid = false;};
		if (!isStringValid("Carpenter rate", m_scarpenterrate, SMTableorderheaders.scarpenterrateLength,true)){bEntriesAreValid = false;}
		if (!isStringValid("Laborer rate", m_slaborerrate, SMTableorderheaders.slaborerrateLength,true)){bEntriesAreValid = false;}
		if (!isStringValid("Electrician rate", m_slaborerrate, SMTableorderheaders.selectricianrateLength,true)){bEntriesAreValid = false;}
		if (!isDecimalValid("Total markup", m_bdtotalmarkup, SMTableorderheaders.bdtotalmarkupScale)){bEntriesAreValid = false;};
		if (!isDecimalValid("Deposit amount", m_bddepositamount, SMTableorderheaders.bddepositamountScale)){bEntriesAreValid = false;};
		if (!isDecimalValid("Total contract amount", m_bdtotalcontractamount, SMTableorderheaders.bdtotalcontractamountScale)){bEntriesAreValid = false;};
		if (!isDateValid("Warranty expiration date", m_datwarrantyexpiration, true)){bEntriesAreValid = false;}
		if (!isStringValid("Wage scale notes", m_swagescalenotes, SMTableorderheaders.swagescalenotesLength,true)){bEntriesAreValid = false;}
		if (!isStringValid("Secondary bill-to phone", m_ssecondarybilltophone, SMTableorderheaders.ssecondarybilltophoneLength,true)){bEntriesAreValid = false;}
		if (!isStringValid("Secondary ship-to phone", m_ssecondaryshiptophone, SMTableorderheaders.ssecondaryshiptophoneLength,true)){bEntriesAreValid = false;}
		if (!isStringValid("Ship-to phone", m_sshiptophone, SMTableorderheaders.sShipToPhoneLength,true)){bEntriesAreValid = false;}
		if (!isStringValid("Bill-to phone", m_sbilltophone, SMTableorderheaders.sBillToPhoneLength,true)){bEntriesAreValid = false;}
		if (!isStringValid("Bill-to contact", m_sbilltocontact, SMTableorderheaders.sBillToContactLength,true)){bEntriesAreValid = false;}
		if (!isStringValid("Ship-to contact", m_sshiptocontact, SMTableorderheaders.sShipToContactLength,true)){bEntriesAreValid = false;}

		// m_sBillToName
		if (!isStringValid("Bill To Name", m_sBillToName, SMTableorderheaders.sBillToNameLength,false)){bEntriesAreValid = false;}
		// m_sShipToName
		if (!isStringValid("Ship To Name", m_sShipToName, SMTableorderheaders.sShipToNameLength,false)){bEntriesAreValid = false;}
		// m_sDirections
		// m_sTicketComments
		// m_sFieldNotes
		// m_sServiceTypeCode
		
			//If it's a valid service type code, read the service type description:
			SQL = "SELECT"
				+ " " + SMTableservicetypes.sName + " FROM " + SMTableservicetypes.TableName
				+ " WHERE ("
					+ "(" + SMTableservicetypes.sCode + " = '" + m_sServiceTypeCode + "')"
				+ ")"
			;
			if (bDebugMode){
				System.out.println("[1332265716]:" + clsDateAndTimeConversions.now("yyyyMMdd HH:mm:ss:SSS") + SQL);
			}
			try {
				ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
				if (rs.next()){
					m_sServiceTypeCodeDescription = rs.getString(SMTableservicetypes.sName);
				}else{
					super.addErrorMessage("Service type '" + m_sServiceTypeCode + "' does not exist.");
					bEntriesAreValid = false;
				}
				rs.close();
			} catch (SQLException e) {
				super.addErrorMessage("Error reading service type data with SQL: " + SQL + " - " + e.getMessage());
				bEntriesAreValid = false;
			}

			// m_sServiceTypeCodeDescription
			if (!isStringValid("Service code description", m_sServiceTypeCodeDescription,
				SMTableorderheaders.sServiceTypeCodeDescriptionLength,false)){bEntriesAreValid = false; }
			
		// m_strimmedordernumber
		if (m_siID.compareToIgnoreCase("-1") != 0){
			if (!isLongValid("Trimmed order number", m_strimmedordernumber, true)){bEntriesAreValid = false; }
		}
		// m_sCustomerCode
		//On quotes, we do NOT require a customer code:
		if (!bIsQuote){
			if (!isStringValid("Customer code", m_sCustomerCode, SMTableorderheaders.sCustomerCodeLength,false)){bEntriesAreValid = false; }
		}
		if (m_sCustomerCode.compareToIgnoreCase("") != 0){
			//Now validate that the customer exists and is active:
			SQL = "SELECT"
				+ " " + SMTablearcustomer.iActive
				+ ", " + SMTablearcustomer.iOnHold 
				+ " FROM " + SMTablearcustomer.TableName
				+ " WHERE ("
					+ "(" + SMTablearcustomer.sCustomerNumber + " = '" + m_sCustomerCode + "')"
				+ ")"
			;
			if (bDebugMode){
				System.out.println("[1332265658]:" + clsDateAndTimeConversions.now("yyyyMMdd HH:mm:ss:SSS") + SQL);
			}
			try {
				ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
				if (rs.next()){
					if (rs.getLong(SMTablearcustomer.iActive) == 0){
						super.addErrorMessage("Customer '" + m_sCustomerCode + "' is no longer active.");
						bEntriesAreValid = false; 
					}
				}else{
					super.addErrorMessage("Customer '" + m_sCustomerCode + "' does not exist.");
					bEntriesAreValid = false;
				}
				rs.close();
			} catch (Exception e) {
				super.addErrorMessage("Error reading customer data - " + e.getMessage());
				bEntriesAreValid = false;
			}
		}
		// m_sBillToAddressLine1
		if (!isStringValid("Bill to address line 1", m_sBillToAddressLine1, SMTableorderheaders.sBillToAddressLine1Length,true)){bEntriesAreValid = false;}		
		// m_sBillToAddressLine2
		if (!isStringValid("Bill to address line 2", m_sBillToAddressLine2, SMTableorderheaders.sBillToAddressLine2Length,true)){bEntriesAreValid = false;}
		// m_sBillToAddressLine3
		if (!isStringValid("Bill to address line 3", m_sBillToAddressLine3, SMTableorderheaders.sBillToAddressLine3Length,true)){bEntriesAreValid = false;}
		// m_sBillToAddressLine4
		if (!isStringValid("Bill to address line 4", m_sBillToAddressLine4, SMTableorderheaders.sBillToAddressLine4Length,true)){bEntriesAreValid = false;}
		// m_sBillToCity
		if (!isStringValid("Bill to city", m_sBillToCity, SMTableorderheaders.sBillToCityLength,false)){bEntriesAreValid = false;}
		// m_sBillToState
		if (!isStringValid("Bill to state", m_sBillToState, SMTableorderheaders.sBillToStateLength,false)){bEntriesAreValid = false;}
		// m_sBillToZip
		if (!isStringValid("Bill to zip", m_sBillToZip, SMTableorderheaders.sBillToZipLength,false)){bEntriesAreValid = false;}
		// m_sBillToCountry
		if (!isStringValid("Bill to country", m_sBillToCountry, SMTableorderheaders.sBillToCountryLength,true)){bEntriesAreValid = false;}
		// m_sBillToFax
		if (!isStringValid("Bill to fax", m_sBillToFax, SMTableorderheaders.sBillToFaxLength,true)){bEntriesAreValid = false;}
		// m_sShipToCode
		if (!isStringValid("Ship to code", m_sShipToCode, SMTableorderheaders.sShipToCodeLength,true)){bEntriesAreValid = false;}
		// m_sShipToAddress1
		if (!isStringValid("Ship to address line 1", m_sShipToAddress1, SMTableorderheaders.sShipToAddress1Length,true)){bEntriesAreValid = false;}
		// m_sShipToAddress2
		if (!isStringValid("Ship to address line 2", m_sShipToAddress2, SMTableorderheaders.sShipToAddress2Length,true)){bEntriesAreValid = false;}
		// m_sShipToAddress3
		if (!isStringValid("Ship to address line 3", m_sShipToAddress3, SMTableorderheaders.sShipToAddress3Length,true)){bEntriesAreValid = false;}
		// m_sShipToAddress4
		if (!isStringValid("Ship to address line 4", m_sShipToAddress4, SMTableorderheaders.sShipToAddress4Length,true)){bEntriesAreValid = false;}
		// m_sShipToCity
		if (!isStringValid("Ship to city", m_sShipToCity, SMTableorderheaders.sShipToCityLength,true)){bEntriesAreValid = false;}
		// m_sShipToState
		if (!isStringValid("Ship to state", m_sShipToState, SMTableorderheaders.sShipToStateLength,true)){bEntriesAreValid = false;}
		// m_sShipToZip
		if (bIsQuote){
			if (!isStringValid("Ship to zip", m_sShipToZip, SMTableorderheaders.sShipToZipLength,true)){bEntriesAreValid = false;}
		}else{
			if (!isStringValid("Ship to zip", m_sShipToZip, SMTableorderheaders.sShipToZipLength,false)){bEntriesAreValid = false;}	
		}
		// m_sShipToCountry
		if (!isStringValid("Ship to country", m_sShipToCountry, SMTableorderheaders.sShipToCountryLength,true)){bEntriesAreValid = false;}
		// m_sShipToFax
		if (!isStringValid("Ship to fax", m_sShipToFax, SMTableorderheaders.sShipToFaxLength,true)){bEntriesAreValid = false;}
		
		// m_iCustomerDiscountLevel
		if (m_iCustomerDiscountLevel.trim().compareToIgnoreCase("") == 0){
			super.addErrorMessage("Customer price level was not selected.");
			bEntriesAreValid = false;
		}else{
			if (!isLongValid("Customer price level", m_iCustomerDiscountLevel, true)){
				bEntriesAreValid = false;
			}else{
				long lTest = Long.parseLong(m_iCustomerDiscountLevel);
				if ((lTest < SMTableorderheaders.CUSTOMERTYPE_BASE) || (lTest > SMTableorderheaders.CUSTOMERTYPE_E)){
					super.addErrorMessage("Customer price level '" + m_iCustomerDiscountLevel + "' is invalid.");
					bEntriesAreValid = false;
				}
			}
		}
		// m_sDefaultPriceListCode
		if (!isStringValid("Default price list code", m_sDefaultPriceListCode, SMTableorderheaders.sDefaultPriceListCodeLength,false)){bEntriesAreValid = false;}
		// m_sPONumber
		if (!isStringValid("PO Number", m_sPONumber, SMTableorderheaders.sPONumberLength,true)){bEntriesAreValid = false;}
		// m_sSpecialWageRate
		if (!isStringValid("Special wage rate", m_sSpecialWageRate, SMTableorderheaders.sSpecialWageRateLength,false)){bEntriesAreValid = false;}
		// m_sTerms
		m_sTerms = m_sTerms.trim();
		if (!isStringValid("Terms", m_sTerms, SMTableorderheaders.sTermsLength,false)){bEntriesAreValid = false;}
		// m_iOrderType
		if (!isLongValid("Order type", m_iOrderType, true)){
			bEntriesAreValid = false;
		}else{
			long lTest = Long.parseLong(m_iOrderType);
			if ((lTest < SMTableorderheaders.ORDERTYPE_ACTIVE) || (lTest > SMTableorderheaders.ORDERTYPE_QUOTE)){
				super.addErrorMessage("Order type '" + m_iOrderType + "' is invalid.");
				bEntriesAreValid = false;
			}
		}
		//IF this is an existing order, don't let anyone change it from Active/Standing into a Quote:
		if (
			//If it's a current order
			(currentorder != null)
			//AND if it's NOT CURRENTLY a quote
			&& (currentorder.getM_iOrderType().compareToIgnoreCase(
				Integer.toString(SMTableorderheaders.ORDERTYPE_QUOTE)) != 0)
			//But someone is trying to SAVE it as a quote:
			&& (getM_iOrderType().compareToIgnoreCase(
				Integer.toString(SMTableorderheaders.ORDERTYPE_QUOTE)) == 0)
		){
			//Then don't let it be saved as a quote:
			super.addErrorMessage("You cannot change an existing order into a quote.");
			bEntriesAreValid = false;
		}
		//If it's a current order, and it WAS a quote, but it's now being changed to an order, set all the
		//line booked dates to the order date:
		if (
			//If it's a current order
			(currentorder != null)
			//AND if it's CURRENTLY a quote
			&& (currentorder.getM_iOrderType().compareToIgnoreCase(
				Integer.toString(SMTableorderheaders.ORDERTYPE_QUOTE)) == 0)
			//But someone is trying to SAVE it as an order:
			&& (getM_iOrderType().compareToIgnoreCase(
				Integer.toString(SMTableorderheaders.ORDERTYPE_QUOTE)) != 0)
		){
			//Then set all the line booked dates to the order date and set all the shipped qtys to zero
			for (int i = 0; i < m_arrDetails.size(); i++){
				m_arrDetails.get(i).setM_datLineBookedDate(getM_datOrderDate());
				m_arrDetails.get(i).setM_dQtyShipped("0.0000");
			}		
		}
		
		// m_datOrderDate
		if(!isDateValid("Order date", m_datOrderDate, false)){bEntriesAreValid = false;};
		// m_datExpectedShipDate
		if(!isDateValid("Expected ship date", m_datExpectedShipDate, true)){bEntriesAreValid = false;};
		//We can' check this one because it includes a time, too:
		// m_datOrderCreationDate
		//These are no longer used:
		// m_sOrderFiscalYear
		// m_iOrderFiscalPeriod
		// m_sLastInvoiceNumber
		if (!isStringValid("Last invoice number", m_sLastInvoiceNumber, SMTableorderheaders.sLastInvoiceNumberLength,true)){bEntriesAreValid = false;}
		// m_iNumberOfInvoices
		if (!isLongValid("Number of invoices", m_iNumberOfInvoices, true)){bEntriesAreValid = false;}
		// m_sLocation
		if (!isStringValid("Location", m_sLocation, SMTableorderheaders.sLocationLength,true)){bEntriesAreValid = false;}
		//Now validate that the location exists:
		SQL = "SELECT"
			+ " " + SMTablelocations.sLocation + " FROM " + SMTablelocations.TableName
			+ " WHERE ("
				+ "(" + SMTablelocations.sLocation + " = '" + m_sLocation + "')"
			+ ")"
		;
		if (bDebugMode){
			System.out.println("[1332265717]:"  + clsDateAndTimeConversions.now("yyyyMMdd HH:mm:ss:SSS") + SQL);
		}
		try {
			ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
			if (!rs.next()){
				super.addErrorMessage("Location '" + m_sLocation + "' does not exist.");
				bEntriesAreValid = false;
			}
			rs.close();
		} catch (SQLException e) {
			super.addErrorMessage("Error reading location data - " + e.getMessage());
			bEntriesAreValid = false;
		}
		// m_idoingbusinessasaddressid
		if (!isStringValid("Doing business as address ID ", m_idoingbusinessasaddressid, Integer.MAX_VALUE , false)){bEntriesAreValid = false;}
		//Now validate that the DBA ID exists:
		SQL = "SELECT"
			+ " " + SMTabledoingbusinessasaddresses.lid + " FROM " + SMTabledoingbusinessasaddresses.TableName
			+ " WHERE ("
				+ "(" + SMTabledoingbusinessasaddresses.lid + " = " + m_idoingbusinessasaddressid + ")"
			+ ")"
		;
		if (bDebugMode){
			System.out.println("[13322657412]:"  + clsDateAndTimeConversions.now("yyyyMMdd HH:mm:ss:SSS") + SQL);
		}
		try {
			ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
			if (!rs.next()){
				super.addErrorMessage(" Doing business as address ID '" + m_idoingbusinessasaddressid + "' cannot be blank or has been deleted. Please save a valid DBA on this order header.  ");
				bEntriesAreValid = false;
			}
			rs.close();
		} catch (SQLException e) {
			super.addErrorMessage("doing business as address id does not exist.");
			bEntriesAreValid = false;
		}
		// m_iOnHold
		if (!isBooleanIntValid("On hold", m_iOnHold)){bEntriesAreValid = false;}
		// m_datLastPostingDate
		if(!isDateValid("Last posting date", m_datLastPostingDate, true)){bEntriesAreValid = false;}
		// m_dTotalAmountItems
		if (!isDoubleValid("Total item amount", m_dTotalAmountItems, 2)){bEntriesAreValid = false;}
		// m_iNumberOfLinesOnOrder
		if (!isLongValid("Number of lines", m_iNumberOfLinesOnOrder, true)){bEntriesAreValid = false;}
		// m_sSalesperson
		if (m_sSalesperson.compareToIgnoreCase(SMEditOrderEdit.NO_SALESPERSON_SELECTED_VALUE) == 0){
			super.addErrorMessage("You must choose from the salesperson list.");
			bEntriesAreValid = false;
		}else{
			if (!isStringValid("Salesperson", m_sSalesperson, SMTableorderheaders.sSalespersonLength,true)){bEntriesAreValid = false;}
		}
		// m_sOrderCreatedBy
		if (!isStringValid("Order created by", m_sOrderCreatedByFullName, SMTableorderheaders.sOrderCreatedByLength,true)){bEntriesAreValid = false;}
		
		//Process tax:
		boolean bTaxIsValid = false;
       	try {
			m_itaxid = clsValidateFormFields.validateLongIntegerField(m_itaxid, "Tax ID", -1L, clsValidateFormFields.MAX_LONG_VALUE);
			bTaxIsValid = true;
		} catch (Exception e1) {
			super.addErrorMessage(e1.getMessage());
			bEntriesAreValid = false;
		}
       	
       	//If the tax ID is a valid value, now check to make sure it's a valid tax:
       	if (bTaxIsValid){
			SQL = "SELECT * FROM " + SMTabletax.TableName
				+ " WHERE ("
					+ "(" + SMTabletax.lid + " = " + getitaxid() + ")"
				+ ")"
			;
			try {
				ResultSet rsTax = clsDatabaseFunctions.openResultSet(SQL, conn);
				if (rsTax.next()){
					setstaxjurisdiction(rsTax.getString(SMTabletax.staxjurisdiction));
					setstaxtype(rsTax.getString(SMTabletax.staxtype));
				}else{
					//The tax must have been deleted - the tax type, and jurisdiction and tax ID will just stay as they are
				}
				rsTax.close();
			} catch (SQLException e1) {
				super.addErrorMessage("Error [1454433061] reading taxes to validate order - " + e1.getMessage());
				bEntriesAreValid = false;
			}
       	}
		// m_sDefaultItemCategory
		if (!isStringValid("Default item category", m_sDefaultItemCategory, SMTableorderheaders.sDefaultItemCategoryLength,true)){bEntriesAreValid = false;}
		if (m_sDefaultItemCategory.compareToIgnoreCase("") != 0){
			//Now validate that the item category exists:
			SQL = "SELECT"
				+ " " + SMTableiccategories.iActive
				+ ", " + SMTableiccategories.datInactive + " FROM " + SMTableiccategories.TableName
				+ " WHERE ("
					+ "(" + SMTableiccategories.sCategoryCode + " = '" + m_sDefaultItemCategory + "')"
				+ ")"
			;
			if (bDebugMode){
				System.out.println("[1332265785]:" + clsDateAndTimeConversions.now("yyyyMMdd HH:mm:ss:SSS") + SQL);
			}
			try {
				ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
				if (!rs.next()){
					super.addErrorMessage("Category '" + m_sDefaultItemCategory + "' does not exist.");
					bEntriesAreValid = false;
				}else{
					if (rs.getLong(SMTableiccategories.iActive) == 0){
						super.addErrorMessage("Category '" + m_sDefaultItemCategory + "' is inactive.");
						bEntriesAreValid = false;
					}
				}
				rs.close();
			} catch (SQLException e) {
				super.addErrorMessage("Error reading category data with SQL: " + SQL + " - " + e.getMessage());
				bEntriesAreValid = false;
			}
		}

		// m_iRequisitionDueDay
		//if (!isLongValid("Requisition due day", m_iRequisitionDueDay, true)){bEntriesAreValid = false;}
		// m_dTaxBase
		if (!isDecimalValid("Tax base", m_dTaxBase, SMTableorderheaders.bdTaxBaseScale)){bEntriesAreValid = false;}
		// m_dOrderTaxAmount
		if (!isDecimalValid("Order tax amount", m_sordersalestaxamount, SMTableorderheaders.bdOrderTaxAmountScale)){bEntriesAreValid = false;}
		// m_iNextDetailNumber
		if (!isLongValid("Next detail number", m_iNextDetailNumber, true)){bEntriesAreValid = false;}
		if (!isPositiveDoubleValid("Pre-posting invoice discount percentage", m_dPrePostingInvoiceDiscountPercentage, 2)){bEntriesAreValid = false;}
		// m_dPrePostingInvoiceDiscountAmount
		if (!isDoubleValid("Pre-posting invoice discount percentage", m_dPrePostingInvoiceDiscountPercentage, 2)){bEntriesAreValid = false;}
		// m_LASTEDITUSER
		if (!isStringValid("Last edit user", m_LASTEDITUSERFULLNAME, SMTableorderheaders.LASTEDITUSERLength,true)){bEntriesAreValid = false;}
		// m_LASTEDITPROCESS
		if (!isStringValid("Last edit process", m_LASTEDITPROCESS, SMTableorderheaders.LASTEDITPROCESSLength,true)){bEntriesAreValid = false;}
		//We don't validate these because on new orders they get set at time of saving:
		// m_LASTEDITDATE
		// m_LASTEDITTIME
		// m_sCustomerControlAcctSet
		//No control acct set required on quotes without customers:
		if (!bIsQuote){
			if (!isStringValid("Customer control acct set", m_sCustomerControlAcctSet, 
				SMTableorderheaders.sCustomerControlAcctSetLength,false)){bEntriesAreValid = false;}
		}
		// m_sPrePostingInvoiceDiscountDesc
		if (!isStringValid("Pre-posting invoice discount description", m_sPrePostingInvoiceDiscountDesc, SMTableorderheaders.sPrePostingInvoiceDiscountDescLength,true)){bEntriesAreValid = false;}
		// m_datOrderCanceledDate
		if(!isDateValid("Canceled date", m_datOrderCanceledDate, true)){bEntriesAreValid = false;}
		// m_iOrderSourceID
		if (!isLongValid("Order source ID", m_iOrderSourceID, true)){
			bEntriesAreValid = false;
		}else{
			//If it's a valid order source code, read the order source description:
			SQL = "SELECT"
				+ " " + SMTableordersources.sSourceDesc + " FROM " + SMTableordersources.TableName
				+ " WHERE ("
					+ "(" + SMTableordersources.iSourceID + " = " + m_iOrderSourceID + ")"
				+ ")"
			;
			if (bDebugMode){
				System.out.println("[1332265786]:" + clsDateAndTimeConversions.now("yyyyMMdd HH:mm:ss:SSS") + SQL);
			}
			try {
				ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
				if (rs.next()){
					m_sOrderSourceDesc = rs.getString(SMTableordersources.sSourceDesc);
				}else{
					super.addErrorMessage("Order source '" + m_iOrderSourceID + "' does not exist on order '" + getM_strimmedordernumber() + "'.");
					bEntriesAreValid = false;
				}
				rs.close();
			} catch (SQLException e) {
				super.addErrorMessage("Error reading order sources with SQL: " + SQL + " - " + e.getMessage());
				bEntriesAreValid = false;
			}

			// m_sOrderSourceDesc
			if (!isStringValid("Order source description", m_sOrderSourceDesc, SMTableorderheaders.sOrderSourceDescLength,true)){bEntriesAreValid = false;}		
		}
		
		// m_dEstimatedHour
		if (!isPositiveDoubleValid("Estimated hours", m_dEstimatedHour, 2)){bEntriesAreValid = false;}
		// m_datCompletedDate
		//if(!isDateValid("Completed date", m_datCompletedDate, true)){bEntriesAreValid = false;}
		// m_sEmailAddress 
		if (!isStringValid("Email address", m_sEmailAddress, SMTableorderheaders.sEmailAddressLength,true)){bEntriesAreValid = false;}
		// m_iSalesGroup
		if (!isLongValid("Sales group ID", m_iSalesGroup, true)){bEntriesAreValid = false;}
		// m_sClonedFrom
		if (!isStringValid("Cloned from", m_sClonedFrom, SMTableorderheaders.sclonedfromlength,true)){bEntriesAreValid = false;}
		// m_sgeocode
		if (!isStringValid("Geocode", m_sgeocode, SMTableorderheaders.sgeocodeLength,true)){bEntriesAreValid = false;}
		//m_sshiptoemail
		if (!isStringValid("Ship to email", m_sshiptoemail, SMTableorderheaders.sEmailAddressLength,true)){bEntriesAreValid = false;}
		if (!isLongValid(SMBidEntry.ParamObjectName + " ID", m_sbidid, true)){bEntriesAreValid = false;}
		if (!isStringValid("Quote description", m_squotedescription, SMTableorderheaders.squotedescriptionLength,true)){bEntriesAreValid = false;}
		if (!isStringValid("Invoicing contact", m_sinvoicingcontact, SMTableorderheaders.sInvoicingContactLength,true)){bEntriesAreValid = false;}
		if (!isStringValid("Invoicing email address", m_sinvoicingemailaddress, SMTableorderheaders.sInvoicingEmailLength,true)){bEntriesAreValid = false;}
		
		//If it's not a quote, AND the system is set to 'Enforce credit limits', then check to see if this customer is over their limit:
		try {
			enforceCreditLimit(conn, getM_sCustomerCode(), currentorder);
		} catch (Exception e) {
			super.addErrorMessage(e.getMessage());
			bEntriesAreValid = false;
		}
		
		return bEntriesAreValid;
	}
	private void enforceCreditLimit(Connection conn, String sCustomer, SMOrderHeader currentorder) throws Exception {

		boolean bCheckCreditLimit = false;

		//If it's not a quote, 
		if (getM_iOrderType().compareToIgnoreCase(Integer.toString(SMTableorderheaders.ORDERTYPE_QUOTE)) != 0){
			//Then if it's a new order, it's eligible for credit limit check:
			if (currentorder == null){
				bCheckCreditLimit = true;
			}else{
				//If it's an existing quote, but being turned INTO an order, it's eligible for credit check:
				if (
					(currentorder.getM_iOrderType().compareToIgnoreCase(Integer.toString(SMTableorderheaders.ORDERTYPE_QUOTE)) == 0)
					&& (getM_iOrderType().compareToIgnoreCase(Integer.toString(SMTableorderheaders.ORDERTYPE_QUOTE)) != 0)
				){
					bCheckCreditLimit = true;
				}
			}
		}
		
		//If it's not eligible for credit limit check, then just return:
		if (!bCheckCreditLimit){
			return;
		}
		AROptions aropts = new AROptions();
		if (!aropts.load(conn)){
			throw new Exception("Error loading AR options to check credit limit enforcement - " + aropts.getErrorMessageString());
		}else{
			if (aropts.getEnforceCreditLimit().compareToIgnoreCase("1") == 0){
				//Check the customer's credit limit:
				String SQL = "SELECT"
					+ " " + SMTablearcustomer.dCreditLimit
					+ ", BALANCEQUERY.CURRENTTOTAL"
					+ " FROM " + SMTablearcustomer.TableName
					+ " LEFT JOIN "
					+ "(SELECT "
					+ SMTableartransactions.spayeepayor + " AS CUSTOMER"
					+ ", SUM(" + SMTableartransactions.dcurrentamt + ") AS CURRENTTOTAL"
					+ " FROM " + SMTableartransactions.TableName
					+ " WHERE (" 
						+ "(" + SMTableartransactions.spayeepayor + " = '" + this.getM_sCustomerCode() + "')"
					+ ")"
					+ " GROUP BY " + SMTableartransactions.spayeepayor
					+ ") AS BALANCEQUERY"
					+ " ON BALANCEQUERY.CUSTOMER=" + SMTablearcustomer.TableName + "." + SMTablearcustomer.sCustomerNumber
					+ " WHERE ("
						+ "(" + SMTablearcustomer.TableName + "." + SMTablearcustomer.sCustomerNumber + " = '" + getM_sCustomerCode() + "')" 
					+ ")"
				;
				try {
					ResultSet rsCustomerCredit = clsDatabaseFunctions.openResultSet(SQL, conn);
					if (rsCustomerCredit.next()){
						BigDecimal bdCreditLimit = BigDecimal.valueOf(rsCustomerCredit.getDouble(SMTablearcustomer.TableName + "." + SMTablearcustomer.dCreditLimit));
						BigDecimal bdCurrentBalance = BigDecimal.valueOf(rsCustomerCredit.getDouble("BALANCEQUERY.CURRENTTOTAL"));
						if (bdCreditLimit.compareTo(bdCurrentBalance) <=0){
							rsCustomerCredit.close();
							throw new Exception("Customer '" + getM_sCustomerCode() + "' currently has " + bdCurrentBalance.toString() 
								+ " in open transactions, but their credit limit is only " + bdCreditLimit.toString() + ".");
						}
					}
					rsCustomerCredit.close();
				} catch (SQLException e) {
					throw new Exception("Error checking customer balance using SQL: " + SQL + " - " + e.getMessage());
				}
			}
		}
		return;
	}
	public boolean validate_for_invoicing(Connection conn){
		
		//First, check that the customer is not on hold or inactive:
		String SQL = "SELECT"
			+ " " + SMTablearcustomer.iOnHold
			+ ", " + SMTablearcustomer.iActive
			+ " FROM " + SMTablearcustomer.TableName
			+ " WHERE ("
				+ "(" + SMTablearcustomer.sCustomerNumber + " = '" + getM_sCustomerCode() + "')"
			+ ")"
			;
		
		try {
			ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
			if (rs.next()){
				if (rs.getLong(SMTablearcustomer.iOnHold) == 1){
					rs.close();
					super.addErrorMessage("Customer '" + getM_sCustomerCode() + " is on hold - could not create invoice.");
					return false;
				}
				if (rs.getLong(SMTablearcustomer.iActive) == 0){
					rs.close();
					super.addErrorMessage("Customer '" + getM_sCustomerCode() + " is inactive - could not create invoice.");
					return false;
				}
			}else{
				rs.close();
				super.addErrorMessage("Could not find customer '" + getM_sCustomerCode() + " - could not create invoice.");
				return false;
			}
		} catch (SQLException e) {
			super.addErrorMessage("Error checking customer to create invoice - " + e.getMessage());
			return false;
		}
		
		//Check that the tax is a valid one:
		SQL = "SELECT"
			+ " " + SMTabletax.lid
			+ " FROM " + SMTabletax.TableName
			+ " WHERE ("
				+ "(" + SMTabletax.lid + " = " + getitaxid() + ")"
				+ " AND (" + SMTabletax.iactive + " = 1)"
			+ ")"
		;
		try {
			ResultSet rsTax = clsDatabaseFunctions.openResultSet(SQL, conn);
			if (!rsTax.next()){
				super.addErrorMessage("Selected tax is not an active or valid tax.");
				rsTax.close();
				return false;
			}
			rsTax.close();
		} catch (SQLException e1) {
			super.addErrorMessage("Error [1454524949] checking for valid tax with SQL: '" + SQL + "' - " + e1.getMessage());
			return false;
		}
		
		SQL = "SELECT"
			+ " " + SMTableorderdetails.TableName + "." + SMTableorderdetails.iDetailNumber
			+ ", " + SMTableorderdetails.TableName + "." + SMTableorderdetails.sOrderUnitOfMeasure
			+ ", " + SMTableorderdetails.TableName + "." + SMTableorderdetails.sItemNumber
			+ ", " + SMTableicitems.TableName + "." + SMTableicitems.iActive
			+ ", " + SMTableicitems.TableName + "." + SMTableicitems.sCostUnitOfMeasure
			+ ", " + SMTableicitems.TableName + "." + SMTableicitems.sItemNumber
			+ " FROM " + SMTableorderdetails.TableName + " LEFT JOIN " + SMTableicitems.TableName + " ON "
			+ SMTableorderdetails.TableName + "." + SMTableorderdetails.sItemNumber + " = " + SMTableicitems.TableName + "." + SMTableicitems.sItemNumber 
			+ " WHERE ("
				+ "(" + SMTableorderdetails.TableName + "." + SMTableorderdetails.dQtyShipped + " > 0.00)"
				+ " AND (" + SMTableorderdetails.TableName + "." + SMTableorderdetails.strimmedordernumber 
				+ " = '" + getM_strimmedordernumber() + "')"
			+ ")"
			;
		try {
			ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
			int iLineCounter = 0;
			while(rs.next()){
				if (rs.getString(SMTableicitems.TableName + "." + SMTableicitems.sItemNumber) == null){
					super.addErrorMessage("Item '" + rs.getString(SMTableorderdetails.TableName + "." + SMTableorderdetails.sItemNumber) 
						+ "' is not an inventory item - cannot create invoice.");
					rs.close();
					return false;
				}
				if (rs.getInt(SMTableicitems.TableName + "." + SMTableicitems.iActive) == 0){
					super.addErrorMessage("Item '" + rs.getString(SMTableorderdetails.TableName + "." + SMTableorderdetails.sItemNumber) 
						+ "' is not active in inventory - cannot create invoice.");
					rs.close();
					return false;
				}
				if (rs.getString(SMTableicitems.TableName + "." 
					+ SMTableicitems.sCostUnitOfMeasure).compareTo(
						rs.getString(SMTableorderdetails.TableName + "." + SMTableorderdetails.sOrderUnitOfMeasure)) != 0){
					super.addErrorMessage("Item '" + rs.getString(SMTableorderdetails.TableName + "." + SMTableorderdetails.sItemNumber) 
						+ "' has a different unit of measure than it does in inventory - cannot create invoice.");
					rs.close();
					return false;
				}
				iLineCounter++;
			}
			rs.close();
			if (iLineCounter == 0){
				super.addErrorMessage("Order #" + getM_strimmedordernumber() + " has nothing shipped on it - cannot create invoice.");
				return false;
			}
		} catch (SQLException e) {
			super.addErrorMessage("Error checking order #" + getM_strimmedordernumber() + " for shipped lines - " + e.getMessage());
			return false;
		}
		
		return true;
	}
	private void recalculateEntireOrder(Connection conn, boolean bIsNewOrder, boolean bRecalculatePrice) throws Exception{
		
		//First, we re-calculate all the order lines:
		//update the lines:
		for (int i = 0; i < m_arrDetails.size(); i++){
			if (bIsNewOrder){
				//If it's a NEW order, set all the 'QtyOriginallyOrdered' values to match
				//the QtyOrdered:
				m_arrDetails.get(i).setM_dOriginalQty(m_arrDetails.get(i).getM_dQtyOrdered());
			}
			m_arrDetails.get(i).setM_dUniqueOrderID(getM_siID());
			m_arrDetails.get(i).setM_strimmedordernumber(getM_strimmedordernumber());
			//Set the line number here, just in case it is different than it was before:
			m_arrDetails.get(i).setM_iLineNumber(Integer.toString(i + 1));
			if (bRecalculatePrice){
				try {
					updateLinePrice(m_arrDetails.get(i), conn);
				} catch (SQLException e) {
					throw new Exception("Error recalculating order in updateLinePrice - " + e.getMessage());			
				}
			}
		}
		
		//Set the header fields now:
		m_iNumberOfLinesOnOrder = Long.toString(m_arrDetails.size());
		
		try {
			m_dTotalAmountItems = clsManageBigDecimals.BigDecimalToScaledFormattedString(
				SMTableorderheaders.bdtotalamountitemsScale, totalAmountOfItems());
		} catch (Exception e2) {
			throw new Exception("Error recalculating order - " + e2.getMessage());
		}
		
		try {
			setM_dTaxBase(clsManageBigDecimals.BigDecimalToScaledFormattedString(
				SMTableorderheaders.bdTaxBaseScale, taxableTotalAfterDiscount(conn)));
		} catch (Exception e1) {
			throw new Exception("Error recalculating order - " + e1.getMessage());
		}

		try {
			setsordersalestaxamount(clsManageBigDecimals.BigDecimalToScaledFormattedString(
				SMTableorderheaders.bdTaxBaseScale, calculateSalesTaxAmount(conn)));
		} catch (Exception e2) {
			throw new Exception("Error recalculating order - " + e2.getMessage());
		}
	}
	private boolean validate_fieldinfo_fields(Connection conn){

		//Validate the entries here:
		boolean bEntriesAreValid = true;

		m_siID = m_siID.trim().replace(",", "");
        try {
			long lTest = Long.parseLong(m_siID);
			if ((lTest <=0)){
				super.addErrorMessage("ID '" + m_siID + "' is invalid.");
				bEntriesAreValid = false;
			}
        } catch (NumberFormatException e) {
        	super.addErrorMessage("ID '" + m_siID + "' is invalid.");
        	bEntriesAreValid = false;	
		}
		
		if (m_sOrderNumber.compareToIgnoreCase("") == 0){
			super.addErrorMessage("Order number cannot be empty.");
			bEntriesAreValid = false;
			return bEntriesAreValid;
		}
		if (m_sOrderNumber.length() > SMTableorderheaders.sOrderNumberLength){
			super.addErrorMessage("Order Number is too long.");
			bEntriesAreValid = false;
		}

		if (m_sBillToName.length() > SMTableorderheaders.sBillToNameLength){
			super.addErrorMessage("Bill to name is too long.");
			bEntriesAreValid = false;
		}

		if (m_sShipToName.length() > SMTableorderheaders.sShipToNameLength){
			super.addErrorMessage("Ship to name is too long.");
			bEntriesAreValid = false;
		}
		return bEntriesAreValid;
	}
	public void processLineDeletions(
			String sTrimmedOrderNumber,
			ArrayList<String>sDetailNumbers, 
			String sDBIB, 
			ServletContext context, 
			String sUserID,
			String sUserFullName
			) throws SQLException{
		
		if (sDetailNumbers.size() == 0){
			throw new SQLException("No lines were selected.");
		}
		
		Connection conn = clsDatabaseFunctions.getConnection(
			context, 
			sDBIB, 
			"MySQL", 
			this.toString() + ".processLineDeletions - user: " + sUserID + " - " + sUserFullName);
		if (conn == null){
			throw new SQLException("Could not get connection to delete selected lines.");
		}
		
		setM_strimmedordernumber(sTrimmedOrderNumber);
		
		SMLogEntry log = new SMLogEntry(sDBIB, context);
		
		String sDeletedDetailLines = "";
		for (int i = 0; i < sDetailNumbers.size(); i++){
			//Remove each detail from the order details array:
			sDeletedDetailLines += sDetailNumbers.get(i) + ", ";
		}
		try {
			deleteLines(sDetailNumbers, conn, sDBIB, context, sUserID, sUserFullName);
		} catch (Exception e) {
			log.writeEntry(
					sUserID, 
					SMLogEntry.LOG_OPERATION_SMDELETEORDERLINESFAIL,
					"On order number '" + sTrimmedOrderNumber + "', failed to delete detail lines: " 
						+ sDeletedDetailLines + ".",
					"ERROR: " + e.getMessage(),
					"[1376509304]");
			clsDatabaseFunctions.freeConnection(context, conn, "[1547067731]");
			throw new SQLException(e.getMessage());
		}
		if (bDebugMode){
			log.writeEntry(
					sUserID, 
				SMLogEntry.LOG_OPERATION_SMDELETEORDERLINESSUCCEED,
				"On order number '" + sTrimmedOrderNumber + "', successfully deleted detail lines: " 
					+ sDeletedDetailLines + ".",
				"SUCCESSFUL ORDER LINE DELETIONS",
				"[1376509292]");
		}
		clsDatabaseFunctions.freeConnection(context, conn, "[1547067732]");
		
	}
	private void deleteLines(
			ArrayList<String>sDetailNumbers, 
			Connection conn,
			String sDBIB,
			ServletContext context, 
			String sUserID,
			String sUserFullName
		) throws SQLException{
		
		if (!load(conn)){
			throw new SQLException("Could not load order to delete lines - " + getErrorMessages());
		}
		
		if (!loadDetailLines(conn)){
			throw new SQLException("Attempting to delete lines - " + getErrorMessages());
		}
		
		if (bDebugMode){
			System.out.println("In " + this.toString() + ".deleteLines - starting into removal loop");
		}
		
		//Don't let the user delete all the lines:
		if (sDetailNumbers.size() == m_arrDetails.size()){
			throw new SQLException("You cannot delete EVERY line from an order.");
		}
		
		//First validate that the lines can ALL be deleted:
		for (int i = 0; i < sDetailNumbers.size(); i++){
			//Remove each detail from the order details array:
			for (int j = 0; j < m_arrDetails.size(); j++){
				//Remove each detail from the order details array:
				SMOrderDetail line = m_arrDetails.get(j);
				if (line.getM_iDetailNumber().compareToIgnoreCase(sDetailNumbers.get(i)) == 0){
					//Validate the deletion - do not allow lines that have already been shipped to be deleted:
					if (new BigDecimal(line.getM_dQtyShippedToDate().replace(",", "")).compareTo(BigDecimal.ZERO) > 0){
						throw new SQLException("Some of the lines you wish to delete have previously been shipped and so cannot be deleted.");
					}
				}
			}
		}
		
		//Remove the lines from the array by matching the detail numbers:
		for (int i = 0; i < sDetailNumbers.size(); i++){
			//Remove each detail from the order details array:
			for (int j = 0; j < m_arrDetails.size(); j++){
				//Remove each detail from the order details array:
				SMOrderDetail line = m_arrDetails.get(j);
				if (bDebugMode){
					System.out.println("In " + this.toString() + ".deleteLines - "
						+ " sDetailNumbers(" + j + ") = '" + sDetailNumbers.get(i) + "', "
						+ "line.getM_iDetailNumber() = '" + line.getM_iDetailNumber() + "'."
					);
				}
				if (line.getM_iDetailNumber().compareToIgnoreCase(sDetailNumbers.get(i)) == 0){
					if (bDebugMode){
						System.out.println("In " + this.toString() + ".deleteLines - "
							+ " line is being removed."
						);
					}
					m_arrDetails.remove(j);
					
					//Now actually delete the lines from the database:
					String SQL = "DELETE FROM " + SMTableorderdetails.TableName
						+ " WHERE ("
							+ "(" + SMTableorderdetails.strimmedordernumber + " = '" 
								+ getM_strimmedordernumber() + "')"
							+ " AND (" + SMTableorderdetails.iDetailNumber + " = " + line.getM_iDetailNumber() + ")"
						+ ")"
					;
					try {
						Statement stmt = conn.createStatement();
						stmt.execute(SQL);
					} catch (Exception e) {
						throw new SQLException("Error deleting line from database with SQL: " 
							+ SQL + " - " + e.getMessage());
					}
					break;
				}
			}
		}
		
		//Now recalculate the entire order:
		try {
			recalculateEntireOrder(conn, false, false);
		} catch (Exception e2) {
			throw new SQLException("Error recalculating order - " + e2.getMessage());
		}

		try {
			Statement stmt = conn.createStatement();
			stmt.execute("START TRANSACTION");
		} catch (SQLException e) {
			throw new SQLException("Error starting data transaction - " + e.getMessage());
		}
		
		//Update the record:
		try {
			updateHeader(conn, sUserID, sUserFullName, "DELETEDLINES");
		} catch (Exception e1) {
			clsDatabaseFunctions.rollback_data_transaction(conn);
			throw new SQLException(e1.getMessage());
		}
		//If the last edit time doesn't match, return with an error:
		
		//Now actually save the lines:
		for (int i = 0; i < m_arrDetails.size(); i++){
			try {
				m_arrDetails.get(i).save_line(conn);
			} catch (Exception e) {
				clsDatabaseFunctions.rollback_data_transaction(conn);
				throw new SQLException("Line number " + m_arrDetails.get(i).getM_iLineNumber() 
					+ " for item '" + m_arrDetails.get(i).getM_sItemNumber() + "' - " 
					+ e.getMessage());
			}
		}
		
		//Check the 'LASTEDIT' in the stored record against this one:
		try {
			checkConcurrency(sDBIB, context);
		} catch (Exception e1) {
			clsDatabaseFunctions.rollback_data_transaction(conn);
			throw new SQLException("Error saving - " + e1.getMessage());
		}
		
		try {
			Statement stmt = conn.createStatement();
			stmt.execute("COMMIT");
		} catch (SQLException e) {
			clsDatabaseFunctions.rollback_data_transaction(conn);
			throw new SQLException("Error committing data transaction - " + e.getMessage());
		}
	}
	public void processLineUnships(
			String sTrimmedOrderNumber,
			ArrayList<String>sDetailNumbers, 
			String sDBIB, 
			ServletContext context, 
			String sUser,
			String sUserID,
			String sUserFullName
			) throws SQLException{
		
		if (sDetailNumbers.size() == 0){
			throw new SQLException("No lines were selected.");
		}
		
		Connection conn = clsDatabaseFunctions.getConnection(
			context, 
			sDBIB, 
			"MySQL", 
			this.toString() + ".processLineUnships - user: " + sUserID + " - " + sUserFullName);
		if (conn == null){
			throw new SQLException("Could not get connection to unship selected lines.");
		}
		
		setM_strimmedordernumber(sTrimmedOrderNumber);
		
		SMLogEntry log = new SMLogEntry(sDBIB, context);
		String sUnshippedDetailLines = "";
		for (int i = 0; i < sDetailNumbers.size(); i++){
			sUnshippedDetailLines += sDetailNumbers.get(i) + ", ";
		}
		try {
			unshipLines(sDetailNumbers, conn, sDBIB, context, sUserID, sUserFullName);
		} catch (Exception e) {
			log.writeEntry(
					sUserID, 
					SMLogEntry.LOG_OPERATION_SMUNSHIPORDERLINESFAIL,
					"On order number '" + sTrimmedOrderNumber + "', failed to unship detail lines: " 
						+ sUnshippedDetailLines + ".",
					"ERROR: " + e.getMessage(),
					"[1376509293]");
			clsDatabaseFunctions.freeConnection(context, conn, "[1547067739]");
			throw new SQLException(e.getMessage());
		}
		if (bDebugMode){
			log.writeEntry(
					sUserID, 
				SMLogEntry.LOG_OPERATION_SMUNSHIPORDERLINESSUCCEED,
				"On order number '" + sTrimmedOrderNumber + "', successfully unshipped detail lines: " 
					+ sUnshippedDetailLines + ".",
				"SUCCESSFUL ORDER LINE UNSHIPS",
				"[1376509294]");
		}
		clsDatabaseFunctions.freeConnection(context, conn, "[1547067740]");
	}
	private void unshipLines(
			ArrayList<String>sDetailNumbers, 
			Connection conn,
			String sDBIB,
			ServletContext context, 
			String sUserID,
			String sUserFullName
		) throws SQLException{
		
		if (!load(conn)){
			throw new SQLException("Could not load order to unship lines - " + getErrorMessages());
		}
		
		if (!loadDetailLines(conn)){
			throw new SQLException("Attempting to unship lines - " + getErrorMessages());
		}
				
		//Unship the lines from the array by matching the detail numbers:
		for (int i = 0; i < sDetailNumbers.size(); i++){
			for (int j = 0; j < m_arrDetails.size(); j++){
				SMOrderDetail line = m_arrDetails.get(j);
				if (line.getM_iDetailNumber().compareToIgnoreCase(sDetailNumbers.get(i)) == 0){
					m_arrDetails.get(j).setM_dQtyShipped("0.0000");
					//LTO 20120801 resetting the extended order price will set tax back to 0
					m_arrDetails.get(j).setM_dExtendedOrderPrice("0");
				}
			}
		}
		
		//Now recalculate the entire order:
		try {
			recalculateEntireOrder(conn, false, false);
		} catch (Exception e2) {
			throw new SQLException("Error recalculating order - " + e2.getMessage());
		}

		try {
			Statement stmt = conn.createStatement();
			stmt.execute("START TRANSACTION");
		} catch (SQLException e) {
			throw new SQLException("Error starting data transaction - " + e.getMessage());
		}
		
		//Update the record:
		try {
			updateHeader(conn, sUserID, sUserFullName, "UNSHIPPEDLINES");
		} catch (Exception e1) {
			clsDatabaseFunctions.rollback_data_transaction(conn);
			throw new SQLException(e1.getMessage());
		}
		
		//Now actually save the lines:
		for (int i = 0; i < m_arrDetails.size(); i++){
			try {
				m_arrDetails.get(i).save_line(conn);
			} catch (Exception e) {
				clsDatabaseFunctions.rollback_data_transaction(conn);
				throw new SQLException("Line number " + m_arrDetails.get(i).getM_iLineNumber() 
					+ " for item '" + m_arrDetails.get(i).getM_sItemNumber() + "' - " 
					+ e.getMessage());
			}
		}
		
		//Check the 'LASTEDIT' in the stored record against this one:
		try {
			checkConcurrency(sDBIB, context);
		} catch (Exception e1) {
			clsDatabaseFunctions.rollback_data_transaction(conn);
			throw new SQLException("Error saving - " + e1.getMessage());
		}
		
		try {
			Statement stmt = conn.createStatement();
			stmt.execute("COMMIT");
		} catch (SQLException e) {
			clsDatabaseFunctions.rollback_data_transaction(conn);
			throw new SQLException("Error committing data transaction - " + e.getMessage());
		}
	}
	public void processLineShips(
			String sTrimmedOrderNumber,
			ArrayList<String>sDetailNumbers,
			String sDBIB, 
			ServletContext context, 
			String sUser,
			String sUserID,
			String sUserFullName
			) throws SQLException{
		
		if (sDetailNumbers.size() == 0){
			throw new SQLException("No lines were selected.");
		}
		
		Connection conn = clsDatabaseFunctions.getConnection(
			context, 
			sDBIB, 
			"MySQL", 
			this.toString() + ".processLineShips - user: " + sUserID + " - " + sUserFullName);
		if (conn == null){
			throw new SQLException("Could not get connection to ship selected lines.");
		}
		
		setM_strimmedordernumber(sTrimmedOrderNumber);
		
		SMLogEntry log = new SMLogEntry(sDBIB, context);
		String sShippedDetailLines = "";
		for (int i = 0; i < sDetailNumbers.size(); i++){
			sShippedDetailLines += sDetailNumbers.get(i) + ", ";
		}
		try {
			shipLines(sDetailNumbers, conn, sDBIB, context, sUserID, sUserFullName);
		} catch (Exception e) {
			log.writeEntry(
					sUserID, 
					SMLogEntry.LOG_OPERATION_SMSHIPORDERLINESFAIL,
					"On order number '" + sTrimmedOrderNumber + "', failed to ship detail lines: " 
						+ sShippedDetailLines + ".",
					"ERROR: " + e.getMessage(),
					"[1376509294]");
			clsDatabaseFunctions.freeConnection(context, conn, "[1547067737]");
			throw new SQLException(e.getMessage());
		}
		if (bDebugMode){
			log.writeEntry(
					sUserID, 
				SMLogEntry.LOG_OPERATION_SMSHIPORDERLINESSUCCEED,
				"On order number '" + sTrimmedOrderNumber + "', successfully shipped detail lines: " 
					+ sShippedDetailLines + ".",
				"SUCCESSFUL ORDER LINE SHIPS",
				"[1376509295]");
		}
		clsDatabaseFunctions.freeConnection(context, conn, "[1547067738]");
	}
	private void shipLines(
			ArrayList<String>sDetailNumbers,
			Connection conn, 
			String sDBIB,
			ServletContext context, 
			String sUserID,
			String sUserFullName
		) throws SQLException{
		
		if (!load(conn)){
			throw new SQLException("Error [1395408953] Could not load order to ship lines - " + getErrorMessages());
		}
		
		if (!loadDetailLines(conn)){
			throw new SQLException("Error [1395408954] loading details to ship lines - " + getErrorMessages());
		}
				
		//Ship the lines from the array by matching the detail numbers:
		for (int i = 0; i < sDetailNumbers.size(); i++){
			for (int j = 0; j < m_arrDetails.size(); j++){
				SMOrderDetail line = m_arrDetails.get(j);
				if (line.getM_iDetailNumber().compareToIgnoreCase(sDetailNumbers.get(i)) == 0){
					//m_arrDetails.get(j).setM_dQtyShipped("0.0000");
					//If we are not passing in any qtys to ship, then just ship everything ordered:
					BigDecimal bdQtyOrdered = new BigDecimal(line.getM_dQtyOrdered().replace(",", ""));
					BigDecimal bdQtyShipped = new BigDecimal(line.getM_dQtyShipped().replace(",", ""));
			        if (bdQtyOrdered.compareTo(bdQtyShipped) > 0){
			        	m_arrDetails.get(j).setM_dQtyShipped(line.getM_dQtyOrdered());
			        }
			        try {
						calculateExtendedPrice(line);
					} catch (Exception e) {
						throw new SQLException(e.getMessage());
					}
				}
			}
		}
		//Now recalculate the entire order:
		try {
			recalculateEntireOrder(conn, false, false);
		} catch (Exception e2) {
			throw new SQLException("Error [1395408955] recalculating order - " + e2.getMessage());
		}

		try {
			Statement stmt = conn.createStatement();
			stmt.execute("START TRANSACTION");
		} catch (SQLException e) {
			throw new SQLException("Error [1395408956] starting data transaction - " + e.getMessage());
		}
		
		//Update the record:
		try {
			updateHeader(conn, sUserID, sUserFullName, "SHIPPEDLINES");
		} catch (Exception e1) {
			clsDatabaseFunctions.rollback_data_transaction(conn);
			throw new SQLException(e1.getMessage());
		}
		
		//Now actually save the lines:
		for (int i = 0; i < m_arrDetails.size(); i++){
			try {
				m_arrDetails.get(i).save_line(conn);
			} catch (Exception e) {
				clsDatabaseFunctions.rollback_data_transaction(conn);
				throw new SQLException("Error [1395408957] Line number " + m_arrDetails.get(i).getM_iLineNumber() 
					+ " for item '" + m_arrDetails.get(i).getM_sItemNumber() + "' - " 
					+ e.getMessage());
			}
		}
		
		//Check the 'LASTEDIT' in the stored record against this one:
		try {
			checkConcurrency(sDBIB, context);
		} catch (Exception e1) {
			clsDatabaseFunctions.rollback_data_transaction(conn);
			throw new SQLException("Error Error [1395408958] saving - " + e1.getMessage());
		}
		
		try {
			Statement stmt = conn.createStatement();
			stmt.execute("COMMIT");
		} catch (SQLException e) {
			clsDatabaseFunctions.rollback_data_transaction(conn);
			throw new SQLException("Error Error [1395408959] committing data transaction - " + e.getMessage());
		}
	}

	public void shipWorkOrderLinesForExistingDetails(
			ArrayList<String>sDetailNumbers,
			ArrayList<String>sQtys,
			SMWorkOrderHeader wo,
			String sMechID,
			String sMechanicName,
			String sMechanicInitials,
			String sWorkPerformedCodes,
			String sLocation,
			String sCategory,
			Connection conn, 
			String sDBIB,
			ServletContext context, 
			String sUserID,
			String sUserFullName
		) throws Exception{
		
		if (!load(conn)){
			throw new Exception("Error [1395408963] Could not load order to ship lines - " + getErrorMessages());
		}
		
		if (!loadDetailLines(conn)){
			throw new Exception("Error [1395408964] loading details to ship lines - " + getErrorMessages());
		}
				
		//Ship the lines from the array by matching the detail numbers:
		boolean bMatchingOrderLineFound = false;
		boolean bWPCsAlreadyPasted = false;
		for (int i = 0; i < sDetailNumbers.size(); i++){
			for (int j = 0; j < m_arrDetails.size(); j++){
				SMOrderDetail line = m_arrDetails.get(j);
				if (line.getM_iDetailNumber().compareToIgnoreCase(sDetailNumbers.get(i)) == 0){
					//If we are using the location from the work order details on every line, then we need to
					//get the location from every line:
					String sLineLocation = "";
					if (sLocation.compareToIgnoreCase(SMImportWorkOrdersEdit.LOCATION_OPTION_USE_WORK_ORDER_LOCATIONS) == 0){
						sLineLocation = wo.getDetailByOrderDetailNumber(line.getM_iDetailNumber()).getslocationcode();
					//But if the person importing the work order chose a particular location, then all of the work order
					//lines will carry THAT location when they are placed on the order:
					}else{
						sLineLocation = sLocation;
					}
					BigDecimal bdQtyOrdered = new BigDecimal(m_arrDetails.get(j).getM_dQtyOrdered().replace(",", ""));
					BigDecimal bdQtyAlreadyShipped = new BigDecimal(m_arrDetails.get(j).getM_dQtyShipped().replace(",", ""));
					BigDecimal bdQtyToShip = new BigDecimal(sQtys.get(i).replace(",", ""));
					if ((bdQtyAlreadyShipped.add(bdQtyToShip)).compareTo(bdQtyOrdered) > 0){
						throw new Exception("Shipping a qty of " + sQtys.get(i) + " for item number " + line.getM_sItemNumber()
							+ " will result in shipping more than the qty left on order."
						);
					}
					m_arrDetails.get(j).setM_dQtyShipped(sQtys.get(i));
					m_arrDetails.get(j).setM_sMechFullName(sMechanicName);
					m_arrDetails.get(j).setM_sMechID(sMechID);
					m_arrDetails.get(j).setM_sMechInitial(sMechanicInitials);
					m_arrDetails.get(j).setM_sLocationCode(sLineLocation);
					m_arrDetails.get(j).setM_sItemCategory(sCategory);
					if (!bWPCsAlreadyPasted){
						if (m_arrDetails.get(j).getM_mInvoiceComments().compareToIgnoreCase("") == 0){
							m_arrDetails.get(j).setM_mInvoiceComments(sWorkPerformedCodes);
						}else{
							m_arrDetails.get(j).setM_mInvoiceComments(m_arrDetails.get(j).getM_mInvoiceComments() + "\n" + sWorkPerformedCodes);
						}
						bWPCsAlreadyPasted = true;
					}
					/* FOR INTERNAL COMMENTS
					if (!bWPCsAlreadyPasted){
						if (m_arrDetails.get(j).getM_mInvoiceComments().compareToIgnoreCase("") == 0){
							m_arrDetails.get(j).setM_mInvoiceComments(sWorkPerformedCodes);
						}else{
							m_arrDetails.get(j).setM_mInvoiceComments(m_arrDetails.get(j).getM_mInvoiceComments() + "\n" + sWorkPerformedCodes);
						}
						bWPCsAlreadyPasted = true;
					}
					*/
			        try {
			        	//TJR - 5/21/2014 - commented this out because it was setting a lot of existing items unit price back to zero:
			        	//If the item has a zero list price, but people had set a price on it in the order, this was re-setting it 
			        	//back to zero.
			        	//TJR - 5/21/2014 - commented this out and added 'calculateExtendedPrice' instead:
			        	//updateLinePrice(line, conn);
			        	calculateExtendedPrice(line);
					} catch (Exception e) {
						throw new Exception(e.getMessage());
					}
			        bMatchingOrderLineFound = true;
				}
			}
			if (!bMatchingOrderLineFound){
				throw new Exception("Could not find existing order detail that is supposed to match work order detail: '" 
					+ sDetailNumbers.get(i) + "' - cannot import.");
			}
			bMatchingOrderLineFound = false;
		}
		//Now recalculate the entire order:
		try {
			recalculateEntireOrder(conn, false, false);
		} catch (Exception e2) {
			throw new SQLException("Error [1395408965] recalculating order - " + e2.getMessage());
		}
		
		//Update the record:
		try {
			updateHeader(conn, sUserID, sUserFullName, "IMPORTEDWOLINES");
		} catch (Exception e1) {
			throw new SQLException(e1.getMessage());
		}
		
		//Now actually save the lines:
		for (int i = 0; i < m_arrDetails.size(); i++){
			try {
				m_arrDetails.get(i).save_line(conn);
			} catch (Exception e) {
				throw new SQLException("Error [1395408967] Line number " + m_arrDetails.get(i).getM_iLineNumber() 
					+ " for item '" + m_arrDetails.get(i).getM_sItemNumber() + "' - " 
					+ e.getMessage());
			}
		}
		
		//Check the 'LASTEDIT' in the stored record against this one:
		try {
			checkConcurrency(sDBIB, context);
		} catch (Exception e1) {
			throw new SQLException("Error [1395408968] saving - " + e1.getMessage());
		}
	}
	
	public void processLineItemCreations(
			String sTrimmedOrderNumber,
			ArrayList<String>sDetailNumbers, 
			String sDBIB, 
			ServletContext context, 
			String sUserID,
			String sUserFullName
			
			) throws SQLException{
		if (sDetailNumbers.size() == 0){
			throw new SQLException("No lines were selected.");
		}
		
		Connection conn = clsDatabaseFunctions.getConnection(
			context, 
			sDBIB, 
			"MySQL", 
			this.toString() + ".processLineItemCreations - user: " + sUserID + " - " + sUserFullName);
		if (conn == null){
			throw new SQLException("Could not get connection to create items for selected lines.");
		}
		
		setM_strimmedordernumber(sTrimmedOrderNumber);
		
		SMLogEntry log = new SMLogEntry(sDBIB, context);
		String sItemCreatedDetailLines = "";
		for (int i = 0; i < sDetailNumbers.size(); i++){
			sItemCreatedDetailLines += sDetailNumbers.get(i).trim() + ", ";
		}
		try {
			createItemsForLines(sDetailNumbers, conn, sDBIB, context, sUserFullName, sUserID);
		} catch (Exception e) {
			log.writeEntry(
					sUserID, 
					SMLogEntry.LOG_OPERATION_SMCREATEITEMORDERLINESFAIL,
					"On order number '" + sTrimmedOrderNumber + "', failed to create items for detail lines: " 
						+ sItemCreatedDetailLines + ".",
					"ERROR: " + e.getMessage(),
					"[1376509296]");
			clsDatabaseFunctions.freeConnection(context, conn, "[1547067733]");
			throw new SQLException(e.getMessage());
		}
		if (bDebugMode){
			log.writeEntry(
					sUserID, 
				SMLogEntry.LOG_OPERATION_SMCREATEITEMORDERLINESSUCCEED,
				"On order number '" + sTrimmedOrderNumber + "', successfully created items for detail lines: " 
					+ sItemCreatedDetailLines + ".",
				"SUCCESSFUL ORDER LINE SHIPS",
				"[1376509297]");
		}
		clsDatabaseFunctions.freeConnection(context, conn, "[1547067734]");
	}
	private void createItemsForLines(
			ArrayList<String>sDetailNumbers, 
			Connection conn, 
			String sDBIB,
			ServletContext context, 
			String sUserFullName,
			String sUserID
		) throws SQLException{
		
		if (!load(conn)){
			throw new SQLException("Could not load order to create items for lines - " + getErrorMessages());
		}
		if (!loadDetailLines(conn)){
			throw new SQLException("Attempting to create items for lines - " + getErrorMessages());
		}
		
		//Create items for the lines from the array by matching the detail numbers:
		for (int i = 0; i < sDetailNumbers.size(); i++){
			for (int j = 0; j < m_arrDetails.size(); j++){
				SMOrderDetail line = m_arrDetails.get(j);
				if (line.getM_iDetailNumber().compareToIgnoreCase(sDetailNumbers.get(i).trim()) == 0){
					//First, confirm that the item on the line is not ALREADY an inventory item:
					String SQL = "SELECT"
						+ " " + SMTableicitems.inonstockitem
						+ " FROM " + SMTableicitems.TableName
						+ " WHERE ("
							+ "(" + SMTableicitems.sItemNumber + " = '" + line.getM_sItemNumber() + "')"
							+ " AND (" + SMTableicitems.inonstockitem + " = 0)"
						+ ")"
					;
					try {
						ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
						if (rs.next()){
							rs.close();
							throw new SQLException("Item number '" + line.getM_sItemNumber() 
									+ "' is already an inventory item; can't create item for line " + line.getM_iLineNumber());
						}
						rs.close();
					} catch (Exception e) {
						throw new SQLException("Error checking item number '" + line.getM_sItemNumber() 
								+ " - " + e.getMessage());
					}
					//Create the item
					String sNewItemNumber = "";
					ICItem item = new ICItem("");
					try {
						sNewItemNumber = item.getNextDedicatedItemNumberForOrder(conn, getM_strimmedordernumber());
					} catch (Exception e) {
						throw new SQLException("Error getting dedicated item number - " + e.getMessage());
					}
					item.setItemNumber(sNewItemNumber);
					item.setActive("1");
					item.setCategoryCode(line.getM_sItemCategory());
					item.setCostUnitOfMeasure(line.getM_sOrderUnitOfMeasure());
					item.setDedicatedToOrderNumber(getM_strimmedordernumber());
					item.setDefaultPriceListCode(getM_sDefaultPriceListCode());
					item.setHideOnInvoiceDefault(line.getM_isuppressdetailoninvoice());
					item.setItemDescription(line.getM_sItemDesc());
					item.setLaborItem("0");
					item.setLastEditUserFullName(sUserFullName);
					item.setMostRecentCost(line.getM_bdEstimatedUnitCost());
					item.setNewRecord("1");
					item.setNonStockItem("0");
					item.setNumberOfLabels(line.getM_dQtyOrdered());
					item.setSuppressItemQtyLookup("0");
					item.setTaxable(line.getM_iTaxable());
					if (!item.save(sUserFullName, sUserID, conn)){
						throw new SQLException("Error adding item '" + sNewItemNumber + "': " + item.getErrorMessageString());
					}
					
					//Update the line
					line.setM_iIsStockItem("1");
					line.setM_sItemNumber(sNewItemNumber);
			        
					//TJR - 2/22/2013 - Actually we do NOT want to calculate the extended price because we want this line to be exactly what it was
					//before, except for the item number
					//calculateExtendedPrice(line);
				}
			}
		}
		//Now recalculate the entire order:
		try {
			recalculateEntireOrder(conn, false, false);
		} catch (Exception e2) {
			throw new SQLException("Error recalculating order - " + e2.getMessage());
		}

		try {
			Statement stmt = conn.createStatement();
			stmt.execute("START TRANSACTION");
		} catch (SQLException e) {
			throw new SQLException("Error starting data transaction - " + e.getMessage());
		}
		
		//Update the record:
		try {
			updateHeader(conn, sUserID, sUserFullName, "CREATEDITEMSFORLINES");
		} catch (Exception e1) {
			clsDatabaseFunctions.rollback_data_transaction(conn);
			throw new SQLException(e1.getMessage());
		}
		
		//Now actually save the lines:
		for (int i = 0; i < m_arrDetails.size(); i++){
			try {
				m_arrDetails.get(i).save_line(conn);
			} catch (Exception e) {
				clsDatabaseFunctions.rollback_data_transaction(conn);
				throw new SQLException("Line number " + m_arrDetails.get(i).getM_iLineNumber() 
					+ " for item '" + m_arrDetails.get(i).getM_sItemNumber() + "' - " 
					+ e.getMessage());
			}
		}
		
		//Check the 'LASTEDIT' in the stored record against this one:
		try {
			checkConcurrency(sDBIB, context);
		} catch (Exception e1) {
			clsDatabaseFunctions.rollback_data_transaction(conn);
			throw new SQLException("Error saving - " + e1.getMessage());
		}
		
		try {
			Statement stmt = conn.createStatement();
			stmt.execute("COMMIT");
		} catch (SQLException e) {
			clsDatabaseFunctions.rollback_data_transaction(conn);
			throw new SQLException("Error committing data transaction - " + e.getMessage());
		}
	}
	public void processLineMoves(
			String sTrimmedOrderNumber,
			ArrayList<String>sDetailNumbers,
			String sLineNumberToMoveAbove,
			String sDBIB, 
			ServletContext context, 
			String sUser,
			String sUserID,
			String sUserFullName
			) throws SQLException{
		
		if (sDetailNumbers.size() == 0){
			throw new SQLException("No lines were selected.");
		}
		
		Connection conn = clsDatabaseFunctions.getConnection(
			context, 
			sDBIB, 
			"MySQL", 
			this.toString() + ".processLineMoves - user: " + sUserID + " - " + sUserFullName);
		if (conn == null){
			throw new SQLException("Could not get connection to move selected lines.");
		}
		
		setM_strimmedordernumber(sTrimmedOrderNumber);
		
		SMLogEntry log = new SMLogEntry(sDBIB, context);
		String sMovedDetailLines = "";
		for (int i = 0; i < sDetailNumbers.size(); i++){
			sMovedDetailLines += sDetailNumbers.get(i) + ", ";
		}
		try {
			moveLines(sDetailNumbers, sLineNumberToMoveAbove, conn, sDBIB, context, sUserID, sUserFullName);
		} catch (Exception e) {
			log.writeEntry(
					sUserID, 
					SMLogEntry.LOG_OPERATION_SMMOVEORDERLINESFAIL,
					"On order number '" + sTrimmedOrderNumber + "', failed to move detail lines: " 
						+ sMovedDetailLines + ".",
					"ERROR: " + e.getMessage(),
					"[1376509298]");
			clsDatabaseFunctions.freeConnection(context, conn, "[1547067735]");
			throw new SQLException(e.getMessage());
		}
		if (bDebugMode){
			log.writeEntry(
					sUserID, 
				SMLogEntry.LOG_OPERATION_SMMOVEORDERLINESSUCCEED,
				"On order number '" + sTrimmedOrderNumber + "', successfully moved detail lines: " 
					+ sMovedDetailLines + " above line number: " + sLineNumberToMoveAbove + ".",
				"SUCCESSFUL ORDER LINE DELETIONS",
				"[1376509299]");
		}
		clsDatabaseFunctions.freeConnection(context, conn, "[1547067736]");
		
	}
	public void moveLines(
			ArrayList<String>sDetailNumbersOfLinesToBeMoved,
			String sLineNumberToMoveAbove,
			Connection conn,
			String sDBIB,
			ServletContext context,
			String sUserID,
			String sUserFullName
		) throws Exception{
		
		if (!load(conn)){
			throw new Exception("Could not load order to move lines - " + getErrorMessages());
		}
		
		if (!loadDetailLines(conn)){
			throw new Exception("Attempting to move lines - " + getErrorMessages());
		}
		//First get the detail line number of the line to move above:
		String sDetailNumberToMoveAbove = "";
		for (int i = 0; i < m_arrDetails.size(); i++){
			if (m_arrDetails.get(i).getM_iLineNumber().compareToIgnoreCase(sLineNumberToMoveAbove) == 0){
				sDetailNumberToMoveAbove = m_arrDetails.get(i).getM_iDetailNumber();
				break;
			}
		}
		
		//Separate the lines into the ones selected to be moved, and the ones NOT selected to be moved:
		ArrayList<SMOrderDetail>arrMovedLines = new ArrayList<SMOrderDetail>(0);
		ArrayList<SMOrderDetail>arrUnmovedLines = new ArrayList<SMOrderDetail>(0);
		//First, copy all the lines to the 'TempLines' array:
		boolean bAdded;
		for (int i = 0; i < m_arrDetails.size(); i++){
			bAdded = false;
			for (int j = 0; j < sDetailNumbersOfLinesToBeMoved.size(); j++){
				if (m_arrDetails.get(i).getM_iDetailNumber().compareToIgnoreCase(sDetailNumbersOfLinesToBeMoved.get(j)) == 0){
					arrMovedLines.add(m_arrDetails.get(i));
					bAdded = true;
					break;
				}
			}
			if (!bAdded){
				arrUnmovedLines.add(m_arrDetails.get(i));
			}
		}
		
		//At this point, all the lines which have been selected to be 'moved' are in the 'arrMovedLines' array, and the remaining lines are in the 'arrUnovedLines' array
		
		//Now clear the original array, and start adding the lines back in:
		//Clear the original array:
		m_arrDetails.clear();
		//Now add back in the lines UP TO the line we want to insert above:
		for (int i = 0; i < arrUnmovedLines.size(); i++){
			//If we have reached the line we want to add ABOVE, then insert the lines which were selected to BE MOVED:
			if (arrUnmovedLines.get(i).getM_iDetailNumber().compareToIgnoreCase(sDetailNumberToMoveAbove) == 0){
				//Now add back in all the lines we moved out:
				try {
					for (int j = 0; j < arrMovedLines.size(); j++){
						m_arrDetails.add(arrMovedLines.get(j));
					}
				} catch (Exception e) {
					throw new Exception("Error adding arrMovedLines.get() - " + e.getMessage());
				}
			}
			//Add the line back into the details array:
			try {
				m_arrDetails.add(arrUnmovedLines.get(i));
			} catch (Exception e) {
				throw new Exception("Error adding arrUnmovedLines.get(" + i + ") - " + e.getMessage());
			}
		}
		
		//Renumber the lines one last time:
		for (int i = 0; i < m_arrDetails.size(); i++){
			m_arrDetails.get(i).setM_iLineNumber(Integer.toString(i + 1));
		}

		try {
			Statement stmt = conn.createStatement();
			stmt.execute("START TRANSACTION");
		} catch (SQLException e) {
			throw new Exception("Error starting data transaction - " + e.getMessage());
		}
		//Update the record:
		try {
			updateHeader(conn, sUserID, sUserFullName, "MOVEDLINES");
		} catch (Exception e1) {
			clsDatabaseFunctions.rollback_data_transaction(conn);
			throw new Exception("Error saving order with moved lines - " + e1.getMessage());
		}

		//Now actually save the lines:
		for (int i = 0; i < m_arrDetails.size(); i++){
			try {
				if (bDebugMode){
					System.out.println("In " + this.toString() + ".moveLines - saving detail with line number = " 
						+ m_arrDetails.get(i).getM_iLineNumber());
				}
				m_arrDetails.get(i).save_line(conn);
			} catch (SQLException e) {
				clsDatabaseFunctions.rollback_data_transaction(conn);
				throw new Exception("Line number " + m_arrDetails.get(i).getM_iLineNumber() 
					+ " for item '" + m_arrDetails.get(i).getM_sItemNumber() + "' - " 
					+ e.getMessage());
			}
		}
		
		//Check the 'LASTEDIT' in the stored record against this one:
		try {
			checkConcurrency(sDBIB, context);
		} catch (Exception e1) {
			clsDatabaseFunctions.rollback_data_transaction(conn);
			throw new Exception("Error saving order with moved lines - " + e1.getMessage());
		}
		try {
			Statement stmt = conn.createStatement();
			stmt.execute("COMMIT");
		} catch (SQLException e) {
			clsDatabaseFunctions.rollback_data_transaction(conn);
			throw new Exception("Error committing data transaction - " + e.getMessage());
		}
	}
	
	public void processLineCopy(
			String sTrimmedOrderNumber,
			ArrayList<String>sDetailNumbers,
			String sLineNumberToCopyAbove,
			String sDBIB, 
			ServletContext context, 
			String sUser,
			String sUserID,
			String sUserFullName
			) throws SQLException{
		
		if (sDetailNumbers.size() == 0){
			throw new SQLException("No lines were selected.");
		}
		
		Connection conn = clsDatabaseFunctions.getConnection(
			context, 
			sDBIB, 
			"MySQL", 
			this.toString() + ".processLineMoves - user: " + sUserID + " - " + sUserFullName);
		if (conn == null){
			throw new SQLException("Could not get connection to copy selected lines.");
		}
		
		setM_strimmedordernumber(sTrimmedOrderNumber);
		
		SMLogEntry log = new SMLogEntry(sDBIB, context);
		String sCopiedDetailLines = "";
		for (int i = 0; i < sDetailNumbers.size(); i++){
			sCopiedDetailLines += sDetailNumbers.get(i) + ", ";
		}
		try {
			copyLines(sDetailNumbers, sLineNumberToCopyAbove, conn, sDBIB, context, sUserID, sUserFullName);
		} catch (Exception e) {
			log.writeEntry(
					sUserID, 
					SMLogEntry.LOG_OPERATION_SMCOPYORDERLINESFAIL,
					"On order number '" + sTrimmedOrderNumber + "', failed to move detail lines: " 
						+ sCopiedDetailLines + ".",
					"ERROR: " + e.getMessage(),
					"[1392658098]");
			clsDatabaseFunctions.freeConnection(context, conn, "[1547067729]");
			throw new SQLException(e.getMessage());
		}
		if (bDebugMode){
			log.writeEntry(
					sUserID, 
				SMLogEntry.LOG_OPERATION_SMMOVEORDERLINESSUCCEED,
				"On order number '" + sTrimmedOrderNumber + "', successfully moved detail lines: " 
					+ sCopiedDetailLines + " above line number: " + sLineNumberToCopyAbove + ".",
				"SUCCESSFUL ORDER LINE DELETIONS",
				"[1392658099]");
		}
		clsDatabaseFunctions.freeConnection(context, conn, "[1547067730]");
		
	}
	public void copyLines(
			ArrayList<String>sDetailNumbers,
			String sLineNumberToCopyAbove,
			Connection conn,
			String sDBIB,
			ServletContext context,
			String sUserID,
			String sUserFullName
		) throws Exception{
		
		if (!load(conn)){
			throw new Exception("Could not load order to copy lines - " + getErrorMessages());
		}
		
		if (!loadDetailLines(conn)){
			throw new Exception("Attempting to copy lines - " + getErrorMessages());
		}
		//First get the detail line number of the line to move above:
		String sDetailNumberToCopyAbove = "";
		for (int i = 0; i < m_arrDetails.size(); i++){
			if (m_arrDetails.get(i).getM_iLineNumber().compareToIgnoreCase(sLineNumberToCopyAbove) == 0){
				sDetailNumberToCopyAbove = m_arrDetails.get(i).getM_iDetailNumber();
				break;
			}
		}
		//Create an array of details that need to be copied
		ArrayList<SMOrderDetail>arrCopiedLines = new ArrayList<SMOrderDetail>(0);
		//Also create an array of all the original details so we can add them back in later.
		ArrayList<SMOrderDetail>arrOriginalLines = new ArrayList<SMOrderDetail>(0);
		SMOrderDetail cDetail = new SMOrderDetail();
		int iNewLineCounter = 0;
		//First, copy all the lines to the 'TempLines' array:
		for (int i = 0; i < m_arrDetails.size(); i++){
			for (int j = 0; j < sDetailNumbers.size(); j++){
				if (m_arrDetails.get(i).getM_iDetailNumber().compareToIgnoreCase(sDetailNumbers.get(j)) == 0){
					cDetail = CloneDetail(m_arrDetails.get(i));
					//assign new detail number so to distinguish this line from its origin
					cDetail.setM_iDetailNumber(Integer.toString(calculateNextDetailNumber() + iNewLineCounter));
				    //Set the next detail number on the order using the detail number of this line:
					setM_iNextDetailNumber(Integer.toString(Integer.parseInt(cDetail.getM_iDetailNumber()) + 1));
					//System.out.println("[1392750757] set detail number: " + cDetail.getM_iDetailNumber());
					//System.out.println("[1392750758] new next detail number: " + getM_iNextDetailNumber());
					arrCopiedLines.add(cDetail);
					iNewLineCounter++;
					break;
				}
			}
			arrOriginalLines.add(m_arrDetails.get(i));
		}
		
		//Now clear the original array, and start adding the lines back in:
		//Clear the original array:
		m_arrDetails.clear();
		//Now add back in the lines UP TO the line we want to insert above:
		for (int i = 0; i < arrOriginalLines.size(); i++){
			//If we have reached the line we want to add ABOVE, then insert the moved lines:
			if (arrOriginalLines.get(i).getM_iDetailNumber().compareToIgnoreCase(sDetailNumberToCopyAbove) == 0){
				//Now add back in all the lines we copied:
				try {
					for (int j = 0; j < arrCopiedLines.size(); j++){
						//System.out.println("[1392670998] inserting copied line # " + j + ": " + arrCopiedLines.get(j).getM_iLineNumber() + " / " + arrCopiedLines.get(j).getM_iDetailNumber());
						m_arrDetails.add(arrCopiedLines.get(j));
					}
				} catch (Exception e) {
					throw new Exception("Error adding arrCopiedLines.get() - " + e.getMessage());
				}
			}
			//Add the line back into the details array:
			try {
				//System.out.println("[1392670998] inserting original line # " + i + ": " + arrOriginalLines.get(i).getM_iLineNumber() + " / " + arrOriginalLines.get(i).getM_iDetailNumber());
				m_arrDetails.add(arrOriginalLines.get(i));
			} catch (Exception e) {
				throw new Exception("Error adding arrOriginalLines.get(" + i + ") - " + e.getMessage());
			}
		}
		//set the total number of lines on this order
		setM_iNumberOfLinesOnOrder(Integer.toString(m_arrDetails.size()));
		//Renumber the lines one last time:
		for (int i = 0; i < m_arrDetails.size(); i++){
			m_arrDetails.get(i).setM_iLineNumber(Integer.toString(i + 1));
			//System.out.println("[1392670998] detail number: " + m_arrDetails.get(i).getM_iDetailNumber());
			//System.out.println("[1392670998] line number: " + m_arrDetails.get(i).getM_iLineNumber());
		}

		try {
			Statement stmt = conn.createStatement();
			stmt.execute("START TRANSACTION");
		} catch (SQLException e) {
			throw new Exception("Error starting data transaction - " + e.getMessage());
		}
		//Update the record:
		try {
			updateHeader(conn, sUserID, sUserFullName, "COPIEDLINES");
		} catch (Exception e1) {
			clsDatabaseFunctions.rollback_data_transaction(conn);
			throw new Exception("Error saving order with copied lines - " + e1.getMessage());
		}

		//Now actually save the lines:
		for (int i = 0; i < m_arrDetails.size(); i++){
			try {
				if (bDebugMode){
					System.out.println("In " + this.toString() + ".copyLines - saving detail with line number = " 
						+ m_arrDetails.get(i).getM_iLineNumber());
				}
				m_arrDetails.get(i).save_line(conn);
			} catch (SQLException e) {
				clsDatabaseFunctions.rollback_data_transaction(conn);
				throw new Exception("Line number " + m_arrDetails.get(i).getM_iLineNumber() 
					+ " for item '" + m_arrDetails.get(i).getM_sItemNumber() + "' - " 
					+ e.getMessage());
			}
		}
		
		//Check the 'LASTEDIT' in the stored record against this one:
		try {
			checkConcurrency(sDBIB, context);
		} catch (Exception e1) {
			clsDatabaseFunctions.rollback_data_transaction(conn);
			throw new Exception("Error saving order with copied lines - " + e1.getMessage());
		}
		try {
			Statement stmt = conn.createStatement();
			stmt.execute("COMMIT");
		} catch (SQLException e) {
			clsDatabaseFunctions.rollback_data_transaction(conn);
			throw new Exception("Error committing data transaction - " + e.getMessage());
		}
	}
	//#####################################################################################################
	
	public void setMechanic(
			String sTrimmedOrderNumber,
			ArrayList<String>sDetailNumbers,
			String sSelectedMechanicID,
			String sDBIB, 
			ServletContext context, 
			String sUser,
			String sUserID,
			String sUserFullName) throws SQLException{
		
		if (sDetailNumbers.size() == 0){
			throw new SQLException("No lines were selected.");
		}
		
		Connection conn = clsDatabaseFunctions.getConnection(
			context, 
			sDBIB, 
			"MySQL", 
			this.toString() + ".setMechanics - user: " + sUser);
		if (conn == null){
			throw new SQLException("Could not get connection to set mechanics.");
		}
		
		setM_strimmedordernumber(sTrimmedOrderNumber);
		
		SMLogEntry log = new SMLogEntry(sDBIB, context);
		String sSelectedLines = "";
		for (int i = 0; i < sDetailNumbers.size(); i++){
			sSelectedLines += sDetailNumbers.get(i) + ", ";
		}
		sSelectedLines = sSelectedLines.substring(0, sSelectedLines.length() - 2);
		try {
			//get mechanic info first.
			String sMechanicInitials = "N/A";
			String sMechanicFullName = "N/A";
			if (
				(sSelectedMechanicID.compareToIgnoreCase("") != 0)
				& (sSelectedMechanicID.compareToIgnoreCase("0") != 0)
			){
				String SQL = "SELECT * FROM " + SMTablemechanics.TableName
					+ " WHERE ("
						+ "(" + SMTablemechanics.lid + " = " + sSelectedMechanicID + ")"
					+ ")"
				;
				ResultSet rsMechanicInfo = clsDatabaseFunctions.openResultSet(SQL, conn);
				if (rsMechanicInfo.next()){
					sMechanicInitials = rsMechanicInfo.getString(SMTablemechanics.sMechInitial);
					sMechanicFullName = rsMechanicInfo.getString(SMTablemechanics.sMechFullName);
				}else{
					throw new SQLException(
						"Error [1413813719] - Failed to get mechanic info in SMOrderHeader.setMechanics() with ID: '" 
						+ sSelectedMechanicID + "'.");
				}
				rsMechanicInfo.close();
			}
			setMechanic(sDetailNumbers,
				sSelectedMechanicID,
				sMechanicInitials,
				sMechanicFullName,
				conn,
				sDBIB,
				context, 
				sUserID,
				sUserFullName);
		} catch (Exception e) {
			log.writeEntry(
					sUserID, 
					SMLogEntry.LOG_OPERATION_SMSETDETAILMECHANICSFAIL,
					"On order number '" + sTrimmedOrderNumber + "', failed to set mechanics for detail lines: " 
						+ sSelectedLines + ".",
					"ERROR: " + e.getMessage(),
					"[1376509300]");
			clsDatabaseFunctions.freeConnection(context, conn, "[1547067745]");
			throw new SQLException(e.getMessage());
		}
		if (bDebugMode){
			log.writeEntry(
					sUserID, 
				SMLogEntry.LOG_OPERATION_SMSETDETAILMECHANICSSSUCCEED,
				"On order number '" + sTrimmedOrderNumber + "', successfully set mechanics for  detail lines: " 
					+ sSelectedLines + " above line number: " + sSelectedLines + ".",
				"SUCCESSFUL SETTING ORDER LINE MECHANICS",
				"[1376509301]");
		}
		clsDatabaseFunctions.freeConnection(context, conn, "[1547067746]");
		
	}
	private void setMechanic(ArrayList<String>sDetailNumbers,
							  String sMechID,
							  String sMechInit,
							  String sMechName,
							  Connection conn, 
							  String sDBIB,
							  ServletContext context, 
							  String sUserID,
							  String sUserFullName
							  ) throws SQLException{
		//String SQL = "";
		if (!load(conn)){
			throw new SQLException("Could not load order to move lines - " + getErrorMessages());
		}
		
		if (!loadDetailLines(conn)){
			throw new SQLException("Attempting to move lines - " + getErrorMessages());
		}
		
		for (int i=0;i<m_arrDetails.size();i++){
			if (sDetailNumbers.indexOf(m_arrDetails.get(i).getM_iDetailNumber()) >= 0){
				m_arrDetails.get(i).setM_sMechID(sMechID);
				m_arrDetails.get(i).setM_sMechInitial(sMechInit);
				m_arrDetails.get(i).setM_sMechFullName(sMechName);
			}
		}

		if (!clsDatabaseFunctions.start_data_transaction(conn)){
			//Statement stmt = conn.createStatement();
			//stmt.execute("START TRANSACTION");
			throw new SQLException("Error starting data transaction");
		}
		
		//Update the record:
		try {
			updateHeader(conn, sUserID, sUserFullName, "SETMECHANIC");
		} catch (Exception e1) {
			clsDatabaseFunctions.rollback_data_transaction(conn);
			throw new SQLException(e1.getMessage());
		}
		
		//Now actually save the lines:
		for (int i = 0; i < m_arrDetails.size(); i++){
			try {
				if (bDebugMode){
					System.out.println("In " + this.toString() + ".setMechanic - saving detail with line number = " 
						+ m_arrDetails.get(i).getM_iLineNumber());
				}
				m_arrDetails.get(i).save_line(conn);
			} catch (Exception e) {
				clsDatabaseFunctions.rollback_data_transaction(conn);
				throw new SQLException("Line number " + m_arrDetails.get(i).getM_iLineNumber() 
					+ " for item '" + m_arrDetails.get(i).getM_sItemNumber() + "' - " 
					+ e.getMessage());
			}
		}
		//Check the 'LASTEDIT' in the stored record against this one with a NEW CONNECTION:
		try {
			checkConcurrency(sDBIB, context);
		} catch (Exception e1) {
			clsDatabaseFunctions.rollback_data_transaction(conn);
			throw new SQLException(e1.getMessage());
		}
		if (!clsDatabaseFunctions.commit_data_transaction(conn)){
			clsDatabaseFunctions.rollback_data_transaction(conn);
			throw new SQLException("Error committing data transaction");
		}
	}

	public void repriceQuote(
			String sTrimmedOrderNumber,
			ArrayList<String>sDetailNumbers,
			String sSelectedRepriceMethod,
			String sRepriceAmt,
			String sDBIB, 
			ServletContext context, 
			String sUser,
			String sUserID,
			String sUserFullName
			
			) throws SQLException{
		
		if (sDetailNumbers.size() == 0){
			throw new SQLException("No lines were selected.");
		}
		
		if (!isDecimalValid("Repricing amount", sRepriceAmt.replace(",", ""), 2)){
			throw new SQLException("The 'reprice amount' ('" + sRepriceAmt + "' is invalid.");
		}
		
		Connection conn = clsDatabaseFunctions.getConnection(
			context, 
			sDBIB, 
			"MySQL", 
			this.toString() + ".setMechanics - user: " + sUserID + " - " + sUserFullName);
		if (conn == null){
			throw new SQLException("Could not get connection to reprice quote.");
		}
		
		setM_strimmedordernumber(sTrimmedOrderNumber);
		
		SMLogEntry log = new SMLogEntry(sDBIB, context);
		String sSelectedLines = "";
		for (int i = 0; i < sDetailNumbers.size(); i++){
			sSelectedLines += sDetailNumbers.get(i) + ", ";
		}
		sSelectedLines = sSelectedLines.substring(0, sSelectedLines.length() - 2);
		try {
			repriceQuote(sDetailNumbers, 
					sSelectedRepriceMethod,
					sRepriceAmt,
					conn,
					sDBIB,
					context, 
					sUserID,
					sUserFullName);
		} catch (Exception e) {
			log.writeEntry(
					sUserID, 
					SMLogEntry.LOG_OPERATION_SMREPRICEQUOTEFAIL,
					"On order number '" + sTrimmedOrderNumber + "', failed to reprice lines: " 
						+ sSelectedLines + ".",
					"ERROR: " + e.getMessage(),
					"[1376509302]");
			clsDatabaseFunctions.freeConnection(context, conn, "[1547067742]");
			throw new SQLException(e.getMessage());
		}
		if (bDebugMode){
			log.writeEntry(
					sUserID, 
				SMLogEntry.LOG_OPERATION_SMREPRICEQUOTESUCCEED,
				"On order number '" + sTrimmedOrderNumber + "', successfully repriced detail lines: " 
					+ sSelectedLines + " above line number: " + sSelectedLines + ".",
				"SUCCESSFUL ORDER LINE REPRICING",
				"[1376509303]");
		}
		clsDatabaseFunctions.freeConnection(context, conn, "[1547067741]");
	}
	private void repriceQuote(ArrayList<String>sDetailNumbers,
			String sRepriceMethod,
			String sRepriceAmt,
			Connection conn, 
			String sDBIB,
			ServletContext context, 
			String sUserID,
			String sUserFullName
			) throws SQLException{
		//String SQL = "";
		if (!load(conn)){
			throw new SQLException("Could not load order to reprice quote - " + getErrorMessages());
		}

		if (!loadDetailLines(conn)){
			throw new SQLException("Attempting to reprice quote - " + getErrorMessages());
		}

		BigDecimal bdTotalEstimatedCost = new BigDecimal(0.00);
		BigDecimal bdTotalPrice = new BigDecimal(0.00);
		BigDecimal bdTotalLaborUnits = new BigDecimal(0.00);
		BigDecimal bdRepriceAmt = new BigDecimal(sRepriceAmt.replace(",", ""));
		
		//First get the totals:
		for (int i=0;i<m_arrDetails.size();i++){
			BigDecimal bdEstimatedUnitCost = new BigDecimal(m_arrDetails.get(i).getM_bdEstimatedUnitCost().replace(",", ""));
			BigDecimal bdQtyQuoted = new BigDecimal(m_arrDetails.get(i).getM_dQtyOrdered().replace(",", ""));
			bdTotalEstimatedCost = bdTotalEstimatedCost.add(bdEstimatedUnitCost.multiply(bdQtyQuoted));
			bdTotalPrice = bdTotalPrice.add(new BigDecimal(m_arrDetails.get(i).getM_dExtendedOrderPrice().replace(",", "")));
			ICItem item = new ICItem(m_arrDetails.get(i).getM_sItemNumber());
			if (!item.load(conn)){
				throw new SQLException("Could not read labor status of item number '" + item.getItemNumber() + " - " + item.getErrorMessageString());
			}
			if (item.getLaborItem().compareToIgnoreCase("1") == 0){
				bdTotalLaborUnits = bdTotalLaborUnits.add(new BigDecimal(m_arrDetails.get(i).getM_dQtyOrdered().replace(",", "")));
			}
		}

		//So we have to mark up each of the selected items an additional amount that will total to the difference we need.
		//First we need to get the total mark up amount of the UNselected items and subtract that total from the requested total MU:
		BigDecimal bdTotalMUOfUnselectedLines = new BigDecimal(0.00);
		BigDecimal bdExtendedEstimatedCost = new BigDecimal(0.00);
		BigDecimal bdTotalEstimatedCostOfSelectedLines = new BigDecimal(0.00);
		BigDecimal bdTotalPriceOfSelectedLines = new BigDecimal(0.00);
		BigDecimal bdTotalEstimatedCostForOrder = new BigDecimal(0.00);
		for (int i=0;i<m_arrDetails.size();i++){
			bdExtendedEstimatedCost = new BigDecimal(m_arrDetails.get(i).getM_bdEstimatedUnitCost().replace(",", "")).multiply(
				new BigDecimal(m_arrDetails.get(i).getM_dQtyOrdered().replace(",", "")));
			bdTotalEstimatedCostForOrder = bdTotalEstimatedCostForOrder.add(bdExtendedEstimatedCost);
			if (sDetailNumbers.indexOf(m_arrDetails.get(i).getM_iDetailNumber()) < 0){
				bdTotalMUOfUnselectedLines = bdTotalMUOfUnselectedLines.add(
					new BigDecimal(m_arrDetails.get(i).getM_dExtendedOrderPrice().replace(",", "")).subtract(
					bdExtendedEstimatedCost));
			}else{
				bdTotalEstimatedCostOfSelectedLines = bdTotalEstimatedCostOfSelectedLines.add(bdExtendedEstimatedCost);
				bdTotalPriceOfSelectedLines = bdTotalPriceOfSelectedLines.add(
					new BigDecimal(m_arrDetails.get(i).getM_dExtendedOrderPrice().replace(",", "")));
			}
		}

		//if (bdTotalEstimatedCostOfSelectedLines.compareTo(BigDecimal.ZERO) == 0){
		//	throw new SQLException("The total estimated cost of the selected lines is zero, and so the program cannot properly calculate a markup for this quote.");
		//}
		
		//Now figure out how to re-price:
		int iRepricingMethod = Integer.parseInt(sRepriceMethod);
		BigDecimal bdRequestedTotalMarkup = new BigDecimal(0.00);
		switch (iRepricingMethod){
		case SMOrderDetailList.REPRICEUSINGMARKUP:
			bdRequestedTotalMarkup = bdRepriceAmt;
			break;
		case SMOrderDetailList.REPRICEUSINGMULTIPLIER:
			bdRequestedTotalMarkup = bdTotalEstimatedCost.multiply(bdRepriceAmt.subtract(BigDecimal.ONE));
			break;
		case SMOrderDetailList.REPRICEUSINGMUPERLABORUNIT:
			bdRequestedTotalMarkup = bdTotalLaborUnits.multiply(bdRepriceAmt);
			break;
		case SMOrderDetailList.REPRICEUSINGMARGIN:
			//The formula for figuring markup from margin is:
			// MU = (MARGIN * COST) / (1 - MARGIN)
			
			bdRepriceAmt = bdRepriceAmt.divide(new BigDecimal("100.00"));
			
			if (bdRepriceAmt.compareTo(new BigDecimal("100.00")) >= 0){
				throw new SQLException("The gross profit percentage must be lower than 100 percent.");
			}
			bdRequestedTotalMarkup = (bdRepriceAmt.multiply(bdTotalEstimatedCostForOrder)).divide((BigDecimal.ONE.subtract(bdRepriceAmt)), 2, RoundingMode.HALF_UP);
			break;
		case SMOrderDetailList.REPRICEUSINGTOTALPRICE:
			//LTO
			bdRequestedTotalMarkup = bdRepriceAmt.subtract(bdTotalEstimatedCost);
			break;
			
		default:
			bdRequestedTotalMarkup = new BigDecimal(sRepriceAmt.replace(",", ""));
			break;
		}		
		
		//This will be the total amount we'll need to mark up the selected lines to reach the requested MU: 
		BigDecimal bdMUToProportionAmongSelectedLines = new BigDecimal(0.00);
		bdMUToProportionAmongSelectedLines = bdRequestedTotalMarkup.subtract(bdTotalMUOfUnselectedLines);

		//So we need to figure out how to add bdMUToProportionAmongSelectedLines to the selected lines.  Since the lines probably have different
		//extended prices, we need to figure out how to add proportionate amounts to each line's cost to get the desired markup in total.
		
		//Each line's cost represents some fraction of the total cost of the selected lines.  That fraction represents how much of the
		//total markup we need to add to each line's cost to make sure we get to the right total amount of MU.
		
		//This will be the factor we need to proportion each line to get to the selling price:
		BigDecimal bdFractionOfCostPerLine = new BigDecimal(0.00);
		for (int i=0;i<m_arrDetails.size();i++){
			if (sDetailNumbers.indexOf(m_arrDetails.get(i).getM_iDetailNumber()) >= 0){
				BigDecimal bdEstimatedUnitCost = new BigDecimal(m_arrDetails.get(i).getM_bdEstimatedUnitCost().replace(",", ""));
				BigDecimal bdQtyOrdered = new BigDecimal(m_arrDetails.get(i).getM_dQtyOrdered().replace(",", ""));
				if (bdQtyOrdered.compareTo(BigDecimal.ZERO) == 0){
					throw new SQLException("Line " + m_arrDetails.get(i).getM_iLineNumber() + " has no qty - the program can't reprice the quote.");
				}
				BigDecimal bdExtendedEstimatedLineCost = bdEstimatedUnitCost.multiply(bdQtyOrdered);
				
				//This number is the fraction which represents this line's share of the total costs of the selected lines:
				if (bdTotalEstimatedCostOfSelectedLines.compareTo(BigDecimal.ZERO) == 0){
					bdFractionOfCostPerLine = BigDecimal.ONE.divide(new BigDecimal(Integer.toString(sDetailNumbers.size())), 2, RoundingMode.HALF_UP);
				}else{
					bdFractionOfCostPerLine = bdExtendedEstimatedLineCost.divide(bdTotalEstimatedCostOfSelectedLines, 2, RoundingMode.HALF_UP);
				}
				//This number is the amount of mark up we need to add to this line:
				BigDecimal bdAmountToMarkUpLine = new BigDecimal(0.00);
				bdAmountToMarkUpLine = bdFractionOfCostPerLine.multiply(bdMUToProportionAmongSelectedLines);
				
				//Now we add that amount of mark up to get to our final price for this line:
				BigDecimal bdExtendedOrderPricePerLine = new BigDecimal(0.00);
				bdExtendedOrderPricePerLine = bdExtendedEstimatedLineCost.add(bdAmountToMarkUpLine);
				BigDecimal bdUnitPricePerLine = new BigDecimal(0.00);
				bdUnitPricePerLine = bdExtendedOrderPricePerLine.divide(bdQtyOrdered, 2, RoundingMode.HALF_UP);
				
				m_arrDetails.get(i).setM_dExtendedOrderPrice(clsManageBigDecimals.BigDecimalToScaledFormattedString(
					SMTableorderdetails.dExtendedOrderPriceScale, bdExtendedOrderPricePerLine));
				m_arrDetails.get(i).setM_dOrderUnitPrice(clsManageBigDecimals.BigDecimalToScaledFormattedString(
						SMTableorderdetails.dOrderUnitPriceScale, bdUnitPricePerLine));
			}
		}

		try {
			recalculateEntireOrder(conn, false, false);
		} catch (Exception e2) {
			throw new SQLException("Error recalculating order - " + e2.getMessage());
		}
		
		if (!clsDatabaseFunctions.start_data_transaction(conn)){
			throw new SQLException("Error starting data transaction");
		}

		//Update the record:
		try {
			updateHeader(conn, sUserID, sUserFullName, "REPRICEQUOTE");
		} catch (Exception e1) {
			clsDatabaseFunctions.rollback_data_transaction(conn);
			throw new SQLException(e1.getMessage());
		}

		//Now actually save the lines:
		for (int i = 0; i < m_arrDetails.size(); i++){
			try {
				if (bDebugMode){
					System.out.println("In " + this.toString() + ".repriceQuote - saving detail with line number = " 
							+ m_arrDetails.get(i).getM_iLineNumber());
				}
				m_arrDetails.get(i).save_line(conn);
			} catch (Exception e) {
				clsDatabaseFunctions.rollback_data_transaction(conn);
				throw new SQLException("Line number " + m_arrDetails.get(i).getM_iLineNumber() 
						+ " for item '" + m_arrDetails.get(i).getM_sItemNumber() + "' - " 
						+ e.getMessage());
			}
		}
		//Check the 'LASTEDIT' in the stored record against this one with a NEW CONNECTION:
		try {
			checkConcurrency(sDBIB, context);
		} catch (Exception e1) {
			clsDatabaseFunctions.rollback_data_transaction(conn);
			throw new SQLException(e1.getMessage());
		}
		if (!clsDatabaseFunctions.commit_data_transaction(conn)){
			clsDatabaseFunctions.rollback_data_transaction(conn);
			throw new SQLException("Error committing data transaction");
		}
	}
	
	public String read_out_debug_data(){
		String sResult = "  ** SMOrderHeader read out: ";
		sResult += "\nID: " + getM_siID();
		sResult += "\nOrder Number: " + getM_sOrderNumber();
		sResult += "\nService type code: " + getM_sServiceTypeCode();
		sResult += "\nBill To Name: " + getM_sBillToName();
		sResult += "\nShip To Name: " + getM_sShipToName();
		sResult += "\nDirections: " + getM_sDirections();
		sResult += "\nTicket comments: " + getM_sTicketComments();
		sResult += "\nTruck days: " + getM_bdtruckdays();		
		sResult += "\nDeposit amount: " + getM_bddepositamount();		
		sResult += "\nCarpenter rate: " + getM_scarpenterrate();
		sResult += "\nLaborer rate: " + getM_slaborerrate();
		sResult += "\nElectrician rate: " + getM_selectricianrate();		
		sResult += "\nTotal markup: " + getM_bdtotalmarkup();		
		sResult += "\nTotal contract amount: " + getM_bdtotalcontractamount();
		sResult += "\nWarranty expiration: " + getM_datwarrantyexpiration();
		sResult += "\nWage scale notes: " + getM_swagescalenotes();		
		sResult += "\nBill to phone: " + getM_sBilltoPhone();		
		sResult += "\nSecondary bill to phone: " + getM_ssecondarybilltophone();
		sResult += "\nSecondary ship to phone: " + getM_ssecondaryshiptophone();
		sResult += "\nShip to phone: " + getM_sShiptoPhone();
		sResult += "\nBill to phone: " + getM_sBilltoPhone();
		sResult += "\nBill to contact: " + getM_sBilltoContact();
		sResult += "\nShip to contact: " + getM_sShiptoContact();
		sResult += "\nTrimmed order number: " + getM_strimmedordernumber();
		return sResult;
	}

	public void addErrorMessage(String sMsg){
		super.addErrorMessage(sMsg);
	}

	public String getFieldInfoQueryString(){
		String sQueryString = "";
		sQueryString += "&" + ParamObjectName + "=" + clsServletUtilities.URLEncode(this.getObjectName());
		sQueryString += "&" + ParamsiID + "=" + clsServletUtilities.URLEncode(getM_siID());
		sQueryString += "&" + ParamsOrderNumber + "=" + clsServletUtilities.URLEncode(getM_sOrderNumber());
		sQueryString += "&" + ParamsBillToName + "=" + clsServletUtilities.URLEncode(getM_sBillToName());
		sQueryString += "&" + ParamsShipToName + "=" + clsServletUtilities.URLEncode(getM_sShipToName());
		sQueryString += "&" + ParamsServiceTypeCode + "=" + clsServletUtilities.URLEncode(getM_sServiceTypeCode());
		sQueryString += "&" + ParammDirections + "=" + clsServletUtilities.URLEncode(getM_sDirections());
		sQueryString += "&" + ParammTicketComments + "=" + clsServletUtilities.URLEncode(getM_sTicketComments());
		return sQueryString;
	}
	private int calculateNextDetailNumber() throws Exception{
		if (m_arrDetails.size() == 0){
			throw new Exception ("Cannot calculate next detail number because details are not loaded.");
		}
		int iHighestDetailNumber = 0;
		for (int i = 0; i < m_arrDetails.size(); i++){
			int iTestInt = Integer.parseInt(m_arrDetails.get(i).getM_iDetailNumber());
			if (iTestInt > iHighestDetailNumber){
				iHighestDetailNumber = iTestInt;
			}
		}
		return iHighestDetailNumber + 1;
	}
	private BigDecimal taxableTotalAfterDiscount(Connection conn) throws Exception{
		if (m_arrDetails.size() == 0){
			throw new Exception ("Cannot calculate taxable total after discount because details are not loaded.");
		}
	    
		SMSalesOrderTaxCalculator sotc = new SMSalesOrderTaxCalculator(
			salesTaxRate(conn), new BigDecimal(getM_dPrePostingInvoiceDiscountAmount().replace(",",""))); 
		for (int i = 0; i < m_arrDetails.size(); i++){
			sotc.addLine(
				new BigDecimal(m_arrDetails.get(i).getM_dExtendedOrderPrice().replace(",","")), 
				Integer.parseInt(m_arrDetails.get(i).getM_iTaxable()),
				new BigDecimal(m_arrDetails.get(i).getM_dQtyShipped().replace(",","")),
				m_arrDetails.get(i).getM_sItemNumber()
			);
		}
		sotc.calculateSalesTax();
		return sotc.getTotalSalesTaxBase();
	}
	public void updateLinePrice(SMOrderDetail line, String sDBIB, String sUser, ServletContext context) throws Exception{
		Connection conn = null;
		try {
			conn = clsDatabaseFunctions.getConnectionWithException(
					context, 
					sDBIB, 
					"MySQL", 
					SMUtilities.getFullClassName(this.toString()) + ".updateLinePrice - user: " + sUser
			);
		} catch (Exception e) {
			clsDatabaseFunctions.freeConnection(context, conn, "[1547067747]");
			throw new Exception("Error [1397655970] getting connection to update line price - " + e.getMessage());
		}
		try {
			updateLinePrice(line, conn);
		} catch (Exception e) {
			clsDatabaseFunctions.freeConnection(context, conn, "[1547067748]");
			throw new Exception("Error [1397655971] updating line price - " + e.getMessage());
		}
		clsDatabaseFunctions.freeConnection(context, conn, "[1547067749]");
		
	}
	public void updateLinePrice(
    		SMOrderDetail line,
    		Connection conn) throws SQLException{

		String sPriceLevelField = "";
		if (bDebugMode){
			System.out.println("In " + this.toString() + ".updateLinePrice - getM_iCustomerDiscountLevel() =  " 
					+ getM_iCustomerDiscountLevel());
			System.out.println("In " + this.toString() + ".updateLinePrice - Integer.parseInt(getM_iCustomerDiscountLevel()) =  " 
					+ Integer.parseInt(getM_iCustomerDiscountLevel()));
		}
		switch (Integer.parseInt(getM_iCustomerDiscountLevel())){
		case  0:
			sPriceLevelField = SMTableicitemprices.bdBasePrice;
			break;
		case  1:
			sPriceLevelField = SMTableicitemprices.bdLevel1Price;
			break;
		case  2:
			sPriceLevelField = SMTableicitemprices.bdLevel2Price;
			break;
		case  3:
			sPriceLevelField = SMTableicitemprices.bdLevel3Price;
			break;
		case  4:
			sPriceLevelField = SMTableicitemprices.bdLevel4Price;
			break;
		case  5:
			sPriceLevelField = SMTableicitemprices.bdLevel5Price;
			break;
		default:
			sPriceLevelField = SMTableicitemprices.bdBasePrice;
			break;
		}
		String SQL = "SELECT"
			+ " " + SMTableicitemprices.TableName + "." + SMTableicitemprices.bdBasePrice
			+ ", " + SMTableicitemprices.TableName + "." + SMTableicitemprices.bdLevel1Price
			+ ", " + SMTableicitemprices.TableName + "." + SMTableicitemprices.bdLevel2Price
			+ ", " + SMTableicitemprices.TableName + "." + SMTableicitemprices.bdLevel3Price
			+ ", " + SMTableicitemprices.TableName + "." + SMTableicitemprices.bdLevel4Price
			+ ", " + SMTableicitemprices.TableName + "." + SMTableicitemprices.bdLevel5Price
			+ " FROM " + SMTableicitemprices.TableName
		+ " WHERE ("
			+ "(" + SMTableicitemprices.TableName + "." + SMTableicitemprices.sItemNumber + " = '" 
				+ line.getM_sItemNumber() + "')"
			+ " AND (" + SMTableicitemprices.TableName + "." + SMTableicitemprices.sPriceListCode 
			+ " = '" + getM_sDefaultPriceListCode() + "')"
		+ ")"
		;
		if (bDebugMode){
			System.out.println("[1332265964]:" + clsDateAndTimeConversions.now("yyyyMMdd HH:mm:ss:SSS") + SQL);
		}
		if (bDebugMode){
			System.out.println("In " + this.toString() + ".updateLinePrice - SQL =  " + SQL);
		}
		try {
			ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
			if (rs.next()){
				line.setM_dOrderUnitPrice(clsManageBigDecimals.BigDecimalToScaledFormattedString(
					SMTableorderdetails.dOrderUnitPriceScale,
					rs.getBigDecimal(SMTableicitemprices.TableName + "." + sPriceLevelField)));
				if (bDebugMode){
					System.out.println("In " + this.toString() + ".updateLinePrice sPriceLevelField =  " 
							+ sPriceLevelField);
					System.out.println("In " + this.toString() + ".updateLinePrice line.getorderunitprice =  " 
						+ line.getM_dOrderUnitPrice());
				}
				rs.close();
			}else{
				line.setM_dOrderUnitPrice("0.00");
				if (bDebugMode){
					System.out.println("In " + this.toString() + ".updateLinePrice line.getorderunitprice =  " 
						+ "0.00");
				}
				rs.close();
			}
		} catch (SQLException e) {
			throw new SQLException("Error reading item price data to add new line with SQL: " 
					+ SQL + " - " + e.getMessage() + ".");
		}
		
		try {
			calculateExtendedPrice(line);
		} catch (Exception e) {
			throw new SQLException(e.getMessage() + ".");
		}
    }
	public void calculateExtendedPrice(SMOrderDetail line) throws Exception{
		//TODO - make sure that this ONLY happens in these cases:
		//Three events should trigger this:
		// 1 - change of unit price
        // 2 - change of qtyshipped
        // 3 - new item selection
		try {
			line.setM_dExtendedOrderPrice(
					clsManageBigDecimals.BigDecimalToScaledFormattedString(
						SMTableorderdetails.dExtendedOrderPriceScale, 
						new BigDecimal(line.getM_dQtyShipped().replace(",", "")).multiply(
							new BigDecimal(line.getM_dOrderUnitPrice().replace(",", ""))))
			);
		} catch (Exception e) {
			throw new Exception("Error [1388784331] calculating extended price in SMOrderHeader - " + e.getMessage());
		}
	}
	public boolean addAndSaveOrderDetailFromPO(SMOrderDetail detail, String sUserID, String sUserFullName, Connection conn){
		//This function adds a NEW order detail to the bottom of an order,
		//updates ONLY the affected fields in the order header,
		//and finally saves the new order detail line.
		//It HAS to be called after loading the order header first.
		
		if (getM_siID().compareToIgnoreCase("-1") == 0){
			super.addErrorMessage("Order must be loaded to save detail to it.");
			return false;
		}
		
		if (!loadDetailLines(conn)){
			return false;
		}
		
		try {
			updateLineWithItemData(
				detail,
				detail.getM_sItemNumber(), 
				getM_sDefaultPriceListCode(), 
				getM_iCustomerDiscountLevel(),
				true,
				conn
			);
		} catch (SQLException e3) {
			super.addErrorMessage("Error saving item to order - " + e3.getMessage());
			return false;
		}
		
		detail.setM_dUniqueOrderID(getM_siID());
		detail.setM_sLocationCode(getM_sLocation());
		detail.setM_sItemCategory(getM_sDefaultItemCategory());
		detail.setM_strimmedordernumber(getM_strimmedordernumber());
		
        //The 'Line Booked Date' defaults to the order date:
		detail.setM_datLineBookedDate(getM_datOrderDate());
		detail.setM_datDetailExpectedShipDate(getM_datExpectedShipDate());
		
		//Calculate and set the detail number for the line:
		try {
			setM_iNextDetailNumber(Integer.toString(calculateNextDetailNumber()));
		} catch (Exception e1) {
			super.addErrorMessage(e1.getMessage());
			return false;
		}
		detail.setM_iDetailNumber(getM_iNextDetailNumber());

		//Set the line number for the line:
		detail.setM_iLineNumber(Integer.toString(m_arrDetails.size() + 1));
	    if (!detail.validate_line(conn)){
	    	super.addErrorMessage(detail.getErrorMessages());
	    	return false;
	    }
		
	    //Set the next detail number on the order using the detail number of this line:
		setM_iNextDetailNumber(Integer.toString(Integer.parseInt(detail.getM_iDetailNumber()) + 1));
		setM_iNumberOfLinesOnOrder(Integer.toString(m_arrDetails.size() + 1));
		try {
			calculateDiscountAmount();
		} catch (Exception e4) {
			super.addErrorMessage("Could not add line - " + e4.getMessage());
			return false;
		}
		try {
			setM_dTotalAmountItems(clsManageBigDecimals.BigDecimalToScaledFormattedString(
				SMTableorderheaders.bdtotalamountitemsScale, totalAmountOfItems()));
		} catch (Exception e3) {
			super.addErrorMessage("Could not add line - " + e3.getMessage());
			return false;
		}
		//calculate tax amount
		try {
			setsordersalestaxamount(clsManageBigDecimals.BigDecimalToScaledFormattedString(
					SMTableorderheaders.bdTaxBaseScale, calculateSalesTaxAmount(conn)));
		} catch (Exception e2) {
			super.addErrorMessage("Could not add line - " + e2.getMessage());
			return false;
		}
		
		try {
			setM_dTaxBase(clsManageBigDecimals.BigDecimalToScaledFormattedString(
				SMTableorderheaders.bdTaxBaseScale, taxableTotalAfterDiscount(conn)));
		} catch (Exception e1) {
			super.addErrorMessage("Could not add line - " + e1.getMessage());
			return false;
		}
		
		if (!clsDatabaseFunctions.start_data_transaction(conn)){
			super.addErrorMessage("Could not start data transaction.");
			return false;
		}

		//First, update this order:
		String SQL = "UPDATE"
			+ " " + SMTableorderheaders.TableName
			+ " SET " + SMTableorderheaders.iNextDetailNumber + " = " + getM_iNextDetailNumber()
			+ ", " + SMTableorderheaders.iNumberOfLinesOnOrder + " = " + getM_iNumberOfLinesOnOrder()
			+ ", " + SMTableorderheaders.dPrePostingInvoiceDiscountAmount + " = " + getM_dPrePostingInvoiceDiscountAmount().replace(",", "")
			+ ", " + SMTableorderheaders.bdordertaxamount + " = " + getsordersalestaxamount().replace(",", "")
			+ ", " + SMTableorderheaders.dTotalAmountItems + " = " + getM_dTotalAmountItems().replace(",", "")
			+ ", " + SMTableorderheaders.bdtaxbase + " = " + getM_dTaxBase().replace(",", "")
			+ ", " + SMTableorderheaders.LASTEDITDATE + " = DATE_FORMAT(NOW(), '%Y%m%d')" //LAST EDIT DATE
			+ ", " + SMTableorderheaders.LASTEDITPROCESS + " = 'ADDED ITEM TO ORDER FROM IC'" //LAST EDIT PROCESS
			+ ", " + SMTableorderheaders.LASTEDITTIME + " = DATE_FORMAT(NOW(), '%k%i%s')" // LAST EDIT TIME
			+ ", " + SMTableorderheaders.LASTEDITUSERID + " = " + sUserID + ""
			+ ", " + SMTableorderheaders.LASTEDITUSERFULLNAME+ " = '" + clsDatabaseFunctions.FormatSQLStatement(sUserFullName) + "'"
			+ " WHERE ("
				+ SMTableorderheaders.dOrderUniqueifier + " = " + getM_siID()
			+ ")"
		;
		
		try {
			Statement stmt = conn.createStatement();
			stmt.execute(SQL);
		} catch (SQLException e) {
			super.addErrorMessage("Error updating header with SQL: " + SQL + " - " + e.getMessage());
			clsDatabaseFunctions.rollback_data_transaction(conn);
			return false;
		}
		
		//Next save the line:
		try {
			detail.save_line(conn);
		} catch (SQLException e) {
			super.addErrorMessage("Could not save detail line - " + detail.getErrorMessages());
			clsDatabaseFunctions.rollback_data_transaction(conn);
			return false;
		}
		if (!clsDatabaseFunctions.commit_data_transaction(conn)){
			clsDatabaseFunctions.rollback_data_transaction(conn);
			super.addErrorMessage("Could not commit data transaction.");
			return false;
		}
		
		return true;
	}
	public void addMultipleDetails(ArrayList <SMOrderDetail> arrDetails, Connection conn, String UserID, String sUserFullName) throws Exception{
		
		//NOTE: the order must be loaded before this function can be used.
		//if (!loadDetailLines(conn)){
		//	throw new Exception(this.getErrorMessages());
		//}
		int iNextDetailNumber = 1;
		for (int j = 0; j < m_arrDetails.size(); j++){
			if (Integer.parseInt(m_arrDetails.get(j).getM_iDetailNumber()) >= iNextDetailNumber){
				iNextDetailNumber = Integer.parseInt(m_arrDetails.get(j).getM_iDetailNumber()) + 1;
			}
		}
		
		for (int i = 0; i < arrDetails.size(); i++){
			SMOrderDetail line = new SMOrderDetail();
			line = arrDetails.get(i);
			line.setM_dUniqueOrderID(getM_siID());
			line.setM_strimmedordernumber(getM_strimmedordernumber());
			line.setM_iLineNumber(Integer.toString(m_arrDetails.size() + 1));
			line.setM_iDetailNumber(Integer.toString(iNextDetailNumber));
			m_arrDetails.add(line);
			try {
				line.save_line(conn);
			} catch (SQLException e) {
				throw new Exception("Could not save detail line - " + arrDetails.get(i).getErrorMessages());
			}
			iNextDetailNumber++;
		}
	    //Set the next detail number on the order:
		setM_iNextDetailNumber(Integer.toString(iNextDetailNumber + 1));
		setM_iNumberOfLinesOnOrder(Integer.toString(m_arrDetails.size() + 1));
		
		try {
			calculateDiscountAmount();
		} catch (Exception e4) {
			throw new Exception("Error calculating discount amt - " + e4.getMessage());
		}
		try {
			setM_dTotalAmountItems(clsManageBigDecimals.BigDecimalToScaledFormattedString(
				SMTableorderheaders.bdtotalamountitemsScale, totalAmountOfItems()));
		} catch (Exception e3) {
			throw new Exception("Error setting total items amt - " + e3.getMessage());
		}
		//calculate tax amount
		try {
			setsordersalestaxamount(clsManageBigDecimals.BigDecimalToScaledFormattedString(
					SMTableorderheaders.bdTaxBaseScale, calculateSalesTaxAmount(conn)));
		} catch (Exception e2) {
			throw new Exception("Error setting order tax amt - " + e2.getMessage());
		}
		try {
			setM_dTaxBase(clsManageBigDecimals.BigDecimalToScaledFormattedString(
				SMTableorderheaders.bdTaxBaseScale, taxableTotalAfterDiscount(conn)));
		} catch (Exception e1) {
			throw new Exception("Error setting tax base - " + e1.getMessage());
		}
		//Update this order:
		String SQL = "UPDATE"
			+ " " + SMTableorderheaders.TableName
			+ " SET " + SMTableorderheaders.iNextDetailNumber + " = " + getM_iNextDetailNumber()
			+ ", " + SMTableorderheaders.iNumberOfLinesOnOrder + " = " + getM_iNumberOfLinesOnOrder()
			+ ", " + SMTableorderheaders.dPrePostingInvoiceDiscountAmount + " = " + getM_dPrePostingInvoiceDiscountAmount().replace(",", "")
			+ ", " + SMTableorderheaders.bdordertaxamount + " = " + getsordersalestaxamount().replace(",", "")
			+ ", " + SMTableorderheaders.dTotalAmountItems + " = " + getM_dTotalAmountItems().replace(",", "")
			+ ", " + SMTableorderheaders.bdtaxbase + " = " + getM_dTaxBase().replace(",", "")
			+ ", " + SMTableorderheaders.LASTEDITDATE + " = DATE_FORMAT(NOW(), '%Y%m%d')" //LAST EDIT DATE
			+ ", " + SMTableorderheaders.LASTEDITPROCESS + " = 'ADDED MULTIPLE ITEMS'" //LAST EDIT PROCESS
			+ ", " + SMTableorderheaders.LASTEDITTIME + " = DATE_FORMAT(NOW(), '%k%i%s')" // LAST EDIT TIME
			+ ", " + SMTableorderheaders.LASTEDITUSERID + " = " + UserID + ""
			+ ", " + SMTableorderheaders.LASTEDITUSERFULLNAME + " = '" + clsDatabaseFunctions.FormatSQLStatement(sUserFullName) + "'"
			+ " WHERE ("
				+ SMTableorderheaders.dOrderUniqueifier + " = " + getM_siID()
			+ ")"
		;

		try {
			Statement stmt = conn.createStatement();
			stmt.execute(SQL);
		} catch (SQLException e) {
			throw new Exception("Error updating header after adding multiple details with SQL: " + SQL + " - " + e.getMessage());
		}
	}
	public void addNewDetail(SMOrderDetail detail){
		m_arrDetails.add(detail);
	}

	public boolean addNewDetail(SMOrderDetail detail, String sUserID, String sUserFullName, Connection conn, String sInsertLineAboveDetailNumber){
		//This function adds a NEW order detail ,
		//updates ONLY the affected fields in the order header,
		//and finally saves the new order detail line.
		//It HAS to be called after loading the order header first.
		
		if (getM_siID().compareToIgnoreCase("-1") == 0){
			super.addErrorMessage("Order must be loaded to save new detail to it.");
			return false;
		}
		
		if (!loadDetailLines(conn)){
			return false;
		}
		
		detail.setM_dUniqueOrderID(getM_siID());
		detail.setM_strimmedordernumber(getM_strimmedordernumber());
		
		//Calculate and set the detail number for the line:
		try {
			setM_iNextDetailNumber(Integer.toString(calculateNextDetailNumber()));
		} catch (Exception e1) {
			super.addErrorMessage(e1.getMessage());
			return false;
		}
		detail.setM_iDetailNumber(getM_iNextDetailNumber());

		//First get the line number of the line we want to insert above:
		String sLineNumberToInsertAbove = "";
		for (int i = 0; i < m_arrDetails.size(); i++){
			if (m_arrDetails.get(i).getM_iDetailNumber().compareToIgnoreCase(sInsertLineAboveDetailNumber) == 0){
				sLineNumberToInsertAbove = m_arrDetails.get(i).getM_iLineNumber();
			}
		}
		
		if (sLineNumberToInsertAbove.trim().compareToIgnoreCase("") != 0){
			//In this case, we need to re-order all the lines:
			detail.setM_iLineNumber(sLineNumberToInsertAbove);
		}else{
			//If we are not INSERTING this line before another, then just add it to the end:
			detail.setM_iLineNumber(Integer.toString(m_arrDetails.size() + 1));
		}
	    //Set the next detail number on the order using the detail number of this line:
		setM_iNextDetailNumber(Integer.toString(Integer.parseInt(detail.getM_iDetailNumber()) + 1));
		setM_iNumberOfLinesOnOrder(Integer.toString(m_arrDetails.size() + 1));
		//Now we need to ADD this detail to this order class, so we can accurately calculate the total amount, etc.:
		m_arrDetails.add(detail);
		
		try {
			calculateDiscountAmount();
		} catch (Exception e4) {
			super.addErrorMessage("Could not add new detail - " + e4.getMessage());
			return false;
		}
		try {
			setM_dTotalAmountItems(clsManageBigDecimals.BigDecimalToScaledFormattedString(
				SMTableorderheaders.bdtotalamountitemsScale, totalAmountOfItems()));
		} catch (Exception e3) {
			super.addErrorMessage("Could not add new detail - " + e3.getMessage());
			return false;
		}
		//calculate tax amount
		try {
			setsordersalestaxamount(clsManageBigDecimals.BigDecimalToScaledFormattedString(
					SMTableorderheaders.bdTaxBaseScale, calculateSalesTaxAmount(conn)));
		} catch (Exception e2) {
			super.addErrorMessage("Could not add new detail - " + e2.getMessage());
			return false;
		}
		try {
			setM_dTaxBase(clsManageBigDecimals.BigDecimalToScaledFormattedString(
				SMTableorderheaders.bdTaxBaseScale, taxableTotalAfterDiscount(conn)));
		} catch (Exception e1) {
			super.addErrorMessage("Could not add new detail - " + e1.getMessage());
			return false;
		}
		if (!clsDatabaseFunctions.start_data_transaction(conn)){
			super.addErrorMessage("Could not start data transaction to add new detail.");
			return false;
		}

		//First, update this order:
		String SQL = "UPDATE"
			+ " " + SMTableorderheaders.TableName
			+ " SET " + SMTableorderheaders.iNextDetailNumber + " = " + getM_iNextDetailNumber()
			+ ", " + SMTableorderheaders.iNumberOfLinesOnOrder + " = " + getM_iNumberOfLinesOnOrder()
			+ ", " + SMTableorderheaders.dPrePostingInvoiceDiscountAmount + " = " + getM_dPrePostingInvoiceDiscountAmount().replace(",", "")
			+ ", " + SMTableorderheaders.bdordertaxamount + " = " + getsordersalestaxamount().replace(",", "")
			+ ", " + SMTableorderheaders.dTotalAmountItems + " = " + getM_dTotalAmountItems().replace(",", "")
			+ ", " + SMTableorderheaders.bdtaxbase + " = " + getM_dTaxBase().replace(",", "")
			+ ", " + SMTableorderheaders.LASTEDITDATE + " = DATE_FORMAT(NOW(), '%Y%m%d')" //LAST EDIT DATE
			+ ", " + SMTableorderheaders.LASTEDITPROCESS + " = 'ADDED ITEM TO ORDER FROM IC'" //LAST EDIT PROCESS
			+ ", " + SMTableorderheaders.LASTEDITTIME + " = DATE_FORMAT(NOW(), '%k%i%s')" // LAST EDIT TIME
			+ ", " + SMTableorderheaders.LASTEDITUSERID + " = " + sUserID 
			+ ", " + SMTableorderheaders.LASTEDITUSERFULLNAME + " = '" + clsDatabaseFunctions.FormatSQLStatement(sUserFullName) + "'"
			+ " WHERE ("
				+ SMTableorderheaders.dOrderUniqueifier + " = " + getM_siID()
			+ ")"
		;
		if (bDebugMode){
			System.out.println("[1334177346]:" + clsDateAndTimeConversions.now("yyyyMMdd HH:mm:ss:SSS") + SQL);
		}
		try {
			Statement stmt = conn.createStatement();
			stmt.execute(SQL);
		} catch (SQLException e) {
			super.addErrorMessage("Error updating header after adding new detail with SQL: " + SQL + " - " + e.getMessage());
			clsDatabaseFunctions.rollback_data_transaction(conn);
			return false;
		}
		
		//If we are inserting a line, renumber all the subsequent lines now:
		if (sLineNumberToInsertAbove.trim().compareToIgnoreCase("") != 0){
			SQL = "UPDATE " + SMTableorderdetails.TableName
				+ " SET " + SMTableorderdetails.iLineNumber + " = " + SMTableorderdetails.iLineNumber + " + 1"
				+ " WHERE ("
					+ "(" + SMTableorderdetails.strimmedordernumber + " = '" + this.getM_strimmedordernumber() + "')"
					+ " AND (" + SMTableorderdetails.iLineNumber + " >= " + sLineNumberToInsertAbove + ")"
				+ ")"
				;
			if (bDebugMode){
				System.out.println("[1334177346]:" + clsDateAndTimeConversions.now("yyyyMMdd HH:mm:ss:SSS") + SQL);
			}
			try {
				Statement stmt = conn.createStatement();
				stmt.execute(SQL);
			} catch (SQLException e) {
				super.addErrorMessage("Error renumbering line numbers to insert new detail with SQL: " + SQL + " - " + e.getMessage());
				clsDatabaseFunctions.rollback_data_transaction(conn);
				return false;
			}
		}
		//Next save the line:
		try {
			detail.save_line(conn);
		} catch (SQLException e) {
			super.addErrorMessage("Could not save detail line - " + detail.getErrorMessages());
			clsDatabaseFunctions.rollback_data_transaction(conn);
			return false;
		}
		if (!clsDatabaseFunctions.commit_data_transaction(conn)){
			clsDatabaseFunctions.rollback_data_transaction(conn);
			super.addErrorMessage("Could not commit data transaction.");
			return false;
		}
		
		return true;
	}
	public void addNewDetailLine_wo_transaction(SMOrderDetail detail, String sUserID, String sUserFullName, Connection conn) throws Exception{
		//This function adds a NEW order detail without starting a data transaction,
		//updates ONLY the affected fields in the order header,
		//and finally saves the new order detail line.
		//It HAS to be called after loading the order header first.
		
		if (getM_siID().compareToIgnoreCase("-1") == 0){
			throw new Exception("Order must be loaded to save new detail to it.");
		}
		
		if (!loadDetailLines(conn)){
			throw new Exception(getErrorMessages());
		}
		
		detail.setM_dUniqueOrderID(getM_siID());
		detail.setM_strimmedordernumber(getM_strimmedordernumber());
		
		//Calculate and set the detail number for the line:
		try {
			setM_iNextDetailNumber(Integer.toString(calculateNextDetailNumber()));
		} catch (Exception e1) {
			throw new Exception(getErrorMessages());
		}
		detail.setM_iDetailNumber(getM_iNextDetailNumber());
		detail.setM_iLineNumber(Integer.toString(m_arrDetails.size() + 1));
	    //Set the next detail number on the order using the detail number of this line:
		setM_iNextDetailNumber(Integer.toString(Integer.parseInt(detail.getM_iDetailNumber()) + 1));
		setM_iNumberOfLinesOnOrder(Integer.toString(m_arrDetails.size() + 1));
		//Now we need to ADD this detail to this order class, so we can accurately calculate the total amount, etc.:
		m_arrDetails.add(detail);
		
		try {
			calculateDiscountAmount();
		} catch (Exception e4) {
			throw new Exception("Error [1395426667] calculating discount amount - " + e4.getMessage());
		}
		try {
			setM_dTotalAmountItems(clsManageBigDecimals.BigDecimalToScaledFormattedString(
				SMTableorderheaders.bdtotalamountitemsScale, totalAmountOfItems()));
		} catch (Exception e3) {
			throw new Exception("Error [1395426668] Could not add new detail - " + e3.getMessage());
		}
		//calculate tax amount
		try {
			setsordersalestaxamount(clsManageBigDecimals.BigDecimalToScaledFormattedString(
					SMTableorderheaders.bdTaxBaseScale, calculateSalesTaxAmount(conn)));
		} catch (Exception e2) {
			throw new Exception("Error [1395426669] Could not add new detail - " + e2.getMessage());
		}
		try {
			setM_dTaxBase(clsManageBigDecimals.BigDecimalToScaledFormattedString(
				SMTableorderheaders.bdTaxBaseScale, taxableTotalAfterDiscount(conn)));
		} catch (Exception e1) {
			throw new Exception("Error [1395426670] Could not add new detail - " + e1.getMessage());
		}

		//First, update this order:
		String SQL = "UPDATE"
			+ " " + SMTableorderheaders.TableName
			+ " SET " + SMTableorderheaders.iNextDetailNumber + " = " + getM_iNextDetailNumber()
			+ ", " + SMTableorderheaders.iNumberOfLinesOnOrder + " = " + getM_iNumberOfLinesOnOrder()
			+ ", " + SMTableorderheaders.dPrePostingInvoiceDiscountAmount + " = " + getM_dPrePostingInvoiceDiscountAmount().replace(",", "")
			+ ", " + SMTableorderheaders.bdordertaxamount + " = " + getsordersalestaxamount().replace(",", "")
			+ ", " + SMTableorderheaders.dTotalAmountItems + " = " + getM_dTotalAmountItems().replace(",", "")
			+ ", " + SMTableorderheaders.bdtaxbase + " = " + getM_dTaxBase().replace(",", "")
			+ ", " + SMTableorderheaders.LASTEDITDATE + " = DATE_FORMAT(NOW(), '%Y%m%d')" //LAST EDIT DATE
			+ ", " + SMTableorderheaders.LASTEDITPROCESS + " = 'IMPORTED LINE TO ORDER FROM WO'" //LAST EDIT PROCESS
			+ ", " + SMTableorderheaders.LASTEDITTIME + " = DATE_FORMAT(NOW(), '%k%i%s')" // LAST EDIT TIME
			+ ", " + SMTableorderheaders.LASTEDITUSERID + " = " + sUserID
			+ ", " + SMTableorderheaders.LASTEDITUSERFULLNAME + " = '" + clsDatabaseFunctions.FormatSQLStatement(sUserFullName) + "'"
			+ " WHERE ("
				+ SMTableorderheaders.dOrderUniqueifier + " = " + getM_siID()
			+ ")"
		;
		if (bDebugMode){
			System.out.println("[1334177346]:" + clsDateAndTimeConversions.now("yyyyMMdd HH:mm:ss:SSS") + SQL);
		}
		try {
			Statement stmt = conn.createStatement();
			stmt.execute(SQL);
		} catch (SQLException e) {
			throw new Exception("Error [1395426671] updating header after adding new detail with SQL: " + SQL + " - " + e.getMessage());
		}
		
		//Next save the line:
		try {
			detail.save_line(conn);
		} catch (SQLException e) {
			throw new Exception(" Error [1395426671] could not save detail line - " + detail.getErrorMessages());
		}
		return;
	}
	private BigDecimal calculateSalesTaxAmount(Connection conn) throws Exception{
		SMSalesOrderTaxCalculator sotc = new SMSalesOrderTaxCalculator(
			salesTaxRate(conn), new BigDecimal(getM_dPrePostingInvoiceDiscountAmount().replace(",",""))); 
		for (int i = 0; i < m_arrDetails.size(); i++){
			sotc.addLine(
				new BigDecimal(m_arrDetails.get(i).getM_dExtendedOrderPrice().replace(",","")), 
				Integer.parseInt(m_arrDetails.get(i).getM_iTaxable()),
				new BigDecimal(m_arrDetails.get(i).getM_dQtyShipped().replace(",","")),
				m_arrDetails.get(i).getM_sItemNumber()
			);
		}
		sotc.calculateSalesTax();
		return sotc.getTotalSalesTax();

	}
	public BigDecimal salesTaxRate(Connection conn) throws SQLException{
		BigDecimal bdSalesTaxRate = new BigDecimal(0.00);
		String SQL = "SELECT"
			+ " " + SMTabletax.bdtaxrate
			+ ", " + SMTabletax.icalculateonpurchaseorsale
			+ ", " + SMTabletax.icalculatetaxoncustomerinvoice
			+ " FROM " + SMTabletax.TableName
			+ " WHERE ("
				+ "(" + SMTabletax.lid + " = " + getitaxid() + ")"
			+ ")"
			;
		if (bDebugMode){
			System.out.println("[1332266163]:" + clsDateAndTimeConversions.now("yyyyMMdd HH:mm:ss:SSS") + SQL);
		}
		try {
			ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
			if (rs.next()){
				if ((rs.getInt(SMTabletax.icalculateonpurchaseorsale) == SMTabletax.TAX_CALCULATION_BASED_ON_SELLING_PRICE)
						&& (rs.getInt(SMTabletax.icalculatetaxoncustomerinvoice) == 1)
				){
					bdSalesTaxRate = rs.getBigDecimal(SMTabletax.bdtaxrate);
				}
				rs.close();
			}else{
				rs.close();
				throw new SQLException("No tax rate available for tax jurisdiction '" + getstaxjurisdiction()
					+ "' and tax type '" + getstaxtype() + "'.");
			}
		} catch (Exception e) {
			throw new SQLException("Error [1454608171] reading tax rate with SQL: " + SQL + " - " + e.getMessage() + ".");
		}
		return bdSalesTaxRate;
	}
	private void calculateDiscountAmount() throws Exception{
    	BigDecimal bdDiscountPercentAsFactor = 
    		new BigDecimal(getM_dPrePostingInvoiceDiscountPercentage()).divide(new BigDecimal("100.00"));
    	try {
			setM_dPrePostingInvoiceDiscountAmount(
				clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(totalAmountOfItems().multiply(bdDiscountPercentAsFactor)));
		} catch (Exception e) {
			throw new Exception("Error calculating discount amount - " + e.getMessage());
		}
	}
    
	public String getM_siID() {
		return m_siID;
	}

	public void setM_siID(String mSiID) {
		m_siID = mSiID;
	}
	public String getM_sOrderNumber() {
		return m_sOrderNumber;
	}
	public void setM_sOrderNumber(String sOrderNumber) {
		m_sOrderNumber = sOrderNumber;
	}
	public String getM_sBillToName() {
		return m_sBillToName;
	}
	public void setM_sBillToName(String sBillToName) {
		m_sBillToName = sBillToName;
	}
	public String getM_sShipToName() {
		return m_sShipToName;
	}
	public void setM_sShipToName(String sShipToName) {
		m_sShipToName = sShipToName;
	}
	public String getM_sDirections() {
		return m_sDirections;
	}
	public void setM_sDirections(String sDirections) {
		m_sDirections = sDirections;
	}
	public String getM_sTicketComments() {
		return m_sTicketComments;
	}
	public void setM_sTicketComments(String sTicketComments) {
		m_sTicketComments = sTicketComments;
	}
	public String getM_sFieldNotes() {
		return m_sFieldNotes;
	}
	public void setM_sFieldNotes(String sFieldNotes) {
		m_sFieldNotes = sFieldNotes;
	}

	public String getM_bdtruckdays() {
		return m_bdtruckdays;
	}

	public void setM_bdtruckdays(String mBdtruckdays) {
		m_bdtruckdays = mBdtruckdays;
	}

	public String getM_bddepositamount() {
		return m_bddepositamount;
	}

	public void setM_bddepositamount(String mBddepositamount) {
		m_bddepositamount = mBddepositamount;
	}

	public String getM_scarpenterrate() {
		return m_scarpenterrate;
	}

	public void setM_scarpenterrate(String mScarpenterrate) {
		m_scarpenterrate = mScarpenterrate;
	}

	public String getM_slaborerrate() {
		return m_slaborerrate;
	}

	public void setM_slaborerrate(String mSlaborerrate) {
		m_slaborerrate = mSlaborerrate;
	}

	public String getM_selectricianrate() {
		return m_selectricianrate;
	}

	public void setM_selectricianrate(String mSelectricianrate) {
		m_selectricianrate = mSelectricianrate;
	}

	public String getM_bdtotalmarkup() {
		return m_bdtotalmarkup;
	}

	public void setM_bdtotalmarkup(String mBdtotalmarkup) {
		m_bdtotalmarkup = mBdtotalmarkup;
	}

	public String getM_bdtotalcontractamount() {
		return m_bdtotalcontractamount;
	}

	public void setM_bdtotalcontractamount(String mBdtotalcontractamount) {
		m_bdtotalcontractamount = mBdtotalcontractamount;
	}

	public String getM_datwarrantyexpiration() {
		return m_datwarrantyexpiration;
	}

	public void setM_datwarrantyexpiration(String mDatwarrantyexpiration) {
		m_datwarrantyexpiration = mDatwarrantyexpiration;
	}

	public String getM_swagescalenotes() {
		return m_swagescalenotes;
	}

	public void setM_swagescalenotes(String mSwagescalenotes) {
		m_swagescalenotes = mSwagescalenotes;
	}

	public String getM_ssecondarybilltophone() {
		return m_ssecondarybilltophone;
	}

	public void setM_ssecondarybilltophone(String mSsecondarybilltophone) {
		m_ssecondarybilltophone = mSsecondarybilltophone;
	}

	public String getM_ssecondaryshiptophone() {
		return m_ssecondaryshiptophone;
	}

	public void setM_ssecondaryshiptophone(String mSsecondaryshiptophone) {
		m_ssecondaryshiptophone = mSsecondaryshiptophone;
	}

	public String getM_sShiptoPhone() {
		return m_sshiptophone;
	}

	public void setM_sShiptoPhone(String mSshiptophone) {
		m_sshiptophone = mSshiptophone;
	}

	public String getM_sBilltoPhone() {
		return m_sbilltophone;
	}

	public void setM_sBilltoPhone(String mSbilltophone) {
		m_sbilltophone = mSbilltophone;
	}

	public String getM_sBilltoContact() {
		return m_sbilltocontact;
	}

	public void setM_sBilltoContact(String mSbilltocontact) {
		m_sbilltocontact = mSbilltocontact;
	}

	public String getM_sShiptoContact() {
		return m_sshiptocontact;
	}

	public void setM_sShiptoContact(String mSshiptocontact) {
		m_sshiptocontact = mSshiptocontact;
	}

	public String getM_sServiceTypeCode() {
		return m_sServiceTypeCode;
	}

	public void setM_sServiceTypeCode(String sServiceTypeCode) {
		m_sServiceTypeCode = sServiceTypeCode;
	}

	public String getM_strimmedordernumber() {
		return m_strimmedordernumber;
	}

	public void setM_strimmedordernumber(String sTrimmedOrderNumber) {
		m_strimmedordernumber = sTrimmedOrderNumber;
	}
	
	public String getM_sCustomerCode() {
		return m_sCustomerCode;
	}

	public void setM_sCustomerCode(String mSCustomerCode) {
		m_sCustomerCode = mSCustomerCode;
	}

	public String getM_sBillToAddressLine1() {
		return m_sBillToAddressLine1;
	}

	public void setM_sBillToAddressLine1(String mSBillToAddressLine1) {
		m_sBillToAddressLine1 = mSBillToAddressLine1;
	}

	public String getM_sBillToAddressLine2() {
		return m_sBillToAddressLine2;
	}

	public void setM_sBillToAddressLine2(String mSBillToAddressLine2) {
		m_sBillToAddressLine2 = mSBillToAddressLine2;
	}

	public String getM_sBillToAddressLine3() {
		return m_sBillToAddressLine3;
	}

	public void setM_sBillToAddressLine3(String mSBillToAddressLine3) {
		m_sBillToAddressLine3 = mSBillToAddressLine3;
	}

	public String getM_sBillToAddressLine4() {
		return m_sBillToAddressLine4;
	}

	public void setM_sBillToAddressLine4(String mSBillToAddressLine4) {
		m_sBillToAddressLine4 = mSBillToAddressLine4;
	}

	public String getM_sBillToCity() {
		return m_sBillToCity;
	}

	public void setM_sBillToCity(String mSBillToCity) {
		m_sBillToCity = mSBillToCity;
	}

	public String getM_sBillToState() {
		return m_sBillToState;
	}

	public void setM_sBillToState(String mSBillToState) {
		m_sBillToState = mSBillToState;
	}

	public String getM_sBillToZip() {
		return m_sBillToZip;
	}

	public void setM_sBillToZip(String mSBillToZip) {
		m_sBillToZip = mSBillToZip;
	}

	public String getM_sBillToCountry() {
		return m_sBillToCountry;
	}

	public void setM_sBillToCountry(String mSBillToCountry) {
		m_sBillToCountry = mSBillToCountry;
	}

	public String getM_sBillToFax() {
		return m_sBillToFax;
	}

	public void setM_sBillToFax(String mSBillToFax) {
		m_sBillToFax = mSBillToFax;
	}

	public String getM_sShipToCode() {
		return m_sShipToCode;
	}

	public void setM_sShipToCode(String mSShipToCode) {
		m_sShipToCode = mSShipToCode;
	}

	public String getM_sShipToAddress1() {
		return m_sShipToAddress1;
	}

	public void setM_sShipToAddress1(String mSShipToAddress1) {
		m_sShipToAddress1 = mSShipToAddress1;
	}

	public String getM_sShipToAddress2() {
		return m_sShipToAddress2;
	}

	public void setM_sShipToAddress2(String mSShipToAddress2) {
		m_sShipToAddress2 = mSShipToAddress2;
	}

	public String getM_sShipToAddress3() {
		return m_sShipToAddress3;
	}

	public void setM_sShipToAddress3(String mSShipToAddress3) {
		m_sShipToAddress3 = mSShipToAddress3;
	}

	public String getM_sShipToAddress4() {
		return m_sShipToAddress4;
	}

	public void setM_sShipToAddress4(String mSShipToAddress4) {
		m_sShipToAddress4 = mSShipToAddress4;
	}

	public String getM_sShipToCity() {
		return m_sShipToCity;
	}

	public void setM_sShipToCity(String mSShipToCity) {
		m_sShipToCity = mSShipToCity;
	}

	public String getM_sShipToState() {
		return m_sShipToState;
	}

	public void setM_sShipToState(String mSShipToState) {
		m_sShipToState = mSShipToState;
	}

	public String getM_sShipToZip() {
		return m_sShipToZip;
	}

	public void setM_sShipToZip(String mSShipToZip) {
		m_sShipToZip = mSShipToZip;
	}

	public String getM_sShipToCountry() {
		return m_sShipToCountry;
	}

	public void setM_sShipToCountry(String mSShipToCountry) {
		m_sShipToCountry = mSShipToCountry;
	}

	public String getM_sShipToFax() {
		return m_sShipToFax;
	}

	public void setM_sShipToFax(String mSShipToFax) {
		m_sShipToFax = mSShipToFax;
	}

	public String getM_iCustomerDiscountLevel() {
		return m_iCustomerDiscountLevel;
	}

	public void setM_iCustomerDiscountLevel(String mICustomerDiscountLevel) {
		m_iCustomerDiscountLevel = mICustomerDiscountLevel;
	}

	public String getM_sDefaultPriceListCode() {
		return m_sDefaultPriceListCode;
	}

	public void setM_sDefaultPriceListCode(String mSDefaultPriceListCode) {
		m_sDefaultPriceListCode = mSDefaultPriceListCode;
	}

	public String getM_sPONumber() {
		return m_sPONumber;
	}

	public void setM_sPONumber(String mSPONumber) {
		m_sPONumber = mSPONumber;
	}

	public String getM_sSpecialWageRate() {
		if (m_sSpecialWageRate != null){
			return m_sSpecialWageRate;
		}else{
			return "F";
		}
	}

	public void setM_sSpecialWageRate(String mSSpecialWageRate) {
		m_sSpecialWageRate = mSSpecialWageRate;
	}

	public String getM_sTerms() {
		return m_sTerms;
	}

	public void setM_sTerms(String mSTerms) {
		m_sTerms = mSTerms;
	}

	public String getM_iOrderType() {
		return m_iOrderType;
	}

	public void setM_iOrderType(String mIOrderType) {
		m_iOrderType = mIOrderType;
	}

	public String getM_datOrderDate() {
		return m_datOrderDate;
	}

	public void setM_datOrderDate(String mDatOrderDate) {
		m_datOrderDate = mDatOrderDate;
	}

	public String getM_datExpectedShipDate() {
		return m_datExpectedShipDate;
	}

	public void setM_datExpectedShipDate(String mDatExpectedShipDate) {
		m_datExpectedShipDate = mDatExpectedShipDate;
	}

	public String getM_datOrderCreationDate() {
		return m_datOrderCreationDate;
	}

	public void setM_datOrderCreationDate(String mDatOrderCreationDate) {
		m_datOrderCreationDate = mDatOrderCreationDate;
	}

	public String getM_sServiceTypeCodeDescription() {
		return m_sServiceTypeCodeDescription;
	}

	public void setM_sServiceTypeCodeDescription(String mSServiceTypeCodeDescription) {
		m_sServiceTypeCodeDescription = mSServiceTypeCodeDescription;
	}

	public String getM_sLastInvoiceNumber() {
		return m_sLastInvoiceNumber;
	}

	public void setM_sLastInvoiceNumber(String mSLastInvoiceNumber) {
		m_sLastInvoiceNumber = mSLastInvoiceNumber;
	}

	public String getM_iNumberOfInvoices() {
		return m_iNumberOfInvoices;
	}

	public void setM_iNumberOfInvoices(String mINumberOfInvoices) {
		m_iNumberOfInvoices = mINumberOfInvoices;
	}

	public String getM_sLocation() {
		return m_sLocation;
	}

	public void setM_sLocation(String mSLocation) {
		m_sLocation = mSLocation;
	}

	public String getM_iOnHold() {
		return m_iOnHold;
	}

	public void setM_iOnHold(String mIOnHold) {
		m_iOnHold = mIOnHold;
	}

	public String getM_datLastPostingDate() {
		return m_datLastPostingDate;
	}

	public void setM_datLastPostingDate(String mDatLastPostingDate) {
		m_datLastPostingDate = mDatLastPostingDate;
	}

	public String getM_dTotalAmountItems() {
		return m_dTotalAmountItems;
	}

	public void setM_dTotalAmountItems(String mDTotalAmountItems) {
		m_dTotalAmountItems = mDTotalAmountItems;
	}

	public String getM_iNumberOfLinesOnOrder() {
		return m_iNumberOfLinesOnOrder;
	}

	public void setM_iNumberOfLinesOnOrder(String mINumberOfLinesOnOrder) {
		m_iNumberOfLinesOnOrder = mINumberOfLinesOnOrder;
	}

	public String getM_sSalesperson() {
		return m_sSalesperson;
	}

	public void setM_sSalesperson(String mSSalesperson) {
		m_sSalesperson = mSSalesperson;
	}

	public String getM_sOrderCreatedByFullName() {
		return m_sOrderCreatedByFullName;
	}

	public void setM_sOrderCreatedByFullName(String mSOrderCreatedBy) {
		m_sOrderCreatedByFullName = mSOrderCreatedBy;
	}
	
	public String getM_lOrderCreatedByID() {
		return m_lOrderCreatedByID;
	}

	public void setM_lOrderCreatedByID(String mSOrderCreatedByID) {
		m_lOrderCreatedByID = mSOrderCreatedByID;
	}

	public String getstaxjurisdiction() {
		return m_staxjurisdiction;
	}

	public void setstaxjurisdiction(String mSTaxGroup) {
		m_staxjurisdiction = mSTaxGroup;
	}

	public String getM_sDefaultItemCategory() {
		return m_sDefaultItemCategory;
	}

	public void setM_sDefaultItemCategory(String mSDefaultItemCategory) {
		m_sDefaultItemCategory = mSDefaultItemCategory;
	}

	public String getM_dTaxBase() {
		return m_dTaxBase;
	}

	public void setM_dTaxBase(String mDTaxBase) {
		m_dTaxBase = mDTaxBase;
	}

	public String getsordersalestaxamount() {
		return m_sordersalestaxamount;
	}

	public void setsordersalestaxamount(String sOrderSalesTaxAmount) {
		m_sordersalestaxamount = sOrderSalesTaxAmount;
	}

	public String getM_iNextDetailNumber() {
		return m_iNextDetailNumber;
	}

	public void setM_iNextDetailNumber(String mINextDetailNumber) {
		m_iNextDetailNumber = mINextDetailNumber;
	}

	public String getM_mInternalComments() {
		if (m_mInternalComments == null){
			return "";
		}else{
			return m_mInternalComments;
		}
	}
	public void setM_mInternalComments(String mMInternalComments) {
		if (mMInternalComments == null){
			m_mInternalComments = "";
		}else{
			m_mInternalComments = mMInternalComments;
		}
	}

	public String getM_mInvoiceComments() {
		return m_mInvoiceComments;
	}

	public void setM_mInvoiceComments(String mMInvoiceComments) {
		m_mInvoiceComments = mMInvoiceComments;
	}

	public String getM_dPrePostingInvoiceDiscountPercentage() {
		return m_dPrePostingInvoiceDiscountPercentage;
	}

	public void setM_dPrePostingInvoiceDiscountPercentage(
			String mDPrePostingInvoiceDiscountPercentage) {
		m_dPrePostingInvoiceDiscountPercentage = mDPrePostingInvoiceDiscountPercentage;
	}

	public String getM_dPrePostingInvoiceDiscountAmount() {
		return m_dPrePostingInvoiceDiscountAmount;
	}

	public void setM_dPrePostingInvoiceDiscountAmount(
			String mDPrePostingInvoiceDiscountAmount) {
		m_dPrePostingInvoiceDiscountAmount = mDPrePostingInvoiceDiscountAmount;
	}

	public String getM_LASTEDITUSERFULLNAME() {
		return m_LASTEDITUSERFULLNAME;
	}

	public void setM_LASTEDITUSERFULLNAME(String mLASTEDITUSERFULLNAME) {
		m_LASTEDITUSERFULLNAME = mLASTEDITUSERFULLNAME;
	}
	
	public String getM_LASTEDITUSERID() {
		return m_LASTEDITUSERID;
	}

	public void setM_LASTEDITUSERID(String mLASTEDITUSERID) {
		m_LASTEDITUSERID = mLASTEDITUSERID;
	}

	public String getM_LASTEDITPROCESS() {
		return m_LASTEDITPROCESS;
	}

	public void setM_LASTEDITPROCESS(String mLASTEDITPROCESS) {
		m_LASTEDITPROCESS = mLASTEDITPROCESS;
	}

	public String getM_LASTEDITDATE() {
		return m_LASTEDITDATE;
	}

	public void setM_LASTEDITDATE(String mLASTEDITDATE) {
		m_LASTEDITDATE = mLASTEDITDATE;
	}

	public String getM_LASTEDITTIME() {
		return m_LASTEDITTIME;
	}

	public void setM_LASTEDITTIME(String mLASTEDITTIME) {
		m_LASTEDITTIME = mLASTEDITTIME;
	}

	public String getM_sCustomerControlAcctSet() {
		return m_sCustomerControlAcctSet;
	}

	public void setM_sCustomerControlAcctSet(String mSCustomerControlAcctSet) {
		m_sCustomerControlAcctSet = mSCustomerControlAcctSet;
	}

	public String getM_sPrePostingInvoiceDiscountDesc() {
		return m_sPrePostingInvoiceDiscountDesc;
	}

	public void setM_sPrePostingInvoiceDiscountDesc(
			String mSPrePostingInvoiceDiscountDesc) {
		m_sPrePostingInvoiceDiscountDesc = mSPrePostingInvoiceDiscountDesc;
	}

	public String getM_datOrderCanceledDate() {
		return m_datOrderCanceledDate;
	}

	public void setM_datOrderCanceledDate(String mDatOrderCanceledDate) {
		m_datOrderCanceledDate = mDatOrderCanceledDate;
	}

	public String getM_iOrderSourceID() {
		return m_iOrderSourceID;
	}

	public void setM_iOrderSourceID(String mIOrderSourceID) {
		m_iOrderSourceID = mIOrderSourceID;
	}

	public String getM_sOrderSourceDesc() {
		return m_sOrderSourceDesc;
	}

	public void setM_sOrderSourceDesc(String mSOrderSourceDesc) {
		m_sOrderSourceDesc = mSOrderSourceDesc;
	}

	public String getM_dEstimatedHour() {
		return m_dEstimatedHour;
	}

	public void setM_dEstimatedHour(String mDEstimatedHour) {
		m_dEstimatedHour = mDEstimatedHour;
	}

	public String getM_sEmailAddress() {
		return m_sEmailAddress;
	}

	public void setM_sEmailAddress(String mSEmailAddress) {
		m_sEmailAddress = mSEmailAddress;
	}

	public String getM_sShipToEmail() {
		return m_sshiptoemail;
	}

	public void setM_sShipToEmail(String sShipToEmail) {
		m_sshiptoemail = sShipToEmail;
	}
	public String getM_sGDocLink() {
		return m_sgdoclink;
	}
	public void setM_sGDocLink(String sGDocLink) {
		m_sgdoclink = sGDocLink;
	}
	
	public String getM_iSalesGroup() {
		return m_iSalesGroup;
	}

	public void setM_iSalesGroup(String mISalesGroup) {
		m_iSalesGroup = mISalesGroup;
	}

	public String getM_sClonedFrom() {
		return m_sClonedFrom;
	}

	public void setM_sClonedFrom(String mSClonedFrom) {
		m_sClonedFrom = mSClonedFrom;
	}

	public String getM_sgeocode() {
		return m_sgeocode;
	}

	public void setM_sgeocode(String mSgeocode) {
		m_sgeocode = mSgeocode;
	}
	
	public String getsQuoteDescription() {
		return m_squotedescription;
	}

	public void setsQuoteDescription(String sQuoteDescription) {
		m_squotedescription = sQuoteDescription;
	}

	public int get_iOrderDetailCount() {
		return m_arrDetails.size();
	}

	public ArrayList<SMOrderDetail> getM_arrOrderDetails() {
		return m_arrDetails;
	}

	public String getsBidID() {
		if (m_sbidid.compareToIgnoreCase("0") ==0){
			return "";
		}else{
			return m_sbidid;
		}
	}

	public void setsBidID(String sBidID) {
		if (sBidID.trim().compareToIgnoreCase("") == 0){
			m_sbidid = "0";
		}else{
			m_sbidid = sBidID;
		}
	}
	public String getitaxid(){
		return m_itaxid;
	}
	public void settaxid(String sitaxid){
		m_itaxid = sitaxid;
	}
	public String getstaxtype(){
		return m_staxtype;
	}
	public void setstaxtype(String staxtype){
		m_staxtype = staxtype;
	}
	
	public String getM_sInvoicingEmail() {
		return m_sinvoicingemailaddress;
	}

	public void setM_sInvoicingEmail(String sinvoicingemailaddress) {
		m_sinvoicingemailaddress = sinvoicingemailaddress;
	}
	
	public String getM_sInvoicingContact() {
		return m_sinvoicingcontact;
	}

	public void setM_sInvoicingContact(String sinvoicingcontact) {
		m_sinvoicingcontact = sinvoicingcontact;
	}
	
	public String getM_sInvoicingNotes() {
		return m_sinvoicingnotes;
	}

	public void setM_sInvoicingNotes(String sinvoicingnotes) {
		m_sinvoicingnotes = sinvoicingnotes;
	}
	
	public String getM_idoingbusinessasaddressid() {
		return m_idoingbusinessasaddressid;
	}

	public void setM_idoingbusinessasaddressid(String idoingbusinessasaddressid) {
		m_idoingbusinessasaddressid = idoingbusinessasaddressid;
	}
	
	private void initOrderVariables(){
		m_siID = "-1";
		m_sOrderNumber = "-1";
		m_sBillToName = "";
		m_sShipToName = "";
		m_sDirections = "";
		m_sTicketComments = "";
		m_sFieldNotes = "";
		m_sServiceTypeCode = "";
		m_strimmedordernumber = "";
    	m_bdtruckdays = "0.0000";
    	m_scarpenterrate = "";
    	m_slaborerrate = "";
    	m_selectricianrate = "";
    	m_bdtotalmarkup = "0.00";
    	m_bdtotalcontractamount = "0.00";
    	m_datwarrantyexpiration = EMPTY_DATE_STRING;
    	m_swagescalenotes = "";
    	m_ssecondarybilltophone = "";
    	m_ssecondaryshiptophone = "";
    	m_sshiptophone = "";
    	m_sbilltophone = "";
    	m_sbilltocontact = "";
    	m_sshiptocontact = "";
		m_sCustomerCode = "";
		m_sBillToAddressLine1 = "";
		m_sBillToAddressLine2 = "";
		m_sBillToAddressLine3 = "";
		m_sBillToAddressLine4 = "";
		m_sBillToCity = "";
		m_sBillToState = "";
		m_sBillToZip = "";
		m_sBillToCountry = "";
		m_sBillToFax = "";
		m_sShipToCode = "";
		m_sShipToAddress1 = "";
		m_sShipToAddress2 = "";
		m_sShipToAddress3 = "";
		m_sShipToAddress4 = "";
		m_sShipToCity = "";
		m_sShipToState = "";
		m_sShipToZip = "";
		m_sShipToCountry = "";
		m_sShipToFax = "";
		m_iCustomerDiscountLevel = ""; //Integer.toString(SMTableorderheaders.CUSTOMERTYPE_BASE);
		m_sDefaultPriceListCode = "";
		m_sPONumber = "";
		m_sSpecialWageRate = "F";
		m_sTerms = "";
		m_iOrderType = Integer.toString(SMTableorderheaders.ORDERTYPE_ACTIVE);
		m_datOrderDate = clsDateAndTimeConversions.now("M/d/yyyy");
		m_datExpectedShipDate = EMPTY_DATE_STRING;
		m_datOrderCreationDate = clsDateAndTimeConversions.now("MM/dd/yyyy HH:mm:ss");
		m_sServiceTypeCodeDescription = "";
		m_sLastInvoiceNumber = "";
		m_iNumberOfInvoices = "0";
		m_sLocation = "";
		m_iOnHold = "0";
		m_datLastPostingDate = EMPTY_DATE_STRING;
		m_dTotalAmountItems = "0.00";
		m_iNumberOfLinesOnOrder = "0";
		m_sSalesperson = "";
		m_sOrderCreatedByFullName = "";
		m_lOrderCreatedByID = "";
		m_staxjurisdiction = "";
		m_sDefaultItemCategory = "";
		m_dTaxBase = "0.00";
		m_sordersalestaxamount = "0.00";
		m_iNextDetailNumber = "1";
		m_mInternalComments = "";
		m_mInvoiceComments = "";
		m_dPrePostingInvoiceDiscountPercentage = "0.00";
		m_dPrePostingInvoiceDiscountAmount = "0.00";
		m_LASTEDITUSERFULLNAME = "";
		m_LASTEDITUSERID = "0";
		m_LASTEDITPROCESS = "";
		m_LASTEDITDATE = "";
		m_LASTEDITTIME = "";
		m_sCustomerControlAcctSet = "";
		m_sPrePostingInvoiceDiscountDesc = "";
		m_datOrderCanceledDate = EMPTY_DATE_STRING;
		m_iOrderSourceID = "0";
		m_sOrderSourceDesc = "";
		m_dEstimatedHour = "0";
		m_sEmailAddress = ""; 
		m_iSalesGroup = "0";
		m_sClonedFrom = "";
		m_sgeocode = "";
		m_sshiptoemail = "";
		m_sgdoclink = "";
		m_bddepositamount = "0";
		m_sbidid = "0";
		m_squotedescription = "";
		m_itaxid = "";
		m_staxtype = "";
		m_sinvoicingemailaddress = "";
		m_sinvoicingcontact = "";
		m_sinvoicingnotes = "";
		m_idoingbusinessasaddressid = "0";
		m_arrDetails.clear();
		super.initVariables();
		super.setObjectName(ParamObjectName);
	}
    private boolean isDateValid(String sDateLabel, String sTestDate, boolean bAllowBlankDate){
        if (sTestDate.compareTo(EMPTY_DATE_STRING) == 0){
        	if (!bAllowBlankDate){
        		super.addErrorMessage(sDateLabel + " cannot be blank.");
        		return false;
        	}
        }else{
	        if (!clsDateAndTimeConversions.IsValidDateString("M/d/yyyy", sTestDate)){
	        	super.addErrorMessage(sDateLabel + " '" + sTestDate + "' is invalid.  ");
	        	return false;
	        }
        }
        return true;
    }
    private boolean isDoubleValid(String sDoubleLabel, String sTestDouble, int iScale){
    	@SuppressWarnings("unused")
        Double dTest;
        if (sTestDouble.compareToIgnoreCase("") == 0){
        	sTestDouble = "0." + clsStringFunctions.PadLeft("", "0", iScale);
        }else{
        	try {
				dTest = Double.parseDouble(sTestDouble.replace(",", ""));
			} catch (NumberFormatException e) {
				super.addErrorMessage(sDoubleLabel + " value (" + sTestDouble + ") is not valid.");
				return false;
			}
        }
        return true;
    }
    private boolean isPositiveDoubleValid(String sDoubleLabel, String sTestDouble, int iScale){
        Double dTest;
        if (sTestDouble.trim().compareToIgnoreCase("") == 0){
			super.addErrorMessage(sDoubleLabel + " value ('" + sTestDouble + "') cannot be blank.");
			return false;
        }else{
        	try {
				dTest = Double.parseDouble(sTestDouble.replace(",", ""));
			} catch (NumberFormatException e) {
				super.addErrorMessage(sDoubleLabel + " value (" + sTestDouble + ") is not valid.");
				return false;
			}
			if (dTest.compareTo(new Double("0.00")) < 0){
				super.addErrorMessage(sDoubleLabel + " value (" + sTestDouble + ") cannot be less than zero.");
				return false;
			}
        }
        return true;
    }
    
    private boolean isDecimalValid(String sDecimalLabel, String sTestDecimal, int iScale){
    	@SuppressWarnings("unused")
        BigDecimal bdTest;
    	sTestDecimal = sTestDecimal.trim();
        if (sTestDecimal.compareToIgnoreCase("") == 0){
			super.addErrorMessage(sDecimalLabel + " value ('" + sTestDecimal + "') cannot be blank.");
			return false;
        }else{
        	try {
        		bdTest = new BigDecimal(sTestDecimal.replace(",", ""));
			} catch (NumberFormatException e) {
				super.addErrorMessage(sDecimalLabel + " value (" + sTestDecimal + ") is not valid.");
				return false;
			}
        }
        return true;
    }
    private boolean isBooleanIntValid(String sBooleanLabel, String sBooleanInt){
    	sBooleanInt = sBooleanInt.trim();
        if (
        	(sBooleanInt.compareToIgnoreCase("0") != 0)
        	&& (sBooleanInt.compareToIgnoreCase("1") != 0)
        ){
  	       	super.addErrorMessage(sBooleanLabel + " '" + sBooleanInt + "' is not valid.");
   	       	return false;
        }
        return true;
    }
    private boolean isLongValid(String sLongLabel, String sLong, boolean bMustBeZeroOrGreater){
    	sLong = sLong.trim();
    	if (sLong.compareToIgnoreCase("") == 0){
    		super.addErrorMessage(sLongLabel + " is blank.");
    		return false;
    	}
    	long lTestLong = 0;
    	try {
			lTestLong = Long.parseLong(sLong);
		} catch (NumberFormatException e) {
    		super.addErrorMessage(sLongLabel + " (" + sLong + ") is invalid.");
    		return false;
		}
    	
		if (bMustBeZeroOrGreater){
	    	if (lTestLong < 0){
	    		super.addErrorMessage(sLongLabel + " is less than zero.");
	    		return false;
	    	}
		}
		return true;
    }
    private boolean isStringValid(String sStringLabel, String sString, int iMaxLength, boolean bAllowBlank){
    	if (sString == null){
    		sString = "";
    	}
    	try {
			sString = sString.trim();
		} catch (Exception e) {
			return false;
		}
        if (!bAllowBlank){
        	if (sString.compareToIgnoreCase("") == 0){
   	       		super.addErrorMessage(sStringLabel + " cannot be blank.");
   	       		return false;
        	}
        }
        if (sString.length() > iMaxLength){
   	       	super.addErrorMessage(sStringLabel + " is too long.");
   	       	return false;
        }
        return true;
    }
    
    public void Upgrade_Qty_Ordered(int iLine){
    	m_arrDetails.get(iLine).setM_dQtyOrdered(String.valueOf(
    		Double.parseDouble(m_arrDetails.get(iLine).getM_dQtyOrdered().replace(",", "")) - 
    			Double.parseDouble(m_arrDetails.get(iLine).getM_dQtyShipped().replace(",", "")))
    	);
    }

    public String getTaxAmount(String sDBID, String sUser, ServletContext context) throws Exception{
    	Connection conn = clsDatabaseFunctions.getConnectionWithException(
    		context, 
    		sDBID, 
    		"MySQL", 
    		this.toString() + ".getTaxAmount - user = " + sUser + " [1377028099]");
    	if (conn == null){
    		clsDatabaseFunctions.freeConnection(context, conn, "[1547067724]");
    		throw new Exception("Could not open connection to get tax amount.");
    	}
    	String sTaxAmount;
		try {
			sTaxAmount = getSalesTaxAmount(conn);
		} catch (Exception e) {
			clsDatabaseFunctions.freeConnection(context, conn, "[1547067725]");
			throw new Exception(e);
		}
		clsDatabaseFunctions.freeConnection(context, conn, "[1547067726]");
    	return sTaxAmount;
    }
    public String getSalesTaxAmount(Connection conn) throws Exception{
    	return clsManageBigDecimals.BigDecimalTo2DecimalSQLFormat(calculateSalesTaxAmount(conn));
    }
    
    public SMOrderHeader Clone(boolean bCloneDetails, 
    						   Date datNow, 
    						   String sUserID, 
    						   String sUserFullName, 
    						   Connection conn) throws Exception{
    	
    	//SimpleDateFormat sdfLong = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
    	SimpleDateFormat sdfShort = new SimpleDateFormat(SMUtilities.DATE_FORMAT_FOR_DISPLAY);
    	
    	//If this is not a quote, it can't have a blank customer code:
    	m_sCustomerCode = m_sCustomerCode.trim();
    	if (getM_iOrderType().compareToIgnoreCase(Integer.toString(SMTableorderheaders.ORDERTYPE_QUOTE)) != 0){
    		if (m_sCustomerCode.compareToIgnoreCase("") == 0){
    			throw new Exception("Customer cannot be blank on orders.");
    		}
    	}
    	if (m_sCustomerCode.compareToIgnoreCase("") != 0){
    		ARCustomer cus = new ARCustomer(m_sCustomerCode);
	    	if (!cus.load(conn)){
	    		throw new Exception("Error loading customer '" + m_sCustomerCode + "' - " + cus.getErrorMessageString());
	    	}
	    	if (cus.getM_iActive().compareToIgnoreCase("1") != 0){
	    		throw new Exception("Customer '" + m_sCustomerCode + " is not active - this order cannot be cloned.");
	    	}
	    	if (cus.getM_iOnHold().compareToIgnoreCase("1") == 0){
	    		throw new Exception("Customer '" + m_sCustomerCode + " is on hold - this order cannot be cloned.");
	    	}
    	}
    	SMOrderHeader newOrder = new SMOrderHeader();
    	newOrder.setM_sBillToName(m_sBillToName);
    	newOrder.setM_sShipToName(m_sShipToName);
    	newOrder.setM_sDirections(m_sDirections);
    	newOrder.setM_sTicketComments(m_sTicketComments);
    	newOrder.setM_sFieldNotes(m_sFieldNotes);
    	newOrder.setM_sServiceTypeCode(m_sServiceTypeCode);
    	//Project info:
    	newOrder.setM_bdtruckdays(m_bdtruckdays);
    	newOrder.setM_scarpenterrate(m_scarpenterrate);
    	newOrder.setM_slaborerrate(m_slaborerrate);
    	newOrder.setM_selectricianrate(m_selectricianrate);
    	newOrder.setM_bdtotalmarkup(m_bdtotalmarkup);
    	newOrder.setM_bdtotalcontractamount(m_bdtotalcontractamount);
    	newOrder.setM_datwarrantyexpiration(m_datwarrantyexpiration);
    	newOrder.setM_swagescalenotes(m_swagescalenotes);
    	newOrder.setM_ssecondarybilltophone(m_ssecondarybilltophone);
    	newOrder.setM_ssecondaryshiptophone(m_ssecondaryshiptophone);
    	newOrder.setM_sBilltoPhone(m_sbilltophone);
    	newOrder.setM_sShiptoPhone(m_sshiptophone);
    	newOrder.setM_sBilltoContact(m_sbilltocontact);
    	newOrder.setM_sShiptoContact(m_sshiptocontact);
    	newOrder.setM_strimmedordernumber("");  //changed field
    	
    	//The rest of the order header fields:
    	newOrder.setM_sCustomerCode(m_sCustomerCode);
    	newOrder.setM_sBillToAddressLine1(m_sBillToAddressLine1);
    	newOrder.setM_sBillToAddressLine2(m_sBillToAddressLine2);
    	newOrder.setM_sBillToAddressLine3(m_sBillToAddressLine3);
    	newOrder.setM_sBillToAddressLine4(m_sBillToAddressLine4);
    	newOrder.setM_sBillToCity(m_sBillToCity);
    	newOrder.setM_sBillToState(m_sBillToState);
    	newOrder.setM_sBillToZip(m_sBillToZip);
    	newOrder.setM_sBillToCountry(m_sBillToCountry);
    	newOrder.setM_sBillToFax(m_sBillToFax);
    	newOrder.setM_sShipToCode(m_sShipToCode);
    	newOrder.setM_sShipToAddress1(m_sShipToAddress1);
    	newOrder.setM_sShipToAddress2(m_sShipToAddress2);
    	newOrder.setM_sShipToAddress3(m_sShipToAddress3);
    	newOrder.setM_sShipToAddress4(m_sShipToAddress4);
    	newOrder.setM_sShipToCity(m_sShipToCity);
    	newOrder.setM_sShipToState(m_sShipToState);
    	newOrder.setM_sShipToZip(m_sShipToZip);
    	newOrder.setM_sShipToCountry(m_sShipToCountry);
    	newOrder.setM_sShipToFax(m_sShipToFax);
    	newOrder.setM_iCustomerDiscountLevel(m_iCustomerDiscountLevel);
    	newOrder.setM_sDefaultPriceListCode(m_sDefaultPriceListCode);
    	newOrder.setM_sPONumber(m_sPONumber);
    	newOrder.setM_sSpecialWageRate(m_sSpecialWageRate);
    	newOrder.setM_sTerms(m_sTerms);
    	newOrder.setM_iOrderType(m_iOrderType);
    	newOrder.setM_datOrderDate(sdfShort.format(datNow));
    	newOrder.setM_sOrderCreatedByFullName(sUserFullName);
    	newOrder.setM_lOrderCreatedByID(sUserID);
    	newOrder.setM_datExpectedShipDate(EMPTY_DATE_STRING);
    	newOrder.setM_sServiceTypeCodeDescription(m_sServiceTypeCodeDescription);
    	newOrder.setM_sLastInvoiceNumber(""); //changed field
    	newOrder.setM_iNumberOfInvoices("0");  //changed field
    	newOrder.setM_sLocation(m_sLocation);
    	newOrder.setM_iOnHold(m_iOnHold);
    	newOrder.setM_dTotalAmountItems(m_dTotalAmountItems);
    	newOrder.setM_iNumberOfLinesOnOrder(m_iNumberOfLinesOnOrder);
    	newOrder.setM_sSalesperson(m_sSalesperson);
    	newOrder.setstaxjurisdiction(m_staxjurisdiction);
    	newOrder.settaxid(m_itaxid);
    	newOrder.setstaxtype(m_staxtype);
    	newOrder.setM_sDefaultItemCategory(m_sDefaultItemCategory);
    	newOrder.setM_dTaxBase(m_dTaxBase);
    	newOrder.setsordersalestaxamount(m_sordersalestaxamount);
    	newOrder.setM_iNextDetailNumber(m_iNextDetailNumber);
    	newOrder.setM_mInternalComments(m_mInternalComments);
    	newOrder.setM_mInvoiceComments(m_mInvoiceComments);
    	newOrder.setM_dPrePostingInvoiceDiscountPercentage(m_dPrePostingInvoiceDiscountPercentage);
    	newOrder.setM_dPrePostingInvoiceDiscountAmount(m_dPrePostingInvoiceDiscountAmount);
    	newOrder.setM_bddepositamount(m_bddepositamount);
    	newOrder.setM_sCustomerControlAcctSet(m_sCustomerControlAcctSet);
    	newOrder.setM_sPrePostingInvoiceDiscountDesc(m_sPrePostingInvoiceDiscountDesc);
    	newOrder.setM_datOrderCanceledDate(m_datOrderCanceledDate);
    	newOrder.setM_iOrderSourceID(m_iOrderSourceID);
    	newOrder.setM_sOrderSourceDesc(m_sOrderSourceDesc);
    	newOrder.setM_dEstimatedHour(m_dEstimatedHour);
    	newOrder.setM_sEmailAddress(m_sEmailAddress); 
    	newOrder.setM_iSalesGroup(m_iSalesGroup);
    	newOrder.setM_sClonedFrom(m_sOrderNumber);
    	newOrder.setM_sgeocode(m_sgeocode);
    	newOrder.setM_sShipToEmail(m_sshiptoemail);
    	newOrder.setM_sInvoicingContact(m_sinvoicingcontact);
    	newOrder.setM_sInvoicingEmail(m_sinvoicingemailaddress);
    	newOrder.setM_sInvoicingNotes(m_sinvoicingnotes);
    	newOrder.setM_idoingbusinessasaddressid(m_idoingbusinessasaddressid);
    	
    	if (bCloneDetails){
    		for (int i=0;i<m_arrDetails.size();i++){
    			SMOrderDetail newDetail = new SMOrderDetail();
    			SMOrderDetail oriDetail = m_arrDetails.get(i);
    			//m_dUniqueOrderID;
    			newDetail.setM_iDetailNumber(oriDetail.getM_iDetailNumber());
    			newDetail.setM_mInvoiceComments(oriDetail.getM_mInvoiceComments());
    			newDetail.setM_mInternalComments(oriDetail.getM_mInternalComments());
    			newDetail.setM_mTicketComments(oriDetail.getM_mTicketComments());
    			newDetail.setM_iLineNumber(oriDetail.getM_iLineNumber());
    			newDetail.setM_sItemNumber(oriDetail.getM_sItemNumber());
    			newDetail.setM_sItemDesc(oriDetail.getM_sItemDesc());
    			newDetail.setM_sItemCategory(oriDetail.getM_sItemCategory());
    			newDetail.setM_sLocationCode(oriDetail.getM_sLocationCode());
    			newDetail.setM_datDetailExpectedShipDate(oriDetail.getM_datDetailExpectedShipDate());
    			newDetail.setM_iIsStockItem(oriDetail.getM_iIsStockItem());
    			newDetail.setM_dQtyOrdered(oriDetail.getM_dQtyOrdered());
    			//LTO 20120202 The following qty should be 0.
    			newDetail.setM_dQtyShipped("0");
    			newDetail.setM_dQtyShippedToDate("0");
    			newDetail.setM_dOriginalQty(oriDetail.getM_dOriginalQty());
    			newDetail.setM_sOrderUnitOfMeasure(oriDetail.getM_sOrderUnitOfMeasure());
    			newDetail.setM_dOrderUnitPrice(oriDetail.getM_dOrderUnitPrice());
    			newDetail.setM_dOrderUnitCost(oriDetail.getM_dOrderUnitCost());
    			newDetail.setM_bdEstimatedUnitCost(oriDetail.getM_bdEstimatedUnitCost());
    			newDetail.setM_dExtendedOrderPrice(oriDetail.getM_dExtendedOrderPrice());
    			newDetail.setM_dExtendedOrderCost(oriDetail.getM_dExtendedOrderCost());
    			newDetail.setM_iTaxable(oriDetail.getM_iTaxable());
    			newDetail.setM_datLineBookedDate(sdfShort.format(datNow));
    			newDetail.setM_sMechID(oriDetail.getM_sMechID());
    			newDetail.setM_sMechInitial(oriDetail.getM_sMechInitial());
    			newDetail.setM_sMechFullName(oriDetail.getM_sMechFullName());
    			newDetail.setM_sLabel(oriDetail.getM_sLabel());
    			newDetail.setM_strimmedordernumber(oriDetail.getM_strimmedordernumber());
    			newDetail.setM_isuppressdetailoninvoice(oriDetail.getM_isuppressdetailoninvoice());
    			newOrder.addNewDetail(newDetail);
    		}
    	}else{
    		//if not cloning details, then insert a default one.
    		try {
				newOrder.addInitialItem(conn);
			} catch (Exception e) {
    			throw new Exception("Error adding initial item - " + e.getMessage() + ".");
    		}
    	}
		try {
			recalculateEntireOrder(conn, true, false);
		} catch (Exception e2) {
			throw new Exception("Error [1422568962] recalculating order on clone - " + e2.getMessage());
		}
    	return newOrder;
    }
    public void calculateBillingTotals(Connection conn, String strimmedordernumber) throws Exception{
    	m_bdTotalBilled = new BigDecimal("0.00");
    	m_bdChangeOrderTotal = new BigDecimal("0.00");
    	m_bdOriginalContractAmount = new BigDecimal("0.00");
    	m_bdTotalContractAmtRemaining = new BigDecimal("0.00");
    	m_bdRemainingAmtDifference = new BigDecimal("0.00");
    	m_bdRemainingOrderedLineTotal = new BigDecimal("0.00");
    	m_bdRemainingShippedLineTotal = new BigDecimal("0.00");
    	String SQL = "";

    	try{
			SQL = "SELECT"
				+ " " + SMTableinvoicedetails.TableName + "." + SMTableinvoicedetails.sInvoiceNumber
				+ ", " + SMTableinvoiceheaders.TableName + "." + SMTableinvoiceheaders.datInvoiceDate
				+ ", " + SMTableinvoiceheaders.TableName + "." + SMTableinvoiceheaders.iTransactionType
				+ ", SUM(" + SMTableinvoicedetails.TableName + "." 
					+  SMTableinvoicedetails.dExtendedPriceAfterDiscount + ") AS EXTPRICE"
				+ " FROM " + SMTableinvoicedetails.TableName + " INNER JOIN "
				+ SMTableinvoiceheaders.TableName
				+ " ON " + SMTableinvoicedetails.TableName + "." + SMTableinvoicedetails.sInvoiceNumber
				+ " = " + SMTableinvoiceheaders.TableName + "." + SMTableinvoiceheaders.sInvoiceNumber
				+ " WHERE ("
					+ SMTableinvoiceheaders.TableName + "." + SMTableinvoiceheaders.strimmedordernumber
					+ " = '" + strimmedordernumber + "'"
				+ ")"
				+ " GROUP BY (" + SMTableinvoicedetails.TableName + "." + SMTableinvoicedetails.sInvoiceNumber 
				+ ")"
				;

			ResultSet rsInvoices = clsDatabaseFunctions.openResultSet(SQL, conn);
			while(rsInvoices.next()){
				m_bdTotalBilled = m_bdTotalBilled.add(rsInvoices.getBigDecimal("EXTPRICE").setScale(2, BigDecimal.ROUND_HALF_UP));
				m_bdTotalBilled = m_bdTotalBilled.setScale(2, BigDecimal.ROUND_HALF_UP);
			}
			rsInvoices.close();
		}catch(SQLException e){
			throw new Exception("Error reading invoices for order number " + strimmedordernumber + " - " + e.getMessage());
		}
		try{
			SQL = "SELECT * FROM " + SMTablechangeorders.TableName
			+ " WHERE ("
			+ SMTablechangeorders.sJobNumber + " = '" + strimmedordernumber + "'" 
			+ ")"
			+ " ORDER BY " + SMTablechangeorders.datChangeOrderDate
			;
			ResultSet rsChangeOrders = clsDatabaseFunctions.openResultSet(SQL, conn);
			while(rsChangeOrders.next()){
				m_bdChangeOrderTotal = m_bdChangeOrderTotal.add(rsChangeOrders.getBigDecimal(SMTablechangeorders.dAmount.replace("`", "")).setScale(
					SMTablechangeorders.dAmountScale, BigDecimal.ROUND_HALF_UP));
				m_bdChangeOrderTotal = m_bdChangeOrderTotal.setScale(SMTablechangeorders.dAmountScale, BigDecimal.ROUND_HALF_UP);
			}
			rsChangeOrders.close();
		}catch(SQLException e){
			throw new Exception("Error reading change orders for order number " + strimmedordernumber + " - " + e.getMessage());
		}
		
		//***********************
		try{
			SQL = "SELECT"
				+ " " + SMTableorderheaders.TableName + "." + SMTableorderheaders.bdtotalcontractamount
				+ ", " + SMTableorderdetails.TableName + "." + SMTableorderdetails.dQtyOrdered
				+ ", " + SMTableorderdetails.TableName + "." + SMTableorderdetails.dExtendedOrderPrice
				+ ", " + SMTableorderdetails.TableName + "." + SMTableorderdetails.dOrderUnitPrice
				+ ", " + SMTableorderdetails.TableName + "." + SMTableorderdetails.dQtyShipped
				+ " FROM " + SMTableorderdetails.TableName + " LEFT JOIN " + SMTableorderheaders.TableName + " ON "
				+ SMTableorderdetails.TableName + "." + SMTableorderdetails.strimmedordernumber + " = "
				+ SMTableorderheaders.TableName + "." + SMTableorderheaders.strimmedordernumber
				+ " WHERE ("
					+ SMTableorderdetails.TableName + "." + SMTableorderdetails.strimmedordernumber
					+ " = '" + strimmedordernumber + "'"
				+ ")"
				;
			//System.out.println("[1350407412] - SQL = " + SQL);
			ResultSet rsOrder = clsDatabaseFunctions.openResultSet(SQL, conn);
			while(rsOrder.next()){
				m_bdOriginalContractAmount = rsOrder.getBigDecimal(SMTableorderheaders.bdtotalcontractamount).setScale(SMTableorderheaders.bdtotalcontractamountScale, BigDecimal.ROUND_HALF_UP);
				//If the line has a shipped qty, use the extended price for the amount shipped:
				BigDecimal bdQtyShipped = rsOrder.getBigDecimal(SMTableorderdetails.TableName + "." 
					+ SMTableorderdetails.dQtyShipped).setScale(SMTableorderdetails.dQtyShippedScale, BigDecimal.ROUND_HALF_UP);
				BigDecimal bdQtyOrdered = rsOrder.getBigDecimal(SMTableorderdetails.TableName + "." 
					+ SMTableorderdetails.dQtyOrdered).setScale(SMTableorderdetails.dQtyOrderedScale, BigDecimal.ROUND_HALF_UP);
				BigDecimal bdQtyRemainingToBeShipped = bdQtyOrdered.subtract(bdQtyShipped).setScale(
					SMTableorderdetails.dQtyOrderedScale, BigDecimal.ROUND_HALF_UP);
				BigDecimal bdExtendedPrice = rsOrder.getBigDecimal(SMTableorderdetails.TableName + "." + SMTableorderdetails.dExtendedOrderPrice);
				BigDecimal bdUnitPrice = rsOrder.getBigDecimal(SMTableorderdetails.TableName + "." + SMTableorderdetails.dOrderUnitPrice);
				
				//If there is currently a qty shipped, get the value of that into the remaining shipped line total:
				if (bdQtyShipped.compareTo(BigDecimal.ZERO) > 0){
					m_bdRemainingShippedLineTotal = m_bdRemainingShippedLineTotal.add(bdExtendedPrice).setScale(2, BigDecimal.ROUND_HALF_UP);
					//If there is still more left in the order qty, add that to the value still on the order:
					if (bdQtyRemainingToBeShipped.compareTo(BigDecimal.ZERO) > 0){
						m_bdRemainingOrderedLineTotal = m_bdRemainingOrderedLineTotal.add(bdQtyRemainingToBeShipped.multiply(
							bdUnitPrice)).setScale(2, BigDecimal.ROUND_HALF_UP);
					}
				//If there is NO qty shipped, then get the value of the amount still on order:
				}else{
					//If there is not an extended price on the line, then the remaining order value for the line is just the qty ordered
					//times the unit price:
					if (bdExtendedPrice.compareTo(BigDecimal.ZERO) == 0){
						m_bdRemainingOrderedLineTotal = m_bdRemainingOrderedLineTotal.add(bdQtyRemainingToBeShipped.multiply(
							bdUnitPrice)).setScale(2, BigDecimal.ROUND_HALF_UP);
					//But if there IS an extended price on the line, then the remaining order value for the line is equal to the
					//extended price:
					}else{
						m_bdRemainingOrderedLineTotal = m_bdRemainingOrderedLineTotal.add(
							bdExtendedPrice).setScale(2, BigDecimal.ROUND_HALF_UP);							
					}
				}
			}
			rsOrder.close();
		}catch(SQLException e){
			throw new Exception("Error reading contract amt for order number " + strimmedordernumber + " - " + e.getMessage());
		}
				
		if (getM_datOrderCanceledDate().compareToIgnoreCase("00/00/0000") != 0){
			m_bdRemainingShippedLineTotal = BigDecimal.ZERO;
			m_bdRemainingOrderedLineTotal = BigDecimal.ZERO;
		}			
		m_bdTotalContractAmtRemaining = m_bdOriginalContractAmount.add(m_bdChangeOrderTotal).subtract(m_bdTotalBilled) ;
		m_bdTotalContractAmtRemaining = m_bdTotalContractAmtRemaining.setScale(SMTableorderheaders.bdtotalcontractamountScale, BigDecimal.ROUND_HALF_UP);
		m_bdRemainingAmtDifference = m_bdTotalContractAmtRemaining.subtract(m_bdRemainingShippedLineTotal.add(m_bdRemainingOrderedLineTotal)).setScale(
				SMTableorderheaders.bdtotalcontractamountScale, BigDecimal.ROUND_HALF_UP);
    }
    public BigDecimal getCalculatedOrderTotals_TotalBilled(){
    	return m_bdTotalBilled;
    }
    public BigDecimal getCalculatedOrderTotals_TotalAmtStillOnOrder(){
        return m_bdRemainingOrderedLineTotal;
    }
    public BigDecimal getCalculatedOrderTotals_TotalAmtCurrentlyShipped(){
        return m_bdRemainingShippedLineTotal;
    }
    public BigDecimal getCalculatedOrderTotals_ChangeOrderTotal(){
        return m_bdChangeOrderTotal;
    }
    public BigDecimal getCalculatedOrderTotals_OriginalContractAmount(){
        return m_bdOriginalContractAmount;
    }
    public BigDecimal getCalculatedOrderTotals_TotalContractAmtRemaining(){
        return m_bdTotalContractAmtRemaining;
    }
    public BigDecimal getCalculatedOrderTotals_RemainingAmtDifference(){
        return m_bdRemainingAmtDifference;
    }
    public boolean isOrderCanceled(){
    	return getM_datOrderCanceledDate().compareToIgnoreCase("00/00/0000") != 0;
    }
    public boolean isDBAValid(Connection conn){
    	String SQL = "SELECT " + SMTabledoingbusinessasaddresses.lid 
    				+ " FROM " + SMTabledoingbusinessasaddresses.TableName
    				+ " WHERE (" + SMTabledoingbusinessasaddresses.lid + "=" + getM_idoingbusinessasaddressid() + ")"
    				;   	
    	try {
			ResultSet rs = clsDatabaseFunctions.openResultSet(SQL,conn);
			if (rs.next()){
				rs.close();
				return true;
			}else {
				rs.close();
				return false;
			}
		} catch (Exception e) {
			return false;
		}  	
    }
    public String checkOrderForDiscrepancies(String sDBIB, ServletContext context, String sUserID, String sUserFullName) throws Exception{
    	String sReturn = "";
    	//First, check to see if the price list code is valid:
    	//If this is a quote with no customer on it, then bypass this check:
    	if (
    			(getM_iOrderType().compareToIgnoreCase(Integer.toString(SMTableorderheaders.ORDERTYPE_QUOTE)) == 0)
    			&& (getM_sCustomerCode().compareToIgnoreCase("") == 0) 
    	){
    		return sReturn;
    	}
    	
    	String SQL = "SELECT"
    			+ " " + SMTablepricelistcodes.spricelistcode
    			+ " FROM " + SMTablepricelistcodes.TableName
    			+ " WHERE ("
    				+ SMTablepricelistcodes.spricelistcode + " = '" + getM_sDefaultPriceListCode() + "'"
    			+ ")"
    	;
    	try {
			ResultSet rs = clsDatabaseFunctions.openResultSet(
					SQL, 
					context, 
					sDBIB, 
					"MySQL", 
					this.toString() + ".checkOrderForDiscrepancies - user: " 
					+ sUserID
					+ " - "
					+ sUserFullName
					);
			if (!rs.next()){
				sReturn += "  This order has an invalid price list code ('" + getM_sDefaultPriceListCode() 
						+ "') - a new code must be selected; check prices after updating.";
			}
			rs.close();
		} catch (Exception e) {
			throw new Exception ("Error checking for price list code - " + e.getMessage());
		}
    	return sReturn;
    }
    public void changeCustomer(
    		String sDBID, 
    		String sUserID,
    		String sUserFullName,
    		ServletContext context,
    		String sNewCustomer,
    		String sTrimmedOrderNumber,
    		boolean bOverwriteBillTo) throws Exception{
    	
    	//Load the order:
    	setM_strimmedordernumber(sTrimmedOrderNumber);
    	if (!load(context, sDBID, sUserID, sUserFullName)){
    		throw new Exception("Could not load order number '" + sTrimmedOrderNumber + "' - " + getErrorMessages());
    	}
    	//First, validate the customer:
		String SQL = "SELECT * FROM " + SMTablearcustomer.TableName
			+ " WHERE ("
				+ "(" + SMTablearcustomer.sCustomerNumber + " = '" + sNewCustomer + "')"
			+ ")"
			;
		try {
			ResultSet rs = clsDatabaseFunctions.openResultSet(
				SQL, 
				context, 
				sDBID, 
				"MySQL", 
				this.toString() + " [1373466257] - SQL: " + clsDateAndTimeConversions.now("yyyyMMdd HH:mm:ss:SSS") + " - user: " 
				+ sUserID
				+ " - "
				+ sUserFullName
				+ ".");
			if (rs.next()){
				if (rs.getLong(SMTablearcustomer.iActive) == 0){
					rs.close();
					throw new Exception("Customer '" + sNewCustomer + "' is inactive.");
				}
				if (rs.getLong(SMTablearcustomer.iOnHold) == 1){
					String sCustomerComments = rs.getString(SMTablearcustomer.mCustomerComments);
					rs.close();
					throw new Exception("Customer '" + sNewCustomer + "' is on hold. <BR>" 
						+ "Customer Comments: " + sCustomerComments);
				}
				if (bOverwriteBillTo){
					setM_sBillToAddressLine1(rs.getString(SMTablearcustomer.sAddressLine1));
					setM_sBillToAddressLine2(rs.getString(SMTablearcustomer.sAddressLine2));
					setM_sBillToAddressLine3(rs.getString(SMTablearcustomer.sAddressLine3));
					setM_sBillToAddressLine4(rs.getString(SMTablearcustomer.sAddressLine4));
					setM_sBillToCity(rs.getString(SMTablearcustomer.sCity));
					setM_sBilltoContact(rs.getString(SMTablearcustomer.sContactName));
					setM_sBillToCountry(rs.getString(SMTablearcustomer.sCountry));
					setM_sBillToFax(rs.getString(SMTablearcustomer.sFaxNumber));
					setM_sBillToName(rs.getString(SMTablearcustomer.sCustomerName));
					setM_sBilltoPhone(rs.getString(SMTablearcustomer.sPhoneNumber));
					setM_sBillToState(rs.getString(SMTablearcustomer.sState));
					setM_sBillToZip(rs.getString(SMTablearcustomer.sPostalCode));
					setM_sEmailAddress(rs.getString(SMTablearcustomer.sEmailAddress));
					setM_sInvoicingContact(rs.getString(SMTablearcustomer.sinvoicingcontact));
					setM_sInvoicingEmail(rs.getString(SMTablearcustomer.sinvoicingemail));
					setM_sInvoicingNotes(rs.getString(SMTablearcustomer.sinvoicingnotes));
				}
				setM_iOnHold(Long.toString(rs.getLong(SMTablearcustomer.iOnHold)));
				setM_sCustomerCode(sNewCustomer);
				setM_sCustomerControlAcctSet(rs.getString(SMTablearcustomer.sAccountSet));
			}else{
				rs.close();
				throw new Exception("Customer '" + sNewCustomer + "' could not be found.");
			}
			rs.close();
		} catch (SQLException e) {
			throw new Exception("Unable to change customer to '" + sNewCustomer + "' - " + e.getMessage() + ".");
		}
		
		//Now save the order:
		Connection conn = null;
		try {
			conn = clsDatabaseFunctions.getConnectionWithException(
				context, 
				sDBID, 
				"MySQL", 
				this.toString() + ".save after customer change - user: " + sUserID + " - " + sUserFullName);
		} catch (Exception e) {
			throw new Exception("Could not get connection - " + e.getMessage() + ".");
		}
		
		if (!save_order_without_data_transaction(
			conn, 
			sDBID, 
			context, 
			sUserID,
			sUserFullName,
			false, 
			false, 
			"CUSTOMERCHANGE")){
			clsDatabaseFunctions.freeConnection(context, conn, "[1547067722]");
			throw new Exception("Could not save order - " + getErrorMessages());
		}
		clsDatabaseFunctions.freeConnection(context, conn, "[1547067723]");
    }
    public SMOrderDetail getOrderDetail(int iIndex){
    	return m_arrDetails.get(iIndex); 
    }
    public SMOrderDetail getOrderDetailByDetailNumber(String sDetailNumber){
    	for (int i = 0; i < m_arrDetails.size(); i++ ){
    		if (m_arrDetails.get(i).getM_iDetailNumber().compareToIgnoreCase(sDetailNumber) == 0){
    			return m_arrDetails.get(i);
    		}
    	}
    	return null;
    }
    private SMOrderDetail CloneDetail (SMOrderDetail cOriginalDetail){
    	SMOrderDetail cClonedDetail = new SMOrderDetail();
    	cClonedDetail.setM_dUniqueOrderID(cOriginalDetail.getM_dUniqueOrderID());
    	cClonedDetail.setM_iDetailNumber(cOriginalDetail.getM_iDetailNumber());
    	cClonedDetail.setM_mInvoiceComments(cOriginalDetail.getM_mInvoiceComments());
    	cClonedDetail.setM_mInternalComments(cOriginalDetail.getM_mInternalComments());
    	cClonedDetail.setM_mTicketComments(cOriginalDetail.getM_mTicketComments());
    	cClonedDetail.setM_iLineNumber(cOriginalDetail.getM_iLineNumber());
    	cClonedDetail.setM_sItemNumber(cOriginalDetail.getM_sItemNumber());
    	cClonedDetail.setM_sItemDesc(cOriginalDetail.getM_sItemDesc());
    	cClonedDetail.setM_sItemCategory(cOriginalDetail.getM_sItemCategory());
    	cClonedDetail.setM_sLocationCode(cOriginalDetail.getM_sLocationCode());
    	cClonedDetail.setM_datDetailExpectedShipDate(cOriginalDetail.getM_datDetailExpectedShipDate());
    	cClonedDetail.setM_iIsStockItem(cOriginalDetail.getM_iIsStockItem());
    	cClonedDetail.setM_dQtyOrdered(cOriginalDetail.getM_dQtyOrdered());
    	//cClonedDetail.setM_dQtyShipped(cOriginalDetail.getM_dQtyShipped());
    	//cClonedDetail.setM_dQtyShippedToDate(cOriginalDetail.getM_dQtyShippedToDate());
    	//Original qty should be equal to the qty, since this line is just being added now:
    	cClonedDetail.setM_dOriginalQty(cOriginalDetail.getM_dQtyOrdered());
    	cClonedDetail.setM_sOrderUnitOfMeasure(cOriginalDetail.getM_sOrderUnitOfMeasure());
    	cClonedDetail.setM_dOrderUnitPrice(cOriginalDetail.getM_dOrderUnitPrice());
    	//A new item will have NO cost because it hasn't been processed through invoicing:
    	cClonedDetail.setM_dOrderUnitCost("0.00");
    	cClonedDetail.setM_dExtendedOrderPrice(cOriginalDetail.getM_dExtendedOrderPrice());
    	//A new item will have NO extended cost because it hasn't been processed through invoicing:
    	cClonedDetail.setM_dExtendedOrderCost("0.00");
    	cClonedDetail.setM_iTaxable(cOriginalDetail.getM_iTaxable());
    	cClonedDetail.setM_datLineBookedDate(cOriginalDetail.getM_datLineBookedDate());
    	//Mechanic details should not go on NEW lines, because the assumption is that they haven't been shipped:
    	cClonedDetail.setM_sMechInitial("");
    	cClonedDetail.setM_sMechFullName("");
    	cClonedDetail.setM_sMechID("0");
    	cClonedDetail.setM_sLabel(cOriginalDetail.getM_sLabel());
    	cClonedDetail.setM_strimmedordernumber(cOriginalDetail.getM_strimmedordernumber());
    	cClonedDetail.setM_isuppressdetailoninvoice(cOriginalDetail.getM_isuppressdetailoninvoice());
    	cClonedDetail.setM_iprintondeliveryticket(cOriginalDetail.getM_iprintondeliveryticket());
    	//Estimated cost isn't necessarily the estimated cost of the original detail:
    	cClonedDetail.setM_bdEstimatedUnitCost("0.00");
    	cClonedDetail.setM_sErrorMessageArray(cOriginalDetail.getM_sErrorMessageArray());

    	return cClonedDetail;
    }
    
}