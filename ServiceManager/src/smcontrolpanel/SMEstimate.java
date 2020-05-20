package smcontrolpanel;

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
import SMDataDefinition.SMTablesmestimates;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsDateAndTimeConversions;
import ServletUtilities.clsManageBigDecimals;
import ServletUtilities.clsManageRequestParameters;
import ServletUtilities.clsServletUtilities;
import ServletUtilities.clsValidateFormFields;
import smgl.GLEditEntryEdit;
import smgl.GLFiscalYear;
import smgl.GLSourceLedgers;
import smgl.GLTransactionBatchLine;

public class SMEstimate {

	public static final String OBJECT_NAME = "Estimate";
	
	private String m_lid;
	private String m_lsummarylid;
	private String m_lsummarylinenumber;
	private String m_sdescription;
	private String m_sprefixlabelitem;
	private String m_svendorquotenumber;
	private String m_ivendorquotelinenumber;
	private String m_bdquantity;
	private String m_sitemnumber;
	private String m_sproductdescription;
	private String m_sunitofmeasure;
	private String m_bdextendedcost;
	private String m_bdfreight;
	private String m_bdlaborquantity;
	private String m_bdlaborcostperunit;
	private String m_sadditionalpretaxcostlabel;
	private String m_bdadditionalpretaxcostamount;
	private String m_bdmarkupamount;
	private String m_sadditionalposttaxcostlabel;
	private String m_bdadditionalposttaxcostamount;
	private String m_bdlaborsellpriceperunit;
	private String m_lcreatedbyid;
	private String m_datetimecreated;
	private String m_screatedbyfullname;
	private String m_llastmodifiedbyid;
	private String m_datetimeslastmodifiedby;
	private String m_slastmodifiedbyfullname;
	
	private ArrayList<SMEstimateLine>arrEstimateLines;

	public static final int LINE_NUMBER_PADDING_LENGTH = 6;
	public static final String LINE_NUMBER_PARAMETER = "LINENOPARAM";

