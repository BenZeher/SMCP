package SMDataDefinition;

public class SMTablearmatchingline {
	//Table Name
	public static final String TableName = "armatchinglines";
	
	//Field names:
	public static final String lid  = "id";
	public static final String spayeepayor = "spayeepayor";
	public static final String sdocnumber = "sdocnumber";
	public static final String sapplytodoc = "sapplytodoc";
	public static final String ldocappliedtoid = "ldocappliedtoid";
	public static final String damount = "damount";
	public static final String sdescription = "sdescription";
	public static final String dattransactiondate = "dattransactiondate";
	public static final String lparenttransactionid = "lparenttransactionid";
	
	//If the DOCUMENT APPLIED TO is flagged as retainage, then the armatchingline is ALSO flagged as retainage:
	public static final String iretainage = "iretainage";
	
	//Field lengths:
	public static final int spayeepayorlength = 12;
	public static final int sdocnumberlength = 75;
	public static final int sdescriptionlength = 75;
	public static final int sapplytodoclength = 75;
}
