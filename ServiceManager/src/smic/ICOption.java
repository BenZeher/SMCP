package smic;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.servlet.ServletContext;
import SMDataDefinition.SMExportTypes;
import SMDataDefinition.SMTableiccosts;
import SMDataDefinition.SMTableicoptions;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsServletUtilities;
import smcontrolpanel.SMUtilities;

public class ICOption extends Thread{

	private long m_lbatchpostinginprocess;
	private String m_spostinguserfullname;
	private String m_spostingprocess;
	private String m_spostingstartdate;
	private long m_lpostingtimestamp;
	private Date m_dtime;
	private long m_lcostingmethod;
	private long m_lallownegativeqtys;
	private long m_lexportto;
	private long m_isuppressbarcodesonnonstockitems;
	private String m_serrormessage;
	private String m_sgdrivepurchaseordersparentfolderid;
	private String m_sgdrivepurchaseordersfolderprefix;
	private String m_sgdrivepurchaseordersfoldersuffix;
	private static final int PAUSE_BETWEEN_POSTING_FLAG_CHECKS_IN_MILLISECONDS = 1000;
	DateFormat dateFormat;
	
	private int m_iFlagImports;
	
	public ICOption(){
		dateFormat = new SimpleDateFormat("yyyy-dd-MM HH:mm:ss");
		m_lbatchpostinginprocess = 0;
		m_spostinguserfullname = "";
		m_spostingprocess = "";
		m_lpostingtimestamp = 0;
		m_dtime = new Date();
		m_spostingstartdate = null;
		m_lcostingmethod = 0;
		m_lallownegativeqtys = 1;
		m_lexportto = 0;
		m_isuppressbarcodesonnonstockitems = 0;
		m_sgdrivepurchaseordersparentfolderid = "";
		m_sgdrivepurchaseordersfolderprefix = "";
		m_sgdrivepurchaseordersfoldersuffix = "";
		m_iFlagImports = 0;
	}

	private long getTimeMilliSeconds(){
		return m_dtime.getTime();
	}
	private long getPostingTimeStamp(){
		return m_lpostingtimestamp;
	}
	private void setPostingTimeStamp(long lPostingTimeStamp){
		m_lpostingtimestamp = lPostingTimeStamp;
	}
	