	public SMEstimate() 
	{
		/* initializeVariables(); */
	}
	public SMEstimate(HttpServletRequest req){
		//Read the batch fields from a servlet request:
		/* initializeVariables(); */
		
		m_lid = clsManageRequestParameters.get_Request_Parameter(SMTablesmestimates.lid, req).replace("&quot;", "\"");
		if(m_lid.compareToIgnoreCase("") == 0){
			m_lid = "-1";
		}
		
		m_lsummarylid = clsManageRequestParameters.get_Request_Parameter(SMTablesmestimates.lsummarylid, req).replace("&quot;", "\"");
		if(m_lsummarylid.compareToIgnoreCase("") == 0){
			m_lsummarylid = "-1";
		}
		
		m_lsummarylinenumber = clsManageRequestParameters.get_Request_Parameter(SMTablesmestimates.lsummarylinenumber, req).replace("&quot;", "\"");
		if(m_lsummarylinenumber.compareToIgnoreCase("") == 0){
			m_lsummarylinenumber = "-1";
		}
		
		m_sdescription = clsManageRequestParameters.get_Request_Parameter(SMTablesmestimates.sdescription, req).replace("&quot;", "\"");
		m_sprefixlabelitem = clsManageRequestParameters.get_Request_Parameter(SMTablesmestimates.sprefixlabelitem, req).replace("&quot;", "\"");
		m_svendorquotenumber = clsManageRequestParameters.get_Request_Parameter(SMTablesmestimates.svendorquotenumber, req).replace("&quot;", "\"");
		m_ivendorquotelinenumber = clsManageRequestParameters.get_Request_Parameter(SMTablesmestimates.ivendorquotelinenumber, req).replace("&quot;", "\"");
		m_bdquantity = clsManageRequestParameters.get_Request_Parameter(SMTablesmestimates.bdquantity, req).replace("&quot;", "\"");
		m_sitemnumber = clsManageRequestParameters.get_Request_Parameter(SMTablesmestimates.sitemnumber, req).replace("&quot;", "\"");
		m_sproductdescription = clsManageRequestParameters.get_Request_Parameter(SMTablesmestimates.sproductdescription, req).replace("&quot;", "\"");
		m_sunitofmeasure = clsManageRequestParameters.get_Request_Parameter(SMTablesmestimates.sunitofmeasure, req).replace("&quot;", "\"");
		m_bdextendedcost = clsManageRequestParameters.get_Request_Parameter(SMTablesmestimates.bdextendedcost, req).replace("&quot;", "\"");
		m_bdfreight = clsManageRequestParameters.get_Request_Parameter(SMTablesmestimates.bdfreight, req).replace("&quot;", "\"");
		m_bdlaborquantity = clsManageRequestParameters.get_Request_Parameter(SMTablesmestimates.bdlaborquantity, req).replace("&quot;", "\"");
		m_bdlaborcostperunit = clsManageRequestParameters.get_Request_Parameter(SMTablesmestimates.bdlaborcostperunit, req).replace("&quot;", "\"");
		m_sadditionalpretaxcostlabel = clsManageRequestParameters.get_Request_Parameter(SMTablesmestimates.sadditionalpretaxcostlabel, req).replace("&quot;", "\"");
		m_bdadditionalpretaxcostamount = clsManageRequestParameters.get_Request_Parameter(SMTablesmestimates.bdadditionalpretaxcostamount, req).replace("&quot;", "\"");
		m_bdmarkupamount = clsManageRequestParameters.get_Request_Parameter(SMTablesmestimates.bdmarkupamount, req).replace("&quot;", "\"");
		m_sadditionalposttaxcostlabel = clsManageRequestParameters.get_Request_Parameter(SMTablesmestimates.sadditionalposttaxcostlabel, req).replace("&quot;", "\"");
		m_bdadditionalposttaxcostamount = clsManageRequestParameters.get_Request_Parameter(SMTablesmestimates.bdadditionalposttaxcostamount, req).replace("&quot;", "\"");
		m_bdlaborsellpriceperunit = clsManageRequestParameters.get_Request_Parameter(SMTablesmestimates.bdlaborsellpriceperunit, req).replace("&quot;", "\"");
		m_lcreatedbyid = clsManageRequestParameters.get_Request_Parameter(SMTablesmestimates.lcreatedbyid, req).replace("&quot;", "\"");

		m_datetimecreated = clsManageRequestParameters.get_Request_Parameter(SMTablesmestimates.lcreatedbyid, req).replace("&quot;", "\"");
		if(m_datetimecreated.compareToIgnoreCase("") == 0){
			m_datetimecreated = SMUtilities.EMPTY_DATETIME_VALUE;
		}
		
		m_screatedbyfullname = clsManageRequestParameters.get_Request_Parameter(SMTablesmestimates.screatedbyfullname, req).replace("&quot;", "\"");
		m_llastmodifiedbyid = clsManageRequestParameters.get_Request_Parameter(SMTablesmestimates.llastmodifiedbyid, req).replace("&quot;", "\"");

		m_datetimeslastmodifiedby = clsManageRequestParameters.get_Request_Parameter(SMTablesmestimates.datetimeslastmodifiedby, req).replace("&quot;", "\"");
		if(m_datetimeslastmodifiedby.compareToIgnoreCase("") == 0){
			m_datetimeslastmodifiedby = SMUtilities.EMPTY_DATETIME_VALUE;
		}
		
		m_slastmodifiedbyfullname = clsManageRequestParameters.get_Request_Parameter(SMTablesmestimates.slastmodifiedbyfullname, req).replace("&quot;", "\"");
		
		/* readEntryLines(req); */
	}

