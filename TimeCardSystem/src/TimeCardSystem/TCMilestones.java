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
import TCSDataDefinition.TCSTableMilestones;

public class TCMilestones extends java.lang.Object{

	public static final String ParamObjectName = "Milestone";
	public static final String EDIT_FORM_NAME = "EDITMILESTONEFORM";
	
	private String m_slid;
	private String m_sName;
	private String m_sDescription;
	private String m_sEmployeeTypeID;
	
	public TCMilestones(
        ) {
		initializeVariables();
    }
    public TCMilestones(HttpServletRequest req) {
    	initializeVariables();
    	m_slid = clsManageRequestParameters.get_Request_Parameter(TCSTableMilestones.lid, req).trim();
    	m_sName = clsManageRequestParameters.get_Request_Parameter(TCSTableMilestones.sName, req).trim();
    	m_sDescription = clsManageRequestParameters.get_Request_Parameter(TCSTableMilestones.sDescription, req).trim();
		m_sEmployeeTypeID = clsManageRequestParameters.get_Request_Parameter(TCSTableMilestones.sEmployeeTypeID, req).trim();
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
			throw new Exception("Error [14866863014] - could not get connection - " + e.getMessage());
		}
    	
    	try {
			load(conn);
		} catch (Exception e) {
			throw new Exception(e.getMessage());
		}
    			
