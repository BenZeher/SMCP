package smic;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.servlet.ServletContext;

import SMClasses.SMBatchStatuses;
import SMClasses.SMEntryBatch;
import SMClasses.SMLogEntry;
import SMDataDefinition.SMTableicbatchentries;
import SMDataDefinition.SMTableicentrylines;
import SMDataDefinition.SMTableicitems;
import SMDataDefinition.SMTableicpoheaders;
import SMDataDefinition.SMTableicporeceiptheaders;
import SMDataDefinition.SMTableicporeceiptlines;
import SMDataDefinition.SMTablelocations;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsManageBigDecimals;
import ServletUtilities.clsServletUtilities;

public class ICAutoCreateReceiptBatch extends java.lang.Object{

	private String m_sErrorMessage;
	private String m_sCreatedByFullName;
	private String m_sCreatedByID;
	private String m_sBatchNumber;
	
	private static final String POSTING_PROCESS_IDENTIFIER = "POSTING PROCESS";
	
	public ICAutoCreateReceiptBatch(){
		m_sErrorMessage = "";
		m_sCreatedByFullName = "";
		m_sCreatedByID = "0";
		m_sBatchNumber = "-1";
	}
	public String getErrorMessage (){
		return m_sErrorMessage;
	}
	public boolean setCreatedBy(String sCreatedByFullName, String sCreatedByID){
		
		String s = sCreatedByFullName.trim();
		if (s.equalsIgnoreCase("")){
			m_sErrorMessage = "Created by string cannot be blank";
			return false;
		}
		if (s.length() > SMEntryBatch.screatedbyfullnameLength){
			m_sErrorMessage = "Created by string is too long";
			return false;
		}
		
		m_sCreatedByFullName = sCreatedByFullName;
		m_sCreatedByID = sCreatedByID;
		return true;
	}
	
