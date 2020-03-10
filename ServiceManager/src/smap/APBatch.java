package smap;

import java.io.PrintWriter;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import SMClasses.SMBatchStatuses;
import SMClasses.SMLogEntry;
import SMClasses.SMModuleTypes;
import SMClasses.SMOption;
import SMDataDefinition.SMTableapaccountsets;
import SMDataDefinition.SMTableapbatchentries;
import SMDataDefinition.SMTableapbatchentrylines;
import SMDataDefinition.SMTableapbatches;
import SMDataDefinition.SMTableapchecks;
import SMDataDefinition.SMTableapmatchinglines;
import SMDataDefinition.SMTableapoptions;
import SMDataDefinition.SMTableaptransactionlines;
import SMDataDefinition.SMTableaptransactions;
import SMDataDefinition.SMTableapvendorstatistics;
import SMDataDefinition.SMTablebkaccountentries;
import SMDataDefinition.SMTableglexportdetails;
import SMDataDefinition.SMTableicporeceiptlines;
import SMDataDefinition.SMTableicvendors;
import SMDataDefinition.SMTableusers;
import ServletUtilities.clsDBServerTime;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsDateAndTimeConversions;
import ServletUtilities.clsManageBigDecimals;
import ServletUtilities.clsManageRequestParameters;
import ServletUtilities.clsStringFunctions;
import ServletUtilities.clsValidateFormFields;
import smcontrolpanel.SMUtilities;
import smgl.GLTransactionBatch;
import smgl.SMGLExport;

public class APBatch {

	public final static String OBJECT_NAME = "AP Batch";
	public final static String INVALID_ENTRY_ERROR_STARTING_TAG = "STARTINVALIDENTRYERROR";
	public final static String INVALID_ENTRY_ERROR_ENDING_TAG = "ENDINVALIDENTRYERROR";
	
	private String m_sbatchnumber;
	private String m_sbatchdate;
	private String m_sbatchstatus;
	private String m_sbatchdescription;
	private String m_sbatchtype;
	private String m_slasteditdate;
	private String m_sbatchlastentry;
	private String m_lcreatedby;
	private String m_llasteditedby;
	private String m_sdatpostdate;
	private static final boolean bDebugMode = false;

	private ArrayList<APBatchEntry>m_arrBatchEntries;

	public APBatch(
			String sBatchNumber
			) 
	{
		initializeVariables();
		setsbatchnumber(sBatchNumber);
	}
	public APBatch(HttpServletRequest req){
		//Read the batch fields from a servlet request:
		initializeVariables();
		
		setsbatchdate(clsManageRequestParameters.get_Request_Parameter(SMTableapbatches.datbatchdate, req).replace("&quot;", "\""));
		if(getsbatchdate().compareToIgnoreCase("") == 0){
			setsbatchdate(clsDateAndTimeConversions.now(SMUtilities.DATE_FORMAT_FOR_DISPLAY));
		}
		setsbatchdescription(clsManageRequestParameters.get_Request_Parameter(SMTableapbatches.sbatchdescription, req).replace("&quot;", "\""));
		setsbatchlastentry(clsManageRequestParameters.get_Request_Parameter(SMTableapbatches.lbatchlastentry, req).replace("&quot;", "\""));
		if (getsbatchlastentry().compareToIgnoreCase("") == 0){
			setsbatchlastentry("0");
		}
		setsbatchnumber(clsManageRequestParameters.get_Request_Parameter(SMTableapbatches.lbatchnumber, req).replace("&quot;", "\""));
		if(getsbatchnumber().compareToIgnoreCase("") == 0){
			setsbatchnumber("-1");
		}
		setsbatchstatus(clsManageRequestParameters.get_Request_Parameter(SMTableapbatches.ibatchstatus, req).replace("&quot;", "\""));
		if (getsbatchstatus().compareToIgnoreCase("") == 0){
			setsbatchstatus(Integer.toString(SMBatchStatuses.ENTERED));
		}
		setsbatchtype(clsManageRequestParameters.get_Request_Parameter(SMTableapbatches.ibatchtype, req).replace("&quot;", "\""));
		if (getsbatchtype().compareToIgnoreCase("") == 0){
			setsbatchtype(Integer.toString(SMTableapbatches.AP_BATCH_TYPE_INVOICE));
		}
		setlcreatedby(clsManageRequestParameters.get_Request_Parameter(SMTableapbatches.lcreatedby, req).replace("&quot;", "\""));
		setslasteditdate(clsManageRequestParameters.get_Request_Parameter(SMTableapbatches.datlasteditdate, req).replace("&quot;", "\""));
		if(getslasteditdate().compareToIgnoreCase("") == 0){
			setslasteditdate(SMUtilities.EMPTY_DATETIME_VALUE);
		}
		setllasteditedby(clsManageRequestParameters.get_Request_Parameter(SMTableapbatches.llasteditedby, req).replace("&quot;", "\""));
		setsposteddate(clsManageRequestParameters.get_Request_Parameter(SMTableapbatches.datpostdate, req).replace("&quot;", "\""));
		if (getsposteddate().compareToIgnoreCase("") == 0){
			setsposteddate(SMUtilities.EMPTY_DATETIME_VALUE);
		}
	}
	public void save_without_data_transaction (Connection conn, String sUserID, String sUsersFullName, boolean bBatchIsBeingPosted) throws Exception{

		//System.out.println("[1489623830]");
		//See what's in the batch and lines at this point:
		//System.out.println("[1493666897] - batch dump:\n" + dumpData()); 
		try {
			validate_fields(conn, sUserID, bBatchIsBeingPosted);
		} catch (Exception e1) {
			throw new Exception("Error [1489612922] validating batch fields - " + e1.getMessage());
		}
		
		//System.out.println("[1489623831]");
		String SQL = "";
		if (getsbatchnumber().compareToIgnoreCase("-1") == 0){
			//Add a new batch:
			SQL = "INSERT into " + SMTableapbatches.TableName
				+ " (" 
				+ SMTableapbatches.datbatchdate
				+ ", " + SMTableapbatches.datlasteditdate
				+ ", " + SMTableapbatches.datpostdate
				+ ", " + SMTableapbatches.ibatchstatus
				+ ", " + SMTableapbatches.ibatchtype
				+ ", " + SMTableapbatches.lbatchlastentry
				+ ", " + SMTableapbatches.sbatchdescription
				+ ", " + SMTableapbatches.lcreatedby
				+ ", " + SMTableapbatches.llasteditedby
				+ ", " + SMTableapbatches.slasteditedbyfullname
				+ ", " + SMTableapbatches.screatedbyfullname
				+ ")"
				+ " VALUES ("
				+ "'" + getsbatchdateInSQLFormat() + "'"
				+ ", NOW()"
				+ ", '" + getsposteddateInSQLFormat() + "'"
				+ ", " + getsbatchstatus()
				+ ", " + getsbatchtype()
				+ ", " + getsbatchlastentry()
				+ ", '" + clsDatabaseFunctions.FormatSQLStatement(getsbatchdescription()) + "'"
				+ ", " + sUserID
				+ ", " + sUserID
				+ ", '" + sUsersFullName + "'"
				+ ", '" + sUsersFullName + "'"
				+ ")"
			;
		}else{
			SQL = " UPDATE " + SMTableapbatches.TableName + " SET"
			+ " " + SMTableapbatches.datbatchdate + " = '" + getsbatchdateInSQLFormat() + "'"
			+ ", " + SMTableapbatches.datlasteditdate + " = NOW()"
			+ ", " + SMTableapbatches.datpostdate + " = '" + getsposteddateInSQLFormat() + "'"
			+ ", " + SMTableapbatches.ibatchstatus + " = " + getsbatchstatus()
			+ ", " + SMTableapbatches.ibatchtype + " = " + getsbatchtype()
			+ ", " + SMTableapbatches.lbatchlastentry + " = " + getsbatchlastentry()
			+ ", " + SMTableapbatches.sbatchdescription + " = '" + clsDatabaseFunctions.FormatSQLStatement(getsbatchdescription()) + "'"
			+ ", " + SMTableapbatches.llasteditedby + " = " + sUserID + ""
			+ ", " + SMTableapbatches.slasteditedbyfullname + " = '" + sUsersFullName + "'"
			+ " WHERE ("
				+ "(" + SMTableapbatches.lbatchnumber + " = " + getsbatchnumber() + ")"
			+ ")"
			;
		}
		//System.out.println("[1517597656] AP Batch save SQL = '" + SQL + "'.");
		Statement stmt = conn.createStatement();
		try {
			stmt.execute(SQL);
		} catch (Exception e) {
			throw new Exception("Error [1488916082] updating AP batch with SQL: '" + SQL + "' - " + e.getMessage());
		}
		//System.out.println("[1489623833]");
		//If the batch was newly created, get the new batch number:
		if (getsbatchnumber().compareToIgnoreCase("-1") == 0){
			String sSQL = "SELECT last_insert_id()";
			try {
				ResultSet rs = clsDatabaseFunctions.openResultSet(sSQL, conn);
				if (rs.next()) {
					setsbatchnumber(Long.toString(rs.getLong(1)));
				}else {
					setsbatchnumber("0");
				}
				rs.close();
			} catch (SQLException e) {
				throw new Exception("Error [1488916452] Could not get last ID number - " + e.getMessage());
			}
			//If something went wrong, we can't get the last ID:
			if (getsbatchnumber().compareToIgnoreCase("0") == 0){
				throw new Exception("Error [1488916453] Could not get last ID number.");
			}
		}
		//System.out.println("[1489623834]");
		
		//System.out.println("[14896238356]");
		//Finally, save the entries:
		for (int i = 0; i < m_arrBatchEntries.size(); i++){
			APBatchEntry entry = m_arrBatchEntries.get(i);
			try {
				entry.setsbatchnumber(getsbatchnumber());
				//entry.setsentrynumber(Integer.toString(i + 1)); - this is already set in the 'validate fields' function
				entry.save_without_data_transaction(conn, sUserID, this, bBatchIsBeingPosted);
			} catch (Exception e) {
				throw new Exception("Error [1488920514] saving entry number " + entry.getsentrynumber() + " - " + e.getMessage());
			}
		}
		
		//Remove any entries with entry numbers HIGHER than our highest one - this can happen if we remove an entry
		//in the batch - then there would be an entry record that should no longer be in the batch:
		String sHighestValidEntryNumber = "0";
		if (m_arrBatchEntries.size() > 0){
			sHighestValidEntryNumber = m_arrBatchEntries.get(m_arrBatchEntries.size() - 1).getsentrynumber();
		}
		SQL = "DELETE FROM " + SMTableapbatchentries.TableName
			+ " WHERE ("
				+ "(" + SMTableapbatchentries.lbatchnumber + " = " + getsbatchnumber() + ")"
				+ " AND (" + SMTableapbatchentries.lentrynumber + " > " + sHighestValidEntryNumber + ")"
			+ ")"
		;
		
		//System.out.println("[1489623835] - SQL = '" + SQL + "'");
		//System.out.println("[1489623845] - SQLm_arrBatchEntries.size() = '" + m_arrBatchEntries.size() + "'");
		
		stmt = conn.createStatement();
		try {
			stmt.execute(SQL);
		} catch (Exception e) {
			throw new Exception("Error [1489528988] removing discarded AP batch entries with SQL: '" + SQL + "' - " + e.getMessage());
		}
		
		//Now delete any entry lines that are orphaned:
		SQL = "DELETE APBATCHLINES.* FROM " + SMTableapbatchentrylines.TableName + " APBATCHLINES"
			+ " LEFT JOIN " + SMTableapbatchentries.TableName + " APBATCHENTRIES"
			+ " ON (APBATCHLINES." + SMTableapbatchentrylines.lbatchnumber + " = APBATCHENTRIES." + SMTableapbatchentries.lbatchnumber + ")"
			+ " AND (APBATCHLINES." + SMTableapbatchentrylines.lentrynumber + " = APBATCHENTRIES." + SMTableapbatchentries.lentrynumber + ")"
			+ " WHERE (APBATCHENTRIES." + SMTableapbatchentries.lentrynumber + " IS NULL)"	
		;
		
		stmt = conn.createStatement();
		try {
			stmt.execute(SQL);
		} catch (Exception e) {
			throw new Exception("Error [1501086274] removing orphaned AP batch entry lines with SQL: '" + SQL + "' - " + e.getMessage());
		}
		//System.out.println("[148962383567]");
		return;
	}

	public void save_with_data_transaction (ServletContext context, String sDBID, String sUserID, String sUsersFullName, boolean bBatchIsBeingPosted) throws Exception{

		Connection conn = clsDatabaseFunctions.getConnectionWithException(
				context, 
				sDBID, 
				"MySQL", 
				SMUtilities.getFullClassName(this.toString()) + ".save_with_data_transaction - user: '" + sUserID + "'");

		if(!clsDatabaseFunctions.start_data_transaction(conn)){
			clsDatabaseFunctions.freeConnection(context, conn, "[1546998948]");
			throw new Exception("Error [1488918751] could not get data connection.");
		}

		try {
			save_without_data_transaction(conn, sUserID, sUsersFullName, bBatchIsBeingPosted);
		} catch (Exception e) {
			clsDatabaseFunctions.rollback_data_transaction(conn);
			clsDatabaseFunctions.freeConnection(context, conn, "[1546998949]");
			throw new Exception("Error [1488918752] saving - " + e.getMessage());
		}
		clsDatabaseFunctions.commit_data_transaction(conn);
		clsDatabaseFunctions.freeConnection(context, conn, "[1546998950]");
		return;
	}
	private void validate_fields(Connection conn, String sUserID, boolean bBatchIsBeingPosted) throws Exception{
		
		String sResult = "";
		try {
			m_sbatchnumber = clsValidateFormFields.validateLongIntegerField(m_sbatchnumber, "Batchnumber", -1L, clsValidateFormFields.MAX_LONG_VALUE);
		} catch (Exception e) {
			sResult += "  " + e.getMessage() + ".";
		}
		try {
			m_sbatchdate = clsValidateFormFields.validateStandardDateField(m_sbatchdate, "Batch date", false);
		} catch (Exception e) {
			sResult += "  " + e.getMessage() + ".";
		}
		
		try {
			m_sbatchstatus = clsValidateFormFields.validateIntegerField(
				m_sbatchstatus, 
				"Batch status", 
				SMBatchStatuses.ENTERED, 
				SMBatchStatuses.POSTED);
		} catch (Exception e) {
			sResult += "  " + e.getMessage() + ".";
		}
		
		try {
			m_sbatchdescription = clsValidateFormFields.validateStringField(m_sbatchdescription, SMTableapbatches.sBatchDescriptionLength, "Description", false);
		} catch (Exception e) {
			sResult += "  " + e.getMessage() + ".";
		}
		try {
			m_sbatchtype = clsValidateFormFields.validateIntegerField(
				m_sbatchtype, 
				"Batch type", 
				SMTableapbatches.AP_BATCH_TYPE_INVOICE, 
				SMTableapbatches.AP_BATCH_TYPE_ADJUSTMENT);
		} catch (Exception e) {
			sResult += "  " + e.getMessage() + ".";
		}
		try {
			m_slasteditdate = clsValidateFormFields.validateDateTimeField(
				m_slasteditdate, 
				"Last edited date", 
				SMUtilities.DATETIME_FORMAT_FOR_DISPLAY,
				true);
		} catch (Exception e) {
			sResult += "  " + e.getMessage() + ".";
		}
		m_sbatchlastentry = Integer.toString(m_arrBatchEntries.size());
		try {
			m_sbatchlastentry = clsValidateFormFields.validateLongIntegerField(
				m_sbatchlastentry, 
				"Last entry", 
				0, 
				clsValidateFormFields.MAX_LONG_VALUE);
		} catch (Exception e) {
			sResult += "  " + e.getMessage() + ".";
		}
		/*
		try {
			m_lcreatedby = clsValidateFormFields.validateIntegerField(m_lcreatedby,"Created by", 0, Integer.MAX_VALUE);
		} catch (Exception e) {
			sResult += "  " + e.getMessage() + ".";
		}
		try {
			m_llasteditedby = clsValidateFormFields.validateIntegerField(
				m_llasteditedby, 
				"Last updated by", 
				0,
				Integer.MAX_VALUE);
		} catch (Exception e) {
			sResult += "  " + e.getMessage() + ".";
		}
		*/
		try {
			m_sdatpostdate = clsValidateFormFields.validateDateTimeField(
				m_sdatpostdate, 
				"Posting date", 
				SMUtilities.DATETIME_FORMAT_FOR_DISPLAY, 
				true);
		} catch (Exception e) {
			sResult += "  " + e.getMessage() + ".";
		}

		//Insure that the batch date is within the posting period:
        SMOption opt = new SMOption();
        if (!opt.load(conn)){
        	sResult += "  " + "Error [1551456648] loading SM Options to check batch date range - " + opt.getErrorMessage() + ".";
        }else{
            try {
    			opt.checkDateForPosting(m_sbatchdate, "Batch Date", conn, sUserID);
    		} catch (Exception e) {
    			sResult += "  " + "Error [1551456649]  - " + e.getMessage() + ".";
    		}
        }
		
		//Validate the entries:
		//System.out.println("[1490382128] m_arrBatchEntries.size() = " + m_arrBatchEntries.size());
		for (int i = 0; i < m_arrBatchEntries.size(); i++){
			APBatchEntry entry = m_arrBatchEntries.get(i);
			entry.setsentrynumber(Integer.toString(i + 1));
			try {
				entry.validate_fields(conn, sUserID, this, bBatchIsBeingPosted);
			} catch (Exception e) {
				sResult += "  In entry " + Integer.toString(i + 1) + " - " + e.getMessage() + ".";
			}
		}
		
		if (sResult.compareToIgnoreCase("") != 0){
			//We'll surround the actual error message with tags so we can strip it out at the end:
			throw new Exception(INVALID_ENTRY_ERROR_STARTING_TAG + sResult + INVALID_ENTRY_ERROR_ENDING_TAG);
		}
	}

	public void addBatchEntry (APBatchEntry entry){
		//entry.setsentrynumber(Integer.toString(m_arrBatchEntries.size() + 1));
		//entry.setsbatchnumber(getsbatchnumber());
		m_arrBatchEntries.add(entry);
	}
	public APBatchEntry getEntryByEntryNumber(String sEntryNumber){
		for (int i = 0; i < m_arrBatchEntries.size(); i++){
			if (m_arrBatchEntries.get(i).getsentrynumber().compareToIgnoreCase(sEntryNumber) == 0){
				return m_arrBatchEntries.get(i);
			}
		}
		return null;
	}
	public String updateBatchEntry (
		APBatchEntry entry,
		ServletContext context,
		String sDBID, 
		String sUserID,
		String sUserFullName
		) throws Exception{
		//First, load this batch:
		try {
			load(context, sDBID, sUserID);
		} catch (Exception e) {
			throw new Exception("Error [1489525942] loading batch to update entry - " + e.getMessage());
		}
		
		//If the entry number is -1, it just needs to be added:
		//System.out.println("[1489527262] entry.getsentrynumber() = '" + entry.getsentrynumber() + "'");
		
		//Save this entry:
		if (entry.getsentrynumber().compareToIgnoreCase("-1") == 0){
			entry.setsentrynumber(Integer.toString(Integer.parseInt(getsbatchlastentry()) + 1));
		}
		Connection conn;
		
		try {
			conn = clsDatabaseFunctions.getConnectionWithException(context, sDBID, "MySQL", this.toString() + ".updateBatchEntry - user: " + sUserID + " - " );
		} catch (Exception e1) {
			throw new Exception(e1.getMessage());
		}
		
		try {
			clsDatabaseFunctions.start_data_transaction(conn);
		} catch (Exception e3) {
			clsDatabaseFunctions.freeConnection(context, conn, "[1546998951]");
			throw new Exception(e3.getMessage());
		}
		try {
			entry.save_without_data_transaction(conn, sUserID, this, false);
		} catch (Exception e1) {
			clsDatabaseFunctions.rollback_data_transaction(conn);
			clsDatabaseFunctions.freeConnection(context, conn, "[1546998952]");
			throw new Exception(e1.getMessage());
		}
		
		//System.out.println("[1494863200]\n" + entry.dumpData());
		
		//Now that the entry has been saved, re-load the batch to get the correct entry count, so we can update it:
		try {
			loadBatch(conn);
		} catch (Exception e) {
			clsDatabaseFunctions.rollback_data_transaction(conn);
			clsDatabaseFunctions.freeConnection(context, conn, "[1546998953]");
			throw new Exception("Error [1520615104] re-loading batch - " + e.getMessage());
		}
		
		String SQL = "UPDATE " + SMTableapbatches.TableName
			+ " SET " + SMTableapbatches.datlasteditdate + " = NOW()"
			+ ", " + SMTableapbatches.lbatchlastentry + " = " + m_arrBatchEntries.size()
			+ ", " + SMTableapbatches.llasteditedby + " = " + sUserID + ""
			+ ", " + SMTableapbatches.slasteditedbyfullname + " = '" + sUserFullName + "'"
			+ " WHERE ("
				+ "(" + SMTableapbatches.lbatchnumber + " = " + getsbatchnumber() + ")"
			+ ")"
		;
		try {
			Statement stmt = conn.createStatement();
			stmt.execute(SQL);
		} catch (Exception e2) {
			clsDatabaseFunctions.rollback_data_transaction(conn);
			clsDatabaseFunctions.freeConnection(context, conn, "[1546998954]");
			throw new Exception(e2.getMessage());
		}
		
		try {
			clsDatabaseFunctions.commit_data_transaction(conn);
		} catch (Exception e1) {
			clsDatabaseFunctions.rollback_data_transaction(conn);
			clsDatabaseFunctions.freeConnection(context, conn, "[1546998955]");
			throw new Exception(e1.getMessage());
		}
		
		clsDatabaseFunctions.freeConnection(context, conn, "[1546998956]");

		return entry.getslid();
	}
	
	public void load(ServletContext context, String sDBID, String sUserID) throws Exception{
		
		Connection conn = null;
		try {
			conn = clsDatabaseFunctions.getConnectionWithException(
				context, 
				sDBID, 
				"MySQL", 
				SMUtilities.getFullClassName(this.toString() + ".load - userID: '" + sUserID + "'"));
		} catch (Exception e) {
			throw new Exception("Error [1489175148] getting connection to load batch number " + getsbatchnumber() + " - " + e.getMessage());
		}
		
		try {
			loadBatch(conn);
		} catch (Exception e) {
			throw new Exception("Error [1489175149] loading batch number " + getsbatchnumber() + " - " + e.getMessage());
		}
		
		clsDatabaseFunctions.freeConnection(context, conn, "[1546998941]");
		
	}
	
