package SMDataDefinition;

public class SMTableappointments {
	public static final String TableName = "appointments";
	public static final String lid = "lid"; //auto inc.
	//public static final String suser = "suser"; //128
	public static final String luserid = "luserid"; //11
	public static final String datentrydate = "datentrydate"; 
	public static final String iminuteofday = "iminuteofday"; //11
	public static final String mcomment = "mcomment"; 
	public static final String sordernumber = "sordernumber"; //22
	public static final String isalescontactid = "isalescontactid"; //11
	public static final String ibidid = "ibidid"; //11
	public static final String scontactname = "scontactname"; //60
	public static final String sbilltoname = "sbilltoname"; //60
	public static final String sshiptoname = "sshiptoname"; //60
	public static final String saddress1 = "saddress1"; //60
	public static final String saddress2 = "saddress2"; //60
	public static final String saddress3 = "saddress3"; //60
	public static final String saddress4 = "saddress4"; //60
	public static final String scity = "scity"; //30
	public static final String sstate = "sstate"; //30
	public static final String szip = "szip"; //20
	public static final String sphone = "sphone"; //60
	public static final String semail = "semail"; //60
	public static final String sgeocode = "sgeocode"; //64
	public static final String datcreatedtime = "datcreatedtime"; 
	//public static final String screateduser = "screateduser"; //128
	public static final String lcreateduserid = "lcreateduserid"; //11
	public static final String inotificationtime = "inotificationtime"; //11
	public static final String inotificationsent = "inotificationsent"; //11
	//public static final String luserid = "luserid"; //11

	//Lengths:
	public static final int saddress1Length = 60;
	public static final int saddress2Length = 60;
	public static final int saddress3Length = 60;
	public static final int saddress4Length = 60;
	public static final int scityLength = 30;
	public static final int sstateLength = 30;
	public static final int szipLength = 20;
	public static final int isequencenumberlength = 6;
	public static final int sordernumberlength = 11;
	public static final int ibididlength = 11;
	public static final int isalescontactidlength = 11;
	public static final int icontactnameLength = 120;
	public static final int ibilltonameLength = 120;
	public static final int ishiptonameLength = 120;
	public static final int iphoneLength = 30;
	public static final int iemailLength = 128;
	public static final int screateduserLength = 128;

	
	//Month values:
	public static final int NUMBER_OF_MONTH_VALUES = 12;
	public static final int MONTH_VALUE_JANUARY = 1;
	public static final int MONTH_VALUE_FEBRUARY = 2;
	public static final int MONTH_VALUE_MARCH = 3;
	public static final int MONTH_VALUE_APRIL = 4;
	public static final int MONTH_VALUE_MAY = 5;
	public static final int MONTH_VALUE_JUNE = 6;
	public static final int MONTH_VALUE_JULY = 7;
	public static final int MONTH_VALUE_AUGUST = 8;
	public static final int MONTH_VALUE_SEPTEMBER = 9;
	public static final int MONTH_VALUE_OCTOBER = 10;
	public static final int MONTH_VALUE_NOVEMBER = 11;
	public static final int MONTH_VALUE_DECEMBER = 12;
	
	//Weekday values:
	public static final int NUMBER_OF_WEEKDAY_VALUES = 7;
	public static final int WEEKDAY_VALUE_SUNDAY = 1;
	public static final int WEEKDAY_VALUE_MONDAY = 2;
	public static final int WEEKDAY_VALUE_TUESDAY = 3;
	public static final int WEEKDAY_VALUE_WEDNESDAY = 4;
	public static final int WEEKDAY_VALUE_THURSDAY = 5;
	public static final int WEEKDAY_VALUE_FRIDAY = 6;
	public static final int WEEKDAY_VALUE_SATURDAY = 7;
	
	//Reminder Intervals in minutes
	public static final String arrNotificationTimeIntervals[][] = 
		{
		{"-1","None"},
		{"0","Appointment time"},
		{"30","30 minutes before"},
		{"60","60 minutes before"},
		{"90","90 minutes before"},
		{"120","2 hours before"},
		{"180","3 hours before"}
		};

}

