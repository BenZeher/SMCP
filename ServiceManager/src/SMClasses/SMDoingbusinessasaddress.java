package SMClasses;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import SMDataDefinition.SMTabledoingbusinessasaddresses;
import ServletUtilities.clsMasterEntry;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsManageRequestParameters;



public class SMDoingbusinessasaddress extends clsMasterEntry{

	public static final String FORM_NAME = "MAINFORM";
	
	public static final String ParamObjectName = "Doing Business As Address";
	public static final String Paramlid= "lid";
	public static final String Paramsdescription= "sdescription";
	public static final String Paramslogo = "slogo";
	public static final String Parammaddress = "maddress";
	public static final String Parammremittoaddress = "mremittoaddress";
	public static final String ParamNewRecord = "NewRecord";
	public static final String ParamiInvoicelogo = "sInvoiceLogo";
	public static final String ParamiProposallogo = "sProposalLogo";
	public static final String ParamiDeliveryTicketReceiptLogo = "sDeliveryTicketReceiptLogo";
	public static final String ParamiWorkOrderReceiptLogo = "sWorkOrderReceiptLogo";

	
	private String m_slid;
	private String m_sdescription;
	private String m_slogo;
	private String m_maddress;
	private String m_mremittoaddress;
	private String m_sInvoiceLogo;
	private String m_sProposalLogo;
	private String m_sDeliveryTicketReceiptLogo;
	private String m_sWorkOrderReceiptLogo;

	private String m_sNewRecord;
	private boolean bDebugMode = false;
	
    public SMDoingbusinessasaddress() {
		super();
		initEntryVariables();
        }
    
    public SMDoingbusinessasaddress(HttpServletRequest req){
		super(req);
		initEntryVariables();
		
		m_slid = clsManageRequestParameters.get_Request_Parameter(Paramlid, req).trim();

		m_sdescription = clsManageRequestParameters.get_Request_Parameter(
				Paramsdescription, req).trim().replace("&quot;", "\"");

		m_slogo = clsManageRequestParameters.get_Request_Parameter(
				Paramslogo, req).trim().replace("&quot;", "\"");
		
		m_maddress = clsManageRequestParameters.get_Request_Parameter(
				Parammaddress, req).replace("&quot;", "\"");
		
		m_mremittoaddress = clsManageRequestParameters.get_Request_Parameter(
				Parammremittoaddress, req).replace("&quot;", "\"");
		
		m_sNewRecord = clsManageRequestParameters.get_Request_Parameter(
				ParamNewRecord, req).trim().replace("&quot;", "\"");
		
		m_sInvoiceLogo  = clsManageRequestParameters.get_Request_Parameter(
				ParamiInvoicelogo, req).trim().replace("&quot;", "\"");
		
		m_sProposalLogo = clsManageRequestParameters.get_Request_Parameter(
				ParamiProposallogo, req).trim().replace("&quot;", "\"");
		
		m_sDeliveryTicketReceiptLogo  = clsManageRequestParameters.get_Request_Parameter(
				ParamiDeliveryTicketReceiptLogo, req).trim().replace("&quot;", "\"");
		
		m_sWorkOrderReceiptLogo  = clsManageRequestParameters.get_Request_Parameter(
				ParamiWorkOrderReceiptLogo, req).trim().replace("&quot;", "\"");
    }
    
    public void checkLogoFileName() throws Exception{
    		if((m_sInvoiceLogo.compareToIgnoreCase("") == 0) ||
    			(m_sProposalLogo.compareToIgnoreCase("") == 0) ||
    			(m_sDeliveryTicketReceiptLogo.compareToIgnoreCase("") == 0) ||
    			(m_sWorkOrderReceiptLogo.compareToIgnoreCase("") == 0)) {
    			throw new Exception("ERROR[1544030331] The invoice, proposal, delivery ticket receipt, or work order receipt logo file name CANNOT BE BLANK");
    		}
    }

