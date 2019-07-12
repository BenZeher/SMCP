package smar;

import SMClasses.SMBatchStatuses;
import SMClasses.SMEntryBatch;
import SMDataDefinition.SMTablearchronlog;
import SMDataDefinition.SMTablearcustomer;
import SMDataDefinition.SMTablearcustomershiptos;
import SMDataDefinition.SMTablearcustomerstatistics;
import SMDataDefinition.SMTablearmatchingline;
import SMDataDefinition.SMTablearmonthlystatistics;
import SMDataDefinition.SMTablearoptions;
import SMDataDefinition.SMTableartransactions;
import SMDataDefinition.SMTablecallsheets;
import SMDataDefinition.SMTabledefaultsalesgroupsalesperson;
import SMDataDefinition.SMTableentries;
import SMDataDefinition.SMTableinvoiceheaders;
import SMDataDefinition.SMTableorderheaders;
import SMDataDefinition.SMTablesalescontacts;
import SMDataDefinition.SMTablesitelocations;
import SMDataDefinition.SMTablessorderheaders;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsDateAndTimeConversions;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class ARCustomerNumberChange extends java.lang.Object{

	/*
	 * These are the tables that we have to be concerned about when we change or merge customer numbers:
	archronlog
	arcustomers
	arcustomershiptos
	arcustomerstatistics
	armatchinglines
	armonthlystatistics
	artransactions
	callsheets
	entries
	invoiceheaders
	orderheaders
	salescontacts
	sitelocations
	ssorderheaders
	 */
	
	private String m_sErrorMessage;
	
	ARCustomerNumberChange(
			){
		m_sErrorMessage = "";
	}
	public boolean processChange(
			Connection conn,
			String sFromCustomer,
			String sToCustomer,
			boolean bIsMerge
			){
		
	    ARCustomer arcust = new ARCustomer(sFromCustomer);
	    if(!arcust.load(conn)){
	    	m_sErrorMessage = "Cannot load customer " + sFromCustomer + " - " + arcust.getErrorMessageString();
			return false;
	    }
		
	    //Validate that there are no unposted batch entries AT ALL:
	    String SQL = "SELECT " + SMEntryBatch.ibatchnumber + " FROM " + SMEntryBatch.TableName
	    	+ " WHERE ("
	    		+ "(" + SMEntryBatch.ibatchstatus + " = " + SMBatchStatuses.ENTERED + ")"
	    		+ " OR (" + SMEntryBatch.ibatchstatus + " = " + SMBatchStatuses.IMPORTED + ")"
	    	+ ")"
	    	;
	    try{
	    	ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
	    	if(rs.next()){
	    		m_sErrorMessage = "Unposted batches found - cannot change customer code.";
	    		rs.close();
	    		return false;
	    	}else{
	    		rs.close();
	    	}
	    }catch(SQLException e){
	    	System.out.println("Error checking for unposted batches in " + this.toString() + " - " + e.getMessage());
	    	m_sErrorMessage = "Error checking for unposted batches in " + this.toString() + " - " + e.getMessage();
	    	return false;
	    }
	    
	    if(bIsMerge){
	    	//If it's a merge, make sure the 'to' customer exists:
	    	SQL = "SELECT " 
	    		+ SMTablearcustomer.sCustomerNumber 
	    		+ " FROM " + SMTablearcustomer.TableName 
	    		+ " WHERE ("
	    			+ "(" + SMTablearcustomer.sCustomerNumber + " = '" + sToCustomer + "')"
	    		+ ")"
	    		;
		    try{
		    	ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
		    	if(!rs.next()){
		    		m_sErrorMessage = "The 'MERGE INTO' customer cannot be found.";
		    		rs.close();
		    		return false;
		    	}else{
		    		rs.close();
		    	}
		    }catch(SQLException e){
		    	System.out.println("Error checking for MERGE TO customer in " + this.toString() + " - " + e.getMessage());
		    	m_sErrorMessage = "Error checking for MERGE TO customer in " + this.toString() + " - " + e.getMessage();
		    	return false;
		    }
	    	
	    	//If it's a merge, make sure there are no conflicting document numbers in the transactions file:
	    	SQL = "SELECT " 
	    		+ SMTableartransactions.TableName + "." + SMTableartransactions.sdocnumber 
	    		+ " FROM " + SMTableartransactions.TableName 
	    		+ ", " + SMTableartransactions.TableName + " AS artransactions_1"
	    		+ " WHERE ("
	    			+ "((" + SMTableartransactions.TableName + "." + SMTableartransactions.spayeepayor 
	    				+ ")='" + sFromCustomer + "')" 
	    			+ " AND ((artransactions_1." + SMTableartransactions.spayeepayor + ")='" 
	    				+ sToCustomer + "')"
	    			+ " AND (" + SMTableartransactions.TableName + "." + SMTableartransactions.sdocnumber 
	    				+ " = artransactions_1." + SMTableartransactions.sdocnumber + ")"
	    			+ ")"
	    			;
	    	boolean bDocumentConflict = false;
	    	String sConflictingDocuments = "";
		    try{
		    	ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
		    	while(rs.next()){
		    		bDocumentConflict = true;
		    		sConflictingDocuments = sConflictingDocuments + rs.getString(SMTableartransactions.sdocnumber) + ", ";
		    	}
		    	rs.close();
		    }catch(SQLException e){
		    	System.out.println("Error checking for conflicting transaction documents in " + this.toString() + " - " + e.getMessage());
		    	m_sErrorMessage = "Error checking for conflicting transaction documents in " + this.toString() + " - " + e.getMessage();
		    	return false;
		    }
		    
		    //Now check the matching lines for the same thing:
	    	SQL = "SELECT " 
	    		+ SMTablearmatchingline.TableName + "." + SMTablearmatchingline.sdocnumber 
	    		+ " FROM " + SMTablearmatchingline.TableName 
	    		+ ", " + SMTablearmatchingline.TableName + " AS armatchingline_1"
	    		+ " WHERE ("
	    			+ "((" + SMTablearmatchingline.TableName + "." + SMTablearmatchingline.spayeepayor 
	    				+ ")='" + sFromCustomer + "')" 
	    			+ " AND ((armatchingline_1." + SMTablearmatchingline.spayeepayor + ")='" 
	    				+ sToCustomer + "')"
	    			+ " AND (" + SMTablearmatchingline.TableName + "." + SMTablearmatchingline.sdocnumber 
	    				+ " = armatchingline_1." + SMTablearmatchingline.sdocnumber + ")"
	    			+ ")"
	    			;
		    try{
		    	ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
		    	while(rs.next()){
		    		bDocumentConflict = true;
		    		sConflictingDocuments = sConflictingDocuments + rs.getString(SMTableartransactions.sdocnumber) + ", ";
		    	}
		    	rs.close();
		    }catch(SQLException e){
		    	System.out.println("Error checking for conflicting matching documents in " + this.toString() + " - " + e.getMessage());
		    	m_sErrorMessage = "Error checking for conflicting matching documents in " + this.toString() + " - " + e.getMessage();
		    	return false;
		    }
		    
		    if(bDocumentConflict){
		    	m_sErrorMessage = "Customer " + sFromCustomer + " and customer " + sToCustomer 
		    		+ " have the following document numbers in common: " + sConflictingDocuments 
		    		+ " - customer code cannot be merged.";
		    	return false;
		    }
		    
		    //Check for duplicate customer ship-tos:
	    	boolean bShipToConflict = false;
	    	String sConflictingShipTos = "";
	    	SQL = "SELECT " 
	    		+ SMTablearcustomershiptos.TableName + "." + SMTablearcustomershiptos.sShipToCode 
	    		+ " FROM " + SMTablearcustomershiptos.TableName 
	    		+ ", " + SMTablearcustomershiptos.TableName + " AS " + SMTablearcustomershiptos.TableName + "_1"
	    		+ " WHERE ("
	    			+ "((" + SMTablearcustomershiptos.TableName + "." + SMTablearcustomershiptos.sCustomerNumber 
	    				+ ")='" + sFromCustomer + "')" 
	    			+ " AND ((" + SMTablearcustomershiptos.TableName + "_1." + SMTablearcustomershiptos.sCustomerNumber + ")='" 
	    				+ sToCustomer + "')"
	    			+ " AND (" + SMTablearcustomershiptos.TableName + "." + SMTablearcustomershiptos.sShipToCode 
	    				+ " = " + SMTablearcustomershiptos.TableName + "_1." + SMTablearcustomershiptos.sShipToCode + ")"
	    			+ ")"
	    			;
		    try{
		    	ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
		    	while(rs.next()){
		    		bShipToConflict = true;
		    		sConflictingShipTos = sConflictingShipTos + rs.getString(SMTablearcustomershiptos.sShipToCode) + ", ";
		    	}
		    	rs.close();
		    }catch(SQLException e){
		    	System.out.println("Error checking for conflicting ship-tos in " + this.toString() + " - " + e.getMessage());
		    	m_sErrorMessage = "Error checking for conflicting ship-tos in " + this.toString() + " - " + e.getMessage();
		    	return false;
		    }
		    if(bShipToConflict){
		    	m_sErrorMessage = "Customer " + sFromCustomer + " and customer " + sToCustomer 
		    		+ " have the following ship-to codes in common: " + sConflictingShipTos 
		    		+ " - customer code cannot be merged.";
		    	return false;
		    }
		    
		    //Check that there will be no conflicts in call sheets:
	    	boolean bCallSheetConflict = false;
	    	String sConflictingCallSheets = "";
	    	SQL = "SELECT " 
	    		+ SMTablecallsheets.TableName + "." + SMTablecallsheets.sCallSheetName
	    		+ " FROM " + SMTablecallsheets.TableName 
	    		+ ", " + SMTablecallsheets.TableName + " AS " + SMTablecallsheets.TableName + "_1"
	    		+ " WHERE ("
	    			+ "((" + SMTablecallsheets.TableName + "." + SMTablecallsheets.sAcct 
	    				+ ")='" + sFromCustomer + "')" 
	    			+ " AND ((" + SMTablecallsheets.TableName + "_1." + SMTablecallsheets.sAcct + ")='" 
	    				+ sToCustomer + "')"
	    			+ " AND (" + SMTablecallsheets.TableName + "." + SMTablecallsheets.sCallSheetName 
	    				+ " = " + SMTablecallsheets.TableName + "_1." + SMTablecallsheets.sCallSheetName + ")"
	    			+ ")"
	    			;
		    try{
		    	ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
		    	while(rs.next()){
		    		bCallSheetConflict = true;
		    		sConflictingCallSheets = sConflictingCallSheets + rs.getString(SMTablecallsheets.sCallSheetName) + ", ";
		    	}
		    	rs.close();
		    }catch(SQLException e){
		    	System.out.println("Error checking for conflicting call sheets in " + this.toString() + " - " + e.getMessage());
		    	m_sErrorMessage = "Error checking for conflicting call sheets in " + this.toString() + " - " + e.getMessage();
		    	return false;
		    }
		    if(bCallSheetConflict){
		    	m_sErrorMessage = "Customer " + sFromCustomer + " and customer " + sToCustomer 
		    		+ " have the following call sheets in common: " + sConflictingCallSheets 
		    		+ " - customer code cannot be merged.";
		    	return false;
		    }
		    //Check that there will be no conflicts in salescontacts:
	    	boolean bSalesContactConflict = false;
	    	String sConflictingSalesContacts = "";
	    	SQL = "SELECT " 
	    		+ SMTablesalescontacts.TableName + "." + SMTablesalescontacts.salespersoncode
	    		+ " FROM " + SMTablesalescontacts.TableName 
	    		+ ", " + SMTablesalescontacts.TableName + " AS " + SMTablesalescontacts.TableName + "_1"
	    		+ " WHERE ("
	    			+ "((" + SMTablesalescontacts.TableName + "." + SMTablesalescontacts.scustomernumber 
	    				+ ")='" + sFromCustomer + "')" 
	    			+ " AND ((" + SMTablesalescontacts.TableName + "_1." + SMTablesalescontacts.scustomernumber + ")='" 
	    				+ sToCustomer + "')"
	    			+ " AND (" + SMTablesalescontacts.TableName + "." + SMTablesalescontacts.salespersoncode 
	    				+ " = " + SMTablesalescontacts.TableName + "_1." + SMTablesalescontacts.salespersoncode + ")"
	    			+ ")"
	    			;
		    try{
		    	ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
		    	while(rs.next()){
		    		bSalesContactConflict = true;
		    		sConflictingSalesContacts = sConflictingSalesContacts + rs.getString(SMTablesalescontacts.salespersoncode) + ", ";
		    	}
		    	rs.close();
		    }catch(SQLException e){
		    	System.out.println("Error checking for conflicting Sales Contacts in " + this.toString() + " - " + e.getMessage());
		    	m_sErrorMessage = "Error checking for conflicting Sales Contacts in " + this.toString() + " - " + e.getMessage();
		    	return false;
		    }
		    if(bSalesContactConflict){
		    	m_sErrorMessage = "Customer " + sFromCustomer + " and customer " + sToCustomer 
		    		+ " have the following Sales Contacts in common: " + sConflictingSalesContacts 
		    		+ " - customer code cannot be merged.";
		    	return false;
		    }
		    //

	    }else{  //If this is a CHANGE request:
	    	//Make sure there is NO 'change to' customer already:
	    	SQL = "SELECT " 
	    		+ SMTablearcustomer.sCustomerNumber 
	    		+ " FROM " + SMTablearcustomer.TableName 
	    		+ " WHERE ("
	    			+ "(" + SMTablearcustomer.sCustomerNumber + " = '" + sToCustomer + "')"
	    		+ ")"
	    		;
		    try{
		    	ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
		    	if(rs.next()){
		    		m_sErrorMessage = "The 'CHANGE TO' customer already exists.";
		    		rs.close();
		    		return false;
		    	}else{
		    		rs.close();
		    	}
		    }catch(SQLException e){
		    	System.out.println("Error checking for CHANGE TO customer in " + this.toString() + " - " + e.getMessage());
		    	m_sErrorMessage = "Error checking for CHANGE TO customer in " + this.toString() + " - " + e.getMessage();
		    	return false;
		    }
	    	
			//Make sure there are no orders for this customer number already - even if there are no 
			//AR records, there COULD be an order from some old customer:
		    SQL = "SELECT " + SMTableorderheaders.sOrderNumber + " FROM " + SMTableorderheaders.TableName
	    		+ " WHERE ("
	    			+ "(" + SMTableorderheaders.sCustomerCode + " = '" + sToCustomer + "')"
	    			+ ")"
	    			;
		    try{
		    	ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
		    	if(rs.next()){
		    		m_sErrorMessage = "Orders found with the 'change to' code - cannot change customer code.";
		    		rs.close();
		    		return false;
		    	}else{
		    		rs.close();
		    	}
		    }catch(SQLException e){
		    	System.out.println("Error checking for orders in " + this.toString() + " - " + e.getMessage());
		    	m_sErrorMessage = "Error checking for orders in " + this.toString() + " - " + e.getMessage();
		    	return false;
		    }
		    
			//Next, make sure there are no invoices for this customer number already - even if there are no 
			//AR records, there COULD be an invoice from some old customer:
		    SQL = "SELECT " + SMTableinvoiceheaders.sOrderNumber + " FROM " + SMTableinvoiceheaders.TableName
	    		+ " WHERE ("
	    			+ "(" + SMTableinvoiceheaders.sCustomerCode + " = '" + sToCustomer + "')"
	    			+ ")"
	    			;
		    try{
		    	ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
		    	if(rs.next()){
		    		m_sErrorMessage = "Invoices found with the 'change to' code - cannot change customer code.";
		    		rs.close();
		    		return false;
		    	}else{
		    		rs.close();
		    	}
		    }catch(SQLException e){
		    	System.out.println("Error checking for Invoices in " + this.toString() + " - " + e.getMessage());
		    	m_sErrorMessage = "Error checking for Invoices in " + this.toString() + " - " + e.getMessage();
		    	return false;
		    }
		    
			//Next, make sure there are no speed search records for this customer number already - even if there are no 
			//AR records, there COULD be a speed search record from some old customer:
		    SQL = "SELECT " + SMTablessorderheaders.ORDNUMBER + " FROM " + SMTablessorderheaders.TableName
	    		+ " WHERE ("
	    			+ "(" + SMTablessorderheaders.CUSTOMER + " = '" + sToCustomer + "')"
	    			+ ")"
	    			;
		    try{
		    	ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
		    	if(rs.next()){
		    		m_sErrorMessage = "Speed search orders found with the 'change to' code - cannot change customer code.";
		    		rs.close();
		    		return false;
		    	}else{
		    		rs.close();
		    	}
		    }catch(SQLException e){
		    	System.out.println("Error checking for Speed search orders in " + this.toString() + " - " + e.getMessage());
		    	m_sErrorMessage = "Error checking for Speed search orders in " + this.toString() + " - " + e.getMessage();
		    	return false;
		    }
		    
		    //Make sure there are no call sheet records with the 'change to' code
		    SQL = "SELECT " + SMTablecallsheets.sCallSheetName + " FROM " + SMTablecallsheets.TableName
    			+ " WHERE ("
    			+ "(" + SMTablecallsheets.sAcct + " = '" + sToCustomer + "')"
    			+ ")"
    			;
		    try{
		    	ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
		    	if(rs.next()){
		    		m_sErrorMessage = "Call sheets found with the 'change to' code - cannot change customer code.";
		    		rs.close();
		    		return false;
		    	}else{
		    		rs.close();
		    	}
		    }catch(SQLException e){
		    	System.out.println("Error checking for call sheets in " + this.toString() + " - " + e.getMessage());
		    	m_sErrorMessage = "Error checking for call sheets in " + this.toString() + " - " + e.getMessage();
		    	return false;
		    }
		    //Make sure there are no sales contact records with the 'change to' code
		    SQL = "SELECT " + SMTablesalescontacts.salespersoncode + " FROM " + SMTablesalescontacts.TableName
		    	+ " WHERE ("
		    	+ "(" + SMTablesalescontacts.scustomernumber + " = '" + sToCustomer + "')"
		    	+ ")"
			;
		    try{
		    	ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
		    	if(rs.next()){
		    		m_sErrorMessage = "Sales contacts found with the 'change to' code - cannot change customer code.";
		    		rs.close();
		    		return false;
		    	}else{
		    		rs.close();
		    	}
		    }catch(SQLException e){
		    	System.out.println("Error checking for sales contacts in " + this.toString() + " - " + e.getMessage());
		    	m_sErrorMessage = "Error checking for sales contacts in " + this.toString() + " - " + e.getMessage();
		    	return false;
		    }
		    
	    }
	    
	    //If everything passes up to this point, then we have to set the 'posting flag' in AR to prevent
	    //anyone from trying to post while this is happening:
    	//First check to make sure no one else is posting:
    	try{
    		SQL = "SELECT * FROM " + SMTablearoptions.TableName;
    		ResultSet rsAROptions = clsDatabaseFunctions.openResultSet(SQL, conn);
    		if (!rsAROptions.next()){
    			m_sErrorMessage = "Error getting aroptions record";
        		System.out.println("In " + this.toString() + ": Error getting aroptions record");
        		return false;
    		}else{
    			if(rsAROptions.getLong(SMTablearoptions.ibatchpostinginprocess) == 1){
    				m_sErrorMessage = "A previous posting is not completed";
    				rsAROptions.close();
            		return false;
    			}
    		}
    	}catch (SQLException e){
    		m_sErrorMessage = "Error checking for previous posting - " + e.getMessage();
    		return false;
    	}
    	//If not, then set the posting flag:
    	try{
    		SQL = "UPDATE " + SMTablearoptions.TableName 
    			+ " SET " + SMTablearoptions.ibatchpostinginprocess + " = 1";
    		if (!clsDatabaseFunctions.executeSQL(SQL, conn)){
    			m_sErrorMessage = "Error setting posting flag in aroptions";
        		return false;
    		}
    	}catch (SQLException e){
    		m_sErrorMessage = "Error setting posting flag in aroptions - " + e.getMessage();
    		return false;
    	}
    	
    	//Try to start a data transaction:
    	if(!clsDatabaseFunctions.start_data_transaction(conn)){
    		//Clear the posting flag:
    		try{
        		SQL = "UPDATE " + SMTablearoptions.TableName 
        			+ " SET " + SMTablearoptions.ibatchpostinginprocess + " = 0";
        		if (!clsDatabaseFunctions.executeSQL(SQL, conn)){
        			m_sErrorMessage = "Error clearing posting flag in aroptions";
            		return false;
        		}
        	}catch (SQLException e){
        		m_sErrorMessage = "Error clearing posting flag in aroptions - " + e.getMessage();
        		return false;
        	}
    	}
	    
    	if (!updateCustomers(conn, sFromCustomer, sToCustomer, bIsMerge)){
    		//Clear the posting flag:
    		clsDatabaseFunctions.rollback_data_transaction(conn);
    		try{
        		SQL = "UPDATE " + SMTablearoptions.TableName 
        			+ " SET " + SMTablearoptions.ibatchpostinginprocess + " = 0";
        		if (!clsDatabaseFunctions.executeSQL(SQL, conn)){
        			m_sErrorMessage = "Error clearing posting flag in aroptions";
            		return false;
        		}
        	}catch (SQLException e){
        		m_sErrorMessage = "Error clearing posting flag in aroptions - " + e.getMessage();
        		return false;
        	}
    		return false;
    	}else{
    		if(!clsDatabaseFunctions.commit_data_transaction(conn)){
        		clsDatabaseFunctions.rollback_data_transaction(conn);
        		try{
            		SQL = "UPDATE " + SMTablearoptions.TableName 
            			+ " SET " + SMTablearoptions.ibatchpostinginprocess + " = 0";
            		if (!clsDatabaseFunctions.executeSQL(SQL, conn)){
            			m_sErrorMessage = "Error committing data transaction";
                		return false;
            		}
            	}catch (SQLException e){
            		m_sErrorMessage = "Error committing data transaction - " + e.getMessage();
            		return false;
            	}
        		return false;
    		}
    		try{
        		SQL = "UPDATE " + SMTablearoptions.TableName 
        			+ " SET " + SMTablearoptions.ibatchpostinginprocess + " = 0";
        		if (!clsDatabaseFunctions.executeSQL(SQL, conn)){
        			m_sErrorMessage = "Error clearing posting flag in aroptions";
            		return false;
        		}
        	}catch (SQLException e){
        		m_sErrorMessage = "Error clearing posting flag in aroptions - " + e.getMessage();
        		return false;
        	}
    		//Clear the posting flag, then return
    		return true;
    	}
	}
	private boolean updateCustomers(Connection conn, String sFromCustomer, String sToCustomer, boolean bIsMerge){

		if (bIsMerge){
			return mergeCustomer(conn, sFromCustomer, sToCustomer);
		}else{
			return changeCustomer(conn, sFromCustomer, sToCustomer);
		}    	
	}
	private boolean mergeCustomer(Connection conn, String sFromCustomer, String sToCustomer){
		String SQL = "";
		//Remove the original customer
		try{
    		SQL = "DELETE FROM " + SMTablearcustomer.TableName
			+ " WHERE ("
				+ "(" + SMTablearcustomer.sCustomerNumber + " = '" + sFromCustomer + "')"
			+ ")"
			;
    		if (!clsDatabaseFunctions.executeSQL(SQL, conn)){
    			m_sErrorMessage = "Error deleting current customer number";
        		return false;
    		}
    	}catch (SQLException e){
    		m_sErrorMessage = "Error changing customer number - " + e.getMessage();
    		return false;
    	}
		
	    //Update archronlog:
		try{
    		SQL = "UPDATE " + SMTablearchronlog.TableName
			+ " SET " + SMTablearchronlog.spayeepayor + " = '" + sToCustomer + "'"
			+ " WHERE ("
				+ "(" + SMTablearchronlog.spayeepayor + " = '" + sFromCustomer + "')"
			+ ")"
			;
    		if (!clsDatabaseFunctions.executeSQL(SQL, conn)){
    			m_sErrorMessage = "Error changing ar chron log";
        		return false;
    		}
    	}catch (SQLException e){
    		m_sErrorMessage = "Error changing ar chron log - " + e.getMessage();
    		return false;
    	}
    	
	    //Update customer ship-tos
		try{
    		SQL = "UPDATE " + SMTablearcustomershiptos.TableName
			+ " SET " + SMTablearcustomershiptos.sCustomerNumber + " = '" + sToCustomer + "'"
			+ " WHERE ("
				+ "(" + SMTablearcustomershiptos.sCustomerNumber + " = '" + sFromCustomer + "')"
			+ ")"
			;
    		if (!clsDatabaseFunctions.executeSQL(SQL, conn)){
    			m_sErrorMessage = "Error changing customer ship-to";
        		return false;
    		}
    	}catch (SQLException e){
    		m_sErrorMessage = "Error changing customer ship-to - " + e.getMessage();
    		return false;
    	}
    	
	    //Update artransactions first:
		try{
    		SQL = "UPDATE " + SMTableartransactions.TableName
			+ " SET " + SMTableartransactions.spayeepayor + " = '" + sToCustomer + "'"
			+ " WHERE ("
				+ "(" + SMTableartransactions.spayeepayor + " = '" + sFromCustomer + "')"
			+ ")"
			;
    		if (!clsDatabaseFunctions.executeSQL(SQL, conn)){
    			m_sErrorMessage = "Error changing ar transactions";
        		return false;
    		}
    	}catch (SQLException e){
    		m_sErrorMessage = "Error changing ar transactions - " + e.getMessage();
    		return false;
    	}

	    //Update armatchinglines next:
		try{
    		SQL = "UPDATE " + SMTablearmatchingline.TableName
			+ " SET " + SMTablearmatchingline.spayeepayor + " = '" + sToCustomer + "'"
			+ " WHERE ("
				+ "(" + SMTablearmatchingline.spayeepayor + " = '" + sFromCustomer + "')"
			+ ")"
			;
    		if (!clsDatabaseFunctions.executeSQL(SQL, conn)){
    			m_sErrorMessage = "Error changing ar matchinglines";
        		return false;
    		}
    	}catch (SQLException e){
    		m_sErrorMessage = "Error changing ar matchinglines - " + e.getMessage();
    		return false;
    	}
    	
	    //Update arcustomerstatistics
    	ARCustomerStatistics arFromCustomer = new ARCustomerStatistics(sFromCustomer);
    	if(!arFromCustomer.load(conn)){
    		m_sErrorMessage = "Error loading statistics for " + sFromCustomer;
    		return false;
    	}

    	ARCustomerStatistics arToCustomer = new ARCustomerStatistics(sToCustomer);
    	if(!arToCustomer.load(conn)){
    		m_sErrorMessage = "Error loading statistics for " + sToCustomer;
    		return false;
    	}
    	
    	if(arFromCustomer.getM_bdamountofhighestbalance().compareTo(arToCustomer.getM_bdamountofhighestbalance()) > 0){
    		arToCustomer.setM_bdamountofhighestbalance(arFromCustomer.getM_bdamountofhighestbalance());
    	}    	
    	if(arFromCustomer.getM_bdamountofhighestbalancelastyear().compareTo(arToCustomer.getM_bdamountofhighestbalancelastyear()) > 0){
    		arToCustomer.setM_bdamountofhighestbalancelastyear(arFromCustomer.getM_bdamountofhighestbalancelastyear());
    	}
    	if(arFromCustomer.getM_bdamountofhighestinvoice().compareTo(arToCustomer.getM_bdamountofhighestinvoice()) > 0){
    		arToCustomer.setM_bdamountofhighestinvoice(arFromCustomer.getM_bdamountofhighestinvoice());
    	}
    	if(arFromCustomer.getM_bdamountofhighestinvoicelastyear().compareTo(arToCustomer.getM_bdamountofhighestinvoicelastyear()) > 0){
    		arToCustomer.setM_bdamountofhighestinvoicelastyear(arFromCustomer.getM_bdamountofhighestinvoicelastyear());
    	}

       	if(clsDateAndTimeConversions.DateStringToLong(arFromCustomer.getM_datlastcredit()) 
       			> clsDateAndTimeConversions.DateStringToLong(arToCustomer.getM_datlastcredit())){
    		arToCustomer.setM_datlastcredit(arFromCustomer.getM_datlastcredit());
    		arToCustomer.setM_bdamountoflastcredit(arFromCustomer.getM_bdamountoflastcredit());
    	}

       	if(clsDateAndTimeConversions.DateStringToLong(arFromCustomer.getM_datlastinvoice()) 
       			> clsDateAndTimeConversions.DateStringToLong(arToCustomer.getM_datlastinvoice())){
    		arToCustomer.setM_datlastinvoice(arFromCustomer.getM_datlastinvoice());
    		arToCustomer.setM_bdamountoflastinvoice(arFromCustomer.getM_bdamountoflastinvoice());
    	}

       	if(clsDateAndTimeConversions.DateStringToLong(arFromCustomer.getM_datlastpayment()) 
       			> clsDateAndTimeConversions.DateStringToLong(arToCustomer.getM_datlastpayment())){
    		arToCustomer.setM_datlastpayment(arFromCustomer.getM_datlastpayment());
    		arToCustomer.setM_bdamountoflastpayment(arFromCustomer.getM_bdamountoflastpayment());
    	}

       	//Number of open invoices gets set in the 'updateCustomerStatistics' below:
       	arToCustomer.setM_ltotalnumberofdaystopay(arToCustomer.getM_ltotalnumberofdaystopay() + arFromCustomer.getM_ltotalnumberofdaystopay());
       	arToCustomer.setM_ltotalnumberofpaidinvoices(arToCustomer.getM_ltotalnumberofpaidinvoices() + arFromCustomer.getM_ltotalnumberofpaidinvoices());
       	if(!arToCustomer.updateCustomerStatistics(
       			arToCustomer.getM_datlastinvoice(), 
       			arToCustomer.getM_datlastcredit(),
       			arToCustomer.getM_datlastpayment(),
       			arToCustomer.getM_bdamountoflastinvoice(),
       			arToCustomer.getM_bdamountoflastcredit(),
       			arToCustomer.getM_bdamountoflastpayment(),
       			arToCustomer.getM_ltotalnumberofpaidinvoices(),
       			arToCustomer.getM_ltotalnumberofdaystopay(), 
       			conn)){
       		
    		m_sErrorMessage = "Error updating statistics for " + sToCustomer;
    		return false;
       	}
       	
       	if(!arToCustomer.save(conn)){
    		m_sErrorMessage = "Error saving statistics for " + sToCustomer;
    		return false;
       	}
       	
       	//Delete the current arcustomerstatistics record for the 'from' customer:
		try{
    		SQL = "DELETE FROM " + SMTablearcustomerstatistics.TableName
       		+ " WHERE " + SMTablearcustomerstatistics.sCustomerNumber + " ='" + sFromCustomer + "'";
    		if (!clsDatabaseFunctions.executeSQL(SQL, conn)){
    			m_sErrorMessage = "Error changing ar matchinglines";
        		return false;
    		}
    	}catch (SQLException e){
    		m_sErrorMessage = "Error changing ar matchinglines - " + e.getMessage();
    		return false;
    	}
    	
	    //Update armonthlystatistics
    	//First, sum all the records from the current customer into any matching records for the merge-into
    	//customer
		try{
	    	SQL = "UPDATE " + SMTablearmonthlystatistics.TableName 
    		+ " INNER JOIN " + SMTablearmonthlystatistics.TableName 
    		+ " AS " + SMTablearmonthlystatistics.TableName + "_1"
    		+ " ON (" + SMTablearmonthlystatistics.TableName + "." + SMTablearmonthlystatistics.sMonth 
    		+ " = " + SMTablearmonthlystatistics.TableName + "_1." + SMTablearmonthlystatistics.sMonth 
    		+ ") AND (" + SMTablearmonthlystatistics.TableName + "." + SMTablearmonthlystatistics.sYear 
    		+ " = " + SMTablearmonthlystatistics.TableName + "_1." + SMTablearmonthlystatistics.sYear + ")"
    		+ " SET "
    		+ SMTablearmonthlystatistics.TableName + "_1." + SMTablearmonthlystatistics.sInvoiceTotal 
    			+ " = " + SMTablearmonthlystatistics.TableName + "." + SMTablearmonthlystatistics.sInvoiceTotal
    			+ " + " + SMTablearmonthlystatistics.TableName + "_1." + SMTablearmonthlystatistics.sInvoiceTotal
    		+ ", " + SMTablearmonthlystatistics.TableName + "_1." + SMTablearmonthlystatistics.sCreditTotal 
				+ " = " + SMTablearmonthlystatistics.TableName + "." + SMTablearmonthlystatistics.sCreditTotal
				+ " + " + SMTablearmonthlystatistics.TableName + "_1." + SMTablearmonthlystatistics.sCreditTotal
	    	+ ", " + SMTablearmonthlystatistics.TableName + "_1." + SMTablearmonthlystatistics.sPaymentTotal 
				+ " = " + SMTablearmonthlystatistics.TableName + "." + SMTablearmonthlystatistics.sPaymentTotal
				+ " + " + SMTablearmonthlystatistics.TableName + "_1." + SMTablearmonthlystatistics.sPaymentTotal
		    + ", " + SMTablearmonthlystatistics.TableName + "_1." + SMTablearmonthlystatistics.sNumberOfCredits 
				+ " = " + SMTablearmonthlystatistics.TableName + "." + SMTablearmonthlystatistics.sNumberOfCredits
				+ " + " + SMTablearmonthlystatistics.TableName + "_1." + SMTablearmonthlystatistics.sNumberOfCredits
			+ ", " + SMTablearmonthlystatistics.TableName + "_1." + SMTablearmonthlystatistics.sNumberOfInvoices 
				+ " = " + SMTablearmonthlystatistics.TableName + "." + SMTablearmonthlystatistics.sNumberOfInvoices
				+ " + " + SMTablearmonthlystatistics.TableName + "_1." + SMTablearmonthlystatistics.sNumberOfInvoices
			+ ", " + SMTablearmonthlystatistics.TableName + "_1." + SMTablearmonthlystatistics.sNumberOfPayments 
				+ " = " + SMTablearmonthlystatistics.TableName + "." + SMTablearmonthlystatistics.sNumberOfPayments
				+ " + " + SMTablearmonthlystatistics.TableName + "_1." + SMTablearmonthlystatistics.sNumberOfPayments				
			+ ", " + SMTablearmonthlystatistics.TableName + "_1." + SMTablearmonthlystatistics.sNumberOfPaidInvoices
				+ " = " + SMTablearmonthlystatistics.TableName + "." + SMTablearmonthlystatistics.sNumberOfPaidInvoices
				+ " + " + SMTablearmonthlystatistics.TableName + "_1." + SMTablearmonthlystatistics.sNumberOfPaidInvoices				
			+ ", " + SMTablearmonthlystatistics.TableName + "_1." + SMTablearmonthlystatistics.sTotalNumberOfDaysToPay
				+ " = " + SMTablearmonthlystatistics.TableName + "." + SMTablearmonthlystatistics.sTotalNumberOfDaysToPay
				+ " + " + SMTablearmonthlystatistics.TableName + "_1." + SMTablearmonthlystatistics.sTotalNumberOfDaysToPay				
    		+ " WHERE ("
    			+ "(" + SMTablearmonthlystatistics.TableName + "." 
    				+ SMTablearmonthlystatistics.sCustomerNumber + "='" + sFromCustomer + "')"
    			+ " AND (" + SMTablearmonthlystatistics.TableName + "_1." 
    				+ SMTablearmonthlystatistics.sCustomerNumber + "='" + sToCustomer + "')"
    		+ ")"
    		;
	    //System.out.println("In ARCustomerNumberChange.mergeCustomer: SQL = " + SQL);
		if (!clsDatabaseFunctions.executeSQL(SQL, conn)){
			m_sErrorMessage = "Error updating monthly statistics";
    		return false;
		}
    	}catch (SQLException e){
    		m_sErrorMessage = "Error updating monthly statistics - " + e.getMessage();
    		return false;
    	}

		//Now set any of the records that were updated FROM in the last statement to have a blank customer code, so that we can
		//next delete them:
		try{
	    	SQL = "UPDATE " + SMTablearmonthlystatistics.TableName 
    		+ " INNER JOIN " + SMTablearmonthlystatistics.TableName 
    		+ " AS " + SMTablearmonthlystatistics.TableName + "_1"
    		+ " ON (" + SMTablearmonthlystatistics.TableName + "." + SMTablearmonthlystatistics.sMonth 
    		+ " = " + SMTablearmonthlystatistics.TableName + "_1." + SMTablearmonthlystatistics.sMonth 
    		+ ") AND (" + SMTablearmonthlystatistics.TableName + "." + SMTablearmonthlystatistics.sYear 
    		+ " = " + SMTablearmonthlystatistics.TableName + "_1." + SMTablearmonthlystatistics.sYear + ")"
    		+ " SET "
			//Flag the 'from' records to be deleted:
				+ SMTablearmonthlystatistics.TableName + "." + SMTablearmonthlystatistics.sCustomerNumber + " = ' '"				
    		+ " WHERE ("
    			+ "(" + SMTablearmonthlystatistics.TableName + "." 
    				+ SMTablearmonthlystatistics.sCustomerNumber + "='" + sFromCustomer + "')"
    			+ " AND (" + SMTablearmonthlystatistics.TableName + "_1." 
    				+ SMTablearmonthlystatistics.sCustomerNumber + "='" + sToCustomer + "')"
    		+ ")"
    		;
	    //System.out.println("In ARCustomerNumberChange.mergeCustomer: SQL = " + SQL);
	    	Statement stmt = conn.createStatement();
	    	stmt.execute(SQL);
    	}catch (SQLException e){
    		m_sErrorMessage = "Error flagging monthly statistics with blank customer code - " + e.getMessage();
    		return false;
    	}
		
		//Next, delete any of the 'from customer' monthly statistics records that have already 
    	//been added to the 'to' customer:
		try{
    		SQL = "DELETE FROM " + SMTablearmonthlystatistics.TableName
    		+ " WHERE ("
				+ "(" + SMTablearmonthlystatistics.sCustomerNumber + "='" + sFromCustomer + "')"
			+ ")"
			;
    		if (!clsDatabaseFunctions.executeSQL(SQL, conn)){
    			m_sErrorMessage = "Error removing already summed ar monthly statistics";
        		return false;
    		}
    	}catch (SQLException e){
    		m_sErrorMessage = "Error removing already summed ar monthly statistics - " + e.getMessage();
    		return false;
    	}

		//Next convert any remaining records to the merge-into customer's code
		try{
    		SQL = "UPDATE " + SMTablearmonthlystatistics.TableName + " SET "
    		+ SMTablearmonthlystatistics.sCustomerNumber + " = '" + sToCustomer + "'"
    		+ " WHERE ("
				+ "(" + SMTablearmonthlystatistics.TableName + "." 
					+ SMTablearmonthlystatistics.sCustomerNumber + "='" + sFromCustomer + "')"
			+ ")"
			;
    		if (!clsDatabaseFunctions.executeSQL(SQL, conn)){
    			m_sErrorMessage = "Error renumbering ar monthly statistics";
        		return false;
    		}
    	}catch (SQLException e){
    		m_sErrorMessage = "Error renumbering ar monthly statistics - " + e.getMessage();
    		return false;
    	}
    	
    	//Next, update the average days to pay for all the merge-into customer's records
		try{
    		SQL = "UPDATE " + SMTablearmonthlystatistics.TableName + " SET "
    		+ SMTablearmonthlystatistics.sAverageDaysToPay + " = " 
    			+ SMTablearmonthlystatistics.sTotalNumberOfDaysToPay + "/" + SMTablearmonthlystatistics.sNumberOfPaidInvoices
    		+ " WHERE ("
				+ "(" + SMTablearmonthlystatistics.TableName + "." 
					+ SMTablearmonthlystatistics.sCustomerNumber + "='" + sToCustomer + "')"
				+ " AND (" + SMTablearmonthlystatistics.sNumberOfPaidInvoices + " > 0)"
			+ ")"
			;
    		if (!clsDatabaseFunctions.executeSQL(SQL, conn)){
    			m_sErrorMessage = "Error updating days-to-pay in ar monthly statistics";
        		return false;
    		}
    	}catch (SQLException e){
    		m_sErrorMessage = "Error updating days-to-pay in  ar monthly statistics - " + e.getMessage();
    		return false;
    	}
    	
    	//Update call sheets:
		try{
    		SQL = "UPDATE " + SMTablecallsheets.TableName
			+ " SET " + SMTablecallsheets.sAcct + " = '" + sToCustomer + "'"
			+ " WHERE ("
				+ "(" + SMTablecallsheets.sAcct + " = '" + sFromCustomer + "')"
			+ ")"
			;
    		if (!clsDatabaseFunctions.executeSQL(SQL, conn)){
    			m_sErrorMessage = "Error changing call sheets";
        		return false;
    		}
    	}catch (SQLException e){
    		m_sErrorMessage = "Error changing call sheets - " + e.getMessage();
    		return false;
    	}
    	
    	//Update entries:
		try{
    		SQL = "UPDATE " + SMTableentries.TableName
			+ " SET " + SMTableentries.spayeepayor + " = '" + sToCustomer + "'"
			+ " WHERE ("
				+ "(" + SMTableentries.spayeepayor + " = '" + sFromCustomer + "')"
			+ ")"
			;
    		if (!clsDatabaseFunctions.executeSQL(SQL, conn)){
    			m_sErrorMessage = "Error changing entries";
        		return false;
    		}
    	}catch (SQLException e){
    		m_sErrorMessage = "Error changing entries - " + e.getMessage();
    		return false;
    	}
    	
    	//Update invoice headers
		try{
    		SQL = "UPDATE " + SMTableinvoiceheaders.TableName
			+ " SET " + SMTableinvoiceheaders.sCustomerCode + " = '" + sToCustomer + "'"
			+ " WHERE ("
				+ "(" + SMTableinvoiceheaders.sCustomerCode + " = '" + sFromCustomer + "')"
			+ ")"
			;
    		if (!clsDatabaseFunctions.executeSQL(SQL, conn)){
    			m_sErrorMessage = "Error changing invoice headers";
        		return false;
    		}
    	}catch (SQLException e){
    		m_sErrorMessage = "Error changing invoice headers - " + e.getMessage();
    		return false;
    	}
    	
    	//Update order headers
		try{
    		SQL = "UPDATE " + SMTableorderheaders.TableName
			+ " SET " + SMTableorderheaders.sCustomerCode + " = '" + sToCustomer + "'"
			+ " WHERE ("
				+ "(" + SMTableorderheaders.sCustomerCode + " = '" + sFromCustomer + "')"
			+ ")"
			;
    		if (!clsDatabaseFunctions.executeSQL(SQL, conn)){
    			m_sErrorMessage = "Error changing order headers";
        		return false;
    		}
    	}catch (SQLException e){
    		m_sErrorMessage = "Error changing order headers - " + e.getMessage();
    		return false;
    	}
    	
	    //Update sales contacts
		try{
    		SQL = "UPDATE " + SMTablesalescontacts.TableName
			+ " SET " + SMTablesalescontacts.scustomernumber + " = '" + sToCustomer + "'"
			+ " WHERE ("
				+ "(" + SMTablesalescontacts.scustomernumber + " = '" + sFromCustomer + "')"
			+ ")"
			;
    		if (!clsDatabaseFunctions.executeSQL(SQL, conn)){
    			m_sErrorMessage = "Error changing sales contacts";
        		return false;
    		}
    	}catch (SQLException e){
    		m_sErrorMessage = "Error changing sales contacts - " + e.getMessage();
    		return false;
    	}
    	
	    //Update site locations
		try{
    		SQL = "UPDATE " + SMTablesitelocations.TableName
			+ " SET " + SMTablesitelocations.sAcct + " = '" + sToCustomer + "'"
			+ " WHERE ("
				+ "(" + SMTablesitelocations.sAcct + " = '" + sFromCustomer + "')"
			+ ")"
			;
    		if (!clsDatabaseFunctions.executeSQL(SQL, conn)){
    			m_sErrorMessage = "Error changing site location";
        		return false;
    		}
    	}catch (SQLException e){
    		m_sErrorMessage = "Error changing customer site location - " + e.getMessage();
    		return false;
    	}
	    
    	//Update speed search headers
		try{
    		SQL = "UPDATE " + SMTablessorderheaders.TableName
			+ " SET " + SMTablessorderheaders.CUSTOMER + " = '" + sToCustomer + "'"
			+ " WHERE ("
				+ "(" + SMTablessorderheaders.CUSTOMER + " = '" + sFromCustomer + "')"
			+ ")"
			;
    		if (!clsDatabaseFunctions.executeSQL(SQL, conn)){
    			m_sErrorMessage = "Error changing speed search headers";
        		return false;
    		}
    	}catch (SQLException e){
    		m_sErrorMessage = "Error changing speed search headers - " + e.getMessage();
    		return false;
    	}
		
       	//Delete any default salesperson records for the 'from' customer:
		try{
    		SQL = "DELETE FROM " + SMTabledefaultsalesgroupsalesperson.TableName
       		+ " WHERE " + SMTabledefaultsalesgroupsalesperson.scustomercode + " ='" + sFromCustomer + "'";
    		if (!clsDatabaseFunctions.executeSQL(SQL, conn)){
    			m_sErrorMessage = "Error removing default sales group salespersons";
        		return false;
    		}
    	}catch (SQLException e){
    		m_sErrorMessage = "Error removing default sales group salespersons - " + e.getMessage();
    		return false;
    	}

		return true;
	}
	private boolean changeCustomer(Connection conn, String sFromCustomer, String sToCustomer){
		//Update customers
		String SQL = "";		
		
	    //Start updating tables:
		//customers:
		try{
    		SQL = "UPDATE " + SMTablearcustomer.TableName
			+ " SET " + SMTablearcustomer.sCustomerNumber + " = '" + sToCustomer + "'"
			+ " WHERE ("
				+ "(" + SMTablearcustomer.sCustomerNumber + " = '" + sFromCustomer + "')"
			+ ")"
			;
    		if (!clsDatabaseFunctions.executeSQL(SQL, conn)){
    			m_sErrorMessage = "Error changing customer number";
        		return false;
    		}
    	}catch (SQLException e){
    		m_sErrorMessage = "Error changing customer number - " + e.getMessage();
    		return false;
    	}

    	//Update archronlog:
		try{
    		SQL = "UPDATE " + SMTablearchronlog.TableName
			+ " SET " + SMTablearchronlog.spayeepayor + " = '" + sToCustomer + "'"
			+ " WHERE ("
				+ "(" + SMTablearchronlog.spayeepayor + " = '" + sFromCustomer + "')"
			+ ")"
			;
    		if (!clsDatabaseFunctions.executeSQL(SQL, conn)){
    			m_sErrorMessage = "Error changing ar chron log";
        		return false;
    		}
    	}catch (SQLException e){
    		m_sErrorMessage = "Error changing ar chron log - " + e.getMessage();
    		return false;
    	}
    	
	    //Update customer ship-tos
		try{
    		SQL = "UPDATE " + SMTablearcustomershiptos.TableName
			+ " SET " + SMTablearcustomershiptos.sCustomerNumber + " = '" + sToCustomer + "'"
			+ " WHERE ("
				+ "(" + SMTablearcustomershiptos.sCustomerNumber + " = '" + sFromCustomer + "')"
			+ ")"
			;
    		if (!clsDatabaseFunctions.executeSQL(SQL, conn)){
    			m_sErrorMessage = "Error changing customer ship-to";
        		return false;
    		}
    	}catch (SQLException e){
    		m_sErrorMessage = "Error changing customer ship-to - " + e.getMessage();
    		return false;
    	}
	    
	    //Update arcustomerstatistics
		try{
    		SQL = "UPDATE " + SMTablearcustomerstatistics.TableName
			+ " SET " + SMTablearcustomerstatistics.sCustomerNumber + " = '" + sToCustomer + "'"
			+ " WHERE ("
				+ "(" + SMTablearcustomerstatistics.sCustomerNumber + " = '" + sFromCustomer + "')"
			+ ")"
			;
    		if (!clsDatabaseFunctions.executeSQL(SQL, conn)){
    			m_sErrorMessage = "Error changing customer statistics";
        		return false;
    		}
    	}catch (SQLException e){
    		m_sErrorMessage = "Error changing customer statistics - " + e.getMessage();
    		return false;
    	}
    	
	    //Update armatchinglines
		try{
    		SQL = "UPDATE " + SMTablearmatchingline.TableName
			+ " SET " + SMTablearmatchingline.spayeepayor + " = '" + sToCustomer + "'"
			+ " WHERE ("
				+ "(" + SMTablearmatchingline.spayeepayor + " = '" + sFromCustomer + "')"
			+ ")"
			;
    		if (!clsDatabaseFunctions.executeSQL(SQL, conn)){
    			m_sErrorMessage = "Error changing customer transactions";
        		return false;
    		}
    	}catch (SQLException e){
    		m_sErrorMessage = "Error changing customer matchinglines - " + e.getMessage();
    		return false;
    	}
    	
	    //Update armonthlystatistics
		try{
    		SQL = "UPDATE " + SMTablearmonthlystatistics.TableName
			+ " SET " + SMTablearmonthlystatistics.sCustomerNumber + " = '" + sToCustomer + "'"
			+ " WHERE ("
				+ "(" + SMTablearmonthlystatistics.sCustomerNumber + " = '" + sFromCustomer + "')"
			+ ")"
			;
    		if (!clsDatabaseFunctions.executeSQL(SQL, conn)){
    			m_sErrorMessage = "Error changing monthly statistics";
        		return false;
    		}
    	}catch (SQLException e){
    		m_sErrorMessage = "Error changing monthly statistics - " + e.getMessage();
    		return false;
    	}
    	
	    //Update artransactions
		try{
    		SQL = "UPDATE " + SMTableartransactions.TableName
			+ " SET " + SMTableartransactions.spayeepayor + " = '" + sToCustomer + "'"
			+ " WHERE ("
				+ "(" + SMTableartransactions.spayeepayor + " = '" + sFromCustomer + "')"
			+ ")"
			;
    		if (!clsDatabaseFunctions.executeSQL(SQL, conn)){
    			m_sErrorMessage = "Error changing customer transactions";
        		return false;
    		}
    	}catch (SQLException e){
    		m_sErrorMessage = "Error changing customer transactions - " + e.getMessage();
    		return false;
    	}
    	
	    //Update call sheets
		try{
    		SQL = "UPDATE " + SMTablecallsheets.TableName
			+ " SET " + SMTablecallsheets.sAcct + " = '" + sToCustomer + "'"
			+ " WHERE ("
				+ "(" + SMTablecallsheets.sAcct + " = '" + sFromCustomer + "')"
			+ ")"
			;
    		if (!clsDatabaseFunctions.executeSQL(SQL, conn)){
    			m_sErrorMessage = "Error changing call sheets";
        		return false;
    		}
    	}catch (SQLException e){
    		m_sErrorMessage = "Error changing call sheets - " + e.getMessage();
    		return false;
    	}
    	
	    //Update entries
		try{
    		SQL = "UPDATE " + SMTableentries.TableName
			+ " SET " + SMTableentries.spayeepayor + " = '" + sToCustomer + "'"
			+ " WHERE ("
				+ "(" + SMTableentries.spayeepayor + " = '" + sFromCustomer + "')"
			+ ")"
			;
    		if (!clsDatabaseFunctions.executeSQL(SQL, conn)){
    			m_sErrorMessage = "Error changing batch entries";
        		return false;
    		}
    	}catch (SQLException e){
    		m_sErrorMessage = "Error changing batch entries - " + e.getMessage();
    		return false;
    	}
    	
    	//Update invoice headers
		try{
    		SQL = "UPDATE " + SMTableinvoiceheaders.TableName
			+ " SET " + SMTableinvoiceheaders.sCustomerCode + " = '" + sToCustomer + "'"
			+ " WHERE ("
				+ "(" + SMTableinvoiceheaders.sCustomerCode + " = '" + sFromCustomer + "')"
			+ ")"
			;
    		if (!clsDatabaseFunctions.executeSQL(SQL, conn)){
    			m_sErrorMessage = "Error changing invoice headers";
        		return false;
    		}
    	}catch (SQLException e){
    		m_sErrorMessage = "Error changing invoice headers - " + e.getMessage();
    		return false;
    	}
    	
    	//Update order headers
		try{
    		SQL = "UPDATE " + SMTableorderheaders.TableName
			+ " SET " + SMTableorderheaders.sCustomerCode + " = '" + sToCustomer + "'"
			+ " WHERE ("
				+ "(" + SMTableorderheaders.sCustomerCode + " = '" + sFromCustomer + "')"
			+ ")"
			;
    		if (!clsDatabaseFunctions.executeSQL(SQL, conn)){
    			m_sErrorMessage = "Error changing order headers";
        		return false;
    		}
    	}catch (SQLException e){
    		m_sErrorMessage = "Error changing order headers - " + e.getMessage();
    		return false;
    	}
    	
    	//Update sales contacts
		try{
    		SQL = "UPDATE " + SMTablesalescontacts.TableName
			+ " SET " + SMTablesalescontacts.scustomernumber + " = '" + sToCustomer + "'"
			+ " WHERE ("
				+ "(" + SMTablesalescontacts.scustomernumber + " = '" + sFromCustomer + "')"
			+ ")"
			;
    		if (!clsDatabaseFunctions.executeSQL(SQL, conn)){
    			m_sErrorMessage = "Error changing sales contacts";
        		return false;
    		}
    	}catch (SQLException e){
    		m_sErrorMessage = "Error changing sales contacts - " + e.getMessage();
    		return false;
    	}
    	
	    //Update site locations
		try{
    		SQL = "UPDATE " + SMTablesitelocations.TableName
			+ " SET " + SMTablesitelocations.sAcct + " = '" + sToCustomer + "'"
			+ " WHERE ("
				+ "(" + SMTablesitelocations.sAcct + " = '" + sFromCustomer + "')"
			+ ")"
			;
    		if (!clsDatabaseFunctions.executeSQL(SQL, conn)){
    			m_sErrorMessage = "Error changing customer site location";
        		return false;
    		}
    	}catch (SQLException e){
    		m_sErrorMessage = "Error changing customer site location - " + e.getMessage();
    		return false;
    	}

    	//Update speed search headers
		try{
    		SQL = "UPDATE " + SMTablessorderheaders.TableName
			+ " SET " + SMTablessorderheaders.CUSTOMER + " = '" + sToCustomer + "'"
			+ " WHERE ("
				+ "(" + SMTablessorderheaders.CUSTOMER + " = '" + sFromCustomer + "')"
			+ ")"
			;
    		if (!clsDatabaseFunctions.executeSQL(SQL, conn)){
    			m_sErrorMessage = "Error changing speed search headers";
        		return false;
    		}
    	}catch (SQLException e){
    		m_sErrorMessage = "Error changing speed search headers - " + e.getMessage();
    		return false;
    	}
    	
    	//Update default sales group salespersons:
		try{
    		SQL = "UPDATE " + SMTabledefaultsalesgroupsalesperson.TableName
			+ " SET " + SMTabledefaultsalesgroupsalesperson.scustomercode + " = '" + sToCustomer + "'"
			+ " WHERE ("
				+ "(" + SMTabledefaultsalesgroupsalesperson.scustomercode + " = '" + sFromCustomer + "')"
			+ ")"
			;
    		if (!clsDatabaseFunctions.executeSQL(SQL, conn)){
    			m_sErrorMessage = "Error changing default salesperson records";
        		return false;
    		}
    	}catch (SQLException e){
    		m_sErrorMessage = "Error changing default salesperson records - " + e.getMessage();
    		return false;
    	}
		
		return true;
	}
	public String getErrorMessage (){
		return m_sErrorMessage;
	}
}
