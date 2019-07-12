package smic;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import java.sql.Connection;
import java.text.ParseException;
import java.util.ArrayList;

import SMDataDefinition.*;
import ServletUtilities.clsServletUtilities;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsDateAndTimeConversions;
import ServletUtilities.clsManageRequestParameters;
import ServletUtilities.clsStringFunctions;

public class ICAccountSet extends Object{
	public static final String ParamAddingNewRecord = "bAddingNewRecord";
	
    public static String ParamAccountSetCode = "AccountSetCode";
    public static String ParamLastEditUser = "LastEditUser";
    public static String ParamDescription = "Description";
    public static String ParamCostingMethod = "CostingMethod";
    public static String ParamInventoryAccount = "InventoryAccount";
    public static String ParamPayablesClearingAccount = "PayablesClearingAccount";
    public static String ParamAdjustmentWriteOffAccount = "AdjustmentWriteOffAccount";
    public static String ParamNonStockClearingAccount = "NonStockClearingAccount";
    public static String ParamTransferClearingAccount = "TransferClearingAccount";
    public static String ParamActive = "Active";
    public static String ParamLastMaintainedDate = "LastMaintainedDate";
    public static String ParamInactiveDate = "InactiveDate";

    private String m_sAccountSetCode;
    private String m_sLastEditUser;
    private String m_sDescription;
    private String m_sInventoryAccount;
    private String m_sPayablesClearingAccount;
    private String m_sAdjustmentWriteOffAccount;
    private String m_sNonStockClearingAccount;
    private String m_sTransferClearingAccount;
    private String m_sActive;
    private String m_sLastMaintainedDate;
    private String m_sInactiveDate;
    private String m_sNewRecord;

	private ArrayList<String> m_sErrorMessageArray = new ArrayList<String> (0);

    ICAccountSet(
    		String sAccountSetCode
        ) {
    	m_sAccountSetCode = sAccountSetCode;
    	m_sLastEditUser = "";
    	m_sDescription = "";
    	m_sInventoryAccount = "";
    	m_sPayablesClearingAccount = "";
    	m_sAdjustmentWriteOffAccount = "";
    	m_sNonStockClearingAccount = "";
    	m_sTransferClearingAccount = "";
    	m_sActive = "1";
    	m_sLastMaintainedDate = clsDateAndTimeConversions.now("MM/dd/yyyy");;
    	m_sInactiveDate = "00/00/0000";
    	m_sNewRecord = "1";
    	m_sErrorMessageArray = new ArrayList<String> (0);
        }
    
