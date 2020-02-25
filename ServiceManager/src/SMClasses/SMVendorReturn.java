package SMClasses;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import SMDataDefinition.SMTablematerialreturns;
import SMDataDefinition.SMTablevendorreturns;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsDateAndTimeConversions;
import ServletUtilities.clsManageBigDecimals;
import ServletUtilities.clsManageRequestParameters;
import ServletUtilities.clsMasterEntry;
import ServletUtilities.clsServletUtilities;
import smap.APVendor;
import smcontrolpanel.SMMasterEditSelect;
import smic.ICEntry;
import smic.ICPOHeader;

public class SMVendorReturn extends clsMasterEntry{

	public static final String ParamObjectName = "Vendor Return";

	public static final String Paramlid = "lid";
	public static final String Paramiinvoiceonhold = "iinvoiceonhold";
	public static final String Paramitobereturned = "itobereturned";
	public static final String Paramicreditdue = "iCreditDue";
	public static final String Paramsvendoracct = "svendoracct";
	public static final String Paramiponumber = "iponumber";
	public static final String Parammvendorcomments = "mVendorComments";
	public static final String Paramdatreturnsent = "datreturnsent";
	public static final String Paramladjustedbatchnumber = "ladjustedbatchnumber";
	public static final String Paramladjustedentrynumber = "ladjustedentrynumber";
	public static final String Parambdadjustmentamount = "bdadjustmentamount";
	public static final String Paramscreditmemonumber = "screditmemonumber";
	public static final String Paramdatcreditnotedate = "datcreditnotedate";
	public static final String Parambdcreditamt = "bdcreditamt";
	public static final String Paramicreditnotexpected = "icreditnotexpected";
	public static final String Paramdatinitiated = "datinitiated";
	public static final String Paramlinitiatedbyid = "linitiatedbyid";
	public static final String Paramsinitiatedbyfullname = "sinitiatedbyfullname";


	private String m_slid;
	private String m_sNewRecord;
	private String m_screditnotexpected;
	private String m_sponumber;
	private String m_itobereturned;
	private String m_svendoracct;
	private String m_ladjustedbatchnumber;
	private String m_ladjustedentrynumber;
	private String m_bdadjustmentamount;
	private String m_datcreditnotedate;
	private String m_screditmemonumber;
	private String m_bdcreditamt;
	private String m_datreturnsent;
	private String m_sinvoiceonhold;
	private String m_svendorcomments;
	private String m_icreditdue;
	private String m_datinitiated;
	private String m_linitiatedbyid;
	private String m_sinitiatedbyfullname;

	private boolean bDebugMode = false;

	public SMVendorReturn() {
		super();
		initEntryVariables();
	}

