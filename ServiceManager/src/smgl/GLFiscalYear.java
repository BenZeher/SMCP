package smgl;

import java.sql.Connection;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import SMDataDefinition.SMTableglaccounts;
import SMDataDefinition.SMTableglfinancialstatementdata;
import SMDataDefinition.SMTableglfiscalperiods;
import SMDataDefinition.SMTableglfiscalsets;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsDateAndTimeConversions;
import ServletUtilities.clsManageRequestParameters;
import ServletUtilities.clsServletUtilities;
import ServletUtilities.clsValidateFormFields;
import smcontrolpanel.SMUtilities;

public class GLFiscalYear extends java.lang.Object{

	public static final String ParamObjectName = "Fiscal year";
	public static final String ParamsNewRecord = "sNewRecord";
	public static final String ADDING_NEW_RECORD_PARAM_VALUE_TRUE = "T";
	public static final String ADDING_NEW_RECORD_PARAM_VALUE_FALSE = "F";
	public static final String EDIT_FORM_NAME = "EDITFISCALPERIODFORM";
	
	public String m_sifiscalyear;
	public String m_silasteditedbyuserid;
	public String m_silastediteddatetime;
	public String m_slasteditedbyfullusername;
	public String m_sinumberofperiods;
	public String m_siactive;
	public String m_siclosed;
	public String m_silockclosingperiod;
	public String m_sdatbeginningdateperiod1;
	public String m_sdatendingdateperiod1;
	public String m_speriod1locked;
	public String m_sdatbeginningdateperiod2;
	public String m_sdatendingdateperiod2;
	public String m_speriod2locked;
	public String m_sdatbeginningdateperiod3;
	public String m_sdatendingdateperiod3;
	public String m_speriod3locked;
	public String m_sdatbeginningdateperiod4;
	public String m_sdatendingdateperiod4;
	public String m_speriod4locked;
	public String m_sdatbeginningdateperiod5;
	public String m_sdatendingdateperiod5;
	public String m_speriod5locked;
	public String m_sdatbeginningdateperiod6;
	public String m_sdatendingdateperiod6;
	public String m_speriod6locked;
	public String m_sdatbeginningdateperiod7;
	public String m_sdatendingdateperiod7;
	public String m_speriod7locked;
	public String m_sdatbeginningdateperiod8;
	public String m_sdatendingdateperiod8;
	public String m_speriod8locked;
	public String m_sdatbeginningdateperiod9;
	public String m_sdatendingdateperiod9;
	public String m_speriod9locked;
	public String m_sdatbeginningdateperiod10;
	public String m_sdatendingdateperiod10;
	public String m_speriod10locked;
	public String m_sdatbeginningdateperiod11;
	public String m_sdatendingdateperiod11;
	public String m_speriod11locked;
	public String m_sdatbeginningdateperiod12;
	public String m_sdatendingdateperiod12;
	public String m_speriod12locked;
	public String m_sdatbeginningdateperiod13;
	public String m_sdatendingdateperiod13;
	public String m_speriod13locked;
	
	private String m_snewrecord;
	
	public GLFiscalYear(
        ) {
		m_sifiscalyear = "0";
		m_silasteditedbyuserid = "0";
		m_slasteditedbyfullusername = "";
		m_silastediteddatetime = clsServletUtilities.EMPTY_DATETIME_VALUE;
		m_sinumberofperiods = "0";
		m_siactive = "0";
		m_siclosed = "0";
		m_silockclosingperiod = "1";
		m_sdatbeginningdateperiod1 = clsServletUtilities.EMPTY_SQL_DATE_VALUE;
		m_sdatendingdateperiod1 = clsServletUtilities.EMPTY_SQL_DATE_VALUE;
		m_sdatbeginningdateperiod2 = clsServletUtilities.EMPTY_SQL_DATE_VALUE;
		m_sdatendingdateperiod2 = clsServletUtilities.EMPTY_SQL_DATE_VALUE;
		m_sdatbeginningdateperiod3 = clsServletUtilities.EMPTY_SQL_DATE_VALUE;
		m_sdatendingdateperiod3 = clsServletUtilities.EMPTY_SQL_DATE_VALUE;
		m_sdatbeginningdateperiod4 = clsServletUtilities.EMPTY_SQL_DATE_VALUE;
		m_sdatendingdateperiod4 = clsServletUtilities.EMPTY_SQL_DATE_VALUE;
		m_sdatbeginningdateperiod5 = clsServletUtilities.EMPTY_SQL_DATE_VALUE;
		m_sdatendingdateperiod5 = clsServletUtilities.EMPTY_SQL_DATE_VALUE;
		m_sdatbeginningdateperiod6 = clsServletUtilities.EMPTY_SQL_DATE_VALUE;
		m_sdatendingdateperiod6 = clsServletUtilities.EMPTY_SQL_DATE_VALUE;
		m_sdatbeginningdateperiod7 = clsServletUtilities.EMPTY_SQL_DATE_VALUE;
		m_sdatendingdateperiod7 = clsServletUtilities.EMPTY_SQL_DATE_VALUE;
		m_sdatbeginningdateperiod8 = clsServletUtilities.EMPTY_SQL_DATE_VALUE;
		m_sdatendingdateperiod8 = clsServletUtilities.EMPTY_SQL_DATE_VALUE;
		m_sdatbeginningdateperiod9 = clsServletUtilities.EMPTY_SQL_DATE_VALUE;
		m_sdatendingdateperiod9 = clsServletUtilities.EMPTY_SQL_DATE_VALUE;
		m_sdatbeginningdateperiod10 = clsServletUtilities.EMPTY_SQL_DATE_VALUE;
		m_sdatendingdateperiod10 = clsServletUtilities.EMPTY_SQL_DATE_VALUE;
		m_sdatbeginningdateperiod11 = clsServletUtilities.EMPTY_SQL_DATE_VALUE;
		m_sdatendingdateperiod11 = clsServletUtilities.EMPTY_SQL_DATE_VALUE;
		m_sdatbeginningdateperiod12 = clsServletUtilities.EMPTY_SQL_DATE_VALUE;
		m_sdatendingdateperiod12 = clsServletUtilities.EMPTY_SQL_DATE_VALUE;
		m_sdatbeginningdateperiod13 = clsServletUtilities.EMPTY_SQL_DATE_VALUE;
		m_sdatendingdateperiod13 = clsServletUtilities.EMPTY_SQL_DATE_VALUE;
		m_speriod1locked = "1";
		m_speriod2locked = "1";
		m_speriod3locked = "1";
		m_speriod4locked = "1";
		m_speriod5locked = "1";
		m_speriod6locked = "1";
		m_speriod7locked = "1";
		m_speriod8locked = "1";
		m_speriod9locked = "1";
		m_speriod10locked = "1";
		m_speriod11locked = "1";
		m_speriod12locked = "1";
		m_speriod13locked = "1";
		
		m_snewrecord = ADDING_NEW_RECORD_PARAM_VALUE_FALSE;
    }
    public GLFiscalYear(HttpServletRequest req) {
		m_sifiscalyear = clsManageRequestParameters.get_Request_Parameter(SMTableglfiscalperiods.ifiscalyear, req).trim();
		m_silasteditedbyuserid = clsManageRequestParameters.get_Request_Parameter(SMTableglfiscalperiods.ilasteditedbyuserid, req).trim();
		m_slasteditedbyfullusername = clsManageRequestParameters.get_Request_Parameter(SMTableglfiscalperiods.slasteditedbyfullusername, req).trim();
		m_silastediteddatetime = clsManageRequestParameters.get_Request_Parameter(SMTableglfiscalperiods.datlastediteddateandtime, req).trim();
		m_sinumberofperiods = clsManageRequestParameters.get_Request_Parameter(SMTableglfiscalperiods.inumberofperiods, req).trim();
		
		if (clsManageRequestParameters.get_Request_Parameter(SMTableglfiscalperiods.iactive, req).compareToIgnoreCase("") == 0){
			m_siactive = "0";
		}else{
			m_siactive = "1";
		}
		
		if (clsManageRequestParameters.get_Request_Parameter(SMTableglfiscalperiods.ilockclosingperiod, req).compareToIgnoreCase("") == 0){
			m_silockclosingperiod = "0";
		}else{
			m_silockclosingperiod = "1";
		}
		m_siclosed = clsManageRequestParameters.get_Request_Parameter(SMTableglfiscalperiods.iclosed, req);

		m_sdatbeginningdateperiod1 = clsManageRequestParameters.get_Request_Parameter(SMTableglfiscalperiods.datbeginningdateperiod1, req).trim();
		m_sdatbeginningdateperiod2 = clsManageRequestParameters.get_Request_Parameter(SMTableglfiscalperiods.datbeginningdateperiod2, req).trim();
		m_sdatbeginningdateperiod3 = clsManageRequestParameters.get_Request_Parameter(SMTableglfiscalperiods.datbeginningdateperiod3, req).trim();
		m_sdatbeginningdateperiod4 = clsManageRequestParameters.get_Request_Parameter(SMTableglfiscalperiods.datbeginningdateperiod4, req).trim();
		m_sdatbeginningdateperiod5 = clsManageRequestParameters.get_Request_Parameter(SMTableglfiscalperiods.datbeginningdateperiod5, req).trim();
		m_sdatbeginningdateperiod6 = clsManageRequestParameters.get_Request_Parameter(SMTableglfiscalperiods.datbeginningdateperiod6, req).trim();
		m_sdatbeginningdateperiod7 = clsManageRequestParameters.get_Request_Parameter(SMTableglfiscalperiods.datbeginningdateperiod7, req).trim();
		m_sdatbeginningdateperiod8 = clsManageRequestParameters.get_Request_Parameter(SMTableglfiscalperiods.datbeginningdateperiod8, req).trim();
		m_sdatbeginningdateperiod9 = clsManageRequestParameters.get_Request_Parameter(SMTableglfiscalperiods.datbeginningdateperiod9, req).trim();
		m_sdatbeginningdateperiod10 = clsManageRequestParameters.get_Request_Parameter(SMTableglfiscalperiods.datbeginningdateperiod10, req).trim();
		m_sdatbeginningdateperiod11 = clsManageRequestParameters.get_Request_Parameter(SMTableglfiscalperiods.datbeginningdateperiod11, req).trim();
		m_sdatbeginningdateperiod12 = clsManageRequestParameters.get_Request_Parameter(SMTableglfiscalperiods.datbeginningdateperiod12, req).trim();
		m_sdatbeginningdateperiod13 = clsManageRequestParameters.get_Request_Parameter(SMTableglfiscalperiods.datbeginningdateperiod13, req).trim();

		m_sdatendingdateperiod1 = clsManageRequestParameters.get_Request_Parameter(SMTableglfiscalperiods.datendingdateperiod1, req).trim();
		m_sdatendingdateperiod2 = clsManageRequestParameters.get_Request_Parameter(SMTableglfiscalperiods.datendingdateperiod2, req).trim();
		m_sdatendingdateperiod3 = clsManageRequestParameters.get_Request_Parameter(SMTableglfiscalperiods.datendingdateperiod3, req).trim();
		m_sdatendingdateperiod4 = clsManageRequestParameters.get_Request_Parameter(SMTableglfiscalperiods.datendingdateperiod4, req).trim();
		m_sdatendingdateperiod5 = clsManageRequestParameters.get_Request_Parameter(SMTableglfiscalperiods.datendingdateperiod5, req).trim();
		m_sdatendingdateperiod6 = clsManageRequestParameters.get_Request_Parameter(SMTableglfiscalperiods.datendingdateperiod6, req).trim();
		m_sdatendingdateperiod7 = clsManageRequestParameters.get_Request_Parameter(SMTableglfiscalperiods.datendingdateperiod7, req).trim();
		m_sdatendingdateperiod8 = clsManageRequestParameters.get_Request_Parameter(SMTableglfiscalperiods.datendingdateperiod8, req).trim();
		m_sdatendingdateperiod9 = clsManageRequestParameters.get_Request_Parameter(SMTableglfiscalperiods.datendingdateperiod9, req).trim();
		m_sdatendingdateperiod10 = clsManageRequestParameters.get_Request_Parameter(SMTableglfiscalperiods.datendingdateperiod10, req).trim();
		m_sdatendingdateperiod11 = clsManageRequestParameters.get_Request_Parameter(SMTableglfiscalperiods.datendingdateperiod11, req).trim();
		m_sdatendingdateperiod12 = clsManageRequestParameters.get_Request_Parameter(SMTableglfiscalperiods.datendingdateperiod12, req).trim();
		m_sdatendingdateperiod13 = clsManageRequestParameters.get_Request_Parameter(SMTableglfiscalperiods.datendingdateperiod13, req).trim();
		
		if (clsManageRequestParameters.get_Request_Parameter(SMTableglfiscalperiods.iperiod1locked, req).compareToIgnoreCase("") == 0){
			m_speriod1locked = "0";
		}else{
			m_speriod1locked = "1";
		}
		if (clsManageRequestParameters.get_Request_Parameter(SMTableglfiscalperiods.iperiod2locked, req).compareToIgnoreCase("") == 0){
			m_speriod2locked = "0";
		}else{
			m_speriod2locked = "1";
		}
		if (clsManageRequestParameters.get_Request_Parameter(SMTableglfiscalperiods.iperiod3locked, req).compareToIgnoreCase("") == 0){
			m_speriod3locked = "0";
		}else{
			m_speriod3locked = "1";
		}
		if (clsManageRequestParameters.get_Request_Parameter(SMTableglfiscalperiods.iperiod4locked, req).compareToIgnoreCase("") == 0){
			m_speriod4locked = "0";
		}else{
			m_speriod4locked = "1";
		}
		if (clsManageRequestParameters.get_Request_Parameter(SMTableglfiscalperiods.iperiod5locked, req).compareToIgnoreCase("") == 0){
			m_speriod5locked = "0";
		}else{
			m_speriod5locked = "1";
		}
		if (clsManageRequestParameters.get_Request_Parameter(SMTableglfiscalperiods.iperiod6locked, req).compareToIgnoreCase("") == 0){
			m_speriod6locked = "0";
		}else{
			m_speriod6locked = "1";
		}
		if (clsManageRequestParameters.get_Request_Parameter(SMTableglfiscalperiods.iperiod7locked, req).compareToIgnoreCase("") == 0){
			m_speriod7locked = "0";
		}else{
			m_speriod7locked = "1";
		}
		if (clsManageRequestParameters.get_Request_Parameter(SMTableglfiscalperiods.iperiod8locked, req).compareToIgnoreCase("") == 0){
			m_speriod8locked = "0";
		}else{
			m_speriod8locked = "1";
		}
		if (clsManageRequestParameters.get_Request_Parameter(SMTableglfiscalperiods.iperiod9locked, req).compareToIgnoreCase("") == 0){
			m_speriod9locked = "0";
		}else{
			m_speriod9locked = "1";
		}
		if (clsManageRequestParameters.get_Request_Parameter(SMTableglfiscalperiods.iperiod10locked, req).compareToIgnoreCase("") == 0){
			m_speriod10locked = "0";
		}else{
			m_speriod10locked = "1";
		}
		if (clsManageRequestParameters.get_Request_Parameter(SMTableglfiscalperiods.iperiod11locked, req).compareToIgnoreCase("") == 0){
			m_speriod11locked = "0";
		}else{
			m_speriod11locked = "1";
		}
		if (clsManageRequestParameters.get_Request_Parameter(SMTableglfiscalperiods.iperiod12locked, req).compareToIgnoreCase("") == 0){
			m_speriod12locked = "0";
		}else{
			m_speriod12locked = "1";
		}
		if (clsManageRequestParameters.get_Request_Parameter(SMTableglfiscalperiods.iperiod13locked, req).compareToIgnoreCase("") == 0){
			m_speriod13locked = "0";
		}else{
			m_speriod13locked = "1";
		}
		
		m_snewrecord = clsManageRequestParameters.get_Request_Parameter(ParamsNewRecord, req).trim();
		if (m_snewrecord.compareToIgnoreCase("") == 0){
			m_snewrecord = ADDING_NEW_RECORD_PARAM_VALUE_FALSE;
		}
	}
    public void load(String sDBName, ServletContext context, String sUser) throws Exception{
    	Connection conn = null;
    	try {
			conn = clsDatabaseFunctions.getConnectionWithException(
				context, 
				sDBName, 
				"MySQL", 
				SMUtilities.getFullClassName(this.toString()) + ".load - user: " + sUser);
		} catch (Exception e) {
			throw new Exception("Error [1530824407] - could not get connection - " + e.getMessage());
		}
    	
    	try {
			load(conn);
		} catch (Exception e) {
			throw new Exception(e.getMessage());
		}
    			
    	clsDatabaseFunctions.freeConnection(context, conn, "[1547080751]");
    }

