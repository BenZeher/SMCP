package SMClasses;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Enumeration;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import SMDataDefinition.SMTableappointmentgroups;
import SMDataDefinition.SMTableappointmentusergroups;
import ServletUtilities.clsMasterEntry;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsManageRequestParameters;

public class SMAppointmentCalendarGroup extends clsMasterEntry{
	
	public static final String ParamObjectName = "Appointment calendar group";
	public static final String UPDATE_USER_MARKER = "User***Update";
	
	private String m_igroupid;
	private String m_sappointmentgroupname;
	private String m_sappointmentgroupdesc;
	
	private boolean bDebugMode = false;
	
    public SMAppointmentCalendarGroup() {
		super();
		initAppointmentGroupVariables();
        }
    
    public SMAppointmentCalendarGroup(HttpServletRequest req){
		super(req);
		initAppointmentGroupVariables();
		m_igroupid = clsManageRequestParameters.get_Request_Parameter(
				SMTableappointmentgroups.igroupid, req).trim();
		m_sappointmentgroupname = clsManageRequestParameters.get_Request_Parameter(
				SMTableappointmentgroups.sappointmentgroupname, req).trim();
		m_sappointmentgroupdesc  = clsManageRequestParameters.get_Request_Parameter(
				SMTableappointmentgroups.sappointmentgroupdesc, req).trim().replace("&quot;", "\"");
    }
    public void load (ServletContext context, String sDBIB, String sUserID, String sUserFullName) throws Exception{
    	Connection conn = clsDatabaseFunctions.getConnection(
    			context, 
    			sDBIB, 
    			"MySQL", 
    			this.toString() + " - user: " + sUserID + " - " + sUserFullName
    			);
    	
    	if (conn == null){
    		throw new Exception("Error [1469646846] opening data connection to load " + ParamObjectName + ".");
    	}
    	
    	try {
			load (conn);
		} catch (Exception e) {
			clsDatabaseFunctions.freeConnection(context, conn, "[1547067674]");
			throw new Exception(e.getMessage());
		}
    	clsDatabaseFunctions.freeConnection(context, conn, "[1547067675]");
    }
    public void load (Connection conn) throws Exception{
    	load (m_igroupid, conn);
    }
    private void load (String sID, Connection conn) throws Exception{

    	sID = sID.trim();
    	if (sID.compareToIgnoreCase("") == 0){
    		throw new Exception("Group ID cannot be blank when loading " + ParamObjectName + ".");
    	}
		
		String SQL = "SELECT * FROM " + SMTableappointmentgroups.TableName
			+ " WHERE ("
				+ SMTableappointmentgroups.igroupid + " = " + sID
			+ ")";
		if (bDebugMode){
			System.out.println("[1579185903] In " + this.toString() + " - load SQL = " + SQL);
		}
		try {
			ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
			if (rs.next()) {
				//Load the variables here:
				m_igroupid = Long.toString(rs.getLong(SMTableappointmentgroups.igroupid));
				m_sappointmentgroupname = rs.getString(SMTableappointmentgroups.sappointmentgroupname).trim();
				m_sappointmentgroupdesc = rs.getString(SMTableappointmentgroups.sappointmentgroupdesc).trim();
				rs.close();
			} else {
				rs.close();
				throw new Exception(ParamObjectName + " with ID '" + sID + "' was not found.");
			}
		} catch (Exception e) {
			throw new Exception("Error [1469734864] reading " + ParamObjectName + " for lid : '" + sID
					+ "' - " + e.getMessage());
		}
		return;
    }
    
