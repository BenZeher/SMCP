package SMDataDefinition;

public class SMTableapchecklines {
	public static final String TableName = "apchecklines";
	
	public static final String lid = "lid";
	public static final String lcheckid = "lcheckid";
	public static final String lchecklinenumber = "lchecklinenumber";
	public static final String bdgrossamount = "bdgrossamount";  //This is the total amount payable on the invoice
	public static final String bddiscounttaken = "bddiscounttaken";  //Discount taken on this check line
	public static final String bdnetpaid = "bdnetpaid";  //The net amount actually paid - the total of these equals the check amount
														 //This is also equal to the amount paid on each line of the batch entry
	public static final String lbatchnumber = "lbatchnumber";
	public static final String lentrynumber = "lentrynumber";
	public static final String lentrylinenumber = "lentrylinenumber";
	public static final String sapplytdocnumber = "sapplytdocnumber";
	public static final String datapplydocdate = "datapplydocdate";
	

	public static final int sapplytdocnumberLength = 75;
	public static final int bdgrossamountScale = 2;
	public static final int bddiscounttakenScale = 2;
	public static final int bdnetpaidScale = 2;
}
