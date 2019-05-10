package smar;

import java.io.PrintWriter;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import javax.servlet.ServletContext;

import SMClasses.SMBatchStatuses;
import SMClasses.SMBatchTypes;
import SMClasses.SMEntryBatch;
import SMClasses.SMLogEntry;
import SMClasses.SMModuleTypes;
import SMDataDefinition.SMTableapbatches;
import SMDataDefinition.SMTableapoptions;
import SMDataDefinition.SMTablearcustomerstatistics;
import SMDataDefinition.SMTablearmatchingline;
import SMDataDefinition.SMTablearmonthlystatistics;
import SMDataDefinition.SMTablearoptions;
import SMDataDefinition.SMTableartransactions;
import SMDataDefinition.SMTablebkaccountentries;
import SMDataDefinition.SMTableentries;
import SMDataDefinition.SMTableentrylines;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsDateAndTimeConversions;
import ServletUtilities.clsManageBigDecimals;
import ServletUtilities.clsServletUtilities;
import smgl.GLFiscalPeriod;
import smgl.GLSourceLedgers;
import smgl.GLTransactionBatch;
import smgl.GLTransactionBatchEntry;
import smgl.GLTransactionBatchLine;

public class ARBatch extends SMClasses.SMEntryBatch{
	private SMLogEntry log;
	private ARChronLog archron;
	private boolean bLogDiagnostics = false;
	private static final boolean bDebugMode = false;
	ARBatch(
			String sBatchNumber
	){

		super(sBatchNumber);
	}
	public void post_with_data_transaction(
			ServletContext context,
			String sDBID,
			String sUserID,
			String sUserFullName,
			PrintWriter out
	) throws Exception{

		log = new SMLogEntry(sDBID, context);

		super.clearErrorMessages();

		if (this.iBatchStatus() == SMBatchStatuses.POSTED){
			throw new Exception("This batch was already posted.");
		}
		if (this.iBatchStatus() == SMBatchStatuses.DELETED){
			throw new Exception("This batch is deleted - it cannot be posted.");
		}

		Connection conn = clsDatabaseFunctions.getConnection(
				context, 
				sDBID, 
				"MySQL",
				this.toString() 
				+ ".post_with_data_transaction - User: " 
				+ sUserID
				+ " - "
				+ sUserFullName
		);
		//First check to make sure no one else is posting:
		try{
			String SQL = "SELECT * FROM " + SMTablearoptions.TableName;
			ResultSet rsAROptions = clsDatabaseFunctions.openResultSet(SQL, conn);
			if (!rsAROptions.next()){
				clsDatabaseFunctions.freeConnection(context, conn, "[1547067489]");
				throw new Exception("Error getting aroptions record - no record.");
			}else{
				if(rsAROptions.getLong(SMTablearoptions.ibatchpostinginprocess) == 1){
					String sError = "A previous posting is not completed - "
							+ rsAROptions.getString(SMTablearoptions.suserfullname) + " has been "
							+ rsAROptions.getString(SMTablearoptions.sprocess) + " "
							+ "since " + rsAROptions.getString(SMTablearoptions.datstartdate) + ".";
					log.writeEntry(
							sUserID, 
							SMLogEntry.LOG_OPERATION_ARBATCHPOST, 
							sError,
							"",
							"[1376509260]");
					rsAROptions.close();
					clsDatabaseFunctions.freeConnection(context, conn, "[1547067490]");
					throw new Exception(sError);
				}
			}
			rsAROptions.close();
		}catch (SQLException e){
			clsDatabaseFunctions.freeConnection(context, conn, "[1547067491]");
			throw new Exception("Error checking for previous posting - " + e.getMessage());
		}
		//If not, then set the posting flag:
		try{
			String SQL = "UPDATE " + SMTablearoptions.TableName 
			+ " SET " + SMTablearoptions.ibatchpostinginprocess + " = 1"
			+ ", " + SMTablearoptions.datstartdate + " = NOW()"
			+ ", " + SMTablearoptions.sprocess 
			+ " = 'POSTING AR BATCH NUMBER " + super.lBatchNumber() + "'"
			+ ", " + SMTablearoptions.suserfullname + " = '" + sUserFullName + "'"
			;
			
			Statement stmt = conn.createStatement();
			stmt.execute(SQL);
		}catch (SQLException e){
			clsDatabaseFunctions.freeConnection(context, conn, "[1547067492]");
			throw new Exception("Error setting posting flag in aroptions - " + e.getMessage());
		}

		if(!clsDatabaseFunctions.start_data_transaction(conn)){
			//Clear the posting flag:
			try{
				String SQL = "UPDATE " + SMTablearoptions.TableName 
				+ " SET " + SMTablearoptions.ibatchpostinginprocess + " = 0"
				+ ", " + SMTablearoptions.datstartdate + " = '0000-00-00 00:00:00'"
				+ ", " + SMTablearoptions.sprocess + " = ''"
				+ ", " + SMTablearoptions.suserfullname + " = ''"
				;
				
				Statement stmt = conn.createStatement();
				stmt.execute(SQL);
				
			}catch (SQLException e){
				clsDatabaseFunctions.freeConnection(context, conn, "[1547067493]");
				throw new Exception("Error [1457961244] clearing posting flag in aroptions - " + e.getMessage() 
					+ "-  could not start data transaction");
			}

			clsDatabaseFunctions.freeConnection(context, conn, "[1547067494]");
			throw new Exception("Error [1457961243] - could not start data transaction");
		}

		//Instantiate the archron log:
		archron = new ARChronLog(conn);

		try {
			post_without_data_transaction(conn, sUserID, sUserFullName);
		} catch (Exception e1) {
			
			//Clear the posting flag:
			clsDatabaseFunctions.rollback_data_transaction(conn);
			String SQL = "UPDATE " + SMTablearoptions.TableName 
			+ " SET " + SMTablearoptions.ibatchpostinginprocess + " = 0"
			+ ", " + SMTablearoptions.datstartdate + " = '0000-00-00 00:00:00'"
			+ ", " + SMTablearoptions.sprocess + " = ''"
			+ ", " + SMTablearoptions.suserfullname + " = ''"
			;
			
			try {
				Statement stmt = conn.createStatement();
				stmt.execute(SQL);
			} catch (Exception e) {
				clsDatabaseFunctions.freeConnection(context, conn, "[1547067495]");
				throw new Exception("Error [1457961309] clearing posting flag in aroptions - " + e.getMessage()
					+ " - " + e1.getMessage()
				);
			}
			
			clsDatabaseFunctions.freeConnection(context, conn, "[1547067496]");
			throw new Exception("Error [1457961319] posting - " + e1.getMessage());
		}

		clsDatabaseFunctions.commit_data_transaction(conn);
		String SQL = "UPDATE " + SMTablearoptions.TableName 
		+ " SET " + SMTablearoptions.ibatchpostinginprocess + " = 0"
		+ ", " + SMTablearoptions.datstartdate + " = '0000-00-00 00:00:00'"
		+ ", " + SMTablearoptions.sprocess + " = ''"
		+ ", " + SMTablearoptions.suserfullname + " = ''"
		;

		try {
			Statement stmt = conn.createStatement();
			stmt.execute(SQL);
		} catch (Exception e) {
			clsDatabaseFunctions.freeConnection(context, conn, "[1547067497]");
			throw new Exception("Error [1457961580] clearing posting flag in aroptions - " + e.getMessage());
		}

		clsDatabaseFunctions.freeConnection(context, conn, "[1547067498]");
		return;
	}

