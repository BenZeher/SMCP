package smgl;

import java.sql.Connection;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import SMDataDefinition.SMTableglfiscalperiods;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsDateAndTimeConversions;
import ServletUtilities.clsManageRequestParameters;
import ServletUtilities.clsServletUtilities;
import ServletUtilities.clsValidateFormFields;
import smcontrolpanel.SMUtilities;

public class GLFiscalYear extends java.lang.Object{

	public static final String ParamObjectName = "Fiscal period";
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
	public String m_silockadjustmentperiod;
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
		m_silockadjustmentperiod = "0";
		m_silockclosingperiod = "0";
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
		if (clsManageRequestParameters.get_Request_Parameter(SMTableglfiscalperiods.ilockadjustmentperiod, req).compareToIgnoreCase("") == 0){
			m_silockadjustmentperiod = "0";
		}else{
			m_silockadjustmentperiod = "1";
		}
		if (clsManageRequestParameters.get_Request_Parameter(SMTableglfiscalperiods.ilockclosingperiod, req).compareToIgnoreCase("") == 0){
			m_silockclosingperiod = "0";
		}else{
			m_silockclosingperiod = "1";
		}
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
				m_silockadjustmentperiod = Integer.toString(rs.getInt(SMTableglfiscalperiods.ilockadjustmentperiod));
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
    public void save(ServletContext context, String sDBIB, String sUserID, String sUserFullName) throws Exception{
		
    	//Set the user ID and full name first:
    	set_slasteditedbyuserid(sUserID);
    	set_slasteditedbyuserfullname(sUserFullName);
    	
    	//Get connection
		Connection conn;
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
		
		//Validate entries
		try {
			validateEntries(conn);
		} catch (Exception e1) {
			clsDatabaseFunctions.freeConnection(context, conn, "[1547080752]");
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
				+ ", " + SMTableglfiscalperiods.ifiscalyear
				+ ", " + SMTableglfiscalperiods.ilasteditedbyuserid
				+ ", " + SMTableglfiscalperiods.ilockadjustmentperiod
				+ ", " + SMTableglfiscalperiods.ilockclosingperiod
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
			+ ", " + get_sifiscalyear()
			+ ", " + get_slasteditedbyuserid()
			+ ", " + get_silockadjustmentperiod()
			+ ", " + get_silockclosingperiod()
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
				+ ", " + SMTableglfiscalperiods.ilasteditedbyuserid + " = " + get_slasteditedbyuserid()	
				+ ", " + SMTableglfiscalperiods.ilockadjustmentperiod + " = " + get_silockadjustmentperiod()
				+ ", " + SMTableglfiscalperiods.ilockclosingperiod + " = " + get_silockclosingperiod()
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
	 		clsDatabaseFunctions.freeConnection(context, conn, "[1547080753]");
	 		throw new Exception("Error [1530901737] saving " + GLFiscalYear.ParamObjectName + " record - with SQL:" + SQL + " - " + e.getMessage());
	 	}

	 	set_snewrecord(ADDING_NEW_RECORD_PARAM_VALUE_FALSE);
	 	clsDatabaseFunctions.freeConnection(context, conn, "[1547080754]");
    }
    private void validateEntries(Connection conn) throws Exception{
    	
    	String s = "";
    	
    	try {
    		set_sdatbeginningdateperiod1(clsValidateFormFields.validateStandardDateField(get_sdatbeginningdateperiod1(), "Beginning date period 1", false));
		} catch (Exception e1) {
			s += e1.getMessage() + "\n";
		}
    	try {
    		set_sdatbeginningdateperiod2(clsValidateFormFields.validateStandardDateField(get_sdatbeginningdateperiod2(), "Beginning date period 2", false));
		} catch (Exception e1) {
			s += e1.getMessage() + "\n";
		}    	
    	try {
    		set_sdatbeginningdateperiod3(clsValidateFormFields.validateStandardDateField(get_sdatbeginningdateperiod3(), "Beginning date period 3", false));
		} catch (Exception e1) {
			s += e1.getMessage() + "\n";
		}    	
    	try {
    		set_sdatbeginningdateperiod4(clsValidateFormFields.validateStandardDateField(get_sdatbeginningdateperiod4(), "Beginning date period 4", false));
		} catch (Exception e1) {
			s += e1.getMessage() + "\n";
		}    	
    	try {
    		set_sdatbeginningdateperiod5(clsValidateFormFields.validateStandardDateField(get_sdatbeginningdateperiod5(), "Beginning date period 5", false));
		} catch (Exception e1) {
			s += e1.getMessage() + "\n";
		}    	
    	try {
    		set_sdatbeginningdateperiod6(clsValidateFormFields.validateStandardDateField(get_sdatbeginningdateperiod6(), "Beginning date period 6", false));
		} catch (Exception e1) {
			s += e1.getMessage() + "\n";
		}    	
    	try {
    		set_sdatbeginningdateperiod7(clsValidateFormFields.validateStandardDateField(get_sdatbeginningdateperiod7(), "Beginning date period 7", false));
		} catch (Exception e1) {
			s += e1.getMessage() + "\n";
		}    	
    	try {
    		set_sdatbeginningdateperiod8(clsValidateFormFields.validateStandardDateField(get_sdatbeginningdateperiod8(), "Beginning date period 8", false));
		} catch (Exception e1) {
			s += e1.getMessage() + "\n";
		}    	
    	try {
    		set_sdatbeginningdateperiod9(clsValidateFormFields.validateStandardDateField(get_sdatbeginningdateperiod9(), "Beginning date period 9", false));
		} catch (Exception e1) {
			s += e1.getMessage() + "\n";
		}    	
    	try {
    		set_sdatbeginningdateperiod10(clsValidateFormFields.validateStandardDateField(get_sdatbeginningdateperiod10(), "Beginning date period 10", false));
		} catch (Exception e1) {
			s += e1.getMessage() + "\n";
		}    	
    	try {
    		set_sdatbeginningdateperiod11(clsValidateFormFields.validateStandardDateField(get_sdatbeginningdateperiod11(), "Beginning date period 11", false));
		} catch (Exception e1) {
			s += e1.getMessage() + "\n";
		}    	
    	try {
    		set_sdatbeginningdateperiod12(clsValidateFormFields.validateStandardDateField(get_sdatbeginningdateperiod12(), "Beginning date period 12", false));
		} catch (Exception e1) {
			s += e1.getMessage() + "\n";
		}    	
    	
    	//If this fiscal year has 13 periods, then we need to require a valid date for the 13th period
    	boolean bAllowEmptyDateFor13thPeriod = get_sinumberofperiods().compareTo("13") != 0;
    	try {
    		set_sdatbeginningdateperiod13(clsValidateFormFields.validateStandardDateField(get_sdatbeginningdateperiod13(), "Beginning date period 13", bAllowEmptyDateFor13thPeriod));
		} catch (Exception e1) {
			s += e1.getMessage() + "\n";
		}   	
    	
    	try {
    		set_sdatendingdateperiod1(clsValidateFormFields.validateStandardDateField(get_sdatendingdateperiod1(), "Ending date period 1", false));
		} catch (Exception e1) {
			s += e1.getMessage() + "\n";
		}
    	try {
    		set_sdatendingdateperiod2(clsValidateFormFields.validateStandardDateField(get_sdatendingdateperiod2(), "Ending date period 2", false));
		} catch (Exception e1) {
			s += e1.getMessage() + "\n";
		}    	
    	try {
    		set_sdatendingdateperiod3(clsValidateFormFields.validateStandardDateField(get_sdatendingdateperiod3(), "Ending date period 3", false));
		} catch (Exception e1) {
			s += e1.getMessage() + "\n";
		}    	
    	try {
    		set_sdatendingdateperiod4(clsValidateFormFields.validateStandardDateField(get_sdatendingdateperiod4(), "Ending date period 4", false));
		} catch (Exception e1) {
			s += e1.getMessage() + "\n";
		}    	
    	try {
    		set_sdatendingdateperiod5(clsValidateFormFields.validateStandardDateField(get_sdatendingdateperiod5(), "Ending date period 5", false));
		} catch (Exception e1) {
			s += e1.getMessage() + "\n";
		}    	
    	try {
    		set_sdatendingdateperiod6(clsValidateFormFields.validateStandardDateField(get_sdatendingdateperiod6(), "Ending date period 6", false));
		} catch (Exception e1) {
			s += e1.getMessage() + "\n";
		}    	
    	try {
    		set_sdatendingdateperiod7(clsValidateFormFields.validateStandardDateField(get_sdatendingdateperiod7(), "Ending date period 7", false));
		} catch (Exception e1) {
			s += e1.getMessage() + "\n";
		}    	
    	try {
    		set_sdatendingdateperiod8(clsValidateFormFields.validateStandardDateField(get_sdatendingdateperiod8(), "Ending date period 8", false));
		} catch (Exception e1) {
			s += e1.getMessage() + "\n";
		}    	
    	try {
    		set_sdatendingdateperiod9(clsValidateFormFields.validateStandardDateField(get_sdatendingdateperiod9(), "Ending date period 9", false));
		} catch (Exception e1) {
			s += e1.getMessage() + "\n";
		}    	
    	try {
    		set_sdatendingdateperiod10(clsValidateFormFields.validateStandardDateField(get_sdatendingdateperiod10(), "Ending date period 10", false));
		} catch (Exception e1) {
			s += e1.getMessage() + "\n";
		}    	
    	try {
    		set_sdatendingdateperiod11(clsValidateFormFields.validateStandardDateField(get_sdatendingdateperiod11(), "Ending date period 11", false));
		} catch (Exception e1) {
			s += e1.getMessage() + "\n";
		}    	
    	try {
    		set_sdatendingdateperiod12(clsValidateFormFields.validateStandardDateField(get_sdatendingdateperiod12(), "Ending date period 12", false));
		} catch (Exception e1) {
			s += e1.getMessage() + "\n";
		}    	
    	try {
    		set_sdatendingdateperiod13(clsValidateFormFields.validateStandardDateField(get_sdatendingdateperiod13(), "Ending date period 13", bAllowEmptyDateFor13thPeriod));
		} catch (Exception e1) {
			s += e1.getMessage() + "\n";
		}   
    	
       	try {
			set_siactive(clsValidateFormFields.validateIntegerField(get_siactive(), "Active", 0, 1));
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
			set_silockadjustmentperiod(clsValidateFormFields.validateIntegerField(get_silockadjustmentperiod(), "Lock adjustment period", 0, 1));
		} catch (Exception e1) {
			s += e1.getMessage() + "\n";
		}
       	try {
			set_silockclosingperiod(clsValidateFormFields.validateIntegerField(get_silockclosingperiod(), "Lock closing period", 0, 1));
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
    	
    	//First: make sure the first period beginning date is later than any EXISTING dates:
		String sPeriod1BeginningDateAsSQL = clsDateAndTimeConversions.convertDateFormat(
				get_sdatbeginningdateperiod1(), 
				clsServletUtilities.DATE_FORMAT_FOR_DISPLAY, 
				clsServletUtilities.DATE_FORMAT_FOR_SQL, 
				clsServletUtilities.EMPTY_SQL_DATE_VALUE
			);
    	String SQL = "SELECT"
    		+ " *"
    		+ " FROM " + SMTableglfiscalperiods.TableName
    		+ " WHERE ("
    			+ "(" + SMTableglfiscalperiods.ifiscalyear + " < " + get_sifiscalyear() + ")"
    		+ ")"
    		+ " ORDER BY " + SMTableglfiscalperiods.ifiscalyear + " DESC LIMIT 1"
    	;
    	try {
			ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
			if (rs.next()){
				//System.out.println("[1531931647] - SQL = " + SQL);
				//System.out.println("[1531931648] sPeriod1BeginningDateAsSQL = '" + sPeriod1BeginningDateAsSQL + "', datendingdateperiod13 = '" + rs.getString(SMTableglfiscalperiods.datendingdateperiod13) + "'");
				
				String sHighestPreviousEndingDate = "";
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
					sHighestPreviousEndingDate = "9999-99-99";
				}
				
				//System.out.println("[1531931649] - sHighestPreviousEndingDate = " + sHighestPreviousEndingDate);
				
				if (
					sPeriod1BeginningDateAsSQL.compareToIgnoreCase(sHighestPreviousEndingDate) <= 0
				){
					s += "  Fiscal period 1 is earlier than the last fiscal period in fiscal year " 
						+ Long.toString(rs.getLong(SMTableglfiscalperiods.ifiscalyear)) + ".";
					rs.close();
				}
			}
			rs.close();
		} catch (Exception e) {
			s += "  Error [1531927995] checking beginning date - " + e.getMessage() + ".";
		}
    	
    	//Next make sure that all the dates are in order:
    	ArrayList <String>arrBeginningDates = new ArrayList<String>(SMTableglfiscalperiods.MAX_NUMBER_OF_PERIODS);
    	ArrayList <String>arrEndingDates = new ArrayList<String>(SMTableglfiscalperiods.MAX_NUMBER_OF_PERIODS);
    	
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
					s += "  Starting date in period " + i + " must be earlier than ending date in period " + i + ".";
				}
				
