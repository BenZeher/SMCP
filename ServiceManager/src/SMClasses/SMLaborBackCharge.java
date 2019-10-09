package SMClasses;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import smap.APVendor;
import smcontrolpanel.SMMasterEditSelect;
import SMDataDefinition.SMTablelaborbackcharges;
import ServletUtilities.clsMasterEntry;
import ServletUtilities.clsServletUtilities;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsDateAndTimeConversions;
import ServletUtilities.clsManageBigDecimals;
import ServletUtilities.clsManageRequestParameters;

public class SMLaborBackCharge extends clsMasterEntry{
	
	public static final String ParamObjectName = "Labor Back Charge";
	
	public static final String Paramlid = "lid";
	public static final String Paramdatinitiated = "datinitiated";
	public static final String Paramlinitiatedbyid = "linitiatedbyid";
	public static final String Paramsinitiatedbyfullname = "sinitiatedbyfullname";
	public static final String Parambdcreditreceived = "bdcreditreceived";
	public static final String Parambdoutstandingcredits = "bdoutstandingcredits";
	public static final String Paramstrimmedordernumber = "strimmedordernumber";
	public static final String Paramscustomername = "scustomername";
	public static final String Paramdatdatesent = "datdatesent";
	public static final String Paramsvendoracct = "svendoracct";
	public static final String Paramsdescription = "sdescription";
	public static final String Paramscomments = "scomments";
	public static final String Parambdhours = "bdHours";
	public static final String Parambdlaborrate = "bdLaborRate";
	public static final String Parambdmisccost = "bdMiscCost";
	public static final String Parambdcreditrequested = "bdcreditrequested";
	public static final String Paramdatcreditnotedate = "datcreditnotedate";
	public static final String Parambdcreditdenied = "bdcreditdenied";
	public static final String Paramsvendoritemnumber = "svendoritemnumber";
	public static final String Paramsgdoclink = "sgdoclink";
	public static final String Paramlcostcenterid = "lcostcenterid";
	

	private String m_lid;
	private String m_datinitiated;
	private String m_linitiatedbyid;
	private String m_sinitiatedbyfullname;
	private String m_sdescription;
	private String m_scomments;
	private String m_strimmedordernumber;
	private String m_scustomername;
	private String m_datdatesent;
	private String m_svendoracct;
	private String m_bdcreditreceived;
	private String m_bdoutstandingcredits;
	private String m_bdhours;
	private String m_bdlaborrate;
	private String m_bdmisccost;
	private String m_bdcreditrequested;
	private String m_datcreditnotedate;
	private String m_bdcreditdenied;
	private String m_svendoritemnumber;
	private String m_sgdoclink;
	private String m_lcostcenterid;
	
	private String m_sNewRecord;
	
	private boolean bDebugMode = false;
	
    public SMLaborBackCharge() {
		super();
		initEntryVariables();
        }
    
