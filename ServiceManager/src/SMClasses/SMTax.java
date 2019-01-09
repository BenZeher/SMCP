package SMClasses;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import SMDataDefinition.SMTableorderheaders;
import SMDataDefinition.SMTabletax;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsManageBigDecimals;
import ServletUtilities.clsManageRequestParameters;
import ServletUtilities.clsValidateFormFields;
import smcontrolpanel.SMUtilities;

public class SMTax extends java.lang.Object{

	public static final String ParamObjectName = "Tax";
	public static final String ParamsNewRecord = "sNewRecord";
	public static final String ADDING_NEW_RECORD_PARAM_VALUE_TRUE = "T";
	public static final String ADDING_NEW_RECORD_PARAM_VALUE_FALSE = "F";
	public static final String EDIT_FORM_NAME = "EDITTAXFORM";
	
	private String m_staxjurisdiction;
	private String m_bdtaxrate;
	private String m_staxdescription;
	private String m_staxtype;
	private String m_sglacct;
	private String m_sactive;
	private String m_icalculateonpurchaseorsale;
	private String m_icalculatetaxoncustomerinvoice;
	private String m_ishowinorderentry;
	private String m_ishowinaccountspayable;
	private String m_lid;
	private String m_snewrecord;
	
	public SMTax(
        ) {
		m_staxjurisdiction = "";
		m_bdtaxrate = "0.0000";
		m_staxdescription = "";
		m_staxtype = "";
		m_sglacct = "";
		m_sactive = "1";
		m_icalculateonpurchaseorsale = Integer.toString(SMTabletax.TAX_CALCULATION_BASED_ON_PURCHASE_COST);
		m_icalculatetaxoncustomerinvoice = "0";
		m_ishowinorderentry = "0";
		m_ishowinaccountspayable = "0";
		m_lid = "-1";
		m_snewrecord = ADDING_NEW_RECORD_PARAM_VALUE_FALSE;
    }
    public SMTax(HttpServletRequest req) {
    	m_staxjurisdiction = clsManageRequestParameters.get_Request_Parameter(SMTabletax.staxjurisdiction, req).trim();
		m_bdtaxrate = clsManageRequestParameters.get_Request_Parameter(SMTabletax.bdtaxrate, req).trim();
		if(m_bdtaxrate.compareToIgnoreCase("")==0){
			m_bdtaxrate = "0.0000";
		}
		m_staxdescription = clsManageRequestParameters.get_Request_Parameter(SMTabletax.sdescription, req).trim();
		m_staxtype = clsManageRequestParameters.get_Request_Parameter(SMTabletax.staxtype, req).trim();
		m_sglacct = clsManageRequestParameters.get_Request_Parameter(SMTabletax.sglacct, req).trim();
		if (req.getParameter(SMTabletax.iactive) == null){
			m_sactive = "0";
		}else{
			m_sactive = "1";
		}
		m_icalculateonpurchaseorsale = clsManageRequestParameters.get_Request_Parameter(SMTabletax.icalculateonpurchaseorsale, req).trim();
		if (req.getParameter(SMTabletax.icalculatetaxoncustomerinvoice) == null){
			m_icalculatetaxoncustomerinvoice = "0";
		}else{
			m_icalculatetaxoncustomerinvoice = "1";
		}
		if (req.getParameter(SMTabletax.ishowinorderentry) == null){
			m_ishowinorderentry = "0";
		}else{
			m_ishowinorderentry = "1";
		}
		if (req.getParameter(SMTabletax.ishowinaccountspayable) == null){
			m_ishowinaccountspayable = "0";
		}else{
			m_ishowinaccountspayable = "1";
		}
		m_lid = clsManageRequestParameters.get_Request_Parameter(SMTabletax.lid, req).trim();

		m_snewrecord = clsManageRequestParameters.get_Request_Parameter(ParamsNewRecord, req).trim();
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
			throw new Exception("Error [1454524948] - could not get connection - " + e.getMessage());
		}
    	
