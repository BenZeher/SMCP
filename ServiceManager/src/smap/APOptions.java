package smap;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import SMDataDefinition.SMTableapoptions;
import ServletUtilities.clsServletUtilities;
import smcontrolpanel.SMUtilities;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsManageRequestParameters;

public class APOptions extends java.lang.Object{
	
	public static final String ParamObjectName = "APOptions";
	public static final String Parambatchpostinginprocess = "batchpostinginprocess";
	public static final String Parampostinguserid = "postinguserid";
	public static final String Parampostingprocess = "postingprocess";
	public static final String Parampostingstartdate = "postingstartdate";
	public static final String Paramusessmcpap = "usessmcpap";
	public static final String Paramaccpacversion = "accpacversion";
	public static final String Paramgdrivevendorsparentfolderid = "gdrivevendorsparentfolderid";
	public static final String Paramgdrivevendorsfolderprefix = "gdrivevendorsderfolderprefix";
	public static final String Paramgdrivevendorsfoldersuffix = "gdrivevendorsfoldersuffix";
	public static final String Paramaccpacdatabaseurl = "accpacdatabaseurl";
	public static final String Paramaccpacdatabasename = "accpacdatabasename";
	public static final String Paramaccpacdatabaseuser = "accpacdatabaseuser";
	public static final String Paramaccpacdatabaseuserpw = "accpacdatabaseuserpw";
	public static final String Paramaccpacdatabasetype = "accpacdatabasetype";
	public static final String Paramiexportoption = "iexportoption";
	public static final String FORM_NAME = "MAINFORM";
	
	private String m_ibatchpostinginprocess;
	private String m_lpostinguserid;
	private String m_spostingprocess;
	private String m_datpostingstartdate;
	private String m_iusessmcpap;
	private String m_iaccpacversion;
	private String m_gdrivevendorsparentfolderid;
	private String m_gdrivevendorsderfolderprefix;
	private String m_gdrivevendorsfoldersuffix;
	private String m_saccpacdatabaseurl;
	private String m_saccpacdatabasename;
	private String m_saccpacdatabaseuser;
	private String m_saccpacdatabaseuserpw;
	private String m_iaccpacdatabasetype;
	private String m_iexportoption;
	
	private ArrayList<String> m_sErrorMessageArray = new ArrayList<String> (0);
	

	public APOptions(){
		m_ibatchpostinginprocess = "0";
		m_lpostinguserid = "0";
		m_spostingprocess = "";
		m_datpostingstartdate = "0000-00-00 00:00:00";
		m_iusessmcpap = "0";
		m_iaccpacversion = "0";
		m_gdrivevendorsparentfolderid = "";
		m_gdrivevendorsderfolderprefix = "";
		m_gdrivevendorsfoldersuffix = "";
		m_saccpacdatabaseurl = "";
		m_saccpacdatabasename = "";
		m_saccpacdatabaseuser = "";
		m_saccpacdatabaseuserpw = "";
		m_iaccpacdatabasetype = "0";
		m_iexportoption = "0";
		m_sErrorMessageArray = new ArrayList<String> (0);
	}
	
