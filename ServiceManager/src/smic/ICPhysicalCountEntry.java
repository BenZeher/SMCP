package smic;

import SMDataDefinition.*;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Connection;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import ServletUtilities.*;

public class ICPhysicalCountEntry extends clsMasterEntry{

	public static final String ParamObjectName = "Physical Count";
	
	//Particular to the specific class
	public static final String ParamID  = "id";
	public static final String ParamPhysicalInventoryID  = "physicalinventoryid";
	public static final String ParamDesc  = "Description";
	public static final String ParamCreatedByID = "lCreatedByID";
	public static final String ParamCreatedByFullName = "lCreatedByFullName";
	public static final String ParamdatCreated = "datCreated";
	
	private String m_sid;
	private String m_sphysicalinventoryid;
	private String m_sdesc;
	private String m_lcreatedbyid;
	private String m_screatedbyfullname;
	private String m_datcreated;
	
	public ICPhysicalCountEntry() {
		super();
		initEntryVariables();
        }

	ICPhysicalCountEntry (HttpServletRequest req){
		super(req);
		initEntryVariables();
		
		m_sid = clsManageRequestParameters.get_Request_Parameter(ICPhysicalCountEntry.ParamID, req).trim();
		m_sphysicalinventoryid = clsManageRequestParameters.get_Request_Parameter(ICPhysicalCountEntry.ParamPhysicalInventoryID, req).trim();
		m_sdesc = clsManageRequestParameters.get_Request_Parameter(ICPhysicalCountEntry.ParamDesc, req).trim();
		m_lcreatedbyid = clsManageRequestParameters.get_Request_Parameter(ICPhysicalCountEntry.ParamCreatedByID, req).trim();
		m_screatedbyfullname = clsManageRequestParameters.get_Request_Parameter(ICPhysicalCountEntry.ParamCreatedByFullName, req).trim();
		m_datcreated = clsManageRequestParameters.get_Request_Parameter(ICPhysicalCountEntry.ParamdatCreated, req).trim();
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
    	clsDatabaseFunctions.freeConnection(context, conn, "[1547080878]");
    	return bResult;
    	
    }
    public boolean load (Connection conn){
    	return load (m_sid, conn);
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

		String SQL = " SELECT * FROM " + SMTableicphysicalcounts.TableName
			+ " WHERE ("
				+ SMTableicphysicalcounts.lid + " = " + sID
			+ ")"
			;
		
		try {
			ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
			if (rs.next()) {
				//Load the variables here:
				m_sid = sID;
				m_sphysicalinventoryid = Long.toString(rs.getLong(SMTableicphysicalcounts.lphysicalinventoryid));
				m_sdesc = rs.getString(SMTableicphysicalcounts.sdesc);
				m_lcreatedbyid = Long.toString(rs.getLong(SMTableicphysicalcounts.lcreatedbyid));
				m_screatedbyfullname= rs.getString(SMTableicphysicalcounts.screatedbyfullname);
				m_datcreated = clsDateAndTimeConversions.resultsetDateStringToString(
						rs.getString(SMTableicphysicalcounts.datcreated));
			} else {
				super.addErrorMessage("No " + ParamObjectName + " found for ID: '" + sID
						+ "'");
				rs.close();
				return false;
			}
		} catch (SQLException e) {
			super.addErrorMessage("Error reading " + ParamObjectName + " for ID: '" + sID
					+ "' - " + e.getMessage());
			return false;
		}
		return true;
    }
    