    public SMLaborBackCharge(HttpServletRequest req){
		super(req);
		initEntryVariables();
		m_lid = clsManageRequestParameters.get_Request_Parameter(
				SMTablelaborbackcharges.lid, req).trim();
		m_datinitiated = clsManageRequestParameters.get_Request_Parameter(
				SMTablelaborbackcharges.datinitiated, req).trim().replace("&quot;", "\"");
		if(m_datinitiated.compareToIgnoreCase("") == 0){
			m_datinitiated = EMPTY_DATETIME_STRING;
		}
		m_linitiatedbyid = clsManageRequestParameters.get_Request_Parameter(
				SMTablelaborbackcharges.linitiatedbyid, req).trim().replace("&quot;", "\"");
		m_sinitiatedbyfullname = clsManageRequestParameters.get_Request_Parameter(
				SMTablelaborbackcharges.sinitiatedbyfullname, req).trim().replace("&quot;", "\"");


		m_datdatesent = clsManageRequestParameters.get_Request_Parameter(
				SMTablelaborbackcharges.datdatesent, req).trim().replace("&quot;", "\"");
		
		if(m_datdatesent.compareToIgnoreCase("") == 0){
			m_datdatesent = EMPTY_DATE_STRING;
			}	
		
		m_datcreditnotedate = clsManageRequestParameters.get_Request_Parameter(
				SMTablelaborbackcharges.datcreditnotedate, req).trim().replace("&quot;", "\"");
		
		if(m_datcreditnotedate.compareToIgnoreCase("") == 0){
			m_datcreditnotedate = EMPTY_DATE_STRING;
			}
		m_sdescription = clsManageRequestParameters.get_Request_Parameter(
				SMTablelaborbackcharges.sdescription, req).trim().replace("&quot;", "\"");
		
		m_scomments = clsManageRequestParameters.get_Request_Parameter(
				SMTablelaborbackcharges.scomments, req).trim().replace("&quot;", "\"");
		m_scustomername = clsManageRequestParameters.get_Request_Parameter(
				SMTablelaborbackcharges.scustomername, req).trim().replace("&quot;", "\"");
		m_svendoracct = clsManageRequestParameters.get_Request_Parameter(
				SMTablelaborbackcharges.svendoracct, req).trim();
		m_strimmedordernumber = clsManageRequestParameters.get_Request_Parameter(
				SMTablelaborbackcharges.strimmedordernumber, req).trim().replace("&quot;", "\"");
		m_bdhours = clsManageRequestParameters.get_Request_Parameter(
				SMTablelaborbackcharges.bdhours, req).trim().replace("&quot;", "\"");
		m_bdlaborrate = clsManageRequestParameters.get_Request_Parameter(
				SMTablelaborbackcharges.bdlaborrate, req).trim().replace("&quot;", "\"");
		m_bdmisccost = clsManageRequestParameters.get_Request_Parameter(
				SMTablelaborbackcharges.bdmisccost, req).trim().replace("&quot;", "\"");
		m_bdcreditrequested = clsManageRequestParameters.get_Request_Parameter(
				SMTablelaborbackcharges.bdcreditrequested, req).trim().replace("&quot;", "\"");
		m_bdcreditreceived = clsManageRequestParameters.get_Request_Parameter(
				SMTablelaborbackcharges.bdcreditreceived, req).trim().replace("&quot;", "\"");
		m_bdoutstandingcredits = clsManageRequestParameters.get_Request_Parameter(
				SMTablelaborbackcharges.bdoutstandingcredits, req).trim().replace("&quot;", "\"");
		m_bdcreditdenied = clsManageRequestParameters.get_Request_Parameter(
				SMTablelaborbackcharges.bdcreditdenied, req).trim().replace("&quot;", "\"");
		m_svendoritemnumber = clsManageRequestParameters.get_Request_Parameter(
				SMTablelaborbackcharges.svendoritemnumber, req).trim().replace("&quot;", "\"");
		m_sgdoclink = clsManageRequestParameters.get_Request_Parameter(
				SMTablelaborbackcharges.sgdoclink, req).trim().replace("&quot;", "\"");
		m_lcostcenterid = clsManageRequestParameters.get_Request_Parameter(
				SMTablelaborbackcharges.lcostcenterid, req).trim();
		/*if(clsManageRequestParameters.get_Request_Parameter(SMMaterialReturn.Paramladjustedbatchnumber, req).compareToIgnoreCase("") == 0){
			m_lcostcenterid = "0";
		}*/

		
		m_sNewRecord = clsManageRequestParameters.get_Request_Parameter(SMMasterEditSelect.SUBMIT_ADD_BUTTON_NAME, req).trim().replace("&quot;", "\"");
    }
    public void load (ServletContext context, String sDBIB, String sUserID, String sUserFullName) throws Exception{
    	Connection conn = clsDatabaseFunctions.getConnection(
    			context, 
    			sDBIB, 
    			"MySQL", 
    			this.toString() + " - user: " + sUserID + " - " + sUserFullName
    			);
    	
    	if (conn == null){
    		throw new Exception("Error opening data connection to load " + ParamObjectName + ".");
    	}
    	
    	try {
			load (conn);
		} catch (Exception e) {
			clsDatabaseFunctions.freeConnection(context, conn, "[1547067702]");
			throw new Exception(e.getMessage());
		}
    	clsDatabaseFunctions.freeConnection(context, conn, "[1547067703]");
    }
    public boolean load (Connection conn) throws Exception{
    	return load (m_lid, conn);
    }
    private boolean load (String sID, Connection conn) throws Exception{

    	sID = sID.trim();
    	if (sID.compareToIgnoreCase("") == 0){
    		throw new Exception("ID code cannot be blank when loading " + ParamObjectName + ".");
    	}
		
		String SQL = "SELECT * FROM " + SMTablelaborbackcharges.TableName
			+ " WHERE ("
				+ SMTablelaborbackcharges.lid + " = " + sID
			+ ")";
		if (bDebugMode){
			System.out.println("In " + this.toString() + " - load SQL = " + SQL);
		}
		try {
			ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
			if (rs.next()) {
				//Load the variables here:
				m_lid = Long.toString(rs.getLong(SMTablelaborbackcharges.lid));
				
				m_datinitiated = clsDateAndTimeConversions.resultsetDateTimeStringToString(
					rs.getString(SMTablelaborbackcharges.datinitiated));
				m_linitiatedbyid = Long.toString(rs.getLong(SMTablelaborbackcharges.linitiatedbyid));
				m_sinitiatedbyfullname = rs.getString(SMTablelaborbackcharges.sinitiatedbyfullname).trim();
				m_sdescription = rs.getString(SMTablelaborbackcharges.sdescription).trim();
				m_scomments = rs.getString(SMTablelaborbackcharges.scomments).trim();
				m_scustomername = rs.getString(SMTablelaborbackcharges.scustomername).trim();
				m_svendoracct = rs.getString(SMTablelaborbackcharges.svendoracct);
				m_datdatesent = clsDateAndTimeConversions.resultsetDateStringToString(rs.getString(SMTablelaborbackcharges.datdatesent));
				m_bdcreditreceived = clsManageBigDecimals.BigDecimalToScaledFormattedString(SMTablelaborbackcharges.bdcreditreceivedscale, rs.getBigDecimal(SMTablelaborbackcharges.bdcreditreceived));
				m_bdlaborrate = clsManageBigDecimals.BigDecimalToScaledFormattedString(SMTablelaborbackcharges.bdlaborratescale, rs.getBigDecimal(SMTablelaborbackcharges.bdlaborrate));
				m_bdmisccost = clsManageBigDecimals.BigDecimalToScaledFormattedString(SMTablelaborbackcharges.bdmisccostscale, rs.getBigDecimal(SMTablelaborbackcharges.bdmisccost));
				m_bdcreditrequested = clsManageBigDecimals.BigDecimalToScaledFormattedString(SMTablelaborbackcharges.bdcreditrequestedscale, rs.getBigDecimal(SMTablelaborbackcharges.bdcreditrequested));
				m_bdcreditdenied  = clsManageBigDecimals.BigDecimalToScaledFormattedString(SMTablelaborbackcharges.bdcreditdeniedscale, rs.getBigDecimal(SMTablelaborbackcharges.bdcreditdenied));
				m_bdhours = clsManageBigDecimals.BigDecimalToScaledFormattedString(SMTablelaborbackcharges.bdhoursscale, rs.getBigDecimal(SMTablelaborbackcharges.bdhours));
				m_datcreditnotedate = clsDateAndTimeConversions.resultsetDateStringToString(rs.getString(SMTablelaborbackcharges.datcreditnotedate));
				m_strimmedordernumber = rs.getString(SMTablelaborbackcharges.strimmedordernumber).trim();
				m_svendoritemnumber = rs.getString(SMTablelaborbackcharges.svendoritemnumber).trim();
				m_sgdoclink = rs.getString(SMTablelaborbackcharges.sgdoclink).trim();
				m_lcostcenterid = Long.toString(rs.getLong(SMTablelaborbackcharges.lcostcenterid));
				/*if(m_lcostcenterid.compareToIgnoreCase("")== 0) {
					m_lcostcenterid = "0";
				}*/
				rs.close();
			} else {
				rs.close();
				throw new Exception("Could not load labor back charge with ID '" + sID + "'.");
			}
		} catch (Exception e) {
			throw new Exception("Error reading " + ParamObjectName + " for lid : '" + sID
				+ "' - " + e.getMessage());
		}
		return true;
    }
    
