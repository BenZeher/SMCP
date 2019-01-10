package smic;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import SMDataDefinition.SMTableicinventoryworksheet;
import SMDataDefinition.SMTableicitems;
import SMDataDefinition.SMTableicphysicalcountlines;
import SMDataDefinition.SMTableicphysicalinventories;
import ServletUtilities.clsMasterEntry;
import ServletUtilities.clsServletUtilities;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsDateAndTimeConversions;
import ServletUtilities.clsManageBigDecimals;
import ServletUtilities.clsManageRequestParameters;

public class ICPhysicalCountLineEntry extends clsMasterEntry{

	public static final String ParamObjectName = "Physical Count Line";
	
	//Particular to the specific class
	public static final String ParamID  = "id";
	public static final String ParamPhysicalInventoryID  = "physicalinventoryid";
	public static final String ParamCountID  = "countid";
	public static final String ParamQty  = "bdqty";
	public static final String ParamItemNumber = "sitemnumber";
	public static final String ParamUnitOfMeasure = "sunitofmeasure";
	public static final String ParamdatCreated = "datCreated";
	
	private String m_sid;
	private String m_sphysicalinventoryid;
	private String m_scountid;
	private String m_sqty;
	private String m_sitemnumber;
	private String m_sunitofmeasure;
	private String m_datcreated;
	
	public ICPhysicalCountLineEntry() {
		super();
		initEntryVariables();
        }

