package smic;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import SMDataDefinition.SMTableicitems;
import SMDataDefinition.SMTableicpoheaders;
import SMDataDefinition.SMTableicpolines;
import SMDataDefinition.SMTableicporeceiptlines;
import SMDataDefinition.SMTableicvendoritems;
import SMDataDefinition.SMTablelocations;
import ServletUtilities.clsMasterEntry;
import ServletUtilities.clsServletUtilities;
import smcontrolpanel.SMUtilities;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsManageBigDecimals;
import ServletUtilities.clsManageRequestParameters;

public class ICPOReceiptLine extends clsMasterEntry{

	public static final String ParamObjectName = "Purchase order receipt line";
	public static final String Paramlid = "lid";
	public static final String Paramllinenumber = "llinenumber";
	public static final String Paramlreceiptheaderid = "lreceiptheaderid";
	public static final String Paramlpolineid = "lpolineid";
	public static final String Paramipostedtoic = "ipostedtoic";
	public static final String Parambdqtyreceived = "bdqtyreceived";
	public static final String Parambdextendedcost = "bdextendedcost";
	public static final String Paramsitemnumber = "sitemnumber";
	public static final String Paramsitemdescription = "sitemdescription";
	public static final String Paramslocation = "slocation";
	public static final String Paramsglexpenseacct = "sglexpenseacct";
	public static final String Paramsunitofmeasure = "sunitofmeasure";
	public static final String Paramlnoninventoryitem = "lnoninventoryitem";
	public static final String Parambdunitcost = "bdunitcost";
	public static final String Paramsworkordercomment = "sitemworkordercomment";

	private String m_slid;
	private String m_sllinenumber;
	private String m_slreceiptheaderid;
	private String m_slpolineid;
	private String m_sbdqtyreceived;
	private String m_sbdextendedcost;
	private String m_sitemnumber;
	private String m_sitemdescription;
	private String m_slocation;
	private String m_sglexpenseacct;
	private String m_sunitofmeasure;
	private String m_slnoninventoryitem;
	private String m_sbdunitcost;
	private String m_sworkordercomment;
	private boolean bDebugMode = false;
	
	public ICPOReceiptLine() {
		super();
		initBidVariables();
        }

	ICPOReceiptLine (HttpServletRequest req){
		super(req);
		initBidVariables();
		m_slid = clsManageRequestParameters.get_Request_Parameter(ICPOReceiptLine.Paramlid, req).trim();
		if (m_slid.compareToIgnoreCase("") == 0){
			m_slid = "-1";
		}
		m_sllinenumber = clsManageRequestParameters.get_Request_Parameter(ICPOReceiptLine.Paramllinenumber, req).trim();
		if (m_sllinenumber.compareToIgnoreCase("") == 0){
			m_sllinenumber = "-1";
		}		
		m_slreceiptheaderid = clsManageRequestParameters.get_Request_Parameter(
				ICPOReceiptLine.Paramlreceiptheaderid, req).trim();
		m_slpolineid = clsManageRequestParameters.get_Request_Parameter(
				ICPOReceiptLine.Paramlpolineid, req).trim();
		m_sbdqtyreceived = clsManageRequestParameters.get_Request_Parameter(ICPOReceiptLine.Parambdqtyreceived, req).trim();
		if (m_sbdqtyreceived.compareToIgnoreCase("") == 0){
			m_sbdqtyreceived = clsManageBigDecimals.BigDecimalToScaledFormattedString(SMTableicporeceiptlines.bdqtyreceivedScale, BigDecimal.ZERO);
		}
		m_sbdextendedcost = clsManageRequestParameters.get_Request_Parameter(ICPOReceiptLine.Parambdextendedcost, req).trim();
		if (m_sbdextendedcost.compareToIgnoreCase("") == 0){
			m_sbdextendedcost = clsManageBigDecimals.BigDecimalToScaledFormattedString(SMTableicporeceiptlines.bdextendedcostScale, BigDecimal.ZERO);
		}
		m_sbdunitcost = clsManageRequestParameters.get_Request_Parameter(ICPOReceiptLine.Parambdunitcost, req).trim();
		if (m_sbdunitcost.compareToIgnoreCase("") == 0){
			m_sbdunitcost = clsManageBigDecimals.BigDecimalToScaledFormattedString(SMTableicporeceiptlines.bdunitcostScale, BigDecimal.ZERO);
		}
		m_sitemnumber = clsManageRequestParameters.get_Request_Parameter(ICPOReceiptLine.Paramsitemnumber, req).trim().toUpperCase();
		m_sitemdescription = clsManageRequestParameters.get_Request_Parameter(ICPOReceiptLine.Paramsitemdescription, req).replace("&quot;", "\"").trim();
		m_slocation = clsManageRequestParameters.get_Request_Parameter(ICPOReceiptLine.Paramslocation, req).trim().toUpperCase();
		m_sglexpenseacct = clsManageRequestParameters.get_Request_Parameter(ICPOReceiptLine.Paramsglexpenseacct, req).trim();
		m_sunitofmeasure = clsManageRequestParameters.get_Request_Parameter(ICPOReceiptLine.Paramsunitofmeasure, req).trim().toUpperCase();
		m_slnoninventoryitem = clsManageRequestParameters.get_Request_Parameter(ICPOReceiptLine.Paramlnoninventoryitem, req).trim();
		if (m_slnoninventoryitem.compareToIgnoreCase("") == 0){
			m_slnoninventoryitem= "0";
		}
		m_sworkordercomment = clsManageRequestParameters.get_Request_Parameter(ICPOReceiptLine.Paramsworkordercomment, req).replace("&quot;", "\"").trim();
	}

