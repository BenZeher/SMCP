package SMDataDefinition;

public class SMTablebkaccountentries {
	public static final String TableName = "bkaccountentries";
	
	public static final String lid = "lid";
	public static final String lstatementid = "lstatementid";
	public static final String ientrytype = "ientrytype";
	public static final String bdamount = "bdamount";
	public static final String ssourcemodule = "ssourcemodule";
	public static final String ibatchtype = "ibatchtype";
	public static final String ibatchnumber = "ibatchnumber";
	public static final String ibatchentrynumber = "ibatchentrynumber";
	public static final String sdescription = "sdescription";
	public static final String datentrydate = "datentrydate";
	public static final String sglaccount = "sglaccount";
	public static final String sdocnumber = "sdocnumber";
	public static final String icleared = "icleared";
	
	//Lengths
	public static final int bdamountscale = 2;
	public static final int ssourcemodulelength = 2;
	public static final int sdescriptionlength = 128;
	public static final int sglaccountlength = 45;
	public static final int sdocnumberlength = 75;
	
	public static final int INITIAL_STATEMENT_ID_VALUE = 0;
	public static final int ENTRY_TYPE_DEPOSIT = 0;
	public static final int ENTRY_TYPE_WITHDRAWAL = 1;
	public static final String SOURCE_MODULE_MANUAL_ENTRY = "ME";
	public static final String SOURCE_MODULE_IMPORTED_ENTRY = "IM";
	
	public static String getEntryLabel(int iEntryType){
		switch (iEntryType){
		case ENTRY_TYPE_DEPOSIT:
			return "Deposit";
		case ENTRY_TYPE_WITHDRAWAL:
			return "Withdrawal";
		default:
			return "Deposit";
		}
	}
}