	public void loadBatch(Connection conn) throws Exception{
		String SQL = "SELECT * FROM " + SMTableapbatches.TableName
			+ " WHERE ("
				+ "(" + SMTableapbatches.lbatchnumber + " = " + getsbatchnumber() + ")"
			+ ")"
		;
		ResultSet rs = null;
		try {
			rs = clsDatabaseFunctions.openResultSet(SQL, conn);
			if (rs.next()){
				setsbatchdate(clsDateAndTimeConversions.resultsetDateTimeStringToFormattedString(
					rs.getString(SMTableapbatches.datbatchdate), SMUtilities.DATE_FORMAT_FOR_DISPLAY, SMUtilities.EMPTY_DATE_VALUE));
				setsbatchdescription(rs.getString(SMTableapbatches.sbatchdescription));
				setsbatchlastentry(Long.toString(rs.getLong(SMTableapbatches.lbatchlastentry)));
				setsbatchstatus(Integer.toString(rs.getInt(SMTableapbatches.ibatchstatus)));
				setsbatchtype(Integer.toString(rs.getInt(SMTableapbatches.ibatchtype)));
				setlcreatedby(rs.getString(SMTableapbatches.lcreatedby));
				setslasteditdate(clsDateAndTimeConversions.resultsetDateTimeStringToFormattedString(
					rs.getString(SMTableapbatches.datlasteditdate), SMUtilities.DATETIME_FORMAT_FOR_DISPLAY, SMUtilities.EMPTY_DATETIME_VALUE));
				setllasteditedby(rs.getString(SMTableapbatches.llasteditedby));
				setsposteddate(clsDateAndTimeConversions.resultsetDateTimeStringToFormattedString(
					rs.getString(SMTableapbatches.datpostdate), SMUtilities.DATETIME_FORMAT_FOR_DISPLAY, SMUtilities.EMPTY_DATETIME_VALUE));
			}else{
				rs.close();
				throw new Exception("Error [1489175150] - No AP batch found with batch number " + getsbatchnumber() + ".");
			}
			rs.close();
		} catch (Exception e1) {
			rs.close();
			throw new Exception("Error [1489248898] loading batch - " + e1.getMessage());
		}
		
		try {
			loadEntries(conn);
		} catch (Exception e) {
			throw new Exception(e.getMessage());
		}

	}
	/*
	public void loadBatchNew(Connection conn) throws Exception{
		String SQL = "";
		ResultSet rs = null;
		long lLastEntryID = 0;
		int iRecordCounter = 0;
		APBatchEntry entry = null;
		
		//Before we start to populate the batch, we have to empty the entry array:
		m_arrBatchEntries.clear();
		//Initialize the batch:
		String sTempBatchNumber = getsbatchnumber();
		initializeVariables();
		setsbatchnumber(sTempBatchNumber);
		
		SQL = "SELECT * FROM " 
			+ SMTableapbatchentrylines.TableName
			+ " LEFT JOIN " + SMTableapbatchentries.TableName
			+ " ON (" 
				+ "(" + SMTableapbatchentrylines.TableName + "." + SMTableapbatchentrylines.lbatchnumber + "=" 
				+  SMTableapbatchentries.TableName + "." + SMTableapbatchentries.lbatchnumber + ")"
				+ " AND ("
				+ SMTableapbatchentrylines.TableName + "." + SMTableapbatchentrylines.lentrynumber + "=" 
				+  SMTableapbatchentries.TableName + "." + SMTableapbatchentries.lentrynumber + ")"
			+ ")"
			+ " LEFT JOIN " + SMTableapbatches.TableName
			+ " ON " + SMTableapbatchentries.TableName + "." + SMTableapbatchentries.lbatchnumber + "="
				+ SMTableapbatches.TableName + "." + SMTableapbatches.lbatchnumber
			+ " WHERE ("
				+ "(" + SMTableapbatchentrylines.TableName + "." + SMTableapbatchentrylines.lbatchnumber + " = " + getsbatchnumber() + ")"
			+ ") ORDER BY " + SMTableapbatchentrylines.TableName + "." + SMTableapbatchentrylines.lentrynumber
			+ ", " + SMTableapbatchentrylines.TableName + "." + SMTableapbatchentrylines.llinenumber;

		try {
			rs = clsDatabaseFunctions.openResultSet(SQL, conn);
			while (rs.next()){
				iRecordCounter++;
				//If this is the first record, populate the batch info:
				if (iRecordCounter == 1){
					setsbatchdate(clsDateAndTimeConversions.resultsetDateTimeStringToFormattedString(
						rs.getString(SMTableapbatches.TableName + "." + SMTableapbatches.datbatchdate), SMUtilities.DATE_FORMAT_FOR_DISPLAY, SMUtilities.EMPTY_DATE_VALUE));
					setsbatchdescription(rs.getString(SMTableapbatches.TableName + "." + SMTableapbatches.sbatchdescription));
					setsbatchlastentry(Long.toString(rs.getLong(SMTableapbatches.TableName + "." + SMTableapbatches.lbatchlastentry)));
					setsbatchstatus(Integer.toString(rs.getInt(SMTableapbatches.TableName + "." + SMTableapbatches.ibatchstatus)));
					setsbatchtype(Integer.toString(rs.getInt(SMTableapbatches.TableName + "." + SMTableapbatches.ibatchtype)));
					setlcreatedby(rs.getString(SMTableapbatches.TableName + "." + SMTableapbatches.lcreatedby));
					setslasteditdate(clsDateAndTimeConversions.resultsetDateTimeStringToFormattedString(
						rs.getString(SMTableapbatches.TableName + "." + SMTableapbatches.datlasteditdate), SMUtilities.DATETIME_FORMAT_FOR_DISPLAY, SMUtilities.EMPTY_DATETIME_VALUE));
					setllasteditedby(rs.getString(SMTableapbatches.TableName + "." + SMTableapbatches.llasteditedby));
					setsposteddate(clsDateAndTimeConversions.resultsetDateTimeStringToFormattedString(
						rs.getString(SMTableapbatches.TableName + "." + SMTableapbatches.datpostdate), SMUtilities.DATETIME_FORMAT_FOR_DISPLAY, SMUtilities.EMPTY_DATETIME_VALUE));
				}
				
				//If this is a new entry, populate the entry info:
				if ((lLastEntryID != rs.getLong(SMTableapbatchentries.TableName + "." + SMTableapbatchentries.lid)) && (lLastEntryID != 0L)){
					//Add the current entry:
					addBatchEntry(entry);
					entry = null;
				}
					
				//If the entry is null, load it:
				if (entry == null){
					//Populate the next entry:
					entry = new APBatchEntry();
					entry.setsapplytoinvoicenumber(rs.getString(SMTableapbatchentries.TableName + "." + SMTableapbatchentries.sapplytoinvoicenumber));
					entry.setsbatchnumber(Long.toString(rs.getLong(SMTableapbatchentries.TableName + "." + SMTableapbatchentries.lbatchnumber)));
					entry.setscontrolacct(rs.getString(SMTableapbatchentries.TableName + "." + SMTableapbatchentries.scontrolacct));
					entry.setschecknumber(rs.getString(SMTableapbatchentries.TableName + "." + SMTableapbatchentries.schecknumber));
					entry.setsdatdiscount(clsDateAndTimeConversions.resultsetDateStringToFormattedString(
						rs.getString(SMTableapbatchentries.TableName + "." + SMTableapbatchentries.datdiscount), SMUtilities.DATE_FORMAT_FOR_DISPLAY, SMUtilities.EMPTY_DATE_VALUE));
					entry.setsdatdocdate(clsDateAndTimeConversions.resultsetDateStringToFormattedString(
						rs.getString(SMTableapbatchentries.TableName + "." + SMTableapbatchentries.datdocdate), SMUtilities.DATE_FORMAT_FOR_DISPLAY, SMUtilities.EMPTY_DATE_VALUE));
					entry.setsdatduedate(clsDateAndTimeConversions.resultsetDateStringToFormattedString(
						rs.getString(SMTableapbatchentries.TableName + "." + SMTableapbatchentries.datduedate), SMUtilities.DATE_FORMAT_FOR_DISPLAY, SMUtilities.EMPTY_DATE_VALUE));
					entry.setsdatentrydate(clsDateAndTimeConversions.resultsetDateStringToFormattedString(
						rs.getString(SMTableapbatchentries.TableName + "." + SMTableapbatchentries.datentrydate), SMUtilities.DATE_FORMAT_FOR_DISPLAY, SMUtilities.EMPTY_DATE_VALUE));
					entry.setsdocnumber(rs.getString(SMTableapbatchentries.TableName + "." + SMTableapbatchentries.sdocnumber));
					entry.setsentrytype(Integer.toString(rs.getInt(SMTableapbatchentries.TableName + "." + SMTableapbatchentries.ientrytype)));
					entry.setsentryamount(clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(rs.getBigDecimal(SMTableapbatchentries.TableName + "." + SMTableapbatchentries.bdentryamount)));
					entry.setsentrydescription(rs.getString(SMTableapbatchentries.TableName + "." + SMTableapbatchentries.sentrydescription));
					entry.setsentrynumber(Long.toString(rs.getLong(SMTableapbatchentries.TableName + "." + SMTableapbatchentries.lentrynumber)));
					entry.setsiinvoiceincludestax(Integer.toString(rs.getInt(SMTableapbatchentries.TableName + "." + SMTableapbatchentries.iinvoiceincludestax)));
					entry.setsionhold(Integer.toString(rs.getInt(SMTableapbatchentries.TableName + "." + SMTableapbatchentries.ionhold)));
					entry.setsiprintcheck(Integer.toString(rs.getInt(SMTableapbatchentries.TableName + "." + SMTableapbatchentries.iprintcheck)));
					entry.setsiprintingfinalized(Integer.toString(rs.getInt(SMTableapbatchentries.TableName + "." + SMTableapbatchentries.iprintingfinalized)));
					entry.setslbankid(Long.toString(rs.getLong(SMTableapbatchentries.TableName + "." + SMTableapbatchentries.lbankid)));
					entry.setslid(Long.toString(rs.getLong(SMTableapbatchentries.TableName + "." + SMTableapbatchentries.lid)));
					entry.setslastline(Long.toString(rs.getLong(SMTableapbatchentries.TableName + "." + SMTableapbatchentries.llastline)));
					entry.setslpurchaseordernumber(Long.toString(rs.getLong(SMTableapbatchentries.TableName + "." + SMTableapbatchentries.lpurchaseordernumber)));
					entry.setslsalesordernumber(Long.toString(rs.getLong(SMTableapbatchentries.TableName + "." + SMTableapbatchentries.lsalesordernumber)));
					entry.setsremittoaddressline1(rs.getString(SMTableapbatchentries.TableName + "." + SMTableapbatchentries.sremittoaddressline1));
					entry.setsremittoaddressline2(rs.getString(SMTableapbatchentries.TableName + "." + SMTableapbatchentries.sremittoaddressline2));
					entry.setsremittoaddressline3(rs.getString(SMTableapbatchentries.TableName + "." + SMTableapbatchentries.sremittoaddressline3));
					entry.setsremittoaddressline4(rs.getString(SMTableapbatchentries.TableName + "." + SMTableapbatchentries.sremittoaddressline4));
					entry.setsremittocity(rs.getString(SMTableapbatchentries.TableName + "." + SMTableapbatchentries.sremittocity));
					entry.setsremittocode(rs.getString(SMTableapbatchentries.TableName + "." + SMTableapbatchentries.sremittocode));
					entry.setsremittocountry(rs.getString(SMTableapbatchentries.TableName + "." + SMTableapbatchentries.sremittocountry));
					entry.setsremittoname(rs.getString(SMTableapbatchentries.TableName + "." + SMTableapbatchentries.sremittoname));
					entry.setsremittopostalcode(rs.getString(SMTableapbatchentries.TableName + "." + SMTableapbatchentries.sremittopostalcode));
					entry.setsremittostate(rs.getString(SMTableapbatchentries.TableName + "." + SMTableapbatchentries.sremittostate));
					entry.setsvendoracct(rs.getString(SMTableapbatchentries.TableName + "." + SMTableapbatchentries.svendoracct));
					entry.setsterms(rs.getString(SMTableapbatchentries.TableName + "." + SMTableapbatchentries.sterms));
					entry.setsdiscountamt(clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(rs.getBigDecimal(SMTableapbatchentries.TableName + "." + SMTableapbatchentries.bddiscount)));
					entry.setsvendorname(rs.getString(SMTableapbatchentries.TableName + "." + SMTableapbatchentries.svendorname));
					entry.setstaxjurisdiction(rs.getString(SMTableapbatchentries.TableName + "." + SMTableapbatchentries.staxjurisdiction));
					entry.setsitaxid(Integer.toString(rs.getInt(SMTableapbatchentries.TableName + "." + SMTableapbatchentries.itaxid)));
					entry.setsbdtaxrate(clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(rs.getBigDecimal(SMTableapbatchentries.TableName + "." + SMTableapbatchentries.bdtaxrate)));
					entry.setstaxtype(rs.getString(SMTableapbatchentries.TableName + "." + SMTableapbatchentries.staxtype));
					entry.setsicalculateonpurchaseorsale(Integer.toString(rs.getInt(SMTableapbatchentries.TableName + "." + SMTableapbatchentries.icalculateonpurchaseorsale)));
				}
				
				lLastEntryID = rs.getLong(SMTableapbatchentries.TableName + "." + SMTableapbatchentries.lid);
				
				//Load the line and add it to the entry:
				APBatchEntryLine line = new APBatchEntryLine();
				line.setsiapplytodoctype(Long.toString(rs.getLong(SMTableapbatchentrylines.TableName + "." + SMTableapbatchentrylines.iapplytodoctype)));
				line.setslapplytodocid(Long.toString(rs.getLong(SMTableapbatchentrylines.TableName + "." + SMTableapbatchentrylines.lapplytodocid)));
				line.setsbatchnumber(Long.toString(rs.getLong(SMTableapbatchentrylines.TableName + "." + SMTableapbatchentrylines.lbatchnumber)));
				line.setsentrynumber(Long.toString(rs.getLong(SMTableapbatchentrylines.TableName + "." + SMTableapbatchentrylines.lentrynumber)));
				line.setsbdpayableamt(clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(rs.getBigDecimal(SMTableapbatchentrylines.TableName + "." + SMTableapbatchentrylines.bdpayableamount)));
				line.setslinenumber(Long.toString(rs.getLong(SMTableapbatchentrylines.TableName + "." + SMTableapbatchentrylines.llinenumber)));
				line.setsbdamount(clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(rs.getBigDecimal(SMTableapbatchentrylines.TableName + "." + SMTableapbatchentrylines.bdamount)));
				line.setsbddiscountappliedamt(clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(rs.getBigDecimal(SMTableapbatchentrylines.TableName + "." + SMTableapbatchentrylines.bdapplieddiscountamt)));
				line.setsapplytodocnumber(rs.getString(SMTableapbatchentrylines.TableName + "." + SMTableapbatchentrylines.sapplytodocnumber));
				line.setscomment(rs.getString(SMTableapbatchentrylines.TableName + "." + SMTableapbatchentrylines.scomment));
				line.setsdescription(rs.getString(SMTableapbatchentrylines.TableName + "." + SMTableapbatchentrylines.sdescription));
				line.setsdistributionacct(rs.getString(SMTableapbatchentrylines.TableName + "." + SMTableapbatchentrylines.sdistributionacct));
				line.setsdistributioncodename(rs.getString(SMTableapbatchentrylines.TableName + "." + SMTableapbatchentrylines.sdistributioncodename));
				line.setslid(Long.toString(rs.getLong(SMTableapbatchentrylines.TableName + "." + SMTableapbatchentrylines.lid)));
				line.setslpoheaderid(Long.toString(rs.getLong(SMTableapbatchentrylines.TableName + "." + SMTableapbatchentrylines.lpoheaderid)));
				line.setslreceiptheaderid(Long.toString(rs.getLong(SMTableapbatchentrylines.TableName + "." + SMTableapbatchentrylines.lreceiptheaderid)));
				line.setslporeceiptlineid(Long.toString(rs.getLong(SMTableapbatchentrylines.TableName + "." + SMTableapbatchentrylines.lporeceiptlineid)));
				
				entry.addLine(line);
			}

			rs.close();
		} catch (Exception e1) {
			rs.close();
			throw new Exception("Error [1489248998] loading batch - " + e1.getMessage());
		}
		
		//If we read anything at all, load the last entry:
		if (iRecordCounter > 0){
			addBatchEntry(entry);
		}
			
		return;
	}
	*/
	public void loadEntries(ServletContext context, String sDBID, String sUserID) throws Exception{
		Connection conn;
		try {
			conn = clsDatabaseFunctions.getConnectionWithException(
				context, 
				sDBID, 
				"MySQL", 
				"APBatch.loadEntries - user: " + sUserID);
		} catch (Exception e) {
			throw new Exception("Error [1489357918] getting connection to load AP Batch Entries - " + e.getMessage());
		}
		
		try {
			loadEntries(conn);
		} catch (Exception e) {
			clsDatabaseFunctions.freeConnection(context, conn, "[1546998942]");
			throw new Exception("Error [1489357919] loading AP Batch Entries - " + e.getMessage());
		}
		
		clsDatabaseFunctions.freeConnection(context, conn, "[1546998943]");
	}
	private void loadEntries(Connection conn) throws Exception{
		//Load the entries:
		m_arrBatchEntries.clear();
		String SQL = "SELECT"
			+ " " + SMTableapbatchentries.lid
			+ " FROM " + SMTableapbatchentries.TableName 
			+ " WHERE ("
				+ "(" + SMTableapbatchentries.lbatchnumber + " = " + getsbatchnumber() + ")"
			+ ") ORDER BY " + SMTableapbatchentries.lentrynumber
		;
		ResultSet rs = null;
		try {
			rs = clsDatabaseFunctions.openResultSet(SQL, conn);
			while (rs.next()){
				APBatchEntry entry = new APBatchEntry();
				entry.setslid(Long.toString(rs.getLong(SMTableapbatchentries.lid)));
				entry.load(conn);
				addBatchEntry(entry);
			}
			rs.close();
		} catch (Exception e) {
			rs.close();
			throw new Exception("Error [1489248897] loading batch entries - " + e.getMessage());
		}
	}
    public void flag_as_deleted (
    		String sBatchNumber,
    		ServletContext context, 
    		String sDBID,
    		String sUserFullName
    		) throws Exception{
        
    	    String SQL = "UPDATE "
				+ SMTableapbatches.TableName
				+ " SET "
				+ SMTableapbatches.ibatchstatus + " = " + SMBatchStatuses.DELETED
				+ " WHERE ("
					+ "(" + SMTableapbatches.lbatchnumber + " = " + sBatchNumber + ")"
				+ ")";
    	    try {
				if(!clsDatabaseFunctions.executeSQL(
					SQL, 
					context, 
					sDBID, 
					"MySQL", 
					"APBatch.flag_as_deleted - userID: " + sUserFullName
				)){
					throw new Exception("Error [1489355474] - could not update batch number " + sBatchNumber + " as deleted ");
				}
			} catch (Exception e) {
				throw new Exception("Error [1489355475] - could not update batch number " + sBatchNumber + " as deleted with SQL: " + SQL + " - " + e.getMessage());
			}
 
    }
    public void deleteEntry(
    	String sBatchNumber,
    	String sEntryNumber, 
    	ServletContext context, 
    	String sDBID, 
    	String sUserID,
    	String sUsersFullName
    	) throws Exception{
    	
    	setsbatchnumber(sBatchNumber);
    	try {
			load(context, sDBID, sUserID);
		} catch (Exception e) {
			throw new Exception("Error [1489524343] loading batch number " + sBatchNumber 
				+ " to delete entry nuber " + sEntryNumber + " - " + e.getMessage());
		}
    	removeEntryByEntryNumber(sEntryNumber);
    	try {
			save_with_data_transaction(context, sDBID, sUserID, sUsersFullName, false);
		} catch (Exception e) {
			throw new Exception("Error [1489524344] saving batch number " + sBatchNumber 
				+ " to delete entry number " + sEntryNumber + " - " + e.getMessage());
		}
    	return;
    }
    public void deleteEntryLine(
        	String sBatchNumber,
        	String sEntryNumber,
        	String sEntryLineNumber,
        	ServletContext context, 
        	String sDBID, 
        	String sUserID,
        	String sUserFullName
        	) throws Exception{
        	
        	setsbatchnumber(sBatchNumber);
        	try {
    			load(context, sDBID, sUserID);
    		} catch (Exception e) {
    			throw new Exception("Error [1490308723] loading batch number " + sBatchNumber 
    				+ " to delete line number " + sEntryLineNumber + " on entry number " + sEntryNumber + " - " + e.getMessage());
    		}
        	//System.out.println("[1490316268] - sBatchNumber = " + sBatchNumber + ", entry number = " + sEntryNumber + ", line number = " + sEntryLineNumber);
        	removeLineByLineNumber(sEntryNumber, sEntryLineNumber);
        	try {
    			save_with_data_transaction(context, sDBID, sUserID, sUserFullName, false);
    		} catch (Exception e) {
    			throw new Exception("Error [1490308723] saving batch number " + sBatchNumber 
    				+ " to delete line number " + sEntryLineNumber + " on entry number " + sEntryNumber + " - " + e.getMessage());
    		}
        	return;
        }

    private void removeEntryByEntryNumber(String sEntryNumber) throws Exception{
    	//System.out.println("[1489528400] - sEntryNumber = " + sEntryNumber);
    	for (int i = 0; i < m_arrBatchEntries.size(); i++){
    		if (m_arrBatchEntries.get(i).getsentrynumber().compareToIgnoreCase(sEntryNumber) ==0){
    			m_arrBatchEntries.remove(i);
    			break;
    		}
    	}
    	//Renumber the entries:
    	for (int i = 0; i < m_arrBatchEntries.size(); i++){
    		m_arrBatchEntries.get(i).setsentrynumber(Integer.toString(i + 1));
    	}
    	//System.out.println("[1489528401] - m_arrBatchEntries.size() = " + m_arrBatchEntries.size());
    }
    private void removeLineByLineNumber(String sEntryNumber, String sEntryLineNumber) throws Exception{
    	boolean bEntryWasFound = false;
    	for (int i = 0; i < m_arrBatchEntries.size(); i++){
    		if (m_arrBatchEntries.get(i).getsentrynumber().compareToIgnoreCase(sEntryNumber) == 0){
    			bEntryWasFound = true;
    			m_arrBatchEntries.get(i).removeLineByLineNumber(sEntryLineNumber);
    		}
    	}
    	if (!bEntryWasFound){
    		throw new Exception("Error [1490316394] - entry number '" + sEntryNumber + "' was not found in the batch.");
    	}
    }
    public void post_with_data_transaction (
    		ServletContext context,
    		String sDBID,
    		String sUserID,
    		String sUserFullName,
    		PrintWriter out
    		) throws Exception{

    	Connection conn;
    	try {
    		conn = clsDatabaseFunctions.getConnectionWithException(
    				context, 
    				sDBID, 
    				"MySQL",
    				this.toString() + ".post_with_data_transaction - User: " + sUserID + " - " + sUserFullName
    				);
    	} catch (Exception e) {
    		throw new Exception("Error [1489706314] getting connection - " + e.getMessage());
    	}

    	clsDatabaseFunctions.start_data_transaction(conn);
    	try {
			post_with_connection(conn, sUserID, sUserFullName);
		} catch (Exception e) {
			clsDatabaseFunctions.rollback_data_transaction(conn);
	    	try {
	    		unsetPostingFlag(conn);
	    	} catch (Exception e1) {
	    		clsDatabaseFunctions.freeConnection(context, conn, "[1546998944]");
	    		throw new Exception("Error [1489704632] UNsetting AP posting flag - " + e1.getMessage());
	    	}
	    	clsDatabaseFunctions.freeConnection(context, conn, "[1546998945]");
			throw new Exception("Error [1489771735] posting - " + e.getMessage());
		}
    	clsDatabaseFunctions.commit_data_transaction(conn);
    	try {
    		unsetPostingFlag(conn);
    	} catch (Exception e) {
    		clsDatabaseFunctions.freeConnection(context, conn, "[1546998946]");
    		throw new Exception("Error [1489704332] UNsetting AP posting flag - " + e.getMessage());
    	}
    	clsDatabaseFunctions.freeConnection(context, conn, "[1546998947]");
    	return;
    }    
    
    public void post_with_connection(
    		Connection conn,
    		String sUserID,
    		String sUserFullName
    	) throws Exception{
    	
    	//This connection should already have a data transaction started, so there's
    	//no need to start or commit a data transaction inside this process anywhere:
    	
    	//First, make sure the batch is loaded:
    	try {
			loadBatch(conn);
		} catch (Exception e) {
			throw new Exception("Error [1489778949] loading batch number " + getsbatchnumber() + " - " + e.getMessage());
		}
    	
    	if (getsbatchstatus().compareToIgnoreCase(Integer.toString(SMBatchStatuses.POSTED)) == 0){
    		throw new Exception("This batch is already posted");
    	}
    	if (getsbatchstatus().compareToIgnoreCase(Integer.toString(SMBatchStatuses.DELETED)) == 0){
    		throw new Exception("Deleted batches cannot be posted");
    	}

    	//If this is a PAYMENT batch, and any checks have not been finalized, then we can't post:
		if (getsbatchtype().compareToIgnoreCase(Integer.toString(SMTableapbatches.AP_BATCH_TYPE_PAYMENT)) == 0){
			if (!bAllChecksHaveBeenFinalized()){
				throw new Exception("All of the checks for this batch have NOT been finalized - they must ALL be printed and confirmed as finalized before you can post.");
			}
		}
		
    	SMLogEntry log = new SMLogEntry(conn);

    	try {
    		setPostingFlag(conn, sUserID);
    	} catch (Exception e) {
    		throw new Exception("Error [1489705834] - " + e.getMessage());
    	}

    	try {
    		post_without_data_transaction(conn, sUserID, sUserFullName, log);
    	} catch (Exception e1) {
    		throw new Exception("Error [1489706451] posting - " + e1.getMessage());
    	}
    	
    	//Need to unset posting flag:
    	try {
    		unsetPostingFlag(conn);
    	} catch (Exception e) {
    		throw new Exception("Error [1490793137] UNsetting AP posting flag - " + e.getMessage());
    	}
    }
    
	private void post_without_data_transaction(Connection conn, String sUserID, String sUserFullName, SMLogEntry log) throws Exception{

    	//The connection will ALREADY have a data transaction started....
    	
    	//If there are no entries, don't post
    	if (m_arrBatchEntries.size() == 0){
    		throw new Exception("Error [1489707586] - batch has no entries in it and can't be posted.");
    	}
    	
    	//Check all of the entries first to make sure they can be posted:
    	try {
			checkBatchEntries(log, sUserID, conn);
		} catch (Exception e1) {
			throw new Exception("Error [1489707495] - " + e1.getMessage());
		}
    	
    	//Next, create transactions for all of the entries:
    	try {
			createEntryTransactions(log, sUserID, conn);
		} catch (Exception e) {
			throw new Exception("Error [1489708211] creating entry transactions - " + e.getMessage());
		}

    	
    	//This array will carry a list of the apmatchingline IDs which were created by the automatic application of any eligible prepays.
    	//Then this array gets passed, later on, to the 'create GL batch' function which needs it to create the necessary
    	//GL transactions for the auto-applied prepays:
    	ArrayList<Long>arrMatchingLineIDsFromPrepays = new ArrayList<Long>(0);
    	try {
    		arrMatchingLineIDsFromPrepays = autoApplyPrePays(conn);
		} catch (Exception e1) {
			throw new Exception("Error [1494442649] auto-applying Prepayments - " + e1.getMessage());
		}
    	
    	//Update the batch:
    	setsbatchstatus(Integer.toString(SMBatchStatuses.POSTED));
    	clsDBServerTime dt = new clsDBServerTime(conn);
    	setsposteddate(dt.getCurrentDateTimeInSelectedFormat(SMUtilities.DATETIME_FORMAT_FOR_DISPLAY));
    	//System.out.println("[1517597655] this.getsposteddate() = " + this.getsposteddate());
    	
    	if (bDebugMode){    	
    		log.writeEntry(
    			sUserID,
    			SMLogEntry.LOG_OPERATION_APBATCHPOST,
        		"In post_without_data_transaction Batch #:" + getsbatchnumber(), 
        		"Going into save_without_data_transaction",
        		"[1489711360]"
    		);
    	}
    	try {
			save_without_data_transaction(conn, sUserID, sUserFullName, true);
		} catch (Exception e) {
			throw new Exception("Error [1489711363] updating batch - " + e.getMessage());
		}
 
    	if (bDebugMode){
    		log.writeEntry(
    			sUserID,
    			SMLogEntry.LOG_OPERATION_APBATCHPOST,
        		"In post_without_data_transaction Batch #:" + getsbatchnumber(), 
        		"After successful save_without_data_transaction",
        		"[1489711361]"
    		);
    	}
    	
    	//Use this to get and save the 'GL Feed' integer:
    	int iFeedGLStatus = 0;
		APOptions apopt = new APOptions();
		if(!apopt.load(conn)){
			throw new Exception("Error [1507061354] reading AP Options table - " + apopt.getErrorMessageString());
		}
		
		try {
			iFeedGLStatus = Integer.parseInt(apopt.getifeedgl());
		} catch (Exception e1) {
			throw new Exception("Error [1556895837] parsing Feed GL status of '" + apopt.getifeedgl() + "'.");
		}
    	
    	//Handle some clean-up processing here:
    	//If it's a payment batch, then update the 'transaction ID' on any related checks, and also update the 'check numbers' on the payment entries:
    	if (getsbatchtype().compareToIgnoreCase(Integer.toString(SMTableapbatches.AP_BATCH_TYPE_PAYMENT)) == 0){
    		
    		try {
				updateTransactionIDonChecks(conn);
			} catch (Exception e) {
				throw new Exception(e.getMessage());
			}
       		
    		try {
				updateCheckNumberonTransactions(conn);
			} catch (Exception e) {
				throw new Exception(e.getMessage());
			}
    	}
    	
    	//If it's a CHECK REVERSAL batch, then flag the apchecks as voided:
    	if (getsbatchtype().compareToIgnoreCase(Integer.toString(SMTableapbatches.AP_BATCH_TYPE_REVERSALS)) == 0){
    		flagReversedChecksAsVoid(conn);
    	}
    	
    	//First, set any transactions with a 'current amt' of zero to have a discount available of zero as well:
    	updateDiscountAvailableOnClosedTransactions(conn);
    	
    	//If the batch had any checks or check reversals in it, add bank account entries:
    	if (
    		(getsbatchtype().compareToIgnoreCase(Integer.toString(SMTableapbatches.AP_BATCH_TYPE_PAYMENT)) == 0)
    		|| (getsbatchtype().compareToIgnoreCase(Integer.toString(SMTableapbatches.AP_BATCH_TYPE_REVERSALS)) == 0)
    	){
    		SMOption option = new SMOption();
    		if(!option.load(conn)){
    			throw new Exception("Error [1507061352] reading SM Options table - " + option.getErrorMessage());
    		}
    		
    		//If we need to automatically create a bank reconciliation batch, do that now:	
			if (option.getcreatebankrecexport().compareToIgnoreCase("1") == 0){
				//If the user is using AP:
				if (
					(apopt.getUsesSMCPAP().compareToIgnoreCase("1") == 0)
					//OR if the 'testing' flag is on:
					|| (apopt.checkTestingFlag(conn))
				){
					//Add records to the bankaccountentries table:
					try {
						addBankAcctEntries(conn);
					} catch (Exception e) {
						throw new Exception("Error [1507061353] adding bank account entries - " + e.getMessage());
					}
				}
			}
		}
    	
    	//After all the processing, create the GL export(s):
    	//Reload the batch here, to get the updated information in entries, etc.:
    	try {
			loadBatch(conn);
		} catch (Exception e1) {
			throw new Exception("Error [1510607243] re-loading batch to create GL export - " + e1.getMessage());
		}
    	
    	try {
			createGLBatch(conn, sUserID, log, arrMatchingLineIDsFromPrepays, sUserFullName, iFeedGLStatus);
		} catch (Exception e) {
			throw new Exception("Error [1489708212] creating GL batch - " + e.getMessage());
		}
    	
    	return;
    }

