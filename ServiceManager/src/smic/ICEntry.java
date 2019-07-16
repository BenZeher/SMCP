package smic;

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

import SMClasses.SMBatchStatuses;
import SMClasses.SMOption;
import SMDataDefinition.SMTableentrylines;
import SMDataDefinition.SMTableicbatchentries;
import SMDataDefinition.SMTableiccategories;
import SMDataDefinition.SMTableiccosts;
import SMDataDefinition.SMTableicentrylines;
import SMDataDefinition.SMTableicitems;
import SMDataDefinition.SMTableicoptions;
import SMDataDefinition.SMTablelocations;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsDateAndTimeConversions;
import ServletUtilities.clsManageBigDecimals;
import ServletUtilities.clsManageRequestParameters;
import ServletUtilities.clsServletUtilities;
import ServletUtilities.clsStringFunctions;

public class ICEntry extends java.lang.Object {

	public static final String ParamEntryID = "EntryID";
	public static final String ParamBatchNumber = "BatchNumber";
	public static final String ParamEntryNumber = "EntryNumber";
	public static final String ParamEntryType = "EntryType";
	public static final String ParamDocNumber = "DocNumber";
	public static final String ParamEntryDescription = "EntryDescription";
	public static final String ParamEntryDate = "EntryDate";
	public static final String ParamEntryAmount = "EntryAmount";
	public static final String ParamLastLine = "LastLine";
	public static final String ParamBatchType = "BatchType";
	public static final String ParamNumberOfLines = "NumberOfLines";
	
	public static final String ParamObjectName = "IC Batch Entry";

	private long m_lid;
	private long m_lBatchNumber;
	private long m_lEntryNumber;
	private String m_sEntryType;
	private String m_sDocNumber;
	private String m_sEntryDescription;
	private String m_sEntryDate; // Always stored as MM/dd/yyyy
	private String m_sBatchType;
	private long m_lNumberOfLines;
	private ArrayList<ICEntryLine> LineArray;
	private ArrayList<String> m_sErrorMessage;
	// Used until an umbrella data transaction is completed:
	private long m_lTempID;
	private boolean bDebugMode = false;

	ICEntry(String sBatchNumber, String sEntryNumber) {
		m_lid = -1;
		sBatchNumber(sBatchNumber);
		sEntryNumber(sEntryNumber);
		m_sEntryType = "-1";
		m_sDocNumber = "";
		m_sEntryDescription = "INITIALIZED ENTRY";
		m_sEntryDate = clsDateAndTimeConversions.now("MM/dd/yyyy");
		m_lNumberOfLines = 0;
		m_sBatchType = "-1";
		m_sErrorMessage = new ArrayList<String>(0);

		LineArray = new ArrayList<ICEntryLine>(0);
		// System.out.println(this.toString() + "***Customer number in class = "
		// + m_sCustomerNumber);
	}

	ICEntry(String sEntryID) {
		slid(sEntryID);
		m_lBatchNumber = -1;
		m_lEntryNumber = -1;
		m_sEntryType = "-1";
		m_sDocNumber = "";
		m_sEntryDescription = "INITIALIZED ENTRY";
		m_sEntryDate = clsDateAndTimeConversions.now("MM/dd/yyyy");
		m_lNumberOfLines = 0;
		m_sBatchType = "-1";
		m_sErrorMessage = new ArrayList<String>(0);

		LineArray = new ArrayList<ICEntryLine>(0);
	}

	ICEntry() {
		m_lid = -1;
		m_lBatchNumber = -1;
		m_lEntryNumber = -1;
		m_sEntryType = "-1";
		m_sDocNumber = "";
		m_sEntryDescription = "INITIALIZED ENTRY";
		m_sEntryDate = clsDateAndTimeConversions.now("MM/dd/yyyy");
		m_lNumberOfLines = 0;
		m_sBatchType = "-1";
		m_sErrorMessage = new ArrayList<String>(0);

		LineArray = new ArrayList<ICEntryLine>(0);
	}

	// *****************
	ICEntry(HttpServletRequest req) {
		m_lid = -1;
		m_sBatchType = "-1";
		m_sErrorMessage = new ArrayList<String>(0);

		LineArray = new ArrayList<ICEntryLine>(0);

		m_sBatchType = clsManageRequestParameters.get_Request_Parameter(
				ICEntry.ParamBatchType, req).trim();
		m_lBatchNumber = Long.parseLong(clsManageRequestParameters.get_Request_Parameter(
				ICEntry.ParamBatchNumber, req));
		m_lEntryNumber = Long.parseLong(clsManageRequestParameters.get_Request_Parameter(
				ICEntry.ParamEntryNumber, req));
		m_sEntryType = clsManageRequestParameters.get_Request_Parameter(
				ICEntry.ParamEntryType, req);
		m_sDocNumber = clsManageRequestParameters.get_Request_Parameter(
				ICEntry.ParamDocNumber, req);
		m_sEntryDescription = clsManageRequestParameters.get_Request_Parameter(
				ICEntry.ParamEntryDescription, req);
		m_sEntryDate = clsManageRequestParameters.get_Request_Parameter(
				ICEntry.ParamEntryDate, req);
		m_lNumberOfLines = Long.parseLong(clsManageRequestParameters.get_Request_Parameter(
				ICEntry.ParamNumberOfLines, req));

		// Load the lines from the servlet request:
		int iNumberOfLines = Integer.parseInt(req
				.getParameter(ICEntry.ParamNumberOfLines));
		LineArray = new ArrayList<ICEntryLine>(0);
		for (int i = 0; i < iNumberOfLines; i++) {
			ICEntryLine line = new ICEntryLine();
			LineArray.add((ICEntryLine) line);
		}

		// System.out.println("In AREntryInput.AREntryInput(req), m_iNumberOfLines = "
		// + m_iNumberOfLines);
		// System.out.println("In AREntryInput.AREntryInput(req), m_LineInputArray.size() = "
		// + m_LineInputArray.size());

		Enumeration<String> paramNames = req.getParameterNames();
		int iLineNumber = 0;
		while (paramNames.hasMoreElements()) {
			String sParamName = (String) paramNames.nextElement();
			// System.out.println("In AREntryInput.AREntryInput(req), sParamName = "
			// + sParamName);
			if (sParamName.contains(ICEntryLine.ParamLineComment)) {
				// Strip off the line number
				iLineNumber = Integer.parseInt(sParamName.substring(
						ICEntryLine.ParamLineComment.length(), sParamName
						.length()));
				LineArray.get(iLineNumber).sComment(
						(String) clsManageRequestParameters.get_Request_Parameter(sParamName,
								req).trim());
			}
			if (sParamName.contains(ICEntryLine.ParamLineCategory)) {
				// Strip off the line number
				iLineNumber = Integer.parseInt(sParamName.substring(
						ICEntryLine.ParamLineCategory.length(), sParamName
						.length()));
				LineArray.get(iLineNumber).sCategoryCode(
						(String) clsManageRequestParameters.get_Request_Parameter(sParamName,
								req).trim());
			}
			if (sParamName.contains(ICEntryLine.ParamLineControlAccount)) {
				// Strip off the line number
				iLineNumber = Integer.parseInt(sParamName.substring(
						ICEntryLine.ParamLineControlAccount.length(),
						sParamName.length()));
				LineArray.get(iLineNumber).sControlAcct(
						(String) clsManageRequestParameters.get_Request_Parameter(sParamName,
								req).trim());
			}
			if (sParamName.contains(ICEntryLine.ParamLineCost)) {
				// Strip off the line number
				iLineNumber = Integer.parseInt(sParamName
						.substring(ICEntryLine.ParamLineCost.length(),
								sParamName.length()));
				LineArray.get(iLineNumber).setCostString(
						(String) clsManageRequestParameters.get_Request_Parameter(sParamName,
								req).trim());
			}
			if (sParamName.contains(ICEntryLine.ParamLineDesc)) {
				// Strip off the line number
				iLineNumber = Integer.parseInt(sParamName
						.substring(ICEntryLine.ParamLineDesc.length(),
								sParamName.length()));
				LineArray.get(iLineNumber).sDescription(
						(String) clsManageRequestParameters.get_Request_Parameter(sParamName,
								req).trim());
			}
			if (sParamName.contains(ICEntryLine.ParamLineDistributionAccount)) {
				// Strip off the line number
				iLineNumber = Integer.parseInt(sParamName.substring(
						ICEntryLine.ParamLineDistributionAccount.length(),
						sParamName.length()));
				LineArray.get(iLineNumber).sDistributionAcct(
						(String) clsManageRequestParameters.get_Request_Parameter(sParamName,
								req).trim());
			}
			if (sParamName.contains(ICEntryLine.ParamLineEntryID)) {
				// Strip off the line number
				iLineNumber = Integer.parseInt(sParamName.substring(
						ICEntryLine.ParamLineEntryID.length(), sParamName
						.length()));
				LineArray.get(iLineNumber).sEntryId(
						(String) clsManageRequestParameters.get_Request_Parameter(sParamName,
								req).trim());
			}
			if (sParamName.contains(ICEntryLine.ParamLineID)) {
				// Strip off the line number
				iLineNumber = Integer.parseInt(sParamName.substring(
						ICEntryLine.ParamLineID.length(), sParamName.length()));
				LineArray.get(iLineNumber).sId(
						(String) clsManageRequestParameters.get_Request_Parameter(sParamName,
								req).trim());
			}
			if (sParamName.contains(ICEntryLine.ParamLineItemNumber)) {
				// Strip off the line number
				iLineNumber = Integer.parseInt(sParamName.substring(
						ICEntryLine.ParamLineItemNumber.length(), sParamName
						.length()));
				LineArray.get(iLineNumber).sItemNumber(
						(String) clsManageRequestParameters.get_Request_Parameter(sParamName,
								req).trim());
			}
			if (sParamName.contains(ICEntryLine.ParamLineLocation)) {
				// Strip off the line number
				iLineNumber = Integer.parseInt(sParamName.substring(
						ICEntryLine.ParamLineLocation.length(), sParamName
						.length()));
				LineArray.get(iLineNumber).sLocation(
						(String) clsManageRequestParameters.get_Request_Parameter(sParamName,
								req).trim());
			}
			if (sParamName.contains(ICEntryLine.ParamLineTargetLocation)) {
				// Strip off the line number
				iLineNumber = Integer.parseInt(sParamName.substring(
						ICEntryLine.ParamLineTargetLocation.length(),
						sParamName.length()));
				LineArray.get(iLineNumber).sTargetLocation(
						(String) clsManageRequestParameters.get_Request_Parameter(sParamName,
								req).trim());
			}
			if (sParamName.contains(ICEntryLine.ParamLinePrice)) {
				// Strip off the line number
				iLineNumber = Integer.parseInt(sParamName.substring(
						ICEntryLine.ParamLinePrice.length(), sParamName
						.length()));
				LineArray.get(iLineNumber).setPriceString(
						(String) clsManageRequestParameters.get_Request_Parameter(sParamName,
								req).trim());
			}
			if (sParamName.contains(ICEntryLine.ParamLineQty)) {
				// Strip off the line number
				iLineNumber = Integer
				.parseInt(sParamName.substring(ICEntryLine.ParamLineQty
						.length(), sParamName.length()));
				LineArray.get(iLineNumber).setQtyString(
						(String) clsManageRequestParameters.get_Request_Parameter(sParamName,
								req).trim());
			}
			if (sParamName.contains(ICEntryLine.ParamLineReceiptNum)) {
				// Strip off the line number
				iLineNumber = Integer.parseInt(sParamName.substring(
						ICEntryLine.ParamLineReceiptNum.length(), sParamName
						.length()));
				LineArray.get(iLineNumber).sReceiptNum(
						(String) clsManageRequestParameters.get_Request_Parameter(sParamName,
								req).trim());
			}
			if (sParamName.contains(ICEntryLine.ParamLineCostBucketID)) {
				// Strip off the line number
				iLineNumber = Integer.parseInt(sParamName.substring(
						ICEntryLine.ParamLineCostBucketID.length(), sParamName
						.length()));
				LineArray.get(iLineNumber).sCostBucketID(
						(String) clsManageRequestParameters.get_Request_Parameter(sParamName,
								req).trim());
			}
			if (sParamName.contains(ICEntryLine.ParamInvoiceLineNumber)) {
				// Strip off the line number
				iLineNumber = Integer.parseInt(sParamName.substring(
						ICEntryLine.ParamInvoiceLineNumber.length(), sParamName
						.length()));
				LineArray.get(iLineNumber).sInvoiceLineNumber(
						(String) clsManageRequestParameters.get_Request_Parameter(sParamName,
								req).trim());
			}
			if (sParamName.contains(ICEntryLine.ParamInvoiceNumber)) {
				// Strip off the line number
				iLineNumber = Integer.parseInt(sParamName.substring(
						ICEntryLine.ParamInvoiceNumber.length(), sParamName
						.length()));
				LineArray.get(iLineNumber).sInvoiceNumber(
						(String) clsManageRequestParameters.get_Request_Parameter(sParamName,
								req).trim());
			}
		}

		// Remove any lines with a "0.00" amount:
		remove_zero_amount_lines();
		m_lNumberOfLines = LineArray.size();
	}

