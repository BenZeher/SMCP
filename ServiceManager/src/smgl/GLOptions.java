package smgl;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import SMDataDefinition.SMTablegloptions;
import ServletUtilities.clsServletUtilities;
import smcontrolpanel.SMUtilities;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsManageRequestParameters;

public class GLOptions extends java.lang.Object{
	
	public static final String ParamObjectName = "GLOptions";
	public static final String Parambatchpostinginprocess = "batchpostinginprocess";
	public static final String Parampostinguserfullname = "postinguserfullname";
	public static final String Parampostinguserid = "postinguserid";
	public static final String Parampostingprocess = "postingprocess";
	public static final String Parampostingstartdate = "postingstartdate";
	public static final String Paramusessmcpgl = "usessmcpgl";
	public static final String Paramaccpacversion = "accpacversion";
	public static final String Paramaccpacdatabaseurl = "accpacdatabaseurl";
	public static final String Paramaccpacdatabasename = "accpacdatabasename";
	public static final String Paramaccpacdatabaseuser = "accpacdatabaseuser";
	public static final String Paramaccpacdatabaseuserpw = "accpacdatabaseuserpw";
	public static final String Paramaccpacdatabasetype = "accpacdatabasetype";
	public static final String Paramsclosingaccount = "sclosingaccount";
	public static final String FORM_NAME = "MAINFORM";
	
	private String m_ibatchpostinginprocess;
	private String m_spostinguserfullname;
	private String m_lpostinguserid;
	private String m_spostingprocess;
	private String m_datpostingstartdate;
	private String m_iusessmcpgl;
	private String m_iaccpacversion;
	private String m_saccpacdatabaseurl;
	private String m_saccpacdatabasename;
	private String m_saccpacdatabaseuser;
	private String m_saccpacdatabaseuserpw;
	private String m_iaccpacdatabasetype;
	private String m_sclosingaccount;
	
	private ArrayList<String> m_sErrorMessageArray = new ArrayList<String> (0);
	

	public GLOptions(){
		m_ibatchpostinginprocess = "0";
		m_spostinguserfullname = "";
		m_lpostinguserid = "0";
		m_spostingprocess = "";
		m_datpostingstartdate = "0000-00-00 00:00:00";
		m_iusessmcpgl = "0";
		m_iaccpacversion = "0";
		m_saccpacdatabaseurl = "";
		m_saccpacdatabasename = "";
		m_saccpacdatabaseuser = "";
		m_saccpacdatabaseuserpw = "";
		m_iaccpacdatabasetype = "0";
		m_sclosingaccount = "";
		m_sErrorMessageArray = new ArrayList<String> (0);
	}
	
