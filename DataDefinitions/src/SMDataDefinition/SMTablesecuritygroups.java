package SMDataDefinition;

public class SMTablesecuritygroups {
	
	public static String TableName = "securitygroups";
	/*
	 *  +--------------------+-------------+------+-----+---------+-------+
		| Field              | Type        | Null | Key | Default | Extra |
		+--------------------+-------------+------+-----+---------+-------+
		| iGroupID           | int(10)     |      | PRI | 0       |       |
		| sSecurityGroupName | varchar(50) |      | PRI |         |       |
		| sSecurityGroupDesc | text        | YES  |     | NULL    |       |
		+--------------------+-------------+------+-----+---------+-------+
	 */
	public static String iGroupID = "iGroupID";
	public static String sSecurityGroupName = "sSecurityGroupName";
	public static String sSecurityGroupDesc = "sSecurityGroupDesc";
	
}
