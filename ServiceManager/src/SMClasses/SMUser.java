package SMClasses;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import smcontrolpanel.SMEditUsersSelection;
import smcontrolpanel.SMMySQLs;
import SMDataDefinition.SMTableappointments;
import SMDataDefinition.SMTablecolortable;
import SMDataDefinition.SMTablereminders;
import SMDataDefinition.SMTablereminderusers;
import SMDataDefinition.SMTablesecurityusergroups;
import SMDataDefinition.SMTableusers;
import ServletUtilities.clsMasterEntry;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsManageRequestParameters;


public class SMUser extends clsMasterEntry{
	
	public static final String ParamObjectName = "SMUser";
	
	public static final String ParamsDefaultSalespersonCode = "sDefaultSalespersonCode";
	public static final String ParamsIdentifierInitials = "sIdentifierInitials";
	public static final String ParamsUserFirstName = "sUserFirstName";
	public static final String ParamsUserLastName = "sUserLastName";
	public static final String ParamsUserName = "sUserName";
	public static final String ParamsHashedPw = "sHashedPw"; 
	public static final String Paramsemail = "semail";
	public static final String Paramsmechanicinitials = "smechanicinitials";
	public static final String Paramiactive = "iactive";
	public static final String Paramsusercolorcoderow = "susercolorcoderow";
	public static final String Paramsusercolorcodecol = "susercolorcodecol";
	public static final String Paramlid= "lid";
	
	public static final String Paramcolorhex= "colorhex";

	//Other parameters
	public static final String ParamNewRecord = "NewRecord";
	public static final String ParamNewRecordValue = "1";
	
	private String m_sDefaultSalespersonCode;
	private String m_sIdentifierInitials;
	private String m_sUserFirstName;
	private String m_sUserLastName;
	private String m_sUserName;
	private String m_sHashedPw;
	private String m_semail;
	private String m_smechanicinitials;
	private String m_iactive;
	private String m_susercolorcoderow;
	private String m_susercolorcodecol;
	private String m_lid;
	
	private String m_sNewRecord;

	private static boolean bDebugMode = false;
	
    public SMUser() {
		super();
		initUserEntryVariables();
        }
    
    public SMUser(HttpServletRequest req, ServletContext context, String sDBIB){
		super(req);
		initUserEntryVariables(); 
		m_lid = clsManageRequestParameters.get_Request_Parameter(SMUser.Paramlid, req).trim();
		m_sDefaultSalespersonCode = clsManageRequestParameters.get_Request_Parameter(SMUser.ParamsDefaultSalespersonCode, req).trim();
		m_sIdentifierInitials = clsManageRequestParameters.get_Request_Parameter(SMUser.ParamsIdentifierInitials, req).trim().replace("&quot;", "\"");
		m_sUserFirstName  = clsManageRequestParameters.get_Request_Parameter(SMUser.ParamsUserFirstName, req).trim().replace("&quot;", "\"");
		m_sUserLastName = clsManageRequestParameters.get_Request_Parameter(SMUser.ParamsUserLastName, req).trim();
		m_sUserName = clsManageRequestParameters.get_Request_Parameter(SMUser.ParamsUserName, req).trim().replace("&quot;", "\"");
		
		m_sHashedPw  = clsManageRequestParameters.get_Request_Parameter(SMUser.ParamsHashedPw, req).trim().replace("&quot;", "\"");
		m_semail = clsManageRequestParameters.get_Request_Parameter(SMUser.Paramsemail, req).trim();
		m_smechanicinitials = clsManageRequestParameters.get_Request_Parameter(SMUser.Paramsmechanicinitials, req).trim().replace("&quot;", "\"");
		
		m_iactive  = clsManageRequestParameters.get_Request_Parameter(SMUser.Paramiactive, req).trim().replace("&quot;", "\"");
		if (m_iactive.compareToIgnoreCase("") == 0){
			m_iactive = "0";
		}else{
			m_iactive = "1";
		}
		if(clsManageRequestParameters.get_Request_Parameter(Paramcolorhex, req).compareToIgnoreCase("") != 0){
			String sSQL = "SELECT * FROM " + SMTablecolortable.TableName 
				+ " WHERE " + SMTablecolortable.scolorcode + " = '" 
				+ clsManageRequestParameters.get_Request_Parameter(Paramcolorhex, req).substring(1) + "'";
			try{
				ResultSet rsCoor = clsDatabaseFunctions.openResultSet(sSQL, context, sDBIB);
				if (rsCoor.next()){
					m_susercolorcoderow = Integer.toString(rsCoor.getInt(SMTablecolortable.irow));
					m_susercolorcodecol = Integer.toString(rsCoor.getInt(SMTablecolortable.icol));
				}
			}catch(SQLException ex){
				m_susercolorcoderow = "0";
				m_susercolorcodecol = "0";
			}
		}
		
		m_sNewRecord = clsManageRequestParameters.get_Request_Parameter(SMUser.ParamNewRecord, req).trim().replace("&quot;", "\"");
		if( (req.getParameter(SMEditUsersSelection.ADD_NEW_USER_BUTTON_NAME) != null || m_lid.compareToIgnoreCase("-1") == 0)
				&& req.getParameter(SMEditUsersSelection.DELETE_USER_BUTTON_NAME) == null){
			m_sNewRecord = SMUser.ParamNewRecordValue;
			m_lid = "-1";
		}
    }
    