    public void save_without_data_transaction (ServletContext context, String sDBIB, String sUserID, String sUserFullName) throws Exception{
    	
       	Connection conn = clsDatabaseFunctions.getConnection(
    			context, 
    			sDBIB, 
    			"MySQL", 
    			this.toString() + " - user: " + sUserID + " - " + sUserFullName
    			);
    	
    	if (conn == null){
    		throw new Exception("Error [1447079869] opening data connection.");
    	}
    	
    	try {
			save_without_data_transaction (conn, sUserID, sUserFullName);
		} catch (Exception e) {
			clsDatabaseFunctions.freeConnection(context, conn, "[1547067704]");
			throw new Exception(e.getMessage());
		}
    	clsDatabaseFunctions.freeConnection(context, conn, "[1547067705]");
    	
    }
    public void save_without_data_transaction (Connection conn, String sUserID, String sUserFullName) throws Exception{

    	try {
			validate_entry_fields(conn);
		} catch (Exception e1) {
			throw new Exception (e1.getMessage());
		}
    	//If it's a new record OR if it's being 'resolved', then we need to get the user's info:
    	String SQL = "";

    	long lid;
		try {
			lid = Long.parseLong(getlid());
		} catch (Exception e1) {
			throw new Exception("Error [1447079869] parsing " + ParamObjectName + " lid '" + this.getlid() + "' - " + e1.getMessage());
		}
    	if (lid < 1){
    		//It's a new record
    		setsNewRecord("1");
    	}

    	if (getsNewRecord().compareToIgnoreCase("1") == 0){
    		setlinitiatedbyid(sUserID);
    		setsinitiatedbyfullname(sUserFullName);
    		
    	}

		//If it's a new record, do an insert:
    	if (getsNewRecord().compareToIgnoreCase("1") == 0){
			SQL = "INSERT INTO " + SMTablelaborbackcharges.TableName + " ("
				+ SMTablelaborbackcharges.datinitiated
				+ ", " + SMTablelaborbackcharges.linitiatedbyid
				+ ", " + SMTablelaborbackcharges.sinitiatedbyfullname
				+ ", " + SMTablelaborbackcharges.scustomername
				+ ", " + SMTablelaborbackcharges.scomments
				+ ", " + SMTablelaborbackcharges.sdescription  		   //10
				+ ", " + SMTablelaborbackcharges.datdatesent
				+ ", " + SMTablelaborbackcharges.strimmedordernumber
				+ ", " + SMTablelaborbackcharges.svendoracct
				+ ", " + SMTablelaborbackcharges.bdcreditreceived
				+ ", " + SMTablelaborbackcharges.bdhours
				+ ", " + SMTablelaborbackcharges.bdlaborrate
				+ ", " + SMTablelaborbackcharges.bdmisccost
				+ ", " + SMTablelaborbackcharges.bdcreditrequested	
				+ ", " + SMTablelaborbackcharges.datcreditnotedate
				+ ", " + SMTablelaborbackcharges.bdcreditdenied
				+ ", " + SMTablelaborbackcharges.svendoritemnumber
				+ ", " + SMTablelaborbackcharges.sgdoclink
				+ ", " + SMTablelaborbackcharges.lcostcenterid
				+ ") VALUES ("
				+ "NOW()"
				+ ", " + clsDatabaseFunctions.FormatSQLStatement(getlinitiatedbyid().trim()) + ""
				+ ", '" + clsDatabaseFunctions.FormatSQLStatement(getsinitiatedbyfullname().trim()) + "'"
				+ ", '" + clsDatabaseFunctions.FormatSQLStatement(getscustomername().trim()) + "'"
				+ ", '" + clsDatabaseFunctions.FormatSQLStatement(getscomments().trim()) + "'"
				+ ", '" + clsDatabaseFunctions.FormatSQLStatement(getsdescription().trim()) + "'"//10
				+ ", '" + clsDateAndTimeConversions.stdDateStringToSQLDateString(getdatdatesent()) + "'"
				+ ", '" + clsDatabaseFunctions.FormatSQLStatement(getstrimmedordernumber().trim()) + "'"
				+ ", '" + clsDatabaseFunctions.FormatSQLStatement(getsvendor().trim()) + "'"
				+ ", " + clsDatabaseFunctions.FormatSQLStatement(getbdcreditreceived().trim())
				+ ", " + clsDatabaseFunctions.FormatSQLStatement(getbdhours().trim())
				+ ", " + clsDatabaseFunctions.FormatSQLStatement(getbdlaborrate().trim())
				+ ", " + clsDatabaseFunctions.FormatSQLStatement(getbdmisccost().trim())
				+ ", " + clsDatabaseFunctions.FormatSQLStatement(getbdcreditrequested().trim())
				+ ", '" + clsDateAndTimeConversions.stdDateStringToSQLDateString(getdatcreditnotedate()) + "'"
				+ ", " + clsDatabaseFunctions.FormatSQLStatement(getbdcreditdenied().trim())
				+ ", '" + clsDatabaseFunctions.FormatSQLStatement(getsvendoritemnumber().trim()) + "'"
				+ ", '" + clsDatabaseFunctions.FormatSQLStatement(getsgdoclink().trim()) + "'"
				+ ", " + clsDatabaseFunctions.FormatSQLStatement(getlcostcenterid().trim()) + ""
				+ ")"
			;

    	}else{
			SQL = " UPDATE " + SMTablelaborbackcharges.TableName + " SET "
			;

			SQL += " " + SMTablelaborbackcharges.scomments  + " = '" + clsDatabaseFunctions.FormatSQLStatement(getscomments().trim()) + "'"
				+ ", " + SMTablelaborbackcharges.sdescription  + " = '" + clsDatabaseFunctions.FormatSQLStatement(getsdescription().trim()) + "'"
				+ ", " + SMTablelaborbackcharges.scustomername  + " = '" + clsDatabaseFunctions.FormatSQLStatement(getscustomername().trim()) + "'"
				+ ", " + SMTablelaborbackcharges.datdatesent + " = '" + clsDateAndTimeConversions.stdDateStringToSQLDateString(getdatdatesent().trim()) + "'"
				+ ", " + SMTablelaborbackcharges.strimmedordernumber  + " = '" + clsDatabaseFunctions.FormatSQLStatement(getstrimmedordernumber().trim()) + "'"
				+ ", " + SMTablelaborbackcharges.svendoracct + " = '" + clsDatabaseFunctions.FormatSQLStatement(getsvendor().trim()) + "'"
				+ ", " + SMTablelaborbackcharges.bdcreditreceived + " = " + clsDatabaseFunctions.FormatSQLStatement(getbdcreditreceived().trim()) 
				+ ", " + SMTablelaborbackcharges.bdhours + " = " + clsDatabaseFunctions.FormatSQLStatement(getbdhours().trim())
				+ ", " + SMTablelaborbackcharges.bdlaborrate + " = " + clsDatabaseFunctions.FormatSQLStatement(getbdlaborrate().trim())
				+ ", " + SMTablelaborbackcharges.bdmisccost + " = " + clsDatabaseFunctions.FormatSQLStatement(getbdmisccost().trim()) 
				+ ", " + SMTablelaborbackcharges.bdcreditrequested + " = " + clsDatabaseFunctions.FormatSQLStatement(getbdcreditrequested().trim())
				+ ", " + SMTablelaborbackcharges.datcreditnotedate + " = '" + clsDateAndTimeConversions.stdDateStringToSQLDateString(getdatcreditnotedate().trim()) + "'"
				+ ", " + SMTablelaborbackcharges.bdcreditdenied + " = " + clsDatabaseFunctions.FormatSQLStatement(getbdcreditdenied().trim())
				+ ", " + SMTablelaborbackcharges.svendoritemnumber + " = '" + clsDatabaseFunctions.FormatSQLStatement(getsvendoritemnumber().trim()) + "'"
				+ ", " + SMTablelaborbackcharges.sgdoclink + " = '" + clsDatabaseFunctions.FormatSQLStatement(getsgdoclink().trim()) + "'"
				+ ", " + SMTablelaborbackcharges.lcostcenterid + " = " + clsDatabaseFunctions.FormatSQLStatement(getlcostcenterid().trim()) + ""
				+ " WHERE ("
					+ "(" + SMTablelaborbackcharges.lid + " = " + getlid() + ")"
				+ ")"
			;
    	}
		if (bDebugMode){
			System.out.println("In " + this.toString() + " - save SQL = " + SQL);
		}
		try{
		    Statement stmt = conn.createStatement();
		    stmt.executeUpdate(SQL);
		}catch (Exception ex) {
			throw new Exception ("Error [1408649180] in insert/update with SQL: " + SQL + " - " + ex.getMessage());
		}
		//Update the ID if it's an insert:
		if (getsNewRecord().compareToIgnoreCase("1") == 0){
			SQL = "SELECT last_insert_id()";
			try {
				ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
				if (rs.next()) {
					m_lid = Long.toString(rs.getLong(1));
				}else {
					m_lid = "0";
				}
				rs.close();
			} catch (SQLException e) {
				throw new Exception("Could not get last ID number - " + e.getMessage());
			}
			//If something went wrong, we can't get the last ID:
			if (m_lid.compareToIgnoreCase("0") == 0){
				throw new Exception("Could not get last ID number.");
			}
		}
    }



