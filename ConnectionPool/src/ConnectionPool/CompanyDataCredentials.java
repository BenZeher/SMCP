package ConnectionPool;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;

public class CompanyDataCredentials extends java.lang.Object{

	//Table Name
	public static final String TableName = "companydatacredentials";

	//Field names:
	public static final String lid  = "lid";
	public static final String sdatabasename  = "sdatabasename";
	public static final String sdatabaseurl = "sdatabaseurl";
	public static final String sdatabaseport = "sdatabaseport";
	public static final String sdatabaseuser = "sdatabaseuser";
	public static final String sdatabaseuserpw = "sdatabaseuserpw";
	public static final String sdatabaseid = "sdatabaseid";
	public static final String scomment = "scomment";

	//Field lengths:
	public static final int sdatabasenameLength = 64;
	public static final int sdatabaseurlLength = 128;
	public static final int sdatabaseportLength = 6;
	public static final int sdatabaseuserLength = 32;
	public static final int sdatabaseuserpwLength = 64;
	public static final int sdatabaseidLength = 64;
	public static final int scommentLength = 254;
	
	private String m_sid;
	private String m_sdatabasename;
	private String m_sdatabaseurl;
	private String m_sdatabaseport;
	private String m_sdatabaseuser;
	private String m_sdatabaseuserpw;
	private String m_sdatabaseid;
	private String m_scomment;
	
	//private boolean bDebugMode = false;
	
	public CompanyDataCredentials(
        ) {
		m_sid = "0";
		m_sdatabasename = "";
		m_sdatabaseurl = "";;
		m_sdatabaseport = "";
		m_sdatabaseuser = "";
		m_sdatabaseuserpw = "";
		m_sdatabaseid = "";
		m_scomment = "";
    }
    public void load (
    		String sDatabaseID,
    		String sControlDatabaseURL,
    		String sControlDatabasePort,
    		String sControlDatabaseName,
    		String sControlDatabaseUser,
    		String sControlDatabasePw
    		) throws Exception{
        
		String sSQL = "SELECT"
			+ " * FROM " + TableName
			+ " WHERE ("
				+ "(" + sdatabaseid + " = '" + sDatabaseID + "')"
			+ ")"
		;
		
		Connection conn = null;
		String sConnectionString = 
			"jdbc:mysql://" 
				+ sControlDatabaseURL 
				+ ":" + sControlDatabasePort + "/" 
				+ sControlDatabaseName 
				+ "?noDatetimeStringSync=true&connectTimeout=28800000&interactiveClient=True"
		;
		try {
			conn = DriverManager.getConnection(
				sConnectionString, 
				sControlDatabaseUser, 
				sControlDatabasePw);
		}catch (Exception E) { 
			try{
				Class.forName("com.mysql.jdbc.Driver").newInstance();
				conn = DriverManager.getConnection(
						sConnectionString, 
						sControlDatabaseUser, 
						sControlDatabasePw);
			}catch(Exception F){
				throw new Exception("Could not get connection with connection string '" + sConnectionString + "' - " + F.getMessage());
			}
		}
		
		try {
			ResultSet rs = PoolUtilities.openResultSet(sSQL, conn); 
			if(rs.next()){
				m_sid = Long.toString(rs.getLong(lid));
				m_sdatabaseurl = rs.getString(sdatabaseurl);
				m_sdatabaseport = rs.getString(sdatabaseport);
				m_sdatabasename = rs.getString(sdatabasename);
				m_sdatabaseuser = rs.getString(sdatabaseuser);
				m_sdatabaseuserpw = rs.getString(sdatabaseuserpw);
				//m_sdatabaseid = rs.getString(sdatabaseid);
				m_scomment = rs.getString(scomment);
				rs.close();
			}else{
				rs.close();
				conn.close();
				throw new Exception("Error [1483722084] No company data credentials found for database ID: '" + sDatabaseID + "'.");
			}
		}catch (SQLException ex){
			conn.close();
			throw new Exception("Error [1483722085] reading company data credentials - " + ex.getMessage() + ".");
		}
		conn.close();
    }
    
    public void load (
    		String sDatabaseID,
    		Connection conn //This is a connection to the CONTROL database (smcpcontrols)
    		) throws Exception{
        
		String sSQL = "SELECT"
			+ " * FROM " + TableName
			+ " WHERE ("
				+ "(" + sdatabaseid + " = '" + sDatabaseID + "')"
			+ ")"
		;
		
		try {
			ResultSet rs = PoolUtilities.openResultSet(sSQL, conn); 
			if(rs.next()){
				m_sid = Long.toString(rs.getLong(lid));
				m_sdatabaseurl = rs.getString(sdatabaseurl);
				m_sdatabaseport = rs.getString(sdatabaseport);
				m_sdatabasename = rs.getString(sdatabasename);
				m_sdatabaseuser = rs.getString(sdatabaseuser);
				m_sdatabaseuserpw = rs.getString(sdatabaseuserpw);
				//m_sdatabaseid = rs.getString(sdatabaseid);
				m_scomment = rs.getString(scomment);
				rs.close();
			}else{
				rs.close();
				throw new Exception("Error [1483722474] No company data credentials found for database ID: '" + sDatabaseID + "'.");
			}
		}catch (SQLException ex){
			throw new Exception("Error [1483722475] reading company data credentials - " + ex.getMessage() + ".");
		}
    }

	public String get_sid() {
		return m_sid;
	}
	public void set_sid(String sID) {
		m_sid = sID;
	}
	public String get_databasename() {
		return m_sdatabasename;
	}
	public void set_databasename(String sDatabaseName) {
		m_sdatabasename = sDatabaseName;
	}
	public String get_databaseurl() {
		return m_sdatabaseurl;
	}
	public void set_databaseurl(String sDatabaseUrl) {
		m_sdatabaseurl = sDatabaseUrl;
	}
	public String get_databaseport() {
		return m_sdatabaseport;
	}
	public void set_databaseport(String sDatabasePort) {
		m_sdatabaseport = sDatabasePort;
	}
	public String get_databaseuser() {
		return m_sdatabaseuser;
	}
	public void set_databaseuser(String sDatabaseUser) {
		m_sdatabaseuser = sDatabaseUser;
	}
	public String get_databaseuserpw() {
		return m_sdatabaseuserpw;
	}
	public void set_databaseuserpw(String sDatabaseUserPw) {
		m_sdatabaseuserpw = sDatabaseUserPw;
	}
	public String get_databaseid() {
		return m_sdatabaseid;
	}
	public void set_databaseid(String sDatabaseID) {
		m_sdatabaseid = sDatabaseID;
	}
	public String get_commentcomment() {
		return m_scomment;
	}
	public void set_comment(String sComment) {
		m_scomment = sComment;
	}
}