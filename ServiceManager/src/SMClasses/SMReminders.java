package SMClasses;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import SMDataDefinition.SMTablereminderusers;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsDateAndTimeConversions;
import ServletUtilities.clsManageRequestParameters;
import SMDataDefinition.SMTablereminders;
import smcontrolpanel.SMRemindersEdit;
import smcontrolpanel.SMUtilities;

public class SMReminders extends java.lang.Object{
	
	public static final String ParamObjectName = "Reminders";
	
	public static final String Paramlid = "lid";
	public static final String Paramsschedulecode = "sschedulecode";
	public static final String Paramsdescription = "sdescription";
	public static final String Paramiinterval = "iinterval";;
	public static final String Paramidayofmonth = "idayofmonth";
	public static final String Paramimonth = "imonth";
	public static final String Paramisunday = "isunday";
	public static final String Paramimonday = "imonday";
	public static final String Paramituesday = "ituesday";
	public static final String Paramiwednesday = "iwednesday";
	public static final String Paramithursday = "ithursday";
	public static final String Paramifriday = "ifriday";
	public static final String Paramisaturday = "isaturday";
	public static final String Paramdatstartdate = "datstartdate";	
	public static final String Paramdatlasteditdate = "datlasteditdate";
	public static final String Paramslasteditedby = "slasteditedby";
	
	public static final String Paramiremindermode = "iremindermode";
	public static final String ParamsNewRecord = "sNewRecord";
	public static final String FORM_NAME = "MAINFORM";
	
	private String m_lid;
	private String m_sschedulecode;
	private String m_sdescription;
	private String m_iinterval;
	private String m_idayofmonth;
	private String m_imonth;
	private String m_isunday;
	private String m_imonday;
	private String m_ituesday;
	private String m_iwednesday;
	private String m_ithursday;
	private String m_ifriday;
	private String m_isaturday;
	private String m_datstartdate;
	private String m_iremindermode;
	private String m_datlasteditdate;
	private String m_slasteditedfullname;
	private ArrayList<String> arrUsersToUpdate;
	DateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");

	private String m_sNewRecord;
	
	private ArrayList<String> m_sErrorMessageArray = new ArrayList<String> (0);	

	public SMReminders(){
		m_lid = "0";
		m_sschedulecode = "";
		m_sdescription = "";
		m_iinterval = "1";
		m_idayofmonth = "1";
		m_imonth = "1";
		m_isunday = "0";
		m_imonday = "0";
		m_ituesday = "0";
		m_iwednesday = "0";
		m_ithursday = "0";
		m_ifriday = "0";
		m_isaturday = "0";
		m_datstartdate = "0000-00-00";
		m_iremindermode = "1";
		m_datlasteditdate = "0000-00-00 00:00:00";
		m_slasteditedfullname = "";
		
		m_sNewRecord = "";
		
		m_sErrorMessageArray = new ArrayList<String> (0);
	}
	
