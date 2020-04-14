package smar;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import SMDataDefinition.SMTablecallsheets;
import SMDataDefinition.SMTableorderheaders;
import SMDataDefinition.SMTablesalesperson;
import SMDataDefinition.SMTableusers;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsDateAndTimeConversions;
import ServletUtilities.clsManageRequestParameters;
import ServletUtilities.clsMasterEntry;
import ServletUtilities.clsServletUtilities;
import ServletUtilities.clsStringFunctions;

public class ARCallSheet extends clsMasterEntry{

	public static final String NAME_NOT_FOUND_MARKER = "(NOT FOUND)";
	
	public static final String ParamObjectName = "Call Sheet";

	public static final String ParamsID = "id";
	public static final String ParamsAcct = "sAcct";
	public static final String ParamsCallSheetName = "sCallSheetName";
	public static final String ParamsPhone = "sPhone";
	public static final String ParamsCollector = "sCollector";
	public static final String ParamsResponsibility = "sResponsibility";
	public static final String ParamsAccountTerms = "sAccountTerms";
	public static final String ParamdatLastContact = "datLastContact";
	public static final String ParamdatNextContact = "datNextContact";
	public static final String ParammNotes = "mNotes";
	public static final String ParamsJobPhone = "sJobPhone";
	public static final String ParamsOrderNumber = "sOrderNumber";
	public static final String ParamsCustomerName = "sCustomerName";
	public static final String ParamsAlertInits = "sAlertInits";
	public static final String ParambAlertInits = "sAlertInits";
	public static final String ParamsCollectorFullName = SMTablecallsheets.sCollectorFullName;
	public static final String ParamsResponsibilityFullName = SMTablecallsheets.sResponsibilityFullName;
	public static final String ParamsAlertsFulllName = SMTablecallsheets.sAlertFullName;
	
	private String m_sid;
	private String m_sacct;
	private String m_scallsheetname;
	private String m_sphone;
	private String m_scollector;
	private String m_sresponsibility;
	private String m_saccountterms;
	private String m_datlastcontact;
	private String m_datnextcontact;
	private String m_snotes;
	private String m_sjobphone;
	private String m_sordernumber;
	private String m_scustomername;
	private String m_salertinits;
	private String m_scollectorfullname;
	private String m_sresponsibilityfullname;
	private String m_salertsfullname;
	private boolean m_balertinits;

	public ARCallSheet() {
		super();
		initCallSheetVariables();
	}