	public void delete (ServletContext context, String sDBIB, String sUserID, String sUserFullName) throws Exception{
    	
    	Connection conn = clsDatabaseFunctions.getConnection(
    			context, 
    			sDBIB, 
    			"MySQL", 
    			this.toString() + " - user: " + sUserID +" - " + sUserFullName
    			);
    	
    	if (conn == null){
    		throw new Exception ("Error [1447079871] opening data connection.");
    	}
    	
    	try {
			delete (conn);
		} catch (Exception e) {
			clsDatabaseFunctions.freeConnection(context, conn, "[1547067700]");
			throw new Exception(e.getMessage());
		}
    	clsDatabaseFunctions.freeConnection(context, conn, "[1547067701]");
    }
    public void delete (Connection conn) throws Exception{
    	
    	//Validate deletions
    	String SQL = "";

    	//Delete record:
    	if (!clsDatabaseFunctions.start_data_transaction(conn)){
    		throw new Exception("Error [1447079873] - Could not start transaction when deleting " + ParamObjectName + ".");
    	}
    	SQL = "DELETE FROM " + SMTablelaborbackcharges.TableName
    		+ " WHERE ("
    			+ SMTablelaborbackcharges.lid + " = " + this.getlid()
    		+ ")"
    		;
    	
		try{
		    Statement stmt = conn.createStatement();
		    stmt.executeUpdate(SQL);
		}catch (Exception ex) {
			clsDatabaseFunctions.rollback_data_transaction(conn);
			throw new Exception("Error [1447079874] - Could not delete " + ParamObjectName + " with ID " + getlid() + " with SQL: " + SQL + " - " + ex.getMessage());
		}
		
		if (!clsDatabaseFunctions.commit_data_transaction(conn)){
			clsDatabaseFunctions.rollback_data_transaction(conn);
			throw new Exception("Error [1447079875] - Could not commit data transaction while deleting " + ParamObjectName + ".");
		}
		//Empty the values:
		initEntryVariables();
    }