	public SMReminders (HttpServletRequest req){
		m_lid = clsManageRequestParameters.get_Request_Parameter(Paramlid, req).trim();
		m_sschedulecode = clsManageRequestParameters.get_Request_Parameter(Paramsschedulecode, req).trim();
		m_sdescription = clsManageRequestParameters.get_Request_Parameter(Paramsdescription, req).trim();		
		m_iinterval = clsManageRequestParameters.get_Request_Parameter(Paramiinterval, req).trim();
		if(m_iinterval.compareToIgnoreCase("")==0){
			m_iinterval = "1";
		}
		m_idayofmonth = clsManageRequestParameters.get_Request_Parameter(Paramidayofmonth, req).trim();
		if(m_idayofmonth.compareToIgnoreCase("")==0){
			m_idayofmonth = "0";
		}
	
		m_imonth = clsManageRequestParameters.get_Request_Parameter(Paramimonth, req).trim();
		if(m_imonth.compareToIgnoreCase("")==0){
			m_imonth = "0";
		}
		m_isunday = clsManageRequestParameters.get_Request_Parameter(Paramisunday, req).trim();
		if(m_isunday.compareToIgnoreCase("")==0){
			m_isunday = "0";
		}else{
			m_isunday = "1";
		}
		m_imonday = clsManageRequestParameters.get_Request_Parameter(Paramimonday, req).trim();
		if(m_imonday.compareToIgnoreCase("")==0){
			m_imonday = "0";
		}else{
			m_imonday = "1";
		}
		m_ituesday = clsManageRequestParameters.get_Request_Parameter(Paramituesday, req).trim();
		if(m_ituesday.compareToIgnoreCase("")==0){
			m_ituesday = "0";
		}else{
			m_ituesday = "1";
		}
		m_iwednesday = clsManageRequestParameters.get_Request_Parameter(Paramiwednesday, req).trim();
		if(m_iwednesday.compareToIgnoreCase("")==0){
			m_iwednesday = "0";
		}else{
			m_iwednesday = "1";
		}
		m_ithursday = clsManageRequestParameters.get_Request_Parameter(Paramithursday, req).trim();
		if(m_ithursday.compareToIgnoreCase("")==0){
			m_ithursday = "0";
		}else{
			m_ithursday = "1";
		}
		m_ifriday = clsManageRequestParameters.get_Request_Parameter(Paramifriday, req).trim();
		if(m_ifriday.compareToIgnoreCase("")==0){
			m_ifriday = "0";
		}else{
			m_ifriday = "1";
		}
		m_isaturday = clsManageRequestParameters.get_Request_Parameter(Paramisaturday, req).trim();
		if(m_isaturday.compareToIgnoreCase("")==0){
			m_isaturday = "0";
		}else{
			m_isaturday = "1";
		}
			
		m_datstartdate = clsManageRequestParameters.get_Request_Parameter(Paramdatstartdate, req).trim();
		Date nowdate = new Date();
		if(m_datstartdate.compareToIgnoreCase("")==0 || m_datstartdate.compareToIgnoreCase("00/00/0000") == 0 ){
				m_datstartdate = dateFormat.format(nowdate);
			}

		m_iremindermode = clsManageRequestParameters.get_Request_Parameter(Paramiremindermode, req).trim();
		if(m_iremindermode.compareToIgnoreCase("")==0){
			m_iremindermode = Integer.toString(SMTablereminders.PERSONAL_REMINDER_VALUE);
		}
		
		Enumeration <String> e = req.getParameterNames();
		arrUsersToUpdate = new ArrayList<String> (0);
		String sParam = "";
		arrUsersToUpdate.clear();
		
		while (e.hasMoreElements()){
			sParam = (String) e.nextElement();
			String sUserToUpdate = "";
			if (sParam.contains(SMRemindersEdit.USER_ID_MARKER)){
				sUserToUpdate = sParam.substring(SMRemindersEdit.USER_ID_MARKER.length(), sParam.length());
			}		
			if (req.getParameter(sParam) != null && sUserToUpdate.compareToIgnoreCase("") != 0){
				arrUsersToUpdate.add(sUserToUpdate);
			}	
		}	
		
		m_sNewRecord = clsManageRequestParameters.get_Request_Parameter(ParamsNewRecord, req).trim();
		m_sErrorMessageArray = new ArrayList<String> (0);
	}
	
    public boolean load (Connection conn){
        	
    	String SQL = "SELECT * FROM " + SMTablereminders.TableName
				+ " WHERE ("
				+ SMTablereminders.lid + " = " + m_lid 
			+	")";
        	try {
        		ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
        		if (rs.next()){
        			m_lid = Long.toString(rs.getLong(SMTablereminders.lid));
        			m_sschedulecode = rs.getString(SMTablereminders.sschedulecode);		
        			m_sdescription = rs.getString(SMTablereminders.sdescription);
        			m_iinterval =  Long.toString(rs.getLong(SMTablereminders.iinterval));
        			m_idayofmonth = Long.toString(rs.getLong(SMTablereminders.idayofmonth));
        			m_imonth = Long.toString(rs.getLong(SMTablereminders.imonth));
        			m_isunday = Long.toString(rs.getLong(SMTablereminders.isunday));
        			m_imonday = Long.toString(rs.getLong(SMTablereminders.imonday));
        			m_ituesday = Long.toString(rs.getLong(SMTablereminders.ituesday));
        			m_iwednesday = Long.toString(rs.getLong(SMTablereminders.iwednesday));
        			m_ithursday = Long.toString(rs.getLong(SMTablereminders.ithursday));
        			m_ifriday = Long.toString(rs.getLong(SMTablereminders.ifriday));
        			m_isaturday = Long.toString(rs.getLong(SMTablereminders.isaturday));
        			m_datstartdate = clsDateAndTimeConversions.resultsetDateStringToString(rs.getString(SMTablereminders.datstartdate));
        			try{
        			Date nowdate = new Date();
        			Date startdate = dateFormat.parse(m_datstartdate);
        				if(	startdate.before(nowdate)){
        					m_datstartdate = dateFormat.format(nowdate);
        				}
        			} catch (Exception e1) {
        				m_sErrorMessageArray.add("Error loading start date: " + e1.getMessage());
        			}
        			m_iremindermode = Long.toString(rs.getLong(SMTablereminders.iremindermode));
        			m_datlasteditdate = clsDateAndTimeConversions.resultsetDateTimeToTheMinuteStringToString(rs.getString(SMTablereminders.datlasteditdate));
        			m_slasteditedfullname = rs.getString(SMTablereminders.slastediteduserfullname);
        			m_sNewRecord = "0";
        			rs.close();
        			return true;
        		}else{
        		rs.close();
        		setNewRecord("1");
        		return true;
        		}
        	}catch (SQLException e){
        		m_sErrorMessageArray.add("SQL Error reading schedule record: " + e.getMessage());
        		return false;
        	}
    	}
    
