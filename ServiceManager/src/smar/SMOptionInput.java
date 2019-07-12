package smar;

import java.util.ArrayList;

import javax.servlet.http.HttpServletRequest;

import ServletUtilities.clsServletUtilities;
import ServletUtilities.clsManageRequestParameters;
import smcontrolpanel.SMBidEntry;

public class SMOptionInput extends java.lang.Object{

	public static final String ParamObjectName = "SMOptionInput";
	public static final String ParamDummyKey = "DummyKey";
	public static final String ParamNextOrderUniquifier = "NextOrderUniquifier";
	public static final String ParamNextOrderNumber = "NextOrderNumber";
	public static final String ParamLastEditUserFullName = "LastEditUserFullName";
	public static final String ParamLastEditUserID = "LastEditUserID";
	public static final String ParamLastEditProcess = "LastEditProcess";
	public static final String ParamLastEditDate = "LastEditDate";
	public static final String ParamLastEditTime = "LastEditTime";
	public static final String ParamNextInvoiceNumber = "NextInvoiceNumber";
	public static final String ParamFileExportPath = "FileExportPath";
	public static final String ParamSMTPServer = "SMTPServer";
	public static final String ParamSMTPPort = "SMTPPort";
	public static final String ParamSMTPSourceServerName = "SMTPSourceServerName";
	public static final String ParamTimeCardDatabase = "TimeCardDatabase";
	public static final String ParamOrderDocsFTPUrl = "OrderDocsFTPUrl";
	public static final String ParamBidDocsFTPUrl = "BidDocsFTPUrl";
	public static final String Paramsbackgroundcolor = "sbackgroundcolor";
	public static final String Paramssmtpusername = "ssmtpusername";
	public static final String Paramssmtppassword = "ssmtppassword";
	public static final String Paramssmtpreplytoname = "ssmtpreplytoname";
	public static final String Paramismtpauthentication = "ismtpauthentication";
	public static final String Paramsinvoicelogofilename = "sinvoicelogofilename";
	public static final String Paramsproposallogofilename = "sproposallogofilename";
	public static final String Paramsftpexporturl = "sftpexporturl";
	public static final String Paramsftpexportuser = "sftpexportuser";
	public static final String Paramsftpexportpw = "sftpexportpw";
	public static final String Paramsftpfileexportpath = "sftpfileexportpath";
	public static final String Paramsbankname = "sbankname";
	public static final String Paramsbankrecglacct = "sbankrecglacct";
	public static final String Paramicreatebankrecexport = "icreatebankrecexport";
	public static final String Paramsgoogleapikey = "Paramsgoogleapikey";
	public static final String Paramsgoogleapiclientid = "Paramsgoogleapiclientid";
	public static final String Paramsgoogleapiprojectid = "Paramsgoogleapiprojectid";
	public static final String Paramsgoogledomain = "Paramsgoogledomain";
	public static final String Paramiusegoogledrivepickerapi = "Paramiusegoogledrivepickerapi";
	public static final String Paramiusegoogleplacesapi = "Paramiusegoogleplacesapi";
	public static final String Paramifeedgl = "Paramifeedgl";
	
	//Field for creating new folder google web app URL:
	public static final String Paramsgdrivecreatenewfolderurl = "gdrivecreatenewfolderurl";
	//Fields for creating new Order folders in Google Drive:
	public static final String Paramsgdriveorderparentfolderid = "gdriveorderparentfolderid";
	public static final String Paramsgdriveorderfolderprefix = "gdriveorderfolderprefix";
	public static final String Paramsgdriveorderfoldersuffix = "gdriveorderfoldersuffix";
	
	//Fields for creating new sales lead folders in Google Drive:
	public static final String Paramsgdrivesalesleadparentfolderid = "gdrivesalesleadparentfolderid";
	public static final String Paramsgdrivesalesleadfolderprefix = "gdrivesalesleadfolderprefix";
	public static final String Paramsgdrivesalesleadfoldersuffix = "gdrivesalesleadfoldersuffix";
	
	//Fields for creating new Work Order folders in Google Drive:
	public static final String Paramsgdriveworkorderparentfolderid = "gdriveworkorderparentfolderid";
	public static final String Paramsgdriveworkorderfolderprefix = "gdriveworkorderfolderprefix";
	public static final String Paramsgdriveworkorderfoldersuffix = "gdriveworkorderfoldersuffix";
	
	public static final String Paramsgdriveuploadfileurl = "gdriveuploadfileurl";
	public static final String Paramsgdriverenamefolderurl = "gdriverenamefolderurl";
	public static final String Paramicopysalesleadfolderurltoorder = "icopysalesleadfolderurltoorder";
	
	public static final String Paramdatcurrentperiodstartdate = "datcurrentperiodstartdate";
	public static final String Paramdatcurrentperiodenddate = "datcurrentperiodenddate";
	
	public static final String Paramisigantureboxwidth = "Paramisigantureboxwidth";
	
	private String m_sdummykey;
	private String m_snextorderuniquifier;
	private String m_snextordernumber;
	private String m_slastedituserfullname;
	private String m_llastedituserid;
	private String m_slasteditprocess;
	private String m_slasteditdate;
	private String m_slastedittime;
	private String m_snextinvoicenumber;
	private String m_sfileexportpath;
	private String m_ssmtpserver;
	private String m_ssmtpport;
	private String m_ssmtpsourceservername;
	private String m_stimecarddatabase;
	private String m_sorderdocsftpurl;
	private String m_sbiddocsftpurl;
	private String m_sbackgroundcolor;
	private String m_ssmtpusername;
	private String m_ssmtppassword;
	private String m_ssmtpreplytoname;
	private String m_sauthentication;
	private String m_sftpexporturl;
	private String m_sftpexportpw;
	private String m_sftpexportuser;
	private String m_sftpfileexportpath;
	//private String m_sbankname;
	//private String m_sbankrecglacct;
	private String m_screatebankrecexport;
	//private String m_sgdrivecreatenewfolderurl;
	private String m_sgdriveorderparentfolderid;
	private String m_sgdriveorderfolderprefix;
	private String m_sgdriveorderfoldersuffix;
	private String m_sgdrivesalesleadparentfolderid;
	private String m_sgdrivesalesleadfolderprefix;
	private String m_sgdrivesalesleadfoldersuffix;
	private String m_sgdriveworkorderparentfolderid;
	private String m_sgdriveworkorderfolderprefix;
	private String m_sgdriveworkorderfoldersuffix;
	private String m_sgdriveuploadfileurl;
	private String m_sgdriverenamefolderurl;
	private String m_icopysalesleadfolderurltoorder;
	private String m_datcurrentperiodstartdate;
	private String m_datcurrentperiodenddate;
	private String m_isignatureboxwidth;
	private String m_sgoogleapikey;
	private String m_sgoogleapiclientid;
	private String m_sgoogleapiprojectid;
	private String m_sgoogledomain;
	private String m_iusegoogledrivepickerapi;
	private String m_iusegoogleplacesapi;
	private String m_ifeedgl;
	
