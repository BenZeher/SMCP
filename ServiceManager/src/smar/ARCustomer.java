package smar;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Enumeration;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import smcontrolpanel.SMSystemFunctions;
import smcontrolpanel.SMUtilities;
import SMClasses.SMBatchStatuses;
import SMClasses.SMEntryBatch;
import SMClasses.SMModuleTypes;
import SMClasses.SMTax;
import SMDataDefinition.SMTablearacctset;
import SMDataDefinition.SMTablearcustomer;
import SMDataDefinition.SMTablearcustomershiptos;
import SMDataDefinition.SMTablearcustomerstatistics;
import SMDataDefinition.SMTablearmonthlystatistics;
import SMDataDefinition.SMTableartransactions;
import SMDataDefinition.SMTablecallsheets;
import SMDataDefinition.SMTabledefaultsalesgroupsalesperson;
import SMDataDefinition.SMTableentries;
import SMDataDefinition.SMTableinvoiceheaders;
import SMDataDefinition.SMTableorderdetails;
import SMDataDefinition.SMTableorderheaders;
import SMDataDefinition.SMTablesalescontacts;
import SMDataDefinition.SMTablesalesgroups;
import SMDataDefinition.SMTablesecuritygroupfunctions;
import SMDataDefinition.SMTablesecurityusergroups;
import SMDataDefinition.SMTablesitelocations;
import SMDataDefinition.SMTablesmoptions;
import SMDataDefinition.SMTabletax;
import SMDataDefinition.SMTableusers;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsDateAndTimeConversions;
import ServletUtilities.clsManageBigDecimals;
import ServletUtilities.clsManageRequestParameters;
import ServletUtilities.clsStringFunctions;
import ServletUtilities.clsValidateFormFields;

public class ARCustomer extends Object{
	public static final String ParamsAddingNewRecord = "bAddingNewRecord";
	public static final String ParamsCustomerNumber = "sCustomerNumber";
	public static final String ParamsCustomerName = "sCustomerName";
	public static final String ParamsAddressLine1 = "sAddressLine1";
	public static final String ParamsAddressLine2 = "sAddressLine2";
	public static final String ParamsAddressLine3 = "sAddressLine3";
	public static final String ParamsAddressLine4 = "sAddressLine4";
	public static final String ParamsCity = "sCity";
	public static final String ParamsState = "sState";
	public static final String ParamsCountry = "sCountry";
	public static final String ParamsPostalCode = "sPostalCode";
	public static final String ParamsContactName = "sContactName";
	public static final String ParamsPhoneNumber = "sPhoneNumber";
	public static final String ParamsFaxNumber = "sFaxNumber";
	public static final String ParamsTerms = "sTerms";
	public static final String ParamsAccountSet = "sAccountSet";
	public static final String ParamiOnHold = "iOnHold";
	public static final String ParamdatStartDate = "datStartDate";
	public static final String ParamdCreditLimit = "dCreditLimit";
	//public static final String ParammProductionNotes = "mProductionNotes";
	public static final String ParammAccountingNotes = "mAccountingNotes";
	public static final String ParammCustomerComments = "mCustomerComments";
	public static final String ParamiActive = "iActive";
	public static final String ParamdatLastMaintained = "datLastMaintained";
	public static final String ParamsLastEditUserFullName = "sLastEditUserFullName";
	public static final String ParamsLastEditUserID = "sLastEditUserID";
	public static final String ParamsCustomerGroup = "sCustomerGroup";
	//public static final String ParamsSalesperson = "sSalesperson";
	public static final String ParamsEmailAddress = "sEmailAddress";
	public static final String ParamsWebAddress = "sWebAddress";
	public static final String Paramspricelistcode = "spricelistcode";
	public static final String Paramspricelevel = "spricelevel";
	public static final String Paramiuseselectronicdeposit = "iuseselectronicdeposit";
	public static final String Paramirequiresstatements = "irequiresstatements";
	public static final String Paramirequirespo = "irequirespo";
	public static final String Paramsgdoclink = "sgdoclink";
	public static final String Paramitaxid = "itaxid";
	public static final String Paramsinvoicingcontact = "sinvoicingcontact";
	public static final String Paramsinvoicingemail = "sinvoicingemail";
	public static final String Paramsinvoicingnotes = "sinvoicingnotes";

	private String m_sCustomerNumber;
	private String m_sCustomerName;
	private String m_sAddressLine1;
	private String m_sAddressLine2;
	private String m_sAddressLine3;
	private String m_sAddressLine4;
	private String m_sCity;
	private String m_sState;
	private String m_sCountry;
	private String m_sPostalCode;
	private String m_sContactName;
	private String m_sPhoneNumber;
	private String m_sFaxNumber;
	private String m_sTerms;
	private String m_sAccountSet;
	private String m_iOnHold;
	private String m_datStartDate;
	private String m_dCreditLimit;
	//private String m_mProductionNotes;
	private String m_mAccountingNotes;
	private String m_mCustomerComments;
	private String m_iActive;
	private String m_datLastMaintained;
	private String m_sLastEditUserFullName;
	private String m_sLastEditUserID;
	private String m_sCustomerGroup;
	//private String m_sSalesperson;
	private String m_sEmailAddress;
	private String m_sWebAddress;
	private String m_sPriceListCode;
	private String m_sPriceLevel;
	//private String m_sTaxJurisdiction;
	//private String m_sTaxType;
	private String m_iNewRecord;
	private String m_sUsesElectronicDeposit;
	private String m_sRequiresStatements;
	private String m_sRequiresPO;
	private String m_sgdoclink;
	private String m_itaxid;
	private String m_sinvoicingcontact;
	private String m_sinvoicingemail;
	private String m_sinvoicingnotes;
	private ArrayList<String> m_sErrorMessageArray = new ArrayList<String> (0);
	private ArrayList<String> m_sSalesGroupIDArray = new ArrayList<String> (0);
	private ArrayList<String> m_sSalesGroupDefaultSalespersonArray = new ArrayList<String> (0);

    public ARCustomer(
    		String sCustomerNumber
        ) {
    	m_sCustomerNumber = sCustomerNumber;
    	m_sCustomerName = "";
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
    	m_sTerms = "";
    	m_sAccountSet = "";
    	m_iOnHold = "0";
    	m_datStartDate = clsDateAndTimeConversions.now("MM/dd/yyyy");
    	m_dCreditLimit = "0.00";
    	m_mAccountingNotes = "";
    	m_mCustomerComments = "";
    	m_iActive = "1";
    	m_datLastMaintained = clsDateAndTimeConversions.now("MM/dd/yyyy");
    	m_sLastEditUserFullName = "";
    	m_sLastEditUserID= "0";
    	m_sCustomerGroup = "";
    	m_sEmailAddress = "";
    	m_sWebAddress = "";
    	m_sPriceListCode = "";
    	m_sPriceLevel = "0";
    	m_iNewRecord = "1";
    	//m_sTaxJurisdiction = "";
    	//m_sTaxType = "0";
    	m_sUsesElectronicDeposit = "0";
    	m_sRequiresStatements = "0";
    	m_sRequiresPO = "0";
    	m_sgdoclink = "";
    	m_itaxid = "0";
    	m_sinvoicingcontact = "";
    	m_sinvoicingemail = "";
    	m_sinvoicingnotes = "";
    	m_sErrorMessageArray = new ArrayList<String> (0);
    	m_sSalesGroupIDArray = new ArrayList<String> (0);
    	m_sSalesGroupDefaultSalespersonArray = new ArrayList<String> (0);
        }
    