	public void checkAndUpdatePostingFlagWithoutConnection(
		ServletContext context, 
		String sDBID, 
		String sCallingClass,
		String sUserFullName,
		String sPostingProcess
		) throws Exception{
		
		Connection conn = null;
		try {
			conn = clsDatabaseFunctions.getConnectionWithException(context, sDBID, "MySQL", sCallingClass);
		} catch (Exception e) {
			throw new Exception("Error [1529949616] getting connection - " + e.getMessage());
		}
		
		try {
			checkAndUpdateICPostingFlagUsingConnection(conn, sUserFullName, sPostingProcess);
		} catch (Exception e) {
			clsDatabaseFunctions.freeConnection(context, conn);
			throw new Exception("Error [1529949618] checking and updating IC posting flag - " + e.getMessage());
		}
		
		if(!clsDatabaseFunctions.freeConnection(context, conn)){
			throw new Exception("Error [1529949617] could not free database connection.");
		}
		
	}
	private void checkAndUpdateICPostingFlagUsingConnection(Connection conn,String sUserFullName, String PostingProcess) throws Exception{

		if(!load(conn)){
			throw new Exception("Error [1529674278] loading IC options to read posting flag - " + this.getErrorMessage());
		}
		
		//First, check to see if the posting flag is set:
		if (getBatchPostingInProcess() == 1){
			//If it's set, then just return an error, and don't let this posting function continue:
			throw new Exception(getConcurrentPostingError(0l));
		}

		//But if the flag is NOT set, then go ahead and set it, and also set the time stamp:
		long lTimeStamp = getTimeMilliSeconds();
		setPostingTimeStamp(lTimeStamp);
		try{
			updatePostingInformation(conn, sUserFullName, PostingProcess, dateFormat.format(m_dtime));
		}catch (Exception e){
			throw new Exception("Error [1529674608] updating Ic options - " + e.getMessage());
		}

		//Now pause so we can RE-check to make sure we still 'own' the posting flag:
		Thread.sleep(PAUSE_BETWEEN_POSTING_FLAG_CHECKS_IN_MILLISECONDS);
		
		//Reload the class again, so we can re-check:
		if(!load(conn)){
			throw new Exception("Error [1529971567] reloading IC options to read posting flag - " + this.getErrorMessage());
		}
		
		//Now make sure that the timestamp is still OUR timestamp.
		
		// If it's the SAME timestamp, which will happen most of the time, then we're good to go
		
		//But if it is a LATER time stamp, we'll want to jump out, because we want the VERY LATEST process to take priority:
		if (getPostingTimeStamp() > lTimeStamp){
			//This means that some other process came along a fraction of a second later, and so we have to give priority to that one.
			//So we'll throw an exception and just jump out:
			//System.out.println("Error [1531764234] "+getConcurrentPostingError(lTimeStamp)+"");
			throw new Exception(getConcurrentPostingError(lTimeStamp));
		}
		
		// And if it's an OLDER timestamp, then it's not ours.  But ours, being later, will take priority and we can just continue.
		//But we'll update the timestamp just to confirm that we 'own' this posting flag:
		if (getPostingTimeStamp() < lTimeStamp){
			setPostingTimeStamp(lTimeStamp);
			try{
				updatePostingInformation(conn, sUserFullName, PostingProcess, dateFormat.format(m_dtime));
			} catch (Exception e){
				throw new Exception("Error [1529674708] updating IC options - " + e.getMessage());
			}
		}
	}
	private String getConcurrentPostingError(long lTimeStamp){
		return "User " + getPostingUserFullName()
			+ " has been " + getPostingProcess()
			+ " since " + getPostingStartDate()
			+ ". Try again in a minute or so.\n"
			+ "(User TimeStamp = "+Long.toString(lTimeStamp)+" "
			+ " Other User TimeStamp = "+Long.toString(getPostingTimeStamp())+")"
		;
	}
	public void resetPostingFlagWithoutConnection(ServletContext context, String sDBID) throws Exception{

		Connection conn = null;
		try {
			conn = clsDatabaseFunctions.getConnectionWithException(
				context, 
				sDBID, 
				"MySQL", 
				clsServletUtilities.getFullClassName(this.toString()) + ".resetPostingFlagWithoutConnection"
			);
		} catch (Exception e1) {
			throw new Exception("Error [1529950341] - " + e1.getMessage());
		}
		String SQL = "UPDATE " + SMTableicoptions.TableName 
		+ " SET " + SMTableicoptions.ibatchpostinginprocess + " = 0"
		+ ", " + SMTableicoptions.datstartdate + " = '0000-00-00 00:00:00'"
		+ ", " + SMTableicoptions.sprocess + " = ''"
		+ ", " + SMTableicoptions.suserfullname + " = ''"
		+ ", " + SMTableicoptions.lpostingtimestamp + " = 0"
		;
		
		try {
			Statement stmt = conn.createStatement();
			stmt.execute(SQL);
		} catch (Exception e) {
			clsDatabaseFunctions.freeConnection(context, conn);
			throw new Exception ("Error [1529970242] resetting IC posting flag with connection - " + e.getMessage());
		}
		clsDatabaseFunctions.freeConnection(context, conn);
}
	
	private void updatePostingInformation(Connection conn, String sUserFullName, String sPostingProcess, String sDate) throws Exception{
		
		if(!setPostingUserFullName(sUserFullName))
			throw new Exception("Error [1529972780] setting posting user's full name.");
		if(!setPostingProcess(sPostingProcess))
			throw new Exception("Error [1529972780] setting posting process.");
		setPostingStartDate(sDate);
		
		try {
			String SQL = "UPDATE " + SMTableicoptions.TableName
			+ " SET "
			+ SMTableicoptions.datstartdate + " = " + "STR_TO_DATE( '"+m_spostingstartdate + "', '%Y-%d-%m %H:%i:%s') "
			+ ", " + SMTableicoptions.ibatchpostinginprocess + " = " + Long.toString(m_lbatchpostinginprocess)
			+ ", " + SMTableicoptions.sprocess + " = '" + m_spostingprocess + "'"
			+ ", " + SMTableicoptions.suserfullname + " = '" + clsDatabaseFunctions.FormatSQLStatement(m_spostinguserfullname) + "'"
			+ ", " + SMTableicoptions.lpostingtimestamp + " = " + Long.toString(getPostingTimeStamp()) + ""
			;
			Statement stmt = conn.createStatement();
			stmt.execute(SQL);
		} catch (Exception e) {
			throw new Exception("Error [1529972941] updating IC options posting information - " + e.getMessage());
		}
	}
	
