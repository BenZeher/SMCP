package smbk;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import smgl.GLAccount;
import SMDataDefinition.SMTableapchecks;
import SMDataDefinition.SMTablebkbanks;
import SMDataDefinition.SMTablebkstatements;
import SMDataDefinition.SMTableusers;
import ServletUtilities.clsMasterEntry;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsDateAndTimeConversions;
import ServletUtilities.clsManageBigDecimals;
import ServletUtilities.clsManageRequestParameters;
import smcontrolpanel.SMMasterEditSelect;

public class BKBank extends clsMasterEntry{
	
	public static final String ParamObjectName = "Bank";
	
	public static final String Paramlid = "lid";
	public static final String Paramsshortname = "shortname";
	public static final String Paramsbankname = "sbankname";
	public static final String Paramsaccountname = "saccountname";
	public static final String Paramsaccountnumber = "saccountnumber";
	public static final String Paramsroutingnumber = "sroutingnumber";
	public static final String Paramdattimelastmaintained = "dattimelastmaintained"; 
	public static final String Paramllastmaintainedbyid = "llastmaintainedbyid";
	public static final String Paramslastmaintainedbyfullname = "slastmaintainedbyfullname";
	public static final String Paramiactive = "iactive";
	public static final String Parambdrecentbalance = "bdrecentbalance";
	public static final String Paramdatrecentbalancedate = "datrecentbalancedate";
	public static final String Paramsglaccount = "sglaccount";
	public static final String Paramsaddressline1 = "saddressline1";
	public static final String Paramsaddressline2 = "saddressline2";
	public static final String Paramsaddressline3 = "saddressline3";
	public static final String Paramsaddressline4 = "saddressline4";
	public static final String Paramscity = "scity";
	public static final String Paramsstate = "sstate";
	public static final String Paramscountry = "scountry";
	public static final String Paramspostalcode = "spostalcode";
	public static final String Paramicheckformid = "icheckformid";
	public static final String Paramlnextchecknumber = "lnextchecknumber";
	
	
	private String m_slid;
	private String m_sshortname;
	private String m_sbankname;
	private String m_saccountname;
	private String m_saccountnumber;
	private String m_sroutingnumber;
	private String m_sdattimelastmaintained;
	private String m_llastmaintainedbyid;
	private String m_slastmaintainedbyfullname;
	private String m_sactive;
	private String m_srecentbalance;
	private String m_srecentbalancedate;
	private String m_sglaccount;
	private String m_saddressline1;
	private String m_saddressline2;
	private String m_saddressline3;
	private String m_saddressline4;
	private String m_scity;
	private String m_sstate;
	private String m_scountry;
	private String m_spostalcode;
	private String m_scheckformid;
	private String m_snextchecknumber;
	private String m_sNewRecord;
	private boolean bDebugMode = false;
	
    public BKBank() {
		super();
		initBankVariables();
        }
    
    BKBank(HttpServletRequest req){
		super(req);
		initBankVariables();
		m_slid = clsManageRequestParameters.get_Request_Parameter(
				BKBank.Paramlid, req).trim();
		m_sshortname = clsManageRequestParameters.get_Request_Parameter(
				BKBank.Paramsshortname, req).trim().toUpperCase().replace("&quot;", "\"");
		m_sbankname  = clsManageRequestParameters.get_Request_Parameter(
				BKBank.Paramsbankname, req).trim().replace("&quot;", "\"");
		m_saccountname = clsManageRequestParameters.get_Request_Parameter(
				BKBank.Paramsaccountname, req).trim().replace("&quot;", "\"");
		m_saccountnumber = clsManageRequestParameters.get_Request_Parameter(
				BKBank.Paramsaccountnumber, req).trim().replace("&quot;", "\"");
		m_sroutingnumber = clsManageRequestParameters.get_Request_Parameter(
				BKBank.Paramsroutingnumber, req).trim().replace("&quot;", "\"");
		m_sdattimelastmaintained = clsManageRequestParameters.get_Request_Parameter(
				BKBank.Paramdattimelastmaintained, req).trim().replace("&quot;", "\"");
		if(m_sdattimelastmaintained.compareToIgnoreCase("") == 0){
			m_sdattimelastmaintained = EMPTY_DATETIME_STRING;
		}
		m_llastmaintainedbyid = clsManageRequestParameters.get_Request_Parameter(
				BKBank.Paramllastmaintainedbyid, req).trim().replace("&quot;", "\"");
		m_slastmaintainedbyfullname = clsManageRequestParameters.get_Request_Parameter(
				BKBank.Paramslastmaintainedbyfullname, req).trim().replace("&quot;", "\"");
		if(req.getParameter(BKBank.Paramiactive) == null){
			m_sactive = "0";
		}else{
			m_sactive = "1";
		}
		m_srecentbalance = clsManageRequestParameters.get_Request_Parameter(
				BKBank.Parambdrecentbalance, req).trim().replace("&quot;", "\"");
		m_srecentbalancedate = clsManageRequestParameters.get_Request_Parameter(
				BKBank.Paramdatrecentbalancedate, req).trim().replace("&quot;", "\"");
		if(m_srecentbalancedate.compareToIgnoreCase("") == 0){
			m_srecentbalancedate = EMPTY_DATE_STRING;
		}
		m_sglaccount = clsManageRequestParameters.get_Request_Parameter(
				BKBank.Paramsglaccount, req).trim().replace("&quot;", "\"");
		m_saddressline1 = clsManageRequestParameters.get_Request_Parameter(
				BKBank.Paramsaddressline1, req).trim().replace("&quot;", "\"");
		m_saddressline2 = clsManageRequestParameters.get_Request_Parameter(
				BKBank.Paramsaddressline2, req).trim().replace("&quot;", "\"");
		m_saddressline3 = clsManageRequestParameters.get_Request_Parameter(
				BKBank.Paramsaddressline3, req).trim().replace("&quot;", "\"");
		m_saddressline4 = clsManageRequestParameters.get_Request_Parameter(
				BKBank.Paramsaddressline4, req).trim().replace("&quot;", "\"");
		m_scity = clsManageRequestParameters.get_Request_Parameter(
				BKBank.Paramscity, req).trim().replace("&quot;", "\"");
		m_sstate = clsManageRequestParameters.get_Request_Parameter(
				BKBank.Paramsstate, req).trim().replace("&quot;", "\"");
		m_scountry = clsManageRequestParameters.get_Request_Parameter(
				BKBank.Paramscountry, req).trim().replace("&quot;", "\"");
		m_spostalcode = clsManageRequestParameters.get_Request_Parameter(
				BKBank.Paramspostalcode, req).trim().replace("&quot;", "\"");
		m_scheckformid = clsManageRequestParameters.get_Request_Parameter(
				BKBank.Paramicheckformid, req).trim().replace("&quot;", "\"");
		m_snextchecknumber = clsManageRequestParameters.get_Request_Parameter(
				BKBank.Paramlnextchecknumber, req).trim().replace("&quot;", "\"");
		

		m_sNewRecord = clsManageRequestParameters.get_Request_Parameter(SMMasterEditSelect.SUBMIT_ADD_BUTTON_NAME, req).trim().replace("&quot;", "\"");
    }
    public void load (ServletContext context, String sDBIB, String sUserID, String sUserFullName) throws Exception{
    	Connection conn = clsDatabaseFunctions.getConnection(
    			context, 
    			sDBIB, 
    			"MySQL", 
    			this.toString() + " - user: " 
    			+ sUserID
    			+ " - "
    			+ sUserFullName
    			);
    	
    	if (conn == null){
    		throw new Exception("Error opening data connection to load bank.");
    	}
    	
    	try {
			load (conn);
		} catch (Exception e) {
			clsDatabaseFunctions.freeConnection(context, conn, "[1547060543]");
			throw new Exception(e.getMessage());
		}
    	clsDatabaseFunctions.freeConnection(context, conn, "[1547060544]");
    }
    public boolean load (Connection conn) throws Exception{
    	return load (m_slid, conn);
    }

