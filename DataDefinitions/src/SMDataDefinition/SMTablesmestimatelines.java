package SMDataDefinition;

public class SMTablesmestimatelines {

	public static String TableName = "smestimatelines";

	public static String lid = "lid";
	public static String lsummarylid = "lsummarylid";
	public static String lestimatelid = "lestimatelid";
	public static String lestimatelinenumber = "lestimatelinenumber";
	public static String bdquantity = "bdquantity";
	public static String sitemnumber = "sitemnumber";
	public static String slinedescription = "slinedescription";
	public static String sunitofmeasure = "sunitofmeasure";
	public static String bdextendedcost = "bdextendedcost";
	public static String bdunitcost = "bdunitcost";
	public static String bdextendedsellprice = "bdextendedsellprice";
	public static String bdunitsellprice = "bdunitsellprice";
	public static String iincludeonorder = "iincludeonorder";
	
	//String lengths:
	public static int sitemnumberLength = 32;
	public static int slinedescriptionLength = 75;
	public static int sunitofmeasureLength = 10;
	
	//Scales:
	public static int bdquantityScale = 4;
	public static int bdextendedcostScale = 2;
	public static int bdunitcostScale = 2;
	public static int bdextendedsellpriceScale = 2;
	public static int bdunitsellpriceScale = 2;
}
