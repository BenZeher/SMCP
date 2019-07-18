package smic;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;

import org.apache.commons.net.ftp.FTP;

import SMClasses.SMOption;
import SMDataDefinition.SMExportTypes;
import SMDataDefinition.SMTableicpoinvoiceheaders;
import ServletUtilities.*;

public class ICAPExport extends java.lang.Object{

	private static int TRANSACTION_NUMBER_INCREMENT = 20;
	
	private String m_sExportFilePath;
	private String  m_sExportFileName;
	private ArrayList<ICAPExportHeader> m_HeaderRecordArray;
	private String m_sErrorMessage;
	
	public ICAPExport(){
		m_sExportFilePath = "";
		m_sExportFileName = "";
		m_HeaderRecordArray = new ArrayList<ICAPExportHeader> (0);
		m_sErrorMessage = "";
	}
	
	public void setExportFilePath(String sExportFilePath){
		m_sExportFilePath = sExportFilePath;
	}
	public String getFullExportFileName(){
		if ((m_sExportFilePath.equalsIgnoreCase("")) || (m_sExportFileName.equalsIgnoreCase(""))){
			return "";
		}else{
			return m_sExportFilePath + m_sExportFileName;
		}
	}
	public boolean addHeader(
			String sVendor,
			String sDocNumber,
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

		ICAPExportHeader header = new ICAPExportHeader(
	    	1,
	    	0,
	    	m_HeaderRecordArray.size() + 1,
	    	sVendor,
	    	sDocNumber,
	    	1,
	    	12,
	    	sPONumber,
	    	sInvoiceDescription,
	    	datInvoiceDate,
	    	sTermsCode,
	    	datDueDate,
	    	datDiscountDate,
	    	bdDiscountPercentage,
	    	bdDiscountAmtAvailable,
	    	bdAmountDue,
	    	bdAmountTotal,
	    	datPostingDate
		);
	    
	    m_HeaderRecordArray.add(header);
    	return true;
	}
	public void addDetail(
			String sDetailDescription,
		    String sDetailGLAcct,
		    BigDecimal bdDetailDistributionAmount,
		    java.sql.Date datBillingDate
		    ){

		ICAPExportDetail detail = new ICAPExportDetail(
    		2,
            0,
            m_HeaderRecordArray.get(m_HeaderRecordArray.size() - 1).getItemCounter(),
            getNextDetailTransactionNumber(),
            sDetailDescription,
            sDetailGLAcct,
            bdDetailDistributionAmount,
            datBillingDate
		);
		m_HeaderRecordArray.get(m_HeaderRecordArray.size() - 1).addDetail(detail);
			
	}
	public void addPaymentSchedule(
			java.sql.Date datPaymentDateDue,
			BigDecimal bdPaymentAmountDue,
			java.sql.Date datPaymentDiscountDate,
			BigDecimal bdDiscountAmount
		    ){
		ICAPExportPaymentSchedule schedule = new ICAPExportPaymentSchedule(
			3,
	        0,
	        m_HeaderRecordArray.get(m_HeaderRecordArray.size() - 1).getItemCounter(),
	        1, //We only add one payment schedule
	        datPaymentDateDue,
	        bdPaymentAmountDue,
	        datPaymentDiscountDate,
	        bdDiscountAmount
        );

		m_HeaderRecordArray.get(m_HeaderRecordArray.size() - 1).addPaymentSchedule(schedule);
			
	}

	private long getNextDetailTransactionNumber(){

		long lLastDetailTransactionNumber;
		int iCurrentHeaderIndex = m_HeaderRecordArray.size() - 1;
		int iCurrentDetailIndex = m_HeaderRecordArray.get(iCurrentHeaderIndex).getDetailArray().size();

	    if (iCurrentDetailIndex == 0){
	    	lLastDetailTransactionNumber = 0;
	    }else{
	    	lLastDetailTransactionNumber = m_HeaderRecordArray.get(iCurrentHeaderIndex).getDetailArray().get(iCurrentDetailIndex - 1).getTransactionNumber();
	    }

	    return lLastDetailTransactionNumber + TRANSACTION_NUMBER_INCREMENT;
	}