	ARCallSheet (HttpServletRequest req){
		super(req);
		initCallSheetVariables();

		m_sid = clsManageRequestParameters.get_Request_Parameter(ParamsID, req).trim();
		//If there is no PARAM ID, then check to see if one has been passed in via the 'drop down list':
		if (m_sid.compareToIgnoreCase("") == 0){
			if (clsManageRequestParameters.get_Request_Parameter(
				AREditCallSheetsSelection.PARAM_DROP_DOWN_LIST, req).compareToIgnoreCase("") != 0){
				m_sid = clsManageRequestParameters.get_Request_Parameter(AREditCallSheetsSelection.PARAM_DROP_DOWN_LIST, req).trim();
			}
		}
		m_sacct = clsManageRequestParameters.get_Request_Parameter(ParamsAcct, req).trim();
		m_scallsheetname = clsManageRequestParameters.get_Request_Parameter(ParamsCallSheetName, req).trim();
		m_sphone = clsManageRequestParameters.get_Request_Parameter(ParamsPhone, req).trim();
		m_scollector = clsManageRequestParameters.get_Request_Parameter(ParamsCollector, req).trim();
		m_sresponsibility = clsManageRequestParameters.get_Request_Parameter(ParamsResponsibility, req).trim();
		m_saccountterms = clsManageRequestParameters.get_Request_Parameter(ParamsAccountTerms, req).trim();
		m_datlastcontact = clsManageRequestParameters.get_Request_Parameter(ParamdatLastContact, req).trim();
		if (m_datlastcontact.trim().compareToIgnoreCase("") == 0){m_datlastcontact = clsDateAndTimeConversions.now("MM/dd/yyyy");}
		m_datnextcontact = clsManageRequestParameters.get_Request_Parameter(ParamdatNextContact, req).trim();
		if (m_datnextcontact.trim().compareToIgnoreCase("") == 0){m_datnextcontact = clsDateAndTimeConversions.now("MM/dd/yyyy");}
		m_snotes = clsManageRequestParameters.get_Request_Parameter(ParammNotes, req).trim();
		m_sjobphone = clsManageRequestParameters.get_Request_Parameter(ParamsJobPhone, req).trim();
		m_sordernumber = clsManageRequestParameters.get_Request_Parameter(ParamsOrderNumber, req).trim();
		m_scustomername = clsManageRequestParameters.get_Request_Parameter(ParamsCustomerName, req).trim();
		m_balertinits = (req.getParameter(ParamsAlertInits) != null);
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
		clsDatabaseFunctions.freeConnection(context, conn, "[1547067501]");
		return bResult;

	}
	public boolean load (Connection conn){
		return load (m_sid, conn);
	}
	private boolean load (String sID, Connection conn){

		sID = sID.trim();
		if (sID.compareToIgnoreCase("") == 0){
			super.addErrorMessage("ID cannot be blank.");
			return false;
		}

		try {
			@SuppressWarnings("unused")
			long lTest = Long.parseLong(sID);
		} catch (NumberFormatException e1) {
			super.addErrorMessage("Invalid call sheet ID: '" + sID + "'");
			return false;
		}

		String SQL = " SELECT * FROM " + SMTablecallsheets.TableName
		+ " WHERE ("
		+ SMTablecallsheets.sID + " = " + sID
		+ ")";

		try {
			ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
			if (rs.next()) {
				//Load the variables here:
				m_sid = Long.toString(rs.getLong(SMTablecallsheets.sID));
				m_sacct = rs.getString(SMTablecallsheets.sAcct);
				if (m_sacct ==null){m_sacct = "";}
				m_scallsheetname = rs.getString(SMTablecallsheets.sCallSheetName);
				if (m_scallsheetname ==null){m_scallsheetname = "";}
				m_sphone = rs.getString(SMTablecallsheets.sPhone);
				if (m_sphone ==null){m_sphone = "";}
				m_scollector = rs.getString(SMTablecallsheets.sCollector);
				if (m_scollector ==null){m_scollector = "";}
				m_sresponsibility = rs.getString(SMTablecallsheets.sResponsibility);
				if (m_sresponsibility ==null){m_sresponsibility = "";}
				m_saccountterms = rs.getString(SMTablecallsheets.sAccountTerms);
				if (m_saccountterms ==null){m_saccountterms = "";}
				m_datlastcontact = clsDateAndTimeConversions.resultsetDateStringToString(
						rs.getString(SMTablecallsheets.datLastContact));
				m_datnextcontact = clsDateAndTimeConversions.resultsetDateStringToString(
						rs.getString(SMTablecallsheets.datNextContact));
				//System.out.println("In " + this.toString() 
				//	+ ".load - rs.getString(SMTablecallsheets.mNotes) = " 
				//	+ rs.getString(SMTablecallsheets.mNotes));
				m_snotes  = rs.getString(SMTablecallsheets.mNotes);
				if (m_snotes == null){m_snotes = "";}
				//System.out.println("In " + this.toString() 
				//	+ ".load - rs.getString(SMTablecallsheets.mNotes) = " 
				//	+ rs.getString(SMTablecallsheets.mNotes));

				m_sjobphone = rs.getString(SMTablecallsheets.sJobPhone);
				if (m_sjobphone ==null){m_sjobphone = "";}
				m_sordernumber = rs.getString(SMTablecallsheets.sOrderNumber);
				if (m_sordernumber ==null){m_sordernumber = "";}
				m_scustomername = rs.getString(SMTablecallsheets.sCustomerName);
				if (m_scustomername ==null){m_scustomername = "";}
				
				m_salertinits = rs.getString(SMTablecallsheets.sAlertInits);
				if (m_salertinits == null){m_salertinits = "";}
				if (m_salertinits.compareToIgnoreCase("") == 0){
					m_balertinits = false;
				}else{
					m_balertinits = true;
				}
				
				m_scollectorfullname = rs.getString(SMTablecallsheets.sCollectorFullName);
				m_sresponsibilityfullname = rs.getString(SMTablecallsheets.sResponsibilityFullName);
				m_salertsfullname = rs.getString(SMTablecallsheets.sAlertFullName);
				
				rs.close();
			} else {
				super.addErrorMessage("No " + ParamObjectName + " found for : '" + sID
						+ "'");
				rs.close();
				return false;
			}
		} catch (SQLException e) {
			super.addErrorMessage("Error reading " + ParamObjectName + " for : '" + sID
					+ "' - " + e.getMessage());
			return false;
		}
		return true;
	}