	private void flagReversedChecksAsVoid(Connection conn) throws Exception{
		
		//For each line in the reversal entry, we have to identify the check that was reversed.  That check might be a 'multipage' check, so we have to void ALL the checks
		// that were created from the same original batch and entry as the single check we're identifying:
    	for (int iEntryIndex = 0; iEntryIndex < m_arrBatchEntries.size(); iEntryIndex++){
    		APBatchEntry entry = m_arrBatchEntries.get(iEntryIndex);
			//For each check reversal entry, get all the check pages that were reversed, and flag them as 'void':
			String sOriginalCheckBatchNumber = "0";
			String sOriginalCheckEntryNumber = "0";
			String SQL = "SELECT"
				+ " " + SMTableaptransactions.loriginalbatchnumber
				+ ", " + SMTableaptransactions.loriginalentrynumber
				+ " FROM " + SMTableaptransactions.TableName
        		+ " WHERE ("
    			+ "(" + SMTableaptransactions.schecknumber + " = '" + entry.getschecknumber() + "')"
    			+ " AND (" + SMTableaptransactions.svendor + " = '" + entry.getsvendoracct() + "')"
    			+ " AND (" 
    				+ "(" + SMTableaptransactions.idoctype + " = " + SMTableaptransactions.AP_TRANSACTION_TYPE_PAYMENT_MISCPAYMENT + ")"
    				+ " OR (" + SMTableaptransactions.idoctype + " = " + SMTableaptransactions.AP_TRANSACTION_TYPE_PAYMENT_PAYMENT + ")"
    				+ " OR (" + SMTableaptransactions.idoctype + " = " + SMTableaptransactions.AP_TRANSACTION_TYPE_PAYMENT_PREPAYMENT + ")"
    			+ ")"
    		+ ")"
    		;
        	try {
				ResultSet rsApplyToDoc = clsDatabaseFunctions.openResultSet(SQL, conn);
				if (rsApplyToDoc.next()){
					sOriginalCheckBatchNumber = Long.toString(rsApplyToDoc.getLong(SMTableaptransactions.loriginalbatchnumber));
					sOriginalCheckEntryNumber = Long.toString(rsApplyToDoc.getLong(SMTableaptransactions.loriginalentrynumber));
					rsApplyToDoc.close();
				}else{
					rsApplyToDoc.close();
					throw new Exception("Error [1513133327] - Could not find check number '" + entry.getschecknumber() 
						+ "' for vendor '" + entry.getsvendoracct() + "' in check reversal entry number " + entry.getsentrynumber() + ".");
				}
			} catch (Exception e) {
				throw new Exception("Error [1513133328] - Could not read reversed check transaction with SQL '" + SQL + " - " + e.getMessage());
			}
        	
        	//Now update ALL the checks from that original batch and entry:
        	SQL = "UPDATE"
        		+  " " + SMTableapchecks.TableName
        		+ " SET " + SMTableapchecks.TableName + "." + SMTableapchecks.ivoid + " = 1"
        		+ " WHERE ("
        			+ "(" + SMTableapchecks.TableName + "." + SMTableapchecks.lbatchnumber + " = " + sOriginalCheckBatchNumber + ")"
        			+ " AND (" + SMTableapchecks.TableName + "." + SMTableapchecks.lentrynumber + " = " + sOriginalCheckEntryNumber + ")"
        		+ ")"
        	;
        	try {
    			Statement stmt = conn.createStatement();
    			stmt.execute(SQL);
    		} catch (Exception e) {
    			throw new Exception("Error [1513132238] - updating reversed checks with SQL: " + SQL + " - " + e.getMessage());
    		}
    	}
		
       	return;
		
	}
	private void addBankAcctEntries(
			Connection conn) throws Exception{
			
			//Here we need to add a new bank account entry, for every AP batch entry, AND different GL 'Cash' account:
			String SQL = "SELECT"
				+ " " + SMTableapbatchentries.TableName + "." + SMTableapbatchentries.bdentryamount
				+ ", " + SMTableapbatchentries.TableName + "." + SMTableapbatchentries.datdocdate
				+ ", " + SMTableapbatchentries.TableName + "." + SMTableapbatchentries.ientrytype
				+ ", " + SMTableapbatchentries.TableName + "." + SMTableapbatchentries.lentrynumber
				+ ", " + SMTableapbatchentries.TableName + "." + SMTableapbatchentries.schecknumber
				+ ", " + SMTableapbatchentries.TableName + "." + SMTableapbatchentries.sdocnumber
				+ ", " + SMTableapbatchentries.TableName + "." + SMTableapbatchentries.scontrolacct
				+ ", " + SMTableapbatchentries.TableName + "." + SMTableapbatchentries.svendoracct
				+ ", " + SMTableapbatchentries.TableName + "." + SMTableapbatchentries.svendorname
				+ ", " + SMTableapbatchentries.TableName + "." + SMTableapbatchentries.sremittoname
				+ ", " + SMTableapbatches.TableName + "." + SMTableapbatches.lcreatedby
				+ ", " + SMTableusers.TableName + "." + SMTableusers.sUserFirstName
				+ ", " + SMTableusers.TableName + "." + SMTableusers.sUserLastName
				+ " FROM "
				+ SMTableapbatchentries.TableName
				+ " LEFT JOIN " + SMTableapbatches.TableName + " ON " + SMTableapbatchentries.TableName + "." + SMTableapbatchentries.lbatchnumber
				+ " = " + SMTableapbatches.TableName + "." + SMTableapbatches.lbatchnumber
				+ " LEFT JOIN " + SMTableusers.TableName + " ON " + SMTableapbatches.TableName + "." + SMTableapbatches.lcreatedby 
				+ " = " + SMTableusers.TableName + "." + SMTableusers.lid
				+ " WHERE ("
					+ "(" + SMTableapbatchentries.TableName + "." + SMTableapbatchentries.lbatchnumber + " = " + getsbatchnumber() + ")"

					//We only create bank entries for PAYMENTS (i.e., not 'APPLY-TO's) OR REVERSALS:
					+ " AND ("
						+ "(" + SMTableapbatchentries.TableName + "." + SMTableapbatchentries.ientrytype + " = " + Integer.toString(SMTableapbatchentries.ENTRY_TYPE_PAYMENT_MISCPAYMENT) + ")"
						+ " OR (" + SMTableapbatchentries.TableName + "." + SMTableapbatchentries.ientrytype + " = " + Integer.toString(SMTableapbatchentries.ENTRY_TYPE_PAYMENT_PAYMENT) + ")"
						+ " OR (" + SMTableapbatchentries.TableName + "." + SMTableapbatchentries.ientrytype + " = " + Integer.toString(SMTableapbatchentries.ENTRY_TYPE_PAYMENT_PREPAYMENT) + ")"
						+ " OR (" + SMTableapbatchentries.TableName + "." + SMTableapbatchentries.ientrytype + " = " + Integer.toString(SMTableapbatchentries.ENTRY_TYPE_REVERSAL) + ")"
					+ ")" //END AND
					
					+ " AND (" + SMTableapbatchentries.TableName + "." + SMTableapbatchentries.bdentryamount + " != 0.00)"
				+ ")"  //END WHERE
			;
			try {
				ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
				while (rs.next()){
					//Insert a bank account entry record for each of these:
					
					String sPayee = rs.getString(SMTableapbatchentries.TableName + "." + SMTableapbatchentries.sremittoname);
					if (sPayee.compareToIgnoreCase("") == 0){
						sPayee = rs.getString(SMTableapbatchentries.TableName + "." + SMTableapbatchentries.svendorname);
					}
					String sDesc = "Payment to"
						+ " " + rs.getString(SMTableapbatchentries.TableName + "." + SMTableapbatchentries.svendoracct)
						+ " - " + sPayee
						+ " created by " + rs.getString(SMTableusers.TableName + "." + SMTableusers.sUserFirstName) + " "
						+ rs.getString(SMTableusers.TableName + "." + SMTableusers.sUserLastName);
					
					String sBankAccountEntryType = Integer.toString(SMTablebkaccountentries.ENTRY_TYPE_WITHDRAWAL);
					String sCheckNumber = rs.getString(SMTableapbatchentries.TableName + "." + SMTableapbatchentries.schecknumber);
					if (rs.getInt(SMTableapbatchentries.TableName + "." + SMTableapbatchentries.ientrytype) == SMTableapbatchentries.ENTRY_TYPE_REVERSAL){
						sBankAccountEntryType = Integer.toString(SMTablebkaccountentries.ENTRY_TYPE_DEPOSIT);
						sCheckNumber = rs.getString(SMTableapbatchentries.TableName + "." + SMTableapbatchentries.sdocnumber);
						sDesc = "REVERSAL of Payment #" + rs.getString(SMTableapbatchentries.TableName + "." + SMTableapbatchentries.schecknumber) + " to"
								+ " " + rs.getString(SMTableapbatchentries.TableName + "." + SMTableapbatchentries.svendoracct)
								+ " - " + sPayee
								+ " created by " + rs.getString(SMTableusers.TableName + "." + SMTableusers.sUserFirstName) + " "
								+ rs.getString(SMTableusers.TableName + "." + SMTableusers.sUserLastName);
					}
					
					//The description could potentially be longer than the description field length, so truncate if that's the case:
					if (sDesc.length() > SMTablebkaccountentries.sdescriptionlength){
						sDesc = sDesc.substring(0, SMTablebkaccountentries.sdescriptionlength - 1);
					}
					
					SQL = " INSERT INTO"
						+ " " + SMTablebkaccountentries.TableName
						+ " ("
						+ SMTablebkaccountentries.bdamount
						+ ", " + SMTablebkaccountentries.datentrydate
						+ ", " + SMTablebkaccountentries.ibatchentrynumber
						+ ", " + SMTablebkaccountentries.ibatchnumber
						+ ", " + SMTablebkaccountentries.ibatchtype
						+ ", " + SMTablebkaccountentries.icleared
						+ ", " + SMTablebkaccountentries.ientrytype
						+ ", " + SMTablebkaccountentries.lstatementid
						+ ", " + SMTablebkaccountentries.sdescription
						+ ", " + SMTablebkaccountentries.sdocnumber
						+ ", " + SMTablebkaccountentries.sglaccount
						+ ", " + SMTablebkaccountentries.ssourcemodule
						
						+ ") VALUES ("
						//Payment entries have a negative value, and we want them to be withdrawals, so they need to remain negative:
						//Reversal entries have a positive value, and we want them to be deposits, so they need to remain positive:
						+ clsManageBigDecimals.BigDecimalTo2DecimalSQLFormat(rs.getBigDecimal(SMTableapbatchentries.TableName + "." + SMTableapbatchentries.bdentryamount))
						+ ", '" + clsDateAndTimeConversions.resultsetDateStringToFormattedString(
							rs.getString(SMTableapbatchentries.TableName + "." + SMTableapbatchentries.datdocdate), 
							SMUtilities.DATE_FORMAT_FOR_SQL,
							SMUtilities.EMPTY_SQL_DATE_VALUE) 
							+ "'"
						+ ", " + Long.toString(rs.getLong(SMTableapbatchentries.TableName + "." + SMTableapbatchentries.lentrynumber)) 
						+ ", " + getsbatchnumber()
						+ ", " + getsbatchtype()
						+ ", 0" //icleared
						+ ", " + sBankAccountEntryType
						+ ", 0" //statement ID
						+ ", '" + clsDatabaseFunctions.FormatSQLStatement(sDesc) + "'"
						+ ", '" + sCheckNumber + "'"
						+ ", '" + rs.getString(SMTableapbatchentries.TableName + "." + SMTableapbatchentries.scontrolacct) + "'"
						+ ", '" + SMModuleTypes.AP + "'"
						
						+ ")"
					;
					Statement stmt = conn.createStatement();
					stmt.execute(SQL);
				}
				rs.close();
			} catch (Exception e) {
				throw new Exception("Error [1507061477] using SQL: " + SQL + " - " + e.getMessage());
			}
			
			//Testing:
//			if(true){
//				System.out.println("[1511968726] - Bank account entry insert SQL = '" + SQL + "'.");
//				throw new Exception("TESTING");
//			}
			return;
		}

    private void updateTransactionIDonChecks(Connection conn) throws Exception{
    	
    	for (int i = 0; i < getBatchEntryArray().size(); i++){
    		//Update all the check pages generated from this batch with the corresponding aptransaction ID:
    		APBatchEntry entry = getBatchEntryArray().get(i);
    		
    		String SQL = "SELECT"
    			+ " " + SMTableaptransactions.lid
    			+ " FROM " + SMTableaptransactions.TableName
    			+  " WHERE ("
    				+ " (" + SMTableaptransactions.lbatchentryid + " = " + entry.getslid() + ")"
    			+ ")"
    		;
    		String sTransactionID = "";
    		try {
				ResultSet rsTransaction = clsDatabaseFunctions.openResultSet(SQL, conn);
				if (rsTransaction.next()){
					sTransactionID = Long.toString(rsTransaction.getLong(SMTableaptransactions.lid));
				}
				rsTransaction.close();
			} catch (Exception e) {
				throw new Exception("Error [1513212711] - couldn't check for AP Transaction with SQL: '" + SQL + "' - " + e.getMessage());
			}
    		
    		if (sTransactionID.compareToIgnoreCase("") == 0){
    			throw new Exception("Error [1513212712] - couldn't find AP Transaction with batch entry ID : '" + entry.getslid() + "'.");
    		}
    		
    		//Now go and update the check pages generated from this entry with the corresponding transaction ID:
    		SQL = "UPDATE"
    			+ " " + SMTableapchecks.TableName
    			+ " SET " + SMTableapchecks.ltransactionid + " = " + sTransactionID
    			+ " WHERE ("
    				+ "(" + SMTableapchecks.lbatchentryid + " = " + entry.getslid() + ")"
    			+ ")"
    		; 
    		try {
				Statement stmt = conn.createStatement();
				stmt.execute(SQL);
			} catch (Exception e) {
				throw new Exception("Error [1513212713] - couldn't update AP transaction ID with SQL: '" + SQL + "'. - " + e.getMessage());
			}
    		
    	}
    	
    	return;
    	
    }
    
    private void updateCheckNumberonTransactions(Connection conn) throws Exception{
    	
    	String SQL = "UPDATE"
    		+  " " + SMTableaptransactions.TableName
    		+ " LEFT JOIN " + SMTableapchecks.TableName
    		+ " ON ("
    			+ "(" + SMTableapchecks.TableName + "." + SMTableapchecks.lbatchentryid + "=" + SMTableaptransactions.TableName + "." + SMTableaptransactions.lbatchentryid + ")"
    		+ ")"
    		+ " SET " + SMTableaptransactions.TableName + "." + SMTableaptransactions.schecknumber + " = " + SMTableapchecks.TableName + "." + SMTableapchecks.schecknumber
    		+ " WHERE ("
    			+ "(" + SMTableapchecks.TableName + "." + SMTableapchecks.lbatchnumber + " = " + getsbatchnumber() + ")"
    		+ ")"
    	;
    	try {
			Statement stmt = conn.createStatement();
			stmt.execute(SQL);
		} catch (Exception e) {
			throw new Exception("Error [1505835902] - updating check numbers on AP transactions with SQL: " + SQL + " - " + e.getMessage());
		}
    	return;
    	
    }
    
