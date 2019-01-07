package SMDataDefinition;

public class SMTableglexportdetails {
	//Table Name
	public static final String TableName = "glexportdetails";

	//Field names:
	public static final String id  = "id";
	public static final String irecordtype  = "irecordtype";
	public static final String lbatchnumber  = "lbatchnumber";
	public static final String lbatchentry  = "lbatchentry";
	public static final String ldetailjournalid = "ldetailjournalid";
	public static final String ldetailtransactionnumber = "ldetailtransactionnumber";
	public static final String sdetailaccountid = "sdetailaccountid";
	public static final String bddetailtransactionamount = "bddetailtransactionamount";
	public static final String sdetailtransactiondescription = "sdetailtransactiondescription";
	public static final String sdetailtransactionreference = "sdetailtransactionreference";
	public static final String datdetailtransactiondate = "datdetailtransactiondate";
	public static final String sdetailsourceledger = "sdetailsourceledger";
	public static final String sdetailsourcetype = "sdetailsourcetype";
	public static final String sdetailcomment = "sdetailcomment";
	public static final String sdetailformattedaccountid = "sdetailformattedaccountid";
	    
	//Field lengths:
	public static final int sdetailaccountidlength = 45;
	public static final int sdetailtransactiondescriptionlength = 60;
	public static final int sdetailtransactionreferencelength = 60;
	public static final int sdetailsourceledgerlength = 2;
	public static final int sdetailsourcetypelength = 2;
	public static final int sdetailcommentlength = 254;
	public static final int sdetailformattedaccountidlength = 60;
	
	public static final int bddetailtransactionamountscale = 2;
}
