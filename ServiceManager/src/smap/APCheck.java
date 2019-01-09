package smap;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import javax.servlet.ServletContext;

import SMDataDefinition.SMTableapcheckforms;
import SMDataDefinition.SMTableapchecklines;
import SMDataDefinition.SMTableapchecks;
import SMDataDefinition.SMTableaptransactions;

import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsDateAndTimeConversions;
import ServletUtilities.clsManageBigDecimals;
import ServletUtilities.clsValidateFormFields;
import smbk.BKBank;
import smcontrolpanel.SMUtilities;

public class APCheck {

	public static final String OBJECT_NAME = "AP Check";
	
	private String m_slid  = "lid";
	private String m_schecknumber;
	private String m_slbankid;
	private String m_sicheckformid;
	private String m_sbdamount;
	private String m_sdatcheckdate;
	private String m_sbatchnumber;
	private String m_sentrynumber;
	private String m_sltransactionid;
	private String m_sivoid;
	private String m_siprinted;
	private String m_lcreatedbyid;
	private String m_svendoracct;
	private String m_svendorname;
	private String m_sipagenumber;
	private String m_silastpage;
	private String m_slbatchentryid;
	private String m_sdatetimecreated;
	private String m_sdatetimeprinted;
	private String m_screatedbyfullname;
	private String m_lprintedbyid;
	private String m_sprintedbyfullname;
	
	//Remit tos:
	private String m_sremittoname;
	private String m_sremittoaddressline1;
	private String m_sremittoaddressline2;
	private String m_sremittoaddressline3;
	private String m_sremittoaddressline4;
	private String m_sremittocity;
	private String m_sremittostate;
	private String m_sremittocountry;
	private String m_sremittopostalcode;
	
	
	private ArrayList<APCheckLine>m_arrCheckLines;

	public APCheck() 
	{
		initializeVariables();
	}

	public static void updateCheckPrintedStatus (Connection conn, String sStartingCheckNumber, String sEndingCheckNumber, String sStatus,  String sUserID, String sUserFullName) throws Exception{
		
		String sPrintedTime = "'" + SMUtilities.EMPTY_SQL_DATETIME_VALUE + "'";
		
		String SQL = "UPDATE"
			+ " " + SMTableapchecks.TableName
			+ " SET " + SMTableapchecks.TableName + "." + SMTableapchecks.iprinted + " = " + sStatus
			+ ", " + SMTableapchecks.TableName + "." + SMTableapchecks.dattimeprinted + " = " + sPrintedTime
			+ ", " + SMTableapchecks.TableName + "." + SMTableapchecks.lprintedbyid + " = " + sUserID + ""
			+ ", " + SMTableapchecks.TableName + "." + SMTableapchecks.sprintedbyfullname + " = '" + sUserFullName + "'"
			+ " WHERE ("
				+ "(CAST(" + SMTableapchecks.schecknumber + " AS UNSIGNED) >= '" + sStartingCheckNumber.trim() + "')"
				+ " AND (CAST(" + SMTableapchecks.schecknumber + " AS UNSIGNED) <= '" + sEndingCheckNumber.trim() + "')"
			+ ")"
		;
		try {
			Statement stmt = conn.createStatement();
			stmt.execute(SQL);
		} catch (Exception e) {
			throw new Exception("Error [1504638287] updating check with SQL: '" + SQL + "' - " + e.getMessage());
		}
		return;
	}
	