	public void load (ServletContext context, String sDBIB, String sUserID, String sUserFullName) throws Exception{
    	Connection conn = clsDatabaseFunctions.getConnection(
    			context, 
    			sDBIB, 
    			"MySQL", 
    			this.toString() 
    			+ " - user: " 
    			+ sUserID
    			+" - "
    			+ sUserFullName
    			);
    	
    	if (conn == null){
    		throw new Exception("Error opening data connection to load " + ParamObjectName + ".");
    	}
    	
    	try {
			load (conn);
		} catch (Exception e) {
			clsDatabaseFunctions.freeConnection(context, conn, "[1547067690]");
			throw new Exception(e.getMessage());
		}
    	clsDatabaseFunctions.freeConnection(context, conn, "[1547067691]");
    }
    public void load (Connection conn) throws Exception{
    	try {
			load (m_slid, conn);
		} catch (Exception e) {
			throw new Exception(e.getMessage());
		}
    }
    private void load (String sID, Connection conn) throws Exception{
    	
    	sID = sID.trim();
    	if (sID.compareToIgnoreCase("") == 0){
    		throw new Exception("ID can not be blank when loading " + ParamObjectName + ".");
    	}
		
		String SQL = "SELECT * FROM " + SMTabledoingbusinessasaddresses.TableName
					+ " WHERE ("
					+ SMTabledoingbusinessasaddresses.lid + " = " + sID
					+ ")";
		if (bDebugMode){
			System.out.println("In " + this.toString() + " - load SQL = " + SQL);
		}
		try {
			ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
			if (rs.next()) {
				//Load the variables here:
				m_slid = Long.toString(rs.getLong(SMTabledoingbusinessasaddresses.lid));
				m_sdescription = rs.getString(SMTabledoingbusinessasaddresses.sDescription).trim();
				m_slogo =  rs.getString(SMTabledoingbusinessasaddresses.sLogo).trim();
				m_maddress =  rs.getString(SMTabledoingbusinessasaddresses.mAddress);
				m_mremittoaddress =  rs.getString(SMTabledoingbusinessasaddresses.mRemitToAddress);
				m_sInvoiceLogo = rs.getString(SMTabledoingbusinessasaddresses.sInvoiceLogo);
				m_sProposalLogo = rs.getString(SMTabledoingbusinessasaddresses.sProposalLogo);
				m_sDeliveryTicketReceiptLogo = rs.getString(SMTabledoingbusinessasaddresses.sDeliveryTicketReceiptLogo);
				m_sWorkOrderReceiptLogo = rs.getString(SMTabledoingbusinessasaddresses.sWorkOrderReceiptlogo);
				rs.close();
			} else {
				rs.close();
				throw new Exception("Error [1518811443] Could not load record with ID '" + sID + "'.");
			}
		} catch (Exception e) {
			throw new Exception("Error [1518811444] reading " + ParamObjectName + " for lid : '" + sID
				+ "' - " + e.getMessage());
		}
    }

    
    public void save_without_data_transaction (ServletContext context, String sDBIB, String sUserID, String sUserFullName) throws Exception{
    	
       	Connection conn = clsDatabaseFunctions.getConnection(
    			context, 
    			sDBIB, 
    			"MySQL", 
    			this.toString() + " - user: " 
    			+ sUserID
    			+ " - "
    			+ sUserFullName
    			);
    	
    	if (conn == null){
    		throw new Exception("Error [1518815497] opening data connection.");
    	}
    	
    	try {
			save_without_data_transaction (conn, sUserID);
		} catch (Exception e) {
			clsDatabaseFunctions.freeConnection(context, conn, "[1547067692]");
			throw new Exception(e.getMessage());
		}
    	clsDatabaseFunctions.freeConnection(context, conn, "[1547067693]");
    	
    }
    public void save_without_data_transaction (Connection conn, String sUserID) throws Exception{
    	try {
    		checkLogoFileName();
			validate_entry_fields(conn);
		} catch (Exception e1) {
			throw new Exception (e1.getMessage());
		}

    	String SQL = "";
		//If it's a new record, do an insert

    	if (getsNewRecord().compareToIgnoreCase("1") == 0){
    		SQL = "INSERT INTO " + SMTabledoingbusinessasaddresses.TableName + " ("
				+ " " + SMTabledoingbusinessasaddresses.sDescription
				+ ", " + SMTabledoingbusinessasaddresses.sLogo
				+ ", " + SMTabledoingbusinessasaddresses.mAddress
				+ ", " + SMTabledoingbusinessasaddresses.mRemitToAddress
				+ ", " + SMTabledoingbusinessasaddresses.mComments
				+ ", " + SMTabledoingbusinessasaddresses.sInvoiceLogo
				+ ", " + SMTabledoingbusinessasaddresses.sProposalLogo
				+ ", " + SMTabledoingbusinessasaddresses.sDeliveryTicketReceiptLogo
				+ ", " + SMTabledoingbusinessasaddresses.sWorkOrderReceiptlogo
				+ ") VALUES ("
				+ " '" + clsDatabaseFunctions.FormatSQLStatement(getsdescription()).trim() + "'"			
				+ ", '" + clsDatabaseFunctions.FormatSQLStatement(getslogo()).trim() + "'"
				+ ", '" + clsDatabaseFunctions.FormatSQLStatement(getmaddress()) + "'"
				+ ", '" + clsDatabaseFunctions.FormatSQLStatement(getmremittoaddress()) + "'"
				+ ", ''"	
				+ ", '" + clsDatabaseFunctions.FormatSQLStatement(getsInvoiceLogo()) + "'"
				+ ", '" + clsDatabaseFunctions.FormatSQLStatement(getsProposalLogo()) + "'"
				+ ", '" + clsDatabaseFunctions.FormatSQLStatement(getsDeliveryTicketReceiptLogo()) + "'"
				+ ", '" + clsDatabaseFunctions.FormatSQLStatement(getsWorkOrderReceiptLogo()) + "'"
				+ ")"
			;

    	}else{
    		
			SQL = " UPDATE " + SMTabledoingbusinessasaddresses.TableName + " SET "
			+ " " + SMTabledoingbusinessasaddresses.sDescription + " = '" + clsDatabaseFunctions.FormatSQLStatement(getsdescription().trim()) + "'"
			+ ", " + SMTabledoingbusinessasaddresses.sLogo  + " = '" + clsDatabaseFunctions.FormatSQLStatement(getslogo().trim()) + "'"
			+ ", " + SMTabledoingbusinessasaddresses.mAddress  + " = '" + clsDatabaseFunctions.FormatSQLStatement(getmaddress()) + "'"
			+ ", " + SMTabledoingbusinessasaddresses.mRemitToAddress  + " = '" + clsDatabaseFunctions.FormatSQLStatement(getmremittoaddress()) + "'"
			+ ", " + SMTabledoingbusinessasaddresses.sInvoiceLogo  + " = '" + clsDatabaseFunctions.FormatSQLStatement(getsInvoiceLogo()) + "'"			
			+ ", " + SMTabledoingbusinessasaddresses.sProposalLogo  + " = '" + clsDatabaseFunctions.FormatSQLStatement(getsProposalLogo()) + "'"
			+ ", " + SMTabledoingbusinessasaddresses.sDeliveryTicketReceiptLogo + " = '" + clsDatabaseFunctions.FormatSQLStatement(getsDeliveryTicketReceiptLogo()) + "'"
			+ ", " + SMTabledoingbusinessasaddresses.sWorkOrderReceiptlogo  + " = '" + clsDatabaseFunctions.FormatSQLStatement(getsWorkOrderReceiptLogo()) + "'"			
			+ " WHERE ("
				+ "(" + SMTabledoingbusinessasaddresses.lid + " = " + getslid() + ")"
			+ ")"
			;
    	}

		try{
			//System.out.println("[1519242836] Executing SQL: " + SQL);
		    Statement stmt = conn.createStatement();
		    stmt.executeUpdate(SQL);
		}catch (Exception ex) {
			throw new Exception ("Error [1518815498] in insert/update with SQL: " + SQL + " - " + ex.getMessage());
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
    			this.toString() + " - user: " 
    			+ sUserID
    			+ " - "
    			+ sUserFullName
    			);
    	
    	if (conn == null){
    		throw new Exception ("Error [1518815653] opening data connection.");
    	}
    	
    	try {
			delete (conn);
		} catch (Exception e) {
			clsDatabaseFunctions.freeConnection(context, conn, "[1547067688]");
			throw new Exception(e.getMessage());
		}
    	clsDatabaseFunctions.freeConnection(context, conn, "[1547067689]");
    }
    public void delete (Connection conn) throws Exception{
    	
    	//Validate deletions
    	String SQL = "";

    	//Don't need a data transaction if we are just doing a single SQL execute:
    	SQL = "DELETE FROM " + SMTabledoingbusinessasaddresses.TableName
    		+ " WHERE ("
    			+ SMTabledoingbusinessasaddresses.lid + " = " + this.getslid()
    		+ ")"
    		;
    	
		try{
		    Statement stmt = conn.createStatement();
		    stmt.executeUpdate(SQL);
		}catch (Exception ex) {
			throw new Exception("Error [1518815654] - Could not delete " + ParamObjectName + " with ID " + getslid() + " with SQL: " + SQL + " - " + ex.getMessage());
		}
		
		//Empty the values:
		initEntryVariables();
    }

