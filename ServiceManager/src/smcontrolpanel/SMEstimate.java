package smcontrolpanel;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Enumeration;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import SMDataDefinition.SMTablesmestimatelines;
import SMDataDefinition.SMTablesmestimates;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsDateAndTimeConversions;
import ServletUtilities.clsManageBigDecimals;
import ServletUtilities.clsManageRequestParameters;
import ServletUtilities.clsServletUtilities;
import ServletUtilities.clsValidateFormFields;

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
	private String m_datetimelastmodified;
	private String m_slastmodifiedbyfullname;
	
	private ArrayList<SMEstimateLine>arrEstimateLines;

	public static final int LINE_NUMBER_PADDING_LENGTH = 6;
	public static final String LINE_NUMBER_PARAMETER = "LINENOPARAM";

	public SMEstimate() 
	{
		initializeVariables();
	}
	public SMEstimate(HttpServletRequest req){
		//Read the batch fields from a servlet request:
		initializeVariables();
		
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

		m_datetimecreated = clsManageRequestParameters.get_Request_Parameter(SMTablesmestimates.datetimecreated, req).replace("&quot;", "\"");
		if(m_datetimecreated.compareToIgnoreCase("") == 0){
			m_datetimecreated = SMUtilities.EMPTY_DATETIME_VALUE;
		}
		
		m_screatedbyfullname = clsManageRequestParameters.get_Request_Parameter(SMTablesmestimates.screatedbyfullname, req).replace("&quot;", "\"");
		m_llastmodifiedbyid = clsManageRequestParameters.get_Request_Parameter(SMTablesmestimates.llastmodifiedbyid, req).replace("&quot;", "\"");
		if(m_llastmodifiedbyid.compareToIgnoreCase("") == 0){
			m_llastmodifiedbyid = "-1";
		}
		
		m_datetimelastmodified = clsManageRequestParameters.get_Request_Parameter(SMTablesmestimates.datetimelastmodified, req).replace("&quot;", "\"");
		if(m_datetimelastmodified.compareToIgnoreCase("") == 0){
			m_datetimelastmodified = SMUtilities.EMPTY_DATETIME_VALUE;
		}
		
		m_slastmodifiedbyfullname = clsManageRequestParameters.get_Request_Parameter(SMTablesmestimates.slastmodifiedbyfullname, req).replace("&quot;", "\"");
		
		readEntryLines(req);
	}

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
    		//System.out.println("[1490711988] sLineParam = '" + sLineParam +"'");
    		//If it contains a line number parameter, then it's an GLTransactionBatchLine field:
    		if (sLineParam.startsWith(SMEstimate.LINE_NUMBER_PARAMETER)){
    			//System.out.println("[1490711988] sLineParam = '" + sLineParam +"'");
    			sLineNumber = sLineParam.substring(
    				SMEstimate.LINE_NUMBER_PARAMETER.length(),
    				SMEstimate.LINE_NUMBER_PARAMETER.length() + SMEstimate.LINE_NUMBER_PADDING_LENGTH);
    			iLineNumber = Integer.parseInt(sLineNumber);
    			//System.out.println("[1490711989] sLineNumber = '" + sLineNumber +"'");
    			sFieldName = sLineParam.substring(SMEstimate.LINE_NUMBER_PARAMETER.length() + SMEstimate.LINE_NUMBER_PADDING_LENGTH);
    			//System.out.println("[1490711990] sFieldName = '" + sFieldName +"'");
    			sParamValue = clsManageRequestParameters.get_Request_Parameter(sLineParam, request).trim();
    			//System.out.println("[1490711991] sParamValue = '" + sParamValue +"'");
    			//If the line array needs another row to fit all the line numbers, add it now:
				while (arrEstimateLines.size() < iLineNumber){
					SMEstimateLine line = new SMEstimateLine();
					arrEstimateLines.add(line);
				}
				
				//If any of the line fields have a '0' for their line number, then that means the user is adding a new field:
    			if (iLineNumber == 0){
    				bAddingNewLine = true;

    				//Now update the new line, and we'll add it to the entry down below:
        			if (sFieldName.compareToIgnoreCase(SMTablesmestimatelines.bdextendedcost) == 0){
        				newline.setsbdextendedcost(sParamValue.replace(",", "").trim());
        			}
        			if (sFieldName.compareToIgnoreCase(SMTablesmestimatelines.bdquantity) == 0){
        				newline.setsbdquantity(sParamValue.replace(",", "").trim());
        			}
        			if (sFieldName.compareToIgnoreCase(SMTablesmestimatelines.lestimatelid) == 0){
        				newline.setslestimateid(sParamValue.trim());
        			}
        			if (sFieldName.compareToIgnoreCase(SMTablesmestimatelines.lestimatelinenumber) == 0){
        				newline.setslestimatelinenumber(sParamValue.trim());
        			}
        			if (sFieldName.compareToIgnoreCase(SMTablesmestimatelines.lid) == 0){
        				newline.setslid(sParamValue.trim());
        			}
        			if (sFieldName.compareToIgnoreCase(SMTablesmestimatelines.lsummarylid) == 0){
        				newline.setslsummaryid(sParamValue.trim());
        			}
        			if (sFieldName.compareToIgnoreCase(SMTablesmestimatelines.sitemnumber) == 0){
        				newline.setsitemnumber(sParamValue.trim());
        			}
        			if (sFieldName.compareToIgnoreCase(SMTablesmestimatelines.slinedescription) == 0){
        				newline.setslinedescription(sParamValue.trim());
        			}
        			if (sFieldName.compareToIgnoreCase(SMTablesmestimatelines.sunitofmeasure) == 0){
        				newline.setsunitofmeasure(sParamValue.trim());
        			}
    			}else{
        			//Now update the field on the line we're reading:
        			if (sFieldName.compareToIgnoreCase(SMTablesmestimatelines.bdextendedcost) == 0){
        				arrEstimateLines.get(iLineNumber - 1).setsbdextendedcost(sParamValue.replace(",", "").trim());
        				//System.out.println("[1511887996] - sParamValue = '" + sParamValue + "', m_arrBatchEntryLines.get(iLineNumber - 1).getsbdamount() = " + m_arrBatchEntryLines.get(iLineNumber - 1).getsbdamount());
        			}
        			if (sFieldName.compareToIgnoreCase(SMTablesmestimatelines.bdquantity) == 0){
        				arrEstimateLines.get(iLineNumber - 1).setsbdquantity(sParamValue.replace(",", "").trim());
        			}
           			if (sFieldName.compareToIgnoreCase(SMTablesmestimatelines.lestimatelid) == 0){
        				arrEstimateLines.get(iLineNumber - 1).setslestimateid(sParamValue.trim());
        			}
        			if (sFieldName.compareToIgnoreCase(SMTablesmestimatelines.lestimatelinenumber) == 0){
        				arrEstimateLines.get(iLineNumber - 1).setslestimatelinenumber(sParamValue.trim());
        			}
        			if (sFieldName.compareToIgnoreCase(SMTablesmestimatelines.lid) == 0){
        				arrEstimateLines.get(iLineNumber - 1).setslid(sParamValue.trim());
        			}
        			if (sFieldName.compareToIgnoreCase(SMTablesmestimatelines.lsummarylid) == 0){
        				arrEstimateLines.get(iLineNumber - 1).setslsummaryid(sParamValue.trim());
        			}
        			if (sFieldName.compareToIgnoreCase(SMTablesmestimatelines.sitemnumber) == 0){
        				arrEstimateLines.get(iLineNumber - 1).setsitemnumber(sParamValue.trim());
        			}
        			if (sFieldName.compareToIgnoreCase(SMTablesmestimatelines.slinedescription) == 0){
        				arrEstimateLines.get(iLineNumber - 1).setslinedescription(sParamValue.trim());
        			}
        			if (sFieldName.compareToIgnoreCase(SMTablesmestimatelines.sunitofmeasure) == 0){
        				arrEstimateLines.get(iLineNumber - 1).setsunitofmeasure(sParamValue.trim());
        			}
    			}
    		}
    	}

    	//If the user was adding a new line, then....
    	if (bAddingNewLine){
    		//Just add that line to the entry:
    		//If the user has actually added anything to the new line:
    		if (
    			(newline.getsbdextendedcost().compareToIgnoreCase("") != 0)
    			|| (newline.getsbdquantity().compareToIgnoreCase("") != 0)
    			|| (newline.getsitemnumber().compareToIgnoreCase("0.00") != 0)
    			|| (newline.getslinedescription().compareToIgnoreCase("0.00") != 0)
    			|| (newline.getsunitofmeasure().compareToIgnoreCase("") != 0)
    		){
    			addLine(newline);
    		}
    	}
    	//Make sure we set the summary and estimate IDs:
    	for (int i = 0; i < arrEstimateLines.size(); i++){
    		arrEstimateLines.get(i).setslestimateid(m_lid);
    		arrEstimateLines.get(i).setslsummaryid(m_lsummarylid);
    	}
	}
	
	public void save_without_data_transaction (Connection conn, String sUserID, String sUserFullName) throws Exception{

		//long lStarttime = System.currentTimeMillis();
		
		try {
			validate_fields(conn, sUserID);
		} catch (Exception e1) {
			throw new Exception(e1.getMessage());
		}
		
		//System.out.println("[1543341150] - elapsed time 10 = " + (System.currentTimeMillis() - lStarttime) + " ms");
		
		String SQL = "";
		SQL = "INSERT into " + SMTablesmestimates.TableName
			+ " (" 
			+ SMTablesmestimates.bdadditionalposttaxcostamount
			+ ", " + SMTablesmestimates.bdadditionalpretaxcostamount
			+ ", " + SMTablesmestimates.bdextendedcost
			+ ", " + SMTablesmestimates.bdfreight
			+ ", " + SMTablesmestimates.bdlaborcostperunit
			+ ", " + SMTablesmestimates.bdlaborquantity
			+ ", " + SMTablesmestimates.bdlaborsellpriceperunit
			+ ", " + SMTablesmestimates.bdmarkupamount
			+ ", " + SMTablesmestimates.bdquantity
			+ ", " + SMTablesmestimates.datetimecreated
			+ ", " + SMTablesmestimates.datetimelastmodified
			+ ", " + SMTablesmestimates.ivendorquotelinenumber
			+ ", " + SMTablesmestimates.lcreatedbyid
			+ ", " + SMTablesmestimates.llastmodifiedbyid
			+ ", " + SMTablesmestimates.lsummarylid
			+ ", " + SMTablesmestimates.lsummarylinenumber
			+ ", " + SMTablesmestimates.sadditionalposttaxcostlabel
			+ ", " + SMTablesmestimates.sadditionalpretaxcostlabel
			+ ", " + SMTablesmestimates.screatedbyfullname
			+ ", " + SMTablesmestimates.sdescription
			+ ", " + SMTablesmestimates.sitemnumber
			+ ", " + SMTablesmestimates.slastmodifiedbyfullname
			+ ", " + SMTablesmestimates.sprefixlabelitem
			+ ", " + SMTablesmestimates.sproductdescription
			+ ", " + SMTablesmestimates.sunitofmeasure
			+ ", " + SMTablesmestimates.svendorquotenumber
			+ ")"
			+ " VALUES ("
			+ m_bdadditionalposttaxcostamount.replace(",", "")
			+ ", " + m_bdadditionalpretaxcostamount.replace(",", "")
			+ ", " + m_bdextendedcost.replace(",", "")
			+ ", " + m_bdfreight.replace(",", "")
			+ ", " + m_bdlaborcostperunit.replace(",", "")
			+ ", " + m_bdlaborquantity.replace(",", "")
			+ ", " + m_bdlaborsellpriceperunit.replace(",", "")
			+ ", " + m_bdmarkupamount.replace(",", "")
			+ ", " + m_bdquantity.replace(",", "")
			+ ", '" + getsdatetimecreatedInSQLFormat() + "'"
			+ ", '" + getsdatetimelastmodifiedbyInSQLFormat() + "'"
			+ ", " + m_ivendorquotelinenumber
			+ ", " + sUserID
			+ ", " + sUserID
			+ ", " + this.m_lsummarylid
			+ ", " + this.m_lsummarylinenumber
			+ ", '" + clsDatabaseFunctions.FormatSQLStatement(m_sadditionalposttaxcostlabel) + "'"
			+ ", '" + clsDatabaseFunctions.FormatSQLStatement(m_sadditionalpretaxcostlabel) + "'"
			+ ", '" + clsDatabaseFunctions.FormatSQLStatement(sUserFullName) + "'"
			+ ", '" + clsDatabaseFunctions.FormatSQLStatement(m_sdescription) + "'"
			+ ", '" + clsDatabaseFunctions.FormatSQLStatement(m_sitemnumber) + "'"
			+ ", '" + clsDatabaseFunctions.FormatSQLStatement(sUserFullName) + "'"
			+ ", '" + clsDatabaseFunctions.FormatSQLStatement(m_sprefixlabelitem) + "'"
			+ ", '" + clsDatabaseFunctions.FormatSQLStatement(m_sproductdescription) + "'"
			+ ", '" + clsDatabaseFunctions.FormatSQLStatement(m_sunitofmeasure) + "'"
			+ ", '" + clsDatabaseFunctions.FormatSQLStatement(m_svendorquotenumber) + "'"
					
			+ " ON DUPLICATE KEY UPDATE"
			+ " " + SMTablesmestimates.bdadditionalposttaxcostamount + " = " + m_bdadditionalposttaxcostamount.replace(",", "")
			+ ", " + SMTablesmestimates.bdadditionalpretaxcostamount + " = " + m_bdadditionalpretaxcostamount.replace(",", "")
			+ ", " + SMTablesmestimates.bdextendedcost + " = " + m_bdextendedcost.replace(",", "")
			+ ", " + SMTablesmestimates.bdfreight + " = " + m_bdfreight.replace(",", "")
			+ ", " + SMTablesmestimates.bdlaborcostperunit + " = " + m_bdlaborcostperunit.replace(",", "")
			+ ", " + SMTablesmestimates.bdlaborquantity + " = " + m_bdfreight.replace(",", "")
			+ ", " + SMTablesmestimates.bdlaborsellpriceperunit + " = " + m_bdlaborsellpriceperunit.replace(",", "")
			+ ", " + SMTablesmestimates.bdmarkupamount + " = " + m_bdmarkupamount.replace(",", "")
			+ ", " + SMTablesmestimates.bdquantity + " = " + m_bdquantity.replace(",", "")
			
			+ ", " + SMTablesmestimates.datetimecreated + " = '" + getsdatetimecreatedInSQLFormat() + "'"
			+ ", " + SMTablesmestimates.datetimelastmodified + " = '" + getsdatetimelastmodifiedbyInSQLFormat() + "'"
			
			+ ", " + SMTablesmestimates.ivendorquotelinenumber + " = " + m_ivendorquotelinenumber
			+ ", " + SMTablesmestimates.llastmodifiedbyid + " = " + sUserID
			+ ", " + SMTablesmestimates.lsummarylid + " = " + m_lsummarylid
			+ ", " + SMTablesmestimates.lsummarylinenumber + " = " + m_lsummarylinenumber
			
			+ ", " + SMTablesmestimates.sadditionalposttaxcostlabel + " = '" + clsDatabaseFunctions.FormatSQLStatement(m_sadditionalposttaxcostlabel) + "'"
			+ ", " + SMTablesmestimates.sadditionalpretaxcostlabel + " = '" + clsDatabaseFunctions.FormatSQLStatement(m_sadditionalpretaxcostlabel) + "'"
			+ ", " + SMTablesmestimates.sdescription + " = '" + clsDatabaseFunctions.FormatSQLStatement(m_sdescription) + "'"
			+ ", " + SMTablesmestimates.sitemnumber + " = '" + clsDatabaseFunctions.FormatSQLStatement(m_sitemnumber) + "'"
			+ ", " + SMTablesmestimates.slastmodifiedbyfullname + " = '" + clsDatabaseFunctions.FormatSQLStatement(sUserFullName) + "'"
			+ ", " + SMTablesmestimates.sprefixlabelitem + " = '" + clsDatabaseFunctions.FormatSQLStatement(m_sprefixlabelitem) + "'"
			+ ", " + SMTablesmestimates.sproductdescription + " = '" + clsDatabaseFunctions.FormatSQLStatement(m_sproductdescription) + "'"
			+ ", " + SMTablesmestimates.sunitofmeasure + " = '" + clsDatabaseFunctions.FormatSQLStatement(m_sunitofmeasure) + "'"
			+ ", " + SMTablesmestimates.svendorquotenumber + " = '" + clsDatabaseFunctions.FormatSQLStatement(m_svendorquotenumber) + "'"
																					
		;
		
		//System.out.println("[1494260859] - SQL = '" + SQL + "'");
		
		Statement stmt = conn.createStatement();
		try {
			stmt.execute(SQL);
		} catch (Exception e) {
			throw new Exception("Error [1590163606] updating Estimate with SQL: '" + SQL + "' - " + e.getMessage());
		}
		
		//Update the ID:
		String sSQL = "SELECT last_insert_id()";
		try {
			ResultSet rs = clsDatabaseFunctions.openResultSet(sSQL, conn);
			if (rs.next()) {
				m_lid = Long.toString(rs.getLong(1));
			}else {
				m_lid = "0";
			}
			rs.close();
		} catch (SQLException e) {
			throw new Exception("Error [1590163604] Could not get last ID number - " + e.getMessage());
		}
		//If something went wrong, we can't get the last ID:
		if (m_lid.compareToIgnoreCase("0") == 0){
			throw new Exception("Error [1590163605] Could not get last ID number.");
		}
		
		//Finally, save the lines....
		try {
			saveLines(conn, sUserID);
		} catch (Exception e) {
			throw new Exception("Error [1590163607] saving entry lines - " + e.getMessage() + ".");
		}
		
		return;
	}
	
	public void validate_fields(Connection conn, String sUserID) throws Exception{
		
		String sResult = "";
		
		try {
			m_lid  = clsValidateFormFields.validateLongIntegerField(m_lid, "Estimate ID", -1L, clsValidateFormFields.MAX_LONG_VALUE);
		} catch (Exception e) {
			sResult += "  " + e.getMessage() + ".";
		}
		
		try {
			m_lsummarylid  = clsValidateFormFields.validateLongIntegerField(m_lid, "Estimate Summary ID", 1L, clsValidateFormFields.MAX_LONG_VALUE);
		} catch (Exception e) {
			sResult += "  " + e.getMessage() + ".";
		}

		try {
			m_lsummarylinenumber  = clsValidateFormFields.validateLongIntegerField(m_lid, "Estimate Summary line number", 1L, clsValidateFormFields.MAX_LONG_VALUE);
		} catch (Exception e) {
			sResult += "  " + e.getMessage() + ".";
		}
		
		try {
			m_sdescription = clsValidateFormFields.validateStringField(
				m_sdescription, 
				SMTablesmestimates.sdescriptionLength, 
				"Estimate description", 
				true
			);
		} catch (Exception e) {
			sResult += "  " + e.getMessage() + ".";
		}

		try {
			m_sprefixlabelitem = clsValidateFormFields.validateStringField(
					m_sprefixlabelitem, 
				SMTablesmestimates.sprefixlabelitemLength, 
				"Prefix item label", 
				true
			);
		} catch (Exception e) {
			sResult += "  " + e.getMessage() + ".";
		}
		
		try {
			m_svendorquotenumber = clsValidateFormFields.validateStringField(
				m_svendorquotenumber, 
				SMTablesmestimates.svendorquotenumberLength, 
				"Vendor quote number", 
				true
			);
		} catch (Exception e) {
			sResult += "  " + e.getMessage() + ".";
		}
		
		try {
			m_ivendorquotelinenumber = clsValidateFormFields.validateIntegerField(
				m_ivendorquotelinenumber, 
				"Vendor quote line number", 
				1, 
				clsValidateFormFields.MAX_INT_VALUE
			);
		} catch (Exception e) {
			sResult += "  " + e.getMessage() + ".";
		}
		
		try {
			m_bdquantity = clsValidateFormFields.validateBigdecimalField(
				m_bdquantity, 
				"Quantity", 
				SMTablesmestimates.bdquantityScale, 
				new BigDecimal("0.0001"), 
				new BigDecimal("999999999.9999")
			);
		} catch (Exception e) {
			sResult += "  " + e.getMessage() + ".";
		}
		
		try {
			m_sitemnumber = clsValidateFormFields.validateStringField(
				m_sitemnumber, 
				SMTablesmestimates.sitemnumberLength, 
				"Item number", 
				true
			);
		} catch (Exception e) {
			sResult += "  " + e.getMessage() + ".";
		}
		
		try {
			m_sproductdescription = clsValidateFormFields.validateStringField(
				m_sproductdescription, 
				SMTablesmestimates.sproductdescriptionLength, 
				"Product description", 
				true
			);
		} catch (Exception e) {
			sResult += "  " + e.getMessage() + ".";
		}
		
		try {
			m_sunitofmeasure = clsValidateFormFields.validateStringField(
				m_sunitofmeasure, 
				SMTablesmestimates.sunitofmeasureLength, 
				"Unit of measure", 
				true
			);
		} catch (Exception e) {
			sResult += "  " + e.getMessage() + ".";
		}

		try {
			m_bdextendedcost = clsValidateFormFields.validateBigdecimalField(
					m_bdextendedcost, 
				"Extended cost", 
				SMTablesmestimates.bdextendedcostScale, 
				new BigDecimal("0.01"), 
				new BigDecimal("999999999.99")
			);
		} catch (Exception e) {
			sResult += "  " + e.getMessage() + ".";
		}

		try {
			m_bdfreight = clsValidateFormFields.validateBigdecimalField(
				m_bdfreight, 
				"Freight", 
				SMTablesmestimates.bdfreightScale, 
				new BigDecimal("0.00"), 
				new BigDecimal("999999999.99")
			);
		} catch (Exception e) {
			sResult += "  " + e.getMessage() + ".";
		}

		try {
			m_bdlaborquantity = clsValidateFormFields.validateBigdecimalField(
				m_bdlaborquantity, 
				"Labor quantity", 
				SMTablesmestimates.bdlaborquantityScale, 
				new BigDecimal("0.0000"), 
				new BigDecimal("999999999.9999")
			);
		} catch (Exception e) {
			sResult += "  " + e.getMessage() + ".";
		}
		
		try {
			m_bdlaborcostperunit = clsValidateFormFields.validateBigdecimalField(
				m_bdlaborcostperunit, 
				"Labor cost per unit", 
				SMTablesmestimates.bdlaborcostperunitScale, 
				new BigDecimal("0.00"), 
				new BigDecimal("999999999.99")
			);
		} catch (Exception e) {
			sResult += "  " + e.getMessage() + ".";
		}
		
		try {
			m_sadditionalpretaxcostlabel = clsValidateFormFields.validateStringField(
				m_sadditionalpretaxcostlabel, 
				SMTablesmestimates.sadditionalpretaxcostlabelLength, 
				"Additional pre tax cost label", 
				true
			);
		} catch (Exception e) {
			sResult += "  " + e.getMessage() + ".";
		}
		
		try {
			m_bdadditionalpretaxcostamount = clsValidateFormFields.validateBigdecimalField(
					m_bdadditionalpretaxcostamount, 
				"Additional pre tax cost amount", 
				SMTablesmestimates.bdadditionalpretaxcostamountScale, 
				new BigDecimal("0.00"), 
				new BigDecimal("999999999.99")
			);
		} catch (Exception e) {
			sResult += "  " + e.getMessage() + ".";
		}

		try {
			m_bdmarkupamount = clsValidateFormFields.validateBigdecimalField(
				m_bdmarkupamount, 
				"Mark-up amount", 
				SMTablesmestimates.bdmarkupamountScale, 
				new BigDecimal("0.00"), 
				new BigDecimal("999999999.99")
			);
		} catch (Exception e) {
			sResult += "  " + e.getMessage() + ".";
		}
		
		try {
			m_sadditionalposttaxcostlabel = clsValidateFormFields.validateStringField(
				m_sadditionalposttaxcostlabel, 
				SMTablesmestimates.sadditionalposttaxcostlabelLength, 
				"Additional post tax cost label", 
				true
			);
		} catch (Exception e) {
			sResult += "  " + e.getMessage() + ".";
		}
		
		try {
			m_bdadditionalposttaxcostamount = clsValidateFormFields.validateBigdecimalField(
				m_bdadditionalposttaxcostamount, 
				"Additional post tax cost amount", 
				SMTablesmestimates.bdadditionalposttaxcostamountScale, 
				new BigDecimal("0.00"), 
				new BigDecimal("999999999.99")
			);
		} catch (Exception e) {
			sResult += "  " + e.getMessage() + ".";
		}
		
		try {
			m_bdlaborsellpriceperunit = clsValidateFormFields.validateBigdecimalField(
				m_bdlaborsellpriceperunit, 
				"Labor sell prioce per unit", 
				SMTablesmestimates.bdlaborsellpriceperunitScale, 
				new BigDecimal("0.00"), 
				new BigDecimal("999999999.99")
			);
		} catch (Exception e) {
			sResult += "  " + e.getMessage() + ".";
		}
		
		try {
			m_lcreatedbyid  = clsValidateFormFields.validateLongIntegerField(m_lcreatedbyid, "Created by user ID", 1L, clsValidateFormFields.MAX_LONG_VALUE);
		} catch (Exception e) {
			sResult += "  " + e.getMessage() + ".";
		}
		
		try {
			m_datetimecreated = clsValidateFormFields.validateDateTimeField(
				m_datetimecreated, 
				"Date created", 
				SMUtilities.DATETIME_FORMAT_FOR_DISPLAY,
				true);
		} catch (Exception e) {
			sResult += "  " + e.getMessage() + ".";
		}
		
		try {
			m_screatedbyfullname = clsValidateFormFields.validateStringField(
				m_screatedbyfullname, 
				SMTablesmestimates.screatedbyfullnameLength, 
				"Created by full name", 
				true
			);
		} catch (Exception e) {
			sResult += "  " + e.getMessage() + ".";
		}
		
		try {
			m_llastmodifiedbyid  = clsValidateFormFields.validateLongIntegerField(m_llastmodifiedbyid, "Last modified by user ID", -1L, clsValidateFormFields.MAX_LONG_VALUE);
		} catch (Exception e) {
			sResult += "  " + e.getMessage() + ".";
		}
		
		try {
			m_datetimelastmodified = clsValidateFormFields.validateDateTimeField(
				m_datetimelastmodified, 
				"Date last modified", 
				SMUtilities.DATETIME_FORMAT_FOR_DISPLAY,
				true);
		} catch (Exception e) {
			sResult += "  " + e.getMessage() + ".";
		}
		
		try {
			m_slastmodifiedbyfullname = clsValidateFormFields.validateStringField(
				m_slastmodifiedbyfullname, 
				SMTablesmestimates.slastmodifiedbyfullnameLength, 
				"Last modified by full name", 
				true
			);
		} catch (Exception e) {
			sResult += "  " + e.getMessage() + ".";
		}
		
		//Validate the lines:
		for (int i = 0; i < arrEstimateLines.size(); i++){
			SMEstimateLine line = arrEstimateLines.get(i);
			line.setslestimateid(m_lid);
			line.setslsummaryid(m_lsummarylid);
			line.setslestimatelinenumber((Integer.toString(i + 1)));
			
			try {
				line.validate_fields(conn);
			} catch (Exception e) {
				sResult += "  In line " + line.getslestimatelinenumber() + " - " + e.getMessage() + ".";
			}
		}
		
		if (sResult.compareToIgnoreCase("") != 0){
			throw new Exception(sResult);
		}
		
		return;
	}
	
	
	public void load(ServletContext context, String sDBID, String sUserID) throws Exception{

		Connection conn; try { conn =
				clsDatabaseFunctions.getConnectionWithException( context, sDBID, "MySQL",
						SMUtilities.getFullClassName(this.toString()) + ".load - user: " + sUserID);
		} catch (Exception e) { throw new
			Exception("Error [1590167714] getting connection - " + e.getMessage()); }

		try { 
			load(conn); 
		} catch (Exception e){ 
			clsDatabaseFunctions.freeConnection(context, conn, "[1590167716]");
			throw new Exception("Error [1590167715] loading - " + e.getMessage()); 
		}
		
		clsDatabaseFunctions.freeConnection(context, conn, "[1590167716]");
	}
	
	public void load(Connection conn) throws Exception{
		
		String SQL = "";
		if (
			(m_lid.compareToIgnoreCase("") != 0)
			&& (m_lid.compareToIgnoreCase("-1") != 0)
			&& (m_lid.compareToIgnoreCase("0") != 0)	
		){
			SQL = "SELECT * FROM " + SMTablesmestimates.TableName
				+ " WHERE ("
					+ "(" + SMTablesmestimates.lid + " = " + m_lid + ")"
				+ ")"
			;
		}else{
			if (
				(m_lsummarylid.compareToIgnoreCase("") != 0)
				&& (m_lsummarylinenumber.compareToIgnoreCase("") != 0)
			){
			SQL = "SELECT * FROM " + SMTablesmestimates.TableName
					+ " WHERE ("
						+ "(" + SMTablesmestimates.lsummarylid + " = " + m_lsummarylid + ")"
						+ " AND (" + SMTablesmestimates.lsummarylinenumber + " = " + m_lsummarylinenumber + ")"
					+ ")"
				;
			}
		}
		
		if (SQL.compareToIgnoreCase("") == 0){
			throw new Exception("Error [1590168080] - can't load Estimate without an ID or Summary number and line.");
		}
		
		ResultSet rs = null;
		try {
			rs = clsDatabaseFunctions.openResultSet(SQL, conn);
			if (rs.next()){
				m_lid = (Long.toString(rs.getLong(SMTablesmestimates.lid)));
				m_lsummarylid = (Long.toString(rs.getLong(SMTablesmestimates.lsummarylid)));
				m_lsummarylinenumber = (Long.toString(rs.getLong(SMTablesmestimates.lsummarylinenumber)));
				m_sdescription = rs.getString(SMTablesmestimates.sdescription);
				m_sprefixlabelitem = rs.getString(SMTablesmestimates.sprefixlabelitem);
				m_svendorquotenumber = rs.getString(SMTablesmestimates.svendorquotenumber);
				m_ivendorquotelinenumber = (Integer.toString(rs.getInt(SMTablesmestimates.ivendorquotelinenumber)));
				m_bdquantity = clsManageBigDecimals.BigDecimalToScaledFormattedString(SMTablesmestimates.bdquantityScale, rs.getBigDecimal(SMTablesmestimates.bdquantity));
				m_sitemnumber = rs.getString(SMTablesmestimates.sitemnumber);
				m_sproductdescription = rs.getString(SMTablesmestimates.sproductdescription);
				m_sunitofmeasure = rs.getString(SMTablesmestimates.sunitofmeasure);
				m_bdextendedcost = clsManageBigDecimals.BigDecimalToScaledFormattedString(SMTablesmestimates.bdextendedcostScale, rs.getBigDecimal(SMTablesmestimates.bdextendedcost));
				m_bdfreight = clsManageBigDecimals.BigDecimalToScaledFormattedString(SMTablesmestimates.bdfreightScale, rs.getBigDecimal(SMTablesmestimates.bdfreight));
				m_bdlaborquantity = clsManageBigDecimals.BigDecimalToScaledFormattedString(SMTablesmestimates.bdlaborquantityScale, rs.getBigDecimal(SMTablesmestimates.bdlaborquantity));
				m_bdlaborcostperunit = clsManageBigDecimals.BigDecimalToScaledFormattedString(SMTablesmestimates.bdlaborcostperunitScale, rs.getBigDecimal(SMTablesmestimates.bdlaborcostperunit));
				m_sadditionalpretaxcostlabel = rs.getString(SMTablesmestimates.sadditionalpretaxcostlabel);
				m_bdadditionalpretaxcostamount = clsManageBigDecimals.BigDecimalToScaledFormattedString(SMTablesmestimates.bdadditionalpretaxcostamountScale, rs.getBigDecimal(SMTablesmestimates.bdadditionalpretaxcostamount));
				m_bdmarkupamount = clsManageBigDecimals.BigDecimalToScaledFormattedString(SMTablesmestimates.bdmarkupamountScale, rs.getBigDecimal(SMTablesmestimates.bdmarkupamount));
				m_sadditionalposttaxcostlabel = rs.getString(SMTablesmestimates.sadditionalposttaxcostlabel);
				m_bdadditionalposttaxcostamount = clsManageBigDecimals.BigDecimalToScaledFormattedString(SMTablesmestimates.bdadditionalposttaxcostamountScale, rs.getBigDecimal(SMTablesmestimates.bdadditionalposttaxcostamount));
				m_bdlaborsellpriceperunit = clsManageBigDecimals.BigDecimalToScaledFormattedString(SMTablesmestimates.bdlaborsellpriceperunitScale, rs.getBigDecimal(SMTablesmestimates.bdlaborsellpriceperunit));
				m_lcreatedbyid = (Long.toString(rs.getLong(SMTablesmestimates.lcreatedbyid)));
				m_datetimecreated = clsDateAndTimeConversions.resultsetDateTimeStringToFormattedString(
					rs.getString(SMTablesmestimates.datetimecreated), SMUtilities.DATETIME_FORMAT_FOR_DISPLAY, SMUtilities.EMPTY_DATETIME_VALUE);
				m_sadditionalposttaxcostlabel = rs.getString(SMTablesmestimates.sadditionalposttaxcostlabel);	
				m_screatedbyfullname = rs.getString(SMTablesmestimates.screatedbyfullname);
				m_llastmodifiedbyid = (Long.toString(rs.getLong(SMTablesmestimates.llastmodifiedbyid)));
				m_datetimelastmodified = clsDateAndTimeConversions.resultsetDateTimeStringToFormattedString(
					rs.getString(SMTablesmestimates.datetimelastmodified), SMUtilities.DATETIME_FORMAT_FOR_DISPLAY, SMUtilities.EMPTY_DATETIME_VALUE);
				m_slastmodifiedbyfullname = rs.getString(SMTablesmestimates.slastmodifiedbyfullname);
			}else{
				rs.close();
				throw new Exception("Error [1590169269] - No Estimate found with lid = " + m_lid + ".");
			}
			rs.close();
		} catch (Exception e) {
			rs.close();
			throw new Exception("Error [1590169270] - loading " + OBJECT_NAME + " with ID " + m_lid + " - " + e.getMessage());
		}
		
		//Load the lines:
		arrEstimateLines.clear();
		SQL = "SELECT"
			+ " " + SMTablesmestimatelines.lid
			+ " FROM " + SMTablesmestimatelines.TableName 
			+ " WHERE ("
				+ "(" + SMTablesmestimatelines.lestimatelid + " = " + m_lid + ")"
			+ ") ORDER BY " + SMTablesmestimatelines.lestimatelinenumber
		;
		rs = null;
		try {
			rs = clsDatabaseFunctions.openResultSet(SQL, conn);
			while (rs.next()){
				SMEstimateLine line = new SMEstimateLine();
				line.load(conn, Long.toString(rs.getLong(SMTablesmestimatelines.lid)));
				addLine(line);
			}
			rs.close();
		} catch (Exception e) {
			rs.close();
			throw new Exception("Error [1590169271] loading Estimate lines - " + e.getMessage());
		}
	}
	
	public String getsdatetimecreatedInSQLFormat() throws Exception{
		if (this.m_datetimecreated.compareToIgnoreCase("") == 0){
			return SMUtilities.EMPTY_DATETIME_VALUE;
		}else{
			return clsDateAndTimeConversions.convertDateFormat(m_datetimecreated, SMUtilities.DATETIME_FORMAT_FOR_DISPLAY, SMUtilities.DATETIME_FORMAT_FOR_SQL, SMUtilities.EMPTY_SQL_DATETIME_VALUE);
		}
	}
	public String getsdatetimelastmodifiedbyInSQLFormat() throws Exception{
		if (this.m_datetimelastmodified.compareToIgnoreCase("") == 0){
			return SMUtilities.EMPTY_DATETIME_VALUE;
		}else{
			return clsDateAndTimeConversions.convertDateFormat(m_datetimelastmodified, SMUtilities.DATETIME_FORMAT_FOR_DISPLAY, SMUtilities.DATETIME_FORMAT_FOR_SQL, SMUtilities.EMPTY_SQL_DATETIME_VALUE);
		}
	}
	
	public String getslid(){
		return m_lid;
	}
	public void setslid(String slid){
		m_lid = slid;
	}

	public String getslsummarylid(){
		return m_lsummarylid;
	}
	public void setslsummarylid(String ssummarylid){
		m_lsummarylid = ssummarylid;
	}
	
	public String getslsummarylinenumber(){
		return m_lsummarylinenumber;
	}
	public void setslsummarylinenumber(String ssummarylinenumber){
		m_lsummarylid = ssummarylinenumber;
	}
	
	public String getsdescription(){
		return m_sdescription;
	}
	public void setsdescription(String sdescription){
		m_sdescription = sdescription;
	}
	
	public String getsprefixlabelitem(){
		return m_sprefixlabelitem;
	}
	public void setsprefixlabelitem(String sprefixlabelitem){
		m_sprefixlabelitem = sprefixlabelitem;
	}
	
	public String getsvendorquotenumber(){
		return m_svendorquotenumber;
	}
	public void setsvendorquotenumber(String svendorquotenumber){
		m_svendorquotenumber = svendorquotenumber;
	}
	
	public String getsivendorquotelinenumber(){
		return m_ivendorquotelinenumber;
	}
	public void setsivendorquotelinenumber(String sivendorquotelinenumber){
		m_ivendorquotelinenumber = sivendorquotelinenumber;
	}
	
	public String getsbdquantity(){
		return m_bdquantity;
	}
	public void setsbdquantity(String sbdquantity){
		m_bdquantity = sbdquantity;
	}
	
	public String getsitemnumber(){
		return m_sitemnumber;
	}
	public void setsitemnumber(String sitemnumber){
		m_sitemnumber = sitemnumber;
	}
	
	public String getsproductdescription(){
		return m_sproductdescription;
	}
	public void setsproductdescription(String sproductdescription){
		m_sproductdescription = sproductdescription;
	}
	
	public String getsunitofmeasure(){
		return m_sunitofmeasure;
	}
	public void setsunitofmeasure(String sunitofmeasure){
		m_sunitofmeasure = sunitofmeasure;
	}
	
	public String getsbdextendedcost(){
		return m_bdextendedcost;
	}
	public void setsbdextendedcost(String sbdextendedcost){
		m_bdextendedcost = sbdextendedcost;
	}
	
	public String getsbdfreight(){
		return m_bdfreight;
	}
	public void setsbdfreight(String sbdfreight){
		m_bdfreight = sbdfreight;
	}
	
	public String getsbdlaborquantity(){
		return m_bdlaborquantity;
	}
	public void setsbdlaborquantity(String sbdlaborquantity){
		m_bdlaborquantity = sbdlaborquantity;
	}
	
	public String getsbdlaborcostperunit(){
		return m_bdlaborcostperunit;
	}
	public void setsbdlaborcostperunit(String sbdlaborcostperunit){
		m_bdlaborcostperunit = sbdlaborcostperunit;
	}
	
	public String getsadditionalpretaxcostlabel(){
		return m_sadditionalpretaxcostlabel;
	}
	public void setsadditionalpretaxcostlabel(String sadditionalpretaxcostlabel){
		m_sadditionalpretaxcostlabel = sadditionalpretaxcostlabel;
	}
	
	public String getsbdadditionalpretaxcostamount(){
		return m_bdadditionalpretaxcostamount;
	}
	public void setsbdadditionalpretaxcostamount(String sbdadditionalpretaxcostamount){
		m_bdadditionalpretaxcostamount = sbdadditionalpretaxcostamount;
	}
	
	public String getsbdmarkupamount(){
		return m_bdmarkupamount;
	}
	public void setsbdbdmarkupamount(String sbdmarkupamount){
		m_bdmarkupamount = sbdmarkupamount;
	}
	
	public String getsadditionalposttaxcostlabel(){
		return m_sadditionalposttaxcostlabel;
	}
	public void setsadditionalposttaxcostlabel(String sadditionalposttaxcostlabel){
		m_sadditionalposttaxcostlabel = sadditionalposttaxcostlabel;
	}
	
	public String getsbdadditionalposttaxcostamount(){
		return m_bdadditionalposttaxcostamount;
	}
	public void setsbdadditionalposttaxcostamount(String sbdadditionalposttaxcostamount){
		m_bdadditionalposttaxcostamount = sbdadditionalposttaxcostamount;
	}
	
	public String getsbdlaborsellpriceperunit(){
		return m_bdlaborsellpriceperunit;
	}
	public void setsbdlaborsellpriceperunit(String sbdlaborsellpriceperunit){
		m_bdlaborsellpriceperunit = sbdlaborsellpriceperunit;
	}
	
	public String getslcreatedbyid(){
		return m_lcreatedbyid;
	}
	public void setslcreatedbyid(String slcreatedbyid){
		m_lcreatedbyid = slcreatedbyid;
	}
	
	public String getsdatetimecreated(){
		return m_datetimecreated;
	}
	public void setsdatetimecreated(String sdatetimecreated){
		m_datetimecreated = sdatetimecreated;
	}
	
	public String getscreatedbyfullname(){
		return m_screatedbyfullname;
	}
	public void setscreatedbyfullname(String screatedbyfullname){
		m_screatedbyfullname = screatedbyfullname;
	}
	
	public String getsllastmodifiedbyid(){
		return m_llastmodifiedbyid;
	}
	public void setsllastmodifiedbyid(String sllastmodifiedbyid){
		m_llastmodifiedbyid = sllastmodifiedbyid;
	}
	
	public String getsdatetimelastmodified(){
		return m_datetimelastmodified;
	}
	public void setsdatetimelastmodified(String sdatetimelastmodified){
		m_datetimelastmodified = sdatetimelastmodified;
	}
	
	public String getslastmodifiedbyfullname(){
		return m_slastmodifiedbyfullname;
	}
	public void setslastmodifiedbyfullname(String slastmodifiedbyfullname){
		m_slastmodifiedbyfullname = slastmodifiedbyfullname;
	}
	
	public void addLine(SMEstimateLine line){
		arrEstimateLines.add(line);
	}

	public ArrayList<SMEstimateLine> getLineArray(){
		return arrEstimateLines;
	}
	
	private void saveLines(Connection conn, String sUser) throws Exception{
		for (int i = 0; i < arrEstimateLines.size(); i++){
			SMEstimateLine line = arrEstimateLines.get(i);
			line.setslestimateid(m_lid);
			line.setslsummaryid(m_lsummarylid);
			try {
				line.save_without_data_transaction(conn, sUser);
			} catch (Exception e) {
				throw new Exception("Error [1590170725] saving line number " + line.getslestimatelinenumber() 
					+ " on Estimate " + m_lid + " - " + e.getMessage()
				);
			}
		}
		//We also have to delete any EXTRA lines that might be left that are higher than our current highest line number:
		//This can happen if we removed a line and we now have fewer lines than we previously had:
		String SQL = "DELETE FROM " + SMTablesmestimatelines.TableName
			+ " WHERE ("
				+ "(" + SMTablesmestimatelines.lestimatelid + " = " + m_lid + ")"
				+ " AND (" + SMTablesmestimatelines.lestimatelinenumber + " > " + Integer.toString(arrEstimateLines.size()) + ")"
			+ ")"
		;
		try {
			Statement stmt = conn.createStatement();
			stmt.execute(SQL);
		} catch (Exception e) {
			throw new Exception("Error [1590170726] deleting leftover Estimate lines - " + e.getMessage());
		}
		
	}

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
    		throw new Exception("Error [1590170726] line number '" + sLineNumber + "' was not found in the Estimate.");
    	}
	}

	public String getQueryString(){
		//Particular to the specific class
		String sQueryString = "";
		
		sQueryString += SMTablesmestimates.bdadditionalposttaxcostamount + "=" + clsServletUtilities.URLEncode(getsbdadditionalposttaxcostamount());
		sQueryString += "&" + SMTablesmestimates.bdadditionalpretaxcostamount + "=" + clsServletUtilities.URLEncode(getsbdadditionalpretaxcostamount());
		sQueryString += "&" + SMTablesmestimates.bdextendedcost + "=" + clsServletUtilities.URLEncode(getsbdextendedcost());
		sQueryString += "&" + SMTablesmestimates.bdfreight + "=" + clsServletUtilities.URLEncode(getsbdfreight());
		sQueryString += "&" + SMTablesmestimates.bdlaborcostperunit + "=" + clsServletUtilities.URLEncode(getsbdlaborcostperunit());
		sQueryString += "&" + SMTablesmestimates.bdlaborquantity + "=" + clsServletUtilities.URLEncode(getsbdlaborquantity());
		sQueryString += "&" + SMTablesmestimates.bdlaborsellpriceperunit + "=" + clsServletUtilities.URLEncode(getsbdlaborsellpriceperunit());
		sQueryString += "&" + SMTablesmestimates.bdmarkupamount + "=" + clsServletUtilities.URLEncode(getsbdmarkupamount());
		sQueryString += "&" + SMTablesmestimates.bdquantity + "=" + clsServletUtilities.URLEncode(getsbdquantity());
		sQueryString += "&" + SMTablesmestimates.datetimecreated + "=" + clsServletUtilities.URLEncode(getsdatetimecreated());
		sQueryString += "&" + SMTablesmestimates.datetimelastmodified + "=" + clsServletUtilities.URLEncode(getsdatetimelastmodified());
		sQueryString += "&" + SMTablesmestimates.ivendorquotelinenumber + "=" + clsServletUtilities.URLEncode(getsivendorquotelinenumber());
		sQueryString += "&" + SMTablesmestimates.lcreatedbyid + "=" + clsServletUtilities.URLEncode(getslcreatedbyid());
		sQueryString += "&" + SMTablesmestimates.lid + "=" + clsServletUtilities.URLEncode(getslid());
		sQueryString += "&" + SMTablesmestimates.llastmodifiedbyid + "=" + clsServletUtilities.URLEncode(getsllastmodifiedbyid());
		sQueryString += "&" + SMTablesmestimates.lsummarylid + "=" + clsServletUtilities.URLEncode(getslsummarylid());
		sQueryString += "&" + SMTablesmestimates.lsummarylinenumber + "=" + clsServletUtilities.URLEncode(getslsummarylinenumber());
		sQueryString += "&" + SMTablesmestimates.sadditionalposttaxcostlabel + "=" + clsServletUtilities.URLEncode(getsadditionalposttaxcostlabel());
		sQueryString += "&" + SMTablesmestimates.sadditionalpretaxcostlabel + "=" + clsServletUtilities.URLEncode(getsadditionalpretaxcostlabel());
		sQueryString += "&" + SMTablesmestimates.screatedbyfullname + "=" + clsServletUtilities.URLEncode(getscreatedbyfullname());
		sQueryString += "&" + SMTablesmestimates.sdescription + "=" + clsServletUtilities.URLEncode(getsdescription());
		sQueryString += "&" + SMTablesmestimates.sitemnumber + "=" + clsServletUtilities.URLEncode(getsitemnumber());
		sQueryString += "&" + SMTablesmestimates.slastmodifiedbyfullname + "=" + clsServletUtilities.URLEncode(getslastmodifiedbyfullname());
		sQueryString += "&" + SMTablesmestimates.sprefixlabelitem + "=" + clsServletUtilities.URLEncode(getsprefixlabelitem());
		sQueryString += "&" + SMTablesmestimates.sproductdescription + "=" + clsServletUtilities.URLEncode(getsproductdescription());
		sQueryString += "&" + SMTablesmestimates.sunitofmeasure + "=" + clsServletUtilities.URLEncode(getsunitofmeasure());
		sQueryString += "&" + SMTablesmestimates.svendorquotenumber + "=" + clsServletUtilities.URLEncode(getsvendorquotenumber());
		
		return sQueryString;
	}
	
	public String dumpData(){
		String s = "";
		
		s += "  lid: " +  getslid() + "\n";
		s += "  summary lid: " +  getslsummarylid() + "\n";
		s += "  summary line number: " +  getslsummarylinenumber() + "\n";
		s += "  description: " +  getsdescription() + "\n";
		s += "  prefix label item: " +  getsprefixlabelitem() + "\n";
		s += "  vendor quote number: " +  getsvendorquotenumber() + "\n";
		s += "  vendor quote line number: " +  getsivendorquotelinenumber() + "\n";
		s += "  quantity: " +  getsbdquantity() + "\n";
		s += "  item number: " +  getsitemnumber() + "\n";
		s += "  product description: " +  getsproductdescription() + "\n";
		s += "  unit of measure: " +  getsunitofmeasure() + "\n";
		s += "  extended cost: " +  getsbdextendedcost() + "\n";
		s += "  freight: " +  getsbdfreight() + "\n";
		s += "  labor quantity: " +  getsbdlaborquantity() + "\n";
		s += "  labor cost per unit: " +  getsbdlaborcostperunit() + "\n";
		s += "  additional pre tax cost label: " +  getsadditionalpretaxcostlabel() + "\n";
		s += "  additional pre tax cost: " +  getsbdadditionalpretaxcostamount() + "\n";
		s += "  Mark-up amount: " +  getsbdmarkupamount() + "\n";
		s += "  additional post tax cost label: " +  getsadditionalposttaxcostlabel() + "\n";
		s += "  additional post tax cost: " +  getsbdadditionalposttaxcostamount() + "\n";
		s += "  labor sell price per unit: " +  getsbdlaborsellpriceperunit() + "\n";
		s += "  createdby user ID: " +  getslcreatedbyid() + "\n";
		s += "  date created: " +  getsdatetimecreated() + "\n";
		s += "  created by full name: " +  getscreatedbyfullname() + "\n";
		s += "  last modified by user ID: " +  getsllastmodifiedbyid() + "\n";
		s += "  date last modified: " +  getsdatetimelastmodified() + "\n";
		s += "  modified by full name: " +  getslastmodifiedbyfullname() + "\n";
		
		s += "  -- Number of lines: " + arrEstimateLines.size() + "\n";
		
		for (int i = 0; i < arrEstimateLines.size(); i++){
			s += "  LINE " + (i + 1) + ":\n";
			s += arrEstimateLines.get(i).dumpData();
		}
		
		return s;
	}
	
	private void initializeVariables(){
		
		m_lid = "-1";
		m_lsummarylid = "-1";
		m_lsummarylinenumber = "0";
		m_sdescription = "";
		m_sprefixlabelitem = "";
		m_svendorquotenumber = "";
		m_ivendorquotelinenumber = "0";
		m_bdquantity = "0.0000";
		m_sitemnumber = "";
		m_sproductdescription = "";
		m_sunitofmeasure = "";
		m_bdextendedcost = "0.00";
		m_bdfreight = "0.00";
		m_bdlaborquantity = "0.0000";
		m_bdlaborcostperunit = "0.00";
		m_sadditionalpretaxcostlabel = "";
		m_bdadditionalpretaxcostamount = "";
		m_bdmarkupamount = "0.00";
		m_sadditionalposttaxcostlabel = "";
		m_bdadditionalposttaxcostamount = "0.00";
		m_bdlaborsellpriceperunit = "0.00";
		m_lcreatedbyid = "0";
		m_datetimecreated = SMUtilities.EMPTY_DATETIME_VALUE;
		m_screatedbyfullname = "";
		m_llastmodifiedbyid = "0";
		m_datetimelastmodified = SMUtilities.EMPTY_DATETIME_VALUE;
		m_slastmodifiedbyfullname = "";
		arrEstimateLines = new ArrayList<SMEstimateLine>(0);
	}
}