	private ArrayList<String> m_sErrorMessageArray = new ArrayList<String> (0);

	public SMOptionInput(){
		m_sdummykey = "";
		m_snextorderuniquifier = "";
		m_snextordernumber = "";
		m_slastedituserfullname = "";
		m_llastedituserid = "";
		m_slasteditprocess = "";
		m_slasteditdate = "";
		m_slastedittime = "";
		m_snextinvoicenumber = "";
		m_sfileexportpath = "";
		m_ssmtpserver = "";
		m_ssmtpport = "";
		m_ssmtpsourceservername = "";
		m_stimecarddatabase = "";
		m_sorderdocsftpurl = "";
		m_sbiddocsftpurl = "";
		m_sbackgroundcolor = "";
		m_ssmtpusername = "";
		m_ssmtppassword = "";
		m_ssmtpreplytoname = "";
		m_sauthentication = "0";
		m_sftpexporturl = "";
		m_sftpexportuser = "";
		m_sftpexportpw = "";
		m_sftpfileexportpath = "";
		//m_sbankname = "";
		//m_sbankrecglacct = "";
		m_screatebankrecexport = "0";
		//m_sgdrivecreatenewfolderurl = "";
		m_sgdriveorderparentfolderid = "";
		m_sgdriveorderfolderprefix = "";
		m_sgdriveorderfoldersuffix = "";
		m_sgdrivesalesleadparentfolderid = "";
		m_sgdrivesalesleadfolderprefix = "";
		m_sgdrivesalesleadfoldersuffix = "";
		m_sgdriveworkorderparentfolderid = "";
		m_sgdriveworkorderfolderprefix = "";
		m_sgdriveworkorderfoldersuffix = "";
		m_sgdriveuploadfileurl = "";
		m_sgdriverenamefolderurl = "";
		m_icopysalesleadfolderurltoorder = "0";
		m_datcurrentperiodenddate = "";
		m_datcurrentperiodenddate = "";
		m_isignatureboxwidth = "400";
		m_sgoogleapikey = "";
		m_sgoogleapiclientid = "";
		m_sgoogleapiprojectid = "";
		m_sgoogledomain = "";
		m_iusegoogledrivepickerapi = "0";
		m_iusegoogleplacesapi = "0";
		m_ifeedgl = "0";
		
		m_sErrorMessageArray = new ArrayList<String> (0);
	}
	
