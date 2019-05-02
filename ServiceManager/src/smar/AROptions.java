package smar;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import SMDataDefinition.SMTablearoptions;
import ServletUtilities.clsServletUtilities;
import smcontrolpanel.SMUtilities;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsManageRequestParameters;

public class AROptions extends java.lang.Object{
	
	public static final String ParamBatchPostingInProcess = "BatchPostingInProcess";
	public static final String ParamPostingUserFullName = "PostingUserFullName";
	public static final String ParamPostingProcess = "PostingProcess";
	public static final String ParamPostingStartDate = "PostingStartDate";
	public static final String Paramiexportto = "iexportto";
	public static final String Paramienforcecreditlimit = "ienforcecreditlimit";
	public static final String Paramsgdrivecustomerparentfolderid = "gdrivecustomerparentfolderid";
	public static final String Paramsgdrivecustomerfolderprefix = "gdrivecustomerfolderprefix";
	public static final String Paramsgdrivecustomerfoldersuffix = "gdrivecustomerfoldersuffix";
	public static final String Paramifeedgl = "ifeedgl";
	
	private String m_sBatchPostingInProcess;
	private String m_sPostingUserFullName;
	private String m_sPostingProcess;
	private String m_sPostingStartDate;
	private String m_sExportTo;
	private String m_ifeedgl;
	private String m_senforcecreditlimit;
	private ArrayList<String> m_sErrorMessageArray = new ArrayList<String> (0);
	private String m_sgdrivecustomerparentfolderid;
	private String m_sgdrivecustomerfolderprefix;
	private String m_sgdrivecustomerfoldersuffix;
	

	public AROptions(){
		m_sBatchPostingInProcess = "0";
		m_sPostingUserFullName = "";
		m_sPostingProcess = "";
		m_sPostingStartDate = "00/00/0000 00:00:00";
		m_sExportTo = "0";
		m_ifeedgl = "0";
		m_senforcecreditlimit = "0";
		m_sgdrivecustomerparentfolderid = "";
		m_sgdrivecustomerfolderprefix = "";
		m_sgdrivecustomerfoldersuffix = "";
		m_sErrorMessageArray = new ArrayList<String> (0);
	}
	
	public AROptions (HttpServletRequest req){

		m_sBatchPostingInProcess = clsManageRequestParameters.get_Request_Parameter(AROptions.ParamBatchPostingInProcess, req).trim();
		m_sPostingUserFullName = clsManageRequestParameters.get_Request_Parameter(AROptions.ParamPostingUserFullName, req).trim();
		m_sPostingProcess = clsManageRequestParameters.get_Request_Parameter(AROptions.ParamPostingProcess, req).trim();
		m_sPostingStartDate = clsManageRequestParameters.get_Request_Parameter(AROptions.ParamPostingStartDate, req).trim();
		m_sExportTo = clsManageRequestParameters.get_Request_Parameter(AROptions.Paramiexportto, req).trim();
		m_ifeedgl = clsManageRequestParameters.get_Request_Parameter(AROptions.Paramifeedgl, req).trim();
		if (req.getParameter(AROptions.Paramienforcecreditlimit) == null){
			m_senforcecreditlimit = "0";
		}else{
			m_senforcecreditlimit = "1";
		}
		m_sErrorMessageArray = new ArrayList<String> (0);
		m_sgdrivecustomerparentfolderid = clsManageRequestParameters.get_Request_Parameter(AROptions.Paramsgdrivecustomerparentfolderid, req).trim();
		m_sgdrivecustomerfolderprefix = clsManageRequestParameters.get_Request_Parameter(AROptions.Paramsgdrivecustomerfolderprefix, req).trim();
		m_sgdrivecustomerfoldersuffix = clsManageRequestParameters.get_Request_Parameter(AROptions.Paramsgdrivecustomerfoldersuffix, req).trim();
	}
	
