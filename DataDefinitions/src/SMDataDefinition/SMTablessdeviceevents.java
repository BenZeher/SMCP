package SMDataDefinition;

public class SMTablessdeviceevents {
	public static final String TableName = "ssdeviceevents";
	
	public static final String lid = "lid";
	public static final String ldeviceid = "ldeviceid";
	public static final String lcontrollerid = "lcontrollerid";
	public static final String lzoneid = "lzoneid";
	public static final String sdevicedescription = "sdevicedescription";
	public static final String scontrollername = "scontrollername";
	public static final String scontrollerdescription = "scontrollerdescription";
	public static final String szonename = "szonename";
	public static final String szonedescription = "szonedescription";
	public static final String sterminalnumber = "sterminalnumber";
	public static final String iterminaltype = "iterminaltype";
	public static final String ieventtype = "ieventtype";
	public static final String dattimeoccurrence = "dattimeoccurrence";
	public static final String scomment = "scomment";
	public static final String sreferenceid = "sreferenceid";
	
	//Lengths
	public static final int sdevicedescriptionlength = 128;
	public static final int scontrollernamelength = 32;
	public static final int scontrollerdescriptionlength = 128;
	public static final int szonenamelength = 32;
	public static final int szonedescriptionlength = 128;
	public static final int sterminalnumberlength = 8;
	public static final int scommentlength = 254;
	public static final int sreferenceidlength = 12;
	
	public static final int DEVICE_EVENT_TYPE_ACTIVATED = 0;
	public static final int DEVICE_EVENT_TYPE_TRIGGER_STARTED = 1;
	public static final int DEVICE_EVENT_TYPE_TRIGGER_STOPPED = 2;
	public static final int DEVICE_EVENT_TYPE_DEACTIVATED = 3;
	
	
	public static String getDeviceEventTypeLabel(int iDeviceEventType){
		switch (iDeviceEventType){
		case DEVICE_EVENT_TYPE_ACTIVATED:
			return "Activated";
		case DEVICE_EVENT_TYPE_TRIGGER_STARTED:
			return "Trigger start";
		case DEVICE_EVENT_TYPE_TRIGGER_STOPPED:
			return "Trigger stop";
		case DEVICE_EVENT_TYPE_DEACTIVATED:
			return "De-activated";
		default:
			return "UKNOWN";
		}
	}
}
