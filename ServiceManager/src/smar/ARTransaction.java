package smar;

import SMClasses.*;
import SMDataDefinition.*;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsDateAndTimeConversions;
import ServletUtilities.clsManageBigDecimals;

import java.sql.Date;
import java.util.Calendar;
import java.sql.ResultSet;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.ParseException;

import javax.servlet.ServletContext;

import java.math.BigDecimal;

public class ARTransaction extends java.lang.Object{

	/*
	 * The current amount is the original amount PLUS (because they are opposite signs) the amount of any 
	 * posted transaction lines applied to it.  This does not include lines from UNposted batches.
	 * 
	 * The net amount is the current amount PLUS (because they are opposite signs) the amount of ANY 
	 * transaction lines applied to it.  This DOES include lines from UNposted as well as posted batches.
	 * 
	 * The pending amount is the OPPOSITE of (i.e., -1 *) the total amount of any UNposted transaction 
	 * lines that are applied to it.
	 * 
	 */
	
	/*
	 * OVERVIEW of aropentransactions:
	 * 
	 *   
	 */
	
	private long m_lid;
	private long m_loriginalbatchnumber;
	private long m_loriginalentrynumber;
	private String m_spayeepayor;
	private String m_sdocnumber;
	private int m_idoctype;
	private String m_sterms;
	private java.sql.Date  m_datdocdate;
	private java.sql.Date m_datduedate;
	private BigDecimal m_doriginalamt;
	private BigDecimal m_dcurrentamt;
	private String m_sdocdescription;
	private String m_sordernumber;
	private String m_scontrolacct;
	private long m_lretainage;
	private String m_sponumber;
	private boolean bLoaded;
	private boolean bDirty;
	private String m_sErrorMessage;
	
	//This variable holds the new ID of a transaction after it is first saved.  It is used so that we can
	//update the lines with the correct new ID, but in case the transaction fails, the m_lid in the 
	//transaction and lines doesn't get changed until we reload the transaction.
	private long m_lTempID;
	/*
	 * Definitions of fields:
	 * 
	 * The original amount is the original entered amount for the transaction: for an invoice, it's the invoice
	 * amount, for cash, it's the check or received amount, for an  adjustment, it's the total amount of
	 * the adjustment.  This should ALWAYS be the arithmetic inverse total amount of the lines in the 
	 * ar matching lines table.
	 * (i.e., original amount = (-1 * total amount of the matching lines in the ar matching lines table)
	 * 
	 * The current amount is the original amount PLUS (because they are opposite signs) the amount of any 
	 * armatching transaction lines applied to it.
	 * 
	 */
	
	/*
	 *  For the purpose of calculating and detailing an aging:
	 *  We calculate from the 'aropentransactions' and the 'armatchinglines'.  
	 */
	