    public void load (ServletContext context, String sDBIB, String sUser, String sUserID, String sUserFullName) throws Exception{
    	Connection conn = clsDatabaseFunctions.getConnection(
    			context, 
    			sDBIB, 
    			"MySQL", 
    			this.toString() + " - user: " + sUserID + " - " + sUserFullName
    			);
    	
    	if (conn == null){
    		throw new Exception("Error opening data connection to load SMuser entry.");
    	}
    	
    	try {
			load (conn);
		} catch (Exception e) {
			clsDatabaseFunctions.freeConnection(context, conn, "[1547067773]");
			throw new Exception(e.getMessage());
		}
    	clsDatabaseFunctions.freeConnection(context, conn, "[1547067774]");
    }
    public void load (Connection conn) throws Exception{
    	try {
    		load (m_lid, conn);
    	} catch (Exception e) {
    		throw new Exception(e.getMessage());
    	}  	
    }
    public void load (String sID, Connection conn) throws Exception{

    	sID = sID.trim();
    	if (sID.compareToIgnoreCase("") == 0){
    		throw new Exception("ID cannot be blank when loading SMUser entry.");
    	}
		
		String SQL = "SELECT * FROM " + SMTableusers.TableName
			+ " WHERE ("
				+ SMTableusers.lid + " = " + sID
			+ ")";
		if (bDebugMode){
			System.out.println("In " + this.toString() + " - load SQL = " + SQL);
		}
		try {
			ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
			if (rs.next()) {
				//Load the variables here:
				m_lid = Long.toString(rs.getLong(SMTableusers.lid));
				m_sDefaultSalespersonCode = rs.getString(SMTableusers.sDefaultSalespersonCode).trim();
				m_sIdentifierInitials = rs.getString(SMTableusers.sIdentifierInitials).trim();
				m_sUserFirstName = rs.getString(SMTableusers.sUserFirstName);
				m_sUserLastName = rs.getString(SMTableusers.sUserLastName).trim();
				m_sUserName = rs.getString(SMTableusers.sUserName);
				m_sHashedPw = rs.getString(SMTableusers.sHashedPw);
				m_semail = rs.getString(SMTableusers.semail).trim();
				m_smechanicinitials = rs.getString(SMTableusers.smechanicinitials).trim();
				m_iactive = Integer.toString(rs.getInt(SMTableusers.iactive)).trim();
				m_susercolorcoderow = rs.getString(SMTableusers.susercolorcoderow).trim();
				m_susercolorcodecol = rs.getString(SMTableusers.susercolorcodecol).trim();

				rs.close();
			} else {
				rs.close();
				throw new Exception("Error [1490535456] - could not load user with userID '" + sID + "'");
			}
		} catch (Exception e) {
			throw new Exception("Error [1490535] reading " + ParamObjectName + " for lid : '" + sID
					+ "' - " + e.getMessage());
		}
    }
    public void loadFromUserName(String sUserName, Connection conn) throws Exception{
    	sUserName = sUserName.trim();
    	if (sUserName.compareToIgnoreCase("") == 0){
    		throw new Exception("Error [1525453694] - User name cannot be blank when loading SMUser entry.");
    	}
		
		String SQL = "SELECT * FROM " + SMTableusers.TableName
			+ " WHERE ("
				+ SMTableusers.sUserName + " = '" + sUserName + "'"
			+ ")";
		if (bDebugMode){
			System.out.println("In " + this.toString() + " - load SQL = " + SQL);
		}
		try {
			ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
			if (rs.next()) {
				//Load the variables here:
				m_lid = Long.toString(rs.getLong(SMTableusers.lid));
				m_sDefaultSalespersonCode = rs.getString(SMTableusers.sDefaultSalespersonCode).trim();
				m_sIdentifierInitials = rs.getString(SMTableusers.sIdentifierInitials).trim();
				m_sUserFirstName = rs.getString(SMTableusers.sUserFirstName);
				m_sUserLastName = rs.getString(SMTableusers.sUserLastName).trim();
				m_sUserName = rs.getString(SMTableusers.sUserName);
				m_sHashedPw = rs.getString(SMTableusers.sHashedPw);
				m_semail = rs.getString(SMTableusers.semail).trim();
				m_smechanicinitials = rs.getString(SMTableusers.smechanicinitials).trim();
				m_iactive = Integer.toString(rs.getInt(SMTableusers.iactive)).trim();
				m_susercolorcoderow = rs.getString(SMTableusers.susercolorcoderow).trim();
				m_susercolorcodecol = rs.getString(SMTableusers.susercolorcodecol).trim();

				rs.close();
			} else {
				rs.close();
				throw new Exception("Error [1525453692] - could not load user with username '" + sUserName + "'");
			}
		} catch (Exception e) {
			throw new Exception("Error [1525453693] reading " + ParamObjectName + " for user name : '" + sUserName
					+ "' - " + e.getMessage());
		}
    }
    public void save_without_data_transaction (ServletContext context, String sDBIB, String sUserID, String sUserFullName) throws Exception{
    	
       	Connection conn = clsDatabaseFunctions.getConnection(
    			context, 
    			sDBIB, 
    			"MySQL", 
    			this.toString() + " - user: " + sUserID + " - " + sUserFullName
    			);
    	
    	if (conn == null){
    		throw new Exception("Error [1496637] opening data connection.");
    	}
    	
    	try {
			save_without_data_transaction (conn);
		} catch (Exception e) {
			clsDatabaseFunctions.freeConnection(context, conn, "[1547067775]");
			throw new Exception(e.getMessage());
		}
    	clsDatabaseFunctions.freeConnection(context, conn, "[1547067776]");
    	
    }
    public void save_without_data_transaction (Connection conn) throws Exception{
    	
    	//Validate fields
    	try {
			validate_entry_fields(conn);
		} catch (Exception e1) {
			throw new Exception (e1.getMessage());
		}
    	//Check if this username is already in use.
    	String SQL = ""; 	
			String sSQL = SMMySQLs.Get_User_By_Username(getsUserName());
			try{
				//System.out.println(sSQL);
				ResultSet rs = clsDatabaseFunctions.openResultSet(sSQL, conn);
				if (rs.next()){
					//This user already exists, so we can't add it:
					rs.close();
					throw new Exception("The user '" + getsUserName() + "' already exists - it cannot be added.<BR>");
				}
				rs.close();
			}catch(SQLException ex){
				throw new Exception ("[1421996275]" + ex.getMessage());
				}


		if(getsNewRecord().compareToIgnoreCase(SMUser.ParamNewRecordValue) == 0){			
			 SQL = "INSERT INTO " + SMTableusers.TableName + " ("
				+ SMTableusers.sDefaultSalespersonCode
				+ ", " + SMTableusers.sIdentifierInitials
					+ ", " + SMTableusers.sUserFirstName
					+ ", " + SMTableusers.sUserLastName
					+ ", " + SMTableusers.sUserName
					//+ ", " + SMTableusers.sHashedPw
					+ ", " + SMTableusers.semail
					+ ", " + SMTableusers.smechanicinitials
					+ ", " + SMTableusers.iactive
					+ ", " + SMTableusers.susercolorcoderow
					+ ", " + SMTableusers.susercolorcodecol
					+ ") VALUES ("
					+ "'" + clsDatabaseFunctions.FormatSQLStatement(getsDefaultSalespersonCode().trim()) + "'"
					+ ", '" + clsDatabaseFunctions.FormatSQLStatement(getsIdentifierInitials()) + "'"
					+ ", '" + clsDatabaseFunctions.FormatSQLStatement(getsUserFirstName().trim()) + "'"
					+ ", '" + clsDatabaseFunctions.FormatSQLStatement(getsUserLastName().trim()) + "'"
					+ ", '" + clsDatabaseFunctions.FormatSQLStatement(getsUserName().trim()) + "'"
					//+ ", '" + SMUtilities.FormatSQLStatement(getsHashedPw().trim()) + "'"
					+ ", '" + clsDatabaseFunctions.FormatSQLStatement(getsemail().trim()) + "'"
					+ ", '" + clsDatabaseFunctions.FormatSQLStatement(getsmechanicinitials().trim()) + "'"
					+ ", " + getiactive().trim()
					+ ", " + clsDatabaseFunctions.FormatSQLStatement(getsusercolorcoderow().trim()) + ""
					+ ", " + clsDatabaseFunctions.FormatSQLStatement(getsusercolorcodecol().trim()) + ""
					+ ")";
		}else{

			SQL = "UPDATE " +  SMTableusers.TableName
				+ " SET " + SMTableusers.sDefaultSalespersonCode + " = '" + clsDatabaseFunctions.FormatSQLStatement(getsDefaultSalespersonCode().trim()) + "'"
				+ ", " + SMTableusers.sIdentifierInitials + " = '" + clsDatabaseFunctions.FormatSQLStatement(getsIdentifierInitials()) + "'"
			+ ", " + SMTableusers.sUserFirstName + " = '" + clsDatabaseFunctions.FormatSQLStatement(getsUserFirstName()) + "'"
			+ ", " + SMTableusers.sUserLastName + " = '" + clsDatabaseFunctions.FormatSQLStatement(getsUserLastName()) + "'"
			+ ", " + SMTableusers.sUserName + " = '" + clsDatabaseFunctions.FormatSQLStatement(getsUserName().trim()) + "'"
			+ ", " + SMTableusers.semail + " = '" + clsDatabaseFunctions.FormatSQLStatement(getsemail().trim()) +"'"
			+ ", " + SMTableusers.smechanicinitials + " = '" + clsDatabaseFunctions.FormatSQLStatement(getsmechanicinitials()) + "'"
			+ ", " + SMTableusers.iactive + " = " + getiactive().trim()
		    + ", " + SMTableusers.susercolorcoderow + " = " + clsDatabaseFunctions.FormatSQLStatement(getsusercolorcoderow().trim()) + ""
			+ ", " + SMTableusers.susercolorcodecol + " = " + clsDatabaseFunctions.FormatSQLStatement(getsusercolorcodecol().trim()) + ""

			+ " WHERE (" + SMTableappointments.lid + "=" + getlid() + ")"
		;

		}
		System.out.println("In " + this.toString() + " - save SQL = " + SQL);
		if (bDebugMode){
			System.out.println("In " + this.toString() + " - save SQL = " + SQL);
		}
		try{
		    Statement stmt = conn.createStatement();
		    stmt.executeUpdate(SQL);
		}catch (Exception ex) {
			throw new Exception ("Error [142199627] in insert/update with SQL: " + SQL + " - " + ex.getMessage());
		}
		//Update the ID if it's an insert:
		if (getsNewRecord().compareToIgnoreCase(SMUser.ParamNewRecordValue) == 0){
			SQL = "SELECT last_insert_id()";
			try {
				ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
				if (rs.next()) {
					m_lid = Long.toString(rs.getLong(1));
				}else {
					m_lid = "0";
				}
				rs.close();
			} catch (SQLException e) {
				throw new Exception("Could not get last ID number - " + e.getMessage());
			}
			//If something went wrong, we can't get the last ID:
			if (m_lid.compareToIgnoreCase("0") == 0){
				throw new Exception("Could not get last ID number.");
			}
		}
    }
    
