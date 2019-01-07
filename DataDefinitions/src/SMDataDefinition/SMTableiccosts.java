package SMDataDefinition;

public class SMTableiccosts {
	public static String TableName = "iccosts";
	
	//Field names:
	public static String iId = "id";
	public static String sItemNumber = "sitemnumber";
	public static String sLocation = "slocation";
    public static String bdQty = "bdqty";
    public static String bdCost = "bdcost";
    public static String bdQtyShipped = "bdqtyshipped";
    public static String bdCostShipped = "bdcostshipped";
    public static String iSource = "isource";
    public static String sPONumber = "sponumber";
    public static String sRemark = "sremark";
    public static String datCreationDate = "datcreationdate";
    public static String sReceiptNumber = "sreceiptnumber";
    public static String lReceiptLineID = "lreceiptlineid";
    
	//Field Lengths:
    public static int sLocationLength = 6;
    public static int sItemNumberLength = 24;
    public static int sPONumberLength = 22;
    public static int sRemarkLength = 128;
    public static int sReceiptNumberLength = 22;
    
    public static String getCostSourceLabel (int iSource){
    	
    	switch (iSource){
    	case 0: return "Receipt";
    	case 1: return "Offset";
    		default: return "";
    	}
    }
}
