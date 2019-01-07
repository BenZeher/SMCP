package smap;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import java.sql.Connection;
import java.util.ArrayList;
import SMDataDefinition.*;
import ServletUtilities.clsMasterEntry;
import ServletUtilities.clsServletUtilities;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsManageRequestParameters;
import ServletUtilities.clsStringFunctions;

public class APAccountSet extends clsMasterEntry{
	public static final String ParamAddingNewRecord = "bAddingNewRecord";
	public static final String ParamObjectName = "AP Account Set";
    public static final String Paramlid = "lid";
    public static final String Paramsacctsetname = "sacctsetname";
    public static final String Paramsdescription = "sdescription";
    public static final String Paramiactive = "iactive";
    public static final String Paramspayablescontrolacct = "spayablescontrolacct";
    public static final String Paramspurchasediscountacct = "spurchasediscountacct";
    public static final String Paramsprepaymentacct = "sprepaymentacct";

    private String m_lid;
    private String m_sacctsetname;
    private String m_sdescription;
    private String m_iactive;
    private String m_spayablescontrolacct;
    private String m_spurchasediscountacct;
    private String m_sprepaymentacct;
    private String m_sNewRecord;
    
	private ArrayList<String> m_sErrorMessageArray = new ArrayList<String> (0);

    public APAccountSet(
        ) {
    	m_lid = "";
    	m_sacctsetname = "";
    	m_sdescription = "";
    	m_iactive = "0";
    	m_spayablescontrolacct = "";
    	m_spurchasediscountacct = "";
    	m_sprepaymentacct = "";
    	m_sNewRecord = "";
    	m_sErrorMessageArray = new ArrayList<String> (0);
        }
    
    public void loadFromHTTPRequest(HttpServletRequest req){

    	m_lid = clsManageRequestParameters.get_Request_Parameter(APAccountSet.Paramlid, req).trim().replace("&quot;", "\"");
    	m_sacctsetname = clsManageRequestParameters.get_Request_Parameter(APAccountSet.Paramsacctsetname, req).trim().replace("&quot;", "\"");
    	m_sdescription = clsManageRequestParameters.get_Request_Parameter(APAccountSet.Paramsdescription, req).trim().replace("&quot;", "\"");
    	m_spayablescontrolacct = clsManageRequestParameters.get_Request_Parameter(APAccountSet.Paramspayablescontrolacct, req).trim().replace("&quot;", "\"");
    	m_spurchasediscountacct = clsManageRequestParameters.get_Request_Parameter(APAccountSet.Paramspurchasediscountacct, req).trim().replace("&quot;", "\"");
    	m_sprepaymentacct = clsManageRequestParameters.get_Request_Parameter(APAccountSet.Paramsprepaymentacct, req).trim().replace("&quot;", "\"");

		if(req.getParameter(APAccountSet.Paramiactive) == null){
			m_iactive = "0";
		}else{
			m_iactive = "1";
		}

		//m_sNewRecord = SMUtilities.get_Request_Parameter(APAccountSet.ParamAddingNewRecord, req).trim().replace("&quot;", "\"");
    }
    private boolean load(
			String sAccountSetCode,
    		ServletContext context, 
    		String sDBID
			){
		m_sErrorMessageArray.clear();
		try{
			//Get the record to edit:
			String sSQL = "SELECT * FROM " + SMTableapaccountsets.TableName
				+ " WHERE ("
					+ SMTableapaccountsets.lid + " = '" + sAccountSetCode + "'"
				+ ")"
			;
	        ResultSet rs = clsDatabaseFunctions.openResultSet(
    				sSQL, 
    				context, 
    				sDBID, 
    				"MySQL", 
    				this.toString() + ".load");
	        if (loadFromResultSet(rs)){
	        	rs.close();
	        	return true;
	        }else{
	        	rs.close();
	        	return false;
	        }
		}catch (SQLException ex){
	    	m_sErrorMessageArray.add("Error [1450321890] loading account set '" + sAccountSetCode + "'");
			return false;
		}
	}
	//Need this one with the connection:
	private boolean load(
			String sAccountSetID,
			Connection conn
			){
		m_sErrorMessageArray.clear();
		try{
			//Get the record to edit:
			String sSQL = "SELECT * FROM " + SMTableapaccountsets.TableName
				+ " WHERE ("
					+ SMTableapaccountsets.lid + " = '" + sAccountSetID + "'"
				+	")"
			;
	        ResultSet rs = clsDatabaseFunctions.openResultSet(sSQL, conn);
	        if (loadFromResultSet(rs)){
	        	rs.close();
	        	return true;
	        }else{
	        	rs.close();
	        	return false;
	        }
	        
		}catch (SQLException ex){
			m_sErrorMessageArray.add("Error [1450321891] loading" + ParamObjectName + " ID=" + sAccountSetID);
			return false;
		}
	}