    public void validate_entry_fields (Connection conn) throws Exception{
        //Validate the entries here:
    	String sErrors = "";
    	m_lid = m_lid.trim();
    	if (m_lid.compareToIgnoreCase("") == 0){
    		m_lid = "-1";
    	}
    	try {
			@SuppressWarnings("unused")
			long lID = Long.parseLong(m_lid);
		} catch (Exception e) {
			throw new Exception("Invalid ID: '" + m_lid + "'.");
		}
    	
    	m_datinitiated = m_datinitiated.trim();
        if (m_datinitiated.compareToIgnoreCase("") == 0){
        	m_datinitiated = EMPTY_DATETIME_STRING;
        }
        if (m_datinitiated.compareToIgnoreCase(EMPTY_DATETIME_STRING) != 0){
        	if (!clsDateAndTimeConversions.IsValidDateString("M/d/yyyy hh:ss a", m_datinitiated)){
        		sErrors += "Date initiated is invalid: '" + m_datinitiated + "'.";
        	}
        }
        m_linitiatedbyid = m_linitiatedbyid.trim();
        if (m_linitiatedbyid.length() > SMTablelaborbackcharges.linitiatedbyidlength){
        	sErrors += "Initiated by user ID cannot be more than " + Integer.toString(SMTablelaborbackcharges.linitiatedbyidlength) + " characters.  ";
        }
        m_sinitiatedbyfullname = m_sinitiatedbyfullname.trim();
        if (m_sinitiatedbyfullname.length() > SMTablelaborbackcharges.sinitiatedbyfullnamelength){
        	sErrors += "Initiated by full name cannot be more than " + Integer.toString(SMTablelaborbackcharges.sinitiatedbyfullnamelength) + " characters.  ";
        }
        
        m_sdescription = m_sdescription.trim();
        if (m_sdescription.length() > SMTablelaborbackcharges.sdescriptionlength){
        	sErrors += "Description cannot be more than " + Integer.toString(SMTablelaborbackcharges.sdescriptionlength) + " characters.  ";
        }
       
        m_strimmedordernumber = m_strimmedordernumber.trim();
        if (m_strimmedordernumber.length() > SMTablelaborbackcharges.strimmedordernumberlength){
        	sErrors += "Order number cannot be more than " + Integer.toString(SMTablelaborbackcharges.strimmedordernumberlength) + " characters.  ";
        }
    	//Make sure it's a real order:
        if (m_strimmedordernumber.compareToIgnoreCase("") != 0){
	        SMOrderHeader ord = new SMOrderHeader();
	        ord.setM_strimmedordernumber(m_strimmedordernumber);
	        if (!ord.load(conn)){
	        	sErrors += "Could not load order number '" + m_strimmedordernumber + "' - " + ord.getErrorMessages() + ".  ";
	        }
        }else {
        	sErrors += "Order Number is required";
        }	
        
        m_datdatesent = m_datdatesent.trim();
        if (m_datdatesent.compareToIgnoreCase("") == 0){
        	m_datdatesent = EMPTY_DATE_STRING;
        }

        if (m_datdatesent.compareToIgnoreCase(EMPTY_DATE_STRING) != 0){
        	if (!clsDateAndTimeConversions.IsValidDateString("M/d/yyyy", m_datdatesent)){
        		sErrors += "Date sent is invalid: '" + m_datdatesent + "'.  ";
        	}
        }
        
        m_datcreditnotedate = m_datcreditnotedate.trim();
        if (m_datcreditnotedate.compareToIgnoreCase("") == 0){
        	m_datcreditnotedate = EMPTY_DATE_STRING;
        }
        
      m_svendoracct = m_svendoracct.trim();
      if (m_svendoracct.length() > SMTablelaborbackcharges.svendoracctlength){
        sErrors += "Vendor cannot be more than " + Integer.toString(SMTablelaborbackcharges.svendoracctlength) + " characters.  ";
       }
      
      //Make sure it's a real vendor
      APVendor ven = new APVendor();
	  ven.setsvendoracct(getsvendor());
	  if (!ven.load(conn)){
		  sErrors += " Invalid vendor '" + getsvendor() + "'.  " + ven.getErrorMessages() + "  ";
	  }
	  
	  
	  m_lcostcenterid = m_lcostcenterid.trim();
	  if (m_lcostcenterid.length() > SMTablelaborbackcharges.lcostcenteridlength){
	      sErrors += "Cost Center cannot be more than " + Integer.toString(SMTablelaborbackcharges.lcostcenteridlength) + " characters.  ";
	    }
	  if (m_lcostcenterid.compareToIgnoreCase("") == 0 || m_lcostcenterid.compareToIgnoreCase("0") == 0){
		  sErrors += "Cost Center is required.  ";
	  }
	  
	  m_svendoritemnumber = m_svendoritemnumber.trim();
	  if (m_svendoritemnumber.length() > SMTablelaborbackcharges.svendoritemnumberlength){
	      sErrors += "Vendor item number cannot be more than " + Integer.toString(SMTablelaborbackcharges.svendoritemnumberlength) + " characters.  ";
	    }
	  if (m_svendoritemnumber.compareToIgnoreCase("") == 0){
		  sErrors += "Vendor item number cannot be blank.   ";
      }
	  
	  
      if(m_bdcreditreceived.compareToIgnoreCase("") == 0){
    	  m_bdcreditreceived = "0.00";
      }
      //Test convert to number to make sure it's not zero - otherwise someone could enter '0.0' or '0' and the program
      //might think it's NOT zero....
      BigDecimal bdCreditReceived = new BigDecimal(m_bdcreditreceived.replace(",", ""));
      if((bdCreditReceived.compareTo(BigDecimal.ZERO) != 0) &&
    		  m_datcreditnotedate.compareToIgnoreCase(EMPTY_DATE_STRING) == 0 ){
    	  sErrors += "A Credit Memo date is required if any credits have been received. ";
      }
      if(m_bdhours.compareToIgnoreCase("") == 0){
    	  m_bdhours = "0.00";
      }
      if(m_bdoutstandingcredits.compareToIgnoreCase("") == 0){
    	  m_bdoutstandingcredits = "0.00";
      }
      if(m_bdlaborrate.compareToIgnoreCase("") == 0){
    	  m_bdlaborrate = "0.00";
      }
      if(m_bdmisccost.compareToIgnoreCase("") == 0){
    	  m_bdmisccost = "0.00";
      }
      if(m_bdcreditrequested.compareToIgnoreCase("") == 0){
    	  m_bdcreditrequested = "0.00";
      }
      if(m_bdcreditdenied.compareToIgnoreCase("") == 0){
    	  m_bdcreditdenied = "0.00";
      }

       try{
    	   validate_number_entries();
       }catch (Exception e){
    	   sErrors = e.getMessage();
       }
    	if (sErrors.compareToIgnoreCase("") != 0){
    		throw new Exception(sErrors);
    	}
    }

