package smar;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import java.sql.Connection;
import java.util.ArrayList;

import SMDataDefinition.*;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsManageRequestParameters;
import ServletUtilities.clsServletUtilities;
import ServletUtilities.clsStringFunctions;

public class ARCustomerShipTo extends Object{
	public static final String ParamsAddingNewRecord = "bAddingNewRecord";
	
	public static final String ParamsCustomerNumber = "sCustomerNumber";
	public static final String ParamsShipToCode = "sShipToCode";
	public static final String ParamsDescription = "sDescription";
	public static final String ParamsAddressLine1 = "sAddressLine1";
	public static final String ParamsAddressLine2 = "sAddressLine2";
	public static final String ParamsAddressLine3 = "sAddressLine3";
	public static final String ParamsAddressLine4 = "sAddressLine4";
	public static final String ParamsCity = "sCity";
	public static final String ParamsState = "sState";
	public static final String ParamsCountry = "sCountry";
	public static final String ParamsPostalCode = "sPostalCode";
	public static final String ParamsTaxGroup = "sTaxGroup";
	public static final String ParamsContactName = "sContactName";
	public static final String ParamsPhoneNumber = "sPhoneNumber";
	public static final String ParamsFaxNumber = "sFaxNumber";
	
	public  String m_sCustomerNumber;
	public  String m_sShipToCode;
	public  String m_sDescription;
	public  String m_sAddressLine1;
	public  String m_sAddressLine2;
	public  String m_sAddressLine3;
	public  String m_sAddressLine4;
	public  String m_sCity;
	public  String m_sState;
	public  String m_sCountry;
	public  String m_sPostalCode;
	public  String m_sContactName;
	public  String m_sPhoneNumber;
	public  String m_sFaxNumber;
	private String m_iNewRecord;
	private ArrayList<String> m_sErrorMessageArray = new ArrayList<String> (0);

    ARCustomerShipTo(
    		String sCustomerNumber,
    		String sShipToCode
        ) {
    	m_sCustomerNumber = sCustomerNumber;
		m_sShipToCode = sShipToCode;
		m_sDescription = "";
		m_sAddressLine1 = "";
		m_sAddressLine2 = "";
		m_sAddressLine3 = "";
		m_sAddressLine4 = "";
		m_sCity = "";
		m_sState = "";
		m_sCountry = "";
		m_sPostalCode = "";
		m_sContactName = "";
		m_sPhoneNumber = "";
		m_sFaxNumber = "";
    	m_iNewRecord = "1";
    	m_sErrorMessageArray = new ArrayList<String> (0);
        }
    