    public ArrayList<Long> autoApplyPrePays(Connection conn) throws Exception{
    	
    	/*
    	Any pre-pays that have been previously linked to an order number, purchase order number, or an invoice number will get automatically applied here.
    	
    	Example: a $100.00 pre-pay, and it is automatically taking a $10.00 discount:
    	
    	1) This will create a DEBIT to the pre-pay liability account - $100.00 (the amount in the pre-pay liability from this pre-pay should go to zero)
    	
    	2) A DEBIT to the discount taken account for 10.00
    	
    	3) A CREDIT to the AP control account, for 110.00 (100.00 PLUS 10.00)
    	
    	If there is a discount taken, it will also debit the pre-pay liability account, and credit the 'discounts taken' account for that amount.
    	
    	*/
    	
    	ArrayList<Long>arrMatchingLinesForAutoPrePays = new ArrayList<Long>(0);
    	
    	//First check ALL the prepays that still have a current amount on them:
    	String SQL = "SELECT * FROM " + SMTableaptransactions.TableName
    		+ " WHERE ("
    			+ "(" + SMTableaptransactions.bdcurrentamt + " != 0.00)"
    			+ " AND (" + SMTableaptransactions.idoctype + " = " + SMTableaptransactions.AP_TRANSACTION_TYPE_PAYMENT_PREPAYMENT + ")"
    			+ " AND ("
    				+ "(" + SMTableaptransactions.lapplytopurchaseorderid + " > 0)"
    				+ " OR (" + SMTableaptransactions.lapplytosalesorderid + " > 0)"
    				+ " OR (" + SMTableaptransactions.sapplytoinvoicenumber + " != '')"
    			+ ")"
    		+ ") ORDER BY " + SMTableaptransactions.datdocdate + ", " + SMTableaptransactions.lid
    	;
    	try {
			ResultSet rsPrePays = clsDatabaseFunctions.openResultSet(SQL, conn);
			
			//Go through each prepay and see if there's a document that matches the 'apply-to':
			while (rsPrePays.next()){
				
				//Get the current amount remaining on the pre-pay:
				BigDecimal bdPrepayAmtRemaining = rsPrePays.getBigDecimal(SMTableaptransactions.bdcurrentamt).negate();  // Setting this NEGATIVE value to a POSITIVE for the logic below...
				String SQL1 = "SELECT"
					+ " " + SMTableaptransactions.TableName + ".*"
					+ ", NOW() AS TODAY"
					+ " FROM " + SMTableaptransactions.TableName
					+ " WHERE ("
				;
				
				//What field do we use to find the apply-to?
				if (rsPrePays.getLong(SMTableaptransactions.lapplytopurchaseorderid) > 0){
					SQL1 += "(" + SMTableaptransactions.lapplytopurchaseorderid + " = " + rsPrePays.getLong(SMTableaptransactions.lapplytopurchaseorderid) + ")";
				}
				if (rsPrePays.getLong(SMTableaptransactions.lapplytosalesorderid) > 0){
					SQL1 += "(" + SMTableaptransactions.lapplytosalesorderid + " = " + rsPrePays.getLong(SMTableaptransactions.lapplytosalesorderid) + ")";
				}
				if (rsPrePays.getString(SMTableaptransactions.sapplytoinvoicenumber).compareToIgnoreCase("") != 0){
					SQL1 += "(" + SMTableaptransactions.sdocnumber + " = '" + rsPrePays.getString(SMTableaptransactions.sapplytoinvoicenumber) + "')";
				}
				
				//Make sure it's for the same vendor:
				SQL1 += " AND (" + SMTableaptransactions.svendor + " = '" + rsPrePays.getString(SMTableaptransactions.svendor) + "')"
					+ " AND (" + SMTableaptransactions.bdcurrentamt + " > 0.00)"
					+ ")"  //End the where clause
					+ " ORDER BY " + SMTableaptransactions.datdocdate  //Get the oldest first...
				;
				ResultSet rsEligibleApplyToDocs = clsDatabaseFunctions.openResultSet(SQL1, conn);
				while (rsEligibleApplyToDocs.next() && bdPrepayAmtRemaining.compareTo(BigDecimal.ZERO) > 0){
					
					//For each eligible 'apply-to' (usually an invoice) create a matching line:
					
					BigDecimal bdApplyToDocCurrentAmt = rsEligibleApplyToDocs.getBigDecimal(SMTableaptransactions.bdcurrentamt);
					BigDecimal bdApplyToDocDiscountAvailable = rsEligibleApplyToDocs.getBigDecimal(SMTableaptransactions.bdcurrentdiscountavailable);
					BigDecimal bdApplyToDocDiscountedCurrentAmt = bdApplyToDocCurrentAmt.subtract(bdApplyToDocDiscountAvailable);
					
					//Determine how much we can apply:
					BigDecimal bdApplyAmt = new BigDecimal("0.00");
					BigDecimal bdAppliedDiscountAmt = new BigDecimal("0.00");
					
					//If the amount remaining on the APPLY-TO doc is LESS than the amount of prepay amount we have available to apply, then
					//only apply enough to match the APPLY-TO) doc current amt:
					if(bdApplyToDocDiscountedCurrentAmt.compareTo(bdPrepayAmtRemaining) < 0){
						bdApplyAmt = bdApplyToDocDiscountedCurrentAmt;
					}else{
						bdApplyAmt = bdPrepayAmtRemaining;
					}
					
					//If we are paying the full outstanding amt, then we can also take the discount:
					if (bdApplyAmt.compareTo(bdApplyToDocDiscountedCurrentAmt) >= 0){
						bdAppliedDiscountAmt = bdApplyToDocDiscountAvailable;
					}
					
					//Update the one transaction line on the Pre-pay with the id of the invoice, since we know what it was actually applied to now:
					String SQLUpdateTransactionLine = "UPDATE " + SMTableaptransactionlines.TableName
						+ " SET " + SMTableaptransactionlines.lapplytodocid + " = " + Long.toString(rsEligibleApplyToDocs.getLong(SMTableaptransactions.TableName + "." + SMTableaptransactions.lid))
						+ " WHERE ("
							+ "(" + SMTableaptransactionlines.ltransactionheaderid + " = " + Long.toString(rsPrePays.getLong(SMTableaptransactions.lid)) + ")"
						+ ")"
					;
	            	try {
	        			Statement stmt = conn.createStatement();
	        			stmt.execute(SQLUpdateTransactionLine);
	        		} catch (Exception e) {
	        			throw new Exception("Error [1498252851] AP transaction line for pre-pay with SQL: '" + SQLUpdateTransactionLine + "' - " + e.getMessage());
	        		}
					
					// AP matching lines:
		        		
			    	//Create a matching line to the 'apply-to' and update the current amt of the applying-to
	    			//If there IS an 'applying' line, then we insert a matching line to point to the 'applying-to' transaction:
	            	String SQL2 = "INSERT INTO " + SMTableapmatchinglines.TableName + "("
	            		+ SMTableapmatchinglines.bdappliedamount
	            		+ ", " + SMTableapmatchinglines.bddiscountappliedamount
	            		+ ", " + SMTableapmatchinglines.dattransactiondate
	            		+ ", " + SMTableapmatchinglines.ltransactionappliedfromid
	            		+ ", " + SMTableapmatchinglines.ltransactionappliedtoid
	            		+ ", " + SMTableapmatchinglines.sappliedfromdocnumber
	            		+ ", " + SMTableapmatchinglines.sappliedtodocnumber
	            		+ ", " + SMTableapmatchinglines.sdescription
	            		+ ", " + SMTableapmatchinglines.svendor
	            			
	            		+ ") VALUES ("
	            		// We want to be able to SUBTRACT the line amount from the applied to amount to REDUCE (or INCREASE, in the case
	            		//of a CREDIT) the vendor liability (i.e., the CURRENT AMT on the apply-to transaction:
	            		+ clsManageBigDecimals.BigDecimalTo2DecimalSQLFormat(bdApplyAmt) // bdApplyAmt is positive at this point...
	            		+ ", " + clsManageBigDecimals.BigDecimalTo2DecimalSQLFormat(bdAppliedDiscountAmt)  //discount amt
	            		+ ", NOW()"  //transaction date
	            		+ ", " + Long.toString(rsPrePays.getLong(SMTableaptransactions.lid))  //ltransactionappliedfromid
	            		+ ", " + Long.toString(rsEligibleApplyToDocs.getLong(SMTableaptransactions.lid)) //ltransactionappliedtoid
	            		+ ", '" + clsDatabaseFunctions.FormatSQLStatement(rsPrePays.getString(SMTableaptransactions.sdocnumber)) + "'"  //sappliedfromdocnumber
	            		+ ", '" + clsDatabaseFunctions.FormatSQLStatement(rsEligibleApplyToDocs.getString(SMTableaptransactions.sdocnumber)) + "'"  //sappliedtodocnumber
	            		+ ", '" + clsDatabaseFunctions.FormatSQLStatement("Auto Pre-pay Application from Batch #" + getsbatchnumber()) + "'" 
	            		+ ", '" + rsPrePays.getString(SMTableaptransactions.svendor) + "'" //vendor
	            		+ ")"
	            	;
	            	try {
	        			Statement stmt = conn.createStatement();
	        			stmt.execute(SQL2);
	        		} catch (Exception e) {
	        			throw new Exception("Error [1495479422] inserting auto pre-pay matching line for AP transaction ID " + Long.toString(rsEligibleApplyToDocs.getLong(SMTableaptransactions.lid)) 
	        				+ " to apply to APPLY_TO doc with SQL: " + SQL2 + " - " + e.getMessage());
	        		}
	            	
	            	//if (bdAppliedDiscountAmt.compareTo(BigDecimal.ZERO) != 0){
	            	//Now get the newly inserted AP matching line record's ID and add it to the array of pre-pays - this will be used to create GL export details
	            	//when the batch is posted:
	            	String SQLLastInsert = "SELECT last_insert_id()";
	            	long lLastInsertID = 0;
	            	try {
						ResultSet rsLastInsert = clsDatabaseFunctions.openResultSet(SQLLastInsert, conn);
						if (rsLastInsert.next()){
							lLastInsertID = rsLastInsert.getLong(1);
						}else{
							rsLastInsert.close();
							throw new Exception("Error [1498233487] getting last AP matching line insert ID.");
						}
						rsLastInsert.close();
					} catch (Exception e) {
						throw new Exception("Error [1498233489] getting last AP matching line insert ID - " + e.getMessage());
					}
	            	
	            	if (lLastInsertID == 0){
	            		throw new Exception("Error [1498233488] getting last AP matching line insert ID.");
	            	}
	            	
	            	//Finally, add this matching line ID to the list of matching lines for which we need to create an export detail:
	            	arrMatchingLinesForAutoPrePays.add(lLastInsertID);
	            	//}
	            	
	            	//Now update the current amt on the apply-to transaction - the current amt of the APPLY-TO transaction gets updated with the applying amt AND and available discount amt.
	            	//In other words, we're taking the discount available, automatically:
	            	SQL2 = "UPDATE " + SMTableaptransactions.TableName
	            		+ " SET " + SMTableaptransactions.bdcurrentamt
	            			+ " = (" + SMTableaptransactions.bdcurrentamt + " - " + clsManageBigDecimals.BigDecimalTo2DecimalSQLFormat(bdApplyAmt.add(bdAppliedDiscountAmt)) + ")"
	            		+ ", " + SMTableaptransactions.bdcurrentdiscountavailable
	            			+ " = (" + SMTableaptransactions.bdcurrentdiscountavailable + " - " + clsManageBigDecimals.BigDecimalTo2DecimalSQLFormat(bdAppliedDiscountAmt) + ")"
	            		+ " WHERE ("
	            			+ "(" + SMTableaptransactions.lid + " = " + Long.toString(rsEligibleApplyToDocs.getLong(SMTableaptransactions.lid)) + ")"
	            		+ ")"
	            	;
	            	try {
	        			Statement stmt = conn.createStatement();
	        			stmt.execute(SQL2);
	        		} catch (Exception e) {
	        			throw new Exception("Error [1495479423] updating current amt on APPLY-TO document with SQL: " + SQL2 + " - " + e.getMessage());
	            	}
	            	
	                //Now create a matching line pointing back to the APPLY FROM entry, to reduce its current amount also:
	            	SQL2 = "INSERT INTO " + SMTableapmatchinglines.TableName + "("
	            		+ SMTableapmatchinglines.bdappliedamount
	            		+ ", " + SMTableapmatchinglines.bddiscountappliedamount
	            		+ ", " + SMTableapmatchinglines.dattransactiondate
	            		+ ", " + SMTableapmatchinglines.ltransactionappliedfromid
	            		+ ", " + SMTableapmatchinglines.ltransactionappliedtoid
	            		+ ", " + SMTableapmatchinglines.sappliedfromdocnumber
	            		+ ", " + SMTableapmatchinglines.sappliedtodocnumber
	            		+ ", " + SMTableapmatchinglines.sdescription
	            		+ ", " + SMTableapmatchinglines.svendor
	            			
	            		+ ") VALUES ("
	            		// The sign of the matching lines here remains the same as the original entry line - so that when we SUBTRACT the matching lines from the APPLY FROM entry,
	            		//it decreases the original amount of the apply from entry:
	            		+ clsManageBigDecimals.BigDecimalTo2DecimalSQLFormat(bdApplyAmt.negate())  //Make the applied amt negative here, for applying against the pre-pay
	            		//APPLIED discount amt is always zero - even if we are taking a discount, 
            			//because the 'apply-from' doc isn't getting a discount applied against ITSELF:
	            		+ ", 0.00"  
	            		+ ", NOW()"  //transaction date
	            		+ ", " + Long.toString(rsPrePays.getLong(SMTableaptransactions.lid))  //ltransactionappliedfromid
	            		+ ", " + Long.toString(rsPrePays.getLong(SMTableaptransactions.lid)) //ltransactionappliedtoid - this matching line APPLIES BACK TO THE ENTRY THAT CREATED IT
	            		+ ", '" + clsDatabaseFunctions.FormatSQLStatement(rsPrePays.getString(SMTableaptransactions.sdocnumber)) + "'"  //sappliedfromdocnumber
	            		+ ", '" + clsDatabaseFunctions.FormatSQLStatement(rsPrePays.getString(SMTableaptransactions.sdocnumber)) + "'"  //sappliedtodocnumber
	            		+ ", '" + clsDatabaseFunctions.FormatSQLStatement("Auto Pre-pay Application from Batch #" + getsbatchnumber()) + "'" 
	            		+ ", '" + rsPrePays.getString(SMTableaptransactions.svendor) + "'" //vendor
	            		+ ")"
	            	;
	            	try {
	        			Statement stmt = conn.createStatement();
	        			stmt.execute(SQL2);
	        		} catch (Exception e) {
	        			throw new Exception("Error [1495479424] inserting auto pre-pay matching line for AP transaction ID " + Long.toString(rsEligibleApplyToDocs.getLong(SMTableaptransactions.lid)) 
	        				+ " to apply to APPLY_TO doc with SQL: " + SQL2 + " - " + e.getMessage());
	        		}
	            	
	            	//Now update the current amt on the applied-FROM transaction:
	            	SQL2 = "UPDATE " + SMTableaptransactions.TableName
	            		+ " SET " + SMTableaptransactions.bdcurrentamt
	            		+ " = (" + SMTableaptransactions.bdcurrentamt + " - (" + clsManageBigDecimals.BigDecimalTo2DecimalSQLFormat(bdApplyAmt.negate()) + "))"
	            		+ " WHERE ("
	            			+ "(" + SMTableaptransactions.lid + " = " + Long.toString(rsPrePays.getLong(SMTableaptransactions.lid)) + ")"
	            		+ ")"
	            	;
	            	
	            	try {
	        			Statement stmt = conn.createStatement();
	        			stmt.execute(SQL2);
	        		} catch (Exception e) {
	        			throw new Exception("Error [1495479425] updating current amt on APPLY-FROM document with SQL: " + SQL2 + " - " + e.getMessage());
	        		}
			    	
	            	bdPrepayAmtRemaining = bdPrepayAmtRemaining.subtract(bdApplyAmt);
	            	
	            	//Next, we have to record the 'discount taken' or 'discount lost', invoices paid, etc.:
	            	Date docDate = rsEligibleApplyToDocs.getDate("TODAY");
	                Calendar cal = Calendar.getInstance();
	                cal.setTime(docDate);
	                String sYear = Integer.toString(cal.get(Calendar.YEAR));
	                String sMonth = Integer.toString(cal.get(Calendar.MONTH));
	                
	            	String SQLStatistics =
                		"INSERT INTO " + SMTableapvendorstatistics.TableName + "("
        	    		+ SMTableapvendorstatistics.bdamountofdiscounts
        	    		+ ", " + SMTableapvendorstatistics.lmonth
        	    		+ ", " + SMTableapvendorstatistics.lnumberofdiscountstaken
        	    		+ ", " + SMTableapvendorstatistics.lnumberofinvoicespaid
        	    		+ ", " + SMTableapvendorstatistics.lyear
        	    		+ ", " + SMTableapvendorstatistics.svendoracct
        	    		+ ") VALUES ("
        	    		+ clsManageBigDecimals.BigDecimalTo2DecimalSQLFormat(bdAppliedDiscountAmt)
        	    		+ ", " + sMonth
        	    		+ ", " + "1"
        	    		+ ", " + "1"
        	    		+ ", " + sYear
        	    		+ ", '" + rsPrePays.getString(SMTableaptransactions.svendor) + "'"
        	    		
        	    		+ ") ON DUPLICATE KEY UPDATE"
        	    		+ " " + SMTableapvendorstatistics.bdamountofadjustments + " = " + SMTableapvendorstatistics.bdamountofadjustments + " + " + clsManageBigDecimals.BigDecimalTo2DecimalSQLFormat(bdAppliedDiscountAmt)
        	    		+ ", " + SMTableapvendorstatistics.lnumberofdiscountstaken + " = " + SMTableapvendorstatistics.lnumberofdiscountstaken + " + 1"
        	    		+ ", " + SMTableapvendorstatistics.lnumberofinvoicespaid + " = " + SMTableapvendorstatistics.lnumberofinvoicespaid + " + 1"
        	    	;
        	    	//System.out.println("[1498236896] SQL = '" + SQL + "'");
        	    	try {
        				Statement stmt = conn.createStatement();
        				stmt.execute(SQLStatistics);
        			} catch (Exception e) {
        				throw new Exception("Error [1498236919] updating statistics with SQL: '" + SQLStatistics + "' - " + e.getMessage());
        			}
				}
				rsEligibleApplyToDocs.close();
			}
			rsPrePays.close();
		} catch (Exception e) {
			throw new Exception("Error [1495466730] reading pre-pays to apply to invoices with SQL - " + SQL + " ' - "+ e.getMessage());
		}
    	
    	return arrMatchingLinesForAutoPrePays;
    }
    private void updateDiscountAvailableOnClosedTransactions(Connection conn) throws Exception{
    	String SQL = "UPDATE " + SMTableaptransactions.TableName
    		+ " SET " + SMTableaptransactions.bdcurrentdiscountavailable + " = 0.00"
    		+ " WHERE ("
    			+ "(" + SMTableaptransactions.bdcurrentamt + " = 0.00)"
    		+ ")"
    	;
    	try {
			Statement stmt = conn.createStatement();
			stmt.execute(SQL);
		} catch (SQLException e) {
			throw new Exception("Error [1494436522]  - could not update discount available on closed transactions with SQL: '" + SQL + "' - " + e.getMessage());
		}
    	
    	return;
    }
    private void createGLBatch(
    	Connection conn, 
    	String sUserID, 
    	SMLogEntry log, 
    	ArrayList<Long>arrMatchingLineIDsFromAutoPrepays,
    	String sUserFullName,
    	int iFeedGL
    	) throws Exception{
    	
    	//TJR - here is where the GL batch gets created during posting:
    	if (bDebugMode){
	    	log.writeEntry(
	    			sUserID,
	    			SMLogEntry.LOG_OPERATION_APBATCHPOST,
	        		"In post_without_data_transaction Batch #:" + getsbatchnumber(), 
	        		"Going into createGLBatch",
	        		"[1489707497]"
	        );
    	}
    	
    	SMGLExport export = new SMGLExport();
    	
    	SMOption smopt = new SMOption();
    	if(!smopt.load(conn)){
    		throw new Exception("Error [1489711672] reading SM Options - " + smopt.getErrorMessage());
    	}
    	export.setExportFilePath(smopt.getFileExportPath());

    	//Branch off here to do different processes, depending on whether this is an INVOICE or a PAYMENT batch:
    	//***********************
    	//If it's an invoice batch, create a new invoice transaction:
    	if (getsbatchtype().compareToIgnoreCase(Integer.toString(SMTableapbatches.AP_BATCH_TYPE_INVOICE)) == 0){
    		createGLBatchForInvBatchType(export, conn, sUserID);
    	}
   
    	//If it's a payment batch, create a new payment transaction:
    	if (getsbatchtype().compareToIgnoreCase(Integer.toString(SMTableapbatches.AP_BATCH_TYPE_PAYMENT)) == 0){
    		createGLBatchForPmtBatchType(export, conn, sUserID);
    	}
    	
       	//If it's a check reversal batch, create a new reversal transaction:
    	if (getsbatchtype().compareToIgnoreCase(Integer.toString(SMTableapbatches.AP_BATCH_TYPE_REVERSALS)) == 0){
    		createGLBatchForReversalBatchType(export, conn, sUserID);
    	}
    	
    	//Now add GL entries for any discounts applied by automatically generated pre-pays:
    	addAutoPrepaysToGLBatch(export, conn, arrMatchingLineIDsFromAutoPrepays);
    	
        String sExportBatchNumber = getsbatchnumber();
        sExportBatchNumber = clsStringFunctions.PadLeft(sExportBatchNumber, "0", 6);
        
        if (
        	(iFeedGL == SMTableapoptions.FEED_GL_BOTH_EXTERNAL_AND_SMCP_GL)
        	|| (iFeedGL == SMTableapoptions.FEED_GL_SMCP_GL_ONLY)
        ){
	        GLTransactionBatch gltransactionbatch = null;
	        try {
				gltransactionbatch = export.createGLTransactionBatch(
					conn, 
					sUserID, 
					sUserID, 
					getsbatchdate(), 
					"AP " + SMTableapbatches.getBatchTypeLabel(Integer.parseInt(getsbatchtype())) + " Batch #" + getsbatchnumber());
			} catch (Exception e1) {
				throw new Exception("Error [1557429977] creating GL transactionbatch - " + e1.getMessage());
			}
	        
	        try {
				gltransactionbatch.save_without_data_transaction(conn, sUserID, sUserFullName, true);
			} catch (Exception e1) {
				throw new Exception("Error [1557429977] saving GL transactionbatch - " + e1.getMessage());
			}
        }
        try {
			export.saveExport(sExportBatchNumber, conn);
		} catch (Exception e) {
			throw new Exception("Error [1489711674] saving GL export file - " + e.getMessage());
		}
        
		APOptions apopt = new APOptions();
		if(!apopt.load(conn)){
			throw new Exception("Error [1489711675] getting export file type - " + apopt.getErrorMessageString());
		}
		
        if (
           	(iFeedGL == SMTableapoptions.FEED_GL_BOTH_EXTERNAL_AND_SMCP_GL)
           	|| (iFeedGL == SMTableapoptions.FEED_GL_EXTERNAL_GL_ONLY)
        ){
	        if (export.getExportFilePath().compareToIgnoreCase("") != 0){
		        if (!export.writeExportFile(
		        		SMModuleTypes.AP, 
		        		SMTableapbatches.getBatchTypeLabel(Integer.parseInt(getsbatchtype())), 
		        		sExportBatchNumber,
		        		Integer.parseInt(apopt.getiexportoption()),
		        		conn)
		        	){
		        	throw new Exception("Error [1489711676] writing to export file - " + apopt.getErrorMessageString());
		        }
		    }
        }
    	return;
    }
    private void createGLBatchForInvBatchType(SMGLExport export, Connection conn, String sUserID) throws Exception{
    	
    	for (int i = 0; i < m_arrBatchEntries.size(); i++){
			APBatchEntry entry = m_arrBatchEntries.get(i);
			
			String sEntryTransactionID = "";
			try {
				sEntryTransactionID = getAPTransactionID(m_arrBatchEntries.get(i).getslid(), conn);
			} catch (Exception e3) {
				throw new Exception("Error [20192271449261] " + e3.getMessage());
			}
			
    		export.addHeader(
    			SMModuleTypes.AP, 
    			SMTableapbatches.getBatchSourceTypeLabels(Integer.parseInt(getsbatchtype())),
    			"AP Batch Export", 
    			"SMAP",
    			entry.getsdatdocdate(),
    			entry.getsdatentrydate(),
    			buildGLTransactionEntryDescription(entry),
    			sEntryTransactionID
    		);
    		
//    		java.sql.Date datEntry = null;
//			try {
//				datEntry = SMUtilities.StringTojavaSQLDate(SMUtilities.DATE_FORMAT_FOR_DISPLAY, entry.getsdatentrydate());
//			} catch (Exception e) {
//				throw new Exception("Error [1494535602] - cannot convert entry date '" 
//					+ entry.getsdatentrydate() + "' to SQL date - " + e.getMessage());
//			}
			
    		java.sql.Date datDocDate = null;
			try {
				datDocDate = clsDateAndTimeConversions.StringTojavaSQLDate(SMUtilities.DATE_FORMAT_FOR_DISPLAY, entry.getsdatdocdate());
			} catch (Exception e) {
				throw new Exception("Error [1494535102] - cannot convert document date '" 
					+ entry.getsdatdocdate() + "' to SQL date - " + e.getMessage());
			}
			
			//Get the GL accts we might need from the AP Account set for the vendor:
    		String sDiscountTakenAcct = "";
    		String sAccountsPayableAcct = "";
    		String sPrePayAcct = "";
    		String SQL = "SELECT"
    			+ " " + SMTableapaccountsets.TableName + "." + SMTableapaccountsets.spurchasediscountacct
    			+ ", " + SMTableapaccountsets.TableName + "." + SMTableapaccountsets.spayablescontrolacct
    			+ ", " + SMTableapaccountsets.TableName + "." + SMTableapaccountsets.sprepaymentacct
    			+ " FROM " + SMTableicvendors.TableName
    			+ " LEFT JOIN " + SMTableapaccountsets.TableName
    			+ " ON " + SMTableapaccountsets.TableName + "." + SMTableapaccountsets.lid + " = "
    				+ SMTableicvendors.iapaccountset
    			+ " WHERE ("
    				+ "(" + SMTableicvendors.TableName + "." + SMTableicvendors.svendoracct + " = '" + entry.getsvendoracct() + "')"
    			+ ")"
    		;
    		try {
				ResultSet rsDiscountAcct = clsDatabaseFunctions.openResultSet(SQL, conn);
				if (rsDiscountAcct.next()){
					sDiscountTakenAcct = rsDiscountAcct.getString(SMTableapaccountsets.TableName + "." + SMTableapaccountsets.spurchasediscountacct);
					if (sDiscountTakenAcct == null){
						sDiscountTakenAcct = "";
					}
					sAccountsPayableAcct = rsDiscountAcct.getString(SMTableapaccountsets.TableName + "." + SMTableapaccountsets.spayablescontrolacct);
					if (sAccountsPayableAcct == null){
						sAccountsPayableAcct = "";
					}
					sPrePayAcct = rsDiscountAcct.getString(SMTableapaccountsets.TableName + "." + SMTableapaccountsets.sprepaymentacct);
					if (sPrePayAcct == null){
						sPrePayAcct = "";
					}
				}
				rsDiscountAcct.close();
			} catch (Exception e1) {
				throw new Exception("Error:[1498155002] reading Discounts Taken GL for vendor '" + entry.getsvendoracct() + "'"
					+ " with SQL: " + SQL + e1.getMessage());
			}
    		
    		if (sDiscountTakenAcct.compareTo("") == 0){
    			throw new Exception("Error:[1498155001] - could not get Discounts Taken GL Account for vendor '" + entry.getsvendoracct() + "'.");
    		}
			
    		if (sAccountsPayableAcct.compareTo("") == 0){
    			throw new Exception("Error:[1498155002] - could not get Accounts Payable GL Account for vendor '" + entry.getsvendoracct() + "'.");
    		}
			
       		String sEntryDesc = entry.getsvendoracct() + " " + entry.getsvendorname();
    		if (sEntryDesc.length() > SMTableglexportdetails.sdetailtransactiondescriptionlength){
    			sEntryDesc = sEntryDesc.substring(0, SMTableglexportdetails.sdetailtransactiondescriptionlength - 1).trim();
    		}
       		String sEntryReference = "Doc #: " + entry.getsdocnumber() + " Doc Date " + entry.getsdatdocdate();
    		if (sEntryReference.length() > SMTableglexportdetails.sdetailtransactionreferencelength){
    			sEntryReference = sEntryReference.substring(0, SMTableglexportdetails.sdetailtransactionreferencelength - 1).trim();
    		}
       		String sEntryComment = entry.getsentrydescription();
    		if (sEntryComment.length() > SMTableglexportdetails.sdetailcommentlength){
    			sEntryComment = sEntryComment.substring(0, SMTableglexportdetails.sdetailcommentlength - 1).trim();
    		}
    		
    		//Add a GL Entry for the control account (typically Accounts Payable for invoices, credit and debit notes,  or CASH for payments):
    		BigDecimal bdCost;
			try {
				bdCost = new BigDecimal(entry.getsentryamount().trim().replace(",", ""));
			} catch (Exception e1) {
				throw new Exception("Error [1494535601] - cannot convert entry.getsentryamount()() '" 
					+ entry.getsentryamount() + "' on entry number " + entry.getsentrynumber() + " to SQL date - " + e1.getMessage());
			}
			
			//Get the SIGN of the cost correct:
			// the entry amount on an invoice or debit entry is a 
			
			//Now add one detail for the entry itself - this is the 'control' side:
    		try {
				export.addDetail(
					datDocDate,
					bdCost.negate(), //Cost on invoices/credit notes/debit notes need to be reversed to come in correctly as 'debit' or 'credit':
					entry.getscontrolacct(),
					sEntryComment,
					sEntryDesc,
					sEntryReference,
					"0",
					conn
				);
			} catch (Exception e) {
				throw new Exception("Error:[1494535603] adding export detail from ENTRY details - "
						+ e.getMessage());
			}
    		
    		//Handle a pre-pay application:
    		//Here we'll check to see if any of the entries had a pre-pay automatically applied to them.  Because in that case, we have to add another pair of GL transactions:
    		// One to credit the 'pre-pay' account, and one to debit the AP control acct.  This is done so that the 'pre-pay' account accurately represents the total amount
    		//which we are currently holding as a 'pre-pay' amt:
    		
    		//Get the transaction created from this entry, so we can check to see if the current amt has already been reduced:
    		String SQLTransactions = "SELECT * FROM " + SMTableaptransactions.TableName
    			+ " WHERE ("
    				+ "(" + SMTableaptransactions.TableName + "." + SMTableaptransactions.sdocnumber + " = '" + entry.getsdocnumber() + "')"
    				+ " AND (" + SMTableaptransactions.TableName + "." + SMTableaptransactions.svendor + " = '" + entry.getsvendoracct() + "')"
    			+ ")"
    		;
    		String sLineDesc = entry.getsvendoracct() + " " + entry.getsvendorname();
    		if (sLineDesc.length() > SMTableglexportdetails.sdetailtransactiondescriptionlength){
    			sLineDesc = sEntryDesc.substring(0, SMTableglexportdetails.sdetailtransactiondescriptionlength - 1).trim();
    		}
       		String sLineReference = "Doc #: " + entry.getsdocnumber() + " Doc Date " + entry.getsdatdocdate();
    		if (sLineReference.length() > SMTableglexportdetails.sdetailtransactionreferencelength){
    			sLineReference = sEntryReference.substring(0, SMTableglexportdetails.sdetailtransactionreferencelength - 1).trim();
    		}

    		try {
				ResultSet rs = clsDatabaseFunctions.openResultSet(SQLTransactions, conn);
				if (rs.next()){
					//If the current amt is already different than the original amt, then the transaction must have had a pre-pay applied to it in this posting:
					BigDecimal bdAppliedAmt = rs.getBigDecimal(SMTableaptransactions.bdoriginalamt).subtract(rs.getBigDecimal(SMTableaptransactions.bdcurrentamt));
					if (bdAppliedAmt.compareTo(BigDecimal.ZERO) != 0){
						//Add a detail to reduce the AP control liability:
						export.addDetail(
							datDocDate,
							bdAppliedAmt, //Costs need to be reversed to come in correctly as 'debit' or 'credit':
							entry.getscontrolacct(),
							"From auto-prepay application",
							sLineDesc,
							sLineReference,
							"0",
							conn
						);
						
						//Add a detail to reduce the pre-pay liability:
						export.addDetail(
							datDocDate,
							bdAppliedAmt.negate(), //Costs need to be reversed to come in correctly as 'debit' or 'credit':
							sPrePayAcct,
							"From auto-prepay application",
							sLineDesc,
							sLineReference,
							"0",
							conn
						);
					}
				}else{
					rs.close();
					throw new Exception("Error:[1498590815] adding export detail from ENTRY details - could not find transaction with vendor '" + entry.getsvendoracct()
						+ "' and doc number '" + entry.getsdocnumber() + "'"
					);
				}
			} catch (Exception e2) {
				throw new Exception("Error:[1498590816] reading transaction with vendor '" + entry.getsvendoracct()
					+ "' and doc number '" + entry.getsdocnumber() + "' - " + e2.getMessage()
				);
			}
    		
    		//Now add details for each of the lines:
    		for (int j = 0; j < entry.getLineArray().size(); j ++){
    			//Now add each line from the entry as a GL transaction:
    			APBatchEntryLine line = entry.getLineArray().get(j);
           		String sLineComment = line.getsdescription();
        		if (sLineComment.length() > SMTableglexportdetails.sdetailcommentlength){
        			sLineComment = sLineComment.substring(0, SMTableglexportdetails.sdetailcommentlength - 1).trim();
        		}
        		//Add a GL Entry to the control account (typically Accounts Payable for invoices, credit and debit notes,  or CASH for payments):
        		BigDecimal bdLineCost;
				try {
					bdLineCost = new BigDecimal(line.getsbdamount().trim().replace(",", ""));
				} catch (Exception e1) {
					throw new Exception("Error [1493315835] - cannot convert line.getsbdamount() '" 
						+ line.getsbdamount() + "' on line " + Integer.toString((j + 1)) + ", entry number " + entry.getsentrynumber() + " to decimal - " + e1.getMessage());
				}

        		//Add a GL Entry for each line - the balancing account ('distribution' or 'expense' account) to the GL detail for the entry (above)
    			try {
					export.addDetail(
							datDocDate,
							bdLineCost, //Costs need to be reversed to come in correctly as 'debit' or 'credit'
							line.getsdistributionacct(),
							sLineComment,
							sLineDesc,
							sLineReference,
							line.getslinenumber(),
							conn
							);
				} catch (Exception e) {
					throw new Exception("Error:[1489780017] adding export detail from LINE details - "
						+ e.getMessage());
				}
    			
    			//IF any lines include discounts applied, these need separate GL transactions.
    			// The transactions look like this:
    			// 1) the discount taken creates a Debit to Accounts Payable for the discount amt taken AND
    			// 2) a Credit to a 'Discounts Taken' GL account for the discount amt
    			
        		BigDecimal bdDiscountAmtApplied;
				try {
					bdDiscountAmtApplied = new BigDecimal(line.getsbddiscountappliedamt().trim().replace(",", ""));
				} catch (Exception e1) {
					throw new Exception("Error [1498151650] - cannot convert line.getsbddiscountappliedamt() '" 
						+ line.getsbddiscountappliedamt() + "' on line " + Integer.toString((j + 1)) + ", entry number " + entry.getsentrynumber() + " to decimal - " + e1.getMessage());
				}
    			if (bdDiscountAmtApplied.compareTo(BigDecimal.ZERO) != 0){  //Applied discounts are negative
    				
            		//Add a GL Entry to the 'Discounts Taken' and 'Accounts Payable' accounts:
    				//Debit the AP account:
            		//Add a GL Entry to reduce the AP liability (control) account:
        			try {
    					export.addDetail(
							datDocDate,
							bdDiscountAmtApplied, //Costs need to be reversed to come in correctly as 'debit' or 'credit'
							sAccountsPayableAcct,
							sLineComment,
							sLineDesc,
							sLineReference,
							line.getslinenumber(),
							conn
						);
        			} catch (Exception e) {
    					throw new Exception("Error:[1498154051] adding export detail from LINE details for applied discount - "
    						+ e.getMessage());
    				}
        			
        			try {
    					export.addDetail(
    						datDocDate,
							bdDiscountAmtApplied.negate(), //Costs need to be reversed to come in correctly as 'debit' or 'credit'
							sDiscountTakenAcct,
							sLineComment,
							sLineDesc,
							sLineReference,
							line.getslinenumber(),
							conn
						);
    				} catch (Exception e) {
    					throw new Exception("Error:[1498154052] adding export detail from LINE details for applied discount - "
    						+ e.getMessage());
    				}
    			}
    		}
    	}
    	return;
    }
    private void createGLBatchForPmtBatchType(SMGLExport export, Connection conn, String sUserID) throws Exception{
    	
    	for (int i = 0; i < m_arrBatchEntries.size(); i++){
			APBatchEntry entry = m_arrBatchEntries.get(i);
			String sEntryTransactionID = "";
			try {
				sEntryTransactionID = getAPTransactionID(m_arrBatchEntries.get(i).getslid(), conn);
			} catch (Exception e3) {
				throw new Exception("Error [20192271449262] " + e3.getMessage());
			}
    		export.addHeader(
    			SMModuleTypes.AP, 
    			SMTableapbatches.getBatchSourceTypeLabels(Integer.parseInt(getsbatchtype())),
    			"AP Batch Export", 
    			"SMAP",
    			entry.getsdatdocdate(),
    			entry.getsdatentrydate(),
    			buildGLTransactionEntryDescription(entry),
    			sEntryTransactionID
    		);
			
    		java.sql.Date datDocDate = null;
			try {
				datDocDate = clsDateAndTimeConversions.StringTojavaSQLDate(SMUtilities.DATE_FORMAT_FOR_DISPLAY, entry.getsdatdocdate());
			} catch (Exception e) {
				throw new Exception("Error [1494535802] - cannot convert document date '" 
					+ entry.getsdatdocdate() + "' to SQL date - " + e.getMessage());
			}
			
			//Get the GL accts we might need from the AP Account set for the vendor:
    		String sDiscountTakenAcct = "";
    		String sAccountsPayableAcct = "";
    		String sPrePayAcct = "";
    		String SQL = "SELECT"
    			+ " " + SMTableapaccountsets.TableName + "." + SMTableapaccountsets.spurchasediscountacct
    			+ ", " + SMTableapaccountsets.TableName + "." + SMTableapaccountsets.spayablescontrolacct
    			+ ", " + SMTableapaccountsets.TableName + "." + SMTableapaccountsets.sprepaymentacct
    			+ " FROM " + SMTableicvendors.TableName
    			+ " LEFT JOIN " + SMTableapaccountsets.TableName
    			+ " ON " + SMTableapaccountsets.TableName + "." + SMTableapaccountsets.lid + " = "
    				+ SMTableicvendors.iapaccountset
    			+ " WHERE ("
    				+ "(" + SMTableicvendors.TableName + "." + SMTableicvendors.svendoracct + " = '" + entry.getsvendoracct() + "')"
    			+ ")"
    		;
    		try {
				ResultSet rsDiscountAcct = clsDatabaseFunctions.openResultSet(SQL, conn);
				if (rsDiscountAcct.next()){
					sDiscountTakenAcct = rsDiscountAcct.getString(SMTableapaccountsets.TableName + "." + SMTableapaccountsets.spurchasediscountacct);
					if (sDiscountTakenAcct == null){
						sDiscountTakenAcct = "";
					}
					sAccountsPayableAcct = rsDiscountAcct.getString(SMTableapaccountsets.TableName + "." + SMTableapaccountsets.spayablescontrolacct);
					if (sAccountsPayableAcct == null){
						sAccountsPayableAcct = "";
					}
					sPrePayAcct = rsDiscountAcct.getString(SMTableapaccountsets.TableName + "." + SMTableapaccountsets.sprepaymentacct);
					if (sPrePayAcct == null){
						sPrePayAcct = "";
					}
				}
				rsDiscountAcct.close();
			} catch (Exception e1) {
				throw new Exception("Error:[1498155102] reading Discounts Taken GL for vendor '" + entry.getsvendoracct() + "'"
					+ " with SQL: " + SQL + e1.getMessage());
			}
    		
    		if (sDiscountTakenAcct.compareTo("") == 0){
    			throw new Exception("Error:[1498155101] - could not get Discounts Taken GL Account for vendor '" + entry.getsvendoracct() + "'.");
    		}
			
    		if (sAccountsPayableAcct.compareTo("") == 0){
    			throw new Exception("Error:[1498155202] - could not get Accounts Payable GL Account for vendor '" + entry.getsvendoracct() + "'.");
    		}
			
    		//If the entry is an APPLY-TO, the net amount of the entry is ZERO, so we don't add that:
    		if(entry.getientrytype() == SMTableapbatchentries.ENTRY_TYPE_PAYMENT_APPLYTO){
    			//We don't create a GL detail for the entry itself...
    		}else{
	       		String sEntryDesc = entry.getsvendoracct() + " " + entry.getsvendorname();
	    		if (sEntryDesc.length() > SMTableglexportdetails.sdetailtransactiondescriptionlength){
	    			sEntryDesc = sEntryDesc.substring(0, SMTableglexportdetails.sdetailtransactiondescriptionlength - 1).trim();
	    		}
	       		String sEntryReference = "CK#: " + entry.getschecknumber() + " Doc Date " + entry.getsdatdocdate();
	    		if (sEntryReference.length() > SMTableglexportdetails.sdetailtransactionreferencelength){
	    			sEntryReference = sEntryReference.substring(0, SMTableglexportdetails.sdetailtransactionreferencelength - 1).trim();
	    		}
	       		String sEntryComment = entry.getsentrydescription();
	    		if (sEntryComment.length() > SMTableglexportdetails.sdetailcommentlength){
	    			sEntryComment = sEntryComment.substring(0, SMTableglexportdetails.sdetailcommentlength - 1).trim();
	    		}
	    		//Add a GL Entry for the control account (typically CASH for payments):
	    		
	    		//For normal payments, the entry amount is a NEGATIVE number:
	    		BigDecimal bdEntryCost;
				try {
					bdEntryCost = new BigDecimal(entry.getsentryamount().trim().replace(",", ""));
				} catch (Exception e1) {
					throw new Exception("Error [1494535701] - cannot convert entry.getsentryamount()() '" 
						+ entry.getsentryamount() + "' on entry number " + entry.getsentrynumber() + " to SQL number - " + e1.getMessage());
				}
				
				//Now add one detail for the entry itself - this is the 'control' side, normally, for payments, to the CASH account:
	    		try {
					export.addDetail(
						datDocDate,
						bdEntryCost, //Costs on payments NORMALLY need to go into the GL as a NEGATIVE, which is what payment entry amounts are normally:
						entry.getscontrolacct(),
						sEntryComment,
						sEntryDesc,
						sEntryReference,
						"0",
						conn
					);
				} catch (Exception e) {
					throw new Exception("Error:[1494535703] adding export detail from ENTRY details - "
							+ e.getMessage());
				}
    		}
    		
    		//Now add details for each of the lines:
    		for (int j = 0; j < entry.getLineArray().size(); j ++){
    			//Now add each line from the entry as a GL transaction:
    			APBatchEntryLine line = entry.getLineArray().get(j);
    			//Here we want to note the apply-to invoice date so we have to read the aptransactions table for each line:
    			String sApplyToDocDate = "N/A";
    			SQL = "SELECT"
    				+ " " + SMTableaptransactions.datdocdate
    				+ " FROM " + SMTableaptransactions.TableName
    				+ " WHERE ("
    					+ "(" + SMTableaptransactions.lid + " = " + line.getslapplytodocid() + ")"
    				+ ")"
    			;
    			try {
					ResultSet rsApplyToTransaction = clsDatabaseFunctions.openResultSet(SQL, conn);
					if (rsApplyToTransaction.next()){
						sApplyToDocDate = clsDateAndTimeConversions.resultsetDateStringToFormattedString(
							rsApplyToTransaction.getString(SMTableaptransactions.datdocdate), 
							SMUtilities.DATE_FORMAT_FOR_DISPLAY, 
							SMUtilities.EMPTY_DATE_VALUE
						);
					}
					rsApplyToTransaction.close();
				} catch (Exception e2) {
					throw new Exception("Error [1510606490] reading apply-to document date with SQL '" + SQL + "' - " + e2.getMessage());
				}
    			
        		String sLineDesc = entry.getsvendoracct() + " " + entry.getsvendorname();
        		if (sLineDesc.length() > SMTableglexportdetails.sdetailtransactiondescriptionlength){
        			sLineDesc = sLineDesc.substring(0, SMTableglexportdetails.sdetailtransactiondescriptionlength - 1).trim();
        		}
           		String sLineReference = "IN " + line.getsapplytodocnumber() + " " + sApplyToDocDate + " CK " + entry.getschecknumber() + " " + entry.getsdatdocdate();
        		if (sLineReference.length() > SMTableglexportdetails.sdetailtransactionreferencelength){
        			sLineReference = sLineReference.substring(0, SMTableglexportdetails.sdetailtransactionreferencelength - 1).trim();
        		}
          		String sLineComment = line.getsdescription();
        		if (sLineComment.length() > SMTableglexportdetails.sdetailcommentlength){
        			sLineComment = sLineComment.substring(0, SMTableglexportdetails.sdetailcommentlength - 1).trim();
        		}
        		//Add a GL Entry to the distribution account (typically AP for payment lines):
        		BigDecimal bdLineCost;
				try {
					bdLineCost = new BigDecimal(line.getsbdamount().trim().replace(",", ""));
				} catch (Exception e1) {
					throw new Exception("Error [1493315935] - cannot convert line.getsbdamount() '" 
						+ line.getsbdamount() + "' on line " + Integer.toString((j + 1)) + ", entry number " + entry.getsentrynumber() + " to decimal - " + e1.getMessage());
				}

        		//Add a GL Entry for each line - the balancing account ('distribution' or 'expense' account) to the GL detail for the entry (above)
				
				//If the line is an apply-to, the line amt DOES NOT get REVERSED.  But if it is any kind of actual payment, it DOES:
				if(entry.getientrytype() != SMTableapbatchentries.ENTRY_TYPE_PAYMENT_APPLYTO){
					bdLineCost = bdLineCost.negate();
				}
				
    			try {
					export.addDetail(
						datDocDate,
						//Line Costs need to be reversed from (normally) negative to positive to come in correctly as 'debit' or 'credit' (they 
						//should normally appear in the GL export file as POSITIVE numbers
						bdLineCost,
						line.getsdistributionacct(),
						sLineComment,
						sLineDesc,
						sLineReference,
						line.getslinenumber(),
						conn
					);
				} catch (Exception e) {
					throw new Exception("Error:[1489780217] adding export detail from LINE details - "
						+ e.getMessage());
				}
    			
    			// Handle discounts:
    			
    			//IF any lines include discounts applied, these need separate GL transactions.
    			// The transactions look like this:
    			// 1) the discount taken creates a Debit to Accounts Payable for the discount amt taken AND
    			// 2) a Credit to a 'Discounts Taken' GL account for the discount amt
    			
        		BigDecimal bdDiscountAmtApplied;
				try {
					bdDiscountAmtApplied = new BigDecimal(line.getsbddiscountappliedamt().trim().replace(",", ""));
				} catch (Exception e1) {
					throw new Exception("Error [1498151750] - cannot convert line.getsbddiscountappliedamt() '" 
						+ line.getsbddiscountappliedamt() + "' on line " + Integer.toString((j + 1)) + ", entry number " + entry.getsentrynumber() + " to decimal - " + e1.getMessage());
				}
    			if (bdDiscountAmtApplied.compareTo(BigDecimal.ZERO) != 0){  //Applied discounts are negative
    				
            		//Add a GL Entry to the 'Discounts Taken' and 'Accounts Payable' accounts:
    				//Debit the AP account:
            		//Add a GL Entry to reduce the AP liability (control) account:
        			try {
    					export.addDetail(
							datDocDate,
							bdDiscountAmtApplied.negate(), //Discount line costs need to be reversed to come in correctly as 'debit' or 'credit'
							sAccountsPayableAcct,
							sLineComment,
							sLineDesc,
							sLineReference,
							line.getslinenumber(),
							conn
						);
    				} catch (Exception e) {
    					throw new Exception("Error:[1498154151] adding export detail from LINE details for applied discount - "
    						+ e.getMessage());
    				}
        			
        			//Credit the discounts taken account:
            		//Now add a GL entry to the 'Discounts Taken' account:
        			try {
    					export.addDetail(
    						datDocDate,
							bdDiscountAmtApplied, //Discount amt applied (normally negative) do NOT need to be reversed here to come in correctly as 'debit' or 'credit'
							sDiscountTakenAcct,
							sLineComment,
							sLineDesc,
							sLineReference,
							line.getslinenumber(),
							conn
							);
    				} catch (Exception e) {
    					throw new Exception("Error:[1498154152] adding export detail from LINE details for applied discount - "
    						+ e.getMessage());
    				}
    			}
    		}
    	}
    	return;
    }
    private void createGLBatchForReversalBatchType(SMGLExport export, Connection conn, String sUserID) throws Exception{
    	
    	for (int i = 0; i < m_arrBatchEntries.size(); i++){
			APBatchEntry entry = m_arrBatchEntries.get(i);
			String sEntryTransactionID = "";
			try {
				sEntryTransactionID = getAPTransactionID(m_arrBatchEntries.get(i).getslid(), conn);
			} catch (Exception e3) {
				throw new Exception("Error [20192271449263] " + e3.getMessage());
			}
    		export.addHeader(
    			SMModuleTypes.AP, 
    			SMTableapbatches.getBatchSourceTypeLabels(Integer.parseInt(getsbatchtype())),
    			"AP Batch Export", 
    			"SMAP",
    			entry.getsdatdocdate(),
    			entry.getsdatentrydate(),
    			buildGLTransactionEntryDescription(entry),
    			sEntryTransactionID
    		);
    		
//    		java.sql.Date datEntry = null;
//			try {
//				datEntry = SMUtilities.StringTojavaSQLDate(SMUtilities.DATE_FORMAT_FOR_DISPLAY, entry.getsdatentrydate());
//			} catch (Exception e) {
//				throw new Exception("Error [1511972441] - cannot convert entry date '" 
//					+ entry.getsdatentrydate() + "' to SQL date - " + e.getMessage());
//			}
			
    		java.sql.Date datDocDate = null;
			try {
				datDocDate = clsDateAndTimeConversions.StringTojavaSQLDate(SMUtilities.DATE_FORMAT_FOR_DISPLAY, entry.getsdatdocdate());
			} catch (Exception e) {
				throw new Exception("Error [1494535111] - cannot convert document date '" 
					+ entry.getsdatdocdate() + "' to SQL date - " + e.getMessage());
			}
			
			//Get the GL accts we might need from the AP Account set for the vendor:
    		String sDiscountTakenAcct = "";
    		String sAccountsPayableAcct = "";
    		String sPrePayAcct = "";
    		String SQL = "SELECT"
    			+ " " + SMTableapaccountsets.TableName + "." + SMTableapaccountsets.spurchasediscountacct
    			+ ", " + SMTableapaccountsets.TableName + "." + SMTableapaccountsets.spayablescontrolacct
    			+ ", " + SMTableapaccountsets.TableName + "." + SMTableapaccountsets.sprepaymentacct
    			+ " FROM " + SMTableicvendors.TableName
    			+ " LEFT JOIN " + SMTableapaccountsets.TableName
    			+ " ON " + SMTableapaccountsets.TableName + "." + SMTableapaccountsets.lid + " = "
    				+ SMTableicvendors.iapaccountset
    			+ " WHERE ("
    				+ "(" + SMTableicvendors.TableName + "." + SMTableicvendors.svendoracct + " = '" + entry.getsvendoracct() + "')"
    			+ ")"
    		;
    		try {
				ResultSet rsDiscountAcct = clsDatabaseFunctions.openResultSet(SQL, conn);
				if (rsDiscountAcct.next()){
					sDiscountTakenAcct = rsDiscountAcct.getString(SMTableapaccountsets.TableName + "." + SMTableapaccountsets.spurchasediscountacct);
					if (sDiscountTakenAcct == null){
						sDiscountTakenAcct = "";
					}
					sAccountsPayableAcct = rsDiscountAcct.getString(SMTableapaccountsets.TableName + "." + SMTableapaccountsets.spayablescontrolacct);
					if (sAccountsPayableAcct == null){
						sAccountsPayableAcct = "";
					}
					sPrePayAcct = rsDiscountAcct.getString(SMTableapaccountsets.TableName + "." + SMTableapaccountsets.sprepaymentacct);
					if (sPrePayAcct == null){
						sPrePayAcct = "";
					}
				}
				rsDiscountAcct.close();
			} catch (Exception e1) {
				throw new Exception("Error:[1511972442] reading Discounts Taken GL for vendor '" + entry.getsvendoracct() + "'"
					+ " with SQL: " + SQL + e1.getMessage());
			}
    		
    		if (sDiscountTakenAcct.compareTo("") == 0){
    			throw new Exception("Error:[1511972443] - could not get Discounts Taken GL Account for vendor '" + entry.getsvendoracct() + "'.");
    		}
			
    		if (sAccountsPayableAcct.compareTo("") == 0){
    			throw new Exception("Error:[1511972444] - could not get Accounts Payable GL Account for vendor '" + entry.getsvendoracct() + "'.");
    		}
			
       		String sEntryDesc = entry.getsvendoracct() + " " + entry.getsvendorname();
    		if (sEntryDesc.length() > SMTableglexportdetails.sdetailtransactiondescriptionlength){
    			sEntryDesc = sEntryDesc.substring(0, SMTableglexportdetails.sdetailtransactiondescriptionlength - 1).trim();
    		}
       		String sEntryReference = entry.getsdocnumber() + " " + entry.getsdatdocdate() + " ORIG CK# " + entry.getschecknumber();
    		if (sEntryReference.length() > SMTableglexportdetails.sdetailtransactionreferencelength){
    			sEntryReference = sEntryReference.substring(0, SMTableglexportdetails.sdetailtransactionreferencelength - 1).trim();
    		}
       		String sEntryComment = entry.getsentrydescription();
    		if (sEntryComment.length() > SMTableglexportdetails.sdetailcommentlength){
    			sEntryComment = sEntryComment.substring(0, SMTableglexportdetails.sdetailcommentlength - 1).trim();
    		}
    		//Add a GL Entry for the control account (typically CASH for reversals):
    		
    		//For reversals, the entry amount is a POSITIVE number:
    		BigDecimal bdEntryCost;
			try {
				bdEntryCost = new BigDecimal(entry.getsentryamount().trim().replace(",", ""));
			} catch (Exception e1) {
				throw new Exception("Error [1511972445] - cannot convert entry.getsentryamount()() '" 
					+ entry.getsentryamount() + "' on entry number " + entry.getsentrynumber() + " to SQL number - " + e1.getMessage());
			}
			
			//Now add one detail for the entry itself - this is the 'control' side, normally, for reversals, to the CASH account:
    		try {
				export.addDetail(
					datDocDate,
					bdEntryCost, //Costs on reversals NORMALLY need to go into the GL as a POSITIVE, which is what reversal entry amounts are normally:
					entry.getscontrolacct(),
					sEntryComment,
					sEntryDesc,
					sEntryReference,
					"0",
					conn
				);
			} catch (Exception e) {
				throw new Exception("Error:[1511972446] adding export detail from ENTRY details - "
						+ e.getMessage());
			}
    		
    		//Now add details for each of the lines:
    		for (int j = 0; j < entry.getLineArray().size(); j ++){
    			//Now add each line from the entry as a GL transaction:
    			APBatchEntryLine line = entry.getLineArray().get(j);
    			
    			//Here we want to note the apply-to invoice date so we have to read the aptransactions table for each line:
    			String sApplyToDocDate = "N/A";
    			SQL = "SELECT"
    				+ " " + SMTableaptransactions.datdocdate
    				+ " FROM " + SMTableaptransactions.TableName
    				+ " WHERE ("
    					+ "(" + SMTableaptransactions.lid + " = " + line.getslapplytodocid() + ")"
    				+ ")"
    			;
    			try {
					ResultSet rsApplyToTransaction = clsDatabaseFunctions.openResultSet(SQL, conn);
					if (rsApplyToTransaction.next()){
						sApplyToDocDate = clsDateAndTimeConversions.resultsetDateStringToFormattedString(
							rsApplyToTransaction.getString(SMTableaptransactions.datdocdate), 
							SMUtilities.DATE_FORMAT_FOR_DISPLAY, 
							SMUtilities.EMPTY_DATE_VALUE
						);
					}
					rsApplyToTransaction.close();
				} catch (Exception e2) {
					throw new Exception("Error [1511972447] reading apply-to document date with SQL '" + SQL + "' - " + e2.getMessage());
				}
    			
        		String sLineDesc = entry.getsvendoracct() + " " + entry.getsvendorname();
        		if (sLineDesc.length() > SMTableglexportdetails.sdetailtransactiondescriptionlength){
        			sLineDesc = sLineDesc.substring(0, SMTableglexportdetails.sdetailtransactiondescriptionlength - 1).trim();
        		}
           		
           		String sLineReference = "IN " + line.getsapplytodocnumber() + " " + sApplyToDocDate + " DOC " + entry.getsdocnumber() + " " + entry.getsdatdocdate() + " ORIG CK# " + entry.getschecknumber();
        		if (sLineReference.length() > SMTableglexportdetails.sdetailtransactionreferencelength){
        			sLineReference = sLineReference.substring(0, SMTableglexportdetails.sdetailtransactionreferencelength - 1).trim();
        		}
        		
          		String sLineComment = line.getsdescription();
        		if (sLineComment.length() > SMTableglexportdetails.sdetailcommentlength){
        			sLineComment = sLineComment.substring(0, SMTableglexportdetails.sdetailcommentlength - 1).trim();
        		}
        		//Add a GL Entry to the distribution account (typically AP for reversals):
        		BigDecimal bdLineCost;
				try {
					bdLineCost = new BigDecimal(line.getsbdamount().trim().replace(",", ""));
				} catch (Exception e1) {
					throw new Exception("Error [1511972448] - cannot convert line.getsbdamount() '" 
						+ line.getsbdamount() + "' on line " + Integer.toString((j + 1)) + ", entry number " + entry.getsentrynumber() + " to decimal - " + e1.getMessage());
				}

        		//Add a GL Entry for each line - the balancing account ('distribution' or 'expense' account) to the GL detail for the entry (above)
    			try {
					export.addDetail(
						datDocDate,
						//Line Costs need to be reversed from (normally) positive to negative to come in correctly as 'debit' or 'credit' (they 
						//should normally appear in the GL export file as NEGATIVE numbers
						bdLineCost.negate(),
						line.getsdistributionacct(),
						sLineComment,
						sLineDesc,
						sLineReference,
						line.getslinenumber(),
						conn
					);
				} catch (Exception e) {
					throw new Exception("Error:[1511972449] adding export detail from LINE details - "
						+ e.getMessage());
				}
    			
    			// Handle discounts:
    			
    			//IF any lines include discounts reversed, these need separate GL transactions.
    			// The transactions look like this:
    			// 1) the discount reversed creates a Credit to Accounts Payable for the discount amt reversed AND
    			// 2) a Debit to a 'Discounts Taken' GL account for the discount amt
    			
        		BigDecimal bdDiscountAmtReversed;
				try {
					bdDiscountAmtReversed = new BigDecimal(line.getsbddiscountappliedamt().trim().replace(",", ""));
				} catch (Exception e1) {
					throw new Exception("Error [1511972450] - cannot convert line.getsbddiscountappliedamt() '" 
						+ line.getsbddiscountappliedamt() + "' on line " + Integer.toString((j + 1)) + ", entry number " + entry.getsentrynumber() + " to decimal - " + e1.getMessage());
				}
    			if (bdDiscountAmtReversed.compareTo(BigDecimal.ZERO) != 0){  //Reversed discounts are positive
    				
            		//Add a GL Entry to the 'Discounts Taken' and 'Accounts Payable' accounts:
            		
    				//Credit the AP account:
            		//Add a GL Entry to increase the AP liability (control) account:
        			try {
    					export.addDetail(
							datDocDate,
							bdDiscountAmtReversed.negate(), //Reversed discount line costs need to be reversed to come in correctly as 'debit' or 'credit'
							sAccountsPayableAcct,
							sLineComment,
							sLineDesc,
							sLineReference,
							line.getslinenumber(),
							conn
							);
    				} catch (Exception e) {
    					throw new Exception("Error:[1511972451] adding export detail from LINE details for applied discount - "
    						+ e.getMessage());
    				}
        			
        			//Credit the discounts taken account:
            		//Now add a GL entry to the 'Discounts Taken' account:
        			try {
    					export.addDetail(
    						datDocDate,
							bdDiscountAmtReversed, //Reversed Discount Amt does NOT get reversed here, to come in correctly as 'debit' or 'credit'
							sDiscountTakenAcct,
							sLineComment,
							sLineDesc,
							sLineReference,
							line.getslinenumber(),
							conn
							);
    				} catch (Exception e) {
    					throw new Exception("Error:[1511972452] adding export detail from LINE details for applied discount - "
    						+ e.getMessage());
    				}
    			}
    		}
    	}
    	return;
    }
    private void addAutoPrepaysToGLBatch(SMGLExport export, Connection conn, ArrayList<Long>arrMatchingLineIDSOfAutoAppliedPrePays) throws Exception{
    	
    	for (int i = 0; i < arrMatchingLineIDSOfAutoAppliedPrePays.size(); i++){
    		
    		//First, get the AP matching line record that was created by the pre-pay, and is pointing TO the 'apply-to' document (invoice):
    		String SQL = "SELECT * FROM " + SMTableapmatchinglines.TableName
    			+ " WHERE ("
    				+ "(" + SMTableapmatchinglines.lid + " = " + arrMatchingLineIDSOfAutoAppliedPrePays.get(i) + ")"
    			+ ")"
    		;
    		try {
    			ResultSet rsPrepays = clsDatabaseFunctions.openResultSet(SQL, conn);
    			if (rsPrepays.next()){
					//Add a GL Entry to the 'Discounts Taken' and 'Accounts Payable' accounts:
					String sDiscountTakenAcct = "";
					String sAccountsPayableAcct = "";
					String sPrePayLiabilityAccount = "";
					SQL = "SELECT"
						+ " " + SMTableapaccountsets.TableName + "." + SMTableapaccountsets.spurchasediscountacct
						+ ", " + SMTableapaccountsets.TableName + "." + SMTableapaccountsets.spayablescontrolacct
						+ ", " + SMTableapaccountsets.TableName + "." + SMTableapaccountsets.sprepaymentacct
						+ " FROM " + SMTableicvendors.TableName
						+ " LEFT JOIN " + SMTableapaccountsets.TableName
						+ " ON " + SMTableapaccountsets.TableName + "." + SMTableapaccountsets.lid + " = "
							+ SMTableicvendors.iapaccountset
						+ " WHERE ("
							+ "(" + SMTableicvendors.TableName + "." + SMTableicvendors.svendoracct + " = '" + rsPrepays.getString(SMTableapmatchinglines.svendor) + "')"
						+ ")"
					;
					try {
						ResultSet rsDiscountAcct = clsDatabaseFunctions.openResultSet(SQL, conn);
						if (rsDiscountAcct.next()){
							sDiscountTakenAcct = rsDiscountAcct.getString(SMTableapaccountsets.TableName + "." + SMTableapaccountsets.spurchasediscountacct);
							if (sDiscountTakenAcct == null){
								sDiscountTakenAcct = "";
							}
							sAccountsPayableAcct = rsDiscountAcct.getString(SMTableapaccountsets.TableName + "." + SMTableapaccountsets.spayablescontrolacct);
							if (sAccountsPayableAcct == null){
								sAccountsPayableAcct = "";
							}
							sPrePayLiabilityAccount = rsDiscountAcct.getString(SMTableapaccountsets.TableName + "." + SMTableapaccountsets.sprepaymentacct);
							if (sPrePayLiabilityAccount == null){
								sPrePayLiabilityAccount = "";
							}
						}
						rsDiscountAcct.close();
					} catch (Exception e1) {
						rsPrepays.close();
						throw new Exception("Error:[1498156002] reading Discounts Taken GL for vendor '" + rsPrepays.getString(SMTableapmatchinglines.svendor) + "'"
							+ " with SQL: " + SQL + e1.getMessage());
					}
					
					if (sDiscountTakenAcct.compareTo("") == 0){
						rsPrepays.close();
						throw new Exception("Error:[1498156001] - could not get Discounts Taken GL Account for vendor '" + rsPrepays.getString(SMTableapmatchinglines.svendor) + "'.");
					}
					
					if (sAccountsPayableAcct.compareTo("") == 0){
						rsPrepays.close();
						throw new Exception("Error:[1498156002] - could not get Accounts Payable GL Account for vendor '" + rsPrepays.getString(SMTableapmatchinglines.svendor) + "'.");
					}
					
					if (sPrePayLiabilityAccount.compareTo("") == 0){
						rsPrepays.close();
						throw new Exception("Error:[1498157002] - could not get PrePay Liability GL Account for vendor '" + rsPrepays.getString(SMTableapmatchinglines.svendor) + "'.");
					}
					
			    	/*
			    	We create the GL transactions for automatically applied pre-pays here.
			    	
			    	Example: a $100.00 pre-pay, and it is automatically taking a $10.00 discount:
			    	
			    	1) This will create a DEBIT to the pre-pay liability account - $100.00 (the amount in the pre-pay liability from this pre-pay should go to zero)
			    	
			    	2) A DEBIT to the discount taken account for 10.00
			    	
			    	3) A CREDIT to the AP control account, for 110.00 (100.00 PLUS 10.00)
			    	
			    	If there is a discount taken, it will also debit the pre-pay liability account, and credit the 'discounts taken' account for that amount.
			    	
			    	*/
					
					//Credit the pre-pay liability account:
					String sLineDesc = "Applied from Pre-Pay";
					if (sLineDesc.length() > SMTableglexportdetails.sdetailtransactiondescriptionlength){
						sLineDesc = sLineDesc.substring(0, SMTableglexportdetails.sdetailtransactiondescriptionlength - 1).trim();
					}
			   		String sLineReference = "Applying from PrePay Doc #: " + rsPrepays.getString(SMTableapmatchinglines.sappliedfromdocnumber);
					if (sLineReference.length() > SMTableglexportdetails.sdetailtransactionreferencelength){
						sLineReference = sLineReference.substring(0, SMTableglexportdetails.sdetailtransactionreferencelength - 1).trim();
					}
			
					try {
						export.addDetail(
							rsPrepays.getDate(SMTableapmatchinglines.dattransactiondate),
							rsPrepays.getBigDecimal(SMTableapmatchinglines.bdappliedamount).negate(), //Costs need to be reversed to come in correctly as 'debit' or 'credit'
							sPrePayLiabilityAccount,
							"Applied to doc # " + rsPrepays.getString(SMTableapmatchinglines.sappliedtodocnumber),
							sLineDesc,
							sLineReference,
							"0",
							conn
							);
					} catch (Exception e) {
						rsPrepays.close();
						throw new Exception("Error:[1498235072] adding export detail from LINE details for applied discount - "
							+ e.getMessage());
					}
							
					//Credit the discounts taken account:
					sLineDesc = "Applied Discount from Pre-Pay";
					if (sLineDesc.length() > SMTableglexportdetails.sdetailtransactiondescriptionlength){
						sLineDesc = sLineDesc.substring(0, SMTableglexportdetails.sdetailtransactiondescriptionlength - 1).trim();
					}
			   		sLineReference = "Applying from PrePay Doc #: " + rsPrepays.getString(SMTableapmatchinglines.sappliedfromdocnumber);
					if (sLineReference.length() > SMTableglexportdetails.sdetailtransactionreferencelength){
						sLineReference = sLineReference.substring(0, SMTableglexportdetails.sdetailtransactionreferencelength - 1).trim();
					}
			
					try {
						export.addDetail(
							rsPrepays.getDate(SMTableapmatchinglines.dattransactiondate),
							rsPrepays.getBigDecimal(SMTableapmatchinglines.bddiscountappliedamount).negate(), //Costs need to be reversed to come in correctly as 'debit' or 'credit'
							sDiscountTakenAcct,
							"Applied to doc # " + rsPrepays.getString(SMTableapmatchinglines.sappliedtodocnumber),
							sLineDesc,
							sLineReference,
							"0",
							conn
							);
					} catch (Exception e) {
						rsPrepays.close();
						throw new Exception("Error:[1498235072] adding export detail from LINE details for applied discount - "
							+ e.getMessage());
					}
							
					//Debit the AP account:
			   		sLineDesc = "Applied Discount from Pre-Pay";
					if (sLineDesc.length() > SMTableglexportdetails.sdetailtransactiondescriptionlength){
						sLineDesc = sLineDesc.substring(0, SMTableglexportdetails.sdetailtransactiondescriptionlength - 1).trim();
					}
			   		sLineReference = "Applying TO Doc #: " + rsPrepays.getString(SMTableapmatchinglines.sappliedtodocnumber);
					if (sLineReference.length() > SMTableglexportdetails.sdetailtransactionreferencelength){
						sLineReference = sLineReference.substring(0, SMTableglexportdetails.sdetailtransactionreferencelength - 1).trim();
					}
			
					//Add a GL Entry to reduce the AP liability (control) account:
					try {
						export.addDetail(
								rsPrepays.getDate(SMTableapmatchinglines.dattransactiondate),
								rsPrepays.getBigDecimal(SMTableapmatchinglines.bdappliedamount).add(rsPrepays.getBigDecimal(SMTableapmatchinglines.bddiscountappliedamount)),
								sAccountsPayableAcct,
								"Applied to doc # " + rsPrepays.getString(SMTableapmatchinglines.sappliedtodocnumber),
								sLineDesc,
								sLineReference,
								"0",
								conn
								);
					} catch (Exception e) {
						throw new Exception("Error:[1498235071] adding export detail from Pre-pay for applied discount - "
							+ e.getMessage());
					}
    			}
				rsPrepays.close();
			} catch (Exception e2) {
				throw new Exception("Error [1498234167] reading prepay matching line with ID '" + Long.toString(arrMatchingLineIDSOfAutoAppliedPrePays.get(i)) + "' - " + e2.getMessage());
			}
		}
    }
    private void createEntryTransactions(SMLogEntry log, String sUserID, Connection conn) throws Exception{
    	if (bDebugMode){
	    	log.writeEntry(
	        		sUserID, 
	        		SMLogEntry.LOG_OPERATION_APBATCHPOST, 
	        		"In post_without_data_transaction Batch #:" + getsbatchnumber()
	        		+ " Going into createTransactions",
	        		"",
	        		"[1489707496]"
	        );
    	}
    	for (int i = 0; i < m_arrBatchEntries.size(); i++){
			APBatchEntry entry = m_arrBatchEntries.get(i);
    		createIndividualTransaction(
    			entry, 
    			conn
    		);
    	}
    }
    private void createIndividualTransaction(
    		APBatchEntry entry,
    		Connection conn) throws Exception{
    	
    	//If it's an invoice batch, create a new invoice transaction:
    	if (getsbatchtype().compareToIgnoreCase(Integer.toString(SMTableapbatches.AP_BATCH_TYPE_INVOICE)) == 0){
    		createTransactionForInvBatchType(entry, conn);
    	}
    	
    	//If it's a payment batch, create a new payment transaction:
    	if (getsbatchtype().compareToIgnoreCase(Integer.toString(SMTableapbatches.AP_BATCH_TYPE_PAYMENT)) == 0){
    		createTransactionForPmtBatchType(entry, conn);
    	}
    	
    	//If it's a reversal batch, create a new reversal transaction:
    	if (getsbatchtype().compareToIgnoreCase(Integer.toString(SMTableapbatches.AP_BATCH_TYPE_REVERSALS)) == 0){
    		createTransactionForReversalBatchType(entry, conn);
    	}
    	
    	//Insert/update a record in the statistics table:
    	
    	//Used only for testing:
    	//if(true){throw new Exception("TESTING");}
    	updateStatistics(entry, conn);
 
    }
    private void createTransactionForInvBatchType(APBatchEntry entry, Connection conn) throws Exception{
       	//Create a new transaction in the aptransactions table:
    	String SQL = "INSERT INTO"
    		+ " " + SMTableaptransactions.TableName
    		+ " ("
    		+ " " + SMTableaptransactions.datdiscountdate
    		+ ", " + SMTableaptransactions.datdocdate
    		+ ", " + SMTableaptransactions.datduedate
    		+ ", " + SMTableaptransactions.bdcurrentamt
    		+ ", " + SMTableaptransactions.bdcurrentdiscountavailable
    		+ ", " + SMTableaptransactions.bdoriginaldiscountavailable
    		+ ", " + SMTableaptransactions.bdoriginalamt
    		+ ", " + SMTableaptransactions.idoctype
    		+ ", " + SMTableaptransactions.iinvoiceincludestax
    		+ ", " + SMTableaptransactions.ionhold
    		+ ", " + SMTableaptransactions.lapplytopurchaseorderid
    		+ ", " + SMTableaptransactions.lapplytosalesorderid
    		+ ", " + SMTableaptransactions.lbatchentryid
    		+ ", " + SMTableaptransactions.loriginalbatchnumber
    		+ ", " + SMTableaptransactions.loriginalentrynumber
    		+ ", " + SMTableaptransactions.sapplytoinvoicenumber
    		+ ", " + SMTableaptransactions.scontrolacct
    		+ ", " + SMTableaptransactions.sdocdescription
    		+ ", " + SMTableaptransactions.sdocnumber
    		+ ", " + SMTableaptransactions.svendor
    		+ ", " + SMTableaptransactions.staxjurisdiction
    		+ ", " + SMTableaptransactions.itaxid
    		+ ", " + SMTableaptransactions.bdtaxrate
    		+ ", " + SMTableaptransactions.staxtype
    		+ ", " + SMTableaptransactions.icalculateonpurchaseorsale
    		+ ", " + SMTableaptransactions.sonholdbyfullname
    		+ ", " + SMTableaptransactions.lonholdbyuserid
    		+ ", " + SMTableaptransactions.datplacedonhold
    		+ ", " + SMTableaptransactions.monholdreason
    		+ ", " + SMTableaptransactions.lonholdpoheaderid
    		
    		+ ") VALUES ("
    		+ " '" + clsDateAndTimeConversions.convertDateFormat(entry.getsdatdiscount(), SMUtilities.DATE_FORMAT_FOR_DISPLAY, SMUtilities.DATE_FORMAT_FOR_SQL, SMUtilities.EMPTY_SQL_DATE_VALUE) + "'"
    		+ ", '" + clsDateAndTimeConversions.convertDateFormat(entry.getsdatdocdate(), SMUtilities.DATE_FORMAT_FOR_DISPLAY, SMUtilities.DATE_FORMAT_FOR_SQL, SMUtilities.EMPTY_SQL_DATE_VALUE) + "'"
    		+ ", '" + clsDateAndTimeConversions.convertDateFormat(entry.getsdatduedate(), SMUtilities.DATE_FORMAT_FOR_DISPLAY, SMUtilities.DATE_FORMAT_FOR_SQL, SMUtilities.EMPTY_SQL_DATE_VALUE) + "'"
    		+ ", " + entry.getsentryamount().replace(",", "")
    		+ ", " + entry.getsdiscountamt().replace(",", "")
    		+ ", " + entry.getsdiscountamt().replace(",", "")
    		+ ", " + entry.getsentryamount().replace(",", "")
    		+ ", " + entry.getsentrytype()
    		+ ", " + entry.getsiinvoiceincludestax()
    		+ ", " + entry.getsionhold()
    		+ ", " + entry.getslpurchaseordernumber()
    		+ ", " + entry.getslsalesordernumber()
    		+ ", " + entry.getslid()
    		+ ", " + entry.getsbatchnumber()
    		+ ", " + entry.getsentrynumber()
    		+ ", '" + clsDatabaseFunctions.FormatSQLStatement(entry.getsapplytoinvoicenumber()) + "'"
    		+ ", '" + clsDatabaseFunctions.FormatSQLStatement(entry.getscontrolacct()) + "'"
    		+ ", '" + clsDatabaseFunctions.FormatSQLStatement(entry.getsentrydescription()) + "'"
    		+ ", '" + clsDatabaseFunctions.FormatSQLStatement(entry.getsdocnumber()) + "'"
    		+ ", '" + clsDatabaseFunctions.FormatSQLStatement(entry.getsvendoracct()) + "'"
    		+ ", '" + clsDatabaseFunctions.FormatSQLStatement(entry.getstaxjurisdiction()) + "'"
    		+ ", " + entry.getsitaxid()
    		+ ", " + entry.getsbdtaxrate()
    		+ ", '" + clsDatabaseFunctions.FormatSQLStatement(entry.getstaxtype()) + "'"
    		+ ", " + entry.getsicalculateonpurchaseorsale()
    		+ ", '" + clsDatabaseFunctions.FormatSQLStatement(entry.getsonholdbyfullname()) + "'"
    		+ ", " + entry.getsonholdbyuserid()
    		+ ", '" + clsDateAndTimeConversions.stdDateTimeToSQLDateTimeInSecondsString(entry.getsdatonhold()) + "'"
    		+ ", '" + entry.getsonholdreason() + "'"
    		+ ", " + entry.getsonholdpoheaderid()
    		+ ")"
    	;

    	try {
			Statement stmt = conn.createStatement();
			stmt.execute(SQL);
		} catch (Exception e) {
			throw new Exception("Error [1489769672] inserting transaction for entry number " + entry.getsentrynumber() + " - " + e.getMessage());
		}
    	
    	long lAppliedFromTransactionID = 0L;
    	
		SQL = "SELECT last_insert_id()";
		try {
			ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
			if (rs.next()) {
				lAppliedFromTransactionID = rs.getLong(1);
			}else 
			rs.close();
		} catch (SQLException e) {
			throw new Exception("Error [1491263777] Could not get last ID number - " + e.getMessage());
		}
		if(lAppliedFromTransactionID == 0L){
			throw new Exception("Error [1491263778] Could not get last ID number record.");
		}

    	try {
    		createTransactionLines(conn, entry, lAppliedFromTransactionID);
		} catch (Exception e) {
			throw new Exception("Error [1496158038] creating transaction lines for entry number " + entry.getsentrynumber() + " - " + e.getMessage());
		}
		
    	try {
    		createMatchingLinesForInvBatchType(conn, entry, lAppliedFromTransactionID);
		} catch (Exception e) {
			throw new Exception("Error [1491263677] creating matching line transactions for entry number " + entry.getsentrynumber() + " - " + e.getMessage());
		}
    	
    	return;
    }
    
