package smgl;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import SMDataDefinition.SMTablecostcenters;
import SMDataDefinition.SMTableglaccounts;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsManageRequestParameters;
import ServletUtilities.clsValidateFormFields;
import smcontrolpanel.SMUtilities;

public class GLCostCenter extends java.lang.Object{

	public static final String ParamObjectName = "Cost Center";
	public static final String ParamsNewRecord = "sNewRecord";
	public static final String ADDING_NEW_RECORD_PARAM_VALUE_TRUE = "T";
	public static final String ADDING_NEW_RECORD_PARAM_VALUE_FALSE = "F";
	public static final String EDIT_FORM_NAME = "EDITCOSTCENTERFORM";
	
	private String m_lid;
	private String m_iactive;
	private String m_scostcentername;
	private String m_sdescription;

	private String m_snewrecord;
	
	public GLCostCenter(
        ) {
		m_lid = "-1";
		m_iactive = "1";
		m_scostcentername = "";
		m_sdescription = "";
		m_snewrecord = ADDING_NEW_RECORD_PARAM_VALUE_FALSE;
    }
    public GLCostCenter(HttpServletRequest req) {
		m_lid = clsManageRequestParameters.get_Request_Parameter(SMTablecostcenters.lid, req).trim();
		if (req.getParameter(SMTablecostcenters.iactive) == null){
			m_iactive = "0";
		}else{
			m_iactive = "1";
		}	
		m_scostcentername = clsManageRequestParameters.get_Request_Parameter(SMTablecostcenters.scostcentername, req).trim();
		m_sdescription = clsManageRequestParameters.get_Request_Parameter(SMTablecostcenters.sdescription, req).trim();
		
		m_snewrecord = clsManageRequestParameters.get_Request_Parameter(ParamsNewRecord, req).trim();
	}
    public void load(String sDBName, ServletContext context, String sUser) throws Exception{
    	Connection conn = null;
    	try {
			conn = clsDatabaseFunctions.getConnectionWithException(
				context, 
				sDBName, 
				"MySQL", 
				SMUtilities.getFullClassName(this.toString()) + ".load - user: " + sUser);
		} catch (Exception e) {
			throw new Exception("Error [1454964579] - could not get connection - " + e.getMessage());
		}
    	
    	try {
			load(conn);
		} catch (Exception e) {
			throw new Exception(e.getMessage());
		}
    			
    	clsDatabaseFunctions.freeConnection(context, conn, "[1547080735]");
    }

