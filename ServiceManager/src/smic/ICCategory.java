package smic;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import java.text.ParseException;
import java.sql.Connection;
import java.util.ArrayList;
import SMDataDefinition.*;
import ServletUtilities.clsServletUtilities;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsDateAndTimeConversions;
import ServletUtilities.clsManageRequestParameters;
import ServletUtilities.clsStringFunctions;
import smar.*;

public class ICCategory extends Object{
	public static final String ParamAddingNewRecord = "bAddingNewRecord";
    public static String ParamCategoryCode = "CategoryCode";
    public static String ParamDescription = "Description";
    public static String ParamCostOfGoodsSoldAccount = "CostOfGoodsSoldAccount";
    public static String ParamSalesAccount = "SalesAccount";
    public static String ParamActive = "Active";
    public static String ParamLastMaintainedDate = "LastMaintainedDate";
    public static String ParamInactiveDate = "InactiveDate";
    public static String ParamLastEditUser = "LastEditUser";
    
    public static String sCategoryCode = "scategorycode";
    public static String sDescription = "sdescription";
    public static String sCostofGoodsSoldAccount = "scostofgoodssoldacct";
    public static String sSalesAccount = "ssalesaccount";
    public static String iActive = "iactive";
    public static String datLastMaintained = "datlastmaintained";
    public static String datInactive = "datinactive";
    public static String sLastEditUser = "slastedituser";

    private String m_sCategoryCode;
    private String m_sDescription;
    private String m_sCostOfGoodsSoldAccount;
    private String m_sSalesAccount;
    private String m_sActive;
    private String m_sLastMaintainedDate;
    private String m_sInactiveDate;
    private String m_sLastEditUser;
    private String m_sNewRecord;
	private ArrayList<String> m_sErrorMessageArray = new ArrayList<String> (0);

    public ICCategory(
    		String sCategory
        ) {
    	m_sCategoryCode = sCategory;
    	m_sLastEditUser = "";
    	m_sDescription = "";
    	m_sCostOfGoodsSoldAccount = "";
    	m_sSalesAccount = "";
    	m_sActive = "1";
    	m_sLastMaintainedDate = clsDateAndTimeConversions.now("MM/dd/yyyy");;
    	m_sInactiveDate = "00/00/0000";
    	m_sNewRecord = "1";
    	m_sErrorMessageArray = new ArrayList<String> (0);
        }
    