    public void loadFromHTTPRequest(HttpServletRequest req){

		m_sAccountSetCode = clsManageRequestParameters.get_Request_Parameter(ICAccountSet.ParamAccountSetCode, req).trim().replace("&quot;", "\"");
		m_sLastEditUser = clsManageRequestParameters.get_Request_Parameter(ICAccountSet.ParamLastEditUser, req).trim().replace("&quot;", "\"");
		m_sDescription = clsManageRequestParameters.get_Request_Parameter(ICAccountSet.ParamDescription, req).trim().replace("&quot;", "\"");
		m_sInventoryAccount = clsManageRequestParameters.get_Request_Parameter(ICAccountSet.ParamInventoryAccount, req).trim().replace("&quot;", "\"");
		m_sPayablesClearingAccount = clsManageRequestParameters.get_Request_Parameter(ICAccountSet.ParamPayablesClearingAccount, req).trim().replace("&quot;", "\"");
		m_sAdjustmentWriteOffAccount = clsManageRequestParameters.get_Request_Parameter(ICAccountSet.ParamAdjustmentWriteOffAccount, req).trim().replace("&quot;", "\"");
		m_sNonStockClearingAccount = clsManageRequestParameters.get_Request_Parameter(ICAccountSet.ParamNonStockClearingAccount, req).trim().replace("&quot;", "\"");
		m_sTransferClearingAccount = clsManageRequestParameters.get_Request_Parameter(ICAccountSet.ParamTransferClearingAccount, req).trim().replace("&quot;", "\"");
		m_sTransferClearingAccount = clsManageRequestParameters.get_Request_Parameter(ICAccountSet.ParamTransferClearingAccount, req).trim().replace("&quot;", "\"");
		if(req.getParameter(ICAccountSet.ParamActive) == null){
			m_sActive = "0";
		}else{
			m_sActive = "1";
		}
		m_sLastMaintainedDate = clsManageRequestParameters.get_Request_Parameter(ICAccountSet.ParamLastMaintainedDate, req).trim().replace("&quot;", "\"");
		if(m_sLastMaintainedDate.compareToIgnoreCase("") == 0){
			m_sLastMaintainedDate = "00/00/0000";
		}
		m_sInactiveDate = clsManageRequestParameters.get_Request_Parameter(ICAccountSet.ParamInactiveDate, req).trim().replace("&quot;", "\"");
		if(m_sInactiveDate.compareToIgnoreCase("") == 0){
			m_sInactiveDate = "00/00/0000";
		}
		m_sNewRecord = clsManageRequestParameters.get_Request_Parameter(ICAccountSet.ParamAddingNewRecord, req).trim().replace("&quot;", "\"");
    }
    private boolean load(
			String sAccountSetCode,
    		ServletContext context, 
    		String sDBID
			){
		m_sErrorMessageArray.clear();
		try{
			//Get the record to edit:
			String sSQL = "SELECT * FROM " + SMTableicaccountsets.TableName
				+ " WHERE ("
					+ SMTableicaccountsets.sAccountSetCode + " = '" + sAccountSetCode + "'"
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
			String sAccountSetCode,
			Connection conn
			){
		m_sErrorMessageArray.clear();
		try{
			//Get the record to edit:
			String sSQL = "SELECT * FROM " + SMTableicaccountsets.TableName
				+ " WHERE ("
					+ SMTableicaccountsets.sAccountSetCode + " = '" + sAccountSetCode + "'"
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
	        	/*
    	m_sNewRecord = "1";
	        	 */
	        	
	        	m_sAccountSetCode = clsStringFunctions.checkStringForNull(rs.getString(SMTableicaccountsets.sAccountSetCode));
	        	m_sLastEditUser = clsStringFunctions.checkStringForNull(rs.getString(SMTableicaccountsets.sLastEditUserFullName));
	        	m_sDescription = clsStringFunctions.checkStringForNull(rs.getString(SMTableicaccountsets.sDescription));
	        	m_sInventoryAccount = clsStringFunctions.checkStringForNull(rs.getString(SMTableicaccountsets.sInventoryAccount));
	        	m_sPayablesClearingAccount = clsStringFunctions.checkStringForNull(rs.getString(SMTableicaccountsets.sPayablesClearingAccount));
	        	m_sAdjustmentWriteOffAccount = clsStringFunctions.checkStringForNull(rs.getString(SMTableicaccountsets.sAdjustmentWriteOffAccount));
	        	m_sNonStockClearingAccount = clsStringFunctions.checkStringForNull(rs.getString(SMTableicaccountsets.sNonStockClearingAccount));
	        	m_sTransferClearingAccount = clsStringFunctions.checkStringForNull(rs.getString(SMTableicaccountsets.sTransferClearingAccount));
	        	m_sActive = Integer.toString(rs.getInt(SMTableicaccountsets.iActive));
	        	String sDate = rs.getString(SMTableicaccountsets.datLastMaintained);
	        	m_sLastMaintainedDate = sDate.substring(5, 7) + "/" + sDate.substring(8, 10) + "/" + sDate.substring(0, 4);
	        	sDate = rs.getString(SMTableicaccountsets.datInactive);
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

		return load(m_sAccountSetCode, context, sDBID);
	}
	public boolean load(
    		Connection conn
			){

		return load(m_sAccountSetCode, conn);
	}
	//Need a connection here for the data transaction:
	public boolean save (String sUserFullName, String sUserID, String sCompany, Connection conn){
		m_sErrorMessageArray.clear();
		//Check to see if the record already exists:
		String SQL = "SELECT * FROM " + SMTableicaccountsets.TableName
			+ " WHERE ("
				+ SMTableicaccountsets.sAccountSetCode + " = '" + m_sAccountSetCode + "'"
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
					m_sErrorMessageArray.add("Invalid inactive date - Error:[1423670858] - '" + sInactiveDate + "' - " + e.getMessage());
					rs.close();
					return false;
				}
			}
			
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
				SQL = "UPDATE " + SMTableicaccountsets.TableName + " SET "
					+ " " + SMTableicaccountsets.sLastEditUserFullName + " = '" + clsDatabaseFunctions.FormatSQLStatement(sUserFullName) + "'"
					+ ", " + SMTableicaccountsets.lLastEditUserID + " = " + clsDatabaseFunctions.FormatSQLStatement(sUserID) + ""
					+ ", " + SMTableicaccountsets.sDescription + " = '" + clsDatabaseFunctions.FormatSQLStatement(m_sDescription) + "'"
					+ ", " + SMTableicaccountsets.sInventoryAccount + " = '" + clsDatabaseFunctions.FormatSQLStatement(m_sInventoryAccount) + "'"
					+ ", " + SMTableicaccountsets.sPayablesClearingAccount + " = '" + clsDatabaseFunctions.FormatSQLStatement(m_sPayablesClearingAccount) + "'"
					+ ", " + SMTableicaccountsets.sAdjustmentWriteOffAccount + " = '" + clsDatabaseFunctions.FormatSQLStatement(m_sAdjustmentWriteOffAccount) + "'"
					+ ", " + SMTableicaccountsets.sNonStockClearingAccount + " = '" + clsDatabaseFunctions.FormatSQLStatement(m_sNonStockClearingAccount) + "'"
					+ ", " + SMTableicaccountsets.sTransferClearingAccount + " = '" + clsDatabaseFunctions.FormatSQLStatement(m_sTransferClearingAccount) + "'"
					+ ", " + SMTableicaccountsets.iActive + " = " + m_sActive
					+ ", " + SMTableicaccountsets.datLastMaintained + " = '" + clsDateAndTimeConversions.nowSqlFormat() + "'"
					+ ", " + SMTableicaccountsets.datInactive + " = '" + sInactiveDate + "'"
					
					+ " WHERE ("
						+ SMTableicaccountsets.sAccountSetCode + " = '" + clsDatabaseFunctions.FormatSQLStatement(m_sAccountSetCode) + "'"
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
					m_sErrorMessageArray.add("Cannot save - can't get existing account set.");
					rs.close();
					return false;
				}
				rs.close();
				//Insert the record:
				//First, validate the new account set code:
				if (!validateNewCode()){
					return false;
				}
				if(!validateEntries()){
					return false;
				}
				