    private void createTransactionLines(Connection conn, APBatchEntry entry, long lAppliedFromTransactionID) throws Exception{
    	
    	for (int i = 0; i < entry.getLineArray().size(); i++){
    		APBatchEntryLine line = entry.getLineArray().get(i);
    		String SQL = "INSERT INTO " + SMTableaptransactionlines.TableName + "("
    			+ SMTableaptransactionlines.bdamount
    			+ ", " + SMTableaptransactionlines.bddiscountappliedamount
    			+ ", " + SMTableaptransactionlines.lapplytodocid
    			+ ", " + SMTableaptransactionlines.loriginalbatchnumber
    			+ ", " + SMTableaptransactionlines.loriginalentrynumber
    			+ ", " + SMTableaptransactionlines.loriginallinenumber
    			+ ", " + SMTableaptransactionlines.lpoheaderid
    			+ ", " + SMTableaptransactionlines.lporeceiptlineid
    			+ ", " + SMTableaptransactionlines.lreceiptheaderid
    			+ ", " + SMTableaptransactionlines.ltransactionheaderid
    			+ ", " + SMTableaptransactionlines.sapplytodocnumber
    			+ ", " + SMTableaptransactionlines.scomment
    			+ ", " + SMTableaptransactionlines.sdescription
    			+ ", " + SMTableaptransactionlines.sdistributionacct
    			+ ", " + SMTableaptransactionlines.sdistributioncodename
    			+ ") VALUES ("
    			+ line.getsbdamount().replaceAll(",", "").trim()
    			+ ", " + line.getsbddiscountappliedamt().replaceAll(",", "").trim()
    			+ ", " + line.getslapplytodocid()
    			+ ", " + entry.getsbatchnumber()
    			+ ", " + entry.getsentrynumber()
    			+ ", " + line.getslinenumber()
    			+ ", " + line.getslpoheaderid()
    			+ ", " + line.getslporeceiptlineid()
    			+ ", " + line.getslreceiptheaderid()
    			+ ", " + Long.toString(lAppliedFromTransactionID)
    			+ ", '" + clsDatabaseFunctions.FormatSQLStatement(line.getsapplytodocnumber()) + "'"
    			+ ", '" + clsDatabaseFunctions.FormatSQLStatement(line.getscomment()) + "'"
    			+ ", '" + clsDatabaseFunctions.FormatSQLStatement(line.getsdescription()) + "'"
    			+ ", '" + clsDatabaseFunctions.FormatSQLStatement(line.getsdistributionacct()) + "'"
    			+ ", '" + clsDatabaseFunctions.FormatSQLStatement(line.getsdistributioncodename()) + "'"
    			
    			+ ")"
    		;
    		try {
				Statement stmt = conn.createStatement();
				stmt.execute(SQL);
			} catch (Exception e) {
				throw new Exception("Error [1496158858] inserting AP transaction line with SQL: '" + SQL + "' - " + e.getMessage());
			}
    		
    		//Update the item number, qty received, and unit of measure, if there's a legitimate PO receipt line:
    		if (
    			(line.getslporeceiptlineid().compareToIgnoreCase("-1") != 0)
    			&& (line.getslporeceiptlineid().compareToIgnoreCase("0") != 0)
    				
    		){
    			SQL = "UPDATE " + SMTableaptransactionlines.TableName
    				+ " LEFT JOIN " + SMTableicporeceiptlines.TableName
    				+ " ON " + SMTableaptransactionlines.TableName + "." + SMTableaptransactionlines.lporeceiptlineid
    				+ " = " + SMTableicporeceiptlines.TableName + "." + SMTableicporeceiptlines.lid
    				+ " SET " + SMTableaptransactionlines.TableName + "." + SMTableaptransactionlines.sitemnumber 
    					+ " = " + SMTableicporeceiptlines.TableName + "." + SMTableicporeceiptlines.sitemnumber
    				+ ", " + SMTableaptransactionlines.TableName + "." + SMTableaptransactionlines.bdqtyreceived 
    					+ " = " + SMTableicporeceiptlines.TableName + "." + SMTableicporeceiptlines.bdqtyreceived
       				+ ", " + SMTableaptransactionlines.TableName + "." + SMTableaptransactionlines.sunitofmeasure 
    					+ " = " + SMTableicporeceiptlines.TableName + "." + SMTableicporeceiptlines.sunitofmeasure
       				+ ", " + SMTableaptransactionlines.TableName + "." + SMTableaptransactionlines.sitemdescription 
       					+ " = " + SMTableicporeceiptlines.TableName + "." + SMTableicporeceiptlines.sitemdescription
    					
    				+ " WHERE ("
    					+ "(" + SMTableaptransactionlines.TableName + "." + SMTableaptransactionlines.loriginalbatchnumber
    						+ " = " + entry.getsbatchnumber() + ")"
    					+ " AND (" + SMTableaptransactionlines.TableName + "." + SMTableaptransactionlines.loriginalentrynumber
   	    					+ " = " + entry.getsentrynumber() + ")"
    					+ " AND (" + SMTableaptransactionlines.TableName + "." + SMTableaptransactionlines.loriginallinenumber
   	    					+ " = " + line.getslinenumber() + ")"
    				+ ")"
    			;
    			//System.out.println("[1519678915] - SQL = '" + SQL + "'.");
        		try {
    				Statement stmt = conn.createStatement();
    				stmt.execute(SQL);
    			} catch (Exception e) {
    				throw new Exception("Error [1519678857] updating AP transaction line with SQL: '" + SQL + "' - " + e.getMessage());
    			}
    		}
    	}
    }
    
