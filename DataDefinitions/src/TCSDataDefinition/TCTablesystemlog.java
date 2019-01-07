package TCSDataDefinition;

public class TCTablesystemlog {
	//Table Name
	public static final String TableName = TimeCardSystemTables.TCSystemLog;
	
	//Field names:
	public static final String id = "id";
	public static final String datlogdate = "datlogdate";
	public static final String suser = "suser";
	public static final String soperation = "soperation";
	public static final String sdescription = "sdescription";
	public static final String scomment = "scomment";
	public static final String sreferenceid = "sreferenceid";

	//Field Lengths:
	public static final int suserLength = 128;
	public static final int ssoperationLength = 128;
	public static final int sreferenceidLength = 12;
	public static final int scommentLength = 65535;
	public static final int ssdescriptionLength = 65535;
	
	/*
CREATE TABLE `systemlog` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `datlogdate` datetime DEFAULT NULL,
  `suser` varchar(128) NOT NULL DEFAULT '',
  `soperation` varchar(128) DEFAULT NULL,
  `sdescription` text,
  `scomment` text,
  `sreferenceid` varchar(12) NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `date_key` (`datlogdate`),
  KEY `user_key` (`suser`),
  KEY `operation_key` (`soperation`)
) ENGINE=InnoDB
    */
}
