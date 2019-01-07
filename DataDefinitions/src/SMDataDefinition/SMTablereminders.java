package SMDataDefinition;

public class SMTablereminders {
	public static final String TableName = "reminders";
	public static final String lid = "lid";
	public static final String sschedulecode = "sschedulecode";
	public static final String sdescription = "sdescription";
	public static final String iinterval = "iinterval"; //Daily, weekly, monthly, semi-monthly, yearly
	public static final String idayofmonth = "idayofmonth";
	
	//Which day of the week, (Sunday is '1'), that the event occurs, such as 'on Tuesday of the second week of the month' - WEEKDAY value would be '3'
	public static final String iweekday = "iweekday";
	
	//Which month (January is 1) in which the event occurs, if it's a 'yearly' interval
	public static final String imonth = "imonth";
	
	//Days of the week on which the event occurs:
	public static final String isunday = "isunday";
	public static final String imonday = "imonday";
	public static final String ituesday = "ituesday";
	public static final String iwednesday = "iwednesday";
	public static final String ithursday = "ithursday";
	public static final String ifriday = "ifriday";
	public static final String isaturday = "isaturday";
	
	//Starting date:
	public static final String datstartdate = "datstartdate";
	
	//Last edited:
	public static final String slastediteduserfullname = "slastediteduserfullname";
	public static final String datlasteditdate = "datlasteditdate";
	
	//Created by
	public static final String lcreatedbyuserid = "lcreatedbyuserid";
	
	//Indicates if scheduled reminder was created as a personal reminder
	public static final String iremindermode = "iremindermode";
	
	//Lengths:
	public static final int sschedulecodelength = 32;
	public static final int sdescriptionlength = 65535;
	public static final int slasteditedbylength = 128;
	
	//Reminder mode values
	public static final int PERSONAL_REMINDER_VALUE = 0;
	public static final int GENERAL_REMINDER_VALUE = 1;
	
	//Interval values:
	public static final int NUMBER_OF_INTERVAL_VALUES = 3;
	public static final int INTERVAL_TYPE_WEEKLY = 1;
	public static final int INTERVAL_TYPE_MONTHLY = 2;
	public static final int INTERVAL_TYPE_YEARLY = 3;
	
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
	

	public static String getIntervalDescription(int iInterval){
		switch(iInterval){
		case INTERVAL_TYPE_WEEKLY:
			return "Weekly";
		case INTERVAL_TYPE_MONTHLY:
			return "Monthly";
		case INTERVAL_TYPE_YEARLY:
			return "Yearly";
		default:
			return "N/A";
		}
	}
	
	
	public static String getMonthDescription(int iMonth){
		switch(iMonth){
		case MONTH_VALUE_JANUARY:
			return "January";
		case MONTH_VALUE_FEBRUARY:
			return "February";
		case MONTH_VALUE_MARCH:
			return "March";
		case MONTH_VALUE_APRIL:
			return "April";
		case MONTH_VALUE_MAY:
			return "May";
		case MONTH_VALUE_JUNE:
			return "June";
		case MONTH_VALUE_JULY:
			return "July";
		case MONTH_VALUE_AUGUST:
			return "August";
		case MONTH_VALUE_SEPTEMBER:
			return "September";
		case MONTH_VALUE_OCTOBER:
			return "October";
		case MONTH_VALUE_NOVEMBER:
			return "November";
		case MONTH_VALUE_DECEMBER:
			return "December";
		default:
			return "N/A";
		}
	}
	
	public static String getWeekdayDescription(int iWeekday){
		switch(iWeekday){
		case WEEKDAY_VALUE_SUNDAY:
			return "Sunday";
		case WEEKDAY_VALUE_MONDAY:
			return "Monday";
		case WEEKDAY_VALUE_TUESDAY:
			return "Tuesday";
		case WEEKDAY_VALUE_WEDNESDAY:
			return "Wednesday";
		case WEEKDAY_VALUE_THURSDAY:
			return "Thursday";
		case WEEKDAY_VALUE_FRIDAY:
			return "Friday";
		case WEEKDAY_VALUE_SATURDAY:
			return "Saturday";
		default:
			return "N/A";
		}
	}

	/*
	ACCPAC table CSSKTB
	SCHEDKEY - Schedule code
	SCHEDDESC - description
	INTERVAL - 1 = DAILY, 2 = WEEKLY, 3 = Semi-monthly, 4 = Monthly, 5 = Yearly
	PHASE - every month/week/etc. (1), every 3 months (3), etc.
	MONTHDAY - day of the month on which the event occurs
	WEEK - which week the day occurs, as in 'the THIRD week of the month' - the value would be '3'
	WEEKDAY - which day of the week, Sunday is '1', that the event occurs, such as 'on Tuesday of the second week of the month' - WEEKDAY value would be '3'
	MONTH - which month the event occurs, if it's a YEARLY interval - January is '1'
	
	//If the interval is daily or weekly, and it has to happen on certain days of the week, then these are 1, otherwise, 0:
	WDFSUN
	WDFMON
	WDFTUE
	WDFWED
	WDFTHU
	WDFFRI
	WDFSAT
	ACTIVEDATE - schedule start date
	REMINDLEAD - how many days in advance to remind the user
	USERID - the user who is to be reminded
	USERMODE - 1 = 'No users', 2 = 'Specific user', 3 = 'All users'
	LASTDATE - the last time an instance of this schedule was activated.  So if it's a monthly schedule, on the 1st of the month
		and it's 12/21/2015, then the 'LAST DATE' would be 12/1/2015.  I don't see that we need this.
	FREQUENCY
		If it's a DAILY recurring period, and the frequency is just 'every so many days', then the FREQUENCY is 5.
		If it's a DAILY recurring period, and the frequency is on certain days of the week, the FREQUENCY is 1.
		If it's a WEEKLY recurring period, the frequency is 5.
		If it's a SEMI-MONTHLY period, and it's on the LAST day of the month and a selected FIRST day (for example
		 if it happens on the 10th AND the last day of the month), then the FREQUENCY is 4.
		If it's a SEMI-MONTHLY period, and it's on the FIRST day of the month and a selected LAST day (for example
		 if it happens on the 1st day and the 25th day of the month), then the frequency is 5.
		If it's a MONTHLY recurring period, and it's on a certain day of the month, like the 3rd, or the 10th, the FREQUENCY is 5.
		If it's a MONTHLY recurring period, and it's on a certain weekday, like 'the first Tuesday of the month', the FREQUENCY is 2.
		If it's a YEARLY recurring period, and it's on a certain month and day of the month, like 'January, on the 3rd day' of the month,
		 then the FREQUENCY is 5.
		If it's a YEARLY recurring period, and it's on a certain weekday of the month, like 'January, on the 3rd Monday' of the month,
		 then the FREQUENCY is 2.
	REMINDLEAD - number of days in advance to remind the user(s)
	USERMODE - No users = 1, Specific user = 2, All users = 3
	USERID - the user to be reminded
	
	NOTE: Washington only uses one 'monthly' reminder, checking the FL companies on 1/11/2016, I found only these 'schedule' records in their ACCPAC data:
	Tampa Bay - 4 records - all schedule interval '4'
	Capitol City - 2 records - both schedule interval '4'
	Daytona - 3 records - two with schedule interval '4', and one with schedue interval '2'

	*/
}
