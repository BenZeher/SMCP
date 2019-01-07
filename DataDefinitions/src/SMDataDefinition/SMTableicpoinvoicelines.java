package SMDataDefinition;

public class SMTableicpoinvoicelines {

	public static String TableName = "icpoinvoicelines";
	
	//Field names
	public static final String lid = "lid";
	public static final String lpoinvoiceheaderid = "lpoinvoiceheaderid";
	public static final String lporeceiptlineid = "lporeceiptlineid";
	public static final String sinvoicenumber = "sinvoicenumber";
	public static final String bdreceivedcost = "bdreceivedcost";
	public static final String bdinvoicedcost = "bdinvoicedcost";
	public static final String sexpenseaccount = "sexpenseaccount";
	public static final String bdqtyreceived = "bdqtyreceived";
	public static final String sitemnumber = "sitemnumber";
	public static final String sitemdescription = "sitemdescription";
	public static final String slocation = "slocation";
	public static final String sunitofmeasure = "sunitofmeasure";
	public static final String lnoninventoryitem = "lnoninventoryitem";
	public static final String lporeceiptid = "lporeceiptid";
	
	//Field lengths
	public static final int sinvoicenumberLength = 22;
	public static final int sexpenseaccountLength = 75;
	public static final int sitemnumberLength = 24;
	public static final int slocationLength = 6;
	public static final int sitemdescriptionLength = 75;
	public static final int sunitofmeasureLength = 10;
	
	//Field scales
	public static final int bdreceivedcostScale = 2;
	public static final int bdinvoicedcostScale = 2;
	public static final int bdqtyreceivedScale = 4;
	
}