package smar;

import SMDataDefinition.*;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsDateAndTimeConversions;
import ServletUtilities.clsManageRequestParameters;
import ServletUtilities.clsServletUtilities;
import ServletUtilities.clsStringFunctions;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

public class ARCustomerGroup extends java.lang.Object{
	
	public static final String ParamsAddingNewRecord = "bAddingNewRecord";
	public static final String ParamsGroupCode = "sGroupCode";
	public static final String ParamsDescription = "sDescription";
	public static final String ParamiActive = "iActive";
	public static final String ParamdatLastMaintained = "datLastMaintained";
	public static final String ParamsLastEditUserFullName = "sLastEditUserFullName";
	public static final String ParamlLastEditUserID = "lLastEditUserID";
	
	private String m_sGroupCode;
	private String m_sDescription;
	private String m_iActive;
	private String m_datLastMaintained;
	private String m_sLastEditUserFullName;
	private String m_lLastEditUserID;
	private String m_iNewRecord;
	private ArrayList<String> m_sErrorMessageArray = new ArrayList<String> (0);

    ARCustomerGroup(
    		String sCustomerGroup
        ) {
    	m_sGroupCode = sCustomerGroup;
    	m_sDescription = "";
    	m_iActive = "0";
    	m_datLastMaintained = clsDateAndTimeConversions.now("MM/dd/yyyy");
    	m_sLastEditUserFullName = "";
    	m_lLastEditUserID = "0";
    	m_iNewRecord = "1";
    	m_sErrorMessageArray = new ArrayList<String> (0);

        }
    
    public void loadFromHTTPRequest(HttpServletRequest req){
    	m_iNewRecord = clsManageRequestParameters.get_Request_Parameter(ParamsAddingNewRecord, req).trim().replace("&quot;", "\"");
    	m_sGroupCode = clsManageRequestParameters.get_Request_Parameter(ParamsGroupCode, req).trim().replace("&quot;", "\"");
    	m_sDescription = clsManageRequestParameters.get_Request_Parameter(ParamsDescription, req).trim().replace("&quot;", "\"");
		if(req.getParameter(ParamiActive) == null){
			m_iActive = "0";
		}else{
			if(req.getParameter(ParamiActive).compareToIgnoreCase("0") ==0){
				m_iActive = "0";
			}else{
				m_iActive = "1";
			}
		}
		m_datLastMaintained = clsManageRequestParameters.get_Request_Parameter(ParamdatLastMaintained, req).trim().replace("&quot;", "\"");
		if(m_datLastMaintained.compareToIgnoreCase("") == 0){
			m_datLastMaintained = clsDateAndTimeConversions.now("MM/dd/yyyy");
		}
		m_sLastEditUserFullName = clsManageRequestParameters.get_Request_Parameter(ParamsLastEditUserFullName, req).trim().replace("&quot;", "\"");
		m_lLastEditUserID = clsManageRequestParameters.get_Request_Parameter(ParamlLastEditUserID, req).trim().replace("&quot;", "\"");
    }
	private boolean load(
			String sCustomerGroup,
    		ServletContext context, 
    		String sDBID
			){
		m_sErrorMessageArray.clear();
		try{
			//Get the record to edit:
			String sSQL =  "SELECT * FROM " + SMTablearcustomergroups.TableName + 
					" WHERE (" + 
					"(" + SMTablearcustomergroups.sGroupCode + " = '" + sCustomerGroup + "')" +
				")";
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
	    	System.out.println("Error in load function!!");
	        System.out.println("SQLException: " + ex.getMessage());
	        System.out.println("SQLState: " + ex.getSQLState());
	        System.out.println("SQL: " + ex.getErrorCode());
			return false;
		}
	}

