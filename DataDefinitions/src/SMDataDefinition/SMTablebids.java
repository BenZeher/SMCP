package SMDataDefinition;

public class SMTablebids {
	//Table Name
	public static final String TableName = "bids";
	
	//Statuses:
	//Pending (P)
	//Successful (S)
    //Unsuccessful (U)
	//Inactive (I)
	
	//Field names:
	public static final String lid  = "id";
	public static final String ssalespersoncode = "ssalespersoncode";
	public static final String dattimeoriginationdate = "dattimeoriginationdate";
	public static final String dattimebiddate = "dattimebiddate";
	public static final String scustomername = "scustomername";
	public static final String sprojectname = "sprojectname";
	public static final String dattimeplansreceived = "dattimeplansreceived";
	public static final String dattimetakeoffcomplete = "dattimetakeoffcomplete";
	public static final String dattimepricecomplete = "dattimepricecomplete";
	public static final String mdescription = "mdescription";
	public static final String dattimeactualbiddate = "dattimeactualbiddate";
	public static final String scontactname = "scontactname";
	public static final String sphonenumber = "sphonenumber";
	//TJR - 6/4/2014 - removed this field:
	//public static final String sextension = "sextension";
	public static final String emailaddress = "emailaddress";
	public static final String datlastcontactdate = "datlastcontactdate";
	public static final String datnextcontactdate = "datnextcontactdate";
	public static final String mfollowupnotes = "mfollwupnotes";
	public static final String sstatus = "sstatus";
	public static final String dapproximateamount = "dapproximateamount";
	public static final String iprojecttype = "iprojecttype";
	//LTO 20140108 Obsolete
	//public static final String sbinnumber = "sbinnumber";
	public static final String screatedbyfullname = "screatedbyfullname";
	public static final String lcreatedbyuserid = "lcreatedbyuserid";
	public static final String saltphonenumber = "saltphonenumber";
	public static final String sfaxnumber = "sfaxnumber";
	public static final String iordersourceid = "iordersourceid";
	public static final String sordersourcedesc = "sordersourcedesc";
	public static final String datcreatedtime = "datcreatedtime";
	public static final String sgdoclink = "sgdoclink";
	public static final String isalescontactid = "isalescontactid";
	//TJR - added these 5/27/2014:
	public static final String sshiptoaddress1 = "sshiptoaddress1"; //60
	public static final String sshiptoaddress2 = "sshiptoaddress2"; //60
	public static final String sshiptoaddress3 = "sshiptoaddress3"; //60
	public static final String sshiptoaddress4 = "sshiptoaddress4"; //60
	public static final String sshiptocity = "sshiptocity"; //30
	public static final String sshiptostate = "sshiptostate"; //30
	public static final String sshiptozip = "sshiptozip"; //20
	
	public static final String stakeoffpersoncode = "stakeoffpersoncode";
	public static final String spricingpersoncode = "spricingpersoncode";
	//BJZ - added 10/4/17
	public static final String screatedfromordernumber = "screatedfromordernumber"; //22
	//BJZ - added 4/17/18
	public static final String lsalesgroupid = "lsalesgroupid";
	
	public static final int ssalespersoncodeLength = 9;
	public static final int stakeoffpersoncodeLength = 8;
	public static final int spricingpersoncodeLength = 8;
	public static final int scustomernameLength = 254;
	public static final int sprojectnameLength = 254;
	public static final int scontactnameLength = 60;
	public static final int sphonenumberLength = 30;
	//public static final int sextensionLength = 6;
	public static final int emailaddressLength = 75;
	public static final int sstatusLength = 1;
	//LTO 20140108 Obsolete
	//public static final int sbinnumberLength = 30;
	public static final int sCreatedByLength = 128;
	public static final int saltphonenumberLength = 30;
	public static final int sfaxnumberLength = 30;
	public static final int ssordersourcedescLength = 255;
	
	public static final int sshiptoaddress1Length = 60;
	public static final int sshiptoaddress2Length = 60;
	public static final int sshiptoaddress3Length = 60;
	public static final int sshiptoaddress4Length = 60;
	public static final int sshiptocityLength = 30;
	public static final int sshiptostateLength = 30;
	public static final int sshiptozipLength = 20;
	
	public static final String STATUS_PENDING = "P";
	public static final String STATUS_SUCCESSFUL = "S";
	public static final String STATUS_UNSUCCESSFUL = "U";
	public static final String STATUS_INACTIVE = "I";
	
	
}
