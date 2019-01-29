package TimeCardSystem;


import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsManageRequestParameters;
import ServletUtilities.clsValidateFormFields;
import TCSDataDefinition.Employees;
import TCSDataDefinition.TCSTablemadgicevents;
import TCSDataDefinition.TCSTablemadgiceventusers;

public class TCMADGICEvent extends java.lang.Object{

	public static final String ParamObjectName = "MADGIC Event";
	public static final String EDIT_FORM_NAME = "EDITMADGICEVENTFORM";
	
	private String m_slid;
	private String m_seventtypename;
	private String m_sdescription;
	private String m_inumberofpoints;
	private String m_sdatevent;
	
	public TCMADGICEvent(
        ) {
		initializeVariables();
    }
    public TCMADGICEvent(HttpServletRequest req) {
    	initializeVariables();
    	m_slid = clsManageRequestParameters.get_Request_Parameter(TCSTablemadgicevents.lid, req).trim();
    	m_seventtypename = clsManageRequestParameters.get_Request_Parameter(TCSTablemadgicevents.seventtypename, req).trim();
    	m_inumberofpoints = clsManageRequestParameters.get_Request_Parameter(TCSTablemadgicevents.inumberofpoints, req).trim();
		m_sdescription = clsManageRequestParameters.get_Request_Parameter(TCSTablemadgicevents.sdescription, req).trim();
		m_sdatevent = clsManageRequestParameters.get_Request_Parameter(TCSTablemadgicevents.datevent, req).trim();
		if (m_sdatevent.compareToIgnoreCase("") == 0){
			m_sdatevent = TimeCardUtilities.EMPTY_DATE_STRING;
		}
	}
    public void load(String sDBName, ServletContext context, String sUser) throws Exception{
    	Connection conn = null;
    	try {
			conn = clsDatabaseFunctions.getConnectionWithException(
				context, 
				sDBName, 
				"MySQL", 
				this.toString() + ".load - user: " + sUser);
		} catch (Exception e) {
			throw new Exception("Error [1486653014] - could not get connection - " + e.getMessage());
		}
    	
    	try {
			load(conn);
		} catch (Exception e) {
			throw new Exception(e.getMessage());
		}
    			
    	clsDatabaseFunctions.freeConnection(context, conn, "[1547060131]");
    }
 
