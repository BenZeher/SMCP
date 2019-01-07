package SMDataDefinition;

public class SMTablessorderheaders {

	//Field names:
	public static String TableName = "ssorderheaders";
	public static String ORDNUMBER = "ORDNUMBER";//` varchar(22) NOT NULL default '',
	public static String CUSTOMER = "CUSTOMER";//` varchar(12) default NULL,
	public static String BILNAME = "BILNAME";//` varchar(60) default NULL,
	public static String BILADDR1 = "BILADDR1";//` varchar(60) default NULL,
	public static String BILADDR2 = "BILADDR2";//` varchar(60) default NULL,
	public static String BILADDR3 = "BILADDR3";//` varchar(60) default NULL,
	public static String BILADDR4 = "BILADDR4";//` varchar(60) default NULL,
	public static String BILCITY = "BILCITY";//` varchar(30) default NULL,
	public static String BILSTATE = "BILSTATE";//` varchar(30) default NULL,
	public static String BILZIP = "BILZIP";//` varchar(20) default NULL,
	public static String BILPHONE = "BILPHONE";//` varchar(30) default NULL,
	public static String BILFAX = "BILFAX";//` varchar(30) default NULL,
	public static String BILCONTACT = "BILCONTACT";//` varchar(60) default NULL,
	public static String SHIPTO = "SHIPTO";//` varchar(6) default NULL,
	public static String SHPNAME = "SHPNAME";//` varchar(60) default NULL,
	public static String SHPADDR1 = "SHPADDR1";//` varchar(60) default NULL,
	public static String SHPADDR2 = "SHPADDR2";//` varchar(60) default NULL,
	public static String SHPADDR3 = "SHPADDR3";//` varchar(60) default NULL,
	public static String SHPADDR4 = "SHPADDR4";//` varchar(60) default NULL,
	public static String SHPCITY = "SHPCITY";//` varchar(30) default NULL,
	public static String SHPSTATE = "SHPSTATE";//` varchar(30) default NULL,
	public static String SHPZIP = "SHPZIP";//` varchar(20) default NULL,
	public static String SHPPHONE = "SHPPHONE";//` varchar(30) default NULL,
	public static String SHPFAX = "SHPFAX";//` varchar(30) default NULL,
	public static String SHPCONTACT = "SHPCONTACT";//` varchar(60) default NULL,
	public static String PONUMBER = "PONUMBER";//` varchar(22) default NULL,
	public static String ORDDATE = "ORDDATE";//` double NOT NULL default '0',
	public static String SHIPVIA = "SHIPVIA";//` varchar(6) default NULL,
	public static String LOCATION = "LOCATION";//` varchar(6) default NULL,
	public static String COMMENT = "COMMENT";//` varchar(250) default NULL,
	public static String SALESPER1 = "SALESPER1";//` varchar(8) default NULL,
	public static String TAXGROUP = "TAXGROUP";//` varchar(12) default NULL,
	public static String TAUTH1 = "TAUTH1";//` varchar(12) default NULL,
	public static String TCLASS1 = "TCLASS1";//` double default NULL,
	//public static String TEXEMPT1 = "TEXEMPT1";//` varchar(20) default NULL, //TJR - REMOVE
	//public static String TEXEMPT2 = "TEXEMPT2";//` varchar(20) default NULL, //TJR - REMOVE
	//public static String From_Speed_Search = "From_Speed_Search";//` char(1) default 'Y', //TJR - REMOVE
	public static String BILALLADDRESSES = "BILALLADDRESSES";//` varchar(240) default NULL,
	public static String SHPALLADDRESSES = "SHPALLADDRESSES";//` varchar(240) default NULL,
	//public static String DELETEFLAG = "DELETEFLAG";//` char(1) default 'Y', //TJR - REMOVE
	
	//Field Lengths:
	public static int ORDNUMBERLength = 22;
	public static int CUSTOMERLength = 12;
	public static int BILNAMELength = 60;
	public static int BILADDR1Length = 60;
	public static int BILADDR2Length = 60;
	public static int BILADDR3Length = 60;
	public static int BILADDR4Length = 60;
	public static int BILCITYLength = 30;
	public static int BILSTATELength = 30;
	public static int BILZIPLength = 20;
	public static int BILPHONELength = 30;
	public static int BILFAXLength = 30;
	public static int BILCONTACTLength = 60;
	public static int SHIPTOLength =  6;
	public static int SHPNAMELength = 60;
	public static int SHPADDR1Length = 60;
	public static int SHPADDR2Length = 60;
	public static int SHPADDR3Length = 60;
	public static int SHPADDR4Length = 60;
	public static int SHPCITYLength = 30;
	public static int SHPSTATELength = 30;
	public static int SHPZIPLength = 20;
	public static int SHPPHONELength = 30;
	public static int SHPFAXLength = 30;
	public static int SHPCONTACTLength = 60;
	public static int PONUMBERLength = 22;
	public static int SHIPVIALength = 6;
	public static int LOCATIONLength = 6;
	public static int COMMENTLength = 250;
	public static int SALESPER1Length = 8;
	public static int TAXGROUPLength = 12;
	public static int TAUTH1Length = 12;
	//public static int TEXEMPT1Length = 20;
	//public static int TEXEMPT2Length = 20;
	//public static int From_Speed_SearchLength = 1;
	public static int BILALLADDRESSESLength = 240;
	public static int SHPALLADDRESSESLength = 240;
	//public static int DELETEFLAGLength = 1;
}
