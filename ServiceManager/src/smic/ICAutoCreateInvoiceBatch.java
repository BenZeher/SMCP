package smic;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;

import javax.servlet.ServletContext;

import SMClasses.SMBatchStatuses;
import SMClasses.SMEntryBatch;
import SMClasses.SMModuleTypes;
import SMClasses.SMUser;
import SMDataDefinition.SMTableapbatchentries;
import SMDataDefinition.SMTableapbatches;
import SMDataDefinition.SMTableiccosts;
import SMDataDefinition.SMTableicentrylines;
import SMDataDefinition.SMTableicinvoiceexportsequences;
import SMDataDefinition.SMTableicpoinvoiceheaders;
import SMDataDefinition.SMTableicpoinvoicelines;
import SMDataDefinition.SMTablelocations;
import SMDataDefinition.SMTablesmoptions;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsDateAndTimeConversions;
import ServletUtilities.clsManageBigDecimals;
import ServletUtilities.clsServletUtilities;
import ServletUtilities.clsStringFunctions;
import smap.APAccountSet;
import smap.APBatch;
import smap.APBatchEntry;
import smap.APBatchEntryLine;
import smap.APOptions;
import smap.APVendor;

public class ICAutoCreateInvoiceBatch extends java.lang.Object{

	private String m_sErrorMessage;
	private String m_sCreatedByFullName;
	private String m_sUserID;
	private String m_sBatchNumber;
	private String m_sExportSequenceNumber;
	private boolean bDebugMode = false;
	
	public ICAutoCreateInvoiceBatch(){
		m_sErrorMessage = "";
		m_sCreatedByFullName = "";
		m_sUserID = "0";
		m_sBatchNumber = "-1";
		m_sExportSequenceNumber = "-1";
	}
//	@Override
//	public void run() {
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
//		ICAutoCreateInvoiceBatch iccreate = new ICAutoCreateInvoiceBatch();
//		iccreate.setCreatedBy("Bruce Wayne","333");
//		if(iccreate.createNewBatch(conn, "Bruce Wayne")){
//			String sStatus = "";
//			if (iccreate.getM_sBatchNumber().compareToIgnoreCase("-1") == 0){
//				sStatus += "No adjustment batch was created";
//			}else{
//				sStatus += "Successfully created and posted adjustment batch " + iccreate.getM_sBatchNumber();
//			}
//			sStatus += " - export sequence number was " + iccreate.getsExportSequenceNumber() + ".";
//			System.out.println(sStatus);
//		}else{
//			System.out.println(iccreate.getErrorMessage());
//		}
////		System.out.println("DONE");
//	
//	}
	