    public boolean load (
        	Connection conn
        ){
        	String SQL = "SELECT * FROM " + SMTablearoptions.TableName;
        	try {
        		ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
        		if (rs.next()){
        			m_sBatchPostingInProcess 
        				= Long.toString(rs.getLong(SMTablearoptions.ibatchpostinginprocess));
        			m_sPostingUserFullName = rs.getString(SMTablearoptions.suserfullname);
        			m_sPostingProcess = rs.getString(SMTablearoptions.sprocess);
        			m_sPostingStartDate = rs.getString(SMTablearoptions.datstartdate);
        			m_sExportTo = rs.getString(SMTablearoptions.iexportto);
        			m_ifeedgl = Long.toString(rs.getLong(SMTablearoptions.ifeedgl));
        			m_senforcecreditlimit = Long.toString(rs.getLong(SMTablearoptions.ienforcecreditlimit));
        			m_sgdrivecustomerparentfolderid = rs.getString(SMTablearoptions.gdrivecustomersparentfolderid);
        			m_sgdrivecustomerfolderprefix = rs.getString(SMTablearoptions.gdrivecustomersderfolderprefix);
        			m_sgdrivecustomerfoldersuffix = rs.getString(SMTablearoptions.gdrivecustomersfoldersuffix);
        			
        			rs.close();
        			return true;
        		}else{
        			rs.close();
        			SQL = "INSERT INTO " + SMTablearoptions.TableName 
        			+ " (" +  SMTablearoptions.ibatchpostinginprocess 
        			+ ", " + SMTablearoptions.suserfullname 
        			+ ", " + SMTablearoptions.sprocess 
        			+ ", " + SMTablearoptions.datstartdate 
        			+ ", " + SMTablearoptions.iexportto //5
        			+ ", " + SMTablearoptions.ienforcecreditlimit
        			+ ", " + SMTablearoptions.ifeedgl
        			+ ", " + SMTablearoptions.gdrivecustomersparentfolderid 
        			+ ", " + SMTablearoptions.gdrivecustomersderfolderprefix 
        			+ ", " + SMTablearoptions.gdrivecustomersfoldersuffix //9
		
       				+ ") VALUES(" 
       				+      m_sBatchPostingInProcess
       				+ ", '" + clsDatabaseFunctions.FormatSQLStatement(m_sPostingUserFullName) + "'"
       				+ ", '" + m_sPostingProcess + "'"
       				+ ", '" + m_sPostingStartDate + "'"
       				+ ", "  + m_sExportTo  //5
       				+ ", "  + m_senforcecreditlimit 
       				+ ", "  + m_ifeedgl
       				+ ", '" + m_sgdrivecustomerparentfolderid + "'"
        			+ ", '" + m_sgdrivecustomerfolderprefix + "'"
        			+ ", '" + m_sgdrivecustomerfoldersuffix + "'" //9
       				+ ")"
        			;
        			try {
        				Statement stmt = conn.createStatement();
        				stmt.executeUpdate(SQL);
        			}catch (SQLException e){
        				m_sErrorMessageArray.add("Error updating record with SQL: " + SQL + " - " + e.getMessage());
        				return false;
        				}
        			return true;
        		}
        	}catch (SQLException e){
        		m_sErrorMessageArray.add("SQL Error reading AR options record: " + e.getMessage());
        		return false;
        	}
    	}
    
    public void load(String sDBID, ServletContext context, String sUser) throws Exception{
    	Connection conn;
    	try {
    		conn = clsDatabaseFunctions.getConnectionWithException(context, 
    			   sDBID, 
    			   "MySQL",
    			   SMUtilities.getFullClassName(this.toString()) + ".load - user: " + sUser);
    	} catch (Exception e) {
    		throw new Exception("Error getting connection to load AROptions - " + e.getMessage());
    	}
    	if (conn == null){
    		throw new Exception("Counld not get connection to load AROptions.");
    	}
    	if (!load(conn)){
    		clsDatabaseFunctions.freeConnection(context, conn, "[1547067562]");
    		throw new Exception("Error loading AROptions - " + getErrorMessages());
    	}
    	clsDatabaseFunctions.freeConnection(context, conn, "[1547067563]");
    }
    