	// *****************
	public boolean save_without_data_transaction(Connection conn, String sUserID) {

		if (!saveEntry(conn, sUserID)){
			return false;
		}else{
			return true;
		}
	}

	private boolean saveEntry(Connection conn, String sUserID){
		// Make sure there's no open transaction already using this customer
		// number and doc number:
		if (!checkIfKeyIsUnique(conn)) {
			return false;
		}

		//Make sure the batch is still open, just in case someone posted it since we loaded it:
		if (!batchIsOpen(conn)){
			return false;
		}

		// If it's a NEW entry, make sure there's no entry already using this
		// customer number and doc number:
		if (m_lEntryNumber == -1) {
			if (!checkIfEntryKeyIsUnique(conn)) {
				return false;
			}
		}
		// Now we'll validate, and save the entry:
		if (bDebugMode){
			clsServletUtilities.sysprint(this.toString(), "system", "entryline cost = " + this.LineArray.get(0).sCostSTDFormat() + " before validate lines");
		}
		if (!validate_lines(conn)) {
			return false;
		}
		if (bDebugMode){
			clsServletUtilities.sysprint(this.toString(), "system", "entryline cost = " + this.LineArray.get(0).sCostSTDFormat() + " after validate lines");
		}
		if (!validate_entry_fields(conn, sUserID)) {
			return false;
		}
		/*
		 * If it's a NEW entry, we have to save the entry first, to get the
		 * entry id. Next, we validate the lines, then the Entries, then save
		 * the lines, then update the entry, then update the batch.
		 */

		// We have to save the ID carefully, because we need the original ID AND
		// the one we get from
		// saving a new transaction. This is because if the transaction doesn't
		// complete, we
		// need to be able to revert back to the old ID:
		String sEntryID = "";
		boolean bNewEntry = false;
		//System.out.println("[1475189511] m_lEntryNumber = " + m_lEntryNumber);
		if (m_lEntryNumber == -1) {
			bNewEntry = true;
			if (!add_new_entry(conn, sUserID)) {
				System.out
				.println(this.toString() + "Could not add new entry.");
				return false;
			}
			sEntryID = Long.toString(m_lTempID);

		} else {
			sEntryID = Long.toString(m_lid);
			// System.out.println("In " + this.toString() +
			// ".save_without_data_transaction - 006");
		}
		// At this point, we know we already have a saved entry, whether it's a

		// new entry or not:
		if (!save_lines(conn, sEntryID)) {
			return false;
		}
		// Now save the entry:
		java.sql.Date datEntry = null;
		try {
			datEntry = clsDateAndTimeConversions.StringTojavaSQLDate("MM/dd/yyyy",m_sEntryDate);
		} catch (ParseException e) {
			m_sErrorMessage.add("Error:[1423767980] Invalid entry date: '" 
				+ datEntry + "' - " + e.getMessage());
			return false;
		}
		String SQL = "";
		SQL = "UPDATE " + SMTableicbatchentries.TableName + " SET "
		+ SMTableicbatchentries.bdentryamount + " = " + dEntryTotal()
		+ ", " + SMTableicbatchentries.datentrydate + " = '"
		+ clsDateAndTimeConversions.utilDateToString(datEntry, "yyyy-MM-dd") + "'"
		+ ", " + SMTableicbatchentries.ientrytype + " = " + sEntryType() 
		+ ", " + SMTableicbatchentries.llastline + " = " + sLastLine() 
		+ ", " + SMTableicbatchentries.sdocnumber
		+ " = '" + clsDatabaseFunctions.FormatSQLStatement(sDocNumber()) + "'"
		+ ", " + SMTableicbatchentries.sentrydescription + " = '"
		+ clsDatabaseFunctions.FormatSQLStatement(sEntryDescription()) + "'"
		+ " WHERE (" 
		+ "(" + SMTableicbatchentries.lbatchnumber + " = " + sBatchNumber() + ")" 
		+ " AND (" + SMTableicbatchentries.lentrynumber + " = " + sEntryNumber() + ")" 
		+ ")";
		//System.out.println("[1475181787] SQL = " + SQL);
		try {
			if (clsDatabaseFunctions.executeSQL(SQL, conn) == false) {
				System.out.println(this.toString()
						+ "Could not complete update transaction - entry"
						+ " was not updated.<BR>");
				m_sErrorMessage.add("Could not update entry with SQL: " + SQL);
				return false;
			} else {
			}
		} catch (SQLException ex) {
			m_sErrorMessage.add("Error [1423860829] updating entry: " + ex.getMessage());
			return false;
		}
		// Update the batch with the latest entry number:
		ICEntryBatch batch = new ICEntryBatch(sBatchNumber());
		if (!batch.load(conn)) {
			m_sErrorMessage.add("Could not load batch after saving entry");
			return false;
		}
		if (!batch.update_last_entry_number(conn)) {
			m_sErrorMessage.add("Could not update last entry number in batch");
			return false;
		}
		// If everything went OK, and if it was a new entry, we can set the real
		// entry ID to the one we got when we inserted
		if (bNewEntry) {
			m_lid = m_lTempID;
		}
		return true;

	}
	public boolean load(ServletContext context, String sDBID) {

		return load(sBatchNumber(), sEntryNumber(), context, sDBID);
	}

	public boolean load(Connection conn) {

		return load(sBatchNumber(), sEntryNumber(), conn);
	}

	public boolean load(String sBatchNumber, String sEntryNumber,
			ServletContext context, String sDBID) {

		if (!sBatchNumber(sBatchNumber)) {
			System.out.println(this.toString() + "Invalid sBatchNumber - "
					+ sBatchNumber);
			return false;
		}

		if (!sEntryNumber(sEntryNumber)) {
			System.out.println(this.toString() + "Invalid sEntryNumber - "
					+ sEntryNumber);
			return false;
		}

		Connection conn = clsDatabaseFunctions.getConnection(context, sDBID,
				"MySQL", this.toString() + ".load");
		if (conn == null) {
			m_sErrorMessage.add("Could not open connection in load");
			return false;
		}
		String SQL = "SELECT " + SMTableicbatchentries.TableName + ".*" + ", "
		+ ICEntryBatch.TableName + "." + ICEntryBatch.ibatchtype
		+ " FROM " + SMTableicbatchentries.TableName + ", "
		+ ICEntryBatch.TableName + " WHERE (" + "("
		+ SMTableicbatchentries.TableName + "."
		+ SMTableicbatchentries.lbatchnumber + " = " + sBatchNumber
		+ ")" + " AND (" + SMTableicbatchentries.TableName + "."
		+ SMTableicbatchentries.lentrynumber + " = " + sEntryNumber
		+ ")" + " AND (" + SMTableicbatchentries.TableName + "."
		+ SMTableicbatchentries.lbatchnumber + " = "
		+ ICEntryBatch.TableName + "." + ICEntryBatch.lbatchnumber
		+ ")" + ")";
		// System.out.println("In " + this.toString() + " - SQL = " + SQL);
		try {
			ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
			if (rs.next()) {
				if (!loadFromResultSet(rs)) {
					rs.close();
					return false;
				}
			} else {
				m_sErrorMessage.add("In " + this.toString()
						+ " - Could not load record with SQL = " + SQL);
				return false;
			}
			rs.close();

		} catch (SQLException ex) {
			System.out.println("Error in " + this.toString() + ".load class!!");
			System.out.println("SQLException: " + ex.getMessage());
			System.out.println("SQLState: " + ex.getSQLState());
			System.out.println("SQL: " + ex.getErrorCode());
			clsDatabaseFunctions.freeConnection(context, conn, "[1547081120]");
			return false;
		}

		if (!load_lines(conn)) {
			clsDatabaseFunctions.freeConnection(context, conn, "[1547081121]");
			return false;
		}

		clsDatabaseFunctions.freeConnection(context, conn, "[1547081122]");
		return true;
	}

	public boolean load(String sBatchNumber, String sEntryNumber,
			Connection conn) {

		if (!sBatchNumber(sBatchNumber)) {
			System.out.println(this.toString() + "Invalid sBatchNumber - "
					+ sBatchNumber);
			return false;
		}

		if (!sEntryNumber(sEntryNumber)) {
			System.out.println(this.toString() + "Invalid sEntryNumber - "
					+ sEntryNumber);
			return false;
		}
		String SQL = "SELECT " + SMTableicbatchentries.TableName + ".*" + ", "
		+ ICEntryBatch.TableName + "." + ICEntryBatch.ibatchtype
		+ " FROM " + SMTableicbatchentries.TableName + ", "
		+ ICEntryBatch.TableName + " WHERE (" + "("
		+ SMTableicbatchentries.TableName + "."
		+ SMTableicbatchentries.lbatchnumber + " = " + sBatchNumber
		+ ")" + " AND (" + SMTableicbatchentries.TableName + "."
		+ SMTableicbatchentries.lentrynumber + " = " + sEntryNumber
		+ ")" + " AND (" + SMTableicbatchentries.TableName + "."
		+ SMTableicbatchentries.lbatchnumber + " = "
		+ ICEntryBatch.TableName + "." + ICEntryBatch.lbatchnumber
		+ ")" + ")";
		try {
			ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
			rs.next();
			if (!loadFromResultSet(rs)) {
				rs.close();
				return false;
			}
			rs.close();
		} catch (SQLException ex) {
			System.out.println("Error in " + this.toString() + ".load class!!");
			System.out.println("SQLException: " + ex.getMessage());
			System.out.println("SQLState: " + ex.getSQLState());
			System.out.println("SQL: " + ex.getErrorCode());
			return false;
		}

		if (!load_lines(conn)) {
			return false;
		}

		return true;
	}

	public boolean load(String sEntryID, ServletContext context,
			String sDBID) {

		Connection conn = clsDatabaseFunctions.getConnection(context, sDBID,
				"MySQL", this.toString() + ".load (2)");
		if (conn == null) {
			m_sErrorMessage.add("Could not open connection in load");
			return false;
		}
		String SQL = "SELECT * " + " FROM " + SMTableicbatchentries.TableName
		+ ", " + ICEntryBatch.TableName + " WHERE (" + "("
		+ SMTableicbatchentries.lid + " = " + sEntryID + ")" + " AND ("
		+ SMTableicbatchentries.TableName + "."
		+ SMTableicbatchentries.lbatchnumber + " = "
		+ ICEntryBatch.TableName + "." + ICEntryBatch.lbatchnumber
		+ ")" + ")";
		try {
			ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, context, sDBID,
					"MySQL", this.toString() + ".load (3)");
			rs.next();
			if (!loadFromResultSet(rs)) {
				rs.close();
				return false;
			}
			rs.close();
		} catch (SQLException ex) {
			System.out.println("Error in " + this.toString() + ".load class!!");
			System.out.println("SQLException: " + ex.getMessage());
			System.out.println("SQLState: " + ex.getSQLState());
			System.out.println("SQL: " + ex.getErrorCode());
			clsDatabaseFunctions.freeConnection(context, conn, "[1547080847]");
			return false;
		}

		if (!load_lines(conn)) {
			clsDatabaseFunctions.freeConnection(context, conn, "[1547080848]");
			return false;
		}