    public boolean save_without_data_transaction (ServletContext context, String sDBID, String sUserID, String sUserFullName){
    	
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
    	
    	boolean bResult = save_without_data_transaction(conn, sUserID, sUserFullName);
    	clsDatabaseFunctions.freeConnection(context, conn, "[1547080879]");
    	return bResult;	
    	
    }
    public boolean save_without_data_transaction (Connection conn, String sUserID, String sUserFullName){

    	m_lcreatedbyid = sUserID;
    	m_screatedbyfullname = sUserFullName;
    	
    	ICPhysicalInventoryEntry icphys = new ICPhysicalInventoryEntry();
    	icphys.slid(m_sphysicalinventoryid);
    	if (!icphys.load(conn)){
    		super.addErrorMessage("Could not load physical inventory #" + m_sphysicalinventoryid + " to "
    				+ "check status.");
    		return false;
    	}
    	
		//If the phys inventory is in any state EXCEPT for 'entered', it can't be updated:
		if (icphys.getStatus().compareToIgnoreCase(
				Integer.toString(SMTableicphysicalinventories.STATUS_ENTERED)) != 0){
			super.addErrorMessage("Physical inventories that have been processed can not be edited.");
			return false;
		}
    	
    	if (!validate_entry_fields(conn)){
    		return false;
    	}

    	String SQL = "";
    	
    	if(m_sid.compareToIgnoreCase("-1") == 0){
    		SQL = "INSERT INTO " + SMTableicphysicalcounts.TableName + "("
    			+ SMTableicphysicalcounts.lphysicalinventoryid
    			+ ", " + SMTableicphysicalcounts.datcreated
    			+ ", " + SMTableicphysicalcounts.lcreatedbyid
    			+ ", " + SMTableicphysicalcounts.screatedbyfullname
    			+ ", " + SMTableicphysicalcounts.sdesc
    			+ ") VALUES ("
    			+ m_sphysicalinventoryid
    			+ ", NOW()"
    			+ ", " + m_lcreatedbyid + ""
    			+ ", '" + clsDatabaseFunctions.FormatSQLStatement(m_screatedbyfullname) + "'"
    			+ ", '" + clsDatabaseFunctions.FormatSQLStatement(m_sdesc) + "'"
    			+ ")"
    			;
    	}else{
    		SQL = "UPDATE " + SMTableicphysicalcounts.TableName + " SET"
				+ " " + SMTableicphysicalcounts.sdesc + " = '" + clsDatabaseFunctions.FormatSQLStatement(m_sdesc) + "'"
    			+ " WHERE ("
    				+ SMTableicphysicalcounts.lid + " = " + m_sid
    			+ ")"
    			;
    	}

    	//System.out.println(" In " + this.toString() + " Save SQL = " + SQL);
    	
    	try{
	    	if (clsDatabaseFunctions.executeSQL(SQL, conn) == false){
	    		System.out.println("[1579201952] " + this.toString() + "Could not insert/update " + ParamObjectName + ".<BR>");
	    		super.addErrorMessage("Could not insert/update " + ParamObjectName + " with SQL: " + SQL);
	    		return false;
	    	}else{
	    	}
    	}catch(SQLException ex){
    		System.out.println("[1579201962] Error in " + this.toString() + " class!!");
    	    System.out.println("SQLException: " + ex.getMessage());
    	    System.out.println("SQLState: " + ex.getSQLState());
    	    System.out.println("SQL: " + ex.getErrorCode());
    	    super.addErrorMessage("Error inserting " + ParamObjectName + ": " + ex.getMessage());
    	    return false;
    	}
    	
    	//Update the ID if it's an insert:
    	if (m_sid.compareToIgnoreCase("-1") == 0){
    		SQL = "SELECT last_insert_id()";
    		try {
				ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
				if (rs.next()) {
					m_sid = Long.toString(rs.getLong(1));
				}else {
					m_sid = "0";
				}
				rs.close();
			} catch (SQLException e) {
    			//SMUtilities.rollback_data_transaction(conn);
    			super.addErrorMessage("Could not get last ID number - " + e.getMessage());
    			return false;
			}
			//If something went wrong, we can't get the last ID:
    		if (m_sid.compareToIgnoreCase("0") == 0){
    			//SMUtilities.rollback_data_transaction(conn);
    			super.addErrorMessage("Could not get last ID number.");
    			return false;
    		}
    		//Now reload it one time to get the correct creation date:
    		SQL = "SELECT"
    			+ " " + SMTableicphysicalcounts.datcreated
    			+ " FROM " + SMTableicphysicalcounts.TableName
    			+ " WHERE ("
    				+ SMTableicphysicalcounts.lid + " = " + m_sid
    			+ ")"
    			;
    		
    		try {
				ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
				if (rs.next()){
					m_datcreated = clsDateAndTimeConversions.resultsetDateStringToString(
						rs.getString(SMTableicphysicalcounts.datcreated));
				}else{
					super.addErrorMessage("Could not get created date.");
					return false;
				}
				rs.close();
			} catch (SQLException e) {
				super.addErrorMessage("Could not get created date - " + e.getMessage() + ".");
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
    	clsDatabaseFunctions.freeConnection(context, conn, "[1547080877]");
    	return bResult;
    	
    }
    public boolean delete (Connection conn){
    	
    	ICPhysicalInventoryEntry icphys = new ICPhysicalInventoryEntry();
    	icphys.slid(m_sphysicalinventoryid);
    	if (!icphys.load(conn)){
    		super.addErrorMessage("Could not load physical inventory #" + m_sphysicalinventoryid + " to "
    				+ "check status.");
    		return false;
    	}
    	
		//If the phys inventory is in any state EXCEPT for 'entered', it can't be updated:
		if (icphys.getStatus().compareToIgnoreCase(
				Integer.toString(SMTableicphysicalinventories.STATUS_ENTERED)) != 0){
			super.addErrorMessage("Physical inventories that have been processed can not be edited.");
			return false;
		}
    	
    	if (!clsDatabaseFunctions.start_data_transaction(conn)){
			super.addErrorMessage("Could not start data transaction to delete " + ParamObjectName 
					+ " with ID '" + m_sid + "'.");
			return false;
    	}
    	
    	boolean bResult = true;

		//delete the inventory count lines
    	String SQL = "DELETE FROM " + SMTableicphysicalcountlines.TableName
		+ " WHERE ("
			+ SMTableicphysicalcountlines.lcountid + " = " + m_sid
		+ ")"
		;
	
		try {
			if (!clsDatabaseFunctions.executeSQL(SQL, conn)) {
				super.addErrorMessage("Could not delete " + ParamObjectName + "lines with ID '"
						+ m_sid + "'.");
				clsDatabaseFunctions.rollback_data_transaction(conn);
				return false;
			}
		} catch (SQLException e) {
			super.addErrorMessage("Could not delete " + ParamObjectName + "lines with ID '"
					+ m_sid + "' - " + e.getMessage());
			clsDatabaseFunctions.rollback_data_transaction(conn);
			return false;
		}
		
		//delete the inventory counts
    	SQL = "DELETE FROM " + SMTableicphysicalcounts.TableName
		+ " WHERE ("
			+ SMTableicphysicalcounts.lid + " = " + m_sid
		+ ")"
		;

		try {
			if (!clsDatabaseFunctions.executeSQL(SQL, conn)) {
				super.addErrorMessage("Could not delete " + ParamObjectName + "counts with ID '"
						+ m_sid + "'.");
				clsDatabaseFunctions.rollback_data_transaction(conn);
				return false;
			}
		} catch (SQLException e) {
			super.addErrorMessage("Could not delete " + ParamObjectName + "counts with ID '"
					+ m_sid + "' - " + e.getMessage());
			clsDatabaseFunctions.rollback_data_transaction(conn);
			return false;
		}
		
		if (!bResult){
			clsDatabaseFunctions.rollback_data_transaction(conn);
			return false;
		}
		
		if (!clsDatabaseFunctions.commit_data_transaction(conn)){
			clsDatabaseFunctions.rollback_data_transaction(conn);
			super.addErrorMessage(
				"Could not commit data transaction after deleting " + ParamObjectName + "."); 
			return false;
		}
		
		//Empty the values:
		initEntryVariables();
		return true;
    }

    public String slid (){
    	return m_sid;
    }
    public boolean slid(String slid){
    	try{
    		m_sid = slid;
    		return true;
    	}catch (NumberFormatException e){
    		System.out.println("[1579201972] " + this.toString() + "Error formatting entry ID from string: " + slid + ".");
    		System.out.println(this.toString() + "Error: " + e.getMessage());
    		return false;
    	}
    }

    public boolean validate_entry_fields (Connection conn){
        //Validate the entries here:
    	boolean bEntriesAreValid = true;
    	
    	//ID:
    	long lID;
		try {
			lID = Long.parseLong(m_sid);
		} catch (NumberFormatException e) {
        	super.addErrorMessage("Invalid ID: '" + m_sid + "'.");
        	bEntriesAreValid = false;
        	return bEntriesAreValid;
		}
    	
    	if ((lID < -1) || (lID == 0)){
        	super.addErrorMessage("Invalid ID: '" + m_sid + "'.");
        	bEntriesAreValid = false;
        	return bEntriesAreValid;
    	}
    	
    	//Physical inventory id:
    	long lphysicalinventoryID;
		try {
			lphysicalinventoryID = Long.parseLong(m_sphysicalinventoryid);
		} catch (NumberFormatException e) {
        	super.addErrorMessage("Invalid physical inventory ID: '" + m_sphysicalinventoryid + "'.");
        	bEntriesAreValid = false;
        	return bEntriesAreValid;
		}
    	
    	if (lphysicalinventoryID <= 0){
        	super.addErrorMessage("Invalid physical inventory ID: '" + m_sphysicalinventoryid + "'.");
        	bEntriesAreValid = false;
        	return bEntriesAreValid;
    	}
    	
		//Description
		m_sdesc = m_sdesc.trim();
		if (m_sdesc.compareToIgnoreCase("") == 0){
        	super.addErrorMessage("Description cannot be empty.");
        	bEntriesAreValid = false;
        	return bEntriesAreValid;
        }
        if (m_sdesc.length() > SMTableicphysicalcounts.sdescLength){
        	super.addErrorMessage("Description is too long.");
        	bEntriesAreValid = false;
        	return bEntriesAreValid;
        }

		//Created by ID
		m_lcreatedbyid = m_lcreatedbyid.trim();
		if (m_lcreatedbyid.compareToIgnoreCase("") == 0){
        	super.addErrorMessage("Created by ID cannot be empty.");
        	bEntriesAreValid = false;
        	return bEntriesAreValid;
        }
        if (m_lcreatedbyid.length() > SMTableicphysicalcounts.lcreatedbyidLength){
        	super.addErrorMessage("Created by is too long.");
        	bEntriesAreValid = false;
        	return bEntriesAreValid;
        }
        
      //Created by Full name
        m_screatedbyfullname = m_screatedbyfullname.trim();
		if (m_screatedbyfullname.compareToIgnoreCase("") == 0){
        	super.addErrorMessage("Created by full name cannot be empty.");
        	bEntriesAreValid = false;
        	return bEntriesAreValid;
        }
        if (m_screatedbyfullname.length() > SMTableicphysicalcounts.screatedbyfullnameLength){
        	super.addErrorMessage("Created by full name is too long.");
        	bEntriesAreValid = false;
        	return bEntriesAreValid;
        }

        //Don't need to check this - we always get the created date on an insert using NOW()
        //and we don't need to check it again.
        //Creation date:
        //if (lID != -1){	//Only check if it's NOT a new entry
	    //    if (!SMUtilities.IsValidDateString("M/d/yyyy", m_datcreated)){
	    //    	super.addErrorMessage("Creation date '" + m_datcreated + "' is invalid.  ");
	    //    	bEntriesAreValid = false;
	    //    }
        //}
    	return bEntriesAreValid;
    }

    @Override

    public void addErrorMessage(String sMsg){
    	super.addErrorMessage(sMsg);
    }
	public String printDebugString() {
		return "ICPhysicalCountEntry [m_datcreated=" + m_datcreated
				+ ", m_lcreatedbyid=" + m_lcreatedbyid + ", m_sdesc=" + m_sdesc
				+ ", m_sid=" + m_sid + ", m_sphysicalinventoryid="
				+ m_sphysicalinventoryid + "]";
	}

	public String getQueryString(){

		//Particular to the specific class
		return ParamID + "=" + clsServletUtilities.URLEncode(m_sid)
			+ "&" + ParamPhysicalInventoryID + clsServletUtilities.URLEncode(m_sphysicalinventoryid)
			+ "&" + ParamCreatedByID + clsServletUtilities.URLEncode(m_lcreatedbyid)
			+ "&" + ParamdatCreated + clsServletUtilities.URLEncode(m_datcreated)
			+ "&" + ParamDesc + clsServletUtilities.URLEncode(m_sdesc)
		;
	}

	public String getsCreatedByID() {
		return m_lcreatedbyid;
	}

	public void setsCreatedByID(String sCreatedByID) {
		m_lcreatedbyid = sCreatedByID;
	}
	
	public String getsCreatedByFullName() {
		return m_screatedbyfullname;
	}

	public void setsCreatedByFullName(String screatedbyfullname) {
		m_screatedbyfullname = screatedbyfullname;
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
	public String getPhysicalInventoryID() {
		return m_sphysicalinventoryid;
	}

	public void setsPhysicalInventoryID(String sPhysicalInventoryID) {
		m_sphysicalinventoryid = sPhysicalInventoryID;
	}

    private void initEntryVariables(){
		m_sid = "-1";
		m_sphysicalinventoryid = "-1";
		m_sdesc = "";
		m_lcreatedbyid = "0";
		m_screatedbyfullname = "";
		m_datcreated = clsDateAndTimeConversions.now("M/d/yyyy");
		super.initVariables();
		super.setObjectName(ParamObjectName);
    }
}