	//NOTE: This function is ONLY used for 
	//debugging - it is never called in the real
	//program:
	/*
	private boolean post_with_data_transaction (
			Connection conn,
			Connection logConn,
			String sUserName,
			boolean bCommitTransaction
	){

		log = new SMLogEntry(logConn);
		archron = new ARChronLog(conn);
		super.sLastEditedBy(sUserName);
		super.clearErrorMessages();

		//First check to make sure no one else is posting:
		try{
			String SQL = "SELECT * FROM " + SMTablearoptions.TableName;
			ResultSet rsAROptions = ARUtilities.openResultSet(SQL, conn);
			if (!rsAROptions.next()){
				super.addErrorMessage("Error getting aroptions record");
				//System.out.println("In ARBatch.post: Error getting aroptions record");
				return false;
			}else{
				if(rsAROptions.getLong(SMTablearoptions.ibatchpostinginprocess) == 1){
					String sError = "A previous posting is not completed - "
							+ rsAROptions.getString(SMTablearoptions.suser) + " has been "
							+ rsAROptions.getString(SMTablearoptions.sprocess) + " "
							+ "since " + rsAROptions.getString(SMTablearoptions.datstartdate) + ".";
					super.addErrorMessage(sError);
					log.writeEntry(
							sUserName, 
							SMLogEntry.LOG_OPERATION_ARBATCHPOST, 
							sError,
							"",
							"[1376509259]");
					return false;
				}
			}
			rsAROptions.close();
		}catch (SQLException e){
			super.addErrorMessage("Error checking for previous posting - " + e.getMessage());
			//System.out.println("Error checking for previous posting - " + e.getMessage());
			return false;
		}
		//If not, then set the posting flag:
		try{
			String SQL = "UPDATE " + SMTablearoptions.TableName 
			+ " SET " + SMTablearoptions.ibatchpostinginprocess + " = 1"
			+ ", " + SMTablearoptions.datstartdate + " = NOW()"
			+ ", " + SMTablearoptions.sprocess 
			+ " = 'POSTING AR BATCH NUMBER " + super.lBatchNumber() + "'"
			+ ", " + SMTablearoptions.suser 
			+ " = '" + sUserName + "'"
			;
			if (!ARUtilities.executeSQL(SQL, conn)){
				super.addErrorMessage("Error setting posting flag in aroptions");
				//System.out.println("In ARBatch.post: Error setting posting flag in aroptions");
				return false;
			}
		}catch (SQLException e){
			super.addErrorMessage("Error setting posting flag in aroptions - " + e.getMessage());
			//System.out.println("Error setting posting flag in aroptions - " + e.getMessage());
			return false;
		}

		if(!ARUtilities.start_data_transaction(conn)){
			//Clear the posting flag:
			try{
				String SQL = "UPDATE " + SMTablearoptions.TableName 
				+ " SET " + SMTablearoptions.ibatchpostinginprocess + " = 0"
				+ ", " + SMTablearoptions.datstartdate + " = '0000-00-00 00:00:00'"
				+ ", " + SMTablearoptions.sprocess + " = ''"
				+ ", " + SMTablearoptions.suser + " = ''"
				;
				if (!ARUtilities.executeSQL(SQL, conn)){
					super.addErrorMessage("Error clearing posting flag in aroptions");
					//System.out.println("In ARBatch.post: Error clearing posting flag in aroptions");
					return false;
				}
			}catch (SQLException e){
				super.addErrorMessage("Error clearing posting flag in aroptions - " + e.getMessage());
				//System.out.println("Error clearing posting flag in aroptions - " + e.getMessage());
				return false;
			}

			//Clear the posting flag, then return
			return false;
		}

		try {
			post_without_data_transaction(conn, sUserName);
		} catch (Exception e1) {
			ARUtilities.rollback_data_transaction(conn);
			String SQL = "UPDATE " + SMTablearoptions.TableName 
			+ " SET " + SMTablearoptions.ibatchpostinginprocess + " = 0"
			+ ", " + SMTablearoptions.datstartdate + " = '0000-00-00 00:00:00'"
			+ ", " + SMTablearoptions.sprocess + " = ''"
			+ ", " + SMTablearoptions.suser + " = ''"
			;
		
			try{
			Statement stmt = conn.createStatement();
			stmt.execute(SQL);
			} catch (Exception e){
				super.addErrorMessage("Error clearing posting flag in aroptions");
				return false;
			}
			//Clear the posting flag, then return
			return false;
		}
		
		ARUtilities.commit_data_transaction(conn);
		String SQL = "UPDATE " + SMTablearoptions.TableName 
		+ " SET " + SMTablearoptions.ibatchpostinginprocess + " = 0"
		+ ", " + SMTablearoptions.datstartdate + " = '0000-00-00 00:00:00'"
		+ ", " + SMTablearoptions.sprocess + " = ''"
		+ ", " + SMTablearoptions.suser + " = ''"
		;

		try {
			Statement stmt = conn.createStatement();
			stmt.execute(SQL);
		} catch (Exception e) {
			super.addErrorMessage("Error clearing posting flag in aroptions");
			return false;
		}
		//Clear the posting flag, then return
		return true;
	}
*/
	public void post_without_data_transaction(Connection conn,  String sUserID, String sUserFullName) throws Exception{

		//Check all of the entries first to make sure they can be posted:
		String SQL = "SELECT *"
				+ " FROM " + SMTableentries.TableName
				+ " WHERE (" 
				+ SMTableentries.ibatchnumber + " = " + super.sBatchNumber()
				+ ")"
				+ " ORDER BY " + SMTableentries.ientrynumber + " ASC";
		//boolean bBatchPassed = true;
		if (bLogDiagnostics){
			log.writeEntry(
				sUserID, 
				SMLogEntry.LOG_OPERATION_ARBATCHPOST, 
				"Entering post_without_data_transaction", "Batch #:" + super.sBatchNumber(),
				"[1376509261]");
		}
		long lEntryCount = 0;
		String sCheckEntryErrors = "";
		try {
			ResultSet rsAscendingEntryList = clsDatabaseFunctions.openResultSet(SQL, conn);
			
			while (rsAscendingEntryList.next()){
				//Check each entry
				try {
					checkEntry(
						Long.toString(rsAscendingEntryList.getLong(SMTableentries.ientrynumber)),
						Long.toString(rsAscendingEntryList.getLong(SMTableentries.lid)), 
						conn,
						sUserID);
				} catch (Exception e) {
					sCheckEntryErrors+= e.getMessage() + "<BR>";
				}
				lEntryCount ++;
			}
			rsAscendingEntryList.close();
		}catch (SQLException e){
			throw new Exception("Error opening entry list result set - " + e.getMessage());
		}

		//If the batch didn't pass, just return false:
		if (sCheckEntryErrors.compareToIgnoreCase("") != 0){
			throw new Exception("Errors found checking batch - " + sCheckEntryErrors);
		}
		
		//If there are not any entries, don't post
		if (lEntryCount == 0){
			throw new Exception("Batch cannot be posted with no entries.");
		}

		if (bLogDiagnostics){
			log.writeEntry(
				sUserID, 
				SMLogEntry.LOG_OPERATION_ARBATCHPOST, 
				"In post_without_data_transaction Batch #:" + super.sBatchNumber(), "Passed check entries",
				"[1376509262]");
		}
		//Store the last artransaction id so we can use it to limit our lists later:
		long lLastArTransactionID = 0;
		SQL = "SELECT " + SMTableartransactions.lid + " FROM " + SMTableartransactions.TableName
		+ " ORDER BY " + SMTableartransactions.lid + " DESC LIMIT 1";

		try{
			ResultSet rsLastTransactionID = clsDatabaseFunctions.openResultSet(SQL, conn);
			if(rsLastTransactionID.next()){
				lLastArTransactionID = rsLastTransactionID.getLong(SMTableartransactions.lid);
			}
			rsLastTransactionID.close();
		}catch(SQLException e){
			throw new Exception("Error getting highest artransaction ID - " + e.getMessage());
		}

		//Next, create artransactions for all of the entries:
		if (bLogDiagnostics){
			log.writeEntry(
				sUserID, 
				"ARBATCHPOST", 
				"In post_without_data_transaction Batch #:" + super.sBatchNumber(), "Going into createTransactions",
				"[1376509513]");
		}
		SQL = "SELECT *"
			+ " FROM " + SMTableentries.TableName
			+ " WHERE (" 
			+ SMTableentries.ibatchnumber + " = " + super.sBatchNumber()
			+ ")"
			+ " ORDER BY " + SMTableentries.ientrynumber + " ASC";
		try {
			ResultSet rsCreateTransactions = clsDatabaseFunctions.openResultSet(SQL, conn);

			while (rsCreateTransactions.next()){
				//Process each entry
				if (bLogDiagnostics){
        			log.writeEntry(
        				sUserID, 
        				SMLogEntry.LOG_OPERATION_ARBATCHPOST, 
        				"In post_without_data_transaction Batch #:" + super.sBatchNumber(),
        				"Create entry for entry #" + Long.toString(rsCreateTransactions.getLong(SMTableentries.ientrynumber)),
        				"[1376509264]"
        			);
        		}
				
				try{
					createTransaction(
						Long.toString(rsCreateTransactions.getLong(SMTableentries.ientrynumber)),
						Long.toString(rsCreateTransactions.getLong(SMTableentries.lid)), 
						conn,
						sUserID,
						sUserFullName
						);
				} catch (Exception e){
					rsCreateTransactions.close();
					throw new Exception(e.getMessage());
				}
			}
			rsCreateTransactions.close();

		}catch (SQLException e){
			throw new Exception("Error opening entry list result set - " + e.getMessage());
		}

		//Store the last armatchingline id so we can use it to limit our lists later:
		long lLastArMatchingLineID = 0;
		SQL = "SELECT " + SMTablearmatchingline.lid + " FROM " + SMTablearmatchingline.TableName
		+ " ORDER BY " + SMTablearmatchingline.lid + " DESC LIMIT 1";

		try{
			ResultSet rsLastMatchingLine = clsDatabaseFunctions.openResultSet(SQL, conn);
			if(rsLastMatchingLine.next()){
				lLastArMatchingLineID = rsLastMatchingLine.getLong(SMTablearmatchingline.lid);
			}
			rsLastMatchingLine.close();
		}catch (Exception e){
			throw new Exception("Error getting highest armatchingline ID - " + e.getMessage());
		}

		if (bLogDiagnostics){
			log.writeEntry(
				sUserID, 
				SMLogEntry.LOG_OPERATION_ARBATCHPOST,
				"In post_without_data_transaction Batch #:" + super.sBatchNumber(), 
				"Going into processApplyingLines, Last AR Matching Line ID = " + Long.toString(lLastArMatchingLineID),
				"[1376509514]"
				);
		}

		//create armatchinglines from all the APPLYING lines in the batch - this does NOT include apply-to
		//entries, which are handled differently below:
		try {
			processApplyingLines(
				super.sBatchNumber(), 
				conn,
				sUserFullName,
				sUserID,
				lLastArTransactionID);
		} catch (Exception e1) {
			throw new Exception(e1.getMessage());
		}
		
		if (bLogDiagnostics){
			log.writeEntry(
				sUserFullName,
				SMLogEntry.LOG_OPERATION_ARBATCHPOST,
				"In post_without_data_transaction Batch #:" + super.sBatchNumber(), 
				"Going into processApplyingLinesFromApplyToEntries",
				"[1376509520]"
					);
		}

		//create armatchinglines from all the APPLY-TO entry lines in the batch:
		if(!processApplyingLinesFromApplyToEntries(
			super.sBatchNumber(), 
			conn,
			sUserID,
			sUserFullName,
			lLastArTransactionID)){
			throw new Exception(getErrorMessages());
		}
		if (bLogDiagnostics){
			log.writeEntry(
				sUserID,
				SMLogEntry.LOG_OPERATION_ARBATCHPOST,
				"In post_without_data_transaction Batch #:" + super.sBatchNumber(), 
				"Going into updateParentAndRetainage",
				"[1376509265]"
				);
		}

		//Update the retainage flag and parent transactions in the matching lines we've created:
		if(!updateRetainageFlagOnMatchingLines(conn, lLastArMatchingLineID)){
			throw new Exception(getErrorMessages());
		}
		if (bLogDiagnostics){
			log.writeEntry(
				sUserID,
				SMLogEntry.LOG_OPERATION_ARBATCHPOST,
				"In post_without_data_transaction Batch #:" + super.sBatchNumber(), 
				"Going into linkPrepays",
				"[1376509521]"
				);
		}

		SMOption option = new SMOption();
		if (!option.load(conn)){
			throw new Exception("Could not read system options - " + option.getErrorMessage());
		}
		
		//If the flag is set to use the SMCP GL, we'll create a GL Transaction batch
		//System.out.println("[1556909964] - iFeedGL = '" + iFeedGLStatus + "'.");
		AROptions aropt = new AROptions();
		int iFeedGLStatus = 0;
		if(!aropt.load(conn)){
			throw new Exception("Error [1557164337] loading AR Options to check GL feed - " 
				+ aropt.getErrorMessageString());
		}
		
		try {
			iFeedGLStatus = Integer.parseInt(aropt.getFeedGl());
		} catch (Exception e2) {
			throw new Exception("Error [1557165783] - error parsing AR GL Feed status '" + aropt.getFeedGl());
		}
		
		if (bLogDiagnostics){
			log.writeEntry(
					sUserID, 
					SMLogEntry.LOG_OPERATION_ARBATCHPOST, 
					"In post_without_data_transaction, setting export file path to: " + option.getFileExportPath(), 
					"",
					"[1376509512]"
			);
		}
		
		SMGLExport glexport = new SMGLExport();
		glexport.setExportFilePath(option.getFileExportPath());
		
		//Check ALL the prepays in artransactions and see if any can be linked:
		//This function will update the parent ID and retainage flag for the prepay matching lines, too
		if(!linkPrepays(conn, sUserID, sUserFullName, glexport)){
			throw new Exception(getErrorMessages());
		}
		
		if (bLogDiagnostics){
			log.writeEntry(
				sUserID,
				SMLogEntry.LOG_OPERATION_ARBATCHPOST,
				"In post_without_data_transaction Batch #:" + super.sBatchNumber(), 
				"Going into updateStatistics",
				"[1376509522]"
				);
		}

		//Update statistics:
		if (!updateStatistics(conn, lLastArTransactionID, lLastArMatchingLineID)){
			throw new Exception(getErrorMessages());
		}
		if (bLogDiagnostics){
			log.writeEntry(
				sUserID,
				SMLogEntry.LOG_OPERATION_ARBATCHPOST,
				"In post_without_data_transaction Batch #:" + super.sBatchNumber(), 
				"Going into createGLBatch",
				"[1376509266]"
			);
		}
		
		//Update the batch:
		super.iBatchStatus(SMBatchStatuses.POSTED);
		super.setPostingDate(clsDateAndTimeConversions.nowAsSQLDate());
		if (bLogDiagnostics){
			log.writeEntry(
				sUserID,
				SMLogEntry.LOG_OPERATION_ARBATCHPOST,
				"In post_without_data_transaction Batch #:" + super.sBatchNumber(), 
				"Going into save_without_data_transaction",
				"[1376509550]"
				);
		}
		try {
			super.save_without_data_transaction(conn, sUserID, sUserFullName);
		} catch (Exception e) {
			throw new Exception("Error updating batch - " + e.getMessage());
		}
		if (bLogDiagnostics){
			log.writeEntry(
				sUserID,
				SMLogEntry.LOG_OPERATION_ARBATCHPOST,
				"In post_without_data_transaction Batch #:" + super.sBatchNumber(), 
				"After successful save_without_data_transaction",
				"[1376509267]"
			);
		}
		
		if (super.iBatchType() == SMBatchTypes.AR_CASH){
			//If we need to automatically create a bank reconciliation batch, do that now:	
			if (option.getcreatebankrecexport().compareToIgnoreCase("1") == 0){
				//Add records to the bankaccountentries table:
				try {
					addBankAcctEntry(conn);
				} catch (Exception e) {
					throw new Exception("Error [1403121822] adding bank account entries - " + e.getMessage());
				}
			}
		}
		
		if (!createGLBatch(conn, glexport)){
			throw new Exception(getErrorMessages());
		}
		
		return;
	}
/*
private GLTransactionBatch createGLTransactionBatch(Connection conn, String sUserID, String sUsersFullName) throws Exception{
	
	GLTransactionBatch glbatch = new GLTransactionBatch("-1");
	glbatch.setlcreatedby(sCreatedByID());
	glbatch.setllasteditedby(sLastEditedByID());
	glbatch.setsbatchdate(sStdBatchDateString());
	glbatch.setsbatchdescription("Generated from AR Batch #" + sBatchNumber());
	glbatch.setsbatchstatus(Integer.toString(SMBatchStatuses.IMPORTED));

	//System.out.println("[1556909165] - in createGLTransactionBatch.");
	
	//System.out.println("[1556909166] - m_arrBatchEntries.size() = '" + m_arrBatchEntries.size() + "'.");
	
	//Get the AR batch entries here:
	String SQL = "SELECT *"
		+ " FROM " + SMTableentrylines.TableName
		+ " LEFT JOIN " + SMTableentries.TableName
		+ " ON " + SMTableentrylines.TableName + "." + SMTableentrylines.lentryid
		+ "=" + SMTableentries.TableName + "." + SMTableentries.lid
		+ " WHERE (" 
			+ SMTableentrylines.TableName + "." + SMTableentries.ibatchnumber + " = " + super.sBatchNumber()
		+ ")"
		+ " ORDER BY " + SMTableentrylines.TableName + "." + SMTableentries.ientrynumber 
			+ ", " + SMTableentrylines.TableName + "." + SMTableentrylines.lentryid
			+ " ASC"
	;

	ResultSet rsBatchEntries = clsDatabaseFunctions.openResultSet(SQL, conn);

	long lLastEntryID = 0L;
	GLTransactionBatchEntry glentry = new GLTransactionBatchEntry();
	while (rsBatchEntries.next()){
		if (rsBatchEntries.getLong(SMTableentrylines.lentryid) != lLastEntryID){
			if(lLastEntryID != 0L){
				//Add the previous entry to the batch:
				glbatch.addBatchEntry(glentry);
				//And start a new entry:
				glentry = new GLTransactionBatchEntry();
			}
			//Now populate the entry:
			glentry.setsautoreverse("0");
			glentry.setsdatdocdate(ServletUtilities.clsDateAndTimeConversions.convertDateFormat(
				rsBatchEntries.getString(SMTableentries.TableName + "." + SMTableentries.datdocdate), 
				clsServletUtilities.DATETIME_FORMAT_FOR_SQL,
				clsServletUtilities.DATE_FORMAT_FOR_DISPLAY,
				clsServletUtilities.EMPTY_DATE_VALUE)
			);
			glentry.setsdatentrydate(sStdLastEditDateString());
			glentry.setsentrydescription(rsBatchEntries.getString(SMTableentries.TableName + "." + SMTableentries.sdocdescription));
			
			//Figure out the appropriate fiscal period:
			int iFiscalYear = GLFiscalPeriod.getFiscalYearForSelectedDate(sStdLastEditDateString(), conn);
			int iFiscalPeriod = GLFiscalPeriod.getFiscalPeriodForSelectedDate(sStdLastEditDateString(), conn);
			glentry.setsfiscalperiod(Integer.toString(iFiscalPeriod));
			glentry.setsfiscalyear(Integer.toString(iFiscalYear));
			glentry.setssourceledger(GLSourceLedgers.getSourceLedgerDescription(GLSourceLedgers.SOURCE_LEDGER_AR));
			glentry.setssourceledgertransactionlineid("0");
			
			//Add one GL transaction batch line for the entry side UNLESS the entry nets to zero:
			if(rsBatchEntries.getBigDecimal(SMTableentries.TableName + "." + SMTableentries.doriginalamount).compareTo(BigDecimal.ZERO) != 0){
				GLTransactionBatchLine glentryline = new GLTransactionBatchLine();
				glentryline.setsacctid(rsBatchEntries.getString(SMTableentries.TableName + "." + SMTableentries.scontrolacct));
				glentryline.setscomment("AR Control");
				//TODO - figure out how credits and debits will work:
				//We never save a debit or credit as a NEGATIVE number.
				//If the account is normally a 'credit' account, it's normally negative:
				// so a negative number would become a POSITIVE credit amt,
				// and a positive number would become a POSITIVE debit amt.
				
				//If the account is normally a 'debit' account, it's normally positive,
				// so a positive number would become a POSITIVE debit amt,
				// and a negative number would become a POSITIVE credit amt.
				
				glentryline.setAmount(
					ServletUtilities.clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(
						rsBatchEntries.getBigDecimal(SMTableentries.TableName + "." + SMTableentries.doriginalamount)
					), conn
				);
				glentryline.setsdescription(rsBatchEntries.getString(SMTableentries.TableName + "." + SMTableentries.sdocdescription));
				glentryline.setsreference("");
				glentryline.setssourceledger(GLSourceLedgers.getSourceLedgerDescription(GLSourceLedgers.SOURCE_LEDGER_AR));
				glentryline.setssourcetype(
					ARDocumentTypes.getSourceTypes(
						rsBatchEntries.getInt(SMTableentries.TableName + "." + SMTableentries.idocumenttype)
					)
				);
				glentryline.setstransactiondate(sStdLastEditDateString());
				
				glentry.addLine(glentryline);
			}
		}
		
		//Now add each line to the entry:
		if(rsBatchEntries.getBigDecimal(SMTableentrylines.TableName + "." + SMTableentrylines.damount).compareTo(BigDecimal.ZERO) != 0){
			GLTransactionBatchLine glentryline = new GLTransactionBatchLine();
			glentryline.setsacctid(rsBatchEntries.getString(SMTableentrylines.TableName + "." + SMTableentrylines.sglacct));
			glentryline.setscomment(rsBatchEntries.getString(SMTableentrylines.TableName + "." + SMTableentrylines.scomment));
			
			//TODO - figure out how credits and debits will work:
			//glline.setscreditamt(apentry.getsentryamount());
			//glline.setsdebitamt(apentry.getsentryamount());
			glentryline.setAmount(
				ServletUtilities.clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(
					rsBatchEntries.getBigDecimal(SMTableentrylines.TableName + "." + SMTableentrylines.damount)),
				conn
			);
			glentryline.setsdescription(rsBatchEntries.getString(SMTableentrylines.TableName + "." + SMTableentrylines.sdescription));
			glentryline.setsreference("");
			glentryline.setssourceledger(GLSourceLedgers.getSourceLedgerDescription(GLSourceLedgers.SOURCE_LEDGER_AR));
			glentryline.setssourcetype(
				ARDocumentTypes.getSourceTypes(
					rsBatchEntries.getInt(SMTableentries.TableName + "." + SMTableentries.idocumenttype)
				)
			);
			glentryline.setstransactiondate(sStdLastEditDateString());
			
			//Add the line:
			glentry.addLine(glentryline);
		}
		
		//Keep track of which entry we're on:
		lLastEntryID = rsBatchEntries.getLong(SMTableentrylines.lentryid);
	}
	rsBatchEntries.close();
	
	//Now add the last glentry:
	glbatch.addBatchEntry(glentry);

	try {
		glbatch.save_without_data_transaction(conn, sUserID, sUsersFullName, false);
	} catch (Exception e) {
		throw new Exception("Error [1557172272] saving GL Transaction Batch - " + e.getMessage());
	}
	return glbatch;
}
*/
	private void addBankAcctEntry(
		Connection conn) throws Exception{
		
		//Here we need to add a new bank account entry, for every AR batch, AND different GL 'Cash' account:
		String SQL = "SELECT"
			+ " SUM(" + SMTableentries.TableName + "." + SMTableentries.doriginalamount + ") AS TOTAL"
			+ ", " + SMEntryBatch.TableName + "." + SMEntryBatch.datpostdate
			+ ", " + SMEntryBatch.TableName + "." + SMEntryBatch.screatedbyfullname
			+ ", " + SMTableentries.TableName + "." + SMTableentries.scontrolacct
			+ " FROM "
			+ SMTableentries.TableName
			+ " LEFT JOIN " + SMEntryBatch.TableName + " ON " + SMTableentries.TableName + "." + SMTableentries.ibatchnumber
			+ " = " + SMEntryBatch.TableName + "." + SMEntryBatch.ibatchnumber
			//TJR - Removed this on 6/27/2016 because it was causing the 'SUM' above to be doubled if there was more than one
			//bank with the same GL Account
			//+ " LEFT JOIN " + SMTablebkbanks.TableName + " ON " + SMTablebkbanks.TableName + "." + SMTablebkbanks.sglaccount
			//+ " = " + SMTableentries.TableName + "." + SMTableentries.scontrolacct
			+ " WHERE ("
				+ "(" + SMTableentries.TableName + "." + SMTableentries.ibatchnumber + " = " + sBatchNumber() + ")"
				//TJR - Removed this on 6/27/2016 - see note above
				//+ " AND (" + SMTablebkbanks.TableName + "." + SMTablebkbanks.iactive + " = 1)"
				//+ " AND (" + SMTablebkbanks.TableName + "." + SMTablebkbanks.lid + " IS NOT NULL)"
				+ " AND (" + SMTableentries.TableName + "." + SMTableentries.doriginalamount + " != 0.00)"
			+ ")"
			+ " GROUP BY " + SMTableentries.TableName + "." + SMTableentries.scontrolacct
		;
		try {
			ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
			while (rs.next()){
				//Insert a bank account entry record for each of these:
				SQL = " INSERT INTO"
					+ " " + SMTablebkaccountentries.TableName
					+ " ("
					+ SMTablebkaccountentries.bdamount
					+ ", " + SMTablebkaccountentries.datentrydate
					+ ", " + SMTablebkaccountentries.ibatchentrynumber
					+ ", " + SMTablebkaccountentries.ibatchnumber
					+ ", " + SMTablebkaccountentries.ibatchtype
					+ ", " + SMTablebkaccountentries.icleared
					+ ", " + SMTablebkaccountentries.ientrytype
					+ ", " + SMTablebkaccountentries.lstatementid
					+ ", " + SMTablebkaccountentries.sdescription
					+ ", " + SMTablebkaccountentries.sdocnumber
					+ ", " + SMTablebkaccountentries.sglaccount
					+ ", " + SMTablebkaccountentries.ssourcemodule
					
					+ ") VALUES ("
					//Cash entries have a negative value, but we want them to be deposits, so they need to be reversed:
					+ "-1 * " + clsManageBigDecimals.BigDecimalTo2DecimalSQLFormat(rs.getBigDecimal("TOTAL"))
					+ ", '" + rs.getString(SMEntryBatch.TableName + "." + SMEntryBatch.datpostdate) + "'"
					+ ", -1"
					+ ", " + sBatchNumber()
					+ ", " + sBatchType()
					+ ", 0" //icleared
					+ ", " + Integer.toString(SMTablebkaccountentries.ENTRY_TYPE_DEPOSIT)
					+ ", 0" //statement ID
					+ ", '" + "AR Cash Deposit by " + rs.getString(SMEntryBatch.TableName + "." + SMEntryBatch.screatedbyfullname) + "'"
					+ ", '" + "Batch #" + sBatchNumber() + "'"
					+ ", '" + rs.getString(SMTableentries.TableName + "." + SMTableentries.scontrolacct) + "'"
					+ ", '" + SMModuleTypes.AR + "'"
					
					+ ")"
				;
				Statement stmt = conn.createStatement();
				stmt.execute(SQL);
			}
			rs.close();
		} catch (Exception e) {
			throw new Exception("error using SQL: " + SQL + " - " + e.getMessage());
		}
	}
	private void processApplyingLines(
			String sBatchNumber,
			Connection conn,
			String sUserFullName,
			String sUserID,
			long lLastTransactionID) throws Exception{

		//Here we want to get all of the entry lines from this batch which apply to some document: 
		String SQL = "SELECT * FROM "
			+ SMTableentries.TableName + ", " + SMTableentrylines.TableName + ", " + SMTableartransactions.TableName
			+ " WHERE (" 
			+ "(" + SMTableentries.TableName + "." + SMTableentries.ibatchnumber + " = " + sBatchNumber + ")"
			+ " AND (" + SMTableentrylines.TableName + "." + SMTableentrylines.ibatchnumber + " = " + sBatchNumber + ")"
			+ " AND (" + SMTableentrylines.TableName + "." + SMTableentrylines.ientrynumber + " = " 
			+ SMTableentries.TableName + "." + SMTableentries.ientrynumber + ")"
			+ " AND (" + SMTableentrylines.TableName + "." + SMTableentrylines.ldocappliedtoid + " != -1)"

			//Do NOT get any apply-to entries - those will be handled specially:
			+ " AND (" + SMTableentries.TableName + "." + SMTableentries.idocumenttype + " != " 
			+ ARDocumentTypes.APPLYTO_STRING + ")"

			//Link the artransactions table to the entry - we want the transaction that was created by this 
			//entry:
			+ " AND (" + SMTableentries.TableName + "." + SMTableentries.spayeepayor + " = "
			+ SMTableartransactions.TableName + "." + SMTableartransactions.spayeepayor + ")"

			+ " AND (" + SMTableentries.TableName + "." + SMTableentries.sdocnumber + " = "
			+ SMTableartransactions.TableName + "." + SMTableartransactions.sdocnumber + ")"

			+ ")"
			+ " ORDER BY " 
			+ SMTableentries.TableName + "." + SMTableentries.ientrynumber 
			+ "," + SMTableentrylines.TableName + "." + SMTableentrylines.ilinenumber + " ASC";
		/*
    	log.writeEntry(
    			sUserName, 
        		SMLogEntry.LOG_OPERATION_ARBATCHPOST, 
        		"In processApplyingLines - Get_AscendingAppliedEntryLineList_By_BatchNumber", 
        		SQL
        		);
		 */
		try {
			ResultSet rsEntryLineListForProcessingApplyingLine = clsDatabaseFunctions.openResultSet(SQL, conn);

			while (rsEntryLineListForProcessingApplyingLine.next()){
				//Process each applying line:
				//If it's unapplied, it's either a prepay or an unapplied line:
				try {
					buildRegularApplyingLine(
						rsEntryLineListForProcessingApplyingLine, 
						Long.toString(rsEntryLineListForProcessingApplyingLine.getLong(SMTableentrylines.ientrynumber)),
						conn, 
						sUserID,
						sUserFullName,
						lLastTransactionID
					);
				} catch (Exception e) {
					rsEntryLineListForProcessingApplyingLine.close();
					throw new Exception("Error applying line for entry " 
						+ Integer.toString(rsEntryLineListForProcessingApplyingLine.getInt(SMTableentries.TableName + "." + SMTableentries.ientrynumber)) + " - " + e.getMessage());
				}
			}
			rsEntryLineListForProcessingApplyingLine.close();

		}catch (Exception e){
			throw new Exception("Error [1541774173] creating armatchinglines - " + e.getMessage());
		}

		return;
	}
	private boolean processApplyingLinesFromApplyToEntries(
			String sBatchNumber,
			Connection conn,
			String sUserID,
			String sUserFullName,
			long lLastTransactionID
	){

		//First, get a list of all the apply-to entries in this batch:
		String SQL = "SELECT " + SMTableentries.ientrynumber + " FROM " + SMTableentries.TableName
		+ " WHERE ("
		+ "(" + SMTableentries.idocumenttype + " = " + ARDocumentTypes.APPLYTO_STRING + ")"
		+ " AND (" + SMTableentries.ibatchnumber + " = " + sBatchNumber + ")"
		+ ")"
		+ " ORDER BY " + SMTableentries.ientrynumber
		;
		/*
    	log.writeEntry(
    			sUserName, 
        		SMLogEntry.LOG_OPERATION_ARBATCHPOST, 
        		"In processApplyingLinesFromApplyToEntries - Get rsApplyToEntries", 
        		SQL
        		);
		 */
		try {
			ResultSet rsApplyToEntries = clsDatabaseFunctions.openResultSet(SQL, conn);
			while (rsApplyToEntries.next()){
				//Process each applying line:
				//If it's unapplied, it's either a prepay or an unapplied line:
				if(!buildMatchingLinesFromApplyToEntry(
						Long.toString(rsApplyToEntries.getLong(SMTableentries.ientrynumber)),
						conn, 
						sUserID,
						sUserFullName,
						lLastTransactionID
				)){
					rsApplyToEntries.close();
					return false;
				}
			}
			rsApplyToEntries.close();

		}catch (SQLException e){
			super.addErrorMessage("Error reading apply-to entries - " + e.getMessage());
			return false;
		}
		/*
    	log.writeEntry(
    			sUserName, 
        		SMLogEntry.LOG_OPERATION_ARBATCHPOST, 
        		"In processApplyingLinesFromApplyToEntries - returning true", 
        		SQL
        		);
		 */
		return true;
	}

