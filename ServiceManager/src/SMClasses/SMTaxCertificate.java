package SMClasses;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import smar.ARCustomer;
import smcontrolpanel.SMMasterEditSelect;
import SMDataDefinition.SMTabletaxcertificates;
import SMDataDefinition.SMTableusers;
import ServletUtilities.clsMasterEntry;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsDateAndTimeConversions;
import ServletUtilities.clsManageRequestParameters;

public class SMTaxCertificate extends clsMasterEntry{
	
	public static final String ParamObjectName = "Tax Certificate";
	
	
	public static final String Paramlid = "lid";
	public static final String Paramscreatedbyfullname = "screatedbyfullname";
	public static final String Paramlcreatedbyuserid = "lcreatedbyuserid";
	public static final String Paramdatreceived = "datreceived";
	public static final String Paramdatcreated = "datcreated";
	public static final String Paramdatexpired = "datexpired";
	public static final String Paramdatissued = "datissued";
	public static final String Paramscustomername = "scustomername";
	public static final String Paramscustomernumber = "scustomernumber";
	public static final String Paramsexemptnumber = "sexemptnumber";
	public static final String Paramsjobnumber = "sjobnumber";
	public static final String Paramstaxjurisdiction = "staxjurisdiction";
	public static final String ParammNotes = "mNotes";
	public static final String Paramsprojectlocation = "sprojectlocation";
	public static final String Paramsgdoclink = "sgdoclink";

	
	private String m_slid;
	private String m_screatedbyfullname;
	private String m_lcreatedbyuserid;
	private String m_datcreated;
	private String m_datexpired;
	private String m_datissued;
	private String m_datreceived;
	private String m_scustomername;
	private String m_scustomernumber;
	private String m_sexemptnumber;
	private String m_sjobnumber;
	private String m_staxjurisdiction;
	private String m_mnotes;
	private String m_sprojectlocation;
	private String m_snewrecord;
	private String m_sgdoclink;
	
	private boolean bDebugMode = false;
	
    public SMTaxCertificate() {
		super();
		initEntryVariables();
        }
    