    public boolean saveEditableFields(ServletContext context, String sDBID, String sUserName, String sUserID, String sUserFullName){
    	
    	//Only re-sets the fields that can be edited from the 'Edit Options' screen:
		String SQL = "UPDATE " + SMTablearoptions.TableName
		+ " SET"
		//+ " " + SMTablearoptions.datstartdate + " = '" + m_sPostingStartDate + "'"
		//+ ", " + SMTablearoptions.ibatchpostinginprocess + " = " + m_sBatchPostingInProcess
		//+ ", " + SMTablearoptions.sprocess + " = '" + m_sPostingProcess + "'"
		//+ ", " + SMTablearoptions.suser + " = '" + m_sPostingUser + "'"
		+ " " + SMTablearoptions.iexportto + " = " + m_sExportTo
		+ ", " + SMTablearoptions.ifeedgl + " = " + m_ifeedgl
		+ ", " + SMTablearoptions.ienforcecreditlimit + " = " + m_senforcecreditlimit
		+ ", " + SMTablearoptions.gdrivecustomersparentfolderid + " = '" + clsDatabaseFunctions.FormatSQLStatement(m_sgdrivecustomerparentfolderid) + "'"
		+ ", " + SMTablearoptions.gdrivecustomersderfolderprefix + " = '" + clsDatabaseFunctions.FormatSQLStatement(m_sgdrivecustomerfolderprefix) + "'"
		+ ", " + SMTablearoptions.gdrivecustomersfoldersuffix + " = '" + clsDatabaseFunctions.FormatSQLStatement(m_sgdrivecustomerfoldersuffix) + "'"
		;
		
		Connection conn = clsDatabaseFunctions.getConnection(
			context, 
			sDBID, 
			"MySQL", 
			SMUtilities.getFullClassName(this.toString()) + ":saveEditableFields - user: " 
			+ sUserID
			+ " - "
			+ sUserFullName
		);
		if (conn == null){
			m_sErrorMessageArray.add("Error getting data connection.");
			return false;
		}
    	try {
    		Statement stmt = conn.createStatement();
		    stmt.executeUpdate(SQL);
		}catch (SQLException e){
			clsDatabaseFunctions.freeConnection(context, conn, "[1547067564]");
			m_sErrorMessageArray.add("Error updating record with SQL: " + SQL + " - " + e.getMessage());
			return false;
		}
		
		clsDatabaseFunctions.freeConnection(context, conn, "[1547067565]");
		
		return true;
    }
	public void clearErrorMessages(){
		m_sErrorMessageArray.clear();
	}
	public ArrayList<String> getErrorMessages(){
		return m_sErrorMessageArray;
	}
	public String getErrorMessageString(){
		String s = "";
		for (int i = 0; i < m_sErrorMessageArray.size(); i ++){
			s += "\n" + m_sErrorMessageArray.get(i);
		}
		return s;
	}
	
	public String getBatchPostingInProcess() {
		return m_sBatchPostingInProcess;
	}

	public void setBatchPostingInProcess(String sBatchPostingInProcess) {
		this.m_sBatchPostingInProcess = sBatchPostingInProcess;
	}

	public String getPostingUserFullName() {
		return m_sPostingUserFullName;
	}

	public void setPostingUserFullName(String sPostingUserFullName) {
		this.m_sPostingUserFullName = sPostingUserFullName;
	}

	public String getPostingProcess() {
		return m_sPostingProcess;
	}

	public void setPostingProcess(String sPostingProcess) {
		this.m_sPostingProcess = sPostingProcess;
	}

	public String getPostingStartDate() {
		return m_sPostingStartDate;
	}

	public void setPostingStartDate(String sPostingStartDate) {
		this.m_sPostingStartDate = sPostingStartDate;
	}

	public String getExportTo() {
		return m_sExportTo;
	}

	public void setExportTo(String sExportTo) {
		this.m_sExportTo = sExportTo;
	}
	
	public String getFeedGl() {
		return m_ifeedgl;
	}

	public void setFeedGl(String sFeedGl) {
		this.m_ifeedgl = sFeedGl;
	}
	
	public String getEnforceCreditLimit() {
		return m_senforcecreditlimit;
	}