    public boolean load (
    	Connection conn
    ){
    	String SQL = "SELECT * FROM " + SMTableicoptions.TableName;
    	try {
    		ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
    		if (rs.next()){
    			
    			m_lbatchpostinginprocess = rs.getLong(SMTableicoptions.ibatchpostinginprocess);
    			m_spostinguserfullname = rs.getString(SMTableicoptions.suserfullname);
    			m_spostingprocess = rs.getString(SMTableicoptions.sprocess);
   				m_spostingstartdate = rs.getString(SMTableicoptions.datstartdate);
    			m_lcostingmethod = rs.getLong(SMTableicoptions.lcostingmethod);
    			m_lallownegativeqtys = rs.getLong(SMTableicoptions.lallownegativeqtys);
    			m_lexportto = rs.getLong(SMTableicoptions.iexportto);
    			m_isuppressbarcodesonnonstockitems = rs.getLong(SMTableicoptions.isuppressbarcodesonnonstockitems);
    			m_sgdrivepurchaseordersparentfolderid = rs.getString(SMTableicoptions.gdrivepurchaseordersparentfolderid);
    			m_sgdrivepurchaseordersfolderprefix = rs.getString(SMTableicoptions.gdrivepurchaseordersfolderprefix);
    			m_sgdrivepurchaseordersfoldersuffix = rs.getString(SMTableicoptions.gdrivepurchaseordersfoldersuffix);
    			m_iFlagImports = rs.getInt(SMTableicoptions.iflagimports);
    			m_lpostingtimestamp = rs.getLong(SMTableicoptions.lpostingtimestamp);
    			
    			//System.out.println("[1528998596] SELECT " + m_lpostingtimestamp+" "+Thread.currentThread());
    			rs.close();
    			return true;
    		}else{
    			m_serrormessage = "Could not get record";
    			rs.close();
    			return false;
    		}
    	}catch (SQLException e){
    		m_serrormessage = "SQL Error: " + e.getMessage();
    		return false;
    	}
	}
    
    public void load(String sConf, ServletContext context, String sUser) throws Exception{
    	Connection conn;
    	try {
    		conn = clsDatabaseFunctions.getConnectionWithException(context, 
    			   sConf, 
    			   "MySQL",
    			   SMUtilities.getFullClassName(this.toString()) + ".load - user: " + sUser);
    	} catch (Exception e) {
    		throw new Exception("Error [1440684678] getting connection to load ICOptions - " + e.getMessage());
    	}
    
    	if (conn == null){
    		throw new Exception("Error [1440684679] ould not get connection to load ICOptions.");
    	}
    	if (!load(conn)){
    		clsDatabaseFunctions.freeConnection(context, conn);
    		throw new Exception("Error [1440684680] loading ICOptions - " + getErrorMessage());
    	}
    	clsDatabaseFunctions.freeConnection(context, conn);
    }