    public String getIDFromUsername( ServletContext context, String sDBIB, String sUser) throws Exception{
       	
    	Connection conn = clsDatabaseFunctions.getConnection(
    			context, 
    			sDBIB, 
    			"MySQL", 
    			this.toString() + " - user: " + sUser
    			);
    	
    	if (conn == null){
    		throw new Exception("Error [1096637] opening data connection.");
    	}
    	
    	String SQL = "";
			SQL = "SELECT " +  SMTableusers.lid
				+ " FROM " + SMTableusers.TableName 
			+ " WHERE (" + SMTableusers.lid + "=" + getlid() + ")"
		;
			
		ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
		if(rs.next()){
			clsDatabaseFunctions.freeConnection(context, conn, "[1547067771]");
			return Long.toString(rs.getLong(SMTableusers.lid));
		}
		clsDatabaseFunctions.freeConnection(context, conn, "[1547067772]");
		return "-1";
    }

	public void delete (ServletContext context, String sDBIB, String sUserID, String sUserFullName) throws Exception{
    	
    	Connection conn = clsDatabaseFunctions.getConnection(
    			context, 
    			sDBIB, 
    			"MySQL", 
    			this.toString() + " - user: " + sUserID + " - " + sUserFullName
    			);
    	
    	if (conn == null){
    		throw new Exception ("Error [142804150] opening data connection.");
    	}
    	
    	try {
			delete (conn);
		} catch (Exception e) {
			clsDatabaseFunctions.freeConnection(context, conn, "[1547067769]");
			throw new Exception(e.getMessage());
		}
    	clsDatabaseFunctions.freeConnection(context, conn, "[1547067770]");
    }
    public void delete (Connection conn) throws Exception{
    	
    	String SQL = "START TRANSACTION";
    	Statement stmt = conn.createStatement();
    	try {
    		stmt.execute(SQL);
    	} catch (SQLException e) {
    		clsDatabaseFunctions.rollback_data_transaction(conn);
    		throw new Exception("Error starting data transaction - " + e.getMessage());
    	}
    			
    	//Delete user from security user groups
    	SQL = "DELETE FROM " 
    		+ SMTablesecurityusergroups.TableName
    		+ " WHERE (" 
    		+ "(" + SMTablesecurityusergroups.luserid + " = " + getlid() + ")" 
    	+ ")";
    	stmt = conn.createStatement();
    		try {
    			stmt.execute(SQL);
    		} catch (SQLException e) {
    			clsDatabaseFunctions.rollback_data_transaction(conn);
    			throw new Exception("Error deleting user from security groups - " + e.getMessage());
    		}
    			
    	//Delete user from users table
    	SQL = "DELETE FROM "
    			+ SMTableusers.TableName
    			+ " WHERE (" 
    				+ "(" + SMTableusers.lid + " = " + getlid()  + ")"
    			+ ")";
    	stmt = conn.createStatement();
    	try {
    		stmt.execute(SQL);
    	} catch (SQLException e) {
    		clsDatabaseFunctions.rollback_data_transaction(conn);
    		throw new Exception("Error deleting user from users table - " + e.getMessage());
    	}
    			
    	//Delete all reminder user records for this user
    	SQL = "DELETE FROM " + SMTablereminderusers.TableName + " "
    		+ " WHERE (" 
    			+ "(" + SMTablereminderusers.TableName + "." + SMTablereminderusers.luserid + " = " + getlid()  + ")"
    		+ ")";
    	stmt = conn.createStatement();
    	try {
    		stmt.execute(SQL);
    	} catch (SQLException e) {
    		clsDatabaseFunctions.rollback_data_transaction(conn);
    		throw new Exception("Error deleting reminder user records in smscheduledusers table - " + e.getMessage());
    	}
    		
    	//Delete all personal reminders for this user
    	SQL = "DELETE FROM "
    		+ SMTablereminders.TableName
    		+ " WHERE (" 
    			+ "(" + SMTablereminders.lcreatedbyuserid + " = " + getlid()  + ")"
    		+ " AND "
				+ "(" + SMTablereminders.iremindermode + " = " + SMTablereminders.PERSONAL_REMINDER_VALUE  + ")"
    		+ ")";
    	stmt = conn.createStatement();
    	try {
    		stmt.execute(SQL);
    	} catch (SQLException e) {
    		clsDatabaseFunctions.rollback_data_transaction(conn);
    		throw new Exception("Error deleting personal reminder records in smschedules table - " + e.getMessage());
    	}
    			
    	SQL = "COMMIT";
    	stmt = conn.createStatement();
    	try {
    		stmt.execute(SQL);
    	} catch (SQLException e) {
    		clsDatabaseFunctions.rollback_data_transaction(conn);
    		throw new Exception("Error committing transaction - " + e.getMessage());
    	}
		//Empty the values:
		//initUserEntryVariables();
    }