    public SMTaxCertificate(HttpServletRequest req){
		super(req);
		initEntryVariables();
		m_slid = clsManageRequestParameters.get_Request_Parameter(
				SMTaxCertificate.Paramlid, req).trim();
		m_datcreated = clsManageRequestParameters.get_Request_Parameter(
			SMTaxCertificate.Paramdatcreated, req).trim().replace("&quot;", "\"");
		if(m_datcreated.compareToIgnoreCase("") == 0){
			m_datcreated = EMPTY_DATETIME_STRING;
		}
		m_screatedbyfullname = clsManageRequestParameters.get_Request_Parameter(
			SMTaxCertificate.Paramscreatedbyfullname, req).trim().replace("&quot;", "\"");
		m_lcreatedbyuserid = clsManageRequestParameters.get_Request_Parameter(
				SMTaxCertificate.Paramlcreatedbyuserid, req).trim().replace("&quot;", "\"");
		m_datexpired = clsManageRequestParameters.get_Request_Parameter(
			SMTaxCertificate.Paramdatexpired, req).trim().replace("&quot;", "\"");
		if(m_datexpired.compareToIgnoreCase("") == 0){
			m_datexpired = EMPTY_DATE_STRING;
		}
		m_datissued = clsManageRequestParameters.get_Request_Parameter(
			SMTaxCertificate.Paramdatissued, req).trim().replace("&quot;", "\"");
		if(m_datissued.compareToIgnoreCase("") == 0){
			m_datissued = EMPTY_DATE_STRING;
		}
		m_datreceived = clsManageRequestParameters.get_Request_Parameter(
				SMTaxCertificate.Paramdatreceived, req).trim().replace("&quot;", "\"");
			if(m_datreceived.compareToIgnoreCase("") == 0){
				m_datreceived = EMPTY_DATE_STRING;
			}
		m_scustomername = clsManageRequestParameters.get_Request_Parameter(
			SMTaxCertificate.Paramscustomername, req).trim().replace("&quot;", "\"");
		m_scustomernumber = clsManageRequestParameters.get_Request_Parameter(
			SMTaxCertificate.Paramscustomernumber, req).trim().replace("&quot;", "\"");
		m_sexemptnumber = clsManageRequestParameters.get_Request_Parameter(
				SMTaxCertificate.Paramsexemptnumber, req).trim().replace("&quot;", "\"");
		m_sjobnumber = clsManageRequestParameters.get_Request_Parameter(
				SMTaxCertificate.Paramsjobnumber, req).trim();
		m_staxjurisdiction = clsManageRequestParameters.get_Request_Parameter(
				SMTaxCertificate.Paramstaxjurisdiction, req).trim().replace("&quot;", "\"");
		m_mnotes = clsManageRequestParameters.get_Request_Parameter(
				SMTaxCertificate.ParammNotes, req).trim().replace("&quot;", "\"");
		m_sgdoclink = clsManageRequestParameters.get_Request_Parameter(
				SMTaxCertificate.Paramsgdoclink, req).trim().replace("&quot;", "\"");
		m_sprojectlocation = clsManageRequestParameters.get_Request_Parameter(
				SMTaxCertificate.Paramsprojectlocation, req).trim().replace("&quot;", "\"");
		
	    m_snewrecord = clsManageRequestParameters.get_Request_Parameter(SMMasterEditSelect.SUBMIT_ADD_BUTTON_NAME, req).trim().replace("&quot;", "\"");
    }
    public void load (ServletContext context, String sDBIB, String sUserID, String sUserFullName) throws Exception{
    	Connection conn = clsDatabaseFunctions.getConnection(
    			context, 
    			sDBIB, 
    			"MySQL", 
    			this.toString() + " - user: " + sUserID + " - " + sUserFullName
    			);
    	
    	if (conn == null){
    		throw new Exception("Error [1426192859] opening data connection to load " + ParamObjectName + ".");
    	}
    	
    	try {
			load (conn);
		} catch (Exception e) {
			clsDatabaseFunctions.freeConnection(context, conn, "[1547067765]");
			throw new Exception(e.getMessage());
		}
    	clsDatabaseFunctions.freeConnection(context, conn, "[1547067766]");
    }
    public boolean load (Connection conn) throws Exception{
    	return load (m_slid, conn);
    }
    private boolean load (String sID, Connection conn) throws Exception{

    	sID = sID.trim();
    	if (sID.compareToIgnoreCase("") == 0){
    		throw new Exception(" Error [1426192860] ID code cannot be blank when loading " + ParamObjectName + ".");
    	}
		
		String SQL = "SELECT * FROM " + SMTabletaxcertificates.TableName
			+ " WHERE ("
				+ SMTabletaxcertificates.lid + " = " + sID
			+ ")";
		if (bDebugMode){
			System.out.println("[1579186380] In " + this.toString() + " - load SQL = " + SQL);
		}
		try {
			ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
			if (rs.next()) {
				//Load the variables here:
				m_slid = Long.toString(rs.getLong(SMTabletaxcertificates.lid));
				m_datcreated = clsDateAndTimeConversions.resultsetDateTimeToTheMinuteStringToString(
					rs.getString(SMTabletaxcertificates.datcreated));
				m_screatedbyfullname = rs.getString(SMTabletaxcertificates.screatedbyfullname).trim();
				m_lcreatedbyuserid = rs.getString(SMTabletaxcertificates.lcreatedbyuserid).trim();
				m_datreceived = clsDateAndTimeConversions.resultsetDateStringToString(rs.getString(SMTabletaxcertificates.datreceived));
				m_datissued = clsDateAndTimeConversions.resultsetDateStringToString(rs.getString(SMTabletaxcertificates.datissued));
				m_datexpired = clsDateAndTimeConversions.resultsetDateStringToString(rs.getString(SMTabletaxcertificates.datexpired));
				m_scustomername = rs.getString(SMTabletaxcertificates.scustomername).trim();
				m_scustomernumber = rs.getString(SMTabletaxcertificates.scustomernumber).trim();
				m_sexemptnumber = rs.getString(SMTabletaxcertificates.sexemptnumber).trim();
				m_mnotes = rs.getString(SMTabletaxcertificates.mnotes).trim();
			  //m_sexemptnumber = rs.getString(SMTabletaxcertificates.sexemptnumber).trim();
				m_sjobnumber = rs.getString(SMTabletaxcertificates.sjobnumber).trim();
				m_staxjurisdiction = rs.getString(SMTabletaxcertificates.staxjurisdiction).trim();
				m_sprojectlocation = rs.getString(SMTabletaxcertificates.sprojectlocation).trim();
				m_sgdoclink = rs.getString(SMTabletaxcertificates.sgdoclink).trim();
				rs.close();
			} else {
				rs.close();
				throw new Exception("Could not load tax certificate with ID '" + sID + "'.");
			}
		} catch (Exception e) {
			throw new Exception("Error reading " + ParamObjectName + " for lid : '" + sID
				+ "' - " + e.getMessage());
		}
		return true;
    }
    
