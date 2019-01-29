package smar;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import SMDataDefinition.SMTablearacctset;
import SMDataDefinition.SMTablearterms;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsDateAndTimeConversions;
import ServletUtilities.clsStringFunctions;

public class ARAccountSet extends java.lang.Object{

	public static final String ParamsAddingNewRecord = "bAddingNewRecord";
	public static final String ParamsAcctSetCode = "sAcctSetCode";
	public static final String ParamsDescription = "sDescription";
	public static final String ParamiActive = "iActive";
	public static final String ParamdatLastMaintained = "datLastMaintained";
	public static final String ParamdsAcctsReceivableControlAcct = "sAcctsReceivableControlAcct"; 
	public static final String ParamsReceiptDiscountsAcct = "sReceiptDiscountsAcct"; 
	public static final String ParamsPrepaymentLiabilityAcct = "sPrepaymentLiabilityAcct"; 
	public static final String ParamsWriteOffAcct = "sWriteOffAcct"; 
	public static final String ParamsRetainageAcct = "sRetainageAcct";
	public static final String ParamsCashAcct = "sCashAcct";
	
	private String m_sAcctSetCode;
	private String m_sDescription;
	private String m_iActive;
	private String m_datLastMaintained;
	private String m_sAcctsReceivableControlAcct; 
	private String m_sReceiptDiscountsAcct; 
	private String m_sPrepaymentLiabilityAcct; 
	private String m_sWriteOffAcct; 
	private String m_sRetainageAcct;
	private String m_sCashAcct;
	private String m_iNewRecord;
	private ArrayList<String> m_sErrorMessageArray = new ArrayList<String> (0);
    ARAccountSet(
    		String sAcctSetCode
        ) {
    		m_sAcctSetCode = sAcctSetCode;
        	m_sDescription = "";
        	m_iActive = "0";
        	m_datLastMaintained = clsDateAndTimeConversions.now("MM/dd/yyyy");
        	m_sAcctsReceivableControlAcct = "";
        	m_sReceiptDiscountsAcct = "";
        	m_sPrepaymentLiabilityAcct = "";
        	m_sWriteOffAcct = "";
        	m_sRetainageAcct = "";
        	m_sCashAcct = "";
        	m_iNewRecord = "1";
        	m_sErrorMessageArray = new ArrayList<String> (0);
        }
    public void loadFromHTTPRequest(HttpServletRequest req){
    	m_iNewRecord = ARUtilities.get_Request_Parameter(ParamsAddingNewRecord, req).trim().replace("&quot;", "\"");
    	m_sAcctSetCode = ARUtilities.get_Request_Parameter(ParamsAcctSetCode, req).trim().replace("&quot;", "\"");
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
    	m_sAcctsReceivableControlAcct = ARUtilities.get_Request_Parameter(ParamdsAcctsReceivableControlAcct, req).trim().replace("&quot;", "\"");
    	m_sReceiptDiscountsAcct =  ARUtilities.get_Request_Parameter(ParamsReceiptDiscountsAcct, req).trim().replace("&quot;", "\"");
    	m_sPrepaymentLiabilityAcct = ARUtilities.get_Request_Parameter(ParamsPrepaymentLiabilityAcct, req).trim().replace("&quot;", "\"");
    	m_sWriteOffAcct = ARUtilities.get_Request_Parameter(ParamsWriteOffAcct, req).trim().replace("&quot;", "\"");
    	m_sRetainageAcct = ARUtilities.get_Request_Parameter(ParamsRetainageAcct, req).trim().replace("&quot;", "\"");
    	m_sCashAcct = ARUtilities.get_Request_Parameter(ParamsCashAcct, req).trim().replace("&quot;", "\"");
		
    }
	private boolean load(
			String sAcctSet,
    		ServletContext context, 
    		String sDBID
			){
		m_sErrorMessageArray.clear();
		try{
			//Get the record to edit:
			String sSQL = ARSQLs.Get_AcctSet_By_Code(sAcctSet);
	        ResultSet rs = clsDatabaseFunctions.openResultSet(sSQL, context, sDBID, "MySQL",this.toString() + ".load");
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
	        	m_sAcctSetCode = ARUtilities.checkStringForNull(rs.getString(SMTablearacctset.sAcctSetCode));
            	m_sDescription = ARUtilities.checkStringForNull(rs.getString(SMTablearacctset.sDescription));
	        	m_iActive = rs.getString(SMTablearterms.iActive);
	        	if(clsDateAndTimeConversions.IsValidDate(rs.getDate(SMTablearterms.datLastMaintained))){
	        		m_datLastMaintained = clsDateAndTimeConversions.utilDateToString(rs.getDate(SMTablearterms.datLastMaintained),"MM/dd/yyyy");
	        	}else{
	        		m_datLastMaintained = clsDateAndTimeConversions.now("MM/dd/yyyy");
	        	}
            	m_sAcctsReceivableControlAcct = ARUtilities.checkStringForNull(rs.getString(SMTablearacctset.sAcctsReceivableControlAcct));
            	m_sReceiptDiscountsAcct = ARUtilities.checkStringForNull(rs.getString(SMTablearacctset.sReceiptDiscountsAcct));
            	m_sPrepaymentLiabilityAcct = ARUtilities.checkStringForNull(rs.getString(SMTablearacctset.sPrepaymentLiabilityAcct));
            	m_sWriteOffAcct = ARUtilities.checkStringForNull(rs.getString(SMTablearacctset.sWriteOffAcct));
            	m_sRetainageAcct = ARUtilities.checkStringForNull(rs.getString(SMTablearacctset.sRetainageAcct));
            	m_sCashAcct = ARUtilities.checkStringForNull(rs.getString(SMTablearacctset.sCashAcct));	
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

		return load(m_sAcctSetCode, context, sDBID);
	}

	public boolean save (ServletContext context, String sDBID){
		m_sErrorMessageArray.clear();
		//Check to see if the record already exists:
		String SQL = ARSQLs.Get_AcctSet_By_Code(m_sAcctSetCode);
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
					m_sErrorMessageArray.add("Cannot save - account set already exists.");
					rs.close();
					return false;
				}
				rs.close();
				
				m_sErrorMessageArray.clear();
				if(!validateEntries()){
					return false;
				}
				
				//Update the record:
				SQL = ARSQLs.Update_AcctSet_SQL(
						clsDatabaseFunctions.FormatSQLStatement(m_sAcctSetCode),
						m_iActive, 
						clsDatabaseFunctions.FormatSQLStatement(m_sAcctsReceivableControlAcct), 
						clsDatabaseFunctions.FormatSQLStatement(m_sDescription), 
						clsDatabaseFunctions.FormatSQLStatement(m_sPrepaymentLiabilityAcct), 
						clsDatabaseFunctions.FormatSQLStatement(m_sReceiptDiscountsAcct), 
						clsDatabaseFunctions.FormatSQLStatement(m_sRetainageAcct), 
						clsDatabaseFunctions.FormatSQLStatement(m_sWriteOffAcct), 
						clsDatabaseFunctions.FormatSQLStatement(m_sCashAcct)
						);
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
					m_sErrorMessageArray.add("Cannot save - can't get existing account set.");
					rs.close();
					return false;
				}
				rs.close();
				//Insert the record:
				//First, validate the new code:
				if (!validateNewCode()){
					return false;
				}
				SQL = ARSQLs.Insert_AcctSet_SQL(
						clsDatabaseFunctions.FormatSQLStatement(m_sAcctSetCode),
						m_iActive, 
						clsDatabaseFunctions.FormatSQLStatement(m_sAcctsReceivableControlAcct), 
						clsDatabaseFunctions.FormatSQLStatement(m_sDescription), 
						clsDatabaseFunctions.FormatSQLStatement(m_sPrepaymentLiabilityAcct), 
						clsDatabaseFunctions.FormatSQLStatement(m_sReceiptDiscountsAcct), 
						clsDatabaseFunctions.FormatSQLStatement(m_sRetainageAcct), 
						clsDatabaseFunctions.FormatSQLStatement(m_sWriteOffAcct), 
						clsDatabaseFunctions.FormatSQLStatement(m_sCashAcct)
					);