    public void load(ServletContext context, String sDBIB, String sUser) throws Exception{
    	Connection conn;
    	try {
    		conn = clsDatabaseFunctions.getConnectionWithException(context, 
    			   sDBIB, 
    			   "MySQL",
    			   SMUtilities.getFullClassName(this.toString()) + ".load - user: " + sUser);
    	} catch (Exception e) {
    		throw new Exception("Error getting connection to load SMReminders - " + e.getMessage());
    	}
    	if (conn == null){
    		throw new Exception("Counld not get connection to load SMReminders.");
    	}
    	if (!load(conn)){
    		clsDatabaseFunctions.freeConnection(context, conn, "[1547067751]");
    		throw new Exception("Error loading SMReminders - " + getErrorMessages());
    	}
    	clsDatabaseFunctions.freeConnection(context, conn, "[1547067752]");
    }
    

    public boolean save(ServletContext context, String sDBIB, String sUserID, String sUserFullName, String sStartDate){
    	m_sErrorMessageArray.clear();
		
    	//Get connection
		Connection conn = clsDatabaseFunctions.getConnection(
			context, 
			sDBIB, 
			"MySQL", 
			SMUtilities.getFullClassName(this.toString()) 
			+ ":save - user: " + sUserID
			+ " - "
			+ sUserFullName
		);
		if (conn == null){
			m_sErrorMessageArray.add("Error getting data connection.");
			return false;
		}
		
		//Validate entries
		if(!validateEntries()){
			clsDatabaseFunctions.freeConnection(context, conn, "[1547067753]");
			return false;
		}
		
		//Now begin a data transaction to update both schedule and scheduled users tables:
		if(!clsDatabaseFunctions.start_data_transaction(conn)){
			clsDatabaseFunctions.freeConnection(context, conn, "[1547067754]");
			m_sErrorMessageArray.add("Error [1452537672] beginning data transaction to update schedule tables");
			return false;
		}
		//Update reminders users table
		try {
			updateRemindersUsersTable(conn, sStartDate);
		} catch (Exception e) {
			clsDatabaseFunctions.rollback_data_transaction(conn);
			clsDatabaseFunctions.freeConnection(context, conn, "[1547067755]");
			m_sErrorMessageArray.add("Error [1530546752] updating reminders users table - " + e.getMessage());
			return false;
		}
		
		//Update reminders table
		try {
			updateRemindersTable(conn, context, sUserFullName, sUserID);
		} catch (Exception e) {
			clsDatabaseFunctions.rollback_data_transaction(conn);
			clsDatabaseFunctions.freeConnection(context, conn, "[1547067756]");
			m_sErrorMessageArray.add("Error [1530546753] updating reminders table - " + e.getMessage());
			return false;
		}
		
		//Commit transaction
		if(!clsDatabaseFunctions.commit_data_transaction(conn)){
			clsDatabaseFunctions.rollback_data_transaction(conn);
			clsDatabaseFunctions.freeConnection(context, conn, "[1547067757]");
			m_sErrorMessageArray.add("Error [1452537846] commiting data transaction for schedule code -' " + m_sschedulecode + "'");
			return false;
		}
	

		clsDatabaseFunctions.freeConnection(context, conn, "[1547067758]");
	
		
		return true;
    }
   

private void updateRemindersUsersTable(Connection conn, String sStartDate) throws Exception{
	
	ArrayList<String> arrUsersLastAckReminderDate = new ArrayList<String> (0);
	ArrayList<String> arrUsersLastAckDate = new ArrayList<String> (0);
	ArrayList<String> arrUsersLastRunDate = new ArrayList<String> (0);
	String sLastAckReminderDate = "";
	String sLastAckDate = "";
	String sLastRunDate = "";
	String SQL = "";
	//Save any existing dates from the users scheduled reminder record. 
	for (int i = 0; i < arrUsersToUpdate.size(); i++){	
	 SQL = "SELECT " + SMTablereminderusers.TableName + "." + SMTablereminderusers.datlastacknowledgedreminderdate 
			+ ", " + SMTablereminderusers.TableName + "." + SMTablereminderusers.datlastrundate
			+ ", " + SMTablereminderusers.TableName + "." + SMTablereminderusers.datlastacknowledgeddate
		    + ", " + SMTablereminders.TableName + "." + SMTablereminders.datstartdate			
			+ " FROM " + SMTablereminderusers.TableName
			+ " LEFT JOIN " + SMTablereminders.TableName
			+ " ON " + SMTablereminderusers.TableName + "." + SMTablereminderusers.sschedulecode + " = " 
			+ SMTablereminders.TableName + "." + SMTablereminders.sschedulecode
			+ " WHERE ("
			+ "(" + SMTablereminderusers.TableName + "."+ SMTablereminderusers.luserid + " = " + arrUsersToUpdate.get(i) + ")"
			+ " AND "
			+ "(" + SMTablereminderusers.TableName + "." + SMTablereminderusers.sschedulecode + " = '" + m_sschedulecode + "')"
			+ ")" ;	
		ResultSet rsLastDates = clsDatabaseFunctions.openResultSet(SQL, conn);
		sLastAckReminderDate = "0000-00-00";
		sLastAckDate = "0000-00-00 00:00:00";
		sLastRunDate = "0000-00-00 00:00:00";
		
		if(rsLastDates.next()){
			sLastAckReminderDate = clsDateAndTimeConversions.resultsetDateStringToString(rsLastDates.getString(SMTablereminderusers.datlastacknowledgedreminderdate));
			sLastAckDate =  clsDateAndTimeConversions.resultsetDateTimeToTheMinuteStringToString(rsLastDates.getString(SMTablereminderusers.datlastacknowledgeddate));
			sLastRunDate = clsDateAndTimeConversions.resultsetDateTimeToTheMinuteStringToString(rsLastDates.getString(SMTablereminderusers.datlastrundate));
			arrUsersLastAckReminderDate.add(sLastAckReminderDate);
			arrUsersLastAckDate.add(sLastAckDate);
			arrUsersLastRunDate.add(sLastRunDate);
		} else{
			//Set the last acknowledged reminder date back one day so it will display the day it is created.
			Date date = dateFormat.parse(sStartDate);		
			Calendar cal = Calendar.getInstance();
			cal.setTime(date);
			cal.add(Calendar.DATE, -1);
			date= cal.getTime();		
			sLastAckReminderDate = dateFormat.format(date);		
			arrUsersLastAckReminderDate.add(sLastAckReminderDate);
			arrUsersLastAckDate.add(sLastAckDate);
			arrUsersLastRunDate.add(sLastRunDate);
		}
		
		rsLastDates.close();
	}

	//Delete all records with current schedule code

	try{
		SQL = "DELETE FROM " + SMTablereminderusers.TableName 
				+ " WHERE("
				+ SMTablereminderusers.sschedulecode + "='" + m_sschedulecode
				+ "')"
				;
		if(!clsDatabaseFunctions.executeSQL(SQL, conn)){
			throw new Exception("Error [145253852] deleting schedule " + m_sschedulecode);
		}
		
	}catch(Exception e){
		throw new Exception("Error [1452537846] deleting schedule code " + m_sschedulecode + " - " + e.getMessage());
	}
	
	if((arrUsersToUpdate.size() != arrUsersLastAckReminderDate.size()) || 
		(arrUsersToUpdate.size() != arrUsersLastAckDate.size()) ||
		(arrUsersToUpdate.size() != arrUsersLastRunDate.size())){
		throw new Exception("Error [1455052779] updating users schedule code '" + m_sschedulecode 
				+ "' - " + "Could not save unacknowledged reminder dates.");
	}
	//Insert new records for all selected users
	for (int i = 0; i < arrUsersToUpdate.size(); i++){
	
			SQL = "INSERT INTO " + SMTablereminderusers.TableName + "("
				+ SMTablereminderusers.sschedulecode 
				+ ", " + SMTablereminderusers.luserid
				+ ", " + SMTablereminderusers.datlastacknowledgedreminderdate
				+ ", " + SMTablereminderusers.datlastrundate
				+ ", " + SMTablereminderusers.datlastacknowledgeddate
				+ ") VALUES("
				+ "'" + clsDatabaseFunctions.FormatSQLStatement(m_sschedulecode) + "'"
				+ ", " + clsDatabaseFunctions.FormatSQLStatement(arrUsersToUpdate.get(i)) + ""
				+ ", '" + clsDateAndTimeConversions.stdDateStringToSQLDateString(arrUsersLastAckReminderDate.get(i)) + "'"
				+ ", '" + clsDateAndTimeConversions.stdDateTimeToSQLDateTimeString(arrUsersLastRunDate.get(i)) + "'"
				+ ", '" + clsDateAndTimeConversions.stdDateTimeToSQLDateTimeString(arrUsersLastAckDate.get(i)) + "'"
				+ ")"
				;
		try{
			if(!clsDatabaseFunctions.executeSQL(SQL, conn)){
				throw new Exception("Error [145253468] inserting into scheduledusers - " + m_sschedulecode);
			}
		
		}catch(Exception e){
				throw new Exception("Error [1452537846] inserting schedule codes " + m_sschedulecode + " - " + e.getMessage());
		}
	}	
	
	return;
}

private void updateRemindersTable(Connection conn,ServletContext context, String sUserFullName, String sUserID) throws Exception{
	
	//Check to see if the record already exists:
			String SQL = "SELECT * FROM " + SMTablereminders.TableName
				+ " WHERE ("
					+ SMTablereminders.lid + " = " + m_lid
				+ ")"
				;
			try{
				ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
				
				if(rs.next()){

				rs.close();
					
				m_sErrorMessageArray.clear();
					
	    	//Only update the fields that can be edited from the 'Edit Schedules' screen:
			 SQL = "UPDATE " + SMTablereminders.TableName
			+ " SET "
				+ SMTablereminders.sschedulecode + " = '" +  clsDatabaseFunctions.FormatSQLStatement(m_sschedulecode) + "'"
			+ ", " + SMTablereminders.sdescription + " = '" + clsDatabaseFunctions.FormatSQLStatement(m_sdescription) + "'"
			+ ", " + SMTablereminders.iinterval + " = " + clsDatabaseFunctions.FormatSQLStatement(m_iinterval)
			+ ", " + SMTablereminders.idayofmonth + " = " + clsDatabaseFunctions.FormatSQLStatement(m_idayofmonth) 
			+ ", " + SMTablereminders.imonth + " = " + clsDatabaseFunctions.FormatSQLStatement(m_imonth) 
			+ ", " + SMTablereminders.isunday + " = " + clsDatabaseFunctions.FormatSQLStatement(m_isunday) 
			+ ", " + SMTablereminders.imonday + " = " + clsDatabaseFunctions.FormatSQLStatement(m_imonday) 
			+ ", " + SMTablereminders.ituesday + " = " + clsDatabaseFunctions.FormatSQLStatement(m_ituesday) 
			+ ", " + SMTablereminders.iwednesday + " = " + clsDatabaseFunctions.FormatSQLStatement(m_iwednesday) 
			+ ", " + SMTablereminders.ithursday + " = " + clsDatabaseFunctions.FormatSQLStatement(m_ithursday) 
			+ ", " + SMTablereminders.ifriday + " = " + clsDatabaseFunctions.FormatSQLStatement(m_ifriday) 
			+ ", " + SMTablereminders.isaturday + " = " + clsDatabaseFunctions.FormatSQLStatement(m_isaturday) 
			+ ", " + SMTablereminders.datstartdate + " = '" + clsDateAndTimeConversions.stdDateStringToSQLDateString(m_datstartdate) + "'"
			+ ", " + SMTablereminders.slastediteduserfullname + " = '" + clsDatabaseFunctions.FormatSQLStatement(sUserFullName) + "'"
			+ ", " + SMTablereminders.datlasteditdate + " = NOW()" 
			+ ", " + SMTablereminders.iremindermode + " = " + clsDatabaseFunctions.FormatSQLStatement(m_iremindermode) 
			+ " WHERE ("
							+ SMTablereminders.lid + " = " + m_lid
						+ ")"
			;
			 	try {
			 		Statement stmt = conn.createStatement();
			 		stmt.executeUpdate(SQL);
			 		m_sNewRecord = "0";
			 	}catch (SQLException e){
			 		throw new Exception("Error updating record with SQL: " + " - " + e.getMessage());
			}
			 	
			 //If the schedule code is updated we need to delete any orphaned smscheduledusers records. 
			 SQL = "DELETE SCHEDULEDUSERS.* FROM " + SMTablereminderusers.TableName + " SCHEDULEDUSERS "
			 		+ "LEFT JOIN " + SMTablereminders.TableName + " SCHEDULES "
			 		+ "ON SCHEDULEDUSERS." + SMTablereminderusers.sschedulecode + "= SCHEDULES." + SMTablereminders.sschedulecode 
			 		+ " WHERE (SCHEDULES." + SMTablereminders.sschedulecode + " IS NULL)";  
			 try {
			 		Statement stmt = conn.createStatement();
			 		stmt.executeUpdate(SQL);
			 	}catch (SQLException e){
			 		throw new Exception("Error deleting orphan smscheduled user records with SQL: " + " - " + e.getMessage());
			 	}
			}else{
				
				rs.close();

				SQL = "INSERT INTO " + SMTablereminders.TableName + " ("
						+ SMTablereminders.sschedulecode
						+ ", " + SMTablereminders.sdescription
						+ ", " + SMTablereminders.iinterval
						+ ", " + SMTablereminders.idayofmonth //5
						+ ", " + SMTablereminders.imonth
						+ ", " + SMTablereminders.isunday
						+ ", " + SMTablereminders.imonday
						+ ", " + SMTablereminders.ituesday 
						+ ", " + SMTablereminders.iwednesday //10
						+ ", " + SMTablereminders.ithursday
						+ ", " + SMTablereminders.ifriday
						+ ", " + SMTablereminders.isaturday
						+ ", " + SMTablereminders.datstartdate 
						+ ", " + SMTablereminders.iremindermode
						+ ", " + SMTablereminders.slastediteduserfullname
						+ ", " + SMTablereminders.lcreatedbyuserid
						+ ", " + SMTablereminders.datlasteditdate
						+ " ) VALUES ("
							+ "'" + clsDatabaseFunctions.FormatSQLStatement(m_sschedulecode) + "'" 
							+ ", '" + clsDatabaseFunctions.FormatSQLStatement(m_sdescription) + "'" 
							+ ", " + m_iinterval
							+ ", " + m_idayofmonth //5
							+ ", " + m_imonth 
							+ ", " + m_isunday 
							+ ", " + m_imonday
							+ ", " + m_ituesday
							+ ", " + m_iwednesday  //10
							+ ", " + m_ithursday
							+ ", " + m_ifriday
							+ ", " + m_isaturday
							+ ", '" + clsDateAndTimeConversions.stdDateStringToSQLDateString(m_datstartdate) + "'" 
							+ ", " + m_iremindermode
							+ ", '" + clsDatabaseFunctions.FormatSQLStatement(sUserFullName) + "'" 
							+ ", " + clsDatabaseFunctions.FormatSQLStatement(sUserID) + "" 
							+ ", NOW()"
						+ ")"
						;
				try {
					clsDatabaseFunctions.executeSQL(SQL, conn);
					m_sNewRecord = "0";
				}catch (SQLException e){
			 		m_sNewRecord = "1";
			 		throw new Exception("Error inserting record with SQL: " +  " - " + e.getMessage());
				}
				
				//Update the ID if it's an insert successful:
				SQL = "SELECT last_insert_id()";
				try {
					ResultSet srs = clsDatabaseFunctions.openResultSet(SQL, conn);
					if (srs.next()) {
						m_lid = Long.toString(srs.getLong(1));
					}else {
						m_lid = "";
					}
					srs.close();
				} catch (SQLException e) {
					throw new Exception("Could not get last ID number - " + e.getMessage());
				}
				//If something went wrong, we can't get the last ID:
				if (m_lid.compareToIgnoreCase("") == 0){
					throw new Exception("Could not get last ID number.");
				}


				return;			
	    	}
				}catch(SQLException e){
					throw new Exception("Error [1452537405] saving  - " + e.getMessage());
				}
	
	return;
}

