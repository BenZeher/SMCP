package SMDataDefinition;

public class SMTablefadepreciationtype {
	//Table Name
	public static final String TableName = "fa_depreciationtype";
	
	//Field names:
	public static final String sDepreciationType  = "sDepreciationType";
	public static final String sCalculationType = "sCalculationType";
	public static final String iLifeInMonths = "iLifeInMonths";
	/*
	+-------------------+-------------+------+-----+---------+-------+
	| Field             | Type        | Null | Key | Default | Extra |
	+-------------------+-------------+------+-----+---------+-------+
	| sDepreciationType | varchar(12) | NO   |     |         |       |
	| sCalculationType  | varchar(12) | YES  |     | NULL    |       |
	| iLifeInMonths     | int(11)     | YES  |     | NULL    |       |
	+-------------------+-------------+------+-----+---------+-------+
	*/
	//Field lengths:
	public static final int sDepreciationTypeLength = 12;
	public static final int sCalculationTypeLength = 12;
	
}