	public SMVendorReturn(HttpServletRequest req){
		super(req);
		initEntryVariables();
		m_slid = clsManageRequestParameters.get_Request_Parameter(
				SMVendorReturn.Paramlid, req).trim();
		m_slid = clsManageRequestParameters.get_Request_Parameter(
				SMVendorReturn.Paramlid, req).trim();
		m_datinitiated = clsManageRequestParameters.get_Request_Parameter(
				SMVendorReturn.Paramdatinitiated, req).trim().replace("&quot;", "\"");
		if(m_datinitiated.compareToIgnoreCase("") == 0){
			m_datinitiated = EMPTY_DATETIME_STRING;
		}
		m_linitiatedbyid = clsManageRequestParameters.get_Request_Parameter(
				SMVendorReturn.Paramlinitiatedbyid, req).trim().replace("&quot;", "\"");
		if( m_linitiatedbyid.compareToIgnoreCase("") == 0){
			m_linitiatedbyid = "0";
		}
		m_sinitiatedbyfullname = clsManageRequestParameters.get_Request_Parameter(
				SMVendorReturn.Paramsinitiatedbyfullname, req).trim().replace("&quot;", "\"");
		m_screditnotexpected = clsManageRequestParameters.get_Request_Parameter(
				SMVendorReturn.Paramicreditnotexpected, req).trim().replace("&quot;", "\"");
		m_sponumber = clsManageRequestParameters.get_Request_Parameter(
				SMVendorReturn.Paramiponumber, req).trim().replace("&quot;", "\"");
		
		if((req.getParameter(SMVendorReturn.Paramitobereturned) == null)||(req.getParameter(SMVendorReturn.Paramitobereturned).compareToIgnoreCase("0")==0)){
			m_itobereturned = "0";
		}else{
			m_itobereturned = "1";
		}
		
		m_svendoracct = clsManageRequestParameters.get_Request_Parameter(
				SMVendorReturn.Paramsvendoracct, req).trim().replace("&quot;", "\"");
		m_ladjustedbatchnumber = clsManageRequestParameters.get_Request_Parameter(
				SMVendorReturn.Paramladjustedbatchnumber, req).trim().replace("&quot;", "\"");
		if(clsManageRequestParameters.get_Request_Parameter(SMVendorReturn.Paramladjustedbatchnumber, req).compareToIgnoreCase("") == 0){
			m_ladjustedbatchnumber = "0";
		}
		m_ladjustedentrynumber = clsManageRequestParameters.get_Request_Parameter(
				SMVendorReturn.Paramladjustedentrynumber, req).trim().replace("&quot;", "\"");
		if(clsManageRequestParameters.get_Request_Parameter(SMVendorReturn.Paramladjustedentrynumber, req).compareToIgnoreCase("") == 0){
			m_ladjustedentrynumber = "0";
		}
		m_bdadjustmentamount = clsManageRequestParameters.get_Request_Parameter(
				SMVendorReturn.Parambdadjustmentamount, req).trim().replace("&quot;", "\"");
		if(clsManageRequestParameters.get_Request_Parameter(SMVendorReturn.Parambdadjustmentamount, req).compareToIgnoreCase("") == 0){
			m_bdadjustmentamount = "0.00";
		}
		m_datcreditnotedate = clsManageRequestParameters.get_Request_Parameter(
				SMVendorReturn.Paramdatcreditnotedate, req).trim().replace("&quot;", "\"");
		if(clsManageRequestParameters.get_Request_Parameter(SMVendorReturn.Paramdatcreditnotedate, req).compareToIgnoreCase("") == 0){
			m_datcreditnotedate = clsServletUtilities.EMPTY_DATE_VALUE;
		}
		m_screditmemonumber = clsManageRequestParameters.get_Request_Parameter(
				SMVendorReturn.Paramscreditmemonumber, req).trim().replace("&quot;", "\"");

		m_bdcreditamt = clsManageRequestParameters.get_Request_Parameter(
				SMVendorReturn.Parambdcreditamt, req).trim().replace("&quot;", "\"");
		if(clsManageRequestParameters.get_Request_Parameter(SMVendorReturn.Parambdcreditamt, req).compareToIgnoreCase("") == 0){
			m_bdcreditamt = "0.00";
		}

		m_datreturnsent = clsManageRequestParameters.get_Request_Parameter(
				SMVendorReturn.Paramdatreturnsent, req).trim().replace("&quot;", "\"");
		if(clsManageRequestParameters.get_Request_Parameter(SMVendorReturn.Paramdatreturnsent, req).compareToIgnoreCase("") == 0){
			m_datreturnsent = clsServletUtilities.EMPTY_DATE_VALUE;
		}
		
		if((req.getParameter(SMVendorReturn.Paramiinvoiceonhold) == null)|| (req.getParameter(SMVendorReturn.Paramiinvoiceonhold).compareToIgnoreCase("0")==0)){
			m_sinvoiceonhold = "0";
		}else{
			m_sinvoiceonhold = "1";
		}
		
		m_svendorcomments = clsManageRequestParameters.get_Request_Parameter(
				SMVendorReturn.Parammvendorcomments, req).trim().replace("&quot;", "\""); 
		
		if(req.getParameter(SMVendorReturn.Paramicreditdue) == null){
			m_icreditdue = "0";
		}else{
			m_icreditdue = "1";
		}

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

		String SQL = "SELECT * FROM " + SMTablevendorreturns.TableName
				+ " WHERE ("
				+ SMTablevendorreturns.lid + " = " + sID
				+ ")";
		if (bDebugMode){
			System.out.println("[1579186172] In " + this.toString() + " - load SQL = " + SQL);
		}
		try {
			ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
			if (rs.next()) {
				//Load the variables here:
				m_slid = Long.toString(rs.getLong(SMTablevendorreturns.lid));
				m_datinitiated = clsDateAndTimeConversions.resultsetDateTimeStringToString(
						rs.getString(SMTablematerialreturns.datinitiated));
				m_linitiatedbyid = Long.toString(rs.getLong(SMTablematerialreturns.linitiatedbyid));
				m_sinitiatedbyfullname = rs.getString(SMTablematerialreturns.sinitiatedbyfullname).trim();
				m_screditnotexpected = Long.toString(rs.getLong(SMTablevendorreturns.icreditnotexpected));
				m_sponumber = Long.toString(rs.getLong(SMTablevendorreturns.iponumber));
				if (Long.parseLong(m_sponumber) < 1){
					m_sponumber = "";
				}
				m_itobereturned = Long.toString(rs.getLong(SMTablevendorreturns.itobereturned));
				m_svendoracct = rs.getString(SMTablevendorreturns.svendoracct).trim();

				m_ladjustedbatchnumber = Long.toString(rs.getLong(SMTablevendorreturns.ladjustedbatchnumber));
				if(m_ladjustedbatchnumber.compareToIgnoreCase("")== 0) {
					m_ladjustedbatchnumber = "0";
				}
				m_ladjustedentrynumber = Long.toString(rs.getLong(SMTablevendorreturns.ladjustedentrynumber));
				if(m_ladjustedentrynumber.compareToIgnoreCase("")== 0) {
					m_ladjustedentrynumber = "0";
				}
				m_bdadjustmentamount = clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(rs.getBigDecimal(SMTablevendorreturns.bdadjustmentamount));
				m_datcreditnotedate = clsDateAndTimeConversions.resultsetDateStringToString(rs.getString(SMTablevendorreturns.datcreditnotedate));
				m_screditmemonumber = rs.getString(SMTablevendorreturns.screditmemonumber).trim();
				m_bdcreditamt = clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(rs.getBigDecimal(SMTablevendorreturns.bdcreditamt));
				m_datreturnsent = clsDateAndTimeConversions.resultsetDateStringToString(rs.getString(SMTablevendorreturns.datreturnsent));
				m_sinvoiceonhold = Long.toString(rs.getLong(SMTablevendorreturns.iinvoiceonhold));
				m_svendorcomments = rs.getString(SMTablevendorreturns.mVendorComments).trim();
				m_icreditdue = Integer.toString(rs.getInt(SMTablevendorreturns.iCreditDue));
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
		
		long lid;
		try {
			lid = Long.parseLong(getslid());
		} catch (Exception e1) {
			throw new Exception("Error [1408653581] parsing " + ParamObjectName + " lid '" + this.getslid() + "' - " + e1.getMessage());
		}
		if (lid < 1){
			//It's a new record
			setsNewRecord("1");
		}

		
		String sPONumber = getsponumber();
		if (sPONumber.compareToIgnoreCase("") == 0){
			sPONumber = "0";
		}
		String sCreditStatus = getscreditnotexpected();
		if (sCreditStatus.compareToIgnoreCase("") == 0){
			sCreditStatus = "0";
		}
		String sCreditDue = getiCreditDue();
		if (sCreditDue.compareToIgnoreCase("") == 0){
			sCreditDue = "0";
		}

		//If it's a new record, do an insert:
		if (getsNewRecord().compareToIgnoreCase("1") == 0){
			SQL = "INSERT INTO " + SMTablevendorreturns.TableName + " ("
					+ " " + SMTablevendorreturns.itobereturned
					+ ", " + SMTablevendorreturns.icreditnotexpected
					+ ", " + SMTablevendorreturns.iponumber
					+ ", " + SMTablevendorreturns.svendoracct
					+ ", " + SMTablevendorreturns.ladjustedbatchnumber
					+ ", " + SMTablevendorreturns.ladjustedentrynumber
					+ ", " + SMTablevendorreturns.bdadjustmentamount
					+ ", " + SMTablevendorreturns.datcreditnotedate
					+ ", " + SMTablevendorreturns.screditmemonumber
					+ ", " + SMTablevendorreturns.bdcreditamt
					+ ", " + SMTablevendorreturns.datreturnsent
					+ ", " + SMTablevendorreturns.iinvoiceonhold
					+ ", " + SMTablevendorreturns.mVendorComments
					+ ", " + SMTablevendorreturns.iCreditDue
					+ ", " + SMTablevendorreturns.datinitiated
					+ ", " + SMTablevendorreturns.sinitiatedbyfullname
					+ ", " + SMTablevendorreturns.linitiatedbyid
					+ ") VALUES ( " 
					+  getstobereturned()
					+ ", " + sCreditStatus
					+ ", " + sPONumber
					+ ", '" + clsDatabaseFunctions.FormatSQLStatement(getsvendoracct().trim()) + "'"
					+ ", " + clsDatabaseFunctions.FormatSQLStatement(getladjustedbatchnumber().trim()) + ""
					+ ", " +  clsDatabaseFunctions.FormatSQLStatement(getladjustedentrynumber().trim()) + ""
					+ ", " +  clsDatabaseFunctions.FormatSQLStatement(getbdadjustmentamount().trim()) + ""
					+ ", '" +  clsDateAndTimeConversions.stdDateStringToSQLDateString(getdatcreditnotedate().trim()) + "'"
					+ ", '" +  clsDatabaseFunctions.FormatSQLStatement(getscreditmemonumber().trim()) + "'"
					+ ", " +  clsDatabaseFunctions.FormatSQLStatement(getbdcreditamt().trim()) + ""
					+ ", '" +  clsDateAndTimeConversions.stdDateStringToSQLDateString(getdatreturnsent().trim()) + "'"
					+ ", " + getsinvoiceonhold()
					+ ", '" + clsDatabaseFunctions.FormatSQLStatement(getsVendorComments().trim()) + "'"
					+ ", " + sCreditDue
					+ ", NOW()"
					+ ", '" + clsDatabaseFunctions.FormatSQLStatement(sUserFullName) + "'"
					+ ", " + clsDatabaseFunctions.FormatSQLStatement(sUserID) + ""
					+ ")"
					;
		}else{
			SQL = " UPDATE " + SMTablevendorreturns.TableName + " SET ";
			SQL +=  " " + SMTablevendorreturns.itobereturned + " = " + getstobereturned()
			+ ", " + SMTablevendorreturns.icreditnotexpected + " = " + sCreditStatus
			+ ", " + SMTablevendorreturns.iponumber + " = " + sPONumber
			+ ", " + SMTablevendorreturns.svendoracct  + " = '" + clsDatabaseFunctions.FormatSQLStatement(getsvendoracct().trim()) + "'"
			+ ", " + SMTablevendorreturns.ladjustedbatchnumber  + " = " + clsDatabaseFunctions.FormatSQLStatement(getladjustedbatchnumber().trim()) + ""
			+ ", " + SMTablevendorreturns.ladjustedentrynumber  + " = " + clsDatabaseFunctions.FormatSQLStatement(getladjustedentrynumber().trim()) + ""
			+ ", " + SMTablevendorreturns.bdadjustmentamount  + " = " + clsDatabaseFunctions.FormatSQLStatement(getbdadjustmentamount().trim()) + ""
			+ ", " + SMTablevendorreturns.datcreditnotedate  + " = '" + clsDateAndTimeConversions.stdDateStringToSQLDateString(getdatcreditnotedate().trim()) + "'"
			+ ", " + SMTablevendorreturns.screditmemonumber  + " = '" + clsDatabaseFunctions.FormatSQLStatement(getscreditmemonumber().trim()) + "'"
			+ ", " + SMTablevendorreturns.bdcreditamt  + " = " + clsDatabaseFunctions.FormatSQLStatement(getbdcreditamt().trim()) + ""
			+ ", " + SMTablevendorreturns.datreturnsent  + " = '" + clsDateAndTimeConversions.stdDateStringToSQLDateString(getdatreturnsent().trim()) + "'"
			+ ", " + SMTablevendorreturns.iinvoiceonhold + " = " + getsinvoiceonhold()
			+ ", " + SMTablevendorreturns.mVendorComments  + " = '" + clsDatabaseFunctions.FormatSQLStatement(getsVendorComments().trim()) + "'"
			+ ", " + SMTablevendorreturns.iCreditDue + " = " + sCreditDue
			+ " WHERE ("
			+ "(" + SMTablevendorreturns.lid + " = " + getslid() + ")"
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
		SQL = "DELETE FROM " + SMTablevendorreturns.TableName
				+ " WHERE ("
				+ SMTablevendorreturns.lid + " = " + this.getslid()
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

		if (m_sponumber.compareToIgnoreCase("") != 0){
			if (m_sponumber.compareToIgnoreCase("0") != 0){
				ICPOHeader po = new ICPOHeader();
				po.setsID(m_sponumber);
				if (!po.load(conn)){
					sErrors += "Could not load PO number '" + m_sponumber + "' - " + po.getErrorMessages() + ".  ";
				}
			}
		}

		if((m_ladjustedbatchnumber.compareToIgnoreCase("0") == 0 &&  m_ladjustedentrynumber.compareToIgnoreCase("0") != 0) || (m_ladjustedbatchnumber.compareToIgnoreCase("0") != 0 &&  m_ladjustedentrynumber.compareToIgnoreCase("0") == 0)) {
			sErrors += "Could not load Batch #: '" + m_ladjustedbatchnumber + "', Entry #: " + m_ladjustedentrynumber + ".  ";
		}

		try {
			if(m_ladjustedbatchnumber.compareToIgnoreCase("0") != 0 &&  m_ladjustedentrynumber.compareToIgnoreCase("0") != 0) {
				ICEntry ent = new ICEntry(m_ladjustedbatchnumber,m_ladjustedentrynumber);
				System.out.println("[2019295133351] " + " Batch# " + m_ladjustedbatchnumber + " Entry #: " +m_ladjustedentrynumber );
				if(!ent.load(conn)) {
					sErrors += "Could not load Batch #: '" + m_ladjustedbatchnumber + "', Entry #: " + m_ladjustedentrynumber + " - " + ent.getErrorMessage() + ".  ";
				}
			}
		}catch( Exception e){
			throw new Exception( sErrors + " Could not load Batch #: '" + m_ladjustedbatchnumber + "', Entry #: " + m_ladjustedentrynumber + ".  ");
		}

		//Validate the vendor:

		if (m_svendoracct.compareToIgnoreCase("") != 0){
			if (m_svendoracct.compareToIgnoreCase("0") != 0){
				APVendor vendor = new APVendor();
				vendor.setsvendoracct(m_svendoracct);;
				if (!vendor.load(conn)){
					sErrors += "Could not load vendor '" + m_svendoracct + "' - " + vendor.getErrorMessages() + ".  ";
				}
			}
		}

		m_datcreditnotedate=m_datcreditnotedate.trim();
		if (m_datcreditnotedate.compareToIgnoreCase("") == 0){
			m_datcreditnotedate = EMPTY_DATE_STRING;
		}

		if (m_datcreditnotedate.compareToIgnoreCase(EMPTY_DATE_STRING) != 0){
			if (!clsDateAndTimeConversions.IsValidDateString(clsServletUtilities.DATE_FORMAT_FOR_DISPLAY, m_datcreditnotedate)){
				sErrors += "Date of Credit Memo is invalid: '" + m_datcreditnotedate + "'.  ";
			}
		}

		m_datreturnsent=m_datreturnsent.trim();
		if (m_datreturnsent.compareToIgnoreCase("") == 0){
			m_datreturnsent = EMPTY_DATE_STRING;
		}

		if (m_datreturnsent.compareToIgnoreCase(EMPTY_DATE_STRING) != 0){
			if (!clsDateAndTimeConversions.IsValidDateString(clsServletUtilities.DATE_FORMAT_FOR_DISPLAY, m_datreturnsent)){
				sErrors += "Date of Return Sent is invalid: '" + m_datreturnsent + "'.  ";
			}
		}


		if((m_datcreditnotedate.compareToIgnoreCase(EMPTY_DATE_STRING)!=0) && (m_screditmemonumber.compareToIgnoreCase("")==0 )) {
			sErrors += "Credit Memo Number needs to be filled out";
		}else if((m_datcreditnotedate.compareToIgnoreCase(EMPTY_DATE_STRING)==0) && (m_screditmemonumber.compareToIgnoreCase("")!=0 )) {
			sErrors += "Date of Credit Memo needs to be filled out";
		}


		if (
				(m_itobereturned.compareToIgnoreCase("0") != 0)
				&& (m_itobereturned.compareToIgnoreCase("1") != 0)
				){
			sErrors += "'To Be Returned' status (" + m_itobereturned + ") is invalid.";
		}

		if (
				(m_sinvoiceonhold.compareToIgnoreCase("0") != 0)
				&& (m_sinvoiceonhold.compareToIgnoreCase("1") != 0)
				){
			sErrors += "'AP Invoice on Hold' status (" + m_sinvoiceonhold + ") is invalid.";
		}
		
		if (
				(m_icreditdue.compareToIgnoreCase("0") != 0)
				&& (m_icreditdue.compareToIgnoreCase("1") != 0)
				){
			sErrors += "'Misc Credit Due' status (" + m_icreditdue + ") is invalid.";
		}

		m_bdcreditamt = m_bdcreditamt.replaceAll(",", "");
		m_bdadjustmentamount = m_bdadjustmentamount.replaceAll(",", "");

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
	public String getsNewRecord() {
		return m_sNewRecord;
	}
	public void setsNewRecord(String mSNewRecord) {
		m_sNewRecord = mSNewRecord;
	}
	public String getscreditnotexpected() {
		return m_screditnotexpected;
	}
	public void setscreditnotexpected(String screditnotexpected) {
		m_screditnotexpected = screditnotexpected;
	}
	public String getsponumber() {
		return m_sponumber;
	}
	public void setsponumber(String sponumber) {
		m_sponumber = sponumber;
	}
	public String getstobereturned() {
		return m_itobereturned;
	}
	public void setstobereturned(String sToBeReturned) {
		m_itobereturned = sToBeReturned;
	}
	public String getsvendoracct() {
		return m_svendoracct;
	}
	public void setsvendoracct(String sVendorAcct) {
		m_svendoracct = sVendorAcct;
	}
	public String getladjustedbatchnumber() {
		return m_ladjustedbatchnumber;
	}
	public void setladjustedbatchnumber(String ladjustedbatchnumber) {
		m_ladjustedbatchnumber = ladjustedbatchnumber;
	}
	public String getladjustedentrynumber() {
		return m_ladjustedentrynumber;
	}
	public void setladjustedentrynumber(String ladjustedentrynumber) {
		m_ladjustedentrynumber = ladjustedentrynumber;
	}
	public String getbdadjustmentamount() {
		return m_bdadjustmentamount;
	}
	public void setbdadjustmentamount(String bdadjustmentamount) {
		m_bdadjustmentamount = bdadjustmentamount;
	}
	public String getdatcreditnotedate() {
		return m_datcreditnotedate;
	}
	public void setdatcreditnotedate(String datcreditnotedate) {
		m_datcreditnotedate = datcreditnotedate;
	}
	public String getscreditmemonumber() {
		return m_screditmemonumber;
	}
	public void setscreditmemonumber(String screditmemonumber) {
		m_screditmemonumber = screditmemonumber;
	}
	public String getbdcreditamt() {
		return m_bdcreditamt;
	}
	public void setbdcreditamt(String bdcreditamt) {
		m_bdcreditamt = bdcreditamt;
	}
	public String getdatreturnsent() {
		return m_datreturnsent;
	}
	public void setdatreturnsent(String datreturnsent) {
		m_datreturnsent = datreturnsent;
	}
	public String getsinvoiceonhold() {
		return m_sinvoiceonhold;
	}
	public void setsinvoiceonhold(String sinvoiceonhold) {
		m_sinvoiceonhold = sinvoiceonhold;
	}
	public String getsVendorComments() {
		return m_svendorcomments;
	}
	public void setsVendorComments(String svendorcomments) {
		m_svendorcomments = svendorcomments;
	}
	public String getiCreditDue() {
		return m_icreditdue;
	}
	public void setiCreditDue(String iCreditDue) {
		m_icreditdue = iCreditDue;
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
		m_sNewRecord = "1";
		m_screditnotexpected = "0";
		m_sponumber = "0";
		m_itobereturned = "0";
		m_svendoracct = "";
		m_ladjustedbatchnumber ="0";
		m_ladjustedentrynumber ="0";
		m_bdadjustmentamount ="0.00";
		m_datcreditnotedate = EMPTY_DATE_STRING;
		m_screditmemonumber = "";
		m_bdcreditamt = "0.00";
		m_datreturnsent = EMPTY_DATE_STRING;
		m_sinvoiceonhold = "0";
		m_svendorcomments = "";
		m_icreditdue = "0";
		m_datinitiated = EMPTY_DATETIME_STRING;
		m_linitiatedbyid = "0";
		m_sinitiatedbyfullname = "";
	}
}
