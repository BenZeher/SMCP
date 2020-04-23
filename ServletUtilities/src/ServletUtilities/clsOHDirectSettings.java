package ServletUtilities;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import SMDataDefinition.SMTableohdirectsettings;

public class clsOHDirectSettings extends clsMasterEntry{

	public static final String ParamObjectName = "OHDirect Connection Settings";


	private String m_sclientid = "";
	private String m_sclientsecret = "";
	private String m_stokenurl = "";
	private String m_stokenusername = "";
	private String m_stokenuserpassword = "";
	private String m_srequesturlbase = "";

	private boolean bDebugMode = false;

	public clsOHDirectSettings() {
		super();
		initEntryVariables();
	}

	public clsOHDirectSettings(HttpServletRequest req){
		super(req);
		initEntryVariables();
		m_sclientid = clsManageRequestParameters.get_Request_Parameter(
				SMTableohdirectsettings.sclientid, req).trim();
		m_sclientsecret = clsManageRequestParameters.get_Request_Parameter(
				SMTableohdirectsettings.sclientsecret, req).trim();
		m_stokenurl = clsManageRequestParameters.get_Request_Parameter(
				SMTableohdirectsettings.stokenurl, req).trim();
		m_stokenusername = clsManageRequestParameters.get_Request_Parameter(
				SMTableohdirectsettings.stokenusername, req).trim();
		m_stokenuserpassword = clsManageRequestParameters.get_Request_Parameter(
				SMTableohdirectsettings.stokenuserpassword, req).trim();
		m_srequesturlbase = clsManageRequestParameters.get_Request_Parameter(
				SMTableohdirectsettings.srequesturlbase, req).trim();

	}
	public void load (ServletContext context, String sDBID, String sUserID, String sUserFullName) throws Exception{
		Connection conn;
		try {
			conn = clsDatabaseFunctions.getConnectionWithException(
				context, 
				sDBID, 
				"MySQL", 
				this.toString() + ".load - user = " + sUserFullName + "."
			);
		} catch (Exception e1) {
			throw new Exception("Error [202004232636] - opening data connection to load " + ParamObjectName + ".");
		}

		try {
			load (conn);
		} catch (Exception e) {
			clsDatabaseFunctions.freeConnection(context, conn, "[1587655647]");
			throw new Exception(e.getMessage());
		}
		clsDatabaseFunctions.freeConnection(context, conn, "[1587655648]");
	}

	public boolean load (Connection conn) throws Exception{

		String SQL = "SELECT * FROM " + SMTableohdirectsettings.TableName;
		if (bDebugMode){
			System.out.println("[1587655649] In " + this.toString() + " - load SQL = " + SQL);
		}
		try {
			ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
			if (rs.next()) {
				//Load the variables here:
				m_sclientid = rs.getString(SMTableohdirectsettings.sclientid).trim();
				m_sclientsecret = rs.getString(SMTableohdirectsettings.sclientsecret).trim();
				m_stokenurl = rs.getString(SMTableohdirectsettings.stokenurl).trim();
				m_stokenusername = rs.getString(SMTableohdirectsettings.stokenusername).trim();
				m_stokenuserpassword = rs.getString(SMTableohdirectsettings.stokenuserpassword).trim();
				m_srequesturlbase = rs.getString(SMTableohdirectsettings.srequesturlbase).trim();
				rs.close();
			} else {
				rs.close();
				throw new Exception("Error [1587655650] Could not load OHDirect connection settings.");
			}
		} catch (Exception e) {
			throw new Exception("Error [1587655651] reading " + ParamObjectName + " - " + e.getMessage());
		}
		return true;
	}

