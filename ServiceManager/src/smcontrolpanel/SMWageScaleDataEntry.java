package smcontrolpanel;

import SMDataDefinition.*;
import ServletUtilities.clsMasterEntry;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsDateAndTimeConversions;
import ServletUtilities.clsManageRequestParameters;
import TCSDataDefinition.*;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Connection;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;


public class SMWageScaleDataEntry extends clsMasterEntry{

	public static final String ParamObjectName = "Wage Scale Record";
	private static boolean bDebugMode = false;
	
	//Particular to the specific class
	//public static final String ParamID = "sid";
	public static final String ParamCreatedByID = "lCreatedByID";
	public static final String ParamEmployeeName = "sEmployeeName";
	public static final String ParamEmployeeSSN = "sEmployeeSSN";
	public static final String ParamEmployeeAddress = "sEmployeeAddress";
	public static final String ParamEmployeeAddress2 = "sEmployeeAddress2";
	public static final String ParamEmployeeCity = "sEmployeeCity"; 
	public static final String ParamEmployeeState = "sEmployeeState";
	public static final String ParamEmployeeZipCode = "sEmployeeZipCode";
	public static final String ParamEmployeeTitle = "sEmployeeTitle";
	public static final String ParamPeriodEndDate = "datPeriodEndDate";
	public static final String ParamCostNumber = "sCostNumber"; //combination of day and order number
	public static final String ParamRegHours = "sRegHours";
	public static final String ParamOTHours = "sOTHours";
	public static final String ParamDTHours = "sDTHours";
	public static final String ParamPayRate = "sPayRate";	
	public static final String ParamHolidayHours = "sHolidayHours";
	public static final String ParamPersonalHours = "sPersonalHours";
	public static final String ParamVacHours = "sVacHours";
	public static final String ParamGross = "sGross";
	public static final String ParamFederal = "sFederal";
	public static final String ParamSS = "sSS";
	public static final String ParamMedicare = "sMedicare";
	public static final String ParamState = "sState";
	public static final String ParamMiscDed = "sMiscDed";
	public static final String ParamNetPay = "sNetPay";
	public static final String ParamVacAllowed = "sVacAllowed";
	public static final String ParamEncryptionKey = "ENCRYPTIONKEY";
	public static final int MinimumEncryptionKeyLength = 5;
	public static final String DELETE_BUTTON_LABEL = "DELETE";
	public static final String DELETE_BUTTON_VALUE = "Delete ALL wage scale records";
	public static final String CONFIRM_DELETE_CHECKBOX = "CONFIRMDELETE";


	//private String m_sid;
	private String m_lCreatedByID = "0";
	private String m_sEmployeeName = "";
	private String m_sEmployeeSSN = "";
	private String m_sEmployeeAddress = "";
	private String m_sEmployeeAddress2 = "";
	private String m_sEmployeeCity = ""; 
	private String m_sEmployeeState = "";
	private String m_sEmployeeZipCode = "";
	private String m_sEmployeeTitle = "";
	private Date m_datPeriodEndDate = new Date(System.currentTimeMillis());
	private String m_sCostNumber = ""; //combination of day and order number
	private double m_dRegHours = 0;
	private double m_dOTHours = 0;
	private double m_dDTHours = 0;
	private double m_dPayRate = 0;	
	private double m_dHolidayHours = 0;
	private double m_dPersonalHours = 0;
	private double m_dVacHours = 0;
	private double m_dGross = 0;
	private double m_dFederal = 0;
	private double m_dSS = 0;
	private double m_dMedicare = 0;
	private double m_dState = 0;
	private double m_dMiscDed = 0;
	private double m_dNetPay = 0;
	private double m_dVacAllowed = 0;
	private String m_sEncryptionKey = "";
	/*
	private String m_sphysicalinventoryid;
	private String m_sdesc;*/
	//private String m_datcreated;
	
	//private SimpleDateFormat sdf = new SimpleDateFormat("MM/DD/YYYY");
	private SimpleDateFormat sdfSQLDateFormatter = new SimpleDateFormat("yyyy-MM-dd");
	
	public SMWageScaleDataEntry() {
		super();
		initEntryVariables();
        }

