package smgl;

import java.io.PrintWriter;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import SMClasses.SMBatchStatuses;
import SMClasses.SMLogEntry;
import SMDataDefinition.SMTableglaccounts;
import SMDataDefinition.SMTablegloptions;
import SMDataDefinition.SMTablegltransactionbatchentries;
import SMDataDefinition.SMTablegltransactionbatches;
import SMDataDefinition.SMTablegltransactionbatchlines;
import SMDataDefinition.SMTablegltransactionlines;
import ServletUtilities.clsDBServerTime;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsDateAndTimeConversions;
import ServletUtilities.clsManageRequestParameters;
import ServletUtilities.clsValidateFormFields;
import smar.SMOption;
import smcontrolpanel.SMUtilities;

public class GLTransactionBatch {

	public final static String OBJECT_NAME = "GL Transaction Batch";
	public final static String INVALID_ENTRY_ERROR_STARTING_TAG = "STARTINVALIDENTRYERROR";
	public final static String INVALID_ENTRY_ERROR_ENDING_TAG = "ENDINVALIDENTRYERROR";
	
	private String m_sbatchnumber;
	private String m_sbatchdate;
	private String m_sbatchstatus;
	private String m_sbatchdescription;
	private String m_slasteditdate;
	private String m_sbatchlastentry;
	private String m_lcreatedby;
	private String m_screatedbyfullname;
	private String m_llasteditedby;
	private String m_slasteditedbyfullname;
	private String m_sdatpostdate;
	private static final boolean bDebugMode = false;

	private ArrayList<GLTransactionBatchEntry>m_arrBatchEntries;

