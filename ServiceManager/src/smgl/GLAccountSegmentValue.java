package smgl;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import SMDataDefinition.SMTableglacctsegmentvalues;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsManageRequestParameters;
import ServletUtilities.clsValidateFormFields;
import smcontrolpanel.SMUtilities;

public class GLAccountSegmentValue extends java.lang.Object{
	
	public static final String ParamObjectName = "Account Segment Value";
	
	public static final String ParamsNewRecord = "sNewRecord";
	public static final String FORM_NAME = "MAINFORM";
	
	private String m_lid;
	private String m_lssegmentid;
	private String m_sdescription;
	private String m_svalue;
	
	private String m_sNewRecord;
	
	public GLAccountSegmentValue(){
		m_lid = "0";
		m_lssegmentid = "0";
		m_sdescription = "";
		m_svalue = "";
		m_sNewRecord = "";
	}
	
	public GLAccountSegmentValue (HttpServletRequest req){

		m_lid = clsManageRequestParameters.get_Request_Parameter(SMTableglacctsegmentvalues.lid, req).trim();
		m_lssegmentid = clsManageRequestParameters.get_Request_Parameter(SMTableglacctsegmentvalues.lsegmentid, req).trim();
		m_sdescription = clsManageRequestParameters.get_Request_Parameter(SMTableglacctsegmentvalues.sdescription, req).trim();
		m_svalue = clsManageRequestParameters.get_Request_Parameter(SMTableglacctsegmentvalues.svalue, req).trim();
		m_sNewRecord = clsManageRequestParameters.get_Request_Parameter(ParamsNewRecord, req).trim();
	}
	
    public void load (Connection conn) throws Exception{
        	
    	String SQL = "SELECT * FROM " + SMTableglacctsegmentvalues.TableName
				+ " WHERE ("
				+ SMTableglacctsegmentvalues.lid + " = " + m_lid 
			+	")";
    	try {
    		ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
    		if (rs.next()){
    			m_lid = Long.toString(rs.getLong(SMTableglacctsegmentvalues.lid));
    			m_lssegmentid = Long.toString(rs.getLong(SMTableglacctsegmentvalues.lsegmentid));
    			m_sdescription = rs.getString(SMTableglacctsegmentvalues.sdescription);
    			m_svalue = rs.getString(SMTableglacctsegmentvalues.svalue);
    			m_sNewRecord = "0";
    			rs.close();
    			return;
    		}else{
    			rs.close();
    			setNewRecord("1");
    			return;
    		}
    	}catch (Exception e){
    		throw new Exception("Error [1523306954] reading " + ParamObjectName + " record: " + e.getMessage());
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
    		throw new Exception("Error [1523306955] getting connection to load " + ParamObjectName + " - " + e.getMessage());
    	}
    	if (conn == null){
    		throw new Exception("Error [1523306956] could not get connection to load " + ParamObjectName + ".");
    	}
    	try {
			load(conn);
			clsDatabaseFunctions.freeConnection(context, conn, "[1547080711]");
		} catch (Exception e) {
			clsDatabaseFunctions.freeConnection(context, conn, "[1547080712]");
			throw new Exception("Error [1523306957] could load " + ParamObjectName + " - " + e.getMessage());
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
			throw new Exception("Error [1523306958] could not get connection to save " + ParamObjectName + ".");
		}
		
		//Validate entries
		try {
			validateEntries(conn);
		} catch (Exception e) {
			clsDatabaseFunctions.freeConnection(context, conn, "[1547080713]");
			throw new Exception(e.getMessage());
		}
		
		//Check to see if the record already exists:
		String SQL = "";
		if (getNewRecord().compareTo("1") == 0){
			SQL = "INSERT INTO " + SMTableglacctsegmentvalues.TableName
				+ " ("
					+ SMTableglacctsegmentvalues.lsegmentid
					+ ", " + SMTableglacctsegmentvalues.sdescription
					+ ", " + SMTableglacctsegmentvalues.svalue
				+ ") VALUES ("
					+ m_lssegmentid
					+ ", '" + clsDatabaseFunctions.FormatSQLStatement(m_sdescription) + "'"
					+ ", '" + clsDatabaseFunctions.FormatSQLStatement(m_svalue) + "'"
				+ ")"
			;
		}else{
			SQL =  " UPDATE " + SMTableglacctsegmentvalues.TableName
				+ " SET " + SMTableglacctsegmentvalues.lsegmentid + " = " + m_lssegmentid
				+ ", " + SMTableglacctsegmentvalues.sdescription + " = '" + clsDatabaseFunctions.FormatSQLStatement(m_sdescription) + "'"
				+ ", " + SMTableglacctsegmentvalues.svalue + " = '" + clsDatabaseFunctions.FormatSQLStatement(m_svalue) + "'"
				+ " WHERE ("
					+ "(" + SMTableglacctsegmentvalues.lid + "='" + getlid() + "')"
				+ ")"
			;
		}

	 	try {
	 		Statement stmt = conn.createStatement();
	 		stmt.executeUpdate(SQL);
	 	}catch (SQLException e){
	 		clsDatabaseFunctions.freeConnection(context, conn, "[1547080714]");
	 		throw new Exception("Error [1523307214] saving " + ParamObjectName + " with SQL: '" + SQL + "' - " + e.getMessage());
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
				clsDatabaseFunctions.freeConnection(context, conn, "[1547080715]");
				throw new Exception("Error [1523307215] saving with SQL '" + SQL + "' - " + e.getMessage());
			}
			//If something went wrong, we can't get the last ID:
			if (m_lid.compareToIgnoreCase("") == 0){
				clsDatabaseFunctions.freeConnection(context, conn, "[1547080716]");
				throw new Exception("Error [1523307216] -could not get last ID number.");
			}
	 	}
	 	
