package smap;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import SMDataDefinition.SMTableap1099cprscodes;
import SMDataDefinition.SMTableapdistributioncodes;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsManageRequestParameters;
import smcontrolpanel.SMUtilities;

public class APDistributionCode extends java.lang.Object{
	
	public static final String ParamObjectName = "Distribution Code";
	
	public static final String Paramlid = "lid";
	public static final String Paramsdistcodename = "sdistcodename";
	public static final String Paramsdescription = "sdescription";
	public static final String Paramsglacct = "sglacct";
	public static final String Paramidiscountable = "idiscountable";
	
	public static final String ParamsNewRecord = "sNewRecord";
	public static final String FORM_NAME = "MAINFORM";
	
	private String m_lid;
	private String m_sdistcodename;
	private String m_sdescription;
	private String m_idiscountable;
	private String m_sglacct;
	private String m_sNewRecord;
	
	private ArrayList<String> m_sErrorMessageArray = new ArrayList<String> (0);
	
	public APDistributionCode(){
		m_lid = "0";
		m_sdistcodename = "";
		m_sdescription = "";
		m_idiscountable = "0";
		m_sglacct = "";
		m_sNewRecord = "";
		
		m_sErrorMessageArray = new ArrayList<String> (0);
	}
	
	public APDistributionCode (HttpServletRequest req){

		m_lid = clsManageRequestParameters.get_Request_Parameter(APDistributionCode.Paramlid, req).trim();
		m_sdistcodename = clsManageRequestParameters.get_Request_Parameter(APDistributionCode.Paramsdistcodename, req).trim();
		m_sdescription = clsManageRequestParameters.get_Request_Parameter(APDistributionCode.Paramsdescription, req).trim();	
		m_sglacct = clsManageRequestParameters.get_Request_Parameter(APDistributionCode.Paramsglacct, req).trim();	
		if (req.getParameter(APDistributionCode.Paramidiscountable) == null){
			m_idiscountable = "0";
		}else{
			m_idiscountable = "1";
		}
		m_sNewRecord = clsManageRequestParameters.get_Request_Parameter(APDistributionCode.ParamsNewRecord, req).trim();
		m_sErrorMessageArray = new ArrayList<String> (0);
	}
	
    public boolean load (Connection conn){
        	
    	String SQL = "SELECT * FROM " + SMTableapdistributioncodes.TableName
				+ " WHERE ("
				+ SMTableapdistributioncodes.lid + " = " + m_lid 
			+	")";
        	try {
        		ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
        		if (rs.next()){
        			m_lid = Long.toString(rs.getLong(SMTableapdistributioncodes.lid));
        			m_sdistcodename = rs.getString(SMTableapdistributioncodes.sdistcodename);		
        			m_sdescription = rs.getString(SMTableapdistributioncodes.sdescription);
        			m_idiscountable =  Long.toString(rs.getLong(SMTableapdistributioncodes.idiscountable));
        			m_sglacct = rs.getString(SMTableapdistributioncodes.sglacct);
        			m_sNewRecord = "0";
        			rs.close();
        			return true;
        		}else{
        		rs.close();
        		setNewRecord("1");
        		return true;
        		}
        	}catch (SQLException e){
        		m_sErrorMessageArray.add("SQL Error reading Distribution Codes record: " + e.getMessage());
        		return false;
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
    		throw new Exception("Error getting connection to load APDistributionCodes - " + e.getMessage());
    	}
    	if (conn == null){
    		throw new Exception("Counld not get connection to load APDistributionCodes.");
    	}
    	if (!load(conn)){
    		clsDatabaseFunctions.freeConnection(context, conn);
    		throw new Exception("Error loading APDistributionCodes - " + getErrorMessages());
    	}
    	clsDatabaseFunctions.freeConnection(context, conn);
    }
    public void loadByDistributionCode(
    	Connection conn, 
    	String sUserID, 
    	String sDistributionCode) throws Exception{
    	
    	String SQL = "SELECT * FROM " + SMTableapdistributioncodes.TableName
				+ " WHERE ("
				+ SMTableapdistributioncodes.sdistcodename + " = '" + sDistributionCode + "'" 
			+	")";
    	try {
    		ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
    		if (rs.next()){
    			m_lid = Long.toString(rs.getLong(SMTableapdistributioncodes.lid));
    			m_sdistcodename = rs.getString(SMTableapdistributioncodes.sdistcodename);		
    			m_sdescription = rs.getString(SMTableapdistributioncodes.sdescription);
    			m_idiscountable =  Long.toString(rs.getLong(SMTableapdistributioncodes.idiscountable));
    			m_sglacct = rs.getString(SMTableapdistributioncodes.sglacct);
    			m_sNewRecord = "0";
    			rs.close();
    		}else{
    		rs.close();
    		setNewRecord("1");
    		throw new Exception("Error [1490965746] - could not load a distribution code named '" + sDistributionCode + "'.");
    		}
    	}catch (SQLException e){
    		throw new Exception("Error [1490965747] reading Distribution Codes record - " + e.getMessage());
    	}
    }

