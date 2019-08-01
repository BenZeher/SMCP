package smgl;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;

import org.apache.commons.net.ftp.FTP;

import SMClasses.SMBatchStatuses;
import SMClasses.SMOption;
import SMDataDefinition.SMExportTypes;
import SMDataDefinition.SMTableglexportdetails;
import SMDataDefinition.SMTableglexportheaders;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsDateAndTimeConversions;
import ServletUtilities.clsFTPFunctions;
import ServletUtilities.clsServletUtilities;

public class SMGLExport extends java.lang.Object{

	private static int EXPORT_TRANSACTION_NUMBER_INCREMENT = 20;
	
	private String m_sExportFilePath;
	private String  m_sExportFileName;
	private ArrayList<SMGLExportHeader> m_HeaderRecordArray;
	private String m_sErrorMessage;
	private static final boolean bDebugMode = false;
	
	public SMGLExport(){
		m_sExportFilePath = "";
		m_sExportFileName = "";
		m_HeaderRecordArray = new ArrayList<SMGLExportHeader> (0);
		m_sErrorMessage = "";
	}
	
	public void setExportFilePath(String sExportFilePath){
		m_sExportFilePath = sExportFilePath;
	}
	public String getExportFilePath(){
		return m_sExportFilePath;
	}
	public String getFullExportFileName(){
		if ((m_sExportFilePath.equalsIgnoreCase("")) || (m_sExportFileName.equalsIgnoreCase(""))){
			return "";
		}else{
			return m_sExportFilePath + m_sExportFileName;
		}
	}
	public boolean addHeader(
			String sSourceLedger,
			String sSourceType,
			String sJournalDescription, 
			String sSourceDescription,
			String smmddyyyyDocDate,
			String smmddyyyyEntryDate,
        	String sEntryDescription,
        	String sSourceLedgerTransactionID 
			){

	    SMGLExportHeader header = new SMGLExportHeader(
	        	1,
	        	0,
	        	m_HeaderRecordArray.size() + 1,
	        	replaceCriticalStringLiterals(sSourceLedger),
	        	replaceCriticalStringLiterals(sSourceType),
	        	replaceCriticalStringLiterals(sJournalDescription),
	        	replaceCriticalStringLiterals(sSourceDescription),
	           	smmddyyyyDocDate,
	        	smmddyyyyEntryDate,
	        	sEntryDescription,
	        	sSourceLedgerTransactionID
	        	);
	    
	    m_HeaderRecordArray.add(header);
    	return true;
	}
	public void addDetail(
			java.sql.Date datTransDate,
		    BigDecimal dTransAmount,
		    String sAccountID,
		    String sComment,
		    String sDescription,
		    String sReference,
		    String sLineNumber,
		    Connection conn
		    ) throws Exception{

			String sFormattedAccountID = "";
			if (sAccountID.compareToIgnoreCase("") != 0){
				GLAccount glacct = new GLAccount(sAccountID);
				if (!glacct.load(conn)){
					throw new Exception("Error [1478816275] - could not load GL account '" + sAccountID + "' - " + glacct.getErrorMessageString());
				}
				sFormattedAccountID = glacct.getM_sformattedacctid();
			}
			SMGLExportDetail detail = new SMGLExportDetail(
					2,
		            0,
		            m_HeaderRecordArray.get(m_HeaderRecordArray.size() - 1).getBatchEntry(),
		            getNextTransactionNumber(),
		            replaceCriticalStringLiterals(sAccountID),
		            replaceCriticalStringLiterals(sFormattedAccountID),
		            dTransAmount,
		            replaceCriticalStringLiterals(sDescription),
		            replaceCriticalStringLiterals(sReference),
		            datTransDate,
		            replaceCriticalStringLiterals(m_HeaderRecordArray.get(m_HeaderRecordArray.size() - 1).getSourceLedger()),
		            replaceCriticalStringLiterals(m_HeaderRecordArray.get(m_HeaderRecordArray.size() - 1).getSourceType()),
		            replaceCriticalStringLiterals(sComment),
		            sLineNumber
			);

			m_HeaderRecordArray.get(m_HeaderRecordArray.size() - 1).addDetail(detail);
	}
	private long getNextTransactionNumber(){

		long lLastTransactionNumber;
		int iCurrentHeaderIndex = m_HeaderRecordArray.size() - 1;
		int iCurrentDetailIndex = m_HeaderRecordArray.get(iCurrentHeaderIndex).getDetailArray().size();

	    if (iCurrentDetailIndex == 0){
	        lLastTransactionNumber = 0;
	    }else{
	    	lLastTransactionNumber = m_HeaderRecordArray.get(iCurrentHeaderIndex).getDetailArray().get(iCurrentDetailIndex - 1).getTransactionNumber();
	    }

	    return lLastTransactionNumber + EXPORT_TRANSACTION_NUMBER_INCREMENT;
	}
	public boolean writeExportFile(String sSourceLedger, String sBatchTypeLabel, String sBatchNumber, int iExportType, Connection conn){

	    BufferedWriter bwExportFile;
		//Create the file name:
	    m_sExportFileName = getExportFileName(sSourceLedger, sBatchTypeLabel, sBatchNumber, "");
	    
	    if (m_sExportFilePath.equalsIgnoreCase("")){
	    	m_sErrorMessage = "Export file path has not been set.";
	    	//System.out.println("In " + this.toString() + ".writeExportFile - Export file path has not been set.");
	    	return false;
	    }
	    //Make sure the file doesn't already exist (not likely):
	    File f = new File(m_sExportFilePath + m_sExportFileName);
	    if (f.exists()){
	    	//System.out.println("In " + this.toString() + ".writeExportFile - Export file already exists.");
	    	m_sErrorMessage = "Export file '" + m_sExportFilePath + m_sExportFileName + "' already exists";
	    	return false;
	    }
	        
		try{
			bwExportFile = new BufferedWriter(new FileWriter(m_sExportFilePath + m_sExportFileName, true));
		}catch(IOException ex){
			m_sErrorMessage = "Error opening file '" + m_sExportFilePath + m_sExportFileName + "' " + ex.getMessage();
			//System.out.println("In " + this.toString() + ".writeExportFile - error opening file: " + ex.getMessage());
			return false;
		}
    
		//Header record definition:
		if (iExportType == SMExportTypes.EXPORT_TO_MAS200){
			try {
				writeToMAS200ExportFile(bwExportFile, f);
			} catch (Exception e) {
				m_sErrorMessage = e.getMessage();
				return false;
			}
		}else{
			try {
				writeToACCPACExportFile(bwExportFile, f);
			} catch (Exception e) {
				m_sErrorMessage = e.getMessage();
				return false;
			}
		}

		//If it needs to be FTP'ed to a server, do that, then remove the file:
		try {
			putFTPExport(m_sExportFilePath, m_sExportFileName, sSourceLedger, conn);
		} catch (Exception e) {
			m_sErrorMessage = e.getMessage();
			return false;
		}
		
		return true;
	}
	private void writeToACCPACExportFile(BufferedWriter bwFile, File fFile) throws Exception{
		try{
			bwFile.write("\"RECTYPE\"");
			bwFile.write(",\"BATCHID\"");
			bwFile.write(",\"BTCHENTRY\"");
			bwFile.write(",\"SRCELEDGER\"");
			bwFile.write(",\"SRCETYPE\"");
			bwFile.write(",\"JRNLDESC\"");
			bwFile.write(",\"SRCDESC\"");
			bwFile.newLine();
	    
			//Detail record definition:
			bwFile.write("\"RECTYPE\"");
			bwFile.write(",\"BATCHNBR\"");
			bwFile.write(",\"JOURNALID\"");
			bwFile.write(",\"TRANSNBR\"");
			bwFile.write(",\"ACCTID\"");
			bwFile.write(",\"TRANSAMT\"");
			bwFile.write(",\"TRANSDESC\"");
			bwFile.write(",\"TRANSREF\"");
			bwFile.write(",\"TRANSDATE\"");
			bwFile.write(",\"SRCELDGR\"");
			bwFile.write(",\"SRCETYPE\"");
			bwFile.write(",\"COMMENT\"");
			bwFile.newLine();
		}catch (IOException e){
			
			try {
				bwFile.close();
			} catch (IOException e1) {
				throw new Exception("Error [1474645639] closing export file: " + e1.getMessage());
			}
			throw new Exception("Error [1474645640] writing export file definitions: " + e.getMessage());
		}
		
		for (int i = 0; i < m_HeaderRecordArray.size(); i ++){
			try {
				writeACCPACEntryToFile(i, bwFile);
			} catch (Exception e1) {
				try{
					bwFile.close();
				} catch (IOException e){
					throw new Exception("Error [1474645641] closing export file after writing entry - " + e.getMessage() + ".");
				}
				fFile.delete();
				throw new Exception("Error [1474645651] writing entry to file - " + e1.getMessage() + ".");
			}
		}
		
		try {
			bwFile.close();
		} catch (IOException e){
			//System.out.println("In " + this.toString() + ".writeExportFile - error closing export file: " + e.getMessage());
			throw new Exception("Error [1474645641] closing export file: " + e.getMessage());
		}
	}
	private void writeToMAS200ExportFile(BufferedWriter bwFile, File fFile) throws Exception{

		try{
			bwFile.write("\"AcctNum\"");
			bwFile.write(",\"AcctDesc\"");
			bwFile.write(",\"TransDate\"");
			bwFile.write(",\"SrcJrnl\"");
			bwFile.write(",\"RegNum\"");
			bwFile.write(",\"LineSeq\"");
			bwFile.write(",\"SrcMod\"");
			bwFile.write(",\"Amt\"");
			bwFile.write(",\"Comment\"");
			bwFile.newLine();
		}catch (IOException e){
			
			try {
				bwFile.close();
			} catch (IOException e1) {
				throw new Exception("Error [1474645739] closing export file: " + e1.getMessage());
			}
			throw new Exception("Error [1474645740] writing export file definitions: " + e.getMessage());
		}
		
		for (int i = 0; i < m_HeaderRecordArray.size(); i ++){
			try {
				writeMAS200EntryToFile(i, bwFile);
			} catch (Exception e1) {
				try{
					bwFile.close();
				} catch (IOException e){
					throw new Exception("Error [1474645741] closing export file after writing entry - " + e.getMessage() + ".");
				}
				fFile.delete();
				throw new Exception("Error [1474645751] writing entry to file - " + e1.getMessage() + ".");
			}
		}
		
		try {
			bwFile.close();
		} catch (IOException e){
			//System.out.println("In " + this.toString() + ".writeExportFile - error closing export file: " + e.getMessage());
			throw new Exception("Error [1474645641] closing export file: " + e.getMessage());
		}
	}
	private void putFTPExport(String sFilePath, String sFileName, String sModuleType, Connection conn) throws Exception{
		SMOption opt = new SMOption();
		if (!opt.load(conn)){
			removeExportFile(sFilePath + sFileName);
			throw new Exception("Error loading FTP settings for export - " + opt.getErrorMessage());
		}
		//If there is no FTP URL, then assume we don't need to upload to FTP:
		if (opt.getftpexporturl().compareToIgnoreCase("") == 0){
			return;
		}
		//Otherwise, try to upload the file to the FTP server:
		String sTargetFileName = opt.getftpfileexportpath() + sFileName;
		if (sTargetFileName.compareToIgnoreCase("") == 0){
			removeExportFile(sFilePath + sFileName);
			throw new Exception("Target file name cannot be blank.");
		}
		try {
			clsFTPFunctions.putFile(
				opt.getftpexporturl(), 
				opt.getftpexportuser(), 
				opt.getftpexportpw(), 
				sFilePath + sFileName, 
				sTargetFileName,
				FTP.ASCII_FILE_TYPE
			);
		} catch (Exception e) {
			removeExportFile(sFilePath + sFileName);
			throw new Exception("Error uploading FTP export file - " + e.getMessage());
		}
		//If we succeed in sending the file, we want to delete the original:
		removeExportFile(sFilePath + sFileName);
	}
	private void removeExportFile(String sFullFileName) throws Exception{
		//System.out.println("[1381332004] "
		//		+ "sFullFileName = '" + sFullFileName + "'"
		//	);
		File fil = new File(sFullFileName);
		if (!fil.exists()) {
			//If the file doesn't exist, then just return
			return;
		}
		if (!fil.delete()){
			throw new Exception("Unable to delete export file '" + sFullFileName + "'.");
		}
	}
	private void writeACCPACEntryToFile(int iIndex, BufferedWriter bwExportFile) throws Exception{

		//Write the header line:
		try{
		bwExportFile.write(
				"\""
				+ Integer.toString(m_HeaderRecordArray.get(iIndex).getRecordType()) 
				+ "\",\""
				+ Long.toString(m_HeaderRecordArray.get(iIndex).getBatchNumber())
				+ "\",\""
				+ Long.toString(m_HeaderRecordArray.get(iIndex).getBatchEntry())
				+ "\",\""
				+ m_HeaderRecordArray.get(iIndex).getSourceLedger()
				+ "\",\""
				+ m_HeaderRecordArray.get(iIndex).getSourceType()
				+ "\",\""
				+ m_HeaderRecordArray.get(iIndex).getJournalDescription()
				+ "\",\""
				+ m_HeaderRecordArray.get(iIndex).getSourceDescription()
				+ "\""
		);
		bwExportFile.newLine();
		}catch(IOException e){
			//System.out.println("In " + this.toString() + ".writeEntry - error on entry " + iIndex + ": " + e.getMessage());
			throw new Exception("Error [1474645829] - Could not export entry " + iIndex + ": " + e.getMessage());
		}
		//Write the detail lines:
		for (int i = 0; i < m_HeaderRecordArray.get(iIndex).getDetailArray().size(); i++){
			try {
			bwExportFile.write(
					"\""
					+ Integer.toString(m_HeaderRecordArray.get(iIndex).getDetailArray().get(i).getRecordType()) 
					+ "\",\""
					+ Long.toString(m_HeaderRecordArray.get(iIndex).getDetailArray().get(i).getBatchNumber())
					+ "\",\""
					+ Long.toString(m_HeaderRecordArray.get(iIndex).getDetailArray().get(i).getJournalID())
					+ "\",\""
					+ Long.toString(m_HeaderRecordArray.get(iIndex).getDetailArray().get(i).getTransactionNumber())
					+ "\",\""
					+ m_HeaderRecordArray.get(iIndex).getDetailArray().get(i).getAccountID()
					+ "\",\""
					+ m_HeaderRecordArray.get(iIndex).getDetailArray().get(i).getsTransactionAmount("##########0.00")
					+ "\",\""
					+ m_HeaderRecordArray.get(iIndex).getDetailArray().get(i).getTransactionDescription()
					+ "\",\""
					+ m_HeaderRecordArray.get(iIndex).getDetailArray().get(i).getTransactionReference()
					+ "\",\""
					+ m_HeaderRecordArray.get(iIndex).getDetailArray().get(i).getsTransactionDate("yyyyMMdd")
					+ "\",\""
					+ m_HeaderRecordArray.get(iIndex).getDetailArray().get(i).getSourceLedger()
					+ "\",\""
					+ m_HeaderRecordArray.get(iIndex).getDetailArray().get(i).getSourceType()
					+ "\",\""
					+ m_HeaderRecordArray.get(iIndex).getDetailArray().get(i).getComment().replace("\"", "''").replace(",", ";")
					+ "\""
			);
			bwExportFile.newLine();
			}catch(IOException e){
				//System.out.println("In " + this.toString() + ".writeEntry - error on entry " + iIndex + ", line " + i + ": " + e.getMessage());
				throw new Exception("Error [1474645829] - Could not export line on entry " + iIndex + ", line " + i + ": " + e.getMessage());
			}
		}
	}
	private void writeMAS200EntryToFile(int iIndex, BufferedWriter bwExportFile) throws Exception{

		//Write the detail lines:
		for (int i = 0; i < m_HeaderRecordArray.get(iIndex).getDetailArray().size(); i++){
			try {
			bwExportFile.write(
					"\""
					+ m_HeaderRecordArray.get(iIndex).getDetailArray().get(i).getsDetailFormattedAccountID() //AcctNum
					+ "\",\""
					+ "" 	//AcctDesc
					+ "\",\""
					+ m_HeaderRecordArray.get(iIndex).getDetailArray().get(i).getsTransactionDate("yyyy-MM-dd") //TransDate
					+ "\",\""
					+ m_HeaderRecordArray.get(iIndex).getSourceType() //SrcJrnl
					+ "\",\""
					+ Long.toString(m_HeaderRecordArray.get(iIndex).getBatchNumber())  //RegNum
					+ "\",\""
					+ "" 	//LineSeq
					+ "\",\""
					+ "GL" 	//SrcMod
					+ "\",\""
					+ "\",\""
					+ m_HeaderRecordArray.get(iIndex).getDetailArray().get(i).getsTransactionAmount("##########0.00") //Amt
					+ "\",\""
					+ m_HeaderRecordArray.get(iIndex).getDetailArray().get(i).getTransactionDescription()
					+ " - "
					+ m_HeaderRecordArray.get(iIndex).getDetailArray().get(i).getTransactionReference() //Comment					
					+ "\""
			);
			bwExportFile.newLine();
			}catch(IOException e){
				//System.out.println("In " + this.toString() + ".writeEntry - error on entry " + iIndex + ", line " + i + ": " + e.getMessage());
				throw new Exception("Error [1474645929] - Could not export line on entry " + iIndex + ", line " + i + ": " + e.getMessage());
			}
		}
	}

