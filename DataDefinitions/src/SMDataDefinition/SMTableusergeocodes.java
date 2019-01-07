package SMDataDefinition;

public class SMTableusergeocodes {
		
	//Table name:
	public static String TableName = "usergeocodes";
	
	//Field names:
    public static String lID = "id";
    public static String lUserID = "luserid";
    public static String sFirstName = "sfirstname";
    public static String sLastName = "slastname";
    public static String sLatitude = "slatitude";
    public static String sLongitude = "slongitude";
    public static String sSpeed = "sspeed";
    public static String sAltitude = "saltitude";
    public static String sAccuracy = "saccuracy";
    public static String sAltitudeAccuracy = "saltitudeaccuracy";
    public static String datimeEntry = "dattimeentry";

    //Field lengths:
    public static int lUserIDLength = 11;
    public static int lUserFullName = 128;
    public static int sFirstNameLength = 50;
    public static int sLastNameLength = 50;
    public static int sLatitudeLength = 32;
    public static int sLongitudeLength = 32;
    public static int sSpeedLength = 32;
    public static int sAltitudeLength = 32;
    public static int sAccuracyLength = 32;
    public static int sAltitudeAccuracyLength = 32;
}
