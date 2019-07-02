package smar;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import SMClasses.SMBatchStatuses;
import SMClasses.SMEntryBatch;
import SMClasses.SMModuleTypes;
import SMDataDefinition.SMTablearcustomer;
import SMDataDefinition.SMTablearterms;
import SMDataDefinition.SMTableartransactions;
import SMDataDefinition.SMTableentries;
import SMDataDefinition.SMTableinvoiceheaders;
import SMDataDefinition.SMTableorderdetails;
import SMDataDefinition.SMTableorderheaders;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsDateAndTimeConversions;
import ServletUtilities.clsManageBigDecimals;
import ServletUtilities.clsStringFunctions;

public class ARTerms extends java.lang.Object{
	public static final String ParamsAddingNewRecord = "bAddingNewRecord";
	public static final String ParamsTermsCode = "sTermsCode";
	public static final String ParamsDescription = "sDescription";
	public static final String ParamiActive = "iActive";
	public static final String ParamdatLastMaintained = "datLastMaintained";
	public static final String ParamdDiscountPercent = "dDiscountPercent"; 
	public static final String ParamiDiscountNumberOfDays = "iDiscountNumberOfDays"; 
	public static final String ParamiDiscountDayOfTheMonth = "iDiscountDayOfTheMonth"; 
	public static final String ParamiDueNumberOfDays = "iDueNumberOfDays"; 
	public static final String ParamiDueDayOfTheMonth = "iDueDayOfTheMonth"; 
	
	public String m_sTermsCode = "sTermsCode";
	public String m_sDescription = "sDescription";
	public String m_iActive = "iActive";
	public String m_datLastMaintained = "datLastMaintained";
	public String m_dDiscountPercent = "dDiscountPercent"; 
	public String m_iDiscountNumberOfDays = "iDiscountNumberOfDays"; 
	public String m_iDiscountDayOfTheMonth = "iDiscountDayOfTheMonth"; 
	public String m_iDueNumberOfDays = "iDueNumberOfDays"; 
	public String m_iDueDayOfTheMonth = "iDueDayOfTheMonth"; 
	private String m_iNewRecord;
	private ArrayList<String> m_sErrorMessageArray = new ArrayList<String> (0);

	public ARTerms(
    		String sTermsCode
        ) {
		m_sTermsCode = sTermsCode;
    	m_sDescription = "";
    	m_iActive = "0";
    	m_datLastMaintained = clsDateAndTimeConversions.now("MM/dd/yyyy");
    	m_dDiscountPercent = "0.00"; 
    	m_iDiscountNumberOfDays = "0"; 
    	m_iDiscountDayOfTheMonth = "0"; 
    	m_iDueNumberOfDays = "0"; 
    	m_iDueDayOfTheMonth = "0"; 
    	m_iNewRecord = "1";
    	m_sErrorMessageArray = new ArrayList<String> (0);

        }
    
