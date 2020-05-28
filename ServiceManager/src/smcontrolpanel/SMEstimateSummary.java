package smcontrolpanel;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import SMDataDefinition.SMTableorderheaders;
import SMDataDefinition.SMTablesmestimates;
import SMDataDefinition.SMTablesmestimatesummaries;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsDateAndTimeConversions;
import ServletUtilities.clsManageBigDecimals;
import ServletUtilities.clsManageRequestParameters;
import ServletUtilities.clsServletUtilities;
import ServletUtilities.clsValidateFormFields;

public class SMEstimateSummary {

	public static final String OBJECT_NAME = "Estimate Summary";
	
	private String m_lid;
	private String m_sjobname;
	private String m_lsalesleadid;
	private String m_itaxid;
	private String m_ilabortype;
	private String m_iordertype;
	private String m_lcreatedbyid;
	private String m_datetimecreated;
	private String m_screatedbyfullname;
	private String m_llastmodifiedbyid;
	private String m_datetimelastmodified;
	private String m_slastmodifiedbyfullname;
	private String m_sdescription;
	private String m_sremarks;
	private String m_bdadjustedfreight;
	private String m_bdadjustedlaborunitqty;
	private String m_bdadjustedlaborcostperunit;
	private String m_bdadjustedlmarkupamt;
	
	private ArrayList<SMEstimate>arrEstimates;

	public static final int LINE_NUMBER_PADDING_LENGTH = 6;
	public static final String LINE_NUMBER_PARAMETER = "LINENOPARAM";

	public SMEstimateSummary() 
	{
		initializeVariables();
	}
	public SMEstimateSummary(HttpServletRequest req){
		//Read the batch fields from a servlet request:
		initializeVariables();
		
		m_lid = clsManageRequestParameters.get_Request_Parameter(SMTablesmestimatesummaries.lid, req).replace("&quot;", "\"");
		if(m_lid.compareToIgnoreCase("") == 0){
			m_lid = "-1";
		}
		m_sjobname = clsManageRequestParameters.get_Request_Parameter(SMTablesmestimatesummaries.sjobname, req).replace("&quot;", "\"");
		m_lsalesleadid = clsManageRequestParameters.get_Request_Parameter(SMTablesmestimatesummaries.lsalesleadid, req).replace("&quot;", "\"");
		if(m_lsalesleadid.compareToIgnoreCase("") == 0){
			m_lsalesleadid = "0";
		}
		m_itaxid = clsManageRequestParameters.get_Request_Parameter(SMTablesmestimatesummaries.itaxid, req).replace("&quot;", "\"");
		if(m_itaxid.compareToIgnoreCase("") == 0){
			m_itaxid = "0";
		}
		m_ilabortype = clsManageRequestParameters.get_Request_Parameter(SMTablesmestimatesummaries.ilabortype, req).replace("&quot;", "\"");
		if(m_ilabortype.compareToIgnoreCase("") == 0){
			m_ilabortype = "0";
		}
		m_iordertype = clsManageRequestParameters.get_Request_Parameter(SMTablesmestimatesummaries.iordertype, req).replace("&quot;", "\"");
		if(m_iordertype.compareToIgnoreCase("") == 0){
			m_iordertype = "0";
		}
		m_lcreatedbyid = clsManageRequestParameters.get_Request_Parameter(SMTablesmestimatesummaries.lcreatedbyid, req).replace("&quot;", "\"");
		if(m_lcreatedbyid.compareToIgnoreCase("") == 0){
			m_lcreatedbyid = "0";
		}
		m_datetimecreated = clsManageRequestParameters.get_Request_Parameter(SMTablesmestimatesummaries.datetimecreated, req).replace("&quot;", "\"");
		if(m_datetimecreated.compareToIgnoreCase("") == 0){
			m_datetimecreated = SMUtilities.EMPTY_DATETIME_VALUE;
		}
		m_screatedbyfullname = clsManageRequestParameters.get_Request_Parameter(SMTablesmestimatesummaries.screatedbyfullname, req).replace("&quot;", "\"");
		m_llastmodifiedbyid = clsManageRequestParameters.get_Request_Parameter(SMTablesmestimatesummaries.llastmodifiedbyid, req).replace("&quot;", "\"");
		if(m_llastmodifiedbyid.compareToIgnoreCase("") == 0){
			m_llastmodifiedbyid = "0";
		}
		m_datetimelastmodified = clsManageRequestParameters.get_Request_Parameter(SMTablesmestimatesummaries.datetimelastmodified, req).replace("&quot;", "\"");
		if(m_datetimelastmodified.compareToIgnoreCase("") == 0){
			m_datetimelastmodified = SMUtilities.EMPTY_DATETIME_VALUE;
		}
		m_slastmodifiedbyfullname = clsManageRequestParameters.get_Request_Parameter(SMTablesmestimatesummaries.slastmodifiedbyfullname, req).replace("&quot;", "\"");
		m_sdescription = clsManageRequestParameters.get_Request_Parameter(SMTablesmestimatesummaries.sdescription, req).replace("&quot;", "\"");
		m_sremarks = clsManageRequestParameters.get_Request_Parameter(SMTablesmestimatesummaries.sremarks, req).replace("&quot;", "\"");
		m_bdadjustedfreight = clsManageRequestParameters.get_Request_Parameter(SMTablesmestimatesummaries.bdadjustedfreight, req).replace("&quot;", "\"");
		m_bdadjustedlaborunitqty = clsManageRequestParameters.get_Request_Parameter(SMTablesmestimatesummaries.bdadjustedlaborunitqty, req).replace("&quot;", "\"");
		m_bdadjustedlaborcostperunit = clsManageRequestParameters.get_Request_Parameter(SMTablesmestimatesummaries.bdadjustedlaborcostperunit, req).replace("&quot;", "\"");
		m_bdadjustedlmarkupamt = clsManageRequestParameters.get_Request_Parameter(SMTablesmestimatesummaries.bdadjustedlmarkupamt, req).replace("&quot;", "\"");
		
	}

