package SMDataDefinition;

public class SMTableapmatchinglines {
	//Table Name
	public static final String TableName = "apmatchinglines";
	
	//Field names:
	public static final String lid  = "lid";
	public static final String svendor = "svendor";
	public static final String bdappliedamount = "bdappliedamount";
	public static final String sdescription = "sdescription";
	public static final String dattransactiondate = "dattransactiondate";
	
	public static final String sappliedfromdocnumber = "sappliedfromdocnumber";
	public static final String sappliedtodocnumber = "sappliedtodocnumber";
	public static final String ltransactionappliedfromid = "ltransactionappliedfromid";
	public static final String ltransactionappliedtoid = "ltransactionappliedtoid";
	public static final String bddiscountappliedamount = "bddiscountappliedamount";
	
	//Field lengths:
	public static final int svendorlength = 12;
	public static final int sappliedfromdocnumberlength = 75;
	public static final int sappliedtodocnumberlength = 75;
	public static final int sdescriptionlength = 75;
	
	public static final int bdappliedamountScale = 2;
	public static final int bddiscountappliedamountScale = 2;
}

/*

AP 'matching' lines are used to record the application of transaction to each other.

The original amount of a transaction, MINUS the total of the matching lines that apply to it, equals the CURRENT AMT of the transaction.

So normally the SIGN of the matching lines that APPLY TO a transaction is the same as the original transaction:
If an invoice is entered and has a positive value, then a credit (or a payment) applied to that invoice will create a
matching line, with a positive amount, that 'applies to' that invoice.  The credit/payment will ALSO create a matching line
that applies to the credit/payment itself.  The credit/payment will normally have a NEGATIVE sign, and the matching line that
gets created, applying to the credit/payment itself will ALSO have a NEGATIVE sign.

The value of the transaction represents, when totaled, the value of the AP Payables control account, or the company's liability
to vendors.  So an invoice is POSITIVE, INCREASING that liability, and a credit/payment is NEGATIVE, DECREASING that liability.












*/