	public GLOptions (HttpServletRequest req){

		m_ibatchpostinginprocess = clsManageRequestParameters.get_Request_Parameter(GLOptions.Parambatchpostinginprocess, req).trim();
		m_spostinguserfullname = clsManageRequestParameters.get_Request_Parameter(GLOptions.Parampostinguserfullname, req).trim();
		m_lpostinguserid = clsManageRequestParameters.get_Request_Parameter(GLOptions.Parampostinguserid, req).trim();
		m_spostingprocess = clsManageRequestParameters.get_Request_Parameter(GLOptions.Parampostingprocess, req).trim();
		m_datpostingstartdate = clsManageRequestParameters.get_Request_Parameter(GLOptions.Parampostingstartdate, req).trim();	
		if (req.getParameter(GLOptions.Paramusessmcpgl) == null){
			m_iusessmcpgl = "0";
		}else{
			m_iusessmcpgl = "1";
		}
		m_iaccpacversion = clsManageRequestParameters.get_Request_Parameter(GLOptions.Paramaccpacversion, req).trim();
		m_saccpacdatabaseurl = clsManageRequestParameters.get_Request_Parameter(GLOptions.Paramaccpacdatabaseurl, req).trim();
		m_saccpacdatabasename = clsManageRequestParameters.get_Request_Parameter(GLOptions.Paramaccpacdatabasename, req).trim();
		m_saccpacdatabaseuser = clsManageRequestParameters.get_Request_Parameter(GLOptions.Paramaccpacdatabaseuser, req).trim();
		m_saccpacdatabaseuserpw = clsManageRequestParameters.get_Request_Parameter(GLOptions.Paramaccpacdatabaseuserpw, req).trim();
		m_sclosingaccount = clsManageRequestParameters.get_Request_Parameter(GLOptions.Paramsclosingaccount, req).trim();
		m_iaccpacdatabasetype = clsManageRequestParameters.get_Request_Parameter(GLOptions.Paramaccpacdatabasetype, req).trim();
		m_sErrorMessageArray = new ArrayList<String> (0);
	}
	public boolean checkTestingFlag(Connection conn) throws Exception{
		try {
			//This is for TESTING only:
			ResultSet rsAPTest = clsDatabaseFunctions.openResultSet("SELECT * FROM " + SMTablegloptions.TableName, conn);
			if (rsAPTest.next()){
				if (rsAPTest.getInt(SMTablegloptions.icreatetestbatchesfromsubmodules) == 1){
					rsAPTest.close();
					return true;
				}else{
					rsAPTest.close();
					return false;
				}
			}
		} catch (SQLException e) {
			throw new Exception("Error [1522772561] reading AP 'Testing Flag' - " + e.getMessage());
		}
		return false;
	}
    public boolean load (Connection conn){
        	
    	String SQL = "SELECT * FROM " + SMTablegloptions.TableName;
        	try {
        		ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
        		if (rs.next()){
        			m_ibatchpostinginprocess = Long.toString(rs.getLong(SMTablegloptions.ibatchpostinginprocess));
        			m_spostinguserfullname = rs.getString(SMTablegloptions.suserfullname);
        			m_lpostinguserid = Long.toString(rs.getLong(SMTablegloptions.luserid));
        			m_spostingprocess = rs.getString(SMTablegloptions.sprocess);
        			m_datpostingstartdate = rs.getString(SMTablegloptions.datstartdate);        			
        			m_iusessmcpgl = Long.toString(rs.getLong(SMTablegloptions.iusessmcpgl));
        			m_iaccpacversion = Long.toString(rs.getLong(SMTablegloptions.iaccpacversion));
        			m_saccpacdatabaseurl = rs.getString(SMTablegloptions.saccpacdatabaseurl);
        			m_saccpacdatabasename = rs.getString(SMTablegloptions.saccpacdatabasename);
        			m_saccpacdatabaseuser = rs.getString(SMTablegloptions.saccpacdatabaseuser);
        			m_saccpacdatabaseuserpw = rs.getString(SMTablegloptions.saccpacdatabaseuserpw);
        			m_iaccpacdatabasetype = Long.toString(rs.getLong(SMTablegloptions.iaccpacdatabasetype));
        			m_sclosingaccount = rs.getString(SMTablegloptions.sclosingaccount);
        			rs.close();
        			return true;
        		}else{
        		rs.close();
        		GLOptions defaultGLOptions = new GLOptions();
        		SQL = "INSERT INTO " + SMTablegloptions.TableName 
        			+ " (" +  SMTablegloptions.ibatchpostinginprocess 
        			+ ", " + SMTablegloptions.suserfullname 
        			+ ", " + SMTablegloptions.luserid
        			+ ", " + SMTablegloptions.sprocess 
        			+ ", " + SMTablegloptions.datstartdate //5
        			+ ", " + SMTablegloptions.iusessmcpgl 
        			+ ", " + SMTablegloptions.iaccpacversion
        			+ ", " + SMTablegloptions.saccpacdatabaseurl 
        			+ ", " + SMTablegloptions.saccpacdatabasename //10
        			+ ", " + SMTablegloptions.saccpacdatabaseuser  
        			+ ", " + SMTablegloptions.saccpacdatabaseuserpw 
        			+ ", " + SMTablegloptions.iaccpacdatabasetype 
        			+ ", " + SMTablegloptions.sclosingaccount
       				+ ") VALUES(" 
       				+      defaultGLOptions.m_ibatchpostinginprocess 
       				+ ", '" + clsDatabaseFunctions.FormatSQLStatement(defaultGLOptions.m_spostinguserfullname) + "'"
       				+ ", " + defaultGLOptions.m_lpostinguserid + ""
       				+ ", '" + defaultGLOptions.m_spostingprocess + "'"
       				+ ", '" + defaultGLOptions.m_datpostingstartdate + "'"//5
       				+ ", "  + defaultGLOptions.m_iusessmcpgl  
       				+ ", "  + defaultGLOptions.m_iaccpacversion 
       				+ ", '" + defaultGLOptions.m_saccpacdatabaseurl + "'"
        			+ ", '" + defaultGLOptions.m_saccpacdatabasename + "'" //10
        			+ ", '" + defaultGLOptions.m_saccpacdatabaseuser + "'"  
        			+ ", '" + defaultGLOptions.m_saccpacdatabaseuserpw + "'"
       				+ ", "  + defaultGLOptions.m_iaccpacdatabasetype
       				+ ", '" + defaultGLOptions.m_sclosingaccount
       				+ ")"
        			;
        		try {
        				Statement stmt = conn.createStatement();
        				stmt.executeUpdate(SQL);
        			}catch (SQLException e){
        
        				m_sErrorMessageArray.add("Error [1522772562] updating record with SQL: " + SQL + " - " + e.getMessage());
        				return false;
        				}
        			return true;
        		}
        	}catch (SQLException e){
        		m_sErrorMessageArray.add("SQL Error []1522772563] reading AP options record: " + e.getMessage());
        		return false;
        	}
    	}
    
