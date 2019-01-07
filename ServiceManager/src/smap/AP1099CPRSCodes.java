package smap;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import SMDataDefinition.SMTableap1099cprscodes;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsManageBigDecimals;
import ServletUtilities.clsManageRequestParameters;
import smcontrolpanel.SMUtilities;

public class AP1099CPRSCodes extends java.lang.Object{
	
	public static final String ParamObjectName = "1099/CPRS Code";
	
	public static final String Paramlid = "lid";
	public static final String Paramsclassid = "sclassid";
	public static final String Paramsdescription = "sdescription";
	public static final String Paramiactive = "iactive";
	public static final String Parambdminimumreportingamt = "bdminimumreportingamt";
	
	public static final String ParamsNewRecord = "sNewRecord";
	public static final String FORM_NAME = "MAINFORM";
	
	private String m_lid;
	private String m_sclassid;
	private String m_sdescription;
	private String m_iactive;
	private String m_bdminimumreportingamt;
	private String m_sNewRecord;
	
	private ArrayList<String> m_sErrorMessageArray = new ArrayList<String> (0);
	

	public AP1099CPRSCodes(){
		m_lid = "0";
		m_sclassid = "";
		m_sdescription = "";
		m_iactive = "0";
		m_bdminimumreportingamt = "0.00";
		m_sNewRecord = "";
		
		m_sErrorMessageArray = new ArrayList<String> (0);
	}
	
	public AP1099CPRSCodes (HttpServletRequest req){

		m_lid = clsManageRequestParameters.get_Request_Parameter(AP1099CPRSCodes.Paramlid, req).trim();
		m_sclassid = clsManageRequestParameters.get_Request_Parameter(AP1099CPRSCodes.Paramsclassid, req).trim();
		m_sdescription = clsManageRequestParameters.get_Request_Parameter(AP1099CPRSCodes.Paramsdescription, req).trim();		
		if (req.getParameter(AP1099CPRSCodes.Paramiactive) == null){
			m_iactive = "0";
		}else{
			m_iactive = "1";
		}
		m_bdminimumreportingamt = clsManageRequestParameters.get_Request_Parameter(AP1099CPRSCodes.Parambdminimumreportingamt, req).trim();
		if(m_bdminimumreportingamt.compareToIgnoreCase("")==0){
			m_bdminimumreportingamt = "0.00";
		}
		m_sNewRecord = clsManageRequestParameters.get_Request_Parameter(AP1099CPRSCodes.ParamsNewRecord, req).trim();
		m_sErrorMessageArray = new ArrayList<String> (0);
	}
	