    public void save_without_data_transaction (ServletContext context, String sDBIB, String sUser, String sUserID, String sUserFullName) throws Exception{
    	
       	Connection conn = clsDatabaseFunctions.getConnection(
    			context, 
    			sDBIB, 
    			"MySQL", 
    			this.toString() + " - user: " + sUserID + " - " + sUserFullName
    			);
    	
    	if (conn == null){
    		throw new Exception("Error [1408649178] opening data connection.");
    	}
    	
    	try {
			save_without_data_transaction (conn, sUser);
		} catch (Exception e) {
			clsDatabaseFunctions.freeConnection(context, conn, "[1547067767]");
			throw new Exception(e.getMessage());
		}
    	clsDatabaseFunctions.freeConnection(context, conn, "[1547067768]");
    	
    }
    public void save_without_data_transaction (Connection conn, String sUser) throws Exception{


    	try {
			validate_entry_fields(conn);
		} catch (Exception e1) {
			throw new Exception (e1.getMessage());
		}
    	
    	//If it's a new record OR if it's being 'resolved', then we need to get the user's info:
    	String SQL = "";
    	long lid;
		try {
			lid = Long.parseLong(getslid());
		} catch (Exception e1) {
			throw new Exception("Error [1408653581] parsing " + ParamObjectName + " lid '" + this.getslid() + "' - " + e1.getMessage());
		}
    	if (lid < 1){
    		//It's a new record
    		setsnewrecord("1");
    	}
    	//If we are inserting, we need to get the user's full name:
    	String sUsersFullName = "N/A";
    	long sUserID = 0;
    	if (getsnewrecord().compareToIgnoreCase("1") == 0){
	    	SQL = "SELECT"
	    		+ " " + SMTableusers.lid
	    		+ ", " + SMTableusers.sUserFirstName
	    		+ ", " + SMTableusers.sUserLastName
	    		+ " FROM " + SMTableusers.TableName
	    		+ " WHERE ("
	    			+ "(" + SMTableusers.sUserName + " = '" + sUser + "')"
	    		+ ")"
	    	;
	    	try {
				ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
				if (rs.next()){
					sUsersFullName = rs.getString(SMTableusers.sUserFirstName).trim() 
							+ " " + rs.getString(SMTableusers.sUserLastName).trim();
					sUserID = rs.getInt(SMTableusers.lid);
				}
				rs.close();
			} catch (Exception e) {
				throw new Exception("Error [1408649179] reading user full name to save record - " + e.getMessage());
			}
    	}

    	if (getsnewrecord().compareToIgnoreCase("1") == 0){
    		setlcreatedbyuserid(Long.toString(sUserID));
    		setscreatedbyfullname(sUsersFullName);
    		
    	}

		//If it's a new record, do an insert:
    	if (getsnewrecord().compareToIgnoreCase("1") == 0){
			SQL = "INSERT INTO " + SMTabletaxcertificates.TableName + " ("
				+ SMTabletaxcertificates.datcreated
				+ ", " + SMTabletaxcertificates.screatedbyfullname
				+ ", " + SMTabletaxcertificates.lcreatedbyuserid
				+ ", " + SMTabletaxcertificates.datexpired
				+ ", " + SMTabletaxcertificates.datissued
				+ ", " + SMTabletaxcertificates.datreceived
				+ ", " + SMTabletaxcertificates.scustomername
				+ ", " + SMTabletaxcertificates.scustomernumber
				+ ", " + SMTabletaxcertificates.sexemptnumber
				+ ", " + SMTabletaxcertificates.sjobnumber
				+ ", " + SMTabletaxcertificates.staxjurisdiction
				+ ", " + SMTabletaxcertificates.mnotes
				+ ", " + SMTabletaxcertificates.sgdoclink
				+ ", " + SMTabletaxcertificates.sprojectlocation
				+ ") VALUES ("
				+ "NOW()"
				+ ", '" + clsDatabaseFunctions.FormatSQLStatement(getscreatedbyfullname().trim()) + "'"
				+ ", "  + clsDatabaseFunctions.FormatSQLStatement(getlcreatedbyuserid().trim()) + ""
				+ ", '" + clsDateAndTimeConversions.stdDateStringToSQLDateString(getdatexpired().trim()) + "'"
				+ ", '" + clsDateAndTimeConversions.stdDateStringToSQLDateString(getdatissued().trim()) + "'"
				+ ", '" + clsDateAndTimeConversions.stdDateStringToSQLDateString(getdatreceived().trim()) + "'"
				+ ", '" + clsDatabaseFunctions.FormatSQLStatement(getscustomername().trim()) + "'"
				+ ", '" + clsDatabaseFunctions.FormatSQLStatement(getscustomernumber().trim()) + "'"
				+ ", '" + clsDatabaseFunctions.FormatSQLStatement(getsexemptnumber().trim()) + "'"
				+ ", '" + clsDatabaseFunctions.FormatSQLStatement(getsjobnumber().trim()) + "'"
				+ ", '" + clsDatabaseFunctions.FormatSQLStatement(getstaxjurisdiction().trim()) + "'"
				+ ", '" + clsDatabaseFunctions.FormatSQLStatement(getmnotes().trim()) + "'"
				+ ", '" + clsDatabaseFunctions.FormatSQLStatement(getsgdoclink().trim()) + "'"
				+ ", '" + clsDatabaseFunctions.FormatSQLStatement(getsprojectlocation().trim()) + "'"
				+ ")"
			;

    	}else{
			SQL = " UPDATE " + SMTabletaxcertificates.TableName + " SET "
			    +  " " + SMTabletaxcertificates.datcreated + " = '" + clsDateAndTimeConversions.stdDateTimeToSQLDateTimeString(getdatcreated()) + "'"
				+ ", " + SMTabletaxcertificates.screatedbyfullname + " = '" + clsDatabaseFunctions.FormatSQLStatement(getscreatedbyfullname().trim()) + "'"
				+ ", " + SMTabletaxcertificates.lcreatedbyuserid + " = " + clsDatabaseFunctions.FormatSQLStatement(getlcreatedbyuserid().trim()) + ""
				+ ", " + SMTabletaxcertificates.datexpired + " = '" + clsDateAndTimeConversions.stdDateStringToSQLDateString(getdatexpired()) + "'"
				+ ", " + SMTabletaxcertificates.datissued + " = '" + clsDateAndTimeConversions.stdDateStringToSQLDateString(getdatissued()) + "'"
				+ ", " + SMTabletaxcertificates.datreceived + " = '" + clsDateAndTimeConversions.stdDateStringToSQLDateString(getdatreceived()) + "'"
				+ ", " + SMTabletaxcertificates.scustomername + " = '" + clsDatabaseFunctions.FormatSQLStatement(getscustomername().trim()) + "'"
				+ ", " + SMTabletaxcertificates.scustomernumber + " = '" + clsDatabaseFunctions.FormatSQLStatement(getscustomernumber().trim()) + "'"
				+ ", " + SMTabletaxcertificates.sexemptnumber + " = '" + clsDatabaseFunctions.FormatSQLStatement(getsexemptnumber().trim()) + "'"
				+ ", " + SMTabletaxcertificates.sjobnumber + " = '" + clsDatabaseFunctions.FormatSQLStatement(getsjobnumber().trim()) + "'"
				+ ", " + SMTabletaxcertificates.staxjurisdiction + " = '" + clsDatabaseFunctions.FormatSQLStatement(getstaxjurisdiction().trim()) + "'"
				+ ", " + SMTabletaxcertificates.mnotes + " = '" + clsDatabaseFunctions.FormatSQLStatement(getmnotes().trim()) + "'"
				+ ", " + SMTabletaxcertificates.sgdoclink + " = '" + clsDatabaseFunctions.FormatSQLStatement(getsgdoclink().trim()) + "'"
				+ ", " + SMTabletaxcertificates.sprojectlocation + " = '" + clsDatabaseFunctions.FormatSQLStatement(getsprojectlocation().trim()) + "'"
				+ " WHERE ("
					+ "(" + SMTabletaxcertificates.lid + " = " + getslid() + ")"
				+ ")"
			;
    	}
		if (bDebugMode){
			System.out.println("[1579186385] In " + this.toString() + " - save SQL = " + SQL);
		}
		try{
		    Statement stmt = conn.createStatement();
		    stmt.executeUpdate(SQL);
		}catch (Exception ex) {
			throw new Exception ("Error [1408649281] in insert/update with SQL: " + SQL + " - " + ex.getMessage());
		}
		//Update the ID if it's an insert:
		if (getsnewrecord().compareToIgnoreCase("1") == 0){
			SQL = "SELECT last_insert_id()";
			try {
				ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
				if (rs.next()) {
					m_slid = Long.toString(rs.getLong(1));
				}else {
					m_slid = "0";
				}
				rs.close();
			} catch (SQLException e) {
				throw new Exception("Could not get last ID number - " + e.getMessage());
			}
			//If something went wrong, we can't get the last ID:
			if (m_slid.compareToIgnoreCase("0") == 0){
				throw new Exception("Could not get last ID number.");
			}
		}
    }

