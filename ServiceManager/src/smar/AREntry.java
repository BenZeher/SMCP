package smar;

import SMClasses.*;
import SMDataDefinition.*;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsDateAndTimeConversions;
import ServletUtilities.clsManageBigDecimals;
import ServletUtilities.clsStringFunctions;

import java.sql.Date;
import java.util.Calendar;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Connection;
import java.text.ParseException;
import java.util.ArrayList;

import javax.servlet.ServletContext;

import smgl.GLAccount;

import java.math.BigDecimal;

public class AREntry extends java.lang.Object{
	
	private long m_lid;
	private int m_iBatchNumber;
	private int m_iEntryNumber;
	private int m_iDocumentType;
	private String m_sCustomerNumber;
	private String m_sDocNumber;
	private String m_sDocDescription;
	private java.sql.Date m_datDocDate;
	private String m_sTerms;
	private java.sql.Date m_datDueDate;
	private int m_iLastLine;
	private String m_sControlAcct;
	private String m_sOrderNumber;
	private String m_sPONumber;
	private BigDecimal m_dOriginalAmount;
//	These are always calculated amounts, so we don't allow it to be set:
	private BigDecimal m_dDistributedAmount;
	private BigDecimal m_dUnDistributedAmount;
	private int m_iBatchType;
	private ArrayList<ARLine> LineArray;
	private String m_sErrorMessage;
	//Used until an umbrella data transaction is completed:
	private long m_lTempID;
	/*
	 * Definitions of fields:
	 * 
	 * The original amount is the total entered amount for the entry: for an invoice, it's the invoice
	 * amount, for cash, it's the check or received amount, for an  adjustment, it's the total amount of
	 * the adjustment.  This should ALWAYS be the arithmetic inverse total amount of the lines in the entry.
	 * (i.e., original amount = (-1 * total amount of the lines in the entry)
	 * 
	 * The Distributed amount is just the OPPOSITE of (i.e., -1 *) the total of the line amounts saved in
	 * this entry.  This is always a calculated amount, so we don't allow it to be set
	 * 
	 * The unDistributed amount is the original amount minus the Distributed amount.  No entry can be posted
	 * with an unDistributed amount.
	 * 
	 */
	
	/*
	 *  For the purpose of calculating and detailing an aging:
	 *  We calculate from the 'aropentransactions' and the 'armatchinglines'.  
	 */
	
	/*
	 * 	Arithmetic signs of entries and lines:
	 *  The signs of the entries and lines reflect an INCREASE (positive) or DECREASE (negative) in the
	 *  associated customer's liability to the company.  
	 *  
	 *  The lines on an entry would have an OPPOSITE sign and would have to equal the OPPOSITE (i.e., -1 *)
	 *  of the original entry amount.
	 *  
	 *  So, for example, an INVOICE entry would normally have a POSITIVE original amount.  The lines on 
	 *  an invoice, typically to revenue and/or tax accounts, would have NEGATIVE amounts.  CREDITS would
	 *  be exactly the opposite.
	 *  
	 *  Cash entries, because they DECREASE the customer's liability to the company, would have a NEGATIVE amount
	 *  and their child lines would all have POSITIVE amounts.  Cash entries can either be regular receipts
	 *  or 'prepays'.
	 *  
	 *  Adjustment entries can be either 'entry reversals' (in which case the signs of the entries and lines
	 *  are EXACTLY the OPPOSITE of the entry and lines being reversed) OR misc. adjustments, such as misc.
	 *  receipts, etc.  The sign of misc adjustments depends on what kind of adjustment it is (to cash or to
	 *  AR), but the line total is still always the exact opposite of the entry amount (original amount).
	 *  
	 *  AROPENTRANSACTIONS get the same sign as the amount of their matching entry in TRANSACTIONENTRIES, and
	 *  ARMATCHINGLINES get the same sign as the amount of the TRANSACTIONLINE they came from.
	 *  
	 */ 

