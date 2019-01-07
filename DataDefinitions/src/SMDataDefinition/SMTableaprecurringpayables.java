package SMDataDefinition;

public class SMTableaprecurringpayables {
	
	
	public static final String TableName = "aprecurringpayables";
	
	public static final String lid = "lid";
	public static final String srecurringpayablecode = "srecurringpayablecode";
	public static final String sdescription = "sdescription";
	public static final String svendoracct = "svendoracct";
	public static final String iactive = "iactive";
	public static final String sremittocode = "sremittocode";
	public static final String sacctsetname = "sacctsetname";
	public static final String stermscode = "stermscode";
	//public static final String sglacct = "sglacct";
	//public static final String sdistcodename = "sdistcodename";
	public static final String lscheduleid = "lscheduleid";
	public static final String datstartdate = "datstartdate";
	public static final String iexpiretype = "iexpiretype";
	public static final String iinvoicecount = "iinvoicecount";
	public static final String bdtotalamountinvoiced = "bdtotalamountinvoiced";

	
	//Labels and values for expiration types
	public static final int NUMBER_OF_EXPIRE_TYPES = 4;
	public static final int NONE_EXPIRE = 0;
	public static final int DATE_EXPIRE = 1;
	public static final int MAX_COUNT_EXPIRE = 2;
	public static final int MAX_AMOUNT_EXPIRE = 3;
	
	public static final String NONE_EXPIRE_LABEL = "No Expiration";
	public static final String DATE_EXPIRE_LABEL = "Specific Date";
	public static final String MAX_COUNT_EXPIRE_LABEL = "Maximuim Number of Invoices";
	public static final String MAX_AMOUNT_EXPIRE_LABEL = "Maximuim Amount";
	
	public static String getExpireTypeLabel(int iExportIndex){
		
		if (iExportIndex == NONE_EXPIRE){
			return NONE_EXPIRE_LABEL;
		}
		if (iExportIndex == DATE_EXPIRE){
			return DATE_EXPIRE_LABEL;
		}
		if (iExportIndex == MAX_COUNT_EXPIRE){
			return MAX_COUNT_EXPIRE_LABEL;
		}
		if (iExportIndex == MAX_AMOUNT_EXPIRE){
			return MAX_AMOUNT_EXPIRE_LABEL;
		}
		return NONE_EXPIRE_LABEL;
	}
	
	
	

}
