package SMClasses;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

import javax.servlet.ServletContext;
import SMDataDefinition.SMTabledoingbusinessasaddresses;
import SMDataDefinition.SMTableinvoicedetails;
import SMDataDefinition.SMTableinvoiceheaders;
import SMDataDefinition.SMTabletax;
import ServletUtilities.clsMasterEntry;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsDateAndTimeConversions;
import ServletUtilities.clsManageBigDecimals;
import ServletUtilities.clsStringFunctions;
import smar.SMOption;
import smcontrolpanel.SMSalesOrderTaxCalculator;

public class SMInvoice extends clsMasterEntry{//java.lang.Object{

	public final static int INVOICE_NUMBER_LENGTH_USED = 8;
	public final static int TransactionTypeInvoice = 0;
	public final static int TransactionTypeCredit = 1;
	
	private java.sql.Date m_datInvoiceDate;
	private BigDecimal m_dDiscountAmount;
	private BigDecimal m_dSalesTaxAmount;
	private int m_iCustomerDiscountLevel;
	private int m_iNumberOfLinesOnInvoice;
	private int m_iRequisitionDueDay;
	private String m_mInvoiceComments;
	private String m_sBillToAddressLine1;
	private String m_sBillToAddressLine2;
	private String m_sBillToAddressLine3;
	private String m_sBillToAddressLine4;
	private String m_sBillToCity;
	private String m_sBillToContact;
	private String m_sBillToCountry;
	private String m_sBillToFax;
	private String m_sBillToName;
	private String m_sBillToPhone;
	private String m_sBillToState;
	private String m_sBillToZip;
	private String m_sCustomerCode;
	private String m_sDefaultPriceListCode;
	private String m_sInvoiceNumber;
	private String m_sLocation;
	private String m_sOrderNumber;
	private String m_sPONumber;
	private String m_sSalesperson;
	private String m_sServiceTypeCode;
	private String m_sServiceTypeCodeDescription;
	private String m_sShipToAddress1;
	private String m_sShipToAddress2;
	private String m_sShipToAddress3;
	private String m_sShipToAddress4;
	private String m_sShipToCity;
	private String m_sShipToCode;
	private String m_sShipToContact;
	private String m_sShipToCountry;
	private String m_sShipToFax;
	private String m_sShipToName;
	private String m_sShipToPhone;
	private String m_sShipToState;
	private String m_sShipToZip;
	private String m_sTaxExemptNumber;
	private String m_staxjurisdiction;
	private String m_sTerms;
	private BigDecimal m_dDiscountPercentage;
	private String m_sDesc;
	private String m_sCustomerControlAcctSet;
	private java.sql.Date m_datDueDate;
	private java.sql.Date m_datTermsDiscountDate;
	private BigDecimal m_dTermsDiscountPercentage;
	private BigDecimal m_dTermsDiscountAvailable;
	private int m_iExportedToAR;
	private int m_iDayEndNumber;
	private java.sql.Date m_datOrderDate;
	private BigDecimal m_dTaxRate;
	private BigDecimal m_dSalesTaxBase;
	private BigDecimal m_dPrePayment;
	private String m_sDiscountDesc;
	private int m_iTransactionType;
	private String m_sMatchingInvoiceNumber;
	private int m_iIsCredited;
	private int m_iExportedToIC;
	private String m_sCreatedByFullName;
	private Long m_lCreatedByID;
	private String m_sInvoiceCreationDate;
	private int m_iOrderSourceID;
	private String m_sOrderSourceDesc;
	private int m_iSalesGroup;
	private String m_strimmedordernumber;
	private String m_itaxid;
	private String m_staxtype;
	private String m_icalculatetaxonpurchaseorsale;
	private String m_icalculatetaxoncustomerinvoice;
	private String m_sdbalogo;
	private String m_sdbadescription;
	private String m_mdbaaddress;
	private String m_mdbaremittoaddress;
	private String m_mdbainvoicelogo;
	
	private ArrayList<SMInvoiceDetail> LineArray;
	private String m_sErrorMessage;
	private String m_sSMOptionLastEditDate;
	private String m_sSMOptionLastEditTime;
	private String m_sSMOptionLastEditUserID;
	private String m_sSMOptionLastEditProcess;

	public SMInvoice(
        ) {
		m_datInvoiceDate = clsDateAndTimeConversions.nowAsSQLDate();
		m_dDiscountAmount = new BigDecimal(0);
		m_dSalesTaxAmount = new BigDecimal(0);
		m_iCustomerDiscountLevel = 0;
		m_iNumberOfLinesOnInvoice = 0;
		m_iRequisitionDueDay = 0;
		//m_iTaxClass = 0;
		m_mInvoiceComments = "";
		m_sBillToAddressLine1 = "";
		m_sBillToAddressLine2 = "";
		m_sBillToAddressLine3 = "";
		m_sBillToAddressLine4 = "";
		m_sBillToCity = "";
		m_sBillToContact = "";
		m_sBillToCountry = "";
		m_sBillToFax = "";
		m_sBillToName = "";
		m_sBillToPhone = "";
		m_sBillToState = "";
		m_sBillToZip = "";
		m_sCustomerCode = "";
		m_sDefaultPriceListCode = "";
		m_sInvoiceNumber = "";
		m_sLocation = "";
		m_sOrderNumber = "";
		m_sPONumber = "";
		m_sSalesperson = "";
		m_sServiceTypeCode = "";
		m_sServiceTypeCodeDescription = "";
		m_sShipToAddress1 = "";
		m_sShipToAddress2 = "";
		m_sShipToAddress3 = "";
		m_sShipToAddress4 = "";
		m_sShipToCity = "";
		m_sShipToCode = "";
		m_sShipToContact = "";
		m_sShipToCountry = "";
		m_sShipToFax = "";
		m_sShipToName = "";
		m_sShipToPhone = "";
		m_sShipToState = "";
		m_sShipToZip = "";
		m_sTaxExemptNumber = "";
		m_staxjurisdiction = "";
		m_sTerms = "";
		m_dDiscountPercentage = new BigDecimal(0);
		m_sDesc = "";
		m_sCustomerControlAcctSet = "";
		m_datDueDate = clsDateAndTimeConversions.nowAsSQLDate();
		m_datTermsDiscountDate = clsDateAndTimeConversions.nowAsSQLDate();
		m_dTermsDiscountPercentage = new BigDecimal(0);
		m_dTermsDiscountAvailable = new BigDecimal(0);
		m_iExportedToAR = 0;
		m_iDayEndNumber = 0;
		m_datOrderDate = clsDateAndTimeConversions.nowAsSQLDate();
		m_dTaxRate = new BigDecimal(0);
		m_dSalesTaxBase = new BigDecimal(0);
		m_dPrePayment = new BigDecimal(0);
		m_sDiscountDesc = "";
		m_iTransactionType = 0;
		m_sMatchingInvoiceNumber = "";
		m_iIsCredited = 0;
		m_iExportedToIC = 0;
		m_sCreatedByFullName = "";
		m_lCreatedByID = 0L;
		m_sInvoiceCreationDate = clsDateAndTimeConversions.now("yyyy-MM-dd hh:mm:ss a");
		m_iOrderSourceID = 0;
		m_sOrderSourceDesc = "";
		m_iSalesGroup = 0;
		m_strimmedordernumber = "";
		m_itaxid = "";
		m_staxtype = "";
		m_icalculatetaxonpurchaseorsale = "";
		m_icalculatetaxoncustomerinvoice = "";
		m_sdbalogo = "";
		m_sdbadescription = "";
		m_mdbaaddress = "";
		m_mdbaremittoaddress = "";
		m_mdbainvoicelogo = "";
		LineArray = new ArrayList<SMInvoiceDetail>(0);
		m_sErrorMessage = "";
        }