    public void validate_entry_fields (Connection conn) throws Exception{
        //Validate the entries here:
    	String sErrors = "";
    	m_lid = m_lid.trim();
    	if (m_lid.compareToIgnoreCase("") == 0){
    		m_lid = "-1";
    	}
    	try {
			@SuppressWarnings("unused")
			long lID = Long.parseLong(m_lid);
		} catch (Exception e) {
			throw new Exception("Invalid ID: '" + m_lid + "'.");
		}
        
        if (sErrors.compareToIgnoreCase("") != 0){
        	throw new Exception(sErrors);
        }
    }
  

	public String getlid() {
		return m_lid;
	}

	public void setlid(String slid) {
		m_lid = slid;
	}

	public String getsDefaultSalespersonCode() {
		return m_sDefaultSalespersonCode;
	}

	public void setsDefaultSalespersonCode(String sDefaultSalespersonCode) {
		m_sDefaultSalespersonCode = sDefaultSalespersonCode;
	}

	public String getsIdentifierInitials() {
		return m_sIdentifierInitials;
	}

	public void setsIdentifierInitials(String sIdentifierInitials) {
		m_sIdentifierInitials = sIdentifierInitials;
	}

	public String getsUserFirstName() {
		return m_sUserFirstName;
	}

	public void setsUserFirstName(String sUserFirstName) {
		m_sUserFirstName = sUserFirstName;
	}