	public void load (
    		Connection conn
    		) throws Exception{
        String SQL = "SELECT * FROM " + TCSTablemadgicevents.TableName
        	+ " WHERE ("
        		+ "(" + TCSTablemadgicevents.lid + " = " + get_slid() + ")"
        	+ ")"
        ;
		
		try {
			ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn); 
			if(rs.next()){
				m_seventtypename = rs.getString(TCSTablemadgicevents.seventtypename);
				m_inumberofpoints = Integer.toString(rs.getInt(TCSTablemadgicevents.inumberofpoints));
				m_sdescription = rs.getString(TCSTablemadgicevents.sdescription);
				m_sdatevent = TimeCardUtilities.resultsetDateStringToString(rs.getString(TCSTablemadgicevents.datevent));
			}else{
				throw new Exception("Error [1486653015] - " + ParamObjectName + " ID '" + this.get_slid() + "' does not exist.");
			}
			rs.close();
		}catch (SQLException ex){
			throw new Exception("Error [1486653016] loading " + ParamObjectName + " with lid '" + get_slid() + "' using SQL: " + SQL 
				+ " - " + ex.getMessage());
		}
	}
    public void save(ServletContext context, String sDBID, String sUserName, HttpServletRequest req ) throws Exception{
		
    	//Get connection
		Connection conn;
		try {
			conn = clsDatabaseFunctions.getConnectionWithException(
				context, 
				sDBID, 
				"MySQL", 
				this.toString() + ":save - user: " + sUserName
			);
		} catch (Exception e1) {
			throw new Exception("Error [1486653017] - could not get connection to save.");
		}
		
		//Validate entries
		try {
			validateEntries(conn);
		} catch (Exception e1) {
			clsDatabaseFunctions.freeConnection(context, conn, "[1547060132]");
			throw new Exception(e1.getMessage());
		}
		
		//Update the editable fields.
		String SQL = "";
		if (get_slid().compareToIgnoreCase("-1") == 0){
			SQL = "INSERT INTO " + TCSTablemadgicevents.TableName + "("
				+ TCSTablemadgicevents.datevent
				+ ", " + TCSTablemadgicevents.inumberofpoints
				+ ", " + TCSTablemadgicevents.sdescription
				+ ", " + TCSTablemadgicevents.seventtypename
			+ ") VALUES ("
				+ "'" + TimeCardUtilities.stdDateStringToSQLDateString(get_sdatevent()) + "'"
				+ ", " + get_snumberofpoints()
				+ ", '" + clsDatabaseFunctions.FormatSQLStatement(get_sdescription()) + "'"
				+ ", '" + clsDatabaseFunctions.FormatSQLStatement(get_seventtypename()) + "'"
			+ ")"
			;
		}else{
			SQL =  "UPDATE " + TCSTablemadgicevents.TableName
				+ " SET " + TCSTablemadgicevents.sdescription + " = '"  + clsDatabaseFunctions.FormatSQLStatement(get_sdescription()) + "'"
				+ ", " + TCSTablemadgicevents.inumberofpoints + " = " + get_snumberofpoints()
				+ ", " + TCSTablemadgicevents.datevent + " = '" + TimeCardUtilities.stdDateStringToSQLDateString(get_sdatevent()) + "'"
				+ " WHERE ("
					+ "(" + TCSTablemadgicevents.lid + " = " + get_slid() + ")"
				+ ")"
			;
		}
		
		//Start a transaction:
		try {
			clsDatabaseFunctions.start_data_transaction(conn);
		} catch (Exception e1) {
			throw new Exception("Error [1486741282] starting data transaction - " + e1.getMessage());
		}
		
		try {
	 		Statement stmt = conn.createStatement();
	 		stmt.executeUpdate(SQL);
	 	}catch (SQLException e){
	 		clsDatabaseFunctions.rollback_data_transaction(conn);
	 		clsDatabaseFunctions.freeConnection(context, conn, "[1547060133]");
	 		throw new Exception("Error [1486653655] saving " + ParamObjectName + " record with SQL: '" + SQL + "' - " + e.getMessage());
	 	}

	 	//Update the ID if it's a successful insert:
	 	if (get_slid().compareToIgnoreCase("-1") == 0){
			SQL = "SELECT last_insert_id()";
			try {
				ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
				if (rs.next()) {
					set_slid(Long.toString(rs.getLong(1)));
				}else {
					set_slid("");
				}
				rs.close();
			} catch (SQLException e) {
				set_slid("");
			}
			//If something went wrong, we can't get the last ID:
			if (get_slid().compareToIgnoreCase("") == 0){
				clsDatabaseFunctions.rollback_data_transaction(conn);
				clsDatabaseFunctions.freeConnection(context, conn, "[1547060134]");
				throw new Exception("Error [1486653656] - record was saved but the ID is incorrect");
			}
	 	}
	 	
	 	//Now save the employees:
	 	try {
			saveEmployees(get_slid(), conn, req);
		} catch (Exception e) {
			clsDatabaseFunctions.rollback_data_transaction(conn);
			clsDatabaseFunctions.freeConnection(context, conn, "[1547060135]");
			throw new Exception("Error [1486741495] - could not save employees - " + e.getMessage());
		}
	 	
	 	clsDatabaseFunctions.commit_data_transaction(conn);
	 	clsDatabaseFunctions.freeConnection(context, conn, "[1547060136]");
    }
    
    private void saveEmployees(String slid, Connection conn, HttpServletRequest req) throws Exception{
    	
    	//First, delete ALL the employees for this event:
    	String SQL = "DELETE FROM " + TCSTablemadgiceventusers.TableName
    		+ " WHERE ("
    			+ "(" + TCSTablemadgiceventusers.lmadgiceventid + " = " + slid + ")"
    		+ ")"
    	;
    	try {
			Statement stmt = conn.createStatement();
			stmt.execute(SQL);
		} catch (Exception e) {
			throw new Exception ("Error [1486741736] - could not remove existing employees from this event with SQL: '" + SQL + "' - " + e.getMessage());
		}
    	
    	//Now read the employees from the list, and insert new records for each one:
    	//Active users first:
		Enumeration<?> paramNames = req.getParameterNames();
	    String sActiveEmployeeMarker = TCEditMADGICEventsEdit.ACTIVE_EMPLOYEE_CHECKBOX_PREFIX;
	    String sINActiveEmployeeMarker = TCEditMADGICEventsEdit.INACTIVE_EMPLOYEE_CHECKBOX_PREFIX;
	    while(paramNames.hasMoreElements()) {
	    	String sParamName = (String)paramNames.nextElement();
	    	if (sParamName.contains(sActiveEmployeeMarker)){
	    		String sEmployeeID = (sParamName.substring(sParamName.indexOf(sActiveEmployeeMarker) + sActiveEmployeeMarker.length()));
	    		try {
					insertEmployee(slid, sEmployeeID, conn);
				} catch (Exception e) {
					throw new Exception("Error [1486744306] inserting active employee - " + e.getMessage());
				}
	    	}
	    	if (sParamName.contains(sINActiveEmployeeMarker)){
	    		String sEmployeeID = (sParamName.substring(sParamName.indexOf(sINActiveEmployeeMarker) + sINActiveEmployeeMarker.length()));
	    		try {
					insertEmployee(slid, sEmployeeID, conn);
				} catch (Exception e) {
					throw new Exception("Error [1486744307] inserting inactive employee - " + e.getMessage());
				}
	    	}
	    }
    }
    
    private void insertEmployee(String slid, String sEmployeeID, Connection conn) throws Exception{
		//Now add an insert statement for each employee:
		String SQL = 
			"INSERT INTO " + TCSTablemadgiceventusers.TableName + "(" 
				+ TCSTablemadgiceventusers.lmadgiceventid
				+ ", " + TCSTablemadgiceventusers.semployeefullname
				+ ", " + TCSTablemadgiceventusers.semployeeid
				+ ")"
				+ " SELECT" 
					+ " " + slid 
					+ ", CONCAT(" + Employees.sEmployeeFirstName + ", ' ', " + Employees.sEmployeeLastName + ")"
					+ ", '" + sEmployeeID + "'"
					+ " FROM " + Employees.TableName
					+ " WHERE ("
						+ "(" + Employees.sEmployeeID + " = '" + sEmployeeID + "')"
					+ ")"
				;
		//System.out.println("[1486743839] - SQL = '" + SQL + "'");
	   	try {
			Statement stmt = conn.createStatement();
			stmt.execute(SQL);
		} catch (Exception e) {
			throw new Exception ("Error [1486744055] - could not insert employees for this event with SQL: '" + SQL + "' - " + e.getMessage());
		}
    }
    private void validateEntries(Connection conn) throws Exception{
    	
    	String s = "";
    	
    	try {
    		m_seventtypename = clsValidateFormFields.validateStringField(m_seventtypename, TCSTablemadgicevents.seventtypenameLength, "Event type", false);
		} catch (Exception e1) {
			s += e1.getMessage() + "\n";
		}
      	try {
      		m_inumberofpoints = clsValidateFormFields.validateIntegerField(m_inumberofpoints, "Number of points", -1000, clsValidateFormFields.MAX_INT_VALUE);
		} catch (Exception e1) {
			s += e1.getMessage() + "\n";
		}
      	try {
      		m_sdatevent = clsValidateFormFields.validateStandardDateField(m_sdatevent, "Event date", false);
		} catch (Exception e1) {
			s += e1.getMessage() + "\n";
		}
      	
     	if (s.compareToIgnoreCase("") != 0){
     		throw new Exception(s);
     	}
     	return;
    }
    public void delete(String slid, String sDBID, ServletContext context, String sUser) throws Exception{
    	
    	Connection conn = null;
    	conn = clsDatabaseFunctions.getConnection(context, sDBID, "MySQL", this.getClass().getName() + "- user: " + sUser);
    	delete(slid, conn);
    	clsDatabaseFunctions.freeConnection(context, conn, "[1547060137]");
    	return;
    	
    }
	public void delete(String slid, Connection conn) throws Exception{
		
		try {
			clsDatabaseFunctions.start_data_transaction(conn);
		} catch (Exception e1) {
			throw new Exception("Error [1486654740] starting data transaction to delete - " + e1.getMessage());
		}
		
		//First delete all the user details:
		String SQL = "DELETE FROM " + TCSTablemadgiceventusers.TableName
				+ " WHERE ("
					+ "(" + TCSTablemadgiceventusers.lmadgiceventid + " = " + get_slid() + ")"
				+ ")"
			;
			try {
				Statement stmt = conn.createStatement();
				stmt.execute(SQL);
			} catch (Exception e) {
				clsDatabaseFunctions.rollback_data_transaction(conn);
				throw new Exception("Error [1486654354] deleting " + ParamObjectName + " with ID '" 
					+ get_slid() + " - " + e.getMessage());
			}
		
		//Now delete the type:
		SQL = "DELETE FROM " + TCSTablemadgicevents.TableName
			+ " WHERE ("
				+ "(" + TCSTablemadgicevents.lid + " = " + get_slid() + ")"
			+ ")"
		;
		try {
			Statement stmt = conn.createStatement();
			stmt.execute(SQL);
		} catch (Exception e) {
			clsDatabaseFunctions.rollback_data_transaction(conn);
			throw new Exception("Error [1486654354] deleting " + ParamObjectName + " with ID '" 
				+ get_slid() + " - " + e.getMessage());
		}
		
		try {
			clsDatabaseFunctions.commit_data_transaction(conn);
		} catch (Exception e1) {
			clsDatabaseFunctions.rollback_data_transaction(conn);
			throw new Exception("Error [1486654741] committing data transaction to delete - " + e1.getMessage());
		}
	}
	public String get_seventtypename() {
		return m_seventtypename;
	}
	public void set_seventtypename(String sName) {
		m_seventtypename = sName;
	}
	public String get_snumberofpoints() {
		return m_inumberofpoints;
	}
	public void set_snumberofpoints(String sNumberOfPoints) {
		m_inumberofpoints = sNumberOfPoints;
	}
	public String get_sdescription() {
		return m_sdescription;
	}
	public void set_sdescription(String sdescription) {
		m_sdescription = sdescription;
	}
	public String get_sdatevent() {
		return m_sdatevent;
	}
	public void set_sdatevent(String sdatevent) {
		m_sdatevent = sdatevent;
	}
	public String get_slid() {
		return m_slid;
	}
	public void set_slid(String slid) {
		m_slid = slid;
	}
	public static String getStartingMADGICReportPeriodDate(int iStartingPeriod, int iStartingYear){
		
		switch(iStartingPeriod){
		case 1:
			return "12/1/" + Integer.toString(iStartingYear);
		case 2:
			return "3/1/" + Integer.toString(iStartingYear + 1);
		case 3:
			return "6/1/" + Integer.toString(iStartingYear + 1);
		case 4:
			return "9/1/" + Integer.toString(iStartingYear + 1);
		default:
			return "";
		}
	}
	public static String getEndingMADGICReportPeriodDate(int iStartingPeriod, int iStartingYear){
		
		//Unfortunately, we have to figure out the last day of February here:
		DateFormat formatter = new SimpleDateFormat("MM/dd/yy");
		Date datFebFirst;
		int iLastDayOfFebruary = 28;
		try {
			datFebFirst = formatter.parse("02/01/" + Integer.toString(iStartingYear + 1));
			Calendar calFebFirst = Calendar.getInstance();
			calFebFirst.setTime(datFebFirst);
			iLastDayOfFebruary = calFebFirst.getActualMaximum(Calendar.DAY_OF_MONTH);
		} catch (ParseException e) {
			//Nothing to do, just keep going...
		}
		
		switch(iStartingPeriod){
		case 1:
			return "2/" + Integer.toString(iLastDayOfFebruary) + "/" + Integer.toString(iStartingYear + 1);
		case 2:
			return "5/31/" + Integer.toString(iStartingYear + 1);
		case 3:
			return "8/31/" + Integer.toString(iStartingYear + 1);
		case 4:
			return "11/30/" + Integer.toString(iStartingYear + 1);
		default:
			return "";
		}
	}
	public static String createReportingPeriodListBox(){
    	String s = "\n" + "<SELECT NAME=\"" + TimeCardUtilities.PARAM_REPORTING_PERIOD + "\">" + "\n";
    	s += "  <OPTION VALUE=\"" + "" + "\">----Select a reporting period---- " + "\n";
    	
    	Calendar calToday = Calendar.getInstance();
    	int iCurrentYear = calToday.get(Calendar.YEAR);
    	
    	for (int i = 2016; i <= iCurrentYear + 1; i++){
    		for (int j = 1; j <=4; j++){
    			String sPeriod = Integer.toString(i) + Integer.toString(j);
    			String sDisplayedValue = // "Period " + Integer.toString(j) + " "
    				TCMADGICEvent.getStartingMADGICReportPeriodDate(j, i) + " - " 
    				+ TCMADGICEvent.getEndingMADGICReportPeriodDate(j, i);
    			s += "  <OPTION VALUE=" + sPeriod + ">" + sDisplayedValue + "\n";
    		}
    	}
    	
        s += "</SELECT>" + "\n\n";
        return s;
	}
	private void initializeVariables (){
		m_slid = "-1";
		m_seventtypename = "";
		m_sdescription = "";
		m_inumberofpoints = "0";
		m_sdatevent = TimeCardUtilities.EMPTY_DATE_STRING;
	}
}
