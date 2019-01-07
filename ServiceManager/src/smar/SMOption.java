package smar;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.servlet.ServletContext;

import smcontrolpanel.SMBidEntry;
import smcontrolpanel.SMUtilities;
import SMClasses.SMLogEntry;
import SMDataDefinition.SMTableorderheaders;
import SMDataDefinition.SMTablesmoptions;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsDateAndTimeConversions;
import ServletUtilities.clsStringFunctions;

public class SMOption {
	
	public final static String EMPTY_DATE_STRING = "00/00/0000";
	
	private int m_idummykey;
	private long m_lnextorderuniquifier;
	private String m_snextordernumber;
	private String m_slastedituserfullname;
	private long  m_llastedituserid;
	private String m_slasteditprocess;
	private long m_llasteditdate;
	private long m_llastedittime;
	private String m_snextinvoicenumber;
	private String m_sfileexportpath;
	private String m_ssmtpserver;
	private String m_ssmtpport;
	private String m_ssmtpsourceservername;
	private String m_stimecarddatabase;
	private String m_sftporderdocsurl;
	private String m_sftpbiddocsurl;
	private String m_sbackgroundcolor;
	private String m_serrormessage;
	private String m_ssmtpusername;
	private String m_ssmtppassword;
	private String m_iusesauthentication;
	private String m_sinvoicelogofilename;
	private String m_sproposallogofilename;
	private String m_sftpexporturl;
	private String m_sftpexportuser;
	private String m_sftpexportpw;
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
	private String m_icopysalesleadfolderurltoorder;
	private String m_sgdriverenamefolderurl;
	private String m_sdatcurrentperiodstartdate;
	private String m_sdatcurrentperiodenddate;
	private String m_isignatureboxwidth;
	private String m_sgoogleapikey;
	private String m_iusegoogleplacesapi;
	private String m_ssmtpreplytoname;
		