	public String getErrorMessage (){
		return m_sErrorMessage;
	}
	public boolean setCreatedBy(String sCreatedByFullName, String sUserID){
		
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
		m_sUserID = sUserID;
		return true;
	}
	public boolean checkToCreateNewBatch(
		Connection conn, 
		String sCreatedByFullName, 
		ServletContext context, 
		String sDBID, 
		String sUserID,
		String smmddyyyyBatchDate){
		
		m_sCreatedByFullName = sCreatedByFullName;
		
		//If there are NO invoices to be processed, set that message, and return:
		String SQL = "SELECT"
			+ " " + SMTableicpoinvoiceheaders.lid
			+ " FROM " + SMTableicpoinvoiceheaders.TableName
			+ " WHERE ("
				+ "(" + SMTableicpoinvoiceheaders.lexportsequencenumber + " = 0)"
			+ ")"
		;
		
		try {
			ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
			if (!rs.next()){
				rs.close();
				m_sErrorMessage = "There are no unposted invoices to be processed. \n";
				return false;
			}
			rs.close();
		} catch (SQLException e1) {
			m_sErrorMessage = "Error [1492099077] checking for unposted invoices with SQL: '" + SQL + "' - " + e1.getMessage();
			return false;
		}
		
		//Check posting flag
		ICOption option = new ICOption();
    	try{
    		String sPostingProcess = "CREATING BATCH FROM INVOICES";
    		option.checkAndUpdatePostingFlagWithoutConnection(
    			context, 
    			sDBID, 
    			clsServletUtilities.getFullClassName(this.toString() + ".createNewBatch"), 
    			sCreatedByFullName, 
    			sPostingProcess
    		);
    	}catch (Exception e){
    		m_sErrorMessage = "Error [1529951303] checking for previous posting - " + e.getMessage();
    		return false;
    	}

    	try {
			createNewInvoiceBatch(context, conn, sDBID, sUserID, smmddyyyyBatchDate);
		} catch (Exception e1) {
			m_sErrorMessage = "Error [1529951627] - " + e1.getMessage();
			try {
				option.resetPostingFlagWithoutConnection(context, sDBID);
			} catch (Exception e) {
				//Don't trap this, because the user will just have to reset the posting FLAG - AND we don't want to lose any error message coming back from the createNewInvoiceBatch function
			}
			return false;
		}

		try {
			option.resetPostingFlagWithoutConnection(context, sDBID);
		} catch (Exception e) {
			m_sErrorMessage = "Error [1529951626] - " + e.getMessage();
			return false;
		}
		
		return true;
	}
	private void createNewInvoiceBatch(
		ServletContext context, 
		Connection conn, 
		String sDBID, 
		String sUserID,
		String smmddyyyyBatchDate
		) throws Exception{
		//Start a data transaction:
		if (!clsDatabaseFunctions.start_data_transaction(conn)){
			throw new Exception("Error [1529951752]  - Could not start data transaction");
		}
		
		//Update ALL the most recent costs here for inventory items, whether 'STOCK' on 'NON-STOCK' inventory:
		// [1519070245] - this should be the ONLY place we update the most recent cost....
		
		//Next we check to see if any invoices were entered at different costs than the receipt and if so
		//we create and adjustment batch for them:
		if (!createRequiredAdjustments(conn, sUserID)){
			clsDatabaseFunctions.rollback_data_transaction(conn);
			throw new Exception("Error [1529951753] - could not create required adjustments - " + this.getErrorMessage());
		}
		
		//Record the export sequence so we have this AP export documented:
		if (!insertExportSequence(conn, "PO Invoice Export", m_sUserID, m_sCreatedByFullName)){
			clsDatabaseFunctions.rollback_data_transaction(conn);
			throw new Exception("Error [1529951754] - could not insert invoice export sequence - " + this.getErrorMessage());
		}
		
		//IF we are using the SMCP AP module, then we only create a new batch of AP invoices for the SMCP AP system.
		APOptions apopt = new APOptions();
		try {
			apopt.load(conn);
		} catch (Exception e) {
			clsDatabaseFunctions.rollback_data_transaction(conn);
			throw new Exception("Error [1489071020] loading AP options - " + e.getMessage());
			
		}
		
		//Get the user ID:
		SMUser usr = new SMUser();
		try {
			usr.load(m_sUserID, conn);
		} catch (Exception e1) {
			clsDatabaseFunctions.rollback_data_transaction(conn);
			throw new Exception("Error [1525453822] getting user information - " + e1.getMessage());
		}
		
		if (apopt.getUsesSMCPAP().compareToIgnoreCase("1") == 0){
			// Create a batch for SMCP AP
			try {
				createInvoiceBatchInAP(
					conn, 
					m_sExportSequenceNumber, 
					usr.getlid(), 
					usr.getsUserFullName(), 
					smmddyyyyBatchDate
				);
			} catch (Exception e) {
				clsDatabaseFunctions.rollback_data_transaction(conn);
				throw new Exception("Error [1489078938] creating AP batch in SMCP - " + e.getMessage());
			}
		}else{
			//If we are NOT using the SMCP AP system, then we just create a file that can later be imported into an AP module:
			//Create an export file for AP:
			if (!createFileForManualAPImport(conn, m_sExportSequenceNumber)){
				clsDatabaseFunctions.rollback_data_transaction(conn);
				throw new Exception("Error [1529952006] creating AP batch in SMCP - " + getErrorMessage());
			}
			
			try {
				//This is for TESTING only:
				APOptions aropt = new APOptions();
				if (aropt.checkTestingFlag(conn)){
					try {
						createInvoiceBatchInAP(conn, m_sExportSequenceNumber, usr.getlid(), usr.getsUserFullName(), smmddyyyyBatchDate);
					} catch (Exception e) {
						clsDatabaseFunctions.rollback_data_transaction(conn);
						throw new Exception("Error [1489078939] creating TEST AP batch in SMCP - " + e.getMessage());
					}
				}
			} catch (Exception e) {
				clsDatabaseFunctions.rollback_data_transaction(conn);
				throw new Exception("Error [1489071021] reading AP option for testing SMCP AP Batch creation - " + e.getMessage());
			}
		}
		
		//Update the exported flag:
		String SQL = "UPDATE"
			+ " " + SMTableicpoinvoiceheaders.TableName
			+ " SET " + SMTableicpoinvoiceheaders.lexportsequencenumber + " = " + m_sExportSequenceNumber
			+ " WHERE ("
				+ "(" + SMTableicpoinvoiceheaders.lexportsequencenumber + " = -1" + ")"
			+ ")"
			;
		try{
		    Statement stmt = conn.createStatement();
		    stmt.executeUpdate(SQL);
		}catch (Exception ex) {
			clsDatabaseFunctions.rollback_data_transaction(conn);
			throw new Exception("Error [1529952127] - Could not update invoices exported flag to 'exported' with SQL: " + SQL
    			+ " - " + ex.getMessage());
		}
		
		//Now go ahead and post the adjustment batch (if there was one):
		//If there were any adjustments needed, there will be a batch number - if not, the
		//batch number will still be -1, and we won't need to post a batch:
		if (m_sBatchNumber.compareToIgnoreCase("-1") != 0){
			
			if (!postBatch(conn)){
				clsDatabaseFunctions.rollback_data_transaction(conn);
				throw new Exception("Error [1529952189] - " + this.getErrorMessage());
			}
		}
		
		if (!clsDatabaseFunctions.commit_data_transaction(conn)){
			clsDatabaseFunctions.rollback_data_transaction(conn);
			throw new Exception("Error [1529952190] Could not commit data transaction - failed to process invoices");
		}
		
	}
	public void createInvoiceBatchInAP(
		Connection conn,
		String sExportSequence,
		String sUserID,
		String sUserFullName,
		String smmddyyyyBatchDate
		) throws Exception{
			
		//Create a batch in SMCP for the AP invoices:
		APBatch batch = new APBatch("-1");
		batch.setsbatchdate(smmddyyyyBatchDate);
		batch.setsbatchdescription("Invoices from PO export sequence " + sExportSequence);
		batch.setsbatchstatus(Integer.toString(SMBatchStatuses.IMPORTED));
		batch.setsbatchtype(Integer.toString(SMTableapbatches.AP_BATCH_TYPE_INVOICE));
		batch.setlcreatedby(sUserID);
		batch.setllasteditedby(sUserID);

		//Now add each of the invoices:
		//Next, read each invoice to create an export record for it:
		//At this point the export sequence number still hasn't been set on the IC PO Invoice Headers:
		String SQL = "SELECT"
			+ " " + SMTableicpoinvoiceheaders.lid
			+ " FROM " + SMTableicpoinvoiceheaders.TableName
			+ " WHERE ("
				+ "(" + SMTableicpoinvoiceheaders.lexportsequencenumber + " = " + "-1" + ")"
			+ ")"
			+ " ORDER BY " + SMTableicpoinvoiceheaders.lid
		;
        try {
        	ResultSet rsInvoices = clsDatabaseFunctions.openResultSet(SQL, conn);
        	while (rsInvoices.next()){
        		
            	ICPOInvoice inv = new ICPOInvoice();
            	inv.setM_slid(Long.toString(rsInvoices.getLong(SMTableicpoinvoiceheaders.lid)));
            	if (!inv.load(conn)){
            		rsInvoices.close();
            		throw new Exception("Error [1489087545]" 
            			+ " Could not load invoice with ID: " + inv.getM_slid() + " to create AP batch.");
        		}
            	//Load up the AP batch entry, then add it to the batch:
            	APBatchEntry entry = new APBatchEntry();
            	entry.setsdatdiscount(inv.getM_sdatdiscount());
            	entry.setsdatdocdate(inv.getM_sdatinvoice());
            	entry.setsdatduedate(inv.getM_sdatdue());
            	entry.setsdatentrydate(inv.getM_sdatentered());
            	entry.setsdocnumber(inv.getM_sinvoicenumber());
            	entry.setsentryamount(inv.getM_sinvoicetotal());
            	entry.setsentrydescription(inv.getM_sdescription());
            	entry.setsdiscountamt(inv.getM_sdiscount());
            	entry.setsbdtaxrate(inv.getbdtaxrate());
            	entry.setsicalculateonpurchaseorsale(inv.geticalculateonpurchaseorsale());
            	entry.setsiinvoiceincludestax(inv.getiinvoiceincludestax());
            	entry.setsitaxid(inv.getitaxid());
            	entry.setstaxjurisdiction(inv.getstaxjurisdiction());
            	entry.setstaxtype(inv.getstaxtype());
            	entry.setsterms(inv.getM_sterms());
            	entry.setsvendorname(inv.getsVendorName());
            	entry.setsentrytype(Integer.toString(SMTableapbatchentries.ENTRY_TYPE_INV_INVOICE));
            	
            	APVendor ven = new APVendor();
            	ven.setsvendoracct(inv.getM_svendor());
            	if (!ven.load(conn)){
               		rsInvoices.close();
            		throw new Exception("Error [1489168003]" 
            			+ " Could not load vendor information for vendor : '" + inv.getM_svendor() 
            			+ "'"
            			+ " for PO Invoice ID " + inv.getM_slid()
            			+ " - " + ven.getErrorMessages());
        		}
            	APAccountSet apset = new APAccountSet();
            	apset.setlid(ven.getiapaccountset());
            	if (!apset.load(conn)){
               		rsInvoices.close();
            		throw new Exception("Error [1489168004]" 
            			+ " Could not load AP account set info for vendor : '" + inv.getM_svendor() + "'"
            			+ " for PO Invoice ID " + inv.getM_slid()
            			+ " - " + ven.getErrorMessages());
        		}
            	entry.setscontrolacct(apset.getspayablescontrolacct());
            	entry.setsvendoracct(inv.getM_svendor());
            	for (int i = 0; i < inv.getLines().size(); i++){
            		ICPOInvoiceLine invline = inv.getLines().get(i);
            		APBatchEntryLine line = new APBatchEntryLine();
            		
            		ICPOReceiptHeader rcpt = new ICPOReceiptHeader();
            		rcpt.setsID(invline.getsporeceiptid());
            		String sPOHeaderID = "0";
            		try {
						if(rcpt.load(conn)){
							sPOHeaderID = rcpt.getspoheaderid();
						}
					} catch (Exception e) {
						//No need to catch this - leave the PO at '0'
					}
            		
            		line.setslpoheaderid(sPOHeaderID);
            		line.setslreceiptheaderid(invline.getsporeceiptid());
            		line.setsbdamount(invline.getsinvoicedcost());
            		line.setscomment("RCPT #" + invline.getsporeceiptid());
            		line.setsdescription("("+ invline.getsqtyreceived() + ") - " + invline.getsitemdescription());
            		line.setsdistributionacct(invline.getsexpenseaccount());
            		line.setslporeceiptlineid(invline.getsporeceiptlineid());
            		entry.addLine(line);
            	}
            	batch.addBatchEntry(entry);
        	}
        } catch (Exception e){
        	throw new Exception("Error [1489087546]" 
        		+ " Could not read invoice recordset with SQL: " + SQL + " - " + e.getMessage());
        }
        
        //Save the batch - the connection being used (conn) is already inside a data transaction, so we don't have to deal with transactions here:
        //SMUtilities.start_data_transaction(conn);
        try {
			batch.save_without_data_transaction(conn, sUserID, sUserFullName, false);
		} catch (Exception e) {
			//SMUtilities.rollback_data_transaction(conn);
			throw new Exception("Error [1489113737] - could not save AP batch - " + e.getMessage());
		}
        //SMUtilities.commit_data_transaction(conn);
        
        //Now go to post the batch:
        // TJR - 6/12/2017 - turned off the automatic posting for now:
        
        //try {
		//	batch.post_with_connection(conn, sUser);
		//} catch (Exception e) {
        //	throw new Exception("Error [1489771857]" 
        //   	+ " Could not post AP batch number " + batch.getsbatchnumber() + " - " + e.getMessage());
		//}
        
        return;
	}
	private boolean postBatch(Connection conn){
		
		ICEntryBatch icbatch = new ICEntryBatch(m_sBatchNumber);
		if (!icbatch.load(conn)){
			m_sErrorMessage = "Could not load imported batch # " + m_sBatchNumber 
			+ " - import failed - " + icbatch.getErrorMessages();
			return false;
		}
		try {
			icbatch.postImportedBatchwithout_data_transaction(conn, m_sCreatedByFullName, m_sUserID);
		} catch (Exception e) {
			m_sErrorMessage = "Could not post imported batch # " + m_sBatchNumber 
			+ " - " + icbatch.getErrorMessages() + " - import failed";
			return false;
		}

		return true;
	}