    public void incrementnextchecknumber(Connection conn, String sUserID) throws Exception{

    	//This class has to have already been loaded for this to work:
    	if (
    		(getslid().compareToIgnoreCase("") == 0)
    		|| (getslid().compareToIgnoreCase("-1") == 0)
    	){
    		throw new Exception("Error [1504888018] - the bank record is not loaded.");
    	}
    	
    	//This function is used when the next check number is issued to a check, so the system has to figure out what the next check number should be.
    	//Because there may be checks already in the data with a higher number, we have to look for the next 'gap' in the check numbers:
    	
    	//Start by testing the 'next check number' stored with the bank:
    	long lNextCheckNumberCandidate = 0L;
    	try {
			lNextCheckNumberCandidate = Long.parseLong(getsnextchecknumber().trim()) + 1;
		} catch (Exception e) {
			throw new Exception("Error [1504887766] parsing next check number '" + getsnextchecknumber() + "' - " + e.getMessage());
		}
    	
    	//Starting with the 'next check number', walk up through all the EXISTING check numbers in the 'apchecks' table, and get the next gap in numbers
    	//available so we can assign that next available as the real 'next check number':
    	String SQL = "SELECT"
    		+ " CAST(" + SMTableapchecks.schecknumber + " AS UNSIGNED) AS CHECKNUMBER"
    		+ " FROM " + SMTableapchecks.TableName
    		+ " WHERE ("
    			+ "(CAST(" + SMTableapchecks.schecknumber + " AS UNSIGNED) >= " + getsnextchecknumber().trim() + ")"
    			+ " AND (" + SMTableapchecks.iprinted + " = 1)"
    		+ ")"
    		+ " ORDER BY CAST(" + SMTableapchecks.schecknumber + " AS UNSIGNED)"
    	;
    	try {
			ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
			while (rs.next()){
				//If the next apcheck record has a check number that is NOT equal to our 'next check number candidate', then
				// break, because we've found the next available check number:
				if (rs.getLong("CHECKNUMBER") != lNextCheckNumberCandidate){
					break;
				//But if our candidate is already in the apchecks table, then increment it and loop again to try the next number:
				}else{
					//Increment the next available check number, and then test the next check number
					lNextCheckNumberCandidate++;
				}
			}
		} catch (SQLException e) {
			throw new Exception("Error [1504887765] reading AP check numbers - " + e.getMessage());
		}
    	
    	//At this point, whether we found a 'gap' to use in the AP check numbers - OR - just got to the end of the check numbers, we should have our next available check number:
    	setsnextchecknumber(Long.toString(lNextCheckNumberCandidate));
    	try {
			save_without_data_transaction(conn, sUserID);
		} catch (Exception e) {
			throw new Exception("Error [1504887775] updating next check number for bank - " + e.getMessage());
		}
    }
    private boolean load (String sID, Connection conn) throws Exception{

    	sID = sID.trim();
    	if (sID.compareToIgnoreCase("") == 0){
    		throw new Exception("ID code cannot be blank when loading bank.");
    	}
		
		String SQL = "SELECT * FROM " + SMTablebkbanks.TableName
			+ " WHERE ("
				+ SMTablebkbanks.lid + " = " + sID
			+ ")";
		if (bDebugMode){
			System.out.println("[1579184768] In " + this.toString() + " - load SQL = " + SQL);
		}
		try {
			ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
			if (rs.next()) {
				//Load the variables here:
				m_slid = Long.toString(rs.getLong(SMTablebkbanks.lid));
				m_sshortname = rs.getString(SMTablebkbanks.sshortname).trim();
				m_sbankname = rs.getString(SMTablebkbanks.sbankname).trim();
				m_saccountname = rs.getString(SMTablebkbanks.saccountname).trim();
				m_saccountnumber = rs.getString(SMTablebkbanks.saccountnumber).trim();
				m_sdattimelastmaintained = clsDateAndTimeConversions.resultsetDateTimeStringToString(
						rs.getString(SMTablebkbanks.dattimelastmaintained));
				m_llastmaintainedbyid = rs.getString(SMTablebkbanks.llastmaintainedbyid).trim();
				m_slastmaintainedbyfullname = rs.getString(SMTablebkbanks.slastmaintainedbyfullname).trim();
				m_sactive = Long.toString(rs.getLong(SMTablebkbanks.iactive));
				m_srecentbalance = clsManageBigDecimals.BigDecimalToScaledFormattedString(
			    		SMTablebkbanks.bdrecentbalancescale, rs.getBigDecimal(SMTablebkbanks.bdrecentbalance));
				m_srecentbalancedate = clsDateAndTimeConversions.resultsetDateStringToString(
						rs.getString(SMTablebkbanks.datrecentbalancedate));
				m_sglaccount = rs.getString(SMTablebkbanks.sglaccount).trim();
				m_sroutingnumber = rs.getString(SMTablebkbanks.sroutingnumber).trim();
				m_saddressline1 = rs.getString(SMTablebkbanks.saddressline1).trim();
				m_saddressline2 = rs.getString(SMTablebkbanks.saddressline2).trim();
				m_saddressline3 = rs.getString(SMTablebkbanks.saddressline3).trim();
				m_saddressline4 = rs.getString(SMTablebkbanks.saddressline4).trim();
				m_scity = rs.getString(SMTablebkbanks.scity).trim();
				m_sstate = rs.getString(SMTablebkbanks.sstate).trim();
				m_scountry = rs.getString(SMTablebkbanks.scountry).trim();
				m_spostalcode = rs.getString(SMTablebkbanks.spostalcode).trim();
				m_scheckformid = Long.toString(rs.getLong(SMTablebkbanks.icheckformid));
				m_snextchecknumber = Long.toString(rs.getLong(SMTablebkbanks.lnextchecknumber));
				rs.close();
			} else {
				rs.close();
				return false;
			}
		} catch (Exception e) {
			throw new Exception("Error [1400590535] reading " + ParamObjectName + " for lid : '" + sID
					+ "' - " + e.getMessage());
		}
		return true;
    }
    
