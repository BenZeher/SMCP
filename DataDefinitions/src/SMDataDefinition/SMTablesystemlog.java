package SMDataDefinition;

public class SMTablesystemlog {
	//Table Name
	public static final String TableName = "systemlog";
	
	//Field names:
	public static final String lid = "lid";
	public static final String datloggingtime = "datloggingtime";
	public static final String suserid = "suserid";
	public static final String suserfullname = "suserfullname";
	public static final String soperation = "soperation";
	public static final String mdescription = "mdescription";
	public static final String mcomment = "mcomment";
	public static final String sreferenceid = "sreferenceid";

	//Field Lengths:
	public static final int datloggingtimeLength = 6;
	public static final int suseridLength = 128;
	public static final int soperationLength = 50;
	public static final int sreferenceidLength = 12;
	public static final int mdescriptionLength = 65535;
	public static final int mcommentLength = 65535;
}