	public void save_without_data_transaction (Connection conn, String sUserID, String sUserFullName) throws Exception{

		//long lStarttime = System.currentTimeMillis();
		
		try {
			validate_fields(conn, sUserID);
		} catch (Exception e1) {
			throw new Exception(e1.getMessage());
		}
		
		//System.out.println("[1590502610] - elapsed time 10 = " + (System.currentTimeMillis() - lStarttime) + " ms");
		
		String SQL = "";
		SQL = "INSERT into " + SMTablesmestimatesummaries.TableName
			+ " (" 
			+ SMTablesmestimatesummaries.bdadjustedfreight
			+ ", " + SMTablesmestimatesummaries.bdadjustedlaborcostperunit
			+ ", " + SMTablesmestimatesummaries.bdadjustedlaborunitqty
			+ ", " + SMTablesmestimatesummaries.bdadjustedlmarkupamt
			+ ", " + SMTablesmestimatesummaries.datetimecreated
			+ ", " + SMTablesmestimatesummaries.datetimelastmodified
			+ ", " + SMTablesmestimatesummaries.ilabortype
			+ ", " + SMTablesmestimatesummaries.iordertype
			+ ", " + SMTablesmestimatesummaries.itaxid
			+ ", " + SMTablesmestimatesummaries.lcreatedbyid
			+ ", " + SMTablesmestimatesummaries.llastmodifiedbyid
			+ ", " + SMTablesmestimatesummaries.lsalesleadid
			+ ", " + SMTablesmestimatesummaries.screatedbyfullname
			+ ", " + SMTablesmestimatesummaries.sdescription
			+ ", " + SMTablesmestimatesummaries.sjobname
			+ ", " + SMTablesmestimatesummaries.slastmodifiedbyfullname
			+ ", " + SMTablesmestimatesummaries.sremarks
			+ ")"
			+ " VALUES ("
			+ m_bdadjustedfreight.replace(",", "")
			+ ", " + m_bdadjustedlaborcostperunit.replace(",", "")
			+ ", " + m_bdadjustedlaborunitqty.replace(",", "")
			+ ", " + m_bdadjustedlmarkupamt.replace(",", "")
			+ ", NOW()"
			+ ", NOW()"
			+ ", " + m_ilabortype
			+ ", " + m_iordertype
			+ ", " + m_itaxid
			+ ", " + sUserID
			+ ", " + sUserID
			+ ", " + m_lsalesleadid
			+ ", '" + sUserFullName + "'"
			+ ", '" + m_sdescription + "'"
			+ ", '" + m_sjobname + "'"
			+ ", '" + sUserFullName + "'"
			+ ", '" + m_sremarks + "'"
			+ ")"
					
			+ " ON DUPLICATE KEY UPDATE"
			+ " " + SMTablesmestimatesummaries.bdadjustedfreight + " = " + m_bdadjustedfreight.replace(",", "")
			+ ", " + SMTablesmestimatesummaries.bdadjustedlaborcostperunit + " = " + m_bdadjustedlaborcostperunit.replace(",", "")
			+ ", " + SMTablesmestimatesummaries.bdadjustedlaborunitqty + " = " + m_bdadjustedlaborunitqty.replace(",", "")
			+ ", " + SMTablesmestimatesummaries.bdadjustedlmarkupamt + " = " + m_bdadjustedlmarkupamt.replace(",", "")
			+ ", " + SMTablesmestimatesummaries.datetimelastmodified + " = NOW()"
			+ ", " + SMTablesmestimatesummaries.ilabortype + " = " + m_ilabortype
			+ ", " + SMTablesmestimatesummaries.iordertype + " = " + m_iordertype
			+ ", " + SMTablesmestimatesummaries.itaxid + " = " + m_itaxid
			+ ", " + SMTablesmestimatesummaries.llastmodifiedbyid + " = " + m_llastmodifiedbyid
			+ ", " + SMTablesmestimatesummaries.lsalesleadid + " = " + m_lsalesleadid
			+ ", " + SMTablesmestimatesummaries.sdescription + " = '" + clsDatabaseFunctions.FormatSQLStatement(m_sdescription) + "'"
			+ ", " + SMTablesmestimatesummaries.sjobname + " = '" + clsDatabaseFunctions.FormatSQLStatement(m_sjobname) + "'"
			+ ", " + SMTablesmestimatesummaries.slastmodifiedbyfullname + " = '" + clsDatabaseFunctions.FormatSQLStatement(m_slastmodifiedbyfullname) + "'"
			+ ", " + SMTablesmestimatesummaries.sremarks + " = '" + clsDatabaseFunctions.FormatSQLStatement(m_sremarks) + "'"
		;
		
		//System.out.println("[159050375] - SQL = '" + SQL + "'");
		
		Statement stmt = conn.createStatement();
		try {
			stmt.execute(SQL);
		} catch (Exception e) {
			throw new Exception("Error [1590503757] updating Estimate Summary with SQL: '" + SQL + "' - " + e.getMessage());
		}
		
		//Update the ID:
		String sSQL = "SELECT last_insert_id()";
		try {
			ResultSet rs = clsDatabaseFunctions.openResultSet(sSQL, conn);
			if (rs.next()) {
				m_lid = Long.toString(rs.getLong(1));
			}else {
				m_lid = "0";
			}
			rs.close();
		} catch (SQLException e) {
			throw new Exception("Error [1590503758] Could not get last ID number - " + e.getMessage());
		}
		//If something went wrong, we can't get the last ID:
		if (m_lid.compareToIgnoreCase("0") == 0){
			throw new Exception("Error [1590503759] Could not get last ID number.");
		}
		
		//Finally, save the entries....
		try {
			saveEstimates(conn, sUserID, sUserFullName);
		} catch (Exception e) {
			throw new Exception("Error [1590503760] saving entry lines - " + e.getMessage() + ".");
		}
		
		return;
	}
	
