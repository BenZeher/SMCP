package TCSDataDefinition;

public class TCSTableEmployeeTypes {

	//Table Name
	public static String TableName = "EmployeeTypes";

	//Field List
	public static final String lid = "lid";
	public static final String sName = "sName";
	public static final String sDescription = "sDescription";
	
	public static final int sNameLength = 60;
	/*
CREATE TABLE EmployeeTypes(
lid int(11) NOT NULL auto_increment COMMENT '[040201] Unique auto_incrementing ID'
, sName varchar(60) NOT NULL DEFAULT '' 
, sDescription MEDIUMTEXT NOT NULL
, PRIMARY KEY  (`lid`)
) ENGINE=InnoDB COMMENT='Table [T0402]EmployeeTypes - 1:M to [T0101]Employees, 1:M to [T0401]Milestones';
	*/
}