    public void save_without_data_transaction (ServletContext context, String sDBIB, String sUserID, String sUserFullName) throws Exception{
    	
       	Connection conn = clsDatabaseFunctions.getConnection(
    			context, 
    			sDBIB, 
    			"MySQL", 
    			this.toString() + " - user: " 
    			+ sUserID
    			+ " - "
    			+ sUserFullName
    			);
    	
    	if (conn == null){
    		throw new Exception("Error [1400595337] opening data connection.");
    	}
    	
    	try {
			save_without_data_transaction (conn, sUserID);
		} catch (Exception e) {
			clsDatabaseFunctions.freeConnection(context, conn, "[1547060545]");
			throw new Exception(e.getMessage());
		}
    	clsDatabaseFunctions.freeConnection(context, conn, "[1547060546]");
    	
    }
    public void save_without_data_transaction (Connection conn, String sUserID) throws Exception{

    	try {
			validate_entry_fields(conn);
		} catch (Exception e1) {
			throw new Exception (e1.getMessage());
		}
    	String SQL = "";
    	String sLastMaintainedByFullName = "N/A";
    	SQL = "SELECT"
    		+ " " + SMTableusers.sUserFirstName
    		+ ", " + SMTableusers.sUserLastName
    		+ " FROM " + SMTableusers.TableName
    		+ " WHERE ("
    			+ "(" + SMTableusers.lid + " = " + sUserID + ")"
    		+ ")"
    	;
    	try {
			ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
			if (rs.next()){
				sLastMaintainedByFullName = rs.getString(SMTableusers.sUserFirstName).trim() 
						+ " " + rs.getString(SMTableusers.sUserLastName).trim();
			}
			rs.close();
		} catch (Exception e) {
			throw new Exception("Error [1400594371] reading user full name to save bank record - " + e.getMessage());
		}
    	setllastmaintainedbyid(sUserID);
    	setslastmaintainedbyfullname(sLastMaintainedByFullName);
		//If it's a new record, do an insert:
		SQL = "INSERT INTO " + SMTablebkbanks.TableName + " ("
			+ SMTablebkbanks.bdrecentbalance
			+ ", " + SMTablebkbanks.datrecentbalancedate
			+ ", " + SMTablebkbanks.dattimelastmaintained
			+ ", " + SMTablebkbanks.iactive
			+ ", " + SMTablebkbanks.icheckformid
			+ ", " + SMTablebkbanks.lnextchecknumber
			+ ", " + SMTablebkbanks.saccountname
			+ ", " + SMTablebkbanks.saccountnumber
			+ ", " + SMTablebkbanks.sbankname
			+ ", " + SMTablebkbanks.sglaccount
			+ ", " + SMTablebkbanks.llastmaintainedbyid
			+ ", " + SMTablebkbanks.slastmaintainedbyfullname
			+ ", " + SMTablebkbanks.sroutingnumber
			+ ", " + SMTablebkbanks.sshortname
			+ ", " + SMTablebkbanks.saddressline1
			+ ", " + SMTablebkbanks.saddressline2
			+ ", " + SMTablebkbanks.saddressline3
			+ ", " + SMTablebkbanks.saddressline4
			+ ", " + SMTablebkbanks.scity
			+ ", " + SMTablebkbanks.sstate
			+ ", " + SMTablebkbanks.scountry
			+ ", " + SMTablebkbanks.spostalcode
			+ ") VALUES ("
			+ m_srecentbalance.replace(",", "")
			+ ", '" + clsDateAndTimeConversions.stdDateStringToSQLDateString(getsrecentbalancedate()) + "'"
			+ ", NOW()"
			+ ", " + getsactive()
			+ ", " + getscheckformid()
			+ ", " + getsnextchecknumber()
			+ ", '" + clsDatabaseFunctions.FormatSQLStatement(getsaccountname().trim()) + "'"
			+ ", '" + clsDatabaseFunctions.FormatSQLStatement(getsaccountnumber().trim()) + "'"
			+ ", '" + clsDatabaseFunctions.FormatSQLStatement(getsbankname().trim()) + "'"
			+ ", '" + clsDatabaseFunctions.FormatSQLStatement(getsglaccount().trim()) + "'"
			+ ", " + clsDatabaseFunctions.FormatSQLStatement(getllastmaintainedbyid().trim()) + ""
			+ ", '" + clsDatabaseFunctions.FormatSQLStatement(getslastmaintainedbyfullname().trim()) + "'"
			+ ", '" + clsDatabaseFunctions.FormatSQLStatement(getsroutingnumber().trim()) + "'"
			+ ", '" + clsDatabaseFunctions.FormatSQLStatement(getsshortname().trim()) + "'"
			+ ", '" + clsDatabaseFunctions.FormatSQLStatement(getsaddressline1().trim()) + "'"
			+ ", '" + clsDatabaseFunctions.FormatSQLStatement(getsaddressline2().trim()) + "'"
			+ ", '" + clsDatabaseFunctions.FormatSQLStatement(getsaddressline3().trim()) + "'"
			+ ", '" + clsDatabaseFunctions.FormatSQLStatement(getsaddressline4().trim()) + "'"
			+ ", '" + clsDatabaseFunctions.FormatSQLStatement(getscity().trim()) + "'"
			+ ", '" + clsDatabaseFunctions.FormatSQLStatement(getsstate().trim()) + "'"
			+ ", '" + clsDatabaseFunctions.FormatSQLStatement(getscountry().trim()) + "'"
			+ ", '" + clsDatabaseFunctions.FormatSQLStatement(getspostalcode().trim()) + "'"
			+ ")"
			+ " ON DUPLICATE KEY UPDATE"
			+ " " + SMTablebkbanks.bdrecentbalance + " = " + m_srecentbalance.replace(",", "")
			+ ", " + SMTablebkbanks.datrecentbalancedate + " = '" + clsDateAndTimeConversions.stdDateStringToSQLDateString(getsrecentbalancedate()) + "'"
			+ ", " + SMTablebkbanks.dattimelastmaintained + " = NOW()" 
			+ ", " + SMTablebkbanks.iactive + " = " + getsactive()
			+ ", " + SMTablebkbanks.icheckformid + " = " + getscheckformid()
			+ ", " + SMTablebkbanks.lnextchecknumber + " = " + getsnextchecknumber()
			+ ", " + SMTablebkbanks.saccountname + " = '" + clsDatabaseFunctions.FormatSQLStatement(getsaccountname().trim()) + "'"
			+ ", " + SMTablebkbanks.saccountnumber + " = '" + clsDatabaseFunctions.FormatSQLStatement(getsaccountnumber().trim()) + "'"
			+ ", " + SMTablebkbanks.sbankname + " = '" + clsDatabaseFunctions.FormatSQLStatement(getsbankname().trim()) + "'"
			+ ", " + SMTablebkbanks.sglaccount + " = '" + clsDatabaseFunctions.FormatSQLStatement(getsglaccount().trim()) + "'"
			+ ", " + SMTablebkbanks.llastmaintainedbyid + " = " + clsDatabaseFunctions.FormatSQLStatement(getllastmaintainedbyid().trim()) + ""
			+ ", " + SMTablebkbanks.slastmaintainedbyfullname + " = '" + clsDatabaseFunctions.FormatSQLStatement(getslastmaintainedbyfullname().trim()) + "'"
			+ ", " + SMTablebkbanks.sroutingnumber + " = '" + clsDatabaseFunctions.FormatSQLStatement(getsroutingnumber().trim()) + "'"
			+ ", " + SMTablebkbanks.sshortname + " = '" + clsDatabaseFunctions.FormatSQLStatement(getsshortname().trim()) + "'"
			+ ", " + SMTablebkbanks.saddressline1 + " = '" + clsDatabaseFunctions.FormatSQLStatement(getsaddressline1().trim()) + "'"
			+ ", " + SMTablebkbanks.saddressline2 + " = '" + clsDatabaseFunctions.FormatSQLStatement(getsaddressline2().trim()) + "'"
			+ ", " + SMTablebkbanks.saddressline3 + " = '" + clsDatabaseFunctions.FormatSQLStatement(getsaddressline3().trim()) + "'"
			+ ", " + SMTablebkbanks.saddressline4 + " = '" + clsDatabaseFunctions.FormatSQLStatement(getsaddressline4().trim()) + "'"
			+ ", " + SMTablebkbanks.scity + " = '" + clsDatabaseFunctions.FormatSQLStatement(getscity().trim()) + "'"
			+ ", " + SMTablebkbanks.sstate + " = '" + clsDatabaseFunctions.FormatSQLStatement(getsstate().trim()) + "'"
			+ ", " + SMTablebkbanks.scountry + " = '" + clsDatabaseFunctions.FormatSQLStatement(getscountry().trim()) + "'"
			+ ", " + SMTablebkbanks.spostalcode + " = '" + clsDatabaseFunctions.FormatSQLStatement(getspostalcode().trim()) + "'"
		;

		if (bDebugMode){
			System.out.println("[1579184774] In " + this.toString() + " - save SQL = " + SQL);
		}
		try{
		    Statement stmt = conn.createStatement();
		    stmt.executeUpdate(SQL);
		}catch (Exception ex) {
			throw new Exception ("Error [1400595406] in insert/update with SQL: " + SQL + " - " + ex.getMessage());
		}
		//Update the ID if it's an insert:
		if (m_slid.compareToIgnoreCase("-1") == 0){
			SQL = "SELECT last_insert_id()";
			try {
				ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
				if (rs.next()) {
					m_slid = Long.toString(rs.getLong(1));
				}else {
					m_slid = "0";
				}
				rs.close();
			} catch (SQLException e) {
				throw new Exception("Could not get last ID number - " + e.getMessage());
			}
			//If something went wrong, we can't get the last ID:
			if (m_slid.compareToIgnoreCase("0") == 0){
				throw new Exception("Could not get last ID number.");
			}
		}
    }

