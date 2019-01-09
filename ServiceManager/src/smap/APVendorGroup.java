package smap;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import SMDataDefinition.SMTableapvendorgroups;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsManageRequestParameters;
import smcontrolpanel.SMUtilities;

public class APVendorGroup extends java.lang.Object{
	
	public static final String ParamObjectName = "AP Vendor Group";
	
	public static final String Paramlid = "lid";
	public static final String Paramsgroupid = "sgroupid";
	public static final String Paramsdescription = "sdescription";
	public static final String Paramiactive = "iactive";
	public static final String Paramiapaccountset = "iapaccountset";
	public static final String Paramibankcode = "ibankcode";
	public static final String Paramstermscode = "stermscode";
	public static final String Paramiprintseparatechecks = "iprintseparatechecks";
	public static final String Paramidistributioncodeusedfordistribution = "idistributioncodeusedfordistribution";
	public static final String Paramsglacctusedfordistribution = "sglacctusedfordistribution";
	public static final String Paramidistributeby = "idistributeby";
	public static final String Paramstaxjurisdiction = "staxjurisdiction";
	public static final String Paramitaxtype = "itaxtype";
	public static final String Paramitaxreportingtype = "itaxreportingtype";
	public static final String Parami1099CPRScode = "i1099CPRScode";
	public static final String ParamsNewRecord = "sNewRecord";
	public static final String FORM_NAME = "MAINFORM";
	
	private String m_lid;
	private String m_sgroupid;
	private String m_sdescription;
	private String m_iactive;
	private String m_iapaccountset;
	private String m_ibankcode;
	private String m_stermscode;
	private String m_iprintseparatechecks;
	private String m_idistributioncodeusedfordistribution;
	private String m_sglacctusedfordistribution;
	private String m_idistributeby;
	private String m_staxjurisdiction;
	private String m_itaxtype;
	private String m_itaxreportingtype;
	private String m_i1099CPRScode;
	private String m_sNewRecord;
	
	private ArrayList<String> m_sErrorMessageArray = new ArrayList<String> (0);
	

	public APVendorGroup(){
		m_lid = "0";
		m_sgroupid = "";
		m_sdescription = "";
		m_iactive = "0";
		m_iapaccountset = "0";
		m_ibankcode = "0";
		m_stermscode = "";
		m_iprintseparatechecks = "0";
		m_idistributioncodeusedfordistribution = "0";
		m_sglacctusedfordistribution = "";
		m_idistributeby = "0";
		m_staxjurisdiction = "";
		m_itaxtype = "0";
		m_itaxreportingtype = "0";
		m_i1099CPRScode = "0";
		m_sNewRecord = "";
		
		m_sErrorMessageArray = new ArrayList<String> (0);
	}
	
