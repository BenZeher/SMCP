package SMDataDefinition;

public class SMTableicpoheaders {
	public static final String TableName = "icpoheaders";
	
	public static final String lid = "lid";
	public static final String svendor = "svendor";
	public static final String sponumber = "sponumber";
	public static final String sreference = "sreference";
	public static final String svendorname = "svendorname";
	public static final String lstatus = "lstatus";
	public static final String datpodate = "datpodate";
	public static final String sshipcode = "sshipcode";
	public static final String sshipname = "sshipname";
	public static final String sshipaddress1 = "sshipaddress1";
	public static final String sshipaddress2 = "sshipaddress2";
	public static final String sshipaddress3 = "sshipaddress3";
	public static final String sshipaddress4 = "sshipaddress4";
	public static final String sshipcity = "sshipcity";
	public static final String sshipstate = "sshipstate";
	public static final String sshippostalcode = "sshippostalcode";
	public static final String sshipcountry = "sshipcountry";
	public static final String sshipphone = "sshipphone";
	public static final String sshipfax = "sshipfax";
	public static final String sshipcontactname = "sshipcontactname";
	public static final String sshipviacode = "sshipviacode";
	public static final String sshipvianame = "sshipvianame";
	public static final String datexpecteddate = "datexpecteddate";
	public static final String sbillcode = "sbillcode";
	public static final String sbillname = "sbillname";
	public static final String sbilladdress1 = "sbilladdress1";
	public static final String sbilladdress2 = "sbilladdress2";
	public static final String sbilladdress3 = "sbilladdress3";
	public static final String sbilladdress4 = "sbilladdress4";
	public static final String sbillcity = "sbillcity";
	public static final String sbillstate = "sbillstate";
	public static final String sbillpostalcode = "sbillpostalcode";
	public static final String sbillcountry = "sbillcountry";
	public static final String sbillphone = "sbillphone";
	public static final String sbillfax = "sbillfax";
	public static final String sbillcontactname = "sbillcontactname";
	public static final String datassigned = "datassigned";
	public static final String sassignedtofullname = "sassignedtofullname";
	public static final String lassignedtouserid = "lassignedtouserid";
	public static final String scomment = "scomment";
	public static final String sdescription = "sdescription";
	public static final String sdeletedbyfullname = "sdeletedbyfullname";
	public static final String ldeletedbyuserid = "ldeletedbyuserid";
	public static final String datdeleted = "datdeleted";
	public static final String sgdoclink = "sgdoclink";
	public static final String screatedbyfullname = "screatedbyfullname";
	public static final String lcreatedbyuserid = "lcreatedbyuserid";
	//public static final String lphase = "lphase"; - removed 2/21/2019 - TJR
	public static final String ipaymentonhold = "ipaymentonhold";
	public static final String spaymentonholdbyfullname = "spaymentonholdbyfullname";
	public static final String lpaymentonholdbyuserid = "lpaymentonholdbyuserid";
	public static final String datpaymentplacedonhold = "datpaymentplacedonhold";
	public static final String mpaymentonholdreason = "mpaymentonholdreason";
	public static final String mpaymentonholdvendorcomment = "mpaymentonholdvendorcomment";
	
	//Field lengths:
	public static final int svendorLength = 24;
	public static final int sponumberLength = 22;
	public static final int sreferenceLength = 60;
	public static final int svendornameLength = 60;
	public static final int sshipcodeLength = 6;
	public static final int sshipnameLength = 60;
	public static final int sshipaddress1Length = 60;
	public static final int sshipaddress2Length = 60;
	public static final int sshipaddress3Length = 60;
	public static final int sshipaddress4Length = 60;
	public static final int sshipcityLength = 30;
	public static final int sshipstateLength = 30;
	public static final int sshippostalcodeLength = 20;
	public static final int sshipcountryLength = 30;
	public static final int sshipphoneLength = 30;
	public static final int sshipfaxLength = 30;
	public static final int sshipcontactnameLength = 60;
	public static final int sbillcodeLength = 6;
	public static final int sbillnameLength = 60;
	public static final int sbilladdress1Length = 60;
	public static final int sbilladdress2Length = 60;
	public static final int sbilladdress3Length = 60;
	public static final int sbilladdress4Length = 60;
	public static final int sbillcityLength = 30;
	public static final int sbillstateLength = 30;
	public static final int sbillpostalcodeLength = 20;
	public static final int sbillcountryLength = 30;
	public static final int sbillphoneLength = 30;
	public static final int sbillfaxLength = 30;
	public static final int sbillcontactnameLength = 60;
	public static final int sshipviacodeLength = 6;
	public static final int sshipvianameLength = 60;
	public static final int sassignedtonameLength = 128;
	public static final int scommentLength = 254;
	public static final int sdescriptionLength = 60;
	public static final int sdeletedbyLength = 128;
	public static final int screatedbylength = 128;
	public static final int spaymentonholdbyfullnamelength = 128;
	
	public static final int STATUS_ENTERED = 0;
	public static final int STATUS_PARTIALLY_RECEIVED = 1;
	public static final int STATUS_COMPLETE = 2;
	public static final int STATUS_DELETED = 3;
	
	public static String getStatusDescription(int iStatus){
		switch (iStatus){
			case STATUS_ENTERED:
				return "Entered";
			case STATUS_PARTIALLY_RECEIVED:
				return "Partially received";
			case STATUS_COMPLETE:
				return "Completed";
			case STATUS_DELETED:
				return "Deleted";
			default:
				return "N/A";
		}
	}
}