    public void delete (ServletContext context, String sDBIB, String sUserID, String sUserFullName) throws Exception{
    	
    	Connection conn = clsDatabaseFunctions.getConnection(
    			context, 
    			sDBIB, 
    			"MySQL", 
    			this.toString() + " - user: " 
    			+ sUserID
    			+ " - "
    			+ sUserFullName
    			);
    	
    	if (conn == null){
    		throw new Exception ("Error [1400595654] opening data connection.");
    	}
    	
    	try {
			delete (conn);
		} catch (Exception e) {
			clsDatabaseFunctions.freeConnection(context, conn, "[1547060541]");
			throw new Exception(e.getMessage());
		}
    	clsDatabaseFunctions.freeConnection(context, conn, "[1547060542]");
    }
    public void delete (Connection conn) throws Exception{
    	
    	//Validate deletions
    	String SQL = "";
    	//TODO
    	//If there are any unposted bank statements, we can't delete it:
    	SQL = "SELECT "
    		+ " " + SMTablebkstatements.lbankid
    		+ " FROM " + SMTablebkstatements.TableName
    		+ " WHERE ("
    			+ "(" + SMTablebkstatements.lbankid + " = " + getslid() + ")"
    			+ " AND (" + SMTablebkstatements.iposted + " = 0)"
    		+ ")"
    		;
    	try {
			ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
			if (rs.next()) {
				rs.close();
				throw new Exception("Cannot delete because there are some unposted statements for this bank.");
			}else{
				rs.close();
			}
		} catch (SQLException e) {
			throw new Exception("Error [1416603101] reading bank statements to delete bank - " + e.getMessage());
		}
    	//Delete bank:
    	if (!clsDatabaseFunctions.start_data_transaction(conn)){
    		throw new Exception("Error [1400595580] - Could not start transaction when deleting bank.");
    	}
    	SQL = "DELETE FROM " + SMTablebkbanks.TableName
    		+ " WHERE ("
    			+ SMTablebkbanks.lid + " = " + this.getslid()
    		+ ")"
    		;
		try{
		    Statement stmt = conn.createStatement();
		    stmt.executeUpdate(SQL);
		}catch (Exception ex) {
			clsDatabaseFunctions.rollback_data_transaction(conn);
			throw new Exception("Error [1400595652] - Could not delete bank with ID " + getslid() + " with SQL: " + SQL + " - " + ex.getMessage());
		}
		
		if (!clsDatabaseFunctions.commit_data_transaction(conn)){
			clsDatabaseFunctions.rollback_data_transaction(conn);
			throw new Exception("Error [1400595653] - Could not commit data transaction while deleting bank.");
		}
		//Empty the values:
		initBankVariables();
    }