	public GLTransactionBatch(
			String sBatchNumber
			) 
	{
		initializeVariables();
		setsbatchnumber(sBatchNumber);
	}
	public GLTransactionBatch(HttpServletRequest req){
		//Read the batch fields from a servlet request:
		initializeVariables();
		
		setsbatchdate(clsManageRequestParameters.get_Request_Parameter(SMTablegltransactionbatches.datbatchdate, req).replace("&quot;", "\""));
		if(getsbatchdate().compareToIgnoreCase("") == 0){
			setsbatchdate(clsDateAndTimeConversions.now(SMUtilities.DATE_FORMAT_FOR_DISPLAY));
		}
		setsbatchdescription(clsManageRequestParameters.get_Request_Parameter(SMTablegltransactionbatches.sbatchdescription, req).replace("&quot;", "\""));
		setsbatchlastentry(clsManageRequestParameters.get_Request_Parameter(SMTablegltransactionbatches.lbatchlastentry, req).replace("&quot;", "\""));
		if (getsbatchlastentry().compareToIgnoreCase("") == 0){
			setsbatchlastentry("0");
		}
		setsbatchnumber(clsManageRequestParameters.get_Request_Parameter(SMTablegltransactionbatches.lbatchnumber, req).replace("&quot;", "\""));
		if(getsbatchnumber().compareToIgnoreCase("") == 0){
			setsbatchnumber("-1");
		}
		setsbatchstatus(clsManageRequestParameters.get_Request_Parameter(SMTablegltransactionbatches.ibatchstatus, req).replace("&quot;", "\""));
		if (getsbatchstatus().compareToIgnoreCase("") == 0){
			setsbatchstatus(Integer.toString(SMBatchStatuses.ENTERED));
		}
		setlcreatedby(clsManageRequestParameters.get_Request_Parameter(SMTablegltransactionbatches.lcreatedby, req).replace("&quot;", "\""));
		setslasteditdate(clsManageRequestParameters.get_Request_Parameter(SMTablegltransactionbatches.datlasteditdate, req).replace("&quot;", "\""));
		if(getslasteditdate().compareToIgnoreCase("") == 0){
			setslasteditdate(SMUtilities.EMPTY_DATETIME_VALUE);
		}
		setllasteditedby(clsManageRequestParameters.get_Request_Parameter(SMTablegltransactionbatches.llasteditedby, req).replace("&quot;", "\""));
		setsposteddate(clsManageRequestParameters.get_Request_Parameter(SMTablegltransactionbatches.datpostdate, req).replace("&quot;", "\""));
		if (getsposteddate().compareToIgnoreCase("") == 0){
			setsposteddate(SMUtilities.EMPTY_DATETIME_VALUE);
		}
	}
	public void save_without_data_transaction (
		Connection conn, 
		String sUserID, 
		String sUsersFullName, 
		boolean bBatchIsBeingPosted) throws Exception{

		//System.out.println("[1489623830]");
		//See what's in the batch and lines at this point:
		//System.out.println("[1493666897] - batch dump:\n" + dumpData()); 
		try {
			validate_fields(conn, sUserID, bBatchIsBeingPosted);
		} catch (Exception e1) {
			throw new Exception("Error [1555339489] validating batch fields - " + e1.getMessage());
		}
		
		//System.out.println("[1489623831]");
		String SQL = "";
		if (getsbatchnumber().compareToIgnoreCase("-1") == 0){
			//Add a new batch:
			SQL = "INSERT into " + SMTablegltransactionbatches.TableName
				+ " (" 
				+ SMTablegltransactionbatches.datbatchdate
				+ ", " + SMTablegltransactionbatches.datlasteditdate
				+ ", " + SMTablegltransactionbatches.datpostdate
				+ ", " + SMTablegltransactionbatches.ibatchstatus
				+ ", " + SMTablegltransactionbatches.lbatchlastentry
				+ ", " + SMTablegltransactionbatches.sbatchdescription
				+ ", " + SMTablegltransactionbatches.lcreatedby
				+ ", " + SMTablegltransactionbatches.llasteditedby
				+ ", " + SMTablegltransactionbatches.slasteditedbyfullname
				+ ", " + SMTablegltransactionbatches.screatedbyfullname
				+ ")"
				+ " VALUES ("
				+ "'" + getsbatchdateInSQLFormat() + "'"
				+ ", NOW()"
				+ ", '" + getsposteddateInSQLFormat() + "'"
				+ ", " + getsbatchstatus()
				+ ", " + getsbatchlastentry()
				+ ", '" + clsDatabaseFunctions.FormatSQLStatement(getsbatchdescription()) + "'"
				+ ", " + sUserID
				+ ", " + sUserID
				+ ", '" + sUsersFullName + "'"
				+ ", '" + sUsersFullName + "'"
				+ ")"
			;
		}else{
			SQL = " UPDATE " + SMTablegltransactionbatches.TableName + " SET"
			+ " " + SMTablegltransactionbatches.datbatchdate + " = '" + getsbatchdateInSQLFormat() + "'"
			+ ", " + SMTablegltransactionbatches.datlasteditdate + " = NOW()"
			+ ", " + SMTablegltransactionbatches.datpostdate + " = '" + getsposteddateInSQLFormat() + "'"
			+ ", " + SMTablegltransactionbatches.ibatchstatus + " = " + getsbatchstatus()
			+ ", " + SMTablegltransactionbatches.lbatchlastentry + " = " + getsbatchlastentry()
			+ ", " + SMTablegltransactionbatches.sbatchdescription + " = '" + clsDatabaseFunctions.FormatSQLStatement(getsbatchdescription()) + "'"
			+ ", " + SMTablegltransactionbatches.llasteditedby + " = " + sUserID + ""
			+ ", " + SMTablegltransactionbatches.slasteditedbyfullname + " = '" + sUsersFullName + "'"
			+ " WHERE ("
				+ "(" + SMTablegltransactionbatches.lbatchnumber + " = " + getsbatchnumber() + ")"
			+ ")"
			;
		}
		//System.out.println("[1517597656] GL Batch save SQL = '" + SQL + "'.");
		Statement stmt = conn.createStatement();
		try {
			stmt.execute(SQL);
		} catch (Exception e) {
			throw new Exception("Error [1555339570] updating GL transaction batch with SQL: '" + SQL + "' - " + e.getMessage());
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
				throw new Exception("Error [1555339571] Could not get last ID number - " + e.getMessage());
			}
			//If something went wrong, we can't get the last ID:
			if (getsbatchnumber().compareToIgnoreCase("0") == 0){
				throw new Exception("Error [1555339572] Could not get last ID number.");
			}
		}
		//Finally, save the entries:
		for (int i = 0; i < m_arrBatchEntries.size(); i++){
			GLTransactionBatchEntry entry = m_arrBatchEntries.get(i);
			try {
				entry.setsbatchnumber(getsbatchnumber());
				//entry.setsentrynumber(Integer.toString(i + 1)); - this is already set in the 'validate fields' function
				entry.save_without_data_transaction(conn, sUserID, bBatchIsBeingPosted);
			} catch (Exception e) {
				throw new Exception("Error [1555339573] saving entry number " + entry.getsentrynumber() + " - " + e.getMessage());
			}
		}
		
		//Remove any entries with entry numbers HIGHER than our highest one - this can happen if we remove an entry
		//in the batch - then there would be an entry record that should no longer be in the batch:
		String sHighestValidEntryNumber = "0";
		if (m_arrBatchEntries.size() > 0){
			sHighestValidEntryNumber = m_arrBatchEntries.get(m_arrBatchEntries.size() - 1).getsentrynumber();
		}
		SQL = "DELETE FROM " + SMTablegltransactionbatchentries.TableName
			+ " WHERE ("
				+ "(" + SMTablegltransactionbatchentries.lbatchnumber + " = " + getsbatchnumber() + ")"
				+ " AND (" + SMTablegltransactionbatchentries.lentrynumber + " > " + sHighestValidEntryNumber + ")"
			+ ")"
		;
		
		//System.out.println("[1489623835] - SQL = '" + SQL + "'");
		//System.out.println("[1489623845] - SQLm_arrBatchEntries.size() = '" + m_arrBatchEntries.size() + "'");
		
		stmt = conn.createStatement();
		try {
			stmt.execute(SQL);
		} catch (Exception e) {
			throw new Exception("Error [1555339574] removing discarded GL transaction batch entries with SQL: '" + SQL + "' - " + e.getMessage());
		}
		
