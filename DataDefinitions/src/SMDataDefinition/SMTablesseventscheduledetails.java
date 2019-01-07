package SMDataDefinition;

public class SMTablesseventscheduledetails {
	public static final String TableName = "sseventscheduledetails";
	
	public static final String lid = "lid";
	public static final String lsseventscheduleid = "lsseventscheduleid";
	public static final String ideviceorsequence = "ideviceorsequence";
	public static final String ldeviceorsequenceid = "ldeviceorsequenceid";  //The device ID or the alarm sequence ID
	public static final String iactiontype = "iactiontype";  //turn on, turn off for alarm sequences, set to open or closed for contacts
	public static final String iresetdelay  = "iresetdelay";  //how long in minutes after the event is manually turned off should it activate
	public static final String iactivated = "iactivated";  //Indicates whether the sequence or device was activated on schedule
	
	public static final int DEVICEORSEQUENCE_DEVICE = 0;
	public static final int DEVICEORSEQUENCE_SEQUENCE = 1;
	
	public static String getDetailTypeLabel(int iDeviceOrSequence){
		switch(iDeviceOrSequence){
		case DEVICEORSEQUENCE_DEVICE:
			return "Device";
		case DEVICEORSEQUENCE_SEQUENCE:
			return "Alarm Sequence";
		default:
			return "N/A";
		}
	}
}