	private boolean buildMatchingLinesFromApplyToEntry(
			String sEntryNumber, 
			Connection conn, 
			String sUserID,
			String sUserFullName,
			long lLastTransactionID
	){

		//Get the artransaction ID of the transaction that was created by this entry:
		String SQL = "SELECT * FROM " + SMTableartransactions.TableName
		+ " WHERE ("
		+ "(" + SMTableartransactions.loriginalbatchnumber + " = " + super.sBatchNumber() + ")"
		+ " AND (" + SMTableartransactions.loriginalentrynumber + " = " + sEntryNumber + ")"
		//We need to make sure that it's among the transactions created in this posting, because
		//there may be some old transactions with the same batch number:
		+ " AND (" + SMTableartransactions.lid + " > " + Long.toString(lLastTransactionID) + ")"
		+ ")"
		;

		String sARTransactionID = "";
		java.sql.Date datTransactionDate;
		String sCustomerNumber = "";
		String sDocNumber = "";
		try {
			ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
			if (rs.next()){
				//Read the artransaction ID for the transaction which this entry created:
				sARTransactionID = Long.toString(rs.getLong(SMTableartransactions.lid));
				datTransactionDate = rs.getDate(SMTableartransactions.datdocdate);
				sCustomerNumber = rs.getString(SMTableartransactions.spayeepayor);
				sDocNumber = rs.getString(SMTableartransactions.sdocnumber);
				rs.close();
			}else{
				super.addErrorMessage("Found no artransaction ID for apply-to on entry " + sEntryNumber);
				rs.close();
				return false;
			}
		}catch (SQLException e){
			super.addErrorMessage("Error reading artransaction ID for apply-to on entry " + sEntryNumber + " - " + e.getMessage());
			return false;
		}

		//Here we get the entry, the entryline and the transaction that the entry line was applied to:
		SQL = "SELECT * FROM "
			+ SMTableentries.TableName + ", " + SMTableentrylines.TableName + ", " + SMTableartransactions.TableName
			+ " WHERE (" 
			+ "(" + SMTableentries.TableName + "." + SMTableentries.ibatchnumber + " = " + super.sBatchNumber() + ")"
			+ " AND (" + SMTableentrylines.TableName + "." + SMTableentrylines.ibatchnumber + " = " + super.sBatchNumber() + ")"
			+ " AND (" + SMTableentrylines.TableName + "." + SMTableentrylines.ientrynumber + " = " 
			+ SMTableentries.TableName + "." + SMTableentries.ientrynumber + ")"
			+ " AND (" + SMTableentries.TableName + "." + SMTableentries.ientrynumber + " = " + sEntryNumber + ")"

			//Link the ar transactions table:
			+ " AND (" + SMTableartransactions.TableName + "." + SMTableartransactions.lid + " = "
			+ SMTableentrylines.TableName + "." + SMTableentrylines.ldocappliedtoid + ")"
			+ ")"
			+ " ORDER BY " + SMTableentrylines.TableName + "." + SMTableentrylines.ilinenumber + " ASC";
		/*
    	log.writeEntry(
    			sUserName, 
        		SMLogEntry.LOG_OPERATION_ARBATCHPOST, 
        		"In buildMatchingLinesFromApplyToEntry - Get rsEntryLines", 
        		SQL
        		);
		 */
		ArrayList<ARLine> negativeLineArray = new ArrayList<ARLine>(0);
		ArrayList<ARLine> positiveLineArray = new ArrayList<ARLine>(0);
		BigDecimal bdEntryLineApplyingAmt = BigDecimal.ZERO;

		try {
			ResultSet rsEntryLines = clsDatabaseFunctions.openResultSet(SQL, conn);
			while (rsEntryLines.next()){
				bdEntryLineApplyingAmt = rsEntryLines.getBigDecimal(SMTableentrylines.TableName + "." + SMTableentrylines.damount);
				//Load each line into the array:
				ARLine line = new ARLine();
				line.dAmount(bdEntryLineApplyingAmt);
				line.iBatchNumber(Integer.parseInt(super.sBatchNumber()));
				line.iEntryNumber(Integer.parseInt(sEntryNumber));
				line.iLineNumber(rsEntryLines.getInt(SMTableentrylines.TableName + "." + SMTableentrylines.ilinenumber));
				line.lDocAppliedToId(rsEntryLines.getLong(SMTableentrylines.TableName + "." + SMTableentrylines.ldocappliedtoid));
				line.lEntryId(rsEntryLines.getLong(SMTableentries.TableName + "." + SMTableentries.lid));
				line.lId(rsEntryLines.getLong(SMTableentrylines.TableName + "." + SMTableentrylines.lid));
				line.sComment(rsEntryLines.getString(SMTableentrylines.TableName + "." + SMTableentrylines.scomment));
				line.sDescription(rsEntryLines.getString(SMTableentrylines.TableName + "." + SMTableentrylines.sdescription));
				line.sDocAppliedTo(rsEntryLines.getString(SMTableentrylines.TableName + "." + SMTableentrylines.sdocappliedto));
				line.setApplyToOrderNumber(rsEntryLines.getString(SMTableentrylines.TableName + "." + SMTableentrylines.sapplytoordernumber));
				line.sGLAcct(rsEntryLines.getString(SMTableentrylines.TableName + "." + SMTableentrylines.sglacct));
				if (line.dAmount().compareTo(BigDecimal.ZERO) == -1){
					negativeLineArray.add(line);
				}else{
					positiveLineArray.add(line);
				}
			}
			rsEntryLines.close();

		}catch (SQLException e){
			super.addErrorMessage("Error [1541774174] creating armatchinglines - " + e.getMessage());
			return false;
		}

		//Now all the negative lines are in one arraylist, and all the positive lines are in another
		//we need to create the individual armatchinglines from these

		//In a single Apply-to entry, NO particular 'apply-from' line applies to ANY particular 'apply-to'
		//line.  ALL of the negative entries are expected to apply against ALL of the positive entries.
		//So we just have to go down the line of negative entries, and apply them to positive entries,
		//one after the other, until they are all exhausted.

		//We'll take the negative items one at a time until we've used each one up:
		int iCurrentNegativeLineIndex = 0;
		int iCurrentPositiveLineIndex = 0;
		while (iCurrentNegativeLineIndex < negativeLineArray.size()){
			BigDecimal bdApplyingAmount = BigDecimal.ZERO;
			//If the abs of the negative number is greater than the abs of the positive one, then
			//set the applying amount to equal the positive amount and subtract it from both the
			//negative and the positive amounts:
			if (negativeLineArray.get(iCurrentNegativeLineIndex).dAmount().abs().compareTo(positiveLineArray.get(iCurrentPositiveLineIndex).dAmount().abs()) > 0){
				bdApplyingAmount = positiveLineArray.get(iCurrentPositiveLineIndex).dAmount();
			}else{
				bdApplyingAmount = negativeLineArray.get(iCurrentNegativeLineIndex).dAmount().abs();
			}

			//Reduce the remaining amount of the negative side:
			negativeLineArray.get(iCurrentNegativeLineIndex).dAmount(negativeLineArray.get(iCurrentNegativeLineIndex).dAmount().add(bdApplyingAmount));

			//Reduce the remaining amount of the positive side:
			positiveLineArray.get(iCurrentPositiveLineIndex).dAmount(positiveLineArray.get(iCurrentPositiveLineIndex).dAmount().subtract(bdApplyingAmount));

			/*
    		Here's the logic for setting apply-from, apply-to, and the sign of the amounts:
    		The first armatchingline:
    		Apply from = cash/prepay - the negative amt doc
    		Apply to = invoice, etc. - the positive amount
    		Amount = positive (because amounts applying to positive transactions are also positive)

    		The second armatchingline:
    		Apply from = invoice, etc. - the positive amount
    		Apply to = cash/prepay - the negative amt doc
    		Amount = negative (because amounts applying to negative transactions are also negative)

			 */

			//Insert an armatching line into the table:
			SQL = "INSERT INTO " + SMTablearmatchingline.TableName
			+ "("
			+ SMTablearmatchingline.damount
			+ ", " + SMTablearmatchingline.dattransactiondate
			+ ", " + SMTablearmatchingline.iretainage
			+ ", " + SMTablearmatchingline.ldocappliedtoid
			+ ", " + SMTablearmatchingline.lparenttransactionid
			+ ", " + SMTablearmatchingline.sapplytodoc
			+ ", " + SMTablearmatchingline.sdescription
			+ ", " + SMTablearmatchingline.sdocnumber
			+ ", " + SMTablearmatchingline.spayeepayor
			+ ") VALUES ("
			//amount
			+ clsManageBigDecimals.BigDecimalTo2DecimalSQLFormat(bdApplyingAmount)
			//transaction date
			+ ", '" + clsDateAndTimeConversions.utilDateToString(datTransactionDate, "yyyy-MM-dd") + "'"
			//retainage - actual retainage value will be set in a later function:
			+ ", 0"
			//doc applied to ID
			+ ", " + positiveLineArray.get(iCurrentPositiveLineIndex).sDocAppliedToId()
			//parent transaction ID - this would be the AR transaction ID
			+ ", " + sARTransactionID
			//apply-to doc
			+ ", '" + clsDatabaseFunctions.FormatSQLStatement(positiveLineArray.get(iCurrentPositiveLineIndex).sDocAppliedTo()) + "'"
			//description
			+ ", '" + clsDatabaseFunctions.FormatSQLStatement(negativeLineArray.get(iCurrentNegativeLineIndex).sDescription()) + "'"
			//apply-from doc number
			+ ", '" + clsDatabaseFunctions.FormatSQLStatement(negativeLineArray.get(iCurrentNegativeLineIndex).sDocAppliedTo()) + "'"
			//payee/payor
			+ ", '" + sCustomerNumber + "'"
			+ ")"
			;
			/*
        	log.writeEntry(
        			sUserName, 
            		SMLogEntry.LOG_OPERATION_ARBATCHPOST, 
            		"In buildMatchingLinesFromApplyToEntry - Writing first armatchingline", 
            		SQL
            		);
			 */
			try{
				if(!clsDatabaseFunctions.executeSQL(SQL, conn)){
					super.addErrorMessage("Could not execute SQL to create first apply-to armatchingline");
					return false;
				}
			}catch(SQLException e){
				super.addErrorMessage("Error inserting first apply-to armatchingline: " + e.getMessage());
				return false;
			}

			//Load the ar transaction corresponding to the POSITIVE line:
			ARTransaction arPOStrans = new ARTransaction();
			try {
				arPOStrans.load(clsDatabaseFunctions.FormatSQLStatement(positiveLineArray.get(iCurrentPositiveLineIndex).sDocAppliedToId()), conn);
			} catch (Exception e2) {
				super.addErrorMessage("Error loading apply-to transaction - " + e2.getMessage());
				return false;
			}
			
			//Load the ar transaction corresponding to the NEGATIVE line:
			ARTransaction arNEGtrans = new ARTransaction();
			try {
				arNEGtrans.load(negativeLineArray.get(iCurrentNegativeLineIndex).sDocAppliedToId(), conn);
			} catch (Exception e2) {
				super.addErrorMessage("Could not load apply-to doc - " + e2.getMessage());
				return false;
			}
					
			BigDecimal bdCurrentAmt = arPOStrans.getdCurrentAmount().subtract(bdApplyingAmount);

			//Record a chron file entry here from the apply-from:
			try {
				archron.writeEntry(
					bdApplyingAmount, 
					-1, 
					super.lBatchNumber(), 
					Long.parseLong(sEntryNumber), 
					-1, 
					"applied " + clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(bdApplyingAmount) 
					+ " from Doc # "
					+ clsDatabaseFunctions.FormatSQLStatement(negativeLineArray.get(iCurrentNegativeLineIndex).sDocAppliedTo())
					+ " to Doc # " 
					+ clsDatabaseFunctions.FormatSQLStatement(positiveLineArray.get(iCurrentPositiveLineIndex).sDocAppliedTo())
					+ " with APPLY-TO entry " + sDocNumber
					+ " reducing the current amt on "
					+ clsDatabaseFunctions.FormatSQLStatement(positiveLineArray.get(iCurrentPositiveLineIndex).sDocAppliedTo())
					+ " to " + clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(bdCurrentAmt)
					, 
					sDocNumber, 
					sCustomerNumber, 
					sUserID,
					sUserFullName,
					SQL, 
					clsDatabaseFunctions.FormatSQLStatement(positiveLineArray.get(iCurrentPositiveLineIndex).sDocAppliedTo()), 
					clsDatabaseFunctions.FormatSQLStatement(negativeLineArray.get(iCurrentNegativeLineIndex).sDocAppliedTo()), 
					sDocNumber
				);
			} catch (Exception e1) {
				super.addErrorMessage("Could insert chron log entry for first apply-to armatchingline - " + e1.getMessage());
				return false;
			}

			//Now update the current amount of the apply-to doc:
			SQL = "UPDATE " + SMTableartransactions.TableName
			+ " SET " + SMTableartransactions.dcurrentamt + " = "
			+ SMTableartransactions.dcurrentamt + " - (" 
			+ clsManageBigDecimals.BigDecimalTo2DecimalSQLFormat(bdApplyingAmount) + ")"
			+ " WHERE ("
			+ "(" + SMTableartransactions.spayeepayor + " = '" + sCustomerNumber + "')"
			+ " AND (" + SMTableartransactions.sdocnumber + " = '" 
			+ clsDatabaseFunctions.FormatSQLStatement(positiveLineArray.get(iCurrentPositiveLineIndex).sDocAppliedTo()) 
			+ "')"
			+ ")"
			;

			try {
				Statement stmt = conn.createStatement();
				stmt.execute(SQL);
			} catch (SQLException e1) {
				super.addErrorMessage("Error updating current amount from first apply-to armatchingline: " + e1.getMessage());
				return false;
			}
			
			//Insert the second armatching line into the table:
			SQL = "INSERT INTO " + SMTablearmatchingline.TableName
			+ "("
			+ SMTablearmatchingline.damount
			+ ", " + SMTablearmatchingline.dattransactiondate
			+ ", " + SMTablearmatchingline.iretainage
			+ ", " + SMTablearmatchingline.ldocappliedtoid
			+ ", " + SMTablearmatchingline.lparenttransactionid
			+ ", " + SMTablearmatchingline.sapplytodoc
			+ ", " + SMTablearmatchingline.sdescription
			+ ", " + SMTablearmatchingline.sdocnumber
			+ ", " + SMTablearmatchingline.spayeepayor
			+ ") VALUES ("
			//amount
			+ clsManageBigDecimals.BigDecimalTo2DecimalSQLFormat(bdApplyingAmount.negate())
			//transaction date
			+ ", '" + clsDateAndTimeConversions.utilDateToString(datTransactionDate, "yyyy-MM-dd") + "'"
			//retainage - actual retainage value will be set in a later function:
			+ ", 0"
			//doc applied to ID
			+ ", " + negativeLineArray.get(iCurrentNegativeLineIndex).sDocAppliedToId()
			//parent transaction ID - this would be the AR transaction ID
			+ ", " + sARTransactionID
			//apply-to doc
			+ ", '" + clsDatabaseFunctions.FormatSQLStatement(negativeLineArray.get(iCurrentNegativeLineIndex).sDocAppliedTo()) + "'"
			//description
			+ ", '" + clsDatabaseFunctions.FormatSQLStatement(positiveLineArray.get(iCurrentPositiveLineIndex).sDescription()) + "'"
			//apply-from doc number
			+ ", '" + clsDatabaseFunctions.FormatSQLStatement(positiveLineArray.get(iCurrentPositiveLineIndex).sDocAppliedTo()) + "'"
			//payee/payor
			+ ", '" + sCustomerNumber + "'"
			+ ")"
			;
			/*
        	log.writeEntry(
        			sUserName, 
            		SMLogEntry.LOG_OPERATION_ARBATCHPOST, 
            		"In buildMatchingLinesFromApplyToEntry - Writing second armatchingline", 
            		SQL
            		);
			 */
			try{
				if(!clsDatabaseFunctions.executeSQL(SQL, conn)){
					super.addErrorMessage("Could not execute SQL to create second apply-to armatchingline");
					return false;
				}
			}catch(SQLException e){
				super.addErrorMessage("Error inserting second apply-to armatchingline: " + e.getMessage());
				return false;
			}

			//Record a chron file entry here from the apply-from:
			bdCurrentAmt = arNEGtrans.getdCurrentAmount().subtract(bdApplyingAmount.negate());
			try{
				archron.writeEntry(
					bdApplyingAmount.negate(), 
					-1, 
					super.lBatchNumber(), 
					Long.parseLong(sEntryNumber), 
					-1, 
					"applied " + clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(bdApplyingAmount.negate()) 
					+ " from Doc # "
					+ clsDatabaseFunctions.FormatSQLStatement(positiveLineArray.get(iCurrentPositiveLineIndex).sDocAppliedTo())
					+ " to Doc # " 
					+ clsDatabaseFunctions.FormatSQLStatement(negativeLineArray.get(iCurrentNegativeLineIndex).sDocAppliedTo())
					+ " with APPLY-TO entry " + sDocNumber
					+ " reducing the current amt on "
					+ clsDatabaseFunctions.FormatSQLStatement(negativeLineArray.get(iCurrentNegativeLineIndex).sDocAppliedTo())
					+ " to " + clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(bdCurrentAmt)
					, 
					sDocNumber, 
					sCustomerNumber, 
					sUserID,
					sUserFullName,
					SQL, 
					clsDatabaseFunctions.FormatSQLStatement(negativeLineArray.get(iCurrentNegativeLineIndex).sDocAppliedTo()), 
					clsDatabaseFunctions.FormatSQLStatement(positiveLineArray.get(iCurrentPositiveLineIndex).sDocAppliedTo()), 
					sDocNumber
				);
			} catch (Exception e) {
				super.addErrorMessage("Error [1387574932]  - " + e.getMessage() 
					+ " - Could not insert chron log entry for first apply-to armatchingline - ");
				return false;
			}

			//Now update the current amount of the apply-from doc:
			SQL = "UPDATE " + SMTableartransactions.TableName
			+ " SET " + SMTableartransactions.dcurrentamt + " = "
			+ SMTableartransactions.dcurrentamt + " - (" 
			+ clsManageBigDecimals.BigDecimalTo2DecimalSQLFormat(bdApplyingAmount.negate()) + ")"
			+ " WHERE ("
			+ "(" + SMTableartransactions.spayeepayor + " = '" + sCustomerNumber + "')"
			+ " AND (" + SMTableartransactions.sdocnumber + " = '" 
			+ clsDatabaseFunctions.FormatSQLStatement(negativeLineArray.get(iCurrentNegativeLineIndex).sDocAppliedTo()) 
			+ "')"
			+ ")"
			;

			try {
				Statement stmt = conn.createStatement();
				stmt.execute(SQL);
			} catch (SQLException e1) {
				super.addErrorMessage("Error updating current amount from second apply-to armatchingline: " + e1.getMessage());
				return false;
			}

			//Increment the pointers if one line has been used up to zero:
			if(negativeLineArray.get(iCurrentNegativeLineIndex).dAmount().compareTo(BigDecimal.ZERO) == 0){
				iCurrentNegativeLineIndex++;
			}
			if(positiveLineArray.get(iCurrentPositiveLineIndex).dAmount().compareTo(BigDecimal.ZERO) == 0){
				iCurrentPositiveLineIndex++;
			}
		}
		return true;
	}
	private boolean buildRegularApplyingLine(
			ResultSet rsEntryLines,
			String sEntryNumber,
			Connection conn, 
			String sUserID,
			String sUserFullName,
			long lLastTransactionID) throws Exception{

		//Get the artransaction ID of the transaction that was created by this entry:
		String SQL = "SELECT * FROM " + SMTableartransactions.TableName
		+ " WHERE ("
		+ "(" + SMTableartransactions.loriginalbatchnumber + " = " + super.sBatchNumber() + ")"
		+ " AND (" + SMTableartransactions.loriginalentrynumber + " = " + sEntryNumber + ")"
		//We need to make sure that it's among the transactions created in this posting, because
		//there may be some old transactions with the same batch number:
		+ " AND (" + SMTableartransactions.lid + " > " + Long.toString(lLastTransactionID) + ")"
		+ ")"
		;

		String sARTransactionID = "";
		String sDocNumber = "";
		String sDocType = "";
		BigDecimal dDocCurrentAmt = BigDecimal.ZERO;
		try {
			ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
			if (rs.next()){
				//Read the artransaction ID for the transaction which this entry created:
				sARTransactionID = Long.toString(rs.getLong(SMTableartransactions.lid));
				sDocNumber = rs.getString(SMTableartransactions.sdocnumber);
				sDocType = ARDocumentTypes.Get_Document_Type_Label(rs.getInt(SMTableartransactions.idoctype));
				dDocCurrentAmt = rs.getBigDecimal(SMTableartransactions.dcurrentamt);
				rs.close();
			}else{
				rs.close();
				throw new Exception("Found no artransaction ID for entry " + sEntryNumber);
			}
		}catch (SQLException e){
			throw new Exception("Error reading artransaction ID for entry " + sEntryNumber + " - " + e.getMessage());
		}

		//Create a matching record for the parent doc and the apply-to doc:
		//First, the apply-from doc:

		//First, get the apply-to transaction so we can check the current amount, etc.:
		ARTransaction applytotrans = new ARTransaction();
		BigDecimal bdEntryLineAmt = BigDecimal.ZERO;
		BigDecimal bdApplyingAmt = BigDecimal.ZERO;
		
		try{
			applytotrans.load(Long.toString(rsEntryLines.getLong(SMTableentrylines.TableName + "." + SMTableentrylines.ldocappliedtoid)), conn);
		}catch (Exception e){
			throw new Exception("Error reading apply-to transaction to insert armatchingline - " + e.getMessage() + ".");
		}
		
		try{
			bdEntryLineAmt = 
				rsEntryLines.getBigDecimal(SMTableentrylines.TableName + "." + SMTableentrylines.damount);
		}catch(Exception e){
			throw new Exception("SQL exception reading apply-to transaction to insert armatchingline.");
		}
		/*
		 * The logic below was added to prevent overapplys, but we decided on 5/19/09 to take it out.
		 * From now on, the system WILL allow you to overapply.
   		//Set the amount to be applied - UP TO the remaining amount on the apply-to doc:
   		//If the current amount is more than the applying amount, apply the full applying amount
   		if (applytotrans.getdCurrentAmount().abs().compareTo(bdEntryLineAmt.abs()) > 0){
   			bdApplyingAmt = bdEntryLineAmt;
   		//If the applying amount is more than the current amount, then just apply the current amount:
   		}else{
   			bdApplyingAmt = applytotrans.getdCurrentAmount();
   		}
   		//If there is no amount to apply, as when the current amount is zero, then don't add any matching
   		//lines, but just return:
   		if (bdApplyingAmt.compareTo(BigDecimal.ZERO) == 0){
   			return true;
   		}
		 */
		bdApplyingAmt = bdEntryLineAmt;

		SQL = "INSERT INTO " + SMTablearmatchingline.TableName
		+ "("
		+  SMTablearmatchingline.damount
		+ ", " + SMTablearmatchingline.dattransactiondate
		+ ", " + SMTablearmatchingline.sapplytodoc
		+ ", " + SMTablearmatchingline.sdescription
		+ ", " + SMTablearmatchingline.sdocnumber
		+ ", " + SMTablearmatchingline.spayeepayor
		+ ", " + SMTablearmatchingline.ldocappliedtoid
		+ ", " + SMTablearmatchingline.lparenttransactionid
		+ ") VALUES ("
		+ clsManageBigDecimals.BigDecimalTo2DecimalSQLFormat(bdApplyingAmt)
		+ ", '" + clsDateAndTimeConversions.utilDateToString(rsEntryLines.getDate(SMTableentries.TableName + "." + SMTableentries.datdocdate), "yyyy-MM-dd") + "'"
		+ ", '" + clsDatabaseFunctions.FormatSQLStatement(rsEntryLines.getString(SMTableentrylines.TableName + "." + SMTableentrylines.sdocappliedto)) + "'"
		+ ", '" + clsDatabaseFunctions.FormatSQLStatement(rsEntryLines.getString(SMTableentrylines.TableName + "." + SMTableentrylines.sdescription)) + "'" //SMTablearmatchingline.sdescription
		+ ", '" + clsDatabaseFunctions.FormatSQLStatement(rsEntryLines.getString(SMTableentries.TableName + "." + SMTableentries.sdocnumber)) + "'"
		+ ", '" + clsDatabaseFunctions.FormatSQLStatement(rsEntryLines.getString(SMTableentries.TableName + "." + SMTableentries.spayeepayor)) + "'"
		+ ", " + Long.toString(rsEntryLines.getLong(SMTableentrylines.TableName + "." + SMTableentrylines.ldocappliedtoid))
		+ ", " + sARTransactionID
		+ ")";
		/*
		log.writeEntry(
				sUserName, 
				SMLogEntry.LOG_OPERATION_ARBATCHPOST, 
				"In buildRegularApplyingLine inserting matching line for APPLY-FROM doc - entry " 
					+ Long.toString(rsEntryLines.getLong(SMTableentries.ientrynumber)), 
				SQL
			);
		 */
		try {
			Statement stmt = conn.createStatement();
			stmt.execute(SQL);
		} catch (Exception e) {
			throw new Exception ("Error inserting armatchingline with SQL: '" + SQL + " - " + e.getMessage() + ".");
		}
		
		//Get the customer balance:
		String sCustomer = rsEntryLines.getString(SMTableentries.TableName + "." + SMTableentries.spayeepayor);

		//Add a chron entry here for the apply FROM line:
		try {
			archron.writeEntry(
				bdApplyingAmt, 
				-1, 
				super.lBatchNumber(), 
				Long.parseLong(sEntryNumber), 
				-1, 
				"applied " + clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(bdApplyingAmt) 
				+ " from Doc # "
				+ clsDatabaseFunctions.FormatSQLStatement(rsEntryLines.getString(SMTableentries.TableName + "." + SMTableentries.sdocnumber))
				+ " to Doc # " 
				+ clsDatabaseFunctions.FormatSQLStatement(rsEntryLines.getString(SMTableentrylines.TableName + "." + SMTableentrylines.sdocappliedto))
				+ " with " + sDocType + " entry " + sDocNumber
				+ " reducing the current amt on "
				+ clsDatabaseFunctions.FormatSQLStatement(rsEntryLines.getString(SMTableentrylines.TableName + "." + SMTableentrylines.sdocappliedto))
				+ " to " + clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(applytotrans.getdCurrentAmount().subtract(bdApplyingAmt))
				, 
				sDocNumber, 
				sCustomer, 
				sUserID,
				sUserFullName,
				SQL, 
				clsDatabaseFunctions.FormatSQLStatement(rsEntryLines.getString(SMTableentrylines.TableName + "." + SMTableentrylines.sdocappliedto)), 
				clsDatabaseFunctions.FormatSQLStatement(rsEntryLines.getString(SMTableentries.TableName + "." + SMTableentries.sdocnumber)), 
				sDocNumber
			);
		} catch (Exception e) {
			throw new Exception("Error [1387574942]  - " + e.getMessage() 
					+ " - Could not insert chron log entry for first apply-to armatchingline");
		}

		//Now reduce the current amount:
		applytotrans.setCurrentAmount(applytotrans.getdCurrentAmount().subtract(bdApplyingAmt));

		/*
		log.writeEntry(
				sUserName, 
				SMLogEntry.LOG_OPERATION_ARBATCHPOST, 
				"In buildRegularApplyingLine updating current amt for doc - entry " 
					+ Long.toString(rsEntryLines.getLong(SMTableentries.ientrynumber)), 
				applytotrans.read_out_debug_data()
			);
		 */
		try {
			applytotrans.save_without_data_transaction(conn, sUserFullName);
		} catch (Exception e) {
			throw new Exception("Error saving apply-to transaction after current amt update - " + e.getMessage());
		}
		/*
		log.writeEntry(
			sUserName, 
			SMLogEntry.LOG_OPERATION_ARBATCHPOST, 
			"Added armatchingline for apply-from document", 
			SQL
		);
		 */
		//Next, the apply-to doc:
		//We don't create this matching line if it's a retainage entry: retainage entries
		//only create one matchingline applying to the original invoice:
		int iDocType = 0;
		try {
			iDocType = rsEntryLines.getInt(SMTableentries.idocumenttype);
		} catch (SQLException e2) {
			throw new Exception("Error [1387575756] getting document type - " + e2.getMessage());
		}
		if(iDocType != ARDocumentTypes.RETAINAGE){
			try {
				SQL = "INSERT INTO " + SMTablearmatchingline.TableName
				+ "("
				+  SMTablearmatchingline.damount
				+ ", " + SMTablearmatchingline.dattransactiondate
				+ ", " + SMTablearmatchingline.sapplytodoc
				+ ", " + SMTablearmatchingline.sdescription
				+ ", " + SMTablearmatchingline.sdocnumber
				+ ", " + SMTablearmatchingline.spayeepayor
				+ ", " + SMTablearmatchingline.ldocappliedtoid
				+ ", " + SMTablearmatchingline.lparenttransactionid
				+ ") VALUES ("
				//Sign is reversed on the apply-to matchingline:
				+ clsManageBigDecimals.BigDecimalTo2DecimalSQLFormat(bdApplyingAmt.negate())
				+ ", '" + clsDateAndTimeConversions.utilDateToString(rsEntryLines.getDate(SMTableentries.TableName + "." + SMTableentries.datdocdate), "yyyy-MM-dd") + "'"
				+ ", '" + clsDatabaseFunctions.FormatSQLStatement(rsEntryLines.getString(SMTableentries.TableName + "." + SMTableentries.sdocnumber)) + "'"
				+ ", '" + clsDatabaseFunctions.FormatSQLStatement(rsEntryLines.getString(SMTableentrylines.TableName + "." + SMTableentrylines.sdescription)) + "'" //SMTablearmatchingline.sdescription
				//The doc number is the apply-to's doc number for this INSERT:
				+ ", '" + clsDatabaseFunctions.FormatSQLStatement(rsEntryLines.getString(SMTableentrylines.TableName + "." + SMTableentrylines.sdocappliedto)) + "'"
				+ ", '" + clsDatabaseFunctions.FormatSQLStatement(rsEntryLines.getString(SMTableentries.TableName + "." + SMTableentries.spayeepayor)) + "'"
				+ ", " + Long.toString(rsEntryLines.getLong(SMTableartransactions.TableName + "." + SMTableartransactions.lid))
				+ ", " + sARTransactionID
				+ ")";
			} catch (Exception e2) {
				throw new Exception("Error [1387575756] creating SQL to insert armatchingline - " + e2.getMessage());
			}
			/*
			log.writeEntry(
					sUserName, 
    				SMLogEntry.LOG_OPERATION_ARBATCHPOST, 
    				"In buildRegularApplyingLine inserting matching line for APPLY-TO doc - entry " 
    					+ Long.toString(rsEntryLines.getLong(SMTableentries.ientrynumber)), 
    				SQL
    			);
			 */
			
			try {
				Statement stmt = conn.createStatement();
				stmt.execute(SQL);
			} catch (Exception e2) {
				throw new Exception("Error [1387574971] inserting armatchingline - " + e2.getMessage()	+ ".");
			}
			
			try{
				archron.writeEntry(
					bdApplyingAmt.negate(), 
					-1, 
					super.lBatchNumber(), 
					Long.parseLong(sEntryNumber), 
					-1, 
					"applied " + clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(bdApplyingAmt.negate()) 
					+ " from Doc # "
					+ clsDatabaseFunctions.FormatSQLStatement(rsEntryLines.getString(SMTableentrylines.TableName + "." + SMTableentrylines.sdocappliedto))
					+ " to Doc # " 
					+ clsDatabaseFunctions.FormatSQLStatement(rsEntryLines.getString(SMTableentries.TableName + "." + SMTableentries.sdocnumber))
					+ " with " + sDocType + " entry " + sDocNumber
					+ " reducing the current amt on "
					+ clsDatabaseFunctions.FormatSQLStatement(rsEntryLines.getString(SMTableentries.TableName + "." + SMTableentries.sdocnumber))
					+ " to " + clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(dDocCurrentAmt.subtract(bdApplyingAmt.negate()))
					, 
					sDocNumber, 
					clsDatabaseFunctions.FormatSQLStatement(rsEntryLines.getString(SMTableentries.TableName + "." + SMTableentries.spayeepayor)), 
					sUserID,
					sUserFullName,
					SQL, 
					clsDatabaseFunctions.FormatSQLStatement(rsEntryLines.getString(SMTableentries.TableName + "." + SMTableentries.sdocnumber)), 
					clsDatabaseFunctions.FormatSQLStatement(rsEntryLines.getString(SMTableentrylines.TableName + "." + SMTableentrylines.sdocappliedto)), 
					sDocNumber
				);
			} catch (Exception e) {
				throw new Exception("Error [1387574951] formatting entrynumber '" + sEntryNumber + "' - " + e.getMessage() 
					+ " - Could not insert chron log entry.");
			}
				
			//Now update the current amount in the apply-from document:
			SQL = "UPDATE " + SMTableartransactions.TableName
				+ " SET " + SMTableartransactions.dcurrentamt + " = "
				+ SMTableartransactions.dcurrentamt + " + " 
				+ clsManageBigDecimals.BigDecimalTo2DecimalSQLFormat(bdApplyingAmt)
				+ " WHERE ("
				+ "(" + SMTableartransactions.sdocnumber + " = '" 
				+ clsDatabaseFunctions.FormatSQLStatement(rsEntryLines.getString(SMTableentries.TableName 
				+ "." + SMTableentries.sdocnumber)) + "')"
				+ " AND (" + SMTableartransactions.spayeepayor + " = '" 
				+ clsDatabaseFunctions.FormatSQLStatement(rsEntryLines.getString(SMTableentries.TableName 
				+ "." + SMTableentries.spayeepayor)) + "')"
				+ ")"
			;

			try{
				Statement stmt = conn.createStatement();
				stmt.execute(SQL);
			} catch (Exception e){
				throw new Exception("Error [13875749561] updating artransactions table - with SQL: " + SQL + " - " + e.getMessage());
			}
			/*
    		log.writeEntry(
    			sUserName, 
    			SMLogEntry.LOG_OPERATION_ARBATCHPOST, 
    			"Added armatchingline for apply-to document", 
    			SQL
        	);
			 */
		}

		return true;
	}
	private boolean linkPrepays(
		Connection conn, 
		String sUserID, 
		String sUserFullName, 
		SMGLExport export){

		//Get ALL the open prepays:
		String SQL = "SELECT *"
			+ " FROM " + SMTableartransactions.TableName + " INNER JOIN"
			+ "(SELECT DISTINCT"
			+ " " + SMTableentries.TableName + "." + SMTableentries.ibatchnumber
			+ ", " + SMTableentries.TableName + "." + SMTableentries.spayeepayor
			+ " FROM " + SMTableentries.TableName
			+ " WHERE ("
			+ "((" + SMTableentries.TableName + "." + SMTableentries.ibatchnumber + ") = " + super.sBatchNumber() + ")"
			+ " AND ((" + SMTableentries.TableName + "." + SMTableentries.spayeepayor + ") != '')"
			+ ")"
			+ ") AS EntryCustomers"

			+ " ON " + SMTableartransactions.TableName + "." + SMTableartransactions.spayeepayor
			+ " = EntryCustomers." + SMTableentries.spayeepayor
			+ " WHERE ("
			+ "(" + SMTableartransactions.TableName + "." + SMTableartransactions.idoctype + " = 3)"
			+ " AND (" + SMTableartransactions.TableName + "." 
			+ SMTableartransactions.dcurrentamt + " != 0.00)"
			+ ")"
			;

		if (bLogDiagnostics){
			log.writeEntry(
					sUserID, 
					SMLogEntry.LOG_OPERATION_ARBATCHPOST, 
					"In linkPrepays - select prepays with a current amount", 
					SQL,
					"[1376509258]"
			);
		}
		try{
			ResultSet rsPrepays = clsDatabaseFunctions.openResultSet(SQL, conn);
			while(rsPrepays.next()){
				//Get an invoice candidate for linking this prepay:
				SQL = "SELECT * FROM " + SMTableartransactions.TableName
				+ " WHERE ("
				+ "(" + SMTableartransactions.idoctype + " = " + ARDocumentTypes.INVOICE_STRING + ")"
				+ " AND (" + SMTableartransactions.dcurrentamt + " >= " 
				+ clsManageBigDecimals.BigDecimalTo2DecimalSQLFormat(
						rsPrepays.getBigDecimal(SMTableartransactions.dcurrentamt).negate()) + ")"
						+ " AND (" + SMTableartransactions.spayeepayor 
						+ " = '" + rsPrepays.getString(SMTableartransactions.spayeepayor) + "')"
						+ " AND (" + SMTableartransactions.sordernumber 
						+ " = '" + rsPrepays.getString(SMTableartransactions.sordernumber) + "')"
						//Do not apply prepays to retainage transactions:
						+ " AND (" + SMTableartransactions.iretainage + " != 1)"
						+ ")"
						+ " ORDER BY " + SMTableartransactions.datdocdate
						;
				ResultSet rsInvoices = clsDatabaseFunctions.openResultSet(SQL, conn);
				if (bLogDiagnostics){
					log.writeEntry(
							sUserID, 
							SMLogEntry.LOG_OPERATION_ARBATCHPOST, 
							"In linkPrepays, querying for invoices with order # " 
							+  rsPrepays.getString(SMTableartransactions.sordernumber),
							SQL,
							"[1376509267]"
					);
				}
				//If we find a candidate, create a set of matching lines for it:
				if(rsInvoices.next()){
					//First, the apply-from doc:
					//Get the transaction date, whichever (the apply-to or the apply-from)
					//is later:
					java.sql.Date datInvoice = rsInvoices.getDate(SMTableartransactions.datdocdate);
					java.sql.Date datPrepay = rsPrepays.getDate(SMTableartransactions.datdocdate);
					java.sql.Date datMatch;
					if(datPrepay.before(datInvoice)){
						datMatch = datInvoice;
					}else{
						datMatch = datPrepay;
					}
					//One line for the prepay - this line points to the applied to doc:
					//NOTE: parent IDs and retainage flags get updated here on prepays:
					String sPrepayDocNumber = rsPrepays.getString(SMTableartransactions.sdocnumber);
					String sInvoiceNumber = rsInvoices.getString(SMTableartransactions.sdocnumber);
					String sPayeePayor = rsPrepays.getString(SMTableartransactions.spayeepayor);
					BigDecimal bdAmount = rsPrepays.getBigDecimal(SMTableartransactions.dcurrentamt);
					SQL = "INSERT INTO " + SMTablearmatchingline.TableName
					+ "("
					+  SMTablearmatchingline.damount
					+ ", " + SMTablearmatchingline.dattransactiondate
					+ ", " + SMTablearmatchingline.sapplytodoc
					+ ", " + SMTablearmatchingline.sdescription
					+ ", " + SMTablearmatchingline.sdocnumber
					+ ", " + SMTablearmatchingline.spayeepayor
					+ ", " + SMTablearmatchingline.lparenttransactionid
					+ ", " + SMTablearmatchingline.iretainage
					+ ", " + SMTablearmatchingline.ldocappliedtoid

					+ ") VALUES ("
					+  clsManageBigDecimals.BigDecimalTo2DecimalSQLFormat(bdAmount.negate())
					+ ", '" + clsDateAndTimeConversions.utilDateToString(datMatch, "yyyy-MM-dd") + "'"
					+ ", '" + sInvoiceNumber + "'"
					+ ", '" + clsDatabaseFunctions.FormatSQLStatement(rsPrepays.getString(SMTableartransactions.sdocdescription)) + "'"
					+ ", '" + clsDatabaseFunctions.FormatSQLStatement(sPrepayDocNumber) + "'"
					+ ", '" + clsDatabaseFunctions.FormatSQLStatement(sPayeePayor) + "'"
					//There IS no parent on a system-linked prepay - we indicate this with a zero:
					+ ", 0"
					+ ", " + Long.toString(rsPrepays.getLong(SMTableartransactions.iretainage))
					+ ", " + Long.toString(rsInvoices.getLong(SMTableartransactions.lid))
					+ ")";
					/*
        	    	log.writeEntry(
        	    			sUserName, 
        	        		SMLogEntry.LOG_OPERATION_ARBATCHPOST, 
        	        		"In linkPrepays - inserting a matching line for prepay pointing to apply-to", 
        	        		SQL
        	        		);
					 */            	
					if(!clsDatabaseFunctions.executeSQL(SQL, conn)){
						rsPrepays.close();
						rsInvoices.close();
						super.addErrorMessage("Error inserting armatchingline (1) with SQL: " + SQL + ".");
						return false;
					}
					/*
            		log.writeEntry(
            			sUserName, 
            			SMLogEntry.LOG_OPERATION_ARBATCHPOST, 
            			"In linkPrepays, adding armatchingline record for prepay (apply-from) entry", 
            			""
            		);
					 */

					//Add a GL entry here:
					if (export != null){
						export.addHeader(
								super.sModuleType(), 
								ARDocumentTypes.getSourceTypes(ARDocumentTypes.PREPAYMENT),
								"AR Batch Export", 
								"SMAR",
								sStdBatchDateString(),
								sStdBatchDateString(),
								buildGLTransactionEntryDescription(ARDocumentTypes.PREPAYMENT)
						);
					}

					//Get the customer deposit account for this customer:
					ARCustomer cust = new ARCustomer(sPayeePayor);
					if (!cust.load(conn)){
						super.addErrorMessage("Error reading customer deposit GL account in linkPrepays: " 
								+ cust.getErrorMessageString() + ".");
						return false;
					}

					String sComment = sPayeePayor + " " + cust.getM_sCustomerName();
					sComment = sComment.trim()  + " - Auto-linked to " + sInvoiceNumber;

					String sEntryDesc = "Prepay automatically linked FROM";
					if (sEntryDesc.length() > 60){
						sEntryDesc = sEntryDesc.substring(0, 59).trim();
					}

					String sReference = sPayeePayor + " - " + sPrepayDocNumber;
					if (sReference.length() > 60){
						sReference = sReference.substring(0, 59).trim();
					}
					if (export != null){
						try {
							export.addDetail(
									datMatch,
									bdAmount.negate(),
									cust.getARPrepayLiabilityAccount(conn),
									sComment,
									sEntryDesc,
									sReference,
									"0",
									conn
							);
						} catch (Exception e3) {
							super.addErrorMessage(e3.getMessage() + " - apply-to entry - 01 for ID# " 
									+ Long.toString(rsInvoices.getLong(SMTableartransactions.lid)) + ".");
							rsPrepays.close();
							rsInvoices.close();
							return false;
						}
					}
					
					//Add a chron record here to record the apply-FROM
					//Add a chron entry here for the apply TO line:
					ARTransaction arinvtrans = new ARTransaction();
					try {
						arinvtrans.load(Long.toString(rsInvoices.getLong(SMTableartransactions.lid)),conn);
					} catch (Exception e1) {
						super.addErrorMessage("Could not load apply-to entry - 01 for ID# " 
								+ Long.toString(rsInvoices.getLong(SMTableartransactions.lid)) + ".");
						rsPrepays.close();
						rsInvoices.close();
						return false;
					}
					
					BigDecimal bdCurrentAmt 
					= arinvtrans.getdCurrentAmount().subtract(rsPrepays.getBigDecimal(SMTableartransactions.dcurrentamt).negate());
					try{
						archron.writeEntry(
							rsPrepays.getBigDecimal(SMTableartransactions.dcurrentamt).negate(), 
							-1, 
							super.lBatchNumber(), 
							-1, 
							0, 
							"during batch posting, system AUTOMATICALLY linked " 
							+ clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(rsPrepays.getBigDecimal(SMTableartransactions.dcurrentamt).negate()) 
							+ " from PREPAY Doc # "
							+ sPrepayDocNumber
							+ " to Invoice #" 
							+ sInvoiceNumber
							+ ", reducing the current amt on "
							+ sInvoiceNumber
							+ " to " + clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(bdCurrentAmt)
							, 
							"", 
							sPayeePayor, 
							sUserID,
							sUserFullName,
							SQL, 
							sInvoiceNumber, 
							sPrepayDocNumber, 
							"**SYSTEM GENERATED**"
						);
					} catch (Exception e) {
						super.addErrorMessage("Error [1387575894] inserting archron log entry - " + e.getMessage() + ".");
						rsPrepays.close();
						rsInvoices.close();
						return false;
					}

					//Next, the apply-to doc:
					//NOTE: parent IDs and retainage flags get updated here on prepays:
					SQL = "INSERT INTO " + SMTablearmatchingline.TableName
					+ "("
					+  SMTablearmatchingline.damount
					+ ", " + SMTablearmatchingline.dattransactiondate
					+ ", " + SMTablearmatchingline.sapplytodoc
					+ ", " + SMTablearmatchingline.sdescription
					+ ", " + SMTablearmatchingline.sdocnumber
					+ ", " + SMTablearmatchingline.spayeepayor
					+ ", " + SMTablearmatchingline.lparenttransactionid
					+ ", " + SMTablearmatchingline.iretainage
					+ ", " + SMTablearmatchingline.ldocappliedtoid

					+ ") VALUES ("
					//Sign is reversed on the apply-to matchingline:
					+  clsManageBigDecimals.BigDecimalTo2DecimalSQLFormat(
						rsPrepays.getBigDecimal(SMTableartransactions.dcurrentamt))
						+ ", '" + clsDateAndTimeConversions.utilDateToString(datMatch, "yyyy-MM-dd") + "'"
						+ ", '" + clsDatabaseFunctions.FormatSQLStatement(rsPrepays.getString(SMTableartransactions.sdocnumber)) + "'"
						+ ", '" + clsDatabaseFunctions.FormatSQLStatement(rsPrepays.getString(SMTableartransactions.sdocdescription)) + "'"
						+ ", '" + clsDatabaseFunctions.FormatSQLStatement(sInvoiceNumber) + "'"
						+ ", '" + clsDatabaseFunctions.FormatSQLStatement(rsPrepays.getString(SMTableartransactions.spayeepayor)) + "'"
						//There IS no parent on a system-linked prepay - we indicate this with a zero:
						+ ", 0"
						+ ", " + Long.toString(rsInvoices.getLong(SMTableartransactions.iretainage))
						+ ", " + Long.toString(rsPrepays.getLong(SMTableartransactions.lid))
						+ ")";
					/*
        	    	log.writeEntry(
        	    			sUserName, 
        	        		SMLogEntry.LOG_OPERATION_ARBATCHPOST, 
        	        		"In linkPrepays - inserting a matching line for prepay pointing to apply-from", 
        	        		SQL
        	        		);
					 */
					if(!clsDatabaseFunctions.executeSQL(SQL, conn)){
						rsPrepays.close();
						rsInvoices.close();
						super.addErrorMessage("Error inserting armatchingline with SQL: " + SQL + ".");
						return false;
					}
					/*
            		log.writeEntry(
            				sUserName, 
                			SMLogEntry.LOG_OPERATION_ARBATCHPOST, 
                			"In linkPrepays, adding armatchingline record for invoice (apply-to) entry", 
                			""
                		);
					 */

					//Add a gl export batch entry for the apply-TO matching line:
					sComment = sPayeePayor + " " + cust.getM_sCustomerName();
					sComment = sComment.trim() + " - Auto-linked to " + sPrepayDocNumber; 

					sEntryDesc = "Prepay automatically linked TO";
					if (sEntryDesc.length() > 60){
						sEntryDesc = sEntryDesc.substring(0, 59).trim();
					}
					sReference = sPayeePayor + " - " + sInvoiceNumber;
					if (sReference.length() > 60){
						sReference = sReference.substring(0, 59).trim();
					}
					if (export != null){
						try {
							export.addDetail(
									datMatch,
									bdAmount,
									cust.getARControlAccount(conn),
									sComment,
									sEntryDesc,
									sReference,
									"0",
									conn
							);
						} catch (Exception e2) {
							super.addErrorMessage(e2.getMessage() + " - Doc ID#: " 
									+ Long.toString(rsPrepays.getLong(SMTableartransactions.lid)) + ".");
							rsPrepays.close();
							rsInvoices.close();
							return false;
						}
					}
					
					ARTransaction arprepaytrans = new ARTransaction();
					
					try {
						arprepaytrans.load(Long.toString(rsPrepays.getLong(SMTableartransactions.lid)),conn);
					} catch (Exception e1) {
						super.addErrorMessage("Could not load apply-to entry - 02 for Doc ID#: " 
								+ Long.toString(rsPrepays.getLong(SMTableartransactions.lid)) + ".");
						rsPrepays.close();
						rsInvoices.close();
						return false;
					}
					
					bdCurrentAmt = arprepaytrans.getdCurrentAmount().subtract(
							rsPrepays.getBigDecimal(SMTableartransactions.dcurrentamt));
					//Add a chron record here to record the apply-TO:
					try{
						archron.writeEntry(
							rsPrepays.getBigDecimal(SMTableartransactions.dcurrentamt), 
							-1, 
							super.lBatchNumber(), 
							-1, 
							0, 
							"during batch posting, system AUTOMATICALLY linked " 
							+ clsManageBigDecimals.BigDecimalTo2DecimalSQLFormat(rsPrepays.getBigDecimal(SMTableartransactions.dcurrentamt))
							+ " from Invoice # "
							+ sInvoiceNumber
							+ " to PrePay #" 
							+ sPrepayDocNumber
							+ " reducing the current amt on "
							+ sPrepayDocNumber
							+ " to " + clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(bdCurrentAmt)
							, 
							"", 
							sPayeePayor, 
							sUserID,
							sUserFullName,
							SQL, 
							sPrepayDocNumber, 
							sInvoiceNumber, 
							"**SYSTEM GENERATED**"
						);
					} catch (Exception e) {
						super.addErrorMessage("Error [1387576013] inserting archron entry - " + e.getMessage() + ".");
						rsPrepays.close();
						rsInvoices.close();
						return false;
					}
					//Now update the current amounts for these two artransactions:
					//First, update the apply-to transaction:
					//We add the amount of the prepay transaction, which is negative, to the current
					//amount of the apply-to invoice:
					SQL = "UPDATE " 
						+ SMTableartransactions.TableName 
						+ " SET " 
						+ SMTableartransactions.dcurrentamt + " = " 
						+ SMTableartransactions.dcurrentamt + " + (" 
						+ clsManageBigDecimals.BigDecimalTo2DecimalSQLFormat(
								rsPrepays.getBigDecimal(SMTableartransactions.dcurrentamt)) + ")"
						+ " WHERE ("
							+ "(" + SMTableartransactions.TableName + "." + SMTableartransactions.lid + " = " 
							+ Long.toString(rsInvoices.getLong(SMTableartransactions.lid)) + ")"
						+ ")"
					;
					/*
        	    	log.writeEntry(
        	    			sUserName, 
        	        		SMLogEntry.LOG_OPERATION_ARBATCHPOST, 
        	        		"In linkPrepays - updating apply-to trans", 
        	        		SQL
        	        		);
					 */
					if(!clsDatabaseFunctions.executeSQL(SQL, conn)){
						rsPrepays.close();
						rsInvoices.close();
						super.addErrorMessage("Error updating prepay apply-to.");
						return false;
					}
					/*
            		log.writeEntry(
            			sUserName, 
            			SMLogEntry.LOG_OPERATION_ARBATCHPOST, 
            			"In linkPrepays, updating current amount on apply-to doc", 
            			""
            		);
					 */
					//Next, update the apply-from transaction:
					SQL = "UPDATE " 
						+ SMTableartransactions.TableName 
						+ " SET " 
						+ SMTableartransactions.dcurrentamt + " = " 
						+ SMTableartransactions.dcurrentamt + " - (" 
						+ clsManageBigDecimals.BigDecimalTo2DecimalSQLFormat(
								rsPrepays.getBigDecimal(SMTableartransactions.dcurrentamt)) + ")"
						+ " WHERE ("
							+ "(" + SMTableartransactions.TableName + "." + SMTableartransactions.lid + " = " 
							+ Long.toString(rsPrepays.getLong(SMTableartransactions.lid)) + ")"
						+ ")"
					;
					/*
        	    	log.writeEntry(
        	    			sUserName, 
        	        		SMLogEntry.LOG_OPERATION_ARBATCHPOST, 
        	        		"In linkPrepays - updating apply-from trans", 
        	        		SQL
        	        		);
					 */
					if(!clsDatabaseFunctions.executeSQL(SQL, conn)){
						rsPrepays.close();
						rsInvoices.close();
						super.addErrorMessage("Error updating prepay apply-to.");
						return false;
					}
					/*
            		log.writeEntry(
            				sUserName, 
                			SMLogEntry.LOG_OPERATION_ARBATCHPOST, 
                			"In linkPrepays, updating current amount on apply-from doc", 
                			""
                		);
					 */
				} //end if(rsInvoices.next()){
				rsInvoices.close();
			} //end while(rsPrepays.next()){
			rsPrepays.close();
		}catch(SQLException e){
			super.addErrorMessage("Error linking prepays - " + e.getMessage());
			return false;
		}

		return true;
	}

