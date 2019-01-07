package SMDataDefinition;

public class SMTablearterms {
	
	//Table Name
	public static final String TableName = "arterms";
	
	//Field names:
	public static final String sTermsCode = "sTermsCode";
	public static final String sDescription = "sDescription";
	public static final String iActive = "iActive";
	public static final String datLastMaintained = "datLastMaintained";
	public static final String dDiscountPercent = "dDiscountPercent"; 
	public static final String iDiscountNumberOfDays = "iDiscountNumberOfDays"; 
	public static final String iDiscountDayOfTheMonth = "iDiscountDayOfTheMonth"; 
	public static final String iDueNumberOfDays = "iDueNumberOfDays"; 
	public static final String iDueDayOfTheMonth = "iDueDayOfTheMonth"; 
	
	//Field Lengths:
	public static final int sTermsCodeLength = 6;
	public static final int sDescriptionLength = 60;

}

//Create table SQL:
/*
create table arterms (
`sTermsCode` varchar(6) NOT NULL default '',
`sDescription` varchar(60) NOT NULL default '',
`iActive` int(11) NOT NULL default '1',
`datLastMaintained` datetime NOT NULL default '0000-00-00 00:00:00',
`dDiscountPercent` double NOT NULL default '0.00',
`iDiscountNumberOfDays` int(11) NOT NULL default '0',
`iDiscountDayOfTheMonth` int(11) NOT NULL default '0',
`iDueNumberOfDays` int(11) NOT NULL default '0',
`iDueDayOfTheMonth` int(11) NOT NULL default '0',
PRIMARY KEY (sTermsCode)
) Engine=InnoDB;
*/