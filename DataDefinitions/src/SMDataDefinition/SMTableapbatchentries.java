package SMDataDefinition;

public class SMTableapbatchentries {
	//Table Name
	public static final String TableName = "apbatchentries";
	
	//Field names:
	public static final String lid  = "lid";
	public static final String lbatchnumber  = "lbatchnumber";
	public static final String lentrynumber = "lentrynumber";
	public static final String ientrytype = "ientrytype";
	public static final String sdocnumber = "sdocnumber"; //invoice number, check number, adjustment number
	public static final String sentrydescription = "sentrydescription";
	public static final String datentrydate = "datentrydate";
	public static final String llastline = "llastline";
	public static final String bdentryamount = "bdentryamount";
	public static final String scontrolacct = "scontrolacct";
	public static final String svendoracct = "svendoracct";
	public static final String datdocdate = "datdocdate";
	public static final String datdiscount = "datdiscount";
	public static final String datduedate = "datduedate";
	public static final String sterms = "sterms";
	public static final String bddiscount = "bddiscount";  //This is only meaningful on invoice documents, payments, etc., get no 'discount amt'
	public static final String svendorname = "svendorname";
	public static final String staxjurisdiction = "staxjurisdiction";
	public static final String itaxid = "itaxid";
	public static final String bdtaxrate = "bdtaxrate";
	public static final String staxtype = "staxtype";
	public static final String icalculateonpurchaseorsale = "icalculateonpurchaseorsale";
	public static final String lsalesordernumber = "lsalesordernumber";
	public static final String lpurchaseordernumber = "lpurchaseordernumber";
	public static final String sapplytoinvoicenumber = "sapplytoinvoicenumber";
	
	//These additional fields are needed for payment batches:
	public static final String schecknumber = "schecknumber";
	public static final String sremittocode = "sremittocode";
	public static final String sremittoname	= "sremittoname";
	public static final String sremittoaddressline1	= "sremittoaddressline1"; 	 	 
	public static final String sremittoaddressline2	= "sremittoaddressline2"; 
	public static final String sremittoaddressline3	= "sremittoaddressline3"; 
	public static final String sremittoaddressline4	= "sremittoaddressline4"; 
	public static final String sremittocity	= "sremittocity";
	public static final String sremittostate = "sremittostate"; 	 
	public static final String sremittopostalcode = "sremittopostalcode";
	public static final String sremittocountry = "sremittocountry";
	
	public static final String ionhold = "ionhold";
	
	public static final String lbankid = "lbankid";
	
	public static final String iprintcheck = "iprintcheck";
	public static final String iprintingfinalized = "iprintingfinalized";
	
	public static final String iinvoiceincludestax = "iinvoiceincludestax";
	
	//Field lengths:
	public static final int sdocnumberLength = 75;
	public static final int sentrydescriptionLength = 128;
	public static final int bdentryamountScale = 2;
	public static final int scontrolacctLength = 75;
	public static final int svendoracctLength = SMTableicvendors.svendoracctLength;
	public static final int stermsLength = SMTableicvendorterms.sTermsCodeLength;
	public static final int bddiscountScale = 2;
	public static final int svendornameLength = SMTableicvendors.snameLength;
	public static final int staxjurisdictionLength = 12;
	public static final int staxtypeLength = 254;
	public static final int bdtaxrateScale = 4;
	public static final int sapplytodocnumberLength = 75;
	public static final int schecknumberLength = 12;
	public static final int sremittocodeLength = 12;
	public static final int sremittonameLength = 60;	 	 
	public static final int sremittoaddressline1Length = 60;	 	 	 
	public static final int sremittoaddressline2Length = 60;	 	 	 
	public static final int sremittoaddressline3Length = 60;	 	 	 
	public static final int sremittoaddressline4Length = 60;	 	 	 
	public static final int sremittocityLength = 30;	 	 	 
	public static final int sremittostateLength = 30;	 	 	 
	public static final int sremittopostalcodeLength = 20;	 	 	 
	public static final int sremittocountryLength = 30;
	public static final int sapplytoinvoicenumberLength = 75;
	