	private boolean loadFromResultSet(ResultSet rs){
		try{
	        if (rs.next()){
	        	m_lid = Long.toString(rs.getLong(SMTableapaccountsets.lid));
	        	m_sacctsetname = clsStringFunctions.checkStringForNull(rs.getString(SMTableapaccountsets.sacctsetname));
	        	m_sdescription = clsStringFunctions.checkStringForNull(rs.getString(SMTableapaccountsets.sdescription));
	        	m_spayablescontrolacct = clsStringFunctions.checkStringForNull(rs.getString(SMTableapaccountsets.spayablescontrolacct));
	        	m_spurchasediscountacct = clsStringFunctions.checkStringForNull(rs.getString(SMTableapaccountsets.spurchasediscountacct));
	        	m_sprepaymentacct = clsStringFunctions.checkStringForNull(rs.getString(SMTableapaccountsets.sprepaymentacct));
	        	m_iactive = Long.toString(rs.getLong(SMTableapaccountsets.iactive));
	        	m_sNewRecord = "0";
	        	rs.close();
	        	return true;
	        }
	        else{
	        	rs.close();
	        	return false;
	        }
		}catch(SQLException ex){
			m_sErrorMessageArray.add("Error [1450321892] loading" + ParamObjectName + ": " + ex.getMessage());
		}
		return true;
	}
	public boolean load(
    		ServletContext context, 
    		String sDBID
			){

		return load(m_lid, context, sDBID);
	}
	public boolean load(
    		Connection conn
			){

		return load(m_lid, conn);
	}
	//Need a connection here for the data transaction:
	public boolean save (Connection conn){
		m_sErrorMessageArray.clear();
		//Check to see if the record already exists:
		String SQL = "SELECT * FROM " + SMTableapaccountsets.TableName
			+ " WHERE ("
				+ SMTableapaccountsets.lid + " = '" + m_lid + "'"
			+ ")"
			;
		try{
			ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
			
			if(rs.next()){
				//If it's supposed to be a new record, then return an error:
				if(m_sNewRecord.compareToIgnoreCase("1") == 0){
					m_sErrorMessageArray.add("Cannot save - account set already exists.");
					rs.close();
					return false;
				}
				rs.close();
				
				m_sErrorMessageArray.clear();
				if(!validateEntries()){
					return false;
				}
				SQL = "UPDATE " + SMTableapaccountsets.TableName + " SET "
					+ " " + SMTableapaccountsets.sacctsetname + " = '" + clsDatabaseFunctions.FormatSQLStatement(m_sacctsetname) + "'"
					+ ", " + SMTableapaccountsets.sdescription + " = '" + clsDatabaseFunctions.FormatSQLStatement(m_sdescription) + "'"
					+ ", " + SMTableapaccountsets.spayablescontrolacct + " = '" + clsDatabaseFunctions.FormatSQLStatement(m_spayablescontrolacct) + "'"
					+ ", " + SMTableapaccountsets.spurchasediscountacct + " = '" + clsDatabaseFunctions.FormatSQLStatement(m_spurchasediscountacct) + "'"
					+ ", " + SMTableapaccountsets.sprepaymentacct + " = '" + clsDatabaseFunctions.FormatSQLStatement(m_sprepaymentacct) + "'"
					+ ", " + SMTableapaccountsets.iactive + " = " + m_iactive	
					+ " WHERE ("
						+ SMTableapaccountsets.lid + " = " + m_lid
					+ ")"
					;
					
				if(!clsDatabaseFunctions.executeSQL(SQL, conn)){
					m_sErrorMessageArray.add("Error [1450321890] Cannot execute UPDATE SQL: " + SQL);
					return false;
				}else{
					m_sNewRecord = "0";
					return true;
				}
			}else{
				//If it DOESN'T exist:
				//If it's supposed to be an existing record, then return an error:
				if(m_sNewRecord.compareToIgnoreCase("0") == 0){
					m_sErrorMessageArray.add("Cannot save - can't get existing account set.");
					rs.close();
					return false;
				}
				rs.close();
				//Insert the record:

				
				if(!validateEntries()){
					return false;
				}
				
				if (!clsDatabaseFunctions.start_data_transaction(conn)){
					m_sErrorMessageArray.add("Could not start data transaction for account set insert.");
					return false;
				}
			
				SQL = "INSERT INTO " + SMTableapaccountsets.TableName + " ("
					+ SMTableapaccountsets.sacctsetname
					+ ", " + SMTableapaccountsets.sdescription
					+ ", " + SMTableapaccountsets.iactive
					+ ", " + SMTableapaccountsets.spayablescontrolacct
					+ ", " + SMTableapaccountsets.spurchasediscountacct
					+ ", " + SMTableapaccountsets.sprepaymentacct
					+ " ) VALUES ("
						+ "'" + clsDatabaseFunctions.FormatSQLStatement(m_sacctsetname) + "'" 
						+ ", '" + clsDatabaseFunctions.FormatSQLStatement(m_sdescription) + "'" 
						+ ", " + m_iactive
						+ ", '" + clsDatabaseFunctions.FormatSQLStatement(m_spayablescontrolacct) + "'"
						+ ", '" + clsDatabaseFunctions.FormatSQLStatement(m_spurchasediscountacct) + "'"
						+ ", '" + clsDatabaseFunctions.FormatSQLStatement(m_sprepaymentacct) + "'"
					+ ")"
					;
				if(!clsDatabaseFunctions.executeSQL(SQL, conn)){
					clsDatabaseFunctions.rollback_data_transaction(conn);
					m_sErrorMessageArray.add("Cannot execute account set INSERT sql.");
					return false;
				}

				if (!clsDatabaseFunctions.commit_data_transaction(conn)){
					clsDatabaseFunctions.rollback_data_transaction(conn);
					m_sErrorMessageArray.add("Cannot commit data transaction.");
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
						m_sErrorMessageArray.add("Could not get last ID number - " + e.getMessage());
					}
					//If something went wrong, we can't get the last ID:
					if (m_lid.compareToIgnoreCase("") == 0){
						m_sErrorMessageArray.add("Could not get last ID number.");
					}
		
				//Change new record status
				m_sNewRecord = "0";
				return true;				
			}	
		}catch(SQLException e){
			m_sErrorMessageArray.add("Error [1450322005] saving  - " + e.getMessage());
			return false;
		}
	}

