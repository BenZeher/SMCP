package SMDataDefinition;

public class SMTablesseventschedules {

	public static final String TableName = "sseventschedules";
	
	public static final String lid = "lid";
	public static final String sdescription = "sdescription";
	public static final String sname = "sname";
	public static final String iactive = "iactive";
	public static final String istarttime = "istarttime";  //Minutes after midnight
	public static final String idurationinminutes = "idurationinminutes";
	public static final String idaysoftheweek  = "idaysoftheweek";  
	
	public static final int sdescriptionlength = 254;
	public static final int snamelength = 64;
	
	//Each day of the week is represented by a consecutive integer: Sunday is 1, Monday is 2, etc.
	//The value for the day of the week is 2 to the nth power, where n is the day of the week.
	//So Wednesday is digit 4, and Wednesday is recorded as 2 ^ 4, which equals 16.  If a schedule
	// is supposed to happen on Monday, Tuesday, Wednesday, Thursday, and Friday, then all the weekday values are
	// totaled: 4 + 8 + 16 + 32 + 62, and the value for the 'idaysoftheweek' field would be 124
}