	private boolean loadFromResultSet(ResultSet rs){
		try{
	        if (rs.next()){
	        	m_sGroupCode = clsStringFunctions.checkStringForNull(rs.getString(SMTablearcustomergroups.sGroupCode));
	        	m_sDescription = clsStringFunctions.checkStringForNull(rs.getString(SMTablearcustomergroups.sDescription));
	        	m_iActive = rs.getString(SMTablearcustomergroups.iActive);
	        	if(clsDateAndTimeConversions.IsValidDate(rs.getDate(SMTablearcustomergroups.datLastMaintained))){
	        		m_datLastMaintained = clsDateAndTimeConversions.utilDateToString(rs.getDate(SMTablearcustomergroups.datLastMaintained),"MM/dd/yyyy");
	        	}else{
	        		m_datLastMaintained = clsDateAndTimeConversions.now("MM/dd/yyyy");
	        	}
	        	m_sLastEditUserFullName = clsStringFunctions.checkStringForNull(rs.getString(SMTablearcustomergroups.sLastEditUserFullName));
	        	m_lLastEditUserID = clsStringFunctions.checkStringForNull(Long.toString(rs.getLong(SMTablearcustomergroups.lLastEditUserID)));
	        	m_iNewRecord = "0";
	        	rs.close();
	        	return true;
	        }
	        else{
	        	rs.close();
	        	return false;
	        }
		}catch(SQLException ex){
	    	System.out.println("Error in loadFromResultSet function!!");
	        System.out.println("SQLException: " + ex.getMessage());
	        System.out.println("SQLState: " + ex.getSQLState());
	        System.out.println("SQL: " + ex.getErrorCode());
		}
		return true;
	}
	public boolean load(
    		ServletContext context, 
    		String sDBID
			){

		return load(m_sGroupCode, context, sDBID);
	}

