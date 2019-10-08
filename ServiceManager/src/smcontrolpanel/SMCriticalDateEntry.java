package smcontrolpanel;

import SMDataDefinition.*;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Connection;
import java.sql.Date;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import ServletUtilities.*;


public class SMCriticalDateEntry extends clsMasterEntry{

	public static final String ParamObjectName = "Critical Date";
	
	//Particular to the specific class
	public static final String ParamID  = "id";
	public static final String ParamCriticalDate = "Critical Date";
	public static final String ParamDocNumber = "DocNumber";
	public static final String ParamResolvedFlag = "Resolved Flag";
	public static final String ParamComments = "Comments";
	public static final String ParamTimeStampAudit = "TimeStampAudit";
	public static final String ParamResponsibleUserID = "ResponsibleUserID";
	public static final String ParamResponsibleUserFullName = "ResponsibleUserFullName";
	public static final String ParamiType = "iType";
	public static final String ParamAssignedbyUserID = "sAssignedbyUserID";
	public static final String ParamAssignedbyUserFullName = "sAssignedbyUserFullName";
	
	private String m_sid;
	private String m_datcriticaldate;
	private String m_sdocnumber;
	private String m_sresolvedflag;
	private String m_scomments;
	private String m_stimestampaudit;
	private String m_lresponsibleuserid;
	private String m_sresponsibleuserfullname;
	private String m_itype;
	private String m_lassignedbyuserid;
	private String m_sassignedbyuserfullname;
	
	public SMCriticalDateEntry() {
		super();
		initCriticalDateVariables();
        }

	SMCriticalDateEntry (HttpServletRequest req){
		super(req);
		initCriticalDateVariables();
		
		//id
		m_sid = clsManageRequestParameters.get_Request_Parameter(SMCriticalDateEntry.ParamID, req).trim();
		
		//critical date
		m_datcriticaldate = clsManageRequestParameters.get_Request_Parameter(SMCriticalDateEntry.ParamCriticalDate, req).trim();
		if (m_datcriticaldate.compareToIgnoreCase("") == 0){
			m_datcriticaldate = EMPTY_DATE_STRING;
		}
		
		//jobnumber
		m_sdocnumber = clsManageRequestParameters.get_Request_Parameter(SMCriticalDateEntry.ParamDocNumber, req).trim();
		
		//resolved flag
		if (clsManageRequestParameters.get_Request_Parameter(SMCriticalDateEntry.ParamResolvedFlag, req).trim().compareToIgnoreCase("on") == 0){
			m_sresolvedflag = "1";
		}else{
			m_sresolvedflag = "0";
		}
		//System.out.println("m_sresolvedflag = " + m_sresolvedflag);

		//comments
		m_scomments = clsManageRequestParameters.get_Request_Parameter(SMCriticalDateEntry.ParamComments, req).trim();
		
		//timestamp audit
		m_stimestampaudit = clsManageRequestParameters.get_Request_Parameter(SMCriticalDateEntry.ParamTimeStampAudit, req).trim();
		if (m_stimestampaudit.compareToIgnoreCase("") == 0){
			m_stimestampaudit = "NOW()";
		}
		//responsible
		m_sresponsibleuserfullname = clsManageRequestParameters.get_Request_Parameter(SMCriticalDateEntry.ParamResponsibleUserFullName, req).trim();
		m_lresponsibleuserid = clsManageRequestParameters.get_Request_Parameter(SMCriticalDateEntry.ParamResponsibleUserID, req).trim();
		if(m_lresponsibleuserid.compareToIgnoreCase("") == 0) {
			m_lresponsibleuserid = "0";
		}
		//rec type
		m_itype = clsManageRequestParameters.get_Request_Parameter(SMCriticalDateEntry.ParamiType, req).trim();
		if(m_itype.compareToIgnoreCase("") == 0) {
			m_itype = "0";
		}
		//assigned by
		m_sassignedbyuserfullname = clsManageRequestParameters.get_Request_Parameter(SMCriticalDateEntry.ParamAssignedbyUserFullName, req).trim();
		m_lassignedbyuserid = clsManageRequestParameters.get_Request_Parameter(SMCriticalDateEntry.ParamAssignedbyUserID, req).trim();
		if(m_lassignedbyuserid.compareToIgnoreCase("") == 0) {
			m_lassignedbyuserid = "0";
		}
	}
	
