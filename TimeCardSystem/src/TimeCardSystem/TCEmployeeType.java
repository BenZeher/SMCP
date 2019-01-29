package TimeCardSystem;


import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Enumeration;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsManageRequestParameters;
import ServletUtilities.clsValidateFormFields;
import TCSDataDefinition.TCSTableEmployeeTypeAccess;
import TCSDataDefinition.TCSTableEmployeeTypeLinks;
import TCSDataDefinition.TCSTableEmployeeTypes;
import TCSDataDefinition.TCSTableMilestones;

public class TCEmployeeType extends java.lang.Object{

	public static final String ParamObjectName = "EmployeeType";
	public static final String EDIT_FORM_NAME = "EDITEMPLOYEETYPEFORM";
	
	private String m_slid;
	private String m_sName;
	private String m_sDescription;
	
	public TCEmployeeType(
        ) {
		initializeVariables();
    }
    public TCEmployeeType(HttpServletRequest req) {
    	initializeVariables();
    	m_slid = clsManageRequestParameters.get_Request_Parameter(TCSTableMilestones.lid, req).trim();
    	m_sName = clsManageRequestParameters.get_Request_Parameter(TCSTableMilestones.sName, req).trim();
    	m_sDescription = clsManageRequestParameters.get_Request_Parameter(TCSTableMilestones.sDescription, req).trim();
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
			throw new Exception("Error [1508953522] - could not get connection - " + e.getMessage());
		}
    	
    	try {
			load(conn);
		} catch (Exception e) {
			throw new Exception(e.getMessage());
		}
    			
