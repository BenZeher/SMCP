package smic;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import SMClasses.SMOrderHeader;
import SMDataDefinition.SMTableicitems;
import SMDataDefinition.SMTableicpoheaders;
import SMDataDefinition.SMTableicpolines;
import SMDataDefinition.SMTableicporeceiptheaders;
import SMDataDefinition.SMTableicporeceiptlines;
import SMDataDefinition.SMTableicvendoritems;
import SMDataDefinition.SMTablelocations;
import ServletUtilities.clsMasterEntry;
import ServletUtilities.clsServletUtilities;
import smcontrolpanel.SMUtilities;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsDateAndTimeConversions;
import ServletUtilities.clsManageBigDecimals;
import ServletUtilities.clsManageRequestParameters;

public class ICPOLine extends clsMasterEntry{

   	//Qtys:
	//Qty ordered - gets locked at the first receipt
	//Qty received - total of qty on receipt lines for this po line
	//Qty outstanding - CALCULATION: qty ordered minus qty received (except that it can't go below zero)
	//Qty extra received - CALCULATION: qty received minus qty ordered (can't go below zero)
	
	//Costs:
	//extended cost - gets locked at first receipt
	//extended received cost - total of costs on receipts for this line
	//outstanding cost - extended cost minus received cost (except that it can't go below zero)
	//extra received cost - received cost minus extended cost (except that it can't go below zero)
	
	public static final String ParamObjectName = "Purchase order line";
	
	//Particular to the specific class
	public static final String Paramlid = "lid";
	public static final String Paramlpoheaderid = "lpoheaderid";
	public static final String Paramllinenumber = "llinenumber";
	public static final String Paramsitemnumber = "sitemnumber";
	public static final String Paramslocation = "slocation";
	public static final String Paramsitemdescription = "sitemdescription";
	public static final String Paramsunitofmeasure = "sunitofmeasure";
	public static final String Parambdunitcost = "bdunitcost";
	public static final String Parambdextendedordercost = "bdextendedordercost";
	public static final String Parambdextendedreceivedcost = "bdextendedreceivedcost";
	public static final String Parambdqtyordered = "bdqtyordered";
	public static final String Parambdqtyreceived = "bdqtyreceived";
	public static final String Paramsglexpenseacct = "sglexpenseacct";
	public static final String Paramdatexpected = "datexpected";
	public static final String Paramsvendorsitemnumber = "svendorsitemnumber";
	public static final String Paramsinstructions = "sinstructions";
	public static final String Paraminoninventoryitem = "inoninventoryitem";
	public static final String Parambdnumberoflabels = "bdnumberoflabels";
	public static final String Paramsvendorsitemcomment = "svendorsitemcomment";

	private String m_slid;
	private String m_lpoheaderid;
	private String m_llinenumber;
	private String m_sitemnumber;
	private String m_slocation;
	private String m_sitemdescription;
	private String m_sunitofmeasure;
	private String m_bdunitcost;
	private String m_bdextendedordercost;
	private String m_bdextendedreceivedcost;
	private String m_bdqtyordered;
	private String m_bdqtyreceived;
	private String m_sglexpenseacct;
	private String m_svendorsitemnumber;
	//This does NOT get saved:
	private String m_svendorsitemcomment;
	private String m_sinstructions;
	private String m_datexpected;
	private String m_snoninventoryitem;
	private String m_snumberoflabels;
	private boolean bDebugMode = false;
	
	public ICPOLine() {
		super();
		initBidVariables();
        }

