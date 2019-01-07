package smar;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Connection;
import SMDataDefinition.*;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsManageBigDecimals;

import java.math.BigDecimal;

import javax.servlet.ServletContext;

public class ARCustomerStatistics extends Object{
		
	private String m_sCustomerNumber;
	private String m_sCustomerName;
	private BigDecimal m_bdcurrentbalance;
	private String m_datlastinvoice;
	private String m_datlastcredit;
	private String m_datlastpayment;
	private BigDecimal m_bdamountoflastinvoice;
	private BigDecimal m_bdamountoflastcredit;
	private BigDecimal m_bdamountoflastpayment;
	private BigDecimal m_bdamountofhighestinvoice;
	private BigDecimal m_bdamountofhighestinvoicelastyear;
	private BigDecimal m_bdamountofhighestbalance;
	private BigDecimal m_bdamountofhighestbalancelastyear;
	private long m_ltotalnumberofpaidinvoices;
	private long m_lnumberofopeninvoices;
	private long m_ltotalnumberofdaystopay;
	private String m_serrormessage;
	
    ARCustomerStatistics(
    		String sCustomerNumber
        ) {
    	m_sCustomerNumber = sCustomerNumber;
    	m_sCustomerName = "";
    	m_bdcurrentbalance = new BigDecimal(0.00);
    	m_datlastinvoice = "0000-00-00";
    	m_datlastcredit = "0000-00-00";
    	m_datlastpayment = "0000-00-00";
    	m_bdamountoflastinvoice = new BigDecimal(0.00);
    	m_bdamountoflastcredit = new BigDecimal(0.00);
    	m_bdamountoflastpayment = new BigDecimal(0.00);
    	m_bdamountofhighestinvoice = new BigDecimal(0.00);
    	m_bdamountofhighestinvoicelastyear = new BigDecimal(0.00);
    	m_bdamountofhighestbalance = new BigDecimal(0.00);
    	m_bdamountofhighestbalancelastyear = new BigDecimal(0.00);
    	m_ltotalnumberofpaidinvoices = 0;
    	m_lnumberofopeninvoices = 0;
    	m_ltotalnumberofdaystopay = 0;
    	m_serrormessage = "";
        }
    
    public String getCustomerName(){
    	return m_sCustomerName;
    }