    public void validate_number_entries() throws Exception{
    	String sErrors = "";
  
    	m_bdcreditreceived = m_bdcreditreceived.trim().replace(",", "");
        m_bdhours = m_bdhours.trim().replace(",", "");
        m_bdoutstandingcredits = m_bdoutstandingcredits.trim().replace(",", "");
        m_bdlaborrate = m_bdlaborrate.trim().replace(",", "");
        m_bdmisccost = m_bdmisccost.trim().replace(",", "");
        m_bdcreditrequested = m_bdcreditrequested.trim().replace(",", "");
        
        
        try{
            BigDecimal bdcreditreceived = new BigDecimal(m_bdcreditreceived);
         if (bdcreditreceived.compareTo(BigDecimal.ZERO) < 0){
        	 sErrors += "Credit Amount must be a positive number: " + m_bdcreditreceived + ".  ";
         }
           }catch (NumberFormatException e){
        	   sErrors += "Invalid credit amount: '" + m_bdcreditreceived + "'.  ";
           }
        
        try{
            BigDecimal bdoutstandingcredits = new BigDecimal(m_bdoutstandingcredits);
         if (bdoutstandingcredits.compareTo(BigDecimal.ZERO) < 0){
        	 sErrors += "Outstanding credits must be a positive number: " + m_bdoutstandingcredits + ".  ";
         }
           }catch (NumberFormatException e){
        	   sErrors += "Invalid credit amount: '" + m_bdoutstandingcredits + "'.  ";
           }
        
        try{
            BigDecimal bdhours = new BigDecimal(m_bdhours);
         if (bdhours.compareTo(BigDecimal.ZERO) < 0){
        	 sErrors += "Hours must be a positive number: " + m_bdhours + ".  ";
         }
           }catch (NumberFormatException e){
        	   sErrors += "Invalid hours amount: '" + m_bdhours + "'.  ";
           }
        
        try{
            BigDecimal bdlaborrate = new BigDecimal(m_bdlaborrate);
         if (bdlaborrate.compareTo(BigDecimal.ZERO) < 0){
        	 sErrors += "Labor rate must be a positive number: " + m_bdlaborrate + ".  ";
         }
           }catch (NumberFormatException e){
        	   sErrors += "Invalid labor rate: '" + m_bdlaborrate + "'.  ";
           }
        
        try{
            BigDecimal bdmisccost = new BigDecimal(m_bdmisccost);
         if (bdmisccost.compareTo(BigDecimal.ZERO) < 0){
        	 sErrors += "Misc cost must be a positive number: " + m_bdmisccost + ".  ";
         }
           }catch (NumberFormatException e){
        	   sErrors += "Invalid misc cost amount: '" + m_bdmisccost + "'.  ";
           }
        
        try{
            BigDecimal bdtotal = new BigDecimal(m_bdcreditrequested);
         if (bdtotal.compareTo(BigDecimal.ZERO) < 0){
        	 sErrors += "Total amount must be a positive number: " + m_bdcreditrequested + ".  ";
         }
           }catch (NumberFormatException e){
        	   sErrors += "Invalid Total amount: '" + m_bdcreditrequested + "'.  ";
           }
        
        try{
            BigDecimal bdtotal = new BigDecimal(m_bdcreditdenied);
         if (bdtotal.compareTo(BigDecimal.ZERO) < 0){
        	 sErrors += "Credit denied amount must be a positive number: " + m_bdcreditdenied + ".  ";
         }
           }catch (NumberFormatException e){
        	   sErrors += "Invalid Credit denied amount: '" + m_bdcreditdenied + "'.  ";
           }
    	 
    	if (sErrors.compareToIgnoreCase("") != 0){
    		throw new Exception(sErrors);
    	}

    }
   