				if (!clsDatabaseFunctions.start_data_transaction(conn)){
					m_sErrorMessageArray.add("Could not start data transaction for account set insert.");
					return false;
				}
				
				SQL = "INSERT INTO " + SMTableicaccountsets.TableName + " ("
					+ SMTableicaccountsets.datInactive
					+ ", " + SMTableicaccountsets.datLastMaintained
					+ ", " + SMTableicaccountsets.iActive
					+ ", " + SMTableicaccountsets.sAccountSetCode
					+ ", " + SMTableicaccountsets.sAdjustmentWriteOffAccount
					+ ", " + SMTableicaccountsets.sDescription
					+ ", " + SMTableicaccountsets.sInventoryAccount
					+ ", " + SMTableicaccountsets.sLastEditUserFullName
					+ ", " + SMTableicaccountsets.lLastEditUserID
					+ ", " + SMTableicaccountsets.sNonStockClearingAccount
					+ ", " + SMTableicaccountsets.sPayablesClearingAccount
					+ ", " + SMTableicaccountsets.sTransferClearingAccount

					+ " ) VALUES ("
						+ "'" + sInactiveDate + "'" 
						+ ", '" + clsDateAndTimeConversions.nowSqlFormat() + "'" 
						+ ", " + m_sActive
						+ ", '" + clsDatabaseFunctions.FormatSQLStatement(m_sAccountSetCode) + "'"
						+ ", '" + clsDatabaseFunctions.FormatSQLStatement(m_sAdjustmentWriteOffAccount) + "'"
						+ ", '" + clsDatabaseFunctions.FormatSQLStatement(m_sDescription) + "'"
						+ ", '" + clsDatabaseFunctions.FormatSQLStatement(m_sInventoryAccount) + "'"
						+ ", '" + clsDatabaseFunctions.FormatSQLStatement(sUserFullName) + "'"
						+ ", " + clsDatabaseFunctions.FormatSQLStatement(sUserID) + ""
						+ ", '" + clsDatabaseFunctions.FormatSQLStatement(m_sNonStockClearingAccount) + "'"
						+ ", '" + clsDatabaseFunctions.FormatSQLStatement(m_sPayablesClearingAccount) + "'"
						+ ", '" + clsDatabaseFunctions.FormatSQLStatement(m_sTransferClearingAccount) + "'"
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
				
