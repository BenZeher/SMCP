package SMClasses;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import SMDataDefinition.SMTablearcustomer;
import SMDataDefinition.SMTableicitemprices;
import SMDataDefinition.SMTablepricelistcodes;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsManageRequestParameters;
import ServletUtilities.clsServletUtilities;
import ServletUtilities.clsStringFunctions;

public class SMPriceListCode extends java.lang.Object{

	public static final String ParamsAddingNewRecord = "bAddingNewRecord";
	public static final String ParamsPriceListCode = "sPriceListCode";
	public static final String ParamsDescription = "sDescription";
	
	private String m_sPriceListCode;
	private String m_sDescription;
	private String m_iNewRecord;
	private ArrayList<String> m_sErrorMessageArray = new ArrayList<String> (0);
	public SMPriceListCode(
    		String sPriceListCode
        ) {
    		m_sPriceListCode = sPriceListCode;
        	m_sDescription = "";
        	m_iNewRecord = "1";
        	m_sErrorMessageArray = new ArrayList<String> (0);
        }
    public void loadFromHTTPRequest(HttpServletRequest req){
    	m_iNewRecord = clsManageRequestParameters.get_Request_Parameter(ParamsAddingNewRecord, req).trim().replace("&quot;", "\"");
    	m_sPriceListCode = clsManageRequestParameters.get_Request_Parameter(ParamsPriceListCode, req).trim().replace("&quot;", "\"");
    	m_sDescription = clsManageRequestParameters.get_Request_Parameter(ParamsDescription, req).trim().replace("&quot;", "\"");
    }
	private boolean load(
			String sCode,
    		ServletContext context, 
    		String sDBID
			){
		m_sErrorMessageArray.clear();
		try{
			//Get the record to edit:
			String sSQL =  "SELECT * FROM " + SMTablepricelistcodes.TableName + 
					" WHERE (" + 
					"(" + SMTablepricelistcodes.spricelistcode + " = '" + sCode + "')" +
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
	    	System.out.println("[1579186362] Error in load function!!");
	        System.out.println("SQLException: " + ex.getMessage());
	        System.out.println("SQLState: " + ex.getSQLState());
	        System.out.println("SQL: " + ex.getErrorCode());
			return false;
		}
	}