	SMWageScaleDataEntry (HttpServletRequest req){
		super(req);
		initEntryVariables();

		m_lCreatedByID = clsManageRequestParameters.get_Request_Parameter(SMWageScaleDataEntry.ParamCreatedByID, req).trim();
		m_sEmployeeName = clsManageRequestParameters.get_Request_Parameter(SMWageScaleDataEntry.ParamEmployeeName, req).trim();
		m_sEmployeeSSN = clsManageRequestParameters.get_Request_Parameter(SMWageScaleDataEntry.ParamEmployeeSSN, req).trim();
		m_sEmployeeAddress = clsManageRequestParameters.get_Request_Parameter(SMWageScaleDataEntry.ParamEmployeeAddress, req).trim();
		m_sEmployeeAddress2 = clsManageRequestParameters.get_Request_Parameter(SMWageScaleDataEntry.ParamEmployeeAddress2, req).trim();
		m_sEmployeeCity = clsManageRequestParameters.get_Request_Parameter(SMWageScaleDataEntry.ParamEmployeeCity, req).trim(); 
		m_sEmployeeState = clsManageRequestParameters.get_Request_Parameter(SMWageScaleDataEntry.ParamEmployeeState, req).trim();;
		m_sEmployeeZipCode = clsManageRequestParameters.get_Request_Parameter(SMWageScaleDataEntry.ParamEmployeeZipCode, req).trim();
		m_sEmployeeTitle = clsManageRequestParameters.get_Request_Parameter(SMWageScaleDataEntry.ParamEmployeeTitle, req).trim();
		m_sCostNumber = clsManageRequestParameters.get_Request_Parameter(SMWageScaleDataEntry.ParamCostNumber, req).trim(); //combination of day and order number
		m_dRegHours = Double.parseDouble(clsManageRequestParameters.get_Request_Parameter(SMWageScaleDataEntry.ParamRegHours, req));
		m_dOTHours = Double.parseDouble(clsManageRequestParameters.get_Request_Parameter(SMWageScaleDataEntry.ParamOTHours, req));
		m_dDTHours = Double.parseDouble(clsManageRequestParameters.get_Request_Parameter(SMWageScaleDataEntry.ParamDTHours, req));
		m_dPayRate = Double.parseDouble(clsManageRequestParameters.get_Request_Parameter(SMWageScaleDataEntry.ParamPayRate, req));	
		m_dHolidayHours = Double.parseDouble(clsManageRequestParameters.get_Request_Parameter(SMWageScaleDataEntry.ParamHolidayHours, req));
		m_dPersonalHours = Double.parseDouble(clsManageRequestParameters.get_Request_Parameter(SMWageScaleDataEntry.ParamPersonalHours, req));
		m_dVacHours = Double.parseDouble(clsManageRequestParameters.get_Request_Parameter(SMWageScaleDataEntry.ParamVacHours, req));
		m_dGross = Double.parseDouble(clsManageRequestParameters.get_Request_Parameter(SMWageScaleDataEntry.ParamGross, req));
		m_dFederal = Double.parseDouble(clsManageRequestParameters.get_Request_Parameter(SMWageScaleDataEntry.ParamFederal, req));
		m_dSS = Double.parseDouble(clsManageRequestParameters.get_Request_Parameter(SMWageScaleDataEntry.ParamSS, req));
		m_dMedicare = Double.parseDouble(clsManageRequestParameters.get_Request_Parameter(SMWageScaleDataEntry.ParamMedicare, req));
		m_dState = Double.parseDouble(clsManageRequestParameters.get_Request_Parameter(SMWageScaleDataEntry.ParamState, req));
		m_dMiscDed = Double.parseDouble(clsManageRequestParameters.get_Request_Parameter(SMWageScaleDataEntry.ParamMiscDed, req));
		m_dNetPay = Double.parseDouble(clsManageRequestParameters.get_Request_Parameter(SMWageScaleDataEntry.ParamNetPay, req));
		m_dVacAllowed = Double.parseDouble(clsManageRequestParameters.get_Request_Parameter(SMWageScaleDataEntry.ParamVacAllowed, req));
		
	}
	/*
    public boolean load (ServletContext context, String sConf, String sUser){
    	Connection conn = SMUtilities.getConnection(
    			context, 
    			sConf, 
    			"MySQL", 
    			this.toString() + " - user: " + sUser
    			);
    	
    	if (conn == null){
    		super.addErrorMessage("Error opening data connection.");
    		return false;
    	}
    	
    	boolean bResult = load (conn);
    	SMUtilities.freeConnection(context, conn);
    	return bResult;
    	
    }
    public boolean load (Connection conn){
    	return load (m_sid, conn);
    }
    private boolean load (String sID, Connection conn){

    	@SuppressWarnings("unused")
		long lID;
		try{
			lID = Long.parseLong(sID);
		}catch(NumberFormatException n){
			super.addErrorMessage("Invalid ID: '" + sID + "'");
			return false;
		}

		String SQL = " SELECT * FROM " + SMTableicphysicalcounts.TableName
			+ " WHERE ("
				+ SMTableicphysicalcounts.lid + " = " + sID
			+ ")"
			;
		
		try {
			ResultSet rs = SMUtilities.openResultSet(SQL, conn);
			if (rs.next()) {
				//Load the variables here:
				m_sid = sID;
				m_sphysicalinventoryid = Long.toString(rs.getLong(SMTableicphysicalcounts.lphysicalinventoryid));
				m_sdesc = rs.getString(SMTableicphysicalcounts.sdesc);
				m_screatedby = rs.getString(SMTableicphysicalcounts.screatedby);
				m_datcreated = SMUtilities.resultsetDateStringToString(
						rs.getString(SMTableicphysicalcounts.datcreated));
				//System.out.println("In ICPhysicalCountEntry - rs.getstring(datcreated) = " 
				//		+ rs.getString(SMTableicphysicalcounts.datcreated));
				//System.out.println("In ICPhysicalCountEntry - m_datCreated = " + m_datcreated);
			} else {
				super.addErrorMessage("No " + ParamObjectName + " found for ID: '" + sID
						+ "'");
				rs.close();
				return false;
			}
		} catch (SQLException e) {
			super.addErrorMessage("Error reading " + ParamObjectName + " for ID: '" + sID
					+ "' - " + e.getMessage());
			return false;
		}
		return true;
    }
    
    public boolean save_without_data_transaction (ServletContext context, String sConf, String sUser){
    	
       	Connection conn = SMUtilities.getConnection(
    			context, 
    			sConf, 
    			"MySQL", 
    			this.toString() + " - user: " + sUser
    			);
    	
    	if (conn == null){
    		super.addErrorMessage("Error opening data connection.");
    		return false;
    	}
    	
    	boolean bResult = save_without_data_transaction (conn, sUser);
    	SMUtilities.freeConnection(context, conn);
    	return bResult;	
    	
    }
    */
    public boolean save_without_data_transaction (int iLine, Connection conn, String sUserID){

    	m_lCreatedByID = sUserID;
    	
    	//SMWageScaleDataEntry record = new SMWageScaleDataEntry();
    	
    	if (!validate_entry_fields(iLine, conn)){
    		//imported 
    		return false;
    	}

    	String SQL = "INSERT INTO " + SMTablewagescalerecords.TableName + "(" +
	
			 " " + SMTablewagescalerecords.sEmployeeName + "," +
			 " " + SMTablewagescalerecords.sEmployeeSSN + "," +
			 " " + SMTablewagescalerecords.sEmployeeAddress + "," +
			 " " + SMTablewagescalerecords.sEmployeeAddress2 + "," +
			 " " + SMTablewagescalerecords.sEmployeeCity + "," +
			 " " + SMTablewagescalerecords.sEmployeeState + "," +
			 " " + SMTablewagescalerecords.sEmployeeZipCode + "," +
			 " " + SMTablewagescalerecords.sEmployeeTitle + "," +
			 " " + SMTablewagescalerecords.datPeriodEndDate + "," +
			 " " + SMTablewagescalerecords.sCostNumber + "," +
			 " " + SMTablewagescalerecords.sRegHours + "," +
			 " " + SMTablewagescalerecords.sOTHours + "," +
			 " " + SMTablewagescalerecords.sDTHours + "," +
			 " " + SMTablewagescalerecords.sPayRate + "," +
			 " " + SMTablewagescalerecords.sHolidayHours + "," +
			 " " + SMTablewagescalerecords.sPersonalHours + "," +
			 " " + SMTablewagescalerecords.sVacHours + "," +
			 " " + SMTablewagescalerecords.sGross + "," +
			 " " + SMTablewagescalerecords.sFederal + "," +
			 " " + SMTablewagescalerecords.sSS + "," +
			 " " + SMTablewagescalerecords.sMedicare + "," +
			 " " + SMTablewagescalerecords.sState + "," +
			 " " + SMTablewagescalerecords.sMiscDed + "," +
			 " " + SMTablewagescalerecords.sNetPay + "," +
			 " " + SMTablewagescalerecords.sVacAllowed + "," +
			 " " + SMTablewagescalerecords.lCreatedByID + "," +
			 " " + SMTablewagescalerecords.sEncryptedEmployeeName +
		 ") VALUES (" + 
			" '" + m_sEmployeeName + "'," +
			"AES_ENCRYPT('" + m_sEmployeeSSN + "','" + getEncryptionKey() + "')" + ", " +
			"AES_ENCRYPT('" + m_sEmployeeAddress + "','" + getEncryptionKey() + "')" + ", " +
			"AES_ENCRYPT('" + m_sEmployeeAddress2 + "','" + getEncryptionKey() + "')" + ", " +
			"AES_ENCRYPT('" + m_sEmployeeCity + "','" + getEncryptionKey() + "')" + ", " +
			"AES_ENCRYPT('" + m_sEmployeeState + "','" + getEncryptionKey() + "')" + ", " +
			"AES_ENCRYPT('" + m_sEmployeeZipCode + "','" + getEncryptionKey() + "')" + ", " +
			" '" + m_sEmployeeTitle + "'," +
			" '" + sdfSQLDateFormatter.format(m_datPeriodEndDate) + "', " +
			" '" + m_sCostNumber + "'," +
			"AES_ENCRYPT('" + m_dRegHours + "','" + getEncryptionKey() + "')" + ", " +
			"AES_ENCRYPT('" + m_dOTHours + "','" + getEncryptionKey() + "')" + ", " +
			"AES_ENCRYPT('" + m_dDTHours + "','" + getEncryptionKey() + "')" + ", " +
			"AES_ENCRYPT('" + m_dPayRate + "','" + getEncryptionKey() + "')" + ", " +
			"AES_ENCRYPT('" + m_dHolidayHours + "','" + getEncryptionKey() + "')" + ", " +
			"AES_ENCRYPT('" + m_dPersonalHours + "','" + getEncryptionKey() + "')" + ", " +
			"AES_ENCRYPT('" + m_dVacHours + "','" + getEncryptionKey() + "')" + ", " +
			"AES_ENCRYPT('" + m_dGross + "','" + getEncryptionKey() + "')" + ", " +
			"AES_ENCRYPT('" + m_dFederal + "','" + getEncryptionKey() + "')" + ", " +
			"AES_ENCRYPT('" + m_dSS + "','" + getEncryptionKey() + "')" + ", " +
			"AES_ENCRYPT('" + m_dMedicare + "','" + getEncryptionKey() + "')" + ", " +
			"AES_ENCRYPT('" + m_dState + "','" + getEncryptionKey() + "')" + ", " +
			"AES_ENCRYPT('" + m_dMiscDed + "','" + getEncryptionKey() + "')" + ", " +
			"AES_ENCRYPT('" + m_dNetPay + "','" + getEncryptionKey() + "')" + ", " +
			"AES_ENCRYPT('" + m_dVacAllowed + "','" + getEncryptionKey() + "')" + ", " +
			" " + m_lCreatedByID + ", " +
			"AES_ENCRYPT('" + m_sEmployeeName + "','" + getEncryptionKey() + "')"
			+ ")";

    	if (bDebugMode){
    		System.out.println("[1368045921] In " + this.toString() + " Save SQL = " + SQL);
    	}
    	try{
	    	if (clsDatabaseFunctions.executeSQL(SQL, conn) == false){
	    		System.out.println(this.toString() + "Could not insert " + ParamObjectName + ".<BR>");
	    		super.addErrorMessage("Could not insert " + ParamObjectName + " with SQL: " + SQL);
	    		return false;
	    	}else{
	    	}
    	}catch(SQLException ex){
    		System.out.println("[1370362002] Error in " + this.toString() + " class!!");
    	    System.out.println("[1370362002] SQLException: " + ex.getMessage());
    	    System.out.println("[1370362002] SQLState: " + ex.getSQLState());
    	    System.out.println("[1370362002] SQL: " + ex.getErrorCode());
    	    super.addErrorMessage("Error inserting " + ParamObjectName + ": " + ex.getMessage());
    	    return false;
    	}
    	
    	return true;
    }
    public boolean validate_encryption_key (ServletContext context, String sConf, String sUserID, String sUserFullName, String sDecryptKey){
    	boolean bKeyisValid = false;
    	String sSQL = "";
    	ResultSet rs = null;
    	
    	Connection conn = clsDatabaseFunctions.getConnection(
				context, 
				sConf, 
				"MySQL", 
				SMUtilities.getFullClassName(this.toString() + ".validate_encryption_key - user: " + sUserID + " - " + sUserFullName)
		);
    	try{ 
	    	sSQL = " SELECT " + SMTablewagescalerecords.sEmployeeName + " FROM " + SMTablewagescalerecords.TableName 
	    			+ " WHERE ( AES_DECRYPT(" + SMTablewagescalerecords.TableName + "." 
    	                     + SMTablewagescalerecords.sEncryptedEmployeeName +",'"+ sDecryptKey +"') =" + SMTablewagescalerecords.sEmployeeName + ")";
	   	
	    	rs = clsDatabaseFunctions.openResultSet(sSQL, conn);

	    	//If the result set is not empty the key is valid
	    	if (rs.next()){
	    		bKeyisValid = true;
	    	}else{
	    		//no data to run report
	    	}
	    	rs.close();
    	}catch (SQLException ex){
    		
    		System.out.println("Error validating ecryption key");
    	}
    	clsDatabaseFunctions.freeConnection(context, conn, "[1547080683]");
    	return bKeyisValid;
    }
   