	AREntry(
    		String sBatchNumber,
    		String sEntryNumber,
    		String sCustomerNumber
        ) {
    		m_lid = -1;
        	sBatchNumber(sBatchNumber);
        	sEntryNumber(sEntryNumber);
        	m_iDocumentType = ARDocumentTypes.INVOICE;
        	m_sCustomerNumber = sCustomerNumber;
        	m_sDocNumber = "";
        	m_sDocDescription = "INITIALIZED ENTRY";
        	m_datDocDate = clsDateAndTimeConversions.nowAsSQLDate();
        	m_sTerms = "";
        	m_datDueDate = clsDateAndTimeConversions.nowAsSQLDate();
        	m_iLastLine = 0;
        	m_sControlAcct = "";
        	m_sOrderNumber = "";
        	m_sPONumber = "";
        	m_dOriginalAmount = new BigDecimal(0);
        	m_iBatchType = -1;
        	m_dDistributedAmount = new BigDecimal(0);
        	m_dUnDistributedAmount = new BigDecimal(0);
        	m_sErrorMessage = "";

        	LineArray = new ArrayList<ARLine>(0);
        }
    AREntry(
    		String sBatchNumber,
    		String sEntryNumber
        ) {
    		m_lid = -1;
        	sBatchNumber(sBatchNumber);
        	sEntryNumber(sEntryNumber);
        	m_iDocumentType = ARDocumentTypes.INVOICE;
        	m_sCustomerNumber = "";
        	m_sDocNumber = "";
        	m_sDocDescription = "INITIALIZED ENTRY";
        	m_datDocDate = clsDateAndTimeConversions.nowAsSQLDate();
        	m_sTerms = "";
        	m_datDueDate = clsDateAndTimeConversions.nowAsSQLDate();
        	m_iLastLine = 0;
        	m_sControlAcct = "";
        	m_sOrderNumber = "";
        	m_sPONumber = "";
        	m_dOriginalAmount = new BigDecimal(0);
        	m_iBatchType = -1;
        	m_dDistributedAmount = new BigDecimal(0);
        	m_dUnDistributedAmount = new BigDecimal(0);
        	m_sErrorMessage = "";
        	
        	LineArray = new ArrayList<ARLine>(0);
        }
    AREntry(
    		String sEntryID
        ) {
    		slid(sEntryID);
        	m_iBatchNumber = -1;
        	m_iEntryNumber = -1;
        	m_iDocumentType = ARDocumentTypes.INVOICE;
        	m_sCustomerNumber = "";
        	m_sDocNumber = "";
        	m_sDocDescription = "INITIALIZED ENTRY";
        	m_datDocDate = clsDateAndTimeConversions.nowAsSQLDate();
        	m_sTerms = "";
        	m_datDueDate = clsDateAndTimeConversions.nowAsSQLDate();
        	m_iLastLine = 0;
        	m_sControlAcct = "";
        	m_sOrderNumber = "";
        	m_sPONumber = "";
        	m_dOriginalAmount = new BigDecimal(0);
        	m_iBatchType = -1;
        	m_dDistributedAmount = new BigDecimal(0);
        	m_dUnDistributedAmount = new BigDecimal(0);
        	m_sErrorMessage = "";
        	
        	LineArray = new ArrayList<ARLine>(0);
        }
    AREntry(
        ) {
    		m_lid = -1;
    		m_iBatchNumber = -1;
    		m_iEntryNumber = -1;
        	m_iDocumentType = ARDocumentTypes.INVOICE;
        	m_sCustomerNumber = "";
        	m_sDocNumber = "";
        	m_sDocDescription = "INITIALIZED ENTRY";
        	m_datDocDate = clsDateAndTimeConversions.nowAsSQLDate();
        	m_sTerms = "";
        	m_datDueDate = clsDateAndTimeConversions.nowAsSQLDate();
        	m_iLastLine = 0;
        	m_sControlAcct = "";
        	m_sOrderNumber = "";
        	m_sPONumber = "";
        	m_dOriginalAmount = new BigDecimal(0);
        	m_iBatchType = -1;
        	m_dDistributedAmount = new BigDecimal(0);
        	m_dUnDistributedAmount = new BigDecimal(0);
        	m_sErrorMessage = "";
        	
        	LineArray = new ArrayList<ARLine>(0);
        }
    public boolean save_without_data_transaction (Connection conn){
    	
    	//Make sure the batch is still open, just in case someone posted it since we loaded it:
    	if (!batchIsOpen(conn)){
    		return false;
    	}
    	
    	//Make sure there's no open transaction already using this customer number and doc number:
    	try {
			checkIfKeyIsUnique(conn);
		} catch (Exception e1) {
			m_sErrorMessage = "Key is not unique - " + e1.getMessage();
			return false;
		}
    	
    	//If it's a NEW entry, make sure there's no entry already using this customer number and doc number:
    	if(m_iEntryNumber == -1){
	    	if (!checkIfEntryKeyIsUnique(conn)){
	    		return false;
	    	}
    	}
    	//Now we'll validate, and save the entry:
    	if (!validate_lines(conn)){
    		return false;
    	}
    	if (!validate_entry_fields(conn)){
    		return false;
    	}
    	if (!update_distributed_amount()){
    		m_sErrorMessage = "Could not update distributed amt in entry.";
    		return false;
    	}
    		 	
    	/*
    	 * If it's a NEW entry, we have to save the entry first, to get the entry id.
    	 * Next, we validate the lines, then the Entries, then save the lines, then update the entry,
    	 * then update the batch. 
    	*/
    	
    	//We have to save the ID carefully, because we need the original ID AND the one we get from 
    	//saving a new transaction.  This is because if the transaction doesn't complete, we
    	//need to be able to revert back to the old ID:
    	String sEntryID = "";
    	boolean bNewEntry = false;
    	if(m_iEntryNumber == -1){
    		bNewEntry = true;
    		if (!add_new_entry(conn)){
    			return false;
    		}
    		
    		sEntryID = Long.toString(m_lTempID);
    		
    		//If it's a new apply-to entry, also give it a doc number:
    		if(m_iDocumentType == ARDocumentTypes.APPLYTO){
	    		if (!setApplyToDocNumber(m_lTempID, conn)){
	    			return false;
	    		}
    		}
       		//If it's a new apply-to entry, also give it a doc number:
    		if(m_iDocumentType == ARDocumentTypes.REVERSAL){
	    		if (!setReversalDocNumber(m_lTempID, conn)){
	    			return false;
	    		}
    		}
    	}else{
    		sEntryID = Long.toString(m_lid);
    	}
		//At this point, we know we already have a saved entry, whether it's a new entry or not:
    	if (!save_lines(conn, sEntryID)){
    		return false;
    	}
    	//Now save the entry:
        String SQL = "";
		SQL = "UPDATE "
			+ SMTableentries.TableName
			+ " SET "
			+ SMTableentries.spayeepayor + " = '" + clsDatabaseFunctions.FormatSQLStatement(m_sCustomerNumber) + "'"
			+ ", " + SMTableentries.idocumenttype + " = " + sDocumentType()
			+ ", " + SMTableentries.sdocnumber + " = '" + clsDatabaseFunctions.FormatSQLStatement(m_sDocNumber) + "'"
			+ ", " + SMTableentries.sdocdescription + " = '" + clsDatabaseFunctions.FormatSQLStatement(m_sDocDescription) + "'"
			+ ", " + SMTableentries.datdocdate + " = '" + sSQLDocDate() + "'"
			+ ", " + SMTableentries.stermscode + " = '" + clsDatabaseFunctions.FormatSQLStatement(m_sTerms) + "'"
			+ ", " + SMTableentries.datduedate + " = '" + sSQLDueDate() + "'"
			+ ", " + SMTableentries.ilastline + " = " + sLastLine()
			+ ", " + SMTableentries.scontrolacct + " = '" + clsDatabaseFunctions.FormatSQLStatement(m_sControlAcct) + "'"
			+ ", " + SMTableentries.sordernumber + " = '" + sOrderNumber() + "'"
			+ ", " + SMTableentries.sentryponumber + " = '" + clsDatabaseFunctions.FormatSQLStatement(sPONumber()) + "'"
			+ ", " + SMTableentries.doriginalamount + " = " + sOriginalAmountSQLFormat()
			+ " WHERE ("
				+ "(" + SMTableentries.ibatchnumber + " = " + sBatchNumber() + ")"
				+ " AND (" + SMTableentries.ientrynumber + " = " + sEntryNumber() + ")"
			+ ")";
    	try{
	    	if (clsDatabaseFunctions.executeSQL(SQL, conn) == false){
	    		m_sErrorMessage = "Could not update entry with SQL: " + SQL;
	    		return false;
	    	}
    	}catch(SQLException ex){
    		System.out.println("[1579117274] Error in " + this.toString() + " class!!");
    	    System.out.println("SQLException: " + ex.getMessage());
    	    System.out.println("SQLState: " + ex.getSQLState());
    	    System.out.println("SQL: " + ex.getErrorCode());
    	    m_sErrorMessage = "Error updating entry: " + ex.getMessage();
    	    return false;
    	}
    	//Update the batch with the latest entry number:
    	ARBatch batch = new ARBatch(sBatchNumber());
    	try {
			batch.load(conn);
		} catch (Exception e) {
			m_sErrorMessage = "Could not load batch after saving entry - " + e.getMessage();
    		return false;
		}
    	if (!batch.update_last_entry_number(conn)){
    		m_sErrorMessage = "Could not update last entry number in batch";
    		return false;
    	}
    	
    	//If everything went OK, and if it was a new entry, we can set the real entry ID to the one we got when we inserted 
    	if (bNewEntry){
    		m_lid = m_lTempID;
    	}
    	return true;
    	
    }
    public boolean load (
    		ServletContext context, 
    		String sDBID
    		){
    
    	return load (sBatchNumber(), sEntryNumber(), context, sDBID);
    }
    public boolean load (
    		Connection conn
    		){
    
    	return load (sBatchNumber(), sEntryNumber(), conn);
    }
    public boolean load (
		String sBatchNumber,
		String sEntryNumber,
		ServletContext context, 
		String sDBID
		){
    
	    if (! sBatchNumber(sBatchNumber)){
	    	m_sErrorMessage = "Invalid sBatchNumber - " + sBatchNumber;
	    	return false;
	    }
	
	    if (! sEntryNumber(sEntryNumber)){
	    	m_sErrorMessage = "Invalid sEntryNumber - " + sEntryNumber;
	    	return false;
	    }
	 
	    if ((sDBID == null) || (sDBID.compareToIgnoreCase("") == 0)){
	    	m_sErrorMessage = "Error:[1423849756] Invalid sDBID - '" + sDBID + "'";
	    	return false;
	    }
	    
	    Connection conn = clsDatabaseFunctions.getConnection(
	    		context, 
	    		sDBID,
	    		"MySQL",
	    		this.toString() + ".load"
	    		);
	    if (conn == null){
	    	m_sErrorMessage = "Could not open connection in load";
	    	return false;
	    }
	    
	    String SQL = TRANSACTIONSQLs.Get_TransactionEntry(sBatchNumber, sEntryNumber);
		try {
			ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn); 
			rs.next();

			//Load the variables:
			try {
				datDocDate(rs.getDate(SMTableentries.datdocdate));
				datDueDate(rs.getDate(SMTableentries.datduedate));
			} catch (Exception e) {
				m_sErrorMessage = "Error:[1437408984] Invalid document or due date - " + e.getMessage() + ".";
				rs.close();
				clsDatabaseFunctions.freeConnection(context, conn, "[1547067550]");
		    	return false;
			}
			m_dOriginalAmount = rs.getBigDecimal(SMTableentries.doriginalamount);
			iBatchNumber(rs.getInt(SMTableentries.ibatchnumber));
			iDocumentType(rs.getInt(SMTableentries.idocumenttype));
			iEntryNumber(rs.getInt(SMTableentries.ientrynumber));
			iLastLine(rs.getInt(SMTableentries.ilastline));
			lid(rs.getLong(SMTableentries.lid));
			sControlAcct(rs.getString(SMTableentries.scontrolacct));
			sCustomerNumber(rs.getString(SMTableentries.spayeepayor));
			sDocDescription(rs.getString(SMTableentries.sdocdescription));
			sDocNumber(rs.getString(SMTableentries.sdocnumber));
			sTerms(rs.getString(SMTableentries.stermscode));
			sOrderNumber(rs.getString(SMTableentries.sordernumber));
			sPONumber(rs.getString(SMTableentries.sentryponumber));
			//This comes from the batch table:
			m_iBatchType = rs.getInt(SMEntryBatch.ibatchtype);
			
			rs.close();
			
		}catch (SQLException ex){
			m_sErrorMessage = "Error:[1437408985] with SQL: '" + SQL + ex.getMessage() + ".";
			clsDatabaseFunctions.freeConnection(context, conn, "[1547067551]");
	        return false;
		}
		
		if (! load_lines(conn)){
			clsDatabaseFunctions.freeConnection(context, conn, "[1547067552]");
			return false;
		}
		
