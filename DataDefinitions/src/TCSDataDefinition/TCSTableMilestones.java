package TCSDataDefinition;

public class TCSTableMilestones {

	//Table Name
	public static String TableName = "Milestones"; 

	//Field List
	public static final String lid = "lid"; 
	public static final String sName = "sName"; 
	public static final String sDescription = "sDescription";
	public static final String sEmployeeTypeID = "sEmployeeTypeID"; 
	
	public static final int sNameLength = 60;
	public static final int sEmployeeTypeIDLength = 9;
	
	/*
CREATE TABLE Milestones(
lid int(11) NOT NULL auto_increment COMMENT '[040101] Unique auto_incrementing ID'
, sName varchar(60) NOT NULL DEFAULT '' 
, sDescription MEDIUMTEXT NOT NULL
, sEmployeeTypeID varchar(9) NOT NULL DEFAULT '' COMMENT '[040201] M:1'
, PRIMARY KEY  (`lid`)
) ENGINE=InnoDB COMMENT='Table [T0401]Milestones - 1:M to [T0403]EmployeeMilestones, M:1 to [T0402]EmployeeTypes';
	*/
}