	public SMOptionInput (HttpServletRequest req){

		m_sdummykey = clsManageRequestParameters.get_Request_Parameter(SMOptionInput.ParamDummyKey, req).trim();
		m_snextorderuniquifier = clsManageRequestParameters.get_Request_Parameter(SMOptionInput.ParamNextOrderUniquifier, req).trim();
		m_snextordernumber = clsManageRequestParameters.get_Request_Parameter(SMOptionInput.ParamNextOrderNumber, req).trim();
		m_slastedituserfullname = clsManageRequestParameters.get_Request_Parameter(SMOptionInput.ParamLastEditUserFullName, req).trim();
		m_llastedituserid = clsManageRequestParameters.get_Request_Parameter(SMOptionInput.ParamLastEditUserID, req).trim();
		m_slasteditprocess = clsManageRequestParameters.get_Request_Parameter(SMOptionInput.ParamLastEditProcess, req).trim();
		m_slasteditdate = clsManageRequestParameters.get_Request_Parameter(SMOptionInput.ParamLastEditDate, req).trim();
		m_slastedittime = clsManageRequestParameters.get_Request_Parameter(SMOptionInput.ParamLastEditTime, req).trim();
		m_snextinvoicenumber = clsManageRequestParameters.get_Request_Parameter(SMOptionInput.ParamNextInvoiceNumber, req).trim();
		m_sfileexportpath = clsManageRequestParameters.get_Request_Parameter(SMOptionInput.ParamFileExportPath, req).trim();
		m_ssmtpserver = clsManageRequestParameters.get_Request_Parameter(SMOptionInput.ParamSMTPServer, req).trim();
		m_ssmtpport = clsManageRequestParameters.get_Request_Parameter(SMOptionInput.ParamSMTPPort, req).trim();
		m_ssmtpsourceservername = clsManageRequestParameters.get_Request_Parameter(SMOptionInput.ParamSMTPSourceServerName, req).trim();
		m_stimecarddatabase = clsManageRequestParameters.get_Request_Parameter(SMOptionInput.ParamTimeCardDatabase, req).trim();
		m_sorderdocsftpurl = clsManageRequestParameters.get_Request_Parameter(SMOptionInput.ParamOrderDocsFTPUrl, req).trim();
		m_sbiddocsftpurl = clsManageRequestParameters.get_Request_Parameter(SMOptionInput.ParamBidDocsFTPUrl, req).trim();
		m_sErrorMessageArray = new ArrayList<String> (0);
		m_sbackgroundcolor = clsManageRequestParameters.get_Request_Parameter(SMOptionInput.Paramsbackgroundcolor, req).trim();
		m_ssmtpusername = clsManageRequestParameters.get_Request_Parameter(SMOptionInput.Paramssmtpusername, req).trim();
		m_ssmtppassword = clsManageRequestParameters.get_Request_Parameter(SMOptionInput.Paramssmtppassword, req).trim();
		m_ssmtpreplytoname = clsManageRequestParameters.get_Request_Parameter(SMOptionInput.Paramssmtpreplytoname, req).trim();
		if (req.getParameter(SMOptionInput.Paramismtpauthentication) == null){
			m_sauthentication = "0";
		}else{
			m_sauthentication = "1";
		}
		m_sftpexporturl = clsManageRequestParameters.get_Request_Parameter(SMOptionInput.Paramsftpexporturl, req).trim();
		m_sftpexportuser = clsManageRequestParameters.get_Request_Parameter(SMOptionInput.Paramsftpexportuser, req).trim();
		m_sftpexportpw = clsManageRequestParameters.get_Request_Parameter(SMOptionInput.Paramsftpexportpw, req).trim();
		m_sftpfileexportpath = clsManageRequestParameters.get_Request_Parameter(SMOptionInput.Paramsftpfileexportpath, req).trim();
		//m_sbankname = SMUtilities.get_Request_Parameter(SMOptionInput.Paramsbankname, req).trim();
		//m_sbankrecglacct = SMUtilities.get_Request_Parameter(SMOptionInput.Paramsbankrecglacct, req).trim();
		if (req.getParameter(SMOptionInput.Paramicreatebankrecexport) == null){
			m_screatebankrecexport = "0";
		}else{
			m_screatebankrecexport = "1";
		}
		//m_sbankrecglacct = SMUtilities.get_Request_Parameter(SMOptionInput.Paramsbankrecglacct, req).trim();
		m_sgdriveorderparentfolderid = clsManageRequestParameters.get_Request_Parameter(SMOptionInput.Paramsgdriveorderparentfolderid, req).trim();
		m_sgdriveorderfolderprefix = clsManageRequestParameters.get_Request_Parameter(SMOptionInput.Paramsgdriveorderfolderprefix, req).trim();
		m_sgdriveorderfoldersuffix = clsManageRequestParameters.get_Request_Parameter(SMOptionInput.Paramsgdriveorderfoldersuffix, req).trim();
		m_sgdrivesalesleadparentfolderid = clsManageRequestParameters.get_Request_Parameter(SMOptionInput.Paramsgdrivesalesleadparentfolderid, req).trim();
		m_sgdrivesalesleadfolderprefix = clsManageRequestParameters.get_Request_Parameter(SMOptionInput.Paramsgdrivesalesleadfolderprefix, req).trim();
		m_sgdrivesalesleadfoldersuffix = clsManageRequestParameters.get_Request_Parameter(SMOptionInput.Paramsgdrivesalesleadfoldersuffix, req).trim();
		m_sgdriveworkorderparentfolderid = clsManageRequestParameters.get_Request_Parameter(SMOptionInput.Paramsgdriveworkorderparentfolderid, req).trim();
		m_sgdriveworkorderfolderprefix = clsManageRequestParameters.get_Request_Parameter(SMOptionInput.Paramsgdriveworkorderfolderprefix, req).trim();
		m_sgdriveworkorderfoldersuffix = clsManageRequestParameters.get_Request_Parameter(SMOptionInput.Paramsgdriveworkorderfoldersuffix, req).trim();
		//m_sgdrivecreatenewfolderurl = SMUtilities.get_Request_Parameter(SMOptionInput.Paramsgdrivecreatenewfolderurl, req).trim();
		m_sgdriveuploadfileurl = clsManageRequestParameters.get_Request_Parameter(SMOptionInput.Paramsgdriveuploadfileurl, req).trim();
		m_sgdriverenamefolderurl = clsManageRequestParameters.get_Request_Parameter(SMOptionInput.Paramsgdriverenamefolderurl, req).trim();
		if (req.getParameter(SMOptionInput.Paramicopysalesleadfolderurltoorder) == null){
			m_icopysalesleadfolderurltoorder = "0";
		}else{
			m_icopysalesleadfolderurltoorder = "1";
		}
		m_datcurrentperiodstartdate = clsManageRequestParameters.get_Request_Parameter(SMOptionInput.Paramdatcurrentperiodstartdate, req).trim();
		if (getscurrentperiodstartdate().compareToIgnoreCase("") == 0){
			setscurrentperiodstartdate(SMOption.EMPTY_DATE_STRING);
		}
		m_datcurrentperiodenddate = clsManageRequestParameters.get_Request_Parameter(SMOptionInput.Paramdatcurrentperiodenddate, req).trim();
		if (getscurrentperiodenddate().compareToIgnoreCase("") == 0){
			setscurrentperiodenddate(SMOption.EMPTY_DATE_STRING);
		}
		m_isignatureboxwidth = clsManageRequestParameters.get_Request_Parameter(SMOptionInput.Paramisigantureboxwidth, req).trim();
		m_sgoogleapikey = clsManageRequestParameters.get_Request_Parameter(SMOptionInput.Paramsgoogleapikey, req).trim();
		m_sgoogleapiclientid = clsManageRequestParameters.get_Request_Parameter(SMOptionInput.Paramsgoogleapiclientid, req).trim();
		m_sgoogleapiprojectid = clsManageRequestParameters.get_Request_Parameter(SMOptionInput.Paramsgoogleapiprojectid, req).trim();
		m_sgoogledomain = clsManageRequestParameters.get_Request_Parameter(SMOptionInput.Paramsgoogledomain, req).trim();
		
		if (req.getParameter(SMOptionInput.Paramiusegoogledrivepickerapi) == null){
			m_iusegoogledrivepickerapi = "0";
		}else{
			m_iusegoogledrivepickerapi = "1";
		}
		
		if (req.getParameter(SMOptionInput.Paramiusegoogleplacesapi) == null){
			m_iusegoogleplacesapi = "0";
		}else{
			m_iusegoogleplacesapi = "1";
		}
		m_ifeedgl = clsManageRequestParameters.get_Request_Parameter(SMOptionInput.Paramifeedgl, req).trim();
	}
	
