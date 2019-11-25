package SMDataDefinition;

public class SMTableorderheaders {

	public static String TableName = "orderheaders";

	public static final int ORDERTYPE_ACTIVE = 1;
	public static final int ORDERTYPE_FUTURE = 2;
	public static final int ORDERTYPE_STANDING = 3;
	public static final int ORDERTYPE_QUOTE = 4;

	public static final int CUSTOMERTYPE_BASE = 0;
	public static final int CUSTOMERTYPE_A = 1;
	public static final int CUSTOMERTYPE_B = 2;
	public static final int CUSTOMERTYPE_C = 3;
	public static final int CUSTOMERTYPE_D = 4;
	public static final int CUSTOMERTYPE_E = 5;
	
	public static final int PRINTSTATUS_NOTPRINTED = 1;
	public static final int PRINTSTATUS_PRINTED = 2;

	public static final int DAYEND_NOTPRINTED = 0;
	public static final int DAYEND_PRINTED = 1;
	
	//Field Titles
	public static String mAddressNotes_Name = "Address Notes";
	
	//Field names
	public static String dOrderUniqueifier = "dOrderUniqueifier"; //NOT NULL default '0',
	public static String sOrderNumber = "sOrderNumber"; //22) default NULL,
	public static String sCustomerCode = "sCustomerCode"; //12) default NULL,
	public static String sBillToName = "sBillToName"; //60) default NULL,
	public static String sBillToAddressLine1 = "sBillToAddressLine1"; //60) default NULL,
	public static String sBillToAddressLine2 = "sBillToAddressLine2"; //60) default NULL,
	public static String sBillToAddressLine3 = "sBillToAddressLine3"; //60) default NULL,
	public static String sBillToAddressLine4 = "sBillToAddressLine4"; //60) default NULL,
	public static String sBillToCity = "sBillToCity"; //30) default NULL,
	public static String sBillToState = "sBillToState"; //30) default NULL,
	public static String sBillToZip = "sBillToZip"; //20) default NULL,
	public static String sBillToCountry = "sBillToCountry"; //30) default NULL,
	public static String sBillToPhone = "sBillToPhone"; //30) default NULL,
	public static String sBillToFax = "sBillToFax"; //30) default NULL,
	public static String sBillToContact = "sBillToContact"; //60) default NULL,
	public static String sShipToCode = "sShipToCode"; //6) default NULL,
	public static String sShipToName = "sShipToName"; //60) default NULL,
	public static String sShipToAddress1 = "sShipToAddress1"; //60) default NULL,
	public static String sShipToAddress2 = "sShipToAddress2"; //60) default NULL,
	public static String sShipToAddress3 = "sShipToAddress3"; //60) default NULL,
	public static String sShipToAddress4 = "sShipToAddress4"; //60) default NULL,
	public static String sShipToCity = "sShipToCity"; //30) default NULL,
	public static String sShipToState = "sShipToState"; //30) default NULL,
	public static String sShipToZip = "sShipToZip"; //20) default NULL,
	public static String sShipToCountry = "sShipToCountry"; //30) default NULL,
	public static String sShipToPhone = "sShipToPhone"; //30) default NULL,
	public static String sShipToFax = "sShipToFax"; //30) default NULL,
	public static String sShipToContact = "sShipToContact"; //60) default NULL,
	public static String iCustomerDiscountLevel = "iCustomerDiscountLevel"; //11) NOT NULL default
	public static String sDefaultPriceListCode = "sDefaultPriceListCode"; //6) default NULL,
	public static String sPONumber = "sPONumber"; //22) default NULL,
	public static String sSpecialWageRate = "sSpecialWageRate"; //2) default NULL,
	public static String sTerms = "sTerms"; //6) default NULL,
	public static String iOrderType = "iOrderType"; //11) NOT NULL default '0',
	public static String datOrderDate = "datOrderDate"; //default NULL,
	public static String datExpectedShipDate = "datExpectedShipDate"; //default NULL,
	public static String datOrderCreationDate = "datOrderCreationDate"; //default NULL,
	public static String sServiceTypeCode = "sServiceTypeCode"; //6) default NULL,
	public static String sServiceTypeCodeDescription = "sServiceTypeCodeDescription"; //30) default
	public static String sLastInvoiceNumber = "sLastInvoiceNumber"; //15) default NULL,
	public static String iNumberOfInvoices = "iNumberOfInvoices"; //11) NOT NULL default '0',
	public static String sLocation = "sLocation"; //6) default NULL,
	public static String iOnHold = "iOnHold"; //11) NOT NULL default '0',
	public static String datLastPostingDate = "datLastPostingDate"; // default NULL,
	public static String dTotalAmountItems = "dTotalAmountItems"; // NOT NULL default '0',
	public static String iNumberOfLinesOnOrder = "iNumberOfLinesOnOrder"; //11) NOT NULL default
	public static String sSalesperson = "sSalesperson"; //8) default NULL,
	public static String sOrderCreatedByFullName = "sOrderCreatedByFullName"; //8) default NULL,
	public static String lOrderCreatedByID = "lOrderCreatedByID"; //8) default NULL,
	public static String staxjurisdiction = "staxjurisdiction"; //12) default NULL,
	public static String sDefaultItemCategory = "sDefaultItemCategory"; //6) default NULL,
	public static String bdtaxbase = "bdtaxbase"; // TJR - 1/21/2016 - changed name and type
	public static String bdordertaxamount = "bdordertaxamount"; // TJR - 1/21/2016 - changed name and type
	public static String iNextDetailNumber = "iNextDetailNumber"; //11) NOT NULL default '0',
	public static String mInternalComments = "mInternalComments"; //` text,
	public static String mInvoiceComments = "mInvoiceComments";//` text,
	public static String mTicketComments = "mTicketComments"; //` text,
	public static String dPrePostingInvoiceDiscountPercentage = "dPrePostingInvoiceDiscountPercentage"; // NOT
	public static String dPrePostingInvoiceDiscountAmount = "dPrePostingInvoiceDiscountAmount"; // NOT NUL
	public static String LASTEDITUSERFULLNAME = "LASTEDITUSERFULLNAME"; //128) default NULL,
	public static String LASTEDITUSERID = "LASTEDITUSERID"; //128) default NULL,
	public static String LASTEDITPROCESS = "LASTEDITPROCESS"; //50) default NULL,
	public static String LASTEDITDATE = "LASTEDITDATE"; //11) NOT NULL default '0',
	public static String LASTEDITTIME = "LASTEDITTIME"; //11) NOT NULL default '0',
	public static String sCustomerControlAcctSet = "sCustomerControlAcctSet"; //6) default NULL
	public static String sPrePostingInvoiceDiscountDesc = "sPrePostingInvoiceDiscountDesc"; //255) def
	public static String datOrderCanceledDate = "datOrderCanceledDate"; // default NULL,
	public static String mDirections = "mDirections"; //` text,
	public static String iOrderSourceID = "iOrderSourceID"; //9) default '0',
	public static String sOrderSourceDesc = "sOrderSourceDesc"; //255) default NULL,
	public static String dEstimatedHour = "dEstimatedHour"; //NOT NULL default '0',
	//public static String datCompletedDate = "datCompletedDate"; //datetime, NULL
	public static String sEmailAddress = "sEmailAddress"; //80) 
	public static String iSalesGroup = "iSalesGroup"; // int
	public static String bdtruckdays = "bdtruckdays";
	public static String scarpenterrate = "scarpenterrate";
	public static String slaborerrate = "slaborerrate";
	public static String selectricianrate = "selectricianrate";
	public static String bdtotalmarkup = "bdtotalmarkup";
	public static String bdtotalcontractamount = "bdtotalcontractamount";
	public static String datwarrantyexpiration = "datwarrantyexpiration";
	public static String swagescalenotes = "swagescalenotes";
	public static String ssecondarybilltophone = "ssecondarybilltophone";
	public static String ssecondaryshiptophone = "ssecondaryshiptophone";
	public static String mFieldNotes = "mFieldNotes"; //10/2/2014 - TJR - this can be removed at some point
	public static String sclonedfrom = "sclonedfrom";
	public static String strimmedordernumber = "strimmedordernumber";
	public static String sgeocode = "sgeocode";
	public static String sshiptoemail = "sshiptoemail";
	public static String sgdoclink = "sgdoclink";
	public static String bddepositamount = "bddepositamount";
	public static String lbidid = "lbidid";
	public static String squotedescription = "squotedescription";
	public static String itaxid = "itaxid";
	public static String staxtype = "staxtype";
	public static String sinvoicingemail = "sinvoicingemail";
	public static String sinvoicingcontact = "sinvoicingcontact";
	public static String sinvoicingnotes = "sinvoicingnotes";
	public static String idoingbusinessasaddressid = "idoingbusinessasaddressid";
	public static String mAddressNotes = "mAddressNotes"; //` text,
	