	public void save_without_data_transaction (Connection conn, String sUserID, String sUserFullName) throws Exception{
    	
    	//Now we'll validate, and save the entry:
    	try{
    		validate_lines(conn);
    	}catch (Exception ex){
    		throw new Exception("Error [1487943147] validating lines - "
    						+ ex.getMessage());
    	}
    	try{
    		validate_invoice_fields(conn, sUserID, sUserFullName);
    	}catch (Exception ex){
    		throw new Exception("Could not validate - " + ex.getMessage());
    	}

    	//Find out if the invoice is already in the database:
    	boolean bInvoiceAlreadyExists = false;
    	String SQL = MySQLs.Get_SM_Invoice(m_sInvoiceNumber);
    	try{
    		ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
    		if (rs.next()){
    			bInvoiceAlreadyExists = true;
    		}
    		rs.close();
    	}catch (SQLException e){
    		throw new Exception("In " + this.toString() + ".save_without_data_transaction - SQL error checking invoice: " + e.getMessage()
    							+ "<BR>SQL = " + SQL);
    		//m_sErrorMessage = "SQL error checking invoice: " + e.getMessage();
    	}
   
    	
    	//Now save the entry:
    	if (bInvoiceAlreadyExists){
    		//shouldn't get here.
    		throw new Exception("Invoice Number " + m_sInvoiceNumber + " already exists.");
    	}else{
 
        	//Validate the DBA id on the order
        	SMOrderHeader order = new SMOrderHeader();
        	order.setM_strimmedordernumber(getM_strimmedordernumber());
        	order.load(conn);
        	if(!order.isDBAValid(conn)) {
        		throw new Exception("The 'Doing Business As Address' for this order as been deleted or is invalid.");
        	}			
    			//Get Company Information
    		    String sSQL = "SELECT *"
    		    	+ " FROM " + SMTabledoingbusinessasaddresses.TableName
    		    	+ " WHERE ("
    		    	+ "(" + SMTabledoingbusinessasaddresses.lid + " = '" + order.getM_idoingbusinessasaddressid() + "')"
    		    	+ ")"
    		    ;
    		    try {
    				ResultSet rsDBA = clsDatabaseFunctions.openResultSet(sSQL, conn);
    				if (rsDBA.next()){
    					m_sdbalogo = rsDBA.getString(SMTabledoingbusinessasaddresses.sLogo).trim();
    					m_sdbadescription = rsDBA.getString(SMTabledoingbusinessasaddresses.sDescription).trim();
    					m_mdbaaddress = rsDBA.getString(SMTabledoingbusinessasaddresses.mAddress).trim();
    					m_mdbaremittoaddress = rsDBA.getString(SMTabledoingbusinessasaddresses.mRemitToAddress).trim();
    					m_mdbainvoicelogo = rsDBA.getString(SMTabledoingbusinessasaddresses.sInvoiceLogo).trim();
    				}
    				rsDBA.close();
    			} catch (Exception e) {
    				throw new Exception("Error [14464845612] reading DBA for this invoice - " + e.getMessage());
    			}
    		
    		SQL = MySQLs.Insert_SM_Invoice(
    				clsDateAndTimeConversions.utilDateToString(m_datInvoiceDate, "yyyy-MM-dd"),
    				clsManageBigDecimals.BigDecimalTo2DecimalSQLFormat(m_dDiscountAmount), 
    				clsManageBigDecimals.BigDecimalTo2DecimalSQLFormat(m_dSalesTaxAmount), 
    				Integer.toString(m_iCustomerDiscountLevel), 
    				Integer.toString(m_iNumberOfLinesOnInvoice), 
    				Integer.toString(m_iRequisitionDueDay), 
    				//Integer.toString(m_iTaxClass), 
    				clsDatabaseFunctions.FormatSQLStatement(m_mInvoiceComments), 
    				clsDatabaseFunctions.FormatSQLStatement(m_sBillToAddressLine1), 
    				clsDatabaseFunctions.FormatSQLStatement(m_sBillToAddressLine2), 
    				clsDatabaseFunctions.FormatSQLStatement(m_sBillToAddressLine3), 
    				clsDatabaseFunctions.FormatSQLStatement(m_sBillToAddressLine4), 
    				clsDatabaseFunctions.FormatSQLStatement(m_sBillToCity), 
    				clsDatabaseFunctions.FormatSQLStatement(m_sBillToContact), 
    				clsDatabaseFunctions.FormatSQLStatement(m_sBillToCountry), 
    				clsDatabaseFunctions.FormatSQLStatement(m_sBillToFax), 
    				clsDatabaseFunctions.FormatSQLStatement(m_sBillToName), 
    				clsDatabaseFunctions.FormatSQLStatement(m_sBillToPhone), 
    				clsDatabaseFunctions.FormatSQLStatement(m_sBillToState), 
    				clsDatabaseFunctions.FormatSQLStatement(m_sBillToZip), 
    				clsDatabaseFunctions.FormatSQLStatement(m_sCustomerCode), 
    				clsDatabaseFunctions.FormatSQLStatement(m_sDefaultPriceListCode), 
    				clsDatabaseFunctions.FormatSQLStatement(m_sInvoiceNumber), 
    				clsDatabaseFunctions.FormatSQLStatement(m_sLocation), 
    				clsDatabaseFunctions.FormatSQLStatement(m_sOrderNumber), 
    				clsDatabaseFunctions.FormatSQLStatement(m_sPONumber), 
    				clsDatabaseFunctions.FormatSQLStatement(m_sSalesperson), 
    				clsDatabaseFunctions.FormatSQLStatement(m_sServiceTypeCode), 
    				clsDatabaseFunctions.FormatSQLStatement(m_sServiceTypeCodeDescription), 
    				clsDatabaseFunctions.FormatSQLStatement(m_sShipToAddress1), 
    				clsDatabaseFunctions.FormatSQLStatement(m_sShipToAddress2), 
    				clsDatabaseFunctions.FormatSQLStatement(m_sShipToAddress3), 
    				clsDatabaseFunctions.FormatSQLStatement(m_sShipToAddress4), 
    				clsDatabaseFunctions.FormatSQLStatement(m_sShipToCity), 
    				clsDatabaseFunctions.FormatSQLStatement(m_sShipToCode), 
    				clsDatabaseFunctions.FormatSQLStatement(m_sShipToContact), 
    				clsDatabaseFunctions.FormatSQLStatement(m_sShipToCountry), 
    				clsDatabaseFunctions.FormatSQLStatement(m_sShipToFax), 
    				clsDatabaseFunctions.FormatSQLStatement(m_sShipToName), 
    				clsDatabaseFunctions.FormatSQLStatement(m_sShipToPhone), 
    				clsDatabaseFunctions.FormatSQLStatement(m_sShipToState), 
    				clsDatabaseFunctions.FormatSQLStatement(m_sShipToZip), 
    				clsDatabaseFunctions.FormatSQLStatement(m_sTaxExemptNumber), 
    				clsDatabaseFunctions.FormatSQLStatement(m_staxjurisdiction), 
    				clsDatabaseFunctions.FormatSQLStatement(m_sTerms), 
    				clsManageBigDecimals.BigDecimalTo2DecimalSQLFormat(m_dDiscountPercentage), 
    				clsDatabaseFunctions.FormatSQLStatement(m_sDesc), 
    				clsDatabaseFunctions.FormatSQLStatement(m_sCustomerControlAcctSet), 
    				clsDateAndTimeConversions.utilDateToString(m_datDueDate, "yyyy-MM-dd"),
    				clsDateAndTimeConversions.utilDateToString(m_datTermsDiscountDate, "yyyy-MM-dd"), 
    				clsManageBigDecimals.BigDecimalTo2DecimalSQLFormat(m_dTermsDiscountPercentage), 
    				clsManageBigDecimals.BigDecimalTo2DecimalSQLFormat(m_dTermsDiscountAvailable), 
    				Integer.toString(m_iExportedToAR), 
    				Integer.toString(m_iDayEndNumber), 
    				clsDateAndTimeConversions.utilDateToString(m_datOrderDate, "yyyy-MM-dd"), 
    				clsManageBigDecimals.BigDecimalTo2DecimalSQLFormat(m_dTaxRate), 
    				clsManageBigDecimals.BigDecimalTo2DecimalSQLFormat(m_dSalesTaxBase), 
    				clsManageBigDecimals.BigDecimalTo2DecimalSQLFormat(m_dPrePayment), 
    				clsDatabaseFunctions.FormatSQLStatement(m_sDiscountDesc), 
    				Integer.toString(m_iTransactionType), 
    				clsDatabaseFunctions.FormatSQLStatement(m_sMatchingInvoiceNumber), 
    				Integer.toString(m_iIsCredited), 
    				Integer.toString(m_iExportedToIC), 
    				clsDatabaseFunctions.FormatSQLStatement(m_sCreatedByFullName), 
    				Long.toString(m_lCreatedByID), 
    				Integer.toString(m_iOrderSourceID), 
    				clsDatabaseFunctions.FormatSQLStatement(m_sOrderSourceDesc), 
    				Integer.toString(m_iSalesGroup), 
    				clsDatabaseFunctions.FormatSQLStatement(m_strimmedordernumber),
    				m_itaxid,
    				clsDatabaseFunctions.FormatSQLStatement(m_staxtype),
    				m_icalculatetaxonpurchaseorsale,
    				m_icalculatetaxoncustomerinvoice,
    				clsDatabaseFunctions.FormatSQLStatement(m_sdbalogo),
    				clsDatabaseFunctions.FormatSQLStatement(m_sdbadescription),
    				clsDatabaseFunctions.FormatSQLStatement(m_mdbaaddress),
    				clsDatabaseFunctions.FormatSQLStatement(m_mdbaremittoaddress),
    				clsDatabaseFunctions.FormatSQLStatement(m_mdbainvoicelogo)
    				);
    	}
    	
    	try{
    		clsDatabaseFunctions.executeSQL(SQL, conn);
    	}catch(SQLException ex){
	    	throw new Exception(this.toString() + "Could not insert invoice."
	    						+ "<BR>" + ex.getMessage()
	    						+ "<BR>" + ex.getSQLState());
    	}

    	try{
    		save_lines(conn);
    	}catch (Exception ex){
    		throw new Exception("Failed to save invoice lines."
    							+ ex.getMessage());
    	}
    	return;
    }


