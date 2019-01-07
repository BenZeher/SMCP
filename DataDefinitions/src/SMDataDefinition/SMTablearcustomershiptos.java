package SMDataDefinition;

public class SMTablearcustomershiptos {
	
	//Table Name
	public static final String TableName = "arcustomershiptos";
	
	//Field names:
	public static String sCustomerNumber = "sCustomerNumber";
	public static String sShipToCode = "sShipToCode";
	public static String sDescription = "sDescription";
	public static String sAddressLine1 = "sAddressLine1";
	public static String sAddressLine2 = "sAddressLine2";
	public static String sAddressLine3 = "sAddressLine3";
	public static String sAddressLine4 = "sAddressLine4";
	public static String sCity = "sCity";
	public static String sState = "sState";
	public static String sCountry = "sCountry";
	public static String sPostalCode = "sPostalCode";
	//public static String sTaxGroup = "sTaxGroup";
	public static String sContactName = "sContactName";
	public static String sPhoneNumber = "sPhoneNumber";
	public static String sFaxNumber = "sFaxNumber";
	
	//Field Lengths:
	public static int sCustomerNumberLength = 12;
	public static int sShipToCodeLength = 6;
	public static int sDescriptionLength = 60;
	public static int sAddressLine1Length = 60;
	public static int sAddressLine2Length = 60;
	public static int sAddressLine3Length = 60;
	public static int sAddressLine4Length = 60;
	public static int sCityLength = 30;
	public static int sStateLength = 30;
	public static int sCountryLength = 30;
	public static int sPostalCodeLength = 20;
	//public static int sTaxGroupLength = 12;
	public static int sContactNameLength = 60;
	public static int sPhoneNumberLength = 30;
	public static int sFaxNumberLength = 30;
	
	/*
create table arcustomershiptos (
sCustomerNumber varchar(12) NOT NULL default '',
sShipToCode varchar(6) NOT NULL default '',
sDescription varchar(60) NOT NULL default '',
sAddressLine1 varchar(60) NOT NULL default '',
sAddressLine2 varchar(60) NOT NULL default '',
sAddressLine3 varchar(60) NOT NULL default '',
sAddressLine4 varchar(60) NOT NULL default '',
sCity varchar(30) NOT NULL default '',
sState varchar(30) NOT NULL default '',
sCountry varchar(30) NOT NULL default '',
sPostalCode varchar(20) NOT NULL default '',
sTaxGroup varchar (12) NOT NULL default '',
sContactName varchar(60) NOT NULL default '',
sPhoneNumber varchar(30) NOT NULL default '',
sFaxNumber varchar(30) NOT NULL default '',
PRIMARY KEY (sCustomerNumber)
) Engine=InnoDB;
	*/
}
