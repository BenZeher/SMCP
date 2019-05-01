package SMDataDefinition;

public class SMTablearoptions {

	public static final String TableName = "aroptions";
	
	//Field names:
	public static String ibatchpostinginprocess = "ibatchpostinginprocess";
	public static String suserfullname = "suserfullname";
	public static String sprocess = "sprocess";
	public static String datstartdate = "datstartdate";
	public static String iflagimports = "iflagimports";
	public static String iexportto = "iexportto";
	public static String ienforcecreditlimit = "ienforcecreditlimit";
	
	//Automatically feed SMCP GL?
	public static String ifeedgl = "ifeedgl";
	
	//Fields for creating new customer folders in Google Drive:
	public static String gdrivecustomersparentfolderid = "gdrivecustomersparentfolderid";
	public static String gdrivecustomersderfolderprefix = "gdrivecustomersfolderprefix";
	public static String gdrivecustomersfoldersuffix = "gdrivecustomersfoldersuffix";
	
	public static int suserlength = 128;
	public static int sprocesslength = 128;
	public static int gdrivecustomerparentfolderidlength = 72;
	public static int gdrivecustomerfolderprefixlength = 32;
	public static int gdrivecustomerfoldersuffixlength = 32;
	public static int suserfullnameLength = 128;
	
	public static final int FEED_GL_EXTERNAL_GL_ONLY = 0;
	public static final int FEED_GL_SMCP_GL_ONLY = 1;
	public static final int FEED_GL_BOTH_EXTERNAL_AND_SMCP_GL = 2;

}
