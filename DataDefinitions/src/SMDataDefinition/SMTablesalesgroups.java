package SMDataDefinition;

public class SMTablesalesgroups {

	/*
		+-----------------+--------------+------+-----+---------+-------+
		| Field           | Type         | Null | Key | Default | Extra |
		+-----------------+--------------+------+-----+---------+-------+
		| iSalesGroupId   | int(11)      | NO   | PRI | 0       |       | 
		| sSalesGroupCode | varchar(8)   | YES  |     |         |       | 
		| sSalesGroupDesc | varchar(255) | YES  |     |         |       | 
		+-----------------+--------------+------+-----+---------+-------+

	 */
	//Table Name
	public static String TableName = "salesgroups";
	
	//Field names:
	public static String iSalesGroupId = "iSalesGroupId";
    public static String sSalesGroupCode = "sSalesGroupCode";
    public static String sSalesGroupDesc = "sSalesGroupDesc";
    
    //Field lengths:
    public static int sSalesGroupCodeLength = 8;
    public static int sSalesGroupDescLength = 255;
    
}