	public boolean save_without_data_transaction (
			ServletContext context, 
			String sDBID, 
			String sUserID,
			String sUserFullName,
			String sCompany){

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

		boolean bResult = save_without_data_transaction (conn, sUserID, sCompany);
		clsDatabaseFunctions.freeConnection(context, conn, "[1547067502]");
		return bResult;	

	}
	public boolean save_without_data_transaction (Connection conn, String sUserID, String sCompany){

		if (!validate_entry_fields(conn)){
			return false;
		}

		//Get the correct 'alert' initials to save with:
		String SQL = "";
		if (m_balertinits){
			//IF this call sheet does NOT currently have the 'alert' set, then it's being set now,
			//and we need to store the user's initials as the 'ALERT INITIALS':
			String sAlertCode = "";
			String sAlertFullName = "";
			String sUserInitials = "";
			String sUserFullName = "";
			//Only worry about this if it's an existing call sheet:
			if (getM_siID().compareToIgnoreCase("-1") != 0){
				SQL = "SELECT "
					+ SMTablecallsheets.sAlertInits
					+ ", " + SMTablecallsheets.sAlertFullName
					+ " FROM " + SMTablecallsheets.TableName
					+ " WHERE ("
						+ "(" + SMTablecallsheets.sID + " = " + getM_siID() + ")"
					+ ")"
					;
				try {
					ResultSet rsAlerts = clsDatabaseFunctions.openResultSet(SQL, conn);
					if (rsAlerts.next()){
						//This means this alert was already set
						sAlertCode = rsAlerts.getString(SMTablecallsheets.sAlertInits);
						sAlertFullName = rsAlerts.getString(SMTablecallsheets.sAlertFullName);
					}
					rsAlerts.close();
				} catch (SQLException e) {
					super.addErrorMessage("Error getting user alert initials for call sheet - " + e.getMessage());
					return false;
				}
			}
			
			//Now get the user's initials:
			SQL = "SELECT " 
					+ SMTableusers.sDefaultSalespersonCode 
					+ ", " + SMTableusers.sUserFirstName
					+ ", " + SMTableusers.sUserLastName
					+ " FROM " + SMTableusers.TableName
				+ " WHERE ("
					+ "(" + SMTableusers.lid + " = " + sUserID + ")"
				+ ")"
				;
			try {
				ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
				if (rs.next()){
					sUserInitials = rs.getString(SMTableusers.sDefaultSalespersonCode);
					sUserFullName = rs.getString(SMTableusers.sUserFirstName) + " " + rs.getString(SMTableusers.sUserLastName);
					rs.close();
				}else{
					super.addErrorMessage("No user salesperson code available for '" + sUserFullName + "'");
					rs.close();
					return false;
				}
			} catch (SQLException e1) {
				super.addErrorMessage("Error getting user salesperson code for '" + sUserFullName + "' - " + e1.getMessage());
				return false;
			}
			
			//So if the alert was NOT set on the current record, now set it to the current user's initials:
			if (sAlertCode.compareToIgnoreCase("") == 0){
				m_salertinits = sUserInitials;
				m_salertsfullname = sUserFullName;
			//But if it WAS already set, then just KEEP that previous alert initial in the record:
			}else{
				m_salertinits = sAlertCode;
				m_salertsfullname = sAlertFullName;
			}
		}else{
			m_salertinits = "";
			m_salertsfullname = "";
		}
		
		if (this.m_sid.compareToIgnoreCase("-1") == 0){
			SQL = "INSERT INTO " + SMTablecallsheets.TableName + " ("
			+ SMTablecallsheets.datLastContact
			+ ", " + SMTablecallsheets.datNextContact
			+ ", " + SMTablecallsheets.mNotes
			+ ", " + SMTablecallsheets.sAccountTerms
			+ ", " + SMTablecallsheets.sAcct
			+ ", " + SMTablecallsheets.sAlertInits
			+ ", " + SMTablecallsheets.sCallSheetName
			+ ", " + SMTablecallsheets.sCollector
			+ ", " + SMTablecallsheets.sCustomerName
			+ ", " + SMTablecallsheets.sJobPhone
			+ ", " + SMTablecallsheets.sOrderNumber
			+ ", " + SMTablecallsheets.sPhone
			+ ", " + SMTablecallsheets.sResponsibility
			+ ", " + SMTablecallsheets.sCollectorFullName
			+ ", " + SMTablecallsheets.sResponsibilityFullName
			+ ", " + SMTablecallsheets.sAlertFullName
			+ ") VALUES ("
			+ "'" + clsDateAndTimeConversions.stdDateStringToSQLDateString(m_datlastcontact) + "'"
			+ ", '" + clsDateAndTimeConversions.stdDateStringToSQLDateString(m_datnextcontact) + "'"
			+ ", '" + clsDatabaseFunctions.FormatSQLStatement(m_snotes.trim()) + "'"
			+ ", '" + clsDatabaseFunctions.FormatSQLStatement(m_saccountterms.trim()) + "'"
			+ ", '" + clsDatabaseFunctions.FormatSQLStatement(m_sacct.trim()) + "'"
			+ ", '" + clsDatabaseFunctions.FormatSQLStatement(m_salertinits.trim()) + "'"
			+ ", '" + clsDatabaseFunctions.FormatSQLStatement(m_scallsheetname.trim()) + "'"
			+ ", '" + clsDatabaseFunctions.FormatSQLStatement(m_scollector.trim()) + "'"
			+ ", '" + clsDatabaseFunctions.FormatSQLStatement(m_scustomername.trim()) + "'"
			+ ", '" + clsDatabaseFunctions.FormatSQLStatement(m_sjobphone.trim()) + "'"
			+ ", '" + clsDatabaseFunctions.FormatSQLStatement(m_sordernumber.trim()) + "'"
			+ ", '" + clsDatabaseFunctions.FormatSQLStatement(m_sphone.trim()) + "'"
			+ ", '" + clsDatabaseFunctions.FormatSQLStatement(m_sresponsibility.trim()) + "'"
			+ ", '" + clsDatabaseFunctions.FormatSQLStatement(m_scollectorfullname.trim()) + "'"
			+ ", '" + clsDatabaseFunctions.FormatSQLStatement(m_sresponsibilityfullname.trim()) + "'"
			+ ", '" + clsDatabaseFunctions.FormatSQLStatement(m_salertsfullname.trim()) + "'"
			+ ")"
			;
		}else{

			SQL = "UPDATE " + SMTablecallsheets.TableName + " SET"
			+ " " + SMTablecallsheets.datLastContact + " = '" 
			+ clsDateAndTimeConversions.stdDateStringToSQLDateString(m_datlastcontact) + "'"
			+ ", " + SMTablecallsheets.datNextContact + " = '" 
			+ clsDateAndTimeConversions.stdDateStringToSQLDateString(m_datnextcontact) + "'"
			+ ", " + SMTablecallsheets.mNotes + " = '" + clsDatabaseFunctions.FormatSQLStatement(m_snotes.trim()) + "'"
			+ ", " + SMTablecallsheets.sAccountTerms + " = '" 
			+ clsDatabaseFunctions.FormatSQLStatement(m_saccountterms.trim()) + "'"	
			+ ", " + SMTablecallsheets.sAcct + " = '" + clsDatabaseFunctions.FormatSQLStatement(m_sacct.trim()) + "'"
			+ ", " + SMTablecallsheets.sAlertInits + " = '" + clsDatabaseFunctions.FormatSQLStatement(m_salertinits.trim()) + "'"
			+ ", " + SMTablecallsheets.sCallSheetName + " = '" + clsDatabaseFunctions.FormatSQLStatement(m_scallsheetname.trim()) + "'"
			+ ", " + SMTablecallsheets.sCollector + " = '" + clsDatabaseFunctions.FormatSQLStatement(m_scollector.trim()) + "'"
			+ ", " + SMTablecallsheets.sCustomerName + " = '" + clsDatabaseFunctions.FormatSQLStatement(m_scustomername.trim()) + "'"
			+ ", " + SMTablecallsheets.sJobPhone + " = '" + clsDatabaseFunctions.FormatSQLStatement(m_sjobphone.trim()) + "'"
			+ ", " + SMTablecallsheets.sOrderNumber + " = '" + clsDatabaseFunctions.FormatSQLStatement(m_sordernumber.trim()) + "'"
			+ ", " + SMTablecallsheets.sPhone + " = '" + clsDatabaseFunctions.FormatSQLStatement(m_sphone.trim()) + "'"
			+ ", " + SMTablecallsheets.sResponsibility + " = '" + clsDatabaseFunctions.FormatSQLStatement(m_sresponsibility.trim()) + "'"
			+ ", " + SMTablecallsheets.sCollectorFullName + " = '" + clsDatabaseFunctions.FormatSQLStatement(m_scollectorfullname.trim()) + "'"
			+ ", " + SMTablecallsheets.sResponsibilityFullName + " = '" + clsDatabaseFunctions.FormatSQLStatement(m_sresponsibilityfullname.trim()) + "'"
			+ ", " + SMTablecallsheets.sAlertFullName + " = '" + clsDatabaseFunctions.FormatSQLStatement(m_salertsfullname.trim()) + "'"
			+ " WHERE ("
			+ SMTablecallsheets.sID + " = " + m_sid 
			+ ")"
			;
		}
		try{
			Statement stmt = conn.createStatement();
			stmt.executeUpdate(SQL);
		}catch(SQLException ex){
			super.addErrorMessage("Error updating " + ParamObjectName + ": " + ex.getMessage());
			return false;
		}

		//Get the call sheet id here:
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
		clsDatabaseFunctions.freeConnection(context, conn, "[1547067500]");
		return bResult;

	}
	public boolean delete (Connection conn){

		String SQL = "DELETE FROM " + SMTablecallsheets.TableName
		+ " WHERE ("
		+ "(" + SMTablecallsheets.sID + " = " + m_sid + ")"
		+ ")"
		;
		try{
			Statement stmt = conn.createStatement();
			stmt.executeUpdate(SQL);
		}catch (Exception ex) {
			super.addErrorMessage("Error deleting " + ParamObjectName + " with SQL: " 
					+ SQL + " - " + ex.getMessage());
			return false;
		}

		//Empty the values:
		initCallSheetVariables();
		return true;
	}
	public boolean validate_entry_fields (Connection conn){
		//Validate the entries here:
		//Validate the entries here:
		boolean bEntriesAreValid = true;

		m_sid = m_sid.trim().replace(",", "");
		try {
			long lTest = Long.parseLong(m_sid);
			if ((lTest <-1)){
				super.addErrorMessage("ID '" + m_sid + "' is invalid.");
				bEntriesAreValid = false;
			}
		} catch (NumberFormatException e) {
			super.addErrorMessage("ID '" + m_sid + "' is invalid.");
			bEntriesAreValid = false;	
		}

		m_sacct = m_sacct.trim();
		if (m_sacct.compareToIgnoreCase("") == 0){
			super.addErrorMessage("Account number cannot be empty.");
			bEntriesAreValid = false;
			return bEntriesAreValid;
		}
		if (m_sacct.length() > SMTablecallsheets.sAcctLength){
			super.addErrorMessage("Account Number is too long.");
			bEntriesAreValid = false;
		}

		m_scallsheetname = m_scallsheetname.trim();
		if (m_scallsheetname.compareToIgnoreCase("") == 0){
			super.addErrorMessage("Call sheet name cannot be empty.");
			bEntriesAreValid = false;
			return bEntriesAreValid;
		}
		if (m_scallsheetname.length() > SMTablecallsheets.sCallSheetNameLength){
			super.addErrorMessage("Call sheet name is too long.");
			bEntriesAreValid = false;
		}

		m_sphone = m_sphone.trim();
		if (m_sphone.length() > SMTablecallsheets.sPhoneLength){
			super.addErrorMessage("Phone number is too long.");
			bEntriesAreValid = false;
		}

		m_scollector = m_scollector.trim();
		if (m_scollector.compareToIgnoreCase("") == 0){
			super.addErrorMessage("Collector cannot be empty.");
			bEntriesAreValid = false;
			return bEntriesAreValid;
		}
		if (m_scollector.length() > SMTablecallsheets.sCollectorLength){
			super.addErrorMessage("Collector is too long.");
			bEntriesAreValid = false;
		}

		m_sresponsibility = m_sresponsibility.trim();
		if (m_sresponsibility.length() > SMTablecallsheets.sResponsibilityLength){
			super.addErrorMessage("Responsibility is too long.");
			bEntriesAreValid = false;
		}

		m_saccountterms = m_saccountterms.trim();
		if (m_saccountterms.length() > SMTablecallsheets.sAccountTermsLength){
			super.addErrorMessage("Account terms code is too long.");
			bEntriesAreValid = false;
		}

		if(!isDateValid("Last contact date", m_datlastcontact)){bEntriesAreValid = false;};
		if(!isDateValid("Next contact date", m_datnextcontact)){bEntriesAreValid = false;};

		m_snotes = m_snotes.trim();

		m_sjobphone = m_sjobphone.trim();
		if (m_sjobphone.length() > SMTablecallsheets.sJobPhoneLength){
			super.addErrorMessage("Job phone is too long.");
			bEntriesAreValid = false;
		}

		m_sordernumber = m_sordernumber.trim();
		if (m_sordernumber.length() > SMTablecallsheets.sOrderNumberLength){
			super.addErrorMessage("Order number is too long.");
			bEntriesAreValid = false;
		}

		m_scustomername = m_scustomername.trim();
		if (m_scustomername.length() > SMTablecallsheets.sCustomerNameLength){
			super.addErrorMessage("Customer name is too long.");
			bEntriesAreValid = false;
		}

		//Read the actual names of the collector, if available:
		if (m_scollector.compareToIgnoreCase("") != 0) {
			String SQL = "SELECT"
				+ " " + SMTablesalesperson.sSalespersonFirstName
				+ ", " + SMTablesalesperson.sSalespersonFirstName
				+ " FROM " + SMTablesalesperson.TableName
				+ " WHERE ("
					+ "(" + SMTablesalesperson.sSalespersonCode + " = '" + m_scollector + "')"
				+ ")"
			;
			try {
				ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
				if (rs.next()) {
					m_scollectorfullname = rs.getString(SMTablesalesperson.sSalespersonFirstName) + " " + rs.getString(SMTablesalesperson.sSalespersonLastName);
				}else {
					//We'll just leave the collector full name as it is here, in case it's an old name of someone who's been removed:
					//m_scollectorfullname = NAME_NOT_FOUND_MARKER;
				}
				rs.close();
			} catch (SQLException e) {
				super.addErrorMessage("Error [1586813050] reading collector full name with SQL '" + SQL + "' - " + e.getMessage() + ".");
			}
		}else {
			m_scollectorfullname = "";
		}
		
		//Read the actual names of the responsibility, if available:
		if (m_scollector.compareToIgnoreCase("") != 0) {
			String SQL = "SELECT"
				+ " " + SMTablesalesperson.sSalespersonFirstName
				+ ", " + SMTablesalesperson.sSalespersonFirstName
				+ " FROM " + SMTablesalesperson.TableName
				+ " WHERE ("
					+ "(" + SMTablesalesperson.sSalespersonCode + " = '" + m_sresponsibility + "')"
				+ ")"
			;
			try {
				ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
				if (rs.next()) {
					m_sresponsibilityfullname = rs.getString(SMTablesalesperson.sSalespersonFirstName) + " " + rs.getString(SMTablesalesperson.sSalespersonLastName);
				}else {
					//We'll just leave the responsibility full name as it is here, in case it's an old name of someone who's been removed:
					//m_sresponsibilityfullname = NAME_NOT_FOUND_MARKER;
				}
				rs.close();
			} catch (SQLException e) {
				super.addErrorMessage("Error [1586813051] reading responsibility full name with SQL '" + SQL + "' - " + e.getMessage() + ".");
			}
		}else {
			m_sresponsibilityfullname = "";
		}
		
		return bEntriesAreValid;
	}
	private boolean isDateValid(String sDateLabel, String sTestDate){
		if (sTestDate.compareTo(EMPTY_DATE_STRING) != 0){
			if (!clsDateAndTimeConversions.IsValidDateString("M/d/yyyy", sTestDate)){
				super.addErrorMessage(sDateLabel + " '" + sTestDate + "' is invalid.  ");
				return false;
			}
		}
		return true;
	}
	public boolean updateDefaultsFromOrder(ServletContext context, String sDBID, String sUserID, String sUserFullName){
		
		String sOrderNumber = m_sordernumber.trim();
		if (sOrderNumber.compareToIgnoreCase("") == 0){
			super.addErrorMessage("Cannot update from order - order number is blank.");
			return false;
		}
		String SQL = "SELECT"
			+ " " + SMTableorderheaders.sBillToPhone
			+ ", " + SMTableorderheaders.sBillToName
			+ ", " + SMTableorderheaders.sCustomerCode
			+ ", " + SMTableorderheaders.sSalesperson
			+ ", " + SMTableorderheaders.sShipToPhone
			+ ", " + SMTableorderheaders.sShipToName
			+ ", " + SMTableorderheaders.sTerms
			+ " FROM " + SMTableorderheaders.TableName
			+ " WHERE ("
				+ "(" + SMTableorderheaders.sOrderNumber + " = '" + clsStringFunctions.PadLeft(sOrderNumber, " ", 8) + "')"
			+ ")"
			;

		try {
			ResultSet rs = clsDatabaseFunctions.openResultSet(
					SQL, 
					context, 
					sDBID, 
					"MySQL", 
					this.toString() + ".updateDefaultsFromOrder - user: " 
					+ sUserID
					+ " - "
					+ sUserFullName
					);
			if (rs.next()){
				m_saccountterms = rs.getString(SMTableorderheaders.sTerms);
				m_sacct = rs.getString(SMTableorderheaders.sCustomerCode);
				m_scallsheetname = sOrderNumber + " - " + rs.getString(SMTableorderheaders.sShipToName);
				m_scustomername = rs.getString(SMTableorderheaders.sBillToName);
				m_sjobphone = rs.getString(SMTableorderheaders.sShipToPhone);
				m_sphone = rs.getString(SMTableorderheaders.sBillToPhone);
				//We don't necessarily want the salesperson to be the default 'responsibility':
				// TJR - 9/27/2011 - commented this out:
				//m_sresponsibility = rs.getString(SMTableorderheaders.sSalesperson);
			}else{
				super.addErrorMessage("Could not read order number '" + sOrderNumber + "'.");
				return false;
			}
		} catch (SQLException e) {
			super.addErrorMessage("Error reading default order info with SQL: " + SQL + " - " + e.getMessage());
			return false;
		}
		
		return true;
	}
	public String read_out_debug_data(){
		String sResult = "  ** ARCallSheet read out: ";
		sResult += "\nID: " + m_sid;
		sResult += "\nAcct: " + m_sacct;
		sResult += "\nCall sheet name: " + m_scallsheetname;
		sResult += "\nPhone: " + m_sphone;
		sResult += "\nCollector: " + m_scollector;
		sResult += "\nResponsibility: " + m_sresponsibility;
		sResult += "\nAccount terms: " + m_saccountterms;
		sResult += "\nLast contact date: " + m_datlastcontact;
		sResult += "\nNext contact date: " + m_datnextcontact;
		sResult += "\nNotes: " + m_snotes;
		sResult += "\nJob phone: " + m_sjobphone;
		sResult += "\nOrder number: " + m_sordernumber;
		sResult += "\nCustomer name: " + m_scustomername;
		sResult += "\nAlert initials: " + m_salertinits;
		sResult += "\nCollector full name: " + m_scollectorfullname;
		sResult += "\nAlert full name: " + m_salertsfullname;
		sResult += "\nResponsibility full name: " + m_sresponsibilityfullname;
		return sResult;
	}

