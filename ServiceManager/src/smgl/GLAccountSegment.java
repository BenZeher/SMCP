package smgl;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import SMDataDefinition.SMTableglaccountsegments;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsManageRequestParameters;
import ServletUtilities.clsValidateFormFields;
import smcontrolpanel.SMUtilities;

public class GLAccountSegment extends java.lang.Object{
	
	public static final String ParamObjectName = "Account Segment";
	
	public static final String ParamsNewRecord = "sNewRecord";
	public static final String FORM_NAME = "MAINFORM";
	
	private String m_lid;
	private String m_sdescription;
	private String m_silength;
	private String m_siusedinclosing;
	
	private String m_sNewRecord;
	
	public GLAccountSegment(){
		m_lid = "0";
		m_sdescription = "";
		m_silength = "0";
		m_siusedinclosing = "0";
		m_sNewRecord = "";
	}
	
	public GLAccountSegment (HttpServletRequest req){

		m_lid = clsManageRequestParameters.get_Request_Parameter(SMTableglaccountsegments.lid, req).trim();
		m_sdescription = clsManageRequestParameters.get_Request_Parameter(SMTableglaccountsegments.sdescription, req).trim();
		m_silength = clsManageRequestParameters.get_Request_Parameter(SMTableglaccountsegments.ilength, req).trim();
		if (req.getParameter(SMTableglaccountsegments.iuseinclosing) == null){
			m_siusedinclosing = "0";
		}else{
			m_siusedinclosing = "1";
		}
		m_sNewRecord = clsManageRequestParameters.get_Request_Parameter(GLAccountSegment.ParamsNewRecord, req).trim();
	}
	
    public void load (Connection conn) throws Exception{
        	
    	String SQL = "SELECT * FROM " + SMTableglaccountsegments.TableName
				+ " WHERE ("
				+ SMTableglaccountsegments.lid + " = " + m_lid 
			+	")";
    	try {
    		ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
    		if (rs.next()){
    			m_lid = Long.toString(rs.getLong(SMTableglaccountsegments.lid));
    			m_sdescription = rs.getString(SMTableglaccountsegments.sdescription);
    			m_silength =  Long.toString(rs.getLong(SMTableglaccountsegments.ilength));
    			m_siusedinclosing =  Long.toString(rs.getLong(SMTableglaccountsegments.iuseinclosing));
    			m_sNewRecord = "0";
    			rs.close();
    			return;
    		}else{
    			rs.close();
    			setNewRecord("1");
    			return;
    		}
    	}catch (Exception e){
    		throw new Exception("Error [1523044982] reading " + ParamObjectName + " record: " + e.getMessage());
    	}
	}
    
