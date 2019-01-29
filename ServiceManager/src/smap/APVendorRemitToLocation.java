package smap;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import SMDataDefinition.SMTableapvendorremittolocations;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsManageRequestParameters;
import smcontrolpanel.SMUtilities;

public class APVendorRemitToLocation extends java.lang.Object{
	
	public static final String ParamObjectName = "Vendor Remit To Location";
	
	public static final String Paramsvendoracct = "svendoracct";
	public static final String Paramsremittocode = "sremittocode";
	public static final String Paramsremittoname = "sremittoname";
	public static final String Paramiactive = "iactive";
	public static final String Paramsaddressline1 = "saddressline1";
	public static final String Paramsaddressline2 = "saddressline2";
	public static final String Paramsaddressline3 = "saddressline3";
	public static final String Paramsaddressline4 = "saddressline4";
	public static final String Paramscity = "scity";
	public static final String Paramsstate = "sstate";
	public static final String Paramspostalcode = "spostalcode";
	public static final String Paramscountry = "scountry";
	public static final String Paramscontactname = "scontactname";
	public static final String Paramsphonenumber = "sphonenumber";
	public static final String Paramsfaxnumber = "sfaxnumber";
	public static final String Paramswebaddress = "swebaddress";
	public static final String Paramsemailaddress = "semailaddress";
	public static final String Paramdatlastmaintained = "datlastmaintained";
	public static final String Paramslasteditedby = "slasteditedby";
	
	public static final String ParamsNewRecord = "sNewRecord";
	public static final String FORM_NAME = "MAINFORM";
	
	private String m_svendoracct;
	private String m_sremittocode;
	private String m_sremittoname;
	private String m_iactive;
	private String m_saddressline1;
	private String m_saddressline2;
	private String m_saddressline3;
	private String m_saddressline4;
	private String m_scity;
	private String m_sstate;
	private String m_spostalcode;
	private String m_scountry;
	private String m_scontactname;
	private String m_sphonenumber;
	private String m_sfaxnumber;
	private String m_swebaddress;
	private String m_semailaddress;
	private String m_datlastmaintained;
	private String m_slasteditedby;

	private String m_sNewRecord;
	
	private ArrayList<String> m_sErrorMessageArray = new ArrayList<String> (0);
	

	public APVendorRemitToLocation(){
		m_svendoracct = "";
		m_sremittocode = "";
		m_sremittoname = "";
		m_iactive = "1";
		m_saddressline1 = "";
		m_saddressline2 = "";
		m_saddressline3 = "";
		m_saddressline4 = "";
		m_scity = "";
		m_sstate = "";
		m_spostalcode = "";
		m_scountry = "";
		m_scontactname = "";
		m_sphonenumber = "";
		m_sfaxnumber = "";
		m_swebaddress = "";
		m_semailaddress = "";
		m_datlastmaintained = "00/00/0000";
		m_slasteditedby = "";
		
		m_sNewRecord = "1";
		
		m_sErrorMessageArray = new ArrayList<String> (0);
	}
	
