package SMDataDefinition;

public class SMTablematerialreturns {
	public static final String TableName = "materialreturns";
	
	public static final String lid = "lid";
	public static final String datinitiated = "datinitiated";
	public static final String linitiatedbyid = "linitiatedbyid";
	public static final String sinitiatedbyfullname = "sinitiatedbyfullname";
	public static final String iresolved = "iresolved";
	public static final String datresolved = "datresolved";
	public static final String lresolvedbyid = "lresolvedbyid";
	public static final String sresolvedbyfullname = "sresolvedbyfullname";
	public static final String sdescription = "sdescription";
	public static final String mcomments = "mcomments";
	public static final String mresolutioncomments = "mresolutioncomments";
	public static final String iworkorderid = "iworkorderid";
	public static final String strimmedordernumber = "strimmedordernumber";
	public static final String icreditstatus = "icreditstatus";
	public static final String iponumber = "iponumber";		
	//Lengths
	public static final int linitiatedbyidlength = 11;
	public static final int sinitiatedbyfullnamelength = 128;
	public static final int lresolvedbyidlength = 11;
	public static final int sresolvedbyfullnamelength = 128;
	public static final int sdescriptionlength = 254;
	public static final int strimmedordernumberlength = 22;
	
	public static int STATUS_CREDITNOTEXPECTED = 1;
	public static int STATUS_CREDITANTICIPATED = 2;
	public static int STATUS_CREDITRECEIVED = 3;
}