		//Now delete any entry lines that are orphaned:
		SQL = "DELETE GLBATCHLINES.* FROM " + SMTablegltransactionbatchlines.TableName + " GLBATCHLINES"
			+ " LEFT JOIN " + SMTablegltransactionbatchentries.TableName + " GLBATCHENTRIES"
			+ " ON (GLBATCHLINES." + SMTablegltransactionbatchlines.lbatchnumber + " = GLBATCHENTRIES." + SMTablegltransactionbatchentries.lbatchnumber + ")"
			+ " AND (GLBATCHLINES." + SMTablegltransactionbatchlines.lentrynumber + " = GLBATCHENTRIES." + SMTablegltransactionbatchentries.lentrynumber + ")"
			+ " WHERE (GLBATCHENTRIES." + SMTablegltransactionbatchentries.lentrynumber + " IS NULL)"	
		;
		
		stmt = conn.createStatement();
		try {
			stmt.execute(SQL);
		} catch (Exception e) {
			throw new Exception("Error [1555339575] removing orphaned GL transaction batch entry lines with SQL: '" + SQL + "' - " + e.getMessage());
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
			clsDatabaseFunctions.freeConnection(context, conn, "[1555339576]");
			throw new Exception("Error [1555339577] could not get data connection.");
		}

		try {
			save_without_data_transaction(conn, sUserID, sUsersFullName, bBatchIsBeingPosted);
		} catch (Exception e) {
			clsDatabaseFunctions.rollback_data_transaction(conn);
			clsDatabaseFunctions.freeConnection(context, conn, "[1555339578]");
			throw new Exception("Error [1555339579] saving - " + e.getMessage());
		}
		clsDatabaseFunctions.commit_data_transaction(conn);
		clsDatabaseFunctions.freeConnection(context, conn, "[1555339580]");
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
			m_sbatchdescription = clsValidateFormFields.validateStringField(m_sbatchdescription, SMTablegltransactionbatches.sBatchDescriptionLength, "Description", false);
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
        	sResult += "  " + "Error [1555340486] loading SM Options to check batch date range - " + opt.getErrorMessage() + ".";
        }else{
            try {
    			opt.checkDateForPosting(m_sbatchdate, "Batch Date", conn, sUserID);
    		} catch (Exception e) {
    			sResult += "  " + "Error [1555340487]  - " + e.getMessage() + ".";
    		}
        }
		
		//Validate the entries:
		//System.out.println("[1490382128] m_arrBatchEntries.size() = " + m_arrBatchEntries.size());
		for (int i = 0; i < m_arrBatchEntries.size(); i++){
			GLTransactionBatchEntry entry = m_arrBatchEntries.get(i);
			entry.setsentrynumber(Integer.toString(i + 1));
			try {
				entry.validate_fields(conn, sUserID, bBatchIsBeingPosted);
			} catch (Exception e) {
				sResult += "  In entry " + Integer.toString(i + 1) + " - " + e.getMessage() + ".";
			}
		}
		
		if (sResult.compareToIgnoreCase("") != 0){
			//We'll surround the actual error message with tags so we can strip it out at the end:
			throw new Exception(INVALID_ENTRY_ERROR_STARTING_TAG + sResult + INVALID_ENTRY_ERROR_ENDING_TAG);
		}
	}
	public void addBatchEntry (GLTransactionBatchEntry entry){
		m_arrBatchEntries.add(entry);
	}
	public GLTransactionBatchEntry getEntryByEntryNumber(String sEntryNumber){
		for (int i = 0; i < m_arrBatchEntries.size(); i++){
			if (m_arrBatchEntries.get(i).getsentrynumber().compareToIgnoreCase(sEntryNumber) == 0){
				return m_arrBatchEntries.get(i);
			}
		}
		return null;
	}
	public String updateBatchEntry (
		GLTransactionBatchEntry entry,
		ServletContext context,
		String sDBID, 
		String sUserID,
		String sUserFullName
		) throws Exception{
		//First, load this batch:
		try {
			load(context, sDBID, sUserID);
		} catch (Exception e) {
			throw new Exception("Error [1555340521] loading batch to update entry - " + e.getMessage());
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
			clsDatabaseFunctions.freeConnection(context, conn, "[1555340522]");
			throw new Exception(e3.getMessage());
		}
		
		try {
			entry.save_without_data_transaction(conn, sUserID, false);
		} catch (Exception e1) {
			clsDatabaseFunctions.rollback_data_transaction(conn);
			clsDatabaseFunctions.freeConnection(context, conn, "[1555340523]");
			throw new Exception(e1.getMessage());
		}
		
		//System.out.println("[1494863200]\n" + entry.dumpData());
		
		//Now that the entry has been saved, re-load the batch to get the correct entry count, so we can update it:
		try {
			loadBatch(conn);
		} catch (Exception e) {
			clsDatabaseFunctions.rollback_data_transaction(conn);
			clsDatabaseFunctions.freeConnection(context, conn, "[1555340524]");
			throw new Exception("Error [1555340525] re-loading batch - " + e.getMessage());
		}
		
		String SQL = "UPDATE " + SMTablegltransactionbatches.TableName
			+ " SET " + SMTablegltransactionbatches.datlasteditdate + " = NOW()"
			+ ", " + SMTablegltransactionbatches.lbatchlastentry + " = " + m_arrBatchEntries.size()
			+ ", " + SMTablegltransactionbatches.llasteditedby + " = " + sUserID + ""
			+ ", " + SMTablegltransactionbatches.slasteditedbyfullname + " = '" + sUserFullName + "'"
			+ " WHERE ("
				+ "(" + SMTablegltransactionbatches.lbatchnumber + " = " + getsbatchnumber() + ")"
			+ ")"
		;
		try {
			Statement stmt = conn.createStatement();
			stmt.execute(SQL);
		} catch (Exception e2) {
			clsDatabaseFunctions.rollback_data_transaction(conn);
			clsDatabaseFunctions.freeConnection(context, conn, "[1555340526]");
			throw new Exception(e2.getMessage());
		}
		
		try {
			clsDatabaseFunctions.commit_data_transaction(conn);
		} catch (Exception e1) {
			clsDatabaseFunctions.rollback_data_transaction(conn);
			clsDatabaseFunctions.freeConnection(context, conn, "[1555340527]");
			throw new Exception(e1.getMessage());
		}
		
		clsDatabaseFunctions.freeConnection(context, conn, "[1555340528]");

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
			throw new Exception("Error [1555340529] getting connection to load batch number " + getsbatchnumber() + " - " + e.getMessage());
		}
		
		try {
			loadBatch(conn);
		} catch (Exception e) {
			throw new Exception("Error [1555340530] loading batch number " + getsbatchnumber() + " - " + e.getMessage());
		}
		
		clsDatabaseFunctions.freeConnection(context, conn, "[1555340531]");
		
	}
	
	public void loadBatch(Connection conn) throws Exception{
		String SQL = "SELECT * FROM " + SMTablegltransactionbatches.TableName
			+ " WHERE ("
				+ "(" + SMTablegltransactionbatches.lbatchnumber + " = " + getsbatchnumber() + ")"
			+ ")"
		;
		ResultSet rs = null;
		try {
			rs = clsDatabaseFunctions.openResultSet(SQL, conn);
			if (rs.next()){
				setsbatchdate(clsDateAndTimeConversions.resultsetDateTimeStringToFormattedString(
					rs.getString(SMTablegltransactionbatches.datbatchdate), SMUtilities.DATE_FORMAT_FOR_DISPLAY, SMUtilities.EMPTY_DATE_VALUE));
				setsbatchdescription(rs.getString(SMTablegltransactionbatches.sbatchdescription));
				setsbatchlastentry(Long.toString(rs.getLong(SMTablegltransactionbatches.lbatchlastentry)));
				setsbatchstatus(Integer.toString(rs.getInt(SMTablegltransactionbatches.ibatchstatus)));
				setlcreatedby(rs.getString(SMTablegltransactionbatches.lcreatedby));
				setslasteditdate(clsDateAndTimeConversions.resultsetDateTimeStringToFormattedString(
					rs.getString(SMTablegltransactionbatches.datlasteditdate), SMUtilities.DATETIME_FORMAT_FOR_DISPLAY, SMUtilities.EMPTY_DATETIME_VALUE));
				setllasteditedby(rs.getString(SMTablegltransactionbatches.llasteditedby));
				setsposteddate(clsDateAndTimeConversions.resultsetDateTimeStringToFormattedString(
					rs.getString(SMTablegltransactionbatches.datpostdate), SMUtilities.DATETIME_FORMAT_FOR_DISPLAY, SMUtilities.EMPTY_DATETIME_VALUE));
			}else{
				rs.close();
				throw new Exception("Error [1555340529] - No GL transaction batch found with batch number " + getsbatchnumber() + ".");
			}
			rs.close();
		} catch (Exception e1) {
			rs.close();
			throw new Exception("Error [1555340530] loading batch - " + e1.getMessage());
		}
		
		try {
			loadEntries(conn);
		} catch (Exception e) {
			throw new Exception(e.getMessage());
		}

	}

	public void loadEntries(ServletContext context, String sDBID, String sUserID) throws Exception{
		Connection conn;
		try {
			conn = clsDatabaseFunctions.getConnectionWithException(
				context, 
				sDBID, 
				"MySQL", 
				"GLTransactionBatch.loadEntries - user: " + sUserID);
		} catch (Exception e) {
			throw new Exception("Error [1555340531] getting connection to load GL Transaction Batch Entries - " + e.getMessage());
		}
		
		try {
			loadEntries(conn);
		} catch (Exception e) {
			clsDatabaseFunctions.freeConnection(context, conn, "[1555340532]");
			throw new Exception("Error [1489357919] loading GL Transaction Batch Entries - " + e.getMessage());
		}
		
		clsDatabaseFunctions.freeConnection(context, conn, "[155534053]");
	}
	private void loadEntries(Connection conn) throws Exception{
		//Load the entries:
		m_arrBatchEntries.clear();
		String SQL = "SELECT"
			+ " " + SMTablegltransactionbatchentries.lid
			+ " FROM " + SMTablegltransactionbatchentries.TableName 
			+ " WHERE ("
				+ "(" + SMTablegltransactionbatchentries.lbatchnumber + " = " + getsbatchnumber() + ")"
			+ ") ORDER BY " + SMTablegltransactionbatchentries.lentrynumber
		;
		ResultSet rs = null;
		try {
			rs = clsDatabaseFunctions.openResultSet(SQL, conn);
			while (rs.next()){
				GLTransactionBatchEntry entry = new GLTransactionBatchEntry();
				entry.setslid(Long.toString(rs.getLong(SMTablegltransactionbatchentries.lid)));
				entry.load(conn);
				addBatchEntry(entry);
			}
			rs.close();
		} catch (Exception e) {
			rs.close();
			throw new Exception("Error [155534054] loading batch entries - " + e.getMessage());
		}
	}
    public void flag_as_deleted (
    		String sBatchNumber,
    		ServletContext context, 
    		String sDBID,
    		String sUserFullName
    		) throws Exception{
        
    	    String SQL = "UPDATE "
				+ SMTablegltransactionbatches.TableName
				+ " SET "
				+ SMTablegltransactionbatches.ibatchstatus + " = " + SMBatchStatuses.DELETED
				+ " WHERE ("
					+ "(" + SMTablegltransactionbatches.lbatchnumber + " = " + sBatchNumber + ")"
				+ ")";
    	    try {
				if(!clsDatabaseFunctions.executeSQL(
					SQL, 
					context, 
					sDBID, 
					"MySQL", 
					"GLTransactionBatch.flag_as_deleted - userID: " + sUserFullName
				)){
					throw new Exception("Error [155534055] - could not update batch number " + sBatchNumber + " as deleted ");
				}
			} catch (Exception e) {
				throw new Exception("Error [155534056] - could not update batch number " + sBatchNumber + " as deleted with SQL: " + SQL + " - " + e.getMessage());
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
			throw new Exception("Error [155534057] loading batch number " + sBatchNumber 
				+ " to delete entry nuber " + sEntryNumber + " - " + e.getMessage());
		}
    	removeEntryByEntryNumber(sEntryNumber);
    	try {
			save_with_data_transaction(context, sDBID, sUserID, sUsersFullName, false);
		} catch (Exception e) {
			throw new Exception("Error [155534058] saving batch number " + sBatchNumber 
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
    			throw new Exception("Error [155534059] loading batch number " + sBatchNumber 
    				+ " to delete line number " + sEntryLineNumber + " on entry number " + sEntryNumber + " - " + e.getMessage());
    		}
        	//System.out.println("[1490316268] - sBatchNumber = " + sBatchNumber + ", entry number = " + sEntryNumber + ", line number = " + sEntryLineNumber);
        	removeLineByLineNumber(sEntryNumber, sEntryLineNumber);
        	try {
    			save_with_data_transaction(context, sDBID, sUserID, sUserFullName, false);
    		} catch (Exception e) {
    			throw new Exception("Error [155534060] saving batch number " + sBatchNumber 
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
    		throw new Exception("Error [155534061] - entry number '" + sEntryNumber + "' was not found in the batch.");
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
    		throw new Exception("Error [155534062] getting connection - " + e.getMessage());
    	}

    	clsDatabaseFunctions.start_data_transaction(conn);
    	try {
			post_with_connection(conn, sUserID, sUserFullName);
		} catch (Exception e) {
			clsDatabaseFunctions.rollback_data_transaction(conn);
	    	try {
	    		unsetPostingFlag(conn);
	    	} catch (Exception e1) {
	    		clsDatabaseFunctions.freeConnection(context, conn, "[155534063]");
	    		throw new Exception("Error [1489704632] UNsetting GL posting flag - " + e1.getMessage());
	    	}
	    	clsDatabaseFunctions.freeConnection(context, conn, "[155534064]");
			throw new Exception("Error [155534065] posting - " + e.getMessage());
		}
    	clsDatabaseFunctions.commit_data_transaction(conn);
    	try {
    		unsetPostingFlag(conn);
    	} catch (Exception e) {
    		clsDatabaseFunctions.freeConnection(context, conn, "[155534066]");
    		throw new Exception("Error [1555347589] UNsetting GL posting flag - " + e.getMessage());
    	}
    	clsDatabaseFunctions.freeConnection(context, conn, "[155534067]");
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
			throw new Exception("Error [1555347590] loading batch number " + getsbatchnumber() + " - " + e.getMessage());
		}
    	
    	if (getsbatchstatus().compareToIgnoreCase(Integer.toString(SMBatchStatuses.POSTED)) == 0){
    		throw new Exception("This batch is already posted");
    	}
    	if (getsbatchstatus().compareToIgnoreCase(Integer.toString(SMBatchStatuses.DELETED)) == 0){
    		throw new Exception("Deleted batches cannot be posted");
    	}

    	SMLogEntry log = new SMLogEntry(conn);

    	try {
    		setPostingFlag(conn, sUserID);
    	} catch (Exception e) {
    		throw new Exception("Error [1555347591] - " + e.getMessage());
    	}

    	try {
    		post_without_data_transaction(conn, sUserID, sUserFullName, log);
    	} catch (Exception e1) {
    		throw new Exception("Error [1555347592] posting - " + e1.getMessage());
    	}
    	
    	//Need to unset posting flag:
    	try {
    		unsetPostingFlag(conn);
    	} catch (Exception e) {
    		throw new Exception("Error [1555347593] UNsetting GL posting flag - " + e.getMessage());
    	}
    }
    
	private void post_without_data_transaction(Connection conn, String sUserID, String sUserFullName, SMLogEntry log) throws Exception{

    	//The connection will ALREADY have a data transaction started....
    	
    	//If there are no entries, don't post
    	if (m_arrBatchEntries.size() == 0){
    		throw new Exception("Error [1555347594] - batch has no entries in it and can't be posted.");
    	}
    	
    	//Check all of the entries first to make sure they can be posted:
    	try {
			checkBatchEntries(log, sUserID, conn);
		} catch (Exception e1) {
			throw new Exception("Error [1555347595] - " + e1.getMessage());
		}
    	
    	//Next, create transactions for all of the entries:
    	try {
			createEntryTransactions(log, sUserID, conn);
		} catch (Exception e) {
			throw new Exception("Error [1555347596] creating entry transactions - " + e.getMessage());
		}

    	//Update the batch:
    	setsbatchstatus(Integer.toString(SMBatchStatuses.POSTED));
    	clsDBServerTime dt = new clsDBServerTime(conn);
    	setsposteddate(dt.getCurrentDateTimeInSelectedFormat(SMUtilities.DATETIME_FORMAT_FOR_DISPLAY));
    	//System.out.println("[1517597655] this.getsposteddate() = " + this.getsposteddate());
    	
    	if (bDebugMode){    	
    		log.writeEntry(
    			sUserID,
    			SMLogEntry.LOG_OPERATION_GLBATCHPOST,
        		"In GL post_without_data_transaction Batch #:" + getsbatchnumber(), 
        		"Going into save_without_data_transaction",
        		"[1555347597]"
    		);
    	}
    	try {
			save_without_data_transaction(conn, sUserID, sUserFullName, true);
		} catch (Exception e) {
			throw new Exception("Error [1555347598] updating batch - " + e.getMessage());
		}
 
    	if (bDebugMode){
    		log.writeEntry(
    			sUserID,
    			SMLogEntry.LOG_OPERATION_GLBATCHPOST,
        		"In GL post_without_data_transaction Batch #:" + getsbatchnumber(), 
        		"After successful save_without_data_transaction",
        		"[1555347599]"
    		);
    	}
    	
    	return;
    }

    private void createEntryTransactions(SMLogEntry log, String sUserID, Connection conn) throws Exception{
    	if (bDebugMode){
	    	log.writeEntry(
	        		sUserID, 
	        		SMLogEntry.LOG_OPERATION_GLBATCHPOST, 
	        		"In post_without_data_transaction Batch #:" + getsbatchnumber()
	        		+ " Going into createTransactions",
	        		"",
	        		"[1555347600]"
	        );
    	}
    	for (int i = 0; i < m_arrBatchEntries.size(); i++){
			GLTransactionBatchEntry entry = m_arrBatchEntries.get(i);
    		createIndividualTransaction(
    			entry, 
    			conn
    		);
    	}
    }
    private void createIndividualTransaction(
    		GLTransactionBatchEntry entry,
    		Connection conn) throws Exception{
    	
    	for (int i = 0; i < entry.getLineArray().size(); i++){
    		GLTransactionBatchLine line = entry.getLineArray().get(i);
    		//Get the normal GL account type:
        	GLAccount acct = new GLAccount(line.getsacctid());
        	
        	String sTransactionAmt = "0.00";
			if (Integer.parseInt(acct.getsbinormalbalancetype()) == SMTableglaccounts.NORMAL_BALANCE_TYPE_DEBIT){
				if (line.getsdebitamt().compareToIgnoreCase("0.00") != 0){
					sTransactionAmt = line.getsdebitamt().replaceAll(",", "");
				}
				if (line.getscreditamt().compareToIgnoreCase("0.00") != 0){
					sTransactionAmt = "-" + line.getscreditamt().replaceAll(",", "");
				}
			// But if the account is normally a credit balance:
			}else{
				if (line.getsdebitamt().compareToIgnoreCase("0.00") != 0){
					sTransactionAmt = "-" +line.getsdebitamt().replaceAll(",", "");
				}
				if (line.getscreditamt().compareToIgnoreCase("0.00") != 0){
					sTransactionAmt = line.getscreditamt().replaceAll(",", "");
				}
			}
	    	String SQL = "INSERT INTO"
        		+ " " + SMTablegltransactionlines.TableName
        		+ " ("
        		+ " " + SMTablegltransactionlines.bdamount
        		+ ", " + SMTablegltransactionlines.datpostingdate
        		+ ", " + SMTablegltransactionlines.dattransactiondate
        		+ ", " + SMTablegltransactionlines.iconsolidatedposting
        		+ ", " + SMTablegltransactionlines.ifiscalperiod
        		+ ", " + SMTablegltransactionlines.ifiscalyear
        		+ ", " + SMTablegltransactionlines.loriginalbatchnumber
        		+ ", " + SMTablegltransactionlines.loriginalentrynumber
        		+ ", " + SMTablegltransactionlines.loriginallinenumber
        		+ ", " + SMTablegltransactionlines.lsourceledgertransactionlineid
        		+ ", " + SMTablegltransactionlines.sacctid
        		+ ", " + SMTablegltransactionlines.sdescription
        		+ ", " + SMTablegltransactionlines.sreference
        		+ ", " + SMTablegltransactionlines.ssourceledger
        		+ ", " + SMTablegltransactionlines.ssourcetype
        		+ ", " + SMTablegltransactionlines.stransactiontype
        		
        		+ ") VALUES ("
        		+ " " + sTransactionAmt				//bdamount
        		+ " '" + getsposteddateInSQLFormat() + "'" //datpostingdate
        		+ ", '" + entry.getsdatdocdateInSQLFormat() + "'"  //dattransactiondate
        		+ ", 0" //iconsolidatedposting
        		+ ", " + entry.getsfiscalperiod() //ifiscalperiod
        		+ ", " + entry.getsfiscalyear() //ifiscalyear
           		+ ", " + line.getsbatchnumber() //loriginalbatchnumber
        		+ ", " + line.getsentrynumber() //loriginalentrynumber
        		+ ", " + line.getslinenumber() //loriginallinenumber
        		+ ", " + entry.getssourceledgertransactionlineid() //lsourceledgertransactionlineid
        		+ ", '" + line.getsacctid() + "'" //sacctid
        		+ ", '" + line.getsdescription() + "'" //sdescription
        		+ ", '" + line.getsreference() + "'" //sreference
        		+ ", '" + line.getssourceledger() + "'" //ssourceledger
        		+ ", '" + line.getssourcetype() + "'" //ssourcetype
        		+ ", 0" //stransactiontype
        		+ ")"
        	;

        	try {
    			Statement stmt = conn.createStatement();
    			stmt.execute(SQL);
    		} catch (Exception e) {
    			throw new Exception("Error [1555349622] inserting transaction for entry number " + entry.getsentrynumber() + " - " + e.getMessage());
    		}
    	}
       	return;
    }

    private void checkBatchEntries(SMLogEntry log, String sUserID, Connection conn) throws Exception{
    	if (bDebugMode){
    		log.writeEntry(
   				sUserID, 
   				SMLogEntry.LOG_OPERATION_GLBATCHPOST, 
   				"Entering checkBatchEntries", "Batch #:" + getsbatchnumber(), "[1555349623]");
    	}
    	String sCheckResults = "";
		for (int i = 0; i < m_arrBatchEntries.size(); i++){
			//Check each entry
			GLTransactionBatchEntry entry = m_arrBatchEntries.get(i);
			try {
				checkIndividualEntry(
					entry, 
					conn,
					sUserID);
			} catch (Exception e) {
				//Record any check errors we run across:
				sCheckResults += "  Error [1555349624] on entry " + entry.getsentrynumber() + e.getMessage() + ".";
			}
		}
    	if (sCheckResults.compareToIgnoreCase("") != 0){
    		throw new Exception(sCheckResults);
    	}
    	return;
    }
    private void checkIndividualEntry(
    		GLTransactionBatchEntry entry,
    	Connection conn,
    	String sUserID
    	) throws Exception{
       	//Check every entry to make sure they are all in balance - if any aren't, add the error message

    	//Check that each entry is balanced:
    	try {
			entry.entryIsInBalance(conn);
		} catch (Exception e) {
			throw new Exception("Error [1555349625] - " + e.getMessage());
		}
    	
		//Check that the transaction date is within the allowed posting period:
		SMOption opt = new SMOption();
		if (!opt.load(conn)){
			throw new Exception("Error [1555349626] - could not check posting period - " + opt.getErrorMessage() + ".");
		}
		try {
			opt.checkDateForPosting(
				entry.getsdatentrydate(), 
				"ENTRY DATE", 
				conn, 
				sUserID
			);
		} catch (Exception e2) {
			throw new Exception("Error [1555349627] on Entry " + entry.getsentrynumber()
				+ " - " + e2.getMessage() + "."); 
		}
    	
    	return;
    }
    private void setPostingFlag(Connection conn, String sUserID) throws Exception{
    	//First check to make sure no one else is posting:
    	try{
    		String SQL = "SELECT * FROM " + SMTablegloptions.TableName;
    		ResultSet rsGLOptions = clsDatabaseFunctions.openResultSet(SQL, conn);
    		if (!rsGLOptions.next()){
        		throw new Exception("Error [1555349628] - could not get GL Options record.");
    		}else{
    			if(rsGLOptions.getLong(SMTablegloptions.ibatchpostinginprocess) == 1){
    				throw new Exception("Error [1555349629] - A previous GL posting is not completed - "
    					+ SMUtilities.getFullNamebyUserID(rsGLOptions.getString(SMTablegloptions.luserid), conn) + " has been "
    					+ rsGLOptions.getString(SMTablegloptions.sprocess) + " "
    					+ "since " + rsGLOptions.getString(SMTablegloptions.datstartdate) + "."
    				);
    			}
    		}
    		rsGLOptions.close();
    	}catch (SQLException e){
    		throw new Exception("Error [1555349630] checking for previous posting - " + e.getMessage());
    	}
    	//If not, then set the posting flag:
    	try{
    		String SQL = "UPDATE " + SMTablegloptions.TableName 
    			+ " SET " + SMTablegloptions.ibatchpostinginprocess + " = 1"
    			+ ", " + SMTablegloptions.datstartdate + " = NOW()"
    			+ ", " + SMTablegloptions.sprocess 
    				+ " = 'POSTING GL TRANSACTION BATCH NUMBER " + getsbatchnumber() + "'"
       			+ ", " + SMTablegloptions.luserid 
    				+ " = " + sUserID
    		;
    		if (!clsDatabaseFunctions.executeSQL(SQL, conn)){
    			throw new Exception("Error [1555349631] setting posting flag in GL Options");
        		
    		}
    	}catch (SQLException e){
    		throw new Exception("Error [1555349632] setting posting flag in GL Options - " + e.getMessage());
    		
    	}
    }
    private void unsetPostingFlag(Connection conn) throws Exception{
    	try{
    		String SQL = "UPDATE " + SMTablegloptions.TableName 
    		+ " SET " + SMTablegloptions.ibatchpostinginprocess + " = 0"
    		+ ", " + SMTablegloptions.datstartdate + " = '0000-00-00 00:00:00'"
    		+ ", " + SMTablegloptions.sprocess + " = ''"
    			+ ", " + SMTablegloptions.luserid + " = 0"
    		;
    		if (!clsDatabaseFunctions.executeSQL(SQL, conn)){
    			throw new Exception("Error [1555349633] clearing posting flag in GL Options");
    		}
    	}catch (SQLException e){
    		throw new Exception("Error [1555349634] clearing posting flag in GL Options - " + e.getMessage());
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
	public BigDecimal getTotalDebits() throws Exception{
		BigDecimal bdTotalDebits = new BigDecimal("0.00");
		for (int i = 0; i < m_arrBatchEntries.size(); i++){
			bdTotalDebits = bdTotalDebits.add(m_arrBatchEntries.get(i).getDebitTotal());
		}
		return bdTotalDebits;
	}
	public BigDecimal getTotalCredits() throws Exception{
		BigDecimal bdTotalCredits = new BigDecimal("0.00");
		for (int i = 0; i < m_arrBatchEntries.size(); i++){
			bdTotalCredits = bdTotalCredits.add(m_arrBatchEntries.get(i).getCreditTotal());
		}
		return bdTotalCredits;
	}
	public ArrayList<GLTransactionBatchEntry> getBatchEntryArray(){
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

	public String dumpData(){
		String s = "";
		
		s += "Batch date: '" + getsbatchdate() + "'\n"
			+ "Desc: '" + getsbatchdescription() + "'\n"
			+ "Last entry: '" + getsbatchlastentry() + "'\n"
			+ "Batch number: '" + getsbatchnumber() + "'\n"
			+ "Status: '" + getsbatchstatus() + "'\n"
			+ "Created by: '" + getlcreatedby() + "'\n"
			+ "Last edit date: '" + getslasteditdate() + "'\n"
			+ "Edited by: '" + getllasteditedby() + "'\n"
			+ "Posted: '" + getsposteddate() + "'\n"
		;
		for (int i = 0; i < m_arrBatchEntries.size(); i++){
			s += "Entry " + (i + 1) + ": " + m_arrBatchEntries.get(i).dumpData();
		}
		return s;
	}
	private void initializeVariables(){
		m_sbatchnumber = "-1";
		m_sbatchdate = clsDateAndTimeConversions.now(SMUtilities.DATE_FORMAT_FOR_DISPLAY);
		m_sbatchstatus = Integer.toString(SMBatchStatuses.ENTERED);
		m_sbatchdescription = "INITIALIZED BATCH";
		m_slasteditdate = SMUtilities.EMPTY_DATETIME_VALUE;
		m_sbatchlastentry = "0";
		m_lcreatedby = "0";
		m_llasteditedby = "0";
		m_sdatpostdate = SMUtilities.EMPTY_DATETIME_VALUE;
		m_arrBatchEntries = new ArrayList<GLTransactionBatchEntry>(0);
	}
}
