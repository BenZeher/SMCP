package smar;

import java.math.BigDecimal;

import ServletUtilities.*;

public class SMGLExportDetail extends java.lang.Object{

    private int m_iDetailRecordType;
    private long m_lDetailBatchNumber;
    private long m_lDetailJournalID;
    private long m_lDetailTransactionNumber;
    private String m_sDetailAccountID;
    private BigDecimal m_dDetailTransactionAmount;
    private String m_sDetailTransactionDescription;
    private String m_sDetailTransactionReference;
    private java.sql.Date m_datDetailTransactionDate;
    private String m_sDetailSourceLedger;
    private String m_sDetailSourceType;
    private String m_sDetailComment;
    private String m_sformattedaccountid;
    private String m_slinenumber;
	
    SMGLExportDetail(
    		int iRecordType,
            long lDetailBatchNumber,
            long lDetailJournalID,
            long lDetailTransactionNumber,
            String sDetailAccountID,
            String sDetailFormattedAccountID,
            BigDecimal dDetailTransactionAmount,
            String sDetailTransactionDescription,
            String sDetailTransactionReference,
            java.sql.Date datDetailTransactionDate,
            String sDetailSourceLedger,
            String sDetailSourceType,
            String sDetailComment,
            String sLineNumber
        ) {
	        m_iDetailRecordType = iRecordType;
	        m_lDetailBatchNumber = lDetailBatchNumber;
	        m_lDetailJournalID = lDetailJournalID;
	        m_lDetailTransactionNumber = lDetailTransactionNumber;
	        m_sDetailAccountID = sDetailAccountID;
	        m_sformattedaccountid = sDetailFormattedAccountID;
	        m_dDetailTransactionAmount = dDetailTransactionAmount;
	        m_sDetailTransactionDescription = sDetailTransactionDescription;
	        m_sDetailTransactionReference = sDetailTransactionReference;
	        m_datDetailTransactionDate = datDetailTransactionDate;
	        m_sDetailSourceLedger = sDetailSourceLedger;
	        m_sDetailSourceType = sDetailSourceType;
	        m_sDetailComment = sDetailComment; 
	        m_slinenumber = sLineNumber;
    }
    
    public int getRecordType(){
    	return m_iDetailRecordType;
    }
    public long getBatchNumber(){
    	return m_lDetailBatchNumber;
    }
    public long getJournalID(){
    	return m_lDetailJournalID;
    }
    public long getTransactionNumber(){
    	return m_lDetailTransactionNumber;
    }
    public String getAccountID(){
    	return m_sDetailAccountID;
    }
    public String getTransactionDescription(){
    	return m_sDetailTransactionDescription;
    }
    public String getTransactionReference(){
    	return m_sDetailTransactionReference;
    }
    public String getsTransactionDate(String sFormat){
    	return clsDateAndTimeConversions.utilDateToString(m_datDetailTransactionDate, sFormat);
    }    
    public String getSourceLedger(){
    	return m_sDetailSourceLedger;
    }
    public String getSourceType(){
    	return m_sDetailSourceType;
    }
    public String getComment(){
    	return m_sDetailComment;
    }
    public String getLineNumber(){
    	return m_slinenumber;
    }
    public String getsTransactionAmount(String sFormat){
    	return clsManageBigDecimals.BigDecimalToFormattedString(sFormat, m_dDetailTransactionAmount);
    }
    public BigDecimal getsTransactionAmount(){
    	return m_dDetailTransactionAmount;
    }
    public void setsTransactionAmount(BigDecimal bd){
    	m_dDetailTransactionAmount = bd;
    }
    public String getsDetailFormattedAccountID(){
    	return m_sformattedaccountid;
    }
    public void setsDetailFormattedAccountID(String sDetailFormattedAccountID){
    	m_sformattedaccountid = sDetailFormattedAccountID;
    }
}