	public String getsUserLastName() {
		return m_sUserLastName;
	}

	public void setsUserLastName(String sUserLastName) {
		m_sUserLastName = sUserLastName;
	}
	
	public String getsUserFullName() {
		if(m_sUserFirstName.trim().compareToIgnoreCase("") != 0) {
			return (m_sUserFirstName + " " + m_sUserLastName).trim();
		}else {
			return "";
		}
	}

	public String getsUserName() {
		return m_sUserName;
	}

	public void setsUserName(String sUserName) {
		m_sUserName = sUserName;
	}

	public String getsHashedPw() {
		return m_sHashedPw;
	}

	public void setsHashedPw(String sHashedPw) {
		m_sHashedPw = sHashedPw;
	}

	public String getsemail() {
		return m_semail;
	}

	public void setsemail(String semail) {
		m_semail = semail;
	}

	public String getsmechanicinitials() {
		return m_smechanicinitials;
	}

	public void setsmechanicinitials(String smechanicinitials) {
		m_smechanicinitials = smechanicinitials;
	}
	
	public String getiactive() {
		return m_iactive;
	}

	public void setiactive(String iactive) {
		m_iactive = iactive;
	}
	
	public String getsusercolorcoderow() {
		return m_susercolorcoderow;
	}

	public void setsusercolorcoderow(String susercolorcoderow) {
		m_susercolorcoderow = susercolorcoderow;
	}
	
	public String getsusercolorcodecol() {
		return m_susercolorcodecol;
	}

	public void setsusercolorcodecol(String susercolorcodecol) {
		m_susercolorcodecol = susercolorcodecol;
	}
	
	public String getsNewRecord() {
		return m_sNewRecord;
	}

	public void setsNewRecord(String mSNewRecord) {
		m_sNewRecord = mSNewRecord;
	}
	public boolean bIsNewRecord(){
		if(getsNewRecord().compareToIgnoreCase(SMUser.ParamNewRecordValue) == 0){
			return true;
		}else{
			return false;
		}
		
	}
	public String getObjectName(){
		return ParamObjectName;
	}
	
    private void initUserEntryVariables(){
    	m_sDefaultSalespersonCode = "";
    	m_sIdentifierInitials = "";
    	m_sUserFirstName = "";
    	m_sUserLastName = "";
    	m_sUserName = "";
    	m_sHashedPw = "";
    	m_semail = "";
    	m_smechanicinitials = "";
    	m_iactive = "0";
    	m_susercolorcoderow = "0";
    	m_susercolorcodecol = "0";
    	m_lid = "-1";
	}


}

