package SMDataDefinition;

public class SMTablessalarmsequences {

	public static final String TableName = "ssalarmsequences";
	
	public static final String lid = "lid";
	public static final String ialarmstate = "ialarmstate";
	public static final String semailnotifications = "semailnotifications";
	public static final String sname = "sname";
	public static final String sdescription = "sdescription";
	public static final String lalarmsetdelaycountdown = "lalarmsetcountdown";
	public static final String datlastarmed  = "datlastarmed";
	public static final String llastarmedbyid = "llastarmedbyid";
	public static final String slastarmedbyfullname = "slastarmedbyfullname";
	public static final String datlastdisarmed  = "datlastdisarmed";
	public static final String llastdisarmedbyid = "llastdisarmedbyid";
	public static final String slastdisarmedbyfullname = "slastdisarmedbyfullname";
	
	//Lengths
	public static final int snamelength = 32;
	public static final int sdescriptionlength = 128;
	public static final int llastarmedbyidlength = 11;
	public static final int slastarmedbyfullnamelength = 128;
	public static final int llastdisarmedbyidlength = 11;
	public static final int slastdisarmedbyfullnamelength = 128;
	
	//Alarm states:
	public static final int ALARM_STATE_UNARMED = 0;
	public static final int ALARM_STATE_ARMED = 1;
	public static final int ALARM_STATE_TRIGGERED = 2;
	
	//Alarm state labels:
	public static final String ALARM_STATE_LABEL_UNARMED = "Disarmed";
	public static final String ALARM_STATE_LABEL_ARMED = "Armed";
	public static final String ALARM_STATE_LABEL_TRIGGERED = "Triggered";
	
	public static String getAlarmStateLabel(int iDeviceType){
		switch(iDeviceType){
		case ALARM_STATE_UNARMED:
			return ALARM_STATE_LABEL_UNARMED;
		case ALARM_STATE_ARMED:
			return ALARM_STATE_LABEL_ARMED;
		case ALARM_STATE_TRIGGERED:
			return ALARM_STATE_LABEL_TRIGGERED;
		default:
			return "(UNKNOWN)";
		}
	}
	
}
