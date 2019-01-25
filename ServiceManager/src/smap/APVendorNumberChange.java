package smap;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import SMClasses.SMBatchStatuses;
import SMDataDefinition.SMTableapbatchentries;
import SMDataDefinition.SMTableapbatches;
import SMDataDefinition.SMTableapchecks;
import SMDataDefinition.SMTableapmatchinglines;
import SMDataDefinition.SMTableaptransactions;
import SMDataDefinition.SMTableapvendorremittolocations;
import SMDataDefinition.SMTableapvendorstatistics;
import SMDataDefinition.SMTableicpoheaders;
import SMDataDefinition.SMTableicpoinvoiceheaders;
import SMDataDefinition.SMTableicvendoritems;
import SMDataDefinition.SMTableicvendors;
import SMDataDefinition.SMTablelaborbackcharges;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsServletUtilities;
import smcontrolpanel.SMUtilities;

public class APVendorNumberChange extends java.lang.Object{

	/*
	 * These are the tables that we have to be concerned about when we change or merge vendor accounts:
	apbatchentries.svendoracct
	apbatchentries.svendorname
	apchecks.svendoracct
	apchecks.svendorname
	apmatchinglines.svendor
	aptransactions.svendor
	apvendorremittolocations.svendoracct
	apvendorstatistics.svendoracct
	icpoheaders.svendor
	icpoheaders.svendorname
	icpoinvoiceheaders.svendor
	icpoinvoiceheaders.svendorname
	icvendoritems.svendor
	icvendors.svendoracct
	icvendors.sname
	laborbackcharges.svendoracct
	 */
	
	private static final String VENDOR_NUMBER_CHANGE = "CHANGING VENDOR NUMBER";
	