	public APOptions (HttpServletRequest req){

		m_ibatchpostinginprocess = clsManageRequestParameters.get_Request_Parameter(APOptions.Parambatchpostinginprocess, req).trim();
		m_lpostinguserid = clsManageRequestParameters.get_Request_Parameter(APOptions.Parampostinguserid, req).trim();
		m_spostingprocess = clsManageRequestParameters.get_Request_Parameter(APOptions.Parampostingprocess, req).trim();
		m_datpostingstartdate = clsManageRequestParameters.get_Request_Parameter(APOptions.Parampostingstartdate, req).trim();	
		if (req.getParameter(APOptions.Paramusessmcpap) == null){
			m_iusessmcpap = "0";
		}else{
			m_iusessmcpap = "1";
		}
		m_iaccpacversion = clsManageRequestParameters.get_Request_Parameter(APOptions.Paramaccpacversion, req).trim();
		m_saccpacdatabaseurl = clsManageRequestParameters.get_Request_Parameter(APOptions.Paramaccpacdatabaseurl, req).trim();
		m_saccpacdatabasename = clsManageRequestParameters.get_Request_Parameter(APOptions.Paramaccpacdatabasename, req).trim();;
		m_saccpacdatabaseuser = clsManageRequestParameters.get_Request_Parameter(APOptions.Paramaccpacdatabaseuser, req).trim();;
		m_saccpacdatabaseuserpw = clsManageRequestParameters.get_Request_Parameter(APOptions.Paramaccpacdatabaseuserpw, req).trim();;
		m_iaccpacdatabasetype = clsManageRequestParameters.get_Request_Parameter(APOptions.Paramaccpacdatabasetype, req).trim();;
		m_gdrivevendorsparentfolderid = clsManageRequestParameters.get_Request_Parameter(APOptions.Paramgdrivevendorsparentfolderid, req).trim();
		m_gdrivevendorsderfolderprefix = clsManageRequestParameters.get_Request_Parameter(APOptions.Paramgdrivevendorsfolderprefix, req).trim();
		m_gdrivevendorsfoldersuffix = clsManageRequestParameters.get_Request_Parameter(APOptions.Paramgdrivevendorsfoldersuffix, req).trim();
		m_iexportoption = clsManageRequestParameters.get_Request_Parameter(APOptions.Paramiexportoption, req).trim();
		m_sErrorMessageArray = new ArrayList<String> (0);
	}
	public boolean checkTestingFlag(Connection conn) throws Exception{
		try {
			//This is for TESTING only:
			ResultSet rsAPTest = clsDatabaseFunctions.openResultSet("SELECT * FROM " + SMTableapoptions.TableName, conn);
			if (rsAPTest.next()){
				if (rsAPTest.getInt(SMTableapoptions.icreatetestbatchesfrompoinvoices) == 1){
					rsAPTest.close();
					return true;
				}else{
					rsAPTest.close();
					return false;
				}
			}
		} catch (SQLException e) {
			throw new Exception("Error [1507075456] reading AP 'Testing Flag' - " + e.getMessage());
		}
		return false;
	}
    public boolean load (Connection conn){
        	
    	String SQL = "SELECT * FROM " + SMTableapoptions.TableName;
        	try {
        		ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
        		if (rs.next()){
        			m_ibatchpostinginprocess = Long.toString(rs.getLong(SMTableapoptions.ibatchpostinginprocess));
        			m_lpostinguserid = rs.getString(SMTableapoptions.luserid);
        			m_spostingprocess = rs.getString(SMTableapoptions.sprocess);
        			m_datpostingstartdate = rs.getString(SMTableapoptions.datstartdate);        			
        			m_iusessmcpap = Long.toString(rs.getLong(SMTableapoptions.iusessmcpap));
        			m_iaccpacversion = Long.toString(rs.getLong(SMTableapoptions.iaccpacversion));
        			m_saccpacdatabaseurl = rs.getString(SMTableapoptions.saccpacdatabaseurl);
        			m_saccpacdatabasename = rs.getString(SMTableapoptions.saccpacdatabasename);
        			m_saccpacdatabaseuser = rs.getString(SMTableapoptions.saccpacdatabaseuser);
        			m_saccpacdatabaseuserpw = rs.getString(SMTableapoptions.saccpacdatabaseuserpw);
        			m_iaccpacdatabasetype = Long.toString(rs.getLong(SMTableapoptions.iaccpacdatabasetype));
        			m_gdrivevendorsparentfolderid = rs.getString(SMTableapoptions.gdrivevendorsparentfolderid);
        			m_gdrivevendorsderfolderprefix = rs.getString(SMTableapoptions.gdrivevendorsderfolderprefix);
        			m_gdrivevendorsfoldersuffix = rs.getString(SMTableapoptions.gdrivevendorsfoldersuffix);
        			m_iexportoption = Integer.toString(rs.getInt(SMTableapoptions.iexportto));
        			rs.close();
        			return true;
        		}else{
        		rs.close();
        		APOptions defaultAPOptions = new APOptions();
        		SQL = "INSERT INTO " + SMTableapoptions.TableName 
        			+ " (" +  SMTableapoptions.ibatchpostinginprocess 
        			+ ", " + SMTableapoptions.luserid 
        			+ ", " + SMTableapoptions.sprocess 
        			+ ", " + SMTableapoptions.datstartdate 
        			+ ", " + SMTableapoptions.iusessmcpap //5
        			+ ", " + SMTableapoptions.iaccpacversion
        			+ ", " + SMTableapoptions.iexportto
        			+ ", " + SMTableapoptions.saccpacdatabaseurl 
        			+ ", " + SMTableapoptions.saccpacdatabasename 
        			+ ", " + SMTableapoptions.saccpacdatabaseuser  //10
        			+ ", " + SMTableapoptions.saccpacdatabaseuserpw 
        			+ ", " + SMTableapoptions.iaccpacdatabasetype 
        			+ ", " + SMTableapoptions.gdrivevendorsparentfolderid 
        			+ ", " + SMTableapoptions.gdrivevendorsderfolderprefix 
       				+ ", " + SMTableapoptions.gdrivevendorsfoldersuffix   //15			
       				+ ") VALUES(" 
       				+      defaultAPOptions.m_ibatchpostinginprocess 
       				+ ", " + defaultAPOptions.m_lpostinguserid + ""
       				+ ", '" + defaultAPOptions.m_spostingprocess + "'"
       				+ ", '" + defaultAPOptions.m_datpostingstartdate + "'"
       				+ ", "  + defaultAPOptions.m_iusessmcpap  //5
       				+ ", "  + defaultAPOptions.m_iaccpacversion 
       				+ ", " + defaultAPOptions.m_iexportoption
       				+ ", '" + defaultAPOptions.m_saccpacdatabaseurl + "'"
        			+ ", '" + defaultAPOptions.m_saccpacdatabasename + "'"
        			+ ", '" + defaultAPOptions.m_saccpacdatabaseuser + "'"  //10
        			+ ", '" + defaultAPOptions.m_saccpacdatabaseuserpw + "'"
       				+ ", "  + defaultAPOptions.m_iaccpacdatabasetype 
       				+ ", '" + defaultAPOptions.m_gdrivevendorsparentfolderid + "'"
       				+ ", '" + defaultAPOptions.m_gdrivevendorsderfolderprefix + "'"
       				+ ", '" + defaultAPOptions.m_gdrivevendorsfoldersuffix + "'"//15
       				+ ")"
        			;
        		try {
        				Statement stmt = conn.createStatement();
        				stmt.executeUpdate(SQL);
        			}catch (SQLException e){
        
        				m_sErrorMessageArray.add("Error updating record with SQL: " + SQL + " - " + e.getMessage());
        				return false;
        				}
        			return true;
        		}
        	}catch (SQLException e){
        		m_sErrorMessageArray.add("SQL Error reading AP options record: " + e.getMessage());
        		return false;
        	}
    	}
    