	public boolean load (
			Connection conn
	){

		String SQL = "SELECT * FROM "  + SMTableinvoiceheaders.TableName
			+ " WHERE "
				+ "(" + SMTableinvoiceheaders.sInvoiceNumber + " = '" +  clsStringFunctions.PadLeft(m_sInvoiceNumber.trim(), " ", 8)  + "')"
			;;
		try {
			ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn); 
			if (!rs.next()){
				m_sErrorMessage = "Could not load invoice for invoice number: " + m_sInvoiceNumber + ". No invoice with that invoice number.";
				rs.close();
				return false;
			}

			//Load the variables:
			m_datInvoiceDate = rs.getDate(SMTableinvoiceheaders.datInvoiceDate);
			m_dDiscountAmount = BigDecimal.valueOf(rs.getDouble(SMTableinvoiceheaders.dDiscountAmount));
			m_dSalesTaxAmount = rs.getBigDecimal(SMTableinvoiceheaders.bdsalestaxamount);
			m_iCustomerDiscountLevel = rs.getInt(SMTableinvoiceheaders.iCustomerDiscountLevel);
			m_iRequisitionDueDay = rs.getInt(SMTableinvoiceheaders.iRequisitionDueDay);
			m_mInvoiceComments = rs.getString(SMTableinvoiceheaders.mInvoiceComments);
			m_sBillToAddressLine1 = rs.getString(SMTableinvoiceheaders.sBillToAddressLine1);
			m_sBillToAddressLine2 = rs.getString(SMTableinvoiceheaders.sBillToAddressLine2);
			m_sBillToAddressLine3 = rs.getString(SMTableinvoiceheaders.sBillToAddressLine3);
			m_sBillToAddressLine4 = rs.getString(SMTableinvoiceheaders.sBillToAddressLine4);
			m_sBillToCity = rs.getString(SMTableinvoiceheaders.sBillToCity);
			m_sBillToContact = rs.getString(SMTableinvoiceheaders.sBillToContact);
			m_sBillToCountry = rs.getString(SMTableinvoiceheaders.sBillToCountry);
			m_sBillToFax = rs.getString(SMTableinvoiceheaders.sBillToFax);
			m_sBillToName = rs.getString(SMTableinvoiceheaders.sBillToName);
			m_sBillToPhone = rs.getString(SMTableinvoiceheaders.sBillToPhone);
			m_sBillToState = rs.getString(SMTableinvoiceheaders.sBillToState);
			m_sBillToZip = rs.getString(SMTableinvoiceheaders.sBillToZip);
			m_sCustomerCode = rs.getString(SMTableinvoiceheaders.sCustomerCode);
			m_sDefaultPriceListCode = rs.getString(SMTableinvoiceheaders.sDefaultPriceListCode);
			m_sLocation = rs.getString(SMTableinvoiceheaders.sLocation);
			m_sOrderNumber = rs.getString(SMTableinvoiceheaders.sOrderNumber);
			m_sPONumber = rs.getString(SMTableinvoiceheaders.sPONumber);
			m_sSalesperson = rs.getString(SMTableinvoiceheaders.sSalesperson);
			m_sServiceTypeCode = rs.getString(SMTableinvoiceheaders.sServiceTypeCode);
			m_sServiceTypeCodeDescription = rs.getString(SMTableinvoiceheaders.sServiceTypeCodeDescription);
			m_sShipToAddress1 = rs.getString(SMTableinvoiceheaders.sShipToAddress1);
			m_sShipToAddress2 = rs.getString(SMTableinvoiceheaders.sShipToAddress2);
			m_sShipToAddress3 = rs.getString(SMTableinvoiceheaders.sShipToAddress3);
			m_sShipToAddress4 = rs.getString(SMTableinvoiceheaders.sShipToAddress4);
			m_sShipToCity = rs.getString(SMTableinvoiceheaders.sShipToCity);
			m_sShipToCode = rs.getString(SMTableinvoiceheaders.sShipToCode);
			m_sShipToContact = rs.getString(SMTableinvoiceheaders.sShipToContact);
			m_sShipToCountry = rs.getString(SMTableinvoiceheaders.sShipToCountry);
			m_sShipToFax = rs.getString(SMTableinvoiceheaders.sShipToFax);
			m_sShipToName = rs.getString(SMTableinvoiceheaders.sShipToName);
			m_sShipToPhone = rs.getString(SMTableinvoiceheaders.sShipToPhone);
			m_sShipToState = rs.getString(SMTableinvoiceheaders.sShipToState);
			m_sShipToZip = rs.getString(SMTableinvoiceheaders.sShipToZip);
			m_sTaxExemptNumber = rs.getString(SMTableinvoiceheaders.sTaxExemptNumber);
			m_staxjurisdiction = rs.getString(SMTableinvoiceheaders.staxjurisdiction);
			m_sTerms = rs.getString(SMTableinvoiceheaders.sTerms);
			m_dDiscountPercentage = BigDecimal.valueOf(rs.getDouble(SMTableinvoiceheaders.dDiscountPercentage));
			m_sDesc = rs.getString(SMTableinvoiceheaders.sDesc);
			m_sCustomerControlAcctSet = rs.getString(SMTableinvoiceheaders.sCustomerControlAcctSet);
			m_datDueDate = rs.getDate(SMTableinvoiceheaders.datDueDate);
			m_datTermsDiscountDate = rs.getDate(SMTableinvoiceheaders.datTermsDiscountDate);
			m_dTermsDiscountPercentage = BigDecimal.valueOf(rs.getDouble(SMTableinvoiceheaders.dTermsDiscountPercentage));
			m_dTermsDiscountAvailable = BigDecimal.valueOf(rs.getDouble(SMTableinvoiceheaders.dTermsDiscountAvailable));
			m_iExportedToAR = rs.getInt(SMTableinvoiceheaders.iExportedToAR);
			m_iDayEndNumber = rs.getInt(SMTableinvoiceheaders.iDayEndNumber);
			m_datOrderDate = rs.getDate(SMTableinvoiceheaders.datOrderDate);
			m_dTaxRate = rs.getBigDecimal(SMTableinvoiceheaders.bdtaxrate);
			m_dSalesTaxBase = rs.getBigDecimal(SMTableinvoiceheaders.bdsalestaxbase);
			m_dPrePayment = BigDecimal.valueOf(rs.getDouble(SMTableinvoiceheaders.dPrePayment));
			m_sDiscountDesc = rs.getString(SMTableinvoiceheaders.sDiscountDesc);
			m_iTransactionType = rs.getInt(SMTableinvoiceheaders.iTransactionType);
			m_sMatchingInvoiceNumber = rs.getString(SMTableinvoiceheaders.sMatchingInvoiceNumber);
			m_iIsCredited = rs.getInt(SMTableinvoiceheaders.iIsCredited);
			m_iExportedToIC = rs.getInt(SMTableinvoiceheaders.iExportedToIC);
			m_sCreatedByFullName = rs.getString(SMTableinvoiceheaders.sCreatedByFullName);
			m_lCreatedByID = rs.getLong(SMTableinvoiceheaders.lCreatedByID);
			m_sInvoiceCreationDate = clsDateAndTimeConversions.resultsetDateTimeStringToString(rs.getString(SMTableinvoiceheaders.datInvoiceCreationDate));
			m_iOrderSourceID = rs.getInt(SMTableinvoiceheaders.iOrderSourceID);
			m_iSalesGroup = rs.getInt(SMTableinvoiceheaders.iSalesGroup);
			m_sOrderSourceDesc = rs.getString(SMTableinvoiceheaders.sOrderSourceDesc);
			m_strimmedordernumber = rs.getString(SMTableinvoiceheaders.strimmedordernumber);
			m_itaxid = Integer.toString(rs.getInt(SMTableinvoiceheaders.itaxid));
			m_staxtype = rs.getString(SMTableinvoiceheaders.staxtype);
			m_icalculatetaxonpurchaseorsale = Integer.toString(rs.getInt(SMTableinvoiceheaders.icalculatetaxonpurchaseorsale));
			m_icalculatetaxoncustomerinvoice = Integer.toString(rs.getInt(SMTableinvoiceheaders.icalculatetaxoncustomerinvoice));
			m_sdbalogo = rs.getString(SMTableinvoiceheaders.sdbalogo);
			m_sdbadescription = rs.getString(SMTableinvoiceheaders.sdbadescription);
			m_mdbaaddress = rs.getString(SMTableinvoiceheaders.mdbaaddress);
			m_mdbaremittoaddress = rs.getString(SMTableinvoiceheaders.mdbaremittoaddress);
			m_mdbainvoicelogo = rs.getString(SMTableinvoiceheaders.sdbainvoicelogo);
			rs.close();
		}catch (SQLException ex){
			m_sErrorMessage = "Error loading invoice with SQL: " + SQL + " - " + ex.getMessage();
			return false;
		}

