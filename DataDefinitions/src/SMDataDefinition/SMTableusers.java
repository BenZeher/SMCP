package SMDataDefinition;

public class SMTableusers {

	//Table Name
	public static String TableName = "users";
	
	//Field names:
	public static String lid = "lid";
    public static String sDefaultSalespersonCode = "sDefaultSalespersonCode";
    public static String sIdentifierInitials = "sIdentifierInitials";
    public static String sUserFirstName = "sUserFirstName";
    public static String sUserLastName = "sUserLastName";
    public static String sUserName = "sUserName";
    public static String sHashedPw = "sHashedPw";
    public static String semail = "semail";
    public static String smechanicinitials = "smechanicinitials";
    public static String iactive = "iactive"; //1 - Active, 0 - Inactive
    public static String susercolorcoderow = "susercolorrow";
    public static String susercolorcodecol = "susercolorcol";
	
    //Field Lengths:
    public static int sUserUserNameLength = 128;
    public static int sUserDefaultSalespersonCodeLength = 10;
    public static int sUserIdentifierInitialsLength = 5;
    public static int sUserUserFirstNameLength = 50;
    public static int sUserUserLastNameLength = 50;
    public static int semailLength = 128;
    public static int smechanicinitialsLength = 4;
}