	public void validate_fields(Connection conn, String sUserID) throws Exception{
		
		String sResult = "";
		
		try {
			m_lid  = clsValidateFormFields.validateLongIntegerField(m_lid, "Estimate Summary ID", -1L, clsValidateFormFields.MAX_LONG_VALUE);
		} catch (Exception e) {
			sResult += "  " + e.getMessage() + ".";
		}
		
		try {
			m_sjobname = clsValidateFormFields.validateStringField(
				m_sjobname, 
				SMTablesmestimatesummaries.sjobnameLength, 
				"Job Name", 
				true
			);
		} catch (Exception e) {
			sResult += "  " + e.getMessage() + ".";
		}
		
		try {
			m_lsalesleadid  = clsValidateFormFields.validateLongIntegerField(m_lsalesleadid, "Sales Lead ID", 0L, clsValidateFormFields.MAX_LONG_VALUE);
		} catch (Exception e) {
			sResult += "  " + e.getMessage() + ".";
		}

		try {
			m_itaxid  = clsValidateFormFields.validateLongIntegerField(m_itaxid, "Tax ID", 1L, clsValidateFormFields.MAX_INT_VALUE);
		} catch (Exception e) {
			sResult += "  " + e.getMessage() + ".";
		}

		try {
			m_ilabortype  = clsValidateFormFields.validateLongIntegerField(m_ilabortype, "Labor type", 1L, clsValidateFormFields.MAX_INT_VALUE);
		} catch (Exception e) {
			sResult += "  " + e.getMessage() + ".";
		}
		
		if (
			(m_iordertype.compareToIgnoreCase(Integer.toString(SMTableorderheaders.ORDERTYPE_ACTIVE)) !=0)
			&& (m_iordertype.compareToIgnoreCase(Integer.toString(SMTableorderheaders.ORDERTYPE_FUTURE)) !=0)
			&& (m_iordertype.compareToIgnoreCase(Integer.toString(SMTableorderheaders.ORDERTYPE_QUOTE)) !=0)
			&& (m_iordertype.compareToIgnoreCase(Integer.toString(SMTableorderheaders.ORDERTYPE_STANDING)) !=0)
		) {
			sResult += "  Order type '" + m_iordertype + "' is not valid.";
		}
		
		long lMinimumCreatedByID = 1L;
		if ( (m_lid.compareToIgnoreCase("0") == 0) || (m_lid.compareToIgnoreCase("-1") == 0) ){
			lMinimumCreatedByID = 0L;
		}
		try {
			m_lcreatedbyid  = clsValidateFormFields.validateLongIntegerField(m_lcreatedbyid, "Created By ID", lMinimumCreatedByID, clsValidateFormFields.MAX_LONG_VALUE);
		} catch (Exception e) {
			sResult += "  " + e.getMessage() + ".";
		}
		
		try {
			m_datetimecreated = clsValidateFormFields.validateDateTimeField(
				m_datetimecreated, 
				"Date created", 
				SMUtilities.DATETIME_FORMAT_FOR_DISPLAY,
				true);
		} catch (Exception e) {
			sResult += "  " + e.getMessage() + ".";
		}
		
		try {
			m_screatedbyfullname = clsValidateFormFields.validateStringField(
				m_screatedbyfullname, 
				SMTablesmestimatesummaries.sscreatedbyfullnameLength, 
				"Created by full name", 
				true
			);
		} catch (Exception e) {
			sResult += "  " + e.getMessage() + ".";
		}
		
		try {
			m_llastmodifiedbyid  = clsValidateFormFields.validateLongIntegerField(m_llastmodifiedbyid, "Last modified By ID", 0L, clsValidateFormFields.MAX_LONG_VALUE);
		} catch (Exception e) {
			sResult += "  " + e.getMessage() + ".";
		}
		
		try {
			m_datetimelastmodified = clsValidateFormFields.validateDateTimeField(
				m_datetimelastmodified, 
				"Date last modified", 
				SMUtilities.DATETIME_FORMAT_FOR_DISPLAY,
				true);
		} catch (Exception e) {
			sResult += "  " + e.getMessage() + ".";
		}
		
		try {
			m_screatedbyfullname = clsValidateFormFields.validateStringField(
				m_screatedbyfullname, 
				SMTablesmestimatesummaries.sscreatedbyfullnameLength, 
				"Created by full name", 
				true
			);
		} catch (Exception e) {
			sResult += "  " + e.getMessage() + ".";
		}
		
		try {
			m_slastmodifiedbyfullname = clsValidateFormFields.validateStringField(
				m_slastmodifiedbyfullname, 
				SMTablesmestimatesummaries.slastmodifiedbyfullnameLength, 
				"Last modified by full name", 
				true
			);
		} catch (Exception e) {
			sResult += "  " + e.getMessage() + ".";
		}
		
		try {
			m_sdescription = clsValidateFormFields.validateStringField(
				m_sdescription, 
				SMTablesmestimatesummaries.slastmodifiedbyfullnameLength, 
				"Description", 
				false
			);
		} catch (Exception e) {
			sResult += "  " + e.getMessage() + ".";
		}

		try {
			m_sremarks = clsValidateFormFields.validateStringField(
				m_sremarks, 
				SMTablesmestimatesummaries.sremarksLength, 
				"Remarks", 
				false
			);
		} catch (Exception e) {
			sResult += "  " + e.getMessage() + ".";
		}

		try {
			m_bdadjustedfreight = clsValidateFormFields.validateBigdecimalField(
				m_bdadjustedfreight, 
				"Adjusted freight", 
				SMTablesmestimatesummaries.bdadjustedfreightScale, 
				new BigDecimal("0.01"), 
				new BigDecimal("999999999.99")
			);
		} catch (Exception e) {
			sResult += "  " + e.getMessage() + ".";
		}
		
		try {
			m_bdadjustedlaborunitqty = clsValidateFormFields.validateBigdecimalField(
				m_bdadjustedlaborunitqty, 
				"Adjusted labor unit qty", 
				SMTablesmestimatesummaries.bdadjustedlaborunitqtyScale, 
				new BigDecimal("0.0001"), 
				new BigDecimal("999999999.9999")
			);
		} catch (Exception e) {
			sResult += "  " + e.getMessage() + ".";
		}
		
		try {
			m_bdadjustedlaborcostperunit = clsValidateFormFields.validateBigdecimalField(
				m_bdadjustedlaborcostperunit, 
				"Adjusted labor cost per unit", 
				SMTablesmestimatesummaries.bdadjustedlaborcostperunitScale, 
				new BigDecimal("0.01"), 
				new BigDecimal("999999999.99")
			);
		} catch (Exception e) {
			sResult += "  " + e.getMessage() + ".";
		}
		
		try {
			m_bdadjustedlmarkupamt = clsValidateFormFields.validateBigdecimalField(
				m_bdadjustedlmarkupamt, 
				"Adjusted mark-up amount", 
				SMTablesmestimatesummaries.bdadjustedlmarkupamtScale, 
				new BigDecimal("0.01"), 
				new BigDecimal("999999999.99")
			);
		} catch (Exception e) {
			sResult += "  " + e.getMessage() + ".";
		}
		
		//Validate the entries:
		for (int i = 0; i < arrEstimates.size(); i++){
			SMEstimate estimate = arrEstimates.get(i);
			estimate.setslsummarylid(m_lid);
			estimate.setslsummarylinenumber((Integer.toString(i + 1)));
			
			try {
				estimate.validate_fields(conn, sUserID);
			} catch (Exception e) {
				sResult += "  In estimate " + estimate.getslid() + " - " + e.getMessage() + ".";
			}
		}
		
		if (sResult.compareToIgnoreCase("") != 0){
			throw new Exception(sResult);
		}
		
		return;
	}
	
