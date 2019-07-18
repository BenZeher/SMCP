package ServletUtilities;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;

/*
Date formats:
Letter 	Date or Time Component 	Presentation 	Examples
G 	Era designator 	Text 	AD
y 	Year 	Year 	1996; 96
M 	Month in year 	Month 	July; Jul; 07
w 	Week in year 	Number 	27
W 	Week in month 	Number 	2
D 	Day in year 	Number 	189
d 	Day in month 	Number 	10
F 	Day of week in month 	Number 	2
E 	Day in week 	Text 	Tuesday; Tue
a 	Am/pm marker 	Text 	PM
H 	Hour in day (0-23) 	Number 	0
k 	Hour in day (1-24) 	Number 	24
K 	Hour in am/pm (0-11) 	Number 	0
h 	Hour in am/pm (1-12) 	Number 	12
m 	Minute in hour 	Number 	30
s 	Second in minute 	Number 	55
S 	Millisecond 	Number 	978
z 	Time zone 	General time zone 	Pacific Standard Time; PST; GMT-08:00
Z 	Time zone 	RFC 822 time zone 	-0800
 */

public class clsDateAndTimeConversions {

	public static int NormalToMilitary(int iInput, int iAMPM){
		if (iAMPM == Calendar.PM && iInput !=12){
			return iInput + 12;
		}else{
			return iInput;
		}
	}

	public static String nowSqlFormat() {
		Calendar cal = Calendar.getInstance();
		SimpleDateFormat sdf = new SimpleDateFormat(clsDateAndTimeConversions.DATETIME_FORMAT_SQL);
		return sdf.format(cal.getTime());
	}

	public static String nowStdFormat() {
		Calendar cal = Calendar.getInstance();
		SimpleDateFormat sdf = new SimpleDateFormat(clsDateAndTimeConversions.DATETIME_FORMAT_STD);
		return sdf.format(cal.getTime());
	}

	public static String nowStdFormatWithSeconds() {
		Calendar cal = Calendar.getInstance();
		SimpleDateFormat sdf = new SimpleDateFormat(clsDateAndTimeConversions.DATETIME_FORMAT_STD_W_SECONDS);
		return sdf.format(cal.getTime());
	}

	public static String nowStdMdyyyFormat() {
		Calendar cal = Calendar.getInstance();
		SimpleDateFormat sdf = new SimpleDateFormat(clsDateAndTimeConversions.DATE_FORMAT_STD_Mdyyy);
		return sdf.format(cal.getTime());
	}

	public static java.sql.Date nowAsSQLDate() {
		//Calendar cal = Calendar.getInstance();
		//return (Date) cal.getTime();
		java.util.Date date = new java.util.Date();
		return clsDateAndTimeConversions.UtilDateToSQLDate(date);
	}

	public static java.sql.Timestamp nowAsTimestamp() {
		Timestamp ts = new Timestamp ( System.currentTimeMillis (  )  ) ; 
		return ts;
	}

	public static String now(String sDateFormat) {
		Calendar cal = Calendar.getInstance();
		SimpleDateFormat sdf = new SimpleDateFormat(sDateFormat);
		return sdf.format(cal.getTime());
	
		/*
		 Samples:
		 System.out.println(DateUtils.now("dd MMMMM yyyy"));
		 System.out.println(DateUtils.now("yyyyMMdd"));
		 System.out.println(DateUtils.now("dd.MM.yy"));
		 System.out.println(DateUtils.now("MM/dd/yy"));
		 System.out.println(DateUtils.now("yyyy.MM.dd G 'at' hh:mm:ss z"));
		 System.out.println(DateUtils.now("EEE, MMM d, ''yy"));
		 System.out.println(DateUtils.now("h:mm a"));
		 System.out.println(DateUtils.now("H:mm:ss:SSS"));
		 System.out.println(DateUtils.now("K:mm a,z"));
		 System.out.println(DateUtils.now("yyyy.MMMMM.dd GGG hh:mm aaa"));
		 */
	}