	public APVendorGroup (HttpServletRequest req){

		m_lid = clsManageRequestParameters.get_Request_Parameter(APVendorGroup.Paramlid, req).trim();
		m_sgroupid = clsManageRequestParameters.get_Request_Parameter(APVendorGroup.Paramsgroupid, req).trim();
		m_sdescription = clsManageRequestParameters.get_Request_Parameter(APVendorGroup.Paramsdescription, req).trim();		
		if (req.getParameter(APVendorGroup.Paramiactive) == null){
			m_iactive = "0";
		}else{
			m_iactive = "1";
		}
		m_iapaccountset = clsManageRequestParameters.get_Request_Parameter(APVendorGroup.Paramiapaccountset, req).trim();
		if(m_iapaccountset.compareToIgnoreCase("")==0){
			m_iapaccountset = "0";
		}
		m_ibankcode = clsManageRequestParameters.get_Request_Parameter(APVendorGroup.Paramibankcode, req).trim();
		if(m_ibankcode.compareToIgnoreCase("")==0){
			m_ibankcode = "0";
		}
		m_stermscode = clsManageRequestParameters.get_Request_Parameter(APVendorGroup.Paramstermscode, req).trim();
		
		if (req.getParameter(APVendorGroup.Paramiprintseparatechecks) == null){
			m_iprintseparatechecks = "0";
		}else{
			m_iprintseparatechecks = "1";
		}
		
		m_idistributioncodeusedfordistribution = clsManageRequestParameters.get_Request_Parameter(APVendorGroup.Paramidistributioncodeusedfordistribution, req).trim();
		if(m_idistributioncodeusedfordistribution.compareToIgnoreCase("")==0){
			m_idistributioncodeusedfordistribution = "0";
		}		
		m_sglacctusedfordistribution = clsManageRequestParameters.get_Request_Parameter(APVendorGroup.Paramsglacctusedfordistribution, req).trim();
		m_idistributeby = clsManageRequestParameters.get_Request_Parameter(APVendorGroup.Paramidistributeby, req).trim();
		if(m_idistributeby.compareToIgnoreCase("")==0){
			m_idistributeby = "0";
		}	
		m_staxjurisdiction = clsManageRequestParameters.get_Request_Parameter(APVendorGroup.Paramstaxjurisdiction, req).trim();
		m_itaxtype = clsManageRequestParameters.get_Request_Parameter(APVendorGroup.Paramitaxtype, req).trim();
		if(m_itaxtype.compareToIgnoreCase("")==0){
			m_itaxtype = "0";
		}	
		m_itaxreportingtype = clsManageRequestParameters.get_Request_Parameter(APVendorGroup.Paramitaxreportingtype, req).trim();	
		if(m_itaxreportingtype.compareToIgnoreCase("")==0){
			m_itaxreportingtype = "0";
		}	
		m_i1099CPRScode = clsManageRequestParameters.get_Request_Parameter(APVendorGroup.Parami1099CPRScode, req).trim();
		if(m_i1099CPRScode.compareToIgnoreCase("")==0){
			m_i1099CPRScode = "0";
		}	
		m_sNewRecord = clsManageRequestParameters.get_Request_Parameter(APVendorGroup.ParamsNewRecord, req).trim();
		m_sErrorMessageArray = new ArrayList<String> (0);
	}
	
    public boolean load (Connection conn){
        	
    	String SQL = "SELECT * FROM " + SMTableapvendorgroups.TableName
				+ " WHERE ("
				+ SMTableapvendorgroups.lid + " = " + m_lid 
			+	")";
        	try {
        		ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
        		if (rs.next()){
        			m_lid = Long.toString(rs.getLong(SMTableapvendorgroups.lid));
        			m_sgroupid = rs.getString(SMTableapvendorgroups.sgroupid);		
        			m_sdescription = rs.getString(SMTableapvendorgroups.sdescription);
        			m_iactive =  Long.toString(rs.getLong(SMTableapvendorgroups.iactive));
        			m_iapaccountset = Long.toString(rs.getLong(SMTableapvendorgroups.iapaccountset));
        			m_ibankcode = Long.toString(rs.getLong(SMTableapvendorgroups.ibankcode));
        			m_stermscode = rs.getString(SMTableapvendorgroups.stermscode);
        			m_iprintseparatechecks = Long.toString(rs.getLong(SMTableapvendorgroups.iprintseparatechecks));        			
        			m_idistributioncodeusedfordistribution = Long.toString(rs.getLong(SMTableapvendorgroups.idistributioncodeusedfordistribution));
        			m_sglacctusedfordistribution = rs.getString(SMTableapvendorgroups.sglacctusedfordistribution);
        			m_idistributeby = Long.toString(rs.getLong(SMTableapvendorgroups.idistributeby));
        			m_staxjurisdiction = rs.getString(SMTableapvendorgroups.staxjurisdiction);
        			m_itaxtype = Long.toString(rs.getLong(SMTableapvendorgroups.itaxtype));
        			m_itaxreportingtype = Long.toString(rs.getLong(SMTableapvendorgroups.itaxreportingtype));
        			m_i1099CPRScode = Long.toString(rs.getLong(SMTableapvendorgroups.i1099CPRScode));
        			m_sNewRecord = "0";
        			rs.close();
        			return true;
        		}else{
        		rs.close();
        		setNewRecord("1");
        		return true;
        		}
        	}catch (SQLException e){
        		m_sErrorMessageArray.add("SQL Error reading AP vendor groups record: " + e.getMessage());
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
    		throw new Exception("Error getting connection to load APVendorGroups - " + e.getMessage());
    	}
    	if (conn == null){
    		throw new Exception("Counld not get connection to load APVendorGroups.");
    	}
    	if (!load(conn)){
    		clsDatabaseFunctions.freeConnection(context, conn, "[1547059485]");
    		throw new Exception("Error loading APVendorGroups - " + getErrorMessages());
    	}
    	clsDatabaseFunctions.freeConnection(context, conn, "[1547059486]");
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
			clsDatabaseFunctions.freeConnection(context, conn, "[1547059487]");
			return false;
		}
		