	//TODO - left off here:
	/*
	private void readEntryLines(HttpServletRequest request){
		//Read the estimate lines:
    	Enumeration <String> eParams = request.getParameterNames();
    	String sLineParam = "";
    	String sLineNumber = "";
    	int iLineNumber = 0;
    	String sFieldName = "";
    	String sParamValue = "";
    	boolean bAddingNewLine = false;
    	SMEstimateLine newline = new SMEstimateLine();
    	while (eParams.hasMoreElements()){
    		sLineParam = eParams.nextElement();
    		//System.out.println("[1490711688] sLineParam = '" + sLineParam +"'");
    		//If it contains a line number parameter, then it's an GLTransactionBatchLine field:
    		if (sLineParam.startsWith(SMEstimate.LINE_NUMBER_PARAMETER)){
    			//System.out.println("[1490711588] sLineParam = '" + sLineParam +"'");
    			sLineNumber = sLineParam.substring(
    				SMEstimate.LINE_NUMBER_PARAMETER.length(),
    				SMEstimate.LINE_NUMBER_PARAMETER.length() + SMEstimate.LINE_NUMBER_PADDING_LENGTH);
    			iLineNumber = Integer.parseInt(sLineNumber);
    			//System.out.println("[1490711589] sLineNumber = '" + sLineNumber +"'");
    			sFieldName = sLineParam.substring(SMEstimate.LINE_NUMBER_PARAMETER.length() + SMEstimate.LINE_NUMBER_PADDING_LENGTH);
    			//System.out.println("[1490711590] sFieldName = '" + sFieldName +"'");
    			sParamValue = clsManageRequestParameters.get_Request_Parameter(sLineParam, request).trim();
    			//System.out.println("[1490711591] sParamValue = '" + sParamValue +"'");
    			//If the line array needs another row to fit all the line numbers, add it now:
				while (arrEstimateLines.size() < iLineNumber){
					GLTransactionBatchLine line = new GLTransactionBatchLine();
					arrEstimateLines.add(line);
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

        			if (sFieldName.compareToIgnoreCase(GLEditEntryEdit.PARAM_SOURCE_LEDGER_AND_TYPE) == 0){
		    			String sSourceLedgerAndType[] =sParamValue.replace("&quot;", "\"").split(GLSourceLedgers.SOURCE_LEDGER_AND_TYPE_DELIMITER);
	    				try {
	    					newline.setssourceledger(sSourceLedgerAndType[0]);
	    				} catch (Exception e) {
	    					newline.setssourceledger("");
	    				}
	    				try {
	    					newline.setssourcetype(sSourceLedgerAndType[1]);
	    				} catch (Exception e) {
	    					newline.setssourcetype("");
	    				}
        			}
        			
    			}else{
        			//Now update the field on the line we're reading:
        			if (sFieldName.compareToIgnoreCase(SMTablegltransactionbatchlines.bdcreditamt) == 0){
        				arrEstimateLines.get(iLineNumber - 1).setscreditamt(sParamValue);
        				//System.out.println("[1511887696] - sParamValue = '" + sParamValue + "', m_arrBatchEntryLines.get(iLineNumber - 1).getsbdamount() = " + m_arrBatchEntryLines.get(iLineNumber - 1).getsbdamount());
        			}
        			if (sFieldName.compareToIgnoreCase(SMTablegltransactionbatchlines.bddebitamt) == 0){
        				arrEstimateLines.get(iLineNumber - 1).setsdebitamt(sParamValue);
        			}
           			if (sFieldName.compareToIgnoreCase(SMTablegltransactionbatchlines.dattransactiondate) == 0){
        				arrEstimateLines.get(iLineNumber - 1).setstransactiondate(sParamValue);
        			}
        			if (sFieldName.compareToIgnoreCase(SMTablegltransactionbatchlines.lid) == 0){
        				arrEstimateLines.get(iLineNumber - 1).setslid(sParamValue);
        			}
        			if (sFieldName.compareToIgnoreCase(SMTablegltransactionbatchlines.llinenumber) == 0){
        				arrEstimateLines.get(iLineNumber - 1).setslinenumber(sParamValue);
        			}
        			if (sFieldName.compareToIgnoreCase(SMTablegltransactionbatchlines.sacctid) == 0){
        				arrEstimateLines.get(iLineNumber - 1).setsacctid(sParamValue);
        			}
        			if (sFieldName.compareToIgnoreCase(SMTablegltransactionbatchlines.scomment) == 0){
        				arrEstimateLines.get(iLineNumber - 1).setscomment(sParamValue);
        			}
        			if (sFieldName.compareToIgnoreCase(SMTablegltransactionbatchlines.sdescription) == 0){
        				arrEstimateLines.get(iLineNumber - 1).setsdescription(sParamValue);
        			}
        			if (sFieldName.compareToIgnoreCase(SMTablegltransactionbatchlines.sreference) == 0){
        				arrEstimateLines.get(iLineNumber - 1).setsreference(sParamValue);
        			}
        			if (sFieldName.compareToIgnoreCase(GLEditEntryEdit.PARAM_SOURCE_LEDGER_AND_TYPE) == 0){
		    			String sSourceLedgerAndType[] = 
		    					sParamValue.replace("&quot;", "\"").split(GLSourceLedgers.SOURCE_LEDGER_AND_TYPE_DELIMITER);
	    				try {
	    					arrEstimateLines.get(iLineNumber - 1).setssourceledger(sSourceLedgerAndType[0]);
	    				} catch (Exception e) {
	    					arrEstimateLines.get(iLineNumber - 1).setssourceledger("");
	    				}
	    				try {
	    					arrEstimateLines.get(iLineNumber - 1).setssourcetype(sSourceLedgerAndType[1]);
	    				} catch (Exception e) {
	    					arrEstimateLines.get(iLineNumber - 1).setssourcetype("");
	    				}
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
    			//|| (newline.getssourceledger().compareToIgnoreCase("") != 0)
    			//|| (newline.getssourcetype().compareToIgnoreCase("") != 0)
    			//|| (newline.getstransactiondate().compareToIgnoreCase("") != 0)
    		){
    			addLine(newline);
    		}
    	}
    	//Make sure we set the batch number and entry number:
    	for (int i = 0; i < arrEstimateLines.size(); i++){
    		arrEstimateLines.get(i).setsbatchnumber(getsbatchnumber());
    		arrEstimateLines.get(i).setsentrynumber(getsentrynumber());
    	}
	}
	*/
	