	public static boolean IsValidDate(Date d){
		try {
			GregorianCalendar gc = new GregorianCalendar();
			gc.setLenient(false);        // must do this
			gc.setTime(d);
			gc.getTime(); // exception thrown here
			return true;
		}
		catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	
	}

	public static boolean IsValidDateString (String sFormat, String sDateString){
		
		//First do a couple of simple tests:
		if (sDateString == null){
			return false;
		}
		sDateString = sDateString.trim();
		if (sDateString.compareToIgnoreCase("") == 0){
			return false;
		}
		
		//Now we'll check for the 'special' case of a '6 digit date' (like '060117' for 6/1/2017)
		if (sFormat.compareToIgnoreCase(clsServletUtilities.DATE_FORMAT_SIX_DIGIT_FOR_DISPLAY) == 0){
			//We have to convert this date into a standard format.
			//We know the last two digits must be the last two digits of the year, and we'll have to assume it's a '2000' date, so:
			sDateString = sDateString.substring(0, sDateString.length() - 2) + "20" + sDateString.substring(sDateString.length() - 2, sDateString.length());
			sFormat = "MMddyyyy";
		}
		
		SimpleDateFormat sdf = new SimpleDateFormat(sFormat);
		//		strFormat is the required format of the date.
	
		sdf.setLenient(false); // This is very important
	
		//		Parse the date entered by the user to check the
		//		format.
		try{
	
			//		 get the valid value into the Date class object.
			@SuppressWarnings("unused")
			java.util.Date myDate = sdf.parse(sDateString);
			//		strDate is the Date String Entered by the User.
			return true;
	
		}catch(ParseException pse){
			//		Handle Your invalid date format exception here.
			return false;
		}
	
	}

	public static final String DATETIME_FORMAT_SQL = "yyyy-MM-dd HH:mm:ss";
	public static final String DATETIME_FORMAT_STD = "MM/dd/yyyy hh:mm a";
	public static final String DATETIME_FORMAT_STD_W_SECONDS = "MM/dd/yyyy hh:mm:ss a";
	public static final String DATE_FORMAT_STD_Mdyyy = "M/d/yyyy";
	public static java.sql.Date StringTojavaSQLDate (String sFormat, String s)throws ParseException {
		SimpleDateFormat sdf = new SimpleDateFormat(sFormat);
		sdf.setLenient(false); // This is very important
	
		try{
			java.util.Date myDate = sdf.parse(s);
			return clsDateAndTimeConversions.UtilDateToSQLDate(myDate);
		}catch(ParseException pse){
			throw pse;
		}catch(Exception e){
			throw new ParseException("General exception [1424470832] - " + e.getMessage(), 0);
		}
	}

	public static java.sql.Timestamp StringToTimestamp (String sFormat, String s){
		SimpleDateFormat sdf = new SimpleDateFormat(sFormat);
		sdf.setLenient(false); // This is very important
		try{
	
			//		 get the valid value into the Date class object.
			java.util.Date myDate = sdf.parse(s);
			java.sql.Timestamp timest = new java.sql.Timestamp(myDate.getTime());
			return timest;
	
		}catch(ParseException pse){
			//		Handle Your invalid date format exception here.
			System.out.println("Error [1418853569]: " + pse.getMessage());
			return nowAsTimestamp();
		}
	}

	public static String CalendarToString (Calendar c, String sDateFormat){
		try {
			SimpleDateFormat sdf = new SimpleDateFormat(sDateFormat);
			return sdf.format(c.getTime());
		} catch (Exception e) {
			return "00/00/0000";
		}
	}

	//NOTE: This function will throw an exception for 0000-00-00 date using MySQL Connector/J 3.1 and later versions 
	//Use resultsetDateStringToString if zero dates are being passed to this function.
	public static String utilDateToString (java.util.Date d, String sDateFormat){
	
		Calendar cal = new GregorianCalendar();
		try{
			cal.setTime(d);
			SimpleDateFormat sdf = new SimpleDateFormat(sDateFormat);
			return sdf.format(cal.getTime());
		}catch (Exception e) {
			return "N/A";
		}
	}

