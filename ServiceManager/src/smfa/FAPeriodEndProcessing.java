package smfa;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

import SMClasses.SMModuleTypes;
import SMClasses.SMOption;
import SMDataDefinition.SMTablefamaster;
import SMDataDefinition.SMTablefaoptions;
import SMDataDefinition.SMTablefatransactions;
import SMDataDefinition.SMTablesmoptions;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsDateAndTimeConversions;
import ServletUtilities.clsServletUtilities;
import smar.AROptions;
import smgl.GLTransactionBatch;
import smgl.SMGLExport;

public class FAPeriodEndProcessing extends java.lang.Object{

	//private static boolean bDebugMode = false;
	public static SimpleDateFormat sdfTime = new SimpleDateFormat("hhmmss");
	public static SimpleDateFormat sdfNormalDate = new SimpleDateFormat("MM/dd/yyyy");

	private String m_sFiscalYear = "";
	private String m_sFiscalPeriod = "";
	private String m_sTransactionDate = sdfNormalDate.format(new Date(System.currentTimeMillis()));
	private boolean m_bProvisional = true;
	private boolean m_bConsolidate = true;
	private ArrayList<String> m_sErrorMessageArray = new ArrayList<String>(0);
	private ArrayList<String> m_sDetailAccts = new ArrayList<String>(0);
	private ArrayList<BigDecimal> m_bdDetailAmts = new ArrayList<BigDecimal>(0);
	private ArrayList<String> m_sDetailComments = new ArrayList<String>(0);
	private ArrayList<String> m_sDetailReferences = new ArrayList<String>(0);

	private SMGLExport m_cGLExportBatch = new SMGLExport();

	public FAPeriodEndProcessing(){
		m_sFiscalYear = "";
		m_sFiscalPeriod = "";
		m_sTransactionDate = sdfNormalDate.format(new Date(System.currentTimeMillis()));
		m_bProvisional = true;
		m_bConsolidate = true;
	}

