package smic;

import java.io.PrintWriter;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

import javax.servlet.ServletContext;

import SMClasses.SMBatchStatuses;
import SMClasses.SMEntryBatch;
import SMClasses.SMLogEntry;
import SMClasses.SMModuleTypes;
import SMClasses.SMOption;
import SMDataDefinition.SMTableglexportdetails;
import SMDataDefinition.SMTableicbatchentries;
import SMDataDefinition.SMTableiccosts;
import SMDataDefinition.SMTableicentrylines;
import SMDataDefinition.SMTableicitemlocations;
import SMDataDefinition.SMTableicitems;
import SMDataDefinition.SMTableicitemstatistics;
import SMDataDefinition.SMTableicoptions;
import SMDataDefinition.SMTableictransactions;
import SMDataDefinition.SMTableinvoicedetails;
import SMDataDefinition.SMTableinvoiceheaders;
import SMDataDefinition.SMTablelocations;
import SMDataDefinition.SMTablesmoptions;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsDateAndTimeConversions;
import ServletUtilities.clsManageBigDecimals;
import ServletUtilities.clsServletUtilities;
import ServletUtilities.clsStringFunctions;
import smgl.GLTransactionBatch;
import smgl.SMGLExport;

public class ICEntryBatch {

	//Table Name
	public static final String TableName = "icbatches";
	
	private final String EMPTY_DATE_TIME_STRING = "00/00/0000 00:00 AM";
	private final String TEMP_COST_DELETE_TABLE_NAME = "TEMPCOSTDELETE";
	
	//Field names:
	public static final String lbatchnumber = "lbatchnumber";
	public static final String datbatchdate = "datbatchdate";
	public static final String ibatchstatus = "ibatchstatus";
	public static final String sbatchdescription = "sbatchdescription";
	public static final String smoduletype = "smoduletype";
	public static final String ibatchtype = "ibatchtype";
	public static final String datlasteditdate = "datlasteditdate";
	public static final String lbatchlastentry = "lbatchlastentry";
	public static final String lcreatedbyid = "lcreatedbyid";
	public static final String screatedbyfullname = "screatedbyfullname";
	public static final String llasteditedbyid = "llasteditedbyid";
	public static final String slasteditedbyfullname = "slasteditedbyfullname"; 
	public static final String datpostdate = "datpostdate";
	
	//Field Lengths:
	public static final int sBatchDescriptionLength = 128;
	public static final int sCreatedByLength = 128;
	public static final int sLastEditUserLength = 128; 
	public static final int screatedbyfullnameLength = 128;
	public static final int slasteditedbyfullnameLength = 128;
	
	private long m_lbatchnumber;
	private String m_sbatchdate;
	private int m_ibatchstatus;
	private String m_sbatchdescription;
	private int m_ibatchtype;
	private String m_slasteditdate;
	private int m_ibatchlastentry;
	private String m_lcreatedbyid;
	private String m_screatedbyfullname;
	private String m_llasteditedbyid;
	private String m_slasteditedbyfullname;
	private String m_smoduletype;
	private String m_sdatpostdate;
	private String m_sErrorMessages;
	private SMLogEntry log;
	private static final boolean bDebugMode = false;
	//Used in posting to hold transaction details until we write them:
	private ArrayList<ICTransactionDetail>m_arrTransactionDetails;
	
	//We just need one transaction detail record to hold any 'TO LOCATION' transfers, since when we
	//transfer TO a location, we always create a single, new bucket:
	private ICTransactionDetail m_tdToLocation;
	
	//Carries the costing method:
	private long lCostingMethod = 0;
	
	//Carries the 'allow negative qtys' value:
	private boolean bAllowNegativeQtys = true;
	
	//The flag tells the system whether or not to write the batch number back to the SM invoice header,
	//or whether to write cost information back to the invoice lines.  It's set to false while testing
	// the inventory system so we can test without touching REAL SM invoice data:
	private boolean m_iFlagInvoices = false;
	
	//Debugging flag for logging - this causes several debugging messages to appear in the log:
	private boolean bLogDebug = false;
	
    public ICEntryBatch(
    		String sBatchNumber
        ) 
    {
    	sBatchNumber(sBatchNumber);
    	m_sbatchdate = clsDateAndTimeConversions.now("yyyy-MM-dd");
    	m_ibatchstatus = SMBatchStatuses.ENTERED;
    	m_sbatchdescription = "INITIALIZED BATCH";
    	m_ibatchtype = ICBatchTypes.IC_SHIPMENT;
    	m_slasteditdate = "0000-00-00";
    	m_ibatchlastentry = 0;
    	m_lcreatedbyid = "0";
    	m_screatedbyfullname = "";
    	m_llasteditedbyid = "0";
    	m_slasteditedbyfullname = "";
    	m_smoduletype = SMModuleTypes.IC;
    	m_sdatpostdate = EMPTY_DATE_TIME_STRING;
    	m_sErrorMessages = "";
        }
    public boolean load (
    		ServletContext context, 
    		String sDBID
    		){
    
    	return load (sBatchNumber(), context, sDBID);
    }
    public boolean load (
    		Connection conn
    		){
    	return load (sBatchNumber(), conn);
    }
    public boolean load (
		String sBatchNumber,
		ServletContext context, 
		String sDBID
		){
	    if (! sBatchNumber(sBatchNumber)){
	    	addErrorMessage("Invalid sBatchnumber '" + sBatchNumber + "'");
	    	return false;
	    }

		String SQL = "SELECT * " 
			+ " FROM " + ICEntryBatch.TableName
			+ " WHERE (" 
			+ ICEntryBatch.lbatchnumber + " = " + sBatchNumber
			+ ")";
		try {
			ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, context, sDBID);
			rs.next();
    		if (!sBatchDescription(rs.getString(sbatchdescription))){ rs.close(); return false; };
    		if (!iBatchType(rs.getInt(ibatchtype))){ rs.close(); return false; };
    		m_slasteditdate = rs.getString(datlasteditdate);
    		if (!sSetLastEditedByID(rs.getString(llasteditedbyid))){ rs.close(); return false; };
    		if (!sSetLastEditedByFullName(rs.getString(slasteditedbyfullname))){ rs.close(); return false; };
    		m_sbatchdate = rs.getString(datbatchdate);
    		if (!iBatchStatus(rs.getInt(ibatchstatus))){ rs.close(); return false; };
    		if (!sSetCreatedByID(rs.getString(lcreatedbyid))){ rs.close(); return false; };
    		if (!sSetCreatedByFullName(rs.getString(screatedbyfullname))){ rs.close(); return false; };
    		m_ibatchlastentry = rs.getInt(lbatchlastentry);
    		if (!sModuleType(rs.getString(smoduletype))){ rs.close(); return false; };
    		m_sdatpostdate = clsDateAndTimeConversions.resultsetDateTimeStringToString(
					rs.getString(datpostdate));
			rs.close();
		}catch (SQLException ex){
			addErrorMessage("Error loading entry batch - " + ex.getMessage());
	        return false;
		}
    