	public void setBatchDate(String smmddyyyDate) throws Exception{
		
		
		
	}
	public boolean checkToCreateNewReceiptBatch(
		Connection conn, 
		ServletContext context, 
		String sDBID, 
		String sUserID, 
		String sUserFullName){

    	//Double check that there are no receipts appearing in previous batches which are eligible to be posted here - if that's the
    	//case, then there is a problem and we need to figure it out:
    	String SQL = "SELECT"
    		+ " " + SMTableicentrylines.lreceiptlineid
    		+ ", " + SMTableicporeceiptheaders.lpostedtoic

    		+ " FROM " + SMTableicentrylines.TableName + " LEFT JOIN " + SMTableicporeceiptlines.TableName
    		
    		+ " ON " + SMTableicentrylines.TableName + "." + SMTableicentrylines.lreceiptlineid + " = " 
    		+ SMTableicporeceiptlines.TableName + "." + SMTableicporeceiptlines.lid
    		
    		+ " LEFT JOIN " + SMTableicporeceiptheaders.TableName + " ON " + SMTableicporeceiptlines.TableName 
    		+ "." + SMTableicporeceiptlines.lreceiptheaderid
    		+ " = " + SMTableicporeceiptheaders.TableName + "." + SMTableicporeceiptheaders.lid

    		+ " LEFT JOIN " + SMTableicbatchentries.TableName + " ON " + SMTableicentrylines.TableName + "." + SMTableicentrylines.lentryid
    		+ " = " + SMTableicbatchentries.TableName + "." + SMTableicbatchentries.lid

    		+ " WHERE ("
    		+	"(" + SMTableicporeceiptheaders.lpostedtoic + " = 0)"
    		+ " AND (" + SMTableicbatchentries.TableName + "." + SMTableicbatchentries.ientrytype 
    			+ " = " + Integer.toString(ICEntryTypes.RECEIPT_ENTRY) + ")"
    		+ ")"
    	;
    	//System.out.println("[1369768038] - SQL = '" + SQL + "'.");
    	try {
			ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
			if (rs.next()){
				m_sErrorMessage = "There are receipts (e.g. IC receipt line ID " 
					+ Long.toString(rs.getLong(SMTableicentrylines.TableName + "." + SMTableicentrylines.lreceiptlineid)) 
					+ ") marked as unposted that are already in previous receipt batches - please contact the program developer.";
				rs.close();
				try {
				} catch (Exception e) {
					//We won't stop for this, but the next user will need to reset the posting flag
				}
				return false;
			}
			rs.close();
		} catch (SQLException e) {
			m_sErrorMessage = "Error [1529954602] checking for receipts in previous batches that are marked as unposted with SQL: '" + SQL + "' - " + e.getMessage();
			return false;
		}
    	
		//Check posting flag
		ICOption option = new ICOption();
		String sPostingProcess = "CREATING BATCH FROM RECEIPTS";
    	try{
    		option.checkAndUpdatePostingFlagWithoutConnection(
    			context, 
    			sDBID, 
    			clsServletUtilities.getFullClassName(this.toString()) + ".checkToCreateNewReceiptBatch", 
    			sUserFullName, 
    			sPostingProcess);
    	}catch (Exception e){
    		m_sErrorMessage = "Error checking for previous posting - " + e.getMessage();
			//System.out.println("Error checking for previous posting - " + e.getMessage());
    		return false;
    	}
    	
    	try {
			createNewReceiptBatch(conn, sUserFullName, sUserID, context, sDBID);
		} catch (Exception e) {
			try {
				option.resetPostingFlagWithoutConnection(context, sDBID);
			} catch (Exception e1) {
				//We'll let this go because it just means that the next user will have to reset the IC posting flag
			}
			
			m_sErrorMessage = "Error [1529954961] creating new receipt batch - " + e.getMessage() + ".";
			return false;
		}
    	
		try {
			option.resetPostingFlagWithoutConnection(context, sDBID);
		} catch (Exception e1) {
			//We'll let this go because it just means that the next user will have to reset the IC posting flag
		}
		
		return true;
	}
	private boolean postBatch(Connection conn, ServletContext context, String sDBID, String sUserFullName){
		
		ICEntryBatch icbatch = new ICEntryBatch(m_sBatchNumber);
		if (!icbatch.load(conn)){
			m_sErrorMessage = "Could not load imported batch - import failed";
			return false;
		}
		
		try {
			icbatch.postImportedBatchwithout_data_transaction(conn, m_sCreatedByFullName, m_sCreatedByID);
		} catch (Exception e) {
			m_sErrorMessage = "Could not post imported batch - " + icbatch.getErrorMessages() + " - import failed";
			return false;
		}

		return true;
	}
	private boolean flagNonInventoryReceiptsAsProcessed(Connection conn, String sUserFullName, String sUserID){
		
		//This function flags ANY receipts that include ONLY NON-INVENTORY ITEMS or NON-STOCK INVENTORY ITEMS as 'already processed',
		// since we don't need to process those thru inventory costing.
		
		//We don't need this in a transaction because if it only goes through part way, the
		//rest of the receipts will be flagged in the next batch creation.
		String SQL = "SELECT"
			+ " " + SMTableicporeceiptheaders.TableName + "." + SMTableicporeceiptheaders.lid
			+ ", SUM(" 

				+ " IF (" 
					+ "(" + SMTableicporeceiptlines.TableName + "." + SMTableicporeceiptlines.lnoninventoryitem + " = 1)"
					+ " OR (" + SMTableicitems.TableName + "." + SMTableicitems.inonstockitem + " = 1)"
					+ ", 1, 0"
				+ ")"  //END IF
			
			+ ") AS NONSTOCKINVENTORY"
			+ ", COUNT(" + SMTableicporeceiptlines.TableName + "." 
				+ SMTableicporeceiptlines.lreceiptheaderid + ") AS LINECOUNT"
			+ " FROM " + SMTableicporeceiptheaders.TableName + " LEFT JOIN"
			+ " " + SMTableicporeceiptlines.TableName + " ON"
			+ " " + SMTableicporeceiptheaders.TableName + "." + SMTableicporeceiptheaders.lid + " ="
			+ " " + SMTableicporeceiptlines.TableName + "." + SMTableicporeceiptlines.lreceiptheaderid
			+ " LEFT JOIN"
			
			+ " " + SMTableicitems.TableName + " ON " + SMTableicitems.TableName + "." + SMTableicitems.sItemNumber
			+ "=" + SMTableicporeceiptlines.TableName + "." + SMTableicporeceiptlines.sitemnumber
			
			+ " WHERE ("
				+ "(" + SMTableicporeceiptheaders.TableName + "." + SMTableicporeceiptheaders.lpostedtoic + " = 0)"
				//Don't process deleted transactions:
				+ " AND (" + SMTableicporeceiptheaders.lstatus + " != " 
				+ Long.toString(SMTableicporeceiptheaders.STATUS_DELETED) + ")"
			+ ")"
			+ " GROUP BY " + SMTableicporeceiptheaders.TableName + "." + SMTableicporeceiptheaders.lid
			;
		//System.out.println("[1369765758] Flag non-inventory receipts SQL = '" + SQL + "'.");
		try {
			ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
			while (rs.next()){
				if (
					//If there IS a line count, and if the line count equals the 'non inventory OR NON-STOCK inventory' count,
					//that means that every line on this receipt is either a NON INVENTORY item, OR a NON-STOCK inventory item, so we
					//can flag the whole receipt as having been processed:
					(rs.getLong("LINECOUNT")) > 0
					&& (rs.getLong("NONSTOCKINVENTORY") == rs.getLong("LINECOUNT"))
				){
					//FLAG the receipt as 'processed':
					//PORECEIPTCHECK
					SQL = "UPDATE"
						+ " " + SMTableicporeceiptheaders.TableName
						+ " SET " + SMTableicporeceiptheaders.lpostedtoic + " = 1"
						+ ", " + SMTableicporeceiptheaders.dattimelastupdated + " = NOW()"
						+ ", " + SMTableicporeceiptheaders.slastupdateprocess + " = '" 
							+ SMTableicporeceiptheaders.UPDATE_PROCESS_FLAGASNONINVENTORY + "'"
						+ ", " + SMTableicporeceiptheaders.slastupdateuserfullname + " = '" + sUserFullName + "'"
						+ ", " + SMTableicporeceiptheaders.llastupdateuserid+ " = " + sUserID + ""
						+ " WHERE ("
							+ "(" + SMTableicporeceiptheaders.lid + " = " 
							+ rs.getLong(SMTableicporeceiptheaders.lid) + ")"
						+ ")"
						;
					
					try{
					    Statement stmt = conn.createStatement();
					    stmt.executeUpdate(SQL);
					}catch (Exception ex) {
						m_sErrorMessage = "Error [1428932629] pre-flagging non inventory receipt with SQL: " 
							+ SQL + " - " + ex.getMessage();
						rs.close();
						return false;
					}
					//For diagnostic purposes - record that this entry was flagged as posted:
					SMLogEntry log = new SMLogEntry(conn);
					log.writeEntry(
						sUserID, 
						SMLogEntry.LOG_OPERATION_ICRECEIPTFLAGGEDASNONINVENTORY, 
						"Receipt #" + Long.toString(rs.getLong(SMTableicporeceiptheaders.lid)) + " flagged as Non-Inventory", 
						SQL, 
						"[1424972111]"
					);
					
					//But if there are ANY lines with a zero receipt amount, flag them as not needing to be invoiced now:
					SQL = "UPDATE"
							+ " " + SMTableicporeceiptlines.TableName
							+ " SET " + SMTableicporeceiptlines.lpoinvoiceid + " = " 
								+ Integer.toString(SMTableicporeceiptlines.PO_INVOICE_STATUS_NOT_TO_BE_INVOICED)
							+ " WHERE ("
								+ "(" + SMTableicporeceiptlines.lreceiptheaderid + " = " 
								+ rs.getLong(SMTableicporeceiptheaders.lid) + ")"
								+ " AND (" + SMTableicporeceiptlines.bdextendedcost + " = 0.00)"
							+ ")"
							;
						
						try{
						    Statement stmt = conn.createStatement();
						    stmt.executeUpdate(SQL);
						}catch (Exception ex) {
							m_sErrorMessage = "Error [1428932628] flagging non inventory zero amount receipt line with SQL: " 
								+ SQL + " - " + ex.getMessage();
							rs.close();
							return false;
						}
				}
			}
			rs.close();
		} catch (SQLException e) {
			m_sErrorMessage = 
				"Error [1428932627] Could not read receipts to pre-flag non inventory receipts as processed - " 
				+ e.getMessage();
			return false;
		}
		
		return true;
	}
	private void createNewReceiptBatch(
		Connection conn,
		String sUserFullName,
		String sUserID,
		ServletContext context,
		String sDBID
		) throws Exception{
    	//First, flag any available receipts which are exclusively for non inventory items as processed:
		if (!flagNonInventoryReceiptsAsProcessed(conn, sUserFullName, sUserID)){
			throw new Exception(this.getErrorMessage());
		}
		
		//Remove any 'childless' PO receipt headers older than today:
		try {
			flagChildlessReceiptHeadersAsDeleted(conn, sUserFullName, sUserID);
		} catch (Exception e2) {
			throw new Exception("Error [1529955115] - " + e2.getMessage());
		}
		
		//Check to see if, after flagging non-stock and non-inventory receipts as already having been processed, there are any receipts left to 
		//process through inventory:
		try {
			if (!checkForProcessibleReceipts(conn, sUserFullName)){
				throw new Exception("Error [1529955116] - There are no UNPOSTED receipts for stock inventory - no batch needs to be created.  "); 
			}
		} catch (Exception e1) {
			throw new Exception("Error [1529955117] - " + e1.getMessage()); 
		}
		
		//Start a data transaction:
		if (!clsDatabaseFunctions.start_data_transaction(conn)){
			throw new Exception("Error [1529955118] - Could not start data transaction");
		}
		
		if (!createBatch(conn)){
			clsDatabaseFunctions.rollback_data_transaction(conn);
			throw new Exception("Error [1529955119] creating batch - " + this.getErrorMessage());
		}
		
		if (!processRecords(conn, sUserID)){
			clsDatabaseFunctions.rollback_data_transaction(conn);
			throw new Exception("Error [1529955120] processing records - " + this.getErrorMessage());
		}
		
		//Here is where we automatically post the batch:
		if (!postBatch(conn, context, sDBID, sUserFullName)){
			clsDatabaseFunctions.rollback_data_transaction(conn);
			throw new Exception("Error [1529955121] posting batch - " + this.getErrorMessage());
		}
		
		if (!clsDatabaseFunctions.commit_data_transaction(conn)){
			clsDatabaseFunctions.rollback_data_transaction(conn);
			throw new Exception("Error [1529955122] could not commit data transaction - import failed.");
		}
		
		//Now AFTER the data transaction, update all the receipts in the batch as posted:
		String SQL = "UPDATE"
			+ " " + SMTableicentrylines.TableName + " LEFT JOIN " + SMTableicporeceiptlines.TableName + " ON "
			+ SMTableicentrylines.TableName + "." + SMTableicentrylines.lreceiptlineid + " = " + SMTableicporeceiptlines.TableName
			+ "." + SMTableicporeceiptlines.lid
			+ " LEFT JOIN " + SMTableicporeceiptheaders.TableName + " ON " + SMTableicporeceiptlines.TableName + "."
			+ SMTableicporeceiptlines.lreceiptheaderid + " = " + SMTableicporeceiptheaders.TableName + "." + SMTableicporeceiptheaders.lid
			+ " SET " + SMTableicporeceiptheaders.TableName + "." + SMTableicporeceiptheaders.lpostedtoic + " = 1"
			+ ", " + SMTableicporeceiptheaders.TableName + "." + SMTableicporeceiptheaders.dattimelastupdated + " = NOW()"
			+ ", " + SMTableicporeceiptheaders.TableName + "." + SMTableicporeceiptheaders.slastupdateprocess + " = '" + SMTableicporeceiptheaders.UPDATE_PROCESS_POSTINGRECEIPT + "'"
			+ ", " + SMTableicporeceiptheaders.TableName + "." + SMTableicporeceiptheaders.slastupdateuserfullname + " = '" + sUserFullName + "'"
			+ ", " + SMTableicporeceiptheaders.TableName + "." + SMTableicporeceiptheaders.llastupdateuserid + " = " + sUserID + ""
			+ " WHERE ("
				+ "(" + SMTableicentrylines.TableName + "." + SMTableicentrylines.lbatchnumber + " = " + m_sBatchNumber + ")"
			+ ")"
		;
		SMLogEntry log = new SMLogEntry(sDBID, context);
		log.writeEntry(sUserID, SMLogEntry.LOG_OPERATION_ICUPDATERECEIPTSTATUS, "Batch " + m_sBatchNumber, SQL, "[1379014554]");

		try {
			Statement stmt = conn.createStatement();
			stmt.execute(SQL);
		} catch (SQLException e) {
			log.writeEntry(sUserID, SMLogEntry.LOG_OPERATION_ICUPDATERECEIPTSTATUS, "FAIL", "Error: " + e.getMessage(), "[1379014554]");
			throw new Exception("Error [1529955491] updating posted flag on receipts - " + e.getMessage());
		}
		log.writeEntry(sUserID, SMLogEntry.LOG_OPERATION_ICUPDATERECEIPTSTATUS, "SUCCESSFUL", SQL, "[1379014554]");
		
	}

