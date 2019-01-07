package TCSDataDefinition;

public class TCSTablemadgiceventusers {

	//Table Name
	public static String TableName = "madgiceventusers";

	//Field List
	public static final String lid = "lid";
	public static final String semployeeid = "semployeeid";
	public static final String semployeefullname = "semployeefullname";
	public static final String lmadgiceventid = "lmadgiceventid";
	
	public static final int semployeeidLength = 9;
	public static final int semployeefullnameLength = 128;
	
	/*
	create table madgiceventusers (
	lid int(11) NOT NULL AUTO_INCREMENT,
	semployeeid varchar(9) NOT NULL DEFAULT '',
	semployeefullname varchar(128) NOT NULL DEFAULT '',
	lmadgiceventid int(11) NOT NULL DEFAULT '0',
	PRIMARY KEY (lid),
	UNIQUE KEY userevent (lmadgiceventid, semployeeid)
	) ENGINE=InnoDB;
	*/
}
