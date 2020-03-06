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
import ServletUtilities.clsManageBigDecimals;
import ServletUtilities.clsManageRequestParameters;
import ServletUtilities.clsMasterEntry;
import ServletUtilities.clsServletUtilities;
import smap.APVendor;
import smcontrolpanel.SMMasterEditSelect;
import smic.ICEntry;
import smic.ICPOHeader;

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
	public static final String Paramicreditnotexpected = "icreditnotexpected";
	public static final String Paramiponumber = "iponumber";
	public static final String Paramitobereturned = "itobereturned";
	public static final String Paramsvendoracct = "svendoracct";
	public static final String Paramladjustedbatchnumber = "ladjustedbatchnumber";
	public static final String Paramladjustedentrynumber = "ladjustedentrynumber";
	public static final String Parambdadjustmentamount = "bdadjustmentamount";
	public static final String Paramdatcreditnotedate = "datcreditnotedate";
	public static final String Paramscreditmemonumber = "screditmemonumber";
	public static final String Parambdcreditamt = "bdcreditamt";
	public static final String Paramdatreturnsent = "datreturnsent";
	public static final String Paramiinvoiceonhold = "iinvoiceonhold";
	public static final String Parammvendorcomments = "mVendorComments";
	public static final String Paramicreditdue = "iCreditDue";


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
		m_screditnotexpected = clsManageRequestParameters.get_Request_Parameter(
				SMMaterialReturn.Paramicreditnotexpected, req).trim().replace("&quot;", "\"");
		m_sponumber = clsManageRequestParameters.get_Request_Parameter(
				SMMaterialReturn.Paramiponumber, req).trim().replace("&quot;", "\"");
		if(req.getParameter(SMMaterialReturn.Paramitobereturned) == null){
			m_itobereturned = "0";
		}else{
			m_itobereturned = "1";
		}
		m_svendoracct = clsManageRequestParameters.get_Request_Parameter(
				SMMaterialReturn.Paramsvendoracct, req).trim().replace("&quot;", "\"");
		m_ladjustedbatchnumber = clsManageRequestParameters.get_Request_Parameter(
				SMMaterialReturn.Paramladjustedbatchnumber, req).trim().replace("&quot;", "\"");
		if(clsManageRequestParameters.get_Request_Parameter(SMMaterialReturn.Paramladjustedbatchnumber, req).compareToIgnoreCase("") == 0){
			m_ladjustedbatchnumber = "0";
		}
		m_ladjustedentrynumber = clsManageRequestParameters.get_Request_Parameter(
				SMMaterialReturn.Paramladjustedentrynumber, req).trim().replace("&quot;", "\"");
		if(clsManageRequestParameters.get_Request_Parameter(SMMaterialReturn.Paramladjustedentrynumber, req).compareToIgnoreCase("") == 0){
			m_ladjustedentrynumber = "0";
		}
		m_bdadjustmentamount = clsManageRequestParameters.get_Request_Parameter(
				SMMaterialReturn.Parambdadjustmentamount, req).trim().replace("&quot;", "\"");
		if(clsManageRequestParameters.get_Request_Parameter(SMMaterialReturn.Parambdadjustmentamount, req).compareToIgnoreCase("") == 0){
			m_bdadjustmentamount = "0.00";
		}
		m_datcreditnotedate = clsManageRequestParameters.get_Request_Parameter(
				SMMaterialReturn.Paramdatcreditnotedate, req).trim().replace("&quot;", "\"");
		if(clsManageRequestParameters.get_Request_Parameter(SMMaterialReturn.Paramdatcreditnotedate, req).compareToIgnoreCase("") == 0){
			m_datcreditnotedate = clsServletUtilities.EMPTY_DATE_VALUE;
		}
		m_screditmemonumber = clsManageRequestParameters.get_Request_Parameter(
				SMMaterialReturn.Paramscreditmemonumber, req).trim().replace("&quot;", "\"");

		m_bdcreditamt = clsManageRequestParameters.get_Request_Parameter(
				SMMaterialReturn.Parambdcreditamt, req).trim().replace("&quot;", "\"");
		if(clsManageRequestParameters.get_Request_Parameter(SMMaterialReturn.Parambdcreditamt, req).compareToIgnoreCase("") == 0){
			m_bdcreditamt = "0.00";
		}

		m_datreturnsent = clsManageRequestParameters.get_Request_Parameter(
				SMMaterialReturn.Paramdatreturnsent, req).trim().replace("&quot;", "\"");
		if(clsManageRequestParameters.get_Request_Parameter(SMMaterialReturn.Paramdatreturnsent, req).compareToIgnoreCase("") == 0){
			m_datreturnsent = clsServletUtilities.EMPTY_DATE_VALUE;
		}
		if(req.getParameter(SMMaterialReturn.Paramiinvoiceonhold) == null){
			m_sinvoiceonhold = "0";
		}else{
			m_sinvoiceonhold = "1";
		}
		
		m_svendorcomments = clsManageRequestParameters.get_Request_Parameter(
				SMMaterialReturn.Parammvendorcomments, req).trim().replace("&quot;", "\""); 
		
		if(req.getParameter(SMMaterialReturn.Paramicreditdue) == null){
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
				m_screditnotexpected = Long.toString(rs.getLong(SMTablematerialreturns.icreditnotexpected));
				m_sponumber = Long.toString(rs.getLong(SMTablematerialreturns.iponumber));
				if (Long.parseLong(m_sponumber) < 1){
					m_sponumber = "";
				}
				m_strimmedordernumber = rs.getString(SMTablematerialreturns.strimmedordernumber).trim();
				m_itobereturned = Long.toString(rs.getLong(SMTablematerialreturns.itobereturned));
				m_svendoracct = rs.getString(SMTablematerialreturns.svendoracct).trim();

				m_ladjustedbatchnumber = Long.toString(rs.getLong(SMTablematerialreturns.ladjustedbatchnumber));
				if(m_ladjustedbatchnumber.compareToIgnoreCase("")== 0) {
					m_ladjustedbatchnumber = "0";
				}
				m_ladjustedentrynumber = Long.toString(rs.getLong(SMTablematerialreturns.ladjustedentrynumber));
				if(m_ladjustedentrynumber.compareToIgnoreCase("")== 0) {
					m_ladjustedentrynumber = "0";
				}
				m_bdadjustmentamount = clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(rs.getBigDecimal(SMTablematerialreturns.bdadjustmentamount));
				m_datcreditnotedate = clsDateAndTimeConversions.resultsetDateStringToString(rs.getString(SMTablematerialreturns.datcreditnotedate));
				m_screditmemonumber = rs.getString(SMTablematerialreturns.screditmemonumber).trim();
				m_bdcreditamt = clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(rs.getBigDecimal(SMTablematerialreturns.bdcreditamt));
				m_datreturnsent = clsDateAndTimeConversions.resultsetDateStringToString(rs.getString(SMTablematerialreturns.datreturnsent));
				m_sinvoiceonhold = Long.toString(rs.getLong(SMTablematerialreturns.iinvoiceonhold));
				m_svendorcomments = rs.getString(SMTablematerialreturns.mVendorComments).trim();
				m_icreditdue = Integer.toString(rs.getInt(SMTablematerialreturns.iCreditDue));
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
			SQL = "INSERT INTO " + SMTablematerialreturns.TableName + " ("
					+ SMTablematerialreturns.datinitiated
					+ ", " + SMTablematerialreturns.linitiatedbyid
					+ ", " + SMTablematerialreturns.sinitiatedbyfullname
					+ ", " + SMTablematerialreturns.datresolved
					+ ", " + SMTablematerialreturns.iresolved
					+ ", " + SMTablematerialreturns.itobereturned
					+ ", " + SMTablematerialreturns.lresolvedbyid
					+ ", " + SMTablematerialreturns.sresolvedbyfullname
					+ ", " + SMTablematerialreturns.mresolutioncomments
					+ ", " + SMTablematerialreturns.mcomments
					+ ", " + SMTablematerialreturns.sdescription
					+ ", " + SMTablematerialreturns.iworkorderid
					+ ", " + SMTablematerialreturns.strimmedordernumber
					+ ", " + SMTablematerialreturns.icreditnotexpected
					+ ", " + SMTablematerialreturns.iponumber
					+ ", " + SMTablematerialreturns.svendoracct
					+ ", " + SMTablematerialreturns.ladjustedbatchnumber
					+ ", " + SMTablematerialreturns.ladjustedentrynumber
					+ ", " + SMTablematerialreturns.bdadjustmentamount
					+ ", " + SMTablematerialreturns.datcreditnotedate
					+ ", " + SMTablematerialreturns.screditmemonumber
					+ ", " + SMTablematerialreturns.bdcreditamt
					+ ", " + SMTablematerialreturns.datreturnsent
					+ ", " + SMTablematerialreturns.iinvoiceonhold
					+ ", " + SMTablematerialreturns.mVendorComments
					+ ", " + SMTablematerialreturns.iCreditDue
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
			+ ", " + getstobereturned()
			+ ", " + clsDatabaseFunctions.FormatSQLStatement(getlresolvedbyid()) + ""
			+ ", '" + clsDatabaseFunctions.FormatSQLStatement(getsresolvedbyfullname().trim()) + "'"
			+ ", '" + clsDatabaseFunctions.FormatSQLStatement(getsresolutioncomments().trim()) + "'"
			+ ", '" + clsDatabaseFunctions.FormatSQLStatement(getscomments().trim()) + "'"
			+ ", '" + clsDatabaseFunctions.FormatSQLStatement(getsdescription().trim()) + "'"
			+ ", " + sWorkOrderID
			+ ", '" + clsDatabaseFunctions.FormatSQLStatement(getstrimmedordernumber().trim()) + "'"
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
			+ ", " + SMTablematerialreturns.itobereturned + " = " + getstobereturned()
			+ ", " + SMTablematerialreturns.mcomments  + " = '" + clsDatabaseFunctions.FormatSQLStatement(getscomments().trim()) + "'"
			+ ", " + SMTablematerialreturns.sdescription  + " = '" + clsDatabaseFunctions.FormatSQLStatement(getsdescription().trim()) + "'"
			//+ ", " + SMTablematerialreturns.mresolutioncomments  + " = '" + clsDatabaseFunctions.FormatSQLStatement(getsresolutioncomments().trim()) + "'"
			+ ", " + SMTablematerialreturns.lresolvedbyid  + " = " + clsDatabaseFunctions.FormatSQLStatement(getlresolvedbyid().trim()) + ""
			+ ", " + SMTablematerialreturns.sresolvedbyfullname  + " = '" + clsDatabaseFunctions.FormatSQLStatement(getsresolvedbyfullname().trim()) + "'"
			+ ", " + SMTablematerialreturns.iworkorderid + " = " + sWorkOrderID
			+ ", " + SMTablematerialreturns.strimmedordernumber  + " = '" + clsDatabaseFunctions.FormatSQLStatement(getstrimmedordernumber().trim()) + "'"
			+ ", " + SMTablematerialreturns.icreditnotexpected + " = " + sCreditStatus
			+ ", " + SMTablematerialreturns.iponumber + " = " + sPONumber
			+ ", " + SMTablematerialreturns.svendoracct  + " = '" + clsDatabaseFunctions.FormatSQLStatement(getsvendoracct().trim()) + "'"
			+ ", " + SMTablematerialreturns.ladjustedbatchnumber  + " = " + clsDatabaseFunctions.FormatSQLStatement(getladjustedbatchnumber().trim()) + ""
			+ ", " + SMTablematerialreturns.ladjustedentrynumber  + " = " + clsDatabaseFunctions.FormatSQLStatement(getladjustedentrynumber().trim()) + ""
			+ ", " + SMTablematerialreturns.bdadjustmentamount  + " = " + clsDatabaseFunctions.FormatSQLStatement(getbdadjustmentamount().trim()) + ""
			+ ", " + SMTablematerialreturns.datcreditnotedate  + " = '" + clsDateAndTimeConversions.stdDateStringToSQLDateString(getdatcreditnotedate().trim()) + "'"
			+ ", " + SMTablematerialreturns.screditmemonumber  + " = '" + clsDatabaseFunctions.FormatSQLStatement(getscreditmemonumber().trim()) + "'"
			+ ", " + SMTablematerialreturns.bdcreditamt  + " = " + clsDatabaseFunctions.FormatSQLStatement(getbdcreditamt().trim()) + ""
			+ ", " + SMTablematerialreturns.datreturnsent  + " = '" + clsDateAndTimeConversions.stdDateStringToSQLDateString(getdatreturnsent().trim()) + "'"
			+ ", " + SMTablematerialreturns.iinvoiceonhold + " = " + getsinvoiceonhold()
			+ ", " + SMTablematerialreturns.mVendorComments  + " = '" + clsDatabaseFunctions.FormatSQLStatement(getsVendorComments().trim()) + "'"
			+ ", " + SMTablematerialreturns.iCreditDue + " = " + sCreditDue
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
	}
}
