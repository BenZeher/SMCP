package SMDataDefinition;

public class SMTablecolortable {
/*
	+------------+--------------+------+-----+---------+-------+
	| Field      | Type         | Null | Key | Default | Extra |
	+------------+--------------+------+-----+---------+-------+
	| irow       | mediumint(8) | NO   | PRI | 0       |       |
	| icol       | mediumint(8) | NO   | PRI | 0       |       |
	| scolorcode | varchar(6)   | YES  |     | 000000  |       |
	+------------+--------------+------+-----+---------+-------+
*/
	//Table Name
	public static String TableName = "colortable";
	
	//Field names:
    public static String irow = "irow";
    public static String icol = "icol";
    public static String scolorcode = "scolorcode";
    
    //Field lengths:
    public static int scolorcodelength = 6;
    
}