	public void doProcess(
		String sUserID,
		String sUserFullName,
		Connection conn)throws Exception{

		long lRecordNumber = 0;
		String sProvisionalDescription = "";
		BigDecimal bdDepAmt = BigDecimal.ZERO;
		String sSQL = "";
		String sInfo = "";

		//Make sure that this fiscal year and period have NOT already been run:
		sSQL = "SELECT * FROM " + SMTablefatransactions.TableName
			+ " WHERE ("
				+ "(" + SMTablefatransactions.iFiscalPeriod + " = " + m_sFiscalPeriod + ")"
				+ " AND (" + SMTablefatransactions.iFiscalYear + " = " + m_sFiscalYear + ")"
			+ ")"
		;
		ResultSet rsTransactions = clsDatabaseFunctions.openResultSet(sSQL, conn);
		if (rsTransactions.next()){
			rsTransactions.close();
			throw new Exception("Error [1530305547] - depreciation transactions for fiscal year " + m_sFiscalYear + " and period " + m_sFiscalPeriod
				+ " are already recorded in the system - this fiscal year and period must have already been processed."
			);
		}
		rsTransactions.close();
		
		//First check that the transaction date is within the allowed posting period:
		SMOption opt = new SMOption();
		if (!opt.load(conn)){
			throw new Exception("Error [1457642862] - could not check posting period - " + opt.getErrorMessage() + ".");
		}
		try {
			opt.checkDateForPosting(
				m_sTransactionDate, 
				"Fixed Assets Period End TRANSACTION DATE", 
				conn, 
				sUserID
			);
		} catch (Exception e2) {
			throw new Exception(e2.getMessage());
		}
		
		//Get the asset count
		sSQL = "SELECT COUNT(*) FROM " + SMTablefamaster.TableName;
		ResultSet rsAssetCount = clsDatabaseFunctions.openResultSet(sSQL, conn);
		if (rsAssetCount.next()){
			lRecordNumber = rsAssetCount.getLong("COUNT(*)");
		}else{
			lRecordNumber = 0;
		}
		rsAssetCount.close();
		Delete_Provisional_Postings(conn);
		
		//Get a list of all the 'eligible' assets, meaning the ones with NO transactions in this period ('TRANSQUERY'), 
		// and having a current value still greater than the salvage value, and finally having no date sold
		// (i.e., not having been sold off)
		sSQL = "SELECT"
			+ " " + SMTablefamaster.sAssetNumber
			+ " FROM"
			+ " " + SMTablefamaster.TableName
			+ " LEFT JOIN " 
			+ "("
			+ " SELECT " + SMTablefatransactions.sTransAssetNumber + " AS TRANSASSET FROM "+ SMTablefatransactions.TableName
			+ " WHERE ("
				+ "(" + SMTablefatransactions.iFiscalPeriod + " = " + m_sFiscalPeriod + ")"
				+ " AND (" + SMTablefatransactions.iFiscalYear + " = " + m_sFiscalYear + ")"
				+ " AND (" + SMTablefatransactions.sTransactionType + " = '" + SMTablefatransactions.DEPRECIATION_FLAG + "')"
			+ ")"
			+ ") AS TRANSQUERY"
			+ " ON " + SMTablefamaster.sAssetNumber + " = TRANSQUERY.TRANSASSET"
			+ " WHERE ("
				+ "(" + SMTablefamaster.bdCurrentValue + " > " + SMTablefamaster.bdSalvageValue + ")"
				+ " AND (" + SMTablefamaster.datDateSold + " = '0000-00-00')"
				+ " AND (TRANSQUERY.TRANSASSET IS NULL)"
			+ ")"
			+ " ORDER BY" 
			+ " " + SMTablefamaster.sAssetNumber;
		ResultSet rsAssets = clsDatabaseFunctions.openResultSet(sSQL, conn);
		
    	String SQL = "SELECT * FROM " + SMTablesmoptions.TableName;
    	try{
	    	ResultSet rsOptions = clsDatabaseFunctions.openResultSet(SQL, conn);
	    	
	    	if (rsOptions.next()){
	    		m_cGLExportBatch.setExportFilePath(rsOptions.getString(SMTablesmoptions.sfileexportpath));
	    	}else{
	    		throw new Exception("Could not get SMOption record to read export file using SQL: " + SQL);
	    	}
	    	rsOptions.close();
    	}catch (SQLException e){
    		throw new Exception("Error reading path for export file: " + e.getMessage());
    	}
		lRecordNumber = 0;
		long lNumberOfAssetsDepreciated = 0;
		ServletUtilities.clsDBServerTime clsCurrentTime = new ServletUtilities.clsDBServerTime(conn);
		
		//Create a GL export batch for the transactions:
		if (m_bProvisional){
			if (!m_cGLExportBatch.addHeader(SMModuleTypes.FA, 
					"SS",
					"PROVISIONAL Depreciation",
					"PROVISIONAL Processing",
					clsCurrentTime.getCurrentDateTimeInSelectedFormat(clsServletUtilities.DATE_FORMAT_FOR_DISPLAY),
					clsCurrentTime.getCurrentDateTimeInSelectedFormat(clsServletUtilities.DATE_FORMAT_FOR_DISPLAY),
					"Monthly Depreciation",
					"0"
					)){
				throw new Exception("Error setting export batch file header - " + m_cGLExportBatch.getErrorMessage()); 
			}
			sProvisionalDescription = "PROVISIONAL Depreciation";
		}else{
			if (!m_cGLExportBatch.addHeader(SMModuleTypes.FA, 
					"SS",
					"Periodic Depreciation",
					"Periodic Processing",
					clsCurrentTime.getCurrentDateTimeInSelectedFormat(clsServletUtilities.DATE_FORMAT_FOR_DISPLAY),
					clsCurrentTime.getCurrentDateTimeInSelectedFormat(clsServletUtilities.DATE_FORMAT_FOR_DISPLAY),
					"Monthly Depreciation",
					"0"
					)){
				throw new Exception("Error setting export batch file header - " + m_cGLExportBatch.getErrorMessage()); 
			}
			sProvisionalDescription = "Monthly Depreciation";
		}

		//Now calculate the depreciation amount for each eligible asset:
		while (rsAssets.next()){
			FAAsset asset = null;
			try{
				sInfo = "loading asset info";
				asset = new FAAsset(rsAssets.getString(SMTablefamaster.sAssetNumber).trim());
				if (!asset.load(conn)){
					throw new Exception("Could not load asset '" + rsAssets.getString(SMTablefamaster.sAssetNumber).trim() + "' - " + asset.getErrorMessageString());
				}
				sInfo = "depreciating asset";
				bdDepAmt = asset.Depreciate_Asset(Integer.parseInt(m_sFiscalYear), 
						Integer.parseInt(m_sFiscalPeriod), 
						m_bProvisional, 
						clsDateAndTimeConversions.StringTojavaSQLDate("MM/dd/yyyy", m_sTransactionDate), 
						sUserID,
						conn);
			}catch (Exception e){
				m_sErrorMessageArray.add("Error " + sInfo + " " + asset.getAssetNumber().trim() + ". <BR>" + e.getMessage());
				continue;
			}

			if (bdDepAmt.compareTo(BigDecimal.ZERO) != 0){
				lNumberOfAssetsDepreciated++;
				//Add the debit:
				if (!m_bConsolidate){
					//System.out.println("not consolidated");
					m_cGLExportBatch.addDetail(clsDateAndTimeConversions.StringTojavaSQLDate("MM/dd/yyyy", m_sTransactionDate), 
							bdDepAmt, 
							asset.getDepreciationGLAcct(), 
							"Depreciation Acct", 
							asset.getAssetNumber() + "", 
							sProvisionalDescription,
							"0",
							conn);

					//Add the credit:
					m_cGLExportBatch.addDetail(clsDateAndTimeConversions.StringTojavaSQLDate("MM/dd/yyyy", m_sTransactionDate), 
							bdDepAmt.negate(), 
							asset.getAccumulatedDepreciationGLAcct(), 
							"Accumulated Depreciation Acct", 
							asset.getAssetNumber() + "", 
							sProvisionalDescription,
							"0",
							conn);
					//If, instead, the user chose to consolidate,
					//then add an entry to the 'consolidated' array:
				}else{
					//System.out.println("consolidated");
					//Depreciation Acct
					Add_Array_Detail(bdDepAmt, 
							asset.getDepreciationGLAcct(), 
							"Depreciation Acct", 
							sProvisionalDescription);

					//Accumulated depreciation account
					Add_Array_Detail (bdDepAmt.negate(), 
							asset.getAccumulatedDepreciationGLAcct(), 
							"Accumulated Depreciation Acct", 
							sProvisionalDescription);

				}
			}
			lRecordNumber++; //total processed asset counter. 
		}
		rsAssets.close();
		//System.out.println(lRecordNumber + " assets processed.");

		if (m_bConsolidate){
			for (int i=0;i<m_sDetailAccts.size();i++){
				m_cGLExportBatch.addDetail(clsDateAndTimeConversions.StringTojavaSQLDate("MM/dd/yyyy", m_sTransactionDate), 
						m_bdDetailAmts.get(i), 
						m_sDetailAccts.get(i), 
						m_sDetailComments.get(i), 
						"Consolidated entry", 
						m_sDetailReferences.get(i),
						"0",
						conn);
			}
		}

		//TJR - 9/23/2016 - for now, we'll use the AR export type for FA as well:
		AROptions aropt = new AROptions();
		if(!aropt.load(conn)){
			throw new Exception("Error [1474646217] getting export file type - " + aropt.getErrorMessageString()
				+ " - make sure that the export file type is set in the 'AR Edit Accounts Receivable Options (1099)' menu."
			); 
		}
		int iExportFileType;
		try {
			iExportFileType = Integer.parseInt(aropt.getExportTo());
		} catch (Exception e) {
			throw new Exception("Error [1477688454] converting AR Options export type '" +aropt.getExportTo() + "' to integer - "
				+ e.getMessage()); 
		}
		//For FA transactions, since there IS no batch processing, we use the fiscal year and period as our 'batchnumber':
		String sBatchNumber = Long.toString((Long.parseLong(m_sFiscalYear.trim()) * 100) + Long.parseLong(m_sFiscalPeriod));
		if (!m_bProvisional){
			try {
				m_cGLExportBatch.saveExport(sBatchNumber, conn);
			} catch (Exception e1) {
				m_sErrorMessageArray.add("Error saving GL export records: " + e1.getMessage());
			}
			
			//Get the GL Feed info and then create GL batches accordingly:
			int iFeedGL = 0;
			String faSQL = "SELECT"
				+ " * FROM " + SMTablefaoptions.TableName
			;
			try {
				ResultSet rsFAOptions = clsDatabaseFunctions.openResultSet(faSQL, conn);
				if (rsFAOptions.next()){
					iFeedGL = rsFAOptions.getInt(SMTablefaoptions.ifeedgl);
				}else{
					rsFAOptions.close();
					throw new Exception("Error [1557501017] could not get FA Options record."); 
				}
				rsFAOptions.close();
			} catch (Exception e) {
				throw new Exception("Error [1557501018] reading FA Options record - " + e.getMessage());
			}
			
			if (
				(iFeedGL == SMTablefaoptions.FEED_GL_BOTH_EXTERNAL_AND_SMCP_GL)
				|| (iFeedGL == SMTablefaoptions.FEED_GL_SMCP_GL_ONLY)
			){
				try {
					GLTransactionBatch gltransactionbatch = m_cGLExportBatch.createGLTransactionBatch(
						conn, 
						sUserID, 
						sUserID, 
						clsCurrentTime.getCurrentDateTimeInSelectedFormat(clsServletUtilities.DATE_FORMAT_FOR_DISPLAY), 
						"FA Periodic Depreciation Batch #" + sBatchNumber
					);

					gltransactionbatch.save_without_data_transaction(conn, sUserID, sUserFullName, true);
				} catch (Exception e) {
					throw new Exception("Error [1557501019] could not create GL batch for SMCP - " + e.getMessage());
				}
			}
			
			if (
				(iFeedGL == SMTablefaoptions.FEED_GL_BOTH_EXTERNAL_AND_SMCP_GL)
				|| (iFeedGL == SMTablefaoptions.FEED_GL_EXTERNAL_GL_ONLY)
			){			
		        if (m_cGLExportBatch.getExportFilePath().compareToIgnoreCase("") != 0){
			        if (!m_cGLExportBatch.writeExportFile(
			        		SMModuleTypes.FA, 
			        		"DEPRECIATION", 
			        		sBatchNumber,
			        		iExportFileType,
			        		conn)
			        	){
			        	throw new Exception("Error writing GL export file to " 
			        	+ m_cGLExportBatch.getExportFilePath() + " - " + m_cGLExportBatch.getErrorMessage());
			        }
			    }
			}
		}

		//TESTING ONLY!!!!
		//m_sErrorMessageArray.add("Just testing the rollback....");
		
		if (m_sErrorMessageArray.size() > 0){
			String sError = "";
			for (int i=0;i<m_sErrorMessageArray.size();i++){
				sError += m_sErrorMessageArray.get(i) + "<BR>";
			}
			throw new Exception(sError);
		}

		//Store the status in the error message array to be displayed on the screen:
		m_sErrorMessageArray.add(Long.toString(lRecordNumber) + " total assets were checked - " 
			+ Long.toString(lNumberOfAssetsDepreciated) + " assets generated depreciation transactions.");
		return;
	}