    public boolean validate_entry_fields (int iLine, Connection conn){
        //Validate the entries here:
    	//	****1. If EmployeeSSN is valid.
    	//	****2. If the COSTNUMBER field has corrected "day info" and valid order number.
    	//	****4. If Order number in COSTNUMBER is valid.
    	//	3. If PERIODENDDATE field has existing period end date.
    	//	****5. if CreatedBy is valid.
    	boolean bEntriesAreValid = true;
    	String sSQL = "";
	    String sTCSDatabaseName = "";
    	ResultSet rs = null;
    	
    	//get Timecard database info for additinal verification information.
    	try{
	    	sSQL = "SELECT * FROM " + SMTablesmoptions.TableName;
	    	rs = clsDatabaseFunctions.openResultSet(sSQL, conn);
	    	if (rs.next()){
	    		sTCSDatabaseName = rs.getString(SMTablesmoptions.TableName + "." + SMTablesmoptions.stimecarddatabase);
	    	}
    	}catch (SQLException ex){
    		//error getting time card system database name
    		System.out.println(this.toString() + "[1368033910] Error getting corresponding time card system database name.");
    		System.out.println(this.toString() + "[1368033911] Error: " + ex.getMessage());
    		super.addErrorMessage("[1368033910] Error getting corresponding time card system database name.<BR> Error: " + ex.getMessage());
        	bEntriesAreValid = false;
        	return bEntriesAreValid;
    	}
    	
    	boolean bCheckSSN = false;
    	if (bCheckSSN){
			//Employee SSN
			m_sEmployeeSSN = m_sEmployeeSSN.trim();
	    	if (bDebugMode){
				System.out.println("m_sEmployeeSSN: " + m_sEmployeeSSN);
			}
			if (m_sEmployeeSSN.compareToIgnoreCase("") == 0){
	        	super.addErrorMessage("[1368033963] Employee SSN cannot be empty on line " + iLine + ".");
	        	bEntriesAreValid = false;
	        	return bEntriesAreValid;
	        }else{
	        	try{
	        		//compare the last 4 digits of SSN number 
		        	sSQL = "SELECT * FROM " + sTCSDatabaseName + "." + Employees.TableName + 
		        			" WHERE" +
		        				" RIGHT(" + sTCSDatabaseName + "." + Employees.TableName + "." + Employees.sSSN + ", 4) = '" + m_sEmployeeSSN.substring(5) + "'";
		        	rs = clsDatabaseFunctions.openResultSet(sSQL, conn);
		        	if (rs.next()){
		        		//this SSN is valid
		        	}else{
		    			super.addErrorMessage("[1368033913] Error validating SSN for " + m_sEmployeeName + " on line " + iLine + ".");
		            	bEntriesAreValid = false;
		            	return bEntriesAreValid;
		        	}
	        	}catch (SQLException ex){
		        		//this SSN is not valid
		        		System.out.println(this.toString() + "[1368033912] Error getting employee SSN.");
		        		System.out.println(this.toString() + "[1368033913] Error: " + ex.getMessage());
		        		super.addErrorMessage("[1368033912] Error getting employee SSN " + iLine + ".<BR> Error: " + ex.getMessage());
		            	bEntriesAreValid = false;
		            	return bEntriesAreValid;
	        	}
	        }
    	}
        
    	//check to see if cost number is valid
    	String sDayCheck = "MO,TU,WE,TH,FR,SA,SU";
    	if (bDebugMode){
			System.out.println("m_sCostNumber: " + m_sCostNumber);
		}
    	if (m_sCostNumber.trim().compareTo("") == 0){
    		//if the cost number is empty, assuming this is a line for regular jobs. Don't do anything.
    	}else if (sDayCheck.indexOf(m_sCostNumber.substring(0, 2)) >= 0){
			//day field is valid, validate order now.
        	if (bDebugMode){
    			System.out.println("Day: " + m_sCostNumber.substring(0, 2));
    			System.out.println("Index: " + sDayCheck.indexOf(m_sCostNumber.substring(0, 2)));
    		}
			try{
	        	sSQL = "SELECT * FROM " + SMTableorderheaders.TableName + 
						" WHERE" +
							" " + SMTableorderheaders.strimmedordernumber + " = '" + m_sCostNumber.substring(2).trim().replaceAll("^0*", "") + "'"; 
	        	if (bDebugMode){
	    			System.out.println("[1368033916] sSQL: " + sSQL);
	    		}
	        	rs = clsDatabaseFunctions.openResultSet(sSQL, conn);
	        	if (rs.next()){
	        		//this order number is valid
	        	}else{
	    			super.addErrorMessage("[1368033915] Error validating order number on line " + iLine + "."); 
	            	bEntriesAreValid = false;
	            	return bEntriesAreValid;
	        	}
        	}catch (SQLException ex){
	        		//this SSN is not valid
	        		System.out.println(this.toString() + "[1368033914] Error getting employee SSN on line " + iLine + ".");
	        		System.out.println(this.toString() + "[1368033915] Error: " + ex.getMessage());
	        		super.addErrorMessage("[1368033914] Error getting employee SSN on line " + iLine + ".<BR> Error: " + ex.getMessage());
	            	bEntriesAreValid = false;
	            	return bEntriesAreValid;
        	}
		}else{
    		//this Cost Number is not valid
    		System.out.println(this.toString() + "[1368033917] The day of week is not recognized on line " + iLine + ".");
    		super.addErrorMessage("[1368033917] The day of week is not recognized on line " + iLine + ".");
        	bEntriesAreValid = false;
        	return bEntriesAreValid;
		}
    	
		//Check to see if Period End Date is valid date
    	if (bDebugMode){
			System.out.println("m_sPeriodEndDate: " + m_datPeriodEndDate);
		}
		//check if period date is already in database.
    	//if there is any record that matches the one line, halt the process and report back.
    	
    	if (m_sCostNumber.length() > 0){
			try{
				//assuming same mechanic will not work on the same job for the same amount of time on the same day. 
	        	sSQL = "SELECT *" +
	        			" FROM " + SMTablewagescalerecords.TableName +
	        			" WHERE" +
	        				" " + SMTablewagescalerecords.TableName + "." + SMTablewagescalerecords.datPeriodEndDate + " = '" + m_datPeriodEndDate + "'" +
	        				" AND" +
	        				" " + SMTablewagescalerecords.TableName + "." + SMTablewagescalerecords.sCostNumber + " = '" + m_sCostNumber + "'" +
	        				" AND" +
	        				" " + SMTablewagescalerecords.TableName + "." + SMTablewagescalerecords.sEmployeeName + " = '" + m_sEmployeeName + "'"
	        				; 
	        	if (bDebugMode){
	    			System.out.println("sSQL: " + sSQL);
	    		}
	        	rs = clsDatabaseFunctions.openResultSet(sSQL, conn);
	        	if (rs.next()){
	        		//this period already exist, stop process now.
	    			super.addErrorMessage("[1368034916] This file has already been imported.");
	            	bEntriesAreValid = false;
	            	return bEntriesAreValid;
	        	}
	    	}catch (SQLException ex){
	        		//error when getting period end date info
	        		System.out.println(this.toString() + "[1368033916] Error getting period end date information.");
	        		System.out.println(this.toString() + "[1368033917] Error: " + ex.getMessage());
	            	bEntriesAreValid = false;
	            	return bEntriesAreValid;
	    	}
    	}
    	
		try{
			rs.close();
		}catch(SQLException ex){
    		System.out.println(this.toString() + "[1368033918] Error closing ResulSet: " + ex.getMessage());
		}
    	return bEntriesAreValid;
    }
    
