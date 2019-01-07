package SMDataDefinition;

public class SMTableicvendorterms {
	
	//Table Name
	public static final String TableName = "icvendorterms";
	
	//Field names:
	public static final String sTermsCode = "stermscode";
	public static final String sDescription = "sdescription";
	public static final String iActive = "iactive";
	public static final String datLastMaintained = "datlastmaintained";
	public static final String bdDiscountPercent = "bddiscountpercent"; 
	public static final String iDiscountNumberOfDays = "idiscountnumberofdays"; 
	public static final String iDiscountDayOfTheMonth = "idiscountdayofthemonth"; 
	public static final String iDueNumberOfDays = "iduenumberofdays"; 
	public static final String iDueDayOfTheMonth = "iduedayofthemonth";
	
	//If the due day follows very close on the invoice date, some vendors will allow the due date of the month to be moved to the following month.
	//For example: 
	public static final String iminimumdaysallowedforduedayofmonth = "iminimumdaysallowedforduedayofmonth";
	public static final String iminimumdaysallowedfordiscountduedayofmonth = "iminimumdaysallowedfordiscountduedayofmonth";
	
	//Field Lengths:
	public static final int sTermsCodeLength = 6;
	public static final int sDescriptionLength = 60;
	
	//Field scale:
	public static final int bdDiscountPercentScale = 4;

}