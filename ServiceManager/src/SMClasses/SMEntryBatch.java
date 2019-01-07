package SMClasses;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.Calendar;

import javax.servlet.ServletContext;

import smar.ARUtilities;
import SMDataDefinition.SMTableentries;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsDateAndTimeConversions;
import ServletUtilities.clsManageBigDecimals;

public class SMEntryBatch {

	//Table Name
	public static final String TableName = "entrybatches";
	
	//Field names:
	public static final String ibatchnumber = "ibatchnumber";
	public static final String datbatchdate = "datbatchdate";
	public static final String ibatchstatus = "ibatchstatus";
	public static final String sbatchdescription = "sbatchdescription";
	public static final String smoduletype = "smoduletype";
	public static final String ibatchtype = "ibatchtype";
	public static final String datlasteditdate = "datlasteditdate";
	public static final String ibatchlastentry = "ibatchlastentry";
	public static final String lcreatedbyid = "lcreatedbyid";
	public static final String screatedbyfullname = "screatedbyfullname";
	public static final String llasteditedbyid = "llasteditedbyid";
	public static final String slasteditedbyfullname = "slasteditedbyfullname";
	public static final String datpostdate = "datpostdate";
	
	//Field Lengths:
	public static final int sBatchDescriptionLength = 128;
	public static final int screatedbyfullnameLength = 128;
	public static final int slasteditedbyfullnameLength = 128;
	
	private long m_lbatchnumber;
	private java.sql.Timestamp m_tsbatchdate;
	private int m_ibatchstatus;
	private String m_sbatchdescription;
	private int m_ibatchtype;
	private java.sql.Timestamp m_tslasteditdate;
	private int m_ibatchlastentry;
	private String m_lcreatedbyid;
	private String m_llasteditedbyid;
	private String m_screatedbyfullname;
	private String m_slasteditedbyfullname;
	private String m_smoduletype;
	private java.sql.Date m_datpostdate;
	private String m_spostingdate;
	private String m_sErrorMessages;
	
