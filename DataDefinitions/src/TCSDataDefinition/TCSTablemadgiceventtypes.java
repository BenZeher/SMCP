package TCSDataDefinition;

public class TCSTablemadgiceventtypes {

	//Table Name
	public static String TableName = "madgiceventtypes";

	//Field List
	public static final String lid = "lid";
	public static final String sname = "sname";
	public static final String sdescription = "sdescription";
	public static final String inumberofpoints = "inumberofpoints";

	public static final int snameLength = 32;
	public static final int sdescriptionLength = 254;
	
	/*
	create table madgiceventtypes (
	lid int(11) NOT NULL AUTO_INCREMENT,
	sname varchar(32) NOT NULL DEFAULT '',
	sdescription varchar(254) NOT NULL DEFAULT '',
	inumberofpoints int(11) NOT NULL DEFAULT '0',
	PRIMARY KEY (lid),
	UNIQUE KEY snamekey (sname)
	) ENGINE=InnoDB
	;
	*/
}