	public void load(ServletContext context, String sDBID, String sUserID) throws Exception{

		Connection conn;
		try { conn =
				clsDatabaseFunctions.getConnectionWithException( context, sDBID, "MySQL",
						SMUtilities.getFullClassName(this.toString()) + ".load - user: " + sUserID);
		} catch (Exception e) { throw new
			Exception("Error [1590507091] getting connection - " + e.getMessage()); }

		try { 
			load(conn); 
		} catch (Exception e){ 
			clsDatabaseFunctions.freeConnection(context, conn, "[1590167716]");
			throw new Exception("Error [1590507092] loading - " + e.getMessage()); 
		}
		
		clsDatabaseFunctions.freeConnection(context, conn, "[1590507093]");
	}
	
	public void load(Connection conn) throws Exception{
		
		String SQL = "";
		if (
			(m_lid.compareToIgnoreCase("") != 0)
			&& (m_lid.compareToIgnoreCase("-1") != 0)
			&& (m_lid.compareToIgnoreCase("0") != 0)	
		){
			SQL = "SELECT * FROM " + SMTablesmestimatesummaries.TableName
				+ " WHERE ("
					+ "(" + SMTablesmestimatesummaries.lid + " = " + m_lid + ")"
				+ ")"
			;
		}else{
			throw new Exception("Error [1590168080] - can't load Estimate Summary without an ID.");
		}
		
		ResultSet rs = null;
		try {
			rs = clsDatabaseFunctions.openResultSet(SQL, conn);
			if (rs.next()){
				m_lid = (Long.toString(rs.getLong(SMTablesmestimatesummaries.lid)));
				m_sjobname = rs.getString(SMTablesmestimatesummaries.sjobname);
				m_lsalesleadid = (Long.toString(rs.getLong(SMTablesmestimatesummaries.lsalesleadid)));
				m_itaxid = (Integer.toString(rs.getInt(SMTablesmestimatesummaries.itaxid)));
				m_ilabortype = (Integer.toString(rs.getInt(SMTablesmestimatesummaries.ilabortype)));
				m_iordertype = (Integer.toString(rs.getInt(SMTablesmestimatesummaries.iordertype)));
				m_lcreatedbyid = (Long.toString(rs.getLong(SMTablesmestimatesummaries.lcreatedbyid)));
				m_datetimecreated = clsDateAndTimeConversions.resultsetDateTimeStringToFormattedString(
					rs.getString(SMTablesmestimatesummaries.datetimecreated), SMUtilities.DATETIME_FORMAT_FOR_DISPLAY, SMUtilities.EMPTY_DATETIME_VALUE);
				m_screatedbyfullname = rs.getString(SMTablesmestimatesummaries.screatedbyfullname);
				m_llastmodifiedbyid = (Long.toString(rs.getLong(SMTablesmestimatesummaries.llastmodifiedbyid)));
				m_datetimelastmodified = clsDateAndTimeConversions.resultsetDateTimeStringToFormattedString(
					rs.getString(SMTablesmestimatesummaries.datetimelastmodified), SMUtilities.DATETIME_FORMAT_FOR_DISPLAY, SMUtilities.EMPTY_DATETIME_VALUE);
				m_slastmodifiedbyfullname = rs.getString(SMTablesmestimatesummaries.slastmodifiedbyfullname);
				m_sdescription = rs.getString(SMTablesmestimatesummaries.sdescription);
				m_sremarks = rs.getString(SMTablesmestimatesummaries.sremarks);
				m_bdadjustedfreight = clsManageBigDecimals.BigDecimalToScaledFormattedString(SMTablesmestimatesummaries.bdadjustedfreightScale, rs.getBigDecimal(SMTablesmestimatesummaries.bdadjustedfreight));
				m_bdadjustedlaborunitqty = clsManageBigDecimals.BigDecimalToScaledFormattedString(SMTablesmestimatesummaries.bdadjustedlaborunitqtyScale, rs.getBigDecimal(SMTablesmestimatesummaries.bdadjustedlaborunitqty));
				m_bdadjustedlaborcostperunit = clsManageBigDecimals.BigDecimalToScaledFormattedString(SMTablesmestimatesummaries.bdadjustedlaborcostperunitScale, rs.getBigDecimal(SMTablesmestimatesummaries.bdadjustedlaborcostperunit));
				m_bdadjustedlmarkupamt = clsManageBigDecimals.BigDecimalToScaledFormattedString(SMTablesmestimatesummaries.bdadjustedlmarkupamtScale, rs.getBigDecimal(SMTablesmestimatesummaries.bdadjustedlmarkupamt));
				}else{
				rs.close();
				throw new Exception("Error [1590509859] - No Estimate Summary found with lid = " + m_lid + ".");
			}
			rs.close();
		} catch (Exception e) {
			rs.close();
			throw new Exception("Error [1590509860] - loading " + OBJECT_NAME + " with ID " + m_lid + " - " + e.getMessage());
		}
		
		//Load the lines:
		arrEstimates.clear();
		SQL = "SELECT"
			+ " " + SMTablesmestimates.lid
			+ " FROM " + SMTablesmestimates.TableName 
			+ " WHERE ("
				+ "(" + SMTablesmestimates.lsummarylid + " = " + m_lid + ")"
			+ ") ORDER BY " + SMTablesmestimates.lsummarylinenumber
		;
		rs = null;
		try {
			rs = clsDatabaseFunctions.openResultSet(SQL, conn);
			while (rs.next()){
				SMEstimate estimate = new SMEstimate();
				estimate.setslid(Long.toString(rs.getLong(SMTablesmestimates.lid)));
				estimate.load(conn);
				addEstimate(estimate);
			}
			rs.close();
		} catch (Exception e) {
			rs.close();
			throw new Exception("Error [1590510109] loading Estimates - " + e.getMessage());
		}
	}
	