	private boolean loadFromResultSet(ResultSet rs){
		try{
	        if (rs.next()){
	        	m_sPriceListCode = clsStringFunctions.checkStringForNull(rs.getString(SMTablepricelistcodes.spricelistcode));
            	m_sDescription = clsStringFunctions.checkStringForNull(rs.getString(SMTablepricelistcodes.sdescription));
	        	m_iNewRecord = "0";
	        	rs.close();
	        	return true;
	        }
	        else{
	        	rs.close();
	        	return false;
	        }
		}catch(SQLException ex){
	    	System.out.println("[1579186366] Error in loadFromResultSet function!!");
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

		return load(m_sPriceListCode, context, sDBID);
	}

	public boolean save (ServletContext context, String sDBID){
		m_sErrorMessageArray.clear();
		//Check to see if the record already exists:
		String SQL =   "SELECT * FROM " + SMTablepricelistcodes.TableName + 
				" WHERE (" + 
				"(" + SMTablepricelistcodes.spricelistcode + " = '" + m_sPriceListCode + "')" +
			")";
	;
		try{
			ResultSet rs = clsDatabaseFunctions.openResultSet(
				SQL,
				context,
				sDBID,
				"MySQL",
				this.toString() + ".save"
			);
			if(rs.next()){
				//If it's supposed to be a new record, then return an error:
				if(m_iNewRecord.compareToIgnoreCase("1") == 0){
					m_sErrorMessageArray.add("Cannot save - price list code already exists.");
					rs.close();
					return false;
				}
				rs.close();
				
				m_sErrorMessageArray.clear();
				if(!validateEntries()){
					return false;
				}
				
				//Update the record:
				SQL = "UPDATE " + SMTablepricelistcodes.TableName
						+ " SET " 
						+ SMTablepricelistcodes.sdescription + " = '" + clsDatabaseFunctions.FormatSQLStatement(m_sDescription) + "'"
						
						+ " WHERE (" 
							+ "(" + SMTablepricelistcodes.spricelistcode + " = '" + clsDatabaseFunctions.FormatSQLStatement(m_sPriceListCode) + "')"
							+ ")"; 
				
				if(!clsDatabaseFunctions.executeSQL(
						SQL, 
						context, 
						sDBID,
						"MySQL",
						this.toString() + ".save (update)")){
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
					m_sErrorMessageArray.add("Cannot save - can't get existing price list code.");
					rs.close();
					return false;
				}
				rs.close();
				//Insert the record:
				//First, validate the new code:
				if (!validateNewCode()){
					return false;
				}
				SQL =  "INSERT into " + SMTablepricelistcodes.TableName
						+ " (" 
						+ SMTablepricelistcodes.spricelistcode
						+ ", " + SMTablepricelistcodes.sdescription
					+ ")"
					+ " VALUES ("
						+ "'" + clsDatabaseFunctions.FormatSQLStatement(m_sPriceListCode) + "'"
						+ ", '" + clsDatabaseFunctions.FormatSQLStatement(m_sDescription) + "'"
					+ ")"
					;

				if(!clsDatabaseFunctions.executeSQL(
						SQL, 
						context, 
						sDBID,
						"MySQL",
						this.toString() + ".save (insert)")){
					m_sErrorMessageArray.add("Cannot execute INSERT sql.");
					return false;
				}else{
					m_iNewRecord = "0";
					return true;
				}
			}
		}catch(SQLException e){
			System.out.println("[1579186372] Error saving price list code - " + e.getMessage());
			m_sErrorMessageArray.add("Error saving price list code - " + e.getMessage());
			return false;
		}
	}
	private boolean validateNewCode(){
		m_sErrorMessageArray.clear();
		//All upper case:
		m_sPriceListCode = m_sPriceListCode.toUpperCase();
		
		if(!clsStringFunctions.validateStringCharacters(m_sPriceListCode, "ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890-")){
			m_sErrorMessageArray.add("Invalid characters in price list code");
			return false;
		}
		return true; 
	}
	private boolean validateEntries(){
		
		boolean bEntriesAreValid = true;
		m_sErrorMessageArray.clear();
    	if (m_sPriceListCode.trim().compareToIgnoreCase("") == 0){
    		m_sErrorMessageArray.add("price list code cannot be blank");
    		bEntriesAreValid = false;
    	}
    	if (m_sPriceListCode.length() > SMTablepricelistcodes.spricelistcodeLength){
    		m_sErrorMessageArray.add("price list code cannot be longer than " 
    			+ SMTablepricelistcodes.spricelistcodeLength + " characters.");
    		bEntriesAreValid = false;
    	}
    	
    	if (m_sDescription.length() > SMTablepricelistcodes.sdescriptionLength){
    		m_sErrorMessageArray.add("description cannot be longer than " 
    			+ SMTablepricelistcodes.sdescriptionLength + " characters.");
    		bEntriesAreValid = false;
    	}
    	
    	return bEntriesAreValid;
	}
	public String getQueryString(){
		
		String sQueryString = "";
		sQueryString += ParamsAddingNewRecord + "=" + clsServletUtilities.URLEncode(m_iNewRecord);
		sQueryString += "&" + ParamsPriceListCode + "=" + clsServletUtilities.URLEncode(m_sPriceListCode);
		sQueryString += "&" + ParamsDescription + "=" + clsServletUtilities.URLEncode(m_sDescription);
		return sQueryString;
	}
	public void delete(String sPriceListCode, ServletContext context, String sDBID) throws Exception{
		
		m_sErrorMessageArray.clear();
		
		//First, check that the price list code exists:
		String SQL =  "SELECT * FROM " + SMTablepricelistcodes.TableName + 
				" WHERE (" + 
				"(" + SMTablepricelistcodes.spricelistcode + " = '" + sPriceListCode + "')" +
			")";
		
		try{
			ResultSet rs = clsDatabaseFunctions.openResultSet(
				SQL, 
				context,
				sDBID,
				"MySQL",
				this.toString() + ".delete (1)");
			if(!rs.next()){
				rs.close();
				throw new Exception("Price list code " + sPriceListCode + " cannot be found.");
			}else{
				rs.close();
			}
		}catch(SQLException e){
			throw new Exception("Error reading price list code - " + e.getMessage() + ".");
		}
		//Customers
		SQL =  "SELECT"
				+ " " + SMTablearcustomer.sCustomerNumber
				+ " FROM " + SMTablearcustomer.TableName 
				+ " WHERE (" 
					+ "(" + SMTablearcustomer.sPriceListCode + " = '" + sPriceListCode + "')"
				+ ")"
				;
		try{
			ResultSet rs = clsDatabaseFunctions.openResultSet(
					SQL, 
					context,
					sDBID,
					"MySQL",
					this.toString() + ".delete (2)");
			if(rs.next()){
				rs.close();
				throw new Exception("Price list code " + sPriceListCode + " is being used on some customers.");
			}else{
				rs.close();
			}
		}catch(SQLException e){
			throw new Exception("Error checking Price list code on customers - " + e.getMessage() + ".");
		}
				
		//Order headers:
		//As of 1/29/2013, we are no longer checking this:
		/*
		SQL = ARSQLs.Get_Open_Orders_For_PriceListCode_SQL(sPriceListCode);
		try{
			ResultSet rs = ARUtilities.openResultSet(
					SQL, 
					context,
					sDBID,
					"MySQL",
					this.toString() + ".delete (3)");
			if(rs.next()){
				rs.close();
				throw new Exception("Price list code " + sPriceListCode + " is used on some open orders.");
			}else{
				rs.close();
			}
		}catch(SQLException e){
			throw new Exception("Error checking price list codes on orders - " + e.getMessage() + ".");
		}
		*/
		SQL =  "DELETE FROM " +
				SMTablepricelistcodes.TableName +
				" WHERE (" + 
					"(" + SMTablepricelistcodes.spricelistcode + " = '" + sPriceListCode + "')" +
				")";
		Connection conn = clsDatabaseFunctions.getConnection(context, sDBID, "MySQL", this.toString());
		
		clsDatabaseFunctions.start_data_transaction(conn);
		Statement stmt = conn.createStatement();
		try {
			stmt.execute(SQL);
		} catch (Exception e) {
			clsDatabaseFunctions.rollback_data_transaction(conn);
			clsDatabaseFunctions.freeConnection(context, conn, "[1547067579]");
			throw new Exception("Error deleting price code - " + e.getMessage());
		}
		
		//Now, remove all the price records with this price list code:
		SQL = "DELETE FROM " + SMTableicitemprices.TableName
			+ " WHERE ("
				+ "(" + SMTableicitemprices.sPriceListCode + " = '" + sPriceListCode + "')"
			+ ")"
		;
		try {
			stmt.execute(SQL);
		} catch (Exception e) {
			clsDatabaseFunctions.rollback_data_transaction(conn);
			clsDatabaseFunctions.freeConnection(context, conn, "[1547067580]");
			throw new Exception("Error deleting item prices for price list code '" + sPriceListCode + "' - " + e.getMessage());
		}
		
		clsDatabaseFunctions.freeConnection(context, conn, "[1547067581]");
	}
	public String getM_sPriceListCode() {
		return m_sPriceListCode;
	}
	public void setM_sPriceListCode(String priceListCode) {
		m_sPriceListCode = priceListCode;
	}
	public String getM_sDescription() {
		return m_sDescription;
	}
	public void setM_sDescription(String description) {
		m_sDescription = description;
	}
	public String getM_iNewRecord() {
		return m_iNewRecord;
	}
	public void setM_iNewRecord(String newRecord) {
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