    private void createTransactionForPmtBatchType(APBatchEntry entry, Connection conn) throws Exception{
      	//Create a new transaction in the aptransactions table:
    	
    	String SQL = "INSERT INTO"
    		+ " " + SMTableaptransactions.TableName
    		+ " ("
    		+ " " + SMTableaptransactions.datdiscountdate
    		+ ", " + SMTableaptransactions.datdocdate
    		+ ", " + SMTableaptransactions.datduedate
    		+ ", " + SMTableaptransactions.bdcurrentamt
    		+ ", " + SMTableaptransactions.bdcurrentdiscountavailable
    		+ ", " + SMTableaptransactions.bdoriginaldiscountavailable
    		+ ", " + SMTableaptransactions.bdoriginalamt
    		+ ", " + SMTableaptransactions.idoctype
    		+ ", " + SMTableaptransactions.ionhold
    		+ ", " + SMTableaptransactions.lapplytopurchaseorderid
    		+ ", " + SMTableaptransactions.lapplytosalesorderid
    		+ ", " + SMTableaptransactions.lbatchentryid
    		+ ", " + SMTableaptransactions.loriginalbatchnumber
    		+ ", " + SMTableaptransactions.loriginalentrynumber
    		+ ", " + SMTableaptransactions.sapplytoinvoicenumber
    		+ ", " + SMTableaptransactions.schecknumber
    		+ ", " + SMTableaptransactions.scontrolacct
    		+ ", " + SMTableaptransactions.sdocdescription
    		+ ", " + SMTableaptransactions.sdocnumber
    		+ ", " + SMTableaptransactions.svendor
    		+ ", " + SMTableaptransactions.sonholdbyfullname
    		+ ", " + SMTableaptransactions.lonholdbyuserid
    		+ ", " + SMTableaptransactions.datplacedonhold
    		+ ", " + SMTableaptransactions.monholdreason
    		+ ", " + SMTableaptransactions.lonholdpoheaderid
    		
    		+ ") VALUES ("
    		+ " '" + clsDateAndTimeConversions.convertDateFormat(entry.getsdatdiscount(), SMUtilities.DATE_FORMAT_FOR_DISPLAY, SMUtilities.DATE_FORMAT_FOR_SQL, SMUtilities.EMPTY_SQL_DATE_VALUE) + "'"
    		+ ", '" + clsDateAndTimeConversions.convertDateFormat(entry.getsdatdocdate(), SMUtilities.DATE_FORMAT_FOR_DISPLAY, SMUtilities.DATE_FORMAT_FOR_SQL, SMUtilities.EMPTY_SQL_DATE_VALUE) + "'"
    		+ ", '" + clsDateAndTimeConversions.convertDateFormat(entry.getsdatduedate(), SMUtilities.DATE_FORMAT_FOR_DISPLAY, SMUtilities.DATE_FORMAT_FOR_SQL, SMUtilities.EMPTY_SQL_DATE_VALUE) + "'"
    		+ ", " + entry.getsentryamount().replaceAll(",", "")  //Current amt
    		+ ", " + entry.getsdiscountamt().replace(",", "")
    		+ ", " + entry.getsdiscountamt().replace(",", "")
    		+ ", " + entry.getsentryamount().replace(",", "")  //Original amt
    		+ ", " + entry.getsentrytype()
    		+ ", 0" //on hold
    		+ ", " + entry.getslpurchaseordernumber()
    		+ ", " + entry.getslsalesordernumber()
    		+ ", " + entry.getslid()
    		+ ", " + entry.getsbatchnumber()
    		+ ", " + entry.getsentrynumber()
    		+ ", '" + clsDatabaseFunctions.FormatSQLStatement(entry.getsapplytoinvoicenumber()) + "'"
			+ ", '" + clsDatabaseFunctions.FormatSQLStatement(entry.getschecknumber()) + "'"
    		+ ", '" + clsDatabaseFunctions.FormatSQLStatement(entry.getscontrolacct()) + "'"
    		+ ", '" + clsDatabaseFunctions.FormatSQLStatement(entry.getsentrydescription()) + "'"
    		+ ", '" + clsDatabaseFunctions.FormatSQLStatement(entry.getsdocnumber()) + "'"
    		+ ", '" + clsDatabaseFunctions.FormatSQLStatement(entry.getsvendoracct()) + "'"
    		+ ", '" + clsDatabaseFunctions.FormatSQLStatement(entry.getsonholdbyfullname()) + "'"
    		+ ", " + entry.getsonholdbyuserid()
    		+ ", '" + clsDateAndTimeConversions.stdDateTimeToSQLDateTimeInSecondsString(entry.getsdatonhold()) + "'"
    		+ ", '" + entry.getsonholdreason() + "'"
    		+ ", " + entry.getsonholdpoheaderid()
    		+ ")"
    	;

    	try {
			Statement stmt = conn.createStatement();
			stmt.execute(SQL);
		} catch (Exception e) {
			throw new Exception("Error [1493155382] inserting transaction for entry number " + entry.getsentrynumber() + " - " + e.getMessage());
		}
    	
    	long lAppliedFromTransactionID = 0L;
    	
		SQL = "SELECT last_insert_id()";
		try {
			ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
			if (rs.next()) {
				lAppliedFromTransactionID = rs.getLong(1);
			}
			rs.close();
		} catch (SQLException e) {
			throw new Exception("Error [1493155383] Could not get last ID number - " + e.getMessage());
		}
		if(lAppliedFromTransactionID == 0L){
			throw new Exception("Error [1493155384] Could not get last ID number record.");
		}

    	try {
    		createTransactionLines(conn, entry, lAppliedFromTransactionID);
		} catch (Exception e) {
			throw new Exception("Error [1497033526] creating transaction lines for entry number " + entry.getsentrynumber() + " - " + e.getMessage());
		}
		
    	try {
    		createMatchingLinesForPmtBatchType(conn, entry, lAppliedFromTransactionID);
		} catch (Exception e) {
			throw new Exception("Error [1493155385] creating matching line transactions for entry number " + entry.getsentrynumber() + " - " + e.getMessage());
		}
    	
    	return;
    }

    private void createTransactionForReversalBatchType(APBatchEntry entry, Connection conn) throws Exception{
      	//Create a new transaction in the aptransactions table:
    	
    	String SQL = "INSERT INTO"
    		+ " " + SMTableaptransactions.TableName
    		+ " ("
    		+ " " + SMTableaptransactions.datdiscountdate
    		+ ", " + SMTableaptransactions.datdocdate
    		+ ", " + SMTableaptransactions.datduedate
    		+ ", " + SMTableaptransactions.bdcurrentamt
    		+ ", " + SMTableaptransactions.bdcurrentdiscountavailable
    		+ ", " + SMTableaptransactions.bdoriginaldiscountavailable
    		+ ", " + SMTableaptransactions.bdoriginalamt
    		+ ", " + SMTableaptransactions.idoctype
    		+ ", " + SMTableaptransactions.ionhold
    		+ ", " + SMTableaptransactions.lapplytopurchaseorderid
    		+ ", " + SMTableaptransactions.lapplytosalesorderid
    		+ ", " + SMTableaptransactions.lbatchentryid
    		+ ", " + SMTableaptransactions.loriginalbatchnumber
    		+ ", " + SMTableaptransactions.loriginalentrynumber
    		+ ", " + SMTableaptransactions.sapplytoinvoicenumber
    		+ ", " + SMTableaptransactions.schecknumber
    		+ ", " + SMTableaptransactions.scontrolacct
    		+ ", " + SMTableaptransactions.sdocdescription
    		+ ", " + SMTableaptransactions.sdocnumber
    		+ ", " + SMTableaptransactions.svendor
    		+ ", " + SMTableaptransactions.sonholdbyfullname
    		+ ", " + SMTableaptransactions.lonholdbyuserid
    		+ ", " + SMTableaptransactions.datplacedonhold
    		+ ", " + SMTableaptransactions.monholdreason
    		+ ", " + SMTableaptransactions.lonholdpoheaderid
    		+ ") VALUES ("
    		+ " '" + clsDateAndTimeConversions.convertDateFormat(entry.getsdatdiscount(), SMUtilities.DATE_FORMAT_FOR_DISPLAY, SMUtilities.DATE_FORMAT_FOR_SQL, SMUtilities.EMPTY_SQL_DATE_VALUE) + "'"
    		+ ", '" + clsDateAndTimeConversions.convertDateFormat(entry.getsdatdocdate(), SMUtilities.DATE_FORMAT_FOR_DISPLAY, SMUtilities.DATE_FORMAT_FOR_SQL, SMUtilities.EMPTY_SQL_DATE_VALUE) + "'"
    		+ ", '" + clsDateAndTimeConversions.convertDateFormat(entry.getsdatduedate(), SMUtilities.DATE_FORMAT_FOR_DISPLAY, SMUtilities.DATE_FORMAT_FOR_SQL, SMUtilities.EMPTY_SQL_DATE_VALUE) + "'"
    		+ ", " + entry.getsentryamount().replaceAll(",", "")  //Current amt
    		+ ", " + entry.getsdiscountamt().replace(",", "")
    		+ ", " + entry.getsdiscountamt().replace(",", "")
    		+ ", " + entry.getsentryamount().replace(",", "")  //Original amt
    		+ ", " + entry.getsentrytype()
    		+ ", 0" //on hold
    		+ ", " + entry.getslpurchaseordernumber()
    		+ ", " + entry.getslsalesordernumber()
    		+ ", " + entry.getslid()
    		+ ", " + entry.getsbatchnumber()
    		+ ", " + entry.getsentrynumber()
    		+ ", '" + clsDatabaseFunctions.FormatSQLStatement(entry.getsapplytoinvoicenumber()) + "'"
    		+ ", '" + clsDatabaseFunctions.FormatSQLStatement(entry.getschecknumber()) + "'"
    		+ ", '" + clsDatabaseFunctions.FormatSQLStatement(entry.getscontrolacct()) + "'"
    		+ ", '" + clsDatabaseFunctions.FormatSQLStatement(entry.getsentrydescription()) + "'"
    		+ ", '" + clsDatabaseFunctions.FormatSQLStatement(entry.getsdocnumber()) + "'"
    		+ ", '" + clsDatabaseFunctions.FormatSQLStatement(entry.getsvendoracct()) + "'"
    		+ ", '" + clsDatabaseFunctions.FormatSQLStatement(entry.getsonholdbyfullname()) + "'"
    		+ ", " + entry.getsonholdbyuserid()
    		+ ", '" + clsDateAndTimeConversions.stdDateTimeToSQLDateTimeInSecondsString(entry.getsdatonhold()) + "'"
    		+ ", '" + entry.getsonholdreason() + "'"
    		+ ", " + entry.getsonholdpoheaderid()
    		+ ")"
    	;

    	try {
			Statement stmt = conn.createStatement();
			stmt.execute(SQL);
		} catch (Exception e) {
			throw new Exception("Error [1511879903] inserting transaction for entry number " + entry.getsentrynumber() + " - " + e.getMessage());
		}
    	
    	long lAppliedFromTransactionID = 0L;
    	
		SQL = "SELECT last_insert_id()";
		try {
			ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
			if (rs.next()) {
				lAppliedFromTransactionID = rs.getLong(1);
			}
			rs.close();
		} catch (SQLException e) {
			throw new Exception("Error [1511879904] Could not get last ID number - " + e.getMessage());
		}
		if(lAppliedFromTransactionID == 0L){
			throw new Exception("Error [1511879905] Could not get last ID number record.");
		}

    	try {
    		createTransactionLines(conn, entry, lAppliedFromTransactionID);
		} catch (Exception e) {
			throw new Exception("Error [1511879906] creating transaction lines for entry number " + entry.getsentrynumber() + " - " + e.getMessage());
		}
		
    	try {
    		createMatchingLinesForReversalBatchType(conn, entry, lAppliedFromTransactionID);
		} catch (Exception e) {
			throw new Exception("Error [1511879907] creating matching line transactions for entry number " + entry.getsentrynumber() + " - " + e.getMessage());
		}
    	
    	return;
    }
    
