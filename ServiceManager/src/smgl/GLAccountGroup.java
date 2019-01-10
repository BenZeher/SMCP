package smgl;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import SMDataDefinition.SMTableglaccountgroups;
import SMDataDefinition.SMTableglaccounts;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsManageRequestParameters;
import ServletUtilities.clsValidateFormFields;
import smcontrolpanel.SMUtilities;

public class GLAccountGroup extends java.lang.Object{
	
	public static final String ParamObjectName = "Account Group";
	
	public static final String ParamsNewRecord = "sNewRecord";
	public static final String FORM_NAME = "MAINFORM";
	
	private String m_lid;
	private String m_sdescription; 
	private String m_sgroupcode;
	private String m_ssortcode;
	
	private String m_sNewRecord;
	
	public GLAccountGroup(){
		m_lid = "0";
		m_sdescription = "";
		m_sgroupcode = "";
		m_ssortcode = "";
		m_sNewRecord = "";
	}
	
	public GLAccountGroup (HttpServletRequest req){

		m_lid = clsManageRequestParameters.get_Request_Parameter(SMTableglaccountgroups.lid, req).trim();
		m_sdescription = clsManageRequestParameters.get_Request_Parameter(SMTableglaccountgroups.sdescription, req).trim();
		m_sgroupcode = clsManageRequestParameters.get_Request_Parameter(SMTableglaccountgroups.sgroupcode, req).trim();
		m_ssortcode = clsManageRequestParameters.get_Request_Parameter(SMTableglaccountgroups.ssortcode, req).trim();
		m_sNewRecord = clsManageRequestParameters.get_Request_Parameter(GLAccountSegment.ParamsNewRecord, req).trim();
	}
	
    public void load (Connection conn) throws Exception{
        	
    	String SQL = "SELECT * FROM " + SMTableglaccountgroups.TableName
				+ " WHERE ("
				+ SMTableglaccountgroups.lid + " = " + m_lid 
			+	")";
    	try {
    		ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
    		if (rs.next()){
    			m_lid = Long.toString(rs.getLong(SMTableglaccountgroups.lid));
    			m_sdescription = rs.getString(SMTableglaccountgroups.sdescription);
    			m_sgroupcode =  rs.getString(SMTableglaccountgroups.sgroupcode);
    			m_ssortcode =  rs.getString(SMTableglaccountgroups.ssortcode);
    			m_sNewRecord = "0";
    			rs.close();
    			return;
    		}else{
    			rs.close();
    			setNewRecord("1");
    			return;
    		}
    	}catch (Exception e){
    		throw new Exception("Error [1528403147] reading " + ParamObjectName + " record: " + e.getMessage());
    	}
	}
    
    public void load(String sConf, ServletContext context, String sUser) throws Exception{
    	Connection conn;
    	try {
    		conn = clsDatabaseFunctions.getConnectionWithException(context, 
    			   sConf, 
    			   "MySQL",
    			   SMUtilities.getFullClassName(this.toString()) + ".load - user: " + sUser);
    	} catch (Exception e) {
    		throw new Exception("Error [1528403148] getting connection to load " + ParamObjectName + " - " + e.getMessage());
    	}
    	if (conn == null){
    		throw new Exception("Error [1528403149] could not get connection to load " + ParamObjectName + ".");
    	}
    	try {
			load(conn);
			clsDatabaseFunctions.freeConnection(context, conn, "[1547080693]");
		} catch (Exception e) {
			clsDatabaseFunctions.freeConnection(context, conn, "[1547080694]");
			throw new Exception("Error [1528403150] could load " + ParamObjectName + " - " + e.getMessage());
		}
    }

    public void saveEditableFields(ServletContext context, String sDBID, String sUserName) throws Exception{
		
    	//Get connection
		Connection conn;
		try {
			conn = clsDatabaseFunctions.getConnectionWithException(
				context, 
				sDBID, 
				"MySQL", 
				SMUtilities.getFullClassName(this.toString()) + ":saveEditableFields - user: " + sUserName
			);
		} catch (Exception e1) {
			throw new Exception("Error [1528403151] could not get connection to save " + ParamObjectName + ".");
		}
		
		//Validate entries
		try {
			validateEntries();
		} catch (Exception e) {
			clsDatabaseFunctions.freeConnection(context, conn, "[1547080695]");
			throw new Exception(e.getMessage());
		}
		
		//Check to see if the record already exists:
		String SQL = "INSERT INTO " + SMTableglaccountgroups.TableName
			+ " ("
				+ SMTableglaccountgroups.sdescription
				+ ", " + SMTableglaccountgroups.sgroupcode
				+ ", " + SMTableglaccountgroups.ssortcode
			+ ") VALUES ("
				+ " '" + clsDatabaseFunctions.FormatSQLStatement(m_sdescription) + "'"
				+ ", '" + clsDatabaseFunctions.FormatSQLStatement(m_sgroupcode) + "'"
				+ ", '" + clsDatabaseFunctions.FormatSQLStatement(m_ssortcode) + "'"
			+ ") ON DUPLICATE KEY UPDATE"
				+ " " + SMTableglaccountgroups.sdescription + " = '" + clsDatabaseFunctions.FormatSQLStatement(m_sdescription) + "'"
				+ ", " + SMTableglaccountgroups.ssortcode + " = '" + clsDatabaseFunctions.FormatSQLStatement(m_ssortcode) + "'"
			;
		System.out.println("[1528409761] - SQL = '" + SQL + "'");
	 	try {
	 		Statement stmt = conn.createStatement();
	 		stmt.executeUpdate(SQL);
	 	}catch (SQLException e){
	 		clsDatabaseFunctions.freeConnection(context, conn, "[1547080696]");
	 		throw new Exception("Error [1528403152] saving " + ParamObjectName + " - " + e.getMessage());
	 	}
		
		//Update the ID if it's an insert successful:
	 	if (m_sNewRecord.compareToIgnoreCase("1") == 0){
			SQL = "SELECT last_insert_id()";
			try {
				ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
				if (rs.next()) {
					m_lid = Long.toString(rs.getLong(1));
				}else {
					m_lid = "";
				}
				rs.close();
			} catch (SQLException e) {
				clsDatabaseFunctions.freeConnection(context, conn, "[1547080697]");
				throw new Exception("Error [1528403153] saving with SQL '" + SQL + "' - " + e.getMessage());
			}
			//If something went wrong, we can't get the last ID:
			if (m_lid.compareToIgnoreCase("") == 0){
				clsDatabaseFunctions.freeConnection(context, conn, "[1547080698]");
				throw new Exception("Error [1528403154] - could not get last ID number.");
			}
	 	}
	 	
		//Change new record status
	 	m_sNewRecord = "1";
		clsDatabaseFunctions.freeConnection(context, conn, "[1547080699]");
		return;			
    }
    