	public static final int PAYMENT_DOCNUMBER_LENGTH = 12;
	public static final int REVERSAL_DOCNUMBER_LENGTH = 12;
	
	//Entry types:
	public static final int ENTRY_TYPE_INV_INVOICE = 0;
	public static final int ENTRY_TYPE_INV_DEBITNOTE = 1;
	public static final int ENTRY_TYPE_INV_CREDITNOTE = 2;
	//public static final int ENTRY_TYPE_INV_INTEREST = 3;  TJR - 5/12/2017 - Not using this
	
	public static final int ENTRY_TYPE_PAYMENT_PAYMENT = 4;
	public static final int ENTRY_TYPE_PAYMENT_PREPAYMENT = 5;
	public static final int ENTRY_TYPE_PAYMENT_APPLYTO = 6;
	public static final int ENTRY_TYPE_PAYMENT_MISCPAYMENT = 7;

	public static final int ENTRY_TYPE_REVERSAL = 8;
	
	public static final int NUMBER_OF_ENTRY_TYPES = 9;
	
	//public static final int ENTRY_TYPE_ADJUSTMENT = 9;  TJR - 5/12/2017 - Not using this
	
	public static final String DOC_NUMBER_PREFIX_PAYMENT = "PY";
	public static final String DOC_NUMBER_PREFIX_PREPAYMENT = "PP";
	public static final String DOC_NUMBER_PREFIX_APPLYTO = "AT";
	public static final String DOC_NUMBER_PREFIX_MISCPAYMENT = "MP";
	public static final String DOC_NUMBER_PREFIX_REVERSAL = "RV";
	
	//NOTE: in invoices, the amounts are POSITIVE, and in credits, the amounts are NEGATIVE.  
	//This reflects how the amounts affect the AP Payables control account:
	// an invoice would CREDIT the account (i.e., increase the value of it) and
	// a credit would DEBIT the account (i.e. decrease the value of it)
	
	public static String getDocumentTypeLabel (int iEntryType){
		
		switch(iEntryType){
			case ENTRY_TYPE_INV_INVOICE:
				return "Invoice";
			case ENTRY_TYPE_INV_DEBITNOTE:
				return "Debit Note";
			case ENTRY_TYPE_INV_CREDITNOTE:
				return "Credit Note";
				
			case ENTRY_TYPE_PAYMENT_PAYMENT:
				return "Payment";
			case ENTRY_TYPE_PAYMENT_PREPAYMENT:
				return "Prepayment";
			case ENTRY_TYPE_PAYMENT_APPLYTO:
				return "Apply to";
			case ENTRY_TYPE_PAYMENT_MISCPAYMENT:
				return "Misc payment";
				
			case ENTRY_TYPE_REVERSAL:
				return "Check reversal";
				
			default:
				return "Invoice";
		}
	}
	
	public static String getPaymentDocNumberPrefix (int iEntryType){
		
		switch(iEntryType){
		case ENTRY_TYPE_PAYMENT_PAYMENT:
			return DOC_NUMBER_PREFIX_PAYMENT;
		case ENTRY_TYPE_PAYMENT_PREPAYMENT:
			return DOC_NUMBER_PREFIX_PREPAYMENT;
		case ENTRY_TYPE_PAYMENT_APPLYTO:
			return DOC_NUMBER_PREFIX_APPLYTO;
		case ENTRY_TYPE_PAYMENT_MISCPAYMENT:
			return DOC_NUMBER_PREFIX_MISCPAYMENT;
		case ENTRY_TYPE_REVERSAL:
			return DOC_NUMBER_PREFIX_REVERSAL;
		default:
			return "N/A";		
		}
	}
}
