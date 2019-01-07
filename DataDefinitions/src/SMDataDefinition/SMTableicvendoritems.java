package SMDataDefinition;

public class SMTableicvendoritems {
	//Table Name
	public static final String TableName = "icvendoritems";

	//Field names:
	public static final String sItemNumber = "sitemnumber";
	public static final String sVendor = "svendor";
	public static final String sVendorItemNumber = "svendoritemnumber";
	public static final String sCost = "bdcost";
	public static final String sComment = "scomment";
	
	//Field Lengths:
	public static final int sItemNumberLength = 24;
	public static final int sVendorLength = 12;
	public static final int sVendorItemNumberLength = 24;
	public static final int sCommentLength = 128;
	
	public static final int sCostScale = 4;
}