		//Check to see if the record already exists:
		String SQL = "SELECT * FROM " + SMTableapvendorgroups.TableName
			+ " WHERE ("
				+ SMTableapvendorgroups.lid + " = " + m_lid
			+ ")"
			;
		try{
			ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
			
			if(rs.next()){

				rs.close();
				
				m_sErrorMessageArray.clear();
				
    	//Only re-sets the fields that can be edited from the 'Edit Options' screen:
		 SQL = "UPDATE " + SMTableapvendorgroups.TableName
		+ " SET "
			+ SMTableapvendorgroups.sgroupid + " = '" +  clsDatabaseFunctions.FormatSQLStatement(m_sgroupid) + "'"
		+ ", " + SMTableapvendorgroups.sdescription + " = '" + clsDatabaseFunctions.FormatSQLStatement(m_sdescription) + "'"
		+ ", " + SMTableapvendorgroups.iactive + " = " + clsDatabaseFunctions.FormatSQLStatement(m_iactive)
		+ ", " + SMTableapvendorgroups.iapaccountset + " = " + clsDatabaseFunctions.FormatSQLStatement(m_iapaccountset)
		+ ", " + SMTableapvendorgroups.ibankcode + " = " + clsDatabaseFunctions.FormatSQLStatement(m_ibankcode) 
		+ ", " + SMTableapvendorgroups.stermscode + " = '" + clsDatabaseFunctions.FormatSQLStatement(m_stermscode) + "'"
		+ ", " + SMTableapvendorgroups.iprintseparatechecks + " = " + clsDatabaseFunctions.FormatSQLStatement(m_iprintseparatechecks)
		+ ", " + SMTableapvendorgroups.idistributioncodeusedfordistribution + " = " + clsDatabaseFunctions.FormatSQLStatement(m_idistributioncodeusedfordistribution)
		+ ", " + SMTableapvendorgroups.sglacctusedfordistribution + " = '" + clsDatabaseFunctions.FormatSQLStatement(m_sglacctusedfordistribution) + "'"
		+ ", " + SMTableapvendorgroups.idistributeby + " = " + clsDatabaseFunctions.FormatSQLStatement(m_idistributeby) 
		+ ", " + SMTableapvendorgroups.staxjurisdiction + " = '" + clsDatabaseFunctions.FormatSQLStatement(m_staxjurisdiction) + "'"
		+ ", " + SMTableapvendorgroups.itaxtype + " = " + clsDatabaseFunctions.FormatSQLStatement(m_itaxtype) 
		+ ", " + SMTableapvendorgroups.itaxreportingtype + " = " + clsDatabaseFunctions.FormatSQLStatement(m_itaxreportingtype) 
		+ ", " + SMTableapvendorgroups.i1099CPRScode + " = " + clsDatabaseFunctions.FormatSQLStatement(m_i1099CPRScode)
		+ " WHERE ("
						+ SMTableapvendorgroups.lid + " = " + m_lid
					+ ")"
		;

		 	try {
		 		Statement stmt = conn.createStatement();
		 		stmt.executeUpdate(SQL);
		 		m_sNewRecord = "0";
		 	}catch (SQLException e){
		 		clsDatabaseFunctions.freeConnection(context, conn, "[1547059488]");
		 		m_sErrorMessageArray.add("Error updating record with SQL: " + " - " + e.getMessage());
		 		return false;
		}
		}else{
			
			rs.close();
			
			if(!validateEntries()){
				clsDatabaseFunctions.freeConnection(context, conn, "[1547059489]");
				return false;
			}
			SQL = "INSERT INTO " + SMTableapvendorgroups.TableName + " ("
					+ SMTableapvendorgroups.sgroupid
					+ ", " + SMTableapvendorgroups.sdescription
					+ ", " + SMTableapvendorgroups.iactive
					+ ", " + SMTableapvendorgroups.iapaccountset
					+ ", " + SMTableapvendorgroups.ibankcode
					+ ", " + SMTableapvendorgroups.stermscode
					+ ", " + SMTableapvendorgroups.iprintseparatechecks
					+ ", " + SMTableapvendorgroups.idistributioncodeusedfordistribution
					+ ", " + SMTableapvendorgroups.sglacctusedfordistribution
					+ ", " + SMTableapvendorgroups.idistributeby
					+ ", " + SMTableapvendorgroups.staxjurisdiction
					+ ", " + SMTableapvendorgroups.itaxtype
					+ ", " + SMTableapvendorgroups.itaxreportingtype
					+ ", " + SMTableapvendorgroups.i1099CPRScode
					+ " ) VALUES ("
						+ "'" + clsDatabaseFunctions.FormatSQLStatement(m_sgroupid) + "'" 
						+ ", '" + clsDatabaseFunctions.FormatSQLStatement(m_sdescription) + "'" 
						+ ", " + m_iactive
						+ ", " + m_iapaccountset
						+ ", " + m_ibankcode
						+ ", '" + clsDatabaseFunctions.FormatSQLStatement(m_stermscode) + "'"
						+ ", " + m_iprintseparatechecks
						+ ", " + m_idistributioncodeusedfordistribution
						+ ", '" + clsDatabaseFunctions.FormatSQLStatement(m_sglacctusedfordistribution) + "'"
						+ ", " + m_idistributeby
						+ ", '" + clsDatabaseFunctions.FormatSQLStatement(m_staxjurisdiction) + "'"
						+ ", " + m_itaxtype
						+ ", " + m_itaxreportingtype
						+ ", " + m_i1099CPRScode
					+ ")"
					;
			try {
				clsDatabaseFunctions.executeSQL(SQL, conn);
				m_sNewRecord = "0";
			}catch (SQLException e){
		 		clsDatabaseFunctions.freeConnection(context, conn, "[1547059490]");
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
					clsDatabaseFunctions.freeConnection(context, conn, "[1547059491]");
					m_sErrorMessageArray.add("Could not get last ID number - " + e.getMessage());
				}
				//If something went wrong, we can't get the last ID:
				if (m_lid.compareToIgnoreCase("") == 0){
					m_sErrorMessageArray.add("Could not get last ID number.");
				}
	
			//Change new record status
			clsDatabaseFunctions.freeConnection(context, conn, "[1547059492]");

			return true;			
    	}
			}catch(SQLException e){
				clsDatabaseFunctions.freeConnection(context, conn, "[1547059493]");
				m_sErrorMessageArray.add("Error [1450322005] saving  - " + e.getMessage());
				return false;
			}
		return true;
    }
    
