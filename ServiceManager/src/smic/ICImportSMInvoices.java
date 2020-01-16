package smic;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DecimalFormat;

import javax.servlet.ServletContext;

import SMClasses.SMBatchStatuses;
import SMClasses.SMEntryBatch;
import SMClasses.SMInvoice;
import SMClasses.SMLogEntry;
import SMDataDefinition.SMTableicitems;
import SMDataDefinition.SMTableinvoicedetails;
import SMDataDefinition.SMTableinvoiceheaders;
import ServletUtilities.clsServletUtilities;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsDateAndTimeConversions;
import ServletUtilities.clsManageBigDecimals;
import ServletUtilities.clsStringFunctions;

public class ICImportSMInvoices extends java.lang.Object{

	private String m_sErrorMessage;
	private String m_sCreatedByFullName;
	private String m_lCreatedByID;
	private boolean m_bFlagImports;
	private String m_sBatchNumber;

	private long m_lNumberOfInvoicesProcessed;
	private long m_lNumberOfInvoicesImported;
	private SMLogEntry log;

	public ICImportSMInvoices(){
		m_sErrorMessage = "";
		m_sCreatedByFullName = "";
		m_lCreatedByID = "0";
		m_sBatchNumber = "-1";
		m_lNumberOfInvoicesProcessed = 0;
		m_lNumberOfInvoicesImported = 0;
	}
//	@Override
//	public void run(){
//		String sURL = "localhost";
//		String sDBID = "servmgr1"; //servmgr1 - default
//		String sConnString = "jdbc:mysql://" + sURL + ":3306/" + sDBID + "?noDatetimeStringSync=true&connectTimeout=28800000&interactiveClient=True";
//		String sUser = "root";
//		String sPassword = "password123";
//		Connection conn = null;
//		try{
//			conn = DriverManager.getConnection(sConnString, sUser, sPassword);
//		}catch(Exception e){
//			System.out.println(e);
//		}
//		if(!clearPostingFlag(conn))
//			return;
//		if(!importInvoices(conn, "Edwin Mwaniki"))
//			return;
//	}
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
		m_lCreatedByID = sCreatedByID;
		return true;
	}
	public boolean importInvoices(Connection conn, ServletContext context, String sUserFullName, String sDBID, String sUserID){
		m_sCreatedByFullName = sUserFullName;
		log = new SMLogEntry(conn);
		log.writeEntry(
				m_lCreatedByID, 
				SMLogEntry.LOG_OPERATION_ICSMINVOICEIMPORT, 
				"Entering " + clsServletUtilities.getFileNameSuffix(this.toString()) + ".importInvoices()",
				"",
				"[1376509394]"
		);

		m_bFlagImports = false;
		
		//Check posting flag
		ICOption option = new ICOption();
		try{
			String sPostingprocess = "IMPORTING SERVICE MANAGER INVOICES";
			option.checkAndUpdatePostingFlagWithoutConnection(
				context, 
				sDBID, 
				clsServletUtilities.getFullClassName(this.toString()) + ".importInvoices", 
				sUserFullName, 
				sPostingprocess);
			if(option.getiFlagImports() == 1){
				m_bFlagImports = true;
			}
		}catch (Exception e){
			m_sErrorMessage = "Error checking for previous posting - " + e.getMessage();
			System.out.println("[1579195349] Error checking for previous posting - " + e.getMessage());
			return false;
		}

		//Start a data transaction:
		if (!clsDatabaseFunctions.start_data_transaction(conn)){
			log.writeEntry(
					m_lCreatedByID, 
					SMLogEntry.LOG_OPERATION_ICSMINVOICEIMPORT, 
					"Could not start data transaction",
					"",
					"[1376509397]"
			);

			m_sErrorMessage = "Could not start data transaction";
			try {
				option.resetPostingFlagWithoutConnection(context, sDBID);
			} catch (Exception e) {
				//We won't stop for this, but the next user will have to clear the IC posting flag
			}
			return false;
		}

		if (!createBatch(conn, m_sCreatedByFullName, m_lCreatedByID)){
			log.writeEntry(
					m_lCreatedByID, 
					SMLogEntry.LOG_OPERATION_ICSMINVOICEIMPORT, 
					"Could not create batch",
					m_sErrorMessage,
					"[1376509398]"
			);
			clsDatabaseFunctions.rollback_data_transaction(conn);
			try {
				option.resetPostingFlagWithoutConnection(context, sDBID);
			} catch (Exception e) {
				//We won't stop for this, but the next user will have to clear the IC posting flag
			}
			return false;
		}

		if (!processRecords(conn, sUserID)){
			clsDatabaseFunctions.rollback_data_transaction(conn);
			log.writeEntry(
					m_lCreatedByID, 
					SMLogEntry.LOG_OPERATION_ICSMINVOICEIMPORT,
					"Error processing records",
					m_sErrorMessage,
					"[1376509399]"
			);

			try {
				option.resetPostingFlagWithoutConnection(context, sDBID);
			} catch (Exception e) {
				//We won't stop for this, but the next user will have to clear the IC posting flag
			}
			return false;
		}

		//TJR - 5/10/2011 - Commented out because of problems with double posting, for now
		//Here is where we automatically post the batch:
		/*
		if (!postBatch(conn, context)){
			SMUtilities.rollback_data_transaction(conn);
			log.writeEntry(
					m_sCreatedBy, 
					SMLogEntry.LOG_OPERATION_ICSMINVOICEIMPORT,
					"Error posting imported batch",
					m_sErrorMessage
			);

			clearPostingFlag(conn);
			return false;
		}
		*/
		if (!clsDatabaseFunctions.commit_data_transaction(conn)){
			clsDatabaseFunctions.rollback_data_transaction(conn);
			try {
				option.resetPostingFlagWithoutConnection(context, sDBID);
			} catch (Exception e) {
				//We won't stop for this, but the next user will have to clear the IC posting flag
			}
			m_sErrorMessage = "Could not commit data transaction - import failed";
			log.writeEntry(
					m_lCreatedByID, 
					SMLogEntry.LOG_OPERATION_ICSMINVOICEIMPORT, 
					m_sErrorMessage,
					"",
					"[1376509400]"
			);
			return false;
		}

		try {
			option.resetPostingFlagWithoutConnection(context, sDBID);
		} catch (Exception e) {
			//We won't stop for this, but the next user will have to clear the IC posting flag
		}
		log.writeEntry(
				m_lCreatedByID, 
				"ICSMINVOICEIMPORT", 
				"Import completed",
				"",
				"[1376509401]"
		);
		return true;
	}
	/* Temporarily commented this out - 5/11/2011 - TJR
	private boolean postBatch(Connection conn, ServletContext context){
		
		ICEntryBatch icbatch = new ICEntryBatch(m_sBatchNumber);
		if (!icbatch.load(conn)){
			m_sErrorMessage = "Could not load imported batch - import failed";
			log.writeEntry(
					m_sCreatedBy, 
					SMLogEntry.LOG_OPERATION_ICSMINVOICEIMPORT, 
					m_sErrorMessage,
					""
			);
			return false;
		}
		if (!icbatch.postImportedBatchwithout_data_transaction(conn, context, m_sCreatedBy)){
			m_sErrorMessage = "Could not post imported batch - " + icbatch.getErrorMessages() + " - import failed";
			log.writeEntry(
					m_sCreatedBy, 
					SMLogEntry.LOG_OPERATION_ICSMINVOICEIMPORT, 
					m_sErrorMessage,
					""
			);
			return false;
		}
		return true;
	}
	*/