	public String getsdatetimecreatedInSQLFormat() throws Exception{
		if (m_datetimecreated.compareToIgnoreCase("") == 0){
			return SMUtilities.EMPTY_DATETIME_VALUE;
		}else{
			return clsDateAndTimeConversions.convertDateFormat(m_datetimecreated, SMUtilities.DATETIME_FORMAT_FOR_DISPLAY, SMUtilities.DATETIME_FORMAT_FOR_SQL, SMUtilities.EMPTY_SQL_DATETIME_VALUE);
		}
	}
	public String getsdatetimelastmodifiedbyInSQLFormat() throws Exception{
		if (m_datetimelastmodified.compareToIgnoreCase("") == 0){
			return SMUtilities.EMPTY_DATETIME_VALUE;
		}else{
			return clsDateAndTimeConversions.convertDateFormat(m_datetimelastmodified, SMUtilities.DATETIME_FORMAT_FOR_DISPLAY, SMUtilities.DATETIME_FORMAT_FOR_SQL, SMUtilities.EMPTY_SQL_DATETIME_VALUE);
		}
	}
	
	public String getslid(){
		return m_lid;
	}
	public void setslid(String slid){
		m_lid = slid;
	}

	public String getsjobname(){
		return m_sjobname;
	}
	public void setsjobname(String sjobname){
		m_sjobname = sjobname;
	}
	