	public void addErrorMessage(String sMsg){
		super.addErrorMessage(sMsg);
	}
	public String getQueryString(){
		String sQueryString = "";
		sQueryString += "&" + ParamObjectName + "=" + clsServletUtilities.URLEncode(getObjectName());
		sQueryString += "&" + ParamdatLastContact + "=" + clsServletUtilities.URLEncode(m_datlastcontact);
		sQueryString += "&" + ParamdatNextContact + "=" + clsServletUtilities.URLEncode(m_datnextcontact);
		sQueryString += "&" + ParammNotes + "=" + clsServletUtilities.URLEncode(m_snotes);
		sQueryString += "&" + ParamsAccountTerms + "=" + clsServletUtilities.URLEncode(m_saccountterms);
		sQueryString += "&" + ParamsAcct + "=" + clsServletUtilities.URLEncode(m_sacct);
		sQueryString += "&" + ParamsAlertInits + "=" + clsServletUtilities.URLEncode(m_salertinits);
		if (m_balertinits){
			sQueryString += "&" + ParambAlertInits + "=" + clsServletUtilities.URLEncode(m_salertinits);
		}
		sQueryString += "&" + ParamsCallSheetName + "=" + clsServletUtilities.URLEncode(m_scallsheetname);
		sQueryString += "&" + ParamsCollector + "=" + clsServletUtilities.URLEncode(m_scollector);
		sQueryString += "&" + ParamsCustomerName + "=" + clsServletUtilities.URLEncode(m_scustomername);
		sQueryString += "&" + ParamsID + "=" + clsServletUtilities.URLEncode(m_sid);
		sQueryString += "&" + ParamsJobPhone + "=" + clsServletUtilities.URLEncode(m_sjobphone);
		sQueryString += "&" + ParamsOrderNumber + "=" + clsServletUtilities.URLEncode(m_sordernumber);
		sQueryString += "&" + ParamsPhone + "=" + clsServletUtilities.URLEncode(m_sphone);
		sQueryString += "&" + ParamsResponsibility + "=" + clsServletUtilities.URLEncode(m_sresponsibility);
		sQueryString += "&" + ParamsCollectorFullName + "=" + clsServletUtilities.URLEncode(m_scollectorfullname);
		sQueryString += "&" + ParamsResponsibilityFullName + "=" + clsServletUtilities.URLEncode(m_sresponsibilityfullname);
		sQueryString += "&" + ParamsAlertsFulllName + "=" + clsServletUtilities.URLEncode(m_salertsfullname);
		return sQueryString;
	}