    public void load(String sDBID, ServletContext context, String sUserID) throws Exception{
    	Connection conn;
    	try {
    		conn = clsDatabaseFunctions.getConnectionWithException(context, 
    			   sDBID, 
    			   "MySQL",
    			   SMUtilities.getFullClassName(this.toString()) + ".load - userID: " + sUserID);
    	} catch (Exception e) {
    		throw new Exception("Error getting connection to load APOptions - " + e.getMessage());
    	}
    	if (conn == null){
    		throw new Exception("Counld not get connection to load APOptions.");
    	}
    	if (!load(conn)){
    		clsDatabaseFunctions.freeConnection(context, conn, "[1547059459]");
    		throw new Exception("Error loading APOptions - " + getErrorMessages());
    	}
    	clsDatabaseFunctions.freeConnection(context, conn, "[1547059460]");
    }
    

    public boolean saveEditableFields(ServletContext context, String sDBID, String sUserID, String sUserFullName){
    	
    	//Only re-sets the fields that can be edited from the 'Edit Options' screen:
		String SQL = "UPDATE " + SMTableapoptions.TableName
		+ " SET "
			+ SMTableapoptions.iaccpacversion + " = " +  clsDatabaseFunctions.FormatSQLStatement(m_iaccpacversion)
		+ ", " + SMTableapoptions.iusessmcpap + " = " + clsDatabaseFunctions.FormatSQLStatement(m_iusessmcpap)
		+ ", " + SMTableapoptions.gdrivevendorsparentfolderid + " = '" + clsDatabaseFunctions.FormatSQLStatement(m_gdrivevendorsparentfolderid) + "'"
		+ ", " + SMTableapoptions.gdrivevendorsderfolderprefix + " = '" + clsDatabaseFunctions.FormatSQLStatement(m_gdrivevendorsderfolderprefix) + "'"
		+ ", " + SMTableapoptions.gdrivevendorsfoldersuffix + " = '" + clsDatabaseFunctions.FormatSQLStatement(m_gdrivevendorsfoldersuffix) + "'"
		+ ", " + SMTableapoptions.saccpacdatabaseurl + " = '" + clsDatabaseFunctions.FormatSQLStatement(m_saccpacdatabaseurl) + "'"
		+ ", " + SMTableapoptions.saccpacdatabasename + " = '" + clsDatabaseFunctions.FormatSQLStatement(m_saccpacdatabasename) + "'"
		+ ", " + SMTableapoptions.saccpacdatabaseuser + " = '" + clsDatabaseFunctions.FormatSQLStatement(m_saccpacdatabaseuser) + "'"
		+ ", " + SMTableapoptions.saccpacdatabaseuserpw + " = '" + clsDatabaseFunctions.FormatSQLStatement(m_saccpacdatabaseuserpw) + "'"
		+ ", " + SMTableapoptions.iaccpacdatabasetype + " = " + clsDatabaseFunctions.FormatSQLStatement(m_iaccpacdatabasetype) 
		+ ", " + SMTableapoptions.iexportto + " = " + clsDatabaseFunctions.FormatSQLStatement(m_iexportoption)
		;
		
		Connection conn = clsDatabaseFunctions.getConnection(
			context, 
			sDBID, 
			"MySQL", 
			SMUtilities.getFullClassName(this.toString()) 
			+ ":saveEditableFields - user: " 
			+ sUserID
			+ " - "
			+ sUserFullName
		);
		if (conn == null){
			m_sErrorMessageArray.add("Error getting data connection.");
			return false;
		}
    	try {
    		Statement stmt = conn.createStatement();
		    stmt.executeUpdate(SQL);
		}catch (SQLException e){
			clsDatabaseFunctions.freeConnection(context, conn, "[1547059461]");
			m_sErrorMessageArray.add("Error updating record with SQL: " + SQL + " - " + e.getMessage());
			return false;
		}
		
		clsDatabaseFunctions.freeConnection(context, conn, "[1547059462]");
		
		return true;
    }
    public void savePostingProcessState(
    	Connection conn,
    	String sPostingProcessState,
    	String sPostingProcess,
    	String sPostingUserID
    	) throws Exception{
    	
    	//Only re-sets the fields that can be edited from the 'Edit Options' screen:
    	
    	String sPostingStartTime = "";
    	//If we are SETTING the posting state, then we need to store the starting posting time:
    	if (sPostingProcessState.compareToIgnoreCase("1") == 0){
    		sPostingStartTime = "NOW()";
    	}else{
    		sPostingStartTime = "'0000-00-00 00:00:00'";
    	}
    	
		String SQL = "UPDATE " + SMTableapoptions.TableName
		+ " SET "
			+ SMTableapoptions.ibatchpostinginprocess + " = " +  sPostingProcessState
		+ ", " + SMTableapoptions.sprocess + " = '" + sPostingProcess + "'"
		+ ", " + SMTableapoptions.luserid + " = " + sPostingUserID + ""
		+ ", " + SMTableapoptions.datstartdate + " = " + sPostingStartTime
		;
		
    	try {
    		Statement stmt = conn.createStatement();
		    stmt.executeUpdate(SQL);
		}catch (SQLException e){
			throw new Exception("Error [1507579547] AP posting state with SQL: " + SQL + " - " + e.getMessage());
		}
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
	
	public String getBatchPostingInProcess() {
		return m_ibatchpostinginprocess;
	}

	public void setBatchPostingInProcess(String sBatchPostingInProcess) {
		m_ibatchpostinginprocess = sBatchPostingInProcess;
	}

	public String getPostingUserID() {
		return m_lpostinguserid;
	}

	public void setPostingUserID(String sPostingUserID) {
		m_lpostinguserid = sPostingUserID;
	}

	public String getPostingProcess() {
		return m_spostingprocess;
	}

	public void setPostingProcess(String sPostingProcess) {
		m_spostingprocess = sPostingProcess;
	}

	public String getPostingStartDate() {
		return m_datpostingstartdate;
	}

	public void setPostingStartDate(String sPostingStartDate) {
		this.m_datpostingstartdate = sPostingStartDate;
	}

	public String getUsesSMCPAP() {
		return m_iusessmcpap;
	}
	
	public void setUsesSMCPAP(String iUsesSMCPAP) {
		m_iusessmcpap = iUsesSMCPAP;
	}

	public void setACCPACversion(String ACCPACversion) {
		m_iaccpacversion = ACCPACversion;
	}
	
	public String getACCPACversion() {
		return m_iaccpacversion;
	}

	public void setACCPACDatabaseURL(String accpacdatabaseurl) {
		m_saccpacdatabaseurl = accpacdatabaseurl;
	}
	
	public String getACCPACDatabaseURL() {
		return m_saccpacdatabaseurl;
	}
	
	public void setACCPACDatabaseName(String accpacdatabasename) {
		m_saccpacdatabasename = accpacdatabasename;
	}
	
	public String getACCPACDatabaseName() {
		return m_saccpacdatabasename;
	}

	public void setACCPACDatabaseUser(String accpacdatabaseuser) {
		m_saccpacdatabaseuser = accpacdatabaseuser;
	}
	
	public String getACCPACDatabaseUser() {
		return m_saccpacdatabaseuser;
	}
	
	public void setACCPACDatabaseUserPW(String accpacdatabaseuserpw) {
		m_saccpacdatabaseuserpw = accpacdatabaseuserpw;
	}
	
	public String getACCPACDatabaseUserPW() {
		return m_saccpacdatabaseuserpw;
	}
	
	
	public void setACCPACDatabasetype(String accpacdatabasetype) {
		m_iaccpacdatabasetype = accpacdatabasetype;
	}
	
	public String getACCPACDatabaseType() {
		return m_iaccpacdatabasetype;
	}
	
	 public String getgdrivevendorparentfolderid(){
	    return m_gdrivevendorsparentfolderid;
	}
	 public void setgdrivevendorparentfolderid(String sgdrivevendorparentfolderid){
		 m_gdrivevendorsparentfolderid = sgdrivevendorparentfolderid;
	}
	 public String getgdrivevendorfolderprefix(){
	    return m_gdrivevendorsderfolderprefix;
	}
	public void setgdrivecvendorfolderprefix(String sgdrivevendorsfolderprefix){
		m_gdrivevendorsderfolderprefix = sgdrivevendorsfolderprefix;
	}
	public String getgdrivevendorfoldersuffix(){
	    return m_gdrivevendorsfoldersuffix;
	}
	public void setgdrivevendorfoldersuffix(String sgdrivevendorfoldersuffix){
		m_gdrivevendorsfoldersuffix = sgdrivevendorfoldersuffix;
	}
	public String getiexportoption(){
	    return m_iexportoption;
	}
	public void setiexportoption(String siexportoption){
		m_iexportoption = siexportoption;
	}
	
	public String getObjectName(){
	    return ParamObjectName;
	}

	public String getQueryString(){
	
		String sQueryString = "";
		sQueryString += Parambatchpostinginprocess + "=" + clsServletUtilities.URLEncode(m_ibatchpostinginprocess);
		sQueryString += "&" + Parampostinguserid + "=" + clsServletUtilities.URLEncode(m_lpostinguserid);
		sQueryString += "&" + Parampostingprocess + "=" + clsServletUtilities.URLEncode(m_spostingprocess);
		sQueryString += "&" + Parampostingstartdate + "=" + clsServletUtilities.URLEncode(m_datpostingstartdate);
		sQueryString += "&" + Paramgdrivevendorsparentfolderid + "=" + clsServletUtilities.URLEncode(m_gdrivevendorsparentfolderid);
		sQueryString += "&" + Paramgdrivevendorsfolderprefix + "=" + clsServletUtilities.URLEncode(m_gdrivevendorsderfolderprefix);
		sQueryString += "&" + Paramgdrivevendorsfoldersuffix + "=" + clsServletUtilities.URLEncode(m_gdrivevendorsfoldersuffix);
		sQueryString += "&" + Paramiexportoption + "=" + clsServletUtilities.URLEncode(m_iexportoption);
		return sQueryString;
	}

}