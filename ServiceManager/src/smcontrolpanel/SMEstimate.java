package smcontrolpanel;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Enumeration;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import SMClasses.SMOHDirectQuoteLineList;
import SMClasses.SMOHDirectQuoteList;
import SMClasses.SMTax;
import SMDataDefinition.SMOHDirectFieldDefinitions;
import SMDataDefinition.SMTablesmestimatelines;
import SMDataDefinition.SMTablesmestimates;
import SMDataDefinition.SMTablesmestimatesummaries;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsDateAndTimeConversions;
import ServletUtilities.clsManageBigDecimals;
import ServletUtilities.clsManageRequestParameters;
import ServletUtilities.clsServletUtilities;
import ServletUtilities.clsValidateFormFields;
import smic.ICItem;

public class SMEstimate {

	public static final String OBJECT_NAME = "Estimate";
	
	private String m_lid;
	private String m_lsummarylid;
	private String m_lsummarylinenumber;
	private String m_sdescription;
	private String m_sprefixlabelitem;
	private String m_svendorquoteid;
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
	private String m_sadditionalpostsalestaxcostlabel;
	private String m_bdadditionalpostsalestaxcostamt;
	
	private SMEstimateSummary m_estimatesummary;
	
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
		m_svendorquoteid = clsManageRequestParameters.get_Request_Parameter(SMTablesmestimates.svendorquoteid, req).replace("&quot;", "\"");
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
		
		m_sadditionalpostsalestaxcostlabel = clsManageRequestParameters.get_Request_Parameter(SMTablesmestimates.sadditionalpostsalestaxcostlabel, req).replace("&quot;", "\"");
		m_bdadditionalpostsalestaxcostamt = clsManageRequestParameters.get_Request_Parameter(SMTablesmestimates.bdadditionalpostsalestaxcostamt, req).replace("&quot;", "\"");
		