    public void delete (ServletContext context, String sDBIB, String sUserID, String sUserFullName) throws Exception{
    	
    	Connection conn = clsDatabaseFunctions.getConnection(
    			context, 
    			sDBIB, 
    			"MySQL", 
    			this.toString() + " - user: " + sUserID + " - " + sUserFullName
    			);
    	
    	if (conn == null){
    		throw new Exception ("Error [1408649281] opening data connection.");
    	}
    	
    	try {
			delete (conn);
		} catch (Exception e) {
			clsDatabaseFunctions.freeConnection(context, conn, "[1547067763]");
			throw new Exception(e.getMessage());
		}
    	clsDatabaseFunctions.freeConnection(context, conn, "[1547067764]");
    }
    public void delete (Connection conn) throws Exception{
    	
    	//Validate deletions
    	String SQL = "";

    	//Delete record:
    	if (!clsDatabaseFunctions.start_data_transaction(conn)){
    		throw new Exception("Error [1408649282] - Could not start transaction when deleting " + ParamObjectName + ".");
    	}
    	SQL = "DELETE FROM " + SMTabletaxcertificates.TableName
    		+ " WHERE ("
    			+ SMTabletaxcertificates.lid + " = " + this.getslid()
    		+ ")"
    		;
    	
		try{
		    Statement stmt = conn.createStatement();
		    stmt.executeUpdate(SQL);
		}catch (Exception ex) {
			clsDatabaseFunctions.rollback_data_transaction(conn);
			throw new Exception("Error [1408649283] - Could not delete " + ParamObjectName + " with ID " + getslid() + " with SQL: " + SQL + " - " + ex.getMessage());
		}
		
		if (!clsDatabaseFunctions.commit_data_transaction(conn)){
			clsDatabaseFunctions.rollback_data_transaction(conn);
			throw new Exception("Error [1408649284] - Could not commit data transaction while deleting " + ParamObjectName + ".");
		}
		//Empty the values:
		initEntryVariables();
    }

