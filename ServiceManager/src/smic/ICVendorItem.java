package smic;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.servlet.ServletContext;

import smap.APVendor;
import smcontrolpanel.SMUtilities;
import SMDataDefinition.SMTableicitems;
import SMDataDefinition.SMTableicvendoritems;
import ServletUtilities.clsMasterEntry;
import ServletUtilities.clsServletUtilities;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsManageBigDecimals;

public class ICVendorItem extends clsMasterEntry{

	public static final String ParamObjectName = "Vendor item";
	
	//Particular to the specific class
	public static final String sItemNumber = "sitemnumber";
	public static final String sVendor = "svendor";
	public static final String sVendorItemNumber = "svendoritemnumber";
	public static final String sCost = "bdcost";
	public static final String sComment = "scomment";
	
	public static final String Paramsitemnumber = "sitemnumber";
	public static final String Paramsvendor = "svendor";
	public static final String Paramsvendoritemnumber = "svendoritemnumber";
	public static final String Paramscost = "bdcost";
	public static final String Paramscomment = "bcomment";

	private String m_sitemnumber;
	private String m_svendor;
	private String m_svendoritemnumber;
	private String m_scost;
	private String m_scomment;
	private boolean bDebugMode = false;
	
	public ICVendorItem() {
		super();
		initBidVariables();
        }