		readEstimateLines(req);
	}

	private void readEstimateLines(HttpServletRequest request){
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
    		//System.out.println("[1490712988] sLineParam = '" + sLineParam +"'");
    		//If it contains a line number parameter, then it's an GLTransactionBatchLine field:
    		if (sLineParam.startsWith(SMEditSMEstimateEdit.ESTIMATE_LINE_PREFIX)){
    			//System.out.println("[1490712188] sLineParam = '" + sLineParam +"'");
    			sLineNumber = sLineParam.substring(
    				SMEditSMEstimateEdit.ESTIMATE_LINE_PREFIX.length(),
    				SMEditSMEstimateEdit.ESTIMATE_LINE_PREFIX.length() + SMEstimate.LINE_NUMBER_PADDING_LENGTH);
    			iLineNumber = Integer.parseInt(sLineNumber);
    			//System.out.println("[1490712989] sLineNumber = '" + sLineNumber +"'");
    			sFieldName = sLineParam.substring(SMEditSMEstimateEdit.ESTIMATE_LINE_PREFIX.length() + SMEstimate.LINE_NUMBER_PADDING_LENGTH);
    			//System.out.println("[1490712990] sFieldName = '" + sFieldName +"'");
    			sParamValue = clsManageRequestParameters.get_Request_Parameter(sLineParam, request).trim();
    			//System.out.println("[1490712991] sParamValue = '" + sParamValue +"'");
    			//If the line array needs another row to fit all the line numbers, add it now:
				while (arrEstimateLines.size() < iLineNumber){
					SMEstimateLine line = new SMEstimateLine();
					arrEstimateLines.add(line);
				}
				
				//If any of the line fields have a '0' for their line number, then that means the user is adding a new field:
    			if (iLineNumber == 0){
    				bAddingNewLine = true;
    				//System.out.println("[202006085409] - adding new line");
    				//Now update the new line, and we'll add it to the entry down below:
        			if (sFieldName.compareToIgnoreCase(SMTablesmestimatelines.bdextendedcost) == 0){
        				newline.setsbdextendedcost(sParamValue.replace(",", "").trim());
        			}
        			if (sFieldName.compareToIgnoreCase(SMTablesmestimatelines.bdunitcost) == 0){
        				newline.setsbdunitcost(sParamValue.replace(",", "").trim());
        			}
        			if (sFieldName.compareToIgnoreCase(SMTablesmestimatelines.bdquantity) == 0){
        				newline.setsbdquantity(sParamValue.replace(",", "").trim());
        			}
        			if (sFieldName.compareToIgnoreCase(SMTablesmestimatelines.bdunitsellprice) == 0){
        				newline.setsbdunitsellprice(sParamValue.replace(",", "").trim());
        			}
        			if (sFieldName.compareToIgnoreCase(SMTablesmestimatelines.bdextendedsellprice) == 0){
        				newline.setsbdextendedsellprice(sParamValue.replace(",", "").trim());
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
        				//System.out.println("[1511882996] - sParamValue = '" + sParamValue + "'.");
        			}
        			if (sFieldName.compareToIgnoreCase(SMTablesmestimatelines.bdunitcost) == 0){
        				arrEstimateLines.get(iLineNumber - 1).setsbdunitcost(sParamValue.replace(",", "").trim());
        			}
        			if (sFieldName.compareToIgnoreCase(SMTablesmestimatelines.bdquantity) == 0){
        				arrEstimateLines.get(iLineNumber - 1).setsbdquantity(sParamValue.replace(",", "").trim());
        			}
        			if (sFieldName.compareToIgnoreCase(SMTablesmestimatelines.bdunitsellprice) == 0){
        				arrEstimateLines.get(iLineNumber - 1).setsbdunitsellprice(sParamValue.replace(",", "").trim());
        			}
        			if (sFieldName.compareToIgnoreCase(SMTablesmestimatelines.bdextendedsellprice) == 0){
        				arrEstimateLines.get(iLineNumber - 1).setsbdextendedsellprice(sParamValue.replace(",", "").trim());
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
    	
    	//System.out.println("[202006080956] - estimate dump:\n" + dumpData());
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
			+ ", " + SMTablesmestimates.svendorquoteid
			+ ", " + SMTablesmestimates.svendorquotenumber
			+ ", " + SMTablesmestimates.sadditionalpostsalestaxcostlabel
			+ ", " + SMTablesmestimates.bdadditionalpostsalestaxcostamt
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
			+ ", NOW()"
			+ ", NOW()"
			+ ", " + m_ivendorquotelinenumber
			+ ", " + sUserID
			+ ", " + sUserID
			+ ", " + m_lsummarylid
			+ ", " + m_lsummarylinenumber
			+ ", '" + clsDatabaseFunctions.FormatSQLStatement(m_sadditionalposttaxcostlabel) + "'"
			+ ", '" + clsDatabaseFunctions.FormatSQLStatement(m_sadditionalpretaxcostlabel) + "'"
			+ ", '" + clsDatabaseFunctions.FormatSQLStatement(sUserFullName) + "'"
			+ ", '" + clsDatabaseFunctions.FormatSQLStatement(m_sdescription) + "'"
			+ ", '" + clsDatabaseFunctions.FormatSQLStatement(m_sitemnumber) + "'"
			+ ", '" + clsDatabaseFunctions.FormatSQLStatement(sUserFullName) + "'"
			+ ", '" + clsDatabaseFunctions.FormatSQLStatement(m_sprefixlabelitem) + "'"
			+ ", '" + clsDatabaseFunctions.FormatSQLStatement(m_sproductdescription) + "'"
			+ ", '" + clsDatabaseFunctions.FormatSQLStatement(m_sunitofmeasure) + "'"
			+ ", '" + clsDatabaseFunctions.FormatSQLStatement(m_svendorquoteid) + "'"
			+ ", '" + clsDatabaseFunctions.FormatSQLStatement(m_svendorquotenumber) + "'"
			+ ", '" + clsDatabaseFunctions.FormatSQLStatement(m_sadditionalpostsalestaxcostlabel) + "'"
			+ ", " + m_bdadditionalpostsalestaxcostamt.replace(",", "")
			+ ")"
					
			+ " ON DUPLICATE KEY UPDATE"
			+ " " + SMTablesmestimates.bdadditionalposttaxcostamount + " = " + m_bdadditionalposttaxcostamount.replace(",", "")
			+ ", " + SMTablesmestimates.bdadditionalpretaxcostamount + " = " + m_bdadditionalpretaxcostamount.replace(",", "")
			+ ", " + SMTablesmestimates.bdextendedcost + " = " + m_bdextendedcost.replace(",", "")
			+ ", " + SMTablesmestimates.bdfreight + " = " + m_bdfreight.replace(",", "")
			+ ", " + SMTablesmestimates.bdlaborcostperunit + " = " + m_bdlaborcostperunit.replace(",", "")
			+ ", " + SMTablesmestimates.bdlaborquantity + " = " + m_bdlaborquantity.replace(",", "")
			+ ", " + SMTablesmestimates.bdlaborsellpriceperunit + " = " + m_bdlaborsellpriceperunit.replace(",", "")
			+ ", " + SMTablesmestimates.bdmarkupamount + " = " + m_bdmarkupamount.replace(",", "")
			+ ", " + SMTablesmestimates.bdquantity + " = " + m_bdquantity.replace(",", "")
			
			+ ", " + SMTablesmestimates.datetimecreated + " = '" + getsdatetimecreatedInSQLFormat() + "'"
			+ ", " + SMTablesmestimates.datetimelastmodified + " = NOW()"
			
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
			+ ", " + SMTablesmestimates.svendorquoteid + " = '" + clsDatabaseFunctions.FormatSQLStatement(m_svendorquoteid) + "'"
			+ ", " + SMTablesmestimates.svendorquotenumber + " = '" + clsDatabaseFunctions.FormatSQLStatement(m_svendorquotenumber) + "'"
			+ ", " + SMTablesmestimates.sadditionalpostsalestaxcostlabel + " = '" + clsDatabaseFunctions.FormatSQLStatement(m_sadditionalpostsalestaxcostlabel) + "'"
			+ ", " + SMTablesmestimates.bdadditionalpostsalestaxcostamt + " = " + m_bdadditionalpostsalestaxcostamt.replace(",", "")												
		;
		
		//System.out.println("[1494260859] - SQL = '" + SQL + "'");
		
		Statement stmt = conn.createStatement();
		try {
			stmt.execute(SQL);
		} catch (Exception e) {
			throw new Exception("Error [1590163606] updating Estimate with SQL: '" + SQL + "' - " + e.getMessage());
		}
		
		//Update the ID:
		if (
			(m_lid.compareToIgnoreCase("") == 0)
			|| (m_lid.compareToIgnoreCase("0") == 0)
			|| (m_lid.compareToIgnoreCase("-1") == 0)
		) {
			String sSQL = "SELECT last_insert_id()";
			try {
				ResultSet rs = clsDatabaseFunctions.openResultSet(sSQL, conn);
				if (rs.next()) {
					m_lid = Long.toString(rs.getLong(1));
				}else {
					m_lid = "0";
				}
				rs.close();
			} catch (Exception e) {
				throw new Exception("Error [1590163604] Could not get last ID number - " + e.getMessage());
			}
			//If something went wrong, we can't get the last ID:
			if (m_lid.compareToIgnoreCase("0") == 0){
				throw new Exception("Error [1590163605] Could not get last ID number.");
			}
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
			m_lsummarylid  = clsValidateFormFields.validateLongIntegerField(m_lsummarylid, "Estimate Summary ID", 1L, clsValidateFormFields.MAX_LONG_VALUE);
		} catch (Exception e) {
			sResult += "  " + e.getMessage() + ".";
		}

		if (m_lsummarylinenumber.compareToIgnoreCase("-1") == 0) {
			//Get the highest line number for this summary and increment it:
			String SQL = "SELECT"
				+ " " + SMTablesmestimates.lsummarylinenumber
				+ " FROM " + SMTablesmestimates.TableName
				+ " WHERE ("
					+ "(" + SMTablesmestimates.lsummarylid + " = " + m_lsummarylid + ")"
				+ ") ORDER BY " + SMTablesmestimates.lsummarylinenumber + " DESC"
				+ " LIMIT 1"
			;
			ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
			try {
				if (rs.next()) {
					m_lsummarylinenumber = Long.toString(rs.getLong(SMTablesmestimates.lsummarylinenumber) + 1);
				}
				else {
					m_lsummarylinenumber = "1";
				}
				rs.close();
			} catch (Exception e) {
				throw new Exception("Error [202006100147] - could not read highest summary line number for summary #" + m_lsummarylid + " - " + e.getMessage());
			}
		}
		try {
			m_lsummarylinenumber  = clsValidateFormFields.validateLongIntegerField(m_lsummarylinenumber, "Estimate Summary line number", 1L, clsValidateFormFields.MAX_LONG_VALUE);
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
			m_svendorquoteid = clsValidateFormFields.validateStringField(
					m_svendorquoteid, 
				SMTablesmestimates.svendorquoteidLength, 
				"Vendor quote ID", 
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
		
		m_ivendorquotelinenumber = m_ivendorquotelinenumber.trim();
		if (m_ivendorquotelinenumber.compareToIgnoreCase("") == 0) {
			m_ivendorquotelinenumber = "0";
		}
		try {
			m_ivendorquotelinenumber = clsValidateFormFields.validateIntegerField(
				m_ivendorquotelinenumber, 
				"Vendor quote line number", 
				0, 
				clsValidateFormFields.MAX_INT_VALUE
			);
		} catch (Exception e) {
			sResult += "  " + e.getMessage() + ".";
		}
		
		if (m_bdquantity.compareToIgnoreCase("") == 0) {
			m_bdquantity = "0.0000";
		}
		try {
			m_bdquantity = clsValidateFormFields.validateBigdecimalField(
				m_bdquantity.replace(",", ""), 
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

		if (m_bdextendedcost.compareToIgnoreCase("") == 0) {
			m_bdextendedcost = "0.00";
		}
		try {
			m_bdextendedcost = clsValidateFormFields.validateBigdecimalField(
				m_bdextendedcost.replace(",", ""), 
				"Extended cost", 
				SMTablesmestimates.bdextendedcostScale, 
				new BigDecimal("0.00"), 
				new BigDecimal("999999999.99")
			);
		} catch (Exception e) {
			sResult += "  " + e.getMessage() + ".";
		}

		if (m_bdfreight.compareToIgnoreCase("") == 0) {
			m_bdfreight = "0.00";
		}
		try {
			m_bdfreight = clsValidateFormFields.validateBigdecimalField(
				m_bdfreight.replace(",", ""), 
				"Freight", 
				SMTablesmestimates.bdfreightScale, 
				new BigDecimal("0.00"), 
				new BigDecimal("999999999.99")
			);
		} catch (Exception e) {
			sResult += "  " + e.getMessage() + ".";
		}

		if (m_bdlaborquantity.compareToIgnoreCase("") == 0) {
			m_bdlaborquantity = "0.0000";
		}
		try {
			m_bdlaborquantity = clsValidateFormFields.validateBigdecimalField(
				m_bdlaborquantity.replace(",", ""), 
				"Labor quantity", 
				SMTablesmestimates.bdlaborquantityScale, 
				new BigDecimal("0.0000"), 
				new BigDecimal("999999999.9999")
			);
		} catch (Exception e) {
			sResult += "  " + e.getMessage() + ".";
		}
		
		if (m_bdlaborcostperunit.compareToIgnoreCase("") == 0) {
			m_bdlaborcostperunit = "0.00";
		}
		try {
			m_bdlaborcostperunit = clsValidateFormFields.validateBigdecimalField(
				m_bdlaborcostperunit.replace(",", ""), 
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
		
		if (m_bdadditionalpretaxcostamount.compareToIgnoreCase("") == 0) {
			m_bdadditionalpretaxcostamount = "0.00";
		}
		try {
			m_bdadditionalpretaxcostamount = clsValidateFormFields.validateBigdecimalField(
					m_bdadditionalpretaxcostamount.replace(",", ""), 
				"Additional pre tax cost amount", 
				SMTablesmestimates.bdadditionalpretaxcostamountScale, 
				new BigDecimal("0.00"), 
				new BigDecimal("999999999.99")
			);
		} catch (Exception e) {
			sResult += "  " + e.getMessage() + ".";
		}

		if (m_bdmarkupamount.compareToIgnoreCase("") == 0) {
			m_bdmarkupamount = "0.00";
		}
		try {
			m_bdmarkupamount = clsValidateFormFields.validateBigdecimalField(
				m_bdmarkupamount.replace(",", ""), 
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
		
		if (m_bdadditionalposttaxcostamount.compareToIgnoreCase("") == 0) {
			m_bdadditionalposttaxcostamount = "0.00";
		}
		try {
			m_bdadditionalposttaxcostamount = clsValidateFormFields.validateBigdecimalField(
				m_bdadditionalposttaxcostamount.replace(",", ""), 
				"Additional post tax cost amount", 
				SMTablesmestimates.bdadditionalposttaxcostamountScale, 
				new BigDecimal("0.00"), 
				new BigDecimal("999999999.99")
			);
		} catch (Exception e) {
			sResult += "  " + e.getMessage() + ".";
		}
		
		if (m_bdlaborsellpriceperunit.compareToIgnoreCase("") == 0) {
			m_bdlaborsellpriceperunit = "0.00";
		}
		try {
			m_bdlaborsellpriceperunit = clsValidateFormFields.validateBigdecimalField(
				m_bdlaborsellpriceperunit.replace(",", ""), 
				"Labor sell prioce per unit", 
				SMTablesmestimates.bdlaborsellpriceperunitScale, 
				new BigDecimal("0.00"), 
				new BigDecimal("999999999.99")
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
		
		try {
			m_sadditionalpostsalestaxcostlabel = clsValidateFormFields.validateStringField(
				m_sadditionalpostsalestaxcostlabel, 
				SMTablesmestimates.sadditionalpostsalestaxcostlabelLength, 
				"Additional cost after sales tax label", 
				true
			);
		} catch (Exception e) {
			sResult += "  " + e.getMessage() + ".";
		}
		
		try {
			m_bdadditionalpostsalestaxcostamt = clsValidateFormFields.validateBigdecimalField(
				m_bdadditionalpostsalestaxcostamt.replace(",", ""), 
				"Additional cost after sales tax", 
				SMTablesmestimates.bdadditionalpostsalestaxcostamtScale, 
				new BigDecimal("0.00"), 
				new BigDecimal("999999.00")
			);
		} catch (Exception e) {
			sResult += "  " + e.getMessage() + ".";
		}
		
		//First, check that the quantities are all valid:
		for (int i = 0; i < arrEstimateLines.size(); i++){
			if (arrEstimateLines.get(i).getsbdquantity().compareToIgnoreCase("") == 0) {
				arrEstimateLines.get(i).setsbdquantity("0.0000");
			}
			try {
				arrEstimateLines.get(i).setsbdquantity(
				clsValidateFormFields.validateBigdecimalField(
						arrEstimateLines.get(i).getsbdquantity().replace(",", ""), 
						"Line quantity", 
						SMTablesmestimatelines.bdquantityScale,
						new BigDecimal("0.0000"),
						new BigDecimal("999999999.9999")
						).replaceAll(",", "")
				);
			} catch (Exception e) {
				sResult += "  " + e.getMessage() + ".";
			}
		}
		
		//Next, remove any zero quantity lines:
		try {
			removeZeroQtyLines();
		} catch (Exception e1) {
			throw new Exception("Error [202006084004] - error removing lines with zero quantity - " + e1.getMessage());
		}
		
		//Now validate the lines:
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
		
		if (sResult.compareToIgnoreCase("") != 0) {
			throw new Exception("Error [202006121844] validating estimate - " + sResult);
		}
		
		return;
	}
	public void lookUpItem(String sLineNumber, Connection conn) throws Exception{
		//This function replaces the description, U/M, and extended cost on the line number specified:
		int iLineNumber = 0;
		try {
			iLineNumber = Integer.parseInt(sLineNumber);
		} catch (Exception e) {
			throw new Exception("Error [202006101549] - line number '" + sLineNumber + "' is invalid.");
		}
		
		//If the user is changing the item number on a new (unsaved) line, this function will be passed in a 'ZERO' as the
		// Line number.  But that line will have been added to the end of the line array at this point, so it's REAL
		// line number will just be the last line number in the array:
		if (iLineNumber == 0) {
			iLineNumber = arrEstimateLines.size();
		}
		
		//Get the item number that's now on that line:
		String sItemNumber = "";
		try {
			sItemNumber = arrEstimateLines.get(iLineNumber - 1).getsitemnumber();
		} catch (Exception e) {
			throw new Exception("Error [202006101756] - could not read line number '" + Integer.toString(iLineNumber) 
				+ "' in estimate line array - arr size = " + Integer.toString(arrEstimateLines.size()));
		}
		
		ICItem item = new ICItem(sItemNumber);
		if(!item.load(conn)){
			//We just assume it's not an inventory number, and we just make it all blank:
			arrEstimateLines.get(iLineNumber - 1).setslinedescription("(not found)");
			arrEstimateLines.get(iLineNumber - 1).setsunitofmeasure("(N/A)");
			arrEstimateLines.get(iLineNumber - 1).setsbdextendedcost("0.00");
		}else {
			arrEstimateLines.get(iLineNumber - 1).setslinedescription(item.getItemDescription());
			arrEstimateLines.get(iLineNumber - 1).setsunitofmeasure(item.getCostUnitOfMeasure());
			BigDecimal bdExtendedCost = new BigDecimal("0.00");
			BigDecimal bdQuantity = new BigDecimal(arrEstimateLines.get(iLineNumber - 1).getsbdquantity().replace(",", ""));
			BigDecimal bdUnitCost = new BigDecimal(item.getMostRecentCost());
			
			//If it's a real inventory item that we'll need to calculate an extended cost for, we need a quantity:
			if (bdQuantity.compareTo(BigDecimal.ZERO) <= 0) {
				throw new Exception("Error [202006103041] - Quantity for line number " + Integer.toString(iLineNumber) + ", item number '" + sItemNumber + "' cannot be zero.");
			}
			bdExtendedCost = bdQuantity.multiply(bdUnitCost).setScale(SMTablesmestimatelines.bdextendedcostScale, BigDecimal.ROUND_HALF_UP);
			arrEstimateLines.get(iLineNumber - 1).setsbdextendedcost(clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(bdExtendedCost));
			arrEstimateLines.get(iLineNumber - 1).setsbdunitcost(clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(bdUnitCost));
			
			//We'll need to read the established sell price for this item, if it's a real inventory item:
			try {
				loadSummary(conn);
			} catch (Exception e) {
				throw new Exception("Error [202006183021] - could not load summary to get price list and level - " + e.getMessage());
			}
			arrEstimateLines.get(iLineNumber - 1).setsbdunitsellprice(
				clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(item.getItemPrice(
					m_estimatesummary.getspricelistcode(), m_estimatesummary.getsipricelevel(), conn)));
		}
		
		//In case the user was changing one of the existing lines, don't let any 'zero quantity' lines stay in the array at this point:
		removeZeroQtyLines();
		
		return;
		
	}
	public void refreshAllItems(ServletContext context, String sDBID, String sUserID) throws Exception{
		
		Connection conn;
		try {
			conn = clsDatabaseFunctions.getConnectionWithException(context, sDBID, "MySQL", SMUtilities.getFullClassName(this.toString()));
		} catch (Exception e1) {
			throw new Exception("Error [202006080743] - could not get connection - " + e1.getMessage());
		}

		for (int i = 0; i < arrEstimateLines.size(); i++) {
			try {
				refreshItem(Integer.toString(i + 1), conn);
			} catch (Exception e) {
				throw new Exception("Error [202006122552] - " + e.getMessage());
			}
		}
		clsDatabaseFunctions.freeConnection(context, conn, "[1591986288]");
		return;
	}
	public String refreshVendorQuoteLine(Connection conn, String sDBID, String sUserID, String sUserFullName) throws Exception{
		
		String sResult = "NOTE: Vendor quote number " + m_svendorquotenumber + " was last modified ";
		
		//Get the vendor quote:
		SMOHDirectQuoteList quotelist = new SMOHDirectQuoteList();
		
		String sRequest = SMOHDirectFieldDefinitions.ENDPOINT_QUOTE + "?%24filter="
			+ SMOHDirectFieldDefinitions.QUOTE_FIELD_QUOTENUMBER + "%20eq%20'" + m_svendorquotenumber + "'"
		;
		try {
			quotelist.getQuoteList(sRequest, conn, sDBID, sUserID);
		} catch (Exception e) {
			throw new Exception("Error [202004274322] - " + e.getMessage());
		}
		
		sResult += quotelist.getLastModifiedDates().get(0) + ".";
		
		SMOHDirectQuoteLineList quotelineslist = new SMOHDirectQuoteLineList();
		sRequest = SMOHDirectFieldDefinitions.ENDPOINT_QUOTELINE + "?$filter=" 
				+ SMOHDirectFieldDefinitions.QUOTELINE_FIELD_QUOTENUMBER + "%20eq%20'" + quotelist.getQuoteIDs().get(0) + "'"
				+ "&%24orderby%20eq%20" + SMOHDirectFieldDefinitions.QUOTELINE_FIELD_LINENUMBER + "%20asc"
			;
		try {
			quotelineslist.getQuoteLineList(sRequest, conn, sDBID, sUserID);
		} catch (Exception e4) {
			throw new Exception("Error [202004273322] - " + e4.getMessage());
		}
		
		int iQuoteLineNumber = Integer.parseInt(m_ivendorquotelinenumber);
		
		//System.out.println("[202006124851] - iQuoteLineNumber = '" + iQuoteLineNumber + "'.");
		try {
			setsbdextendedcost(clsManageBigDecimals.BigDecimalToScaledFormattedString(
				SMTablesmestimates.bdextendedcostScale, quotelineslist.getTotalCosts().get(iQuoteLineNumber - 1)));
			setsbdquantity(clsManageBigDecimals.BigDecimalToScaledFormattedString(
				SMTablesmestimates.bdquantityScale, quotelineslist.getQuantities().get(iQuoteLineNumber - 1)));
			setsdescription("");
			setsitemnumber("MISC");
			setsproductdescription(quotelineslist.getDescriptions().get(iQuoteLineNumber - 1));
			setsunitofmeasure("EA");
		} catch (Exception e) {
			throw new Exception("Error [202006115356] - error adding estimates to summary from vendor quote - " + e.getMessage());
		}
		
		//Now save the estimate:
		try {
			save_without_data_transaction(conn, sUserID, sUserFullName);
		} catch (Exception e) {
			throw new Exception("Error [202006124529] - saving estimate - " + e.getMessage());
		}
		
		return sResult;
	}
	public String replaceVendorQuoteLine(
			Connection conn, 
			String sDBID, 
			String sUserID, 
			String sUserFullName,
			String sVendorQuoteNumber,
			String sVendorQuoteLineNumber
			) throws Exception{
		
		String sResult = "NOTE: Vendor quote number " + sVendorQuoteNumber + " was last modified ";
		
		//Get the vendor quote:
		SMOHDirectQuoteList quotelist = new SMOHDirectQuoteList();
		
		String sRequest = SMOHDirectFieldDefinitions.ENDPOINT_QUOTE + "?%24filter="
			+ SMOHDirectFieldDefinitions.QUOTE_FIELD_QUOTENUMBER + "%20eq%20'" + sVendorQuoteNumber + "'"
		;
		try {
			quotelist.getQuoteList(sRequest, conn, sDBID, sUserID);
		} catch (Exception e) {
			throw new Exception("Error [202004274122] - " + e.getMessage());
		}
		
		if (quotelist.getQuoteNumbers().size() == 0) {
			throw new Exception("Could not read quote number '" + sVendorQuoteNumber + "'.");
		}
		
		sResult += quotelist.getLastModifiedDates().get(0) + ".";
		
		SMOHDirectQuoteLineList quotelineslist = new SMOHDirectQuoteLineList();
		sRequest = SMOHDirectFieldDefinitions.ENDPOINT_QUOTELINE + "?$filter=" 
				+ SMOHDirectFieldDefinitions.QUOTELINE_FIELD_QUOTENUMBER + "%20eq%20'" + quotelist.getQuoteIDs().get(0) + "'"
				+ "&%24orderby%20eq%20" + SMOHDirectFieldDefinitions.QUOTELINE_FIELD_LINENUMBER + "%20asc"
			;
		try {
			quotelineslist.getQuoteLineList(sRequest, conn, sDBID, sUserID);
		} catch (Exception e4) {
			throw new Exception("Error [202004273122] - " + e4.getMessage());
		}
		
		if (quotelineslist.getQuoteLineIDs().size() == 0) {
			throw new Exception("Could not read line number '" + sVendorQuoteLineNumber + "' on quote number '" + sVendorQuoteNumber + "'.");
		}
		
		int iQuoteLineNumber;
		try {
			iQuoteLineNumber = Integer.parseInt(sVendorQuoteLineNumber.trim());
		} catch (Exception e1) {
			throw new Exception("Error [202006142108] - vendor quote line number '" + sVendorQuoteLineNumber + "' is not a valid number.");
		}
		if (quotelineslist.getQuoteLineIDs().size() < iQuoteLineNumber) {
			throw new Exception("Vendor quote number '" + sVendorQuoteNumber + "' only has " + Integer.toString(quotelineslist.getQuoteLineIDs().size())
				+ " lines, but you are asking for line " + sVendorQuoteLineNumber + ".");
		}
		
		//System.out.println("[202006124851] - iQuoteLineNumber = '" + iQuoteLineNumber + "'.");
		try {
			setsbdextendedcost(clsManageBigDecimals.BigDecimalToScaledFormattedString(
				SMTablesmestimates.bdextendedcostScale, quotelineslist.getTotalCosts().get(iQuoteLineNumber - 1)));
			setsbdquantity(clsManageBigDecimals.BigDecimalToScaledFormattedString(
				SMTablesmestimates.bdquantityScale, quotelineslist.getQuantities().get(iQuoteLineNumber - 1)));
			setsdescription("");
			setsitemnumber("MISC");
			setsproductdescription(quotelineslist.getDescriptions().get(iQuoteLineNumber - 1));
			setsunitofmeasure("EA");
			setsvendorquotenumber(sVendorQuoteNumber);
			setsivendorquotelinenumber(sVendorQuoteLineNumber);
			setsvendorquoteid(quotelist.getQuoteIDs().get(0));
		} catch (Exception e) {
			throw new Exception("Error [202006115156] - error replacing vendor quote - " + e.getMessage());
		}
		
		//Now save the estimate:
		try {
			save_without_data_transaction(conn, sUserID, sUserFullName);
		} catch (Exception e) {
			throw new Exception("Error [202006124129] - saving estimate - " + e.getMessage());
		}
		
		return sResult;
	}
	public void refreshItem(String sLineNumber, Connection conn) throws Exception{
		//This function updates the description, U/M, and unit/extended cost on the line number specified:
		int iLineNumber = 0;
		try {
			iLineNumber = Integer.parseInt(sLineNumber);
		} catch (Exception e) {
			throw new Exception("Error [202006101559] - line number '" + sLineNumber + "' is invalid.");
		}
		
		//Get the item number that's now on that line:
		String sItemNumber = "";
		try {
			sItemNumber = arrEstimateLines.get(iLineNumber - 1).getsitemnumber();
		} catch (Exception e) {
			throw new Exception("Error [202006101776] - could not read line number '" + Integer.toString(iLineNumber) 
				+ "' in estimate line array - arr size = " + Integer.toString(arrEstimateLines.size()));
		}
		
		ICItem item = new ICItem(sItemNumber);
		if(!item.load(conn)){
			//We just assume it's not an inventory number, and we just make it all blank:
			if (arrEstimateLines.get(iLineNumber - 1).getslinedescription().compareToIgnoreCase("") == 0){
				arrEstimateLines.get(iLineNumber - 1).setslinedescription("(not found)");
			}
			if (arrEstimateLines.get(iLineNumber - 1).getsunitofmeasure().compareToIgnoreCase("") == 0){
				arrEstimateLines.get(iLineNumber - 1).setsunitofmeasure("(N/A)");
			}
			if (arrEstimateLines.get(iLineNumber - 1).getsbdunitcost().compareToIgnoreCase("") == 0){
				arrEstimateLines.get(iLineNumber - 1).setsbdunitcost("0.00");
			}
			if (arrEstimateLines.get(iLineNumber - 1).getsbdextendedcost().compareToIgnoreCase("") == 0){
				arrEstimateLines.get(iLineNumber - 1).setsbdextendedcost("0.00");
			}
		}else {
			arrEstimateLines.get(iLineNumber - 1).setslinedescription(item.getItemDescription());
			arrEstimateLines.get(iLineNumber - 1).setsunitofmeasure(item.getCostUnitOfMeasure());
			BigDecimal bdExtendedCost = new BigDecimal("0.00");
			BigDecimal bdQuantity = new BigDecimal(arrEstimateLines.get(iLineNumber - 1).getsbdquantity().replace(",", ""));
			BigDecimal bdUnitCost = new BigDecimal(item.getMostRecentCost());
			
			//If it's a real inventory item that we'll need to calculate an extended cost for, we need a quantity:
			if (bdQuantity.compareTo(BigDecimal.ZERO) <= 0) {
				throw new Exception("Error [202006103031] - Quantity for line number " + Integer.toString(iLineNumber) + ", item number '" + sItemNumber + "' cannot be zero.");
			}
			bdExtendedCost = bdQuantity.multiply(bdUnitCost).setScale(SMTablesmestimatelines.bdextendedcostScale, BigDecimal.ROUND_HALF_UP);
			arrEstimateLines.get(iLineNumber - 1).setsbdextendedcost(clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(bdExtendedCost));
			arrEstimateLines.get(iLineNumber - 1).setsbdunitcost(clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(bdUnitCost));
			
			//We'll need to read the established sell price for this item, if it's a real inventory item:
			try {
				loadSummary(conn);
			} catch (Exception e) {
				throw new Exception("Error [202006183121] - could not load summary to get price list and level - " + e.getMessage());
			}
			arrEstimateLines.get(iLineNumber - 1).setsbdunitsellprice(
				clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(item.getItemPrice(
					m_estimatesummary.getspricelistcode(), m_estimatesummary.getsipricelevel(), conn)));
		}
		
		//Remove any duplicate zero lines, but this shouldn't happen....
		removeZeroQtyLines();
		
		return;
		
	}
    private void removeZeroQtyLines() throws Exception{
    	ArrayList<SMEstimateLine> m_arrTempLines = new ArrayList<SMEstimateLine> (0);
    	for (int i = 0; i < arrEstimateLines.size(); i++){
    		BigDecimal bdQty = null;
			try {
				bdQty = new BigDecimal(arrEstimateLines.get(i).getsbdquantity().replace(",", ""));
			} catch (Exception e) {
				throw new Exception("Error [202006083912] - quantity '" + arrEstimateLines.get(i).getsbdquantity() + "' is invalid.");
			}
    		if (bdQty.compareTo(BigDecimal.ZERO) > 0){
    			SMEstimateLine line = arrEstimateLines.get(i);
    			m_arrTempLines.add(line);
    		}
    	}
    	arrEstimateLines.clear();
    	for (int i = 0; i < m_arrTempLines.size(); i++){
    		SMEstimateLine line = m_arrTempLines.get(i);
			arrEstimateLines.add(line);
    	}
     }
	public void load(ServletContext context, String sDBID, String sUserID) throws Exception{

		Connection conn; 
		try { 
			conn =
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
				m_svendorquoteid = rs.getString(SMTablesmestimates.svendorquoteid);
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
				m_sadditionalpostsalestaxcostlabel = rs.getString(SMTablesmestimates.sadditionalpostsalestaxcostlabel);
				m_bdadditionalpostsalestaxcostamt = clsManageBigDecimals.BigDecimalToScaledFormattedString(
					SMTablesmestimatesummaries.bdadditionalpostsalestaxcostamtScale, rs.getBigDecimal(SMTablesmestimates.bdadditionalpostsalestaxcostamt));
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
		m_lsummarylinenumber = ssummarylinenumber;
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
	
	public String getsvendorquoteid(){
		return m_svendorquoteid;
	}
	public void setsvendorquoteid(String svendorquoteid){
		m_svendorquoteid = svendorquoteid;
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
	
	public String getsadditionalpostsalestaxcostlabel(){
		return m_sadditionalpostsalestaxcostlabel;
	}
	public void setsadditionalpostsalestaxcostlabel(String sadditionalpostsalestaxcostlabel){
		m_sadditionalpostsalestaxcostlabel = sadditionalpostsalestaxcostlabel;
	}
	
	public String getsbdadditionalpostsalestaxcostamt(){
		return m_bdadditionalpostsalestaxcostamt;
	}
	public void setsbdadditionalpostsalestaxcostamt(String sbdadditionalpostsalestaxcostamt){
		m_bdadditionalpostsalestaxcostamt = sbdadditionalpostsalestaxcostamt;
	}
	
	public SMEstimateSummary getsummary() {
		return m_estimatesummary;
	}
	
	public void addLine(SMEstimateLine line){
		arrEstimateLines.add(line);
	}

	public ArrayList<SMEstimateLine> getLineArray(){
		return arrEstimateLines;
	}
	
	public BigDecimal getTotalPrice(Connection conn) throws Exception{
		BigDecimal bdTotalPrice = new BigDecimal("0.00");
		
		try {
			bdTotalPrice = bdTotalPrice.add(new BigDecimal(m_bdextendedcost.replace(",", "")));
			
			//TODO - add estimate line amts:
			
			bdTotalPrice = bdTotalPrice.add(new BigDecimal(m_bdfreight.replace(",", "")));
			BigDecimal bdLaborUnits = new BigDecimal(m_bdlaborquantity.replace(",", ""));
			BigDecimal bdLaborCostPerUnit = new BigDecimal(m_bdlaborcostperunit.replace(",", ""));
			bdTotalPrice = bdTotalPrice.add(bdLaborUnits.multiply(bdLaborCostPerUnit));
			bdTotalPrice = bdTotalPrice.add(new BigDecimal(m_bdadditionalpretaxcostamount.replace(",", "")));
			bdTotalPrice = bdTotalPrice.add(new BigDecimal(m_bdmarkupamount.replace(",", "")));
			bdTotalPrice = bdTotalPrice.add(getTotalTaxOnMaterial(conn));
			bdTotalPrice = bdTotalPrice.add(new BigDecimal(m_bdadditionalposttaxcostamount.replace(",", "")));
		} catch (Exception e) {
			throw new Exception("Error [202005270912] - calculating total price - " + e.getMessage());
		}
		
		return bdTotalPrice;
	}
	public BigDecimal getTotalTaxOnMaterial(Connection conn) throws Exception{
		
		BigDecimal bdTaxOnMaterial = new BigDecimal("0.00");
		SMEstimateSummary summary = new SMEstimateSummary();
		summary.setslid(m_lsummarylid);
		try {
			summary.load(conn);
		} catch (Exception e) {
			throw new Exception("Error [202005270035] - could not load summary number '" + m_lsummarylid + "' - " + e.getMessage());
		}
		
		SMTax tax = new SMTax();
		tax.set_slid(summary.getsitaxid());
		try {
			tax.load(conn);
		} catch (Exception e) {
			throw new Exception("Error [202005270128] - could not load tax with ID '" + summary.getsitaxid() + "' - " + e.getMessage());
		}
		
		bdTaxOnMaterial = getTotalMaterialCostSubjectToUseTax(conn).multiply((new BigDecimal(tax.get_bdtaxrate().replace(",", ""))).divide(new BigDecimal("100.00"), BigDecimal.ROUND_HALF_UP));
		
		return bdTaxOnMaterial;
	}
	public BigDecimal getTotalAddlMaterialCostNotSubjectToUseTax(Connection conn) throws Exception{
		BigDecimal bdTotalAddlMaterialCostNotSubjectToUseTax = new BigDecimal("0.00");
		
		bdTotalAddlMaterialCostNotSubjectToUseTax = new BigDecimal(m_bdadditionalposttaxcostamount.replace(",", ""));
		
		return bdTotalAddlMaterialCostNotSubjectToUseTax;
	}
	public BigDecimal getTotalMaterialCostSubjectToUseTax(Connection conn) throws Exception{
		BigDecimal bdTotalMaterialCost = new BigDecimal("0.00");
		
		bdTotalMaterialCost = bdTotalMaterialCost.add(new BigDecimal(m_bdextendedcost.replace(",", "")));
		
		//TODO - add in the estimate line costs:
		for (int i = 0; i < arrEstimateLines.size(); i++) {
			bdTotalMaterialCost.add(new BigDecimal(arrEstimateLines.get(i).getsbdextendedcost().replace(",", "")));
		}
		
		bdTotalMaterialCost = bdTotalMaterialCost.add(new BigDecimal(m_bdadditionalpretaxcostamount.replace(",", "")));
		
		return bdTotalMaterialCost;
	}

	public void loadSummary(ServletContext context, String sDBID, String sUserID) throws Exception{
		
		Connection conn;
		try {
			conn = clsDatabaseFunctions.getConnectionWithException(context, sDBID, "MySQL", SMUtilities.getFullClassName(this.toString()));
		} catch (Exception e1) {
			throw new Exception("Error [202006080443] - could not get connection - " + e1.getMessage());
		}

		try {
			loadSummary(conn);
		} catch (Exception e) {
			clsDatabaseFunctions.freeConnection(context, conn, "[1591986274]");
			throw new Exception("Error [202006080801] - loading summary - " + e.getMessage());
		}
		clsDatabaseFunctions.freeConnection(context, conn, "[1591986275]");
		return;
	}
	private void loadSummary(Connection conn) throws Exception{
		
		m_estimatesummary = new SMEstimateSummary();
		m_estimatesummary.setslid(m_lsummarylid);
		try {
			m_estimatesummary.load(conn);
		} catch (Exception e) {
			throw new Exception("Error [202006083715] - could not load summary with ID '" + m_lsummarylid + "' - " + e.getMessage());
		}
		
		return;
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
		sQueryString += "&" + SMTablesmestimates.svendorquotenumber + "=" + clsServletUtilities.URLEncode(getsvendorquoteid());
		sQueryString += "&" + SMTablesmestimates.svendorquotenumber + "=" + clsServletUtilities.URLEncode(getsvendorquotenumber());
		sQueryString += "&" + SMTablesmestimatesummaries.sadditionalpostsalestaxcostlabel + "=" + clsServletUtilities.URLEncode(getsadditionalpostsalestaxcostlabel());
		sQueryString += "&" + SMTablesmestimatesummaries.bdadditionalpostsalestaxcostamt + "=" + clsServletUtilities.URLEncode(getsbdadditionalpostsalestaxcostamt());
		return sQueryString;
	}
	
	public String dumpData(){
		String s = "";
		
		s += "\n  lid: " +  getslid() + "\n";
		s += "  summary lid: " +  getslsummarylid() + "\n";
		s += "  summary line number: " +  getslsummarylinenumber() + "\n";
		s += "  description: " +  getsdescription() + "\n";
		s += "  prefix label item: " +  getsprefixlabelitem() + "\n";
		s += "  vendor quote ID: " +  getsvendorquoteid() + "\n";
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
		s += "&" + "Additional post sales tax cost label: " + getsadditionalpostsalestaxcostlabel() + "\n";
		s += "&" + "Additional post sales tax cost amt: " + getsbdadditionalpostsalestaxcostamt() + "\n";
				
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
		m_svendorquoteid = "";
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
		m_bdadditionalpretaxcostamount = "0.00";
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
		m_sadditionalpostsalestaxcostlabel = "";
		m_bdadditionalpostsalestaxcostamt = "0.00";
	}
}
