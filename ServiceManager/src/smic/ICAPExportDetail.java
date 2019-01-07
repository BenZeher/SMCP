package smic;

import java.math.BigDecimal;

import ServletUtilities.*;

public class ICAPExportDetail extends java.lang.Object{

    private int m_iDetailRecordType;
    private long m_lDetailBatchNumber;
    private long m_lDetailItemCounter;
    private long m_lDetailTransactionNumber;
    private String m_sDescription;
    private String m_sGLAcct;
    private BigDecimal m_bdDistributionAmt;
    private java.sql.Date m_datBillingDate;
	
    ICAPExportDetail(
    		int iRecordType,
            long lDetailBatchNumber,
            long lDetailItemCounter,
            long lDetailTransactionNumber,
            String sDetailDescription,
            String sDetailGLAcct,
            BigDecimal bdDetailDistributionAmount,
            java.sql.Date datBillingDate
        ) {
	        m_iDetailRecordType = iRecordType;
	        m_lDetailBatchNumber = lDetailBatchNumber;
	        m_lDetailItemCounter = lDetailItemCounter;
	        m_lDetailTransactionNumber = lDetailTransactionNumber;
	        m_sDescription = sDetailDescription.replace("\"", "''").replace(",\"", ";\"").replace("\",", "\";");
	        m_sGLAcct = sDetailGLAcct;
	        m_bdDistributionAmt = bdDetailDistributionAmount;
	        m_datBillingDate = datBillingDate;
    }
    
    public int getRecordType(){
    	return m_iDetailRecordType;
    }
    public long getBatchNumber(){
    	return m_lDetailBatchNumber;
    }
    public long getItemCounter(){
    	return m_lDetailItemCounter;
    }
    public long getTransactionNumber(){
    	return m_lDetailTransactionNumber;
    }
    public String getDescription(){
    	return m_sDescription;
    }
    public String getGLAcct(){
    	return m_sGLAcct;
    }
    public String getsDistributionAmount(String sFormat){
    	return clsManageBigDecimals.BigDecimalToFormattedString(sFormat, m_bdDistributionAmt);
    }
    public String getsBillingDate(String sFormat){
    	return clsDateAndTimeConversions.utilDateToString(m_datBillingDate, sFormat);
    }
}