	private boolean updateRetainageFlagOnMatchingLines(Connection conn, long lLastMatchingLineID){

		//Need to update the retainage flag
		//If the DOCUMENT APPLIED TO is flagged as retainage, then the armatchingline is ALSO flagged as retainage:
		String SQL = "UPDATE " + SMTablearmatchingline.TableName 
		+ ", " + SMTableartransactions.TableName + " SET "
		+ SMTablearmatchingline.TableName + "." + SMTablearmatchingline.iretainage 
		+ "=" + SMTableartransactions.TableName  + "." + SMTableartransactions.iretainage
		+ " WHERE ("
		+ "(" + SMTablearmatchingline.TableName + "." + SMTablearmatchingline.ldocappliedtoid
		+ "=" + SMTableartransactions.TableName  + "." + SMTableartransactions.lid + ")"
		+ " AND (" + SMTablearmatchingline.TableName + "." + SMTablearmatchingline.lid
		+ " > " + Long.toString(lLastMatchingLineID) + ")"
		+ ")"
		;
		/*
    	log.writeEntry(
    			sUserName, 
        		SMLogEntry.LOG_OPERATION_ARBATCHPOST, 
        		"In updateRetainageFlagOnMatchingLines - Updating armatchinglines retainage", 
        		SQL
        		);
		 */
		try{
			if(!clsDatabaseFunctions.executeSQL(SQL, conn)){
				super.addErrorMessage("Error updating armatchingline retainage flag.");
				return false;
			}
		}catch(SQLException e){
			super.addErrorMessage("Error updating armatchingline retainage flag - " + e.getMessage());
			return false;
		}
		/*
    	log.writeEntry(
    		sUserName, 
    		SMLogEntry.LOG_OPERATION_ARBATCHPOST, 
    		"In updateRetainageFlagOnMatchingLines - records updated successfully", 
    		""
    	);
		 */
		return true;

	}
	private void checkEntry(String sEntryNumber, String sEntryID, Connection conn, String sUserID) throws Exception{

		//Check every entry to make sure they are all in balance - if any aren't, add the error message
		//and return false:
		AREntry entry = new AREntry();

		if (!entry.load(sEntryID, conn)){
			throw new Exception("<br>In checking entries - could not load entry with ID " + sEntryID);
		}

		BigDecimal bdZero = new BigDecimal(0);
		if (entry.dUnDistributedAmount().compareTo(bdZero) != 0){
			throw new Exception(
				"<br>Entry number " 
				+ sEntryNumber 
				+ " is out of balance by " 
				+ entry.sUnDistributedAmountSTDFormat() + ".");
		}

		try {
			entry.checkIfKeyIsUnique(conn);
		} catch (Exception e1) {
			throw new Exception("Entry " + sEntryNumber 
					+ " has the same customer and document number as an existing open transaction.");
		}
		
		//Make sure that the entry date is within the posting period:
		SMOption opt = new SMOption();
		try {
			opt.checkDateForPosting(entry.sStdDocDate(),
				"Entry date for entry number " + Integer.toString(entry.iEntryNumber()), 
				conn,
				sUserID);
		} catch (Exception e) {
			throw new Exception("<br>" + e.getMessage() + ".");
		}
		
		//If it's a reversal, check to make sure that ALL the armatchinglines are available to re reversed:
		//if (entry.getDocumentType() == ARDocumentTypes.REVERSAL){
		//	if (!checkReversal(entry, conn)){
		//		return false;
		//	}
		//}
		return;
	}
	private void createTransaction(String sEntryNumber, 
			String sEntryID, 
			Connection conn,
			String sUserID,
			String sUserFullName) throws Exception{

		//Open the entry, post it to the opentransactions and armatchinglines tables:
		AREntry entry = new AREntry();
		if (!entry.load(sEntryID, conn)){
			throw new Exception("<br>In checking entries - could not load entry with ID " + sEntryID);
		}    	

		//Add the entry as a transaction in artransactions:    	
		ARTransaction trans = new ARTransaction();
		//Populate the transaction:
		trans.setBatchNumber(super.sBatchNumber());
		trans.setControlAcct(entry.sControlAcct());
		trans.setCurrentAmount(entry.dOriginalAmount());
		trans.setCustomerNumber(entry.sCustomerNumber());
		trans.setDocDate("MM/dd/yyyy", entry.sStdDocDate());
		trans.setDocDescription(entry.sDocDescription().trim());
		trans.setDocNumber(entry.sDocNumber().trim());
		trans.setDocType(entry.sDocumentType());
		trans.setDueDate("MM/dd/yyyy", entry.sStdDueDate());
		trans.setEntryNumber(entry.sEntryNumber());
		trans.setOrderNumber(entry.sOrderNumber().trim());
		trans.setPONumber(entry.sPONumber().trim());
		trans.setOriginalAmount(entry.dOriginalAmount());
		trans.setTerms(entry.sTerms());
		if(entry.sDocumentType().compareToIgnoreCase(ARDocumentTypes.RETAINAGE_STRING) == 0){
			trans.setRetainageFlag(1);
		}else{
			trans.setRetainageFlag(0);
		}
		
		try {
			trans.save_without_data_transaction(conn, sUserFullName);
		} catch (Exception e1) {
			throw new Exception("Error saving transaction for entry " + sEntryNumber + " - " + trans.getErrorMessage());
		}
		
		if(!trans.load(conn)){
			throw new Exception("Could not reload transaction for entry " + sEntryNumber + " - " + trans.getErrorMessage());
		}
		//Record a chron file entry here for the artransaction:
		try {
			archron.writeEntry(
				entry.dOriginalAmount(), 
				entry.getDocumentType(), 
				super.lBatchNumber(), 
				Long.parseLong(sEntryNumber), 
				-1, 
				"posted " + ARDocumentTypes.Get_Document_Type_Label(entry.getDocumentType())
				+ " for " + clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(entry.dOriginalAmount()) 
				+ " with Doc # " + trans.getDocNumber() + " when posting batch number " + super.sBatchNumber()
				+ ", setting the current amt to " + trans.getCurrentAmountSTDFormat()
				, 
				trans.getDocNumber(), 
				trans.getCustomerNumber(), 
				sUserID, 
				sUserFullName,
				"(USED ARTransaction.save method)", 
				trans.getDocNumber(), 
				"", 
				trans.getDocNumber()
			);
		} catch (NumberFormatException e) {
			throw new Exception("Error [1387574921] formatting entrynumber '" + sEntryNumber + "' - " + e.getMessage() 
				+ " - Could not insert chron log entry for first apply-to armatchingline - ");
		} catch (Exception e) {
			throw new Exception("Error [1387574922]  - " + e.getMessage() 
				+ " - Could not insert chron log entry for first apply-to armatchingline - ");
		}

		/*
    	log.writeEntry(
    		sUserName, 
    		SMLogEntry.LOG_OPERATION_ARBATCHPOST, 
    		"In createTransaction, transaction saved", 
    		"Batch entry # " + sEntryNumber + ", artransaction ID: " + trans.getlTransactionID()
    	);
		 */
		return;
	}
	public boolean createGLBatch(Connection conn, SMGLExport export){

		//Get the entries and lines from the batch:
		String SQL = "SELECT "
			+ SMTableentries.lid
			+ ", " + SMTableentries.ientrynumber
			+ " FROM " + SMTableentries.TableName
			+ " WHERE (" 
			+ SMTableentries.ibatchnumber + " = " + super.sBatchNumber()
			+ ")"
			+ " ORDER BY " + SMTableentries.ientrynumber + " ASC";
		try {
			ResultSet rsEntryListForGLExport = clsDatabaseFunctions.openResultSet(SQL, conn);
			while (rsEntryListForGLExport.next()){
				AREntry entry = new AREntry();
				if (!entry.load(rsEntryListForGLExport.getString(SMTableentries.lid), conn)){
					super.addErrorMessage("Could not load entry " + rsEntryListForGLExport.getLong(SMTableentries.ientrynumber) + " for export");
					rsEntryListForGLExport.close();
					return false;
				}

				export.addHeader(
						super.sModuleType(), 
						ARDocumentTypes.getSourceTypes(entry.getDocumentType()),
						"AR Batch Export", 
						"SMAR",
						entry.sStdDocDate(),
						sStdBatchDateString(),
						buildGLTransactionEntryDescription(entry.getDocumentType())
				);

				//First, add the entry itself as a GL transaction:
				BigDecimal bdentryAmount = BigDecimal.ZERO;

				//Cash, prepays, and misc receipts have negative values, but INCREASE the cash account, so
				//they must be written to the import file as POSITIVE values:
				if(
						entry.getDocumentType() == ARDocumentTypes.RECEIPT
						|| entry.getDocumentType() == ARDocumentTypes.PREPAYMENT
						|| entry.getDocumentType() == ARDocumentTypes.MISCRECEIPT
						|| entry.getDocumentType() == ARDocumentTypes.APPLYTO
						|| entry.getDocumentType() == ARDocumentTypes.REVERSAL
				){
					bdentryAmount = entry.dOriginalAmount().negate();
				}else{
					bdentryAmount = entry.dOriginalAmount();
				}

				ARCustomer cust = new ARCustomer(entry.sCustomerNumber());
				String sComment = entry.sCustomerNumber();
				if (cust.load(conn)){
					sComment = sComment + " " + cust.getM_sCustomerName();
				}
				sComment = sComment.trim() + " - Batch " + super.sBatchNumber() 
				+ ", entry " + entry.sEntryNumber();

				String sEntryDesc = entry.sDocDescription();
				if (sEntryDesc.length() > 60){
					sEntryDesc = sEntryDesc.substring(0, 59).trim();
				}

				String sReference = entry.sCustomerNumber() + " - " + entry.sDocNumber();
				if (sReference.length() > 60){
					sReference = sReference.substring(0, 59).trim();
				}
				
				try {
					export.addDetail(
							entry.DocDate(),
							bdentryAmount,
							entry.sControlAcct(),
							sComment,
							sEntryDesc,
							sReference,
							"0",
							conn
					);
				} catch (Exception e1) {
					super.addErrorMessage("Error [1478816643] - " + e1.getMessage());
					return false;
				}

				for (int i = 0; i < entry.iLastLine(); i ++){
					//Now add each line from the entry as a GL transaction:
					ARLine line = entry.getLineByIndex(i);

					//Retainage lines AND entries are always positive, so we have to reverse one of those
					// for the GL, even though they both go to the same account and cancel each other:
					//Cash, prepay, and misc cash entries need to reverse the apply-to account (AR)
					//in the GL:
					BigDecimal bdLineAmount = BigDecimal.ZERO;
					if(
							entry.getDocumentType() == ARDocumentTypes.RETAINAGE
							|| entry.getDocumentType() == ARDocumentTypes.RECEIPT
							|| entry.getDocumentType() == ARDocumentTypes.PREPAYMENT
							|| entry.getDocumentType() == ARDocumentTypes.MISCRECEIPT
							|| entry.getDocumentType() == ARDocumentTypes.APPLYTO
							|| entry.getDocumentType() == ARDocumentTypes.REVERSAL
					){
						bdLineAmount = line.dAmount().negate();
					}else{
						bdLineAmount = line.dAmount();
					}

					String sLineDesc = line.sDescription();
					if (sLineDesc.length() > 60){
						sLineDesc = sLineDesc.substring(0, 59).trim();
					}
					String sLineReference = entry.sCustomerNumber() + " - " + line.sDocAppliedTo();

					if (entry.getDocumentType() == ARDocumentTypes.INVOICE){
						sLineReference = entry.sCustomerNumber() + " - " + entry.sDocNumber();
					}
					if (sLineReference.length() > 60){
						sLineReference = sLineReference.substring(0, 59).trim();
					}
					
					try {
						export.addDetail(
								entry.DocDate(),
								bdLineAmount,
								line.sGLAcct(),
								sComment,
								sLineDesc,
								sLineReference,
								Integer.toString(i + 1),
								conn
						);
					} catch (Exception e) {
						super.addErrorMessage("Error [1478816604] - " + e.getMessage());
						return false;
					}
				}
				/*
        		log.writeEntry(
        			sUserName, 
        			SMLogEntry.LOG_OPERATION_ARBATCHPOST, 
        			"In createGLBatch, adding entry for " + entry.sEntryNumber(), 
        			""
        		);
				 */
			}
			rsEntryListForGLExport.close();
		} catch (SQLException e){
			//System.out.println("Error in " + this.toString() + ".createGLBatch - SQL error: " + e.getMessage());
			super.addErrorMessage("SQL Error opening batch for export: " + e.getMessage());
			return false;
		}

		String sExportBatchNumber = Long.toString(lBatchNumber());
		sExportBatchNumber = ARUtilities.PadLeft(sExportBatchNumber, "0", 6);
		
		try {
			export.saveExport(sExportBatchNumber, conn);
		} catch (Exception e) {
			if (bDebugMode){
				System.out.println("In " + this.toString() + " - save export error message = " + e.getMessage());
				System.out.println("In " + this.toString() + " - URL encode save export error message = " + clsServletUtilities.URLEncode(e.getMessage()));
			}
			super.addErrorMessage("Error saving GL export records - " + clsServletUtilities.URLEncode(e.getMessage()));
			return false;
		}

		AROptions aropt = new AROptions();
		if(!aropt.load(conn)){
			super.addErrorMessage("Error [1474646117] getting export file type - " + aropt.getErrorMessageString());
			return false;
		}
		int iFeedGL = Integer.parseInt(aropt.getFeedGl());
		if (
			(iFeedGL == SMTablearoptions.FEED_GL_BOTH_EXTERNAL_AND_SMCP_GL)
			|| (iFeedGL == SMTablearoptions.FEED_GL_SMCP_GL_ONLY)
				
		){
			GLTransactionBatch gltransactionbatch = null;
			try {
				gltransactionbatch = export.createGLTransactionBatch(
					conn, 
					sCreatedByID(), 
					sLastEditedByID(), 
					sStdBatchDateString(), 
					"AR " + ARBatchTypes.Get_AR_Batch_Type(iBatchType()) + " Batch #" + lBatchNumber()
				);
			} catch (Exception e) {
				super.addErrorMessage("Error [1557446823] creating SMCP GL batch - " + e.getMessage());
				return false;
			}
			
			try {
				gltransactionbatch.save_without_data_transaction(conn, sLastEditedByID(), sLastEditedByFullName(), true);
			} catch (Exception e) {
				super.addErrorMessage("Error [1557446824] saving SMCP GL batch - " + e.getMessage());
				return false;
			}
		}
		
		if (
				(iFeedGL == SMTablearoptions.FEED_GL_BOTH_EXTERNAL_AND_SMCP_GL)
				|| (iFeedGL == SMTablearoptions.FEED_GL_EXTERNAL_GL_ONLY)
					
		){
			if (export.getExportFilePath().compareToIgnoreCase("") != 0){
				if (!export.writeExportFile(
						SMModuleTypes.AR, 
						super.sBatchTypeLabel(), 
						sExportBatchNumber,
						Integer.parseInt(aropt.getExportTo()),
						conn)
				){
					//System.out.println("Error writing GL export file");
					super.addErrorMessage("Error writing GL export file - " + export.getErrorMessage());
					return false;
				}
		    }		
		}

		return true;
	}

