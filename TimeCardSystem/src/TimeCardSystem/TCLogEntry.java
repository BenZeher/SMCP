package TimeCardSystem;

import java.sql.Connection;
import java.sql.Statement;

import javax.servlet.ServletContext;

import ServletUtilities.clsDatabaseFunctions;
import TCSDataDefinition.TCTablesystemlog;

public class TCLogEntry {

	public final static String LOG_OPERATION_MISC_DIAGNOSTIC = "MISCDIAGNOSTIC";
	public final static String LOG_OPERATION_QUERY_DELETE = "QUERYDELETE";
	public final static String LOG_OPERATION_QUERY = "QUERYPROCESSING";
	public final static String LOG_OPERATION_QUERY_SAVE = "QUERYSAVE";
	public final static String LOG_OPERATION_USED_TIME_CALCULATOR = "USEDTIMECALCULATOR";
	public final static String LOG_OPERATION_POSTED_TIME_ENTRIES = "POSTEDTIMEENTRIES";
	public final static String LOG_OPERATION_FINALIZED_TIME_ENTRIES = "FINALIZEDTIMEENTRIES";
	public final static String LOG_OPERATION_ADMIN_ADDED_DEFAULTPUNCH_TIME_ENTRY = "ADDEDDEFAULTPUNCH";
	public final static String LOG_OPERATION_ADMIN_SAVED_TIME_ENTRY = "TIMEENTRYSAVE";
	public final static String LOG_OPERATION_ADMIN_REMOVED_TIME_ENTRY = "TIMEENTRYREMOVE";
	public final static String LOG_OPERATION_ADMIN_SAVED_LEAVE_ADJUSTMENT = "LEAVEADJUSTMENTSAVE";
	public final static String LOG_OPERATION_ADMIN_ADDED_LEAVE_ADJUSTMENT = "LEAVEADJUSTMENTADDED";
	public final static String LOG_OPERATION_ADMIN_REMOVED_LEAVE_ADJUSTMENT = "LEAVEADJUSTMENTREMOVED";
	public final static String LOG_OPERATION_ADMIN_ADDED_VACATION_LEAVE_ADJUSTMENT = "INSERTEDVACATIONLEAVEADJUSTMENT";
	public final static String LOG_OPERATION_RECORD_BROWSER_DATA = "RECORDBROWSERDATA";

	private Connection conn;
	private ServletContext m_context;
	private String m_sDBID;
    public TCLogEntry(Connection cn)
    {
    	conn = cn;
    }
    public TCLogEntry(String sDBID, ServletContext context)
    {
    	m_context = context;
    	m_sDBID = sDBID;
    	//System.out.println("In SMLogEntry, sConfFile = " + sConfFile);
    	
    }

    public boolean writeEntry (
    		String sUser,
    		String sOperation,
    		String sDescription,
    		String sComment,
    		String sreferenceid
   		) {
    	
    	String m_sOperation = "";
    	try {
			m_sOperation = sOperation.substring(0, TCTablesystemlog.ssoperationLength - 1);
		} catch (Exception e1) {
			m_sOperation = sOperation;
		}
    	String m_sUser = "";
    	try {
    		m_sUser = sUser.substring(0, TCTablesystemlog.suserLength - 1);
		} catch (Exception e1) {
			m_sUser = sUser;
		}
    	
    	if (sComment.length() > TCTablesystemlog.scommentLength){
    		sComment = sComment.substring(0, TCTablesystemlog.scommentLength - 1);
    		
    	}
    	if (sDescription.length() > TCTablesystemlog.ssdescriptionLength){
    		sDescription = sDescription.substring(0, TCTablesystemlog.ssdescriptionLength - 1);
    	}
    	
		String SQL = "INSERT INTO " + TCTablesystemlog.TableName
			+ " ("
    			+ TCTablesystemlog.datlogdate
    			+ ", " + TCTablesystemlog.scomment
    			+ ", " + TCTablesystemlog.sdescription
    			+ ", " + TCTablesystemlog.soperation
    			+ ", " + TCTablesystemlog.sreferenceid
    			+ ", " + TCTablesystemlog.suser
			+ ") VALUES ("
				+ " NOW()"
				+ ", '" + clsDatabaseFunctions.FormatSQLStatement(sComment) + "'"
				+ ", '" + clsDatabaseFunctions.FormatSQLStatement(sDescription) + "'"
				+ ", '" + clsDatabaseFunctions.FormatSQLStatement(m_sOperation) + "'"
				+ ", '" + clsDatabaseFunctions.FormatSQLStatement(sreferenceid) + "'"
				+ ", '" + clsDatabaseFunctions.FormatSQLStatement(m_sUser) + "'"
			+ ")"
			;
		
		if (conn != null){
			try {
				Statement stmt = conn.createStatement();
				stmt.execute(SQL);
			} catch (Exception e) {
				System.out.println("Error [1517537777]" + Long.toString(System.currentTimeMillis()) 
						+ " - logging operation '" + sOperation + "' in " + this.toString() 
						+ " error using connnection: " + e.getMessage() + ".");
				return false;
			}

		}else{
			try {
				clsDatabaseFunctions.executeSQL(SQL, m_context, m_sDBID, "MySQL", this.toString() + " - user: " + sUser);
			} catch (Exception e) {
				System.out.println(System.currentTimeMillis() + " Error [1517537777] executing SQL command for " 
						+ "logging operation '" + sOperation + "', and reference ID " + sreferenceid + ", in " 
						+ this.toString() 
						+ " error using context: **" + m_context.toString() + "** and database: '" + m_sDBID + "' - " + e.getMessage() + ".");
					return false;
			}
		}
    	return true;
    }
}
