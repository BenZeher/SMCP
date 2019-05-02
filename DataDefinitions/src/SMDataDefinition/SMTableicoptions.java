package SMDataDefinition;

public class SMTableicoptions {

	//Table Name
	public static final String TableName = "icoptions";
	
	//Field names:
	public static String ibatchpostinginprocess = "ibatchpostinginprocess";
	//public static String suser = "suser";
	public static String suserfullname = "suserfullname";
	public static String sprocess = "sprocess";
	public static String datstartdate = "datstartdate";
	public static String lcostingmethod = "lcostingmethod";
	public static String lallownegativeqtys = "lallownegativeqtys";
	public static String lpostingtimestamp = "lpostingtimestamp";
	public static String isuppressbarcodesonnonstockitems = "isuppressbarcodesonnonstockitems";
	
	//The flag tells the system whether or not to write the batch number back to the SM invoice header,
	//or whether to write cost information back to the invoice lines.  It's set to false while testing
	// the inventory system so we can test without touching REAL SM invoice data:
	public static String iflagimports = "iflagimports"; //If true (1), then when invoices are imported, they are flagged as having been
														//imported into inventory, so they won't be imported again.
	public static String ssistercompanyname1 = "ssistercompanyname1";
	public static String ssistercompanyname2 = "ssistercompanyname2";
	public static String ssistercompanydb1 = "ssistercompanydb1";
	public static String ssistercompanydb2 = "ssistercompanydb2";
	public static String iexportto = "iexportto";
	
	//Automatically feed SMCP GL?
	public static String ifeedgl = "ifeedgl";
	
	//Fields for creating new purchase order folders in Google Drive:
	public static String gdrivepurchaseordersparentfolderid = "gdrivepurchaseordersparentfolderid";
	public static String gdrivepurchaseordersfolderprefix = "gdrivepurchaseordersfolderprefix";
	public static String gdrivepurchaseordersfoldersuffix = "gdrivepurchaseordersfoldersuffix";
	
	public static int suserfullnamelength = 128;
	public static int sprocesslength = 128;
	public static int ssistercompanyname1length = 128;
	public static int ssistercompanyname2length = 128;
	public static int ssistercompanydb1length = 128;
	public static int ssistercompanydb2length = 128;
	
	public static int COSTING_METHOD_LIFO = 0;
	public static int COSTING_METHOD_FIFO = 1;
	public static int COSTING_METHOD_AVERAGECOST = 2;
	
	public static int gdrivepurchaseordersparentfolderidlength = 72;
	public static int gdrivepurchaseordersfolderprefixlength = 32;
	public static int gdrivepurchaseordersfoldersuffixlength = 32;
	
	public static String COSTING_METHOD_LIFO_LABEL = "LIFO";
	public static String COSTING_METHOD_FIFO_LABEL = "FIFO";
	public static String COSTING_METHOD_AVERAGECOST_LABEL = "Average Cost";
	
	public static final int FEED_GL_EXTERNAL_GL_ONLY = 0;
	public static final int FEED_GL_SMCP_GL_ONLY = 1;
	public static final int FEED_GL_BOTH_EXTERNAL_AND_SMCP_GL = 2;
	
	public static String Get_Costing_Method_Label(int iCostingMethod){
		
		switch (iCostingMethod) {
		case 0:
			return COSTING_METHOD_LIFO_LABEL;
		case 1:
			return COSTING_METHOD_FIFO_LABEL;
		case 2:
			return COSTING_METHOD_AVERAGECOST_LABEL;
		default:  // optional default case
			return COSTING_METHOD_LIFO_LABEL;
		}
	}
}
