package smgl;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import SMDataDefinition.SMTableglaccounts;
import SMDataDefinition.SMTablegltransactionbatchlines;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsDateAndTimeConversions;
import ServletUtilities.clsManageBigDecimals;
import ServletUtilities.clsValidateFormFields;
import smcontrolpanel.SMUtilities;

public class GLTransactionBatchLine {

	public static final String ssourceledger = "ssourceledger";
	public static final String ssourcetype = "ssourcetype";
	
	private String m_slid;
	private String m_sbatchnumber;
	private String m_sentrynumber;
	private String m_slinenumber;
	private String m_sdescription;
	private String m_sreference;
	private String m_scomment;
	private String m_sdattransactiondate;
	private String m_sacctid;
	private String m_sdebitamt;
	private String m_screditamt;
	private String m_ssourceledger;
	private String m_ssourcetype;
	
	public GLTransactionBatchLine() 
	{
		initializeVariables();
	}
	
	public void save_without_data_transaction (Connection conn, String sUserName, boolean bBatchIsBeingPosted) throws Exception{

		try {
			validate_fields(conn, bBatchIsBeingPosted);
		} catch (Exception e1) {
			throw new Exception(e1.getMessage());
		}
		
		String SQL = "";
		SQL = "INSERT into " + SMTablegltransactionbatchlines.TableName
			+ " (" 
			+ SMTablegltransactionbatchlines.bdcreditamt
			+ ", " + SMTablegltransactionbatchlines.bddebitamt
			+ ", " + SMTablegltransactionbatchlines.dattransactiondate
			+ ", " + SMTablegltransactionbatchlines.lbatchnumber
			+ ", " + SMTablegltransactionbatchlines.lentrynumber
			+ ", " + SMTablegltransactionbatchlines.llinenumber
			+ ", " + SMTablegltransactionbatchlines.sacctid
			+ ", " + SMTablegltransactionbatchlines.scomment
			+ ", " + SMTablegltransactionbatchlines.sdescription
			+ ", " + SMTablegltransactionbatchlines.sreference
			+ ", " + SMTablegltransactionbatchlines.ssourceledger
			+ ", " + SMTablegltransactionbatchlines.ssourcetype
			+ ")"
			+ " VALUES ("
			+ "" + getscreditamt().trim().replaceAll(",", "")
			+ ", " + getsdebitamt().trim().replaceAll(",", "")
			+ ", '" + getstransactiondateInSQLFormat() + "'"
			+ ", " + getsbatchnumber()
			+ ", " + getsentrynumber()
			+ ", " + getslinenumber()
			+ ", '" + clsDatabaseFunctions.FormatSQLStatement(getsacctid()) + "'"
			+ ", '" + clsDatabaseFunctions.FormatSQLStatement(getscomment()) + "'"
			+ ", '" + clsDatabaseFunctions.FormatSQLStatement(getsdescription()) + "'"
			+ ", '" + clsDatabaseFunctions.FormatSQLStatement(getsreference()) + "'"
			+ ", '" + clsDatabaseFunctions.FormatSQLStatement(getssourceledger()) + "'"
			+ ", '" + clsDatabaseFunctions.FormatSQLStatement(getssourcetype()) + "'"
			+ ")"
			
			+ " ON DUPLICATE KEY UPDATE"
			+ " " + SMTablegltransactionbatchlines.bdcreditamt + " = " + getscreditamt().trim().replace(",", "")
			+ ", " + SMTablegltransactionbatchlines.bddebitamt + " = " + getsdebitamt().trim().replace(",", "")
			+ ", " + SMTablegltransactionbatchlines.dattransactiondate + " = '" + getstransactiondateInSQLFormat() + "'"
			+ ", " + SMTablegltransactionbatchlines.lbatchnumber + " = " + getsbatchnumber()
			+ ", " + SMTablegltransactionbatchlines.lentrynumber + " = " + getsentrynumber()
			+ ", " + SMTablegltransactionbatchlines.llinenumber + " = " + getslinenumber()
			+ ", " + SMTablegltransactionbatchlines.sacctid + " = '" + clsDatabaseFunctions.FormatSQLStatement(getsacctid()) + "'"
			+ ", " + SMTablegltransactionbatchlines.scomment + " = '" + clsDatabaseFunctions.FormatSQLStatement(getscomment()) + "'"
			+ ", " + SMTablegltransactionbatchlines.sdescription + " = '" + clsDatabaseFunctions.FormatSQLStatement(getsdescription()) + "'"
			+ ", " + SMTablegltransactionbatchlines.sreference + " = '" + clsDatabaseFunctions.FormatSQLStatement(getsreference()) + "'"
			+ ", " + SMTablegltransactionbatchlines.ssourceledger + " = '" + clsDatabaseFunctions.FormatSQLStatement(getssourceledger()) + "'"
			+ ", " + SMTablegltransactionbatchlines.ssourcetype + " = '" + clsDatabaseFunctions.FormatSQLStatement(getssourcetype()) + "'"		
			;
		
		//System.out.println("[1497979197] Line insert SQL = " + SQL);
		
		Statement stmt = conn.createStatement();
		try {
			stmt.execute(SQL);
		} catch (Exception e) {
			throw new Exception("Error [1555095205] updating GL transaction batch entry " + getsentrynumber() + ", line " + getslinenumber() 
				+ " with SQL: '" + SQL + "' - " + e.getMessage());
		}

		//If the batch was newly created, get the new batch number:

		if (getslid().compareToIgnoreCase("-1") == 0){
			String sSQL = "SELECT last_insert_id()";
			try {
				ResultSet rs = clsDatabaseFunctions.openResultSet(sSQL, conn);
				if (rs.next()) {
					setslid(Long.toString(rs.getLong(1)));
				}else {
					setslid("0");
				}
				rs.close();
			} catch (SQLException e) {
				throw new Exception("Error [1555095206] Could not get last ID number - " + e.getMessage());
			}
			//If something went wrong, we can't get the last ID:
			if (getslid().compareToIgnoreCase("0") == 0){
				throw new Exception("Error [1555095207] Could not get last ID number.");
			}
		}
		return;
	}
	public void validate_fields(Connection conn, boolean bBatchIsBeingPosted) throws Exception{
		
		String sResult = "";
		try {
			m_slid  = clsValidateFormFields.validateLongIntegerField(m_slid, "Entry ID", -1L, clsValidateFormFields.MAX_LONG_VALUE);
		} catch (Exception e) {
			sResult += "  " + e.getMessage() + ".";
		}
		try {
			m_sbatchnumber = clsValidateFormFields.validateLongIntegerField(
				m_sbatchnumber, 
				"Batch number", 
				-1L, 
				clsValidateFormFields.MAX_LONG_VALUE);
		} catch (Exception e) {
			sResult += "  " + e.getMessage() + ".";
		}
		try {
			m_sentrynumber = clsValidateFormFields.validateLongIntegerField(
					m_sentrynumber, 
				"Entry number", 
				1, 
				clsValidateFormFields.MAX_LONG_VALUE);
		} catch (Exception e) {
			sResult += "  " + e.getMessage() + ".";
		}
		try {
			m_slinenumber = clsValidateFormFields.validateLongIntegerField(
					m_slinenumber, 
				"Line number", 
				1, 
				clsValidateFormFields.MAX_LONG_VALUE);
		} catch (Exception e) {
			sResult += "  " + e.getMessage() + ".";
		}
		
		try {
			m_sdescription = clsValidateFormFields.validateStringField(
					m_sdescription, 
					SMTablegltransactionbatchlines.sdescriptionLength, 
				"Description", 
				true
			);
		} catch (Exception e) {
			sResult += "  " + e.getMessage() + ".";
		}
		
		try {
			m_scomment = clsValidateFormFields.validateStringField(
					m_scomment, 
					SMTablegltransactionbatchlines.scommentLength, 
				"Comment", 
				true
			);
		} catch (Exception e) {
			sResult += "  " + e.getMessage() + ".";
		}
		
		try {
			m_sreference = clsValidateFormFields.validateStringField(
				m_sreference, 
				SMTablegltransactionbatchlines.sreferenceLength,
				"Comment", 
				true
			);
		} catch (Exception e) {
			sResult += "  " + e.getMessage() + ".";
		}

		try {
			m_sacctid = clsValidateFormFields.validateStringField(
					m_sacctid, 
					SMTablegltransactionbatchlines.sacctidLength, 
				"GL account", 
				false
			);
		} catch (Exception e) {
			sResult += "  " + e.getMessage() + ".";
		}
		
		//Now validate the acct:
		if (m_sacctid.compareToIgnoreCase("") != 0){
			GLAccount glacct = new GLAccount(m_sacctid);
			if(!glacct.load(conn)){
				sResult += "  GL Account '" + m_sacctid + "' could not be found.";
			}
		}
		
		try {
			m_sdattransactiondate  = clsValidateFormFields.validateStandardDateField(m_sdattransactiondate, "Transaction date", false);
		} catch (Exception e) {
			sResult += "  " + e.getMessage() + ".";
		}
		
		if (m_sdebitamt.compareTo("") == 0){
			m_sdebitamt = "0.00";
		}
		m_sdebitamt = m_sdebitamt.replaceAll(",", "");
		try {
			m_sdebitamt = clsValidateFormFields.validateBigdecimalField(
					m_sdebitamt, 
				"Debit Amount", 
				SMTablegltransactionbatchlines.bddebitamtScale,
				new BigDecimal("-9999999.99"),
				new BigDecimal("9999999.99")
				).replaceAll(",", "");
		} catch (Exception e) {
			sResult += "  " + e.getMessage() + ".";
		}
		
		if (m_screditamt.compareTo("") == 0){
			m_screditamt = "0.00";
		}
		m_screditamt = m_screditamt.replaceAll(",", "");
		try {
			m_screditamt = clsValidateFormFields.validateBigdecimalField(
					m_screditamt, 
				"Credit Amount", 
				SMTablegltransactionbatchlines.bdcreditamtScale,
				new BigDecimal("-9999999.99"),
				new BigDecimal("9999999.99")
				).replaceAll(",", "");
		} catch (Exception e) {
			sResult += "  " + e.getMessage() + ".";
		}
		
		//either the debit or the credit has to be ZERO for every line:
		if (
			(m_sdebitamt.compareToIgnoreCase("0.00") != 0)
			&& (m_screditamt.compareToIgnoreCase("0.00") != 0)
		){
			sResult += "  Lines must be EITHER debits or credits, but line " + getslinenumber() + " has both.";
		}
		
		if (
				(m_sdebitamt.compareToIgnoreCase("0.00") == 0)
				&& (m_screditamt.compareToIgnoreCase("0.00") == 0)
			){
				sResult += "  must have a DEBIT amount or a CREDIT amount.";
			}
		
		try {
			m_ssourceledger = clsValidateFormFields.validateStringField(
				m_ssourceledger, 
				SMTablegltransactionbatchlines.ssourceledgerLength, 
				"Source ledger", 
				true
			).trim();
		} catch (Exception e) {
			sResult += "  " + e.getMessage() + ".";
		}
		
		try {
			m_ssourcetype = clsValidateFormFields.validateStringField(
					m_ssourcetype, 
				SMTablegltransactionbatchlines.ssourcetypeLength, 
				"Source type", 
				true
			).trim();
		} catch (Exception e) {
			sResult += "  " + e.getMessage() + ".";
		}
		
		if (sResult.compareToIgnoreCase("") != 0){
			throw new Exception(sResult);
		}
	}
	public void load(Connection conn, String sLid) throws Exception{
		String SQL = "SELECT * FROM " + SMTablegltransactionbatchlines.TableName
			+ " WHERE ("
				+ "(" + SMTablegltransactionbatchlines.lid + " = " + sLid + ")"
			+ ")"
		;
	
		try {
			ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
			if (rs.next()){
				setslid(Long.toString(rs.getLong(SMTablegltransactionbatchlines.lid)));
				setsbatchnumber(Long.toString(rs.getLong(SMTablegltransactionbatchlines.lbatchnumber)));
				setsentrynumber(Long.toString(rs.getLong(SMTablegltransactionbatchlines.lentrynumber)));
				setslinenumber(Long.toString(rs.getLong(SMTablegltransactionbatchlines.llinenumber)));
				setsdescription(rs.getString(SMTablegltransactionbatchlines.sdescription));
				setsreference(rs.getString(SMTablegltransactionbatchlines.sreference));
				setscomment(rs.getString(SMTablegltransactionbatchlines.scomment));
				setstransactiondate(clsDateAndTimeConversions.resultsetDateStringToFormattedString(
						rs.getString(SMTablegltransactionbatchlines.dattransactiondate), SMUtilities.DATE_FORMAT_FOR_DISPLAY, SMUtilities.EMPTY_DATE_VALUE));
				setsacctid(rs.getString(SMTablegltransactionbatchlines.sacctid));
				setsdebitamt(clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(rs.getBigDecimal(SMTablegltransactionbatchlines.bddebitamt)));
				setscreditamt(clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(rs.getBigDecimal(SMTablegltransactionbatchlines.bdcreditamt)));
				setssourceledger(rs.getString(SMTablegltransactionbatchlines.ssourceledger));
				setssourcetype(rs.getString(SMTablegltransactionbatchlines.ssourcetype));

			}else{
				rs.close();
				throw new Exception("Error [1555098256] - No GL transaction batch line found with lid = " + sLid + ".");
			}
			rs.close();
		} catch (Exception e) {
			throw new Exception("Error [1555098257] - " + e.getMessage());
		}
	}
	public void setAmount(String sAmount, Connection conn) throws Exception{
		//This function determines whether a transaction amount should be recorded as a debit or a credit:
		//First we have to get the 'normal' balance type:
		GLAccount glacct = new GLAccount(getsacctid());
		try {
			glacct.load(conn);
		} catch (Exception e) {
			throw new Exception("Error [1556906520] reading GL account '" + getsacctid() + " to determine normal balance type - " + glacct.getErrorMessageString());
		}
		
		if (glacct.getsinormalbalancetype().compareToIgnoreCase(Integer.toString(SMTableglaccounts.NORMAL_BALANCE_TYPE_DEBIT)) == 0){
			if (sAmount.contains("-")){
				setscreditamt(sAmount.replace("-", "").replace(",", ""));
				setsdebitamt("0.00");
			}else{
				setsdebitamt(sAmount.replace(",", ""));
				setscreditamt("0.00");
			}
		}else{
			if (sAmount.contains("-")){
				setsdebitamt(sAmount.replace("-", "").replace(",", ""));
				setscreditamt("0.00");
			}else{
				setscreditamt(sAmount.replace(",", ""));
				setsdebitamt("0.00");
			}
		}
	}
	public String getslid(){
		return m_slid;
	}
	public void setslid(String sLid){
		m_slid = sLid;
	}
	public String getsbatchnumber(){
		return m_sbatchnumber;
	}
	public void setsbatchnumber(String sBatchNumber){
		m_sbatchnumber = sBatchNumber;
	}
	public String getsentrynumber(){
		return m_sentrynumber;
	}
	public void setsentrynumber(String sEntryNumber){
		m_sentrynumber = sEntryNumber;
	}
	public String getslinenumber(){
		return m_slinenumber;
	}
	public void setslinenumber(String sLineNumber){
		m_slinenumber = sLineNumber;
	}
	public String getsdescription(){
		return m_sdescription;
	}
	public void setsdescription(String sdescription){
		m_sdescription = sdescription;
	}
	public String getsreference(){
		return m_sreference;
	}
	public void setsreference(String sreference){
		m_sreference = sreference;
	}
	public String getscomment(){
		return m_scomment;
	}
	public void setscomment(String scomment){
		m_scomment = scomment;
	}
	public String getstransactiondate(){
		return m_sdattransactiondate;
	}
	public void setstransactiondate(String stransactiondate){
		m_sdattransactiondate = stransactiondate;
	}
	public String getstransactiondateInSQLFormat(){
		if (m_sdattransactiondate.compareToIgnoreCase("") == 0){
			return SMUtilities.EMPTY_SQL_DATE_VALUE;
		}else{
			try {
				return clsDateAndTimeConversions.convertDateFormat(
					m_sdattransactiondate, SMUtilities.DATE_FORMAT_FOR_DISPLAY, SMUtilities.DATE_FORMAT_FOR_SQL, SMUtilities.EMPTY_SQL_DATE_VALUE);
			} catch (Exception e) {
				return SMUtilities.EMPTY_SQL_DATE_VALUE;
			}
		}		
	}
	public String getsacctid(){
		return m_sacctid;
	}
	public void setsacctid(String sacctid){
		m_sacctid = sacctid;
	}
	public String getsdebitamt(){
		return m_sdebitamt;
	}
	public void setsdebitamt(String sdebitamt){
		m_sdebitamt = sdebitamt;
	}
	public String getscreditamt(){
		return m_screditamt;
	}
	public void setscreditamt(String screditamt){
		m_screditamt = screditamt;
	}
	public String getssourceledger(){
		return m_ssourceledger;
	}
	public void setssourceledger(String ssourceledger){
		m_ssourceledger = ssourceledger;
	}
	public String getssourcetype(){
		return m_ssourcetype;
	}
	public void setssourcetype(String ssourcetype){
		m_ssourcetype = ssourcetype;
	}
	