    public boolean load (ServletContext context, String sConf, String sUserID, String sUserFullName){
    	Connection conn = clsDatabaseFunctions.getConnection(
    			context, 
    			sConf, 
    			"MySQL", 
    			this.toString() + ".load - user: " + sUserID
    			+ " - "
    			+ sUserFullName
    			);
    	
    	if (conn == null){
    		super.addErrorMessage("Error opening data connection.");
    		return false;
    	}
    	
    	boolean bResult = load (conn);
    	clsDatabaseFunctions.freeConnection(context, conn, "[1547080941]");
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
    	
		String SQL = " SELECT " + SMTableicporeceiptlines.TableName + ".*, " + SMTableicitems.TableName + "." 
			+ SMTableicitems.sworkordercomment
			+ " FROM " + SMTableicporeceiptlines.TableName
			+ " LEFT JOIN " + SMTableicitems.TableName + " ON " + SMTableicporeceiptlines.TableName + "." + SMTableicporeceiptlines.sitemnumber
			+ " = " + SMTableicitems.TableName + "." + SMTableicitems.sItemNumber
			+ " WHERE ("
				+ SMTableicporeceiptlines.lid + " = " + sID
			+ ")";
		
		try {
			ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
			if (rs.next()) {
				//Load the variables here:
				m_slid = Long.toString(rs.getLong(SMTableicporeceiptlines.lid));
				m_sllinenumber = Long.toString(rs.getLong(SMTableicporeceiptlines.llinenumber));
				m_slreceiptheaderid = Long.toString(rs.getLong(SMTableicporeceiptlines.lreceiptheaderid));
				m_slpolineid = Long.toString(rs.getLong(SMTableicporeceiptlines.lpolineid));
				m_sbdqtyreceived = clsManageBigDecimals.BigDecimalToScaledFormattedString(
						SMTableicporeceiptlines.bdqtyreceivedScale, 
						rs.getBigDecimal(SMTableicporeceiptlines.bdqtyreceived));
				m_sbdextendedcost = clsManageBigDecimals.BigDecimalToScaledFormattedString(
						SMTableicporeceiptlines.bdextendedcostScale, 
						rs.getBigDecimal(SMTableicporeceiptlines.bdextendedcost));
				m_sitemnumber = rs.getString(SMTableicporeceiptlines.sitemnumber).trim();
				m_sitemdescription = rs.getString(SMTableicporeceiptlines.sitemdescription).trim();
				m_slocation = rs.getString(SMTableicporeceiptlines.slocation).trim();
				m_sglexpenseacct = rs.getString(SMTableicporeceiptlines.sglexpenseacct).trim();
				m_sunitofmeasure = rs.getString(SMTableicporeceiptlines.sunitofmeasure).trim();
				m_slnoninventoryitem = Long.toString(rs.getLong(SMTableicporeceiptlines.lnoninventoryitem));
				m_sbdunitcost = clsManageBigDecimals.BigDecimalToScaledFormattedString(
						SMTableicporeceiptlines.bdunitcostScale, 
						rs.getBigDecimal(SMTableicporeceiptlines.bdunitcost));
				//Trap in case there is no corresponding record in icitems:
				try {
					m_sworkordercomment = rs.getString(SMTableicitems.sworkordercomment).trim();
				} catch (Exception e) {
					m_sworkordercomment = "";
				}
				
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
		return true;
    }
    
    public boolean save_without_data_transaction (ServletContext context, String sConf, String sUser, String sUserID, String sUserFullName, boolean bAllowZeroReceiptQty){
    	
       	Connection conn = clsDatabaseFunctions.getConnection(
    			context, 
    			sConf, 
    			"MySQL", 
    			this.toString() + ".save_without_data_transaction - user: " 
    			+ sUserID
    			+ " - "
    			+ sUserFullName
    			);
    	
    	if (conn == null){
    		super.addErrorMessage("Error opening data connection.");
    		return false;
    	}
    	
    	if (!clsDatabaseFunctions.start_data_transaction(conn)){
    		super.addErrorMessage("Error starting data transaction.");
    		return false;
    	}
    	boolean bResult = save_without_data_transaction (conn, sUser, bAllowZeroReceiptQty);
    	if (bResult){
    		if (!clsDatabaseFunctions.commit_data_transaction(conn)){
        		super.addErrorMessage("Error committing data transaction.");
        		clsDatabaseFunctions.rollback_data_transaction(conn);
        		bResult = false;
    		}
    	}else{
    		clsDatabaseFunctions.rollback_data_transaction(conn);
    	}
    	clsDatabaseFunctions.freeConnection(context, conn, "[1547080942]");
    	return bResult;	
    	
    }
    public boolean save_without_data_transaction (Connection conn, String sUserFullName, boolean bAllowZeroReceiptQty){

    	if (!validate_entry_fields(conn, bAllowZeroReceiptQty)){
    		return false;
    	}
    	
    	//We have to calculate the net change in qty and cost that this save produces, then
    	//we have to update the corresponding po line with those changes:
    	BigDecimal bdNetQtyChange = new BigDecimal(0);
    	BigDecimal bdNetCostChange = new BigDecimal(0);
    	String SQL = "";
    	
    	if (m_sllinenumber.compareToIgnoreCase("-1") == 0){
    		bdNetQtyChange = new BigDecimal(this.getsqtyreceived().replace(",", ""));
    		bdNetCostChange = new BigDecimal(this.getsextendedcost().replace(",", ""));
    	}else{
    		//Have to read the old receipt line and subtract from the current values to get the net change:
    		SQL = "SELECT"
    			+ " " + SMTableicporeceiptlines.bdextendedcost
    			+ ", " + SMTableicporeceiptlines.bdqtyreceived
    			+ ", " + SMTableicporeceiptlines.bdunitcost
    			+ " FROM " + SMTableicporeceiptlines.TableName
    			+ " WHERE ("
    				+ "(" + SMTableicporeceiptlines.lid + " = " + this.getsID() + ")"
    			+ ")"
    			;
    		
    		//System.out.println("[1544555704] - SQL='" + SQL + "'.");
    		
    		try {
				ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
				if (rs.next()){
					//If we can read the current line, we subtract the amount already received on the line
					//from the current qty received to get the NET CHANGE in qty received:
					bdNetQtyChange = new BigDecimal(
						this.getsqtyreceived().replace(",", "")).subtract(
							rs.getBigDecimal(SMTableicporeceiptlines.bdqtyreceived));
					bdNetCostChange = new BigDecimal(
						this.getsextendedcost().replace(",", "")).subtract(
							rs.getBigDecimal(SMTableicporeceiptlines.bdextendedcost));
					rs.close();
				}else{
					rs.close();
					super.addErrorMessage("Could not read existing po receipt line to get net qty change.");
					return false;
				}
			} catch (SQLException e) {
				super.addErrorMessage("Could not read existing po receipt line to get net qty change - " + e.getMessage());
				return false;
			}
    	}
    	
    	//If the qty is set to zero, we'll delete the line and re-populate the class:
    	//TJR - 10/1/2013 - The ONLY TIME we allow a receipt line with zero qty to be saved is when we 'delete' a receipt, because at that
    	//time, we set the qty on the all the lines to zero.
    	//So we no longer need any code that actually deletes a receipt line, and I commented all this out:
    	/*
    	if(checkForZeroQty()){
    		//If it's an existing line:
    		if (m_sllinenumber.compareToIgnoreCase("-1") != 0){
    			//Remember all the values we need:
    			String sCurrentPOLineID = this.getspolineid();
    			String sGLAcct = this.getsglexpenseacct();
    			String sItemDescription = this.getsitemdescription();
    			String sItemNumber = this.getsitemnumber();
    			String sLocation = this.getslocation();
    			String sNonInventoryItem = this.getsnoninventoryitem();
    			String sReceiptHeaderID = this.getsreceiptheaderid();
    			String sUnitOfMeasure = this.getsunitofmeasure();
    			String sWorkOrderComment = this.getsworkordercomment();

    			//Delete it and reset the values:
    			if (!this.delete(conn, sUser, bdNetQtyChange, bdNetCostChange)){
    				return false;
    			}
    			this.setsextendedcost(SMUtilities.BigDecimalToScaledFormattedString(
    				SMTableicporeceiptlines.bdextendedcostScale, BigDecimal.ZERO));
    			this.setsglexpenseacct(sGLAcct);
    			this.setsitemdescription(sItemDescription);
    			this.setsitemnumber(sItemNumber);
    			this.setslocation(sLocation);
    			this.setsnoninventoryitem(sNonInventoryItem);
    			this.setspolineid(sCurrentPOLineID);
    			this.setsreceiptheaderid(sReceiptHeaderID);
    			this.setsunitofmeasure(sUnitOfMeasure);
    			this.setsitemworkordercomment(sWorkOrderComment);
    		}
    		return true;
    	}
    	*/
    	if (bDebugMode){
    		System.out.println("In " + this.toString() + ".save_without . . . : net qty change = " 
    			+ bdNetQtyChange + ", net cost change = " + bdNetCostChange);
    	}
    	
    	//If it's a new line, get the next available line number for it:
    	if (m_sllinenumber.compareToIgnoreCase("-1") == 0){
    		SQL = "SELECT " + SMTableicporeceiptlines.llinenumber
    			+ " FROM " + SMTableicporeceiptlines.TableName
    			+ " WHERE ("
    				+ "(" + SMTableicporeceiptlines.lreceiptheaderid + " = " + m_slreceiptheaderid + ")"
    			+ ")"
    			+ " ORDER BY " + SMTableicporeceiptlines.llinenumber + " DESC LIMIT 1"
    			;
    		
    		try {
				ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
				if (rs.next()){
					m_sllinenumber = Long.toString(rs.getLong(SMTableicporeceiptlines.llinenumber) + 1L);
				}else{
					m_sllinenumber = "1";
				}
				rs.close();
			} catch (SQLException e) {
				super.addErrorMessage("Error getting next line number - " + e.getMessage());
	        	return false;
			}
    	}
		SQL = "INSERT INTO " + SMTableicporeceiptlines.TableName + " ("
			+ SMTableicporeceiptlines.bdextendedcost
			+ ", " + SMTableicporeceiptlines.bdqtyreceived
			+ ", " + SMTableicporeceiptlines.llinenumber
			+ ", " + SMTableicporeceiptlines.lnoninventoryitem
			+ ", " + SMTableicporeceiptlines.lpolineid
			+ ", " + SMTableicporeceiptlines.lreceiptheaderid
			+ ", " + SMTableicporeceiptlines.sglexpenseacct
			+ ", " + SMTableicporeceiptlines.sitemdescription
			+ ", " + SMTableicporeceiptlines.sitemnumber
			+ ", " + SMTableicporeceiptlines.slocation
			+ ", " + SMTableicporeceiptlines.sunitofmeasure
			+ ", " + SMTableicporeceiptlines.bdunitcost
			+ ") VALUES ("
			+ m_sbdextendedcost.replace(",", "")
			+ ", " + m_sbdqtyreceived.replace(",", "")
			+ ", " + m_sllinenumber
			+ ", " + m_slnoninventoryitem
			+ ", " + m_slpolineid
			+ ", " + m_slreceiptheaderid
			+ ", '" + clsDatabaseFunctions.FormatSQLStatement(m_sglexpenseacct) + "'"
			+ ", '" + clsDatabaseFunctions.FormatSQLStatement(m_sitemdescription) + "'"
			+ ", '" + clsDatabaseFunctions.FormatSQLStatement(m_sitemnumber) + "'"
			+ ", '" + clsDatabaseFunctions.FormatSQLStatement(m_slocation) + "'"
			+ ", '" + clsDatabaseFunctions.FormatSQLStatement(m_sunitofmeasure) + "'"
			+ ", " + m_sbdunitcost.replace(",", "")
			+ ")"
			+ " ON DUPLICATE KEY UPDATE "
			+ SMTableicporeceiptlines.bdextendedcost + " = " + m_sbdextendedcost.replace(",", "")
			+ ", " + SMTableicporeceiptlines.bdqtyreceived + " = " + m_sbdqtyreceived.replace(",", "")
			+ ", " + SMTableicporeceiptlines.llinenumber + " = " + m_sllinenumber
			+ ", " + SMTableicporeceiptlines.lnoninventoryitem + " = " + m_slnoninventoryitem
			+ ", " + SMTableicporeceiptlines.lpolineid + " = " + m_slpolineid
			+ ", " + SMTableicporeceiptlines.lreceiptheaderid + " = " + m_slreceiptheaderid
			+ ", " + SMTableicporeceiptlines.sglexpenseacct + " = '" 
				+ clsDatabaseFunctions.FormatSQLStatement(m_sglexpenseacct) + "'"
			+ ", " + SMTableicporeceiptlines.sitemdescription + " = '" 
				+ clsDatabaseFunctions.FormatSQLStatement(m_sitemdescription) + "'"
			+ ", " + SMTableicporeceiptlines.sitemnumber + " = '" 
				+ clsDatabaseFunctions.FormatSQLStatement(m_sitemnumber) + "'"
			+ ", " + SMTableicporeceiptlines.slocation + " = '" 
				+ clsDatabaseFunctions.FormatSQLStatement(m_slocation) + "'"
			+ ", " + SMTableicporeceiptlines.sunitofmeasure + " = '" 
				+ clsDatabaseFunctions.FormatSQLStatement(m_sunitofmeasure) + "'"
			+ ", " + SMTableicporeceiptlines.bdunitcost + " = " + m_sbdunitcost.replace(",", "")
			;
		try{
		    Statement stmt = conn.createStatement();
		    stmt.executeUpdate(SQL);
		}catch (Exception ex) {
    	    super.addErrorMessage("Error inserting " + ParamObjectName + " with SQL: " + SQL + " - " + ex.getMessage());
    	    return false;		
    	}
		SQL = "SELECT"
			+ " " + SMTableicporeceiptlines.lid
			+ " FROM " + SMTableicporeceiptlines.TableName
			+ " WHERE ("
				+ "(" + SMTableicporeceiptlines.lreceiptheaderid + " = " + m_slreceiptheaderid + ")"
				+ " AND (" + SMTableicporeceiptlines.llinenumber + " = " + m_sllinenumber + ")"
			+ ")"
			;
		
		try {
			ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
			if (rs.next()) {
				m_slid = Long.toString(rs.getLong(SMTableicporeceiptlines.lid));
			} else {
				super.addErrorMessage("Could not get last ID number with SQL: " + SQL);
				return false;
			}
			rs.close();
		} catch (SQLException e) {
			super.addErrorMessage("Could not get last ID number - with SQL: " + SQL + " - " + e.getMessage());
			return false;
		}
				
		//Now we have to update the po line that this receipt points to, to make sure the po line
		//has the correct received and outstanding costs, qtys, etc.:
		//Update with the NET CHANGES, not the current amounts . . . .
    	if (bDebugMode){
    		System.out.println("In " + this.toString() + ".save_without . . . : going to updatePOLine");
    	}
    	
    	//System.out.println("[1544555708] - getspolineid() ='" + this.getspolineid() + "'.");
    	
		if (!updatePOLine(conn, bdNetQtyChange, bdNetCostChange, sUserFullName)){
			return false;
		}
		
		//Now update the vendor item information - the item most recent cost gets updated when the 
		//batch gets posted, but we can't update vendor item information there because at that point
		//we don't have all the necessary information to update vendor item info
		if (!updateVendorItem(conn)){
			return false;
		}
		
		//Finally, update the work order comment on the item:
		if (getsnoninventoryitem().compareToIgnoreCase("0") == 0){
			SQL = "UPDATE " + SMTableicitems.TableName
				+ " SET " + SMTableicitems.sworkordercomment + " = '" + getsworkordercomment() + "'"
				+ " WHERE ("
					+ "(" + SMTableicitems.sItemNumber + " = '" + getsitemnumber() + "')"
				+ ")"
			;
		    try {
				Statement stmt = conn.createStatement();
				stmt.executeUpdate(SQL);
			} catch (SQLException e) {
				super.addErrorMessage("Could not update item work order comment - with SQL: " + SQL + " - " + e.getMessage());
				return false;
			}
		}
		// If something went wrong, we can't get the last ID:
		if (m_slid.compareToIgnoreCase("-1") == 0) {
			super.addErrorMessage("Could not get last ID number.");
			return false;
		}
    	
    	return true;
    }
    private boolean updateVendorItem(Connection conn){
    	
    	if (getsnoninventoryitem().compareToIgnoreCase("1") == 0){
    		return true;
    	}
    	
		//Update the vendor's item number and cost here:
		ICVendorItem venitem = new ICVendorItem();
		String SQL = "SELECT"
			+ " " + SMTableicpoheaders.TableName + "." + SMTableicpoheaders.svendor
			+ ", " + SMTableicpolines.TableName + "." + SMTableicpolines.svendorsitemnumber
			+ " FROM " + SMTableicpoheaders.TableName + ", " + SMTableicpolines.TableName
			+ " WHERE ("
				+ "(" + SMTableicpolines.TableName + "." + SMTableicpolines.lid + " = "
					+ this.getspolineid() + ")"
				+ " AND (" + SMTableicpoheaders.TableName + "." + SMTableicpoheaders.lid + " = "
					+ SMTableicpolines.TableName + "." + SMTableicpolines.lpoheaderid + ")"
			+ ")"
		;
		String sVendor = "";
		String sVendorItem = "";
		
		try {
			ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
			if (rs.next()) {
				sVendor = rs.getString(SMTableicpoheaders.TableName + "."
						+ SMTableicpoheaders.svendor);
				sVendorItem = rs.getString(SMTableicpolines.TableName + "."
						+ SMTableicpolines.svendorsitemnumber);
			} else {
				addErrorMessage("Error reading vendor item on line #: "
						+ this.getslinenumber() + " - PO Line not found");
				rs.close();
				return false;
			}
			rs.close();
		} catch (SQLException e) {
			addErrorMessage("Error reading vendor item on line #: " + this.getslinenumber()
				+ " - " + e.getMessage());
		}
		BigDecimal bdUnitCost = new BigDecimal(0);
		BigDecimal bdTotalCost = new BigDecimal(this.getsextendedcost().replace(",", ""));
		BigDecimal bdQty = new BigDecimal(this.getsqtyreceived().replace(",", ""));
		if (bdQty.compareTo(BigDecimal.ZERO) > 0){
			bdUnitCost = bdTotalCost.divide(
				bdQty, SMTableicvendoritems.sCostScale, BigDecimal.ROUND_HALF_UP);
		}
		
		if (!venitem.updateVendorItem(
			this.getsitemnumber(), 
			sVendor, 
			sVendorItem, 
			bdUnitCost, 
			"",
			conn
			)
		){
			//System.out.println("Error updating vendor item on line #: " + this.getslinenumber()
			//		+ " - " + venitem.getErrorMessages());
			addErrorMessage("Error updating vendor item on line #: " + this.getslinenumber()
					+ " - " + venitem.getErrorMessages());
			return false;
		}
		return true;
    }
    /*
    private boolean checkForZeroQty(){
    	
    	BigDecimal bdQtyReceived;
    	try {
			bdQtyReceived = new BigDecimal(this.getsqtyreceived().replace(",", ""));
		} catch (NumberFormatException e) {
			//This shouldn't happen, because we validate the entry fields before we get here:
			return false;
		}
    	if (bdQtyReceived.compareTo(BigDecimal.ZERO) == 0){
    		return true;
    	}else{
    		return false;
    	}
    }

    public boolean delete (
    		Connection conn, 
    		String sUser, 
    		BigDecimal bdNetQtyChange, 
    		BigDecimal bdNetCostChange){
    	
    	String SQL = "DELETE FROM " + SMTableicporeceiptlines.TableName
    		+ " WHERE ("
    			+ "(" + SMTableicporeceiptlines.lid + " = " + m_slid + ")"
    		+ ")"
    		;
		try{
		    Statement stmt = conn.createStatement();
		    stmt.executeUpdate(SQL);
		}catch (Exception ex) {
			super.addErrorMessage("Error deleting po receipt line with SQL: " + SQL + " - " + ex.getMessage());
			return false;
		}
    	
		if (bDebugMode){
			System.out.println("In " + this.toString() + ".delete - going to update PO line with net qty change: "
					+ bdNetQtyChange + " and net cost change: " + bdNetCostChange);
		}
		if (!updatePOLine(conn, bdNetQtyChange, bdNetCostChange, sUser)){
			return false;
		}
		
		//Update the other lines and PO header as needed:
		ICPOReceiptHeader poreceiptheader = new ICPOReceiptHeader();
		poreceiptheader.setsID(m_slreceiptheaderid);
		if (!poreceiptheader.updateLineNumbersAfterLineDeletion(conn)){
			super.addErrorMessage("Error updating PO receipt lines - " + poreceiptheader.getErrorMessages());
			return false;
		}
		
		//Empty the values:
		initBidVariables();
		return true;
    }
    */
    private boolean updatePOLine(
    		Connection conn, 
    		BigDecimal bdNetQtyChange, 
    		BigDecimal bdNetCostChange, 
    		String sUserFullName){
    	
    	ICPOLine line = new ICPOLine();
		line.setsID(this.getspolineid());
		if (!line.load(conn)){
			super.addErrorMessage("Could not load po line to update receipt information - " 
					+ line.getErrorMessages());
			return false;
		}
		
		//System.out.println("[1544555705] - bdNetQtyChange ='" + bdNetQtyChange + "'.");
		if (!line.receiveLine(bdNetQtyChange, bdNetCostChange)){
			super.addErrorMessage("Could not receive po line with receipt information - " 
					+ line.getErrorMessages());
			return false;
		}
		if (!line.save_without_data_transaction(conn, sUserFullName)){
			super.addErrorMessage("Could not save po line with receipt information - " 
					+ line.getErrorMessages());
			return false;
		}
		//Have to update the PO status, in case this change affects the status of the PO:
		ICPOHeader pohead = new ICPOHeader();
		pohead.setsID(line.getspoheaderid());
		
		if(!pohead.updatePOStatus(conn)){
			super.addErrorMessage("Could not update PO header status - " 
					+ pohead.getErrorMessages());
			return false;
		}
		
    	return true;
    }
    public void calculateCosts() throws Exception{
    try{	
    	//if there is no quantity, or the quantity is zero, nothing happens.
    	if (this.getsqtyreceived().compareToIgnoreCase("") == 0){
    		return;
    	}
    	
    	BigDecimal bdQtyReceived = new BigDecimal(this.getsqtyreceived().replace(",", ""));
    	if (bdQtyReceived.compareTo(BigDecimal.ZERO) == 0){
    		return;
    	}

    	//if there is a quantity and an extended cost, whether there is a unit cost or not, 
    	//the program will divide the extended cost by the quantity and put the result in the unit cost.
    	if (this.getsextendedcost().compareToIgnoreCase("") == 0){
    	}else{
        	BigDecimal bdExtendedCost = new BigDecimal(this.getsextendedcost().replace(",", ""));
        	if (bdExtendedCost.compareTo(BigDecimal.ZERO) == 0){
        	}else{
        		BigDecimal bdUnitCost = bdExtendedCost.divide(
        			bdQtyReceived, SMTableicporeceiptlines.bdunitcostScale, RoundingMode.HALF_UP);
        		this.setsunitcost(clsManageBigDecimals.BigDecimalToScaledFormattedString(
        			SMTableicporeceiptlines.bdunitcostScale, bdUnitCost));
        		return;
        	}
    	}

    	//if there IS a quantity and a unit cost (most likely case), the program will 
    	//multiply the quantity times the unit cost and put the result in the extended cost.
    	if (this.getsunitcost().compareToIgnoreCase("") == 0){
    		return;
    	}
    	
    	BigDecimal bdUnitCost = new BigDecimal(this.getsunitcost().replace(",", ""));
    	if (bdUnitCost.compareTo(BigDecimal.ZERO) == 0){
    		return;
    	}else{
    		BigDecimal bdExtendedCost = bdQtyReceived.multiply(bdUnitCost);
    		this.setsextendedcost(
    			clsManageBigDecimals.BigDecimalToScaledFormattedString(
    				SMTableicporeceiptlines.bdextendedcostScale, bdExtendedCost));
    		return;
    	}
    }catch (Exception e){
    	throw new Exception("Error calculating cost - " + e.getMessage());
    }
    }
    public boolean validate_entry_fields(
    		ServletContext context, 
			String sDBID, 
			String sUser,
			String sUserID,
			String sUserFullName,
			boolean bAllowZeroReceiptQty){
    	
    	boolean bResult = true;
    	Connection conn = clsDatabaseFunctions.getConnection(
    			context, 
    			sDBID, 
    			"MySQL", 
    			SMUtilities.getFullClassName(this.toString() + ".validate_entry_fields - user: " + sUserID
    			+ " - " + sUserFullName		
    					)
    	);
    	if (conn == null){
    		super.addErrorMessage("Could not get connection to validate entry fields.");
    		return false;
    	}
    	
    	bResult = validate_entry_fields(conn, bAllowZeroReceiptQty);
    	clsDatabaseFunctions.freeConnection(context, conn, "[1547080943]");
    	
    	return bResult;
    	
    }
    public boolean validate_entry_fields (Connection conn, boolean bAllowZeroReceiptQty){
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
    	
    	//Receipt Header ID:
    	long lReceiptHeaderID;
		try {
			lReceiptHeaderID = Long.parseLong(m_slreceiptheaderid);
		} catch (NumberFormatException e) {
        	super.addErrorMessage("Invalid PO receipt header ID: '" + m_slreceiptheaderid + "'.");
        	bEntriesAreValid = false;
        	return bEntriesAreValid;
		}
    	
    	if ((lReceiptHeaderID <= 0)){
        	super.addErrorMessage("Invalid PO receipt header ID: '" + m_slreceiptheaderid + "'.");
        	bEntriesAreValid = false;
        	return bEntriesAreValid;
    	}
    	
    	//Line number:
    	long lLineNumber;
		try {
			lLineNumber = Long.parseLong(m_sllinenumber);
		} catch (NumberFormatException e) {
        	super.addErrorMessage("Invalid PO receipt line number: '" + m_sllinenumber + "'.");
        	bEntriesAreValid = false;
        	return bEntriesAreValid;
		}
    	
    	if ((lLineNumber < -1) || (lLineNumber == 0)){
        	super.addErrorMessage("Invalid PO receipt line number: '" + m_sllinenumber + "'.");
        	bEntriesAreValid = false;
        	return bEntriesAreValid;
    	}
    	
    	//PO Line ID:
    	long lPOLineID = 0;
		try {
			lPOLineID = Long.parseLong(m_slpolineid);
		} catch (NumberFormatException e) {
        	super.addErrorMessage("Invalid PO line ID: '" + m_sllinenumber + "'.");
        	bEntriesAreValid = false;
		}
    	
    	if ((lPOLineID <= 0)){
        	super.addErrorMessage("Invalid PO line ID: '" + m_sllinenumber + "'.");
        	bEntriesAreValid = false;
    	}

		m_sbdqtyreceived = m_sbdqtyreceived.replace(",", "");
        if (m_sbdqtyreceived.compareToIgnoreCase("") == 0){
        	m_sbdqtyreceived = clsManageBigDecimals.BigDecimalToScaledFormattedString(
        		SMTableicporeceiptlines.bdqtyreceivedScale, BigDecimal.ZERO);
        }
		BigDecimal bdQtyReceived = new BigDecimal(0);
        try{
        	bdQtyReceived = new BigDecimal(m_sbdqtyreceived);
            if (bdQtyReceived.compareTo(BigDecimal.ZERO) <= 0){
            	//We ONLY allow qtys less than zero if the receipt has been deleted:
            	if (!bAllowZeroReceiptQty){
	            	super.addErrorMessage("Qty received must be more than zero: " + m_sbdqtyreceived + ".  ");
	        		bEntriesAreValid = false;
            	}
            }else{
            	m_sbdqtyreceived = clsManageBigDecimals.BigDecimalToScaledFormattedString(
                		SMTableicporeceiptlines.bdqtyreceivedScale, bdQtyReceived);
            }
        }catch(NumberFormatException e){
    		super.addErrorMessage("Invalid quantity: received '" + m_sbdqtyreceived + "'.  ");
    		bEntriesAreValid = false;
        }

        //Unit cost:
		m_sbdunitcost = m_sbdunitcost.replace(",", "");
        if (m_sbdunitcost.compareToIgnoreCase("") == 0){
        	m_sbdunitcost = clsManageBigDecimals.BigDecimalToScaledFormattedString(
        		SMTableicporeceiptlines.bdunitcostScale, BigDecimal.ZERO);
        }
		BigDecimal bdUnitCost = new BigDecimal(0);
        try{
        	bdUnitCost = new BigDecimal(m_sbdunitcost);
            if (bdUnitCost.compareTo(BigDecimal.ZERO) < 0){
            	super.addErrorMessage("Unit cost cannot be negative: " + m_sbdunitcost + ".  ");
        		bEntriesAreValid = false;
            }else{
            	m_sbdunitcost = clsManageBigDecimals.BigDecimalToScaledFormattedString(
                		SMTableicporeceiptlines.bdunitcostScale, bdUnitCost);
            }
        }catch(NumberFormatException e){
    		super.addErrorMessage("Invalid unit cost: '" + m_sbdunitcost + "'.  ");
    		bEntriesAreValid = false;
        }
        
        //Extended cost:
        //If the qty received is zero, then the cost received must be zero:
        if (bdQtyReceived.compareTo(BigDecimal.ZERO) == 0){
        	m_sbdextendedcost = 
        		clsManageBigDecimals.BigDecimalToScaledFormattedString(
        			SMTableicporeceiptlines.bdextendedcostScale, BigDecimal.ZERO);
        }else{
	        m_sbdextendedcost = m_sbdextendedcost.replace(",", "");
	        if (m_sbdextendedcost.compareToIgnoreCase("") == 0){
	        	m_sbdextendedcost = clsManageBigDecimals.BigDecimalToScaledFormattedString(
	            		SMTableicporeceiptlines.bdextendedcostScale, BigDecimal.ZERO);
	        }
	        BigDecimal bdExtendedCost = new BigDecimal(0);
	        try{
	        	bdExtendedCost = new BigDecimal(m_sbdextendedcost);
	            if (bdExtendedCost.compareTo(BigDecimal.ZERO) < 0){
	            	super.addErrorMessage("Extended cost cannot be negative: " + m_sbdextendedcost + ".  ");
	        		bEntriesAreValid = false;
	            }else{
	            	m_sbdextendedcost = clsManageBigDecimals.BigDecimalToScaledFormattedString(
	            			SMTableicporeceiptlines.bdextendedcostScale, bdExtendedCost);
	            }
	        }catch(NumberFormatException e){
	    		super.addErrorMessage("Invalid extended cost: '" + m_sbdextendedcost + "'.  ");
	    		bEntriesAreValid = false;
	        }
        }
        
        //Item number:
        boolean bIsStockInventory = false;
    	m_sitemnumber = m_sitemnumber.trim();
    	ICItem item = new ICItem(m_sitemnumber);
    	if(m_slnoninventoryitem.compareToIgnoreCase("0") == 0){
            if (!item.load(conn)){
       	       	super.addErrorMessage("Invalid item number: '" + m_sitemnumber + "'.");
       	       	bEntriesAreValid = false;
            }
            bIsStockInventory = item.getNonStockItem().compareToIgnoreCase("0") == 0;
            // TJR - 2/20/2018 - we are allowing non-stock inventory on PO's now:
            //if (item.getNonStockItem().compareToIgnoreCase("1") == 0){
       	    //   	super.addErrorMessage("Item number: '" + m_sitemnumber + "' is a NON-STOCK item.");
       	    //   	bEntriesAreValid = false;
            // }
    	}else{
            if (m_sitemnumber.length() > SMTableicporeceiptlines.sitemnumberLength){
            	super.addErrorMessage("Item number is too long.");
            	bEntriesAreValid = false;
            }
    	}
        
        //Item description:
        //m_sitemdescription
        //m_sitemdescription = m_sitemdescription.replace(" ", "");
        //If it's NOT a non-inventory item, get the description
        if (m_slnoninventoryitem.compareToIgnoreCase("0") == 0){
        	//Get the description from the item:
        	m_sitemdescription = item.getItemDescription();
        }
        if (m_sitemdescription.length() > SMTableicporeceiptlines.sitemdescriptionLength){
        	super.addErrorMessage("Item description is too long.");
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
				//If it's an inventory item:
				if (bIsStockInventory){
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

        //Unit of measure:
        if (m_slnoninventoryitem.compareToIgnoreCase("0") == 0){
        	m_sunitofmeasure = item.getCostUnitOfMeasure();
        }
        m_sunitofmeasure = m_sunitofmeasure.trim();
        if (m_sunitofmeasure.length() > SMTableicporeceiptlines.sunitofmeasureLength){
        	super.addErrorMessage("Unit of measure is too long.");
        	bEntriesAreValid = false;
        }
        if (m_sunitofmeasure.compareToIgnoreCase("") == 0){
        	super.addErrorMessage("Unit of measure cannot be blank.");
        	bEntriesAreValid = false;
        }
    	return bEntriesAreValid;
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
	public String getslinenumber() {
		return m_sllinenumber;
	}
	public void setslinenumber(String slinenumber) {
		this.m_sllinenumber = slinenumber;
	}
	public String getsreceiptheaderid() {
		return m_slreceiptheaderid;
	}
	public void setsreceiptheaderid(String sreceiptheaderid) {
		this.m_slreceiptheaderid = sreceiptheaderid;
	}
	public String getspolineid() {
		return m_slpolineid;
	}
	public void setspolineid(String spolineid) {
		this.m_slpolineid = spolineid;
	}
	public String getsqtyreceived() {
		return m_sbdqtyreceived;
	}
	public void setsqtyreceived(String sqtyreceived) {
		this.m_sbdqtyreceived = sqtyreceived;
	}
	public void setsextendedcost(String sextendedcost) {
		this.m_sbdextendedcost = sextendedcost;
	}
	public String getsextendedcost() {
		return m_sbdextendedcost;
	}
	public void setsunitcost(String sunitcost) {
		this.m_sbdunitcost = sunitcost;
	}
	public String getsunitcost() {
		return m_sbdunitcost;
	}
	public void setsitemnumber(String sitemnumber) {
		this.m_sitemnumber = sitemnumber;
	}
	public String getsitemnumber() {
		return m_sitemnumber;
	}
	public void setsitemdescription(String sitemdescription) {
		this.m_sitemdescription = sitemdescription;
	}
	public String getsitemdescription() {
		return m_sitemdescription;
	}
	public void setslocation(String slocation) {
		this.m_slocation = slocation;
	}
	public String getslocation() {
		return m_slocation;
	}
	public void setsglexpenseacct(String sglexpenseacct) {
		this.m_sglexpenseacct = sglexpenseacct;
	}
	public String getsglexpenseacct() {
		return m_sglexpenseacct;
	}
	public void setsunitofmeasure(String sunitofmeasure) {
		this.m_sunitofmeasure = sunitofmeasure;
	}
	public String getsunitofmeasure() {
		return m_sunitofmeasure;
	}
	public void setsnoninventoryitem(String snoninventoryitem) {
		this.m_slnoninventoryitem = snoninventoryitem;
	}
	public String getsnoninventoryitem() {
		return m_slnoninventoryitem;
	}
	public void setsitemworkordercomment(String swordordercomment) {
		this.m_sworkordercomment = swordordercomment;
	}
	public String getsworkordercomment() {
		return m_sworkordercomment;
	}
    private void initBidVariables(){
    	m_slid = "-1";
    	m_sllinenumber = "-1";
    	m_slreceiptheaderid = "-1";
    	m_slpolineid = "-1";
    	m_sbdqtyreceived = "0.0000";
    	m_sbdextendedcost = "0.00";
    	m_sitemnumber = "";
    	m_sitemdescription = "";
    	m_slocation = "";
    	m_sglexpenseacct = "";
    	m_sunitofmeasure = "";
    	m_slnoninventoryitem = "0";
    	m_sbdunitcost = "0.000000";
    	m_sworkordercomment = "";
		super.initVariables();
		super.setObjectName(ParamObjectName);
    }
    public String read_out_debug_data(){
    	String sResult = "  ** ICPOReceiptLine read out: ";
    	sResult += "\nID: " + this.getsID();
    	sResult += "\nLine number: " + this.getslinenumber();
    	sResult += "\nReceipt header ID: " + this.getsreceiptheaderid();
    	sResult += "\nPO Line ID: " + this.getspolineid();
    	sResult += "\nQty received: " + this.getsqtyreceived();
    	sResult += "\nExtended cost: " + this.getsextendedcost();
    	sResult += "\nItem number: " + this.getsitemnumber();
    	sResult += "\nItem desc: " + this.getsitemdescription();
    	sResult += "\nLocation: " + this.getslocation();
    	sResult += "\nGL expense account: " + this.getsglexpenseacct();
    	sResult += "\nUnit of measure: " + this.getsunitofmeasure();
    	sResult += "\nNon inventory item: " + this.getsnoninventoryitem();
    	sResult += "\nUnit cost: " + this.getsunitcost();
    	sResult += "\nWork order comment: " + this.getsworkordercomment();
    	return sResult;
    }
	public String getQueryString(){
		//Particular to the specific class
		String sQueryString = "";
		sQueryString += "&" + ParamObjectName + "=" 
			+ clsServletUtilities.URLEncode(getObjectName());
		sQueryString += "&" + Paramlid + "=" 
			+ clsServletUtilities.URLEncode(getsID());
		sQueryString += "&" + Parambdextendedcost + "=" 
			+ clsServletUtilities.URLEncode(getsextendedcost());
		sQueryString += "&" + Parambdqtyreceived + "="
			+ clsServletUtilities.URLEncode(getsqtyreceived());
		sQueryString += "&" + Paramllinenumber + "="
			+ clsServletUtilities.URLEncode(getslinenumber());
		sQueryString += "&" + Paramlnoninventoryitem + "="
			+ clsServletUtilities.URLEncode(getsnoninventoryitem());
		sQueryString += "&" + Paramlpolineid + "="
			+ clsServletUtilities.URLEncode(getspolineid());
		sQueryString += "&" + Paramlreceiptheaderid + "="
			+ clsServletUtilities.URLEncode(getsreceiptheaderid());
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
		sQueryString += "&" + Parambdunitcost + "="
			+ clsServletUtilities.URLEncode(getsunitcost());
		return sQueryString;
	}
}