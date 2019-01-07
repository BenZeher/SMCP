package SMDataDefinition;

public class SMTableicpolines {
	public static final String TableName = "icpolines";
	
	public static final String lid = "lid";
	public static final String lpoheaderid = "lpoheaderid";
	public static final String llinenumber = "llinenumber";
	public static final String sitemnumber = "sitemnumber";
	public static final String slocation = "slocation";
	public static final String sitemdescription = "sitemdescription";
	public static final String sunitofmeasure = "sunitofmeasure";
	public static final String bdunitcost = "bdunitcost";
	public static final String bdextendedordercost = "bdextendedordercost";
	public static final String bdextendedreceivedcost = "bdextendedreceivedcost";
	public static final String bdqtyordered = "bdqtyordered";
	public static final String bdqtyreceived = "bdqtyreceived";
	public static final String sglexpenseacct = "sglexpenseacct";
	public static final String datexpected = "datexpected";
	public static final String svendorsitemnumber = "svendorsitemnumber";
	public static final String sinstructions = "sinstructions";
	public static final String lnoninventoryitem = "lnoninventoryitem";

	//Field lengths:
	public static final int sitemnumberLength = 24;
	public static final int slocationLength = 6;
	public static final int sitemdescriptionLength = 75;
	public static final int sunitofmeasureLength = 10;
	public static final int sglexpenseacctLength = 75;
	public static final int svendorsitemnumberLength = 24;
	
	public static final int bdunitcostScale = 6;
	public static final int bdextendedordercostScale = 2;
	public static final int bdextendedreceivedcostScale = 2;
	public static final int bdqtyorderedScale = 4;
	public static final int bdqtyreceivedScale = 4;
	
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