	private boolean updateStatistics(
			Connection conn, 
			long lLastTransactionID, 
			long lLastMatchingLineID){

		String SQL = "SELECT * FROM " + SMTableartransactions.TableName
		+ " WHERE ("
		+ "(" + SMTableartransactions.lid + " > " + Long.toString(lLastTransactionID) + ")"
		//And don't update misc receipts . . . 
		+ " AND (" + SMTableartransactions.spayeepayor + " > '')"
		+ ")"
		;
		/*
    	log.writeEntry(
    		sUserName, 
    		SMLogEntry.LOG_OPERATION_ARBATCHPOST, 
    		"In updateStatistics, listing transactions created by this posting", 
    		SQL
    		);
		 */
		try{
			ResultSet rsRecentTransactions = clsDatabaseFunctions.openResultSet(SQL, conn);
			while(rsRecentTransactions.next()){
				//Update the statistics:
				int iDocumentType = rsRecentTransactions.getInt(SMTableartransactions.idoctype);

				//We only care about invoices, receipts, or credits:
				if(
						(iDocumentType == ARDocumentTypes.INVOICE)
						|| (iDocumentType == ARDocumentTypes.CREDIT)
						|| (iDocumentType == ARDocumentTypes.RECEIPT)
				){

					//Update the customer statistics:
					SQL = "SELECT " + SMTablearcustomerstatistics.sCustomerNumber
					+ " FROM " + SMTablearcustomerstatistics.TableName
					+ " WHERE " + 
					SMTablearcustomerstatistics.sCustomerNumber + " = '" 
					+ rsRecentTransactions.getString(SMTableartransactions.spayeepayor) + "'";

					ResultSet rsCustomerStatistics = clsDatabaseFunctions.openResultSet(SQL, conn);
					if(rsCustomerStatistics.next()){
						rsCustomerStatistics.close();
						SQL = "UPDATE " + SMTablearcustomerstatistics.TableName
						+ " SET ";

						if(iDocumentType == ARDocumentTypes.INVOICE){
							SQL = SQL + SMTablearcustomerstatistics.sAmountOfHighestInvoice + " = "
							+ "IF(" + SMTablearcustomerstatistics.sAmountOfHighestInvoice + " < "
							+ clsManageBigDecimals.BigDecimalTo2DecimalSQLFormat(
									rsRecentTransactions.getBigDecimal(SMTableartransactions.doriginalamt)) + ", "
									+ clsManageBigDecimals.BigDecimalTo2DecimalSQLFormat(
											rsRecentTransactions.getBigDecimal(SMTableartransactions.doriginalamt)) + ", "
											+ SMTablearcustomerstatistics.sAmountOfHighestInvoice + ")"

											+ ", " + SMTablearcustomerstatistics.sAmountOfLastInvoice + " = "
											+ "IF(" + SMTablearcustomerstatistics.sDateOfLastInvoice + " < "
											+ "'" + clsDateAndTimeConversions.utilDateToString(
													rsRecentTransactions.getDate(
															SMTableartransactions.datdocdate), "yyyy-MM-dd") + "', "
															+ clsManageBigDecimals.BigDecimalTo2DecimalSQLFormat(
																	rsRecentTransactions.getBigDecimal(SMTableartransactions.doriginalamt)) + ", "
																	+ SMTablearcustomerstatistics.sAmountOfLastInvoice + ")"

																	+ ", " + SMTablearcustomerstatistics.sDateOfLastInvoice + " = "
																	+ "IF(" + SMTablearcustomerstatistics.sDateOfLastInvoice + " < "
																	+ "'" + clsDateAndTimeConversions.utilDateToString(
																			rsRecentTransactions.getDate(
																					SMTableartransactions.datdocdate), "yyyy-MM-dd") + "', "
																					+ "'" + clsDateAndTimeConversions.utilDateToString(
																							rsRecentTransactions.getDate(
																									SMTableartransactions.datdocdate), "yyyy-MM-dd") + "', "
																									+ SMTablearcustomerstatistics.sDateOfLastInvoice + ")"
																									;
						} //end if(iDocumentType == ARDocumentTypes.INVOICE){
						if(iDocumentType == ARDocumentTypes.CREDIT){
							SQL = SQL + SMTablearcustomerstatistics.sAmountOfLastCredit + " = "
							+ "IF(" + SMTablearcustomerstatistics.sDateOfLastCredit + " < "
							+ "'" + clsDateAndTimeConversions.utilDateToString(
									rsRecentTransactions.getDate(
											SMTableartransactions.datdocdate), "yyyy-MM-dd") + "', "
											+ clsManageBigDecimals.BigDecimalTo2DecimalSQLFormat(
													rsRecentTransactions.getBigDecimal(
															SMTableartransactions.doriginalamt)) + ", "
															+ SMTablearcustomerstatistics.sAmountOfLastCredit + ")"

															+ ", " + SMTablearcustomerstatistics.sDateOfLastCredit + " = "
															+ "IF(" + SMTablearcustomerstatistics.sDateOfLastCredit + " < "
															+ "'" + clsDateAndTimeConversions.utilDateToString(
																	rsRecentTransactions.getDate(
																			SMTableartransactions.datdocdate), "yyyy-MM-dd") + "', "
																			+ "'" + clsDateAndTimeConversions.utilDateToString(
																					rsRecentTransactions.getDate(
																							SMTableartransactions.datdocdate), "yyyy-MM-dd") + "', "
																							+ SMTablearcustomerstatistics.sDateOfLastCredit + ")";

						} //end if(iDocumentType == ARDocumentTypes.CREDIT){
						if((iDocumentType == ARDocumentTypes.RECEIPT) 
								|| (iDocumentType == ARDocumentTypes.RECEIPT)){

							SQL = SQL + SMTablearcustomerstatistics.sAmountOfLastPayment + " = "
							+ "IF(" + SMTablearcustomerstatistics.sDateOfLastPayment + " < "
							+ "'" + clsDateAndTimeConversions.utilDateToString(
									rsRecentTransactions.getDate(
											SMTableartransactions.datdocdate), "yyyy-MM-dd") + "', "
											+ clsManageBigDecimals.BigDecimalTo2DecimalSQLFormat(
													rsRecentTransactions.getBigDecimal(
															SMTableartransactions.doriginalamt)) + ", "
															+ SMTablearcustomerstatistics.sAmountOfLastPayment + ")"

															+ ", " + SMTablearcustomerstatistics.sDateOfLastPayment + " = "
															+ "IF(" + SMTablearcustomerstatistics.sDateOfLastPayment + " < "
															+ "'" + clsDateAndTimeConversions.utilDateToString(
																	rsRecentTransactions.getDate(
																			SMTableartransactions.datdocdate), "yyyy-MM-dd") + "', "
																			+ "'" + clsDateAndTimeConversions.utilDateToString(
																					rsRecentTransactions.getDate(
																							SMTableartransactions.datdocdate), "yyyy-MM-dd") + "', "
																							+ SMTablearcustomerstatistics.sDateOfLastPayment + ")";

						} // end if((iDocumentType == ARDocumentTypes.RECEIPT) 
						//		|| (iDocumentType == ARDocumentTypes.RECEIPT)){

						SQL = SQL + " WHERE ("
						+ "(" + SMTablearcustomerstatistics.sCustomerNumber 
						+ " = '" + rsRecentTransactions.getString(SMTableartransactions.spayeepayor) + "')"
						+ ")"
						;
					}else{
						rsCustomerStatistics.close();
						//New record - insert:
						if(iDocumentType == ARDocumentTypes.INVOICE){
							SQL = "INSERT INTO " + SMTablearcustomerstatistics.TableName + "("
							+ SMTablearcustomerstatistics.sCustomerNumber
							+ ", " + SMTablearcustomerstatistics.sAmountOfHighestInvoice
							+ ", " + SMTablearcustomerstatistics.sAmountOfLastInvoice
							+ ", " + SMTablearcustomerstatistics.sDateOfLastInvoice
							+ ") VALUES ("
							+ "'" + rsRecentTransactions.getString(SMTableartransactions.spayeepayor) + "'"
							+ ", " + clsManageBigDecimals.BigDecimalTo2DecimalSQLFormat(
									rsRecentTransactions.getBigDecimal(SMTableartransactions.doriginalamt))
									+ ", " + clsManageBigDecimals.BigDecimalTo2DecimalSQLFormat(
											rsRecentTransactions.getBigDecimal(SMTableartransactions.doriginalamt)) + ", "
											+ "'" + clsDateAndTimeConversions.utilDateToString(
													rsRecentTransactions.getDate(
															SMTableartransactions.datdocdate), "yyyy-MM-dd") + "'"
															+ ")";
						} //end if(iDocumentType == ARDocumentTypes.INVOICE){
						if(iDocumentType == ARDocumentTypes.CREDIT){
							SQL = "INSERT INTO " + SMTablearcustomerstatistics.TableName + "("
							+ SMTablearcustomerstatistics.sCustomerNumber
							+ ", " + SMTablearcustomerstatistics.sAmountOfLastCredit
							+ ", " + SMTablearcustomerstatistics.sDateOfLastCredit
							+ ") VALUES ("
							+ "'" + rsRecentTransactions.getString(SMTableartransactions.spayeepayor) + "'"
							+ ", " + clsManageBigDecimals.BigDecimalTo2DecimalSQLFormat(
									rsRecentTransactions.getBigDecimal(SMTableartransactions.doriginalamt)) + ", "
									+ "'" + clsDateAndTimeConversions.utilDateToString(
											rsRecentTransactions.getDate(
													SMTableartransactions.datdocdate), "yyyy-MM-dd") + "'"
													+ ")";
						} //end if(iDocumentType == ARDocumentTypes.CREDIT){
						if((iDocumentType == ARDocumentTypes.RECEIPT) 
								|| (iDocumentType == ARDocumentTypes.RECEIPT)){
							SQL = "INSERT INTO " + SMTablearcustomerstatistics.TableName + "("
							+ SMTablearcustomerstatistics.sCustomerNumber
							+ ", " + SMTablearcustomerstatistics.sAmountOfLastPayment
							+ ", " + SMTablearcustomerstatistics.sDateOfLastPayment
							+ ") VALUES ("
							+ "'" + rsRecentTransactions.getString(SMTableartransactions.spayeepayor) + "'"
							+ ", " + clsManageBigDecimals.BigDecimalTo2DecimalSQLFormat(
									rsRecentTransactions.getBigDecimal(SMTableartransactions.doriginalamt)) + ", "
									+ "'" + clsDateAndTimeConversions.utilDateToString(
											rsRecentTransactions.getDate(
													SMTableartransactions.datdocdate), "yyyy-MM-dd") + "'"
													+ ")";
						} // end if((iDocumentType == ARDocumentTypes.RECEIPT) 
						//		|| (iDocumentType == ARDocumentTypes.RECEIPT)){
						;
					}

					//Execute the statement:
					/*
        	    	log.writeEntry(
        	    			sUserName, 
        	        		SMLogEntry.LOG_OPERATION_ARBATCHPOST, 
        	        		"In updateStatistics - updating/inserting statistics", 
        	        		SQL
        	        		);
					 */
					if(!clsDatabaseFunctions.executeSQL(SQL, conn)){
						super.addErrorMessage("Could not execute update/insert statement in arcustomerstatistics.");
						rsRecentTransactions.close();
						return false;
					}

				}
				//Update the current balance and the highest balance for this customer
				// in the arcustomerstatistics table:
				//We DO NOT COUNT retainage transactions in calculating the current balance:
				SQL = "SELECT SUM(" + SMTableartransactions.dcurrentamt + ") AS CURRENTBALANCE"
				+ " FROM " + SMTableartransactions.TableName
				+ " WHERE ("
				+ "(" + SMTableartransactions.spayeepayor 
				+ " = '" + rsRecentTransactions.getString(SMTableartransactions.spayeepayor) + "')"
				+ " AND (" + SMTableartransactions.iretainage 
				+ " = 0)"
				+ ")"
				;

				ResultSet rsBalance = clsDatabaseFunctions.openResultSet(SQL, conn);
				BigDecimal bdBalance = new BigDecimal(0);
				if(rsBalance.next()){
					bdBalance = rsBalance.getBigDecimal("CURRENTBALANCE");
					SQL = "UPDATE " + SMTablearcustomerstatistics.TableName
					+ " SET " + SMTablearcustomerstatistics.sCurrentBalance + " = "
					+ clsManageBigDecimals.BigDecimalTo2DecimalSQLFormat(bdBalance)
					+ ", " + SMTablearcustomerstatistics.sHighestBalance + " = "
					+ "IF(" + SMTablearcustomerstatistics.sHighestBalance + " < "
					+ clsManageBigDecimals.BigDecimalTo2DecimalSQLFormat(bdBalance) + ", "
					+ clsManageBigDecimals.BigDecimalTo2DecimalSQLFormat(bdBalance) + ", "
					+ SMTablearcustomerstatistics.sHighestBalance + ")"
					+ " WHERE ("
					+ "(" + SMTablearcustomerstatistics.sCustomerNumber 
					+ " = '" + rsRecentTransactions.getString(SMTableartransactions.spayeepayor) + "')" 
					+ ")"
					;
					/*
        	    	log.writeEntry(
        	    			sUserName, 
        	        		SMLogEntry.LOG_OPERATION_ARBATCHPOST, 
        	        		"In updateStatistics - updating customer balance statistics", 
        	        		SQL
        	        		);
					 */
					if(!clsDatabaseFunctions.executeSQL(SQL, conn)){
						super.addErrorMessage("Could not update customer balance statistics.");
						rsBalance.close();
						rsRecentTransactions.close();
						return false;
					}
				}
				rsBalance.close();

				//Number of open invoices
				//We'll get this by just reading artransactions for this customer:
				//We DON'T count retainage transactions as open invoices:
				SQL = "SELECT COUNT(" + SMTableartransactions.lid + ") AS OPENINVOICECOUNT"
				+ " FROM " + SMTableartransactions.TableName
				+ " WHERE ("
				+ "(" + SMTableartransactions.spayeepayor 
				+ " = '" + rsRecentTransactions.getString(SMTableartransactions.spayeepayor) + "')"
				+ " AND (" + SMTableartransactions.idoctype + " = " + ARDocumentTypes.INVOICE_STRING + ")"
				+ " AND (" + SMTableartransactions.dcurrentamt + " != 0.00)"
				+ ")"
				;
				long lNumberOfOpenInvoices = 0;
				ResultSet rsOpenInvoices = clsDatabaseFunctions.openResultSet(SQL, conn);
				if(rsOpenInvoices.next()){
					lNumberOfOpenInvoices = rsOpenInvoices.getLong("OPENINVOICECOUNT");
				}
				rsOpenInvoices.close();
				SQL = "UPDATE " + SMTablearcustomerstatistics.TableName
				+ " SET " + SMTablearcustomerstatistics.sNumberOfOpenInvoices + " = "
				+ Long.toString(lNumberOfOpenInvoices)
				+ " WHERE ("
				+ "(" + SMTablearcustomerstatistics.sCustomerNumber 
				+ " = '" + rsRecentTransactions.getString(SMTableartransactions.spayeepayor) + "')" 
				+ ")"
				;
				/*
				log.writeEntry(
						sUserName, 
    					SMLogEntry.LOG_OPERATION_ARBATCHPOST, 
    					"In updateStatistics, updating open invoice count", 
    					SQL
    				);
				 */
				if(!clsDatabaseFunctions.executeSQL(SQL, conn)){
					super.addErrorMessage("Could not update open invoice count.");
					rsOpenInvoices.close();
					rsRecentTransactions.close();
					return false;
				}

				//Update the monthly statistics:
				//We only care about invoices, receipts, or credits:
				if(
						(iDocumentType == ARDocumentTypes.INVOICE)
						|| (iDocumentType == ARDocumentTypes.CREDIT)
						|| (iDocumentType == ARDocumentTypes.RECEIPT)
				){
					//See if we need to insert or update:
					//Update the customer statistics:
					SQL = "SELECT " + SMTablearmonthlystatistics.sCustomerNumber
					+ " FROM " + SMTablearmonthlystatistics.TableName
					+ " WHERE (" 
					+ "(" + SMTablearmonthlystatistics.sCustomerNumber + " = '" 
					+ rsRecentTransactions.getString(SMTableartransactions.spayeepayor) + "')"
					+ " AND (" + SMTablearmonthlystatistics.sMonth + " = " 
					+ clsDateAndTimeConversions.utilDateToString(rsRecentTransactions.getDate(SMTableartransactions.datdocdate),"M") + ")"
					+ " AND (" + SMTablearmonthlystatistics.sYear + " = " 
					+ clsDateAndTimeConversions.utilDateToString(rsRecentTransactions.getDate(SMTableartransactions.datdocdate),"yyyy") + ")"
					+ ")"	
					;

					ResultSet rsCustomerMonthlyStatistics = clsDatabaseFunctions.openResultSet(SQL, conn);
					if(rsCustomerMonthlyStatistics.next()){
						rsCustomerMonthlyStatistics.close();
						//We need to UPDATE the current record:
						SQL = "UPDATE " + SMTablearmonthlystatistics.TableName
						+ " SET "
						;
						if(iDocumentType == ARDocumentTypes.INVOICE){
							SQL = SQL + SMTablearmonthlystatistics.sInvoiceTotal + " = "
							+ SMTablearmonthlystatistics.sInvoiceTotal + " + "
							+ clsManageBigDecimals.BigDecimalTo2DecimalSQLFormat(
									rsRecentTransactions.getBigDecimal(SMTableartransactions.doriginalamt))

									+ ", " + SMTablearmonthlystatistics.sNumberOfInvoices + " = "
									+ SMTablearmonthlystatistics.sNumberOfInvoices + " + 1"
									;
						} //end if(iDocumentType == ARDocumentTypes.INVOICE){
						if(iDocumentType == ARDocumentTypes.CREDIT){
							SQL = SQL + SMTablearmonthlystatistics.sCreditTotal + " = "
							+ SMTablearmonthlystatistics.sCreditTotal + " + "
							+ clsManageBigDecimals.BigDecimalTo2DecimalSQLFormat(
									rsRecentTransactions.getBigDecimal(SMTableartransactions.doriginalamt))

									+ ", " + SMTablearmonthlystatistics.sNumberOfCredits + " = "
									+ SMTablearmonthlystatistics.sNumberOfCredits + " + 1"
									;
						} //end if(iDocumentType == ARDocumentTypes.CREDIT){
						if((iDocumentType == ARDocumentTypes.RECEIPT) 
								|| (iDocumentType == ARDocumentTypes.RECEIPT)){

							SQL = SQL + SMTablearmonthlystatistics.sPaymentTotal + " = "
							+ SMTablearmonthlystatistics.sPaymentTotal + " + "
							+ clsManageBigDecimals.BigDecimalTo2DecimalSQLFormat(
									rsRecentTransactions.getBigDecimal(SMTableartransactions.doriginalamt))

									+ ", " + SMTablearmonthlystatistics.sNumberOfPayments + " = "
									+ SMTablearmonthlystatistics.sNumberOfPayments + " + 1"
									;					} // end if((iDocumentType == ARDocumentTypes.RECEIPT) 

						SQL = SQL + " WHERE ("
						+ "(" + SMTablearmonthlystatistics.sCustomerNumber 
						+ " = '" + rsRecentTransactions.getString(SMTableartransactions.spayeepayor) + "')"

						+ " AND (" + SMTablearmonthlystatistics.sMonth + " = " 
						+ clsDateAndTimeConversions.utilDateToString(rsRecentTransactions.getDate(SMTableartransactions.datdocdate),"M") + ")"
						+ " AND (" + SMTablearmonthlystatistics.sYear + " = " 
						+ clsDateAndTimeConversions.utilDateToString(rsRecentTransactions.getDate(SMTableartransactions.datdocdate),"yyyy") + ")"
						+ ")"
						;

					}else{
						//We need to INSERT a new record:
						rsCustomerMonthlyStatistics.close();
						if(iDocumentType == ARDocumentTypes.INVOICE){
							SQL = "INSERT INTO " + SMTablearmonthlystatistics.TableName + "("
							+ SMTablearmonthlystatistics.sCustomerNumber
							+ ", " + SMTablearmonthlystatistics.sMonth
							+ ", " + SMTablearmonthlystatistics.sYear
							+ ", " + SMTablearmonthlystatistics.sInvoiceTotal
							+ ", " + SMTablearmonthlystatistics.sNumberOfInvoices
							+ ") VALUES ('"
							+ rsRecentTransactions.getString(SMTableartransactions.spayeepayor) + "'"
							+ ", " + clsDateAndTimeConversions.utilDateToString(
									rsRecentTransactions.getDate(SMTableartransactions.datdocdate),"M")
									+ ", " + clsDateAndTimeConversions.utilDateToString(
											rsRecentTransactions.getDate(SMTableartransactions.datdocdate),"yyyy")
											+ ", " + clsManageBigDecimals.BigDecimalTo2DecimalSQLFormat(
													rsRecentTransactions.getBigDecimal(SMTableartransactions.doriginalamt))
													+ ", 1" 
													+ ")"
													;
						} //end if(iDocumentType == ARDocumentTypes.INVOICE){
						if(iDocumentType == ARDocumentTypes.CREDIT){
							SQL = "INSERT INTO " + SMTablearmonthlystatistics.TableName + "("
							+ SMTablearmonthlystatistics.sCustomerNumber
							+ ", " + SMTablearmonthlystatistics.sMonth
							+ ", " + SMTablearmonthlystatistics.sYear
							+ ", " + SMTablearmonthlystatistics.sCreditTotal
							+ ", " + SMTablearmonthlystatistics.sNumberOfCredits
							+ ") VALUES ('"
							+ rsRecentTransactions.getString(SMTableartransactions.spayeepayor) + "'"
							+ ", " + clsDateAndTimeConversions.utilDateToString(
									rsRecentTransactions.getDate(SMTableartransactions.datdocdate),"M")
									+ ", " + clsDateAndTimeConversions.utilDateToString(
											rsRecentTransactions.getDate(SMTableartransactions.datdocdate),"yyyy")
											+ ", " + clsManageBigDecimals.BigDecimalTo2DecimalSQLFormat(
													rsRecentTransactions.getBigDecimal(SMTableartransactions.doriginalamt))
													+ ", 1" 
													+ ")"
													;					} //end if(iDocumentType == ARDocumentTypes.CREDIT){
						if((iDocumentType == ARDocumentTypes.RECEIPT) 
								|| (iDocumentType == ARDocumentTypes.RECEIPT)){
							SQL = "INSERT INTO " + SMTablearmonthlystatistics.TableName + "("
							+ SMTablearmonthlystatistics.sCustomerNumber
							+ ", " + SMTablearmonthlystatistics.sMonth
							+ ", " + SMTablearmonthlystatistics.sYear
							+ ", " + SMTablearmonthlystatistics.sPaymentTotal
							+ ", " + SMTablearmonthlystatistics.sNumberOfPayments
							+ ") VALUES ('"
							+ rsRecentTransactions.getString(SMTableartransactions.spayeepayor) + "'"
							+ ", " + clsDateAndTimeConversions.utilDateToString(
									rsRecentTransactions.getDate(SMTableartransactions.datdocdate),"M")
									+ ", " + clsDateAndTimeConversions.utilDateToString(
											rsRecentTransactions.getDate(SMTableartransactions.datdocdate),"yyyy")
											+ ", " + clsManageBigDecimals.BigDecimalTo2DecimalSQLFormat(
													rsRecentTransactions.getBigDecimal(SMTableartransactions.doriginalamt))
													+ ", 1" 
													+ ")"
													;					} // end if((iDocumentType == ARDocumentTypes.RECEIPT) 
						//		|| (iDocumentType == ARDocumentTypes.RECEIPT)){
						;

					}
					//Execute the statement:
					/*
	    			log.writeEntry(
	    					sUserName, 
		    				SMLogEntry.LOG_OPERATION_ARBATCHPOST, 
		    				"In updateStatistics, inserting/updating monthly statistics", 
		    				SQL);
					 */
					if(!clsDatabaseFunctions.executeSQL(SQL, conn)){
						super.addErrorMessage("Could not execute update/insert statement in armonthlystatistics.");
						rsRecentTransactions.close();
						return false;
					}
				}
			} //end while(rsRecentTransactions.next()){
		}catch(SQLException e){
			super.addErrorMessage("Error updating statistics - " + e.getMessage());
			return false;
		}

		//Get all the recent matchinglines (from this posting) and link them to their 'apply-to' docs:
		//We ONLY care about invoices for this statistic
		//And we ONLY care about invoices that have a zero current amount (i.e., invoices that are fully
		//paid.  We have to assume that these were fully paid by these matching lines.  So we are assured
		//that we are NOT counting any invoices that were fully paid before now:

		//We need to get a recordset of invoice transactions which are fully paid that have been affected
		//by any of the matching lines for this batch.  IF these invoices now have zero balances, and IF
		//one or more matching lines from this batch posting have touched that balance, then we know that
		//the invoice was not fully paid until now, and we can use it to recalculate the 'total days to pay'
		//and the 'total number of paid invoices' statistics.  We CAN'T just assume that ALL of the 'paid
		//invoices are still in the transactions file, because some records may have been cleared.  That's 
		//why we have to ADD to the current number.  Otherwise, we could just get a SUM or a COUNT of all 
		//the paid invoices for this customer and use that . . . but we can't.
		//And we have to be certain that we get only 1 matching line for each zero balance invoice, even if 
		//the batch contained more than one - otherwise we'll add too many 'paid invoices' to the total
		//since only one of the matching transactions could have 'paid off' the invoice . . . .
		//So we sort them by transaction id first, then by armatchingline transactiondate, in descending
		//order, so we know we're getting the LATEST matching line that should be designated as the one
		//that paid off the invoice.

		//We link the matching lines to their parent transactions also, because we ONLY want to get
		//matching lines that were matched through a cash receipt, not prepays or adjustments . . . 

		//We don't care about retainage invoices for this statistic
		SQL = "SELECT " 
			+ SMTableartransactions.TableName + "." + SMTableartransactions.lid 
			+ ", " + SMTableartransactions.TableName + "." + SMTableartransactions.spayeepayor
			+ ", " + SMTableartransactions.TableName + "." + SMTableartransactions.datduedate
			+ ", " + SMTablearmatchingline.TableName + "." + SMTablearmatchingline.dattransactiondate
			+ ", " + SMTablearmatchingline.TableName + "." + SMTablearmatchingline.lid
			+ " FROM " 
			+ SMTablearmatchingline.TableName 
			+ ", " + SMTableartransactions.TableName
			+ ", " + SMTableartransactions.TableName + " AS parenttransactions"
			+ " WHERE ("
			+ "(" + SMTablearmatchingline.TableName + "." + SMTablearmatchingline.lid + " > " + Long.toString(lLastMatchingLineID) + ")"
			+ " AND (" + SMTablearmatchingline.ldocappliedtoid + " = " 
			+ SMTableartransactions.TableName + "." + SMTableartransactions.lid + ")"
			+ " AND (" + SMTableartransactions.TableName + "." + SMTableartransactions.idoctype + " = " 
			+ ARDocumentTypes.INVOICE_STRING + ")"
			+ " AND (" + SMTableartransactions.TableName + "." + SMTableartransactions.dcurrentamt + " = 0.00)"

			//Link the parenttransactions table:
			+ " AND (parenttransactions." + SMTableartransactions.spayeepayor + " = " 
			+ SMTablearmatchingline.TableName + "." + SMTablearmatchingline.spayeepayor + ")"
			+ " AND (parenttransactions." + SMTableartransactions.sdocnumber + " = " 
			+ SMTablearmatchingline.TableName + "." + SMTablearmatchingline.sdocnumber + ")"
			+ " AND (parenttransactions." + SMTableartransactions.idoctype + " = " 
			+ ARDocumentTypes.RECEIPT_STRING + ")"

			+ ")"
			+ " ORDER BY " + SMTableartransactions.TableName + "." + SMTableartransactions.lid
			+ ", " + SMTablearmatchingline.dattransactiondate + " DESC"
			;
		long lLastReadTransactionID = 0;
		long lCurrentTransactionID = 0;

		try{
			ResultSet rsRecentlyPaidInvoices = clsDatabaseFunctions.openResultSet(SQL, conn);
			while(rsRecentlyPaidInvoices.next()){
				lCurrentTransactionID = rsRecentlyPaidInvoices.getLong(
						SMTableartransactions.TableName + "." + SMTableartransactions.lid);
				//Need to make sure we don't process an invoice twice, just because it has more than one
				//matching line:
				if (lLastReadTransactionID != lCurrentTransactionID){
					//New transaction - process it:
					//Note: we don't allow negative numbers in the 'days-to-pay':
					SQL = "UPDATE " + SMTablearcustomerstatistics.TableName
					+ " SET "
					//DAYS-TO-PAY:
					+ SMTablearcustomerstatistics.sTotalDaysToPay + " = "
					+ "IF ("
					+ "(TO_DAYS('" 
					+ clsDateAndTimeConversions.utilDateToString(
							rsRecentlyPaidInvoices.getDate(
									SMTablearmatchingline.dattransactiondate), "yyyy-MM-dd")
									+ "') - TO_DAYS('" + clsDateAndTimeConversions.utilDateToString(
											rsRecentlyPaidInvoices.getDate(
													SMTableartransactions.datduedate), "yyyy-MM-dd") + "')"
													+ ") > 0, "
													+ SMTablearcustomerstatistics.sTotalDaysToPay + " + "
													+ "(TO_DAYS('" 
													+ clsDateAndTimeConversions.utilDateToString(
															rsRecentlyPaidInvoices.getDate(
																	SMTablearmatchingline.dattransactiondate), "yyyy-MM-dd")
																	+ "') - TO_DAYS('" + clsDateAndTimeConversions.utilDateToString(
																			rsRecentlyPaidInvoices.getDate(
																					SMTableartransactions.datduedate), "yyyy-MM-dd") + "')"
																					+ ")"
																					+ ", " + SMTablearcustomerstatistics.sTotalDaysToPay + ")"				
																					+ ", " + SMTablearcustomerstatistics.sTotalNumberOfPaidInvoices
																					+ " = " + SMTablearcustomerstatistics.sTotalNumberOfPaidInvoices + " + 1"
																					+ " WHERE ("
																					+ "(" + SMTablearcustomerstatistics.sCustomerNumber 
																					+ " = '" + rsRecentlyPaidInvoices.getString(SMTableartransactions.spayeepayor) + "')"
																					+ ")"
																					;
					/*
	    			log.writeEntry(
	    					sUserName, 
		    				SMLogEntry.LOG_OPERATION_ARBATCHPOST, 
		    				"In updateStatistics, updating days-to-pay and no of invoices in arcustomerstatistics", 
		    				SQL
		    			);
					 */
					//Execute the statement:
					if(!clsDatabaseFunctions.executeSQL(SQL, conn)){
						super.addErrorMessage("Could not update days to pay and number of invoices in arcustomerstatistics.");
						rsRecentlyPaidInvoices.close();
						return false;
					}

					//Update the monthly statistics:
					//Note - we don't allow negative numbers in the 'days-to-pay':
					SQL = "UPDATE " + SMTablearmonthlystatistics.TableName
					+ " SET "
					+ SMTablearmonthlystatistics.sNumberOfPaidInvoices + " = "
					+ SMTablearmonthlystatistics.sNumberOfPaidInvoices + " + 1"

					+ ", " + SMTablearmonthlystatistics.sTotalNumberOfDaysToPay + " = " 
					+ "IF ("
					+ "(TO_DAYS('" 
					+ clsDateAndTimeConversions.utilDateToString(
							rsRecentlyPaidInvoices.getDate(
									SMTablearmatchingline.dattransactiondate), "yyyy-MM-dd")
									+ "') - TO_DAYS('" + clsDateAndTimeConversions.utilDateToString(
											rsRecentlyPaidInvoices.getDate(
													SMTableartransactions.datduedate), "yyyy-MM-dd") + "')"
													+ ") > 0, "
													+ "(TO_DAYS('" 
													+ clsDateAndTimeConversions.utilDateToString(
															rsRecentlyPaidInvoices.getDate(
																	SMTablearmatchingline.dattransactiondate), "yyyy-MM-dd")
																	+ "') - TO_DAYS('" + clsDateAndTimeConversions.utilDateToString(
																			rsRecentlyPaidInvoices.getDate(
																					SMTableartransactions.datduedate), "yyyy-MM-dd") + "')"
																					+ "), " + SMTablearcustomerstatistics.sTotalDaysToPay + ")"		
																					+ " WHERE ("
																					+ "(" + SMTablearmonthlystatistics.sCustomerNumber 
																					+ " = '" + rsRecentlyPaidInvoices.getString(SMTableartransactions.spayeepayor) + "')"
																					+ " AND (" + SMTablearmonthlystatistics.sMonth + " = "
																					+ clsDateAndTimeConversions.utilDateToString(rsRecentlyPaidInvoices.getDate(
																							SMTablearmatchingline.dattransactiondate), "M") + ")"
																							+ " AND (" + SMTablearmonthlystatistics.sYear + " = "
																							+ clsDateAndTimeConversions.utilDateToString(rsRecentlyPaidInvoices.getDate(
																									SMTablearmatchingline.dattransactiondate), "yyyy") + ")"
																									+ ")"
																									;

					//Execute the statement:
					if(!clsDatabaseFunctions.executeSQL(SQL, conn)){
						super.addErrorMessage("Could not update days to pay and number of invoices in armonthlystatistics.");
						rsRecentlyPaidInvoices.close();
						return false;
					}
					/*
	    			log.writeEntry(
	    				sUserName, 
	    				SMLogEntry.LOG_OPERATION_ARBATCHPOST, 
	    				"In updateStatistics, updating days-to-pay and no of invoices in armonthlystatistics", 
	    				SQL
	    			);
					 */
				}else{
					//Same transaction as last time - ignore it:
				}
				//Reset the last read ID:
				lLastReadTransactionID = lCurrentTransactionID;
			} //end while(rsRecentlyPaidInvoices.next()){
			rsRecentlyPaidInvoices.close();
		}catch(SQLException e){
			super.addErrorMessage("Error updating statistics from matchinglines - " + e.getMessage());
			return false;
		}
		return true;
	}
	private String buildGLTransactionEntryDescription(int ARDocumentType){
		String sEntryDescription = "";
		if (ARDocumentType == ARDocumentTypes.APPLYTO){
			//sEntryDescription = arentry.getsvendoracct()
			//	+ " " + arentry.getsvendorname()
			//;
		}
		if (ARDocumentType == ARDocumentTypes.CASHADJUSTMENT){
			//sEntryDescription = arentry.getsvendoracct()
			//	+ " " + arentry.getsvendorname()
			//;
		}
		if (ARDocumentType == ARDocumentTypes.CREDIT){
			//sEntryDescription = arentry.getsvendoracct()
			//	+ " " + arentry.getsvendorname()
			//;
		}
		if (ARDocumentType == ARDocumentTypes.CREDITADJUSTMENT){
			
		}
		if (ARDocumentType == ARDocumentTypes.INVOICE){
			//sEntryDescription = arentry.getsvendoracct()
			//	+ " " + arentry.getsvendorname()
			//	+ " CK# " + arentry.getschecknumber()
			//;
		}
		if (ARDocumentType == ARDocumentTypes.INVOICEADJUSTMENT){
			//sEntryDescription = arentry.getsvendoracct()
			//	+ " " + arentry.getsvendorname()
			//	+ " CK# " + arentry.getschecknumber()
			//;
		}
		if (ARDocumentType == ARDocumentTypes.MISCRECEIPT){
			//sEntryDescription = arentry.getsvendoracct()
			//	+ " " + arentry.getsvendorname()
			//	+ " CK# " + arentry.getschecknumber()
			//;				
		}
		if (ARDocumentType == ARDocumentTypes.PREPAYMENT){
			
		}
		if (ARDocumentType == ARDocumentTypes.RECEIPT){
			
		}
		if (ARDocumentType == ARDocumentTypes.REVERSAL){
			
		}
		return sEntryDescription;
	}
}