package SMDataDefinition;

public class SMTableicinventoryworksheet {

	public static final String TableName = "icinventoryworksheet";
	
	public static final String lid = "lid";
	public static final String lphysicalinventoryid = "lphysicalinventoryid";
	public static final String sitemnumber = "sitemnumber";
	public static final String bdqtyonhand = "bdqtyonhand";
	public static final String bdmostrecentcost = "bdmostrecentcost";
	public static final String sinvacct = "sinvacct";
	public static final String swriteoffacct = "swriteoffacct";

	//Field lengths:
	public static final int sitemnumberLength = 24;
	public static final int sinvacctLength = 75;
	public static final int swriteoffacctLength = 75;
	
	public static final int bdmostrecentcostScale = 4;
}