	public APVendorRemitToLocation (HttpServletRequest req){

		m_svendoracct = clsManageRequestParameters.get_Request_Parameter(Paramsvendoracct, req).trim();
		m_sremittocode = clsManageRequestParameters.get_Request_Parameter(Paramsremittocode, req).trim();
		m_sremittoname = clsManageRequestParameters.get_Request_Parameter(Paramsremittoname, req).trim();		
		if (req.getParameter(Paramiactive) == null){
			m_iactive = "0";
		}else{
			m_iactive = "1";
		}
		m_saddressline1 = clsManageRequestParameters.get_Request_Parameter(Paramsaddressline1, req).trim();
		m_saddressline2 = clsManageRequestParameters.get_Request_Parameter(Paramsaddressline2, req).trim();
		m_saddressline3 = clsManageRequestParameters.get_Request_Parameter(Paramsaddressline3, req).trim();
		m_saddressline4 = clsManageRequestParameters.get_Request_Parameter(Paramsaddressline4, req).trim();
		m_scity = clsManageRequestParameters.get_Request_Parameter(Paramscity, req).trim();
		m_sstate = clsManageRequestParameters.get_Request_Parameter(Paramsstate, req).trim();
		m_spostalcode = clsManageRequestParameters.get_Request_Parameter(Paramspostalcode, req).trim();
		m_scountry = clsManageRequestParameters.get_Request_Parameter(Paramscountry, req).trim();
		m_scontactname = clsManageRequestParameters.get_Request_Parameter(Paramscontactname, req).trim();
		m_sphonenumber = clsManageRequestParameters.get_Request_Parameter(Paramsphonenumber, req).trim();
		m_sfaxnumber = clsManageRequestParameters.get_Request_Parameter(Paramsfaxnumber, req).trim();
		m_swebaddress = clsManageRequestParameters.get_Request_Parameter(Paramswebaddress, req).trim();
		m_semailaddress = clsManageRequestParameters.get_Request_Parameter(Paramsemailaddress, req).trim();
		m_datlastmaintained = clsManageRequestParameters.get_Request_Parameter(Paramdatlastmaintained, req).trim();
		if(m_datlastmaintained.compareToIgnoreCase("") == 0 || m_datlastmaintained == null){
			m_datlastmaintained = "00/00/0000";
		}
		m_slasteditedby = clsManageRequestParameters.get_Request_Parameter(Paramslasteditedby, req).trim();

		m_sNewRecord = clsManageRequestParameters.get_Request_Parameter(ParamsNewRecord, req).trim();
		if(clsManageRequestParameters.get_Request_Parameter(APVendorRemitToLocationSelection.SUBMIT_ADD_BUTTON_NAME,req)
				.compareToIgnoreCase(APVendorRemitToLocationSelection.SUBMIT_ADD_BUTTON_VALUE) == 0){
			m_sNewRecord = "1";
		}else{
			m_sNewRecord = "0";
		}
		m_sErrorMessageArray = new ArrayList<String> (0);
	}
	
    public boolean load (Connection conn){
        	
    	String SQL = "SELECT * FROM " + SMTableapvendorremittolocations.TableName
				+ " WHERE ("
				+ "(" + SMTableapvendorremittolocations.sremittocode + " = '" + m_sremittocode + "')"
				+ "AND"
				+ "(" + SMTableapvendorremittolocations.svendoracct + " = '" + m_svendoracct + "')"
			+	")";
        	try {
        		ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
        		if (rs.next()){
        			m_sremittoname = rs.getString(SMTableapvendorremittolocations.sremittoname);		
        			m_iactive =  Long.toString(rs.getLong(SMTableapvendorremittolocations.iactive));
        			m_sremittoname = rs.getString(SMTableapvendorremittolocations.sremittoname);
        			m_saddressline1 = rs.getString(SMTableapvendorremittolocations.saddressline1);
        			m_saddressline2 = rs.getString(SMTableapvendorremittolocations.saddressline2);
        			m_saddressline3 = rs.getString(SMTableapvendorremittolocations.saddressline3);
        			m_saddressline4 = rs.getString(SMTableapvendorremittolocations.saddressline4);
        			m_scity = rs.getString(SMTableapvendorremittolocations.scity);
        			m_sstate = rs.getString(SMTableapvendorremittolocations.sstate);
        			m_spostalcode = rs.getString(SMTableapvendorremittolocations.spostalcode);
        			m_scountry = rs.getString(SMTableapvendorremittolocations.scountry);
        			m_scontactname = rs.getString(SMTableapvendorremittolocations.scontactname);
        			m_sphonenumber = rs.getString(SMTableapvendorremittolocations.sphonenumber);
        			m_sfaxnumber = rs.getString(SMTableapvendorremittolocations.sfaxnumber);
        			m_swebaddress = rs.getString(SMTableapvendorremittolocations.swebaddress);
        			m_semailaddress = rs.getString(SMTableapvendorremittolocations.semailaddress);
        			m_datlastmaintained = rs.getString(SMTableapvendorremittolocations.datlastmaintained);
        			m_slasteditedby = rs.getString(SMTableapvendorremittolocations.slasteditedby);
        			//m_sNewRecord = "0";
        			rs.close();
        			return true;
        		}else{
        		rs.close();
        		setNewRecord("1");
        		return true;
        		}
        	}catch (SQLException e){
        		m_sErrorMessageArray.add("SQL Error reading vendor remit to location record: " + e.getMessage());
        		return false;
        	}
    	}
    
