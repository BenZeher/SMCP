package TCSDataDefinition;

public class TCSTableEmployeeTypeAccess {

	//Table Name
	public static String TableName = "EmployeeTypeAccess"; 

	//Field List
	public static final String sEmployeeID = "sEmployeeID"; 
	public static final String sEmployeeTypeID = "sEmployeeTypeID"; 

	
	/*
CREATE TABLE EmployeeTypeAccess(
sEmployeeID varchar(9) NOT NULL DEFAULT '' COMMENT '[010101] M:1 CPK'
, sEmployeeTypeID varchar(9) NOT NULL DEFAULT '' COMMENT '[040201] M:1 CPK'
, PRIMARY KEY  (`sEmployeeID`, `sEmployeeTypeID`)
) ENGINE=InnoDB COMMENT='Table [T0405]EmployeeTypeAccess - M:1 to [T0101]Employees, M:1 to [T0402]EmployeeTypes';
	*/
}