public boolean delete(String sVendorGroup, Connection conn){
		
		m_sErrorMessageArray.clear();
		
		//First, check that the account set exists:
		String SQL = "SELECT * FROM " + SMTableapvendorgroups.TableName
			+ " WHERE ("
				+ SMTableapvendorgroups.lid + " = " + m_lid 
			+ ")"
		;
		try{
			ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
			if(!rs.next()){
				m_sErrorMessageArray.add("Vendor Group " + sVendorGroup + " cannot be found.");
				rs.close();
				return false;
			}else{
				rs.close();
			}
			
		}catch(SQLException e){
			m_sErrorMessageArray.add("Error [1450382391] checking vendor group " + sVendorGroup + " to delete - " + e.getMessage());
			return false;
		}

		//Now begin a data transaction that we can roll back:
		if(!clsDatabaseFunctions.start_data_transaction(conn)){
			m_sErrorMessageArray.add("Error [1450382390] beginning data transaction to delete  " + sVendorGroup + "");
			return false;
		}
		
		try{
			SQL = "DELETE FROM " + SMTableapvendorgroups.TableName
				+ " WHERE ("
					+ SMTableapvendorgroups.lid + " = " + m_lid
				+ ")"
			;
			if(!clsDatabaseFunctions.executeSQL(SQL, conn)){
				m_sErrorMessageArray.add("Error [1450382389] deleting vendor group " + sVendorGroup);
				clsDatabaseFunctions.rollback_data_transaction(conn);
				return false;
			}


		}catch(SQLException e){
			m_sErrorMessageArray.add("Error [1450382388] deleting account set " + sVendorGroup + " - " + e.getMessage());
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

	if (m_sgroupid.length() > SMTableapvendorgroups.sgroupidlength){
		m_sErrorMessageArray.add("Vendor group name cannot be longer than  " 
			+ SMTableapvendorgroups.sgroupidlength + " characters.");
		bEntriesAreValid = false;
	}
	if (m_sgroupid.trim().compareToIgnoreCase("") == 0){
		m_sErrorMessageArray.add("Vendor group name cannot be blank.");
		bEntriesAreValid = false;
	}
	
	if (m_sdescription.length() > SMTableapvendorgroups.sdescriptionlength){
		m_sErrorMessageArray.add("Description cannot be longer than " 
			+ SMTableapvendorgroups.sdescriptionlength + " characters.");
		bEntriesAreValid = false;
	}
	if (m_sdescription.trim().compareToIgnoreCase("") == 0){
		m_sErrorMessageArray.add("Description cannot be blank.");
		bEntriesAreValid = false;
	}

	if (m_stermscode.length() > SMTableapvendorgroups.stermscodelength){
		m_sErrorMessageArray.add("Terms code cannot be longer than " 
			+ SMTableapvendorgroups.stermscodelength + " characters.");
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

	public String getsgroupid() {
		return m_sgroupid;
	}

	public void setsgroupid(String sgroupid) {
		m_sgroupid = sgroupid;
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

	public String getiapaccountset() {
		return m_iapaccountset;
	}
	
	public void setiapaccountset(String iapaccountset) {
		m_iapaccountset = iapaccountset;
	}

	public void setibankcode(String ibankcode) {
		m_ibankcode = ibankcode;
	}
	public String getibankcode() {
		return m_ibankcode;
	}
	
	public String getstermscode() {
		return m_stermscode;
	}

	public void setstermscode(String stermscode) {
		m_stermscode = stermscode;
	}
	
	public String getiprintseparatechecks() {
		return m_iprintseparatechecks;
	}
	
	public void setiprintseparatechecks(String iprintseparatechecks) {
		m_iprintseparatechecks = iprintseparatechecks;
	}
	
	public String getidistributioncodeusedfordistribution() {
		return m_idistributioncodeusedfordistribution;
	}

	public void setidistributioncodeusedfordistribution(String idistributioncodeusedfordistribution) {
		m_idistributioncodeusedfordistribution = idistributioncodeusedfordistribution;
	}
	
	public String getsglacctusedfordistribution() {
		return m_sglacctusedfordistribution;
	}
	
	public void setsglacctusedfordistribution(String sglacctusedfordistribution) {
		m_sglacctusedfordistribution = sglacctusedfordistribution;
	}
	
	public String getidistributeby() {
		return m_idistributeby;
	}	
	public void setidistributeby(String idistributeby) {
		m_idistributeby = idistributeby;
	}
	
	public String getstaxjurisdiction() {
		return m_staxjurisdiction;
	}
	public void setstaxjurisdiction(String staxjurisdiction) {
		m_staxjurisdiction = staxjurisdiction;
	}
	
	 public String getitaxtype(){
	    return m_itaxtype;
	}
	 public void setitaxtype(String itaxtype){
		 m_itaxtype = itaxtype;
	}
	 
	 public String getitaxreportingtype(){
	    return m_itaxreportingtype;
	}
	public void setitaxreportingtype(String itaxreportingtype){
		m_itaxreportingtype = itaxreportingtype;
	}
	
	public String geti1099CPRScode(){
	    return m_i1099CPRScode;
	}
	public void seti1099CPRScode(String i1099CPRScode){
		m_i1099CPRScode = i1099CPRScode;
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