package smic;

import java.math.BigDecimal;
import java.util.ArrayList;

import ServletUtilities.clsDateAndTimeConversions;
import ServletUtilities.clsManageBigDecimals;

public class ICAPExportHeader extends java.lang.Object{

    private int m_iHeaderRecordType;
    private long m_lHeaderBatchNumber;
    private long m_lHeaderItemCounter;
    private String m_sVendor;
    private String m_sDocNumber;
    private int m_iTransactionType;
    private int m_iDocumentType;
    private String m_sPONumber;
    private String m_sInvoiceDesc;
    private java.sql.Date m_datInvoiceDate;
    private String m_sTermsCode;
    private java.sql.Date m_datDueDate;
    private java.sql.Date m_datDiscountDate;
    private BigDecimal m_bdDiscountPercentage;
    private BigDecimal m_bdDiscountAmtAvailable;
    private BigDecimal m_bdAmountDue; //Doesn't include discount amt
    private BigDecimal m_bdAmountTotal; //Includes discount amt
    private java.sql.Date m_datPostingDate;
    private ArrayList<ICAPExportDetail> m_ExportDetailArray;
    private ArrayList<ICAPExportPaymentSchedule> m_ExportPaymentArray;
    
    ICAPExportHeader(
    	int iHeaderRecordType,
    	long lHeaderBatchNumber,
    	long lHeaderItemCounter,
    	String sVendor,
    	String sDocNumber,
    	int iTransactionType,
    	int iDocumentType,
    	String sPONumber,
    	String sInvoiceDescription,
    	java.sql.Date datInvoiceDate,
    	String sTermsCode,
    	java.sql.Date datDueDate,
    	java.sql.Date datDiscountDate,
    	BigDecimal bdDiscountPercentage,
    	BigDecimal bdDiscountAmtAvailable,
    	BigDecimal bdAmountDue,
    	BigDecimal bdAmountTotal,
    	java.sql.Date datPostingDate
    ){
        m_iHeaderRecordType = iHeaderRecordType;
        m_lHeaderBatchNumber = lHeaderBatchNumber;
        m_lHeaderItemCounter = lHeaderItemCounter;
        m_sVendor = sVendor;
        m_sDocNumber = sDocNumber;
        m_iTransactionType = iTransactionType;
        m_iDocumentType = iDocumentType;
        m_sPONumber = sPONumber;
        m_sInvoiceDesc = sInvoiceDescription.replace(",\"", ";\"").replace("\",", "\";");
        m_datInvoiceDate = datInvoiceDate;
        m_sTermsCode = sTermsCode;
        m_datDueDate = datDueDate;
        m_datDiscountDate = datDiscountDate;
        m_bdDiscountPercentage = bdDiscountPercentage;
        m_bdDiscountAmtAvailable = bdDiscountAmtAvailable;
        m_bdAmountDue = bdAmountDue;
        m_bdAmountTotal = bdAmountTotal;
        m_datPostingDate = datPostingDate;
        m_ExportDetailArray = new ArrayList<ICAPExportDetail>(0);
        m_ExportPaymentArray = new ArrayList<ICAPExportPaymentSchedule>(0);
    	
    }

	public int getRecordType(){
		return m_iHeaderRecordType;
	}
	public long getBatchNumber(){
		return m_lHeaderBatchNumber;
	}
	public long getItemCounter(){
		return m_lHeaderItemCounter;
	}
	public String getVendor(){
		return m_sVendor;
	}
	public String getdocNumber(){
		return m_sDocNumber;
	}
	public int getTransactionType(){
		return m_iTransactionType;
	}
	public int getDocumentType(){
		return m_iDocumentType;
	}
	public String getPONumber(){
		return m_sPONumber;
	}
	public String getInvoiceDescription(){
		return m_sInvoiceDesc;
	}
    public String getsInvoiceDate(String sFormat){
    	return clsDateAndTimeConversions.utilDateToString(m_datInvoiceDate, sFormat);
    }
	public String getTermsCode(){
		return m_sTermsCode;
	}
    public String getsDueDate(String sFormat){
    	return clsDateAndTimeConversions.utilDateToString(m_datDueDate, sFormat);
    }
    public String getsDiscountDate(String sFormat){
   		return clsDateAndTimeConversions.utilDateToString(m_datDiscountDate, sFormat);
    }
    public String getsDiscountPercentage(String sFormat){
    	return clsManageBigDecimals.BigDecimalToFormattedString(sFormat, m_bdDiscountPercentage);
    }
    public String getsDiscountAmtAvailable(String sFormat){
    	return clsManageBigDecimals.BigDecimalToFormattedString(sFormat, m_bdDiscountAmtAvailable);
    }
    public String getsAmountDue(String sFormat){
    	return clsManageBigDecimals.BigDecimalToFormattedString(sFormat, m_bdAmountDue);
    }
    public String getsAmountTotal(String sFormat){
    	return clsManageBigDecimals.BigDecimalToFormattedString(sFormat, m_bdAmountTotal);
    }
    public String getsPostingDate(String sFormat){
    	return clsDateAndTimeConversions.utilDateToString(m_datPostingDate, sFormat);
    }
	public void addDetail(ICAPExportDetail detail){
		m_ExportDetailArray.add(detail);
	}
	public void addPaymentSchedule(ICAPExportPaymentSchedule schedule){
		m_ExportPaymentArray.add(schedule);
	}
	public ArrayList<ICAPExportDetail> getDetailArray(){
		return m_ExportDetailArray;
	}
	public ArrayList<ICAPExportPaymentSchedule> getPaymentArray(){
		return m_ExportPaymentArray;
	}
}