				if(!clsDatabaseFunctions.executeSQL(SQL, context, sDBID)){
					m_sErrorMessageArray.add("Cannot execute INSERT sql.");
					return false;
				}else{
					m_iNewRecord = "0";
					return true;
				}
			}
		}catch(SQLException e){
			System.out.println("Error saving account set - " + e.getMessage());
			m_sErrorMessageArray.add("Error account set - " + e.getMessage());
			return false;
		}
	}
	private boolean validateNewCode(){
		m_sErrorMessageArray.clear();
		//All upper case:
		m_sAcctSetCode = m_sAcctSetCode.toUpperCase();
		
		if(!clsStringFunctions.validateStringCharacters(m_sAcctSetCode, "ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890-")){
			m_sErrorMessageArray.add("Invalid characters in account set code");
			return false;
		}
		return true; 
	}
	private boolean validateEntries(){
		
		boolean bEntriesAreValid = true;
		m_sErrorMessageArray.clear();
    	if (m_sAcctSetCode.trim().compareToIgnoreCase("") == 0){
    		m_sErrorMessageArray.add("account set code cannot be blank");
    		bEntriesAreValid = false;
    	}
    	if (m_sAcctSetCode.length() > SMTablearacctset.sAcctSetCodeLength){
    		m_sErrorMessageArray.add("account set code cannot be longer than " 
    			+ SMTablearacctset.sAcctSetCodeLength + " characters.");
    		bEntriesAreValid = false;
    	}
    	
    	if (m_sDescription.length() > SMTablearacctset.sDescriptionLength){
    		m_sErrorMessageArray.add("description cannot be longer than " 
    			+ SMTablearacctset.sDescriptionLength + " characters.");
    		bEntriesAreValid = false;
    	}
    	if(!clsDateAndTimeConversions.IsValidDateString("MM/dd/yyyy", m_datLastMaintained)){
    		m_sErrorMessageArray.add("Invalid last maintained date: " + m_datLastMaintained); 
        		bEntriesAreValid = false;
    	}
    	if (m_sAcctsReceivableControlAcct.length() > SMTablearacctset.sAcctsReceivableControlAcctLength){
    		m_sErrorMessageArray.add("Accts receivable account cannot be longer than " 
    			+ SMTablearacctset.sDescriptionLength + " characters.");
    		bEntriesAreValid = false;
    	}
    	if (m_sAcctsReceivableControlAcct.compareToIgnoreCase("") == 0){
    		m_sErrorMessageArray.add("Accts receivable account cannot be blank");
    		bEntriesAreValid = false;
    	}
    	
    	if (m_sReceiptDiscountsAcct.length() > SMTablearacctset.sReceiptDiscountsAcctLength){
    		m_sErrorMessageArray.add("Receipt Discounts Acct cannot be longer than " 
    			+ SMTablearacctset.sReceiptDiscountsAcctLength + " characters.");
    		bEntriesAreValid = false;
    	}
    	if (m_sReceiptDiscountsAcct.compareToIgnoreCase("") == 0){
    		m_sErrorMessageArray.add("Receipt discounts account cannot be blank");
    		bEntriesAreValid = false;
    	}

    	if (m_sPrepaymentLiabilityAcct.length() > SMTablearacctset.sPrepaymentLiabilityAcctLength){
    		m_sErrorMessageArray.add("Prepayment Liability Acct cannot be longer than " 
    			+ SMTablearacctset.sPrepaymentLiabilityAcctLength + " characters.");
    		bEntriesAreValid = false;
    	}
    	if (m_sPrepaymentLiabilityAcct.compareToIgnoreCase("") == 0){
    		m_sErrorMessageArray.add("Prepayment Liability account cannot be blank");
    		bEntriesAreValid = false;
    	}
    	if (m_sWriteOffAcct.length() > SMTablearacctset.sWriteOffAcctLength){
    		m_sErrorMessageArray.add("Write Off Acct cannot be longer than " 
    			+ SMTablearacctset.sWriteOffAcctLength + " characters.");
    		bEntriesAreValid = false;
    	}
    	if (m_sWriteOffAcct.compareToIgnoreCase("") == 0){
    		m_sErrorMessageArray.add("Write Off account cannot be blank");
    		bEntriesAreValid = false;
    	}
    	if (m_sRetainageAcct.length() > SMTablearacctset.sReceiptDiscountsAcctLength){
    		m_sErrorMessageArray.add("Receipt Discounts Acct cannot be longer than " 
    			+ SMTablearacctset.sReceiptDiscountsAcctLength + " characters.");
    		bEntriesAreValid = false;
    	}
    	if (m_sRetainageAcct.compareToIgnoreCase("") == 0){
    		m_sErrorMessageArray.add("Retainage account cannot be blank");
    		bEntriesAreValid = false;
    	}
    	if (m_sCashAcct.length() > SMTablearacctset.sCashAcctLength){
    		m_sErrorMessageArray.add("Cash acct Length cannot be longer than " 
    			+ SMTablearacctset.sCashAcctLength + " characters.");
    		bEntriesAreValid = false;
    	}
    	//Cash account CAN be blank
    	
    	return bEntriesAreValid;
	}
	public String getQueryString(){
		
		String sQueryString = "";
		sQueryString += ParamsAddingNewRecord + "=" + ARUtilities.URLEncode(m_iNewRecord);
		sQueryString += "&" + ParamsAcctSetCode + "=" + ARUtilities.URLEncode(m_sAcctSetCode);
		sQueryString += "&" + ParamsDescription + "=" + ARUtilities.URLEncode(m_sDescription);
		sQueryString += "&" + ParamiActive + "=" + ARUtilities.URLEncode(m_iActive);
		sQueryString += "&" + ParamdatLastMaintained + "=" + ARUtilities.URLEncode(m_datLastMaintained);
		sQueryString += "&" + ParamdsAcctsReceivableControlAcct + "=" + ARUtilities.URLEncode(m_sAcctsReceivableControlAcct);
		sQueryString += "&" + ParamsReceiptDiscountsAcct + "=" + ARUtilities.URLEncode(m_sReceiptDiscountsAcct);
		sQueryString += "&" + ParamsPrepaymentLiabilityAcct + "=" + ARUtilities.URLEncode(m_sPrepaymentLiabilityAcct);
		sQueryString += "&" + ParamsWriteOffAcct + "=" + ARUtilities.URLEncode(m_sWriteOffAcct);
		sQueryString += "&" + ParamsRetainageAcct + "=" + ARUtilities.URLEncode(m_sRetainageAcct);
		sQueryString += "&" + ParamsCashAcct + "=" + ARUtilities.URLEncode(m_sCashAcct);
		return sQueryString;
	}
	public boolean delete(String sAcctSetCode, ServletContext context, String sDBID){
		
		m_sErrorMessageArray.clear();
		
		//First, check that the terms exist:
		String SQL = ARSQLs.Get_AcctSet_By_Code(sAcctSetCode);
		
		try{
			ResultSet rs = clsDatabaseFunctions.openResultSet(
				SQL, 
				context,
				sDBID,
				"MySQL",
				this.toString() + ".delete (1)");
			if(!rs.next()){
				m_sErrorMessageArray.add("Account set " + sAcctSetCode + " cannot be found.");
				rs.close();
				return false;
			}else{
				rs.close();
			}
		}catch(SQLException e){
			System.out.println("Error checking account set to delete - " + e.getMessage());
			m_sErrorMessageArray.add("Error checking account set to delete - " + e.getMessage());
			return false;
		}
		
		//Customers
		SQL = ARSQLs.Get_Customers_By_AccountSet(sAcctSetCode);
		try{
			ResultSet rs = clsDatabaseFunctions.openResultSet(
					SQL, 
					context,
					sDBID,
					"MySQL",
					this.toString() + ".delete (2)");
			if(rs.next()){
				m_sErrorMessageArray.add("Account set " + sAcctSetCode + " is used on some customers.");
				rs.close();
				return false;
			}else{
				rs.close();
			}
		}catch(SQLException e){
			System.out.println("Error checking account sets on customers - " + e.getMessage());
			m_sErrorMessageArray.add("Error checking account sets on customers - " + e.getMessage());
			return false;
		}
		
		//Unexported invoice headers:
		SQL = ARSQLs.Get_Unexported_Invoices_For_AcctSet_SQL(sAcctSetCode);
		try{
			ResultSet rs = clsDatabaseFunctions.openResultSet(
					SQL, 
					context,
					sDBID,
					"MySQL",
					this.toString() + ".delete (3)");
			if(rs.next()){
				m_sErrorMessageArray.add("Account set " + sAcctSetCode + " is used on unexported invoices.");
				rs.close();
				return false;
			}else{
				rs.close();
			}
		}catch(SQLException e){
			System.out.println("Error checking account sets on invoices - " + e.getMessage());
			m_sErrorMessageArray.add("Error checking account sets on invoices - " + e.getMessage());
			return false;
		}
		
		//Order headers:
		SQL = ARSQLs.Get_Open_Orders_For_AcctSet_SQL(sAcctSetCode);
		try{
			ResultSet rs = clsDatabaseFunctions.openResultSet(
					SQL, 
					context,
					sDBID,
					"MySQL",
					this.toString() + ".delete (4)");
			if(rs.next()){
				m_sErrorMessageArray.add("Account set " + sAcctSetCode + " is used on open orders.");
				rs.close();
				return false;
			}else{
				rs.close();
			}
		}catch(SQLException e){
			System.out.println("Error checking account sets on orders - " + e.getMessage());
			m_sErrorMessageArray.add("Error checking account sets on orders - " + e.getMessage());
			return false;
		}
		
		try{
			SQL = ARSQLs.Delete_AcctSet_SQL(sAcctSetCode);
			if(!clsDatabaseFunctions.executeSQL(SQL, context, sDBID)){
				m_sErrorMessageArray.add("Error deleting account set");
				return false;
			}

		}catch(SQLException e){
			System.out.println("Error deleting account set - " + e.getMessage());
			m_sErrorMessageArray.add("Error deleting account set - " + e.getMessage());
			return false;
		}
		
		return true;
	}

	public String getM_sAcctSetCode() {
		return m_sAcctSetCode;
	}
	public void setM_sAcctSetCode(String acctSetCode) {
		m_sAcctSetCode = acctSetCode;
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
	public void setM_iActive(String active) {
		m_iActive = active;
	}
	public String getM_datLastMaintained() {
		return m_datLastMaintained;
	}
	public void setM_datLastMaintained(String lastMaintained) {
		m_datLastMaintained = lastMaintained;
	}
	public String getM_sAcctsReceivableControlAcct(){
		return m_sAcctsReceivableControlAcct;
	}
	public void setM_sAcctsReceivableControlAcct(String acctsReceivableControlAcct) {
		m_sAcctsReceivableControlAcct = acctsReceivableControlAcct;
	}
	public String getM_sReceiptDiscountsAcct() {
		return m_sReceiptDiscountsAcct;
	}
	public void setM_sReceiptDiscountsAcct(String receiptDiscountsAcct) {
		m_sReceiptDiscountsAcct = receiptDiscountsAcct;
	}
	public String getM_sPrepaymentLiabilityAcct() {
		return m_sPrepaymentLiabilityAcct;
	}
	public void setM_sPrepaymentLiabilityAcct(String prepaymentLiabilityAcct) {
		m_sPrepaymentLiabilityAcct = prepaymentLiabilityAcct;
	}
	public String getM_sWriteOffAcct() {
		return m_sWriteOffAcct;
	}
	public void setM_sWriteOffAcct(String writeOffAcct) {
		m_sWriteOffAcct = writeOffAcct;
	}
	public String getM_sRetainageAcct() {
		return m_sRetainageAcct;
	}
	public void setM_sRetainageAcct(String retainageAcct) {
		m_sRetainageAcct = retainageAcct;
	}
	public String getM_sCashAcct(){
		return m_sCashAcct;
	}
	public void setM_sCashAcct(String cashAcct) {
		m_sCashAcct = cashAcct;
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