//	private boolean clearPostingFlag (Connection conn){
//
//		try{
//			String SQL = "UPDATE " + SMTableicoptions.TableName 
//			+ " SET " + SMTableicoptions.ibatchpostinginprocess + " = 0"
//			+ ", " + SMTableicoptions.datstartdate + " = '0000-00-00 00:00:00'"
//			+ ", " + SMTableicoptions.sprocess + " = ''"
//			+ ", " + SMTableicoptions.suserfullname + " = ''"
//			+ ", " + SMTableicoptions.lpostingtimestamp+ " = 0"
//			;
//			if (!clsDatabaseFunctions.executeSQL(SQL, conn)){
//				m_sErrorMessage = "Error clearing posting flag in icoptions";
//				System.out.println("In " + this.toString() + ".importInvoices: Error clearing posting flag in icoptions");
//				return false;
//			}
//		}catch (SQLException e){
//			m_sErrorMessage = "Error clearing posting flag in icoptions - " + e.getMessage();
//			//System.out.println("Error clearing posting flag in icoptions - " + e.getMessage());
//			return false;
//		}
//
//		return true;
//	}
	private boolean processRecords(Connection conn, String sUserID){
		boolean bNoInvoicesFound = true;
		String SQL = "SELECT " + SMTableinvoiceheaders.sInvoiceNumber

		+ " FROM " + SMTableinvoiceheaders.TableName

		+ " WHERE ("
		+ "(" + SMTableinvoiceheaders.iExportedToIC + " = 0)"

		+ ")"
		+ " ORDER BY " 
		+ SMTableinvoiceheaders.sInvoiceNumber;
		try {
			ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
			while (rs.next()){
				m_lNumberOfInvoicesProcessed++;
				bNoInvoicesFound = false;
				SMInvoice inv = new SMInvoice();
				String sInvoiceNumber = rs.getString(SMTableinvoiceheaders.sInvoiceNumber);
				//If the invoice has ANY stock items on it, create an entry:
				if (checkInvoiceForStockItems(sInvoiceNumber, conn)){
					inv.setM_sInvoiceNumber(sInvoiceNumber);
					if (!inv.load(conn)){
						m_sErrorMessage = "Could not load invoice " + inv.getM_sInvoiceNumber();
						rs.close();
						return false;
					}

					//Populate the entry:
					ICEntry m_CurrentICEntry = new ICEntry();
					m_CurrentICEntry.sBatchNumber(m_sBatchNumber);
					m_CurrentICEntry.sBatchType(Integer.toString(ICBatchTypes.IC_SHIPMENT));
					m_CurrentICEntry.sEntryDate(clsDateAndTimeConversions.utilDateToString(inv.getM_datInvoiceDate(), "MM/dd/yyyy"));
					m_CurrentICEntry.sDocNumber(inv.getM_sInvoiceNumber().trim());
					if (inv.getM_iTransactionType() == SMTableinvoiceheaders.TYPE_INVOICE){
						m_CurrentICEntry.sEntryDescription("INV-" + inv.getM_sShipToName());
					}else{
						m_CurrentICEntry.sEntryDescription("CRD-" + inv.getM_sShipToName());
					}
					m_CurrentICEntry.sEntryType(Integer.toString(ICEntryTypes.SHIPMENT_ENTRY));
					m_CurrentICEntry.sEntryNumber("-1");
					/*
					 * might need this:
				if (inv.getM_iTransactionType() == SMTableinvoiceheaders.TYPE_INVOICE){
					m_CurrentICEntry.sDocumentType(ARDocumentTypes.INVOICE_STRING);
					//m_CurrentAREntry.sDocDescription("SM Invoice");
				}else{
					m_CurrentICEntry.sDocumentType(ARDocumentTypes.CREDIT_STRING);
					//m_CurrentAREntry.sDocDescription("SM Credit");
				}
					 */

					//Figure out how to create entry lines out of the invoice, including taxes, etc.
					if (!loadInvoiceLines(conn, inv, m_CurrentICEntry)){
						rs.close();
						return false;
					}

					if (!m_CurrentICEntry.save_without_data_transaction(conn, sUserID)){
						m_sErrorMessage = "Could not save entry - " + m_CurrentICEntry.getErrorMessage();
						rs.close();
						return false;
					}
					
					//If the invoice was imported, increment the counter:
					m_lNumberOfInvoicesImported++;
				}
				//Set the invoice exported flag for ALL invoices, we we don't try to re-import them next time:
				if (m_bFlagImports){
					try {
						updateInvoiceExportedFlag(sInvoiceNumber, conn);
					} catch (SQLException e) {
						m_sErrorMessage = e.getMessage();
						rs.close();
						return false;					
					}
					try {
						flagInvoiceLineTransactionIDs(sInvoiceNumber, conn);
					} catch (SQLException e) {
						m_sErrorMessage = e.getMessage();
						rs.close();
						return false;					
					}
				}
			}
			rs.close();
		}catch (SQLException e){
			m_sErrorMessage = "Error processing invoices - " + e.getMessage();
			return false;
		}
		if (bNoInvoicesFound){
			m_sErrorMessage = "No unexported invoices found";
			return false;
		}
		return true;
	}
	private boolean checkInvoiceForStockItems(String sInvNumber, Connection con) throws SQLException{
		
		boolean bInvoiceHasStockItems = false;
		String SQL = "SELECT"
			+ " " + SMTableinvoicedetails.TableName + "." + SMTableinvoicedetails.iLineNumber
			+ " FROM " + SMTableinvoicedetails.TableName
			+ " LEFT JOIN " + SMTableicitems.TableName 
			+ " ON " + SMTableinvoicedetails.TableName + "." + SMTableinvoicedetails.sItemNumber
			+ " = " + SMTableicitems.TableName + "." + SMTableicitems.sItemNumber
			+ " WHERE ("
				+ "(" + SMTableicitems.TableName + "." + SMTableicitems.inonstockitem + " = 0)"
				+ " AND (" + SMTableinvoicedetails.TableName + "." 
					+ SMTableinvoicedetails.sInvoiceNumber + " = '" + sInvNumber + "')"
			+ ")"
		;
		ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, con);
		if (rs.next()){
			bInvoiceHasStockItems = true;
		}
		rs.close();
		
		return bInvoiceHasStockItems;
	}
	private void flagInvoiceLineTransactionIDs(String sInvoiceNumber, Connection conn)throws SQLException{

		//Flagging these with a -1 indicates that they have been processed by the import, but not all of them
		//will wind up in the batch.  The ones that do, when they are posted, will wind up being linked
		//to the actual IC transactions which are created.

		String SQL = "UPDATE " + SMTableinvoicedetails.TableName
		+ " SET " + SMTableinvoicedetails.lictransactionid
		+ " = -1"

		+ " WHERE ("
			+ "(" + SMTableinvoicedetails.sInvoiceNumber
			+ " = '" + clsStringFunctions.PadLeft(sInvoiceNumber.trim()," ", 8) + "')"
		+ ")";
		try {
			Statement stmt = conn.createStatement();
			stmt.execute(SQL);
		}catch (SQLException e){
			throw new SQLException("Error flagging transaction ID on invoice lines for invoicenumber " + sInvoiceNumber
				+ " - " + e.getMessage());
		}
	}
	
	private void updateInvoiceExportedFlag(String sInvoiceNumber, Connection conn) throws SQLException{

		//Here the invoices are flagged as having been exported.
		//ALSO, they are given a day end number of -1.  If they go into a batch and actually get posted,
		//that day end number will be replaced with that batch number at the time of posting.
		//But any invoices that have ONLY non-inventory items will never get posted in a batch.  So
		//the -1 in the day end number will let Service Manager know that they have been processed
		//and allow them to be credited.
		String SQL = "UPDATE " + SMTableinvoiceheaders.TableName
		+ " SET " + SMTableinvoiceheaders.iExportedToIC
		+ " = 1"
		+ ", " + SMTableinvoiceheaders.iDayEndNumber + " = -1"

		+ " WHERE ("
		+ "(" + SMTableinvoiceheaders.sInvoiceNumber
		+ " = '" + clsStringFunctions.PadLeft(sInvoiceNumber.trim()," ", 8) + "')"

		+ ")";
		try {
			Statement stmt = conn.createStatement();
			stmt.execute(SQL);
		}catch (SQLException e){
			throw new SQLException("Error updating invoice exported flag - " + e.getMessage());
		}
	}
	private boolean loadInvoiceLines(
			Connection conn, 
			SMInvoice inv, 
			ICEntry entry){

		try{
			String SQL = "SELECT " + SMTableinvoicedetails.TableName + ".* FROM " + SMTableinvoicedetails.TableName
			+ " LEFT JOIN " + SMTableicitems.TableName + " ON "
			+ SMTableinvoicedetails.TableName + "." + SMTableinvoicedetails.sItemNumber + " = "
			+ SMTableicitems.TableName + "." + SMTableicitems.sItemNumber
			+ " WHERE ("
			+ "(" + SMTableinvoicedetails.TableName + "." + SMTableinvoicedetails.sInvoiceNumber 
			+ " = '" + inv.getM_sInvoiceNumber() + "')"
			+ " AND (" + SMTableinvoicedetails.TableName + "." + SMTableinvoicedetails.dQtyShipped + " > 0.00)"
			+ " AND (" + SMTableicitems.TableName + "." + SMTableicitems.inonstockitem + " = 0)"
			+ ")"
			;

			ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
			Long lLineNumber = 0L;
			while (rs.next()){
				lLineNumber++;
				//Create a new line for the entry for each:
				ICEntryLine line = new ICEntryLine();
				line.sLineNumber(Long.toString(lLineNumber));
				line.sComment(inv.getM_sInvoiceNumber() + "-" 
						+ Long.toString(rs.getLong(SMTableinvoicedetails.iLineNumber)));
				line.sInvoiceNumber(inv.getM_sInvoiceNumber());
				line.sInvoiceLineNumber(Long.toString(rs.getLong(SMTableinvoicedetails.iLineNumber)));
				line.sCategoryCode(rs.getString(SMTableinvoicedetails.sItemCategory));
				//The ICEntry class will set this properly:
				line.sControlAcct("");
				line.sCostBucketID("-1");
				line.sDescription(inv.getM_sCustomerCode() + "-" + inv.getM_sShipToName());
				line.sDistributionAcct(rs.getString(SMTableinvoicedetails.sExpenseGLAcct).replace("-", ""));

				DecimalFormat FourDecimal = new DecimalFormat("###,###,##0.0000");
				if (inv.getM_iTransactionType() == SMTableinvoiceheaders.TYPE_INVOICE){
					line.setQtyString(FourDecimal.format(
							-1 * rs.getDouble(SMTableinvoicedetails.dQtyShipped)));
					line.setPriceString(clsManageBigDecimals.doubleTo2DecimalSTDFormat(
							-1 * rs.getDouble(SMTableinvoicedetails.dExtendedPriceAfterDiscount)));
				}else{
					line.setQtyString(FourDecimal.format(
							rs.getDouble(SMTableinvoicedetails.dQtyShipped)));
					line.setPriceString(clsManageBigDecimals.doubleTo2DecimalSTDFormat(
							-1 * rs.getDouble(SMTableinvoicedetails.dExtendedPriceAfterDiscount)));
				}

				line.sItemNumber(rs.getString(SMTableinvoicedetails.sItemNumber));
				line.sLocation(rs.getString(SMTableinvoicedetails.sLocationCode));
				//This indicates to the posting process that it's from an invoice and needs to update
				//that invoice:
				line.sReceiptNum("SMINVOICE");
				entry.add_line(line);
			}
			rs.close();
		}catch (SQLException e){
			System.out.println("[1579195355] Error in " + this.toString() + ".loadInvoiceLines: " + e.getMessage());
			m_sErrorMessage = "Error in " + this.toString() + ".loadInvoiceLines: " + e.getMessage();
			return false;
		}

		return true;
	}
	private boolean createBatch(Connection conn, String sUserFullName, String sUserID){

		ICEntryBatch batch = new ICEntryBatch("-1");

		batch.iBatchStatus(SMBatchStatuses.IMPORTED);
		batch.iBatchType(ICBatchTypes.IC_SHIPMENT);
		batch.sBatchDescription("SM Invoice Batch");
		batch.sSetCreatedByFullName(sUserFullName);
		batch.sSetCreatedByID(sUserID);
		batch.sSetLastEditedByFullName(sUserFullName);
		batch.sSetLastEditedByFullName(sUserID);
		if (!batch.save_without_data_transaction(conn, sUserFullName, sUserID)){
			m_sErrorMessage = "Could not save batch - " + batch.getErrorMessages();
			return false;
		}

		m_sBatchNumber = batch.sBatchNumber();

		return true;
	}
	public String getM_sBatchNumber() {
		return m_sBatchNumber;
	}
	public long getNumberOfInvoicesProcessed(){
		return m_lNumberOfInvoicesProcessed;
	}
	public long getNumberOfInvoicesImported(){
		return m_lNumberOfInvoicesImported;
	}
}
