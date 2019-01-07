package SMDataDefinition;

public class SMTablebkbanks {
	public static final String TableName = "bkbanks";
	
	public static final String lid = "lid";
	public static final String sshortname = "sshortname";
	public static final String sbankname = "sbankname";
	public static final String saccountname = "saccountname";
	public static final String saccountnumber = "saccountnumber";
	public static final String sglaccount = "sglaccount"; 
	public static final String dattimelastmaintained = "dattimelastmaintained"; 
	public static final String llastmaintainedbyid = "llastmaintainedbyid";
	public static final String slastmaintainedbyfullname = "slastmaintainedbyfullname";
	public static final String iactive = "iactive";
	public static final String bdrecentbalance = "bdrecentbalance";
	public static final String datrecentbalancedate = "datrecentbalancedate";
	public static final String sroutingnumber = "sroutingnumber";
	public static final String saddressline1 = "saddressline1";
	public static final String saddressline2 = "saddressline2";
	public static final String saddressline3 = "saddressline3";
	public static final String saddressline4 = "saddressline4";
	public static final String scity = "scity";
	public static final String sstate = "sstate";
	public static final String scountry = "scountry";
	public static final String spostalcode = "spostalcode";
	public static final String icheckformid = "icheckformid";
	public static final String lnextchecknumber = "lnextchecknumber";
	
	//Used ONLY to convert the ACCPAC AP data - when that's all done, this field can be removed:
	public static final String iaddedbyapconversion = "iaddedbyapconversion";

	//Lengths
	public static final int sshortnamelength = 32;
	public static final int sbanknamelength = 72;
	public static final int saccountnamelength = 60;
	public static final int saccountnumberlength = 60;
	public static final int sroutingnumberlength = 60;
	public static final int sglaccountlength = 45;
	public static final int llastmaintainedbyidlength = 11;
	public static final int slastmaintainedbyfullnamelength = 128;
	public static final int saddressline1length = 60;
	public static final int saddressline2length = 60;
	public static final int saddressline3length = 60;
	public static final int saddressline4length = 60;
	public static final int scitylength = 30;
	public static final int sstatelength = 30;
	public static final int scountrylength = 30;
	public static final int spostalcodelength = 20;
	
	public static final int bdrecentbalancescale = 2;
	
	public static final int STATMENT_ENTRY_TYPE_DEPOSIT = 0;
	public static final int STATMENT_ENTRY_TYPE_WITHDRAWAL = 1;

	public String getStatementEntryTypeDescription(int iEntryType){
		switch(iEntryType){
		case 0:
			return "Deposit";
		case 1:
			return "Withdrawal";
		default:
			return "N/A";
		}
	}
}
