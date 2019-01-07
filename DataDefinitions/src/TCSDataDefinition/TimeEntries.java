package TCSDataDefinition;

public class TimeEntries {

	//Table Name
	public static String TableName = TimeCardSystemTables.TimeEntries;

	//Field List
	public static final String id = "id";
	public static final String sEmployeeID = "sEmployeeID";
	public static final String dtInTime = "dtInTime";
	public static final String dtOutTime = "dtOutTime";
	public static final String iInModified = "iInModified";
	public static final String iOutModified = "iOutModified";
	public static final String iEarlyStart = "iEarlyStart";
	public static final String sPeriodDate = "sPeriodDate";
	public static final String mChangeLog = "mChangeLog";
	public static final String dtInTimeOri = "dtInTimeOri";
	public static final String iLate = "iLate";
	public static final String iLateMinute = "iLateMinute";
	
	//Time entry types are listed in the 'Special Entry Types' table.  'Regular time' is usually '0'
	public static final String iEntryTypeID = "iEntryTypeID";
	
}