	public void delete(String sCode, Connection conn) throws Exception{
			
			//First, check that the record exists:
			String SQL = "SELECT * FROM " + SMTableglaccountgroups.TableName
				+ " WHERE ("
					+ SMTableglaccountgroups.lid + " = " + m_lid 
				+ ")"
			;
			try{
				ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
				if(!rs.next()){
					rs.close();
					throw new Exception(ParamObjectName + " " + sCode + " cannot be found.");
				}else{
					rs.close();
				}
				
			}catch(SQLException e){
				throw new Exception("Error [1528403155] - checking " + ParamObjectName + " to delete - " + e.getMessage());
			}
			
			//Make sure it's not being used on any accounts:
			SQL = "SELECT * FROM " + SMTableglaccounts.TableName
				+ " WHERE ("
					+ "(" + SMTableglaccounts.laccountgroupid + " = " + getlid() + ")"
				+ ")"
			;
			try{
				ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
				if(rs.next()){
					rs.close();
					throw new Exception(ParamObjectName + " " + sCode + " is associated with at least one GL Account"
						+ " - remove it from all the GL Accounts before you try to delete it.");
				}else{
					rs.close();
				}
				
			}catch(SQLException e){
				throw new Exception("Error [1528403165] - checking for GL Accounts using " + ParamObjectName 
					+ " '" + sCode + "' before deleting - " + e.getMessage());
			}
			
			try{
				SQL = "DELETE FROM " + SMTableglaccountgroups.TableName
					+ " WHERE ("
						+ SMTableglaccountgroups.lid + " = " + m_lid
					+ ")"
				;
				Statement stmt = conn.createStatement();
				stmt.execute(SQL);
			}catch(Exception e){
				throw new Exception("Error [1528403156] deleting " + ParamObjectName + " with SQL '" + SQL + "' - " + e.getMessage());
			}
			return;
		}
	
	private void validateEntries() throws Exception{
		
		String s = "";
		
		try {
			clsValidateFormFields.validateLongIntegerField(m_lid, "ID", 0L, clsValidateFormFields.MAX_LONG_VALUE);
		} catch (Exception e) {
			s += e.getMessage() + "  ";
		}
		
		try {
			clsValidateFormFields.validateStringField(m_sdescription, SMTableglaccountgroups.sdescriptionLength, "Group description", false);
		} catch (Exception e) {
			s += e.getMessage() + "  ";
		}
		
		try {
			clsValidateFormFields.validateStringField(m_sgroupcode, SMTableglaccountgroups.sgroupcodeLength, "Group code", false);
		} catch (Exception e) {
			s += e.getMessage() + "  ";
		}

		try {
			clsValidateFormFields.validateStringField(m_ssortcode, SMTableglaccountgroups.ssortcodeLength, "Sort code", false);
		} catch (Exception e) {
			s += e.getMessage() + "  ";
		}

		if (s.compareToIgnoreCase("") != 0){
			throw new Exception(s);
		}
	}
	
	public String getlid() {
		return m_lid;
	}
	public void setlid(String lid) {
		m_lid = lid;
	}
	public String getsdescription() {
		return m_sdescription;
	}
	public void setsdescription(String sdescription) {
		m_sdescription = sdescription;
	}

	public String getsgroupcode() {
		return m_sgroupcode;
	}
	public void setsgroupcode(String sgroupcode) {
		m_sgroupcode = sgroupcode;
	}

	public String getssortcode() {
		return m_ssortcode;
	}
	public void setssortcode(String ssortcode) {
		m_ssortcode = ssortcode;
	}
	
	public void setNewRecord(String newrecord){
		m_sNewRecord = newrecord;
	}
	public String getNewRecord(){
		return m_sNewRecord;
	}
	public String getObjectName(){
	    return ParamObjectName;
	}

}