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
	public static final String icreditnotexpected = "icreditnotexpected";
	public static final String iponumber = "iponumber";
	// TJR - Added these two fields 2/22/2019:
	public static final String itobereturned = "itobereturned";
	public static final String svendoracct = "svendoracct";
	// TJR - Added these fields 9/26/2019:
	public static final String ladjustedbatchnumber = "ladjustedbatchnumber"; //AdjustedBatch#
	public static final String lentrynumber = "lentrynumber"; //Adjusted Entry Number 
	public static final String bdentryamount = "bdentryamount"; //Expected Credit Amount
	public static final String datcreditnotedate = "datcreditnotedate";
	public static final String screditmemonumber = "screditmemonumber";
	public static final String bdcreditamt = "bdcreditamt"; //Actual Credit Amount
	public static final String datreturnsent = "datreturnsent";
	
	//Lengths
	public static final int linitiatedbyidlength = 11;
	public static final int sinitiatedbyfullnamelength = 128;
	public static final int lresolvedbyidlength = 11;
	public static final int sresolvedbyfullnamelength = 128;
	public static final int sdescriptionlength = 254;
	public static final int strimmedordernumberlength = 22;
	public static final int svendoracctlength = 12;  //Same as icvendors.svendoracctLength
	public static final int ladjustedbatchnumberlength = 11;
	public static final int lentrynumberlength = 11;
	public static final int bdentryamountlength = 19;
	public static final int screditmemonumberlength = 24;
	public static final int bdcreditamtlength = 19;
	
	public static int STATUS_CREDITNOTEXPECTED = 1;

}