				//If one period is not immediately AFTER the previous one:
				if (i > 1){
					//clsDateAndTimeConversions.
					//if (){
					//	
					//}
				}
				
				//If the first to the last doesn't comprise a complete year:
			}
		} catch (Exception e) {
			throw new Exception("Error [1555698830] checking starting and ending dates - " + e.getMessage());
		}
    	
    	//If period 2 starting date is not one day after period 1 ending date:
    	
    	
    	//Now make sure there aren't any skipped DAYS between periods:
    	
    	
    	//Also, if there are fewer than 13 periods being used, make sure that the unused periods get default dates in them:
    	
    	
    	if (s.compareToIgnoreCase("") != 0){
    		throw new Exception(s);
    	}
    	return;
    }
	public void delete(String sFiscalYear, Connection conn) throws Exception{
		
		load(conn);
	
		//Now delete the cost center:
		String SQL = "DELETE FROM " + SMTableglfiscalperiods.TableName
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
					+ " >= '" + sDateInSQLFormat + "', " + SMTableglfiscalperiods.datbeginningdateperiod12+ " >= '" + sDateInSQLFormat + "')"
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
					+ " AND IF(" + SMTableglfiscalperiods.inumberofperiods  + " = 13, " + SMTableglfiscalperiods.datbeginningdateperiod13
						+ " >= '" + sDateInSQLFormat + "', " + SMTableglfiscalperiods.datbeginningdateperiod12+ " >= '" + sDateInSQLFormat + "')"
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
	public void set_silockadjustmentperiod(String sLockAdjustmentPeriod) {
		m_silockadjustmentperiod = sLockAdjustmentPeriod;
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
	public String get_silockadjustmentperiod() {
		return m_silockadjustmentperiod;
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