	private boolean validateEntries(){
		
		boolean bEntriesAreValid = true;
		m_sErrorMessageArray.clear();

    	if (m_sacctsetname.length() > SMTableapaccountsets.sacctsetnamelength){
    		m_sErrorMessageArray.add("account set name cannot be longer than " 
    			+ SMTableapaccountsets.sacctsetnamelength + " characters.");
    		bEntriesAreValid = false;
    	}
    	if (m_sacctsetname.trim().compareToIgnoreCase("") == 0){
    		m_sErrorMessageArray.add("Account set name cannot be blank");
    		bEntriesAreValid = false;
    	}
    	if (m_sdescription.length() > SMTableapaccountsets.sdescriptionlength){
    		m_sErrorMessageArray.add("Description cannot be longer than " 
    			+ SMTableapaccountsets.sdescriptionlength + " characters.");
    		bEntriesAreValid = false;
    	}
    	if (m_sdescription.trim().compareToIgnoreCase("") == 0){
    		m_sErrorMessageArray.add("Description cannot be blank");
    		bEntriesAreValid = false;
    	}

    	if (m_spayablescontrolacct.length() > SMTableapaccountsets.spayablescontrolacctlength){
    		m_sErrorMessageArray.add("Payables control account cannot be longer than " 
    			+ SMTableapaccountsets.spayablescontrolacctlength + " characters.");
    		bEntriesAreValid = false;
    	}
    	if (m_spayablescontrolacct.trim().compareToIgnoreCase("") == 0){
    		m_sErrorMessageArray.add("Payable control account cannot be blank");
    		bEntriesAreValid = false;
    	}   	

    	if (m_spurchasediscountacct.length() > SMTableapaccountsets.spurchasediscountacctlength){
    		m_sErrorMessageArray.add("Purchase discount account cannot be longer than " 
    			+ SMTableapaccountsets.spurchasediscountacctlength + " characters.");
    		bEntriesAreValid = false;
    	}
    	if (m_spurchasediscountacct.trim().compareToIgnoreCase("") == 0){
    		m_sErrorMessageArray.add("purchase discount account cannot be blank");
    		bEntriesAreValid = false;
    	}   

    	if (m_sprepaymentacct.length() > SMTableapaccountsets.sprepaymentacctlength){
    		m_sErrorMessageArray.add("payables clearing cannot be longer than " 
    			+ SMTableapaccountsets.sprepaymentacctlength + " characters.");
    		bEntriesAreValid = false;
    	}
    	if (m_sprepaymentacct.trim().compareToIgnoreCase("") == 0){
    		m_sErrorMessageArray.add("prepayment account cannot be blank");
    		bEntriesAreValid = false;
    	}   
    
    	return bEntriesAreValid;
	}
	public String getQueryString(){
		
		String sQueryString = "";
		sQueryString += APAccountSet.ParamAddingNewRecord + "=" + clsServletUtilities.URLEncode(m_sNewRecord);
		sQueryString += "&" + APAccountSet.Paramlid + "=" + clsServletUtilities.URLEncode(m_lid);
		if (m_iactive.compareToIgnoreCase("1") == 0){
			sQueryString += "&" + APAccountSet.Paramiactive + "=" + m_iactive;
		}
		sQueryString += "&" + APAccountSet.Paramsacctsetname + "=" + clsServletUtilities.URLEncode(m_sacctsetname);
		sQueryString += "&" + APAccountSet.Paramsdescription + "=" + clsServletUtilities.URLEncode(m_sdescription);
		sQueryString += "&" + APAccountSet.Paramspayablescontrolacct + "=" + clsServletUtilities.URLEncode(m_spayablescontrolacct);
		sQueryString += "&" + APAccountSet.Paramspurchasediscountacct + "=" + clsServletUtilities.URLEncode(m_spurchasediscountacct);
		sQueryString += "&" + APAccountSet.Paramsprepaymentacct + "=" + clsServletUtilities.URLEncode(m_sprepaymentacct);
				
		return sQueryString;
	}
	//Requires connection since it is used as part of a data transaction in places:
	public boolean delete(String sAccountSet, Connection conn){
		
		m_sErrorMessageArray.clear();
		
		//First, check that the account set exists:
		String SQL = "SELECT * FROM " + SMTableapaccountsets.TableName
			+ " WHERE ("
				+ SMTableapaccountsets.lid + " = '" + m_lid + "'"
			+ ")"
		;
		try{
			ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
			if(!rs.next()){
				m_sErrorMessageArray.add("Account set " + sAccountSet + " cannot be found.");
				rs.close();
				return false;
			}else{
				rs.close();
			}
			
		}catch(SQLException e){
			m_sErrorMessageArray.add("Error [1450322006] checking account set " + sAccountSet + " to delete - " + e.getMessage());
			return false;
		}

		//Now begin a data transaction that we can roll back:
		if(!clsDatabaseFunctions.start_data_transaction(conn)){
			m_sErrorMessageArray.add("Error [1450322007] beginning data transaction to delete  " + sAccountSet + "");
			return false;
		}
		
		try{
			SQL = "DELETE FROM " + SMTableapaccountsets.TableName
				+ " WHERE ("
					+ SMTableapaccountsets.lid + " = '" + m_lid + "'"
				+ ")"
			;
			if(!clsDatabaseFunctions.executeSQL(SQL, conn)){
				m_sErrorMessageArray.add("Error [1450322008] deleting account set " + sAccountSet);
				clsDatabaseFunctions.rollback_data_transaction(conn);
				return false;
			}


		}catch(SQLException e){
			m_sErrorMessageArray.add("Error [1450322009] deleting account set " + sAccountSet + " - " + e.getMessage());
			return false;
		}
		
		if(!clsDatabaseFunctions.commit_data_transaction(conn)){
			clsDatabaseFunctions.rollback_data_transaction(conn);
			return false;
		}
		
		return true;
	}
    public String getlid(){
    	return m_lid;
    }
    public void setlid(String sAccountSetCode){
    	m_lid = sAccountSetCode.trim();
    }
	public String getsacctsetname() {
		return m_sacctsetname;
	}
	public void setsacctsetname(String sacctsetname) {
		m_sacctsetname = sacctsetname.trim();
	}
    public String getsdescription(){
    	return m_sdescription;
    }
	public void setsdescription(String sDescription) {
		m_sdescription = sDescription.trim();
	}
    public String getspayablescontrolacct(){
    	return m_spayablescontrolacct;
    }
	public void setspayablescontrolacct(String spayablescontrolacct) {
		m_spayablescontrolacct = spayablescontrolacct.trim();
	}
    public String getspurchasediscountacct(){
    	return m_spurchasediscountacct;
    }
	public void setspurchasediscountacct(String spurchasediscountacct) {
		m_spurchasediscountacct = spurchasediscountacct.trim();
	}
    public String getsprepaymentacct(){
    	return m_sprepaymentacct;
    }
	public void setsprepaymentacct(String sprepaymentacct) {
		m_sprepaymentacct = sprepaymentacct.trim();
	}
    
	public String getiactive() {
		return m_iactive;
	}
	public void setiactive(String iActive) {
		m_iactive = iActive;
	}

	public String getNewRecord() {
		return m_sNewRecord;
	}
	public void setNewRecord(String newRecord) {
		m_sNewRecord = newRecord;
	}
	public String getObjectName() {
		return ParamObjectName;
	}
	public String getErrorMessageString(){
		String s = "";
		for (int i = 0; i < m_sErrorMessageArray.size(); i ++){
			s += "<BR>" + m_sErrorMessageArray.get(i);
		}
		return s;
	}
}