	ICPhysicalCountLineEntry (HttpServletRequest req){
		super(req);
		initEntryVariables();
		
		m_sid = clsManageRequestParameters.get_Request_Parameter(ICPhysicalCountLineEntry.ParamID, req).trim();
		m_sphysicalinventoryid = 
			clsManageRequestParameters.get_Request_Parameter(ICPhysicalCountLineEntry.ParamPhysicalInventoryID, req).trim();
		m_scountid = clsManageRequestParameters.get_Request_Parameter(ICPhysicalCountLineEntry.ParamCountID, req).trim();
		m_sqty = clsManageRequestParameters.get_Request_Parameter(ICPhysicalCountLineEntry.ParamQty, req).trim();
		m_sitemnumber = 
			clsManageRequestParameters.get_Request_Parameter(
				ICPhysicalCountLineEntry.ParamItemNumber, req).trim().toUpperCase();
		m_sunitofmeasure = 
			clsManageRequestParameters.get_Request_Parameter(ICPhysicalCountLineEntry.ParamUnitOfMeasure, req).trim();
		m_datcreated = 
			clsManageRequestParameters.get_Request_Parameter(ICPhysicalCountLineEntry.ParamdatCreated, req).trim();
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
    	clsDatabaseFunctions.freeConnection(context, conn, "[1547080882]");
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

		String SQL = " SELECT * FROM " + SMTableicphysicalcountlines.TableName
			+ " WHERE ("
				+ SMTableicphysicalcountlines.lid + " = " + sID
			+ ")"
			;
		
		try {
			ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
			if (rs.next()) {
				//Load the variables here:
				m_sid = sID;
				m_sphysicalinventoryid = 
					Long.toString(rs.getLong(SMTableicphysicalcountlines.lphysicalinventoryid));
				m_scountid = 
					Long.toString(rs.getLong(SMTableicphysicalcountlines.lcountid));
				m_sqty = clsManageBigDecimals.BigDecimalToFormattedString(
						"###,###,##0.0000", rs.getBigDecimal(SMTableicphysicalcountlines.bdqty));
				m_sitemnumber = rs.getString(SMTableicphysicalcountlines.sitemnumber);
				m_sunitofmeasure = rs.getString(SMTableicphysicalcountlines.sunitofmeasure);
				m_datcreated = clsDateAndTimeConversions.resultsetDateStringToString(
						rs.getString(SMTableicphysicalcountlines.datcreated));
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
    
    public boolean save_without_data_transaction (
    	ServletContext context, 
    	String sConf, 
    	String sUserID, 
    	String sUserFullName,
    	boolean bAddNewItems){
    	
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
    	
    	boolean bResult = save_without_data_transaction (conn, bAddNewItems);
    	clsDatabaseFunctions.freeConnection(context, conn, "[1547080883]");
    	return bResult;	
    	
    }
    public boolean save_without_data_transaction (Connection conn, boolean bAddNewItems){

    	if (!validate_entry_fields(conn, bAddNewItems)){
    		return false;
    	}

    	ICPhysicalInventoryEntry icphys = new ICPhysicalInventoryEntry();
    	icphys.slid(m_sphysicalinventoryid);
    	if (!icphys.load(conn)){
    		super.addErrorMessage("Could not load physical inventory #" + m_sphysicalinventoryid + " to "
    				+ "check status.");
    		return false;
    	}
    	
    	//IF the user selected to ADD NEW ITEMS to the inventory worksheet, then do that now:
    	if (bAddNewItems){
    		try {
				icphys.addSingleItem(m_sitemnumber, conn);
			} catch (Exception e) {
				super.addErrorMessage(e.getMessage());
				return false;
			}
    	}
    	
		//If the phys inventory is in any state EXCEPT for 'entered', it can't be updated:
		if (icphys.getStatus().compareToIgnoreCase(
				Integer.toString(SMTableicphysicalinventories.STATUS_ENTERED)) != 0){
			super.addErrorMessage("Physical inventories that have been processed can not be edited.");
			return false;
		}
    	
    	String SQL = "";
    	
    	if(m_sid.compareToIgnoreCase("-1") == 0){
    		SQL = "INSERT INTO " + SMTableicphysicalcountlines.TableName + "("
    			+ SMTableicphysicalcountlines.bdqty
    			+ ", " + SMTableicphysicalcountlines.datcreated
    			+ ", " + SMTableicphysicalcountlines.lcountid
    			+ ", " + SMTableicphysicalcountlines.lphysicalinventoryid
    			+ ", " + SMTableicphysicalcountlines.sitemnumber
    			+ ", " + SMTableicphysicalcountlines.sunitofmeasure
    			+ ") VALUES ("
    			+ m_sqty.replace(",", "")
    			+ ", NOW()"
    			+ ", " + m_scountid
    			+ ", " + m_sphysicalinventoryid
    			+ ", '" + clsDatabaseFunctions.FormatSQLStatement(m_sitemnumber) + "'"
    			+ ", '" + clsDatabaseFunctions.FormatSQLStatement(m_sunitofmeasure) + "'"
    			+ ")"
    			;
    	}else{
    		SQL = "UPDATE " + SMTableicphysicalcountlines.TableName + " SET "
    			+ SMTableicphysicalcountlines.bdqty + " = " + m_sqty.replace(",", "")
    			+ ", " + SMTableicphysicalcountlines.sitemnumber + " = '" 
					+ clsDatabaseFunctions.FormatSQLStatement(m_sitemnumber) + "'"
    			+ ", " + SMTableicphysicalcountlines.sunitofmeasure + " = '" 
					+ clsDatabaseFunctions.FormatSQLStatement(m_sunitofmeasure) + "'"
    			+ " WHERE ("
    				+ SMTableicphysicalcountlines.lid + " = " + m_sid
    			+ ")"
    			;
    	}

    	//System.out.println(" In " + this.toString() + " Save SQL = " + SQL);
    	
    	try{
	    	if (clsDatabaseFunctions.executeSQL(SQL, conn) == false){
	    		System.out.println(this.toString() + "Could not insert/update " + ParamObjectName + ".<BR>");
	    		super.addErrorMessage("Could not insert/update " + ParamObjectName + " with SQL: " + SQL);
	    		return false;
	    	}else{
	    	}
    	}catch(SQLException ex){
    	    super.addErrorMessage("Error [1538513662] inserting " + ParamObjectName + ": " + ex.getMessage());
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
    			+ " " + SMTableicphysicalcountlines.datcreated
    			+ " FROM " + SMTableicphysicalcountlines.TableName
    			+ " WHERE ("
    				+ SMTableicphysicalcountlines.lid + " = " + m_sid
    			+ ")"
    			;
    		
    		try {
				ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
				if (rs.next()){
					m_datcreated = clsDateAndTimeConversions.resultsetDateStringToString(
						rs.getString(SMTableicphysicalcountlines.datcreated));
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
    	clsDatabaseFunctions.freeConnection(context, conn, "[1547080881]");
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
    	
		//delete the inventory count lines
    	String SQL = "DELETE FROM " + SMTableicphysicalcountlines.TableName
		+ " WHERE ("
			+ SMTableicphysicalcountlines.lid + " = " + m_sid
		+ ")"
		;
	
		try {
			if (!clsDatabaseFunctions.executeSQL(SQL, conn)) {
				super.addErrorMessage("Could not delete " + ParamObjectName + "lines with ID '"
						+ m_sid + "'.");
				return false;
			}
		} catch (SQLException e) {
			super.addErrorMessage("Could not delete " + ParamObjectName + "lines with ID '"
					+ m_sid + "' - " + e.getMessage());
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
    		System.out.println(this.toString() + "Error formatting entry ID from string: " + slid + ".");
    		System.out.println(this.toString() + "Error: " + e.getMessage());
    		return false;
    	}
    }

    public boolean validate_entry_fields (Connection conn, boolean bAddNewItems){
        //Validate the entries here:
    	boolean bEntriesAreValid = true;
    	
    	//If the user had NOT chosen to add new items to the inventory worksheet, then make sure this item is already in it:
    	if (!bAddNewItems){
			String SQL = "SELECT " + SMTableicinventoryworksheet.sitemnumber
				+ " FROM " + SMTableicinventoryworksheet.TableName
				+ " WHERE ("
					+ "(" + SMTableicinventoryworksheet.sitemnumber + " = '" + m_sitemnumber + "')"
					+ " AND (" + SMTableicinventoryworksheet.lphysicalinventoryid + " = " + m_sphysicalinventoryid + ")"
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
				super.addErrorMessage("Error [1538502549] - Could not check for item '" + m_sitemnumber + "' with SQL: '" + SQL + "' - " + e1.getMessage());
			}
			if (!bItemIsAlreadyInWorksheet){
				super.addErrorMessage("Item '" + m_sitemnumber 
					+ "' is not included in this physical inventory, and you did not choose to add new items to the physical inventory.");
	        	bEntriesAreValid = false;
			}
    	}
    	
    	//ID:
    	long lID = 0;
		try {
			lID = Long.parseLong(m_sid);
		} catch (NumberFormatException e) {
        	super.addErrorMessage("Invalid ID: '" + m_sid + "'.");
        	bEntriesAreValid = false;
		}
    	
    	if ((lID < -1) || (lID == 0)){
        	super.addErrorMessage("Invalid ID: '" + m_sid + "'.");
        	bEntriesAreValid = false;
    	}
    	
    	//Physical inventory id:
    	long lphysicalinventoryID = 0;
		try {
			lphysicalinventoryID = Long.parseLong(m_sphysicalinventoryid);
		} catch (NumberFormatException e) {
        	super.addErrorMessage("Invalid physical inventory ID: '" + m_sphysicalinventoryid + "'.");
        	bEntriesAreValid = false;
		}
    	
    	if (lphysicalinventoryID <= 0){
        	super.addErrorMessage("Invalid physical inventory ID: '" + m_sphysicalinventoryid + "'.");
        	bEntriesAreValid = false;
    	}
    	
    	//Count id:
    	long lcountID = 0;
		try {
			lcountID = Long.parseLong(m_scountid);
		} catch (NumberFormatException e) {
        	super.addErrorMessage("Invalid count ID: '" + m_scountid + "'.");
        	bEntriesAreValid = false;
		}
    	
    	if (lcountID <= 0){
        	super.addErrorMessage("Invalid count ID: '" + m_scountid + "'.");
        	bEntriesAreValid = false;
    	}
    	
		// Qty:
    	try {
			BigDecimal bdqty = new BigDecimal(m_sqty);
			if (bdqty.compareTo(BigDecimal.ZERO) < 1){
				super.addErrorMessage("Invalid qty : '" + m_sqty + "'.");
			}
		} catch (Exception e) {
        	super.addErrorMessage("Invalid qty: '" + m_sqty + "'.");
        	bEntriesAreValid = false;
		}
        	
		//Item number
		boolean bItemNumberIsValid = true;
		m_sitemnumber = m_sitemnumber.trim();
		if (m_sitemnumber.compareToIgnoreCase("") == 0){
        	super.addErrorMessage("Item number cannot be empty.");
        	bEntriesAreValid = false;
        	bItemNumberIsValid = false;
        }
        if (m_sitemnumber.length() > SMTableicphysicalcountlines.sitemnumberLength){
        	super.addErrorMessage("Item number is too long.");
        	bEntriesAreValid = false;
        	bItemNumberIsValid = false;
        	return bEntriesAreValid;
        }

        //Validate the item number:
        String SQL = "SELECT " + SMTableicitems.sCostUnitOfMeasure + " FROM " + SMTableicitems.TableName
        	+ " WHERE ("
        		+ SMTableicitems.sItemNumber + " = '" + m_sitemnumber + "'"
        	+ ")"
        	;
        //System.out.println("In " + this.toString() + ".validate_entry_fields - SQL = " + SQL);
        try {
			ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
			if (!rs.next()){
				super.addErrorMessage("Invalid item number - '" + m_sitemnumber + "'.");
				bEntriesAreValid = false;
				bItemNumberIsValid = false;
			}else{
				m_sunitofmeasure = rs.getString(SMTableicitems.sCostUnitOfMeasure);
			}
			rs.close();
		} catch (SQLException e) {
			super.addErrorMessage(
				"Error validating item number '" + m_sitemnumber + "' - " + e.getMessage() + ".");
			bEntriesAreValid = false;
			bItemNumberIsValid = false;
		}
        
		//Unit of measure:
		m_sunitofmeasure = m_sunitofmeasure.trim();
		//No need to check the unit of measure if the item number is invalid:
		if (bItemNumberIsValid){
			if (m_sunitofmeasure.compareToIgnoreCase("") == 0){
				super.addErrorMessage(
						"Unit of measure cannot be blank.");
					bEntriesAreValid = false;
			}
	        if (m_sunitofmeasure.length() > SMTableicphysicalcountlines.sunitofmeasureLength){
	        	super.addErrorMessage("Unit of measure is too long.");
	        	bEntriesAreValid = false;
	        }
		}

        //Creation date:
        //No need to check this - it gets set when the line is created, and only read after that:
        //if (lID != -1){	//Only check if it's NOT a new entry
	    //    if (!SMUtilities.IsValidDateString("M/d/yyyy", m_datcreated)){
	    //    	super.addErrorMessage("Creation date '" + m_datcreated + "' is invalid.  ");
	    //    	bEntriesAreValid = false;
	    //    }
        // }
    	return bEntriesAreValid;
    }

    @Override

    public void addErrorMessage(String sMsg){
    	super.addErrorMessage(sMsg);
    }

	public String printDebugData() {
		return "ICPhysicalCountLineEntry [m_datcreated=" + m_datcreated
				+ ", m_sid=" + m_sid + ", m_sitemnumber=" + m_sitemnumber
				+ ", m_sphysicalinventoryid="
				+ m_sphysicalinventoryid + ", m_sqty=" + m_sqty
				+ ", m_sunitofmeasure=" + m_sunitofmeasure + "]";
	}

	public String getQueryString(){

		//Particular to the specific class
		return ParamID + "=" + clsServletUtilities.URLEncode(m_sid)
			+ "&" + ICPhysicalCountLineEntry.ParamPhysicalInventoryID + clsServletUtilities.URLEncode(m_sphysicalinventoryid)
			+ "&" + ICPhysicalCountLineEntry.ParamCountID + m_scountid
			+ "&" + ICPhysicalCountLineEntry.ParamdatCreated + clsServletUtilities.URLEncode(m_datcreated)
			+ "&" + ICPhysicalCountLineEntry.ParamID + m_sid
			+ "&" + ICPhysicalCountLineEntry.ParamItemNumber + clsServletUtilities.URLEncode(m_sitemnumber)
			+ "&" + ICPhysicalCountLineEntry.ParamQty + clsServletUtilities.URLEncode(m_sqty)
			+ "&" + ICPhysicalCountLineEntry.ParamUnitOfMeasure + clsServletUtilities.URLEncode(m_sunitofmeasure)
		;
	}

	public String getdatCreated() {
		return m_datcreated;
	}

	public void setdatCreated(String datCreated) {
		m_datcreated = datCreated;
	}
	public String getCountID() {
		return m_scountid;
	}

	public void setsCountID(String sCountID) {
		m_scountid = sCountID;
	}
	public String getPhysicalInventoryID() {
		return m_sphysicalinventoryid;
	}
	public void setsPhysicalInventoryID(String sPhysicalInventoryID) {
		m_sphysicalinventoryid = sPhysicalInventoryID;
	}
	public String getsQty() {
		return m_sqty;
	}
	public void setsQty(String sQty) {
		m_sqty = sQty;
	}
	public String getsItemNumber() {
		return m_sitemnumber;
	}
	public void setsItemNumber(String sItemNumber) {
		m_sitemnumber = sItemNumber;
	}
	public String getsUnitOfMeasure() {
		return m_sunitofmeasure;
	}
	public void setsUnitOfMeasure(String sUnitOfMeasure) {
		m_sunitofmeasure = sUnitOfMeasure;
	}
	public String getsID() {
		return m_sid;
	}
	public void setsID(String sID) {
		m_sid = sID;
	}
	
    private void initEntryVariables(){
    	m_sid = "-1";
		m_sphysicalinventoryid = "-1";
		m_scountid = "-1";
		m_sqty = "0.0000";
		m_sitemnumber = "";
		m_sunitofmeasure = "";
		m_datcreated = clsDateAndTimeConversions.now("M/d/yyyy");
		super.initVariables();
		super.setObjectName(ParamObjectName);
    }
}