	public void load (
    		Connection conn
    		) throws Exception{
        String SQL = "SELECT * FROM " + SMTableglfiscalperiods.TableName
        	+ " WHERE ("
        		+ "(" + SMTableglfiscalperiods.ifiscalyear + " = " + m_sifiscalyear + ")"
        	+ ")"
        ;
		
		try {
			ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn); 
			if(rs.next()){
				m_silasteditedbyuserid = Integer.toString(rs.getInt(SMTableglfiscalperiods.ilasteditedbyuserid));
				m_slasteditedbyfullusername = rs.getString(SMTableglfiscalperiods.slasteditedbyfullusername);
				m_silastediteddatetime  = clsDateAndTimeConversions.resultsetDateTimeStringToFormattedString(
					rs.getString(SMTableglfiscalperiods.datlastediteddateandtime), SMUtilities.DATETIME_FORMAT_FOR_DISPLAY, SMUtilities.EMPTY_DATETIME_VALUE);
				m_sinumberofperiods = Integer.toString(rs.getInt(SMTableglfiscalperiods.inumberofperiods));
				m_siactive = Integer.toString(rs.getInt(SMTableglfiscalperiods.iactive));
				m_siclosed = Integer.toString(rs.getInt(SMTableglfiscalperiods.iclosed));
				m_silockclosingperiod = Integer.toString(rs.getInt(SMTableglfiscalperiods.ilockclosingperiod));
				m_sdatbeginningdateperiod1 = clsDateAndTimeConversions.resultsetDateStringToFormattedString(
					rs.getString(SMTableglfiscalperiods.datbeginningdateperiod1), SMUtilities.DATE_FORMAT_FOR_DISPLAY, SMUtilities.EMPTY_DATE_VALUE);
				m_sdatbeginningdateperiod2 = clsDateAndTimeConversions.resultsetDateStringToFormattedString(
						rs.getString(SMTableglfiscalperiods.datbeginningdateperiod2), SMUtilities.DATE_FORMAT_FOR_DISPLAY, SMUtilities.EMPTY_DATE_VALUE);
				m_sdatbeginningdateperiod3 = clsDateAndTimeConversions.resultsetDateStringToFormattedString(
						rs.getString(SMTableglfiscalperiods.datbeginningdateperiod3), SMUtilities.DATE_FORMAT_FOR_DISPLAY, SMUtilities.EMPTY_DATE_VALUE);
				m_sdatbeginningdateperiod4 = clsDateAndTimeConversions.resultsetDateStringToFormattedString(
						rs.getString(SMTableglfiscalperiods.datbeginningdateperiod4), SMUtilities.DATE_FORMAT_FOR_DISPLAY, SMUtilities.EMPTY_DATE_VALUE);
				m_sdatbeginningdateperiod5 = clsDateAndTimeConversions.resultsetDateStringToFormattedString(
						rs.getString(SMTableglfiscalperiods.datbeginningdateperiod5), SMUtilities.DATE_FORMAT_FOR_DISPLAY, SMUtilities.EMPTY_DATE_VALUE);
				m_sdatbeginningdateperiod6 = clsDateAndTimeConversions.resultsetDateStringToFormattedString(
						rs.getString(SMTableglfiscalperiods.datbeginningdateperiod6), SMUtilities.DATE_FORMAT_FOR_DISPLAY, SMUtilities.EMPTY_DATE_VALUE);
				m_sdatbeginningdateperiod7 = clsDateAndTimeConversions.resultsetDateStringToFormattedString(
						rs.getString(SMTableglfiscalperiods.datbeginningdateperiod7), SMUtilities.DATE_FORMAT_FOR_DISPLAY, SMUtilities.EMPTY_DATE_VALUE);
				m_sdatbeginningdateperiod8 = clsDateAndTimeConversions.resultsetDateStringToFormattedString(
						rs.getString(SMTableglfiscalperiods.datbeginningdateperiod8), SMUtilities.DATE_FORMAT_FOR_DISPLAY, SMUtilities.EMPTY_DATE_VALUE);
				m_sdatbeginningdateperiod9 = clsDateAndTimeConversions.resultsetDateStringToFormattedString(
						rs.getString(SMTableglfiscalperiods.datbeginningdateperiod9), SMUtilities.DATE_FORMAT_FOR_DISPLAY, SMUtilities.EMPTY_DATE_VALUE);
				m_sdatbeginningdateperiod10 = clsDateAndTimeConversions.resultsetDateStringToFormattedString(
						rs.getString(SMTableglfiscalperiods.datbeginningdateperiod10), SMUtilities.DATE_FORMAT_FOR_DISPLAY, SMUtilities.EMPTY_DATE_VALUE);
				m_sdatbeginningdateperiod11 = clsDateAndTimeConversions.resultsetDateStringToFormattedString(
						rs.getString(SMTableglfiscalperiods.datbeginningdateperiod11), SMUtilities.DATE_FORMAT_FOR_DISPLAY, SMUtilities.EMPTY_DATE_VALUE);
				m_sdatbeginningdateperiod12 = clsDateAndTimeConversions.resultsetDateStringToFormattedString(
						rs.getString(SMTableglfiscalperiods.datbeginningdateperiod12), SMUtilities.DATE_FORMAT_FOR_DISPLAY, SMUtilities.EMPTY_DATE_VALUE);
				m_sdatbeginningdateperiod13 = clsDateAndTimeConversions.resultsetDateStringToFormattedString(
						rs.getString(SMTableglfiscalperiods.datbeginningdateperiod13), SMUtilities.DATE_FORMAT_FOR_DISPLAY, SMUtilities.EMPTY_DATE_VALUE);
				m_sdatendingdateperiod1 = clsDateAndTimeConversions.resultsetDateStringToFormattedString(
						rs.getString(SMTableglfiscalperiods.datendingdateperiod1), SMUtilities.DATE_FORMAT_FOR_DISPLAY, SMUtilities.EMPTY_DATE_VALUE);
				m_sdatendingdateperiod2 = clsDateAndTimeConversions.resultsetDateStringToFormattedString(
						rs.getString(SMTableglfiscalperiods.datendingdateperiod2), SMUtilities.DATE_FORMAT_FOR_DISPLAY, SMUtilities.EMPTY_DATE_VALUE);
				m_sdatendingdateperiod3 = clsDateAndTimeConversions.resultsetDateStringToFormattedString(
						rs.getString(SMTableglfiscalperiods.datendingdateperiod3), SMUtilities.DATE_FORMAT_FOR_DISPLAY, SMUtilities.EMPTY_DATE_VALUE);
				m_sdatendingdateperiod4 = clsDateAndTimeConversions.resultsetDateStringToFormattedString(
						rs.getString(SMTableglfiscalperiods.datendingdateperiod4), SMUtilities.DATE_FORMAT_FOR_DISPLAY, SMUtilities.EMPTY_DATE_VALUE);					
				m_sdatendingdateperiod5 = clsDateAndTimeConversions.resultsetDateStringToFormattedString(
						rs.getString(SMTableglfiscalperiods.datendingdateperiod5), SMUtilities.DATE_FORMAT_FOR_DISPLAY, SMUtilities.EMPTY_DATE_VALUE);					
				m_sdatendingdateperiod6 = clsDateAndTimeConversions.resultsetDateStringToFormattedString(
						rs.getString(SMTableglfiscalperiods.datendingdateperiod6), SMUtilities.DATE_FORMAT_FOR_DISPLAY, SMUtilities.EMPTY_DATE_VALUE);					
				m_sdatendingdateperiod7 = clsDateAndTimeConversions.resultsetDateStringToFormattedString(
						rs.getString(SMTableglfiscalperiods.datendingdateperiod7), SMUtilities.DATE_FORMAT_FOR_DISPLAY, SMUtilities.EMPTY_DATE_VALUE);					
				m_sdatendingdateperiod8 = clsDateAndTimeConversions.resultsetDateStringToFormattedString(
						rs.getString(SMTableglfiscalperiods.datendingdateperiod8), SMUtilities.DATE_FORMAT_FOR_DISPLAY, SMUtilities.EMPTY_DATE_VALUE);					
				m_sdatendingdateperiod9 = clsDateAndTimeConversions.resultsetDateStringToFormattedString(
						rs.getString(SMTableglfiscalperiods.datendingdateperiod9), SMUtilities.DATE_FORMAT_FOR_DISPLAY, SMUtilities.EMPTY_DATE_VALUE);					
				m_sdatendingdateperiod10 = clsDateAndTimeConversions.resultsetDateStringToFormattedString(
						rs.getString(SMTableglfiscalperiods.datendingdateperiod10), SMUtilities.DATE_FORMAT_FOR_DISPLAY, SMUtilities.EMPTY_DATE_VALUE);					
				m_sdatendingdateperiod11 = clsDateAndTimeConversions.resultsetDateStringToFormattedString(
						rs.getString(SMTableglfiscalperiods.datendingdateperiod11), SMUtilities.DATE_FORMAT_FOR_DISPLAY, SMUtilities.EMPTY_DATE_VALUE);					
				m_sdatendingdateperiod12 = clsDateAndTimeConversions.resultsetDateStringToFormattedString(
						rs.getString(SMTableglfiscalperiods.datendingdateperiod12), SMUtilities.DATE_FORMAT_FOR_DISPLAY, SMUtilities.EMPTY_DATE_VALUE);				
				m_sdatendingdateperiod13 = clsDateAndTimeConversions.resultsetDateStringToFormattedString(
						rs.getString(SMTableglfiscalperiods.datendingdateperiod13), SMUtilities.DATE_FORMAT_FOR_DISPLAY, SMUtilities.EMPTY_DATE_VALUE);				
				m_speriod1locked = Integer.toString(rs.getInt(SMTableglfiscalperiods.iperiod1locked));
				m_speriod2locked = Integer.toString(rs.getInt(SMTableglfiscalperiods.iperiod2locked));
				m_speriod3locked = Integer.toString(rs.getInt(SMTableglfiscalperiods.iperiod3locked));
				m_speriod4locked = Integer.toString(rs.getInt(SMTableglfiscalperiods.iperiod4locked));
				m_speriod5locked = Integer.toString(rs.getInt(SMTableglfiscalperiods.iperiod5locked));
				m_speriod6locked = Integer.toString(rs.getInt(SMTableglfiscalperiods.iperiod6locked));
				m_speriod7locked = Integer.toString(rs.getInt(SMTableglfiscalperiods.iperiod7locked));
				m_speriod8locked = Integer.toString(rs.getInt(SMTableglfiscalperiods.iperiod8locked));
				m_speriod9locked = Integer.toString(rs.getInt(SMTableglfiscalperiods.iperiod9locked));
				m_speriod10locked = Integer.toString(rs.getInt(SMTableglfiscalperiods.iperiod10locked));
				m_speriod11locked = Integer.toString(rs.getInt(SMTableglfiscalperiods.iperiod11locked));
				m_speriod12locked = Integer.toString(rs.getInt(SMTableglfiscalperiods.iperiod12locked));
				m_speriod13locked = Integer.toString(rs.getInt(SMTableglfiscalperiods.iperiod13locked));
				
				m_snewrecord = ADDING_NEW_RECORD_PARAM_VALUE_FALSE;
			}else{
				throw new Exception("Error [1530825053] - Fiscal year '" + m_sifiscalyear + "' does not exist.");
			}
			rs.close();
		}catch (SQLException ex){
			throw new Exception("Error [1530825054] loading fiscal year '" + m_sifiscalyear + "' using SQL: " + SQL + " - " + ex.getMessage());
		}
	}
	public void saveWithoutConnection(ServletContext context, String sDBIB, String sUserID, String sUserFullName) throws Exception{
    	//Get connection
		Connection conn = null;
		try {
			conn = clsDatabaseFunctions.getConnectionWithException(
				context, 
				sDBIB, 
				"MySQL", 
				SMUtilities.getFullClassName(this.toString()) + ":save - user: " + sUserID + " - " + sUserFullName
			);
		} catch (Exception e1) {
			throw new Exception("Error [1530899382] - could not get connection to save.");
		}
		
		try {
			saveWithConnection(conn, sUserID, sUserFullName);
		} catch (Exception e) {
			clsDatabaseFunctions.freeConnection(context, conn, "[1547080752]");
			throw new Exception("Error [20193441252436] " + e.getMessage());
		}
		clsDatabaseFunctions.freeConnection(context, conn, "[1547081752]");
		return;
	}
    public void saveWithConnection(Connection conn, String sUserID, String sUserFullName) throws Exception{
		
	   	String currentDateTime = new SimpleDateFormat(clsServletUtilities.DATETIME_FORMAT_FOR_DISPLAY).format(new Date(System.currentTimeMillis()));
    	//Set the user ID and full name first:
    	set_slasteditedbyuserid(sUserID);
    	set_slasteditedbyuserfullname(sUserFullName);
    	set_sdattimelastedited(clsValidateFormFields.validateDateTimeField(currentDateTime, "Time last edited", clsServletUtilities.DATETIME_FORMAT_FOR_DISPLAY, true));
		
		//Validate entries
		try {
			validateEntries(conn);
		} catch (Exception e1) {
			throw new Exception("ERROR [1535029204] "+e1.getMessage());
		}
		//Update the editable fields.
		String SQL = "";
		if (get_snewrecord().compareToIgnoreCase(ADDING_NEW_RECORD_PARAM_VALUE_TRUE) == 0){
			SQL = "INSERT INTO " + SMTableglfiscalperiods.TableName + "("
				+  SMTableglfiscalperiods.datbeginningdateperiod1
				+ ", " + SMTableglfiscalperiods.datbeginningdateperiod2
				+ ", " + SMTableglfiscalperiods.datbeginningdateperiod3
				+ ", " + SMTableglfiscalperiods.datbeginningdateperiod4
				+ ", " + SMTableglfiscalperiods.datbeginningdateperiod5
				+ ", " + SMTableglfiscalperiods.datbeginningdateperiod6
				+ ", " + SMTableglfiscalperiods.datbeginningdateperiod7
				+ ", " + SMTableglfiscalperiods.datbeginningdateperiod8
				+ ", " + SMTableglfiscalperiods.datbeginningdateperiod9
				+ ", " + SMTableglfiscalperiods.datbeginningdateperiod10
				+ ", " + SMTableglfiscalperiods.datbeginningdateperiod11
				+ ", " + SMTableglfiscalperiods.datbeginningdateperiod12
				+ ", " + SMTableglfiscalperiods.datbeginningdateperiod13
				+ ", " + SMTableglfiscalperiods.datendingdateperiod1
				+ ", " + SMTableglfiscalperiods.datendingdateperiod2
				+ ", " + SMTableglfiscalperiods.datendingdateperiod3
				+ ", " + SMTableglfiscalperiods.datendingdateperiod4
				+ ", " + SMTableglfiscalperiods.datendingdateperiod5
				+ ", " + SMTableglfiscalperiods.datendingdateperiod6
				+ ", " + SMTableglfiscalperiods.datendingdateperiod7
				+ ", " + SMTableglfiscalperiods.datendingdateperiod8
				+ ", " + SMTableglfiscalperiods.datendingdateperiod9
				+ ", " + SMTableglfiscalperiods.datendingdateperiod10
				+ ", " + SMTableglfiscalperiods.datendingdateperiod11
				+ ", " + SMTableglfiscalperiods.datendingdateperiod12
				+ ", " + SMTableglfiscalperiods.datendingdateperiod13
				+ ", " + SMTableglfiscalperiods.iactive
				+ ", " + SMTableglfiscalperiods.iclosed
				+ ", " + SMTableglfiscalperiods.ilockclosingperiod
				+ ", " + SMTableglfiscalperiods.ifiscalyear
				+ ", " + SMTableglfiscalperiods.ilasteditedbyuserid
				+ ", " + SMTableglfiscalperiods.inumberofperiods
				+ ", " + SMTableglfiscalperiods.slasteditedbyfullusername
				+ ", " + SMTableglfiscalperiods.datlastediteddateandtime
				+ ", " + SMTableglfiscalperiods.iperiod1locked
				+ ", " + SMTableglfiscalperiods.iperiod2locked
				+ ", " + SMTableglfiscalperiods.iperiod3locked
				+ ", " + SMTableglfiscalperiods.iperiod4locked
				+ ", " + SMTableglfiscalperiods.iperiod5locked
				+ ", " + SMTableglfiscalperiods.iperiod6locked
				+ ", " + SMTableglfiscalperiods.iperiod7locked
				+ ", " + SMTableglfiscalperiods.iperiod8locked
				+ ", " + SMTableglfiscalperiods.iperiod9locked
				+ ", " + SMTableglfiscalperiods.iperiod10locked
				+ ", " + SMTableglfiscalperiods.iperiod11locked
				+ ", " + SMTableglfiscalperiods.iperiod12locked
				+ ", " + SMTableglfiscalperiods.iperiod13locked
				
			+ ") VALUES ("
				+ "'" + ServletUtilities.clsDateAndTimeConversions.convertDateFormat(
					get_sdatbeginningdateperiod1(), 
					clsServletUtilities.DATE_FORMAT_FOR_DISPLAY,
					clsServletUtilities.DATE_FORMAT_FOR_SQL, 
					clsServletUtilities.EMPTY_SQL_DATE_VALUE
				) + "'"
			+ ", '" + ServletUtilities.clsDateAndTimeConversions.convertDateFormat(
					get_sdatbeginningdateperiod2(), 
					clsServletUtilities.DATE_FORMAT_FOR_DISPLAY,
					clsServletUtilities.DATE_FORMAT_FOR_SQL, 
					clsServletUtilities.EMPTY_SQL_DATE_VALUE
				) + "'"
			+ ", '" + ServletUtilities.clsDateAndTimeConversions.convertDateFormat(
					get_sdatbeginningdateperiod3(), 
					clsServletUtilities.DATE_FORMAT_FOR_DISPLAY,
					clsServletUtilities.DATE_FORMAT_FOR_SQL, 
					clsServletUtilities.EMPTY_SQL_DATE_VALUE
				) + "'"
			+ ", '" + ServletUtilities.clsDateAndTimeConversions.convertDateFormat(
					get_sdatbeginningdateperiod4(), 
					clsServletUtilities.DATE_FORMAT_FOR_DISPLAY,
					clsServletUtilities.DATE_FORMAT_FOR_SQL, 
					clsServletUtilities.EMPTY_SQL_DATE_VALUE
				) + "'"
			+ ", '" + ServletUtilities.clsDateAndTimeConversions.convertDateFormat(
					get_sdatbeginningdateperiod5(), 
					clsServletUtilities.DATE_FORMAT_FOR_DISPLAY,
					clsServletUtilities.DATE_FORMAT_FOR_SQL, 
					clsServletUtilities.EMPTY_SQL_DATE_VALUE
				) + "'"
			+ ", '" + ServletUtilities.clsDateAndTimeConversions.convertDateFormat(
					get_sdatbeginningdateperiod6(), 
					clsServletUtilities.DATE_FORMAT_FOR_DISPLAY,
					clsServletUtilities.DATE_FORMAT_FOR_SQL, 
					clsServletUtilities.EMPTY_SQL_DATE_VALUE
				) + "'"
			+ ", '" + ServletUtilities.clsDateAndTimeConversions.convertDateFormat(
					get_sdatbeginningdateperiod7(), 
					clsServletUtilities.DATE_FORMAT_FOR_DISPLAY,
					clsServletUtilities.DATE_FORMAT_FOR_SQL, 
					clsServletUtilities.EMPTY_SQL_DATE_VALUE
				) + "'"
			+ ", '" + ServletUtilities.clsDateAndTimeConversions.convertDateFormat(
					get_sdatbeginningdateperiod8(), 
					clsServletUtilities.DATE_FORMAT_FOR_DISPLAY,
					clsServletUtilities.DATE_FORMAT_FOR_SQL, 
					clsServletUtilities.EMPTY_SQL_DATE_VALUE
				) + "'"
			+ ", '" + ServletUtilities.clsDateAndTimeConversions.convertDateFormat(
					get_sdatbeginningdateperiod9(), 
					clsServletUtilities.DATE_FORMAT_FOR_DISPLAY,
					clsServletUtilities.DATE_FORMAT_FOR_SQL, 
					clsServletUtilities.EMPTY_SQL_DATE_VALUE
				) + "'"
			+ ", '" + ServletUtilities.clsDateAndTimeConversions.convertDateFormat(
					get_sdatbeginningdateperiod10(), 
					clsServletUtilities.DATE_FORMAT_FOR_DISPLAY,
					clsServletUtilities.DATE_FORMAT_FOR_SQL, 
					clsServletUtilities.EMPTY_SQL_DATE_VALUE
				) + "'"
			+ ", '" + ServletUtilities.clsDateAndTimeConversions.convertDateFormat(
					get_sdatbeginningdateperiod11(), 
					clsServletUtilities.DATE_FORMAT_FOR_DISPLAY,
					clsServletUtilities.DATE_FORMAT_FOR_SQL, 
					clsServletUtilities.EMPTY_SQL_DATE_VALUE
				) + "'"
			+ ", '" + ServletUtilities.clsDateAndTimeConversions.convertDateFormat(
					get_sdatbeginningdateperiod12(), 
					clsServletUtilities.DATE_FORMAT_FOR_DISPLAY,
					clsServletUtilities.DATE_FORMAT_FOR_SQL, 
					clsServletUtilities.EMPTY_SQL_DATE_VALUE
				) + "'"
			+ ", '" + ServletUtilities.clsDateAndTimeConversions.convertDateFormat(
					get_sdatbeginningdateperiod13(), 
					clsServletUtilities.DATE_FORMAT_FOR_DISPLAY,
					clsServletUtilities.DATE_FORMAT_FOR_SQL, 
					clsServletUtilities.EMPTY_SQL_DATE_VALUE
				) + "'"
			+ ", '" + ServletUtilities.clsDateAndTimeConversions.convertDateFormat(
					get_sdatendingdateperiod1(), 
					clsServletUtilities.DATE_FORMAT_FOR_DISPLAY,
					clsServletUtilities.DATE_FORMAT_FOR_SQL, 
					clsServletUtilities.EMPTY_SQL_DATE_VALUE
				) + "'"
			+ ", '" + ServletUtilities.clsDateAndTimeConversions.convertDateFormat(
					get_sdatendingdateperiod2(), 
					clsServletUtilities.DATE_FORMAT_FOR_DISPLAY,
					clsServletUtilities.DATE_FORMAT_FOR_SQL, 
					clsServletUtilities.EMPTY_SQL_DATE_VALUE
				) + "'"
			+ ", '" + ServletUtilities.clsDateAndTimeConversions.convertDateFormat(
					get_sdatendingdateperiod3(), 
					clsServletUtilities.DATE_FORMAT_FOR_DISPLAY,
					clsServletUtilities.DATE_FORMAT_FOR_SQL, 
					clsServletUtilities.EMPTY_SQL_DATE_VALUE
				) + "'"
			+ ", '" + ServletUtilities.clsDateAndTimeConversions.convertDateFormat(
					get_sdatendingdateperiod4(), 
					clsServletUtilities.DATE_FORMAT_FOR_DISPLAY,
					clsServletUtilities.DATE_FORMAT_FOR_SQL, 
					clsServletUtilities.EMPTY_SQL_DATE_VALUE
				) + "'"
			+ ", '" + ServletUtilities.clsDateAndTimeConversions.convertDateFormat(
					get_sdatendingdateperiod5(), 
					clsServletUtilities.DATE_FORMAT_FOR_DISPLAY,
					clsServletUtilities.DATE_FORMAT_FOR_SQL, 
					clsServletUtilities.EMPTY_SQL_DATE_VALUE
				) + "'"
			+ ", '" + ServletUtilities.clsDateAndTimeConversions.convertDateFormat(
					get_sdatendingdateperiod6(), 
					clsServletUtilities.DATE_FORMAT_FOR_DISPLAY,
					clsServletUtilities.DATE_FORMAT_FOR_SQL, 
					clsServletUtilities.EMPTY_SQL_DATE_VALUE
				) + "'"
			+ ", '" + ServletUtilities.clsDateAndTimeConversions.convertDateFormat(
					get_sdatendingdateperiod7(), 
					clsServletUtilities.DATE_FORMAT_FOR_DISPLAY,
					clsServletUtilities.DATE_FORMAT_FOR_SQL, 
					clsServletUtilities.EMPTY_SQL_DATE_VALUE
				) + "'"
			+ ", '" + ServletUtilities.clsDateAndTimeConversions.convertDateFormat(
					get_sdatendingdateperiod8(), 
					clsServletUtilities.DATE_FORMAT_FOR_DISPLAY,
					clsServletUtilities.DATE_FORMAT_FOR_SQL, 
					clsServletUtilities.EMPTY_SQL_DATE_VALUE
				) + "'"
			+ ", '" + ServletUtilities.clsDateAndTimeConversions.convertDateFormat(
					get_sdatendingdateperiod9(), 
					clsServletUtilities.DATE_FORMAT_FOR_DISPLAY,
					clsServletUtilities.DATE_FORMAT_FOR_SQL, 
					clsServletUtilities.EMPTY_SQL_DATE_VALUE
				) + "'"
			+ ", '" + ServletUtilities.clsDateAndTimeConversions.convertDateFormat(
					get_sdatendingdateperiod10(), 
					clsServletUtilities.DATE_FORMAT_FOR_DISPLAY,
					clsServletUtilities.DATE_FORMAT_FOR_SQL, 
					clsServletUtilities.EMPTY_SQL_DATE_VALUE
				) + "'"
			+ ", '" + ServletUtilities.clsDateAndTimeConversions.convertDateFormat(
					get_sdatendingdateperiod11(), 
					clsServletUtilities.DATE_FORMAT_FOR_DISPLAY,
					clsServletUtilities.DATE_FORMAT_FOR_SQL, 
					clsServletUtilities.EMPTY_SQL_DATE_VALUE
				) + "'"
			+ ", '" + ServletUtilities.clsDateAndTimeConversions.convertDateFormat(
					get_sdatendingdateperiod12(), 
					clsServletUtilities.DATE_FORMAT_FOR_DISPLAY,
					clsServletUtilities.DATE_FORMAT_FOR_SQL, 
					clsServletUtilities.EMPTY_SQL_DATE_VALUE
				) + "'"
			+ ", '" + ServletUtilities.clsDateAndTimeConversions.convertDateFormat(
					get_sdatendingdateperiod13(), 
					clsServletUtilities.DATE_FORMAT_FOR_DISPLAY,
					clsServletUtilities.DATE_FORMAT_FOR_SQL, 
					clsServletUtilities.EMPTY_SQL_DATE_VALUE
				) + "'"

			+ ", " + get_siactive()
			+ ", " + get_siclosed()
			+ ", " + get_silockclosingperiod()
			+ ", " + get_sifiscalyear()
			+ ", " + get_slasteditedbyuserid()
			+ ", " + get_sinumberofperiods()
			+ ", '" + clsDatabaseFunctions.FormatSQLStatement(get_slasteditedbyuserfullname()) + "'"
			+ ", '" + ServletUtilities.clsDateAndTimeConversions.convertDateFormat(
					get_sdattimelastedited(), 
					clsServletUtilities.DATETIME_FORMAT_FOR_DISPLAY,
					clsServletUtilities.DATETIME_FORMAT_FOR_SQL, 
					clsServletUtilities.EMPTY_SQL_DATETIME_VALUE
				) + "'"			
			+ ", " + get_siperiod1locked()
			+ ", " + get_siperiod2locked()
			+ ", " + get_siperiod3locked()
			+ ", " + get_siperiod4locked()
			+ ", " + get_siperiod5locked()
			+ ", " + get_siperiod6locked()
			+ ", " + get_siperiod7locked()
			+ ", " + get_siperiod8locked()
			+ ", " + get_siperiod9locked()
			+ ", " + get_siperiod10locked()
			+ ", " + get_siperiod11locked()
			+ ", " + get_siperiod12locked()
			+ ", " + get_siperiod13locked()
			+ ")"
			;
			
		}else{
			SQL = "UPDATE " + SMTableglfiscalperiods.TableName 
				+ " SET " +  SMTableglfiscalperiods.datbeginningdateperiod1 + " = "
					+ "'" + ServletUtilities.clsDateAndTimeConversions.convertDateFormat(
						get_sdatbeginningdateperiod1(), 
						clsServletUtilities.DATE_FORMAT_FOR_DISPLAY,
						clsServletUtilities.DATE_FORMAT_FOR_SQL, 
						clsServletUtilities.EMPTY_SQL_DATE_VALUE
					) + "'"
				+ ", " + SMTableglfiscalperiods.datbeginningdateperiod2 + " = "
					+ "'" + ServletUtilities.clsDateAndTimeConversions.convertDateFormat(
						get_sdatbeginningdateperiod2(), 
						clsServletUtilities.DATE_FORMAT_FOR_DISPLAY,
						clsServletUtilities.DATE_FORMAT_FOR_SQL, 
						clsServletUtilities.EMPTY_SQL_DATE_VALUE
					) + "'"
				+ ", " + SMTableglfiscalperiods.datbeginningdateperiod3 + " = "
					+ "'" + ServletUtilities.clsDateAndTimeConversions.convertDateFormat(
						get_sdatbeginningdateperiod3(), 
						clsServletUtilities.DATE_FORMAT_FOR_DISPLAY,
						clsServletUtilities.DATE_FORMAT_FOR_SQL, 
						clsServletUtilities.EMPTY_SQL_DATE_VALUE
					) + "'"				
				+ ", " + SMTableglfiscalperiods.datbeginningdateperiod4 + " = "
					+ "'" + ServletUtilities.clsDateAndTimeConversions.convertDateFormat(
						get_sdatbeginningdateperiod4(), 
						clsServletUtilities.DATE_FORMAT_FOR_DISPLAY,
						clsServletUtilities.DATE_FORMAT_FOR_SQL, 
						clsServletUtilities.EMPTY_SQL_DATE_VALUE
					) + "'"				
				+ ", " + SMTableglfiscalperiods.datbeginningdateperiod5 + " = "
					+ "'" + ServletUtilities.clsDateAndTimeConversions.convertDateFormat(
						get_sdatbeginningdateperiod5(), 
						clsServletUtilities.DATE_FORMAT_FOR_DISPLAY,
						clsServletUtilities.DATE_FORMAT_FOR_SQL, 
						clsServletUtilities.EMPTY_SQL_DATE_VALUE
					) + "'"				
				+ ", " + SMTableglfiscalperiods.datbeginningdateperiod6 + " = "
					+ "'" + ServletUtilities.clsDateAndTimeConversions.convertDateFormat(
						get_sdatbeginningdateperiod6(), 
						clsServletUtilities.DATE_FORMAT_FOR_DISPLAY,
						clsServletUtilities.DATE_FORMAT_FOR_SQL, 
						clsServletUtilities.EMPTY_SQL_DATE_VALUE
					) + "'"				
				+ ", " + SMTableglfiscalperiods.datbeginningdateperiod7 + " = "
					+ "'" + ServletUtilities.clsDateAndTimeConversions.convertDateFormat(
						get_sdatbeginningdateperiod7(), 
						clsServletUtilities.DATE_FORMAT_FOR_DISPLAY,
						clsServletUtilities.DATE_FORMAT_FOR_SQL, 
						clsServletUtilities.EMPTY_SQL_DATE_VALUE
					) + "'"				
				+ ", " + SMTableglfiscalperiods.datbeginningdateperiod8 + " = "
					+ "'" + ServletUtilities.clsDateAndTimeConversions.convertDateFormat(
						get_sdatbeginningdateperiod8(), 
						clsServletUtilities.DATE_FORMAT_FOR_DISPLAY,
						clsServletUtilities.DATE_FORMAT_FOR_SQL, 
						clsServletUtilities.EMPTY_SQL_DATE_VALUE
					) + "'"				
				+ ", " + SMTableglfiscalperiods.datbeginningdateperiod9 + " = "
					+ "'" + ServletUtilities.clsDateAndTimeConversions.convertDateFormat(
						get_sdatbeginningdateperiod9(), 
						clsServletUtilities.DATE_FORMAT_FOR_DISPLAY,
						clsServletUtilities.DATE_FORMAT_FOR_SQL, 
						clsServletUtilities.EMPTY_SQL_DATE_VALUE
					) + "'"				
				+ ", " + SMTableglfiscalperiods.datbeginningdateperiod10 + " = "
					+ "'" + ServletUtilities.clsDateAndTimeConversions.convertDateFormat(
						get_sdatbeginningdateperiod10(), 
						clsServletUtilities.DATE_FORMAT_FOR_DISPLAY,
						clsServletUtilities.DATE_FORMAT_FOR_SQL, 
						clsServletUtilities.EMPTY_SQL_DATE_VALUE
					) + "'"				
				+ ", " + SMTableglfiscalperiods.datbeginningdateperiod11 + " = "
					+ "'" + ServletUtilities.clsDateAndTimeConversions.convertDateFormat(
						get_sdatbeginningdateperiod11(), 
						clsServletUtilities.DATE_FORMAT_FOR_DISPLAY,
						clsServletUtilities.DATE_FORMAT_FOR_SQL, 
						clsServletUtilities.EMPTY_SQL_DATE_VALUE
					) + "'"				
				+ ", " + SMTableglfiscalperiods.datbeginningdateperiod12 + " = "
					+ "'" + ServletUtilities.clsDateAndTimeConversions.convertDateFormat(
						get_sdatbeginningdateperiod12(), 
						clsServletUtilities.DATE_FORMAT_FOR_DISPLAY,
						clsServletUtilities.DATE_FORMAT_FOR_SQL, 
						clsServletUtilities.EMPTY_SQL_DATE_VALUE
					) + "'"				
				+ ", " + SMTableglfiscalperiods.datbeginningdateperiod13 + " = "
					+ "'" + ServletUtilities.clsDateAndTimeConversions.convertDateFormat(
						get_sdatbeginningdateperiod13(), 
						clsServletUtilities.DATE_FORMAT_FOR_DISPLAY,
						clsServletUtilities.DATE_FORMAT_FOR_SQL, 
						clsServletUtilities.EMPTY_SQL_DATE_VALUE
					) + "'"				
				+ ", " + SMTableglfiscalperiods.datendingdateperiod1 + " = "
					+ "'" + ServletUtilities.clsDateAndTimeConversions.convertDateFormat(
						get_sdatendingdateperiod1(), 
						clsServletUtilities.DATE_FORMAT_FOR_DISPLAY,
						clsServletUtilities.DATE_FORMAT_FOR_SQL, 
						clsServletUtilities.EMPTY_SQL_DATE_VALUE
					) + "'"				
				+ ", " + SMTableglfiscalperiods.datendingdateperiod2 + " = "
					+ "'" + ServletUtilities.clsDateAndTimeConversions.convertDateFormat(
						get_sdatendingdateperiod2(), 
						clsServletUtilities.DATE_FORMAT_FOR_DISPLAY,
						clsServletUtilities.DATE_FORMAT_FOR_SQL, 
						clsServletUtilities.EMPTY_SQL_DATE_VALUE
					) + "'"				
				+ ", " + SMTableglfiscalperiods.datendingdateperiod3 + " = "
					+ "'" + ServletUtilities.clsDateAndTimeConversions.convertDateFormat(
						get_sdatendingdateperiod3(), 
						clsServletUtilities.DATE_FORMAT_FOR_DISPLAY,
						clsServletUtilities.DATE_FORMAT_FOR_SQL, 
						clsServletUtilities.EMPTY_SQL_DATE_VALUE
					) + "'"				
				+ ", " + SMTableglfiscalperiods.datendingdateperiod4 + " = "
					+ "'" + ServletUtilities.clsDateAndTimeConversions.convertDateFormat(
						get_sdatendingdateperiod4(), 
						clsServletUtilities.DATE_FORMAT_FOR_DISPLAY,
						clsServletUtilities.DATE_FORMAT_FOR_SQL, 
						clsServletUtilities.EMPTY_SQL_DATE_VALUE
					) + "'"				
				+ ", " + SMTableglfiscalperiods.datendingdateperiod5 + " = "
					+ "'" + ServletUtilities.clsDateAndTimeConversions.convertDateFormat(
						get_sdatendingdateperiod5(), 
						clsServletUtilities.DATE_FORMAT_FOR_DISPLAY,
						clsServletUtilities.DATE_FORMAT_FOR_SQL, 
						clsServletUtilities.EMPTY_SQL_DATE_VALUE
					) + "'"				
				+ ", " + SMTableglfiscalperiods.datendingdateperiod6 + " = "
					+ "'" + ServletUtilities.clsDateAndTimeConversions.convertDateFormat(
						get_sdatendingdateperiod6(), 
						clsServletUtilities.DATE_FORMAT_FOR_DISPLAY,
						clsServletUtilities.DATE_FORMAT_FOR_SQL, 
						clsServletUtilities.EMPTY_SQL_DATE_VALUE
					) + "'"				
				+ ", " + SMTableglfiscalperiods.datendingdateperiod7 + " = "
					+ "'" + ServletUtilities.clsDateAndTimeConversions.convertDateFormat(
						get_sdatendingdateperiod7(), 
						clsServletUtilities.DATE_FORMAT_FOR_DISPLAY,
						clsServletUtilities.DATE_FORMAT_FOR_SQL, 
						clsServletUtilities.EMPTY_SQL_DATE_VALUE
					) + "'"				
				+ ", " + SMTableglfiscalperiods.datendingdateperiod8 + " = "
					+ "'" + ServletUtilities.clsDateAndTimeConversions.convertDateFormat(
						get_sdatendingdateperiod8(), 
						clsServletUtilities.DATE_FORMAT_FOR_DISPLAY,
						clsServletUtilities.DATE_FORMAT_FOR_SQL, 
						clsServletUtilities.EMPTY_SQL_DATE_VALUE
					) + "'"				
				+ ", " + SMTableglfiscalperiods.datendingdateperiod9 + " = "
					+ "'" + ServletUtilities.clsDateAndTimeConversions.convertDateFormat(
						get_sdatendingdateperiod9(), 
						clsServletUtilities.DATE_FORMAT_FOR_DISPLAY,
						clsServletUtilities.DATE_FORMAT_FOR_SQL, 
						clsServletUtilities.EMPTY_SQL_DATE_VALUE
					) + "'"				
				+ ", " + SMTableglfiscalperiods.datendingdateperiod10 + " = "
					+ "'" + ServletUtilities.clsDateAndTimeConversions.convertDateFormat(
						get_sdatendingdateperiod10(), 
						clsServletUtilities.DATE_FORMAT_FOR_DISPLAY,
						clsServletUtilities.DATE_FORMAT_FOR_SQL, 
						clsServletUtilities.EMPTY_SQL_DATE_VALUE
					) + "'"				
				+ ", " + SMTableglfiscalperiods.datendingdateperiod11 + " = "
					+ "'" + ServletUtilities.clsDateAndTimeConversions.convertDateFormat(
						get_sdatendingdateperiod11(), 
						clsServletUtilities.DATE_FORMAT_FOR_DISPLAY,
						clsServletUtilities.DATE_FORMAT_FOR_SQL, 
						clsServletUtilities.EMPTY_SQL_DATE_VALUE
					) + "'"				
				+ ", " + SMTableglfiscalperiods.datendingdateperiod12 + " = "
					+ "'" + ServletUtilities.clsDateAndTimeConversions.convertDateFormat(
						get_sdatendingdateperiod12(), 
						clsServletUtilities.DATE_FORMAT_FOR_DISPLAY,
						clsServletUtilities.DATE_FORMAT_FOR_SQL, 
						clsServletUtilities.EMPTY_SQL_DATE_VALUE
					) + "'"				
				+ ", " + SMTableglfiscalperiods.datendingdateperiod13 + " = "
					+ "'" + ServletUtilities.clsDateAndTimeConversions.convertDateFormat(
						get_sdatendingdateperiod13(), 
						clsServletUtilities.DATE_FORMAT_FOR_DISPLAY,
						clsServletUtilities.DATE_FORMAT_FOR_SQL, 
						clsServletUtilities.EMPTY_SQL_DATE_VALUE
					) + "'"				
				+ ", " + SMTableglfiscalperiods.iactive + " = " + get_siactive()
				+ ", " + SMTableglfiscalperiods.iclosed + " = " + get_siclosed()
				+ ", " + SMTableglfiscalperiods.ilockclosingperiod + " = " + get_silockclosingperiod()
				+ ", " + SMTableglfiscalperiods.ilasteditedbyuserid + " = " + get_slasteditedbyuserid()	
				+ ", " + SMTableglfiscalperiods.inumberofperiods + " = " + get_sinumberofperiods()
				+ ", " + SMTableglfiscalperiods.slasteditedbyfullusername + " = '" + clsDatabaseFunctions.FormatSQLStatement(get_slasteditedbyuserfullname()) + "'"
				+ ", " + SMTableglfiscalperiods.datlastediteddateandtime + " = '" + ServletUtilities.clsDateAndTimeConversions.convertDateFormat(
					get_sdattimelastedited(), 
					clsServletUtilities.DATETIME_FORMAT_FOR_DISPLAY,
					clsServletUtilities.DATETIME_FORMAT_FOR_SQL, 
					clsServletUtilities.EMPTY_SQL_DATETIME_VALUE
				) + "'"	
				+ ", " + SMTableglfiscalperiods.iperiod1locked + " = " + get_siperiod1locked()
				+ ", " + SMTableglfiscalperiods.iperiod2locked + " = " + get_siperiod2locked()
				+ ", " + SMTableglfiscalperiods.iperiod3locked + " = " + get_siperiod3locked()
				+ ", " + SMTableglfiscalperiods.iperiod4locked + " = " + get_siperiod4locked()
				+ ", " + SMTableglfiscalperiods.iperiod5locked + " = " + get_siperiod5locked()
				+ ", " + SMTableglfiscalperiods.iperiod6locked + " = " + get_siperiod6locked()
				+ ", " + SMTableglfiscalperiods.iperiod7locked + " = " + get_siperiod7locked()
				+ ", " + SMTableglfiscalperiods.iperiod8locked + " = " + get_siperiod8locked()
				+ ", " + SMTableglfiscalperiods.iperiod9locked + " = " + get_siperiod9locked()
				+ ", " + SMTableglfiscalperiods.iperiod10locked + " = " + get_siperiod10locked()
				+ ", " + SMTableglfiscalperiods.iperiod11locked + " = " + get_siperiod11locked()
				+ ", " + SMTableglfiscalperiods.iperiod12locked + " = " + get_siperiod12locked()
				+ ", " + SMTableglfiscalperiods.iperiod13locked + " = " + get_siperiod13locked()
				
				+ " WHERE (" + SMTableglfiscalperiods.ifiscalyear + "=" + get_sifiscalyear() + ")"
			;
		}

	 	try {
	 		Statement stmt = conn.createStatement();
	 		stmt.executeUpdate(SQL);
	 	}catch (SQLException e){
	 		throw new Exception("Error [1530901737] saving " + GLFiscalYear.ParamObjectName + " record - with SQL:" + SQL + " - " + e.getMessage());
	 	}
	 	
	 	//If it's a new fiscal year, then also add blank fiscal set and financial statement data for the new fiscal year:
	 	if (get_snewrecord().compareToIgnoreCase(ADDING_NEW_RECORD_PARAM_VALUE_TRUE) == 0){
	 		try {
				addBlankFiscalData(conn, get_sifiscalyear());
			} catch (Exception e) {
				throw new Exception("Error [20193531318295] " + " could not add fiscal set and financial statement data for fiscal year " + get_sifiscalyear()
					+ " - " + e.getMessage() + "."
				);
			}
	 	}

	 	set_snewrecord(ADDING_NEW_RECORD_PARAM_VALUE_FALSE);
	 	return;
    }
    private void addBlankFiscalData(Connection conn, String sFiscalYear) throws Exception{
    	
    	//First, get a recordset of all the GL accounts:
    	String SQL = "SELECT "
    		+ " " + SMTableglaccounts.sAcctID
    		+ " FROM " + SMTableglaccounts.TableName
    		+ " ORDER BY " + SMTableglaccounts.sAcctID
    	;
    	try {
			ResultSet rsGLAccts = ServletUtilities.clsDatabaseFunctions.openResultSet(SQL, conn);
			while (rsGLAccts.next()){
				//Insert a new fiscal set record here:
				SQL = "INSERT IGNORE INTO " + SMTableglfiscalsets.TableName
					+ " ("
					+ SMTableglfiscalsets.ifiscalyear
					+ ", " + SMTableglfiscalsets.sAcctID
					+ ") VALUES ("
					+ sFiscalYear
					+ ", '" + rsGLAccts.getString(SMTableglaccounts.sAcctID) + "'"
					+ ")"
				;
				try {
					Statement stmt = conn.createStatement();
					stmt.execute(SQL);
				} catch (Exception e) {
					rsGLAccts.close();
					throw new Exception("Error [20193531326426] " + "could not insert new fiscal set record with SQL: '" + SQL + "' - "
						+ " - " + e.getMessage() + "."
					);
				}
			}
			rsGLAccts.close();
		} catch (Exception e) {
			throw new Exception("Error [20193531327337] " + " - " + e.getMessage());
		}
    	
    	int iNumberOfPeriods;
		try {
			iNumberOfPeriods = Integer.parseInt(get_sinumberofperiods());
		} catch (Exception e1) {
			throw new Exception("Error [20193531333335] " + "Could not parse '" + get_sinumberofperiods() + "' into an integer - " + e1.getMessage() + ".");
		}
    	
    	//Now create the financial statement data:
    	SQL = "SELECT"
    		+ " " + SMTableglfiscalsets.sAcctID
    		+ ", " + SMTableglfiscalsets.ifiscalyear
    		+ " FROM " + SMTableglfiscalsets.TableName
    		+ " WHERE ("
    			+ "(" + SMTableglfiscalsets.ifiscalyear + " = " + sFiscalYear + ")"
    		+ ")"
    	;
    	try {
			ResultSet rsGLFiscalSets = ServletUtilities.clsDatabaseFunctions.openResultSet(SQL, conn);
			while (rsGLFiscalSets.next()){
				//Insert all the appropriate financial statement data records:
				for (int iPeriod = 1; iPeriod <= iNumberOfPeriods; iPeriod++){
					//Insert a financial statement record for each account, and each period:
					SQL = "INSERT IGNORE INTO " + SMTableglfinancialstatementdata.TableName
						+ "("
						+ SMTableglfinancialstatementdata.sacctid
						+ ", " + SMTableglfinancialstatementdata.ifiscalperiod
						+ ", " + SMTableglfinancialstatementdata.ifiscalyear
						+ ") VALUES ("
						+ "'" + rsGLFiscalSets.getString(SMTableglfiscalsets.sAcctID) + "'"
						+ ", " + Integer.toString(iPeriod)
						+ ", " + sFiscalYear
						+ ")"
					;
					try {
						Statement stmt = conn.createStatement();
						stmt.execute(SQL);
					} catch (Exception e) {
						rsGLFiscalSets.close();
						throw new Exception("Error [20193531326426] " + "could not insert new financial statement data record with SQL: '" + SQL + "' - "
							+ " - " + e.getMessage() + "."
						);
					}
				}
			}
			rsGLFiscalSets.close();
		} catch (Exception e) {
			throw new Exception("Error [20193531330486] " + " could not update financial statement data - " + e.getMessage() + ".");
		}
    	
    	return;
		
    }
    private void validateEntries(Connection conn) throws Exception{
    	
    	String s = "";
    	
    	boolean bAllowEmptyDateFor1stPeriod = Integer.parseInt(get_sinumberofperiods())<1;
    	try {
    		   set_sdatbeginningdateperiod1(clsValidateFormFields.validateStandardDateField(get_sdatbeginningdateperiod1(), "Beginning date period 1", bAllowEmptyDateFor1stPeriod));
    		   if(bAllowEmptyDateFor1stPeriod==true && clsValidateFormFields.validateStandardDateField(get_sdatbeginningdateperiod1(), "Beginning date period 1", bAllowEmptyDateFor1stPeriod).compareToIgnoreCase("00/00/0000")!=0) {
    		   			s+= "The Default Starting Value for Period 1 is not set to 00/00/0000 ";
    		  	}
		} catch (Exception e1) {
			s += e1.getMessage() + "\n";
		}
    	boolean bAllowEmptyDateFor2ndPeriod = Integer.parseInt(get_sinumberofperiods())<2;
    	try {
    		set_sdatbeginningdateperiod2(clsValidateFormFields.validateStandardDateField(get_sdatbeginningdateperiod2(), "Beginning date period 2", bAllowEmptyDateFor2ndPeriod));
    		if(bAllowEmptyDateFor2ndPeriod==true &&clsValidateFormFields.validateStandardDateField(get_sdatbeginningdateperiod2(), "Beginning date period 2", bAllowEmptyDateFor2ndPeriod).compareToIgnoreCase("00/00/0000")!=0) {
    			s+= "The Default Starting Value for Period 2 is not set to 00/00/0000 ";
    		}
		} catch (Exception e1) {
			s += e1.getMessage() + "\n";
		}    	
    	boolean bAllowEmptyDateFor3rdPeriod = Integer.parseInt(get_sinumberofperiods())<3;
    	try {
    		set_sdatbeginningdateperiod3(clsValidateFormFields.validateStandardDateField(get_sdatbeginningdateperiod3(), "Beginning date period 3", bAllowEmptyDateFor3rdPeriod));
    		if(bAllowEmptyDateFor3rdPeriod ==true&&clsValidateFormFields.validateStandardDateField(get_sdatbeginningdateperiod3(), "Beginning date period 3", bAllowEmptyDateFor3rdPeriod).compareToIgnoreCase("00/00/0000")!=0) {
    			s+= "The Default Starting Value for Period 3 is not set to 00/00/0000 ";
    		}
		} catch (Exception e1) {
			s += e1.getMessage() + "\n";
		}    	
    	boolean bAllowEmptyDateFor4thPeriod = Integer.parseInt(get_sinumberofperiods())<4;
    	try {
    		set_sdatbeginningdateperiod4(clsValidateFormFields.validateStandardDateField(get_sdatbeginningdateperiod4(), "Beginning date period 4", bAllowEmptyDateFor4thPeriod));
    		if(bAllowEmptyDateFor4thPeriod==true &&clsValidateFormFields.validateStandardDateField(get_sdatbeginningdateperiod4(), "Beginning date period  4", bAllowEmptyDateFor4thPeriod).compareToIgnoreCase("00/00/0000")!=0) {
    			s+= "The Default Starting Value for Period 4 is not set to 00/00/0000 ";
    		}
		} catch (Exception e1) {
			s += e1.getMessage() + "\n";
		}    	
    	boolean bAllowEmptyDateFor5thPeriod = Integer.parseInt(get_sinumberofperiods())<5;
    	try {
    		set_sdatbeginningdateperiod5(clsValidateFormFields.validateStandardDateField(get_sdatbeginningdateperiod5(), "Beginning date period 5", bAllowEmptyDateFor5thPeriod));
    		if(bAllowEmptyDateFor5thPeriod==true &&clsValidateFormFields.validateStandardDateField(get_sdatbeginningdateperiod5(), "Beginning date period 5", bAllowEmptyDateFor5thPeriod).compareToIgnoreCase("00/00/0000")!=0) {
    			s+= "The Default Starting Value for Period 5 is not set to 00/00/0000 ";
    		}
		} catch (Exception e1) {
			s += e1.getMessage() + "\n";
		}    	
    	boolean bAllowEmptyDateFor6thPeriod = Integer.parseInt(get_sinumberofperiods())<6;
    	try {
    		set_sdatbeginningdateperiod6(clsValidateFormFields.validateStandardDateField(get_sdatbeginningdateperiod6(), "Beginning date period 6", bAllowEmptyDateFor6thPeriod));
    		if(bAllowEmptyDateFor6thPeriod==true &&clsValidateFormFields.validateStandardDateField(get_sdatbeginningdateperiod6(), "Beginning date period 6", bAllowEmptyDateFor6thPeriod).compareToIgnoreCase("00/00/0000")!=0) {
    			s+= "The Default Starting Value for Period 6 is not set to 00/00/0000 ";
    		}
		} catch (Exception e1) {
			s += e1.getMessage() + "\n";
		}   
    	boolean bAllowEmptyDateFor7thPeriod = Integer.parseInt(get_sinumberofperiods())<7;
    	try {
    		set_sdatbeginningdateperiod7(clsValidateFormFields.validateStandardDateField(get_sdatbeginningdateperiod7(), "Beginning date period 7", bAllowEmptyDateFor7thPeriod));
    		if(bAllowEmptyDateFor7thPeriod==true &&clsValidateFormFields.validateStandardDateField(get_sdatbeginningdateperiod7(), "Beginning date period 7", bAllowEmptyDateFor7thPeriod).compareToIgnoreCase("00/00/0000")!=0) {
    			s+= "The Default Starting Value for Period 7 is not set to 00/00/0000 ";
    		}
		} catch (Exception e1) {
			s += e1.getMessage() + "\n";
		}    	
    	boolean bAllowEmptyDateFor8thPeriod = Integer.parseInt(get_sinumberofperiods())<8;
    	try {
    		set_sdatbeginningdateperiod8(clsValidateFormFields.validateStandardDateField(get_sdatbeginningdateperiod8(), "Beginning date period 8", bAllowEmptyDateFor8thPeriod));
    		if(bAllowEmptyDateFor8thPeriod==true &&clsValidateFormFields.validateStandardDateField(get_sdatbeginningdateperiod8(), "Beginning date period 8", bAllowEmptyDateFor8thPeriod).compareToIgnoreCase("00/00/0000")!=0) {
    			s+= "The Default Starting Value for Period 8 is not set to 00/00/0000 ";
    		}
		} catch (Exception e1) {
			s += e1.getMessage() + "\n";
		}    	
    	boolean bAllowEmptyDateFor9thPeriod = Integer.parseInt(get_sinumberofperiods())<9;
    	try {
    		set_sdatbeginningdateperiod9(clsValidateFormFields.validateStandardDateField(get_sdatbeginningdateperiod9(), "Beginning date period 9", bAllowEmptyDateFor9thPeriod));
    		if(bAllowEmptyDateFor9thPeriod==true &&clsValidateFormFields.validateStandardDateField(get_sdatbeginningdateperiod9(), "Beginning date period 9", bAllowEmptyDateFor9thPeriod).compareToIgnoreCase("00/00/0000")!=0) {
    			s+= "The Default Starting Value for Period 9 is not set to 00/00/0000 ";
    		}
		} catch (Exception e1) {
			s += e1.getMessage() + "\n";
		}    	
    	boolean bAllowEmptyDateFor10thPeriod = Integer.parseInt(get_sinumberofperiods())<10;
    	try {
    		set_sdatbeginningdateperiod10(clsValidateFormFields.validateStandardDateField(get_sdatbeginningdateperiod10(), "Beginning date period 10", bAllowEmptyDateFor10thPeriod));
    		if(bAllowEmptyDateFor10thPeriod==true &&clsValidateFormFields.validateStandardDateField(get_sdatbeginningdateperiod10(), "Beginning date period 10", bAllowEmptyDateFor10thPeriod).compareToIgnoreCase("00/00/0000")!=0) {
    			s+= "The Default Starting Value for Period 10 is not set to 00/00/0000 ";
    		}
		} catch (Exception e1) {
			s += e1.getMessage() + "\n";
		}    	
    	boolean bAllowEmptyDateFor11thPeriod = Integer.parseInt(get_sinumberofperiods())<11;
    	try {
    		set_sdatbeginningdateperiod11(clsValidateFormFields.validateStandardDateField(get_sdatbeginningdateperiod11(), "Beginning date period 11", bAllowEmptyDateFor11thPeriod));
    		if(bAllowEmptyDateFor11thPeriod==true &&clsValidateFormFields.validateStandardDateField(get_sdatbeginningdateperiod11(), "Beginning date period 11", bAllowEmptyDateFor11thPeriod).compareToIgnoreCase("00/00/0000")!=0) {
    			s+= "The Default Starting Value for Period 11 is not set to 00/00/0000 ";
    		}
		} catch (Exception e1) {
			s += e1.getMessage() + "\n";
		}    	
    	boolean bAllowEmptyDateFor12thPeriod = Integer.parseInt(get_sinumberofperiods())<12;
    	try {
    		set_sdatbeginningdateperiod12(clsValidateFormFields.validateStandardDateField(get_sdatbeginningdateperiod12(), "Beginning date period 12", bAllowEmptyDateFor12thPeriod));
    		if(bAllowEmptyDateFor12thPeriod &&clsValidateFormFields.validateStandardDateField(get_sdatbeginningdateperiod12(), "Beginning date period 12", bAllowEmptyDateFor12thPeriod).compareToIgnoreCase("00/00/0000")!=0) {
    			s+= "The Default Starting Value for Period 12 is not set to 00/00/0000 ";
    		}
		} catch (Exception e1) {
			s += e1.getMessage() + "\n";
		}    	
    	
    	//If this fiscal year has 13 periods, then we need to require a valid date for the 13th period
    	boolean bAllowEmptyDateFor13thPeriod = Integer.parseInt(get_sinumberofperiods())<13;
    	try {
    		set_sdatbeginningdateperiod13(clsValidateFormFields.validateStandardDateField(get_sdatbeginningdateperiod13(), "Beginning date period 13", bAllowEmptyDateFor13thPeriod));
    		if(bAllowEmptyDateFor13thPeriod &&clsValidateFormFields.validateStandardDateField(get_sdatbeginningdateperiod13(), "Beginning date period 13", bAllowEmptyDateFor13thPeriod).compareToIgnoreCase("00/00/0000")!=0) {
    			s+= "The Default Starting Value for Period 13 is not set to 00/00/0000  ";
    		}
		} catch (Exception e1) {
			s += e1.getMessage() + "\n";
		}   	
    	
    	try {
    		set_sdatendingdateperiod1(clsValidateFormFields.validateStandardDateField(get_sdatendingdateperiod1(), "Ending date period 1", bAllowEmptyDateFor1stPeriod));
    		 if(bAllowEmptyDateFor1stPeriod &&clsValidateFormFields.validateStandardDateField(get_sdatendingdateperiod1(), "Ending date period 1", bAllowEmptyDateFor1stPeriod).compareToIgnoreCase("00/00/0000")!=0) {
	    			s+= "The Default Ending Value for Period 1 is not set to 00/00/0000  ";
	    		}
		} catch (Exception e1) {
			s += e1.getMessage() + "\n";
		}
    	try {
    		set_sdatendingdateperiod2(clsValidateFormFields.validateStandardDateField(get_sdatendingdateperiod2(), "Ending date period 2", bAllowEmptyDateFor2ndPeriod));
    		if(bAllowEmptyDateFor2ndPeriod &&clsValidateFormFields.validateStandardDateField(get_sdatendingdateperiod2(), "Ending date period 2", bAllowEmptyDateFor2ndPeriod).compareToIgnoreCase("00/00/0000")!=0) {
    			s+= "The Default Ending Value for Period 2 is not set to 00/00/0000  ";
    		}
		} catch (Exception e1) {
			s += e1.getMessage() + "\n";
		}    	
    	try {
    		set_sdatendingdateperiod3(clsValidateFormFields.validateStandardDateField(get_sdatendingdateperiod3(), "Ending date period 3", bAllowEmptyDateFor3rdPeriod));
    		if(bAllowEmptyDateFor3rdPeriod &&clsValidateFormFields.validateStandardDateField(get_sdatendingdateperiod3(), "Ending date period 3", bAllowEmptyDateFor3rdPeriod).compareToIgnoreCase("00/00/0000")!=0) {
    			s+= "The Default Ending Value for Period 3 is not set to 00/00/0000 ";
    		}
		} catch (Exception e1) {
			s += e1.getMessage() + "\n";
		}    	
    	try {
    		set_sdatendingdateperiod4(clsValidateFormFields.validateStandardDateField(get_sdatendingdateperiod4(), "Ending date period 4", bAllowEmptyDateFor4thPeriod));
    		if(bAllowEmptyDateFor4thPeriod &&clsValidateFormFields.validateStandardDateField(get_sdatendingdateperiod4(), "Ending date period  4", bAllowEmptyDateFor4thPeriod).compareToIgnoreCase("00/00/0000")!=0) {
    			s+= "The Default Ending Value for Period 4 is not set to 00/00/0000 ";
    		}
		} catch (Exception e1) {
			s += e1.getMessage() + "\n";
		}    	
    	try {
    		set_sdatendingdateperiod5(clsValidateFormFields.validateStandardDateField(get_sdatendingdateperiod5(), "Ending date period 5", bAllowEmptyDateFor5thPeriod));
    		if(bAllowEmptyDateFor5thPeriod &&clsValidateFormFields.validateStandardDateField(get_sdatendingdateperiod5(), "Ending date period 5", bAllowEmptyDateFor5thPeriod).compareToIgnoreCase("00/00/0000")!=0) {
    			s+= "The Default Ending Value for Period 5 is not set to 00/00/0000 ";
    		}
		} catch (Exception e1) {
			s += e1.getMessage() + "\n";
		}    	
    	try {
    		set_sdatendingdateperiod6(clsValidateFormFields.validateStandardDateField(get_sdatendingdateperiod6(), "Ending date period 6", bAllowEmptyDateFor6thPeriod));
    		if(bAllowEmptyDateFor6thPeriod &&clsValidateFormFields.validateStandardDateField(get_sdatendingdateperiod6(), "Ending date period 6", bAllowEmptyDateFor6thPeriod).compareToIgnoreCase("00/00/0000")!=0) {
    			s+= "The Default Ending Value for Period 6 is not set to 00/00/0000 ";
    		}
		} catch (Exception e1) {
			s += e1.getMessage() + "\n";
		}    	
    	try {
    		set_sdatendingdateperiod7(clsValidateFormFields.validateStandardDateField(get_sdatendingdateperiod7(), "Ending date period 7", bAllowEmptyDateFor7thPeriod));
    		if(bAllowEmptyDateFor7thPeriod &&clsValidateFormFields.validateStandardDateField(get_sdatendingdateperiod7(), "Ending date period 7", bAllowEmptyDateFor7thPeriod).compareToIgnoreCase("00/00/0000")!=0) {
    			s+= "The Default Ending Value for Period 7 is not set to 00/00/0000 ";
    		}
		} catch (Exception e1) {
			s += e1.getMessage() + "\n";
		}    	
    	try {
    		set_sdatendingdateperiod8(clsValidateFormFields.validateStandardDateField(get_sdatendingdateperiod8(), "Ending date period 8", bAllowEmptyDateFor8thPeriod));
    		if(bAllowEmptyDateFor8thPeriod &&clsValidateFormFields.validateStandardDateField(get_sdatendingdateperiod8(), "Ending date period 8", bAllowEmptyDateFor8thPeriod).compareToIgnoreCase("00/00/0000")!=0) {
    			s+= "The Default Ending Value for Period 8 is not set to 00/00/0000 ";
    		}
		} catch (Exception e1) {
			s += e1.getMessage() + "\n";
		}    	
    	try {
    		set_sdatendingdateperiod9(clsValidateFormFields.validateStandardDateField(get_sdatendingdateperiod9(), "Ending date period 9", bAllowEmptyDateFor9thPeriod));
    		if(bAllowEmptyDateFor9thPeriod &&clsValidateFormFields.validateStandardDateField(get_sdatendingdateperiod9(), "Ending date period 9", bAllowEmptyDateFor9thPeriod).compareToIgnoreCase("00/00/0000")!=0) {
    			s+= "The Default Ending Value for Period 9 is not set to 00/00/0000 ";
    		}
		} catch (Exception e1) {
			s += e1.getMessage() + "\n";
		}    	
    	try {
    		set_sdatendingdateperiod10(clsValidateFormFields.validateStandardDateField(get_sdatendingdateperiod10(), "Ending date period 10", bAllowEmptyDateFor10thPeriod));
    		if(bAllowEmptyDateFor10thPeriod &&clsValidateFormFields.validateStandardDateField(get_sdatendingdateperiod10(), "Ending date period 10", bAllowEmptyDateFor10thPeriod).compareToIgnoreCase("00/00/0000")!=0) {
    			s+= "The Default Ending Value for Period 10 is not set to 00/00/0000 ";
    		}
		} catch (Exception e1) {
			s += e1.getMessage() + "\n";
		}    	
    	try {
    		set_sdatendingdateperiod11(clsValidateFormFields.validateStandardDateField(get_sdatendingdateperiod11(), "Ending date period 11", bAllowEmptyDateFor11thPeriod));
    		if(bAllowEmptyDateFor11thPeriod &&clsValidateFormFields.validateStandardDateField(get_sdatendingdateperiod11(), "Ending date period 11", bAllowEmptyDateFor11thPeriod).compareToIgnoreCase("00/00/0000")!=0) {
    			s+= "The Default Ending Value for Period 11 is not set to 00/00/0000 ";
    		}
		} catch (Exception e1) {
			s += e1.getMessage() + "\n";
		}    	
    	try {
    		set_sdatendingdateperiod12(clsValidateFormFields.validateStandardDateField(get_sdatendingdateperiod12(), "Ending date period 12", bAllowEmptyDateFor12thPeriod));
    		if(bAllowEmptyDateFor12thPeriod &&clsValidateFormFields.validateStandardDateField(get_sdatendingdateperiod12(), "Ending date period 12", bAllowEmptyDateFor12thPeriod).compareToIgnoreCase("00/00/0000")!=0) {
    			s+= "The Default Ending Value for Period 12 is not set to 00/00/0000 ";
    		}
		} catch (Exception e1) {
			s += e1.getMessage() + "\n";
		}    	
    	try {
    		set_sdatendingdateperiod13(clsValidateFormFields.validateStandardDateField(get_sdatendingdateperiod13(), "Ending date period 13", bAllowEmptyDateFor13thPeriod));
    		if(bAllowEmptyDateFor13thPeriod &&clsValidateFormFields.validateStandardDateField(get_sdatendingdateperiod13(), "Ending date period 13", bAllowEmptyDateFor13thPeriod).compareToIgnoreCase("00/00/0000")!=0) {
    			s+= "The Default Ending Value for Period 13 is not set to 00/00/0000 ";
    		}
		} catch (Exception e1) {
			s += e1.getMessage() + "\n";
		}   
    	
       	try {
			set_siactive(clsValidateFormFields.validateIntegerField(get_siactive(), "Active", 0, 1));
		} catch (Exception e1) {
			s += e1.getMessage() + "\n";
		}
       	
       	try {
			set_siclosed(clsValidateFormFields.validateIntegerField(get_siclosed(), "Closed", 0, 1));
		} catch (Exception e1) {
			s += e1.getMessage() + "\n";
		}

       	try {
			set_silockclosingperiod(clsValidateFormFields.validateIntegerField(get_silockclosingperiod(), "Lock closing period", 0, 1));
		} catch (Exception e1) {
			s += e1.getMessage() + "\n";
		}
       	
       	try {
			set_sifiscalyear(clsValidateFormFields.validateIntegerField(get_sifiscalyear(), "Fiscal year", 1990, 3000));
		} catch (Exception e1) {
			s += e1.getMessage() + "\n";
		}
       	try {
			set_slasteditedbyuserid(clsValidateFormFields.validateLongIntegerField(get_slasteditedbyuserid(), "Last edited by user ID", -1L, clsValidateFormFields.MAX_LONG_VALUE));
		} catch (Exception e1) {
			s += e1.getMessage() + "\n";
		}
       	try {
       		set_slasteditedbyuserfullname(clsValidateFormFields.validateStringField(
       			get_slasteditedbyuserfullname(),
       			SMTableglfiscalperiods.slasteditedbyfullusernameLength,
       			"Full name of user last editing",
       			false)
       		);
		} catch (Exception e1) {
			s += e1.getMessage() + "\n";
		}
       	
       	if (get_sdattimelastedited().compareToIgnoreCase("") == 0){
       		set_sdattimelastedited(clsServletUtilities.EMPTY_DATETIME_VALUE);
       	}
    	try {
    		set_sdattimelastedited(clsValidateFormFields.validateDateTimeField(get_sdattimelastedited(), "Time last edited", clsServletUtilities.DATETIME_FORMAT_FOR_DISPLAY, true));
		} catch (Exception e1) {
			s += e1.getMessage() + "\n";
		} 
       	try {
			set_sinumberofperiods(clsValidateFormFields.validateIntegerField(get_sinumberofperiods(), "Number of periods", 12, 13));
		} catch (Exception e1) {
			s += e1.getMessage() + "\n";
		}
  
       	try {
			set_siperiod1locked(clsValidateFormFields.validateIntegerField(get_siperiod1locked(), "Period 1 locked", 0, 1));
		} catch (Exception e1) {
			s += e1.getMessage() + "\n";
		}
       	try {
			set_siperiod2locked(clsValidateFormFields.validateIntegerField(get_siperiod2locked(), "Period 2 locked", 0, 1));
		} catch (Exception e1) {
			s += e1.getMessage() + "\n";
		}
       	try {
			set_siperiod3locked(clsValidateFormFields.validateIntegerField(get_siperiod3locked(), "Period 3 locked", 0, 1));
		} catch (Exception e1) {
			s += e1.getMessage() + "\n";
		}
       	try {
			set_siperiod4locked(clsValidateFormFields.validateIntegerField(get_siperiod4locked(), "Period 4 locked", 0, 1));
		} catch (Exception e1) {
			s += e1.getMessage() + "\n";
		}
       	try {
			set_siperiod5locked(clsValidateFormFields.validateIntegerField(get_siperiod5locked(), "Period 5 locked", 0, 1));
		} catch (Exception e1) {
			s += e1.getMessage() + "\n";
		}
       	try {
			set_siperiod6locked(clsValidateFormFields.validateIntegerField(get_siperiod6locked(), "Period6 locked", 0, 1));
		} catch (Exception e1) {
			s += e1.getMessage() + "\n";
		}
       	try {
			set_siperiod7locked(clsValidateFormFields.validateIntegerField(get_siperiod7locked(), "Period 7 locked", 0, 1));
		} catch (Exception e1) {
			s += e1.getMessage() + "\n";
		}
       	try {
			set_siperiod8locked(clsValidateFormFields.validateIntegerField(get_siperiod8locked(), "Period 8 locked", 0, 1));
		} catch (Exception e1) {
			s += e1.getMessage() + "\n";
		}
       	try {
			set_siperiod9locked(clsValidateFormFields.validateIntegerField(get_siperiod9locked(), "Period 9 locked", 0, 1));
		} catch (Exception e1) {
			s += e1.getMessage() + "\n";
		}
       	try {
			set_siperiod10locked(clsValidateFormFields.validateIntegerField(get_siperiod10locked(), "Period 10 locked", 0, 1));
		} catch (Exception e1) {
			s += e1.getMessage() + "\n";
		}
       	try {
			set_siperiod11locked(clsValidateFormFields.validateIntegerField(get_siperiod11locked(), "Period 11 locked", 0, 1));
		} catch (Exception e1) {
			s += e1.getMessage() + "\n";
		}
       	try {
			set_siperiod12locked(clsValidateFormFields.validateIntegerField(get_siperiod12locked(), "Period 12 locked", 0, 1));
		} catch (Exception e1) {
			s += e1.getMessage() + "\n";
		}
       	try {
			set_siperiod13locked(clsValidateFormFields.validateIntegerField(get_siperiod13locked(), "Period 13 locked", 0, 1));
		} catch (Exception e1) {
			s += e1.getMessage() + "\n";
		}
       	
       	try {
			validate_dates(conn);
		} catch (Exception e) {
			s += e.getMessage() + "";
		}
       	
     	m_snewrecord = m_snewrecord.trim();
     	if (
     		(m_snewrecord.compareToIgnoreCase(GLFiscalYear.ADDING_NEW_RECORD_PARAM_VALUE_FALSE) == 0)
     		|| (m_snewrecord.compareToIgnoreCase(GLFiscalYear.ADDING_NEW_RECORD_PARAM_VALUE_TRUE) == 0)
     	){
     	}else{
     		s += "New record flag '" + m_snewrecord + "' is invalid.";
     	}

     	if (s.compareToIgnoreCase("") != 0){
     		throw new Exception(s);
     	}
     	return;
    	
    }
    private void validate_dates(Connection conn) throws Exception{
    	String s = "";
    	
       	//Have to validate that dates don't cross existing dates, and that ending dates aren't earlier than starting dates 
       	// and that no two periods overlap:
    	
    	String SQL = "SELECT"
    		+ " *"
    		+ " FROM " + SMTableglfiscalperiods.TableName
    		+ " WHERE ("
    			+ "(" + SMTableglfiscalperiods.ifiscalyear + " < " + get_sifiscalyear() + ")"
    		+ ")"
    		+ " ORDER BY " + SMTableglfiscalperiods.ifiscalyear + " DESC LIMIT 1"
    	;
    	String sHighestPreviousEndingDate = "";
    	try {
			ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
			if (rs.next()){
				//System.out.println("[1531931647] - SQL = " + SQL);
				//System.out.println("[1531931648] sPeriod1BeginningDateAsSQL = '" + sPeriod1BeginningDateAsSQL + "', datendingdateperiod13 = '" + rs.getString(SMTableglfiscalperiods.datendingdateperiod13) + "'");
				
				
				switch(rs.getInt(SMTableglfiscalperiods.inumberofperiods)){
				case 1:
					sHighestPreviousEndingDate = rs.getString(SMTableglfiscalperiods.datendingdateperiod1);
					break;
				case 2:
					sHighestPreviousEndingDate = rs.getString(SMTableglfiscalperiods.datendingdateperiod2);
					break;
				case 3:
					sHighestPreviousEndingDate = rs.getString(SMTableglfiscalperiods.datendingdateperiod3);
					break;
				case 4:
					sHighestPreviousEndingDate = rs.getString(SMTableglfiscalperiods.datendingdateperiod4);
					break;
				case 5:
					sHighestPreviousEndingDate = rs.getString(SMTableglfiscalperiods.datendingdateperiod5);
					break;
				case 6:
					sHighestPreviousEndingDate = rs.getString(SMTableglfiscalperiods.datendingdateperiod6);
					break;
				case 7:
					sHighestPreviousEndingDate = rs.getString(SMTableglfiscalperiods.datendingdateperiod7);
					break;
				case 8:
					sHighestPreviousEndingDate = rs.getString(SMTableglfiscalperiods.datendingdateperiod8);
					break;
				case 9:
					sHighestPreviousEndingDate = rs.getString(SMTableglfiscalperiods.datendingdateperiod9);
					break;
				case 10:
					sHighestPreviousEndingDate = rs.getString(SMTableglfiscalperiods.datendingdateperiod10);
					break;
				case 11:
					sHighestPreviousEndingDate = rs.getString(SMTableglfiscalperiods.datendingdateperiod11);
					break;
				case 12:
					sHighestPreviousEndingDate = rs.getString(SMTableglfiscalperiods.datendingdateperiod12);
					break;
				case 13:
					sHighestPreviousEndingDate = rs.getString(SMTableglfiscalperiods.datendingdateperiod13);
					break;					
				default:
					sHighestPreviousEndingDate = "0000-00-00";
				}
				
				//System.out.println("[1531931649] - sHighestPreviousEndingDate = " + sHighestPreviousEndingDate);
				// This is not needed. This is done farther down so all errors are displayed at the same time.
			/*	if (
					sPeriod1BeginningDateAsSQL.compareToIgnoreCase(sHighestPreviousEndingDate) <= 0
				){
		    		Calendar LastYear  = Calendar.getInstance();
					LastYear.setTime(Date.valueOf(sHighestPreviousEndingDate));
					int year = LastYear.get(Calendar.YEAR);
		    		int day = LastYear.get(Calendar.DATE);
		    		int month = LastYear.get(Calendar.MONTH)+1;
		    		s += " The Ending date in Period 1 is overlaping with the previous Fiscal Years final Ending Date: "+month+"/"+day+"/" + year + ". \n";
					rs.close();
				} */
			}
			rs.close();
		} catch (Exception e) {
			s += "  Error [1531927995] checking beginning date - " + e.getMessage() + ".";
		}
    	
    	//Next make sure that all the dates are in order:
    	ArrayList <String>arrBeginningDates = new ArrayList<String>(SMTableglfiscalperiods.MAX_NUMBER_OF_EDITABLE_USER_PERIODS);
    	ArrayList <String>arrEndingDates = new ArrayList<String>(SMTableglfiscalperiods.MAX_NUMBER_OF_EDITABLE_USER_PERIODS);
    	
    	//Load all the dates:
    	
    	//Beginning dates:
    	arrBeginningDates.add(clsDateAndTimeConversions.convertDateFormat(
			get_sdatbeginningdateperiod1(), 
			clsServletUtilities.DATE_FORMAT_FOR_DISPLAY, 
			clsServletUtilities.DATE_FORMAT_FOR_SQL, 
			clsServletUtilities.EMPTY_SQL_DATE_VALUE)
    	);
    	arrBeginningDates.add(clsDateAndTimeConversions.convertDateFormat(
			get_sdatbeginningdateperiod2(), 
			clsServletUtilities.DATE_FORMAT_FOR_DISPLAY, 
			clsServletUtilities.DATE_FORMAT_FOR_SQL, 
			clsServletUtilities.EMPTY_SQL_DATE_VALUE)
    	);
    	arrBeginningDates.add(clsDateAndTimeConversions.convertDateFormat(
			get_sdatbeginningdateperiod3(), 
			clsServletUtilities.DATE_FORMAT_FOR_DISPLAY, 
			clsServletUtilities.DATE_FORMAT_FOR_SQL, 
			clsServletUtilities.EMPTY_SQL_DATE_VALUE)
    	);
    	arrBeginningDates.add(clsDateAndTimeConversions.convertDateFormat(
			get_sdatbeginningdateperiod4(), 
			clsServletUtilities.DATE_FORMAT_FOR_DISPLAY, 
			clsServletUtilities.DATE_FORMAT_FOR_SQL, 
			clsServletUtilities.EMPTY_SQL_DATE_VALUE)
    	);
    	arrBeginningDates.add(clsDateAndTimeConversions.convertDateFormat(
			get_sdatbeginningdateperiod5(), 
			clsServletUtilities.DATE_FORMAT_FOR_DISPLAY, 
			clsServletUtilities.DATE_FORMAT_FOR_SQL, 
			clsServletUtilities.EMPTY_SQL_DATE_VALUE)
    	);
    	arrBeginningDates.add(clsDateAndTimeConversions.convertDateFormat(
			get_sdatbeginningdateperiod6(), 
			clsServletUtilities.DATE_FORMAT_FOR_DISPLAY, 
			clsServletUtilities.DATE_FORMAT_FOR_SQL, 
			clsServletUtilities.EMPTY_SQL_DATE_VALUE)
    	);
    	arrBeginningDates.add(clsDateAndTimeConversions.convertDateFormat(
			get_sdatbeginningdateperiod7(), 
			clsServletUtilities.DATE_FORMAT_FOR_DISPLAY, 
			clsServletUtilities.DATE_FORMAT_FOR_SQL, 
			clsServletUtilities.EMPTY_SQL_DATE_VALUE)
    	);
    	arrBeginningDates.add(clsDateAndTimeConversions.convertDateFormat(
			get_sdatbeginningdateperiod8(), 
			clsServletUtilities.DATE_FORMAT_FOR_DISPLAY, 
			clsServletUtilities.DATE_FORMAT_FOR_SQL, 
			clsServletUtilities.EMPTY_SQL_DATE_VALUE)
    	);
    	arrBeginningDates.add(clsDateAndTimeConversions.convertDateFormat(
			get_sdatbeginningdateperiod9(), 
			clsServletUtilities.DATE_FORMAT_FOR_DISPLAY, 
			clsServletUtilities.DATE_FORMAT_FOR_SQL, 
			clsServletUtilities.EMPTY_SQL_DATE_VALUE)
    	);
    	arrBeginningDates.add(clsDateAndTimeConversions.convertDateFormat(
			get_sdatbeginningdateperiod10(), 
			clsServletUtilities.DATE_FORMAT_FOR_DISPLAY, 
			clsServletUtilities.DATE_FORMAT_FOR_SQL, 
			clsServletUtilities.EMPTY_SQL_DATE_VALUE)
    	);
    	arrBeginningDates.add(clsDateAndTimeConversions.convertDateFormat(
			get_sdatbeginningdateperiod11(), 
			clsServletUtilities.DATE_FORMAT_FOR_DISPLAY, 
			clsServletUtilities.DATE_FORMAT_FOR_SQL, 
			clsServletUtilities.EMPTY_SQL_DATE_VALUE)
    	);
    	arrBeginningDates.add(clsDateAndTimeConversions.convertDateFormat(
			get_sdatbeginningdateperiod12(), 
			clsServletUtilities.DATE_FORMAT_FOR_DISPLAY, 
			clsServletUtilities.DATE_FORMAT_FOR_SQL, 
			clsServletUtilities.EMPTY_SQL_DATE_VALUE)
    	);
    	arrBeginningDates.add(clsDateAndTimeConversions.convertDateFormat(
			get_sdatbeginningdateperiod13(), 
			clsServletUtilities.DATE_FORMAT_FOR_DISPLAY, 
			clsServletUtilities.DATE_FORMAT_FOR_SQL, 
			clsServletUtilities.EMPTY_SQL_DATE_VALUE)
    	);
    	
    	//Ending dates:
    	arrEndingDates.add(clsDateAndTimeConversions.convertDateFormat(
			get_sdatendingdateperiod1(), 
			clsServletUtilities.DATE_FORMAT_FOR_DISPLAY, 
			clsServletUtilities.DATE_FORMAT_FOR_SQL, 
			clsServletUtilities.EMPTY_SQL_DATE_VALUE)
    	);
    	arrEndingDates.add(clsDateAndTimeConversions.convertDateFormat(
			get_sdatendingdateperiod2(), 
			clsServletUtilities.DATE_FORMAT_FOR_DISPLAY, 
			clsServletUtilities.DATE_FORMAT_FOR_SQL, 
			clsServletUtilities.EMPTY_SQL_DATE_VALUE)
    	);
    	arrEndingDates.add(clsDateAndTimeConversions.convertDateFormat(
			get_sdatendingdateperiod3(), 
			clsServletUtilities.DATE_FORMAT_FOR_DISPLAY, 
			clsServletUtilities.DATE_FORMAT_FOR_SQL, 
			clsServletUtilities.EMPTY_SQL_DATE_VALUE)
    	);
    	arrEndingDates.add(clsDateAndTimeConversions.convertDateFormat(
			get_sdatendingdateperiod4(), 
			clsServletUtilities.DATE_FORMAT_FOR_DISPLAY, 
			clsServletUtilities.DATE_FORMAT_FOR_SQL, 
			clsServletUtilities.EMPTY_SQL_DATE_VALUE)
    	);
    	arrEndingDates.add(clsDateAndTimeConversions.convertDateFormat(
			get_sdatendingdateperiod5(), 
			clsServletUtilities.DATE_FORMAT_FOR_DISPLAY, 
			clsServletUtilities.DATE_FORMAT_FOR_SQL, 
			clsServletUtilities.EMPTY_SQL_DATE_VALUE)
    	);
    	arrEndingDates.add(clsDateAndTimeConversions.convertDateFormat(
			get_sdatendingdateperiod6(), 
			clsServletUtilities.DATE_FORMAT_FOR_DISPLAY, 
			clsServletUtilities.DATE_FORMAT_FOR_SQL, 
			clsServletUtilities.EMPTY_SQL_DATE_VALUE)
    	);
    	arrEndingDates.add(clsDateAndTimeConversions.convertDateFormat(
			get_sdatendingdateperiod7(), 
			clsServletUtilities.DATE_FORMAT_FOR_DISPLAY, 
			clsServletUtilities.DATE_FORMAT_FOR_SQL, 
			clsServletUtilities.EMPTY_SQL_DATE_VALUE)
    	);
    	arrEndingDates.add(clsDateAndTimeConversions.convertDateFormat(
			get_sdatendingdateperiod8(), 
			clsServletUtilities.DATE_FORMAT_FOR_DISPLAY, 
			clsServletUtilities.DATE_FORMAT_FOR_SQL, 
			clsServletUtilities.EMPTY_SQL_DATE_VALUE)
    	);
    	arrEndingDates.add(clsDateAndTimeConversions.convertDateFormat(
			get_sdatendingdateperiod9(), 
			clsServletUtilities.DATE_FORMAT_FOR_DISPLAY, 
			clsServletUtilities.DATE_FORMAT_FOR_SQL, 
			clsServletUtilities.EMPTY_SQL_DATE_VALUE)
    	);
    	arrEndingDates.add(clsDateAndTimeConversions.convertDateFormat(
			get_sdatendingdateperiod10(), 
			clsServletUtilities.DATE_FORMAT_FOR_DISPLAY, 
			clsServletUtilities.DATE_FORMAT_FOR_SQL, 
			clsServletUtilities.EMPTY_SQL_DATE_VALUE)
    	);
    	arrEndingDates.add(clsDateAndTimeConversions.convertDateFormat(
			get_sdatendingdateperiod11(), 
			clsServletUtilities.DATE_FORMAT_FOR_DISPLAY, 
			clsServletUtilities.DATE_FORMAT_FOR_SQL, 
			clsServletUtilities.EMPTY_SQL_DATE_VALUE)
    	);
    	arrEndingDates.add(clsDateAndTimeConversions.convertDateFormat(
			get_sdatendingdateperiod12(), 
			clsServletUtilities.DATE_FORMAT_FOR_DISPLAY, 
			clsServletUtilities.DATE_FORMAT_FOR_SQL, 
			clsServletUtilities.EMPTY_SQL_DATE_VALUE)
    	);
    	arrEndingDates.add(clsDateAndTimeConversions.convertDateFormat(
			get_sdatendingdateperiod13(), 
			clsServletUtilities.DATE_FORMAT_FOR_DISPLAY, 
			clsServletUtilities.DATE_FORMAT_FOR_SQL, 
			clsServletUtilities.EMPTY_SQL_DATE_VALUE)
    	);
    	
    	
    	try {
			for (int i = 0; i < Integer.parseInt(get_sinumberofperiods()); i++){
				//If period ending date is earlier or equal to period starting date:
				if(Date.valueOf(arrEndingDates.get(i)).before(Date.valueOf(arrBeginningDates.get(i)))){
					s += "  Starting date in Period " + (i+1) + " must be earlier than ending date in Period " + (i+1) + ". \t";
				}
			}
		} catch (Exception e) {
			throw new Exception("Error [1555698830] checking starting and ending dates - " + e.getMessage()+"\t");
		}
    	
    	/*This checks 3 cases:
    	 * Case1: the Starting date of the next period is before the ending date of the current period
    	 * Case2: the Ending date of the current period is not next to the beginning date of the next period (some dates are being passed over)
    	 * Case3: It checks to ensure that there are now missing days between the First period of this year and the Last day of the last period of the year before
    	 */
    	
    	int i = 0;
    	try {

    		for( i = 0; i  < Integer.parseInt(get_sinumberofperiods())-1; i++) {
    			Calendar EndingCalendar = Calendar.getInstance(); //Convert Date to Calendar for easy adding of date
    			Calendar BeginningCalendar = Calendar.getInstance();//Convert Date to Calendar for easy comparison of Calendars
    			Calendar LastYear  = Calendar.getInstance();
    			Calendar StartCalendar = Calendar.getInstance();

    			EndingCalendar.setTime(Date.valueOf(arrEndingDates.get(i)));
    			BeginningCalendar.setTime(Date.valueOf(arrBeginningDates.get(i+1)));
    			StartCalendar.setTime(Date.valueOf(arrBeginningDates.get(i)));
    			
    			//This is for the case if there is no previous year.
    			if (sHighestPreviousEndingDate == null || sHighestPreviousEndingDate.trim().isEmpty()) {
    				LastYear.setTime( Date.valueOf(arrBeginningDates.get(i)));
    				LastYear.add(Calendar.DATE, -1);
    			}else {
    				LastYear.setTime(Date.valueOf(sHighestPreviousEndingDate));
    			}

    			int year = LastYear.get(Calendar.YEAR);
    			int day = LastYear.get(Calendar.DATE);
    			int month = LastYear.get(Calendar.MONTH)+1;
    			EndingCalendar.add(Calendar.DATE, 1);//Make the End of the current month +1 so it will be the beginning of the next month.
    			LastYear.add(Calendar.DATE,1);//Make the end of the last year +1 so it will be the beginning of the first period.
    			if(StartCalendar.compareTo(LastYear)<0&&i==0) {
    				s += " The Ending date in Period 1 is overlaping with the previous Fiscal Years final Ending Date: "+month+"/"+day+"/" + year + ". \n";	
    				if(EndingCalendar.compareTo(BeginningCalendar)<0) {
    					s += "  There are missing days between Ending date in Period " +(i+1) + " and Beginning Date in Period " +(i+2) + ". \t";
    				}else if(EndingCalendar.compareTo(BeginningCalendar)>0) {
    					s += "  The Ending date in Period " +(i+1) + " is overlaping with Beginning Date in Period " +(i+2) + ". \t";
    				}
    			}else if(StartCalendar.compareTo(LastYear)>0&&i==0) {
    				s += " There are missing days between Ending Date in  Period " + (i+1) +  " and  previous Fiscal Years final Ending Date: "+month+"/"+day+"/" + year + ". \t";
    				if(EndingCalendar.compareTo(BeginningCalendar)<0) {
    					s += "  There are missing days between Ending date in Period " +(i+1) + " and Beginning Date in Period " +(i+2) + ". \t";
    				}else if(EndingCalendar.compareTo(BeginningCalendar)>0) {
    					s += "  The Ending date in Period " +(i+1) + " is overlaping with Beginning Date in Period " +(i+2) + ". \t";
    				}
    			}else if(EndingCalendar.compareTo(BeginningCalendar)<0){ //if The Ending day of the month +1 is not the Beginning Day of the next month, there will be an issue.
    				s += "  There are missing days between Ending date in Period " +(i+1) + " and Beginning Date in Period " +(i+2) + ". \t";
    			}else if(EndingCalendar.compareTo(BeginningCalendar)>0) {
    				s += "  The Ending date in Period " +(i+1) + " is overlaping with Beginning Date  in Period " +(i+2) + ". \t";
    			} 

    		}
    	}catch(Exception e) {
    		throw new Exception("Error [1557944615] checking starting and ending dates - " + e.getMessage() + ". At period " + (i+1) + " \t");
    	}

    	if (s.compareToIgnoreCase("") != 0){
    		throw new Exception(s);
    	}
    	return;
    }
	public void delete(String sFiscalYear, Connection conn) throws Exception{
		
		load(conn);
	
		String SQL = "SELECT"
			+ " " + SMTableglfiscalsets.ifiscalyear
			+ " FROM " + SMTableglfiscalsets.TableName
			+ " WHERE ("
				+ "(" + SMTableglfiscalsets.ifiscalyear + " = " + sFiscalYear + ")"
			+ ")"
		;
		boolean bFiscalSetsExist = false;
		ResultSet rsFiscalSets;
		try {
			rsFiscalSets = clsDatabaseFunctions.openResultSet(SQL, conn);
			if (rsFiscalSets.next()){
				bFiscalSetsExist = true;
			}
			rsFiscalSets.close();
		} catch (Exception e1) {
			throw new Exception("Error [20193371756157] " + "checking for existing fiscal sets with SQL: " 
				+ SQL + " - " + e1.getMessage());
		}
		
		if (bFiscalSetsExist){
			rsFiscalSets.close();
			throw new Exception("Error [20193371754126] " + "Fiscal sets with data already exist for this"
				+ " fiscal year, so it cannot be deleted.");
		}
		
		SQL = "DELETE FROM " + SMTableglfiscalperiods.TableName
			+ " WHERE ("
				+ "(" + SMTableglfiscalperiods.ifiscalyear + " = " + sFiscalYear + ")"
			+ ")"
		;
		try {
			Statement deletestmt = conn.createStatement();
			deletestmt.execute(SQL);
		} catch (Exception e) {
			throw new Exception("Error [1530906208] deleting fiscal year '" + sFiscalYear + "' - " + e.getMessage());
		}
	}
	public static int getCurrentFiscalYear(Connection conn) throws Exception{
		ServletUtilities.clsDBServerTime servertime = new ServletUtilities.clsDBServerTime(conn);
		String sCurrentDate = servertime.getCurrentDateTimeInSelectedFormat(ServletUtilities.clsServletUtilities.DATE_FORMAT_FOR_DISPLAY);
		return getFiscalYearForSelectedDate(
			sCurrentDate, 
			conn)
		;
	}
	public static int getFiscalYearForSelectedDate(
		String sDateInMMDDYYYYFormat, 
		Connection conn) throws Exception{
		
		int iFiscalYear = 0;
		String sDateInSQLFormat = ServletUtilities.clsDateAndTimeConversions.convertDateFormat(
			sDateInMMDDYYYYFormat, 
			clsServletUtilities.DATE_FORMAT_FOR_DISPLAY, 
			clsServletUtilities.DATE_FORMAT_FOR_SQL, 
			clsServletUtilities.EMPTY_SQL_DATE_VALUE);
		
		String sSQL = "SELECT"
			+ " " + SMTableglfiscalperiods.ifiscalyear
			+ " FROM " + SMTableglfiscalperiods.TableName
			+ " WHERE ("
			+ "(" + SMTableglfiscalperiods.datbeginningdateperiod1 + " <= '" + sDateInSQLFormat + "')"
			+ " AND IF(" + SMTableglfiscalperiods.inumberofperiods  + " = 13, " + SMTableglfiscalperiods.datbeginningdateperiod13
			+ " >= '" + sDateInSQLFormat + "', " + SMTableglfiscalperiods.datendingdateperiod12+ " >= '" + sDateInSQLFormat + "')"
			+ ")"
		;
		ResultSet rs = ServletUtilities.clsDatabaseFunctions.openResultSet(sSQL, conn);
		if (rs.next()){
			iFiscalYear = rs.getInt(SMTableglfiscalperiods.ifiscalyear);
			rs.close();
		}else{
			rs.close();
			throw new Exception("Error [1555700119] - no fiscal year found for current date: " + sDateInSQLFormat);
		}
		return iFiscalYear;		
	}
	public static int getFiscalPeriodForSelectedDate(		
		String sDateInMMDDYYYYFormat, 
		Connection conn) throws Exception{
		
		int iFiscalYear = 0;
		String sDateInSQLFormat = ServletUtilities.clsDateAndTimeConversions.convertDateFormat(
			sDateInMMDDYYYYFormat, 
			clsServletUtilities.DATE_FORMAT_FOR_DISPLAY, 
			clsServletUtilities.DATE_FORMAT_FOR_SQL, 
			clsServletUtilities.EMPTY_SQL_DATE_VALUE);		
		
		String sSQL = "SELECT"
				+ " " + SMTableglfiscalperiods.ifiscalyear
				+ " FROM " + SMTableglfiscalperiods.TableName
				+ " WHERE ("
					+ "(" + SMTableglfiscalperiods.datbeginningdateperiod1 + " <= '" + sDateInSQLFormat + "')"
					+ " AND IF(" + SMTableglfiscalperiods.inumberofperiods  + " = 13, " + SMTableglfiscalperiods.datendingdateperiod13
						+ " >= '" + sDateInSQLFormat + "', " + SMTableglfiscalperiods.datendingdateperiod12+ " >= '" + sDateInSQLFormat + "')"
				+ ")"
			;
			ResultSet rs = ServletUtilities.clsDatabaseFunctions.openResultSet(sSQL, conn);
			if (rs.next()){
				iFiscalYear = rs.getInt(SMTableglfiscalperiods.ifiscalyear);
				rs.close();
			}else{
				rs.close();
				throw new Exception("Error [1555700120] - no fiscal period found for date: " + sDateInSQLFormat);
			}
			GLFiscalYear period = new GLFiscalYear();
			period.set_sifiscalyear(Integer.toString(iFiscalYear));
			period.load(conn);
			
			//Now find the correct period:
			//System.out.println("[1555701768] beginning period date = '" + getSTDDateAsSQLDate(period.get_sdatbeginningdateperiod1()) + ", sCurrentDate = '" + sCurrentDate + "'.");
			if (
				(getSTDDateAsSQLDate(period.get_sdatbeginningdateperiod1()).compareToIgnoreCase(sDateInSQLFormat) <= 0)
				&& (getSTDDateAsSQLDate(period.get_sdatendingdateperiod1()).compareToIgnoreCase(sDateInSQLFormat) >= 0)
			){
				return 1;
			}
			if (
					(getSTDDateAsSQLDate(period.get_sdatbeginningdateperiod2()).compareToIgnoreCase(sDateInSQLFormat) <= 0)
					&& (getSTDDateAsSQLDate(period.get_sdatendingdateperiod2()).compareToIgnoreCase(sDateInSQLFormat) >= 0)
				){
					return 2;
				}
			if (
					(getSTDDateAsSQLDate(period.get_sdatbeginningdateperiod3()).compareToIgnoreCase(sDateInSQLFormat) <= 0)
					&& (getSTDDateAsSQLDate(period.get_sdatendingdateperiod3()).compareToIgnoreCase(sDateInSQLFormat) >= 0)
				){
					return 3;
				}
			if (
					(getSTDDateAsSQLDate(period.get_sdatbeginningdateperiod4()).compareToIgnoreCase(sDateInSQLFormat) <= 0)
					&& (getSTDDateAsSQLDate(period.get_sdatendingdateperiod4()).compareToIgnoreCase(sDateInSQLFormat) >= 0)
				){
					return 4;
				}
			if (
					(getSTDDateAsSQLDate(period.get_sdatbeginningdateperiod5()).compareToIgnoreCase(sDateInSQLFormat) <= 0)
					&& (getSTDDateAsSQLDate(period.get_sdatendingdateperiod5()).compareToIgnoreCase(sDateInSQLFormat) >= 0)
				){
					return 5;
				}
			if (
					(getSTDDateAsSQLDate(period.get_sdatbeginningdateperiod6()).compareToIgnoreCase(sDateInSQLFormat) <= 0)
					&& (getSTDDateAsSQLDate(period.get_sdatendingdateperiod6()).compareToIgnoreCase(sDateInSQLFormat) >= 0)
				){
					return 6;
				}
			if (
					(getSTDDateAsSQLDate(period.get_sdatbeginningdateperiod7()).compareToIgnoreCase(sDateInSQLFormat) <= 0)
					&& (getSTDDateAsSQLDate(period.get_sdatendingdateperiod7()).compareToIgnoreCase(sDateInSQLFormat) >= 0)
				){
					return 7;
				}
			if (
					(getSTDDateAsSQLDate(period.get_sdatbeginningdateperiod8()).compareToIgnoreCase(sDateInSQLFormat) <= 0)
					&& (getSTDDateAsSQLDate(period.get_sdatendingdateperiod8()).compareToIgnoreCase(sDateInSQLFormat) >= 0)
				){
					return 8;
				}
			if (
					(getSTDDateAsSQLDate(period.get_sdatbeginningdateperiod9()).compareToIgnoreCase(sDateInSQLFormat) <= 0)
					&& (getSTDDateAsSQLDate(period.get_sdatendingdateperiod9()).compareToIgnoreCase(sDateInSQLFormat) >= 0)
				){
					return 9;
				}
			if (
					(getSTDDateAsSQLDate(period.get_sdatbeginningdateperiod10()).compareToIgnoreCase(sDateInSQLFormat) <= 0)
					&& (getSTDDateAsSQLDate(period.get_sdatendingdateperiod10()).compareToIgnoreCase(sDateInSQLFormat) >= 0)
				){
					return 10;
				}
			if (
					(getSTDDateAsSQLDate(period.get_sdatbeginningdateperiod11()).compareToIgnoreCase(sDateInSQLFormat) <= 0)
					&& (getSTDDateAsSQLDate(period.get_sdatendingdateperiod11()).compareToIgnoreCase(sDateInSQLFormat) >= 0)
				){
					return 11;
				}
			if (
					(getSTDDateAsSQLDate(period.get_sdatbeginningdateperiod12()).compareToIgnoreCase(sDateInSQLFormat) <= 0)
					&& (getSTDDateAsSQLDate(period.get_sdatendingdateperiod12()).compareToIgnoreCase(sDateInSQLFormat) >= 0)
				){
					return 12;
				}
			if (
					(getSTDDateAsSQLDate(period.get_sdatbeginningdateperiod13()).compareToIgnoreCase(sDateInSQLFormat) <= 0)
					&& (getSTDDateAsSQLDate(period.get_sdatendingdateperiod13()).compareToIgnoreCase(sDateInSQLFormat) >= 0)
				){
					return 13;
				}
			
			return 0;
	}
	public static int getCurrentFiscalPeriod(Connection conn) throws Exception{
		ServletUtilities.clsDBServerTime servertime = new ServletUtilities.clsDBServerTime(conn);
		String sCurrentDate = servertime.getCurrentDateTimeInSelectedFormat(ServletUtilities.clsServletUtilities.DATE_FORMAT_FOR_DISPLAY);
		return getFiscalPeriodForSelectedDate(sCurrentDate, conn);
	}
	
	public static String getNextFiscalPeriod(String sFiscalYear, String sFiscalPeriod, Connection conn)
		throws Exception{
		
		GLFiscalYear fycurrent = new GLFiscalYear();
		fycurrent.set_sifiscalyear(sFiscalYear);
		try {
			fycurrent.load(conn);
		} catch (Exception e) {
			throw new Exception("Error [1559329380] loading fiscal year " + sFiscalYear + "' - " + e.getMessage());
		}
		//If it's the last period of the year, then we have to get the next fiscal year and use the first period:
		if (fycurrent.get_sinumberofperiods().compareToIgnoreCase(sFiscalPeriod) == 0){
			return "1";
		}else{
			return Integer.toString((Integer.parseInt(sFiscalPeriod) + 1));
		}
	}
	public static String getNextFiscalYear(String sFiscalYear, String sFiscalPeriod, Connection conn)
			throws Exception{
			
			GLFiscalYear fycurrent = new GLFiscalYear();
			fycurrent.set_sifiscalyear(sFiscalYear);
			try {
				fycurrent.load(conn);
			} catch (Exception e) {
				throw new Exception("Error [1559329381] loading fiscal year " + sFiscalYear + "' - " + e.getMessage());
			}
			//If it's the last period of the year, then we have to get the next fiscal year:
			if (fycurrent.get_sinumberofperiods().compareToIgnoreCase(sFiscalPeriod) == 0){
				return Integer.toString((Integer.parseInt(sFiscalYear) + 1));
			}else{
				return sFiscalYear;
			}
		}
	
	public static String getLatestUnlockedFiscalYearAndPeriod(
			ServletContext context,
			String sDBID,
			String sClassName,
			String sUserID,
			String sUserFullName
		) throws Exception{
		
		//This function will return a single string, with the fiscal year first, followed by a hyphen, then the
		// fiscal period after the hyphen.  For example: '2019-4'
		String sYearAndPeriod = "";
		String sDelimiter = " - ";
		
		//Get a list of the fiscal years, then get the latest unlocked fiscal period:
		String sSQL = "SELECT * FROM " + SMTableglfiscalperiods.TableName
			+ " ORDER BY " + SMTableglfiscalperiods.ifiscalyear + " DESC"
		;
		ResultSet rs = ServletUtilities.clsDatabaseFunctions.openResultSet(
				sSQL, 
				context, 
				sDBID,
				"MySQL",
				sClassName + " - User: " + sUserID
				+ " - "
				+ sUserFullName);
		while (rs.next()){
			int iNumberOfPeriods = rs.getInt(SMTableglfiscalperiods.inumberofperiods );

				if (rs.getInt(SMTableglfiscalperiods.iperiod13locked) == 0){
					if (iNumberOfPeriods >= 13){
						sYearAndPeriod = Integer.toString(rs.getInt(SMTableglfiscalperiods.ifiscalyear)) + sDelimiter + "13";
						break;
					}
				}
				if (rs.getInt(SMTableglfiscalperiods.iperiod12locked) == 0){
					if (iNumberOfPeriods >= 12){
						sYearAndPeriod = Integer.toString(rs.getInt(SMTableglfiscalperiods.ifiscalyear)) + sDelimiter + "12";
						break;
					}
				}
				if (rs.getInt(SMTableglfiscalperiods.iperiod11locked) == 0){
					if (iNumberOfPeriods >= 11){
						sYearAndPeriod = Integer.toString(rs.getInt(SMTableglfiscalperiods.ifiscalyear)) + sDelimiter + "11";
						break;
					}
				}
				if (rs.getInt(SMTableglfiscalperiods.iperiod10locked) == 0){
					if (iNumberOfPeriods >= 10){
						sYearAndPeriod = Integer.toString(rs.getInt(SMTableglfiscalperiods.ifiscalyear)) + sDelimiter + "10";
						break;
					}
				}
				if (rs.getInt(SMTableglfiscalperiods.iperiod9locked) == 0){
					if (iNumberOfPeriods >= 9){
						sYearAndPeriod = Integer.toString(rs.getInt(SMTableglfiscalperiods.ifiscalyear)) + sDelimiter + "9";
						break;
					}
				}
				if (rs.getInt(SMTableglfiscalperiods.iperiod8locked) == 0){
					if (iNumberOfPeriods >= 8){
						sYearAndPeriod = Integer.toString(rs.getInt(SMTableglfiscalperiods.ifiscalyear)) + sDelimiter + "8";
						break;
					}
				}
				if (rs.getInt(SMTableglfiscalperiods.iperiod7locked) == 0){
					if (iNumberOfPeriods >= 7){
						sYearAndPeriod = Integer.toString(rs.getInt(SMTableglfiscalperiods.ifiscalyear)) +  sDelimiter + "7";
						break;
					}
				}
				if (rs.getInt(SMTableglfiscalperiods.iperiod6locked) == 0){
					if (iNumberOfPeriods >= 6){
						sYearAndPeriod = Integer.toString(rs.getInt(SMTableglfiscalperiods.ifiscalyear)) +  sDelimiter + "6";
						break;
					}
				}
				if (rs.getInt(SMTableglfiscalperiods.iperiod5locked) == 0){
					if (iNumberOfPeriods >= 5){
						sYearAndPeriod = Integer.toString(rs.getInt(SMTableglfiscalperiods.ifiscalyear)) +  sDelimiter + "5";
						break;
					}
				}
				if (rs.getInt(SMTableglfiscalperiods.iperiod4locked) == 0){
					if (iNumberOfPeriods >= 4){
						sYearAndPeriod = Integer.toString(rs.getInt(SMTableglfiscalperiods.ifiscalyear)) +  sDelimiter + "4";
						break;
					}
				}
				if (rs.getInt(SMTableglfiscalperiods.iperiod3locked) == 0){
					if (iNumberOfPeriods >= 3){
						sYearAndPeriod = Integer.toString(rs.getInt(SMTableglfiscalperiods.ifiscalyear)) +  sDelimiter + "3";
						break;
					}
				}
				if (rs.getInt(SMTableglfiscalperiods.iperiod2locked) == 0){
					if (iNumberOfPeriods >= 2){
						sYearAndPeriod = Integer.toString(rs.getInt(SMTableglfiscalperiods.ifiscalyear)) +  sDelimiter + "2";
						break;
					}
				}
				if (rs.getInt(SMTableglfiscalperiods.iperiod1locked) == 0){
					if (iNumberOfPeriods >= 1){
						sYearAndPeriod = Integer.toString(rs.getInt(SMTableglfiscalperiods.ifiscalyear)) +  sDelimiter + "1";
						break;
					}
				}
		}
		rs.close();
		
		if (sYearAndPeriod.compareToIgnoreCase("") == 0){
			throw new Exception("Error [1559850334] - could not find an unlocked fiscal period.");
		}
		return sYearAndPeriod;
	}
	
	public final boolean isPeriodLocked(String sFiscalYear, int iPeriod, Connection conn) throws Exception{
		String sPeriodIsLocked = "1";
		
		//Load the fiscal year first:
		set_sifiscalyear(sFiscalYear);
		try {
			load(conn);
		} catch (Exception e) {
			throw new Exception("Error [2019193817296] " + "Could not load fiscal year " + sFiscalYear + " - " + e.getMessage() + ".");
		}
		
		switch (iPeriod){
		case 1:
			sPeriodIsLocked = get_siperiod1locked();
			break;
		case 2:
			sPeriodIsLocked = get_siperiod2locked();
			break;
		case 3:
			sPeriodIsLocked = get_siperiod3locked();
			break;
		case 4:
			sPeriodIsLocked = get_siperiod4locked();
			break;
		case 5:
			sPeriodIsLocked = get_siperiod5locked();
			break;
		case 6:
			sPeriodIsLocked = get_siperiod6locked();
			break;
		case 7:
			sPeriodIsLocked = get_siperiod7locked();
			break;
		case 8:
			sPeriodIsLocked = get_siperiod8locked();
			break;
		case 9:
			sPeriodIsLocked = get_siperiod9locked();
			break;
		case 10:
			sPeriodIsLocked = get_siperiod10locked();
			break;
		case 11:
			sPeriodIsLocked = get_siperiod11locked();
			break;
		case 12:
			sPeriodIsLocked = get_siperiod12locked();
			break;
		case 13:
			sPeriodIsLocked = get_siperiod13locked();
			break;
		case 15:
			sPeriodIsLocked = get_silockclosingperiod();
			break;
		default:
			throw new Exception("Error [2019190160302] " + "period '" + Integer.toString(iPeriod) + "' is not valid.");
		}
		
		if (sPeriodIsLocked.compareToIgnoreCase("1") == 0){
			return true;
		}else{
			return false;
		}
	}
	
	public void NextYear() throws Exception {
		int periods = Integer.parseInt(get_sinumberofperiods());
		if(periods>=1) {
		set_sdatbeginningdateperiod1(addYear(get_sdatbeginningdateperiod1()));
		set_sdatendingdateperiod1(addYear(get_sdatendingdateperiod1()));
		}
		if(periods>=2) {
		set_sdatbeginningdateperiod2(addYear(get_sdatbeginningdateperiod2()));
		set_sdatendingdateperiod2(addYear(get_sdatendingdateperiod2()));
		}
		if(periods>=3) {
		set_sdatbeginningdateperiod3(addYear(get_sdatbeginningdateperiod3()));
		set_sdatendingdateperiod3(addYear(get_sdatendingdateperiod3()));
		}
		if(periods>=4) {
		set_sdatbeginningdateperiod4(addYear(get_sdatbeginningdateperiod4()));
		set_sdatendingdateperiod4(addYear(get_sdatendingdateperiod4()));
		}
		if(periods>=5) {
		set_sdatbeginningdateperiod5(addYear(get_sdatbeginningdateperiod5()));
		set_sdatendingdateperiod5(addYear(get_sdatendingdateperiod5()));
		}
		if(periods>=6) {
		set_sdatbeginningdateperiod6(addYear(get_sdatbeginningdateperiod6()));
		set_sdatendingdateperiod6(addYear(get_sdatendingdateperiod6()));
		}
		if(periods>=7) {
		set_sdatbeginningdateperiod7(addYear(get_sdatbeginningdateperiod7()));
		set_sdatendingdateperiod7(addYear(get_sdatendingdateperiod7()));
		}
		if(periods>=8) {
		set_sdatbeginningdateperiod8(addYear(get_sdatbeginningdateperiod8()));
		set_sdatendingdateperiod8(addYear(get_sdatendingdateperiod8()));
		}
		if(periods>=9) {
		set_sdatbeginningdateperiod9(addYear(get_sdatbeginningdateperiod9()));
		set_sdatendingdateperiod9(addYear(get_sdatendingdateperiod9()));
		}
		if(periods>=10) {
		set_sdatbeginningdateperiod10(addYear(get_sdatbeginningdateperiod10()));
		set_sdatendingdateperiod10(addYear(get_sdatendingdateperiod10()));
		}
		if(periods>=11) {
		set_sdatbeginningdateperiod11(addYear(get_sdatbeginningdateperiod11()));
		set_sdatendingdateperiod11(addYear(get_sdatendingdateperiod11()));
		}
		if(periods>=12) {
		set_sdatbeginningdateperiod12(addYear(get_sdatbeginningdateperiod12()));
		set_sdatendingdateperiod12(addYear(get_sdatendingdateperiod12()));
		}	
		if(periods>=13) {
		set_sdatbeginningdateperiod13(addYear(get_sdatbeginningdateperiod13()));
		set_sdatendingdateperiod13(addYear(get_sdatendingdateperiod13()));
		}	


	}

	public String addYear(String date) throws Exception {
		String temp=clsDateAndTimeConversions.convertDateFormat(
				date, 
				clsServletUtilities.DATE_FORMAT_FOR_DISPLAY, 
				clsServletUtilities.DATE_FORMAT_FOR_SQL, 
				clsServletUtilities.EMPTY_SQL_DATE_VALUE);
		Calendar TempCalendar = Calendar.getInstance(); //Convert Date to Calendar for easy adding of date
		TempCalendar.setTime(Date.valueOf(temp));
		GregorianCalendar leapYear = new GregorianCalendar();
		if(TempCalendar.get(Calendar.MONTH)==1 && TempCalendar.get(Calendar.DAY_OF_MONTH) == 29) {
			TempCalendar.set(Calendar.DAY_OF_MONTH, 28);
		}
		TempCalendar.set(Calendar.YEAR, TempCalendar.get(Calendar.YEAR)+1);
		if( 
				(leapYear.isLeapYear(TempCalendar.get(Calendar.YEAR)))
				&& (TempCalendar.get(Calendar.MONTH)==1) 
				&& (TempCalendar.get(Calendar.DAY_OF_MONTH)==28) ) 
		{
			TempCalendar.set(Calendar.DAY_OF_MONTH, 29);
		}
		
		java.util.Date convert = TempCalendar.getTime();
		DateFormat dateFormat = new SimpleDateFormat(clsServletUtilities.DATE_FORMAT_FOR_SQL);
		temp = clsDateAndTimeConversions.convertDateFormat(
				dateFormat.format(convert), 
				clsServletUtilities.DATE_FORMAT_FOR_SQL, 
				clsServletUtilities.DATE_FORMAT_FOR_DISPLAY, 
				clsServletUtilities.EMPTY_SQL_DATE_VALUE);
		return temp;
	}
	
	public void set_sdatbeginningdateperiod1(String sDate) {
		m_sdatbeginningdateperiod1 = sDate;
	}
	public void set_sdatbeginningdateperiod2(String sDate) {
		m_sdatbeginningdateperiod2 = sDate;
	}
	public void set_sdatbeginningdateperiod3(String sDate) {
		m_sdatbeginningdateperiod3 = sDate;
	}
	public void set_sdatbeginningdateperiod4(String sDate) {
		m_sdatbeginningdateperiod4 = sDate;
	}
	public void set_sdatbeginningdateperiod5(String sDate) {
		m_sdatbeginningdateperiod5 = sDate;
	}
	public void set_sdatbeginningdateperiod6(String sDate) {
		m_sdatbeginningdateperiod6 = sDate;
	}
	public void set_sdatbeginningdateperiod7(String sDate) {
		m_sdatbeginningdateperiod7 = sDate;
	}
	public void set_sdatbeginningdateperiod8(String sDate) {
		m_sdatbeginningdateperiod8 = sDate;
	}
	public void set_sdatbeginningdateperiod9(String sDate) {
		m_sdatbeginningdateperiod9 = sDate;
	}
	public void set_sdatbeginningdateperiod10(String sDate) {
		m_sdatbeginningdateperiod10 = sDate;
	}
	public void set_sdatbeginningdateperiod11(String sDate) {
		m_sdatbeginningdateperiod11 = sDate;
	}
	public void set_sdatbeginningdateperiod12(String sDate) {
		m_sdatbeginningdateperiod12 = sDate;
	}
	public void set_sdatbeginningdateperiod13(String sDate) {
		m_sdatbeginningdateperiod13 = sDate;
	}
	public void set_sdatendingdateperiod1(String sDate) {
		m_sdatendingdateperiod1 = sDate;
	}
	public void set_sdatendingdateperiod2(String sDate) {
		m_sdatendingdateperiod2 = sDate;
	}
	public void set_sdatendingdateperiod3(String sDate) {
		m_sdatendingdateperiod3 = sDate;
	}
	public void set_sdatendingdateperiod4(String sDate) {
		m_sdatendingdateperiod4 = sDate;
	}
	public void set_sdatendingdateperiod5(String sDate) {
		m_sdatendingdateperiod5 = sDate;
	}
	public void set_sdatendingdateperiod6(String sDate) {
		m_sdatendingdateperiod6 = sDate;
	}
	public void set_sdatendingdateperiod7(String sDate) {
		m_sdatendingdateperiod7 = sDate;
	}
	public void set_sdatendingdateperiod8(String sDate) {
		m_sdatendingdateperiod8 = sDate;
	}
	public void set_sdatendingdateperiod9(String sDate) {
		m_sdatendingdateperiod9 = sDate;
	}
	public void set_sdatendingdateperiod10(String sDate) {
		m_sdatendingdateperiod10 = sDate;
	}
	public void set_sdatendingdateperiod11(String sDate) {
		m_sdatendingdateperiod11 = sDate;
	}
	public void set_sdatendingdateperiod12(String sDate) {
		m_sdatendingdateperiod12 = sDate;
	}
	public void set_sdatendingdateperiod13(String sDate) {
		m_sdatendingdateperiod13 = sDate;
	}
	public void set_sifiscalyear(String sFiscalYear) {
		m_sifiscalyear = sFiscalYear;
	}
	public void set_slasteditedbyuserid(String sUserID) {
		m_silasteditedbyuserid = sUserID;
	}
	public void set_slasteditedbyuserfullname(String sLastEditedByFullName) {
		m_slasteditedbyfullusername = sLastEditedByFullName;
	}
	public void set_sdattimelastedited(String sDatTimeLastEdited) {
		m_silastediteddatetime = sDatTimeLastEdited;
	}
	public void set_sinumberofperiods(String sNumberOfPeriods) {
		m_sinumberofperiods = sNumberOfPeriods;
	}
	public void set_siactive(String sActive) {
		m_siactive = sActive;
	}
	public void set_siclosed(String sClosed) {
		m_siclosed = sClosed;
	}
	public void set_silockclosingperiod(String sLockClosingPeriod) {
		m_silockclosingperiod = sLockClosingPeriod;
	}
	public void set_siperiod1locked(String sPeriod1Locked) {
		m_speriod1locked = sPeriod1Locked;
	}
	public void set_siperiod2locked(String sPeriod2Locked) {
		m_speriod2locked = sPeriod2Locked;
	}
	public void set_siperiod3locked(String sPeriod3Locked) {
		m_speriod3locked = sPeriod3Locked;
	}
	public void set_siperiod4locked(String sPeriod4Locked) {
		m_speriod4locked = sPeriod4Locked;
	}
	public void set_siperiod5locked(String sPeriod5Locked) {
		m_speriod5locked = sPeriod5Locked;
	}
	public void set_siperiod6locked(String sPeriod6Locked) {
		m_speriod6locked = sPeriod6Locked;
	}
	public void set_siperiod7locked(String sPeriod7Locked) {
		m_speriod7locked = sPeriod7Locked;
	}
	public void set_siperiod8locked(String sPeriod8Locked) {
		m_speriod8locked = sPeriod8Locked;
	}
	public void set_siperiod9locked(String sPeriod9Locked) {
		m_speriod9locked = sPeriod9Locked;
	}
	public void set_siperiod10locked(String sPeriod10Locked) {
		m_speriod10locked = sPeriod10Locked;
	}
	public void set_siperiod11locked(String sPeriod11Locked) {
		m_speriod11locked = sPeriod11Locked;
	}
	public void set_siperiod12locked(String sPeriod12Locked) {
		m_speriod12locked = sPeriod12Locked;
	}
	public void set_siperiod13locked(String sPeriod13Locked) {
		m_speriod13locked = sPeriod13Locked;
	}
	
	public String get_sdatbeginningdateperiod1() {
		return m_sdatbeginningdateperiod1;
	}
	public String get_sdatbeginningdateperiod2() {
		return m_sdatbeginningdateperiod2;
	}
	public String get_sdatbeginningdateperiod3() {
		return m_sdatbeginningdateperiod3;
	}
	public String get_sdatbeginningdateperiod4() {
		return m_sdatbeginningdateperiod4;
	}
	public String get_sdatbeginningdateperiod5() {
		return m_sdatbeginningdateperiod5;
	}
	public String get_sdatbeginningdateperiod6() {
		return m_sdatbeginningdateperiod6;
	}
	public String get_sdatbeginningdateperiod7() {
		return m_sdatbeginningdateperiod7;
	}
	public String get_sdatbeginningdateperiod8() {
		return m_sdatbeginningdateperiod8;
	}
	public String get_sdatbeginningdateperiod9() {
		return m_sdatbeginningdateperiod9;
	}
	public String get_sdatbeginningdateperiod10() {
		return m_sdatbeginningdateperiod10;
	}
	public String get_sdatbeginningdateperiod11() {
		return m_sdatbeginningdateperiod11;
	}
	public String get_sdatbeginningdateperiod12() {
		return m_sdatbeginningdateperiod12;
	}
	public String get_sdatbeginningdateperiod13() {
		return m_sdatbeginningdateperiod13;
	}
	public String get_sdatendingdateperiod1() {
		return m_sdatendingdateperiod1;
	}
	public String get_sdatendingdateperiod2() {
		return m_sdatendingdateperiod2;
	}
	public String get_sdatendingdateperiod3() {
		return m_sdatendingdateperiod3;
	}
	public String get_sdatendingdateperiod4() {
		return m_sdatendingdateperiod4;
	}
	public String get_sdatendingdateperiod5() {
		return m_sdatendingdateperiod5;
	}
	public String get_sdatendingdateperiod6() {
		return m_sdatendingdateperiod6;
	}
	public String get_sdatendingdateperiod7() {
		return m_sdatendingdateperiod7;
	}
	public String get_sdatendingdateperiod8() {
		return m_sdatendingdateperiod8;
	}
	public String get_sdatendingdateperiod9() {
		return m_sdatendingdateperiod9;
	}
	public String get_sdatendingdateperiod10() {
		return m_sdatendingdateperiod10;
	}
	public String get_sdatendingdateperiod11() {
		return m_sdatendingdateperiod11;
	}
	public String get_sdatendingdateperiod12() {
		return m_sdatendingdateperiod12;
	}
	public String get_sdatendingdateperiod13() {
		return m_sdatendingdateperiod13;
	}
	public String get_sifiscalyear() {
		return m_sifiscalyear;
	}
	public String get_slasteditedbyuserid() {
		return m_silasteditedbyuserid;
	}
	public String get_slasteditedbyuserfullname() {
		return m_slasteditedbyfullusername;
	}
	public String get_sdattimelastedited(){
		return m_silastediteddatetime;
	}
	public String get_sinumberofperiods() {
		return m_sinumberofperiods;
	}
	public String get_siactive() {
		return m_siactive;
	}
	public String get_siclosed() {
		return m_siclosed;
	}
	public String get_silockclosingperiod() {
		return m_silockclosingperiod;
	}
	public String get_siperiod1locked() {
		return m_speriod1locked;
	}
	public String get_siperiod2locked() {
		return m_speriod2locked;
	}
	public String get_siperiod3locked() {
		return m_speriod3locked;
	}
	public String get_siperiod4locked() {
		return m_speriod4locked;
	}
	public String get_siperiod5locked() {
		return m_speriod5locked;
	}
	public String get_siperiod6locked() {
		return m_speriod6locked;
	}
	public String get_siperiod7locked() {
		return m_speriod7locked;
	}
	public String get_siperiod8locked() {
		return m_speriod8locked;
	}
	public String get_siperiod9locked() {
		return m_speriod9locked;
	}
	public String get_siperiod10locked() {
		return m_speriod10locked;
	}
	public String get_siperiod11locked() {
		return m_speriod11locked;
	}
	public String get_siperiod12locked() {
		return m_speriod12locked;
	}
	public String get_siperiod13locked() {
		return m_speriod13locked;
	}

	public void set_snewrecord(String snewrecord){
		m_snewrecord = snewrecord;
	}
	public String get_snewrecord(){
		return m_snewrecord;
	}
	private static String getSTDDateAsSQLDate(String sStdDate) throws Exception{
		return ServletUtilities.clsDateAndTimeConversions.convertDateFormat(
			sStdDate, 
			ServletUtilities.clsServletUtilities.DATE_FORMAT_FOR_DISPLAY, 
			ServletUtilities.clsServletUtilities.DATE_FORMAT_FOR_SQL, 
			ServletUtilities.clsServletUtilities.EMPTY_SQL_DATE_VALUE
		);
	}
}