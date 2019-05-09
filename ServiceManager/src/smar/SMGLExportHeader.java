package smar;

import java.util.ArrayList;

public class SMGLExportHeader extends java.lang.Object{

    private int m_iHeaderRecordType;
    private long m_lHeaderBatchNumber;
    private long m_lHeaderBatchEntry;
    private String m_sHeaderSourceLedger;
    private String m_sHeaderSourceType;
    private String m_sHeaderJournalDescription;
    private String m_sHeaderSourceDescription;
    private String m_sDocDate;
    private String m_sEntryDate;
    private String m_sEntryDescription;
    private ArrayList<SMGLExportDetail> m_ExportDetailArray;
    
    SMGLExportHeader(
    	int iHeaderRecordType,
    	long lHeaderBatchNumber,
    	long lHeaderBatchEntry,
    	String sHeaderSourceLedger,
    	String sHeaderSourceType,
    	String sHeaderJournalDescription,
    	String sHeaderSourceDescription,
    	String smmddyyyyDocDate,
    	String smmddyyyyEntryDate,
    	String sEntryDescription

    ){
        m_iHeaderRecordType = iHeaderRecordType;
        m_lHeaderBatchNumber = lHeaderBatchNumber;
        m_lHeaderBatchEntry = lHeaderBatchEntry;
        m_sHeaderSourceLedger = sHeaderSourceLedger;
        m_sHeaderSourceType = sHeaderSourceType;
        m_sHeaderJournalDescription = sHeaderJournalDescription;
        m_sHeaderSourceDescription = sHeaderSourceDescription;
        m_sDocDate = smmddyyyyDocDate;
        m_sEntryDate = smmddyyyyEntryDate;
        m_sEntryDescription = sEntryDescription;
        m_ExportDetailArray = new ArrayList<SMGLExportDetail>(0);
    	
    }

	public int getRecordType(){
		return m_iHeaderRecordType;
	}
	public long getBatchNumber(){
		return m_lHeaderBatchNumber;
	}
	public long getBatchEntry(){
		return m_lHeaderBatchEntry;
	}
	public String getSourceLedger(){
		return m_sHeaderSourceLedger;
	}
	public String getSourceType(){
		return m_sHeaderSourceType;
	}
	public String getJournalDescription(){
		return m_sHeaderJournalDescription;
	}
	public String getSourceDescription(){
		return m_sHeaderSourceDescription;
	}
	public String getDocDate(){
		return m_sDocDate;
	}
	public String getEntryDate(){
		return m_sEntryDate;
	}
	public String getEntryDescription(){
		return m_sEntryDescription;
	}
	public void addDetail(SMGLExportDetail detail){
		m_ExportDetailArray.add(detail);
	}
	public ArrayList<SMGLExportDetail> getDetailArray(){
		return m_ExportDetailArray;
	}
}