    public boolean delete (ServletContext context, String sDBIB, String sUserID, String sUserFullName){
    	
    	Connection conn = clsDatabaseFunctions.getConnection(
    			context, 
    			sDBIB, 
    			"MySQL", 
    			this.toString() + " - user: " + sUserID + " - " + sUserFullName
    			);
    	
    	if (conn == null){
    		m_sErrorMessageArray.add("Error opening data connection.");
    		return false;
    	}
    	
    	boolean bResult = delete (conn);
    	clsDatabaseFunctions.freeConnection(context, conn, "[1547067750]");
    	return bResult;
    	
    }
    
    public boolean delete(Connection conn){
		
		m_sErrorMessageArray.clear();
		
		//First, check that the schedule entry exists:
		String SQL = "SELECT * FROM " + SMTablereminders.TableName
			+ " WHERE ("
				+ SMTablereminders.lid + " = " + m_lid 
			+ ")"
		;
		try{
			ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
			if(!rs.next()){
				m_sErrorMessageArray.add("Schedule " + m_lid + " cannot be found.");
				rs.close();
				return false;
			}else{
				rs.close();
			}
			
		}catch(SQLException e){
			m_sErrorMessageArray.add("Error [1452537537] checking schedule " + m_lid + " to delete - " + e.getMessage());
			return false;
		}

		//Now begin a data transaction that we can roll back:
		if(!clsDatabaseFunctions.start_data_transaction(conn)){
			m_sErrorMessageArray.add("Error [1452537538] beginning data transaction to delete schedule  " + m_lid + "");
			return false;
		}
		//Delete record from smschedules table
		try{
			SQL = "DELETE FROM " + SMTablereminders.TableName
				+ " WHERE ("
					+ SMTablereminders.lid + " = " + m_lid
				+ ")"
			;
			if(!clsDatabaseFunctions.executeSQL(SQL, conn)){
				m_sErrorMessageArray.add("Error [1452537539] deleting schedule " + m_lid);
				clsDatabaseFunctions.rollback_data_transaction(conn);
				return false;
			}


		}catch(SQLException e){
			m_sErrorMessageArray.add("Error [1452537540] deleting schedule " + m_lid + " - " + e.getMessage());
			return false;
		}
		///Delete all records from smschedledusers table
		try{
			SQL = "DELETE FROM " + SMTablereminderusers.TableName
				+ " WHERE ("
					+ SMTablereminderusers.sschedulecode + " = '" + m_sschedulecode + "'"
				+ ")"
			;
			if(!clsDatabaseFunctions.executeSQL(SQL, conn)){
				m_sErrorMessageArray.add("Error [1452585462] deleting scheduled users, code '" + m_sschedulecode + "'");
				clsDatabaseFunctions.rollback_data_transaction(conn);
				return false;
			}


		}catch(SQLException e){
			m_sErrorMessageArray.add("Error [1452537540] deleting schedule '" + m_sschedulecode + "' - " + e.getMessage());
			return false;
		}
		
		if(!clsDatabaseFunctions.commit_data_transaction(conn)){
			clsDatabaseFunctions.rollback_data_transaction(conn);
			return false;
		}
		
		return true;
	}

private boolean validateEntries(){
	
	boolean bEntriesAreValid = true;
	m_sErrorMessageArray.clear();

	if (m_sschedulecode.length() > SMTablereminders.sschedulecodelength){
		m_sErrorMessageArray.add("Schdule code cannot be longer than  " 
			+ SMTablereminders.sschedulecodelength + " characters.");
		bEntriesAreValid = false;
	}
	if (m_sschedulecode.trim().compareToIgnoreCase("") == 0){
		m_sErrorMessageArray.add("Schedule Code cannot be blank.");
		bEntriesAreValid = false;
	}
	
	if (m_sdescription.length() > SMTablereminders.sdescriptionlength){
		m_sErrorMessageArray.add("Description cannot be longer than " 
			+ SMTablereminders.sdescriptionlength + " characters.");
		bEntriesAreValid = false;
	}
	if (m_sdescription.trim().compareToIgnoreCase("") == 0){
		m_sErrorMessageArray.add("Description cannot be blank.");
		bEntriesAreValid = false;
	}
	
	Date datNow = new Date();
	//Subtract a day from the current date to compare
	long msInDay = 1000 * 60 * 60 * 24; 
	long msPortion = datNow.getTime() % msInDay;
	datNow = new Date(datNow.getTime() - msPortion);
	
	Date datStartDate = new Date();
	try {
		datStartDate = dateFormat.parse(m_datstartdate);
	} catch (ParseException e) {
		m_sErrorMessageArray.add("Error parsing start date - " + e.getMessage());
		bEntriesAreValid = false;
	}
	if (datNow.after(datStartDate)){
		m_sErrorMessageArray.add("Start Date cannot be before todays date.");
		bEntriesAreValid = false;
	}
	if (m_iinterval.trim().compareToIgnoreCase(Integer.toString(SMTablereminders.INTERVAL_TYPE_WEEKLY)) == 0){
		m_idayofmonth = "0";
		m_imonth = "0";
		if (m_isunday.compareToIgnoreCase("0") == 0 
			&& m_imonday.compareToIgnoreCase("0") == 0
			&& m_ituesday.compareToIgnoreCase("0") == 0
			&& m_iwednesday.compareToIgnoreCase("0") == 0
			&& m_ithursday.compareToIgnoreCase("0") == 0
			&& m_ifriday.compareToIgnoreCase("0") == 0
			&& m_isaturday.compareToIgnoreCase("0") == 0		
			){
			m_sErrorMessageArray.add("A weekday must be selected for a weekly reminder interval.");
			bEntriesAreValid = false;
		}
			
	} else if (m_iinterval.trim().compareToIgnoreCase(Integer.toString(SMTablereminders.INTERVAL_TYPE_MONTHLY)) == 0){
		m_imonth = "0";
		m_isunday = "0";
		m_imonday = "0";
		m_ituesday = "0";
		m_iwednesday = "0";
		m_ithursday = "0";
		m_ifriday = "0";
		m_isaturday = "0";	
	} else if (m_iinterval.trim().compareToIgnoreCase(Integer.toString(SMTablereminders.INTERVAL_TYPE_YEARLY)) == 0){
		m_isunday = "0";
		m_imonday = "0";
		m_ituesday = "0";
		m_iwednesday = "0";
		m_ithursday = "0";
		m_ifriday = "0";
		m_isaturday = "0";	
	} else {
		m_sErrorMessageArray.add("An interval must be selected.");
		bEntriesAreValid = false;
	}
	
	return bEntriesAreValid;
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
	
	public String getlid() {
		return m_lid;
	}

	public void setlid(String lid) {
		m_lid = lid;
	}

	public String getsschedulecode() {
		return m_sschedulecode;
	}

	public void setsschedulecode(String sschedulecode) {
		m_sschedulecode = sschedulecode;
	}

	public String getsdescription() {
		return m_sdescription;
	}

	public void setsdescription(String sdescription) {
		m_sdescription = sdescription;
	}

	public String getiinterval() {
		return m_iinterval;
	}

	public void setiinterval(String iinterval) {
		m_iinterval = iinterval;
	}

	public String getidayofmonth() {
		return m_idayofmonth;
	}
	
	public void setidayofmonth(String idayofmonth) {
		m_idayofmonth = idayofmonth;
	}

	
	public String getimonth() {
		return m_imonth;
	}
	public void setimonth(String imonth) {
		m_imonth = imonth;
	}
	
	public String getisunday() {
		return m_isunday;
	}
	public void setisunday(String isunday) {
		m_isunday = isunday;
	}
	
	public String getimonday() {
		return m_imonday;
	}
	public void setimonday(String imonday) {
		m_imonday = imonday;
	}
	
	public String getituesday() {
		return m_ituesday;
	}
	public void setituesday(String ituesday) {
		m_ituesday = ituesday;
	}
	
	public String getiwednesday() {
		return m_iwednesday;
	}
	public void setiwednesday(String iwednesday) {
		m_iwednesday = iwednesday;
	}
	
	public String getithursday() {
		return m_ithursday;
	}
	public void setithursday(String ithursday) {
		m_ithursday = ithursday;
	}
	
	public String getifriday() {
		return m_ifriday;
	}
	public void setifriday(String ifriday) {
		m_ifriday = ifriday;
	}
	
	public String getisaturday() {
		return m_isaturday;
	}
	public void setisaturday(String isaturday) {
		m_isaturday = isaturday;
	}
	
	public String getdatstartdate() {
		return m_datstartdate;
	}
	public void setdatstartdate(String datstartdate) {
		m_datstartdate = datstartdate;
	}
	
	public String getiremindermode() {
		return m_iremindermode;
	}
	public void setiremindermode(String iremindermode) {
		m_iremindermode = iremindermode;
	}
	
	public String getdatlasteditdate() {
		return m_datlasteditdate;
	}
	public void setdatlasteditdate(String datlasteditdate) {
		m_datlasteditdate = datlasteditdate;
	}
	
	public String getslasteditedfullname() {
		return m_slasteditedfullname;
	}
	public void setslasteditedfullname(String slasteditedby) {
		m_slasteditedfullname = slasteditedby;
	}
	
	public void setNewRecord(String newrecord){
		m_sNewRecord = newrecord;
	}
	public String getNewRecord(){
		return m_sNewRecord;
	}
	public String getObjectName(){
	    return ParamObjectName;
	}

}