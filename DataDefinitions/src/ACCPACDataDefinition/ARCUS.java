package ACCPACDataDefinition;

public class ARCUS {

	public static String TableName = ACCPACTables.AR_CUSTOMERS;
	//Field names:
	public static String adStartDate = "DATESTART";
    public static String adLastInvoiceDate = "DATELASTIV";
    public static String adLastPaymentDate = "DATELASTPA";
    public static String dAmountPastDue = "AMTPDUE";
    public static String dNumberOfDaysToPay = "DAYSTOPAY";
    public static String dHighBalanceAmountFuncCurrency = "AMTBALHIH";
    public static String dCreditLimitAmountCustCurrency = "AMTCRLIMT";
    public static String dBalanceDueCustCurrency = "AMTBALDUET";
    public static String dHighBalanceLastYearFuncCurrency = "AMTBALHILH";
    public static String dLargestInvoiceFuncCurrency = "AMTINVHIH";
    public static String dLargestInvoiceLastYearFuncCurrency = "AMTINVHILH";
    public static String dLargestCreditNoteFuncCurrency = "AMTLASTCRH";
    public static String adLastCreditNoteDate = "DATELASTCR";
    public static String dLastInvoiceAmtFuncCurrency = "AMTLASTIVH";
    public static String dLastReceiptAmtFuncCurrenct = "AMTLASTPYH";
    public static String dNumberOfOpenDocuments = "CNTOPENINV";
    public static String dNumberOfPaidInvoices = "CNTINVPAID";
    public static String sCustomerNumber = "IDCUST";
    public static String sCustomerGroup = "IDGRP";
    public static String iOnHold = "SWHOLD";
    public static String iCustomerDiscountType = "CUSTTYPE";
    public static String iTaxClassCode1 = "TAXSTTS1";
    public static String sCustomerName = "NAMECUST";
    public static String sAddressLine1 = "TEXTSTRE1";
    public static String sAddressLine2 = "TEXTSTRE2";
    public static String sAddressLine3 = "TEXTSTRE3";
    public static String sAddressLine4 = "TEXTSTRE4";
    public static String sCity = "NAMECITY";
    public static String sState = "CODESTTE";
    public static String sZipCode = "CODEPSTL";
    public static String sCountry = "CODECTRY";
    public static String sContact = "NAMECTAC";
    public static String sPhone1 = "TEXTPHON1";
    public static String sPhone2 = "TEXTPHON2";
    public static String sAccountSet = "IDACCTSET";
    public static String sTermsCode = "CODETERM";
    public static String sTaxGroup = "CODETAXGRP";
    public static String sSalesperson1 = "CODESLSP1";
    public static String sPriceListCode = "PRICLIST";
    public static String sShipViaCode = "SHPVIACODE";
    public static String sPrimaryShipTo = "PRIMSHIPTO";

  	//Field lengths:
    public static int sCustomerNumberLength = 12;
    public static int sCustomerGroupLength = 6;
    public static int sCustomerNameLength = 30;
    public static int sAddressLine1Length = 30;
    public static int sAddressLine2Length = 30;
    public static int sAddressLine3Length = 30;
    public static int sAddressLine4Length = 30;
    public static int sCityLength = 20;
    public static int sStateLength = 20;
    public static int sZipCodeLength = 15;
    public static int sCountryLength = 20;
    public static int sContactLength = 30;
    public static int sPhone1Length = 21;
    public static int sPhone2Length = 21;
    public static int sAccountSetLength = 6;
    public static int sTermsCodeLength = 6;
    public static int sTaxGroupLength = 6;
    public static int sSalesperson1Length = 8;
    public static int sPriceListCodeLength = 6;
    public static int sShipViaCodeLength = 6;
    public static int sPrimaryShipToLength = 6;

	
}
