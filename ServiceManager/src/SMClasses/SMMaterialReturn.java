package SMClasses;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import SMDataDefinition.SMTablematerialreturns;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsDateAndTimeConversions;
import ServletUtilities.clsManageRequestParameters;
import ServletUtilities.clsMasterEntry;
import smcontrolpanel.SMMasterEditSelect;

public class SMMaterialReturn extends clsMasterEntry{

	public static final String ParamObjectName = "Material Return";

	public static final String Paramlid = "lid";
	public static final String Paramdatinitiated = "datinitiated";
	public static final String Paramlinitiatedbyid = "linitiatedbyid";
	public static final String Paramsinitiatedbyfullname = "sinitiatedbyfullname";
	public static final String Paramiresolved = "iresolved";
	public static final String Paramdatresolved = "datresolved";
	public static final String Paramlresolvedbyid = "sresolvedby";
	public static final String Paramsresolvedbyfullname = "sresolvedbyfullname";
	public static final String Paramsdescription = "sdescription";
	public static final String Parammcomments = "mcomments";
	public static final String Parammresolutioncomments = "mresolutioncomments";
	public static final String Paramiworkorderid = "iworkorderid";
	public static final String Paramstrimmedordernumber = "strimmedordernumber";


	private String m_slid;
	private String m_datinitiated;
	private String m_linitiatedbyid;
	private String m_sinitiatedbyfullname;
	private String m_sresolved;
	private String m_datresolved;
	private String m_lresolvedbyid;
	private String m_sresolvedbyfullname;
	private String m_sdescription;
	private String m_scomments;
	private String m_sresolutioncomments;
	private String m_sworkorderid;
	private String m_strimmedordernumber;
	private String m_sNewRecord;

	private boolean bDebugMode = false;

	public SMMaterialReturn() {
		super();
		initEntryVariables();
	}