    	clsDatabaseFunctions.freeConnection(context, conn, "[1547060157]");
    }
 
	public void load (
    		Connection conn
    		) throws Exception{
        String SQL = "SELECT * FROM " + TCSTableMilestones.TableName
        	+ " WHERE ("
        		+ "(" + TCSTableMilestones.lid + " = " + get_slid() + ")"
        	+ ")"
        ;
		
		try {
			ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn); 
			if(rs.next()){
				m_sName = rs.getString(TCSTableMilestones.sName);
				m_sDescription = rs.getString(TCSTableMilestones.sDescription);
				m_sEmployeeTypeID = rs.getString(TCSTableMilestones.sEmployeeTypeID);
			}else{
				throw new Exception("Error [1426653015] - " + ParamObjectName + " ID '" + this.get_slid() + "' does not exist.");
			}
			rs.close();
		}catch (SQLException ex){
			throw new Exception("Error [1489653016] loading " + ParamObjectName + " with lid '" + get_slid() + "' using SQL: " + SQL 
				+ " - " + ex.getMessage());
		}
	}
    public void save(ServletContext context, String sDBID, String sUserName, HttpServletRequest req ) throws Exception{
		
    	//Get connection
		Connection conn;
		try {
			conn = clsDatabaseFunctions.getConnectionWithException(
				context, 
				sDBID, 
				"MySQL", 
				this.toString() + ":save - user: " + sUserName
			);
		} catch (Exception e1) {
			throw new Exception("Error [1486653017] - could not get connection to save.");
		}
		
		//Validate entries
		try {
			validateEntries(conn);
		} catch (Exception e1) {
			clsDatabaseFunctions.freeConnection(context, conn, "[1547060158]");
			throw new Exception(e1.getMessage());
		}
		
		//Update the editable fields.
		String SQL = "";
		if (get_slid().compareToIgnoreCase("-1") == 0){
			SQL = "INSERT INTO " + TCSTableMilestones.TableName + "("
				+ TCSTableMilestones.sName
				+ ", " + TCSTableMilestones.sDescription
				+ ", " + TCSTableMilestones.sEmployeeTypeID
			+ ") VALUES ("
				+ "'" + clsDatabaseFunctions.FormatSQLStatement(get_sName()) + "'"
				+ ", '" + clsDatabaseFunctions.FormatSQLStatement(get_sDescription()) + "'"
				+ ", '" + clsDatabaseFunctions.FormatSQLStatement(get_sEmployeeTypeID()) + "'"
			+ ")"
			;
		}else{
			SQL =  "UPDATE " + TCSTableMilestones.TableName
				+ " SET " + TCSTableMilestones.sName + " = '"  + clsDatabaseFunctions.FormatSQLStatement(get_sName()) + "'"
				+ ", " + TCSTableMilestones.sDescription + " = '" + clsDatabaseFunctions.FormatSQLStatement(get_sDescription()) + "'"
				+ ", " + TCSTableMilestones.sEmployeeTypeID + " = '" + clsDatabaseFunctions.FormatSQLStatement(get_sEmployeeTypeID()) + "'"
				+ " WHERE ("
					+ "(" + TCSTableMilestones.lid + " = " + get_slid() + ")"
				+ ")"
			;
		}
		
		//Start a transaction:
		try {
			clsDatabaseFunctions.start_data_transaction(conn);
		} catch (Exception e1) {
			throw new Exception("Error [156741282] starting data transaction - " + e1.getMessage());
		}
		
		try {
	 		Statement stmt = conn.createStatement();
	 		stmt.executeUpdate(SQL);
	 	}catch (SQLException e){
	 		clsDatabaseFunctions.rollback_data_transaction(conn);
	 		clsDatabaseFunctions.freeConnection(context, conn, "[1547060159]");
	 		throw new Exception("Error [416653655] saving " + ParamObjectName + " record with SQL: '" + SQL + "' - " + e.getMessage());
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
				clsDatabaseFunctions.rollback_data_transaction(conn);
				clsDatabaseFunctions.freeConnection(context, conn, "[1547060160]");
				throw new Exception("Error [486653656] - record was saved but the ID is incorrect");
			}
	 	}
	 	
	 	clsDatabaseFunctions.commit_data_transaction(conn);
	 	clsDatabaseFunctions.freeConnection(context, conn, "[1547060161]");
    }
    
    private void validateEntries(Connection conn) throws Exception{
    	
    	String s = "";
    	
    	try {
    		m_sName = clsValidateFormFields.validateStringField(m_sName, TCSTableMilestones.sNameLength, "Milestone name", false);
		} catch (Exception e1) {
			s += e1.getMessage() + "\n";
		}
      	try {
      		m_sDescription = clsValidateFormFields.validateStringField(m_sDescription, Integer.MAX_VALUE, "Milestone description", false);
		} catch (Exception e1) {
			s += e1.getMessage() + "\n";
		}
      	
      	try {
      		if(m_sEmployeeTypeID.compareToIgnoreCase("0") == 0) {
      			s+="Milestone must be assigned to an employee type.";
      		}
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
    	clsDatabaseFunctions.freeConnection(context, conn, "[1547060156]");
    	return;
    	
    }
	public void delete(String slid, Connection conn) throws Exception{
		
		try {
			clsDatabaseFunctions.start_data_transaction(conn);
		} catch (Exception e1) {
			throw new Exception("Error [1486654740] starting data transaction to delete - " + e1.getMessage());
		}
		

		//delete the type:
		String SQL = "DELETE FROM " + TCSTableMilestones.TableName
			+ " WHERE ("
				+ "(" + TCSTableMilestones.lid + " = " + get_slid() + ")"
			+ ")"
		;
		try {
			Statement stmt = conn.createStatement();
			stmt.execute(SQL);
		} catch (Exception e) {
			clsDatabaseFunctions.rollback_data_transaction(conn);
			throw new Exception("Error [1486654354] deleting " + ParamObjectName + " with ID '" 
				+ get_slid() + " - " + e.getMessage());
		}
		
		try {
			clsDatabaseFunctions.commit_data_transaction(conn);
		} catch (Exception e1) {
			clsDatabaseFunctions.rollback_data_transaction(conn);
			throw new Exception("Error [1486654741] committing data transaction to delete - " + e1.getMessage());
		}
	}
	public String get_sName() {
		return m_sName;
	}
	public void set_sName(String sName) {
		m_sName = sName;
	}
	public String get_sDescription() {
		return m_sDescription;
	}
	public void set_sDescription(String sDescription) {
		m_sDescription = sDescription;
	}
	public String get_sEmployeeTypeID() {
		return m_sEmployeeTypeID;
	}
	public void set_sEmployeeTypeID(String sEmployeeTypeID) {
		m_sEmployeeTypeID = sEmployeeTypeID;
	}

	public String get_slid() {
		return m_slid;
	}
	public void set_slid(String slid) {
		m_slid = slid;
	}

	private void initializeVariables (){
		m_slid = "-1";
		m_sName = "";
		m_sDescription = "";
		m_sEmployeeTypeID = "0";
	}
}