	public String getslsalesleadid(){
		return m_lsalesleadid;
	}
	public void setssalesleadid(String ssalesleadid){
		m_lsalesleadid = ssalesleadid;
	}
	
	public String getsitaxid(){
		return m_itaxid;
	}
	public void setsitaxid(String sitaxid){
		m_itaxid = sitaxid;
	}
	
	public String getsilabortype(){
		return m_ilabortype;
	}
	public void setsilabortype(String silabortype){
		m_ilabortype = silabortype;
	}
	
	public String getsiordertype(){
		return m_iordertype;
	}
	public void setsiordertype(String siordertype){
		m_iordertype = siordertype;
	}
	
	public String getslcreatedbyid(){
		return m_lcreatedbyid;
	}
	public void setslcreatedbyid(String slcreatedbyid){
		m_lcreatedbyid = slcreatedbyid;
	}
	
	public String getsdatetimecreated(){
		return m_datetimecreated;
	}
	public void setsdatetimecreated(String sdatetimecreated){
		m_datetimecreated = sdatetimecreated;
	}
	
	public String getscreatedbyfullname(){
		return m_screatedbyfullname;
	}
	public void setscreatedbyfullname(String screatedbyfullname){
		m_screatedbyfullname = screatedbyfullname;
	}
	
	public String getsllastmodifiedbyid(){
		return m_llastmodifiedbyid;
	}
	public void setsllastmodifiedbyid(String sllastmodifiedbyid){
		m_llastmodifiedbyid = sllastmodifiedbyid;
	}
	
	public String getsdatetimeslastmodified(){
		return m_datetimelastmodified;
	}
	public void setsdatetimeslastmodified(String sdatetimeslastmodified){
		m_datetimelastmodified = sdatetimeslastmodified;
	}
	
	public String getslastmodifiedbyfullname(){
		return m_slastmodifiedbyfullname;
	}
	public void setslastmodifiedbyfullname(String slastmodifiedbyfullname){
		m_slastmodifiedbyfullname = slastmodifiedbyfullname;
	}
	
	public String getsdescription(){
		return m_sdescription;
	}
	public void setsdescription(String sdescription){
		m_sdescription = sdescription;
	}
	
	public String getsremarks(){
		return m_sremarks;
	}
	public void setsremarks(String sremarks){
		m_sremarks = sremarks;
	}
	
	public String getsbdadjustedfreight(){
		return m_bdadjustedfreight;
	}
	public void setsbdadjustedfreight(String sbdadjustedfreight){
		m_bdadjustedfreight = sbdadjustedfreight;
	}
	
