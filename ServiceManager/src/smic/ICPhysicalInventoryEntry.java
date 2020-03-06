package smic;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import smic.ICEntryLine;
import SMClasses.SMBatchStatuses;
import SMClasses.SMModuleTypes;
import SMDataDefinition.SMTableicinventoryworksheet;
import SMDataDefinition.SMTableicitemlocations;
import SMDataDefinition.SMTableicitems;
import SMDataDefinition.SMTableicphysicalcountlines;
import SMDataDefinition.SMTableicphysicalcounts;
import SMDataDefinition.SMTableicphysicalinventories;
import SMDataDefinition.SMTablelocations;
import ServletUtilities.clsMasterEntry;
import ServletUtilities.clsServletUtilities;
import smcontrolpanel.SMUtilities;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsDateAndTimeConversions;
import ServletUtilities.clsManageBigDecimals;
import ServletUtilities.clsManageRequestParameters;

public class ICPhysicalInventoryEntry extends clsMasterEntry {

	public static final String ParamObjectName = "Physical Inventory";

	// Particular to the specific class
	public static final String ParamID = "id";
	public static final String ParamDesc = "Description";
	public static final String ParamCreatedByFullName = "sCreatedByFullName";
	public static final String ParamdatCreated = "datCreated";
	public static final String ParamiStatus = "istatus";
	public static final String ParamlBatchnumber = "lBatchNumber";
	public static final String ParamLocation = "slocation";
	//public static final String ParamStartingItemNumber = "sstartingitemnumber";
	//public static final String ParamEndingItemNumber = "sendingitemnumber";

	private String m_sid;
	private String m_sdesc;
	private String m_screatedbyfullname;
	private String m_datcreated;
	private String m_sstatus;
	private String m_sbatchnumber;
	private String m_slocation;
	private ICOption option = new ICOption();
	//private String m_sstartingitemnumber;
	//private String m_sendingitemnumber;
	
	private static boolean bDebugMode = false;

	public ICPhysicalInventoryEntry() {
		super();
		initEntryVariables();
	}

	ICPhysicalInventoryEntry(HttpServletRequest req) {
		super(req);
		initEntryVariables();

		m_sid = clsManageRequestParameters.get_Request_Parameter(
				ICPhysicalInventoryEntry.ParamID, req).trim();
		m_sdesc = clsManageRequestParameters.get_Request_Parameter(
				ICPhysicalInventoryEntry.ParamDesc, req).trim();
		m_screatedbyfullname = clsManageRequestParameters.get_Request_Parameter(
				ICPhysicalInventoryEntry.ParamCreatedByFullName, req).trim();
		m_datcreated = clsManageRequestParameters.get_Request_Parameter(
				ICPhysicalInventoryEntry.ParamdatCreated, req).trim();
		if (m_datcreated.compareToIgnoreCase("") == 0) {
			m_datcreated = EMPTY_DATE_STRING;
		}

		if (clsManageRequestParameters.get_Request_Parameter(
				ICPhysicalInventoryEntry.ParamiStatus, req).trim()
				.compareToIgnoreCase(Integer.toString(SMTableicphysicalinventories.STATUS_BATCHED)) == 0) {
			m_sstatus = Integer.toString(SMTableicphysicalinventories.STATUS_BATCHED);
		}
		if (clsManageRequestParameters.get_Request_Parameter(
				ICPhysicalInventoryEntry.ParamiStatus, req).trim()
				.compareToIgnoreCase(Integer.toString(SMTableicphysicalinventories.STATUS_DELETED)) == 0) {
			m_sstatus = Integer.toString(SMTableicphysicalinventories.STATUS_DELETED);
		}else{
			m_sstatus = Integer.toString(SMTableicphysicalinventories.STATUS_ENTERED);
		}
		
		if (clsManageRequestParameters.get_Request_Parameter(
				ICPhysicalInventoryEntry.ParamlBatchnumber, req).trim()
				.compareToIgnoreCase("") != 0) {
			m_sbatchnumber = clsManageRequestParameters.get_Request_Parameter(
					ICPhysicalInventoryEntry.ParamlBatchnumber, req).trim();
		} else {
			m_sbatchnumber = "0";
		}
		m_slocation = clsManageRequestParameters.get_Request_Parameter(
				ICPhysicalInventoryEntry.ParamLocation, req).trim().toUpperCase();
	}

	public boolean load(ServletContext context, String sDBID, String sUserID, String sUserFullName) {
		Connection conn = clsDatabaseFunctions.getConnection(context, sDBID, "MySQL",
				this.toString() + " - user: " 
		+ sUserID
		+ " - "
		+ sUserFullName
				);

		if (conn == null) {
			super.addErrorMessage("Error opening data connection.");
			return false;
		}

		boolean bResult = load(conn);
		clsDatabaseFunctions.freeConnection(context, conn, "[1547080892]");
		return bResult;

	}

	public boolean load(Connection conn) {
		return load(m_sid, conn);
	}

	private boolean load(String sID, Connection conn) {

		@SuppressWarnings("unused")
		long lID;
		try {
			lID = Long.parseLong(sID);
		} catch (NumberFormatException n) {
			super.addErrorMessage("Invalid ID: '" + sID + "'");
			return false;
		}

		String SQL = " SELECT * FROM " + SMTableicphysicalinventories.TableName
				+ " WHERE (" + SMTableicphysicalinventories.lid + " = " + sID
				+ ")";

		try {
			ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
			if (rs.next()) {
				// Load the variables here:
				m_sid = sID;
				m_sdesc = rs.getString(SMTableicphysicalinventories.sdesc);
				m_screatedbyfullname = rs
						.getString(SMTableicphysicalinventories.screatedbyfullname);
				m_datcreated = clsDateAndTimeConversions.resultsetDateTimeToTheMinuteStringToString(rs
						.getString(SMTableicphysicalinventories.datcreated));
				m_sstatus = Long.toString(rs
						.getLong(SMTableicphysicalinventories.istatus));
				m_sbatchnumber = Long.toString(rs
						.getLong(SMTableicphysicalinventories.lbatchnumber));
				m_slocation = rs
						.getString(SMTableicphysicalinventories.slocation);
			} else {
				super.addErrorMessage("No " + ParamObjectName
						+ " found for ID: '" + sID + "'");
				rs.close();
				return false;
			}
		} catch (SQLException e) {
			super.addErrorMessage("Error reading " + ParamObjectName
					+ " for ID: '" + sID + "' - " + e.getMessage());
			return false;
		}
		return true;
	}

	public boolean save_with_data_transaction(ServletContext context,
			String sDBID, String sUserID, String sUserFullName) {

		Connection conn = clsDatabaseFunctions.getConnection(context, sDBID, "MySQL",
				this.toString() + " - user: " 
		+ sUserID
		+ " - "
		+ sUserFullName
				);

		if (conn == null) {
			super.addErrorMessage("Error opening data connection.");
			return false;
		}

		//First, set the in process flag:
		try {
			option.checkAndUpdatePostingFlagWithoutConnection(
				context, 
				sDBID, 
				clsServletUtilities.getFullClassName(this.toString()) + ".save_with_data_transaction", 
				sUserFullName, 
				"SAVING PHYSICAL INVENTORY");
		} catch (Exception e1) {
			addErrorMessage("Error [1529957437] - " + e1.getMessage());
			return false;
		}
		
		if (!clsDatabaseFunctions.start_data_transaction(conn)){
			try {
				option.resetPostingFlagWithoutConnection(context, sDBID);
			} catch (Exception e) {
				//We won't stop for this, but the next user will have to clear the IC posting flag
			}
			clsDatabaseFunctions.freeConnection(context, conn, "[1547080895]");
			super.addErrorMessage("Could not start data transaction.");
			return false;
		}
		
		boolean bResult = save_without_data_transaction(conn, sUserFullName, sUserID);
		
		if (!clsDatabaseFunctions.commit_data_transaction(conn)){
			clsDatabaseFunctions.rollback_data_transaction(conn);
			try {
				option.resetPostingFlagWithoutConnection(context, sDBID);
			} catch (Exception e) {
				//We won't stop for this, but the next user will have to clear the IC posting flag
			}
			clsDatabaseFunctions.freeConnection(context, conn, "[1547080896]");
			super.addErrorMessage("Could not commit data transaction.");
			return false;
		}
		try {
			option.resetPostingFlagWithoutConnection(context, sDBID);
		} catch (Exception e) {
			//We won't stop for this, but the next user will have to clear the IC posting flag
		}
		clsDatabaseFunctions.freeConnection(context, conn, "[1547080897]");
		return bResult;
	}