    public void load(String sDBIB, ServletContext context, String sUser) throws Exception{
    	Connection conn;
    	try {
    		conn = clsDatabaseFunctions.getConnectionWithException(context, 
    			   sDBIB, 
    			   "MySQL",
    			   SMUtilities.getFullClassName(this.toString()) + ".load - user: " + sUser);
    	} catch (Exception e) {
    		throw new Exception("Error [1523045033] getting connection to load " + ParamObjectName + " - " + e.getMessage());
    	}
    	if (conn == null){
    		throw new Exception("Error [1523045034] could not get connection to load " + ParamObjectName + ".");
    	}
    	try {
			load(conn);
			clsDatabaseFunctions.freeConnection(context, conn, "[1547080702]");
		} catch (Exception e) {
			clsDatabaseFunctions.freeConnection(context, conn, "[1547080703]");
			throw new Exception("Error [1523045035] could load " + ParamObjectName + " - " + e.getMessage());
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
			throw new Exception("Error [1523045035] could not get connection to save " + ParamObjectName + ".");
		}
		
		//Validate entries
		try {
			validateEntries();
		} catch (Exception e) {
			clsDatabaseFunctions.freeConnection(context, conn, "[1547080704]");
			throw new Exception(e.getMessage());
		}
		
		//Check to see if the record already exists:
		String SQL = "INSERT INTO " + SMTableglaccountsegments.TableName
			+ " ("
				+ SMTableglaccountsegments.ilength
				+ ", " + SMTableglaccountsegments.iuseinclosing
				+ ", " + SMTableglaccountsegments.sdescription
			+ ") VALUES ("
				+ m_silength
				+ ", " + m_siusedinclosing
				+ ", '" + clsDatabaseFunctions.FormatSQLStatement(m_sdescription) + "'"
			+ ") ON DUPLICATE KEY UPDATE "
				+ SMTableglaccountsegments.ilength + " = " + m_silength
				+ ", " + SMTableglaccountsegments.iuseinclosing + " = " + m_siusedinclosing
				+ ", " + SMTableglaccountsegments.sdescription + " = '" + clsDatabaseFunctions.FormatSQLStatement(m_sdescription) + "'"
			;
	 	try {
	 		Statement stmt = conn.createStatement();
	 		stmt.executeUpdate(SQL);
	 	}catch (SQLException e){
	 		clsDatabaseFunctions.freeConnection(context, conn, "[1547080705]");
	 		throw new Exception("Error [1523045973] saving " + ParamObjectName + " - " + e.getMessage());
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
				clsDatabaseFunctions.freeConnection(context, conn, "[1547080706]");
				throw new Exception("Error [1523046122] saving with SQL '" + SQL + "' - " + e.getMessage());
			}
			//If something went wrong, we can't get the last ID:
			if (m_lid.compareToIgnoreCase("") == 0){
				clsDatabaseFunctions.freeConnection(context, conn, "[1547080707]");
				throw new Exception("Error [1523046123] -could not get last ID number.");
			}
	 	}
	 	
		//Change new record status
	 	m_sNewRecord = "1";
		clsDatabaseFunctions.freeConnection(context, conn, "[1547080708]");
		return;			
    }
    
	public void delete(String sCode, Connection conn) throws Exception{
			
			//First, check that the record exists:
			String SQL = "SELECT * FROM " + SMTableglaccountsegments.TableName
				+ " WHERE ("
					+ SMTableglaccountsegments.lid + " = " + m_lid 
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
				throw new Exception("Error [1523046395] - checking " + ParamObjectName + " to delete - " + e.getMessage());
			}
			
			try{
				SQL = "DELETE FROM " + SMTableglaccountsegments.TableName
					+ " WHERE ("
						+ SMTableglaccountsegments.lid + " = " + m_lid
					+ ")"
				;
				Statement stmt = conn.createStatement();
				stmt.execute(SQL);
			}catch(Exception e){
				throw new Exception("Error [1523046580] deleting " + ParamObjectName + " with SQL '" + SQL + "' - " + e.getMessage());
			}
			return;
		}
	
	private void validateEntries() throws Exception{
		
		String s = "";
		
		try {
			clsValidateFormFields.validateLongIntegerField(m_lid, "ID", -1L, clsValidateFormFields.MAX_LONG_VALUE);
		} catch (Exception e) {
			s += e.getMessage() + "  ";
		}
		
		try {
			clsValidateFormFields.validateLongIntegerField(m_silength, "Segment length", 0L, clsValidateFormFields.MAX_LONG_VALUE);
		} catch (Exception e) {
			s += e.getMessage() + "  ";
		}
		
		try {
			clsValidateFormFields.validateLongIntegerField(m_silength, "Used in closing", 0L, 1L);
		} catch (Exception e) {
			s += e.getMessage() + "  ";
		}
		
		try {
			clsValidateFormFields.validateStringField(m_sdescription, SMTableglaccountsegments.sdescriptionLength, "Segment description", false);
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
	public String getsilength() {
		return m_silength;
	}
	public void setsilength(String silength) {
		m_silength = silength;
	}
	public String getsiusedinclosing() {
		return m_siusedinclosing;
	}
	public void setsiusedinclosing(String siusedinclosing) {
		m_siusedinclosing = siusedinclosing;
	}
	public String getsdescription() {
		return m_sdescription;
	}
	public void setsdescription(String sdescription) {
		m_sdescription = sdescription;
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