		if (!update_distributed_amount()){
			clsDatabaseFunctions.freeConnection(context, conn, "[1547067553]");
			return false;
		}
		clsDatabaseFunctions.freeConnection(context, conn, "[1547067554]");
    	return true;
    }
    public boolean load (
    		String sBatchNumber,
    		String sEntryNumber,
    		Connection conn
    		){
        
    	    if (! sBatchNumber(sBatchNumber)){
    	    	m_sErrorMessage = "Invalid sBatchNumber - " + sBatchNumber;
    	    	return false;
    	    }
    	
    	    if (! sEntryNumber(sEntryNumber)){
    	    	m_sErrorMessage = "Invalid sEntryNumber - " + sEntryNumber;
    	    	return false;
    	    }
    	
    	    String SQL = TRANSACTIONSQLs.Get_TransactionEntry(sBatchNumber, sEntryNumber);
    		try {
    			ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn); 
    			rs.next();

    			//Load the variables:
    			datDocDate(rs.getDate(SMTableentries.datdocdate));
    			datDueDate(rs.getDate(SMTableentries.datduedate));
    			m_dOriginalAmount = rs.getBigDecimal(SMTableentries.doriginalamount);
    			iBatchNumber(rs.getInt(SMTableentries.ibatchnumber));
    			iDocumentType(rs.getInt(SMTableentries.idocumenttype));
    			iEntryNumber(rs.getInt(SMTableentries.ientrynumber));
    			iLastLine(rs.getInt(SMTableentries.ilastline));
    			lid(rs.getLong(SMTableentries.lid));
    			sControlAcct(rs.getString(SMTableentries.scontrolacct));
    			sCustomerNumber(rs.getString(SMTableentries.spayeepayor));
    			sDocDescription(rs.getString(SMTableentries.sdocdescription));
    			sDocNumber(rs.getString(SMTableentries.sdocnumber));
    			sTerms(rs.getString(SMTableentries.stermscode));
    			sOrderNumber(rs.getString(SMTableentries.sordernumber));
    			sPONumber(rs.getString(SMTableentries.sentryponumber));
    			//This comes from the batch table:
    			m_iBatchType = rs.getInt(SMEntryBatch.ibatchtype);
    			rs.close();
    		}catch (SQLException ex){
    	    	System.out.println("[1579117283] Error in " + this.toString()+ ".load class!!");
    	        System.out.println("SQLException: " + ex.getMessage());
    	        System.out.println("SQLState: " + ex.getSQLState());
    	        System.out.println("SQL: " + ex.getErrorCode());
    	        return false;
    		}
    		
    		if (! load_lines(conn)){
    			return false;
    		}
    		
    		if (!update_distributed_amount()){
    			return false;
    		}
        
        	return true;
        }
    public boolean load (
    	String sEntryID,
    	ServletContext context, 
		String sDBID
    ){
    	
    	Connection conn = clsDatabaseFunctions.getConnection(
    			context, 
    			sDBID,
    			"MySQL",
    			this.toString() + ".load (2)");
    	if (conn == null){
    		m_sErrorMessage = "Could not open connection in load";
    		return false;
    	}
    	
    	    String SQL = TRANSACTIONSQLs.Get_TransactionEntry_By_EntryID(sEntryID);
    		try {
    			ResultSet rs = clsDatabaseFunctions.openResultSet(
    				SQL, 
    				context, 
    				sDBID,
    				"MySQL",
    				this.toString() + ".load (3)"); 
    			rs.next();

    			//Load the variables:
    			m_lid = Long.parseLong(sEntryID);
    			datDocDate(rs.getDate(SMTableentries.datdocdate));
    			datDueDate(rs.getDate(SMTableentries.datduedate));
    			m_dOriginalAmount = rs.getBigDecimal(SMTableentries.doriginalamount);
    			iBatchNumber(rs.getInt(SMTableentries.ibatchnumber));
    			iDocumentType(rs.getInt(SMTableentries.idocumenttype));
    			iEntryNumber(rs.getInt(SMTableentries.ientrynumber));
    			iLastLine(rs.getInt(SMTableentries.ilastline));
    			lid(rs.getLong(SMTableentries.lid));
    			sControlAcct(rs.getString(SMTableentries.scontrolacct));
    			sCustomerNumber(rs.getString(SMTableentries.spayeepayor));
    			sDocDescription(rs.getString(SMTableentries.sdocdescription));
    			sDocNumber(rs.getString(SMTableentries.sdocnumber));
    			sTerms(rs.getString(SMTableentries.stermscode));
    			sOrderNumber(rs.getString(SMTableentries.sordernumber));
    			sPONumber(rs.getString(SMTableentries.sentryponumber));
    			//This comes from the batch table:
    			m_iBatchType = rs.getInt(SMEntryBatch.ibatchtype);
    			rs.close();
    		}catch (SQLException ex){
    	    	System.out.println("[1579117288] Error in " + this.toString()+ ".load class!!");
    	        System.out.println("SQLException: " + ex.getMessage());
    	        System.out.println("SQLState: " + ex.getSQLState());
    	        System.out.println("SQL: " + ex.getErrorCode());
    	        clsDatabaseFunctions.freeConnection(context, conn, "[1547067540]");
    	        return false;
    		}
    		
    		if (! load_lines(conn)){
    			clsDatabaseFunctions.freeConnection(context, conn, "[1547067541]");
    			return false;
    		}
    		
    		if (!update_distributed_amount()){
    			clsDatabaseFunctions.freeConnection(context, conn, "[1547067542]");
    			return false;
    		}
    		clsDatabaseFunctions.freeConnection(context, conn, "[1547067543]");
        	return true;
        }
    public boolean load (
        	String sEntryID,
        	Connection conn
        ){
        	
        	    String SQL = TRANSACTIONSQLs.Get_TransactionEntry_By_EntryID(sEntryID);
        		try {
        			ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn); 
        			rs.next();

        			//Load the variables:
        			m_lid = Long.parseLong(sEntryID);
        			datDocDate(rs.getDate(SMTableentries.datdocdate));
        			datDueDate(rs.getDate(SMTableentries.datduedate));
        			m_dOriginalAmount = rs.getBigDecimal(SMTableentries.doriginalamount);
        			iBatchNumber(rs.getInt(SMTableentries.ibatchnumber));
        			iDocumentType(rs.getInt(SMTableentries.idocumenttype));
        			iEntryNumber(rs.getInt(SMTableentries.ientrynumber));
        			iLastLine(rs.getInt(SMTableentries.ilastline));
        			lid(rs.getLong(SMTableentries.lid));
        			sControlAcct(rs.getString(SMTableentries.scontrolacct));
        			sCustomerNumber(rs.getString(SMTableentries.spayeepayor));
        			sDocDescription(rs.getString(SMTableentries.sdocdescription));
        			sDocNumber(rs.getString(SMTableentries.sdocnumber));
        			sTerms(rs.getString(SMTableentries.stermscode));
        			sOrderNumber(rs.getString(SMTableentries.sordernumber));
        			sPONumber(rs.getString(SMTableentries.sentryponumber));
        			//This comes from the batch table:
        			m_iBatchType = rs.getInt(SMEntryBatch.ibatchtype);
        			rs.close();
        		}catch (SQLException ex){
        	    	System.out.println("[1579117293] Error in " + this.toString()+ ".load class!!");
        	        System.out.println("SQLException: " + ex.getMessage());
        	        System.out.println("SQLState: " + ex.getSQLState());
        	        System.out.println("SQL: " + ex.getErrorCode());
        	        return false;
        		}
        		
        		if (! load_lines(conn)){
        			return false;
        		}
        		
        		if (!update_distributed_amount()){
        			return false;
        		}
            
            	return true;
            }
    private boolean load_lines (
    		Connection conn
    		){
    	return load_lines(sBatchNumber(), sEntryNumber(), conn);
    }
    private boolean load_lines (
    		String sBatchNumber,
    		String sEntryNumber,
    		Connection conn
    		){
        
    	    if (! sBatchNumber(sBatchNumber)){
    	    	m_sErrorMessage = "Invalid sBatchNumber - " + sBatchNumber;
    	    	return false;
    	    }
    	
    	    if (! sEntryNumber(sEntryNumber)){
    	    	m_sErrorMessage = "Invalid sEntryNumber - " + sEntryNumber;
    	    	return false;
    	    }

    	    //ArrayList LineArray = new ArrayList(0);
    	    
    	    String SQL = TRANSACTIONSQLs.Get_Selected_TransactionLines(sBatchNumber, sEntryNumber);
    		try {
    			ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
    			LineArray.clear();
    			while(rs.next()){
    				//Add a new line class, and load it up:
    				ARLine line = new ARLine(
    						sBatchNumber(), 
    						sEntryNumber(), 
    						Integer.toString(rs.getInt(SMTableentrylines.ilinenumber)));
    				line.dAmount(rs.getBigDecimal(SMTableentrylines.damount));
    				line.lDocAppliedToId(rs.getLong(SMTableentrylines.ldocappliedtoid));
    				line.lEntryId(rs.getLong(SMTableentrylines.lentryid));
    				line.lId(rs.getLong(SMTableentrylines.lid));
    				line.sComment(rs.getString(SMTableentrylines.scomment));
    				line.sDescription(rs.getString(SMTableentrylines.sdescription));
    				line.sDocAppliedTo(rs.getString(SMTableentrylines.sdocappliedto));
    				line.sGLAcct(rs.getString(SMTableentrylines.sglacct));
    				line.setApplyToOrderNumber(rs.getString(SMTableentrylines.sapplytoordernumber));
    				LineArray.add((ARLine) line);
    			}
    			rs.close();
    		}catch (SQLException ex){
    	    	System.out.println("[1579117299] Error in " + this.toString()+ ".load class!!");
    	        System.out.println("SQLException: " + ex.getMessage());
    	        System.out.println("SQLState: " + ex.getSQLState());
    	        System.out.println("SQL: " + ex.getErrorCode());
    	        return false;
    		}
    		if (!update_distributed_amount()){
    			return false;
    		}
        	return true;
        }
    public boolean add_line(ARLine line){

    	line.iBatchNumber(iBatchNumber());
    	line.iEntryNumber(iEntryNumber());
    	line.lEntryId(lid());
    	LineArray.add((ARLine) line);
    	m_iLastLine = LineArray.size();
    	return true;
    }
    public void clearLines(){
    	LineArray.clear();
    }
    private boolean delete_single_line_from_database (
    		String sLineID,
    		Connection conn
    		){
	
	    String SQL = TRANSACTIONSQLs.Delete_Transaction_Line(
	    		sLineID);
		try{
	    	if (clsDatabaseFunctions.executeSQL(SQL, conn) == false){
	    		m_sErrorMessage = "Could not delete single line from database";
	    		return false;
	    	}
	    }catch (SQLException ex){
			System.out.println("[1579117304] Error in " + this.toString() + " - delete_single_line!!");
		    System.out.println("SQLException: " + ex.getMessage());
		    System.out.println("SQLState: " + ex.getSQLState());
		    System.out.println("SQL: " + ex.getErrorCode());
		    m_sErrorMessage = "Could not delete single line from database: - " + ex.getMessage();
		    return false;
		}
    	return true;
    }
    private boolean setApplyToDocNumber(long lEntryID, Connection conn){
    	
    	//Keep trying numbers until we get one that's not been used:
    	int iUniqueifier = 0;
    	boolean bNoMatch = false;
    	
    	while (bNoMatch == false){
    		iUniqueifier++;
    		String sTestDocNumber = "AT" + clsStringFunctions.PadLeft(Long.toString(lEntryID), "0", 9) 
    			+ "-" + clsStringFunctions.PadLeft(Integer.toString(iUniqueifier), "0", 3);
    		
    		try{
    			String SQL = "SELECT * FROM " + SMTableentries.TableName
    				+ " WHERE ("
    					+ "(" + SMTableentries.spayeepayor + " = '" + m_sCustomerNumber + "')"
    					+ " AND (" + SMTableentries.sdocnumber + " = '" + sTestDocNumber + "')"
    				+ ")"
    				;
    			ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
    			if (!rs.next()){
    				m_sDocNumber = sTestDocNumber;
    				bNoMatch = true;
    			}
    			rs.close();
    		}catch (SQLException e){
    			m_sErrorMessage = "Could not check for unique doc numbers - " + e.getMessage();
    			return false;
    		}
    	}
    	if (!bNoMatch){
    		m_sErrorMessage = "Could not create unique doc number up to uniqueifier " + iUniqueifier;
    	}
    	return bNoMatch;
    }
    private boolean setReversalDocNumber(long lEntryID, Connection conn){
    	
    	//Keep trying numbers until we get one that's not been used:
    	int iUniqueifier = 0;
    	boolean bNoMatch = false;
    	
    	while (bNoMatch == false){
    		iUniqueifier++;
    		String sTestDocNumber = "RV" + clsStringFunctions.PadLeft(Long.toString(lEntryID), "0", 9) 
    			+ "-" + clsStringFunctions.PadLeft(Integer.toString(iUniqueifier), "0", 3);
    		
    		try{
    			String SQL = "SELECT * FROM " + SMTableentries.TableName
    				+ " WHERE ("
    					+ "(" + SMTableentries.spayeepayor + " = '" + m_sCustomerNumber + "')"
    					+ " AND (" + SMTableentries.sdocnumber + " = '" + sTestDocNumber + "')"
    				+ ")"
    				;
    			ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
    			if (!rs.next()){
    				m_sDocNumber = sTestDocNumber;
    				bNoMatch = true;
    			}
    			rs.close();
    		}catch (SQLException e){
    			m_sErrorMessage = "Could not check for unique doc numbers - " + e.getMessage();
    			return false;
    		}
    	}
    	
    	if (!bNoMatch){
    		m_sErrorMessage = "Could not create unique doc number up to uniqueifier " + iUniqueifier;
    	}
    	
    	return bNoMatch;
    }
    
    public ARLine getLineByLineID(Long lLineID){
    	for (int i = 0;i<LineArray.size();i++){
    		ARLine Line = (ARLine) LineArray.get(i);
    		if(Line.lId() == lLineID){
    			return Line;
    		}
    	}
    	return null;
    }
    public boolean line_exists(Long lLineID){
    	for (int i = 0;i<LineArray.size();i++){
    		ARLine Line = (ARLine) LineArray.get(i);
    		if(Line.lId() == lLineID){
    			return true;
    		}
    	}
    	return false;
    }
    public ARLine getLineByIndex(int iLineIndex){
    	
    	if (iLineIndex > LineArray.size()){
    		return null;
    	}
    	if (iLineIndex < 0){
    		return null;
    	}
    	
		ARLine Line = (ARLine) LineArray.get(iLineIndex);
		return Line;
    }
    public boolean any_line_applies_to_entry(Long lEntryID){
        //If any line already applies to the selected entry,
        //return true:

    	for (int i = 0;i<LineArray.size();i++){
    		ARLine Line = (ARLine) LineArray.get(i);
    		if(Line.lDocAppliedToId() == lEntryID){
    			return true;
    		}
    	}
    	return false;
    }
    public boolean update_distributed_amount(
		){

    	//This is just a total of the line amounts on this entry, so we don't need to read the disk:
    	m_dDistributedAmount = new BigDecimal(0);
    	for (int i =0; i < LineArray.size(); i++){
    		ARLine line = (ARLine) LineArray.get(i);
    		switch (m_iDocumentType) {
			//case ARDocumentTypes.APPLYTO:
			//	break;
			default: 
				m_dDistributedAmount = m_dDistributedAmount.subtract(line.dAmount());
    		}
    	}
		
		switch (m_iDocumentType) {
			case ARDocumentTypes.RETAINAGE: 
				m_dUnDistributedAmount = m_dOriginalAmount.add(m_dDistributedAmount);
				break;
			case ARDocumentTypes.APPLYTO: 
				m_dUnDistributedAmount = m_dOriginalAmount.add(m_dDistributedAmount);
				break;
			default: 
				m_dUnDistributedAmount = m_dOriginalAmount.subtract(m_dDistributedAmount);
		}

    	return true;
    }
    public void lid (long lid){
    	m_lid = lid;
    }
    public long lid (){
    	return m_lid;
    }
    public String slid (){
    	return Long.toString(m_lid);
    }
    public boolean slid(String slid){
    	try{
    		m_lid = Long.parseLong(slid);
    		return true;
    	}catch (NumberFormatException e){
    		m_sErrorMessage = "Error formatting entry ID from string: " + slid + ".";
    		return false;
    	}
    }
    public void iBatchNumber (int iBatchNumber){
    	m_iBatchNumber = iBatchNumber;
    }
    public int iBatchNumber (){
    	return m_iBatchNumber;
    }
    public boolean sBatchNumber (String sBatchNumber){
    	try{
    		m_iBatchNumber = Integer.parseInt(sBatchNumber);
    		return true;
    	}catch (NumberFormatException e){
    		m_sErrorMessage = "Error formatting batch number from string: " + sBatchNumber + ".";
    		return false;
    	}
    }
    public String sBatchNumber (){
    	return Integer.toString(m_iBatchNumber);
    }
    private void iEntryNumber (int iEntryNumber){
    	m_iEntryNumber = iEntryNumber;
    }
    public int iEntryNumber (){
    	return m_iEntryNumber;
    }
    public boolean sEntryNumber (String sEntryNumber){
    	try{
    		m_iEntryNumber = Integer.parseInt(sEntryNumber);
    		return true;
    	}catch (NumberFormatException e){
    		m_sErrorMessage = "Error formatting Entry number from string: " + sEntryNumber + ".";
    		return false;
    	}
    }
    public String sEntryNumber (){
    	return Integer.toString(m_iEntryNumber);
    }
    private void iDocumentType (int iDocumentType){
    	m_iDocumentType = iDocumentType;
    }
    public boolean sDocumentType (String sDocumentType){
    	
    	try{
    		m_iDocumentType = Integer.parseInt(sDocumentType);
    		return true;
    	}catch (NumberFormatException e){
    		m_sErrorMessage = "Error formatting document type from string: " + sDocumentType + ".";
    		return false;
    	}
    }
    public String sDocumentType (){
    	return Integer.toString(m_iDocumentType);
    }
    public int getDocumentType (){
    	return m_iDocumentType;
    }
    public String sDocumentTypeLabel(){
    	return ARDocumentTypes.Get_Document_Type_Label(m_iDocumentType);
    }
    public boolean sCustomerNumber (String sCustomerNumber){
    	
    	//Only Misc Receipts and reversals of misc receipts don't require customer numbers:
    	if (
    			(m_iDocumentType == ARDocumentTypes.CREDIT)
    			|| (m_iDocumentType == ARDocumentTypes.CREDITADJUSTMENT)
    			|| (m_iDocumentType == ARDocumentTypes.INVOICE)
    			|| (m_iDocumentType == ARDocumentTypes.INVOICEADJUSTMENT)
    			|| (m_iDocumentType == ARDocumentTypes.PREPAYMENT)
    			|| (m_iDocumentType == ARDocumentTypes.RECEIPT)
    			|| (m_iDocumentType == ARDocumentTypes.RETAINAGE)
    			|| (m_iDocumentType == ARDocumentTypes.APPLYTO)
    		){
	    	if (sCustomerNumber.length() == 0){
	    		m_sErrorMessage = "sCustomer in AREntry - customer number is empty.";
	    		return false;
	    	}
    	}
    	if (sCustomerNumber.length() > SMTableentries.spayeepayorLength){
    		m_sErrorMessage = "Customer number is too long.";
    		return false;
    	}
    	
    	m_sCustomerNumber = sCustomerNumber;
    	return true;
    }
    public String sCustomerNumber (){
    	return m_sCustomerNumber;
    }
    public boolean sDocNumber (String sDocNumber){
    	//Apply-to and reversal doc numbers can be empty, if the entry hasn't been saved yet:
    	if (
    			(
    				(m_iDocumentType == ARDocumentTypes.APPLYTO)
    				|| (m_iDocumentType == ARDocumentTypes.REVERSAL)
    			)
    			&& (m_lid == -1L)
    		){
    		m_sDocNumber = sDocNumber;
    		return true;
    	}
    	
    	if (sDocNumber.length() == 0){
    		return false;
    	}else{
    		m_sDocNumber = sDocNumber;
    		return true;
    	}
    }
    public String sDocNumber (){
    	return m_sDocNumber;
    }
    public boolean sDocDescription (String sDocDescription){
    	m_sDocDescription = sDocDescription;
    	return true;
    }
    public String sDocDescription (){
    	return m_sDocDescription;
    }
    public boolean datDocDate (java.sql.Date datDocDate){
    	if (! clsDateAndTimeConversions.IsValidDate(datDocDate)){
    		return false;
    	}else{
    		m_datDocDate = datDocDate;
    		return true;
    	}
    }
    public boolean datDocDate (String sYear, String sMonth, String sDay){
    	
		if (! clsDateAndTimeConversions.IsValidDate(
				Integer.parseInt(sYear), 
				Integer.parseInt(sMonth) - 1,
				Integer.parseInt(sDay)
				)
		){
    		return false;
    	}else{
    		try {
				m_datDocDate = clsDateAndTimeConversions.StringTojavaSQLDate(
					"MM/dd/yyyy", sMonth + "/" + sDay + "/" + sYear);
			} catch (ParseException e) {
				m_sErrorMessage = "Error:[1423840166] Invalid document date: " 
				    + sMonth + "/" + sDay + "/" + sYear + ".- " + e.getMessage();
			}
    		return true;
    	}
    }
    public boolean datDocDate (String sFormat, String sDocDate){
		if (! clsDateAndTimeConversions.IsValidDateString(sFormat, sDocDate)){
    		return false;
    	}else{
    		java.sql.Date datTestDate;
    		datTestDate = clsDateAndTimeConversions.StringToSQLDateStrict(sFormat, sDocDate);
    		
    		//Test that the date is within 90 years in either direction:
    		Calendar c1 = Calendar.getInstance();
    		c1.add(Calendar.YEAR, 90);
    		java.sql.Date datBoundDate = new java.sql.Date(c1.getTimeInMillis());
    		if (datTestDate.after(datBoundDate)){
    			return false;
    		}
    		c1.add(Calendar.YEAR, -180);
    		datBoundDate = new java.sql.Date(c1.getTimeInMillis());
        	if (datTestDate.before(datBoundDate)){
        		return false;
    		}
        	m_datDocDate = datTestDate;
    		return true;
    	}
    }
    public String sStdDocDate (){
    	return clsDateAndTimeConversions.utilDateToString(m_datDocDate, "MM/dd/yyyy");
    }
    public String sSQLDocDate (){
    	return clsDateAndTimeConversions.utilDateToString(m_datDocDate, "yyyy-MM-dd");
    }
    public Calendar calDocDate (){
    	return clsDateAndTimeConversions.sqlDateToCalendar(m_datDocDate);
    }
    public java.sql.Date DocDate(){
    	return m_datDocDate;
    }
    public boolean sTerms (String sTerms){
    	if (sTerms.equalsIgnoreCase("")){
    		if (m_iDocumentType == ARDocumentTypes.INVOICE){
    			return false;
    		}
    	}
    	m_sTerms = sTerms;
    	return true;
    }
    public String sTerms (){
    	return m_sTerms;
    }
    public boolean datDueDate (Date datDueDate){
    	if (! clsDateAndTimeConversions.IsValidDate(datDueDate)){
    		if (m_iDocumentType == ARDocumentTypes.INVOICE){
    			return false;
    		}else {
    			m_datDueDate = clsDateAndTimeConversions.nowAsSQLDate();
				return true;
    		}
    	}else{
    		m_datDueDate = datDueDate;
    		return true;
    	}
    }
    public boolean datDueDate (String sYear, String sMonth, String sDay){
		if (! clsDateAndTimeConversions.IsValidDate(
				Integer.parseInt(sYear), 
				Integer.parseInt(sMonth),
				Integer.parseInt(sDay)
				)
		){
			if (m_iDocumentType == ARDocumentTypes.INVOICE){
				return false;
			}else{
				m_datDueDate = clsDateAndTimeConversions.nowAsSQLDate();
				return true;
			}
    	}else{
    		try {
				m_datDueDate = clsDateAndTimeConversions.StringTojavaSQLDate("MM/dd/yyyy", sMonth + "/" + sDay + "/" + sYear);
			} catch (ParseException e) {
				m_sErrorMessage = "Error:[1423840167] Invalid due date: " 
					    + sMonth + "/" + sDay + "/" + sYear + ".- " + e.getMessage();
			}
    		return true;
    	}
    }
    public boolean datDueDate (String sFormat, String sDueDate){
    	
		if (! clsDateAndTimeConversions.IsValidDateString(sFormat, sDueDate)){
			if (m_iDocumentType == ARDocumentTypes.INVOICE){
				return false;
			}else{
				m_datDueDate = clsDateAndTimeConversions.nowAsSQLDate();
				return true;
			}
    	}else{
    		try {
				m_datDueDate = clsDateAndTimeConversions.StringTojavaSQLDate(sFormat, sDueDate);
			} catch (ParseException e) {
				m_sErrorMessage = "Error:[1423840168] Invalid due date: " 
					    + sDueDate + ".- " + e.getMessage();
			}
    		return true;
    	}
    }
    public Calendar calDueDate (){
    	return clsDateAndTimeConversions.sqlDateToCalendar(m_datDueDate);
    }
    public String sStdDueDate (){
    	return clsDateAndTimeConversions.utilDateToString(m_datDueDate, "MM/dd/yyyy");
    }
    public String sSQLDueDate (){
    	return clsDateAndTimeConversions.utilDateToString(m_datDueDate, "yyyy-MM-dd");
    }
    public int iLastLine (){
    	return m_iLastLine;
    }
    private void iLastLine (int iLastLine){
    	m_iLastLine = iLastLine;
    }
    private String sLastLine (){
    	return Integer.toString(m_iLastLine);
    }
    public boolean sControlAcct (String sControlAcct){
    	m_sControlAcct = sControlAcct;
    	return true;
    }
    public String sControlAcct (){
    	return m_sControlAcct;
    }
    public String sOrderNumber (){
    	return m_sOrderNumber;
    }
    public boolean sOrderNumber (String sOrderNumber){
    	m_sOrderNumber = sOrderNumber;
    	return true;
    }
    public String sPONumber (){
    	return m_sPONumber;
    }
    public boolean sPONumber (String sPONumber){
    	m_sPONumber = sPONumber;
    	return true;
    }
    public void dOriginalAmount (BigDecimal dOriginalAmount){
    	m_dOriginalAmount = dOriginalAmount;
    }
    public BigDecimal dOriginalAmount (){
    	return m_dOriginalAmount;
    }
    public boolean sOriginalAmount (String sOriginalAmount){
    	sOriginalAmount = sOriginalAmount.replace(",", "");
    	try{
    		sOriginalAmount = sOriginalAmount.replace(",", "");
    		BigDecimal bd = new BigDecimal(sOriginalAmount);
    		bd = bd.setScale(2, BigDecimal.ROUND_HALF_UP);
    		m_dOriginalAmount =  bd;
    		return true;
    	}catch (NumberFormatException e){
    		m_sErrorMessage = "Error converting Original amount from string: " + sOriginalAmount + ".";
    		return false;
    	}
    }
    public String sOriginalAmountSTDFormat (){
    	return clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(m_dOriginalAmount);
    }
    private String sOriginalAmountSQLFormat (){
    	return clsManageBigDecimals.BigDecimalTo2DecimalSQLFormat(m_dOriginalAmount);
    }
    public BigDecimal dDistributedAmount (){
    	return m_dDistributedAmount;
    }    
    public String sDistributedAmountSTDFormat (){
    	return clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(m_dDistributedAmount);
    }
    public BigDecimal dUnDistributedAmount (){
    	return m_dUnDistributedAmount;
    } 
    public String sUnDistributedAmountSTDFormat (){
    	return clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(m_dUnDistributedAmount);
    }
    public boolean iBatchType(int iBatchType){
    	//Only three batch types at this time:
    	switch (iBatchType){
	    	case 0: m_iBatchType = iBatchType; return true;
	    	case 1: m_iBatchType = iBatchType; return true;
	    	case 2: m_iBatchType = iBatchType; return true;
    	default: return false;
    	}
    }
    public boolean sBatchType(String sBatchType){
    	int iBatchType = -1;
    	try{
    		iBatchType = Integer.parseInt(sBatchType);
    	}catch (NumberFormatException e){
    		return false;
    	}
    	return iBatchType(iBatchType);
    }
    public int iBatchType(){
    	return m_iBatchType;
    }
    public String sBatchType(){
    	return Integer.toString(m_iBatchType);
    }
    public boolean setLineAmountString(String sLineID, String sAmount){
    	
    	long lLineID;
    	try{
    		lLineID = Long.parseLong(sLineID);
    	}catch (NumberFormatException e){
    		m_sErrorMessage = "Error formatting long from " + sLineID + " in setLineAmountString";
    		return false;
    	}

    	for (int i = 0;i < LineArray.size();i++){
    		ARLine Line = (ARLine) LineArray.get(i);
    		if(Line.lId() == lLineID){
    			return Line.setAmountString(sAmount);
    		}
    	}
    	return true;
    }
    public boolean setLineAmount(String sLineID, BigDecimal dAmount){
    	
    	long lLineID;
    	try{
    		lLineID = Long.parseLong(sLineID);
    	}catch (NumberFormatException e){
    		m_sErrorMessage = "Error formatting long from " + sLineID + " in setLineAmountString";
    		return false;
    	}

    	for (int i = 0;i < LineArray.size();i++){
    		ARLine Line = (ARLine) LineArray.get(i);
    		if(Line.lId() == lLineID){
    			return Line.dAmount(dAmount);
    		}
    	}
    	return true;
    }
    public BigDecimal getLineAmount(String sLineID){
    	
    	long lLineID;
    	BigDecimal d = new BigDecimal(0);
    	try{
    		lLineID = Long.parseLong(sLineID);
    	}catch (NumberFormatException e){
    		m_sErrorMessage = "Error formatting long from " + sLineID + " in setLineAmountString";
    		return d;
    	}

    	for (int i = 0;i < LineArray.size();i++){
    		ARLine Line = (ARLine) LineArray.get(i);
    		if(Line.lId() == lLineID){
    			d = Line.dAmount();
    		}
    	}
    	return d;
    }
    public boolean validate_entry_fields (Connection conn){
        //Validate the entries here:
    	//Don't need customer numbers on misc adjustments:
    	//Reversals don't need to be checked either, because they are not user entered, 
    	//and they may include misc receipt reversals anyway:
    	if (
    			(m_iDocumentType != ARDocumentTypes.MISCRECEIPT)
    			&& (m_iDocumentType != ARDocumentTypes.REVERSAL)
    			){ 
	        if (m_sCustomerNumber.length() == 0){
	        	m_sErrorMessage = "Customer number is empty.";
	        	return false;
	        }
    	}
	    if (m_sCustomerNumber.length() > SMTableentries.spayeepayorLength){
        	m_sErrorMessage = "Customer number is too long.";
        	return false;
        }

        if (m_sDocNumber.length() == 0){
        	//If it's a new entry and if it's an apply-to or a reversal, the doc number can be blank 
        	//until it's saved the first time:
        	if(
        			((m_iDocumentType == ARDocumentTypes.APPLYTO)
        			|| (m_iDocumentType == ARDocumentTypes.REVERSAL))
        			&& (m_lid == -1L)
        	){
        	}else{
	        	m_sErrorMessage = "Document number is empty.";
	        	return false;
        	}
        }
        if (m_sDocNumber.length() > SMTableentries.sdocnumberLength){
        	m_sErrorMessage = "Document number is too long.";
        	return false;
        }

        if (m_sControlAcct.length() == 0){
        	m_sErrorMessage = "GL Acct is empty.";
        	return false;
        }
        if (m_sControlAcct.length() > SMTableentries.sdocnumberLength){
        	m_sErrorMessage = "GL Acct is too long.";
        	return false;
        }
        
        //Need to validate the GL account:
        GLAccount gl = new GLAccount(m_sControlAcct);
        if (!gl.load(conn)){
        	m_sErrorMessage = "Control Acct '" + m_sControlAcct + "' is invalid.";
        	return false;
        }
        
        if (m_sOrderNumber.length() > SMTableentries.sordernumberLength){
        	m_sErrorMessage = "Order number is too long.";
        	return false;
        }
        if (m_sPONumber.length() > SMTableentries.sentryponumberLength){
        	m_sErrorMessage = "PO number is too long.";
        	return false;
        }
        //Set the last line value:
        iLastLine(LineArray.size());
        
        if (!validateEntryAmount(conn)){
        	return false;
        }
        
        if (!update_distributed_amount()){
        	m_sErrorMessage = "Could not update amounts in validate_entries.";
        }

    	return true;
    }
    private boolean validateEntryAmount(Connection conn){
    	
    	//Here's where we check that the entry amount is valid - any checks get put in here:
    	if (m_iDocumentType == ARDocumentTypes.CREDIT){
    		//The entry amount can not be more than the invoice amount:
    		ARLine line = (ARLine) LineArray.get(0);
    		ARTransaction trans = new ARTransaction(line.sDocAppliedToId());
	    	if (! trans.load(conn)){
	    		//m_sErrorMessage = "Could not load apply-to invoice";
	    		//return false;
	    	}else{
		    	if (m_dOriginalAmount.negate().compareTo(trans.getdOriginalAmount()) == 1){
		    		m_sErrorMessage = "Credit is larger than original invoice";
		    		return false;
		    	}
	    	}
    	}
    	return true;
    }
    private boolean validate_lines (Connection conn){
        //Validate the lines here:

    	assign_line_numbers();
    	
    	//Don't allow any entries to be saved without lines:
    	if (LineArray.size() < 1){
    		m_sErrorMessage = "Entry cannot be saved with no distribution lines; customer: " 
    				+ m_sCustomerNumber + ", document number: " + m_sDocNumber + ".";
    		return false;
    	}
    	
    	BigDecimal bdTotalAmountCredited = BigDecimal.ZERO;
    	BigDecimal bdApplyToTransCurrentAmt = BigDecimal.ZERO;
    	BigDecimal bdApplyToTransOriginalAmt = BigDecimal.ZERO;
    	String sOriginalInvoice = "";
    	
    	for (int i = 0; i < LineArray.size(); i++){
    		ARLine line = (ARLine) LineArray.get(i);
        	line.iBatchNumber(iBatchNumber());
        	line.iEntryNumber(iEntryNumber());
        	line.lEntryId(lid());
        	if (line.sGLAcct().trim().equals("")){
        		m_sErrorMessage = "GLAcct cannot be blank on line number " 
        				+ line.sLineNumber() + ", customer: " + m_sCustomerNumber + ", document number: " + m_sDocNumber  + ".";
        		return false;
        	}
        	
        	//If it's a cash entry, validate that the apply-to doc is available:
        	if (line.lDocAppliedToId() != -1){
        		//The entry amount can not be more than the invoice amount:
        		ARTransaction trans = new ARTransaction();
        		//Only check if we are applying to a real document:
    	    	if (! trans.load(m_sCustomerNumber, line.sDocAppliedTo(), conn)){
    	    		m_sErrorMessage = "Could not find apply-to document";
    	    		return false;
    	    	}
    	    	if (m_iDocumentType == ARDocumentTypes.RECEIPT){
	    	    	if (
	    	    		(trans.getiDocType() != ARDocumentTypes.INVOICE)
	    	    		&& (trans.getiDocType() != ARDocumentTypes.RETAINAGE)
	    	    	){
	    	    		m_sErrorMessage = "Apply-to document is not an invoice";
	    	    		return false;
	    	    	}
    	    	}
    	    	bdApplyToTransCurrentAmt = trans.getdCurrentAmount();
    	    	bdApplyToTransOriginalAmt = trans.getdOriginalAmount();
    	    	//Check to make sure we are not trying to apply more to the document than it's
    	    	//current (open) amount - this is OK for a credit, since we allow credits
    	    	//to be for more than the current amt of the credited invoice, but NOT more than 
    	    	//the original amount of the invoice:
    	    	
    	    	if (m_iDocumentType == ARDocumentTypes.CREDIT){
    	    		//Keep a running track on how much was credited so we can check at the end to make
    	    		//sure that we haven't credited more than the original invoice amount:
    	    		bdTotalAmountCredited = bdTotalAmountCredited.add(line.dAmount());
    	    		sOriginalInvoice = trans.getDocNumber();
    	    	}else{
    	    	//Else if it's NOT a credit:
    	    		//Unless it's a reversal, we can't apply more than the remaining amount:
    	    		if (m_iDocumentType != ARDocumentTypes.REVERSAL){
	        	    	if (bdApplyToTransCurrentAmt.abs().compareTo(line.dAmount().abs()) < 0){
	        	    		m_sErrorMessage = "You are trying to apply more than the remaining amt to document " 
	        	    			+ trans.getDocNumber() + " for customer " + trans.getCustomerNumber() + ".";
	        	    		return false;

	        	    	}
    	    		}
    	    	}
            	//If the entry is applying to a document, see if there are any other unposted lines applying
            	//to the same document.  If there are, then make sure we are not overapplying with this 
            	//entry:
    	    	String SQL = "SELECT SUM(" + SMTableentrylines.damount + ") AS pendingamt"
    	    		+ " FROM " + SMTableentrylines.TableName + ", " + SMEntryBatch.TableName
    	    		+ " WHERE ("
    	    			//First, get any lines that already apply to this doc:
    	    			+ "(" + SMTableentrylines.ldocappliedtoid + " = " + line.sDocAppliedToId() + ")"
    	    			//And only those in unposted and undeleted batches:
    	    			+ " AND (" 
    	    				+ "(" + SMEntryBatch.TableName + "." + SMEntryBatch.ibatchstatus + " = " 
    	    					+ SMBatchStatuses.ENTERED + ")"
    	    				+ " OR (" + SMEntryBatch.TableName + "." + SMEntryBatch.ibatchstatus + " = " 
    							+ SMBatchStatuses.IMPORTED + ")"
    					+ ")"
    					//Link the lines to the batches:		
    					+ " AND (" + SMEntryBatch.TableName + "." + SMEntryBatch.ibatchnumber + " = "
    						+ SMTableentrylines.TableName + "." + SMTableentrylines.ibatchnumber + ")"
    					//Don't include this entry itself:	
        				+ " AND (" + SMTableentrylines.TableName + "." + SMTableentrylines.lid + " != "
    						+ Long.toString(line.lId()) + ")"
    	    		+ ")"
    	    		;
    	    	try{
    	    		ResultSet rsApplyToCheck = clsDatabaseFunctions.openResultSet(SQL, conn);
    	    		if(rsApplyToCheck.next()){
    	    			if (rsApplyToCheck.getBigDecimal("pendingamt") != null){
    	    				BigDecimal bdPendingApplyToAmts = rsApplyToCheck.getBigDecimal("pendingamt");
	    	    			if (bdApplyToTransCurrentAmt.subtract(bdPendingApplyToAmts).compareTo(line.dAmount()) < 0){
	        	    			m_sErrorMessage = "One or more previous unposted batch entries are already applied to document " + line.sDocAppliedTo()
	    	    				+ " in the total amount of " + clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(bdPendingApplyToAmts)
	    	    				+ " so this entry is trying to apply more than the amount pending on that document."
	    	    				+ "  The entry cannot be saved.";
	    	    			}
    	    			}
    	    		}
    	    		rsApplyToCheck.close();
    	    	}catch(SQLException e){
    	    		m_sErrorMessage = "Error checking unposted apply-to's: " + e.getMessage();
    	    		return false;
    	    	}
        	}
    	}
    	if (m_iDocumentType == ARDocumentTypes.CREDIT){
			if (bdApplyToTransOriginalAmt.compareTo(bdTotalAmountCredited) < 0){
	    		m_sErrorMessage = "You are trying to credit more (" + bdTotalAmountCredited.toString() 
	    			+ ") than the original amt (" + bdApplyToTransOriginalAmt.toString()
	    			+ ") on document " + sOriginalInvoice + " for customer " + this.m_sCustomerNumber 
	    			+ ".";
	    		return false;
	    	}
    	}
    	return true;
    }
    private boolean save_lines (Connection conn, String sEntryID){

    	/*
    	 * Save/update each line, one at a time.
    	 * 
    	 * If there are any lines that are in the data, but NOT in the entry, we need to delete those.
    	 * If the line is a new line, add it.
    	 * If the line is an existing line, update it.
    	*/
    	if (!delete_removed_lines(conn)){
    		return false;
    	}
    	
    	for (int i = 0; i < LineArray.size(); i++){
    		ARLine line = (ARLine) LineArray.get(i);
    		//Set the line number based on the index of the LineArray:
    		line.iLineNumber(i + 1);
    		if (line.lId() == -1){
    			if (! add_single_line(line, conn, sEntryID)){
    				return false;
    			}
    		}else{
    			//Else, if it's already got a line id, then it's an existing line, and we can update it:
    			if (! update_single_line(line, conn, sEntryID)){
    				return false;
    			}
    		}
    	}
    	return true;
    }
    private int get_last_entry_number_in_batch(String sBatchNumber, Connection conn){

    	String SQL = TRANSACTIONSQLs.Get_Last_Entry_Number(sBatchNumber);
    	try{
	    	ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
	    	if (rs.next()){
	    		int iEntry = rs.getInt(SMTableentries.ientrynumber);
	    		rs.close();
	    		return iEntry;
	    	}
	    	else{
	    		rs.close();
	    		return 0;
	    	}		
        }catch (SQLException ex){
    		System.out.println("[1579117391] Error in " + this.toString() + " class!!");
    	    System.out.println(this.toString() + "SQLException: " + ex.getMessage());
    	    System.out.println(this.toString() + "SQLState: " + ex.getSQLState());
    	    System.out.println(this.toString() + "SQL: " + ex.getErrorCode());
    	    return -1;
    	}
    	
    }
    private boolean add_new_entry(Connection conn){
		if(!validate_entry_fields(conn)){
			return false;
		}
		int iLastEntryNumberInBatch = -1;
		iLastEntryNumberInBatch = get_last_entry_number_in_batch(sBatchNumber(), conn) + 1;
		if (iLastEntryNumberInBatch == -1){
			m_sErrorMessage = "Could not get last entry number.";
			return false;
		}else{
			iEntryNumber(iLastEntryNumberInBatch);
		}

		//Add a new entry:
		String SQL = "";
		SQL = "INSERT INTO "
			+ SMTableentries.TableName
			+ " ("
			+ SMTableentries.ibatchnumber
			+ ", " + SMTableentries.ientrynumber
			+ ", " + SMTableentries.spayeepayor
			+ ", " + SMTableentries.sdocnumber
			+ ", " + SMTableentries.sdocdescription
			+ ", " + SMTableentries.datdocdate
			+ ", " + SMTableentries.stermscode
			+ ", " + SMTableentries.datduedate
			+ ", " + SMTableentries.ilastline
			+ ", " + SMTableentries.scontrolacct
			+ ", " + SMTableentries.sordernumber
			+ ", " + SMTableentries.sentryponumber
			+ ", " + SMTableentries.doriginalamount
			+ ") VALUES ("
			+ sBatchNumber()
			+ ", " + sEntryNumber()
			+ ", '" + clsDatabaseFunctions.FormatSQLStatement(m_sCustomerNumber) + "'"
			+ ", '" + clsDatabaseFunctions.FormatSQLStatement(m_sDocNumber) + "'"
			+ ", '" + clsDatabaseFunctions.FormatSQLStatement(m_sDocDescription) + "'"
			+ ", '" + sSQLDocDate() + "'"
			+ ", '" + clsDatabaseFunctions.FormatSQLStatement(m_sTerms) + "'"
			+ ", '" + sSQLDueDate() + "'"
			+ ", " + sLastLine()
			+ ", '" + clsDatabaseFunctions.FormatSQLStatement(m_sControlAcct) + "'"
			+ ", '" + sOrderNumber() + "'"
			+ ", '" + clsDatabaseFunctions.FormatSQLStatement(m_sPONumber) + "'"
			+ ", " + sOriginalAmountSQLFormat()
			+ ")";

    	try{
	    	if (clsDatabaseFunctions.executeSQL(SQL, conn) == false){
	    		m_sErrorMessage = "Could not complete add new entry.";
	    		return false;
	    	}
    	}catch(SQLException ex){
    		m_sErrorMessage = "Error in " + this.toString() + " class!!";
    		System.out.println("[1579117400] Error in " + this.toString() + " class!!");
    	    System.out.println("SQLException: " + ex.getMessage());
    	    System.out.println("SQLState: " + ex.getSQLState());
    	    System.out.println("SQL: " + ex.getErrorCode());
    	    return false;
    	}
    	
    	//Update the entry ID in this entry, now that we've added it:
    	SQL = TRANSACTIONSQLs.Get_TransactionEntry(sBatchNumber(), sEntryNumber());
    	try{
			ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn); 
			if(!rs.next()){
				m_sErrorMessage = "Could not get entry ID for new entry.";
				rs.close();
				return false;
			}
			m_lTempID = rs.getLong(SMTableentries.lid);
			rs.close();
    	}catch(SQLException ex){
    		m_sErrorMessage = "Error in " + this.toString() + " class!!";
    		System.out.println("[1579117405] Error in " + this.toString() + " class!!");
    	    System.out.println("SQLException: " + ex.getMessage());
    	    System.out.println("SQLState: " + ex.getSQLState());
    	    System.out.println("SQL: " + ex.getErrorCode());
    	    return false;
    	}
    	
    	return true;
    }
    public void remove_zero_amount_lines(){
    	
    	ArrayList<ARLine> tempLineArray = new ArrayList<ARLine>(0);

    	//Copy the lines into the temporary array:
    	for (int i = 0; i < LineArray.size(); i++){
    		tempLineArray.add((ARLine) LineArray.get(i));
    	}
    	
    	//Now copy back ONLY the non-zero amount lines:
    	LineArray.clear();
    	BigDecimal bdzero = new BigDecimal(0);
    	for (int i = 0; i < tempLineArray.size(); i++){
    		ARLine line = (ARLine) tempLineArray.get(i); 
    		if (line.dAmount().compareTo(bdzero) != 0){
    			LineArray.add((ARLine) tempLineArray.get(i));
    		}else{
    			//If it's the first line of an apply-to doc, a zero amount is allowed:
    			if (m_iDocumentType == ARDocumentTypes.APPLYTO){
    				if (i == 0){
    					LineArray.add((ARLine) tempLineArray.get(i));
    				}
    			}
    		}
    	}
    }
    private void assign_line_numbers(){
    	for (int i = 0; i < LineArray.size(); i++){
    		ARLine line = (ARLine) LineArray.get(i);
    		line.iLineNumber(i + 1);
    	}    	
    }
    private boolean delete_removed_lines(Connection conn){
    	
    	//Looks for lines in the database for this entry, which are no longer IN this entry.
    	//If it finds any, the 'delete_single_line_from_database' function is called to remove those:
    	String SQL = TRANSACTIONSQLs.Get_Selected_TransactionLines(sBatchNumber(), sEntryNumber());
    	try{
	    	ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
	    	
	    	while (rs.next()){
	    		//See if each line is still in the entry:
	    		if (! line_exists(rs.getLong(SMTableentrylines.lid))){
	    			if (!delete_single_line_from_database(
	    					Long.toString(rs.getLong(SMTableentrylines.lid)), 
	    					conn)){
	    				m_sErrorMessage = "Error deleting removed line with ID: " + Long.toString(rs.getLong(SMTableentrylines.lid)) + ".";
	    				rs.close();
	    				return false;
	    			}
	    		}
	    	}		
	    	rs.close();
    	}catch (SQLException ex){
    		System.out.println("[1579117410] Error in " + this.toString() + " class!!");
    	    System.out.println("SQLException: " + ex.getMessage());
    	    System.out.println("SQLState: " + ex.getSQLState());
    	    System.out.println("SQL: " + ex.getErrorCode());
    	    m_sErrorMessage = "Could not delete removed lines - " + ex.getMessage();
    	    return false;
    	}
    	
    	return true;
    }
    private boolean update_single_line(ARLine line, Connection conn, String sEntryID){
        String SQL = TRANSACTIONSQLs.Update_TransactionLine_By_ID(
        		line.sId(),
        		sBatchNumber(), 
        		sEntryNumber(), 
        		line.sLineNumber(), 
        		line.sAmountSQLFormat(), 
        		line.sDocAppliedToId(), 
        		sEntryID, 
        		clsDatabaseFunctions.FormatSQLStatement(line.sComment()), 
        		clsDatabaseFunctions.FormatSQLStatement(line.sDescription()), 
        		clsDatabaseFunctions.FormatSQLStatement(line.sDocAppliedTo()), 
        		line.sGLAcct(),
        		line.getApplyToOrderNumber()
        		);
        
    	try{
	    	if (clsDatabaseFunctions.executeSQL(SQL, conn) == false){
	    		m_sErrorMessage = "Could not complete update line with statement: " + SQL;
	    		return false;
	    	}
    	}catch(SQLException ex){
    		System.out.println("[1579117428] Error in " + this.toString() + "update_single_line class!!");
    	    System.out.println("SQLException: " + ex.getMessage());
    	    System.out.println("SQLState: " + ex.getSQLState());
    	    System.out.println("SQL: " + ex.getErrorCode());
    	    m_sErrorMessage = "Could not complete update line - SQL error: " + ex.getMessage();
    	    return false;
    	}
    	return true;
    }
    private boolean add_single_line(ARLine line, Connection conn, String sEntryID){
        String SQL = TRANSACTIONSQLs.Add_TransactionLine(
        		sBatchNumber(), 
        		sEntryNumber(), 
        		line.sLineNumber(), 
        		clsDatabaseFunctions.FormatSQLStatement(line.sDocAppliedTo()), 
        		line.sGLAcct(), 
        		clsDatabaseFunctions.FormatSQLStatement(line.sDescription()), 
        		line.sAmountSQLFormat(), 
        		clsDatabaseFunctions.FormatSQLStatement(line.sComment()), 
        		line.sDocAppliedToId(), 
        		sEntryID,
        		line.getApplyToOrderNumber()
        		);
    	try{
	    	if (clsDatabaseFunctions.executeSQL(SQL, conn) == false){
	    		m_sErrorMessage = "Could not complete add line with statement: " + SQL;
	    		return false;
	    	}
    	}catch(SQLException ex){
    		System.out.println("[1579117416] Error in " + this.toString() + "add_single_line class!!");
    	    System.out.println("SQLException: " + ex.getMessage());
    	    System.out.println("SQLState: " + ex.getSQLState());
    	    System.out.println("SQL: " + ex.getErrorCode());
    	    m_sErrorMessage = "Could not complete add line - SQL error: " + ex.getMessage();
    	    return false;
    	}
    	return true;
    }
    public void checkIfKeyIsUnique(Connection conn) throws Exception{
    	
    	//Make sure there is no open transaction with this customer number and doc number already:
    	try {
    		String SQL = "SELECT *" 
    				+ " FROM " + SMTableartransactions.TableName
    				+ " WHERE ("
    					+ "(" + SMTableartransactions.spayeepayor + " = '" + m_sCustomerNumber + "')"
    					+ " AND (" + SMTableartransactions.sdocnumber + " = '" + m_sDocNumber + "')"
    					+ ")";
    		ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
    		if (rs.next()){
    			rs.close();
    			throw new Exception("There is already an entry with this customer (" 
        				+ m_sCustomerNumber + ") and document number (" + m_sDocNumber + ").");
    		}else{
    			rs.close();
    			return;
    		}
    	}catch (SQLException e){
    		throw new Exception ("Error [1457961954] checking if customer code and doc # is unique - " + e.getMessage());
    	}
    }
    public boolean batchIsOpen(Connection conn){
    	
    	boolean bResult = false;
    	
    	//Make sure the batch is still open:
    	try {
    		String SQL = "SELECT"
    			+ " " + ARBatch.ibatchstatus
    			+ " FROM " + ARBatch.TableName
    			+ " WHERE ("
    				+ ARBatch.ibatchnumber + " = " + m_iBatchNumber
    			+ ")"
    		;
    		ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
    		if (rs.next()){
    			int iStatus = rs.getInt(ARBatch.ibatchstatus);
    			if (
    					(iStatus == SMBatchStatuses.ENTERED)
    					|| (iStatus == SMBatchStatuses.IMPORTED)
    			){
    				bResult = true;
    			}else{
    				m_sErrorMessage = "This batch (" + m_iBatchNumber + ") is no longer open.";
    			}
    		}else{
    			m_sErrorMessage = "Could not read batch to see if it is still open.";
    		}
    		rs.close();
    	}catch (SQLException e){
    		m_sErrorMessage = "Error checking if customer code and doc # is unique - " + e.getMessage();
    	}
    	return bResult;
    }
    public boolean checkIfEntryKeyIsUnique(Connection conn){
    	
    	//Make sure there is no open transaction with this customer number and doc number already:
    	try {
    		String SQL = "SELECT * FROM " + SMTableentries.TableName 
    			+ ", " + SMEntryBatch.TableName 
    			+ " WHERE ("
    				+ "(" + SMTableentries.spayeepayor + " = '" +  m_sCustomerNumber + "')"
    				+ " AND (" + SMTableentries.sdocnumber + " = '" +  m_sDocNumber + "')"
    				+ " AND (" + SMTableentries.TableName + "." + SMTableentries.ibatchnumber 
    					+ " = " +  SMEntryBatch.TableName  + "." + ARBatch.ibatchnumber + ")"
    				+ " AND (" 
    					+"(" + SMEntryBatch.TableName  + "." + ARBatch.ibatchstatus
    						+ " = " + SMBatchStatuses.ENTERED + ")"
    					+ " OR "
    						+"(" + SMEntryBatch.TableName  + "." + ARBatch.ibatchstatus
    						+ " = " + SMBatchStatuses.IMPORTED + ")"
    				+ ")"
    			+ ")"
    			;
    		ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
    		if (rs.next()){
    			rs.close();
    			m_sErrorMessage = "There is already an entry key with this customer (" 
    				+ m_sCustomerNumber + ") and document number (" + m_sDocNumber + ").";
    			return false;
    		}else{
    			rs.close();
    			return true;
    		}
    	}catch (SQLException e){
    		m_sErrorMessage = "SQL error checking for unique document key: " + e.getMessage();
    		return false;
    	}
    }
    public void distributeRemainingAmtToUnappliedCash(ServletContext context, String sDBID){
    	
    	//We only do this for receipts
    	if (m_iDocumentType != ARDocumentTypes.RECEIPT){
    		return;
    	}
    	update_distributed_amount();
    	
    	//If there is an undistributed amount, apply it to 'unapplied cash'
    	if (m_dUnDistributedAmount.compareTo(BigDecimal.ZERO) != 0){
    		
			ARLine line = new ARLine(
					sBatchNumber(), 
					sEntryNumber() 
					);
			line.dAmount(m_dUnDistributedAmount.negate());
			line.lDocAppliedToId(-1L);
			line.lEntryId(lid());
			//line.lId();
			line.sComment("Distributed to unapplied cash");
			line.sDescription("");
			line.sDocAppliedTo("");
			
			ARCustomer cus = new ARCustomer(m_sCustomerNumber);
			line.sGLAcct(cus.getARControlAccount(context, sDBID));
			line.setApplyToOrderNumber("");
			LineArray.add((ARLine) line);
    	}
    	return;
    }
    public String read_out_debug_data(){
    	String sResult = "  ** AREntry read out: ";
    	sResult += "\nbatch: " + sBatchNumber();
    	sResult += "\nentry: " + sEntryNumber();
    	sResult += "\nDistributed amt: " + sDistributedAmountSTDFormat();
    	sResult += "\nbatch type: " + sBatchType();
    	sResult += "\nctl acct: " + sControlAcct();
    	sResult += "\npayeepayor: " + sCustomerNumber();
    	sResult += "\ndocnumber: " + sDocNumber();
    	sResult += "\nordernumber: " + sOrderNumber();
    	sResult += "\nentryponumber: " + sPONumber();
    	sResult += "\ndoctypelabel: " + sDocumentTypeLabel();
    	sResult += "\nlastline: " + sLastLine();
    	sResult += "\nentryid: " + slid();
    	sResult += "\noriginalamt: " + sOriginalAmountSTDFormat();
    	sResult += "\ndocdate: " + sStdDocDate();
    	sResult += "\nduedate: " + sStdDueDate();
    	sResult += "\nterms: " + sTerms();
    	
    	for (int i = 0; i < LineArray.size(); i++){
    		sResult += "\n  ** LINE " + i + ":";
    		ARLine linetest = (ARLine) LineArray.get(i);
    		sResult += "\nbatch: " + linetest.sBatchNumber();
    		sResult += "\nentry: " + linetest.sEntryNumber();
    		sResult += "\nlineno: " + linetest.sLineNumber();
    		sResult += "\namt: " + linetest.sAmountSTDFormat();
    		sResult += "\ncomment: " + linetest.sComment();
    		sResult += "\ndesc: " + linetest.sDescription();
    		sResult += "\ndocappliedto: " + linetest.sDocAppliedTo();
    		sResult += "\ndocappliedtoid: " + linetest.sDocAppliedToId();
    		sResult += "\nentryid: " + linetest.sEntryId();
    		sResult += "\nglacct: " + linetest.sGLAcct();
    		sResult += "\nlineid: " + linetest.sId();
    	}
    	return sResult;
    }
    public String getErrorMessage(){
    	return m_sErrorMessage;
    }
    public void clearError(){
    	m_sErrorMessage = "";
    }
    public void setErrorMessage(String sMsg){
    	m_sErrorMessage = sMsg;
    }
}