		clsDatabaseFunctions.freeConnection(context, conn, "[1547080849]");
		return true;
	}

	public boolean load(String sEntryID, Connection conn) {

		String SQL = "SELECT * " + " FROM " + SMTableicbatchentries.TableName
		+ ", " + ICEntryBatch.TableName + " WHERE (" + "("
		+ SMTableicbatchentries.lid + " = " + sEntryID + ")" + " AND ("
		+ SMTableicbatchentries.TableName + "."
		+ SMTableicbatchentries.lbatchnumber + " = "
		+ ICEntryBatch.TableName + "." + ICEntryBatch.lbatchnumber
		+ ")" + ")";
		try {
			ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
			rs.next();
			if (!loadFromResultSet(rs)) {
				rs.close();
				return false;
			}
			rs.close();
		} catch (SQLException ex) {
			System.out.println("Error in " + this.toString() + ".load class!!");
			System.out.println("SQLException: " + ex.getMessage());
			System.out.println("SQLState: " + ex.getSQLState());
			System.out.println("SQL: " + ex.getErrorCode());
			return false;
		}

		if (!load_lines(conn)) {
			return false;
		}

		return true;
	}

	private boolean loadFromResultSet(ResultSet rs) {
		// Load the variables:
		try {
			java.sql.Date datEntryDate = rs
			.getDate(SMTableicbatchentries.datentrydate);
			sEntryDate(clsDateAndTimeConversions.utilDateToString(datEntryDate, "MM/dd/yyyy"));
			lBatchNumber(rs.getLong(SMTableicbatchentries.lbatchnumber));
			m_sEntryType = Integer.toString(rs
					.getInt(SMTableicbatchentries.ientrytype));
			lEntryNumber(rs.getLong(SMTableicbatchentries.lentrynumber));
			lNumberOfLines(rs.getLong(SMTableicbatchentries.llastline));
			lid(rs.getLong(SMTableicbatchentries.lid));
			sEntryDescription(rs
					.getString(SMTableicbatchentries.sentrydescription));
			sDocNumber(rs.getString(SMTableicbatchentries.sdocnumber));
			// This comes from the batch table:
			m_sBatchType = Integer.toString(rs.getInt(ICEntryBatch.ibatchtype));
		} catch (SQLException e) {
			m_sErrorMessage
			.add("Error loading entry record: " + e.getMessage());
			return false;
		}
		return true;
	}

	private boolean load_lines(Connection conn) {
		return load_lines(sBatchNumber(), sEntryNumber(), conn);
	}

	private boolean load_lines(String sBatchNumber, String sEntryNumber,
			Connection conn) {

		if (!sBatchNumber(sBatchNumber)) {
			System.out.println(this.toString() + "Invalid sBatchNumber - "
					+ sBatchNumber);
			return false;
		}

		if (!sEntryNumber(sEntryNumber)) {
			System.out.println(this.toString() + "Invalid sEntryNumber - "
					+ sEntryNumber);
			return false;
		}

		// ArrayList LineArray = new ArrayList(0);
		String SQL = "SELECT * " + " FROM " + SMTableicentrylines.TableName
		+ " WHERE (" + "(" + SMTableicentrylines.lbatchnumber + " = "
		+ sBatchNumber + ")" + " AND ("
		+ SMTableicentrylines.lentrynumber + " = " + sEntryNumber + ")"
		+ ")"
		+ " ORDER BY " + SMTableicentrylines.llinenumber
		;
		try {
			ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
			LineArray.clear();
			while (rs.next()) {
				// Add a new line class, and load it up:
				ICEntryLine line = new ICEntryLine(sBatchNumber(),
						sEntryNumber(), Integer.toString(rs
								.getInt(SMTableicentrylines.llinenumber)));
				// System.out.println("In " + this.toString() +
				// ".loadlines, line batch number is " + line.sBatchNumber());
				line.setCostString(clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(rs
						.getBigDecimal(SMTableicentrylines.bdcost)));
				line.setPriceString(clsManageBigDecimals
						.BigDecimalTo2DecimalSTDFormat(rs
								.getBigDecimal(SMTableicentrylines.bdprice)));
				line.setQtyString(clsManageBigDecimals.BigDecimalToFormattedString(
						"#########0.0000", rs
						.getBigDecimal(SMTableicentrylines.bdqty)));
				line.sId(Long.toString(rs.getLong(SMTableicentrylines.lid)));
				line.lEntryId(rs.getLong(SMTableicentrylines.lentryid));
				line.sComment(rs.getString(SMTableicentrylines.scomment));
				line.sDescription(rs
						.getString(SMTableicentrylines.sdescription));
				line.sItemNumber(rs.getString(SMTableicentrylines.sitemnumber));
				line.sLocation(rs.getString(SMTableicentrylines.slocation));
				line.sTargetLocation(rs
						.getString(SMTableicentrylines.stargetlocation));
				line.sCategoryCode(rs
						.getString(SMTableicentrylines.scategorycode));
				line.sReceiptNum(rs.getString(SMTableicentrylines.sreceiptnum));
				line.sCostBucketID(Long.toString(rs
						.getLong(SMTableicentrylines.lcostbucketid)));
				line.sControlAcct(rs
						.getString(SMTableicentrylines.scontrolacct));
				line.sDistributionAcct(rs
						.getString(SMTableicentrylines.sdistributionacct));
				line.sInvoiceLineNumber(Long.toString(rs
						.getLong(SMTableicentrylines.linvoicelinenumber)));
				line.sInvoiceNumber(rs
						.getString(SMTableicentrylines.sinvoicenumber));
				line.sReceiptLineID(Long.toString(rs
						.getLong(SMTableicentrylines.lreceiptlineid)));
				LineArray.add((ICEntryLine) line);
			}
			rs.close();
		} catch (SQLException ex) {
			m_sErrorMessage.add("Error [1533583093] - could no load lines with SQL '" + SQL + "' - " + ex.getMessage());
			System.out.println("Error in " + this.toString() + ".load class!!");
			System.out.println("SQLException: " + ex.getMessage());
			System.out.println("SQLState: " + ex.getSQLState());
			System.out.println("SQL: " + ex.getErrorCode());
			return false;
		}
		return true;
	}

	public boolean add_line(ICEntryLine line) {

		line.sBatchNumber(Long.toString(lBatchNumber()));
		line.sEntryNumber(Long.toString(lEntryNumber()));
		line.lEntryId(lid());
		LineArray.add((ICEntryLine) line);
		m_lNumberOfLines = LineArray.size();
		return true;
	}

	public void clearLines() {
		LineArray.clear();
	}

	private boolean delete_single_line_from_database(String sLineID,
			Connection conn) {

		String SQL = "DELETE " + " FROM " + SMTableicentrylines.TableName
		+ " WHERE (" + "(" + SMTableicentrylines.lid + " = " + sLineID
		+ ")" + ")";
		try {
			if (clsDatabaseFunctions.executeSQL(SQL, conn) == false) {
				m_sErrorMessage
				.add("Could not delete single line from database");
				return false;
			}
		} catch (SQLException ex) {
			System.out.println("Error in " + this.toString()
					+ " - delete_single_line!!");
			System.out.println("SQLException: " + ex.getMessage());
			System.out.println("SQLState: " + ex.getSQLState());
			System.out.println("SQL: " + ex.getErrorCode());
			m_sErrorMessage
			.add("Could not delete single line from database: - "
					+ ex.getMessage());
			return false;
		}
		return true;
	}

	public ICEntryLine getLineByLineID(Long lLineID) {
		for (int i = 0; i < LineArray.size(); i++) {
			ICEntryLine Line = (ICEntryLine) LineArray.get(i);
			if (Line.sId().compareToIgnoreCase(Long.toString(lLineID)) == 0) {
				return Line;
			}
		}
		return null;
	}

	public boolean line_exists(Long lLineID) {
		for (int i = 0; i < LineArray.size(); i++) {
			ICEntryLine Line = (ICEntryLine) LineArray.get(i);
			if (Line.sId().compareToIgnoreCase(Long.toString(lLineID)) == 0) {
				return true;
			}
		}
		return false;
	}

	public ICEntryLine getLineByIndex(int iLineIndex) {

		if (iLineIndex > LineArray.size()) {
			return null;
		}
		if (iLineIndex < 0) {
			return null;
		}

		ICEntryLine Line = (ICEntryLine) LineArray.get(iLineIndex);
		return Line;
	}

	public void lid(long lid) {
		m_lid = lid;
	}

	public long lid() {
		return m_lid;
	}

	public String slid() {
		return Long.toString(m_lid);
	}

	public boolean slid(String slid) {
		try {
			m_lid = Long.parseLong(slid);
			return true;
		} catch (NumberFormatException e) {
			System.out.println(this.toString()
					+ "Error formatting entry ID from string: " + slid + ".");
			System.out.println(this.toString() + "Error: " + e.getMessage());
			return false;
		}
	}

	public void lBatchNumber(long lBatchNumber) {
		m_lBatchNumber = lBatchNumber;
	}

	public long lBatchNumber() {
		return m_lBatchNumber;
	}

	public boolean sBatchNumber(String sBatchNumber) {
		try {
			m_lBatchNumber = Long.parseLong(sBatchNumber);
			return true;
		} catch (NumberFormatException e) {
			System.out.println(this.toString()
					+ "Error formatting batch number from string: "
					+ sBatchNumber + ".");
			System.out.println(this.toString() + "Error: " + e.getMessage());
			return false;
		}
	}

	public String sBatchNumber() {
		return Long.toString(m_lBatchNumber);
	}

	private void lEntryNumber(long lEntryNumber) {
		m_lEntryNumber = lEntryNumber;
	}

	public long lEntryNumber() {
		return m_lEntryNumber;
	}

	public boolean sEntryNumber(String sEntryNumber) {
		try {
			m_lEntryNumber = Long.parseLong(sEntryNumber);
			return true;
		} catch (NumberFormatException e) {
			System.out.println(this.toString()
					+ "Error formatting Entry number from string: "
					+ sEntryNumber + ".");
			System.out.println(this.toString() + "Error: " + e.getMessage());
			return false;
		}
	}

	public String sEntryNumber() {
		return Long.toString(m_lEntryNumber);
	}

	public void sEntryType(String sEntryType) {
		m_sEntryType = sEntryType;
	}

	public String sEntryType() {
		return m_sEntryType;
	}

	public boolean sDocNumber(String sDocNumber) {

		if (sDocNumber.length() == 0) {
			return false;
		} else {
			m_sDocNumber = sDocNumber;
			return true;
		}
	}

	public String sDocNumber() {
		return m_sDocNumber;
	}

	public boolean sEntryDescription(String sEntryDescription) {
		m_sEntryDescription = sEntryDescription;
		return true;
	}

	public String sEntryDescription() {
		return m_sEntryDescription;
	}

	public void sEntryDate(String sEntryDate) {
		m_sEntryDate = sEntryDate;
	}

	/*
	 * public boolean datEntryDate (String sFormat, String sEntryDate){ if (!
	 * SMUtilities.isValidDateStr(sEntryDate, sFormat)){ return false; }else{
	 * java.sql.Date datTestDate; datTestDate =
	 * SMUtilities.StringToSQLDateStrict(sFormat, sEntryDate);
	 * 
	 * //Test that the date is within 90 years in either direction: Calendar c1
	 * = Calendar.getInstance(); c1.add(Calendar.YEAR, 90); java.sql.Date
	 * datBoundDate = new java.sql.Date(c1.getTimeInMillis());
	 * //System.out.println("In ARentry.datEntryDate - first datBoundDate = " +
	 * datBoundDate); if (datTestDate.after(datBoundDate)){ return false; }
	 * c1.add(Calendar.YEAR, -180); datBoundDate = new
	 * java.sql.Date(c1.getTimeInMillis());
	 * //System.out.println("In ARentry.datEntryDate - first datBoundDate = " +
	 * datBoundDate); if (datTestDate.before(datBoundDate)){ return false; }
	 * m_sEntryDate = datTestDate;
	 * //System.out.println("In ARentry.datEntryDate - m_datDocDate = " +
	 * m_datDocDate); return true; } }
	 */
	public String sStdEntryDate() {
		return m_sEntryDate;
	}

	public long lNumberOfLines() {
		return m_lNumberOfLines;
	}

	public void lNumberOfLines(long lNumberOfLines) {
		m_lNumberOfLines = lNumberOfLines;
	}

	public String sLastLine() {
		return Long.toString(m_lNumberOfLines);
	}

	public String sEntryAmount() {
		return clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(dEntryTotal());
	}

	/*
	 * public boolean sEntryAmount (String sEntryAmount){ sEntryAmount =
	 * sEntryAmount.replace(",", ""); try{ sEntryAmount =
	 * sEntryAmount.replace(",", ""); BigDecimal bd = new
	 * BigDecimal(sEntryAmount); bd = bd.setScale(2, BigDecimal.ROUND_HALF_UP);
	 * m_sEntryAmount = bd; return true; }catch (NumberFormatException e){
	 * System.out.println(this.toString() +
	 * "Error converting Entry amount from string: " + sEntryAmount + ".");
	 * System.out.println(this.toString() + e.getMessage()); return false; } }
	 */

	public void sBatchType(String sBatchType) {
		m_sBatchType = sBatchType;
	}

	public String sBatchType() {
		return m_sBatchType;
	}

	public boolean setLineItemNumber(String sLineID, String sItemNumber) {
		long lLineID;
		try {
			lLineID = Long.parseLong(sLineID);
		} catch (NumberFormatException e) {
			System.out.println(this.toString() + "error formatting long from "
					+ sLineID + " in setLineItemNumber");
			return false;
		}
		for (int i = 0; i < LineArray.size(); i++) {
			ICEntryLine Line = (ICEntryLine) LineArray.get(i);
			if (Line.sId().compareToIgnoreCase(Long.toString(lLineID)) == 0) {
				if (!Line.sItemNumber(sItemNumber)) {
					m_sErrorMessage.add(Line.getErrorMessage());
				}
				return Line.sItemNumber(sItemNumber);
			}
		}
		return false;
	}

	public boolean setLineLocation(String sLineID, String sLocation) {
		long lLineID;
		try {
			lLineID = Long.parseLong(sLineID);
		} catch (NumberFormatException e) {
			System.out.println(this.toString() + "error formatting long from "
					+ sLineID + " in setLineLocation");
			return false;
		}
		for (int i = 0; i < LineArray.size(); i++) {
			ICEntryLine Line = (ICEntryLine) LineArray.get(i);
			if (Line.sId().compareToIgnoreCase(Long.toString(lLineID)) == 0) {
				if (!Line.sLocation(sLocation)) {
					m_sErrorMessage.add(Line.getErrorMessage());
				}
				return Line.sLocation(sLocation);
			}
		}
		return false;
	}

	public boolean setLineTargetLocation(String sLineID, String sTargetLocation) {
		long lLineID;
		try {
			lLineID = Long.parseLong(sLineID);
		} catch (NumberFormatException e) {
			System.out.println(this.toString() + "error formatting long from "
					+ sLineID + " in setLineTargetLocation");
			return false;
		}
		for (int i = 0; i < LineArray.size(); i++) {
			ICEntryLine Line = (ICEntryLine) LineArray.get(i);
			if (Line.sId().compareToIgnoreCase(Long.toString(lLineID)) == 0) {
				if (!Line.sTargetLocation(sTargetLocation)) {
					m_sErrorMessage.add(Line.getErrorMessage());
				}
				return Line.sTargetLocation(sTargetLocation);
			}
		}
		return false;
	}

	public boolean setLineDescription(String sLineID, String sDescription) {
		long lLineID;
		try {
			lLineID = Long.parseLong(sLineID);
		} catch (NumberFormatException e) {
			System.out.println(this.toString() + "error formatting long from "
					+ sLineID + " in setLineDescription");
			return false;
		}
		for (int i = 0; i < LineArray.size(); i++) {
			ICEntryLine Line = (ICEntryLine) LineArray.get(i);
			if (Line.sId().compareToIgnoreCase(Long.toString(lLineID)) == 0) {
				if (!Line.sDescription(sDescription)) {
					m_sErrorMessage.add(Line.getErrorMessage());
				}
				return Line.sDescription(sDescription);
			}
		}
		return false;
	}

	public boolean setLineComment(String sLineID, String sComment) {
		long lLineID;
		try {
			lLineID = Long.parseLong(sLineID);
		} catch (NumberFormatException e) {
			System.out.println(this.toString() + "error formatting long from "
					+ sLineID + " in setLineComment");
			return false;
		}
		for (int i = 0; i < LineArray.size(); i++) {
			ICEntryLine Line = (ICEntryLine) LineArray.get(i);
			if (Line.sId().compareToIgnoreCase(Long.toString(lLineID)) == 0) {
				if (!Line.sComment(sComment)) {
					m_sErrorMessage.add(Line.getErrorMessage());
				}
				return Line.sComment(sComment);
			}
		}
		return false;
	}

	public boolean setLineControlAcct(String sLineID, String sControlAcct) {
		long lLineID;
		try {
			lLineID = Long.parseLong(sLineID);
		} catch (NumberFormatException e) {
			System.out.println(this.toString() + "error formatting long from "
					+ sLineID + " in setLineControlAcct");
			return false;
		}
		for (int i = 0; i < LineArray.size(); i++) {
			ICEntryLine Line = (ICEntryLine) LineArray.get(i);
			if (Line.sId().compareToIgnoreCase(Long.toString(lLineID)) == 0) {
				if (!Line.sControlAcct(sControlAcct)) {
					m_sErrorMessage.add(Line.getErrorMessage());
				}
				return Line.sControlAcct(sControlAcct);
			}
		}
		return false;
	}

	public boolean setLineDistributionAcct(String sLineID,
			String sDistributionAcct) {
		long lLineID;
		try {
			lLineID = Long.parseLong(sLineID);
		} catch (NumberFormatException e) {
			System.out.println(this.toString() + "error formatting long from "
					+ sLineID + " in setLineDistributionAcct");
			return false;
		}
		for (int i = 0; i < LineArray.size(); i++) {
			ICEntryLine Line = (ICEntryLine) LineArray.get(i);
			if (Line.sId().compareToIgnoreCase(Long.toString(lLineID)) == 0) {
				if (!Line.sDistributionAcct(sDistributionAcct)) {
					m_sErrorMessage.add(Line.getErrorMessage());
				}
				return Line.sDistributionAcct(sDistributionAcct);
			}
		}
		return false;
	}

	public boolean setLineCategory(String sLineID, String sCategory) {
		long lLineID;
		try {
			lLineID = Long.parseLong(sLineID);
		} catch (NumberFormatException e) {
			System.out.println(this.toString() + "error formatting long from "
					+ sLineID + " in setLineCategory");
			return false;
		}
		for (int i = 0; i < LineArray.size(); i++) {
			ICEntryLine Line = (ICEntryLine) LineArray.get(i);
			if (Line.sId().compareToIgnoreCase(Long.toString(lLineID)) == 0) {
				if (!Line.sCategoryCode(sCategory)) {
					m_sErrorMessage.add(Line.getErrorMessage());
				}
				return Line.sCategoryCode(sCategory);
			}
		}
		return false;
	}

	public boolean setLineReceiptNumber(String sLineID, String sReceiptNumber) {
		long lLineID;
		try {
			lLineID = Long.parseLong(sLineID);
		} catch (NumberFormatException e) {
			System.out.println(this.toString() + "error formatting long from "
					+ sLineID + " in setLineReceiptNumber");
			return false;
		}
		for (int i = 0; i < LineArray.size(); i++) {
			ICEntryLine Line = (ICEntryLine) LineArray.get(i);
			if (Line.sId().compareToIgnoreCase(Long.toString(lLineID)) == 0) {
				if (!Line.sReceiptNum(sReceiptNumber)) {
					m_sErrorMessage.add(Line.getErrorMessage());
				}
				return Line.sReceiptNum(sReceiptNumber);
			}
		}
		return false;
	}

	public boolean setLineCostBucketID(String sLineID, String sCostBucketID) {
		long lLineID;
		try {
			lLineID = Long.parseLong(sLineID);
		} catch (NumberFormatException e) {
			System.out.println(this.toString() + "error formatting long from "
					+ sLineID + " in setLineCostBucketID");
			return false;
		}
		for (int i = 0; i < LineArray.size(); i++) {
			ICEntryLine Line = (ICEntryLine) LineArray.get(i);
			if (Line.sId().compareToIgnoreCase(Long.toString(lLineID)) == 0) {
				Line.sCostBucketID(sCostBucketID);
				return true;
			}
		}
		return false;
	}

	public boolean setLinePriceString(String sLineID, String sPrice) {

		long lLineID;
		try {
			lLineID = Long.parseLong(sLineID);
		} catch (NumberFormatException e) {
			System.out.println(this.toString() + "error formatting long from "
					+ sLineID + " in setLinePriceString");
			return false;
		}

		for (int i = 0; i < LineArray.size(); i++) {
			ICEntryLine Line = (ICEntryLine) LineArray.get(i);
			if (Line.sId().compareToIgnoreCase(Long.toString(lLineID)) == 0) {
				if (!Line.setPriceString(sPrice)) {
					m_sErrorMessage.add(Line.getErrorMessage());
					return false;
				}
				return true;
			}
		}
		return true;
	}

	public boolean setLineCostString(String sLineID, String sCost) {

		long lLineID;
		try {
			lLineID = Long.parseLong(sLineID);
		} catch (NumberFormatException e) {
			System.out.println(this.toString() + "error formatting long from "
					+ sLineID + " in setLineCostString");
			return false;
		}

		for (int i = 0; i < LineArray.size(); i++) {
			ICEntryLine Line = (ICEntryLine) LineArray.get(i);
			if (Line.sId().compareToIgnoreCase(Long.toString(lLineID)) == 0) {
				if (!Line.setCostString(sCost)) {
					m_sErrorMessage.add(Line.getErrorMessage());
				}
				return Line.setCostString(sCost);
			}
		}
		return true;
	}

	public boolean setLineCost(String sLineID, BigDecimal dCost) {

		long lLineID;
		try {
			lLineID = Long.parseLong(sLineID);
		} catch (NumberFormatException e) {
			System.out.println(this.toString() + "error formatting long from "
					+ sLineID + " in setLineCostString");
			return false;
		}

		for (int i = 0; i < LineArray.size(); i++) {
			ICEntryLine Line = (ICEntryLine) LineArray.get(i);
			if (Line.sId().compareToIgnoreCase(Long.toString(lLineID)) == 0) {
				Line.setCostString(clsManageBigDecimals
						.BigDecimalTo2DecimalSTDFormat(dCost));
			}
		}
		return true;
	}

	public boolean setLineQty(String sLineID, BigDecimal dQty) {

		long lLineID;
		try {
			lLineID = Long.parseLong(sLineID);
		} catch (NumberFormatException e) {
			System.out.println(this.toString() + "error formatting long from "
					+ sLineID + " in setLineQtyString");
			return false;
		}

		for (int i = 0; i < LineArray.size(); i++) {
			ICEntryLine Line = (ICEntryLine) LineArray.get(i);
			if (Line.sId().compareToIgnoreCase(Long.toString(lLineID)) == 0) {
				Line.setQtyString(clsManageBigDecimals.BigDecimalToFormattedString(
						"#########0.0000", dQty));
			}
		}
		return true;
	}

	public boolean setLineQtyString(String sLineID, String sQty) {

		long lLineID;
		try {
			lLineID = Long.parseLong(sLineID);
		} catch (NumberFormatException e) {
			System.out.println(this.toString() + "error formatting long from "
					+ sLineID + " in setLineQtyString");
			m_sErrorMessage.add("Invalid qty: " + sQty);
			return false;
		}

		for (int i = 0; i < LineArray.size(); i++) {
			ICEntryLine Line = (ICEntryLine) LineArray.get(i);
			if (Line.sId().compareToIgnoreCase(Long.toString(lLineID)) == 0) {
				if (!Line.setQtyString(sQty)) {
					m_sErrorMessage.add(Line.getErrorMessage());
					System.out.println("In " + this.toString()
							+ " adding Line.getErrorMessage: "
							+ Line.getErrorMessage());
				}
				return Line.setQtyString(sQty);
			}
		}
		return true;
	}

	public BigDecimal getLineCost(String sLineID) {

		long lLineID;
		BigDecimal d = BigDecimal.ZERO;
		try {
			lLineID = Long.parseLong(sLineID);
		} catch (NumberFormatException e) {
			System.out.println(this.toString() + "error formatting long from "
					+ sLineID + " in setLineAmountString");
			return d;
		}

		for (int i = 0; i < LineArray.size(); i++) {
			ICEntryLine Line = (ICEntryLine) LineArray.get(i);
			if (Line.sId().compareToIgnoreCase(Long.toString(lLineID)) == 0) {
				d = new BigDecimal(Line.sCostSTDFormat().replace(",", ""));
			}
		}
		return d;
	}

	public boolean validate_entry_fields(Connection conn, String sUserID) {
		// Validate the entries here:
		boolean bEntriesAreValid = true;
		m_sDocNumber = m_sDocNumber.trim();
		if (m_sDocNumber.length() == 0) {
			// TBDL
			//System.out.println(this.toString()
			//		+ "Document number is empty - ICEntry.save.");
			m_sErrorMessage.add("Document number is empty.");
			bEntriesAreValid = false;
		}
		if (m_sDocNumber.length() > SMTableicbatchentries.sdocnumberLength) {
			System.out.println(this.toString()
					+ "Document number is too long - ICEntry.save.");
			m_sErrorMessage.add("Document number is too long.");
			bEntriesAreValid = false;
		}
		if (m_sEntryDescription.length() > SMTableicbatchentries.sentrydescriptionLength) {
			System.out.println(this.toString()
					+ "Entry description is too long - ICEntry.save.");
			m_sErrorMessage.add("Entry description is too long.");
			bEntriesAreValid = false;
		}
		// Validate the entry date:
		if (!clsDateAndTimeConversions.IsValidDateString("MM/dd/yyyy", m_sEntryDate)) {
			m_sErrorMessage.add("Entry date '" + m_sEntryDate + "' is invalid.");
			bEntriesAreValid = false;
		}

		SMOption opt = new SMOption();
		try {
			opt.checkDateForPosting(m_sEntryDate, "Entry Date", conn, sUserID);
		} catch (Exception e1) {
			m_sErrorMessage.add(e1.getMessage());
			bEntriesAreValid = false;
		}
		
		try {
			Integer.parseInt(m_sBatchType);
		} catch (NumberFormatException e) {
			m_sErrorMessage.add("Invalid batch type: " + m_sBatchType);
			bEntriesAreValid = false;
		}
		if (ICBatchTypes.Get_Batch_Type(Integer.parseInt(m_sBatchType))
				.compareToIgnoreCase("") == 0) {
			m_sErrorMessage.add("Invalid batch type: " + m_sBatchType);
			bEntriesAreValid = false;
		}

		try {
			Integer.parseInt(m_sEntryType);
		} catch (NumberFormatException e) {
			m_sErrorMessage.add("Invalid entry type: " + m_sEntryType);
			bEntriesAreValid = false;
		}
		if (ICEntryTypes.Get_Entry_Type(Integer.parseInt(m_sEntryType))
				.compareToIgnoreCase("") == 0) {
			m_sErrorMessage.add("Invalid entry type: " + m_sEntryType);
			bEntriesAreValid = false;
		}

		// Set the last line value:
		lNumberOfLines(LineArray.size());

		return bEntriesAreValid;
	}

	public boolean validate_lines(Connection conn) {
		// Validate the lines here:

		assign_line_numbers();

		boolean bLinesValidated = true;
		// Don't allow any entries to be saved without lines:
		/*
		 * if (LineArray.size() < 1){
		 * m_sErrorMessage.add("Entry cannot be saved with no detail lines.");
		 * System.out.println(m_sErrorMessage); bLinesValidated = false; }
		 */
		for (int i = 0; i < LineArray.size(); i++) {
			ICEntryLine line = (ICEntryLine) LineArray.get(i);

			if (!validateSingleLine(line, conn)) {
				bLinesValidated = false;
			}
		}
		return bLinesValidated;
	}

	/**
	 * @param entryline
	 * @param conn
	 * @return
	 */
	/**
	 * @param entryline
	 * @param conn
	 * @return
	 */
	/**
	 * @param entryline
	 * @param conn
	 * @return
	 */
	public boolean validateSingleLine(ICEntryLine entryline, Connection conn) {

		boolean bLineValidated = true;

		entryline.sBatchNumber(Long.toString(lBatchNumber()));
		entryline.sEntryNumber(Long.toString(lEntryNumber()));
		entryline.lEntryId(lid());

		// ***********************************
		// ITEM VALIDATION
		entryline.sItemNumber(entryline.sItemNumber().trim());
		if (entryline.sItemNumber().length() > SMTableicentrylines.sitemnumberLength) {
			m_sErrorMessage.add("Item number cannot be longer than "
					+ SMTableicentrylines.sitemnumberLength
					+ " characters on line number " + entryline.sLineNumber()
					+ ".");
			bLineValidated = false;
		}
		if (entryline.sItemNumber().compareToIgnoreCase("") == 0) {
			m_sErrorMessage.add("Item number cannot be blank on line number "
					+ entryline.sLineNumber() + ".");
			bLineValidated = false;
		}
		// Validate the item number itself:
		String SQL = "SELECT " 
		+ SMTableicitems.sItemNumber 
		+ ", " + SMTableicitems.inonstockitem
		+ ", " + SMTableicitems.iActive
		+ ", " + SMTableicitems.sItemDescription
		+ " FROM " + SMTableicitems.TableName 
		+ " WHERE "
		+ SMTableicitems.sItemNumber + " = '" + entryline.sItemNumber()
		+ "'";
		try {
			ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
			if (rs.next()) {
				if (rs.getLong(SMTableicitems.inonstockitem) == 1){
					m_sErrorMessage.add("Item number " + entryline.sItemNumber()
							+ " is a NON-STOCK item.");
					bLineValidated = false;
				}
				if (rs.getLong(SMTableicitems.iActive) != 1){
					m_sErrorMessage.add("Item number " + entryline.sItemNumber()
							+ " is an INACTIVE item.");
					bLineValidated = false;
				}
				// DESCRIPTION VALIDATION
				// *******************************************************************
				entryline.sDescription(entryline.sDescription().trim());
				if (entryline.sDescription().length() > SMTableicentrylines.sdescriptionLength) {
					m_sErrorMessage.add("Description cannot be longer than "
							+ SMTableicentrylines.sdescriptionLength
							+ " characters on line number " + entryline.sLineNumber()
							+ ".");
					bLineValidated = false;
				}
				if (entryline.sDescription().compareToIgnoreCase("") == 0){
					entryline.sDescription(rs.getString(SMTableicitems.sItemDescription));
				}
				// END DESCRIPTION VALIDATION
				// *********************************************************************
			}else{
				m_sErrorMessage.add("Item number " + entryline.sItemNumber()
						+ " is not valid.");
				bLineValidated = false;
			}
			rs.close();
		} catch (SQLException e) {
			m_sErrorMessage.add("SQL error validating item number "
					+ entryline.sItemNumber() + " on " + " line number "
					+ entryline.sLineNumber() + ": " + e.getMessage());
			bLineValidated = false;
		}
		// END ITEM VALIDATION
		// ***********************************************

		// LOCATION VALIDATION
		// ***********************************************
		// Location:
		entryline.sLocation(entryline.sLocation().trim());
		if (entryline.sLocation().length() > SMTableicentrylines.slocationLength) {
			m_sErrorMessage.add("Location cannot be longer than "
					+ SMTableicentrylines.slocationLength
					+ " characters on line number " + entryline.sLineNumber()
					+ ".");
			bLineValidated = false;
		}
		if (entryline.sLocation().compareToIgnoreCase("") == 0) {
			m_sErrorMessage.add("Location cannot be blank on line number "
					+ entryline.sLineNumber() + ".");
			bLineValidated = false;
		}
		// Validate the location itself:
		String sInvAccount = "";
		String sPayablesClearingAccount = "";
		String sTransferClearingAcct = "";
		SQL = "SELECT " + SMTablelocations.sGLInventoryAcct 
		+ ", " + SMTablelocations.sGLPayableClearingAcct
		+ ", " + SMTablelocations.sGLTransferClearingAcct
		+ " FROM "
		+ SMTablelocations.TableName + " WHERE ("
		+ SMTablelocations.sLocation + " = '" + entryline.sLocation()
		+ "'" + ")";
		try {
			ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
			if (!rs.next()) {
				m_sErrorMessage.add("Location " + entryline.sLocation()
						+ " is not valid on " + " line number "
						+ entryline.sLineNumber() + ".");
				bLineValidated = false;
			} else {
				sInvAccount = rs.getString(SMTablelocations.sGLInventoryAcct);
				sPayablesClearingAccount = rs
				.getString(SMTablelocations.sGLPayableClearingAcct);
				sTransferClearingAcct = rs
				.getString(SMTablelocations.sGLTransferClearingAcct);
			}
			rs.close();
		} catch (SQLException e) {
			m_sErrorMessage.add("SQL error validating location "
					+ entryline.sLocation() + " on " + " line number "
					+ entryline.sLineNumber() + ": " + e.getMessage());
			bLineValidated = false;
		}
		// END LOCATION VALIDATION
		// ***************************************************

		// TARGET LOCATION VALIDATION
		// ***********************************************
		// Target Location:
		entryline.sTargetLocation(entryline.sTargetLocation().trim());
		if (entryline.sTargetLocation().length() > SMTableicentrylines.stargetlocationLength) {
			m_sErrorMessage.add("TO Location cannot be longer than "
					+ SMTableicentrylines.stargetlocationLength
					+ " characters on line number " + entryline.sLineNumber()
					+ ".");
			bLineValidated = false;
		}

		if (Integer.parseInt(m_sEntryType) == ICEntryTypes.TRANSFER_ENTRY) {
			if (entryline.sTargetLocation().compareToIgnoreCase("") == 0) {
				m_sErrorMessage
				.add("TO Location cannot be blank on line number "
						+ entryline.sLineNumber() + ".");
				bLineValidated = false;
			}

			// Must have different 'FROM' and 'TO' locations:
			if (entryline.sTargetLocation().compareToIgnoreCase(
					entryline.sLocation()) == 0) {
				m_sErrorMessage
				.add("FROM and TO Locations must be different on line number "
						+ entryline.sLineNumber() + ".");
				bLineValidated = false;
			}

			// Validate the location itself:
			SQL = "SELECT " + SMTablelocations.sLocation + " FROM "
			+ SMTablelocations.TableName + " WHERE ("
			+ SMTablelocations.sLocation + " = '"
			+ entryline.sTargetLocation() + "'" + ")";
			try {
				ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
				if (!rs.next()) {
					m_sErrorMessage.add("TO location "
							+ entryline.sTargetLocation() + " is not valid on "
							+ " line number " + entryline.sLineNumber() + ".");
					bLineValidated = false;
				}
				rs.close();
			} catch (SQLException e) {
				m_sErrorMessage.add("SQL error validating TO location "
						+ entryline.sTargetLocation() + " on "
						+ " line number " + entryline.sLineNumber() + ": "
						+ e.getMessage());
				bLineValidated = false;
			}
		}
		// END TARGET LOCATION VALIDATION
		// ***************************************************

		// CATEGORY VALIDATION
		// ****************************************************************************
		// Category code:
		entryline.sCategoryCode(entryline.sCategoryCode().trim());
		if (entryline.sCategoryCode().length() > SMTableicentrylines.scategorycodeLength) {
			m_sErrorMessage.add("Category code cannot be longer than "
					+ SMTableicentrylines.scategorycodeLength
					+ " characters on line number " + entryline.sLineNumber()
					+ ".");
			bLineValidated = false;
		}
		// Shipments must have a category code:
		if (Integer.parseInt(m_sEntryType) == ICEntryTypes.SHIPMENT_ENTRY) {
			if (entryline.sCategoryCode().compareToIgnoreCase("") == 0) {
				m_sErrorMessage
				.add("Category code cannot be blank on line number "
						+ entryline.sLineNumber() + ".");
				bLineValidated = false;
			}
		}
		// END CATEGORY VALIDATION
		// **********************************************************************

		// CONTROL ACCT VALIDATION
		// ****************************************************
		// If the control account is blank, try to set it to the default INV
		// account for the location:
		if (entryline.sControlAcct().trim().equals("")) {
			entryline.sControlAcct(sInvAccount);
		}
		// END CONTROL ACCT VALIDATION
		// ****************************************************

		// DISTRIBUTION ACCT VALIDATION
		// ******************************************************
		// Validate distribution account:
		if (Integer.parseInt(m_sEntryType) == ICEntryTypes.ADJUSTMENT_ENTRY) {
			if (entryline.sDistributionAcct().trim().equals("")) {
				m_sErrorMessage
				.add("Distribution Acct cannot be blank on line number "
						+ entryline.sLineNumber() + ".");
				bLineValidated = false;
			}
		}
		if (Integer.parseInt(m_sEntryType) == ICEntryTypes.PHYSICALCOUNT_ENTRY) {
			if (entryline.sDistributionAcct().trim().equals("")) {
				m_sErrorMessage
				.add("Write-off Acct cannot be blank on line number "
						+ entryline.sLineNumber() + ".");
				bLineValidated = false;
			}
		}
		if (Integer.parseInt(m_sEntryType) == ICEntryTypes.RECEIPT_ENTRY) {
			if (entryline.sDistributionAcct().trim().equals("")) {
				entryline.sDistributionAcct(sPayablesClearingAccount);
			}
		}

		if (Integer.parseInt(m_sEntryType) == ICEntryTypes.SHIPMENT_ENTRY) {
			if (entryline.sDistributionAcct().trim().equals("")) {
				// Need to read the category to get the correct distribution
				// account:
				SQL = "SELECT " + SMTableiccategories.sCostofGoodsSoldAccount
				+ " FROM " + SMTableiccategories.TableName + " WHERE "
				+ SMTableiccategories.sCategoryCode + " = '"
				+ entryline.sCategoryCode() + "'";
				try {
					ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
					if (rs.next()) {
						entryline
						.sDistributionAcct(rs
								.getString(SMTableiccategories.sCostofGoodsSoldAccount));
					} else {
						m_sErrorMessage
						.add("Cannot read cost of goods sold account from category on line number "
								+ entryline.sLineNumber() + ".");
						bLineValidated = false;
					}
					rs.close();
				} catch (SQLException e) {
					m_sErrorMessage
					.add("Cannot read cost of goods sold account from category on line number "
							+ entryline.sLineNumber()
							+ " - "
							+ e.getMessage());
					bLineValidated = false;
				}
			}
		}

		//This is the account for the FROM location - the account for the TO location
		//will have to be calculated at the time of the batch posting:
		if (Integer.parseInt(m_sEntryType) == ICEntryTypes.TRANSFER_ENTRY) {
			entryline.sDistributionAcct(sTransferClearingAcct);
		}

		// END DISTRIBUTION ACCT VALIDATION
		// *******************************************************

		// COMMENT VALIDATION:
		// ***********************************************************
		entryline.sComment(entryline.sComment().trim());
		if (entryline.sComment().length() > SMTableicentrylines.scommentLength) {
			m_sErrorMessage.add("Comment cannot be longer than "
					+ SMTableicentrylines.scommentLength
					+ " characters on line number " + entryline.sLineNumber()
					+ ".");
			bLineValidated = false;
		}
		// END COMMENT VALIDATION
		// ****************************************************************

		// COST VALIDATION
		// ****************************************************************
		// Test for a valid cost on adjustments:
		if (Integer.parseInt(m_sEntryType) == ICEntryTypes.ADJUSTMENT_ENTRY) {
			if (!entryline.setCostString(entryline.sCostSTDFormat().replace(
					",", ""))) {
				m_sErrorMessage.add("Invalid cost: "
						+ entryline.sCostSTDFormat() + " on line number "
						+ entryline.sLineNumber() + ".");
				bLineValidated = false;
			}
		}
		// Test for a valid cost on shipment returns:
		if (Integer.parseInt(m_sEntryType) == ICEntryTypes.SHIPMENT_ENTRY) {
			// If it's a shipment return:
			if (!entryline.sQtySTDFormat().startsWith("-")) {
				if (!entryline.setCostString(entryline.sCostSTDFormat()
						.replace(",", ""))) {
					m_sErrorMessage.add("Invalid cost: "
							+ entryline.sCostSTDFormat() + " on line number "
							+ entryline.sLineNumber() + ".");
					bLineValidated = false;
				}
				// Else if it's a shipment:
			} else {
				if (!entryline.setCostString(entryline.sCostSTDFormat()
						.replace(",", ""))) {
					m_sErrorMessage.add("Invalid cost: "
							+ entryline.sCostSTDFormat() + " on line number "
							+ entryline.sLineNumber() + ".");
					bLineValidated = false;
				}
			}
		}

		// END COST VALIDATION
		// ******************************************************************

		// PRICE VALIDATION
		// **********************************************************************
		// Only shipments need a validated price:
		if (Integer.parseInt(m_sEntryType) != ICEntryTypes.SHIPMENT_ENTRY) {
			entryline.setPriceString("0.00");
		} else {
			if (!entryline.setPriceString(entryline.sPriceSTDFormat().replace(
					",", ""))) {
				m_sErrorMessage.add("Invalid price: "
						+ entryline.sPriceSTDFormat() + " on line number "
						+ entryline.sLineNumber() + ".");
				bLineValidated = false;
			}
		}
		// END PRICE VALIDATION
		// ***********************************************************************

		// QTY VALIDATION
		// **********************************************************************
		// Qty:
		if (!entryline.setQtyString(entryline.sQtySTDFormat().replace(",", ""))) {
			m_sErrorMessage.add("Invalid qty: " + entryline.sQtySTDFormat()
					+ " on line number " + entryline.sLineNumber() + ".");
			bLineValidated = false;
		}
		if ((Integer.parseInt(m_sEntryType) == ICEntryTypes.RECEIPT_ENTRY)
				|| (Integer.parseInt(m_sEntryType) == ICEntryTypes.TRANSFER_ENTRY)) {
			// Receipts and transfers cannot have a negative qty:
			if (entryline.sQtySTDFormat().contains("-")) {
				m_sErrorMessage.add("Qty cannot be negative"
						+ " on line number " + entryline.sLineNumber() + ".");
				bLineValidated = false;
			}
			// Receipts and transfers cannot have a zero qty:
			if (entryline.sQtySTDFormat().compareToIgnoreCase("0.0000") == 0) {
				m_sErrorMessage.add("Qty cannot be zero" + " on line number "
						+ entryline.sLineNumber() + ".");
				bLineValidated = false;
			}
		}
		if (Integer.parseInt(m_sEntryType) == ICEntryTypes.PHYSICALCOUNT_ENTRY) {
			// Physical counts cannot have a zero qty:
			if (entryline.sQtySTDFormat().compareToIgnoreCase("0.0000") == 0) {
				m_sErrorMessage.add("Qty cannot be zero" + " on line number "
						+ entryline.sLineNumber() + ".");
				bLineValidated = false;
			}
		}

		// END QTY VALIDATION
		// *************************************************************************

		// RECEIPT NUMBER VALIDATION
		// ***********************************************************************
		// Receipt number:
		entryline.sReceiptNum(entryline.sReceiptNum().trim());
		if (entryline.sReceiptNum().length() > SMTableicentrylines.sreceiptnumLength) {
			m_sErrorMessage.add("Receipt number cannot be longer than "
					+ SMTableicentrylines.sreceiptnum
					+ " characters on line number " + entryline.sLineNumber()
					+ ".");
			bLineValidated = false;
		}

		// Must have receipt numbers on receipts/returns:
		if (Integer.parseInt(m_sEntryType) == ICEntryTypes.RECEIPT_ENTRY) {
			if (entryline.sReceiptNum().compareToIgnoreCase("") == 0) {
				m_sErrorMessage
				.add("Receipt number cannot be blank on line number "
						+ entryline.sLineNumber() + ".");
				bLineValidated = false;
			}
		}
		// END RECEIPT NUMBER VALIDATION
		// ***********************************************************************

		// COST BUCKET ID AND QTY OR COST FOR ADJUSTMENTS VALIDATION
		// ****************************************************************************
		// Cost Bucket ID:
		// First, get the costing method:

		long lCostingMethod = 0;
		SQL = "SELECT " + SMTableicoptions.lcostingmethod + " FROM "
		+ SMTableicoptions.TableName;

		try {
			ResultSet rsOptions = clsDatabaseFunctions.openResultSet(SQL, conn);
			if (!rsOptions.next()) {
				m_sErrorMessage.add("Could not read costing method for "
						+ entryline.sLineNumber() + ".");
				bLineValidated = false;
			} else {
				lCostingMethod = rsOptions
				.getLong(SMTableicoptions.lcostingmethod);
			}
			rsOptions.close();
		} catch (SQLException e) {
			m_sErrorMessage.add("Could not read costing method for "
					+ entryline.sLineNumber() + " - " + e.getMessage());
			bLineValidated = false;
		}

		if (lCostingMethod != SMTableicoptions.COSTING_METHOD_AVERAGECOST) {
			entryline.sCostBucketID(entryline.sCostBucketID().trim());
			// Adjustments must have a cost bucket ID:
			// TODO - may need to validate for other types: shipments, receipts,
			// transfers, etc.
			if (Integer.parseInt(m_sEntryType) == ICEntryTypes.ADJUSTMENT_ENTRY) {
				if (entryline.sCostBucketID().compareToIgnoreCase("") == 0) {
					m_sErrorMessage
					.add("No cost bucket selected on line number "
							+ entryline.sLineNumber() + ".");
					bLineValidated = false;
				}
				// Must have either a cost or a qty if it's an adjustment:
				BigDecimal dCost = BigDecimal.ZERO;
				try {
					dCost = new BigDecimal(entryline.sCostSTDFormat().replace(",", ""));
				} catch (Exception e1) {
					m_sErrorMessage
					.add("Cost entered ('" + entryline.sCostSTDFormat() + "') on line number "
							+ entryline.sLineNumber() + " is invalid.");
					return false;
				}
				BigDecimal dQty = BigDecimal.ZERO;
				try {
					dQty = new BigDecimal(entryline.sQtySTDFormat().replace(",", ""));
				} catch (Exception e1) {
					m_sErrorMessage
					.add("Quantity entered ('" + entryline.sCostSTDFormat() + "') on line number "
							+ entryline.sLineNumber() + " is invalid.");
					return false;
				}
				if ((dCost.compareTo(BigDecimal.ZERO) == 0)
						&& (dQty.compareTo(BigDecimal.ZERO) == 0)) {
					m_sErrorMessage
					.add("Adjustment needs either a qty or a cost on line "
							+ entryline.sLineNumber() + ".");
					bLineValidated = false;
				}
				// If a particular cost bucket was chosen, validate it:
				if ((entryline.sCostBucketID().compareToIgnoreCase("-1") != 0)
						&& (entryline.sCostBucketID().compareToIgnoreCase("") != 0)) {
					// Validate the cost bucket:
					SQL = "SELECT " + SMTableiccosts.iId + " FROM "
					+ SMTableiccosts.TableName + " WHERE (" + "("
					+ SMTableiccosts.sItemNumber + " = '"
					+ entryline.sItemNumber() + "')" + " AND ("
					+ SMTableiccosts.sLocation + " = '"
					+ entryline.sLocation() + "')" + " AND ("
					+ SMTableiccosts.iId + " = "
					+ entryline.sCostBucketID() + ")" + ")";
					try {
						ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
						if (!rs.next()) {
							m_sErrorMessage.add("Cost bucket "
									+ entryline.sItemNumber()
									+ " is not valid on " + " line number "
									+ entryline.sLineNumber() + ".");
							bLineValidated = false;
						}
						rs.close();
					} catch (SQLException e) {
						m_sErrorMessage.add("SQL error validating cost bucket "
								+ entryline.sItemNumber() + " on "
								+ " line number " + entryline.sLineNumber()
								+ ": " + e.getMessage());
						bLineValidated = false;
					}
				}
			}
		} else {
			// If it IS average costing:
			entryline.sCostBucketID("-1");
		}
		// END COST BUCKET AND QTY OR COST VALIDATION
		// ********************************************
		return bLineValidated;
	}

	private boolean save_lines(Connection conn, String sEntryID) {

		/*
		 * Save/update each line, one at a time.
		 * 
		 * If there are any lines that are in the data, but NOT in the entry, we
		 * need to delete those. If the line is a new line, add it. If the line
		 * is an existing line, update it.
		 */
		if (!delete_removed_lines(conn)) {
			return false;
		}

		for (int i = 0; i < LineArray.size(); i++) {
			ICEntryLine line = (ICEntryLine) LineArray.get(i);
			// Set the line number based on the index of the LineArray:
			line.sLineNumber(Integer.toString(i + 1));
			if (line.sId().compareToIgnoreCase("-1") == 0) {
				if (!add_single_line(line, conn, sEntryID)) {
					return false;
				}
			} else {
				// Else, if it's already got a line id, then it's an existing
				// line, and we can update it:
				if (!update_single_line(line, conn, sEntryID)) {
					return false;
				}
			}
		}
		return true;
	}

	private int get_last_entry_number_in_batch(String sBatchNumber,
			Connection conn) {

		String SQL = "SELECT " + SMTableicbatchentries.lentrynumber + " FROM "
		+ SMTableicbatchentries.TableName + " WHERE (" + "("
		+ SMTableicbatchentries.lbatchnumber + " = " + sBatchNumber
		+ ")" + ")" + " ORDER BY " + SMTableicbatchentries.lentrynumber
		+ " DESC LIMIT 1";
		try {
			ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
			if (rs.next()) {
				int iEntry = rs.getInt(SMTableicbatchentries.lentrynumber);
				rs.close();
				return iEntry;
			} else {
				rs.close();
				return 0;
			}
		} catch (SQLException ex) {
			System.out.println("Error in " + this.toString() + " class!!");
			System.out.println(this.toString() + "SQLException: "
					+ ex.getMessage());
			System.out.println(this.toString() + "SQLState: "
					+ ex.getSQLState());
			System.out.println(this.toString() + "SQL: " + ex.getErrorCode());
			return -1;
		}

	}

	private boolean add_new_entry(Connection conn, String sUserID) {
		if (!validate_entry_fields(conn, sUserID)) {
			// System.out.println(m_sErrorMessage);
			return false;
		}
		int iLastEntryNumberInBatch = -1;
		iLastEntryNumberInBatch = get_last_entry_number_in_batch(
				sBatchNumber(), conn) + 1;
		if (iLastEntryNumberInBatch == -1) {
			m_sErrorMessage.add("Could not get last entry number.");
			System.out.println(this.toString() + " - " + m_sErrorMessage);
			return false;
		} else {
			lEntryNumber(iLastEntryNumberInBatch);
		}

		//System.out.println("In " + this.toString() + ".add_new_entry - 001");
		// Add a new entry:
		java.sql.Date datEntryDate = null;
		try {
			datEntryDate = clsDateAndTimeConversions.StringTojavaSQLDate("MM/dd/yyyy",m_sEntryDate);
		} catch (ParseException e) {
			System.out.println("Error:[1423767981] Invalid entry date: '" 
					+ m_sEntryDate + "' - " + e.getMessage());
		}
		sEntryDate(clsDateAndTimeConversions.utilDateToString(datEntryDate, "MM/dd/yyyy"));
		String SQL = "";
		SQL = "INSERT INTO "
			+ SMTableicbatchentries.TableName
			+ " ("
			+ SMTableicbatchentries.bdentryamount
			+ ", "
			+ SMTableicbatchentries.datentrydate
			+ ", "
			+ SMTableicbatchentries.ientrytype
			+ ", "
			+ SMTableicbatchentries.lbatchnumber
			+ ", "
			+ SMTableicbatchentries.lentrynumber
			+ ", "
			+ SMTableicbatchentries.llastline
			+ ", "
			+ SMTableicbatchentries.sdocnumber
			+ ", "
			+ SMTableicbatchentries.sentrydescription
			+ ") VALUES ("
			+ clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(dEntryTotal())
			.replace(",", "") + ", '"
			+ clsDateAndTimeConversions.utilDateToString(datEntryDate, "yyyy-MM-dd")
			+ "'" + ", '" + sEntryType() + "'" + ", " + sBatchNumber()
			+ ", " + sEntryNumber() + ", " + sLastLine() + ", '"
			+ clsDatabaseFunctions.FormatSQLStatement(m_sDocNumber) + "'" + ", '"
			+ clsDatabaseFunctions.FormatSQLStatement(m_sEntryDescription) + "'"
			+ ")";
		//System.out.println("In " + this.toString() + ".add_new_entry - SQL = " + SQL);
		try {
			if (clsDatabaseFunctions.executeSQL(SQL, conn) == false) {
				m_sErrorMessage.add("Could not complete add new entry.");
				System.out.println(m_sErrorMessage);
				return false;
			} else {
				// System.out.println(this.toString() +
				// " - Successfully added new entry: " + sEntryNumber() + ".");
			}
		} catch (SQLException ex) {
			m_sErrorMessage.add("SQL Error inserting entry record: "
					+ ex.getMessage());
			System.out.println("Error in " + this.toString() + " class!!");
			System.out.println("SQLException: " + ex.getMessage());
			System.out.println("SQLState: " + ex.getSQLState());
			System.out.println("SQL: " + ex.getErrorCode());
			return false;
		}

		// Update the entry ID in this entry, now that we've added it:
		SQL = "SELECT " + SMTableicbatchentries.TableName + ".*" + ", "
		+ ICEntryBatch.TableName + "." + ICEntryBatch.ibatchtype
		+ " FROM " + SMTableicbatchentries.TableName + ", "
		+ ICEntryBatch.TableName + " WHERE (" + "("
		+ SMTableicbatchentries.TableName + "."
		+ SMTableicbatchentries.lbatchnumber + " = " + sBatchNumber()
		+ ")" + " AND (" + SMTableicbatchentries.TableName + "."
		+ SMTableicbatchentries.lentrynumber + " = " + sEntryNumber()
		+ ")" + " AND (" + SMTableicbatchentries.TableName + "."
		+ SMTableicbatchentries.lbatchnumber + " = "
		+ ICEntryBatch.TableName + "." + ICEntryBatch.lbatchnumber
		+ ")" + ")";

		try {
			ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
			if (!rs.next()) {
				m_sErrorMessage.add("Could not get entry ID for new entry.");
				System.out.println(m_sErrorMessage);
				rs.close();
				return false;
			}
			m_lTempID = rs.getLong(SMTableicbatchentries.lid);
			rs.close();
		} catch (SQLException ex) {
			m_sErrorMessage.add("SQL Error getting entry ID for new entry: "
					+ ex.getMessage());
			System.out.println("Error in " + this.toString() + " class!!");
			System.out.println("SQLException: " + ex.getMessage());
			System.out.println("SQLState: " + ex.getSQLState());
			System.out.println("SQL: " + ex.getErrorCode());
			return false;
		}
		return true;
	}

	public void remove_zero_amount_lines() {

		// TODO - we have to allow zero amount lines for adjustments, but no
		// line should have both
		// a zero amount AND a zero qty . . . .
		ArrayList<ICEntryLine> tempLineArray = new ArrayList<ICEntryLine>(0);

		// Copy the lines into the temporary array:
		for (int i = 0; i < LineArray.size(); i++) {
			tempLineArray.add((ICEntryLine) LineArray.get(i));
		}

		// Now copy back ONLY the non-zero amount lines:
		LineArray.clear();
		for (int i = 0; i < tempLineArray.size(); i++) {
			ICEntryLine line = (ICEntryLine) tempLineArray.get(i);
			// System.out.println("Line " + i + " amt: " + line.dAmount());
			if ((line.sCostSTDFormat().compareToIgnoreCase("0.00") == 0)
					&& (line.sQtySTDFormat().compareToIgnoreCase("0.0000") == 0)) {
			} else {
				// System.out.println("Adding line . . .");
				LineArray.add((ICEntryLine) tempLineArray.get(i));
			}
		}
	}

	private void assign_line_numbers() {
		for (int i = 0; i < LineArray.size(); i++) {
			ICEntryLine line = (ICEntryLine) LineArray.get(i);
			line.sLineNumber(Integer.toString(i + 1));
		}
	}

	private boolean delete_removed_lines(Connection conn) {

		// Looks for lines in the database for this entry, which are no longer
		// IN this entry.
		// If it finds any, the 'delete_single_line_from_database' function is
		// called to remove those:
		String SQL = "SELECT * " + " FROM " + SMTableicentrylines.TableName
		+ " WHERE (" + "(" + SMTableicentrylines.lbatchnumber + " = "
		+ sBatchNumber() + ")" + " AND ("
		+ SMTableicentrylines.lentrynumber + " = " + sEntryNumber()
		+ ")" + ")";
		try {
			ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);

			while (rs.next()) {
				// See if each line is still in the entry:
				if (!line_exists(rs.getLong(SMTableicentrylines.lid))) {
					if (!delete_single_line_from_database(Long.toString(rs
							.getLong(SMTableicentrylines.lid)), conn)) {
						m_sErrorMessage
						.add("Error deleting removed line with ID: "
								+ Long
								.toString(rs
										.getLong(SMTableentrylines.lid))
										+ ".");
						rs.close();
						return false;
					}
				}
			}
			rs.close();
		} catch (SQLException ex) {
			m_sErrorMessage.add("Error [1533581944] - Could not delete removed lines - "
					+ ex.getMessage());
			return false;
		}

		return true;
	}

	private boolean update_single_line(ICEntryLine line, Connection conn,
			String sEntryID) {
		String SQL = "UPDATE " + SMTableicentrylines.TableName + " SET "

		+ SMTableicentrylines.bdcost + " = "
		+ line.sCostSTDFormat().replace(",", "") 
		+ ", " + SMTableicentrylines.bdprice + " = " + line.sPriceSTDFormat().replace(",", "")
		+ ", " + SMTableicentrylines.bdqty + " = " + line.sQtySTDFormat().replace(",", "") 
		+ ", " + SMTableicentrylines.lbatchnumber + " = " + sBatchNumber()
		+ ", " + SMTableicentrylines.lentryid + " = " + sEntryID 
		+ ", " + SMTableicentrylines.lentrynumber + " = " + sEntryNumber()
		+ ", " + SMTableicentrylines.llinenumber + " = " + line.sLineNumber() 
		+ ", " + SMTableicentrylines.scomment + " = '" + clsDatabaseFunctions.FormatSQLStatement(line.sComment()) + "'" 
		+ ", " + SMTableicentrylines.sdescription + " = '" + clsDatabaseFunctions.FormatSQLStatement(line.sDescription()) + "'" 
		+ ", " + SMTableicentrylines.sitemnumber + " = '" + clsDatabaseFunctions.FormatSQLStatement(line.sItemNumber().toUpperCase()) + "'" 
		+ ", " + SMTableicentrylines.slocation + " = '" + clsDatabaseFunctions.FormatSQLStatement(line.sLocation()) + "'" 
		+ ", " + SMTableicentrylines.stargetlocation + " = '" + clsDatabaseFunctions.FormatSQLStatement(line.sTargetLocation()) + "'"
		+ ", " + SMTableicentrylines.scategorycode + " = '" + line.sCategoryCode() + "'" 
		+ ", " + SMTableicentrylines.sreceiptnum + " = '" + line.sReceiptNum() + "'" 
		+ ", " + SMTableicentrylines.lcostbucketid + " = " + line.sCostBucketID() 
		+ ", " + SMTableicentrylines.sdistributionacct + " = '" + line.sDistributionAcct() + "'" 
		+ ", " + SMTableicentrylines.scontrolacct + " = '" + line.sControlAcct() + "'" 
		+ ", " + SMTableicentrylines.linvoicelinenumber + " = " + line.sInvoiceLineNumber() 
		+ ", " + SMTableicentrylines.sinvoicenumber + " = '" + line.sInvoiceNumber() 
		+ "'" + " WHERE (" 
		+ "(" + SMTableicentrylines.lid + " = " + line.sId() + ")" 
		+ ")"
		;

		// System.out.println("In " + this.toString() +
		// ".update_single_line - SQL = " + SQL);

		try {
			// System.out.println(this.toString() +
			// " - update_single_line_SQL = " + SQL);
			Statement stmt = conn.createStatement();
			stmt.executeUpdate(SQL);
		} catch (SQLException ex) {
			System.out.println(this.toString()
					+ "Could not complete update line with line id: "
					+ line.sId() + " - " + ex.getMessage() + " - SQL = " + SQL);
			m_sErrorMessage.add("Could not complete update line - SQL error: "
					+ ex.getMessage());
			return false;
		}
		return true;
	}

	private boolean add_single_line(ICEntryLine line, Connection conn,
			String sEntryID) {
		String SQL = "INSERT INTO " + SMTableicentrylines.TableName + " ("
		+ SMTableicentrylines.bdcost + ", "
		+ SMTableicentrylines.bdprice + ", "
		+ SMTableicentrylines.bdqty + ", "
		+ SMTableicentrylines.lbatchnumber + ", "
		+ SMTableicentrylines.lentryid + ", "
		+ SMTableicentrylines.lentrynumber + ", "
		+ SMTableicentrylines.llinenumber + ", "
		+ SMTableicentrylines.scomment + ", "
		+ SMTableicentrylines.sdescription + ", "
		+ SMTableicentrylines.scontrolacct + ", "
		+ SMTableicentrylines.sdistributionacct + ", "
		+ SMTableicentrylines.sitemnumber + ", "
		+ SMTableicentrylines.slocation + ", "
		+ SMTableicentrylines.scategorycode + ", "
		+ SMTableicentrylines.sreceiptnum + ", "
		+ SMTableicentrylines.lcostbucketid + ", "
		+ SMTableicentrylines.stargetlocation + ", "
		+ SMTableicentrylines.linvoicelinenumber + ", "
		+ SMTableicentrylines.sinvoicenumber + ", "
		+ SMTableicentrylines.lreceiptlineid

		+ ") VALUES (" + line.sCostSTDFormat().replace(",", "") 
		+ ", " + line.sPriceSTDFormat().replace(",", "") 
		+ ", " + line.sQtySTDFormat().replace(",", "") 
		+ ", " + sBatchNumber()
		+ ", " + sEntryID 
		+ ", " + sEntryNumber() 
		+ ", " + line.sLineNumber() 
		+ ", '" + clsDatabaseFunctions.FormatSQLStatement(line.sComment()) + "'" 
		+ ", '" + clsDatabaseFunctions.FormatSQLStatement(line.sDescription())+ "'" 
		+ ", '" + line.sControlAcct() + "'"
		+ ", '" + line.sDistributionAcct() + "'" 
		+ ", '" + clsDatabaseFunctions.FormatSQLStatement(line.sItemNumber()) + "'" 
		+ ", '" + line.sLocation() + "'"
		+ ", '" + line.sCategoryCode() + "'" 
		+ ", '" + line.sReceiptNum() + "'" 
		+ ", " + line.sCostBucketID()
		+ ", '" + line.sTargetLocation() + "'" 
		+ ", " + line.sInvoiceLineNumber() 
		+ ", '" + line.sInvoiceNumber() + "'"
		+ ", " + line.sReceiptLineID()
		+ ")"
		;

		try {
			 //System.out.println(this.toString() + " - add_single_line_SQL = "
			 //+ SQL);
			Statement stmt = conn.createStatement();
			stmt.executeUpdate(SQL);
		} catch (SQLException ex) {
			m_sErrorMessage.add("Could not complete add line with SQL:" + SQL + " - SQL error: "
					+ ex.getMessage());
			return false;
		}
		return true;
	}

	public boolean checkIfKeyIsUnique(Connection conn) {

		// check ic transactions for uniqueness here, IF needed:
		return true;
	}

	public boolean checkIfEntryKeyIsUnique(Connection conn) {

		// Make sure there is no unposted entry that matches, IF this is needed:
		return true;
	}

	public String read_out_debug_data() {
		String sResult = "  ** ICEntry read out: ";
		sResult += "\nbatch: " + sBatchNumber();
		sResult += "\nentry: " + sEntryNumber();
		sResult += "\nentry type: " + sEntryType();
		sResult += "\ndoc number: " + sDocNumber();
		sResult += "\nentry desc: " + sEntryDescription();
		sResult += "\nentry date: " + sStdEntryDate();
		sResult += "\nentryid: " + slid();
		sResult += "\nlastline: " + sLastLine();
		sResult += "\nentry amt: " + dEntryTotal();
		sResult += "\nbatch type: " + sBatchType();

		for (int i = 0; i < LineArray.size(); i++) {
			sResult += "\n  ** LINE " + i + ":";
			ICEntryLine linetest = (ICEntryLine) LineArray.get(i);
			sResult += "\nbatch: " + linetest.sBatchNumber();
			sResult += "\nentry: " + linetest.sEntryNumber();
			sResult += "\nlineno: " + linetest.sLineNumber();
			sResult += "\ncost: " + linetest.sCostSTDFormat();
			sResult += "\nprice: " + linetest.sPriceSTDFormat();
			sResult += "\nqty: " + linetest.sQtySTDFormat();
			sResult += "\nitem number: " + linetest.sItemNumber();
			sResult += "\nlocation: " + linetest.sLocation();
			sResult += "\ncomment: " + linetest.sComment();
			sResult += "\ndesc: " + linetest.sDescription();
			sResult += "\nentryid: " + linetest.sEntryId();
			sResult += "\ncontrol acct: " + linetest.sControlAcct();
			sResult += "\ncategory code: " + linetest.sCategoryCode();
			sResult += "\ndistribution acct: " + linetest.sDistributionAcct();
			sResult += "\nreceipt number: " + linetest.sReceiptNum();
			sResult += "\ncostbucketid: " + linetest.sCostBucketID();
			sResult += "\ntargetlocation: " + linetest.sTargetLocation();
			sResult += "\nlineid: " + linetest.sId();
		}
		return sResult;
	}

	public ArrayList<String> getErrorMessage() {
		return m_sErrorMessage;
	}

	public void clearError() {
		m_sErrorMessage.clear();
	}

	public void setErrorMessage(String sMsg) {
		m_sErrorMessage.add(sMsg);
	}

	public int getLineCount() {
		return LineArray.size();
	}
	public String getErrorMessageString(){
		String s = "";
		for (int i = 0; i < m_sErrorMessage.size(); i++){
			s += m_sErrorMessage.get(i) + ".  ";
		}
		return s;
	}
	public BigDecimal dEntryTotal() {

		BigDecimal dTotal = new BigDecimal(0);
		BigDecimal dLineCost = new BigDecimal(0);

		for (int i = 0; i < LineArray.size(); i++) {
			dLineCost = new BigDecimal(LineArray.get(i).sCostSTDFormat()
					.replace(",", ""));
			dTotal = dTotal.add(dLineCost);
		}

		return dTotal;

	}

	public boolean batchIsOpen(Connection conn){

		boolean bResult = false;

		//Make sure the batch is still open:
		try {
			String SQL = "SELECT"
				+ " " + ICEntryBatch.ibatchstatus
				+ " FROM " + ICEntryBatch.TableName
				+ " WHERE ("
				+ ICEntryBatch.lbatchnumber + " = " + m_lBatchNumber
				+ ")"
				;
			ResultSet rs = clsDatabaseFunctions.openResultSet(
					SQL, 
					conn
			);
			if (rs.next()){
				int iStatus = rs.getInt(ICEntryBatch.ibatchstatus);
				if (
						(iStatus == SMBatchStatuses.ENTERED)
						|| (iStatus == SMBatchStatuses.IMPORTED)
				){
					bResult = true;
				}else{
					m_sErrorMessage.add("This batch (" + m_lBatchNumber + ") is no longer open.");
				}
			}else{
				m_sErrorMessage.add("Could not read batch to see if it is still open.");
			}
			rs.close();
		}catch (SQLException e){
			System.out.println("Error in " + this.toString() + " - " + e.getMessage());
			m_sErrorMessage.add("Error checking if batch is open - " + e.getMessage());
		}
		return bResult;
	}

	public String getQueryString() {

		String sQueryString = "";
		sQueryString += ParamEntryID + "="
		+ clsServletUtilities.URLEncode(Long.toString(m_lid));
		sQueryString += "&" + ParamBatchNumber + "="
		+ clsServletUtilities.URLEncode(Long.toString(m_lBatchNumber));
		sQueryString += "&" + ParamEntryNumber + "="
		+ clsServletUtilities.URLEncode(Long.toString(m_lEntryNumber));
		sQueryString += "&" + ParamEntryType + "="
		+ clsServletUtilities.URLEncode(m_sEntryType);
		sQueryString += "&" + ParamDocNumber + "="
		+ clsServletUtilities.URLEncode(m_sDocNumber);
		sQueryString += "&" + ParamEntryDescription + "="
		+ clsServletUtilities.URLEncode(m_sEntryDescription);
		sQueryString += "&" + ParamEntryDate + "="
		+ clsServletUtilities.URLEncode(m_sEntryDate);
		sQueryString += "&"
			+ ParamEntryAmount
			+ "="
			+ clsServletUtilities.URLEncode(clsManageBigDecimals
					.BigDecimalTo2DecimalSTDFormat(dEntryTotal()));
		sQueryString += "&" + ParamNumberOfLines + "="
		+ clsServletUtilities.URLEncode(Long.toString(m_lNumberOfLines));
		sQueryString += "&" + ParamBatchType + "="
		+ clsServletUtilities.URLEncode(m_sBatchType);

		for (int i = 0; i < LineArray.size(); i++) {
			sQueryString += "&" + ICEntryLine.ParamLineComment
			+ clsStringFunctions.PadLeft(Integer.toString(i), "0", 6) + "="
			+ clsServletUtilities.URLEncode(LineArray.get(i).sComment());
			sQueryString += "&"
				+ ICEntryLine.ParamLineControlAccount
				+ clsStringFunctions.PadLeft(Integer.toString(i), "0", 6)
				+ "="
				+ clsServletUtilities.URLEncode(LineArray.get(i)
						.sControlAcct());
			sQueryString += "&"
				+ ICEntryLine.ParamLineCost
				+ clsStringFunctions.PadLeft(Integer.toString(i), "0", 6)
				+ "="
				+ clsServletUtilities.URLEncode(LineArray.get(i)
						.sCostSTDFormat());
			sQueryString += "&"
				+ ICEntryLine.ParamLineDesc
				+ clsStringFunctions.PadLeft(Integer.toString(i), "0", 6)
				+ "="
				+ clsServletUtilities.URLEncode(LineArray.get(i)
						.sDescription());
			sQueryString += "&"
				+ ICEntryLine.ParamLineDistributionAccount
				+ clsStringFunctions.PadLeft(Integer.toString(i), "0", 6)
				+ "="
				+ clsServletUtilities.URLEncode(LineArray.get(i)
						.sDescription());
			sQueryString += "&"
				+ ICEntryLine.ParamLineDistributionAccount
				+ clsStringFunctions.PadLeft(Integer.toString(i), "0", 6)
				+ "="
				+ clsServletUtilities.URLEncode(LineArray.get(i)
						.sDistributionAcct());
			sQueryString += "&" + ICEntryLine.ParamLineEntryID
			+ clsStringFunctions.PadLeft(Integer.toString(i), "0", 6) + "="
			+ clsServletUtilities.URLEncode(LineArray.get(i).sEntryId());
			sQueryString += "&" + ICEntryLine.ParamLineID
			+ clsStringFunctions.PadLeft(Integer.toString(i), "0", 6) + "="
			+ clsServletUtilities.URLEncode(LineArray.get(i).sId());
			sQueryString += "&"
				+ ICEntryLine.ParamLineItemNumber
				+ clsStringFunctions.PadLeft(Integer.toString(i), "0", 6)
				+ "="
				+ clsServletUtilities
				.URLEncode(LineArray.get(i).sItemNumber());
			sQueryString += "&" + ICEntryLine.ParamLineLocation
			+ clsStringFunctions.PadLeft(Integer.toString(i), "0", 6) + "="
			+ clsServletUtilities.URLEncode(LineArray.get(i).sLocation());
			sQueryString += "&"
				+ ICEntryLine.ParamLineTargetLocation
				+ clsStringFunctions.PadLeft(Integer.toString(i), "0", 6)
				+ "="
				+ clsServletUtilities.URLEncode(LineArray.get(i)
						.sTargetLocation());
			sQueryString += "&"
				+ ICEntryLine.ParamLinePrice
				+ clsStringFunctions.PadLeft(Integer.toString(i), "0", 6)
				+ "="
				+ clsServletUtilities.URLEncode(LineArray.get(i)
						.sPriceSTDFormat());
			sQueryString += "&"
				+ ICEntryLine.ParamLineQty
				+ clsStringFunctions.PadLeft(Integer.toString(i), "0", 6)
				+ "="
				+ clsServletUtilities.URLEncode(LineArray.get(i)
						.sQtySTDFormat());
			sQueryString += "&"
				+ ICEntryLine.ParamLineCategory
				+ clsStringFunctions.PadLeft(Integer.toString(i), "0", 6)
				+ "="
				+ clsServletUtilities.URLEncode(LineArray.get(i)
						.sCategoryCode());
			sQueryString += "&"
				+ ICEntryLine.ParamLineReceiptNum
				+ clsStringFunctions.PadLeft(Integer.toString(i), "0", 6)
				+ "="
				+ clsServletUtilities
				.URLEncode(LineArray.get(i).sReceiptNum());
			sQueryString += "&"
				+ ICEntryLine.ParamLineCostBucketID
				+ clsStringFunctions.PadLeft(Integer.toString(i), "0", 6)
				+ "="
				+ clsServletUtilities.URLEncode(LineArray.get(i)
						.sCostBucketID());
		}

		return sQueryString;
	}

}