	public SMMaterialReturn(HttpServletRequest req){
		super(req);
		initEntryVariables();
		m_slid = clsManageRequestParameters.get_Request_Parameter(
				SMMaterialReturn.Paramlid, req).trim();
		m_datinitiated = clsManageRequestParameters.get_Request_Parameter(
				SMMaterialReturn.Paramdatinitiated, req).trim().replace("&quot;", "\"");
		if(m_datinitiated.compareToIgnoreCase("") == 0){
			m_datinitiated = EMPTY_DATETIME_STRING;
		}
		m_linitiatedbyid = clsManageRequestParameters.get_Request_Parameter(
				SMMaterialReturn.Paramlinitiatedbyid, req).trim().replace("&quot;", "\"");
		if(req.getParameter(SMMaterialReturn.Paramiresolved) == null || m_linitiatedbyid.compareToIgnoreCase("") == 0){
			m_linitiatedbyid = "0";
		}
		m_sinitiatedbyfullname = clsManageRequestParameters.get_Request_Parameter(
				SMMaterialReturn.Paramsinitiatedbyfullname, req).trim().replace("&quot;", "\"");
		if(req.getParameter(SMMaterialReturn.Paramiresolved) == null){
			m_sresolved = "0";
		}else{
			m_sresolved = "1";
		}
		m_datresolved = clsManageRequestParameters.get_Request_Parameter(
				SMMaterialReturn.Paramdatresolved, req).trim().replace("&quot;", "\"");

		if(m_datresolved.compareToIgnoreCase("") == 0){
			m_datresolved = EMPTY_DATETIME_STRING;
		}

		m_lresolvedbyid = clsManageRequestParameters.get_Request_Parameter(
				SMMaterialReturn.Paramlresolvedbyid, req).trim().replace("&quot;", "\"");
		if(req.getParameter(SMMaterialReturn.Paramiresolved) == null || m_lresolvedbyid.compareToIgnoreCase("") == 0){
			m_lresolvedbyid = "0";
		}
		m_sresolvedbyfullname = clsManageRequestParameters.get_Request_Parameter(
				SMMaterialReturn.Paramsresolvedbyfullname, req).trim().replace("&quot;", "\"");
		m_sdescription = clsManageRequestParameters.get_Request_Parameter(
				SMMaterialReturn.Paramsdescription, req).trim().replace("&quot;", "\"");
		m_scomments = clsManageRequestParameters.get_Request_Parameter(
				SMMaterialReturn.Parammcomments, req).trim().replace("&quot;", "\"");
		/*m_sresolutioncomments = clsManageRequestParameters.get_Request_Parameter(
				SMMaterialReturn.Parammresolutioncomments, req).trim().replace("&quot;", "\"");*/
		m_sworkorderid = clsManageRequestParameters.get_Request_Parameter(
				SMMaterialReturn.Paramiworkorderid, req).trim();
		m_strimmedordernumber = clsManageRequestParameters.get_Request_Parameter(
				SMMaterialReturn.Paramstrimmedordernumber, req).trim().replace("&quot;", "\"");
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
			clsDatabaseFunctions.freeConnection(context, conn, "[1547067708]");
			throw new Exception(e.getMessage());
		}
		clsDatabaseFunctions.freeConnection(context, conn, "[1547067709]");
	}
	public boolean load (Connection conn) throws Exception{
		return load (m_slid, conn);
	}
	private boolean load (String sID, Connection conn) throws Exception{

		sID = sID.trim();
		if (sID.compareToIgnoreCase("") == 0){
			throw new Exception("ID code cannot be blank when loading " + ParamObjectName + ".");
		}

		String SQL = "SELECT * FROM " + SMTablematerialreturns.TableName
				+ " WHERE ("
				+ SMTablematerialreturns.lid + " = " + sID
				+ ")";
		if (bDebugMode){
			System.out.println("[1579186172] In " + this.toString() + " - load SQL = " + SQL);
		}
		try {
			ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
			if (rs.next()) {
				//Load the variables here:
				m_slid = Long.toString(rs.getLong(SMTablematerialreturns.lid));

				m_datinitiated = clsDateAndTimeConversions.resultsetDateTimeToTheMinuteStringToString(
						rs.getString(SMTablematerialreturns.datinitiated));
				m_linitiatedbyid = Long.toString(rs.getLong(SMTablematerialreturns.linitiatedbyid));
				m_sinitiatedbyfullname = rs.getString(SMTablematerialreturns.sinitiatedbyfullname).trim();
				m_sresolved = Long.toString(rs.getLong(SMTablematerialreturns.iresolved));
				m_datresolved = clsDateAndTimeConversions.resultsetDateTimeToTheMinuteStringToString(rs.getString(SMTablematerialreturns.datresolved));
				m_lresolvedbyid = Long.toString(rs.getLong(SMTablematerialreturns.lresolvedbyid));
				m_sresolvedbyfullname = rs.getString(SMTablematerialreturns.sresolvedbyfullname).trim();
				m_sdescription = rs.getString(SMTablematerialreturns.sdescription).trim();
				m_scomments = rs.getString(SMTablematerialreturns.mcomments).trim();
				//m_sresolutioncomments = rs.getString(SMTablematerialreturns.mresolutioncomments).trim();
				m_sworkorderid = Long.toString(rs.getLong(SMTablematerialreturns.iworkorderid));
				if (Long.parseLong(m_sworkorderid) < 1){
					m_sworkorderid = "";
				}
				m_strimmedordernumber = rs.getString(SMTablematerialreturns.strimmedordernumber).trim();
				rs.close();
			} else {
				rs.close();
				throw new Exception("Could not load material return with ID '" + sID + "'.");
			}
		} catch (Exception e) {
			throw new Exception("Error reading " + ParamObjectName + " for lid : '" + sID
					+ "' - " + e.getMessage());
		}
		return true;
	}

	public void save_without_data_transaction (ServletContext context, String sDBID,  String sUserID, String sUserFullName) throws Exception{

		Connection conn = clsDatabaseFunctions.getConnection(
				context, 
				sDBID, 
				"MySQL", 
				this.toString() + " - user: " + sUserID + " - "+ sUserFullName
				);

		if (conn == null){
			throw new Exception("Error [1408649178] opening data connection.");
		}

		try {
			save_without_data_transaction (conn, sUserID, sUserFullName, sDBID);
		} catch (Exception e) {
			clsDatabaseFunctions.freeConnection(context, conn, "[1547067710]");
			throw new Exception(e.getMessage());
		}
		clsDatabaseFunctions.freeConnection(context, conn, "[1547067711]");

	}
	public void save_without_data_transaction (Connection conn, String sUserID, String sUserFullName, String sDBID) throws Exception{

		try {
			validate_entry_fields(conn, sDBID);
		} catch (Exception e1) {
			throw new Exception (e1.getMessage());
		}

		//If it's a new record OR if it's being 'resolved', then we need to get the user's info:
		String SQL = "";

		boolean bReturnIsBeingResolved = false;
		long lid;
		try {
			lid = Long.parseLong(getslid());
		} catch (Exception e1) {
			throw new Exception("Error [1408653581] parsing " + ParamObjectName + " lid '" + this.getslid() + "' - " + e1.getMessage());
		}
		if (lid < 1){
			//It's a new record
			setsNewRecord("1");
			if (getsresolved().compareToIgnoreCase("1") == 0){
				bReturnIsBeingResolved = true;
			}
		}else{
			//If it's marked as resolved, see if it was not resolved until now:
			if (getsresolved().compareToIgnoreCase("1") == 0){
				SQL = "SELECT"
						+ " " + SMTablematerialreturns.iresolved
						+ " FROM " + SMTablematerialreturns.TableName
						+ " WHERE ("
						+ "(" + SMTablematerialreturns.lid + " = " + getslid() + ")"
						+ ")"
						;
				ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
				if (rs.next()){
					if (rs.getInt(SMTablematerialreturns.iresolved) == 0){
						bReturnIsBeingResolved = true;
					}
				}else{
					rs.close();
					throw new Exception("Error [1413917865] - could not read record for ID '" + this.getslid() + "'.");
				}
				rs.close();
			}
		}

		if (getsNewRecord().compareToIgnoreCase("1") == 0){
			setlinitiatedbyid(sUserID);
			setsinitiatedbyfullname(sUserFullName);

		}
		if (bReturnIsBeingResolved){
			setlresolvedbyid(sUserID);
			setsresolvedbyfullname(sUserFullName);
		}
		String sWorkOrderID = getsworkorderid();
		if (sWorkOrderID.compareToIgnoreCase("") == 0){
			sWorkOrderID = "-1";
		}

		//If it's a new record, do an insert:
		if (getsNewRecord().compareToIgnoreCase("1") == 0){
			SQL = "INSERT INTO " + SMTablematerialreturns.TableName + " ("
					+ SMTablematerialreturns.datinitiated
					+ ", " + SMTablematerialreturns.linitiatedbyid
					+ ", " + SMTablematerialreturns.sinitiatedbyfullname
					+ ", " + SMTablematerialreturns.datresolved
					+ ", " + SMTablematerialreturns.iresolved
					+ ", " + SMTablematerialreturns.lresolvedbyid
					+ ", " + SMTablematerialreturns.sresolvedbyfullname
					+ ", " + SMTablematerialreturns.mresolutioncomments
					+ ", " + SMTablematerialreturns.mcomments
					+ ", " + SMTablematerialreturns.sdescription
					+ ", " + SMTablematerialreturns.iworkorderid
					+ ", " + SMTablematerialreturns.strimmedordernumber
					+ ") VALUES ("
					+ "NOW()"
					+ ", " + clsDatabaseFunctions.FormatSQLStatement(sUserID) + ""
					+ ", '" + clsDatabaseFunctions.FormatSQLStatement(sUserFullName) + "'"
					;
			if (bReturnIsBeingResolved){
				SQL += ", NOW()";
			}else{
				SQL += ", '" + clsDateAndTimeConversions.stdDateTimeToSQLDateTimeString(getsdatresolved()) + "'";
			}
			SQL += ", " + getsresolved()
			+ ", " + clsDatabaseFunctions.FormatSQLStatement(getlresolvedbyid()) + ""
			+ ", '" + clsDatabaseFunctions.FormatSQLStatement(getsresolvedbyfullname().trim()) + "'"
			+ ", '" + clsDatabaseFunctions.FormatSQLStatement(getsresolutioncomments().trim()) + "'"
			+ ", '" + clsDatabaseFunctions.FormatSQLStatement(getscomments().trim()) + "'"
			+ ", '" + clsDatabaseFunctions.FormatSQLStatement(getsdescription().trim()) + "'"
			+ ", " + sWorkOrderID
			+ ", '" + clsDatabaseFunctions.FormatSQLStatement(getstrimmedordernumber().trim()) + "'"
			+ ")"
			;
		}else{
			SQL = " UPDATE " + SMTablematerialreturns.TableName + " SET "
					;
			if (bReturnIsBeingResolved){
				SQL +=  SMTablematerialreturns.datresolved + " = NOW()";
			}else{
				SQL += " " + SMTablematerialreturns.datresolved + " = '" + clsDateAndTimeConversions.stdDateTimeToSQLDateTimeString(getsdatresolved()) + "'";
			}
			SQL += ", " + SMTablematerialreturns.iresolved + " = " + getsresolved()
			+ ", " + SMTablematerialreturns.mcomments  + " = '" + clsDatabaseFunctions.FormatSQLStatement(getscomments().trim()) + "'"
			+ ", " + SMTablematerialreturns.sdescription  + " = '" + clsDatabaseFunctions.FormatSQLStatement(getsdescription().trim()) + "'"
			//+ ", " + SMTablematerialreturns.mresolutioncomments  + " = '" + clsDatabaseFunctions.FormatSQLStatement(getsresolutioncomments().trim()) + "'"
			+ ", " + SMTablematerialreturns.lresolvedbyid  + " = " + clsDatabaseFunctions.FormatSQLStatement(getlresolvedbyid().trim()) + ""
			+ ", " + SMTablematerialreturns.sresolvedbyfullname  + " = '" + clsDatabaseFunctions.FormatSQLStatement(getsresolvedbyfullname().trim()) + "'"
			+ ", " + SMTablematerialreturns.iworkorderid + " = " + sWorkOrderID
			+ ", " + SMTablematerialreturns.strimmedordernumber  + " = '" + clsDatabaseFunctions.FormatSQLStatement(getstrimmedordernumber().trim()) + "'"
			+ " WHERE ("
			+ "(" + SMTablematerialreturns.lid + " = " + getslid() + ")"
			+ ")"
			;
		}
		if (bDebugMode){
			System.out.println("[1579186178] In " + this.toString() + " - save SQL = " + SQL);
		}
		try{
			Statement stmt = conn.createStatement();
			stmt.executeUpdate(SQL);
		}catch (Exception ex) {
			throw new Exception ("Error [1572032662] in insert/update with SQL: " + SQL + " - " + ex.getMessage());
		}
		//Update the ID if it's an insert:
		if (getsNewRecord().compareToIgnoreCase("1") == 0){
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
			throw new Exception ("Error [1408649181] opening data connection.");
		}

		try {
			delete (conn);
		} catch (Exception e) {
			clsDatabaseFunctions.freeConnection(context, conn, "[1547067706]");
			throw new Exception(e.getMessage());
		}
		clsDatabaseFunctions.freeConnection(context, conn, "[1547067707]");
	}
	public void delete (Connection conn) throws Exception{

		//Validate deletions
		String SQL = "";

		//Delete record:
		if (!clsDatabaseFunctions.start_data_transaction(conn)){
			throw new Exception("Error [1408649182] - Could not start transaction when deleting " + ParamObjectName + ".");
		}
		SQL = "DELETE FROM " + SMTablematerialreturns.TableName
				+ " WHERE ("
				+ SMTablematerialreturns.lid + " = " + this.getslid()
				+ ")"
				;

		try{
			Statement stmt = conn.createStatement();
			stmt.executeUpdate(SQL);
		}catch (Exception ex) {
			clsDatabaseFunctions.rollback_data_transaction(conn);
			throw new Exception("Error [1408649183] - Could not delete " + ParamObjectName + " with ID " + getslid() + " with SQL: " + SQL + " - " + ex.getMessage());
		}

		if (!clsDatabaseFunctions.commit_data_transaction(conn)){
			clsDatabaseFunctions.rollback_data_transaction(conn);
			throw new Exception("Error [1408649184] - Could not commit data transaction while deleting " + ParamObjectName + ".");
		}
		//Empty the values:
		initEntryVariables();
	}

	public void validate_entry_fields (Connection conn, String sDBID) throws Exception{
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
			throw new Exception("Invalid ID: '" + m_slid + "'.");
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
		if (m_linitiatedbyid.length() > SMTablematerialreturns.linitiatedbyidlength){
			sErrors += "Initiated by ID cannot be more than " + Integer.toString(SMTablematerialreturns.linitiatedbyidlength) + " characters.  ";
		}
		m_sinitiatedbyfullname = m_sinitiatedbyfullname.trim();
		if (m_sinitiatedbyfullname.length() > SMTablematerialreturns.sinitiatedbyfullnamelength){
			sErrors += "Initiated by full name cannot be more than " + Integer.toString(SMTablematerialreturns.sinitiatedbyfullnamelength) + " characters.  ";
		}
		if (
				(m_sresolved.compareToIgnoreCase("0") != 0)
				&& (m_sresolved.compareToIgnoreCase("1") != 0)
				){
			sErrors += "'Resolved' status (" + m_sresolved + ") is invalid.";
		}

		m_datresolved = m_datresolved.trim();
		if (m_datresolved.compareToIgnoreCase("") == 0){
			m_datresolved = EMPTY_DATETIME_STRING;
		}
		if (m_datresolved.compareToIgnoreCase(EMPTY_DATETIME_STRING) != 0){
			if (!clsDateAndTimeConversions.IsValidDateString("M/d/yyyy hh:ss a", m_datresolved)){
				sErrors += "Date resolved is invalid: '" + m_datresolved + "'.";
			}
		}
		m_lresolvedbyid = m_lresolvedbyid.trim();
		if (m_lresolvedbyid.length() > SMTablematerialreturns.lresolvedbyidlength){
			sErrors += "Resolved by ID cannot be more than " + Integer.toString(SMTablematerialreturns.lresolvedbyidlength) + " characters.  ";
		}
		m_sresolvedbyfullname = m_sresolvedbyfullname.trim();
		if (m_sresolvedbyfullname.length() > SMTablematerialreturns.sresolvedbyfullnamelength){
			sErrors += "Resolved by full name cannot be more than " + Integer.toString(SMTablematerialreturns.sresolvedbyfullnamelength) + " characters.  ";
		}
		m_sdescription = m_sdescription.trim();
		if (m_sdescription.length() > SMTablematerialreturns.sdescriptionlength){
			sErrors += "Description cannot be more than " + Integer.toString(SMTablematerialreturns.sdescriptionlength) + " characters.  ";
		}
		//Also, the description can NOT be blank:
		if (m_sdescription.compareToIgnoreCase("") == 0){
			sErrors += "Description cannot be blank.  ";
		}

		m_scomments = m_scomments.trim();

		//m_sresolutioncomments = m_sresolutioncomments.trim();
		/*if (getsresolved().compareToIgnoreCase("1") == 0){
			if (m_sresolutioncomments.compareToIgnoreCase("") == 0){
				sErrors += ParamObjectName + "s cannot be resolved without adding a resolution comment.  ";
			}
		}else{
			if (m_sresolutioncomments.compareToIgnoreCase("") != 0){
				sErrors += "You cannot enter resolution comments unless the return is resolved.  ";
			}
		}*/
		m_sworkorderid = m_sworkorderid.trim();
		if (m_sworkorderid.compareToIgnoreCase("") == 0){
		}else{
			try {
				@SuppressWarnings("unused")
				long lID = Long.parseLong(m_sworkorderid);
			} catch (Exception e) {
				sErrors += "Invalid work order ID: '" + m_sworkorderid + "'.  ";
			}
		}
		m_strimmedordernumber = m_strimmedordernumber.trim();
		if (m_strimmedordernumber.length() > SMTablematerialreturns.strimmedordernumberlength){
			sErrors += "Order number cannot be more than " + Integer.toString(SMTablematerialreturns.strimmedordernumberlength) + " characters.  ";
		}
		//Make sure it's a real order:
		if (m_strimmedordernumber.compareToIgnoreCase("") != 0){
			SMOrderHeader ord = new SMOrderHeader();
			ord.setM_strimmedordernumber(m_strimmedordernumber);
			if (!ord.load(conn)){
				sErrors += "Could not load order number '" + m_strimmedordernumber + "' - " + ord.getErrorMessages() + ".  ";
			}
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
	public String getsresolved() {
		return m_sresolved;
	}
	public void setsresolved(String sresolved) {
		m_sresolved = sresolved;
	}
	public String getsdatresolved() {
		return m_datresolved;
	}
	public void setsdatresolved(String sdatresolved) {
		m_datresolved = sdatresolved;
	}
	public String getlresolvedbyid() {
		return m_lresolvedbyid;
	}
	public void setlresolvedbyid(String lresolvedbyid) {
		m_lresolvedbyid = lresolvedbyid;
	}
	public String getsresolvedbyfullname() {
		return m_sresolvedbyfullname;
	}
	public void setsresolvedbyfullname(String sresolvedbyfullname) {
		m_sresolvedbyfullname = sresolvedbyfullname;
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
	public String getsresolutioncomments() {
		return m_sresolutioncomments;
	}
	public void setsresolutioncomments(String sresolutioncomments) {
		m_sresolutioncomments = sresolutioncomments;
	}
	public String getsNewRecord() {
		return m_sNewRecord;
	}
	public void setsNewRecord(String mSNewRecord) {
		m_sNewRecord = mSNewRecord;
	}
	public String getsworkorderid() {
		return m_sworkorderid;
	}
	public void setsworkorderid(String sworkorderid) {
		m_sworkorderid = sworkorderid;
	}
	public String getstrimmedordernumber() {
		return m_strimmedordernumber;
	}
	public void setstrimmedordernumber(String strimmedordernumber) {
		m_strimmedordernumber = strimmedordernumber;
	}
	


	public String getObjectName(){
		return ParamObjectName;
	}

	/*
	public String read_out_debug_data(){
    	String sResult = "  ** " + SMUtilities.getFullClassName(this.toString()) + " read out: ";
    	sResult += "\nTerms code: " + this.
    	sResult += "\nTerms desc: " + this.getsTermsDescription();    	
    	sResult += "\nLast maintained: " + this.getsLastMaintainedDate();
    	sResult += "\nActive: " + this.getsActive();
    	sResult += "\nDiscount percentage: " + this.getsDiscountPercentage();
    	sResult += "\nDiscount number of days: " + this.getsDiscountNumberOfDays();
    	sResult += "\nDiscount day of the month: " + this.getsDiscountDayOfTheMonth();
    	sResult += "\nDue number of days: " + this.getsDueNumberOfDays();
    	sResult += "\nDue day of the month: " + this.getsDueDayOfTheMonth();    	
    	sResult += "\nObject name: " + this.getObjectName();
    	return sResult;
    }
	 */
	private void initEntryVariables(){
		m_slid = "-1";
		m_datinitiated = EMPTY_DATETIME_STRING;
		m_linitiatedbyid = "0";
		m_sinitiatedbyfullname = "";
		m_sresolved = "0";
		m_datresolved = EMPTY_DATETIME_STRING;
		m_lresolvedbyid = "0";
		m_sresolvedbyfullname = "";
		m_sdescription = "";
		m_scomments = "";
		m_sresolutioncomments = "";
		m_sworkorderid = "";
		m_strimmedordernumber = "";
		m_sNewRecord = "1";
	}
}
