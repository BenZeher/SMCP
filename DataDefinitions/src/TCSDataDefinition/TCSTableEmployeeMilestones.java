package TCSDataDefinition;

public class TCSTableEmployeeMilestones {

	//Table Name
	public static String TableName = "EmployeeMilestones"; 

	//Field List
	public static final String lid = "lid"; 
	public static final String sEmployeeTypeID = "sEmployeeTypeID"; 
	public static final String sEmployeeID = "sEmployeeID";
	public static final String sEmployeeFirstName = "sEmployeeFirstName"; 
	public static final String sEmployeeMiddleName = "sEmployeeMiddleName"; 
	public static final String sEmployeeLastName = "sEmployeeLastName";
	public static final String sMilestoneID = "sMilestoneID";
	public static final String sMilestoneName = "sMilestoneName"; 
	public static final String sMilestoneDescription = "sMilestoneDescription";
	public static final String datDateCompleted = "datDateCompleted";	
	public static final String datEntryDate = "datEntryDate";
	public static final String sRecordedByID = "sRecordedByID"; 
	public static final String sRecordedByFirstName = "sRecordedByFirstName"; 
	public static final String sRecordedByMiddleName = "sRecordedByMiddleName";
	public static final String sRecordedByLastName = "sRecordedByLastName";
	public static final String mComment = "mComment";

	
	/*
CREATE TABLE EmployeeMilestones(
 lid int(11) NOT NULL auto_increment COMMENT '[040301] Unique auto_incrementing ID'
, sEmployeeTypeID varchar(9) NOT NULL DEFAULT '' COMMENT '[040201] M:1'
, sEmployeeID varchar(9) NOT NULL DEFAULT '' COMMENT '[010101] M:1'
, sEmployeeFirstName varchar(20) NOT NULL DEFAULT '' 
, sEmployeeMiddleName varchar(20) NOT NULL DEFAULT ''
, sEmployeeLastName varchar(20) NOT NULL DEFAULT '' 
, sMilestoneID varchar(9) NOT NULL DEFAULT '' COMMENT '[040101] M:1'
, sMilestoneName varchar(60) NOT NULL DEFAULT '' 
, sMilestoneDescription MEDIUMTEXT NOT NULL
, datDateCompleted DATETIME NOT NULL DEFAULT '0000-00-00 00:00:00'
, datEntryDate  DATETIME NOT NULL default '0000-00-00 00:00:00'
, sRecordedByID  varchar(9) NOT NULL default '' COMMENT '[010101] M:1'
, sRecordedByFirstName varchar(20) NOT NULL DEFAULT '' 
, sRecordedByMiddleName varchar(20) NOT NULL DEFAULT ''
, sRecordedByLastName varchar(20) NOT NULL DEFAULT ''  
, PRIMARY KEY  (`lid`)
) ENGINE=InnoDB COMMENT='Table [T0403]EmployeeMilestones - M:1 to [T0401]Milestones, M:1 to [T0402]EmployeeTypes, M:1 [T0101]Employees';

ALTER TABLE EmployeeMilestones ADD COLUMN `mComment` MEDIUMTEXT NOT NULL;
	*/
}