	public SMOption(){
		m_idummykey = 0;
		m_lnextorderuniquifier = 0;
		m_snextordernumber = "";
		m_slastedituserfullname = "";
		m_llastedituserid = 0;
		m_slasteditprocess = "";
		m_llasteditdate = 0;
		m_llastedittime = 0;
		m_sfileexportpath = "";
		m_snextinvoicenumber = "";
		m_ssmtpserver = "";
		m_ssmtpport = "";
		m_ssmtpsourceservername = "";
		m_stimecarddatabase = "";
		m_sftporderdocsurl = "";
		m_sftpbiddocsurl = "";
		m_sbackgroundcolor = "";
		m_ssmtpusername = "";
		m_ssmtppassword = "";
		m_ssmtpreplytoname = "";
		m_iusesauthentication = "0";
		m_sinvoicelogofilename = "";
		m_sproposallogofilename = "";
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
		m_icopysalesleadfolderurltoorder = "0";
		m_sgdriverenamefolderurl = "";
		m_sdatcurrentperiodstartdate = "";
		m_sdatcurrentperiodenddate = "";
		m_isignatureboxwidth = "400";
		m_sgoogleapikey = "";
		m_iusegoogleplacesapi = "0";
	}
    public boolean load (
    	Connection conn
    ){
    	String SQL = "SELECT * FROM " + SMTablesmoptions.TableName;
		//System.out.println("[1332266265]:" + SQL);
    	try {
    		ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
    		if (rs.next()){
    			m_idummykey = rs.getInt(SMTablesmoptions.idummykey);
    			m_lnextorderuniquifier = rs.getLong(SMTablesmoptions.lnextorderuniquifier);
    			m_snextordernumber = rs.getString(SMTablesmoptions.lnextordernumber);
    			m_slastedituserfullname = rs.getString(SMTablesmoptions.slastedituserfullname);
    			m_llastedituserid = rs.getLong(SMTablesmoptions.llastedituserid);
    			m_slasteditprocess = rs.getString(SMTablesmoptions.slasteditprocess);
    			m_llasteditdate = rs.getLong(SMTablesmoptions.slasteditdate);
    			m_llastedittime = rs.getLong(SMTablesmoptions.slastedittime);
    			m_sfileexportpath = rs.getString(SMTablesmoptions.sfileexportpath);
    			m_snextinvoicenumber = rs.getString(SMTablesmoptions.snextinvoicenumber);
    			m_ssmtpserver = rs.getString(SMTablesmoptions.ssmtpserver);
    			m_ssmtpport = rs.getString(SMTablesmoptions.ssmtpport);
    			m_ssmtpsourceservername = rs.getString(SMTablesmoptions.ssmtpsourceservername);
    			m_stimecarddatabase = rs.getString(SMTablesmoptions.stimecarddatabase);
    			m_sftporderdocsurl = rs.getString(SMTablesmoptions.sorderdocsftpurl);
    			m_sftpbiddocsurl = rs.getString(SMTablesmoptions.sbiddocsftpurl);
    			m_sbackgroundcolor = rs.getString(SMTablesmoptions.sbackgroundcolor);
    			m_ssmtpusername = rs.getString(SMTablesmoptions.ssmtpusername);
    			m_ssmtppassword = rs.getString(SMTablesmoptions.ssmtppassword);
    			m_ssmtpreplytoname = rs.getString(SMTablesmoptions.ssmtpreplytoname);
    			m_iusesauthentication = Long.toString(rs.getLong(SMTablesmoptions.iusesauthentication));
    			m_sinvoicelogofilename = rs.getString(SMTablesmoptions.sinvoicelogofilename);
    			m_sproposallogofilename = rs.getString(SMTablesmoptions.sproposallogofilename);
    			m_sftpexporturl = rs.getString(SMTablesmoptions.sftpexporturl);
    			m_sftpexportuser = rs.getString(SMTablesmoptions.sftpexportuser);
    			m_sftpexportpw = rs.getString(SMTablesmoptions.sftpexportpw);
    			m_sftpfileexportpath = rs.getString(SMTablesmoptions.sftpfileexportpath);
    			//m_sbankname = rs.getString(SMTablesmoptions.sbankname);
    			//m_sbankrecglacct = rs.getString(SMTablesmoptions.sbankrecglacct);
    			m_screatebankrecexport = rs.getString(SMTablesmoptions.icreatebankrecexport);
    			m_sgdriveorderparentfolderid = rs.getString(SMTablesmoptions.gdriveorderparentfolderid);
    			m_sgdriveorderfolderprefix = rs.getString(SMTablesmoptions.gdriveorderfolderprefix);
    			m_sgdriveorderfoldersuffix = rs.getString(SMTablesmoptions.gdriveorderfoldersuffix);
    			m_sgdrivesalesleadparentfolderid = rs.getString(SMTablesmoptions.gdrivesalesleadparentfolderid);
    			m_sgdrivesalesleadfolderprefix = rs.getString(SMTablesmoptions.gdrivesalesleadfolderprefix);
    			m_sgdrivesalesleadfoldersuffix = rs.getString(SMTablesmoptions.gdrivesalesleadfoldersuffix);
    			m_sgdriveworkorderparentfolderid = rs.getString(SMTablesmoptions.gdriveworkorderparentfolderid);
    			m_sgdriveworkorderfolderprefix = rs.getString(SMTablesmoptions.gdriveworkorderfolderprefix);
    			m_sgdriveworkorderfoldersuffix = rs.getString(SMTablesmoptions.gdriveworkorderfoldersuffix);
    			//m_sgdrivecreatenewfolderurl = rs.getString(SMTablesmoptions.gdrivecreatenewfolderurl);
    			m_sgdriveuploadfileurl = rs.getString(SMTablesmoptions.gdriveuploadfileurl);
    			m_icopysalesleadfolderurltoorder = rs.getString(SMTablesmoptions.icopysalesleadfolderurltoorder);
    			m_sgdriverenamefolderurl = rs.getString(SMTablesmoptions.gdriverenamefolderurl);
    			m_sdatcurrentperiodstartdate = clsDateAndTimeConversions.resultsetDateStringToString(rs.getString(SMTablesmoptions.datpostingperiodstartdate));
    			m_sdatcurrentperiodenddate = clsDateAndTimeConversions.resultsetDateStringToString(rs.getString(SMTablesmoptions.datpostingperiodenddate));
    			m_isignatureboxwidth = Integer.toString(rs.getInt(SMTablesmoptions.isignatureboxwidth));
    			m_sgoogleapikey = rs.getString(SMTablesmoptions.sgoogleapikey);
    			m_iusegoogleplacesapi = Integer.toString(rs.getInt(SMTablesmoptions.iusegoogleplacesapi));
    			rs.close();
    			return true;
    		}else{
    			m_serrormessage = "Could not get record";
    			rs.close();
    			return false;
    		}
    	}catch (SQLException e){
    		m_serrormessage = "SQL Error: " + e.getMessage();
    		return false;
    	}
	}
    public void load(String sConf, ServletContext context, String sUserID) throws Exception{
    	Connection conn;
		try {
			conn = clsDatabaseFunctions.getConnectionWithException(
				context, 
				sConf, 
				"MySQL", 
				SMUtilities.getFullClassName(this.toString()) + ".load - userID: " + sUserID);
		} catch (Exception e) {
			throw new Exception("Error getting connection to load SMOptions - " + e.getMessage());
		}
    	if (conn == null){
    		throw new Exception("Could not get connection to load SMOptions.");
    	}
    	if (!load(conn)){
    		clsDatabaseFunctions.freeConnection(context, conn);
    		throw new Exception("Error loading SMOptions - " + getErrorMessage());
    	}
    	clsDatabaseFunctions.freeConnection(context, conn);
    }
    private void validate_entries() throws Exception{
    	String sErrors = "";
    	boolean bValid = true;
    	
    	if (getscurrentperiodstartdate().compareToIgnoreCase(EMPTY_DATE_STRING) != 0){
	        if (!clsDateAndTimeConversions.IsValidDateString("M/d/yyyy", getscurrentperiodstartdate())){
	        	bValid = false;
	        	sErrors += "Invalid current posting period START date: '" + getscurrentperiodstartdate() + "'.  ";
	        }
    	}
    	
    	if (getscurrentperiodenddate().compareToIgnoreCase(EMPTY_DATE_STRING) != 0){
			if (!clsDateAndTimeConversions.IsValidDateString("M/d/yyyy", getscurrentperiodenddate())){
				bValid = false;
				sErrors += "Invalid current posting period END date: '" + getscurrentperiodenddate() + "'.  ";
			}
	        if (bValid == false){
	        	throw new Exception(sErrors);
	        }
    	}
    }
    public void save(Connection conn) throws Exception{

    	try {
			validate_entries();
		} catch (Exception e1) {
			throw new Exception("Cannot save - " + e1.getMessage());
		}
    	
		String SQL = "UPDATE " + SMTablesmoptions.TableName
		+ " SET "
		+ SMTablesmoptions.sfileexportpath + " = '" + clsDatabaseFunctions.FormatSQLStatement(m_sfileexportpath) + "'"
		+ ", " + SMTablesmoptions.slasteditdate + " = DATE_FORMAT(NOW(), '%Y%m%d')"
		+ ", " + SMTablesmoptions.slasteditprocess + " = '" + m_slasteditprocess + "'"
		+ ", " + SMTablesmoptions.slastedittime + " = DATE_FORMAT(NOW(), '%H%i%s')"
		+ ", " + SMTablesmoptions.slastedituserfullname + " = '" + clsDatabaseFunctions.FormatSQLStatement(m_slastedituserfullname) + "'"
		+ ", " + SMTablesmoptions.llastedituserid + " = " + Long.toString(m_llastedituserid) 
		+ ", " + SMTablesmoptions.ssmtpserver + " = '" + clsDatabaseFunctions.FormatSQLStatement(m_ssmtpserver) + "'"
		+ ", " + SMTablesmoptions.ssmtpport + " = '" + clsDatabaseFunctions.FormatSQLStatement(m_ssmtpport) + "'"
		+ ", " + SMTablesmoptions.ssmtpsourceservername + " = '" + clsDatabaseFunctions.FormatSQLStatement(m_ssmtpsourceservername) + "'"
		+ ", " + SMTablesmoptions.stimecarddatabase + " = '" + clsDatabaseFunctions.FormatSQLStatement(m_stimecarddatabase) + "'"
		+ ", " + SMTablesmoptions.sorderdocsftpurl + " = '" + clsDatabaseFunctions.FormatSQLStatement(m_sftporderdocsurl) + "'"
		+ ", " + SMTablesmoptions.sbiddocsftpurl + " = '" + clsDatabaseFunctions.FormatSQLStatement(m_sftpbiddocsurl) + "'"
		+ ", " + SMTablesmoptions.sbackgroundcolor + " = '" + clsDatabaseFunctions.FormatSQLStatement(m_sbackgroundcolor) + "'"
		+ ", " + SMTablesmoptions.snextinvoicenumber + " = '" + m_snextinvoicenumber + "'"
		+ ", " + SMTablesmoptions.lnextordernumber + " = '" + m_snextordernumber + "'"
		+ ", " + SMTablesmoptions.lnextorderuniquifier + " = " + m_lnextorderuniquifier
		+ ", " + SMTablesmoptions.ssmtpusername + " = '" + clsDatabaseFunctions.FormatSQLStatement(m_ssmtpusername) + "'"
		+ ", " + SMTablesmoptions.ssmtppassword + " = '" + clsDatabaseFunctions.FormatSQLStatement(m_ssmtppassword) + "'"
		+ ", " + SMTablesmoptions.ssmtpreplytoname + " = '" + clsDatabaseFunctions.FormatSQLStatement(m_ssmtpreplytoname) + "'"
		+ ", " + SMTablesmoptions.iusesauthentication + " = " + m_iusesauthentication
		+ ", " + SMTablesmoptions.sinvoicelogofilename + " = '" + clsDatabaseFunctions.FormatSQLStatement(m_sinvoicelogofilename) + "'"
		+ ", " + SMTablesmoptions.sproposallogofilename + " = '" + clsDatabaseFunctions.FormatSQLStatement(m_sproposallogofilename) + "'"
		+ ", " + SMTablesmoptions.sftpexporturl + " = '" + clsDatabaseFunctions.FormatSQLStatement(m_sftpexporturl) + "'"
		+ ", " + SMTablesmoptions.sftpexportuser + " = '" + clsDatabaseFunctions.FormatSQLStatement(m_sftpexportuser) + "'"
		+ ", " + SMTablesmoptions.sftpexportpw + " = '" + clsDatabaseFunctions.FormatSQLStatement(m_sftpexportpw) + "'"
		+ ", " + SMTablesmoptions.sftpfileexportpath + " = '" + clsDatabaseFunctions.FormatSQLStatement(m_sftpfileexportpath) + "'"
		//+ ", " + SMTablesmoptions.sbankname + " = '" + SMUtilities.FormatSQLStatement(m_sbankname) + "'"
		//+ ", " + SMTablesmoptions.sbankrecglacct + " = '" + SMUtilities.FormatSQLStatement(m_sbankrecglacct) + "'"
		+ ", " + SMTablesmoptions.icreatebankrecexport + " = " + clsDatabaseFunctions.FormatSQLStatement(m_screatebankrecexport)
		+ ", " + SMTablesmoptions.gdriveorderparentfolderid + " = '" + clsDatabaseFunctions.FormatSQLStatement(m_sgdriveorderparentfolderid) + "'"
		+ ", " + SMTablesmoptions.gdriveorderfolderprefix + " = '" + clsDatabaseFunctions.FormatSQLStatement(m_sgdriveorderfolderprefix) + "'"
		+ ", " + SMTablesmoptions.gdriveorderfoldersuffix + " = '" + clsDatabaseFunctions.FormatSQLStatement(m_sgdriveorderfoldersuffix) + "'"
		+ ", " + SMTablesmoptions.gdrivesalesleadparentfolderid + " = '" + clsDatabaseFunctions.FormatSQLStatement(m_sgdrivesalesleadparentfolderid) + "'"
		+ ", " + SMTablesmoptions.gdrivesalesleadfolderprefix + " = '" + clsDatabaseFunctions.FormatSQLStatement(m_sgdrivesalesleadfolderprefix) + "'"
		+ ", " + SMTablesmoptions.gdrivesalesleadfoldersuffix + " = '" + clsDatabaseFunctions.FormatSQLStatement(m_sgdrivesalesleadfoldersuffix) + "'"
		+ ", " + SMTablesmoptions.gdriveworkorderparentfolderid + " = '" + clsDatabaseFunctions.FormatSQLStatement(m_sgdriveworkorderparentfolderid) + "'"
		+ ", " + SMTablesmoptions.gdriveworkorderfolderprefix + " = '" + clsDatabaseFunctions.FormatSQLStatement(m_sgdriveworkorderfolderprefix) + "'"
		+ ", " + SMTablesmoptions.gdriveworkorderfoldersuffix + " = '" + clsDatabaseFunctions.FormatSQLStatement(m_sgdriveworkorderfoldersuffix) + "'"
		//+ ", " + SMTablesmoptions.gdrivecreatenewfolderurl + " = '" + SMUtilities.FormatSQLStatement(m_sgdrivecreatenewfolderurl) + "'"
		+ ", " + SMTablesmoptions.gdriveuploadfileurl + " = '" + clsDatabaseFunctions.FormatSQLStatement(m_sgdriveuploadfileurl) + "'"
		+ ", " + SMTablesmoptions.icopysalesleadfolderurltoorder + " = " + clsDatabaseFunctions.FormatSQLStatement(m_icopysalesleadfolderurltoorder)
		+ ", " + SMTablesmoptions.gdriverenamefolderurl + " = '" + clsDatabaseFunctions.FormatSQLStatement(m_sgdriverenamefolderurl) + "'"
		+ ", " + SMTablesmoptions.datpostingperiodstartdate + " = '" + clsDateAndTimeConversions.stdDateStringToSQLDateString(m_sdatcurrentperiodstartdate) + "'" 
		+ ", " + SMTablesmoptions.datpostingperiodenddate + " = '" + clsDateAndTimeConversions.stdDateStringToSQLDateString(m_sdatcurrentperiodenddate) + "'"
		+ ", " + SMTablesmoptions.isignatureboxwidth + " = " + clsDatabaseFunctions.FormatSQLStatement(m_isignatureboxwidth) 
		+ ", " + SMTablesmoptions.sgoogleapikey + " = '" + clsDatabaseFunctions.FormatSQLStatement(m_sgoogleapikey)  + "'"
		+ ", " + SMTablesmoptions.iusegoogleplacesapi + " = " + clsDatabaseFunctions.FormatSQLStatement(m_iusegoogleplacesapi)  + ""
		;
		
		try {
			Statement stmt = conn.createStatement();
			stmt.execute(SQL);
		} catch (Exception e) {
			throw new Exception("Error saving SMOption with SQL: " + SQL + " - " + e.getMessage());
		}
    }
    public void saveEditableFields(ServletContext context, String sConf, String sUserName) throws Exception{
    	
    	try {
			validate_entries();
		} catch (Exception e1) {
			throw new Exception("Cannot save - " + e1.getMessage());
		}
    	
    	//Only re-sets the fields that can be edited from the 'Edit Options' screen:
    	m_llasteditdate = Long.parseLong(clsDateAndTimeConversions.now("yyyyMMdd"));
    	m_llastedittime = Long.parseLong(clsDateAndTimeConversions.now("HHmmssSSS"));
    	
		String SQL = "UPDATE " + SMTablesmoptions.TableName
		+ " SET "
		+ SMTablesmoptions.sfileexportpath + " = '" + m_sfileexportpath + "'"
		+ ", " + SMTablesmoptions.slasteditdate + " = " + Long.toString(m_llasteditdate)
		+ ", " + SMTablesmoptions.slasteditprocess + " = '" + m_slasteditprocess + "'"
		+ ", " + SMTablesmoptions.slastedittime + " = " + Long.toString(m_llastedittime)
		+ ", " + SMTablesmoptions.slastedituserfullname + " = '" + clsDatabaseFunctions.FormatSQLStatement(m_slastedituserfullname) + "'"
		+ ", " + SMTablesmoptions.llastedituserid + " = " + Long.toString( m_llastedituserid) + ""
		+ ", " + SMTablesmoptions.ssmtpserver + " = '" + clsDatabaseFunctions.FormatSQLStatement(m_ssmtpserver) + "'"
		+ ", " + SMTablesmoptions.ssmtpport + " = '" + m_ssmtpport + "'"
		+ ", " + SMTablesmoptions.ssmtpsourceservername + " = '" + clsDatabaseFunctions.FormatSQLStatement(m_ssmtpsourceservername) + "'"
		+ ", " + SMTablesmoptions.stimecarddatabase + " = '" + clsDatabaseFunctions.FormatSQLStatement(m_stimecarddatabase) + "'"
		+ ", " + SMTablesmoptions.sorderdocsftpurl + " = '" + clsDatabaseFunctions.FormatSQLStatement(m_sftporderdocsurl) + "'"
		+ ", " + SMTablesmoptions.sbiddocsftpurl + " = '" + clsDatabaseFunctions.FormatSQLStatement(m_sftpbiddocsurl) + "'"
		+ ", " + SMTablesmoptions.sbackgroundcolor + " = '" + clsDatabaseFunctions.FormatSQLStatement(m_sbackgroundcolor) + "'"
		+ ", " + SMTablesmoptions.ssmtpusername + " = '" + clsDatabaseFunctions.FormatSQLStatement(m_ssmtpusername) + "'"
		+ ", " + SMTablesmoptions.ssmtppassword + " = '" + clsDatabaseFunctions.FormatSQLStatement(m_ssmtppassword) + "'"
		+ ", " + SMTablesmoptions.ssmtpreplytoname + " = '" + clsDatabaseFunctions.FormatSQLStatement(m_ssmtpreplytoname) + "'"
		+ ", " + SMTablesmoptions.iusesauthentication + " = " + m_iusesauthentication
		+ ", " + SMTablesmoptions.sinvoicelogofilename + " = '" + clsDatabaseFunctions.FormatSQLStatement(m_sinvoicelogofilename) + "'"
		+ ", " + SMTablesmoptions.sproposallogofilename + " = '" + clsDatabaseFunctions.FormatSQLStatement(m_sproposallogofilename) + "'"
		+ ", " + SMTablesmoptions.sftpexporturl + " = '" + clsDatabaseFunctions.FormatSQLStatement(m_sftpexporturl) + "'"
		+ ", " + SMTablesmoptions.sftpexportuser + " = '" + clsDatabaseFunctions.FormatSQLStatement(m_sftpexportuser) + "'"
		+ ", " + SMTablesmoptions.sftpexportpw + " = '" + clsDatabaseFunctions.FormatSQLStatement(m_sftpexportpw) + "'"
		+ ", " + SMTablesmoptions.sftpfileexportpath + " = '" + clsDatabaseFunctions.FormatSQLStatement(m_sftpfileexportpath) + "'"
		//+ ", " + SMTablesmoptions.sbankname + " = '" + SMUtilities.FormatSQLStatement(m_sbankname) + "'"
		//+ ", " + SMTablesmoptions.sbankrecglacct + " = '" + SMUtilities.FormatSQLStatement(m_sbankrecglacct) + "'"
		+ ", " + SMTablesmoptions.icreatebankrecexport + " = " + m_screatebankrecexport
		+ ", " + SMTablesmoptions.gdriveorderparentfolderid + " = '" + clsDatabaseFunctions.FormatSQLStatement(m_sgdriveorderparentfolderid) + "'"
		+ ", " + SMTablesmoptions.gdriveorderfolderprefix + " = '" + clsDatabaseFunctions.FormatSQLStatement(m_sgdriveorderfolderprefix) + "'"
		+ ", " + SMTablesmoptions.gdriveorderfoldersuffix + " = '" + clsDatabaseFunctions.FormatSQLStatement(m_sgdriveorderfoldersuffix) + "'"
		+ ", " + SMTablesmoptions.gdrivesalesleadparentfolderid + " = '" + clsDatabaseFunctions.FormatSQLStatement(m_sgdrivesalesleadparentfolderid) + "'"
		+ ", " + SMTablesmoptions.gdrivesalesleadfolderprefix + " = '" + clsDatabaseFunctions.FormatSQLStatement(m_sgdrivesalesleadfolderprefix) + "'"
		+ ", " + SMTablesmoptions.gdrivesalesleadfoldersuffix + " = '" + clsDatabaseFunctions.FormatSQLStatement(m_sgdrivesalesleadfoldersuffix) + "'"
		+ ", " + SMTablesmoptions.gdriveworkorderparentfolderid + " = '" + clsDatabaseFunctions.FormatSQLStatement(m_sgdriveworkorderparentfolderid) + "'"
		+ ", " + SMTablesmoptions.gdriveworkorderfolderprefix + " = '" + clsDatabaseFunctions.FormatSQLStatement(m_sgdriveworkorderfolderprefix) + "'"
		+ ", " + SMTablesmoptions.gdriveworkorderfoldersuffix + " = '" + clsDatabaseFunctions.FormatSQLStatement(m_sgdriveworkorderfoldersuffix) + "'"
		//+ ", " + SMTablesmoptions.gdrivecreatenewfolderurl + " = '" + SMUtilities.FormatSQLStatement(m_sgdrivecreatenewfolderurl) + "'"
		+ ", " + SMTablesmoptions.gdriveuploadfileurl + " = '" + clsDatabaseFunctions.FormatSQLStatement(m_sgdriveuploadfileurl) + "'"
		+ ", " + SMTablesmoptions.icopysalesleadfolderurltoorder + " = " + clsDatabaseFunctions.FormatSQLStatement(m_icopysalesleadfolderurltoorder)
		+ ", " + SMTablesmoptions.gdriverenamefolderurl + " = '" + clsDatabaseFunctions.FormatSQLStatement(m_sgdriverenamefolderurl) + "'"
		+ ", " + SMTablesmoptions.datpostingperiodstartdate + " = '" + clsDateAndTimeConversions.stdDateStringToSQLDateString(m_sdatcurrentperiodstartdate) + "'"
		+ ", " + SMTablesmoptions.datpostingperiodenddate + " = '" + clsDateAndTimeConversions.stdDateStringToSQLDateString(m_sdatcurrentperiodenddate) + "'"
		+ ", " + SMTablesmoptions.isignatureboxwidth + " = " + clsDatabaseFunctions.FormatSQLStatement(m_isignatureboxwidth) 
		+ ", " + SMTablesmoptions.sgoogleapikey + " = '" + clsDatabaseFunctions.FormatSQLStatement(m_sgoogleapikey) + "'"
		+ ", " + SMTablesmoptions.iusegoogleplacesapi + " = " + clsDatabaseFunctions.FormatSQLStatement(m_iusegoogleplacesapi) + ""
		;		
    	try {
    		if(!clsDatabaseFunctions.executeSQL(SQL, 
    				context,
    				sConf,
    				"MySQL",
    				this.toString() + ".saveEditableFields - User: " + sUserName)){
    			throw new Exception ("Error [1457451814] - Could not update smoptions record with SQL: " + SQL + ".");
    		}
		}catch (SQLException e){
			//System.out.println("Error updating record in " + this.toString() + ": " + e.getMessage());
			throw new Exception ("Error [1439760319] updating record with SQL : '" + SQL + "' - " + e.getMessage());
		}
    }  
    
