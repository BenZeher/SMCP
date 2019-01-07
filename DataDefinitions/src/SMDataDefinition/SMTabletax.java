package SMDataDefinition;

public class SMTabletax {
	//Table Name
	public static final String TableName = "tax";
	
	//Field names
	public static String staxjurisdiction = "staxjurisdiction";
	//public static String itaxtype = "itaxtype"; Removed 9/20/2018 - TJR
	public static String bdtaxrate = "bdtaxrate";
	public static String sdescription = "sdescription";
	public static String staxtype = "staxtype";
	//public static String sTaxAuthority = "sTaxAuthority"; Removed 1/20/2016 - TJR
	//public static String sTaxAuthDesc = "sTaxAuthDesc"; Removed 1/20/2016 - TJR
	public static String sglacct = "sglacct";
	public static String iactive = "iactive";
	
	//Added 1/22/2016 - TJR:
	public static String icalculateonpurchaseorsale = "icalculateonpurchaseorsale";
	public static String icalculatetaxoncustomerinvoice = "icalculatetaxoncustomerinvoice";
	public static String ishowinorderentry = "ishowinorderentry";
	public static String ishowinaccountspayable = "ishowinaccountspayable";
	public static String lid = "lid";
	
	//Field lengths:
	public static int staxjurisdictionLength = 12;
	public static int sdescriptionLength = 254;
	public static int staxtypeLength = 254;
	public static int bdtaxratescale = 4;
	//public static int sTaxAuthorityLength = 12;
	//public static int sTaxAuthDescLength = 255;
	public static int sglacctLength = 45;
	
	public static final int TAX_CALCULATION_BASED_ON_PURCHASE_COST = 0;
	public static final int TAX_CALCULATION_BASED_ON_SELLING_PRICE = 1;
	
	public static String getCalculationTypeDescription(int iCalculationType){
		switch (iCalculationType){
		case TAX_CALCULATION_BASED_ON_PURCHASE_COST:
			return "Purchase amount";
		case TAX_CALCULATION_BASED_ON_SELLING_PRICE:
			return "Selling price";
		default:
			return "N/A";
		}
	}
	
}
