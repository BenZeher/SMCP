package TCSDataDefinition;

public class TCSTableEmployeeTypeLinks {

	//Table Name
	public static String TableName = "EmployeeTypeLinks"; 

	//Field List
	public static final String sEmployeeID = "sEmployeeID"; 
	public static final String sEmployeeTypeID = "sEmployeeTypeID"; 

	
	/*
CREATE TABLE EmployeeTypeLinks(
sEmployeeID varchar(9) NOT NULL DEFAULT '' COMMENT '[010101] M:1'
, sEmployeeTypeID varchar(9) NOT NULL DEFAULT '' COMMENT '[040201] M:1'
, PRIMARY KEY  (`sEmployeeID`, `sEmployeeTypeID`)
) ENGINE=InnoDB COMMENT='Table [T0404]EmployeeTypeLinks - M:1 to [T0101]Employees, M:1 to [T0402]EmployeeTypes';
	*/
}