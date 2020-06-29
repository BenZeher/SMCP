package SMDataDefinition;

public class SMTablesmestimatesummaries {

	public static String TableName = "smestimatesummaries";

	public static String lid = "lid";
	public static String sjobname = "sjobname";
	public static String lsalesleadid = "lsalesleadid";
	public static String itaxid = "itaxid";
	public static String ilabortype = "ilabortype";
	public static String iordertype = "iordertype";
	public static String lcreatedbyid = "lcreatedbyid";
	public static String datetimecreated = "datetimecreated";
	public static String screatedbyfullname = "screatedbyfullname";
	public static String llastmodifiedbyid = "llastmodifiedbyid";
	public static String datetimelastmodified = "datetimelastmodified";
	public static String slastmodifiedbyfullname = "slastmodifiedbyfullname";
	public static String scomments = "scomments";
	public static String bdadjustedfreight = "bdadjustedfreight";
	public static String bdadjustedlaborunitqty = "bdadjustedlaborunitqty";
	public static String bdadjustedlaborcostperunit = "bdadjustedlaborcostperunit";
	public static String bdadjustedmarkupamt = "bdadjustedmarkupamt";  //TODO - fix this name
	public static String bdtaxrate = "bdtaxrate";
	public static String icalculatetaxonpurchaseorsale = "icalculatetaxonpurchaseorsale";
	public static String icalculatetaxoncustomerinvoice = "icalculatetaxoncustomerinvoice";
	public static String spricelistcode = "spricelistcode";
	public static String ipricelevel = "ipricelevel";
	public static String sadditionalpostsalestaxcostlabel = "sadditionalpostsalestaxcostlabel";
	public static String bdadditionalpostsalestaxcostamt = "bdadditionalpostsalestaxcostamt";
	public static String strimmedordernumber = "strimmedordernumber";
	
	//Field Lengths:
	public static final int spricelistcodeLength = 6;
	public static int sjobnameLength = 128;
	public static int sscreatedbyfullnameLength = 128;
	public static int slastmodifiedbyfullnameLength = 128;
	public static int scommentsLength = 128;
	public static int bdadjustedfreightScale = 2;
	public static int bdadjustedlaborunitqtyScale = 4;
	public static int bdadjustedlaborcostperunitScale = 2;
	public static int bdadjustedlmarkupamtScale = 2;
	public static int bdtaxrateScale = 4;
	public static int spricelistLength = 6;
	public static int sadditionalpostsalestaxcostlabelLength = 32;
	public static int bdadditionalpostsalestaxcostamtScale = 2;
	public static int strimmedordernumberLength = 22;
}
