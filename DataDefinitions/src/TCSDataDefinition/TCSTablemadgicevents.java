package TCSDataDefinition;

public class TCSTablemadgicevents {

	//Table Name
	public static String TableName = "madgicevents";

	//Field List
	public static final String lid = "lid";
	public static final String seventtypename = "seventtypename";
	public static final String sdescription = "sdescription";
	public static final String inumberofpoints = "inumberofpoints";
	public static final String datevent = "datevent";
	
	public static final int seventtypenameLength = 32;
	
	/*
	create table madgicevents (
	lid int(11) NOT NULL AUTO_INCREMENT,
	seventtypename varchar(32) NOT NULL DEFAULT '',
	sdescription mediumtext NOT NULL,
	inumberofpoints int(11) NOT NULL DEFAULT '0',
	datevent DATE NOT NULL DEFAULT '0000-00-00',
	PRIMARY KEY (lid),
	UNIQUE KEY snamekey (seventtypename)
	) ENGINE=InnoDB;
	;
	*/
}