	public static String sqlDateToString (
			java.sql.Date d, String sDateFormat) throws IllegalArgumentException{
	
		Calendar cal = new GregorianCalendar();
		try{
			cal.setTime(d);
			SimpleDateFormat sdf = new SimpleDateFormat(sDateFormat);
			return sdf.format(cal.getTime());
		}catch (IllegalArgumentException e) {
			throw e;
		}
	}

	//If this function gets passed a null, or if the date is before 2/1/1900,
	//it will return the 'sOutOfBoundsReturnString' rather than throw the exception:
	public static String sqlDateToString (
			java.sql.Date d, 
			String sDateFormat,
			String sOutOfBoundsReturnString
	) throws IllegalArgumentException{
	
		if (d == null){
			return sOutOfBoundsReturnString;
		}
	
		SimpleDateFormat sdfInput = new SimpleDateFormat("MM/dd/yyyy");
		//java.util.Date datCutOffDate;
		try {
			java.util.Date datCutOffDate = (java.util.Date) sdfInput.parse("02/01/1900");
			java.sql.Date sqlCutOffDate = new java.sql.Date(datCutOffDate.getTime());
			if (d.compareTo(sqlCutOffDate) < 0){
				return sOutOfBoundsReturnString;
			}
		} catch (ParseException p) {
			// Should never happen:
			System.out.println("Could not parse cut off date.");
		}
	
		Calendar cal = new GregorianCalendar();
		try{
			cal.setTime(d);
			SimpleDateFormat sdf = new SimpleDateFormat(sDateFormat);
			return sdf.format(cal.getTime());
		}catch (IllegalArgumentException e) {
			throw e;
		}
	}

	public static Calendar sqlDateToCalendar (java.sql.Date d){
	
		Calendar cal = new GregorianCalendar();
		cal.setTime(d);
		return cal;
	}

	public static String TimeStampToString (Timestamp ts, String sFormat){
		SimpleDateFormat sdf = new SimpleDateFormat(sFormat);
		return sdf.format(ts);
	}

	public static String TimeStampToString (
			Timestamp ts, 
			String sFormat,
			String sOutOfBoundsReturnString){
	
		if (ts == null){
			return sOutOfBoundsReturnString;
		}
	
		SimpleDateFormat sdf = new SimpleDateFormat(sFormat);
		return sdf.format(ts);
	}

	public static String TimeStampToStdString (Timestamp ts){
		return TimeStampToString (ts, DATETIME_FORMAT_STD);
	}

	public static java.sql.Date UtilDateToSQLDate(java.util.Date Input){
		Calendar c = Calendar.getInstance();
		c.setTime(Input);
		return new java.sql.Date(c.getTime().getTime());
	}

	public static java.util.Date SQLDateToUtilDate(java.sql.Date Input){
		Calendar c = Calendar.getInstance();
		c.setTime(Input);
		return c.getTime();
	}

	public static boolean IsValidDate(int iYear, int iZeroBasedMonth, int iDayOfMonth){
		//The month must be zero-based because that's the way the calendar function works . . . 
		Calendar c = Calendar.getInstance();
		c.set(Calendar.YEAR, iYear);
		c.set(Calendar.MONTH, iZeroBasedMonth);
		c.set(Calendar.DAY_OF_MONTH, iDayOfMonth);
	
		//If the year, month, or day of month values are out of range, the calendar function will 
		//roll them into the next month or year, so we'll check now to see if they still
		//match the values that were passed in:
		if (c.get(Calendar.YEAR) != iYear){
			return false;
		}
		if (c.get(Calendar.MONTH) != iZeroBasedMonth){
			return false;
		}
	
		return true;
	}

	public static java.sql.Date TimeStampToSqlDate(Timestamp ts){
		Calendar c = Calendar.getInstance();
		c.setTime(ts);
		return new java.sql.Date(c.getTime().getTime());
	}

	public static Calendar TimeStampToCalendar(Timestamp ts){
		Calendar c = Calendar.getInstance();
		c.setTime(ts);
		return c;
	}