    public void load(String sConf, ServletContext context, String sUserID) throws Exception{
    	Connection conn;
    	try {
    		conn = clsDatabaseFunctions.getConnectionWithException(context, 
    			   sConf, 
    			   "MySQL",
    			   SMUtilities.getFullClassName(this.toString()) + ".load - userID: " + sUserID);
    	} catch (Exception e) {
    		throw new Exception("Error getting connection to load GLOptions - " + e.getMessage());
    	}
    	if (conn == null){
    		throw new Exception("Counld not get connection to load GLOptions.");
    	}
    	if (!load(conn)){
    		clsDatabaseFunctions.freeConnection(context, conn, "[1547080755]");
    		throw new Exception("Error loading GLOptions - " + getErrorMessages());
    	}
    	clsDatabaseFunctions.freeConnection(context, conn, "[1547080756]");
    }
    

    public boolean saveEditableFields(ServletContext context, String sConf, String sUserID, String sUserFullName){
    	
    	//Only re-sets the fields that can be edited from the 'Edit Options' screen:
		String SQL = "UPDATE " + SMTablegloptions.TableName
		+ " SET "
			+ SMTablegloptions.iaccpacversion + " = " +  clsDatabaseFunctions.FormatSQLStatement(m_iaccpacversion)
		+ ", " + SMTablegloptions.iusessmcpgl + " = " + clsDatabaseFunctions.FormatSQLStatement(m_iusessmcpgl)
		+ ", " + SMTablegloptions.saccpacdatabaseurl + " = '" + clsDatabaseFunctions.FormatSQLStatement(m_saccpacdatabaseurl) + "'"
		+ ", " + SMTablegloptions.saccpacdatabasename + " = '" + clsDatabaseFunctions.FormatSQLStatement(m_saccpacdatabasename) + "'"
		+ ", " + SMTablegloptions.saccpacdatabaseuser + " = '" + clsDatabaseFunctions.FormatSQLStatement(m_saccpacdatabaseuser) + "'"
		+ ", " + SMTablegloptions.saccpacdatabaseuserpw + " = '" + clsDatabaseFunctions.FormatSQLStatement(m_saccpacdatabaseuserpw) + "'"
		+ ", " + SMTablegloptions.iaccpacdatabasetype + " = " + clsDatabaseFunctions.FormatSQLStatement(m_iaccpacdatabasetype) 
		+ ", " + SMTablegloptions.sclosingaccount + " = " + clsDatabaseFunctions.FormatSQLStatement(m_sclosingaccount)
		;
		
		Connection conn = clsDatabaseFunctions.getConnection(
			context, 
			sConf, 
			"MySQL", 
			SMUtilities.getFullClassName(this.toString()) + ":saveEditableFields - user: " + sUserID + " - " + sUserFullName
		);
		if (conn == null){
			m_sErrorMessageArray.add("Error [1522772565] getting data connection.");
			return false;
		}
    	try {
    		Statement stmt = conn.createStatement();
		    stmt.executeUpdate(SQL);
		}catch (SQLException e){
			clsDatabaseFunctions.freeConnection(context, conn, "[1547080757]");
			m_sErrorMessageArray.add("Error [1522772564] updating record with SQL: " + SQL + " - " + e.getMessage());
			return false;
		}
		
		clsDatabaseFunctions.freeConnection(context, conn, "[1547080758]");
		
		return true;
    }
    public void savePostingProcessState(
    	Connection conn,
    	String sPostingProcessState,
    	String sPostingProcess,
    	String sPostingUserFullName,
    	String sUserID
    	) throws Exception{
    	
    	//Only re-sets the fields that can be edited from the 'Edit Options' screen:
    	
    	String sPostingStartTime = "";
    	//If we are SETTING the posting state, then we need to store the starting posting time:
    	if (sPostingProcessState.compareToIgnoreCase("1") == 0){
    		sPostingStartTime = "NOW()";
    	}else{
    		sPostingStartTime = "'0000-00-00 00:00:00'";
    	}
    	
		String SQL = "UPDATE " + SMTablegloptions.TableName
		+ " SET "
			+ SMTablegloptions.ibatchpostinginprocess + " = " +  sPostingProcessState
		+ ", " + SMTablegloptions.sprocess + " = '" + sPostingProcess + "'"
		+ ", " + SMTablegloptions.suserfullname + " = '" + sPostingUserFullName + "'"
		+ ", " + SMTablegloptions.luserid + " = " + sUserID + ""
		+ ", " + SMTablegloptions.datstartdate + " = " + sPostingStartTime
		;
		
    	try {
    		Statement stmt = conn.createStatement();
		    stmt.executeUpdate(SQL);
		}catch (SQLException e){
			throw new Exception("Error [1522772560] GL posting state with SQL: " + SQL + " - " + e.getMessage());
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

	public String getPostingUserFullName() {
		return m_spostinguserfullname;
	}

	public void setPostingUserFullName(String sPostingUser) {
		m_spostinguserfullname = sPostingUser;
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

	public String getUsesSMCPGL() {
		return m_iusessmcpgl;
	}
	
	public void setUsesSMCPGL(String iUsesSMCPAP) {
		m_iusessmcpgl = iUsesSMCPAP;
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

	public void setsClosingAccount(String sClosingAccount) {
		m_sclosingaccount = sClosingAccount;
	}
	
	public String getsClosingAccount() {
		return m_sclosingaccount;
	}
	
	public String getObjectName(){
	    return ParamObjectName;
	}

	public String getQueryString(){
	
		String sQueryString = "";
		sQueryString += Parambatchpostinginprocess + "=" + clsServletUtilities.URLEncode(m_ibatchpostinginprocess);
		sQueryString += "&" + Parampostinguserfullname + "=" + clsServletUtilities.URLEncode(m_spostinguserfullname);
		sQueryString += "&" + Parampostingprocess + "=" + clsServletUtilities.URLEncode(m_spostingprocess);
		sQueryString += "&" + Parampostingstartdate + "=" + clsServletUtilities.URLEncode(m_datpostingstartdate);
		sQueryString += "&" + Paramsclosingaccount + "=" + clsServletUtilities.URLEncode(m_sclosingaccount);
		return sQueryString;
	}
}