 public void delete (ServletContext context, String sConf, String sUserID, String sUserFullName) throws Exception{
    	
    	Connection conn = clsDatabaseFunctions.getConnection(
    			context, 
    			sConf, 
    			"MySQL", 
    			this.toString() + " - user: " 
    			+ sUserID
    			+ " - "
    			+ sUserFullName
    			);
    	
    	if (conn == null){
    		throw new Exception ("Error [1446044962] opening data connection.");
    	}
    	
    	try {
			delete (conn);
		} catch (Exception e) {
			clsDatabaseFunctions.freeConnection(context, conn, "[1547080681]");
			throw new Exception(e.getMessage());
		}
    	clsDatabaseFunctions.freeConnection(context, conn, "[1547080682]");
    }
    public void delete (Connection conn) throws Exception{
    	
    	//Validate deletions
    	String SQL = "";

    	SQL = "DELETE FROM " + SMTablewagescalerecords.TableName;
   
		try{
		    Statement stmt = conn.createStatement();
		    stmt.executeUpdate(SQL);
		}catch (Exception ex) {
			throw new Exception("Error [1446044963] - Could not delete ALL " + ParamObjectName + " records with SQL: " + SQL + " - " + ex.getMessage());
		}
		
		//Empty the values:
		initEntryVariables();
    }