	public void setEnforceCreditLimit(String sEnforceCreditLimit) {
		this.m_senforcecreditlimit = sEnforceCreditLimit;
	}
	 public String getgdrivecustomerparentfolderid(){
	    	return m_sgdrivecustomerparentfolderid;
	}
	 public void setgdrivecustomerparentfolderid(String sgdrivecustomerparentfolderid){
	    	m_sgdrivecustomerparentfolderid = sgdrivecustomerparentfolderid;
	}
	 public String getgdrivecustomerfolderprefix(){
	    	return m_sgdrivecustomerfolderprefix;
	}
	public void setgdrivecustomerfolderprefix(String sgdrivecustomerfolderprefix){
	    	m_sgdrivecustomerfolderprefix = sgdrivecustomerfolderprefix;
	}
	public String getgdrivecustomerfoldersuffix(){
	    	return m_sgdrivecustomerfoldersuffix;
	}
	public void setgdrivecustomerfoldersuffix(String sgdrivecustomerfoldersuffix){
	    	m_sgdrivecustomerfoldersuffix = sgdrivecustomerfoldersuffix;
	}

	public String getQueryString(){
	
		String sQueryString = "";
		sQueryString += ParamBatchPostingInProcess + "=" + clsServletUtilities.URLEncode(m_sBatchPostingInProcess);
		sQueryString += "&" + ParamPostingUserFullName + "=" + clsServletUtilities.URLEncode(m_sPostingUserFullName);
		sQueryString += "&" + ParamPostingProcess + "=" + clsServletUtilities.URLEncode(m_sPostingProcess);
		sQueryString += "&" + ParamPostingStartDate + "=" + clsServletUtilities.URLEncode(m_sPostingStartDate);
		sQueryString += "&" + Paramiexportto + "=" + clsServletUtilities.URLEncode(m_sExportTo);
		sQueryString += "&" + Paramifeedgl + "=" + clsServletUtilities.URLEncode(m_ifeedgl);
		sQueryString += "&" + Paramienforcecreditlimit + "=" + clsServletUtilities.URLEncode(m_senforcecreditlimit);
		sQueryString += "&" + Paramsgdrivecustomerparentfolderid + "=" + clsServletUtilities.URLEncode(m_sgdrivecustomerparentfolderid);
		sQueryString += "&" + Paramsgdrivecustomerfolderprefix + "=" + clsServletUtilities.URLEncode(m_sgdrivecustomerfolderprefix);
		sQueryString += "&" + Paramsgdrivecustomerfoldersuffix + "=" + clsServletUtilities.URLEncode(m_sgdrivecustomerfoldersuffix);
		return sQueryString;
	}
	public String getDataDump(){
	
		String s = "";
		s += ParamBatchPostingInProcess + "=" + clsServletUtilities.URLEncode(m_sBatchPostingInProcess);
		s += "\n" + ParamPostingUserFullName + "=" + clsServletUtilities.URLEncode(m_sPostingUserFullName);
		s += "\n" + ParamPostingProcess + "=" + clsServletUtilities.URLEncode(m_sPostingProcess);
		s += "\n" + ParamPostingStartDate + "=" + clsServletUtilities.URLEncode(m_sPostingStartDate);
		s += "\n" + Paramiexportto + "=" + clsServletUtilities.URLEncode(m_sExportTo);
		s += "\n" + Paramifeedgl + "=" + clsServletUtilities.URLEncode(m_ifeedgl);
		s += "\n" + Paramienforcecreditlimit + "=" + clsServletUtilities.URLEncode(m_senforcecreditlimit);
		s += "\n" + Paramsgdrivecustomerparentfolderid + "=" + clsServletUtilities.URLEncode(m_sgdrivecustomerparentfolderid);
		s += "\n" + Paramsgdrivecustomerfolderprefix + "=" + clsServletUtilities.URLEncode(m_sgdrivecustomerfolderprefix);
		s += "\n" + Paramsgdrivecustomerfoldersuffix + "=" + clsServletUtilities.URLEncode(m_sgdrivecustomerfoldersuffix);
		return s;
	}
}