    public void loadFromHTTPRequest(HttpServletRequest req){
    	m_iNewRecord = ARUtilities.get_Request_Parameter(ParamsAddingNewRecord, req).trim().replace("&quot;", "\"");
    	m_sTermsCode = ARUtilities.get_Request_Parameter(ParamsTermsCode, req).trim().replace("&quot;", "\"");
    	m_sDescription = ARUtilities.get_Request_Parameter(ParamsDescription, req).trim().replace("&quot;", "\"");
		if(req.getParameter(ParamiActive) == null){
			m_iActive = "0";
		}else{
			if(req.getParameter(ParamiActive).compareToIgnoreCase("0") ==0){
				m_iActive = "0";
			}else{
				m_iActive = "1";
			}
		}
		m_datLastMaintained = ARUtilities.get_Request_Parameter(ParamdatLastMaintained, req).trim().replace("&quot;", "\"");
		if(m_datLastMaintained.compareToIgnoreCase("") == 0){
			m_datLastMaintained = clsDateAndTimeConversions.now("MM/dd/yyyy");
		}
    	m_iDueDayOfTheMonth = "0"; 
    	m_dDiscountPercent = ARUtilities.get_Request_Parameter(ParamdDiscountPercent, req).trim().replace("&quot;", "\"");
    	m_iDiscountNumberOfDays = ARUtilities.get_Request_Parameter(ParamiDiscountNumberOfDays, req).trim().replace("&quot;", "\"");
    	m_iDiscountDayOfTheMonth = ARUtilities.get_Request_Parameter(ParamiDiscountDayOfTheMonth, req).trim().replace("&quot;", "\"");
    	m_iDueNumberOfDays = ARUtilities.get_Request_Parameter(ParamiDueNumberOfDays, req).trim().replace("&quot;", "\"");
    	m_iDueDayOfTheMonth = ARUtilities.get_Request_Parameter(ParamiDueDayOfTheMonth, req).trim().replace("&quot;", "\"");
    }
	private boolean load(
			String sTermsCode,
    		ServletContext context, 
    		String sDBID
			){
		m_sErrorMessageArray.clear();
		try{
			//Get the record to edit:
			String sSQL =  "SELECT * FROM " + SMTablearterms.TableName + 
					" WHERE (" + 
					"(" + SMTablearterms.sTermsCode + " = '" + sTermsCode + "')" +
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
	
	public void load (
		String sTermsCode,
		Connection conn
	) throws Exception{
		//Get the record to edit:
		String sSQL =  "SELECT * FROM " + SMTablearterms.TableName + 
				" WHERE (" + 
				"(" + SMTablearterms.sTermsCode + " = '" + sTermsCode + "')" +
			")";
        ResultSet rs = clsDatabaseFunctions.openResultSet(sSQL, conn); 
        try {
			if (loadFromResultSet(rs)){
				rs.close();
				return;
			}else{
				rs.close();
				throw new Exception(m_sErrorMessageArray.get(m_sErrorMessageArray.size() - 1));
			}
		} catch (Exception e) {
			throw new Exception("Error [1523041269] loading AR terms code - " + e.getMessage());
		}
	}

	private boolean loadFromResultSet(ResultSet rs){
		try{
	        if (rs.next()){
	        	m_sTermsCode = ARUtilities.checkStringForNull(rs.getString(SMTablearterms.sTermsCode));
	        	m_sDescription = ARUtilities.checkStringForNull(rs.getString(SMTablearterms.sDescription));
	        	m_iActive = rs.getString(SMTablearterms.iActive);
	        	if(clsDateAndTimeConversions.IsValidDate(rs.getDate(SMTablearterms.datLastMaintained))){
	        		m_datLastMaintained = clsDateAndTimeConversions.utilDateToString(rs.getDate(SMTablearterms.datLastMaintained),"MM/dd/yyyy");
	        	}else{
	        		m_datLastMaintained = clsDateAndTimeConversions.now("MM/dd/yyyy");
	        	}
	        	m_dDiscountPercent = clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(rs.getBigDecimal(SMTablearterms.dDiscountPercent));
	        	m_iDiscountNumberOfDays = Integer.toString(rs.getInt(SMTablearterms.iDiscountNumberOfDays));
	        	m_iDiscountDayOfTheMonth = Integer.toString(rs.getInt(SMTablearterms.iDiscountDayOfTheMonth));
	        	m_iDueNumberOfDays = Integer.toString(rs.getInt(SMTablearterms.iDueNumberOfDays));
	        	m_iDueDayOfTheMonth = Integer.toString(rs.getInt(SMTablearterms.iDueDayOfTheMonth));
	        	m_iNewRecord = "0";
	        	rs.close();
	        	return true;
	        }
	        else{
	        	rs.close();
	        	return false;
	        }
		}catch(SQLException ex){
			m_sErrorMessageArray.add("Error [1523041268] loading AR Terms - " + ex.getMessage());
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

		return load(m_sTermsCode, context, sDBID);
	}

	public boolean save (ServletContext context, String sDBID){
		m_sErrorMessageArray.clear();
		//Check to see if the record already exists:
		String SQL =  "SELECT * FROM " + SMTablearterms.TableName + 
				" WHERE (" + 
				"(" + SMTablearterms.sTermsCode + " = '" + m_sTermsCode + "')" +
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
					m_sErrorMessageArray.add("Cannot save - terms already exists.");
					rs.close();
					return false;
				}
				rs.close();
				
				m_sErrorMessageArray.clear();
				if(!validateEntries()){
					return false;
				}
				
				//Update the record:
				SQL =  "UPDATE " + SMTablearterms.TableName
						+ " SET " 
						+ SMTablearterms.datLastMaintained + " = NOW(), "
						+ SMTablearterms.iActive + " = " + m_iActive + ", "
						+ SMTablearterms.sDescription + " = '" + clsDatabaseFunctions.FormatSQLStatement(m_sDescription) + "', "
						+ SMTablearterms.dDiscountPercent + " = " + m_dDiscountPercent.replace(",", "") + ", "
						+ SMTablearterms.iDiscountDayOfTheMonth + " = " + m_iDiscountDayOfTheMonth + ", "
						+ SMTablearterms.iDiscountNumberOfDays + " = " + m_iDiscountNumberOfDays + ", "
						+ SMTablearterms.iDueDayOfTheMonth + " = " + m_iDueDayOfTheMonth + ", "
						+ SMTablearterms.iDueNumberOfDays + " = " + m_iDueNumberOfDays
						+ " WHERE (" 
							+ "(" + SMTablearterms.sTermsCode + " = '" + clsDatabaseFunctions.FormatSQLStatement(m_sTermsCode) + "')"
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
					m_sErrorMessageArray.add("Cannot save - can't get existing terms.");
					rs.close();
					return false;
				}
				rs.close();
				//Insert the record:
				//First, validate the new code:
				if (!validateNewCode()){
					return false;
				}
				SQL =  "INSERT INTO " + SMTablearterms.TableName
						+ " (" 
						+ SMTablearterms.sTermsCode
						+ ", " + SMTablearterms.iActive
						+ ", " + SMTablearterms.dDiscountPercent
						+ ", " + SMTablearterms.sDescription
						+ ", " + SMTablearterms.iDiscountDayOfTheMonth
						+ ", " + SMTablearterms.iDiscountNumberOfDays
						+ ", " + SMTablearterms.iDueDayOfTheMonth
						+ ", " + SMTablearterms.iDueNumberOfDays
						+ ", " + SMTablearterms.datLastMaintained
						
						+ ") VALUES (" 
							+ "'" + clsDatabaseFunctions.FormatSQLStatement(m_sTermsCode) + "'"
							+ ", " + m_iActive
							+ ", " + m_dDiscountPercent.replace(",", "")
							+ ", '" + clsDatabaseFunctions.FormatSQLStatement(m_sDescription) + "'"
							+ ", " + m_iDiscountDayOfTheMonth
							+ ", " + m_iDiscountNumberOfDays
							+ ", " + m_iDueDayOfTheMonth
							+ ", " + m_iDueNumberOfDays
							+ ", NOW()"
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
			System.out.println("Error saving terms - " + e.getMessage());
			m_sErrorMessageArray.add("Error saving terms - " + e.getMessage());
			return false;
		}
	}
	private boolean validateNewCode(){
		m_sErrorMessageArray.clear();
		//All upper case:
		m_sTermsCode = m_sTermsCode.toUpperCase();
		
		if(!clsStringFunctions.validateStringCharacters(m_sTermsCode, "ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890-")){
			m_sErrorMessageArray.add("Invalid characters in terms code");
			return false;
		}
		return true; 
		
	}
	private boolean validateEntries(){
		
		boolean bEntriesAreValid = true;
		m_sErrorMessageArray.clear();
    	if (m_sTermsCode.trim().compareToIgnoreCase("") == 0){
    		m_sErrorMessageArray.add("terms code cannot be blank");
    		bEntriesAreValid = false;
    	}
    	if (m_sTermsCode.length() > SMTablearterms.sTermsCodeLength){
    		m_sErrorMessageArray.add("terms code cannot be longer than " 
    			+ SMTablearterms.sTermsCodeLength + " characters.");
    		bEntriesAreValid = false;
    	}
    	
    	if (m_sDescription.length() > SMTablearterms.sDescriptionLength){
    		m_sErrorMessageArray.add("description cannot be longer than " 
    			+ SMTablearterms.sDescriptionLength + " characters.");
    		bEntriesAreValid = false;
    	}
    	if(!clsDateAndTimeConversions.IsValidDateString("MM/dd/yyyy", m_datLastMaintained)){
    		m_sErrorMessageArray.add("Invalid last maintained date: " + m_datLastMaintained); 
        		bEntriesAreValid = false;
    	}
    	if(!ARUtilities.IsValidBigDecimal(m_dDiscountPercent, 2)){
    		m_sErrorMessageArray.add("Invalid discount percent: " + m_dDiscountPercent); 
    		bEntriesAreValid = false;
    	}else{
	    	//Convert the format to a standard format:
	   		BigDecimal bd = new BigDecimal(m_dDiscountPercent.replace(",", ""));
	   		bd = bd.setScale(2, BigDecimal.ROUND_HALF_UP);
	   		m_dDiscountPercent = clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(bd);
    	}
    	if(!ARUtilities.IsValidInteger(m_iDiscountNumberOfDays)){
    		m_sErrorMessageArray.add("Invalid discount number of days: " + m_iDiscountNumberOfDays); 
    		bEntriesAreValid = false;
    	}else{
    		m_iDiscountNumberOfDays = m_iDiscountNumberOfDays.replace(",", "");
    	}
    	if(!ARUtilities.IsValidInteger(m_iDiscountDayOfTheMonth)){
    		m_sErrorMessageArray.add("Invalid discount day of the month: " + m_iDiscountDayOfTheMonth); 
    		bEntriesAreValid = false;
    	}else{
    		m_iDiscountDayOfTheMonth = m_iDiscountDayOfTheMonth.replace(",", "");
    	}
    	if(!ARUtilities.IsValidInteger(m_iDueNumberOfDays)){
    		m_sErrorMessageArray.add("Invalid due number of days: " + m_iDueNumberOfDays); 
    		bEntriesAreValid = false;
    	}else{
    		m_iDueNumberOfDays = m_iDueNumberOfDays.replace(",", "");
    	}
    	if(!ARUtilities.IsValidInteger(m_iDueDayOfTheMonth)){
    		m_sErrorMessageArray.add("Invalid due day of the month: " + m_iDueDayOfTheMonth); 
    		bEntriesAreValid = false;
    	}else{
    		m_iDueDayOfTheMonth = m_iDueDayOfTheMonth.replace(",", "");
    		if(Integer.parseInt(m_iDueDayOfTheMonth) > 31){
        		m_sErrorMessageArray.add("Invalid due day of the month: " + m_iDueDayOfTheMonth); 
        		bEntriesAreValid = false;
    		}
    	}
    	return bEntriesAreValid;
	}
	public String getQueryString(){
		
		String sQueryString = "";
		sQueryString += ParamsAddingNewRecord + "=" + ARUtilities.URLEncode(m_iNewRecord);
		sQueryString += "&" + ParamsTermsCode + "=" + ARUtilities.URLEncode(m_sTermsCode);
		sQueryString += "&" + ParamsDescription + "=" + ARUtilities.URLEncode(m_sDescription);
		sQueryString += "&" + ParamiActive + "=" + ARUtilities.URLEncode(m_iActive);
		sQueryString += "&" + ParamdatLastMaintained + "=" + ARUtilities.URLEncode(m_datLastMaintained);
		sQueryString += "&" + ParamdDiscountPercent + "=" + ARUtilities.URLEncode(m_dDiscountPercent);
		sQueryString += "&" + ParamiDiscountNumberOfDays + "=" + ARUtilities.URLEncode(m_iDiscountNumberOfDays);
		sQueryString += "&" + ParamiDiscountDayOfTheMonth + "=" + ARUtilities.URLEncode(m_iDiscountDayOfTheMonth);
		sQueryString += "&" + ParamiDueNumberOfDays + "=" + ARUtilities.URLEncode(m_iDueNumberOfDays);
		sQueryString += "&" + ParamiDueDayOfTheMonth + "=" + ARUtilities.URLEncode(m_iDueDayOfTheMonth);
				
		return sQueryString;
	}
	public boolean delete(String sTermsCode, ServletContext context, String sDBID){
		
		m_sErrorMessageArray.clear();
		
		//First, check that the terms exist:
		String SQL =  "SELECT * FROM " + SMTablearterms.TableName + 
				" WHERE (" + 
				"(" + SMTablearterms.sTermsCode + " = '" + sTermsCode + "')" +
			")";
		
		try{
			ResultSet rs = clsDatabaseFunctions.openResultSet(
				SQL, 
				context,
				sDBID,
				"MySQL",
				this.toString() + ".delete (1)");
			if(!rs.next()){
				m_sErrorMessageArray.add("Terms " + sTermsCode + " cannot be found.");
				rs.close();
				return false;
			}else{
				rs.close();
			}
		}catch(SQLException e){
			System.out.println("Error checking terms to delete - " + e.getMessage());
			m_sErrorMessageArray.add("Error checking terms to delete - " + e.getMessage());
			return false;
		}
		
		//Customers
		SQL = "SELECT * FROM " + SMTablearcustomer.TableName + 
				" WHERE (" + 
				"(" + SMTablearcustomer.sTerms + " = '" + sTermsCode + "')" +
			")";
		try{
			ResultSet rs = clsDatabaseFunctions.openResultSet(
				SQL, 
				context,
				sDBID,
				"MySQL",
				this.toString() + ".delete (2)");
			if(rs.next()){
				m_sErrorMessageArray.add("Terms code " + sTermsCode + " is used on some customers.");
				rs.close();
				return false;
			}else{
				rs.close();
			}
		}catch(SQLException e){
			System.out.println("Error checking terms on customers - " + e.getMessage());
			m_sErrorMessageArray.add("Error checking terms on customers - " + e.getMessage());
			return false;
		}
		
		//Open Order headers
		SQL =  "SELECT " 
				+ SMTableorderheaders.sOrderNumber
				+ " FROM " + SMTableorderheaders.TableName + ", " + SMTableorderdetails.TableName
				+ " WHERE ("
					+ "(" + SMTableorderdetails.TableName + ".dUniqueOrderID = " 
						+ SMTableorderheaders.TableName + "." + SMTableorderheaders.dOrderUniqueifier + ")"
					
					+ " AND (" + SMTableorderheaders.sTerms + " = '" + sTermsCode + "')"
					
					+ " AND (" + SMTableorderdetails.TableName + ".dQtyOrdered != 0.00)"
					
					+ " AND ("
						+ "(" + SMTableorderheaders.datOrderCanceledDate + " IS NULL)"
						+ " OR (" + SMTableorderheaders.datOrderCanceledDate + " < '1900-01-01')"
					+ ")"
					
				+ ")"
				;
		try{
			ResultSet rs = clsDatabaseFunctions.openResultSet(
					SQL, 
					context,
					sDBID,
					"MySQL",
					this.toString() + ".delete (3)");
			if(rs.next()){
				m_sErrorMessageArray.add("Terms code " + sTermsCode + " is used on some open orders.");
				rs.close();
				return false;
			}else{
				rs.close();
			}
		}catch(SQLException e){
			System.out.println("Error checking terms on open orders - " + e.getMessage());
			m_sErrorMessageArray.add("Error checking terms on open orders - " + e.getMessage());
			return false;
		}
		//Unexported Invoices
		SQL = "SELECT " 
				+ SMTableinvoiceheaders.sInvoiceNumber
				+ " FROM " + SMTableinvoiceheaders.TableName
				+ " WHERE ("
					+ "(" + SMTableinvoiceheaders.sTerms + " = '" + sTermsCode + "')"
					+ " AND (" + SMTableinvoiceheaders.iExportedToAR + " != 1)"
					
				+ ")"
				;
		
		try{
			ResultSet rs = clsDatabaseFunctions.openResultSet(
					SQL, 
					context,
					sDBID,
					"MySQL",
					this.toString() + ".delete (4)");
			if(rs.next()){
				m_sErrorMessageArray.add("Terms code " + sTermsCode + " is used on some invoices that have not been exported.");
				rs.close();
				return false;
			}else{
				rs.close();
			}
		}catch(SQLException e){
			System.out.println("Error checking terms on unexported invoices - " + e.getMessage());
			m_sErrorMessageArray.add("Error checking terms on unexported invoices - " + e.getMessage());
			return false;
		}
		
		//transactionentries
		SQL =  "SELECT " + SMTableentries.lid 
				+ " FROM " + SMTableentries.TableName + ", " + SMEntryBatch.TableName
				+ " WHERE ("
					+ "(" + SMTableentries.stermscode + " = '" + sTermsCode + "')"
					+ " AND (" + SMTableentries.TableName + "." + SMTableentries.ibatchnumber + " = " 
						+ SMEntryBatch.TableName + "." + SMEntryBatch.ibatchnumber + ")"
					+ " AND (" + SMEntryBatch.smoduletype + " = '" + SMModuleTypes.AR + "')"
					+ " AND (" + SMEntryBatch.ibatchstatus + " != " + SMBatchStatuses.DELETED + ")"
					+ " AND (" + SMEntryBatch.ibatchstatus + " != " + SMBatchStatuses.POSTED + ")"
				+ ")"
				;
		try{
			ResultSet rs = clsDatabaseFunctions.openResultSet(
					SQL, 
					context,
					sDBID,
					"MySQL",
					this.toString() + ".delete (5)");
			if(rs.next()){
				m_sErrorMessageArray.add("Terms code " + sTermsCode + " is used on some unposted batch entries.");
				rs.close();
				return false;
			}else{
				rs.close();
			}
		}catch(SQLException e){
			System.out.println("Error checking terms code - " + e.getMessage());
			m_sErrorMessageArray.add("Error checking terms code - " + e.getMessage());
			return false;
		}
		
		//aropentransactions
		SQL =  "SELECT " 
				+ SMTableartransactions.lid
				+ " FROM " + SMTableartransactions.TableName
				+ " WHERE ("
					+ "(" + SMTableartransactions.sterms + " = '" + sTermsCode + "')"
					+ " AND (" + SMTableartransactions.dcurrentamt + " != 0.00)"
				+ ")"
				;
		
		try{
			ResultSet rs = clsDatabaseFunctions.openResultSet(
					SQL, 
					context,
					sDBID,
					"MySQL",
					this.toString() + ".delete (6)");
			if(rs.next()){
				m_sErrorMessageArray.add("Customer " + sTermsCode + " is used on some open transactions.");
				rs.close();
				return false;
			}else{
				rs.close();
			}
		}catch(SQLException e){
			System.out.println("Error checking terms code on open transactions - " + e.getMessage());
			m_sErrorMessageArray.add("Error checking terms code on open transactions - " + e.getMessage());
			return false;
		}
		
		try{
			SQL = "DELETE FROM " +
					SMTablearterms.TableName+
					" WHERE (" + 
						"(" + SMTablearterms.sTermsCode + " = '" + sTermsCode + "')" +
					")";
			if(!clsDatabaseFunctions.executeSQL(SQL, context, sDBID)){
				m_sErrorMessageArray.add("Error deleting terms");
				return false;
			}

		}catch(SQLException e){
			System.out.println("Error deleting terms - " + e.getMessage());
			m_sErrorMessageArray.add("Error deleting terms - " + e.getMessage());
			return false;
		}
		
		return true;
	}
	public String getM_sTermsCode() {
		return m_sTermsCode;
	}