				//If we get to here, we've succeeded:
				m_sNewRecord = "0";
				return true;				
			}
		}catch(SQLException e){
			System.out.println("Error saving  - " + e.getMessage());
			m_sErrorMessageArray.add("Error saving  - " + e.getMessage());
			return false;
		}
	}

	private boolean validateNewCode(){
		m_sErrorMessageArray.clear();
		//All upper case:
		m_sAccountSetCode = m_sAccountSetCode.toUpperCase();
		
		if(!clsStringFunctions.validateStringCharacters(m_sAccountSetCode, "ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890-")){
			m_sErrorMessageArray.add("Invalid characters in account set code");
			return false;
		}
		return true; 
		
	}
	private boolean validateEntries(){
		
		boolean bEntriesAreValid = true;
		m_sErrorMessageArray.clear();
    	if (m_sAccountSetCode.trim().compareToIgnoreCase("") == 0){
    		m_sErrorMessageArray.add("account set code cannot be blank");
    		bEntriesAreValid = false;
    	}
    	if (m_sAccountSetCode.length() > SMTableicaccountsets.sAccountSetCodeLength){
    		m_sErrorMessageArray.add("account set code cannot be longer than " 
    			+ SMTableicaccountsets.sAccountSetCodeLength + " characters.");
    		bEntriesAreValid = false;
    	}
    	if (m_sLastEditUser.length() > SMTableicaccountsets.sLastEditUserLength){
    		m_sErrorMessageArray.add("Last Edit User cannot be longer than " 
    			+ SMTableicaccountsets.sLastEditUserLength + " characters.");
    		bEntriesAreValid = false;
    	}
    	if (m_sDescription.trim().compareToIgnoreCase("") == 0){
    		m_sErrorMessageArray.add("description cannot be blank");
    		bEntriesAreValid = false;
    	}
    	if (m_sDescription.length() > SMTableicaccountsets.sDescriptionLength){
    		m_sErrorMessageArray.add("description cannot be longer than " 
    			+ SMTableicaccountsets.sDescriptionLength + " characters.");
    		bEntriesAreValid = false;
    	}
    	if (m_sInventoryAccount.trim().compareToIgnoreCase("") == 0){
    		m_sErrorMessageArray.add("inventory account cannot be blank");
    		bEntriesAreValid = false;
    	}
    	if (m_sInventoryAccount.length() > SMTableicaccountsets.sInventoryAccountLength){
    		m_sErrorMessageArray.add("inventory account cannot be longer than " 
    			+ SMTableicaccountsets.sInventoryAccountLength + " characters.");
    		bEntriesAreValid = false;
    	}
    	if (m_sPayablesClearingAccount.trim().compareToIgnoreCase("") == 0){
    		m_sErrorMessageArray.add("payables clearing account cannot be blank");
    		bEntriesAreValid = false;
    	}
    	if (m_sPayablesClearingAccount.length() > SMTableicaccountsets.sPayablesClearingAccountLength){
    		m_sErrorMessageArray.add("payables clearing cannot be longer than " 
    			+ SMTableicaccountsets.sPayablesClearingAccountLength + " characters.");
    		bEntriesAreValid = false;
    	}
    	if (m_sAdjustmentWriteOffAccount.trim().compareToIgnoreCase("") == 0){
    		m_sErrorMessageArray.add("Adjustment write-off account cannot be blank");
    		bEntriesAreValid = false;
    	}
    	if (m_sAdjustmentWriteOffAccount.length() > SMTableicaccountsets.sAdjustmentWriteOffAccountLength){
    		m_sErrorMessageArray.add("Adjustment write-off account cannot be longer than " 
    			+ SMTableicaccountsets.sAdjustmentWriteOffAccountLength + " characters.");
    		bEntriesAreValid = false;
    	}
    	if (m_sNonStockClearingAccount.trim().compareToIgnoreCase("") == 0){
    		m_sErrorMessageArray.add("Non stock clearing account cannot be blank");
    		bEntriesAreValid = false;
    	}
    	if (m_sNonStockClearingAccount.length() > SMTableicaccountsets.sNonStockClearingAccountLength){
    		m_sErrorMessageArray.add("Non stock clearing account cannot be longer than " 
    			+ SMTableicaccountsets.sNonStockClearingAccountLength + " characters.");
    		bEntriesAreValid = false;
    	}
    	if (m_sTransferClearingAccount.trim().compareToIgnoreCase("") == 0){
    		m_sErrorMessageArray.add("Transfer clearing account cannot be blank");
    		bEntriesAreValid = false;
    	}
    	if (m_sTransferClearingAccount.length() > SMTableicaccountsets.sTransferClearingAccountLength){
    		m_sErrorMessageArray.add("Transfer clearing account cannot be longer than " 
    			+ SMTableicaccountsets.sTransferClearingAccountLength + " characters.");
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
		sQueryString += ICAccountSet.ParamAddingNewRecord + "=" + clsServletUtilities.URLEncode(m_sNewRecord);
		sQueryString += "&" + ICAccountSet.ParamAccountSetCode + "=" + clsServletUtilities.URLEncode(m_sAccountSetCode);
		if (m_sActive.compareToIgnoreCase("1") == 0){
			sQueryString += "&" + ICAccountSet.ParamActive + "=" + m_sActive;
		}
		sQueryString += "&" + ICAccountSet.ParamAdjustmentWriteOffAccount + "=" + clsServletUtilities.URLEncode(m_sAdjustmentWriteOffAccount);
		sQueryString += "&" + ICAccountSet.ParamDescription + "=" + clsServletUtilities.URLEncode(m_sDescription);
		sQueryString += "&" + ICAccountSet.ParamInactiveDate + "=" + clsServletUtilities.URLEncode(m_sInactiveDate);
		sQueryString += "&" + ICAccountSet.ParamInventoryAccount + "=" + clsServletUtilities.URLEncode(m_sInventoryAccount);
		sQueryString += "&" + ICAccountSet.ParamLastEditUser + "=" + clsServletUtilities.URLEncode(m_sLastEditUser);
		sQueryString += "&" + ICAccountSet.ParamLastMaintainedDate + "=" + clsServletUtilities.URLEncode(m_sLastMaintainedDate);
		sQueryString += "&" + ICAccountSet.ParamNonStockClearingAccount + "=" + clsServletUtilities.URLEncode(m_sNonStockClearingAccount);
		sQueryString += "&" + ICAccountSet.ParamPayablesClearingAccount + "=" + clsServletUtilities.URLEncode(m_sPayablesClearingAccount);
		sQueryString += "&" + ICAccountSet.ParamTransferClearingAccount + "=" + clsServletUtilities.URLEncode(m_sTransferClearingAccount);
				
		return sQueryString;
	}
	//Requires connection since it is used as part of a data transaction in places:
	public boolean delete(String sAccountSet, Connection conn){
		
		m_sErrorMessageArray.clear();
		
		//First, check that the account set exists:
		String SQL = "SELECT * FROM " + SMTableicaccountsets.TableName
			+ " WHERE ("
				+ SMTableicaccountsets.sAccountSetCode + " = '" + m_sAccountSetCode + "'"
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
			System.out.println("Error checking account set " + sAccountSet + " to delete - " + e.getMessage());
			m_sErrorMessageArray.add("Error checking account set " + sAccountSet + " to delete - " + e.getMessage());
			return false;
		}
		
		/*
		//Check tables that account set depends on:
		//No items with this account set:
		SQL = "SELECT " 
			+ SMTableicitems.sItemNumber
			+ " FROM " + SMTableicitems.TableName
			+ " WHERE ("
				+ "(" + SMTableicitems.sAccountSet + " = '" + m_sAccountSetCode + "')"
			+ ")"
			;
		try{
			ResultSet rs = SMUtilities.openResultSet(SQL, conn);
			if(rs.next()){
				m_sErrorMessageArray.add("Account set " + sAccountSet + " is used on some items.");
				rs.close();
				return false;
			}else{
				rs.close();
			}
		}catch(SQLException e){
			System.out.println("Error checking items for " + sAccountSet + " - " + e.getMessage());
			m_sErrorMessageArray.add("Error checking items for " + sAccountSet + " - " + e.getMessage());
			return false;
		}
		*/
		//Now begin a data transaction that we can roll back:
		if(!clsDatabaseFunctions.start_data_transaction(conn)){
			m_sErrorMessageArray.add("Error beginning data transaction to delete  " + sAccountSet + "");
			return false;
		}
		
		try{
			SQL = "DELETE FROM " + SMTableicaccountsets.TableName
				+ " WHERE ("
					+ SMTableicaccountsets.sAccountSetCode + " = '" + m_sAccountSetCode + "'"
				+ ")"
			;
			if(!clsDatabaseFunctions.executeSQL(SQL, conn)){
				m_sErrorMessageArray.add("Error deleting account set " + sAccountSet);
				clsDatabaseFunctions.rollback_data_transaction(conn);
				return false;
			}

			//TODO - delete any related table records:
			/*
			SQL = ARSQLs.Delete_CustomerStatistics_SQL(sNumber);
			if(!SMUtilities.executeSQL(SQL, conn)){
				m_sErrorMessageArray.add("Error deleting customer statistics for " + sNumber);
				SMUtilities.rollback_data_transaction(conn);
				return false;
			}
			*/

		}catch(SQLException e){
			System.out.println("Error deleting account set " + sAccountSet + " - " + e.getMessage());
			m_sErrorMessageArray.add("Error deleting account set " + sAccountSet + " - " + e.getMessage());
			return false;
		}
		
		if(!clsDatabaseFunctions.commit_data_transaction(conn)){
			clsDatabaseFunctions.rollback_data_transaction(conn);
			return false;
		}
		
		return true;
	}
    public String getAccountSetCode(){
    	return m_sAccountSetCode;
    }
    public void setAccountSetCode(String sAccountSetCode){
    	m_sAccountSetCode = sAccountSetCode.trim();
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
    public String getInventoryAccount(){
    	return m_sInventoryAccount;
    }
	public void setInventoryAccount(String sInventoryAccount) {
		m_sInventoryAccount = sInventoryAccount.trim();
	}
    public String getPayablesClearingAccount(){
    	return m_sPayablesClearingAccount;
    }
	public void setPayablesClearingAccount(String sPayablesClearingAccount) {
		m_sPayablesClearingAccount = sPayablesClearingAccount.trim();
	}
    public String getAdjustmentWriteOffAccount(){
    	return m_sAdjustmentWriteOffAccount;
    }
	public void setAdjustmentWriteOffAccount(String sAdjustmentWriteOffAccount) {
		m_sAdjustmentWriteOffAccount = sAdjustmentWriteOffAccount.trim();
	}
    public String getNonStockClearingAccount(){
    	return m_sNonStockClearingAccount;
    }
	public void setNonStockClearingAccount(String sNonStockClearingAccount) {
		m_sNonStockClearingAccount = sNonStockClearingAccount.trim();
	}
    public String getTransferClearingAccount(){
    	return m_sTransferClearingAccount;
    }
	public void setTransferClearingAccount(String sTransferClearingAccount) {
		m_sTransferClearingAccount = sTransferClearingAccount.trim();
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
