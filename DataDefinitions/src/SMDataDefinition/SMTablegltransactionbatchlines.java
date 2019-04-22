package SMDataDefinition;

public class SMTablegltransactionbatchlines {
	//Table Name
	public static final String TableName = "gltransactionbatchlines";
	
	//Field names:
	public static final String lid  = "lid";
	public static final String lbatchnumber  = "lbatchnumber";
	public static final String lentrynumber = "lentrynumber";
	public static final String llinenumber = "llinenumber";
	public static final String sdescription = "sdescription";
	public static final String sreference = "sreference";
	public static final String scomment = "scomment";
	public static final String dattransactiondate = "dattransactiondate";
	public static final String sacctid = "sacctid";
	public static final String bddebitamt = "bddebitamt";
	public static final String bdcreditamt = "bdcreditamt";
	public static final String ssourceledger = "ssourceledger";
	public static final String ssourcetype = "ssourcetype";
	
	//Field lengths:
	public static final int sreferenceLength = 60;
	public static final int scommentLength = 60;
	public static final int sdescriptionLength = 60;
	public static final int ssourceledgerLength = 2;
	public static final int ssourcetypeLength = 2;
	public static final int sacctidLength = 45;
	public static final int bddebitamtScale = 2;
	public static final int bdcreditamtScale = 2;
	
}