	public void clearErrorMessages(){
		m_sErrorMessageArray.clear();
	}
	public ArrayList<String> getErrorMessages(){
		return m_sErrorMessageArray;
	}
	public String getErrorMessageString(){
		String s = "";
		for (int i = 0; i < m_sErrorMessageArray.size(); i ++){
			s += "\n" + m_sErrorMessageArray.get(i);
		}
		return s;
	}
	public boolean loadToSMOptionClass (SMOption smoption){
		
		boolean bEntriesAreValid = true;
		//clearErrorMessages();
		//m_sErrorMessageArray = new ArrayList<String>(0);
		//Set the entry values:
		if(!smoption.setFileExportPath(m_sfileexportpath)){
			bEntriesAreValid = false;
			m_sErrorMessageArray.add("Invalid File Export Path - " + m_sfileexportpath + "'.");
		}
		if(!smoption.setLastEditProcess(m_slasteditprocess)){
			bEntriesAreValid = false;
			m_sErrorMessageArray.add("Invalid last edit process - " + m_slasteditprocess + "'.");
		}
		if(!smoption.setLastEditUserFullName(m_slastedituserfullname)){
			bEntriesAreValid = false;
			m_sErrorMessageArray.add("Invalid last edit user fullname - '" + m_slastedituserfullname + "'.");
		}
		if(!smoption.setLastEditUserID(m_llastedituserid)){
			bEntriesAreValid = false;
			m_sErrorMessageArray.add("Invalid last edit user id - '" + m_llastedituserid + "'.");
		}
		if(!smoption.setSMTPServer(m_ssmtpserver)){
			bEntriesAreValid = false;
			m_sErrorMessageArray.add("Invalid smtp server - '" + m_ssmtpserver + "'.");
		}
		if(!smoption.setSMTPPort(m_ssmtpport)){
			bEntriesAreValid = false;
			m_sErrorMessageArray.add("Invalid smtp port - '" + m_ssmtpport + "'.");
		}
		if(!smoption.setSMTPSourceServerName(m_ssmtpsourceservername)){
			bEntriesAreValid = false;
			m_sErrorMessageArray.add("Invalid smtp source server name - '" + m_ssmtpsourceservername + "'.");
		}
		if(!smoption.setTimeCardDatabase(m_stimecarddatabase)){
			bEntriesAreValid = false;
			m_sErrorMessageArray.add("Invalid time card database name - '" + m_stimecarddatabase + "'.");
		}
		if(!smoption.setOrderDocsFTPUrl(m_sorderdocsftpurl)){
			bEntriesAreValid = false;
			m_sErrorMessageArray.add("Invalid order documents ftp url - '" + m_sorderdocsftpurl + "'.");
		}
		if(!smoption.setBidDocsFTPUrl(m_sbiddocsftpurl)){
			bEntriesAreValid = false;
			m_sErrorMessageArray.add("Invalid " + SMBidEntry.ParamObjectName + " documents ftp url -' " + m_sbiddocsftpurl + "'.");
		}
		if(!smoption.setBackgroundColor(m_sbackgroundcolor)){
			bEntriesAreValid = false;
			m_sErrorMessageArray.add("Invalid background color - '" + m_sbackgroundcolor + "'.");
		}
		if(!smoption.setSMTPUser(m_ssmtpusername)){
			bEntriesAreValid = false;
			m_sErrorMessageArray.add("Invalid SMTP user name - '" + m_ssmtpusername + "'.");
		}
		if(!smoption.setSMTPPassword(m_ssmtppassword)){
			bEntriesAreValid = false;
			m_sErrorMessageArray.add("Invalid SMTP Password - '" + m_ssmtppassword + "'.");
		}
		if(!smoption.setSMTPReplyToAddress(m_ssmtpreplytoname)){
			bEntriesAreValid = false;
			m_sErrorMessageArray.add("Invalid SMTP reply-to name - '" + m_ssmtpreplytoname + "'.");
		}
		if(!smoption.setSMTPAuthentication(m_sauthentication)){
			bEntriesAreValid = false;
			m_sErrorMessageArray.add("Invalid value for SMTP authentication - '" + m_sauthentication + "'.");
		}
		if(!smoption.setftpexporturl(m_sftpexporturl)){
			bEntriesAreValid = false;
			m_sErrorMessageArray.add("Invalid value for FTP export URL - '" + m_sftpexporturl + "'.");
		}
		if(!smoption.setftpexportuser(m_sftpexportuser)){
			bEntriesAreValid = false;
			m_sErrorMessageArray.add("Invalid value for FTP export user - '" + m_sftpexportuser + "'.");
		}
		if(!smoption.setftpexportpw(m_sftpexportpw)){
			bEntriesAreValid = false;
			m_sErrorMessageArray.add("Invalid value for FTP export password - '" + m_sftpexportpw + "'.");
		}
		if(!smoption.setftpfileexportpath(m_sftpfileexportpath)){
			bEntriesAreValid = false;
			m_sErrorMessageArray.add("Invalid value for FTP file export path - '" + m_sftpfileexportpath + "'.");
		}
		if(!smoption.setisignatureboxwidth(m_isignatureboxwidth)){
			bEntriesAreValid = false;
			m_sErrorMessageArray.add("Invalid value for signature box size - '" + m_isignatureboxwidth + "'.");
		}
		
		//if(!smoption.setsbankname(m_sbankname)){
		//	bEntriesAreValid = false;
		//	m_sErrorMessageArray.add("Invalid value for Bank Name - " + m_sbankname + "'.");
		//}
		//if(!smoption.setsbankrecglacct(m_sbankrecglacct)){
		//	bEntriesAreValid = false;
		//	m_sErrorMessageArray.add("Invalid value for Bank Rec GL Acct - " + m_sbankrecglacct + "'.");
		//}
		if(!smoption.setscreatebankrecexport(m_screatebankrecexport)){
			bEntriesAreValid = false;
			m_sErrorMessageArray.add("Invalid value for 'Create Bank Reconciliation' - " + m_screatebankrecexport + "'.");
		}
		
		smoption.setsgoogleapikey(m_sgoogleapikey);
		smoption.setsgoogleapiclientid(m_sgoogleapiclientid);
		smoption.setsgoogleapiprojectid(m_sgoogleapiprojectid);
		smoption.setsgoogledomain(m_sgoogledomain);
		smoption.setiusegoogledrivepickerapi(m_iusegoogledrivepickerapi);
		smoption.setiusegoogleplacesapi(m_iusegoogleplacesapi);
		smoption.setgdriveorderparentfolderid(m_sgdriveorderparentfolderid);
		smoption.setgdriveorderfolderprefix(m_sgdriveorderfolderprefix);
		smoption.setgdriveorderfoldersuffix(m_sgdriveorderfoldersuffix);
		smoption.setgdrivesalesleadparentfolderid(m_sgdrivesalesleadparentfolderid);
		smoption.setgdrivesalesleadfolderprefix(m_sgdrivesalesleadfolderprefix);
		smoption.setgdrivesalesleadfoldersuffix(m_sgdrivesalesleadfoldersuffix);
		smoption.setgdriveworkorderparentfolderid(m_sgdriveworkorderparentfolderid);
		smoption.setgdriveworkorderfolderprefix(m_sgdriveworkorderfolderprefix);
		smoption.setgdriveworkorderfoldersuffix(m_sgdriveworkorderfoldersuffix);
		//smoption.setgdrivecreatenewfolderurl(m_sgdrivecreatenewfolderurl);
		smoption.setgdriveuploadfileurl(m_sgdriveuploadfileurl);
		smoption.setgdriverenamefolderurl(m_sgdriverenamefolderurl);
		smoption.setcopysalesleadfolderurltoorder(m_icopysalesleadfolderurltoorder);
		smoption.setscurrentperiodstartdate(m_datcurrentperiodstartdate);
		smoption.setscurrentperiodenddate(m_datcurrentperiodenddate);
		smoption.setsfeedgl(m_ifeedgl);
		return bEntriesAreValid;
	}
	