    public boolean saveEditableFields(ServletContext context, String sConf, String sUserName){
    	
    	//Only re-sets the fields that can be edited from the 'Edit Options' screen:
    	
    	//Costing method cannot be changed if there are cost buckets already in the system:
    	String SQL = "SELECT COUNT(*) FROM " + SMTableiccosts.TableName;
    	try {
			ResultSet rs = clsDatabaseFunctions.openResultSet(
					SQL, 
					context, 
					sConf, 
					"MySQL", 
					SMUtilities.getFullClassName(this.toString()) + ".saveEditableFields - user: sUserName");
			if (rs.next()){
			}
			rs.close();
		} catch (SQLException e1) {
			m_serrormessage = "Could not check ic cost buckets with SQL: " + SQL + " - " + e1.getMessage();
			return false;
		}
    	
		SQL = "SELECT"
			+ " " + SMTableicoptions.lcostingmethod
			+ " FROM " + SMTableicoptions.TableName
		;
    	try {
			ResultSet rs = clsDatabaseFunctions.openResultSet(
					SQL, 
					context, 
					sConf, 
					"MySQL", 
					SMUtilities.getFullClassName(this.toString()) + ".saveEditableFields - user: sUserName");
			if (rs.next()){
				if (m_lcostingmethod != rs.getLong(SMTableicoptions.lcostingmethod)){
					m_serrormessage = "Costing method can not be changed if there are any existing cost buckets.";
					rs.close();
					return false;
				}
			}else{
				m_serrormessage = "Could not get ic costing method with SQL: " + SQL + ".";
				return false;
			}
			rs.close();
		} catch (SQLException e1) {
			m_serrormessage = "Could not check ic costing method with SQL: " + SQL + " - " + e1.getMessage();
			return false;
		}
    	
		SQL = "UPDATE " + SMTableicoptions.TableName
		+ " SET "
		+ SMTableicoptions.datstartdate + " = '" + m_spostingstartdate + "'"
		+ ", " + SMTableicoptions.ibatchpostinginprocess + " = " + Long.toString(m_lbatchpostinginprocess)
		+ ", " + SMTableicoptions.lcostingmethod + " = " + Long.toString(m_lcostingmethod)
		+ ", " + SMTableicoptions.sprocess + " = '" + m_spostingprocess + "'"
		+ ", " + SMTableicoptions.suserfullname + " = '" + clsDatabaseFunctions.FormatSQLStatement(m_spostinguserfullname) + "'"
		+ ", " + SMTableicoptions.lallownegativeqtys + " = " + Long.toString(m_lallownegativeqtys)
		+ ", " + SMTableicoptions.iexportto + " = " + Long.toString(m_lexportto)
		+ ", " + SMTableicoptions.isuppressbarcodesonnonstockitems + " = " + Long.toString(m_isuppressbarcodesonnonstockitems)
		+ ", " + SMTableicoptions.gdrivepurchaseordersparentfolderid + " = '" + clsDatabaseFunctions.FormatSQLStatement(m_sgdrivepurchaseordersparentfolderid) + "'"
		+ ", " + SMTableicoptions.gdrivepurchaseordersfolderprefix + " = '" + clsDatabaseFunctions.FormatSQLStatement(m_sgdrivepurchaseordersfolderprefix) + "'"
		+ ", " + SMTableicoptions.gdrivepurchaseordersfoldersuffix + " = '" + clsDatabaseFunctions.FormatSQLStatement(m_sgdrivepurchaseordersfoldersuffix) + "'"
		+ ", " + SMTableicoptions.lpostingtimestamp + " = " + Long.toString(getPostingTimeStamp()) + ""
		;
		//System.out.println("[1530292639] In " + this.toString() + ".saveEditableFields - SQL = " + SQL);
    	try {
    		if(!clsDatabaseFunctions.executeSQL(SQL, 
    				context,
    				sConf,
    				"MySQL",
    				this.toString() + ".saveEditableFields - User: " + sUserName)){
    			m_serrormessage = "Could not update icoptions record";
    			return false;
    		}else{
    			return true;
    		}
		}catch (SQLException e){
			//System.out.println("Error [1530307735] updating record in " + this.toString() + ": " + e.getMessage());
			m_serrormessage = "Error [1530307736] updating record: " + e.getMessage();
			return false;
		}
    }
    