	/*
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
			+ ", " + SMTablegltransactionbatchentries.iclosingentry
			+ ", " + SMTablegltransactionbatchentries.ifiscalperiod
			+ ", " + SMTablegltransactionbatchentries.ifiscalyear
			+ ", " + SMTablegltransactionbatchentries.lbatchnumber
			+ ", " + SMTablegltransactionbatchentries.lentrynumber
			+ ", " + SMTablegltransactionbatchentries.llastline
			+ ", " + SMTablegltransactionbatchentries.ssourceledgertransactionid
			+ ", " + SMTablegltransactionbatchentries.sentrydescription
			+ ", " + SMTablegltransactionbatchentries.ssourceledger
			+ ")"
			+ " VALUES ("
			+ "'" + getsdatdocdateInSQLFormat() + "'"
			+ ", '" + getsentrydateInSQLFormat() + "'"
			+ ", " + getsautoreverse()
			+ ", " + getsclosingentry()
			+ ", " + getsfiscalperiod()
			+ ", " + getsfiscalyear()
			+ ", " + getsbatchnumber()
			+ ", " + getsentrynumber()
			+ ", " + getslastline()
			+ ", '" + clsDatabaseFunctions.FormatSQLStatement(getssourceledgertransactionlink()) + "'"
			+ ", '" + clsDatabaseFunctions.FormatSQLStatement(getsentrydescription()) + "'"
			+ ", '" + clsDatabaseFunctions.FormatSQLStatement(getssourceledger()) + "'"
			+ ")"
			
			+ " ON DUPLICATE KEY UPDATE"
			+ " " + SMTablegltransactionbatchentries.datdocdate + " = '" + getsdatdocdateInSQLFormat() + "'"
			+ ", " + SMTablegltransactionbatchentries.datentrydate + " = '" + getsentrydateInSQLFormat() + "'"
			+ ", " + SMTablegltransactionbatchentries.iautoreverse + " = " + getsautoreverse()
			+ ", " + SMTablegltransactionbatchentries.iclosingentry + " = " + getsclosingentry()
			+ ", " + SMTablegltransactionbatchentries.ifiscalperiod + " = " + getsfiscalperiod()
			+ ", " + SMTablegltransactionbatchentries.ifiscalyear + " = " + getsfiscalyear()
			+ ", " + SMTablegltransactionbatchentries.lbatchnumber + " = " + getsbatchnumber()
			+ ", " + SMTablegltransactionbatchentries.lentrynumber + " = " + getsentrynumber()
			+ ", " + SMTablegltransactionbatchentries.llastline + " = " + getslastline()
			+ ", " + SMTablegltransactionbatchentries.ssourceledgertransactionid + " = '" + clsDatabaseFunctions.FormatSQLStatement(getssourceledgertransactionlink()) + "'"
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
	*/
	
