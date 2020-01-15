package smar;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Connection;
import SMDataDefinition.*;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsManageBigDecimals;

import java.math.BigDecimal;

public class ARMonthlyStatistics extends Object{
	
	private String m_sCustomerNumber;
	private long m_lyear;
	private long m_lmonth;
	private BigDecimal m_bdinvoicetotal;
	private BigDecimal m_bdcredittotal;
	private BigDecimal m_bdpaymenttotal;
	private long m_lnumberofinvoices;
	private long m_lnumberofcredits;
	private long m_lnumberofpayments;
	private BigDecimal m_bdaveragenumberofdaystopay;
	private long m_ltotalnumberofpaidinvoices;
	private long m_ltotalnumberofdaystopay;
	private String m_serrormessage;
	
    ARMonthlyStatistics(
    		String sCustomerNumber,
    		long lYear,
    		long lMonth
        ) {
    	m_sCustomerNumber = sCustomerNumber;
    	m_lyear = lYear;
    	m_lmonth = lMonth;
    	m_bdinvoicetotal = new BigDecimal(0.00);
    	m_bdcredittotal = new BigDecimal(0.00);
    	m_bdpaymenttotal = new BigDecimal(0.00);
    	m_lnumberofinvoices = 0;
    	m_lnumberofcredits = 0;
    	m_lnumberofpayments = 0;
    	m_bdaveragenumberofdaystopay = new BigDecimal(0.00);
    	m_ltotalnumberofpaidinvoices = 0;
    	m_ltotalnumberofdaystopay = 0;
    	m_serrormessage = "";
        }
	public boolean load(
			Connection conn
			){
		try{
			//Get the record to edit:
			String sSQL ="SELECT * FROM " + SMTablearmonthlystatistics.TableName 
					+ " WHERE (" 
					+ "(" + SMTablearmonthlystatistics.sCustomerNumber + " = '" + m_sCustomerNumber + "')"
					+ " AND (" + SMTablearmonthlystatistics.sYear + " = " + Long.toString(m_lyear) + ")"
					+ " AND (" + SMTablearmonthlystatistics.sMonth + " = " + Long.toString(m_lmonth) + ")"
				+ ")"; 
	        ResultSet rs = clsDatabaseFunctions.openResultSet(sSQL, conn);

	        if (rs.next()){
	        	m_bdinvoicetotal = rs.getBigDecimal(SMTablearmonthlystatistics.sInvoiceTotal);
	        	m_bdcredittotal = rs.getBigDecimal(SMTablearmonthlystatistics.sCreditTotal);
	        	m_bdpaymenttotal = rs.getBigDecimal(SMTablearmonthlystatistics.sPaymentTotal);
	        	m_lnumberofinvoices = rs.getLong(SMTablearmonthlystatistics.sNumberOfInvoices);
	        	m_lnumberofcredits = rs.getLong(SMTablearmonthlystatistics.sNumberOfCredits);
	        	m_lnumberofpayments = rs.getLong(SMTablearmonthlystatistics.sNumberOfPayments);
	        	m_bdaveragenumberofdaystopay = rs.getBigDecimal(SMTablearmonthlystatistics.sAverageDaysToPay);
	        	m_ltotalnumberofpaidinvoices = rs.getLong(SMTablearmonthlystatistics.sNumberOfPaidInvoices);
	        	m_ltotalnumberofdaystopay = rs.getLong(SMTablearmonthlystatistics.sTotalNumberOfDaysToPay);
	        	rs.close();
	        	return true;
	        }
	        else{
	        	rs.close();
	        	return false;
	        }
		}catch (SQLException ex){
	    	System.out.println("[1579119566] Error in " + this.toString() + ".load!!");
	        System.out.println("SQLException: " + ex.getMessage());
	        System.out.println("SQLState: " + ex.getSQLState());
	        System.out.println("SQL: " + ex.getErrorCode());
	        m_serrormessage = "Error loading customer statistics: " + ex.getMessage();
			return false;
		}
	}
    public boolean save (Connection conn){

    	String SQL;
    	try{
			//Get the record:
			SQL = "SELECT * FROM " + SMTablearmonthlystatistics.TableName 
					+ " WHERE (" 
					+ "(" + SMTablearmonthlystatistics.sCustomerNumber + " = '" + m_sCustomerNumber + "')"
					+ " AND (" + SMTablearmonthlystatistics.sYear + " = " + Long.toString(m_lyear) + ")"
					+ " AND (" + SMTablearmonthlystatistics.sMonth + " = " + Long.toString(m_lmonth) + ")"
				+ ")"; 
			ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);

	        if (rs.next()){
	        	//Do an update
	            SQL =  "UPDATE " + SMTablearmonthlystatistics.TableName 
	        			+ " SET"
	        			+ " " + SMTablearmonthlystatistics.sAverageDaysToPay + " = " + clsManageBigDecimals.BigDecimalTo2DecimalSQLFormat(m_bdaveragenumberofdaystopay)
	        			+ ", " + SMTablearmonthlystatistics.sCreditTotal + " = " + clsManageBigDecimals.BigDecimalTo2DecimalSQLFormat(m_bdcredittotal)
	        			+ ", " + SMTablearmonthlystatistics.sInvoiceTotal + " = " + clsManageBigDecimals.BigDecimalTo2DecimalSQLFormat(m_bdinvoicetotal)
	        			+ ", " + SMTablearmonthlystatistics.sMonth + " = " + Long.toString(m_lmonth)
	        			+ ", " + SMTablearmonthlystatistics.sNumberOfCredits + " = " + Long.toString(m_lnumberofcredits)
	        			+ ", " + SMTablearmonthlystatistics.sNumberOfInvoices + " = " + Long.toString(m_lnumberofinvoices)
	        			+ ", " + SMTablearmonthlystatistics.sNumberOfPayments + " = " + Long.toString(m_lnumberofpayments)
	        			+ ", " + SMTablearmonthlystatistics.sPaymentTotal + " = " + clsManageBigDecimals.BigDecimalTo2DecimalSQLFormat(m_bdpaymenttotal)
	        			+ ", " + SMTablearmonthlystatistics.sNumberOfPaidInvoices + " = " + Long.toString(m_ltotalnumberofpaidinvoices)
	        			+ ", " + SMTablearmonthlystatistics.sTotalNumberOfDaysToPay + " = " + Long.toString(m_ltotalnumberofdaystopay)
	        			+ " WHERE (" 
	        				+ "(" + SMTablearmonthlystatistics.sCustomerNumber + " = '" + m_sCustomerNumber + "')"
	        				+ " AND (" + SMTablearmonthlystatistics.sYear + " = " + Long.toString(m_lyear) + ")"
	        				+ " AND (" + SMTablearmonthlystatistics.sMonth + " = " + Long.toString(m_lmonth) + ")"
	        		+ ")";
	        }else{
	        	//Do an insert
	            SQL =   "INSERT INTO " + SMTablearmonthlystatistics.TableName 
	        			
	        			+ " ("
	        			+ SMTablearmonthlystatistics.sCustomerNumber
	        			+ ", " + SMTablearmonthlystatistics.sYear
	        			+ ", " + SMTablearmonthlystatistics.sCreditTotal
	        			+ ", " + SMTablearmonthlystatistics.sInvoiceTotal
	        			+ ", " + SMTablearmonthlystatistics.sMonth
	        			+ ", " + SMTablearmonthlystatistics.sNumberOfCredits
	        			+ ", " + SMTablearmonthlystatistics.sNumberOfInvoices
	        			+ ", " + SMTablearmonthlystatistics.sNumberOfPayments
	        			+ ", " + SMTablearmonthlystatistics.sPaymentTotal
	        			+ ", " + SMTablearmonthlystatistics.sAverageDaysToPay
	        			+ ", " + SMTablearmonthlystatistics.sNumberOfPaidInvoices
	        			+ ", " + SMTablearmonthlystatistics.sTotalNumberOfDaysToPay
	        			+ ") VALUES ("
	        			
	        			+ "'" + m_sCustomerNumber + "'"
	        			+ ", " + Long.toString(m_lyear)
	        			+ ", " + clsManageBigDecimals.BigDecimalTo2DecimalSQLFormat(m_bdcredittotal)
	        			+ ", " + clsManageBigDecimals.BigDecimalTo2DecimalSQLFormat(m_bdinvoicetotal)
	        			+ ", " + Long.toString(m_lmonth)
	        			+ ", " + Long.toString(m_lnumberofcredits)
	        			+ ", " + Long.toString(m_lnumberofinvoices)
	        			+ ", " + Long.toString(m_lnumberofpayments)
	        			+ ", " + clsManageBigDecimals.BigDecimalTo2DecimalSQLFormat(m_bdpaymenttotal)
	        			+ ", " + clsManageBigDecimals.BigDecimalTo2DecimalSQLFormat(m_bdaveragenumberofdaystopay)
	        			+ ", " + Long.toString(m_ltotalnumberofpaidinvoices)
	        			+ ", " + Long.toString(m_ltotalnumberofdaystopay)

	        		+ ")";
	        }
    		
	        rs.close();
	    	if (clsDatabaseFunctions.executeSQL(SQL, conn) == false){
	    		System.out.println("[1579119574] " + this.toString() + "Could not update monthly statistics");
	    		m_serrormessage = "Error updating monthly statistics";
	    		return false;
	    	}else{
	    	}
    	}catch(SQLException ex){
    		System.out.println("[1579119586] Error in " + this.toString() + " class!!");
    	    System.out.println("SQLException: " + ex.getMessage());
    	    System.out.println("SQLState: " + ex.getSQLState());
    	    System.out.println("SQL: " + ex.getErrorCode());
    	    return false;
    	}
    	return true;
    }
	public boolean updateMonthlyStatistics(
			BigDecimal bdInvoiceTotal,
			BigDecimal bdCreditTotal,
			BigDecimal bdPaymentTotal,
			long lNumberOfInvoices,
			long lNumberOfCredits,
			long lNumberOfPayments,
			long lNumberOfPaidInvoices, 
			long lNumberOfDaysToPay
			){
		
		m_bdinvoicetotal = bdInvoiceTotal;
		m_bdcredittotal = bdCreditTotal;
		m_bdpaymenttotal = bdPaymentTotal;
		m_lnumberofinvoices = lNumberOfInvoices;
		m_lnumberofcredits = lNumberOfCredits;
		m_lnumberofpayments = lNumberOfPayments;
		m_ltotalnumberofpaidinvoices = lNumberOfPaidInvoices;
		m_ltotalnumberofdaystopay = lNumberOfDaysToPay;
		
		BigDecimal bdTotalNumberOfDaysToPay = new BigDecimal(m_ltotalnumberofdaystopay);
		BigDecimal bdTotalNumberOfPaidInvoices = new BigDecimal(m_ltotalnumberofpaidinvoices);
		if (bdTotalNumberOfPaidInvoices.compareTo(BigDecimal.ZERO) > 0){
			m_bdaveragenumberofdaystopay = bdTotalNumberOfDaysToPay.divide(bdTotalNumberOfPaidInvoices);
		}else{
			m_bdaveragenumberofdaystopay = BigDecimal.ZERO;
		}

		return true;
	}
	public String getM_sCustomerNumber() {
		return m_sCustomerNumber;
	}
	public void setM_sCustomerNumber(String customerNumber) {
		m_sCustomerNumber = customerNumber;
	}
	public long getM_lyear() {
		return m_lyear;
	}
	public void setM_lyear(long m_lyear) {
		this.m_lyear = m_lyear;
	}
	public long getM_lmonth() {
		return m_lmonth;
	}
	public void setM_lmonth(long m_lmonth) {
		this.m_lmonth = m_lmonth;
	}
	public BigDecimal getM_bdinvoicetotal() {
		return m_bdinvoicetotal;
	}
	public void setM_bdinvoicetotal(BigDecimal m_bdinvoicetotal) {
		this.m_bdinvoicetotal = m_bdinvoicetotal;
	}
	public BigDecimal getM_bdcredittotal() {
		return m_bdcredittotal;
	}
	public void setM_bdcredittotal(BigDecimal m_bdcredittotal) {
		this.m_bdcredittotal = m_bdcredittotal;
	}
	public BigDecimal getM_bdpaymenttotal() {
		return m_bdpaymenttotal;
	}
	public void setM_bdpaymenttotal(BigDecimal m_bdpaymenttotal) {
		this.m_bdpaymenttotal = m_bdpaymenttotal;
	}
	public long getM_lnumberofinvoices() {
		return m_lnumberofinvoices;
	}
	public void setM_lnumberofinvoices(long m_lnumberofinvoices) {
		this.m_lnumberofinvoices = m_lnumberofinvoices;
	}
	public long getM_lnumberofcredits() {
		return m_lnumberofcredits;
	}
	public void setM_lnumberofcredits(long m_lnumberofcredits) {
		this.m_lnumberofcredits = m_lnumberofcredits;
	}
	public long getM_lnumberofpayments() {
		return m_lnumberofpayments;
	}
	public void setM_lnumberofpayments(long m_lnumberofpayments) {
		this.m_lnumberofpayments = m_lnumberofpayments;
	}
	public BigDecimal getM_bdaveragenumberofdaystopay() {
		return m_bdaveragenumberofdaystopay;
	}
	public void setM_bdaveragenumberofdaystopay(
		BigDecimal m_bdaveragenumberofdaystopay) {
		this.m_bdaveragenumberofdaystopay = m_bdaveragenumberofdaystopay;
	}
	public long getM_ltotalnumberofpaidinvoices() {
		return m_ltotalnumberofpaidinvoices;
	}
	public void setM_ltotalnumberofpaidinvoices(long m_ltotalnumberofpaidinvoices) {
		this.m_ltotalnumberofpaidinvoices = m_ltotalnumberofpaidinvoices;
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