    public void save_without_data_transaction (ServletContext context, String sDBIB, String sUser, String sUserID, String sUserFullName,  HttpServletRequest request) throws Exception{
    	
       	Connection conn = clsDatabaseFunctions.getConnection(
    			context, 
    			sDBIB, 
    			"MySQL", 
    			this.toString() + " - user: " + sUserID + " - " + sUserFullName
    			);
    	
    	if (conn == null){
    		throw new Exception("Error [1458864729] opening data connection.");
    	}
    	
    	try {
			save_without_data_transaction (conn, context, sDBIB, request);
		} catch (Exception e) {
			clsDatabaseFunctions.freeConnection(context, conn, "[1547067676]");
			throw new Exception(e.getMessage());
		}
    	clsDatabaseFunctions.freeConnection(context, conn, "[1547067677]");
    	
    }
    public void save_without_data_transaction (Connection conn, ServletContext context, String sDBIB, HttpServletRequest request) throws Exception{

    	try {
			validate_entry_fields(conn);
		} catch (Exception e1) {
			throw new Exception (e1.getMessage());
		}

    	 String sOrginalGroupName = getsappointmentgroupname();
    	 String sScheduleID = getigroupid();
    	 
    	 //If this is NOT a new appointment group get the original group name in case it has been edited.
    	 if(sScheduleID.compareToIgnoreCase("-1") != 0){
 
    	 String sSQL = "SELECT " +  SMTableappointmentgroups.sappointmentgroupname
    			 + " FROM " + SMTableappointmentgroups.TableName
    			 + " WHERE (" + SMTableappointmentgroups.igroupid + " = " + getigroupid() + ")";
    	 ResultSet rs = clsDatabaseFunctions.openResultSet(sSQL, context, sDBIB);
    	 
    	 	if(rs.next()){
    	 		sOrginalGroupName = rs.getString(SMTableappointmentgroups.sappointmentgroupname);
    	 	}
    	 	rs.close();
    	 
    	 }
    	 
    	 ArrayList<String> sSQLList = new ArrayList<String>(0);
 	    

 	    
 	    //Next, delete all AppointmentUserGroups records:
 	    sSQLList.add("DELETE FROM " + SMTableappointmentusergroups.TableName
 	    		+ " WHERE( " + SMTableappointmentusergroups.sappointmentgroupname + "='" + sOrginalGroupName + "')"
 	    		);
 	    
 	    //Add the ScheduleUserGroup records:
 	    Enumeration<?> paramNames = request.getParameterNames();
    		while(paramNames.hasMoreElements()) {
    		  String sParamName = (String)paramNames.nextElement();

    		  if (sParamName.contains(UPDATE_USER_MARKER)){
    			  String sUserID = (sParamName.substring(sParamName.indexOf(UPDATE_USER_MARKER) + UPDATE_USER_MARKER.length()));

    			sSQLList.add( "INSERT INTO " + SMTableappointmentusergroups.TableName + " ("
    		  					+ " " + SMTableappointmentusergroups.sappointmentgroupname
    		  					+ ", " + SMTableappointmentusergroups.luserid
    		  					+ ") VALUES ("
    		  					+ " '" + clsDatabaseFunctions.FormatSQLStatement(getsappointmentgroupname().trim()) + "'"
    		  					+ ", " + sUserID + ""
    		  					+ ")");
    		   }
    		 }
    			  
    		    try{
    		    	if (clsDatabaseFunctions.executeSQLsInTransaction(sSQLList, context, sDBIB) == false){
    		    		throw new Exception("Could not complete update transaction - group was not updated.<BR>");
    		    	}
    		    }catch (SQLException ex){
    				System.out.println("[1579185912] Error in SMUtilities.commitTransaction class!!");
    			    System.out.println("SQLException: " + ex.getMessage());
    			    System.out.println("SQLState: " + ex.getSQLState());
    			    System.out.println("SQL: " + ex.getErrorCode());
    			}
    		    
    	 	    //Last, update the appointment group and id if its a new entry:
    	 	   String SQL = "INSERT INTO " + SMTableappointmentgroups.TableName + " ("
    	 				+ " " + SMTableappointmentgroups.sappointmentgroupname
    	 				+ ", " + SMTableappointmentgroups.sappointmentgroupdesc
    	 				+ ") VALUES ("
    	 				+ " '" + clsDatabaseFunctions.FormatSQLStatement(getsappointmentgroupname().trim()) + "'"
    	 				+ ", '" + clsDatabaseFunctions.FormatSQLStatement(getsappointmentgroupdesc().trim()) + "'"
    	 				+ ")"
    	 				+ " ON DUPLICATE KEY UPDATE"
    	 				+ " " + SMTableappointmentgroups.sappointmentgroupname + " = '" + clsDatabaseFunctions.FormatSQLStatement(getsappointmentgroupname()) + "'"
    	 				+ ", " + SMTableappointmentgroups.sappointmentgroupdesc + " = '" + clsDatabaseFunctions.FormatSQLStatement(getsappointmentgroupdesc()) + "'"
    	 			;
    	    try{
    			 Statement stmt = conn.createStatement();
    			  stmt.executeUpdate(SQL);
    		}catch (Exception ex) {
    				throw new Exception ("Error [1469803812] in insert/update with SQL: " + SQL + " - " + ex.getMessage());
    			}	
		//Update the ID if it's an insert:
		if (m_igroupid.compareToIgnoreCase("-1") == 0){
			SQL = "SELECT last_insert_id()";
			try {
				ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
				if (rs.next()) {
					m_igroupid = Long.toString(rs.getLong(1));
				}else {
					m_igroupid = "0";
				}
				rs.close();
			} catch (SQLException e) {
				throw new Exception("Could not get last ID number - " + e.getMessage());
			}
			//If something went wrong, we can't get the last ID:
			if (m_igroupid.compareToIgnoreCase("0") == 0){
				throw new Exception("Could not get last ID number.");
			}
		}
    }