    	try {
			load(conn);
		} catch (Exception e) {
			throw new Exception(e.getMessage());
		}
    			
    	clsDatabaseFunctions.freeConnection(context, conn, "[1547067759]");
    }

	public void load (
    		Connection conn
    		) throws Exception{
        String SQL = "SELECT * FROM " + SMTabletax.TableName
        	+ " WHERE ("
        		+ "(" + SMTabletax.lid + " = " + get_slid() + ")"
        	+ ")"
        ;
		
		try {
			ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn); 
			if(rs.next()){
				m_staxjurisdiction = rs.getString(SMTabletax.staxjurisdiction);
				m_bdtaxrate = clsManageBigDecimals.BigDecimalToScaledFormattedString(SMTabletax.bdtaxratescale, rs.getBigDecimal(SMTabletax.bdtaxrate));
				m_staxdescription = rs.getString(SMTabletax.sdescription);
				m_staxtype = rs.getString(SMTabletax.staxtype);
				m_sglacct = rs.getString(SMTabletax.sglacct);
				m_sactive = Integer.toString(rs.getInt(SMTabletax.iactive));
				m_icalculateonpurchaseorsale = Integer.toString(rs.getInt(SMTabletax.icalculateonpurchaseorsale));
				m_icalculatetaxoncustomerinvoice = Integer.toString(rs.getInt(SMTabletax.icalculatetaxoncustomerinvoice));
				m_ishowinorderentry = Integer.toString(rs.getInt(SMTabletax.ishowinorderentry));
				m_ishowinaccountspayable = Integer.toString(rs.getInt(SMTabletax.ishowinaccountspayable));
				m_snewrecord = ADDING_NEW_RECORD_PARAM_VALUE_FALSE;
			}else{
				throw new Exception("Error [1453826285] - tax ID '" + this.get_slid() + "' does not exist.");
			}
			rs.close();
		}catch (SQLException ex){
			throw new Exception("Error [1453826286] loading tax with lid '" + m_lid + "' using SQL: " + SQL + " - " + ex.getMessage());
		}
	}
    public void save(ServletContext context, String sConf, String sUserName) throws Exception{
		
    	//Get connection
		Connection conn;
		try {
			conn = clsDatabaseFunctions.getConnectionWithException(
				context, 
				sConf, 
				"MySQL", 
				SMUtilities.getFullClassName(this.toString()) + ":save - user: " + sUserName
			);
		} catch (Exception e1) {
			throw new Exception("Error [1453836849] - could not get connection to save.");
		}
		
		//Validate entries
		try {
			validateEntries(conn);
		} catch (Exception e1) {
			clsDatabaseFunctions.freeConnection(context, conn, "[1547067760]");
			throw new Exception(e1.getMessage());
		}
		
		//If it's a NEW tax, do an insert:
		String SQL = "";
		if (get_snewrecord().compareToIgnoreCase(ADDING_NEW_RECORD_PARAM_VALUE_TRUE) == 0){
			SQL = "INSERT INTO " + SMTabletax.TableName + "("
				+ SMTabletax.bdtaxrate
				+ ", " + SMTabletax.iactive
				+ ", " + SMTabletax.icalculateonpurchaseorsale
				+ ", " + SMTabletax.icalculatetaxoncustomerinvoice
				+ ", " + SMTabletax.ishowinaccountspayable
				+ ", " + SMTabletax.ishowinorderentry
				+ ", " + SMTabletax.sdescription
				+ ", " + SMTabletax.sglacct
				+ ", " + SMTabletax.staxjurisdiction
				+ ", " + SMTabletax.staxtype
			+ ") VALUES ("
				+ get_bdtaxrate()
				+ ", " + get_sactive()
				+ ", " + get_scalculateonpurchaseorsale()
				+ ", " + get_scalculatetaxoncustomerinvoice()
				+ ", " + get_sshowinaccountspayable()
				+ ", " + get_sshowinorderentry()
				+ ", '" + clsDatabaseFunctions.FormatSQLStatement(get_staxdescription()) + "'"
				+ ", '" + clsDatabaseFunctions.FormatSQLStatement(get_sglacct()) + "'"
				+ ", '" + clsDatabaseFunctions.FormatSQLStatement(get_staxjurisdiction()) + "'"
				+ ", '" + clsDatabaseFunctions.FormatSQLStatement(get_staxtype()) + "'"
				+ ")"
			;
		}
		//If it's NOT a new tax, do an update:
		else{
			SQL = "UPDATE " + SMTabletax.TableName + " SET"
				+ " " + SMTabletax.bdtaxrate + " = " + get_bdtaxrate()
				+ ", " + SMTabletax.iactive + " = " + get_sactive()
				+ ", " + SMTabletax.icalculateonpurchaseorsale + " = " + get_scalculateonpurchaseorsale()
				+ ", " + SMTabletax.icalculatetaxoncustomerinvoice + " = " + get_scalculatetaxoncustomerinvoice()
				+ ", " + SMTabletax.ishowinaccountspayable + " = " + get_sshowinaccountspayable()
				+ ", " + SMTabletax.ishowinorderentry + " = " + get_sshowinorderentry()
				+ ", " + SMTabletax.sdescription + " = '" + clsDatabaseFunctions.FormatSQLStatement(get_staxdescription()) + "'"
				+ ", " + SMTabletax.sglacct + " = '" + clsDatabaseFunctions.FormatSQLStatement(get_sglacct()) + "'"
				+ ", " + SMTabletax.staxjurisdiction + " = '" + clsDatabaseFunctions.FormatSQLStatement(get_staxjurisdiction()) + "'"
				+ ", " + SMTabletax.staxtype + " = '" + clsDatabaseFunctions.FormatSQLStatement(get_staxtype()) + "'"
				+ " WHERE ("
					+ "(" + SMTabletax.lid + " = " + this.get_slid() + ")"
				+ ")"
			;
		}

	 	try {
	 		Statement stmt = conn.createStatement();
	 		stmt.executeUpdate(SQL);
	 	}catch (SQLException e){
	 		clsDatabaseFunctions.freeConnection(context, conn, "[1547067761]");
	 		throw new Exception("Error [1453839347] saving " + SMTax.ParamObjectName + " record - " + e.getMessage());
	 	}

	 	//Update the ID if it's a successful insert:
	 	if (get_snewrecord().compareToIgnoreCase(ADDING_NEW_RECORD_PARAM_VALUE_TRUE) == 0){
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
				throw new Exception("Error [1453839348] - record was saved but the ID is incorrect");
			}
	 	}
	 	set_snewrecord(ADDING_NEW_RECORD_PARAM_VALUE_FALSE);
	 	clsDatabaseFunctions.freeConnection(context, conn, "[1547067762]");
    }
    private void validateEntries(Connection conn) throws Exception{
    	
    	String s = "";
    	
    	try {
    		m_staxjurisdiction = clsValidateFormFields.validateStringField(m_staxjurisdiction, SMTabletax.staxjurisdictionLength, "Tax jurisdiction", false);
		} catch (Exception e1) {
			s += e1.getMessage() + "\n";
		}
       	try {
       		m_staxdescription = clsValidateFormFields.validateStringField(m_staxdescription, SMTabletax.sdescriptionLength, "Description", false);
		} catch (Exception e1) {
			s += e1.getMessage() + "\n";
		}
       	try {
       		m_staxtype = clsValidateFormFields.validateStringField(m_staxtype, SMTabletax.staxtypeLength, "Tax type", false);
		} catch (Exception e1) {
			s += e1.getMessage() + "\n";
		}
       	try {
       		m_sglacct = clsValidateFormFields.validateStringField(m_sglacct, SMTabletax.sglacctLength, "GL Account", false);
		} catch (Exception e1) {
			s += e1.getMessage() + "\n";
		}
       	try {
			m_sactive = clsValidateFormFields.validateIntegerField(m_sactive, "Active", 0, 1);
		} catch (Exception e1) {
			s += e1.getMessage() + "\n";
		}
      	try {
      		m_icalculatetaxoncustomerinvoice = clsValidateFormFields.validateIntegerField(m_icalculatetaxoncustomerinvoice, "Display tax on customer invoice", 0, 1);
		} catch (Exception e1) {
			s += e1.getMessage() + "\n";
		}
      	try {
      		m_ishowinorderentry = clsValidateFormFields.validateIntegerField(m_ishowinorderentry, "List tax in order entry", 0, 1);
		} catch (Exception e1) {
			s += e1.getMessage() + "\n";
		}
      	try {
      		m_ishowinaccountspayable = clsValidateFormFields.validateIntegerField(m_ishowinaccountspayable, "List tax in accounts payable", 0, 1);
		} catch (Exception e1) {
			s += e1.getMessage() + "\n";
		}
     	try {
     		m_icalculateonpurchaseorsale = clsValidateFormFields.validateIntegerField(
     			m_icalculateonpurchaseorsale, 
     			"Calculation type", 
     			SMTabletax.TAX_CALCULATION_BASED_ON_PURCHASE_COST, 
     			SMTabletax.TAX_CALCULATION_BASED_ON_SELLING_PRICE);
		} catch (Exception e1) {
			s += e1.getMessage() + "\n";
		}

     	try {
			m_bdtaxrate = clsValidateFormFields.validateBigdecimalField(
					m_bdtaxrate, 
					"Tax rate", 
					SMTabletax.bdtaxratescale, 
					BigDecimal.ZERO, 
					new BigDecimal("100.0000")
			);
		} catch (Exception e1) {
			s += e1.getMessage() + "\n";
		}

     	try {
			m_lid = clsValidateFormFields.validateLongIntegerField(m_lid, "Tax ID", -1L, clsValidateFormFields.MAX_LONG_VALUE);
		} catch (Exception e1) {
			s += e1.getMessage() + "\n";
		}
     	
     	m_snewrecord = m_snewrecord.trim();
     	if (
     		(m_snewrecord.compareToIgnoreCase(SMTax.ADDING_NEW_RECORD_PARAM_VALUE_FALSE) == 0)
     		|| (m_snewrecord.compareToIgnoreCase(SMTax.ADDING_NEW_RECORD_PARAM_VALUE_TRUE) == 0)
     	){
     	}else{
     		s += "New record flag '" + m_snewrecord + "' is invalid.";
     	}

     	if (s.compareToIgnoreCase("") != 0){
     		throw new Exception(s);
     	}
     	return;
    	
    }
	public void delete(String slid, Connection conn) throws Exception{
		
		//TODO - take this out after the ID gets saved in Order Entry:
		//It's only used to load the jurisdiction and tax type:
		load(conn);
		
		//First check to see if this tax is in use on any orders:
		String SQL = "SELECT"
			+ " " + SMTableorderheaders.strimmedordernumber
			+ " FROM " + SMTableorderheaders.TableName
			+ " WHERE ("
				+ "(" + SMTableorderheaders.staxjurisdiction + " = '" + get_staxjurisdiction() + "')"
				+ " AND (" + SMTableorderheaders.itaxid + " = " + get_slid() + ")"
			+ ")"
		;
		try {
			ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
			if (rs.next()){
				throw new Exception("Cannot delete - this tax is currently in use on some orders.");
			}
			rs.close();
		} catch (Exception e) {
			throw new Exception("Error [1453835423] checking orders for tax jurisdiction '" 
				+ get_staxjurisdiction() + "', tax type '" + get_staxtype() + "' - " + e.getMessage());
		}
		
		//Now delete the tax:
		SQL = "DELETE FROM " + SMTabletax.TableName
			+ " WHERE ("
				+ "(" + SMTabletax.lid + " = " + get_slid() + ")"
			+ ")"
		;
		try {
			Statement stmt = conn.createStatement();
			stmt.execute(SQL);
		} catch (Exception e) {
			throw new Exception("Error [1453835424] deleting tax with jurisdiction '" 
				+ get_staxjurisdiction() + "', tax type '" + get_staxtype() + " - " + e.getMessage());
		}
	}
	public String get_staxjurisdiction() {
		return m_staxjurisdiction;
	}
	public void set_staxjurisdiction(String taxJurisdiction) {
		m_staxjurisdiction = taxJurisdiction;
	}
	public String get_bdtaxrate() {
		return m_bdtaxrate;
	}
	public void set_bdtaxrate(String staxRate) {
		m_bdtaxrate = staxRate;
	}
	public String get_staxdescription() {
		return m_staxdescription;
	}
	public void set_staxdescription(String staxdescription) {
		m_staxdescription = staxdescription;
	}
	public String get_staxtype() {
		return m_staxtype;
	}
	public void set_staxtype(String staxtype) {
		m_staxtype = staxtype;
	}
	public String get_sglacct() {
		return m_sglacct;
	}
	public void set_sglacct(String sglacct) {
		m_sglacct = sglacct;
	}
	public String get_sactive() {
		return m_sactive;
	}
	public void set_sactive(String sactive) {
		m_sactive = sactive;
	}
	public String get_scalculateonpurchaseorsale() {
		return m_icalculateonpurchaseorsale;
	}
	public void set_scalculateonpurchaseorsale(String scalculateonpurchaseorsale) {
		m_icalculateonpurchaseorsale = scalculateonpurchaseorsale;
	}
	public String get_scalculatetaxoncustomerinvoice() {
		return m_icalculatetaxoncustomerinvoice;
	}
	public void set_scalculatetaxoncustomerinvoice(String sdisplaytaxoncustomerinvoice) {
		m_icalculatetaxoncustomerinvoice = sdisplaytaxoncustomerinvoice;
	}
	public String get_sshowinorderentry() {
		return m_ishowinorderentry;
	}
	public void set_sshowinorderentry(String sshowinorderentry) {
		m_ishowinorderentry = sshowinorderentry;
	}
	public String get_sshowinaccountspayable() {
		return m_ishowinaccountspayable;
	}
	public void set_sshowinaccountspayable(String sshowinaccountspayable) {
		m_ishowinaccountspayable = sshowinaccountspayable;
	}
	public String get_slid() {
		return m_lid;
	}
	public void set_slid(String slid) {
		m_lid = slid;
	}
	public void set_snewrecord(String snewrecord){
		m_snewrecord = snewrecord;
	}
	public String get_snewrecord(){
		return m_snewrecord;
	}
	
	public static BigDecimal Get_Tax_Rate(String iTaxID, Connection conn) throws Exception{
		
		//Public Function Get_Tax_Rate(sTaxJuris As String, iTaxTp As Integer) As Double
		BigDecimal bdRate = new BigDecimal("0.0000");
		String SQL = "SELECT * FROM"
			+ " " + SMTabletax.TableName
			+ " WHERE (" 
				+ "(" + SMTabletax.lid + " = " + iTaxID + ")"
			+ ")"
		;
		try{
			ResultSet rsTax = clsDatabaseFunctions.openResultSet(SQL, conn);
			if (rsTax.next()){
				bdRate = rsTax.getBigDecimal(SMTabletax.bdtaxrate);
			}else{
				bdRate = new BigDecimal("-1.0000");
			}
			rsTax.close();
		}catch (SQLException ex){
			throw new Exception("Error [1453827763] - could not read tax rate - " + ex.getMessage());
		}
		return bdRate;
	}
}