	/*
	public void validate_fields(Connection conn, String sUserID, boolean bBatchIsBeingPosted) throws Exception{
		
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


		try {
			m_slastline = clsValidateFormFields.validateLongIntegerField(
				Integer.toString(arrEstimateLines.size()), 
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
		
		boolean bFiscalPeriodIsValidInt = true;
		
		try {
			m_sfiscalperiod = clsValidateFormFields.validateLongIntegerField(
					m_sfiscalperiod, 
				"Fiscal period", 
				1L, 
				15L);
		} catch (Exception e) {
			bFiscalPeriodIsValidInt = false;
			sResult += "  " + e.getMessage() + ".";
		}
		
		if (bFiscalPeriodIsValidInt){
			int iFiscalPeriod = Integer.parseInt(m_sfiscalperiod);
			GLFiscalYear period = new GLFiscalYear();
			period.set_sifiscalyear(m_sfiscalyear);
			try {
				period.load(conn);
			} catch (Exception e1) {
				sResult += "  " + "Could not load fiscal year " + m_sfiscalyear + ".";
			}
			switch(iFiscalPeriod){
				case 1:
					if (period.get_siperiod1locked().compareToIgnoreCase("1") == 0){
						sResult += "  " + "Fiscal year " + period.get_sifiscalyear() + ", period 1 is locked.";
					}
					break;
				case 2:
					if (period.get_siperiod2locked().compareToIgnoreCase("1") == 0){
						sResult += "  " + "Fiscal year " + period.get_sifiscalyear() + ", period 2 is locked.";
					}
					break;
				case 3:
					if (period.get_siperiod3locked().compareToIgnoreCase("1") == 0){
						sResult += "  " + "Fiscal year " + period.get_sifiscalyear() + ", period 3 is locked.";
					}
					break;
				case 4:
					if (period.get_siperiod4locked().compareToIgnoreCase("1") == 0){
						sResult += "  " + "Fiscal year " + period.get_sifiscalyear() + ", period 4 is locked.";
					}
					break;
				case 5:
					if (period.get_siperiod5locked().compareToIgnoreCase("1") == 0){
						sResult += "  " + "Fiscal year " + period.get_sifiscalyear() + ", period 5 is locked.";
					}
					break;
				case 6:
					if (period.get_siperiod6locked().compareToIgnoreCase("1") == 0){
						sResult += "  " + "Fiscal year " + period.get_sifiscalyear() + ", period 6 is locked.";
					}
					break;
				case 7:
					if (period.get_siperiod7locked().compareToIgnoreCase("1") == 0){
						sResult += "  " + "Fiscal year " + period.get_sifiscalyear() + ", period 7 is locked.";
					}
					break;
				case 8:
					if (period.get_siperiod8locked().compareToIgnoreCase("1") == 0){
						sResult += "  " + "Fiscal year " + period.get_sifiscalyear() + ", period 8 is locked.";
					}
					break;
				case 9:
					if (period.get_siperiod9locked().compareToIgnoreCase("1") == 0){
						sResult += "  " + "Fiscal year " + period.get_sifiscalyear() + ", period 9 is locked.";
					}
					break;
				case 10:
					if (period.get_siperiod10locked().compareToIgnoreCase("1") == 0){
						sResult += "  " + "Fiscal year " + period.get_sifiscalyear() + ", period 10 is locked.";
					}
					break;
				case 11:
					if (period.get_siperiod11locked().compareToIgnoreCase("1") == 0){
						sResult += "  " + "Fiscal year " + period.get_sifiscalyear() + ", period 11 is locked.";
					}
					break;
				case 12:
					if (period.get_siperiod12locked().compareToIgnoreCase("1") == 0){
						sResult += "  " + "Fiscal year " + period.get_sifiscalyear() + ", period 12 is locked.";
					}
					break;
				case 13:
					if (period.get_siperiod13locked().compareToIgnoreCase("1") == 0){
						sResult += "  " + "Fiscal year " + period.get_sifiscalyear() + ", period 13 is locked.";
					}
					break;

				case 15:
					//Period 15 is used for the closing entries - this period should always be allowed...
					break;
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
		
		if (m_sourceledgertransactionlink.compareToIgnoreCase("") == 0){
			m_sourceledgertransactionlink = "0";
		}
		//If this is a journal entry, then the source ledger transaction ID is just the batch/entry number:
		if (getssourceledger().compareToIgnoreCase(GLSourceLedgers.getSourceLedgerDescription(GLSourceLedgers.SOURCE_LEDGER_GL)) == 0){
			m_sourceledgertransactionlink = getsbatchnumber() + "," + getsentrynumber();
		}
		try {
			m_sourceledgertransactionlink = clsValidateFormFields.validateStringField(
					m_sourceledgertransactionlink, 
					24, 
					"Source Transaction Ledger ID ", 
					false);
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
		
		try {
			m_siclosingentry = clsValidateFormFields.validateIntegerField(
				m_siclosingentry, 
				"Closing entry", 
				0, 
				1);
		} catch (Exception e) {
			sResult += "  " + e.getMessage() + ".";
		}
		
		//Validate the lines:
		for (int i = 0; i < arrEstimateLines.size(); i++){
			GLTransactionBatchLine line = arrEstimateLines.get(i);
			line.setsentrynumber(getsentrynumber());
			line.setslinenumber(Integer.toString(i + 1));
			
			//IF there's no line description, use the Entry description:
			if (line.getsdescription().compareToIgnoreCase("") == 0){
				line.setsdescription(getsentrydescription());
			}
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
*/
	