	public void setM_sTermsCode(String termsCode) {
		m_sTermsCode = termsCode;
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
	public String getM_dDiscountPercent() {
		return m_dDiscountPercent;
	}

	public void setM_dDiscountPercent(String discountPercent) {
		m_dDiscountPercent = discountPercent;
	}

	public String getM_iDiscountNumberOfDays() {
		return m_iDiscountNumberOfDays;
	}

	public void setM_iDiscountNumberOfDays(String discountNumberOfDays) {
		m_iDiscountNumberOfDays = discountNumberOfDays;
	}

	public String getM_iDiscountDayOfTheMonth() {
		return m_iDiscountDayOfTheMonth;
	}

	public void setM_iDiscountDayOfTheMonth(String discountDayOfTheMonth) {
		m_iDiscountDayOfTheMonth = discountDayOfTheMonth;
	}

	public String getM_iDueNumberOfDays() {
		return m_iDueNumberOfDays;
	}

	public void setM_iDueNumberOfDays(String dueNumberOfDays) {
		m_iDueNumberOfDays = dueNumberOfDays;
	}

	public String getM_iDueDayOfTheMonth() {
		return m_iDueDayOfTheMonth;
	}

	public void setM_iDueDayOfTheMonth(String dueDayOfTheMonth) {
		m_iDueDayOfTheMonth = dueDayOfTheMonth;
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