	public String getQueryString(){
		//Particular to the specific class
		String sQueryString = "";
		sQueryString += ParamObjectName + "=" + clsServletUtilities.URLEncode(this.getObjectName());
		sQueryString += "&" + Paramlid + "=" + clsServletUtilities.URLEncode(this.getlid());
		sQueryString += "&" + Paramdatinitiated + "=" + clsServletUtilities.URLEncode(getsdatinitiated());
		sQueryString += "&" + Paramlinitiatedbyid + "=" + clsServletUtilities.URLEncode(getlinitiatedbyid());
		sQueryString += "&" + Paramsinitiatedbyfullname + "=" + clsServletUtilities.URLEncode(getsinitiatedbyfullname());
		sQueryString += "&" + Parambdcreditreceived + "=" + clsServletUtilities.URLEncode(getbdcreditreceived());
		sQueryString += "&" + Paramstrimmedordernumber + "=" + clsServletUtilities.URLEncode(getstrimmedordernumber());
		sQueryString += "&" + Paramscustomername + "=" + clsServletUtilities.URLEncode(getscustomername());
		sQueryString += "&" + Paramdatdatesent + "=" + clsServletUtilities.URLEncode(getdatdatesent());
		sQueryString += "&" + Paramsvendoracct + "=" + clsServletUtilities.URLEncode(getsvendor());
		sQueryString += "&" + Paramsdescription + "=" + clsServletUtilities.URLEncode(getsdescription());
		sQueryString += "&" + Paramscomments + "=" + clsServletUtilities.URLEncode(getscomments());
		sQueryString += "&" + Parambdhours + "=" + clsServletUtilities.URLEncode(getbdhours());
		sQueryString += "&" + Parambdlaborrate + "=" + clsServletUtilities.URLEncode(getbdlaborrate());
		sQueryString += "&" + Parambdmisccost + "=" + clsServletUtilities.URLEncode(getbdmisccost());
		sQueryString += "&" + Parambdcreditrequested + "=" + clsServletUtilities.URLEncode(getbdcreditrequested());
		sQueryString += "&" + Paramdatcreditnotedate + "=" + clsServletUtilities.URLEncode(getdatcreditnotedate());
		sQueryString += "&" + Parambdcreditdenied + "=" + clsServletUtilities.URLEncode(getbdcreditdenied());
		sQueryString += "&" + Paramsvendoritemnumber + "=" + clsServletUtilities.URLEncode(getsvendoritemnumber());
		sQueryString += "&" + Paramsgdoclink + "=" + clsServletUtilities.URLEncode(getsgdoclink());
		sQueryString += "&" + Paramlcostcenterid + "=" + clsServletUtilities.URLEncode(getlcostcenterid());
		return sQueryString;
	}

