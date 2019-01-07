package SMDataDefinition;

public class SMTablesecurityfunctions {

	public static String TableName = "securityfunctions";
	/*
	 *  +---------------+-------------+------+-----+---------+-------+
		| Field         | Type        | Null | Key | Default | Extra |
		+---------------+-------------+------+-----+---------+-------+
		| iFunctionID   | int(10)     |      | PRI | 0       |       |
		| sFunctionName | varchar(50) |      | PRI |         |       |
		+---------------+-------------+------+-----+---------+-------+
	 */
	public static String iFunctionID = "iFunctionID";
	public static String sFunctionName = "sFunctionName";
	public static String slink = "slink";
	public static String sDescription = "sdescription";
	public static String imodulelevelsum = "imodulelevelsum";
	
	public static int sFunctionNameLength = 50;
	public static int slinkLength = 128;
}