    @Override

    public void addErrorMessage(String sMsg){
    	super.addErrorMessage(sMsg);
    }
	public String printDebugString() {
		return "SMWageScaleDataEntry [ m_lcreatedbyid=" + m_lCreatedByID + ", createdtime=" + clsDateAndTimeConversions.now("M/d/yyyy hh:mm:ss") + "]";
	}
/*
	public String getQueryString(){

		//Particular to the specific class
		return ParamID + "=" + ServletUtilities.URLEncode(m_sid)
			+ "&" + ParamPhysicalInventoryID + ServletUtilities.URLEncode(m_sphysicalinventoryid)
			+ "&" + ParamCreatedBy + ServletUtilities.URLEncode(m_screatedby)
			+ "&" + ParamdatCreated + ServletUtilities.URLEncode(m_datcreated)
			+ "&" + ParamDesc + ServletUtilities.URLEncode(m_sdesc)
		;
	}
	*/
	public String getEmployeeName() {
		return m_sEmployeeName;
	}

	public void setEmployeeName(String sEmployeeName) {
		m_sEmployeeName = sEmployeeName;
	}

	public String getEmployeeSSN() {
		return m_sEmployeeSSN;
	}

	public void setEmployeeSSN(String s) {
		m_sEmployeeSSN = s;
	}