    public void validate_entry_fields (Connection conn) throws Exception{
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

	public String getsdescription() {
		return m_sdescription;
	}
	public void setsdescription(String sdescription) {
		m_sdescription = sdescription;
	}
	public String getsInvoiceLogo() {
		return m_sInvoiceLogo;
	}
	public void setsInvoiceLogo(String iInvoiceLogo) {
		m_sInvoiceLogo = iInvoiceLogo;
	}
	public String getsProposalLogo() {
		return m_sProposalLogo;
	}
	public void setsProposalLogo(String iProposalLogo) {
		m_sProposalLogo = iProposalLogo;
	}
	public String getsDeliveryTicketReceiptLogo() {
		return m_sDeliveryTicketReceiptLogo;
	}
	public void setsDeliveryTicketReceiptLogo(String iDeliveryTicketReceiptLogo) {
		m_sDeliveryTicketReceiptLogo = iDeliveryTicketReceiptLogo;
	}
	public String getsWorkOrderReceiptLogo() {
		return m_sWorkOrderReceiptLogo;
	}
	public void setsWorkOrderReceiptLogo(String iWorkOrderReceiptLogo) {
		m_sWorkOrderReceiptLogo = iWorkOrderReceiptLogo;
	}

	public String getslogo() {
		return m_slogo;
	}
	public void setslogo(String slogo) {
		m_slogo = slogo;
	}
	public String getmaddress() {
		return m_maddress;
	}
	public void setmaddress(String maddress) {
		m_maddress = maddress;
	}
	public String getmremittoaddress() {
		return m_mremittoaddress;
	}
	public void setmremittoaddress(String mremittoaddress) {
		m_mremittoaddress = mremittoaddress;
	}

	public String getsNewRecord() {
		return m_sNewRecord;
	}
	public void setsNewRecord(String mSNewRecord) {
		m_sNewRecord = mSNewRecord;
	}
	

	public String getObjectName(){
		return ParamObjectName;
	}
	
	private void initEntryVariables(){
    	m_slid = "-1";
    	m_sdescription = "";
    	m_slogo = "YES";
    	m_maddress = "";
    	m_mremittoaddress = "";
    	m_sNewRecord = "1";
	}
}