	ICPOLine (HttpServletRequest req){
		super(req);
		initBidVariables();
		m_slid = clsManageRequestParameters.get_Request_Parameter(ICPOLine.Paramlid, req).trim();
		if (m_slid.compareToIgnoreCase("") == 0){
			m_slid = "-1";
		}
		m_lpoheaderid = clsManageRequestParameters.get_Request_Parameter(ICPOLine.Paramlpoheaderid, req).trim();
		m_llinenumber = clsManageRequestParameters.get_Request_Parameter(ICPOLine.Paramllinenumber, req).trim();
		if (m_llinenumber.compareToIgnoreCase("") == 0){
			m_llinenumber = "-1";
		}
		m_sitemnumber = clsManageRequestParameters.get_Request_Parameter(ICPOLine.Paramsitemnumber, req).trim().toUpperCase();
		m_slocation = clsManageRequestParameters.get_Request_Parameter(ICPOLine.Paramslocation, req).trim().toUpperCase();
		m_sitemdescription = clsManageRequestParameters.get_Request_Parameter(ICPOLine.Paramsitemdescription, req).trim();
		m_sunitofmeasure = clsManageRequestParameters.get_Request_Parameter(ICPOLine.Paramsunitofmeasure, req).trim().toUpperCase();
		m_bdunitcost = clsManageRequestParameters.get_Request_Parameter(ICPOLine.Parambdunitcost, req).trim();
		if (m_bdunitcost.compareToIgnoreCase("") == 0){
			m_bdunitcost = clsManageBigDecimals.BigDecimalToScaledFormattedString(SMTableicpolines.bdunitcostScale, BigDecimal.ZERO);
		}
		m_bdextendedordercost = clsManageRequestParameters.get_Request_Parameter(ICPOLine.Parambdextendedordercost, req).trim();
		if (m_bdextendedordercost.compareToIgnoreCase("") == 0){
			m_bdextendedordercost = clsManageBigDecimals.BigDecimalToScaledFormattedString(SMTableicpolines.bdextendedordercostScale, BigDecimal.ZERO);
		}
		m_bdextendedreceivedcost = clsManageRequestParameters.get_Request_Parameter(ICPOLine.Parambdextendedreceivedcost, req).trim();
		if (m_bdextendedreceivedcost.compareToIgnoreCase("") == 0){
			m_bdextendedreceivedcost = clsManageBigDecimals.BigDecimalToScaledFormattedString(SMTableicpolines.bdextendedreceivedcostScale, BigDecimal.ZERO);
		}

		m_bdqtyordered = clsManageRequestParameters.get_Request_Parameter(ICPOLine.Parambdqtyordered, req).trim();
		if (m_bdqtyordered.compareToIgnoreCase("") == 0){
			m_bdqtyordered = clsManageBigDecimals.BigDecimalToScaledFormattedString(SMTableicpolines.bdqtyorderedScale, BigDecimal.ZERO);
		}
		
		m_snumberoflabels = clsManageRequestParameters.get_Request_Parameter(ICPOLine.Parambdnumberoflabels, req).trim();
		if (m_snumberoflabels.compareToIgnoreCase("") == 0){
			m_snumberoflabels = clsManageBigDecimals.BigDecimalToScaledFormattedString(SMTableicitems.bdnumberoflabelsScale, BigDecimal.ONE);
		}
		
		m_bdqtyreceived = clsManageRequestParameters.get_Request_Parameter(ICPOLine.Parambdqtyreceived, req).trim();
		if (m_bdqtyreceived.compareToIgnoreCase("") == 0){
			m_bdqtyreceived = clsManageBigDecimals.BigDecimalToScaledFormattedString(SMTableicpolines.bdqtyreceivedScale, BigDecimal.ZERO);
		}
		m_sglexpenseacct = clsManageRequestParameters.get_Request_Parameter(ICPOLine.Paramsglexpenseacct, req).trim();
		m_datexpected = clsManageRequestParameters.get_Request_Parameter(ICPOLine.Paramdatexpected, req).trim();
		if (m_datexpected.compareToIgnoreCase("") == 0){
			m_datexpected = EMPTY_DATE_STRING;
		}
		m_svendorsitemnumber = clsManageRequestParameters.get_Request_Parameter(ICPOLine.Paramsvendorsitemnumber, req).trim().toUpperCase();
		m_svendorsitemcomment = clsManageRequestParameters.get_Request_Parameter(ICPOLine.Paramsvendorsitemcomment, req).trim();
		m_sinstructions = clsManageRequestParameters.get_Request_Parameter(ICPOLine.Paramsinstructions, req).trim();
		m_snoninventoryitem = clsManageRequestParameters.get_Request_Parameter(ICPOLine.Paraminoninventoryitem, req).trim().toUpperCase();
		if (m_snoninventoryitem.compareToIgnoreCase("") == 0){
			m_snoninventoryitem = "0";
		}
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
    	clsDatabaseFunctions.freeConnection(context, conn, "[1547080922]");
    	return bResult;
    	
    }
    public boolean load (Connection conn){
    	return load (m_slid, conn);
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
    	
		String SQL = " SELECT * FROM " + SMTableicpolines.TableName
			+ " WHERE ("
				+ SMTableicpolines.lid + " = " + sID
			+ ")";
		
		try {
			ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
			if (rs.next()) {
				//Load the variables here:
				m_slid = Long.toString(rs.getLong(SMTableicpolines.lid));
				m_lpoheaderid = Long.toString(rs.getLong(SMTableicpolines.lpoheaderid));
				m_llinenumber = Long.toString(rs.getLong(SMTableicpolines.llinenumber));
				m_sitemnumber = rs.getString(SMTableicpolines.sitemnumber).trim();
				m_slocation = rs.getString(SMTableicpolines.slocation).trim();
				m_sitemdescription = rs.getString(SMTableicpolines.sitemdescription).trim();
				m_sunitofmeasure = rs.getString(SMTableicpolines.sunitofmeasure).trim();
				m_bdunitcost = clsManageBigDecimals.BigDecimalToScaledFormattedString(
						SMTableicpolines.bdunitcostScale, rs.getBigDecimal(SMTableicpolines.bdunitcost));
				m_bdextendedordercost = clsManageBigDecimals.BigDecimalToScaledFormattedString(
						SMTableicpolines.bdextendedordercostScale, rs.getBigDecimal(SMTableicpolines.bdextendedordercost));
				m_bdextendedreceivedcost = clsManageBigDecimals.BigDecimalToScaledFormattedString(
						SMTableicpolines.bdextendedreceivedcostScale, rs.getBigDecimal(SMTableicpolines.bdextendedreceivedcost));
				m_bdqtyordered = clsManageBigDecimals.BigDecimalToScaledFormattedString(
						SMTableicpolines.bdqtyorderedScale, rs.getBigDecimal(SMTableicpolines.bdqtyordered));
				m_bdqtyreceived = clsManageBigDecimals.BigDecimalToScaledFormattedString(
						SMTableicpolines.bdqtyreceivedScale, rs.getBigDecimal(SMTableicpolines.bdqtyreceived));
				m_sglexpenseacct = rs.getString(SMTableicpolines.sglexpenseacct).trim();
				m_datexpected = clsDateAndTimeConversions.resultsetDateStringToString(
						rs.getString(SMTableicpolines.datexpected));
				m_svendorsitemnumber = rs.getString(SMTableicpolines.svendorsitemnumber).trim();
				m_sinstructions = rs.getString(SMTableicpolines.sinstructions).trim();
				m_snoninventoryitem = Long.toString(rs.getLong(SMTableicpolines.lnoninventoryitem));
				rs.close();
			} else {
				super.addErrorMessage("No " + ParamObjectName + " found for : '" + sID
						+ "'");
				rs.close();
				return false;
			}
		} catch (SQLException e) {
			super.addErrorMessage("Error reading " + ParamObjectName + " for : '" + sID
					+ "' - " + e.getMessage());
			return false;
		}
		
		//Load the number of labels, if it's an inventory item:
		if (this.getsnoninventoryitem().compareToIgnoreCase("0") == 0){
			SQL = "SELECT"
				+ " " + SMTableicitems.bdnumberoflabels
				+ " FROM " + SMTableicitems.TableName
				+ " WHERE ("
					+ "(" + SMTableicitems.sItemNumber + "='" + this.getsitemnumber() + "')"
				+ ")"
			;
			
			try {
				ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
				if (rs.next()) {
					m_snumberoflabels = clsManageBigDecimals
							.BigDecimalToScaledFormattedString(
									SMTableicitems.bdnumberoflabelsScale,
									rs
											.getBigDecimal(SMTableicitems.bdnumberoflabels));
				} else {
					m_snumberoflabels = clsManageBigDecimals
							.BigDecimalToScaledFormattedString(
									SMTableicitems.bdnumberoflabelsScale,
									BigDecimal.ONE);
				}
				rs.close();
			} catch (SQLException e) {
				m_snumberoflabels = clsManageBigDecimals
				.BigDecimalToScaledFormattedString(
						SMTableicitems.bdnumberoflabelsScale,
						BigDecimal.ONE);
			}
			//Also, if it's an inventory item, load the vendor item info:
			// TJR - 6/16/2017 We do NOT want to load the vendor's item number from the 'master' table automatically here.  Instead,
			// we want to display what's actually ON the PO line, and the user can choose to update it from the 'master' vendor's
			// item table by updating the item information on the screen:
			//if (!updateVendorItem(conn)){
			//	return false;
			//}
		}
		return true;
    }
    
