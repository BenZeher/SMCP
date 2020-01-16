package smic;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import SMClasses.SMOption;
import SMDataDefinition.SMTableicpolines;
import SMDataDefinition.SMTableicporeceiptheaders;
import SMDataDefinition.SMTableicporeceiptlines;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsDateAndTimeConversions;
import ServletUtilities.clsManageBigDecimals;
import ServletUtilities.clsManageRequestParameters;
import ServletUtilities.clsMasterEntry;
import ServletUtilities.clsServletUtilities;
import smcontrolpanel.SMUtilities;

public class ICPOReceiptHeader extends clsMasterEntry{

	public static final String ParamObjectName = "Purchase Order Receipt";
	
	//Particular to the specific class
	public static final String Paramlid = "lid";
	public static final String Paramlpoheaderid = "lpoheaderid";
	public static final String Paramdatreceived = "datreceived";
	public static final String Paramsreceiptnumber = "sreceiptnumber";
	public static final String Paramlpostedtoic = "lpostedtoic";
	public static final String Paramsdeletedbyfullname= "sdeletedbyfullname";
	public static final String Paramdatdeleted = "datdeleted";
	public static final String Paramlstatus = "lstatus";
	public static final String Paramlcreatedbyid = "lcreatedbyid";
	public static final String Paramscreatedbyfullname = "screatedbyfullname";
	public static final String Paramsdattimelastupdated = "dattimelastupdated";
	public static final String Paramllastupdatedbyid = "slastupdatedbyid";
	public static final String Paramslastupdatedbyfullname = "slastupdatedbyfullname";
	public static final String Paramslastupdatedprocess = "slastupdatedprocess";
	private static final String DATE_TIME_EMPTY = "0/0/0000 00:00 AM";
	
	private String m_slid;
	private String m_spoheaderid;
	private String m_datreceived;
	private String m_sreceiptnumber;
	private String m_spostedtoic;
	private String m_sdeletedbyfullname;
	private String m_datdeleted;
	private String m_lstatus;
	private String m_lcreatedbyid;
	private String m_screatedbyfullname;
	private String m_slastedupdated;
	private String m_llastupdatedbyid;
	private String m_slastupdatedbyfullname;
	private String m_slastupdatedprocess;
	private SimpleDateFormat sdf_lastedited; 
	private boolean bDebugMode = false;
	
	public ICPOReceiptHeader() {
		super();
		initBidVariables();
        }

