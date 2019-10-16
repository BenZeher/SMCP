package SMDataDefinition;

public class SMTablesalescontacts {
	
	/* 
	 *  CREATE TABLE `salescontacts` (
		`id` int(11) NOT NULL auto_increment,
		`scustomernumber` varchar(12) NOT NULL default '',
		`salespersoncode` varchar(3) NOT NULL default '',
		`scustomername` varchar(60) NOT NULL default '',
		`scontactname` varchar(60) NOT NULL default '',
		`sphonenumber` varchar(20) NOT NULL default '',
		`emailaddress` varchar(75) NOT NULL default '',
		`datlastcontactdate` datetime NOT NULL default '0000-00-00 00:00:00',
		`datnextcontactdate` datetime NOT NULL default '0000-00-00 00:00:00',
		`mnotes` mediumtext,
		`binactive` int(11) NOT NULL default '0',
		`sdescription varchar(128) default ''
		PRIMARY KEY (`id`),
		UNIQUE KEY `customer_salesperson` (`scustomernumber`,`salespersoncode`),
		KEY `salesperson_key` (`salespersoncode`),
		KEY `customer_key` (`scustomernumber`)
		) ENGINE=InnoDB 
	 */
	public static String OBJECT_NAME = "SalesContact";
	public static String TableName = "salescontacts";
	
	//Field names:
	public static String id = "id";
	public static String scustomernumber = "scustomernumber";
	public static String salespersoncode = "salespersoncode";
	public static String scustomername = "scustomername";
	public static String scontactname = "scontactname";
	public static String sphonenumber = "sphonenumber";
	public static String semailaddress = "semailaddress";
	public static String mnotes = "mnotes";
	public static String binactive = "binactive";
	public static String sdescription = "sdescription";
	public static String sSalespersonName = "sSalespersonName";
	
	public static int scustomernumberlength = 12;
	public static int salespersoncodelength = 8;
	public static int scustomernamelength = 60;
	public static int scontactnamelength = 60;
	public static int sphonenumberlength = 20;
	public static int semailaddresslength = 75;
	public static int sdescriptionlength = 128;
}
