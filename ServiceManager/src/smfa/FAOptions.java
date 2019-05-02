package smfa;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import SMDataDefinition.SMTablefaoptions;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsManageRequestParameters;
import ServletUtilities.clsServletUtilities;
import smcontrolpanel.SMUtilities;

public class FAOptions extends java.lang.Object{
	
	public static final String ParamObjectName = "FA Options";
	public static final String Paramifeedgl = "ifeedgl";
	public static final String FORM_NAME = "MAINFORM";
	
	private String m_ifeedgl;
	
	private ArrayList<String> m_sErrorMessageArray = new ArrayList<String> (0);
	

	public FAOptions(){
		m_ifeedgl = "0";
		m_sErrorMessageArray = new ArrayList<String> (0);
	}
	
	public FAOptions (HttpServletRequest req){


		m_ifeedgl = clsManageRequestParameters.get_Request_Parameter(FAOptions.Paramifeedgl, req).trim();
		m_sErrorMessageArray = new ArrayList<String> (0);
	}

    public boolean load (Connection conn){
        	
    	String SQL = "SELECT * FROM " + SMTablefaoptions.TableName;
        	try {
        		ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
        		if (rs.next()){
        			m_ifeedgl = Integer.toString(rs.getInt(SMTablefaoptions.ifeedgl));
        			rs.close();
        			return true;
        		}else{
        		rs.close();
        		FAOptions defaultFAOptions = new FAOptions();
        		SQL = "INSERT INTO " + SMTablefaoptions.TableName 
        			+ " (" +  SMTablefaoptions.ifeedgl
       				+ ") VALUES(" 
       				+ defaultFAOptions.m_ifeedgl + ""
       				+ ")"
        			;
        		try {
        				Statement stmt = conn.createStatement();
        				stmt.executeUpdate(SQL);
        			}catch (SQLException e){
        
        				m_sErrorMessageArray.add("Error updating record with SQL: " + SQL + " - " + e.getMessage());
        				return false;
        				}
        			return true;
        		}
        	}catch (SQLException e){
        		m_sErrorMessageArray.add("SQL Error reading FA options record: " + e.getMessage());
        		return false;
        	}
    	}
    
    public void load(String sDBID, ServletContext context, String sUserID) throws Exception{
    	Connection conn;
    	try {
    		conn = clsDatabaseFunctions.getConnectionWithException(context, 
    			   sDBID, 
    			   "MySQL",
    			   SMUtilities.getFullClassName(this.toString()) + ".load - userID: " + sUserID);
    	} catch (Exception e) {
    		throw new Exception("Error getting connection to load FA Options - " + e.getMessage());
    	}
    	if (conn == null){
    		throw new Exception("Could not get connection to load FA Options.");
    	}
    	if (!load(conn)){
    		clsDatabaseFunctions.freeConnection(context, conn, "[1556816653]");
    		throw new Exception("Error loading FA Options - " + getErrorMessages());
    	}
    	clsDatabaseFunctions.freeConnection(context, conn, "[1556816654]");
    }
    
    public boolean saveEditableFields(ServletContext context, String sDBID, String sUserID, String sUserFullName){
    	
    	//Only re-sets the fields that can be edited from the 'Edit Options' screen:
		String SQL = "UPDATE " + SMTablefaoptions.TableName
		+ " SET "
		+ SMTablefaoptions.ifeedgl + " = " + m_ifeedgl
		;
		
		Connection conn = clsDatabaseFunctions.getConnection(
			context, 
			sDBID, 
			"MySQL", 
			SMUtilities.getFullClassName(this.toString()) 
			+ ":saveEditableFields - user: " 
			+ sUserID
			+ " - "
			+ sUserFullName
		);
		if (conn == null){
			m_sErrorMessageArray.add("Error getting data connection.");
			return false;
		}
    	try {
    		Statement stmt = conn.createStatement();
		    stmt.executeUpdate(SQL);
		}catch (SQLException e){
			clsDatabaseFunctions.freeConnection(context, conn, "[1556816655]");
			m_sErrorMessageArray.add("Error updating record with SQL: " + SQL + " - " + e.getMessage());
			return false;
		}
		
		clsDatabaseFunctions.freeConnection(context, conn, "[1556816656]");
		
		return true;
    }
    
	public void clearErrorMessages(){
		m_sErrorMessageArray.clear();
	}
	public ArrayList<String> getErrorMessages(){
		return m_sErrorMessageArray;
	}
	public String getErrorMessageString(){
		String s = "";
		for (int i = 0; i < m_sErrorMessageArray.size(); i ++){
			s += "\n" + m_sErrorMessageArray.get(i);
		}
		return s;
	}
	
	public String getifeedgl(){
	    return m_ifeedgl;
	}
	public void setifeedgl(String sifeedgl){
		m_ifeedgl = sifeedgl;
	}

	public String getQueryString(){
	
		String sQueryString = "";
		sQueryString += "&" + Paramifeedgl + clsServletUtilities.URLEncode(m_ifeedgl);
		return sQueryString;
	}

}