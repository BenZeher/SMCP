package TimeCardSystem;


import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsManageRequestParameters;
import ServletUtilities.clsValidateFormFields;
import TCSDataDefinition.TCSTablemadgiceventtypes;

public class TCMADGICEventType extends java.lang.Object{

	public final static String ParamObjectName = "MADGIC Event Type";
	public static final String ADDING_NEW_RECORD_PARAM_VALUE_TRUE = "T";
	public static final String ADDING_NEW_RECORD_PARAM_VALUE_FALSE = "F";
	public static final String EDIT_FORM_NAME = "EDITMADGICEVENTTYPEFORM";
	
	private String m_slid;
	private String m_sname;
	private String m_sdescription;
	private String m_inumberofpoints;
	
	public TCMADGICEventType(
        ) {
		initializeVariables();
    }
    public TCMADGICEventType(HttpServletRequest req) {
    	initializeVariables();
    	m_slid = clsManageRequestParameters.get_Request_Parameter(TCSTablemadgiceventtypes.lid, req).trim();
    	m_sname = clsManageRequestParameters.get_Request_Parameter(TCSTablemadgiceventtypes.sname, req).trim();
    	m_inumberofpoints = clsManageRequestParameters.get_Request_Parameter(TCSTablemadgiceventtypes.inumberofpoints, req).trim();
		m_sdescription = clsManageRequestParameters.get_Request_Parameter(TCSTablemadgiceventtypes.sdescription, req).trim();
	}
    public void load(String sDBName, ServletContext context, String sUser) throws Exception{
    	Connection conn = null;
    	try {
			conn = clsDatabaseFunctions.getConnectionWithException(
				context, 
				sDBName, 
				"MySQL", 
				this.toString() + ".load - user: " + sUser);
		} catch (Exception e) {
			throw new Exception("Error [1486569766] - could not get connection - " + e.getMessage());
		}
    	
    	try {
			load(conn);
		} catch (Exception e) {
			throw new Exception(e.getMessage());
		}
    			
    	clsDatabaseFunctions.freeConnection(context, conn);
    }
 
