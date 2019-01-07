package smar;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

import SMClasses.MySQLs;
import SMClasses.SMBatchStatuses;
import SMClasses.SMEntryBatch;
import SMClasses.SMInvoice;
import SMClasses.SMLogEntry;
import SMClasses.SMTax;
import SMDataDefinition.SMTablearoptions;
import SMDataDefinition.SMTableinvoicedetails;
import SMDataDefinition.SMTableinvoiceheaders;
import ServletUtilities.clsServletUtilities;
import ServletUtilities.clsDatabaseFunctions;

public class ARImportSMInvoices extends java.lang.Object{

	private String m_sErrorMessage;
	//private String m_sCreatedBy;
	private String m_lCreatedByID;
	private String m_sCreatedByFullName;
	private String m_sBatchNumber;
	private boolean iFlagImports = false;
	private SMLogEntry log;
	private boolean bDebugMode = false;

	ARImportSMInvoices(){
		m_sErrorMessage = "";
		m_lCreatedByID = "0";
		m_sCreatedByFullName = "";
		m_sBatchNumber = "-1";
	}
	public String getErrorMessage (){
		return m_sErrorMessage;
	}
	public boolean setCreatedBy(String sCreatedByID, String sCreatedByFullName){

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
	public boolean importInvoices(Connection conn){

		log = new SMLogEntry(conn);
		if (bDebugMode){
			log.writeEntry(
					m_lCreatedByID, 
					SMLogEntry.LOG_OPERATION_ARSMINVOICEIMPORT, 
					"Entering " + clsServletUtilities.getFileNameSuffix(this.toString()) + ".importInvoices()",
					"",
					"[1376509276]"
					);
		}
		//Check posting flag
		iFlagImports = false;
		try{
			String SQL = "SELECT * FROM " + SMTablearoptions.TableName;
			ResultSet rsAROptions = clsDatabaseFunctions.openResultSet(SQL, conn);
			if (!rsAROptions.next()){
				m_sErrorMessage = "Error getting aroptions record";
				//System.out.println("In " + this.toString() + ".importInvoices: Error getting aroptions record");
				rsAROptions.close();
				return false;
			}else{
				if (rsAROptions.getLong(SMTablearoptions.iflagimports) == 1){
					iFlagImports = true;
				}
				if(rsAROptions.getLong(SMTablearoptions.ibatchpostinginprocess) == 1){
					m_sErrorMessage = "A previous posting is not completed - "
							+ rsAROptions.getString(SMTablearoptions.suserfullname) + " has been "
							+ rsAROptions.getString(SMTablearoptions.sprocess) + " "
							+ "since " + rsAROptions.getString(SMTablearoptions.datstartdate) + ".";
					log.writeEntry(
							m_lCreatedByID, 
							SMLogEntry.LOG_OPERATION_ARSMINVOICEIMPORT, 
							"Options flag already set",
							"A previous posting is not completed - "
									+ rsAROptions.getString(SMTablearoptions.suserfullname) + " has been "
									+ rsAROptions.getString(SMTablearoptions.sprocess) + " "
									+ "since " + rsAROptions.getString(SMTablearoptions.datstartdate)
							, "[1376509277]"
					);
					rsAROptions.close();
					return false;
				}
			}
			rsAROptions.close();
		}catch (SQLException e){
			m_sErrorMessage = "Error checking for previous posting - " + e.getMessage();
			System.out.println("Error checking for previous posting - " + e.getMessage());
			return false;
		}
		//Set posting flag
		try{
			String SQL = "UPDATE " + SMTablearoptions.TableName 
					+ " SET " + SMTablearoptions.ibatchpostinginprocess + " = 1"
					+ ", " + SMTablearoptions.datstartdate + " = NOW()"
					+ ", " + SMTablearoptions.sprocess 
					+ " = 'IMPORTING SERVICE MANAGER INVOICES'"
					+ ", " + SMTablearoptions.suserfullname + " = '" + m_sCreatedByFullName + "'"
					;
			if (!clsDatabaseFunctions.executeSQL(SQL, conn)){
				m_sErrorMessage = "Error setting posting flag in aroptions";
				System.out.println("In " + this.toString() + ".importInvoices: Error setting posting flag in aroptions");
				return false;
			}
		}catch (SQLException e){
			m_sErrorMessage = "Error setting posting flag in aroptions - " + e.getMessage();
			System.out.println("Error setting posting flag in aroptions - " + e.getMessage());
			return false;
		}
		if (bDebugMode){
			log.writeEntry(
					m_lCreatedByID, 
					SMLogEntry.LOG_OPERATION_ARSMINVOICEIMPORT, 
					"AROptions flag is set",
					"",
					"[1376509278]"
					);		
		}
		//Start a data transaction:
		if (!clsDatabaseFunctions.start_data_transaction(conn)){
			m_sErrorMessage = "Could not start data transaction";
			clearPostingFlag(conn);
			log.writeEntry(
					m_lCreatedByID, 
					"ARSMINVOICEIMPORT", 
					"Could not start data transaction",
					"",
					"[1376509279]"
					);
			return false;
		}

		if (!createBatch(conn)){
			clearPostingFlag(conn);
			clsDatabaseFunctions.rollback_data_transaction(conn);
			log.writeEntry(
					m_lCreatedByID, 
					SMLogEntry.LOG_OPERATION_ARSMINVOICEIMPORT, 
					"Could not create batch for invoice import",
					"",
					"[1376509280]"
					);
			return false;
		}

		if (!processRecords(conn)){
			clearPostingFlag(conn);
			clsDatabaseFunctions.rollback_data_transaction(conn);
			log.writeEntry(
					m_lCreatedByID, 
					SMLogEntry.LOG_OPERATION_ARSMINVOICEIMPORT, 
					"Could not process records",
					m_sErrorMessage,
					"[1376509281]"
					);
			return false;
		}

		if (!clsDatabaseFunctions.commit_data_transaction(conn)){
			clearPostingFlag(conn);
			m_sErrorMessage = "Could not commit data transaction - import failed";
			clsDatabaseFunctions.rollback_data_transaction(conn);
			log.writeEntry(
					m_lCreatedByID, 
					SMLogEntry.LOG_OPERATION_ARSMINVOICEIMPORT, 
					"Could not commit data transaction",
					"",
					"[1376509282]"
					);
			return false;
		}

		clearPostingFlag(conn);

		return true;
	}

	private boolean clearPostingFlag (Connection conn){

		try{
			String SQL = "UPDATE " + SMTablearoptions.TableName 
					+ " SET " + SMTablearoptions.ibatchpostinginprocess + " = 0"
					+ ", " + SMTablearoptions.datstartdate + " = '0000-00-00 00:00:00'"
					+ ", " + SMTablearoptions.sprocess + " = ''"
					+ ", " + SMTablearoptions.suserfullname + " = ''"
					;
			if (!clsDatabaseFunctions.executeSQL(SQL, conn)){
				m_sErrorMessage = "Error clearing posting flag in aroptions";
				System.out.println("In " + this.toString() + ".importInvoices: Error clearing posting flag in aroptions");
				return false;
			}
		}catch (SQLException e){
			m_sErrorMessage = "Error clearing posting flag in aroptions - " + e.getMessage();
			System.out.println("Error clearing posting flag in aroptions - " + e.getMessage());
			return false;
		}

		return true;
	}
	private boolean processRecords(Connection conn){
		boolean bNoInvoicesFound = true;
		String SQL = ARSQLs.Get_SM_Invoices_For_Import();
		try {
			ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
			while (rs.next()){
				bNoInvoicesFound = false;
				SMInvoice inv = new SMInvoice();
				inv.setM_sInvoiceNumber(rs.getString(SMTableinvoiceheaders.sInvoiceNumber));
				if (!inv.load(conn)){
					m_sErrorMessage = "Could not load invoice " + inv.getM_sInvoiceNumber();
					rs.close();
					return false;
				}

				//Populate the entry:
				AREntry m_CurrentAREntry = new AREntry();
				m_CurrentAREntry.sBatchNumber(m_sBatchNumber);
				m_CurrentAREntry.iBatchType(ARBatchTypes.AR_INVOICE);

				ARCustomer cust = new ARCustomer(inv.getM_sCustomerCode());
				//System.out.println("In ARImportSMInvoices, cust code = " + cust.getM_sCustomerNumber());
				if (!cust.load(conn)){
					m_sErrorMessage = "Could not load customer " + inv.getM_sCustomerCode();
					rs.close();
					return false;
				}
				m_CurrentAREntry.datDocDate(inv.getM_datInvoiceDate());
				m_CurrentAREntry.datDueDate(inv.getM_datDueDate());
				m_CurrentAREntry.sControlAcct(cust.getARControlAccount(conn));
				m_CurrentAREntry.sCustomerNumber(inv.getM_sCustomerCode());
				m_CurrentAREntry.sDocNumber(inv.getM_sInvoiceNumber().trim());
				if (inv.getM_iTransactionType() == SMTableinvoiceheaders.TYPE_INVOICE){
					m_CurrentAREntry.sDocumentType(ARDocumentTypes.INVOICE_STRING);
					//m_CurrentAREntry.sDocDescription("SM Invoice");
				}else{
					m_CurrentAREntry.sDocumentType(ARDocumentTypes.CREDIT_STRING);
					//m_CurrentAREntry.sDocDescription("SM Credit");
				}
				m_CurrentAREntry.sDocDescription(inv.getM_sShipToName());
				m_CurrentAREntry.sEntryNumber("-1");
				m_CurrentAREntry.sOrderNumber(inv.getM_sOrderNumber().trim());
				m_CurrentAREntry.sPONumber(inv.getM_sPONumber().trim());
				m_CurrentAREntry.sTerms(inv.getM_sTerms());

				//Figure out how to create entry lines out of the invoice, including taxes, etc.
				if (!loadInvoiceLinesByRevenueGL(conn, inv, m_CurrentAREntry)){
					rs.close();
					return false;
				}

				//Get the tax into a line:
				if (inv.getbdsalestaxamount().compareTo(BigDecimal.ZERO) != 0){
					ARLine line = new ARLine();
					line.dAmount(inv.getbdsalestaxamount().negate());
					line.sComment("Tax group: " + inv.getstaxjurisdiction());
					line.sDescription("Line for resale tax");
					SMTax tax = new SMTax();
					//tax.set_staxjurisdiction(inv.getstaxjurisdiction());
					tax.set_slid(inv.getitaxid());
					try{
						tax.load(conn);;
					} catch (Exception e){
						rs.close();
						m_sErrorMessage = "Error [1526927651] - Could not load tax info for " + inv.getstaxjurisdiction() + ":" + inv.getstaxtype() + " - " + e.getMessage();
						return false;
					}
					line.sGLAcct(tax.get_sglacct());

					if(inv.getM_iTransactionType() == SMTableinvoiceheaders.TYPE_CREDIT){
						ARTransaction artrans = new ARTransaction();
						artrans.setCustomerNumber(inv.getM_sCustomerCode());
						artrans.setDocNumber(inv.getM_sMatchingInvoiceNumber().trim());
						if (artrans.load(conn)){
							line.sDocAppliedTo(artrans.getDocNumber());
							line.lDocAppliedToId(artrans.getlTransactionID());
						}else{
							m_sErrorMessage = "Could not load apply-to invoice for credit note: " 
									+ inv.getM_sCustomerCode() + ", " + inv.getM_sMatchingInvoiceNumber().trim()
									+ " to create tax line.";
							rs.close();
							return false;
						}
					}else{
						line.lDocAppliedToId(-1);
						line.sDocAppliedTo("");
					}

					m_CurrentAREntry.add_line(line);
					m_CurrentAREntry.dOriginalAmount(m_CurrentAREntry.dOriginalAmount().add(line.dAmount().negate()));
				}
				if (!m_CurrentAREntry.save_without_data_transaction(conn)){
					m_sErrorMessage = "Could not save entry - " + m_CurrentAREntry.getErrorMessage();
					rs.close();
					return false;
				}

				//Set the invoice exported flag:
				if (iFlagImports){
					try {
						updateInvoiceExportedFlag(inv.getM_sInvoiceNumber(), conn);
					} catch (Exception e) {
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
	private void updateInvoiceExportedFlag(String sInvoiceNumber, Connection conn) throws Exception{

		String SQL = "UPDATE " + SMTableinvoiceheaders.TableName

				+ " SET " + SMTableinvoiceheaders.iExportedToAR
				+ " = 1"

		+ " WHERE ("
		+ "(TRIM(" + SMTableinvoiceheaders.sInvoiceNumber + ")" 
		+ " = '" + sInvoiceNumber.trim() + "')"

			+ ")";
		try {
			if (!clsDatabaseFunctions.executeSQL(SQL, conn)){
				throw new Exception("Error [1490366501] - updating AR export flag with SQL: " + SQL + ".");
			}
		}catch (SQLException e){
			throw new Exception("Error [1490366502] - updating AR export flag with SQL: " + SQL + " - " + e.getMessage());
		}
		return;
	}
	private boolean loadInvoiceLinesByRevenueGL(
			Connection conn, 
			SMInvoice inv, 
			AREntry entry){

		String sDocAppliedTo = "";
		long lDocAppliedTo = -1;

		if(inv.getM_iTransactionType() == SMTableinvoiceheaders.TYPE_CREDIT){
			ARTransaction artrans = new ARTransaction();
			artrans.setCustomerNumber(inv.getM_sCustomerCode());
			artrans.setDocNumber(inv.getM_sMatchingInvoiceNumber().trim());
			if (artrans.load(conn)){
				sDocAppliedTo = artrans.getDocNumber();
				lDocAppliedTo = artrans.getlTransactionID();
			}
		}

		try{
			String SQL = MySQLs.Get_SM_Invoice_Details_Grouped_By_RevenueGL(inv.getM_sInvoiceNumber());
			ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
			while (rs.next()){
				//Create a new line for the entry for each:
				ARLine line = new ARLine();

				line.sComment("From SM Invoice");
				line.sDescription("");
				line.setApplyToOrderNumber("");
				line.sGLAcct(rs.getString(SMTableinvoicedetails.sRevenueGLAcct).replace("-", ""));
				line.lDocAppliedToId(lDocAppliedTo);
				line.sDocAppliedTo(sDocAppliedTo);

				BigDecimal bdAmt = BigDecimal.valueOf(rs.getDouble("revenueamt"));
				bdAmt = bdAmt.setScale(2, BigDecimal.ROUND_HALF_UP);
				//This needs to be negated, because on batch credits, transaction lines have positive amounts and
				//on batch invoices, transaction lines have negative amounts.  In the invoicedetails table, this
				// is the opposite:
				line.dAmount(bdAmt.negate());
				entry.add_line(line);
				//Accumulate the amounts in the entry - the sign needs to be negated BACK for the entry:
				entry.dOriginalAmount(entry.dOriginalAmount().add(line.dAmount().negate()));
			}
			rs.close();
		}catch (SQLException e){
			System.out.println("Error in " + this.toString() + ".loadInvoiceLinesByRevenueGL: " + e.getMessage());
			m_sErrorMessage = "Error in " + this.toString() + ".loadInvoiceLinesByRevenueGL: " + e.getMessage();
			return false;
		}

		return true;
	}
	private boolean createBatch(Connection conn){

		ARBatch batch = new ARBatch("-1");

		batch.iBatchStatus(SMBatchStatuses.IMPORTED);
		batch.iBatchType(ARBatchTypes.AR_INVOICE);
		batch.sBatchDescription("SM Invoice Batch");
		batch.sCreatedByID(m_lCreatedByID);
		batch.sCreatedByFullName(m_sCreatedByFullName);
		batch.sLastEditedByID(m_lCreatedByID);
		batch.sCreatedByFullName(m_sCreatedByFullName);
		batch.sLastEditedByFullName(m_sCreatedByFullName);
		try {
			batch.save_without_data_transaction(conn, m_lCreatedByID, m_sCreatedByFullName);
		} catch (Exception e) {
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
