package SMDataDefinition;

public class SMTableicaccountsets {
	
	public static String TableName = "icaccountsets";
	
	//Field names:
    public static String sAccountSetCode = "saccountsetcode";
    public static String sLastEditUserFullName = "slastedituserfullname";
    public static String lLastEditUserID = "llastedituserid";
    public static String sDescription = "sdescription";
   // public static String iCostingMethod = "icostingmethod";
    public static String sInventoryAccount = "sinventoryaccount";
    public static String sPayablesClearingAccount = "spayablesclearingaccount";
    public static String sAdjustmentWriteOffAccount = "sadjustmentwriteoffaccount";
    public static String sNonStockClearingAccount = "snonstockclearingaccount";
    public static String sTransferClearingAccount = "stransferclearingaccount";
    public static String iActive = "iactive";
    public static String datLastMaintained = "datlastmaintained";
    public static String datInactive = "datinactive";
    
    //Field lengths:
    public static int sAccountSetCodeLength = 6;
    public static int sLastEditUserLength = 128;
    public static int sDescriptionLength = 30;
    public static int sInventoryAccountLength = 45;
    public static int sPayablesClearingAccountLength = 45;
    public static int sAdjustmentWriteOffAccountLength = 45;
    public static int sNonStockClearingAccountLength = 45;
    public static int sTransferClearingAccountLength = 45;
    
}
