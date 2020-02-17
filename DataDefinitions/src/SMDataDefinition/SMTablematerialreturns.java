package SMDataDefinition;

public class SMTablematerialreturns {
	public static final String TableName = "materialreturns";

	public static final String lid = "lid";
	public static final String datinitiated = "datinitiated";
	public static final String linitiatedbyid = "linitiatedbyid";
	public static final String sinitiatedbyfullname = "sinitiatedbyfullname";
	public static final String iresolved = "iresolved";
	public static final String datresolved = "datresolved";
	public static final String lresolvedbyid = "lresolvedbyid";
	public static final String sresolvedbyfullname = "sresolvedbyfullname";
	public static final String sdescription = "sdescription";
	public static final String mcomments = "mcomments";
	
	public static final String mresolutioncomments = "mresolutioncomments"; 
	
	//TODO Be put into Vendor Returns
	public static final String iworkorderid = "iworkorderid";
	public static final String strimmedordernumber = "strimmedordernumber";
	public static final String icreditnotexpected = "icreditnotexpected";
	public static final String iponumber = "iponumber";
	// TJR - Added these two fields 2/22/2019:
	public static final String itobereturned = "itobereturned";
	public static final String svendoracct = "svendoracct";
	// TJR - Added these fields 9/26/2019:
	public static final String ladjustedbatchnumber = "ladjustedbatchnumber"; //AdjustedBatch#
	public static final String ladjustedentrynumber = "ladjustedentrynumber"; //Adjusted Entry Number 
	public static final String bdadjustmentamount = "bdadjustmentamount"; //Adjusted Credit Amount
	public static final String datcreditnotedate = "datcreditnotedate";
	public static final String screditmemonumber = "screditmemonumber";
	public static final String bdcreditamt = "bdcreditamt"; //Actual Credit Amount
	public static final String datreturnsent = "datreturnsent";
	public static final String iinvoiceonhold = "iinvoiceonhold";
	public static final String mVendorComments = "mVendorComments";
	public static final String iCreditDue = "iCreditDue"; //MiscCreditDue

	//Lengths
	public static final int linitiatedbyidlength = 11;
	public static final int sinitiatedbyfullnamelength = 128;
	public static final int lresolvedbyidlength = 11;
	public static final int sresolvedbyfullnamelength = 128;
	public static final int sdescriptionlength = 254;
	public static final int strimmedordernumberlength = 22;
	
	public static final int svendoracctlength = 12;  //Same as icvendors.svendoracctLength
	public static final int ladjustedbatchnumberlength = 11;
	public static final int lentrynumberlength = 11;
	public static final int bdadjustmentamountlength = 19;
	public static final int screditmemonumberlength = 24;
	public static final int bdcreditamtlength = 19;
	
	
	//Labels
	public static final String sidlabel = "Material Return ID";
	public static final String sinitiatedlabel = "Initiated by";
	public static final String sresolvedlabel = "Resolved";
	public static final String sdescriptionlabel = "Description";
	public static final String scommentslabel = "Comments";
	public static final String sresolutioncommentslabel = "Resolution Comments";
	public static final String sworkorderidlabel = "Work order ID";
	public static final String sordernumberlabel = "Order number";
	public static final String screditnotexpectedlabel = "Credit is no longer Expected";
	public static final String sponumberlabel = "PO Number";
	public static final String stobereturnedlabel = "To be returned";
	public static final String svendoracctlabel = "Vendor";
	public static final String sadjustedbatchnumberlabel = "Adjustment Batch Number"; 
	public static final String sadjustedentrynumberlabel = "Adjustment Entry Number";
	public static final String sadjustmentamountlabel = "Adjustment Amount"; 
	public static final String screditnotedatelabel = "Date of Credit Memo";
	public static final String screditmemonumberlabel = "Credit Memo Number";
	public static final String screditamtlabel = "Credit Received";
	public static final String sreturnsentlabel = "Date Returned";
	public static final String sinvoiceonholdlabel = "AP Invoice Was Put on Hold";
	public static final String sVendorCommentsLabel = "Comments";
	public static final String sCreditDueLabel = "Miscellaneous Credit Due?";

	public static int STATUS_CREDITNOTEXPECTED = 1;
	public static int STATUS_CREDITNOTDUE = 0;
	public static int STATUS_CREDITDUE = 1;

}