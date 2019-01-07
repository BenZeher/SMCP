package SMDataDefinition;

public class SMTablesalesperson {

	//Table name:
	public static String TableName = "salesperson";
	
	//Field names:
    public static String lSalespersonUserID = "lSalespersonUserID";
    public static String sSalespersonCode = "sSalespersonCode";
    public static String sSalespersonFirstName = "sSalespersonFirstName";
    public static String sSalespersonLastName = "sSalespersonLastName";
    public static String iShowInSalesReport = "iShowInSalesReport";
    public static String sSalespersonType = "sSalespersonType";
    //LTO20130402
    public static String sSalespersonTitle = "ssalespersontitle";
    public static String sDirectDial = "sdirectdial";
    public static String sSalespersonEmail = "ssalespersonemail";
    //TJR 2013-06-19
    public static String mSignature = "msignature";

    //Field lengths:
    public static int lSalespersonUserIDLength = 11;
    public static int sSalespersonCodeLength = 8;
    public static int sSalespersonFirstNameLength = 50;
    public static int sSalespersonLastNameLength = 50;
    public static int sSalespersonTypeLength = 1;
    //LTO20130402
    public static int sSalespersonTitleLength = 50;
    public static int sDirectDialLength = 32;
    public static int sSalespersonEmailLength = 128;
}