    public void validate_entry_fields (Connection conn) throws Exception{
        //Validate the entries here:
    	String sErrors = "";
    	m_slid = m_slid.trim();
    	if (m_slid.compareToIgnoreCase("") == 0){
    		m_slid = "-1";
    	}
    	try {
			@SuppressWarnings("unused")
			long lID = Long.parseLong(m_slid);
		} catch (Exception e) {
			throw new Exception("Invalid ID: '" + m_slid + "'.");
		}
    	m_sshortname = m_sshortname.trim();
        if (m_sshortname.length() > SMTablebkbanks.sshortnamelength){
        	sErrors += "Short name cannot be more than " + Integer.toString(SMTablebkbanks.sshortnamelength) + " characters.  ";
        }
        if (m_sshortname.compareToIgnoreCase("") == 0){
        	sErrors += "Short name cannot be blank.  ";
        }
        m_sbankname = m_sbankname.trim();
        if (m_sbankname.length() > SMTablebkbanks.sbanknamelength){
        	sErrors += "Bank name cannot be more than " + Integer.toString(SMTablebkbanks.sbanknamelength) + " characters.  ";
        }
        if (m_sbankname.compareToIgnoreCase("") == 0){
        	sErrors += "Bank name cannot be blank.  ";
        }
        m_saccountname = m_saccountname.trim();
        if (m_saccountname.length() > SMTablebkbanks.saccountnamelength){
        	sErrors += "Account name cannot be more than " + Integer.toString(SMTablebkbanks.saccountnamelength) + " characters.  ";
        }
        m_saccountnumber = m_saccountnumber.trim();
        if (m_saccountnumber.length() > SMTablebkbanks.saccountnumberlength){
        	sErrors += "Account number cannot be more than " + Integer.toString(SMTablebkbanks.saccountnumberlength) + " characters.  ";
        }
        m_sroutingnumber = m_sroutingnumber.trim();
        if (m_sroutingnumber.length() > SMTablebkbanks.sroutingnumberlength){
        	sErrors += "Routing number cannot be more than " + Integer.toString(SMTablebkbanks.sroutingnumberlength) + " characters.  ";
        }
        
        m_saddressline1 = m_saddressline1.trim();
        if (m_saddressline1.length() > SMTablebkbanks.saddressline1length){
        	sErrors += "Address line 1 cannot be more than " + Integer.toString(SMTablebkbanks.saddressline1length) + " characters.  ";
        }
        
        m_saddressline2 = m_saddressline2.trim();
        if (m_saddressline2.length() > SMTablebkbanks.saddressline2length){
        	sErrors += "Address line 2 cannot be more than " + Integer.toString(SMTablebkbanks.saddressline2length) + " characters.  ";
        }
        
        m_saddressline3 = m_saddressline3.trim();
        if (m_saddressline3.length() > SMTablebkbanks.saddressline3length){
        	sErrors += "Address line 3 cannot be more than " + Integer.toString(SMTablebkbanks.saddressline3length) + " characters.  ";
        }
        
        m_saddressline4 = m_saddressline4.trim();
        if (m_saddressline4.length() > SMTablebkbanks.saddressline4length){
        	sErrors += "Address line 4 cannot be more than " + Integer.toString(SMTablebkbanks.saddressline4length) + " characters.  ";
        }
        
        m_scity = m_scity.trim();
        if (m_scity.length() > SMTablebkbanks.scitylength){
        	sErrors += "City cannot be more than " + Integer.toString(SMTablebkbanks.scitylength) + " characters.  ";
        }
        
        m_sstate = m_sstate.trim();
        if (m_sstate.length() > SMTablebkbanks.sstatelength){
        	sErrors += "State cannot be more than " + Integer.toString(SMTablebkbanks.sstatelength) + " characters.  ";
        }
        
        m_scountry = m_scountry.trim();
        if (m_scountry.length() > SMTablebkbanks.scountrylength){
        	sErrors += "Country cannot be more than " + Integer.toString(SMTablebkbanks.scountrylength) + " characters.  ";
        }
        
        m_spostalcode = m_spostalcode.trim();
        if (m_spostalcode.length() > SMTablebkbanks.spostalcodelength){
        	sErrors += "Postal code cannot be more than " + Integer.toString(SMTablebkbanks.spostalcodelength) + " characters.  ";
        }
        
        m_sdattimelastmaintained = m_sdattimelastmaintained.trim();
        if (m_sdattimelastmaintained.compareToIgnoreCase("") == 0){
        	m_sdattimelastmaintained = EMPTY_DATETIME_STRING;
        }
        if (m_sdattimelastmaintained.compareToIgnoreCase(EMPTY_DATETIME_STRING) != 0){
        	if (!clsDateAndTimeConversions.IsValidDateString("M/d/yyyy hh:ss a", m_sdattimelastmaintained)){
        		sErrors += "Date last maintained is invalid: '" + m_sdattimelastmaintained + "'.";
        	}
        }
        m_llastmaintainedbyid = m_llastmaintainedbyid.trim();
        if (m_llastmaintainedbyid.length() > SMTablebkbanks.llastmaintainedbyidlength){
        	sErrors += "Last maintained by ID cannot be more than " + Integer.toString(SMTablebkbanks.llastmaintainedbyidlength) + " characters.  ";
        }
        m_slastmaintainedbyfullname = m_slastmaintainedbyfullname.trim();
        if (m_slastmaintainedbyfullname.length() > SMTablebkbanks.slastmaintainedbyfullnamelength){
        	sErrors += "Last maintained by full name cannot be more than " + Integer.toString(SMTablebkbanks.slastmaintainedbyfullnamelength) + " characters.  ";
        }
        if (
        		(m_sactive.compareToIgnoreCase("0") != 0)
        		&& (m_sactive.compareToIgnoreCase("1") != 0)
        ){
        	sErrors += "'Active' status (" + m_sactive + ") is invalid.";
        }
        m_srecentbalance = m_srecentbalance.replace(",", "");
        if (m_srecentbalance.compareToIgnoreCase("") == 0){
        	m_srecentbalance = clsManageBigDecimals.BigDecimalToScaledFormattedString(
        		SMTablebkbanks.bdrecentbalancescale, BigDecimal.ZERO);
        }
		BigDecimal bdRecentBalance = new BigDecimal(0);
        try{
        	bdRecentBalance = new BigDecimal(m_srecentbalance);
            if (bdRecentBalance.compareTo(BigDecimal.ZERO) < 0){
            	sErrors += "Recent balance must be a positive number: " + m_srecentbalance + ".  ";
            }else{
            	m_srecentbalance = clsManageBigDecimals.BigDecimalToScaledFormattedString(
            			SMTablebkbanks.bdrecentbalancescale, bdRecentBalance);
            }
        }catch(NumberFormatException e){
    		sErrors += "Invalid recent balance: '" + m_srecentbalance + "'.  ";
        }
        m_srecentbalancedate = m_srecentbalancedate.trim();
        if (m_srecentbalancedate.compareToIgnoreCase("") == 0){
        	m_srecentbalancedate = EMPTY_DATE_STRING;
        }
        if (m_srecentbalancedate.compareToIgnoreCase(EMPTY_DATE_STRING) != 0){
        	if (!clsDateAndTimeConversions.IsValidDateString("M/d/yyy", m_srecentbalancedate)){
        		sErrors += "Recent balance date is invalid: '" + m_srecentbalancedate + "'.";
        	}
        }
        
        m_sglaccount = m_sglaccount.trim();
        if (m_sglaccount.compareToIgnoreCase("") == 0){
        	sErrors += "GL Account cannot be blank.";
        }
        if (m_sglaccount.length() > SMTablebkbanks.sglaccountlength){
        	sErrors += "GL Account is too long - only " + Integer.toString(SMTablebkbanks.sglaccountlength) + " characters are allowed.";
        }
        //Validate that it's a real GL account:
        GLAccount gl = new GLAccount(m_sglaccount);
        if (!gl.load(conn)){
        	sErrors += "GL Account '" + m_sglaccount + "' is invalid.";
        }
        
        //Validate the check form ID:
        
        //We'll allow the bank to carry an invalid check form ID here, and when people try to print checks, it will let them know
        // at THAT point that the bank needs a valid check ID:
        m_scheckformid = m_scheckformid.trim();
        if (m_scheckformid.compareToIgnoreCase("") == 0){
        	m_scheckformid = "0";
        }
        try {
			@SuppressWarnings("unused")
			long lTestCheckFormID = Long.parseLong(m_scheckformid);
		} catch (Exception e) {
			sErrors += "Check form ID '" + m_scheckformid + "' is invalid.";
		}
        
        //if (bCheckFormIDIsValid){
        //	String SQL = "SELECT"
        //		+ " " + SMTableapcheckforms.lid
        //		+ " FROM " + SMTableapcheckforms.TableName
        //		+ " WHERE ("
        //			+ " (" + SMTableapcheckforms.lid + " = " + m_scheckformid + ")"
        //		+ ")"
        //	;
        //	try {
		//		ResultSet rs = SMUtilities.openResultSet(SQL, conn);
		//		if (!rs.next()){
		//			sErrors += "Check form ID '" + m_scheckformid + "' was not found.";
		//		}
		//		rs.close();
		//	} catch (Exception e) {
		//		sErrors += "Error [1504022409] reading check form IDs with SQL '" + SQL + " - " + e.getMessage();
		//	}
        //}
        
        //Next check number:
        boolean bNextCheckNumberIsValid = true; 
        m_snextchecknumber = m_snextchecknumber.trim();
        long lTestNextCheckNumber = 0L;
        try {
			lTestNextCheckNumber = Long.parseLong(m_snextchecknumber);
		} catch (Exception e) {
			bNextCheckNumberIsValid = false;
			sErrors += "Next check number '" + m_snextchecknumber + "' is invalid.";
		}
        if (bNextCheckNumberIsValid){
        	String SQL = "SELECT"
        		+ " " + SMTableapchecks.schecknumber
        		+ " FROM " + SMTableapchecks.TableName
        		+ " WHERE ("
        			+ " (" + SMTableapchecks.lbankid + " = " + getslid() + ")"
        			+ " AND (" + SMTableapchecks.iprinted + " = 1)"
        		+ ") ORDER BY " + SMTableapchecks.schecknumber + " DESC LIMIT 1"
        	;
        	try {
				ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
				if (rs.next()){
					long lLastUsedCheckNumber = Long.parseLong(rs.getString(SMTableapchecks.schecknumber));
					if (lLastUsedCheckNumber >= lTestNextCheckNumber){
						sErrors += "The next check number (" + m_snextchecknumber + ") is invalid because there is already a check in the system for this bank with an equal or higher check number (" 
							+ rs.getString(SMTableapchecks.schecknumber) + ").";
					}
				}
				rs.close();
			} catch (Exception e) {
				sErrors += "Error [1504022410] reading last used check number with SQL '" + SQL + " - " + e.getMessage();
			}
        }
        
        if (sErrors.compareToIgnoreCase("") != 0){
        	throw new Exception(sErrors);
        }
    }

