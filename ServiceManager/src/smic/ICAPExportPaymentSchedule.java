package smic;

import java.math.BigDecimal;
import ServletUtilities.*;

public class ICAPExportPaymentSchedule extends java.lang.Object{

    private int m_iPaymentRecordType;
    private long m_lPaymentBatchNumber;
    private long m_lPaymentItemCounter;
    private long m_lPaymentTransactionNumber;
    private java.sql.Date m_datPaymentDateDue;
    private BigDecimal m_bdPaymentAmtDue;
    private java.sql.Date m_datPaymentDiscountDate;
    private BigDecimal m_bdDiscountAmt;
	
    ICAPExportPaymentSchedule(
    		int iRecordType,
            long lPaymentBatchNumber,
            long lPaymentItemCounter,
            long lPaymentTransactionNumber,
            java.sql.Date datPaymentDateDue,
            BigDecimal bdPaymentAmountDue,
            java.sql.Date datPaymentDiscountDate,
            BigDecimal bdDiscountAmount
        ) {
	        m_iPaymentRecordType = iRecordType;
	        m_lPaymentBatchNumber = lPaymentBatchNumber;
	        m_lPaymentItemCounter = lPaymentItemCounter;
	        m_lPaymentTransactionNumber = lPaymentTransactionNumber;
	        m_datPaymentDateDue = datPaymentDateDue;
	        m_bdPaymentAmtDue = bdPaymentAmountDue;
	        m_datPaymentDiscountDate = datPaymentDiscountDate;
	        m_bdDiscountAmt = bdDiscountAmount;
    }
    
    public int getRecordType(){
    	return m_iPaymentRecordType;
    }
    public long getBatchNumber(){
    	return m_lPaymentBatchNumber;
    }
    public long getItemCounter(){
    	return m_lPaymentItemCounter;
    }
    public long getTransactionNumber(){
    	return m_lPaymentTransactionNumber;
    }
    public String getsPaymentDateDue(String sFormat){
    	return clsDateAndTimeConversions.utilDateToString(m_datPaymentDateDue, sFormat);
    }
    public String getsPaymentAmountDue(String sFormat){
    	return clsManageBigDecimals.BigDecimalToFormattedString(sFormat, m_bdPaymentAmtDue);
    }
    public String getsPaymentDiscountDate(String sFormat){
    	return clsDateAndTimeConversions.utilDateToString(m_datPaymentDiscountDate, sFormat);
    }
    public String getsDiscountAmount(String sFormat){
    	return clsManageBigDecimals.BigDecimalToFormattedString(sFormat, m_bdDiscountAmt);
    }
}