	public String getEmployeeAddress() {
		return m_sEmployeeAddress;
	}

	public void setEmployeeAddress(String s) {
		m_sEmployeeAddress = s;
	}

	public String getEmployeeAddress2() {
		return m_sEmployeeAddress2;
	}

	public void setEmployeeAddress2(String s) {
		m_sEmployeeAddress2 = s;
	}

	public String getEmployeeCity() {
		return m_sEmployeeCity;
	}

	public void setEmployeeCity(String sEmployeeCity) {
		m_sEmployeeCity = sEmployeeCity;
	}

	public String getEmployeeState() {
		return m_sEmployeeState;
	}

	public void setEmployeeState(String sEmployeeState) {
		m_sEmployeeState = sEmployeeState;
	}

	public String getEmployeeZipCode() {
		return m_sEmployeeZipCode;
	}

	public void setEmployeeZipCode(String sEmployeeZipCode) {
		m_sEmployeeZipCode = sEmployeeZipCode;
	}

	public String getEmployeeTitle() {
		return m_sEmployeeTitle;
	}

	public void setEmployeeTitle(String sEmployeeTitle) {
		m_sEmployeeTitle = sEmployeeTitle;
	}

	public Date getPeriodEndDate() {
		return m_datPeriodEndDate;
	}

	public void setPeriodEndDate(java.util.Date date) {
		m_datPeriodEndDate = new Date(date.getTime());
	}