	public void load (
    		Connection conn
    		) throws Exception{
        String SQL = "SELECT * FROM " + TCSTablemadgiceventtypes.TableName
        	+ " WHERE ("
        		+ "(" + TCSTablemadgiceventtypes.lid + " = " + get_slid() + ")"
        	+ ")"
        ;
		
		try {
			ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn); 
			if(rs.next()){
				m_sname = rs.getString(TCSTablemadgiceventtypes.sname);
				m_inumberofpoints = Integer.toString(rs.getInt(TCSTablemadgiceventtypes.inumberofpoints));
				m_sdescription = rs.getString(TCSTablemadgiceventtypes.sdescription);
			}else{
				throw new Exception("Error [1486569843] - " + ParamObjectName + " ID '" + this.get_slid() + "' does not exist.");
			}
			rs.close();
		}catch (SQLException ex){
			throw new Exception("Error [1486569844] loading " + ParamObjectName + " with lid '" + get_slid() + "' using SQL: " + SQL 
				+ " - " + ex.getMessage());
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
				this.toString() + ":save - user: " + sUserName
			);
		} catch (Exception e1) {
			throw new Exception("Error [1486569845] - could not get connection to save.");
		}
		
		//Validate entries
		try {
			validateEntries(conn);
		} catch (Exception e1) {
			clsDatabaseFunctions.freeConnection(context, conn);
			throw new Exception(e1.getMessage());
		}
		
		//Update the editable fields. 		
		String SQL = "INSERT INTO " + TCSTablemadgiceventtypes.TableName + "("
			+ TCSTablemadgiceventtypes.inumberofpoints
			+ ", " + TCSTablemadgiceventtypes.sdescription
			+ ", " + TCSTablemadgiceventtypes.sname
		+ ") VALUES ("
			+ get_snumberofpoints()
			+ ", '" + clsDatabaseFunctions.FormatSQLStatement(get_sdescription()) + "'"
			+ ", '" + clsDatabaseFunctions.FormatSQLStatement(get_sname()) + "'"
		+ ") ON DUPLICATE KEY UPDATE "
			+ TCSTablemadgiceventtypes.sdescription + " = '"  + clsDatabaseFunctions.FormatSQLStatement(get_sdescription()) + "'"
			+ ", " + TCSTablemadgiceventtypes.inumberofpoints + " = " + get_snumberofpoints()
		;

	 	try {
	 		Statement stmt = conn.createStatement();
	 		stmt.executeUpdate(SQL);
	 	}catch (SQLException e){
	 		clsDatabaseFunctions.freeConnection(context, conn);
	 		throw new Exception("Error [1486569846] saving " + ParamObjectName + " record - " + e.getMessage());
	 	}

	 	//Update the ID if it's a successful insert:
	 	if (get_slid().compareToIgnoreCase("-1") == 0){
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
				throw new Exception("Error [1486569846] - record was saved but the ID is incorrect");
			}
	 	}
	 	clsDatabaseFunctions.freeConnection(context, conn);
    }
    private void validateEntries(Connection conn) throws Exception{
    	
    	String s = "";
    	
    	try {
    		m_sname = clsValidateFormFields.validateStringField(m_sname, TCSTablemadgiceventtypes.snameLength, "Name", false);
		} catch (Exception e1) {
			s += e1.getMessage() + "\n";
		}
       	try {
       		m_sdescription = clsValidateFormFields.validateStringField(m_sdescription, TCSTablemadgiceventtypes.sdescriptionLength, "Description", false);
		} catch (Exception e1) {
			s += e1.getMessage() + "\n";
		}
      	try {
      		m_inumberofpoints = clsValidateFormFields.validateIntegerField(m_inumberofpoints, "Number of points", -1000, clsValidateFormFields.MAX_INT_VALUE);
		} catch (Exception e1) {
			s += e1.getMessage() + "\n";
		}

     	if (s.compareToIgnoreCase("") != 0){
     		throw new Exception(s);
     	}
     	return;
    	
    }
    public void delete(String slid, String sDBID, ServletContext context, String sUser) throws Exception{
    	
    	Connection conn = null;
    	conn = clsDatabaseFunctions.getConnection(context, sDBID, "MySQL", this.getClass().getName() + "- user: " + sUser);
    	delete(slid, conn);
    	clsDatabaseFunctions.freeConnection(context, conn);
    	return;
    	
    }
	public void delete(String slid, Connection conn) throws Exception{
		
		//TODO:
		//First check to see if this tax is in use on any events:
		/*
		String SQL = "SELECT"
			+ " " + SMTableorderheaders.strimmedordernumber
			+ " FROM " + SMTableorderheaders.TableName
			+ " WHERE ("
				+ "(" + SMTableorderheaders.staxjurisdiction + " = '" + get_staxjurisdiction() + "')"
				+ " AND (" + SMTableorderheaders.itaxid + " = " + get_slid() + ")"
			+ ")"
		;
		try {
			ResultSet rs = TimeCardUtilities.openResultSet(SQL, conn);
			if (rs.next()){
				throw new Exception("Cannot delete - this tax is currently in use on some orders.");
			}
			rs.close();
		} catch (Exception e) {
			throw new Exception("Error [1453835423] checking orders for tax jurisdiction '" 
				+ get_staxjurisdiction() + "', tax type '" + get_staxtype() + "' - " + e.getMessage());
		}
		*/
		
		//Now delete the type:
		String SQL = "DELETE FROM " + TCSTablemadgiceventtypes.TableName
			+ " WHERE ("
				+ "(" + TCSTablemadgiceventtypes.lid + " = " + get_slid() + ")"
			+ ")"
		;
		try {
			Statement stmt = conn.createStatement();
			stmt.execute(SQL);
		} catch (Exception e) {
			throw new Exception("Error [1486570504] deleting " + ParamObjectName + " with ID '" 
				+ get_slid() + " - " + e.getMessage());
		}
	}
	public String get_sname() {
		return m_sname;
	}
	public void set_sname(String sName) {
		m_sname = sName;
	}
	public String get_snumberofpoints() {
		return m_inumberofpoints;
	}
	public void set_snumberofpoints(String sNumberOfPoints) {
		m_inumberofpoints = sNumberOfPoints;
	}
	public String get_sdescription() {
		return m_sdescription;
	}
	public void set_sdescription(String sdescription) {
		m_sdescription = sdescription;
	}
	public String get_slid() {
		return m_slid;
	}
	public void set_slid(String slid) {
		m_slid = slid;
	}
	private void initializeVariables (){
		m_slid = "-1";
		m_sname = "";
		m_sdescription = "";
		m_inumberofpoints = "0";
	}
}