    	clsDatabaseFunctions.freeConnection(context, conn, "[1547060123]");
    }
 
	public void load (
    		Connection conn
    		) throws Exception{
        String SQL = "SELECT * FROM " + TCSTableEmployeeTypes.TableName
        	+ " WHERE ("
        		+ "(" + TCSTableEmployeeTypes.lid + " = " + get_slid() + ")"
        	+ ")"
        ;
		
		try {
			ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn); 
			if(rs.next()){
				m_sName = rs.getString(TCSTableEmployeeTypes.sName);
				m_sDescription = rs.getString(TCSTableEmployeeTypes.sDescription);
			}else{
				throw new Exception("Error [1508953583] - " + ParamObjectName + " ID '" + this.get_slid() + "' does not exist.");
			}
			rs.close();
		}catch (SQLException ex){
			throw new Exception("Error [1508953584] loading " + ParamObjectName + " with lid '" + get_slid() + "' using SQL: " + SQL 
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
			throw new Exception("Error [1508953585] - could not get connection to save.");
		}
		
		//Validate entries
		try {
			validateEntries(conn);
		} catch (Exception e1) {
			clsDatabaseFunctions.freeConnection(context, conn, "[1547060124]");
			throw new Exception(e1.getMessage());
		}
		
		//Update the editable fields.
		String SQL = "";
		if (get_slid().compareToIgnoreCase("-1") == 0){
			SQL = "INSERT INTO " + TCSTableEmployeeTypes.TableName + "("
				+ TCSTableEmployeeTypes.sName
				+ ", " + TCSTableEmployeeTypes.sDescription
			+ ") VALUES ("
				+ "'" + clsDatabaseFunctions.FormatSQLStatement(get_sName()) + "'"
				+ ", '" + clsDatabaseFunctions.FormatSQLStatement(get_sDescription()) + "'"
			+ ")"
			;
		}else{
			SQL =  "UPDATE " + TCSTableEmployeeTypes.TableName
				+ " SET " + TCSTableEmployeeTypes.sName + " = '"  + clsDatabaseFunctions.FormatSQLStatement(get_sName()) + "'"
				+ ", " + TCSTableEmployeeTypes.sDescription + " = '" + clsDatabaseFunctions.FormatSQLStatement(get_sDescription()) + "'"
				+ " WHERE ("
					+ "(" + TCSTableEmployeeTypes.lid + " = " + get_slid() + ")"
				+ ")"
			;
		}
		
		//Start a transaction:
		try {
			clsDatabaseFunctions.start_data_transaction(conn);
		} catch (Exception e1) {
			throw new Exception("Error [1508953664] starting data transaction - " + e1.getMessage());
		}
		
		try {
	 		Statement stmt = conn.createStatement();
	 		stmt.executeUpdate(SQL);
	 	}catch (SQLException e){
	 		clsDatabaseFunctions.rollback_data_transaction(conn);
	 		clsDatabaseFunctions.freeConnection(context, conn, "[1547060125]");
	 		throw new Exception("Error [1508953664] saving " + ParamObjectName + " record with SQL: '" + SQL + "' - " + e.getMessage());
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
				clsDatabaseFunctions.freeConnection(context, conn, "[1547060126]");
				throw new Exception("Error [1508953668] - record was saved but the ID is incorrect");
			}
	 	}
	 	
	 	clsDatabaseFunctions.commit_data_transaction(conn);
	 	clsDatabaseFunctions.freeConnection(context, conn, "[1547060127]");
    }
    
    public void updateEmployeeTypeLinksTable(String sEmployeeTypeID, ServletContext context, String sDBID, HttpServletRequest req)throws Exception{
    	//First, start a transaction
	    ArrayList<String> sSQLList = new ArrayList<String>(0);
	    //First, delete all the SecurityGroupFunctions:
	    sSQLList.add(TimeCardSQLs.Delete_Employee_Type_Users_SQL(sEmployeeTypeID));
	    
	    
	    //Now add back in all the Users for this for this group:
		Enumeration<?> paramNames = req.getParameterNames();
	    String sUserMarker = TCEditEmployeeTypesEdit.UPDATE_USER_MARKER;
	    while(paramNames.hasMoreElements()) {
	      String sParamName = (String)paramNames.nextElement();

		  if (sParamName.contains(sUserMarker)){
			  String sUserID = (sParamName.substring(sParamName.indexOf(sUserMarker) + sUserMarker.length()));

			  //Now add an insert statement for each function:
			  sSQLList.add(TimeCardSQLs.Insert_Employee_Type_Users_SQL(sEmployeeTypeID, sUserID));
		  }
	    }
		  
	    try{
	    	if (clsDatabaseFunctions.executeSQLsInTransaction(sSQLList, context, sDBID) == false){
	    		throw new Exception("Could not complete update transaction - group was not updated.<BR>");
	    	}
	    }catch (SQLException ex){
	    	throw new Exception("Error in SMUtilities.commitTransaction class!!"
	    			+ "SQLException: " + ex.getMessage()
	    			+ "SQLState: " + ex.getSQLState()
	    			+ "SQL: " + ex.getErrorCode()
	    			);
		}
    	
    }
    
    public void updateEmployeeTypeAccessTable(String sEmployeeTypeID, ServletContext context, String sDBID, HttpServletRequest req)throws Exception{
    	//First, start a transaction
	    ArrayList<String> sSQLList = new ArrayList<String>(0);
	    //First, delete all the SecurityGroupFunctions:
	    sSQLList.add(TimeCardSQLs.Delete_Employee_Type_Access_Users_SQL(sEmployeeTypeID));
	    
	    
	    //Now add back in all the Users for this for this group:
		Enumeration<?> paramNames = req.getParameterNames();
	    String sUserAccessMarker = TCEditEmployeeTypesEdit.UPDATE_ACCESS_MARKER;
	    while(paramNames.hasMoreElements()) {
	      String sParamName = (String)paramNames.nextElement();

		  if (sParamName.contains(sUserAccessMarker)){

			  String sUserID = (sParamName.substring(sParamName.indexOf(sUserAccessMarker) + sUserAccessMarker.length()));
			  //Now add an insert statement for each function:
			  sSQLList.add(TimeCardSQLs.Insert_Employee_Type_Access_Users_SQL(sEmployeeTypeID, sUserID));
		  }
	    }
		  
	    try{
	    	if (clsDatabaseFunctions.executeSQLsInTransaction(sSQLList, context, sDBID) == false){
	    		throw new Exception("Could not complete update transaction - group was not updated.<BR>");
	    	}
	    }catch (SQLException ex){
	    	throw new Exception("Error in SMUtilities.commitTransaction class!!"
	    			+ "SQLException: " + ex.getMessage()
	    			+ "SQLState: " + ex.getSQLState()
	    			+ "SQL: " + ex.getErrorCode()
	    			);
		}
    	
    }
    
	private void validateEntries(Connection conn) throws Exception{
    	
    	String s = "";
    	
    	try {
    		m_sName = clsValidateFormFields.validateStringField(m_sName, TCSTableEmployeeTypes.sNameLength, "Milestone name", false);
		} catch (Exception e1) {
			s += e1.getMessage() + "\n";
		}
      	try {
      		m_sDescription = clsValidateFormFields.validateStringField(m_sDescription, Integer.MAX_VALUE, "Milestone description", false);
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
    	clsDatabaseFunctions.freeConnection(context, conn, "[1547060122]");
    	return;
    	
    }
	public void delete(String slid, Connection conn) throws Exception{
		
		try {
			clsDatabaseFunctions.start_data_transaction(conn);
		} catch (Exception e1) {
			throw new Exception("Error [1508953669] starting data transaction to delete - " + e1.getMessage());
		}		

		//delete the type:
		String SQL = "DELETE FROM " + TCSTableEmployeeTypes.TableName
			+ " WHERE ("
				+ "(" + TCSTableEmployeeTypes.lid + " = " + get_slid() + ")"
			+ ")"
		;
		try {
			Statement stmt = conn.createStatement();
			stmt.execute(SQL);
		} catch (Exception e) {
			clsDatabaseFunctions.rollback_data_transaction(conn);
			throw new Exception("Error [1511278890] deleting " + ParamObjectName + " with ID '" 
				+ get_slid() + " - " + e.getMessage());
		}
		
		//delete the All users in this type:
		 SQL = "DELETE FROM " + TCSTableEmployeeTypeLinks.TableName
			+ " WHERE ("
				+ "(" + TCSTableEmployeeTypeLinks.sEmployeeTypeID + " = '" + get_slid() + "')"
			+ ")"
		;
		try {
			Statement stmt = conn.createStatement();
			stmt.execute(SQL);
		} catch (Exception e) {
			clsDatabaseFunctions.rollback_data_transaction(conn);
			throw new Exception("Error [1511278891] deleting EmployeeTypeLink record with " + ParamObjectName + " ID '" 
				+ get_slid() + " - " + e.getMessage());
		}
		
		//delete the All users access records for this type:
		 SQL = "DELETE FROM " + TCSTableEmployeeTypeAccess.TableName
			+ " WHERE ("
				+ "(" + TCSTableEmployeeTypeAccess.sEmployeeTypeID + " = '" + get_slid() + "')"
			+ ")"
		;
		try {
			Statement stmt = conn.createStatement();
			stmt.execute(SQL);
		} catch (Exception e) {
			clsDatabaseFunctions.rollback_data_transaction(conn);
			throw new Exception("Error [1511278891] deleting EmployeeTypeAccess record with " + ParamObjectName + " ID '"
				+ get_slid() + " - " + e.getMessage());
		}
		try {
			clsDatabaseFunctions.commit_data_transaction(conn);
		} catch (Exception e1) {
			clsDatabaseFunctions.rollback_data_transaction(conn);
			throw new Exception("Error [1508953826] committing data transaction to delete - " + e1.getMessage());
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
	}
}