    private void createMatchingLinesForInvBatchType(Connection conn, APBatchEntry entry, long lAppliedFromTransactionID) throws Exception{
    	
    	//If it's an invoice, then we don't need to create any 'matching' lines:
    	if (
    		(entry.getsentrytype().compareToIgnoreCase(Integer.toString(SMTableapbatchentries.ENTRY_TYPE_INV_INVOICE)) == 0)
    	){
    		return;
    	}
    	
    	//So it's a credit note or a debit note that applies to a real document, so we create matching lines for it here:
    	//If we can't find the AP transaction we are supposed to be applying to, OR the amount we are applying is more than the current amount, exit out:
    	
    	for (int i = 0; i < entry.getLineArray().size(); i++){
    		APBatchEntryLine line = entry.getLineArray().get(i);
    		//If the line doesn't apply to anything, then we don't need to add any 'matching' lines:
    		if (line.getsapplytodocnumber().compareToIgnoreCase("") != 0){
    			
    			//If there IS an 'applying' line, then we insert a matching line to point to the 'apply-to' transaction:
            	String SQL = "INSERT INTO " + SMTableapmatchinglines.TableName + "("
            		+ SMTableapmatchinglines.bdappliedamount
            		+ ", " + SMTableapmatchinglines.bddiscountappliedamount
            		+ ", " + SMTableapmatchinglines.dattransactiondate
            		+ ", " + SMTableapmatchinglines.ltransactionappliedfromid
            		+ ", " + SMTableapmatchinglines.ltransactionappliedtoid
            		+ ", " + SMTableapmatchinglines.sappliedfromdocnumber
            		+ ", " + SMTableapmatchinglines.sappliedtodocnumber
            		+ ", " + SMTableapmatchinglines.sdescription
            		+ ", " + SMTableapmatchinglines.svendor
            			
            		+ ") VALUES ("
            		// We REVERSE the sign of the applying lines - because we want to be able to SUBTRACT the line amount from the applied to amount to REDUCE (or INCREASE, in the case
            		//of a CREDIT) the vendor liability (i.e., the CURRENT AMT on the apply-to transaction:
            		+ line.getsbdamount().replaceAll(",", "") + " * -1"
            		+ ", " + "0.00"  //discount amt
            		+ ", '" + entry.getsdatdocdateInSQLFormat() + "'"  //transaction date
            		+ ", " + Long.toString(lAppliedFromTransactionID)  //ltransactionappliedfromid
            		+ ", " + line.getslapplytodocid() //ltransactionappliedtoid
            		+ ", '" + clsDatabaseFunctions.FormatSQLStatement(entry.getsdocnumber()) + "'"  //sappliedfromdocnumber
            		+ ", '" + clsDatabaseFunctions.FormatSQLStatement(line.getsapplytodocnumber()) + "'"  //sappliedtodocnumber
            		+ ", '" + clsDatabaseFunctions.FormatSQLStatement(line.getsdescription()) + "'" 
            		+ ", '" + entry.getsvendoracct() + "'" //vendor
            		+ ")"
            	;
            	try {
        			Statement stmt = conn.createStatement();
        			stmt.execute(SQL);
        		} catch (Exception e) {
        			throw new Exception("Error [1494951677] inserting matching line for entry number " + entry.getsentrynumber() 
        				+ " to apply to APPLY_TO doc with SQL: " + SQL + " - " + e.getMessage());
        		}
            	
            	//Now update the current amt on the apply-to transaction:
            	SQL = "UPDATE " + SMTableaptransactions.TableName
            		+ " SET " + SMTableaptransactions.bdcurrentamt
            		+ " = (" + SMTableaptransactions.bdcurrentamt + " - (" + line.getsbdamount().replaceAll(",", "") + " * -1))" 
            		+ " WHERE ("
            			+ "(" + SMTableaptransactions.lid + " = " + line.getslapplytodocid() + ")"
            		+ ")"
            	;
            	try {
        			Statement stmt = conn.createStatement();
        			stmt.execute(SQL);
        		} catch (Exception e) {
        			throw new Exception("Error [1494951678] updating current amt on APPLY-TO document with SQL: " + SQL + " - " + e.getMessage());
            	}
            	
                //Now create a matching line pointing back to the APPLY FROM entry, to reduce it's current amount also:
               	SQL = "INSERT INTO " + SMTableapmatchinglines.TableName + "("
            		+ SMTableapmatchinglines.bdappliedamount
            		+ ", " + SMTableapmatchinglines.bddiscountappliedamount
            		+ ", " + SMTableapmatchinglines.dattransactiondate
            		+ ", " + SMTableapmatchinglines.ltransactionappliedfromid
            		+ ", " + SMTableapmatchinglines.ltransactionappliedtoid
            		+ ", " + SMTableapmatchinglines.sappliedfromdocnumber
            		+ ", " + SMTableapmatchinglines.sappliedtodocnumber
            		+ ", " + SMTableapmatchinglines.sdescription
            		+ ", " + SMTableapmatchinglines.svendor
            			
            		+ ") VALUES ("
            		// The sign of the matching lines here remains the same as the original entry line - so that when we SUBTRACT the matching lines from the APPLY FROM entry,
            		//it decreases the original amount of the apply from entry:
            		+ line.getsbdamount().replaceAll(",", "")
            		+ ", " + "0.00"  //discount amt
            		+ ", '" + entry.getsdatdocdateInSQLFormat() + "'"  //transaction date
            		+ ", " + Long.toString(lAppliedFromTransactionID)  //ltransactionappliedfromid
            		+ ", " + Long.toString(lAppliedFromTransactionID) //ltransactionappliedtoid - this matching line APPLIES BACK TO THE ENTRY THAT CREATED IT
            		+ ", '" + clsDatabaseFunctions.FormatSQLStatement(entry.getsdocnumber()) + "'"  //sappliedfromdocnumber
            		+ ", '" + clsDatabaseFunctions.FormatSQLStatement(entry.getsdocnumber()) + "'"  //sappliedtodocnumber
            		+ ", '" + clsDatabaseFunctions.FormatSQLStatement(line.getsdescription()) + "'" 
            		+ ", '" + entry.getsvendoracct() + "'" //vendor
            		+ ")"
            	;
            	try {
        			Statement stmt = conn.createStatement();
        			stmt.execute(SQL);
        		} catch (Exception e) {
        			throw new Exception("Error [1494951679] inserting matching line from entry number " + entry.getsentrynumber() 
        				+ " to apply to APPLY_TO doc with SQL: " + SQL + " - " + e.getMessage());
        		}
            	
            	//Now update the current amt on the applied-FROM transaction:
            	SQL = "UPDATE " + SMTableaptransactions.TableName
            		+ " SET " + SMTableaptransactions.bdcurrentamt
            		+ " = (" + SMTableaptransactions.bdcurrentamt + " - " + line.getsbdamount().trim().replaceAll(",", "") + ")"
            		+ " WHERE ("
            			+ "(" + SMTableaptransactions.lid + " = " + Long.toString(lAppliedFromTransactionID) + ")"
            		+ ")"
            	;
            	
            	try {
        			Statement stmt = conn.createStatement();
        			stmt.execute(SQL);
        		} catch (Exception e) {
        			throw new Exception("Error [1494951680] updating current amt on APPLY-FROM document with SQL: " + SQL + " - " + e.getMessage());
        		}
            	
    		}else{
    			//No applying lines, so we don't have to add any 'matching' lines....
    		}
    	}
    }
    private void createMatchingLinesForPmtBatchType(Connection conn, APBatchEntry entry, long lAppliedFromTransactionID) throws Exception{
    	
    	String SQL = "";
    	
    	//But if it's any OTHER type of payment entry, we need to process the 'apply to doc', etc.:
    	for (int i = 0; i < entry.getLineArray().size(); i++){
    		APBatchEntryLine line = entry.getLineArray().get(i);
    		long lAppliedtoTransactionID = -1;
    		
    		//If this is a 'misc payment' or a 'prepay', then there ARE no real 'apply-to's involved:
    		if (
    			(entry.getientrytype() != SMTableapbatchentries.ENTRY_TYPE_PAYMENT_MISCPAYMENT)
    			&& (entry.getientrytype() != SMTableapbatchentries.ENTRY_TYPE_PAYMENT_PREPAYMENT)
    		){
	        	//If we can't find the AP transaction we are supposed to be applying to, OR the amount we are applying is more than the current amount, exit out:
	        	SQL = "SELECT * FROM " + SMTableaptransactions.TableName
	        		+ " WHERE ("
	        			+ "(" + SMTableaptransactions.sdocnumber + " = '" + line.getsapplytodocnumber() + "')"
	        			+ " AND (" + SMTableaptransactions.svendor + " = '" + entry.getsvendoracct() + "')"
	        		+ ")"
	        	;
	        	
	        	ResultSet rsApplyToDoc = clsDatabaseFunctions.openResultSet(SQL, conn);
	        	if (rsApplyToDoc.next()){
	        		/* - 4/4/2017 - after talking to Chris R, we may need to apply MORE than the current amt on the apply-to transaction, so 
	        		 this is commented out:
	        		BigDecimal bdEntryAmount = new BigDecimal(entry.getsentryamount().replaceAll(",", ""));
	        		NEEDS TO BE POSITIVE FOR A DEBIT NOTE HERE:
	        		if (bdEntryAmount.multiply(new BigDecimal("-1")).compareTo(rsApplyToDoc.getBigDecimal(SMTableaptransactions.dcurrentamt)) > 0){
	        			rsApplyToDoc.close();
	        			throw new Exception ("The credit note amount (" 
	        				+ SMUtilities.BigDecimalTo2DecimalSTDFormat(bdEntryAmount) + ") is more than the current amount (" 
	        				+ SMUtilities.BigDecimalTo2DecimalSTDFormat(rsApplyToDoc.getBigDecimal(SMTableaptransactions.dcurrentamt)) + ")"
	        				+ " on document number '" + entry.getsapplytodocnumber() + "' for vendor '" + entry.getsvendoracct() + "'"
	        				+ " on entry number " + entry.getsentrynumber() + "."
	        			);
	        		}
	        		*/
	        		lAppliedtoTransactionID = rsApplyToDoc.getLong(SMTableaptransactions.lid);
	        		rsApplyToDoc.close();
	        	}else{
	        		rsApplyToDoc.close();
	        		throw new Exception("Could not find document number '" + line.getsapplytodocnumber() + "' for vendor '" + entry.getsvendoracct() + "' in entry number " + entry.getsentrynumber() + ".");
	        	}
    		}
        	
        	//If all the checks passed, then we now insert matching lines against the apply-to document (e.g., invoice):
        	//First, we'll add a line that applies TO the invoice/document, decreasing the liability on the invoice.  
        	//The 'matching' line will normally have a POSITIVE sign if it's a PAYMENT or a PRE-PAY, and a NEGATIVE sign if it's a debit because
        	//the ORIGINAL AMT minus the APPLYING matching lines must equal the CURRENT AMT on the invoice.
        	//An APPLY-TO will have positive AND negative lines, depending on whether it's applying against invoices, or payments, or debit notes, or credit notes, etc.
        	//So for an APPLY-TO, we still want to reverse the sign, so that the rule always holds:
        	// "A transaction's ORIGINAL AMT minus the APPLYING matching lines must equal the CURRENT AMT on the transaction."
    		
    		//For 'misc payments' or pre-pays, we don't need to create a matching line to any other 'apply to' doc:
    		
    		if (
    			(entry.getientrytype() != SMTableapbatchentries.ENTRY_TYPE_PAYMENT_MISCPAYMENT)
    			&& (entry.getientrytype() != SMTableapbatchentries.ENTRY_TYPE_PAYMENT_PREPAYMENT)
    		){
	        	SQL = "INSERT INTO " + SMTableapmatchinglines.TableName + "("
	        		+ SMTableapmatchinglines.bdappliedamount
	        		+ ", " + SMTableapmatchinglines.bddiscountappliedamount
	        		+ ", " + SMTableapmatchinglines.dattransactiondate
	        		+ ", " + SMTableapmatchinglines.ltransactionappliedfromid
	        		+ ", " + SMTableapmatchinglines.ltransactionappliedtoid
	        		+ ", " + SMTableapmatchinglines.sappliedfromdocnumber
	        		+ ", " + SMTableapmatchinglines.sappliedtodocnumber
	        		+ ", " + SMTableapmatchinglines.sdescription
	        		+ ", " + SMTableapmatchinglines.svendor
	        			
	        		+ ") VALUES ("
	        		//On PAYMENT lines, these functions usually reverse the sign of the STORED number, and gives us positive numbers, but we have to REVERSE that
	        		//to give us negatives to apply it to REDUCE the current amount of the document it's being applied to:
	        		+ line.getsbdamount().replaceAll(",", "") + " * -1"
	        		+ ", " + line.getsbddiscountappliedamt().replaceAll(",", "") + " * -1"
	        		+ ", '" + entry.getsdatdocdateInSQLFormat() + "'"  //transaction date
	        		+ ", " + Long.toString(lAppliedFromTransactionID)  //ltransactionappliedfromid
	        		+ ", " + Long.toString(lAppliedtoTransactionID) //ltransactionappliedtoid
	        		+ ", '" + clsDatabaseFunctions.FormatSQLStatement(entry.getsdocnumber()) + "'"  //sappliedfromdocnumber
	        		+ ", '" + clsDatabaseFunctions.FormatSQLStatement(line.getsapplytodocnumber()) + "'"  //sappliedtodocnumber
	        		+ ", '" + SMTableapbatchentries.getDocumentTypeLabel(Integer.parseInt(entry.getsentrytype())) + "'"   //sdescription
	        		+ ", '" + entry.getsvendoracct() + "'" //vendor
	        		+ ")"
	        	;
	        	try {
	    			Statement stmt = conn.createStatement();
	    			stmt.execute(SQL);
	    		} catch (Exception e) {
	    			throw new Exception("Error [1494424263] inserting matching line to apply to APPLY_TO doc with SQL: " + SQL + " - " + e.getMessage());
	    		}
	        	
	        	//Here we have to update available discount - if the transaction is fully paid the available discount must be zero.  If the remaining amount is LESS THAN
	        	//the discount available, reduce the discount available to equal the remaining amount:
	        	//Now update the current amt on the apply-to transaction:
	        	
	        	// TJR - 5/3/2018 - we want to do this in TWO SEPARATE COMMANDS because I found (the hard way) that combining them into one gives an ambigous value to the
	        	// 'current amt', and so when it does the math for the discount available, it may read the updated value for the current amt, instead of the UNupdated value.
	        	//  Safer to just separate the two commands.
	        	
	        	SQL = "UPDATE " + SMTableaptransactions.TableName
	        			
	        		// The current amt gets reduced by the applied amt AND the discount applied amt:
	        		+ " SET " + SMTableaptransactions.bdcurrentamt
	        		+ " = (" + SMTableaptransactions.bdcurrentamt + " + " + line.getsbdamount().replaceAll(",", "") + ")" + " + " + line.getsbddiscountappliedamt().replaceAll(",", "")
	        		+ " WHERE ("
	        			+ "(" + SMTableaptransactions.lid + " = " + Long.toString(lAppliedtoTransactionID) + ")"
	        		+ ")"
	        	;
	        	try {
	    			Statement stmt = conn.createStatement();
	    			stmt.execute(SQL);
	    		} catch (Exception e) {
	    			throw new Exception("Error [1491315427] updating current amt on APPLY-TO document with SQL: " + SQL + " - " + e.getMessage());
	    		}
	        	
	        	//NOW update the discount available:
	        	BigDecimal bdDiscountAppliedAmt = new BigDecimal(line.getsbddiscountappliedamt().replaceAll(",", ""));
	        	
	        	//Calculating this outside of SQL to get it exactly right:
	        	BigDecimal bdCurrentAmt = new BigDecimal("0.00");
	        	BigDecimal bdDscountAvailableAmt = new BigDecimal("0.00");
	        	BigDecimal bdDiscountRemainingAmt = new BigDecimal("0.00");
	        	SQL = "SELECT"
	        		+ " " + SMTableaptransactions.bdcurrentamt
	        		+ ", " + SMTableaptransactions.bdcurrentdiscountavailable
	        		+ " FROM " + SMTableaptransactions.TableName
	        		+ " WHERE ("
        				+ "(" + SMTableaptransactions.lid + " = " + Long.toString(lAppliedtoTransactionID) + ")"
        			+ ")"
        		;
	        	try {
					ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
					if (rs.next()){
						bdCurrentAmt = rs.getBigDecimal(SMTableaptransactions.bdcurrentamt);
						bdDscountAvailableAmt = rs.getBigDecimal(SMTableaptransactions.bdcurrentdiscountavailable);
						rs.close();
					}else{
						rs.close();
						throw new Exception("Error [1525363749] reading APPLY-TO document to update discount remaining with SQL: " + SQL + ".");
					}
				} catch (Exception e1) {
					throw new Exception("Error [1525363750] reading APPLY-TO document to update discount remaining with SQL: " + SQL + " - " + e1.getMessage());
				}
	        	
	        	//We'll subtract the discount applied from the discount remaining first:
	        	bdDiscountRemainingAmt = bdDscountAvailableAmt.subtract(bdDiscountAppliedAmt);
	        	
	        	//The discount remaining CAN'T be less than the current amount remaining, so check that first:
	        	if (bdDiscountRemainingAmt.compareTo(bdCurrentAmt) > 0){
	        		bdDiscountRemainingAmt = bdCurrentAmt;
	        	}
	        	
	        	//And the discount remaining can't be less than zero:
	        	if (bdDiscountRemainingAmt.compareTo(BigDecimal.ZERO) < 0){
	        		bdDiscountRemainingAmt = BigDecimal.ZERO;
	        	}
	        	
	        	//If the discount available PLUS the discount being applied is MORE than the remaining amount of the invoice, then set the discount available to be equal to the remaining amt:
	        	SQL = "UPDATE " + SMTableaptransactions.TableName
        			
	        		// The current amt gets reduced by the applied amt AND the discount applied amt:
	        		+ " SET " + SMTableaptransactions.bdcurrentdiscountavailable + " = " + ServletUtilities.clsManageBigDecimals.BigDecimalTo2DecimalSQLFormat(bdDiscountRemainingAmt)
	        		+ " WHERE ("
	        			+ "(" + SMTableaptransactions.lid + " = " + Long.toString(lAppliedtoTransactionID) + ")"
	        		+ ")"
	        	;
	        	try {
	    			Statement stmt = conn.createStatement();
	    			stmt.execute(SQL);
	    		} catch (Exception e) {
	    			throw new Exception("Error [1525363748] updating current amt on APPLY-TO document with SQL: " + SQL + " - " + e.getMessage());
	    		}
    		}
    		
        	//Next we need to add a 'matching' line that applies BACK TO this transaction that created the matching lines.  It will normally have a NEGATIVE value, 
        	// just like the matching transaction itself, because the original amount of the transaction MINUS the matching lines applied TO it must equal the
        	// CURRENT AMT on the transaction itself.  So if a payment/credit is fully applied against one or more invoices, the current amount on that applying
        	// transaction is zero after the transaction is created and fully applied:
    		
    		//Prepays get NO matching lines because the documents they apply to aren't in the system yet, AND we don't want to reduce the current amt of
    		//the prepay itself until it's actually applied later to some document:
    		if (entry.getientrytype() != SMTableapbatchentries.ENTRY_TYPE_PAYMENT_PREPAYMENT){
	        	SQL = "INSERT INTO " + SMTableapmatchinglines.TableName + "("
	        		+ SMTableapmatchinglines.bdappliedamount
	        		+ ", " + SMTableapmatchinglines.bddiscountappliedamount
	        		+ ", " + SMTableapmatchinglines.dattransactiondate
	        		+ ", " + SMTableapmatchinglines.ltransactionappliedfromid
	        		+ ", " + SMTableapmatchinglines.ltransactionappliedtoid
	        		+ ", " + SMTableapmatchinglines.sappliedfromdocnumber
	        		+ ", " + SMTableapmatchinglines.sappliedtodocnumber
	        		+ ", " + SMTableapmatchinglines.sdescription
	        		+ ", " + SMTableapmatchinglines.svendor
	        			
	        		+ ") VALUES ("
	        		//On PAYMENT lines, these lines are normally negative, and that's what we need because the rule is:
	        		// "A transaction's ORIGINAL AMT minus the APPLYING matching lines must equal the CURRENT AMT on the transaction."
	        		+ line.getsbdamount().replaceAll(",", "")
	        		+ ", " + line.getsbddiscountappliedamt().replaceAll(",", "")
	        		+ ", '" + entry.getsdatdocdateInSQLFormat() + "'"  //transaction date
	        		+ ", " + Long.toString(lAppliedFromTransactionID)  //ltransactionappliedfromid
	        		+ ", " + Long.toString(lAppliedFromTransactionID) //this matching line points back to the parent transaction which created it
	        		+ ", '" + clsDatabaseFunctions.FormatSQLStatement(entry.getsdocnumber()) + "'"  //sappliedfromdocnumber
	        		+ ", '" + clsDatabaseFunctions.FormatSQLStatement(entry.getsdocnumber()) + "'"  //this matching line points back to the parent transaction which created it
	        		+ ", '" + SMTableapbatchentries.getDocumentTypeLabel(Integer.parseInt(entry.getsentrytype())) + "'"   //sdescription
	        		+ ", '" + entry.getsvendoracct() + "'" //vendor
	        		+ ")"
	        	;
	        	try {
	    			Statement stmt = conn.createStatement();
	    			stmt.execute(SQL);
	    		} catch (Exception e) {
	    			throw new Exception("Error [1494424697] inserting matching line to apply to APPLY-FROM doc with SQL: " + SQL + " - " + e.getMessage());
	    		}
	        	
	        	//Now update the current amt on the applied-from ('parent') transaction, i.e. the transaction that created the lines:
	        	SQL = "UPDATE " + SMTableaptransactions.TableName
	        		+ " SET " + SMTableaptransactions.bdcurrentamt
	        		//The payment amount is generally negative, and the lines are also negative, so we have to SUBTRACT the value of the line from the 'parent'
	        		//transaction to reduce the 'current amt':
	        		
	        		//The CURRENT amt is determined by subtracting the AMT of the line - any 'discount applied' doesn't reduce the amt of the APPLY-FROM transaction....
	        		+ " = (" + SMTableaptransactions.bdcurrentamt + ") - (" + line.getsbdamount().replaceAll(",", "") + ")"
	        		
	        		//The APPLY-FROM document doesn't get its current discount available updated:
	        		//+ ", " + SMTableaptransactions.bdcurrentdiscountavailable
	        		//+ " = (" + SMTableaptransactions.bdcurrentdiscountavailable + " - " + line.getsbddiscountappliedamt().replaceAll(",", "") + ")"
	        		+ " WHERE ("
	        			+ "(" + SMTableaptransactions.lid + " = " + Long.toString(lAppliedFromTransactionID) + ")"
	        		+ ")"
	        	;
	        	
	        	try {
	    			Statement stmt = conn.createStatement();
	    			stmt.execute(SQL);
	    		} catch (Exception e) {
	    			throw new Exception("Error [1494424698] updating current amt on APPLY-FROM document with SQL: " + SQL + " - " + e.getMessage());
	    		}
    		}
    	}
    }
    private void createMatchingLinesForReversalBatchType(Connection conn, APBatchEntry reversalentry, long lAppliedFromTransactionID) throws Exception{
    	
    	String SQL = "";
    	
    	/* 
    	We'll need to add some matching lines, and re-set some 'current amounts':

    	The reversal transaction, after we've updated the original payment, and any invoices it applied to, will wind up with a zero current amt.
    	
    	The payment/prepay that's being reversed, will be set to a zero current amt, if it's not already.  (In the case of a pre-pay, it may have a current amt,
    	  and so have to be reduced to zero.
    	
    	The invoices that were originally being applied to will see their current amts increased, since we are reversing the money we applied to them.
    	
    	1) THE ORIGINAL PAYMENT or PRE-PAY THAT'S BEING REVERSED:
    	    The payment/pre-pay may have a current amt - if it does, then we'll have to reduce it to zero with matching lines FROM the reversal
    	    We'll take care of that first.

        2) THE INVOICES THAT WERE REDUCED BY THE PAYMENT or PRE-PAY WE ARE REVERSING:
           We'll add BACK the amount the original payment/pre-pay applied against the invoice to arrive at a new 'current amt' for the invoice transaction
           We'll add matching lines FROM the reversal, TO each invoice to get the invoices back up to the amount they were at before the payment was applied.
           The original invoice amounts were POSITIVE, so we'll need to add a NEGATIVE amt line in apmatchinglines, FROM the reversal, APPLYING TO the invoice
           to get the original invoice amt back 'up'. (Matching lines get SUBTRACTED from the apply-to transaction to get the transaction's 'CURRENT AMT'.
           So we'll SUBTRACT the amt of a NEGATIVE reversal matching line to get a POSITIVE current amt back on the invoice transaction.
    	
    	3) THE REVERSING ENTRY, WHICH IS BEING POSTED NOW:
    	    The reversal entry has an original amt equal to the amt of the payment, but a current amt, after posting, at zero
            We'll need to create matching lines which point TO the reversal, FROM the reversal, to reduce the reversal's 'original amt' to zero.
            The original payment may or may NOT have been fully applied to invoices (if it was a pre-pay, it may not have been applied at all.)
            So if any of the original payment was applied against invoices, we'll create matching lines to UNapply those invoices, and we'll create a matching line
            for each of those to also reduce the reversing entry to zero.
            
        NOTE: 
        In the aptransactions table, INVOICES, DEBITS have POSITIVE amounts
        In the aptransactions table, CREDITS, PAYMENTS, PRE-PAYS have NEGATIVE amounts
        In the aptransactions table, REVERSALS have POSITIVE amounts
    	
    	ALSO - the general rule for 'matching lines':
    	// "A transaction's ORIGINAL AMT minus the APPLYING matching lines must equal the CURRENT AMT on the transaction."
    	 * 
    	*/

    	
    	//First we'll update the current amt of the payment/pre-pay to be zero:
    	
    	//Let's get the AP transaction ID of the check we are reversing:

    	
    	//Get the current amt of the payment/prepay transaction:
    	BigDecimal bdCurrentAmtOfPaymentToBeReversed = new BigDecimal("0.00");
    	String sPaymentDocNumber = "";
    	long lTransactionIDOfPaymentToBeReversed = -1L;
    	SQL = "SELECT"
    		+ " " + SMTableaptransactions.lid
       		+ ", " + SMTableaptransactions.bdcurrentamt
    		+ ", " + SMTableaptransactions.sdocnumber
    		+ " FROM " + SMTableaptransactions.TableName
    		+ " WHERE ("
    			+ "(" + SMTableaptransactions.TableName + "." + SMTableaptransactions.loriginalbatchnumber + " != " + reversalentry.getsbatchnumber() + ")"
    			+ " AND (" + SMTableaptransactions.TableName + "." + SMTableaptransactions.schecknumber + " = '" + reversalentry.getschecknumber() + "')"
    		+ ")"
    	;
    	ResultSet rsPaymentTransaction = clsDatabaseFunctions.openResultSet(SQL, conn);
    	if (rsPaymentTransaction.next()){
      		bdCurrentAmtOfPaymentToBeReversed = rsPaymentTransaction.getBigDecimal(SMTableaptransactions.bdcurrentamt);
    		sPaymentDocNumber = rsPaymentTransaction.getString(SMTableaptransactions.sdocnumber);
    		lTransactionIDOfPaymentToBeReversed = rsPaymentTransaction.getLong(SMTableaptransactions.lid);
    	}else{
    		rsPaymentTransaction.close();
    		throw new Exception("Error [1521211266] could not read AP reversal transaction with SQL '" + SQL + "'.");
    	}
    	
    	//If the original payment still has a 'current amt', then we need to set that to zero (since we are reversing out this payment), and also create a matching line for it:
    	if (bdCurrentAmtOfPaymentToBeReversed.compareTo(BigDecimal.ZERO) != 0){
        	//Now update the current amount of the payment to zero:
        	SQL = "UPDATE " + SMTableaptransactions.TableName
        		+ " SET " + SMTableaptransactions.bdcurrentamt + " = 0.00"
        		+ " WHERE ("
        			+ "(" + SMTableaptransactions.lid + " = " + Long.toString(lTransactionIDOfPaymentToBeReversed) + ")"
        		+ ")"
        	;
        	try {
    			Statement stmt = conn.createStatement();
    			stmt.execute(SQL);
    		} catch (Exception e) {
    			throw new Exception("Error [1521214044] updating current amt of payment transaction to zero with SQL: " + SQL + " - " + e.getMessage());
    		}
        	
        	//And create a matching line FROM the reversal TO the original payment to justify reducing that current amt to zero:
        	SQL = "INSERT INTO " + SMTableapmatchinglines.TableName + "("
        		+ SMTableapmatchinglines.bdappliedamount
        		+ ", " + SMTableapmatchinglines.bddiscountappliedamount
        		+ ", " + SMTableapmatchinglines.dattransactiondate
        		+ ", " + SMTableapmatchinglines.ltransactionappliedfromid
        		+ ", " + SMTableapmatchinglines.ltransactionappliedtoid
        		+ ", " + SMTableapmatchinglines.sappliedfromdocnumber
        		+ ", " + SMTableapmatchinglines.sappliedtodocnumber
        		+ ", " + SMTableapmatchinglines.sdescription
        		+ ", " + SMTableapmatchinglines.svendor
        			
        		+ ") VALUES ("
        		+ clsManageBigDecimals.BigDecimalTo2DecimalSQLFormat(bdCurrentAmtOfPaymentToBeReversed)  //bdappliedamount
        		+ ", " + "0.00"  //bddiscountappliedamount - there's no discount amount applied on unapplied cash
        		+ ", '" + reversalentry.getsdatdocdateInSQLFormat() + "'"  //transaction date
        		+ ", " + Long.toString(lAppliedFromTransactionID)  //ltransactionappliedfromid
        		+ ", " + Long.toString(lTransactionIDOfPaymentToBeReversed) //ltransactionappliedtoid
        		+ ", '" + clsDatabaseFunctions.FormatSQLStatement(reversalentry.getsdocnumber()) + "'"  //sappliedfromdocnumber
        		+ ", '" + clsDatabaseFunctions.FormatSQLStatement(sPaymentDocNumber) + "'"  //sappliedtodocnumber
        		+ ", '" + SMTableapbatchentries.getDocumentTypeLabel(Integer.parseInt(reversalentry.getsentrytype())) + "'"   //sdescription
        		+ ", '" + reversalentry.getsvendoracct() + "'" //vendor
        		+ ")"
        	;
        	try {
    			Statement stmt = conn.createStatement();
    			stmt.execute(SQL);
    		} catch (Exception e) {
    			throw new Exception("Error [1521214054] inserting matching line to reduce current amt of payment transaction to zero with SQL: " + SQL + " - " + e.getMessage());
    		}
        	
        	//Now reduce the amount of the reversal entry by the same amount:
        	//The reversal is POSITIVE, the amt remaining on the payment was NEGATIVE - so we have to NEGATE the amt remaining, to make it positive,
        	//before we can subtract it from the POSITIVE reversal amt:
        	SQL = "UPDATE " + SMTableaptransactions.TableName
        		+ " SET " + SMTableaptransactions.bdcurrentamt + " = (" + SMTableaptransactions.bdcurrentamt + " - " 
        			+ clsManageBigDecimals.BigDecimalTo2DecimalSQLFormat(bdCurrentAmtOfPaymentToBeReversed.negate()) + ")"
        		+ " WHERE ("
        			+ "(" + SMTableaptransactions.lid + " = " + Long.toString(lAppliedFromTransactionID) + ")"
        		+ ")"
        	;
        	try {
    			Statement stmt = conn.createStatement();
    			stmt.execute(SQL);
    		} catch (Exception e) {
    			throw new Exception("Error [1521216130] updating current amt of payment transaction to zero with SQL: " + SQL + " - " + e.getMessage());
    		}
        	
        	//Now create a matching line FROM the reversal TO the reversal to justify the reduction in current amt:
        	SQL = "INSERT INTO " + SMTableapmatchinglines.TableName + "("
        		+ SMTableapmatchinglines.bdappliedamount
        		+ ", " + SMTableapmatchinglines.bddiscountappliedamount
        		+ ", " + SMTableapmatchinglines.dattransactiondate
        		+ ", " + SMTableapmatchinglines.ltransactionappliedfromid
        		+ ", " + SMTableapmatchinglines.ltransactionappliedtoid
        		+ ", " + SMTableapmatchinglines.sappliedfromdocnumber
        		+ ", " + SMTableapmatchinglines.sappliedtodocnumber
        		+ ", " + SMTableapmatchinglines.sdescription
        		+ ", " + SMTableapmatchinglines.svendor
        			
        		+ ") VALUES ("
        		+ clsManageBigDecimals.BigDecimalTo2DecimalSQLFormat(bdCurrentAmtOfPaymentToBeReversed.negate())  //bdappliedamount
        		+ ", " + "0.00"  //bddiscountappliedamount - there's no discount amount applied on unapplied cash
        		+ ", '" + reversalentry.getsdatdocdateInSQLFormat() + "'"  //transaction date
        		+ ", " + Long.toString(lAppliedFromTransactionID)  //ltransactionappliedfromid
        		+ ", " + Long.toString(lAppliedFromTransactionID) //ltransactionappliedtoid
        		+ ", '" + clsDatabaseFunctions.FormatSQLStatement(reversalentry.getsdocnumber()) + "'"  //sappliedfromdocnumber
        		+ ", '" + clsDatabaseFunctions.FormatSQLStatement(reversalentry.getsdocnumber()) + "'"  //sappliedtodocnumber
        		+ ", '" + SMTableapbatchentries.getDocumentTypeLabel(Integer.parseInt(reversalentry.getsentrytype())) + "'"   //sdescription
        		+ ", '" + reversalentry.getsvendoracct() + "'" //vendor
        		+ ")"
        	;
        	try {
    			Statement stmt = conn.createStatement();
    			stmt.execute(SQL);
    		} catch (Exception e) {
    			throw new Exception("Error [1521216131] inserting matching line to reduce current amt of reversal transaction with SQL: " + SQL + " - " + e.getMessage());
    		}
        	
    	}

    	//NEXT.....
    	//Now we deal with any invoices that the original payment or pre-pay might have applied to:
    	SQL = "SELECT"
        	+ " * FROM " + SMTableapmatchinglines.TableName
        	+ " WHERE ("
        		
        		//We want everything applied FROM the original payment....
        		+ "(" + SMTableapmatchinglines.ltransactionappliedfromid + " = " + Long.toString(lTransactionIDOfPaymentToBeReversed) + ")"
        		//But we DON'T want any lines that applied BACK TO the original payment...
        		+ " AND (" + SMTableapmatchinglines.ltransactionappliedtoid + " != " + Long.toString(lTransactionIDOfPaymentToBeReversed) + ")"
        	+ ")"
        ;
        	
    	ResultSet rsPaidInvoiceMatchingLines = clsDatabaseFunctions.openResultSet(SQL, conn);
    	while (rsPaidInvoiceMatchingLines.next()){
    		//For each of these, we need to:
    		//  1) INCREASE the 'current amt' on the invoice
    		//  2) Create a matching line FROM the reversal TO the invoice to 'justify' the increase (it should have a NEGATIVE applied amount)
    		//  3) DECREASE the 'current amt' on the reversing transaction
    		//  4) Create a matching line FROM the reversal TO the reversal to 'justify' the decrease (it should have a POSITIVE applied amount)
    		
    		//First update the 'current amt' on the invoice:
    		BigDecimal bdAmountApplied = rsPaidInvoiceMatchingLines.getBigDecimal(SMTableapmatchinglines.bdappliedamount);  //this would normally be POSITIVE, when applied to an invoice or debit note
    		BigDecimal bdDiscountAmountApplied = rsPaidInvoiceMatchingLines.getBigDecimal(SMTableapmatchinglines.bddiscountappliedamount); //normally positive, as well
    		Long lInvoiceID = rsPaidInvoiceMatchingLines.getLong(SMTableapmatchinglines.ltransactionappliedtoid);
    		String sInvoiceDocNumber = rsPaidInvoiceMatchingLines.getString(SMTableapmatchinglines.sappliedtodocnumber);
    		//Update the invoice current amt:
        	SQL = "UPDATE " + SMTableaptransactions.TableName
        			
        		//We have to ADD BACK IN TO the 'current amt' any discount amt taken:
           		+ " SET " + SMTableaptransactions.bdcurrentamt + " = (" + SMTableaptransactions.bdcurrentamt + " + (" 
           			+ clsManageBigDecimals.BigDecimalTo2DecimalSQLFormat(bdAmountApplied) 
           			+ " + " + clsManageBigDecimals.BigDecimalTo2DecimalSQLFormat(bdDiscountAmountApplied) + ")"
           			+ ")"
           		+ ", " + SMTableaptransactions.bdcurrentdiscountavailable + " = (" + SMTableaptransactions.bdcurrentdiscountavailable + " + " 
           			+ clsManageBigDecimals.BigDecimalTo2DecimalSQLFormat(bdDiscountAmountApplied) + ")"
           		+ " WHERE ("
           			+ "(" + SMTableaptransactions.lid + " = " + Long.toString(lInvoiceID) + ")"
           		+ ")"
           	;
        	//System.out.println("[1521224178] - SQL = '" + SQL + "'.");
        	try {
    			Statement stmt = conn.createStatement();
    			stmt.execute(SQL);
    		} catch (Exception e) {
    			throw new Exception("Error [1521217406] increasing current amt of invoice with SQL: " + SQL + " - " + e.getMessage());
    		}
        	
        	//Create a matching line to justify the increase in the invoice's current amt:
        	SQL = "INSERT INTO " + SMTableapmatchinglines.TableName + "("
        		+ SMTableapmatchinglines.bdappliedamount
        		+ ", " + SMTableapmatchinglines.bddiscountappliedamount
        		+ ", " + SMTableapmatchinglines.dattransactiondate
        		+ ", " + SMTableapmatchinglines.ltransactionappliedfromid
        		+ ", " + SMTableapmatchinglines.ltransactionappliedtoid
        		+ ", " + SMTableapmatchinglines.sappliedfromdocnumber
        		+ ", " + SMTableapmatchinglines.sappliedtodocnumber
        		+ ", " + SMTableapmatchinglines.sdescription
        		+ ", " + SMTableapmatchinglines.svendor
        			
        		+ ") VALUES ("
        		//Have to add in ALSO the original discount taken here:
        		+ clsManageBigDecimals.BigDecimalTo2DecimalSQLFormat(bdAmountApplied.add(bdDiscountAmountApplied).negate())  //bdappliedamount - should be a NEGATIVE number
        		+ ", " + clsManageBigDecimals.BigDecimalTo2DecimalSQLFormat(bdDiscountAmountApplied.negate())  //bddiscountappliedamount - should be a NEGATIVE number
        		+ ", '" + reversalentry.getsdatdocdateInSQLFormat() + "'"  //transaction date
        		+ ", " + Long.toString(lAppliedFromTransactionID)  //ltransactionappliedfromid
        		+ ", " + Long.toString(lInvoiceID) //ltransactionappliedtoid
        		+ ", '" + clsDatabaseFunctions.FormatSQLStatement(reversalentry.getsdocnumber()) + "'"  //sappliedfromdocnumber
        		+ ", '" + clsDatabaseFunctions.FormatSQLStatement(sInvoiceDocNumber) + "'"  //sappliedtodocnumber
        		+ ", '" + SMTableapbatchentries.getDocumentTypeLabel(Integer.parseInt(reversalentry.getsentrytype())) + "'"   //sdescription
        		+ ", '" + reversalentry.getsvendoracct() + "'" //vendor
        		+ ")"
        	;
        	try {
    			Statement stmt = conn.createStatement();
    			stmt.execute(SQL);
    		} catch (Exception e) {
    			throw new Exception("Error [1521217407] inserting matching line to increase current amt of invoice transaction with SQL: " + SQL + " - " + e.getMessage());
    		}
        	
        	//Now reduce the current amt on the reversing transaction:
        	SQL = "UPDATE " + SMTableaptransactions.TableName
           		+ " SET " + SMTableaptransactions.bdcurrentamt + " = (" + SMTableaptransactions.bdcurrentamt + " - " + clsManageBigDecimals.BigDecimalTo2DecimalSQLFormat(bdAmountApplied) + ")"
           		+ " WHERE ("
           			+ "(" + SMTableaptransactions.lid + " = " + Long.toString(lAppliedFromTransactionID) + ")"
           		+ ")"
           	;
        	try {
    			Statement stmt = conn.createStatement();
    			stmt.execute(SQL);
    		} catch (Exception e) {
    			throw new Exception("Error [1521217408] decreasing current amt of reversing transaction with SQL: " + SQL + " - " + e.getMessage());
    		}
            	
        	//Now add a matching line to 'justify' the reduced current amt of the reversal transaction:
        	SQL = "INSERT INTO " + SMTableapmatchinglines.TableName + "("
        		+ SMTableapmatchinglines.bdappliedamount
        		+ ", " + SMTableapmatchinglines.bddiscountappliedamount
        		+ ", " + SMTableapmatchinglines.dattransactiondate
        		+ ", " + SMTableapmatchinglines.ltransactionappliedfromid
        		+ ", " + SMTableapmatchinglines.ltransactionappliedtoid
        		+ ", " + SMTableapmatchinglines.sappliedfromdocnumber
        		+ ", " + SMTableapmatchinglines.sappliedtodocnumber
        		+ ", " + SMTableapmatchinglines.sdescription
        		+ ", " + SMTableapmatchinglines.svendor
        			
        		+ ") VALUES ("
        		+ clsManageBigDecimals.BigDecimalTo2DecimalSQLFormat(bdAmountApplied)  //bdappliedamount
        		+ ", " + clsManageBigDecimals.BigDecimalTo2DecimalSQLFormat(bdDiscountAmountApplied)  //bddiscountappliedamount
        		+ ", '" + reversalentry.getsdatdocdateInSQLFormat() + "'"  //transaction date
        		+ ", " + Long.toString(lAppliedFromTransactionID)  //ltransactionappliedfromid
        		+ ", " + Long.toString(lAppliedFromTransactionID) //ltransactionappliedtoid
        		+ ", '" + clsDatabaseFunctions.FormatSQLStatement(reversalentry.getsdocnumber()) + "'"  //sappliedfromdocnumber
        		+ ", '" + clsDatabaseFunctions.FormatSQLStatement(reversalentry.getsdocnumber()) + "'"  //sappliedtodocnumber
        		+ ", '" + SMTableapbatchentries.getDocumentTypeLabel(Integer.parseInt(reversalentry.getsentrytype())) + "'"   //sdescription
        		+ ", '" + reversalentry.getsvendoracct() + "'" //vendor
        		+ ")"
        	;
        	try {
    			Statement stmt = conn.createStatement();
    			stmt.execute(SQL);
    		} catch (Exception e) {
    			throw new Exception("Error [1521217409] inserting matching line to reduce current amt of reversal transaction with SQL: " + SQL + " - " + e.getMessage());
    		}
        	
    	}  //end while (rsPaidInvoiceMatchingLines.next())
    	rsPaidInvoiceMatchingLines.close();

    }

