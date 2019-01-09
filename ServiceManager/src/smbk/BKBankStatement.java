package smbk;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Enumeration;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import SMClasses.SMLogEntry;
import SMDataDefinition.SMTablebkaccountentries;
import SMDataDefinition.SMTablebkpostedentries;
import SMDataDefinition.SMTablebkstatements;
import ServletUtilities.clsMasterEntry;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsDateAndTimeConversions;
import ServletUtilities.clsManageBigDecimals;
import ServletUtilities.clsManageRequestParameters;
import ServletUtilities.clsStringFunctions;
import smcontrolpanel.SMUtilities;

public class BKBankStatement extends clsMasterEntry{//java.lang.Object{

	public static final String UNSAVED_STATEMENT_LID = "-1";
	public static final String ObjectName = "Statement";
	public static final String Paramlid = "lid";
	public static final String Paramlbankid = "lbankid";
	public static final String Paramdatstatementdate = "datstatementdate";
	public static final String Parambdstartingbalance = "bdstartingbalance";
	public static final String Parambdstatementbalance = "bdstatementbalance";
	public static final String Paramiposted = "iposted";
	private String m_lid;
	private String m_lbankid;
	private String m_datstatementdate;
	private String m_bdstartingbalance;
	private String m_bdstatementbalance;
	private String m_iposted;
	
	private ArrayList<Long> arrClearedEntries;
	private ArrayList<BigDecimal> arrClearedEntryAmts;
	private ArrayList<Long> arrUnClearedEntries;
	private ArrayList<BigDecimal> arrUnClearedEntryAmts;
	
