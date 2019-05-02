package SMDataDefinition;

public class SMTablesmoptions {

	public static final String TableName = "smoptions";
	
	//Field names:
	public static String idummykey = "DummyKey";
	public static String lnextorderuniquifier = "NextOrderUniquifier";
	public static String lnextordernumber = "NextOrderNumber";
	public static String slastedituserfullname = "LASTEDITUSERFULLNAME";
	public static String llastedituserid = "LASTEDITUSERID";
	public static String slasteditprocess = "LASTEDITPROCESS";
	public static String slasteditdate = "LASTEDITDATE";
	public static String slastedittime = "LASTEDITTIME";
	public static String snextinvoicenumber = "NextInvoiceNumber";
	public static String sfileexportpath = "sfileexportpath";
	public static String ssmtpserver = "ssmtpserver";
	public static String ssmtpport = "ssmtpport";
	public static String ssmtpsourceservername = "ssmtpsourceservername";
	public static String stimecarddatabase = "stimecarddatabase";
	//public static String sorderdocspath = "sorderdocspath"; //Removed by TJR - 11/25/2013
	//public static String sbiddocspath = "sbiddocspath"; //Removed by TJR - 11/25/2013
	public static String sorderdocsftpurl = "sorderdocsftpurl";
	public static String sbiddocsftpurl = "sbiddocsftpurl";
	public static String sbackgroundcolor = "sbackgroundcolor";
	public static String ssmtpusername = "ssmtpusername";
	public static String ssmtppassword = "ssmtppassword";
	public static String iusesauthentication = "iusesauthentication";
	public static String sinvoicelogofilename = "sinvoicelogofilename";
	public static String iinvoicingflag = "iinvoicingflag";
	public static String datlastinvoicingflagtime = "datinvoicingflagdatetime";
	public static String ilastinvoicinguserid = "iinvoicinguserid";
	public static String sproposallogofilename = "sproposallogofilename";
	//Automatically feed SMCP GL?
	public static String ifeedgl = "ifeedgl";

	//FTP info for FTP'ing export files:
	public static String sftpexporturl = "sftpexporturl";
	public static String sftpexportuser = "sftpexportuser";
	public static String sftpexportpw = "sftpexportpw";
	public static String sftpfileexportpath = "sftpfileexportpath";
	
	//Switches for producing bank reconciliation export file:
	//TJR - Removed these 12/11/2015:
	//public static String sbankname = "sbankname";
	//public static String sbankrecglacct = "sbankrecglacct";
	public static String icreatebankrecexport = "icreatebankrecexport";
	
	public static String sgoogleapikey = "sgoogleapikey";
	public static String sgoogleapiclientid= "sgoogleapiclientid";
	public static String sgoogleapiprojectid= "sgoogleapiprojectid";
	public static String sgoogledomain= "sgoogledomain";
	public static String iusegoogledrivepickerapi = "iusegoogledrivepickerapi";
	public static String iusegoogleplacesapi = "iusegoogleplacesapi";
	
	//Fields for creating new order folders in Google Drive:
	public static String gdriveorderparentfolderid = "gdriveorderparentfolderid";
	public static String gdriveorderfolderprefix = "gdriveorderfolderprefix";
	public static String gdriveorderfoldersuffix = "gdriveorderfoldersuffix";
	
	//Fields for creating new sales lead folders in Google Drive:
	public static String gdrivesalesleadparentfolderid = "gdrivesalesleadparentfolderid";
	public static String gdrivesalesleadfolderprefix = "gdrivesalesleadfolderprefix";
	public static String gdrivesalesleadfoldersuffix = "gdrivesalesleadfoldersuffix";
	
	//Fields for creating new work order folders in Google Drive:
	public static String gdriveworkorderparentfolderid = "gdriveworkorderparentfolderid";
	public static String gdriveworkorderfolderprefix = "gdriveworkorderfolderprefix";
	public static String gdriveworkorderfoldersuffix = "gdriveworkorderfoldersuffix";
	
	//Field for URL to upload files to Google Drive:
	public static String gdriveuploadfileurl = "gdriveuploadfileurl";
	
	//Field for URL to rename folder in Google Drive
	public static String gdriverenamefolderurl = "gdriverenamefolderurl";
	
	//Boolean designating whether to copy Google Drive folder URL from sales lead to order when
	//converting sales lead into order:
	public static String icopysalesleadfolderurltoorder = "icopysalesleadfolderurltoorder";
	
	//Current posting period - defined by a starting and ending date:
	public static String datpostingperiodstartdate = "datpostingperiodstartdate";
	public static String datpostingperiodenddate = "datpostingperiodenddate";
	
	//Value to store width of the signature box on Work Orders (height is calculated in proportion to width)
	public static String isignatureboxwidth = "isignatureboxwidth";
	public static double SIGNATURE_BOX_WIDTH_TO_HEIGHT_RATIO = 3.7736;
	
	//Field to store a 'reply-to' address for emails:
	public static String ssmtpreplytoname = "ssmtpreplytoname";
	
	//Field lengths:
	public static int snextordernumberlength = 22;
	public static int slastedituserlength = 128;
	public static int slasteditprocesslength = 50;
	public static int snextinvoicenumberlength = 8;
	public static int sfileexportpathlength = 128;
	public static int ssmtpserverlength = 128;
	public static int ssmtpportlength = 8;
	public static int ssmtpsourceservernamelength = 128;
	public static int stimecarddatabaselength = 128;
	//public static int sorderdocspathlength = 128;
	//public static int sbiddocspathlength = 128;
	public static int sorderdocsftpurllength = 128;
	public static int sbiddocsftpurllength = 128;
	public static int sbackgroundcolorlength = 6;
	public static int ssmtpusernamelength = 72;
	public static int ssmtppasswordlength = 72;
	public static int sinvoicelogofilenamelength = 72;
	public static int sproposallogofilenamelength = 72;
	public static int sftpexporturllength = 128;
	public static int sftpexportuserlength = 72;
	public static int sftpexportpwlength = 72;
	public static int sftpfileexportpathlength = 128;
	//public static int sbanknamelength = 72;
	//public static int sbankrecglacctlength = 45;
	public static int gdriveorderparentfolderidlength = 72;
	public static int gdriveorderfolderprefixlength = 32;
	public static int gdriveorderfoldersuffixlength = 32;
	//public static int gdrivecreatenewfolderurllength = 128;
	public static int gdrivesalesleadparentfolderidlength = 72;
	public static int gdrivesalesleadfolderprefixlength = 32;
	public static int gdrivesalesleadfoldersuffixlength = 32;
	public static int gdriveworkorderparentfolderidlength = 72;
	public static int gdriveworkorderfolderprefixlength = 32;
	public static int gdriveworkorderfoldersuffixlength = 32;
	public static int gdriveuploadfileurllength = 128;
	public static int gdriverenamefolderurllength = 128;
	public static int isignatureboxwidthlength = 11;
	public static int igoogleapikeylength = 100;
	public static int ssmtpreplytonamelength = 72;
	
	public static final int FEED_GL_EXTERNAL_GL_ONLY = 0;
	public static final int FEED_GL_SMCP_GL_ONLY = 1;
	public static final int FEED_GL_BOTH_EXTERNAL_AND_SMCP_GL = 2;

	
}