	public static String resultsetDateStringToFormattedString (
			String rsString, String sDateFormat, String sStringToReturnIfEmpty
	) throws Exception{
	
		//If it's a null string, return the 'zero date':
		if (rsString == null){
			rsString = clsServletUtilities.EMPTY_SQL_DATE_VALUE;
		}
	
		//Convert the string to the format requested:
		try {
			return clsDateAndTimeConversions.convertDateFormat(
				rsString, 
				clsServletUtilities.DATE_FORMAT_FOR_SQL, 
				sDateFormat,
				sStringToReturnIfEmpty)
			;
		} catch (Exception e) {
			throw new Exception("Error [1489246191] - " + e.getMessage());
		}
	}

	public static String resultsetDateTimeStringToFormattedString (
			String rsString, String sDateTimeFormat, String sStringToReturnIfEmpty
	) throws Exception{
	
		//If it's a null string, return the 'zero date':
		if (rsString == null){
			rsString = clsServletUtilities.EMPTY_SQL_DATETIME_VALUE;
		}
	
		//Convert the string to the format requested:
		try {
			return clsDateAndTimeConversions.convertDateFormat(
				rsString, 
				clsServletUtilities.DATETIME_FORMAT_FOR_SQL, 
				sDateTimeFormat,
				sStringToReturnIfEmpty)
			;
		} catch (Exception e) {
			throw new Exception("Error [1489246181] - " + e.getMessage());
		}
	}

	public static String stdDateTimeToSQLDateTimeString(String sAMPMDateString){
		//This takes a string of the form: "00/00/0000 00:00 AM"
		// and converts it to a SQL string that can be used in SQL commands:
		SimpleDateFormat sdf = new SimpleDateFormat("M/d/yyyy hh:mm a");
		sdf.setLenient(false); // This is very important
		try{
			java.util.Date myDate = sdf.parse(sAMPMDateString);
			java.sql.Timestamp ts = new java.sql.Timestamp(myDate.getTime());
			return TimeStampToString(ts, "yyyy-MM-dd HH:mm", "0000-00-00 00:00");
		}catch(ParseException pse){
			return "0000-00-00 00:00";
		}
	}

	public static String stdDateStringToSQLDateString (String sMdyyyy){
		//Modified on 9/30/2015 by BZ to allow for M/d/yy formats as well:
		//Check to see if date is in M/d/yy format (i.e., using a 2 digit year)
		if( (sMdyyyy.substring(sMdyyyy.lastIndexOf("/"), sMdyyyy.length()).length() < 4)){
			String syear = "";
			syear = Integer.toString(Calendar.getInstance().get(Calendar.YEAR));
			syear = syear.substring(0,2);
			
			return syear + clsStringFunctions.StringRight(sMdyyyy, 2) 
			+ "-"
			+ clsStringFunctions.PadLeft(sMdyyyy.substring(0, sMdyyyy.indexOf("/")), "0", 2)
			+ "-"
			+ clsStringFunctions.PadLeft(sMdyyyy.substring(
					sMdyyyy.indexOf("/") + 1, sMdyyyy.lastIndexOf("/")), "0", 2)
					;
		} 
		//If the date is NOT using a 2 digit year, then just parse it out and return:
		return clsStringFunctions.StringRight(sMdyyyy, 4) 
			+ "-"
			+ clsStringFunctions.PadLeft(sMdyyyy.substring(0, sMdyyyy.indexOf("/")), "0", 2)
			+ "-"
			+ clsStringFunctions.PadLeft(sMdyyyy.substring(
				sMdyyyy.indexOf("/") + 1, sMdyyyy.lastIndexOf("/")), "0", 2)
		;	
	}