	/*
	public void entryIsInBalance(Connection conn) throws Exception{
		BigDecimal bdLineTotalDebitAmount = new BigDecimal("0.00");
		BigDecimal bdLineTotalCreditAmount = new BigDecimal("0.00");
		
		for (int i = 0; i < arrEstimateLines.size(); i++){
			GLTransactionBatchLine line = arrEstimateLines.get(i);
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
*/
	
	/*
	 * public void load(ServletContext context, String sDBID, String sUserID) throws
	 * Exception{
	 * 
	 * Connection conn; try { conn =
	 * clsDatabaseFunctions.getConnectionWithException( context, sDBID, "MySQL",
	 * SMUtilities.getFullClassName(this.toString()) + ".load - user: " + sUserID);
	 * } catch (Exception e) { throw new
	 * Exception("Error [1555337049] getting connection - " + e.getMessage()); }
	 * 
	 * try { load(conn); } catch (Exception e) { throw new
	 * Exception("Error [1555337050] loading - " + e.getMessage()); }
	 * clsDatabaseFunctions.freeConnection(context, conn, "[1555337051]"); }
	 */
	
	/*
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
				setssourceledgertransactionlink(rs.getString(SMTablegltransactionbatchentries.ssourceledgertransactionid));
				setssourceledger(rs.getString(SMTablegltransactionbatchentries.ssourceledger));
				setsautoreverse(Integer.toString(rs.getInt(SMTablegltransactionbatchentries.iautoreverse)));
				setsclosingentry(Integer.toString(rs.getInt(SMTablegltransactionbatchentries.iclosingentry)));
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
		arrEstimateLines.clear();
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
	*/
	