	public String getM_siID() {
		return m_sid;
	}

	public void setM_siID(String mSiID) {
		m_sid = mSiID;
	}
	public String getM_sOrderNumber() {
		return m_sordernumber;
	}
	public void setM_sOrderNumber(String sOrderNumber) {
		m_sordernumber = sOrderNumber;
	}
	public String getM_sCallSheetName() {
		return m_scallsheetname;
	}
	public void setM_sCallSheetName(String sCallSheetName) {
		m_scallsheetname = sCallSheetName;
	}
	public String getsPhone() {
		return m_sphone;
	}
	public void setM_sPhone(String sPhone) {
		m_sphone = sPhone;
	}

	public String getsCollector() {
		return m_scollector;
	}
	public void setM_sCollector(String sCollector) {
		m_scollector = sCollector;
	}
	public String getM_sResponsibility() {
		return m_sresponsibility;
	}
	public void setM_sResponsibility(String sResponsibility) {
		m_sresponsibility = sResponsibility;
	}

	public String getM_LastContactDate() {
		return m_datlastcontact;
	}

	public void setM_LastContactDate(String sLastContactDate) {
		m_datlastcontact = sLastContactDate;
	}

	public String getM_NextContactDate() {
		return m_datnextcontact;
	}

	public void setM_NextContactDate(String sNextContactDate) {
		m_datnextcontact = sNextContactDate;
	}

