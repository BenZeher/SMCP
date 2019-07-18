package SMDataDefinition;

public class SMTablegltransactionlines {
	//Table Name
	public static final String TableName = "gltransactionlines";

	//Field names:
	public static final String lid = "lid";
	public static final String loriginalbatchnumber = "loriginalbatchnumber";
	public static final String loriginalentrynumber = "loriginalentrynumber";
	public static final String loriginallinenumber = "loriginallinenumber";
	public static final String lsourceledgertransactionlineid = "lsourceledgertransactionlineid";
	public static final String sacctid = "sacctid";
	public static final String ifiscalyear = "ifiscalyear";
	public static final String ifiscalperiod = "ifiscalperiod";
	public static final String dattransactiondate = "dattransactiondate";
	public static final String ssourceledger = "ssourceledger";
	public static final String ssourcetype = "ssourcetype";
	public static final String stransactiontype = "stransactiontype";
	public static final String bdamount = "bdamount";
	public static final String sdescription = "sdescription";
	public static final String sreference = "sreference";
	public static final String datpostingdate = "datpostingdate";
	public static final String iconsolidatedposting = "iconsolidatedposting";
	public static final String lexternalcompanypullid = "lexternalcompanypullid";
	
	//Field Lengths:
	public static final int sacctidLength = 45;
	public static final int ssourceledgerLength = 2;
	public static final int ssourcetypeLength = 2;
	public static final int stransactiontypeLength = 32;
	public static final int sdescriptionLength = 96;
	public static final int sreferenceLength = 60;
	
	public static final int bdamountScale = 2;
	
	/* UNIQUE SOURCE LEDGERS AND TYPES CURRENTLY IN SMCP - 3/21/2019:
	mysql> select distinct CONCAT(sdetailsourceledger, '-', sdetailsourcetype) from glexportdetails;
	+-----------------------------------------------------+
	| CONCAT(sdetailsourceledger, '-', sdetailsourcetype) |
	+-----------------------------------------------------+
	| IC-SH                                               |
	| IC-AD                                               |
	| IC-RC                                               |
	| IC-TF                                               |
	| FA-SS                                               |
	| AP-IN                                               |
	| AP-PY                                               |
	| AP-RC                                               |
	| AR-PY                                               |
	| AR-PI                                               |
	| AR-RT                                               |
	| AR-AD                                               |
	| AR-UC                                               |
	| AR-CN                                               |
	| AR-IN                                               |
	+-----------------------------------------------------+
    */
	
}
