package SMDataDefinition;

public class SMTablecompanyprofile {

	public static String TableName = "companyprofile";
	
	//Field Names:
	public static String sDatabaseID = "sDatabaseID"; 
	public static String sCompanyName = "sCompanyName";
	public static String sAddress01 = "sAddress01";
	public static String sAddress02 = "sAddress02";
	public static String sAddress03 = "sAddress03";
	public static String sAddress04 = "sAddress04";
	public static String sCity = "sCity";
	public static String sContactName = "sContactName";
	public static String sState = "sState";
	public static String sZipCode = "sZipCode";
	public static String sCountry = "sCountry";
	public static String sPhoneNumber = "sPhoneNumber";
	public static String sFaxNumber = "sFaxNumber";
//	public static String sWOReceiptHeaderComment = "sWOReceiptHeaderComment";
	public static String iDatabaseVersion = "iDatabaseVersion"; // TJR - 2/27/2017 - called the 'Database revision number'
	
	//Field lengths:
	public static int sDatabaseIDLength = 6; 
	public static int sCompanyNameLength = 50;
	public static int sAddress01Length = 40;
	public static int sAddress02Length = 40;
	public static int sAddress03Length = 40;
	public static int sAddress04Length = 40;
	public static int sCityLength = 20;
	public static int sContactNameLength = 40;
	public static int sStateLength = 20;
	public static int sZipCodeLength = 15;
	public static int sCountryLength = 20;
	public static int sPhoneNumberLength = 20;
	public static int sFaxNumberLength = 20;
}