    public boolean setLastEditUserFullName (String sLastEditUserFullName){
    	
    	if (sLastEditUserFullName.equalsIgnoreCase("")){
    		m_serrormessage = "LastEditUser cannot be blank";
    		return false;
    	}
    	if (sLastEditUserFullName.length() > SMTablesmoptions.slastedituserlength){
    		m_serrormessage = "LastEditUser is too long";
    		return false;
    	}
    	
    	m_slastedituserfullname = sLastEditUserFullName;
    	return true;
    }
    
    public boolean setLastEditUserID (String sLastEditUserID){  	
    	try {
    		m_llastedituserid = Long.parseLong(sLastEditUserID);
    	}catch (Exception e) {
    		m_serrormessage = "LastEditUserID is not valid";
    		return false;
    	}
    	return true;
    }
    public boolean setLastEditProcess (String sLastEditProcess){
    	
    	if (sLastEditProcess.equalsIgnoreCase("")){
    		m_serrormessage = "LastEditUser cannot be blank";
    		return false;
    	}
    	if (sLastEditProcess.length() > SMTablesmoptions.slastedituserlength){
    		m_serrormessage = "LastEditUser is too long";
    		return false;
    	}
    	
    	m_slasteditprocess = sLastEditProcess;
    	return true;
    }
    public boolean setFileExportPath (String sFileExportPath){
    	
    	if (sFileExportPath.equalsIgnoreCase("")){
    		m_serrormessage = "File export path cannot be blank";
    		return false;
    	}
    	if (sFileExportPath.length() > SMTablesmoptions.sfileexportpathlength){
    		m_serrormessage = "File export path is too long";
    		return false;
    	}
    	
    	m_sfileexportpath = clsDatabaseFunctions.FormatSQLStatement(filterPath(sFileExportPath));
    	
    	return true;
    }
    public boolean setSMTPServer (String sSMTPServer){
    	
    	if (sSMTPServer.length() > SMTablesmoptions.ssmtpserverlength){
    		m_serrormessage = "SMTP server name is too long";
    		return false;
    	}
    	
    	m_ssmtpserver = clsDatabaseFunctions.FormatSQLStatement(sSMTPServer);
    	
    	return true;
    }
    public boolean setSMTPPort (String sSMTPPort){
    	
    	if (sSMTPPort.length() > SMTablesmoptions.ssmtpportlength){
    		m_serrormessage = "SMTP port is too long";
    		return false;
    	}
    	
    	m_ssmtpport = clsDatabaseFunctions.FormatSQLStatement(sSMTPPort);
    	
    	return true;
    }
    public boolean setSMTPSourceServerName (String sSMTPSourceServerName){
    	
    	if (sSMTPSourceServerName.length() > SMTablesmoptions.ssmtpsourceservernamelength){
    		m_serrormessage = "SMTP source server name is too long";
    		return false;
    	}
    	
    	m_ssmtpsourceservername = clsDatabaseFunctions.FormatSQLStatement(sSMTPSourceServerName);
    	
    	return true;
    }
    public boolean setSMTPUser (String sSMTPUser){
    	
    	if (sSMTPUser.length() > SMTablesmoptions.ssmtpusernamelength){
    		m_serrormessage = "SMTP username is too long";
    		return false;
    	}
    	
    	m_ssmtpusername = clsDatabaseFunctions.FormatSQLStatement(sSMTPUser);
    	
    	return true;
    }
    public boolean setSMTPPassword (String sSMTPPassword){
    	
    	if (sSMTPPassword.length() > SMTablesmoptions.ssmtppasswordlength){
    		m_serrormessage = "SMTP password is too long";
    		return false;
    	}
    	
    	m_ssmtppassword = clsDatabaseFunctions.FormatSQLStatement(sSMTPPassword);
    	
    	return true;
    }
    public boolean setSMTPReplyToAddress (String sSMTPReplyToAddress){
    	
    	if (sSMTPReplyToAddress.length() > SMTablesmoptions.ssmtpreplytonamelength){
    		m_serrormessage = "SMTP reply-to address is too long";
    		return false;
    	}
    	
    	m_ssmtpreplytoname = clsDatabaseFunctions.FormatSQLStatement(sSMTPReplyToAddress);
    	
    	return true;
    }
    public boolean setSMTPAuthentication (String sSMTPAuthentication){
    	
    	if (
    		(sSMTPAuthentication.compareToIgnoreCase("1") != 0)
    		&& (sSMTPAuthentication.compareToIgnoreCase("0") != 0)
    			
    	){
    		m_serrormessage = "SMTP authentication (" + sSMTPAuthentication + " ) is not valid";
    		return false;
    	}
    	
    	m_iusesauthentication = clsDatabaseFunctions.FormatSQLStatement(sSMTPAuthentication);
    	
    	return true;
    }
    