	public boolean loadFromSMOptionClass(SMOption smoption){
		
		m_sdummykey = smoption.getDummyKey();
		m_snextorderuniquifier = smoption.getNextOrderUniquifier();
		m_snextordernumber = smoption.getNextOrderNumber();
		m_slastedituserfullname = smoption.getLastEditUserFullName();
		m_llastedituserid = smoption.getLastEditUserID();
		m_slasteditprocess = smoption.getLastEditProcess();
		m_slasteditdate = smoption.getLastEditDate();
		m_slastedittime = smoption.getLastEditTime();
		m_snextinvoicenumber = smoption.getNextInvoiceNumber();
		m_sfileexportpath = smoption.getFileExportPath();
		m_ssmtpserver = smoption.getSMTPServer();
		m_ssmtpport = smoption.getSMTPPort();
		m_ssmtpsourceservername = smoption.getSMTPSourceServerName();
		m_stimecarddatabase = smoption.getTimeCardDatabase();
		m_sorderdocsftpurl = smoption.getOrderDocsFTPUrl();
		m_sbiddocsftpurl = smoption.getBidDocsFTPUrl();
		m_sbackgroundcolor = smoption.getBackGroundColor();
		m_ssmtpusername = smoption.getSMTPUserName();
		m_ssmtppassword = smoption.getSMTPPassword();
		m_ssmtpreplytoname = smoption.getSMTPReplyToAddress();
		m_sauthentication = smoption.getSMTPAuthentication();
		m_sftpexporturl = smoption.getftpexporturl();
		m_sftpexportuser = smoption.getftpexportuser();
		m_sftpexportpw = smoption.getftpexportpw();
		m_sftpfileexportpath = smoption.getftpfileexportpath();
		//m_sbankname = smoption.getsbankname();
		//m_sbankrecglacct = smoption.getbankrecglacct();
		m_screatebankrecexport = smoption.getcreatebankrecexport();
		m_sgdriveorderparentfolderid = smoption.getgdriveorderparentfolderid();
		m_sgdriveorderfolderprefix = smoption.getgdriveorderfolderprefix();
		m_sgdriveorderfoldersuffix = smoption.getgdriveorderfoldersuffix();
		m_sgdrivesalesleadparentfolderid = smoption.getgdrivesalesleadparentfolderid();
		m_sgdrivesalesleadfolderprefix = smoption.getgdrivesalesleadfolderprefix();
		m_sgdrivesalesleadfoldersuffix = smoption.getgdrivesalesleadfoldersuffix();
		m_sgdriveworkorderparentfolderid = smoption.getgdriveworkorderparentfolderid();
		m_sgdriveworkorderfolderprefix = smoption.getgdriveworkorderfolderprefix();
		m_sgdriveworkorderfoldersuffix = smoption.getgdriveworkorderfoldersuffix();
		//m_sgdrivecreatenewfolderurl = smoption.getgdrivecreatenewfolderurl();
		m_sgdriveuploadfileurl = smoption.getgdriveuploadfileurl();
		m_sgdriverenamefolderurl = smoption.getgdriverenamefolderurl();
		m_icopysalesleadfolderurltoorder = smoption.getcopysalesleadfolderurltoorder();
		m_datcurrentperiodstartdate = smoption.getscurrentperiodstartdate();
		m_datcurrentperiodenddate = smoption.getscurrentperiodenddate();
		m_isignatureboxwidth = smoption.getisignatureboxwidth();
		m_sgoogleapikey = smoption.getsgoogleapikey();
		m_sgoogleapiclientid = smoption.getsgoogleapiclientid();
		m_sgoogleapiprojectid = smoption.getsgoogleapiprojectid();
		m_sgoogledomain= smoption.getsgoogledomain();
		m_iusegoogledrivepickerapi = smoption.getiusegoogledrivepickerapi();
		m_iusegoogleplacesapi = smoption.getiusegoogleplacesapi();
		m_ifeedgl = smoption.getsfeedgl();
		return true;
	}
	public String getM_sdummykey() {
		return m_sdummykey;
	}
	public void setM_sdummykey(String m_sdummykey) {
		this.m_sdummykey = m_sdummykey;
	}
	public String getM_snextorderuniquifier() {
		return m_snextorderuniquifier;
	}
	public void setM_snextorderuniquifier(String m_snextorderuniquifier) {
		this.m_snextorderuniquifier = m_snextorderuniquifier;
	}
	public String getM_snextordernumber() {
		return m_snextordernumber;
	}
	public void setM_snextordernumber(String m_snextordernumber) {
		this.m_snextordernumber = m_snextordernumber;
	}
	public String getM_slastedituserfullname() {
		return m_slastedituserfullname;
	}
	public void setM_slastedituserfullname(String m_slastedituser) {
		this.m_slastedituserfullname = m_slastedituser;
	}
	public String getM_llastedituserid() {
		return m_llastedituserid;
	}
	public void setM_slastedituserid(String m_slastedituser) {
		this.m_llastedituserid = m_slastedituser;
	}
	public String getM_slasteditprocess() {
		return m_slasteditprocess;
	}
	public void setM_slasteditprocess(String m_slasteditprocess) {
		this.m_slasteditprocess = m_slasteditprocess;
	}
	public String getM_slasteditdate() {
		return m_slasteditdate;
	}
	public void setM_slasteditdate(String m_slasteditdate) {
		this.m_slasteditdate = m_slasteditdate;
	}
	public String getM_slastedittime() {
		return m_slastedittime;
	}
	public void setM_slastedittime(String m_slastedittime) {
		this.m_slastedittime = m_slastedittime;
	}
	public String getM_snextinvoicenumber() {
		return m_snextinvoicenumber;
	}
	public void setM_snextinvoicenumber(String m_snextinvoicenumber) {
		this.m_snextinvoicenumber = m_snextinvoicenumber;
	}
	public void setM_ssmtpusername(String m_ssmtpusername) {
		this.m_ssmtpusername = m_ssmtpusername;
	}
	public void setM_ssmtppassword(String m_ssmtppassword) {
		this.m_ssmtppassword = m_ssmtppassword;
	}
	public void setM_ssmtpreplytoname(String m_ssmtpreplytoname) {
		this.m_ssmtpreplytoname = m_ssmtpreplytoname;
	}
	public void setM_ssmtpauthentication(String m_sauthentication) {
		this.m_sauthentication = m_sauthentication;
	}
	public String getM_sfileexportpath() {
		return m_sfileexportpath;
	}
	public String getM_ssmtpserver() {
		return m_ssmtpserver;
	}
	public String getM_ssmtpport() {
		return m_ssmtpport;
	}
	public String getM_ssmtpsourceservername() {
		return m_ssmtpsourceservername;
	}
	public String getM_stimecarddatabase() {
		return m_stimecarddatabase;
	}
	public String getM_sorderdocsftpurl() {
		return m_sorderdocsftpurl;
	}
	public String getM_sbiddocsftpurl() {
		return m_sbiddocsftpurl;
	}
	public String getM_sbackgroundcolor() {
		return m_sbackgroundcolor;
	}
	public String getM_ssmtpusername() {
		return m_ssmtpusername;
	}
	public String getM_ssmtppassword() {
		return m_ssmtppassword;
	}
	public String getM_ssmtpreplytoname() {
		return m_ssmtpreplytoname;
	}
	public String getM_ssmtpauthentication() {
		return m_sauthentication;
	}
	public String getftpexporturl() {
		return m_sftpexporturl;
	}
	public void setftpexporturl(String sftpexporturl) {
		m_sftpexporturl = sftpexporturl;
	}
	public String getftpexportuser() {
		return m_sftpexportuser;
	}
	public void setftpexportuser(String sftpexportuser) {
		m_sftpexportuser = sftpexportuser;
	}
	public String getftpexportpw() {
		return m_sftpexportpw;
	}
	public void setftpexportpw(String sftpexportpw) {
		m_sftpexportpw = sftpexportpw;
	}
	public String getftpfileexportpath() {
		return m_sftpfileexportpath;
	}
	public void setarftpexportpath(String sarftpexportpath) {
		m_sftpfileexportpath = sarftpexportpath;
	}
	//public String getbankname() {
	//	return m_sbankname;
	//}
	//public void setbankname(String sbankname) {
	//	m_sbankname = sbankname;
	//}
	//public String getbankrecglacct() {
	//	return m_sbankrecglacct;
	//}
	//public void setbankrecglacct(String sbankrecglacct) {
	//	m_sbankname = sbankrecglacct;
	//}
	public String getcreatebankrecexport() {
		return m_screatebankrecexport;
	}
	public void setcreatebankrecexport(String screatebankrecexport) {
		m_screatebankrecexport = screatebankrecexport;
	}
    public String getsgoogleapikey(){
    	return m_sgoogleapikey;
    }
    public void setsgoogleapikey(String ssgoogleapikey){
    	m_sgoogleapikey = ssgoogleapikey;
    }
    public String getsgoogleapiclientid(){
    	return m_sgoogleapiclientid;
    }
    public void setsgoogleapiclientid(String sgoogleapiclientid){
    	m_sgoogleapiclientid = sgoogleapiclientid;
    }
    public String getsgoogleapiprojectid(){
    	return m_sgoogleapiprojectid;
    }
    public void setsgoogleapiprojectid(String sgoogleapiprojectid){
    	m_sgoogleapiprojectid = sgoogleapiprojectid;
    }
    public String getsgoogledomain(){
    	return m_sgoogledomain;
    }
    public void setsgoogledomain(String sgoogledomain){
    	m_sgoogledomain = sgoogledomain;
    }
    public String getiusegoogledrivepickerapi(){
    	return m_iusegoogledrivepickerapi;
    }
    public void setiusegoogledrivepickerapi(String iusegoogledrivepickerapi){
    	m_iusegoogledrivepickerapi = iusegoogledrivepickerapi;
    }
    public String getiusegoogleplacesapi(){
    	return m_iusegoogleplacesapi;
    }
    public void setiusegoogleplacesapi(String iusegoogleplacesapi){
    	m_iusegoogleplacesapi = iusegoogleplacesapi;
    }
    public String getgdriveorderparentfolderid(){
    	return m_sgdriveorderparentfolderid;
    }
    public void setgdriveorderparentfolderid(String sgdriveorderparentfolderid){
    	m_sgdriveorderparentfolderid = sgdriveorderparentfolderid;
    }
    public String getgdriveorderfolderprefix(){
    	return m_sgdriveorderfolderprefix;
    }
    public void setgdriveorderfolderprefix(String sgdriveorderfolderprefix){
    	m_sgdriveorderfolderprefix = sgdriveorderfolderprefix;
    }
    public String getgdriveorderfoldersuffix(){
    	return m_sgdriveorderfoldersuffix;
    }
    public void setgdriveorderfoldersuffix(String sgdriveorderfoldersuffix){
    	m_sgdriveorderfoldersuffix = sgdriveorderfoldersuffix;
    }
    public String getgdrivesalesleadparentfolderid(){
    	return m_sgdrivesalesleadparentfolderid;
    }
    public void setgdrivesalesleadparentfolderid(String sgdrivesalesleadparentfolderid){
    	m_sgdrivesalesleadparentfolderid = sgdrivesalesleadparentfolderid;
    }
    public String getgdrivesalesleadfolderprefix(){
    	return m_sgdrivesalesleadfolderprefix;
    }
    public void setgdrivesalesleadfolderprefix(String sgdrivesalesleadfolderprefix){
    	m_sgdrivesalesleadfolderprefix = sgdrivesalesleadfolderprefix;
    }
    public String getgdrivesalesleadfoldersuffix(){
    	return m_sgdrivesalesleadfoldersuffix;
    }
    public void setgdrivesalesleadfoldersuffix(String sgdrivesalesleadfoldersuffix){
    	m_sgdrivesalesleadfoldersuffix = sgdrivesalesleadfoldersuffix;
    }
    public String getgdriveworkorderparentfolderid(){
    	return m_sgdriveworkorderparentfolderid;
    }
    public void setgdriveworkorderparentfolderid(String sgdriveworkorderparentfolderid){
    	m_sgdriveworkorderparentfolderid = sgdriveworkorderparentfolderid;
    }
    public String getgdriveworkorderfolderprefix(){
    	return m_sgdriveworkorderfolderprefix;
    }
    public void setgdriveworkorderfolderprefix(String sgdriveworkorderfolderprefix){
    	m_sgdriveworkorderfolderprefix = sgdriveworkorderfolderprefix;
    }
    public String getgdriveworkorderfoldersuffix(){
    	return m_sgdriveworkorderfoldersuffix;
    }
    public void setgdriveworkorderfoldersuffix(String sgdriveworkorderfoldersuffix){
    	m_sgdriveworkorderfoldersuffix = sgdriveworkorderfoldersuffix;
    }
    //public String getgdrivecreatenewfolderurl(){
    //	return m_sgdrivecreatenewfolderurl;
    //}
    //public void setgdrivecreatenewfolderurl(String sgdrivecreatenewfolderurl){
    //	m_sgdrivecreatenewfolderurl = sgdrivecreatenewfolderurl;
    //}
    
