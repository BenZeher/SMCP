package SMDataDefinition;

public class SMTableglstatementforms {
	public static final String TableName = "glstatementforms";
	
	public static final String lid = "lid";
	public static final String sname = "sname";
	public static final String sdescription = "sdescription";
	public static final String mtext = "mtext";

	//Lengths
	public static final int snamelength = 64;
	public static final int sdescriptionlength = 128;
	
	/*
	SQL = "CREATE TABLE glstatementforms("
			+ "lid int(11) NOT NULL auto_increment COMMENT '[091101]'"
			+ ", sname varchar(64) NOT NULL DEFAULT ''"
			+ ", sdescription varchar(128) NOT NULL DEFAULT ''"
			+ ", mtext MEDIUMTEXT NOT NULL"
			+ ", PRIMARY KEY  (`lid`)"
			+ ", UNIQUE KEY `name_key` (`sname`)"
			+ " ) ENGINE=InnoDB"
		;
	*/
}