    //Needs a connection because it is used in data transactions:
	public boolean load(
			Connection conn
			){
		
		try{
			//Get the record to edit:
			String sSQL = ARSQLs.Get_CustomerStatistics_By_Code(m_sCustomerNumber);
	        ResultSet rs = clsDatabaseFunctions.openResultSet(sSQL, conn);

	        if (rs.next()){
	        	m_bdcurrentbalance = rs.getBigDecimal(SMTablearcustomerstatistics.sCurrentBalance);
	        	m_datlastinvoice = rs.getString(SMTablearcustomerstatistics.sDateOfLastInvoice);
	        	m_datlastcredit = rs.getString(SMTablearcustomerstatistics.sDateOfLastCredit);
	        	m_datlastpayment = rs.getString(SMTablearcustomerstatistics.sDateOfLastPayment);
	        	m_bdamountoflastinvoice = rs.getBigDecimal(SMTablearcustomerstatistics.sAmountOfLastInvoice);
	        	m_bdamountoflastcredit = rs.getBigDecimal(SMTablearcustomerstatistics.sAmountOfLastCredit);
	        	m_bdamountoflastpayment = rs.getBigDecimal(SMTablearcustomerstatistics.sAmountOfLastPayment);
	        	m_bdamountofhighestinvoice = rs.getBigDecimal(SMTablearcustomerstatistics.sAmountOfHighestInvoice);
	        	m_bdamountofhighestinvoicelastyear = rs.getBigDecimal(SMTablearcustomerstatistics.sAmountOfHighestInvoiceLastYear);
	        	m_bdamountofhighestbalance = rs.getBigDecimal(SMTablearcustomerstatistics.sHighestBalance);
	        	m_bdamountofhighestbalancelastyear = rs.getBigDecimal(SMTablearcustomerstatistics.sHighestBalanceLastYear);
	        	m_ltotalnumberofpaidinvoices = rs.getLong(SMTablearcustomerstatistics.sTotalNumberOfPaidInvoices);
	        	m_lnumberofopeninvoices = rs.getLong(SMTablearcustomerstatistics.sNumberOfOpenInvoices);
	        	m_ltotalnumberofdaystopay = rs.getLong(SMTablearcustomerstatistics.sTotalDaysToPay);
	        	rs.close();
	        	return true;
	        }
	        else{
	        	rs.close();
	        	return false;
	        }
	        
		}catch (SQLException ex){
	    	System.out.println("Error in " + this.toString() + ".load!!");
	        System.out.println("SQLException: " + ex.getMessage());
	        System.out.println("SQLState: " + ex.getSQLState());
	        System.out.println("SQL: " + ex.getErrorCode());
	        m_serrormessage = "Error loading customer statistics: " + ex.getMessage();
			return false;
		}
	}
	public boolean load(
			ServletContext context,
			String sConf
			){
		
		try{
			//Get the record to edit:
			String sSQL = ARSQLs.Get_CustomerStatistics_By_Code(m_sCustomerNumber);
	        ResultSet rs = clsDatabaseFunctions.openResultSet(
	        	sSQL, 
	        	context,
	        	sConf,
	        	"MySQL",
	        	this.toString() + ".load");

	        if (rs.next()){
	        	m_bdcurrentbalance = rs.getBigDecimal(SMTablearcustomerstatistics.sCurrentBalance);
	        	m_datlastinvoice = rs.getString(SMTablearcustomerstatistics.sDateOfLastInvoice);
	        	m_datlastcredit = rs.getString(SMTablearcustomerstatistics.sDateOfLastCredit);
	        	m_datlastpayment = rs.getString(SMTablearcustomerstatistics.sDateOfLastPayment);
	        	m_bdamountoflastinvoice = rs.getBigDecimal(SMTablearcustomerstatistics.sAmountOfLastInvoice);
	        	m_bdamountoflastcredit = rs.getBigDecimal(SMTablearcustomerstatistics.sAmountOfLastCredit);
	        	m_bdamountoflastpayment = rs.getBigDecimal(SMTablearcustomerstatistics.sAmountOfLastPayment);
	        	m_bdamountofhighestinvoice = rs.getBigDecimal(SMTablearcustomerstatistics.sAmountOfHighestInvoice);
	        	m_bdamountofhighestinvoicelastyear = rs.getBigDecimal(SMTablearcustomerstatistics.sAmountOfHighestInvoiceLastYear);
	        	m_bdamountofhighestbalance = rs.getBigDecimal(SMTablearcustomerstatistics.sHighestBalance);
	        	m_bdamountofhighestbalancelastyear = rs.getBigDecimal(SMTablearcustomerstatistics.sHighestBalanceLastYear);
	        	m_ltotalnumberofpaidinvoices = rs.getLong(SMTablearcustomerstatistics.sTotalNumberOfPaidInvoices);
	        	m_lnumberofopeninvoices = rs.getLong(SMTablearcustomerstatistics.sNumberOfOpenInvoices);
	        	m_ltotalnumberofdaystopay = rs.getLong(SMTablearcustomerstatistics.sTotalDaysToPay);
	        	rs.close();
	        	return true;
	        }
	        else{
	        	rs.close();
	        	return false;
	        }
	        
		}catch (SQLException ex){
	    	System.out.println("Error in " + this.toString() + ".load!!");
	        System.out.println("SQLException: " + ex.getMessage());
	        System.out.println("SQLState: " + ex.getSQLState());
	        System.out.println("SQL: " + ex.getErrorCode());
	        m_serrormessage = "Error loading customer statistics: " + ex.getMessage();
			return false;
		}
	}

	
	//Needs a connection because it is used in data transactions:
    public boolean save (Connection conn){
    	String SQL;
    	//If there is no record already, insert a new one:
		try{
			//Get the record:
			SQL = ARSQLs.Get_CustomerStatistics_By_Code(m_sCustomerNumber);
	        ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);

	        if (rs.next()){
	        	//Update:
	            SQL = ARSQLs.Update_CustomerStatistics_By_Code(
	            		clsDatabaseFunctions.FormatSQLStatement(m_sCustomerNumber),
	            		clsManageBigDecimals.BigDecimalTo2DecimalSQLFormat(m_bdcurrentbalance),
	        			clsManageBigDecimals.BigDecimalTo2DecimalSQLFormat(m_bdamountofhighestinvoice),
	        			clsManageBigDecimals.BigDecimalTo2DecimalSQLFormat(m_bdamountofhighestinvoicelastyear),
	        			clsManageBigDecimals.BigDecimalTo2DecimalSQLFormat(m_bdamountoflastcredit),
	        			clsManageBigDecimals.BigDecimalTo2DecimalSQLFormat(m_bdamountoflastinvoice),
	        			clsManageBigDecimals.BigDecimalTo2DecimalSQLFormat(m_bdamountoflastpayment),
	        			m_datlastcredit,
	        			m_datlastinvoice,
	        			m_datlastpayment,
	        			clsManageBigDecimals.BigDecimalTo2DecimalSQLFormat(m_bdamountofhighestbalance),
	        			clsManageBigDecimals.BigDecimalTo2DecimalSQLFormat(m_bdamountofhighestbalancelastyear),
	        			Long.toString(m_lnumberofopeninvoices),
	        			Long.toString(m_ltotalnumberofpaidinvoices),
	        			Long.toString(m_ltotalnumberofdaystopay)
	            );
	        }else{
	        	//Insert:
	            SQL = ARSQLs.Insert_CustomerStatistics_By_Code(
	            		clsDatabaseFunctions.FormatSQLStatement(m_sCustomerNumber),
	            		clsManageBigDecimals.BigDecimalTo2DecimalSQLFormat(m_bdcurrentbalance),
	        			clsManageBigDecimals.BigDecimalTo2DecimalSQLFormat(m_bdamountofhighestinvoice),
	        			clsManageBigDecimals.BigDecimalTo2DecimalSQLFormat(m_bdamountofhighestinvoicelastyear),
	        			clsManageBigDecimals.BigDecimalTo2DecimalSQLFormat(m_bdamountoflastcredit),
	        			clsManageBigDecimals.BigDecimalTo2DecimalSQLFormat(m_bdamountoflastinvoice),
	        			clsManageBigDecimals.BigDecimalTo2DecimalSQLFormat(m_bdamountoflastpayment),
	        			m_datlastcredit,
	        			m_datlastinvoice,
	        			m_datlastpayment,
	        			clsManageBigDecimals.BigDecimalTo2DecimalSQLFormat(m_bdamountofhighestbalance),
	        			clsManageBigDecimals.BigDecimalTo2DecimalSQLFormat(m_bdamountofhighestbalancelastyear),
	        			Long.toString(m_lnumberofopeninvoices),
	        			Long.toString(m_ltotalnumberofpaidinvoices),
	        			Long.toString(m_ltotalnumberofdaystopay)
	            );
	        }
	        rs.close();
	        
	        if(!clsDatabaseFunctions.executeSQL(SQL, conn)){
		        m_serrormessage = "Error updating customer statistics.";
				return false;
	        }
	        
		}catch (SQLException ex){
	    	System.out.println("Error in " + this.toString() + ".load!!");
	        System.out.println("SQLException: " + ex.getMessage());
	        System.out.println("SQLState: " + ex.getSQLState());
	        System.out.println("SQL: " + ex.getErrorCode());
	        m_serrormessage = "Error loading customer statistics: " + ex.getMessage();
			return false;
		}