	public void writeACCPACExportDownload(String sSourceLedger, String sBatchTypeLabel, String sBatchNumber, PrintWriter pw) throws Exception{
    
		//Header record definition:
		pw.println(
			"\"RECTYPE\""
			+ ",\"BATCHID\""
			+ ",\"BTCHENTRY\""
			+ ",\"SRCELEDGER\""
			+ ",\"SRCETYPE\""
			+ ",\"JRNLDESC\""
			+ ",\"SRCDESC\""
		);
    
		//Detail record definition:
		pw.println(
			"\"RECTYPE\""
			+ ",\"BATCHNBR\""
			+ ",\"JOURNALID\""
			+ ",\"TRANSNBR\""
			+ ",\"ACCTID\""
			+ ",\"TRANSAMT\""
			+ ",\"TRANSDESC\""
			+ ",\"TRANSREF\""
			+ ",\"TRANSDATE\""
			+ ",\"SRCELDGR\""
			+ ",\"SRCETYPE\""
			+ ",\"COMMENT\""
		);
		for (int i = 0; i < m_HeaderRecordArray.size(); i ++){
			try {
				writeACCPACDownloadEntry(i, pw);
			} catch (Exception e){
				throw new Exception("Error closing export file after writing entry - " + e.getMessage() + ".");
			}
		}
        pw.flush();
		pw.close();
	}
	public void writeMAS200ExportDownload(String sSourceLedger, String sBatchTypeLabel, String sBatchNumber, PrintWriter pw) throws Exception{
	    
		//Header record definition:
		pw.println(
			"\"AcctNum\""
			+ ",\"AcctDesc\""
			+ ",\"TransDate\""
			+ ",\"SrcJrnl\""
			+ ",\"RegNum\""
			+ ",\"LineSeq\""
			+ ",\"SrcMod\""
			+ ",\"Amt\""
			+ ",\"Comment\""
		);
		for (int i = 0; i < m_HeaderRecordArray.size(); i ++){
			try {
				writeMAS200DownloadEntry(i, pw);
			} catch (Exception e){
				throw new Exception("Error [1474648107] closing export file after writing entry - " + e.getMessage() + ".");
			}
		}
        pw.flush();
		pw.close();
	}
	private void writeACCPACDownloadEntry(int iIndex, PrintWriter out) throws Exception{
		//Write the header line:
		out.println(
				"\""
				+ Integer.toString(m_HeaderRecordArray.get(iIndex).getRecordType()) 
				+ "\",\""
				//+ Long.toString(m_HeaderRecordArray.get(iIndex).getBatchNumber())
				+ "0" //ACCPAC needs a zero for the batch number
				+ "\",\""
				+ Long.toString(m_HeaderRecordArray.get(iIndex).getBatchEntry())
				+ "\",\""
				+ m_HeaderRecordArray.get(iIndex).getSourceLedger()
				+ "\",\""
				+ m_HeaderRecordArray.get(iIndex).getSourceType()
				+ "\",\""
				+ m_HeaderRecordArray.get(iIndex).getJournalDescription()
				+ "\",\""
				+ m_HeaderRecordArray.get(iIndex).getSourceDescription()
				+ "\""
		);
		//Write the detail lines:
		for (int i = 0; i < m_HeaderRecordArray.get(iIndex).getDetailArray().size(); i++){
			String sTransactionNumber = Long.toString(m_HeaderRecordArray.get(iIndex).getDetailArray().get(i).getTransactionNumber() 
				* EXPORT_TRANSACTION_NUMBER_INCREMENT); 
			out.println(
				"\""
				+ Integer.toString(m_HeaderRecordArray.get(iIndex).getDetailArray().get(i).getRecordType()) 
				+ "\",\""
				//+ Long.toString(m_HeaderRecordArray.get(iIndex).getDetailArray().get(i).getBatchNumber())
				+ "0" //ACCPAC needs a zero for the batch number
				+ "\",\""
				+ Long.toString(m_HeaderRecordArray.get(iIndex).getDetailArray().get(i).getJournalID())
				+ "\",\""
				+ sTransactionNumber
				+ "\",\""
				+ m_HeaderRecordArray.get(iIndex).getDetailArray().get(i).getAccountID()
				+ "\",\""
				+ m_HeaderRecordArray.get(iIndex).getDetailArray().get(i).getsTransactionAmount("##########0.00")
				+ "\",\""
				+ m_HeaderRecordArray.get(iIndex).getDetailArray().get(i).getTransactionDescription()
				+ "\",\""
				+ m_HeaderRecordArray.get(iIndex).getDetailArray().get(i).getTransactionReference()
				+ "\",\""
				+ m_HeaderRecordArray.get(iIndex).getDetailArray().get(i).getsTransactionDate("yyyyMMdd")
				+ "\",\""
				+ m_HeaderRecordArray.get(iIndex).getDetailArray().get(i).getSourceLedger()
				+ "\",\""
				+ m_HeaderRecordArray.get(iIndex).getDetailArray().get(i).getSourceType()
				+ "\",\""
				+ m_HeaderRecordArray.get(iIndex).getDetailArray().get(i).getComment()
				+ "\""
			);
		}
	}
	private void writeMAS200DownloadEntry(int iIndex, PrintWriter out) throws Exception{
		
		//Write the detail lines:
		for (int i = 0; i < m_HeaderRecordArray.get(iIndex).getDetailArray().size(); i++){
			//String sTransactionNumber = Long.toString(m_HeaderRecordArray.get(iIndex).getDetailArray().get(i).getTransactionNumber() 
			//	* EXPORT_TRANSACTION_NUMBER_INCREMENT); 
			
			out.println(
				"\""
				+ m_HeaderRecordArray.get(iIndex).getDetailArray().get(i).getsDetailFormattedAccountID()  //AcctNum
				+ "\",\""
				+ ""  //AcctDesc
				+ "\",\""
				+ m_HeaderRecordArray.get(iIndex).getDetailArray().get(i).getsTransactionDate("yyyy-MM-dd")  //TransDate
				+ "\",\""
				+ m_HeaderRecordArray.get(iIndex).getSourceType()  //SrcJrnl
				+ "\",\""
				+ Long.toString(m_HeaderRecordArray.get(iIndex).getBatchNumber())  //RegNum
				+ "\",\""
				+ ""  //LineSeq
				+ "\",\""
				+ "GL"  //SrcMod
				+ "\",\""
				+ m_HeaderRecordArray.get(iIndex).getDetailArray().get(i).getsTransactionAmount("##########0.00")  //Amt
				+ "\",\""
				+ m_HeaderRecordArray.get(iIndex).getDetailArray().get(i).getTransactionDescription()
				+ " - "
				+ m_HeaderRecordArray.get(iIndex).getDetailArray().get(i).getTransactionReference()  //Comment
				+ "\""
			);
		}
	}
	public String getExportFileName(String sSourceLedger, String sBatchTypeLabel, String sBatchNumber, String sDatabaseName){
		//Create the file name:
		String sFileName = "GL-" + sSourceLedger;
	    
	    if (sDatabaseName.trim().compareToIgnoreCase("") != 0){
	    	sFileName += "-" + sDatabaseName;
	    }
	    sFileName += "-" + sBatchTypeLabel
	    	+ "-" + sBatchNumber
	    	+ "-" + clsDateAndTimeConversions.now("yyyy-MM-dd")
	    	+ "-" + clsDateAndTimeConversions.now("hh_mm_ss a")
	    	+ ".CSV";
	    return sFileName;
	}
	public void saveExport(String sBatchNumber, Connection conn) throws Exception{
		String SQL = "";
		for (int i = 0; i < m_HeaderRecordArray.size(); i ++){
			SQL = "INSERT INTO " + SMTableglexportheaders.TableName + "("
				+ SMTableglexportheaders.datdocdate
				+ ", " + SMTableglexportheaders.datentrydate
				+ ", " + SMTableglexportheaders.lbatchentry
				+ ", " + SMTableglexportheaders.irecordtype
				+ ", " + SMTableglexportheaders.lbatchnumber
				+ ", " + SMTableglexportheaders.sjournaldescription
				+ ", " + SMTableglexportheaders.ssourcedescription
				+ ", " + SMTableglexportheaders.ssourceledger
				+ ", " + SMTableglexportheaders.sssourcetype
				+ ", " + SMTableglexportheaders.sentrydescription
				+ ") VALUES ("
				+ "'" + ServletUtilities.clsDateAndTimeConversions.convertDateFormat(
					m_HeaderRecordArray.get(i).getDocDate(), 
					clsServletUtilities.DATE_FORMAT_FOR_DISPLAY, 
					clsServletUtilities.DATE_FORMAT_FOR_SQL, 
					clsServletUtilities.EMPTY_SQL_DATE_VALUE
					) + "'"
				+ ", '" + ServletUtilities.clsDateAndTimeConversions.convertDateFormat(
					m_HeaderRecordArray.get(i).getEntryDate(), 
					clsServletUtilities.DATE_FORMAT_FOR_DISPLAY, 
					clsServletUtilities.DATE_FORMAT_FOR_SQL, 
					clsServletUtilities.EMPTY_SQL_DATE_VALUE
				) + "'"
				+ ", " + Long.toString(m_HeaderRecordArray.get(i).getBatchEntry())
				+ ", " + Integer.toString(m_HeaderRecordArray.get(i).getRecordType()) 
				+ ", " + sBatchNumber
				+ ", '" + sizeFieldForSaving(m_HeaderRecordArray.get(i).getJournalDescription().trim(), SMTableglexportheaders.sjournaldescriptionlength) + "'"
				+ ", '" + sizeFieldForSaving(m_HeaderRecordArray.get(i).getSourceDescription().trim(), SMTableglexportheaders.ssourcedescriptionlength) + "'"
				+ ", '" + sizeFieldForSaving(m_HeaderRecordArray.get(i).getSourceLedger().trim(), SMTableglexportheaders.ssourceledgerlength) + "'"
				+ ", '" + sizeFieldForSaving(m_HeaderRecordArray.get(i).getSourceType().trim(), SMTableglexportheaders.ssourcetypelength) + "'"
						+ ", '" + sizeFieldForSaving(m_HeaderRecordArray.get(i).getEntryDescription().trim(), SMTableglexportheaders.sentrydescriptionlength) + "'"
				+ ")";
			
			try {
				Statement stmt = conn.createStatement();
				stmt.execute(SQL);
			} catch (Exception e) {
				if (bDebugMode){
					System.out.println("In " + this.toString() + " - " + "Error inserting GL header with SQL: " + SQL + " - error: " + e.getMessage());
				}
				throw new Exception("Error inserting GL header with SQL: " + SQL + " - error: " + e.getMessage());
			}
			
			//Write the detail lines:
			for (int j = 0; j < m_HeaderRecordArray.get(i).getDetailArray().size(); j++){
				SQL = "INSERT INTO " + SMTableglexportdetails.TableName + "("
					+ SMTableglexportdetails.bddetailtransactionamount
					+ ", " + SMTableglexportdetails.datdetailtransactiondate
					+ ", " + SMTableglexportdetails.lbatchentry
					+ ", " + SMTableglexportdetails.irecordtype
					+ ", " + SMTableglexportdetails.lbatchnumber
					+ ", " + SMTableglexportdetails.ldetailjournalid
					+ ", " + SMTableglexportdetails.ldetailtransactionnumber
					+ ", " + SMTableglexportdetails.llinenumber
					+ ", " + SMTableglexportdetails.sdetailaccountid
					+ ", " + SMTableglexportdetails.sdetailformattedaccountid
					+ ", " + SMTableglexportdetails.sdetailcomment
					+ ", " + SMTableglexportdetails.sdetailsourceledger
					+ ", " + SMTableglexportdetails.sdetailsourcetype
					+ ", " + SMTableglexportdetails.sdetailtransactiondescription
					+ ", " + SMTableglexportdetails.sdetailtransactionreference
					+ ") VALUES ("
					+ m_HeaderRecordArray.get(i).getDetailArray().get(j).getsTransactionAmount("##########0.00")
					+ ", '" + m_HeaderRecordArray.get(i).getDetailArray().get(j).getsTransactionDate("yyyy-MM-dd") + "'"
					+ ", " + Long.toString(m_HeaderRecordArray.get(i).getBatchEntry())
					+ ", " + Integer.toString(m_HeaderRecordArray.get(i).getDetailArray().get(j).getRecordType())
					+ ", " + sBatchNumber
					+ ", " + Long.toString(m_HeaderRecordArray.get(i).getDetailArray().get(j).getJournalID())
					+ ", " + Integer.toString(j + 1)
					+ ", " + m_HeaderRecordArray.get(i).getDetailArray().get(j).getLineNumber()
					+ ", '" + sizeFieldForSaving(m_HeaderRecordArray.get(i).getDetailArray().get(j).getAccountID().trim(), SMTableglexportdetails.sdetailaccountidlength) + "'"
					+ ", '" + sizeFieldForSaving(m_HeaderRecordArray.get(i).getDetailArray().get(j).getsDetailFormattedAccountID().trim(), SMTableglexportdetails.sdetailformattedaccountidlength) + "'"
					+ ", '" + sizeFieldForSaving(m_HeaderRecordArray.get(i).getDetailArray().get(j).getComment().trim(), SMTableglexportdetails.sdetailcommentlength) + "'"
					+ ", '" + sizeFieldForSaving(m_HeaderRecordArray.get(i).getDetailArray().get(j).getSourceLedger().trim(), SMTableglexportdetails.sdetailsourceledgerlength) + "'"
					+ ", '" + sizeFieldForSaving(m_HeaderRecordArray.get(i).getDetailArray().get(j).getSourceType().trim(), SMTableglexportdetails.sdetailsourcetypelength) + "'"
					+ ", '" + sizeFieldForSaving(m_HeaderRecordArray.get(i).getDetailArray().get(j).getTransactionDescription().trim(), SMTableglexportdetails.sdetailtransactiondescriptionlength) + "'"
					+ ", '" + sizeFieldForSaving(m_HeaderRecordArray.get(i).getDetailArray().get(j).getTransactionReference().trim(), SMTableglexportdetails.sdetailtransactionreferencelength) + "'"
					+ ")";
				
				try {
					Statement stmt = conn.createStatement();
					stmt.execute(SQL);
				} catch (Exception e) {
					if (bDebugMode){
						System.out.println("In " + this.toString() + " - " + "Error inserting GL header with SQL: " + SQL + " - error: " + e.getMessage());
					}
					throw new Exception("Error inserting GL detail with SQL: " + SQL + " - error: " + e.getMessage());
				}
			}
		}
	}
	