    public boolean load (Connection conn){
        	
    	String SQL = "SELECT * FROM " + SMTableap1099cprscodes.TableName
				+ " WHERE ("
				+ SMTableap1099cprscodes.lid + " = " + m_lid 
			+	")";
        	try {
        		ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
        		if (rs.next()){
        			m_lid = Long.toString(rs.getLong(SMTableap1099cprscodes.lid));
        			m_sclassid = rs.getString(SMTableap1099cprscodes.sclassid);		
        			m_sdescription = rs.getString(SMTableap1099cprscodes.sdescription);
        			m_iactive =  Long.toString(rs.getLong(SMTableap1099cprscodes.iactive));
        			m_bdminimumreportingamt = clsManageBigDecimals.BigDecimalToScaledFormattedString(
        				SMTableap1099cprscodes.bdminimumreportingamtscale, rs.getBigDecimal(
        				SMTableap1099cprscodes.bdminimumreportingamt));
        			m_sNewRecord = "0";
        			rs.close();
        			return true;
        		}else{
        		rs.close();
        		setNewRecord("1");
        		return true;
        		}
        	}catch (SQLException e){
        		m_sErrorMessageArray.add("SQL Error reading 1099 CPRS Codes record: " + e.getMessage());
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
    		throw new Exception("Error getting connection to load AP1099CPRSCodes - " + e.getMessage());
    	}
    	if (conn == null){
    		throw new Exception("Counld not get connection to load AP1099CPRSCodes.");
    	}
    	if (!load(conn)){
    		clsDatabaseFunctions.freeConnection(context, conn);
    		throw new Exception("Error loading AP1099CPRSCodes - " + getErrorMessages());
    	}
    	clsDatabaseFunctions.freeConnection(context, conn);
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
		String SQL = "SELECT * FROM " + SMTableap1099cprscodes.TableName
			+ " WHERE ("
				+ SMTableap1099cprscodes.lid + " = " + m_lid
			+ ")"
			;
		try{
			ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
			
			if(rs.next()){

				rs.close();
				
				m_sErrorMessageArray.clear();
		
		//Update the editable fields. 		
		 SQL = "UPDATE " + SMTableap1099cprscodes.TableName
		+ " SET "
			+ SMTableap1099cprscodes.sclassid + " = '" +  clsDatabaseFunctions.FormatSQLStatement(m_sclassid) + "'"
		+ ", " + SMTableap1099cprscodes.sdescription + " = '" + clsDatabaseFunctions.FormatSQLStatement(m_sdescription) + "'"
		+ ", " + SMTableap1099cprscodes.iactive + " = " + clsDatabaseFunctions.FormatSQLStatement(m_iactive)
		+ ", " + SMTableap1099cprscodes.bdminimumreportingamt + " = " + clsDatabaseFunctions.FormatSQLStatement(m_bdminimumreportingamt)
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
			SQL = "INSERT INTO " + SMTableap1099cprscodes.TableName + " ("
					+ SMTableap1099cprscodes.sclassid
					+ ", " + SMTableap1099cprscodes.sdescription
					+ ", " + SMTableap1099cprscodes.iactive
					+ ", " + SMTableap1099cprscodes.bdminimumreportingamt
					+ " ) VALUES ("
						+ "'" + clsDatabaseFunctions.FormatSQLStatement(m_sclassid) + "'" 
						+ ", '" + clsDatabaseFunctions.FormatSQLStatement(m_sdescription) + "'" 
						+ ", " + m_iactive
						+ ", " + m_bdminimumreportingamt
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
				m_sErrorMessageArray.add("Error [1450713294] saving  - " + e.getMessage());
				return false;
			}
		return true;
    }
    
public boolean delete(String s1099CPRSCode, Connection conn){
		
		m_sErrorMessageArray.clear();
		
		//First, check that the 1099/CPRS Code exists:
		String SQL = "SELECT * FROM " + SMTableap1099cprscodes.TableName
			+ " WHERE ("
				+ SMTableap1099cprscodes.lid + " = " + m_lid 
			+ ")"
		;
		System.out.println(SQL);
		try{
			ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
			if(!rs.next()){
				m_sErrorMessageArray.add("1099/CPRS Code " + s1099CPRSCode + " cannot be found.");
				rs.close();
				return false;
			}else{
				rs.close();
			}
			
		}catch(SQLException e){
			m_sErrorMessageArray.add("Error [1450713560] checking 1099/CPRS Code " + s1099CPRSCode + " to delete - " + e.getMessage());
			return false;
		}

		//Now begin a data transaction that we can roll back:
		if(!clsDatabaseFunctions.start_data_transaction(conn)){
			m_sErrorMessageArray.add("Error [1450713561] beginning data transaction to delete  " + s1099CPRSCode + "");
			return false;
		}
		
		try{
			SQL = "DELETE FROM " + SMTableap1099cprscodes.TableName
				+ " WHERE ("
					+ SMTableap1099cprscodes.lid + " = " + m_lid
				+ ")"
			;

			if(!clsDatabaseFunctions.executeSQL(SQL, conn)){
				m_sErrorMessageArray.add("Error [1450713561] deleting 1099/CPRS Code " + s1099CPRSCode);
				clsDatabaseFunctions.rollback_data_transaction(conn);
				return false;
			}


		}catch(SQLException e){
			m_sErrorMessageArray.add("Error [1450713562] deleting 1099/CPRS Code " + s1099CPRSCode + " - " + e.getMessage());
			return false;
		}

		if(!clsDatabaseFunctions.commit_data_transaction(conn)){
			clsDatabaseFunctions.rollback_data_transaction(conn);
			return false;
		}
		
		return true;
	}

private boolean validateEntries(){
	
	boolean bEntriesAreValid = true;
	m_sErrorMessageArray.clear();

	if (m_sclassid.length() > SMTableap1099cprscodes.sclassidlength){
		m_sErrorMessageArray.add("Class ID cannot be longer than  " 
			+ SMTableap1099cprscodes.sclassidlength + " characters.");
		bEntriesAreValid = false;
	}
	if (m_sclassid.trim().compareToIgnoreCase("") == 0){
		m_sErrorMessageArray.add("Class ID cannot be blank.");
		bEntriesAreValid = false;
	}
	
	if (m_sdescription.length() > SMTableap1099cprscodes.sdescriptionlength){
		m_sErrorMessageArray.add("Description cannot be longer than " 
			+ SMTableap1099cprscodes.sdescriptionlength + " characters.");
		bEntriesAreValid = false;
	}
	if (m_sdescription.trim().compareToIgnoreCase("") == 0){
		m_sErrorMessageArray.add("Description cannot be blank.");
		bEntriesAreValid = false;
	}
	
	m_bdminimumreportingamt = m_bdminimumreportingamt.trim().replace(",", "");
	 try{
         BigDecimal bdminimumreportingamt = new BigDecimal(m_bdminimumreportingamt);
      if (bdminimumreportingamt.compareTo(BigDecimal.ZERO) < 0){
    	  m_sErrorMessageArray.add("Minimum reporting amount must be a positive number: " + m_bdminimumreportingamt + ".  ");
    	  bEntriesAreValid = false;
      }
        }catch (NumberFormatException e){
        	 m_sErrorMessageArray.add("Invalid minimum reporting amount: '" + m_bdminimumreportingamt + "'.  ");
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
	public String getsclassid() {
		return m_sclassid;
	}
	public void setsclassid(String sclassid) {
		m_sclassid = sclassid;
	}
	public String getsdescription() {
		return m_sdescription;
	}
	public void setsdescription(String sdescription) {
		m_sdescription = sdescription;
	}
	public String getiactive() {
		return m_iactive;
	}
	public void setiactive(String iactive) {
		m_iactive = iactive;
	}
	public String getbdminimumreportingamt() {
		return m_bdminimumreportingamt;
	}	
	public void setbdminimumreportingamt(String bdminimumreportingamt) {
		m_bdminimumreportingamt = bdminimumreportingamt;
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