	public String getsbdadjustedlaborunitqty(){
		return m_bdadjustedlaborunitqty;
	}
	public void setsbdadjustedlaborunitqty(String sbdadjustedlaborunitqty){
		m_bdadjustedlaborunitqty = sbdadjustedlaborunitqty;
	}
	
	public String getsbdadjustedlaborcostperunit(){
		return m_bdadjustedlaborcostperunit;
	}
	public void setsbdadjustedlaborcostperunit(String sbdadjustedlaborcostperunit){
		m_bdadjustedlaborcostperunit = sbdadjustedlaborcostperunit;
	}
	
	public String getsbdadjustedlmarkupamt(){
		return m_bdadjustedlmarkupamt;
	}
	public void setsbdadjustedlmarkupamt(String sbdadjustedlmarkupamt){
		m_bdadjustedlmarkupamt = sbdadjustedlmarkupamt;
	}
	
	public void addEstimate(SMEstimate estimate){
		arrEstimates.add(estimate);
	}

	public ArrayList<SMEstimate> getEstimateArray(){
		return arrEstimates;
	}
	
	private void saveEstimates(Connection conn, String sUserID, String sUserFullName) throws Exception{
		for (int i = 0; i < arrEstimates.size(); i++){
			SMEstimate estimate = arrEstimates.get(i);
			estimate.setslsummarylid(m_lid);
			estimate.setslsummarylinenumber(Integer.toString(i + 1));
			
			try {
				estimate.save_without_data_transaction(conn, sUserID, sUserFullName);
			} catch (Exception e) {
				throw new Exception("Error [1590511109] saving estimate number " + estimate.getslid() 
					+ " on Estimate Summary " + m_lid + " - " + e.getMessage()
				);
			}
		}
		//We also have to delete any EXTRA estimates that might be left that are higher than our current highest line number:
		//This can happen if we removed an estimate and we now have fewer lines than we previously had:
		String SQL = "DELETE FROM " + SMTablesmestimates.TableName
			+ " WHERE ("
				+ "(" + SMTablesmestimates.lsummarylid + " = " + m_lid + ")"
				+ " AND (" + SMTablesmestimates.lsummarylinenumber + " > " + Integer.toString(arrEstimates.size()) + ")"
			+ ")"
		;
		try {
			Statement stmt = conn.createStatement();
			stmt.execute(SQL);
		} catch (Exception e) {
			throw new Exception("Error [1590511234] deleting leftover Estimates - " + e.getMessage());
		}
		
		//Now delete any orphaned 
		
	}

	public void removeEstimateByLineNumber(String sLineNumber) throws Exception{
    	//System.out.println("[1489528400] - sEntryNumber = " + sEntryNumber);
		
		boolean bSummaryLineNumberWasFound = false;
		for (int i = 0; i < arrEstimates.size(); i++){
			if (arrEstimates.get(i).getslsummarylinenumber().compareToIgnoreCase(sLineNumber) == 0){
				arrEstimates.remove(i);
				bSummaryLineNumberWasFound = true;
			}
		}
   	
    	if (!bSummaryLineNumberWasFound){
    		throw new Exception("Error [1590511389] line number '" + sLineNumber + "' was not found in the Estimate.");
    	}
	}

	public String getQueryString(){
		//Particular to the specific class
		String sQueryString = "";
		
		sQueryString += SMTablesmestimatesummaries.bdadjustedfreight + "=" + clsServletUtilities.URLEncode(getsbdadjustedfreight());
		sQueryString += "&" + SMTablesmestimatesummaries.bdadjustedlaborcostperunit + "=" + clsServletUtilities.URLEncode(getsbdadjustedlaborcostperunit());
		sQueryString += "&" + SMTablesmestimatesummaries.bdadjustedlaborunitqty + "=" + clsServletUtilities.URLEncode(getsbdadjustedlaborunitqty());
		sQueryString += "&" + SMTablesmestimatesummaries.bdadjustedlmarkupamt + "=" + clsServletUtilities.URLEncode(getsbdadjustedlmarkupamt());
		sQueryString += "&" + SMTablesmestimatesummaries.datetimecreated + "=" + clsServletUtilities.URLEncode(getsdatetimecreated());
		sQueryString += "&" + SMTablesmestimatesummaries.datetimelastmodified + "=" + clsServletUtilities.URLEncode(getsdatetimeslastmodified());
		sQueryString += "&" + SMTablesmestimatesummaries.ilabortype + "=" + clsServletUtilities.URLEncode(getsilabortype());
		sQueryString += "&" + SMTablesmestimatesummaries.iordertype + "=" + clsServletUtilities.URLEncode(getsiordertype());
		sQueryString += "&" + SMTablesmestimatesummaries.itaxid + "=" + clsServletUtilities.URLEncode(getsitaxid());
		sQueryString += "&" + SMTablesmestimatesummaries.lcreatedbyid + "=" + clsServletUtilities.URLEncode(getslcreatedbyid());
		sQueryString += "&" + SMTablesmestimatesummaries.lid + "=" + clsServletUtilities.URLEncode(getslid());
		sQueryString += "&" + SMTablesmestimatesummaries.llastmodifiedbyid + "=" + clsServletUtilities.URLEncode(getsllastmodifiedbyid());
		sQueryString += "&" + SMTablesmestimatesummaries.lsalesleadid + "=" + clsServletUtilities.URLEncode(getslsalesleadid());
		sQueryString += "&" + SMTablesmestimatesummaries.screatedbyfullname + "=" + clsServletUtilities.URLEncode(getscreatedbyfullname());
		sQueryString += "&" + SMTablesmestimatesummaries.sdescription + "=" + clsServletUtilities.URLEncode(getsdescription());
		sQueryString += "&" + SMTablesmestimatesummaries.sjobname + "=" + clsServletUtilities.URLEncode(getsjobname());
		sQueryString += "&" + SMTablesmestimatesummaries.slastmodifiedbyfullname + "=" + clsServletUtilities.URLEncode(getslastmodifiedbyfullname());
		sQueryString += "&" + SMTablesmestimatesummaries.sremarks + "=" + clsServletUtilities.URLEncode(getsremarks());
		
		return sQueryString;
	}
	