		if (!load_lines(conn)){
			return false;
		}

		m_iNumberOfLinesOnInvoice = LineArray.size();
		return true;
	}

	private boolean load_lines (
		Connection conn
	){

		if (m_sInvoiceNumber.trim().equalsIgnoreCase("")){
			m_sErrorMessage = "Invalid Invoice number - " + m_sInvoiceNumber;
			return false;
		}

		String SQL = MySQLs.Get_SM_Invoice_Details(clsStringFunctions.PadLeft(m_sInvoiceNumber.trim(), " ", 8));
		try {
			ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
			LineArray.clear();
			while(rs.next()){
				//Add a new line class, and load it up:
				SMInvoiceDetail line = new SMInvoiceDetail();
				line.setM_dExtendedCost(BigDecimal.valueOf(rs.getDouble(SMTableinvoicedetails.dExtendedCost)));
				line.setM_dExtendedPrice(BigDecimal.valueOf(rs.getDouble(SMTableinvoicedetails.dExtendedPrice)));
				line.setM_dExtendedPriceAfterDiscount(BigDecimal.valueOf(rs.getDouble(SMTableinvoicedetails.dExtendedPriceAfterDiscount)));
				line.setbdLineSalesTaxAmount(rs.getBigDecimal(SMTableinvoicedetails.bdlinesalestaxamount));
				line.setM_dQtyShipped(BigDecimal.valueOf(rs.getDouble(SMTableinvoicedetails.dQtyShipped)));
				line.setM_dUnitPrice(BigDecimal.valueOf(rs.getDouble(SMTableinvoicedetails.dUnitPrice)));
				line.setM_iDetailNumber(rs.getInt(SMTableinvoicedetails.iDetailNumber));
				line.setM_iIsStockItem(rs.getInt(SMTableinvoicedetails.iIsStockItem));
				line.setM_iLineNumber(rs.getInt(SMTableinvoicedetails.iLineNumber));
				line.setM_iMatchingInvoiceLineNumber(rs.getInt(SMTableinvoicedetails.iMatchingInvoiceLineNumber));				
				line.setM_iTaxable(rs.getInt(SMTableinvoicedetails.iTaxable));
				line.setM_sDesc(rs.getString(SMTableinvoicedetails.sDesc));
				line.setM_sDetailInvoiceComment(rs.getString(SMTableinvoicedetails.mDetailInvoiceComment));
				line.setM_sExpenseGLAcct(rs.getString(SMTableinvoicedetails.sExpenseGLAcct));
				line.setM_sInventoryGLAcct(rs.getString(SMTableinvoicedetails.sInventoryGLAcct));
				line.setM_sInvoiceNumber(rs.getString(SMTableinvoicedetails.sInvoiceNumber));
				line.setM_sItemCategory(rs.getString(SMTableinvoicedetails.sItemCategory));
				line.setM_sItemNumber(rs.getString(SMTableinvoicedetails.sItemNumber));
				line.setM_sLabel(rs.getString(SMTableinvoicedetails.sLabel));
				line.setM_sLocationCode(rs.getString(SMTableinvoicedetails.sLocationCode));
				line.setM_sMechFullName(rs.getString(SMTableinvoicedetails.sMechFullName));
				line.setM_sMechInitial(rs.getString(SMTableinvoicedetails.sMechInitial));
				line.setM_sMechID(Long.toString(rs.getLong(SMTableinvoicedetails.imechid)));
				line.setM_sRevenueGLAcct(rs.getString(SMTableinvoicedetails.sRevenueGLAcct));
				line.setM_sUnitOfMeasure(rs.getString(SMTableinvoicedetails.sUnitOfMeasure));
				line.setM_iSuppressDetailOnInvoice(rs.getInt(SMTableinvoicedetails.isuppressdetailoninvoice));
				line.set_iLaborItem(rs.getInt(SMTableinvoicedetails.ilaboritem));
				LineArray.add((SMInvoiceDetail) line);
			}
			rs.close();
		}catch (SQLException ex){
			m_sErrorMessage = "Error loading lines with SQL: " + SQL + " - " + ex.getMessage();
			return false;
		}

		return true;
	}

	public boolean add_line(SMInvoiceDetail line){

		line.setM_sInvoiceNumber(m_sInvoiceNumber);
		LineArray.add((SMInvoiceDetail) line);
		m_iNumberOfLinesOnInvoice = LineArray.size();
		return true;
	}
    public void clearLines(){
    	LineArray.clear();
    }
    public SMInvoiceDetail getDetailByIndex(int iDetailIndex){
    	
    	if (iDetailIndex > LineArray.size()){
    		return null;
    	}
    	if (iDetailIndex < 0){
    		return null;
    	}
    	
    	SMInvoiceDetail Line = (SMInvoiceDetail) LineArray.get(iDetailIndex);
		return Line;
    }
    
    public ArrayList<SMInvoiceDetail> getInvoiceDetailArray(){
    	return LineArray;
    }
    
    public boolean validate_invoice_fields (Connection conn, String sUserID, String sUserFullName) throws Exception{
    	
    	//Check the invoice date to make sure it's in the posting range:
    	String sDateName = "Invoice date";
    	if (getM_iTransactionType() == SMInvoice.TransactionTypeCredit){
    		sDateName = "Credit note date";
    	}
    	SMOption smopt = new SMOption();
    	//System.out.println("[1514902775] - getM_datInvoiceDate() = '" + getM_datInvoiceDate() + "'");
    	
    	//System.out.println("[1514902875] - SMUtilities.sqlDateToString(getM_datInvoiceDate(), 'MM/dd/YYYY') = '" +  SMUtilities.sqlDateToString(getM_datInvoiceDate(), "MM/dd/YYYY") + "'");
    	
    	DateFormat df = new SimpleDateFormat("MM/dd/yyyy");
    	String sInvoiceDateAsMMddYYYY = df.format(getM_datInvoiceDate());
    	//System.out.println("[1514902876] - sTestFormattedDate = '" +  sInvoiceDateAsMMddYYYY + "'");
    	
    	try {
			smopt.checkDateForPosting(
				sInvoiceDateAsMMddYYYY, 
				sDateName, 
				conn,
				sUserID);
		} catch (Exception e) {
			throw new Exception("Error [1514903361] - " + e.getMessage());
		}
    	
    	//Make sure the 'deposit amt', if any, is not less than zero:
    	if(getM_dPrePayment().compareTo(BigDecimal.ZERO) < 0){
    		throw new Exception("Error [1537447508] - the deposit amt ('" 
    			+ clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(getM_dPrePayment()) + "')"
    			+ " cannot be less than zero."
    		);
    	}
    	
    	return true;
    }
    private void validate_lines (Connection conn) throws Exception{
        //Validate the lines here:
    	//First, eliminate any entries with zero amounts:
    	assign_line_numbers();
    	
    	//Don't allow any entries to be saved without lines:
    	if (LineArray.size() < 1){
    		throw new Exception("Invoice cannot be saved with no detail lines.");
    	}
    	
    	for (int i = 0; i < LineArray.size(); i++){
    		SMInvoiceDetail line = (SMInvoiceDetail) LineArray.get(i);
        	line.setM_sInvoiceNumber(m_sInvoiceNumber);
    	}

    	//TODO - any additional validation goes here:
    	return;
    }
    private void save_lines (Connection conn) throws Exception{

    	try{
    		delete_current_lines(conn);
    	}catch(Exception ex){
    		throw new Exception("Failed to delete lines."
    							+ ex.getMessage());
    	}
    	
    	for (int i=0;i<LineArray.size();i++){
    		SMInvoiceDetail line = (SMInvoiceDetail) LineArray.get(i);
    		//Set the line number based on the index of the LineArray:
    		line.setM_iLineNumber(i + 1);

        	try{
        		add_single_line(line, conn);
        	}catch(Exception ex){
        		throw new Exception("Failed to insert line."
        							+ ex.getMessage());
        	}
    	}
    }

    private void delete_current_lines(Connection conn) throws Exception{
    	
    	String SQL = MySQLs.Delete_Invoice_Details(m_sInvoiceNumber);
    	try {
    		clsDatabaseFunctions.executeSQL(SQL, conn);
    	}catch (SQLException e){
    		throw new Exception("Error deleting invoice details - " + e.getMessage());
    	}
    }
    private void assign_line_numbers(){
    	for (int i = 0; i < LineArray.size(); i++){
    		SMInvoiceDetail line = (SMInvoiceDetail) LineArray.get(i);
    		line.setM_iLineNumber(i + 1);
    	}    	
    }
    private void add_single_line(SMInvoiceDetail line, Connection conn) throws Exception{
    	String SQL = MySQLs.Insert_Invoice_Details(
    			Integer.toString(line.getM_iIsStockItem()), 
    			clsManageBigDecimals.BigDecimalTo2DecimalSQLFormat(line.getM_dExtendedPrice()), 
    			clsManageBigDecimals.BigDecimalTo2DecimalSQLFormat(line.getM_dQtyShipped()), 
    			clsManageBigDecimals.BigDecimalTo2DecimalSQLFormat(line.getM_dUnitPrice()), 
    			Integer.toString(line.getM_iDetailNumber()), 
    			Integer.toString(line.getM_iTaxable()),
    			Integer.toString(line.get_iLaborItem()),
    			Integer.toString(line.getM_iLineNumber()), 
    			clsDatabaseFunctions.FormatSQLStatement(line.getM_sDetailInvoiceComment()), 
    			clsDatabaseFunctions.FormatSQLStatement(line.getM_sDesc()), 
    			clsDatabaseFunctions.FormatSQLStatement(line.getM_sInvoiceNumber()), 
    			clsDatabaseFunctions.FormatSQLStatement(line.getM_sItemCategory()), 
    			clsDatabaseFunctions.FormatSQLStatement(line.getM_sItemNumber()), 
    			clsDatabaseFunctions.FormatSQLStatement(line.getM_sLocationCode()), 
    			clsDatabaseFunctions.FormatSQLStatement(line.getM_sUnitOfMeasure()), 
    			clsDatabaseFunctions.FormatSQLStatement(line.getM_sInventoryGLAcct()), 
    			clsDatabaseFunctions.FormatSQLStatement(line.getM_sExpenseGLAcct()), 
    			clsDatabaseFunctions.FormatSQLStatement(line.getM_sRevenueGLAcct()), 
    			clsManageBigDecimals.BigDecimalTo2DecimalSQLFormat(line.getM_dExtendedCost()), 
    			clsManageBigDecimals.BigDecimalTo2DecimalSQLFormat(line.getM_dExtendedPriceAfterDiscount()), 
    			clsManageBigDecimals.BigDecimalTo2DecimalSQLFormat(line.getbdLineSalesTaxAmount()), 
    			Integer.toString(line.getM_iMatchingInvoiceLineNumber()), 
    			line.getM_sMechID(),
    			clsDatabaseFunctions.FormatSQLStatement(line.getM_sMechInitial()), 
    			clsDatabaseFunctions.FormatSQLStatement(line.getM_sMechFullName()), 
    			clsDatabaseFunctions.FormatSQLStatement(line.getM_sLabel()),
    			Integer.toString(line.getM_iSuppressDetailOnInvoice())
    	);
    	try{
    		clsDatabaseFunctions.executeSQL(SQL, conn);
    	}catch(SQLException ex){
    		throw new Exception("Error in " + this.toString() + "add_single_line class!!"
    						+ "<BR>" + ex.getMessage()
    						+ "<BR>SQL: " + SQL);
    	}
    	return;
    }
    
    public void Add_Detail(SMInvoiceDetail cInvDetail){
    	LineArray.add(cInvDetail);
    }
    
    public void CalculatePriceAfterDiscount() throws Exception{
/*    	
    	'ltong 08/12/03
        'This function clears the round-up error by adding the difference to the last detail.
*/      
    	//int i = 0;        
    	BigDecimal bdTotalExtendedAfterDiscount = new BigDecimal(0);
        BigDecimal bdTotalCalculatedExtendedAfterDiscount = new BigDecimal(0);
        BigDecimal bdTotalExtendedPrice = new BigDecimal(0);
        
        try{
	        //First get the total extended price:
	        for (int j=0;j<LineArray.size();j++){
	            //Me.Detail Is one - based
	        	bdTotalExtendedPrice = bdTotalExtendedPrice.add(LineArray.get(j).getM_dExtendedPrice());
	        }

	        //Next, subtract the discount amount from the total extended price to get the total extended price AFTER discount:
	        bdTotalExtendedAfterDiscount = bdTotalExtendedPrice.subtract(m_dDiscountAmount);
	        
	        //Get the discount percentage as a decimal"
	        BigDecimal bdDiscountPercentageAsDecimal = m_dDiscountPercentage.divide(BigDecimal.valueOf(100));
	        
	        //Add up the after discount totals for all but the last line:
	        for (int k=0;k<LineArray.size()-1;k++){
	            LineArray.get(k).setM_dExtendedPriceAfterDiscount(LineArray.get(k).getM_dExtendedPrice().multiply(BigDecimal.ONE.subtract(bdDiscountPercentageAsDecimal)));
	            bdTotalCalculatedExtendedAfterDiscount = bdTotalCalculatedExtendedAfterDiscount.add(LineArray.get(k).getM_dExtendedPriceAfterDiscount());
	        }
	        
	        //'Now we need to make sure that the individual line discounts match the total discount, so we'll make up any
	        //'rounding differences in the last line:
	        LineArray.get(LineArray.size() - 1).setM_dExtendedPriceAfterDiscount(bdTotalExtendedAfterDiscount.subtract(bdTotalCalculatedExtendedAfterDiscount));
	    	return;
	    	
        }catch(Exception ex){
        	throw new Exception("Error calculating price after discount - " + ex.getMessage());
        }
    }
    
    public void CalculateInvoiceTaxAmount(String sTaxID, Connection conn) throws Exception{
    	//The tax rate for the selected tax may not be for SALES tax, so we have to figure out first what is the
    	//SALES tax rate to be used in calculating sales tax for this invoice:
    	BigDecimal bdSalesTaxRateAsAWholeNumber = new BigDecimal("0.0000");
    	SMTax tax = new SMTax();
    	tax.set_slid(sTaxID);
    	try {
			tax.load(conn);
		} catch (Exception e1) {
			throw new Exception("Could not load tax - ");
		}
    	//If this is a SALES tax, and if it is supposed to be calculated on the invoice, then set the sales tax rate
    	//for the sales tax calculation.  Otherwise, the sales tax rate is zero:
    	if (
    		(tax.get_scalculateonpurchaseorsale().compareToIgnoreCase(Integer.toString(SMTabletax.TAX_CALCULATION_BASED_ON_SELLING_PRICE)) == 0)
    		&& (tax.get_scalculatetaxoncustomerinvoice().compareToIgnoreCase("1") == 0)
    	){
    		bdSalesTaxRateAsAWholeNumber = new BigDecimal(tax.get_bdtaxrate().replace(",", ""));
    	}
    	SMSalesOrderTaxCalculator sotc = new SMSalesOrderTaxCalculator(bdSalesTaxRateAsAWholeNumber, m_dDiscountAmount);
		//Add each line from the invoice:
    	for (int i=0;i<LineArray.size();i++){
			try {
				sotc.addLine(
					LineArray.get(i).getM_dExtendedPrice(), 
					LineArray.get(i).getM_iTaxable(), 
					LineArray.get(i).getM_dQtyShipped(), 
					LineArray.get(i).getM_sItemNumber());
			} catch (Exception e) {
				throw new Exception("Error [1390319296] adding invoice line to calculate tax for item number '" + LineArray.get(i).getM_sItemNumber() + "' - " + e.getMessage());
			}
		}
    	//Calculate the tax:
		sotc.calculateSalesTax();
		
		//Set the line tax amounts:
    	for (int i=0;i<LineArray.size();i++){
			try {
				LineArray.get(i).setbdLineSalesTaxAmount(sotc.getSalesTaxAmountPerLine(i));
			} catch (Exception e) {
				throw new Exception("Error [1390319297] setting sales tax amount for item number '" + LineArray.get(i).getM_sItemNumber() + "' - " + e.getMessage());
			}
		}
    	//Set the tax base:
    	m_dSalesTaxBase = sotc.getTotalSalesTaxBase();
        m_dSalesTaxAmount = sotc.getTotalSalesTax();
		m_itaxid = sTaxID;
		m_staxtype = tax.get_staxtype();
		m_icalculatetaxonpurchaseorsale = tax.get_scalculateonpurchaseorsale();
		m_icalculatetaxoncustomerinvoice = tax.get_scalculatetaxoncustomerinvoice();
		
    }
    
    public void Save_Invoice(Connection conn, String sUserID, String sUserFullName) throws Exception{
        boolean bDebugMode = false;
//      'If this is an invoice, we cannot have a negative total:
        BigDecimal bdExtPriceAfterDisc = BigDecimal.ZERO;
        BigDecimal bdTotalTax = BigDecimal.ZERO;
        
        for (int i=0;i<LineArray.size();i++){
            bdExtPriceAfterDisc = bdExtPriceAfterDisc.add(LineArray.get(i).getM_dExtendedPriceAfterDiscount());
            bdTotalTax = bdTotalTax.add(LineArray.get(i).getbdLineSalesTaxAmount());
        }
        
        if (m_iTransactionType == TransactionTypeInvoice){
            if (bdExtPriceAfterDisc.add(bdTotalTax).compareTo(BigDecimal.ZERO) < 0){
                throw new Exception("Invoice cannot be created from order number " + m_sOrderNumber + " - total would be less than zero.");
            }
        }else{
            if (bdExtPriceAfterDisc.add(bdTotalTax).compareTo(BigDecimal.ZERO) > 0){
            	throw new Exception("Credit note cannot be created from order number " + m_sOrderNumber + " - total would be greater than zero.");
            }
        }
        
        //First, get the next invoice number:
        SMOption opt = new SMOption();
        if (!opt.load(conn)){
        	throw new SQLException("Could not load SM Options to get next invoice number - " + opt.getErrorMessage());
        }
        m_sInvoiceNumber = clsStringFunctions.PadLeft(String.valueOf(Long.parseLong(opt.getNextInvoiceNumber().trim())), " " , INVOICE_NUMBER_LENGTH_USED);
		m_sSMOptionLastEditDate = opt.getLastEditDate();
		m_sSMOptionLastEditTime = opt.getLastEditTime(); 
		m_sSMOptionLastEditUserID = opt.getLastEditUserID(); 
		m_sSMOptionLastEditProcess = opt.getLastEditProcess();
        try{
        	save_without_data_transaction(conn, sUserID, sUserFullName);
        }catch (Exception ex){
        	throw new Exception("Error writing invoice."
        						+ "<BR>" + ex.getMessage());
        }
        if (bDebugMode){
        	System.out.println("In Save_Invoice - finished save_without_data_transaction(conn)");
        }
        boolean bRecordChanged = true;
        if (!opt.load(conn)){
        	throw new SQLException("Could not load SM Options to get check for changes - " + opt.getErrorMessage());
        }
       	if (m_sSMOptionLastEditDate.compareTo(opt.getLastEditDate()) == 0 &&
    		m_sSMOptionLastEditTime.compareTo(opt.getLastEditTime()) == 0 &&
    		m_sSMOptionLastEditUserID.compareTo(opt.getLastEditUserID()) == 0 &&
    		m_sSMOptionLastEditProcess.compareTo(opt.getLastEditProcess()) == 0){
        		bRecordChanged = false;
        }
        if (!bRecordChanged){
	        //'Next, set the next invoice number:
	    	try {
				opt.setNextInvoiceNumber(Long.toString(Long.parseLong(m_sInvoiceNumber.trim()) + 1));
			} catch (Exception e) {
	        	throw new Exception("The next invoice number could not be set using current invoice number '" + m_sInvoiceNumber + "'."
	            		+ " Your invoice was NOT created - please try again.");
			}
	    	opt.setLastEditUserFullName(m_sCreatedByFullName);
	    	opt.setLastEditUserID(Long.toString(m_lCreatedByID));
	    	opt.setLastEditProcess("Create_Multiple_Invoices");
	    	opt.save(conn);
        }else{
        	//can't save.
	        if (bDebugMode){
	        	System.out.println("In Save_Invoice - " + opt.getLastEditUserFullName() 
	            		+ " created new invoice(s) at " 
	            		+ opt.getLastEditTime() + " since you started.");
	        }
        	throw new Exception("User '" + opt.getLastEditUserFullName() 
        		+ "' created a new invoice at " 
        		+ opt.getLastEditTime() + " since you started. "
        		+ " Your invoice was NOT created - please try again.");
        }
    }
    
	public void updateInvoicingState(String sInvoiceToUpdate, 
									 String sInvoicingState, 
									 String sDBID,
									 ServletContext servletContext,
									 String userID,
									 String sUserFullName) throws Exception {
		// TODO Auto-generated method stub
		Connection conn = clsDatabaseFunctions.getConnection(
				servletContext, 
				sDBID, 
				"MySQL", 
				this.toString() 
				+ ".updateInvoicingState - user: " 
				+ userID
				+ " - "
				+ sUserFullName
				);
			if (conn == null){
				throw new SQLException("Could not get connection to update invoicing state.");
			}
		
		setM_sInvoiceNumber(sInvoiceToUpdate);
		load(conn);
		
		String SQL = "UPDATE " + SMTableinvoiceheaders.TableName
			+ " SET " + SMTableinvoiceheaders.iinvoicingstate + "=" + sInvoicingState
			+ " WHERE ( TRIM(" + SMTableinvoiceheaders.sInvoiceNumber + ")='" + sInvoiceToUpdate.trim() + "')" ;
	
		//System.out.println(SQL);
    	try{
    		clsDatabaseFunctions.executeSQL(SQL, conn);
    	}catch(SQLException ex){
    		clsDatabaseFunctions.freeConnection(servletContext, conn);
    		throw new Exception("Error [1474592482] in " + this.toString() + ".updateInvoicingState - "
    			+ "<BR>" + ex.getMessage()
    			+ "<BR>SQL: " + SQL);
    	}
		clsDatabaseFunctions.freeConnection(servletContext, conn);
		return;
	}
	
    public String getErrorMessage(){
    	return m_sErrorMessage;
    }
    public void clearError(){
    	m_sErrorMessage = "";
    }

	public java.sql.Date getM_datInvoiceDate() {
		return m_datInvoiceDate;
	}

	public void setM_datInvoiceDate(java.sql.Date invoiceDate) {
		m_datInvoiceDate = invoiceDate;
	}

	public void setM_datInvoiceDate(long lInvoiceDate) {
		m_datInvoiceDate = new Date(lInvoiceDate);
	}

	public BigDecimal getM_dDiscountAmount() {
		return m_dDiscountAmount;
	}

	public void setM_dDiscountAmount(BigDecimal discountAmount) {
		m_dDiscountAmount = discountAmount;
	}

	public BigDecimal getbdsalestaxamount() {
		return m_dSalesTaxAmount;
	}

	public void setbdsalestaxamount(BigDecimal taxAmount) {
		m_dSalesTaxAmount = taxAmount;
	}

	public int getM_iCustomerDiscountLevel() {
		return m_iCustomerDiscountLevel;
	}

	public void setM_iCustomerDiscountLevel(int customerDiscountLevel) {
		m_iCustomerDiscountLevel = customerDiscountLevel;
	}

	public int getM_iNumberOfLinesOnInvoice() {
		return m_iNumberOfLinesOnInvoice;
	}

	public void setM_iNumberOfLinesOnInvoice(int numberOfLinesOnInvoice) {
		m_iNumberOfLinesOnInvoice = numberOfLinesOnInvoice;
	}

	public int getM_iRequisitionDueDay() {
		return m_iRequisitionDueDay;
	}

	public void setM_iRequisitionDueDay(int requisitionDueDay) {
		m_iRequisitionDueDay = requisitionDueDay;
	}

	//public int getM_iTaxClass() {
	//	return m_iTaxClass;
	//}

	//public void setM_iTaxClass(int taxClass) {
	//	m_iTaxClass = taxClass;
	//}

	public String getM_mInvoiceComments() {
		return m_mInvoiceComments;
	}

	public void setM_mInvoiceComments(String invoiceComments) {
		m_mInvoiceComments = invoiceComments;
	}

	public String getM_sBillToAddressLine1() {
		return m_sBillToAddressLine1;
	}

	public void setM_sBillToAddressLine1(String billToAddressLine1) {
		m_sBillToAddressLine1 = billToAddressLine1;
	}

	public String getM_sBillToAddressLine2() {
		return m_sBillToAddressLine2;
	}

	public void setM_sBillToAddressLine2(String billToAddressLine2) {
		m_sBillToAddressLine2 = billToAddressLine2;
	}

	public String getM_sBillToAddressLine3() {
		return m_sBillToAddressLine3;
	}

	public void setM_sBillToAddressLine3(String billToAddressLine3) {
		m_sBillToAddressLine3 = billToAddressLine3;
	}

	public String getM_sBillToAddressLine4() {
		return m_sBillToAddressLine4;
	}

	public void setM_sBillToAddressLine4(String billToAddressLine4) {
		m_sBillToAddressLine4 = billToAddressLine4;
	}

	public String getM_sBillToCity() {
		return m_sBillToCity;
	}

	public void setM_sBillToCity(String billToCity) {
		m_sBillToCity = billToCity;
	}

	public String getM_sBillToContact() {
		return m_sBillToContact;
	}

	public void setM_sBillToContact(String billToContact) {
		m_sBillToContact = billToContact;
	}

	public String getM_sBillToCountry() {
		return m_sBillToCountry;
	}

	public void setM_sBillToCountry(String billToCountry) {
		m_sBillToCountry = billToCountry;
	}

	public String getM_sBillToFax() {
		return m_sBillToFax;
	}

	public void setM_sBillToFax(String billToFax) {
		m_sBillToFax = billToFax;
	}

	public String getM_sBillToName() {
		return m_sBillToName;
	}

	public void setM_sBillToName(String billToName) {
		m_sBillToName = billToName;
	}

	public String getM_sBillToPhone() {
		return m_sBillToPhone;
	}

	public void setM_sBillToPhone(String billToPhone) {
		m_sBillToPhone = billToPhone;
	}

	public String getM_sBillToState() {
		return m_sBillToState;
	}

	public void setM_sBillToState(String billToState) {
		m_sBillToState = billToState;
	}

	public String getM_sBillToZip() {
		return m_sBillToZip;
	}

	public void setM_sBillToZip(String billToZip) {
		m_sBillToZip = billToZip;
	}

	public String getM_sCustomerCode() {
		return m_sCustomerCode;
	}

	public void setM_sCustomerCode(String customerCode) {
		m_sCustomerCode = customerCode;
	}

	public String getM_sDefaultPriceListCode() {
		return m_sDefaultPriceListCode;
	}

	public void setM_sDefaultPriceListCode(String defaultPriceListCode) {
		m_sDefaultPriceListCode = defaultPriceListCode;
	}

	public String getM_sInvoiceNumber() {
		return m_sInvoiceNumber;
	}

	public void setM_sInvoiceNumber(String invoiceNumber) {
		m_sInvoiceNumber = invoiceNumber;
	}

	public String getM_sLocation() {
		return m_sLocation;
	}

	public void setM_sLocation(String location) {
		m_sLocation = location;
	}

	public String getM_sOrderNumber() {
		return m_sOrderNumber;
	}

	public void setM_sOrderNumber(String orderNumber) {
		m_sOrderNumber = orderNumber;
	}

	public String getM_sPONumber() {
		return m_sPONumber;
	}

	public void setM_sPONumber(String number) {
		m_sPONumber = number;
	}

	public String getM_sSalesperson() {
		return m_sSalesperson;
	}

	public void setM_sSalesperson(String salesperson) {
		m_sSalesperson = salesperson;
	}

	public String getM_sServiceTypeCode() {
		return m_sServiceTypeCode;
	}

	public void setM_sServiceTypeCode(String serviceTypeCode) {
		m_sServiceTypeCode = serviceTypeCode;
	}

	public String getM_sServiceTypeCodeDescription() {
		return m_sServiceTypeCodeDescription;
	}

	public void setM_sServiceTypeCodeDescription(String serviceTypeCodeDescription) {
		m_sServiceTypeCodeDescription = serviceTypeCodeDescription;
	}

	public String getM_sShipToAddress1() {
		return m_sShipToAddress1;
	}

	public void setM_sShipToAddress1(String shipToAddress1) {
		m_sShipToAddress1 = shipToAddress1;
	}

	public String getM_sShipToAddress2() {
		return m_sShipToAddress2;
	}

	public void setM_sShipToAddress2(String shipToAddress2) {
		m_sShipToAddress2 = shipToAddress2;
	}

	public String getM_sShipToAddress3() {
		return m_sShipToAddress3;
	}

	public void setM_sShipToAddress3(String shipToAddress3) {
		m_sShipToAddress3 = shipToAddress3;
	}

	public String getM_sShipToAddress4() {
		return m_sShipToAddress4;
	}

	public void setM_sShipToAddress4(String shipToAddress4) {
		m_sShipToAddress4 = shipToAddress4;
	}

	public String getM_sShipToCity() {
		return m_sShipToCity;
	}

	public void setM_sShipToCity(String shipToCity) {
		m_sShipToCity = shipToCity;
	}

	public String getM_sShipToCode() {
		return m_sShipToCode;
	}

	public void setM_sShipToCode(String shipToCode) {
		m_sShipToCode = shipToCode;
	}

	public String getM_sShipToContact() {
		return m_sShipToContact;
	}

	public void setM_sShipToContact(String shipToContact) {
		m_sShipToContact = shipToContact;
	}

	public String getM_sShipToCountry() {
		return m_sShipToCountry;
	}

	public void setM_sShipToCountry(String shipToCountry) {
		m_sShipToCountry = shipToCountry;
	}

	public String getM_sShipToFax() {
		return m_sShipToFax;
	}

	public void setM_sShipToFax(String shipToFax) {
		m_sShipToFax = shipToFax;
	}

	public String getM_sShipToName() {
		return m_sShipToName;
	}

	public void setM_sShipToName(String shipToName) {
		m_sShipToName = shipToName;
	}

	public String getM_sShipToPhone() {
		return m_sShipToPhone;
	}

	public void setM_sShipToPhone(String shipToPhone) {
		m_sShipToPhone = shipToPhone;
	}

	public String getM_sShipToState() {
		return m_sShipToState;
	}

	public void setM_sShipToState(String shipToState) {
		m_sShipToState = shipToState;
	}

	public String getM_sShipToZip() {
		return m_sShipToZip;
	}

	public void setM_sShipToZip(String shipToZip) {
		m_sShipToZip = shipToZip;
	}

	public String getM_sTaxExemptNumber() {
		return m_sTaxExemptNumber;
	}

	public void setM_sTaxExemptNumber(String taxExemptNumber) {
		m_sTaxExemptNumber = taxExemptNumber;
	}

	public String getstaxjurisdiction() {
		return m_staxjurisdiction;
	}

	public void setstaxjurisdiction(String taxGroup) {
		m_staxjurisdiction = taxGroup;
	}

	public String getM_sTerms() {
		return m_sTerms;
	}

	public void setM_sTerms(String terms) {
		m_sTerms = terms;
	}

	public BigDecimal getM_dDiscountPercentage() {
		return m_dDiscountPercentage;
	}

	public void setM_dDiscountPercentage(BigDecimal discountPercentage) {
		m_dDiscountPercentage = discountPercentage;
	}

	public String getM_sDesc() {
		return m_sDesc;
	}

	public void setM_sDesc(String desc) {
		m_sDesc = desc;
	}

	public String getM_sCustomerControlAcctSet() {
		return m_sCustomerControlAcctSet;
	}

	public void setM_sCustomerControlAcctSet(String customerControlAcctSet) {
		m_sCustomerControlAcctSet = customerControlAcctSet;
	}

	public java.sql.Date getM_datDueDate() {
		return m_datDueDate;
	}

	public void setM_datDueDate(java.sql.Date dueDate) {
		m_datDueDate = dueDate;
	}

	public java.sql.Date getM_datTermsDiscountDate() {
		return m_datTermsDiscountDate;
	}

	public void setM_datTermsDiscountDate(java.sql.Date termsDiscountDate) {
		m_datTermsDiscountDate = termsDiscountDate;
	}

	public BigDecimal getM_dTermsDiscountPercentage() {
		return m_dTermsDiscountPercentage;
	}

	public void setM_dTermsDiscountPercentage(BigDecimal termsDiscountPercentage) {
		m_dTermsDiscountPercentage = termsDiscountPercentage;
	}

	public BigDecimal getM_dTermsDiscountAvailable() {
		return m_dTermsDiscountAvailable;
	}

	public void setM_dTermsDiscountAvailable(BigDecimal termsDiscountAvailable) {
		m_dTermsDiscountAvailable = termsDiscountAvailable;
	}

	public int getM_iExportedToAR() {
		return m_iExportedToAR;
	}

	public void setM_iExportedToAR(int exportedToAR) {
		m_iExportedToAR = exportedToAR;
	}

	public int getM_iDayEndNumber() {
		return m_iDayEndNumber;
	}

	public void setM_iDayEndNumber(int dayEndNumber) {
		m_iDayEndNumber = dayEndNumber;
	}

	public java.sql.Date getM_datOrderDate() {
		return m_datOrderDate;
	}

	public void setM_datOrderDate(Date orderDate) {
		m_datOrderDate = orderDate;
	}

	public void setM_datOrderDate(long lOrderDate) {
		m_datOrderDate = new Date(lOrderDate);
	}

	public BigDecimal getM_dTaxRate() {
		return m_dTaxRate;
	}

	public void setM_dTaxRate(BigDecimal taxRate) {
		m_dTaxRate = taxRate;
	}

	public BigDecimal getbdsalestaxbase() {
		return m_dSalesTaxBase;
	}

	public void setbdsalestaxbase(BigDecimal taxBase) {
		m_dSalesTaxBase = taxBase;
	}

	public BigDecimal getM_dPrePayment() {
		return m_dPrePayment;
	}

	public void setM_dPrePayment(BigDecimal prePayment) {
		m_dPrePayment = prePayment;
	}

	public String getM_sDiscountDesc() {
		return m_sDiscountDesc;
	}

	public void setM_sDiscountDesc(String discountDesc) {
		m_sDiscountDesc = discountDesc;
	}

	public int getM_iTransactionType() {
		return m_iTransactionType;
	}

	public void setM_iTransactionType(int transactionType) {
		m_iTransactionType = transactionType;
	}

	public String getM_sMatchingInvoiceNumber() {
		return m_sMatchingInvoiceNumber;
	}

	public void setM_sMatchingInvoiceNumber(String matchingInvoiceNumber) {
		m_sMatchingInvoiceNumber = matchingInvoiceNumber;
	}

	public int getM_iIsCredited() {
		return m_iIsCredited;
	}

	public void setM_iIsCredited(int isCredited) {
		m_iIsCredited = isCredited;
	}

	public int getM_iExportedToIC() {
		return m_iExportedToIC;
	}

	public void setM_iExportedToIC(int exportedToIC) {
		m_iExportedToIC = exportedToIC;
	}

	public String getM_sCreatedByFullName() {
		return m_sCreatedByFullName;
	}

	public void setM_sCreatedByFullName(String createdByFullName) {
		m_sCreatedByFullName = createdByFullName;
	}
	public String getM_lCreatedByID() {
		return Long.toString(m_lCreatedByID);
	}

	public void setM_lCreatedByID(String createdByID) {
		m_lCreatedByID = Long.parseLong(createdByID);
	}

	public String getM_datInvoiceCreationDate() {
		return m_sInvoiceCreationDate;
	}

	public void setM_datInvoiceCreationDate(String sInvoiceCreationDate) {
		m_sInvoiceCreationDate = sInvoiceCreationDate;
	}
	public int getM_iOrderSourceID() {
		return m_iOrderSourceID;
	}

	public void setM_iOrderSourceID(int orderSourceID) {
		m_iOrderSourceID = orderSourceID;
	}

	public String getM_sOrderSourceDesc() {
		return m_sOrderSourceDesc;
	}

	public void setM_sOrderSourceDesc(String orderSourceDesc) {
		m_sOrderSourceDesc = orderSourceDesc;
	}

	public String getM_sErrorMessage() {
		return m_sErrorMessage;
	}

	public void setM_sErrorMessage(String errorMessage) {
		m_sErrorMessage = errorMessage;
	}

	public int getM_iSalesGroup() {
		return m_iSalesGroup;
	}

	public void setM_iSalesGroup(int iSalesGroup) {
		m_iSalesGroup = iSalesGroup;
	}

	public String getM_strimmedordernumber() {
		return m_strimmedordernumber;
	}

	public void setM_strimmedordernumber(String sordernumber) {
		m_strimmedordernumber = sordernumber;
	}
	
	public String getitaxid() {
		return m_itaxid;
	}

	public void setitaxid(String sTaxID) {
		m_itaxid = sTaxID;
	}
	public String getstaxtype() {
		return m_staxtype;
	}

	public void setstaxtype(String sTaxType) {
		m_staxtype = sTaxType;
	}
	public String geticalculatetaxonpurchaseorsale() {
		return m_icalculatetaxonpurchaseorsale;
	}

	public void seticalculatetaxonpurchaseorsale(String scalculateonpurchaseorsale) {
		m_icalculatetaxonpurchaseorsale = scalculateonpurchaseorsale;
	}
	public String geticalculatetaxoncustomerinvoice() {
		return m_icalculatetaxoncustomerinvoice;
	}

	public void seticalculatetaxoncustomerinvoice(String scalculatetaxoncustomerinvoice) {
		m_icalculatetaxoncustomerinvoice = scalculatetaxoncustomerinvoice;
	}
	public void setsdbalogo(String sdbalogo	){
		m_sdbalogo	 = sdbalogo	;
	}
	public String getsdbalogo	() {
		return m_sdbalogo;
	}
	public void setsdbadescription(String sdbadescription	){
		m_sdbadescription	 = sdbadescription	;
	}
	public String getsdbadescription	() {
		return m_sdbadescription;
	}
	public void setmdbaaddress(String mdbaaddress	){
		m_mdbaaddress	 = mdbaaddress	;
	}
	public String getmdbaaddress	() {
		return m_mdbaaddress;
	}
	public void setmdbaremittoaddress(String mdbaremittoaddress	){
		m_mdbaremittoaddress	 = mdbaremittoaddress	;
	}
	public String getmdbaremittoaddress	() {
		return m_mdbaremittoaddress;
	}
	public String getmdbainvoicelogo () {
		return m_mdbainvoicelogo;
	}
	public void setmdbainvoicelogo(String mdbainvoicelogo) {
		m_mdbainvoicelogo = mdbainvoicelogo;
	}
	
	public BigDecimal getInvoiceTotalAmount(){
		
		BigDecimal bdTotal = BigDecimal.ZERO;
		for (int i=0;i<LineArray.size();i++){
			bdTotal = bdTotal.add(LineArray.get(i).getM_dExtendedPriceAfterDiscount().add(LineArray.get(i).getbdLineSalesTaxAmount()));
		}
		//System.out.println("[1522330582] - Totoal being passed back to format function " + bdTotal.toString());
		return bdTotal;
	}
}