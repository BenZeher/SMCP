package SMDataDefinition;

public class SMTableglexternalcompanypulls {
	//Table Name
	public static final String TableName = "glexternalcompanypulls";

	//Field names:
	public static final String lid = "lid";
	public static final String dattimepulldate = "dattimepulldate";
	public static final String lcompanyid = "lcompanyid";
	public static final String sdbname = "sdbname";
	public static final String scompanyname = "scompanyname";
	public static final String luserid = "luserid";
	public static final String sfullusername = "sfullusername";
	public static final String ifiscalyear = "ifiscalyear";
	public static final String ifiscalperiod = "ifiscalperiod";
	public static final String ipulltype = "ipulltype";

	//Field Lengths:
	public static final int sdbnameLength = 64;
	public static final int scompanynameLength = 128;
	public static final int sfullusernameLength = 128;
	
	//Pull Types:
	public static final int PULL_TYPE_PULL = 0;  //Normal pull type - when we pull transactions in from
													// an external company
	public static final int PULL_TYPE_REVERSE = 1; //This indicates the REVERSAL of a pull, such as when
													// the user finds it necessary to 'roll back' a previous pull.
}