    public String getgdriveuploadfileurl(){
    	return m_sgdriveuploadfileurl;
    }
    public void setgdriveuploadfileurl(String sgdriveuploadfileurl){
    	m_sgdriveuploadfileurl = sgdriveuploadfileurl;
    }
    
    public String getcopysalesleadfolderurltoorder(){
    	return m_icopysalesleadfolderurltoorder;
    }
    public void setcopysalesleadfolderurltoorder(String icopysalesleadfolderurltoorder){
    	m_icopysalesleadfolderurltoorder = icopysalesleadfolderurltoorder;
    }
    
    public String getgdriverenamefolderurl(){
    	return m_sgdriverenamefolderurl;
    }
    public void setgdriverenamefolderurl(String sgdriverenamefolderurl){
    	m_sgdriverenamefolderurl = sgdriverenamefolderurl;
    }
    
    public String getisignatureboxwidth(){
    	return m_isignatureboxwidth;
    }
    public void setisignatureboxwidth(String isignatureboxwidth){
    	m_isignatureboxwidth = isignatureboxwidth;
    }
    
    public String getscurrentperiodstartdate(){
       	if (m_datcurrentperiodstartdate.compareToIgnoreCase(SMOption.EMPTY_DATE_STRING) == 0){
    		return "";
    	}else{
    		return m_datcurrentperiodstartdate;    		
    	}
    }
    public void setscurrentperiodstartdate(String scurrentperiodstartdate){
    	m_datcurrentperiodstartdate = scurrentperiodstartdate;
    }
    
