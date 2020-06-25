package SMDataDefinition;

public class SMTableworkorders {
	//Table Name
	public static final String TableName = "workorders";
	public static final String ObjectName = "Work Order";
	
	//Field names:
	public static final String lid  = "lid";
	public static final String smechanicinitials = "smechanicinitials";
	public static final String smechanicname = "smechanicname";
	public static final String dattimeposted = "dattimeposted";
	public static final String dattimedone = "dattimedone";
	public static final String strimmedordernumber = "strimmedordernumber";
	public static final String ssignedbyname = "ssignedbyname";
	public static final String dattimesigned = "dattimesigned";
	public static final String msignature = "msignature";
	public static final String mcomments = "mcomments";
	public static final String madditionalworkcomments = "madditionalworkcomments";
	public static final String mdetailsheettext = "mdetailsheettext";
	public static final String iadditionalworkauthorized = "iadditionalworkauthorized";
	public static final String iimported = "iimported";
	public static final String iposted = "iposted";
	public static final String ltimestamp = "ltimestamp";
	public static final String minstructions = "minstructions";
	public static final String imechid = "imechid";
	public static final String mmanagersnotes = "mmanagersnotes";
	public static final String dattimelastschedulechange = "dattimelastschedulechange";


	//TJR - 3/3/2015 - Added these to prepare to merge job cost into work orders:
    public static final String datscheduleddate = "datscheduleddate";
    public static final String bdqtyofhours = "bdqtyofhours";
    public static final String mworkdescription = "mworkdescription";
    public static final String bdtravelhours = "bdtravelhours";
    public static final String bdbackchargehours = "bdbackchargehours";
    public static final String ijoborder = "ijoborder";
    public static final String sassistant = "sassistant";
    public static final String sschedulecomment = "sschedulecomment";
    public static final String sstartingtime = "sstartingtime";
    public static final String dattimeleftprevious = "dattimeleftprevious";
    public static final String dattimearrivedatcurrent = "dattimearrivedatcurrent";
    public static final String dattimeleftcurrent = "dattimeleftcurrent";
    public static final String dattimearrivedatnext = "dattimearrivedatnext";
    public static final String sgdoclink = "sgdoclink";
    public static final String lsignatureboxwidth = "lsignatureboxwidth";
    
    //Fields to store DBA information
	public static final String mdbaaddress= "mdbaaddress";
	public static final String mdbaremittoaddress= "mdbaremittoaddress";
	public static final String sdbaworkorderlogo = "sdbaworkorderlogo";
	
	//User Fields
	public static final String sschedulechangedbyfullname = "sschedulechangedbyfullname";
	public static final String lschedulechangedbyuserid = "lschedulechangedbyuserid";
    public static final String slasteditedbyfullname = "slasteditedbyfullname";
    public static final String llasteditedbyuserid = "llasteditedbyuserid";
    public static final String sscheduledbyfullname = "sscheduledbyfullname";
    public static final String lscheduledbyuserid = "lscheduledbyuserid";
    
    //Discount Fields	
    public static final String dPrePostingWODiscountPercentage = "dPrePostingWODiscountPercentage";
    public static final String dPrePostingWODiscountAmount = "dPrePostingWODiscountAmount";
    public static final String sPrePostingWODiscountDesc = "sPrePostingWODiscountDesc";
	public static final String iViewPrices = "iViewPrices";
    
	
	public static final int smechanicinitialsLength = 4;
	public static final int smechanicnameLength = 50;
	public static final int strimmedordernumberLength = 22;
	public static final int ssignedbynameLength = 80;
	
	//TJR - 3/3/2015 - Added these to prepare to merge job cost into work orders:
	public static final int sschedulechangedbyLength = 128;
	public static final int sassistantLength = 75;
	public static final int sschedulecommentLength = 255;
	public static final int bdqtyofhoursScale = 2;
	public static final int bdtravelhoursScale = 2;
	public static final int bdbackchargehoursScale = 2;
	public static final int sstartingtimeLength = 10;
	public static final int sgdoclinkLength = 254;
	public static final int lsignatureboxwidthlength = 11;
	public static final int sdbaworkorderlogolength = 128;
	public static final int sPrePostingWODiscountDescLength = 255;
	
	//Constants for using signatures in work orders:
	public static final String SIGNATURE_DISPLAY_WIDTH = "200";  //Originally "200"; 
	public static final String SIGNATURE_DISPLAY_HEIGHT = "53"; //Originally "53";
	public static final String SIGNATURE_PEN_WIDTH = "2";
	public static final int SIGNATURE_PEN_R_COLOUR = 20;
	public static final int SIGNATURE_PEN_G_COLOUR = 83;
	public static final int SIGNATURE_PEN_B_COLOUR = 148;
	public static final String SIGNATURE_TOP = "52";
	public static final int SIGNATURE_LINE_R_COLOUR = 12;
	public static final int SIGNATURE_LINE_G_COLOUR = 12;
	public static final int SIGNATURE_LINE_B_COLOUR = 12;
	public static final String SIGNATURE_LINE_WIDTH = "1";
	public static final String SIGNATURE_LINE_MARGIN = "5";
	public static final String SIGNATURE_LINE_TOP = "36";
	
	//This string gets placed in the 'strimmedordernumber' field if it's a schedule entry that's NOT
	//associated with any particular job:
	public static final String DUMMY_JOB_NUMBER = "100";

	
/*	These fields will be locked after posting:
		Work order ID
		Mechanic ID
		Mechanic's initials
		Mechanic's name
		Date and time of posting
		Date done
		Order number
		Signed by name
		Date time signed
		Signature
		Mechanic's comments
		Additional work comments
		Additional work authorized (Y/N)
		Scheduled date
		Job sequence (job order for the day: 1, 2, 3, etc.)

		These can be changed anytime, before or after posting:
		Detail sheet text
		Posted (Y/N) (because we can 'unpost')
		Timestamp - (this is an internal marker to keep track of changes and editing conflicts)
		Instructions
		Imported (Y/N)
		Managers notes (not currently visible on the screen)
		Regular hours
		Backcharge hours
		Travel hours
		Work Description
		Last edited by
		Scheduled by (not really defined and used currently)
		Helper (assistant)
		Starting time
		Schedule comment
		Time left previous
		Time arrived at current
		Time left current
		Time arrived at next*/
}
