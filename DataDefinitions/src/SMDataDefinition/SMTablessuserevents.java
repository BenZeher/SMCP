package SMDataDefinition;

public class SMTablessuserevents {
	public static final String TableName = "ssuserevents";
	
	public static final String lid = "lid";
	public static final String ieventtype = "ieventtype";
	public static final String seventtypelabel = "seventtypelabel";
	public static final String luserid = "luserid";
	public static final String suserfullname = "suserfullname";
	public static final String suserlatitude = "suserlatitude";
	public static final String suserlongitude = "suserlongitude";
	public static final String ldeviceid = "ldeviceid";
	public static final String sdevicedescription = "sdevicedescription";
	public static final String lalarmid = "lalarmid";
	public static final String salarmname = "salarmname";
	public static final String salarmdescription = "salarmdescription";
	public static final String dattimeoccurrence = "dattimeoccurrence";
	public static final String scomment = "scomment";
	public static final String sreferenceid = "sreferenceid";
	
	//Lengths
	public static final int seventtypelabellength = 64;
	public static final int luseridlength = 11;
	public static final int suserfullnamelength = 128;
	public static final int suserlatitudelength = 24;
	public static final int suserlongitudelength = 24;
	public static final int sdevicedescriptionlength = 128;
	public static final int salarmnamelength = 32;
	public static final int salarmdescriptionlength = 128;
	public static final int scommentlength = 254;
	public static final int sreferenceidlength = 12;
	
	public static final int ALARM_ZONE_ARMED = 0;
	public static final int ALARM_ZONE_DISARMED = 1;
	public static final int DEVICE_ACTIVATED = 2;
	
	public static String getUserEventTypeLabel(int iEventType){
		switch (iEventType){
		case ALARM_ZONE_ARMED:
			return "Armed zone";
		case ALARM_ZONE_DISARMED:
			return "Disarmed zone";
		case DEVICE_ACTIVATED:
			return "Activated device";
		default:
			return "UKNOWN";
		}
	}
}
