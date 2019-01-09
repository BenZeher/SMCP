package smap;
import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import java.sql.Connection;
import java.util.ArrayList;
import SMDataDefinition.*;
import ServletUtilities.clsMasterEntry;
import ServletUtilities.clsServletUtilities;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsDateAndTimeConversions;
import ServletUtilities.clsManageBigDecimals;
import ServletUtilities.clsManageRequestParameters;
import smcontrolpanel.SMMasterEditSelect;
import smcontrolpanel.SMUtilities;

public class APVendorTerms extends clsMasterEntry{
	public static final String ParamObjectName = "Vendor Terms";
	
	public static final String ParamsTermsCode = "stermscode";
	public static final String ParamsDescription = "sdescription";
	public static final String ParamiActive = "iactive";
	public static final String ParamdatLastMaintained = "datlastmaintained";
	public static final String ParambdDiscountPercent = "bddiscountpercent"; 
	public static final String ParamiDiscountNumberOfDays = "idiscountnumberofdays"; 
	public static final String ParamiDiscountDayOfTheMonth = "idiscountdayofthemonth"; 
	public static final String ParamiDueNumberOfDays = "iduenumberofdays"; 
	public static final String ParamiDueDayOfTheMonth = "iduedayofthemonth"; 
	public static final String ParamiMinimumDaysAllowedForDueDayOfMonth = "iminimumdaysallowedforduedayofmonth";
	public static final String ParamiMinimumDaysAllowedForDiscountDueDayOfMonth = "iminimumdaysallowedfordiscountduedayofmonth";
	
	private String m_sTermsCode;
	private String m_sTermsDescription;
	private String m_sLastMaintainedDate;
	private String m_sActive;
	private String m_sDiscountPercentage;
	private String m_sDiscountNumberOfDays;
	private String m_sDiscountDayOfTheMonth;
	private String m_sDueNumberOfDays;
	private String m_sDueDayOfTheMonth;
	private String m_sNewRecord;
	private String m_iminimumdaysallowedforduedayofmonth;
	private String m_iminimumdaysallowedfordiscountduedayofmonth;
	private ArrayList<String> m_sErrorMessageArray = new ArrayList<String> (0);
	private boolean bDebugMode = false;
	
    public APVendorTerms() {
		super();
		initBidVariables();
        }
    
    APVendorTerms(HttpServletRequest req){
		super(req);
		initBidVariables();
		m_sTermsCode = clsManageRequestParameters.get_Request_Parameter(
			APVendorTerms.ParamsTermsCode, req).trim().toUpperCase().replace("&quot;", "\"");
		m_sTermsDescription = clsManageRequestParameters.get_Request_Parameter(
			APVendorTerms.ParamsDescription, req).trim().replace("&quot;", "\"");
		m_sLastMaintainedDate = clsManageRequestParameters.get_Request_Parameter(
			APVendorTerms.ParamdatLastMaintained, req).trim().replace("&quot;", "\"");
		if(m_sLastMaintainedDate.compareToIgnoreCase("") == 0){
			m_sLastMaintainedDate = "00/00/0000";
		}
		if(req.getParameter(APVendorTerms.ParamiActive) == null){
			m_sActive = "0";
		}else{
			m_sActive = "1";
		}
    	m_sDiscountPercentage = clsManageRequestParameters.get_Request_Parameter(
    		APVendorTerms.ParambdDiscountPercent, req).trim().replace("&quot;", "\"");
    	if (m_sDiscountPercentage.compareToIgnoreCase("") == 0){
    		m_sDiscountPercentage = "0.0000";
    	}
    	m_sDiscountNumberOfDays = clsManageRequestParameters.get_Request_Parameter(
    			APVendorTerms.ParamiDiscountNumberOfDays, req).trim().replace("&quot;", "\"");
       	if (m_sDiscountNumberOfDays.compareToIgnoreCase("") == 0){
       		m_sDiscountNumberOfDays = "0";
       	}
       	m_sDiscountDayOfTheMonth = clsManageRequestParameters.get_Request_Parameter(
       			APVendorTerms.ParamiDiscountDayOfTheMonth, req).trim().replace("&quot;", "\"");
       	if (m_sDiscountDayOfTheMonth.compareToIgnoreCase("") == 0){
       		m_sDiscountDayOfTheMonth = "0";
       	}
       	m_sDueNumberOfDays = clsManageRequestParameters.get_Request_Parameter(
       			APVendorTerms.ParamiDueNumberOfDays, req).trim().replace("&quot;", "\"");
       	if (m_sDueNumberOfDays.compareToIgnoreCase("") == 0){
       		m_sDueNumberOfDays = "0";
       	}
       	m_sDueDayOfTheMonth = clsManageRequestParameters.get_Request_Parameter(
       			APVendorTerms.ParamiDueDayOfTheMonth, req).trim().replace("&quot;", "\"");
       	if (m_sDueDayOfTheMonth.compareToIgnoreCase("") == 0){
       		m_sDueDayOfTheMonth = "0";
       	}
       	m_iminimumdaysallowedforduedayofmonth = clsManageRequestParameters.get_Request_Parameter(
       			APVendorTerms.ParamiMinimumDaysAllowedForDueDayOfMonth, req).trim().replace("&quot;", "\"");
       	if (m_iminimumdaysallowedforduedayofmonth.compareToIgnoreCase("") == 0){
       		m_iminimumdaysallowedforduedayofmonth = "0";
       	}
       	m_iminimumdaysallowedfordiscountduedayofmonth = clsManageRequestParameters.get_Request_Parameter(
       			APVendorTerms.ParamiMinimumDaysAllowedForDiscountDueDayOfMonth, req).trim().replace("&quot;", "\"");
       	if (m_iminimumdaysallowedfordiscountduedayofmonth.compareToIgnoreCase("") == 0){
       		m_iminimumdaysallowedfordiscountduedayofmonth = "0";
       	}
       	
		m_sNewRecord = clsManageRequestParameters.get_Request_Parameter(SMMasterEditSelect.SUBMIT_ADD_BUTTON_NAME, req).trim().replace("&quot;", "\"");
    }
    public boolean load (ServletContext context, String sConf, String sUserID, String sUserFullName){
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
    		super.addErrorMessage("Error opening data connection.");
    		return false;
    	}
    	