	/*
	 * 	Arithmetic signs of entries and lines:
	 *  The signs of the transactions reflect an INCREASE (positive) or DECREASE (negative) in the
	 *  associated customer's liability to the company.  
	 *  
	 *  The matching transaction lines (armatchinglines) would have an OPPOSITE sign and would have to equal the OPPOSITE (i.e., -1 *)
	 *  of the original transaction amount.
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
	 *  are EXACTLY the OPPOSITE of the entry and lines being reversed) OR misc. adjustments. 
	 *  The sign of misc adjustments depends on what kind of adjustment it is (to cash or to
	 *  AR), but the line total is still always the exact opposite of the entry amount (original amount).
	 *  
	 *  AROPENTRANSACTIONS get the same sign as the amount of their matching entry in TRANSACTIONENTRIES, and
	 *  ARMATCHINGLINES get the same sign as the amount of the TRANSACTIONLINE they came from.
	 *  
	 *  TODO - document how GL transactions are created, including how the sign of the entry is determined.
	 */ 
    ARTransaction(
    		String sDocumentID
        ) {
    	try {
    		m_lid = Long.parseLong(sDocumentID);
    	}catch (NumberFormatException e){
    		m_sErrorMessage = "Could not parse document id '" + sDocumentID + "'.";
    		m_lid = -1;
    	}
    	m_loriginalbatchnumber = -1;
    	m_loriginalentrynumber = -1;
    	m_spayeepayor = "";
    	m_sdocnumber = "";
    	m_idoctype = ARDocumentTypes.INVOICE;
    	m_sterms = "";
    	m_datdocdate = clsDateAndTimeConversions.nowAsSQLDate();
    	m_datduedate = clsDateAndTimeConversions.nowAsSQLDate();
    	m_doriginalamt = new BigDecimal(0);
    	m_dcurrentamt = new BigDecimal(0);
    	m_sdocdescription = "";
    	m_sordernumber = "";
    	m_scontrolacct = "";
    	m_lretainage = 0;
    	m_sponumber = "";
    	bLoaded = false;
    	bDirty = false;
    	m_sErrorMessage = "";
    }
    ARTransaction(
        ) {
    	m_lid = -1;
    	m_loriginalbatchnumber = -1;
    	m_loriginalentrynumber = -1;
    	m_spayeepayor = "";
    	m_sdocnumber = "";
    	m_idoctype = ARDocumentTypes.INVOICE;
    	m_sterms = "";
    	m_datdocdate = clsDateAndTimeConversions.nowAsSQLDate();
    	m_datduedate = clsDateAndTimeConversions.nowAsSQLDate();
    	m_doriginalamt = new BigDecimal(0);
    	m_dcurrentamt = new BigDecimal(0);
    	m_sdocdescription = "";
    	m_sordernumber = "";
    	m_scontrolacct = "";
    	m_lretainage = 0;
    	m_sponumber = "";
    	bLoaded = false;
    	bDirty = false;
    	m_sErrorMessage = "";
        }
    public void save_without_data_transaction (Connection conn, String sUserFullName) throws Exception{
    	//NOTE: to get the correct transaction ID and line ID's, the transaction muse be re-loaded after
    	//saving!
    	
    	//Now we'll validate, and save the transaction:
    	try {
			validate_transaction_fields(conn, sUserFullName);
		} catch (Exception e) {
			throw new Exception("Could not save - " + e.getMessage());
		}
    	/*
    	 * If it's a NEW record, we have to save the entry first, to get the entry id.
    	 * Next, we validate the lines, then the Entries, then save the lines, then update the entry,
    	 * then update the batch. 
    	*/
    	
    	//sTransactionID will contain the ID we save the transaction with.  If it's a current transaction,
    	//it will just contain m_lid.  But if it's a new transaction, we'll want it to hold the transaction
    	//ID we got when we inserted the transaction record.  That's so we can roll back the data transaction
    	//if necessary, and still have the original ID (-1):
    	String sTransactionID = Long.toString(m_lid);
    	if(m_lid == -1){
    		try {
				add_new_transaction(conn, sUserFullName);
			} catch (Exception e) {
				throw new Exception("Error adding new transation - " + e.getMessage());
			}
   			sTransactionID = Long.toString(m_lTempID);
    	} //end if(m_lentrynumber == -1)

    	//Now save the transaction:
        String SQL = "";
        SQL =  "UPDATE " + SMTableartransactions.TableName + " SET "
        		
        		+ SMTableartransactions.datdocdate + " = '" + getSQLDocDate() + "'"
        		+ ", " + SMTableartransactions.datduedate + " = '" + getSQLDueDate() + "'"
        		+ ", " + SMTableartransactions.dcurrentamt + " = " + getCurrentAmountSQLFormat()
        		+ ", " + SMTableartransactions.doriginalamt + " = " + getOriginalAmountSQLFormat()
        		+ ", " + SMTableartransactions.idoctype + " = " + getsDocType()
        		+ ", " + SMTableartransactions.loriginalbatchnumber + " = " + getsOriginalBatchNumber()
        		+ ", " + SMTableartransactions.loriginalentrynumber + " = " + getsOriginalEntryNumber()
        		+ ", " + SMTableartransactions.sdocdescription + " = '" + clsDatabaseFunctions.FormatSQLStatement(getDocDescription()) + "'"
        		+ ", " + SMTableartransactions.sdocnumber + " = '" + clsDatabaseFunctions.FormatSQLStatement(getDocNumber()) + "'"
        		+ ", " + SMTableartransactions.sordernumber + " = '" + clsDatabaseFunctions.FormatSQLStatement(getOrderNumber()) + "'"
        		+ ", " + SMTableartransactions.spayeepayor + " = '" + clsDatabaseFunctions.FormatSQLStatement(getCustomerNumber()) + "'"
        		+ ", " + SMTableartransactions.sterms + " = '" + getTerms() + "'"
        		+ ", " + SMTableartransactions.iretainage + " = " + Long.toString(m_lretainage)
        		+ ", " + SMTableartransactions.sponumber + " = '" + clsDatabaseFunctions.FormatSQLStatement(getPONumber()) + "'"

        		+ " WHERE (" 
        			+ "(" + SMTableartransactions.lid + " = " + sTransactionID + ")"
        			+ ")";
        try {
	    	if (clsDatabaseFunctions.executeSQL(SQL, conn) == false){
	    		throw new Exception("Could not complete update transaction - transaction was not updated.");
	    	}
    	}catch(SQLException ex){
    		throw new Exception("Could not save transaction - SQL error: " + ex.getMessage());
    	}
    	
    	return;
    }
    public boolean load (
    		ServletContext context, 
    		String sDBID
    		){
    
    	return load (getsTransactionID(), context, sDBID);
    }
    public boolean load (
    	String sTransactionID,
    	ServletContext context, 
		String sDBID
    ){
    	    String SQL = "SELECT *" 
    	    		+ " FROM " + SMTableartransactions.TableName
    	    		+ " WHERE ("
    	    			+ "(" + SMTableartransactions.lid + " = " + sTransactionID + ")";
    	    		SQL += ")";
    		try {
    			ResultSet rs = clsDatabaseFunctions.openResultSet(
    					SQL, 
    					context, 
    					sDBID,
    					"MySQL",
    					this.toString() + ".load"); 
    			rs.next();

    			//Load the variables:
    			m_lid = Long.parseLong(sTransactionID);
    			m_loriginalbatchnumber = rs.getLong(SMTableartransactions.loriginalbatchnumber);
    			m_loriginalentrynumber = rs.getLong(SMTableartransactions.loriginalentrynumber);
    			m_spayeepayor = rs.getString(SMTableartransactions.spayeepayor);
    			m_sdocnumber = rs.getString(SMTableartransactions.sdocnumber);
    			m_idoctype = rs.getInt(SMTableartransactions.idoctype);
    			m_sterms = rs.getString(SMTableartransactions.sterms);
    			setDocDateWithDate(rs.getDate(SMTableartransactions.datdocdate));
    			setDueDateWithDate(rs.getDate(SMTableartransactions.datduedate));
    			m_doriginalamt = rs.getBigDecimal(SMTableartransactions.doriginalamt);
    			m_dcurrentamt = rs.getBigDecimal(SMTableartransactions.dcurrentamt);
    			m_sdocdescription = rs.getString(SMTableartransactions.sdocdescription);
    			m_sordernumber = rs.getString(SMTableartransactions.sordernumber);
    			m_scontrolacct = rs.getString(SMTableartransactions.scontrolacct);
    			m_lretainage = rs.getLong(SMTableartransactions.iretainage);
    			m_sponumber = rs.getString(SMTableartransactions.sponumber);
    			rs.close();
    		}catch (SQLException ex){
    			m_sErrorMessage = "Error loading AR transaction - with SQL: " + SQL + " - " + ex.getMessage();
    	        return false;
    		}

        	return true;
    }
    public void load (
        	String sTransactionID,
        	Connection conn
        )throws Exception{
        	    String SQL = "SELECT *" 
        	    		+ " FROM " + SMTableartransactions.TableName
        	    		+ " WHERE ("
        	    			+ "(" + SMTableartransactions.lid + " = " + sTransactionID + ")";
        	    		SQL += ")";
        		try {
        			ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn); 
        			rs.next();

        			//Load the variables:
        			m_lid = Long.parseLong(sTransactionID);
        			m_loriginalbatchnumber = rs.getLong(SMTableartransactions.loriginalbatchnumber);
        			m_loriginalentrynumber = rs.getLong(SMTableartransactions.loriginalentrynumber);
        			m_spayeepayor = rs.getString(SMTableartransactions.spayeepayor);
        			m_sdocnumber = rs.getString(SMTableartransactions.sdocnumber);
        			m_idoctype = rs.getInt(SMTableartransactions.idoctype);
        			m_sterms = rs.getString(SMTableartransactions.sterms);
        			setDocDateWithDate(rs.getDate(SMTableartransactions.datdocdate));
        			setDueDateWithDate(rs.getDate(SMTableartransactions.datduedate));
        			m_doriginalamt = rs.getBigDecimal(SMTableartransactions.doriginalamt);
        			m_dcurrentamt = rs.getBigDecimal(SMTableartransactions.dcurrentamt);
        			m_sdocdescription = rs.getString(SMTableartransactions.sdocdescription);
        			m_sordernumber = rs.getString(SMTableartransactions.sordernumber);
        			m_scontrolacct = rs.getString(SMTableartransactions.scontrolacct);
        			m_lretainage = rs.getLong(SMTableartransactions.iretainage);
        			m_sponumber = rs.getString(SMTableartransactions.sponumber);
        			rs.close();
        		}catch (SQLException ex){
        			throw new Exception("Error loading AR transaction with SQL: " + SQL + " - " + ex.getMessage());
        		}