    public void loadFromHTTPRequest(HttpServletRequest req){
    	m_iNewRecord = clsManageRequestParameters.get_Request_Parameter(ARCustomer.ParamsAddingNewRecord, req).trim().replace("&quot;", "\"");
		m_sCustomerNumber = clsManageRequestParameters.get_Request_Parameter(ARCustomer.ParamsCustomerNumber, req).trim().replace("&quot;", "\"").toUpperCase();
		m_sCustomerName = clsManageRequestParameters.get_Request_Parameter(ARCustomer.ParamsCustomerName, req).trim().replace("&quot;", "\"");
		m_sAddressLine1 = clsManageRequestParameters.get_Request_Parameter(ARCustomer.ParamsAddressLine1, req).trim().replace("&quot;", "\"");
		m_sAddressLine2 = clsManageRequestParameters.get_Request_Parameter(ARCustomer.ParamsAddressLine2, req).trim().replace("&quot;", "\"");
		m_sAddressLine3 = clsManageRequestParameters.get_Request_Parameter(ARCustomer.ParamsAddressLine3, req).trim().replace("&quot;", "\"");
		m_sAddressLine4 = clsManageRequestParameters.get_Request_Parameter(ARCustomer.ParamsAddressLine4, req).trim().replace("&quot;", "\"");
		m_sCity = clsManageRequestParameters.get_Request_Parameter(ARCustomer.ParamsCity, req).trim().replace("&quot;", "\"");
		m_sState = clsManageRequestParameters.get_Request_Parameter(ARCustomer.ParamsState, req).trim().replace("&quot;", "\"");
		m_sCountry = clsManageRequestParameters.get_Request_Parameter(ARCustomer.ParamsCountry, req).trim().replace("&quot;", "\"");
		m_sPostalCode = clsManageRequestParameters.get_Request_Parameter(ARCustomer.ParamsPostalCode, req).trim().replace("&quot;", "\"");
		m_sContactName = clsManageRequestParameters.get_Request_Parameter(ARCustomer.ParamsContactName, req).trim().replace("&quot;", "\"");
		m_sPhoneNumber = clsManageRequestParameters.get_Request_Parameter(ARCustomer.ParamsPhoneNumber, req).trim().replace("&quot;", "\"");
		m_sFaxNumber = clsManageRequestParameters.get_Request_Parameter(ARCustomer.ParamsFaxNumber, req).trim().replace("&quot;", "\"");
		m_sTerms = clsManageRequestParameters.get_Request_Parameter(ARCustomer.ParamsTerms, req).trim().replace("&quot;", "\"");
		m_sAccountSet = clsManageRequestParameters.get_Request_Parameter(ARCustomer.ParamsAccountSet, req).trim().replace("&quot;", "\"");
		//The following allows for BOTH the case in which this parameter is coming from an actual checkbox
		//(in which case a 'false' is indicated by a null) AND the case in which it's coming from a query
		//string (in which case a 'false' is indicated by a '0' in the parameter):
		if(req.getParameter(ARCustomer.ParamiOnHold) == null){
			m_iOnHold = "0";
		}else{
			if(req.getParameter(ARCustomer.ParamiOnHold).compareToIgnoreCase("0") ==0){
				m_iOnHold = "0";
			}else{
				m_iOnHold = "1";
			}
		}
		m_datStartDate = clsManageRequestParameters.get_Request_Parameter(ARCustomer.ParamdatStartDate, req).trim().replace("&quot;", "\"");
		if(m_datStartDate.compareToIgnoreCase("") == 0){
			m_datStartDate = clsDateAndTimeConversions.now("MM/dd/yyyy");
		}
		m_dCreditLimit = clsManageRequestParameters.get_Request_Parameter(ARCustomer.ParamdCreditLimit, req).trim().replace("&quot;", "\"");
		if(m_dCreditLimit.compareToIgnoreCase("") == 0){
			m_dCreditLimit = "0.00";
		}
		m_mAccountingNotes = clsManageRequestParameters.get_Request_Parameter(ARCustomer.ParammAccountingNotes, req).trim().replace("&quot;", "\"");
		m_mCustomerComments = clsManageRequestParameters.get_Request_Parameter(ARCustomer.ParammCustomerComments, req).trim().replace("&quot;", "\"");
		//The following allows for BOTH the case in which this parameter is coming from an actual checkbox
		//(in which case a 'false' is indicated by a null) AND the case in which it's coming from a query
		//string (in which case a 'false' is indicated by a '0' in the parameter):
		if(req.getParameter(ARCustomer.ParamiActive) == null){
			m_iActive = "0";
		}else{
			if(req.getParameter(ARCustomer.ParamiActive).compareToIgnoreCase("0") ==0){
				m_iActive = "0";
			}else{
				m_iActive = "1";
			}
		}
		m_datLastMaintained = clsManageRequestParameters.get_Request_Parameter(ARCustomer.ParamdatLastMaintained, req).trim().replace("&quot;", "\"");
		if(m_datLastMaintained.compareToIgnoreCase("") == 0){
			m_datLastMaintained = clsDateAndTimeConversions.now("MM/dd/yyyy");
		}
		m_sLastEditUserFullName = clsManageRequestParameters.get_Request_Parameter(ARCustomer.ParamsLastEditUserFullName, req).trim().replace("&quot;", "\"");
		m_sLastEditUserID = clsManageRequestParameters.get_Request_Parameter(ARCustomer.ParamsLastEditUserID, req).trim().replace("&quot;", "\"");
		m_sCustomerGroup = clsManageRequestParameters.get_Request_Parameter(ARCustomer.ParamsCustomerGroup, req).trim().replace("&quot;", "\"");
		m_sEmailAddress = clsManageRequestParameters.get_Request_Parameter(ARCustomer.ParamsEmailAddress, req).trim().replace("&quot;", "\"");
		m_sWebAddress = clsManageRequestParameters.get_Request_Parameter(ARCustomer.ParamsWebAddress, req).trim().replace("&quot;", "\"");
		m_sPriceListCode = clsManageRequestParameters.get_Request_Parameter(ARCustomer.Paramspricelistcode, req).trim().replace("&quot;", "\"");
		m_sPriceLevel = clsManageRequestParameters.get_Request_Parameter(ARCustomer.Paramspricelevel, req).trim().replace("&quot;", "\"");

		
		if(req.getParameter(ARCustomer.Paramiuseselectronicdeposit) == null){
			m_sUsesElectronicDeposit = "0";
		}else{
			if(req.getParameter(ARCustomer.Paramiuseselectronicdeposit).compareToIgnoreCase("0") ==0){
				m_sUsesElectronicDeposit = "0";
			}else{
				m_sUsesElectronicDeposit = "1";
			}
		}
		if(req.getParameter(ARCustomer.Paramirequiresstatements) == null){
			m_sRequiresStatements = "0";
		}else{
			if(req.getParameter(ARCustomer.Paramirequiresstatements).compareToIgnoreCase("0") ==0){
				m_sRequiresStatements = "0";
			}else{
				m_sRequiresStatements = "1";
			}
		}
		if(req.getParameter(ARCustomer.Paramirequirespo) == null){
			m_sRequiresPO = "0";
		}else{
			if(req.getParameter(ARCustomer.Paramirequirespo).compareToIgnoreCase("0") ==0){
				m_sRequiresPO = "0";
			}else{
				m_sRequiresPO = "1";
			}
		}
		m_sgdoclink = clsManageRequestParameters.get_Request_Parameter(ARCustomer.Paramsgdoclink, req).trim().replace("&quot;", "\"");
		m_itaxid = clsManageRequestParameters.get_Request_Parameter(ARCustomer.Paramitaxid, req).trim().replace("&quot;", "\"");
		m_sinvoicingcontact = clsManageRequestParameters.get_Request_Parameter(ARCustomer.Paramsinvoicingcontact, req).trim().replace("&quot;", "\"");
		m_sinvoicingemail = clsManageRequestParameters.get_Request_Parameter(ARCustomer.Paramsinvoicingemail, req).trim().replace("&quot;", "\"");
		m_sinvoicingnotes = clsManageRequestParameters.get_Request_Parameter(ARCustomer.Paramsinvoicingnotes, req).trim().replace("&quot;", "\"");
		//Load the default salespersons for each sales group:
		Enumeration <String> e = req.getParameterNames();
		String sParam = "";
		while (e.hasMoreElements()){
			sParam = e.nextElement();
			if (sParam.contains(AREditCustomersEdit.CUSTOMER_SALESGROUP_SALESPERSON_FIELD)){
				String sSalesGroupID = sParam.substring(
						AREditCustomersEdit.CUSTOMER_SALESGROUP_SALESPERSON_FIELD.length(), sParam.length());
				//if (sSalesGroupID.compareToIgnoreCase("") != 0){
					m_sSalesGroupIDArray.add(sSalesGroupID);
					m_sSalesGroupDefaultSalespersonArray.add(clsManageRequestParameters.get_Request_Parameter(sParam, req));
				//}
			}
		}
    }
    public String getCashAccount(
    		ServletContext context, 
    		String sDBID
    		){
        
    	    String SQL = "SELECT "
    	    		+ SMTablearacctset.sCashAcct
    	    		+ " FROM " + SMTablearcustomer.TableName
    	    		+ ", " + SMTablearacctset.TableName
    	    		+ " WHERE (" 
    	    			+ "(" + SMTablearcustomer.sCustomerNumber + " = '" + m_sCustomerNumber + "')"
    	    			+ " AND (" + SMTablearcustomer.TableName + "." + SMTablearcustomer.sAccountSet 
    	    				+ " = " + SMTablearacctset.TableName + "." + SMTablearacctset.sAcctSetCode + ")"
    	    		+ ")";
    		try {
    			ResultSet rs = clsDatabaseFunctions.openResultSet(
    				SQL, 
    				context, 
    				sDBID,
    				"MySQL",
    				this.toString() + ".getCashAccount");
    			String sCashAccount; 
    			if (rs.next()){
    				sCashAccount = rs.getString(SMTablearacctset.sCashAcct);
    			}else{
    				sCashAccount  = "";
    			}
    			rs.close();
    			//Load the name:
    			return sCashAccount;
    			
    		}catch (SQLException ex){
    	    	System.out.println("Error in " + this.toString()+ ".getCashAccount class!!");
    	        System.out.println("SQLException: " + ex.getMessage());
    	        System.out.println("SQLState: " + ex.getSQLState());
    	        System.out.println("SQL: " + ex.getErrorCode());
    	        return "";
    		}
        }
    public String getARRetainageAccount(
    		ServletContext context, 
    		String sDBID
    		){
        
    	    String SQL = "SELECT "
    			+ SMTablearacctset.sRetainageAcct
    			+ " FROM " + SMTablearcustomer.TableName
    			+ ", " + SMTablearacctset.TableName
    			+ " WHERE (" 
    				+ "(" + SMTablearcustomer.sCustomerNumber + " = '" + m_sCustomerNumber + "')"
    				+ " AND (" + SMTablearcustomer.TableName + "." + SMTablearcustomer.sAccountSet 
    				+ " = " + SMTablearacctset.TableName + "." + SMTablearacctset.sAcctSetCode + ")"
    			+ ")";
    		try {
    			ResultSet rs = clsDatabaseFunctions.openResultSet(
        				SQL, 
        				context, 
        				sDBID,
        				"MySQL",
        				this.toString() + ".getARRetainageAccount"); 
    			String sARControlAcct;
    			if (rs.next()){
    				sARControlAcct = rs.getString(SMTablearacctset.sRetainageAcct);
    			}else{
    				sARControlAcct = "";
    			}
    			rs.close();
    			//Load the name:
    			return sARControlAcct;
    			
    		}catch (SQLException ex){
    	    	System.out.println("Error in " + this.toString()+ ".getARControlAccount class!!");
    	        System.out.println("SQLException: " + ex.getMessage());
    	        System.out.println("SQLState: " + ex.getSQLState());
    	        System.out.println("SQL: " + ex.getErrorCode());
    	        return "";
    		}
        }
    public String getARControlAccount(
    		ServletContext context, 
    		String sDBID
    		){
        
    	    String SQL =  "SELECT "
    	    		+ SMTablearacctset.sAcctsReceivableControlAcct
    	    		+ " FROM " + SMTablearcustomer.TableName
    	    		+ ", " + SMTablearacctset.TableName
    	    		+ " WHERE (" 
    	    			+ "(" + SMTablearcustomer.sCustomerNumber + " = '" + m_sCustomerNumber + "')"
    	    			+ " AND (" + SMTablearcustomer.TableName + "." + SMTablearcustomer.sAccountSet 
    	    				+ " = " + SMTablearacctset.TableName + "." + SMTablearacctset.sAcctSetCode + ")"
    	    		+ ")";
    		try {
    			ResultSet rs = clsDatabaseFunctions.openResultSet(
        				SQL, 
        				context, 
        				sDBID,
        				"MySQL",
        				this.toString() + ".getARControlAccount"); 
    			String sARControlAcct;
    			if (rs.next()){
    				sARControlAcct = rs.getString(SMTablearacctset.sAcctsReceivableControlAcct);
    			}else{
    				sARControlAcct = "";
    			}
    			rs.close();
    			//Load the name:
    			return sARControlAcct;
    			
    		}catch (SQLException ex){
    	    	System.out.println("Error in " + this.toString()+ ".getARControlAccount class!!");
    	        System.out.println("SQLException: " + ex.getMessage());
    	        System.out.println("SQLState: " + ex.getSQLState());
    	        System.out.println("SQL: " + ex.getErrorCode());
    	        return "";
    		}
        }
    public String getARPrepayLiabilityAccount(
    		ServletContext context, 
    		String sDBID
    		){
        
    		String SQL = "SELECT "
    			+ SMTablearacctset.sPrepaymentLiabilityAcct
    			+ " FROM " + SMTablearcustomer.TableName
    			+ ", " + SMTablearacctset.TableName
    			+ " WHERE (" 
    				+ "(" + SMTablearcustomer.sCustomerNumber + " = '" + m_sCustomerNumber + "')"
    				+ " AND (" + SMTablearcustomer.TableName + "." + SMTablearcustomer.sAccountSet 
    					+ " = " + SMTablearacctset.TableName + "." + SMTablearacctset.sAcctSetCode + ")"
    			+ ")";
    		try {
    			ResultSet rs = clsDatabaseFunctions.openResultSet(
        				SQL, 
        				context, 
        				sDBID,
        				"MySQL",
        				this.toString() + ".getARPrepayLiabilityAccount"); 
    			String sARPrepaymentLiabilityAcct;
    			if (rs.next()){
    				sARPrepaymentLiabilityAcct = rs.getString(SMTablearacctset.sPrepaymentLiabilityAcct);
    			}else{
    				sARPrepaymentLiabilityAcct = "";
    			}
    			rs.close();
    			//Load the name:
    			return sARPrepaymentLiabilityAcct;
    			
    		}catch (SQLException ex){
    	    	System.out.println("Error in " + this.toString()+ ".getARPrepayLiabilityAccount class!!");
    	        System.out.println("SQLException: " + ex.getMessage());
    	        System.out.println("SQLState: " + ex.getSQLState());
    	        System.out.println("SQL: " + ex.getErrorCode());
    	        return "";
    		}
        }
    public String getARPrepayLiabilityAccount(
    		Connection conn 
    		){
        
    		String SQL = "SELECT "
    			+ SMTablearacctset.sPrepaymentLiabilityAcct
    			+ " FROM " + SMTablearcustomer.TableName
    			+ ", " + SMTablearacctset.TableName
    			+ " WHERE (" 
    				+ "(" + SMTablearcustomer.sCustomerNumber + " = '" + m_sCustomerNumber + "')"
    				+ " AND (" + SMTablearcustomer.TableName + "." + SMTablearcustomer.sAccountSet 
    					+ " = " + SMTablearacctset.TableName + "." + SMTablearacctset.sAcctSetCode + ")"
    			+ ")";
    		try {
    			ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
    			String sARPrepaymentLiabilityAcct;
    			if (rs.next()){
    				sARPrepaymentLiabilityAcct = rs.getString(SMTablearacctset.sPrepaymentLiabilityAcct);
    			}else{
    				sARPrepaymentLiabilityAcct = "";
    			}
    			rs.close();
    			//Load the name:
    			return sARPrepaymentLiabilityAcct;
    			
    		}catch (SQLException ex){
    	    	System.out.println("Error in " + this.toString()+ ".getARPrepayLiabilityAccount class!!");
    	        System.out.println("SQLException: " + ex.getMessage());
    	        System.out.println("SQLState: " + ex.getSQLState());
    	        System.out.println("SQL: " + ex.getErrorCode());
    	        return "";
    		}
    }