    	return true;
    }
    public boolean load (
    		String sBatchNumber,
    		Connection conn 
    ){

    	if (! sBatchNumber(sBatchNumber)){
    		System.out.println("Invalid sBatchNumber - " + sBatchNumber);
    		return false;
    	}
		String SQL = "SELECT * " 
			+ " FROM " + ICEntryBatch.TableName
			+ " WHERE (" 
			+ ICEntryBatch.lbatchnumber + " = " + sBatchNumber
			+ ")";
    	try {
    		ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn); 
    		if (rs.next()){
	    		if (!sBatchDescription(rs.getString(sbatchdescription))){ rs.close(); return false; };
	    		if (!iBatchType(rs.getInt(ibatchtype))){ rs.close(); return false; };
	    		m_slasteditdate = rs.getString(datlasteditdate);
	    		if (!sSetLastEditedByID(rs.getString(llasteditedbyid))){ rs.close(); return false; };
	    		if (!sSetLastEditedByFullName(rs.getString(slasteditedbyfullname))){ rs.close(); return false; };
	    		m_sbatchdate = rs.getString(datbatchdate);
	    		if (!iBatchStatus(rs.getInt(ibatchstatus))){ rs.close(); return false; };
	    		if (!sSetCreatedByID(rs.getString(lcreatedbyid))){ rs.close(); return false; };
	    		if (!sSetCreatedByFullName(rs.getString(screatedbyfullname))){ rs.close(); return false; };
	    		m_ibatchlastentry = rs.getInt(lbatchlastentry);
	    		if (!sModuleType(rs.getString(smoduletype))){ rs.close(); return false; };
	    		m_sdatpostdate = clsDateAndTimeConversions.resultsetDateTimeStringToString(
						rs.getString(datpostdate));
    		}else{
    			return false;
    		}
    		rs.close();
    	}catch (SQLException ex){
    		addErrorMessage(" - Error loading batch record - " + ex.getMessage());
    		return false;
    	}
    	return true;
    }

    public boolean save_without_data_transaction (Connection conn, String sUserFullName, String sUserID){
    	
    	String SQL = "";
    	if (m_lbatchnumber == -1){ 
	    	//Add a new batch:
    		SQL = "INSERT into " + ICEntryBatch.TableName
    		+ " (" 
    			+ ICEntryBatch.ibatchtype
    			+ ", " + ICEntryBatch.datbatchdate
    			+ ", " + ICEntryBatch.datlasteditdate
    			+ ", " + ICEntryBatch.ibatchstatus
    			+ ", " + ICEntryBatch.sbatchdescription //5
    			+ ", " + ICEntryBatch.lcreatedbyid
    			+ ", " + ICEntryBatch.screatedbyfullname
    			+ ", " + ICEntryBatch.llasteditedbyid
    			+ ", " + ICEntryBatch.slasteditedbyfullname
    			+ ", " + ICEntryBatch.smoduletype  //10
    			+ ", " + ICEntryBatch.lbatchlastentry
    		+ ")"
    		+ " VALUES ("
    			+ "'" + sBatchType() + "'"
    			+ ", '" + m_sbatchdate + "'"
    			+ ", NOW()"
    			+ ", " + sBatchStatus()
    			+ ", '" + clsDatabaseFunctions.FormatSQLStatement(m_sbatchdescription) + "'" //5
    			+ ", " + sUserID + ""
    			+ ", '" + clsDatabaseFunctions.FormatSQLStatement(sUserFullName) + "'"
    			+ ", " + sUserID + ""
    			+ ", '" + clsDatabaseFunctions.FormatSQLStatement(sUserFullName) + "'"
    			+ ", '" + SMModuleTypes.IC + "'" //10
    			+ ", " + "0"
    		+ ")";
    	}else{
			//First, get the last entry number for this batch:
    		SQL = "SELECT "
    			+ SMTableicbatchentries.lentrynumber
    			+ " FROM " + SMTableicbatchentries.TableName
    			+ " WHERE (" 
    			+ "(" + SMTableicbatchentries.lbatchnumber + " = " + sBatchNumber() + ")"
    			+ ")"
    			+ " ORDER BY " + SMTableicbatchentries.lentrynumber + " DESC LIMIT 1";
	    	try{
		    	ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
		    	
		    	if (rs.next()){
		    		m_ibatchlastentry = rs.getInt(SMTableicbatchentries.lentrynumber);
		    	}
		    	else{
		    		m_ibatchlastentry = 0;
		    	}		
		    	rs.close();
	        }catch (SQLException ex){
	        	addErrorMessage("Error getting last entry number in ICEntryBatch - " + ex.getMessage());
	    	    return false;
	        }
			SQL = "UPDATE "
				+ ICEntryBatch.TableName
				+ " SET "
				+ ICEntryBatch.datbatchdate + " = '" + m_sbatchdate + "', "
				+ ICEntryBatch.ibatchstatus + " = " + sBatchStatus()
				+ ", " + ICEntryBatch.sbatchdescription + " = '" 
					+ clsDatabaseFunctions.FormatSQLStatement(m_sbatchdescription) + "'"
				+ ", " + ICEntryBatch.ibatchtype + " = " + sBatchType()
				+ ", " + ICEntryBatch.datlasteditdate + " = NOW()"
				+ ", " + ICEntryBatch.lbatchlastentry + " = " + sGetBatchLastEntry()
				+ ", " + ICEntryBatch.lcreatedbyid + " = " + m_lcreatedbyid + ""
				+ ", " + ICEntryBatch.screatedbyfullname + " = '" + clsDatabaseFunctions.FormatSQLStatement(m_screatedbyfullname) + "'"
				+ ", " + ICEntryBatch.llasteditedbyid + " = '" + m_llasteditedbyid + "'"
				+ ", " + ICEntryBatch.slasteditedbyfullname + " = '" + clsDatabaseFunctions.FormatSQLStatement(m_slasteditedbyfullname) + "'"
				+ ", " + ICEntryBatch.smoduletype + " = '" + m_smoduletype + "'"
				;
				try {
					if(getPostingDateAsSQLString().compareToIgnoreCase("0000-00-01 00:00:00") > 0){
						SQL = SQL + ", " + SMEntryBatch.datpostdate + " = '" + getPostingDateAsSQLString() + "'";
					}
				} catch (Exception e) {
					this.addErrorMessage("Error updating entry batch with SQL: " + SQL + " - " + e.getMessage());
					return false;
				}
				SQL += " WHERE ("
					+ "(" + ICEntryBatch.lbatchnumber + " = " + sBatchNumber() + ")"
				+ ")";
    	}	
	        
    	try{
	    	if (clsDatabaseFunctions.executeSQL(SQL, conn) == false){
	    		addErrorMessage("Could not complete IC update transaction - " + "batch" + " was not updated - SQL = " + SQL + ".<BR>");
	    	}else{
	    		//System.out.println("Successfully updated " + "batch" + ": " + sBatchNumber() + ".");
	    	}
    	}catch(SQLException ex){
    		addErrorMessage("Could not complete IC update transaction - " + "batch" 
    	+ " was not updated - SQL = " + SQL + " - " + ex.getMessage() + ".<BR>");
    	    return false;
    	}
    	
    	//If the batch was newly created, get the new batch number:
    	if (m_lbatchnumber == -1){
    		SQL = "SELECT * " 
    			+ " FROM " + ICEntryBatch.TableName
    			+ " ORDER BY " 
    			+ ICEntryBatch.lbatchnumber + " DESC "
    			+ " LIMIT 1";
	    	try{
		    	ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
		    	
		    	if (rs.next()){
		    		m_lbatchnumber = rs.getInt(lbatchnumber);
		    		rs.close();
		    	}
		    	else{
		    		rs.close();
		    		m_lbatchnumber = 0;
		    	}		
	        }catch (SQLException ex){
	        	addErrorMessage("Error getting the new batch number - " + ex.getMessage());
	    	    return false;
	        }
    	}
    	return true;
    }
    public boolean save_with_data_transaction (ServletContext context, String sDBID, String sUserFullName, String sUserID){
    	
    	Connection conn = clsDatabaseFunctions.getConnection(context, sDBID, "MySQL", "smic.ICEntryBatch");
    	
    	if(!clsDatabaseFunctions.start_data_transaction(conn)){
    		clsDatabaseFunctions.freeConnection(context, conn, "[1547080858]");
    		return false;
    	}
    	
    	if (!save_without_data_transaction(conn, sUserFullName, sUserID)){
    		clsDatabaseFunctions.rollback_data_transaction(conn);
    		return false;
    	}else{
    		clsDatabaseFunctions.commit_data_transaction(conn);
    		return true;
    	}
    }
    public boolean flag_as_deleted (
    		String sBatchNumber,
    		ServletContext context, 
    		String sDBID
    		){
        
    	    if (! sBatchNumber(sBatchNumber)){
    	    	System.out.println("Invalid sBatchNumber - " + sBatchNumber);
    	    	return false;
    	    }
    	
    	    String SQL = "UPDATE "
				+ ICEntryBatch.TableName
				+ " SET "
				+ ICEntryBatch.ibatchstatus + " = " + SMBatchStatuses.DELETED
				+ " WHERE ("
					+ "(" + ICEntryBatch.lbatchnumber + " = " + sBatchNumber + ")"
				+ ")";
    		try{
    	    	if (clsDatabaseFunctions.executeSQL(SQL, context, sDBID) == false){
    	    		return false;
    	    	}
    	    }catch (SQLException ex){
    	    	addErrorMessage("Error in flag_as_deleted - " + ex.getMessage());
    		    return false;
    		}
        
        	return true;
        }
    public boolean flag_as_deleted (
    		ServletContext context, 
    		String sDBID
    		){
        
    		return flag_as_deleted (sBatchNumber(), context, sDBID);
        }
    public boolean delete_entry_with_transaction (
    		String sBatchNumber,
    		String sEntryNumber,
    		ServletContext context, 
    		String sDBID
    		){
    	
    	//Set the batch number for our batch:
    	if (!sBatchNumber(sBatchNumber)){
    		addErrorMessage("Invalid batch number in delete_entry: " + sBatchNumber);
    		return false;
    	}

    	Connection conn = clsDatabaseFunctions.getConnection(context, sDBID, "MySQL", "smic.ICEntryBatch");
    	
    	//We do all this in a transaction, so we can roll it back:
    	if (!clsDatabaseFunctions.start_data_transaction(conn)){
    		clsDatabaseFunctions.freeConnection(context, conn, "[1547080850]");
    		return false;
    	}
    	
    	if (!delete_entry(sBatchNumber, sEntryNumber, conn)){
    		clsDatabaseFunctions.rollback_data_transaction(conn);
    		clsDatabaseFunctions.freeConnection(context, conn, "[1547080852]");
    	}else{
    		clsDatabaseFunctions.commit_data_transaction(conn);
    		clsDatabaseFunctions.freeConnection(context, conn, "[1547080853]");
    	}
    	
    	return true;
    }
    public boolean delete_entry(
    		String sBatchNumber, 
    		String sEntryNumber, 
    		Connection conn){
    	
    	//First, we remove the entry and its lines
    	if (! delete_single_entry (sEntryNumber, conn)){
    		return false;
    	}
    	
    	if (! delete_lines_for_entry (sEntryNumber, conn)){
    		return false;
    	}
    	
    	//Then we renumber the entries to keep them all consecutive
    	//This also updates the 'lastentrynumber' in this batch:
    	if (! renumber_all_entries (conn)){
    		return false;
    	}
    	
    	//Next, we update the entrynumber field in the related lines:
    	if (! renumber_all_lines (sBatchNumber, conn)){
    		return false;
    	}
    	
    	//Finally, we update the 'last entry number' in the batch:
    	//Update the batch with the 'lastlentry number' here:
    	if (! update_last_entry_number(conn)){
    		return false;
    	}
    	
    	return true;
    }
    private boolean delete_single_entry (
    		String sEntryNumber,
    		Connection conn
    		){
	
		String SQL = "DELETE " 
			+ " FROM " + SMTableicbatchentries.TableName
			+ " WHERE (" 
			+ "(" + SMTableicbatchentries.lbatchnumber + " = " + sBatchNumber() + ")"
			+ " AND (" + SMTableicbatchentries.lentrynumber + " = " + sEntryNumber + ")"
			+ ")";
		try{
	    	if (clsDatabaseFunctions.executeSQL(SQL, conn) == false){
	    		addErrorMessage("Error deleting single entry with SQL: " + SQL);
	    		return false;
	    	}
	    }catch (SQLException ex){
	    	addErrorMessage("Error deleting single entry with SQL: " + SQL + " - " + ex.getMessage());
		    return false;
		}
    	return true;
    }
    private boolean delete_lines_for_entry (
    		String sEntryNumber,
    		Connection conn
    		){
		String SQL = "DELETE " 
			+ "FROM " + SMTableicentrylines.TableName
			+ " WHERE (" 
			+ "(" + SMTableicentrylines.lbatchnumber + " = " + sBatchNumber() + ")"
			+ " AND (" + SMTableicentrylines.lentrynumber + " = " + sEntryNumber + ")"
			+ ")";
		try{
	    	if (clsDatabaseFunctions.executeSQL(SQL, conn) == false){
	    		return false;
	    	}
	    }catch (SQLException ex){
	    	addErrorMessage("Error deleting lines for entry with SQL: " + SQL + " - " + ex.getMessage());
		    return false;
		}
    	return true;
    }
    private boolean renumber_all_entries (
    		Connection conn
    		){
		String SQL = "SELECT "
			+ SMTableicbatchentries.lid
			+ ", " + SMTableicbatchentries.lentrynumber
			+ " FROM " + SMTableicbatchentries.TableName
			+ " WHERE (" 
			+ SMTableicbatchentries.lbatchnumber + " = " + sBatchNumber()
			+ ")"
			+ " ORDER BY " + SMTableicbatchentries.lentrynumber + " ASC";
    	try{
	    	ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
	    	
	    	int iNewEntryNumber = 0;
	    	while (rs.next()){
	    		iNewEntryNumber ++;
	    		if(!renumber_entry(
	    				rs.getLong(SMTableicbatchentries.lid), 
	    				iNewEntryNumber,
	    				conn
	    				)
	    		){
	    			rs.close();
	    			return false;
	    		}
	    	}
	    	rs.close();
        }catch (SQLException ex){
        	addErrorMessage("Error error in renumber_all_entries with SQL: " + SQL + " - " + ex.getMessage());
    	    return false;
        }
    	return true;
    }
    private boolean renumber_entry (
    		long lEntryID,
    		int iNewEntryNumber,
    		Connection conn
    		){
	
		String SQL = "UPDATE " 
			+ SMTableicbatchentries.TableName
			+ " SET " + SMTableicbatchentries.lentrynumber
			+ " = " + Integer.toString(iNewEntryNumber)
			+ " WHERE (" 
			+ "(" + SMTableicbatchentries.lid + " = " + Long.toString(lEntryID) + ")"
			+ ")";
	    
		try{
	    	if (clsDatabaseFunctions.executeSQL(SQL, conn) == false){
	    		return false;
	    	}
	    }catch (SQLException ex){
	    	addErrorMessage("Error in renumber_entry with SQL: " + SQL + " - " + ex.getMessage());
	    	return false;
		}
    	return true;
    }
    public boolean update_last_entry_number (
    		Connection conn
    		){
	
    	//Get the last entry number:
    	String sLastEntryNumber = "0";
		String SQL = "SELECT "
			+ SMTableicbatchentries.lentrynumber
			+ " FROM " + SMTableicbatchentries.TableName
			+ " WHERE (" 
			+ "(" + SMTableicbatchentries.lbatchnumber + " = " + sBatchNumber() + ")"
			+ ")"
			+ " ORDER BY " + SMTableicbatchentries.lentrynumber + " DESC LIMIT 1";
    	try{
	    	ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
	    	if (rs.next()){
	    		sLastEntryNumber = Integer.toString(rs.getInt(1));
	    	}
	    	rs.close();
        }catch (SQLException ex){
        	addErrorMessage("Error in update_last_entry with select SQL: " + SQL + " - " + ex.getMessage());
    	    return false;
        }
    	
		SQL = "UPDATE "
			+ ICEntryBatch.TableName
			+ " SET "
			+ ICEntryBatch.lbatchlastentry + " = " + sLastEntryNumber
			+ " WHERE ("
				+ "(" + ICEntryBatch.lbatchnumber + " = " + sBatchNumber() + ")"
			+ ")";
		try{
	    	if (clsDatabaseFunctions.executeSQL(SQL, conn) == false){
	    		return false;
	    	}
	    }catch (SQLException ex){
	    	addErrorMessage("Error in update_last_entry with update SQL: " + SQL + " - " + ex.getMessage());
	    	return false;
		}
    	return true;
    }
    private boolean renumber_all_lines (
    		String sBatchNumber,
    		Connection conn
    		){
		String SQL = "UPDATE " 
			+ SMTableicentrylines.TableName + ", " + SMTableicbatchentries.TableName
			+ " SET " + SMTableicentrylines.TableName + "." + SMTableicentrylines.lentrynumber
			+ " = " + SMTableicbatchentries.TableName + "." + SMTableicbatchentries.lentrynumber
			+ " WHERE ("
				+ "(" 
					+ SMTableicentrylines.TableName + "." + SMTableicentrylines.lentryid
					+ " = "
					+ SMTableicbatchentries.TableName + "." + SMTableicbatchentries.lid 
				+ ")"
				+ " AND (" 
					+ SMTableicentrylines.TableName + "." + SMTableicentrylines.lbatchnumber
					+ " = "
					+ sBatchNumber
				+ ")"
			+ ")";
		try{
	    	if (clsDatabaseFunctions.executeSQL(SQL, conn) == false){
	    		return false;
	    	}
	    }catch (SQLException ex){
	    	addErrorMessage("Error in renumber_all_lines SQL: " + SQL + " - " + ex.getMessage());
		    return false;
		}
    	return true;
    }
    public void lBatchNumber (long lBatchNumber){
		m_lbatchnumber = lBatchNumber;
	}
	public long lBatchNumber (){
		return m_lbatchnumber;
	}
	public boolean sBatchNumber (String sBatchNumber){
		try{
			m_lbatchnumber = Integer.parseInt(sBatchNumber);
			return true;
		}catch (NumberFormatException e){
			addErrorMessage("Error formatting batch number from string: " + sBatchNumber + ".");
			return false;
		}
	}
	public String sBatchNumber (){
		return Long.toString(m_lbatchnumber);
	}
    public boolean setBatchDate (String sBatchDate){
		if (!clsDateAndTimeConversions.IsValidDateString("yyyy-MM-dd", sBatchDate)){
    		return false;
    	}else{
    		m_sbatchdate = sBatchDate;
    		return true;
    	}
    }
    public String getBatchDate (){
    	return m_sbatchdate;
    }
    public String getBatchDateInStdFormat (){
    	java.sql.Date datBatchDate = null;
    	//TJR - 2/13/2015 - this one is really messy - several ways to handle it, this maybe isn't the simplest:
    	//It needs to have a string returned - we can't throw an exception here, or return a 'false', without re-writing a lot of 
    	//functions that already use this method.
		try {
			datBatchDate = clsDateAndTimeConversions.StringTojavaSQLDate(m_sbatchdate, "yyyy-MM-dd");
		} catch (ParseException e) {
			//So if it fails, we'll return the current date:
			return clsDateAndTimeConversions.utilDateToString(clsDateAndTimeConversions.nowAsSQLDate(), "MM/dd/yyyy");
		}
    	return clsDateAndTimeConversions.utilDateToString(datBatchDate, "MM/dd/yyyy");
    }

    public void setPostingDate (String sPostingDate){
    	m_sdatpostdate = sPostingDate;
    }

    public String getPostingDateString (){
    	return m_sdatpostdate;
    }
    private String getPostingDateAsSQLString() throws Exception{
    	java.util.Date date;
		try {
			date = new SimpleDateFormat("MM/dd/yyyy hh:mm a").parse(m_sdatpostdate);
		} catch (ParseException e) {
			throw new Exception("Could not parse posting date '" + m_sdatpostdate + "' as date.");
		}
    	return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(date); // 9:00
    }
    public boolean iBatchStatus (int iBatchStatus){
		switch (iBatchStatus){
		//There are only four possible batch statuses - anything else is invalid.
		case 0: m_ibatchstatus = iBatchStatus; return true;
		case 1: m_ibatchstatus = iBatchStatus; return true;
		case 2: m_ibatchstatus = iBatchStatus; return true;
		case 3: m_ibatchstatus = iBatchStatus; return true;
		default: return false;
		}
    }
    public boolean sBatchStatus (String sBatchStatus){
    	
    	int i;
    	try{
    		i = Integer.parseInt(sBatchStatus);
    		switch (i){
    			//There are only four possible batch statuses - anything else is invalid.
    			case 0: m_ibatchstatus = i; return true;
    			case 1: m_ibatchstatus = i; return true;
    			case 2: m_ibatchstatus = i; return true;
    			case 3: m_ibatchstatus = i; return true;
    			default: return false;
    		}
    	}catch (NumberFormatException e){
    		addErrorMessage("Error formatting batch status from string: " + sBatchStatus + ".");
    		return false;
    	}
    }
    public int iBatchStatus (){
    	return m_ibatchstatus;
    }
    public String sBatchStatus (){
    	return Integer.toString(m_ibatchstatus);
    }
    public String sBatchStatusLabel (){
    	return SMBatchStatuses.Get_Transaction_Status(m_ibatchstatus);
    }
    public boolean sBatchDescription (String sBatchDescription){
    	m_sbatchdescription = sBatchDescription;
    	return true;
    }
    public String sBatchDescription (){
    	return m_sbatchdescription;
    }
    public boolean iBatchType (int iBatchType){
		switch (iBatchType){
		//There are only four possible batch types - anything else is invalid.
		case 0: m_ibatchtype = iBatchType; return true;
		case 1: m_ibatchtype = iBatchType; return true;
		case 2: m_ibatchtype = iBatchType; return true;
		case 3: m_ibatchtype = iBatchType; return true;
		case 4: m_ibatchtype = iBatchType; return true;
		default: return false;
		}
    }
    public boolean sBatchType (String sBatchType){
    	
    	int i;
    	try{
    		i = Integer.parseInt(sBatchType);
    		switch (i){
    			//There are only five possible batch types - anything else is invalid.
	    		case 0: m_ibatchtype = i; return true;
	    		case 1: m_ibatchtype = i; return true;
	    		case 2: m_ibatchtype = i; return true;
	    		case 3: m_ibatchtype = i; return true;
	    		case 4: m_ibatchtype = i; return true;
    			default: return false;
    		}
    	}catch (NumberFormatException e){
    		addErrorMessage("Error formatting batch type from string: " + sBatchType + ".");
    		return false;
    	}
    }
    public int iBatchType (){
    	return m_ibatchtype;
    }
    public String sBatchType (){
    	return Integer.toString(m_ibatchtype);
    }
    public String sBatchTypeLabel(){
    	return ICBatchTypes.Get_Batch_Type(m_ibatchtype);
    }
    public boolean sLastEditDate (String sLastEditDate){
		if (!clsDateAndTimeConversions.IsValidDateString("yyyy-MM-dd", sLastEditDate)){
    		return false;
    	}else{
    		m_slasteditdate = sLastEditDate;
    		return true;
    	}
    }
    public String sGetLastEditDate (){
    	return m_slasteditdate;
    }
    public int iGetBatchLastEntry(){
    	return m_ibatchlastentry;
    }
    public String sGetBatchLastEntry(){
    	return Integer.toString(m_ibatchlastentry);
    }
    public boolean sSetCreatedByFullName(String sCreatedByFullName){
    	m_screatedbyfullname = sCreatedByFullName;
    	return true;
    }
    public String sGetCreatedByFullName (){
    	return m_screatedbyfullname;
    }
    public boolean sSetCreatedByID(String sCreatedByID){
    	m_lcreatedbyid = sCreatedByID;
    	return true;
    }
    public String sGetCreatedByID (){
    	return m_lcreatedbyid;
    }
    public boolean sSetLastEditedByFullName(String sLastEditedByFullName){
    	m_slasteditedbyfullname = sLastEditedByFullName;
    	return true;
    }
    public String sGetLastEditedByFullName (){
    	return m_slasteditedbyfullname;
    }
    public boolean sSetLastEditedByID(String sLastEditedByID){
    	m_llasteditedbyid = sLastEditedByID;
    	return true;
    }
    public String sLastEditedByID (){
    	return m_llasteditedbyid;
    }
    public boolean sModuleType (String sModuleType){
    	if (sModuleType.compareToIgnoreCase(SMModuleTypes.AP) ==0){
    		m_smoduletype = SMModuleTypes.AP;
    		return true;
    	}
    	if (sModuleType.compareToIgnoreCase(SMModuleTypes.AR) ==0){
    		m_smoduletype = SMModuleTypes.AR;
    		return true;
    	}

    	if (sModuleType.compareToIgnoreCase(SMModuleTypes.GL) ==0){
    		m_smoduletype = SMModuleTypes.GL;
    		return true;
    	}
    	
    	if (sModuleType.compareToIgnoreCase(SMModuleTypes.IC) ==0){
    		m_smoduletype = SMModuleTypes.IC;
    		return true;
    	}
    	
    	if (sModuleType.compareToIgnoreCase(SMModuleTypes.PO) ==0){
    		m_smoduletype = SMModuleTypes.PO;
    		return true;
    	}
    	
    	//If it's not one of these, return false:
    	return false;
    }
    public String sModuleType (){
    	return m_smoduletype;
    }
    public boolean bEditable(){
        //Indicates whether it's an 'editable' batch, meaning that 
        //it's not posted or deleted and can therefore be edited:
        if ((m_ibatchstatus != SMBatchStatuses.DELETED) && (m_ibatchstatus != SMBatchStatuses.POSTED)){
            return true;
        }
        else{
           return false;
        }   	
    }
    public String sTotalAmount(ServletContext context, String sDBID){
    	
    	String SQL = "SELECT "
    		+ "SUM(" + SMTableicbatchentries.bdentryamount + ") AS batchtotal "
    		+ " FROM " + SMTableicbatchentries.TableName
    		+ " WHERE ("
    			+ SMTableicbatchentries.lbatchnumber + " = " + Long.toString(m_lbatchnumber)
    		+ ")"
    		;
    	try{
    		ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, context, sDBID);
    		if(rs.next()){
    			BigDecimal bdTotal = rs.getBigDecimal("batchtotal").setScale(2, BigDecimal.ROUND_HALF_UP);
    			rs.close();
    			if(bdTotal == null){
    				return "0.00";
    			}else{
    				return clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(bdTotal);
    			}
    		}else{
    			rs.close();
    			return "0.00";
    		}
    	}catch(SQLException e){
    		addErrorMessage("In " + this.toString() + ": Error getting batch total amount " 
        			+ e.getMessage());
    		return "0.00";
    	}
    }
    public void clearErrorMessages(){
    	m_sErrorMessages = "";
    }
    public String getErrorMessages(){
    	return m_sErrorMessages;
    }
    public void addErrorMessage(String sErrMsg){
    	m_sErrorMessages += sErrMsg + "<BR>";
    }
    public boolean post_with_data_transaction (
    		ServletContext context,
    		String sDBID,
    		String sUserID,
    		String sUserFullName,
    		PrintWriter out
    		){
		
    	if (this.iBatchStatus() == SMBatchStatuses.POSTED){
    		addErrorMessage("This batch is already posted");
    		return false;
    	}
    	if (this.iBatchStatus() == SMBatchStatuses.DELETED){
    		addErrorMessage("This batch is deleted - it cannot be posted");
    		return false;
    	}
    	
    	log = new SMLogEntry(sDBID, context);
    	m_iFlagInvoices = false;
    	clearErrorMessages();
    	
    	//First check to make sure no one else is posting:
    	ICOption option = new ICOption();
    	try{
    		String sPostingProcess = "POSTING IC BATCH";
    		option.checkAndUpdatePostingFlagWithoutConnection(
    			context, 
    			sDBID, 
    			clsServletUtilities.getFullClassName(this.toString()) + ".post_with_data_transaction", 
    			sUserFullName, 
    			sPostingProcess);
    		
    	}catch (Exception e){
			addErrorMessage("Error [1529956984] checking for previous posting - " + e.getMessage());
    		return false;
    	}
    	
    	Connection conn;
		try {
			conn = clsDatabaseFunctions.getConnectionWithException(
					context, 
					sDBID, 
					"MySQL",
					this.toString() + ".post_with_data_transaction - User: " 
					+ sUserID
					+ " - "
					+ sUserFullName
				);
		} catch (Exception e2) {
			addErrorMessage("Error [1550676771] getting connection to post batch - " + e2.getMessage());
    		//Clear the posting flag:
    		try {
				option.resetPostingFlagWithoutConnection(context, sDBID);
			} catch (Exception e) {
				//We won't stop for this, but the next user will have to clear the IC posting flag
			}
			return false;
		}    	
    	
    	if(!clsDatabaseFunctions.start_data_transaction(conn)){
    		//Clear the posting flag:
    		try {
				option.resetPostingFlagWithoutConnection(context, sDBID);
			} catch (Exception e) {
				//We won't stop for this, but the next user will have to clear the IC posting flag
			}
    		clsDatabaseFunctions.freeConnection(context, conn, "[1547080855]");
    		//Clear the posting flag, then return
    		return false;
    	}
    	
    	try {
			post_without_data_transaction(conn, sUserFullName, sUserID);
		} catch (Exception e1) {
    		//Clear the posting flag:
    		clsDatabaseFunctions.rollback_data_transaction(conn);
    		try {
    			option.resetPostingFlagWithoutConnection(context, sDBID);
			} catch (Exception e) {
				//We won't stop for this, but the next user will have to clear the IC posting flag
			}
    		clsDatabaseFunctions.freeConnection(context, conn, "[1547080856]");
    		return false;
		}

		clsDatabaseFunctions.commit_data_transaction(conn);
		try {
			option.resetPostingFlagWithoutConnection(context, sDBID);
		} catch (Exception e1) {
			addErrorMessage("Error [1529952579] - " + e1.getMessage());
			clsDatabaseFunctions.freeConnection(context, conn, "[1547080956]");
			return false;
		}
		
		//These functions can proceed OUTSIDE the transaction, since they are 'clean up' functions, and can run anytime:
		
    	//Remove any empty cost buckets here:
    	String SQL = "DELETE FROM " + SMTableiccosts.TableName
    	 	+ " WHERE ("
    	 		+ "(" + SMTableiccosts.bdCost + " = 0.00)"
    	 		+ " AND (" + SMTableiccosts.bdQty + " = 0.0000)"
    	 	+ ")"
    	 ;
    	try {
    		Statement stmt = conn.createStatement();
    		stmt.execute(SQL);
		} catch (SQLException e) {
			//Record this error, but don't stop the show over it - presumably it will run the next time:
			System.out.println("Error [1435002732] removing empty buckets with SQL: '" + SQL + "' - " + e.getMessage());
		}
    	
    	//Remove any 'canceling' cost buckets here:
    	try {
			removeCancelingCosts(conn);
		} catch (Exception e) {
			//We don't need to react to this, just trap it and go on - presumably it will run next time:
			System.out.println(e.getMessage());
		} 
    	
		clsDatabaseFunctions.freeConnection(context, conn, "[1547080857]");
		return true;
	}
	private void removeCancelingCosts(Connection conn) throws Exception{
		ICOption icopt = new ICOption();
		if(!icopt.load(conn)){
			throw new Exception("Error [1435072391] opening IC options - " + icopt.getErrorMessage());
		}
		//We don't remove cost buckets in an average costing system:
		if (icopt.getCostingMethod() == SMTableicoptions.COSTING_METHOD_AVERAGECOST){
			return;
		}
		
		//First make sure that the temporary table isn't there, and if it is, then delete it:
		String SQL = "DROP TEMPORARY TABLE IF EXISTS " + TEMP_COST_DELETE_TABLE_NAME;
		try {
			Statement stmt = conn.createStatement();
			stmt.execute(SQL);
		} catch (Exception e) {
			throw new Exception("Error [1435072393] deleting table '" + TEMP_COST_DELETE_TABLE_NAME + "' - " + e.getMessage());
		}
		
		SQL = "CREATE TEMPORARY TABLE " + TEMP_COST_DELETE_TABLE_NAME + " ("
			+ "sitemnumber varchar(" + Integer.toString(SMTableiccosts.sItemNumberLength) + ") NOT NULL"
			+ ", slocation varchar(" + Integer.toString(SMTableiccosts.sLocationLength) + ") NOT NULL"
			+ ", PRIMARY KEY (sitemnumber, slocation)"
			+ ")"
		;
		try {
			Statement stmt = conn.createStatement();
			stmt.execute(SQL);
		} catch (Exception e) {
			throw new Exception("Error [1435072392] creating table '" + this.TEMP_COST_DELETE_TABLE_NAME + "' - " + e.getMessage());
		}
		
		//Now populate temporary table with any iccost records that sum to zero AND are zero in the icitemslocation table:
		SQL = "INSERT INTO " + TEMP_COST_DELETE_TABLE_NAME + " (sitemnumber, slocation)"
			+ " SELECT"
			+ " COSTQUERY." + SMTableiccosts.sItemNumber
			+ ", COSTQUERY." + SMTableiccosts.sLocation
			+ " FROM"
			+ " ("
			+ "select"
			+ " " + SMTableiccosts.TableName + "." + SMTableiccosts.sItemNumber
			+ ", " + SMTableiccosts.TableName + "." + SMTableiccosts.sLocation
			+ ", SUM(" + SMTableiccosts.TableName + "." + SMTableiccosts.bdQty + ") AS QTY"
			+ ", SUM(" + SMTableiccosts.TableName + "." + SMTableiccosts.bdCost + ") AS COST"
			+ " FROM"
			+ " " + SMTableiccosts.TableName	
			+ " GROUP BY " + SMTableiccosts.TableName + "." + SMTableiccosts.sItemNumber 
			+ ", " + SMTableiccosts.TableName + "." + SMTableiccosts.sLocation
			+ " HAVING ("
				+ "(SUM(" + SMTableiccosts.TableName + "." + SMTableiccosts.bdQty + ") = 0.0000)"
				+ " AND (SUM(" + SMTableiccosts.TableName + "." + SMTableiccosts.bdCost + ") = 0.00)"
			+ ")"
			+ ") AS COSTQUERY"
			+ " LEFT JOIN " + SMTableicitemlocations.TableName
			+ "	ON (COSTQUERY." + SMTableiccosts.sItemNumber + "=" 
				+ SMTableicitemlocations.TableName + "." + SMTableicitemlocations.sItemNumber+ ")"
			+ " AND (COSTQUERY." + SMTableiccosts.sLocation + "="
				+ SMTableicitemlocations.TableName + "." + SMTableicitemlocations.sLocation + ")"
			+ "WHERE ("
				+ "(" + SMTableicitemlocations.TableName + "." + SMTableicitemlocations.sQtyOnHand + " = 0.0000)"
				+ " AND (" + SMTableicitemlocations.TableName + "." + SMTableicitemlocations.sTotalCost + " = 0.00)"
				+ " AND (" + SMTableicitemlocations.TableName + "." + SMTableicitemlocations.sItemNumber + " IS NOT NULL)"
			+ ")"
		;
		try {
			Statement stmt = conn.createStatement();
			stmt.execute(SQL);
		} catch (Exception e) {
			throw new Exception("Error [1435072394] populating table '" + TEMP_COST_DELETE_TABLE_NAME + "' with SQL: " +
				SQL + " - " + e.getMessage());
		}
		
		//Now delete the iccost records:
		SQL = "DELETE a FROM iccosts AS a"
			+ " RIGHT JOIN " + TEMP_COST_DELETE_TABLE_NAME + " AS b"
			+ " ON ("
				+ "(b.sitemnumber = a." + SMTableiccosts.sItemNumber + ")"
				+ " AND (b.slocation = a." + SMTableiccosts.sLocation + ")"
			+ ")"
		;
		try {
			Statement stmt = conn.createStatement();
			stmt.execute(SQL);
		} catch (Exception e) {
			throw new Exception("Error [1435072396] deleting 'canceled' cost buckets with SQL '" + SQL + "' - " + e.getMessage());
		}		
		
		//Finally, delete the temporay table:
		SQL = "DROP TEMPORARY TABLE IF EXISTS " + TEMP_COST_DELETE_TABLE_NAME;
		try {
			Statement stmt = conn.createStatement();
			stmt.execute(SQL);
		} catch (Exception e) {
			throw new Exception("Error [1435072397] deleting table '" + TEMP_COST_DELETE_TABLE_NAME + "' - " + e.getMessage());
		}
		
		return;
	}
    //NOTE - this function is not used in the real program, only in testing!!!
    public boolean post_with_data_transaction (
    		Connection conn,
    		Connection logConn,
    		String sUserFullName,
    		String sUserID,
    		boolean bCommitTransaction,
    		ServletContext context,
    		String sDBID
    		){
		
    	log = new SMLogEntry(logConn);
    	sSetLastEditedByFullName(sUserFullName);
    	clearErrorMessages();
    	m_iFlagInvoices = false;
    	//First check to make sure no one else is posting:
    	ICOption option = new ICOption();
    	try{
    		String sPostingProcess = "POSTING IC BATCH";
    		option.checkAndUpdatePostingFlagWithoutConnection(
    			context, 
    			sDBID, 
    			clsServletUtilities.getFullClassName(this.toString()) + ".post_with_data_transaction", 
    			sUserFullName, sPostingProcess);
    	}catch (Exception e){
			addErrorMessage("Error checking for previous posting - " + e.getMessage());
    		return false;
    	}

    	if(!clsDatabaseFunctions.start_data_transaction(conn)){
    		//Clear the posting flag:
    		try {
    			option.resetPostingFlagWithoutConnection(context, sDBID);
			} catch (Exception e) {
				addErrorMessage("Error [1529952433] resetting posting flag in icoptions - " + e.getMessage());
			}
    		
    		//Clear the posting flag, then return
    		return false;
    	}
    	
    	try {
			post_without_data_transaction(conn, sUserFullName, sUserID);
		} catch (Exception e1) {
    		//Clear the posting flag:
    		clsDatabaseFunctions.rollback_data_transaction(conn);
    		try {
				option.resetPostingFlagWithoutConnection(context, sDBID);
			} catch (Exception e) {
				addErrorMessage("Error [1529952434] resetting posting flag in icoptions - " + e.getMessage());
			}
    		//Clear the posting flag, then return
    		return false;
		}

		clsDatabaseFunctions.commit_data_transaction(conn);
		try {
			option.resetPostingFlagWithoutConnection(context, sDBID);
		} catch (Exception e) {
			addErrorMessage("Error [1529952435] resetting posting flag in icoptions - " + e.getMessage());
		}
		
		//Clear the posting flag, then return
		return true;
    }
       
    
    
    public void postImportedBatchwithout_data_transaction (
    		Connection conn,
    		String sUserFullName,
    		String sUserID)throws Exception{
    	
    	log = new SMLogEntry(conn);    	

    	post_without_data_transaction(conn, sUserFullName, sUserID);
    }
    public void post_without_data_transaction(Connection conn, String sUserFullName, String sUserID) throws Exception{
	
    	if (!getICOptions(conn)){
    		throw new Exception("Error reading IC Options - " + getErrorMessages());
    	}
    	
    	//Check all of the entries first to make sure they can be posted:
    	String SQL = "SELECT "
    		+ SMTableicbatchentries.lid
    		+ ", " + SMTableicbatchentries.lentrynumber
    		+ " FROM " + SMTableicbatchentries.TableName
    		+ " WHERE (" 
    		+ SMTableicbatchentries.lbatchnumber + " = " + sBatchNumber()
    		+ ")"
    		+ " ORDER BY " + SMTableicbatchentries.lentrynumber + " ASC";
    	boolean bBatchPassed = true;
    	if (bDebugMode){
    		log.writeEntry(
    			sUserID, 
    			SMLogEntry.LOG_OPERATION_ICBATCHPOST, 
    			"Entering post_without_data_transaction", "Batch #:" + sBatchNumber(), "[1376509385]");
    	}
    	long lEntryCount = 0;
    	
    	try {
    		ResultSet rsAscendingEntryList = clsDatabaseFunctions.openResultSet(SQL, conn);
    		
        	while (rsAscendingEntryList.next()){
        		//Check each entry
        		if(!checkEntry(
        				Long.toString(rsAscendingEntryList.getLong(SMTableicbatchentries.lentrynumber)),
        				Long.toString(rsAscendingEntryList.getLong(SMTableicbatchentries.lid)), 
        				conn,
        				sUserID)){
        			bBatchPassed = false;
        		}
        		lEntryCount ++;
        	}
        	rsAscendingEntryList.close();
        	
    	}catch (SQLException e){
    		throw new Exception("Error opening entry list result set - " + e.getMessage());
    	}

    	//[1519070245]
    	if (!checkForNonStockItems(conn)){
    		bBatchPassed = false;
    	}
    	
    	//If there are no entries, don't post
    	if (lEntryCount == 0){
    		addErrorMessage("Batch " + sBatchNumber() + " cannot be posted with no entries.");
    		bBatchPassed = false;
    	}
    	
    	//If the batch didn't pass, just return false:
    	if (bBatchPassed == false){
    		throw new Exception(getErrorMessages());
    	}
    	
    	//Next, create transactions for all of the entries:
    	if (bLogDebug){
	    	log.writeEntry(
	    			sUserID, 
	        		SMLogEntry.LOG_OPERATION_ICBATCHPOST, 
	        		"In post_without_data_transaction Batch #:" + sBatchNumber()
	        		+ " Going into createTransactions",
	        		"Costing method = " 
	        		+ SMTableicoptions.Get_Costing_Method_Label((int) lCostingMethod)
	        		+ ", Allow negatives = " + bAllowNegativeQtys,
	        		"[1376509530]"
	        );
    	}
		SQL = "SELECT "
			+ SMTableicbatchentries.lid
			+ ", " + SMTableicbatchentries.lentrynumber
			+ " FROM " + SMTableicbatchentries.TableName
			+ " WHERE (" 
			+ SMTableicbatchentries.lbatchnumber + " = " + sBatchNumber()
			+ ")"
			+ " ORDER BY " + SMTableicbatchentries.lentrynumber + " ASC";

    	try {
    		ResultSet rsCreateTransactions = clsDatabaseFunctions.openResultSet(SQL, conn);

        	while (rsCreateTransactions.next()){
        		if(!createTransactions(
        				Long.toString(rsCreateTransactions.getLong(SMTableicbatchentries.lentrynumber)),
        				Long.toString(rsCreateTransactions.getLong(SMTableicbatchentries.lid)), 
        				conn,
        				sUserFullName,
        				sUserID)){
                	throw new Exception(getErrorMessages());
        		}
        	}
        	rsCreateTransactions.close();
        	
    	}catch (SQLException e){
        	throw new Exception("Error opening entry list result set - " + e.getMessage());
    	}

    	//If it's a shipment batch, update any invoice details with costs from the batch, and update
    	//the 'day end number' (batch number) in the invoice header:
    	if (bLogDebug){
	    	log.writeEntry(
	    			sUserID,
	    			SMLogEntry.LOG_OPERATION_ICBATCHPOSTINVOICEUPDATE,
	        		"In post_without_data_transaction Batch #:" + sBatchNumber(), 
	        		"m_iFlagInvoices = " + m_iFlagInvoices
	        		+ ", this.iBatchType = " + this.iBatchType(),
	        		"[1376509545]"
	        );
    	}
    	
    	if (m_iFlagInvoices){
	    	if (this.iBatchType() == ICBatchTypes.IC_SHIPMENT){
	    		/* Here's the update statement without variables:
	    		UPDATE icentrylines, InvoiceDetails SET InvoiceDetails.dExtendedCost = -1*(icentrylines.bdcost)
 				WHERE (
					(icentrylines.lbatchnumber = ENTERBATCHNUMBER)
 					AND (icentrylines.linvoicelinenumber = InvoiceDetails.iLineNumber)
 					AND (icentrylines.sinvoicenumber = InvoiceDetails.sInvoiceNumber)
	    		 */
	    		SQL = "UPDATE " + SMTableicentrylines.TableName + ", " + SMTableinvoicedetails.TableName
	    		+ " SET " + SMTableinvoicedetails.TableName + "." + SMTableinvoicedetails.dExtendedCost 
	    		+ " = -1*(" + SMTableicentrylines.TableName + "." + SMTableicentrylines.bdcost + ")"
	    		+ " WHERE ("
	    			+ "(" + SMTableicentrylines.TableName + "." + SMTableicentrylines.lbatchnumber 
	    			+ " = " + this.sBatchNumber() + ")"
	    			+ " AND (" + SMTableicentrylines.TableName + "." + SMTableicentrylines.linvoicelinenumber
	    			+ " = " + SMTableinvoicedetails.TableName + "." + SMTableinvoicedetails.iLineNumber + ")"
	    			+ " AND (" + SMTableicentrylines.TableName + "." + SMTableicentrylines.sinvoicenumber
	    			+ " = " + SMTableinvoicedetails.TableName + "." + SMTableinvoicedetails.sInvoiceNumber + ")"
	 			+ ")"
	 			;
	    		if (bLogDebug){
		    		log.writeEntry(
		    			sUserID,
	        			SMLogEntry.LOG_OPERATION_ICBATCHPOSTINVOICEUPDATE,
	            		"UPDATING invoicelines cost for Batch #:" + sBatchNumber(), 
	            		"SQL = " + SQL,
	            		"[1376509527]"
		            );
	    		}
	    		try {
					Statement stmt = conn.createStatement();
					stmt.executeUpdate(SQL);
				} catch (SQLException e) {
					if (bLogDebug){
						log.writeEntry(
							sUserID,
			        		SMLogEntry.LOG_OPERATION_ICBATCHPOSTINVOICEUPDATE,
			            	"ERROR UPDATING invoicelines cost for Batch #:" + sBatchNumber(), 
			            	"SQLException = " + e.getMessage(),
			            	"[1376509554]"
				        );
					}
		        	throw new Exception("Error updating invoice detail costs with SQL: " + SQL + " - " + e.getMessage());
				}
				
				//Update the transaction ID on the invoice details:
				/* Here's the statement without variables:
				UPDATE icentrylines, InvoiceDetails, ictransactions
 				SET InvoiceDetails.lictransactionid = ictransactions.lid
 				WHERE (
				(icentrylines.lbatchnumber = ENTERBATCHNUMBER)
 				AND (icentrylines.linvoicelinenumber = InvoiceDetails.iLineNumber)
 				AND (icentrylines.sinvoicenumber = InvoiceDetails.sInvoiceNumber)
 				AND (ictransactions.llineid = icentrylines.lid)
				)
				 */
				SQL = "UPDATE"
					+ " " + SMTableicentrylines.TableName + ", " + SMTableinvoicedetails.TableName 
					+ ", " + SMTableictransactions.TableName
					+ " SET " + SMTableinvoicedetails.TableName + "." 
						+ SMTableinvoicedetails.lictransactionid + " = " 
						+ SMTableictransactions.TableName + "." + SMTableictransactions.lid
					+ " WHERE ("
						+ "(" + SMTableicentrylines.TableName + "." + SMTableicentrylines.lbatchnumber 
							+ " = " + this.sBatchNumber() + ")"
						+ " AND (" + SMTableicentrylines.TableName + "." 
							+ SMTableicentrylines.linvoicelinenumber + " = " 
							+ SMTableinvoicedetails.TableName + "." 
							+ SMTableinvoicedetails.iLineNumber + ")"
						+ " AND (" + SMTableicentrylines.TableName + "." 
							+ SMTableicentrylines.sinvoicenumber + " = " 
							+ SMTableinvoicedetails.TableName + "." 
							+ SMTableinvoicedetails.sInvoiceNumber + ")"
						+ " AND (" + SMTableictransactions.TableName + "." 
							+ SMTableictransactions.llineid + " = " 
							+ SMTableicentrylines.TableName + "." + SMTableicentrylines.lid + ")"
						+ ")"
				;
	    		try {
					Statement stmt = conn.createStatement();
					stmt.executeUpdate(SQL);
				} catch (SQLException e) {
					if (bLogDebug){
						log.writeEntry(
							sUserID,
			        		SMLogEntry.LOG_OPERATION_ICBATCHPOSTINVOICEUPDATE,
			            	"ERROR UPDATING invoicelines transaction ID for Batch #:" + sBatchNumber(), 
			            	"SQLException = " + e.getMessage(),
			            	"[1376509546]"
				        );
					}
					throw new Exception("Error updating invoice detail transaction IDs with SQL: " + SQL + " - " + e.getMessage());
				}
				
				//Update the day end number (batch number) in the invoice header:
		   		SQL = "UPDATE " + SMTableicentrylines.TableName + ", " + SMTableinvoiceheaders.TableName
	    		+ " SET " + SMTableinvoiceheaders.TableName + "." + SMTableinvoiceheaders.iDayEndNumber 
	    		+ " = " + this.sBatchNumber()
	    		+ " WHERE ("
	    			+ "(" + SMTableicentrylines.TableName + "." + SMTableicentrylines.lbatchnumber 
	    			+ " = " + this.sBatchNumber() + ")"
	    			+ " AND (" + SMTableicentrylines.TableName + "." + SMTableicentrylines.sinvoicenumber
	    			+ " = " + SMTableinvoiceheaders.TableName + "." + SMTableinvoiceheaders.sInvoiceNumber + ")"
	 			+ ")"
	 			;
		   		if (bLogDebug){
		        	log.writeEntry(
		        			sUserID,
		        			SMLogEntry.LOG_OPERATION_ICBATCHPOSTINVOICEUPDATE,
		            		"UPDATING invoiceheaders day end number for Batch #:" + sBatchNumber(), 
		            		"SQL = " + SQL,
		            		"[1376509562]"
			            );
		   		}
		   		try {
					Statement stmt = conn.createStatement();
					stmt.executeUpdate(SQL);
				} catch (SQLException e) {
					if (bLogDebug){
						log.writeEntry(
								sUserID,
			        			SMLogEntry.LOG_OPERATION_ICBATCHPOSTINVOICEUPDATE,
			            		"ERROR UPDATING invoiceheaders day end number for Batch #:" + sBatchNumber(), 
			            		"SQLException = " + e.getMessage(),
			            		"[1376509563]"
				            );
					}
					throw new Exception("Error updating invoice dayend numbers with SQL: " + SQL + " - " + e.getMessage());
				}
	    	}
    	}
    	if (bLogDebug){
	    	log.writeEntry(
	    			sUserID,
	    			SMLogEntry.LOG_OPERATION_ICBATCHPOST,
	        		"In post_without_data_transaction Batch #:" + sBatchNumber(), 
	        		"Going into createGLBatch",
	        		"[1376509568]"
	        );
    	}
    	
    	if (!createGLBatch(conn, sUserID, sUserFullName)){
    		throw new Exception(getErrorMessages());
    	}
    	
    	//Update the batch:
    	iBatchStatus(SMBatchStatuses.POSTED);
    	//setPostingDateOLD("yyyy-MM-dd hh:mm:ss", ARUtilities.nowAsSQLDate());
    	setPostingDate(clsDateAndTimeConversions.now("MM/dd/yyyy hh:mm a"));
    	if (bLogDebug){    	
    		log.writeEntry(
    			sUserID,
    			SMLogEntry.LOG_OPERATION_ICBATCHPOST,
        		"In post_without_data_transaction Batch #:" + sBatchNumber(), 
        		"Going into save_without_data_transaction",
        		"[1376509571]"
    		);
    	}
    	
    	if (!save_without_data_transaction(conn, sUserFullName, sUserID)){
    		throw new Exception("Error updating batch - " + getErrorMessages());
    	}
    	if (bLogDebug){
    		log.writeEntry(
    			sUserID,
    			SMLogEntry.LOG_OPERATION_ICBATCHPOST,
        		"In post_without_data_transaction Batch #:" + sBatchNumber(), 
        		"After successful save_without_data_transaction",
        		"[1376509555]"
    		);
    	}
    	
    	return;
    }

    private boolean checkForNonStockItems(Connection conn){
    	
    	String SQL = "SELECT"
    		+ " " + SMTableicentrylines.TableName + "." + SMTableicentrylines.sitemnumber
    		+ " FROM " + SMTableicentrylines.TableName + " LEFT JOIN "
    		+ SMTableicitems.TableName + " ON " + SMTableicentrylines.TableName + "." + SMTableicentrylines.sitemnumber
    		+ " = " + SMTableicitems.TableName + "." + SMTableicitems.sItemNumber
    		+ " WHERE ("
    			+ "(" + SMTableicentrylines.TableName + "." + SMTableicentrylines.lbatchnumber 
    			+ " = " + sBatchNumber() + ")"
    			+ " AND (" + SMTableicitems.TableName + "." + SMTableicitems.inonstockitem + " = 1)"
    		+ ")"
    	;
    	try {
			ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
			if (rs.next()){
				addErrorMessage("Item " + rs.getString(SMTableicentrylines.TableName + "." 
					+ SMTableicentrylines.sitemnumber) 
					+ " is a NON-STOCK item and cannot be processed through inventory.");
				rs.close();
				return false;
			}
			rs.close();
		} catch (SQLException e) {
			addErrorMessage("Error checking batch for non-stock items with SQL: " + SQL + " - " + e.getMessage()); 
			return false;
		}
    	return true;
    }
    private boolean checkEntry(String sEntryNumber, String sEntryID, Connection conn, String sUserID){

    	//Check every entry to make sure they are all in balance - if any aren't, add the error message
    	//and return false:
    	ICEntry entry = new ICEntry();
    	
    	if (!entry.load(sEntryID, conn)){
    		addErrorMessage("<br>In checking entries - could not load entry with ID " + sEntryID);
    		return false;
    	}
    	
    	if (!entry.checkIfKeyIsUnique(conn)){
    		addErrorMessage("Entry " + sEntryNumber 
    			+ " is not a unique transaction.");
    		return false;
    	}
    	
		//First check that the transaction date is within the allowed posting period:
		SMOption opt = new SMOption();
		if (!opt.load(conn)){
			addErrorMessage("Error [1457642863] - could not check posting period - " + opt.getErrorMessage() + ".");
		}
		try {
			opt.checkDateForPosting(
				entry.sStdEntryDate(), 
				"ENTRY DATE", 
				conn, 
				sUserID
			);
		} catch (Exception e2) {
			addErrorMessage("Error [1483452764] on Entry " + sEntryNumber + " - " 
				+ ICEntryTypes.Get_Entry_Type(Integer.parseInt(entry.sEntryType())) 
				+ " " + entry.sDocNumber() + ", " + entry.sEntryDescription()
				+ " - " + e2.getMessage() + "."); 
			return false;
		}
    	
    	return true;
    }
    private boolean createTransactions(String sEntryNumber, 
    		String sEntryID, 
    		Connection conn,
    		String sUserFullName,
    		String sUserID){

    	if(bLogDebug){
        	log.writeEntry(
        			sUserID, 
        			SMLogEntry.LOG_OPERATION_ICBATCHPOST, 
        			"User " + sUserFullName + " entering createTransactions",
        			"Batch #:" + sBatchNumber() + ", EntryNumber #:" + sEntryNumber, "[1376509383]");
    	}
    	
    	//Open the entry, post it to the ictransactions table:
    	ICEntry entry = new ICEntry();
    	if (!entry.load(sEntryID, conn)){
    		addErrorMessage("<br>In creating transactions - could not load entry with ID " + sEntryID);
    		return false;
    	}    	
    	
       	//If this is a credit note, we want to know that before we go into the loop, so we can get the matching invoice number before
       	// we start iterating through the loop:
       	//If it's a SHIPMENT entry, AND the lines have a POSITIVE qty shipped, it's a RETURNED SHIPMENT or a CREDIT NOTE.
       	//If it then also has an invoice number, then it's a credit note:
       	
       	//This will carry the invoice number from which this credit note is created - IF this is a credit note (if not it will just be blank)
       	String sMatchingInvoiceNumber = "";
       	if(Integer.parseInt(entry.sEntryType()) == ICEntryTypes.SHIPMENT_ENTRY){
       		if (entry.getLineByIndex(0).sInvoiceNumber().compareToIgnoreCase("") !=0){
    			BigDecimal bdQtyShipped = new BigDecimal(entry.getLineByIndex(0).sQtySTDFormat());
    			if (bdQtyShipped.compareTo(BigDecimal.ZERO) > 0){
    				
    				//If the entry corresponds to an SM invoice or credit, then ALL of the entrylines will carry that invoice number in the
    		       	// 'sinvoicenumber' field.  We'll use that invoice number now to get the 'Matching Invoice Number':
    				String SQL = "SELECT " + SMTableinvoiceheaders.sMatchingInvoiceNumber
    					+ " FROM " + SMTableinvoiceheaders.TableName
    					+ " WHERE ("
    						+ "(" + SMTableinvoiceheaders.sInvoiceNumber + " = '" + entry.getLineByIndex(0).sInvoiceNumber() +"')"
    					+ ")"
    				;
    				try {
						ResultSet rsGetMatchingInvoice = clsDatabaseFunctions.openResultSet(SQL, conn);
						if (rsGetMatchingInvoice.next()){
							sMatchingInvoiceNumber = clsStringFunctions.PadLeft(
								rsGetMatchingInvoice.getString(SMTableinvoiceheaders.sMatchingInvoiceNumber).trim(),
								" ",
								SMTableinvoiceheaders.NUMBER_OF_CHARACTERS_USED_IN_INVOICE_NUMBER);
							//Now we've got the matching invoice number for our credit note, padded as it should be in the invoice details table.
						}else{
							
						}
					} catch (SQLException e) {
			   			addErrorMessage("Error [1551413442] - could not reading matching invoice number for credit note with SQL: '" + SQL + "' - " + e.getMessage());
		    			return false;
					}
       			}
       		}
       	}
       	
    	for (int i = 0; i < entry.getLineCount(); i++){
    	
    		ICEntryLine line = entry.getLineByIndex(i);
    		if (!line.getItemDetails(conn)){
    			addErrorMessage("<BR>Could not get item details: " + line.getErrorMessage());
    			return false;
    		}
    		java.sql.Date datEntryDate;
			try {
				datEntryDate = clsDateAndTimeConversions.StringTojavaSQLDate("MM/dd/yyyy", entry.sStdEntryDate());
			} catch (ParseException e1) {
				addErrorMessage("Error:[1423843612] Invalid entry date: '"
					    + entry.sStdEntryDate() + "' - " + e1.getMessage());
				return false;
			}
    		
    		//Initialize the array of transaction details:
    		m_arrTransactionDetails = new ArrayList<ICTransactionDetail>(0);
    		
    		if(Integer.parseInt(entry.sEntryType()) == ICEntryTypes.ADJUSTMENT_ENTRY){
        		//Update the cost buckets:
    	    	if(bLogDebug){
    	        	log.writeEntry(
    	        			sUserID, 
    	        			SMLogEntry.LOG_OPERATION_ICBATCHPOST, 
    	        			"User " + sUserFullName + " in createTransactions, going to processAdjustmentLine",
    	        			"Batch #:" + sBatchNumber() 
    	        			+ ", EntryNumber #:" + sEntryNumber
    	        			+ ", Line #:" + line.sLineNumber(),
    	        			"[1376509523]"
    	        	);
    	    	}

    			if (!processAdjustmentLine(line, datEntryDate, sUserFullName, sUserID, conn)){
    				return false;
    			}
    		}
    		if(Integer.parseInt(entry.sEntryType()) == ICEntryTypes.SHIPMENT_ENTRY){
    			//On SHIPMENTS (and SM Invoices), the 'qty shipped' is negative.
    			// On RETURNS (and SM Credit Notes), the 'qty shipped' is positive
    			BigDecimal bdQtyShipped = new BigDecimal(line.sQtySTDFormat());
    			if (bdQtyShipped.compareTo(BigDecimal.ZERO) < 0){
        	    	if(bLogDebug){
        	        	log.writeEntry(
        	        			sUserID, 
        	        			SMLogEntry.LOG_OPERATION_ICBATCHPOST, 
        	        			"User " + sUserFullName + " in createTransactions, going to processShipmentLine",
        	        			"Batch #:" + sBatchNumber() 
        	        			+ ", EntryNumber #:" + sEntryNumber
        	        			+ ", Line #:" + line.sLineNumber(),
        	        			"[1376509543]"
        	        	);
        	    	}

    				if (!processShipmentLine(line, datEntryDate, sUserFullName, sUserID, conn)){
        				return false;
        			}
    			}else{
    				//First, if it's a credit note from order entry, update the costs on it
    				//based on the original cost from the invoice it is crediting:
    				if (line.sInvoiceNumber().compareToIgnoreCase("") != 0){
    	    	    	if(bLogDebug){
    	    	        	log.writeEntry(
    	    	        			sUserID, 
    	    	        			SMLogEntry.LOG_OPERATION_ICBATCHPOST, 
    	    	        			"User " + sUserFullName + " in createTransactions, going to updateCreditLineCost",
    	    	        			"Batch #:" + sBatchNumber() 
    	    	        			+ ", EntryNumber #:" + sEntryNumber
    	    	        			+ ", Line #:" + line.sLineNumber(),
    	    	        			"[1376509551]"
    	    	        	);
    	    	    	}

    					if (!updateCreditLineCost(
    						sMatchingInvoiceNumber,
    						line, 
    						conn)){
    		    	    	if(bLogDebug){
    		    	        	log.writeEntry(
    		    	        			sUserID, 
    		    	        			SMLogEntry.LOG_OPERATION_ICBATCHPOST, 
    		    	        			"User " + sUserFullName + " in createTransactions, updateCreditLineCost failed",
    		    	        			"Batch #:" + sBatchNumber() + ", EntryNumber #:" + sEntryNumber
    		    	        			+ ", Line #:" + line.sLineNumber(),
    		    	        			"[1376509552]");
    		    	    	}
    						
    						return false;
    					}
    				}
        	    	if(bLogDebug){
        	        	log.writeEntry(
        	        			sUserID, 
        	        			SMLogEntry.LOG_OPERATION_ICBATCHPOST, 
        	        			"User " + sUserFullName + " in createTransactions, going to processShipmentReturnLine",
        	        			"Batch #:" + sBatchNumber() + ", EntryNumber #:" + sEntryNumber
        	        			+ ", Line #:" + line.sLineNumber(),
        	        			"[1376509528]");
        	    	}

    	   			if (!processShipmentReturnLine(line, datEntryDate, sUserFullName, sUserID, conn)){
        				return false;
        			}
    			}
    			//If it's a shipment or return that was created from an invoice or credit note,
    			//then update the invoice line that it was produced from:
    			//We've replaced this with a single SQL at the end:
    			/*
    			if (line.sInvoiceNumber().compareToIgnoreCase("") != 0){
    				sCreatedFromInvoiceNumber = line.sInvoiceNumber();
    				if (m_iFlagInvoices){
    	    	    	if(bLogDebug){
    	    	        	log.writeEntry(
    	    	        			sUserName, 
    	    	        			SMLogEntry.LOG_OPERATION_ICBATCHPOST, 
    	    	        			"User " + sUserName + " in createTransactions, going to updateInvoiceLineCost",
    	    	        			"Batch #:" + sBatchNumber() + ", EntryNumber #:" + sEntryNumber
    	    	        			+ ", Line #:" + line.sLineNumber());
    	    	    	}

	    				if (!updateInvoiceLineCost(line, conn)){
	    					return false;
	    				}
    				}
    			}
    			*/
    		}

    		if(Integer.parseInt(entry.sEntryType()) == ICEntryTypes.PHYSICALCOUNT_ENTRY){
    			BigDecimal bdQtyChanged = new BigDecimal(line.sQtySTDFormat());
    			if (bdQtyChanged.compareTo(BigDecimal.ZERO) < 0){
        	    	if(bLogDebug){
        	        	log.writeEntry(
        	        			sUserID, 
        	        			SMLogEntry.LOG_OPERATION_ICBATCHPOST, 
        	        			"User " + sUserFullName + " in createTransactions, going to processPhysicalCountDecreaseLine",
        	        			"Batch #:" + sBatchNumber() + ", EntryNumber #:" + sEntryNumber
        	        			+ ", Line #:" + line.sLineNumber(),
        	        			"[1376509559]");
        	    	}

    				if (!processPhysicalCountDecreaseLine(line, datEntryDate, sUserFullName, sUserID, conn)){
        				return false;
        			}
    			}else{
    				if(bLogDebug){
    					log.writeEntry(
    						sUserID, 
    	        			SMLogEntry.LOG_OPERATION_ICBATCHPOST, 
    	        			"User " + sUserFullName + " in createTransactions, going to processPhysicalCountIncreaseLine",
    	        			"Batch #:" + sBatchNumber() + ", EntryNumber #:" + sEntryNumber
    	        			+ ", Line #:" + line.sLineNumber(),
    	        			"[1376509556]");
    				}
    	   			if (!processPhysicalCountIncreaseLine(line, datEntryDate, sUserFullName, sUserID, conn)){
        				return false;
        			}
    			}
    		}
    		
    		if(Integer.parseInt(entry.sEntryType()) == ICEntryTypes.TRANSFER_ENTRY){
    			if(bLogDebug){
    				log.writeEntry(
    					sUserID, 
	        			SMLogEntry.LOG_OPERATION_ICBATCHPOST, 
	        			"User " + sUserFullName + " in createTransactions, going to processTransferLine",
	        			"Batch #:" + sBatchNumber() + ", EntryNumber #:" + sEntryNumber
	        			+ ", Line #:" + line.sLineNumber(),
	        			"[1376509529]");
    			}
    			if (!processTransferLine(line, datEntryDate, sUserFullName, sUserID, conn)){
    				return false;
    			}
    		}
    		if(Integer.parseInt(entry.sEntryType()) == ICEntryTypes.RECEIPT_ENTRY){
        		//We need to create a new cost bucket here:
    			if(bLogDebug){
    				log.writeEntry(
    					sUserID, 
	        			SMLogEntry.LOG_OPERATION_ICBATCHPOST, 
	        			"User " + sUserFullName + " in createTransactions, going to processReceiptLine",
	        			"Batch #:" + sBatchNumber() + ", EntryNumber #:" + sEntryNumber
	        			+ ", Line #:" + line.sLineNumber(),
	        			"[1376509544]");
    			}
    			// [1519070245] - we only have to process the receipt line if it's STOCK inventory:
    			if (!processReceiptLineForStockInventory(line, datEntryDate, sUserFullName, sUserID, conn)){
    				return false;
    			}    			
    		}
    		//If the transaction is a transfer, the 'from' location transaction should show a negative
    		//qty and cost:
    		String sQty = line.sQtySTDFormat().replace(",", "");
    		BigDecimal bdCost 
    			= new BigDecimal(
    				line.sCostSTDFormat().replace(",", "")).setScale(2, BigDecimal.ROUND_HALF_UP);

    		if(Integer.parseInt(entry.sEntryType()) == ICEntryTypes.TRANSFER_ENTRY){
    			sQty = "-" + sQty;
    		}
    		//Add each line as a transaction in ictransactions:    		
	    	String SQL = "INSERT INTO " + SMTableictransactions.TableName
	    	+ " ("
	    		+ SMTableictransactions.bdcost
	    		+ ", " + SMTableictransactions.bdprice
	    		+ ", " + SMTableictransactions.bdqty
	    		+ ", " + SMTableictransactions.datpostingdate
	    		+ ", " + SMTableictransactions.ientrytype
	    		+ ", " + SMTableictransactions.llineid
	    		+ ", " + SMTableictransactions.loriginalbatchnumber
	    		+ ", " + SMTableictransactions.loriginalentrynumber
	    		+ ", " + SMTableictransactions.loriginallinenumber
	    		+ ", " + SMTableictransactions.scontrolacct
	    		+ ", " + SMTableictransactions.sdistributionacct
	    		+ ", " + SMTableictransactions.sdocnumber
	    		+ ", " + SMTableictransactions.sentrydescription
	    		+ ", " + SMTableictransactions.sitemnumber
	    		+ ", " + SMTableictransactions.slinedescription
	    		+ ", " + SMTableictransactions.slocation
	    		+ ", " + SMTableictransactions.lpostedbyid
	    		+ ", " + SMTableictransactions.spostedbyfullname
	    		+ ", " + SMTableictransactions.sunitofmeasure
	
	    	+ ") VALUES ("
	    		+ clsManageBigDecimals.BigDecimalTo2DecimalSQLFormat(bdCost)
	    		+ ", " + line.sPriceSTDFormat().replace(",", "")
	    		+ ", " + sQty
	    		+ ", '" + clsDateAndTimeConversions.utilDateToString(datEntryDate, "yyyy-MM-dd") + "'"
	    		+ ", " + entry.sEntryType()
	    		+ ", " + line.sId()
	    		+ ", " + sBatchNumber()
	    		+ ", " + entry.sEntryNumber()
	    		+ ", " + line.sLineNumber()
	    		+ ", '" + line.sControlAcct() + "'"
	    		+ ", '" + line.sDistributionAcct() + "'"
	    		+ ", '" + clsDatabaseFunctions.FormatSQLStatement(entry.sDocNumber()) + "'"
	    		+ ", '" + clsDatabaseFunctions.FormatSQLStatement(entry.sEntryDescription()) + "'"
	    		+ ", '" + clsDatabaseFunctions.FormatSQLStatement(line.sItemNumber()) + "'"
	    		+ ", '" + clsDatabaseFunctions.FormatSQLStatement(line.sDescription()) + "'"
	    		+ ", '" + clsDatabaseFunctions.FormatSQLStatement(line.sLocation()) + "'"
	    		+ ", " +  m_llasteditedbyid + ""
	    		+ ", '" + clsDatabaseFunctions.FormatSQLStatement(m_slasteditedbyfullname) + "'"
	    		+ ", '" + clsDatabaseFunctions.FormatSQLStatement(line.sUnitOfMeasure()) + "'"
	    	+ ")"
	    	;
			if(bLogDebug){
				log.writeEntry(
					sUserID, 
        			SMLogEntry.LOG_OPERATION_ICBATCHPOST, 
        			"User " + sUserFullName + " in createTransactions, inserting into ictransactions"
        			+ " Batch #:" + sBatchNumber() + ", EntryNumber #:" + sEntryNumber
        			+ ", Line #:" + line.sLineNumber(),
        			"SQL = " + SQL,
        			"[1376509526]"
				);
			}
	    	try{
	    		Statement stmt = conn.createStatement();
	    		stmt.executeUpdate(SQL);
	    	}catch(SQLException e){
	    		addErrorMessage("SQL Error inserting transaction for entry #: " + sEntryNumber 
	    				+ " - " + e.getMessage());
	    		return false;
	    	}
	    	
	    	//Get the transaction ID:
	    	long lTransactionID = -1;
	    	try {
	    		lTransactionID = clsDatabaseFunctions.getLastInsertID(conn); //CHECKED
			} catch (SQLException e) {
	    		addErrorMessage("SQL Error getting transaction ID for entry #: " + sEntryNumber 
	    				+ " - " + e.getMessage());
	    		return false;
			}
			if (!writeTransactionDetails(conn, lTransactionID, sUserFullName)){
	    		return false;
	    	}
	    	
	    	BigDecimal bdTotalQty = new BigDecimal(0);
	    	BigDecimal bdTotalCost = new BigDecimal(0);
	    
	    	SQL = "SELECT "
	    		
	    		+ "SUM(" + SMTableiccosts.bdCost + ") AS TOTALCOST"
	    		+ ", SUM(" + SMTableiccosts.bdQty + ") AS TOTALQTY"
	    		+ " FROM " + SMTableiccosts.TableName
	    		+ " WHERE ("
	    			+ "(" + SMTableiccosts.sItemNumber + " = '" + line.sItemNumber() + "')"
	    			+ " AND (" + SMTableiccosts.sLocation + " = '" + line.sLocation() + "')"
	    		+ ")"
	    		;
	    	
	    	try {
	    		ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
	    		if (rs.next()){
	    			bdTotalQty = rs.getBigDecimal("TOTALQTY").setScale(4, BigDecimal.ROUND_HALF_UP);
	    			if (bdTotalQty == null){
	    				bdTotalQty = BigDecimal.ZERO;
	    			}
	    			bdTotalCost = rs.getBigDecimal("TOTALCOST").setScale(2, BigDecimal.ROUND_HALF_UP);
	    			if (bdTotalCost == null){
	    				bdTotalCost = BigDecimal.ZERO;
	    			}
	    		}
	    		rs.close();
	    	}catch (SQLException e){
	    		addErrorMessage("SQL Error getting total qty and cost for entry #: " + sEntryNumber 
	    				+ " - " + e.getMessage());
	    		return false;
	    	}
	    	bdTotalQty = bdTotalQty.setScale(4, BigDecimal.ROUND_HALF_UP);
	    	bdTotalCost = bdTotalCost.setScale(2, BigDecimal.ROUND_HALF_UP);
	    	//Update the ic item locations file:
	    	SQL = "INSERT INTO " + SMTableicitemlocations.TableName
	    		+ " ("
	    		+ SMTableicitemlocations.sItemNumber
	    		+ ", " + SMTableicitemlocations.sLocation
	    		+ ", " + SMTableicitemlocations.sQtyOnHand
	    		+ ", " + SMTableicitemlocations.sTotalCost
	    		+ ") VALUES ("
	    		+ "'" + line.sItemNumber() + "'"
	    		+ ", '" + line.sLocation() + "'"
	    		+ ", " + clsManageBigDecimals.BigDecimalToFormattedString("#######0.0000", bdTotalQty)
	    		+ ", " + clsManageBigDecimals.BigDecimalTo2DecimalSQLFormat(bdTotalCost)
	    		+ ") ON DUPLICATE KEY UPDATE "
	    		+ SMTableicitemlocations.sQtyOnHand + " = " + clsManageBigDecimals.BigDecimalToFormattedString("#######0.0000", bdTotalQty)
	    		+ ", " + SMTableicitemlocations.sTotalCost + " = " + clsManageBigDecimals.BigDecimalTo2DecimalSQLFormat(bdTotalCost)
	    	;
	    	try{
	    		Statement stmt = conn.createStatement();
	    		stmt.executeUpdate(SQL);
	    	}catch(SQLException e){
	    		addErrorMessage("SQL Error inserting item location for entry #: " + sEntryNumber 
	    				+ " - " + e.getMessage());
	    		return false;
	    	}
	    	
	    	//If the transaction was a transfer, we need to add another transaction, and update info
	    	//for the TARGET location, too:
	    	if(Integer.parseInt(entry.sEntryType()) == ICEntryTypes.TRANSFER_ENTRY){
	    		BigDecimal bdTransferToCost = new BigDecimal(line.sCostSTDFormat().replace(",", ""));
	    		//Negate the transfer 'to' cost, because the transfer 'from' cost is negative:
	    		bdTransferToCost = bdTransferToCost.negate().setScale(2, BigDecimal.ROUND_HALF_UP);
	    		//Add another transaction record for the 'to' location:
		    	SQL = "INSERT INTO " + SMTableictransactions.TableName
		    	+ " ("
		    		+ SMTableictransactions.bdcost
		    		+ ", " + SMTableictransactions.bdprice
		    		+ ", " + SMTableictransactions.bdqty
		    		+ ", " + SMTableictransactions.datpostingdate
		    		+ ", " + SMTableictransactions.ientrytype
		    		+ ", " + SMTableictransactions.llineid
		    		+ ", " + SMTableictransactions.loriginalbatchnumber
		    		+ ", " + SMTableictransactions.loriginalentrynumber
		    		+ ", " + SMTableictransactions.loriginallinenumber
		    		+ ", " + SMTableictransactions.scontrolacct
		    		+ ", " + SMTableictransactions.sdistributionacct
		    		+ ", " + SMTableictransactions.sdocnumber
		    		+ ", " + SMTableictransactions.sentrydescription
		    		+ ", " + SMTableictransactions.sitemnumber
		    		+ ", " + SMTableictransactions.slinedescription
		    		+ ", " + SMTableictransactions.slocation
		    		+ ", " + SMTableictransactions.lpostedbyid
		    		+ ", " + SMTableictransactions.spostedbyfullname
		    		+ ", " + SMTableictransactions.sunitofmeasure
		
		    	+ ") VALUES ("
		    		+ clsManageBigDecimals.BigDecimalTo2DecimalSQLFormat(bdTransferToCost)
		    		+ ", " + line.sPriceSTDFormat().replace(",", "")
		    		+ ", " + line.sQtySTDFormat().replace(",", "")
		    		+ ", '" + clsDateAndTimeConversions.utilDateToString(datEntryDate, "yyyy-MM-dd") + "'"
		    		+ ", " + entry.sEntryType()
		    		+ ", " + line.sId()
		    		+ ", " + sBatchNumber()
		    		+ ", " + entry.sEntryNumber()
		    		+ ", " + line.sLineNumber()
		    		+ ", '" + line.sControlAcct() + "'"
		    		+ ", '" + line.sDistributionAcct() + "'"
		    		+ ", '" + clsDatabaseFunctions.FormatSQLStatement(entry.sDocNumber()) + "'"
		    		+ ", '" + clsDatabaseFunctions.FormatSQLStatement(entry.sEntryDescription()) + "'"
		    		+ ", '" + clsDatabaseFunctions.FormatSQLStatement(line.sItemNumber()) + "'"
		    		+ ", '" + clsDatabaseFunctions.FormatSQLStatement(line.sDescription()) + "'"
		    		+ ", '" + clsDatabaseFunctions.FormatSQLStatement(line.sTargetLocation()) + "'"
		    		+ ", " + m_llasteditedbyid + ""
		    		+ ", '" + clsDatabaseFunctions.FormatSQLStatement(m_slasteditedbyfullname) + "'"
		    		+ ", '" + clsDatabaseFunctions.FormatSQLStatement(line.sUnitOfMeasure()) + "'"
		    	+ ")"
		    	;

		    	try{
		    		Statement stmt = conn.createStatement();
		    		stmt.executeUpdate(SQL);
		    	}catch(SQLException e){
		    		addErrorMessage("SQL Error inserting transaction for 'TO' location entry #: " 
		    				+ sEntryNumber + " - " + e.getMessage());
		    		return false;
		    	}

				//Write the transaction detail for the cost bucket that was created for the 'TO LOCATION'
				//transfer:
		    	long lToLocationTransactionID = -1;
		    	try {
					lToLocationTransactionID = clsDatabaseFunctions.getLastInsertID(conn);
				} catch (SQLException e) {
		    		addErrorMessage("SQL getting last insert ID for 'TO' location entry #: " 
		    				+ sEntryNumber + " - " + e.getMessage());
					return false;
				}
				
		    	try{
		    		m_tdToLocation.setM_ltransactionid(lToLocationTransactionID);
		    	}catch(Exception e){
		    		addErrorMessage("Error setting location transaction ID ('" + lToLocationTransactionID 
		    				+ "') for 'TO' location entry #: " 
		    				+ sEntryNumber + " - " + e.getMessage());
					return false;
		    	}
				if (!m_tdToLocation.save_without_data_transaction(conn, sUserFullName)){
		    		addErrorMessage("Error saving TO LOCATION transaction detail for entry #: " + sEntryNumber 
		    				+ " - " + m_tdToLocation.getErrorMessages());
		    		return false;
				}
				
				if(bLogDebug){
					log.writeEntry(
						sUserID, 
	        			SMLogEntry.LOG_OPERATION_ICBATCHPOST, 
	        			"User " + sUserFullName + " in createTransactions, inserting 'transfer to' record "
	        			+ "into ictransactions"
	        			+ " Batch #:" + sBatchNumber() + ", EntryNumber #:" + sEntryNumber
	        			+ ", Line #:" + line.sLineNumber(),
	        			"SQL = " + SQL,
	        			"[1376509540]"
					);
				}
				
				SQL = "SELECT "
		    		
		    		+ "SUM(" + SMTableiccosts.bdCost + ") AS TOTALCOST"
		    		+ ", SUM(" + SMTableiccosts.bdQty + ") AS TOTALQTY"
		    		+ " FROM " + SMTableiccosts.TableName
		    		+ " WHERE ("
		    			+ "(" + SMTableiccosts.sItemNumber + " = '" + line.sItemNumber() + "')"
		    			+ " AND (" + SMTableiccosts.sLocation + " = '" + line.sTargetLocation() + "')"
		    		+ ")"
		    		;
		    	
		    	try {
		    		ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
		    		if (rs.next()){
		    			bdTotalQty = rs.getBigDecimal("TOTALQTY");
		    			if (bdTotalQty == null){
		    				bdTotalQty = BigDecimal.ZERO;
		    			}
		    			bdTotalCost = rs.getBigDecimal("TOTALCOST");
		    			if (bdTotalCost == null){
		    				bdTotalCost = BigDecimal.ZERO;
		    			}
		    		}
		    		rs.close();
		    	}catch (SQLException e){
		    		addErrorMessage("SQL Error getting (target) total qty and cost for entry #: " + sEntryNumber 
		    				+ " - " + e.getMessage());
		    		return false;
		    	}
		    	bdTotalQty = bdTotalQty.setScale(4, BigDecimal.ROUND_HALF_UP);
		    	bdTotalCost = bdTotalCost.setScale(2, BigDecimal.ROUND_HALF_UP);
		    	//Update the ic item locations file:
		    	SQL = "INSERT INTO " + SMTableicitemlocations.TableName
		    		+ " ("
		    		+ SMTableicitemlocations.sItemNumber
		    		+ ", " + SMTableicitemlocations.sLocation
		    		+ ", " + SMTableicitemlocations.sQtyOnHand
		    		+ ", " + SMTableicitemlocations.sTotalCost
		    		+ ") VALUES ("
		    		+ "'" + line.sItemNumber() + "'"
		    		+ ", '" + line.sTargetLocation() + "'"
		    		+ ", " + clsManageBigDecimals.BigDecimalToFormattedString("#######0.0000", bdTotalQty)
		    		+ ", " + clsManageBigDecimals.BigDecimalTo2DecimalSQLFormat(bdTotalCost)
		    		+ ") ON DUPLICATE KEY UPDATE "
		    		+ SMTableicitemlocations.sQtyOnHand + " = " + clsManageBigDecimals.BigDecimalToFormattedString("#######0.0000", bdTotalQty)
		    		+ ", " + SMTableicitemlocations.sTotalCost + " = " + clsManageBigDecimals.BigDecimalTo2DecimalSQLFormat(bdTotalCost)
		    	;
		    	try{
		    		Statement stmt = conn.createStatement();
		    		stmt.executeUpdate(SQL);
		    	}catch(SQLException e){
		    		addErrorMessage("SQL Error inserting (target) item location for entry #: " + sEntryNumber 
		    				+ " - " + e.getMessage());
		    		return false;
		    	}
	    	}
    	}
    	
    	//Now that costs have been updated, save the entry:
    	if (!entry.save_without_data_transaction(conn, sUserID)){
    		String sErr = "";
    		for (int i = 0; i < entry.getErrorMessage().size(); i++){
    			sErr = entry.getErrorMessage().get(i) + "\n";
    		}
    		addErrorMessage("Could not save entry - " + sErr);
    		return false;
    	}

    	return true;
    }
    private boolean writeTransactionDetails(Connection conn, long lTransactionID, String sUserFullName){
    	
    	for (int i = 0; i < m_arrTransactionDetails.size(); i++){
    		m_arrTransactionDetails.get(i).setM_ltransactionid(lTransactionID);
    		m_arrTransactionDetails.get(i).setM_ldetailnumber(i + 1);
    		if (!m_arrTransactionDetails.get(i).save_without_data_transaction(conn, sUserFullName)){
    			addErrorMessage("Could not write transaction detail for cost bucket ID: " 
    				+ m_arrTransactionDetails.get(i).getM_lcostbucketid()
    				+ " - " + m_arrTransactionDetails.get(i).getErrorMessages());
    			return false;
    		}
    	}
    	
		return true;
	}
    private boolean processAdjustmentLine(
    		ICEntryLine ln, 
    		java.sql.Date datEntry, 
    		String sUserFullName, 
    		String sUserID,
    		Connection conn){
    	
    	String SQL = "";
    	BigDecimal bdCost = new BigDecimal(0);
    	/*
    	 	If the user has chosen to adjust a NEW bucket, we can create that bucket and update it.
    	 
    	 	IF they choose to update an EXISTING bucket, we have to validate that the bucket can drop below
    	 	zero, then, if it can, update that bucket.
    	 */
		//If we are using AVERAGE COSTING, then we get the bucket ID for the one cost bucket now:    	
    	if (Integer.valueOf(Long.toString(lCostingMethod)) == SMTableicoptions.COSTING_METHOD_AVERAGECOST) {

			SQL = " SELECT " + SMTableiccosts.iId + " FROM "
					+ SMTableiccosts.TableName + " WHERE (" + "("
					+ SMTableiccosts.sItemNumber + " = '" + ln.sItemNumber()
					+ "')" + " AND (" + SMTableiccosts.sLocation + " = '"
					+ ln.sLocation() + "')" + ")";
			try {
				ResultSet rsCosts = clsDatabaseFunctions.openResultSet(SQL, conn);
				if (rsCosts.next()) {
					ln.sCostBucketID(Long.toString(rsCosts
							.getLong(SMTableiccosts.iId)));
				} else {
					ln.sCostBucketID("-1");
				}
				rsCosts.close();
			} catch (SQLException e) {
    			addErrorMessage("Error reading avg. cost bucket for entry #: " + ln.sEntryNumber() 
	    				+ ", line number #:" + ln.sLineNumber() + " - " + e.getMessage());
    			return false;
			}
		}
    	
		//First, if the user chose to use a NEW cost bucket, we have to create that bucket:
    	if (ln.sCostBucketID().compareToIgnoreCase("-1") == 0){
    		if (!bAllowNegativeQtys){
    			BigDecimal bdQtyAdjusted = new BigDecimal(ln.sQtySTDFormat().replace(",", ""));
    			if (bdQtyAdjusted.compareTo(BigDecimal.ZERO) < 0){
	    			addErrorMessage(
	    					"<br>Posting adjustment entry " + ln.sEntryNumber() + ", line " + ln.sLineNumber()
	    						+ " would place a negative quantity in a new cost bucket."
	    			);
	    			return false;
    			}
    		}
    		//Create a new cost bucket:
    		bdCost = new BigDecimal(
    			ln.sCostSTDFormat().replace(",", "")).setScale(2, BigDecimal.ROUND_HALF_UP);
    		SQL = "INSERT INTO " + SMTableiccosts.TableName + " ("
    			+ SMTableiccosts.bdCost
    			+ ", " + SMTableiccosts.bdQty
    			+ ", " + SMTableiccosts.bdCostShipped
    			+ ", " + SMTableiccosts.bdQtyShipped
    			+ ", " + SMTableiccosts.datCreationDate
    			+ ", " + SMTableiccosts.iSource
    			+ ", " + SMTableiccosts.sItemNumber
    			+ ", " + SMTableiccosts.sLocation
    			+ ", " + SMTableiccosts.sPONumber
    			+ ", " + SMTableiccosts.sReceiptNumber
    			+ ", " + SMTableiccosts.sRemark
    			
    			+ ") VALUES ("
    			+ clsManageBigDecimals.BigDecimalTo2DecimalSQLFormat(bdCost)
    			+ ", " + ln.sQtySTDFormat().replace(",", "")
    			+ ", 0.00" //When creating a new cost bucket, the shipped values should always be zero
    			+ ", 0.0000"
    			+ ", NOW()"
    			+ ", 1"								//Source '1' is a system created bucket
    			+ ", '" + ln.sItemNumber() + "'"
    			+ ", '" + ln.sLocation() + "'"
    			+ ", ''"
    			+ ", ''"
    			+ ", 'Created from adjustment batch " + Long.toString(m_lbatchnumber) 
    				+ ", entry " + ln.sEntryNumber() + ", line " + ln.sLineNumber() + "'"
    			
    			+ ")"
    			;
	    	if(bLogDebug){
	        	log.writeEntry(
	        			sUserID, 
	        			SMLogEntry.LOG_OPERATION_ICBATCHPOST, 
	        			"User " + sUserFullName + " in processAdjustmentLine"
	        			+ "Batch #:" + sBatchNumber() + ", EntryNumber #:" + ln.sEntryNumber()
	        			+ ", LineNumber #: " + ln.sLineNumber(),
	        			"INSERT SQL = " + SQL,
	        			"[1376509386]"
	        	);
	    	}	
    		try{
    			clsDatabaseFunctions.executeSQL(SQL, conn);
    		}catch (SQLException e){
    			addErrorMessage("Error creating adjustment bucket for entry #: " + ln.sEntryNumber() 
	    				+ ", line number #:" + ln.sLineNumber() + " - " + e.getMessage());
    			return false;
    		}

    		long lCostBucketID = -1L;
    		try{
    			lCostBucketID = clsDatabaseFunctions.getLastInsertID(conn);
    		}catch(SQLException x){
				addErrorMessage("Error reading new bucket ID for entry #: "
						+ ln.sEntryNumber()
						+ ", line number #:"
						+ ln.sLineNumber()
						+ " - " + x.getMessage());
				return false;
    		}
    		ln.sCostBucketID(Long.toString(lCostBucketID));
    	}else{
    		//If the user chose to adjust an EXISTING bucket, update it now:

    		//All we care about for preventing negatives, is that the quantity is not caused to 
    		//go from positive or zero to negative.  If the qty in the bucket is already negative
    		//then we can allow it to increase, even if it winds up still being negative.  But
    		//we can't allow it to go 'MORE' negative.
    		if (!bAllowNegativeQtys){
	    		BigDecimal bdQtyAvailable = new BigDecimal(0);
	    		try{
	    			bdQtyAvailable = getQtyAvailableInBucket(ln.sCostBucketID(), conn);
	    		}catch(SQLException e){
	    			addErrorMessage(
	    					"<br>Could not read available qty for cost bucket: " + ln.sCostBucketID());
	    			return false;
	    		}
	    		BigDecimal bdQtyAdjusted = new BigDecimal(ln.sQtySTDFormat());
	    		if(bdQtyAvailable.compareTo(BigDecimal.ZERO) >= 0){
	    			if (bdQtyAvailable.add(bdQtyAdjusted).compareTo(BigDecimal.ZERO) < 0){
		    			addErrorMessage(
		    					"<br>Posting adjustment entry " + ln.sEntryNumber() + ", line " + ln.sLineNumber()
		    						+ " would cause quantity in cost bucket " + ln.sCostBucketID()
		    						+ " to drop below zero."
		    			);
		    			return false;
	    			}
	    		}
	    		
	    		if(bdQtyAvailable.compareTo(BigDecimal.ZERO) < 0){
	    			if (bdQtyAdjusted.compareTo(BigDecimal.ZERO) < 0){
		    			addErrorMessage(
		    					"<br>Posting adjustment entry " + ln.sEntryNumber() + ", line " + ln.sLineNumber()
		    						+ " would cause current negative quantity in cost bucket " 
		    						+ ln.sCostBucketID() + " to become MORE negative."
		    			);
		    			return false;
	    			}
	    		}
    		}
    		
    		//Update the bucket here:
    		bdCost = new BigDecimal(
    				ln.sCostSTDFormat().replace(",", "")).setScale(2, BigDecimal.ROUND_HALF_UP);
    		SQL = "UPDATE " + SMTableiccosts.TableName + " SET "
    			+ SMTableiccosts.bdQty + " = " + SMTableiccosts.bdQty + " + " 
    				+ "(" + ln.sQtySTDFormat().replace(",", "") + ")"
    			+ ", " + SMTableiccosts.bdCost + " = " + SMTableiccosts.bdCost + " + " 
    				+ "(" + clsManageBigDecimals.BigDecimalTo2DecimalSQLFormat(bdCost) + ")"
    			+ ", " + SMTableiccosts.bdCostShipped + " = " + SMTableiccosts.bdCostShipped + " - " 
    				+ "(" + clsManageBigDecimals.BigDecimalTo2DecimalSQLFormat(bdCost) + ")"
    			+ ", " + SMTableiccosts.bdQtyShipped + " = " + SMTableiccosts.bdQtyShipped + " - " 
    				+ "(" + ln.sQtySTDFormat().replace(",", "") + ")"
    			+ " WHERE ("
    				+ SMTableiccosts.iId + " = " + ln.sCostBucketID()
    			+ ")"
    			;
    		try{
    			clsDatabaseFunctions.executeSQL(SQL, conn);
    		}catch (SQLException e){
    			addErrorMessage("Error updating adjustment bucket for entry #: " + ln.sEntryNumber() 
	    				+ ", line number #:" + ln.sLineNumber() + " - " + e.getMessage());
    			return false;
    		}
    		if(bLogDebug){
	        	log.writeEntry(
	        		sUserID, 
        			SMLogEntry.LOG_OPERATION_ICBATCHPOST, 
        			"User " + sUserFullName + " in processAdjustmentLine"
        			+ "Batch #:" + sBatchNumber() + ", EntryNumber #:" + ln.sEntryNumber()
        			+ ", LineNumber #: " + ln.sLineNumber(),
        			"UPDATE SQL = " + SQL,
        			"[1376509387]"
	        	);
    		}
    	}
    	
    	if (!addTransactionDetail(
    		conn, 
    		Long.parseLong(ln.sCostBucketID()), 
    		bdCost, 
    		new BigDecimal(ln.sQtySTDFormat().replace(",", ""))
    		)
    	){
    		return false;
    	}
    	
    	//Do not need to update the statistics table for adjustments:
    	
    	return true;
    }
    private boolean processShipmentLine(
    		ICEntryLine ln, 
    		java.sql.Date datEntry,
    		String sUserFullName,
    		String sUserID,
    		Connection conn){
    	
		if (!bAllowNegativeQtys){
    		BigDecimal bdQtyAvailable = new BigDecimal(0);
    		try{
    			bdQtyAvailable = getQtyAvailableForItemAndLocation(ln.sItemNumber(), ln.sLocation(), conn);
    		}catch(SQLException e){
    			addErrorMessage(
    					"<br>Could not read available qty for item " + ln.sItemNumber()
    					+ ", location " + ln.sLocation() + "."
    			);
    			return false;
    		}
    		BigDecimal bdQtyShipped = new BigDecimal(ln.sQtySTDFormat());
    		//Because the shipment qty is negative, we have to negate it to work with it here:
    		bdQtyShipped = bdQtyShipped.negate();
    		if (bdQtyAvailable.compareTo(bdQtyShipped) < 0){
    			addErrorMessage(
    					"<br>Posting shipment entry " + ln.sEntryNumber() + ", line " + ln.sLineNumber()
    						+ " would cause quantity in cost bucket(s) " + ln.sCostBucketID()
    						+ " to drop below zero."
    			);
    			return false;
    		}
		}
		
		//Start relieving buckets until the shipment is satisfied:
    	BigDecimal bdQtyRequestedRemaining = new BigDecimal(ln.sQtySTDFormat().replace(",", ""));
    	//Because the qty is negative, we have to negate it to work with it here:
    	bdQtyRequestedRemaining = bdQtyRequestedRemaining.negate();
    	//While the requested quantity is not satisfied, keep drawing from cost buckets
    	while (bdQtyRequestedRemaining.compareTo(BigDecimal.ZERO) != 0){
    		try{
    			//This method updates the costs in the Entry Line object each time it's called:
    			bdQtyRequestedRemaining = bdQtyRequestedRemaining.subtract(
    				drawFromCostBucket(sUserFullName, sUserID, ln, bdQtyRequestedRemaining, conn));
    		}catch(SQLException e){
    			addErrorMessage(
    					"<br>Error in entry " + ln.sEntryNumber() + ", line " + ln.sLineNumber()
    						+ ": could not update cost buckets."
    			);
    			return false;
    		}
    	}
		
    	//Update shipment statistics:
    	//Cost is NEGATIVE at this point:
    	BigDecimal bdCost 
    		= new BigDecimal(ln.sCostSTDFormat().replace(",", "")).setScale(2, BigDecimal.ROUND_HALF_UP);
    	String SQL = "INSERT INTO "
    		+ SMTableicitemstatistics.TableName
    		+ " ("
    		+ SMTableicitemstatistics.bdAmountSold
    		+ ", " + SMTableicitemstatistics.bdCostOfItemsSold
    		+ ", " + SMTableicitemstatistics.bdQtySold
    		+ ", " + SMTableicitemstatistics.lCountSold
    		+ ", " + SMTableicitemstatistics.lMonth
    		+ ", " + SMTableicitemstatistics.lYear
    		+ ", " + SMTableicitemstatistics.sItemNumber
    		+ ", " + SMTableicitemstatistics.sLocation

    		+ ") VALUES ("
    		
    		+ ln.sPriceSTDFormat().replace(",", "").replace("-", "")
    		+ ", " + clsManageBigDecimals.BigDecimalTo2DecimalSQLFormat(bdCost).replace("-", "")
    		+ ", " + ln.sQtySTDFormat().replace(",", "").replace("-", "")
    		+ ", 1"
    		+ ", " + clsDateAndTimeConversions.utilDateToString(datEntry, "M")
    		+ ", " + clsDateAndTimeConversions.utilDateToString(datEntry, "yyyy")
    		+ ", '" + ln.sItemNumber() + "'"
    		+ ", '" + ln.sLocation() + "'"
    		
    		+ ")"
    		
    		+ " ON DUPLICATE KEY UPDATE"
    		+ " " + SMTableicitemstatistics.bdAmountSold + " = " + SMTableicitemstatistics.bdAmountSold
    			+ " + " + ln.sPriceSTDFormat().replace(",", "").replace("-", "")
    		+ ", " + SMTableicitemstatistics.bdCostOfItemsSold + " = " + SMTableicitemstatistics.bdCostOfItemsSold
    			+ " + " + clsManageBigDecimals.BigDecimalTo2DecimalSQLFormat(bdCost).replace("-", "")
    		+ ", " + SMTableicitemstatistics.bdQtySold + " = " + SMTableicitemstatistics.bdQtySold
    			+ " + " + ln.sQtySTDFormat().replace(",", "").replace("-", "")
    		+ ", " + SMTableicitemstatistics.lCountSold + " = " + SMTableicitemstatistics.lCountSold
    			+ " + 1"
    	;
    	
    	try{
    		clsDatabaseFunctions.executeSQL(SQL, conn);
    	}catch (SQLException e){
    		addErrorMessage("<BR>Error [1550674322] updating statistics for item " + ln.sItemNumber() + ", location"
    				+ ln.sLocation() + " - entry number: " +  ln.sEntryNumber() + ", line number " + ln.sLineNumber() + " - " +  e.getMessage() + "."
    		);
    	}
    	if(bLogDebug){
	    	log.writeEntry(
	    		sUserID, 
    			SMLogEntry.LOG_OPERATION_ICBATCHPOST, 
    			"User " + sUserFullName + " in processShipmentLine"
    			+ "Batch #:" + sBatchNumber() + ", EntryNumber #:" + ln.sEntryNumber()
    			+ ", LineNumber #: " + ln.sLineNumber(),
    			"INSERT SQL = " + SQL,
    			"[1376509391]"
	    	);
    	}
    	return true;
    }
    private boolean processShipmentReturnLine(
    		ICEntryLine ln, 
    		java.sql.Date datEntry,
    		String sUserFullName,
    		String sUserID,
    		Connection conn){
    	
    	String SQL = "";
    	
    	//These are positive on a return:
    	BigDecimal bdQty = new BigDecimal(ln.sQtySTDFormat().replace(",", "")).setScale(4, BigDecimal.ROUND_HALF_UP);
    	BigDecimal bdCost = new BigDecimal(ln.sCostSTDFormat().replace(",", "")).setScale(2, BigDecimal.ROUND_HALF_UP);
    	BigDecimal bdPrice = new BigDecimal(ln.sPriceSTDFormat().replace(",", "")).setScale(2, BigDecimal.ROUND_HALF_UP);
    	long lCostBucketID = -1;
    	
    	//This SQL statement will be used if it's NOT avg costing, OR if it IS avg. costing, but there's
    	//no cost bucket yet:
		SQL = "INSERT INTO " + SMTableiccosts.TableName + "("
		+ SMTableiccosts.bdCost
		+ ", " + SMTableiccosts.bdQty
		+ ", " + SMTableiccosts.bdCostShipped
		+ ", " + SMTableiccosts.bdQtyShipped
		+ ", " + SMTableiccosts.datCreationDate
		+ ", " + SMTableiccosts.iSource
		+ ", " + SMTableiccosts.sItemNumber
		+ ", " + SMTableiccosts.sLocation
		+ ", " + SMTableiccosts.sPONumber
		+ ", " + SMTableiccosts.sReceiptNumber
		+ ", " + SMTableiccosts.sRemark
		
		+ ") VALUES ("
		+ clsManageBigDecimals.BigDecimalTo2DecimalSQLFormat(bdCost)
		+ ", " + clsManageBigDecimals.BigDecimalTo2DecimalSQLFormat(bdQty)
		+ ", 0.00" //When creating a new bucket, the 'shipped' values should always be zero
		+ ", 0.0000"
		+ ", NOW()"
		+ ", 1"								//Source '1' is a system created bucket
		+ ", '" + ln.sItemNumber() + "'"
		+ ", '" + ln.sLocation() + "'"
		+ ", ''"
		+ ", ''"
		+ ", 'Created from shipment return in batch " + Long.toString(m_lbatchnumber) 
			+ ", entry " + ln.sEntryNumber() + ", line " + ln.sLineNumber() + "'"
		+ ")"
		;
    	
    	//If it's average costing that's being used:
    	if (lCostingMethod == SMTableicoptions.COSTING_METHOD_AVERAGECOST){
    		//We only work from one bucket:
    		try {
    			//AND if there already IS a bucket:
    			lCostBucketID
    				= checkForAvgCostingItemAndLocationBucket(ln.sItemNumber(), ln.sLocation(), conn);
				if (lCostBucketID > -1L){
					//Then Update the bucket:
					SQL = "UPDATE " + SMTableiccosts.TableName + " SET"
						+ " " + SMTableiccosts.bdCost + " = " + SMTableiccosts.bdCost + " + "
							+ clsManageBigDecimals.BigDecimalTo2DecimalSQLFormat(bdCost)
						+ ", " + SMTableiccosts.bdQty + " = " + SMTableiccosts.bdQty + " + " 
							+ clsManageBigDecimals.BigDecimalTo2DecimalSQLFormat(bdQty)
						+ ", " + SMTableiccosts.bdCostShipped + " = " + SMTableiccosts.bdCostShipped + " - "
							+ clsManageBigDecimals.BigDecimalTo2DecimalSQLFormat(bdCost)
						+ ", " + SMTableiccosts.bdQtyShipped + " = " + SMTableiccosts.bdQtyShipped + " - " 
							+ clsManageBigDecimals.BigDecimalTo2DecimalSQLFormat(bdQty)
						+ " WHERE ("
							+ "(" + SMTableiccosts.iId + " = '" + lCostBucketID + "')"
						+ ")"
					;
				}
			} catch (SQLException e) {
    			addErrorMessage(
    					"<br>Error in shipment return entry " + ln.sEntryNumber() + ", line " + ln.sLineNumber()
    						+ ": checking for existing cost bucket."
    			);
    			return false;			
    		}
    	}
    	//Now the SQL statement is set - execute it:
		try{
			clsDatabaseFunctions.executeSQL(SQL, conn);
		}catch (SQLException e){
			addErrorMessage("Error creating returned shipment bucket for entry #: " + ln.sEntryNumber() 
    				+ ", line number #:" + ln.sLineNumber() + " - " + e.getMessage());
    			return false;
    		}
		
		//If a new bucket was inserted, get the bucket ID for it:
		if (lCostBucketID == -1L){
			try{
				lCostBucketID = clsDatabaseFunctions.getLastInsertID(conn);
			}catch(SQLException x){
				addErrorMessage("Error getting new cost bucket ID for entry #: " + ln.sEntryNumber() 
	    				+ ", line number #:" + ln.sLineNumber() + " - " + x.getMessage());
	    			return false;
			}
		}
		
		if (bLogDebug){
			log.writeEntry(
				sUserID, 
    			SMLogEntry.LOG_OPERATION_ICBATCHPOST, 
    			"User " + sUserFullName + " in processShipmentReturnLine"
    			+ "Batch #:" + sBatchNumber() + ", EntryNumber #:" + ln.sEntryNumber()
    			+ ", LineNumber #: " + ln.sLineNumber(),
    			"INSERT SQL = " + SQL,
    			"[1376509392]"
			);
		}
		
    	//Update shipment return statistics:
    	SQL = "INSERT INTO "
    		+ SMTableicitemstatistics.TableName
    		+ "("
    		+ SMTableicitemstatistics.bdAmountReturned
    		+ ", " + SMTableicitemstatistics.bdCostOfItemsReturned
    		+ ", " + SMTableicitemstatistics.bdQtyReturned
    		+ ", " + SMTableicitemstatistics.lCountReturned
    		+ ", " + SMTableicitemstatistics.lMonth
    		+ ", " + SMTableicitemstatistics.lYear
    		+ ", " + SMTableicitemstatistics.sItemNumber
    		+ ", " + SMTableicitemstatistics.sLocation

    		+ ") VALUES ("
    		
    		+ clsManageBigDecimals.BigDecimalTo2DecimalSQLFormat(bdPrice)
    		+ ", " + clsManageBigDecimals.BigDecimalTo2DecimalSQLFormat(bdCost)
    		+ ", " + clsManageBigDecimals.BigDecimalTo2DecimalSQLFormat(bdQty)
    		+ ", 1"
    		+ ", " + clsDateAndTimeConversions.utilDateToString(datEntry, "M")
    		+ ", " + clsDateAndTimeConversions.utilDateToString(datEntry, "yyyy")
    		+ ", '" + ln.sItemNumber() + "'"
    		+ ", '" + ln.sLocation() + "'"
    		+ ")"
    		
    		+ " ON DUPLICATE KEY UPDATE"
    		+ " " + SMTableicitemstatistics.bdAmountReturned + " = " + SMTableicitemstatistics.bdAmountReturned
    			+ " + " + clsManageBigDecimals.BigDecimalTo2DecimalSQLFormat(bdPrice)
    		+ ", " + SMTableicitemstatistics.bdCostOfItemsReturned + " = " + SMTableicitemstatistics.bdCostOfItemsReturned
    			+ " + " + clsManageBigDecimals.BigDecimalTo2DecimalSQLFormat(bdCost)
    		+ ", " + SMTableicitemstatistics.bdQtyReturned + " = " + SMTableicitemstatistics.bdQtyReturned
    			+ " + " + clsManageBigDecimals.BigDecimalTo2DecimalSQLFormat(bdQty)
    		+ ", " + SMTableicitemstatistics.lCountReturned + " = " + SMTableicitemstatistics.lCountReturned
    			+ " + 1"
    	;
    	
    	try{
    		clsDatabaseFunctions.executeSQL(SQL, conn);
    	}catch (SQLException e){
    		addErrorMessage("<BR>Error [1550674323] updating statistics for item " + ln.sItemNumber() + ", location"
    				+ ln.sLocation() + " - entry number: " +  ln.sEntryNumber() + ", line number " + ln.sLineNumber() + " - " +  e.getMessage() + "."
    		);
    	}
    	
    	if (!addTransactionDetail(conn, lCostBucketID, bdCost, bdQty)){
    		return false;
    	}
    	
    	return true;
    }
    private boolean processPhysicalCountDecreaseLine(
    		ICEntryLine ln, 
    		java.sql.Date datEntry,
    		String sUserFullName,
    		String sUserID,
    		Connection conn){
    	
		if (!bAllowNegativeQtys){
    		BigDecimal bdQtyAvailable = new BigDecimal(0);
    		try{
    			bdQtyAvailable = getQtyAvailableForItemAndLocation(ln.sItemNumber(), ln.sLocation(), conn);
    		}catch(SQLException e){
    			addErrorMessage(
    					"<br>Could not read available qty for item " + ln.sItemNumber()
    					+ ", location " + ln.sLocation() + "."
    			);
    			return false;
    		}
    		BigDecimal bdQtyChanged = new BigDecimal(ln.sQtySTDFormat());
    		//Because the count qty is negative, we have to negate it to work with it here:
    		bdQtyChanged = bdQtyChanged.negate();
    		if (bdQtyAvailable.compareTo(bdQtyChanged) < 0){
    			addErrorMessage(
    					"<br>Posting physical count entry " + ln.sEntryNumber() + ", line " + ln.sLineNumber()
    						+ " would cause quantity in cost bucket(s) " + ln.sCostBucketID()
    						+ " to drop below zero."
    			);
    			return false;
    		}
		}
		
		//Start relieving buckets until the qty changed is satisfied:
    	BigDecimal bdQtyRequestedRemaining = new BigDecimal(ln.sQtySTDFormat());
    	//Because the qty is negative, we have to negate it to work with it here:
    	bdQtyRequestedRemaining = bdQtyRequestedRemaining.negate();
    	//While the requested quantity is not satisfied, keep drawing from cost buckets
    	while (bdQtyRequestedRemaining.compareTo(BigDecimal.ZERO) != 0){
    		try{
    			//This method updates the costs in the Entry Line object each time it's called:
    			bdQtyRequestedRemaining = bdQtyRequestedRemaining.subtract(
    				drawFromCostBucket(sUserFullName, sUserID, ln, bdQtyRequestedRemaining, conn));
    		}catch(SQLException e){
    			addErrorMessage(
    					"<br>Error in entry " + ln.sEntryNumber() + ", line " + ln.sLineNumber()
    						+ ": could not update cost buckets."
    			);
    			return false;
    		}
    	}
    	//No statistics for physical counts:

    	return true;
    }
    private boolean processPhysicalCountIncreaseLine(
    		ICEntryLine ln, 
    		java.sql.Date datEntry,
    		String sUserFullName,
    		String sUserID,
    		Connection conn){
    	
    	//These are positive on an increase:
    	BigDecimal bdQty = new BigDecimal(ln.sQtySTDFormat().replace(",", ""));
    	BigDecimal bdMostRecentCost = new BigDecimal(0);
    	long lCostBucketID = -1;
    	
    	try{
    		bdMostRecentCost = getItemMostRecentCost(ln.sItemNumber(), conn);
    	}catch (SQLException e){
    		addErrorMessage("Error getting most recent cost for '" + ln.sItemNumber() 
        			+ e.getMessage());
    		return false;
    	}
    	BigDecimal bdCost = bdQty.multiply(bdMostRecentCost).setScale(2, BigDecimal.ROUND_HALF_UP);

    	//This statement will be used to insert a new bucket - either in the case that it's NOT
    	//Average costing, OR in the case that it IS average costing, but there's no bucket yet:
		String SQL = "INSERT INTO " + SMTableiccosts.TableName + "("
			+ SMTableiccosts.bdCost
			+ ", " + SMTableiccosts.bdQty
			+ ", " + SMTableiccosts.bdQtyShipped
			+ ", " + SMTableiccosts.bdCostShipped
			+ ", " + SMTableiccosts.datCreationDate
			+ ", " + SMTableiccosts.iSource
			+ ", " + SMTableiccosts.sItemNumber
			+ ", " + SMTableiccosts.sLocation
			+ ", " + SMTableiccosts.sPONumber
			+ ", " + SMTableiccosts.sReceiptNumber
			+ ", " + SMTableiccosts.sRemark
			
			+ ") VALUES ("
			+ clsManageBigDecimals.BigDecimalTo2DecimalSQLFormat(bdCost)
			+ ", " + clsManageBigDecimals.BigDecimalTo2DecimalSQLFormat(bdQty)
			+ ", 0.0000"
			+ ", 0.00" //When creating a new bucket, the 'shipped' values are always zero
			+ ", NOW()"
			+ ", 1"								//Source '1' is a system created bucket
			+ ", '" + ln.sItemNumber() + "'"
			+ ", '" + ln.sLocation() + "'"
			+ ", ''"
			+ ", ''"
			+ ", 'Created from shipment return in batch " + Long.toString(m_lbatchnumber) 
				+ ", entry " + ln.sEntryNumber() + ", line " + ln.sLineNumber() + "'"
			+ ")"
		;
    	
    	//If it's average costing that's being used, AND if we already have a bucket:
    	if (lCostingMethod == SMTableicoptions.COSTING_METHOD_AVERAGECOST){
    		//We only work from one bucket:
    		try {
    			//AND if there already IS a bucket:
    			lCostBucketID
				= checkForAvgCostingItemAndLocationBucket(ln.sItemNumber(), ln.sLocation(), conn);
				if (lCostBucketID > -1L){
					//Then Update the bucket:
					SQL = "UPDATE " + SMTableiccosts.TableName + " SET"
						+ " " + SMTableiccosts.bdCost + " = " + SMTableiccosts.bdCost + " + "
							+ clsManageBigDecimals.BigDecimalTo2DecimalSQLFormat(bdCost)
						+ ", " + SMTableiccosts.bdQty + " = " + SMTableiccosts.bdQty + " + " 
							+ clsManageBigDecimals.BigDecimalTo2DecimalSQLFormat(bdQty)
						+ ", " + SMTableiccosts.bdQtyShipped + " = " + SMTableiccosts.bdQtyShipped + " - " 
							+ clsManageBigDecimals.BigDecimalTo2DecimalSQLFormat(bdQty)
						+ ", " + SMTableiccosts.bdCostShipped + " = " + SMTableiccosts.bdCostShipped + " - " 
							+ clsManageBigDecimals.BigDecimalTo2DecimalSQLFormat(bdCost)
						+ " WHERE ("
							+ "(" + SMTableiccosts.iId + " = '" + lCostBucketID + "')"
						+ ")"
					;
				}
			} catch (SQLException e) {
    			addErrorMessage(
    					"<br>Error in physical count increase entry " + ln.sEntryNumber() 
    					+ ", line " + ln.sLineNumber()
    						+ ": checking for existing cost bucket."
    			);
    			return false;			
    		}
    	}
		try{
			clsDatabaseFunctions.executeSQL(SQL, conn);
		}catch (SQLException e){
			addErrorMessage("Error creating physical count increase bucket for entry #: " + ln.sEntryNumber() 
    				+ ", line number #:" + ln.sLineNumber() + " - " + e.getMessage());
    			return false;
    		}

		//If we added a new cost bucket record, get that ID:
		if (lCostBucketID == -1L){
			try {
				lCostBucketID = clsDatabaseFunctions.getLastInsertID(conn);
			} catch (SQLException e) {
				addErrorMessage("Error getting new cost bucekt ID for entry #: " + ln.sEntryNumber() 
	    				+ ", line number #:" + ln.sLineNumber() + " - " + e.getMessage());
	    			return false;
			}
		}
		
		//Add a transaction detail here:
		if (!addTransactionDetail(conn, lCostBucketID, bdCost, bdQty)){
			return false;
		}

		//Update the line object with the increased cost:
		ln.setCostString(clsManageBigDecimals.BigDecimalTo2DecimalSQLFormat(bdCost));
		
		if (bLogDebug){
			log.writeEntry(
				sUserID, 
    			SMLogEntry.LOG_OPERATION_ICBATCHPOST, 
    			"User " + sUserFullName + " in processPhysicalCountIncreaseLine"
    			+ "Batch #:" + sBatchNumber() + ", EntryNumber #:" + ln.sEntryNumber()
    			+ ", LineNumber #: " + ln.sLineNumber(),
    			"INSERT/UPDATE SQL = " + SQL,
    			"[1376509389]"
			);
		}
    	//No statistics for physical counts

		return true;
    }
    private boolean processTransferLine(
    		ICEntryLine ln, 
    		java.sql.Date datEntry,
    		String sUserFullName,
    		String sUserID,
    		Connection conn){
    	
    	 //A transfer has to work like this:
    	 //1) If the total quantity being transferred FROM a location would leave that location
    	 //in a negative state, we have to first trap that.  If negatives are not allowed,
    	 //we have to return and advise the user.
		long lCostBucketID = -1;
    	
    	if (!bAllowNegativeQtys){
    		BigDecimal bdQtyAvailable = new BigDecimal(0);
    		try{
    			bdQtyAvailable = getQtyAvailableForItemAndLocation(ln.sItemNumber(), ln.sLocation(), conn).setScale(4, BigDecimal.ROUND_HALF_UP);
    		}catch(SQLException e){
    			addErrorMessage(
    					"<br>Could not read available qty for item " + ln.sItemNumber()
    					+ ", location " + ln.sLocation() + "."
    			);
    			return false;
    		}
    		BigDecimal bdQtyShipped = new BigDecimal(ln.sQtySTDFormat()).setScale(4, BigDecimal.ROUND_HALF_UP);
    		if (bdQtyAvailable.compareTo(bdQtyShipped) < 0){
    			addErrorMessage(
    					"<br>Posting transfer entry " + ln.sEntryNumber() + ", line " + ln.sLineNumber()
    						+ " would cause the quantity in location " + ln.sLocation()
    						+ " to drop below zero."
    			);
    			return false;
    		}
		}    	 

		//Get all the buckets for this item and location:
		//Start relieving buckets until the transfer is satisfied:
    	BigDecimal bdQtyRequestedRemaining = new BigDecimal(ln.sQtySTDFormat().replace(",", ""));
    	//While the requested quantity is not satisfied, keep drawing from cost buckets
    	while (bdQtyRequestedRemaining.compareTo(BigDecimal.ZERO) != 0){
    		try{
    			//This method updates the costs in the Entry Line object each time it's called:
    			bdQtyRequestedRemaining = bdQtyRequestedRemaining.subtract(
    				drawFromCostBucket(sUserFullName, sUserID, ln, bdQtyRequestedRemaining, conn));
    		}catch(SQLException e){
    			addErrorMessage(
    					"<br>Error in transfer entry " + ln.sEntryNumber() + ", line " + ln.sLineNumber()
    						+ ": could not update cost buckets."
    			);
    			return false;
    		}
    	}
    	
    	//Now we have to increase a target bucket:
    	//The cost added to the target bucket is the cost of the line - normally, this is negative, because we are
    	//drawing it down:
    	BigDecimal bdTransferToCost 
    		= new BigDecimal(ln.sCostSTDFormat().replace(",", "")).setScale(2, BigDecimal.ROUND_HALF_UP);
    	//Reverse it for the transfer 'to':
    	bdTransferToCost = bdTransferToCost.negate();

    	//This is the default - to insert a new bucket.  But if it's average costing
    	//AND there's already a bucket, this SQL statement will get changed below:
		String SQL = "INSERT INTO " + SMTableiccosts.TableName + " ("
			+ SMTableiccosts.bdCost
			+ ", " + SMTableiccosts.bdCostShipped
			+ ", " + SMTableiccosts.bdQty
			+ ", " + SMTableiccosts.bdQtyShipped
			+ ", " + SMTableiccosts.datCreationDate
			+ ", " + SMTableiccosts.iSource
			+ ", " + SMTableiccosts.sItemNumber
			+ ", " + SMTableiccosts.sLocation
			+ ", " + SMTableiccosts.sPONumber
			+ ", " + SMTableiccosts.sReceiptNumber
			+ ", " + SMTableiccosts.sRemark
		
			+ ") VALUES ("
			+ clsManageBigDecimals.BigDecimalTo2DecimalSQLFormat(bdTransferToCost)
			+ ", 0.00" //When creating a new bucket, the 'shipped' values should always be zero
			+ ", " + ln.sQtySTDFormat().replace(",", "")
			+ ", 0.0000"
			+ ", NOW()"
			+ ", 1"								//Source '1' is a system created bucket
			+ ", '" + ln.sItemNumber() + "'"
			+ ", '" + ln.sTargetLocation() + "'"
			+ ", ''"
			+ ", ''"
			+ ", 'Created from transfer batch " + Long.toString(m_lbatchnumber) 
				+ ", entry " + ln.sEntryNumber() + ", line " + ln.sLineNumber() + "'"
			
			+ ")"
		;
    	
    	//If it's average costing that's being used:
    	if (lCostingMethod == SMTableicoptions.COSTING_METHOD_AVERAGECOST){
    		//We only work from one bucket:
			//AND if there already IS a bucket:
			try {
    			lCostBucketID
				= checkForAvgCostingItemAndLocationBucket(ln.sItemNumber(), ln.sTargetLocation(), conn);
				if (lCostBucketID > -1L){
					//Then Update the bucket:
					SQL = "UPDATE " + SMTableiccosts.TableName + " SET"
						+ " " + SMTableiccosts.bdCost + " = " + SMTableiccosts.bdCost + " + "
							+ clsManageBigDecimals.BigDecimalTo2DecimalSQLFormat(bdTransferToCost)
						+ ", " + SMTableiccosts.bdQty + " = " + SMTableiccosts.bdQty + " + " 
							+ ln.sQtySTDFormat().replace(",", "")
						+ ", " + SMTableiccosts.bdCostShipped + " = " + SMTableiccosts.bdCostShipped + " - "
							+ clsManageBigDecimals.BigDecimalTo2DecimalSQLFormat(bdTransferToCost)
						+ ", " + SMTableiccosts.bdQtyShipped + " = " + SMTableiccosts.bdQtyShipped + " - " 
							+ ln.sQtySTDFormat().replace(",", "")
						+ " WHERE ("
							+ "(" + SMTableiccosts.iId + " = " + lCostBucketID + ")"
						+ ")";
					//Don't run this SQL - let it run below
					//try{
					//	SMUtilities.executeSQL(SQL, conn);
					//	//'To' location is updated, return:
					//	return true;
					//} catch (SQLException e) {
					//	addErrorMessage(
					//		"<br>Error in transfer entry " + ln.sEntryNumber() + ", line " + ln.sLineNumber()
					//			+ ": updating target cost bucket.");
					//	return false;			
					//}
				}else{
					//There's no bucket, drop down into the following logic
				}
			} catch (SQLException e) {
				addErrorMessage(
						"<br>Error checking for item and location bucket on entry " + ln.sEntryNumber() 
						+ ", line " + ln.sLineNumber() + ": updating target cost bucket.");
				return false;			
			}
			if (bLogDebug){
				log.writeEntry(
					sUserID, 
	    			SMLogEntry.LOG_OPERATION_ICBATCHPOST, 
	    			"User " + sUserFullName + " in processTransferLine"
	    			+ "Batch #:" + sBatchNumber() + ", EntryNumber #:" + ln.sEntryNumber()
	    			+ ", LineNumber #: " + ln.sLineNumber(),
	    			"INSERT/UPDATE SQL for AVG costing = " + SQL,
	    			"[1376509393]"
				);
			}
    	}

		try{
			clsDatabaseFunctions.executeSQL(SQL, conn);
		}catch (SQLException e){
			addErrorMessage("Error creating/updating transfer bucket for entry #: " + ln.sEntryNumber() 
    				+ ", line number #:" + ln.sLineNumber() + " - " + e.getMessage());
    			return false;
    	}
		
		//Add a transaction detail here for the 'TO' location:
		if (lCostBucketID == -1){
			try {
				lCostBucketID = clsDatabaseFunctions.getLastInsertID(conn);
			} catch (SQLException e) {
    			return false;
			}
		}
		m_tdToLocation = new ICTransactionDetail();
		//Get the cost bucket information:
    	SQL = "SELECT * FROM " + SMTableiccosts.TableName
    		+ " WHERE ("
    			+ "(" + SMTableiccosts.iId + " = " + Long.toString(lCostBucketID) + ")"
    		+ ")"
    		;
    	
    	try {
			ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
			if (rs.next()) {
				m_tdToLocation.setM_ldetailnumber(1L);
				m_tdToLocation.setM_bdcostbucketcostbeforetrans(rs.getBigDecimal(SMTableiccosts.bdCost));
				m_tdToLocation.setM_bdcostbucketqtybeforetrans(rs.getBigDecimal(SMTableiccosts.bdQty));
				m_tdToLocation.setM_bdcostchange(bdTransferToCost);
				m_tdToLocation.setM_bdqtychange(new BigDecimal(ln.sQtySTDFormat().replace(",", "")));
				m_tdToLocation.setM_datetimecostbucketcreation(rs.getDate(SMTableiccosts.datCreationDate));
				m_tdToLocation.setM_lbucketreceiptlineid(rs.getLong(SMTableiccosts.lReceiptLineID));
				m_tdToLocation.setM_lcostbucketid(lCostBucketID);
				m_tdToLocation.setM_scostbucketlocation(rs.getString(SMTableiccosts.sLocation));
				m_tdToLocation.setM_scostbucketremark(rs.getString(SMTableiccosts.sRemark));
				rs.close();
			} else {
				rs.close();
				addErrorMessage("Could not read cost bucket ID "
						+ Long.toString(lCostBucketID)
						+ " - record not found.");
				return false;
			}
		} catch (SQLException e) {
			addErrorMessage("Could not read cost bucket ID "
				+ Long.toString(lCostBucketID)
				+ " - " + e.getMessage());
			return false;
		}
		
		if (bLogDebug){
			log.writeEntry(
				sUserID, 
    			SMLogEntry.LOG_OPERATION_ICBATCHPOST, 
    			"User " + sUserFullName + " in processTransferLine"
    			+ "Batch #:" + sBatchNumber() + ", EntryNumber #:" + ln.sEntryNumber()
    			+ ", LineNumber #: " + ln.sLineNumber(),
    			"INSERT/UPDATE SQL creating a new bucket = " + SQL,
    			"[1376509524]"
			);
		}
		
		//Don't need any statistics updated on a transfer
		
    	return true;
    }
    private boolean processReceiptLineForStockInventory(
   		ICEntryLine ln, 
   		java.sql.Date datEntry,
   		String sUserFullName,
   		String sUserID,
   		Connection conn){
    	
    	String SQL = "";
    	BigDecimal bdCost = new BigDecimal(0);
    	long lCostBucketID = -1;
    	BigDecimal bdQty 
    		= new BigDecimal(
    			ln.sQtySTDFormat().replace(",", "")).setScale(4, BigDecimal.ROUND_HALF_UP);
    	//If it's average costing that's being used:
    	if (lCostingMethod == SMTableicoptions.COSTING_METHOD_AVERAGECOST){
    		//We only work from one bucket:
    		try {
    			//AND if there already IS a bucket:
	    			lCostBucketID
					= checkForAvgCostingItemAndLocationBucket(ln.sItemNumber(), ln.sLocation(), conn);
					if (lCostBucketID > -1L){
						bdCost = new BigDecimal(
								ln.sCostSTDFormat().replace(",", "")).setScale(2, BigDecimal.ROUND_HALF_UP);
						//Then Update the bucket:
						SQL = "UPDATE " + SMTableiccosts.TableName + " SET"
							+ " " + SMTableiccosts.bdCost + " = " + SMTableiccosts.bdCost + " + "
								+ clsManageBigDecimals.BigDecimalTo2DecimalSQLFormat(bdCost)
							+ ", " + SMTableiccosts.bdQty + " = " + SMTableiccosts.bdQty + " + " 
								+ clsManageBigDecimals.BigDecimalToFormattedString("########0.0000", bdQty)
							+ ", " + SMTableiccosts.bdCostShipped + " = " + SMTableiccosts.bdCostShipped + " - "
								+ clsManageBigDecimals.BigDecimalTo2DecimalSQLFormat(bdCost)
							+ ", " + SMTableiccosts.bdQtyShipped + " = " + SMTableiccosts.bdQtyShipped + " - " 
								+ clsManageBigDecimals.BigDecimalToFormattedString("########0.0000", bdQty)
							+ " WHERE ("
								+ "(" + SMTableiccosts.iId + " = '" + lCostBucketID + "')"
							+ ")"
						;
					}
			} catch (SQLException e) {
    			addErrorMessage(
    					"<br>Error in receipt entry " + ln.sEntryNumber() + ", line " + ln.sLineNumber()
    						+ ": checking for existing cost bucket."
    			);
    			return false;			
    		}
    	}

    	//If we are using AVG cost, AND there already was a bucket, then the SQL statement was created - but if NOT, then just create it now:
    	if (SQL.compareToIgnoreCase("") == 0){
			//Create a new cost bucket:
    		bdCost = new BigDecimal(
    				ln.sCostSTDFormat().replace(",", "")).setScale(2, BigDecimal.ROUND_HALF_UP);
			SQL = "INSERT INTO " + SMTableiccosts.TableName + "("
				+ SMTableiccosts.bdCost
				+ ", " + SMTableiccosts.bdQty
				+ ", " + SMTableiccosts.bdCostShipped
				+ ", " + SMTableiccosts.bdQtyShipped
				+ ", " + SMTableiccosts.datCreationDate
				+ ", " + SMTableiccosts.iSource
				+ ", " + SMTableiccosts.sItemNumber
				+ ", " + SMTableiccosts.sLocation
				+ ", " + SMTableiccosts.sPONumber
				+ ", " + SMTableiccosts.sReceiptNumber
				+ ", " + SMTableiccosts.sRemark
				+ ", " + SMTableiccosts.lReceiptLineID
				
				+ ") VALUES ("
				+ clsManageBigDecimals.BigDecimalTo2DecimalSQLFormat(bdCost)
				+ ", " + clsManageBigDecimals.BigDecimalToFormattedString("########0.0000", bdQty)
				+ ", 0.00" //When creating a new bucket, the 'shipped' values should always be zero
				+ ", 0.0000"
				+ ", NOW()"
				+ ", 0"								//Source '0' is a manually created bucket
				+ ", '" + ln.sItemNumber() + "'"
				+ ", '" + ln.sLocation() + "'"
				//Need to get a PO number for this receipt?
				+ ", ''"
				+ ", '" + ln.sReceiptNum() + "'"
				+ ", 'Created from receipt in batch " + Long.toString(m_lbatchnumber) 
					+ ", entry " + ln.sEntryNumber() + ", line " + ln.sLineNumber() + "'"
				+ ", " + ln.sReceiptLineID()
				+ ")"
				;
    	}
		try{
			clsDatabaseFunctions.executeSQL(SQL, conn);
		}catch (SQLException e){
			//System.out.println("[1369075663] Error creating receipt bucket with SQL: " + SQL + "- " + e.getMessage());
			addErrorMessage("Error creating receipt bucket for entry #: " + ln.sEntryNumber() 
    				+ ", line number #:" + ln.sLineNumber() + " - " + e.getMessage());
    			return false;
    		}
		if (lCostBucketID == -1L){
			try {
				lCostBucketID = clsDatabaseFunctions.getLastInsertID(conn);
			} catch (SQLException e) {
				addErrorMessage("Error getting cost bucket ID for entry #: " + ln.sEntryNumber() 
	    				+ ", line number #:" + ln.sLineNumber() + " - " + e.getMessage());
	    			return false;
			}
		}
		
		if (!addTransactionDetail(conn, lCostBucketID, bdCost, bdQty)){
			return false;
		}
		if (bLogDebug){
			log.writeEntry(
				sUserID, 
    			SMLogEntry.LOG_OPERATION_ICBATCHPOST, 
    			"User " + sUserFullName + " in processReceiptLine"
    			+ "Batch #:" + sBatchNumber() + ", EntryNumber #:" + ln.sEntryNumber()
    			+ ", LineNumber #: " + ln.sLineNumber(),
    			"INSERT/UPDATE SQL = " + SQL,
    			"[1376509390]"
			);
		}
		
		//Update the most recent cost on the item here:
		BigDecimal bdUnitCost 
			= bdCost.divide(bdQty, BigDecimal.ROUND_HALF_UP).setScale(2, BigDecimal.ROUND_HALF_UP);
		
		SQL = "UPDATE"
			+ " " + SMTableicitems.TableName
			+ " SET " + SMTableicitems.bdmostrecentcost
			+ " = " + clsManageBigDecimals.BigDecimalToScaledFormattedString(
					SMTableicitems.bdmostrecentcostScale, bdUnitCost).replace(",", "")
			+ " WHERE ("
				+ SMTableicitems.sItemNumber + " = '" + ln.sItemNumber() + "'"
			+ ")"
		;
		try {
			if (!clsDatabaseFunctions.executeSQL(SQL, conn)){
				addErrorMessage("Error updating most recent cost for item on entry #: " + ln.sEntryNumber() 
						+ ", line number #:" + ln.sLineNumber());
				return false;
			}
		} catch (SQLException e) {
			addErrorMessage("Error updating most recent cost for item on entry #: " + ln.sEntryNumber() 
					+ ", line number #:" + ln.sLineNumber() + " - " + e.getMessage());
			return false;
		}
		
    	//No statistics on receipts:

    	return true;
    }
    private BigDecimal drawFromCostBucket(
    		String sUserFullName,
    		String sUserID,
    		ICEntryLine ln, 
    		BigDecimal bdQtyRequested, 
    		Connection conn) throws SQLException{
    	
    	if(bLogDebug){
        	log.writeEntry(
        			sUserID, 
        			SMLogEntry.LOG_OPERATION_ICBATCHPOST, 
        			"User " + sUserFullName + " in drawFromCostBucket",
        			"Batch #:" + sBatchNumber() + ", EntryNumber #:" + ln.sEntryNumber()
        			+ ", LineNumber #: " + ln.sLineNumber()
        			+ ", bdQtyRequested = " + bdQtyRequested
        			+ ", item = " + ln.sItemNumber()
        			+ ", location = " + ln.sLocation(),
        			"[1376509384]"
        	);
    	}
    	
    	String sSortOrder = "";
    	int iCostingMethod = Integer.valueOf(Long.toString(lCostingMethod)); 
    	switch (iCostingMethod){
    	//LIFO:
    	case 0: sSortOrder = "DESC"; break;
    	
    	//FIFO:
    	case 1: sSortOrder = "ASC"; break;
    		
    	//AVG COST:
    	case 2: sSortOrder = "ASC"; break;  //Doesn't really matter with only one bucket . . . . 
    		
    	default: sSortOrder = "ASC"; break;
    	}
    	
    	BigDecimal bdQtyDrawn = new BigDecimal(0);
    	BigDecimal bdCostDrawn = new BigDecimal(0);
    	BigDecimal bdQtyAvailableInBucket = new BigDecimal(0);
    	BigDecimal bdCostAvailableInBucket = new BigDecimal(0);
    	long lCostBucketID = 0;
    	
    	//If we are using the AVERAGE COST costing method, then we only need to get the single
    	//bucket for this item and location, so we don't need to qualify the statement with
    	// the 'Qty > 0' clause.  But if we are using any other costing method, 
    	// we have to only get buckets with more than zero in them, because we are not going to take items
    	//from negative qty buckets:
    	String SQL = "SELECT * FROM " + SMTableiccosts.TableName
    		+ " WHERE ("
    			+ "(" + SMTableiccosts.sItemNumber + " = '" + ln.sItemNumber() + "')"
    			+ " AND (" + SMTableiccosts.sLocation + " = '" + ln.sLocation() + "')"
    			;
    	
	    	if (lCostingMethod != SMTableicoptions.COSTING_METHOD_AVERAGECOST){
	    		SQL = SQL + " AND (" + SMTableiccosts.bdQty + " > 0.0000)";
	    	}
	    	SQL = SQL + ")"

    		//This gets all the positives first, sorted ASC or DESC by creation date, depending
    		//on whether it's LIFO or FIFO.  If it's average costing, sorting doesn't matter
    		//because there's only one bucket.)
    		+ " ORDER BY " + "(if(bdqty > 0, 'P', 'N')) DESC"  
    		+ ", " + SMTableiccosts.datCreationDate + " " + sSortOrder
    		+ ", " + SMTableiccosts.iId + " " + sSortOrder;
    	
    	try{
    		ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
    		if (rs.next()){
    			bdQtyAvailableInBucket = rs.getBigDecimal(SMTableiccosts.bdQty).setScale(4, BigDecimal.ROUND_HALF_UP);
    			bdCostAvailableInBucket = rs.getBigDecimal(SMTableiccosts.bdCost).setScale(2, BigDecimal.ROUND_HALF_UP);
    			lCostBucketID = rs.getLong(SMTableiccosts.iId);
    			rs.close();
    	    	if(bLogDebug){
    	        	log.writeEntry(
    	        			sUserID, 
    	        			SMLogEntry.LOG_OPERATION_ICBATCHPOST, 
    	        			"User " + sUserFullName + " in drawFromCostBucket",
    	        			"Batch #:" + sBatchNumber() + ", EntryNumber #:" + ln.sEntryNumber()
    	        			+ ", LineNumber #: " + ln.sLineNumber()
    	        			+ ", item = " + ln.sItemNumber()
    	        			+ ", location = " + ln.sLocation()
    	        			+ " FOUND cost bucket: "
    	        			+ ", bdQtyAvailableInBucket = " + bdQtyAvailableInBucket
    	        			+ ", bdCostAvailableInBucket = " + bdCostAvailableInBucket
    	        			+ ", lCostBucketID = " + lCostBucketID,
    	        			"[1376509525]"
    	        	);
    	    	}

    		}else{
    			//this means there are NO more buckets with qtys available, so adjust or create a negative bucket
    			
    			rs.close();
    	    	if(bLogDebug){
    	        	log.writeEntry(
    	        			sUserID, 
    	        			SMLogEntry.LOG_OPERATION_ICBATCHPOST, 
    	        			"User " + sUserFullName + " in drawFromCostBucket",
    	        			"Batch #:" + sBatchNumber() + ", EntryNumber #:" + ln.sEntryNumber()
    	        			+ ", LineNumber #: " + ln.sLineNumber()
    	        			+ ", item = " + ln.sItemNumber()
    	        			+ ", location = " + ln.sLocation()
    	        			+ " DIDN'T FIND cost bucket, going into processNegativeBucket: ",
    	        			"[1376509543]"
    	        	);
    	    	}
    	    	
    			if (!processNegativeBucket(sUserFullName, sUserID, ln, bdQtyRequested, conn)){
    				addErrorMessage(
    						"<br>Error processing negative cost bucket for item " + ln.sItemNumber() 
    						+ ", location " + ln.sLocation() + "."
    				);
    				throw new SQLException();
    			}
    			bdQtyDrawn = bdQtyRequested;
    			//If the negative bucket is processed, then return the qty drawn:
    	    	if(bLogDebug){
    	        	log.writeEntry(
    	        			sUserID, 
    	        			SMLogEntry.LOG_OPERATION_ICBATCHPOST, 
    	        			"User " + sUserFullName + " in drawFromCostBucket",
    	        			"Batch #:" + sBatchNumber() + ", EntryNumber #:" + ln.sEntryNumber()
    	        			+ ", LineNumber #: " + ln.sLineNumber()
    	        			+ ", item = " + ln.sItemNumber()
    	        			+ ", location = " + ln.sLocation()
    	        			+ " Negative cost bucket created, bdQtyDrawn = " + bdQtyDrawn,
    	        			"[1376509547]"
    	        	);
    	    	}

    			return bdQtyDrawn;
    		}
    		
    	}catch (SQLException e){
			addErrorMessage(
					"<br>Error reading quantities in cost bucket for item " + ln.sItemNumber() 
					+ ", location " + ln.sLocation() + " - " + e.getMessage()
			);
    		throw new SQLException();
    	}
    	
    	//Relieve the bucket:
    	if (iCostingMethod == SMTableicoptions.COSTING_METHOD_AVERAGECOST){
    		//If the bucket is negative or has a zero qty:
    		if (bdQtyAvailableInBucket.compareTo(BigDecimal.ZERO) <= 0){
    	    	if(bLogDebug){
    	        	log.writeEntry(
    	        			sUserID, 
    	        			SMLogEntry.LOG_OPERATION_ICBATCHPOST, 
    	        			"User " + sUserFullName + " in drawFromCostBucket",
    	        			"Batch #:" + sBatchNumber() + ", EntryNumber #:" + ln.sEntryNumber()
    	        			+ ", LineNumber #: " + ln.sLineNumber()
    	        			+ ", item = " + ln.sItemNumber()
    	        			+ ", location = " + ln.sLocation()
    	        			+ " Avg costing, qty available in bucket (" + bdQtyAvailableInBucket + ") <=0 "
    	        			+ "so getting most recent cost",
    	        			"[1376509553]"
    	        	);
    	    	}

    			//Get most recent cost
    	    	BigDecimal bdMostRecentCost = new BigDecimal(0);
    	    	try{
    	    		bdMostRecentCost 
    	    			= getItemMostRecentCost(ln.sItemNumber(), conn).setScale(2, BigDecimal.ROUND_HALF_UP);
    	    	}catch (SQLException e){
    	    		addErrorMessage("Error getting most recent cost for '" + ln.sItemNumber() 
    	        			+ e.getMessage());
    	    		throw new SQLException();
    	    	}
        		bdQtyDrawn = bdQtyRequested;
        		bdCostDrawn = bdMostRecentCost.multiply(bdQtyDrawn).setScale(2, BigDecimal.ROUND_HALF_UP);
    	    	if(bLogDebug){
    	        	log.writeEntry(
    	        			sUserID, 
    	        			SMLogEntry.LOG_OPERATION_ICBATCHPOST, 
    	        			"User " + sUserFullName + " in drawFromCostBucket",
    	        			"Batch #:" + sBatchNumber() + ", EntryNumber #:" + ln.sEntryNumber()
    	        			+ ", LineNumber #: " + ln.sLineNumber()
    	        			+ ", item = " + ln.sItemNumber()
    	        			+ ", location = " + ln.sLocation()
    	        			+ " Avg costing, , neg. qty in bucket, most recent cost = " + bdMostRecentCost
    	        			+ ", bdQtyDrawn = bdQtyRequested = " + bdQtyDrawn
    	        			+ ", bdCostDrawn = " + bdCostDrawn,
    	        			"[1376509561]"
    	        	);
    	    	}

       		//Else if the bucket has a positive quantity:
    		}else{
    	    	if(bLogDebug){
    	        	log.writeEntry(
    	        			sUserID, 
    	        			SMLogEntry.LOG_OPERATION_ICBATCHPOST, 
    	        			"User " + sUserFullName + " in drawFromCostBucket",
    	        			"Batch #:" + sBatchNumber() + ", EntryNumber #:" + ln.sEntryNumber()
    	        			+ ", LineNumber #: " + ln.sLineNumber()
    	        			+ ", item = " + ln.sItemNumber()
    	        			+ ", location = " + ln.sLocation()
    	        			+ " Avg costing, qty available in bucket (" + bdQtyAvailableInBucket + ") >0 "
    	        			+ "so going to draw from bucket",
    	        			"[1376509566]"
    	        	);
    	    	}
    			//If we are requesting more than are in the bucket, use it all up:
        		if (bdQtyRequested.compareTo(bdQtyAvailableInBucket) >= 0){
    	    		bdQtyDrawn = bdQtyAvailableInBucket;
    	    		bdCostDrawn = bdCostAvailableInBucket.setScale(2, BigDecimal.ROUND_HALF_UP);
    	    		
    	        	if (bLogDebug){
    	        		log.writeEntry(
    	        			sUserID, 
    	        			SMLogEntry.LOG_OPERATION_ICBATCHPOST, 
    	        			"User " + sUserFullName + " in drawFromCostBucket",
    	        			"Batch #:" + sBatchNumber() + ", EntryNumber #:" + ln.sEntryNumber()
    	        			+ ", LineNumber #: " + ln.sLineNumber()
    	        			+ ", item = " + ln.sItemNumber()
    	        			+ ", location = " + ln.sLocation()
    	        			+ " Avg costing, qty available in bucket (" + bdQtyAvailableInBucket + ") >0 "
    	        			+ "requesting more than in the bucket, so drawing it all"
    	        			+ ", bdQtyDrawn = " + bdQtyDrawn
    	        			+ ", bdCostDrawn = " + bdCostDrawn,
    	        			"[1376509557]"
    	        		);
    	        	}
    	    	//If we are requesting LESS than are in the bucket, take that portion out:
    	    	}else{
    	    		bdQtyDrawn = bdQtyRequested;
    	    		bdCostDrawn = bdCostAvailableInBucket.divide(
    	    				bdQtyAvailableInBucket, BigDecimal.ROUND_HALF_UP);
    	    		bdCostDrawn = bdCostDrawn.multiply(bdQtyRequested).setScale(2, BigDecimal.ROUND_HALF_UP);
    	        	if (bLogDebug){
    	        		log.writeEntry(
    	        			sUserID, 
    	        			SMLogEntry.LOG_OPERATION_ICBATCHPOST, 
    	        			"User " + sUserFullName + " in drawFromCostBucket",
    	        			"Batch #:" + sBatchNumber() + ", EntryNumber #:" + ln.sEntryNumber()
    	        			+ ", LineNumber #: " + ln.sLineNumber()
    	        			+ ", item = " + ln.sItemNumber()
    	        			+ ", location = " + ln.sLocation()
    	        			+ " Avg costing, qty available in bucket (" + bdQtyAvailableInBucket + ") >0 "
    	        			+ "requesting LESS than in the bucket, so drawing only what we need"
    	        			+ ", bdQtyDrawn = " + bdQtyDrawn
    	        			+ ", bdCostDrawn = " + bdCostDrawn,
    	        			"[1376509560]"
    	        		);
    	        	}
    	    	}
    		}
    	//but if it's NOT average costing:
    	}else{
	    	if (bdQtyRequested.compareTo(bdQtyAvailableInBucket) >= 0){
	        	if (bLogDebug){
	        		log.writeEntry(
	        			sUserID, 
	        			SMLogEntry.LOG_OPERATION_ICBATCHPOST, 
	        			"User " + sUserFullName + " in drawFromCostBucket",
	        			"Batch #:" + sBatchNumber() + ", EntryNumber #:" + ln.sEntryNumber()
	        			+ ", LineNumber #: " + ln.sLineNumber()
	        			+ ", item = " + ln.sItemNumber()
	        			+ ", location = " + ln.sLocation()
	        			+ " NOT Avg costing, qty requested (" + bdQtyRequested + ") >= "
	        			+ "qty available in bucket (" + bdQtyAvailableInBucket + ")"
	        			+ ", bdQtyDrawn = " + bdQtyDrawn
	        			+ ", bdCostDrawn = " + bdCostDrawn,
	        			"[1376509570]"
	        		);
	        	}
	    		bdQtyDrawn = bdQtyAvailableInBucket;
	    		bdCostDrawn = bdCostAvailableInBucket.setScale(2, BigDecimal.ROUND_HALF_UP);
	    	}else{
	    		bdQtyDrawn = bdQtyRequested;
	    		if (bdQtyAvailableInBucket.compareTo(BigDecimal.ZERO) == 0){
		    		bdCostDrawn = BigDecimal.ZERO;
	    		}else{
	    			//TODO - solve rounding problem here?
	    			try {
						bdCostDrawn = bdCostAvailableInBucket.divide(
							bdQtyAvailableInBucket, RoundingMode.HALF_UP);
						
	    	    		//bdCostDrawn = bdCostAvailableInBucket.setScale(2).divide(
	    	    		//		bdQtyAvailableInBucket.setScale(2), BigDecimal.ROUND_HALF_UP);
						
					} catch (ArithmeticException a) {
						addErrorMessage("Error dividing in drawfromcostbucket - bdCostAvailableInBucket = "
							+ bdCostAvailableInBucket + ", bdQtyAvailableInBucket = " + bdQtyAvailableInBucket
							//+ ", bdQtyAvailableInBucket.setScale(2) = " + bdQtyAvailableInBucket.setScale(2)
							+ ", bucket id = " + lCostBucketID + " - " + a.getMessage());
					}
	    			
	    			//Test line:
	    			//bdCost.divide(bdQty, BigDecimal.ROUND_HALF_UP).setScale(2, BigDecimal.ROUND_HALF_UP);
	    		}
	    		bdCostDrawn = bdCostDrawn.multiply(bdQtyRequested).setScale(2, BigDecimal.ROUND_HALF_UP);
	        	if (bLogDebug){
	        		log.writeEntry(
	        			sUserID, 
	        			SMLogEntry.LOG_OPERATION_ICBATCHPOST, 
	        			"User " + sUserFullName + " in drawFromCostBucket",
	        			"Batch #:" + sBatchNumber() + ", EntryNumber #:" + ln.sEntryNumber()
	        			+ ", LineNumber #: " + ln.sLineNumber()
	        			+ ", item = " + ln.sItemNumber()
	        			+ ", location = " + ln.sLocation()
	        			+ " NOT Avg costing, qty requested (" + bdQtyRequested + ") < "
	        			+ "qty available in bucket (" + bdQtyAvailableInBucket + ")"
	        			+ ", bdQtyDrawn = " + bdQtyDrawn
	        			+ ", bdCostDrawn = " + bdCostDrawn,
	        			"[1376509548]"
	        		);
	        	}

	    	}
    	}

    	SQL = "UPDATE " + SMTableiccosts.TableName + " SET "
		+ SMTableiccosts.bdQty + " = " + SMTableiccosts.bdQty + " - " 
			+ "(" + clsManageBigDecimals.BigDecimalToFormattedString("#######0.0000", bdQtyDrawn) + ")"
		+ ", " + SMTableiccosts.bdCost + " = " + SMTableiccosts.bdCost + " - " 
			+ "(" + clsManageBigDecimals.BigDecimalToFormattedString("#######0.00", bdCostDrawn) + ")"
		+ ", " + SMTableiccosts.bdCostShipped + " = " + SMTableiccosts.bdCostShipped + " + " 
			+ "(" + clsManageBigDecimals.BigDecimalToFormattedString("#######0.00", bdCostDrawn) + ")"
		+ ", " + SMTableiccosts.bdQtyShipped + " = " + SMTableiccosts.bdQtyShipped + " + " 
			+ "(" + clsManageBigDecimals.BigDecimalToFormattedString("#######0.0000", bdQtyDrawn) + ")"
		+ " WHERE ("
			+ SMTableiccosts.iId + " = " + Long.toString(lCostBucketID)
		+ ")"
		;
    	
    	if (bLogDebug){
    		log.writeEntry(
    			sUserID, 
    			SMLogEntry.LOG_OPERATION_ICBATCHPOST, 
    			"User " + sUserFullName + " in drawFromCostBucket"
    			+ "Batch #:" + sBatchNumber() + ", EntryNumber #:" + ln.sEntryNumber() 
    			+ ", line number " + ln.sLineNumber() 
    			+ ", updating ICCOSTS",
    			"SQL = " + SQL,
    			"[1376509567]"
    		);
    	}
		
		try{
			clsDatabaseFunctions.executeSQL(SQL, conn);
		}catch (SQLException e){
			addErrorMessage(
					"<br>Error updating cost bucket for item " + ln.sItemNumber() 
					+ ", location " + ln.sLocation() + " - " + e.getMessage()
			);
    		throw new SQLException();
		}
    	//Add a transaction detail here:
    	if (!addTransactionDetail(conn, lCostBucketID, bdCostDrawn.negate(), bdQtyDrawn.negate())){
    		throw new SQLException();
    	}
		
		//Update the cost on the shipment/transfer/physical count line here:
		BigDecimal bdCostSoFar 
			= new BigDecimal(
				ln.sCostSTDFormat().replace(",", "")).setScale(2, BigDecimal.ROUND_HALF_UP);
		//The cost on a shipment/transfer/(negative change) physical count line is negative, so we subtract the cost drawn from the cost so far:
		ln.setCostString(clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(bdCostSoFar.subtract(bdCostDrawn)));
    	if (bLogDebug){
    		log.writeEntry(
    			sUserID, 
    			SMLogEntry.LOG_OPERATION_ICBATCHPOST, 
    			"User " + sUserFullName + " in drawFromCostBucket",
    			"Batch #:" + sBatchNumber() + ", EntryNumber #:" + ln.sEntryNumber()
    			+ ", LineNumber #: " + ln.sLineNumber()
    			+ ", item = " + ln.sItemNumber()
    			+ ", location = " + ln.sLocation()
    			+ ", bdCostSoFar = " + bdCostSoFar
    			+ ", bdCostDrawn = " + bdCostDrawn
    			+ " Updated ln.setCostString(" 
    			+ clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(bdCostSoFar.subtract(bdCostDrawn)) + ")",
    			"[1376509558]"
    		);
    	}
		
    	return bdQtyDrawn;
    }
    private boolean addTransactionDetail(
    		Connection conn, 
    		long lCostBucketID, 
    		BigDecimal bdCostChange, 
    		BigDecimal bdQtyChange){
    	
    	ICTransactionDetail td = new ICTransactionDetail();
    	String SQL = "SELECT * FROM " + SMTableiccosts.TableName
    		+ " WHERE ("
    			+ "(" + SMTableiccosts.iId + " = " + Long.toString(lCostBucketID) + ")"
    		+ ")"
    		;
    	
    	try {
			ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
			if (rs.next()) {
				//Calculate the qty and cost of the bucket BEFORE the update by subtracting the qty change 
				//and cost change from the current qty and cost in the bucket:
				td.setM_bdcostbucketcostbeforetrans(rs.getBigDecimal(SMTableiccosts.bdCost).subtract(bdCostChange));
				td.setM_bdcostbucketqtybeforetrans(rs.getBigDecimal(SMTableiccosts.bdQty).subtract(bdQtyChange));
				td.setM_bdcostchange(bdCostChange);
				td.setM_bdqtychange(bdQtyChange);
				td.setM_datetimecostbucketcreation(rs.getDate(SMTableiccosts.datCreationDate));
				td.setM_lbucketreceiptlineid(rs.getLong(SMTableiccosts.lReceiptLineID));
				td.setM_lcostbucketid(lCostBucketID);
				td.setM_scostbucketlocation(rs.getString(SMTableiccosts.sLocation));
				td.setM_scostbucketremark(rs.getString(SMTableiccosts.sRemark));
				rs.close();
			} else {
				rs.close();
				addErrorMessage("Could not read cost bucket ID "
						+ Long.toString(lCostBucketID)
						+ " - record not found.");
				return false;
			}
		} catch (SQLException e) {
			addErrorMessage("Could not read cost bucket ID "
				+ Long.toString(lCostBucketID)
				+ " - " + e.getMessage());
			return false;
		}
		
		m_arrTransactionDetails.add(td);
		return true;
    }
    private boolean processNegativeBucket(
    		String sUserFullName,
    		String sUserID,
    		ICEntryLine line,
    		BigDecimal bdQtyRequested,
    		Connection conn){

    	BigDecimal bdMostRecentCost = new BigDecimal(0);
    	
    	//First, get the most recent cost:
    	try{
    		bdMostRecentCost = getItemMostRecentCost(line.sItemNumber(), conn);
    	}catch (SQLException e){
    		addErrorMessage("Error getting most recent cost for '" + line.sItemNumber() 
        			+ e.getMessage());
    		return false;
    	}
    	BigDecimal bdTotalCost = bdMostRecentCost.multiply(bdQtyRequested).setScale(
    			2, BigDecimal.ROUND_HALF_UP);
    	
    	//If there is a negative qty bucket available, take the item(s) from there - if not
    	//create a new negative bucket:
    	String SQL = "SELECT"
    		+ " *"
    		+ " FROM " + SMTableiccosts.TableName
    		+ " WHERE ("
    			+ "(" + SMTableiccosts.sItemNumber + " = '" + line.sItemNumber() + "')"
    			+ " AND (" + SMTableiccosts.sLocation + " = '" + line.sLocation() + "')"
    			+ " AND (" + SMTableiccosts.bdQty + " <= 0)" 
    		+ ")"
    		+ " ORDER BY " + SMTableiccosts.datCreationDate + " DESC LIMIT 1";
    	;
    	
    	long lBucketID = -1;
    	
    	//We're using these so they can be used in the update statement below instead of doing
    	// 'bdCost = bdCost + XXX.XX' which may be causing a lock wait timeout
    	BigDecimal bdBucketCost = new BigDecimal(0);
    	BigDecimal bdBucketCostShipped = new BigDecimal(0);
    	BigDecimal bdBucketQty = new BigDecimal(0);
    	BigDecimal bdBucketQtyShipped = new BigDecimal(0);
    	
    	try {
			ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
			if (rs.next()) {
				lBucketID = rs.getLong(SMTableiccosts.iId);
		    	bdBucketCost = rs.getBigDecimal(SMTableiccosts.bdCost);
		    	bdBucketCostShipped = rs.getBigDecimal(SMTableiccosts.bdCostShipped);
		    	bdBucketQty = rs.getBigDecimal(SMTableiccosts.bdQty);
		    	bdBucketQtyShipped = rs.getBigDecimal(SMTableiccosts.bdQtyShipped);
			}
			rs.close();
		} catch (SQLException e) {
    		addErrorMessage("Error finding negative cost bucket for '" + line.sItemNumber() 
        			+ e.getMessage());
    		return false;
		}
		
		//If we didn't find a negative qty bucket, then insert a new one:
		if (lBucketID == -1){
			SQL = "INSERT INTO " + SMTableiccosts.TableName + " ("
    		+  SMTableiccosts.bdCost
    		+ ", " + SMTableiccosts.bdCostShipped
    		+ ", " + SMTableiccosts.bdQty
    		+ ", " + SMTableiccosts.bdQtyShipped
    		+ ", " + SMTableiccosts.datCreationDate
    		+ ", " + SMTableiccosts.iSource
    		+ ", " + SMTableiccosts.sItemNumber
    		+ ", " + SMTableiccosts.sLocation
    		+ ", " + SMTableiccosts.sRemark
    		
    		+ ") VALUES ("
    		+ clsManageBigDecimals.BigDecimalTo2DecimalSQLFormat(bdTotalCost.negate())
    		+ ", 0.00" //When creating a cost bucket the 'shipped' values should always be zero
    		+ ", " + clsManageBigDecimals.BigDecimalToFormattedString("#########0.0000", bdQtyRequested.negate())
    		+ ", 0.0000" //When creating a cost bucket the 'shipped' values should always be zero
    		+ ", NOW()"
    		+ ", 1"
    		+ ", '" + line.sItemNumber() + "'"
    		+ ", '" + line.sLocation() + "'"
    		+ ", 'System generated'"
    		+ ")"
    	;
		//Otherwise, we found a negative qty bucket that we could draw from:
		}else{
			SQL = "UPDATE " + SMTableiccosts.TableName + " SET "
				+ SMTableiccosts.bdCost + " = " 
					+ clsManageBigDecimals.BigDecimalTo2DecimalSQLFormat(bdBucketCost) + " + (" 
					+ clsManageBigDecimals.BigDecimalTo2DecimalSQLFormat(bdTotalCost.negate()) + ")"
				+ ", " + SMTableiccosts.bdCostShipped + " = " 
					+ clsManageBigDecimals.BigDecimalTo2DecimalSQLFormat(bdBucketCostShipped) + " - ("
					+ clsManageBigDecimals.BigDecimalTo2DecimalSQLFormat(bdTotalCost.negate()) + ")"
				+ ", " + SMTableiccosts.bdQty + " = " 
					+ clsManageBigDecimals.BigDecimalTo2DecimalSQLFormat(bdBucketQty) + " + ("
					+ clsManageBigDecimals.BigDecimalToFormattedString("#########0.0000", bdQtyRequested.negate()) + ")"
				+ ", " + SMTableiccosts.bdQtyShipped + " = " 
					+ clsManageBigDecimals.BigDecimalTo2DecimalSQLFormat(bdBucketQtyShipped) + " - ("
					+ clsManageBigDecimals.BigDecimalTo2DecimalSQLFormat(bdQtyRequested.negate()) + ")"
				+ " WHERE ("
					+ "(" + SMTableiccosts.iId + " = " + lBucketID + ")"
				+ ")"
			;
		}
		
    	try {
			Statement stmt = conn.createStatement();
			stmt.executeUpdate(SQL);
		} catch (SQLException e) {
    		addErrorMessage("Error writing negative cost bucket with SQL: " + SQL + "for '" 
    				+ line.sItemNumber() + "' - "
        			+ e.getMessage());
    		return false;
		}
		
		//Add a transaction detail here
		if (lBucketID == -1){
			try {
				lBucketID = clsDatabaseFunctions.getLastInsertID(conn);
			} catch (SQLException e) {
				addErrorMessage("Could not get new cost bucket ID for line '"
					+ line.sItemNumber() + " - " + e.getMessage());
				return false;
			}
		}
    	if (!addTransactionDetail(conn, lBucketID, bdTotalCost.negate(), bdQtyRequested.negate())){
    		return false;
    	}
    	if (bLogDebug){
    		log.writeEntry(
    			sUserID, 
    			SMLogEntry.LOG_OPERATION_ICBATCHPOST, 
    			"User " + sUserFullName + " in processNegativeBucket"
    			+ " Batch #:" + sBatchNumber() + ", EntryNumber #:" + line.sEntryNumber()
    			+ ", LineNumber #: " + line.sLineNumber()
    			+ ", item = " + line.sItemNumber()
    			+ ", location = " + line.sLocation(),
    			"SQL = " + SQL,
    			"[1376509388]"
    		);
    	}
		//Update the cost on the line:
		BigDecimal bdCostSoFar 
			= new BigDecimal(
				line.sCostSTDFormat().replace(",", "")).setScale(2, BigDecimal.ROUND_HALF_UP);
		//This bdTotalCost would be negative on a shipment
		line.setCostString(clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(bdCostSoFar.subtract(bdTotalCost)));
		
    	return true;
    }
    private BigDecimal getItemMostRecentCost(String sItem, Connection conn) throws SQLException{
    	
    	String SQL = "SELECT "
    		+ SMTableicitems.bdmostrecentcost
    		+ " FROM " + SMTableicitems.TableName
    		+ " WHERE ("
    			+ SMTableicitems.sItemNumber + " = '" + sItem + "'"
    		+ ")"
    		;
    	
    	BigDecimal bdMostRecentCost = new BigDecimal(0);
    	try{
    		ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
    		if (rs.next()){
    			bdMostRecentCost = rs.getBigDecimal(SMTableicitems.bdmostrecentcost);
    			rs.close();
    		}else{
    			rs.close();
    			addErrorMessage(
    					"<br>Error reading most recent cost for item " + sItem);
    			throw new SQLException();
    		}
    	}catch (SQLException e){
			addErrorMessage(
					"<br>Error reading most recent cost for item " + sItem + " - " + e.getMessage());
			throw new SQLException();
    	}
    	
    	return bdMostRecentCost.setScale(SMTableicitems.bdmostrecentcostScale, BigDecimal.ROUND_HALF_UP);
    	
    }
    private BigDecimal getQtyAvailableForItemAndLocation(String sItem, String sLoc, Connection conn) throws SQLException{
    	
    	BigDecimal bdQuantityAvailable = new BigDecimal(0);
    	
    	//TODO - should we ignore negative quantities at locations?
    	String SQL = "SELECT SUM(" + SMTableiccosts.bdQty + ") AS QTYAVAILABLE FROM "
    		+ SMTableiccosts.TableName
    		+ " WHERE ("
    			+ "(" + SMTableiccosts.sItemNumber + " = '" + sItem + "')"
    			+ " AND (" + SMTableiccosts.sLocation + " = '" + sLoc + "')"
    		+ ")"
    		;
    	
    	try{
    		ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
    		if (rs.next()){
    			bdQuantityAvailable = rs.getBigDecimal("QTYAVAILABLE");
    			if (bdQuantityAvailable == null){
    				bdQuantityAvailable = BigDecimal.ZERO;
    			}
    		}
    		rs.close();
    	}catch (SQLException e){
    		addErrorMessage("Error reading available quantities for item '" + sItem 
    			+ "' at location '" + sLoc + "': " + e.getMessage());
    		throw new SQLException();
    	}
    	
    	return bdQuantityAvailable.setScale(4, BigDecimal.ROUND_HALF_UP);
    }
    private BigDecimal getQtyAvailableInBucket(String sBucketID, Connection conn) throws SQLException{
    	
    	BigDecimal bdQuantityAvailable = new BigDecimal(0);
    	
    	//TODO - should we ignore negative quantities at locations?
    	String SQL = "SELECT " + SMTableiccosts.bdQty +  " FROM "
    		+ SMTableiccosts.TableName
    		+ " WHERE ("
    			+ "(" + SMTableiccosts.iId + " = " + sBucketID + ")"
    		+ ")"
    		;
    	
    	try{
    		ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
    		if (rs.next()){
    			bdQuantityAvailable = rs.getBigDecimal(SMTableiccosts.bdQty);
    		}
    		rs.close();
    	}catch (SQLException e){
    		addErrorMessage("Error reading available quantities for bucket '" + sBucketID 
    			+ "': " + e.getMessage());
    		throw new SQLException();
    	}
    	return bdQuantityAvailable.setScale(4, BigDecimal.ROUND_HALF_UP);
    }
    private long checkForAvgCostingItemAndLocationBucket(
    		String sItem, 
    		String sLocation, 
    		Connection con
    		) throws SQLException{
    	
    	//This function looks for a single cost bucket, assuming we are using average costing.  If there
    	//is more than one bucket for this item and location, it returns only ONE bucket ID, because
    	//in an average costing system, there should be only one anyway.
    	
    	String SQL = "SELECT"
    		+ " " + SMTableiccosts.iId
    		+ " FROM " + SMTableiccosts.TableName
    		+ " WHERE ("
    			+ "(" + SMTableiccosts.sItemNumber + " = '" + sItem + "')"
    			+ " AND (" + SMTableiccosts.sLocation + " = '" + sLocation + "')"
    		+ ")"
    		+ " ORDER BY " + SMTableiccosts.iId + " DESC"
    		+ " LIMIT 1"
    		;
    	
    	long lCostBucketID = -1;
    	try {
			ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, con);
			if (rs.next()) {
				lCostBucketID = rs.getLong(SMTableiccosts.iId);
			}
			rs.close();
		} catch (SQLException e) {
			//Doesn't matter here - return a zero;
		}
		return lCostBucketID;
    	
    }
    private boolean getICOptions (Connection conn){
    	
    	boolean bResult = false;
    	
    	String SQL = "SELECT "
    		+ SMTableicoptions.lcostingmethod
    		+ ", " + SMTableicoptions.lallownegativeqtys
    		+ ", " + SMTableicoptions.iflagimports
    		+ " FROM " + SMTableicoptions.TableName
    		;
    	
    	try{
    		ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
    		if (rs.next()){
    			lCostingMethod = rs.getLong(SMTableicoptions.lcostingmethod);
    			if (rs.getLong(SMTableicoptions.lallownegativeqtys) == 0){
    				bAllowNegativeQtys = false;
    			}else{
    				bAllowNegativeQtys = true;
    			}
       			if (rs.getLong(SMTableicoptions.iflagimports) == 1){
    				m_iFlagInvoices = true;
    			}else{
    				m_iFlagInvoices = false;
    			}
    			bResult = true;
    		}
    		rs.close();
    	}catch(SQLException e){
    		addErrorMessage("Error getting costing method: " + e.getMessage());
    	}
    	
    	return bResult;
    }

    private boolean createGLBatch(Connection conn, String sUserID, String sUserFullName){
    	//TJR - here is where the GL batch gets created during posting:
    	SMGLExport export = new SMGLExport();
    	
    	String SQL = "SELECT * FROM " + SMTablesmoptions.TableName;
    	try{
	    	ResultSet rsOptions = clsDatabaseFunctions.openResultSet(SQL, conn);
	    	
	    	if (rsOptions.next()){
	    		export.setExportFilePath(rsOptions.getString(SMTablesmoptions.sfileexportpath));
	    	}else{
	    		addErrorMessage("Could not get SMOption record to read export file.");
	    	}
	    	rsOptions.close();
    	}catch (SQLException e){
    		addErrorMessage("Error reading path for export file: " + e.getMessage());
    		return false;
    	}
    	
    	//Get the entries and lines from the batch:
		SQL = "SELECT "
			+ SMTableicbatchentries.lid
			+ ", " + SMTableicbatchentries.lentrynumber
			+ " FROM " + SMTableicbatchentries.TableName
			+ " WHERE (" 
			+ SMTableicbatchentries.lbatchnumber + " = " + sBatchNumber()
			+ ")"
			+ " ORDER BY " + SMTableicbatchentries.lentrynumber + " ASC";
        try {
        	ResultSet rsEntryListForGLExport = clsDatabaseFunctions.openResultSet(SQL, conn);
        	while (rsEntryListForGLExport.next()){
            	ICEntry entry = new ICEntry();
            	if (!entry.load(rsEntryListForGLExport.getString(SMTableicbatchentries.lid), conn)){
        			addErrorMessage(
        				"Could not load entry " 
        					+ rsEntryListForGLExport.getLong(SMTableicbatchentries.lentrynumber) + " for export");
        			rsEntryListForGLExport.close();
        			return false;
        		}
        		
        		export.addHeader(
        				sModuleType(), 
        				ICEntryTypes.getSourceTypes(Integer.parseInt(entry.sEntryType())),
        				"IC Batch Export", 
        				"SMIC",
        				entry.sStdEntryDate(),
        				entry.sStdEntryDate(),
        				"IC " + ICEntryTypes.Get_Entry_Type(Integer.parseInt(entry.sEntryType()))
        		);
        		        		
        		for (int i = 0; i < entry.getLineCount(); i ++){
        			//Now add each line from the entry as a GL transaction:
        			ICEntryLine line = entry.getLineByIndex(i);
        			
            		String sLineDesc = line.sDescription();
           			sLineDesc = "(" + line.sQtySTDFormat() + ") " + line.sItemNumber();

            		if (sLineDesc.length() > SMTableglexportdetails.sdetailtransactiondescriptionlength){
            			sLineDesc = sLineDesc.substring(0, (SMTableglexportdetails.sdetailtransactiondescriptionlength - 1)).trim();
            		}
               		String sLineReference = "Doc #: " + entry.sDocNumber();
               		if (Integer.parseInt(entry.sEntryType()) == ICEntryTypes.RECEIPT_ENTRY){
               			sLineReference = "Rcpt #: " + entry.sDocNumber();
               		}
               		if (Integer.parseInt(entry.sEntryType()) == ICEntryTypes.SHIPMENT_ENTRY){
               			sLineReference = "Inv #: " + entry.sDocNumber();
               		}
               		
            		if (sLineReference.length() > SMTableglexportdetails.sdetailtransactionreferencelength){
            			sLineReference = sLineReference.substring(0, (SMTableglexportdetails.sdetailtransactionreferencelength - 1)).trim();
            		}
            		String sLineComment = "";
               		if (Integer.parseInt(entry.sEntryType()) == ICEntryTypes.RECEIPT_ENTRY){
               			sLineComment = entry.sEntryDescription();
               		}
            		if (sLineComment.length() > SMTableglexportdetails.sdetailcommentlength){
            			sLineComment = sLineComment.substring(0, (SMTableglexportdetails.sdetailcommentlength - 1)).trim();
            		}
            		
            		//Add a GL Entry to the inventory asset account (control)
            		BigDecimal bdCost = new BigDecimal(line.sCostSTDFormat().replace(",", ""));
            		java.sql.Date datEntry;
					try {
						datEntry = clsDateAndTimeConversions.StringTojavaSQLDate("MM/dd/yyyy", entry.sStdEntryDate());
					} catch (ParseException e1) {
						addErrorMessage("Error:[1423843613] Invalid entry date: '"
							    + entry.sStdEntryDate() + "' - " + e1.getMessage());
						return false;
					}
            		try {
						export.addDetail(
								datEntry,
								bdCost,
								line.sControlAcct(),
								sLineComment,
								sLineDesc,
								sLineReference,
								line.sLineNumber(),
								conn
								);
					} catch (Exception e1) {
						addErrorMessage("Error:[1478817279] adding export detail - "
								+ e1.getMessage());
						return false;
					}
            		//Add a GL Entry to the balancing account (distribution)
        			try {
						export.addDetail(
								datEntry,
								bdCost.negate(),
								line.sDistributionAcct(),
								sLineComment,
								sLineDesc,
								sLineReference,
								line.sLineNumber(),
								conn
								);
					} catch (Exception e1) {
						addErrorMessage("Error:[1478817280] adding export detail - "
								+ e1.getMessage());
						return false;
					}

        			//If the line is a transfer, we have to add another transaction, for the
        			//'TO' location:
        			if(Integer.parseInt(entry.sEntryType()) == ICEntryTypes.TRANSFER_ENTRY){
                		//Add a GL Entry to the inventory asset account (control)
        				//Get the inventory acct and the transfer clearing acct for the 
        				// 'TO' location here:
        				String sToLocationInvAcct = "";
        				String sToLocationTransferClearingAcct = "";
        				SQL = "SELECT"
        					+ " " + SMTablelocations.sGLInventoryAcct
        					+ ", " + SMTablelocations.sGLTransferClearingAcct
        					+ " FROM " + SMTablelocations.TableName
        					+ " WHERE ("
        						+ "(" + SMTablelocations.sLocation + " = '" + line.sTargetLocation() + "')"
        					+ ")"
        				;
        				//SMUtilities.sysprint(this.toString(), sUserName, "SQL for traget transfer: " + SQL);
        				try {
							ResultSet rsLocation = clsDatabaseFunctions.openResultSet(
									SQL, conn);
							if (rsLocation.next()) {
								sToLocationInvAcct = rsLocation
										.getString(SMTablelocations.sGLInventoryAcct);
								sToLocationTransferClearingAcct = rsLocation
										.getString(SMTablelocations.sGLTransferClearingAcct);
								rsLocation.close();
							} else {
								addErrorMessage("Could not read target location GL accts for location '"
										+ line.sTargetLocation() + "'.");
								rsEntryListForGLExport.close();
								rsLocation.close();
								return false;
							}
						} catch (SQLException e) {
							addErrorMessage("Error reading target location GL accts for location '"
									+ line.sTargetLocation() + "' - " + e.getMessage());
							rsEntryListForGLExport.close();
							return false;
						}
						try {
							export.addDetail(
									datEntry,
									bdCost.negate(),
									sToLocationInvAcct,
									"Comment",
									sLineDesc,
									sLineReference,
									line.sLineNumber(),
									conn
									);
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
                		//Add a GL Entry to the balancing account (distribution)
            			try {
							export.addDetail(
									datEntry,
									bdCost,
									sToLocationTransferClearingAcct,
									"Comment",
									sLineDesc,
									sLineReference,
									line.sLineNumber(),
									conn
									);
						} catch (Exception e) {
					       	addErrorMessage("Error [1478817197] adding export detail - " + e.getMessage());
				        	return false;
						}
        			}
        		}
        	}
        	rsEntryListForGLExport.close();
        } catch (SQLException e){
        	//System.out.println("Error in " + this.toString() + ".createGLBatch - SQL error: " + e.getMessage());
        	addErrorMessage("SQL Error opening batch for export: " + e.getMessage());
        	return false;
        }
        String sExportBatchNumber = Long.toString(lBatchNumber());
        sExportBatchNumber = clsStringFunctions.PadLeft(sExportBatchNumber, "0", 6);
        
        try {
			export.saveExport(sExportBatchNumber, conn);
		} catch (Exception e) {
        	addErrorMessage("Error saving GL export file - " + e.getMessage());
        	return false;
		}
        
		ICOption icopt = new ICOption();
		if(!icopt.load(conn)){
			addErrorMessage("Error [1474646317] getting export file type - " + icopt.getErrorMessage()); 
			return false;
		}
		int iFeedGL = Integer.parseInt(icopt.getfeedgl());
		if (
			(iFeedGL == SMTableicoptions.FEED_GL_BOTH_EXTERNAL_AND_SMCP_GL)
			|| (iFeedGL == SMTableicoptions.FEED_GL_SMCP_GL_ONLY)
				
		){
			try {
				GLTransactionBatch gltransactionbatch = export.createGLTransactionBatch(
					conn, 
					sUserID, 
					sUserID, 
					getBatchDateInStdFormat(), 
					"IC " + ICBatchTypes.Get_Batch_Type(iBatchType()) + " Batch #" + sBatchNumber()
				);
				
				gltransactionbatch.save_without_data_transaction(
					conn, 
					sUserID, 
					sUserFullName, 
					true
				);
			} catch (Exception e) {
	        	addErrorMessage("Error [1557516918] creating GL transaction batch - " + e.getMessage());
	        	return false;
			}
		}
		
		if (
				(iFeedGL == SMTableicoptions.FEED_GL_BOTH_EXTERNAL_AND_SMCP_GL)
				|| (iFeedGL == SMTableicoptions.FEED_GL_EXTERNAL_GL_ONLY)
					
		){
	        if (export.getExportFilePath().compareToIgnoreCase("") != 0){
		        if (!export.writeExportFile(
		        		SMModuleTypes.IC, 
		        		sBatchTypeLabel(), 
		        		sExportBatchNumber,
		        		(int) icopt.getExportTo(),
		        		conn)
		        	){
		        	addErrorMessage("Error [1557516917] writing GL export file - " + export.getErrorMessage());
		        	return false;
		        }
		    }
		}
    	return true;
    }
	private boolean updateCreditLineCost(
			String sMatchingInvoiceNumber,
			ICEntryLine line, 
			Connection con
	){

		//If the original invoice number is blank, get out:
		if (sMatchingInvoiceNumber.trim().compareToIgnoreCase("") == 0){
			addErrorMessage("Could not get original invoice number to cost credit note on entry "
				+ line.sEntryNumber() + ".");
			return false;
		}
		
		//The iMatchingLineNumber field in the invoice details isn't actually being used.  BOTH the
		// iLineNumber or the iDetailNumber fields on a credit note point back to the corresponding 
		//line on the invoice which was credited, so we'll use one of those to read the costs from
		//the invoice:
		String SQL = "SELECT " + SMTableinvoicedetails.dExtendedCost
			+ " FROM " + SMTableinvoicedetails.TableName
			+ " WHERE ("
				+ "(" + SMTableinvoicedetails.sInvoiceNumber + " = '" + sMatchingInvoiceNumber + "')"
				+ " AND (" + SMTableinvoicedetails.iLineNumber + " = " + line.sInvoiceLineNumber() + ")"
			+ ")"
			;
		
		try {
			ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, con);
			if (rs.next()){
				line.setCostString(
					clsManageBigDecimals.doubleTo2DecimalSTDFormat(
						rs.getDouble(SMTableinvoicedetails.dExtendedCost)).replace(",", ""));
			}else{
				addErrorMessage("Could not update credit line cost from invoice '" + sMatchingInvoiceNumber 
						+ "' for invoice line number " + line.sInvoiceLineNumber() + "."); 
				return false;
			}
			rs.close();
		} catch (SQLException e) {
			addErrorMessage("Could not update credit line cost from invoice '" + sMatchingInvoiceNumber 
			+ "' - " + e.getMessage());
			return false;
		}
		return true;
	}
}