	public void save_without_data_transaction (ServletContext context, String sDBID,  String sUserID, String sUserFullName) throws Exception{

		Connection conn;
		try {
			conn = clsDatabaseFunctions.getConnectionWithException(
					context, 
					sDBID, 
					"MySQL", 
					this.toString() + " - user: " + sUserID + " - "+ sUserFullName
					);
		} catch (Exception e1) {
			throw new Exception("Error [202004233316] - could not save " + ParamObjectName + " - " + e1.getMessage());
		}

		try {
			save_without_data_transaction (conn, sUserID, sUserFullName, sDBID);
		} catch (Exception e) {
			clsDatabaseFunctions.freeConnection(context, conn, "[1587655652]");
			throw new Exception(e.getMessage());
		}
		clsDatabaseFunctions.freeConnection(context, conn, "[1587655653]");

	}
	public void save_without_data_transaction (Connection conn, String sUserID, String sUserFullName, String sDBID) throws Exception{

		try {
			validate_entry_fields(conn, sDBID);
		} catch (Exception e1) {
			throw new Exception (e1.getMessage());
		}

		String SQL = "";


			SQL = " UPDATE " + SMTableohdirectsettings.TableName + " SET "
				+ SMTableohdirectsettings.sclientid + " = '" + m_sclientid + "'"
				+ ", " + SMTableohdirectsettings.sclientsecret  + " = '" + m_sclientsecret + "'"
				+ ", " + SMTableohdirectsettings.srequesturlbase  + " = '" + m_srequesturlbase + "'"
				+ ", " + SMTableohdirectsettings.stokenurl  + " = '" + m_stokenurl + "'"
				+ ", " + SMTableohdirectsettings.stokenusername  + " = '" + m_stokenusername + "'"
				+ ", " + SMTableohdirectsettings.stokenuserpassword  + " = '" + m_stokenuserpassword + "'"
			;	
				
		try{
			Statement stmt = conn.createStatement();
			stmt.executeUpdate(SQL);
		}catch (Exception ex) {
			throw new Exception ("Error [1587655654] in update with SQL: " + SQL + " - " + ex.getMessage());
		}
	}

	public void validate_entry_fields (Connection conn, String sDBID) throws Exception{
		//Validate the entries here:
		String sErrors = "";

		m_sclientid = m_sclientid.trim();
		if (m_sclientid.length() > SMTableohdirectsettings.sclientidlength){
			sErrors += "Client ID cannot be more than " + Integer.toString(SMTableohdirectsettings.sclientidlength) + " characters.  ";
		}

		m_sclientsecret = m_sclientsecret.trim();
		if (m_sclientsecret.length() > SMTableohdirectsettings.sclientsecretlength){
			sErrors += "Client secret cannot be more than " + Integer.toString(SMTableohdirectsettings.sclientsecretlength) + " characters.  ";
		}
		
		m_srequesturlbase = m_srequesturlbase.trim();
		if (m_srequesturlbase.length() > SMTableohdirectsettings.srequesturlbaselength){
			sErrors += "Request URL base cannot be more than " + Integer.toString(SMTableohdirectsettings.srequesturlbaselength) + " characters.  ";
		}
		
		m_stokenurl = m_stokenurl.trim();
		if (m_stokenurl.length() > SMTableohdirectsettings.stokenurllength){
			sErrors += "Token URL cannot be more than " + Integer.toString(SMTableohdirectsettings.stokenurllength) + " characters.  ";
		}
		
		m_stokenusername = m_stokenusername.trim();
		if (m_stokenusername.length() > SMTableohdirectsettings.stokenusernamelength){
			sErrors += "Token user name cannot be more than " + Integer.toString(SMTableohdirectsettings.stokenusernamelength) + " characters.  ";
		}
		
		m_stokenuserpassword = m_stokenuserpassword.trim();
		if (m_stokenuserpassword.length() > SMTableohdirectsettings.stokenuserpasswordlength){
			sErrors += "Token user password cannot be more than " + Integer.toString(SMTableohdirectsettings.stokenuserpasswordlength) + " characters.  ";
		}
		
		if (sErrors.compareToIgnoreCase("") != 0){
			throw new Exception(sErrors);
		}
	}

	public String getsclientid() {
		return m_sclientid;
	}
	public String getsclientsecret() {
		return m_sclientsecret;
	}
	public String getsrequesturlbase() {
		return m_srequesturlbase;
	}
	public String getstokenurl() {
		return m_stokenurl;
	}
	public String getstokenusername() {
		return m_stokenusername;
	}
	public String getstokenuserpassword() {
		return m_stokenuserpassword;
	}
	public String getObjectName(){
		return ParamObjectName;
	}

	private void initEntryVariables(){
		m_sclientid = "";
		m_sclientsecret = "";
		m_srequesturlbase = "";
		m_stokenurl = "";
		m_stokenusername = "";
		m_stokenuserpassword = "";
	}
}
