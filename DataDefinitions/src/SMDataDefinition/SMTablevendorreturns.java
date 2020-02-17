package SMDataDefinition;

public class SMTablevendorreturns {
	public static final String TableName = "vendorreturns";

	public static final String lid = "lid";
	public static final String iinvoiceonhold = "iinvoiceonhold";
	public static final String itobereturned = "itobereturned";
	public static final String iCreditDue = "iCreditDue";
	public static final String svendoracct = "svendoracct";
	public static final String iponumber = "iponumber";
	public static final String mVendorComments = "mVendorComments";
	public static final String datreturnsent = "datreturnsent";
	public static final String ladjustedbatchnumber = "ladjustedbatchnumber";
	public static final String ladjustedentrynumber = "ladjustedentrynumber";
	public static final String bdadjustmentamount = "bdadjustmentamount";
	public static final String screditmemonumber = "screditmemonumber";
	public static final String datcreditnotedate = "datcreditnotedate";
	public static final String bdcreditamt = "bdcreditamt";
	public static final String icreditnotexpected = "icreditnotexpected";

	//Lengths
	public static final int svendoracctlength = 12; 
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