	public boolean writeExportFile(String sSourceLedger, String sBatchTypeLabel, String sExportSequenceNumber, Connection conn){

		//Create the file name:
	    m_sExportFileName = "AP-" + sSourceLedger 
	    	+ "-" + sBatchTypeLabel
	    	+ "-" + sExportSequenceNumber
	    	+ "-" + clsDateAndTimeConversions.now("yyyy-MM-dd")
	    	+ " " + clsDateAndTimeConversions.now("hh_mm_ss a")
	    	+ ".CSV";
	    
	    if (m_sExportFilePath.equalsIgnoreCase("")){
	    	m_sErrorMessage = "Export file path has not been set.";
	    	return false;
	    }
	    //Make sure the file doesn't already exist (not likely):
	    File f = new File(m_sExportFilePath + m_sExportFileName);
	    if (f.exists()){
	    	//System.out.println("In " + this.toString() + ".writeExportFile - Export file already exists.");
	    	m_sErrorMessage = "Export file '" + m_sExportFilePath + m_sExportFileName + "' already exists";
	    	return false;
	    }
	        
	    BufferedWriter bwExportFile;
		try{
			bwExportFile = new BufferedWriter(new FileWriter(m_sExportFilePath + m_sExportFileName, true));
		}catch(IOException ex){
			m_sErrorMessage = "Error opening file '" + m_sExportFilePath + m_sExportFileName + "' " + ex.getMessage();
			//System.out.println("In " + this.toString() + ".writeExportFile - error opening file: " + ex.getMessage());
			return false;
		}
		
		//* - call the write function here:
		ICOption icopt = new ICOption();
		icopt.load(conn);
		if (icopt.getExportTo() == SMExportTypes.EXPORT_TO_MAS200){
			try {
				writeMAS200ExportFile(bwExportFile, f);
			} catch (Exception e) {
				m_sErrorMessage = "Error writing MAS 200 Export file - " + e.getMessage();
				return false;
			}
		}else{
			try {
				writeACCPACExportFile(bwExportFile, f);
			} catch (Exception e) {
				m_sErrorMessage = "Error writing ACCPAC Export file - " + e.getMessage();
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
	private void writeACCPACExportFile(BufferedWriter bwFile, File fFile) throws Exception{
		//Header record definition:
		try{
			bwFile.write("\"RECTYPE\"");
			bwFile.write(",\"CNTBTCH\"");
			bwFile.write(",\"CNTITEM\"");
			bwFile.write(",\"IDVEND\"");
			bwFile.write(",\"IDINVC\"");
			bwFile.write(",\"TEXTTRX\"");
			bwFile.write(",\"IDTRX\"");
			bwFile.write(",\"PONBR\"");
			bwFile.write(",\"INVCDESC\"");
			bwFile.write(",\"DATEINVC\""); // ACCPAC 'Invoice date'
			bwFile.write(",\"TERMCODE\"");
			bwFile.write(",\"DATEDUE\"");
			bwFile.write(",\"DATEDISC\"");
			bwFile.write(",\"PCTDISC\"");
			bwFile.write(",\"AMTDISCAVL\"");
			bwFile.write(",\"AMTDUE\"");
			bwFile.write(",\"AMTGROSTOT\"");
			bwFile.write(",\"DATEBUS\""); // ACCPAC 'Posting date' - this is the 'date entered' on the IC PO Invoice entry screen
												// This sets the period in ACCPAC
			bwFile.newLine();
			
			/*
			 * In ACCPAC, the GL batch carries two (meaningful) dates: the GL batch detail carries a date, that appears on the screen, called
			 * 'Transaction date' ('TRANSDATE').  On the header, there is a 'Date', which is called, underneath, 'DATEENTRY'
			 */
			
			
			//Other dates in the ACCPAC header:
			//DATEASOF - Date As Of
			//DATEPRCS - Date generated
			
			//Detail record definition:
			bwFile.write("\"RECTYPE\"");
			bwFile.write(",\"CNTBTCH\"");
			bwFile.write(",\"CNTITEM\"");
			bwFile.write(",\"CNTLINE\"");
			bwFile.write(",\"TEXTDESC\"");
			bwFile.write(",\"IDGLACCT\"");
			bwFile.write(",\"AMTDIST\"");
			bwFile.write(",\"BILLDATE\"");
			bwFile.newLine();
			
			//One additional date in the ACCPAC detail record:
			//BILLDATE //ACCPAC 'Billing date'
			
			//Payment schedule record definition:
			bwFile.write("\"RECTYPE\"");
			bwFile.write(",\"CNTBTCH\"");
			bwFile.write(",\"CNTITEM\"");
			bwFile.write(",\"CNTPAYM\"");
			bwFile.write(",\"DATEDUE\"");
			bwFile.write(",\"AMTDUE\"");
			bwFile.write(",\"DATEDISC\"");
			bwFile.write(",\"AMTDISC\"");
			bwFile.newLine();
			
		}catch (IOException e){
			//System.out.println("In " + this.toString() + ".writeExportFile - error writing export file definitions: " + e.getMessage());
			m_sErrorMessage = "Error writing export file definitions: " + e.getMessage();
			try {
				bwFile.close();
			} catch (IOException e1) {
				throw new Exception("Error [1474482471] closing export file: " + e1.getMessage());
			}
		}
		for (int i = 0; i < m_HeaderRecordArray.size(); i ++){
			if (!writeACCPACEntry(i, bwFile)){
				try{
					bwFile.close();
				} catch (IOException e){
					fFile.delete();
					throw new Exception("Error [1474482472] writing export file entry - " + e.getMessage());
				}
				fFile.delete();
				throw new Exception("Error [1474482475] unable to write export file entry.");
			}
		}
		try {
			bwFile.close();
		} catch (IOException e){
			//System.out.println("In " + this.toString() + ".writeExportFile - error closing export file: " + e.getMessage());
			throw new Exception("Error [1474482473] closing export file: " + e.getMessage());
		}
		return;
	}
	private void writeMAS200ExportFile(BufferedWriter bwFile, File fFile) throws Exception{
		//Header record definition:
		try{
			bwFile.write("\"ENTRY_NO\"");
			bwFile.write(",\"VENDOR_ID\"");
			bwFile.write(",\"INVOICE_NO\"");
			bwFile.write(",\"PO_NUMBER\"");
			bwFile.write(",\"INV_DESC\"");
			bwFile.write(",\"INV_DATE\""); // ACCPAC 'Invoice date'
			bwFile.write(",\"TERMS\"");
			bwFile.write(",\"DUE_DATE\"");
			bwFile.write(",\"DISC_DATE\"");
			bwFile.write(",\"DISC_PCT\"");
			bwFile.write(",\"AVAILABLE_DISCOUNT_AMT\"");
			bwFile.write(",\"DUE_AMT\"");
			bwFile.write(",\"GROSS_TOTAL\"");
			bwFile.write(",\"INV_ENTRY_DATE\""); // This is the 'date entered' on the IC PO Invoice entry screen
			
			//Details:
			bwFile.write(",\"LINE_NO\"");
			bwFile.write(",\"LINE_DESC\"");
			bwFile.write(",\"GL_DIST_ACCT\"");
			bwFile.write(",\"DIST_AMT\"");
			bwFile.newLine();
			
		}catch (IOException e){
			//System.out.println("In " + this.toString() + ".writeExportFile - error writing export file definitions: " + e.getMessage());
			m_sErrorMessage = "Error writing export file definitions: " + e.getMessage();
			try {
				bwFile.close();
			} catch (IOException e1) {
				throw new Exception("Error [1474482471] closing export file: " + e1.getMessage());
			}
		}
		for (int i = 0; i < m_HeaderRecordArray.size(); i ++){
			if (!writeMAS200Entry(i, bwFile)){
				try{
					bwFile.close();
				} catch (IOException e){
					fFile.delete();
					throw new Exception("Error [1474482472] writing export file entry - " + e.getMessage());
				}
				fFile.delete();
				throw new Exception("Error [1474482475] unable to write export file entry.");
			}
		}
		try {
			bwFile.close();
		} catch (IOException e){
			//System.out.println("In " + this.toString() + ".writeExportFile - error closing export file: " + e.getMessage());
			throw new Exception("Error [1474482473] closing export file: " + e.getMessage());
		}
		return;
	}
	private void putFTPExport(String sFilePath, String sFileName, String sModuleType, Connection conn) throws Exception{
		SMOption opt = new SMOption();
		
		//System.out.println("[1381426951] Into putFTPExport - sFilePath: '" + sFilePath + "', sFileName: '" + sFileName + "', sModuleType: '" + sModuleType + "'.");
		if (!opt.load(conn)){
			removeExportFile(sFilePath + sFileName);
			throw new Exception("Error loading FTP settings for export - " + opt.getErrorMessage());
		}
		//System.out.println("[1381426952] loaded opt.");
		
		//If there is no FTP URL, then assume we don't need to upload to FTP:
		if (opt.getftpexporturl().compareToIgnoreCase("") == 0){
			return;
		}
		//Otherwise, try to upload the file to the FTP server:
		String sTargetFileName = opt.getftpfileexportpath() + sFileName;
		//System.out.println("[1381426953] sTargetFileName: '" + sTargetFileName + "'.");
		if (sTargetFileName.compareToIgnoreCase("") == 0){
			removeExportFile(sFilePath + sFileName);
			throw new Exception("Target file name cannot be blank.");
		}
		//System.out.println("[1381426954] going to putFile...");
		
		//System.out.println("[1381426955]\n"
		//		+ "opt.getftpexporturl() = '" + opt.getftpexporturl() + "'\n"
		//		+ "opt.getftpexportuser() = '" + opt.getftpexportuser() + "'\n"
		//		+ "opt.getftpexportpw() = '" + opt.getftpexportpw() + "'\n"
		//		+ "sFilePath = '" + sFilePath + "'\n"
		//		+ "sFileName = '" + sFileName + "'\n"
		//		+ "sTargetFileName = '" + sTargetFileName + "'\n"
		//);
		
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
		//System.out.println("[1381426956] AFTER putFile...");
		removeExportFile(sFilePath + sFileName);
	}
	private void removeExportFile(String sFullFileName) throws Exception{
		File fil = new File(sFullFileName);
		if (!fil.exists()) {
			//If the file doesn't exist, then just return
			return;
		}
		if (!fil.delete()){
			throw new Exception("Unable to delete export file '" + sFullFileName + "'.");
		}
	}
	private boolean writeACCPACEntry(int iIndex, BufferedWriter bwExportFile){

		//Write the header line:
		try{
			bwExportFile.write(
				"\""
				+ Integer.toString(m_HeaderRecordArray.get(iIndex).getRecordType()) 
				+ "\",\""
				+ Long.toString(m_HeaderRecordArray.get(iIndex).getBatchNumber())
				+ "\",\""
				+ Long.toString(m_HeaderRecordArray.get(iIndex).getItemCounter())
				+ "\",\""
				+ m_HeaderRecordArray.get(iIndex).getVendor()
				+ "\",\""
				+ m_HeaderRecordArray.get(iIndex).getdocNumber()
				+ "\",\""
				+ Integer.toString(m_HeaderRecordArray.get(iIndex).getTransactionType())
				+ "\",\""
				+ Integer.toString(m_HeaderRecordArray.get(iIndex).getDocumentType())
				+ "\",\""
				+ m_HeaderRecordArray.get(iIndex).getPONumber()
				+ "\",\""
				+ m_HeaderRecordArray.get(iIndex).getInvoiceDescription()
				+ "\",\""
				+ m_HeaderRecordArray.get(iIndex).getsInvoiceDate("yyyyMMdd")
				+ "\",\""
				+ m_HeaderRecordArray.get(iIndex).getTermsCode()
				+ "\",\""
				+ m_HeaderRecordArray.get(iIndex).getsDueDate("yyyyMMdd")
				+ "\",\""
				+ m_HeaderRecordArray.get(iIndex).getsDiscountDate("yyyyMMdd")
				+ "\",\""
				+ m_HeaderRecordArray.get(iIndex).getsDiscountPercentage("##########0.00")
				+ "\",\""
				+ m_HeaderRecordArray.get(iIndex).getsDiscountAmtAvailable("##########0.00")
				+ "\",\""
				+ m_HeaderRecordArray.get(iIndex).getsAmountDue("##########0.00")
				+ "\",\""
				+ m_HeaderRecordArray.get(iIndex).getsAmountTotal("##########0.00")
				+ "\",\""
				+ m_HeaderRecordArray.get(iIndex).getsPostingDate("yyyyMMdd")
				+ "\""
		);
		
		bwExportFile.newLine();
		}catch(IOException e){
			//System.out.println("In " + this.toString() + ".writeEntry - error on entry " + iIndex + ": " + e.getMessage());
			m_sErrorMessage = "Could not export entry " + iIndex + ": " + e.getMessage();
			return false;
		}
		//Write the detail lines
		for (int i = 0; i < m_HeaderRecordArray.get(iIndex).getDetailArray().size(); i++){
			try {
				bwExportFile.write(
					"\""
					+ Integer.toString(m_HeaderRecordArray.get(iIndex).getDetailArray().get(i).getRecordType()) 
					+ "\",\""
					+ Long.toString(m_HeaderRecordArray.get(iIndex).getDetailArray().get(i).getBatchNumber())
					+ "\",\""
					+ Long.toString(m_HeaderRecordArray.get(iIndex).getDetailArray().get(i).getItemCounter())
					+ "\",\""
					+ Long.toString(m_HeaderRecordArray.get(iIndex).getDetailArray().get(i).getTransactionNumber())
					+ "\",\""
					+ m_HeaderRecordArray.get(iIndex).getDetailArray().get(i).getDescription()
					+ "\",\""
					+ m_HeaderRecordArray.get(iIndex).getDetailArray().get(i).getGLAcct()
					+ "\",\""
					+ m_HeaderRecordArray.get(iIndex).getDetailArray().get(i).getsDistributionAmount("##########0.00")
					+ "\",\""
					+ m_HeaderRecordArray.get(iIndex).getDetailArray().get(i).getsBillingDate("yyyyMMdd")
					+ "\""
				);
				bwExportFile.newLine();
			}catch(IOException e){
				//System.out.println("In " + this.toString() + ".writeEntry - error on entry " + iIndex + ", line " + i + ": " + e.getMessage());
				m_sErrorMessage = "Could not export line on entry " + iIndex + ", line " + i + ": " + e.getMessage();
				return false;
			}

		}
		//Write the (single) payment schedule line:
		try {
			bwExportFile.write(
				"\""
				+ Integer.toString(m_HeaderRecordArray.get(iIndex).getPaymentArray().get(0).getRecordType()) 
				+ "\",\""
				+ Long.toString(m_HeaderRecordArray.get(iIndex).getPaymentArray().get(0).getBatchNumber())
				+ "\",\""
				+ Long.toString(m_HeaderRecordArray.get(iIndex).getPaymentArray().get(0).getItemCounter())
				+ "\",\""
				+ Long.toString(m_HeaderRecordArray.get(iIndex).getPaymentArray().get(0).getTransactionNumber())
				+ "\",\""
				+ m_HeaderRecordArray.get(iIndex).getPaymentArray().get(0).getsPaymentDateDue("yyyyMMdd")
				+ "\",\""
				+ m_HeaderRecordArray.get(iIndex).getPaymentArray().get(0).getsPaymentAmountDue("##########0.00")
				+ "\",\""
				+ m_HeaderRecordArray.get(iIndex).getPaymentArray().get(0).getsPaymentDiscountDate("yyyyMMdd")
				+ "\",\""
				+ m_HeaderRecordArray.get(iIndex).getPaymentArray().get(0).getsDiscountAmount("##########0.00")
				+ "\""
			);
			bwExportFile.newLine();
		} catch (IOException e) {
			//System.out.println("In " + this.toString() + ".writeEntry - error on entry " + iIndex + 
			//		", payment schedule line: " + e.getMessage());
			m_sErrorMessage = "Could not export payment schedule line on entry " + iIndex + ": " + e.getMessage();
			return false;
		}
		return true;
	}
	private boolean writeMAS200Entry(int iIndex, BufferedWriter bwExportFile){

		//Write the lines
		for (int i = 0; i < m_HeaderRecordArray.get(iIndex).getDetailArray().size(); i++){
			try {
				bwExportFile.write(
					"\""
					+ Long.toString(m_HeaderRecordArray.get(iIndex).getItemCounter())
					+ "\",\""
					+ m_HeaderRecordArray.get(iIndex).getVendor()
					+ "\",\""
					+ m_HeaderRecordArray.get(iIndex).getdocNumber()
					+ "\",\""
					+ m_HeaderRecordArray.get(iIndex).getPONumber()
					+ "\",\""
					+ m_HeaderRecordArray.get(iIndex).getInvoiceDescription()
					+ "\",\""
					+ m_HeaderRecordArray.get(iIndex).getsInvoiceDate("yyyyMMdd")
					+ "\",\""
					+ m_HeaderRecordArray.get(iIndex).getTermsCode()
					+ "\",\""
					+ m_HeaderRecordArray.get(iIndex).getsDueDate("yyyyMMdd")
					+ "\",\""
					+ m_HeaderRecordArray.get(iIndex).getsDiscountDate("yyyyMMdd")
					+ "\",\""
					+ m_HeaderRecordArray.get(iIndex).getsDiscountPercentage("##########0.00")
					+ "\",\""
					+ m_HeaderRecordArray.get(iIndex).getsDiscountAmtAvailable("##########0.00")
					+ "\",\""
					+ m_HeaderRecordArray.get(iIndex).getsAmountDue("##########0.00")
					+ "\",\""
					+ m_HeaderRecordArray.get(iIndex).getsAmountTotal("##########0.00")
					+ "\",\""
					+ m_HeaderRecordArray.get(iIndex).getsPostingDate("yyyyMMdd")
					+ "\",\""

					//Details:
					+ Integer.toString(i + 1)  //Line number
					+ "\",\""
					+ m_HeaderRecordArray.get(iIndex).getDetailArray().get(i).getDescription()  //Line description
					+ "\",\""
					+ m_HeaderRecordArray.get(iIndex).getDetailArray().get(i).getGLAcct()       //Distribution acct
					+ "\",\""
					+ m_HeaderRecordArray.get(iIndex).getDetailArray().get(i).getsDistributionAmount("##########0.00")  //Distribution amt
					+ "\""
				);
				bwExportFile.newLine();
			}catch(IOException e){
				//System.out.println("In " + this.toString() + ".writeEntry - error on entry " + iIndex + ", line " + i + ": " + e.getMessage());
				m_sErrorMessage = "Could not export line on entry " + iIndex + ", line " + i + ": " + e.getMessage();
				return false;
			}
		}

		return true;
	}
	//For re-creating the export:
	public void writeACCPACExportDownload(PrintWriter pw) throws Exception{
	    
		//Header record definition:
		pw.println(
			"\"RECTYPE\""
			+ ",\"CNTBTCH\""
			+ ",\"CNTITEM\""
			+ ",\"IDVEND\""
			+ ",\"IDINVC\""
			+ ",\"TEXTTRX\""
			+ ",\"IDTRX\""
			+ ",\"PONBR\""
			+ ",\"INVCDESC\""
			+ ",\"DATEINVC\""
			+ ",\"TERMCODE\""
			+ ",\"DATEDUE\""
			+ ",\"DATEDISC\""
			+ ",\"PCTDISC\""
			+ ",\"AMTDISCAVL\""
			+ ",\"AMTDUE\""
			+ ",\"AMTGROSTOT\""
			+ ",\"DATEBUS\""
		);
    
		//Detail record definition:
		pw.println(
				"\"RECTYPE\""
				+ ",\"CNTBTCH\""
				+ ",\"CNTITEM\""
				+ ",\"CNTLINE\""
				+ ",\"TEXTDESC\""
				+ ",\"IDGLACCT\""
				+ ",\"AMTDIST\""
				+ ",\"BILLDATE\""
		);
		
		//Payment schedule record definition:
		pw.println(
				"\"RECTYPE\""
				+ ",\"CNTBTCH\""
				+ ",\"CNTITEM\""
				+ ",\"CNTPAYM\""
				+ ",\"DATEDUE\""
				+ ",\"AMTDUE\""
				+ ",\"DATEDISC\""
				+ ",\"AMTDISC\""
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
	public void writeMAS200ExportDownload(PrintWriter pw) throws Exception{

		//Record definition:
		pw.println(
			"\"ENTRY_NO\""
			+ ",\"VENDOR_ID\""
			+ ",\"INVOICE_NO\""
			+ ",\"PO_NUMBER\""
			+ ",\"INV_DESC\""
			+ ",\"INV_DATE\""
			+ ",\"TERMS\""
			+ ",\"DUE_DATE\""
			+ ",\"DISC_DATE\""
			+ ",\"DISC_PCT\""
			+ ",\"AVAILABLE_DISCOUNT_AMT\""
			+ ",\"DUE_AMT\""
			+ ",\"GROSS_TOTAL\""
			+ ",\"INV_ENTRY_DATE\""
			+ ",\"LINE_NO\""
			+ ",\"LINE_DESC\""
			+ ",\"GL_DIST_ACCT\""
			+ ",\"DIST_AMT\""
		);
		
		for (int i = 0; i < m_HeaderRecordArray.size(); i ++){
			try {
				writeMAS200DownloadEntry(i, pw);
			} catch (Exception e){
				throw new Exception("Error closing export file after writing entry - " + e.getMessage() + ".");
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
			+ Long.toString(m_HeaderRecordArray.get(iIndex).getBatchNumber())
			+ "\",\""
			+ Long.toString(m_HeaderRecordArray.get(iIndex).getItemCounter())
			+ "\",\""
			+ m_HeaderRecordArray.get(iIndex).getVendor()
			+ "\",\""
			+ m_HeaderRecordArray.get(iIndex).getdocNumber()
			+ "\",\""
			+ Integer.toString(m_HeaderRecordArray.get(iIndex).getTransactionType())
			+ "\",\""
			+ Integer.toString(m_HeaderRecordArray.get(iIndex).getDocumentType())
			+ "\",\""
			+ m_HeaderRecordArray.get(iIndex).getPONumber()
			+ "\",\""
			+ m_HeaderRecordArray.get(iIndex).getInvoiceDescription()
			+ "\",\""
			+ m_HeaderRecordArray.get(iIndex).getsInvoiceDate("yyyyMMdd")
			+ "\",\""
			+ m_HeaderRecordArray.get(iIndex).getTermsCode()
			+ "\",\""
			+ m_HeaderRecordArray.get(iIndex).getsDueDate("yyyyMMdd")
			+ "\",\""
			+ m_HeaderRecordArray.get(iIndex).getsDiscountDate("yyyyMMdd")
			+ "\",\""
			+ m_HeaderRecordArray.get(iIndex).getsDiscountPercentage("##########0.00")
			+ "\",\""
			+ m_HeaderRecordArray.get(iIndex).getsDiscountAmtAvailable("##########0.00")
			+ "\",\""
			+ m_HeaderRecordArray.get(iIndex).getsAmountDue("##########0.00")
			+ "\",\""
			+ m_HeaderRecordArray.get(iIndex).getsAmountTotal("##########0.00")
			+ "\",\""
			+ m_HeaderRecordArray.get(iIndex).getsPostingDate("yyyyMMdd")
			+ "\""
		);

		//Write the detail lines
		for (int i = 0; i < m_HeaderRecordArray.get(iIndex).getDetailArray().size(); i++){
			out.println(
				"\""
				+ Integer.toString(m_HeaderRecordArray.get(iIndex).getDetailArray().get(i).getRecordType()) 
				+ "\",\""
				+ Long.toString(m_HeaderRecordArray.get(iIndex).getDetailArray().get(i).getBatchNumber())
				+ "\",\""
				+ Long.toString(m_HeaderRecordArray.get(iIndex).getDetailArray().get(i).getItemCounter())
				+ "\",\""
				+ Long.toString(m_HeaderRecordArray.get(iIndex).getDetailArray().get(i).getTransactionNumber())
				+ "\",\""
				+ m_HeaderRecordArray.get(iIndex).getDetailArray().get(i).getDescription()
				+ "\",\""
				+ m_HeaderRecordArray.get(iIndex).getDetailArray().get(i).getGLAcct()
				+ "\",\""
				+ m_HeaderRecordArray.get(iIndex).getDetailArray().get(i).getsDistributionAmount("##########0.00")
				+ "\",\""
				+ m_HeaderRecordArray.get(iIndex).getDetailArray().get(i).getsBillingDate("yyyyMMdd")
				+ "\""
			);
		}
		//Write the (single) payment schedule line:
		out.println(
			"\""
			+ Integer.toString(m_HeaderRecordArray.get(iIndex).getPaymentArray().get(0).getRecordType()) 
			+ "\",\""
			+ Long.toString(m_HeaderRecordArray.get(iIndex).getPaymentArray().get(0).getBatchNumber())
			+ "\",\""
			+ Long.toString(m_HeaderRecordArray.get(iIndex).getPaymentArray().get(0).getItemCounter())
			+ "\",\""
			+ Long.toString(m_HeaderRecordArray.get(iIndex).getPaymentArray().get(0).getTransactionNumber())
			+ "\",\""
			+ m_HeaderRecordArray.get(iIndex).getPaymentArray().get(0).getsPaymentDateDue("yyyyMMdd")
			+ "\",\""
			+ m_HeaderRecordArray.get(iIndex).getPaymentArray().get(0).getsPaymentAmountDue("##########0.00")
			+ "\",\""
			+ m_HeaderRecordArray.get(iIndex).getPaymentArray().get(0).getsPaymentDiscountDate("yyyyMMdd")
			+ "\",\""
			+ m_HeaderRecordArray.get(iIndex).getPaymentArray().get(0).getsDiscountAmount("##########0.00")
			+ "\""
		);
	}
	private void writeMAS200DownloadEntry(int iIndex, PrintWriter out) throws Exception{
		for (int i = 0; i < m_HeaderRecordArray.get(iIndex).getDetailArray().size(); i++){
			//if ((iIndex == 0) && (i == 0)){
			//	System.out.println("[1474555701] Index " + iIndex + ", line " + i + ", line desc: '" + m_HeaderRecordArray.get(iIndex).getDetailArray().get(i).getDescription() + "'");
			//}
			out.println(
				"\""
				+ Long.toString(m_HeaderRecordArray.get(iIndex).getItemCounter())
				+ "\",\""
				+ m_HeaderRecordArray.get(iIndex).getVendor()
				+ "\",\""
				+ m_HeaderRecordArray.get(iIndex).getdocNumber()
				+ "\",\""
				+ m_HeaderRecordArray.get(iIndex).getPONumber()
				+ "\",\""
				+ m_HeaderRecordArray.get(iIndex).getInvoiceDescription()
				+ "\",\""
				+ m_HeaderRecordArray.get(iIndex).getsInvoiceDate("yyyyMMdd")
				+ "\",\""
				+ m_HeaderRecordArray.get(iIndex).getTermsCode()
				+ "\",\""
				+ m_HeaderRecordArray.get(iIndex).getsDueDate("yyyyMMdd")
				+ "\",\""
				+ m_HeaderRecordArray.get(iIndex).getsDiscountDate("yyyyMMdd")
				+ "\",\""
				+ m_HeaderRecordArray.get(iIndex).getsDiscountPercentage("##########0.00")
				+ "\",\""
				+ m_HeaderRecordArray.get(iIndex).getsDiscountAmtAvailable("##########0.00")
				+ "\",\""
				+ m_HeaderRecordArray.get(iIndex).getsAmountDue("##########0.00")
				+ "\",\""
				+ m_HeaderRecordArray.get(iIndex).getsAmountTotal("##########0.00")
				+ "\",\""
				+ m_HeaderRecordArray.get(iIndex).getsPostingDate("yyyyMMdd")
				+ "\",\""

				//Details:
				+ Integer.toString(i + 1)  //Line number
				+ "\",\""
				+ m_HeaderRecordArray.get(iIndex).getDetailArray().get(i).getDescription()  //Line description
				+ "\",\""
				+ m_HeaderRecordArray.get(iIndex).getDetailArray().get(i).getGLAcct()       //Distribution acct
				+ "\",\""
				+ m_HeaderRecordArray.get(iIndex).getDetailArray().get(i).getsDistributionAmount("##########0.00")  //Distribution amt
				+ "\""
			);
		}
	}
	public void loadExport(Connection conn, String sExportSequence) throws Exception{
		//Next, read each invoice to create an export record for it:
		String SQL = "SELECT"
			+ " " + SMTableicpoinvoiceheaders.lid
			+ " FROM " + SMTableicpoinvoiceheaders.TableName
			+ " WHERE ("
				+ "(" + SMTableicpoinvoiceheaders.lexportsequencenumber + " = " + sExportSequence + ")"
			+ ")"
			+ " ORDER BY " + SMTableicpoinvoiceheaders.lid
		;
        try {
        	ResultSet rsInvoices = clsDatabaseFunctions.openResultSet(SQL, conn);
        	while (rsInvoices.next()){
            	ICPOInvoice inv = new ICPOInvoice();
            	inv.setM_slid(Long.toString(rsInvoices.getLong(SMTableicpoinvoiceheaders.lid)));
            	if (!inv.load(conn)){
        			rsInvoices.close();
        			throw new Exception("Could not load invoice with ID: " + inv.getM_slid() + " for export");
        		}
        		
            	//Calculate the discount percentage:
            	BigDecimal bdDiscountAmount = new BigDecimal(inv.getM_sdiscount().replace(",", ""));
            	BigDecimal bdBaseForDiscount = new BigDecimal(inv.getM_sinvoicetotal().replace(",", ""));
            	BigDecimal bdDiscountPercentage = new BigDecimal(0);
            	if (bdBaseForDiscount.compareTo(BigDecimal.ZERO) != 0){
            		bdDiscountPercentage = bdDiscountAmount.divide(
            			bdBaseForDiscount, 2, BigDecimal.ROUND_HALF_UP).multiply(new BigDecimal(100));
            	}
            	
            	//Calculate the amount due:
            	BigDecimal bdAmountDue = new BigDecimal(0);
            	bdAmountDue = bdBaseForDiscount.subtract(bdDiscountAmount);

            	//Get the first PO number from the po invoice lines:
            	String sReceiptID = "";
            	String sPOString = "";
            	boolean bInvoiceIncludesMultipleReceipts = false;
            	for (int i = 0; i < inv.getLines().size(); i++){
            		//Don't bother with any lines that aren't from receipts:
            		if (inv.getLines().get(i).getsporeceiptlineid().compareToIgnoreCase("-1") !=0){
            			//If we've already gotten a receipt ID:
            			if (sReceiptID.compareToIgnoreCase("") != 0){
            				//Then is this line has a DIFFERENT receipt line ID:
            				if (sReceiptID.compareToIgnoreCase(inv.getLines().get(i).getsporeceiptid()) != 0){
            					//This invoice must include multiple receipts:
            					bInvoiceIncludesMultipleReceipts = true;
            					//Since we've already gotten a receipt line ID, we can jump out:
            					break;
            				}
            			}else{
            				//But if we haven't yet gotten a receiptline ID, store this one:
            				sReceiptID = inv.getLines().get(i).getsporeceiptid();
            			}
            		}
            	}
            	
            	//If we DID get a receipt ID, then we need to look up the PO:
            	if (sReceiptID.compareToIgnoreCase("") !=0){
            		ICPOReceiptHeader rcpt = new ICPOReceiptHeader();
            		rcpt.setsID(sReceiptID);
            		if (rcpt.load(conn)){
            			sPOString = rcpt.getspoheaderid();
            		}
            	}
            	
            	if (bInvoiceIncludesMultipleReceipts){
            		sPOString += "...";
            	}
            	
        		//The discount date may be blank - (00/00/0000) - in that case, we have to pass in something different:
        		java.sql.Date datDiscountDate = null;
        		try {
					datDiscountDate = clsDateAndTimeConversions.StringTojavaSQLDate("MM/dd/yyyy", inv.getM_sdatdiscount());
				} catch (Exception e) {
					//If the date is blank, then we'll just set it to the invoice date for the export:
					try {
						datDiscountDate = clsDateAndTimeConversions.StringTojavaSQLDate("MM/dd/yyyy", inv.getM_sdatinvoice());
					} catch (Exception e1) {
	            		m_sErrorMessage = 
	            				"Error [1478570767] Could not parse invoice date '"
	            				+ inv.getM_sdatinvoice() + "' for invoice #: " + inv.getM_sinvoicenumber() + " - " + e.getMessage();
	            			rsInvoices.close();
	            			throw new Exception("Error [1478570768] Could not parse invoice date '"
	            				+ inv.getM_sdatinvoice() + "' for invoice #: " + inv.getM_sinvoicenumber() + " - " + e.getMessage());
					}
				}
            	try {
					addHeader(
						inv.getM_svendor(),
						inv.getM_sinvoicenumber(),
						sPOString, 
						SMTableicpoinvoiceheaders.POINVOICE_SIGNATURE + " " + inv.getM_sdescription(),
						clsDateAndTimeConversions.StringTojavaSQLDate("MM/dd/yyyy", inv.getM_sdatinvoice()),
						inv.getM_sterms(),
						clsDateAndTimeConversions.StringTojavaSQLDate("MM/dd/yyyy", inv.getM_sdatdue()),
						datDiscountDate,
						bdDiscountPercentage,
						bdDiscountAmount,
						bdAmountDue,
						new BigDecimal(inv.getM_sinvoicetotal().replace(",", "")),  //total amt
						clsDateAndTimeConversions.StringTojavaSQLDate("MM/dd/yyyy", inv.getM_sdatentered())
					);
				} catch (ParseException e) {
            		m_sErrorMessage = 
        				"Error [1478540765] Could not load convert date to export header for invoice #: " + inv.getM_sinvoicenumber() + " - " + e.getMessage();
        			rsInvoices.close();
        			throw new Exception("Error [1478540766] Could not load convert date to export header for invoice #: " 
        					+ inv.getM_sinvoicenumber() + " - " + e.getMessage());
				}
        		        		
        		for (int i = 0; i < inv.getLines().size(); i ++){
        			//Now add each line from the invoice as an AP Invoice detail:
        			ICPOInvoiceLine line = inv.getLines().get(i);
        			//if ((i == 0) && (iRecordCounter == 0)){
        			//	System.out.println("[1474555293] line.getsitemdescription() - i = " + i + ", " + line.getsitemdescription());
        			//}
            		addDetail(
        				line.getsitemdescription(),
    					line.getsexpenseaccount(),
    					new BigDecimal(line.getsinvoicedcost().replace(",", "")),
    					clsDateAndTimeConversions.StringTojavaSQLDate("MM/dd/yyyy", inv.getM_sdatinvoice())
   					);
        		}

        		//Finally, add a payment schedule line to the export:
        		try {
					addPaymentSchedule(
						clsDateAndTimeConversions.StringTojavaSQLDate("MM/dd/yyyy", inv.getM_sdatdue()), 
						new BigDecimal(inv.getM_sinvoicetotal().replace(",", "")), 
						datDiscountDate,
						new BigDecimal(inv.getM_sdiscount().replace(",", ""))
					);
				} catch (ParseException e) {
            		m_sErrorMessage = 
        				"Error [1478569395] - Could not load convert date to export payment schedule - " + e.getMessage();
        			rsInvoices.close();
        			throw new Exception("Error [1478569394] - Could not load convert date to export payment schedule - " + e.getMessage());
				}
        	}
        	rsInvoices.close();
        } catch (SQLException e){
        	throw new Exception("SQL Error opening batch for export: " + e.getMessage());
        }
	}
	public String getExportFilePath(){
		return m_sExportFilePath;
	}
	public String getExportFileName(String sSourceLedger, String sBatchTypeLabel, String sExportSequence, String sDatabaseName){
		//Create the file name:
		String sFileName = "AP-" + sSourceLedger;
	    
	    if (sDatabaseName.trim().compareToIgnoreCase("") != 0){
	    	sFileName += "-" + sDatabaseName;
	    }
	    sFileName += "-" + sBatchTypeLabel
	    	+ "-" + sExportSequence
	    	+ "-" + clsDateAndTimeConversions.now("yyyy-MM-dd")
	    	+ " " + clsDateAndTimeConversions.now("hh_mm_ss a")
	    	+ ".CSV";
	    return sFileName;
	}
	
	public String getErrorMessage (){
		return m_sErrorMessage;
	}
}