	public void load (
    		Connection conn
    		) throws Exception{
        String SQL = "SELECT * FROM " + SMTablecostcenters.TableName
        	+ " WHERE ("
        		+ "(" + SMTablecostcenters.lid + " = " + m_lid + ")"
        	+ ")"
        ;
		
		try {
			ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn); 
			if(rs.next()){
				m_scostcentername = rs.getString(SMTablecostcenters.scostcentername);
				m_sdescription = rs.getString(SMTablecostcenters.sdescription);
				m_iactive = Integer.toString(rs.getInt(SMTablecostcenters.iactive));

				m_snewrecord = ADDING_NEW_RECORD_PARAM_VALUE_FALSE;
			}else{
				throw new Exception("Error [1454964790] - Cost Center ID '" + m_lid + "' does not exist.");
			}
			rs.close();
		}catch (SQLException ex){
			throw new Exception("Error [1454964791] loading cost center with lid '" + m_lid + "' using SQL: " + SQL + " - " + ex.getMessage());
		}
	}
    public void save(ServletContext context, String sConf, String sUserName) throws Exception{
		
    	//Get connection
		Connection conn;
		try {
			conn = clsDatabaseFunctions.getConnectionWithException(
				context, 
				sConf, 
				"MySQL", 
				SMUtilities.getFullClassName(this.toString()) + ":save - user: " + sUserName
			);
		} catch (Exception e1) {
			throw new Exception("Error [1454964792] - could not get connection to save.");
		}
		
		//Validate entries
		try {
			validateEntries(conn);
		} catch (Exception e1) {
			clsDatabaseFunctions.freeConnection(context, conn, "[1547080736]");
			throw new Exception(e1.getMessage());
		}
		//Update the editable fields.
		String SQL = "";
		if (get_snewrecord().compareToIgnoreCase(ADDING_NEW_RECORD_PARAM_VALUE_TRUE) == 0){
			SQL = "INSERT INTO " + SMTablecostcenters.TableName + "("
				       +  SMTablecostcenters.iactive
				+ ", " + SMTablecostcenters.scostcentername
				+ ", " + SMTablecostcenters.sdescription
			+ ") VALUES ("
			            + get_sactive()
				+ ", '" + clsDatabaseFunctions.FormatSQLStatement(get_scostcentername()) + "'"
				+ ", '" + clsDatabaseFunctions.FormatSQLStatement(get_sdescription()) + "'"
				+ ")"
			;
		}else{
			SQL = "UPDATE " + SMTablecostcenters.TableName 
				+ " SET " +  SMTablecostcenters.iactive + " = " + get_sactive()
				+ ", " + SMTablecostcenters.scostcentername + " = '" + clsDatabaseFunctions.FormatSQLStatement(get_scostcentername()) + "'"
				+ ", " + SMTablecostcenters.sdescription + " = '" + clsDatabaseFunctions.FormatSQLStatement(get_sdescription()) + "'"
				+ " WHERE (" + SMTablecostcenters.lid + "=" + get_slid() + ")"
			;
		}

	 	try {
	 		Statement stmt = conn.createStatement();
	 		stmt.executeUpdate(SQL);
	 	}catch (SQLException e){
	 		clsDatabaseFunctions.freeConnection(context, conn, "[1547080737]");
	 		throw new Exception("Error [1454964792] saving " + GLCostCenter.ParamObjectName + " record - " + e.getMessage());
	 	}

	 	//Update the ID if it's a successful insert:
	 	if (get_snewrecord().compareToIgnoreCase(ADDING_NEW_RECORD_PARAM_VALUE_TRUE) == 0){
			SQL = "SELECT last_insert_id()";
			try {
				ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
				if (rs.next()) {
					set_slid(Long.toString(rs.getLong(1)));
				}else {
					set_slid("");
				}
				rs.close();
			} catch (SQLException e) {
				set_slid("");
			}
			//If something went wrong, we can't get the last ID:
			if (get_slid().compareToIgnoreCase("") == 0){
				throw new Exception("Error [1454964793] - record was saved but the ID is incorrect");
			}
	 	}
	 	set_snewrecord(ADDING_NEW_RECORD_PARAM_VALUE_FALSE);
	 	clsDatabaseFunctions.freeConnection(context, conn, "[1547080738]");
    }
    private void validateEntries(Connection conn) throws Exception{
    	
    	String s = "";
    	
    	try {
    		m_scostcentername = clsValidateFormFields.validateStringField(m_scostcentername, SMTablecostcenters.scostcenternamelength, "Cost center name", false);
		} catch (Exception e1) {
			s += e1.getMessage() + "\n";
		}
       	try {
       		m_sdescription = clsValidateFormFields.validateStringField(m_sdescription, SMTablecostcenters.sdescriptionlength, "Description", false);
		} catch (Exception e1) {
			s += e1.getMessage() + "\n";
		}

       	try {
			m_iactive = clsValidateFormFields.validateIntegerField(m_iactive, "Active", 0, 1);
		} catch (Exception e1) {
			s += e1.getMessage() + "\n";
		}
       	
     	try {
			m_lid = clsValidateFormFields.validateLongIntegerField(m_lid, "Tax ID", -1L, clsValidateFormFields.MAX_LONG_VALUE);
		} catch (Exception e1) {
			s += e1.getMessage() + "\n";
		}
     	
     	m_snewrecord = m_snewrecord.trim();
     	if (
     		(m_snewrecord.compareToIgnoreCase(GLCostCenter.ADDING_NEW_RECORD_PARAM_VALUE_FALSE) == 0)
     		|| (m_snewrecord.compareToIgnoreCase(GLCostCenter.ADDING_NEW_RECORD_PARAM_VALUE_TRUE) == 0)
     	){
     	}else{
     		s += "New record flag '" + m_snewrecord + "' is invalid.";
     	}

     	if (s.compareToIgnoreCase("") != 0){
     		throw new Exception(s);
     	}
     	return;
    	
    }
	public void delete(String slid, Connection conn) throws Exception{
		
		load(conn);
	
		//Clear cost center ID from any gl account currently using it
		String SQL = "UPDATE " + SMTableglaccounts.TableName
			+ " SET " + SMTableglaccounts.iCostCenterID + "=" + "0"
			+ " WHERE ("
				+ "(" + SMTableglaccounts.iCostCenterID + " = " + get_slid() + ")"
			+ ")"
		;
	
		try {
			Statement updatestmt = conn.createStatement();
			updatestmt.execute(SQL);
		} catch (Exception e) {
			throw new Exception("Error [1455040091] upading glaccounts with cost center name '" 
				+ get_scostcentername() + "' - " + e.getMessage());
		}
	
		//Now delete the cost center:
		 SQL = "DELETE FROM " + SMTablecostcenters.TableName
			+ " WHERE ("
				+ "(" + SMTablecostcenters.lid + " = " + get_slid() + ")"
			+ ")"
		;
		try {
			Statement deletestmt = conn.createStatement();
			deletestmt.execute(SQL);
		} catch (Exception e) {
			throw new Exception("Error [1455030843] deleting cost center with name '" 
				+ get_scostcentername() + "' - " + e.getMessage());
		}
	}
	public String get_slid() {
		return m_lid;
	}
	public void set_slid(String slid) {
		m_lid = slid;
	}

	public String get_sdescription() {
		return m_sdescription;
	}
	public void set_sdescription(String staxdescription) {
		m_sdescription = staxdescription;
	}
	public String get_scostcentername() {
		return m_scostcentername;
	}
	public void set_scostcentername(String scostcentername) {
		m_scostcentername = scostcentername;
	}
	public String get_sactive() {
		return m_iactive;
	}
	public void set_sactive(String sactive) {
		m_iactive = sactive;
	}

	public void set_snewrecord(String snewrecord){
		m_snewrecord = snewrecord;
	}
	public String get_snewrecord(){
		return m_snewrecord;
	}
}