	private void Delete_Provisional_Postings(Connection conn) throws SQLException{

		String sSQL = "DELETE FROM " + SMTablefatransactions.TableName + 
				" WHERE" + 
				" " + SMTablefatransactions.iProvisionalPosting + "=1";
		try {
			Statement stmt = conn.createStatement();
			stmt.execute(sSQL);
		} catch (Exception e) {
			throw new SQLException("Error deleting provisional postings - " + e.getMessage());
		}
	}

	private void Add_Array_Detail(BigDecimal bdDepAmt,
			String sGLAcct,
			String sComment,
			String sReference
			){
		int iMatchingArrayItem;
		//First, check to see if this account has already been added:
		iMatchingArrayItem = Get_Matching_Array_Detail(sGLAcct);

		if (iMatchingArrayItem < 0){
			m_sDetailAccts.add(sGLAcct);
			m_bdDetailAmts.add(bdDepAmt);
			m_sDetailComments.add(sComment);
			m_sDetailReferences.add(sReference);
		}else{
			m_bdDetailAmts.set(iMatchingArrayItem, m_bdDetailAmts.get(iMatchingArrayItem).add(bdDepAmt));
		}		
	}

	private int Get_Matching_Array_Detail(String sGLAcct){

		for (int i=0;i<m_sDetailAccts.size();i++){
			if (m_sDetailAccts.get(i).compareTo(sGLAcct) == 0){
				return i;
			}
		}
		return -1;
	}
	public void setFiscalYear(String s){
		m_sFiscalYear = s;
	}