    public boolean setTimeCardDatabase (String sTimeCardDatabase){
    	
    	if (sTimeCardDatabase.length() > SMTablesmoptions.stimecarddatabaselength){
    		m_serrormessage = "Time card database name is too long";
    		return false;
    	}
    	
    	m_stimecarddatabase = clsDatabaseFunctions.FormatSQLStatement(sTimeCardDatabase);
    	
    	return true;
    }
    
    public boolean setOrderDocsFTPUrl (String sOrderDocsFTPUrl){
    	
    	if (sOrderDocsFTPUrl.length() > SMTablesmoptions.sorderdocsftpurllength){
    		m_serrormessage = "Order documents ftp URL is too long";
    		return false;
    	}
    	
    	m_sftporderdocsurl = clsDatabaseFunctions.FormatSQLStatement(sOrderDocsFTPUrl);
    	
    	return true;
    }
    public boolean setBidDocsFTPUrl (String sBidDocsFTPUrl){
    	
    	if (sBidDocsFTPUrl.length() > SMTablesmoptions.sbiddocsftpurllength){
    		m_serrormessage = SMBidEntry.ParamObjectName + " documents ftp URL is too long";
    		return false;
    	}
    	
    	m_sftpbiddocsurl = clsDatabaseFunctions.FormatSQLStatement(sBidDocsFTPUrl);
    	
    	return true;
    }

