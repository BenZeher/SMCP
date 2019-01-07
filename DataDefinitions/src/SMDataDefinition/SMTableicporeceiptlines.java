package SMDataDefinition;

public class SMTableicporeceiptlines {
	public static final String TableName = "icporeceiptlines";

	public static final String lid = "lid";
	public static final String llinenumber = "llinenumber";
	public static final String lreceiptheaderid = "lreceiptheaderid";
	public static final String lpolineid = "lpolineid";
	public static final String bdqtyreceived = "bdqtyreceived";
	public static final String bdextendedcost = "bdextendedcost";
	public static final String sitemnumber = "sitemnumber";
	public static final String sitemdescription = "sitemdescription";
	public static final String slocation = "slocation";
	public static final String sglexpenseacct = "sglexpenseacct";
	public static final String sunitofmeasure = "sunitofmeasure";
	public static final String lnoninventoryitem = "lnoninventoryitem";
	public static final String bdunitcost = "bdunitcost";
	public static final String lpoinvoiceid = "lpoinvoiceid";
	
	//Field lengths:
	public static final int sitemnumberLength = 24;
	public static final int slocationLength = 6;
	public static final int sitemdescriptionLength = 75;
	public static final int sunitofmeasureLength = 10;
	public static final int sglexpenseacctLength = 75;
	
	public static final int bdqtyreceivedScale = 4;
	public static final int bdextendedcostScale = 2;
	public static final int bdunitcostScale = 6;
	
	public static final int PO_INVOICE_STATUS_NOT_INVOICED_YET = -1;
	public static final int PO_INVOICE_STATUS_NOT_TO_BE_INVOICED = 0;
}