    public void loadFromHTTPRequest(HttpServletRequest req){
    	m_iNewRecord = clsManageRequestParameters.get_Request_Parameter(ARCustomerShipTo.ParamsAddingNewRecord, req).trim().replace("&quot;", "\"");
		m_sCustomerNumber = clsManageRequestParameters.get_Request_Parameter(ARCustomerShipTo.ParamsCustomerNumber, req).trim().replace("&quot;", "\"");
		m_sShipToCode = clsManageRequestParameters.get_Request_Parameter(ARCustomerShipTo.ParamsShipToCode, req).trim().replace("&quot;", "\"");
		m_sDescription = clsManageRequestParameters.get_Request_Parameter(ARCustomerShipTo.ParamsDescription, req).trim().replace("&quot;", "\"");
		m_sAddressLine1 = clsManageRequestParameters.get_Request_Parameter(ARCustomerShipTo.ParamsAddressLine1, req).trim().replace("&quot;", "\"");
		m_sAddressLine2 = clsManageRequestParameters.get_Request_Parameter(ARCustomerShipTo.ParamsAddressLine2, req).trim().replace("&quot;", "\"");
		m_sAddressLine3 = clsManageRequestParameters.get_Request_Parameter(ARCustomerShipTo.ParamsAddressLine3, req).trim().replace("&quot;", "\"");
		m_sAddressLine4 = clsManageRequestParameters.get_Request_Parameter(ARCustomerShipTo.ParamsAddressLine4, req).trim().replace("&quot;", "\"");
		m_sCity = clsManageRequestParameters.get_Request_Parameter(ARCustomerShipTo.ParamsCity, req).trim().replace("&quot;", "\"");
		m_sState = clsManageRequestParameters.get_Request_Parameter(ARCustomerShipTo.ParamsState, req).trim().replace("&quot;", "\"");
		m_sCountry = clsManageRequestParameters.get_Request_Parameter(ARCustomerShipTo.ParamsCountry, req).trim().replace("&quot;", "\"");
		m_sPostalCode = clsManageRequestParameters.get_Request_Parameter(ARCustomerShipTo.ParamsPostalCode, req).trim().replace("&quot;", "\"");
		m_sContactName = clsManageRequestParameters.get_Request_Parameter(ARCustomerShipTo.ParamsContactName, req).trim().replace("&quot;", "\"");
		m_sPhoneNumber = clsManageRequestParameters.get_Request_Parameter(ARCustomerShipTo.ParamsPhoneNumber, req).trim().replace("&quot;", "\"");
		m_sFaxNumber = clsManageRequestParameters.get_Request_Parameter(ARCustomerShipTo.ParamsFaxNumber, req).trim().replace("&quot;", "\"");
    }
	private boolean load(
			String sCustomerNumber,
			String sShipToCode,
    		ServletContext context, 
    		String sDBID
			){
		m_sErrorMessageArray.clear();
		try{
			//Get the record to edit:
			String sSQL = "SELECT * FROM " + SMTablearcustomershiptos.TableName + 
					" WHERE (" + 
					"(" + SMTablearcustomershiptos.sCustomerNumber + " = '" + sCustomerNumber + "')" +
					" AND (" + SMTablearcustomershiptos.sShipToCode + " = '" + sShipToCode + "')" + 
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
	        	m_sCustomerNumber = clsStringFunctions.checkStringForNull(rs.getString(SMTablearcustomershiptos.sCustomerNumber));
	        	m_sShipToCode = clsStringFunctions.checkStringForNull(rs.getString(SMTablearcustomershiptos.sShipToCode));
	        	m_sDescription = clsStringFunctions.checkStringForNull(rs.getString(SMTablearcustomershiptos.sDescription));
				m_sAddressLine1 = clsStringFunctions.checkStringForNull(rs.getString(SMTablearcustomershiptos.sAddressLine1));
				m_sAddressLine2 = clsStringFunctions.checkStringForNull(rs.getString(SMTablearcustomershiptos.sAddressLine2));
				m_sAddressLine3 = clsStringFunctions.checkStringForNull(rs.getString(SMTablearcustomershiptos.sAddressLine3));
				m_sAddressLine4 = clsStringFunctions.checkStringForNull(rs.getString(SMTablearcustomershiptos.sAddressLine4));
				m_sCity = clsStringFunctions.checkStringForNull(rs.getString(SMTablearcustomershiptos.sCity));
				m_sState = clsStringFunctions.checkStringForNull(rs.getString(SMTablearcustomershiptos.sState));
				m_sCountry = clsStringFunctions.checkStringForNull(rs.getString(SMTablearcustomershiptos.sCountry));
				m_sPostalCode = clsStringFunctions.checkStringForNull(rs.getString(SMTablearcustomershiptos.sPostalCode));
				m_sContactName = clsStringFunctions.checkStringForNull(rs.getString(SMTablearcustomershiptos.sContactName));
				m_sPhoneNumber = clsStringFunctions.checkStringForNull(rs.getString(SMTablearcustomershiptos.sPhoneNumber));
				m_sFaxNumber = clsStringFunctions.checkStringForNull(rs.getString(SMTablearcustomershiptos.sFaxNumber));
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

		return load(m_sCustomerNumber, m_sShipToCode, context, sDBID);
	}

	public boolean save (ServletContext context, String sDBID){
		m_sErrorMessageArray.clear();
		//Check to see if the record already exists:
		String SQL = "SELECT * FROM " + SMTablearcustomershiptos.TableName + 
				" WHERE (" + 
				"(" + SMTablearcustomershiptos.sCustomerNumber + " = '" + m_sCustomerNumber + "')" +
				" AND (" + SMTablearcustomershiptos.sShipToCode + " = '" + m_sShipToCode + "')" + 
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
					m_sErrorMessageArray.add("Cannot save - ship to already exists.");
					rs.close();
					return false;
				}
				rs.close();
				
				m_sErrorMessageArray.clear();
				if(!validateEntries()){
					return false;
				}
				
				//Update the record:
				SQL = "UPDATE " + SMTablearcustomershiptos.TableName
						+ " SET " 
						+ SMTablearcustomershiptos.sAddressLine1 + " = '" + clsDatabaseFunctions.FormatSQLStatement(m_sAddressLine1) + "', "
						+ SMTablearcustomershiptos.sAddressLine2 + " = '" + clsDatabaseFunctions.FormatSQLStatement(m_sAddressLine2) + "', "
						+ SMTablearcustomershiptos.sAddressLine3 + " = '" + clsDatabaseFunctions.FormatSQLStatement(m_sAddressLine3) + "', "
						+ SMTablearcustomershiptos.sAddressLine4 + " = '" + clsDatabaseFunctions.FormatSQLStatement(m_sAddressLine4) + "', "
						+ SMTablearcustomershiptos.sCity + " = '" + clsDatabaseFunctions.FormatSQLStatement(m_sCity) + "', "
						+ SMTablearcustomershiptos.sContactName + " = '" + clsDatabaseFunctions.FormatSQLStatement(m_sContactName) + "', "
						+ SMTablearcustomershiptos.sCountry + " = '" + clsDatabaseFunctions.FormatSQLStatement(m_sCountry) + "', "
						+ SMTablearcustomershiptos.sDescription + " = '" + clsDatabaseFunctions.FormatSQLStatement(m_sDescription) + "', "
						+ SMTablearcustomershiptos.sFaxNumber + " = '" + clsDatabaseFunctions.FormatSQLStatement(m_sFaxNumber) + "', "
						+ SMTablearcustomershiptos.sPhoneNumber + " = '" + clsDatabaseFunctions.FormatSQLStatement(m_sPhoneNumber) + "', "
						+ SMTablearcustomershiptos.sPostalCode + " = '" + clsDatabaseFunctions.FormatSQLStatement(m_sPostalCode) + "', "
						+ SMTablearcustomershiptos.sState + " = '" + clsDatabaseFunctions.FormatSQLStatement(m_sState) + "' "
						
						+ " WHERE (" 
							+ "(" + SMTablearcustomershiptos.sCustomerNumber + " = '" + clsDatabaseFunctions.FormatSQLStatement(m_sCustomerNumber) + "')"
							+ " AND (" + SMTablearcustomershiptos.sShipToCode + " = '" + clsDatabaseFunctions.FormatSQLStatement(m_sShipToCode) + "')"
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
					m_sErrorMessageArray.add("Cannot save - can't get existing customer ship to.");
					rs.close();
					return false;
				}
				rs.close();
				//Insert the record:
				//First, validate the new code:
				if (!validateNewCode()){
					return false;
				}
				SQL =  "INSERT into " + SMTablearcustomershiptos.TableName
						+ " ("
						+ SMTablearcustomershiptos.sCustomerNumber
						+ ", " + SMTablearcustomershiptos.sShipToCode 
						+ ", " + SMTablearcustomershiptos.sDescription
						+ ", " + SMTablearcustomershiptos.sAddressLine1
						+ ", " + SMTablearcustomershiptos.sAddressLine2
						+ ", " + SMTablearcustomershiptos.sAddressLine3
						+ ", " + SMTablearcustomershiptos.sAddressLine4
						+ ", " + SMTablearcustomershiptos.sCity
						+ ", " + SMTablearcustomershiptos.sState
						+ ", " + SMTablearcustomershiptos.sCountry
						+ ", " + SMTablearcustomershiptos.sPostalCode
						+ ", " + SMTablearcustomershiptos.sContactName
						+ ", " + SMTablearcustomershiptos.sPhoneNumber
						+ ", " + SMTablearcustomershiptos.sFaxNumber
					+ ")"
					+ " VALUES ("
						+ "'" + clsDatabaseFunctions.FormatSQLStatement(m_sCustomerNumber) + "'" 
						+ ", '" + clsDatabaseFunctions.FormatSQLStatement(m_sShipToCode) + "'"
						+ ", '" + clsDatabaseFunctions.FormatSQLStatement(m_sDescription) + "'"
						+ ", '" + clsDatabaseFunctions.FormatSQLStatement(m_sAddressLine1) + "'"
						+ ", '" + clsDatabaseFunctions.FormatSQLStatement(m_sAddressLine2) + "'"
						+ ", '" + clsDatabaseFunctions.FormatSQLStatement(m_sAddressLine3) + "'"
						+ ", '" + clsDatabaseFunctions.FormatSQLStatement(m_sAddressLine4) + "'"
						+ ", '" + clsDatabaseFunctions.FormatSQLStatement(m_sCity) + "'"
						+ ", '" + clsDatabaseFunctions.FormatSQLStatement(m_sState) + "'"
						+ ", '" + clsDatabaseFunctions.FormatSQLStatement(m_sCountry) + "'"
						+ ", '" + clsDatabaseFunctions.FormatSQLStatement(m_sPostalCode) + "'"
						+ ", '" + clsDatabaseFunctions.FormatSQLStatement(m_sContactName) + "'"
						+ ", '" + clsDatabaseFunctions.FormatSQLStatement(m_sPhoneNumber) + "'"
						+ ", '" + clsDatabaseFunctions.FormatSQLStatement(m_sFaxNumber) + "'"
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
			System.out.println("Error updating customer ship to - " + e.getMessage());
			m_sErrorMessageArray.add("Error updating ship to - " + e.getMessage());
			return false;
		}
	}
	private boolean validateNewCode(){
		m_sErrorMessageArray.clear();
		//All upper case:
		m_sShipToCode = m_sShipToCode.toUpperCase();
		
		if(!clsStringFunctions.validateStringCharacters(m_sShipToCode, "ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890-")){
			m_sErrorMessageArray.add("Invalid characters in ship-to code");
			return false;
		}
		return true; 
		
	}
	private boolean validateEntries(){
		
		boolean bEntriesAreValid = true;
		m_sErrorMessageArray.clear();
    	if (m_sCustomerNumber.trim().compareToIgnoreCase("") == 0){
    		m_sErrorMessageArray.add("customer number cannot be blank");
    		bEntriesAreValid = false;
    	}
    	if (m_sCustomerNumber.length() > SMTablearcustomershiptos.sCustomerNumberLength){
    		m_sErrorMessageArray.add("customer number cannot be longer than " 
    			+ SMTablearcustomershiptos.sCustomerNumberLength + " characters.");
    		bEntriesAreValid = false;
    	}
    	if (m_sShipToCode.length() > SMTablearcustomershiptos.sShipToCodeLength){
    		m_sErrorMessageArray.add("customer name cannot be longer than " 
    			+ SMTablearcustomershiptos.sShipToCodeLength + " characters.");
    		bEntriesAreValid = false;
    	}
	    if (m_sDescription.length() > SMTablearcustomershiptos.sDescriptionLength){
    		m_sErrorMessageArray.add("description cannot be longer than " 
    			+ SMTablearcustomershiptos.sDescriptionLength + " characters.");
    		bEntriesAreValid = false;
    	}
    	if (m_sAddressLine1.length() > SMTablearcustomershiptos.sAddressLine1Length){
    		m_sErrorMessageArray.add("address line 1 cannot be longer than " 
    			+ SMTablearcustomershiptos.sAddressLine1Length + " characters.");
    		bEntriesAreValid = false;
    	}
    	if (m_sAddressLine2.length() > SMTablearcustomershiptos.sAddressLine2Length){
    		m_sErrorMessageArray.add("address line 2 cannot be longer than " 
    			+ SMTablearcustomershiptos.sAddressLine2Length + " characters.");
    		bEntriesAreValid = false;
    	}
    	if (m_sAddressLine3.length() > SMTablearcustomershiptos.sAddressLine3Length){
    		m_sErrorMessageArray.add("address line 3 cannot be longer than " 
    			+ SMTablearcustomershiptos.sAddressLine3Length + " characters.");
    		bEntriesAreValid = false;
    	}
    	if (m_sAddressLine4.length() > SMTablearcustomershiptos.sAddressLine4Length){
    		m_sErrorMessageArray.add("address line 4 cannot be longer than " 
    			+ SMTablearcustomershiptos.sAddressLine4Length + " characters.");
    		bEntriesAreValid = false;
    	}
    	if (m_sCity.length() > SMTablearcustomershiptos.sCityLength){
    		m_sErrorMessageArray.add("City cannot be longer than " 
    			+ SMTablearcustomershiptos.sCityLength + " characters.");
    		bEntriesAreValid = false;
    	}
    	if (m_sState.length() > SMTablearcustomershiptos.sStateLength){
    		m_sErrorMessageArray.add("State cannot be longer than " 
    			+ SMTablearcustomershiptos.sStateLength + " characters.");
    		bEntriesAreValid = false;
    	}
    	if (m_sCountry.length() > SMTablearcustomershiptos.sCountryLength){
    		m_sErrorMessageArray.add("Country cannot be longer than " 
    			+ SMTablearcustomershiptos.sCountryLength + " characters.");
    		bEntriesAreValid = false;
    	}
    	if (m_sPostalCode.length() > SMTablearcustomershiptos.sPostalCodeLength){
    		m_sErrorMessageArray.add("Postal Code cannot be longer than " 
    			+ SMTablearcustomershiptos.sPostalCodeLength + " characters.");
    		bEntriesAreValid = false;
    	}
    	if (m_sContactName.length() > SMTablearcustomershiptos.sContactNameLength){
    		m_sErrorMessageArray.add("Contact Name cannot be longer than " 
    			+ SMTablearcustomershiptos.sContactNameLength + " characters.");
    		bEntriesAreValid = false;
    	}
    	if (m_sPhoneNumber.length() > SMTablearcustomershiptos.sPhoneNumberLength){
    		m_sErrorMessageArray.add("Phone Number cannot be longer than " 
    			+ SMTablearcustomershiptos.sPhoneNumberLength + " characters.");
    		bEntriesAreValid = false;
    	}
    	if (m_sFaxNumber.length() > SMTablearcustomershiptos.sFaxNumberLength){
    		m_sErrorMessageArray.add("Fax Number cannot be longer than " 
    			+ SMTablearcustomershiptos.sFaxNumberLength + " characters.");
    		bEntriesAreValid = false;
    	}
    	
    	return bEntriesAreValid;
	}
	public String getQueryString(){
		
		String sQueryString = "";
		sQueryString += ParamsAddingNewRecord + "=" + clsServletUtilities.URLEncode(m_iNewRecord);
		sQueryString += "&" + ParamsCustomerNumber + "=" + clsServletUtilities.URLEncode(m_sCustomerNumber);
		sQueryString += "&" + ParamsShipToCode + "=" + clsServletUtilities.URLEncode(m_sShipToCode);
		sQueryString += "&" + ParamsDescription + "=" + clsServletUtilities.URLEncode(m_sDescription);
		sQueryString += "&" + ParamsAddressLine1 + "=" + clsServletUtilities.URLEncode(m_sAddressLine1);
		sQueryString += "&" + ParamsAddressLine2 + "=" + clsServletUtilities.URLEncode(m_sAddressLine2);
		sQueryString += "&" + ParamsAddressLine3 + "=" + clsServletUtilities.URLEncode(m_sAddressLine3);
		sQueryString += "&" + ParamsAddressLine4 + "=" + clsServletUtilities.URLEncode(m_sAddressLine4);
		sQueryString += "&" + ParamsCity + "=" + clsServletUtilities.URLEncode(m_sCity);
		sQueryString += "&" + ParamsState + "=" + clsServletUtilities.URLEncode(m_sState);
		sQueryString += "&" + ParamsCountry + "=" + clsServletUtilities.URLEncode(m_sCountry);
		sQueryString += "&" + ParamsPostalCode + "=" + clsServletUtilities.URLEncode(m_sPostalCode);
		sQueryString += "&" + ParamsContactName + "=" + clsServletUtilities.URLEncode(m_sContactName);
		sQueryString += "&" + ParamsPhoneNumber + "=" + clsServletUtilities.URLEncode(m_sPhoneNumber);
		sQueryString += "&" + ParamsFaxNumber + "=" + clsServletUtilities.URLEncode(m_sFaxNumber);
				
		return sQueryString;
	}
	//Need a connection here for the data transaction:
	public boolean delete(String sCustomerCode, String sShipToCode, Connection conn){
		
		m_sErrorMessageArray.clear();
		
		//First, check that the customer exists:
		String SQL ="SELECT * FROM " + SMTablearcustomershiptos.TableName + 
				" WHERE (" + 
				"(" + SMTablearcustomershiptos.sCustomerNumber + " = '" + sCustomerCode + "')" +
				" AND (" + SMTablearcustomershiptos.sShipToCode + " = '" + sShipToCode + "')" + 
			")";
		
		try{
			ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
			if(!rs.next()){
				m_sErrorMessageArray.add("Customer ship-to " + sCustomerCode + " - " + sShipToCode + " cannot be found.");
				rs.close();
				return false;
			}else{
				rs.close();
			}
		}catch(SQLException e){
			System.out.println("Error checking customer ship-to to delete - " + e.getMessage());
			m_sErrorMessageArray.add("Error checking customer ship-to to delete - " + e.getMessage());
			return false;
		}
		
		//Now begin a data transaction that we can roll back:
		if(!clsDatabaseFunctions.start_data_transaction(conn)){
			m_sErrorMessageArray.add("Error beginning data transaction");
			return false;
		}
		
		try{
			SQL = "DELETE FROM " +
					SMTablearcustomershiptos.TableName +
					" WHERE (" + 
						"(" + SMTablearcustomershiptos.sCustomerNumber + " = '" + sCustomerCode + "')" +
						" AND (" + SMTablearcustomershiptos.sShipToCode + " = '" + sShipToCode + "')" + 
					")";
			
			if(!clsDatabaseFunctions.executeSQL(SQL, conn)){
				m_sErrorMessageArray.add("Error deleting customer ship-to");
				clsDatabaseFunctions.rollback_data_transaction(conn);
				return false;
			}

			SQL =  "DELETE FROM " +
					SMTablesitelocations.TableName +
					" WHERE (" + 
						"(" + SMTablesitelocations.sAcct + " = '" + sCustomerCode + "')" +
						" AND (" + SMTablesitelocations.sShipToCode + " = '" + sShipToCode + "')" + 
					")";
			
			if(!clsDatabaseFunctions.executeSQL(SQL, conn)){
				m_sErrorMessageArray.add("Error deleting customer ship-to site locations");
				clsDatabaseFunctions.rollback_data_transaction(conn);
				return false;
			}
			
		}catch(SQLException e){
			System.out.println("Error deleting customer ship-to - " + e.getMessage());
			m_sErrorMessageArray.add("Error deleting customer ship-to - " + e.getMessage());
			return false;
		}
		
		if(!clsDatabaseFunctions.commit_data_transaction(conn)){
			clsDatabaseFunctions.rollback_data_transaction(conn);
			return false;
		}
		
		return true;
	}
    public String getM_sCustomerNumber(){
    	return m_sCustomerNumber;
    }
    public void setM_sCustomerNumber(String sCustomerNumber){
    	m_sCustomerNumber = sCustomerNumber.trim();
    }
	public String getM_sShipToCode() {
		return m_sShipToCode;
	}

	public void setM_sShipToCode(String shipToCode) {
		m_sShipToCode = shipToCode.trim();
	}

	public String getM_sDescription() {
		return m_sDescription;
	}

	public void setM_sDescription(String description) {
		m_sDescription = description.trim();
	}

	public String getM_sAddressLine1() {
		return m_sAddressLine1;
	}

	public void setM_sAddressLine1(String addressLine1) {
		m_sAddressLine1 = addressLine1.trim();
	}

	public String getM_sAddressLine2() {
		return m_sAddressLine2;
	}

	public void setM_sAddressLine2(String addressLine2) {
		m_sAddressLine2 = addressLine2.trim();
	}

	public String getM_sAddressLine3() {
		return m_sAddressLine3;
	}

	public void setM_sAddressLine3(String addressLine3) {
		m_sAddressLine3 = addressLine3.trim();
	}

	public String getM_sAddressLine4() {
		return m_sAddressLine4;
	}

	public void setM_sAddressLine4(String addressLine4) {
		m_sAddressLine4 = addressLine4.trim();
	}

	public String getM_sCity() {
		return m_sCity;
	}

	public void setM_sCity(String city) {
		m_sCity = city.trim();
	}

	public String getM_sState() {
		return m_sState;
	}

	public void setM_sState(String state) {
		m_sState = state.trim();
	}

	public String getM_sCountry() {
		return m_sCountry;
	}

	public void setM_sCountry(String country) {
		m_sCountry = country.trim();
	}

	public String getM_sPostalCode() {
		return m_sPostalCode;
	}

	public void setM_sPostalCode(String postalCode) {
		m_sPostalCode = postalCode.trim();
	}

	public String getM_sContactName() {
		return m_sContactName;
	}

	public void setM_sContactName(String contactName) {
		m_sContactName = contactName.trim();
	}

	public String getM_sPhoneNumber() {
		return m_sPhoneNumber;
	}

	public void setM_sPhoneNumber(String phoneNumber) {
		m_sPhoneNumber = phoneNumber.trim();
	}

	public String getM_sFaxNumber() {
		return m_sFaxNumber;
	}

	public void setM_sFaxNumber(String faxNumber) {
		m_sFaxNumber = faxNumber.trim();
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