    public boolean setBackgroundColor (String sBackGroundColor){
    	
    	if (sBackGroundColor.length() > SMTablesmoptions.sbackgroundcolorlength){
    		m_serrormessage = "Background color is too long";
    		return false;
    	}
    	
    	@SuppressWarnings("unused")
		long lTest = 0L;
    	try {
			lTest = Long.parseLong(sBackGroundColor, 16);
		} catch (NumberFormatException e) {
			m_serrormessage = "Background color '" + sBackGroundColor 
			+ "' is invalid - it must be a valid hex number";
		}
    	
    	m_sbackgroundcolor = clsDatabaseFunctions.FormatSQLStatement(sBackGroundColor);
    	
    	return true;
    }
    
    public void setNextOrderUniquifier(long lNextOrderUniquifier){
    	m_lnextorderuniquifier = lNextOrderUniquifier;
    }
    public void setNextOrderNumber(String sNextOrderNumber) throws NumberFormatException{
    	sNextOrderNumber = sNextOrderNumber.trim();
    	long lTest = 0;
    	try {
			lTest = Long.parseLong(sNextOrderNumber);
		} catch (NumberFormatException e) {
			throw new NumberFormatException("Error setting next order number to '" + sNextOrderNumber 
				+ "' - " + e.getMessage());
		}
    	m_snextordernumber = clsStringFunctions.PadLeft(
    		Long.toString(lTest), " ", SMTableorderheaders.sOrderNumberPaddedLength);
    }
    public void setNextInvoiceNumber(String sNextInvoiceNumber){
    	m_snextinvoicenumber = sNextInvoiceNumber;
    }
    public boolean setInvoiceLogoFileName(String sInvoiceLogoFileName){
    	if (sInvoiceLogoFileName.length() > SMTablesmoptions.sinvoicelogofilenamelength){
    		m_serrormessage = "Invoice logo file name is too long";
    		return false;
    	}
    	m_sinvoicelogofilename = sInvoiceLogoFileName;
    	return true;
    }
    public boolean setProposalLogoFileName(String sProposalLogoFileName){
    	if (sProposalLogoFileName.length() > SMTablesmoptions.sproposallogofilenamelength){
    		m_serrormessage = "Proposal logo file name is too long";
    		return false;
    	}
    	m_sproposallogofilename = sProposalLogoFileName;
    	return true;
    }
    public String getDummyKey(){
    	return Integer.toString(m_idummykey);
    }
    public String getNextOrderUniquifier(){
    	return Long.toString(m_lnextorderuniquifier);
    }
    public String getNextOrderNumber(){
    	return m_snextordernumber;
    }
    public String getLastEditUserFullName(){
    	return m_slastedituserfullname;
    }
    public String getLastEditUserID(){
    	return  Long.toString(m_llastedituserid);
    }
    public String getLastEditProcess(){
    	return m_slasteditprocess;
    }
    public String getLastEditDate(){
    	return Long.toString(m_llasteditdate);
    }
    public String getLastEditTime(){
    	return Long.toString(m_llastedittime);
    }
    public String getNextInvoiceNumber(){
    	return m_snextinvoicenumber;
    }
    public String getFileExportPath(){
    	return m_sfileexportpath;
    }
    public String getSMTPServer(){
    	return m_ssmtpserver;
    }
    public String getSMTPPort(){
    	return m_ssmtpport;
    }
    public String getSMTPSourceServerName(){
    	return m_ssmtpsourceservername;
    }
    public String getSMTPUserName(){
    	return m_ssmtpusername;
    }
    public String getSMTPPassword(){
    	return m_ssmtppassword;
    }
    public String getSMTPReplyToAddress(){
    	return m_ssmtpreplytoname;
    }
    public String getSMTPAuthentication(){
    	return m_iusesauthentication;
    }
    public String getTimeCardDatabase(){
    	return m_stimecarddatabase;
    }
    public String getOrderDocsFTPUrl(){
    	return m_sftporderdocsurl;
    }
    public String getBidDocsFTPUrl(){
    	return m_sftpbiddocsurl;
    }
	public String getBackGroundColor(){
    	return m_sbackgroundcolor;
    }
    public String getInvoiceLogoFileName(){
    	return m_sinvoicelogofilename;
    }
    public String getProposalLogoFileName(){
    	return m_sproposallogofilename;
    }
    public boolean setftpexporturl(String ftpexporturl){
    	if (ftpexporturl.length() > SMTablesmoptions.sftpexporturllength){
    		m_serrormessage = "FTP Export URL maximum length is " + Integer.toString(SMTablesmoptions.sftpexporturllength) + " characters.";
    		return false;
    	}
    	m_sftpexporturl = ftpexporturl;
    	return true;
    }
    public String getftpexporturl(){
    	return m_sftpexporturl;
    }
    public boolean setftpexportuser(String ftpexportuser){
    	if (ftpexportuser.length() > SMTablesmoptions.sftpexportuserlength){
    		m_serrormessage = "FTP Export User maximum length is " + Integer.toString(SMTablesmoptions.sftpexportuserlength) + " characters.";
    		return false;
    	}
    	m_sftpexportuser = ftpexportuser;
    	return true;
    }
    public String getftpexportuser(){
    	return m_sftpexportuser;
    }
    public boolean setftpexportpw(String ftpexportpw){
    	if (ftpexportpw.length() > SMTablesmoptions.sftpexportpwlength){
    		m_serrormessage = "FTP Export Password maximum length is " + Integer.toString(SMTablesmoptions.sftpexportpwlength) + " characters.";
    		return false;
    	}
    	m_sftpexportpw = ftpexportpw;
    	return true;
    }
    public String getftpexportpw(){
    	return m_sftpexportpw;
    }
    public boolean setftpfileexportpath(String ftpfileexportpath){
    	if (ftpfileexportpath.length() > SMTablesmoptions.sftpfileexportpathlength){
    		m_serrormessage = "FTP Export Path maximum length is " + Integer.toString(SMTablesmoptions.sftpfileexportpathlength) + " characters.";
    		return false;
    	}
    	m_sftpfileexportpath = filterPath(ftpfileexportpath);
    	return true;
    }
    public String getftpfileexportpath(){
    	return m_sftpfileexportpath;
    }
   // public boolean setsbankname(String bankname){
   // 	if (bankname.length() > SMTablesmoptions.sbanknamelength){
   // 		m_serrormessage = "Bank Name maximum length is " + Integer.toString(SMTablesmoptions.sbanknamelength) + " characters.";
   // 		return false;
   // 	}
   // 	m_sbankname = bankname;
   // 	return true;
   //}
    //public String getsbankname(){
    //	return m_sbankname;
    //}
    //public boolean setsbankrecglacct(String bankrecglacct){
    //   	if (bankrecglacct.length() > SMTablesmoptions.sbankrecglacctlength){
    //		m_serrormessage = "Bank Rec GL Acct maximum length is " + Integer.toString(SMTablesmoptions.sbankrecglacctlength) + " characters.";
    //		return false;
    //	}
    //	m_sbankrecglacct = bankrecglacct;
    //	return true;
    //}
    //public String getbankrecglacct(){
    //	return m_sbankrecglacct;
    //}
    public boolean setscreatebankrecexport(String createbankrecexport){
       	if (
       			(createbankrecexport.compareToIgnoreCase("0") == 0)
       			|| (createbankrecexport.compareToIgnoreCase("1") == 0)
       		)
       	{
    	}else{
    		m_serrormessage = "'Create bank reconciliation' value '" + createbankrecexport + "' is not valid.";
    		return false;
    	}
    	m_screatebankrecexport = createbankrecexport;
    	return true;
    }
    public String getcreatebankrecexport(){
    	return m_screatebankrecexport;
    }
    