    //Need this one with the connection for importing invoices:
    public String getARControlAccount(
    		Connection conn
    		){
        
    	    String SQL =  "SELECT "
    	    		+ SMTablearacctset.sAcctsReceivableControlAcct
    	    		+ " FROM " + SMTablearcustomer.TableName
    	    		+ ", " + SMTablearacctset.TableName
    	    		+ " WHERE (" 
    	    			+ "(" + SMTablearcustomer.sCustomerNumber + " = '" + m_sCustomerNumber + "')"
    	    			+ " AND (" + SMTablearcustomer.TableName + "." + SMTablearcustomer.sAccountSet 
    	    				+ " = " + SMTablearacctset.TableName + "." + SMTablearacctset.sAcctSetCode + ")"
    	    		+ ")";
    		try {
    			ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn); 
    			String sARControlAcct;
    			if (rs.next()){
    				sARControlAcct = rs.getString(SMTablearacctset.sAcctsReceivableControlAcct);
    			}else{
    				sARControlAcct = "";
    			}
    			rs.close();
    			//Load the name:
    			return sARControlAcct;
    			
    		}catch (SQLException ex){
    	    	System.out.println("Error in " + this.toString()+ ".getARControlAccount class!!");
    	        System.out.println("SQLException: " + ex.getMessage());
    	        System.out.println("SQLState: " + ex.getSQLState());
    	        System.out.println("SQL: " + ex.getErrorCode());
    	        return "";
    		}
        } 
    
    public BigDecimal getCurrentStoredBalance(
    		ServletContext context,
    		String sDBID
    		){
        
    	    String SQL = 
    	    	"SELECT " + SMTablearcustomerstatistics.sCurrentBalance
    	    	+ " FROM " + SMTablearcustomerstatistics.TableName
    	    	+ " WHERE " + SMTablearcustomerstatistics.sCustomerNumber
    	    	+ " = '" + m_sCustomerNumber + "'";
    	    	;
    		try {
    			ResultSet rs = clsDatabaseFunctions.openResultSet(
    				SQL, 
    				context, 
    				sDBID, 
    				"MySQL", 
    				this.toString() + ".getCurrentStoredBalance"); 
    			BigDecimal bdCurrentBalance = BigDecimal.ZERO;
    			if (rs.next()){
    				bdCurrentBalance = rs.getBigDecimal(SMTablearcustomerstatistics.sCurrentBalance);
    			}
    			rs.close();
    			//Load the name:
    			return bdCurrentBalance;
    			
    		}catch (SQLException ex){
    	    	System.out.println("Error in " + this.toString()+ ".getCurrentBalance class!!");
    	        System.out.println("SQLException: " + ex.getMessage());
    	        System.out.println("SQLState: " + ex.getSQLState());
    	        System.out.println("SQL: " + ex.getErrorCode());
    	        return BigDecimal.ZERO;
    		}
        }
    public BigDecimal getCurrentStoredBalance(
    		Connection conn
    		){
        
    	    String SQL = 
    	    	"SELECT " + SMTablearcustomerstatistics.sCurrentBalance
    	    	+ " FROM " + SMTablearcustomerstatistics.TableName
    	    	+ " WHERE " + SMTablearcustomerstatistics.sCustomerNumber
    	    	+ " = '" + m_sCustomerNumber + "'";
    	    	;
    		try {
    			ResultSet rs = clsDatabaseFunctions.openResultSet(
    				SQL, 
    				conn); 
    			BigDecimal bdCurrentBalance = BigDecimal.ZERO;
    			if (rs.next()){
    				bdCurrentBalance = rs.getBigDecimal(SMTablearcustomerstatistics.sCurrentBalance);
    			}
    			rs.close();
    			//Load the name:
    			return bdCurrentBalance;
    			
    		}catch (SQLException ex){
    	    	System.out.println("Error in " + this.toString()+ ".getCurrentBalance class!!");
    	        System.out.println("SQLException: " + ex.getMessage());
    	        System.out.println("SQLState: " + ex.getSQLState());
    	        System.out.println("SQL: " + ex.getErrorCode());
    	        return BigDecimal.ZERO;
    		}
        }

    public BigDecimal getCurrentCalculatedBalance(
    		ServletContext context,
    		String sDBID
    		){
        
    	    String SQL = 
    	    	"SELECT SUM(" + SMTableartransactions.dcurrentamt + ") AS calculatedbalance"
    	    	+ " FROM " + SMTableartransactions.TableName
    	    	+ " WHERE (" 
    	    		+ "(" + SMTableartransactions.iretainage + " = 0)"
    	    		+ " AND (" + SMTableartransactions.spayeepayor + " = '" + m_sCustomerNumber + "')"
    	    	+ ")"
    	    	;
    	    	
    		try {
    			ResultSet rs = clsDatabaseFunctions.openResultSet(
    				SQL, 
    				context, 
    				sDBID, 
    				"MySQL", 
    				this.toString() + ".getCurrentCalculatedBalance"
    				); 
    			BigDecimal bdCalculatedBalance = BigDecimal.ZERO;
    			if (rs.next()){
    				bdCalculatedBalance = rs.getBigDecimal("calculatedbalance");
    			}
    			rs.close();
    			//Load the name:
    			return bdCalculatedBalance;
    			
    		}catch (SQLException ex){
    	    	System.out.println("Error in " + this.toString()+ ".getCurrentCalculatedBalance class!!");
    	        System.out.println("SQLException: " + ex.getMessage());
    	        System.out.println("SQLState: " + ex.getSQLState());
    	        System.out.println("SQL: " + ex.getErrorCode());
    	        return BigDecimal.ZERO;
    		}
        }
    public BigDecimal getCurrentCalculatedBalance(
    		Connection conn
    		){
        
    	    String SQL = 
    	    	"SELECT SUM(" + SMTableartransactions.dcurrentamt + ") AS calculatedbalance"
    	    	+ " FROM " + SMTableartransactions.TableName
    	    	+ " WHERE (" 
    	    		+ "(" + SMTableartransactions.iretainage + " = 0)"
    	    		+ " AND (" + SMTableartransactions.spayeepayor + " = '" + m_sCustomerNumber + "')"
    	    	+ ")"
    	    	;
    	    	
    		try {
    			ResultSet rs = clsDatabaseFunctions.openResultSet(
    				SQL, 
    				conn
    				); 
    			BigDecimal bdCalculatedBalance = BigDecimal.ZERO;
    			if (rs.next()){
    				bdCalculatedBalance = rs.getBigDecimal("calculatedbalance");
    			}
    			rs.close();
    			//Load the name:
    			return bdCalculatedBalance;
    			
    		}catch (SQLException ex){
    	    	System.out.println("Error in " + this.toString()+ ".getCurrentCalculatedBalance class!!");
    	        System.out.println("SQLException: " + ex.getMessage());
    	        System.out.println("SQLState: " + ex.getSQLState());
    	        System.out.println("SQL: " + ex.getErrorCode());
    	        return BigDecimal.ZERO;
    		}
        }

    public BigDecimal getCalculatedRetainageBalance(
    		ServletContext context,
    		String sDBID
    		){
        
    	    String SQL = 
    	    	"SELECT SUM(" + SMTableartransactions.dcurrentamt + ") AS retainagebalance"
    	    	+ " FROM " + SMTableartransactions.TableName
    	    	+ " WHERE (" 
    	    		+ "(" + SMTableartransactions.iretainage + " = 1)"
    	    		+ " AND (" + SMTableartransactions.spayeepayor + " = '" + m_sCustomerNumber + "')"
    	    	+ ")"
    	    	;
    	    	
    		try {
    			ResultSet rs = clsDatabaseFunctions.openResultSet(
    				SQL, 
    				context, 
    				sDBID, 
    				"MySQL",
    				this.toString() + ".getCalculatedRetainageBalance"
    			); 
    			BigDecimal bdRetainageBalance = BigDecimal.ZERO;
    			if (rs.next()){
    				bdRetainageBalance = rs.getBigDecimal("retainagebalance");
    			}
    			rs.close();
    			//Load the name:
    			return bdRetainageBalance;
    			
    		}catch (SQLException ex){
    	    	System.out.println("Error in " + this.toString()+ ".getRetainageBalance class!!");
    	        System.out.println("SQLException: " + ex.getMessage());
    	        System.out.println("SQLState: " + ex.getSQLState());
    	        System.out.println("SQL: " + ex.getErrorCode());
    	        return BigDecimal.ZERO;
    		}
        }
    public BigDecimal getCalculatedRetainageBalance(
    		Connection conn
    		){
        
    	    String SQL = 
    	    	"SELECT SUM(" + SMTableartransactions.dcurrentamt + ") AS retainagebalance"
    	    	+ " FROM " + SMTableartransactions.TableName
    	    	+ " WHERE (" 
    	    		+ "(" + SMTableartransactions.iretainage + " = 1)"
    	    		+ " AND (" + SMTableartransactions.spayeepayor + " = '" + m_sCustomerNumber + "')"
    	    	+ ")"
    	    	;
    	    	
    		try {
    			ResultSet rs = clsDatabaseFunctions.openResultSet(
    				SQL, 
    				conn
    			); 
    			BigDecimal bdRetainageBalance = BigDecimal.ZERO;
    			if (rs.next()){
    				bdRetainageBalance = rs.getBigDecimal("retainagebalance");
    			}
    			rs.close();
    			//Load the name:
    			return bdRetainageBalance;
    			
    		}catch (SQLException ex){
    	    	System.out.println("Error in " + this.toString()+ ".getRetainageBalance class!!");
    	        System.out.println("SQLException: " + ex.getMessage());
    	        System.out.println("SQLState: " + ex.getSQLState());
    	        System.out.println("SQL: " + ex.getErrorCode());
    	        return BigDecimal.ZERO;
    		}
        }

    private boolean load(
			String sCustomerNumber,
    		ServletContext context, 
    		String sDBID
			){
		m_sErrorMessageArray.clear();
		String sSQL = "";
		try{
			//Get the record to edit:
			sSQL = "SELECT * FROM " + SMTablearcustomer.TableName + 
					" WHERE (" + 
					"(" + SMTablearcustomer.sCustomerNumber + " = '" + sCustomerNumber + "')" +
				")";
	        ResultSet rs = clsDatabaseFunctions.openResultSet(
    				sSQL, 
    				context, 
    				sDBID, 
    				"MySQL", 
    				this.toString() + ".load");
	        if (loadFromResultSet(rs)){
	        	rs.close();
	        }else{
	        	rs.close();
	        	return false;
	        }
		}catch (SQLException ex){
			m_sErrorMessageArray.add("Error [1387471820] loading customer with SQL: " + sSQL + " - " + ex.getMessage());
			return false;
		}
		try {
			loadDefaultSalespersons(null, sDBID, context);
		} catch (SQLException e) {
			m_sErrorMessageArray.add("Error loading default salespersons - " + e.getMessage());
			return false;
		}
		return true;
	}
	//Need this one with the connection:
	private boolean load(
			String sCustomerNumber,
			Connection conn
			){
		m_sErrorMessageArray.clear();
		String sSQL = "";
		try{
			//Get the record to edit:
			sSQL = "SELECT * FROM " + SMTablearcustomer.TableName + 
					" WHERE (" + 
					"(" + SMTablearcustomer.sCustomerNumber + " = '" + sCustomerNumber + "')" +
				")";
	        ResultSet rs = clsDatabaseFunctions.openResultSet(sSQL, conn);
	        if (loadFromResultSet(rs)){
	        	rs.close();
	        }else{
	        	m_sErrorMessageArray.add("No record for Customer '" + sCustomerNumber + "'.");
	        	rs.close();
	        	return false;
	        }
	        
		}catch (SQLException ex){
			m_sErrorMessageArray.add("Error [1387471821] loading customer with SQL: " + sSQL + " - " + ex.getMessage());
			return false;
		}
		try {
			loadDefaultSalespersons(conn, "", null);
		} catch (SQLException e) {
			m_sErrorMessageArray.add("Error [1387471822] loading default salespersons - " + e.getMessage());
			return false;
		}
		return true;
	}
	private void loadDefaultSalespersons(Connection conn, String sDBID, ServletContext context) throws SQLException{
		
		//Now load any default salespersons:
		String SQL = "SELECT * FROM " + SMTabledefaultsalesgroupsalesperson.TableName
			+ " LEFT JOIN " + SMTablesalesgroups.TableName + " ON "
			+ SMTabledefaultsalesgroupsalesperson.TableName + "." + SMTabledefaultsalesgroupsalesperson.lsalesgroupid
			+ " = " + SMTablesalesgroups.TableName + "." + SMTablesalesgroups.iSalesGroupId
			+ " WHERE ("
				+ "(" + SMTabledefaultsalesgroupsalesperson.TableName + "." 
				+ SMTabledefaultsalesgroupsalesperson.scustomercode + " = '" + m_sCustomerNumber + "')"
			+ ")"
		;
		ResultSet rs;
		if (conn == null){
			rs = clsDatabaseFunctions.openResultSet(SQL, context, sDBID, "MySQL", this.toString() + ".loadDefaultSalespersons");
		}else{
			rs = clsDatabaseFunctions.openResultSet(SQL, conn);
		}
		m_sSalesGroupIDArray.clear();
		m_sSalesGroupDefaultSalespersonArray.clear();
		while (rs.next()){
			m_sSalesGroupIDArray.add(Long.toString(rs.getLong(SMTablesalesgroups.TableName + "." + SMTablesalesgroups.iSalesGroupId)));
			m_sSalesGroupDefaultSalespersonArray.add(SMTabledefaultsalesgroupsalesperson.TableName + "." + SMTabledefaultsalesgroupsalesperson.ssalespersoncode);
		}
	}
	private boolean loadFromResultSet(ResultSet rs){
		try{
	        if (rs.next()){
	        	m_sCustomerName = clsStringFunctions.checkStringForNull(rs.getString(SMTablearcustomer.sCustomerName));
	        	m_sAddressLine1 = clsStringFunctions.checkStringForNull(rs.getString(SMTablearcustomer.sAddressLine1));
	        	m_sAddressLine2 = clsStringFunctions.checkStringForNull(rs.getString(SMTablearcustomer.sAddressLine2));
	        	m_sAddressLine3 = clsStringFunctions.checkStringForNull(rs.getString(SMTablearcustomer.sAddressLine3));
	        	m_sAddressLine4 = clsStringFunctions.checkStringForNull(rs.getString(SMTablearcustomer.sAddressLine4));
	        	m_sCity = clsStringFunctions.checkStringForNull(rs.getString(SMTablearcustomer.sCity));
	        	m_sState = clsStringFunctions.checkStringForNull(rs.getString(SMTablearcustomer.sState));
	        	m_sCountry = clsStringFunctions.checkStringForNull(rs.getString(SMTablearcustomer.sCountry));
	        	m_sPostalCode = clsStringFunctions.checkStringForNull(rs.getString(SMTablearcustomer.sPostalCode));
	        	m_sContactName = clsStringFunctions.checkStringForNull(rs.getString(SMTablearcustomer.sContactName));
	        	m_sPhoneNumber = clsStringFunctions.checkStringForNull(rs.getString(SMTablearcustomer.sPhoneNumber));
	        	m_sFaxNumber = clsStringFunctions.checkStringForNull(rs.getString(SMTablearcustomer.sFaxNumber));
	        	m_sTerms = clsStringFunctions.checkStringForNull(rs.getString(SMTablearcustomer.sTerms));
	        	m_sAccountSet = clsStringFunctions.checkStringForNull(rs.getString(SMTablearcustomer.sAccountSet));
	        	m_iOnHold = Integer.toString(rs.getInt(SMTablearcustomer.iOnHold));
	        	if(clsDateAndTimeConversions.IsValidDate(rs.getDate(SMTablearcustomer.datStartDate))){
	        		m_datStartDate = clsDateAndTimeConversions.utilDateToString(rs.getDate(SMTablearcustomer.datStartDate),"MM/dd/yyyy");
	        	}else{
	        		m_datStartDate = clsDateAndTimeConversions.now("MM/dd/yyyy");
	        	}
	        	m_dCreditLimit = clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(rs.getBigDecimal(SMTablearcustomer.dCreditLimit));
	        	m_mAccountingNotes = clsStringFunctions.checkStringForNull(rs.getString(SMTablearcustomer.mAccountingNotes));
	        	m_mCustomerComments = clsStringFunctions.checkStringForNull(rs.getString(SMTablearcustomer.mCustomerComments));
	        	m_iActive = Integer.toString(rs.getInt(SMTablearcustomer.iActive));
	        	if(clsDateAndTimeConversions.IsValidDate(rs.getDate(SMTablearcustomer.datLastMaintained))){
	        		m_datLastMaintained = clsDateAndTimeConversions.utilDateToString(rs.getDate(SMTablearcustomer.datLastMaintained),"MM/dd/yyyy");
	        	}else{
	        		m_datLastMaintained = clsDateAndTimeConversions.now("MM/dd/yyyy");
	        	}	        	
	        	m_sLastEditUserFullName = clsStringFunctions.checkStringForNull(rs.getString(SMTablearcustomer.sLastEditUserFullName));
	        	m_sLastEditUserID = clsStringFunctions.checkStringForNull(Long.toString(rs.getLong(SMTablearcustomer.lLastEditUserID)));
	        	m_sCustomerGroup = clsStringFunctions.checkStringForNull(rs.getString(SMTablearcustomer.sCustomerGroup));
	        	m_sEmailAddress = clsStringFunctions.checkStringForNull(rs.getString(SMTablearcustomer.sEmailAddress));
	        	m_sWebAddress = clsStringFunctions.checkStringForNull(rs.getString(SMTablearcustomer.sWebAddress));
	        	m_sPriceListCode = clsStringFunctions.checkStringForNull(rs.getString(SMTablearcustomer.sPriceListCode));
	        	m_sPriceLevel = Long.toString(rs.getLong(SMTablearcustomer.ipricelevel));
	        	m_iNewRecord = "0";
	        	m_sUsesElectronicDeposit = Integer.toString(rs.getInt(SMTablearcustomer.iuseselectronicdeposit));
	        	m_sRequiresStatements = Integer.toString(rs.getInt(SMTablearcustomer.irequiresstatements));
	        	m_sRequiresPO = Integer.toString(rs.getInt(SMTablearcustomer.irequirespo));
	        	m_sgdoclink = clsStringFunctions.checkStringForNull(rs.getString(SMTablearcustomer.sgdoclink));
	        	m_sCustomerNumber = clsStringFunctions.checkStringForNull(rs.getString(SMTablearcustomer.sCustomerNumber));
	        	m_itaxid = Integer.toString(rs.getInt(SMTablearcustomer.itaxid));
	        	m_sinvoicingcontact = clsStringFunctions.checkStringForNull(rs.getString(SMTablearcustomer.sinvoicingcontact));
	        	m_sinvoicingemail = clsStringFunctions.checkStringForNull(rs.getString(SMTablearcustomer.sinvoicingemail));
	        	m_sinvoicingnotes = clsStringFunctions.checkStringForNull(rs.getString(SMTablearcustomer.sinvoicingnotes));
	        	rs.close();
	        }else{
	        	m_sErrorMessageArray.add("No customer found.");
	        	rs.close();
	        	return false;
	        }

		}catch(SQLException ex){
			m_sErrorMessageArray.add("Error [1387471823] loading customer from ResultSet - " + ex.getMessage());
	        return false;
		}

		return true;
	}
	public boolean load(
    		ServletContext context, 
    		String sDBID
			){

		return load(m_sCustomerNumber, context, sDBID);
	}
	public boolean load(
    		Connection conn
			){

		return load(m_sCustomerNumber, conn);
	}
	//Need a connection here for the data transaction:
	public boolean save (String sUserFullName, String sUserID, String sCompany, Connection conn){
		m_sErrorMessageArray.clear();
		//Check to see if the record already exists:
		String SQL = "SELECT * FROM " + SMTablearcustomer.TableName + 
				" WHERE (" + 
				"(" + SMTablearcustomer.sCustomerNumber + " = '" + m_sCustomerNumber + "')" +
			")";
		try{
			ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
			if(rs.next()){
				//If it's supposed to be a new record, then return an error:
				if(m_iNewRecord.compareToIgnoreCase("1") == 0){
					m_sErrorMessageArray.add("Cannot save - customer already exists.");
					rs.close();
					return false;
				}
				rs.close();
				
				m_sErrorMessageArray.clear();
				if(!validateEntries(conn)){
					return false;
				}
				
				String dStartDate = null;
						
					try {
						dStartDate =clsDateAndTimeConversions.utilDateToString(
							clsDateAndTimeConversions.StringTojavaSQLDate("MM/dd/yyyy", m_datStartDate),"yyyy-MM-dd");
					} catch (ParseException e1) {
						m_sErrorMessageArray.add("Error:[1423837814] Invalid Start date: '"
						+ m_datStartDate + "' - " + e1.getMessage());
					  return false;
					}
				
				//Update the record:
				SQL = "UPDATE " + SMTablearcustomer.TableName
						+ " SET " 
						+ SMTablearcustomer.datLastMaintained + " = NOW(), "
						+ SMTablearcustomer.datStartDate + " = '" + dStartDate + "', "
						+ SMTablearcustomer.dCreditLimit + " = " + m_dCreditLimit.replace(",", "") + ", "
						+ SMTablearcustomer.iActive + " = " + m_iActive + ", "
						+ SMTablearcustomer.iOnHold + " = " + m_iOnHold + ", "
						+ SMTablearcustomer.mCustomerComments + " = '" + clsDatabaseFunctions.FormatSQLStatement(m_mCustomerComments) + "', "
						+ SMTablearcustomer.mAccountingNotes + " = '" + clsDatabaseFunctions.FormatSQLStatement(m_mAccountingNotes) + "', "
						+ SMTablearcustomer.sAccountSet + " = '" + clsDatabaseFunctions.FormatSQLStatement(m_sAccountSet) + "', "
						+ SMTablearcustomer.sAddressLine1 + " = '" + clsDatabaseFunctions.FormatSQLStatement(m_sAddressLine1) + "', "
						+ SMTablearcustomer.sAddressLine2 + " = '" + clsDatabaseFunctions.FormatSQLStatement(m_sAddressLine2) + "', "
						+ SMTablearcustomer.sAddressLine3 + " = '" + clsDatabaseFunctions.FormatSQLStatement(m_sAddressLine3) + "', "
						+ SMTablearcustomer.sAddressLine4 + " = '" + clsDatabaseFunctions.FormatSQLStatement(m_sAddressLine4) + "', "
						+ SMTablearcustomer.sCity + " = '" + clsDatabaseFunctions.FormatSQLStatement(m_sCity) + "', "
						+ SMTablearcustomer.sContactName + " = '" + clsDatabaseFunctions.FormatSQLStatement(m_sContactName) + "', "
						+ SMTablearcustomer.sCountry + " = '" + clsDatabaseFunctions.FormatSQLStatement(m_sCountry) + "', "
						+ SMTablearcustomer.sCustomerName + " = '" + clsDatabaseFunctions.FormatSQLStatement(m_sCustomerName) + "', "
						+ SMTablearcustomer.sFaxNumber + " = '" + clsDatabaseFunctions.FormatSQLStatement(m_sFaxNumber) + "', "
						+ SMTablearcustomer.sLastEditUserFullName + " = '" + clsDatabaseFunctions.FormatSQLStatement(m_sLastEditUserFullName) + "', "
						+ SMTablearcustomer.lLastEditUserID + " = " + clsDatabaseFunctions.FormatSQLStatement(m_sLastEditUserID) + ", "
						+ SMTablearcustomer.sPhoneNumber + " = '" + clsDatabaseFunctions.FormatSQLStatement(m_sPhoneNumber) + "', "
						+ SMTablearcustomer.sPostalCode + " = '" + clsDatabaseFunctions.FormatSQLStatement(m_sPostalCode) + "', "
						+ SMTablearcustomer.sState + " = '" + clsDatabaseFunctions.FormatSQLStatement(m_sState) + "', "
						+ SMTablearcustomer.sTerms + " = '" + clsDatabaseFunctions.FormatSQLStatement(m_sTerms) + "', "
						+ SMTablearcustomer.sCustomerGroup + " = '" + clsDatabaseFunctions.FormatSQLStatement(m_sCustomerGroup) + "', "
						+ SMTablearcustomer.sEmailAddress + " = '" + clsDatabaseFunctions.FormatSQLStatement(m_sEmailAddress) + "', "
						+ SMTablearcustomer.sWebAddress + " = '" + clsDatabaseFunctions.FormatSQLStatement(m_sWebAddress) + "', "
						+ SMTablearcustomer.sPriceListCode + " = '" + clsDatabaseFunctions.FormatSQLStatement(m_sPriceListCode) + "', "
						+ SMTablearcustomer.ipricelevel + " = " + clsDatabaseFunctions.FormatSQLStatement(m_sPriceLevel) + ", "
						+ SMTablearcustomer.iuseselectronicdeposit + " = " + m_sUsesElectronicDeposit + ", "
						+ SMTablearcustomer.irequirespo + " = " + m_sRequiresPO + ", "
						+ SMTablearcustomer.irequiresstatements + " = " + m_sRequiresStatements + ", "
						+ SMTablearcustomer.sgdoclink + " = '" + clsDatabaseFunctions.FormatSQLStatement(m_sgdoclink) + "',"
						+ SMTablearcustomer.itaxid + " = " + m_itaxid + ","
						+ SMTablearcustomer.sinvoicingcontact + " = '" + clsDatabaseFunctions.FormatSQLStatement(m_sinvoicingcontact) + "',"
						+ SMTablearcustomer.sinvoicingemail + " = '" + clsDatabaseFunctions.FormatSQLStatement(m_sinvoicingemail) + "',"
						+ SMTablearcustomer.sinvoicingnotes + " = '" + clsDatabaseFunctions.FormatSQLStatement(m_sinvoicingnotes) + "'" 
						+ " WHERE (" 
							+ "(" + SMTablearcustomer.sCustomerNumber + " = '" + clsDatabaseFunctions.FormatSQLStatement(m_sCustomerNumber) + "')"
							+ ")";
					
				try{
					Statement stmt = conn.createStatement();
					stmt.execute(SQL);
				}catch(SQLException e){
					m_sErrorMessageArray.add("Cannot execute UPDATE sql - error: " + e.getMessage());
					return false;
				}
				
				try {
					updateDefaultSalespersons(conn);
				} catch (Exception e) {
					clsDatabaseFunctions.rollback_data_transaction(conn);
					m_sErrorMessageArray.add("Cannot update default salespersons - error: " + e.getMessage());
					return false;
				}
				
				//Now, re-read the customer to get the last maintained date and last edit user:
				SQL = "SELECT"
					+ " " + SMTablearcustomer.datLastMaintained
					+ ", " + SMTablearcustomer.sLastEditUserFullName
					+ ", " + SMTablearcustomer.lLastEditUserID
					+ " FROM " + SMTablearcustomer.TableName
					+ " WHERE ("
						+ "(" + SMTablearcustomer.sCustomerNumber + " = '" + this.getM_sCustomerNumber() + "')"
					+ ")"
				;
				rs = clsDatabaseFunctions.openResultSet(SQL, conn);
				if (!rs.next()){
					//Don't choke on this
					m_sErrorMessageArray.add("Could not read customer to get last maintained information.");
					rs.close();
				}else{
		        	if(clsDateAndTimeConversions.IsValidDate(rs.getDate(SMTablearcustomer.datLastMaintained))){
		        		m_datLastMaintained = clsDateAndTimeConversions.utilDateToString(rs.getDate(SMTablearcustomer.datLastMaintained),"MM/dd/yyyy");
		        	}else{
		        		m_datLastMaintained = clsDateAndTimeConversions.now("MM/dd/yyyy");
		        	}	        	
		        	m_sLastEditUserFullName = clsStringFunctions.checkStringForNull(rs.getString(SMTablearcustomer.sLastEditUserFullName));
		        	m_sLastEditUserID = clsStringFunctions.checkStringForNull(Long.toString(rs.getLong(SMTablearcustomer.lLastEditUserID)));
		        	rs.close();
				}
				m_iNewRecord = "0";
				return true;
			}else{
				//If it DOESN'T exist:
				//If it's supposed to be an existing record, then return an error:
				if(m_iNewRecord.compareToIgnoreCase("0") == 0){
					m_sErrorMessageArray.add("Cannot save - can't get existing customer.");
					rs.close();
					return false;
				}
				rs.close();
				//Insert the record:
				//First, validate the new customer code:
				if (!validateNewCode()){
					return false;
				}
				if(!validateEntries(conn)){
					return false;
				}
				if (!clsDatabaseFunctions.start_data_transaction(conn)){
					m_sErrorMessageArray.add("Could not start data transaction for customer insert.");
					return false;
				}
				
				String dStartDate = null;
				
				try {
					dStartDate =clsDateAndTimeConversions.utilDateToString(
						clsDateAndTimeConversions.StringTojavaSQLDate("MM/dd/yyyy", m_datStartDate),"yyyy-MM-dd");
				} catch (ParseException e1) {
					m_sErrorMessageArray.add("Error:[1423837816] Invalid Start date: '"
					+ m_datStartDate + "' - " + e1.getMessage());
				  return false;
				}
				SQL = "INSERT into " + SMTablearcustomer.TableName
						+ " (" 
						+ SMTablearcustomer.datLastMaintained
						+ ", " + SMTablearcustomer.sCustomerNumber
						+ ", " + SMTablearcustomer.datStartDate
						+ ", " + SMTablearcustomer.dCreditLimit
						+ ", " + SMTablearcustomer.iActive
						+ ", " + SMTablearcustomer.iOnHold
						+ ", " + SMTablearcustomer.mCustomerComments
						+ ", " + SMTablearcustomer.mAccountingNotes
						+ ", " + SMTablearcustomer.sAccountSet
						+ ", " + SMTablearcustomer.sAddressLine1
						+ ", " + SMTablearcustomer.sAddressLine2
						+ ", " + SMTablearcustomer.sAddressLine3
						+ ", " + SMTablearcustomer.sAddressLine4
						+ ", " + SMTablearcustomer.sCity
						+ ", " + SMTablearcustomer.sContactName
						+ ", " + SMTablearcustomer.sCountry
						+ ", " + SMTablearcustomer.sCustomerName
						+ ", " + SMTablearcustomer.sFaxNumber
						+ ", " + SMTablearcustomer.sLastEditUserFullName
						+ ", " + SMTablearcustomer.lLastEditUserID
						+ ", " + SMTablearcustomer.sPhoneNumber
						+ ", " + SMTablearcustomer.sPostalCode
						+ ", " + SMTablearcustomer.sState
						+ ", " + SMTablearcustomer.sTerms
						+ ", " + SMTablearcustomer.sCustomerGroup
						+ ", " + SMTablearcustomer.sEmailAddress
						+ ", " + SMTablearcustomer.sWebAddress
						+ ", " + SMTablearcustomer.sPriceListCode
						+ ", " + SMTablearcustomer.ipricelevel
						+ ", " + SMTablearcustomer.iuseselectronicdeposit
						+ ", " + SMTablearcustomer.irequirespo
						+ ", " + SMTablearcustomer.irequiresstatements
						+ ", " + SMTablearcustomer.sgdoclink
						+ ", " + SMTablearcustomer.itaxid
						+ ", " + SMTablearcustomer.sinvoicingcontact
						+ ", " + SMTablearcustomer.sinvoicingemail
						+ ", " + SMTablearcustomer.sinvoicingnotes
					+")"
					+ " VALUES (" 
						+ "NOW()"
						+ ", '" + clsDatabaseFunctions.FormatSQLStatement(m_sCustomerNumber) + "'"
						+ ", '" + dStartDate + "'"
						+ ", " + m_dCreditLimit.replace(",", "")
						+ ", " + m_iActive
						+ ", " + m_iOnHold
						+ ", '" + clsDatabaseFunctions.FormatSQLStatement(m_mCustomerComments) + "'"
						+ ", '" + clsDatabaseFunctions.FormatSQLStatement(m_mAccountingNotes) + "'"
						+ ", '" + clsDatabaseFunctions.FormatSQLStatement(m_sAccountSet) + "'"
						+ ", '" + clsDatabaseFunctions.FormatSQLStatement(m_sAddressLine1) + "'"
						+ ", '" + clsDatabaseFunctions.FormatSQLStatement(m_sAddressLine2) + "'"
						+ ", '" + clsDatabaseFunctions.FormatSQLStatement(m_sAddressLine3) + "'"
						+ ", '" + clsDatabaseFunctions.FormatSQLStatement(m_sAddressLine4) + "'"
						+ ", '" + clsDatabaseFunctions.FormatSQLStatement(m_sCity) + "'"
						+ ", '" + clsDatabaseFunctions.FormatSQLStatement(m_sContactName) + "'"
						+ ", '" + clsDatabaseFunctions.FormatSQLStatement(m_sCountry) + "'"
						+ ", '" + clsDatabaseFunctions.FormatSQLStatement(m_sCustomerName) + "'"
						+ ", '" + clsDatabaseFunctions.FormatSQLStatement(m_sFaxNumber) + "'"
						+ ", '" + clsDatabaseFunctions.FormatSQLStatement(m_sLastEditUserFullName) + "'"
						+ ", " + clsDatabaseFunctions.FormatSQLStatement(m_sLastEditUserID) + ""
						+ ", '" + clsDatabaseFunctions.FormatSQLStatement(m_sPhoneNumber) + "'"
						+ ", '" + clsDatabaseFunctions.FormatSQLStatement(m_sPostalCode) + "'"
						+ ", '" + clsDatabaseFunctions.FormatSQLStatement(m_sState) + "'"
						+ ", '" + clsDatabaseFunctions.FormatSQLStatement(m_sTerms) + "'"
						+ ", '" + clsDatabaseFunctions.FormatSQLStatement(m_sCustomerGroup) + "'"
						+ ", '" + clsDatabaseFunctions.FormatSQLStatement(m_sEmailAddress) + "'"
						+ ", '" + clsDatabaseFunctions.FormatSQLStatement(m_sWebAddress) + "'"
						+ ", '" + clsDatabaseFunctions.FormatSQLStatement(m_sPriceListCode) + "'"
						+ ", " + clsDatabaseFunctions.FormatSQLStatement(m_sPriceLevel)
						//+ ", '" + sTaxJurisdiction + "'"
						//+ ", " + sTaxType
						+ ", " + m_sUsesElectronicDeposit
						+ ", " + m_sRequiresPO
						+ ", " + m_sRequiresStatements
						+ ", '" + clsDatabaseFunctions.FormatSQLStatement(m_sgdoclink) + "'"
						+ ", " + m_itaxid
						+ ", '" + clsDatabaseFunctions.FormatSQLStatement(m_sinvoicingcontact) + "'"
						+ ", '" + clsDatabaseFunctions.FormatSQLStatement(m_sinvoicingemail) + "'"
						+ ", '" + clsDatabaseFunctions.FormatSQLStatement(m_sinvoicingnotes) + "'"
					+ ")"
					;

				try{
					Statement stmt = conn.createStatement();
					stmt.execute(SQL);
				}catch(SQLException e){
					clsDatabaseFunctions.rollback_data_transaction(conn);
					m_sErrorMessageArray.add("Cannot execute INSERT sql - error: " + e.getMessage());
					return false;
				}

				//Add an initial customer statistics record:
				SQL = "INSERT INTO " + SMTablearcustomerstatistics.TableName
					+ " (" + SMTablearcustomerstatistics.sCustomerNumber + ")"
					+ " VALUES ('" + clsDatabaseFunctions.FormatSQLStatement(m_sCustomerNumber) + "')"
					;

				try{
					Statement stmt = conn.createStatement();
					stmt.execute(SQL);
				}catch(SQLException e){
					clsDatabaseFunctions.rollback_data_transaction(conn);
					m_sErrorMessageArray.add("Cannot execute customer statistics INSERT sql - error: " + e.getMessage());
					return false;
				}

				try {
					updateDefaultSalespersons(conn);
				} catch (Exception e) {
					clsDatabaseFunctions.rollback_data_transaction(conn);
					m_sErrorMessageArray.add("Cannot update default salespersons - error: " + e.getMessage());
					return false;
				}
				
				if (!clsDatabaseFunctions.commit_data_transaction(conn)){
					clsDatabaseFunctions.rollback_data_transaction(conn);
					m_sErrorMessageArray.add("Cannot commit data transaction.");
					return false;
				}
				
				//If we get to here, we've succeeded:
				m_iNewRecord = "0";
				mailNewCustomerNotification(sUserID, sUserFullName, sCompany, conn);
			}
			//Now, re-read the customer to get the last maintained date and last edit user:
			SQL = "SELECT"
				+ " " + SMTablearcustomer.datLastMaintained
				+ ", " + SMTablearcustomer.sLastEditUserFullName
				+ ", " + SMTablearcustomer.lLastEditUserID
				+ " FROM " + SMTablearcustomer.TableName
				+ " WHERE ("
					+ "(" + SMTablearcustomer.sCustomerNumber + " = '" + this.getM_sCustomerNumber() + "')"
				+ ")"
			;
			rs = clsDatabaseFunctions.openResultSet(SQL, conn);
			if (!rs.next()){
				//Don't choke on this
				m_sErrorMessageArray.add("Could not read customer to get last maintained information.");
				rs.close();
			}else{
	        	if(clsDateAndTimeConversions.IsValidDate(rs.getDate(SMTablearcustomer.datLastMaintained))){
	        		m_datLastMaintained = clsDateAndTimeConversions.utilDateToString(rs.getDate(SMTablearcustomer.datLastMaintained),"MM/dd/yyyy");
	        	}else{
	        		m_datLastMaintained = clsDateAndTimeConversions.now("MM/dd/yyyy");
	        	}	        	
	        	m_sLastEditUserFullName = clsStringFunctions.checkStringForNull(rs.getString(SMTablearcustomer.sLastEditUserFullName));
	        	m_sLastEditUserID = clsStringFunctions.checkStringForNull(Long.toString(rs.getLong(SMTablearcustomer.lLastEditUserID)));
	        	rs.close();
			}

		}catch(SQLException e){
			m_sErrorMessageArray.add("Error saving customer - " + e.getMessage());
			return false;
		}
		
		return true;
	}
	private void updateDefaultSalespersons(Connection conn) throws SQLException{
		//Save the default salespersons:
		String SQL = "";
		for (int i = 0; i < m_sSalesGroupIDArray.size(); i++){
			SQL = "INSERT INTO " + SMTabledefaultsalesgroupsalesperson.TableName + "("
				+ SMTabledefaultsalesgroupsalesperson.scustomercode
				+ ", " + SMTabledefaultsalesgroupsalesperson.lsalesgroupid
				+ ", " + SMTabledefaultsalesgroupsalesperson.ssalespersoncode
				+ ") VALUES ("
				+ "'" + getM_sCustomerNumber() + "'"
				+ ", " + m_sSalesGroupIDArray.get(i)
				+ ", '" + m_sSalesGroupDefaultSalespersonArray.get(i) + "'"
				+ ") ON DUPLICATE KEY UPDATE "
				+ SMTabledefaultsalesgroupsalesperson.ssalespersoncode 
				+ " = '" + m_sSalesGroupDefaultSalespersonArray.get(i) + "'"
			;
			Statement stm = conn.createStatement();
			stm.execute(SQL);
		}
	}
	private boolean mailNewCustomerNotification(String sUserID, String sUserFullName, String sCompany, Connection conn){
		//Notify any specified users that a new customer was built:
		String SQL = "SELECT " + SMTableusers.semail 
			+ " FROM " 
				+ SMTableusers.TableName + ", "
				+ SMTablesecuritygroupfunctions.TableName + ", "
				+ SMTablesecurityusergroups.TableName
			+ " WHERE ("
			//TODO:
				+ "(" + SMTablesecuritygroupfunctions.TableName + "." + SMTablesecuritygroupfunctions.ifunctionid
					+ " = " + SMSystemFunctions.ARReceiveNewCustomerNotification + ")"
				+ " AND (" + SMTablesecuritygroupfunctions.TableName + "." + SMTablesecuritygroupfunctions.sGroupName
					+ " = " + SMTablesecurityusergroups.TableName + "." + SMTablesecurityusergroups.sSecurityGroupName + ")"
				+ " AND (" + SMTableusers.TableName + "." + SMTableusers.lid + " = "
					+ SMTablesecurityusergroups.TableName + "." + SMTablesecurityusergroups.luserid + ")"
				+ " AND (" + SMTableusers.TableName + "." + SMTableusers.iactive + "=" + "1" + ")"
			+ ")"
			;
		String sCurrentTime = "";
		String sSMTPServer = "";
		String sSMTPPort = "";
		String sSMTPSourceServerName = "";
		String sSMTPUserName = "";
		String sSMTPPassword = "";
		String sReplyToAddress = "";
		boolean bUsesSMTPAuthentication = false;
		ArrayList<String>arrNotifyEmails = new ArrayList<String>(0);
		try{
			ResultSet rsNotify = clsDatabaseFunctions.openResultSet(SQL, conn);
			while(rsNotify.next()){
				arrNotifyEmails.add(rsNotify.getString(SMTableusers.semail));
			}
			rsNotify.close();
			//If there is no one to notify, just exit out:
			if(arrNotifyEmails.size() == 0){
				return true;
			}
			
			//Now we need to send an email to notify that a new customer was added:

			SQL = "SELECT " + SMTablesmoptions.TableName + ".*"
			+ ", DATE_FORMAT(NOW(),'%c/%e/%Y %h:%i:%s %p')"
				+ " AS CURRENTTIME FROM " 
				+ SMTablesmoptions.TableName;
			ResultSet rsOptions = clsDatabaseFunctions.openResultSet(SQL, conn);
			if(rsOptions.next()){
				sCurrentTime = rsOptions.getString("CURRENTTIME");
				sSMTPServer = rsOptions.getString(SMTablesmoptions.ssmtpserver).trim();
				sSMTPPort = rsOptions.getString(SMTablesmoptions.ssmtpport).trim();
				sSMTPSourceServerName = rsOptions.getString(SMTablesmoptions.ssmtpsourceservername).trim();
				sSMTPUserName = rsOptions.getString(SMTablesmoptions.ssmtpusername).trim();
				sSMTPPassword = rsOptions.getString(SMTablesmoptions.ssmtppassword).trim();
				sReplyToAddress = rsOptions.getString(SMTablesmoptions.ssmtpreplytoname).trim();
				bUsesSMTPAuthentication = (rsOptions.getInt(SMTablesmoptions.iusesauthentication) == 1);
				rsOptions.close();
			}else{
				rsOptions.close();
				m_sErrorMessageArray.add("Could not get record with email info.");
				return false;
			}
		}catch(SQLException e){
			m_sErrorMessageArray.add("Error getting email server information - " + e.getMessage() + ".");
			return false;
		}

		//Get the tax description:
		SMTax tax = new SMTax();
		tax.set_slid(getstaxid());
		try {
			tax.load(conn);
		} catch (Exception e2) {
			m_sErrorMessageArray.add("Error [1454082502] getting tax information - " + e2.getMessage() + ".");
			return false;
		}
		
		//Now construct the email:
		String sActive = "YES";
		if (getM_iActive().compareToIgnoreCase("0") == 0){
			sActive = "NO";
		}
		String sRequiresStatement = "YES";
		if (getM_sRequiresStatements().compareToIgnoreCase("0") == 0){
			sRequiresStatement = "NO";
		}
		String sRequiresPO = "YES";
		if (getM_sRequiresPO().compareToIgnoreCase("0") == 0){
			sRequiresPO = "NO";
		}
		String sBody = "Customer '" + getM_sCustomerNumber() + "' was added " + sCurrentTime
			+ " by " + sUserFullName
			+ " in company " + sCompany
			+ "\n\n"
			+ "NAME: " + getM_sCustomerName() + "\n"
			+ "ADDRESS 1: " + getM_sAddressLine1() + "\n"
			+ "ADDRESS 2: " + getM_sAddressLine2() + "\n"
			+ "ADDRESS 3: " + getM_sAddressLine3() + "\n"
			+ "ADDRESS 4: " + getM_sAddressLine4() + "\n"
			+ "CITY: " + getM_sCity() + "\n"
			+ "STATE: " + getM_sState() + "\n"
			+ "POSTAL CODE: " + getM_sPostalCode() + "\n"
			+ "COUNTRY: " + getM_sCountry() + "\n"
			+ "CONTACT NAME: " + getM_sContactName() + "\n"
			+ "CREDIT LIMIT: " + getM_dCreditLimit() + "\n"
			+ "TERMS: " + getM_sTerms() + "\n"
			+ "TAX: " + tax.get_staxjurisdiction() + " - " + tax.get_staxtype() + "\n"
			+ "CUSTOMER ACTIVE?: " + sActive + "\n"
			+ "REQUIRES STATEMENTS?: " + sRequiresStatement + "\n"
			+ "REQUIRES PO?: " + sRequiresPO + "\n"
			+ "GOOGLE FOLDER LINK: " + getsgdoclink() + "\n"
			;

			int iSMTPPort;
			try {
				iSMTPPort = Integer.parseInt(sSMTPPort);
			} catch (NumberFormatException e) {
				m_sErrorMessageArray.add("Invalid email port - " + sSMTPPort + ".");
				return false;
			}
			
			try {
				SMUtilities.sendEmail(
						sSMTPServer, 
						sSMTPUserName, 
						sSMTPPassword, 
						sReplyToAddress,
						Integer.toString(iSMTPPort), 
						"New Customer " + getM_sCustomerNumber(),
						sBody, 
						"SMAR@" + sSMTPSourceServerName,
						sSMTPSourceServerName, 
						arrNotifyEmails, 
						bUsesSMTPAuthentication,
						false);
			} catch (Exception e1) {
				m_sErrorMessageArray.add("Error sending email - " + e1.getMessage() + ".");
				return false;
			}
			return true;
	}
	private boolean validateNewCode(){
		m_sErrorMessageArray.clear();
		//All upper case:
		m_sCustomerNumber = m_sCustomerNumber.toUpperCase();
		
		if(!clsStringFunctions.validateStringCharacters(m_sCustomerNumber, "ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890-")){
			m_sErrorMessageArray.add("Invalid characters in customer code");
			return false;
		}
		return true; 
		
	}
	private boolean validateEntries(Connection conn){
		
		boolean bEntriesAreValid = true;
		m_sErrorMessageArray.clear();
    	if (m_sCustomerNumber.trim().compareToIgnoreCase("") == 0){
    		m_sErrorMessageArray.add("customer number cannot be blank");
    		bEntriesAreValid = false;
    	}
    	if (m_sCustomerNumber.length() > SMTablearcustomer.sCustomerNumberLength){
    		m_sErrorMessageArray.add("customer number cannot be longer than " 
    			+ SMTablearcustomer.sCustomerNumberLength + " characters.");
    		bEntriesAreValid = false;
    	}
    	
    	if (m_sCustomerName.trim().compareToIgnoreCase("") == 0){
    		m_sErrorMessageArray.add("customer name cannot be blank");
    		bEntriesAreValid = false;
    	}
    	if (m_sCustomerName.length() > SMTablearcustomer.sCustomerNameLength){
    		m_sErrorMessageArray.add("customer name cannot be longer than " 
    			+ SMTablearcustomer.sCustomerNameLength + " characters.");
    		bEntriesAreValid = false;
    	}
    	
    	//if (m_sAddressLine1.trim().compareToIgnoreCase("") == 0){
    	//	m_sErrorMessageArray.add("first address line cannot be blank");
    	//	bEntriesAreValid = false;
    	//}
    	if (m_sAddressLine1.length() > SMTablearcustomer.sAddressLine1Length){
    		m_sErrorMessageArray.add("address line 1 cannot be longer than " 
    			+ SMTablearcustomer.sAddressLine1Length + " characters.");
    		bEntriesAreValid = false;
    	}
    	if (m_sAddressLine2.length() > SMTablearcustomer.sAddressLine2Length){
    		m_sErrorMessageArray.add("address line 2 cannot be longer than " 
    			+ SMTablearcustomer.sAddressLine2Length + " characters.");
    		bEntriesAreValid = false;
    	}
    	if (m_sAddressLine3.length() > SMTablearcustomer.sAddressLine3Length){
    		m_sErrorMessageArray.add("address line 3 cannot be longer than " 
    			+ SMTablearcustomer.sAddressLine3Length + " characters.");
    		bEntriesAreValid = false;
    	}
    	if (m_sAddressLine4.length() > SMTablearcustomer.sAddressLine4Length){
    		m_sErrorMessageArray.add("address line 4 cannot be longer than " 
    			+ SMTablearcustomer.sAddressLine4Length + " characters.");
    		bEntriesAreValid = false;
    	}
    	
    	//if (m_sCity.trim().compareToIgnoreCase("") == 0){
    	//	m_sErrorMessageArray.add("city cannot be blank");
    	//	bEntriesAreValid = false;
    	//}
    	if (m_sCity.length() > SMTablearcustomer.sCityLength){
    		m_sErrorMessageArray.add("city cannot be longer than " 
    			+ SMTablearcustomer.sCityLength + " characters.");
    		bEntriesAreValid = false;
    	}
    	
    	//if (m_sState.trim().compareToIgnoreCase("") == 0){
    	//	m_sErrorMessageArray.add("state cannot be blank");
    	//	bEntriesAreValid = false;
    	//}
    	if (m_sState.length() > SMTablearcustomer.sStateLength){
    		m_sErrorMessageArray.add("State cannot be longer than " 
    			+ SMTablearcustomer.sStateLength + " characters.");
    		bEntriesAreValid = false;
    	}
    	if (m_sCountry.length() > SMTablearcustomer.sCountryLength){
    		m_sErrorMessageArray.add("State cannot be longer than " 
    			+ SMTablearcustomer.sCountryLength + " characters.");
    		bEntriesAreValid = false;
    	}
    	
    	//if (m_sPostalCode.trim().compareToIgnoreCase("") == 0){
    	//	m_sErrorMessageArray.add("postal code cannot be blank");
    	//	bEntriesAreValid = false;
    	//}
    	if (m_sPostalCode.length() > SMTablearcustomer.sPostalCodeLength){
    		m_sErrorMessageArray.add("Postal Code cannot be longer than " 
    			+ SMTablearcustomer.sPostalCodeLength + " characters.");
    		bEntriesAreValid = false;
    	}
    	if (m_sContactName.length() > SMTablearcustomer.sContactNameLength){
    		m_sErrorMessageArray.add("Contact Name cannot be longer than " 
    			+ SMTablearcustomer.sContactNameLength + " characters.");
    		bEntriesAreValid = false;
    	}
    	
    	if (m_sPhoneNumber.trim().compareToIgnoreCase("") == 0){
    		m_sErrorMessageArray.add("phone number cannot be blank");
    		bEntriesAreValid = false;
    	}
    	if (m_sPhoneNumber.length() > SMTablearcustomer.sPhoneNumberLength){
    		m_sErrorMessageArray.add("Phone Number cannot be longer than " 
    			+ SMTablearcustomer.sPhoneNumberLength + " characters.");
    		bEntriesAreValid = false;
    	}
    	if (m_sFaxNumber.length() > SMTablearcustomer.sFaxNumberLength){
    		m_sErrorMessageArray.add("Fax Number cannot be longer than " 
    			+ SMTablearcustomer.sFaxNumberLength + " characters.");
    		bEntriesAreValid = false;
    	}
    	
    	if (m_sTerms.trim().compareToIgnoreCase("") == 0){
    		m_sErrorMessageArray.add("you must select a terms code");
    		bEntriesAreValid = false;
    	}
    	if (m_sTerms.length() > SMTablearcustomer.sTermsLength){
    		m_sErrorMessageArray.add("Terms cannot be longer than " 
    			+ SMTablearcustomer.sTermsLength + " characters.");
    		bEntriesAreValid = false;
    	}
    	
    	if (m_sAccountSet.trim().compareToIgnoreCase("") == 0){
    		m_sErrorMessageArray.add("you must select an account set");
    		bEntriesAreValid = false;
    	}
    	if (m_sAccountSet.length() > SMTablearcustomer.sAccountSetLength){
    		m_sErrorMessageArray.add("Account Set cannot be longer than " 
    			+ SMTablearcustomer.sAccountSetLength + " characters.");
    		bEntriesAreValid = false;
    	}
    	
    	if (m_sLastEditUserFullName.length() > SMTablearcustomer.sLastEditUserFullNameLength){
    		m_sErrorMessageArray.add("Last Edit User cannot be longer than " 
    			+ SMTablearcustomer.sLastEditUserFullNameLength + " characters.");
    		bEntriesAreValid = false;
    	}
    	
    	if (m_sCustomerGroup.trim().compareToIgnoreCase("") == 0){
    		m_sErrorMessageArray.add("you must select a customer group");
    		bEntriesAreValid = false;
    	}
    	if (m_sCustomerGroup.length() > SMTablearcustomer.sCustomerGroupLength){
    		m_sErrorMessageArray.add("Customer Group cannot be longer than " 
    			+ SMTablearcustomer.sCustomerGroupLength + " characters.");
    		bEntriesAreValid = false;
    	}
    	
    	//if (m_sPriceListCode.trim().compareToIgnoreCase("") == 0){
    	//	m_sErrorMessageArray.add("you must select a price list code");
    	//	bEntriesAreValid = false;
    	//}
    	
    	//if (m_sPriceLevel.trim().compareToIgnoreCase("") == 0){
    	//	m_sErrorMessageArray.add("you must select a price level");
    	//	bEntriesAreValid = false;
    	//}
    	
    	//if (m_sTaxJurisdiction.trim().compareToIgnoreCase("") == 0){
    	//	m_sErrorMessageArray.add("you must select a tax jurisdiction");
    	//	bEntriesAreValid = false;
    	//}
    	
    	//if (m_sTaxType.trim().compareToIgnoreCase("") == 0){
    	//	m_sErrorMessageArray.add("you must select a tax type");
    	//	bEntriesAreValid = false;
    	//}
    	//Validate the combination of tax jurisdiction and tax type:
    	String sSQL = "SELECT"
    		+ " " + SMTabletax.iactive
    		+ " FROM " + SMTabletax.TableName
    		+ " WHERE ("
    			+ "(" + SMTabletax.lid + " = " + m_itaxid + ")"
    		+ ")"
    	;
    	try {
			ResultSet rs = clsDatabaseFunctions.openResultSet(sSQL, conn);
			if (!rs.next()){
				m_sErrorMessageArray.add("cannot find ACTIVE tax ID '" + m_itaxid + "'." );
				bEntriesAreValid = false;
			}
			rs.close();
		} catch (SQLException e) {
			m_sErrorMessageArray.add(
				"error loading tax ID '" + m_itaxid + "' - " + e.getMessage() + ".");
			bEntriesAreValid = false;
		}
    	
    	if (m_sEmailAddress.length() > SMTablearcustomer.sEmailAddressLength){
    		m_sErrorMessageArray.add("Email Address cannot be longer than " 
    			+ SMTablearcustomer.sEmailAddressLength + " characters.");
    		bEntriesAreValid = false;
    	}
    	if (m_sWebAddress.length() > SMTablearcustomer.sWebAddressLength){
    		m_sErrorMessageArray.add("Web Address cannot be longer than " 
    			+ SMTablearcustomer.sWebAddressLength + " characters.");
    		bEntriesAreValid = false;
    	}
    	if(!clsDateAndTimeConversions.IsValidDateString("MM/dd/yyyy", m_datStartDate)){
    		m_sErrorMessageArray.add("Invalid start date: " + m_datStartDate); 
        		bEntriesAreValid = false;
    	}
    	if(!clsDateAndTimeConversions.IsValidDateString("MM/dd/yyyy", m_datLastMaintained)){
    		m_sErrorMessageArray.add("Invalid last maintained date: " + m_datLastMaintained); 
        		bEntriesAreValid = false;
    	}
    	if(!clsValidateFormFields.IsValidBigDecimal(m_dCreditLimit, 2)){
    		m_sErrorMessageArray.add("Invalid credit limit: " + m_dCreditLimit); 
    		bEntriesAreValid = false;
    	}else{
	    	//Convert the format to a standard format:
	   		BigDecimal bd = new BigDecimal(m_dCreditLimit.replace(",", ""));
	   		bd = bd.setScale(2, BigDecimal.ROUND_HALF_UP);
	    	m_dCreditLimit = clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(bd);
    	}
    	
    	if (m_sinvoicingcontact.length() > SMTablearcustomer.sInvoicingContactLength){
    		m_sErrorMessageArray.add("Invoicing Contact cannot be longer than " 
    			+ SMTablearcustomer.sInvoicingContactLength + " characters.");
    		bEntriesAreValid = false;
    	}
    	
    	if (m_sinvoicingemail.length() > SMTablearcustomer.sInvoicingEmailLength){
    		m_sErrorMessageArray.add("Invoicing Email cannot be longer than " 
    			+ SMTablearcustomer.sInvoicingEmailLength + " characters.");
    		bEntriesAreValid = false;
    	}
    	return bEntriesAreValid;
	}
	public String getQueryString(){
		
		String sQueryString = "";
		sQueryString += ParamsAddingNewRecord + "=" + ARUtilities.URLEncode(m_iNewRecord);
		sQueryString += "&" + ParamsCustomerNumber + "=" + ARUtilities.URLEncode(m_sCustomerNumber);
		sQueryString += "&" + ParamsCustomerName + "=" + ARUtilities.URLEncode(m_sCustomerName);
		sQueryString += "&" + ParamsAddressLine1 + "=" + ARUtilities.URLEncode(m_sAddressLine1);
		sQueryString += "&" + ParamsAddressLine2 + "=" + ARUtilities.URLEncode(m_sAddressLine2);
		sQueryString += "&" + ParamsAddressLine3 + "=" + ARUtilities.URLEncode(m_sAddressLine3);
		sQueryString += "&" + ParamsAddressLine4 + "=" + ARUtilities.URLEncode(m_sAddressLine4);
		sQueryString += "&" + ParamsCity + "=" + ARUtilities.URLEncode(m_sCity);
		sQueryString += "&" + ParamsState + "=" + ARUtilities.URLEncode(m_sState);
		sQueryString += "&" + ParamsCountry + "=" + ARUtilities.URLEncode(m_sCountry);
		sQueryString += "&" + ParamsPostalCode + "=" + ARUtilities.URLEncode(m_sPostalCode);
		sQueryString += "&" + ParamsContactName + "=" + ARUtilities.URLEncode(m_sContactName);
		sQueryString += "&" + ParamsPhoneNumber + "=" + ARUtilities.URLEncode(m_sPhoneNumber);
		sQueryString += "&" + ParamsFaxNumber + "=" + ARUtilities.URLEncode(m_sFaxNumber);
		sQueryString += "&" + ParamsTerms + "=" + ARUtilities.URLEncode(m_sTerms);
		sQueryString += "&" + ParamsAccountSet + "=" + ARUtilities.URLEncode(m_sAccountSet);
		sQueryString += "&" + ParamiOnHold + "=" + ARUtilities.URLEncode(m_iOnHold);
		sQueryString += "&" + ParamdatStartDate + "=" + ARUtilities.URLEncode(m_datStartDate);
		sQueryString += "&" + ParamdCreditLimit + "=" + ARUtilities.URLEncode(m_dCreditLimit);
		sQueryString += "&" + ParammAccountingNotes + "=" + ARUtilities.URLEncode(m_mAccountingNotes);
		sQueryString += "&" + ParammCustomerComments + "=" + ARUtilities.URLEncode(m_mCustomerComments);
		sQueryString += "&" + ParamiActive + "=" + ARUtilities.URLEncode(m_iActive);
		sQueryString += "&" + ParamdatLastMaintained + "=" + ARUtilities.URLEncode(m_datLastMaintained);
		sQueryString += "&" + ParamsLastEditUserFullName + "=" + ARUtilities.URLEncode(m_sLastEditUserFullName);
		sQueryString += "&" + ParamsLastEditUserID + "=" + ARUtilities.URLEncode(m_sLastEditUserID);
		sQueryString += "&" + ParamsCustomerGroup + "=" + ARUtilities.URLEncode(m_sCustomerGroup);
		sQueryString += "&" + ParamsEmailAddress + "=" + ARUtilities.URLEncode(m_sEmailAddress);
		sQueryString += "&" + ParamsWebAddress + "=" + ARUtilities.URLEncode(m_sWebAddress);
		sQueryString += "&" + Paramspricelistcode + "=" + ARUtilities.URLEncode(m_sPriceListCode);
		sQueryString += "&" + Paramspricelevel + "=" + ARUtilities.URLEncode(m_sPriceLevel);
		sQueryString += "&" + Paramiuseselectronicdeposit + "=" + ARUtilities.URLEncode(m_sUsesElectronicDeposit);
		sQueryString += "&" + Paramirequiresstatements + "=" + ARUtilities.URLEncode(m_sRequiresStatements);
		sQueryString += "&" + Paramirequirespo + "=" + ARUtilities.URLEncode(m_sRequiresPO);
		sQueryString += "&" + Paramsgdoclink + "=" + ARUtilities.URLEncode(m_sgdoclink);
		sQueryString += "&" + Paramitaxid + "=" + ARUtilities.URLEncode(m_itaxid);
		sQueryString += "&" + Paramsinvoicingcontact + "=" + ARUtilities.URLEncode(m_sinvoicingcontact);
		sQueryString += "&" + Paramsinvoicingemail + "=" + ARUtilities.URLEncode(m_sinvoicingemail);
		sQueryString += "&" + Paramsinvoicingnotes + "=" + ARUtilities.URLEncode(m_sinvoicingnotes);
				
		return sQueryString;
	}
	//Requires connection since it is used as part of a data transaction in places:
	public boolean delete(String sCustomerCode, Connection conn){
		
		m_sErrorMessageArray.clear();
		
		//First, check that the customer exists:
		String SQL = "SELECT * FROM " + SMTablearcustomer.TableName + 
				" WHERE (" + 
				"(" + SMTablearcustomer.sCustomerNumber + " = '" + sCustomerCode + "')" +
			")";
		
		try{
			ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
			if(!rs.next()){
				m_sErrorMessageArray.add("Customer " + sCustomerCode + " cannot be found.");
				rs.close();
				return false;
			}else{
				rs.close();
			}
			
		}catch(SQLException e){
			m_sErrorMessageArray.add("Error checking customer " + sCustomerCode + " to delete - " + e.getMessage());
			return false;
		}
		
		SQL = "SELECT " + SMTableentries.lid 
				+ " FROM " + SMTableentries.TableName + ", " + SMEntryBatch.TableName
				+ " WHERE ("
					+ "(" + SMTableentries.spayeepayor + " = '" + sCustomerCode + "')"
					+ " AND (" + SMTableentries.TableName + "." + SMTableentries.ibatchnumber + " = " 
						+ SMEntryBatch.TableName + "." + SMEntryBatch.ibatchnumber + ")"
					+ " AND (" + SMEntryBatch.smoduletype + " = '" + SMModuleTypes.AR + "')"
					+ " AND (" + SMEntryBatch.ibatchstatus + " != " + SMBatchStatuses.DELETED + ")"
					+ " AND (" + SMEntryBatch.ibatchstatus + " != " + SMBatchStatuses.POSTED + ")"
				+ ")"
				;
		try{
			ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
			if(rs.next()){
				m_sErrorMessageArray.add("Customer " + sCustomerCode + " has unposted batch entries.");
				rs.close();
				return false;
			}else{
				rs.close();
			}
		}catch(SQLException e){
			m_sErrorMessageArray.add("Error checking unposted batch entries for " + sCustomerCode + " - " + e.getMessage());
			return false;
		}
		
		SQL = "SELECT *" 
				+ " FROM " + SMTableartransactions.TableName
				+ " WHERE ("
					+ "(" + SMTableartransactions.spayeepayor + " = '" + sCustomerCode + "')";
					
					if (true){
						SQL += " AND (" + SMTableartransactions.dcurrentamt + " != 0.00)";
					}
				SQL += ")"
				+ " ORDER BY " 
				+ SMTableartransactions.datdocdate + ", "
				+ SMTableartransactions.sdocnumber; 
				
		try{
			ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
			if(rs.next()){
				m_sErrorMessageArray.add("Customer " + sCustomerCode + " has open balances.");
				rs.close();
				return false;
			}else{
				rs.close();
			}
		}catch(SQLException e){
			m_sErrorMessageArray.add("Error checking open balances for " + sCustomerCode + " - " + e.getMessage());
			return false;
		}
		
		//No open orders in SM
		SQL = "SELECT " 
				+ SMTableorderheaders.sOrderNumber
				+ " FROM " + SMTableorderheaders.TableName + ", " + SMTableorderdetails.TableName
				+ " WHERE ("
					+ "(" + SMTableorderdetails.TableName + ".dUniqueOrderID = " 
						+ SMTableorderheaders.TableName + "." + SMTableorderheaders.dOrderUniqueifier + ")"
					
					+ " AND (" + SMTableorderheaders.sCustomerCode + " = '" + sCustomerCode + "')"
					
					+ " AND (" + SMTableorderdetails.TableName + ".dQtyOrdered != 0.00)"
					
					+ " AND ("
						+ "(" + SMTableorderheaders.datOrderCanceledDate + " IS NULL)"
						+ " OR (" + SMTableorderheaders.datOrderCanceledDate + " < '1900-01-01')"
					+ ")"
					
				+ ")"
				;
		
		try{
			ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
			if(rs.next()){
				m_sErrorMessageArray.add("Customer " + sCustomerCode + " has open orders.");
				rs.close();
				return false;
			}else{
				rs.close();
			}
		}catch(SQLException e){
			m_sErrorMessageArray.add("Error checking open orders for " + sCustomerCode + " - " + e.getMessage());
			return false;
		}
		
		//No unexported invoices in SM
		SQL = "SELECT " 
				+ SMTableinvoiceheaders.sInvoiceNumber
				+ " FROM " + SMTableinvoiceheaders.TableName
				+ " WHERE ("
					+ "(" + SMTableinvoiceheaders.sCustomerCode + " = '" + sCustomerCode + "')"
					+ " AND (" + SMTableinvoiceheaders.iExportedToAR + " != 1)"
					
				+ ")"
				;
		
		try{
			ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
			if(rs.next()){
				m_sErrorMessageArray.add("Customer " + sCustomerCode + " has invoices that have not been exported.");
				rs.close();
				return false;
			}else{
				rs.close();
			}
		}catch(SQLException e){
			m_sErrorMessageArray.add("Error checking unexported invoices for " + sCustomerCode + " - " + e.getMessage());
			return false;
		}
		
		//Now begin a data transaction that we can roll back:
		if(!clsDatabaseFunctions.start_data_transaction(conn)){
			m_sErrorMessageArray.add("Error beginning data transaction to delete customer " + sCustomerCode + "");
			return false;
		}
		
		//Delete Customer
		try{
			SQL =  "DELETE FROM " +
					SMTablearcustomer.TableName +
					" WHERE (" + 
						"(" + SMTablearcustomer.sCustomerNumber + " = '" + sCustomerCode + "')" +
					")";
			if(!clsDatabaseFunctions.executeSQL(SQL, conn)){
				m_sErrorMessageArray.add("Error deleting customer " + sCustomerCode);
				clsDatabaseFunctions.rollback_data_transaction(conn);
				return false;
			}
			
			//Delete Customer Statistics
			SQL = "DELETE FROM "
					+ SMTablearcustomerstatistics.TableName
					+ " WHERE (" 
						+ "(" + SMTablearcustomerstatistics.sCustomerNumber + " = '" + sCustomerCode + "')"
					+ ")";
			if(!clsDatabaseFunctions.executeSQL(SQL, conn)){
				m_sErrorMessageArray.add("Error deleting customer statistics for " + sCustomerCode);
				clsDatabaseFunctions.rollback_data_transaction(conn);
				return false;
			}
			
			//Delete Monthly Statistics
			SQL =  "DELETE FROM "
					+ SMTablearmonthlystatistics.TableName
					+ " WHERE (" 
						+ "(" + SMTablearmonthlystatistics.sCustomerNumber + " = '" + sCustomerCode + "')"
					+ ")"
					;
			if(!clsDatabaseFunctions.executeSQL(SQL, conn)){
				m_sErrorMessageArray.add("Error deleting monthly statistics for " + sCustomerCode);
				clsDatabaseFunctions.rollback_data_transaction(conn);
				return false;
			}
			
			//Delete Customer shipto's
			SQL = "DELETE FROM "
					+ SMTablearcustomershiptos.TableName
					+ " WHERE (" 
						+ "(" + SMTablearcustomershiptos.sCustomerNumber + " = '" + sCustomerCode + "')"
					+ ")"
					;
			if(!clsDatabaseFunctions.executeSQL(SQL, conn)){
				m_sErrorMessageArray.add("Error deleting customer ship-tos for " + sCustomerCode);
				clsDatabaseFunctions.rollback_data_transaction(conn);
				return false;
			}
			
			SQL = "DELETE FROM "
				+ SMTablesitelocations.TableName
				+ " WHERE (" 
					+ "(" + SMTablesitelocations.sAcct + " = '" + sCustomerCode + "')"
				+ ")"
				;
			if(!clsDatabaseFunctions.executeSQL(SQL, conn)){
				m_sErrorMessageArray.add("Error deleting customer site locations for " + sCustomerCode);
				clsDatabaseFunctions.rollback_data_transaction(conn);
				return false;
			}
			
			SQL = "DELETE FROM "
				+ SMTablesalescontacts.TableName
				+ " WHERE (" 
					+ "(" + SMTablesalescontacts.scustomernumber + " = '" + sCustomerCode + "')"
				+ ")"
				;
			if(!clsDatabaseFunctions.executeSQL(SQL, conn)){
				m_sErrorMessageArray.add("Error deleting sales contacts for " + sCustomerCode);
				clsDatabaseFunctions.rollback_data_transaction(conn);
				return false;
			}

			SQL = "DELETE FROM "
				+ SMTablecallsheets.TableName
				+ " WHERE (" 
					+ "(" + SMTablecallsheets.sAcct + " = '" + sCustomerCode + "')"
				+ ")"
				;
			if(!clsDatabaseFunctions.executeSQL(SQL, conn)){
				m_sErrorMessageArray.add("Error deleting sales contacts for " + sCustomerCode);
				clsDatabaseFunctions.rollback_data_transaction(conn);
				return false;
			}
			
			SQL = "DELETE FROM "
					+ SMTabledefaultsalesgroupsalesperson.TableName
					+ " WHERE (" 
						+ "(" + SMTabledefaultsalesgroupsalesperson.scustomercode + " = '" + sCustomerCode + "')"
					+ ")"
					;
				if(!clsDatabaseFunctions.executeSQL(SQL, conn)){
					m_sErrorMessageArray.add("Error deleting default salespersons for " + sCustomerCode + " with SQL: " + SQL + ".");
					clsDatabaseFunctions.rollback_data_transaction(conn);
					return false;
				}
			
		}catch(SQLException e){
			m_sErrorMessageArray.add("Error deleting customer " + sCustomerCode + " - " + e.getMessage());
			return false;
		}
		
		if(!clsDatabaseFunctions.commit_data_transaction(conn)){
			clsDatabaseFunctions.rollback_data_transaction(conn);
			return false;
		}
		
		return true;
	}
	public String getTaxJurisdictionAndType(String sDBID, ServletContext context, String sUserID, String sUserFullName) throws Exception{
		String s = "";
		String SQL = "SELECT"
			+ " " + SMTabletax.staxjurisdiction
			+ ", " + SMTabletax.staxtype
			+ " FROM " + SMTabletax.TableName
			+ " WHERE ("
				+ "(" + SMTabletax.lid + " = " + getstaxid() + ")"
			+ ")"
		;
		try {
			ResultSet rs = clsDatabaseFunctions.openResultSet(
				SQL, 
				context, 
				sDBID, 
				"MySQL", 
				SMUtilities.getFullClassName(this.toString()) + ".getTaxJurisdictionAndType - user: " 
				+ sUserID
				+ " - "
				+ sUserFullName
					);
			if (rs.next()){
				s += rs.getString(SMTabletax.staxjurisdiction) + " - " + rs.getString(SMTabletax.staxtype);
			}
			rs.close();
		} catch (Exception e) {
			throw new Exception("Error [1454082977] getting tax information from customer - " + e.getMessage());
		}
		return s;
	}
    public String getM_sCustomerNumber(){
    	return m_sCustomerNumber;
    }
    public void setM_sCustomerNumber(String sCustomerNumber){
    	m_sCustomerNumber = sCustomerNumber.trim();
    }
    public String getM_sCustomerName(){
    	return m_sCustomerName;
    }
	public void setM_sCustomerName(String customerName) {
		m_sCustomerName = customerName.trim();
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
    public String getM_sTerms(){
    	return m_sTerms;
    }
	public void setM_sTerms(String terms) {
		m_sTerms = terms.trim();
	}
	public String getM_sAccountSet() {
		return m_sAccountSet;
	}
	public void setM_sAccountSet(String accountSet) {
		m_sAccountSet = accountSet.trim();
	}
	public String getM_iOnHold() {
		return m_iOnHold;
	}
	public void setM_iOnHold(String onHold) {
		m_iOnHold = onHold;
	}
	public String getM_datStartDate() {
		return m_datStartDate;
	}
	public void setM_datStartDate(String startDate) {
		m_datStartDate = startDate;
	}
	public String getM_dCreditLimit() {
		return m_dCreditLimit;
	}
	public void setM_dCreditLimit(String creditLimit) {
		m_dCreditLimit = creditLimit;
	}

	public String getM_mAccountingNotes() {
		return m_mAccountingNotes;
	}
	public void setM_mAccountingNotes(String accountingNotes) {
		m_mAccountingNotes = accountingNotes;
	}
	public String getM_mCustomerComments() {
		return m_mCustomerComments;
	}
	public void setM_mCustomerComments(String customerComments) {
		m_mCustomerComments = customerComments;
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
	public void setM_sLastEditUserFullName(String lastEditUserFullName) {
		lastEditUserFullName = lastEditUserFullName.trim();
	}
	public String getM_sLastEditUserID() {
		return m_sLastEditUserID;
	}
	public void setM_sLastEditUserID(String lastEditUserID) {
		lastEditUserID = lastEditUserID.trim();
	}
	public String getM_sCustomerGroup() {
		return m_sCustomerGroup;
	}
	public void setM_sCustomerGroup(String customerGroup) {
		m_sCustomerGroup = customerGroup.trim();
	}
	public String getM_sEmailAddress() {
		return m_sEmailAddress;
	}
	public void setM_sEmailAddress(String emailAddress) {
		m_sEmailAddress = emailAddress.trim();
	}
	public String getM_sWebAddress() {
		return m_sWebAddress;
	}
	public void setM_sWebAddress(String webAddress) {
		m_sWebAddress = webAddress.trim();
	}
	public String getM_sPriceListCode() {
		return m_sPriceListCode;
	}
	public void setM_sPriceListCode(String PriceListCode) {
		m_sPriceListCode = PriceListCode.trim();
	}
	public String getM_sPriceLevel() {
		return m_sPriceLevel;
	}
	public void setM_sPriceLevel(String PriceLevel) {
		m_sPriceLevel = PriceLevel.trim();
	}
	//public String getM_sTaxJurisdiction() {
	//	return m_sTaxJurisdiction;
	//}
	//public void setM_sTaxJurisdiction(String sTaxJurisdiction) {
	//	m_sTaxJurisdiction = sTaxJurisdiction.trim();
	//}
	//public String getM_sTaxType() {
	//	return m_sTaxType;
	//}
	//public void setM_sTaxType(String TaxType) {
	//	m_sTaxType = TaxType.trim();
	//}
	public String getM_sUsesElectronicDeposit() {
		return m_sUsesElectronicDeposit;
	}
	public void setM_sUsesElectronicDeposit(String sUsesElectronicDeposit) {
		m_sUsesElectronicDeposit = sUsesElectronicDeposit;
	}
	public String getM_sRequiresStatements() {
		return m_sRequiresStatements;
	}
	public void setM_sRequiresStatements(String sRequiresStatements) {
		m_sRequiresStatements = sRequiresStatements;
	}
	public String getM_sRequiresPO() {
		return m_sRequiresPO;
	}
	public void setM_sRequiresPO(String sRequiresPO) {
		m_sRequiresPO = sRequiresPO;
	}
	public String getsgdoclink() {
		return m_sgdoclink;
	}
	public void setsgdoclink(String ssgdoclink) {
		m_sgdoclink = ssgdoclink;
	}
	public String getstaxid() {
		return m_itaxid;
	}
	public void settaxid(String staxid) {
		m_sgdoclink = staxid;
	}
	public String getsinvoicingcontact() {
		return m_sinvoicingcontact;
	}
	public void setsinvoicingcontact(String sinvoicingcontact) {
		m_sinvoicingcontact = sinvoicingcontact;
	}
	public String getsinvoicingemail() {
		return m_sinvoicingemail;
	}
	public void setsinvoicingemail(String sinvoicingemail) {
		m_sinvoicingemail = sinvoicingemail;
	}
	public String getsinvoicingnotes() {
		return m_sinvoicingnotes;
	}
	public void setsinvoicingnotes(String sinvoicingnotes) {
		m_sinvoicingnotes = sinvoicingnotes;
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
			s += "<BR>" + m_sErrorMessageArray.get(i);
		}
		return s;
	}
}