	public static String replaceCriticalStringLiterals(String sSourceString){
		if (sSourceString == null){
			return null;
		}
		return sSourceString.replace("\"", "''").replace(",", ";");
	}
	
	public static String sizeFieldForSaving(String sFieldValue, int iMaxFieldLength){
		String s = "";
		
		if (sFieldValue == null){
			return s;
		}
		
		//First, format the field for recording in the database:
		s = clsDatabaseFunctions.FormatSQLStatement(sFieldValue);
		//If it's not too long, then just send it back:
		if (s.length() <= iMaxFieldLength){
			return s;
		}
		
		//But if it's too long, then we have to truncate:
		s = s.substring(0, iMaxFieldLength);
		
		//If the string is ONLY one character, and that character is a 'literal', then we have to 
		//replace it with a blank and send it back, because we can't send a single literal character
		//over to be recorded in the database:
		if (s.length() == 1){
			if (
				(s.compareToIgnoreCase("'") == 0)
				|| (s.compareToIgnoreCase("\"") == 0)
				|| (s.compareToIgnoreCase("\\") == 0)
			){
				return " ";
			}
			//Otherwise, we're safe to just return the string:
			else{
				return s;
			}
		}
		
		//Now make sure that we didn't chop off part of a doubled literal character:
		if (
			//If the last character IS a literal, but the preceding one is NOT, then we can't
			//try to send that to the database:
			(
				(s.substring(s.length() - 1, s.length()).compareToIgnoreCase("'") == 0)
				&& (s.substring(s.length() - 2, s.length()).compareToIgnoreCase("'") != 0)
			)

			||
			(
				(s.substring(s.length() - 1, s.length()).compareToIgnoreCase("\"") == 0)
				&& (s.substring(s.length() - 2, s.length()).compareToIgnoreCase("\"") != 0)
			)

			||
			(
				(s.substring(s.length() - 1, s.length()).compareToIgnoreCase("'\\") == 0)
				&& (s.substring(s.length() - 2, s.length()).compareToIgnoreCase("\\") != 0)
			)

		){
			//Then the only thing we can do is change that final literal to a blank and let it go:
			s = s.substring(0, s.length() - 1) + " ";
			return s;
		}else{
			return s;
		}
	}
	//TODO - possibly delete this function if we don't need it - TJR 1/14/2014
	public void removeRecords(String sBatchNumber, Connection conn) throws Exception{
		String SQL = "DELETE FROM " + SMTableglexportheaders.TableName
			+ " WHERE ("
				+ "(" + SMTableglexportheaders.lbatchnumber + " = " + sBatchNumber + ")"
				+ " AND (" + SMTableglexportheaders.ssourceledger + " = '" + m_HeaderRecordArray.get(0).getSourceLedger().trim() + "'"
			+ ")"
		;
		try {
			Statement stmt = conn.createStatement();
			stmt.execute(SQL);
		} catch (Exception e1) {
			throw new Exception("Error removing glexport header record with SQL: " + SQL + " - " + e1.getMessage());
		}
		
		SQL = "DELETE FROM " + SMTableglexportdetails.TableName
			+ " WHERE ("
				+ "(" + SMTableglexportdetails.lbatchnumber + " = " + sBatchNumber + ")"
				+ " AND (" + SMTableglexportdetails.sdetailsourceledger + " = '" + m_HeaderRecordArray.get(0).getSourceLedger().trim() + "'"
			+ ")"
		;
		try {
			Statement stmt = conn.createStatement();
			stmt.execute(SQL);
		} catch (Exception e1) {
			throw new Exception("Error removing glexport header record with SQL: " + SQL + " - " + e1.getMessage());
		}
	}
	public void loadExport(String sBatchNumber, String sSourceLedger, Connection conn) throws Exception{
		
		m_HeaderRecordArray.clear();
		
		String SQL = "SELECT * FROM " + SMTableglexportheaders.TableName
			+ " LEFT JOIN " + SMTableglexportdetails.TableName 
			+ " ON ("
				+ "(" + SMTableglexportheaders.TableName + "." + SMTableglexportdetails.lbatchnumber + " = " 
				+ SMTableglexportdetails.TableName + "." + SMTableglexportdetails.lbatchnumber + ")"
				+ " AND (" + SMTableglexportheaders.TableName + "." + SMTableglexportheaders.lbatchentry + " = " 
				+ SMTableglexportdetails.TableName + "." + SMTableglexportdetails.lbatchentry + ")"
				+ " AND (" + SMTableglexportheaders.TableName + "." + SMTableglexportheaders.ssourceledger + " = " 
				+ SMTableglexportdetails.TableName + "." + SMTableglexportdetails.sdetailsourceledger + ")"
			+ ")"
			+ " WHERE ("
				+ "(" + SMTableglexportheaders.TableName + "." + SMTableglexportheaders.lbatchnumber + " = " + sBatchNumber + ")"
				+ " AND (" + SMTableglexportheaders.TableName + "." + SMTableglexportheaders.ssourceledger + " = '" + sSourceLedger + "')"
			+ ")"
		;
		try {
			if (bDebugMode){
				System.out.println("In " + this.toString() + " - SQL = " + SQL);
			}
			ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
			long lCurrentBatchEntry = 0;
			while (rs.next()){
				//If this is the first record for this batch entry, then add a new header record:
				if (rs.getLong(SMTableglexportheaders.TableName + "." + SMTableglexportheaders.lbatchentry) != lCurrentBatchEntry){
					m_HeaderRecordArray.add(new SMGLExportHeader(
					    	rs.getInt(SMTableglexportheaders.TableName + "." + SMTableglexportheaders.irecordtype),
							rs.getLong(SMTableglexportheaders.TableName + "." + SMTableglexportheaders.lbatchnumber),
							rs.getLong(SMTableglexportheaders.TableName + "." + SMTableglexportheaders.lbatchentry),
							replaceCriticalStringLiterals(rs.getString(SMTableglexportheaders.TableName + "." + SMTableglexportheaders.ssourceledger)),
							replaceCriticalStringLiterals(rs.getString(SMTableglexportheaders.TableName + "." + SMTableglexportheaders.sssourcetype)),
							replaceCriticalStringLiterals(rs.getString(SMTableglexportheaders.TableName + "." + SMTableglexportheaders.sjournaldescription)),
							replaceCriticalStringLiterals(rs.getString(SMTableglexportheaders.TableName + "." + SMTableglexportheaders.ssourcedescription)),
					    	ServletUtilities.clsDateAndTimeConversions.convertDateFormat(
					    		rs.getString(SMTableglexportheaders.TableName + "." + SMTableglexportheaders.datdocdate), 
					    		clsServletUtilities.DATE_FORMAT_FOR_SQL, 
					    		clsServletUtilities.DATE_FORMAT_FOR_DISPLAY, 
					    		clsServletUtilities.EMPTY_DATE_VALUE
					    		),
					    	ServletUtilities.clsDateAndTimeConversions.convertDateFormat(
					    		rs.getString(SMTableglexportheaders.TableName + "." + SMTableglexportheaders.datentrydate), 
					    		clsServletUtilities.DATE_FORMAT_FOR_SQL, 
					    		clsServletUtilities.DATE_FORMAT_FOR_DISPLAY, 
					    		clsServletUtilities.EMPTY_DATE_VALUE
					    		),
					    	replaceCriticalStringLiterals(rs.getString(SMTableglexportheaders.TableName + "." + SMTableglexportheaders.sentrydescription)),
					    	rs.getString(SMTableglexportheaders.TableName + "." + SMTableglexportheaders.ssourceledgertransactionid)
						));
					lCurrentBatchEntry = rs.getLong(SMTableglexportheaders.TableName + "." + SMTableglexportheaders.lbatchentry);
				}
				//For every record, add a detail to the current header:
				m_HeaderRecordArray.get(m_HeaderRecordArray.size() - 1).addDetail(
					new SMGLExportDetail(
						rs.getInt(SMTableglexportdetails.TableName + "." + SMTableglexportdetails.irecordtype),
						rs.getLong(SMTableglexportdetails.TableName + "." + SMTableglexportdetails.lbatchnumber),
						rs.getLong(SMTableglexportdetails.TableName + "." + SMTableglexportdetails.lbatchentry),
						rs.getLong(SMTableglexportdetails.TableName + "." + SMTableglexportdetails.ldetailtransactionnumber),
						replaceCriticalStringLiterals(rs.getString(SMTableglexportdetails.TableName + "." + SMTableglexportdetails.sdetailaccountid)),
						replaceCriticalStringLiterals(rs.getString(SMTableglexportdetails.TableName + "." + SMTableglexportdetails.sdetailformattedaccountid)),
			            rs.getBigDecimal(SMTableglexportdetails.TableName + "." + SMTableglexportdetails.bddetailtransactionamount),
			            replaceCriticalStringLiterals(rs.getString(SMTableglexportdetails.TableName + "." + SMTableglexportdetails.sdetailtransactiondescription)),
			            replaceCriticalStringLiterals(rs.getString(SMTableglexportdetails.TableName + "." + SMTableglexportdetails.sdetailtransactionreference)),
			            rs.getDate(SMTableglexportdetails.TableName + "." + SMTableglexportdetails.datdetailtransactiondate),
			            replaceCriticalStringLiterals(rs.getString(SMTableglexportdetails.TableName + "." + SMTableglexportdetails.sdetailsourceledger)),
			            replaceCriticalStringLiterals(rs.getString(SMTableglexportdetails.TableName + "." + SMTableglexportdetails.sdetailsourcetype)),
			            replaceCriticalStringLiterals(rs.getString(SMTableglexportdetails.TableName + "." + SMTableglexportdetails.sdetailcomment)),
			            Long.toString(rs.getLong(SMTableglexportdetails.TableName + "." + SMTableglexportdetails.llinenumber))
					)
				);
			}
			rs.close();
		} catch (Exception e) {
			throw new Exception("Error reading export headers with SQL: " + SQL + " - " + e.getMessage());
		}
		if (bDebugMode){
			System.out.println("In " + this.toString() + " - m_HeaderRecordArray.size() = " + m_HeaderRecordArray.size());
			for (int i = 0; i < m_HeaderRecordArray.size(); i ++){
				writeACCPACDownloadEntry(i, new PrintWriter(System.out));
			}
		}
	}
	public GLTransactionBatch createGLTransactionBatch(
		Connection conn,
		String sCreatedBy,
		String sLastEditedBy,
		String sBatchDate,
		String sBatchDescription) throws Exception{
		GLTransactionBatch gltransactionbatch = new GLTransactionBatch("-1");
		
		gltransactionbatch.setlcreatedby(sCreatedBy);
		
		//*************************************
		gltransactionbatch.setlcreatedby(sCreatedBy);
		gltransactionbatch.setllasteditedby(sLastEditedBy);
		gltransactionbatch.setsbatchdate(sBatchDate);
		gltransactionbatch.setsbatchdescription(sBatchDescription);
		gltransactionbatch.setsbatchstatus(Integer.toString(SMBatchStatuses.IMPORTED));

		//System.out.println("[1556909965] - in createGLTransactionBatch.");
		//System.out.println("[1556909966] - m_arrBatchEntries.size() = '" + m_arrBatchEntries.size() + "'.");
		
		for (int i = 0; i < m_HeaderRecordArray.size(); i++){
			//Populate an entry:
			GLTransactionBatchEntry glentry = new GLTransactionBatchEntry();
			glentry.setsautoreverse("0");
			glentry.setsbatchnumber(Long.toString(m_HeaderRecordArray.get(i).getBatchNumber()));
			glentry.setsdatdocdate(m_HeaderRecordArray.get(i).getDocDate());
			//TJR - 7/3/2019 - this 'entry date' is actually the BATCH date because AR entries do not actually have an entry date:
			glentry.setsdatentrydate(m_HeaderRecordArray.get(i).getEntryDate());
			glentry.setsentrydescription(m_HeaderRecordArray.get(i).getEntryDescription());
			glentry.setsentrynumber(Long.toString(m_HeaderRecordArray.get(i).getBatchEntry()));
			int iFiscalYear = GLFiscalYear.getFiscalYearForSelectedDate(glentry.getsdatentrydate(), conn);
			int iFiscalPeriod = GLFiscalYear.getFiscalPeriodForSelectedDate(glentry.getsdatentrydate(), conn);
			glentry.setsfiscalperiod(Integer.toString(iFiscalPeriod));
			glentry.setsfiscalyear(Integer.toString(iFiscalYear));
			glentry.setssourceledger(m_HeaderRecordArray.get(i).getSourceLedger());
			glentry.setssourceledgertransactionlineid("0");
			
			//Now add the lines:
			for (int j = 0; j < m_HeaderRecordArray.get(i).getDetailArray().size(); j++){
				SMGLExportDetail detail = m_HeaderRecordArray.get(i).getDetailArray().get(j);
				if (detail.getsTransactionAmount().compareTo(BigDecimal.ZERO) != 0){
					GLTransactionBatchLine glline = new GLTransactionBatchLine();
					glline.setAmount(detail.getsTransactionAmount("#########.00"));
					glline.setsacctid(detail.getAccountID());
					glline.setsbatchnumber(glentry.getsbatchnumber());
					glline.setscomment(detail.getComment());
					glline.setsdescription(detail.getTransactionDescription());
					glline.setsentrynumber(glentry.getsentrynumber());
					glline.setslinenumber(detail.getLineNumber());
					glline.setsreference(detail.getTransactionReference());
					glline.setssourceledger(glentry.getssourceledger());
					glline.setssourcetype(detail.getSourceType());
					glline.setstransactiondate(detail.getsTransactionDate(clsServletUtilities.DATE_FORMAT_FOR_DISPLAY));
					glentry.addLine(glline);
				}
			}
			//Finally, add the entry with the lines to the transaction batch:
			gltransactionbatch.addBatchEntry(glentry);
		}
	
		return gltransactionbatch;
	}
	public String getErrorMessage (){
		return m_sErrorMessage;
	}
}