    public String getsgoogleapikey(){
    	return m_sgoogleapikey;
    }
    public void setsgoogleapikey(String sgoogleapikey){
    	m_sgoogleapikey = sgoogleapikey;
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
    public void setgdriverenamefolderurl (String sgdriverenamefolderurl){
    	m_sgdriverenamefolderurl = sgdriverenamefolderurl;
    }
    public String getscurrentperiodstartdate(){
   		return m_sdatcurrentperiodstartdate;    		
    }
    public void setscurrentperiodstartdate (String scurrentperiodstartdate){
    	m_sdatcurrentperiodstartdate = scurrentperiodstartdate;
    }
    public String getscurrentperiodenddate(){
   		return m_sdatcurrentperiodenddate;    		
    }
    public void setscurrentperiodenddate (String scurrentperiodenddate){
    	m_sdatcurrentperiodenddate = scurrentperiodenddate;
    }
    public String getisignatureboxwidth(){
   		return m_isignatureboxwidth;    		
    }
    public boolean setisignatureboxwidth (String ssignatureboxwidth){
    	try{
    		int isignatureboxwidth = Integer.parseInt(ssignatureboxwidth);
    		if(isignatureboxwidth < 1){
    			m_serrormessage = "'" + isignatureboxwidth + "' must be greater than zero for signature box size.";
    			return false;
    		}
    	}catch (Exception ex){
    		m_serrormessage = "'" + ssignatureboxwidth + "' is not a valid number for signature box size.";
    		return false;
    	}
    	m_isignatureboxwidth = ssignatureboxwidth;
		return true;
    }
    private String filterPath(String sPath){
    	String sFilteredPath = sPath;
    
    	if ((!sFilteredPath.endsWith("/")) && (!sFilteredPath.endsWith("\\"))){
    		sFilteredPath = sFilteredPath + System.getProperty("file.separator");
    	}
    	return sFilteredPath;
    }
    public String getErrorMessage(){
    	return m_serrormessage;
    }
    public void checkDateForPosting(String smmddyyyy, String sDateName, Connection conn, String sUserID) throws Exception{
    	if (!load(conn)){
    		throw new Exception ("Error [1457461530] loading SM Options to check " + sDateName + " - " + getErrorMessage());
    	}
    	
    	//If either the starting or ending date is empty, then just return - the date is valid:
    	if (
    		(getscurrentperiodstartdate().compareToIgnoreCase(EMPTY_DATE_STRING) == 0)
    		|| (getscurrentperiodenddate().compareToIgnoreCase(EMPTY_DATE_STRING) == 0)
    			
    	){
    		return;
    	}
    	
    	//System.out.println("[1514903512] - smmddyyyy = '" + smmddyyyy + "'");
    	
    	//See if the entered date is between the starting and ending dates:
		if (!clsDateAndTimeConversions.IsValidDateString("M/d/yyyy", smmddyyyy)){
			throw new Exception ("Error [1457461531] Invalid " + sDateName + ": '" + smmddyyyy +"'.");
		}
		if (
			(clsDateAndTimeConversions.stdDateStringToSQLDateString(smmddyyyy).compareToIgnoreCase(clsDateAndTimeConversions.stdDateStringToSQLDateString(getscurrentperiodstartdate())) < 0)
			|| (clsDateAndTimeConversions.stdDateStringToSQLDateString(smmddyyyy).compareToIgnoreCase(clsDateAndTimeConversions.stdDateStringToSQLDateString(getscurrentperiodenddate())) > 0)
		){
			SMLogEntry log = new SMLogEntry(conn);
			log.writeEntry(
				sUserID, 
				SMLogEntry.LOG_OPERATION_POSTINGPERIODVIOLATION, 
				sDateName, 
				"Tried to post to date " + smmddyyyy + ", but posting starting date is " + getscurrentperiodstartdate() 
					+ ", ending date is " + getscurrentperiodenddate() + ".",
				"[1457641805]"
				);
			throw new Exception(
				sDateName + " '" + smmddyyyy + "' is not in the posting period date range, which is between " 
				+ getscurrentperiodstartdate() + " and " + getscurrentperiodenddate() + ".");
		}
		return;
    }
}