	ICPOReceiptHeader (HttpServletRequest req){
		super(req);
		initBidVariables();
		m_slid = clsManageRequestParameters.get_Request_Parameter(ICPOReceiptHeader.Paramlid, req).trim();
		if (m_slid.compareToIgnoreCase("") == 0){
			m_slid = "-1";
		}
		if (bDebugMode){
			System.out.println("[1579203785] In " + this.toString() + ".ICPOReceiptHeaderEntry - ICPOReceiptHeaderEntry.Paramlid = " + m_slid);
		}
		m_spoheaderid = clsManageRequestParameters.get_Request_Parameter(ICPOReceiptHeader.Paramlpoheaderid, req).trim();
		if(clsManageRequestParameters.get_Request_Parameter(
				ICPOReceiptHeader.Paramdatreceived, req).trim().compareToIgnoreCase("") != 0){
			m_datreceived = clsManageRequestParameters.get_Request_Parameter(ICPOReceiptHeader.Paramdatreceived, req).trim();
		}
		
		m_sreceiptnumber = clsManageRequestParameters.get_Request_Parameter(ICPOReceiptHeader.Paramsreceiptnumber, req).trim().toUpperCase();
		//TJR - RCPTPOSTEDSET
		m_spostedtoic = clsManageRequestParameters.get_Request_Parameter(ICPOReceiptHeader.Paramlpostedtoic, req).trim();
		if (m_spostedtoic.compareToIgnoreCase("") == 0){
			m_spostedtoic = "0";
		}
		m_sdeletedbyfullname = clsManageRequestParameters.get_Request_Parameter(ICPOReceiptHeader.Paramsdeletedbyfullname, req).trim();
		if(clsManageRequestParameters.get_Request_Parameter(
				ICPOReceiptHeader.Paramdatdeleted, req).trim().compareToIgnoreCase("") != 0){
			m_datdeleted = clsManageRequestParameters.get_Request_Parameter(ICPOReceiptHeader.Paramdatdeleted, req).trim();
		}
		m_screatedbyfullname = clsManageRequestParameters.get_Request_Parameter(ICPOReceiptHeader.Paramscreatedbyfullname, req).trim();
		m_lcreatedbyid = clsManageRequestParameters.get_Request_Parameter(ICPOReceiptHeader.Paramlcreatedbyid, req).trim();
		
		m_slastedupdated = clsManageRequestParameters.get_Request_Parameter(ICPOReceiptHeader.Paramsdattimelastupdated, req);
		m_slastupdatedbyfullname = clsManageRequestParameters.get_Request_Parameter(ICPOReceiptHeader.Paramslastupdatedbyfullname, req);
		m_llastupdatedbyid = clsManageRequestParameters.get_Request_Parameter(ICPOReceiptHeader.Paramllastupdatedbyid, req);
		m_slastupdatedprocess = clsManageRequestParameters.get_Request_Parameter(ICPOReceiptHeader.Paramslastupdatedprocess, req);
		
	}
    public boolean load (ServletContext context, String sDBID, String sUserID, String sUserFullName){
    	Connection conn = clsDatabaseFunctions.getConnection(
    			context, 
    			sDBID, 
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
    	clsDatabaseFunctions.freeConnection(context, conn, "[1547080936]");
    	return bResult;
    	
    }
    public boolean load (Connection conn){
    	return load (m_slid, conn);
    }
    private boolean load (String sPOReceiptID, Connection conn){

    	sPOReceiptID = sPOReceiptID.trim();
    	if (sPOReceiptID.compareToIgnoreCase("") == 0){
    		super.addErrorMessage("PO Receipt ID cannot be blank.");
    		return false;
    	}
		@SuppressWarnings("unused")
		long lID;
		try {
			lID = Long.parseLong(sPOReceiptID);
		} catch (NumberFormatException n) {
			super.addErrorMessage("Invalid ID: '" + sPOReceiptID + "'");
			return false;
		}
    	
		String SQL = " SELECT * FROM " + SMTableicporeceiptheaders.TableName
			+ " WHERE ("
				+ SMTableicporeceiptheaders.lid + " = " + sPOReceiptID
			+ ")";
		if (bDebugMode){
			System.out.println("[1579203790] In " + this.toString() + " - load SQL = " + SQL);
		}
		try {
			ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
			if (rs.next()) {
				//Load the variables here:
				m_slid = Long.toString(rs.getLong(SMTableicporeceiptheaders.lid));
				m_spoheaderid = Long.toString(rs.getLong(SMTableicporeceiptheaders.lpoheaderid));
				m_datreceived = clsDateAndTimeConversions.resultsetDateStringToString(
						rs.getString(SMTableicporeceiptheaders.datreceived));
				m_sreceiptnumber = rs.getString(SMTableicporeceiptheaders.sreceiptnumber).trim();
				m_spostedtoic = Long.toString(rs.getLong(SMTableicporeceiptheaders.lpostedtoic));
				m_sdeletedbyfullname = rs.getString(SMTableicporeceiptheaders.sdeletedbyfullname).trim();
				m_datdeleted = clsDateAndTimeConversions.resultsetDateStringToString(
						rs.getString(SMTableicporeceiptheaders.datdeleted));
				m_lstatus = Long.toString(rs.getLong(SMTableicporeceiptheaders.lstatus));
				m_lcreatedbyid = rs.getString(SMTableicporeceiptheaders.lcreatedbyid);
				m_screatedbyfullname = rs.getString(SMTableicporeceiptheaders.screatedbyfullname);
				try {
					m_slastedupdated = sdf_lastedited.format(rs.getTimestamp(SMTableicporeceiptheaders.dattimelastupdated));
				} catch (Exception e) {
					m_slastedupdated = DATE_TIME_EMPTY;
				}
				m_llastupdatedbyid = rs.getString(SMTableicporeceiptheaders.llastupdateuserid);
				m_slastupdatedbyfullname = rs.getString(SMTableicporeceiptheaders.slastupdateuserfullname);
				m_slastupdatedprocess = rs.getString(SMTableicporeceiptheaders.slastupdateprocess);
				rs.close();
			} else {
				super.addErrorMessage("No " + ParamObjectName + " found for : '" + sPOReceiptID
						+ "'");
				rs.close();
				return false;
			}
		} catch (SQLException e) {
			super.addErrorMessage("Error [1488488489]reading " + ParamObjectName + " for : '" + sPOReceiptID
					+ "' - " + e.getMessage());
			return false;
		}
		return true;
    }
    
    public boolean save_without_data_transaction (ServletContext context, String sDBID, String sUser, String sUserID, String sUserFullName){
    	
    	//We have to check for record concurrency OUTSIDE of a data transaction or the timestamp field ('dattimelastupdated') might be inaccurate:
        try {
 			checkRecordConcurrency(
 				context,
 			    sDBID,
 			    sUser);
 		} catch (Exception e) {
 			super.addErrorMessage(e.getMessage());
 			return false;
 		}
    	
       	Connection conn = clsDatabaseFunctions.getConnection(
    			context, 
    			sDBID, 
    			"MySQL", 
    			this.toString() + ".save_without_data_transaction - user: " + sUserID
    			+ " - "
    			+ sUserFullName
    			);
    	
    	if (conn == null){
    		super.addErrorMessage("Error opening data connection.");
    		return false;
    	}
    	if (!clsDatabaseFunctions.start_data_transaction(conn)){
    		super.addErrorMessage("Error starting data transaction.");
    		clsDatabaseFunctions.freeConnection(context, conn, "[1547080937]");
    		return false;
    	}
    	if(!save (conn, sUserFullName, sUserID)){
    		clsDatabaseFunctions.rollback_data_transaction(conn);
    		clsDatabaseFunctions.freeConnection(context, conn, "[1547080938]");
    		return false;
    	}
    	if (!clsDatabaseFunctions.commit_data_transaction(conn)){
    		clsDatabaseFunctions.rollback_data_transaction(conn);
    		clsDatabaseFunctions.freeConnection(context, conn, "[1547080939]");
    		super.addErrorMessage("Could not commit data transaction.");
    		return false;
    	}
    	clsDatabaseFunctions.freeConnection(context, conn, "[1547080940]");
    	return true;	
    	
    }
    private boolean save (Connection conn, String sUserFullName, String sUserID){
    	
    	if (!validate_entry_fields(conn, sUserID)){
    		return false;
    	}
    	String SQL = "";
    	ResultSet rs;
		//If it's a new record, do an insert:
		if (m_slid.compareToIgnoreCase("-1") == 0){
			SQL = "INSERT INTO " + SMTableicporeceiptheaders.TableName + " ("
			+ SMTableicporeceiptheaders.datreceived
			+ ", " + SMTableicporeceiptheaders.lpoheaderid
			+ ", " + SMTableicporeceiptheaders.lpostedtoic
			+ ", " + SMTableicporeceiptheaders.sreceiptnumber
			+ ", " + SMTableicporeceiptheaders.screatedbyfullname
			+ ", " + SMTableicporeceiptheaders.lcreatedbyid
			+ ", " + SMTableicporeceiptheaders.dattimelastupdated
			+ ", " + SMTableicporeceiptheaders.slastupdateprocess
			+ ", " + SMTableicporeceiptheaders.slastupdateuserfullname
			+ ", " + SMTableicporeceiptheaders.llastupdateuserid
			
			+ ") VALUES ( "
			+ "'" + clsDateAndTimeConversions.stdDateStringToSQLDateString(m_datreceived) + "'"
			+ ", " + m_spoheaderid
			+ ", 0" //If it's the first insert, then it can't be posted
			+ ", '" + clsDatabaseFunctions.FormatSQLStatement(m_sreceiptnumber.trim()) + "'"
			+ ", '" + clsDatabaseFunctions.FormatSQLStatement(sUserFullName) + "'"
			+ ", " + sUserID
			+ ", NOW()"
			+ ", '" + SMTableicporeceiptheaders.UPDATE_PROCESS_INSERTINGRECORD + "'"
			+ ", '" + clsDatabaseFunctions.FormatSQLStatement(sUserFullName) + "'"
			+ ", " + sUserID
			+ ")"
			;
		}else{
			//If the record is already posted, we cannot change the posting state on it:
			//TJR ???
			SQL = "UPDATE " + SMTableicporeceiptheaders.TableName + " SET "
			+ SMTableicporeceiptheaders.datreceived + " = '" 
				+ clsDateAndTimeConversions.stdDateStringToSQLDateString(m_datreceived) + "'"
			+ ", " + SMTableicporeceiptheaders.lpoheaderid
				+ " = " + m_spoheaderid
			+ ", " + SMTableicporeceiptheaders.lpostedtoic
				+ " = IF(" + SMTableicporeceiptheaders.lpostedtoic + " = 1, 1, " + m_spostedtoic + ")"
			+ ", " + SMTableicporeceiptheaders.sreceiptnumber
				+ " = '" + clsDatabaseFunctions.FormatSQLStatement(m_sreceiptnumber.trim()) + "'"
				
			+ ", " + SMTableicporeceiptheaders.dattimelastupdated + " = NOW()"
			+ ", " + SMTableicporeceiptheaders.slastupdateprocess + " = '" 
				+ SMTableicporeceiptheaders.UPDATE_PROCESS_UPDATINGRECORD + "'"
			+ ", " + SMTableicporeceiptheaders.slastupdateuserfullname + " = '" 
				+ clsDatabaseFunctions.FormatSQLStatement(sUserFullName) + "'"
			+ ", " + SMTableicporeceiptheaders.llastupdateuserid + " = '" 
				+ sUserID + "'"
			+ " WHERE ("
				+ "(" + SMTableicporeceiptheaders.lid + " = " + m_slid + ")"
			+ ")"
		;
		}

		if (bDebugMode){
			System.out.println("[1579203804] In " + this.toString() + " - save SQL = " + SQL);
		}
		try{
		    Statement stmt = conn.createStatement();
		    stmt.executeUpdate(SQL);
		}catch (Exception ex) {
    		//System.out.println(this.toString() + "Could not insert/update " + ParamObjectName 
    		//		+ " - " + ex.getMessage() + ".<BR>");
    		super.addErrorMessage("Could not insert/update " + ParamObjectName + " with SQL: " + SQL
    				+ " - " + ex.getMessage());
    		return false;
		}
    	
    	//If it's a NEW record, get the last insert ID:
    	if (m_slid.compareToIgnoreCase("-1") == 0){
    			SQL = "SELECT LAST_INSERT_ID()";
    			try {
    				rs = clsDatabaseFunctions.openResultSet(SQL, conn);
    				if (rs.next()) {
    					m_slid = Long.toString(rs.getLong(1));
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
    	}
    	return true;
    }

    public boolean delete (ServletContext context, String sDBID, String sUserID, String sUserFullName){
    	
    	Connection conn = clsDatabaseFunctions.getConnection(
    			context, 
    			sDBID, 
    			"MySQL", 
    			this.toString() + ".delete - user: " + sUserID
    			+ " - "
    			+ sUserFullName
    			);
    	
    	if (conn == null){
    		super.addErrorMessage("Error opening data connection.");
    		return false;
    	}
    	
    	if (!clsDatabaseFunctions.start_data_transaction(conn)){
    		super.addErrorMessage("Could not start data transaction.");
    		clsDatabaseFunctions.freeConnection(context, conn, "[1547081130]");
    		return false;
    	}
    	if(!delete (conn, sUserFullName, sUserID)){
    		clsDatabaseFunctions.rollback_data_transaction(conn);
    		clsDatabaseFunctions.freeConnection(context, conn, "[1547081131]");
    		return false;
    	}
    	
    	if (!clsDatabaseFunctions.commit_data_transaction(conn)){
    		clsDatabaseFunctions.rollback_data_transaction(conn);
    		clsDatabaseFunctions.freeConnection(context, conn, "[1547081132]");
    		super.addErrorMessage("Could not commit data transaction.");
    		return false;
    	}
    	clsDatabaseFunctions.freeConnection(context, conn, "[1547081134]");
    	return true;
    	
    }
    public boolean delete (Connection conn, String sUserFullName, String sUserID){
    	
    	//We don't actually delete receipts, we just flag them as deleted
    	if (m_spostedtoic.compareToIgnoreCase("0") != 0){
    		super.addErrorMessage("Purchase order receipts cannot be deleted if they have been posted to"
    				+ " inventory.");
    		return false;
    	}

    	String SQL = "UPDATE " + SMTableicporeceiptheaders.TableName
    		+ " SET " + SMTableicporeceiptheaders.datdeleted + " = NOW()"
    		+ ", " + SMTableicporeceiptheaders.sdeletedbyfullname + " = '" + clsDatabaseFunctions.FormatSQLStatement(sUserFullName) + "'"
    		+ ", " + SMTableicporeceiptheaders.lstatus + " = " + Integer.toString(SMTableicporeceiptheaders.STATUS_DELETED)
    		+ ", " + SMTableicporeceiptheaders.dattimelastupdated + " = NOW()"
    		+ ", " + SMTableicporeceiptheaders.slastupdateprocess + " = '" 
    			+ SMTableicporeceiptheaders.UPDATE_PROCESS_MARKEDASDELETED + "'"
    		+ ", " + SMTableicporeceiptheaders.slastupdateuserfullname + " = '" + sUserFullName + "'"
    		+ ", " + SMTableicporeceiptheaders.llastupdateuserid + " = " + sUserID + ""
    		+ " WHERE ("
    			+ "(" + SMTableicporeceiptheaders.lid + " = " + m_slid + ")"
    			+ " AND (" + SMTableicporeceiptheaders.lstatus + " != " + Integer.toString(SMTableicporeceiptheaders.STATUS_DELETED) + ")"
    			+ " AND (" + SMTableicporeceiptheaders.lpostedtoic + " = 0)"
    		+ ")"
    		;
    	int iRecordsAffected = 0;
    	try {
			Statement stmt = conn.createStatement();
			iRecordsAffected = stmt.executeUpdate(SQL);
		} catch (SQLException e) {
			super.addErrorMessage("Error [1422998916] flagging receipt as deleted - " + e.getMessage() + ".");
			return false;
		}
    	if (iRecordsAffected != 1){
    		super.addErrorMessage("Error [1422998917] receipt ID '" + m_slid + "' was not flagged as deleted, possibly because it was already posted.");
    		return false;
    	}
    	
		SQL = "SELECT " + SMTableicporeceiptlines.lid + " FROM " + SMTableicporeceiptlines.TableName
			+ " WHERE ("
				+ "(" + SMTableicporeceiptlines.lreceiptheaderid + " = " + getsID() + ")"
			+ ")"
		;
		//System.out.println("[1544555703] - SQL='" + SQL + "'.");
		try {
			ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
			while (rs.next()){
				ICPOReceiptLine recptline = new ICPOReceiptLine();
				String sReceiptLineID = Long.toString(rs.getLong(SMTableicporeceiptlines.lid));
				recptline.setsID(sReceiptLineID);
				if (!recptline.load(conn)){
					super.addErrorMessage("Could not load receipt line with ID " 
						+ sReceiptLineID + " - " + recptline.getErrorMessages());
					rs.close();
					return false;
				}
				recptline.setsextendedcost("0.00");
				recptline.setsqtyreceived("0.00");
				if (!recptline.save_without_data_transaction(conn, sUserFullName, true)){
					super.addErrorMessage("Could not save updated receipt line with ID " 
							+ sReceiptLineID + " - " + recptline.getErrorMessages());
						rs.close();
						return false;
				}
			}
			rs.close();
		} catch (SQLException e) {
			super.addErrorMessage("Error [1422998918] updating receipt line to zero values " 
					+ " - " + e.getMessage());
				return false;
		}
		
		ICPOHeader pohead = new ICPOHeader();
		pohead.setsID(getspoheaderid());
		if (!pohead.updatePOStatus(conn)){
			super.addErrorMessage("Error [1422998919] updating PO status - " + pohead.getErrorMessages());
			return false;
		}		
		if (!load(conn)){
			super.addErrorMessage("Error [1422998920] Could not reload PO receipt after saving.");
			return false;
		}
		return true;
    }

    public boolean validate_entry_fields (Connection conn, String sUserID){
        //Validate the entries here:
    	boolean bEntriesAreValid = true;

    	long lID;
		try {
			lID = Long.parseLong(m_slid);
		} catch (NumberFormatException e) {
        	super.addErrorMessage("Invalid PO receipt header ID: '" + m_slid + "'.");
        	bEntriesAreValid = false;
        	return bEntriesAreValid;
		}
    	
    	if (lID < -1){
        	super.addErrorMessage("Invalid PO receipt header ID: '" + m_slid + "'.");
        	bEntriesAreValid = false;
    	}
    	
		try {
			lID = Long.parseLong(m_spoheaderid);
		} catch (NumberFormatException e) {
        	super.addErrorMessage("Invalid PO header ID: '" + m_spoheaderid + "'.");
        	bEntriesAreValid = false;
		}
    	
    	if (lID < -1){
        	super.addErrorMessage("Invalid PO header ID: '" + m_spoheaderid + "'.");
        	bEntriesAreValid = false;
    	}

    	//date received:
        if (m_datreceived.compareTo(EMPTY_DATE_STRING) != 0){
	        if (!clsDateAndTimeConversions.IsValidDateString("M/d/yyyy", m_datreceived)){
	        	super.addErrorMessage("Receipt date '" + m_datreceived + "' is invalid.  ");
	        	bEntriesAreValid = false;
	        }
        }
        
        //Make sure date received is in the currentposting period:
        SMOption opt = new SMOption();
        if (!opt.load(conn)){
        	super.addErrorMessage("Error [1457652007] loading SM Options to check posting date range - " + opt.getErrorMessage() + ".");
        	bEntriesAreValid = false;
        }
        try {
			opt.checkDateForPosting(getsdatreceived(), "IC Receipt Date", conn, sUserID);
		} catch (Exception e) {
        	super.addErrorMessage("Error [1457652008] " + e.getMessage());
        	bEntriesAreValid = false;
		}
        
        if (m_sreceiptnumber.length() > SMTableicporeceiptheaders.sreceiptnumberLength){
        	super.addErrorMessage("Receipt Number is too long.");
        	bEntriesAreValid = false;
        }

    	if (
    			(m_spostedtoic.compareToIgnoreCase("0") != 0)
    			&& (m_spostedtoic.compareToIgnoreCase("1") != 0)
    	){
        	super.addErrorMessage("Posted to ic value '" + m_spostedtoic + ".");
        	bEntriesAreValid = false;
    	}
        if (m_screatedbyfullname.length() > SMTableicporeceiptheaders.screatedbyLength){
        	super.addErrorMessage("Receipt by is too long.");
        	bEntriesAreValid = false;
        }
        
        return bEntriesAreValid;
    }

    public void checkRecordConcurrency(
    	ServletContext context,
    	String sDBID,
    	String sUserFullName
    	)throws Exception{
    	
    	Connection conn = null;
    	try {
			conn = clsDatabaseFunctions.getConnectionWithException(
				context, 
				sDBID, 
				"MySQL", 
				SMUtilities.getFullClassName(this.toString()) + ".checkRecordConcurrency - user: " + sUserFullName
				);
		} catch (Exception e) {
			throw new Exception("Error [1488388811] getting connection - " + e.getMessage());
		}
    	
        //Check to make sure that this record has the SAME time stamp as the most recent record in the database - if not, then
        //someone must have changed it since the current user retrieved it to his screen:
        //We only have to check for concurrency if it's an EXISTING receipt"
        if (m_slid.compareToIgnoreCase("-1") != 0){
	        String SQL = "SELECT"
	        	+ " " + SMTableicporeceiptheaders.dattimelastupdated
	        	+ ", " + SMTableicporeceiptheaders.slastupdateprocess
	        	+ ", " + SMTableicporeceiptheaders.slastupdateuserfullname
	        	+ " FROM " + SMTableicporeceiptheaders.TableName
	        	+ " WHERE ("
	        		+ "(" + SMTableicporeceiptheaders.lid + " = " + m_slid + ")"
	        	+ ")"
	        ;
	        try {
				ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
				if (rs.next()){
					String sdattimeLastUpdated = "";
					//sdf_lastedited = new SimpleDateFormat("M/d/yyyy hh:mm:ss a");
					try {
						sdattimeLastUpdated = sdf_lastedited.format(rs.getTimestamp(SMTableicporeceiptheaders.dattimelastupdated));
					} catch (Exception e) {
						System.out.println("[1488553245] - error - " + e.getMessage());
						sdattimeLastUpdated = DATE_TIME_EMPTY;
					}
					//System.out.println("[1488553242] - NOW = " + sdf_lastedited.format(System.currentTimeMillis()));
					//System.out.println("[1488553243] - SQL = " + SQL);
					//System.out.println("[1488553244] - rs.getString(SMTableicporeceiptheaders.dattimelastupdated) = '" + rs.getString(SMTableicporeceiptheaders.dattimelastupdated) + "'");
					//System.out.println("[1488553245] - rs.getTimestamp(SMTableicporeceiptheaders.dattimelastupdated) = '" + rs.getTimestamp(SMTableicporeceiptheaders.dattimelastupdated) + "',"
					//	+ " sdf_lastedited.toPattern() = '" + sdf_lastedited.toPattern() + "',"
					//	+ " sdf_lastedited.toString() = '" + sdf_lastedited.toString() + "',"
					//	+ " sdf_lastedited.format(rs.getTimestamp(SMTableicporeceiptheaders.dattimelastupdated))" + sdf_lastedited.format(rs.getTimestamp(SMTableicporeceiptheaders.dattimelastupdated)) + "'"
					//	+ " rs.getTimestamp(SMTableicporeceiptheaders.dattimelastupdated) = '" + rs.getTimestamp(SMTableicporeceiptheaders.dattimelastupdated) + "'"
					//	+ " rs.getDate(SMTableicporeceiptheaders.dattimelastupdated) = '" + rs.getDate(SMTableicporeceiptheaders.dattimelastupdated) + "'"
					//);
					if (sdattimeLastUpdated.compareToIgnoreCase(getsdattimelastupdated()) != 0){
						clsDatabaseFunctions.freeConnection(context, conn, "[1547080925]");
						String sConcurrencyWarning = "Error [1488389088] - This receipt has been updated (function: '" + rs.getString(SMTableicporeceiptheaders.slastupdateprocess) + "')"
							+ " by user '" + rs.getString(SMTableicporeceiptheaders.slastupdateuserfullname) + "'"
							+ " on " + sdattimeLastUpdated
							+ " since you (user '" + sUserFullName + "') started editing it (the previous version, which you had on your screen, had been saved " + getsdattimelastupdated()
							+ " by user '" + getslastupdatedbyfullname() + "'"
							+ "), so"
							+ " it cannot be saved.  Start over and refresh the receipt before you try to edit it.'"
						;
						//System.out.println(sConcurrencyWarning);
						throw new Exception(sConcurrencyWarning);
					}
				}
				rs.close();
			} catch (SQLException e) {
				clsDatabaseFunctions.freeConnection(context, conn, "[1547080926]");
				throw new Exception("Error [1488313112] reading current receipt record with SQL: '" + SQL + "' - " + e.getMessage());
			}
        }
    	clsDatabaseFunctions.freeConnection(context, conn, "[1547080927]");
    	return;
    }

    public void checkToReceiveAllOutstandingLines(String sDBID,
   		String sUserID,
   		String sUserFullName,
   		ServletContext context)throws Exception{
 
    	//Check concurrency - has the record in the database changed since we read it?
    	//System.out.println("[1488561558] - checking concurrency in the 'receiveAllOutstandingLines' function:");
    	try {
			checkRecordConcurrency(context, sDBID, sUserFullName);
		} catch (Exception e1) {
			throw new Exception(e1.getMessage());
		}
    	
    	String SQL = "SELECT"
        		+ " " + SMTableicpolines.lid
        		+ " FROM " + SMTableicpolines.TableName
        		+ " WHERE ("
        			+ "(" + SMTableicpolines.lpoheaderid + " = " + getspoheaderid() + ")"
        			+ " AND (" + SMTableicpolines.bdqtyordered + " > " + SMTableicpolines.bdqtyreceived + ")"
        		+ ")"
        		+ " ORDER BY " + SMTableicpolines.llinenumber
        		;
    	//System.out.println("[1445536480] - SQL = '" + SQL + "'");
    	Connection conn;
		try {
			conn = clsDatabaseFunctions.getConnectionWithException(
				context, 
				sDBID, 
				"MySQL", 
				this.toString() + ".receiveAllOutstandingLines [1386787980] - user: " 
				+ sUserID
				+ " - "
				+ sUserFullName
				
				);
		} catch (Exception e1) {
			throw new Exception("Error [1529953932] - Could not get connection to receive all outstanding lines - " + e1.getMessage());
		}
    	
    	if (!clsDatabaseFunctions.start_data_transaction(conn)){
    		clsDatabaseFunctions.freeConnection(context, conn, "[1547080928]");
    		throw new Exception("Could not start data transaction to receive all outstanding lines.");
    	}
		
    	try {
    		ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
			while (rs.next()){
				receiveQtyRemainingOnPOLine(Long.toString(rs.getLong(SMTableicpolines.lid)), conn, sUserFullName, sUserID);
			}
			rs.close();
		} catch (Exception e) {
			clsDatabaseFunctions.rollback_data_transaction(conn);
			clsDatabaseFunctions.freeConnection(context, conn, "[1547080929]");
			throw new Exception("Error in " + SMUtilities.getFullClassName(this.toString()) 
				+ ".receiveAllOutstandingLines - user: " + sUserFullName + " - " + e.getMessage());
		}
    	
    	//Update the 'updated' fields on the receipt header:
    	try {
			save(conn, sUserFullName, sUserID);
		} catch (Exception e) {
    		clsDatabaseFunctions.freeConnection(context, conn, "[1547080930]");
    		throw new Exception("Could not save " + ParamObjectName + " - " + e.getMessage());
		}
    	
    	if (!clsDatabaseFunctions.commit_data_transaction(conn)){
			clsDatabaseFunctions.rollback_data_transaction(conn);
			clsDatabaseFunctions.freeConnection(context, conn, "[1547080931]");
			throw new Exception("Error in " + SMUtilities.getFullClassName(this.toString()) 
				+ ".receiveAllOutstandingLines - user: " + sUserFullName + " - could not commit data transaction");
    	}
    	clsDatabaseFunctions.freeConnection(context, conn, "[1547080932]");
    }
    
    public void checkToReceiveQtyRemainingOnPOLine(
    		String sPOLineID,
    		ServletContext context,
    		String sDBID,
    		String sUserFullName,
    		String sUserID
    		) throws Exception{
    	Connection conn = clsDatabaseFunctions.getConnectionWithException(
    		context, 
    		sDBID, 
    		"MySQL", 
    		SMUtilities.getFullClassName(this.toString() + ":receiveQtyRemainingOnPOLine - user: " + sUserFullName));
		//Check concurrency:
		//System.out.println("[1488561559] - checking concurrency in the 'receiveQtyRemainingOnPOLine' function:");
    	
		try {
			checkRecordConcurrency(context, sDBID, sUserFullName); 
		} catch (Exception e1) {
			clsDatabaseFunctions.freeConnection(context, conn, "[1547080933]");
			throw new Exception("Error [1488322597] - " + e1.getMessage());
		}
		
    	try {
			receiveQtyRemainingOnPOLine(sPOLineID, conn, sUserFullName, sUserID);
		} catch (Exception e) {
			clsDatabaseFunctions.freeConnection(context, conn, "[1547080934]");
			throw new Exception(e.getMessage());
		}
    	clsDatabaseFunctions.freeConnection(context, conn, "[1547080935]");
    	
    	return;
    }
    
    //When this function is called, the 'check and update IC posting flag should have ALREADY been called, and it will be reset when it finishes:
    public void receiveQtyRemainingOnPOLine(
    	String sPOLineID,
    	Connection conn,
    	String sUserFullName,
    	String sUserID
    ) throws Exception{
    	
		//Make sure we're not posting:
		
		ICPOLine poline = new ICPOLine();
		poline.setsID(sPOLineID);
		if (!poline.load(conn)){
			throw new Exception("Could not load po line with ID " + sPOLineID + " " + poline.getErrorMessages());
		}
       	
		BigDecimal bdQtyOrdered = new BigDecimal(poline.getsqtyordered().replace(",", ""));
		BigDecimal bdQtyReceived = new BigDecimal(poline.getsqtyreceived().replace(",", ""));
		BigDecimal bdExtendedOrderCost = new BigDecimal(poline.getsextendedordercost().replace(",", ""));
		BigDecimal bdExtendedReceivedCost = new BigDecimal(poline.getsextendedreceivedcost().replace(",", ""));
		BigDecimal bdQtyRemaining = new BigDecimal(0);
		bdQtyRemaining = bdQtyOrdered.subtract(bdQtyReceived);
		if (bdQtyRemaining.compareTo(BigDecimal.ZERO) < 0){
			bdQtyRemaining = BigDecimal.ZERO;
		}
		BigDecimal bdCostRemaining = new BigDecimal(0);
		bdCostRemaining = bdExtendedOrderCost.subtract(bdExtendedReceivedCost);
		if (bdCostRemaining.compareTo(BigDecimal.ZERO) < 0){
			bdCostRemaining = BigDecimal.ZERO;
		}
		//Try to get a line on this receipt that already points to this po line:
		//If this receipt is already posted to IC, then we can't be updating it - that's controlled
		//by the edit screen restrictions and logic
		long lPOReceiptLineID = -1;
		String SQL = "SELECT"
			+ " " + SMTableicporeceiptlines.lid
			+ " FROM " + SMTableicporeceiptlines.TableName
			+ " WHERE ("
				+ "(" + SMTableicporeceiptlines.lpolineid + " = " + sPOLineID + ")"
				+ " AND (" + SMTableicporeceiptlines.lreceiptheaderid + " = " + this.getsID() + ")"
			+ ")"
			;
		if (bDebugMode){
			System.out.println("[1579203823] In " + this.toString() + ".receivePOLine - SQL 1 = " + SQL);
		}
		try {
			ResultSet rs = clsDatabaseFunctions.openResultSet(
				SQL, 
				conn
			);
			if (rs.next()){
				lPOReceiptLineID = rs.getLong(SMTableicporeceiptlines.lid);
			}
			rs.close();
		} catch (SQLException e) {
			throw new Exception("Error [1488322639] reading PO receipt lines in .receivePOLine - " + e.getMessage());
		}
		//Load up the po receipt line:
		ICPOReceiptLine receiptline = new ICPOReceiptLine();
		receiptline.setsID(Long.toString(lPOReceiptLineID));
		if (lPOReceiptLineID != -1){
			if (bDebugMode){
				System.out.println("[1579203828] In " + this.toString() + ".receivePOLine - lPOReceiptLine != -1");
			}
			if(!receiptline.load(conn)){
				throw new Exception("Error [1488322724] loading PO receipt line in .receivePOLine - " 
						+ receiptline.getErrorMessages());
			}else{
				BigDecimal bdCurrentCost = new BigDecimal(receiptline.getsextendedcost().replace(",", ""));
				receiptline.setsextendedcost(clsManageBigDecimals.BigDecimalToScaledFormattedString(
					SMTableicporeceiptlines.bdextendedcostScale, 
					bdCurrentCost.add(bdCostRemaining)));
				BigDecimal bdCurrentReceived = new BigDecimal(receiptline.getsqtyreceived().replace(",", ""));
				receiptline.setsqtyreceived(clsManageBigDecimals.BigDecimalToScaledFormattedString(
						SMTableicporeceiptlines.bdqtyreceivedScale, 
						bdCurrentReceived.add(bdQtyRemaining)));
			}
			
		}else{
			if (bDebugMode){
				System.out.println("[1579203833] In " + this.toString() + ".receivePOLine - lPOReceiptLine is new");
			}
			receiptline.setsglexpenseacct(poline.getsglexpenseacct());
			receiptline.setsextendedcost(clsManageBigDecimals.BigDecimalToScaledFormattedString(
				SMTableicporeceiptlines.bdextendedcostScale, bdCostRemaining));
			receiptline.setsitemdescription(poline.getsitemdescription());
			receiptline.setsitemnumber(poline.getsitemnumber());
			receiptline.setslocation(poline.getslocation());
			receiptline.setsnoninventoryitem(poline.getsnoninventoryitem());
			receiptline.setspolineid(sPOLineID);
			receiptline.setsqtyreceived(
				clsManageBigDecimals.BigDecimalToScaledFormattedString(
					SMTableicporeceiptlines.bdqtyreceivedScale, bdQtyRemaining));
			receiptline.setsreceiptheaderid(this.getsID());
			receiptline.setsunitcost(poline.getsunitcost());
			if (bDebugMode){
				System.out.println("[1579203836] In " + this.toString() 
					+ ".receivePOLine - poline.getsunitofmeasure() = " + poline.getsunitofmeasure());
			}
			receiptline.setsunitofmeasure(poline.getsunitofmeasure());
		}
				
		//Now we have the receipt line all loaded - we have to save it:
		if (!receiptline.save_without_data_transaction(conn, sUserFullName, false)){
			throw new Exception("Error [1488322640] - Could not save receipt line - " + receiptline.getErrorMessages());
		}
		
		//Finally, update the po line:
		//Use the po line function for receiving a line:
		if (!poline.receiveLine(bdQtyRemaining, bdCostRemaining)){
			throw new Exception("Error [1488322641] receiving PO line after updating - " + poline.getErrorMessages());
		}
		//Now save the po line:
		if (!poline.save_without_data_transaction(conn, sUserFullName)){
			throw new Exception("Error [1488322642] saving PO line after updating - " + poline.getErrorMessages());
		}
		
		//Save the entry to update the last updated info:
		try {
			save(conn, sUserFullName, sUserID);
		} catch (Exception e) {
			throw new Exception("Error [1488322643] updating " + ParamObjectName + " - " + this.getErrorMessages());
		}
    	return;
    }
    public String read_out_debug_data(){
    	String sResult = "  ** ICPOReceiptHeader read out: ";
    	sResult += "\nID: " + this.getsID();
    	sResult += "\nPO header ID: " + this.getspoheaderid();
    	sResult += "\nDate received: " + this.getsdatreceived();
    	sResult += "\nReceipt number: " + this.getsreceiptnumber();
    	sResult += "\nPosted to ic: " + this.getspostedtoic();
    	sResult += "\nDeleted by: " + this.getsdeletedby();
    	sResult += "\nDate deleted: " + this.getsdatdeleted();
    	sResult += "\nStatus: " + this.getsstatus();
    	sResult += "\nReceived by: " + this.getscreatedbyfullname();
    	return sResult;
    }

    public void addErrorMessage(String sMsg){
    	super.addErrorMessage(sMsg);
    }
	public String getQueryString(){
		//Particular to the specific class
		String sQueryString = "";
		sQueryString += ParamObjectName + "=" + clsServletUtilities.URLEncode(this.getObjectName());
		sQueryString += "&" + Paramlid + "=" + clsServletUtilities.URLEncode(this.getsID());
		sQueryString += "&" + Paramdatreceived + "=" + clsServletUtilities.URLEncode(getsdatreceived());
		sQueryString += "&" + Paramlpoheaderid + "=" + clsServletUtilities.URLEncode(getspoheaderid());
		sQueryString += "&" + Paramlpostedtoic + "=" + clsServletUtilities.URLEncode(getspostedtoic());
		sQueryString += "&" + Paramsreceiptnumber + "=" + clsServletUtilities.URLEncode(getsreceiptnumber());
		sQueryString += "&" + Paramsdeletedbyfullname + "=" + clsServletUtilities.URLEncode(getsdeletedby());
		sQueryString += "&" + Paramdatdeleted + "=" + clsServletUtilities.URLEncode(getsdatdeleted());
		sQueryString += "&" + Paramlstatus + "=" + clsServletUtilities.URLEncode(getsstatus());
		sQueryString += "&" + Paramscreatedbyfullname + "=" + clsServletUtilities.URLEncode(getscreatedbyfullname());
		sQueryString += "&" + Paramlcreatedbyid + "=" + clsServletUtilities.URLEncode(getlcreatedbyid());
		sQueryString += "&" + Paramsdattimelastupdated + "=" + clsServletUtilities.URLEncode(getsdattimelastupdated());
		sQueryString += "&" + Paramslastupdatedbyfullname + "=" + clsServletUtilities.URLEncode(getslastupdatedbyfullname());
		sQueryString += "&" + Paramllastupdatedbyid + "=" + clsServletUtilities.URLEncode(getllastupdatedbyid());
		sQueryString += "&" + Paramslastupdatedprocess + "=" + clsServletUtilities.URLEncode(getslastupdatedprocess());
		return sQueryString;
	}

	public String getsID() {
		return m_slid;
	}
	public void setsID(String sID) {
		this.m_slid = sID;
	}
	public String getsdatreceived() {
		return m_datreceived;
	}
	public void setsdatreceived(String sdatreceived) {
		m_datreceived = sdatreceived;
	}
	public void setspoheaderid(String spoheaderid) {
		m_spoheaderid = spoheaderid;
	}
	public String getspoheaderid() {
		return m_spoheaderid;
	}
	public void setspostedtoic(String spostedtoic) {
		m_spostedtoic = spostedtoic;
	}
	public String getspostedtoic() {
		return m_spostedtoic;
	}
	public void setsreceiptnumber(String sreceiptnumber) {
		m_sreceiptnumber = sreceiptnumber;
	}
	public String getsreceiptnumber(){
		return m_sreceiptnumber;
	}
	public String getsdeletedby(){
		return m_sdeletedbyfullname;
	}
	public String getsdatdeleted(){
		return m_datdeleted;
	}
	public String getsstatus(){
		return m_lstatus;
	}
	public void setscreatedbyfullname(String screatedbyfullname) {
		m_screatedbyfullname = screatedbyfullname;
	}
	public String getscreatedbyfullname(){
		return m_screatedbyfullname;
	}
	public void setlcreatedbyid(String lcreatedbyid) {
		m_lcreatedbyid = lcreatedbyid;
	}
	public String getlcreatedbyid(){
		return m_lcreatedbyid;
	}
	public void setsdattimelastupdated(String sdattimelastupdated) {
		m_slastedupdated = sdattimelastupdated;
	}
	public String getsdattimelastupdated(){
		return m_slastedupdated;
	}
	public void setslastupdatedbyfullname(String slastupdatedbyfullname) {
		m_slastupdatedbyfullname = slastupdatedbyfullname;
	}
	public String getslastupdatedbyfullname(){
		return m_slastupdatedbyfullname;
	}
	public void setllastupdatedbyid(String llastupdatedbyid) {
		m_slastupdatedbyfullname = llastupdatedbyid;
	}
	public String getllastupdatedbyid(){
		return m_llastupdatedbyid;
	}
	public void setslastupdatedprocess(String slastupdatedprocess) {
		m_slastupdatedprocess = slastupdatedprocess;
	}
	public String getslastupdatedprocess(){
		return m_slastupdatedprocess;
	}
    private void initBidVariables(){
    	m_slid = "-1";
    	m_spoheaderid = "-1";
    	m_datreceived  = clsDateAndTimeConversions.now("MM/dd/yyyy");
    	m_sreceiptnumber = "";
    	//TJR - RCPTPOSTEDSET
    	m_spostedtoic = "0";
    	m_sdeletedbyfullname = "";
    	m_datdeleted = EMPTY_DATETIME_STRING;
    	m_lstatus = Integer.toString(SMTableicporeceiptheaders.STATUS_ENTERED);
    	m_screatedbyfullname= "";
    	m_lcreatedbyid= "0";
    	sdf_lastedited = new SimpleDateFormat("M/d/yyyy hh:mm:ss a");
		super.initVariables();
		super.setObjectName(ParamObjectName);
    }
    public boolean updateLineNumbersAfterLineDeletion(Connection conn){
    	String rsSQL = "SELECT"
    		+ " " + SMTableicporeceiptlines.lid
    		+ " FROM " + SMTableicporeceiptlines.TableName
    		+ " WHERE ("
    			+ "(" + SMTableicporeceiptlines.lreceiptheaderid + " = " + m_slid + ")"
    		+ ")"
    		+ " ORDER BY " + SMTableicporeceiptlines.llinenumber
    		;
    	long iLineNumber = 0;
    	try {
			ResultSet rs = clsDatabaseFunctions.openResultSet(rsSQL, conn);
			while (rs.next()){
				iLineNumber++;
				String SQL = "UPDATE " + SMTableicporeceiptlines.TableName
					+ " SET " + SMTableicporeceiptlines.llinenumber + " = " + Long.toString(iLineNumber)
					+ " WHERE ("
						+ "(" + SMTableicporeceiptlines.lid + " = " + rs.getLong(SMTableicporeceiptlines.lid) + ")"
					+ ")"
				;
				try{
				    Statement stmt = conn.createStatement();
				    stmt.executeUpdate(SQL);
				}catch (Exception ex) {
					super.addErrorMessage("Error updating po receipt line number with SQL: " + SQL + " - " + ex.getMessage());
					return false;
				}
			}
			rs.close();
		} catch (SQLException e) {
			super.addErrorMessage("Error reading po receipt line numbers with SQL: " + rsSQL + " - " + e.getMessage());
			return false;
		}
    	
    	return true;
    }
}