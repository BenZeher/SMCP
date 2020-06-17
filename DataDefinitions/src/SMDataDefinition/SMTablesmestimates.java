package SMDataDefinition;

public class SMTablesmestimates {

	public static String TableName = "smestimates";

	public static String lid = "lid";
	public static String lsummarylid = "lsummarylid";
	public static String lsummarylinenumber = "lsummarylinenumber";
	public static String sdescription = "sdescription";
	public static String sprefixlabelitem = "sprefixlabelitem";
	public static String svendorquoteid = "svendorquoteid";
	public static String svendorquotenumber = "svendorquotenumber";
	public static String ivendorquotelinenumber = "ivendorquotelinenumber";
	public static String bdquantity = "bdquantity";
	public static String sitemnumber = "sitemnumber";
	public static String sproductdescription = "sproductdescription";
	public static String sunitofmeasure = "sunitofmeasure";
	public static String bdextendedcost = "bdextendedcost";
	public static String bdfreight = "bdfreight";
	public static String bdlaborquantity = "bdlaborquantity";
	public static String bdlaborcostperunit = "bdlaborcostperunit";
	public static String sadditionalpretaxcostlabel = "sadditionalpretaxcostlabel";
	public static String bdadditionalpretaxcostamount = "bdadditionalpretaxcostamount";
	public static String bdmarkupamount = "bdmarkupamount";
	public static String sadditionalposttaxcostlabel = "sadditionalposttaxcostlabel";
	public static String bdadditionalposttaxcostamount = "bdadditionalposttaxcostamount";
	public static String bdlaborsellpriceperunit = "bdlaborsellpriceperunit";
	public static String lcreatedbyid = "lcreatedbyid";
	public static String datetimecreated = "datetimecreated";
	public static String screatedbyfullname = "screatedbyfullname";
	public static String llastmodifiedbyid = "llastmodifiedbyid";
	public static String datetimelastmodified = "datetimelastmodified";
	public static String slastmodifiedbyfullname = "slastmodifiedbyfullname";
	public static String sadditionalpostsalestaxcostlabel = "sadditionalpostsalestaxcostlabel";
	public static String bdadditionalpostsalestaxcostamt = "bdadditionalpostsalestaxcostamt";
	
	//String lengths:
	public static int sdescriptionLength = 128;
	public static int sprefixlabelitemLength = 32;
	public static int svendorquotenumberLength = 32;
	public static int svendorquoteidLength = 64;
	public static int sitemnumberLength = 32;
	public static int sproductdescriptionLength = 128;
	public static int sunitofmeasureLength = 10;
	public static int sadditionalpretaxcostlabelLength = 32;
	public static int sadditionalposttaxcostlabelLength = 32;
	public static int screatedbyfullnameLength = 128;
	public static int slastmodifiedbyfullnameLength = 128;
	public static int sadditionalpostsalestaxcostlabelLength = 32;
	
	//Scales:
	public static int bdquantityScale = 4;
	public static int bdextendedcostScale = 2;
	public static int bdfreightScale = 2;
	public static int bdlaborquantityScale = 4;
	public static int bdlaborcostperunitScale = 2;
	public static int bdadditionalpretaxcostamountScale = 2;
	public static int bdmarkupamountScale = 2;
	public static int bdadditionalposttaxcostamountScale = 2;
	public static int bdlaborsellpriceperunitScale = 2;
	
	public static int bdadditionalpostsalestaxcostamtScale = 2;
	
}
