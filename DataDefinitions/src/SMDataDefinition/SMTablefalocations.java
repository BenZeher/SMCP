package SMDataDefinition;

public class SMTablefalocations {
	//Table Name
	public static final String TableName = "fa_locations";
	//+-----------------+-------------+------+-----+---------+-------+
	//| Field           | Type        | Null | Key | Default | Extra |
	//+-----------------+-------------+------+-----+---------+-------+
	//| sLocLocation    | varchar(6)  | NO   |     |         |       |
	//| sLocDescription | varchar(30) | YES  |     |         |       |
	//+-----------------+-------------+------+-----+---------+-------+
	//Field names:
	public static final String sLocLocation  = "sLocLocation";
	public static final String sLocDescription = "sLocDescription";
	
	//Field lengths:
	public static final int sLocLocationLength = 6;
	public static final int sLocDescriptionLength = 30;
	
}