	public String getlid() {
		return m_lid;
	}
	public void setlid(String slid) {
		m_lid = slid;
	}
	public String getsdatinitiated() {
		return m_datinitiated;
	}
	public void setsdatinitiated(String sdatinitiated) {
		m_datinitiated = sdatinitiated;
	}
	public String getlinitiatedbyid() {
		return m_linitiatedbyid;
	}
	public void setlinitiatedbyid(String linitiatedbyid) {
		m_linitiatedbyid = linitiatedbyid;
	}
	public String getsinitiatedbyfullname() {
		return m_sinitiatedbyfullname;
	}
	public void setsinitiatedbyfullname(String sinitiatedbyfullname) {
		m_sinitiatedbyfullname = sinitiatedbyfullname;
	}
	public String getsdescription() {
		return m_sdescription;
	}
	public void setsdescription(String sdescription) {
		m_sdescription = sdescription;
	}
	public String getscomments() {
		return m_scomments;
	}
	public void setscomments(String scomments) {
		m_scomments = scomments;
	}
	public String getscustomername() {
		return m_scustomername;
	}
	public void setscustomername(String scustomername) {
		m_scustomername = scustomername;
	}
	public String getsNewRecord() {
		return m_sNewRecord;
	}
	public void setsNewRecord(String mSNewRecord) {
		m_sNewRecord = mSNewRecord;
	}
	public String getdatdatesent() {
		return m_datdatesent;
	}
	public void setdatdatesent(String datdatesent) {
		m_datdatesent = datdatesent;
	}
	public String getstrimmedordernumber() {
		return m_strimmedordernumber;
	}
	public void setstrimmedordernumber(String strimmedordernumber) {
		m_strimmedordernumber = strimmedordernumber;
	}
	public String getsvendor() {
		return m_svendoracct;
	}
	public void setsvendor(String svendor) {
		m_svendoracct = svendor;
	}
	public String getbdcreditreceived() {
		return m_bdcreditreceived;
	}
	public void setbdcreditreceived(String bdcreditreceived) {
		m_bdcreditreceived = bdcreditreceived;
	}	
	public String getbdoutstandingcredits() {
		return m_bdoutstandingcredits;
	}
	public void setbdoutstandingcredits(String bdoutstandingcredits) {
		m_bdoutstandingcredits = bdoutstandingcredits;
	}	
	public String getbdhours() {
		return m_bdhours;
	}
	public void setbdhours(String bdhours) {
		m_bdhours = bdhours;
	}	
	public String getbdlaborrate() {
		return m_bdlaborrate;
	}
	public void setbdlaborrate(String bdlaborrate) {
		m_bdlaborrate = bdlaborrate;
	}	
	public String getbdmisccost() {
		return m_bdmisccost;
	}
	public void setbdmisccost(String bdmisccost) {
		m_bdmisccost = bdmisccost;
	}	
	public String getbdcreditrequested() {
		return m_bdcreditrequested;
	}
	public void setbdcreditrequested(String bdcreditrequested) {
		m_bdcreditrequested = bdcreditrequested;
	}
	public String getbdcreditdenied() {
		return m_bdcreditdenied;
	}
	public void setbdcreditdenied(String bdcreditdenied) {
		m_bdcreditdenied = bdcreditdenied;
	}
	public String getdatcreditnotedate() {
		return m_datcreditnotedate;
	}
	public void setdatcreditnotedate(String datcreditnotedate) {
		m_datcreditnotedate = datcreditnotedate;
	}
	public String getsvendoritemnumber() {
		return m_svendoritemnumber;
	}
	public void setsvendoritemnumber(String svendoritemnumber) {
		m_svendoritemnumber = svendoritemnumber;
	}
	public String getsgdoclink() {
		return m_sgdoclink;
	}
	public void setsgdocklink(String sgdoclink) {
		m_sgdoclink = sgdoclink;
	}
	public String getlcostcenterid() {
		return m_lcostcenterid;
	}
	public void setlcostcenterid(String lcostcenterid) {
		m_lcostcenterid = lcostcenterid;
	}
	

	public String getObjectName(){
		return ParamObjectName;
	}

    private void initEntryVariables(){
    	m_lid = "-1";
    	m_datinitiated = EMPTY_DATETIME_STRING;
    	m_linitiatedbyid = "";
    	m_sinitiatedbyfullname = "";
    	m_sdescription = "";
    	m_scomments = "";
    	m_scustomername = "";
    	m_datdatesent = EMPTY_DATE_STRING;
    	m_strimmedordernumber = "";
    	m_sNewRecord = "1";
    	m_svendoracct = "";
    	m_bdcreditreceived = "0.00";
    	m_bdoutstandingcredits = "0.00"; 
    	m_bdhours = "0.00";
    	m_bdlaborrate = "0.00";
    	m_bdmisccost = "0.00";
    	m_bdcreditrequested = "0.00";
    	m_bdcreditdenied = "0.00";
    	m_datcreditnotedate = EMPTY_DATE_STRING;
        m_svendoritemnumber = "";
        m_lcostcenterid = "0";
	}
}
