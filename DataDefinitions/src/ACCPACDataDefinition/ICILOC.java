package ACCPACDataDefinition;

public class ICILOC {

	/**
	 * LTO 2009-04-28
	 * NOT ALL fields from ICILOC are included in this class at this point.
	 */
	
	public static String TableName = ACCPACTables.IC_ITEMLOCATIONS;
	
	//Field names:
    public static String sItemNumber = "ITEMNO";            //CHAR         24     Unformatted Item Number
    public static String sLocation = "LOCATION";			//CHAR			6
    public static String bActive = "ACTIVE";				//SMALLINT
    public static String dQtyOnHand = "QTYONHAND";			//DECIMAL		19
    public static String dRecentCost = "RECENTCOST";		//DECIMAL		19
    public static String dTotalCost = "TOTALCOST";			//DECIMAL		19
    
	//Field Sizes:
    public static int sItemNumberLength = 24;
    public static int sLocationLength = 6;
    
}