	private boolean insertExportSequence(Connection conn, String sComment, String sUserID, String sUserFullName){
		
		boolean bResult = true;
		String SQL = "INSERT INTO " + SMTableicinvoiceexportsequences.TableName
			+ " ("
			+ SMTableicinvoiceexportsequences.datexported
			+ ", " + SMTableicinvoiceexportsequences.scomment
			+ ", " + SMTableicinvoiceexportsequences.suserfullname
			+ ", " + SMTableicinvoiceexportsequences.luserid
			+ ") VALUES ("
			+ "NOW()"
			+ ", '" + sComment + "'"
			+ ", '" + clsDatabaseFunctions.FormatSQLStatement(sUserFullName) + "'"
			+ ", " + sUserID + ""
			+ ")"
		;
		
		try{
		    Statement stmt = conn.createStatement();
		    stmt.executeUpdate(SQL);
		}catch (SQLException ex) {
    		m_sErrorMessage = "Error [1528851206] - Could not update invoices export sequence with SQL: " + SQL
    				+ " - " + ex.getMessage();
    		bResult = false;
		}
		
		SQL = "SELECT LAST_INSERT_ID()";
		try {
			ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
			if (rs.next()) {
				m_sExportSequenceNumber = Long.toString(rs.getLong(1));
			} else {
				bResult = false;
				m_sErrorMessage = "Error [1528851207] - Could not get last export sequence number with SQL: " + SQL;
			}
			rs.close();
		} catch (SQLException e) {
			m_sErrorMessage = "Error [1528851208] - Could not get last export sequence number with SQL: " + SQL;
			bResult = false;
		}
		
		return bResult;
	}
	private boolean createFileForManualAPImport(Connection conn, String sExportSequence){
		
    	ICAPExport export = new ICAPExport();
    	String SQL = "SELECT * FROM " + SMTablesmoptions.TableName;
    	try{
	    	ResultSet rsOptions = clsDatabaseFunctions.openResultSet(SQL, conn);
	    	
	    	if (rsOptions.next()){
	    		export.setExportFilePath(rsOptions.getString(SMTablesmoptions.sfileexportpath));
	    	}else{
	    		m_sErrorMessage = "Error [1528851209] - Could not get SMOption record to read export file.";
	    	}
	    	rsOptions.close();
    	}catch (SQLException e){
    		m_sErrorMessage = "Error [1528851210] reading path for export file: " + e.getMessage();
    		return false;
    	}
    	
		//Next, read each invoice to create an export record for it:
		SQL = "SELECT"
			+ " " + SMTableicpoinvoiceheaders.lid
			+ " FROM " + SMTableicpoinvoiceheaders.TableName
			+ " WHERE ("
				+ "(" + SMTableicpoinvoiceheaders.lexportsequencenumber + " = -1)"
			+ ")"
			+ " ORDER BY " + SMTableicpoinvoiceheaders.lid
		;
        try {
        	ResultSet rsInvoices = clsDatabaseFunctions.openResultSet(SQL, conn);
        	while (rsInvoices.next()){
            	ICPOInvoice inv = new ICPOInvoice();
            	inv.setM_slid(Long.toString(rsInvoices.getLong(SMTableicpoinvoiceheaders.lid)));
            	if (!inv.load(conn)){
            		m_sErrorMessage = 
        				"Error [1528851211] - Could not load invoice with ID: " + inv.getM_slid() + " for export";
        			rsInvoices.close();
        			return false;
        		}
        		
            	//Calculate the discount percentage:
            	BigDecimal bdDiscountAmount = new BigDecimal(inv.getM_sdiscount().replace(",", ""));
            	BigDecimal bdBaseForDiscount = new BigDecimal(inv.getM_sinvoicetotal().replace(",", ""));
            	BigDecimal bdDiscountPercentage = new BigDecimal(0);
            	if (bdBaseForDiscount.compareTo(BigDecimal.ZERO) != 0){
            		bdDiscountPercentage = bdDiscountAmount.divide(
            			bdBaseForDiscount, 2, BigDecimal.ROUND_HALF_UP).multiply(new BigDecimal(100));
            	}
            	
            	//Calculate the amount due:
            	BigDecimal bdAmountDue = new BigDecimal(0);
            	bdAmountDue = bdBaseForDiscount.subtract(bdDiscountAmount);

            	//Get the first PO number from the po invoice lines:
            	String sReceiptID = "";
            	String sPOString = "";
            	boolean bInvoiceIncludesMultipleReceipts = false;
            	for (int i = 0; i < inv.getLines().size(); i++){
            		//Don't bother with any lines that aren't from receipts:
            		if (inv.getLines().get(i).getsporeceiptlineid().compareToIgnoreCase("-1") !=0){
            			//If we've already gotten a receipt ID:
            			if (sReceiptID.compareToIgnoreCase("") != 0){
            				//Then is this line has a DIFFERENT receipt line ID:
            				if (sReceiptID.compareToIgnoreCase(inv.getLines().get(i).getsporeceiptid()) != 0){
            					//This invoice must include multiple receipts:
            					bInvoiceIncludesMultipleReceipts = true;
            					//Since we've already gotten a receipt line ID, we can jump out:
            					break;
            				}
            			}else{
            				//But if we haven't yet gotten a receiptline ID, store this one:
            				sReceiptID = inv.getLines().get(i).getsporeceiptid();
            			}
            		}
            	}
            	
            	//If we DID get a receipt ID, then we need to look up the PO:
            	if (sReceiptID.compareToIgnoreCase("") !=0){
            		ICPOReceiptHeader rcpt = new ICPOReceiptHeader();
            		rcpt.setsID(sReceiptID);
            		if (rcpt.load(conn)){
            			sPOString = rcpt.getspoheaderid();
            		}
            	}
            	
            	if (bInvoiceIncludesMultipleReceipts){
            		sPOString += "...";
            	}
            	
        		//The discount date may be blank - (00/00/0000) - in that case, we'll just use the invoice date, for the export:
        		java.sql.Date datDiscountDate = null;
        		try {
					datDiscountDate = clsDateAndTimeConversions.StringTojavaSQLDate("MM/dd/yyyy", inv.getM_sdatdiscount());
				} catch (Exception e) {
					//If the date is blank, then use the invoice date...
					try {
						datDiscountDate = clsDateAndTimeConversions.StringTojavaSQLDate("MM/dd/yyyy", inv.getM_sdatinvoice());
					} catch (ParseException e1) {
	            		m_sErrorMessage = 
	            				"Error [1478570658] could not parse invoice date '" + inv.getM_sdatinvoice() 
	            				+ "' for invoice #: " + inv.getM_sinvoicenumber() + " - " + e.getMessage();
	            			rsInvoices.close();
	            			return false;
					}
				}
            	try {
					export.addHeader(
						inv.getM_svendor(),
						inv.getM_sinvoicenumber(),
						sPOString, 
						SMTableicpoinvoiceheaders.POINVOICE_SIGNATURE + " " + inv.getM_sdescription(),
						clsDateAndTimeConversions.StringTojavaSQLDate("MM/dd/yyyy", inv.getM_sdatinvoice()),
						inv.getM_sterms(),
						clsDateAndTimeConversions.StringTojavaSQLDate("MM/dd/yyyy", inv.getM_sdatdue()),
						datDiscountDate,
						bdDiscountPercentage,
						bdDiscountAmount,
						bdAmountDue,
						new BigDecimal(inv.getM_sinvoicetotal().replace(",", "")),  //total amt
						clsDateAndTimeConversions.StringTojavaSQLDate("MM/dd/yyyy", inv.getM_sdatentered())
					);
				} catch (ParseException e) {
            		m_sErrorMessage = 
        				"Error [1478540764] Could not load convert date to export header for invoice #: " + inv.getM_sinvoicenumber() + " - " + e.getMessage();
        			rsInvoices.close();
        			return false;
				}
        		        		
        		for (int i = 0; i < inv.getLines().size(); i ++){
        			//Now add each line from the invoice as an AP Invoice detail:
        			ICPOInvoiceLine line = inv.getLines().get(i);
            		try {
						export.addDetail(
							line.getsitemdescription(),
							line.getsexpenseaccount(),
							new BigDecimal(line.getsinvoicedcost().replace(",", "")),
							clsDateAndTimeConversions.StringTojavaSQLDate("MM/dd/yyyy", inv.getM_sdatinvoice())
						);
					} catch (ParseException e) {
		           		m_sErrorMessage = 
		        				"Could not load convert invoice date '" + inv.getM_sdatinvoice() 
		        				+ "' to export detail for invoice #: " + inv.getM_sinvoicenumber() + " - " + e.getMessage();
		        			rsInvoices.close();
		        			return false;
					}
        		}

        		//Finally, add a payment schedule line to the export:
        		try {
					export.addPaymentSchedule(
						clsDateAndTimeConversions.StringTojavaSQLDate("MM/dd/yyyy", inv.getM_sdatdue()), 
						new BigDecimal(inv.getM_sinvoicetotal().replace(",", "")), 
						datDiscountDate,
						new BigDecimal(inv.getM_sdiscount().replace(",", ""))
					);
				} catch (ParseException e) {
            		m_sErrorMessage = 
        				"Could not load convert date to export payment schedule - " + e.getMessage();
        			rsInvoices.close();
        			return false;

				}
        	}
        	rsInvoices.close();
        } catch (SQLException e){
        	m_sErrorMessage = "SQL Error opening batch for export: " + e.getMessage();
        	return false;
        }
        String sExportSequenceNumber = clsStringFunctions.PadLeft(sExportSequence, "0", 6);
        
        if (export.getExportFilePath().compareToIgnoreCase("") != 0){
	        if (!export.writeExportFile(
	        		SMModuleTypes.IC, 
	        		"INVOICE", 
	        		sExportSequenceNumber,
	        		conn)
	        	){
	        	m_sErrorMessage = "Error writing AP export file";
	        	return false;
	        }
		}
		return true;
	}