	public void save_without_data_transaction (Connection conn, String sUserID, String sUserFullName, APBatch batch) throws Exception{

		//long lStarttime = System.currentTimeMillis();
		
		try {
			validate_fields(conn, sUserID, batch);
		} catch (Exception e1) {
			throw new Exception(e1.getMessage());
		}
		
		//System.out.println("[1543341850] - elapsed time 10 = " + (System.currentTimeMillis() - lStarttime) / 1000);
		
		String SQL = "INSERT INTO " + SMTableapchecks.TableName+ "("
			+ SMTableapchecks.bdamount
			+ ", " + SMTableapchecks.datcheckdate
			+ ", " + SMTableapchecks.dattimecreated
			//+ ", " + SMTableapchecks.dattimeprinted
			+ ", " + SMTableapchecks.ilastpage
			+ ", " + SMTableapchecks.ipagenumber
			//+ ", " + SMTableapchecks.iposted
			+ ", " + SMTableapchecks.iprinted
			+ ", " + SMTableapchecks.ivoid
			+ ", " + SMTableapchecks.lbankid
			+ ", " + SMTableapchecks.lbatchentryid
			+ ", " + SMTableapchecks.lbatchnumber
			+ ", " + SMTableapchecks.lcheckformid
			+ ", " + SMTableapchecks.lentrynumber
			+ ", " + SMTableapchecks.ltransactionid
			+ ", " + SMTableapchecks.schecknumber
			+ ", " + SMTableapchecks.screatedbyfullname
			+ ", " + SMTableapchecks.lcreatedbyid
			+ ", " + SMTableapchecks.svendoracct
			+ ", " + SMTableapchecks.svendorname
			+ ", " + SMTableapchecks.sremittoname
			+ ", " + SMTableapchecks.sremittoaddressline1
			+ ", " + SMTableapchecks.sremittoaddressline2
			+ ", " + SMTableapchecks.sremittoaddressline3
			+ ", " + SMTableapchecks.sremittoaddressline4
			+ ", " + SMTableapchecks.sremittocity
			+ ", " + SMTableapchecks.sremittopostalcode
			+ ", " + SMTableapchecks.sremittocountry
			+ ", " + SMTableapchecks.sremittostate
			
			+ ") VALUES ("
			+ getscheckamount().replace(",", "") //Amt
			+ ", '" + getscheckdateInSQLFormat() + "'" //Check date
			+ ", NOW()"
			//+ ", '" + getstimeprintedInSQLFormat() + "'" //Date/time printed
			+ ", " + getsilastpage() //Last page?
			+ ", " + getsipagenumber() //Page number
			//+ ", 0" // Posted 
			+ ", " + getsiprinted() //Printed
			+ ", " + getsivoid() //Void
			+ ", " + getslbankid() //Bank ID
			+ ", " + getslbatchentryid() //Batch entry ID
			+ ", " + getsbatchnumber() //Batch number
			+ ", " + getsicheckformid() //Check form ID
			+ ", " + getsentrynumber() //Entry number
			+ ", " + getsltransactionid() //Transaction ID (not set yet)
			+ ", '" + clsDatabaseFunctions.FormatSQLStatement(getschecknumber()) + "'"
			+ ", '" + clsDatabaseFunctions.FormatSQLStatement(getscreatedbyfullname()) + "'"
			+ ", " + clsDatabaseFunctions.FormatSQLStatement(sUserID) + ""
			+ ", '" + clsDatabaseFunctions.FormatSQLStatement(getsvendoracct()) + "'"
			+ ", '" + clsDatabaseFunctions.FormatSQLStatement(getsvendorname()) + "'"
			+ ", '" + clsDatabaseFunctions.FormatSQLStatement(getsremittoname()) + "'"
			+ ", '" + clsDatabaseFunctions.FormatSQLStatement(getsremittoaddressline1()) + "'"
			+ ", '" + clsDatabaseFunctions.FormatSQLStatement(getsremittoaddressline2()) + "'"
			+ ", '" + clsDatabaseFunctions.FormatSQLStatement(getsremittoaddressline3()) + "'"
			+ ", '" + clsDatabaseFunctions.FormatSQLStatement(getsremittoaddressline4()) + "'"
			+ ", '" + clsDatabaseFunctions.FormatSQLStatement(getsremittocity()) + "'"
			+ ", '" + clsDatabaseFunctions.FormatSQLStatement(getsremittopostalcode()) + "'"
			+ ", '" + clsDatabaseFunctions.FormatSQLStatement(getsremittocountry()) + "'"
			+ ", '" + clsDatabaseFunctions.FormatSQLStatement(getsremittostate()) + "'"
			+ ")"
		;
		
		//System.out.println("[1494260459] - SQL = '" + SQL + "'");
		
		Statement stmt = conn.createStatement();
		try {
			stmt.execute(SQL);
		} catch (Exception e) {
			throw new Exception("Error [1488916192] updating AP check record " + getschecknumber() + " with SQL: '" + SQL + "' - " + e.getMessage());
		}

		//System.out.println("[1543341851] - elapsed time 11 = " + (System.currentTimeMillis() - lStarttime) / 1000);
		
		//If the entry was newly created, get the new ID:

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
				throw new Exception("Error [1488916562] Could not get last ID number - " + e.getMessage());
			}
			//If something went wrong, we can't get the last ID:
			if (getslid().compareToIgnoreCase("0") == 0){
				throw new Exception("Error [1488916563] Could not get last ID number.");
			}
		}

		//System.out.println("[1543341852] - elapsed time 12 = " + (System.currentTimeMillis() - lStarttime) / 1000);
		
		//Finally, save the lines....
		try {
			saveLines(conn, sUserID);
		} catch (Exception e) {
			throw new Exception("Error [1488988622] saving entry lines - " + e.getMessage() + ".");
		}
		
		//System.out.println("[1543341853] - elapsed time 13 = " + (System.currentTimeMillis() - lStarttime) / 1000);
		
		return;
	}
	public void validate_fields(Connection conn, String sUserID, APBatch batch) throws Exception{
		
		//long lStarttime = System.currentTimeMillis();
		
		String sResult = "";
		//First, validate the fields that are common to all types of entries:
		try {
			m_slid  = clsValidateFormFields.validateLongIntegerField(m_slid, "Check ID", -1, clsValidateFormFields.MAX_LONG_VALUE);
		} catch (Exception e) {
			sResult += "  " + e.getMessage() + ".";
		}
		try {
			m_sbatchnumber = clsValidateFormFields.validateLongIntegerField(
				m_sbatchnumber, 
				"Batch number", 
				1, 
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
		
		//Check number:
		try {
			m_schecknumber = clsValidateFormFields.validateStringField(
					m_schecknumber, 
				SMTableapchecks.schecknumberLength, 
				"Check number", 
				false
			);
		} catch (Exception e) {
			sResult += "  " + e.getMessage() + ".";
		}

		//System.out.println("[1543341870] - elapsed time 70 = " + (System.currentTimeMillis() - lStarttime) / 1000);
		
		//Bank ID:
		BKBank bank = new BKBank();
		bank.setslid(getslbankid());
		
		try {
			bank.load(conn);
		} catch (Exception e2) {
			sResult += "  Bank ID '" + getslbankid() + "' is invalid - " + e2.getMessage() + ".";
		}

		//System.out.println("[1543341871] - elapsed time 71 = " + (System.currentTimeMillis() - lStarttime) / 1000);
		
		//Check form ID:
		String SQL = "SELECT"
			+ " " + SMTableapcheckforms.lid
			+ " FROM " + SMTableapcheckforms.TableName
			+ " WHERE ("
				+ "(" + SMTableapcheckforms.lid + " = " + getsicheckformid() + ")"
			+ ")"
		;
		ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
		if (!rs.next()){
			sResult += "  Can't load check form with ID '" + getsicheckformid() + "'.";
		}
		rs.close();

		//System.out.println("[1543341872] - elapsed time 72 = " + (System.currentTimeMillis() - lStarttime) / 1000);
		
		m_sbdamount = m_sbdamount.replaceAll(",", "");
		try {
			m_sbdamount = clsValidateFormFields.validateBigdecimalField(
					m_sbdamount, 
				"Check amount", 
				SMTableapchecks.bdamountScale,
				new BigDecimal("0.00"),
				new BigDecimal("999999999.99")
				).replaceAll(",", "");
		} catch (Exception e) {
			sResult += "  " + e.getMessage() + ".";
		}
		
		try {
			m_sdatcheckdate  = clsValidateFormFields.validateStandardDateField(m_sdatcheckdate, "Check date", false);
		} catch (Exception e) {
			sResult += "  " + e.getMessage() + ".";
		}
		
		//System.out.println("[1543341873] - elapsed time 73 = " + (System.currentTimeMillis() - lStarttime) / 1000);
		
		
		//System.out.println("[1543341874] - elapsed time 74 = " + (System.currentTimeMillis() - lStarttime) / 1000);
		
		boolean bEntryNotFound = true;
		for (int i = 0; i < batch.getBatchEntryArray().size(); i++){
			if (batch.getBatchEntryArray().get(i).getsentrynumber().compareToIgnoreCase(getsentrynumber()) == 0){
				bEntryNotFound = false;
				break;
			}
		}
		if (bEntryNotFound){
			sResult += "  Entry number " + getsentrynumber() + " is not found in batch number " + getsbatchnumber() + ".";
		}
		
		//Look for the batch entry ID:
		boolean bEntryIDNotFound = true;
		for (int i = 0; i < batch.getBatchEntryArray().size(); i++){
			if (batch.getBatchEntryArray().get(i).getslid().compareToIgnoreCase(m_slbatchentryid) == 0){
				bEntryIDNotFound = false;
				break;
			}
		}
		if (bEntryIDNotFound){
			sResult += "  Entry ID " + m_slbatchentryid + " is not found in batch number " + getsbatchnumber() + ".";
		}

		//System.out.println("[1543341875] - elapsed time 75 = " + (System.currentTimeMillis() - lStarttime) / 1000);
		
		//This is not used....
		//if (getsltransactionid().compareToIgnoreCase("0") != 0){
		//	SQL = "SELECT "
		//		+ " " + SMTableaptransactions.lid
		//		+ " FROM " + SMTableaptransactions.TableName
		//		+ " WHERE ("
		//			+ "(" + SMTableaptransactions.lid + " = " + getsltransactionid() + ")"
		//		+ ")"
		//	;
		//}

		try {
			m_sivoid  = clsValidateFormFields.validateIntegerField(m_sivoid, "Void", 0, 1);
		} catch (Exception e) {
			sResult += "  " + e.getMessage() + ".";
		}

		try {
			m_siprinted  = clsValidateFormFields.validateIntegerField(m_siprinted, "Printed", 0, 1);
		} catch (Exception e) {
			sResult += "  " + e.getMessage() + ".";
		}

		//Created by:
		try {
			m_lcreatedbyid = clsValidateFormFields.validateStringField(
				m_lcreatedbyid, 
				SMTableapchecks.lcreatedbyidLength, 
				"Created by", 
				false
			);
		} catch (Exception e) {
			sResult += "  " + e.getMessage() + ".";
		}

		//Vendor acct:
		try {
			m_svendoracct = clsValidateFormFields.validateStringField(
					m_svendoracct, 
				SMTableapchecks.svendoracctLength, 
				"Vendor acct", 
				false
			);
		} catch (Exception e) {
			sResult += "  " + e.getMessage() + ".";
		}
		
		//Vendor name:
		try {
			m_svendorname = clsValidateFormFields.validateStringField(
					m_svendorname, 
				SMTableapchecks.svendornameLength, 
				"Vendor name", 
				false
			);
		} catch (Exception e) {
			sResult += "  " + e.getMessage() + ".";
		}

		
		try {
			m_sipagenumber  = clsValidateFormFields.validateIntegerField(m_sipagenumber, "Page number", 1, 999);
		} catch (Exception e) {
			sResult += "  " + e.getMessage() + ".";
		}
		
		try {
			m_silastpage  = clsValidateFormFields.validateIntegerField(m_silastpage, "Last page", 0, 1);
		} catch (Exception e) {
			sResult += "  " + e.getMessage() + ".";
		}
		
		//Remit to name:
		m_sremittoname = m_sremittoname.trim();
		try {
			m_sremittoname = clsValidateFormFields.validateStringField(
				m_sremittoname, 
				SMTableapchecks.sremittonameLength, 
				"Remit-to name", 
				false
			);
		} catch (Exception e) {
			sResult += "  " + e.getMessage() + ".";
		}
		
		//Validate the lines:
		for (int i = 0; i < m_arrCheckLines.size(); i++){
			APCheckLine line = m_arrCheckLines.get(i);
			try {
				line.validate_fields(conn);
			} catch (Exception e) {
				sResult += "  In line " + line.getschecklinenumber() + " - " + e.getMessage() + ".";
			}
		}
		
		if (sResult.compareToIgnoreCase("") != 0){
			throw new Exception(sResult);
		}
		
		//System.out.println("[1543341876] - elapsed time 76 = " + (System.currentTimeMillis() - lStarttime) / 1000);
	}

	public void load(ServletContext context, String sDBID, String sUserID, String sLid) throws Exception{
		
		Connection conn;
		try {
			conn = clsDatabaseFunctions.getConnectionWithException(
				context, 
				sDBID, 
				"MySQL", 
				SMUtilities.getFullClassName(this.toString()) + ".load - user: " + sUserID);
		} catch (Exception e) {
			throw new Exception("Error [1489509149] getting connection - " + e.getMessage());
		}
		
		try {
			load(conn, sUserID, sLid);
		} catch (Exception e) {
			throw new Exception("Error [1489509150] loading - " + e.getMessage());
		}
		clsDatabaseFunctions.freeConnection(context, conn, "[1546998961]");
		
	}
	public void load(Connection conn, String sUserID, String sLid) throws Exception{
		String SQL = "SELECT * FROM " + SMTableapchecks.TableName
			+ " WHERE ("
				+ "(" + SMTableapchecks.lid + " = " + sLid + ")"
			+ ")"
		;
	
		ResultSet rs = null;
		try {
			rs = clsDatabaseFunctions.openResultSet(SQL, conn);
			if (rs.next()){
				setscheckamount(clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(rs.getBigDecimal(SMTableapchecks.bdamount)));
				setschecknumber(rs.getString(SMTableapchecks.schecknumber));
				setsdatcheckdate(clsDateAndTimeConversions.resultsetDateStringToFormattedString(
					rs.getString(SMTableapchecks.datcheckdate), SMUtilities.DATE_FORMAT_FOR_DISPLAY, SMUtilities.EMPTY_DATE_VALUE));
				setsilastpage(Integer.toString(rs.getInt(SMTableapchecks.ilastpage)));
				setsipagenumber(Integer.toString(rs.getInt(SMTableapchecks.ipagenumber)));
				setsiprinted(Integer.toString(rs.getInt(SMTableapchecks.iprinted)));
				setsivoid(Integer.toString(rs.getInt(SMTableapchecks.ivoid)));
				setslbankid(Integer.toString(rs.getInt(SMTableapchecks.lbankid)));
				setslbatchentryid(Long.toString(rs.getLong(SMTableapchecks.lbatchentryid)));
				setsbatchnumber(Integer.toString(rs.getInt(SMTableapchecks.lbatchnumber)));
				setsicheckformid(Integer.toString(rs.getInt(SMTableapchecks.lcheckformid)));
				setsentrynumber(Integer.toString(rs.getInt(SMTableapchecks.lentrynumber)));
				setslid(Integer.toString(rs.getInt(SMTableapchecks.lid)));
				setsltransactionid(Integer.toString(rs.getInt(SMTableapchecks.ltransactionid)));
				setscreatedbyid(Integer.toString(rs.getInt(SMTableapchecks.lcreatedbyid)));
				setsvendoracct(rs.getString(SMTableapchecks.svendoracct));
				setsvendorname(rs.getString(SMTableapchecks.svendorname));
				setsremittoname(rs.getString(SMTableapchecks.sremittoname));
				setsremittoaddressline1(rs.getString(SMTableapchecks.sremittoaddressline1));
				setsremittoaddressline2(rs.getString(SMTableapchecks.sremittoaddressline2));
				setsremittoaddressline3(rs.getString(SMTableapchecks.sremittoaddressline3));
				setsremittoaddressline4(rs.getString(SMTableapchecks.sremittoaddressline4));
				setsremittocity(rs.getString(SMTableapchecks.sremittocity));
				setsremittostate(rs.getString(SMTableapchecks.sremittostate));
				setsremittocountry(rs.getString(SMTableapchecks.sremittocountry));
				setsremittopostalcode(rs.getString(SMTableapchecks.sremittopostalcode));
			}else{
				rs.close();
				throw new Exception("Error [1504798021] - No AP check found with lid = " + sLid + ".");
			}
			rs.close();
		} catch (Exception e) {
			rs.close();
			throw new Exception("Error [1504798022] - loading " + OBJECT_NAME + " with ID " + sLid + " - " + e.getMessage());
		}
		
		//Load the lines:
		m_arrCheckLines.clear();
		SQL = "SELECT"
			+ " " + SMTableapchecklines.lid
			+ " FROM " + SMTableapchecklines.TableName 
			+ " WHERE ("
				+ "(" + SMTableapchecklines.lcheckid + " = " + getslid() + ")"
			+ ") ORDER BY " + SMTableapchecklines.lchecklinenumber
		;
		rs = null;
		try {
			rs = clsDatabaseFunctions.openResultSet(SQL, conn);
			while (rs.next()){
				APCheckLine line = new APCheckLine();
				line.load(conn, sUserID, Long.toString(rs.getLong(SMTableapchecklines.lid)));
				addLine(line);
			}
			rs.close();
		} catch (Exception e) {
			rs.close();
			throw new Exception("Error [1504800662] loading check lines - " + e.getMessage());
		}
	}
	public void loadUsingCheckNumber(Connection conn, String sUserID, String sVendor, String sCheckNumber) throws Exception{
		String SQL = "SELECT * FROM " + SMTableapchecks.TableName
			+ " WHERE ("
				+ "(" + SMTableapchecks.svendoracct + " = '" + sVendor + "')"
				+ " AND (" + SMTableapchecks.schecknumber + " = '" + sCheckNumber + "')"
			+ ")"
		;
	
		ResultSet rs = null;
		try {
			rs = clsDatabaseFunctions.openResultSet(SQL, conn);
			if (rs.next()){
				setscheckamount(clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(rs.getBigDecimal(SMTableapchecks.bdamount)));
				setschecknumber(rs.getString(SMTableapchecks.schecknumber));
				setsdatcheckdate(clsDateAndTimeConversions.resultsetDateStringToFormattedString(
					rs.getString(SMTableapchecks.datcheckdate), SMUtilities.DATE_FORMAT_FOR_DISPLAY, SMUtilities.EMPTY_DATE_VALUE));
				setsilastpage(Integer.toString(rs.getInt(SMTableapchecks.ilastpage)));
				setsipagenumber(Integer.toString(rs.getInt(SMTableapchecks.ipagenumber)));
				setsiprinted(Integer.toString(rs.getInt(SMTableapchecks.iprinted)));
				setsivoid(Integer.toString(rs.getInt(SMTableapchecks.ivoid)));
				setslbankid(Integer.toString(rs.getInt(SMTableapchecks.lbankid)));
				setslbatchentryid(Long.toString(rs.getLong(SMTableapchecks.lbatchentryid)));
				setsbatchnumber(Integer.toString(rs.getInt(SMTableapchecks.lbatchnumber)));
				setsicheckformid(Integer.toString(rs.getInt(SMTableapchecks.lcheckformid)));
				setsentrynumber(Integer.toString(rs.getInt(SMTableapchecks.lentrynumber)));
				setslid(Integer.toString(rs.getInt(SMTableapchecks.lid)));
				setsltransactionid(Integer.toString(rs.getInt(SMTableapchecks.ltransactionid)));
				setscreatedbyid(rs.getString(SMTableapchecks.lcreatedbyid));
				setsvendoracct(rs.getString(SMTableapchecks.svendoracct));
				setsvendorname(rs.getString(SMTableapchecks.svendorname));
				setsremittoname(rs.getString(SMTableapchecks.sremittoname));
				setsremittoaddressline1(rs.getString(SMTableapchecks.sremittoaddressline1));
				setsremittoaddressline2(rs.getString(SMTableapchecks.sremittoaddressline2));
				setsremittoaddressline3(rs.getString(SMTableapchecks.sremittoaddressline3));
				setsremittoaddressline4(rs.getString(SMTableapchecks.sremittoaddressline4));
				setsremittocity(rs.getString(SMTableapchecks.sremittocity));
				setsremittostate(rs.getString(SMTableapchecks.sremittostate));
				setsremittocountry(rs.getString(SMTableapchecks.sremittocountry));
				setsremittopostalcode(rs.getString(SMTableapchecks.sremittopostalcode));
			}else{
				rs.close();
				throw new Exception("Error [1513617807] - No AP check found for vendor '" + sVendor 
					+ "', check number '" + sCheckNumber + "'.");
			}
			rs.close();
		} catch (Exception e) {
			rs.close();
			throw new Exception("Error [1513617808] - loading " + OBJECT_NAME + " for vendor '" + sVendor + "', check number: '" 
				+ sCheckNumber + "' - " + e.getMessage());
		}
		
		//Load the lines:
		m_arrCheckLines.clear();
		SQL = "SELECT"
			+ " " + SMTableapchecklines.lid
			+ " FROM " + SMTableapchecklines.TableName 
			+ " WHERE ("
				+ "(" + SMTableapchecklines.lcheckid + " = " + getslid() + ")"
			+ ") ORDER BY " + SMTableapchecklines.lchecklinenumber
		;
		rs = null;
		try {
			rs = clsDatabaseFunctions.openResultSet(SQL, conn);
			while (rs.next()){
				APCheckLine line = new APCheckLine();
				line.load(conn, sUserID, Long.toString(rs.getLong(SMTableapchecklines.lid)));
				addLine(line);
			}
			rs.close();
		} catch (Exception e) {
			rs.close();
			throw new Exception("Error [1513617809] loading check lines - " + e.getMessage());
		}
	}
	public void populateSampleCheckFromPaymentEntry(
		Connection conn, 
		APBatchEntry batchentry, 
		String sCheckFormID, 
		String sUserID, 
		int iPageNumber, 
		int iMaxNumberOfLinesToPrintOnCheck) throws Exception{
		
		BKBank bank = new BKBank();
		bank.setslid(batchentry.getslbankid());
		try {
			bank.load(conn);
		} catch (Exception e) {
			throw new Exception("Error [1504810637] loading bank information - " + e.getMessage());
			
		}
		
		setschecknumber("0000000000");
		setsbatchnumber(batchentry.getsbatchnumber());
		setslbatchentryid(batchentry.getslid());
		setscheckamount("0.00");  //Sample check should have a 'zero' amount
		setscreatedbyid(sUserID);
		setsdatcheckdate(batchentry.getsdatdocdate());
		setsentrynumber(batchentry.getsentrynumber());
		setsicheckformid(sCheckFormID);
		
		//Is this the last page?
		String sIsLastPage = "1";
		if ( (iPageNumber * iMaxNumberOfLinesToPrintOnCheck) < batchentry.getLineArray().size() ){
			sIsLastPage = "0";
		}
		
		setsilastpage(sIsLastPage);
		setsipagenumber(Integer.toString(iPageNumber));
		setsiprinted("0");
		setsivoid("0");
		setslbankid(batchentry.getslbankid());
		setsltransactionid("0");
		setsvendoracct(batchentry.getsvendoracct());
		setsvendorname(batchentry.getsvendorname());
		
		setsremittoname(batchentry.getsremittoname());
		setsremittoaddressline1(batchentry.getsremittoaddressline1());
		setsremittoaddressline2(batchentry.getsremittoaddressline2());
		setsremittoaddressline3(batchentry.getsremittoaddressline3());
		setsremittoaddressline4(batchentry.getsremittoaddressline4());
		setsremittocity(batchentry.getsremittocity());
		setsremittostate(batchentry.getsremittostate());
		setsremittocountry(batchentry.getsremittocountry());
		setsremittopostalcode(batchentry.getsremittopostalcode());
		
		//Create a sample check line here:
		APCheckLine checkline = new APCheckLine();
		APBatchEntryLine batchline = batchentry.getLineArray().get(0); //Just use the first line in the batch for this sample check
		checkline.setsapplytodocnumber(batchline.getsapplytodocnumber());
		checkline.setsbatchnumber(batchline.getsbatchnumber());
		checkline.setschecklinenumber("1");
		//Get the apply-to-doc date:
		checkline.setsdatapplytodocdate(getApplyToDocDate(conn, batchline.getslapplytodocid()));
		checkline.setsdiscounttaken(
			clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(
				new BigDecimal(batchline.getsbddiscountappliedamt().replace(",", "")).negate()
				)
		);
		checkline.setsentrylinenumber(batchline.getslinenumber());
		checkline.setsentrynumber(batchline.getsentrynumber());
		
		checkline.setsgrossamount(
			clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(
				new BigDecimal(getGrossAmount(batchline).replace(",", "")).negate()
			)
		);
		checkline.setsnetpaid(
				clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(
					new BigDecimal(batchline.getsbdamount().replace(",", "")).negate()
					)
			);
		checkline.setslcheckid(getslid());  //This will be 0 until the check is first saved, and we'll update it at that time
		addLine(checkline);
	}
	private String getApplyToDocDate(Connection conn, String sTransactionID) throws Exception{
		
		String sApplyToDocDate = SMUtilities.EMPTY_DATE_VALUE;
		
		String SQL = "SELECT * FROM " + SMTableaptransactions.TableName
				+ " WHERE ("
					+ "(" + SMTableaptransactions.lid + " = " + sTransactionID + ")"
				+ ")"
			;
			try {
				ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
				if (rs.next()){
					sApplyToDocDate = clsDateAndTimeConversions.resultsetDateStringToFormattedString(rs.getString(SMTableaptransactions.datdocdate), SMUtilities.DATE_FORMAT_FOR_DISPLAY, SMUtilities.EMPTY_DATE_VALUE);
				}else{
					rs.close();
					throw new Exception("Error [1503504199] - could not open AP transaction with lid '" + sTransactionID + "'.");
				}
				rs.close();
			} catch (Exception e) {
				throw new Exception("Error [1503504200] - could not read AP transactions with SQL '" + SQL + "'.");
			}	
		
		return sApplyToDocDate;
		
	}
	private String getGrossAmount(APBatchEntryLine line) throws Exception{
		String sGrossAmount = "0.00";
		try {
			BigDecimal bdNetAmount = new BigDecimal(line.getsbdamount().trim().replace(",", ""));
			BigDecimal bdDiscountAmount = new BigDecimal(line.getsbddiscountappliedamt().trim().replace(",", ""));
			sGrossAmount = clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(bdDiscountAmount.add(bdNetAmount));
		} catch (Exception e) {
			throw new Exception("Error [1504887281] calculating gross amount for check line - " + e.getMessage() + ".");
		}
		
		return sGrossAmount;
	}

	public String getslid(){
		return m_slid;
	}
	public void setslid(String slid){
		m_slid = slid;
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
	public String getslbankid(){
		return m_slbankid;
	}
	public void setslbankid(String sBankID){
		m_slbankid = sBankID;
	}
	public String getschecknumber(){
		return m_schecknumber;
	}
	public void setschecknumber(String schecknumber){
		m_schecknumber = schecknumber;
	}
	public String getsicheckformid(){
		return m_sicheckformid;
	}
	public void setsicheckformid(String sCheckFormID){
		m_sicheckformid = sCheckFormID;
	}
	public String getscheckamount(){
		return m_sbdamount;
	}
	public void setscheckamount(String sCheckAmount){
		m_sbdamount = sCheckAmount;
	}
	public String getsdatcheckdate(){
		return m_sdatcheckdate;
	}
	public String getscheckdateInSQLFormat() throws Exception{
		if (m_sdatcheckdate.compareToIgnoreCase("") == 0){
			return SMUtilities.EMPTY_SQL_DATE_VALUE;
		}else{
			return clsDateAndTimeConversions.convertDateFormat(m_sdatcheckdate, SMUtilities.DATE_FORMAT_FOR_DISPLAY, SMUtilities.DATE_FORMAT_FOR_SQL, SMUtilities.EMPTY_SQL_DATE_VALUE);
		}
	}
	public void setsdatcheckdate(String sdatCheckDate){
		m_sdatcheckdate = sdatCheckDate;
	}
	public String getsltransactionid(){
		return m_sltransactionid;
	}
	public void setsltransactionid(String sAPTransactionID){
		m_sltransactionid = sAPTransactionID;
	}
	public String getsivoid(){
		return m_sivoid;
	}
	public void setsivoid(String siVoid){
		m_sivoid = siVoid;
	}
	public String getsiprinted(){
		return m_siprinted;
	}
	public void setsiprinted(String siprinted){
		m_siprinted = siprinted;
	}
	public String getscreatedbyid(){
		return m_lcreatedbyid;
	}
	public void setscreatedbyid(String sCreatedBy){
		m_lcreatedbyid = sCreatedBy;
	}
	public String getsvendoracct(){
		return m_svendoracct;
	}
	public void setsvendoracct(String svendoracct){
		m_svendoracct = svendoracct;
	}
	public String getsvendorname(){
		return m_svendorname;
	}
	public void setsvendorname(String sVendorName){
		m_svendorname = sVendorName;
	}
	public String getsipagenumber(){
		return m_sipagenumber;
	}
	public void setsipagenumber(String siPageNumber){
		m_sipagenumber = siPageNumber;
	}
	public String getsilastpage(){
		return m_silastpage;
	}
	public void setsilastpage(String siLastPage){
		m_silastpage = siLastPage;
	}
	public String getslbatchentryid(){
		return m_slbatchentryid;
	}
	public void setslbatchentryid(String slBatchEntryID){
		m_slbatchentryid = slBatchEntryID;
	}
	public String getsremittoname(){
		return m_sremittoname;
	}
	public void setsremittoname(String sremittoname){
		m_sremittoname = sremittoname;
	}
	public String getsremittoaddressline1(){
		return m_sremittoaddressline1;
	}
	public void setsremittoaddressline1(String sremittoaddressline1){
		m_sremittoaddressline1 = sremittoaddressline1;
	}
	public String getsremittoaddressline2(){
		return m_sremittoaddressline2;
	}
	public void setsremittoaddressline2(String sremittoaddressline2){
		m_sremittoaddressline2 = sremittoaddressline2;
	}
	public String getsremittoaddressline3(){
		return m_sremittoaddressline3;
	}
	public void setsremittoaddressline3(String sremittoaddressline3){
		m_sremittoaddressline3 = sremittoaddressline3;
	}
	public String getsremittoaddressline4(){
		return m_sremittoaddressline4;
	}
	public void setsremittoaddressline4(String sremittoaddressline4){
		m_sremittoaddressline4 = sremittoaddressline4;
	}
	public String getsremittocity(){
		return m_sremittocity;
	}
	public void setsremittocity(String sremittocity){
		m_sremittocity = sremittocity;
	}
	public String getsremittostate(){
		return m_sremittostate;
	}
	public void setsremittostate(String sremittostate){
		m_sremittostate = sremittostate;
	}
	public String getsremittopostalcode(){
		return m_sremittopostalcode;
	}
	public void setsremittopostalcode(String sremittopostalcode){
		m_sremittopostalcode = sremittopostalcode;
	}
	public String getsremittocountry(){
		return m_sremittocountry;
	}
	public void setsremittocountry(String sremittocountry){
		m_sremittocountry = sremittocountry;
	}
	public String getsdatetimecreated(){
		return m_sdatetimecreated;
	}
	public void setsdatetimecreated(String sdatetimecreated){
		m_sdatetimecreated = sdatetimecreated;
	}
	public String getsdatetimeprinted(){
		return m_sdatetimeprinted;
	}
	public void setsdatetimeprinted(String sdatetimeprinted){
		m_sdatetimeprinted = sdatetimeprinted;
	}
	public String getscreatedbyfullname(){
		return m_screatedbyfullname;
	}
	public void setscreatedbyfullname(String screatedbyfullname){
		m_screatedbyfullname = screatedbyfullname;
	}
	public String getsprintedbyid(){
		return m_lprintedbyid;
	}
	public void setsprintedbyid(String sprintedby){
		m_lprintedbyid = sprintedby;
	}
	public String getsprintedbyfullname(){
		return m_sprintedbyfullname;
	}
	public void setsprintedbyfullname(String sprintedbyfullname){
		m_sprintedbyfullname = sprintedbyfullname;
	}
	
	public void addLine(APCheckLine line){
		m_arrCheckLines.add(line);
	}

	public ArrayList<APCheckLine> getLineArray(){
		return m_arrCheckLines;
	}
	private void saveLines(Connection conn, String sUserID) throws Exception{
		
		//long lStarttime = System.currentTimeMillis();
		
		for (int i = 0; i < m_arrCheckLines.size(); i++){
			APCheckLine line = m_arrCheckLines.get(i);
			line.setslcheckid(getslid());
			try {
				line.save_without_data_transaction(conn, sUserID);
			} catch (Exception e) {
				throw new Exception("Error [1504802986] saving check line number " + line.getschecklinenumber() 
					+ " on check number " + getschecknumber() + " - " + e.getMessage()
				);
			}
			//System.out.println("[1543341860] i = " + i + " - elapsed time 20 = " + (System.currentTimeMillis() - lStarttime) / 1000);
			
		}
		//We also have to delete any EXTRA lines that might be left that are higher than our current highest line number:
		//This can happen if we removed a line and we now have fewer lines than we previously had:
		String SQL = "DELETE FROM " + SMTableapchecklines.TableName
			+ " WHERE ("
				+ "(" + SMTableapchecklines.lcheckid + " = " + getslid() + ")"
				+ " AND (" + SMTableapchecklines.lchecklinenumber + " > " + Integer.toString(m_arrCheckLines.size()) + ")"
			+ ")"
		;
		try {
			Statement stmt = conn.createStatement();
			stmt.execute(SQL);
		} catch (Exception e) {
			throw new Exception("Error [1504803120] deleting leftover AP check lines - " + e.getMessage());
		}
		//System.out.println("[1543341861] elapsed time 21 = " + (System.currentTimeMillis() - lStarttime) / 1000);
		
	}
	public void removeLineByLineNumber(String sCheckLineNumber) throws Exception{
    	//System.out.println("[1489528400] - sEntryNumber = " + sEntryNumber);
		
		boolean bLineNumberWasFound = false;
		for (int i = 0; i < m_arrCheckLines.size(); i++){
			if (m_arrCheckLines.get(i).getschecklinenumber().compareToIgnoreCase(sCheckLineNumber) == 0){
				m_arrCheckLines.remove(i);
				bLineNumberWasFound = true;
			}
		}
    	
    	if (!bLineNumberWasFound){
    		throw new Exception("Check line number '" + sCheckLineNumber + "' was not found in the lines.");
    	}
	}

	private void initializeVariables(){
		m_slid  = "-1";
		m_schecknumber = "";
		m_slbankid = "0";
		m_sbatchnumber = "-1";
		m_sentrynumber = "-1";
		m_sicheckformid = "-1";
		m_sbdamount = "0.00";
		m_sdatcheckdate = SMUtilities.EMPTY_DATE_VALUE;
		m_sltransactionid = "-1";
		m_sivoid = "0";
		m_siprinted = "0";
		m_lcreatedbyid = "0";
		m_svendoracct = "";
		m_svendorname = "";
		m_sipagenumber = "0";
		m_silastpage = "0";
		m_slbatchentryid = "0";
		m_sremittoname = "";
		m_sremittoaddressline1 = "";
		m_sremittoaddressline2 = "";
		m_sremittoaddressline3 = "";
		m_sremittoaddressline4 = "";
		m_sremittocity = "";
		m_sremittostate = "";
		m_sremittocountry = "";
		m_sremittopostalcode = "";
		m_sdatetimecreated = "";
		m_sdatetimeprinted = SMUtilities.EMPTY_DATETIME_VALUE;
		m_screatedbyfullname = "";
		m_lprintedbyid = "0";
		m_sprintedbyfullname = "";
		m_arrCheckLines = new ArrayList<APCheckLine>(0);
	}
}