	//Field Lengths:
	public static int sOrderNumberLength = 22;
	public static int sOrderNumberPaddedLength = 8; //In the database, the padded length of the order number is always 8
	public static int sCustomerCodeLength = 12;
	public static int sBillToNameLength = 60;
	public static int sBillToAddressLine1Length = 60;
	public static int sBillToAddressLine2Length = 60;
	public static int sBillToAddressLine3Length = 60;
	public static int sBillToAddressLine4Length = 60;
	public static int sBillToCityLength = 30;
	public static int sBillToStateLength = 30;
	public static int sBillToZipLength = 20;
	public static int sBillToCountryLength = 30;
	public static int sBillToPhoneLength = 30;
	public static int sBillToFaxLength = 30;
	public static int sBillToContactLength = 60;
	public static int sShipToCodeLength = 6;
	public static int sShipToNameLength = 60;
	public static int sShipToAddress1Length = 60;
	public static int sShipToAddress2Length = 60;
	public static int sShipToAddress3Length = 60;
	public static int sShipToAddress4Length = 60;
	public static int sShipToCityLength = 30;
	public static int sShipToStateLength = 30;
	public static int sShipToZipLength = 20;
	public static int sShipToCountryLength = 30;
	public static int sShipToPhoneLength = 30;
	public static int sShipToFaxLength = 30;
	public static int sShipToContactLength = 60;
	public static int sDefaultPriceListCodeLength = 6;
	public static int sPONumberLength = 40;
	public static int sSpecialWageRateLength = 2;
	public static int sTermsLength = 6;
	public static int sOrderFiscalYearLength = 4;
	public static int sServiceTypeCodeLength = 6;
	public static int sServiceTypeCodeDescriptionLength = 30;
	public static int sLastInvoiceNumberLength = 15;
	public static int sLocationLength = 6;
	public static int sSalespersonLength = 8;
	public static int sOrderCreatedByLength = 128;
	public static int staxjurisdictionLength = 12;
	//public static int sTaxAuthorityLength = 12; //TJR - removed 12/10/2012
	public static int sDefaultItemCategoryLength = 6;
	public static int LASTEDITUSERLength = 128;
	public static int LASTEDITPROCESSLength = 50;
	public static int LASTEDITDATELength = 11;
	public static int LASTEDITTIMELength = 11;
	public static int sCustomerControlAcctSetLength = 6;
	public static int sPrePostingInvoiceDiscountDescLength = 255;
	public static int sOrderSourceDescLength = 255;
	public static int sEmailAddressLength = 80;
	public static int scarpenterrateLength = 50;
	public static int slaborerrateLength = 50;
	public static int selectricianrateLength = 50;
	public static int swagescalenotesLength = 255;
	public static int ssecondarybilltophoneLength = 20;
	public static int ssecondaryshiptophoneLength = 20;
	
	public static int bdtruckdaysScale = 4;
	public static int bddepositamountScale = 2;
	public static int bdtotalcontractamountScale = 2;
	public static int bdtotalmarkupScale = 2;
	public static int bdtotalamountitemsScale = 2;
	public static int bdPrePostingInvoicePercentageScale = 4;
	public static int bdPrePostingInvoiceAmountScale = 2;
	public static int bdTaxBaseScale = 2;
	public static int bdOrderTaxAmountScale = 2;
	public static int sclonedfromlength = 22;
	public static int strimmedordernumberLength = 22;
	public static int sgeocodeLength = 64;
	public static int sshiptoemailLength = 80;
	public static int squotedescriptionLength = 254;
	public static int staxtypeLength = 254;
	public static int sInvoicingEmailLength = 128;
	public static int sInvoicingContactLength = 128;
	
	public static String getOrderTypeDescriptions(int iOrderType){
		switch (iOrderType){
		case ORDERTYPE_ACTIVE:
			return "Active";
		case ORDERTYPE_FUTURE:
			return "Future";
		case ORDERTYPE_STANDING:
			return "Standing";
		case ORDERTYPE_QUOTE:
			return "Quote";
		default:
			return "Active";
		}
	}

}
