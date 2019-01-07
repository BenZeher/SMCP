package SMDataDefinition;

public class SMTableaptransactionlines {

	public static final String TableName = "aptransactionlines";
	
	//Field names
	public static final String lid = "lid";
	public static final String ltransactionheaderid = "ltransactionheaderid";
	public static final String loriginalbatchnumber = "loriginalbatchnumber";
	public static final String loriginalentrynumber = "loriginalentrynumber";
	public static final String loriginallinenumber = "loriginallinenumber";
	public static final String bdamount = "bdamount";
	public static final String bddiscountappliedamount = "bddiscountappliedamount";
	public static final String sdistributioncodename = "sdistributioncodename";
	public static final String sdistributionacct = "sdistributionacct";
	public static final String sdescription = "sdescription";
	public static final String scomment = "scomment";
	public static final String lpoheaderid = "lpoheaderid";
	public static final String lreceiptheaderid = "lreceiptheaderid";
	public static final String lporeceiptlineid = "lporeceiptlineid";
	public static final String lapplytodocid = "lapplytodocid";
	public static final String sapplytodocnumber = "sapplytodocnumber";
	public static final String sunitofmeasure = "sunitofmeasure";
	public static final String sitemnumber = "sitemnumber";
	public static final String bdqtyreceived = "bdqtyreceived";
	public static final String sitemdescription = "sitemdescription";
	
	
	//Field lengths:
	public static final int sdistributioncodenameLength = 32;
	public static final int sdistributionacctLength = 75;
	public static final int sdescriptionLength = 96;
	public static final int scommentLength = 254;
	public static final int bdamountScale = 2;
	public static final int sapplytodocnumberLength = 75;
	public static final int sitemnumbernumberLength = 24;
	public static final int sunitofmeasureLength = 10;
	public static final int sitemdescriptionLength = 75;

}