    public void load(String sDBID, ServletContext context, String sUser) throws Exception{
    	Connection conn;
    	try {
    		conn = clsDatabaseFunctions.getConnectionWithException(context, 
    			   sDBID, 
    			   "MySQL",
    			   SMUtilities.getFullClassName(this.toString()) + ".load - user: " + sUser);
    	} catch (Exception e) {
    		throw new Exception("Error getting connection to load vendor remit to location  - " + e.getMessage());
    	}
    	if (conn == null){
    		throw new Exception("Counld not get connection to load vendor remit to location .");
    	}
    	if (!load(conn)){
    		clsDatabaseFunctions.freeConnection(context, conn, "[1547059498]");
    		throw new Exception("Error loading vendor remit to location  - " + getErrorMessages());
    	}
    	clsDatabaseFunctions.freeConnection(context, conn, "[1547059499]");
    }
    

    public boolean saveEditableFields(ServletContext context, String sDBID, String sUserName,  String sUserID, String sUserFullName){
    	m_sErrorMessageArray.clear();
		
    	//Get connection
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
		
		//Validate entries
		if(!validateEntries(conn)){
			clsDatabaseFunctions.freeConnection(context, conn, "[1547059500]");
			return false;
		}
		
		//Check to see if the record already exists:
		String SQL = "SELECT * FROM " + SMTableapvendorremittolocations.TableName
			+ " WHERE ("
			+ "(" + SMTableapvendorremittolocations.svendoracct + " = '" + m_svendoracct + "')"
			+ " AND "
			+ "(" +  SMTableapvendorremittolocations.sremittocode + " = '" + m_sremittocode + "')"
			+ ")"
			;
		try{
			ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
			
			if(rs.next()){

				rs.close();
				
				m_sErrorMessageArray.clear();
				
    	//Update the editable fields
		 SQL = "UPDATE " + SMTableapvendorremittolocations.TableName
		+ " SET "
		 + SMTableapvendorremittolocations.sremittoname + " = '" + clsDatabaseFunctions.FormatSQLStatement(m_sremittoname) + "'"
		+ ", " + SMTableapvendorremittolocations.iactive + " = " + clsDatabaseFunctions.FormatSQLStatement(m_iactive)
		+ ", " + SMTableapvendorremittolocations.saddressline1 + " = '" + clsDatabaseFunctions.FormatSQLStatement(m_saddressline1) + "'"
		+ ", " + SMTableapvendorremittolocations.saddressline2 + " = '" + clsDatabaseFunctions.FormatSQLStatement(m_saddressline2) + "'"
		+ ", " + SMTableapvendorremittolocations.saddressline3 + " = '" + clsDatabaseFunctions.FormatSQLStatement(m_saddressline3) + "'"
		+ ", " + SMTableapvendorremittolocations.saddressline4 + " = '" + clsDatabaseFunctions.FormatSQLStatement(m_saddressline4) + "'"
		+ ", " + SMTableapvendorremittolocations.scity + " = '" + clsDatabaseFunctions.FormatSQLStatement(m_scity) + "'"
		+ ", " + SMTableapvendorremittolocations.sstate + " = '" + clsDatabaseFunctions.FormatSQLStatement(m_sstate) + "'"
		+ ", " + SMTableapvendorremittolocations.spostalcode + " = '" + clsDatabaseFunctions.FormatSQLStatement(m_spostalcode) + "'"
		+ ", " + SMTableapvendorremittolocations.scountry + " = '" + clsDatabaseFunctions.FormatSQLStatement(m_scountry) + "'"
		+ ", " + SMTableapvendorremittolocations.scontactname + " = '" + clsDatabaseFunctions.FormatSQLStatement(m_scontactname) + "'"
		+ ", " + SMTableapvendorremittolocations.sphonenumber + " = '" + clsDatabaseFunctions.FormatSQLStatement(m_sphonenumber) + "'"
		+ ", " + SMTableapvendorremittolocations.sfaxnumber + " = '" + clsDatabaseFunctions.FormatSQLStatement(m_sfaxnumber)+ "'"
		+ ", " + SMTableapvendorremittolocations.swebaddress + " = '" + clsDatabaseFunctions.FormatSQLStatement(m_swebaddress)+ "'"
		+ ", " + SMTableapvendorremittolocations.semailaddress + " = '" + clsDatabaseFunctions.FormatSQLStatement(m_semailaddress) + "'"
		+ ", " + SMTableapvendorremittolocations.datlastmaintained + " = NOW()"
		+ ", " + SMTableapvendorremittolocations.slasteditedby + " = '" + clsDatabaseFunctions.FormatSQLStatement(sUserName) + "'"
		+ " WHERE ("
			+ "(" + SMTableapvendorremittolocations.svendoracct + " = '" + m_svendoracct + "')"
			+ " AND "
			+ "(" + SMTableapvendorremittolocations.sremittocode + " = '" + m_sremittocode + "')"
			+ ")"
		;
		 System.out.println(SQL);
		 	try {
		 		Statement stmt = conn.createStatement();
		 		stmt.executeUpdate(SQL);
		 		m_sNewRecord = "0";
		 	}catch (SQLException e){
		 		clsDatabaseFunctions.freeConnection(context, conn, "[1547059501]");
		 		m_sErrorMessageArray.add("Error updating record with SQL: " + " - " + e.getMessage());
		 		return false;
		}
		}else{
			
			rs.close();
			
			if(!validateEntries(conn)){
				clsDatabaseFunctions.freeConnection(context, conn, "[1547059502]");
				return false;
			}
			SQL = "INSERT INTO " + SMTableapvendorremittolocations.TableName + " ("
					+ SMTableapvendorremittolocations.svendoracct
					+ ", " + SMTableapvendorremittolocations.sremittocode
					+ ", " + SMTableapvendorremittolocations.iactive
					+ ", " + SMTableapvendorremittolocations.sremittoname
					+ ", " + SMTableapvendorremittolocations.saddressline1 //5
					+ ", " + SMTableapvendorremittolocations.saddressline2
					+ ", " + SMTableapvendorremittolocations.saddressline3
					+ ", " + SMTableapvendorremittolocations.saddressline4
					+ ", " + SMTableapvendorremittolocations.scity
					+ ", " + SMTableapvendorremittolocations.sstate //10
					+ ", " + SMTableapvendorremittolocations.spostalcode
					+ ", " + SMTableapvendorremittolocations.scountry
					+ ", " + SMTableapvendorremittolocations.scontactname
					+ ", " + SMTableapvendorremittolocations.sphonenumber
					+ ", " + SMTableapvendorremittolocations.sfaxnumber //15
					+ ", " + SMTableapvendorremittolocations.swebaddress
					+ ", " + SMTableapvendorremittolocations.semailaddress
					+ ", " + SMTableapvendorremittolocations.datlastmaintained
					+ ", " + SMTableapvendorremittolocations.slasteditedby //19
					+ " ) VALUES ("
						+ "'" + clsDatabaseFunctions.FormatSQLStatement(m_svendoracct) + "'" 
						+ ", '" + clsDatabaseFunctions.FormatSQLStatement(m_sremittocode) + "'" 
						+ ", " + m_iactive
						+ ", '" + clsDatabaseFunctions.FormatSQLStatement(m_sremittoname) + "'" 
						+ ", '" + clsDatabaseFunctions.FormatSQLStatement(m_saddressline1) + "'" //5
						+ ", '" + clsDatabaseFunctions.FormatSQLStatement(m_saddressline2) + "'" 
						+ ", '" + clsDatabaseFunctions.FormatSQLStatement(m_saddressline3) + "'" 
						+ ", '" + clsDatabaseFunctions.FormatSQLStatement(m_saddressline4) + "'" 
						+ ", '" + clsDatabaseFunctions.FormatSQLStatement(m_scity) + "'" 
						+ ", '" + clsDatabaseFunctions.FormatSQLStatement(m_sstate) + "'" //10
						+ ", '" + clsDatabaseFunctions.FormatSQLStatement(m_spostalcode) + "'" 
						+ ", '" + clsDatabaseFunctions.FormatSQLStatement(m_scountry) + "'" 
						+ ", '" + clsDatabaseFunctions.FormatSQLStatement(m_scontactname) + "'" 
						+ ", '" + clsDatabaseFunctions.FormatSQLStatement(m_sphonenumber) + "'" 
						+ ", '" + clsDatabaseFunctions.FormatSQLStatement(m_sfaxnumber) + "'" //15
						+ ", '" + clsDatabaseFunctions.FormatSQLStatement(m_swebaddress) + "'" 
						+ ", '" + clsDatabaseFunctions.FormatSQLStatement(m_semailaddress) + "'" 
						+ ", NOW()" 
						+ ", '" + clsDatabaseFunctions.FormatSQLStatement(sUserName) + "'" //19
					+ ")"
					;
			try {
				clsDatabaseFunctions.executeSQL(SQL, conn);
				m_sNewRecord = "0";
			}catch (SQLException e){
		 		clsDatabaseFunctions.freeConnection(context, conn, "[1547059503]");
		 		m_sErrorMessageArray.add("Error inserting record with SQL: " +  " - " + e.getMessage());
		 		m_sNewRecord = "1";
		 		return false;
			}
	
			//Change new record status
			clsDatabaseFunctions.freeConnection(context, conn, "[1547059504]");

			return true;			
    	}
			}catch(SQLException e){
				clsDatabaseFunctions.freeConnection(context, conn, "[1547059505]");
				m_sErrorMessageArray.add("Error [1451316550] saving  - " + e.getMessage());
				return false;
			}
		return true;
    }
    
public boolean delete(String sRemitToCode, Connection conn){
		
		m_sErrorMessageArray.clear();
		
		//First, check that the remit to code code exists:
		String SQL = "SELECT * FROM " + SMTableapvendorremittolocations.TableName
			+ " WHERE ("
			+ "(" + SMTableapvendorremittolocations.svendoracct + " = '" + m_svendoracct + "')"
			+ " AND " 
			+ "(" + SMTableapvendorremittolocations.sremittocode + " = '" + m_sremittocode + "')" 
			+ ")"
		;
		try{
			ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
			if(!rs.next()){
				m_sErrorMessageArray.add("Remit to location " + sRemitToCode + " cannot be found.");
				rs.close();
				return false;
			}else{
				rs.close();
			}
			
		}catch(SQLException e){
			m_sErrorMessageArray.add("Error [1451316551] checking remit to location " + sRemitToCode + " to delete - " + e.getMessage());
			return false;
		}

		//Now begin a data transaction that we can roll back:
		if(!clsDatabaseFunctions.start_data_transaction(conn)){
			m_sErrorMessageArray.add("Error [1451316552] beginning data transaction to delete  " + sRemitToCode + "");
			return false;
		}
		
		try{
			SQL = "DELETE FROM " + SMTableapvendorremittolocations.TableName
				+ " WHERE ("
				+ "(" + SMTableapvendorremittolocations.svendoracct + " = '" + m_svendoracct + "')"
				+ " AND " 
				+ "(" + SMTableapvendorremittolocations.sremittocode + " = '" + m_sremittocode + "')" 
				+ ")"
				;
			if(!clsDatabaseFunctions.executeSQL(SQL, conn)){
				m_sErrorMessageArray.add("Error [1451316553] deleting remit to location " + sRemitToCode);
				clsDatabaseFunctions.rollback_data_transaction(conn);
				return false;
			}

		}catch(SQLException e){
			m_sErrorMessageArray.add("Error [1451316554] deleting remit to location " + sRemitToCode + " - " + e.getMessage());
			return false;
		}
		
		if(!clsDatabaseFunctions.commit_data_transaction(conn)){
			clsDatabaseFunctions.rollback_data_transaction(conn);
			return false;
		}
		
		return true;
	}

private boolean validateEntries(Connection conn){
	
	boolean bEntriesAreValid = true;
	m_sErrorMessageArray.clear();
	
	if (m_svendoracct.trim().compareToIgnoreCase("") == 0){
		m_sErrorMessageArray.add("Vandor account cannot be blank.");
		bEntriesAreValid = false;
	}
	if (m_svendoracct.length() > SMTableapvendorremittolocations.svendoracctlength){
		m_sErrorMessageArray.add("Vendor account cannot be longer than  " 
			+ SMTableapvendorremittolocations.svendoracctlength + " characters.");
		bEntriesAreValid = false;
	}
	
	if (m_sremittocode.trim().compareToIgnoreCase("") == 0){
		m_sErrorMessageArray.add("Remit to code cannot be blank.");
		bEntriesAreValid = false;
	}	
	if (m_sremittocode.length() > SMTableapvendorremittolocations.sremittocodelength){
		m_sErrorMessageArray.add("Remit to code cannot be longer than " 
			+ SMTableapvendorremittolocations.sremittocodelength + " characters.");
		bEntriesAreValid = false;
	}
	if (m_sremittoname.length() > SMTableapvendorremittolocations.sremittonamelength){
		m_sErrorMessageArray.add("Remit to name cannot be longer than " 
			+ SMTableapvendorremittolocations.sremittonamelength + " characters.");
		bEntriesAreValid = false;
	}
	if (m_saddressline1.length() > SMTableapvendorremittolocations.saddressline1length){
		m_sErrorMessageArray.add("Address line 1 cannot be longer than " 
			+ SMTableapvendorremittolocations.saddressline1length + " characters.");
		bEntriesAreValid = false;
	}
	if (m_saddressline2.length() > SMTableapvendorremittolocations.saddressline2length){
		m_sErrorMessageArray.add("Address line 2 cannot be longer than " 
			+ SMTableapvendorremittolocations.saddressline2length + " characters.");
		bEntriesAreValid = false;
	}
	if (m_saddressline3.length() > SMTableapvendorremittolocations.saddressline3length){
		m_sErrorMessageArray.add("Address line 3 cannot be longer than " 
			+ SMTableapvendorremittolocations.saddressline3length + " characters.");
		bEntriesAreValid = false;
	}
	if (m_saddressline4.length() > SMTableapvendorremittolocations.saddressline4length){
		m_sErrorMessageArray.add("Address line 4 cannot be longer than " 
			+ SMTableapvendorremittolocations.saddressline4length + " characters.");
		bEntriesAreValid = false;
	}
	if (m_scity.length() > SMTableapvendorremittolocations.scitylength){
		m_sErrorMessageArray.add("City cannot be longer than " 
			+ SMTableapvendorremittolocations.scitylength + " characters.");
		bEntriesAreValid = false;
	}
	if (m_sstate.length() > SMTableapvendorremittolocations.sstatelength){
		m_sErrorMessageArray.add("State cannot be longer than " 
			+ SMTableapvendorremittolocations.sstatelength + " characters.");
		bEntriesAreValid = false;
	}
	if (m_spostalcode.length() > SMTableapvendorremittolocations.spostalcodelength){
		m_sErrorMessageArray.add("Postal Code cannot be longer than " 
			+ SMTableapvendorremittolocations.spostalcodelength + " characters.");
		bEntriesAreValid = false;
	}
	if (m_scountry.length() > SMTableapvendorremittolocations.scountrylength){
		m_sErrorMessageArray.add("Country cannot be longer than " 
			+ SMTableapvendorremittolocations.scountrylength + " characters.");
		bEntriesAreValid = false;
	}
	if (m_scontactname.length() > SMTableapvendorremittolocations.scontactnamelength){
		m_sErrorMessageArray.add("Contact Name cannot be longer than " 
			+ SMTableapvendorremittolocations.scontactnamelength + " characters.");
		bEntriesAreValid = false;
	}
	if (m_sphonenumber.length() > SMTableapvendorremittolocations.sphonenumberlength){
		m_sErrorMessageArray.add("Phone Number cannot be longer than " 
			+ SMTableapvendorremittolocations.sphonenumberlength + " characters.");
		bEntriesAreValid = false;
	}
	if (m_sfaxnumber.length() > SMTableapvendorremittolocations.sfaxnumberlength){
		m_sErrorMessageArray.add("Fax Number cannot be longer than " 
			+ SMTableapvendorremittolocations.sfaxnumberlength + " characters.");
		bEntriesAreValid = false;
	}
	if (m_swebaddress.length() > SMTableapvendorremittolocations.swebaddresslength){
		m_sErrorMessageArray.add("Webaddress cannot be longer than " 
			+ SMTableapvendorremittolocations.swebaddresslength + " characters.");
		bEntriesAreValid = false;
	}
	if (m_semailaddress.length() > SMTableapvendorremittolocations.semailaddresslength){
		m_sErrorMessageArray.add("Email Address cannot be longer than " 
			+ SMTableapvendorremittolocations.semailaddresslength + " characters.");
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
	public String getsvendoracct() {
		return m_svendoracct;
	}
	public void setsvendoracct(String svendoracct) {
		m_svendoracct = svendoracct;
	}
	public String getsremittocode() {
		return m_sremittocode;
	}
	public void setsremittocode(String sremittocode) {
		m_sremittocode = sremittocode;
	}
	public String getsremittoname() {
		return m_sremittoname;
	}
	public void setsremittoname(String sremittoname) {
		m_sremittoname = sremittoname;
	}
	public String getiactive() {
		return m_iactive;
	}
	public void setiactive(String iactive) {
		m_iactive = iactive;
	}
	public String getsaddressline1() {
		return m_saddressline1;
	}	
	public void setsaddressline1(String saddressline1) {
		m_saddressline1 = saddressline1;
	}
	public String getsaddressline2() {
		return m_saddressline2;
	}	
	public void setsaddressline2(String saddressline2) {
		m_saddressline2 = saddressline2;
	}
	public String getsaddressline3() {
		return m_saddressline3;
	}	
	public void setsaddressline3(String saddressline3) {
		m_saddressline3 = saddressline3;
	}
	public String getsaddressline4() {
		return m_saddressline4;
	}	
	public void setsaddressline4(String saddressline4) {
		m_saddressline4 = saddressline4;
	}
	public String getscity() {
		return m_scity;
	}	
	public void setscity(String scity) {
		m_scity = scity;
	}
	public String getsstate() {
		return m_sstate;
	}	
	public void setsstate(String sstate) {
		m_saddressline4 = sstate;
	}
	public String getspostalcode() {
		return m_spostalcode;
	}	
	public void setspostalcode(String spostalcode) {
		m_spostalcode = spostalcode;
	}
	public String getscountry() {
		return m_scountry;
	}	
	public void setscountry(String scountry) {
		m_scountry = scountry;
	}
	public String getscontactname() {
		return m_scontactname;
	}	
	public void setscontactname(String scontactname) {
		m_scontactname = scontactname;
	}
	public String getsphonenumber() {
		return m_sphonenumber;
	}	
	public void setsphonenumber(String sphonenumber) {
		m_sphonenumber = sphonenumber;
	}
	public String getsfaxnumber() {
		return m_sfaxnumber;
	}	
	public void setsfaxnumber(String sfaxnumber) {
		m_sfaxnumber = sfaxnumber;
	}
	public String getswebaddress() {
		return m_swebaddress;
	}	
	public void setswebaddress(String swebaddress) {
		m_swebaddress = swebaddress;
	}
	public String getsemailaddress() {
		return m_semailaddress;
	}	
	public void setsemailaddress(String semailaddress) {
		m_semailaddress = semailaddress;
	}
	public String getdatlastmaintained() {
		return m_datlastmaintained;
	}	
	public void setdatlastmaintained(String datlastmaintained) {
		m_datlastmaintained = datlastmaintained;
	}
	public String getslasteditedby() {
		return m_slasteditedby;
	}	
	public void setslasteditedby(String slasteditedby) {
		m_slasteditedby = slasteditedby;
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