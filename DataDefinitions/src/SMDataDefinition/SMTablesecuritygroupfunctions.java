package SMDataDefinition;

public class SMTablesecuritygroupfunctions {

	public static String TableName = "securitygroupfunctions";
	/*
	 *  +------------+-------------+------+-----+---------+-------+
		| Field      | Type        | Null | Key | Default | Extra |
		+------------+-------------+------+-----+---------+-------+
		| sGroupName | varchar(50) |      | PRI |         |       |
		| sFunction  | varchar(50) |      | PRI |         |       |
		+------------+-------------+------+-----+---------+-------+
	 */
	public static String sGroupName = "sGroupName";
	public static String sFunction = "sFunction";
	public static String ifunctionid = "ifunctionid";
	
	public static int sGroupNameLength = 50;
	public static int sFunctionLength = 50;
	
}