	public long getBatchPostingInProcess(){
		return m_lbatchpostinginprocess;
	}
	public boolean setCostingMethod (long lCostingMethod){
		
		if (
			(lCostingMethod >= 0)
			&& (lCostingMethod <= 2)
		){
			m_lcostingmethod = lCostingMethod;
			return true;
		}else{
			m_serrormessage = "Cannot set costing method to " + Long.toString(lCostingMethod);
			return false;
		}
	}
	public long getCostingMethod(){
		return m_lcostingmethod;
	}
	public boolean setAllowNegativeQtys (long lAllowNegativeQtys){
		
		if (
			(lAllowNegativeQtys == 0)
			|| (lAllowNegativeQtys == 1)
		){
			m_lallownegativeqtys = lAllowNegativeQtys;
			return true;
		}else{
			m_serrormessage = "Cannot set costing allow negative qtys option to  " + Long.toString(lAllowNegativeQtys);
			return false;
		}
	}
	public long getAllowNegativeQtys(){
		return m_lallownegativeqtys;
	}
	public String getPostingUserFullName(){
		return m_spostinguserfullname;
	}
	public boolean setPostingUserFullName(String sPostingUserFullName){
		if (sPostingUserFullName.length() > SMTableicoptions.suserfullnamelength){
			m_serrormessage = "Posting user '" + sPostingUserFullName + "' is longer than " 
			+ SMTableicoptions.suserfullnamelength + " characters.";
			return false;
		}
		m_spostinguserfullname = sPostingUserFullName;
		return true;
	}
	public int getiFlagImports(){
		return m_iFlagImports;
	}
	public String getPostingProcess(){
		return m_spostingprocess;
	}
	public boolean setPostingProcess(String sPostingProcess){
		if (sPostingProcess.length() > SMTableicoptions.sprocesslength){
			m_serrormessage = "Posting process '" + sPostingProcess + "' is longer than " 
			+ SMTableicoptions.sprocesslength + " characters.";
			return false;
		}
		m_spostingprocess = sPostingProcess;
		return true;
	}
	public String getPostingStartDate(){
		return m_spostingstartdate;
	}
	public void setPostingStartDate(String sPostingStartDate){
		m_spostingstartdate = sPostingStartDate;
	}
	public long getExportTo(){
		return m_lexportto;
	}
	public boolean setExportTo(String sExportTo){
		if (sExportTo.compareToIgnoreCase(Integer.toString(SMExportTypes.EXPORT_TO_ACCPAC54)) == 0){
			m_lexportto = SMExportTypes.EXPORT_TO_ACCPAC54;
			return true;
		}
		if (sExportTo.compareToIgnoreCase(Integer.toString(SMExportTypes.EXPORT_TO_ACCPAC56)) == 0){
			m_lexportto = SMExportTypes.EXPORT_TO_ACCPAC56;
			return true;
		}
		if (sExportTo.compareToIgnoreCase(Integer.toString(SMExportTypes.EXPORT_TO_MAS200)) == 0){
			m_lexportto = SMExportTypes.EXPORT_TO_MAS200;
			return true;
		}
		return false;
	}
	
	public long getSuppressBarCodesOnNonStockItems(){
		return m_isuppressbarcodesonnonstockitems;
	}
	public boolean setSuppressBarCodesOnNonStockItems(String sm_isuppressbarcodesonnonstockitems){
		if (sm_isuppressbarcodesonnonstockitems.compareToIgnoreCase("0") == 0){
			m_isuppressbarcodesonnonstockitems = 0;
			return true;
		}
		if (sm_isuppressbarcodesonnonstockitems.compareToIgnoreCase("1") == 0){
			m_isuppressbarcodesonnonstockitems = 1;
			return true;
		}
		return false;
	}
	
	public String getgdrivepurchaseordersparentfolderid(){
    	return m_sgdrivepurchaseordersparentfolderid;
    }
    public void setgdrivepurchaseordersparentfolderid(String sgdrivepurchaseordersparentfolderid){
    	m_sgdrivepurchaseordersparentfolderid = sgdrivepurchaseordersparentfolderid;
    }
    public String getgdrivepurchaseordersfolderprefix(){
    	return m_sgdrivepurchaseordersfolderprefix;
    }
    public void setgdrivepurchaseordersfolderprefix(String sgdrivepurchaseordersfolderprefix){
    	m_sgdrivepurchaseordersfolderprefix = sgdrivepurchaseordersfolderprefix;
    }
    public String getgdrivepurchaseordersfoldersuffix(){
    	return m_sgdrivepurchaseordersfoldersuffix;
    }
    public void setgdrivepurchaseordersfoldersuffix(String sgdrivepurchaseordersfoldersuffix){
    	m_sgdrivepurchaseordersfoldersuffix = sgdrivepurchaseordersfoldersuffix;
    }
	public String getErrorMessage(){
    	return m_serrormessage;
    }
	
		
	}

