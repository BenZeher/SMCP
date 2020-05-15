package smgl;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import SMClasses.MySQLs;
import SMClasses.SMBatchStatuses;
import SMClasses.SMEntryBatch;
import SMClasses.SMModuleTypes;
import SMDataDefinition.SMTableapaccountsets;
import SMDataDefinition.SMTableapbatches;
import SMDataDefinition.SMTableapdistributioncodes;
import SMDataDefinition.SMTableaptransactionlines;
import SMDataDefinition.SMTableaptransactions;
import SMDataDefinition.SMTableapvendorgroups;
import SMDataDefinition.SMTablearacctset;
import SMDataDefinition.SMTableartransactions;
import SMDataDefinition.SMTablebkaccountentries;
import SMDataDefinition.SMTablebkbanks;
import SMDataDefinition.SMTableentries;
import SMDataDefinition.SMTableentrylines;
import SMDataDefinition.SMTablefamaster;
import SMDataDefinition.SMTableglaccountgroups;
import SMDataDefinition.SMTableglaccounts;
import SMDataDefinition.SMTableglaccountstructures;
import SMDataDefinition.SMTableglacctsegmentvalues;
import SMDataDefinition.SMTablegloptions;
import SMDataDefinition.SMTableicaccountsets;
import SMDataDefinition.SMTableiccategories;
import SMDataDefinition.SMTableicpoheaders;
import SMDataDefinition.SMTableicpoinvoiceheaders;
import SMDataDefinition.SMTableicpoinvoicelines;
import SMDataDefinition.SMTableicpolines;
import SMDataDefinition.SMTableicporeceiptheaders;
import SMDataDefinition.SMTableicporeceiptlines;
import SMDataDefinition.SMTableicvendors;
import SMDataDefinition.SMTableinvoicedetails;
import SMDataDefinition.SMTableinvoiceheaders;
import SMDataDefinition.SMTablelocations;
import SMDataDefinition.SMTabletax;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsManageBigDecimals;
import ServletUtilities.clsManageRequestParameters;
import ServletUtilities.clsServletUtilities;
import ServletUtilities.clsStringFunctions;
import ServletUtilities.clsValidateFormFields;
import smcontrolpanel.SMUtilities;
import smic.ICEntryBatch;

public class GLAccount extends java.lang.Object{
	
	public static final String ParamsAddingNewRecord = "bAddingNewRecord";
	public static final String Paramsacctid = "sacctid";
	public static final String Paramsformattedacctid = "sformattedacctid";
	public static final String Paramsdescription = "sdescription";
	public static final String Paramstype = "stype";
	public static final String Paramlactive = "lactive";
	public static final String Paramicostcenterid = "icostcenterid";
	public static final String Paramiallowaspoexpense = "iallowaspoexpense";
	public static final String Paramlaccountstructureid = "laccountstructureid";
	public static final String Paramlaccountgroupid = "laccountgroupid";
	public static final String Parambdannualbudget = "bdannualbudget";
	public static final String Paraminormalbalancetype = "inormalbalancetype";
	public static final String Paramobjectname = "GL Account";
	
	public static final String ACCOUNT_SEGMENT_DELIMITER = "-";
	
	private String m_sacctid;
	private String m_sformattedacctid;
	private String m_sdescription;
	private String m_stype;
	private String m_sactive;
	private String m_icostcenterid;
	private String m_iallowaspoexpense;
	private String m_laccountstructureid;
	private String m_laccountgroupid;
	private String m_sbdannualbudget;
	private String m_inormalbalancetype;
	private String m_iNewRecord;
	private ArrayList<String> m_sErrorMessageArray = new ArrayList<String> (0);

