package smgl;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Enumeration;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import SMDataDefinition.SMTablegltransactionbatchentries;
import SMDataDefinition.SMTablegltransactionbatchlines;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsDateAndTimeConversions;
import ServletUtilities.clsManageBigDecimals;
import ServletUtilities.clsManageRequestParameters;
import ServletUtilities.clsServletUtilities;
import ServletUtilities.clsValidateFormFields;
import smar.SMOption;
import smcontrolpanel.SMUtilities;

public class GLTransactionBatchEntry {

	public static final String OBJECT_NAME = "GL Transaction Batch Entry";
	
	private String m_slid  = "";
	private String m_sbatchnumber;
	private String m_sentrynumber;
	private String m_sentrydescription;
	private String m_sdatentrydate;
	private String m_slastline;
	private String m_sdatdocdate;
	private String m_sfiscalyear;
	private String m_sfiscalperiod;
	private String m_sourceledgertransactionlineid;
	private String m_ssourceledger;
	private String m_autoreverse;

	private ArrayList<GLTransactionBatchLine>m_arrBatchEntryLines;

	public static final int LINE_NUMBER_PADDING_LENGTH = 6;
	public static final String LINE_NUMBER_PARAMETER = "LINENOPARAM";

	public GLTransactionBatchEntry() 
	{
		initializeVariables();
	}
	public GLTransactionBatchEntry(HttpServletRequest req){
		//Read the batch fields from a servlet request:
		initializeVariables();
		
		setslid(clsManageRequestParameters.get_Request_Parameter(SMTablegltransactionbatchentries.lid, req).replace("&quot;", "\""));
		if(getslid().compareToIgnoreCase("") == 0){
			setslid("-1");
		}
		
		setsbatchnumber(clsManageRequestParameters.get_Request_Parameter(SMTablegltransactionbatchentries.lbatchnumber, req).replace("&quot;", "\""));
		if(getsbatchnumber().compareToIgnoreCase("") == 0){
			setsbatchnumber("-1");
		}
		setsentrynumber(clsManageRequestParameters.get_Request_Parameter(SMTablegltransactionbatchentries.lentrynumber, req).replace("&quot;", "\""));
		if(getsentrynumber().compareToIgnoreCase("") == 0){
			setsentrynumber("-1");
		}
		setsentrydescription(clsManageRequestParameters.get_Request_Parameter(SMTablegltransactionbatchentries.sentrydescription, req).replace("&quot;", "\""));
		setsdatentrydate(clsManageRequestParameters.get_Request_Parameter(SMTablegltransactionbatchentries.datentrydate, req).replace("&quot;", "\""));
		if (clsDateAndTimeConversions.IsValidDateString(SMUtilities.DATE_FORMAT_SIX_DIGIT_FOR_DISPLAY, getsdatentrydate())){
			try {
				setsdatentrydate(clsDateAndTimeConversions.convertDateFormat(getsdatentrydate(), SMUtilities.DATE_FORMAT_SIX_DIGIT_FOR_DISPLAY, SMUtilities.DATE_FORMAT_FOR_DISPLAY, SMUtilities.EMPTY_DATE_VALUE));
			} catch (Exception e) {
				//Don't have to do anything here, since we only have to deal with it IF the user entered a valid '6 digit' date (e.g. 060117)
			}
		}
		if(getsdatentrydate().compareToIgnoreCase("") == 0){
			setsdatentrydate(SMUtilities.EMPTY_DATE_VALUE);
		}
		setslastline(clsManageRequestParameters.get_Request_Parameter(SMTablegltransactionbatchentries.llastline, req).replace("&quot;", "\""));
		setsdatdocdate(clsManageRequestParameters.get_Request_Parameter(SMTablegltransactionbatchentries.datdocdate, req).replace("&quot;", "\""));
		if (clsDateAndTimeConversions.IsValidDateString(SMUtilities.DATE_FORMAT_SIX_DIGIT_FOR_DISPLAY, getsdatdocdate())){
			try {
				setsdatdocdate(clsDateAndTimeConversions.convertDateFormat(getsdatdocdate(), SMUtilities.DATE_FORMAT_SIX_DIGIT_FOR_DISPLAY, SMUtilities.DATE_FORMAT_FOR_DISPLAY, SMUtilities.EMPTY_DATE_VALUE));
			} catch (Exception e) {
				//Don't have to do anything here, since we only have to deal with it IF the user entered a valid '6 digit' date (e.g. 060117)
			}
		}
		if(getsdatdocdate().compareToIgnoreCase("") == 0){
			setsdatdocdate(clsDateAndTimeConversions.now(SMUtilities.DATE_FORMAT_FOR_DISPLAY));
		}
		setsfiscalyear(clsManageRequestParameters.get_Request_Parameter(SMTablegltransactionbatchentries.ifiscalyear, req).replace("&quot;", "\""));
		setsfiscalperiod(clsManageRequestParameters.get_Request_Parameter(SMTablegltransactionbatchentries.ifiscalperiod, req).replace("&quot;", "\""));
		setssourceledgertransactionlineid(clsManageRequestParameters.get_Request_Parameter(SMTablegltransactionbatchentries.lsourceledgertransactionlineid, req).replace("&quot;", "\""));
		setssourceledger(clsManageRequestParameters.get_Request_Parameter(SMTablegltransactionbatchentries.ssourceledger, req).replace("&quot;", "\""));
		if (clsManageRequestParameters.get_Request_Parameter(SMTablegltransactionbatchentries.iautoreverse, req).compareToIgnoreCase("") != 0){
			setsautoreverse("1");
		}else{
			setsautoreverse("0");
		}

		readEntryLines(req);
	}

