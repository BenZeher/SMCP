package SMDataDefinition;

public class SMTableinvoiceheaders {
	public static final String TableName = "invoiceheaders";
	
	public static final String datInvoiceDate = "datInvoiceDate";
	public static final String dDiscountAmount = "dDiscountAmount";
	public static final String bdsalestaxamount = "bdsalestaxamount";  //TJR - 2/4/2016 - renamed
	public static final String iCustomerDiscountLevel = "iCustomerDiscountLevel";
	public static final String iNumberOfLinesOnInvoice = "iNumberOfLinesOnInvoice";
	public static final String iRequisitionDueDay = "iRequisitionDueDay";
	public static final String mInvoiceComments = "mInvoiceComments";
	public static final String sBillToAddressLine1 = "sBillToAddressLine1";
	public static final String sBillToAddressLine2 = "sBillToAddressLine2";
	public static final String sBillToAddressLine3 = "sBillToAddressLine3";
	public static final String sBillToAddressLine4 = "sBillToAddressLine4";
	public static final String sBillToCity = "sBillToCity";
	public static final String sBillToContact = "sBillToContact";
	public static final String sBillToCountry = "sBillToCountry";
	public static final String sBillToFax = "sBillToFax";
	public static final String sBillToName = "sBillToName";
	public static final String sBillToPhone = "sBillToPhone";
	public static final String sBillToState = "sBillToState";
	public static final String sBillToZip = "sBillToZip";
	public static final String sCustomerCode = "sCustomerCode";
	public static final String sDefaultPriceListCode = "sDefaultPriceListCode";
	public static final String sInvoiceNumber = "sInvoiceNumber";
	public static final String sLocation = "sLocation";
	public static final String sOrderNumber = "sOrderNumber";
	public static final String sPONumber = "sPONumber";
	public static final String sSalesperson = "sSalesperson";
	public static final String sServiceTypeCode = "sServiceTypeCode";
	public static final String sServiceTypeCodeDescription = "sServiceTypeCodeDescription";
	public static final String sShipToAddress1 = "sShipToAddress1";
	public static final String sShipToAddress2 = "sShipToAddress2";
	public static final String sShipToAddress3 = "sShipToAddress3";
	public static final String sShipToAddress4 = "sShipToAddress4";
	public static final String sShipToCity = "sShipToCity";
	public static final String sShipToCode = "sShipToCode";
	public static final String sShipToContact = "sShipToContact";
	public static final String sShipToCountry = "sShipToCountry";
	public static final String sShipToFax = "sShipToFax";
	public static final String sShipToName = "sShipToName";
	public static final String sShipToPhone = "sShipToPhone";
	public static final String sShipToState = "sShipToState";
	public static final String sShipToZip = "sShipToZip";
	//public static final String sTaxAuthority = "sTaxAuthority";  //TJR - removed this on 12/10/2012
	public static final String sTaxExemptNumber = "sTaxExemptNumber";
	public static final String staxjurisdiction = "staxjurisdiction"; //TJR - 2/4/2016 - change named from 'sTaxGroup'
	public static final String sTerms = "sTerms";
	public static final String dDiscountPercentage = "dDiscountPercentage";
	public static final String sDesc = "sDesc";
	public static final String sCustomerControlAcctSet = "sCustomerControlAcctSet";
	public static final String datDueDate = "datDueDate";
	public static final String datTermsDiscountDate = "datTermsDiscountDate";
	public static final String dTermsDiscountPercentage = "dTermsDiscountPercentage";
	public static final String dTermsDiscountAvailable = "dTermsDiscountAvailable";
	public static final String iExportedToAR = "iExportedToAR";
	public static final String iDayEndNumber = "iDayEndNumber";
	public static final String datOrderDate = "datOrderDate";
	public static final String bdtaxrate = "bdtaxrate";  //TJR - 1/21/2016 - renamed
	public static final String bdsalestaxbase = "bdsalestaxbase";  //TJR - 2/4/2016 - renamed
	public static final String dPrePayment = "dPrePayment";
	public static final String sDiscountDesc = "sDiscountDesc";
	public static final String iTransactionType = "iTransactionType";
	public static final String sMatchingInvoiceNumber = "sMatchingInvoiceNumber";
	public static final String iIsCredited = "iIsCredited";
	public static final String iExportedToIC = "iExportedToIC";
	public static final String sCreatedByFullName = "sCreatedByFullName";
	public static final String lCreatedByID= "lCreatedByID";
	public static final String datInvoiceCreationDate = "datInvoiceCreationDate";
	public static final String iOrderSourceID = "iOrderSourceID";
	public static final String sOrderSourceDesc = "sOrderSourceDesc";
	public static final String iSalesGroup = "iSalesGroup";
	public static final String strimmedordernumber = "strimmedordernumber";
	//TJR - 2/4/2016 - added these fields:
	public static final String itaxid = "itaxid";
	public static final String staxtype = "staxtype";
	public static final String icalculatetaxonpurchaseorsale = "icalculatetaxonpurchaseorsale";
	public static final String icalculatetaxoncustomerinvoice = "icalculatetaxoncustomerinvoice";

