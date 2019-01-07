package SMDataDefinition;

public class SMTableicbatchentries {
	//Table Name
	public static final String TableName = "icbatchentries";
	
	//Field names:
	public static final String lid  = "lid";
	public static final String lbatchnumber  = "lbatchnumber";
	public static final String lentrynumber = "lentrynumber";
	public static final String ientrytype = "ientrytype";
	public static final String sdocnumber = "sdocnumber"; //invoice number, po number, adjustment number
	public static final String sentrydescription = "sentrydescription";
	public static final String datentrydate = "datentrydate";
	public static final String llastline = "llastline";
	public static final String bdentryamount = "bdentryamount";
	
	//Field lengths:
	public static final int sdocnumberLength = 75;
	public static final int sentrydescriptionLength = 128;
}