    	return true;
    }
    //Needs a connection because it is used in data transactions:
	public boolean updateCustomerStatistics(
			String datLastInvoice,
			String datLastCredit,
			String datLastPayment,
			BigDecimal bdAmountOfLastInvoice,
			BigDecimal bdAmountOfLastCredit,
			BigDecimal bdAmountOfLastPayment,
			long lNumberOfPaidInvoices, 
			long lNumberOfDaysToPay, 
			Connection conn
			){
		
		m_datlastinvoice = datLastInvoice;
		m_datlastcredit = datLastCredit;
		m_datlastpayment = datLastPayment;
		m_bdamountoflastinvoice = bdAmountOfLastInvoice;
		m_bdamountoflastcredit = bdAmountOfLastCredit;
		m_bdamountoflastpayment = bdAmountOfLastPayment;
		
		if(m_bdamountoflastinvoice.compareTo(m_bdamountofhighestinvoice) > 0){
			m_bdamountofhighestinvoice = m_bdamountoflastinvoice;
		}
		
		String SQL = ARSQLs.Get_Open_Invoice_Count(m_sCustomerNumber);
		try{
			ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
			if (rs.next()){
				m_lnumberofopeninvoices = rs.getLong(1);
			}else{
				m_lnumberofopeninvoices = 0;
			}
			rs.close();
			SQL = ARSQLs.Get_Current_Balance(m_sCustomerNumber);
			rs = clsDatabaseFunctions.openResultSet(SQL, conn);
			if (rs.next()){
				m_bdcurrentbalance = rs.getBigDecimal(1);
			}else{
				m_bdcurrentbalance = new BigDecimal(0);
			}
			if(m_bdcurrentbalance.compareTo(m_bdamountofhighestbalance) > 0){
				m_bdamountofhighestbalance = m_bdcurrentbalance;
			}
			rs.close();
			
		}catch (Exception e){
			System.out.println("In " + this.toString() + ".updateCustomerBalances - error: " + e.getMessage());
			m_serrormessage = "In " + this.toString() + ".updateCustomerBalances - error: " + e.getMessage();
			return false;
		}
		
		m_ltotalnumberofpaidinvoices = lNumberOfPaidInvoices;
		m_ltotalnumberofdaystopay = lNumberOfDaysToPay;

		return true;
	}

    public String getCustomerNumber(){
    	return m_sCustomerNumber;
    }
    public boolean setCustomerNumber(String sCustomerNumber){
    	m_sCustomerNumber = sCustomerNumber;
    	return true;
    }

	public BigDecimal getM_bdcurrentbalance() {
		return m_bdcurrentbalance;
	}

	public void setM_bdcurrentbalance(BigDecimal m_bdcurrentbalance) {
		this.m_bdcurrentbalance = m_bdcurrentbalance;
	}

	public String getM_datlastinvoice() {
		return m_datlastinvoice;
	}

	public void setM_datlastinvoice(String m_datlastinvoice) {
		this.m_datlastinvoice = m_datlastinvoice;
	}

	public String getM_datlastcredit() {
		return m_datlastcredit;
	}

	public void setM_datlastcredit(String m_datlastcredit) {
		this.m_datlastcredit = m_datlastcredit;
	}

	public String getM_datlastpayment() {
		return m_datlastpayment;
	}

	public void setM_datlastpayment(String m_datlastpayment) {
		this.m_datlastpayment = m_datlastpayment;
	}

	public BigDecimal getM_bdamountoflastinvoice() {
		return m_bdamountoflastinvoice;
	}

	public void setM_bdamountoflastinvoice(BigDecimal m_bdamountoflastinvoice) {
		this.m_bdamountoflastinvoice = m_bdamountoflastinvoice;
	}

	public BigDecimal getM_bdamountoflastcredit() {
		return m_bdamountoflastcredit;
	}

	public void setM_bdamountoflastcredit(BigDecimal m_bdamountoflastcredit) {
		this.m_bdamountoflastcredit = m_bdamountoflastcredit;
	}

	public BigDecimal getM_bdamountoflastpayment() {
		return m_bdamountoflastpayment;
	}

	public void setM_bdamountoflastpayment(BigDecimal m_bdamountoflastpayment) {
		this.m_bdamountoflastpayment = m_bdamountoflastpayment;
	}

	public BigDecimal getM_bdamountofhighestinvoice() {
		return m_bdamountofhighestinvoice;
	}

	public void setM_bdamountofhighestinvoice(BigDecimal m_bdamountofhighestinvoice) {
		this.m_bdamountofhighestinvoice = m_bdamountofhighestinvoice;
	}

	public BigDecimal getM_bdamountofhighestinvoicelastyear() {
		return m_bdamountofhighestinvoicelastyear;
	}

	public void setM_bdamountofhighestinvoicelastyear(
			BigDecimal m_bdamountofhighestinvoicelastyear) {
		this.m_bdamountofhighestinvoicelastyear = m_bdamountofhighestinvoicelastyear;
	}

	public BigDecimal getM_bdamountofhighestbalance() {
		return m_bdamountofhighestbalance;
	}

	public void setM_bdamountofhighestbalance(BigDecimal m_bdamountofhighestbalance) {
		this.m_bdamountofhighestbalance = m_bdamountofhighestbalance;
	}

	public BigDecimal getM_bdamountofhighestbalancelastyear() {
		return m_bdamountofhighestbalancelastyear;
	}

	public void setM_bdamountofhighestbalancelastyear(
			BigDecimal m_bdamountofhighestbalancelastyear) {
		this.m_bdamountofhighestbalancelastyear = m_bdamountofhighestbalancelastyear;
	}

	public BigDecimal getM_bdaveragenumberofdaystopay() {
		BigDecimal bdTotalNumberOfDaysToPay = new BigDecimal(m_ltotalnumberofdaystopay);
		BigDecimal bdTotalNumberOfPaidInvoices = new BigDecimal(m_ltotalnumberofpaidinvoices);
		if (bdTotalNumberOfPaidInvoices.compareTo(BigDecimal.ZERO) > 0){
			return bdTotalNumberOfDaysToPay.divide(bdTotalNumberOfPaidInvoices);
		}else{
			return BigDecimal.ZERO;
		}
	}

	public long getM_ltotalnumberofpaidinvoices() {
		return m_ltotalnumberofpaidinvoices;
	}

	public void setM_ltotalnumberofpaidinvoices(long m_ltotalnumberofpaidinvoices) {
		this.m_ltotalnumberofpaidinvoices = m_ltotalnumberofpaidinvoices;
	}

	public long getM_lnumberofopeninvoices() {
		return m_lnumberofopeninvoices;
	}

	public void setM_lnumberofopeninvoices(long m_lnumberofopeninvoices) {
		this.m_lnumberofopeninvoices = m_lnumberofopeninvoices;
	}

	public long getM_ltotalnumberofdaystopay() {
		return m_ltotalnumberofdaystopay;
	}

	public void setM_ltotalnumberofdaystopay(long m_ltotalnumberofdaystopay) {
		this.m_ltotalnumberofdaystopay = m_ltotalnumberofdaystopay;
	}

	public String getM_serrormessage() {
		return m_serrormessage;
	}
}