	public static final String iinvoicingstate = "iinvoicingstate";
	public static final String sdbalogo = "sdbalogo"; 
	public static final String sdbadescription = "sdbadescription";
	public static final String mdbaaddress = "mdbaaddress";
	public static final String mdbaremittoaddress= "mdbaremittoaddress";
	public static final String sdbainvoicelogo = "sdbainvoicelogo";
	//Field lengths:
	public static final int sdbainvoicelogoLength = 128;
	public static final int sBillToAddressLine1length = 60;
	public static final int sBillToAddressLine2length = 60;
	public static final int sBillToAddressLine3length = 60;
	public static final int sBillToAddressLine4length = 60;
	public static final int sBillToCitylength = 30;
	public static final int sBillToContactlength = 60;
	public static final int sBillToCountrylength = 30;
	public static final int sBillToFaxlength = 30;
	public static final int sBillToNamelength = 60;
	public static final int sBillToPhonelength = 30;
	public static final int sBillToStatelength = 30;
	public static final int sBillToZiplength = 20;
	public static final int sCustomerCodelength = 12;
	public static final int sDefaultPriceListCodelength = 6;
	public static final int sInvoiceNumberlength = 15;
	public static final int sLocationlength = 6;
	public static final int sOrderNumberlength = 22;
	public static final int sPONumberlength = 40;
	public static final int sSalespersonlength = 8;
	public static final int sServiceTypeCodelength = 6;
	public static final int sServiceTypeCodeDescriptionlength = 30;
	public static final int sShipToAddress1length = 60;
	public static final int sShipToAddress2length = 60;
	public static final int sShipToAddress3length = 60;
	public static final int sShipToAddress4length = 60;
	public static final int sShipToCitylength = 30;
	public static final int sShipToCodelength = 6;
	public static final int sShipToContactlength = 60;
	public static final int sShipToCountrylength = 30;
	public static final int sShipToFaxlength = 30;
	public static final int sShipToNamelength = 60;
	public static final int sShipToPhonelength = 30;
	public static final int sShipToStatelength = 30;
	public static final int sShipToZiplength = 20;
	//public static final int sTaxAuthoritylength = 12;  //TJR - removed this on 12/10/2012
	public static final int sTaxExemptNumberlength = 20;
	public static final int staxjurisdictionlength = 12;
	public static final int sTermslength = 6;
	public static final int sDesclength = 30;
	public static final int sCustomerControlAcctSetlength = 6;
	public static final int sDiscountDesclength = 255;
	public static final int sMatchingInvoiceNumberlength = 15;
	public static final int sCreatedBylength = 128;
	public static final int sOrderSourceDesclength = 255;
	public static final int strimmedordernumberLength = 22;
	public static final int staxtypeLength = 254;
	public static final int bdtaxamountscale = 2;
	public static final int bdtaxratescale = 2;
	public static final int bdtaxbasescale = 2;
	
	public static final int TYPE_INVOICE = 0;
	public static final int TYPE_CREDIT = 1;
	
	public static final int NUMBER_OF_INVOICING_STATES = 3;
	public static final int INVOICING_STATE_NOT_ELIGIBLE = 0;
	public static final int INVOICING_STATE_ELIGIBLE = 1;
	public static final int INVOICING_STATE_SENT = 2;
	
	//Parameters to determine the size of the invoice logo on emails:
	public static final String EMAILED_LOGO_WIDTH = "300";
	public static final String EMAILED_LOGO_HEIGHT = "96";
	
	public static String getInvoicingStateDescription(int iState){
		switch(iState){
		case INVOICING_STATE_ELIGIBLE:
			return "Eligible to be sent";
		case INVOICING_STATE_SENT:
			return "Sent";
		case INVOICING_STATE_NOT_ELIGIBLE:
			return "Not eligible to be sent";
		default:
			return "Not eligible to be sent";
		}
	}
}