	private boolean processRecords(Connection conn, String sUserID){
		boolean bNoReceiptsFound = true;
		String SQL = "SELECT " 
		+ SMTableicporeceiptheaders.TableName + "." + SMTableicporeceiptheaders.lid
		+ ", " + SMTableicpoheaders.TableName + "." + SMTableicpoheaders.svendor
		+ ", " + SMTableicpoheaders.TableName + "." + SMTableicpoheaders.svendorname
		+ " FROM " + SMTableicporeceiptheaders.TableName
		+ " LEFT JOIN " + SMTableicpoheaders.TableName
		+ " ON " + SMTableicporeceiptheaders.TableName + "." + SMTableicporeceiptheaders.lpoheaderid + " = "
			+ SMTableicpoheaders.TableName + "." + SMTableicpoheaders.lid
		+ " WHERE ("
			+ "(" + SMTableicporeceiptheaders.TableName + "." + SMTableicporeceiptheaders.lpostedtoic + " = 0)"
			+ " AND (" + SMTableicporeceiptheaders.TableName + "." + SMTableicporeceiptheaders.lstatus + " != " 
				+ Long.toString(SMTableicporeceiptheaders.STATUS_DELETED) + ")" 
			+ ")"
		+ " ORDER BY " 
		+ SMTableicporeceiptheaders.TableName + "." + SMTableicporeceiptheaders.lid;
		try {
			ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
			while (rs.next()){
				bNoReceiptsFound = false;
				ICPOReceiptHeader receipt = new ICPOReceiptHeader();
				receipt.setsID(rs.getString(SMTableicporeceiptheaders.TableName + "." + SMTableicporeceiptheaders.lid));
				if (!receipt.load(conn)){
					m_sErrorMessage = "Could not load receipt " + receipt.getsID();
					rs.close();
					return false;
				}
				
				//Populate the entry:
				ICEntry m_CurrentICEntry = new ICEntry();
				m_CurrentICEntry.sBatchNumber(m_sBatchNumber);
				m_CurrentICEntry.sBatchType(Integer.toString(ICBatchTypes.IC_RECEIPT));
				m_CurrentICEntry.sEntryDate(receipt.getsdatreceived());
				m_CurrentICEntry.sDocNumber(receipt.getsID());
				m_CurrentICEntry.sEntryDescription("PO RCPT " + receipt.getsreceiptnumber() + " FROM " 
					+ rs.getString(SMTableicpoheaders.TableName + "." + SMTableicpoheaders.svendor)
					+ " " 
					+ rs.getString(SMTableicpoheaders.TableName + "." + SMTableicpoheaders.svendorname)
				);
				m_CurrentICEntry.sEntryType(Integer.toString(ICEntryTypes.RECEIPT_ENTRY));
				m_CurrentICEntry.sEntryNumber("-1");
				
				//Figure out how to create entry lines out of the invoice
				//We read all the lines, but only include those lines which are INVENTORY items:
				if (!loadReceiptLines(conn, receipt.getsID(), m_CurrentICEntry)){
					rs.close();
					return false;
				}

				//It's possible that the receipt had NO inventory items on it - in that case
				//we don't want to save the entry:
				if (m_CurrentICEntry.getLineCount() != 0){
					if (!m_CurrentICEntry.save_without_data_transaction(conn, sUserID)){
						m_sErrorMessage = "Could not save entry - " + m_CurrentICEntry.getErrorMessage();
						rs.close();
						return false;
					}
				}
				
				//If the receipt totals zero, we also want to indicate that it does NOT need to be invoiced:
				try {
					flagReceiptLinesWithNoCostAsNotInvoiceable(receipt, conn);
				} catch (SQLException e) {
					m_sErrorMessage = "Error checking for zero amount total  - " + e.getMessage();
					return false;
				}
			}
			rs.close();
		}catch (SQLException e){
			m_sErrorMessage = "Error processing receipts - " + e.getMessage();
			return false;
		}
		if (bNoReceiptsFound){
			m_sErrorMessage = "No unprocessed receipts found";
			return false;
		}
		return true;
	}
	private boolean flagReceiptLinesWithNoCostAsNotInvoiceable(ICPOReceiptHeader rcpt, Connection conn) throws SQLException{
		//This functions marks and zero cost receipt lines as 'not to be invoiced':
		String SQL = "UPDATE " + SMTableicporeceiptlines.TableName
			+ " SET " + SMTableicporeceiptlines.lpoinvoiceid + " = " 
				+ Integer.toString(SMTableicporeceiptlines.PO_INVOICE_STATUS_NOT_TO_BE_INVOICED)
			+ " WHERE ("
				+ "(" + SMTableicporeceiptlines.lreceiptheaderid + " = " + rcpt.getsID() + ")"
				+ " AND (" + SMTableicporeceiptlines.bdextendedcost + " = 0.0000)" 
			+ ")"
			;
		try {
			Statement stmt = conn.createStatement();
			stmt.execute(SQL);
		} catch (SQLException e) {
			throw new SQLException("Error updating receipt lines as uninvoiceable with SQL: " + SQL + " - " + e.getMessage());
		}
		return true;
	}