            	return;
        }

    public boolean load (
    		Connection conn
    		){

   		return load (getCustomerNumber(), getDocNumber(), conn);
    }
    public boolean load (
    		String sCustomerNumber,
    		String sDocNumber,
    		ServletContext context,
    		String sDBID
    		){

    	Connection conn = clsDatabaseFunctions.getConnection(
    		context, 
    		sDBID,
    		"MySQL",
    		this.toString() + ".load");
    	if (conn == null){
    		return false;
    	}
    	
    	if (load (sCustomerNumber, sDocNumber, conn)){
    		clsDatabaseFunctions.freeConnection(context, conn, "[1547067573]");
    		return true;
    	}else{
    		clsDatabaseFunctions.freeConnection(context, conn, "[1547067574]");
    		return false;
    	}
    }
    public boolean load (
    		String sCustomer,
    		String sDocNumber,
        	Connection conn
        ){
        	    String SQL = "SELECT *" 
        	    		+ " FROM " + SMTableartransactions.TableName
        	    		+ " WHERE ("
        	    			+ "(" + SMTableartransactions.spayeepayor + " = '" + sCustomer+ "')"
        	    			+ " AND (" + SMTableartransactions.sdocnumber + " = '" + sDocNumber + "')"
        	    			+ ")";
        		try {
        			ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn); 
        			if(!rs.next()){
        				m_sErrorMessage = "Could not load ar opentransaction for customer: " 
        					+ sCustomer + ", document number: " + sDocNumber + ".";
        				rs.close();
        				return false;
        			}

        			//Load the variables:
        			m_lid = rs.getLong(SMTableartransactions.lid);
        			m_loriginalbatchnumber = rs.getLong(SMTableartransactions.loriginalbatchnumber);
        			m_loriginalentrynumber = rs.getLong(SMTableartransactions.loriginalentrynumber);
        			m_spayeepayor = rs.getString(SMTableartransactions.spayeepayor);
        			m_sdocnumber = rs.getString(SMTableartransactions.sdocnumber);
        			m_idoctype = rs.getInt(SMTableartransactions.idoctype);
        			m_sterms = rs.getString(SMTableartransactions.sterms);
        			setDocDateWithDate(rs.getDate(SMTableartransactions.datdocdate));
        			setDueDateWithDate(rs.getDate(SMTableartransactions.datduedate));
        			m_doriginalamt = rs.getBigDecimal(SMTableartransactions.doriginalamt);
        			m_dcurrentamt = rs.getBigDecimal(SMTableartransactions.dcurrentamt);
        			m_sdocdescription = rs.getString(SMTableartransactions.sdocdescription);
        			m_sordernumber = rs.getString(SMTableartransactions.sordernumber);
        			m_scontrolacct = rs.getString(SMTableartransactions.scontrolacct);
        			m_lretainage = rs.getLong(SMTableartransactions.iretainage);
        			m_sponumber = rs.getString(SMTableartransactions.sponumber);
        			rs.close();
        		}catch (SQLException ex){
        			m_sErrorMessage = "Error loading AR transaction with SQL: " + SQL + " - " + ex.getMessage();
        	        return false;
        		}

       			return true;	
        }

    public String getsTransactionID (){
    	return Long.toString(m_lid);
    }
    public long getlTransactionID (){
    	return m_lid;
    }
    public boolean setTransactionID(String slid){
    	try{
    		m_lid = Integer.parseInt(slid);
    		return true;
    	}catch (NumberFormatException e){
    		m_sErrorMessage = "Error formatting entry number from string: " + slid + ".";
    		return false;
    	}
    }
    public void setTransactionID (long lid){
    	m_lid = lid;
    }
    public void setBatchNumber (long iBatchNumber){
    	m_loriginalbatchnumber = iBatchNumber;
    }
    public long getlBatchNumber (){
    	return m_loriginalbatchnumber;
    }
    public boolean setBatchNumber (String sBatchNumber){
    	try{
    		m_loriginalbatchnumber = Long.parseLong(sBatchNumber);
    		return true;
    	}catch (NumberFormatException e){
    		m_sErrorMessage = "Error formatting batch number from string: " + sBatchNumber + ".";
    		return false;
    	}
    }
    public String getsOriginalBatchNumber (){
    	return Long.toString(m_loriginalbatchnumber);
    }
    public void setEntryNumber (int iEntryNumber){
    	m_loriginalentrynumber = iEntryNumber;
    }
    public long getlEntryNumber (){
    	return m_loriginalentrynumber;
    }
    public boolean setEntryNumber (String sEntryNumber){
    	try{
    		m_loriginalentrynumber = Long.parseLong(sEntryNumber);
    		return true;
    	}catch (NumberFormatException e){
    		m_sErrorMessage = "Error formatting Entry number from string: " + sEntryNumber + ".";
    		return false;
    	}
    }
    public String getsOriginalEntryNumber (){
    	return Long.toString(m_loriginalentrynumber);
    }
    public void setDocType (int iDocumentType){
    	m_idoctype = iDocumentType;
    }
    public boolean setDocType (String sDocumentType){
    	
    	int i;
    	try{
    		i = Integer.parseInt(sDocumentType);
    		m_idoctype = i;
    		return true;
    	}catch (NumberFormatException e){
    		m_sErrorMessage = "Error formatting document type from string: " + sDocumentType + ".";
    		return false;
    	}
    }
    public String getsDocType (){
    	return Integer.toString(m_idoctype);
    }
    public int getiDocType (){
    	return m_idoctype;
    }
    public String getDocumentTypeLabel(){
    	return ARDocumentTypes.Get_Document_Type_Label(m_idoctype);
    }
    public boolean setCustomerNumber (String sCustomerNumber){
    	
    	//Only Misc Receipts don't require customer numbers:
    	if (m_idoctype != ARDocumentTypes.MISCRECEIPT){
	    	if (sCustomerNumber.length() == 0){
	    		m_sErrorMessage = "Customer number is empty.";
	    		return false;
	    	}
    	}
    	if (sCustomerNumber.length() > SMTableentries.spayeepayorLength){
    		m_sErrorMessage = "Customer number is too long.";
    		return false;
    	}
    	
    	m_spayeepayor = sCustomerNumber;
    	return true;
    }
    public String getCustomerNumber (){
    	return m_spayeepayor;
    }
    public boolean setDocNumber (String sDocNumber){
    	if (sDocNumber.length() == 0){
    		return false;
    	}else{
    		m_sdocnumber = sDocNumber;
    		return true;
    	}
    }
    public String getDocNumber (){
    	return m_sdocnumber;
    }
    public boolean setDocDescription (String sDocDescription){
    	m_sdocdescription = sDocDescription;
    	return true;
    }
    public String getDocDescription (){
    	return m_sdocdescription;
    }
    public boolean setDocDateWithDate (Date datDocDate){
    	if (! clsDateAndTimeConversions.IsValidDate(datDocDate)){
    		return false;
    	}else{
    		m_datdocdate = datDocDate;
    		return true;
    	}
    }
    public boolean setDocDateWithStrings (String sYear, String sMonth, String sDay){
    	
		try {
			m_datdocdate = clsDateAndTimeConversions.StringTojavaSQLDate("MM/dd/yyyy", sMonth + "/" + sDay + "/" + sYear);
		} catch (ParseException e) {
			m_sErrorMessage = "Error:[1423842432] Invalid document date: '" 
			+ sMonth + "/" + sDay + "/" + sYear + "' - " + e.getMessage();
			return false;
		}
		return true;
    }
    public boolean setDocDate (String sFormat, String sDocDate){
		try {
			m_datdocdate = clsDateAndTimeConversions.StringTojavaSQLDate(sFormat, sDocDate);
		} catch (ParseException e) {
			m_sErrorMessage = "Error:[1423842433] Invalid document date: '" 
			+ sDocDate + "' - " + e.getMessage();
			return false;
		}
		return true;
    }
    public String getStdDocDate (){
    	return clsDateAndTimeConversions.utilDateToString(m_datdocdate, "MM/dd/yyyy");
    }
    public String getSQLDocDate (){
    	return clsDateAndTimeConversions.utilDateToString(m_datdocdate, "yyyy-MM-dd");
    }
    public java.sql.Date getDocDate (){
    	return m_datdocdate;
    }
    public Calendar getcalDocDate (){
    	return clsDateAndTimeConversions.sqlDateToCalendar(m_datdocdate);
    }
    public boolean setTerms (String sTerms){
    	if (sTerms.equalsIgnoreCase("")){
    		return false;
    	}
    	m_sterms = sTerms;
    	return true;
    }
    public String getTerms (){
    	return m_sterms;
    }
    public boolean setOrderNumber (String sOrderNumber){
    	if (sOrderNumber.length() > SMTableartransactions.sordernumberlength){
    		m_sErrorMessage = "Ordernumber is too long.";
    		return false;
    	}
    	m_sordernumber = sOrderNumber;
    	return true;
    }
    public String getOrderNumber (){
    	return m_sordernumber;
    }
    public boolean setControlAcct (String sControlAcct){
    	if (sControlAcct.length() > SMTableartransactions.scontrolacctlength){
    		m_sErrorMessage = "Control account is too long.";
    		return false;
    	}
    	m_scontrolacct = sControlAcct;
    	return true;
    }
    public String getControlAcct (){
    	return m_scontrolacct;
    }
    private boolean setDueDateWithDate (Date datDueDate){
    	if (! clsDateAndTimeConversions.IsValidDate(datDueDate)){
    		return false;
    	}else{
    		m_datduedate = datDueDate;
    		return true;
    	}
    }
    public boolean setDueDateWithStrings (String sYear, String sMonth, String sDay){
		try {
			m_datduedate = clsDateAndTimeConversions.StringTojavaSQLDate("MM/dd/yyyy", sMonth + "/" + sDay + "/" + sYear);
		} catch (ParseException e) {
			m_sErrorMessage = "Error:[1423842434] Invalid due date: '" 
					+ sMonth + "/" + sDay + "/" + sYear + "' - " + e.getMessage();
			return false;
		}
		return true;
    }
    public boolean setDueDate (String sFormat, String sDueDate){
		try {
			m_datduedate = clsDateAndTimeConversions.StringTojavaSQLDate(sFormat, sDueDate);
		} catch (ParseException e) {
			m_sErrorMessage = "Error:[1423842435] Invalid due date: '" 
					+ sDueDate + "' - " + e.getMessage();
			return false;
		}
		return true;
    }
    public Calendar getcalDueDate (){
    	return clsDateAndTimeConversions.sqlDateToCalendar(m_datduedate);
    }
    public String getStdDueDate (){
    	return clsDateAndTimeConversions.utilDateToString(m_datduedate, "MM/dd/yyyy");
    }
    public String getSQLDueDate (){
    	return clsDateAndTimeConversions.utilDateToString(m_datduedate, "yyyy-MM-dd");
    }
    public java.sql.Date getDueDate (){
    	return m_datduedate;
    }
    public void setOriginalAmount (BigDecimal dOriginalAmount){
    	m_doriginalamt = dOriginalAmount;
    }
    public BigDecimal getdOriginalAmount (){
    	return m_doriginalamt;
    }
    public boolean setOriginalAmount (String sOriginalAmount){
    	try{
    		sOriginalAmount = sOriginalAmount.replace(",", "");
    		BigDecimal bd = new BigDecimal(sOriginalAmount);
    		bd = bd.setScale(2, BigDecimal.ROUND_HALF_UP);
    		m_doriginalamt =  bd;
    		return true;
    	}catch (NumberFormatException e){
    		m_sErrorMessage = "Error converting Original amount from string: " + sOriginalAmount + ".";
    		return false;
    	}
    }
    public String getOriginalAmountSTDFormat (){
    	return clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(m_doriginalamt);
    }
    public String getOriginalAmountSQLFormat (){
    	return clsManageBigDecimals.BigDecimalTo2DecimalSQLFormat(m_doriginalamt);
    }
    public void setCurrentAmount (BigDecimal dCurrentAmount){
    	m_dcurrentamt = dCurrentAmount;
    }
    public BigDecimal getdCurrentAmount (){
    	return m_dcurrentamt;
    }
    public boolean setCurrentAmount (String sCurrentAmount){
    	try{
    		sCurrentAmount = sCurrentAmount.replace(",", "");
    		BigDecimal bd = new BigDecimal(sCurrentAmount);
    		bd = bd.setScale(2, BigDecimal.ROUND_HALF_UP);
    		m_dcurrentamt = bd;
    		return true;
    	}catch (NumberFormatException e){
    		m_sErrorMessage = "Error converting Current amount from string: " + sCurrentAmount + ".";
    		return false;
    	}
    }
    public String getCurrentAmountSTDFormat (){
    	return clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(m_dcurrentamt);
    }
    public String getCurrentAmountSQLFormat (){
    	return clsManageBigDecimals.BigDecimalTo2DecimalSQLFormat(m_dcurrentamt);
    }
    public void setRetainageFlag(long lRetainageFlag){
    	m_lretainage = lRetainageFlag;
    }
    public long getRetainageFlag(){
    	return m_lretainage;
    }
    public boolean setPONumber (String sPONumber){
    	if (sPONumber.length() > SMTableartransactions.sponumberlength){
    		m_sErrorMessage = "PO number is too long.";
    		return false;
    	}
    	m_sponumber = sPONumber;
    	return true;
    }
    public String getPONumber (){
    	return m_sponumber;
    }
    public boolean validate_transaction_fields (Connection conn, String sUserID) throws Exception{
        //Validate the entries here:
    	//Don't need customer numbers on misc adjustments:
    	if ((m_idoctype != ARDocumentTypes.MISCRECEIPT)
    		&& (m_idoctype != ARDocumentTypes.REVERSAL)
    			){ 
	        if (m_spayeepayor.length() == 0){
	        	m_sErrorMessage = "Customer number is empty.";
	        	return false;
	        }
    	}
	    if (m_spayeepayor.length() > SMTableentries.spayeepayorLength){
        	m_sErrorMessage = "Customer number is too long.";
        	return false;
        }

        if (m_sdocnumber.length() == 0){
        	m_sErrorMessage = "Document number is empty.";
        	return false;
        }
        if (m_sdocnumber.length() > SMTableentries.sdocnumberLength){
        	m_sErrorMessage = "Document number is too long.";
        	return false;
        }
        
        //Make sure the date is in the posting period range:
        SMOption opt = new SMOption();
        try {
			opt.checkDateForPosting(
				getStdDocDate(), 
				"Entry date for entry number " + Long.toString(getlEntryNumber()), 
				conn,
				sUserID
				);
		} catch (Exception e) {
        	m_sErrorMessage = e.getMessage();
        	return false;
		}
        
    	return true;
    }
    private void add_new_transaction(Connection conn, String sUserFullName) throws Exception{
		try {
			validate_transaction_fields(conn, sUserFullName);
		} catch (Exception e) {
			throw new Exception("Error adding transaction - " + e.getMessage());
		}

		//Add a new entry:
		String SQL = "INSERT INTO " + SMTableartransactions.TableName + " ("
				
				+ SMTableartransactions.datdocdate
				+ ", " + SMTableartransactions.datduedate
				+ ", " + SMTableartransactions.dcurrentamt
				+ ", " + SMTableartransactions.doriginalamt
				+ ", " + SMTableartransactions.idoctype
				+ ", " + SMTableartransactions.loriginalbatchnumber
				+ ", " + SMTableartransactions.loriginalentrynumber
				+ ", " + SMTableartransactions.sdocdescription
				+ ", " + SMTableartransactions.sdocnumber
				+ ", " + SMTableartransactions.sordernumber
				+ ", " + SMTableartransactions.spayeepayor
				+ ", " + SMTableartransactions.sterms
				+ ", " + SMTableartransactions.scontrolacct
				+ ", " + SMTableartransactions.iretainage
				+ ", " + SMTableartransactions.sponumber

				+ ") VALUES ("
				+ "'" + getSQLDocDate() + "'"
				+ ", '" + getSQLDueDate() + "'"
				+ ", " + getCurrentAmountSQLFormat()
				+ ", " + getOriginalAmountSQLFormat()
				+ ", " + getsDocType()
				+ ", " + getsOriginalBatchNumber()
				+ ", " + getsOriginalEntryNumber()
				+ ", '" + clsDatabaseFunctions.FormatSQLStatement(getDocDescription()) + "'"
				+ ", '" + clsDatabaseFunctions.FormatSQLStatement(getDocNumber()) + "'"
				+ ", '" + clsDatabaseFunctions.FormatSQLStatement(getOrderNumber()) + "'"
				+ ", '" + clsDatabaseFunctions.FormatSQLStatement(getCustomerNumber()) + "'"
				+ ", '" + clsDatabaseFunctions.FormatSQLStatement(getTerms()) + "'"
				+ ", '" + getControlAcct() + "'"
				+ ", " + Long.toString(m_lretainage)
				+ ", '" + clsDatabaseFunctions.FormatSQLStatement(getPONumber()) + "'" 
				+ ")";

    	try{
	    	if (clsDatabaseFunctions.executeSQL(SQL, conn) == false){
	    		throw new Exception("Could not complete add new transaction. " + SQL);
	    	}else{
	    	}
    	}catch(SQLException ex){
    		throw new Exception("Error adding new AR transaction with SQL: " + SQL + " - " + ex.getMessage());
    	}
    	
    	//Update the transaction ID in this transaction, now that we've added it:
    	SQL ="SELECT *" 
    			+ " FROM " + SMTableartransactions.TableName
    			+ " WHERE ("
    				+ "(" + SMTableartransactions.spayeepayor + " = '" + m_spayeepayor + "')"
    				+ " AND (" + SMTableartransactions.sdocnumber + " = '" + m_sdocnumber + "')"
    				+ ")"; 
    	try{
			ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn); 
			if(!rs.next()){
				rs.close();
				throw new Exception("Could not get transaction ID for new transaction. " + SQL);
			}
			//Store the ID here, temporarily, in case the transaction fails and we have to rollback:
			m_lTempID = (rs.getLong(SMTableartransactions.lid));
			rs.close();
    	}catch(SQLException ex){
    		throw new Exception("Error getting open transactions by customer and docnumber with SQL: " + SQL + " - " + ex.getMessage());
    	}
    	return;
    }
    public BigDecimal getNetAmount(ServletContext context, String sDBID){
    	
    	BigDecimal bdPending = BigDecimal.ZERO;
    	
    	String SQL = "SELECT SUM(" + SMTableentrylines.damount + ") AS pendingtotal"
    		+ " FROM " + SMTableentrylines.TableName + ", " + SMEntryBatch.TableName
    		+ " WHERE ("
    			+ "(" + SMEntryBatch.TableName + "." + SMEntryBatch.ibatchnumber + " = "
    				+ SMTableentrylines.TableName + "." + SMTableentrylines.ibatchnumber + ")"
    			+ " AND (" 
    				+ "(" + SMEntryBatch.TableName + "." + SMEntryBatch.ibatchstatus + " = " + SMBatchStatuses.ENTERED + ")"
    				+ " OR (" + SMEntryBatch.TableName + "." + SMEntryBatch.ibatchstatus + " = " + SMBatchStatuses.IMPORTED + ")"
    			+ ")"
    			+ " AND (" + SMTableentrylines.ldocappliedtoid + " != -1)"
    			+ " AND (" + SMTableentrylines.ldocappliedtoid + " = " + Long.toString(m_lid) + ")"
    		+ ")"
    		;
    	
    	try{
    		ResultSet rs = clsDatabaseFunctions.openResultSet(
    				SQL, 
    				context, 
    				sDBID,
    				"MySQL",
    				this.toString() + ".getNetAmount");
    		if(rs.next()){
    			if (rs.getBigDecimal("pendingtotal") != null){
    				bdPending = rs.getBigDecimal("pendingtotal");
    			}
    		}
    		rs.close();
    	}catch (SQLException e){
    		m_sErrorMessage = "Error getting net amount for transaction ID " + Long.toString(m_lid) + " - " + e.getMessage();
    	}
    	return m_dcurrentamt.subtract(bdPending);
    }
    public String read_out_debug_data(){
    	String sResult = "  ** AROpenTransaction read out: ";
    	sResult += "\nlid: " + getsTransactionID();
    	sResult += "\nlbatchnumber: " + getsOriginalBatchNumber();
    	sResult += "\nlentrynumber: " + getsOriginalEntryNumber();
    	sResult += "\nspayeepayor: " + getCustomerNumber();
    	sResult += "\nsdocnumber: " + getDocNumber();
    	sResult += "\nidoctype: " + getsDocType();
    	sResult += "\nsterms: " + getTerms();
    	sResult += "\ndatdocdate: " + getStdDocDate();
    	sResult += "\ndatduedate: " + getStdDueDate();
    	sResult += "\ndoriginalamt: " + getOriginalAmountSTDFormat();
    	sResult += "\ndcurrentamt: " + getCurrentAmountSTDFormat();
    	sResult += "\nsdocdescription: " + getDocDescription();
    	sResult += "\nsordernumber: " + getOrderNumber();
    	sResult += "\niretainage: " + Long.toString(m_lretainage);
    	sResult += "\nsponumber: " + getPONumber();
    	sResult += "\nbLoaded: " + Boolean.toString(bLoaded);
    	sResult += "\nbDirty: " + Boolean.toString(bDirty);
    	sResult += "\nsErrorMessage: " + getErrorMessage();
    	
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