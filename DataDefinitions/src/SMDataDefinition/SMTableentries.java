package SMDataDefinition;

public class SMTableentries {
	//Table Name
	public static final String TableName = "entries";
	
	//Field names:
	public static final String lid  = "id";
	public static final String ibatchnumber  = "ibatchnumber";
	public static final String ientrynumber = "ientrynumber";
	public static final String idocumenttype = "idocumenttype";
	public static final String spayeepayor = "spayeepayor";
	public static final String sdocnumber = "sdocnumber";
	public static final String sdocdescription = "sdocdescription";
	public static final String datdocdate = "datdocdate";
	public static final String stermscode = "stermscode";
	public static final String datduedate = "datduedate";
	public static final String ilastline = "ilastline";
	public static final String scontrolacct = "scontrolacct";
	public static final String doriginalamount = "doriginalamount";
	public static final String sordernumber = "sordernumber";
	public static final String sentryponumber = "sentryponumber";
	
	//Field lengths:
	public static final int spayeepayorLength = 12;
	public static final int sdocnumberLength = 75;
	public static final int sdocdescriptionLength = 128;
	public static final int sdocappliedtoLength = 75;
	public static final int stermscodeLength = 6;
	public static final int scontrolacctLength = 75;
	public static final int sordernumberLength = 22;
	public static final int sentryponumberLength = 40;
}