	private void readEntryLines(HttpServletRequest request){
		//Read the entry lines:
    	Enumeration <String> eParams = request.getParameterNames();
    	String sLineParam = "";
    	String sLineNumber = "";
    	int iLineNumber = 0;
    	String sFieldName = "";
    	String sParamValue = "";
    	boolean bAddingNewLine = false;
    	GLTransactionBatchLine newline = new GLTransactionBatchLine();
    	while (eParams.hasMoreElements()){
    		sLineParam = eParams.nextElement();
    		//System.out.println("[1490711688] sLineParam = '" + sLineParam +"'");
    		//If it contains a line number parameter, then it's an GLTransactionBatchLine field:
    		if (sLineParam.startsWith(GLTransactionBatchEntry.LINE_NUMBER_PARAMETER)){
    			//System.out.println("[1490711588] sLineParam = '" + sLineParam +"'");
    			sLineNumber = sLineParam.substring(
    				GLTransactionBatchEntry.LINE_NUMBER_PARAMETER.length(),
    				GLTransactionBatchEntry.LINE_NUMBER_PARAMETER.length() + GLTransactionBatchEntry.LINE_NUMBER_PADDING_LENGTH);
    			iLineNumber = Integer.parseInt(sLineNumber);
    			//System.out.println("[1490711589] sLineNumber = '" + sLineNumber +"'");
    			sFieldName = sLineParam.substring(GLTransactionBatchEntry.LINE_NUMBER_PARAMETER.length() + GLTransactionBatchEntry.LINE_NUMBER_PADDING_LENGTH);
    			//System.out.println("[1490711590] sFieldName = '" + sFieldName +"'");
    			sParamValue = clsManageRequestParameters.get_Request_Parameter(sLineParam, request).trim();
    			//System.out.println("[1490711591] sParamValue = '" + sParamValue +"'");
    			//If the line array needs another row to fit all the line numbers, add it now:
				while (m_arrBatchEntryLines.size() < iLineNumber){
					GLTransactionBatchLine line = new GLTransactionBatchLine();
					m_arrBatchEntryLines.add(line);
				}
				
				//If any of the line fields have a '0' for their line number, then that means the user is adding a new field:
    			if (iLineNumber == 0){
    				bAddingNewLine = true;

    				//Now update the new line, and we'll add it to the entry down below:
        			if (sFieldName.compareToIgnoreCase(SMTablegltransactionbatchlines.bdcreditamt) == 0){
        				newline.setscreditamt(sParamValue);
        			}
        			if (sFieldName.compareToIgnoreCase(SMTablegltransactionbatchlines.bddebitamt) == 0){
        				newline.setsdebitamt(sParamValue);
        			}
        			if (sFieldName.compareToIgnoreCase(SMTablegltransactionbatchlines.dattransactiondate) == 0){
        				newline.setstransactiondate(sParamValue);
        			}
        			if (sFieldName.compareToIgnoreCase(SMTablegltransactionbatchlines.lid) == 0){
        				newline.setslid(sParamValue);
        			}
        			if (sFieldName.compareToIgnoreCase(SMTablegltransactionbatchlines.llinenumber) == 0){
        				newline.setslinenumber(sParamValue);
        			}
        			if (sFieldName.compareToIgnoreCase(SMTablegltransactionbatchlines.sacctid) == 0){
        				newline.setsacctid(sParamValue);
        			}
        			if (sFieldName.compareToIgnoreCase(SMTablegltransactionbatchlines.scomment) == 0){
        				newline.setscomment(sParamValue);
        			}
        			if (sFieldName.compareToIgnoreCase(SMTablegltransactionbatchlines.sdescription) == 0){
        				newline.setsdescription(sParamValue);
        			}
        			if (sFieldName.compareToIgnoreCase(SMTablegltransactionbatchlines.sreference) == 0){
        				newline.setsreference(sParamValue);
        			}
        			if (sFieldName.compareToIgnoreCase(SMTablegltransactionbatchlines.ssourceledger) == 0){
        				newline.setssourceledger(sParamValue);
        			}
        			if (sFieldName.compareToIgnoreCase(SMTablegltransactionbatchlines.ssourcetype) == 0){
        				newline.setssourcetype(sParamValue);
        			}
    			}else{
        			//Now update the field on the line we're reading:
        			if (sFieldName.compareToIgnoreCase(SMTablegltransactionbatchlines.bdcreditamt) == 0){
        				m_arrBatchEntryLines.get(iLineNumber - 1).setscreditamt(sParamValue);
        				//System.out.println("[1511887696] - sParamValue = '" + sParamValue + "', m_arrBatchEntryLines.get(iLineNumber - 1).getsbdamount() = " + m_arrBatchEntryLines.get(iLineNumber - 1).getsbdamount());
        			}
        			if (sFieldName.compareToIgnoreCase(SMTablegltransactionbatchlines.bddebitamt) == 0){
        				m_arrBatchEntryLines.get(iLineNumber - 1).setsdebitamt(sParamValue);
        			}
           			if (sFieldName.compareToIgnoreCase(SMTablegltransactionbatchlines.dattransactiondate) == 0){
        				m_arrBatchEntryLines.get(iLineNumber - 1).setstransactiondate(sParamValue);
        			}
        			if (sFieldName.compareToIgnoreCase(SMTablegltransactionbatchlines.lid) == 0){
        				m_arrBatchEntryLines.get(iLineNumber - 1).setslid(sParamValue);
        			}
        			if (sFieldName.compareToIgnoreCase(SMTablegltransactionbatchlines.llinenumber) == 0){
        				m_arrBatchEntryLines.get(iLineNumber - 1).setslinenumber(sParamValue);
        			}
        			if (sFieldName.compareToIgnoreCase(SMTablegltransactionbatchlines.sacctid) == 0){
        				m_arrBatchEntryLines.get(iLineNumber - 1).setsacctid(sParamValue);
        			}
        			if (sFieldName.compareToIgnoreCase(SMTablegltransactionbatchlines.scomment) == 0){
        				m_arrBatchEntryLines.get(iLineNumber - 1).setscomment(sParamValue);
        			}
        			if (sFieldName.compareToIgnoreCase(SMTablegltransactionbatchlines.sdescription) == 0){
        				m_arrBatchEntryLines.get(iLineNumber - 1).setsdescription(sParamValue);
        			}
        			if (sFieldName.compareToIgnoreCase(SMTablegltransactionbatchlines.sreference) == 0){
        				m_arrBatchEntryLines.get(iLineNumber - 1).setsreference(sParamValue);
        			}
        			if (sFieldName.compareToIgnoreCase(SMTablegltransactionbatchlines.ssourceledger) == 0){
        				m_arrBatchEntryLines.get(iLineNumber - 1).setssourceledger(sParamValue);
        			}
        			if (sFieldName.compareToIgnoreCase(SMTablegltransactionbatchlines.ssourcetype) == 0){
        				m_arrBatchEntryLines.get(iLineNumber - 1).setssourcetype(sParamValue);
        			}
    			}
    		}
    	}

    	//If the user was adding a new line, then....
    	if (bAddingNewLine){
    		//Just add that line to the entry:
    		//If the user has actually added anything to the new line:
    		if (
    			(newline.getsacctid().compareToIgnoreCase("") != 0)
    			|| (newline.getscomment().compareToIgnoreCase("") != 0)
    			|| (newline.getscreditamt().compareToIgnoreCase("0.00") != 0)
    			|| (newline.getsdebitamt().compareToIgnoreCase("0.00") != 0)
    			|| (newline.getsdescription().compareToIgnoreCase("") != 0)
    			|| (newline.getsreference().compareToIgnoreCase("") != 0)
    			|| (newline.getssourceledger().compareToIgnoreCase("") != 0)
    			|| (newline.getssourcetype().compareToIgnoreCase("") != 0)
    			|| (newline.getstransactiondate().compareToIgnoreCase("") != 0)
    		){
    			addLine(newline);
    		}
    	}
    	//Make sure we set the batch number and entry number:
    	for (int i = 0; i < m_arrBatchEntryLines.size(); i++){
    		m_arrBatchEntryLines.get(i).setsbatchnumber(getsbatchnumber());
    		m_arrBatchEntryLines.get(i).setsentrynumber(getsentrynumber());
    	}
	}
	