    public void validate_entry_fields (Connection conn) throws Exception{
        //Validate the entries here:
    	String sErrors = "";
    	m_slid = m_slid.trim();
    	if (m_slid.compareToIgnoreCase("") == 0){
    		m_slid = "-1";
    	}

    	try {
			@SuppressWarnings("unused")
			long lID = Long.parseLong(m_slid);
		} catch (Exception e) {
			throw new Exception("Invalid ID: '" + m_slid + "'.  ");
		}

    	
    	m_datcreated = m_datcreated.trim();
        if (m_datcreated.compareToIgnoreCase("") == 0){
        	m_datcreated = EMPTY_DATETIME_STRING;
        }
        if (m_datcreated.compareToIgnoreCase(EMPTY_DATETIME_STRING) != 0){
        	if (!clsDateAndTimeConversions.IsValidDateString("M/d/yyyy hh:ss a", m_datcreated)){
        		sErrors += "Date created is invalid: '" + m_datcreated + "'.  ";
        	}
        }
        
        m_lcreatedbyuserid = m_lcreatedbyuserid.trim();
    	if (m_lcreatedbyuserid.compareToIgnoreCase("") == 0){
    		m_lcreatedbyuserid = "0";
    	}

        m_screatedbyfullname = m_screatedbyfullname.trim();
        if (m_screatedbyfullname.length() > SMTabletaxcertificates.screatedbyfullnamelength){
        	sErrors += "Created by full name cannot be more than " + Integer.toString(SMTabletaxcertificates.screatedbyfullnamelength) + " characters.  ";
        }

        m_datissued = m_datissued.trim();
        if (m_datissued.compareToIgnoreCase("") == 0){
        	m_datissued = EMPTY_DATE_STRING;
        }
        if (m_datissued.compareToIgnoreCase(EMPTY_DATE_STRING) != 0){
        	if (!clsDateAndTimeConversions.IsValidDateString("MM/dd/yyyy", m_datissued)){
        		sErrors += "Date issued is invalid: '" + m_datissued + "'.  ";
        	}
        }
        m_datexpired = m_datexpired.trim();
        if (m_datexpired.compareToIgnoreCase("") == 0){
        	m_datexpired = EMPTY_DATE_STRING;
        }
       if (m_datexpired.compareToIgnoreCase(EMPTY_DATE_STRING) != 0){
        	if (!clsDateAndTimeConversions.IsValidDateString("MM/dd/yyyy", m_datexpired)){
        		sErrors += "Date expired is invalid: '" + m_datexpired + "'.  ";
        	}
        }
       
    	 m_datreceived = m_datreceived.trim();
         if (m_datreceived.compareToIgnoreCase("") == 0){
         	m_datreceived = EMPTY_DATE_STRING;
         }
         if (m_datreceived.compareToIgnoreCase(EMPTY_DATE_STRING) != 0){
         	if (!clsDateAndTimeConversions.IsValidDateString("MM/dd/yyyy", m_datreceived)){
         		sErrors += "Date received is invalid: '" + m_datreceived + "'.  ";
         	}
         }
        m_scustomername = m_scustomername.trim();

        if (m_scustomername.length() > SMTabletaxcertificates.scustomernamelength){
        	sErrors += "Customer name cannot be more than " + Integer.toString(SMTabletaxcertificates.scustomernamelength) + " characters.  ";
        }

        m_scustomernumber = m_scustomernumber.trim();
        ARCustomer customer = new ARCustomer(m_scustomernumber);
		if(!customer.load(conn)){
			if(m_scustomernumber.compareToIgnoreCase("") != 0){			
				sErrors += "Customer number is not valid.";
			}	
	        if (m_scustomernumber.length() > SMTabletaxcertificates.scustomernumberlength){
	        	sErrors += "Customer number cannot be more than " + Integer.toString(SMTabletaxcertificates.scustomernumberlength) + " characters.  ";
	        }
		}

        m_sexemptnumber = m_sexemptnumber.trim();
        if (m_sexemptnumber.length() > SMTabletaxcertificates.sexemptnumberlength){
        	
        	sErrors += "Tax exempt number cannot be more than " + Integer.toString(SMTabletaxcertificates.sexemptnumberlength) + " characters.  ";
        }
        m_sjobnumber = m_sjobnumber.trim();

        SMOrderHeader order = new SMOrderHeader();
		order.setM_sOrderNumber(m_sjobnumber);
 
		if(!order.load(conn)){

			if(m_sjobnumber.compareToIgnoreCase("") != 0){			
				sErrors += "Order number is not valid.";
			}	
			
	        if (m_sjobnumber.length() > SMTabletaxcertificates.sjobnumberlength){
	        	sErrors += "Job number cannot be more than " + Integer.toString(SMTabletaxcertificates.sjobnumberlength) + " characters.  ";
	        }
		}

        
        if (m_sjobnumber.compareToIgnoreCase("") == 0){
        }else{
        
    	 		   try {
					@SuppressWarnings("unused")
					long ljobnumber = Long.parseLong(m_sjobnumber);
				} catch (Exception e) {
					sErrors += "Invalid job number: '" + m_sjobnumber + "'. ";
				}
    	}
        m_staxjurisdiction = m_staxjurisdiction.trim();
        if (m_staxjurisdiction.length() > SMTabletaxcertificates.staxjurisdictionlength){
        	sErrors += "Tax jurisdiction cannot be more than " + Integer.toString(SMTabletaxcertificates.staxjurisdictionlength) + " characters.  ";
        }
        
        m_sprojectlocation = m_sprojectlocation.trim();
        if (m_sprojectlocation.length() > SMTabletaxcertificates.sprojectlocationlength){
        	sErrors += "Project(s) location cannot be more than " + Integer.toString(SMTabletaxcertificates.sprojectlocationlength) + " characters.  ";
        }
        
    	if (sErrors.compareToIgnoreCase("") != 0){
    		throw new Exception(sErrors);
    	}
    }
        
