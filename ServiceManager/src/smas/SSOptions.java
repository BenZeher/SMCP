package smas;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import SMDataDefinition.SMTablessoptions;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsManageRequestParameters;
import smcontrolpanel.SMUtilities;

public class SSOptions {

	private String m_strackuserlocation;
	private static final String OBJECT_NAME = "Security System Options";
	public SSOptions(HttpServletRequest req){
		if (clsManageRequestParameters.get_Request_Parameter(
			SMTablessoptions.itrackuserlocations, req).trim().compareToIgnoreCase("") == 0){
			m_strackuserlocation = "0";
		}else{
			m_strackuserlocation = "1";
		}
	}
	public SSOptions(){
		m_strackuserlocation = "0";
	}
    public void load (
    	Connection conn
    ) throws Exception{
    	String SQL = "SELECT * FROM " + SMTablessoptions.TableName;
    	try {
    		ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
    		if (rs.next()){
    			m_strackuserlocation = Long.toString(rs.getLong(SMTablessoptions.itrackuserlocations));
    			rs.close();
    		}else{
    			rs.close();
    			throw new Exception ("Error [1465315100] - could not load record from '" + SMTablessoptions.TableName + "'.");
    		}
    	}catch (SQLException e){
    		throw new Exception ("Error [1465315101] - could not '" + SMTablessoptions.TableName + "' table - " + e.getMessage());
    	}
	}
    
    public void load(ServletContext context, String sConf, String sUser) throws Exception{
    	Connection conn;
    	try {
    		conn = clsDatabaseFunctions.getConnectionWithException(context, 
    			   sConf, 
    			   "MySQL",
    			   SMUtilities.getFullClassName(this.toString()) + ".load - user: " + sUser);
    	} catch (Exception e) {
    		throw new Exception("Error [1465315102] getting connection to load SSOptions - " + e.getMessage());
    	}
    
    	if (conn == null){
    		throw new Exception("Error [1465315103] could not get connection to load SSOptions.");
    	}
    	try {
			load(conn);
		} catch (Exception e) {
			clsDatabaseFunctions.freeConnection(context, conn, "[1547067662]");
			throw new Exception("Error [1465315104] could not get connection to load SSOptions.");
		}
   		clsDatabaseFunctions.freeConnection(context, conn, "[1547067663]");
    }

    private void validate_entries() throws Exception{
    	
    	String sErrors = "";
    	if (
    		(getstrackuserlocation().compareToIgnoreCase("0") != 0)
    		&& (getstrackuserlocation().compareToIgnoreCase("1") != 0)
    	){
    		sErrors += "Track user location value ('" + getstrackuserlocation() + "') is not valid.  ";
    	}
    	
    	if (sErrors.compareToIgnoreCase("") != 0){
    		throw new Exception(sErrors);
    	}
    }
    public void save(ServletContext context, String sConf, String sUserName) throws Exception{
    	
    	validate_entries();
    	
    	//Note that this ASSUMES THERE IS ALREADY ONE RECORD IN THE TABLE....
		String SQL = "UPDATE " + SMTablessoptions.TableName
		+ " SET "
		+ SMTablessoptions.itrackuserlocations + " = " + getstrackuserlocation()
		;
    	try {
    		if(!clsDatabaseFunctions.executeSQL(SQL, 
    				context,
    				sConf,
    				"MySQL",
    				this.toString() + ".save - User: " + sUserName))
    			throw new Exception("Error [1465315105] - could not save record.");
		}catch (SQLException e){
			throw new Exception("Error [1465315106] - saving record - " + e.getMessage());
		}
    }
    
	public String getstrackuserlocation(){
		return m_strackuserlocation;
	}
	public void setstrackuserlocation(String sTrackUserLocation){
		m_strackuserlocation = sTrackUserLocation;
	}
	public String getobjectname(){
		return OBJECT_NAME;
	}
}