	public String dumpData(){
		String s = "";
		
		s += "Adjusted freight: " + getsbdadjustedfreight();
		s += "&" + "Adjusted labor cost per unit: " + getsbdadjustedlaborcostperunit();
		s += "&" + "Adjusted labor unit qty: " + getsbdadjustedlaborunitqty();
		s += "&" + "Adjusted mark-up amount: " + getsbdadjustedlmarkupamt();
		s += "&" + "Date created: " + getsdatetimecreated();
		s += "&" + "Date last modified: " + getsdatetimeslastmodified();
		s += "&" + "Labor type: " + getsilabortype();
		s += "&" + "Order type: " + getsiordertype();
		s += "&" + "Tax ID: " + getsitaxid();
		s += "&" + "Created by ID: " + getslcreatedbyid();
		s += "&" + "ID: " + getslid();
		s += "&" + "Last modified by ID: " + getsllastmodifiedbyid();
		s += "&" + "Sales lead ID: " + getslsalesleadid();
		s += "&" + "Created by full name: " + getscreatedbyfullname();
		s += "&" + "Description: " + getsdescription();
		s += "&" + "Job Name: " + getsjobname();
		s += "&" + "Last modified by full name: " + getslastmodifiedbyfullname();
		s += "&" + "Remarks: " + getsremarks();
		
		s += "  -- Number of estimates: " + arrEstimates.size() + "\n";
		
		for (int i = 0; i < arrEstimates.size(); i++){
			s += "  ESTIMATE " + (i + 1) + ":\n";
			s += arrEstimates.get(i).dumpData();
		}
		
		return s;
	}
	public static String getFindSummaryLink(
			String sSearchingClassName, 
			String sReturnField, 
			String sParameterString, 
			ServletContext context,
			String sDBID){
			
			String m_sParameterString = sParameterString;
			
			if (m_sParameterString.startsWith("*")){
				m_sParameterString = m_sParameterString.substring(1);
			}
			
			return  
				SMUtilities.getURLLinkBase(context) + "SMClasses.ObjectFinder"
				+ "?"+ "&ObjectName=" + SMEstimateSummary.OBJECT_NAME
				+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
				+ "&ResultClass=FinderResults"
				+ "&SearchingClass=" + sSearchingClassName
				+ "&ReturnField=" + sReturnField
				+ "&SearchField1=" + SMTablesmestimatesummaries.sjobname
				+ "&SearchFieldAlias1=Job%20Name"
				+ "&SearchField2=" + SMTablesmestimatesummaries.lsalesleadid
				+ "&SearchFieldAlias2=Sales%20Lead%20ID"
				+ "&SearchField3=" + SMTablesmestimatesummaries.sdescription
				+ "&SearchFieldAlias3=Description"
				+ "&ParameterString=*" + m_sParameterString
				;
		}
	private void initializeVariables(){
		
		m_lid = "-1";
		m_sjobname = "";
		m_lsalesleadid = "0";
		m_itaxid = "-1";
		m_ilabortype = "-1";
		m_iordertype = "-1";
		m_lcreatedbyid = "-1";
		m_datetimecreated = SMUtilities.EMPTY_DATETIME_VALUE;
		m_screatedbyfullname = "";
		m_llastmodifiedbyid = "-1";
		m_datetimelastmodified = SMUtilities.EMPTY_DATETIME_VALUE;
		m_slastmodifiedbyfullname = "";
		m_sdescription = "";
		m_sremarks = "";
		m_bdadjustedfreight = "0.00";
		m_bdadjustedlaborunitqty = "0.0000";
		m_bdadjustedlaborcostperunit = "0.00";
		m_bdadjustedlmarkupamt = "0.00";
		arrEstimates = new ArrayList<SMEstimate>(0);
	}
}