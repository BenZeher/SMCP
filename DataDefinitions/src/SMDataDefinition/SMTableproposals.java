package SMDataDefinition;

public class SMTableproposals {
	//Table Name
	public static final String TableName = "proposals";
	
	//Object name
	public static final String ObjectName = "Proposal";
	
	//Field names:
	public static final String strimmedordernumber = "strimmedordernumber";
	public static final String sdatproposaldate = "datproposaldate";
	public static final String sfurnishandinstallstring = "sfurnishandinstallstring";
	public static final String lapprovedbyuserid = "lapprovedbyuserid";
	public static final String sapprovedbyfullname = "sapprovedbyfullname";
	public static final String dattimeapproved = "dattimeapproved";
	public static final String sbodydescription = "sbodydescription";
	public static final String salternate1 = "salternate1";
	public static final String salternate2 = "salternate2";
	public static final String salternate3 = "salternate3";
	public static final String salternate4 = "salternate4";
	public static final String salternate1price = "salternate1price";
	public static final String salternate2price = "salternate2price";
	public static final String salternate3price = "salternate3price";
	public static final String salternate4price = "salternate4price";
	public static final String swrittenproposalamt = "swrittenproposalamt";
	public static final String snumericproposalamt = "snumericproposalamt";
	public static final String sterms = "sterms";
	public static final String iprintlogo = "iprintlogo";
	public static final String itermsid = "itermsid";
	public static final String spaymentterms = "spaymentterms";
	public static final String sdaystoaccept = "sdaystoaccept";
	public static final String isigned = "isigned";
	public static final String lsignedbyuserid = "lsignedbyuserid";
	public static final String ssignedbyfullname = "ssignedbyfullname";
	public static final String dattimesigned = "dattimesigned";
	public static final String sdbaproposallogo = "sdbaproposallogo";
	
	//Field Lengths:
	public static final int strimmedordernumberLength = SMTableorderheaders.strimmedordernumberLength;
	public static final int sfurnishandinstallstringLength = 96;
	public static final int sapprovedbyuserLength = SMTableusers.sUserUserNameLength;
	public static final int sapprovedbyfullnameLength = SMTableusers.sUserUserFirstNameLength + 1 + SMTableusers.sUserUserLastNameLength;
	public static final int salternate1Length = 254;
	public static final int salternate2Length = 254;
	public static final int salternate3Length = 254;
	public static final int salternate4Length = 254;
	public static final int sdaproposallogolength = 128;
	public static final int salternate1priceLength = 64;
	public static final int salternate2priceLength = 64;
	public static final int salternate3priceLength = 64;
	public static final int salternate4priceLength = 64;
	public static final int swrittenproposalamtLength = 128;
	public static final int snumericproposalamtLength = 64;
	public static final int spaymenttermsLength = SMTableproposalterms.sdefaultpaymenttermsLength;
	public static final int sdaystoacceptLength = SMTableproposalterms.sdaystoacceptLength;
	public static final int ssignedbyuserLength = SMTableusers.sUserUserNameLength;
	public static final int ssignedbyfullnameLength = SMTableusers.sUserUserFirstNameLength + 1 + SMTableusers.sUserUserLastNameLength;
	
	//constants for using signatures in proposals:
	//Constants for using signatures in work orders:
	public static final String SIGNATURE_CANVAS_WIDTH = "200"; 
	public static final String SIGNATURE_CANVAS_HEIGHT = "53";
	public static final String SIGNATURE_PEN_WIDTH = "2";
	public static final String SIGNATURE_PEN_COLOUR = "#145394";
	public static final int SIGNATURE_PEN_R_COLOUR = 20;
	public static final int SIGNATURE_PEN_G_COLOUR = 83;
	public static final int SIGNATURE_PEN_B_COLOUR = 148;
	public static final String SIGNATURE_TOP = "52";
	public static final int SIGNATURE_LINE_R_COLOUR = 12;
	public static final int SIGNATURE_LINE_G_COLOUR = 12;
	public static final int SIGNATURE_LINE_B_COLOUR = 12;
	public static final String SIGNATURE_LINE_WIDTH = "1";
	public static final String SIGNATURE_LINE_MARGIN = "5";
	public static final String SIGNATURE_LINE_TOP = "36";
}
