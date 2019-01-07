package SMDataDefinition;

public class SMTableapbatchentrylines {
	//Table Name
	public static final String TableName = "apbatchentrylines";
	
	//Field names:
	public static final String lid = "lid";
	public static final String lbatchnumber = "lbatchnumber";
	public static final String lentrynumber = "lentrynumber";
	public static final String llinenumber = "llinenumber";
	public static final String sdistributioncodename = "sdistributioncodename";
	public static final String bdamount = "bdamount";
	public static final String sdistributionacct = "sdistributionacct";
	public static final String sdescription = "sdescription";
	public static final String scomment = "scomment";
	public static final String lpoheaderid = "lpoheaderid";
	public static final String lreceiptheaderid = "lreceiptheaderid";
	public static final String lapplytodocid = "lapplytodocid";
	public static final String sapplytodocnumber = "sapplytodocnumber";
	public static final String bdapplieddiscountamt = "bdapplieddiscountamt";
	public static final String iapplytodoctype = "iapplytodoctype";
	public static final String lporeceiptlineid = "lporeceiptlineid";
	public static final String bdpayableamount = "bdpayableamount";  //The total amount payable on the apply-to invoice at the time 
																	//the batch line is created/posted
	
	//Field lengths:
	public static final int sdistributioncodenameLength = 32;
	public static final int sdistributionacctLength = 75;
	public static final int sdescriptionLength = 96;
	public static final int scommentLength = 254;
	public static final int bdamountScale = 2;
	public static final int bdapplieddiscountamtScale = 2;
	public static final int sapplytodocnumberLength = 75;
	public static final int bdpayableamountScale = 2;
	
	//NOTE: in invoices, the amounts are POSITIVE, and in credits, the amounts are NEGATIVE.  
	//This reflects how the amounts affect the AP Payables control account:
	// an invoice would CREDIT the account (i.e., increase the value of it) and
	// a credit would DEBIT the account (i.e. decrease the value of it)
	
	//Apply to document types
	public static final int APPLY_TO_DOC_TYPE_INVOICENUMBER = 0;
	public static final int APPLY_TO_DOC_TYPE_PONUMBER = 1;
	public static final int APPLY_TO_DOC_TYPE_ORDERNUMBER = 2;
	
	public static String getApplyToDocumentTypeLabel(int iApplyToDocType){
		
		switch (iApplyToDocType){
			case APPLY_TO_DOC_TYPE_INVOICENUMBER:
				return "Invoice number";
			case APPLY_TO_DOC_TYPE_PONUMBER:
				return "PO number";
			case APPLY_TO_DOC_TYPE_ORDERNUMBER:
				return "Order number";
			default:
				return "Invoice number";
		}
	}
}