    public void loadFromHTTPRequest(HttpServletRequest req){

		m_sCategoryCode = clsManageRequestParameters.get_Request_Parameter(ICCategory.ParamCategoryCode, req).trim().replace("&quot;", "\"");
		m_sLastEditUser = clsManageRequestParameters.get_Request_Parameter(ICCategory.ParamLastEditUser, req).trim().replace("&quot;", "\"");
		m_sDescription = clsManageRequestParameters.get_Request_Parameter(ICCategory.ParamDescription, req).trim().replace("&quot;", "\"");
		m_sCostOfGoodsSoldAccount = clsManageRequestParameters.get_Request_Parameter(ICCategory.ParamCostOfGoodsSoldAccount, req).trim().replace("&quot;", "\"");
		m_sSalesAccount = clsManageRequestParameters.get_Request_Parameter(ICCategory.ParamSalesAccount, req).trim().replace("&quot;", "\"");
		if(req.getParameter(ICCategory.ParamActive) == null){
			m_sActive = "0";
		}else{
			m_sActive = "1";
		}
		m_sLastMaintainedDate = clsManageRequestParameters.get_Request_Parameter(ICCategory.ParamLastMaintainedDate, req).trim().replace("&quot;", "\"");
		if(m_sLastMaintainedDate.compareToIgnoreCase("") == 0){
			m_sLastMaintainedDate = "00/00/0000";
		}
		m_sInactiveDate = clsManageRequestParameters.get_Request_Parameter(ICCategory.ParamInactiveDate, req).trim().replace("&quot;", "\"");
		if(m_sInactiveDate.compareToIgnoreCase("") == 0){
			m_sInactiveDate = "00/00/0000";
		}
		m_sNewRecord = clsManageRequestParameters.get_Request_Parameter(ICCategory.ParamAddingNewRecord, req).trim().replace("&quot;", "\"");
    }
    private boolean load(
			String sCategoryCode,
    		ServletContext context, 
    		String sDBID
			){
		m_sErrorMessageArray.clear();
		try{
			//Get the record to edit:
			String sSQL = "SELECT * FROM " + SMTableiccategories.TableName
				+ " WHERE ("
					+ SMTableiccategories.sCategoryCode + " = '" + sCategoryCode + "'"
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
	    	System.out.println("Error in load function!!");
	        System.out.println("SQLException: " + ex.getMessage());
	        System.out.println("SQLState: " + ex.getSQLState());
	        System.out.println("SQL: " + ex.getErrorCode());
			return false;
		}
	}
	//Need this one with the connection:
	private boolean load(
			String sCategoryCode,
			Connection conn
			){
		m_sErrorMessageArray.clear();
		try{
			//Get the record to edit:
			String sSQL = "SELECT * FROM " + SMTableiccategories.TableName
				+ " WHERE ("
					+ SMTableiccategories.sCategoryCode + " = '" + sCategoryCode + "'"
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
	        	m_sCategoryCode = clsStringFunctions.checkStringForNull(rs.getString(SMTableiccategories.sCategoryCode));
	        	m_sLastEditUser = clsStringFunctions.checkStringForNull(rs.getString(SMTableiccategories.sLastEditUserFullName));
	        	m_sDescription = clsStringFunctions.checkStringForNull(rs.getString(SMTableiccategories.sDescription));
	        	m_sCostOfGoodsSoldAccount = clsStringFunctions.checkStringForNull(rs.getString(SMTableiccategories.sCostofGoodsSoldAccount));
	        	m_sSalesAccount = clsStringFunctions.checkStringForNull(rs.getString(SMTableiccategories.sSalesAccount));
	        	m_sActive = Integer.toString(rs.getInt(SMTableiccategories.iActive));
	        	String sDate = rs.getString(SMTableiccategories.datLastMaintained);
	        	m_sLastMaintainedDate = sDate.substring(5, 7) + "/" + sDate.substring(8, 10) + "/" + sDate.substring(0, 4);
	        	sDate = rs.getString(SMTableiccategories.datInactive);
	        	m_sInactiveDate = sDate.substring(5, 7) + "/" + sDate.substring(8, 10) + "/" + sDate.substring(0, 4);
	        	m_sNewRecord = "0";
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

		return load(m_sCategoryCode, context, sDBID);
	}
	public boolean load(
    		Connection conn
			){

		return load(m_sCategoryCode, conn);
	}
	//Need a connection here for the data transaction:
	public boolean save (String sUserFullName, String sUserID, String sCompany, Connection conn){
		m_sErrorMessageArray.clear();
		//Check to see if the record already exists:
		String SQL = "SELECT * FROM " + SMTableiccategories.TableName
			+ " WHERE ("
				+ SMTableiccategories.sCategoryCode + " = '" + m_sCategoryCode + "'"
			+ ")"
			;
		try{
			ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
			
			//Update the record:
			String sInactiveDate = "";
			if(m_sActive.compareToIgnoreCase("1") == 0){
				sInactiveDate = "0000-00-00";
				m_sInactiveDate = "00/00/0000";
			}else{
				try {
					sInactiveDate = clsDateAndTimeConversions.utilDateToString(
						clsDateAndTimeConversions.StringTojavaSQLDate("MM/dd/yyyy", m_sInactiveDate),"yyyy-MM-dd");
				} catch (ParseException e) {
					m_sErrorMessageArray.add("Invalid inactive date - Error:[1423677943] - '" + sInactiveDate + "' - " + e.getMessage());
					rs.close();
					return false;
				}
			}
			
			if(rs.next()){
				//If it's supposed to be a new record, then return an error:
				if(m_sNewRecord.compareToIgnoreCase("1") == 0){
					m_sErrorMessageArray.add("Cannot save - category already exists.");
					rs.close();
					return false;
				}
				rs.close();
				
				m_sErrorMessageArray.clear();
				if(!validateEntries()){
					return false;
				}
				SQL = "UPDATE " + SMTableiccategories.TableName + " SET "
					+ " " + SMTableiccategories.sLastEditUserFullName + " = '" + clsDatabaseFunctions.FormatSQLStatement(sUserFullName) + "'"
					+ ", " + SMTableiccategories.lLastEditUserID + " = " + sUserID + ""
					+ ", " + SMTableiccategories.sDescription + " = '" + clsDatabaseFunctions.FormatSQLStatement(m_sDescription) + "'"
					+ ", " + SMTableiccategories.sCostofGoodsSoldAccount + " = '" + clsDatabaseFunctions.FormatSQLStatement(m_sCostOfGoodsSoldAccount) + "'"
					+ ", " + SMTableiccategories.sSalesAccount + " = '" + clsDatabaseFunctions.FormatSQLStatement(m_sSalesAccount) + "'"
					+ ", " + SMTableiccategories.iActive + " = " + m_sActive
					+ ", " + SMTableiccategories.datLastMaintained + " = '" + clsDateAndTimeConversions.nowSqlFormat() + "'"
					+ ", " + SMTableiccategories.datInactive + " = '" + sInactiveDate + "'"
					
					+ " WHERE ("
						+ SMTableiccategories.sCategoryCode + " = '" + clsDatabaseFunctions.FormatSQLStatement(m_sCategoryCode) + "'"
					+ ")"
					;
					
				if(!clsDatabaseFunctions.executeSQL(SQL, conn)){
					m_sErrorMessageArray.add("Cannot execute UPDATE sql.");
					return false;
				}else{
					m_sNewRecord = "0";
					return true;
				}
			}else{
				//If it DOESN'T exist:
				//If it's supposed to be an existing record, then return an error:
				if(m_sNewRecord.compareToIgnoreCase("0") == 0){
					m_sErrorMessageArray.add("Cannot save - can't get existing category.");
					rs.close();
					return false;
				}
				rs.close();
				//Insert the record:
				//First, validate the new category code:
				if (!validateNewCode()){
					return false;
				}
				if(!validateEntries()){
					return false;
				}
				
				if (!clsDatabaseFunctions.start_data_transaction(conn)){
					m_sErrorMessageArray.add("Could not start data transaction for category insert.");
					return false;
				}
				
				SQL = "INSERT INTO " + SMTableiccategories.TableName + " ("
					+ SMTableiccategories.datInactive
					+ ", " + SMTableiccategories.datLastMaintained
					+ ", " + SMTableiccategories.iActive
					+ ", " + SMTableiccategories.sCategoryCode
					+ ", " + SMTableiccategories.sDescription
					+ ", " + SMTableiccategories.sCostofGoodsSoldAccount
					+ ", " + SMTableiccategories.sLastEditUserFullName
					+ ", " + SMTableiccategories.lLastEditUserID
					+ ", " + SMTableiccategories.sSalesAccount

					+ " ) VALUES ("
						+ "'" + sInactiveDate + "'" 
						+ ", '" + clsDateAndTimeConversions.nowSqlFormat() + "'" 
						+ ", " + m_sActive
						+ ", '" + clsDatabaseFunctions.FormatSQLStatement(m_sCategoryCode) + "'"
						+ ", '" + clsDatabaseFunctions.FormatSQLStatement(m_sDescription) + "'"
						+ ", '" + clsDatabaseFunctions.FormatSQLStatement(m_sCostOfGoodsSoldAccount) + "'"
						+ ", '" + clsDatabaseFunctions.FormatSQLStatement(sUserFullName) + "'"
						+ ", " + clsDatabaseFunctions.FormatSQLStatement(sUserID) + ""
						+ ", '" + clsDatabaseFunctions.FormatSQLStatement(m_sSalesAccount) + "'"
					+ ")"
					;
				if(!clsDatabaseFunctions.executeSQL(SQL, conn)){
					clsDatabaseFunctions.rollback_data_transaction(conn);
					m_sErrorMessageArray.add("Cannot execute category INSERT sql.");
					return false;
				}

				if (!clsDatabaseFunctions.commit_data_transaction(conn)){
					clsDatabaseFunctions.rollback_data_transaction(conn);
					m_sErrorMessageArray.add("Cannot commit data transaction.");
					return false;
				}
				
				//If we get to here, we've succeeded:
				m_sNewRecord = "0";
				return true;				
			}
		}catch(SQLException e){
			System.out.println("Error saving category - " + e.getMessage());
			m_sErrorMessageArray.add("Error saving category - " + e.getMessage());
			return false;
		}
	}

	private boolean validateNewCode(){
		m_sErrorMessageArray.clear();
		//All upper case:
		m_sCategoryCode = m_sCategoryCode.toUpperCase();
		
		if(!clsStringFunctions.validateStringCharacters(m_sCategoryCode, "ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890-")){
			m_sErrorMessageArray.add("Invalid characters in category code");
			return false;
		}
		return true; 
		
	}
	private boolean validateEntries(){
		
		boolean bEntriesAreValid = true;
		m_sErrorMessageArray.clear();
    	if (m_sCategoryCode.trim().compareToIgnoreCase("") == 0){
    		m_sErrorMessageArray.add("category code cannot be blank");
    		bEntriesAreValid = false;
    	}
    	if (m_sCategoryCode.length() > SMTableiccategories.sCategoryCodeLength){
    		m_sErrorMessageArray.add("category code cannot be longer than " 
    			+ SMTableiccategories.sCategoryCodeLength + " characters.");
    		bEntriesAreValid = false;
    	}
    	if (m_sLastEditUser.length() > SMTableiccategories.sLastEditUserLength){
    		m_sErrorMessageArray.add("Last Edit User cannot be longer than " 
    			+ SMTableiccategories.sLastEditUserLength + " characters.");
    		bEntriesAreValid = false;
    	}
    	if (m_sDescription.trim().compareToIgnoreCase("") == 0){
    		m_sErrorMessageArray.add("description cannot be blank");
    		bEntriesAreValid = false;
    	}
    	if (m_sDescription.length() > SMTableiccategories.sDescriptionLength){
    		m_sErrorMessageArray.add("description cannot be longer than " 
    			+ SMTableiccategories.sDescriptionLength + " characters.");
    		bEntriesAreValid = false;
    	}
    	if (m_sCostOfGoodsSoldAccount.trim().compareToIgnoreCase("") == 0){
    		m_sErrorMessageArray.add("cost of goods sold account cannot be blank");
    		bEntriesAreValid = false;
    	}
    	if (m_sCostOfGoodsSoldAccount.length() > SMTableiccategories.sCostofGoodsSoldAccountLength){
    		m_sErrorMessageArray.add("cost of goods sold account cannot be longer than " 
    			+ SMTableiccategories.sCostofGoodsSoldAccountLength + " characters.");
    		bEntriesAreValid = false;
    	}
    	if (m_sSalesAccount.trim().compareToIgnoreCase("") == 0){
    		m_sErrorMessageArray.add("sales account cannot be blank");
    		bEntriesAreValid = false;
    	}
    	if (m_sSalesAccount.length() > SMTableiccategories.sSalesAccountLength){
    		m_sErrorMessageArray.add("sales account cannot be longer than " 
    			+ SMTableiccategories.sSalesAccountLength + " characters.");
    		bEntriesAreValid = false;
    	}
    	if(!clsDateAndTimeConversions.IsValidDateString("MM/dd/yyyy", m_sLastMaintainedDate)){
    		//Allow an empty date:
    		if (m_sLastMaintainedDate.compareToIgnoreCase("00/00/0000") == 0){
    		}else{
        		m_sErrorMessageArray.add("Invalid last maintained date: " + m_sLastMaintainedDate); 
        		bEntriesAreValid = false;
    		}
    	}
    	if(!clsDateAndTimeConversions.IsValidDateString("MM/dd/yyyy", m_sInactiveDate)){
    		//Allow an empty date:
    		if (m_sInactiveDate.compareToIgnoreCase("00/00/0000") == 0){
    		}else{
        		m_sErrorMessageArray.add("Invalid inactive date: " + m_sInactiveDate); 
        		bEntriesAreValid = false;
    		}
    	}

    	return bEntriesAreValid;
	}
	public String getQueryString(){
		
		String sQueryString = "";
		sQueryString += ICCategory.ParamAddingNewRecord + "=" + clsServletUtilities.URLEncode(m_sNewRecord);
		sQueryString += "&" + ICCategory.ParamCategoryCode + "=" + clsServletUtilities.URLEncode(m_sCategoryCode);
		if (m_sActive.compareToIgnoreCase("1") == 0){
			sQueryString += "&" + ICCategory.ParamActive + "=" + m_sActive;
		}
		sQueryString += "&" + ICCategory.ParamDescription + "=" + clsServletUtilities.URLEncode(m_sDescription);
		sQueryString += "&" + ICCategory.ParamInactiveDate + "=" + clsServletUtilities.URLEncode(m_sInactiveDate);
		sQueryString += "&" + ICCategory.ParamCostOfGoodsSoldAccount + "=" + clsServletUtilities.URLEncode(m_sCostOfGoodsSoldAccount);
		sQueryString += "&" + ICCategory.ParamLastEditUser + "=" + clsServletUtilities.URLEncode(m_sLastEditUser);
		sQueryString += "&" + ICCategory.ParamLastMaintainedDate + "=" + clsServletUtilities.URLEncode(m_sLastMaintainedDate);
		sQueryString += "&" + ICCategory.ParamSalesAccount + "=" + clsServletUtilities.URLEncode(m_sSalesAccount);
				
		return sQueryString;
	}
	//Requires connection since it is used as part of a data transaction in places:
	public boolean delete(String sCategory, Connection conn){
		
		m_sErrorMessageArray.clear();
		
		//First, check that the category exists:
		String SQL = "SELECT * FROM " + SMTableiccategories.TableName
			+ " WHERE ("
				+ SMTableiccategories.sCategoryCode + " = '" + m_sCategoryCode + "'"
			+ ")"
		;
		try{
			ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
			if(!rs.next()){
				m_sErrorMessageArray.add("Category " + sCategory + " cannot be found.");
				rs.close();
				return false;
			}else{
				rs.close();
			}
			
		}catch(SQLException e){
			System.out.println("Error checking category " + sCategory + " to delete - " + e.getMessage());
			m_sErrorMessageArray.add("Error checking category " + sCategory + " to delete - " + e.getMessage());
			return false;
		}
		
		//Check tables that category depends on:
		SQL = "SELECT " 
			+ SMTableicitems.sItemNumber
			+ " FROM " + SMTableicitems.TableName
			+ " WHERE ("
				+ "(" + SMTableicitems.sCategoryCode + " = '" + m_sCategoryCode + "')"
			+ ")"
			;
		try{
			ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
			if(rs.next()){
				m_sErrorMessageArray.add("Category " + sCategory + " is used on some items.");
				rs.close();
				return false;
			}else{
				rs.close();
			}
		}catch(SQLException e){
			System.out.println("Error checking items for " + sCategory + " - " + e.getMessage());
			m_sErrorMessageArray.add("Error checking items for " + sCategory + " - " + e.getMessage());
			return false;
		}
		
		//Now begin a data transaction that we can roll back:
		if(!clsDatabaseFunctions.start_data_transaction(conn)){
			m_sErrorMessageArray.add("Error beginning data transaction to delete category " + sCategory + "");
			return false;
		}
		
		try{
			SQL = "DELETE FROM " + SMTableiccategories.TableName
				+ " WHERE ("
					+ SMTableiccategories.sCategoryCode + " = '" + m_sCategoryCode + "'"
				+ ")"
			;
			if(!clsDatabaseFunctions.executeSQL(SQL, conn)){
				m_sErrorMessageArray.add("Error deleting category " + sCategory);
				clsDatabaseFunctions.rollback_data_transaction(conn);
				return false;
			}

			//TODO - delete any related table records:

		}catch(SQLException e){
			System.out.println("Error deleting category " + sCategory + " - " + e.getMessage());
			m_sErrorMessageArray.add("Error deleting category " + sCategory + " - " + e.getMessage());
			return false;
		}
		
		if(!clsDatabaseFunctions.commit_data_transaction(conn)){
			clsDatabaseFunctions.rollback_data_transaction(conn);
			return false;
		}
		
		return true;
	}
    public String getCategoryCode(){
    	return m_sCategoryCode;
    }
    public void setCategoryCode(String sCategoryCode){
    	m_sCategoryCode = sCategoryCode.trim();
    }
	public String getLastEditUser() {
		return m_sLastEditUser;
	}
	public void setLastEditUser(String sLastEditUser) {
		m_sLastEditUser = sLastEditUser.trim();
	}
    public String getDescription(){
    	return m_sDescription;
    }
	public void setDescription(String sDescription) {
		m_sDescription = sDescription.trim();
	}
    public String getCostOfGoodsSoldAccount(){
    	return m_sCostOfGoodsSoldAccount;
    }
	public void setCostOfGoodsSoldAccount(String sCostOfGoodsSoldAccount) {
		m_sCostOfGoodsSoldAccount = sCostOfGoodsSoldAccount.trim();
	}
    public String getSalesAccount(){
    	return m_sSalesAccount;
    }
	public void setSalesAccount(String sSalesAccount) {
		m_sSalesAccount = sSalesAccount.trim();
	}
	public String getActive() {
		return m_sActive;
	}
	public void setActive(String sActive) {
		m_sActive = sActive;
	}
	public String getLastMaintainedDate() {
		return m_sLastMaintainedDate;
	}
	public void setLastMaintainedDate(String sLastMaintainedDate) {
		m_sLastMaintainedDate = sLastMaintainedDate;
	}
	public String getInactiveDate() {
		return m_sInactiveDate;
	}
	public void setInactiveDate(String sInactiveDate) {
		m_sInactiveDate = sInactiveDate.trim();
	}
	public String getNewRecord() {
		return m_sNewRecord;
	}
	public void setNewRecord(String newRecord) {
		m_sNewRecord = newRecord;
	}
	public String getErrorMessageString(){
		String s = "";
		for (int i = 0; i < m_sErrorMessageArray.size(); i ++){
			s += "<BR>" + m_sErrorMessageArray.get(i);
		}
		return s;
	}
}