	public void save_without_data_transaction (Connection conn, String sUserID, boolean bBatchIsBeingPosted) throws Exception{

		//long lStarttime = System.currentTimeMillis();
		
		try {
			validate_fields(conn, sUserID, bBatchIsBeingPosted);
		} catch (Exception e1) {
			throw new Exception(e1.getMessage());
		}
		
		//System.out.println("[1543341850] - elapsed time 10 = " + (System.currentTimeMillis() - lStarttime) + " ms");
		
		String SQL = "";
		SQL = "INSERT into " + SMTablegltransactionbatchentries.TableName
			+ " (" 
			+ SMTablegltransactionbatchentries.datdocdate
			+ ", " + SMTablegltransactionbatchentries.datentrydate
			+ ", " + SMTablegltransactionbatchentries.iautoreverse
			+ ", " + SMTablegltransactionbatchentries.ifiscalperiod
			+ ", " + SMTablegltransactionbatchentries.ifiscalyear
			+ ", " + SMTablegltransactionbatchentries.lbatchnumber
			+ ", " + SMTablegltransactionbatchentries.lentrynumber
			+ ", " + SMTablegltransactionbatchentries.llastline
			+ ", " + SMTablegltransactionbatchentries.lsourceledgertransactionlineid
			+ ", " + SMTablegltransactionbatchentries.sentrydescription
			+ ", " + SMTablegltransactionbatchentries.ssourceledger
			+ ")"
			+ " VALUES ("
			+ "'" + getsdatdocdateInSQLFormat() + "'"
			+ ", '" + getsentrydateInSQLFormat() + "'"
			+ ", " + getsautoreverse()
			+ ", " + getsfiscalperiod()
			+ ", " + getsfiscalyear()
			+ ", " + getsbatchnumber()
			+ ", " + getsentrynumber()
			+ ", " + getslastline()
			+ ", " + getssourceledgertransactionlineid()
			+ ", '" + clsDatabaseFunctions.FormatSQLStatement(getsentrydescription()) + "'"
			+ ", '" + clsDatabaseFunctions.FormatSQLStatement(getssourceledger()) + "'"
			+ ")"
			
			+ " ON DUPLICATE KEY UPDATE"
			+ " " + SMTablegltransactionbatchentries.datdocdate + " = '" + getsdatdocdateInSQLFormat() + "'"
			+ ", " + SMTablegltransactionbatchentries.datentrydate + " = '" + getsentrydateInSQLFormat() + "'"
			+ ", " + SMTablegltransactionbatchentries.iautoreverse + " = " + getsautoreverse()
			+ ", " + SMTablegltransactionbatchentries.ifiscalperiod + " = " + getsfiscalperiod()
			+ ", " + SMTablegltransactionbatchentries.ifiscalyear + " = " + getsfiscalyear()
			+ ", " + SMTablegltransactionbatchentries.lbatchnumber + " = " + getsbatchnumber()
			+ ", " + SMTablegltransactionbatchentries.lentrynumber + " = " + getsentrynumber()
			+ ", " + SMTablegltransactionbatchentries.llastline + " = " + getslastline()
			+ ", " + SMTablegltransactionbatchentries.lsourceledgertransactionlineid + " = " + getssourceledgertransactionlineid()
			+ ", " + SMTablegltransactionbatchentries.sentrydescription + " = '" + clsDatabaseFunctions.FormatSQLStatement(getsentrydescription()) + "'"
			+ ", " + SMTablegltransactionbatchentries.ssourceledger + " = '" + clsDatabaseFunctions.FormatSQLStatement(getssourceledger()) + "'"
		;
		
		//System.out.println("[1494260359] - SQL = '" + SQL + "'");
		
		Statement stmt = conn.createStatement();
		try {
			stmt.execute(SQL);
		} catch (Exception e) {
			throw new Exception("Error [1555335865] updating GL transaction batch entry " + getsentrynumber() + " with SQL: '" + SQL + "' - " + e.getMessage());
		}
		if (
				(getslid().compareToIgnoreCase("-1") == 0)
				|| (getslid().compareToIgnoreCase("") == 0)
				|| (getslid().compareToIgnoreCase("0") == 0)
				
			){
			String sSQL = "SELECT "
				+ SMTablegltransactionbatchentries.lid
				+ " FROM " + SMTablegltransactionbatchentries.TableName
				+ " WHERE ("
					+ "(" + SMTablegltransactionbatchentries.lbatchnumber + " = " + getsbatchnumber() + ")"
					+ " AND (" + SMTablegltransactionbatchentries.lentrynumber + " = " + getsentrynumber() + ")"
				+ ")"
			;
					
			try {
				ResultSet rs = clsDatabaseFunctions.openResultSet(sSQL, conn);
				if (rs.next()) {
					//System.out.println("[1543586793] - rs.getLong('SMTablegltransactionbatchentries.lid') = '" + rs.getLong(SMTablegltransactionbatchentries.lid) + "'");
					setslid(Long.toString(rs.getLong(SMTablegltransactionbatchentries.lid)));
				}else {
					setslid("0");
				}
				rs.close();
			} catch (SQLException e) {
				throw new Exception("Error [1555335866] Could not get last ID number - " + e.getMessage());
			}
			//If something went wrong, we can't get the last ID:
			if (getslid().compareToIgnoreCase("0") == 0){
				throw new Exception("Error [1555335867] Could not get last ID number.");
			}
		}
		
		//Finally, save the lines....
		try {
			saveLines(conn, sUserID, bBatchIsBeingPosted);
		} catch (Exception e) {
			throw new Exception("Error [1555335868] saving entry lines - " + e.getMessage() + ".");
		}
		
		return;
	}
	public void validate_fields(Connection conn, String sUserID, boolean bBatchIsBeingPosted) throws Exception{
		
		String sResult = "";
		
		try {
			m_slid  = clsValidateFormFields.validateLongIntegerField(m_slid, "Entry ID", -1, clsValidateFormFields.MAX_LONG_VALUE);
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
		
		try {
			m_sentrydescription = clsValidateFormFields.validateStringField(
					m_sentrydescription, 
				SMTablegltransactionbatchentries.sentrydescriptionLength, 
				"Entry description", 
				true
			);
		} catch (Exception e) {
			sResult += "  " + e.getMessage() + ".";
		}

		@SuppressWarnings("unused")
		java.sql.Date datEntry = null;
		try {
			datEntry = clsDateAndTimeConversions.StringTojavaSQLDate(SMUtilities.DATE_FORMAT_FOR_DISPLAY, m_sdatentrydate);
		} catch (ParseException e) {
			try {
				m_sdatentrydate  = clsValidateFormFields.validateStandardDateField(m_sdatentrydate, "Entry date", false);
			} catch (Exception e1) {
				sResult += "  " + e.getMessage() + ".";
			}
		}

		// Make sure the entry date is always within the posting date range:
        SMOption opt = new SMOption();
        if (!opt.load(conn)){
        	sResult += "  " + "Error [1555336103] loading SM Options to check posting date range - " + opt.getErrorMessage() + ".";
        }else{
            try {
    			opt.checkDateForPosting(m_sdatentrydate, "Entry Date", conn, sUserID);
    		} catch (Exception e) {
    			sResult += "  " + "Error [1555336104]  - " + e.getMessage() + ".";
    		}
        }
		
		try {
			m_slastline = clsValidateFormFields.validateLongIntegerField(
				Integer.toString(m_arrBatchEntryLines.size()), 
				"Last line", 
				0, 
				clsValidateFormFields.MAX_LONG_VALUE);
		} catch (Exception e) {
			sResult += "  " + e.getMessage() + ".";
		}

		@SuppressWarnings("unused")
		java.sql.Date datDocument = null;
		try {
			datDocument = clsDateAndTimeConversions.StringTojavaSQLDate(SMUtilities.DATE_FORMAT_FOR_DISPLAY, m_sdatdocdate);
		} catch (ParseException e) {
			try {
				m_sdatdocdate  = clsValidateFormFields.validateStandardDateField(m_sdatdocdate, "Document date", false);
			} catch (Exception e1) {
				sResult += "  " + e.getMessage() + ".";
			}
		}
		
		try {
			m_sfiscalyear = clsValidateFormFields.validateLongIntegerField(
					m_sfiscalyear, 
				"Fiscal year", 
				2000L, 
				2050L);
		} catch (Exception e) {
			sResult += "  " + e.getMessage() + ".";
		}
		
		try {
			m_sfiscalperiod = clsValidateFormFields.validateLongIntegerField(
					m_sfiscalperiod, 
				"Fiscal period", 
				1L, 
				13L);
		} catch (Exception e) {
			sResult += "  " + e.getMessage() + ".";
		}
		
		try {
			m_sfiscalperiod = clsValidateFormFields.validateLongIntegerField(
					m_sourceledgertransactionlineid, 
				"Source ledger transaction line ID", 
				0L, 
				clsValidateFormFields.MAX_LONG_VALUE);
		} catch (Exception e) {
			sResult += "  " + e.getMessage() + ".";
		}
		
		try {
			m_ssourceledger = clsValidateFormFields.validateStringField(
				m_ssourceledger, 
				SMTablegltransactionbatchentries.ssourceledgerLength, 
				"Source ledger", 
				false
			);
		} catch (Exception e) {
			sResult += "  " + e.getMessage() + ".";
		}
		
		try {
			m_autoreverse = clsValidateFormFields.validateIntegerField(
				m_autoreverse, 
				"Auto-reverse", 
				0, 
				1);
		} catch (Exception e) {
			sResult += "  " + e.getMessage() + ".";
		}
		
		//Validate the lines:
		for (int i = 0; i < m_arrBatchEntryLines.size(); i++){
			GLTransactionBatchLine line = m_arrBatchEntryLines.get(i);
			line.setsentrynumber(getsentrynumber());
			line.setslinenumber(Integer.toString(i + 1));
			try {
				line.validate_fields(conn, bBatchIsBeingPosted);
			} catch (Exception e) {
				sResult += "  In line " + line.getslinenumber() + " - " + e.getMessage() + ".";
			}
		}
		
		if (sResult.compareToIgnoreCase("") != 0){
			throw new Exception(sResult);
		}
		
		return;
	}

	public void entryIsInBalance(Connection conn) throws Exception{
		BigDecimal bdLineTotalDebitAmount = new BigDecimal("0.00");
		BigDecimal bdLineTotalCreditAmount = new BigDecimal("0.00");
		
		for (int i = 0; i < m_arrBatchEntryLines.size(); i++){
			GLTransactionBatchLine line = m_arrBatchEntryLines.get(i);
			bdLineTotalDebitAmount = bdLineTotalDebitAmount.add(new BigDecimal(line.getsdebitamt().replaceAll(",", "")));
			bdLineTotalCreditAmount = bdLineTotalCreditAmount.add(new BigDecimal(line.getscreditamt().replaceAll(",", "")));
		}
		
		//Confirm that the debit and credit totals match:
		if (bdLineTotalDebitAmount.compareTo(bdLineTotalCreditAmount) != 0){
			throw new Exception("Debit total (" 
				+ clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(bdLineTotalDebitAmount) 
				+ ") doesn't match the credit total (" + clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(bdLineTotalCreditAmount)
				+ ") on entry number " + getsentrynumber() + ".");
		}
		return;
	}

	public void load(ServletContext context, String sDBID, String sUserID) throws Exception{
		
		Connection conn;
		try {
			conn = clsDatabaseFunctions.getConnectionWithException(
				context, 
				sDBID, 
				"MySQL", 
				SMUtilities.getFullClassName(this.toString()) + ".load - user: " + sUserID);
		} catch (Exception e) {
			throw new Exception("Error [1555337049] getting connection - " + e.getMessage());
		}
		
		try {
			load(conn);
		} catch (Exception e) {
			throw new Exception("Error [1555337050] loading - " + e.getMessage());
		}
		clsDatabaseFunctions.freeConnection(context, conn, "[1555337051]");
	}
	
	public void load(Connection conn) throws Exception{
		
		String SQL = "";
		if (
			(getslid().compareToIgnoreCase("") != 0)
			&& (getslid().compareToIgnoreCase("-1") != 0)
			&& (getslid().compareToIgnoreCase("0") != 0)	
		){
			SQL = "SELECT * FROM " + SMTablegltransactionbatchentries.TableName
				+ " WHERE ("
					+ "(" + SMTablegltransactionbatchentries.lid + " = " + getslid() + ")"
				+ ")"
			;
		}else{
			if (
				(getsbatchnumber().compareToIgnoreCase("") != 0)
				&& (getsentrynumber().compareToIgnoreCase("") != 0)
			){
			SQL = "SELECT * FROM " + SMTablegltransactionbatchentries.TableName
					+ " WHERE ("
						+ "(" + SMTablegltransactionbatchentries.lbatchnumber + " = " + getsbatchnumber() + ")"
						+ " AND (" + SMTablegltransactionbatchentries.lentrynumber + " = " + getsentrynumber() + ")"
					+ ")"
				;
			}
		}
		
		if (SQL.compareToIgnoreCase("") == 0){
			throw new Exception("Error [1555337187] - can't load batch entry without an ID or batch and entry number.");
		}
		
		ResultSet rs = null;
		try {
			rs = clsDatabaseFunctions.openResultSet(SQL, conn);
			if (rs.next()){
				setslid(Long.toString(rs.getLong(SMTablegltransactionbatchentries.lid)));
				setsbatchnumber(Long.toString(rs.getLong(SMTablegltransactionbatchentries.lbatchnumber)));
				setsentrynumber(Long.toString(rs.getLong(SMTablegltransactionbatchentries.lentrynumber)));
				setsentrydescription(rs.getString(SMTablegltransactionbatchentries.sentrydescription));
				setsdatentrydate(clsDateAndTimeConversions.resultsetDateStringToFormattedString(
					rs.getString(SMTablegltransactionbatchentries.datentrydate), SMUtilities.DATE_FORMAT_FOR_DISPLAY, SMUtilities.EMPTY_DATE_VALUE));
				setslastline(Long.toString(rs.getLong(SMTablegltransactionbatchentries.llastline)));
				setsdatdocdate(clsDateAndTimeConversions.resultsetDateStringToFormattedString(
					rs.getString(SMTablegltransactionbatchentries.datdocdate), SMUtilities.DATE_FORMAT_FOR_DISPLAY, SMUtilities.EMPTY_DATE_VALUE));
				setsfiscalyear(Long.toString(rs.getLong(SMTablegltransactionbatchentries.ifiscalyear)));
				setsfiscalperiod(Long.toString(rs.getLong(SMTablegltransactionbatchentries.ifiscalperiod)));
				setssourceledgertransactionlineid(Long.toString(rs.getLong(SMTablegltransactionbatchentries.lsourceledgertransactionlineid)));
				setssourceledger(rs.getString(SMTablegltransactionbatchentries.ssourceledger));
				setsautoreverse(Integer.toString(rs.getInt(SMTablegltransactionbatchentries.iautoreverse)));
			}else{
				rs.close();
				throw new Exception("Error [1555337558] - No GL transaction batch entry found with lid = " + getslid() + ".");
			}
			rs.close();
		} catch (Exception e) {
			rs.close();
			throw new Exception("Error [1555337559] - loading " + OBJECT_NAME + " with ID " + getslid() + " - " + e.getMessage());
		}
		
		//Load the lines:
		m_arrBatchEntryLines.clear();
		SQL = "SELECT"
			+ " " + SMTablegltransactionbatchlines.lid
			+ " FROM " + SMTablegltransactionbatchlines.TableName 
			+ " WHERE ("
				+ "(" + SMTablegltransactionbatchlines.lbatchnumber + " = " + getsbatchnumber() + ")"
				+ " AND (" + SMTablegltransactionbatchlines.lentrynumber + " = " + getsentrynumber() + ")"
			+ ") ORDER BY " + SMTablegltransactionbatchlines.llinenumber
		;
		rs = null;
		try {
			rs = clsDatabaseFunctions.openResultSet(SQL, conn);
			while (rs.next()){
				GLTransactionBatchLine line = new GLTransactionBatchLine();
				line.load(conn, Long.toString(rs.getLong(SMTablegltransactionbatchlines.lid)));
				addLine(line);
			}
			rs.close();
		} catch (Exception e) {
			rs.close();
			throw new Exception("Error [1555337560] loading batch entry lines - " + e.getMessage());
		}
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

	public String getsentrydescription(){
		return m_sentrydescription;
	}
	public void setsentrydescription(String sEntryDescription){
		m_sentrydescription = sEntryDescription;
	}

	public String getsdatentrydate(){
		return m_sdatentrydate;
	}
	public String getsentrydateInSQLFormat() throws Exception{
		if (m_sdatentrydate.compareToIgnoreCase("") == 0){
			return SMUtilities.EMPTY_SQL_DATE_VALUE;
		}else{
			return clsDateAndTimeConversions.convertDateFormat(m_sdatentrydate, SMUtilities.DATE_FORMAT_FOR_DISPLAY, SMUtilities.DATE_FORMAT_FOR_SQL, SMUtilities.EMPTY_SQL_DATE_VALUE);
		}
	}
	public void setsdatentrydate(String sdatEntryDate){
		m_sdatentrydate = sdatEntryDate;
	}

	public String getslastline(){
		return m_slastline;
	}
	public void setslastline(String sLastLine){
		m_slastline = sLastLine;
	}
	public String getsdatdocdate(){
		return m_sdatdocdate;
	}
	public String getsdatdocdateInSQLFormat() throws Exception{
		if (m_sdatdocdate.compareToIgnoreCase("") == 0){
			return SMUtilities.EMPTY_SQL_DATE_VALUE;
		}else{
			return clsDateAndTimeConversions.convertDateFormat(m_sdatdocdate, SMUtilities.DATE_FORMAT_FOR_DISPLAY, SMUtilities.DATE_FORMAT_FOR_SQL, SMUtilities.EMPTY_SQL_DATE_VALUE);
		}
	}
	public void setsdatdocdate(String sdatDocDate){
		m_sdatdocdate = sdatDocDate;
	}
	public String getsfiscalyear(){
		return m_sfiscalyear;
	}
	public void setsfiscalyear(String sfiscalyear){
		m_sfiscalyear = sfiscalyear;
	}
	public String getsfiscalperiod(){
		return m_sfiscalperiod;
	}
	public void setsfiscalperiod(String sfiscalperiod){
		m_sfiscalperiod = sfiscalperiod;
	}
	public String getssourceledgertransactionlineid(){
		return m_sourceledgertransactionlineid;
	}
	public void setssourceledgertransactionlineid(String ssourceledgertransactionlineid){
		m_sourceledgertransactionlineid = ssourceledgertransactionlineid;
	}
	public String getssourceledger(){
		return m_ssourceledger;
	}
	public void setssourceledger(String ssourceledger){
		m_ssourceledger = ssourceledger;
	}

	public String getsautoreverse(){
		return m_autoreverse;
	}
	public void setsautoreverse(String sautoreverse){
		m_autoreverse = sautoreverse;
	}
	public BigDecimal getDebitTotal () throws Exception{
		BigDecimal bdDebitTotal = new BigDecimal("0.00");
		
		for (int i = 0; i < m_arrBatchEntryLines.size(); i++){
			bdDebitTotal = bdDebitTotal.add(new BigDecimal (m_arrBatchEntryLines.get(i).getsdebitamt().replaceAll(",", "")));
		}
		
		return bdDebitTotal;
	}
	public BigDecimal getCreditTotal () throws Exception{
		BigDecimal bdCreditTotal = new BigDecimal("0.00");
		
		for (int i = 0; i < m_arrBatchEntryLines.size(); i++){
			bdCreditTotal = bdCreditTotal.add(new BigDecimal (m_arrBatchEntryLines.get(i).getscreditamt().replaceAll(",", "")));
		}
		
		return bdCreditTotal;
	}
	public GLTransactionBatchEntry copyEntry(){
		GLTransactionBatchEntry newentry = new GLTransactionBatchEntry();
		newentry.setsautoreverse(getsautoreverse());
		newentry.setsbatchnumber(getsbatchnumber());
		newentry.setsdatdocdate(getsdatdocdate());
		newentry.setsdatentrydate(getsdatentrydate());
		newentry.setsentrydescription(getsentrydescription());
		newentry.setsentrynumber(getsentrynumber());
		newentry.setsfiscalperiod(getsfiscalperiod());
		newentry.setsfiscalyear(getsfiscalyear());
		newentry.setslastline(getslastline());
		newentry.setslid(getslid());
		newentry.setssourceledger(getssourceledger());
		newentry.setssourceledgertransactionlineid(getssourceledgertransactionlineid());
		
		for (int i = 0; i < m_arrBatchEntryLines.size(); i++){
			newentry.addLine(m_arrBatchEntryLines.get(i));
		}
		
		return newentry;
		
	}

	public void addLine(GLTransactionBatchLine line){
		m_arrBatchEntryLines.add(line);
	}

	public ArrayList<GLTransactionBatchLine> getLineArray(){
		return m_arrBatchEntryLines;
	}
	private void saveLines(Connection conn, String sUser, boolean bBatchIsBeingPosted) throws Exception{
		for (int i = 0; i < m_arrBatchEntryLines.size(); i++){
			GLTransactionBatchLine line = m_arrBatchEntryLines.get(i);
			line.setsbatchnumber(getsbatchnumber());
			try {
				line.save_without_data_transaction(conn, sUser, bBatchIsBeingPosted);
			} catch (Exception e) {
				throw new Exception("Error [1555338302] saving line number " + line.getslinenumber() 
					+ " on entry number " + this.getsentrynumber() + " - " + e.getMessage()
				);
			}
		}
		//We also have to delete any EXTRA lines that might be left that are higher than our current highest line number:
		//This can happen if we removed a line and we now have fewer lines than we previously had:
		String SQL = "DELETE FROM " + SMTablegltransactionbatchlines.TableName
			+ " WHERE ("
				+ "(" + SMTablegltransactionbatchlines.lbatchnumber + " = " + getsbatchnumber() + ")"
				+ " AND (" + SMTablegltransactionbatchlines.lentrynumber + " = " + getsentrynumber() + ")"
				+ " AND (" + SMTablegltransactionbatchlines.llinenumber + " > " + Integer.toString(m_arrBatchEntryLines.size()) + ")"
			+ ")"
		;
		try {
			Statement stmt = conn.createStatement();
			stmt.execute(SQL);
		} catch (Exception e) {
			throw new Exception("Error [1555338303] deleting leftover entry lines - " + e.getMessage());
		}
		
	}
	public void removeLineByLineNumber(String sLineNumber) throws Exception{
    	//System.out.println("[1489528400] - sEntryNumber = " + sEntryNumber);
		
		boolean bLineNumberWasFound = false;
		for (int i = 0; i < m_arrBatchEntryLines.size(); i++){
			if (m_arrBatchEntryLines.get(i).getslinenumber().compareToIgnoreCase(sLineNumber) == 0){
				m_arrBatchEntryLines.remove(i);
				bLineNumberWasFound = true;
			}
		}
   	
    	if (!bLineNumberWasFound){
    		throw new Exception("Line number '" + sLineNumber + "' was not found in the entry.");
    	}
	}

	public String getQueryString(){
		//Particular to the specific class
		String sQueryString = "";
		sQueryString += SMTablegltransactionbatchentries.datdocdate + "=" + clsServletUtilities.URLEncode(getsdatdocdate());
		sQueryString += "&" + SMTablegltransactionbatchentries.datentrydate + "=" + clsServletUtilities.URLEncode(getsdatentrydate());
		sQueryString += "&" + SMTablegltransactionbatchentries.iautoreverse + "=" + clsServletUtilities.URLEncode(getsautoreverse());
		sQueryString += "&" + SMTablegltransactionbatchentries.ifiscalperiod + "=" + clsServletUtilities.URLEncode(getsfiscalperiod());
		sQueryString += "&" + SMTablegltransactionbatchentries.ifiscalyear + "=" + clsServletUtilities.URLEncode(getsfiscalyear());
		sQueryString += "&" + SMTablegltransactionbatchentries.lbatchnumber + "=" + clsServletUtilities.URLEncode(getsbatchnumber());
		sQueryString += "&" + SMTablegltransactionbatchentries.lentrynumber + "=" + clsServletUtilities.URLEncode(getsentrynumber());
		sQueryString += "&" + SMTablegltransactionbatchentries.lid + "=" + clsServletUtilities.URLEncode(getslid());
		sQueryString += "&" + SMTablegltransactionbatchentries.llastline + "=" + clsServletUtilities.URLEncode(getslastline());
		sQueryString += "&" + SMTablegltransactionbatchentries.lsourceledgertransactionlineid + "=" + clsServletUtilities.URLEncode(getssourceledgertransactionlineid());
		sQueryString += "&" + SMTablegltransactionbatchentries.sentrydescription + "=" + clsServletUtilities.URLEncode(getsentrydescription());
		sQueryString += "&" + SMTablegltransactionbatchentries.ssourceledger + "=" + clsServletUtilities.URLEncode(getssourceledger());
		return sQueryString;
	}
	public String dumpData(){
		String s = "";
		s += "  Doc date: " + getsdatdocdate() + "\n";
		s += "  Entry date: " + getsdatentrydate() + "\n";
		s += "  Auto reverse:" + getsautoreverse() + "\n";
		s += "  Batch: " + getsbatchnumber() + "\n";
		s += "  Entry: " + getsentrynumber() + "\n";
		s += "  Fiscal period: " + getsfiscalperiod() + "\n";
		s += "  Fiscal year: " + getsfiscalyear() + "\n";
		s += "  Entry ID: " + getslid() + "\n";
		s += "  Last line: " + getslastline() + "\n";
		s += "  Source ledger transaction line ID: " + getssourceledgertransactionlineid() + "\n";
		s += "  Desc: " + getsentrydescription() + "\n";
		s += "  Source ledger: " + getssourceledger() + "\n";
		
		s += "  -- Number of lines: " + m_arrBatchEntryLines.size() + "\n";
		
		for (int i = 0; i < m_arrBatchEntryLines.size(); i++){
			s += "  LINE " + (i + 1) + ":\n";
			s += m_arrBatchEntryLines.get(i).dumpData();
		}
		
		return s;
	}
	private void initializeVariables(){
		m_slid  = "-1";
		m_sbatchnumber = "-1";
		m_sentrynumber = "-1";
		m_sentrydescription = "";
		m_sdatentrydate = SMUtilities.EMPTY_DATE_VALUE;  //clsDateAndTimeConversions.now(SMUtilities.DATE_FORMAT_FOR_DISPLAY);
		m_slastline = "0";
		m_sdatdocdate = SMUtilities.EMPTY_DATE_VALUE;
		m_sfiscalyear = "0";
		m_sfiscalperiod = "0";
		m_sourceledgertransactionlineid = "0";
		m_ssourceledger = "";
		m_autoreverse = "0";
		m_arrBatchEntryLines = new ArrayList<GLTransactionBatchLine>(0);
	}
}