		//Change new record status
	 	m_sNewRecord = "1";
		clsDatabaseFunctions.freeConnection(context, conn, "[1547080717]");
		return;			
    }
    
	public void delete(String sCode, Connection conn) throws Exception{
			
			//First, check that the record exists:
			String SQL = "SELECT * FROM " + SMTableglacctsegmentvalues.TableName
				+ " WHERE ("
					+ SMTableglacctsegmentvalues.lid + " = " + m_lid 
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
				throw new Exception("Error [1523307265] - checking " + ParamObjectName + " to delete - " + e.getMessage());
			}
			
			try{
				SQL = "DELETE FROM " + SMTableglacctsegmentvalues.TableName
					+ " WHERE ("
						+ SMTableglacctsegmentvalues.lid + " = " + m_lid
					+ ")"
				;
				Statement stmt = conn.createStatement();
				stmt.execute(SQL);
			}catch(Exception e){
				throw new Exception("Error [1523307266] deleting " + ParamObjectName + " with SQL '" + SQL + "' - " + e.getMessage());
			}
			return;
		}
	
	private void validateEntries(Connection conn) throws Exception{
		
		String s = "";
		
		try {
			clsValidateFormFields.validateLongIntegerField(m_lid, "ID", 0L, clsValidateFormFields.MAX_LONG_VALUE);
		} catch (Exception e) {
			s += e.getMessage() + "  ";
		}
		
		try {
			clsValidateFormFields.validateLongIntegerField(m_lssegmentid, "Segment ID", 1L, clsValidateFormFields.MAX_LONG_VALUE);
		} catch (Exception e) {
			s += e.getMessage() + "  ";
		}
		//Validate the segment:
		GLAccountSegment seg = new GLAccountSegment();
		seg.setlid(m_lssegmentid);
		try {
			seg.load(conn);
		} catch (Exception e1) {
			s += "Error [1523324678] reading account segment - " + e1.getMessage() + ".  ";
		}
		//If we didn't load a real segment, kick it back:
		if (seg.getsdescription().compareTo("") == 0){
			s += "Selected segment is invalid.  ";
		}
		
		try {
			clsValidateFormFields.validateStringField(m_sdescription, SMTableglacctsegmentvalues.sdescriptionLength, "Segment value description", false);
		} catch (Exception e) {
			s += e.getMessage() + "  ";
		}

		try {
			clsValidateFormFields.validateStringField(m_svalue, SMTableglacctsegmentvalues.svalueLength, "Segment value", false);
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
	public String getsegmentid() {
		return m_lssegmentid;
	}
	public void setsegmentid(String lsegmentid) {
		m_lssegmentid = lsegmentid;
	}
	public String getsdescription() {
		return m_sdescription;
	}
	public void setsdescription(String sdescription) {
		m_sdescription = sdescription;
	}
	public String getsvalue() {
		return m_svalue;
	}
	public void setsvalue(String svalue) {
		m_svalue = svalue;
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