	public static long getFirstDateMinusSecondDateInDays(String sFirstDateAsMdyyyy, String sSecondDateAsMdyyyy) throws Exception{
		//Subtract dates:
		  Calendar calendar1 = Calendar.getInstance();
		  Calendar calendar2 = Calendar.getInstance();
		  try {
			calendar1.setTime(StringTojavaSQLDate("M/d/yyyy", sFirstDateAsMdyyyy));
		} catch (ParseException e) {
			throw new Exception("Error:[1423662925] Invalid Date: '" + sFirstDateAsMdyyyy + "' - " + e.getMessage());
		}
		  //calendar1.set(2007, 01, 10);
		  try {
			calendar2.setTime(StringTojavaSQLDate("M/d/yyyy", sSecondDateAsMdyyyy));
		} catch (ParseException e) {
			throw new Exception("Error:[1423662926] Invalid Date: '" + sSecondDateAsMdyyyy + "' - " + e.getMessage());
		}
		  //calendar2.set(2007, 07, 01);
		  long milliseconds1 = calendar1.getTimeInMillis();
		  long milliseconds2 = calendar2.getTimeInMillis();
		  long diff = milliseconds1 - milliseconds2;
		  //long diffSeconds = diff / 1000;
		  //long diffMinutes = diff / (60 * 1000);
		  //long diffHours = diff / (60 * 60 * 1000);
		  return diff / (24 * 60 * 60 * 1000);
	}

	public static String resultsetDateStringToString (
			String rsString
	){
		//2010-02-10 00:00:00
	
		//If it's a null string, return the 'zero date':
		if (rsString == null){
			return "00/00/0000";
		}
	
		//Try to parse off the string:
		String sYear = clsStringFunctions.StringLeft(rsString, 4);
	
		if (Integer.parseInt(sYear) < 1970){
			return "00/00/0000";
		}
	
		String sMonth = rsString.substring(5, 6).replace("0", "") + rsString.substring(6, 7);
		String sDay = rsString.substring(8, 9).replace("0", "") + rsString.substring(9, 10);
	
		String s = 
			sMonth
			+ "/"
			+ sDay
			+ "/"
			+ sYear
			;
		return s;
	}

	public static String resultsetDateTimeStringToString (
			String rsString
	){
	
		//If it's a null string, return the 'zero date':
		if (rsString == null){
			return "00/00/0000 00:00 AM";
		}
	
		//Try to parse off the string:
		String sYear = clsStringFunctions.StringLeft(rsString, 4);
		if (Integer.parseInt(sYear) < 1970){
			//System.out.println("Integer.parseInt(sYear) < 1970) is YES");
			return "00/00/0000 00:00 AM";
		}
		String sMonth = rsString.substring(5, 6).replace("0", "") + rsString.substring(6, 7);
		String sDay = rsString.substring(8, 9).replace("0", "") + rsString.substring(9, 10);
	
		String sHour = "";
		String sMinute = "";
		String sAMPM = "";
		if (rsString.length() > 10){
			//It's a date time string, with time in it, so we need to set the hour, minute, and second:
			sHour = rsString.substring(11, 13);
			//Adjust for AM/PM:
			int iHour = Integer.parseInt(sHour);
			if (iHour > 11){
				sHour = Integer.toString(iHour - 12);
				sAMPM = "PM";
			}else{
				sAMPM = "AM";
			}
			if ((iHour == 12) || (iHour == 0)){
				sHour = "12";
			}
			sMinute = rsString.substring(14, 16);
		}
	
		if (Integer.parseInt(sYear) < 1970){
			return "00/00/0000 00:00 AM";
		}
	
		String s = 
			sMonth
			+ "/"
			+ sDay
			+ "/"
			+ sYear
			+ " "
			+ sHour
			+ ":"
			+ sMinute
			+ " "
			+ sAMPM
			;
		return s;
	}