	public BKBankStatement(
        ) {
		initStatementVariables();
        }
	public void loadFromHTTPRequest(HttpServletRequest req) throws Exception{
		initStatementVariables();
		set_lid(clsManageRequestParameters.get_Request_Parameter(Paramlid, req).trim());
		if (get_lid().compareToIgnoreCase("") == 0){
			set_lid(UNSAVED_STATEMENT_LID);
		}
		set_lbankid(clsManageRequestParameters.get_Request_Parameter(Paramlbankid, req).trim());
		if (get_lbankid().compareToIgnoreCase("") == 0){
			set_lbankid("-1");
		}
		set_datstatementdate(clsManageRequestParameters.get_Request_Parameter(Paramdatstatementdate, req).trim());
		if (get_datstatementdate().compareToIgnoreCase("") == 0){
			set_datstatementdate(EMPTY_DATE_STRING);
		}
		set_bdstartingbalance(clsManageRequestParameters.get_Request_Parameter(Parambdstartingbalance, req).trim());
		set_bdstatementbalance(clsManageRequestParameters.get_Request_Parameter(Parambdstatementbalance, req).trim());
		set_iposted(clsManageRequestParameters.get_Request_Parameter(Paramiposted, req).trim());
		if (get_iposted().compareToIgnoreCase("") == 0){
			set_iposted("0");
		}

		//Load the entries from the request:
		//Get the entry lid's of the checkboxes:
		Enumeration <String> e = req.getParameterNames();
		String sParam = "";
		arrClearedEntries.clear();
		arrClearedEntryAmts.clear();
		arrUnClearedEntries.clear();
		arrUnClearedEntryAmts.clear();
		//SMUtilities.printRequestParametersString(req);
		while (e.hasMoreElements()){
			sParam = (String) e.nextElement();
			//System.out.println("[1411672066] sParam = '" + sParam + "', value = '" + SMUtilities.get_Request_Parameter(sParam, req) + "'.");
			long lid = 0L;
			if (sParam.contains(BKEditStatementEdit.ENTRY_ID_MARKER)){
				//System.out.println("[1411672067] sParam with ENTRY_ID_MARKER = '" + sParam + "', value = '" + SMUtilities.get_Request_Parameter(sParam, req) + "'.");
				//Get the entry lid:
				try {
					lid = Long.parseLong(sParam.substring(BKEditStatementEdit.ENTRY_ID_MARKER.length(), sParam.length()));
				} catch (Exception e1) {
					throw new Exception("Error [1403191360] invalid lid value '" + Long.toString(lid) + "' for bank entry.");
				}
			
				//Get the entry amount:
				//Now get the corresponding amount for the entry:
				String sEntryAmount = req.getParameter(BKEditStatementEdit.DEPOSIT_ENTRY_AMOUNT_MARKER + clsStringFunctions.PadLeft(Long.toString(lid), "0",
					BKEditStatementEdit.OVERALL_NUMBER_LENGTH_WITH_PADDING));
				if (sEntryAmount == null){
					sEntryAmount = req.getParameter(BKEditStatementEdit.WITHDRAWAL_ENTRY_AMOUNT_MARKER + clsStringFunctions.PadLeft(Long.toString(lid), "0",
						BKEditStatementEdit.OVERALL_NUMBER_LENGTH_WITH_PADDING));
				}
				
				//If the entry is checked, we'll get a non-null value here:
				if (req.getParameter(BKEditStatementEdit.CHECKBOX_MARKER 
					+ clsStringFunctions.PadLeft(Long.toString(lid), "0", BKEditStatementEdit.OVERALL_NUMBER_LENGTH_WITH_PADDING)) != null){
					arrClearedEntries.add(lid);
					try {
						arrClearedEntryAmts.add(new BigDecimal(sEntryAmount.replace(",", "")));
					} catch (Exception e1) {
						throw new Exception("Error [1403191361] error reading entry amt on cleared entry lid = '" + lid + "' - " + e1.getMessage() + ".");
					}
					//System.out.println("[1411672064] CLEARED lid = '" + lid + "', amt = '" + sEntryAmount);
				}else{
					arrUnClearedEntries.add(lid);
					try {
						arrUnClearedEntryAmts.add(new BigDecimal(sEntryAmount.replace(",", "")));
					} catch (Exception e1) {
						throw new Exception("Error [1403191461] error reading entry amt on uncleared entry lid = '" + lid + "' - " + e1.getMessage() + ".");
					}
					//System.out.println("[1411672065] UNCLEARED lid = '" + lid + "', amt = '" + sEntryAmount);
				}
			}
		}
	}
	public void save(ServletContext context, String sConf, String sUser) throws Exception{
		Connection conn = null;
		try {
			conn = clsDatabaseFunctions.getConnectionWithException(
				context, 
				sConf, 
				"MySQL", 
				SMUtilities.getFullClassName(this.toString()) + ".save - user: " + sUser);
		} catch (Exception e) {
			throw new Exception("Error [1391551096] getting connection - " + e.getMessage());
		}
		if(!clsDatabaseFunctions.start_data_transaction(conn)){
			clsDatabaseFunctions.freeConnection(context, conn, "[1547067440]");
			throw new Exception ("Error [1391551097] - could not start data transaction.");
		}
		try {
			save_without_data_transaction(conn, sUser);
		} catch (Exception e) {
			clsDatabaseFunctions.rollback_data_transaction(conn);
			clsDatabaseFunctions.freeConnection(context, conn, "[1547067441]");
			throw new Exception("Error saving " + ObjectName + " - " + e.getMessage());
		}
		if(!clsDatabaseFunctions.commit_data_transaction(conn)){
			clsDatabaseFunctions.rollback_data_transaction(conn);
			clsDatabaseFunctions.freeConnection(context, conn, "[1547067442]");
			throw new Exception("Error [1391551099] committing transaction.");
		}
		clsDatabaseFunctions.freeConnection(context, conn, "[1547067443]");
		return;
	}
	private void save_without_data_transaction (Connection conn, String sUser) throws Exception{
    	
    	try{
    		validate_fields(conn);
    	}catch (Exception ex){
    		throw new Exception("Error validating " + ObjectName + " - " + ex.getMessage());
    	}

    	String SQL = "";
    	if (get_lid().compareToIgnoreCase(UNSAVED_STATEMENT_LID) == 0){
	    	SQL = "INSERT INTO " + SMTablebkstatements.TableName
	    		+ "("
	    		+ SMTablebkstatements.bdstartingbalance
	    		+ ", " + SMTablebkstatements.bdstatementbalance
	    		+ ", " + SMTablebkstatements.datstatementdate
	    		+ ", " + SMTablebkstatements.iposted
	    		+ ", " + SMTablebkstatements.lbankid
	    		+ ") VALUES ("
	    		+ get_bdstartingbalance().replace(",", "")
	    		+ ", " + get_bdstatementbalance().replace(",", "")
	    		+ ", '" + clsDateAndTimeConversions.stdDateStringToSQLDateString(get_datstatementdate()) + "'"
	    		+ ", " + get_iposted()
	    		+ ", " + get_lbankid()
	    		+ ")"
	    	;
    	}else{
    		SQL = "UPDATE " + SMTablebkstatements.TableName + " SET "
	    		+ SMTablebkstatements.bdstartingbalance + " = " + get_bdstartingbalance().replace(",", "")
	    		+ ", " + SMTablebkstatements.bdstatementbalance + " = " + get_bdstatementbalance().replace(",", "")
	    		+ ", " + SMTablebkstatements.datstatementdate + " = '" + clsDateAndTimeConversions.stdDateStringToSQLDateString(get_datstatementdate()) + "'"
	    		+ ", " + SMTablebkstatements.iposted + " = " + get_iposted()
	    		+ ", " + SMTablebkstatements.lbankid + " = " + get_lbankid()
	    		+ " WHERE ("
	    			+ "(" + SMTablebkstatements.lid + " = " + get_lid() + ")"
	    		+ ")"
	    	;
    	}
    	try {
			Statement stmt = conn.createStatement();
			stmt.execute(SQL);
		} catch (Exception e1) {
			throw new Exception(this.toString() + "Could not insert/update " + ObjectName + " with SQL: " + SQL + " - " + e1.getMessage());
		}
		//Update the ID if it's an insert:
		if (get_lid().compareToIgnoreCase(UNSAVED_STATEMENT_LID) == 0){
			SQL = "SELECT last_insert_id()";
			try {
				ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
				if (rs.next()) {
					set_lid(Long.toString(rs.getLong(1)));
				}else {
					set_lid("0");
				}
				rs.close();
			} catch (SQLException e) {
				throw new Exception("Could not get last ID number - " + e.getMessage());
			}
			//If something went wrong, we can't get the last ID:
			if (get_lid().compareToIgnoreCase("0") == 0){
				//SMUtilities.rollback_data_transaction(conn);
				throw new Exception("Could not get last ID number.");
			}
		}
    	
    	try{
    		save_entries_status(conn, sUser);
    	}catch (Exception ex){
    		throw new Exception("Error [1415308474] Failed to save " + ObjectName + " entries."
    			+ ex.getMessage());
    	}
    	
		//If it's a posting:
    	if (get_iposted().compareToIgnoreCase("1") == 0){
    		//First record all the entries in a 'posted entries' table:
			try {
				savePostedEntries(conn);
			} catch (Exception e) {
				throw new Exception(e.getMessage());
			}
			//Next remove the uncleared entries from the statement:
			SQL = "UPDATE"
				+ " " + SMTablebkaccountentries.TableName
				+ " SET " + SMTablebkaccountentries.lstatementid + " = 0"
				+ " WHERE ("
					+ "(" + SMTablebkaccountentries.lstatementid + " = " + this.get_lid() + ")"
					+ " AND (" + SMTablebkaccountentries.icleared + " = 0)"
				+ ")"
			;
			try {
				Statement stmt = conn.createStatement();
				stmt.execute(SQL);
			} catch (Exception e) {
				throw new Exception("Error [1415308472] removing uncleared entries from statement - " + e.getMessage());
			}
			
			//Finally, remove any remaining statement entries since we've copied them all into the 'posted' entries:
			SQL = "DELETE"
					+ " FROM " + SMTablebkaccountentries.TableName
					+ " WHERE ("
						+ "(" + SMTablebkaccountentries.lstatementid + " = " + this.get_lid() + ")"
					+ ")"
				;
				try {
					Statement stmt = conn.createStatement();
					stmt.execute(SQL);
				} catch (Exception e) {
					throw new Exception("Error [1415308473] deleting cleared entries from statement - " + e.getMessage());
				}
    	}
    	return;
    }
	public void post_without_data_transaction (ServletContext context, String sConf, String sUser) throws Exception{
		if(get_iposted().compareToIgnoreCase("1") == 0){
			throw new Exception("This statement is already posted.");
		}
		
		Connection conn;
		try {
			conn = clsDatabaseFunctions.getConnectionWithException(
				context, 
				sConf, 
				"MySQL", 
				SMUtilities.getFullClassName(this.toString() + ".post_without_datatransaction - user: " + sUser));
		} catch (Exception e1) {
			throw new Exception("Error [1405974558] - unable to get connection - " + e1.getMessage());
		}
		
		set_iposted("1");
		if(!clsDatabaseFunctions.start_data_transaction(conn)){
			clsDatabaseFunctions.freeConnection(context, conn, "[1547060553]");
			throw new Exception ("Error [1391551297] - could not start data transaction.");
		}
		try {
			save_without_data_transaction(conn, sUser);
		} catch (Exception e) {
			set_iposted("0");
			clsDatabaseFunctions.rollback_data_transaction(conn);
			clsDatabaseFunctions.freeConnection(context, conn, "[1547060554]");
			throw new Exception(e.getMessage());
		}
		if(!clsDatabaseFunctions.commit_data_transaction(conn)){
			set_iposted("0");
			clsDatabaseFunctions.rollback_data_transaction(conn);
			clsDatabaseFunctions.freeConnection(context, conn, "[1547060555]");
			throw new Exception("Error [1391551799] committing transaction.");
		}
		clsDatabaseFunctions.freeConnection(context, conn, "[1547060556]");
    	return;
    }
	private void savePostedEntries(Connection conn) throws Exception{
		String sSQL = "";
		for (int i = 0; i < arrClearedEntries.size(); i++){
			sSQL = "INSERT INTO " + SMTablebkpostedentries.TableName + "("
				+ " " + SMTablebkpostedentries.bdamount
				+ ", " + SMTablebkpostedentries.datentrydate
				+ ", " + SMTablebkpostedentries.ibatchentrynumber
				+ ", " + SMTablebkpostedentries.ibatchnumber
				+ ", " + SMTablebkpostedentries.ibatchtype
				+ ", " + SMTablebkpostedentries.icleared
				+ ", " + SMTablebkpostedentries.ientrytype
				+ ", " + SMTablebkpostedentries.loriginalentryid
				+ ", " + SMTablebkpostedentries.lstatementid
				+ ", " + SMTablebkpostedentries.sdescription
				+ ", " + SMTablebkpostedentries.sdocnumber
				+ ", " + SMTablebkpostedentries.sglaccount
				+ ", " + SMTablebkpostedentries.ssourcemodule
				+ ")"
				+ " SELECT"
				+ " " + SMTablebkaccountentries.bdamount
				+ ", " + SMTablebkaccountentries.datentrydate
				+ ", " + SMTablebkaccountentries.ibatchentrynumber
				+ ", " + SMTablebkaccountentries.ibatchnumber
				+ ", " + SMTablebkaccountentries.ibatchtype
				+ ", 1"
				+ ", " + SMTablebkaccountentries.ientrytype
				+ ", " + SMTablebkaccountentries.lid
				+ ", " + this.get_lid()
				+ ", " + SMTablebkaccountentries.sdescription
				+ ", " + SMTablebkaccountentries.sdocnumber
				+ ", " + SMTablebkaccountentries.sglaccount
				+ ", " + SMTablebkaccountentries.ssourcemodule
				+ " FROM " + SMTablebkaccountentries.TableName
				+ " WHERE ("
					+ "(" + SMTablebkaccountentries.lid + " = " + Long.toString(arrClearedEntries.get(i)) + ")"
				+ ")"
				;
			Statement stmt = conn.createStatement();
			try {
				stmt.execute(sSQL);
			} catch (Exception e) {
				throw new Exception("Error [1407179146] inserting cleared posted entry with lid: " 
					+ Long.toString(arrClearedEntries.get(i)) + ", SQL: " + sSQL + " - " + e.getMessage());
			}
		}
		for (int i = 0; i < arrUnClearedEntries.size(); i++){
			sSQL = "INSERT INTO " + SMTablebkpostedentries.TableName + "("
				+ " " + SMTablebkpostedentries.bdamount
				+ ", " + SMTablebkpostedentries.datentrydate
				+ ", " + SMTablebkpostedentries.ibatchentrynumber
				+ ", " + SMTablebkpostedentries.ibatchnumber
				+ ", " + SMTablebkpostedentries.ibatchtype
				+ ", " + SMTablebkpostedentries.icleared
				+ ", " + SMTablebkpostedentries.ientrytype
				+ ", " + SMTablebkpostedentries.loriginalentryid
				+ ", " + SMTablebkpostedentries.lstatementid
				+ ", " + SMTablebkpostedentries.sdescription
				+ ", " + SMTablebkpostedentries.sdocnumber
				+ ", " + SMTablebkpostedentries.sglaccount
				+ ", " + SMTablebkpostedentries.ssourcemodule
				+ ")"
				+ " SELECT"
				+ " " + SMTablebkaccountentries.bdamount
				+ ", " + SMTablebkaccountentries.datentrydate
				+ ", " + SMTablebkaccountentries.ibatchentrynumber
				+ ", " + SMTablebkaccountentries.ibatchnumber
				+ ", " + SMTablebkaccountentries.ibatchtype
				+ ", 0"
				+ ", " + SMTablebkaccountentries.ientrytype
				+ ", " + SMTablebkaccountentries.lid
				+ ", " + this.get_lid()
				+ ", " + SMTablebkaccountentries.sdescription
				+ ", " + SMTablebkaccountentries.sdocnumber
				+ ", " + SMTablebkaccountentries.sglaccount
				+ ", " + SMTablebkaccountentries.ssourcemodule
				+ " FROM " + SMTablebkaccountentries.TableName
				+ " WHERE ("
					+ "(" + SMTablebkaccountentries.lid + " = " + Long.toString(arrUnClearedEntries.get(i)) + ")"
				+ ")"
				;
			Statement stmt = conn.createStatement();
			try {
				stmt.execute(sSQL);
			} catch (Exception e) {
				throw new Exception("Error [1407179147] inserting uncleared posted entry with lid: " 
					+ Long.toString(arrClearedEntries.get(i)) + ", SQL: " + sSQL + " - " + e.getMessage());
			}
		}
	}
	public void load (
		String sConf,
		String sUserID,
		String sUserFullName,
		ServletContext context
	) throws Exception{
		
		Connection conn = clsDatabaseFunctions.getConnection(
			context, 
			sConf, 
			"MySQL", 
			SMUtilities.getFullClassName(this.toString()) + ".load - user: " + sUserID + " " + sUserFullName);
		try {
			load(conn);
		} catch (Exception e) {
			clsDatabaseFunctions.freeConnection(context, conn, "[1547060551]");
			throw new Exception("Error loading - " + e.getMessage());
		}
		clsDatabaseFunctions.freeConnection(context, conn, "[1547060552]");
	}
	private void load (Connection conn) throws Exception{
			//Returns false if there is no record for this ID:
			String SQL = "SELECT * FROM " + SMTablebkstatements.TableName + " WHERE ("
					+ "(" + SMTablebkstatements.lid + " = " + get_lid() + ")"
				+ ")"
			;
			try {
				ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn); 
				if (!rs.next()){
					rs.close();
					throw new Exception("No statement found with this ID (" + get_lid() + ")");
				}
				//Load the variables:
				set_lid(Integer.toString(rs.getInt(SMTablebkstatements.lid)));
				set_bdstartingbalance(clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(rs.getBigDecimal(SMTablebkstatements.bdstartingbalance)));
				set_bdstatementbalance(clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(rs.getBigDecimal(SMTablebkstatements.bdstatementbalance)));
				set_datstatementdate(clsDateAndTimeConversions.resultsetDateStringToString(rs.getString(SMTablebkstatements.datstatementdate)));
				set_iposted(Integer.toString(rs.getInt(SMTablebkstatements.iposted)));
				set_lbankid(Long.toString(rs.getLong(SMTablebkstatements.lbankid)));
				set_lid(Long.toString(rs.getLong(SMTablebkstatements.lid)));
				rs.close();
			}catch (SQLException ex){
				throw new Exception("Error [1391439248] loading " + ObjectName + " with SQL: " + SQL + " - " + ex.getMessage());
			}
			try {
				load_entries(conn);
			} catch (Exception e) {
				throw new Exception("Error [1391439249] loading lines - " + e.getMessage());
			}
		}

	private void load_entries (Connection conn) throws Exception{
		if (get_lid().trim().equalsIgnoreCase("")){
			throw new Exception(" Error [1391439427] Invalid " + ObjectName + "ID - '" + get_lid() + "'");
		}
		String SQL = "SELECT"
			+ " " + SMTablebkaccountentries.TableName + "." + SMTablebkaccountentries.lid
			+ ", " + SMTablebkaccountentries.TableName + "." + SMTablebkaccountentries.bdamount
			+ " FROM " 
			+ SMTablebkaccountentries.TableName 
			+ " WHERE ("
				+ "("
					+ "(" + SMTablebkaccountentries.TableName + "." + SMTablebkaccountentries.lstatementid + " = " + get_lid() + ")"
				+ ")"
			+ ") ORDER BY " + SMTablebkaccountentries.TableName + "." + SMTablebkaccountentries.ssourcemodule + ", " 
				+ SMTablebkaccountentries.TableName + "." + SMTablebkaccountentries.ibatchnumber + ", " 
				+ SMTablebkaccountentries.TableName + "." + SMTablebkaccountentries.datentrydate;
		try {
			ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
			arrClearedEntries.clear();
			arrClearedEntryAmts.clear();
			while(rs.next()){
				arrClearedEntries.add(rs.getLong(SMTablebkaccountentries.TableName + "." + SMTablebkaccountentries.lid));
				arrClearedEntryAmts.add(rs.getBigDecimal(SMTablebkaccountentries.TableName + "." + SMTablebkaccountentries.bdamount));
			}
			rs.close();
		}catch (SQLException ex){
			throw new Exception("Error [1391439426] loading account entries with SQL: " + SQL + " - " + ex.getMessage());
		}
	}

	public void validate_fields (Connection conn) throws Exception{
    	
    	boolean bValid = true;
    	long lID = 0;
    	String sErrors = "";
		try {
			lID = Long.parseLong(get_lid());
		} catch (NumberFormatException e) {
			bValid = false;
			sErrors += "Invalid ID: '" + get_lid() + "'.  ";
		}
    	if (lID < Long.parseLong(UNSAVED_STATEMENT_LID)){
    		bValid = false;
    		sErrors += "Invalid ID: '" + get_lid() + "'.  ";
    	}
    	
		try {
			lID = Long.parseLong(get_lbankid());
		} catch (NumberFormatException e) {
			bValid = false;
			sErrors += "Invalid bank ID: '" + get_lbankid() + "'.  ";
		}
    	if (lID < -1){
    		bValid = false;
    		sErrors += "Invalid bank ID: '" + get_lbankid() + "'.  ";
    	}
    	//bank:
    	BKBank bank = new BKBank();
    	bank.setslid(get_lbankid());
    	if(!bank.load(conn)){
    		bValid = false;
    		sErrors += "Bank with ID: '" + get_lbankid() + "' was not found.  ";
    	}
        //if (get_datstatementdate().compareTo(EMPTY_DATE_STRING) != 0){
	        if (!clsDateAndTimeConversions.IsValidDateString("M/d/yyyy", get_datstatementdate())){
	        	bValid = false;
	        	sErrors += "Invalid statement date: '" + get_datstatementdate() + "  .";
	        }
        //}
        //Starting balance:
		set_bdstartingbalance(get_bdstartingbalance().replace(",", ""));
        if (get_bdstartingbalance().compareToIgnoreCase("") == 0){
        	set_bdstartingbalance(clsManageBigDecimals.BigDecimalToScaledFormattedString(
        		SMTablebkstatements.bdstartingbalancescale, BigDecimal.ZERO));
        }
		BigDecimal bdStartingBalance = new BigDecimal(0);
        try{
        	bdStartingBalance = new BigDecimal(get_bdstartingbalance());
        	set_bdstartingbalance(clsManageBigDecimals.BigDecimalToScaledFormattedString(
        			SMTablebkstatements.bdstartingbalancescale, bdStartingBalance));
        }catch(NumberFormatException e){
        	bValid = false;
    		sErrors += "Invalid starting balance: '" + get_bdstartingbalance() + "'.  ";
        }
        
        //Statement balance:
		set_bdstatementbalance(get_bdstatementbalance().replace(",", ""));
        if (get_bdstatementbalance().compareToIgnoreCase("") == 0){
        	set_bdstatementbalance(clsManageBigDecimals.BigDecimalToScaledFormattedString(
        		SMTablebkstatements.bdstatementbalancescale, BigDecimal.ZERO));
        }
		BigDecimal bdStatementBalance = new BigDecimal(0);
        try{
        	bdStatementBalance = new BigDecimal(get_bdstatementbalance());
        	set_bdstatementbalance(clsManageBigDecimals.BigDecimalToScaledFormattedString(
        			SMTablebkstatements.bdstatementbalancescale, bdStatementBalance));
        }catch(NumberFormatException e){
        	bValid = false;
    		sErrors += "Invalid statement balance: '" + get_bdstatementbalance() + "'.  ";
        }
        
        //Posted:
    	if (
        	(get_iposted().compareToIgnoreCase("0") != 0)
        	&& (get_iposted().compareToIgnoreCase("1") != 0)
       	){
       		bValid = false;
       		sErrors += "Posted has an invalid value: '" + get_iposted() + "'.  ";
        }
    	
    	//If it's being posted, calculate the ending balance, based on the entries:
    	if (get_iposted().compareToIgnoreCase("1") == 0){
	    	BigDecimal bdOutstandingEntryTotal = new BigDecimal("0.00");
	    	for (int i = 0; i < arrUnClearedEntryAmts.size(); i++){
	    		bdOutstandingEntryTotal = bdOutstandingEntryTotal.add(arrUnClearedEntryAmts.get(i));
	    	}
	    	BigDecimal bdBalanceDifference = bdStatementBalance.subtract(bdStartingBalance);
	    	//System.out.println("[1403639848] bdEntryTotal = " + bdEntryTotal + ", bdStatementBalance = " + bdStatementBalance + ", bdStartingBalance = " + bdStartingBalance);
	    	if (bdBalanceDifference.compareTo(bdOutstandingEntryTotal) < 0){
	    		bValid = false;
	    		sErrors += "Statement is out of balance - the total of the outstanding entries (" 
	    			+ clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(bdOutstandingEntryTotal)
	    			+ ") is " 
	    			+ clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(bdOutstandingEntryTotal.subtract(bdBalanceDifference)) + " more than the "
	    			+ " difference in the balances (adjusted balance is " 
	    			+ clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(bdStartingBalance)
	    			+ ", GL balance is "
	    			+ clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(bdStatementBalance)
	    			+ " - difference is "
	    			+ clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(bdBalanceDifference)
	    			+ ").  ";
	    	}
	    	if (bdBalanceDifference.compareTo(bdOutstandingEntryTotal) > 0){
	    		bValid = false;
	    		sErrors += "Statement is out of balance - the total of the outstanding entries (" 
	    			+ clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(bdOutstandingEntryTotal)
	    			+ ") is " 
	    			+ clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(bdOutstandingEntryTotal.subtract(bdBalanceDifference)) + " less than the "
	    			+ " difference in the balances (adjusted balance is " 
	    			+ clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(bdStartingBalance)
	    			+ ", GL balance is "
	    			+ clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(bdStatementBalance)
	    			+ " - difference is "
	    			+ clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(bdBalanceDifference)
	    			+ ").  ";
	    	}
    	}
    	if (!bValid){
    		throw new Exception(sErrors);
    	}
    	return;
    }

    public void delete(ServletContext context, String sConf, String sUser, String sUserID, String sUserFullName) throws Exception{
    	
    	Connection conn = clsDatabaseFunctions.getConnection(
    			context, 
    			sConf, 
    			"MySQL", 
    			SMUtilities.getFullClassName(this.toString()) + ".delete - user: " + sUserID + " " + sUserFullName);
    	if (conn == null){
    		throw new Exception("Error [1392135194] getting connection.");
    	}
    	if (!clsDatabaseFunctions.start_data_transaction(conn)){
    		clsDatabaseFunctions.freeConnection(context, conn, "[1547060547]");
    		throw new Exception("Error [1392135195] starting data transaction.");
    	}
    	try {
			delete_without_transaction(conn, sUserID);
		} catch (Exception e) {
			clsDatabaseFunctions.rollback_data_transaction(conn);
			clsDatabaseFunctions.freeConnection(context, conn, "[1547060548]");
			throw new Exception("Error [1392135196] deleting - " + e.getMessage());
		}
    	if (!clsDatabaseFunctions.commit_data_transaction(conn)){
    		clsDatabaseFunctions.rollback_data_transaction(conn);
    		clsDatabaseFunctions.freeConnection(context, conn, "[1547060549]");
			throw new Exception("Error [1392135196] committing data transaction.");
    	}
    	clsDatabaseFunctions.freeConnection(context, conn, "[1547060550]");
    }
    private void delete_without_transaction (Connection conn,  String sUserID) throws Exception{
    	
    	if ((get_lid().compareToIgnoreCase(UNSAVED_STATEMENT_LID) == 0)){
    		throw new Exception("Bank statement cannot be deleted because it has not been loaded - ID = " + UNSAVED_STATEMENT_LID + ".");
    	}
    	
    	//Rules for deleting:
    	//If the statement has been posted, we cannot delete it:
    	if (get_iposted().compareToIgnoreCase("1") == 0){
    		throw new Exception("This statement has been posted and cannot be deleted.");
    	}
    	try{
    		remove_all_entries_from_statement(conn);
    	}catch(Exception ex){
    		throw new Exception("Failed to delete lines."
    							+ ex.getMessage());
    	}
    	String SQL = "DELETE FROM " + SMTablebkstatements.TableName
    		+ " WHERE ("
    			+ "(" + SMTablebkstatements.lid + " = " + get_lid() + ")"
    		+ ")"
    	;
    	try {
			Statement stmt = conn.createStatement();
			stmt.execute(SQL);
		} catch (Exception e) {
			throw new Exception("Error [1392138193] deleting bank statement with SQL: " + SQL + " - " + e.getMessage());
		}
    	SMLogEntry log = new SMLogEntry(conn);
    	log.writeEntry(
    		sUserID, 
    		SMLogEntry.LOG_OPERATION_DELETEBKSTMT, 
    		"Deleted STMT " + get_lid(), 
    		"Statement date: " + get_datstatementdate()
    		+ ", bank ID: " + get_lbankid()
    		,
    		"[1392159145]");
    }
    private void save_entries_status (Connection conn, String sUser) throws Exception{

    	try{
    		remove_all_entries_from_statement(conn);
    	}catch(Exception ex){
    		throw new Exception("Failed to remove entries from statement."
    			+ ex.getMessage());
    	}
    	String SQL = "";
    	for (int i=0;i<arrClearedEntries.size();i++){
    		SQL = "UPDATE " + SMTablebkaccountentries.TableName + " SET "
    			+ SMTablebkaccountentries.lstatementid + " = " + get_lid()
    			+ ", " + SMTablebkaccountentries.icleared + " = 1"
    			+ " WHERE ("
    				+ "(" + SMTablebkaccountentries.lid + " = " + Long.toString(arrClearedEntries.get(i)) + ")"
    			+ ")"
    		;
    		try {
				Statement stmt = conn.createStatement();
				stmt.execute(SQL);
			} catch (Exception e) {
				throw new Exception("Error [1403203002] updating CLEARED statement entries with SQL: " + SQL + " - " + e.getMessage());
			}
    	}
    	for (int i=0;i<arrUnClearedEntries.size();i++){
    		SQL = "UPDATE " + SMTablebkaccountentries.TableName + " SET "
    			+ SMTablebkaccountentries.lstatementid + " = " + get_lid()
    			+ ", " + SMTablebkaccountentries.icleared + " = 0"
    			+ " WHERE ("
    				+ "(" + SMTablebkaccountentries.lid + " = " + Long.toString(arrUnClearedEntries.get(i)) + ")"
    			+ ")"
    		;
    		try {
				Statement stmt = conn.createStatement();
				stmt.execute(SQL);
			} catch (Exception e) {
				throw new Exception("Error [1403202002] updating UNCLEARED statement entries with SQL: " + SQL + " - " + e.getMessage());
			}
    	}
    }

    private void remove_all_entries_from_statement(Connection conn) throws Exception{
    	String SQL = "UPDATE " + SMTablebkaccountentries.TableName
    		+ " SET " + SMTablebkaccountentries.lstatementid + " = " + Integer.toString(SMTablebkaccountentries.INITIAL_STATEMENT_ID_VALUE)
    		+ ", " + SMTablebkaccountentries.icleared + " = 0"
    		+ " WHERE (" + SMTablebkaccountentries.lstatementid + " = " + get_lid() + ")";
    	try {
    		clsDatabaseFunctions.executeSQL(SQL, conn);
    	}catch (SQLException e){
    		throw new Exception("Error [1403203263] removing bank entries from statement with SQL: " + SQL + " - " + e.getMessage());
    	}
    }
	public String get_lid(){
		return m_lid;
	}
	public void set_lid(String sLid){
		m_lid = sLid;
	}
	public String get_lbankid(){
		return m_lbankid;
	}
	public void set_lbankid(String sLbankid){
		m_lbankid = sLbankid;
	}
	public String get_datstatementdate(){
		return m_datstatementdate;
	}
	public void set_datstatementdate(String sdatstatementdate){
		m_datstatementdate = sdatstatementdate;
	}
	public String get_bdstartingbalance(){
		return m_bdstartingbalance;
	}
	public void set_bdstartingbalance(String sbdstartingbalance){
		m_bdstartingbalance = sbdstartingbalance;
	}
	public String get_bdstatementbalance(){
		return m_bdstatementbalance;
	}
	public void set_bdstatementbalance(String sbdstatementbalance){
		m_bdstatementbalance = sbdstatementbalance;
	}
	public String get_iposted(){
		return m_iposted;
	}
	public void set_iposted(String siposted){
		m_iposted = siposted;
	}

	public int getDetailCount(){
		return arrClearedEntries.size();
	}
	public long getEntryLID(int iIndex){
		return arrClearedEntries.get(iIndex);
	}
	public boolean isEntryOnStatement(long lTestID) throws Exception{
		for (int i = 0; i < arrClearedEntries.size(); i++){
			if (arrClearedEntries.get(i) == lTestID){
				return true;
			}
		}
		return false;
	}
    public String read_out_debug_data(){
    	return "  ** BKBankStatement read out: "
    		+ "\nStarting balance: " + get_bdstartingbalance()
    		+ "\nStatement balance: " + get_bdstatementbalance()
    		+ "\nStatement date: " + get_datstatementdate()
    		+ "\nPosted: " + get_iposted()
    		+ "\nBank ID: " + get_lbankid()
    		+ "\nID: " + get_lid()
    	;
    }
    private void initStatementVariables(){
		set_lid(UNSAVED_STATEMENT_LID);
		set_bdstartingbalance("0.00");
		set_bdstatementbalance("0.00");
		set_datstatementdate(EMPTY_DATE_STRING);
		set_iposted("0");
		set_lbankid("-1");
		arrClearedEntries = new ArrayList<Long>(0);
		arrClearedEntryAmts = new ArrayList<BigDecimal>(0);
		arrUnClearedEntries = new ArrayList<Long>(0);
		arrUnClearedEntryAmts = new ArrayList<BigDecimal>(0);
		super.initVariables();
    }
}