	public String getslid() {
		return m_slid;
	}
	public void setslid(String slid) {
		m_slid = slid;
	}
	public String getlcreatedbyuserid() {
		return m_lcreatedbyuserid;
	}
	public void setlcreatedbyuserid(String lcreatedbyuserid) {
		m_lcreatedbyuserid = lcreatedbyuserid;
	}
	public String getscreatedbyfullname() {
		return m_screatedbyfullname;
	}
	public void setscreatedbyfullname(String screatedbyfullname) {
		m_screatedbyfullname = screatedbyfullname;
	}
	public String getdatcreated() {
		return m_datcreated;
	}
	public void setdatcreated(String datcreated) {
		m_datcreated = datcreated;
	}
	public String getdatexpired() {
		return m_datexpired;
	}
	public void setdatexpired(String datexpired) {
		m_datexpired = datexpired;
	}
	public String getdatissued() {
		return m_datissued;
	}
	public void setdatissued(String datissued) {
		m_datissued = datissued;
	}
	public String getdatreceived() {
		return m_datreceived;
	}
	public void setdatreceived(String datreceived) {
		m_datreceived = datreceived;
	}
	public String getscustomername() {
		return m_scustomername;
	}
	public void setscustomername(String scustomername) {
		m_scustomername = scustomername;
	}
	public String getscustomernumber() {
		return m_scustomernumber;
	}
	public void setscustomernumber(String scustomernumber) {
		m_scustomernumber= scustomernumber;
	}
	public String getsexemptnumber() {
		return m_sexemptnumber;
	}
	public void setsexemptnumber(String sexemptnumber) {
		m_sexemptnumber= sexemptnumber;
	}
	public String getsjobnumber() {
		return m_sjobnumber;
	}
	public void setsjobnumber(String sjobnumber) {
		m_sjobnumber= sjobnumber;
	}
	public String getstaxjurisdiction() {
		return m_staxjurisdiction;
	}
	public void setstaxjurisdiction(String staxjurisdiction) {
		m_staxjurisdiction= staxjurisdiction;
	}
	public String getmnotes() {
		return m_mnotes;
	}
	public void setmnotes(String mnotes) {
		m_mnotes= mnotes;
	}
	public String getsnewrecord() {
		return m_snewrecord;
	}
	public void setsnewrecord(String snewrecord) {
		m_snewrecord= snewrecord;
	}
	public String getObjectName(){
		return ParamObjectName;
	}
	public String getsgdoclink() {
		return m_sgdoclink;
	}
	public void setsgdoclink(String sgdoclink) {
		m_sgdoclink= sgdoclink;
	}
	public String getsprojectlocation() {
		return m_sprojectlocation;
	}
	public void setsprojectlocation(String sprojectlocation) {
		m_sprojectlocation = sprojectlocation;
	}
	
    private void initEntryVariables(){
    	m_slid = "-1";
    	m_datcreated = EMPTY_DATETIME_STRING;
    	m_lcreatedbyuserid = "0";
    	m_screatedbyfullname = "";
    	m_datexpired = EMPTY_DATE_STRING;
    	m_datissued = EMPTY_DATE_STRING;
    	m_datreceived = EMPTY_DATE_STRING;
    	m_scustomername= "";
    	m_scustomernumber= "";
    	m_sexemptnumber= "";
    	m_sjobnumber= "";
    	m_staxjurisdiction= "";
    	m_mnotes= " ";
     	m_snewrecord = "1";
     	m_sgdoclink= " ";
     	m_sprojectlocation = "";
	}

}