package SMDataDefinition;

public class SMTablearcustomergroups {
	
	//Table Name
	public static final String TableName = "arcustomergroups";
	
	//Field names:
	public static final String sGroupCode = "sGroupCode";
	public static final String sDescription = "sDescription";
	public static final String iActive = "iActive";
	public static final String datLastMaintained = "datLastMaintained";
	public static final String sLastEditUserFullName = "sLastEditUserFullName";
	public static final String lLastEditUserID = "lLastEditUserID";
	
	//Field lengths:
	public static final int sGroupCodeLength = 6;
	public static final int sDescriptionLength = 60;
	public static final int sLastEditUserLength = 128;
	
	/*
create table arcustomergroups (
sGroupCode varchar(6) NOT NULL default '',
sDescription varchar(60) NOT NULL default '',
iActive int(11) NOT NULL default '1',
datLastMaintained datetime NOT NULL default '0000-00-00 00:00:00',
sLastEditUser varchar(128) NOT NULL default '',
PRIMARY KEY (sGroupCode)
) Engine=InnoDB;		
	*/
}