	public String getslid() {
		return m_slid;
	}

	public void setslid(String slid) {
		m_slid = slid;
	}

	public String getsshortname() {
		return m_sshortname;
	}

	public void setsshortname(String sshortname) {
		m_sshortname = sshortname;
	}

	public String getsbankname() {
		return m_sbankname;
	}

	public void setsbankname(String sbankname) {
		m_sbankname = sbankname;
	}

	public String getsaccountname() {
		return m_saccountname;
	}

	public void setsaccountname(String saccountname) {
		m_saccountname = saccountname;
	}

	public String getsaccountnumber() {
		return m_saccountnumber;
	}

	public void setsaccountnumber(String saccountnumber) {
		m_saccountnumber = saccountnumber;
	}
	
	public String getsroutingnumber() {
		return m_sroutingnumber;
	}

	public void setsroutingnumber(String sroutingnumber) {
		m_sroutingnumber = sroutingnumber;
	}

	public String getsdattimelastmaintained() {
		return m_sdattimelastmaintained;
	}

	public void setsdattimelastmaintained(String sdattimelastmaintained) {
		m_sdattimelastmaintained = sdattimelastmaintained;
	}

	public String getllastmaintainedbyid() {
		return m_llastmaintainedbyid;
	}

	public void setllastmaintainedbyid(String slastmaintainedby) {
		m_llastmaintainedbyid = slastmaintainedby;
	}