	public APVendorNumberChange(){
	}
	public void processChange(
			Connection conn,
			String sFromVendor,
			String sToVendor,
			boolean bIsMerge,
			String sUserID
			) throws Exception{
		
	    APVendor apvend = new APVendor();
	    apvend.setsvendoracct(sFromVendor);
	    if(!apvend.load(conn)){
	    	throw new Exception("Error [1507573942] - Cannot load vendor " + sFromVendor + " - " + apvend.getErrorMessages() + ".");
	    }
		
	    //Validate that there are no unposted batch entries AT ALL:
	    String SQL = "SELECT " + SMTableapbatches.lbatchnumber + " FROM " + SMTableapbatches.TableName
	    	+ " WHERE ("
	    		+ "(" + SMTableapbatches.ibatchstatus + " = " + SMBatchStatuses.ENTERED + ")"
	    		+ " OR (" + SMTableapbatches.ibatchstatus + " = " + SMBatchStatuses.IMPORTED + ")"
	    	+ ")"
	    	;
	    try{
	    	ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
	    	if(rs.next()){
	    		rs.close();
	    		throw new Exception("Error [1507573943] - there are unposted AP batches - cannot change vendor account.");
	    	}else{
	    		rs.close();
	    	}
	    }catch(SQLException e){
	    	throw new Exception("Error [1507573944] - error checking for unposted AP batches with SQL: '" + SQL + "' - " + e.getMessage() + ".");
	    }
	    
	    if(bIsMerge){
	    	//If it's a merge, make sure the 'to' vendor exists:
	    	SQL = "SELECT " 
	    		+ SMTableicvendors.svendoracct 
	    		+ " FROM " + SMTableicvendors.TableName 
	    		+ " WHERE ("
	    			+ "(" + SMTableicvendors.svendoracct + " = '" + sToVendor + "')"
	    		+ ")"
	    		;
		    try{
		    	ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
		    	if(!rs.next()){
		    		rs.close();
		    		throw new Exception("Error [1507573945] - The 'MERGE INTO' vendor cannot be found.");
		    	}else{
		    		rs.close();
		    	}
		    }catch(SQLException e){
		    	throw new Exception("Error [1507573946] - error checking the 'MERGE INTO' vendor with SQL: '" + SQL + "' - " + e.getMessage());
		    }
	    	
	    	//If it's a merge, make sure there are no conflicting document numbers in the transactions file:
	    	SQL = "SELECT " 
	    		+ SMTableaptransactions.TableName + "." + SMTableaptransactions.sdocnumber 
	    		+ " FROM " + SMTableaptransactions.TableName 
	    		+ ", " + SMTableaptransactions.TableName + " AS APTRANSACTIONS_1"
	    		+ " WHERE ("
	    			+ "((" + SMTableaptransactions.TableName + "." + SMTableaptransactions.svendor 
	    				+ ")='" + sFromVendor + "')" 
	    			+ " AND ((APTRANSACTIONS_1." + SMTableaptransactions.svendor + ")='" 
	    				+ sToVendor + "')"
	    			+ " AND (" + SMTableaptransactions.TableName + "." + SMTableaptransactions.sdocnumber 
	    				+ " = APTRANSACTIONS_1." + SMTableaptransactions.sdocnumber + ")"
	    			+ ")"
	    			;
	    	boolean bDocumentConflict = false;
	    	String sConflictingDocuments = "";
		    try{
		    	ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
		    	while(rs.next()){
		    		bDocumentConflict = true;
		    		sConflictingDocuments = sConflictingDocuments + rs.getString(SMTableaptransactions.sdocnumber) + ", ";
		    	}
		    	rs.close();
		    }catch(SQLException e){
		    	throw new Exception("Error [1507573947] checking for conflicting transaction documents with SQL: '" + SQL + " - " + e.getMessage());
		    }
		    
		    //Now check the matching lines for the same thing:
		    //First check for apmatchinglines which would have the same 'apply-FROM' document:
	    	SQL = "SELECT " 
	    		+ SMTableapmatchinglines.TableName + "." + SMTableapmatchinglines.sappliedfromdocnumber
	    		+ " FROM " + SMTableapmatchinglines.TableName 
	    		+ ", " + SMTableapmatchinglines.TableName + " AS APMATCHINGLINE_1"
	    		+ " WHERE ("
	    			+ "((" + SMTableapmatchinglines.TableName + "." + SMTableapmatchinglines.svendor 
	    				+ ")='" + sFromVendor + "')" 
	    			+ " AND ((APMATCHINGLINE_1." + SMTableapmatchinglines.svendor + ")='" 
	    				+ sToVendor + "')"
	    			+ " AND (" + SMTableapmatchinglines.TableName + "." + SMTableapmatchinglines.sappliedfromdocnumber 
	    				+ " = APMATCHINGLINE_1." + SMTableapmatchinglines.sappliedfromdocnumber + ")"
	    			+ ")"
	    			;
		    try{
		    	ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
		    	while(rs.next()){
		    		bDocumentConflict = true;
		    		sConflictingDocuments = sConflictingDocuments + rs.getString(SMTableapmatchinglines.sappliedfromdocnumber) + ", ";
		    	}
		    	rs.close();
		    }catch(SQLException e){
		    	throw new Exception("Error [1507573948] checking for conflicting AP matching lines with identical applied-FROM document numbers with SQL: '" + SQL + " - " + e.getMessage());
		    }
		    
		    //Now check for apmatchinglines which would have the same 'apply-TO' document:
	    	SQL = "SELECT " 
	    		+ SMTableapmatchinglines.TableName + "." + SMTableapmatchinglines.sappliedtodocnumber
	    		+ " FROM " + SMTableapmatchinglines.TableName 
	    		+ ", " + SMTableapmatchinglines.TableName + " AS APMATCHINGLINE_1"
	    		+ " WHERE ("
	    			+ "((" + SMTableapmatchinglines.TableName + "." + SMTableapmatchinglines.svendor 
	    				+ ")='" + sFromVendor + "')" 
	    			+ " AND ((APMATCHINGLINE_1." + SMTableapmatchinglines.svendor + ")='" 
	    				+ sToVendor + "')"
	    			+ " AND (" + SMTableapmatchinglines.TableName + "." + SMTableapmatchinglines.sappliedtodocnumber 
	    				+ " = APMATCHINGLINE_1." + SMTableapmatchinglines.sappliedtodocnumber + ")"
	    			+ ")"
	    			;
		    try{
		    	ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
		    	while(rs.next()){
		    		bDocumentConflict = true;
		    		sConflictingDocuments = sConflictingDocuments + rs.getString(SMTableapmatchinglines.sappliedtodocnumber) + ", ";
		    	}
		    	rs.close();
		    }catch(SQLException e){
		    	throw new Exception("Error [1507573949] checking for conflicting AP matching lines with identical applied-TO document numbers with SQL: '" + SQL + " - " + e.getMessage());
		    }
		    
		    if(bDocumentConflict){
		    	throw new Exception("Vendor '" + sFromVendor + "' and vendor '" + sToVendor 
		    		+ "' have the following document numbers in common: " + sConflictingDocuments 
		    		+ " - vendor account cannot be merged.");
		    }

		    //Check for duplicate remit-to locations:
	    	boolean bRemitToConflict = false;
	    	String sConflictingRemitToCodes = "";
	    	SQL = "SELECT " 
	    		+ SMTableapvendorremittolocations.TableName + "." + SMTableapvendorremittolocations.sremittocode
	    		+ " FROM " + SMTableapvendorremittolocations.TableName 
	    		+ ", " + SMTableapvendorremittolocations.TableName + " AS " + SMTableapvendorremittolocations.TableName + "_1"
	    		+ " WHERE ("
	    			+ "((" + SMTableapvendorremittolocations.TableName + "." + SMTableapvendorremittolocations.svendoracct 
	    				+ ")='" + sFromVendor + "')" 
	    			+ " AND ((" + SMTableapvendorremittolocations.TableName + "_1." + SMTableapvendorremittolocations.svendoracct + ")='" 
	    				+ sToVendor + "')"
	    			+ " AND (" + SMTableapvendorremittolocations.TableName + "." + SMTableapvendorremittolocations.sremittocode 
	    				+ " = " + SMTableapvendorremittolocations.TableName + "_1." + SMTableapvendorremittolocations.sremittocode + ")"
	    			+ ")"
	    			;
		    try{
		    	ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
		    	while(rs.next()){
		    		bRemitToConflict = true;
		    		sConflictingRemitToCodes = sConflictingRemitToCodes + rs.getString(SMTableapvendorremittolocations.sremittocode) + ", ";
		    	}
		    	rs.close();
		    }catch(SQLException e){
		    	throw new Exception("Error [1507573950] checking for conflicting remit-to codes with SQL: '" + SQL + " - " + e.getMessage());
		    }
		    if(bRemitToConflict){
		    	throw new Exception("Vendor '" + sFromVendor + "' and vendor '" + sToVendor 
		    		+ "' have the following remit-to codes in common: " + sConflictingRemitToCodes 
		    		+ " - vendor accounts cannot be merged.");
		    }
		    
		    /* TJR - 1/25/2019 - removed this in case the two vendors really DID have a record for the same itemnumber - the function shouldn't choke over that:
		    //Check that there will be no conflicts in IC vendor's items:
	    	boolean bICVendorItemsConflict = false;
	    	String sConflictingVendorItems = "";
	    	SQL = "SELECT " 
	    		+ SMTableicvendoritems.TableName + "." + SMTableicvendoritems.sVendorItemNumber
	    		+ " FROM " + SMTableicvendoritems.TableName 
	    		+ ", " + SMTableicvendoritems.TableName + " AS " + SMTableicvendoritems.TableName + "_1"
	    		+ " WHERE ("
	    			+ "((" + SMTableicvendoritems.TableName + "." + SMTableicvendoritems.sVendor
	    				+ ")='" + sFromVendor + "')" 
	    			+ " AND ((" + SMTableicvendoritems.TableName + "_1." + SMTableicvendoritems.sVendor + ")='" 
	    				+ sToVendor + "')"
	    			+ " AND (" + SMTableicvendoritems.TableName + "." + SMTableicvendoritems.sVendorItemNumber 
	    				+ " = " + SMTableicvendoritems.TableName + "_1." + SMTableicvendoritems.sVendorItemNumber + ")"
	    			+ " AND (" + SMTableicvendoritems.TableName + "." + SMTableicvendoritems.sVendorItemNumber + " != '')"
	    			+ ")"
	    			;
	    	//System.out.println("[1546022695] - SQL = '" + SQL + "'");
	    	int iConflictCounter = 0;
		    try{
		    	ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
		    	while(rs.next()){
		    		iConflictCounter++;
		    		bICVendorItemsConflict = true;
		    		sConflictingVendorItems = sConflictingVendorItems + rs.getString(SMTableicvendoritems.sVendorItemNumber) + ", ";
		    	}
		    	rs.close();
		    }catch(SQLException e){
		    	throw new Exception("Error [1507573951] checking for conflicting vendor items with SQL: '" + SQL + " - " + e.getMessage());
		    }
		    x
		    if(bICVendorItemsConflict){
		    	throw new Exception("Vendor '" + sFromVendor + "' and vendor '" + sToVendor 
		    		+ " have the following " + Integer.toString(iConflictCounter) + " vendor items in common: " + sConflictingVendorItems 
		    		+ " - vendor account cannot be merged."
		    	);
		    }
		    */
	    }else{  //If this is a CHANGE request:
	    	//Make sure there is NO 'change to' vendor already:
	    	SQL = "SELECT " 
	    		+ SMTableicvendors.svendoracct
	    		+ " FROM " + SMTableicvendors.TableName 
	    		+ " WHERE ("
	    			+ "(" + SMTableicvendors.svendoracct + " = '" + sToVendor + "')"
	    		+ ")"
	    		;
		    try{
		    	ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
		    	if(rs.next()){
		    		rs.close();
		    		throw new Exception("The vendor '" + sToVendor + "' already exists, you can't change to this account number.");
		    	}else{
		    		rs.close();
		    	}
		    }catch(SQLException e){
		    	throw new Exception("Error [1507573952] checking for existing vendors with SQL: '" + SQL + " - " + e.getMessage());
		    }
	    	
			//Make sure there are no purchase orders for this vendor number already - even if there are no 
			//AP records, there COULD be a purchase order from some old vendor:
		    SQL = "SELECT " + SMTableicpoheaders.lid + " FROM " + SMTableicpoheaders.TableName
	    		+ " WHERE ("
	    			+ "(" + SMTableicpoheaders.svendor + " = '" + sToVendor + "')"
	    			+ ")"
	    			;
		    try{
		    	ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
		    	if(rs.next()){
		    		rs.close();
		    		throw new Exception("There were Purchase Orders found with the 'change to' vendor account - cannot change vendor account.");
		    	}else{
		    		rs.close();
		    	}
		    }catch(SQLException e){
		    	throw new Exception("Error [1507573953] checking for existing purchase orders with SQL: '" + SQL + " - " + e.getMessage());
		    }
		    
			//Next, make sure there are no transactions for this vendor number already - even if there are no 
			//AP records, there COULD be a transaction from some old vendor:
		    SQL = "SELECT " + SMTableaptransactions.lid + " FROM " + SMTableaptransactions.TableName
	    		+ " WHERE ("
	    			+ "(" + SMTableaptransactions.svendor + " = '" + sToVendor + "')"
	    			+ ")"
	    			;
		    try{
		    	ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
		    	if(rs.next()){
		    		rs.close();
		    		throw new Exception("AP transactions were found with the 'change to' vendor - cannot change vendor account.");
		    	}else{
		    		rs.close();
		    	}
		    }catch(SQLException e){
		    	throw new Exception("Error [1507573954] checking for existing AP transactions with SQL: '" + SQL + " - " + e.getMessage());
		    }
		    
			//Next, make sure there are no AP batch entry records for this vendor number already - even if there are no 
			//AP records, there COULD be a batch entry record from some old vendor:
		    SQL = "SELECT " + SMTableapbatchentries.lid + " FROM " + SMTableapbatchentries.TableName
	    		+ " WHERE ("
	    			+ "(" + SMTableapbatchentries.svendoracct + " = '" + sToVendor + "')"
	    			+ ")"
	    			;
		    try{
		    	ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
		    	if(rs.next()){
		    		rs.close();
		    		throw new Exception("AP batch entries were found with the 'change to' vendor - cannot change vendor account.");
		    	}else{
		    		rs.close();
		    	}
		    }catch(SQLException e){
		    	throw new Exception("Error [1507573955] checking for existing AP batch entries with SQL: '" + SQL + " - " + e.getMessage());
		    }
		    
			//Next, make sure there are no AP check records for this vendor number already - even if there are no 
			//AP records, there COULD be a check record from some old vendor:
		    SQL = "SELECT " + SMTableapchecks.lid + " FROM " + SMTableapchecks.TableName
	    		+ " WHERE ("
	    			+ "(" + SMTableapchecks.svendoracct + " = '" + sToVendor + "')"
	    			+ ")"
	    			;
		    try{
		    	ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
		    	if(rs.next()){
		    		rs.close();
		    		throw new Exception("AP checks were found with the 'change to' vendor - cannot change vendor account.");
		    	}else{
		    		rs.close();
		    	}
		    }catch(SQLException e){
		    	throw new Exception("Error [1507573956] checking for existing AP checks with SQL: '" + SQL + " - " + e.getMessage());
		    }
		    
			//Next, make sure there are no remit to location records for this vendor number already - even if there are no 
			//AP records, there COULD be a remit to location record from some old vendor:
		    SQL = "SELECT " + SMTableapvendorremittolocations.sremittocode + " FROM " + SMTableapvendorremittolocations.TableName
	    		+ " WHERE ("
	    			+ "(" + SMTableapvendorremittolocations.svendoracct + " = '" + sToVendor + "')"
	    			+ ")"
	    			;
		    try{
		    	ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
		    	if(rs.next()){
		    		rs.close();
		    		throw new Exception("AP remit-to locations were found with the 'change to' vendor - cannot change vendor account.");
		    	}else{
		    		rs.close();
		    	}
		    }catch(SQLException e){
		    	throw new Exception("Error [1507573957] checking for existing remit-to locations with SQL: '" + SQL + " - " + e.getMessage());
		    }
		    
			//Next, make sure there are no statistics records for this vendor number already - even if there are no 
			//AP records, there COULD be a statistics record from some old vendor:
		    SQL = "SELECT " + SMTableapvendorstatistics.svendoracct + " FROM " + SMTableapvendorstatistics.TableName
	    		+ " WHERE ("
	    			+ "(" + SMTableapvendorstatistics.svendoracct + " = '" + sToVendor + "')"
	    			+ ")"
	    			;
		    try{
		    	ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
		    	if(rs.next()){
		    		rs.close();
		    		throw new Exception("AP vendor statistics were found with the 'change to' vendor - cannot change vendor account.");
		    	}else{
		    		rs.close();
		    	}
		    }catch(SQLException e){
		    	throw new Exception("Error [1507573958] checking for existing statistics with SQL: '" + SQL + " - " + e.getMessage());
		    }
		    
			//Next, make sure there are no PO invoice header records for this vendor number already - even if there are no 
			//AP records, there COULD be a PO invoice header record from some old vendor:
		    SQL = "SELECT " + SMTableicpoinvoiceheaders.svendor + " FROM " + SMTableicpoinvoiceheaders.TableName
	    		+ " WHERE ("
	    			+ "(" + SMTableicpoinvoiceheaders.svendor + " = '" + sToVendor + "')"
	    			+ ")"
	    			;
		    try{
		    	ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
		    	if(rs.next()){
		    		rs.close();
		    		throw new Exception("PO invoice headers were found with the 'change to' vendor - cannot change vendor account.");
		    	}else{
		    		rs.close();
		    	}
		    }catch(SQLException e){
		    	throw new Exception("Error [1507573959] checking for existing PO invoice headers with SQL: '" + SQL + " - " + e.getMessage());
		    }
		    
			//Next, make sure there are no vendor item records for this vendor number already - even if there are no 
			//AP records, there COULD be a vendor item record from some old vendor:
		    SQL = "SELECT " + SMTableicvendoritems.sVendorItemNumber + " FROM " + SMTableicvendoritems.TableName
	    		+ " WHERE ("
	    			+ "(" + SMTableicvendoritems.sVendor + " = '" + sToVendor + "')"
	    			+ ")"
	    			;
		    try{
		    	ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
		    	if(rs.next()){
		    		rs.close();
		    		throw new Exception("Vendor items were found with the 'change to' vendor - cannot change vendor account.");
		    	}else{
		    		rs.close();
		    	}
		    }catch(SQLException e){
		    	throw new Exception("Error [1507573960] checking for existing IC vendor items with SQL: '" + SQL + " - " + e.getMessage());
		    }
		    
			//Next, make sure there are no labor backcharge records for this vendor number already - even if there are no 
			//AP records, there COULD be a labor backcharge record from some old vendor:
		    SQL = "SELECT " + SMTablelaborbackcharges.lid + " FROM " + SMTablelaborbackcharges.TableName
	    		+ " WHERE ("
	    			+ "(" + SMTablelaborbackcharges.svendoracct + " = '" + sToVendor + "')"
	    			+ ")"
	    			;
		    try{
		    	ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
		    	if(rs.next()){
		    		rs.close();
		    		throw new Exception("Labor backcharges were found with the 'change to' vendor - cannot change vendor account.");
		    	}else{
		    		rs.close();
		    	}
		    }catch(SQLException e){
		    	throw new Exception("Error [1507573961] checking for labor backcharges with SQL: '" + SQL + " - " + e.getMessage());
		    }
	    }
	    
	    //If everything passes up to this point, then we have to set the 'posting flag' in AP to prevent
	    //anyone from trying to post while this is happening:
    	//First check to make sure no one else is posting:
	    APOptions apoptions = new APOptions();
	    if(!apoptions.load(conn)){
	    	throw new Exception("Error [1507573962] loading AP options - " + apoptions.getErrorMessageString());
	    }
	    
	    if (apoptions.getBatchPostingInProcess().compareToIgnoreCase("1") == 0){
	    	throw new Exception(
	    		"Error [1507573963] - a previous posting is not completed - "
	    			+ " user '" + SMUtilities.getFullNamebyUserID(apoptions.getPostingUserID(), conn)
	    			+ " has been " + apoptions.getPostingProcess()
	    			+ " since " + apoptions.getPostingStartDate() + "."
	    			+ "  Vendor number change can't proceed."
	    	
	    	);
	    }
	    
	    //Go ahead and set the posting flag:
	    try {
			apoptions.savePostingProcessState(conn, "1", VENDOR_NUMBER_CHANGE, sUserID);
		} catch (Exception e1) {
    		throw new Exception("Error [1507573964] setting the AP posting flag - " + e1.getMessage());
		}
	    
    	try {
			updateVendors(conn, sFromVendor, sToVendor, bIsMerge);
		} catch (Exception e2) {
    		//Clear the posting flag:
    	    try {
    			apoptions.savePostingProcessState(conn, "0", "", "0");
    		} catch (Exception e1) {
        		//Can't return anything here, we'll return the error from the 'Update vendors' function below);
    		}
    	    throw new Exception("Error [1507573966] - " + e2.getMessage());
		}

    	//Clear the posting flag:
	    try {
			apoptions.savePostingProcessState(conn, "0", "", "0");
		} catch (Exception e1) {
			throw new Exception("Error [1507573967] clearing posting flag - " + e1.getMessage());
		}
	}
	private void updateVendors(Connection conn, String sFromVendor, String sToVendor, boolean bIsMerge) throws Exception{

		if(!clsDatabaseFunctions.start_data_transaction(conn)){
			throw new Exception("Error [1507582265] starting data transaction.");
		}
		
		if (bIsMerge){
			try {
				mergeVendor(conn, sFromVendor, sToVendor);
			} catch (Exception e) {
				clsDatabaseFunctions.rollback_data_transaction(conn);
				throw new Exception("Error [1507582266] merging vendors - " + e.getMessage());
			}
		}else{
			try {
				changeVendor(conn, sFromVendor, sToVendor);
			} catch (Exception e) {
				clsDatabaseFunctions.rollback_data_transaction(conn);
				throw new Exception("Error [1507582267] changing vendors - " + e.getMessage());
			}
		}
		
		if (!clsDatabaseFunctions.commit_data_transaction(conn)){
			throw new Exception("Error [1507582268] committing data transaction.");
		}
		
	}
	private void mergeVendor(Connection conn, String sFromVendor, String sToVendor) throws Exception{
		
		/*
		** - The MERGE FROM vendor master will be deleted.
		** - All current and historical AP batch entries will have the MERGE FROM vendor account and name changed to the MERGE TO vendor.
		** - All current and historical AP checks will have the MERGE FROM vendor account and name changed to the MERGE TO vendor.
		** - All current and historical AP 'matching lines' (apply-to-details) will have the MERGE FROM vendor account changed to the MERGE TO vendor.
		** - All current and historical AP transactions will have the MERGE FROM vendor account changed to the MERGE TO vendor.
		** - All the remit to locations of the MERGE FROM vendor will be added to the remit to locations of the MERGE TO vendor.
		** - All the vendor statistics of the MERGE FROM vendor will be added to (or combined with) the statistics of the MERGE TO vendor.
		** - All current and historical Purchase Orders for the MERGE FROM vendor will have the vendor account and name changed to the MERGE TO vendor.
		** - All current and historical Purchase Order Invoices for the MERGE FROM vendor will have the vendor account and name changed to the MERGE TO vendor.
		** - All Vendor Items (used in the inventory system to record vendor item numbers) for the MERGE FROM vendor will be added to the items in the MERGE TO vendor's list of items,
		**        but if there is a duplicate, it will be ignored.
		** - All current and historical Labor Backcharges for the MERGE FROM vendor will be changed to the MERGE TO vendor.
		*/
		
		String SQL = "";

		// ** - The MERGE FROM vendor master will be deleted.
		SQL = "DELETE FROM " + SMTableicvendors.TableName
		+ " WHERE ("
			+ "(" + SMTableicvendors.svendoracct + " = '" + sFromVendor + "')"
		+ ")"
		;
		try {
			Statement stmt = conn.createStatement();
			stmt.execute(SQL);
		} catch (Exception e) {
			throw new Exception("Error [1507582871] deleting vendor '" + sFromVendor + "' with SQL: '" + SQL + "' - " + e.getMessage());
		}
    	
		//Get the 'TO' vendor's name:
		APVendor vendor = new APVendor();
		vendor.setsvendoracct(sToVendor);
		try {
			vendor.load(conn);
		} catch (Exception e1) {
			throw new Exception("Error [1507585794] reading vendor name - " + vendor.getErrorMessages());
		}
		
    	// ** - All current and historical AP batch entries will have the MERGE FROM vendor account and name changed to the MERGE TO vendor.
		SQL = "UPDATE " + SMTableapbatchentries.TableName
		+ " SET " + SMTableapbatchentries.svendoracct + " = '" + sToVendor + "'"
		+ ", " + SMTableapbatchentries.svendorname + " = '" + vendor.getsname() + "'"
		+ " WHERE ("
			+ "(" + SMTableapbatchentries.svendoracct + " = '" + sFromVendor + "')"
		+ ")"
		;
		try {
			Statement stmt = conn.createStatement();
			stmt.execute(SQL);
		} catch (Exception e) {
			throw new Exception("Error [1507582872] updating vendors in AP batch entries with SQL: '" + SQL + "' - " + e.getMessage());
		}

		//** - All current and historical AP checks will have the MERGE FROM vendor account and name changed to the MERGE TO vendor.
		SQL = "UPDATE " + SMTableapchecks.TableName
		+ " SET " + SMTableapchecks.svendoracct + " = '" + sToVendor + "'"
		+ ", " + SMTableapchecks.svendorname + " = '" + vendor.getsname() + "'"
		+ " WHERE ("
			+ "(" + SMTableapchecks.svendoracct + " = '" + sFromVendor + "')"
		+ ")"
		;
		try {
			Statement stmt = conn.createStatement();
			stmt.execute(SQL);
		} catch (Exception e) {
			throw new Exception("Error [1507582873] updating vendors in AP checks with SQL: '" + SQL + "' - " + e.getMessage());
		}
		
		// ** - All current and historical AP 'matching lines' (apply-to-details) will have the MERGE FROM vendor account changed to the MERGE TO vendor.
		SQL = "UPDATE " + SMTableapmatchinglines.TableName
		+ " SET " + SMTableapmatchinglines.svendor + " = '" + sToVendor + "'"
		+ " WHERE ("
			+ "(" + SMTableapmatchinglines.svendor + " = '" + sFromVendor + "')"
		+ ")"
		;
		try {
			Statement stmt = conn.createStatement();
			stmt.execute(SQL);
		} catch (Exception e) {
			throw new Exception("Error [1507582874] updating vendors in AP matching lines with SQL: '" + SQL + "' - " + e.getMessage());
		}
		
		// ** - All current and historical AP transactions will have the MERGE FROM vendor account changed to the MERGE TO vendor.
		SQL = "UPDATE " + SMTableaptransactions.TableName
		+ " SET " + SMTableaptransactions.svendor + " = '" + sToVendor + "'"
		+ " WHERE ("
			+ "(" + SMTableaptransactions.svendor + " = '" + sFromVendor + "')"
		+ ")"
		;
		try {
			Statement stmt = conn.createStatement();
			stmt.execute(SQL);
		} catch (Exception e) {
			throw new Exception("Error [1507582875] updating vendors in AP transactions with SQL: '" + SQL + "' - " + e.getMessage());
		}
		
		// ** - All the remit to locations of the MERGE FROM vendor will be added to the remit to locations of the MERGE TO vendor.
		SQL = "UPDATE " + SMTableapvendorremittolocations.TableName
		+ " SET " + SMTableapvendorremittolocations.svendoracct + " = '" + sToVendor + "'"
		+ " WHERE ("
			+ "(" + SMTableapvendorremittolocations.svendoracct + " = '" + sFromVendor + "')"
		+ ")"
		;
		try {
			Statement stmt = conn.createStatement();
			stmt.execute(SQL);
		} catch (Exception e) {
			throw new Exception("Error [1507582876] updating vendors in AP remit-to locations with SQL: '" + SQL + "' - " + e.getMessage());
		}
		
		// ** - All the vendor statistics of the MERGE FROM vendor will be added to (or combined with) the statistics of the MERGE TO vendor.
		SQL = "UPDATE " + SMTableapvendorstatistics.TableName + " AS " + "TOVENDOR" + SMTableapvendorstatistics.TableName
		+ ", " + SMTableapvendorstatistics.TableName + " AS " + "FROMVENDOR" + SMTableapvendorstatistics.TableName + "\n"
		
		+ " SET" + "\n"
			
			+ " TOVENDOR" + SMTableapvendorstatistics.TableName + "." + SMTableapvendorstatistics.bdamountofadjustments + " = " 
			+ "TOVENDOR" + SMTableapvendorstatistics.TableName + "." + SMTableapvendorstatistics.bdamountofadjustments
			+ " + " + "FROMVENDOR" + SMTableapvendorstatistics.TableName + "." + SMTableapvendorstatistics.bdamountofadjustments
			+ "\n"
			
			+ ", TOVENDOR" + SMTableapvendorstatistics.TableName + "." + SMTableapvendorstatistics.bdamountofcreditnotes + " = " 
			+ "TOVENDOR" + SMTableapvendorstatistics.TableName + "." + SMTableapvendorstatistics.bdamountofcreditnotes
			+ " + " + "FROMVENDOR" + SMTableapvendorstatistics.TableName + "." + SMTableapvendorstatistics.bdamountofcreditnotes
			 + "\n"
			
			+ ", TOVENDOR" + SMTableapvendorstatistics.TableName + "." + SMTableapvendorstatistics.bdamountofdebitnotes + " = " 
			+ "TOVENDOR" + SMTableapvendorstatistics.TableName + "." + SMTableapvendorstatistics.bdamountofdebitnotes
			+ " + " + "FROMVENDOR" + SMTableapvendorstatistics.TableName + "." + SMTableapvendorstatistics.bdamountofdebitnotes
			+ "\n"
			
			+ ", TOVENDOR" + SMTableapvendorstatistics.TableName + "." + SMTableapvendorstatistics.bdamountofdiscounts + " = " 
			+ "TOVENDOR" + SMTableapvendorstatistics.TableName + "." + SMTableapvendorstatistics.bdamountofdiscounts
			+ " + " + "FROMVENDOR" + SMTableapvendorstatistics.TableName + "." + SMTableapvendorstatistics.bdamountofdiscounts
			+ "\n"
			 
			+ ", TOVENDOR" + SMTableapvendorstatistics.TableName + "." + SMTableapvendorstatistics.bdamountofdiscountslost + " = " 
			+ "TOVENDOR" + SMTableapvendorstatistics.TableName + "." + SMTableapvendorstatistics.bdamountofdiscountslost
			+ " + " + "FROMVENDOR" + SMTableapvendorstatistics.TableName + "." + SMTableapvendorstatistics.bdamountofdiscountslost
			+ "\n"
			
			+ ", TOVENDOR" + SMTableapvendorstatistics.TableName + "." + SMTableapvendorstatistics.bdamountofinvoices + " = " 
			+ "TOVENDOR" + SMTableapvendorstatistics.TableName + "." + SMTableapvendorstatistics.bdamountofinvoices
			+ " + " + "FROMVENDOR" + SMTableapvendorstatistics.TableName + "." + SMTableapvendorstatistics.bdamountofinvoices
			+ "\n"
			
			+ ", TOVENDOR" + SMTableapvendorstatistics.TableName + "." + SMTableapvendorstatistics.bdamountofpayments + " = " 
			+ "TOVENDOR" + SMTableapvendorstatistics.TableName + "." + SMTableapvendorstatistics.bdamountofpayments
			+ " + " + "FROMVENDOR" + SMTableapvendorstatistics.TableName + "." + SMTableapvendorstatistics.bdamountofpayments
			+ "\n"
			
			+ ", TOVENDOR" + SMTableapvendorstatistics.TableName + "." + SMTableapvendorstatistics.lnumberofadjustments + " = " 
			+ "TOVENDOR" + SMTableapvendorstatistics.TableName + "." + SMTableapvendorstatistics.lnumberofadjustments
			+ " + " + "FROMVENDOR" + SMTableapvendorstatistics.TableName + "." + SMTableapvendorstatistics.lnumberofadjustments
			+ "\n"
			
			+ ", TOVENDOR" + SMTableapvendorstatistics.TableName + "." + SMTableapvendorstatistics.lnumberofcredits + " = " 
			+ "TOVENDOR" + SMTableapvendorstatistics.TableName + "." + SMTableapvendorstatistics.lnumberofcredits
			+ " + " + "FROMVENDOR" + SMTableapvendorstatistics.TableName + "." + SMTableapvendorstatistics.lnumberofcredits
			+ "\n"
			
			+ ", TOVENDOR" + SMTableapvendorstatistics.TableName + "." + SMTableapvendorstatistics.lnumberofdaystopay + " = " 
			+ "TOVENDOR" + SMTableapvendorstatistics.TableName + "." + SMTableapvendorstatistics.lnumberofdaystopay
			+ " + " + "FROMVENDOR" + SMTableapvendorstatistics.TableName + "." + SMTableapvendorstatistics.lnumberofdaystopay
			+ "\n"
			
			+ ", TOVENDOR" + SMTableapvendorstatistics.TableName + "." + SMTableapvendorstatistics.lnumberofdebits + " = " 
			+ "TOVENDOR" + SMTableapvendorstatistics.TableName + "." + SMTableapvendorstatistics.lnumberofdebits
			+ " + " + "FROMVENDOR" + SMTableapvendorstatistics.TableName + "." + SMTableapvendorstatistics.lnumberofdebits
			+ "\n"
			
			+ ", TOVENDOR" + SMTableapvendorstatistics.TableName + "." + SMTableapvendorstatistics.lnumberofdiscountslost + " = " 
			+ "TOVENDOR" + SMTableapvendorstatistics.TableName + "." + SMTableapvendorstatistics.lnumberofdiscountslost
			+ " + " + "FROMVENDOR" + SMTableapvendorstatistics.TableName + "." + SMTableapvendorstatistics.lnumberofdiscountslost
			+ "\n"
			
			+ ", TOVENDOR" + SMTableapvendorstatistics.TableName + "." + SMTableapvendorstatistics.lnumberofdiscountstaken + " = " 
			+ "TOVENDOR" + SMTableapvendorstatistics.TableName + "." + SMTableapvendorstatistics.lnumberofdiscountstaken
			+ " + " + "FROMVENDOR" + SMTableapvendorstatistics.TableName + "." + SMTableapvendorstatistics.lnumberofdiscountstaken
			+ "\n"
			
			+ ", TOVENDOR" + SMTableapvendorstatistics.TableName + "." + SMTableapvendorstatistics.lnumberofinvoices + " = " 
			+ "TOVENDOR" + SMTableapvendorstatistics.TableName + "." + SMTableapvendorstatistics.lnumberofinvoices
			+ " + " + "FROMVENDOR" + SMTableapvendorstatistics.TableName + "." + SMTableapvendorstatistics.lnumberofinvoices
			+ "\n"
			
			+ ", TOVENDOR" + SMTableapvendorstatistics.TableName + "." + SMTableapvendorstatistics.lnumberofinvoicespaid + " = " 
			+ "TOVENDOR" + SMTableapvendorstatistics.TableName + "." + SMTableapvendorstatistics.lnumberofinvoicespaid
			+ " + " + "FROMVENDOR" + SMTableapvendorstatistics.TableName + "." + SMTableapvendorstatistics.lnumberofinvoicespaid
			+ "\n"
			
			//+ ", TOVENDOR" + SMTableapvendorstatistics.TableName + "." + SMTableapvendorstatistics.lnumberofpayapplicationsusedforaveraging + " = " 
			//+ "TOVENDOR" + SMTableapvendorstatistics.TableName + "." + SMTableapvendorstatistics.lnumberofpayapplicationsusedforaveraging
			//+ " + " + "FROMVENDOR" + SMTableapvendorstatistics.TableName + "." + SMTableapvendorstatistics.lnumberofpayapplicationsusedforaveraging
			//+ "\n"
			
			+ ", TOVENDOR" + SMTableapvendorstatistics.TableName + "." + SMTableapvendorstatistics.lnumberofpayments + " = " 
			+ "TOVENDOR" + SMTableapvendorstatistics.TableName + "." + SMTableapvendorstatistics.lnumberofpayments
			+ " + " + "FROMVENDOR" + SMTableapvendorstatistics.TableName + "." + SMTableapvendorstatistics.lnumberofpayments
			+ "\n"
			
		+ " WHERE ("+ "\n"
			+ "(" + "FROMVENDOR" + SMTableapvendorstatistics.TableName + "." + SMTableapvendorstatistics.svendoracct + " = '" + sFromVendor + "')"+ "\n"
			+ " AND (TOVENDOR" + SMTableapvendorstatistics.TableName + "." + SMTableapvendorstatistics.svendoracct + " = '" + sToVendor + "')"+ "\n"
			
			//Same month:
			+ " AND ("
				+ "TOVENDOR" + SMTableapvendorstatistics.TableName + "." + SMTableapvendorstatistics.lmonth 
				+ " = FROMVENDOR" + SMTableapvendorstatistics.TableName + "." + SMTableapvendorstatistics.lmonth 
			+ ")"+ "\n"
			
			//Same year:
			+ " AND ("
				+ "TOVENDOR" + SMTableapvendorstatistics.TableName + "." + SMTableapvendorstatistics.lyear 
				+ " = FROMVENDOR" + SMTableapvendorstatistics.TableName + "." + SMTableapvendorstatistics.lyear 
			+ ")"+ "\n"
			
			//And only do it for the 'TO' vendor:
			+ " AND ("
				+ "(" + "TOVENDOR" + SMTableapvendorstatistics.TableName + "." + SMTableapvendorstatistics.svendoracct + " = '" + sToVendor + "')"
			+ ")"+ "\n"
				
		+ ")"+ "\n"
		;
		//System.out.println("[1507585004] - SQL = '" + SQL + "'");
		try {
			Statement stmt = conn.createStatement();
			stmt.execute(SQL);
		} catch (Exception e) {
			throw new Exception("Error [1507582877] updating vendor statistics with SQL: '" + SQL + "' - " + e.getMessage());
		}
		
		//Finally, remove the statistics for the 'FROM' vendor:
		SQL = "DELETE FROM " + SMTableapvendorstatistics.TableName
		+ " WHERE ("
			+ "(" + SMTableapvendorstatistics.svendoracct + " = '" + sFromVendor + "')"
		+ ")"
		;
		try {
			Statement stmt = conn.createStatement();
			stmt.execute(SQL);
		} catch (Exception e) {
			throw new Exception("Error [1507582879] removing the 'FROM' vendor statistics with SQL: '" + SQL + "' - " + e.getMessage());
		}
		
		// ** - All current and historical Purchase Orders for the MERGE FROM vendor will have the vendor account and name changed to the MERGE TO vendor.
		SQL = "UPDATE " + SMTableicpoheaders.TableName
		+ " SET " + SMTableicpoheaders.svendor + " = '" + sToVendor + "'"
		+ ", " + SMTableicpoheaders.svendorname + " = '" + vendor.getsname() + "'"
		+ " WHERE ("
			+ "(" + SMTableicpoheaders.svendor + " = '" + sFromVendor + "')"
		+ ")"
		;
		try {
			Statement stmt = conn.createStatement();
			stmt.execute(SQL);
		} catch (Exception e) {
			throw new Exception("Error [1507582880] updating vendors on Purchase Orders with SQL: '" + SQL + "' - " + e.getMessage());
		}
		
		//** - All current and historical Purchase Order Invoices for the MERGE FROM vendor will have the vendor account and name changed to the MERGE TO vendor.
		SQL = "UPDATE " + SMTableicpoinvoiceheaders.TableName
		+ " SET " + SMTableicpoinvoiceheaders.svendor + " = '" + sToVendor + "'"
		+ ", " + SMTableicpoinvoiceheaders.svendorname + " = '" + vendor.getsname() + "'"
		+ " WHERE ("
			+ "(" + SMTableicpoinvoiceheaders.svendor + " = '" + sFromVendor + "')"
		+ ")"
		;
		try {
			Statement stmt = conn.createStatement();
			stmt.execute(SQL);
		} catch (Exception e) {
			throw new Exception("Error [1507582881] updating vendors on Purchase Order invoices with SQL: '" + SQL + "' - " + e.getMessage());
		}
		
		// ** - All Vendor Items (used in the inventory system to record vendor item numbers) for the MERGE FROM vendor will be added to the items in the MERGE TO vendor's list of items.
		// TJR - 1/25/2019 - modified this in case the two vendors really DID have a record for the same itemnumber - the function shouldn't choke over that:
		SQL = "INSERT IGNORE INTO " + SMTableicvendoritems.TableName + "("
			+ SMTableicvendoritems.sCost
			+ ", " + SMTableicvendoritems.sItemNumber
			+ ", " + SMTableicvendoritems.sVendor
			+ ", " + SMTableicvendoritems.sVendorItemNumber
			+ ", " + SMTableicvendoritems.sComment
		+ ") "
		+ " SELECT "
		+ SMTableicvendoritems.sCost
		+ ", " + SMTableicvendoritems.sItemNumber
		+ ", '" + sToVendor + "'"
		+ ", " + SMTableicvendoritems.sVendorItemNumber
		+ ", " + SMTableicvendoritems.sComment
		+ " FROM " + SMTableicvendoritems.TableName
		+ " WHERE ("
			+ "(" + SMTableicvendoritems.sVendor + " = '" + sFromVendor + "')"
		+ ")"
		;
		//System.out.println("[1548442709] - SQL = '" + SQL + "'.");
		
		//SQL = "UPDATE " + SMTableicvendoritems.TableName
		//+ " SET " + SMTableicvendoritems.sVendor + " = '" + sToVendor + "'"
		//+ " WHERE ("
		//	+ "(" + SMTableicvendoritems.sVendor + " = '" + sFromVendor + "')"
		//+ ")"
		;
		try {
			Statement stmt = conn.createStatement();
			stmt.execute(SQL);
		} catch (Exception e) {
			throw new Exception("Error [1507582882] inserting vendor items from vendor '" + sFromVendor + "' to vendor '" + sToVendor + "' with SQL: '" + SQL + "' - " + e.getMessage());
		}
		
		// ** - All current and historical Labor Backcharges for the MERGE FROM vendor will be changed to the MERGE TO vendor.
		SQL = "UPDATE " + SMTablelaborbackcharges.TableName
		+ " SET " + SMTablelaborbackcharges.svendoracct + " = '" + sToVendor + "'"
		+ " WHERE ("
			+ "(" + SMTablelaborbackcharges.svendoracct + " = '" + sFromVendor + "')"
		+ ")"
		;
		try {
			Statement stmt = conn.createStatement();
			stmt.execute(SQL);
		} catch (Exception e) {
			throw new Exception("Error [1507582883] updating vendors on labor backcharges with SQL: '" + SQL + "' - " + e.getMessage());
		}
	}
	private void changeVendor(Connection conn, String sFromVendor, String sToVendor) throws Exception{
		
		
		/*
		** - The vendor master will have the vendor account number changed.
		** - All current and historical AP batch entries will have the vendor account changed.
		** - All current and historical AP checks will have the vendor account changed.
		** - All current and historical AP 'matching lines' (apply-to-details) will have the vendor account changed.
		** - All current and historical AP transactions will have the vendor account changed.
		** - All vendor remit-to locations will have the vendor account changed.
		** - All current and historical vendor statistics will have the vendor account changed.
		** - All current and historical Purchase Orders will have the vendor account changed.
		** - All current and historical Purchase Order Invoices will have the vendor account changed.
		** - All Vendor Items (used in the inventory system to record vendor item numbers) will have the vendor account changed.
		** - All current and historical Labor Backcharges will have the vendor account changed.

		 */
		
		//Update vendors
		String SQL = "";		
		
	    //Start updating tables:
		// ** - The vendor master will have the vendor account number changed.
		SQL = "UPDATE " + SMTableicvendors.TableName
		+ " SET " + SMTableicvendors.svendoracct + " = '" + sToVendor + "'"
		+ " WHERE ("
			+ "(" + SMTableicvendors.svendoracct + " = '" + sFromVendor + "')"
		+ ")"
		;
		try {
			Statement stmt = conn.createStatement();
			stmt.execute(SQL);
		} catch (Exception e) {
			throw new Exception("Error [1507582893] updating vendor master with SQL: '" + SQL + "' - " + e.getMessage());
		}

		// ** - All current and historical AP batch entries will have the vendor account changed.
		SQL = "UPDATE " + SMTableapbatchentries.TableName
		+ " SET " + SMTableapbatchentries.svendoracct + " = '" + sToVendor + "'"
		+ " WHERE ("
			+ "(" + SMTableapbatchentries.svendoracct + " = '" + sFromVendor + "')"
		+ ")"
		;
		try {
			Statement stmt = conn.createStatement();
			stmt.execute(SQL);
		} catch (Exception e) {
			throw new Exception("Error [1507582894] updating AP batch entries with SQL: '" + SQL + "' - " + e.getMessage());
		}
		
		// ** - All current and historical AP checks will have the vendor account changed.
		SQL = "UPDATE " + SMTableapchecks.TableName
		+ " SET " + SMTableapchecks.svendoracct + " = '" + sToVendor + "'"
		+ " WHERE ("
			+ "(" + SMTableapchecks.svendoracct + " = '" + sFromVendor + "')"
		+ ")"
		;
		try {
			Statement stmt = conn.createStatement();
			stmt.execute(SQL);
		} catch (Exception e) {
			throw new Exception("Error [1507582895] updating AP checks with SQL: '" + SQL + "' - " + e.getMessage());
		}
		
		// ** - All current and historical AP 'matching lines' (apply-to-details) will have the vendor account changed.
		SQL = "UPDATE " + SMTableapmatchinglines.TableName
		+ " SET " + SMTableapmatchinglines.svendor + " = '" + sToVendor + "'"
		+ " WHERE ("
			+ "(" + SMTableapmatchinglines.svendor + " = '" + sFromVendor + "')"
		+ ")"
		;
		try {
			Statement stmt = conn.createStatement();
			stmt.execute(SQL);
		} catch (Exception e) {
			throw new Exception("Error [1507582896] updating AP matching lines with SQL: '" + SQL + "' - " + e.getMessage());
		}
		
		// ** - All current and historical AP transactions will have the vendor account changed.
		SQL = "UPDATE " + SMTableaptransactions.TableName
		+ " SET " + SMTableaptransactions.svendor + " = '" + sToVendor + "'"
		+ " WHERE ("
			+ "(" + SMTableaptransactions.svendor + " = '" + sFromVendor + "')"
		+ ")"
		;
		try {
			Statement stmt = conn.createStatement();
			stmt.execute(SQL);
		} catch (Exception e) {
			throw new Exception("Error [1507582897] updating AP transactions with SQL: '" + SQL + "' - " + e.getMessage());
		}
		
		// ** - All vendor remit-to locations will have the vendor account changed.
		SQL = "UPDATE " + SMTableapvendorremittolocations.TableName
		+ " SET " + SMTableapvendorremittolocations.svendoracct + " = '" + sToVendor + "'"
		+ " WHERE ("
			+ "(" + SMTableapvendorremittolocations.svendoracct + " = '" + sFromVendor + "')"
		+ ")"
		;
		try {
			Statement stmt = conn.createStatement();
			stmt.execute(SQL);
		} catch (Exception e) {
			throw new Exception("Error [1507582898] updating AP remit-to locations with SQL: '" + SQL + "' - " + e.getMessage());
		}
		
		// ** - All current and historical vendor statistics will have the vendor account changed.
		SQL = "UPDATE " + SMTableapvendorstatistics.TableName
		+ " SET " + SMTableapvendorstatistics.svendoracct + " = '" + sToVendor + "'"
		+ " WHERE ("
			+ "(" + SMTableapvendorstatistics.svendoracct + " = '" + sFromVendor + "')"
		+ ")"
		;
		try {
			Statement stmt = conn.createStatement();
			stmt.execute(SQL);
		} catch (Exception e) {
			throw new Exception("Error [1507582899] updating AP vendor statistics with SQL: '" + SQL + "' - " + e.getMessage());
		}		
		
		// ** - All current and historical Purchase Orders will have the vendor account changed.
		SQL = "UPDATE " + SMTableicpoheaders.TableName
		+ " SET " + SMTableicpoheaders.svendor + " = '" + sToVendor + "'"
		+ " WHERE ("
			+ "(" + SMTableicpoheaders.svendor + " = '" + sFromVendor + "')"
		+ ")"
		;
		try {
			Statement stmt = conn.createStatement();
			stmt.execute(SQL);
		} catch (Exception e) {
			throw new Exception("Error [1507582900] updating Purchase Orders with SQL: '" + SQL + "' - " + e.getMessage());
		}		
		
		// ** - All current and historical Purchase Order Invoices will have the vendor account changed.
		SQL = "UPDATE " + SMTableicpoinvoiceheaders.TableName
		+ " SET " + SMTableicpoinvoiceheaders.svendor + " = '" + sToVendor + "'"
		+ " WHERE ("
			+ "(" + SMTableicpoinvoiceheaders.svendor + " = '" + sFromVendor + "')"
		+ ")"
		;
		try {
			Statement stmt = conn.createStatement();
			stmt.execute(SQL);
		} catch (Exception e) {
			throw new Exception("Error [1507582901] updating PO invoice headers with SQL: '" + SQL + "' - " + e.getMessage());
		}		
		
		// ** - All Vendor Items (used in the inventory system to record vendor item numbers) will have the vendor account changed.
		SQL = "UPDATE " + SMTableicvendoritems.TableName
		+ " SET " + SMTableicvendoritems.sVendor + " = '" + sToVendor + "'"
		+ " WHERE ("
			+ "(" + SMTableicvendoritems.sVendor + " = '" + sFromVendor + "')"
		+ ")"
		;
		try {
			Statement stmt = conn.createStatement();
			stmt.execute(SQL);
		} catch (Exception e) {
			throw new Exception("Error [1507582902] updating vendor items with SQL: '" + SQL + "' - " + e.getMessage());
		}		
		
		// ** - All current and historical Labor Backcharges will have the vendor account changed.
		SQL = "UPDATE " + SMTablelaborbackcharges.TableName
		+ " SET " + SMTablelaborbackcharges.svendoracct + " = '" + sToVendor + "'"
		+ " WHERE ("
			+ "(" + SMTablelaborbackcharges.svendoracct + " = '" + sFromVendor + "')"
		+ ")"
		;
		try {
			Statement stmt = conn.createStatement();
			stmt.execute(SQL);
		} catch (Exception e) {
			throw new Exception("Error [1507582903] updating labor backcharges with SQL: '" + SQL + "' - " + e.getMessage());
		}		
		
		return;
	}
}