	public GLAccount(
    		String sAccountID
        ) {
		m_sacctid = sAccountID;
		m_sformattedacctid = "";
		m_sdescription = "";
		m_stype = "";
		m_sactive = "1";
		m_icostcenterid = "0";
		m_iallowaspoexpense = "1";
		m_laccountstructureid = "0";
		m_laccountgroupid = "0";
		m_sbdannualbudget = "0.00";
		m_inormalbalancetype = "0";
	}
    public void loadFromHTTPRequest(HttpServletRequest req){
    	m_iNewRecord = clsManageRequestParameters.get_Request_Parameter(ParamsAddingNewRecord, req).trim().replace("&quot;", "\"");
    	m_sacctid = clsManageRequestParameters.get_Request_Parameter(Paramsacctid, req).trim().replace("&quot;", "\"");
    	m_sformattedacctid = clsManageRequestParameters.get_Request_Parameter(Paramsformattedacctid, req).trim().replace("&quot;", "\"");
    	m_sdescription = clsManageRequestParameters.get_Request_Parameter(Paramsdescription, req).trim().replace("&quot;", "\"");
    	m_stype = clsManageRequestParameters.get_Request_Parameter(Paramstype, req).trim().replace("&quot;", "\"");
		if(req.getParameter(Paramlactive) == null){
			m_sactive = "0";
		}else{
			m_sactive = "1";
		}
		if(req.getParameter(Paramiallowaspoexpense) == null){
			m_iallowaspoexpense = "0";
		}else{
			m_iallowaspoexpense = "1";
		}
		m_icostcenterid = clsManageRequestParameters.get_Request_Parameter(Paramicostcenterid, req).trim().replace("&quot;", "\"");
		m_laccountstructureid = clsManageRequestParameters.get_Request_Parameter(Paramlaccountstructureid, req).trim().replace("&quot;", "\"");
		if (m_laccountstructureid.compareToIgnoreCase("") == 0){ m_laccountstructureid = "0"; }
		m_laccountgroupid = clsManageRequestParameters.get_Request_Parameter(Paramlaccountgroupid, req).trim().replace("&quot;", "\"");
		if (m_laccountgroupid.compareToIgnoreCase("") == 0){ m_laccountgroupid = "0"; }
		m_sbdannualbudget = clsManageRequestParameters.get_Request_Parameter(Parambdannualbudget, req).trim().replace("&quot;", "\"");
		if (getsbdannualbudget().compareToIgnoreCase("") == 0){
			setsbdannualbudget("0.00");
		}
		m_inormalbalancetype = clsManageRequestParameters.get_Request_Parameter(Paraminormalbalancetype, req).trim().replace("&quot;", "\"");
    }
    public boolean load (
    		Connection conn
    		){
    
    	return load (m_sacctid, conn);
    }
    public boolean load (
    	String sAccountID,
    	Connection conn
    	){
    
	    String SQL = MySQLs.Get_GL_Account_SQL(sAccountID);
		try {
			ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn); 
			rs.next();

			//Load the variables:
			m_sacctid = clsStringFunctions.checkStringForNull(rs.getString(SMTableglaccounts.sAcctID));
			m_sformattedacctid = clsStringFunctions.checkStringForNull(rs.getString(SMTableglaccounts.sFormattedAcct));
			m_sdescription = clsStringFunctions.checkStringForNull(rs.getString(SMTableglaccounts.sDesc));
			m_stype = clsStringFunctions.checkStringForNull(rs.getString(SMTableglaccounts.sAcctType));
			m_sactive = Long.toString(rs.getLong(SMTableglaccounts.lActive));
			m_icostcenterid = Long.toString(rs.getLong(SMTableglaccounts.iCostCenterID));
			m_iallowaspoexpense = Long.toString(rs.getLong(SMTableglaccounts.iallowaspoexpense));
			m_laccountstructureid = Long.toString(rs.getLong(SMTableglaccounts.lstructureid));
			m_laccountgroupid = Long.toString(rs.getLong(SMTableglaccounts.laccountgroupid));
			m_sbdannualbudget = clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(rs.getBigDecimal(SMTableglaccounts.bdannualbudget));
			m_inormalbalancetype = Long.toString(rs.getLong(SMTableglaccounts.inormalbalancetype));
			m_iNewRecord = "0";
			rs.close();
		}catch (SQLException ex){
	        m_sErrorMessageArray.add("SQL error [1387472162] in GLAccount.load: " + ex.getMessage() + " - SQL: " + SQL);
	        return false;
		}
    	return true;
    }
    public boolean load (
    		ServletContext context,
    		String sDBIB
    		){
    
    	return load (m_sacctid, context, sDBIB);
    }
    public boolean load (
        	String sAccountID,
        	ServletContext context,
        	String sDBIB
        	){
        
    	    String SQL = MySQLs.Get_GL_Account_SQL(sAccountID);
    		try {
    			ResultSet rs = clsDatabaseFunctions.openResultSet(
    					SQL, 
    					context,
    					sDBIB,
    					"MySQL",
    					this.toString() + ".load"); 
    			rs.next();

    			//Load the variables:
    			m_sacctid = clsStringFunctions.checkStringForNull(rs.getString(SMTableglaccounts.sAcctID));
    			m_sformattedacctid = clsStringFunctions.checkStringForNull(rs.getString(SMTableglaccounts.sFormattedAcct));
    			m_sdescription = clsStringFunctions.checkStringForNull(rs.getString(SMTableglaccounts.sDesc));
    			m_stype = clsStringFunctions.checkStringForNull(rs.getString(SMTableglaccounts.sAcctType));
    			m_sactive = Long.toString(rs.getLong(SMTableglaccounts.lActive));
    			m_icostcenterid = Long.toString(rs.getLong(SMTableglaccounts.iCostCenterID));
    			m_iallowaspoexpense = Long.toString(rs.getLong(SMTableglaccounts.iallowaspoexpense));
    			m_laccountstructureid =  Long.toString(rs.getLong(SMTableglaccounts.lstructureid));
    			m_laccountgroupid =  Long.toString(rs.getLong(SMTableglaccounts.laccountgroupid));
    			m_sbdannualbudget = clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(rs.getBigDecimal(SMTableglaccounts.bdannualbudget));
    			m_inormalbalancetype = Long.toString(rs.getLong(SMTableglaccounts.inormalbalancetype));
    			m_iNewRecord = "0";
    			rs.close();
    		}catch (SQLException ex){
    	        m_sErrorMessageArray.add("SQL error [1387472200] in GLAccount.load: " + ex.getMessage() + " - SQL: " + SQL);
    	        return false;
    		}
        	return true;
        }
    public boolean save(Connection conn){
    	String SQL = MySQLs.Get_GL_Account_SQL(m_sacctid);
    	
		m_sErrorMessageArray.clear();
		if(!validateEntries(conn)){
			return false;
		}
    	
    	try{
			ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
			if(rs.next()){
				//If it's supposed to be a new record, then return an error:
				if(m_iNewRecord.compareToIgnoreCase("1") == 0){
					m_sErrorMessageArray.add("Cannot save - gl account already exists.");
					rs.close();
					return false;
				}
				rs.close();
				
				//Update the record:
				SQL = MySQLs.Update_GL_Account_SQL(
						clsDatabaseFunctions.FormatSQLStatement(m_sacctid), 
						clsDatabaseFunctions.FormatSQLStatement(m_sformattedacctid), 
						clsDatabaseFunctions.FormatSQLStatement(m_sdescription), 
						clsDatabaseFunctions.FormatSQLStatement(m_stype),
						m_sactive,
						m_icostcenterid,
						m_iallowaspoexpense,
						m_laccountstructureid,
						m_laccountgroupid,
						m_sbdannualbudget,
						m_inormalbalancetype
						);
				if(!clsDatabaseFunctions.executeSQL(SQL, conn)){
					m_sErrorMessageArray.add("Cannot execute UPDATE SQL: '" + SQL + "'.");
					return false;
				}else{
					m_iNewRecord = "0";
					return true;
				}
			}else{
				//If it DOESN'T exist:
				//If it's supposed to be an existing record, then return an error:
				if(m_iNewRecord.compareToIgnoreCase("0") == 0){
					m_sErrorMessageArray.add("Cannot save - can't get existing gl account.");
					rs.close();
					return false;
				}
				rs.close();
				
				//Insert the record:
				SQL = MySQLs.Insert_GL_Account_SQL(
						clsDatabaseFunctions.FormatSQLStatement(m_sacctid), 
						clsDatabaseFunctions.FormatSQLStatement(m_sformattedacctid), 
						clsDatabaseFunctions.FormatSQLStatement(m_sdescription), 
						clsDatabaseFunctions.FormatSQLStatement(m_stype),
						m_sactive,
						m_icostcenterid,
						m_iallowaspoexpense,
						m_laccountstructureid,
						m_laccountgroupid,
						m_sbdannualbudget,
						m_inormalbalancetype
						);
				if(!clsDatabaseFunctions.executeSQL(SQL, conn)){
					m_sErrorMessageArray.add("Cannot execute INSERT SQL '" + SQL + "'.");
					return false;
				}else{
					m_iNewRecord = "0";
				}
				
				//Now add any missing fiscal set and financial statement data records for the new account:
				//TODO - test this:
				/*
				GLFinancialDataCheck fdc = new GLFinancialDataCheck();
				try {
					fdc.checkMissingFiscalSets(m_sacctid, conn, "");
				} catch (Exception e) {
					throw new Exception("Error [202005150233] - " + e.getMessage());
				}
				*/
			}
		}catch(Exception e){
			m_sErrorMessageArray.add("Error [1520285260] saving gl account with SQL '" + SQL + " - " + e.getMessage());
			return false;
		}
    	
    	return true;
    }

	private boolean validateEntries(Connection conn){
		
		boolean bEntriesAreValid = true;
		m_sErrorMessageArray.clear();
    	//Validate fields:
    	m_sacctid = m_sacctid.trim();
    	if(m_sacctid.length() == 0){
    		m_sErrorMessageArray.add("Account ID cannot be blank");
    		bEntriesAreValid = false;
    	}
    	if(m_sacctid.length() > SMTableglaccounts.sAcctIDLength){
    		m_sErrorMessageArray.add("Account ID is too long");
    		bEntriesAreValid = false;
    	}
    	m_sdescription = m_sdescription.trim();
    	if(m_sdescription.length() == 0){
    		m_sErrorMessageArray.add("Account description cannot be blank");
    		bEntriesAreValid = false;
    	}
    	if(m_sdescription.length() > SMTableglaccounts.sDescLength){
    		m_sErrorMessageArray.add("Account description is too long");
    		bEntriesAreValid = false;
    	}

    	m_sformattedacctid = m_sformattedacctid.trim();
    	if(m_sformattedacctid.length() > SMTableglaccounts.sFormattedAcctLength){
    		m_sErrorMessageArray.add("Formatted account ID is too long");
    		bEntriesAreValid = false;
    	}
    	
    	//If we strip out the separators from the formatted ID, does it even MATCH the unformatted account ID?
    	if (getM_sacctid().compareToIgnoreCase(getM_sformattedacctid().replaceAll(ACCOUNT_SEGMENT_DELIMITER, "")) != 0){
    		m_sErrorMessageArray.add("The formatted account ID ('" + getM_sformattedacctid() + "') has to match the actual account ID ('" + getM_sacctid() + "').");
    	}
    	
    	try {
			validate_account_structure(conn);
		} catch (Exception e) {
			m_sErrorMessageArray.add(e.getMessage());
			bEntriesAreValid = false;
		}
    	
    	//Validate the account group ID:
    	
    	//If there are NO account group records, then we don't need to vaildate the account group:
    	boolean bAccountGroupRecordsExist = false;
    	String SQL;
		try {
			SQL = "SELECT COUNT(*) FROM " + SMTableglaccountgroups.TableName;
			ResultSet rsCount = clsDatabaseFunctions.openResultSet(SQL, conn);
			if (rsCount.next()){
				if (rsCount.getLong(1) != 0L){
					bAccountGroupRecordsExist = true;
				}
			}
			rsCount.close();
		} catch (SQLException e1) {
			m_sErrorMessageArray.add("Error [1528313033] - could not get count of GL Account Groups - " + e1.getMessage());
			bEntriesAreValid = false;
		}
		
		//Annual budget:
		setsbdannualbudget(getsbdannualbudget().replaceAll(",", "").trim());
		try {
			setsbdannualbudget(clsValidateFormFields.validateBigdecimalField(
				getsbdannualbudget(), 
				"Annual Budget", 
				SMTableglaccounts.bdannualbudgetScale,
				new BigDecimal("-999999999.99"),
				new BigDecimal("999999999.99")
				).replaceAll(",", "")
			);
		} catch (Exception e) {
			m_sErrorMessageArray.add("Error [1552315800] - error reading annual budget '" + getsbdannualbudget() + "' - " + e.getMessage());
			bEntriesAreValid = false;
		}
    	
		//Normal balance type:
		if (
			(getsinormalbalancetype().compareToIgnoreCase(Integer.toString(SMTableglaccounts.NORMAL_BALANCE_TYPE_DEBIT)) != 0)
			&& (getsinormalbalancetype().compareToIgnoreCase(Integer.toString(SMTableglaccounts.NORMAL_BALANCE_TYPE_CREDIT)) != 0)
		){
			m_sErrorMessageArray.add("Error [1552316800] - normal balance type '" + getsinormalbalancetype() + "' is invalid.");
			bEntriesAreValid = false;
		}
		
    	if (bAccountGroupRecordsExist){
	    	SQL = " SELECT * FROM " + SMTableglaccountgroups.TableName
	    		+ " WHERE ("
	    			+ "(" + SMTableglaccountgroups.lid + " = " + m_laccountgroupid + ")"
	    		+ ")"
	    	;
	    	try {
				ResultSet rsAccountGroups = clsDatabaseFunctions.openResultSet(SQL, conn);
				if (!rsAccountGroups.next()){
					m_sErrorMessageArray.add("The account group is invalid.");
				}
				rsAccountGroups.close();
			} catch (SQLException e) {
				m_sErrorMessageArray.add(e.getMessage());
				bEntriesAreValid = false;
			}
    	}
    	return bEntriesAreValid;
	}
	
	private void validate_account_structure(Connection conn) throws Exception{
		
		//If there ARE no account structures yet, then we don't try to validate them:
		String SQL = "SELECT COUNT(*) FROM " + SMTableglaccountstructures.TableName;
		try {
			ResultSet rsStructureCount = clsDatabaseFunctions.openResultSet(
				SQL, 
				conn
			);
			if (rsStructureCount.next()){
				if (rsStructureCount.getLong(1) == 0){
					rsStructureCount.close();
					return;
				}
				else{
					rsStructureCount.close();
				}
			}else{
				rsStructureCount.close();
				return;
			}
		} catch (Exception e1) {
			throw new Exception("Error [1528150540] getting number of account structures - " + e1.getMessage() + ".");
		}
		
    	GLAccountStructure struct = new GLAccountStructure();
    	struct.setlid(m_laccountstructureid);
    	try {
			struct.load(conn);
		} catch (Exception e) {
			throw new Exception("Error [1527865061] - could not load GL account structure with ID '" + m_laccountstructureid + "' - " + e.getMessage());
		}
    	if (struct.getNewRecord().compareToIgnoreCase("1") == 0){
    		throw new Exception("GL Account Structure with ID '" + m_laccountstructureid + "' is not valid.");
    	}

    	//Now check the structure against the formatted ID to make sure it has valid values and is formatted correctly:
    	
    	//Split the 'formatted account ID' into it's segments:
    	String sAccountSegments[] = getM_sformattedacctid().split(ACCOUNT_SEGMENT_DELIMITER);

    	//First make sure that the number of segments match the number of segments in the account structure:
    	int iNumberOfSegmentsInStructure = 0;
    	if (struct.getssegmentid1().compareToIgnoreCase("0") != 0){
    		iNumberOfSegmentsInStructure++;
    	}
    	if (struct.getssegmentid2().compareToIgnoreCase("0") != 0){
    		iNumberOfSegmentsInStructure++;
    	}
    	if (struct.getssegmentid3().compareToIgnoreCase("0") != 0){
    		iNumberOfSegmentsInStructure++;
    	}
    	if (struct.getssegmentid4().compareToIgnoreCase("0") != 0){
    		iNumberOfSegmentsInStructure++;
    	}
    	if (struct.getssegmentid5().compareToIgnoreCase("0") != 0){
    		iNumberOfSegmentsInStructure++;
    	}
    	if (struct.getssegmentid6().compareToIgnoreCase("0") != 0){
    		iNumberOfSegmentsInStructure++;
    	}
    	if (struct.getssegmentid7().compareToIgnoreCase("0") != 0){
    		iNumberOfSegmentsInStructure++;
    	}
    	if (struct.getssegmentid8().compareToIgnoreCase("0") != 0){
    		iNumberOfSegmentsInStructure++;
    	}
    	if (struct.getssegmentid9().compareToIgnoreCase("0") != 0){
    		iNumberOfSegmentsInStructure++;
    	}
    	if (struct.getssegmentid10().compareToIgnoreCase("0") != 0){
    		iNumberOfSegmentsInStructure++;
    	}

    	//Make sure that the number of segments in the 'formatted ID' match the number of segments in the account structure:
    	if (iNumberOfSegmentsInStructure != sAccountSegments.length){
    		throw new Exception("The formatted account ID has " + Integer.toString(sAccountSegments.length) + " segments, but the selected account structure has " + Integer.toString(iNumberOfSegmentsInStructure) + " segments.");
    	}
    	
    	//Check the length of each structure:
    	String sErrors = "";
    	for (int iSegmentIndex = 0; iSegmentIndex < iNumberOfSegmentsInStructure; iSegmentIndex++){
    		if (iSegmentIndex == 0){
    			if (sAccountSegments[iSegmentIndex].length() != Integer.parseInt(struct.getssegmentlength1())){
    				sErrors += "Account segment " + Integer.toString(iSegmentIndex + 1) 
    					+ " is not the length defined in the GL Account Structure for segment " + Integer.toString(iSegmentIndex + 1) 
    					+ ", which is " + struct.getssegmentlength1() + ".  ";
    			}
    		}
    		if (iSegmentIndex == 1){
    			if (sAccountSegments[iSegmentIndex].length() != Integer.parseInt(struct.getssegmentlength2())){
    				sErrors += "Account segment " + Integer.toString(iSegmentIndex + 1) 
    					+ " is not the length defined in the GL Account Structure for segment " + Integer.toString(iSegmentIndex + 1) 
    					+ ", which is " + struct.getssegmentlength2() + ".  ";
    			}
    		}
    		if (iSegmentIndex == 2){
    			if (sAccountSegments[iSegmentIndex].length() != Integer.parseInt(struct.getssegmentlength3())){
    				sErrors += "Account segment " + Integer.toString(iSegmentIndex + 1) 
    					+ " is not the length defined in the GL Account Structure for segment " + Integer.toString(iSegmentIndex + 1) 
    					+ ", which is " + struct.getssegmentlength3() + ".  ";
    			}
    		}
    		if (iSegmentIndex == 3){
    			if (sAccountSegments[iSegmentIndex].length() != Integer.parseInt(struct.getssegmentlength4())){
    				sErrors += "Account segment " + Integer.toString(iSegmentIndex + 1) 
    					+ " is not the length defined in the GL Account Structure for segment " + Integer.toString(iSegmentIndex + 1) 
    					+ ", which is " + struct.getssegmentlength4() + ".  ";
    			}
    		}
    		if (iSegmentIndex == 4){
    			if (sAccountSegments[iSegmentIndex].length() != Integer.parseInt(struct.getssegmentlength5())){
    				sErrors += "Account segment " + Integer.toString(iSegmentIndex + 1) 
    					+ " is not the length defined in the GL Account Structure for segment " + Integer.toString(iSegmentIndex + 1) 
    					+ ", which is " + struct.getssegmentlength5() + ".  ";
    			}
    		}
    		if (iSegmentIndex == 5){
    			if (sAccountSegments[iSegmentIndex].length() != Integer.parseInt(struct.getssegmentlength6())){
    				sErrors += "Account segment " + Integer.toString(iSegmentIndex + 1) 
    					+ " is not the length defined in the GL Account Structure for segment " + Integer.toString(iSegmentIndex + 1) 
    					+ ", which is " + struct.getssegmentlength6() + ".  ";
    			}
    		}
    		if (iSegmentIndex == 6){
    			if (sAccountSegments[iSegmentIndex].length() != Integer.parseInt(struct.getssegmentlength7())){
    				sErrors += "Account segment " + Integer.toString(iSegmentIndex + 1) 
    					+ " is not the length defined in the GL Account Structure for segment " + Integer.toString(iSegmentIndex + 1) 
    					+ ", which is " + struct.getssegmentlength7() + ".  ";
    			}
    		}
    		if (iSegmentIndex == 7){
    			if (sAccountSegments[iSegmentIndex].length() != Integer.parseInt(struct.getssegmentlength8())){
    				sErrors += "Account segment " + Integer.toString(iSegmentIndex + 1) 
    					+ " is not the length defined in the GL Account Structure for segment " + Integer.toString(iSegmentIndex + 1) 
    					+ ", which is " + struct.getssegmentlength8() + ".  ";
    			}
    		}
    		if (iSegmentIndex == 8){
    			if (sAccountSegments[iSegmentIndex].length() != Integer.parseInt(struct.getssegmentlength9())){
    				sErrors += "Account segment " + Integer.toString(iSegmentIndex + 1) 
    					+ " is not the length defined in the GL Account Structure for segment " + Integer.toString(iSegmentIndex + 1) 
    					+ ", which is " + struct.getssegmentlength9() + ".  ";
    			}
    		}
    		if (iSegmentIndex == 9){
    			if (sAccountSegments[iSegmentIndex].length() != Integer.parseInt(struct.getssegmentlength10())){
    				sErrors += "Account segment " + Integer.toString(iSegmentIndex + 1) 
    					+ " is not the length defined in the GL Account Structure for segment " + Integer.toString(iSegmentIndex + 1) 
    					+ ", which is " + struct.getssegmentlength10() + ".  ";
    			}
    		}

    	}

    	//Now check each of the segment values:
    	for (int iSegmentIndex = 0; iSegmentIndex < iNumberOfSegmentsInStructure; iSegmentIndex++){
    		String sStructureSegmentID = "0";
    		if (iSegmentIndex == 0){ sStructureSegmentID = struct.getssegmentid1();	}
    		if (iSegmentIndex == 1){ sStructureSegmentID = struct.getssegmentid2();	}
    		if (iSegmentIndex == 2){ sStructureSegmentID = struct.getssegmentid3();	}
    		if (iSegmentIndex == 3){ sStructureSegmentID = struct.getssegmentid4();	}
    		if (iSegmentIndex == 4){ sStructureSegmentID = struct.getssegmentid5();	}
    		if (iSegmentIndex == 5){ sStructureSegmentID = struct.getssegmentid6();	}
    		if (iSegmentIndex == 6){ sStructureSegmentID = struct.getssegmentid7();	}
    		if (iSegmentIndex == 7){ sStructureSegmentID = struct.getssegmentid8();	}
    		if (iSegmentIndex == 8){ sStructureSegmentID = struct.getssegmentid9();	}
    		if (iSegmentIndex == 9){ sStructureSegmentID = struct.getssegmentid10();	}
    		SQL = "SELECT"
    			+ " " + SMTableglacctsegmentvalues.svalue
    			+ " FROM " + SMTableglacctsegmentvalues.TableName
    			+ " WHERE ("
    				+ "(" + SMTableglacctsegmentvalues.lsegmentid + " = " + sStructureSegmentID + ")"
    			+ ")"
    		;
    		ResultSet rs = clsDatabaseFunctions.openResultSet(
    			SQL, conn);
    		boolean bNoMatchingSegmentFound = true;
    		int iRecordCounter = 0;
    		while (rs.next()){
    			if (rs.getString(SMTableglacctsegmentvalues.svalue).compareTo(sAccountSegments[iSegmentIndex]) == 0){
    				bNoMatchingSegmentFound = false;
    			}
    			iRecordCounter++;
    		}
    		rs.close();
    		//Some segments may have NO valid values (for example the first segment which is the 'account',
    		//  and if that's the case, then we don't validate the value:
    		if (iRecordCounter != 0){
    			if (bNoMatchingSegmentFound){
    				sErrors += "The segment value ('" + sAccountSegments[iSegmentIndex] + "') is not a valid value for "
    					+ "segment " + Integer.toString(iSegmentIndex + 1) + " in the selected account structure.  ";
    			}
    		}
    	}
    	
    	if (sErrors.compareToIgnoreCase("") != 0){
    		throw new Exception(sErrors);
    	}
	}
	
	public String getQueryString(){
		String sQueryString = "";
		sQueryString += ParamsAddingNewRecord + "=" + clsServletUtilities.URLEncode(m_iNewRecord);
		sQueryString += "&" + Paramsacctid + "=" + clsServletUtilities.URLEncode(m_sacctid);
		sQueryString += "&" + Paramsformattedacctid + "=" + clsServletUtilities.URLEncode(m_sformattedacctid);
		sQueryString += "&" + Paramsdescription + "=" + clsServletUtilities.URLEncode(m_sdescription);
		sQueryString += "&" + Paramstype + "=" + clsServletUtilities.URLEncode(m_stype);
		sQueryString += "&" + Paramicostcenterid + "=" + clsServletUtilities.URLEncode(m_icostcenterid);
		sQueryString += "&" + Paramlaccountstructureid + "=" + clsServletUtilities.URLEncode(m_laccountstructureid);
		sQueryString += "&" + Paramlaccountgroupid + "=" + clsServletUtilities.URLEncode(m_laccountgroupid);
		sQueryString += "&" + Parambdannualbudget + "=" + clsServletUtilities.URLEncode(getsbdannualbudget());
		sQueryString += "&" + Paraminormalbalancetype + "=" + clsServletUtilities.URLEncode(getsinormalbalancetype());
		
		if (m_iallowaspoexpense.compareToIgnoreCase("1") == 0){
			sQueryString += "&" + Paramiallowaspoexpense + "=" + m_iallowaspoexpense;
		}
		if (m_sactive.compareToIgnoreCase("1") == 0){
			sQueryString += "&" + Paramlactive + "=" + m_sactive;
		}
		
		return sQueryString;
	}
	public boolean delete(String sGLAcct, ServletContext context, String sDBIB){
		
		m_sErrorMessageArray.clear();
		
		//First, check that the acct exists:
		String SQL = MySQLs.Get_GL_Account_SQL(sGLAcct);
		
		try{
			ResultSet rs = clsDatabaseFunctions.openResultSet(
					SQL, 
					context,
					sDBIB,
					"MySQL",
					this.toString() + ".delete (1)");
			if(!rs.next()){
				m_sErrorMessageArray.add("GL Account " + sGLAcct + " cannot be found.");
				rs.close();
				return false;
			}
			rs.close();
		}catch(SQLException e){
			System.out.println("Error [1518554364] checking GL Account to delete - " + e.getMessage());
			m_sErrorMessageArray.add("Error [1518554364] checking GL Account to delete - " + e.getMessage());
			return false;
		}
		
		//Account sets
		SQL =  "SELECT * FROM " + SMTablearacctset.TableName + 
				" WHERE (" + 
				"(" + SMTablearacctset.sAcctsReceivableControlAcct + " = '" + sGLAcct + "')" +
				" OR (" + SMTablearacctset.sCashAcct + " = '" + sGLAcct + "')" +
				" OR (" + SMTablearacctset.sPrepaymentLiabilityAcct + " = '" + sGLAcct + "')" +
				" OR (" + SMTablearacctset.sReceiptDiscountsAcct + " = '" + sGLAcct + "')" +
				" OR (" + SMTablearacctset.sRetainageAcct + " = '" + sGLAcct + "')" +
				" OR (" + SMTablearacctset.sWriteOffAcct + " = '" + sGLAcct + "')" +
			")";
		try{
			ResultSet rs = clsDatabaseFunctions.openResultSet(
					SQL, 
					context,
					sDBIB,
					"MySQL",
					this.toString() + ".delete (2)");
			if(rs.next()){
				m_sErrorMessageArray.add("Error [1518554365] - GL Account " + sGLAcct + " is used in some account sets.");
				rs.close();
				return false;
			}
			rs.close();
		}catch(SQLException e){
			m_sErrorMessageArray.add("Error [1518554366] checking account sets to delete GL - " + e.getMessage());
			return false;
		}
		
		//aropenstransactions - scontrolacct
		SQL = "SELECT " 
				+ SMTableartransactions.lid
				+ " FROM " + SMTableartransactions.TableName
				+ " WHERE ("
					+ "(" + SMTableartransactions.scontrolacct + " = '" + sGLAcct + "')"
					+ " AND (" + SMTableartransactions.dcurrentamt + " != 0.00)"
				+ ")"
				;
		
		try{
			ResultSet rs = clsDatabaseFunctions.openResultSet(
					SQL, 
					context,
					sDBIB,
					"MySQL",
					this.toString() + ".delete (3)");
			if(rs.next()){
				m_sErrorMessageArray.add("Error [1518554367] - GL Acct " + sGLAcct + " has open balances.");
				rs.close();
				return false;
			}
			rs.close();
		}catch(SQLException e){
			m_sErrorMessageArray.add("Error [1518554368] checking open transactions to delete GL - " + e.getMessage());
			return false;
		}
		
		//invoice details
		SQL = "SELECT " 
				+ SMTableinvoiceheaders.TableName + "." + SMTableinvoiceheaders.sInvoiceNumber
				+ " FROM " + SMTableinvoiceheaders.TableName + ", " + SMTableinvoicedetails.TableName
				+ " WHERE ("
					+ "("
						+ "(" + SMTableinvoicedetails.sExpenseGLAcct + " = '" + sGLAcct + "')"
						+ " OR (" + SMTableinvoicedetails.sInventoryGLAcct + " = '" + sGLAcct + "')"
						+ " OR (" + SMTableinvoicedetails.sRevenueGLAcct + " = '" + sGLAcct + "')"
					+ ")"
					
					+ " AND ("
					
					+ " (" + SMTableinvoiceheaders.iExportedToAR + " != 1) OR (" + SMTableinvoiceheaders.iExportedToIC + " != 1)"
					
					+ ")"
					
					+ " AND (" + SMTableinvoiceheaders.TableName + "." + SMTableinvoiceheaders.sInvoiceNumber
						+ " = " + SMTableinvoicedetails.TableName + "." + SMTableinvoicedetails.sInvoiceNumber
						+ ")"
					
				+ ")"
				;
		
		try{
			ResultSet rs = clsDatabaseFunctions.openResultSet(
					SQL, 
					context,
					sDBIB,
					"MySQL",
					this.toString() + ".delete (4)");
			if(rs.next()){
				m_sErrorMessageArray.add("Error [1518554369]structure - GL Account " + sGLAcct + " is used on some unexported invoice details.");
				rs.close();
				return false;
			}
			rs.close();
		}catch(SQLException e){
			m_sErrorMessageArray.add("Error [1518554370] checking unexported invoices to delete GL - " + e.getMessage());
			return false;
		}
		
		//tax - sglacct
		SQL = "SELECT " 
		+ SMTabletax.staxjurisdiction
		+ " FROM " + SMTabletax.TableName
		+ " WHERE ("
			+ "(" + SMTabletax.sglacct + " = '" + sGLAcct + "')"
		+ ")"
		;
		try{
			ResultSet rs = clsDatabaseFunctions.openResultSet(
					SQL, 
					context,
					sDBIB,
					"MySQL",
					this.toString() + ".delete (5)");
			if(rs.next()){
				m_sErrorMessageArray.add("Error [1518554371] - GL Account " + sGLAcct + " is used on some taxes.");
				rs.close();
				return false;
			}
			rs.close();
		}catch(SQLException e){
			m_sErrorMessageArray.add("Error [1518554372] - checking taxes to delete GL - " + e.getMessage());
			return false;
		}
		
		//transactionentries - scontrolacct
		SQL =  "SELECT " + SMTableentries.lid 
				+ " FROM " + SMTableentries.TableName + ", " + SMEntryBatch.TableName
				+ " WHERE ("
					+ "(" + SMTableentries.scontrolacct + " = '" + sGLAcct + "')"
					+ " AND (" + SMTableentries.TableName + "." + SMTableentries.ibatchnumber + " = " 
						+ SMEntryBatch.TableName + "." + SMEntryBatch.ibatchnumber + ")"
					+ " AND (" + SMEntryBatch.smoduletype + " = '" + SMModuleTypes.AR + "')"
					+ " AND (" + SMEntryBatch.ibatchstatus + " != " + SMBatchStatuses.DELETED + ")"
					+ " AND (" + SMEntryBatch.ibatchstatus + " != " + SMBatchStatuses.POSTED + ")"
				+ ")"
				;
		try{
			ResultSet rs = clsDatabaseFunctions.openResultSet(
					SQL, 
					context,
					sDBIB,
					"MySQL",
					this.toString() + ".delete (6)");
			if(rs.next()){
				m_sErrorMessageArray.add("Error [1518554373] - GL Account " + sGLAcct + " is used on unposted batch entries.");
				rs.close();
				return false;
			}
			rs.close();
		}catch(SQLException e){
			m_sErrorMessageArray.add("Error [1518554374] - checking taxes to delete GL - " + e.getMessage());
			return false;
		}
		
		//transactionlines - sglacct
		SQL = "SELECT " + SMTableentries.TableName + "." + SMTableentries.lid 
				+ " FROM " + SMTableentries.TableName + ", " + SMEntryBatch.TableName
				 + ", " + SMTableentrylines.TableName
				+ " WHERE ("
					+ "(" + SMTableentrylines.sglacct + " = '" + sGLAcct + "')"
					+ " AND (" + SMTableentries.TableName + "." + SMTableentries.ibatchnumber + " = " 
						+ SMEntryBatch.TableName + "." + SMEntryBatch.ibatchnumber + ")"
					+ " AND (" + SMTableentrylines.TableName + "." + SMTableentrylines.ibatchnumber + " = " 
						+ SMEntryBatch.TableName + "." + SMEntryBatch.ibatchnumber + ")"
					+ " AND (" + SMTableentries.TableName + "." + SMTableentries.ientrynumber + " = " 
						+ SMTableentrylines.TableName + "." + SMTableentrylines.ientrynumber + ")"
					+ " AND (" + SMEntryBatch.smoduletype + " = '" + SMModuleTypes.AR + "')"
					+ " AND (" + SMEntryBatch.ibatchstatus + " != " + SMBatchStatuses.DELETED + ")"
					+ " AND (" + SMEntryBatch.ibatchstatus + " != " + SMBatchStatuses.POSTED + ")"
				+ ")"
				;
		try{
			ResultSet rs = clsDatabaseFunctions.openResultSet(
					SQL, 
					context,
					sDBIB,
					"MySQL",
					this.toString() + ".delete (7)");
			if(rs.next()){
				m_sErrorMessageArray.add("Error [1518554375] - GL Account " + sGLAcct + " is used on unposted batch lines.");
				rs.close();
				return false;
			}
			rs.close();
		}catch(SQLException e){
			m_sErrorMessageArray.add("Error [1518554376] - checking transaction lines to delete GL - " + e.getMessage());
			return false;
		}
		
		//Make sure all AP batches are posted:
		SQL = "SELECT " + SMTableapbatches.lbatchnumber + " FROM " + SMTableapbatches.TableName
			+ " WHERE ("
				+ "(" + SMTableapbatches.ibatchstatus + " != " + SMBatchStatuses.DELETED + ")"
				+ " AND (" + SMTableapbatches.ibatchstatus + " != " + SMBatchStatuses.POSTED + ")"
			+ ")"
		;
		try{
			ResultSet rs = clsDatabaseFunctions.openResultSet(
				SQL, 
				context,
				sDBIB,
				"MySQL",
				this.toString() + ".delete (8)");
			if(rs.next()){
				m_sErrorMessageArray.add("Error [1552074477] - there are AP batches that must be posted OR deleted before deleting a GL account.");
				rs.close();
				return false;
			}
			rs.close();
		}catch(SQLException e){
			m_sErrorMessageArray.add("Error [1552074478] - checking AP batch statuses to delete GL with SQL '" + SQL + "' - "+ e.getMessage());
			return false;
		}
		
		//Make sure all IC batches are posted:
		SQL = "SELECT " + ICEntryBatch.lbatchnumber + " FROM " + ICEntryBatch.TableName
			+ " WHERE ("
				+ "(" + ICEntryBatch.ibatchstatus + " != " + SMBatchStatuses.DELETED + ")"
				+ " AND (" + ICEntryBatch.ibatchstatus + " != " + SMBatchStatuses.POSTED + ")"
			+ ")"
		;
		try{
			ResultSet rs = clsDatabaseFunctions.openResultSet(
				SQL, 
				context,
				sDBIB,
				"MySQL",
				this.toString() + ".delete (9)");
			if(rs.next()){
				m_sErrorMessageArray.add("Error [1552074479] - there are IC batches that must be posted OR deleted before deleting a GL account.");
				rs.close();
				return false;
			}
			rs.close();
		}catch(SQLException e){
			m_sErrorMessageArray.add("Error [1552074480] - checking IC batch statuses to delete GL with SQL '" + SQL + "' - "+ e.getMessage());
			return false;
		}
		
		//Make sure this isn't the current GL 'closing' account:
		SQL = "SELECT " + SMTablegloptions.sclosingaccount + " FROM " + SMTablegloptions.TableName
			+ " WHERE ("
				+ "(" + SMTablegloptions.sclosingaccount + " = '" + sGLAcct + "')"
			+ ")"
		;
		try{
			ResultSet rs = clsDatabaseFunctions.openResultSet(
				SQL, 
				context,
				sDBIB,
				"MySQL",
				this.toString() + ".delete (10)");
			if(rs.next()){
				m_sErrorMessageArray.add("Error [1552074481] - you are trying to delete the GL closing account.");
				rs.close();
				return false;
			}
			rs.close();
		}catch(SQLException e){
			m_sErrorMessageArray.add("Error [1552074482] - checking the GL closing account to delete GL with SQL '" + SQL + "' - "+ e.getMessage());
			return false;
		}
		
		//AP Account sets:
		SQL = "SELECT " + SMTableapaccountsets.lid + " FROM " + SMTableapaccountsets.TableName
			+ " WHERE ("
				+ "(" + SMTableapaccountsets.spayablescontrolacct + " = '" + sGLAcct + "')"
				+ " OR (" + SMTableapaccountsets.sprepaymentacct + " = '" + sGLAcct + "')"
				+ " OR (" + SMTableapaccountsets.spurchasediscountacct + " = '" + sGLAcct + "')"
			+ ")"
		;
		try{
			ResultSet rs = clsDatabaseFunctions.openResultSet(
				SQL, 
				context,
				sDBIB,
				"MySQL",
				this.toString() + ".delete (11)");
			if(rs.next()){
				m_sErrorMessageArray.add("Error [1552074483] - this account is included in at least one AP account set.");
				rs.close();
				return false;
			}
			rs.close();
		}catch(SQLException e){
			m_sErrorMessageArray.add("Error [1552074484] - checking the AP account sets to delete GL with SQL '" + SQL + "' - "+ e.getMessage());
			return false;
		}
		
		//AP transaction lines:
		SQL = "SELECT " + SMTableaptransactionlines.TableName + "." + SMTableaptransactionlines.lid + " FROM " + SMTableaptransactionlines.TableName
			+ " LEFT JOIN " + SMTableaptransactions.TableName + " ON "
			+ SMTableaptransactions.TableName + "." + SMTableaptransactions.lid + "=" 
			+ SMTableaptransactionlines.TableName + "." + SMTableaptransactionlines.ltransactionheaderid
			+ " WHERE ("
				+ "(" + SMTableaptransactions.TableName + "." + SMTableaptransactions.bdcurrentamt + " != 0.00)"
				+ " AND ("
				+ " (" + SMTableaptransactions.TableName + "." + SMTableaptransactions.scontrolacct + " = '" + sGLAcct + "')"
				+ " OR (" + SMTableaptransactionlines.TableName + "." + SMTableaptransactionlines.sdistributionacct + " = '" + sGLAcct + "')"
				+ ")"
			+ ")"
			;
			try{
				ResultSet rs = clsDatabaseFunctions.openResultSet(
					SQL, 
					context,
					sDBIB,
					"MySQL",
					this.toString() + ".delete (12)");
				if(rs.next()){
					m_sErrorMessageArray.add("Error [1552074485] - this account is included on some open AP transactions.");
					rs.close();
					return false;
				}
				rs.close();
			}catch(SQLException e){
				m_sErrorMessageArray.add("Error [1552074486] - checking the AP transactions to delete GL with SQL '" + SQL + "' - "+ e.getMessage());
				return false;
			}
		
		//AP vendor groups:
		SQL = "SELECT " + SMTableapvendorgroups.lid + " FROM " + SMTableapvendorgroups.TableName
			+ " WHERE ("
				+ "(" + SMTableapvendorgroups.sglacctusedfordistribution + " = '" + sGLAcct + "')"
			+ ")"
		;
		try{
			ResultSet rs = clsDatabaseFunctions.openResultSet(
				SQL, 
				context,
				sDBIB,
				"MySQL",
				this.toString() + ".delete (13)");
			if(rs.next()){
				m_sErrorMessageArray.add("Error [1552074487] - this account is used as the distribtuion account in an AP Vendor Group.");
				rs.close();
				return false;
			}
			rs.close();
		}catch(SQLException e){
			m_sErrorMessageArray.add("Error [1552074488] - checking the AP vendor groups to delete GL with SQL '" + SQL + "' - "+ e.getMessage());
			return false;
		}
		
		//AP Distribution codes:
		SQL = "SELECT " + SMTableapdistributioncodes.lid + " FROM " + SMTableapdistributioncodes.TableName
			+ " WHERE ("
				+ "(" + SMTableapdistributioncodes.sglacct + " = '" + sGLAcct + "')"
			+ ")"
		;
		try{
			ResultSet rs = clsDatabaseFunctions.openResultSet(
				SQL, 
				context,
				sDBIB,
				"MySQL",
				this.toString() + ".delete (14)");
			if(rs.next()){
				m_sErrorMessageArray.add("Error [1552074489] - this account is used as the distribtuion account in an AP Vendor Group.");
				rs.close();
				return false;
			}
			rs.close();
		}catch(SQLException e){
			m_sErrorMessageArray.add("Error [1552074490] - checking the AP distribution codes to delete GL with SQL '" + SQL + "' - "+ e.getMessage());
			return false;
		}
		
		//Fixed assets
		SQL = "SELECT " + SMTablefamaster.sAssetNumber + " FROM " + SMTablefamaster.TableName
			+ " WHERE ("
				//If the current value is <= salvage value, there's no depreciation happening anymore.
				+ "(" + SMTablefamaster.bdCurrentValue + " > " + SMTablefamaster.bdSalvageValue + ")" 
				+ " AND (" 
					+ "(" + SMTablefamaster.sDepreciationGLAcct + " = '" + sGLAcct + "')"
					+ " OR (" + SMTablefamaster.sLossOrGainGL + " = '" + sGLAcct + "')"
					+ " OR (" + SMTablefamaster.sNotePayableGLAcct + " = '" + sGLAcct + "')"
					+ " OR (" + SMTablefamaster.sAccumulatedDepreciationGLAcct + " = '" + sGLAcct + "')"
				+ ")"
			+ ")"
		;
		try{
			ResultSet rs = clsDatabaseFunctions.openResultSet(
				SQL, 
				context,
				sDBIB,
				"MySQL",
				this.toString() + ".delete (15)");
			if(rs.next()){
				m_sErrorMessageArray.add("Error [1552074491] - this account is included on at least one Asset in Fixed Assets.");
				rs.close();
				return false;
			}
			rs.close();
		}catch(SQLException e){
			m_sErrorMessageArray.add("Error [1552074492] - checking Fixed Assets to delete GL with SQL '" + SQL + "' - "+ e.getMessage());
			return false;
		}
			
		//IC Categories:
		SQL = "SELECT " + SMTableiccategories.sCategoryCode + " FROM " + SMTableiccategories.TableName
			+ " WHERE ("
				+ "(" + SMTableiccategories.sCostofGoodsSoldAccount + " = '" + sGLAcct + "')"
					+ " OR (" + SMTableiccategories.sSalesAccount + " = '" + sGLAcct + "')"
			+ ")"
		;
		try{
			ResultSet rs = clsDatabaseFunctions.openResultSet(
				SQL, 
				context,
				sDBIB,
				"MySQL",
				this.toString() + ".delete (16)");
			if(rs.next()){
				m_sErrorMessageArray.add("Error [1552074493] - this account is included on at least one IC Category.");
				rs.close();
				return false;
			}
			rs.close();
		}catch(SQLException e){
			m_sErrorMessageArray.add("Error [1552074494] - checking IC Categories to delete GL with SQL '" + SQL + "' - "+ e.getMessage());
			return false;
		}	
		
		//IC PO Lines
		SQL = "SELECT " + SMTableicpolines.TableName + "." + SMTableicpolines.lid + " FROM " + SMTableicpolines.TableName
			+ " LEFT JOIN " + SMTableicpoheaders.TableName + " ON "
			+ SMTableicpolines.TableName + "." + SMTableicpolines.lpoheaderid + " = "
			+ SMTableicpoheaders.TableName + "." + SMTableicpoheaders.lid
			+ " WHERE ("
				+ "(" + SMTableicpolines.TableName + "." + SMTableicpolines.sglexpenseacct + " = '" + sGLAcct + "')"
				+ " AND (" + SMTableicpoheaders.TableName + "." + SMTableicpoheaders.lstatus + " != " + Integer.toString(SMTableicpoheaders.STATUS_COMPLETE) + ")"
				+ " AND (" + SMTableicpoheaders.TableName + "." + SMTableicpoheaders.lstatus + " != " + Integer.toString(SMTableicpoheaders.STATUS_DELETED) + ")"
			+ ")"
		;
		try{
			ResultSet rs = clsDatabaseFunctions.openResultSet(
				SQL, 
				context,
				sDBIB,
				"MySQL",
				this.toString() + ".delete (17)");
			if(rs.next()){
				m_sErrorMessageArray.add("Error [1552074495] - this account is included on at least open PO.");
				rs.close();
				return false;
			}
			rs.close();
		}catch(SQLException e){
			m_sErrorMessageArray.add("Error [1552074496] - checking open PO's to delete GL with SQL '" + SQL + "' - "+ e.getMessage());
			return false;
		}
		
		//IC PO Receipt Lines
		SQL = "SELECT " + SMTableicporeceiptlines.TableName + "." + SMTableicporeceiptlines.lid + " FROM " + SMTableicporeceiptlines.TableName
			+ " LEFT JOIN " + SMTableicporeceiptheaders.TableName + " ON "
			+ SMTableicporeceiptlines.TableName + "." + SMTableicporeceiptlines.lreceiptheaderid + " = "
			+ SMTableicporeceiptheaders.TableName + "." + SMTableicporeceiptheaders.lid
			+ " WHERE ("
				+ "(" + SMTableicporeceiptlines.TableName + "." +SMTableicporeceiptlines.sglexpenseacct + " = '" + sGLAcct + "')"
				+ " AND (" + SMTableicporeceiptheaders.TableName + "." + SMTableicporeceiptheaders.lstatus + " != " + Integer.toString(SMTableicporeceiptheaders.STATUS_DELETED) + ")"
				+ " AND (" + SMTableicporeceiptheaders.TableName + "." + SMTableicporeceiptheaders.lpostedtoic + " = 0)"
			+ ")"
		;
		try{
			ResultSet rs = clsDatabaseFunctions.openResultSet(
				SQL, 
				context,
				sDBIB,
				"MySQL",
				this.toString() + ".delete (18)");
			if(rs.next()){
				m_sErrorMessageArray.add("Error [1552074497] - this account is included on at least open unposted IC receipts.");
				rs.close();
				return false;
			}
			rs.close();
		}catch(SQLException e){
			m_sErrorMessageArray.add("Error [1552074498] - checking IC unposted receipts to delete GL with SQL '" + SQL + "' - "+ e.getMessage());
			return false;
		}
		
		//IC Vendors
		SQL = "SELECT " + SMTableicvendors.svendoracct + " FROM " + SMTableicvendors.TableName
			+ " WHERE ("
				+ "(" + SMTableicvendors.sdefaultexpenseacct + " = '" + sGLAcct + "')"
			+ ")"
		;
		try{
			ResultSet rs = clsDatabaseFunctions.openResultSet(
				SQL, 
				context,
				sDBIB,
				"MySQL",
				this.toString() + ".delete (19)");
			if(rs.next()){
				m_sErrorMessageArray.add("Error [1552074499] - this account is used as the default expense account on at least one AP Vendor.");
				rs.close();
				return false;
			}
			rs.close();
		}catch(SQLException e){
			m_sErrorMessageArray.add("Error [1552074500] - checking the AP vendors to delete GL with SQL '" + SQL + "' - "+ e.getMessage());
			return false;
		}
		
		//Locations:
		SQL = "SELECT " + SMTablelocations.sLocation + " FROM " + SMTablelocations.TableName
			+ " WHERE ("
				+ "(" + SMTablelocations.sGLInventoryAcct + " = '" + sGLAcct + "')"
					+ " OR (" + SMTablelocations.sGLPayableClearingAcct + " = '" + sGLAcct + "')"
					+ " OR (" + SMTablelocations.sGLTransferClearingAcct + " = '" + sGLAcct + "')"
					+ " OR (" + SMTablelocations.sGLWriteOffAcct + " = '" + sGLAcct + "')"
			+ ")"
		;
		try{
			ResultSet rs = clsDatabaseFunctions.openResultSet(
				SQL, 
				context,
				sDBIB,
				"MySQL",
				this.toString() + ".delete (20)");
			if(rs.next()){
				m_sErrorMessageArray.add("Error [1552074501] - this account is included on at least one IC Location.");
				rs.close();
				return false;
			}
			rs.close();
		}catch(SQLException e){
			m_sErrorMessageArray.add("Error [1552074502] - checking IC Locations to delete GL with SQL '" + SQL + "' - "+ e.getMessage());
			return false;
		}	

		//Bank account entries
		SQL = "SELECT " + SMTablebkaccountentries.lid + " FROM " + SMTablebkaccountentries.TableName
			+ " WHERE ("
				+ "(" + SMTablebkaccountentries.icleared + " = 0)"
				+ " AND (" + SMTablebkaccountentries.sglaccount + " = '" + sGLAcct + "')"
			+ ")"
		;
		try{
			ResultSet rs = clsDatabaseFunctions.openResultSet(
				SQL, 
				context,
				sDBIB,
				"MySQL",
				this.toString() + ".delete (21)");
			if(rs.next()){
				m_sErrorMessageArray.add("Error [1552074503] - this account is used on at least one uncleared bank account entry.");
				rs.close();
				return false;
			}
			rs.close();
		}catch(SQLException e){
			m_sErrorMessageArray.add("Error [1552074504] - checking the uncleared bank account entries to delete GL with SQL '" + SQL + "' - "+ e.getMessage());
			return false;
		}
		
		//Banks
		SQL = "SELECT " + SMTablebkbanks.lid + " FROM " + SMTablebkbanks.TableName
			+ " WHERE ("
				+ "(" + SMTablebkbanks.sglaccount + " = '" + sGLAcct + "')"
			+ ")"
		;
		try{
			ResultSet rs = clsDatabaseFunctions.openResultSet(
				SQL, 
				context,
				sDBIB,
				"MySQL",
				this.toString() + ".delete (22)");
			if(rs.next()){
				m_sErrorMessageArray.add("Error [1552074505] - this account is used on at least one bank account.");
				rs.close();
				return false;
			}
			rs.close();
		}catch(SQLException e){
			m_sErrorMessageArray.add("Error [1552074506] - checking the bank accounts to delete GL with SQL '" + SQL + "' - "+ e.getMessage());
			return false;
		}
		
		//IC Account Sets
		SQL = "SELECT " + SMTableicaccountsets.sAccountSetCode + " FROM " + SMTableicaccountsets.TableName
			+ " WHERE ("
				+ "(" + SMTableicaccountsets.sAdjustmentWriteOffAccount + " = '" + sGLAcct + "')"
				+ " OR (" + SMTableicaccountsets.sInventoryAccount + " = '" + sGLAcct + "')"
				+ " OR (" + SMTableicaccountsets.sNonStockClearingAccount + " = '" + sGLAcct + "')"
				+ " OR (" + SMTableicaccountsets.sPayablesClearingAccount + " = '" + sGLAcct + "')"
				+ " OR (" + SMTableicaccountsets.sTransferClearingAccount + " = '" + sGLAcct + "')"
			+ ")"
		;
		try{
			ResultSet rs = clsDatabaseFunctions.openResultSet(
				SQL, 
				context,
				sDBIB,
				"MySQL",
				this.toString() + ".delete (23)");
			if(rs.next()){
				m_sErrorMessageArray.add("Error [1552074507] - this account is included in at least one IC account set.");
				rs.close();
				return false;
			}
			rs.close();
		}catch(SQLException e){
			m_sErrorMessageArray.add("Error [1552074508] - checking the IC account sets to delete GL with SQL '" + SQL + "' - "+ e.getMessage());
			return false;
		}
		
		//IC PO Invoice Lines:
		SQL = "SELECT " + SMTableicpoinvoicelines.TableName + "." + SMTableicpoinvoicelines.lid + " FROM " + SMTableicpoinvoicelines.TableName
			+ " LEFT JOIN " + SMTableicpoinvoiceheaders.TableName + " ON "
			+ SMTableicpoinvoicelines.TableName + "." + SMTableicpoinvoicelines.lpoinvoiceheaderid + " = "
			+ SMTableicpoinvoiceheaders.TableName + "." + SMTableicpoinvoiceheaders.lid
			+ " WHERE ("
				+ "(" + SMTableicpoinvoicelines.TableName + "." + SMTableicpoinvoicelines.sexpenseaccount + " = '" + sGLAcct + "')"
				+ " AND (" + SMTableicpoinvoiceheaders.TableName + "." + SMTableicpoinvoiceheaders.lexportsequencenumber + " = 0)"
			+ ")"
		;
		try{
			ResultSet rs = clsDatabaseFunctions.openResultSet(
				SQL, 
				context,
				sDBIB,
				"MySQL",
				this.toString() + ".delete (24)");
			if(rs.next()){
				m_sErrorMessageArray.add("Error [1552074509] - this account is included on at least open unexported IC Invoices.");
				rs.close();
				return false;
			}
			rs.close();
		}catch(SQLException e){
			m_sErrorMessageArray.add("Error [1552074510] - checking unexported IC Invoices to delete GL with SQL '" + SQL + "' - "+ e.getMessage());
			return false;
		}
		
		//finally, delete the GL:
		try{
			SQL =  "DELETE FROM " + SMTableglaccounts.TableName
					+ " WHERE " + SMTableglaccounts.sAcctID + " = '" + sGLAcct + "'"
					;
			if(!clsDatabaseFunctions.executeSQL(
					SQL, 
					context,
					sDBIB,
					"MySQL",
					this.toString() + ".delete (8)")){
				m_sErrorMessageArray.add("Error deleting GL Account");
				return false;
			}
		}catch(SQLException e){
			m_sErrorMessageArray.add("Error [1518554377] - deleting GL Account - " + e.getMessage());
			return false;
		}
		return true;
	}
	
	public static String getFindGLAccountLink(
			String sSearchingClassName, 
			String sReturnField, 
			String sParameterString, 
			ServletContext context,
			String sDBID){
			
			String m_sParameterString = sParameterString;
			
			if (m_sParameterString.startsWith("*")){
				m_sParameterString = m_sParameterString.substring(1);
			}
			
			return  
				SMUtilities.getURLLinkBase(context) + "SMClasses.ObjectFinder"
				+ "?"+ "&ObjectName=" + GLAccount.Paramobjectname
				+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
				+ "&ResultClass=FinderResults"
				+ "&SearchingClass=" + sSearchingClassName
				+ "&ReturnField=" + sReturnField
				+ "&SearchField1=" + SMTableglaccounts.sDesc
				+ "&SearchFieldAlias1=Description"
				+ "&SearchField2=" + SMTableglaccounts.sAcctID
				+ "&SearchFieldAlias2=Account%20No."
				+ "&ResultListField1=" + SMTableglaccounts.sAcctID
				+ "&ResultHeading1=Account%20No."
				+ "&ResultListField2="  + SMTableglaccounts.sDesc
				+ "&ResultHeading2=Description"
				+ "&ResultListField3="  + "IF(" + SMTableglaccounts.lActive + " = 1, 'Y', 'N')"
				+ "&ResultHeading11=Active?"
				+ "&ParameterString=*" + m_sParameterString
				;
		}
	
	public String getM_sacctid() {
		return m_sacctid;
	}
	public void setM_sacctid(String sacctid) {
		m_sacctid = sacctid.trim();
	}
	public String getM_sformattedacctid() {
		return m_sformattedacctid;
	}
	public void setM_sformattedacctid(String sformattedacctid) {
		m_sformattedacctid = sformattedacctid.trim();
	}
	public String getM_sdescription() {
		return m_sdescription;
	}
	public void setM_sdescription(String sdescription) {
		m_sdescription = sdescription.trim();
	}
	public String getM_stype() {
		return m_stype;
	}
	public void setM_stype(String stype) {
		m_stype = stype.trim();
	}
	public String getM_sactive() {
		return m_sactive;
	}
	public void setM_sactive(String sactive) {
		m_sactive = sactive.trim();
	}
	public String getM_scostcenterid() {
		return m_icostcenterid;
	}
	public void setM_iallowpoasexpense(String siallowpoasexpense) {
		m_iallowaspoexpense = siallowpoasexpense.trim();
	}
	public String getM_iallowpoasexpense() {
		return m_iallowaspoexpense;
	}
	public void setM_scostcenterid(String scostcenterid) {
		m_icostcenterid = scostcenterid.trim();
	}
	public void setM_laccountstructureid(String slaccountstructureid) {
		m_laccountstructureid = slaccountstructureid.trim();
	}
	public String getM_laccountstructureid() {
		return m_laccountstructureid;
	}
	public void setM_laccountgroupid(String slaccountgroupid) {
		m_laccountgroupid = slaccountgroupid.trim();
	}
	public String getM_laccountgroupid() {
		return m_laccountgroupid;
	}
	public void setsbdannualbudget(String sbdannualbudget) {
		m_sbdannualbudget = sbdannualbudget.trim();
	}
	public String getsbdannualbudget() {
		return m_sbdannualbudget;
	}
	public void setsinormalbalancetype(String sinormalbalancetype) {
		m_inormalbalancetype = sinormalbalancetype.trim();
	}
	public String getsinormalbalancetype() {
		return m_inormalbalancetype;
	}
	public String getM_iNewRecord() {
		return m_iNewRecord;
	}
	public void setM_bNewRecord(String newRecord) {
		m_iNewRecord = newRecord;
	}
	public String getErrorMessageString(){
		String s = "";
		for (int i = 0; i < m_sErrorMessageArray.size(); i ++){
			s += "\n" + m_sErrorMessageArray.get(i);
		}
		return s;
	}
}