	private boolean createRequiredAdjustments(Connection conn, String sUserID){
		
		//First, we have to read all the unexported invoices and look for instances where 
		//we have to create adjustments in a batch
		
		//Set all the unexported flags on the invoices to -1 so we know they are in processing:
		
		String SQL = "UPDATE"
			+ " " + SMTableicpoinvoiceheaders.TableName
			+ " SET " + SMTableicpoinvoiceheaders.lexportsequencenumber + " = -1"
			+ " WHERE ("
				+ "(" + SMTableicpoinvoiceheaders.lexportsequencenumber + " = 0" + ")"
			+ ")"
			;
		try{
		    Statement stmt = conn.createStatement();
		    stmt.executeUpdate(SQL);
		}catch (Exception ex) {
    		m_sErrorMessage = "Could not update invoices exported flag to 'in process' with SQL: " + SQL
    				+ " - " + ex.getMessage();
    		return false;
		}
		
		//Next, read each invoice and create an adjustment entry to any invoice that needs it
		SQL = "SELECT"
			+ " " + SMTableicpoinvoiceheaders.lid
			+ " FROM " + SMTableicpoinvoiceheaders.TableName
			+ " WHERE ("
				+ "(" + SMTableicpoinvoiceheaders.lexportsequencenumber + " = -1)"
			+ ")"
		;
		if (bDebugMode){
			System.out.println("[1579191022] In " + this.toString() + " reading in process invoices SQL = " + SQL);
		}
		boolean bInvalidInvoiceFound = false;
		try {
			ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
			while (rs.next()) {
				ICPOInvoice entry = new ICPOInvoice();
				entry.setM_slid(Long.toString(rs.getLong(SMTableicpoinvoiceheaders.lid)));
				if (!entry.load(conn)){
					m_sErrorMessage = "Could not load invoice - " + entry.getErrorMessages();
				}
				
				if (!entry.validate_totals()){
					m_sErrorMessage += "Invoice # " + entry.getM_slid() + " - " + entry.getErrorMessages() + "<BR>";
					bInvalidInvoiceFound = true;
				}
				
				if (!createAdjustmentEntriesForIndividualInvoice(entry, conn, sUserID)){
					rs.close();
					return false;
				}
			}
			rs.close();
		} catch (SQLException e) {
			m_sErrorMessage = "Could not process invoices - " + e.getMessage();
			return false;
		}
		
		if (bInvalidInvoiceFound){
			return false;
		}
		
		//If all goes OK, we return true:
		return true;

	}
	private boolean createAdjustmentEntriesForIndividualInvoice (ICPOInvoice inv, Connection conn, String sUserID){

		boolean bNeedsAdjustmentEntry = false;
		
		for (int i = 0; i < inv.getLines().size(); i++){
			ICPOInvoiceLine invline = inv.getLines().get(i);
			//We only need adjustments on STOCK inventory items:
			if (invline.getsnoninventoryitem().compareToIgnoreCase("0") == 0){
				ICItem item = new ICItem(invline.getsitemnumber());
				if (!item.load(conn)){
					m_sErrorMessage = "Error [1520372316] could not load item information for item '" + invline.getsitemnumber() + "' - " 
						+ item.getErrorMessageString();
					return false;
				}
				
				//Ignore non-stock items:
				if (item.getNonStockItem().compareToIgnoreCase("0") == 0){
					BigDecimal bdInvoicedCost;
					BigDecimal bdReceivedCost;
					try {
						bdInvoicedCost = new BigDecimal(invline.getsinvoicedcost().replace(",", ""));
						bdReceivedCost = new BigDecimal(invline.getsreceivedcost().replace(",", ""));
					} catch (NumberFormatException e) {
						m_sErrorMessage = "Error [1520372315] converting cost into decimal - " + e.getMessage();
						return false;
					}
					//TODO - allow for a case where the received and invoiced costs are the same, but because
					//of a discount or whatever, the actual amount paid is different:
					if (bdInvoicedCost.compareTo(bdReceivedCost) != 0){
						bNeedsAdjustmentEntry = true;
					}
				}
			}
		}
		
		if (bNeedsAdjustmentEntry){
			//If no adjustment batch has been created yet, create one:
			if (m_sBatchNumber.compareToIgnoreCase("-1") == 0){
				if (!createBatch(conn)){
					return false;
				}
			}
			//Populate the entry:
			ICEntry m_CurrentICEntry = new ICEntry();
			m_CurrentICEntry.sBatchNumber(m_sBatchNumber);
			m_CurrentICEntry.sBatchType(Integer.toString(ICBatchTypes.IC_ADJUSTMENT));
			m_CurrentICEntry.sEntryDate(inv.getM_sdatentered());
			m_CurrentICEntry.sDocNumber("INV" + inv.getM_slid());
			m_CurrentICEntry.sEntryDescription("PO INVOICE");
			m_CurrentICEntry.sEntryType(Integer.toString(ICEntryTypes.ADJUSTMENT_ENTRY));
			m_CurrentICEntry.sEntryNumber("-1");
			
			//Figure out how to create entry lines out of the invoice
			for (int i = 0; i < inv.getLines().size(); i++){
				ICPOInvoiceLine invline = inv.getLines().get(i);
				//We only need adjustments on inventory items:
				if (invline.getsnoninventoryitem().compareToIgnoreCase("0") == 0){
					ICItem item = new ICItem(invline.getsitemnumber());
					if (!item.load(conn)){
						m_sErrorMessage = "Error [1520372318] could not load item information for item '" + invline.getsitemnumber() + "' - " 
							+ item.getErrorMessageString();
						return false;
					}
					
					//Ignore non-stock items:
					if (item.getNonStockItem().compareToIgnoreCase("0") == 0){
						BigDecimal bdInvoicedCost;
						BigDecimal bdReceivedCost;
						try {
							bdInvoicedCost = new BigDecimal(invline.getsinvoicedcost().replace(",", ""));
							bdReceivedCost = new BigDecimal(invline.getsreceivedcost().replace(",", ""));
						} catch (NumberFormatException e) {
							m_sErrorMessage = "Error converting cost into decimal - " + e.getMessage();
							return false;
						}
						
						if (bdInvoicedCost.compareTo(bdReceivedCost) != 0){
							BigDecimal bdLineCostDifference = new BigDecimal(0);
							bdLineCostDifference = bdInvoicedCost.subtract(bdReceivedCost);
							
							//Update the most recent cost of the item:
							item = new ICItem(invline.getsitemnumber());
							if (!item.load(conn)){
								m_sErrorMessage = "Error loading item " + invline.getsitemnumber() + " to "
									+ "update most recent cost";
								return false;
							}
							BigDecimal bdUnitCost = new BigDecimal(0);
							BigDecimal bdQty = new BigDecimal(invline.getsqtyreceived().replace(",", ""));
							bdUnitCost = bdInvoicedCost.divide(
									bdQty, SMTableicpoinvoicelines.bdinvoicedcostScale, BigDecimal.ROUND_HALF_UP);
							item.setMostRecentCost(clsManageBigDecimals.BigDecimalToScaledFormattedString(4, bdUnitCost));
							if (!item.save(m_sCreatedByFullName, m_sUserID, conn)){
								m_sErrorMessage = "Error saving item " + invline.getsitemnumber() + " to "
								+ "update most recent cost of " 
								+ clsManageBigDecimals.BigDecimalToScaledFormattedString(4, bdUnitCost) 
								+ " - " + item.getErrorMessageString();
								return false;
							}
							
							ICVendorItem venitem = new ICVendorItem();
							//We don't need to update the vendor item number, but we DO need to update
							// the unit cost:
							if (!venitem.updateVendorItem(
									invline.getsitemnumber(), 
									inv.getM_svendor(), 
									"", 
									bdUnitCost, 
									"",
									conn)
							){
								m_sErrorMessage = "Error updating vendor item " + invline.getsitemnumber();
								return false;
							}
	
							//Need to add a line to the entry:
							ICEntryLine entryline = new ICEntryLine();
							entryline.sBatchNumber(m_sBatchNumber);
							entryline.sCategoryCode("");
							entryline.sComment("From invoice ID " + inv.getM_slid());
							
							//Get the inventory control account:
					    	String SQL = "SELECT"
					    		+ " " + SMTablelocations.sLocation
					    		+ ", " + SMTablelocations.sGLPayableClearingAcct
					    		+ ", " + SMTablelocations.sGLInventoryAcct
					    		+ " FROM " + SMTablelocations.TableName
					    		+ " WHERE ("
					    			+ "(" + SMTablelocations.sLocation + " = '" + invline.getslocation() + "')"
					    		+ ")"
					    		;
					    	try {
								ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
								if (rs.next()){
									entryline.sControlAcct(rs.getString(SMTablelocations.sGLInventoryAcct));
									entryline.sDistributionAcct(rs.getString(SMTablelocations.sGLPayableClearingAcct));
								}else{
									m_sErrorMessage = "Location " + invline.getslocation() + " is not valid.";
									rs.close();
									return false;
								}
								rs.close();
							} catch (SQLException e) {
								m_sErrorMessage = "Error validating Location " + invline.getslocation() 
								+ " - " + e.getMessage();
								return false;
							}
							//IF the cost bucket is intact, meaning that if the remaining qty and cost
							//in the cost bucket are the same as the originally received qty and cost
							//then we can update the bucket.  Otherwise, we need to create a new one.
							SQL = "SELECT"
								+ " " + SMTableiccosts.iId
								+ " FROM " + SMTableiccosts.TableName
								+ " WHERE ("
									+ "(" + SMTableiccosts.lReceiptLineID + " = " 
										+ invline.getsporeceiptlineid() + ")"
									+ " AND (" + SMTableiccosts.bdCost + " = " 
										+ invline.getsreceivedcost().replace(",", "") + ")"
									+ " AND (" + SMTableiccosts.bdQty + " = " 
										+ invline.getsqtyreceived().replace(",", "") + ")"
								+ ")"
							;
							try {
								ResultSet rscosts = clsDatabaseFunctions.openResultSet(SQL,
										conn);
								if (rscosts.next()) {
									entryline.sCostBucketID(Long.toString(rscosts
											.getLong(SMTableiccosts.iId)));
								} else {
									entryline.sCostBucketID("-1");
								}
								rscosts.close();
							} catch (SQLException e) {
								m_sErrorMessage = "Error reading cost id with SQL: " + SQL
									+ " - " + e.getMessage();
								return false;
							}
							entryline.sDescription("From invoice ID " + inv.getM_slid());
							entryline.setCostString(clsManageBigDecimals.BigDecimalToScaledFormattedString(
								SMTableicentrylines.bdcostScale, bdLineCostDifference));
							entryline.setPriceString("0.00");
							entryline.setQtyString(clsManageBigDecimals.BigDecimalToScaledFormattedString(
									SMTableicentrylines.bdqtyScale, BigDecimal.ZERO));
							entryline.sItemNumber(invline.getsitemnumber());
							entryline.sLocation(invline.getslocation());
							entryline.sReceiptLineID(invline.getsporeceiptlineid());
							m_CurrentICEntry.add_line(entryline);
						}
					}
				}
			}

			if (!m_CurrentICEntry.save_without_data_transaction(conn, sUserID)){
				m_sErrorMessage = "Could not save entry - " + m_CurrentICEntry.getErrorMessage();
				return false;
			}
		}
		return true;
	}
	private boolean createBatch(Connection conn){
		
		ICEntryBatch batch = new ICEntryBatch("-1");
		
		batch.iBatchStatus(SMBatchStatuses.IMPORTED);
		batch.iBatchType(ICBatchTypes.IC_ADJUSTMENT);
		batch.sBatchDescription("SM Invoice Adjustments Batch");
		batch.sSetCreatedByFullName(m_sCreatedByFullName);
		batch.sSetCreatedByID(m_sUserID);
		batch.sSetLastEditedByFullName(m_sCreatedByFullName);
		batch.sSetLastEditedByID(m_sUserID);
		if (!batch.save_without_data_transaction(conn, m_sCreatedByFullName, m_sUserID)){
			m_sErrorMessage = "Could not save batch - " + batch.getErrorMessages();
			return false;
		}
		
		m_sBatchNumber = batch.sBatchNumber();
		
		return true;
	}
	public String getM_sBatchNumber() {
		return m_sBatchNumber;
	}
	public String getsExportSequenceNumber() {
		return this.m_sExportSequenceNumber;
	}
	
}