	public String getslastmaintainedbyfullname() {
		return m_slastmaintainedbyfullname;
	}

	public void setslastmaintainedbyfullname(String slastmaintainedbyfullname) {
		m_slastmaintainedbyfullname = slastmaintainedbyfullname;
	}

	public String getsactive() {
		return m_sactive;
	}

	public void setsactive(String sactive) {
		m_sactive = sactive;
	}

	public String getsrecentbalance() {
		return m_srecentbalance;
	}

	public void setsrecentbalance(String srecentbalance) {
		m_srecentbalance = srecentbalance;
	}

	public String getsrecentbalancedate() {
		return m_srecentbalancedate;
	}

	public void setsrecentbalancedate(String srecentbalancedate) {
		m_srecentbalancedate = srecentbalancedate;
	}
	
	public String getsglaccount() {
		return m_sglaccount;
	}

	public void setsglaccount(String sglaccount) {
		m_sglaccount = sglaccount;
	}

	public String getsaddressline1() {
		return m_saddressline1;
	}

	public void setsaddressline1(String saddressline1) {
		m_saddressline1 = saddressline1;
	}
	
	public String getsaddressline2() {
		return m_saddressline2;
	}

	public void setsaddressline2(String saddressline2) {
		m_saddressline2 = saddressline2;
	}
	