	/*
	public void loadExternalCompanyEntry(Connection conn, String sExternalDBName) throws Exception{
		
		String SQL = "";
		if (
			(getslid().compareToIgnoreCase("") != 0)
			&& (getslid().compareToIgnoreCase("-1") != 0)
			&& (getslid().compareToIgnoreCase("0") != 0)	
		){
			SQL = "SELECT * FROM " + sExternalDBName + "." + SMTablegltransactionbatchentries.TableName
				+ " WHERE ("
					+ "(" + SMTablegltransactionbatchentries.lid + " = " + getslid() + ")"
				+ ")"
			;
		}else{
			if (
				(getsbatchnumber().compareToIgnoreCase("") != 0)
				&& (getsentrynumber().compareToIgnoreCase("") != 0)
			){
			SQL = "SELECT * FROM " + sExternalDBName + "." + SMTablegltransactionbatchentries.TableName
					+ " WHERE ("
						+ "(" + SMTablegltransactionbatchentries.lbatchnumber + " = " + getsbatchnumber() + ")"
						+ " AND (" + SMTablegltransactionbatchentries.lentrynumber + " = " + getsentrynumber() + ")"
					+ ")"
				;
			}
		}
		
		if (SQL.compareToIgnoreCase("") == 0){
			throw new Exception("Error [1555337487] - can't load external company batch entry without an ID or batch and entry number.");
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
				setssourceledgertransactionlink(rs.getString(SMTablegltransactionbatchentries.ssourceledgertransactionid));
				setssourceledger(rs.getString(SMTablegltransactionbatchentries.ssourceledger));
				setsautoreverse(Integer.toString(rs.getInt(SMTablegltransactionbatchentries.iautoreverse)));
				setsclosingentry(Integer.toString(rs.getInt(SMTablegltransactionbatchentries.iclosingentry)));
			}else{
				rs.close();
				throw new Exception("Error [1555337958] - No external company GL transaction batch entry found with lid = " + getslid() + ".");
			}
			rs.close();
		} catch (Exception e) {
			rs.close();
			throw new Exception("Error [1555337959] - loading external company " + OBJECT_NAME + " with ID " + getslid() + " - " + e.getMessage());
		}
		
		//Load the lines:
		arrEstimateLines.clear();
		SQL = "SELECT"
			+ " " + SMTablegltransactionbatchlines.lid
			+ " FROM " + sExternalDBName + "." + SMTablegltransactionbatchlines.TableName 
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
				line.loadExternalCompanyLine(conn, Long.toString(rs.getLong(SMTablegltransactionbatchlines.lid)), sExternalDBName);
				addLine(line);
			}
			rs.close();
		} catch (Exception e) {
			rs.close();
			throw new Exception("Error [1555337960] loading batch entry lines - " + e.getMessage());
		}
	}
	*/
	
	/*
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
	public String getssourceledgertransactionlink(){
		return m_sourceledgertransactionlink;
	}
	public void setssourceledgertransactionlink(String ssourceledgertransactionlink){
		m_sourceledgertransactionlink = ssourceledgertransactionlink;
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
	
	public String getsclosingentry(){
		return m_siclosingentry;
	}
	public void setsclosingentry(String sclosingentry){
		m_siclosingentry = sclosingentry;
	}
	
	public BigDecimal getDebitTotal () throws Exception{
		BigDecimal bdDebitTotal = new BigDecimal("0.00");
		
		for (int i = 0; i < arrEstimateLines.size(); i++){
			bdDebitTotal = bdDebitTotal.add(new BigDecimal (arrEstimateLines.get(i).getsdebitamt().replaceAll(",", "")));
		}
		
		return bdDebitTotal;
	}
	public BigDecimal getCreditTotal () throws Exception{
		BigDecimal bdCreditTotal = new BigDecimal("0.00");
		
		for (int i = 0; i < arrEstimateLines.size(); i++){
			bdCreditTotal = bdCreditTotal.add(new BigDecimal (arrEstimateLines.get(i).getscreditamt().replaceAll(",", "")));
		}
		
		return bdCreditTotal;
	}
	*/
	
	/*
	public SMEstimate copyEntry(){
		SMEstimate newentry = new SMEstimate();
		newentry.setsautoreverse(getsautoreverse());
		newentry.setsclosingentry(getsclosingentry());
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
		newentry.setssourceledgertransactionlink(getssourceledgertransactionlink());
		
		for (int i = 0; i < arrEstimateLines.size(); i++){
			newentry.addLine(arrEstimateLines.get(i));
		}
		
		return newentry;
		
	}
*/
	public void addLine(SMEstimateLine line){
		arrEstimateLines.add(line);
	}

	public ArrayList<SMEstimateLine> getLineArray(){
		return arrEstimateLines;
	}
	
	/*
	private void saveLines(Connection conn, String sUser, boolean bBatchIsBeingPosted) throws Exception{
		for (int i = 0; i < arrEstimateLines.size(); i++){
			GLTransactionBatchLine line = arrEstimateLines.get(i);
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
				+ " AND (" + SMTablegltransactionbatchlines.llinenumber + " > " + Integer.toString(arrEstimateLines.size()) + ")"
			+ ")"
		;
		try {
			Statement stmt = conn.createStatement();
			stmt.execute(SQL);
		} catch (Exception e) {
			throw new Exception("Error [1555338303] deleting leftover entry lines - " + e.getMessage());
		}
		
	}
	*/
	public void removeLineByLineNumber(String sLineNumber) throws Exception{
    	//System.out.println("[1489528400] - sEntryNumber = " + sEntryNumber);
		
		boolean bLineNumberWasFound = false;
		for (int i = 0; i < arrEstimateLines.size(); i++){
			if (arrEstimateLines.get(i).getslestimatelinenumber().compareToIgnoreCase(sLineNumber) == 0){
				arrEstimateLines.remove(i);
				bLineNumberWasFound = true;
			}
		}
   	
    	if (!bLineNumberWasFound){
    		throw new Exception("Line number '" + sLineNumber + "' was not found in the entry.");
    	}
	}