	public static String convertDateFormat(
		String sDateString, 
		String sFromDateFormat, 
		String sToDateFormat,
		String sStringToReturnIfDateIsEmpty) throws Exception{
		
		if (sDateString == null){
			return sStringToReturnIfDateIsEmpty;
		}
		
		//We may be dealing with a '6 digit date', like '060117' for 6/1/2017.
		//If so, we may have to convert it to a different format before java will evaluate it properly:
		if (sFromDateFormat.compareToIgnoreCase(clsServletUtilities.DATE_FORMAT_SIX_DIGIT_FOR_DISPLAY) == 0){
			//We have to convert this date into a standard format.
			//We know the last two digits must be the last two digits of the year, and we'll have to assume it's a '2000' date, so:
			sDateString = sDateString.substring(0, sDateString.length() - 2) + "20" + sDateString.substring(sDateString.length() - 2, sDateString.length());
			sFromDateFormat = "MMddyyyy";
		}
		
		String s = "";
	    DateFormat sFromFormat = new SimpleDateFormat(sFromDateFormat);
	    java.util.Date datTestDate;
		try {
			datTestDate = sFromFormat.parse(sDateString);
		} catch (Exception e) {
			throw new Exception("Error [1488923222] converting date string '" + sDateString + "'"
				+ " with format '" + sFromDateFormat + "'"
				+ " to date format '" + sToDateFormat + "' - " + e.getMessage());
		}
		
		DateFormat sToFormat = new SimpleDateFormat(sToDateFormat);
		
		//Make sure it's not an invalid date by checking to see that it's in a valid date range:
		SimpleDateFormat dateformatcheck = new SimpleDateFormat("M-dd-yyyy");
		java.util.Date datDateZero = dateformatcheck.parse("1-01-1900");
		
		if (datTestDate.before(datDateZero)){
			return sStringToReturnIfDateIsEmpty;
		}
	    
	    s = sToFormat.format(datTestDate);
		return s;
	}

	public static String getElapsedTime(long lStartTime) {
		String sDuration = "";
		long duration = System.currentTimeMillis() - lStartTime;
		long days = duration / (1000 * 60 * 60 * 24);
		if (days > 0){
			duration = duration - (days * (1000 * 60 * 60 * 24));
			if (days > 1){
				sDuration = Long.toString(days) + " days";
			}else{
				sDuration = Long.toString(days) + " day";
			}
		}
		long hours = duration / (1000 * 60 * 60);
		if (hours > 0){
			duration = duration - (hours * (1000 * 60 * 60));
		}
		if (sDuration.compareToIgnoreCase("") != 0){
			if (hours == 1){
				sDuration += ", " + hours + " hour";
			}else{
				sDuration += ", " + hours + " hours";
			}
		}else{
			if (hours == 1){
				sDuration += hours + " hour";
			}else{
				sDuration += hours + " hours";
			}
		}
		long minutes = duration / (1000 * 60);
		if (minutes > 0){
			duration = duration - (minutes * (1000 * 60));
		}
		if (sDuration.compareToIgnoreCase("") != 0){
			if (minutes == 1){
				sDuration += ", " + minutes + " minute";
			}else{
				sDuration += ", " + minutes + " minutes";
			}
		}else{
			if (minutes == 1){
				sDuration += minutes + " minute";
			}else{
				sDuration += minutes + " minutes";
			}
		}
		long seconds = duration / (1000);
		if (sDuration.compareToIgnoreCase("") != 0){
			if (seconds == 1){
				sDuration += ", " + seconds + " second";
			}else{
				sDuration += ", " + seconds + " seconds";
			}
		}else{
			if (seconds == 1){
				sDuration += seconds + " second";
			}else{
				sDuration += seconds + " seconds";
			}
		}
		return sDuration;
	}
	
	public static java.sql.Date StringToSQLDateStrict (String sFormat, String s){
		SimpleDateFormat sdf = new SimpleDateFormat(sFormat);

//		strFormat is the required format of the date.

		sdf.setLenient(false); // This is very important

//		Parse the date entered by the user to check the
//		format.
		try{

//		 get the valid value into the Date class object.
		java.util.Date myDate = sdf.parse(s);
		return clsDateAndTimeConversions.UtilDateToSQLDate(myDate);

		}catch(ParseException pse){
//		Handle Your invalid date format exception here.
			return null;
		}

	}
	
	public static long DateStringToLong (String sDateString){
		
		try{
			return Long.parseLong(sDateString.replace("-", ""));
		}catch (NumberFormatException e) {
			return 0;
		}
	}
	
	public static boolean testResultSetTSFieldForNull(ResultSet rs, String sFieldName){
		
		try{
			@SuppressWarnings("unused")
			Timestamp ts = rs.getTimestamp(sFieldName);
		}catch(SQLException e){
			return false;
		}
		return true;
	}

}