	public String getsaddressline3() {
		return m_saddressline3;
	}

	public void setsaddressline3(String saddressline3) {
		m_saddressline3 = saddressline3;
	}
	
	public String getsaddressline4() {
		return m_saddressline4;
	}

	public void setsaddressline4(String saddressline4) {
		m_saddressline4 = saddressline4;
	}
	
	public String getscity() {
		return m_scity;
	}

	public void setscity(String scity) {
		m_scity = scity;
	}
	
	public String getsstate() {
		return m_sstate;
	}

	public void setsstate(String sstate) {
		m_sstate = sstate;
	}
	
	public String getscountry() {
		return m_scountry;
	}

	public void setscountry(String scountry) {
		m_scountry = scountry;
	}
	
	public String getspostalcode() {
		return m_spostalcode;
	}

	public void setspostalcode(String spostalcode) {
		m_spostalcode = spostalcode;
	}
	
	public String getscheckformid(){
		return m_scheckformid;
	}
	
	public void setscheckformid(String sCheckFormID){
		m_scheckformid = sCheckFormID;
	}
	
	public String getsnextchecknumber(){
		return m_snextchecknumber;
	}
	
	public void setsnextchecknumber(String sNextCheckNumber){
		m_snextchecknumber = sNextCheckNumber;
	}	
	
	public String getsNewRecord() {
		return m_sNewRecord;
	}

	public void setsNewRecord(String mSNewRecord) {
		m_sNewRecord = mSNewRecord;
	}
	
	public String getObjectName(){
		return ParamObjectName;
	}
	
/*
	public String read_out_debug_data(){
    	String sResult = "  ** " + SMUtilities.getFullClassName(this.toString()) + " read out: ";
    	sResult += "\nTerms code: " + this.getsTermsCode();
    	sResult += "\nTerms desc: " + this.getsTermsDescription();    	
    	sResult += "\nLast maintained: " + this.getsLastMaintainedDate();
    	sResult += "\nActive: " + this.getsActive();
    	sResult += "\nDiscount percentage: " + this.getsDiscountPercentage();
    	sResult += "\nDiscount number of days: " + this.getsDiscountNumberOfDays();
    	sResult += "\nDiscount day of the month: " + this.getsDiscountDayOfTheMonth();
    	sResult += "\nDue number of days: " + this.getsDueNumberOfDays();
    	sResult += "\nDue day of the month: " + this.getsDueDayOfTheMonth();    	
    	sResult += "\nObject name: " + this.getObjectName();
    	return sResult;
    }
*/
    private void initBankVariables(){
    	m_slid = "-1";
    	m_sshortname = "";
    	m_sbankname = "";
    	m_saccountname = "";
    	m_saccountnumber = "";
    	m_sdattimelastmaintained = EMPTY_DATETIME_STRING;
    	m_llastmaintainedbyid = "0";
    	m_slastmaintainedbyfullname = "";
    	m_sactive = "1";
    	m_srecentbalance = "0.00";
    	m_srecentbalancedate = "00/00/0000";
    	m_sroutingnumber = "";
    	m_sglaccount = "";
    	m_saddressline1 = "";
    	m_saddressline2 = "";
    	m_saddressline3 = "";
    	m_saddressline4 = "";
    	m_scity = "";
    	m_sstate = "";
    	m_scountry = "";
    	m_spostalcode = "";
		m_scheckformid = "0";
		m_snextchecknumber = "0";
    	m_sNewRecord = "1";
	}
}