    public boolean load (ServletContext context, String sDBID, String sUserID, String sUserFullName){
    	Connection conn = clsDatabaseFunctions.getConnection(
    			context, 
    			sDBID, 
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
    	clsDatabaseFunctions.freeConnection(context, conn, "[1547081021]");
    	return bResult;
    	
    }
    public boolean load (Connection conn){
    	return load (m_sitemnumber, m_svendor, conn);
    }
    private boolean load (String sItem, String sVendor, Connection conn){

		String SQL = " SELECT * FROM " + SMTableicvendoritems.TableName
			+ " WHERE ("
				+ "(" + SMTableicvendoritems.sItemNumber + " = '" + sItem + "')"
				+ " AND (" + SMTableicvendoritems.sVendor + " = '" + sVendor + "')"
			+ ")";
		
		try {
			ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
			if (rs.next()) {
				//Load the variables here:
				m_sitemnumber = rs.getString(SMTableicvendoritems.sItemNumber).trim();
				m_svendor = rs.getString(SMTableicvendoritems.sVendor).trim();
				m_svendoritemnumber = rs.getString(SMTableicvendoritems.sVendorItemNumber).trim();
				m_scost = clsManageBigDecimals.BigDecimalToScaledFormattedString(
						SMTableicvendoritems.sCostScale, rs.getBigDecimal(SMTableicvendoritems.sCost));
				m_scomment = rs.getString(SMTableicvendoritems.sComment).trim();
				rs.close();
			} else {
				super.addErrorMessage("No " + ParamObjectName + " found for : '" + sItem + ", Vendor: " + sVendor
						+ "'");
				rs.close();
				return false;
			}
		} catch (SQLException e) {
			super.addErrorMessage("Error reading " + ParamObjectName + " for : '" + sItem + ", Vendor: " + sVendor
					+ "' - " + e.getMessage());
			return false;
		}
		return true;
    }
    public boolean updateVendorItem(
    	String sItem, 
    	String sVendor, 
    	String sVendorItemNumber, 
    	BigDecimal bdUnitCost,
    	String sComment,
    	Connection conn){
    	
    	//If there is no vendor number, just return:
    	sVendor = sVendor.trim();
    	if (sVendor.compareToIgnoreCase("") == 0){
    		return true;
    	}
    	
    	//If the item is NOT an inventory item at all, we don't want to update it:
    	String SQL = "SELECT"
    		+ " " + SMTableicitems.sItemNumber
    		+ " FROM " + SMTableicitems.TableName
    		+ " WHERE ("
    			+ "(" + SMTableicitems.sItemNumber + " = '" + sItem + "')"
    		+ ")"
    	;
    	try {
			ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
			if (!rs.next()){
				rs.close();
				return true;
			}
			rs.close();
		} catch (SQLException e) {
    		super.addErrorMessage("Could not check item data - with SQL: " + SQL
    				+ " - " + e.getMessage());
    		return false;
		}
    	
		SQL = "INSERT INTO "
			+ " " + SMTableicvendoritems.TableName + "(" 
				+ SMTableicvendoritems.sCost
				+ ", " + SMTableicvendoritems.sItemNumber
				+ ", " + SMTableicvendoritems.sVendor
		;
		
		//Don't update the vendor item number if we are feeding it a blank - it might already have a value
    	//and we don't want to erase it:
    	if (sVendorItemNumber.compareToIgnoreCase("") != 0){
    		SQL += ", " + SMTableicvendoritems.sVendorItemNumber;
    	}

    	//Don't update the vendor item comment if we are feeding it a blank - it might already have a value
    	//and we don't want to erase it:
    	if (sComment.compareToIgnoreCase("") != 0){
    		SQL += ", " + SMTableicvendoritems.sComment;
    	}
		SQL += ") VALUES ("
				+ clsManageBigDecimals.BigDecimalToScaledFormattedString(SMTableicvendoritems.sCostScale, bdUnitCost).replace(",", "")
				+ ", '" + clsDatabaseFunctions.FormatSQLStatement(sItem) + "'"
				+ ", '" + sVendor + "'"
				;
	    	if (sVendorItemNumber.compareToIgnoreCase("") != 0){
	    		SQL += ", '" + clsDatabaseFunctions.FormatSQLStatement(sVendorItemNumber) + "'";
	    	}
	    	if (sComment.compareToIgnoreCase("") != 0){
	    		SQL += ", '" + clsDatabaseFunctions.FormatSQLStatement(sComment) + "'";
	    	}
		SQL += ") ON DUPLICATE KEY UPDATE "
				+ SMTableicvendoritems.sCost + " = " 
					+ clsManageBigDecimals.BigDecimalToScaledFormattedString(
						SMTableicvendoritems.sCostScale, bdUnitCost).replace(",", "")
				;
	    	if (sVendorItemNumber.compareToIgnoreCase("") != 0){
	    		SQL += ", " + SMTableicvendoritems.sVendorItemNumber + " = '" 
	    		+ clsDatabaseFunctions.FormatSQLStatement(sVendorItemNumber) + "'";
	    	}
	    	if (sComment.compareToIgnoreCase("") != 0){
	    		SQL += ", " + SMTableicvendoritems.sComment + " = '" 
	    		+ clsDatabaseFunctions.FormatSQLStatement(sComment) + "'";
	    	}
		if (bDebugMode){
			clsServletUtilities.sysprint(this.toString(), "SYSTEM", "updateVendorItem SQL = " + SQL);
		}
		try{
		    Statement stmt = conn.createStatement();
		    stmt.executeUpdate(SQL);
		}catch (Exception ex) {
    		System.out.println(this.toString() + "Could not update vendor item data - " + ex.getMessage());
    		super.addErrorMessage("Could not update vendor item data - with SQL: " + SQL
    				+ " - " + ex.getMessage());
    		return false;
		}
    	
    	return true;
    }
    public boolean save_without_data_transaction (ServletContext context, String sDBID, String sUser,  String sUserID, String sUserFullName){
    	
       	Connection conn = clsDatabaseFunctions.getConnection(
    			context, 
    			sDBID, 
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
    	clsDatabaseFunctions.freeConnection(context, conn, "[1547081022]");
    	return bResult;	
    	
    }
    public boolean save_without_data_transaction (Connection conn, String sUser){

    	if (!validate_entry_fields(conn)){
    		return false;
    	}
    	
    	//If there is no vendor item AND a zero cost, remove the record:
    	if (
    			(this.getsvendoritemnumber().compareToIgnoreCase("") == 0)
    			&& (this.getscomment().compareToIgnoreCase("") == 0)
    	){
    		BigDecimal bdCost = new BigDecimal(this.getscost().replace(",", ""));
    		if (bdCost.compareTo(BigDecimal.ZERO) == 0){
    			//Remove this record:
    			return delete(conn);
    		}
    	}
    	
		String SQL = "INSERT INTO " + SMTableicvendoritems.TableName + " ("
			+ SMTableicvendoritems.sCost
			+ ", " + SMTableicvendoritems.sItemNumber
			+ ", " + SMTableicvendoritems.sVendor
			+ ", " + SMTableicvendoritems.sVendorItemNumber
			+ ", " + SMTableicvendoritems.sComment
			+ ") VALUES ("
			+ m_scost.replace(",", "")
			+ ", '" + clsDatabaseFunctions.FormatSQLStatement(m_sitemnumber.trim()) + "'"
			+ ", '" + clsDatabaseFunctions.FormatSQLStatement(m_svendor.trim()) + "'"
			+ ", '" + clsDatabaseFunctions.FormatSQLStatement(m_svendoritemnumber.trim()) + "'"
			+ ", '" + clsDatabaseFunctions.FormatSQLStatement(m_scomment.trim()) + "'"
			+ ")"
			+ " ON DUPLICATE KEY UPDATE "
			+ SMTableicvendoritems.sCost + " = " + m_scost.replace(",", "")
			+ ", " + SMTableicvendoritems.sVendorItemNumber + " = '" 
				+ clsDatabaseFunctions.FormatSQLStatement(m_svendoritemnumber.trim()) + "'"
			+ ", " + SMTableicvendoritems.sComment + " = '" 
				+ clsDatabaseFunctions.FormatSQLStatement(m_scomment.trim()) + "'"
			;

    	try{
	    	if (!clsDatabaseFunctions.executeSQL(SQL, conn)){
	    		System.out.println(this.toString() + "Could not insert/update " + ParamObjectName + ".<BR>");
	    		super.addErrorMessage("Could not insert/update " + ParamObjectName + " with SQL: " + SQL);
	    		return false;
	    	}
    	}catch(SQLException ex){
    	    super.addErrorMessage("Error inserting " + ParamObjectName + ": " + ex.getMessage());
    	    return false;
    	}
    	return true;
    }

    public boolean delete (ServletContext context, String sDBID, String sUserID, String sUserFullName){
    	
    	Connection conn = clsDatabaseFunctions.getConnection(
    			context, 
    			sDBID, 
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
    	clsDatabaseFunctions.freeConnection(context, conn, "[1547081020]");
    	return bResult;
    	
    }
    public boolean delete (Connection conn){
    	
    	//TODO - Validate deletions
    	//What are the rules - what CAN'T we delete?
    	if (!clsDatabaseFunctions.start_data_transaction(conn)){
			super.addErrorMessage("Error starting data transaction to delete vendor items.");
			return false;
    	}
    	
    	String SQL = "DELETE FROM " + SMTableicvendoritems.TableName
    		+ " WHERE ("
    			+ "(" + SMTableicvendoritems.sItemNumber + " = '" + m_sitemnumber + "')"
    			+ " AND (" + SMTableicvendoritems.sVendor + " = '" + m_svendor + "')"
    		+ ")"
    		;
		try{
		    Statement stmt = conn.createStatement();
		    stmt.executeUpdate(SQL);
		}catch (Exception ex) {
			super.addErrorMessage("Error deleting vendor item with SQL: " + SQL + " - " + ex.getMessage());
			clsDatabaseFunctions.rollback_data_transaction(conn);
			return false;
		}
    	
		if (!clsDatabaseFunctions.commit_data_transaction(conn)){
			super.addErrorMessage("Error starting data transaction to delete vendor item.");
			clsDatabaseFunctions.rollback_data_transaction(conn);
			return false;
		}
		
		//Empty the values:
		initBidVariables();
		return true;
    }
    public boolean validate_entry_fields(
    		ServletContext context, 
			String sDBID, 
			String sUserID,
			String sUserFullName
    		){
    	
    	boolean bResult = true;
    	Connection conn = clsDatabaseFunctions.getConnection(
    			context, 
    			sDBID, 
    			"MySQL", 
    			SMUtilities.getFullClassName(this.toString() + ".validate_entry_fields - user: " 
    			+ sUserID
    			+ " - "
    			+ sUserFullName
    			
    					)
    	);
    	if (conn == null){
    		super.addErrorMessage("Could not get connection to validate entry fields.");
    		return false;
    	}
    	
    	bResult = validate_entry_fields(conn);
    	clsDatabaseFunctions.freeConnection(context, conn, "[1547081023]");
    	
    	return bResult;
    	
    }
    public boolean validate_entry_fields (Connection conn){
        //Validate the entries here:
    	boolean bEntriesAreValid = true;

    	
        //Item number:
    	m_sitemnumber = m_sitemnumber.trim();
    	ICItem item = new ICItem(m_sitemnumber);
        if (!item.load(conn)){
   	       	super.addErrorMessage("Invalid item number: '" + m_sitemnumber + "'.");
   	       	bEntriesAreValid = false;
        }

        //Vendor:
    	m_svendor = m_svendor.trim();
    	APVendor ven = new APVendor();
    	ven.setsvendoracct(m_svendor);
        if (!ven.load(conn)){
   	       	super.addErrorMessage("Invalid vendor number: '" + m_svendor + "'.");
   	       	bEntriesAreValid = false;
        }

        //Item description:
        //m_sitemdescription
        m_svendoritemnumber = m_svendoritemnumber.replace(" ", "");
        //If it's NOT a non-inventory item, get the description
        if (m_svendoritemnumber.length() > SMTableicvendoritems.sVendorItemNumberLength){
        	super.addErrorMessage("Vendor item is too long.");
        	bEntriesAreValid = false;
        }
         
        //Comment:
        if (m_scomment.length() > SMTableicvendoritems.sCommentLength){
        	super.addErrorMessage("Comment is too long - maximum is " 
        		+ SMTableicvendoritems.sCommentLength + " characters.");
        	bEntriesAreValid = false;
        }
        
        m_scost = m_scost.replace(",", "");
        if (m_scost.compareToIgnoreCase("") == 0){
        	m_scost = clsManageBigDecimals.BigDecimalToScaledFormattedString(
            		SMTableicvendoritems.sCostScale, BigDecimal.ZERO);
        }
        BigDecimal bdCost = new BigDecimal(0);
        try{
        	bdCost = new BigDecimal(m_scost);
            if (bdCost.compareTo(BigDecimal.ZERO) < 0){
            	super.addErrorMessage("Cost cannot be negative: " + m_scost + ".  ");
        		bEntriesAreValid = false;
            }else{
            	m_scost = clsManageBigDecimals.BigDecimalToScaledFormattedString(
            			SMTableicvendoritems.sCostScale, bdCost);
            }
        }catch(NumberFormatException e){
    		super.addErrorMessage("Invalid cost: '" + m_scost + "'.  ");
    		bEntriesAreValid = false;
        }
    	return bEntriesAreValid;
    }

    public void addErrorMessage(String sMsg){
    	super.addErrorMessage(sMsg);
    }
	public void setsitemnumber(String sitemnumber) {
		this.m_sitemnumber = sitemnumber;
	}
	public String getsitemnumber() {
		return m_sitemnumber;
	}
	public void setscomment(String scomment) {
		this.m_scomment = scomment;
	}
	public String getscomment() {
		return m_scomment;
	}
	public void setsvendor(String svendor) {
		this.m_svendor = svendor;
	}
	public String getsvendor() {
		return m_svendor;
	}
	public void setsvendoritemnumber(String svendoritemnumber) {
		this.m_svendoritemnumber = svendoritemnumber;
	}
	public String getsvendoritemnumber() {
		return m_svendoritemnumber;
	}
	public void setscost(String scost) {
		this.m_scost = scost;
	}
	public String getscost() {
		return m_scost;
	}
    private void initBidVariables(){
    	m_sitemnumber = "";
    	m_svendor = "";
    	m_svendoritemnumber = "";
    	m_scost = "0.00";
    	m_scomment = "";
		super.initVariables();
		super.setObjectName(ParamObjectName);
    }
    public String read_out_debug_data(){
    	String sResult = "  ** ICVendorItem read out: ";
    	sResult += "\nItem Number: " + this.m_sitemnumber;
    	sResult += "\nVendor: " + this.m_svendor;
    	sResult += "\nVendor Item Number: " + this.m_svendoritemnumber;
    	sResult += "\nCost: " + this.m_scost;
    	sResult += "\nComment: " + this.m_scomment;
    	return sResult;
    }
	public String getQueryString(){
		//Particular to the specific class
		String sQueryString = "";
		sQueryString += "&" + ParamObjectName + "=" 
			+ clsServletUtilities.URLEncode(getObjectName());
		sQueryString += "&" + Paramsitemnumber + "=" 
			+ clsServletUtilities.URLEncode(getsitemnumber());
		sQueryString += "&" + Paramsvendor + "=" 
			+ clsServletUtilities.URLEncode(getsvendor());
		sQueryString += "&" + Paramsvendoritemnumber + "=" 
			+ clsServletUtilities.URLEncode(getsvendoritemnumber());
		sQueryString += "&" + Paramscost + "=" 
			+ clsServletUtilities.URLEncode(getscost());
		sQueryString += "&" + Paramscomment + "=" 
			+ clsServletUtilities.URLEncode(getscomment());
		return sQueryString;
	}
}