	public String getFiscalYear(){
		return m_sFiscalYear;
	}

	public void setFiscalPeriod(String s){
		m_sFiscalPeriod = s;
	}

	public String getFiscalPeriod(){
		return m_sFiscalPeriod;
	}

	public void setTransactionDate(String s){
		m_sTransactionDate = s;
	}

	public String getTransactionDate(){
		return m_sTransactionDate;
	}

	public void setProvisional(boolean b){
		m_bProvisional = b;
	}

	public boolean getProvisional(){
		return m_bProvisional;
	}

	public void setConsolidateGLBatchDetails(boolean b){
		m_bConsolidate = b;
	}

	public boolean getConsolidateGLBatchDetails(){
		return m_bConsolidate;
	}

	public String getErrorMessageString(){
		String s = "";
		for (int i = 0; i < m_sErrorMessageArray.size(); i ++){
			s += "<BR>" + m_sErrorMessageArray.get(i);
		}
		return s;
	}

	public String getQueryString(){

		String sQueryString = "";
		sQueryString += "FISCALYEAR=" + clsServletUtilities.URLEncode(m_sFiscalYear);
		sQueryString += "&FISCALPERIOD=" + clsServletUtilities.URLEncode(m_sFiscalPeriod);
		sQueryString += "&TRANSACTIONDATE=" + clsServletUtilities.URLEncode(m_sTransactionDate);
		if (m_bProvisional){
			sQueryString += "&PROVISIONALPOSTING=1";
		}
		if (m_bConsolidate){
			sQueryString += "&CONSOLIDATE=1";
		}

		return sQueryString;
	}

}
