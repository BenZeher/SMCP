package TCSDataDefinition;

public class TCTablerawpunchevents {

	//Table Name
	public static String TableName = TimeCardSystemTables.TCRawPunchEvents;

	//Field List
	public static final String id = "id";
	public static final String sEmployeeID = "sEmployeeID";
	public static final String iEntryType = "iEntryType";
	public static final String iApproved = "iApproved";
	public static final String dtTime = "dtTime";
	public static final String sIPAddress = "sIPAddress";
	public static final String iInOut = "iInOut";
	public static final String sgeocode = "sgeocode";
	public static final String igeocodingallowedbyuser = "igeocodingallowedbyuser";
	public static final String igeocodingsupportedbybrowser = "igeocodingsupportedbybrowser";
	
	
	public static final int PUNCH_IN = 0;
	public static final int PUNCH_OUT = 1;
}