	public boolean save_without_data_transaction(Connection conn, String sUserFullName, String sUserID) {

		m_screatedbyfullname = sUserFullName;
		//If the phys inventory is in any state EXCEPT for 'entered', it can't be updated:
		//if (m_sstatus.compareToIgnoreCase(Integer.toString(SMTableicphysicalinventories.STATUS_ENTERED)) != 0){
		//	super.addErrorMessage("Physical inventories that have been processed can not be edited.");
		//	return false;
		//}
		
		if (!validate_entry_fields(conn)) {
			return false;
		}

		return writePhysicalInventory(sUserID, conn);
	}
	public boolean addorremoveItemsFromPhysicalInventory(
			String sAddOrRemove,
			String sStartingItemNumber,
			String sEndingItemNumber,
			String sStartingReportGroup1,
			String sEndingReportGroup1,
			String sStartingReportGroup2,
			String sEndingReportGroup2,
			String sStartingReportGroup3,
			String sEndingReportGroup3,
			String sStartingReportGroup4,
			String sEndingReportGroup4,
			String sStartingReportGroup5,
			String sEndingReportGroup5,
			ServletContext context,
			String sDBID,
			String sUserFullName
			){
		
		//TODO:
		//First validate the ranges:
		if (sStartingItemNumber.compareToIgnoreCase(sEndingItemNumber) > 0){
			super.addErrorMessage("Starting item number is higher than ending item number - cannot continue.");
			return false;
		}
		if (sStartingReportGroup1.compareToIgnoreCase(sEndingReportGroup1) > 0){
			super.addErrorMessage("Starting report group 1 is higher than ending report group 1 - cannot continue.");
			return false;
		}
		if (sStartingReportGroup2.compareToIgnoreCase(sEndingReportGroup2) > 0){
			super.addErrorMessage("Starting report group 2 is higher than ending report group 2 - cannot continue.");
			return false;
		}
		if (sStartingReportGroup3.compareToIgnoreCase(sEndingReportGroup3) > 0){
			super.addErrorMessage("Starting report group 3 is higher than ending report group 3 - cannot continue.");
			return false;
		}
		if (sStartingReportGroup4.compareToIgnoreCase(sEndingReportGroup4) > 0){
			super.addErrorMessage("Starting report group 4 is higher than ending report group 4 - cannot continue.");
			return false;
		}
		if (sStartingReportGroup5.compareToIgnoreCase(sEndingReportGroup5) > 0){
			super.addErrorMessage("Starting report group 5 is higher than ending report group 5 - cannot continue.");
			return false;
		}
		
		//Next, try to add/remove the items:
		if (sAddOrRemove.compareToIgnoreCase(ICAddRemovePhysInvItemsEdit.PARAM_ADD_VALUE) == 0){
			//We need to ADD items:
			try {
				addItems(
					sAddOrRemove,
					sStartingItemNumber,
					sEndingItemNumber,
					sStartingReportGroup1,
					sEndingReportGroup1,
					sStartingReportGroup2,
					sEndingReportGroup2,
					sStartingReportGroup3,
					sEndingReportGroup3,
					sStartingReportGroup4,
					sEndingReportGroup4,
					sStartingReportGroup5,
					sEndingReportGroup5,
					context,
					sDBID,
					sUserFullName)
				;
			} catch (Exception e) {
				super.addErrorMessage("Error adding items: " + e.getMessage() + ".");
				return false;
			}
		}else{
			//We need to REMOVE items:
			try {
				removeItems(
					sAddOrRemove,
					sStartingItemNumber,
					sEndingItemNumber,
					sStartingReportGroup1,
					sEndingReportGroup1,
					sStartingReportGroup2,
					sEndingReportGroup2,
					sStartingReportGroup3,
					sEndingReportGroup3,
					sStartingReportGroup4,
					sEndingReportGroup4,
					sStartingReportGroup5,
					sEndingReportGroup5,
					context,
					sDBID,
					sUserFullName)
				;
			} catch (Exception e) {
				super.addErrorMessage("Error removing items: " + e.getMessage() + ".");
				return false;
			}
		}
		
		return true;
	}
	private void addItems(
			String sAddOrRemove,
			String sStartingItemNumber,
			String sEndingItemNumber,
			String sStartingReportGroup1,
			String sEndingReportGroup1,
			String sStartingReportGroup2,
			String sEndingReportGroup2,
			String sStartingReportGroup3,
			String sEndingReportGroup3,
			String sStartingReportGroup4,
			String sEndingReportGroup4,
			String sStartingReportGroup5,
			String sEndingReportGroup5,
			ServletContext context,
			String sDBID,
			String sUserFullName) throws Exception{
		
		String sInvAcct = "";
		String sWriteOffAcct = "";
		String SQL = "SELECT"
			+ " " + SMTablelocations.sGLInventoryAcct
			+ ", " + SMTablelocations.sGLWriteOffAcct
			+ " FROM "
			+ SMTablelocations.TableName
			+ " WHERE ("
				+ "(" + SMTablelocations.sLocation + " = '" + getLocation() + "')"
			+ ")"
			;
		try {
			ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, context, sDBID, "MySQL", this.toString() + ".getting GL accts - user: " + sUserFullName);
			if (rs.next()){
				sInvAcct = rs.getString(SMTablelocations.sGLInventoryAcct);
				sWriteOffAcct = rs.getString(SMTablelocations.sGLWriteOffAcct);
				rs.close();
			}else{
				rs.close();
				throw new Exception("Could not get GL accounts for location '" + getLocation() + ".");
			}
		} catch (SQLException e1) {
			throw new Exception(
				"Could not get GL accounts for location '" + getLocation() + " - " + e1.getMessage());
		}
		
		SQL = "INSERT IGNORE INTO " + SMTableicinventoryworksheet.TableName + "("
			+ SMTableicinventoryworksheet.bdmostrecentcost
			+ ", " + SMTableicinventoryworksheet.bdqtyonhand
			+ ", " + SMTableicinventoryworksheet.lphysicalinventoryid
			+ ", " + SMTableicinventoryworksheet.sinvacct
			+ ", " + SMTableicinventoryworksheet.sitemnumber
			+ ", " + SMTableicinventoryworksheet.swriteoffacct
			+ ") SELECT "
			+ SMTableicitems.bdmostrecentcost
			+ ", 0.00" 
			+ ", " + this.slid()
			+ ", '" + sInvAcct + "'"
			+ ", " + SMTableicitems.TableName + "." + SMTableicitems.sItemNumber
			+ ", '" + sWriteOffAcct + "'"
			+ " FROM " + SMTableicitems.TableName
			+ " WHERE ("
			+ "(" + SMTableicitems.TableName + "." + SMTableicitems.sItemNumber + " >= '" + sStartingItemNumber + "')"
			+ " AND (" + SMTableicitems.TableName + "." + SMTableicitems.sItemNumber + " <= '" + sEndingItemNumber + "')"
			+ " AND (" + SMTableicitems.TableName + "." + SMTableicitems.sreportgroup1 + " >= '" + sStartingReportGroup1 + "')"
			+ " AND (" + SMTableicitems.TableName + "." + SMTableicitems.sreportgroup1 + " <= '" + sEndingReportGroup1 + "')"
			+ " AND (" + SMTableicitems.TableName + "." + SMTableicitems.sreportgroup2 + " >= '" + sStartingReportGroup2 + "')"
			+ " AND (" + SMTableicitems.TableName + "." + SMTableicitems.sreportgroup2 + " <= '" + sEndingReportGroup2 + "')"
			+ " AND (" + SMTableicitems.TableName + "." + SMTableicitems.sreportgroup3 + " >= '" + sStartingReportGroup3 + "')"
			+ " AND (" + SMTableicitems.TableName + "." + SMTableicitems.sreportgroup3 + " <= '" + sEndingReportGroup3 + "')"
			+ " AND (" + SMTableicitems.TableName + "." + SMTableicitems.sreportgroup4 + " >= '" + sStartingReportGroup4 + "')"
			+ " AND (" + SMTableicitems.TableName + "." + SMTableicitems.sreportgroup4 + " <= '" + sEndingReportGroup4 + "')"
			+ " AND (" + SMTableicitems.TableName + "." + SMTableicitems.sreportgroup5 + " >= '" + sStartingReportGroup5 + "')"
			+ " AND (" + SMTableicitems.TableName + "." + SMTableicitems.sreportgroup5 + " <= '" + sEndingReportGroup5 + "')"
			//Don't want any non-stock items:
			+ " AND (" + SMTableicitems.TableName + "." + SMTableicitems.inonstockitem + " = 0)"
			+ ")"
		;
		if (bDebugMode){
			System.out.println("[1340895900] - SQL to insert phys inv records - " + SQL + ".");
		}
		try {
			clsDatabaseFunctions.executeSQL(SQL, context, sDBID, "MySQL", this.toString() + ".inserting phys inv records - user: " + sUserFullName);
		} catch (Exception e) {
			throw new Exception(
					"Could not insert items into phys inventory with SQL: " + SQL + " - " + e.getMessage());
		}
		