    public SMEntryBatch(
    		String sBatchNumber
        ) 
    {
    	sBatchNumber(sBatchNumber);
    	m_tsbatchdate = clsDateAndTimeConversions.nowAsTimestamp();
    	m_ibatchstatus = SMBatchStatuses.ENTERED;
    	m_sbatchdescription = "INITIALIZED BATCH";
    	m_ibatchtype = SMBatchTypes.AR_INVOICE;
    	m_tslasteditdate = clsDateAndTimeConversions.nowAsTimestamp();
    	m_ibatchlastentry = 0;
    	m_lcreatedbyid = "0";
    	m_llasteditedbyid = "0";
    	m_screatedbyfullname = "";
    	m_slasteditedbyfullname = "";
    	m_smoduletype = SMModuleTypes.AR;
    	m_datpostdate = null;
    	m_spostingdate = "00/00/0000 00:00:00 AM";
    	m_sErrorMessages = "";
        }
    public void load (
    		ServletContext context, 
    		String sDBID
    		) throws Exception{
    
    	try {
			load (sBatchNumber(), context, sDBID);
		} catch (Exception e) {
			m_sErrorMessages += " - " + e.getMessage();
			throw new Exception(e.getMessage());
		}
    }
    public void load (
    		Connection conn
    		) throws Exception{
    	try {
			load (sBatchNumber(), conn);
		} catch (Exception e) {
			m_sErrorMessages += " - " + e.getMessage();
			throw new Exception(e.getMessage());
		}
    }
    public void load (
		String sBatchNumber,
		ServletContext context, 
		String sDBID
		) throws Exception{
    
	    if (! sBatchNumber(sBatchNumber)){
	    	throw new Exception("Invalid batch number '" + sBatchNumber + "' [1385582776]");
	    }

	    String SQL = TRANSACTIONSQLs.Get_TransactionBatch_By_Number(sBatchNumber);
		try {
			ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, context, sDBID); 
			rs.next();
    		if (!sBatchDescription(rs.getString(sbatchdescription))){ rs.close(); throw new Exception("Error getting batch description [1385582776]"); };
    		if (!iBatchType(rs.getInt(ibatchtype))){ rs.close(); throw new Exception("Error getting batch type [1385582777]"); };
    		if(ARUtilities.testResultSetTSFieldForNull(rs, datlasteditdate)){
    			if (!tsLastEditDate(rs.getTimestamp(datlasteditdate))){
    				m_tslasteditdate = null;
    			};
    		}else{
    			m_tslasteditdate = null;
    		}
    		if (!sLastEditedByID(Integer.toString(rs.getInt(llasteditedbyid)))){ rs.close(); throw new Exception("Error getting last edited by [1385582778]"); };
    		if (!sLastEditedByFullName(rs.getString(slasteditedbyfullname))){ rs.close(); throw new Exception("Error getting last edited by [1385582779]"); };
    		if(ARUtilities.testResultSetTSFieldForNull(rs, datbatchdate)){
    			if (!tsBatchDate(rs.getTimestamp(datbatchdate))){
    				m_tsbatchdate = null;
    			};
    		}else{
    			m_tsbatchdate = null;
    		}
    		//System.out.println("in SMEntryBatch: m_tsbatchdate = " + m_tsbatchdate.toString());
    		if (!iBatchStatus(rs.getInt(ibatchstatus))){ rs.close(); throw new Exception("Error getting batch status [1385582779]"); };
    		if (!sCreatedByID(Integer.toString(rs.getInt(lcreatedbyid)))){ rs.close(); throw new Exception("Error getting 'Created by' [1385582780]"); };
    		if (!sCreatedByFullName(rs.getString(screatedbyfullname))){ rs.close(); throw new Exception("Error getting 'Created by' [1385582782]"); };
    		m_ibatchlastentry = rs.getInt(ibatchlastentry);
    		if (!sModuleType(rs.getString(smoduletype))){ rs.close(); throw new Exception("Error getting module type [1385582781]"); };
    		String sPost = rs.getString(datpostdate);
    		if (sPost.contains("0000-00-00")){
    			m_datpostdate = null;
    		}else{
    			try {
					m_datpostdate = clsDateAndTimeConversions.StringTojavaSQLDate("yyyy-MM-dd HH:mm:ss", sPost);
				} catch (Exception e) {
					throw new Exception("Error reading converting date from 'sPost' = '" + sPost + "' [1423157509] - " + e.getMessage());
				}
    		}
    		m_spostingdate = clsDateAndTimeConversions.resultsetDateTimeStringToString(rs.getString(datpostdate));
			rs.close();
		}catch (SQLException ex){
			throw new Exception("Error loading from recordset [1385582782] - " + ex.getMessage());
		}
    	return;
    }
    public void load(
    		String sBatchNumber,
    		Connection conn 
    ) throws Exception{

    	if (! sBatchNumber(sBatchNumber)){
    		throw new Exception("Invalid batch number '" + sBatchNumber + "' [1385584165].");
    	}

    	String SQL = TRANSACTIONSQLs.Get_TransactionBatch_By_Number(sBatchNumber);
    	try {
    		ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn); 
    		rs.next();
    		
    		if (!sBatchDescription(rs.getString(sbatchdescription))){ rs.close();  throw new Exception("Error getting batch description [1385582789]"); };
    		if (!iBatchType(rs.getInt(ibatchtype))){ rs.close(); throw new Exception("Error getting batch type [1385582790]"); };
    		if(ARUtilities.testResultSetTSFieldForNull(rs, datlasteditdate)){
    			if (!tsLastEditDate(rs.getTimestamp(datlasteditdate))){
    				m_tslasteditdate = null;
    			};
    		}else{
    			m_tslasteditdate = null;
    		}
    		if (!sLastEditedByID(Integer.toString(rs.getInt(llasteditedbyid)))){ rs.close(); throw new Exception("Error getting last edited by [1385582791]"); };
    		if(ARUtilities.testResultSetTSFieldForNull(rs, datbatchdate)){
    			if (!tsLastEditDate(rs.getTimestamp(datbatchdate))){
    				m_tsbatchdate = null;
    			};
    		}else{
    			m_tsbatchdate = null;
    		}
    		if (!iBatchStatus(rs.getInt(ibatchstatus))){ rs.close(); throw new Exception("Error getting batch status [1385582792]"); };
    		if (!sCreatedByID(Integer.toString(rs.getInt(lcreatedbyid)))){ rs.close(); throw new Exception("Error getting batch created by [1385582793]"); };
    		m_ibatchlastentry = rs.getInt(ibatchlastentry);
    		if (!sModuleType(rs.getString(smoduletype))){ rs.close(); throw new Exception("Error getting module type [1385582794]"); };
    		String sPost = rs.getString(datpostdate);
    		if (sPost.contains("0000-00-00")){
    			m_datpostdate = null;
    		}else{
    			try {
					m_datpostdate = clsDateAndTimeConversions.StringTojavaSQLDate("yyyy-MM-dd HH:mm:ss", sPost);
				} catch (Exception e) {
					throw new Exception("Error [1423492044] reading posting date: '" + sPost + "' - " + e.getMessage() + ".");
				}
    		}
    		m_spostingdate = clsDateAndTimeConversions.resultsetDateTimeStringToString(rs.getString(datpostdate));
    		rs.close();
    	}catch (SQLException ex){
    		throw new Exception("Error [1385582795] loading from recordset - " + ex.getMessage());
    	}
    	return;
    }

    public void save_without_data_transaction (Connection conn, String sUserID, String sUserFullName) throws Exception{
    	
    	String SQL = "";
    	if (m_lbatchnumber == -1){ 
	    	//Add a new batch:
    		SQL = "INSERT into " + SMEntryBatch.TableName
    		+ " (" 
    			+ SMEntryBatch.ibatchtype
    			+ ", " + SMEntryBatch.datbatchdate
    			+ ", " + SMEntryBatch.datlasteditdate
    			+ ", " + SMEntryBatch.ibatchstatus
    			+ ", " + SMEntryBatch.sbatchdescription
    			+ ", " + SMEntryBatch.lcreatedbyid
    			+ ", " + SMEntryBatch.screatedbyfullname
    			+ ", " + SMEntryBatch.llasteditedbyid
    			+ ", " + SMEntryBatch.slasteditedbyfullname
    			+ ", " + SMEntryBatch.smoduletype
    			+ ", " + SMEntryBatch.ibatchlastentry
    		+ ")"
    		+ " VALUES ("
    			+ "'" + sBatchType() + "'"
    			+ ", '" + sSQLBatchDateTimeString() + "'"
    			+ ", NOW()"
    			+ ", " + sBatchStatus()
    			+ ", '" + clsDatabaseFunctions.FormatSQLStatement(m_sbatchdescription) + "'"
    			+ ", " + sUserID + ""
    			+ ", '" + clsDatabaseFunctions.FormatSQLStatement(sUserFullName) + "'"
    			+ ", " + sUserID + ""
    			+ ", '" + clsDatabaseFunctions.FormatSQLStatement(sUserFullName) + "'"
    			+ ", '" + SMModuleTypes.AR + "'"
    			+ ", " + "0"
    		+ ")";
    	}else{
			//First, get the last entry number for this batch:
	    	SQL = TRANSACTIONSQLs.Get_Last_Entry_Number(sBatchNumber());
	    	try{
		    	ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
		    	
		    	if (rs.next()){
		    		m_ibatchlastentry = rs.getInt(SMTableentries.ientrynumber);
		    	}
		    	else{
		    		m_ibatchlastentry = 0;
		    	}		
		    	rs.close();
	        }catch (SQLException ex){
	        	throw new Exception("Error saving batch [1385584632] with SQL: " + SQL + " - " + ex.getMessage());
	        }
	        
			SQL = "UPDATE "
				+ SMEntryBatch.TableName
				+ " SET ";
			
				if(sSQLBatchDateTimeString().compareToIgnoreCase("0000-00-00") != 0){
					SQL = SQL + SMEntryBatch.datbatchdate + " = '" + sSQLBatchDateTimeString() + "', ";
				}
				
				SQL = SQL + SMEntryBatch.ibatchstatus + " = " + sBatchStatus()
				+ ", " + SMEntryBatch.sbatchdescription + " = '" + clsDatabaseFunctions.FormatSQLStatement(m_sbatchdescription) + "'"
				+ ", " + SMEntryBatch.ibatchtype + " = " + sBatchType()
				+ ", " + SMEntryBatch.datlasteditdate + " = NOW()"
				+ ", " + SMEntryBatch.ibatchlastentry + " = " + Integer.toString(m_ibatchlastentry)
				+ ", " + SMEntryBatch.llasteditedbyid + " = " + sUserID + ""
				+ ", " + SMEntryBatch.slasteditedbyfullname + " = '" + clsDatabaseFunctions.FormatSQLStatement(sUserFullName) + "'"
				+ ", " + SMEntryBatch.smoduletype + " = '" + m_smoduletype + "'";
				
				if(m_datpostdate != null){
					//SQL = SQL + ", " + SMEntryBatch.datpostdate + " = '" + getPostingDate() + "'";
					SQL = SQL + ", " + SMEntryBatch.datpostdate + " = '" + clsDateAndTimeConversions.sqlDateToString(m_datpostdate, "yyyy-MM-dd HH:mm:ss") + "'";
				}
				SQL = SQL +  " WHERE ("
					+ "(" + SMEntryBatch.ibatchnumber + " = " + sBatchNumber() + ")"
				+ ")";
    	}	
	    try {
			Statement stmt = conn.createStatement();
			stmt.execute(SQL);
		} catch (Exception e) {
			throw new Exception("Error saving batch [1385584634] with SQL: " + SQL + " - " + e.getMessage());
		}
    	
    	//If the batch was newly created, get the new batch number:
    	if (m_lbatchnumber == -1){
	    	SQL = TRANSACTIONSQLs.Get_Last_TransactionBatch();
	    	try{
		    	ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
		    	
		    	if (rs.next()){
		    		m_lbatchnumber = rs.getInt(ibatchnumber);
		    		rs.close();
		    	}
		    	else{
		    		rs.close();
		    		m_lbatchnumber = 0;
		    	}		
	        }catch (SQLException ex){
	    		throw new Exception("Error getting last batch number [138564634] " + " - " + ex.getMessage());
	        }
    	}
    	return;
    }
    public void save_with_data_transaction (ServletContext context, String sDBID, String sUserID, String sUserFullName) throws Exception{
    	
    	Connection conn = clsDatabaseFunctions.getConnection(context, sDBID, "MySQL", "SMClasses.SMEntryBatch");
    	
    	if(!clsDatabaseFunctions.start_data_transaction(conn)){
    		clsDatabaseFunctions.freeConnection(context, conn);
    		throw new Exception("Error starting data transaction [1385584931].");
    	}
    	try {
			save_without_data_transaction(conn, sUserID, sUserFullName);
			clsDatabaseFunctions.commit_data_transaction(conn);
		} catch (Exception e) {
			clsDatabaseFunctions.rollback_data_transaction(conn);
			throw new Exception("Error saving [1385584930] " + e.getMessage());
		}
    	return;
    }
    public void flag_as_deleted(
    		String sBatchNumber,
    		ServletContext context, 
    		String sDBID
    		) throws Exception{
        
    	    if (! sBatchNumber(sBatchNumber)){
    	    	throw new Exception("Invalid sBatchNumber - '" + sBatchNumber + "' [1385585222]");
    	    }
    	
    	    String SQL = TRANSACTIONSQLs.Flag_Batch_Deleted(sBatchNumber);
    		try{
    	    	if (clsDatabaseFunctions.executeSQL(SQL, context, sDBID) == false){
    	    		throw new Exception("Error flagging as deleted [1385585223]:.");
    	    	}
    	    }catch (SQLException ex){
    	    	throw new Exception("Error flagging as deleted [1385585224] with SQL: " + SQL + " - " + ex.getMessage());
    		}
        
        	return;
        }
    public void flag_as_deleted (
    		ServletContext context, 
    		String sDBID
    		) throws Exception{
        
    		try {
				flag_as_deleted (sBatchNumber(), context, sDBID);
			} catch (Exception e) {
				m_sErrorMessages += " - " + e.getMessage();
				throw new Exception(e.getMessage());
			}
        }
    //Left off converting to Exceptions here: TJR - 11/27/2013
    public boolean delete_entry_with_transaction (
    		String sBatchNumber,
    		String sEntryNumber,
    		ServletContext context, 
    		String sDBID
    		){
    	
    	//Set the batch number for our batch:
    	if (!sBatchNumber(sBatchNumber)){
    		System.out.println("Invalid batch number in delete_entry: " + sBatchNumber);
    		return false;
    	}

    	Connection conn = clsDatabaseFunctions.getConnection(context, sDBID, "MySQL", "SMClasses.SMEntryBatch");
    	
    	//We do all this in a transaction, so we can roll it back:
    	if (!clsDatabaseFunctions.start_data_transaction(conn)){
    		clsDatabaseFunctions.freeConnection(context, conn);
    		return false;
    	}
    	
    	if (!delete_entry(sBatchNumber, sEntryNumber, conn)){
    		clsDatabaseFunctions.rollback_data_transaction(conn);
    		clsDatabaseFunctions.freeConnection(context, conn);
    	}else{
    		clsDatabaseFunctions.commit_data_transaction(conn);
    		clsDatabaseFunctions.freeConnection(context, conn);
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
	
	    String SQL = TRANSACTIONSQLs.Delete_Transaction_Entry(sBatchNumber(), sEntryNumber);
		try{
	    	if (clsDatabaseFunctions.executeSQL(SQL, conn) == false){
	    		return false;
	    	}
	    }catch (SQLException ex){
			System.out.println("Error in " + this.toString() + " - delete_single_entry!!");
		    System.out.println("SQLException: " + ex.getMessage());
		    System.out.println("SQLState: " + ex.getSQLState());
		    System.out.println("SQL: " + ex.getErrorCode());
		    return false;
		}
    	return true;
    }
    private boolean delete_lines_for_entry (
    		String sEntryNumber,
    		Connection conn
    		){
	
	    String SQL = TRANSACTIONSQLs.Delete_Transaction_Lines_For_Entry(sBatchNumber(), sEntryNumber);
		try{
	    	if (clsDatabaseFunctions.executeSQL(SQL, conn) == false){
	    		return false;
	    	}
	    }catch (SQLException ex){
			System.out.println("Error in " + this.toString() + " - delete_lines_for_entry!!");
		    System.out.println("SQLException: " + ex.getMessage());
		    System.out.println("SQLState: " + ex.getSQLState());
		    System.out.println("SQL: " + ex.getErrorCode());
		    return false;
		}
    	return true;
    }
    private boolean renumber_all_entries (
    		Connection conn
    		){
	
	    String SQL = "SELECT "
	    		+ SMTableentries.lid
	    		+ ", " + SMTableentries.ientrynumber
	    		+ " FROM " + SMTableentries.TableName
	    		+ " WHERE (" 
	    		+ SMTableentries.ibatchnumber + " = " + sBatchNumber()
	    		+ ")"
	    		+ " ORDER BY " + SMTableentries.ientrynumber + " ASC";
    	try{
	    	ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
	    	
	    	int iNewEntryNumber = 0;
	    	while (rs.next()){
	    		iNewEntryNumber ++;
	    		if(!renumber_entry(
	    				rs.getLong(SMTableentries.lid), 
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
    		System.out.println("Error in " + this.toString() + " class - renumbering all entries!!");
    	    System.out.println("SQLException: " + ex.getMessage());
    	    System.out.println("SQLState: " + ex.getSQLState());
    	    System.out.println("SQL: " + ex.getErrorCode());
    	    return false;
        }
    	return true;
    }
    private boolean renumber_entry (
    		long lEntryID,
    		int iNewEntryNumber,
    		Connection conn
    		){
	
	    String SQL = 
	    	TRANSACTIONSQLs.Renumber_Transaction_Entry(
	    			Long.toString(lEntryID), 
	    			Integer.toString(iNewEntryNumber));
		try{
	    	if (clsDatabaseFunctions.executeSQL(SQL, conn) == false){
	    		return false;
	    	}
	    }catch (SQLException ex){
			System.out.println("Error in " + this.toString() + " - renumber_entry!!");
		    System.out.println("SQLException: " + ex.getMessage());
		    System.out.println("SQLState: " + ex.getSQLState());
		    System.out.println("SQL: " + ex.getErrorCode());
		    return false;
		}
    	return true;
    }
    public boolean update_last_entry_number (
    		Connection conn
    		){
	
    	//Get the last entry number:
    	String sLastEntryNumber = "0";
	    String SQL = TRANSACTIONSQLs.Get_Last_Entry_Number(sBatchNumber());
    	try{
	    	ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
	    	if (rs.next()){
	    		sLastEntryNumber = Integer.toString(rs.getInt(1));
	    	}
	    	rs.close();
        }catch (SQLException ex){
    		System.out.println("Error in " + toString() + " class - getting last entry number!!");
    	    System.out.println("SQLException: " + ex.getMessage());
    	    System.out.println("SQLState: " + ex.getSQLState());
    	    System.out.println("SQL: " + ex.getErrorCode());
    	    return false;
        }
    	
	    SQL = TRANSACTIONSQLs.Update_Batch_LastEntryNumber(sBatchNumber(), sLastEntryNumber);
		try{
	    	if (clsDatabaseFunctions.executeSQL(SQL, conn) == false){
	    		return false;
	    	}
	    }catch (SQLException ex){
			System.out.println("Error in " + this.toString() + " - update_last_entry_number!!");
		    System.out.println("SQLException: " + ex.getMessage());
		    System.out.println("SQLState: " + ex.getSQLState());
		    System.out.println("SQL: " + ex.getErrorCode());
		    return false;
		}
    	return true;
    }
    private boolean renumber_all_lines (
    		String sBatchNumber,
    		Connection conn
    		){
	
	    String SQL = TRANSACTIONSQLs.Renumber_All_Lines_For_Batch(sBatchNumber);
		try{
	    	if (clsDatabaseFunctions.executeSQL(SQL, conn) == false){
	    		return false;
	    	}
	    }catch (SQLException ex){
			System.out.println("Error in " + this.toString() + " - delete_single_entry!!");
		    System.out.println("SQLException: " + ex.getMessage());
		    System.out.println("SQLState: " + ex.getSQLState());
		    System.out.println("SQL: " + ex.getErrorCode());
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
			System.out.println("Error formatting batch number from string: " + sBatchNumber + ".");
			System.out.println("Error: " + e.getMessage());
			return false;
		}
	}
	public String sBatchNumber (){
		return Long.toString(m_lbatchnumber);
	}
    public boolean tsBatchDate (String sYear, String sMonth, String sDay){
		if (! clsDateAndTimeConversions.IsValidDate(
				Integer.parseInt(sYear), 
				Integer.parseInt(sMonth) - 1,
				Integer.parseInt(sDay)
				)
		){
    		return false;
    	}else{
    		m_tsbatchdate = clsDateAndTimeConversions.StringToTimestamp("MM/dd/yyyy", sMonth + "/" + sDay + "/" + sYear);
    		return true;
    	}
    }
    public void tsBatchDate (String sTimeStamp){
    	m_tsbatchdate = clsDateAndTimeConversions.StringToTimestamp("yyyy-MM-dd HH:mm:ss", sTimeStamp);
    }
    public boolean tsBatchDate (Timestamp tsBatchDate){
		m_tsbatchdate.setTime(tsBatchDate.getTime());
		return true;
    }
    public java.sql.Timestamp tsBatchDate (){
    	return m_tsbatchdate;
    }
    public java.sql.Date sqldatBatchDate (){
    	return clsDateAndTimeConversions.TimeStampToSqlDate(m_tsbatchdate);
    }
    public Calendar calendarBatchDate (){
    	return clsDateAndTimeConversions.TimeStampToCalendar(m_tsbatchdate);
    }
    public String sStdBatchDateString (){
    	return clsDateAndTimeConversions.utilDateToString(m_tsbatchdate, "MM/dd/yyyy");
    }
    public String sSQLBatchDateString (){
    	return clsDateAndTimeConversions.utilDateToString(m_tsbatchdate, "yyyy-MM-dd");
    }
    public String sStdBatchDateTimeString (){
    	return clsDateAndTimeConversions.utilDateToString(m_tsbatchdate, "MM/dd/yyyy hh:mm:ss a");
    }
    public String sSQLBatchDateTimeString (){
    	if(m_tsbatchdate == null){
    		return "0000-00-00";
    	}else{
    		return clsDateAndTimeConversions.utilDateToString(m_tsbatchdate, "yyyy-MM-dd HH:mm:ss");
    	}
    	
    }
    public boolean setPostingDate (java.sql.Date datPostingDate){
    	
		if (! clsDateAndTimeConversions.IsValidDate(datPostingDate)){
    		return false;
    	}else{
    		m_datpostdate = datPostingDate;
    		if (m_datpostdate == null){
    			return false;
    		}else{
    			return true;
    		}
    	}
    }
    public String getsPostingDate (){
    	return m_spostingdate;
    }
    public java.sql.Date getPostingDate (){
    	if (m_datpostdate == null){
    		return null;
    	}else{
    		return m_datpostdate;
    	}
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
    		System.out.println("Error formatting batch status from string: " + sBatchStatus + ".");
    		System.out.println("Error: " + e.getMessage());
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
		//There are only three possible batch types - anything else is invalid.
		case 0: m_ibatchtype = iBatchType; return true;
		case 1: m_ibatchtype = iBatchType; return true;
		case 2: m_ibatchtype = iBatchType; return true;
		default: return false;
		}
    }
    public boolean sBatchType (String sBatchType){
    	
    	int i;
    	try{
    		i = Integer.parseInt(sBatchType);
    		switch (i){
    			//There are only four possible batch statuses - anything else is invalid.
	    		case 0: m_ibatchtype = i; return true;
	    		case 1: m_ibatchtype = i; return true;
	    		case 2: m_ibatchtype = i; return true;
    			default: return false;
    		}
    	}catch (NumberFormatException e){
    		System.out.println("Error formatting batch type from string: " + sBatchType + ".");
    		System.out.println("Error: " + e.getMessage());
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
    	return SMBatchTypes.Get_Batch_Type(m_ibatchtype);
    }
    public boolean tsLastEditDate (String sYear, String sMonth, String sDay){
		if (! clsDateAndTimeConversions.IsValidDate(
				Integer.parseInt(sYear), 
				Integer.parseInt(sMonth),
				Integer.parseInt(sDay)
				)
		){
    		return false;
    	}else{
    		m_tslasteditdate = clsDateAndTimeConversions.StringToTimestamp("MM/dd/yyyy", sMonth + "/" + sDay + "/" + sYear);
    		return true;
    	}
    }
    public boolean tsLastEditDate (Timestamp tsLastEditDate){
    	m_tslasteditdate.setTime(tsLastEditDate.getTime());
		return true;
    }
    public void tsLastEditDate (String sTimeStamp){
    	m_tslasteditdate = clsDateAndTimeConversions.StringToTimestamp("yyyy-MM-dd HH:mm:ss", sTimeStamp);
    }
    public java.sql.Timestamp tsLastEditDate (){
    	return m_tslasteditdate;
    }
    public String sStdLastEditDateString (){
    	return clsDateAndTimeConversions.utilDateToString(m_tslasteditdate, "MM/dd/yyyy");
    }
    public String sSQLLastEditDateString (){
    	return clsDateAndTimeConversions.utilDateToString(m_tslasteditdate, "yyyy-MM-dd");
    }
    public String sStdLastEditDateTimeString (){
    	return clsDateAndTimeConversions.utilDateToString(m_tslasteditdate, "MM/dd/yyyy hh:mm:ss a");
    }
    public String sSQLLastEditDateTimeString (){
    	return clsDateAndTimeConversions.utilDateToString(m_tslasteditdate, "yyyy-MM-dd HH:mm:ss");
    }
    public int iLastEntry(){
    	return m_ibatchlastentry;
    }
    public String sLastEntry(){
    	return Integer.toString(m_ibatchlastentry);
    }
    
    public boolean sCreatedByID(String lcreatedbyid){
    	m_lcreatedbyid = lcreatedbyid;
    	return true;
    }
    public String sCreatedByID (){
    	return m_lcreatedbyid;
    }
    
    public boolean sLastEditedByID(String llasteditedbyid){
    	m_llasteditedbyid = llasteditedbyid;
    	return true;
    }
    public String sLastEditedByID (){
    	return m_llasteditedbyid;
    } 
    
    public boolean sCreatedByFullName(String screatedbyfullname){
    	m_screatedbyfullname = screatedbyfullname;
    	return true;
    }
    public String sCreatedByFullName (){
    	return m_screatedbyfullname;
    }
    
    public boolean sLastEditedByFullName(String slasteditedbyfullname){
    m_slasteditedbyfullname = slasteditedbyfullname;
   	return true;
    }
    
    public String sLastEditedByFullName (){
    	return m_slasteditedbyfullname;
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
    		+ "SUM(" + SMTableentries.doriginalamount + ") AS batchtotal "
    		+ " FROM " + SMTableentries.TableName
    		+ " WHERE ("
    			+ SMTableentries.ibatchnumber + " = " + Long.toString(m_lbatchnumber)
    		+ ")"
    		;
    	try{
    		ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, context, sDBID);
    		if(rs.next()){
    			BigDecimal bdTotal = rs.getBigDecimal("batchtotal");
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
    		System.out.println("In SMEntryBatch: Error getting batch total amount " + e.getMessage());
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
    	m_sErrorMessages += sErrMsg;
    }
}
