package SMDataDefinition;

public class SMTableictransactiondetails {
	public static final String TableName = "ictransactiondetails";
	
	public static final String lid = "lid";
	public static final String ldetailnumber = "ldetailnumber";
	public static final String ltransactionid = "ltransactionid";
	public static final String lcostbucketid = "lcostbucketid";
	public static final String dattimecostbucketcreation = "dattimecostbucketcreation";
	public static final String scostbucketlocation = "scostbucketlocation";
	public static final String scostbucketremark = "scostbucketremark";
	public static final String lcostbucketreceiptlineid = "lcostbucketreceiptlineid";
	public static final String bdcostbucketcostbeforetrans = "bdcostbucketcostbeforetrans";
	public static final String bdcostchange = "bdcostchange";
	public static final String bdcostbucketqtybeforetrans = "bdcostbucketqtybeforetrans";
	public static final String bdqtychange = "bdqtychange";

	//Field lengths:
	public static final int scostbucketlocationLength = 6;
	public static final int scostbucketremarkLength = 128;
	
	public static final int bdcostbucketcostbeforetransScale = 2;
	public static final int bdcostchangeScale = 2;
	public static final int bdcostbucketqtybeforetransScale = 4;
	public static final int bdqtychangeScale = 4;
	
}