	public boolean save (ServletContext context, String sDBID){
		m_sErrorMessageArray.clear();
		//Check to see if the record already exists:
		String SQL =  "SELECT * FROM " + SMTablearcustomergroups.TableName + 
				" WHERE (" + 
				"(" + SMTablearcustomergroups.sGroupCode + " = '" + m_sGroupCode + "')" +
			")";
		try{
			ResultSet rs = clsDatabaseFunctions.openResultSet(
				SQL, 
				context,
				sDBID,
				"MySQL",
				this.toString() + ".save");
			if(rs.next()){
				//If it's supposed to be a new record, then return an error:
				if(m_iNewRecord.compareToIgnoreCase("1") == 0){
					m_sErrorMessageArray.add("Cannot save - customer group already exists.");
					rs.close();
					return false;
				}
				rs.close();
				
				m_sErrorMessageArray.clear();
				if(!validateEntries()){
					return false;
				}
				
				//Update the record:
				SQL ="UPDATE " + SMTablearcustomergroups.TableName
						+ " SET " 
						+ SMTablearcustomergroups.datLastMaintained + " = NOW(), "
						+ SMTablearcustomergroups.sDescription + " = '" + clsDatabaseFunctions.FormatSQLStatement(m_sDescription) + "', "
						+ SMTablearcustomergroups.iActive + " = " + m_iActive + ", "
						+ SMTablearcustomergroups.sLastEditUserFullName + " = '" + clsDatabaseFunctions.FormatSQLStatement(m_sLastEditUserFullName) + "'" + ","
						+ SMTablearcustomergroups.lLastEditUserID + " = " + clsDatabaseFunctions.FormatSQLStatement(m_lLastEditUserID) 
						
						+ " WHERE (" 
							+ "(" + SMTablearcustomergroups.sGroupCode + " = '" + clsDatabaseFunctions.FormatSQLStatement(m_sGroupCode) + "')"
							+ ")";
				if(!clsDatabaseFunctions.executeSQL(SQL, context, sDBID)){
					m_sErrorMessageArray.add("Cannot execute UPDATE sql.");
					return false;
				}else{
					m_iNewRecord = "0";
					return true;
				}
			}else{
				//If it DOESN'T exist:
				//If it's supposed to be an existing record, then return an error:
				if(m_iNewRecord.compareToIgnoreCase("0") == 0){
					m_sErrorMessageArray.add("Cannot save - can't get existing customer group.");
					rs.close();
					return false;
				}
				rs.close();
				//Insert the record:
				//First, validate the new customer code:
				if (!validateNewCode()){
					return false;
				}
				SQL = "INSERT into " + SMTablearcustomergroups.TableName +
						" (" 
						+ SMTablearcustomergroups.sGroupCode
						+ "," + SMTablearcustomergroups.datLastMaintained
						+ "," + SMTablearcustomergroups.iActive
						+ "," + SMTablearcustomergroups.sDescription
						+ "," + SMTablearcustomergroups.sLastEditUserFullName
						+ "," + SMTablearcustomergroups.lLastEditUserID
					+ ")"
					+ " VALUES ("
						+ "'" + clsDatabaseFunctions.FormatSQLStatement(m_sGroupCode) + "'"
						+ ", NOW()"
						+ ", '" + m_iActive + "'"
						+ ", '" + clsDatabaseFunctions.FormatSQLStatement(m_sDescription) + "'"
						+ ", '" + clsDatabaseFunctions.FormatSQLStatement(m_sLastEditUserFullName) + "'"
						+ ", " + clsDatabaseFunctions.FormatSQLStatement(m_lLastEditUserID) + ""
					+ ")"
					; 
				if(!clsDatabaseFunctions.executeSQL(SQL, context, sDBID)){
					m_sErrorMessageArray.add("Cannot execute INSERT sql.");
					return false;
				}else{
					m_iNewRecord = "0";
					return true;
				}
			}
		}catch(SQLException e){
			System.out.println("Error saving customer group - " + e.getMessage());
			m_sErrorMessageArray.add("Error saving customer group - " + e.getMessage());
			return false;
		}
	}
	private boolean validateNewCode(){
		m_sErrorMessageArray.clear();
		//All upper case:
		m_sGroupCode = m_sGroupCode.toUpperCase();
		
		if(!clsStringFunctions.validateStringCharacters(m_sGroupCode, "ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890-")){
			m_sErrorMessageArray.add("Invalid characters in customer group code");
			return false;
		}
		return true; 
		
	}
	private boolean validateEntries(){
		
		boolean bEntriesAreValid = true;
		m_sErrorMessageArray.clear();
    	if (m_sGroupCode.trim().compareToIgnoreCase("") == 0){
    		m_sErrorMessageArray.add("customer group cannot be blank");
    		bEntriesAreValid = false;
    	}
    	if (m_sGroupCode.length() > SMTablearcustomergroups.sGroupCodeLength){
    		m_sErrorMessageArray.add("customer group code cannot be longer than " 
    			+ SMTablearcustomergroups.sGroupCodeLength + " characters.");
    		bEntriesAreValid = false;
    	}
    	
    	m_sLastEditUserFullName = "";
    	
    	if (m_sDescription.length() > SMTablearcustomergroups.sDescriptionLength){
    		m_sErrorMessageArray.add("description cannot be longer than " 
    			+ SMTablearcustomergroups.sDescriptionLength + " characters.");
    		bEntriesAreValid = false;
    	}
    	if (m_sLastEditUserFullName.length() > SMTablearcustomergroups.sLastEditUserLength){
    		m_sErrorMessageArray.add("last edit user cannot be longer than " 
    			+ SMTablearcustomergroups.sLastEditUserLength + " characters.");
    		bEntriesAreValid = false;
    	}
    	return bEntriesAreValid;
	}
	public String getQueryString(){
		
		String sQueryString = "";
		sQueryString += ParamsAddingNewRecord + "=" + clsServletUtilities.URLEncode(m_iNewRecord);
		sQueryString += "&" + ParamsGroupCode + "=" + clsServletUtilities.URLEncode(m_sGroupCode);
		sQueryString += "&" + ParamsDescription + "=" + clsServletUtilities.URLEncode(m_sDescription);
		sQueryString += "&" + ParamiActive + "=" + clsServletUtilities.URLEncode(m_iActive);
		sQueryString += "&" + ParamdatLastMaintained + "=" + clsServletUtilities.URLEncode(m_datLastMaintained);
		sQueryString += "&" + ParamsLastEditUserFullName + "=" + clsServletUtilities.URLEncode(m_sLastEditUserFullName);
		sQueryString += "&" + ParamlLastEditUserID + "=" + clsServletUtilities.URLEncode(m_lLastEditUserID);
				
		return sQueryString;
	}
	public boolean delete(String sCustomerGroup, ServletContext context, String sDBID){
		
		m_sErrorMessageArray.clear();
		
		//First, check that the customer exists:
		String SQL = "SELECT * FROM " + SMTablearcustomergroups.TableName + 
				" WHERE (" + 
				"(" + SMTablearcustomergroups.sGroupCode + " = '" + sCustomerGroup + "')" +
			")";
		
		try{
			ResultSet rs = clsDatabaseFunctions.openResultSet(
				SQL, 
				context,
				sDBID,
				"MySQL",
				this.toString() + ".delete (1)");
			if(!rs.next()){
				m_sErrorMessageArray.add("Customer group " + sCustomerGroup + " cannot be found.");
				rs.close();
				return false;
			}else{
				rs.close();
			}
		}catch(SQLException e){
			System.out.println("Error checking customer group to delete - " + e.getMessage());
			m_sErrorMessageArray.add("Error checking customer group to delete - " + e.getMessage());
			return false;
		}
		
		SQL = "SELECT * FROM " + SMTablearcustomer.TableName + 
				" WHERE (" + 
				"(" + SMTablearcustomer.sCustomerGroup + " = '" + sCustomerGroup + "')" +
			")";
		try{
			ResultSet rs = clsDatabaseFunctions.openResultSet(
					SQL, 
					context,
					sDBID,
					"MySQL",
					this.toString() + ".delete (2)");
			if(rs.next()){
				m_sErrorMessageArray.add("Customer group " + sCustomerGroup + " is used for some current customers.");
				rs.close();
				return false;
			}else{
				rs.close();
			}
		}catch(SQLException e){
			System.out.println("Error checking customer group - " + e.getMessage());
			m_sErrorMessageArray.add("Error checking customer group - " + e.getMessage());
			return false;
		}
		
		try{
			SQL ="DELETE FROM " +
					SMTablearcustomergroups.TableName +
					" WHERE (" + 
						"(" + SMTablearcustomergroups.sGroupCode + " = '" + sCustomerGroup + "')" +
					")";
			if(!clsDatabaseFunctions.executeSQL(SQL, context, sDBID)){
				m_sErrorMessageArray.add("Error deleting customer");
				return false;
			}

		}catch(SQLException e){
			System.out.println("Error deleting customer - " + e.getMessage());
			m_sErrorMessageArray.add("Error deleting customer - " + e.getMessage());
			return false;
		}
		
		return true;
	}
	public String getM_sGroupCode() {
		return m_sGroupCode;
	}

	public void setM_sGroupCode(String groupCode) {
		m_sGroupCode = groupCode;
	}

	public String getM_sDescription() {
		return m_sDescription;
	}

	public void setM_sDescription(String description) {
		m_sDescription = description;
	}

	public String getM_iActive() {
		return m_iActive;
	}
	public void setM_bActive(String active) {
		m_iActive = active;
	}
	public String getM_datLastMaintained() {
		return m_datLastMaintained;
	}
	public void setM_datLastMaintained(String lastMaintained) {
		m_datLastMaintained = lastMaintained;
	}
	public String getM_sLastEditUserFullName() {
		return m_sLastEditUserFullName;
	}
	public void setM_sLastEditUserFullName(String lastEditUser) {
		m_sLastEditUserFullName = lastEditUser.trim();
	}
	public String getM_iNewRecord() {
		return m_iNewRecord;
	}
	public void setM_bNewRecord(String newRecord) {
		m_iNewRecord = newRecord;
	}
	public String getErrorMessageString(){
		String s = "";
		for (int i = 0; i < m_sErrorMessageArray.size(); i ++){
			s += "\n" + m_sErrorMessageArray.get(i);
		}
		return s;
	}
}