    public void delete (ServletContext context, String sDBIB, String sUser, String sUserID, String sUserFullName) throws Exception{
    	
    	Connection conn = clsDatabaseFunctions.getConnection(
    			context, 
    			sDBIB, 
    			"MySQL", 
    			this.toString() + " - user: " + sUserID + " " + sUserFullName
    			);
    	
    	if (conn == null){
    		throw new Exception ("Error [1469649014] opening data connection.");
    	}
    	
    	try {
			delete (conn);
		} catch (Exception e) {
			clsDatabaseFunctions.freeConnection(context, conn, "[1547067672]");
			throw new Exception(e.getMessage());
		}
    	clsDatabaseFunctions.freeConnection(context, conn, "[1547067673]");
    }
    public void delete (Connection conn) throws Exception{
    	
    	//Validate deletions
    	String SQL = "";
    	//Delete group record:
    	SQL =  " DELETE FROM " + SMTableappointmentgroups.TableName
    		+ " WHERE ("
    			+ "(" + SMTableappointmentgroups.igroupid + " = " + getigroupid() + ")"
    		+ ")"
    		;
    	try {
    	    Statement stmt = conn.createStatement();
 		    stmt.executeUpdate(SQL);
		} catch (SQLException e) {
			throw new Exception("Error [1469651267] deleting group record - " + e.getMessage());
		}
    	//Delete user group records:
    	if (!clsDatabaseFunctions.start_data_transaction(conn)){
    		throw new Exception("Error [1469651268] - Could not start transaction when deleting " + ParamObjectName + ".");
    	}
    	SQL = "DELETE FROM " + SMTableappointmentusergroups.TableName
    		+ " WHERE ("
    			+ SMTableappointmentusergroups.sappointmentgroupname + " = '" + getsappointmentgroupname()
    		+ "')"
    		;
		try{
		    Statement stmt = conn.createStatement();
		    stmt.executeUpdate(SQL);
		}catch (Exception ex) {
			clsDatabaseFunctions.rollback_data_transaction(conn);
			throw new Exception("Error [1469651269] - Could not delete schedule user group records " + getsappointmentgroupname() + " with SQL: " + SQL + " - " + ex.getMessage());
		}
		
		if (!clsDatabaseFunctions.commit_data_transaction(conn)){
			clsDatabaseFunctions.rollback_data_transaction(conn);
			throw new Exception("Error [1469651270] - Could not commit data transaction while deleting " + ParamObjectName + ".");
		}
		//Empty the values:
		initAppointmentGroupVariables();
    }
    
    public void validate_entry_fields (Connection conn) throws Exception{
        //Validate the entries here:
    	String sErrors = "";
    	m_igroupid = m_igroupid.trim();
    	if (m_igroupid.compareToIgnoreCase("") == 0){
    		m_igroupid = "-1";
    	}
    	try {
			@SuppressWarnings("unused")
			long lID = Long.parseLong(m_igroupid);
		} catch (Exception e) {
			throw new Exception("Invalid ID: '" + m_igroupid + "'.");
		}

    	m_sappointmentgroupname = m_sappointmentgroupname.trim();
        if (m_sappointmentgroupname.length() > SMTableappointmentgroups.sappointmentgroupnamelength){
        	sErrors += "Description cannot be more than " + Integer.toString(SMTableappointmentgroups.sappointmentgroupnamelength) + " characters.  ";
        }
        if (m_sappointmentgroupname.compareToIgnoreCase("") == 0){
        	sErrors += "Group name cannot be blank.  ";
        }
        
        m_sappointmentgroupdesc = m_sappointmentgroupdesc.trim();
        if (m_sappointmentgroupdesc.length() > SMTableappointmentgroups.sappointmentgroupdesclength){
        	sErrors += "Description cannot be more than " + Integer.toString(SMTableappointmentgroups.sappointmentgroupdesclength) + " characters.  ";
        }
        
        if (sErrors.compareToIgnoreCase("") != 0){
        	throw new Exception(sErrors);
        }
    }
 
	public String getigroupid() {
		return m_igroupid;
	}

	public void setigroupid(String igroupid) {
		m_igroupid = igroupid;
	}

	public String getsappointmentgroupname() {
		return m_sappointmentgroupname;
	}

	public void setsappointmentgroupname(String sappointmentgroupname) {
		m_sappointmentgroupname = sappointmentgroupname;
	}

	public String getsappointmentgroupdesc() {
		return m_sappointmentgroupdesc;
	}

	public void setsappointmentgroupdesc(String sappointmentgroupdesc) {
		m_sappointmentgroupdesc = sappointmentgroupdesc;
	}

	public String getObjectName(){
		return ParamObjectName;
	}
	
    private void initAppointmentGroupVariables(){
    	m_igroupid = "-1";
    	m_sappointmentgroupname = "";
    	m_sappointmentgroupdesc = "";
	}
}
