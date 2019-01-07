package SMDataDefinition;

public class SMTablechangeorders {

	//Table Name
	public static final String TableName = "changeorders";
	
	//Field names:
	public static final String iID = "id";
	public static final String sJobNumber = "sjobnumber";
	public static final String dChangeOrderNumber = "dchangeordernumber";
	public static final String datChangeOrderDate = "dattimechangeorderdate";
	public static final String sDesc = "sdescription"; 
	public static final String dAmount = "damount"; 
	public static final String dTotalMarkUp = "dtotalmarkup"; 
	public static final String dTruckDays = "dtruckdays"; 
	
	//Field Lengths:
	public static final int sJobNumberLength = 20;
	public static final int sDescriptionLength = 75;
	public static final int dAmountScale = 2;
	public static final int dTotalMarkUPScale = 2;
	public static final int dTruckDaysScale = 4;
	
}