	/*
	public String getQueryString(){
		//Particular to the specific class
		String sQueryString = "";
		sQueryString += SMTablegltransactionbatchentries.datdocdate + "=" + clsServletUtilities.URLEncode(getsdatdocdate());
		sQueryString += "&" + SMTablegltransactionbatchentries.datentrydate + "=" + clsServletUtilities.URLEncode(getsdatentrydate());
		sQueryString += "&" + SMTablegltransactionbatchentries.iautoreverse + "=" + clsServletUtilities.URLEncode(getsautoreverse());
		sQueryString += "&" + SMTablegltransactionbatchentries.iclosingentry + "=" + clsServletUtilities.URLEncode(getsclosingentry());
		sQueryString += "&" + SMTablegltransactionbatchentries.ifiscalperiod + "=" + clsServletUtilities.URLEncode(getsfiscalperiod());
		sQueryString += "&" + SMTablegltransactionbatchentries.ifiscalyear + "=" + clsServletUtilities.URLEncode(getsfiscalyear());
		sQueryString += "&" + SMTablegltransactionbatchentries.lbatchnumber + "=" + clsServletUtilities.URLEncode(getsbatchnumber());
		sQueryString += "&" + SMTablegltransactionbatchentries.lentrynumber + "=" + clsServletUtilities.URLEncode(getsentrynumber());
		sQueryString += "&" + SMTablegltransactionbatchentries.lid + "=" + clsServletUtilities.URLEncode(getslid());
		sQueryString += "&" + SMTablegltransactionbatchentries.llastline + "=" + clsServletUtilities.URLEncode(getslastline());
		sQueryString += "&" + SMTablegltransactionbatchentries.ssourceledgertransactionid + "=" + clsServletUtilities.URLEncode(getssourceledgertransactionlink());
		sQueryString += "&" + SMTablegltransactionbatchentries.sentrydescription + "=" + clsServletUtilities.URLEncode(getsentrydescription());
		sQueryString += "&" + SMTablegltransactionbatchentries.ssourceledger + "=" + clsServletUtilities.URLEncode(getssourceledger());
		return sQueryString;
	}
	*/
	
	/*
	public String dumpData(){
		String s = "";
		s += "  Doc date: " + getsdatdocdate() + "\n";
		s += "  Entry date: " + getsdatentrydate() + "\n";
		s += "  Auto reverse:" + getsautoreverse() + "\n";
		s += "  Closing entry:" + getsclosingentry() + "\n";
		s += "  Batch: " + getsbatchnumber() + "\n";
		s += "  Entry: " + getsentrynumber() + "\n";
		s += "  Fiscal period: " + getsfiscalperiod() + "\n";
		s += "  Fiscal year: " + getsfiscalyear() + "\n";
		s += "  Entry ID: " + getslid() + "\n";
		s += "  Last line: " + getslastline() + "\n";
		s += "  Source ledger transaction line ID: " + getssourceledgertransactionlink() + "\n";
		s += "  Desc: " + getsentrydescription() + "\n";
		s += "  Source ledger: " + getssourceledger() + "\n";
		
		s += "  -- Number of lines: " + arrEstimateLines.size() + "\n";
		
		for (int i = 0; i < arrEstimateLines.size(); i++){
			s += "  LINE " + (i + 1) + ":\n";
			s += arrEstimateLines.get(i).dumpData();
		}
		
		return s;
	}
	*/
	
	/*
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
		m_sourceledgertransactionlink = "0";
		m_ssourceledger = "";
		m_autoreverse = "0";
		m_siclosingentry = "0";
		arrEstimateLines = new ArrayList<GLTransactionBatchLine>(0);
	}
	*/
}
