package SMDataDefinition;

public class SMTableicentrylines {
	//Table Name
	public static final String TableName = "icentrylines";
	
	//Field names:
	public static final String lid = "lid";
	public static final String lbatchnumber = "lbatchnumber";
	public static final String lentrynumber = "lentrynumber";
	public static final String llinenumber = "llinenumber";
	public static final String sitemnumber = "sitemnumber";
	public static final String slocation = "slocation";
	public static final String bdqty = "bdqty";
	public static final String bdcost = "bdcost";
	public static final String bdprice = "bdprice";
	public static final String scontrolacct = "scontrolacct";
	public static final String sdistributionacct = "sdistributionacct";
	public static final String sdescription = "sdescription";
	public static final String scomment = "scomment";
	public static final String lentryid = "lentryid";
	public static final String scategorycode = "scategorycode";
	public static final String sreceiptnum = "sreceiptnum";
	public static final String lcostbucketid = "lcostbucketid";
	public static final String stargetlocation = "stargetlocation";
	public static final String sinvoicenumber  = "sinvoicenumber";
	public static final String linvoicelinenumber = "linvoicelinenumber";
	public static final String lreceiptlineid = "lreceiptlineid";
	
	//Field lengths:
	public static final int sitemnumberLength = 24;
	public static final int slocationLength = 6;
	public static final int scontrolacctLength = 75;
	public static final int sdistributionacctLength = 75;
	public static final int sdescriptionLength = 75;
	public static final int scommentLength = 255;
	public static final int scategorycodeLength = 6;
	public static final int sreceiptnumLength = 22;
	public static final int stargetlocationLength = 6;
	public static final int sinvoicenumberLength = 15;
	public static final int bdqtyScale = 4;
	public static final int bdcostScale = 4;
	public static final int bdpriceScale = 4;
	
}