    public boolean load (ServletContext context, String sDBID, String sUser){
    	Connection conn = clsDatabaseFunctions.getConnection(
    			context, 
    			sDBID, 
    			"MySQL", 
    			this.toString() + " - user: " + sUser
    			);
    	
    	if (conn == null){
    		super.addErrorMessage("Error opening data connection.");
    		return false;
    	}
    	
    	boolean bResult = load (conn);
    	clsDatabaseFunctions.freeConnection(context, conn, "[1547080428]");
    	return bResult;
    	
    }
    public boolean load (Connection conn){
    	return load(m_sid, conn);
    }
    private boolean load (String sID, Connection conn){

    	long lID;
    	
		try{
			lID = Long.parseLong(sID);
		}catch(NumberFormatException n){
			super.addErrorMessage("Invalid ID: '" + sID + "'");
			return false;
		}
		
		
		if (lID < 0){
			//essential informationhas already been loaded.
			return true;
			
		}else{

			String SQL = " SELECT * FROM " + SMTablecriticaldates.TableName
				+ " WHERE ("
					+ SMTablecriticaldates.sId + " = " + sID
				+ ")"
				;
			
			try {
				ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
				if (rs.next()) {
					//Load the variables here:
					m_sid = sID;
					m_datcriticaldate = clsDateAndTimeConversions.resultsetDateStringToString(rs.getString(SMTablecriticaldates.sCriticalDate.replace("`", "")));
					m_sdocnumber = rs.getString(SMTablecriticaldates.sdocnumber.replace("`", ""));
					m_sresolvedflag = rs.getString(SMTablecriticaldates.sResolvedFlag.replace("`", ""));
					m_scomments = rs.getString(SMTablecriticaldates.sComments.replace("`", ""));
					m_stimestampaudit = rs.getString(SMTablecriticaldates.sTimeStampAudit.replace("`", ""));
					m_sresponsibleuserfullname = rs.getString(SMTablecriticaldates.sresponsibleuserfullname.replace("`", ""));
					m_lresponsibleuserid = Long.toString(rs.getLong(SMTablecriticaldates.lresponsibleuserid));
					m_itype = Integer.toString(rs.getInt(SMTablecriticaldates.itype));
					m_sassignedbyuserfullname = rs.getString(SMTablecriticaldates.sassignedbyuserfullname);
					m_lassignedbyuserid = rs.getString(SMTablecriticaldates.lassignedbyuserid);
					rs.close();
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
		}
		return true;
    }
    
    public boolean save_without_data_transaction (ServletContext context, String sDBID, String sUser){
    	
       	Connection conn = clsDatabaseFunctions.getConnection(
    			context, 
    			sDBID, 
    			"MySQL", 
    			this.toString() + " - user: " + sUser
    			);
    	
    	if (conn == null){
    		super.addErrorMessage("Error opening data connection.");
    		return false;
    	}
    	
    	boolean bResult = save_without_data_transaction (conn);
    	clsDatabaseFunctions.freeConnection(context, conn, "[1547080429]");
    	return bResult;	
    	
    }
    public boolean save_without_data_transaction (Connection conn){

    	//m_screatedby = sUser;
    	
    	if (!validate_entry_fields(conn)){
    		return false;
    	}

    	String SQL = "";
    	
    	if(m_sid.compareToIgnoreCase("-1") == 0){
    		SQL = "INSERT INTO " + SMTablecriticaldates.TableName + "(" +
    				" " + SMTablecriticaldates.sCriticalDate + "," +
    				" " + SMTablecriticaldates.sdocnumber + "," +
    				" " + SMTablecriticaldates.sResolvedFlag + "," +
    				" " + SMTablecriticaldates.sComments + "," +
    				" " + SMTablecriticaldates.sTimeStampAudit + "," +
    				" " + SMTablecriticaldates.lresponsibleuserid + "," +
    				" " + SMTablecriticaldates.sresponsibleuserfullname + "," +
    				" " + SMTablecriticaldates.lassignedbyuserid + "," +
    				" " + SMTablecriticaldates.sassignedbyuserfullname + "," +  				
    				" " + SMTablecriticaldates.itype +
    				") VALUES (" +
    			    " '" + clsDateAndTimeConversions.stdDateStringToSQLDateString(m_datcriticaldate) + "'," +
    			    " '" + clsDatabaseFunctions.FormatSQLStatement(m_sdocnumber.trim()) + "'," +
    			    " " + clsDatabaseFunctions.FormatSQLStatement(m_sresolvedflag.trim()) + "," +
    			    " '" + clsDatabaseFunctions.FormatSQLStatement(m_scomments.trim()) + "'," +
    			    " NOW()," +
    			    " " + clsDatabaseFunctions.FormatSQLStatement(m_lresponsibleuserid.trim()) + "," +
    			    " '" + clsDatabaseFunctions.FormatSQLStatement(m_sresponsibleuserfullname.trim()) + "'," +
    			    " " + clsDatabaseFunctions.FormatSQLStatement(m_lassignedbyuserid.trim()) + "," +
    			    " '" + clsDatabaseFunctions.FormatSQLStatement(m_sassignedbyuserfullname.trim()) + "'," +
    			    " " + clsDatabaseFunctions.FormatSQLStatement(m_itype) + "" +
    				")";
    	}else{
    		SQL = "UPDATE " + SMTablecriticaldates.TableName + 
    				" SET" +
    					" " + SMTablecriticaldates.sCriticalDate + " = '" + clsDateAndTimeConversions.stdDateStringToSQLDateString(m_datcriticaldate) + "'," +
    					" " + SMTablecriticaldates.sdocnumber + " = '" + clsDatabaseFunctions.FormatSQLStatement(m_sdocnumber.trim()) + "'," +
    					" " + SMTablecriticaldates.sResolvedFlag + " = " + clsDatabaseFunctions.FormatSQLStatement(m_sresolvedflag.trim()) + "," + 
    					" " + SMTablecriticaldates.sComments + " = '" + clsDatabaseFunctions.FormatSQLStatement(m_scomments.trim()) + "'," +
    					" " + SMTablecriticaldates.sTimeStampAudit + " = '" + clsDatabaseFunctions.FormatSQLStatement(m_stimestampaudit.trim()) + "'," +
    					" " + SMTablecriticaldates.lresponsibleuserid + " = " + clsDatabaseFunctions.FormatSQLStatement(m_lresponsibleuserid) + "," +
    					" " + SMTablecriticaldates.sresponsibleuserfullname + " = '" + clsDatabaseFunctions.FormatSQLStatement(m_sresponsibleuserfullname.trim()) + "'," +
    					" " + SMTablecriticaldates.lassignedbyuserid + " = " + clsDatabaseFunctions.FormatSQLStatement(m_lassignedbyuserid) + "," +
    					" " + SMTablecriticaldates.sassignedbyuserfullname + " = '" + clsDatabaseFunctions.FormatSQLStatement(m_sassignedbyuserfullname.trim()) + "'," +
    					" " + SMTablecriticaldates.itype + " = " + clsDatabaseFunctions.FormatSQLStatement(m_itype) + "" + 
    				" WHERE" + 
    					" " + SMTablecriticaldates.sId + " = " + m_sid;
    	}

    	//System.out.println(" In " + this.toString() + " Save SQL = " + SQL);
    	
    	try{
	    	if (clsDatabaseFunctions.executeSQL(SQL, conn) == false){
	    		//System.out.println(this.toString() + "Could not insert/update " + ParamObjectName + ".<BR>");
	    		super.addErrorMessage("Could not insert/update " + ParamObjectName + " with SQL: " + SQL);
	    		return false;
	    	}else{
	    		//System.out.println(this.toString() + "Successfully updated " + ParamObjectName + "entry.");
	    	}
    	}catch(SQLException ex){
    	    super.addErrorMessage("Error [1434050639] inserting " + ParamObjectName + ": " + ex.getMessage());
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
    	}
    	return true;
    }

    public boolean delete (ServletContext context, String sDBID, String sUserID, String sUserFullName){
    	
    	Connection conn = clsDatabaseFunctions.getConnection(
    			context, 
    			sDBID, 
    			"MySQL", 
    			this.toString() + " - user: " + sUserID + " - " + sUserFullName
    			);
    	
    	if (conn == null){
    		super.addErrorMessage("Error opening data connection.");
    		return false;
    	}
    	
    	boolean bResult = delete (conn);
    	clsDatabaseFunctions.freeConnection(context, conn, "[1547080427]");
    	return bResult;
    	
    }
    public boolean delete (Connection conn){
    	
    	String SQL = "DELETE FROM " + SMTablecriticaldates.TableName
    		+ " WHERE ("
    			+ SMTablecriticaldates.sId+ " = " + m_sid
    		+ ")"
    		;
    	
    	try {
			if (!clsDatabaseFunctions.executeSQL(SQL, conn)) {
				super.addErrorMessage("Could not delete " + ParamObjectName + " with ID '"
						+ m_sid + "'.");
				return false;
			}
		} catch (SQLException e) {
			super.addErrorMessage("Could not delete " + ParamObjectName + " with ID '"
					+ m_sid + "' - " + e.getMessage());
			return false;
		}
		//Empty the values:
		initCriticalDateVariables();
		return true;
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

    	//Type
    	m_itype = m_itype.trim();
    	if(m_itype.compareToIgnoreCase("0") == 0 || m_itype.compareToIgnoreCase("") == 0) {
    		super.addErrorMessage("Critical date '" + m_itype + "' is invalid.  ");
    		bEntriesAreValid = false;
    	}
    	
        //Document number
    	
    	m_sdocnumber.trim();
        if (m_sdocnumber.compareToIgnoreCase("") == 0){
        	super.addErrorMessage("Document number connot be blank.");
        	bEntriesAreValid = false;
        	return bEntriesAreValid;
        }

       //validate document number exists based on type.
        int iDocumentNumber = 0;
        int iType = 0;
		try {
			iDocumentNumber = Integer.parseInt(getdocnumber());
		} catch (NumberFormatException e) {
			super.addErrorMessage("Invalid document number: '" + getdocnumber() + "'.");
			bEntriesAreValid = false;
			return bEntriesAreValid;
		}
		if (iDocumentNumber <= 0){
			super.addErrorMessage("Invalid document number: '" + getdocnumber() + "'.");
			bEntriesAreValid = false;
		}
		try {
			iType = Integer.parseInt(getitype());
		} catch (NumberFormatException e) {
			super.addErrorMessage("Invalid type: '" + getitype() + "'.");
			bEntriesAreValid = false;
			return bEntriesAreValid;
		}
		if (iType <= 0){
			super.addErrorMessage("Invalid type: '" + getitype() + "'.");
			bEntriesAreValid = false;
		}
		String SQL = "";
		switch(iType) {
		case SMTablecriticaldates.SALES_ORDER_RECORD_TYPE:
			SQL = "SELECT * FROM " + SMTableorderheaders.TableName 
			+ " WHERE (" + SMTableorderheaders.strimmedordernumber + "='" + Integer.toString(iDocumentNumber) + "')";
			break;
		case SMTablecriticaldates.SALES_LEAD_RECORD_TYPE:
			SQL = "SELECT * FROM " + SMTablebids.TableName 
			+ " WHERE (" + SMTablebids.lid + "= " + iDocumentNumber + ")";
			break;
		case SMTablecriticaldates.SALES_CONTACT_RECORD_TYPE:
			SQL = "SELECT * FROM " + SMTablesalescontacts.TableName 
			+ " WHERE (" + SMTablesalescontacts.id + "=" + iDocumentNumber + ")";
			break;
		case SMTablecriticaldates.PURCHASE_ORDER_RECORD_TYPE:
			SQL = "SELECT * FROM " + SMTableicpoheaders.TableName 
			+ " WHERE (" + SMTableicpoheaders.lid + "=" + iDocumentNumber + ")";
			break;
		case SMTablecriticaldates.AR_CALL_SHEET_RECORD_TYPE:
			SQL = "SELECT * FROM " + SMTablecallsheets.TableName 
			+ " WHERE (" + SMTablecallsheets.sID + "='" + Integer.toString(iDocumentNumber) + "')";
			break;
		default:
			super.addErrorMessage("document could not be validated with type: '" + Integer.toString(iType) 
			+ "' and document number '" + Integer.toString(iDocumentNumber)+ ".");
			return bEntriesAreValid;
		}
		try {
			ResultSet rsDocument = clsDatabaseFunctions.openResultSet(SQL, conn);
			if (!rsDocument.next()){
				super.addErrorMessage("document" + getdocnumber() + " could not be found.");
				bEntriesAreValid = false;
			}
			rsDocument.close();
		} catch (SQLException e) {
			super.addErrorMessage("Error [1396967234] validating document ID - " + e.getMessage());
			bEntriesAreValid = false;
		}
		
        
        //Critical date:
        //REQUIRED FIELD - can't be a blank:
        if (!clsDateAndTimeConversions.IsValidDateString("M/d/yyyy", m_datcriticaldate)){
        	super.addErrorMessage("Critical date '" + m_datcriticaldate + "' is invalid.  ");
        	bEntriesAreValid = false;
        }
        
        //Comment
        m_scomments = m_scomments.trim();
        
        
		//Responsible
		m_lresponsibleuserid = m_lresponsibleuserid.trim();
        if (m_lresponsibleuserid.compareToIgnoreCase("") == 0 || m_lresponsibleuserid.compareToIgnoreCase("0") == 0){
        	super.addErrorMessage("Responsible user id cannot be empty.");
        	bEntriesAreValid = false;
        	return bEntriesAreValid;
        }
        if (m_lresponsibleuserid.length() > SMTablecriticaldates.lResponsibleuseridLength){
        	super.addErrorMessage("Responsible user id code is too long.");
        	bEntriesAreValid = false;
        	return bEntriesAreValid;
        }
		m_sresponsibleuserfullname = m_sresponsibleuserfullname.trim();
        if (m_sresponsibleuserfullname.compareToIgnoreCase("") == 0 ){
        	super.addErrorMessage("Responsible user full name cannot be empty.");
        	bEntriesAreValid = false;
        	return bEntriesAreValid;
        }
        if (m_lresponsibleuserid.length() > SMTablecriticaldates.sResponsibleuserfullnameLength){
        	super.addErrorMessage("Responsible user full name is too long.");
        	bEntriesAreValid = false;
        	return bEntriesAreValid;
        }

		//Assigned by
		m_lassignedbyuserid = m_lassignedbyuserid.trim();
        if (m_lassignedbyuserid.compareToIgnoreCase("") == 0 || m_lassignedbyuserid.compareToIgnoreCase("0") == 0){
        	super.addErrorMessage("Assigned by user id cannot be empty.");
        	bEntriesAreValid = false;
        	return bEntriesAreValid;
        }
        if (m_lassignedbyuserid.length() > SMTablecriticaldates.lAssignedbyuseridLength){
        	super.addErrorMessage("Assigned by user id is too long.");
        	bEntriesAreValid = false;
        	return bEntriesAreValid;
        }
        
        
		m_sassignedbyuserfullname = m_sassignedbyuserfullname.trim();
        if (m_sassignedbyuserfullname.compareToIgnoreCase("") == 0){
        	super.addErrorMessage("Assigned by user full name cannot be empty.");
        	bEntriesAreValid = false;
        	return bEntriesAreValid;
        }
        if (m_sassignedbyuserfullname.length() > SMTablecriticaldates.sAssignedbyuserfullnameLength){
        	super.addErrorMessage("Assigned by user full name is too long.");
        	bEntriesAreValid = false;
        	return bEntriesAreValid;
        }

    	return bEntriesAreValid;
    }

    public String read_out_debug_data(){
    	
    	String sResult = "  ** SMCriticalDateEntry read out: ";
    	sResult += "\nID: " + m_sid;
    	sResult += "\nCritical date: " + this.getcriticaldate();
    	sResult += "\nDoc number: " + this.getdocnumber();
    	sResult += "\nResolved flag: " + this.getresolvedflag();
    	sResult += "\nComments: " + this.getcomments();
    	sResult += "\nTimestamp audit: " + this.gettimestampaudit();
    	sResult += "\nResponsible: " + this.getresponsibleuserfullname();
    	sResult += "\nAssigned: " + this.getassignedbyuserfullname();
    	sResult += "\nRec type: " + this.getitype();
    	    	
    	return sResult;
    }

    public void addErrorMessage(String sMsg){
    	super.addErrorMessage(sMsg);
    }
	public String getQueryString(){

		//Particular to the specific class
		String sQueryString = "";
		sQueryString += ParamID + "=" + clsServletUtilities.URLEncode(m_sid);
		sQueryString += "&" + ParamObjectName + "=" 
			+ clsServletUtilities.URLEncode(this.getObjectName());
		sQueryString += "&" + ParamID + "=" 
			+ clsServletUtilities.URLEncode(this.getid());
		sQueryString += "&" + ParamCriticalDate + "=" 
			+ clsServletUtilities.URLEncode(this.getcriticaldate());
		sQueryString += "&" + ParamDocNumber + "=" 
			+ clsServletUtilities.URLEncode(this.getdocnumber());
		sQueryString += "&" + ParamResolvedFlag + "=" 
			+ clsServletUtilities.URLEncode(this.getresolvedflag());
		sQueryString += "&" + ParamComments + "=" 
			+ clsServletUtilities.URLEncode(this.getcomments());
		sQueryString += "&" + ParamTimeStampAudit + "=" 
			+ clsServletUtilities.URLEncode(this.gettimestampaudit());
		sQueryString += "&" + ParamResponsibleUserFullName + "=" 
			+ clsServletUtilities.URLEncode(this.getresponsibleuserfullname());
		sQueryString += "&" + ParamAssignedbyUserFullName + "=" 
			+ clsServletUtilities.URLEncode(this.getassignedbyuserfullname());
		sQueryString += "&" + ParamiType + "=" 
			+ clsServletUtilities.URLEncode(this.getitype());
		
		return sQueryString;
	}

	public String getid() {
		return m_sid;
	}

	public void setid(String s) {
		this.m_sid = s;
	}

	public String getcriticaldate() {
		return m_datcriticaldate;
	}

	public void setcriticaldate(String date) {
		this.m_datcriticaldate = date;
	}

	public String getdocnumber() {
		return m_sdocnumber;
	}

	public void setdocnumber(String s) {
		this.m_sdocnumber = s;
	}

	public String getresolvedflag() {
		return m_sresolvedflag;
	}

	public void setresolvedflag(String s) {
		this.m_sresolvedflag = s;
	}

	public String getcomments() {
		return m_scomments;
	}

	public void setcomments(String s) {
		this.m_scomments = s;
	}

	public String getassignedbyuserfullname() {
		return m_sassignedbyuserfullname;
	}

	public void setassignedbyuserfullname(String s) {
		this.m_sassignedbyuserfullname = s;
	}
	
	public String getassignedbyuserid() {
		return m_lassignedbyuserid;
	}

	public void setassignedbyuserid(String s) {
		this.m_lassignedbyuserid = s;
	}

	public String gettimestampaudit() {
		return m_stimestampaudit;
	}

	public void settimestampaudit(String s) {
		this.m_stimestampaudit = s;
	}

	public String getresponsibleuserfullname() {
		return m_sresponsibleuserfullname;
	}

	public void setresponsibleuserfullname(String s) {
		this.m_sresponsibleuserfullname = s;
	}
	
	public String getresponsibleuserid() {
		return m_lresponsibleuserid;
	}

	public void setresponsibleuserid(String s) {
		this.m_lresponsibleuserid = s;
	}

	public String getitype() {
		return m_itype;
	}

	public void setitype(String s) {
		this.m_itype = s;
	}

    private void initCriticalDateVariables(){
		m_sid = "-1";
		m_datcriticaldate = clsDateAndTimeConversions.now("M/d/yyyy");
		m_sdocnumber = "";
		m_sresolvedflag = "0";
		m_scomments = "";
		m_stimestampaudit = clsDateAndTimeConversions.now("yyyy-MM-dd hh:mm:ss");
		m_sresponsibleuserfullname = "";
		m_lresponsibleuserid = "";
		m_sassignedbyuserfullname = "";
		m_lassignedbyuserid = "";
		m_itype = "0";
		
		super.initVariables();
		super.setObjectName(ParamObjectName);
    }
    
    public static String addNewCriticalDateLink(String iType, String sDocNumber, String sUserID, ServletContext context, String sDBID) {
    	String s = "";
    	s +="<a name=\"CriticalDatesFooter\">";
    	s+= "<FONT SIZE=2><a href=\"" + SMUtilities.getURLLinkBase(context) + "smcontrolpanel.SMCriticalDateEdit?" +
				SMCriticalDateEntry.ParamID + "=-1" +
				"&" + SMCriticalDateEntry.ParamCriticalDate + "=" + clsDateAndTimeConversions.utilDateToString(new Date(System.currentTimeMillis()), "M/d/yyyy") +
				"&" + SMCriticalDateEntry.ParamDocNumber + "=" + sDocNumber +
				"&" + SMCriticalDateEntry.ParamResponsibleUserID + "=" + sUserID +
				"&" + SMCriticalDateEntry.ParamAssignedbyUserID + "=" + sUserID +
				"&" + SMCriticalDateEntry.ParamTimeStampAudit + "=" + clsDateAndTimeConversions.utilDateToString(new Date(System.currentTimeMillis()), "yyyy-MM-dd hh:mm:ss") +
				"&" + SMCriticalDateEntry.ParamiType + "=" + iType +
				"&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID + 
				"\">" + "Add new critical date" + "</A></FONT><BR>"
				
			;
    	
    	return s;
    }
    
	public static String listCriticalDates(
			int iType,
			String sDocNumber,
			String sMaxTableWitdh,
			String sTableColor,
			ServletContext context, 
			String sDBID, 
			String sUserID,
			boolean bIncludeAddLink
			) {
		String s = "";
		s += "<br><b><u><FONT SIZE=2>Critical Dates</FONT></u></b><br>";

		if (sDocNumber.compareToIgnoreCase("") != 0){
			String SQL = "SELECT * "
				+ " FROM " + SMTablecriticaldates.TableName			
				+ " WHERE ("
				+ "		(" + SMTablecriticaldates.itype + " = " + Integer.toString(iType) + ")"
				+ " AND (" + SMTablecriticaldates.sdocnumber + " = '" + sDocNumber + "')"
				+ " )"
				+ " ORDER BY " + SMTablecriticaldates.sCriticalDate + " , " + SMTablecriticaldates.sId
				;

			boolean bCriticalDates  = true; 
			
			try {
				ResultSet rs = clsDatabaseFunctions.openResultSet(
					SQL, 
					context, 
					sDBID, 
					"MySQL", 
					" - userID: " + sUserID
			);

			if(rs.isBeforeFirst()) {
				
				s += "<TABLE BORDER=0 WIDTH=100%  cellspacing=0 cellpadding=1 style= \"max-width:"+ sMaxTableWitdh +";"
						+ " background-color: " + sTableColor + "; \" ><TR>";
				s +="<TD class = \" centerjustifiedheading \" ><FONT SIZE=2><B>ID</B></FONT></TD>";
				s +="<TD class = \" leftjustifiedheading \"><FONT SIZE=2><B>Date</B></FONT></TD>";
				s +="<TD class = \" leftjustifiedheading \"><FONT SIZE=2><B>Resolved?</B></FONT></TD>";
				s +="<TD class = \" leftjustifiedheading \"><FONT SIZE=2><B>Responsible</FONT></B></TD>";
				s +="<TD class = \" leftjustifiedheading \"><FONT SIZE=2><B>Assigned&nbsp;by</FONT></B></TD>";
				s +="<TD class = \" leftjustifiedheading \"><FONT SIZE=2><B>Comments</FONT></B></TD>";
			}
				
			boolean bOddRow = false;
			String sBackgroundColor = "";
			while (rs.next()){
				if(bOddRow){
					sBackgroundColor = "\"" + "#f3f3f3" + "\"";
				}else{
					sBackgroundColor = "\"" + "#FFFFFF" + "\"";
				}
				if (rs.getLong(SMTablecriticaldates.TableName + "." + SMTablecriticaldates.sId) > 0L){
					s +="<TR  bgcolor =" + sBackgroundColor +" >";
					String sCriticalDateID = Long.toString(rs.getLong(SMTablecriticaldates.TableName + "." + SMTablecriticaldates.sId)); 
					String sCriticalDateIDLink = "<A HREF=\"" + SMUtilities.getURLLinkBase(context) 
					+ "smcontrolpanel.SMCriticalDateEdit"
					+ "?" + SMTablecriticaldates.sId + "=" + sCriticalDateID 
					+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
					+ "\">" + clsServletUtilities.Fill_In_Empty_String_For_HTML_Cell(sCriticalDateID) + "</A>";
	
					if (bCriticalDates){
						s +="<TD ALIGN=LEFT VALIGN=TOP><FONT SIZE=2>" + sCriticalDateIDLink + "</FONT></TD>";	
					}else{
						s +="<TD ALIGN=LEFT VALIGN=TOP><FONT SIZE=2>" + sCriticalDateID + "</FONT></TD>";
					}
	
					String sFontColor = "BLACK";
					//Critical Date
					s +="<TD VALIGN=TOP><FONT SIZE=2 COLOR=" + sFontColor + ">" 
						+ clsDateAndTimeConversions.resultsetDateStringToString(
							rs.getString(SMTablecriticaldates.TableName + "." + SMTablecriticaldates.sCriticalDate)) 
						+ "</FONT></TD>"
					;
					
					//Resolved:
					s +="<TD  ALIGN=CENTER VALIGN=TOP><FONT SIZE=2 COLOR=" + sFontColor + " >";

					if(rs.getInt(SMTablecriticaldates.TableName + "." + SMTablecriticaldates.sResolvedFlag) == 1) {
						s +="Yes";
					}else {
						s +="No";
					}
					
					s +="</FONT></TD>";
					
					//Responsible
					s +="<TD VALIGN=TOP nowrap><FONT SIZE=2 COLOR=" + sFontColor + ">" 
						+ rs.getString(SMTablecriticaldates.TableName + "." + SMTablecriticaldates.sresponsibleuserfullname) 
						+ "</FONT></TD>"
					;
	
					//Assigned by				
					s +="<TD VALIGN=TOP nowrap><FONT SIZE=2 COLOR=" + sFontColor + ">" 
							+ rs.getString(SMTablecriticaldates.TableName + "." + SMTablecriticaldates.sassignedbyuserfullname) 
							+ "</FONT></TD>"
						;
					
					//Comments
					s +="<TD VALIGN=TOP><FONT SIZE=2 COLOR=" + sFontColor + ">" 
							+ rs.getString(SMTablecriticaldates.TableName + "." + SMTablecriticaldates.sComments) 
							+ "</FONT></TD>"
						;
					s +="</TR>";
					
					bOddRow = !bOddRow;
				}
			}
			rs.close();
			} catch (SQLException e) {
				System.out.println("In " + "Error [1428417888] with SQL: " + SQL + " - " + e.getMessage());
			}
		}
		
		if(bIncludeAddLink) {
			//Add a link add new critical date here:
			s +=SMCriticalDateEntry.addNewCriticalDateLink(
				Integer.toString(iType),
				sDocNumber,
				sUserID,
				context,
				sDBID);
		}
		
		s +="</TABLE>";
		
		
		
		return s;
	}
}