    public boolean save_without_data_transaction (ServletContext context, String sConf, String sUser , String sUserID, String sUserFullName){
    	
       	Connection conn = clsDatabaseFunctions.getConnection(
    			context, 
    			sConf, 
    			"MySQL", 
    			this.toString() + " - user: " + sUserID 
    			+ " - "
    			+ sUserFullName
    			);
    	
    	if (conn == null){
    		super.addErrorMessage("Error opening data connection.");
    		return false;
    	}
    	
    	boolean bResult = save_without_data_transaction (conn, sUser);
    	clsDatabaseFunctions.freeConnection(context, conn, "[1547080923]");
    	return bResult;	
    	
    }
    public boolean save_without_data_transaction (Connection conn, String sUserFullName){

    	if (!validate_entry_fields(conn)){
    		return false;
    	}
    	
    	//If it's a new line, get the next available line number for it:
    	if (m_llinenumber.compareToIgnoreCase("-1") == 0){
    		String SQL = "SELECT " + SMTableicpolines.llinenumber
    			+ " FROM " + SMTableicpolines.TableName
    			+ " WHERE ("
    				+ "(" + SMTableicpolines.lpoheaderid + " = " + m_lpoheaderid + ")"
    			+ ")"
    			+ " ORDER BY " + SMTableicpolines.llinenumber + " DESC LIMIT 1"
    			;
    		
    		try {
				ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
				if (rs.next()){
					m_llinenumber = Long.toString(rs.getLong(SMTableicpolines.llinenumber) + 1L);
				}else{
					m_llinenumber = "1";
				}
				rs.close();
			} catch (SQLException e) {
				super.addErrorMessage("Error getting next line number - " + e.getMessage());
	        	return false;
			}
    	}
		String SQL = "INSERT INTO " + SMTableicpolines.TableName + " ("
			+ SMTableicpolines.bdextendedordercost
			+ ", " + SMTableicpolines.bdextendedreceivedcost
			+ ", " + SMTableicpolines.bdqtyordered
			+ ", " + SMTableicpolines.bdqtyreceived
			+ ", " + SMTableicpolines.bdunitcost
			+ ", " + SMTableicpolines.datexpected
			+ ", " + SMTableicpolines.llinenumber
			+ ", " + SMTableicpolines.lpoheaderid
			+ ", " + SMTableicpolines.sglexpenseacct
			+ ", " + SMTableicpolines.sitemdescription
			+ ", " + SMTableicpolines.sitemnumber
			+ ", " + SMTableicpolines.slocation
			+ ", " + SMTableicpolines.sunitofmeasure
			+ ", " + SMTableicpolines.svendorsitemnumber
			+ ", " + SMTableicpolines.sinstructions
			+ ", " + SMTableicpolines.lnoninventoryitem
			+ ") VALUES ("
			+ m_bdextendedordercost.replace(",", "")
			+ ", " + m_bdextendedreceivedcost.replace(",", "")
			+ ", " + m_bdqtyordered.replace(",", "")
			+ ", " + m_bdqtyreceived.replace(",", "")
			+ ", " + m_bdunitcost.replace(",", "")
			+ ", '" + clsDateAndTimeConversions.stdDateStringToSQLDateString(m_datexpected) + "'"
			+ ", " + m_llinenumber
			+ ", " + m_lpoheaderid
			+ ", '" + clsDatabaseFunctions.FormatSQLStatement(m_sglexpenseacct.trim()) + "'"
			+ ", '" + clsDatabaseFunctions.FormatSQLStatement(m_sitemdescription.trim()) + "'"
			+ ", '" + clsDatabaseFunctions.FormatSQLStatement(m_sitemnumber.trim()) + "'"
			+ ", '" + clsDatabaseFunctions.FormatSQLStatement(m_slocation.trim()) + "'"
			+ ", '" + clsDatabaseFunctions.FormatSQLStatement(m_sunitofmeasure.trim()) + "'"
			+ ", '" + clsDatabaseFunctions.FormatSQLStatement(m_svendorsitemnumber.trim()) + "'"
			+ ", '" + clsDatabaseFunctions.FormatSQLStatement(m_sinstructions.trim()) + "'"
			+ ", " + m_snoninventoryitem
			+ ")"
			+ " ON DUPLICATE KEY UPDATE "
			+ SMTableicpolines.bdextendedordercost + " = " + m_bdextendedordercost.replace(",", "")
			+ ", " + SMTableicpolines.bdextendedreceivedcost + " = " + m_bdextendedreceivedcost.replace(",", "")
			+ ", " + SMTableicpolines.bdqtyordered + " = " + m_bdqtyordered.replace(",", "")
			+ ", " + SMTableicpolines.bdqtyreceived + " = " + m_bdqtyreceived.replace(",", "")
			+ ", " + SMTableicpolines.bdunitcost + " = " + m_bdunitcost.replace(",", "")
			+ ", " + SMTableicpolines.datexpected + " = '" 
				+ clsDateAndTimeConversions.stdDateStringToSQLDateString(m_datexpected) + "'"
			+ ", " + SMTableicpolines.sglexpenseacct + " = '" 
				+ clsDatabaseFunctions.FormatSQLStatement(m_sglexpenseacct.trim()) + "'"
			+ ", " + SMTableicpolines.sitemdescription + " = '" 
				+ clsDatabaseFunctions.FormatSQLStatement(m_sitemdescription.trim()) + "'"
			+ ", " + SMTableicpolines.sitemnumber + " = '" 
				+ clsDatabaseFunctions.FormatSQLStatement(m_sitemnumber.trim()) + "'"
			+ ", " + SMTableicpolines.slocation + " = '" 
				+ clsDatabaseFunctions.FormatSQLStatement(m_slocation.trim()) + "'"
			+ ", " + SMTableicpolines.sunitofmeasure + " = '" 
				+ clsDatabaseFunctions.FormatSQLStatement(m_sunitofmeasure.trim()) + "'"
			+ ", " + SMTableicpolines.svendorsitemnumber + " = '"
				+ clsDatabaseFunctions.FormatSQLStatement(m_svendorsitemnumber.trim()) + "'"
			+ ", " + SMTableicpolines.sinstructions + " = '"
				+ clsDatabaseFunctions.FormatSQLStatement(m_sinstructions.trim()) + "'"
			+ ", " + SMTableicpolines.lnoninventoryitem + " = "
				+ m_snoninventoryitem
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
		SQL = "SELECT"
			+ " " + SMTableicpolines.lid
			+ " FROM " + SMTableicpolines.TableName
			+ " WHERE ("
				+ "(" + SMTableicpolines.lpoheaderid + " = " + m_lpoheaderid + ")"
				+ " AND (" + SMTableicpolines.llinenumber + " = " + m_llinenumber + ")"
			+ ")"
			;
		try {
			ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
			if (rs.next()) {
				m_slid = Long.toString(rs.getLong(SMTableicpolines.lid));
			} else {
				super.addErrorMessage("Could not get last ID number with SQL: " + SQL);
				return false;
			}
			rs.close();
		} catch (SQLException e) {
			super.addErrorMessage("Could not get last ID number - with SQL: " + SQL + " - " + e.getMessage());
			return false;
		}
		// If something went wrong, we can't get the last ID:
		if (m_slid.compareToIgnoreCase("-1") == 0) {
			super.addErrorMessage("Could not get last ID number.");
			return false;
		}
    	
		//Update the PO status here:
		ICPOHeader po = new ICPOHeader();
		po.setsID(this.getspoheaderid());
		
		if (!po.updatePOStatus(conn)){
			super.addErrorMessage("Could not get update PO status - " + po.getErrorMessages());
			return false;
		}
		
		//Update the number of labels on the IC item, if necessary:
		if (this.getsnoninventoryitem().compareToIgnoreCase("0") == 0){
			SQL = "UPDATE"
				+ " " + SMTableicitems.TableName
				+ " SET " + SMTableicitems.bdnumberoflabels
				+ " = " + m_snumberoflabels.replace(",", "")
				+ " WHERE ("
					+ "(" + SMTableicitems.sItemNumber + " = '" + this.getsitemnumber() + "')"
				+ ")"
				;
			
			try {
				if (!clsDatabaseFunctions.executeSQL(SQL, conn)) {
					super.addErrorMessage("Error updating number of labels.");
					return false;
				}
			} catch (SQLException e) {
				super.addErrorMessage("Error updating number of labels - " + e.getMessage());
				return false;
			}
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
    	clsDatabaseFunctions.freeConnection(context, conn, "[1547080921]");
    	return bResult;
    	
    }
    public boolean delete (Connection conn){
    	
    	//TODO - Validate deletions - tie this to the other line numbers and po header
    	//What are the rules - what CAN'T we delete?
    	if (!clsDatabaseFunctions.start_data_transaction(conn)){
			super.addErrorMessage("Error starting data transaction to delete po line.");
			return false;
    	}
    	
    	String SQL = "SELECT"
    		+ " " + SMTableicporeceiptlines.TableName + "." + SMTableicporeceiptlines.lid
    		+ " FROM " + SMTableicporeceiptlines.TableName
    		+ " LEFT JOIN " + SMTableicporeceiptheaders.TableName
    		+ " ON " + SMTableicporeceiptlines.TableName + "." + SMTableicporeceiptlines.lreceiptheaderid + " = "
    		+ SMTableicporeceiptheaders.TableName + "." + SMTableicporeceiptheaders.lid
    		+ " WHERE ("
    			+ "(" + SMTableicporeceiptlines.TableName + "." + SMTableicporeceiptlines.lpolineid + " = " + getsID() + ")"
    			+ " AND (" + SMTableicporeceiptlines.TableName + "." + SMTableicporeceiptlines.bdqtyreceived + " != 0.0000)"
    			+ " AND (" + SMTableicporeceiptlines.TableName + "." + SMTableicporeceiptlines.bdextendedcost + " != 0.00)"
    			+ " AND (" + SMTableicporeceiptheaders.TableName + "." + SMTableicporeceiptheaders.lstatus + " != " + Integer.toString(SMTableicporeceiptheaders.STATUS_DELETED) + ")"
    		+ ")"
    	;
    	try {
    		ResultSet rsReceiptlines = clsDatabaseFunctions.openResultSet(SQL, conn);
			if (rsReceiptlines.next()){
				addErrorMessage("PO Line cannot be deleted - there is a receipt entered against it.");
				rsReceiptlines.close();
				return false;
			}else{
				rsReceiptlines.close();
			}
		} catch (SQLException e) {
			addErrorMessage("Error checking for PO Line deletion - " + e.getMessage());
			return false;
		}
    	
    	SQL = "DELETE FROM " + SMTableicpolines.TableName
    		+ " WHERE ("
    			+ "(" + SMTableicpolines.lid + " = " + m_slid + ")"
    		+ ")"
    		;
		try{
		    Statement stmt = conn.createStatement();
		    stmt.executeUpdate(SQL);
		}catch (Exception ex) {
			super.addErrorMessage("Error deleting po line with SQL: " + SQL + " - " + ex.getMessage());
			return false;
		}
    	
		//Update the other lines and PO header as needed:
		ICPOHeader pohead = new ICPOHeader();
		pohead.setsID(m_lpoheaderid);
		if (!pohead.updatePOStatus(conn)){
			super.addErrorMessage("Error updating PO status - " + pohead.getErrorMessages());
			return false;
		}
		
		if (!pohead.updateLineNumbersAfterLineDeletion(conn)){
			super.addErrorMessage("Error updating PO lines - " + pohead.getErrorMessages());
			return false;
		}
		
		if (!clsDatabaseFunctions.commit_data_transaction(conn)){
			super.addErrorMessage("Error starting data transaction to delete po line.");
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
			String sUserFullName){
    	
    	boolean bResult = true;
    	Connection conn = clsDatabaseFunctions.getConnection(
    			context, 
    			sDBID, 
    			"MySQL", 
    			SMUtilities.getFullClassName(this.toString() + ".validate_entry_fields - user: " + sUserID
    			+ " - "
    			+ sUserFullName
    					)
    	);
    	if (conn == null){
    		super.addErrorMessage("Could not get connection to validate entry fields.");
    		return false;
    	}
    	
    	bResult = validate_entry_fields(conn);
    	clsDatabaseFunctions.freeConnection(context, conn, "[1547080924]");
    	
    	return bResult;
    	
    }
    public boolean validate_entry_fields (Connection conn){
        //Validate the entries here:
    	boolean bEntriesAreValid = true;

    	long lID;
		try {
			lID = Long.parseLong(m_slid);
		} catch (NumberFormatException e) {
        	super.addErrorMessage("Invalid ID: '" + m_slid + "'.");
        	bEntriesAreValid = false;
        	return bEntriesAreValid;
		}
    	
    	if (lID < -1){
        	super.addErrorMessage("Invalid ID: '" + m_slid + "'.");
        	bEntriesAreValid = false;
        	return bEntriesAreValid;
    	}
    	
    	//PO Header ID:
		try {
			lID = Long.parseLong(m_lpoheaderid);
		} catch (NumberFormatException e) {
        	super.addErrorMessage("Invalid PO Header ID: '" + m_lpoheaderid + "'.");
        	bEntriesAreValid = false;
        	return bEntriesAreValid;
		}
    	
    	if ((lID < -1) || (lID == 0)){
        	super.addErrorMessage("Invalid PO Header ID: '" + m_lpoheaderid + "'.");
        	bEntriesAreValid = false;
        	return bEntriesAreValid;
    	}
    	
    	//Verify that the PO has a vendor, a bill to, and a ship to before a line can be saved:
    	if (lID > 0){
    		ICPOHeader pohead = new ICPOHeader();
    		pohead.setsID(m_lpoheaderid);
    		if (!pohead.load(conn)){
    			super.addErrorMessage("Could not load PO header to check entries - " + pohead.getErrorMessages() + ".");
            	bEntriesAreValid = false;
            	return bEntriesAreValid;
    		}
    		if (pohead.getsvendor().trim().compareToIgnoreCase("") == 0){
    			super.addErrorMessage("PO must have a valid vendor before adding any lines.");
            	bEntriesAreValid = false;
    		}
    		if (pohead.getsbillname().trim().compareToIgnoreCase("") == 0){
    			super.addErrorMessage("PO must have a valid bill-to name before adding any lines.");
            	bEntriesAreValid = false;
    		}
    		if (pohead.getsshipname().trim().compareToIgnoreCase("") == 0){
    			super.addErrorMessage("PO must have a valid ship-to name before adding any lines.");
            	bEntriesAreValid = false;
    		}
    	}
    	
    	//Line number:
		try {
			lID = Long.parseLong(m_llinenumber);
		} catch (NumberFormatException e) {
        	super.addErrorMessage("Invalid PO line number: '" + m_llinenumber + "'.");
        	bEntriesAreValid = false;
        	return bEntriesAreValid;
		}
    	
    	if ((lID < -1) || (lID == 0)){
        	super.addErrorMessage("Invalid PO line number: '" + m_llinenumber + "'.");
        	bEntriesAreValid = false;
        	return bEntriesAreValid;
    	}
    	
    	if (
   			(m_snoninventoryitem.compareToIgnoreCase("0") != 0)
   			&& (m_snoninventoryitem.compareToIgnoreCase("1") != 0)
    	){
   	       	super.addErrorMessage("Invalid value for 'non inventory item: '" + m_snoninventoryitem + "'.");
   	       	bEntriesAreValid = false;
    	}
    	
        //Item number:
    	m_sitemnumber = m_sitemnumber.trim();
    	
    	boolean bIsStockInventoryItem = false;
    	ICItem item = new ICItem(m_sitemnumber);
    	
    	//Try to load the item from icitems:
        if (!item.load(conn)){
        	//If it CAN'T be loaded:
        	//IF it's supposed to be an inventory item:
        	if (getsnoninventoryitem().compareToIgnoreCase("0") == 0){
	   	       	super.addErrorMessage("This item is not in inventory: '" + m_sitemnumber + "'.");
	   	       	bEntriesAreValid = false;
        	}
        }else{
        	
        	if(item.getCannotBePurchasedFlag().compareToIgnoreCase("1") == 0) {
        		super.addErrorMessage("Item "+m_sitemnumber+" cannot be purchased");
        		bEntriesAreValid = false;
        	}
        	
        	
        	//If it CAN be loaded, then if it's supposed to be a NON-inventory item, we can't allow that item number:
        	if (getsnoninventoryitem().compareToIgnoreCase("1") == 0){
       	       	super.addErrorMessage("Invalid item number - you've indicated a 'Non-inventory' item,"
       	       		+ " but this item number IS currently in inventory: '" + m_sitemnumber + "'.");
       	       	bEntriesAreValid = false;
        	}else{
        		//If the item can be loaded, and if it's NOT a 'non-inventory' item, then flag it as stock or non-stock inventory:
        		bIsStockInventoryItem = item.getNonStockItem().compareToIgnoreCase("0") == 0;
        	}
        	//[1519070245]
            // TJR - 2/20/2018 - removed this constraint - we'll allow NON-STOCK inventory to be ordered now:
        	//if (item.getNonStockItem().compareToIgnoreCase("1") == 0){
       	    //   	super.addErrorMessage("Item number: '" + m_sitemnumber + "' is a NON-STOCK item.");
       	    //   	bEntriesAreValid = false;
           // }
        }
        
    	
        if (m_sitemnumber.length() > SMTableicpolines.sitemnumberLength){
        	super.addErrorMessage("Item number is too long.");
        	bEntriesAreValid = false;
        }
        
        //Item description:
        //m_sitemdescription
        m_sitemdescription = m_sitemdescription.trim();
        //If it's NOT a non-inventory item, get the description
        if (m_snoninventoryitem.compareToIgnoreCase("0") == 0){
        	//Get the description from the item:
        	m_sitemdescription = item.getItemDescription();
        }
        if (m_sitemdescription.length() > SMTableicpolines.sitemdescriptionLength){
        	super.addErrorMessage("Item description is too long.");
        	bEntriesAreValid = false;
        }
        
        //Unit of measure:
        if (m_snoninventoryitem.compareToIgnoreCase("0") == 0){
        	m_sunitofmeasure = item.getCostUnitOfMeasure();
        }
        m_sunitofmeasure = m_sunitofmeasure.trim();
        if (m_sunitofmeasure.length() > SMTableicpolines.sunitofmeasureLength){
        	super.addErrorMessage("Unit of measure is too long.");
        	bEntriesAreValid = false;
        }
        if (m_sunitofmeasure.compareToIgnoreCase("") == 0){
        	super.addErrorMessage("Unit of measure cannot be blank.");
        	bEntriesAreValid = false;
        }
        
    	m_slocation = m_slocation.trim();
    	String SQL = "SELECT"
    		+ " " + SMTablelocations.sLocation
    		+ ", " + SMTablelocations.sGLPayableClearingAcct
    		+ " FROM " + SMTablelocations.TableName
    		+ " WHERE ("
    			+ "(" + SMTablelocations.sLocation + " = '" + m_slocation + "')"
    		+ ")"
    		;
    	try {
			ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
			if (rs.next()){
				//If it's a STOCK inventory item, then we use the payables clearing account:
				if (bIsStockInventoryItem){
					m_sglexpenseacct = rs.getString(SMTablelocations.sGLPayableClearingAcct);
				}
			}else{
				super.addErrorMessage("Location " + m_slocation + " is not valid.");
				bEntriesAreValid = false;
			}
			rs.close();
		} catch (SQLException e) {
			super.addErrorMessage("Error validating Location " + m_slocation + " - " + e.getMessage());
			bEntriesAreValid = false;
		}
		if (bDebugMode){
			System.out.println("In " + this.toString() + "validate_entry - m_bdqtyordered = '" + m_bdqtyordered + "'"
				+ ", m_bdunitcost = '" + m_bdunitcost + "', m_bdextendedcost = '" + m_bdextendedordercost + "'."
			);
		}
		//Qty ordered:
		boolean bQtyIsValid = true;
		m_bdqtyordered = m_bdqtyordered.replace(",", "");
        if (m_bdqtyordered.compareToIgnoreCase("") == 0){
        	m_bdqtyordered = clsManageBigDecimals.BigDecimalToScaledFormattedString(
        		SMTableicpolines.bdqtyorderedScale, BigDecimal.ZERO);
        }
		BigDecimal bdQty = new BigDecimal(0);
        try{
        	bdQty = new BigDecimal(m_bdqtyordered);
            if (bdQty.compareTo(BigDecimal.ZERO) <= 0){
            	super.addErrorMessage("Qty must be a positive number: " + m_bdqtyordered + ".  ");
        		bEntriesAreValid = false;
        		bQtyIsValid = false;
            }else{
            	m_bdqtyordered = clsManageBigDecimals.BigDecimalToScaledFormattedString(
                		SMTableicpolines.bdqtyorderedScale, bdQty);
            }
        }catch(NumberFormatException e){
    		super.addErrorMessage("Invalid quantity: '" + m_bdqtyordered + "'.  ");
    		bEntriesAreValid = false;
    		bQtyIsValid = false;
        }
        
        boolean bUnitCostIsValid = true;
        m_bdunitcost = m_bdunitcost.replace(",", "");
        if (m_bdunitcost.compareToIgnoreCase("") == 0){
        	m_bdunitcost = clsManageBigDecimals.BigDecimalToScaledFormattedString(
            		SMTableicpolines.bdunitcostScale, BigDecimal.ZERO);
        }
        BigDecimal bdUnitCost = new BigDecimal(0);
        try{
        	bdUnitCost = new BigDecimal(m_bdunitcost);
            if (bdUnitCost.compareTo(BigDecimal.ZERO) < 0){
            	super.addErrorMessage("Unit cost cannot be negative: " + m_bdunitcost + ".  ");
        		bEntriesAreValid = false;
        		bUnitCostIsValid = false;
            }else{
            	m_bdunitcost = clsManageBigDecimals.BigDecimalToScaledFormattedString(
            		SMTableicpolines.bdunitcostScale, bdUnitCost);
            }
        }catch(NumberFormatException e){
    		super.addErrorMessage("Invalid unit cost: '" + m_bdunitcost + "'.  ");
    		bEntriesAreValid = false;
    		bUnitCostIsValid = false;
        }
        if (!bUnitCostIsValid){
        	return false;
        }
        
        //Set the unit cost from the icitem file:
        //if(m_snoninventoryitem.compareToIgnoreCase("0") == 0){
        //	m_bdunitcost = item.getMostRecentCost();
        //	bdUnitCost = new BigDecimal(m_bdunitcost.replace(",", ""));
        //}

        //Extended cost:
        boolean bExtendedCostIsValid = true;
        m_bdextendedordercost = m_bdextendedordercost.replace(",", "");
        if (m_bdextendedordercost.compareToIgnoreCase("") == 0){
        	m_bdextendedordercost = clsManageBigDecimals.BigDecimalToScaledFormattedString(
            		SMTableicpolines.bdextendedordercostScale, BigDecimal.ZERO);
        }
        BigDecimal bdExtendedCost = new BigDecimal(0);
        try{
        	bdExtendedCost = new BigDecimal(m_bdextendedordercost);
            if (bdExtendedCost.compareTo(BigDecimal.ZERO) < 0){
            	super.addErrorMessage("Extended cost cannot be negative: " + m_bdextendedordercost + ".  ");
        		bEntriesAreValid = false;
        		bExtendedCostIsValid = false;
            }else{
            	m_bdextendedordercost = clsManageBigDecimals.BigDecimalToScaledFormattedString(
            			SMTableicpolines.bdextendedordercostScale, bdExtendedCost);
            }
        }catch(NumberFormatException e){
    		super.addErrorMessage("Invalid extended cost: '" + m_bdextendedordercost + "'.  ");
    		bEntriesAreValid = false;
    		bExtendedCostIsValid = false;
        }
        
        //If we have neither a valid unit cost NOR a valid extended cost, it fails:
        if (!bUnitCostIsValid && !bExtendedCostIsValid){
        	super.addErrorMessage("PO line requires a valid unit cost OR valid extended cost");
        	bEntriesAreValid = false;
        }else{
        	//If we have a valid qty, unit cost, and extended cost, maybe we can do some extensions:
        	if (bQtyIsValid && bUnitCostIsValid && bExtendedCostIsValid){
        		//If the unit cost is zero, try to divide the qty into the extended cost to get a unit cost:
        		if (
        				(bdUnitCost.compareTo(BigDecimal.ZERO) == 0)
        				&& (bdExtendedCost.compareTo(BigDecimal.ZERO) > 0)
        		){
            		m_bdunitcost = clsManageBigDecimals.BigDecimalToScaledFormattedString(SMTableicpolines.bdunitcostScale,
            				bdExtendedCost.divide(bdQty, SMTableicpolines.bdunitcostScale, RoundingMode.HALF_UP));
                		//bdExtendedCost.divide(bdQty, ).setScale(
                		//	SMTableicpolines.bdunitcostScale, RoundingMode.HALF_UP));
            	//Otherwise, multiply to get the extended cost every time:
        		}else{
            		m_bdextendedordercost = clsManageBigDecimals.BigDecimalToScaledFormattedString(
            				SMTableicpolines.bdextendedordercostScale,
                		bdQty.multiply(bdUnitCost).setScale(
                		SMTableicpolines.bdextendedordercostScale, RoundingMode.HALF_UP));
        		}
        	}
        }

        //Qty received:
		m_bdqtyreceived = m_bdqtyreceived.replace(",", "");
        if (m_bdqtyreceived.compareToIgnoreCase("") == 0){
        	m_bdqtyreceived = clsManageBigDecimals.BigDecimalToScaledFormattedString(
            		SMTableicpolines.bdqtyreceivedScale, BigDecimal.ZERO);
        }
		BigDecimal bdQtyReceived = new BigDecimal(0);
        try{
        	bdQtyReceived = new BigDecimal(m_bdqtyreceived);
            if (bdQtyReceived.compareTo(BigDecimal.ZERO) < 0){
            	super.addErrorMessage("Qty received must be a positive number: " + m_bdqtyreceived + ".  ");
        		bEntriesAreValid = false;
            }else{
            	m_bdqtyreceived = clsManageBigDecimals.BigDecimalToScaledFormattedString(
            			SMTableicpolines.bdqtyreceivedScale, bdQtyReceived);
            }
        }catch(NumberFormatException e){
    		super.addErrorMessage("Invalid quantity received: '" + m_bdqtyreceived + "'.  ");
    		bEntriesAreValid = false;
        }
        
        //Number of labels:
        if (this.getsnoninventoryitem().compareToIgnoreCase("0") == 0){
			m_snumberoflabels = m_snumberoflabels.replace(",", "");
	        if (m_snumberoflabels.compareToIgnoreCase("") == 0){
	        	m_snumberoflabels = clsManageBigDecimals.BigDecimalToScaledFormattedString(
	            		SMTableicitems.bdnumberoflabelsScale, BigDecimal.ONE);
	        }
			BigDecimal bdNumberOfLabels = new BigDecimal(0);
	        try{
	        	bdNumberOfLabels = new BigDecimal(m_snumberoflabels);
	            if (bdNumberOfLabels.compareTo(BigDecimal.ZERO) < 0){
	            	super.addErrorMessage("Number of labels must be a positive number: " + m_snumberoflabels + ".  ");
	        		bEntriesAreValid = false;
	            }else{
	            	m_snumberoflabels = clsManageBigDecimals.BigDecimalToScaledFormattedString(
	            			SMTableicitems.bdnumberoflabelsScale, bdNumberOfLabels);
	            }
	        }catch(NumberFormatException e){
	    		super.addErrorMessage("Invalid number of labels: '" + m_snumberoflabels + "'.  ");
	    		bEntriesAreValid = false;
	        }
        }
        
    	//Expense account:
    	m_sglexpenseacct = m_sglexpenseacct.trim().replace(" ", "");
        if (m_sglexpenseacct.compareToIgnoreCase("") == 0){
        	if (m_snoninventoryitem.compareToIgnoreCase("1") == 0){
	        	super.addErrorMessage("You must choose an expense account for a non-inventory item.");
	        	bEntriesAreValid = false;
        	}
        	if (item.getNonStockItem().compareToIgnoreCase("1") == 0){
	        	super.addErrorMessage("This is a NON-STOCK ('expensed') inventory item, so you must choose an expense account.");
	        	bEntriesAreValid = false;
        	}
        	
        }
        if (m_sglexpenseacct.length() > SMTableicpolines.sglexpenseacctLength){
        	super.addErrorMessage("Expense acct. is too long.");
        	bEntriesAreValid = false;
        }

        //Vendor's item number:
        m_svendorsitemnumber = m_svendorsitemnumber.trim().replace(" ", "");
        if (m_svendorsitemnumber.length() > SMTableicpolines.svendorsitemnumberLength){
        	super.addErrorMessage("Vendor's item number is too long.");
        	bEntriesAreValid = false;
        }
        
        //TODO - do we need an expected date on every line?
        // if (m_datexpected.compareTo(EMPTY_DATE_STRING) != 0){
	    //    if (!SMUtilities.IsValidDateString("M/d/yyyy", m_datexpected)){
	    //    	super.addErrorMessage("Expected date '" + m_datexpected + "' is invalid.  ");
	    //    	bEntriesAreValid = false;
	    //    }
        //}
    	return bEntriesAreValid;
    }
    public boolean receiveLine (
    		BigDecimal bdNetQtyChange, 
    		BigDecimal bdNetCostChange
    		){

    	setsextendedreceivedcost(
    		clsManageBigDecimals.BigDecimalToScaledFormattedString(
    		SMTableicpolines.bdextendedreceivedcostScale, 
    		new BigDecimal(this.getsextendedreceivedcost().replace(",", "")).add(bdNetCostChange)));
    	setsqtyreceived(
    		clsManageBigDecimals.BigDecimalToScaledFormattedString(
    		SMTableicpolines.bdqtyreceivedScale, 
    		new BigDecimal(this.getsqtyreceived().replace(",", "")).add(bdNetQtyChange)));
    	
    	//System.out.println("[1544555706] - bdNetQtyChange ='" + bdNetQtyChange + "', getsqtyreceived() = '" + getsqtyreceived() + "'.");
    	
    	return true;
    }
    public void addErrorMessage(String sMsg){
    	super.addErrorMessage(sMsg);
    }
	public String getsID() {
		return m_slid;
	}
	public void setsID(String sID) {
		this.m_slid = sID;
	}
	public String getspoheaderid() {
		return m_lpoheaderid;
	}
	public void setspoheaderid(String spoheaderid) {
		this.m_lpoheaderid = spoheaderid;
	}
	public String getslinenumber() {
		return m_llinenumber;
	}
	public void setslinenumber(String slinenumber) {
		this.m_llinenumber = slinenumber;
	}
	public void setsitemnumber(String sitemnumber) {
		this.m_sitemnumber = sitemnumber;
	}
	public String getsitemnumber() {
		return m_sitemnumber;
	}
	public void setslocation(String slocation) {
		this.m_slocation = slocation;
	}
	public String getslocation() {
		return m_slocation;
	}
	public void setsitemdescription(String sitemdescription) {
		this.m_sitemdescription = sitemdescription;
	}
	public String getsitemdescription() {
		return m_sitemdescription;
	}
	public void setsunitofmeasure(String sunitofmeasure) {
		this.m_sunitofmeasure = sunitofmeasure;
	}
	public String getsunitofmeasure() {
		return m_sunitofmeasure;
	}
	public void setsunitcost(String sunitcost) {
		this.m_bdunitcost = sunitcost;
	}
	public String getsunitcost() {
		return m_bdunitcost;
	}
	public void setsextendedordercost(String sextendedcost) {
		this.m_bdextendedordercost = sextendedcost;
	}
	public String getsextendedordercost() {
		return m_bdextendedordercost;
	}
	public void setsextendedreceivedcost(String sextendedreceivedcost) {
		this.m_bdextendedreceivedcost = sextendedreceivedcost;
	}
	public String getsextendedreceivedcost() {
		return m_bdextendedreceivedcost;
	}
	public void setsqtyordered(String sqtyordered) {
		this.m_bdqtyordered = sqtyordered;
	}
	public String getsqtyordered() {
		return m_bdqtyordered;
	}
	public void setsqtyreceived(String sqtybdqtyreceived) {
		this.m_bdqtyreceived = sqtybdqtyreceived;
	}
	public String getsqtyreceived() {
		return m_bdqtyreceived;
	}
	public void setsglexpenseacct(String sglexpenseacct) {
		this.m_sglexpenseacct = sglexpenseacct;
	}
	public String getsglexpenseacct() {
		return m_sglexpenseacct;
	}
	public void setsdatexpected(String sdatexpected) {
		this.m_datexpected = sdatexpected;
	}
	public String getsdatexpected() {
		return m_datexpected;
	}
	public void setsvendorsitemnumber(String svendorsitemnumber) {
		this.m_svendorsitemnumber = svendorsitemnumber;
	}
	public String getsvendorsitemnumber() {
		return m_svendorsitemnumber;
	}
	public void setsvendorsitemcomment(String svendorsitemcomment) {
		this.m_svendorsitemcomment = svendorsitemcomment;
	}
	public String getsvendorsitemcomment() {
		return m_svendorsitemcomment;
	}
	public void setsinstructions(String sinstructions) {
		this.m_sinstructions = sinstructions;
	}
	public String getsinstructions() {
		return m_sinstructions;
	}
	public void setsnoninventoryitem(String snoninventoryitem) {
		this.m_snoninventoryitem = snoninventoryitem;
	}
	public String getsnoninventoryitem() {
		return m_snoninventoryitem;
	}
	public String getsnumberoflabels(){
		return m_snumberoflabels;
	}
	public void setnumberoflabels(String sNumberOfLabels){
		m_snumberoflabels = sNumberOfLabels;
	}
	public int getstatus(){
		BigDecimal bdQtyOrdered;
		BigDecimal bdQtyReceived;
		try {
			bdQtyOrdered = new BigDecimal(m_bdqtyordered);
			bdQtyReceived = new BigDecimal(m_bdqtyreceived);
		} catch (NumberFormatException e) {
			//if any of the numbers are not right, we have to say it is in the first status:
			//This should work because we should not be able to get the record saved unless
			//all of the quantities are valid:
			return SMTableicpolines.STATUS_ENTERED;
		}
		
		if (bdQtyReceived.compareTo(BigDecimal.ZERO) == 0){
			return SMTableicpolines.STATUS_ENTERED;
		}
		
		if (bdQtyReceived.compareTo(bdQtyOrdered) < 0){
			return SMTableicpolines.STATUS_PARTIALLY_RECEIVED;
		}
		
		if (bdQtyReceived.compareTo(bdQtyOrdered) >= 0){
			return SMTableicpolines.STATUS_COMPLETE;
		}
		
		return SMTableicpolines.STATUS_ENTERED;
		
	}
	public boolean updateMostRecentCost(Connection conn){
		
    	//If it IS an inventory item:
    	if(m_snoninventoryitem.compareToIgnoreCase("0") == 0){
    		ICItem item = new ICItem(m_sitemnumber);
    		if (!item.load(conn)){
       	       	super.addErrorMessage("Invalid item number: '" + m_sitemnumber + "'.");
       	       	return false;
            }else{
            	this.setsunitcost(item.getMostRecentCost());
            }
    	}
    	return true;
	}
	public String addItemToOrder(Connection conn) throws Exception{
		String s = "";
    	//If it IS an inventory item:
    	if(m_snoninventoryitem.compareToIgnoreCase("1") == 0){
    		throw new Exception(getsitemnumber() + " is a NON-INVENTORY item, and cannot be added to an order.");
    	}
    	
    	//Now add the item to the order:
   		ICItem item = new ICItem(m_sitemnumber);
   		if (!item.load(conn)){
   			throw new Exception("Cannot read data - " + item.getErrorMessageString() + ".");
        }
   		String sOrderNumber = item.getDedicatedToOrderNumber().trim();
   		if (sOrderNumber.compareToIgnoreCase("") == 0){
   			throw new Exception("Item '" + m_sitemnumber + "' is not dedicated to any order number.");
        }
   		SMOrderHeader ord = new SMOrderHeader();
   		ord.setM_sOrderNumber(sOrderNumber);
   		
    	return s;
	}
	public boolean updateVendorItem(Connection conn){
		
		String sVendor = "";
		//Get the vendor for this PO:
		String SQL = "SELECT"
			+ " " + SMTableicpoheaders.svendor
			+ " FROM " + SMTableicpoheaders.TableName
			+ " WHERE ("
				+ "(" + SMTableicpoheaders.lid + " = " + this.getspoheaderid() + ")"
			+ ")"
			;
		
		try {
			ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
			if (rs.next()) {
				sVendor = (rs.getString(SMTableicpoheaders.svendor));
			}
			rs.close();
		} catch (SQLException e) {
			super.addErrorMessage("Error reading vendor information from PO header: " + e.getMessage());
			return false;
		}
		
		//Get the vendor's item number, if it's available:
		SQL = "SELECT"
			+ " " + SMTableicvendoritems.sVendorItemNumber
			+ ", " + SMTableicvendoritems.sCost
			+ ", " + SMTableicvendoritems.sComment
			+ " FROM " + SMTableicvendoritems.TableName
			+ " WHERE ("
				+ "(" + SMTableicvendoritems.sItemNumber + " = '" + getsitemnumber() + "')"
				+ " AND (" + SMTableicvendoritems.sVendor + " = '" + sVendor + "')" 
			+ ")"
			;
		
		try {
			ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
			if (rs.next()) {
				setsvendorsitemnumber(rs.getString(SMTableicvendoritems.sVendorItemNumber));
				setsvendorsitemcomment(rs.getString(SMTableicvendoritems.sComment));
			}else{
				setsvendorsitemnumber("");
				setsvendorsitemcomment("");
			}
			rs.close();
		} catch (SQLException e) {
			super.addErrorMessage("Error reading vendor item information: " + e.getMessage());
			return false;
		}
		return true;
	}
	
	public String getStatusDescription(){
		return SMTableicpolines.getStatusDescription(getstatus());
	}
    private void initBidVariables(){
    	m_slid = "-1";
    	m_lpoheaderid = "-1";
    	m_llinenumber = "-1";
    	m_sitemnumber = "";
    	m_slocation = "";
    	m_sitemdescription = "";
    	m_sunitofmeasure = "";
    	m_bdunitcost = "0.00";
    	m_bdextendedordercost = "0.00";
    	m_bdextendedreceivedcost = "0.00";
    	m_bdqtyordered = "0.0000";
    	m_bdqtyreceived = "0.0000";
    	m_sglexpenseacct = "";
    	m_datexpected = "00/00/0000";
    	m_svendorsitemnumber = "";
    	m_svendorsitemcomment = "";
    	m_sinstructions = "";
    	m_snoninventoryitem = "0";
		super.initVariables();
		super.setObjectName(ParamObjectName);
    }
    public String read_out_debug_data(){
    	String sResult = "  ** ICPOLine read out: ";
    	sResult += "\nID: " + this.getsID();
    	sResult += "\nPO Header ID: " + this.m_lpoheaderid;
    	sResult += "\nLine Number: " + this.m_llinenumber;
    	sResult += "\nItem Number: " + this.m_sitemnumber;
    	sResult += "\nLocation: " + this.m_slocation;
    	sResult += "\nItem Description: " + this.m_sitemdescription;
    	sResult += "\nUnit of Measure: " + this.m_sunitofmeasure;
    	sResult += "\nUnit Cost: " + this.m_bdunitcost;
    	sResult += "\nExtended Order Cost: " + this.m_bdextendedordercost;
    	sResult += "\nExtended Received Cost: " + this.m_bdextendedreceivedcost;
    	sResult += "\nQty Ordered: " + this.m_bdqtyordered;
    	sResult += "\nQty Received: " + this.m_bdqtyreceived;
    	sResult += "\nPO Number: " + this.m_sglexpenseacct;
    	sResult += "\nPO Number: " + this.m_datexpected;
    	sResult += "\nVendor's item number: " + this.m_svendorsitemnumber;
    	sResult += "\nInstructions: " + this.m_sinstructions;
    	sResult += "\nNon-inventory item: " + this.m_snoninventoryitem;
    	sResult += "\nVendor's item comment: " + this.m_svendorsitemcomment;
    	return sResult;
    }
	public String getQueryString(){
		//Particular to the specific class
		String sQueryString = "";
		sQueryString += "&" + ParamObjectName + "=" 
			+ clsServletUtilities.URLEncode(getObjectName());
		sQueryString += "&" + Paramlid + "=" 
			+ clsServletUtilities.URLEncode(getsID());
		sQueryString += "&" + Parambdextendedordercost + "=" 
			+ clsServletUtilities.URLEncode(getsextendedordercost());
		sQueryString += "&" + Parambdextendedreceivedcost + "=" 
			+ clsServletUtilities.URLEncode(getsextendedreceivedcost());
		sQueryString += "&" + Parambdqtyordered + "=" 
			+ clsServletUtilities.URLEncode(getsqtyordered());
		sQueryString += "&" + Parambdqtyreceived + "=" 
			+ clsServletUtilities.URLEncode(getsqtyreceived());
		sQueryString += "&" + Parambdunitcost + "=" 
			+ clsServletUtilities.URLEncode(getsunitcost());
		sQueryString += "&" + Paramdatexpected + "=" 
			+ clsServletUtilities.URLEncode(getsdatexpected());
		sQueryString += "&" + Paramllinenumber + "=" 
			+ clsServletUtilities.URLEncode(getslinenumber());
		sQueryString += "&" + Paramlpoheaderid + "=" 
			+ clsServletUtilities.URLEncode(getspoheaderid());
		sQueryString += "&" + Paramsglexpenseacct + "=" 
			+ clsServletUtilities.URLEncode(getsglexpenseacct());
		sQueryString += "&" + Paramsitemdescription + "=" 
			+ clsServletUtilities.URLEncode(getsitemdescription());
		sQueryString += "&" + Paramsitemnumber + "=" 
			+ clsServletUtilities.URLEncode(getsitemnumber());
		sQueryString += "&" + Paramslocation + "=" 
			+ clsServletUtilities.URLEncode(getslocation());
		sQueryString += "&" + Paramsunitofmeasure + "=" 
			+ clsServletUtilities.URLEncode(getsunitofmeasure());
		sQueryString += "&" + Paramsvendorsitemnumber + "=" 
			+ clsServletUtilities.URLEncode(getsvendorsitemnumber());
		sQueryString += "&" + Paramsinstructions + "=" 
			+ clsServletUtilities.URLEncode(getsinstructions());
		sQueryString += "&" + Paraminoninventoryitem + "=" 
			+ clsServletUtilities.URLEncode(getsnoninventoryitem());
		sQueryString += "&" + Paramsvendorsitemcomment + "=" 
			+ clsServletUtilities.URLEncode(getsvendorsitemcomment());
		return sQueryString;
	}
}