	public String getM_sAcct() {
		return m_sacct;
	}
	public void setM_sAcct(String sAcct) {
		m_sacct = sAcct;
	}

	public String getM_notes() {
		return m_snotes;
	}
	public void setM_notes(String sNotes) {
		m_snotes = sNotes;
	}

	public String getM_sJobPhone() {
		return m_sjobphone;
	}

	public void setM_sJobPhone(String sJobPhone) {
		m_sjobphone = sJobPhone;
	}

	public String getM_sCustomerName() {
		return m_scustomername;
	}

	public void setM_sCustomerName(String sCustomerName) {
		m_scustomername = sCustomerName;
	}

	public String getM_sAlertInits() {
		return m_salertinits;
	}

	public void setM_sAlertInits(String mAlertInits) {
		m_salertinits = mAlertInits;
	}

	public boolean getbAlertInits() {
		return m_balertinits;
	}

	public void setbAlertInits(boolean bAlertInits) {
		m_balertinits = bAlertInits;
	}

	public String getM_sAccountTerms() {
		return m_saccountterms;
	}

	public void setM_sAccountTerms(String sAccountTerms) {
		m_saccountterms = sAccountTerms;
	}
	
	public String getM_scollectorfullname() {
		return m_scollectorfullname;
	}

	public void setM_scollectorfullname(String sCollectorFullName) {
		m_scollectorfullname = sCollectorFullName;
	}
	
	public String getM_sresponsibilityfullname() {
		return m_sresponsibilityfullname;
	}

	public void setM_sresponsibilityfullname(String sResponsibilityFullName) {
		m_sresponsibilityfullname = sResponsibilityFullName;
	}

	public String getM_salertsfullname() {
		return m_salertsfullname;
	}

	public void setM_salertsfullname(String sAlertsFullName) {
		m_salertsfullname = sAlertsFullName;
	}

	private void initCallSheetVariables(){
		m_sid = "-1";
		m_sordernumber = "";
		m_scallsheetname = "";
		m_sphone = "";
		m_scollector = "";
		m_sresponsibility = "";
		m_saccountterms = "";
		m_sacct = "";
		m_datlastcontact = clsDateAndTimeConversions.now("MM/dd/yyyy");
		m_datnextcontact = clsDateAndTimeConversions.now("MM/dd/yyyy");
		m_snotes = "";
		m_sjobphone = "";
		m_scustomername = "";
		m_balertinits = false;
		m_salertinits = "";
		m_scollectorfullname = "";
		m_sresponsibilityfullname = "";
		m_salertsfullname = "";
		super.initVariables();
		super.setObjectName(ParamObjectName);
	}
}