    public String getscurrentperiodenddate(){
       	if (m_datcurrentperiodenddate.compareToIgnoreCase(SMOption.EMPTY_DATE_STRING) == 0){
    		return "";
    	}else{
    		return m_datcurrentperiodenddate;    		
    	}
    }
    public void setscurrentperiodenddate(String scurrentperiodenddate){
    	m_datcurrentperiodenddate = scurrentperiodenddate;
    }
    
	public String getsfeedgl() {
		return m_ifeedgl;
	}
	public void setsfeedgl(String sfeedgl) {
		m_ifeedgl = sfeedgl;
	}
	public String getQueryString(){
	
		String sQueryString = "";
		sQueryString += ParamDummyKey + "=" + clsServletUtilities.URLEncode(m_sdummykey);
		sQueryString += "&" + ParamNextOrderUniquifier + "=" + clsServletUtilities.URLEncode(m_snextorderuniquifier);
		sQueryString += "&" + ParamNextOrderNumber + "=" + clsServletUtilities.URLEncode(m_snextordernumber);
		sQueryString += "&" + ParamLastEditUserFullName + "=" + clsServletUtilities.URLEncode(m_slastedituserfullname);
		sQueryString += "&" + ParamLastEditUserID + "=" + clsServletUtilities.URLEncode(m_llastedituserid);
		sQueryString += "&" + ParamLastEditProcess + "=" + clsServletUtilities.URLEncode(m_slasteditprocess);
		sQueryString += "&" + ParamLastEditDate + "=" + clsServletUtilities.URLEncode(m_slasteditdate);
		sQueryString += "&" + ParamLastEditTime + "=" + clsServletUtilities.URLEncode(m_slastedittime);
		sQueryString += "&" + ParamNextInvoiceNumber + "=" + clsServletUtilities.URLEncode(m_snextinvoicenumber);
		sQueryString += "&" + ParamFileExportPath + "=" + clsServletUtilities.URLEncode(m_sfileexportpath);
		sQueryString += "&" + ParamSMTPServer + "=" + clsServletUtilities.URLEncode(m_ssmtpserver);
		sQueryString += "&" + ParamSMTPPort + "=" + clsServletUtilities.URLEncode(m_ssmtpport);
		sQueryString += "&" + ParamSMTPSourceServerName + "=" + clsServletUtilities.URLEncode(m_ssmtpsourceservername);
		sQueryString += "&" + ParamTimeCardDatabase + "=" + clsServletUtilities.URLEncode(m_stimecarddatabase);
		sQueryString += "&" + ParamOrderDocsFTPUrl + "=" + clsServletUtilities.URLEncode(m_sorderdocsftpurl);
		sQueryString += "&" + ParamBidDocsFTPUrl + "=" + clsServletUtilities.URLEncode(m_sbiddocsftpurl);
		sQueryString += "&" + Paramsbackgroundcolor + "=" + clsServletUtilities.URLEncode(m_sbackgroundcolor);
		sQueryString += "&" + Paramssmtpusername + "=" + clsServletUtilities.URLEncode(m_ssmtpusername);
		sQueryString += "&" + Paramssmtppassword + "=" + clsServletUtilities.URLEncode(m_ssmtppassword);
		sQueryString += "&" + Paramssmtpreplytoname + "=" + clsServletUtilities.URLEncode(m_ssmtpreplytoname);
		sQueryString += "&" + Paramismtpauthentication + "=" + clsServletUtilities.URLEncode(m_sauthentication);
		sQueryString += "&" + Paramsftpexporturl + "=" + clsServletUtilities.URLEncode(m_sftpexporturl);
		sQueryString += "&" + Paramsftpexportuser + "=" + clsServletUtilities.URLEncode(m_sftpexportuser);
		sQueryString += "&" + Paramsftpexportpw + "=" + clsServletUtilities.URLEncode(m_sftpexportpw);
		sQueryString += "&" + Paramsftpfileexportpath + "=" + clsServletUtilities.URLEncode(m_sftpexporturl);
		sQueryString += "&" + Paramsbankname + "=" + clsServletUtilities.URLEncode(m_sftpexporturl);
		sQueryString += "&" + Paramsbankrecglacct + "=" + clsServletUtilities.URLEncode(m_sftpexporturl);
		sQueryString += "&" + Paramicreatebankrecexport + "=" + clsServletUtilities.URLEncode(m_sftpexporturl);
		sQueryString += "&" + Paramsgdriveorderparentfolderid + "=" + clsServletUtilities.URLEncode(m_sgdriveorderparentfolderid);
		sQueryString += "&" + Paramsgdriveorderfolderprefix + "=" + clsServletUtilities.URLEncode(m_sgdriveorderfolderprefix);
		sQueryString += "&" + Paramsgdriveorderfoldersuffix + "=" + clsServletUtilities.URLEncode(m_sgdriveorderfoldersuffix);
		sQueryString += "&" + Paramsgdrivesalesleadparentfolderid + "=" + clsServletUtilities.URLEncode(m_sgdrivesalesleadparentfolderid);
		sQueryString += "&" + Paramsgdrivesalesleadfolderprefix + "=" + clsServletUtilities.URLEncode(m_sgdrivesalesleadfolderprefix);
		sQueryString += "&" + Paramsgdrivesalesleadfoldersuffix + "=" + clsServletUtilities.URLEncode(m_sgdrivesalesleadfoldersuffix);
		sQueryString += "&" + Paramsgdriveworkorderparentfolderid + "=" + clsServletUtilities.URLEncode(m_sgdriveworkorderparentfolderid);
		sQueryString += "&" + Paramsgdriveworkorderfolderprefix + "=" + clsServletUtilities.URLEncode(m_sgdriveworkorderfolderprefix);
		sQueryString += "&" + Paramsgdriveworkorderfoldersuffix + "=" + clsServletUtilities.URLEncode(m_sgdriveworkorderfoldersuffix);
		//sQueryString += "&" + Paramsgdrivecreatenewfolderurl + "=" + ServletUtilities.URLEncode(m_sgdrivecreatenewfolderurl);
		sQueryString += "&" + Paramsgdriveuploadfileurl + "=" + clsServletUtilities.URLEncode(m_sgdriveuploadfileurl);
		sQueryString += "&" + Paramicopysalesleadfolderurltoorder + "=" + clsServletUtilities.URLEncode(m_icopysalesleadfolderurltoorder);
		sQueryString += "&" + Paramsgdriverenamefolderurl + "=" + clsServletUtilities.URLEncode(m_sgdriverenamefolderurl);
		sQueryString += "&" + Paramdatcurrentperiodstartdate + "=" + clsServletUtilities.URLEncode(m_datcurrentperiodstartdate);
		sQueryString += "&" + Paramdatcurrentperiodenddate + "=" + clsServletUtilities.URLEncode(m_datcurrentperiodenddate);
		sQueryString += "&" + Paramsgoogleapikey + "=" + m_sgoogleapikey;
		sQueryString += "&" + Paramiusegoogleplacesapi + "=" + m_iusegoogleplacesapi;
		sQueryString += "&" + Paramifeedgl + "=" + m_ifeedgl;

		return sQueryString;
	}
}