	private boolean loadReceiptLines(
			Connection conn, 
			String sReceiptID, 
			ICEntry entry){
		
		try{
			//We don't process receipts for NON-INVENTORY items, and we don't process receipts for NON-STOCK inventory:
			String SQL = "SELECT"
				+ " " + SMTableicporeceiptlines.TableName + "." + SMTableicporeceiptlines.slocation
				+ ", " + SMTableicporeceiptlines.TableName + "." + SMTableicporeceiptlines.bdextendedcost
				+ ", " + SMTableicporeceiptlines.TableName + "." + SMTableicporeceiptlines.bdqtyreceived
				+ ", " + SMTableicporeceiptlines.TableName + "." + SMTableicporeceiptlines.sitemnumber
				+ ", " + SMTableicporeceiptlines.TableName + "." + SMTableicporeceiptlines.lid
					
				+ " FROM " + SMTableicporeceiptlines.TableName
				+ " LEFT JOIN " + SMTableicitems.TableName + " ON " 
				+ SMTableicitems.TableName + "." + SMTableicitems.sItemNumber + "=" + SMTableicporeceiptlines.TableName + "." + SMTableicporeceiptlines.sitemnumber
				+ " "
				+ " WHERE ("
					+ "(" + SMTableicporeceiptlines.TableName + "." + SMTableicporeceiptlines.lreceiptheaderid + " = " + sReceiptID + ")"
					+ " AND (" + SMTableicporeceiptlines.TableName + "." + SMTableicporeceiptlines.lnoninventoryitem + " = 0)"
					+ " AND (" + SMTableicitems.TableName + "." + SMTableicitems.inonstockitem + " != 1)"
				+ ")"
				+ " ORDER BY " + SMTableicporeceiptlines.TableName + "." + SMTableicporeceiptlines.llinenumber
			;
			
			ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
			Long lLineNumber = 0L;
			while (rs.next()){
				lLineNumber++;
				//Create a new line for the entry for each:
				ICEntryLine line = new ICEntryLine();
				
				line.sCategoryCode("");
				line.sComment("RECEIPT");
				//Get the control account:
				//If it's an inventory item, the control account is determined by the location
				SQL = "SELECT"
					+ " " + SMTablelocations.sGLInventoryAcct
					+ ", " + SMTablelocations.sGLPayableClearingAcct
					+ " FROM " + SMTablelocations.TableName
					+ " WHERE ("
						+ "(" + SMTablelocations.sLocation + " = '" 
							+ rs.getString(SMTableicporeceiptlines.slocation) + "')"
					+ ")"
				;
				try {
					ResultSet rsLocation = clsDatabaseFunctions.openResultSet(SQL, conn);
					if (rsLocation.next()){
						line.sControlAcct(rsLocation.getString(SMTablelocations.sGLInventoryAcct));
						line.sDistributionAcct(rsLocation.getString(SMTablelocations.sGLPayableClearingAcct));
					}else{
						m_sErrorMessage = "No record for location '" 
							+ rs.getString(SMTableicporeceiptlines.slocation) + " to get GL accounts.";
						return false;
					}
					rsLocation.close();
				} catch (Exception e) {
					m_sErrorMessage = "Error reading record for location '" 
						+ rs.getString(SMTableicporeceiptlines.slocation) + " to get GL accounts - "
						+ e.getMessage()
						;
					return false;
				}

				line.sCostBucketID("-1");
				line.sDescription("Receipt line");
				line.setCostString(
					clsManageBigDecimals.BigDecimalToScaledFormattedString(
						SMTableicentrylines.bdcostScale, 
						rs.getBigDecimal(SMTableicporeceiptlines.bdextendedcost)));
				line.setPriceString("0.00");
				line.setQtyString(clsManageBigDecimals.BigDecimalToScaledFormattedString(
						SMTableicentrylines.bdqtyScale, 
						rs.getBigDecimal(SMTableicporeceiptlines.bdqtyreceived)));
				line.sLineNumber(Long.toString(lLineNumber));
				line.sInvoiceNumber("");
				line.sItemNumber(rs.getString(SMTableicporeceiptlines.sitemnumber));
				line.sLocation(rs.getString(SMTableicporeceiptlines.slocation));
				line.sReceiptNum(sReceiptID);
				line.sReceiptLineID(Long.toString(rs.getLong(SMTableicporeceiptlines.lid)));
				line.sTargetLocation("");
				entry.add_line(line);
			}
			rs.close();
		}catch (SQLException e){
			//System.out.println("Error in " + this.toString() + ".loadInvoiceLines: " + e.getMessage());
			m_sErrorMessage = "Error in " + this.toString() + ".loadReceiptLines: " + e.getMessage();
			return false;
		}
		
		return true;
	}
	