    	boolean bResult = load (conn);
    	clsDatabaseFunctions.freeConnection(context, conn, "[1547059510]");
    	return bResult;
    	
    }
    public boolean load (Connection conn){
    	return load (m_sTermsCode, conn);
    }
    private boolean load (String sTermsCode, Connection conn){

    	sTermsCode = sTermsCode.trim();
    	if (sTermsCode.compareToIgnoreCase("") == 0){
    		super.addErrorMessage("Terms code cannot be blank.");
    		return false;
    	}
		
		String SQL = " SELECT * FROM " + SMTableicvendorterms.TableName
			+ " WHERE ("
				+ SMTableicvendorterms.sTermsCode + " = '" + sTermsCode + "'"
			+ ")";
		if (bDebugMode){
			System.out.println("In " + this.toString() + " - load SQL = " + SQL);
		}
		try {
			ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
			if (rs.next()) {
				//Load the variables here:
		    	m_sTermsCode = rs.getString(SMTableicvendorterms.sTermsCode).trim();
		    	m_sTermsDescription = rs.getString(SMTableicvendorterms.sDescription).trim();
		    	m_sLastMaintainedDate = clsDateAndTimeConversions.resultsetDateStringToString(
					rs.getString(SMTableicvendorterms.datLastMaintained));
		    	m_sActive = Long.toString(rs.getLong(SMTableicvendorterms.iActive));
		    	m_sDiscountPercentage = clsManageBigDecimals.BigDecimalToScaledFormattedString(
		    		SMTableicvendorterms.bdDiscountPercentScale, 
		    		rs.getBigDecimal(SMTableicvendorterms.bdDiscountPercent));
		    	m_sDiscountNumberOfDays = Long.toString(
		    		rs.getLong(SMTableicvendorterms.iDiscountNumberOfDays));
		    	m_sDiscountDayOfTheMonth = Long.toString(
			    	rs.getLong(SMTableicvendorterms.iDiscountDayOfTheMonth));
		    	m_sDueNumberOfDays = Long.toString(
				    rs.getLong(SMTableicvendorterms.iDueNumberOfDays));
		    	m_sDueDayOfTheMonth = Long.toString(
				    rs.getLong(SMTableicvendorterms.iDueDayOfTheMonth));
		    	m_iminimumdaysallowedforduedayofmonth = Long.toString(
				    rs.getLong(SMTableicvendorterms.iminimumdaysallowedforduedayofmonth));
		    	m_iminimumdaysallowedfordiscountduedayofmonth = Long.toString(
					rs.getLong(SMTableicvendorterms.iminimumdaysallowedfordiscountduedayofmonth));
				rs.close();
			} else {
				super.addErrorMessage("No " + ParamObjectName + " found for : '" + sTermsCode
						+ "'");
				rs.close();
				return false;
			}
		} catch (SQLException e) {
			super.addErrorMessage("Error reading " + ParamObjectName + " for : '" + sTermsCode
					+ "' - " + e.getMessage());
			return false;
		}
		return true;
    }
    
    public boolean save_without_data_transaction 
    			   (ServletContext context, 
    				String sConf, 
    				String sUser, 
    				String sUserID, 
    				String sUserFullName){
    	
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
    		super.addErrorMessage("Error opening data connection.");
    		return false;
    	}
    	
    	boolean bResult = save_without_data_transaction (conn, sUser);
    	clsDatabaseFunctions.freeConnection(context, conn, "[1547059509]");
    	return bResult;	
    	
    }
    public boolean save_without_data_transaction (Connection conn, String sUser){

    	if (!validate_entry_fields(conn)){
    		return false;
    	}
    	String SQL = "";

		//If it's a new record, do an insert:
		SQL = "INSERT INTO " + SMTableicvendorterms.TableName + " ("
			+ SMTableicvendorterms.bdDiscountPercent
			+ ", " + SMTableicvendorterms.datLastMaintained
			+ ", " + SMTableicvendorterms.iActive
			+ ", " + SMTableicvendorterms.iDiscountDayOfTheMonth
			+ ", " + SMTableicvendorterms.iDiscountNumberOfDays
			+ ", " + SMTableicvendorterms.iDueDayOfTheMonth
			+ ", " + SMTableicvendorterms.iDueNumberOfDays
			+ ", " + SMTableicvendorterms.iminimumdaysallowedforduedayofmonth
			+ ", " + SMTableicvendorterms.iminimumdaysallowedfordiscountduedayofmonth
			+ ", " + SMTableicvendorterms.sDescription
			+ ", " + SMTableicvendorterms.sTermsCode
			+ ") VALUES ("
			+ m_sDiscountPercentage.replace(",", "")
			+ ", NOW()"
			+ ", " + m_sActive
			+ ", " + m_sDiscountDayOfTheMonth
			+ ", " + m_sDiscountNumberOfDays
			+ ", " + m_sDueDayOfTheMonth
			+ ", " + m_sDueNumberOfDays
			+ ", " + m_iminimumdaysallowedforduedayofmonth
			+ ", " + m_iminimumdaysallowedfordiscountduedayofmonth
			+ ", '" + clsDatabaseFunctions.FormatSQLStatement(m_sTermsDescription.trim()) + "'"
			+ ", '" + clsDatabaseFunctions.FormatSQLStatement(m_sTermsCode.trim()) + "'"
			+ ")"
			+ " ON DUPLICATE KEY UPDATE"
			+ " " + SMTableicvendorterms.bdDiscountPercent + " = " + m_sDiscountPercentage.replace(",", "")
			+ ", " + SMTableicvendorterms.datLastMaintained + " = NOW()" 
			+ ", " + SMTableicvendorterms.iActive + " = " + m_sActive
			+ ", " + SMTableicvendorterms.iDiscountDayOfTheMonth + " = " + m_sDiscountDayOfTheMonth
			+ ", " + SMTableicvendorterms.iDiscountNumberOfDays + " = " + m_sDiscountNumberOfDays
			+ ", " + SMTableicvendorterms.iDueDayOfTheMonth + " = " + m_sDueDayOfTheMonth
			+ ", " + SMTableicvendorterms.iDueNumberOfDays + " = " + m_sDueNumberOfDays
			+ ", " + SMTableicvendorterms.iminimumdaysallowedforduedayofmonth + " = " + m_iminimumdaysallowedforduedayofmonth
			+ ", " + SMTableicvendorterms.iminimumdaysallowedfordiscountduedayofmonth + " = " + m_iminimumdaysallowedfordiscountduedayofmonth
			+ ", " + SMTableicvendorterms.sDescription 
			+ " = '" + clsDatabaseFunctions.FormatSQLStatement(m_sTermsDescription.trim()) + "'"
		;

		if (bDebugMode){
			System.out.println("In " + this.toString() + " - save SQL = " + SQL);
		}
		try{
		    Statement stmt = conn.createStatement();
		    stmt.executeUpdate(SQL);
		}catch (Exception ex) {
    		System.out.println(this.toString() + "Could not insert/update " + ParamObjectName 
    				+ " - " + ex.getMessage() + ".<BR>");
    		super.addErrorMessage("Could not insert/update " + ParamObjectName + " with SQL: " + SQL
    				+ " - " + ex.getMessage());
    		return false;
		}
    	
    	return true;
    }

    public boolean delete (ServletContext context, String sConf, String sUserID, String sUserFullName){
    	
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
    		super.addErrorMessage("Error opening data connection.");
    		return false;
    	}
    	
    	boolean bResult = delete (conn);
    	clsDatabaseFunctions.freeConnection(context, conn, "[1547059508]");
    	return bResult;
    	
    }
    public boolean delete (Connection conn){
    	
    	//Validate deletions
    	//If there are any unexported invoices with this set of terms, we can't delete it:
    	String SQL = "SELECT "
    		+ " " + SMTableicpoinvoiceheaders.sterms
    		+ " FROM " + SMTableicpoinvoiceheaders.TableName
    		+ " WHERE ("
    			+ "(" + SMTableicpoinvoiceheaders.sterms + " = '" + m_sTermsCode + "')"
    			+ " AND (" + SMTableicpoinvoiceheaders.lexportsequencenumber + " = 0)"
    		+ ")"
    		;
    	try {
			ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
			if (rs.next()) {
				super.addErrorMessage("Cannot delete vendor terms "
					+ m_sTermsCode
					+ " - "
					+ "there are some unexported invoices using these terms.");
				rs.close();
				return false;
			}else{
				rs.close();
			}
		} catch (SQLException e) {
			super.addErrorMessage("Error getting invoices to delete vendor terms "
					+ m_sTermsCode
					+ " - "
					+ e.getMessage());
		}
    	
    	//Delete terms:
    	if (!clsDatabaseFunctions.start_data_transaction(conn)){
    		super.addErrorMessage("Could not start transaction when deleting vendor terms.");
    		return false;
    	}
    	SQL = "DELETE FROM " + SMTableicvendorterms.TableName
    		+ " WHERE ("
    			+ SMTableicvendorterms.sTermsCode + " = '" + m_sTermsCode + "'"
    		+ ")"
    		;
    	
		try{
		    Statement stmt = conn.createStatement();
		    stmt.executeUpdate(SQL);
		}catch (Exception ex) {
    		System.out.println("In " + this.toString() 
    			+ " Could not delete vendor terms with terms code " + m_sTermsCode + " - " + ex.getMessage());
    		super.addErrorMessage("In " + this.toString() 
        			+ " Could not delete vendor terms with terms code " + m_sTermsCode + " - " + ex.getMessage());
    		return false;
		}
		
		if (!clsDatabaseFunctions.commit_data_transaction(conn)){
			super.addErrorMessage("Could not commit data transaction while vendor terms.");
			clsDatabaseFunctions.rollback_data_transaction(conn);
			return false;
		}
		//Empty the values:
		initBidVariables();
		return true;
    }

    public boolean validate_entry_fields (Connection conn){
        //Validate the entries here:
    	boolean bEntriesAreValid = true;
    	m_sTermsCode = m_sTermsCode.trim().toUpperCase();
        if (m_sTermsCode.length() > SMTableicvendorterms.sTermsCodeLength){
        	super.addErrorMessage("Terms code is too long.");
        	bEntriesAreValid = false;
        	return bEntriesAreValid;
        }
    	
    	m_sTermsDescription = m_sTermsDescription.trim();
        if (m_sTermsDescription.length() > SMTableicvendorterms.sDescriptionLength){
        	super.addErrorMessage("Description is too long.");
        	bEntriesAreValid = false;
        	return bEntriesAreValid;
        }
        
        if (m_sLastMaintainedDate.compareTo(EMPTY_DATE_STRING) != 0){
	        if (!clsDateAndTimeConversions.IsValidDateString("M/d/yyyy", m_sLastMaintainedDate)){
	        	super.addErrorMessage("Last Maintained date '" + m_sLastMaintainedDate + "' is invalid.");
	        	bEntriesAreValid = false;
	        }
        }
        if (
        		(m_sActive.compareToIgnoreCase("0") != 0)
        		&& (m_sActive.compareToIgnoreCase("1") != 0)
        ){
        	super.addErrorMessage("'Active' status (" + m_sActive + ") is invalid.");
        	bEntriesAreValid = false;	
        }
        
		m_sDiscountPercentage = m_sDiscountPercentage.replace(",", "");
        if (m_sDiscountPercentage.compareToIgnoreCase("") == 0){
        	m_sDiscountPercentage = clsManageBigDecimals.BigDecimalToScaledFormattedString(
        		SMTableicvendorterms.bdDiscountPercentScale, BigDecimal.ZERO);
        }
		BigDecimal bdDiscountPercentage = new BigDecimal(0);
        try{
        	bdDiscountPercentage = new BigDecimal(m_sDiscountPercentage);
            if (bdDiscountPercentage.compareTo(BigDecimal.ZERO) < 0){
            	super.addErrorMessage("Discount percentage must be a positive number: " + m_sDiscountPercentage + ".  ");
        		bEntriesAreValid = false;
            }else{
            	m_sDiscountPercentage = clsManageBigDecimals.BigDecimalToScaledFormattedString(
            			SMTableicvendorterms.bdDiscountPercentScale, bdDiscountPercentage);
            }
        }catch(NumberFormatException e){
    		super.addErrorMessage("Invalid discount percentage: '" + m_sDiscountPercentage + "'.  ");
    		bEntriesAreValid = false;
        }
        
        m_sDiscountNumberOfDays = m_sDiscountNumberOfDays.replace(",", "");
        if (m_sDiscountNumberOfDays.compareToIgnoreCase("") == 0){
        	m_sDiscountNumberOfDays = "0";
        }
		int iDiscountNumberOfDays = 0;
        try{
        	iDiscountNumberOfDays = Integer.parseInt(m_sDiscountNumberOfDays);
            if (iDiscountNumberOfDays < 0){
            	super.addErrorMessage("Discount number of days must be positive: " 
            		+ m_sDiscountNumberOfDays + ".");
        		bEntriesAreValid = false;
            }else{
            	m_sDiscountNumberOfDays = Integer.toString(iDiscountNumberOfDays);
            }
        }catch(NumberFormatException e){
    		super.addErrorMessage("Invalid discount number of days: '" + m_sDiscountNumberOfDays + "'.");
    		bEntriesAreValid = false;
        }

        m_sDiscountDayOfTheMonth = m_sDiscountDayOfTheMonth.replace(",", "");
        if (m_sDiscountDayOfTheMonth.compareToIgnoreCase("") == 0){
        	m_sDiscountDayOfTheMonth = "0";
        }
		int iDiscountdayOfTheMonth = 0;
        try{
        	iDiscountdayOfTheMonth = Integer.parseInt(m_sDiscountDayOfTheMonth);
            if (iDiscountdayOfTheMonth < 0){
            	super.addErrorMessage("Discount day of the month must be positive: " 
            		+ m_sDiscountDayOfTheMonth + ".");
        		bEntriesAreValid = false;
            }else{
            	if (iDiscountdayOfTheMonth > 31){
                	super.addErrorMessage("Discount day of the month is invalid: " 
                   		+ m_sDiscountDayOfTheMonth + ".");
                		bEntriesAreValid = false;
            	}else{
            		m_sDiscountDayOfTheMonth = Integer.toString(iDiscountdayOfTheMonth);
            	}
            }
        }catch(NumberFormatException e){
    		super.addErrorMessage("Invalid discount day of the month: '" + m_sDiscountDayOfTheMonth + "'.");
    		bEntriesAreValid = false;
        }
        
        //Either the discount days OR the discount day of the month must be zero:
        if (
        		(iDiscountNumberOfDays > 0)
        		&& (iDiscountdayOfTheMonth > 0)
        ){
        	super.addErrorMessage("Either the number of discount days OR the discount day of the month must be zero - you cannot use both.");
            		bEntriesAreValid = false;
        }
        
        m_sDueNumberOfDays = m_sDueNumberOfDays.replace(",", "");
        if (m_sDueNumberOfDays.compareToIgnoreCase("") == 0){
        	m_sDueNumberOfDays = "0";
        }
		int iDueNumberOfDays = 0;
        try{
        	iDueNumberOfDays = Integer.parseInt(m_sDueNumberOfDays);
            if (iDueNumberOfDays < 0){
            	super.addErrorMessage("Due number of days must be positive: " 
            		+ m_sDueNumberOfDays + ".");
        		bEntriesAreValid = false;
            }else{
            	m_sDueNumberOfDays = Integer.toString(iDueNumberOfDays);
            }
        }catch(NumberFormatException e){
    		super.addErrorMessage("Invalid due number of days: '" + m_sDueNumberOfDays + "'.");
    		bEntriesAreValid = false;
        }
        
        m_sDueDayOfTheMonth = m_sDueDayOfTheMonth.replace(",", "");
        if (m_sDueDayOfTheMonth.compareToIgnoreCase("") == 0){
        	m_sDueDayOfTheMonth = "0";
        }
		int iDueDayOfTheMonth = 0;
        try{
        	iDueDayOfTheMonth = Integer.parseInt(m_sDueDayOfTheMonth);
            if (iDueDayOfTheMonth < 0){
            	super.addErrorMessage("Due day of the month must be positive: " 
            		+ m_sDueDayOfTheMonth + ".");
        		bEntriesAreValid = false;
            }else{
            	if (iDueDayOfTheMonth > 31){
                	super.addErrorMessage("Due day of the month is invalid: " 
                   		+ m_sDueDayOfTheMonth + ".");
               		bEntriesAreValid = false;
            	}else{
            		m_sDueDayOfTheMonth = Integer.toString(iDueDayOfTheMonth);
            	}
            }
        }catch(NumberFormatException e){
    		super.addErrorMessage("Invalid due day of the month: '" + m_sDueDayOfTheMonth + "'.");
    		bEntriesAreValid = false;
        }
        
        //Either the due days OR the due day of the month must be zero:
        if (
        		(iDueNumberOfDays > 0)
        		&& (iDueDayOfTheMonth > 0)
        ){
        	super.addErrorMessage("Either the number of due days OR the due day of the month must be zero - you cannot use both.");
            		bEntriesAreValid = false;
        }
        
    	m_iminimumdaysallowedforduedayofmonth = m_iminimumdaysallowedforduedayofmonth.replace(",", "");
        if (m_iminimumdaysallowedforduedayofmonth.compareToIgnoreCase("") == 0){
        	m_iminimumdaysallowedforduedayofmonth = "0";
        }
		int iminimumdaysallowedfordueday = 0;
        try{
        	iminimumdaysallowedfordueday = Integer.parseInt(m_iminimumdaysallowedforduedayofmonth);
            if (iminimumdaysallowedfordueday < 0){
            	super.addErrorMessage("Minimum days allowed for due day must be zero or greater: " 
            		+ m_iminimumdaysallowedforduedayofmonth + ".");
        		bEntriesAreValid = false;
            }else{
            	m_iminimumdaysallowedforduedayofmonth = Integer.toString(iminimumdaysallowedfordueday);
            }
        }catch(NumberFormatException e){
    		super.addErrorMessage("Invalid minimum days allowed for due day: '" + m_iminimumdaysallowedforduedayofmonth + "'.");
    		bEntriesAreValid = false;
        }

    	m_iminimumdaysallowedfordiscountduedayofmonth = m_iminimumdaysallowedfordiscountduedayofmonth.replace(",", "");
        if (m_iminimumdaysallowedfordiscountduedayofmonth.compareToIgnoreCase("") == 0){
        	m_iminimumdaysallowedfordiscountduedayofmonth = "0";
        }
		int iminimumdaysallowedfordiscountdueday = 0;
        try{
        	iminimumdaysallowedfordiscountdueday = Integer.parseInt(m_iminimumdaysallowedfordiscountduedayofmonth);
            if (iminimumdaysallowedfordiscountdueday < 0){
            	super.addErrorMessage("Minimum days allowed for discount due day must be zero or greater: " 
            		+ m_iminimumdaysallowedfordiscountduedayofmonth + ".");
        		bEntriesAreValid = false;
            }else{
            	m_iminimumdaysallowedfordiscountduedayofmonth = Integer.toString(iminimumdaysallowedfordiscountdueday);
            }
        }catch(NumberFormatException e){
    		super.addErrorMessage("Invalid minimum days allowed for discount due day: '" + m_iminimumdaysallowedfordiscountduedayofmonth + "'.");
    		bEntriesAreValid = false;
        }
        
        //You can't choose 'day of month' for BOTH due dates AND discount dates:
        if (((iDueDayOfTheMonth > 0) && (iDiscountdayOfTheMonth > 0))){
    		super.addErrorMessage("You can't choose a 'day of the month' for BOTH the due date AND the discount date.");
    		bEntriesAreValid = false;
        }
        
        //You can't choose BOTH a number of due days AND a due day of the month, so check that now:
        if (((iDueNumberOfDays > 0) && (iDueDayOfTheMonth > 0))){
    		super.addErrorMessage("You can't choose BOTH a 'due number of days' AND a 'due day of the month'; you have to use one or the other.");
    		bEntriesAreValid = false;
        }
        
        //You can't choose BOTH a number of discount due days AND a discount due day of the month, so check that now:
        if (((iDiscountNumberOfDays > 0) && (iDiscountdayOfTheMonth > 0))){
    		super.addErrorMessage("You can't choose BOTH a 'discount number of days' AND a 'discount day of the month'; you have to use one or the other.");
    		bEntriesAreValid = false;
        }
        
        //You can't set an 'allowed' number of days for the due day, if you haven't chosen to USE a due day of the month:
        if ((iminimumdaysallowedfordiscountdueday > 0) && (iDiscountNumberOfDays > 0)){
    		super.addErrorMessage("You can't set a 'Minimum days allowed for discount day of the month' if you're not using a 'Discount day of the month'.");
    		bEntriesAreValid = false;
        }
        if ((iminimumdaysallowedfordueday > 0) && (iDueNumberOfDays > 0)){
    		super.addErrorMessage("You can't set a 'Minimum days allowed for due day of the month' if you're not using a 'Due day of the month'.");
    		bEntriesAreValid = false;
        }
        return bEntriesAreValid;
    }

    public String getsTermsCode() {
		return m_sTermsCode;
	}

	public void setsTermsCode(String mSTermsCode) {
		m_sTermsCode = mSTermsCode;
	}

	public String getsTermsDescription() {
		return m_sTermsDescription;
	}

	public void setsTermsDescription(String mSTermsDescription) {
		m_sTermsDescription = mSTermsDescription;
	}

	public String getsLastMaintainedDate() {
		return m_sLastMaintainedDate;
	}

	public void setsLastMaintainedDate(String mSLastMaintainedDate) {
		m_sLastMaintainedDate = mSLastMaintainedDate;
	}

	public String getsActive() {
		return m_sActive;
	}

	public void setsActive(String mSActive) {
		m_sActive = mSActive;
	}

	public String getsDiscountPercentage() {
		return m_sDiscountPercentage;
	}

	public void setsDiscountPercentage(String mSDiscountPercentage) {
		m_sDiscountPercentage = mSDiscountPercentage;
	}

	public String getsDiscountNumberOfDays() {
		return m_sDiscountNumberOfDays;
	}

	public void setsDiscountNumberOfDays(String mSDiscountNumberOfDays) {
		m_sDiscountNumberOfDays = mSDiscountNumberOfDays;
	}

	public String getsDiscountDayOfTheMonth() {
		return m_sDiscountDayOfTheMonth;
	}

	public void setsDiscountDayOfTheMonth(String mSDiscountDayOfTheMonth) {
		m_sDiscountDayOfTheMonth = mSDiscountDayOfTheMonth;
	}

	public String getsDueNumberOfDays() {
		return m_sDueNumberOfDays;
	}

	public void setsDueNumberOfDays(String mSDueNumberOfDays) {
		m_sDueNumberOfDays = mSDueNumberOfDays;
	}

	public String getsDueDayOfTheMonth() {
		return m_sDueDayOfTheMonth;
	}

	public void setsDueDayOfTheMonth(String mSDueDayOfTheMonth) {
		m_sDueDayOfTheMonth = mSDueDayOfTheMonth;
	}

	public void setsMinimumDaysAllowedForDueDayOfMonth(String sMinimumDaysAllowedForDueDayOfMonth) {
		m_iminimumdaysallowedforduedayofmonth = sMinimumDaysAllowedForDueDayOfMonth;
	}

	public String getsMinimumDaysAllowedForDueDayOfMonth() {
		return m_iminimumdaysallowedforduedayofmonth;
	}
	
	public void setsMinimumDaysAllowedForDiscountDueDayOfMonth(String sMinimumDaysAllowedForDiscountDueDayOfMonth) {
		m_iminimumdaysallowedfordiscountduedayofmonth = sMinimumDaysAllowedForDiscountDueDayOfMonth;
	}

	public String getsMinimumDaysAllowedForDiscountDueDay() {
		return m_iminimumdaysallowedfordiscountduedayofmonth;
	}
	
	public String getsNewRecord() {
		return m_sNewRecord;
	}

	public void setsNewRecord(String mSNewRecord) {
		m_sNewRecord = mSNewRecord;
	}
	
	public String getObjectName(){
		return ParamObjectName;
	}
	

	public String read_out_debug_data(){
    	String sResult = "  ** " + SMUtilities.getFullClassName(this.toString()) + " read out: ";
    	sResult += "\nTerms code: " + this.getsTermsCode();
    	sResult += "\nTerms desc: " + this.getsTermsDescription();    	
    	sResult += "\nLast maintained: " + this.getsLastMaintainedDate();
    	sResult += "\nActive: " + this.getsActive();
    	sResult += "\nDiscount percentage: " + this.getsDiscountPercentage();
    	sResult += "\nDiscount number of days: " + this.getsDiscountNumberOfDays();
    	sResult += "\nDiscount day of the month: " + this.getsDiscountDayOfTheMonth();
    	sResult += "\nDue number of days: " + this.getsDueNumberOfDays();
    	sResult += "\nDue day of the month: " + this.getsDueDayOfTheMonth();  
    	sResult += "\nMinimum days allowed for due day: " + this.getsMinimumDaysAllowedForDueDayOfMonth();
    	sResult += "\nMinimum days allowed for discount due day: " + this.getsMinimumDaysAllowedForDiscountDueDay();
    	sResult += "\nObject name: " + this.getObjectName();
    	return sResult;
    }

    public void addErrorMessage(String sMsg){
    	super.addErrorMessage(sMsg);
    }
	public String getQueryString(){
		//Particular to the specific class
		String sQueryString = "";
		sQueryString += ParamObjectName + "=" + clsServletUtilities.URLEncode(this.getObjectName());
		sQueryString += "&" + clsMasterEntry.ParambAddingNewEntry + "=" + clsServletUtilities.URLEncode(this.getsNewRecord());
		sQueryString += "&" + APVendorTerms.ParambdDiscountPercent + "=" + clsServletUtilities.URLEncode(this.getsDiscountPercentage());
		sQueryString += "&" + APVendorTerms.ParamdatLastMaintained + "=" + clsServletUtilities.URLEncode(this.getsLastMaintainedDate());
		sQueryString += "&" + APVendorTerms.ParamiActive + "=" + clsServletUtilities.URLEncode(this.getsActive());
		sQueryString += "&" + APVendorTerms.ParamiDiscountDayOfTheMonth + "=" + clsServletUtilities.URLEncode(this.getsDiscountDayOfTheMonth());
		sQueryString += "&" + APVendorTerms.ParamiDiscountNumberOfDays + "=" + clsServletUtilities.URLEncode(this.getsDiscountNumberOfDays());
		sQueryString += "&" + APVendorTerms.ParamiDueDayOfTheMonth + "=" + clsServletUtilities.URLEncode(this.getsDueDayOfTheMonth());
		sQueryString += "&" + APVendorTerms.ParamiDueNumberOfDays + "=" + clsServletUtilities.URLEncode(this.getsDueNumberOfDays());
		sQueryString += "&" + APVendorTerms.ParamsDescription + "=" + clsServletUtilities.URLEncode(this.getsTermsDescription());
		sQueryString += "&" + APVendorTerms.ParamsTermsCode + "=" + clsServletUtilities.URLEncode(this.getsTermsCode());
		sQueryString += "&" + APVendorTerms.ParamiMinimumDaysAllowedForDueDayOfMonth + "=" + clsServletUtilities.URLEncode(this.getsMinimumDaysAllowedForDueDayOfMonth());
		sQueryString += "&" + APVendorTerms.ParamiMinimumDaysAllowedForDiscountDueDayOfMonth + "=" + clsServletUtilities.URLEncode(this.getsMinimumDaysAllowedForDiscountDueDay());
		return sQueryString;
	}

    private void initBidVariables(){
    	m_sTermsCode = "";
    	m_sTermsDescription = "";
    	m_sLastMaintainedDate = clsDateAndTimeConversions.now("MM/dd/yyyy");;
    	m_sActive = "1";
    	m_sDiscountPercentage = "0.0000";
    	m_sDiscountNumberOfDays = "0";
    	m_sDiscountDayOfTheMonth = "0";
    	m_sDueNumberOfDays = "0";
    	m_sDueDayOfTheMonth = "0";
    	m_sNewRecord = "1";
    	m_iminimumdaysallowedforduedayofmonth = "0";
    	m_iminimumdaysallowedfordiscountduedayofmonth = "0";
    	m_sErrorMessageArray = new ArrayList<String> (0);
	}
	public String getErrorMessageString(){
		String s = "";
		for (int i = 0; i < m_sErrorMessageArray.size(); i ++){
			s += "<BR>" + m_sErrorMessageArray.get(i);
		}
		return s;
	}
}
