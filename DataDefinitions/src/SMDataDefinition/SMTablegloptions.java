package SMDataDefinition;

public class SMTablegloptions {
	public static final String TableName = "gloptions";
	
	//Field names:
	public static String ibatchpostinginprocess = "ibatchpostinginprocess";
	public static String suserfullname = "suserfullname";
	public static String luserid = "luserid";
	public static String sprocess = "sprocess";
	public static String datstartdate = "datstartdate";
	
	//This can be removed when NO ONE is using ACCPAC AP any longer...
	public static String iusessmcpgl = "iusessmcpgl";
	public static String iaccpacversion = "iaccpacversion";
	
	//These fields will only be used for converting from ACCPAC AP:
	public static String saccpacdatabaseurl = "saccpacdatabaseurl";
	public static String saccpacdatabasename = "saccpacdatabasename";
	public static String saccpacdatabaseuser = "saccpacdatabaseuser";
	public static String saccpacdatabaseuserpw = "saccpacdatabasuserpw";
	public static String iaccpacdatabasetype = "iaccpacdatabasetype";
	
	//This field is used for temporarily testing the GL system and logic:
	//If it is set, then SMCP will create GL batches whenever a submodule (i.e. IC, AR, AP, Order Entry, FA) batch is posted,
	//  so we can automatically get some working data in the GL system.  It won't appear on a screen, but it can be manipulated
	//  with SQL commands:
	public static String icreatetestbatchesfromsubmodules = "icreatetestbatchesfromsubmodules";
	
	public static String sclosingaccount = "sclosingaccount";
	
	//Lengths:
	public static int suserlength = 128;
	public static int sprocesslength = 128;
	
	public static int saccpacdatabaseurllength = 128;
	public static int saccpacdatabasenamelength = 128;
	public static int saccpacdatabaseuserlength = 128;
	public static int saccpacdatabasuserpwlength = 128;
	public static int sclosingaccountLength = 45;
	
	public static final int NUMBER_OF_ACCPAC_VERSION_FORMATS = 3;
	public static final int GL_VERSION_ACCPAC54 = 0;
	public static final int GL_VERSION_ACCPAC56 = 1;
	public static final int GL_VERSION_ACCPAC61 = 2;
	
	public static final String GL_VERSION_ACCPAC54_LABEL = "ACCPAC Version 5.4";
	public static final String GL_VERSION_ACCPAC56_LABEL = "ACCPAC Version 5.6";
	public static final String GL_VERSION_ACCPAC61_LABEL = "ACCPAC Version 6.1";
	
	
	public static final int NUMBER_OF_DATABASE_TYPES = 2;
	public static final int ACCPAC_DATABASE_VERSION_TYPE_PERVASIVE = 0;
	public static final int ACCPAC_DATABASE_VERSION_TYPE_MSSQL = 1;
	
	public static final String ACCPAC_DATABASE_VERSION_TYPE_PERVASIVE_LABEL = "Pervasive";
	public static final String ACCPAC_DATABASE_VERSION_TYPE_MSSQL_LABEL = "MS SQL";
	
	public static String getACCPACVersionLabel(int iACCPACVersion){
		
		if (iACCPACVersion == GL_VERSION_ACCPAC54){
			return GL_VERSION_ACCPAC54_LABEL;
		}
		if (iACCPACVersion == GL_VERSION_ACCPAC56){
			return GL_VERSION_ACCPAC56_LABEL;
		}
		if (iACCPACVersion == GL_VERSION_ACCPAC61){
			return GL_VERSION_ACCPAC61_LABEL;
		}
		return GL_VERSION_ACCPAC61_LABEL;
	}
	
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