	private boolean checkForProcessibleReceipts(Connection conn, String sUserFullName) throws Exception{
		
		boolean bResult = false;
		String SQL = "SELECT * FROM " + SMTableicporeceiptheaders.TableName
			+ " WHERE ("
				+ "(" + SMTableicporeceiptheaders.lpostedtoic + " = 0)"
				+ " AND (" + SMTableicporeceiptheaders.lstatus + " != " + Long.toString(SMTableicporeceiptheaders.STATUS_DELETED) + ")"
			+ ")"
		;
		try {
			ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
			if (rs.next()){
				bResult = true;
			}
			rs.close();
		} catch (Exception e) {
			throw new Exception("Error [1519925459] checking for 'processible' receipts with SQL: " + SQL + " - " + e.getMessage());
		}
		
		return bResult;
	}
	
	private void flagChildlessReceiptHeadersAsDeleted(Connection conn, String sUserFullName, String sUserID) throws Exception{
		
		String SQL = "UPDATE " + SMTableicporeceiptheaders.TableName
			+ " LEFT JOIN " + SMTableicporeceiptlines.TableName + " ON "
			+ SMTableicporeceiptheaders.TableName + "." + SMTableicporeceiptheaders.lid + " = "
			+ SMTableicporeceiptlines.TableName + "." + SMTableicporeceiptlines.lreceiptheaderid
			+ " SET " + SMTableicporeceiptheaders.TableName + "." + SMTableicporeceiptheaders.datdeleted + " = NOW()"
			+ ", " + SMTableicporeceiptheaders.TableName + "." + SMTableicporeceiptheaders.lstatus + " = " + Integer.toString(SMTableicporeceiptheaders.STATUS_DELETED)
			+ ", " + SMTableicporeceiptheaders.TableName + "." + SMTableicporeceiptheaders.sdeletedbyfullname + " = '" + POSTING_PROCESS_IDENTIFIER + "'"
			+ ", " + SMTableicporeceiptheaders.TableName + "." + SMTableicporeceiptheaders.slastupdateprocess + " = '" + SMTableicporeceiptheaders.UPDATE_PROCESS_CHILDLESSRECEIPTMARKEDASDELETED + "'"
			+ " WHERE ("
				+ "(" + SMTableicporeceiptlines.TableName + "." + SMTableicporeceiptlines.lid + " IS NULL)"
				+ " AND (" + SMTableicporeceiptheaders.TableName + "." + SMTableicporeceiptheaders.dattimelastupdated + " < CURRENT_DATE)"
				+ " AND (" + SMTableicporeceiptheaders.TableName + "." + SMTableicporeceiptheaders.lstatus + " != " + Integer.toString(SMTableicporeceiptheaders.STATUS_DELETED) + ")"
			+ ")"
		;
		//System.out.println("[1519943696] - SQL: " + SQL);
		try {
			Statement stmt = conn.createStatement();
			stmt.execute(SQL);
		} catch (Exception e) {
			throw new Exception("Error [1519925460] checking flagging receipts without lines as deleted with SQL: " + SQL + " - " + e.getMessage());
		}
		
	}
	private boolean createBatch(Connection conn) throws Exception{
		
		ICEntryBatch batch = new ICEntryBatch("-1");
		
		batch.iBatchStatus(SMBatchStatuses.IMPORTED);
		batch.iBatchType(ICBatchTypes.IC_RECEIPT);
		batch.sBatchDescription("SM Receipts Batch");
		batch.sSetCreatedByFullName(m_sCreatedByFullName);
		batch.sSetCreatedByID(m_sCreatedByID);
		batch.sSetLastEditedByFullName(m_sCreatedByFullName);
		batch.sSetLastEditedByID(m_sCreatedByID);
		
		/*
		ServletUtilities.clsDBServerTime clsCurrentTime = null;
		try {
			clsCurrentTime = new ServletUtilities.clsDBServerTime(conn);
		} catch (Exception e) {
			throw new Exception ("Error [1554403215] - could not read server time - " + e.getMessage() + ".");
		}
		
		try {
			batch.setBatchDate(
				ServletUtilities.clsDateAndTimeConversions.convertDateFormat(
					smmddyyyyBatchDate, 
					ServletUtilities.clsServletUtilities.DATE_FORMAT_FOR_DISPLAY, 
					ServletUtilities.clsServletUtilities.DATE_FORMAT_FOR_SQL, 
					clsCurrentTime.getCurrentDateTimeInSelectedFormat(ServletUtilities.clsServletUtilities.DATE_FORMAT_FOR_SQL)
					)
			);
		} catch (Exception e) {
			throw new Exception("Error [1554404594] setting batch date - " + batch.getErrorMessages());
		}
		*/
		if (!batch.save_without_data_transaction(conn, m_sCreatedByFullName, m_sCreatedByID)){
			m_sErrorMessage = "Could not save batch - " + batch.getErrorMessages();
			return false;
		}
		
		m_sBatchNumber = batch.sBatchNumber();
		
		return true;
	}
	public String getM_sBatchNumber() {
		return m_sBatchNumber;
	}

}