    private void updateStatistics(APBatchEntry entry, Connection conn) throws Exception{
    	
    	String sAmountOfAdjustments = "0.00";  //This is not being calculated below....
    	String sAmountOfCreditNotes = "0.00";
    	String sAmountOfDebitNotes = "0.00";
    	String sAmountOfDiscounts = "0.00";
    	String sAmountOfDiscountsLost = "0.00";
    	String sAmountOfInvoices = "0.00";
    	String sAmountOfPayments = "0.00";
    	
    	Date docDate = new SimpleDateFormat(SMUtilities.DATE_FORMAT_FOR_DISPLAY).parse(entry.getsdatdocdate());  
        Calendar cal = Calendar.getInstance();
        cal.setTime(docDate);
        String sYear = Integer.toString(cal.get(Calendar.YEAR));
        String sMonth = Integer.toString(cal.get(Calendar.MONTH));
    	String sNumberOfAdjustments = "0";
    	String sNumberOfCreditNotes = "0";
    	String sNumberOfDebitNotes = "0";
    	String sNumberOfDiscountsLost = "0";
    	String sNumberOfDiscountsTaken = "0";
    	String sNumberOfInvoices = "0";
    	String sNumberOfPayments = "0";
    	
    	//Get the discount information:
    	int iDiscountsTaken = 0;
    	int iDiscountsLost = 0;
    	BigDecimal bdDiscountTakenAmt = new BigDecimal("0.00");
    	BigDecimal bdDiscountLostAmt = new BigDecimal("0.00");
    	long lNumberOfDaysToPay = 0L;
    	long lNumberOfInvoicesPaid = 0L;
    	String sApplyToInvoiceDate = SMUtilities.EMPTY_DATE_VALUE;
    	for (int i = 0; i < entry.getLineArray().size(); i++){
    		APBatchEntryLine line = entry.getLineArray().get(i);
    		
    		//If this is a CHECK REVERSAL, then we have to roll back any discounts taken and any discount amts:
    		if (entry.getientrytype() == SMTableapbatchentries.ENTRY_TYPE_REVERSAL){
    			//If there is a discount on this line, we are rolling it back when we reverse the check, and so we record that in the statistics:
    			if (new BigDecimal(line.getsbddiscountappliedamt().replaceAll(",", "")).compareTo(BigDecimal.ZERO) != 0 ){
    				iDiscountsTaken = iDiscountsTaken - 1;
    				//Discount amts on reversals are POSITIVE, so we can add them, because the discount total in the statistics records are normally NEGATIVE:
    				bdDiscountTakenAmt = bdDiscountTakenAmt.add(new BigDecimal(line.getsbddiscountappliedamt().replaceAll(",", "")));
    			}
    		//Otherwise, deal with any other kind of transaction here:
    		}else{
	    		//Get the discount available on each 'apply-to':
	    		BigDecimal bdDiscountAvailable = new BigDecimal("0.00");
	    		if (line.getslapplytodocid().compareToIgnoreCase("0") != 0){
	    			String SQL = "SELECT"
	    				+ " " + SMTableaptransactions.bdcurrentdiscountavailable
	    				+ ", " + SMTableaptransactions.datdocdate
	    				+ ", " + SMTableaptransactions.idoctype
	    				+ " FROM " + SMTableaptransactions.TableName
	    				+ " WHERE ("
	    					+ "(" + SMTableaptransactions.lid + " = " + line.getslapplytodocid() + ")"
	    				+ ")"
	    			;
	    			ResultSet rsApplyToDocuments = clsDatabaseFunctions.openResultSet(SQL, conn);
	    			if (rsApplyToDocuments.next()){
	    				bdDiscountAvailable = rsApplyToDocuments.getBigDecimal(SMTableaptransactions.bdcurrentdiscountavailable);
	    				sApplyToInvoiceDate = clsDateAndTimeConversions.resultsetDateStringToFormattedString(rsApplyToDocuments.getString(SMTableaptransactions.datdocdate), SMUtilities.DATE_FORMAT_FOR_DISPLAY, SMUtilities.EMPTY_DATE_VALUE);
	    				
	    				//Get the number of days to pay:
	        			if (rsApplyToDocuments.getInt(SMTableaptransactions.idoctype) == SMTableaptransactions.AP_TRANSACTION_TYPE_INVOICE_INVOICE){
	    	    			if (sApplyToInvoiceDate.compareToIgnoreCase(SMUtilities.EMPTY_DATE_VALUE) != 0){
	    	    				lNumberOfDaysToPay = lNumberOfDaysToPay + clsDateAndTimeConversions.getFirstDateMinusSecondDateInDays(entry.getsdatdocdate(), sApplyToInvoiceDate);
	    	    				lNumberOfInvoicesPaid = lNumberOfInvoicesPaid + 1;
	    	    			}
	        			}
	    			}
	    			rsApplyToDocuments.close();
	    			BigDecimal bdDiscountApplied = new BigDecimal(line.getsbddiscountappliedamt().replaceAll(",", ""));
	    			
	    			//If there is a discount available:
	    			if (bdDiscountAvailable.compareTo(BigDecimal.ZERO) > 0){
	    				//Then IF we are taking ALL of the discount available, add to the 'discounts taken':
	        			if (bdDiscountApplied.compareTo(bdDiscountAvailable) >= 0){
	        				iDiscountsTaken++;
	        				bdDiscountTakenAmt = bdDiscountTakenAmt.add(bdDiscountApplied);
	        			}else{
	        				iDiscountsLost++;
	        				bdDiscountLostAmt = bdDiscountLostAmt.add(bdDiscountAvailable.subtract(bdDiscountApplied));
	        			}
	    			}
	    		}
    		}
    	}
    	
    	sAmountOfDiscounts = clsManageBigDecimals.BigDecimalTo2DecimalSQLFormat(bdDiscountTakenAmt);
    	sAmountOfDiscountsLost = clsManageBigDecimals.BigDecimalTo2DecimalSQLFormat(bdDiscountLostAmt);
    	
    	sNumberOfDiscountsTaken = Integer.toString(iDiscountsTaken);
    	sNumberOfDiscountsLost = Integer.toString(iDiscountsLost);
    	
    	if(entry.getientrytype() == SMTableapbatchentries.ENTRY_TYPE_INV_CREDITNOTE){
    		sAmountOfCreditNotes = sAmountOfCreditNotes + " + " + entry.getsentryamount().replaceAll(",", "");
    		sNumberOfCreditNotes = "1";
    	}
    	
    	if(entry.getientrytype() == SMTableapbatchentries.ENTRY_TYPE_INV_DEBITNOTE){
       		sAmountOfDebitNotes = sAmountOfDebitNotes + " + " + entry.getsentryamount().replaceAll(",", "");
    		sNumberOfDebitNotes = "1";
    	}
    	
    	if(entry.getientrytype() == SMTableapbatchentries.ENTRY_TYPE_INV_INVOICE){
    		sAmountOfInvoices = sAmountOfInvoices + " + " + entry.getsentryamount().replaceAll(",", "");
    		sNumberOfInvoices = "1";
    	}
    	
    	if(entry.getientrytype() == SMTableapbatchentries.ENTRY_TYPE_PAYMENT_APPLYTO){
    		//No statistics on 'apply to's
    	}
    	
    	if(entry.getientrytype() == SMTableapbatchentries.ENTRY_TYPE_PAYMENT_MISCPAYMENT){
    		sAmountOfPayments = sAmountOfPayments + " + " + entry.getsentryamount().replaceAll(",", "");
    		sNumberOfPayments = "1";
    	}
    	
       	if(entry.getientrytype() == SMTableapbatchentries.ENTRY_TYPE_PAYMENT_PAYMENT){
       		sAmountOfPayments = sAmountOfPayments + " + " + entry.getsentryamount().replaceAll(",", "");
    		sNumberOfPayments = "1";
    	}
    	
       	if(entry.getientrytype() == SMTableapbatchentries.ENTRY_TYPE_PAYMENT_PREPAYMENT){
       		sAmountOfPayments = sAmountOfPayments + " + " + entry.getsentryamount().replaceAll(",", "");
    		sNumberOfPayments = "1";
       	}
       	
       	if(entry.getientrytype() == SMTableapbatchentries.ENTRY_TYPE_REVERSAL){
       		sAmountOfPayments = sAmountOfPayments + " + (" + entry.getsentryamount().replaceAll(",", "") + ")";  //Normally a POSITIVE number (payment entry amts are NEGATIVE)
    		sNumberOfPayments = "(-1)";
       	}
       	
       	
    	String SQL = 
    		"INSERT INTO " + SMTableapvendorstatistics.TableName + "("
    		+ SMTableapvendorstatistics.bdamountofadjustments
    		+ ", " + SMTableapvendorstatistics.bdamountofcreditnotes
    		+ ", " + SMTableapvendorstatistics.bdamountofdebitnotes
    		+ ", " + SMTableapvendorstatistics.bdamountofdiscounts
    		+ ", " + SMTableapvendorstatistics.bdamountofdiscountslost
    		+ ", " + SMTableapvendorstatistics.bdamountofinvoices
    		+ ", " + SMTableapvendorstatistics.bdamountofpayments
    		+ ", " + SMTableapvendorstatistics.lmonth
    		+ ", " + SMTableapvendorstatistics.lnumberofadjustments
    		+ ", " + SMTableapvendorstatistics.lnumberofcredits
    		+ ", " + SMTableapvendorstatistics.lnumberofdaystopay
    		+ ", " + SMTableapvendorstatistics.lnumberofdebits
    		+ ", " + SMTableapvendorstatistics.lnumberofdiscountslost
    		+ ", " + SMTableapvendorstatistics.lnumberofdiscountstaken
    		+ ", " + SMTableapvendorstatistics.lnumberofinvoices
    		+ ", " + SMTableapvendorstatistics.lnumberofinvoicespaid
    		//+ ", " + SMTableapvendorstatistics.lnumberofpayapplicationsusedforaveraging
    		+ ", " + SMTableapvendorstatistics.lnumberofpayments
    		+ ", " + SMTableapvendorstatistics.lyear
    		+ ", " + SMTableapvendorstatistics.svendoracct
    		+ ") VALUES ("
    		+ sAmountOfAdjustments
    		+ ", " + sAmountOfCreditNotes
    		+ ", " + sAmountOfDebitNotes
    		+ ", " + sAmountOfDiscounts
    		+ ", " + sAmountOfDiscountsLost
    		+ ", " + sAmountOfInvoices
    		+ ", " + sAmountOfPayments
    		+ ", " + sMonth
    		+ ", " + sNumberOfAdjustments
    		+ ", " + sNumberOfCreditNotes
    		+ ", " + Long.toString(lNumberOfDaysToPay)
    		+ ", " + sNumberOfDebitNotes
    		+ ", " + sNumberOfDiscountsLost
    		+ ", " + sNumberOfDiscountsTaken
    		+ ", " + sNumberOfInvoices
    		+ ", " + Long.toString(lNumberOfInvoicesPaid)
    		//+ ", " + Long.toString(lNumberOfPays)
    		+ ", " + sNumberOfPayments
    		+ ", " + sYear
    		+ ", '" + entry.getsvendoracct() + "'"
    		
    		+ ") ON DUPLICATE KEY UPDATE"
    		+ " " + SMTableapvendorstatistics.bdamountofadjustments + " = " + SMTableapvendorstatistics.bdamountofadjustments + " + (" + sAmountOfAdjustments + ")"
    		+ ", " + SMTableapvendorstatistics.bdamountofcreditnotes + " = " + SMTableapvendorstatistics.bdamountofcreditnotes + " + (" + sAmountOfCreditNotes + ")"
    		+ ", " + SMTableapvendorstatistics.bdamountofdebitnotes + " = " + SMTableapvendorstatistics.bdamountofdebitnotes + " + (" + sAmountOfDebitNotes + ")"
    		+ ", " + SMTableapvendorstatistics.bdamountofdiscounts + " = " + SMTableapvendorstatistics.bdamountofdiscounts + " + (" + sAmountOfDiscounts + ")"
    		+ ", " + SMTableapvendorstatistics.bdamountofdiscountslost + " = " + SMTableapvendorstatistics.bdamountofdiscountslost + " + (" + sAmountOfDiscountsLost + ")"
    		+ ", " + SMTableapvendorstatistics.bdamountofinvoices + " = " + SMTableapvendorstatistics.bdamountofinvoices + " + (" + sAmountOfInvoices + ")"
    		+ ", " + SMTableapvendorstatistics.bdamountofpayments + " = " + SMTableapvendorstatistics.bdamountofpayments + " + (" + sAmountOfPayments + ")"
    		+ ", " + SMTableapvendorstatistics.lnumberofadjustments + " = " + SMTableapvendorstatistics.lnumberofadjustments + " + (" + sNumberOfAdjustments + ")"
    		+ ", " + SMTableapvendorstatistics.lnumberofcredits + " = " + SMTableapvendorstatistics.lnumberofcredits + " + (" + sNumberOfCreditNotes + ")"
    		+ ", " + SMTableapvendorstatistics.lnumberofdaystopay + " = " + SMTableapvendorstatistics.lnumberofdaystopay + " + (" + Long.toString(lNumberOfDaysToPay) + ")"
    		+ ", " + SMTableapvendorstatistics.lnumberofdebits + " = " + SMTableapvendorstatistics.lnumberofdebits + " + (" + sNumberOfDebitNotes + ")"
    		+ ", " + SMTableapvendorstatistics.lnumberofdiscountslost + " = " + SMTableapvendorstatistics.lnumberofdiscountslost + " + (" + sNumberOfDiscountsLost + ")"
    		+ ", " + SMTableapvendorstatistics.lnumberofdiscountstaken + " = " + SMTableapvendorstatistics.lnumberofdiscountstaken + " + (" + sNumberOfDiscountsTaken + ")"
    		+ ", " + SMTableapvendorstatistics.lnumberofinvoices + " = " + SMTableapvendorstatistics.lnumberofinvoices + " + (" + sNumberOfInvoices + ")"
    		+ ", " + SMTableapvendorstatistics.lnumberofinvoicespaid + " = " + SMTableapvendorstatistics.lnumberofinvoicespaid + " + (" + Long.toString(lNumberOfInvoicesPaid) + ")"
    		//+ ", " + SMTableapvendorstatistics.lnumberofpayapplicationsusedforaveraging + " = " + SMTableapvendorstatistics.lnumberofpayapplicationsusedforaveraging + " + " + Long.toString(lNumberOfPays)
    		+ ", " + SMTableapvendorstatistics.lnumberofpayments + " = " + SMTableapvendorstatistics.lnumberofpayments + " + (" + sNumberOfPayments + ")"
    	;
    	//System.out.println("[1497276224] SQL = '" + SQL + "'");
    	try {
			Statement stmt = conn.createStatement();
			stmt.execute(SQL);
		} catch (Exception e) {
			throw new Exception("Error [1497028535] updating statistics with SQL: '" + SQL + "' - " + e.getMessage());
		}
    	
    	//TESTING - 
    	//if (true){
    	//	System.out.println("[1511966360] - Statistics SQL = '" + SQL + "'.");
    	//	throw new Exception("TESTING");
    	//}
    	return;
    }
    
    private void checkBatchEntries(SMLogEntry log, String sUserID, Connection conn) throws Exception{
    	if (bDebugMode){
    		log.writeEntry(
   				sUserID, 
   				SMLogEntry.LOG_OPERATION_APBATCHPOST, 
   				"Entering checkBatchEntries", "Batch #:" + getsbatchnumber(), "[1489707018]");
    	}
    	String sCheckResults = "";
		for (int i = 0; i < m_arrBatchEntries.size(); i++){
			//Check each entry
			APBatchEntry entry = m_arrBatchEntries.get(i);
			try {
				checkIndividualEntry(
					entry, 
					conn,
					sUserID);
			} catch (Exception e) {
				//Record any check errors we run across:
				sCheckResults += "  Error [1489707272] on entry " + entry.getsentrynumber() + e.getMessage() + ".";
			}
		}
    	if (sCheckResults.compareToIgnoreCase("") != 0){
    		throw new Exception(sCheckResults);
    	}
    	return;
    }
    private void checkIndividualEntry(
    	APBatchEntry entry,
    	Connection conn,
    	String sUserID
    	) throws Exception{
       	//Check every entry to make sure they are all in balance - if any aren't, add the error message

    	//Check that each entry is balanced:
    	try {
			entry.validate_entry_totals_before_posting(conn);
		} catch (Exception e) {
			throw new Exception("Error [1491504455] - " + e.getMessage());
		}
    	
		//Check that the transaction date is within the allowed posting period:
		SMOption opt = new SMOption();
		if (!opt.load(conn)){
			throw new Exception("Error [1489757232] - could not check posting period - " + opt.getErrorMessage() + ".");
		}
		try {
			opt.checkDateForPosting(
				entry.getsdatentrydate(), 
				"ENTRY DATE", 
				conn, 
				sUserID
			);
		} catch (Exception e2) {
			throw new Exception("Error [1489757233] on Entry " + entry.getsentrynumber() + " - " 
				+ SMTableapbatches.getBatchTypeLabel(Integer.parseInt(getsbatchtype())) 
				+ " " + entry.getsdocnumber() + ", " + entry.getsentrydescription()
				+ " - " + e2.getMessage() + "."); 
		}
    	
    	return;
    }
    private void setPostingFlag(Connection conn, String sUserID) throws Exception{
    	//First check to make sure no one else is posting:
    	try{
    		String SQL = "SELECT * FROM " + SMTableapoptions.TableName;
    		ResultSet rsAPOptions = clsDatabaseFunctions.openResultSet(SQL, conn);
    		if (!rsAPOptions.next()){
        		throw new Exception("Error [1489704622] - could not get AP Options record.");
    		}else{
    			if(rsAPOptions.getLong(SMTableapoptions.ibatchpostinginprocess) == 1){
    				throw new Exception("Error [1489704623] - A previous posting is not completed - "
    					+ SMUtilities.getFullNamebyUserID(rsAPOptions.getString(SMTableapoptions.luserid), conn) + " has been "
    					+ rsAPOptions.getString(SMTableapoptions.sprocess) + " "
    					+ "since " + rsAPOptions.getString(SMTableapoptions.datstartdate) + "."
    				);
    			}
    		}
    		rsAPOptions.close();
    	}catch (SQLException e){
    		throw new Exception("Error [1489704624] checking for previous posting - " + e.getMessage());
    	}
    	//If not, then set the posting flag:
    	try{
    		String SQL = "UPDATE " + SMTableapoptions.TableName 
    			+ " SET " + SMTableapoptions.ibatchpostinginprocess + " = 1"
    			+ ", " + SMTableapoptions.datstartdate + " = NOW()"
    			+ ", " + SMTableapoptions.sprocess 
    				+ " = 'POSTING AP BATCH NUMBER " + getsbatchnumber() + "'"
       			+ ", " + SMTableapoptions.luserid 
    				+ " = " + sUserID
    		;
    		if (!clsDatabaseFunctions.executeSQL(SQL, conn)){
    			throw new Exception("Error [1489704625] setting posting flag in AP Options");
        		
    		}
    	}catch (SQLException e){
    		throw new Exception("Error [1489704626] setting posting flag in AP Options - " + e.getMessage());
    		
    	}
    }
    private void unsetPostingFlag(Connection conn) throws Exception{
    	try{
    		String SQL = "UPDATE " + SMTableapoptions.TableName 
    		+ " SET " + SMTableapoptions.ibatchpostinginprocess + " = 0"
    		+ ", " + SMTableapoptions.datstartdate + " = '0000-00-00 00:00:00'"
    		+ ", " + SMTableapoptions.sprocess + " = ''"
    			+ ", " + SMTableapoptions.luserid + " = 0"
    		;
    		if (!clsDatabaseFunctions.executeSQL(SQL, conn)){
    			throw new Exception("Error [1489704631] clearing posting flag in AP Options");
    		}
    	}catch (SQLException e){
    		throw new Exception("Error [1489704632] clearing posting flag in AP Options - " + e.getMessage());
    	}
    }
    
	public String getsbatchnumber(){
		return m_sbatchnumber;
	}
	public void setsbatchnumber(String sBatchNumber){
		m_sbatchnumber = sBatchNumber;
	}

	public String getsbatchdate(){
		return m_sbatchdate;
	}
	public String getsbatchdateInSQLFormat() throws Exception{
		if (m_sbatchdate.compareToIgnoreCase("") == 0){
			return SMUtilities.EMPTY_SQL_DATETIME_VALUE;
		}else{
			return clsDateAndTimeConversions.convertDateFormat(m_sbatchdate, SMUtilities.DATE_FORMAT_FOR_DISPLAY, SMUtilities.DATETIME_FORMAT_FOR_SQL, SMUtilities.EMPTY_SQL_DATETIME_VALUE);
		}
	}
	
	public void setsbatchdate(String sBatchDate){
		m_sbatchdate = sBatchDate;
	}

	public String getsbatchstatus(){
		return m_sbatchstatus;
	}
	public void setsbatchstatus(String sBatchStatus){
		m_sbatchstatus = sBatchStatus;
	}

	public String getsbatchdescription(){
		return m_sbatchdescription;
	}
	public void setsbatchdescription(String sBatchDescription){
		m_sbatchdescription = sBatchDescription;
	}

	public String getsbatchtype(){
		return m_sbatchtype;
	}
	public void setsbatchtype(String sBatchType){
		m_sbatchtype = sBatchType;
	}

	public String getslasteditdate(){
		return m_slasteditdate;
	}
	public String getslasteditdateInSQLFormat() throws Exception{
		if (m_slasteditdate.compareToIgnoreCase("") == 0){
			return SMUtilities.EMPTY_SQL_DATETIME_VALUE;
		}else{
			return clsDateAndTimeConversions.convertDateFormat(m_slasteditdate, SMUtilities.DATETIME_FORMAT_FOR_DISPLAY, SMUtilities.DATETIME_FORMAT_FOR_SQL, SMUtilities.EMPTY_SQL_DATETIME_VALUE);
		}
	}
	public void setslasteditdate(String sLastEditDate){
		m_slasteditdate = sLastEditDate;
	}

	public String getsbatchlastentry(){
		return m_sbatchlastentry;
	}
	public void setsbatchlastentry(String sBatchLastEntry){
		m_sbatchlastentry = sBatchLastEntry;
	}

	public String getlcreatedby(){
		return m_lcreatedby;
	}
	public void setlcreatedby(String sCreatedBy){
		m_lcreatedby = sCreatedBy;
	}

	public String getllasteditedby(){
		return m_llasteditedby;
	}
	public void setllasteditedby(String sLastEditedBy){
		m_llasteditedby = sLastEditedBy;
	}
	public String getsposteddate(){
		return m_sdatpostdate;
	}
	public String getsposteddateInSQLFormat() throws Exception{
		if (m_sdatpostdate.compareToIgnoreCase("") == 0){
			return SMUtilities.EMPTY_SQL_DATETIME_VALUE;
		}else{
			if (m_sdatpostdate.compareToIgnoreCase(SMUtilities.EMPTY_DATETIME_VALUE) == 0){
				return SMUtilities.EMPTY_SQL_DATETIME_VALUE;
			}
			//System.out.println("[1517597657] convertDateFormat = " + SMUtilities.convertDateFormat(m_sdatpostdate, SMUtilities.DATETIME_FORMAT_FOR_DISPLAY, SMUtilities.DATETIME_FORMAT_FOR_SQL, SMUtilities.EMPTY_SQL_DATETIME_VALUE));
			return clsDateAndTimeConversions.convertDateFormat(m_sdatpostdate, SMUtilities.DATETIME_FORMAT_FOR_DISPLAY, SMUtilities.DATETIME_24HR_FORMAT_FOR_SQL, SMUtilities.EMPTY_SQL_DATETIME_VALUE);
		}
	}
	public void setsposteddate(String sPostedDate){
		m_sdatpostdate = sPostedDate;
	}
	public String getsbatchstatuslabel (){
		return SMBatchStatuses.Get_Transaction_Status(Integer.parseInt(m_sbatchstatus));
	}
	public boolean bAllChecksHaveBeenFinalized(){
		//Returns true for invoice batches, etc., and for any payment batches that don't have checks to be printed
		//But if there is at least ONE entry in the batch that needs a check printed, and that check has NOT been finalized, then this returns false
		for (int i = 0; i < m_arrBatchEntries.size(); i++){
			APBatchEntry entry = m_arrBatchEntries.get(i);
			//Ignore apply-tos:
			if (entry.getientrytype() != SMTableapbatchentries.ENTRY_TYPE_PAYMENT_APPLYTO){
				if (entry.getsiprintcheck().compareToIgnoreCase("1") == 0){
					if (entry.getsiprintingfinalized().compareToIgnoreCase("0") == 0){
						return false;
					}
				}
			}
		}
		return true;
	}

	public boolean bEditable(){
		boolean bEditable = true;
		//Indicates whether it's an 'editable' batch, meaning that 
		//it's not posted or deleted and can therefore be edited:
		if (m_sbatchstatus.compareToIgnoreCase(Integer.toString(SMBatchStatuses.DELETED)) ==0 ){
			bEditable = false;
		}
		if (m_sbatchstatus.compareToIgnoreCase(Integer.toString(SMBatchStatuses.POSTED)) ==0 ){
			bEditable = false;
		}

		return bEditable;
	}
	
	public BigDecimal getBatchTotal(){
		BigDecimal bdBatchTotal = new BigDecimal("0.00");
		for (int i = 0; i < m_arrBatchEntries.size(); i++){
			bdBatchTotal = bdBatchTotal.add(new BigDecimal(m_arrBatchEntries.get(i).getsentryamount().replaceAll(",", "")));
		}
		return bdBatchTotal;
	}
	public ArrayList<APBatchEntry> getBatchEntryArray(){
		return m_arrBatchEntries;
	}
	public static String stripOutInvalidEntryErrors(String sErrorString){
		String s = "";
		
		if (
			(sErrorString.contains(INVALID_ENTRY_ERROR_STARTING_TAG))
			&& (sErrorString.contains(INVALID_ENTRY_ERROR_ENDING_TAG))
		){
			s = sErrorString.substring(sErrorString.indexOf(INVALID_ENTRY_ERROR_STARTING_TAG) + INVALID_ENTRY_ERROR_STARTING_TAG.length(), 
				sErrorString.indexOf(INVALID_ENTRY_ERROR_ENDING_TAG)).trim();
		}else{
			s = sErrorString;
		}
		
		return s;
		
	}
	public static int getBatchTypeFromEntryType(int iEntryType){

		if (
			(iEntryType == SMTableapbatchentries.ENTRY_TYPE_INV_CREDITNOTE)
			|| (iEntryType == SMTableapbatchentries.ENTRY_TYPE_INV_DEBITNOTE)
			|| (iEntryType == SMTableapbatchentries.ENTRY_TYPE_INV_INVOICE)
		){
			return SMTableapbatches.AP_BATCH_TYPE_INVOICE;
		}
		if (
				(iEntryType == SMTableapbatchentries.ENTRY_TYPE_PAYMENT_APPLYTO)
				|| (iEntryType == SMTableapbatchentries.ENTRY_TYPE_PAYMENT_MISCPAYMENT)
				|| (iEntryType == SMTableapbatchentries.ENTRY_TYPE_PAYMENT_PAYMENT)
				|| (iEntryType == SMTableapbatchentries.ENTRY_TYPE_PAYMENT_PREPAYMENT)
		){
			return SMTableapbatches.AP_BATCH_TYPE_PAYMENT;
		}

		if (iEntryType == SMTableapbatchentries.ENTRY_TYPE_REVERSAL){
			return SMTableapbatches.AP_BATCH_TYPE_REVERSALS;
		}

		return -1;
	}
	public String dumpData(){
		String s = "";
		
		s += "Batch date: '" + getsbatchdate() + "'\n"
			+ "Desc: '" + getsbatchdescription() + "'\n"
			+ "Last entry: '" + getsbatchlastentry() + "'\n"
			+ "Batch number: '" + getsbatchnumber() + "'\n"
			+ "Status: '" + getsbatchstatus() + "'\n"
			+ "Batch type: '" + getsbatchtype() + "'\n"
			+ "Created by: '" + getlcreatedby() + "'\n"
			+ "Last edit date: '" + getslasteditdate() + "'\n"
			+ "Edited by: '" + getllasteditedby() + "'\n"
			+ "Posted: '" + getsposteddate() + "'\n"
			+ "Checks printed: '" + bAllChecksHaveBeenFinalized() + "\n"
		;
		for (int i = 0; i < m_arrBatchEntries.size(); i++){
			s += "Entry " + (i + 1) + ": " + m_arrBatchEntries.get(i).dumpData();
		}
		return s;
	}
	private String buildGLTransactionEntryDescription(APBatchEntry apentry) throws Exception{
		String sEntryDescription = "";
		if (apentry.getientrytype() == SMTableapbatchentries.ENTRY_TYPE_INV_CREDITNOTE){
			sEntryDescription = apentry.getsvendoracct()
				+ " " + apentry.getsvendorname()
			;
		}
		if (apentry.getientrytype() == SMTableapbatchentries.ENTRY_TYPE_INV_DEBITNOTE){
			sEntryDescription = apentry.getsvendoracct()
				+ " " + apentry.getsvendorname()
			;
		}
		if (apentry.getientrytype() == SMTableapbatchentries.ENTRY_TYPE_INV_INVOICE){
			sEntryDescription = apentry.getsvendoracct()
				+ " " + apentry.getsvendorname()
			;
		}
		if (apentry.getientrytype() == SMTableapbatchentries.ENTRY_TYPE_PAYMENT_APPLYTO){
			
		}
		if (apentry.getientrytype() == SMTableapbatchentries.ENTRY_TYPE_PAYMENT_MISCPAYMENT){
			sEntryDescription = apentry.getsvendoracct()
				+ " " + apentry.getsvendorname()
				+ " CK# " + apentry.getschecknumber()
			;
		}
		if (apentry.getientrytype() == SMTableapbatchentries.ENTRY_TYPE_PAYMENT_PAYMENT){
			sEntryDescription = apentry.getsvendoracct()
				+ " " + apentry.getsvendorname()
				+ " CK# " + apentry.getschecknumber()
			;
		}
		if (apentry.getientrytype() == SMTableapbatchentries.ENTRY_TYPE_PAYMENT_PREPAYMENT){
			sEntryDescription = apentry.getsvendoracct()
				+ " " + apentry.getsvendorname()
				+ " CK# " + apentry.getschecknumber()
			;				
		}
		if (apentry.getientrytype() == SMTableapbatchentries.ENTRY_TYPE_REVERSAL){
			
		}
		
		return sEntryDescription;
	}
	private String getAPTransactionID(
		String sBatchEntryID,
		Connection conn) throws Exception {
		
		String sTransactionID = "";
		
		String SQL = "SELECT"
			+ " " + SMTableaptransactions.lid
			+ " FROM " + SMTableaptransactions.TableName
			+ " WHERE ("
				+ "(" + SMTableaptransactions.lbatchentryid + " = " + sBatchEntryID + ")"
			+ ")"
		;
		try {
			ResultSet rs = ServletUtilities.clsDatabaseFunctions.openResultSet(SQL, conn);
			if (rs.next()){
				sTransactionID = Long.toString(rs.getLong(SMTableaptransactions.lid));
			}
			rs.close();
		} catch (Exception e) {
			throw new Exception("Error [20192271447402] " + "Could not get AP Transaction ID with SQL: '" + SQL + "' - " + e.getMessage() + ".");
		}
			
		if (sTransactionID.compareToIgnoreCase("") == 0){
			throw new Exception("Error [20192271446547] " + "Could not get AP Transaction ID for Entry ID '" + sBatchEntryID + ".");
		}
		
		return sTransactionID;
		
	}
	private void initializeVariables(){
		m_sbatchnumber = "-1";
		m_sbatchdate = clsDateAndTimeConversions.now(SMUtilities.DATE_FORMAT_FOR_DISPLAY);
		m_sbatchstatus = Integer.toString(SMBatchStatuses.ENTERED);
		m_sbatchdescription = "INITIALIZED BATCH";
		m_sbatchtype = Integer.toString(SMTableapbatches.AP_BATCH_TYPE_INVOICE);
		m_slasteditdate = SMUtilities.EMPTY_DATETIME_VALUE;
		m_sbatchlastentry = "0";
		m_lcreatedby = "0";
		m_llasteditedby = "0";
		m_sdatpostdate = SMUtilities.EMPTY_DATETIME_VALUE;
		m_arrBatchEntries = new ArrayList<APBatchEntry>(0);
	}
}