	public String getCostNumber() {
		return m_sCostNumber;
	}

	public void setCostNumber(String sCostNumber) {
		m_sCostNumber = sCostNumber;
	}

	public double getRegHours() {
		return m_dRegHours;
	}

	public void setRegHours(double d) {
		m_dRegHours = d;
	}

	public double getOTHours() {
		return m_dOTHours;
	}

	public void setOTHours(double d) {
		m_dOTHours = d;
	}

	public double getDTHours() {
		return m_dDTHours;
	}

	public void setDTHours(double d) {
		m_dDTHours = d;
	}

	public double getPayRate() {
		return m_dPayRate;
	}

	public void setPayRate(double d) {
		m_dPayRate = d;
	}

	public double getHolidayHours() {
		return m_dHolidayHours;
	}

	public void setHolidayHours(double d) {
		m_dHolidayHours = d;
	}

	public double getPersonalHours() {
		return m_dPersonalHours;
	}

	public void setPersonalHours(double d) {
		m_dPersonalHours = d;
	}

	public double getVacHours() {
		return m_dVacHours;
	}

	public void setVacHours(double d) {
		m_dVacHours = d;
	}

	public double getGross() {
		return m_dGross;
	}

	public void setGross(double d) {
		m_dGross = d;
	}

	public double getFederal() {
		return m_dFederal;
	}

	public void setFederal(double d) {
		m_dFederal = d;
	}

	public double getSS() {
		return m_dSS;
	}

	public void setSS(double d) {
		m_dSS = d;
	}

	public double getMedicare() {
		return m_dMedicare;
	}

	public void setMedicare(double d) {
		m_dMedicare = d;
	}

	public double getState() {
		return m_dState;
	}

	public void setState(double d) {
		m_dState = d;
	}

	public double getMiscDed() {
		return m_dMiscDed;
	}

	public void setMiscDed(double d) {
		m_dMiscDed = d;
	}

	public double getNetPay() {
		return m_dNetPay;
	}

	public void setNetPay(double d) {
		m_dNetPay = d;
	}

	public double getVacAllowed() {
		return m_dVacAllowed;
	}

	public void setVacAllowed(double d) {
		m_dVacAllowed = d;
	}
	
    private void initEntryVariables(){
		m_lCreatedByID = "0";
		super.initVariables();
		super.setObjectName(ParamObjectName);
    }

	public void setEncryptionKey(String encryptKey) {
		m_sEncryptionKey = encryptKey;	
	}
	
	public String getEncryptionKey() {
		return m_sEncryptionKey;
		
	}
}