		//Now update the qtys on hand:
		SQL = "UPDATE " + SMTableicitems.TableName
			+ " LEFT JOIN " + SMTableicinventoryworksheet.TableName
			+ " ON " + SMTableicinventoryworksheet.TableName + "." + SMTableicinventoryworksheet.sitemnumber
			+ " = " + SMTableicitems.TableName + "." + SMTableicitems.sItemNumber
			+ " LEFT JOIN " + SMTableicitemlocations.TableName
			+ " ON " + SMTableicitems.TableName + "." + SMTableicitems.sItemNumber + " = "
			+ SMTableicitemlocations.TableName + "." + SMTableicitemlocations.sItemNumber
			+ " SET " + SMTableicinventoryworksheet.TableName + "." + SMTableicinventoryworksheet.bdqtyonhand
			+ " = " + SMTableicitemlocations.TableName + "." + SMTableicitemlocations.sQtyOnHand
			+ " WHERE ("
				+ "(" + SMTableicitems.TableName + "." + SMTableicitems.sItemNumber + " >= '" + sStartingItemNumber + "')"
				+ " AND (" + SMTableicitems.TableName + "." + SMTableicitems.sItemNumber + " <= '" + sEndingItemNumber + "')"
				+ " AND (" + SMTableicitems.TableName + "." + SMTableicitems.sreportgroup1 + " >= '" + sStartingReportGroup1 + "')"
				+ " AND (" + SMTableicitems.TableName + "." + SMTableicitems.sreportgroup1 + " <= '" + sEndingReportGroup1 + "')"
				+ " AND (" + SMTableicitems.TableName + "." + SMTableicitems.sreportgroup2 + " >= '" + sStartingReportGroup2 + "')"
				+ " AND (" + SMTableicitems.TableName + "." + SMTableicitems.sreportgroup2 + " <= '" + sEndingReportGroup2 + "')"
				+ " AND (" + SMTableicitems.TableName + "." + SMTableicitems.sreportgroup3 + " >= '" + sStartingReportGroup3 + "')"
				+ " AND (" + SMTableicitems.TableName + "." + SMTableicitems.sreportgroup3 + " <= '" + sEndingReportGroup3 + "')"
				+ " AND (" + SMTableicitems.TableName + "." + SMTableicitems.sreportgroup4 + " >= '" + sStartingReportGroup4 + "')"
				+ " AND (" + SMTableicitems.TableName + "." + SMTableicitems.sreportgroup4 + " <= '" + sEndingReportGroup4 + "')"
				+ " AND (" + SMTableicitems.TableName + "." + SMTableicitems.sreportgroup5 + " >= '" + sStartingReportGroup5 + "')"
				+ " AND (" + SMTableicitems.TableName + "." + SMTableicitems.sreportgroup5 + " <= '" + sEndingReportGroup5 + "')"
				+ " AND (" + SMTableicitemlocations.TableName + "." + SMTableicitemlocations.sLocation + " = '" + m_slocation + "')"
				+ " AND (" + SMTableicinventoryworksheet.TableName + "." + SMTableicinventoryworksheet.lphysicalinventoryid + " = " + this.slid() + ")"
			+ ")"
		;
		if (bDebugMode){
			System.out.println("[1340895901] - SQL to update qtys on phys inv records - " + SQL + ".");
		}
		try {
			clsDatabaseFunctions.executeSQL(SQL, context, sDBID, "MySQL", this.toString() + ".updating qtys on phys inv records - user: " + sUserFullName);
		} catch (Exception e) {
			throw new Exception(
					"Could not update qtys on phys inventory with SQL: " + SQL + " - " + e.getMessage());
		}
		
	}
	private void removeItems(
			String sAddOrRemove,
			String sStartingItemNumber,
			String sEndingItemNumber,
			String sStartingReportGroup1,
			String sEndingReportGroup1,
			String sStartingReportGroup2,
			String sEndingReportGroup2,
			String sStartingReportGroup3,
			String sEndingReportGroup3,
			String sStartingReportGroup4,
			String sEndingReportGroup4,
			String sStartingReportGroup5,
			String sEndingReportGroup5,
			ServletContext context,
			String sDBID,
			String sUserFullName) throws Exception{
		//This function removes items from the physical inventory worksheet, but only if none of them have been counted yet:
		String SQL = "SELECT"
			+ " " + SMTableicitems.TableName + "." + SMTableicitems.sItemNumber
			+ ", " + SMTableicphysicalcountlines.TableName + "." + SMTableicphysicalcountlines.sitemnumber
			+ " FROM " + SMTableicitems.TableName + " LEFT JOIN " + SMTableicphysicalcountlines.TableName
			+ " ON " + SMTableicitems.TableName + "." + SMTableicitems.sItemNumber + " = "
			+ SMTableicphysicalcountlines.TableName + "." + SMTableicphysicalcountlines.sitemnumber
			+ " WHERE ("
			+ "(" + SMTableicitems.TableName + "." + SMTableicitems.sItemNumber + " >= '" + sStartingItemNumber + "')"
			+ " AND (" + SMTableicitems.TableName + "." + SMTableicitems.sItemNumber + " <= '" + sEndingItemNumber + "')"
			+ " AND (" + SMTableicitems.TableName + "." + SMTableicitems.sreportgroup1 + " >= '" + sStartingReportGroup1 + "')"
			+ " AND (" + SMTableicitems.TableName + "." + SMTableicitems.sreportgroup1 + " <= '" + sEndingReportGroup1 + "')"
			+ " AND (" + SMTableicitems.TableName + "." + SMTableicitems.sreportgroup2 + " >= '" + sStartingReportGroup2 + "')"
			+ " AND (" + SMTableicitems.TableName + "." + SMTableicitems.sreportgroup2 + " <= '" + sEndingReportGroup2 + "')"
			+ " AND (" + SMTableicitems.TableName + "." + SMTableicitems.sreportgroup3 + " >= '" + sStartingReportGroup3 + "')"
			+ " AND (" + SMTableicitems.TableName + "." + SMTableicitems.sreportgroup3 + " <= '" + sEndingReportGroup3 + "')"
			+ " AND (" + SMTableicitems.TableName + "." + SMTableicitems.sreportgroup4 + " >= '" + sStartingReportGroup4 + "')"
			+ " AND (" + SMTableicitems.TableName + "." + SMTableicitems.sreportgroup4 + " <= '" + sEndingReportGroup4 + "')"
			+ " AND (" + SMTableicitems.TableName + "." + SMTableicitems.sreportgroup5 + " >= '" + sStartingReportGroup5 + "')"
			+ " AND (" + SMTableicitems.TableName + "." + SMTableicitems.sreportgroup5 + " <= '" + sEndingReportGroup5 + "')"
			+ " AND (" + SMTableicphysicalcountlines.TableName + "." + SMTableicphysicalcountlines.lphysicalinventoryid + " = " + slid() + ")"
			+ " AND (" + SMTableicphysicalcountlines.TableName + "." + SMTableicphysicalcountlines.sitemnumber + " IS NOT NULL)"
			+ ")"
		;
		if (bDebugMode){
			System.out.println("[1340895878] - Remove item phys inv SELECT SQL: " + SQL);
		}
		ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, context, sDBID, "MySQL", this.toString() + ".removeItems - SELECT - user: " + sUserFullName);
		if (rs.next()){
			rs.close();
			throw new Exception("Some of the items you are trying to remove already have counts entered for them - you cannot remove the items selected.");
		}else{
			rs.close();
		}

		SQL = "DELETE FROM " + SMTableicinventoryworksheet.TableName + " USING"
				+ " " + SMTableicinventoryworksheet.TableName + ", " + SMTableicitems.TableName
				+ " WHERE ("
				+ "(" + SMTableicitems.TableName + "." + SMTableicitems.sItemNumber + " >= '" + sStartingItemNumber + "')"
				+ " AND (" + SMTableicitems.TableName + "." + SMTableicitems.sItemNumber + " <= '" + sEndingItemNumber + "')"
				+ " AND (" + SMTableicitems.TableName + "." + SMTableicitems.sreportgroup1 + " >= '" + sStartingReportGroup1 + "')"
				+ " AND (" + SMTableicitems.TableName + "." + SMTableicitems.sreportgroup1 + " <= '" + sEndingReportGroup1 + "')"
				+ " AND (" + SMTableicitems.TableName + "." + SMTableicitems.sreportgroup2 + " >= '" + sStartingReportGroup2 + "')"
				+ " AND (" + SMTableicitems.TableName + "." + SMTableicitems.sreportgroup2 + " <= '" + sEndingReportGroup2 + "')"
				+ " AND (" + SMTableicitems.TableName + "." + SMTableicitems.sreportgroup3 + " >= '" + sStartingReportGroup3 + "')"
				+ " AND (" + SMTableicitems.TableName + "." + SMTableicitems.sreportgroup3 + " <= '" + sEndingReportGroup3 + "')"
				+ " AND (" + SMTableicitems.TableName + "." + SMTableicitems.sreportgroup4 + " >= '" + sStartingReportGroup4 + "')"
				+ " AND (" + SMTableicitems.TableName + "." + SMTableicitems.sreportgroup4 + " <= '" + sEndingReportGroup4 + "')"
				+ " AND (" + SMTableicitems.TableName + "." + SMTableicitems.sreportgroup5 + " >= '" + sStartingReportGroup5 + "')"
				+ " AND (" + SMTableicitems.TableName + "." + SMTableicitems.sreportgroup5 + " <= '" + sEndingReportGroup5 + "')"
				+ " AND (" + SMTableicinventoryworksheet.TableName + "." + SMTableicinventoryworksheet.sitemnumber + " = "
				+ SMTableicitems.TableName + "." + SMTableicitems.sItemNumber + ")"
				+ " AND (" + SMTableicinventoryworksheet.TableName + "." + SMTableicinventoryworksheet.lphysicalinventoryid 
					+ " = " + this.slid() + ")"
				+ ")"
		;
		if (bDebugMode){
			System.out.println("[1340895888] - Remove item phys inv DELETE SQL: " + SQL);
		}

		try {
			clsDatabaseFunctions.executeSQL(SQL, context, sDBID, "MySQL", this.toString() + ".removeItems - SELECT - user: " + sUserFullName);
		} catch (Exception e) {
			throw new Exception("Error removing items from physical inventory with SQL: " + SQL + " - " + e.getMessage());
		}
		
		return;
	}
	private boolean writePhysicalInventory(String sUserID, Connection conn){
		boolean bResult;
		if (!clsDatabaseFunctions.start_data_transaction(conn)){
			super.addErrorMessage("Could not start data transaction.");
			return false;
		}
		if (m_sid.compareToIgnoreCase("-1") == 0) {
			bResult = insertPhysicalInventory(sUserID, conn);
		} else {
			if (bDebugMode){
				System.out.println(
						"[1579203207] In " + this.toString() 
						+ ".writePhysicalInventory - going into updatePhysicalInventory(conn)");
			}
			bResult =  updatePhysicalInventory(conn);
		}
		
		if (!bResult){
			clsDatabaseFunctions.rollback_data_transaction(conn);
		}else{
			if (!clsDatabaseFunctions.commit_data_transaction(conn)){
				super.addErrorMessage("Could not commit data transaction.");
				bResult = false;
			}
		}
		return bResult;
	}
	private boolean updatePhysicalInventory(Connection conn){

		String SQL = "UPDATE " + SMTableicphysicalinventories.TableName + " SET "
				+ " " + SMTableicphysicalinventories.istatus + " = " + m_sstatus 
				+ ", " + SMTableicphysicalinventories.lbatchnumber + " = "
					+ m_sbatchnumber 
				+ ", " + SMTableicphysicalinventories.sdesc + " = '" 
					+ clsDatabaseFunctions.FormatSQLStatement(m_sdesc) + "'"
				+ " WHERE (" + SMTableicphysicalinventories.lid + " = "
				+ m_sid + ")";
		if (bDebugMode){
			System.out.println(
					"[1579203211] In " + this.toString() 
					+ ".updatePhysicalInventory - SQL = " + SQL);
		}

		try {
			if (clsDatabaseFunctions.executeSQL(SQL, conn) == false) {
				System.out.println("[1579203219] " + this.toString() + "Could not update "
						+ ParamObjectName + ".<BR>");
				super.addErrorMessage("Could not update "
						+ ParamObjectName + " with SQL: " + SQL);
				return false;
			} else {
			}
		} catch (SQLException ex) {
			super.addErrorMessage("Error inserting " + ParamObjectName + ": "
					+ ex.getMessage());
			return false;
		}
		
		if (bDebugMode){
			System.out.println(
					"[1579203225] In " + this.toString() 
					+ ".updatePhysicalInventory - going into updateInventoryWorksheet(conn)");
		}

		//return updateInventoryWorksheet(conn);
		return true;
	}
	/*
	private boolean updateInventoryWorksheet(Connection conn){
		
		//First, get a resultset of items that are not already in the worksheet:
		String SQL = "SELECT"
			+ " " + SMTableicitems.TableName + "." + SMTableicitems.sItemNumber
			+ ", " + SMTableicitems.TableName + "." + SMTableicitems.bdmostrecentcost
			+ ", " + SMTableicinventoryworksheet.TableName + "." 
				+ SMTableicinventoryworksheet.lphysicalinventoryid
			+ " FROM " + SMTableicitems.TableName + " LEFT JOIN " + SMTableicinventoryworksheet.TableName 
				+ " ON " + SMTableicitems.TableName + "." + SMTableicitems.sItemNumber + " = " 
					+ SMTableicinventoryworksheet.TableName + "." + SMTableicinventoryworksheet.sitemnumber
			+ " WHERE ("
				+ "(" + SMTableicitems.TableName + "." + SMTableicitems.sItemNumber + " >= '" 
					+ m_sstartingitemnumber + "')"
				+ " And (" + SMTableicitems.TableName + "." + SMTableicitems.sItemNumber + " <= '" 
					+ m_sendingitemnumber + "')"
				+ " AND (ISNULL(" + SMTableicinventoryworksheet.TableName + "." 
					+ SMTableicinventoryworksheet.lphysicalinventoryid + ") = true)"
			+ ")"
		;
		
		if (bDebugMode){
			System.out.println(
					"In " + this.toString() 
					+ ".updateInventoryWorksheet - SQL = " + SQL);
		}

		try {
			ResultSet rsItems = SMUtilities.openResultSet(SQL, conn);
			while (rsItems.next()){
				if (!insertMissingItemForLocation(
					rsItems.getString(SMTableicitems.sItemNumber),
					rsItems.getBigDecimal(SMTableicitems.bdmostrecentcost),
					m_slocation,
					conn
				)){
					return false;
				}
			}
			rsItems.close();
		} catch (SQLException e) {
			super.addErrorMessage("Error getting missing items - " + e.getMessage());
			return false;
		}

		if (bDebugMode){
			System.out.println(
					"In " + this.toString() 
					+ ".updateInventoryWorksheet - returning TRUE");
		}

		return true; 
		
	}
	
	private boolean insertMissingItemForLocation(
			String sItem, 
			BigDecimal bdMostRecentCost, 
			String sLocation,
			Connection conn
			){
		
		BigDecimal bdQtyOnHand = new BigDecimal(0);
		
		//First get the itemlocation info, if there is any:
		String SQL = "SELECT "
			+ SMTableicitemlocations.sQtyOnHand
			+ " FROM " + SMTableicitemlocations.TableName
			+ " WHERE ("
				+ "(" + SMTableicitemlocations.sItemNumber + " = '" + sItem + "')"
				+ " AND (" + SMTableicitemlocations.sLocation + " = '" + sLocation + "')"
			+ ")"
			;
		
		try {
			ResultSet rsItemLoc = SMUtilities.openResultSet(SQL, conn);
			if (rsItemLoc.next()){
				bdQtyOnHand = rsItemLoc.getBigDecimal(SMTableicitemlocations.sQtyOnHand);
			}
			rsItemLoc.close();
		} catch (SQLException e1) {
			super.addErrorMessage("Error reading item location file to insert missing item - SQL: " + SQL
				+ " - error: " + e1.getMessage()
			);
			return false;
		}
		
		SQL = "INSERT INTO "
			+ SMTableicinventoryworksheet.TableName
			+ " ("
			+ SMTableicinventoryworksheet.bdqtyonhand
			+ ", " + SMTableicinventoryworksheet.lphysicalinventoryid
			+ ", " + SMTableicinventoryworksheet.sitemnumber
			+ ", " + SMTableicinventoryworksheet.bdmostrecentcost
			+ ") VALUES ("
			+ SMUtilities.BigDecimalToFormattedString("########0.0000", bdQtyOnHand)
			+ ", " + m_sid
			+ ", '" + sItem + "'"
			+ ", " + SMUtilities.BigDecimalTo2DecimalSQLFormat(
				bdMostRecentCost.setScale(SMTableicinventoryworksheet.bdmostrecentcostScale,
						BigDecimal.ROUND_HALF_UP))
			+ ")"
			;
		if (bDebugMode){
			System.out.println(
					"In " + this.toString() 
					+ ".insertMissingItemForLocation - SQL = " + SQL);
		}

		try {
			if (!SMUtilities.executeSQL(SQL, conn)){
				super.addErrorMessage("Error inserting missing item with SQL: " + SQL + ".");
				return false;
			}
		} catch (SQLException e) {
			super.addErrorMessage("Error inserting missing item with SQL: " + SQL + " - " + e.getMessage());
			return false;
		}
		
		if (bDebugMode){
			System.out.println(
					"In " + this.toString() 
					+ ".insertMissingItemForLocation - returning TRUE");
		}

		return true;
	}
	*/
	private boolean insertPhysicalInventory(String sUserID, Connection conn){
		
		//TJR - 6/25/2012 - Removed all of this logic so that items could be added and removed from a physical inventory.
		//You can create as many physical inventories for the same location as necessary now; the only restriction is that
		//no two physical inventories for the same location can have the same items in it.
		
		/*
		// Have to make sure there are NO OTHER unprocessed physical
		// inventories - we can only have one
		// physical inventory open at a time for a particular location and we also have to make sure
		// that if there IS another physical inventory for that location, there's no overlap on the 
		//items being counted:
		String SQL = "SELECT"
				+ " "
				+ SMTableicphysicalinventories.lbatchnumber
				+ " FROM "
				+ SMTableicphysicalinventories.TableName
				+ " WHERE ("
					+ "(" + SMTableicphysicalinventories.istatus + " = " + Integer.toString(
						SMTableicphysicalinventories.STATUS_ENTERED) + ")"
					+ " AND (" + SMTableicphysicalinventories.slocation + " = '" + m_slocation + "')"
					
					// If the ending item of the new inv is >= the current starting number of
					// any existing physical inventory
					+ " AND ('" + m_sendingitemnumber + "' >= " 
					+ SMTableicphysicalinventories.sstartingitemnumber + ")"

					// AND

					// if the starting item of the new inv is <= the current ending number of
					// any existing physical inventory
					+ " AND ('" + m_sstartingitemnumber + "' <= " 
					+ SMTableicphysicalinventories.sendingitemnumber + ")"
					
				+ ")";
		//System.out.println(SQL);
		try {
			ResultSet rsPhysInvs = SMUtilities.openResultSet(SQL, conn);
			if (rsPhysInvs.next()) {
				super.addErrorMessage("Cannot create a new physical inventory because there is at least one"
								+ " for this location already, with overlapping item numbers, waiting to be processed."
								+ "  You can only have ONE physical inventory for a location and a specified range of"
								+ " items open for processing at a time.");
				return false;

			}
			rsPhysInvs.close();
		} catch (SQLException e) {
			super.addErrorMessage("Error checking for unprocessed physical inventories - "
							+ e.getMessage());
			return false;
		}
		*/
		//Insert new physical inventory record:
		String SQL = "INSERT INTO " + SMTableicphysicalinventories.TableName + "("
				+ SMTableicphysicalinventories.datcreated 
				+ ", " + SMTableicphysicalinventories.istatus 
				+ ", " + SMTableicphysicalinventories.lbatchnumber 
				+ ", " + SMTableicphysicalinventories.screatedbyfullname
				+ ", " + SMTableicphysicalinventories.lcreatedbyuserid
				+ ", " + SMTableicphysicalinventories.sdesc
				+ ", " + SMTableicphysicalinventories.slocation
				//+ ", " + SMTableicphysicalinventories.sstartingitemnumber
				//+ ", " + SMTableicphysicalinventories.sendingitemnumber
				+ ") VALUES ("
				+ "NOW()" 
				+ ", " + m_sstatus 
				+ ", " + m_sbatchnumber
				+ ", '" + clsDatabaseFunctions.FormatSQLStatement(m_screatedbyfullname) + "'"
				+ ", '" + sUserID + "'" 
				+ ", '" + clsDatabaseFunctions.FormatSQLStatement(m_sdesc) + "'"
				+ ", '" + clsDatabaseFunctions.FormatSQLStatement(m_slocation) + "'"
				//+ ", '" + SMUtilities.FormatSQLStatement(m_sstartingitemnumber) + "'"
				//+ ", '" + SMUtilities.FormatSQLStatement(m_sendingitemnumber) + "'"
				+ ")"
				;
		try {
			if (clsDatabaseFunctions.executeSQL(SQL, conn) == false) {
				System.out.println("[1579203239] " + this.toString() + "Could not insert "
						+ ParamObjectName + ".<BR>");
				super.addErrorMessage("Could not insert "
						+ ParamObjectName + " with SQL: " + SQL);
				return false;
			} else {
			}
		} catch (SQLException ex) {
			super.addErrorMessage("Error inserting " + ParamObjectName + ": "
					+ ex.getMessage());
			return false;
		}
		SQL = "SELECT last_insert_id()";
		try {
			ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
			if (rs.next()) {
				m_sid = Long.toString(rs.getLong(1));
			} else {
				m_sid = "0";
			}
			rs.close();
		} catch (SQLException e) {
			super.addErrorMessage("Could not get last ID number - "
					+ e.getMessage());
			return false;
		}
		// If something went wrong, we can't get the last ID:
		if (m_sid.compareToIgnoreCase("0") == 0) {
			super.addErrorMessage("Could not get last ID number.");
			return false;
		}
		
		//Now reload it to get the created date:
		if (!load(conn)){
			super.addErrorMessage("Could reload entry to get created date.");
			return false;
		}
		
		//TJR - removed this 6/25/2012 - we no longer add items to a physical inventory automatically - this is
		//done by the user in a separate function:
		/*
		//Create the inventory worksheet records IF it's a new record:
		if (!createInvWorksheetRecords (conn)){
			return false;
		}
		*/
		return true;
	}
	/*
	private boolean setPostingFlag(Connection conn, String sUserFullName, String sProcessLabel){
		//Check the ic posting flag, then set it:
    	//First check to make sure no one else is posting:
		String SQL = "";
		ICOption option = new ICOption();
		try{
			String sPostingProcess = sProcessLabel;
			option.checkAndUpdateICPostingFlagUsingConnection(conn, sUserFullName, sPostingProcess);
    	}catch (Exception e){
			super.addErrorMessage("Error checking for previous posting - " + e.getMessage());
    		return false;
    	}
    	//If not, then set the posting flag:
    	try{
    		SQL = "UPDATE " + SMTableicoptions.TableName 
    			+ " SET " + SMTableicoptions.ibatchpostinginprocess + " = 1"
    			+ ", " + SMTableicoptions.datstartdate + " = NOW()"
    			+ ", " + SMTableicoptions.sprocess 
    				+ " = '" + sProcessLabel + "'"
       			+ ", " + SMTableicoptions.suserfullname 
    			+ " = '" + sUserFullName + "'"
    			+ ", " + SMTableicoptions.lpostingtimestamp
			+ " = " +option.getPostingTimeStamp()
    			;
    		if (!clsDatabaseFunctions.executeSQL(SQL, conn)){
        		super.addErrorMessage("Error setting posting flag in icoptions");
        		return false;
    		}
    	}catch (SQLException e){
			super.addErrorMessage("Error setting posting flag in icoptions - " + e.getMessage());
    		return false;
    	}
    	return true;
	}
*/
	//TODO - rebuild this to use a range of items, report groups, etc:
	/*
	private boolean createInvWorksheetRecords(Connection con){
		
		String SQLInsert = "INSERT INTO " + SMTableicinventoryworksheet.TableName
			+ "("
				+ SMTableicinventoryworksheet.sitemnumber
				+ ", " + SMTableicinventoryworksheet.lphysicalinventoryid
			+ ")"
			+ " SELECT"
			+ " " + SMTableicitems.TableName + "." + SMTableicitems.sItemNumber
				+ " AS " + SMTableicinventoryworksheet.sitemnumber
			+ ", " + m_sid
				+ " AS " + SMTableicinventoryworksheet.lphysicalinventoryid
			+ " FROM " + SMTableicitems.TableName
			+ " WHERE ("
				+ "(" + SMTableicitems.TableName + "." + SMTableicitems.sItemNumber 
					+ " >= '" + m_sstartingitemnumber + "')" 
				+ " AND (" + SMTableicitems.TableName + "." + SMTableicitems.sItemNumber 
					+ " <= '" + m_sendingitemnumber + "')" 
			+ ")"
		;
		try{
		    Statement stmt = con.createStatement();
		    stmt.executeUpdate(SQLInsert);
		}catch (Exception ex) {
			super.addErrorMessage("Error inserting records with SQL = "	+ SQLInsert + ex.getMessage());
			return false;
		}
		
		//Now populate the inventory worksheet records with the current quantities on hand:
		String SQL = "UPDATE"
			+ " " + SMTableicinventoryworksheet.TableName + ", " + SMTableicitemlocations.TableName
			+ " SET " + SMTableicinventoryworksheet.TableName + "." + SMTableicinventoryworksheet.bdqtyonhand
				+ " = " + SMTableicitemlocations.TableName + "." + SMTableicitemlocations.sQtyOnHand
			+ " WHERE ("
				+ "(" + SMTableicinventoryworksheet.TableName + "." 
					+ SMTableicinventoryworksheet.lphysicalinventoryid + " = " + m_sid + ")"
				+ " AND (" + SMTableicitemlocations.TableName + "." + SMTableicitemlocations.sItemNumber 
					+ " = " + SMTableicinventoryworksheet.TableName + "." 
					+ SMTableicinventoryworksheet.sitemnumber + ")"
				+ " AND (" + SMTableicitemlocations.TableName + "." + SMTableicitemlocations.sLocation 
					+ " = " + getLocation() + ")"
			+ ")"
			;
		try{
		    Statement stmt = con.createStatement();
		    stmt.executeUpdate(SQL);
		}catch (Exception ex) {
			super.addErrorMessage("Error updating MRC with SQL = "	+ SQL + " - " + ex.getMessage());
			return false;
		}
		
		//Populate the current worksheet with GL accounts, based on the account set of the location:
		if (!updateGLAccts(con)){
			return false;
		}
		
		return true;
	}
	
	private boolean updateGLAccts(Connection conn){
		
		String sInvAcct = "";
		String sWriteOffAcct = "";
		String SQL = "SELECT"
			+ " " + SMTablelocations.sGLInventoryAcct
			+ ", " + SMTablelocations.sGLWriteOffAcct
			+ " FROM "
			+ SMTablelocations.TableName
			+ " WHERE ("
				+ "(" + SMTablelocations.sLocation + " = '" + getLocation() + "')"
			+ ")"
			;
		try {
			ResultSet rs = SMUtilities.openResultSet(SQL, conn);
			if (rs.next()){
				sInvAcct = rs.getString(SMTablelocations.sGLInventoryAcct);
				sWriteOffAcct = rs.getString(SMTablelocations.sGLWriteOffAcct);
				rs.close();
			}else{
				super.addErrorMessage("Could not get GL accounts for location '" + getLocation() + ".");
				rs.close();
				return false;
			}
		} catch (SQLException e1) {
			super.addErrorMessage(
				"Could not get GL accounts for location '" + getLocation() + " - " + e1.getMessage());
			return false;
		}
		
		SQL = "UPDATE"
			+ " " + SMTableicinventoryworksheet.TableName
			+ " SET " + SMTableicinventoryworksheet.TableName + "." + SMTableicinventoryworksheet.sinvacct 
			+ " = '" + sInvAcct + "', " 
			+ SMTableicinventoryworksheet.TableName + "." + SMTableicinventoryworksheet.swriteoffacct 
			+ " = '" + sWriteOffAcct + "'"
			;
			
		try{
		    Statement stmt = conn.createStatement();
		    stmt.executeUpdate(SQL);
		}catch (Exception ex) {
			super.addErrorMessage(
					"Error updating worksheet with GL accounts - SQL = " + ex.getMessage() + ".");
				return false;
		}
		
		return true;
	}
	*/
	public boolean delete(ServletContext context, String sDBID, String sUserID, String sUserFullName) {

		Connection conn = clsDatabaseFunctions.getConnection(context, sDBID, "MySQL",
				this.toString() + " - user: " + sUserID
				+ " - "
				+ sUserFullName
				);

		if (conn == null) {
			super.addErrorMessage("Error opening data connection.");
			return false;
		}

		boolean bResult = delete(conn);
		clsDatabaseFunctions.freeConnection(context, conn, "[1547080891]");
		return bResult;

	}
	public boolean postToBatch(String sDBID, ServletContext context, String sUserID, String sUserFullName){
		
		//Get a connection:
		Connection conn = clsDatabaseFunctions.getConnection(
				context, 
				sDBID, 
				"MySQL", 
				SMUtilities.getFullClassName(this.toString())
					+ ".postToBatch - user: " 
						+ sUserID
						+ " - "
						+ sUserFullName
		);
		
		if (conn == null){
			super.addErrorMessage("Could not get connection.");
			return false;
		}
		
		//First, set the in process flag:
		try {
			option.checkAndUpdatePostingFlagWithoutConnection(
				context, 
				sDBID, 
				clsServletUtilities.getFullClassName(this.toString()) + ".postToBatch", 
				sUserFullName, 
				"POSTING PHYSICAL INVENTORY");
		} catch (Exception e1) {
			addErrorMessage("Error [1529957326] - " + e1.getMessage());
			return false;
		}
		
		//Do the actual posting:
		if (!processPostingToBatch(conn, sUserID, sUserFullName)){
			clsDatabaseFunctions.freeConnection(context, conn, "[1547080893]");
			return false;
		}
		
		try {
			option.resetPostingFlagWithoutConnection(context, sDBID);
		} catch (Exception e) {
			//We won't stop for this, but the next user will have to clear the IC posting flag
		}
		clsDatabaseFunctions.freeConnection(context, conn, "[1547080894]");
		return true;
	}
	private boolean processPostingToBatch(Connection conn, String sUserID, String sUserFullName){
		
		//First, start a data transaction:
		if (!clsDatabaseFunctions.start_data_transaction(conn)){
			super.addErrorMessage("Could not start data transaction.");
			return false;
		}
		
		//Do the posting
		if (!runPostingProcedures(conn, sUserID, sUserFullName, getLocation())){
			clsDatabaseFunctions.rollback_data_transaction(conn);
			return false;
		}else{
			clsDatabaseFunctions.commit_data_transaction(conn);
			return true;
		}
	}
	private boolean runPostingProcedures(Connection conn, String sUserID, String sUserFullName, String sLocation){
		
		//Need to create a new batch and place the inventory worksheet into it:
		ICEntryBatch batch = new ICEntryBatch("-1");
		batch.iBatchStatus(SMBatchStatuses.IMPORTED);
		batch.iBatchType(ICBatchTypes.IC_PHYSICALCOUNT);
		batch.sBatchDescription("Created from physical inventory #" + this.slid());
		batch.sSetCreatedByFullName(sUserFullName);
		batch.sSetCreatedByID(sUserID);
		batch.setBatchDate(clsDateAndTimeConversions.now("yyyy-MM-dd"));
		batch.sModuleType(SMModuleTypes.IC);
		
		//First save the batch to get a batch number:
		//Save the batch:
		if (!batch.save_without_data_transaction(conn, sUserFullName, sUserID)){
			super.addErrorMessage("Could not save created batch - " + batch.getErrorMessages());
			return false;
		}
		
		if (!addBatchEntry(
				sLocation,
				batch,
				conn,
				sUserID
				)){
			return false;
		}
				
		//Save the batch:
		if (!batch.save_without_data_transaction(conn, sUserFullName, sUserID)){
			super.addErrorMessage("Could not save created batch - " + batch.getErrorMessages());
			return false;
		}
		
		//Next, remove the records from the inventory worksheet:
		if (!removePhysicalInventoryRecords(conn)){
			return false;
		}
		
		//Flag the physical inventory as posted:
		m_sbatchnumber = batch.sBatchNumber();
		m_sstatus = Integer.toString(SMTableicphysicalinventories.STATUS_BATCHED);
		if(!save_without_data_transaction(conn, sUserFullName, sUserID)){
			super.addErrorMessage("Could not save physical inventory - " + this.getErrorMessages());
			return false;
		}
		
		return true;
	}
	private boolean removePhysicalInventoryRecords(Connection conn){
		
		String SQL = "DELETE"
			+ " FROM " + SMTableicinventoryworksheet.TableName
			+ " WHERE ("
				+ SMTableicinventoryworksheet.lphysicalinventoryid + " = " + m_sid
			+ ")"
			;
		
		try {
			if (!clsDatabaseFunctions.executeSQL(SQL, conn)){
				super.addErrorMessage("Error removing physical inventory worksheet records.");
				return false;
			}
		} catch (SQLException e) {
			super.addErrorMessage("Error removing physical inventory worksheet records - " + e.getMessage());
			return false;
		}
		
		return true;
	}
	private boolean addBatchEntry(String sLocation, ICEntryBatch entrybatch, Connection conn, String sUserID){
		
		//Add an entry for all the physical inventory variances for this location:
		ICEntry batchentry = new ICEntry();
		batchentry.sBatchNumber(entrybatch.sBatchNumber());
		batchentry.sBatchType(entrybatch.sBatchType());
		batchentry.sDocNumber("PI" + this.slid() + "-LOC" + sLocation);
		batchentry.sEntryDate(clsDateAndTimeConversions.now("MM/dd/yyyy"));
		batchentry.sEntryDescription("Combined physical counts for location " + sLocation);
		batchentry.sEntryNumber("-1");
		batchentry.sEntryType(Integer.toString(ICEntryTypes.PHYSICALCOUNT_ENTRY));

		//Get the GL Inventory Acct for this location:
		String SQL = "SELECT "
			+ SMTablelocations.sGLInventoryAcct
			+ ", " + SMTablelocations.sGLWriteOffAcct
			+ " FROM " + SMTablelocations.TableName
			+ " WHERE ("
				+ SMTablelocations.sLocation + " = '" + sLocation + "'"
			+ ")"
			;
		String sLocationInventoryGLAcct = "";
		String sLocationWriteOffGLAcct = "";
		try {
			ResultSet rsLocation = clsDatabaseFunctions.openResultSet(SQL, conn);
			if (rsLocation.next()){
				sLocationInventoryGLAcct = rsLocation.getString(SMTablelocations.sGLInventoryAcct);
				sLocationWriteOffGLAcct = rsLocation.getString(SMTablelocations.sGLWriteOffAcct);
				rsLocation.close();
			}else{
				super.addErrorMessage("Could not read default GL accts for location " + sLocation + ".");
				rsLocation.close();
				return false;
			}
		} catch (SQLException e1) {
			super.addErrorMessage(
				"Error reading default GL accts for location " + sLocation + " - " + e1.getMessage());
			return false;		}
		
		//Main SQL:
		SQL = "SELECT"
			+ " " + SMTableicinventoryworksheet.TableName + "." + SMTableicinventoryworksheet.lphysicalinventoryid
			+ ", " + SMTableicinventoryworksheet.TableName + "." + SMTableicinventoryworksheet.sitemnumber
			+ ", " + SMTableicinventoryworksheet.TableName + "." + SMTableicinventoryworksheet.bdqtyonhand
			+ ", CountSumQuery.LocationItemQtyCounted"
			+ " FROM " + SMTableicinventoryworksheet.TableName + " LEFT JOIN"
			+ " " + "(SELECT " + SMTableicphysicalcountlines.lphysicalinventoryid
			+ ", SUM(" + SMTableicphysicalcountlines.bdqty + ") AS LocationItemQtyCounted"
			+ ", " + SMTableicphysicalcountlines.sitemnumber
			//+ ", " + SMTableicphysicalcountlines.slocation
			+ " FROM " + SMTableicphysicalcountlines.TableName
			+ " WHERE ("
				+ SMTableicphysicalcountlines.TableName + "." 
				+ SMTableicphysicalcountlines.lphysicalinventoryid + " = " + this.slid()
			+ ")"
			+ " GROUP BY " + SMTableicphysicalcountlines.lphysicalinventoryid
			//+ ", " + SMTableicphysicalcountlines.slocation
			+ ", " + SMTableicphysicalcountlines.sitemnumber 
			+ ") AS CountSumQuery"
			+ " ON"
			+ " (" + SMTableicinventoryworksheet.TableName + "." + SMTableicinventoryworksheet.sitemnumber
			+ " = CountSumQuery." + SMTableicphysicalcountlines.sitemnumber + ")"
			+ " AND (" + SMTableicinventoryworksheet.TableName + "." 
			+ SMTableicinventoryworksheet.lphysicalinventoryid 
			+ " = CountSumQuery." + SMTableicphysicalcountlines.lphysicalinventoryid + ")"
			+ " WHERE ("
				+ "(" + SMTableicinventoryworksheet.TableName + "." 
				+ SMTableicinventoryworksheet.lphysicalinventoryid + " = " + this.slid() + ")"
			+ ")"
		;
		if (bDebugMode){
			System.out.println("[1579203248] In " + this.toString() + ".addBatchEntry - 1st SQL = " + SQL);
		}
		BigDecimal bdQtyOnHand = new BigDecimal(0);
		BigDecimal bdQtyCounted = new BigDecimal(0);
		try {
			ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
			while (rs.next()){
				bdQtyOnHand = rs.getBigDecimal(
					SMTableicinventoryworksheet.TableName 
					+ "." + SMTableicinventoryworksheet.bdqtyonhand);
				if (bdQtyOnHand == null){
					bdQtyOnHand = BigDecimal.ZERO;
				}
				bdQtyCounted = rs.getBigDecimal("LocationItemQtyCounted");
				if (bdQtyCounted == null){
					bdQtyCounted = BigDecimal.ZERO;
				}
				
				//If there is a variance that needs to be processed:
				if (bdQtyOnHand.compareTo(bdQtyCounted) != 0
				){
					if (!addEntryLine(
						batchentry,
						rs.getString(
							SMTableicinventoryworksheet.TableName + "." 
							+ SMTableicinventoryworksheet.sitemnumber),
						sLocation,
						sLocationInventoryGLAcct,
						sLocationWriteOffGLAcct,
						bdQtyCounted,
						bdQtyOnHand
					)){
						rs.close();
						return false;
					}
				}
			}
			rs.close();
		} catch (SQLException e) {
			super.addErrorMessage("Error reading physical inventory lines - " + e.getMessage());
			return false;
		}

		//Save the entry
		if (!batchentry.save_without_data_transaction(conn, sUserID)){
			String sErrorMessages = "";
			for (int i = 0; i < batchentry.getErrorMessage().size(); i++){
				sErrorMessages += "<BR>" + batchentry.getErrorMessage().get(i);
			}
			super.addErrorMessage("Could not save batch entry - " + sErrorMessages);
			return false;
		}
		return true;
	}
	private boolean addEntryLine(
		ICEntry entry,
		String sItemNumber,
		String sLocation,
		String sControlAcct,
		String sDistributionAcct,
		BigDecimal qtyCounted, 
		BigDecimal qtyOnHand
		){
		
		ICEntryLine line = new ICEntryLine();
		line.sComment("Physical count");
		line.sControlAcct(sControlAcct);
		line.sDescription("On hand: " + clsManageBigDecimals.BigDecimalToFormattedString("########0.0000", qtyOnHand)
				+ ", Counted: " + clsManageBigDecimals.BigDecimalToFormattedString("########0.0000", qtyCounted));
		line.sDistributionAcct(sDistributionAcct);
		line.setQtyString(
			clsManageBigDecimals.BigDecimalToFormattedString(
				"#########0.0000", qtyCounted.subtract(qtyOnHand)));
		line.sItemNumber(sItemNumber);
		line.sLocation(sLocation);
		if (!entry.add_line(line)){
			String sError = "";
			for (int i = 0; i < entry.getErrorMessage().size(); i++){
				sError = "<BR>" + entry.getErrorMessage().get(i);
			}
			super.addErrorMessage("Could not add line to entry - " + sError);
			return false;
		}
		return true;
	}
	public boolean delete(Connection conn) {

		if (!clsDatabaseFunctions.start_data_transaction(conn)) {
			super.addErrorMessage("Could not start data transaction to delete "
					+ ParamObjectName + " with ID '" + m_sid + "'.");
			return false;
		}

		boolean bResult = true;

		String SQL = "DELETE FROM " + SMTableicphysicalinventories.TableName
				+ " WHERE (" + SMTableicphysicalinventories.lid + " = " + m_sid
				+ ")";

		try {
			if (!clsDatabaseFunctions.executeSQL(SQL, conn)) {
				super.addErrorMessage("Could not delete " + ParamObjectName
						+ " with ID '" + m_sid + "'.");
				bResult = false;
			}
		} catch (SQLException e) {
			super.addErrorMessage("Could not delete " + ParamObjectName
					+ " with ID '" + m_sid + "' - " + e.getMessage());
			bResult = false;
		}

		// delete the inventory count lines
		SQL = "DELETE FROM " + SMTableicphysicalcountlines.TableName
				+ " WHERE (" + SMTableicphysicalcountlines.lphysicalinventoryid
				+ " = " + m_sid + ")";

		try {
			if (!clsDatabaseFunctions.executeSQL(SQL, conn)) {
				super.addErrorMessage("Could not delete " + ParamObjectName
						+ "lines with ID '" + m_sid + "'.");
				bResult = false;
			}
		} catch (SQLException e) {
			super.addErrorMessage("Could not delete " + ParamObjectName
					+ "lines with ID '" + m_sid + "' - " + e.getMessage());
			bResult = false;
		}

		// delete the inventory counts
		SQL = "DELETE FROM " + SMTableicphysicalcounts.TableName + " WHERE ("
				+ SMTableicphysicalcounts.lphysicalinventoryid + " = " + m_sid
				+ ")";

		try {
			if (!clsDatabaseFunctions.executeSQL(SQL, conn)) {
				super.addErrorMessage("Could not delete " + ParamObjectName
						+ "counts with ID '" + m_sid + "'.");
				bResult = false;
			}
		} catch (SQLException e) {
			super.addErrorMessage("Could not delete " + ParamObjectName
					+ "counts with ID '" + m_sid + "' - " + e.getMessage());
			bResult = false;
		}

		// delete the inventory worksheet lines
		SQL = "DELETE FROM " + SMTableicinventoryworksheet.TableName
				+ " WHERE (" + SMTableicinventoryworksheet.lphysicalinventoryid
				+ " = " + m_sid + ")";

		try {
			if (!clsDatabaseFunctions.executeSQL(SQL, conn)) {
				super.addErrorMessage("Could not delete " + "inventory worksheet "
						+ "lines with physical inventory ID '" + m_sid + "'.");
				bResult = false;
			}
		} catch (SQLException e) {
			super.addErrorMessage("Could not delete " + "inventory worksheet "
					+ "lines with physical inventory ID '" + m_sid + "' - " + e.getMessage());
			bResult = false;
		}
		
		if (!bResult) {
			clsDatabaseFunctions.rollback_data_transaction(conn);
			return false;
		}

		m_sstatus = Integer.toString(SMTableicphysicalinventories.STATUS_DELETED);
		
		if (!clsDatabaseFunctions.commit_data_transaction(conn)) {
			clsDatabaseFunctions.rollback_data_transaction(conn);
			super
					.addErrorMessage("Could not commit data transaction after deleting "
							+ ParamObjectName + ".");
			return false;
		}

		// Empty the values:
		initEntryVariables();
		return true;
	}

	public String slid() {
		return m_sid;
	}

	public boolean slid(String slid) {
		try {
			m_sid = slid;
			return true;
		} catch (NumberFormatException e) {
			System.out.println("[1579203258] " + this.toString()
					+ "Error formatting entry ID from string: " + slid + ".");
			System.out.println(this.toString() + "Error: " + e.getMessage());
			return false;
		}
	}

	public boolean validate_entry_fields(Connection conn) {
		// Validate the entries here:
		boolean bEntriesAreValid = true;

		// ID:
		long lID;
		try {
			lID = Long.parseLong(m_sid);
		} catch (NumberFormatException e) {
			super.addErrorMessage("Invalid ID: '" + m_sid + "'.");
			bEntriesAreValid = false;
			return bEntriesAreValid;
		}

		if ((lID < -1) || (lID == 0)) {
			super.addErrorMessage("Invalid ID: '" + m_sid + "'.");
			bEntriesAreValid = false;
			return bEntriesAreValid;
		}

		// Description
		m_sdesc = m_sdesc.trim();
		if (m_sdesc.compareToIgnoreCase("") == 0) {
			super.addErrorMessage("Description cannot be empty.");
			bEntriesAreValid = false;
			return bEntriesAreValid;
		}
		if (m_sdesc.length() > SMTableicphysicalinventories.sdescLength) {
			super.addErrorMessage("Description is too long.");
			bEntriesAreValid = false;
			return bEntriesAreValid;
		}

		// Created by
		m_screatedbyfullname = m_screatedbyfullname.trim();
		if (m_screatedbyfullname.compareToIgnoreCase("") == 0) {
			super.addErrorMessage("Created by cannot be empty.");
			bEntriesAreValid = false;
			return bEntriesAreValid;
		}
		if (m_screatedbyfullname.length() > SMTableicphysicalinventories.screatedbyLength) {
			super.addErrorMessage("Created by is too long.");
			bEntriesAreValid = false;
			return bEntriesAreValid;
		}

		// Creation date - no need to check this, it can't be entered
		// Don't check unless it's an existing record:
		//if (lID != -1) {
		//	if (!SMUtilities.IsValidDateString("M/d/yyyy", m_datcreated)) {
		//		super.addErrorMessage("Creation date '" + m_datcreated
		//				+ "' is invalid.  ");
		//		bEntriesAreValid = false;
		//	}
		//}
		// Status
		m_sstatus = m_sstatus.trim();
		if (
			(m_sstatus.compareToIgnoreCase(
				Integer.toString(SMTableicphysicalinventories.STATUS_ENTERED)) != 0)
			&& (m_sstatus.compareToIgnoreCase(
				Integer.toString(SMTableicphysicalinventories.STATUS_DELETED)) != 0)
			&& (m_sstatus.compareToIgnoreCase(
				Integer.toString(SMTableicphysicalinventories.STATUS_BATCHED)) != 0)
		) {
			super.addErrorMessage("Status '" + m_sstatus + "' is invalid.  ");
			bEntriesAreValid = false;
		}

		// Batchnumber
		m_sbatchnumber = m_sbatchnumber.trim();

		// Starting and ending locations:
		m_slocation = m_slocation.trim();
		if (m_slocation.compareToIgnoreCase("") == 0){
			super.addErrorMessage("No location was chosen.");
			bEntriesAreValid = false;
		}

		return bEntriesAreValid;
	}
	public void addSingleItem(
		String sItemNumber,
		Connection conn) throws Exception{
		
		//IF the item is already in the worksheet, then just drop out:
		String SQL = "SELECT " + SMTableicinventoryworksheet.sitemnumber
			+ " FROM " + SMTableicinventoryworksheet.TableName
			+ " WHERE ("
				+ "(" + SMTableicinventoryworksheet.sitemnumber + " = '" + sItemNumber + "')"
			+ ")"
		;
		boolean bItemIsAlreadyInWorksheet = false;
		try {
			ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
			if (rs.next()){
				bItemIsAlreadyInWorksheet = true;
			}
			rs.close();
		} catch (Exception e1) {
			throw new Exception(
				"Error [1538502549] - Could not check for item '" + sItemNumber + "' with SQL: '" + SQL + "' - " + e1.getMessage());
		}
		if (bItemIsAlreadyInWorksheet){
			return;
		}
		
		String sInvAcct = "";
		String sWriteOffAcct = "";
		SQL = "SELECT"
			+ " " + SMTablelocations.sGLInventoryAcct
			+ ", " + SMTablelocations.sGLWriteOffAcct
			+ " FROM "
			+ SMTablelocations.TableName
			+ " WHERE ("
				+ "(" + SMTablelocations.sLocation + " = '" + getLocation() + "')"
			+ ")"
			;
		try {
			ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
			if (rs.next()){
				sInvAcct = rs.getString(SMTablelocations.sGLInventoryAcct);
				sWriteOffAcct = rs.getString(SMTablelocations.sGLWriteOffAcct);
				rs.close();
			}else{
				rs.close();
				throw new Exception("Error [1538502448] - Could not get GL account for location '" + getLocation() + " with SQL: '" + SQL + "'.");
			}
		} catch (SQLException e1) {
			throw new Exception(
				"Error [1538502449] - Could not get GL accounts for location '" + getLocation() + " - " + e1.getMessage());
		}
		
		SQL = "INSERT IGNORE INTO " + SMTableicinventoryworksheet.TableName + "("
			+ SMTableicinventoryworksheet.bdmostrecentcost
			+ ", " + SMTableicinventoryworksheet.bdqtyonhand
			+ ", " + SMTableicinventoryworksheet.lphysicalinventoryid
			+ ", " + SMTableicinventoryworksheet.sinvacct
			+ ", " + SMTableicinventoryworksheet.sitemnumber
			+ ", " + SMTableicinventoryworksheet.swriteoffacct
			+ ") SELECT "
			+ SMTableicitems.bdmostrecentcost
			+ ", 0.00" 
			+ ", " + m_sid
			+ ", '" + sInvAcct + "'"
			+ ", " + SMTableicitems.TableName + "." + SMTableicitems.sItemNumber
			+ ", '" + sWriteOffAcct + "'"
			+ " FROM " + SMTableicitems.TableName
			+ " WHERE ("
			+ "(" + SMTableicitems.TableName + "." + SMTableicitems.sItemNumber + " = '" + sItemNumber + "')"
			+ ")"
		;
		if (bDebugMode){
			System.out.println("[1538502653] - SQL to insert phys inv records - " + SQL + ".");
		}
		try {
			clsDatabaseFunctions.executeSQL(SQL, conn);
		} catch (Exception e) {
			throw new Exception(
					"Error [1538502701] - Could not insert items into phys inventory with SQL: " + SQL + " - " + e.getMessage());
		}
		
		//Now update the qtys on hand:
		SQL = "UPDATE " + SMTableicitems.TableName
			+ " LEFT JOIN " + SMTableicinventoryworksheet.TableName
			+ " ON " + SMTableicinventoryworksheet.TableName + "." + SMTableicinventoryworksheet.sitemnumber
			+ " = " + SMTableicitems.TableName + "." + SMTableicitems.sItemNumber
			+ " LEFT JOIN " + SMTableicitemlocations.TableName
			+ " ON " + SMTableicitems.TableName + "." + SMTableicitems.sItemNumber + " = "
			+ SMTableicitemlocations.TableName + "." + SMTableicitemlocations.sItemNumber
			+ " SET " + SMTableicinventoryworksheet.TableName + "." + SMTableicinventoryworksheet.bdqtyonhand
			+ " = " + SMTableicitemlocations.TableName + "." + SMTableicitemlocations.sQtyOnHand
			+ " WHERE ("
				+ "(" + SMTableicitems.TableName + "." + SMTableicitems.sItemNumber + " >= '" + sItemNumber + "')"
				+ " AND (" + SMTableicitemlocations.TableName + "." + SMTableicitemlocations.sLocation + " = '" + getLocation() + "')"
				+ " AND (" + SMTableicinventoryworksheet.TableName + "." + SMTableicinventoryworksheet.lphysicalinventoryid + " = " + m_sid + ")"
			+ ")"
		;
		if (bDebugMode){
			System.out.println("[1538502702] - SQL to update qtys on phys inv records - " + SQL + ".");
		}
		try {
			clsDatabaseFunctions.executeSQL(SQL, conn);
		} catch (Exception e) {
			throw new Exception(
					"Error [1538502703] - Could not update qtys on phys inventory with SQL: " + SQL + " - " + e.getMessage());
		}
		
	}
	public String printDebugData() {
		return "ICPhysicalInventoryEntry [m_datcreated=" + m_datcreated
				+ ", m_sbatchnumber=" + m_sbatchnumber + ", m_screatedby="
				+ m_screatedbyfullname + ", m_sdesc=" + m_sdesc
				+ ", m_sid="
				+ ", m_sstartinglocation=" + m_slocation
				+ ", m_sstatus=" + m_sstatus + "]";
	}

	public void addErrorMessage(String sMsg) {
		super.addErrorMessage(sMsg);
	}

	public String getQueryString() {

		// Particular to the specific class
		return ParamID + "=" + clsServletUtilities.URLEncode(m_sid) + "&"
				+ ParamCreatedByFullName
				+ clsServletUtilities.URLEncode(m_screatedbyfullname) + "&"
				+ ParamdatCreated
				+ clsServletUtilities.URLEncode(m_datcreated) + "&"
				+ ParamDesc + clsServletUtilities.URLEncode(m_sdesc) + "&"
				+ ParamiStatus + clsServletUtilities.URLEncode(m_sstatus)
				+ "&" + ParamlBatchnumber
				+ clsServletUtilities.URLEncode(m_sbatchnumber) + "&"
				+ ParamLocation
				+ clsServletUtilities.URLEncode(m_slocation)
		;
	}

	public String getsCreatedByFullName() {
		return m_screatedbyfullname;
	}

	public void setsCreatedByFullName(String sCreatedByFullName) {
		m_screatedbyfullname = sCreatedByFullName;
	}

	public String getdatCreated() {
		return m_datcreated;
	}

	public void setdatCreated(String datCreated) {
		m_datcreated = datCreated;
	}

	public String getDescription() {
		return m_sdesc;
	}

	public void setsDescription(String sDescription) {
		m_sdesc = sDescription;
	}

	public String getStatus() {
		return m_sstatus;
	}

	public void setsStatus(String sStatus) {
		m_sstatus = sStatus;
	}

	public String getsBatchNumber() {
		return m_sbatchnumber;
	}

	public void setsBatchNumber(String sBatchNumber) {
		m_sbatchnumber = sBatchNumber;
	}
	public String getLocation() {
		return m_slocation;
	}

	public void setsLocation(String sStartingLocation) {
		m_slocation = sStartingLocation;
	}
	
	private void initEntryVariables() {
		m_sid = "-1";
		m_sdesc = "";
		m_screatedbyfullname = "";
		m_datcreated = clsDateAndTimeConversions.now("M/d/yyyy hh:mm:ss a");
		m_sstatus = Integer.toString(SMTableicphysicalinventories.STATUS_ENTERED);
		m_sbatchnumber = "0";
		m_slocation = "";
		super.initVariables();
		super.setObjectName(ParamObjectName);
	}
}