	public String dumpData(){
		String s = "";
		s += "    lid:" + getslid() + "\n";
		s += "    Batch number:" + getsbatchnumber() + "\n";
		s += "    Entry number:" + getsentrynumber() + "\n";
		s += "    Line number:" + getslinenumber() + "\n";
		s += "    Description:" + getsdescription() + "\n";
		s += "    Reference:" + getsreference() + "\n";
		s += "    Comment:" + getscomment() + "\n";
		s += "    Transaction date:" + getstransactiondate() + "\n";
		s += "    Account ID:" + getsacctid() + "\n";
		s += "    Debit amt:" + getsdebitamt() + "\n";
		s += "    Credit amt:" + getscreditamt() + "\n";
		s += "    Source ledger:" + getssourceledger() + "\n";
		s += "    Source type:" + getssourcetype() + "\n";

		return s;
	}
	private void initializeVariables(){
		m_slid  = "-1";
		m_sbatchnumber = "-1";
		m_sentrynumber = "-1";
		m_slinenumber = "0";
		m_sdescription = "";
		m_sreference = "";
		m_scomment = "";
		m_sdattransactiondate = ServletUtilities.clsServletUtilities.EMPTY_DATE_VALUE;
		m_sacctid = "";
		m_sdebitamt = "0.00";
		m_screditamt = "0.00";
		m_ssourceledger = "";
		m_ssourcetype = "";
	}
}