    public boolean saveEditableFields(ServletContext context, String sConf, String sUserID, String sUserFullName){
    	m_sErrorMessageArray.clear();
		
    	//Get connection
		Connection conn = clsDatabaseFunctions.getConnection(
			context, 
			sConf, 
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
		
		//Validate entries
		if(!validateEntries()){
			clsDatabaseFunctions.freeConnection(context, conn);
			return false;
		}
		
		//Check to see if the record already exists:
		String SQL = "SELECT * FROM " + SMTableapdistributioncodes.TableName
			+ " WHERE ("
				+ SMTableapdistributioncodes.lid + " = " + m_lid
			+ ")"
			;
		try{
			ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
			
			if(rs.next()){

				rs.close();
				
				m_sErrorMessageArray.clear();
				
    	//Only re-sets the fields that can be edited from the 'Edit Options' screen:
		 SQL = "UPDATE " + SMTableapdistributioncodes.TableName
		+ " SET "
			+ SMTableapdistributioncodes.sdistcodename + " = '" +  clsDatabaseFunctions.FormatSQLStatement(m_sdistcodename) + "'"
		+ ", " + SMTableapdistributioncodes.sdescription + " = '" + clsDatabaseFunctions.FormatSQLStatement(m_sdescription) + "'"
		+ ", " + SMTableapdistributioncodes.idiscountable + " = " + clsDatabaseFunctions.FormatSQLStatement(m_idiscountable)
		+ ", " + SMTableapdistributioncodes.sglacct + " = '" + clsDatabaseFunctions.FormatSQLStatement(m_sglacct) + "'"
		+ " WHERE ("
						+ SMTableap1099cprscodes.lid + " = " + m_lid
					+ ")"
		;

		 	try {
		 		Statement stmt = conn.createStatement();
		 		stmt.executeUpdate(SQL);
		 		m_sNewRecord = "0";
		 	}catch (SQLException e){
		 		clsDatabaseFunctions.freeConnection(context, conn);
		 		m_sErrorMessageArray.add("Error updating record with SQL: " + " - " + e.getMessage());
		 		return false;
		}
		}else{
			
			rs.close();
			
			if(!validateEntries()){
				clsDatabaseFunctions.freeConnection(context, conn);
				return false;
			}
			SQL = "INSERT INTO " + SMTableapdistributioncodes.TableName + " ("
					+ SMTableapdistributioncodes.sdistcodename
					+ ", " + SMTableapdistributioncodes.sdescription
					+ ", " + SMTableapdistributioncodes.idiscountable
					+ ", " + SMTableapdistributioncodes.sglacct
					+ " ) VALUES ("
						+ "'" + clsDatabaseFunctions.FormatSQLStatement(m_sdistcodename) + "'" 
						+ ", '" + clsDatabaseFunctions.FormatSQLStatement(m_sdescription) + "'" 
						+ ", " + m_idiscountable
						+ ", '" + clsDatabaseFunctions.FormatSQLStatement(m_sglacct) + "'"
					+ ")"
					;
			try {
				clsDatabaseFunctions.executeSQL(SQL, conn);
				m_sNewRecord = "0";
			}catch (SQLException e){
		 		clsDatabaseFunctions.freeConnection(context, conn);
		 		m_sErrorMessageArray.add("Error inserting record with SQL: " +  " - " + e.getMessage());
		 		m_sNewRecord = "1";
		 		return false;
			}
				
				//Update the ID if it's an insert successful:
				SQL = "SELECT last_insert_id()";
				try {
					ResultSet srs = clsDatabaseFunctions.openResultSet(SQL, conn);
					if (srs.next()) {
						m_lid = Long.toString(srs.getLong(1));
					}else {
						m_lid = "";
					}
					rs.close();
				} catch (SQLException e) {
					clsDatabaseFunctions.freeConnection(context, conn);
					m_sErrorMessageArray.add("Could not get last ID number - " + e.getMessage());
				}
				//If something went wrong, we can't get the last ID:
				if (m_lid.compareToIgnoreCase("") == 0){
					m_sErrorMessageArray.add("Could not get last ID number.");
				}
	
			//Change new record status
			clsDatabaseFunctions.freeConnection(context, conn);

			return true;			
    	}
			}catch(SQLException e){
				clsDatabaseFunctions.freeConnection(context, conn);
				m_sErrorMessageArray.add("Error [1450810035] saving  - " + e.getMessage());
				return false;
			}
		return true;
    }
    
public boolean delete(String sDistributionCode, Connection conn){
		
		m_sErrorMessageArray.clear();
		
		//First, check that the 1099/CPRS Code exists:
		String SQL = "SELECT * FROM " + SMTableapdistributioncodes.TableName
			+ " WHERE ("
				+ SMTableapdistributioncodes.lid + " = " + m_lid 
			+ ")"
		;
		try{
			ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
			if(!rs.next()){
				m_sErrorMessageArray.add("Distribution Code " + sDistributionCode + " cannot be found.");
				rs.close();
				return false;
			}else{
				rs.close();
			}
			
		}catch(SQLException e){
			m_sErrorMessageArray.add("Error [1450810036] checking Distribution Code " + sDistributionCode + " to delete - " + e.getMessage());
			return false;
		}
		
		try{
			SQL = "DELETE FROM " + SMTableapdistributioncodes.TableName
				+ " WHERE ("
					+ SMTableapdistributioncodes.lid + " = " + m_lid
				+ ")"
			;
			if(!clsDatabaseFunctions.executeSQL(SQL, conn)){
				m_sErrorMessageArray.add("Error [1450810038] deleting Distribution Code " + sDistributionCode);
				clsDatabaseFunctions.rollback_data_transaction(conn);
				return false;
			}


		}catch(SQLException e){
			m_sErrorMessageArray.add("Error [1450810039] deleting Distribution Code " + sDistributionCode + " - " + e.getMessage());
			return false;
		}
		
		
		return true;
	}

private boolean validateEntries(){
	
	boolean bEntriesAreValid = true;
	m_sErrorMessageArray.clear();

	if (m_sdistcodename.length() > SMTableapdistributioncodes.sdistcodenamelength){
		m_sErrorMessageArray.add("Code name cannot be longer than  " 
			+ SMTableapdistributioncodes.sdistcodenamelength + " characters.");
		bEntriesAreValid = false;
	}
	if (m_sdistcodename.trim().compareToIgnoreCase("") == 0){
		m_sErrorMessageArray.add("Code name ID cannot be blank.");
		bEntriesAreValid = false;
	}
	
	if (m_sdescription.length() > SMTableapdistributioncodes.sdescriptionlength){
		m_sErrorMessageArray.add("Description cannot be longer than " 
			+ SMTableapdistributioncodes.sdescriptionlength + " characters.");
		bEntriesAreValid = false;
	}
	if (m_sdescription.trim().compareToIgnoreCase("") == 0){
		m_sErrorMessageArray.add("Description cannot be blank.");
		bEntriesAreValid = false;
	}
	
	if (m_sglacct.length() > SMTableapdistributioncodes.sglacctlength){
		m_sErrorMessageArray.add("GL account cannot be longer than " 
			+ SMTableapdistributioncodes.sglacctlength + " characters.");
		bEntriesAreValid = false;
	}
	if (m_sglacct.trim().compareToIgnoreCase("") == 0){
		m_sErrorMessageArray.add("GL account cannot be blank.");
		bEntriesAreValid = false;
	}

	return bEntriesAreValid;
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
	public String getlid() {
		return m_lid;
	}
	public void setlid(String lid) {
		m_lid = lid;
	}
	public String getsdistcodename() {
		return m_sdistcodename;
	}
	public void setsdistcodename(String sdistcodename) {
		m_sdistcodename = sdistcodename;
	}
	public String getsdescription() {
		return m_sdescription;
	}
	public void setsdescription(String sdescription) {
		m_sdescription = sdescription;
	}
	public String getidiscountable() {
		return m_idiscountable;
	}
	public void setidiscountable(String idiscountable) {
		m_idiscountable = idiscountable;
	}
	public String getsglacct() {
		return m_sglacct;
	}	
	public void setsglacct(String sglacct) {
		m_sglacct = sglacct;
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