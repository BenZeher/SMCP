package SMDataDefinition;

public class SMTableapoptions {

	public static final String TableName = "apoptions";
	
	//Field names:
	public static String ibatchpostinginprocess = "ibatchpostinginprocess";
	public static String luserid = "luserid";
	public static String sprocess = "sprocess";
	public static String datstartdate = "datstartdate";
	
	//This can be removed when NO ONE is using ACCPAC AP any longer...
	public static String iusessmcpap = "iusessmcpap";
	public static String iaccpacversion = "iaccpacversion";
	
	//Fields for creating new vendor folders in Google Drive:
	public static String gdrivevendorsparentfolderid = "gdrivevendorsparentfolderid";
	public static String gdrivevendorsderfolderprefix = "gdrivevendorsderfolderprefix";
	public static String gdrivevendorsfoldersuffix = "gdrivevendorsfoldersuffix";
	
	//These fields will only be used for converting from ACCPAC AP:
	public static String saccpacdatabaseurl = "saccpacdatabaseurl";
	public static String saccpacdatabasename = "saccpacdatabasename";
	public static String saccpacdatabaseuser = "saccpacdatabaseuser";
	public static String saccpacdatabaseuserpw = "saccpacdatabasuserpw";
	public static String iaccpacdatabasetype = "iaccpacdatabasetype";
	
	//Export file type options:
	public static String iexportto = "iexportto";
	
	//This field is used for temporarily testing the AP system and logic:
	//If it is set, then SMCP will create AP batches whenever a PO invoice batch is posted, so we can automatically
	//get some working data in the AP system.  It won't appear on a screen, but it can be manipulated with SQL commands:
	public static String icreatetestbatchesfrompoinvoices = "icreatetestbatchesfrompoinvoices";
	
	//Automatically feed SMCP GL?
	public static String ifeedgl = "ifeedgl";
	
	//Lengths:
	public static int suserlength = 128;
	public static int sprocesslength = 128;
	public static int gdrivevendorparentfolderidlength = 72;
	public static int gdrivevendorfolderprefixlength = 32;
	public static int gdrivevendorfoldersuffixlength = 32;
	
	public static int saccpacdatabaseurllength = 128;
	public static int saccpacdatabasenamelength = 128;
	public static int saccpacdatabaseuserlength = 128;
	public static int saccpacdatabasuserpwlength = 128;
	
	public static final int NUMBER_OF_ACCPAC_VERSION_FORMATS = 3;
	public static final int AP_VERSION_ACCPAC54 = 0;
	public static final int AP_VERSION_ACCPAC56 = 1;
	public static final int AP_VERSION_ACCPAC61 = 2;
	
	public static final String AP_VERSION_ACCPAC54_LABEL = "ACCPAC Version 5.4";
	public static final String AP_VERSION_ACCPAC56_LABEL = "ACCPAC Version 5.6";
	public static final String AP_VERSION_ACCPAC61_LABEL = "ACCPAC Version 6.1";
	
	public static final int FEED_GL_EXTERNAL_GL_ONLY = 0;
	public static final int FEED_GL_SMCP_GL_ONLY = 1;
	public static final int FEED_GL_BOTH_EXTERNAL_AND_SMCP_GL = 2;
	
	public static String getExportFormatLabel(int iExportIndex){
		
		if (iExportIndex == AP_VERSION_ACCPAC54){
			return AP_VERSION_ACCPAC54_LABEL;
		}
		if (iExportIndex == AP_VERSION_ACCPAC56){
			return AP_VERSION_ACCPAC56_LABEL;
		}
		if (iExportIndex == AP_VERSION_ACCPAC61){
			return AP_VERSION_ACCPAC61_LABEL;
		}
		return AP_VERSION_ACCPAC61_LABEL;
	}
	
	public static final int NUMBER_OF_DATABASE_TYPES = 2;
	public static final int ACCPAC_DATABASE_VERSION_TYPE_PERVASIVE = 0;
	public static final int ACCPAC_DATABASE_VERSION_TYPE_MSSQL = 1;
	
	public static final String ACCPAC_DATABASE_VERSION_TYPE_PERVASIVE_LABEL = "Pervasive";
	public static final String ACCPAC_DATABASE_VERSION_TYPE_MSSQL_LABEL = "MS SQL";
	
	public static String getDatabaseTypeLabel(int iACCPACDatabaseType){
		
		if (iACCPACDatabaseType == ACCPAC_DATABASE_VERSION_TYPE_PERVASIVE){
			return ACCPAC_DATABASE_VERSION_TYPE_PERVASIVE_LABEL;
		}
		if (iACCPACDatabaseType == ACCPAC_DATABASE_VERSION_TYPE_MSSQL){
			return ACCPAC_DATABASE_VERSION_TYPE_MSSQL_LABEL;
		}
		return ACCPAC_DATABASE_VERSION_TYPE_MSSQL_LABEL;
	}
}
