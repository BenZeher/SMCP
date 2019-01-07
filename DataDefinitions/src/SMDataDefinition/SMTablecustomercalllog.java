package SMDataDefinition;

public class SMTablecustomercalllog {

	/*
	 * 
		+----------------+--------------+------+-----+---------------------+----------------+
		| Field          | Type         | Null | Key | Default             | Extra          |
		+----------------+--------------+------+-----+---------------------+----------------+
		| id             | int(11)      |      | PRI | NULL                | auto_increment |
		| datLogTime     | datetime     |      |     | 0000-00-00 00:00:00 |                |
		| sCustomerName  | varchar(100) |      |     |                     |                |
		| sPhoneNumber   | varchar(30)  | YES  |     |                     |                |
		| sCity          | varchar(50)  | YES  |     |                     |                |
		| sUserName      | varchar(128) |      |     |                     |                |
		| datCallTime    | datetime     |      |     | 0000-00-00 00:00:00 |                |
		| iOrderSourceID | int(11)      |      |     | 0                   |                |
		| mNote          | text         | YES  |     | NULL                |                |
		+----------------+--------------+------+-----+---------------------+----------------+
	 */
	public static String TableName = "customercalllog";
	//OBSOLETE
	//Field names:
	public static String id = "id";
	public static String datLogTime = "datLogTime";
	public static String sCustomerName = "sCustomerName"; 
	public static String sPhoneNumber = "sPhoneNumber"; 
	public static String sCity = "sCity"; 
	public static String sUserName = "sUserName"; 
	public static String datCallTime = "datCallTime"; 
	public static String iOrderSourceID = "iOrderSourceID";
	public static String mNote = "mNote";  
	
}
