package smar;

import java.io.PrintWriter;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import smgl.GLAccount;
import SMClasses.SMBatchStatuses;
import SMClasses.SMBatchTypes;
import SMClasses.SMEntryBatch;
import SMClasses.SMLogEntry;
import SMClasses.SMModuleTypes;
import SMDataDefinition.SMMasterStyleSheetDefinitions;
import SMDataDefinition.SMTablearcustomer;
import SMDataDefinition.SMTableentries;
import SMDataDefinition.SMTableentrylines;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsDateAndTimeConversions;
import ServletUtilities.clsManageBigDecimals;
import ServletUtilities.clsServletUtilities;

public class ARPostingJournal extends java.lang.Object{

	private String m_sStartingBatchNumber;
	private String m_sEndingBatchNumber;
	private boolean m_bIncludeInvoiceBatches;
	private boolean m_bIncludeCashBatches;
	private String m_sUserID;
	private String m_sCustomerNumber;
	private String m_sDocumentNumber;
	private String m_sErrorMessage;
	private String m_sCurrentBatchAndEntry;
	private String m_sCustomerName;
	private int m_iTransactionType;
	private String m_sTransactionDate;
	private String m_sBatchNumber;
	private String m_sEntryNumber;
	private String m_sEntryDesc;
	private String m_sEntryAmount;
	private String m_sLineApplyToDocNumber;
	private String m_sLineDistributionAcct;
	private String m_sEntryDebitAmount;
	private String m_sEntryCreditAmount;
	private String m_sLineDebitAmount;
	private String m_sLineCreditAmount;
	private String m_sControlAcct;
	private BigDecimal m_bdDebitTotal;
	private BigDecimal m_bdCreditTotal;
	private String m_sControlAcctDesc;
	private String m_sDistAcctDesc;
	private String m_sPostingDate;
	private long m_lTransactionCount;
	private BigDecimal m_bdReportDebitTotal;
	private BigDecimal m_bdReportCreditTotal;
	private ArrayList<String> m_arrGLAccountArray;
	private ArrayList<BigDecimal> m_arrGLDebitTotalArray;
	private ArrayList<BigDecimal> m_arrGLCreditTotalArray;
	private ResultSet m_rs;
	private BigDecimal m_bdInvoiceTotal;
	private BigDecimal m_bdReceiptTotal;
	private BigDecimal m_bdAdjustmentTotal;
	private static int AR_INVOICE = 0;
	private static int AR_CASH = 1;
	private static int AR_ADJUSTMENT = 0;
	
	ARPostingJournal(
    		String sStartingBatchNumber,
    		String sEndingBatchNumber,
    		boolean bIncludeInvoiceBatches,
    		boolean bIncludeCashBatches,
    		String sUserID,
    		String sUserFullName
        ) {
		
		m_sStartingBatchNumber = sStartingBatchNumber;
		m_sEndingBatchNumber = sEndingBatchNumber;
		m_bIncludeInvoiceBatches = bIncludeInvoiceBatches;
		m_bIncludeCashBatches = bIncludeCashBatches;
		m_sUserID = sUserID;
		m_sErrorMessage = "";
		m_sCurrentBatchAndEntry = "";
		m_sCustomerNumber = "";
		m_sDocumentNumber = "";
		m_sCustomerName = "";
		m_iTransactionType = 0;
		m_sTransactionDate = "";
		m_sBatchNumber = "";
		m_sEntryNumber = "";
		m_sEntryDesc = "";
		m_sEntryAmount = "";
		m_sLineApplyToDocNumber = "";
		m_sLineDistributionAcct = "";
		m_sEntryDebitAmount = "";
		m_sEntryCreditAmount = "";
		m_sLineDebitAmount = "";
		m_sLineCreditAmount = "";
		m_sControlAcct = "";
		m_sControlAcctDesc = "";
		m_sDistAcctDesc = "";
		m_bdDebitTotal = new BigDecimal(0);
		m_bdCreditTotal = new BigDecimal(0);
		m_sPostingDate = "";
		m_lTransactionCount = 0;
		m_bdReportDebitTotal = new BigDecimal(0);
		m_bdReportCreditTotal = new BigDecimal(0);
		m_rs = null;
		m_arrGLAccountArray = new ArrayList<String>(0);
		m_arrGLDebitTotalArray = new ArrayList<BigDecimal>(0);
		m_arrGLCreditTotalArray = new ArrayList<BigDecimal>(0);
		m_bdInvoiceTotal = new BigDecimal(0);
		m_bdReceiptTotal = new BigDecimal(0);
		m_bdAdjustmentTotal = new BigDecimal(0);
	}
	public boolean processReport(Connection conn, PrintWriter out){
		
		//First send out the header:
		
		//System.out.println("In ARPostingJournal 01");
		if (!loopReport(conn, out)){
			try{
				m_rs.close();
			}catch (SQLException e){
				System.out.println("In " + this.toString() + " - error closing resultset: " + e.getMessage());
			}
			return false;
		}
		try{
			m_rs.close();
		}catch (SQLException e){
			System.out.println("In " + this.toString() + " - error closing resultset: " + e.getMessage());
		}
		
	    SMLogEntry log = new SMLogEntry(conn);
	    log.writeEntry(m_sUserID, SMLogEntry.LOG_OPERATION_ARPOSTINGJOURNAL, "REPORT", "AR Posting Journal", "[1376509286]");
		return true;
	}

	private boolean getReportRecords(Connection conn){
		
		String SQL = "SELECT *"
				
			//+ SMTableentries.TableName + "." + AREntry.spayeepayor
			//+ ", " + SMTableentries.TableName + "." + AREntry.sdocnumber
				
			+ " FROM " + SMTableentries.TableName 
			+ ", " + SMEntryBatch.TableName
			+ ", " + SMTableentrylines.TableName
			+ ", " + SMTablearcustomer.TableName
			+ " WHERE ("
				+ "(" + SMEntryBatch.ibatchstatus + " = " + Integer.toString(SMBatchStatuses.POSTED) + ")"

				//Starting batch number:
				+ " AND (" + SMEntryBatch.TableName + "." + SMEntryBatch.ibatchnumber
					+ " >=" + m_sStartingBatchNumber + ")"

				//Ending batch number:
				+ " AND (" + SMEntryBatch.TableName + "." + SMEntryBatch.ibatchnumber
					+ " <=" + m_sEndingBatchNumber + ")"
					
				//Batch and entry link:
				+ " AND (" + SMTableentries.TableName + "." + SMTableentries.ibatchnumber 
					+ " = " + SMEntryBatch.TableName + "." + SMEntryBatch.ibatchnumber + ")"

				//Batch and line link:
				+ " AND (" + SMTableentrylines.TableName + "." + SMTableentrylines.ibatchnumber 
					+ " = " + SMEntryBatch.TableName + "." + SMEntryBatch.ibatchnumber + ")"
					
				//Entry and line link:
				+ " AND (" + SMTableentrylines.TableName + "." + SMTableentrylines.ientrynumber 
					+ " = " + SMTableentries.TableName + "." + SMTableentries.ientrynumber + ")"

				//Customer and entry link:
				+ " AND (" + SMTableentries.TableName + "." + SMTableentries.spayeepayor 
					+ " = " + SMTablearcustomer.TableName + "." + SMTablearcustomer.sCustomerNumber + ")"

				+ " AND (" + SMEntryBatch.TableName + "." + SMEntryBatch.smoduletype
					+ " = '" + SMModuleTypes.AR + "')";
		
				//Select by batch type:
				if (m_bIncludeInvoiceBatches == false){
					SQL = SQL + " AND (" + SMEntryBatch.TableName + "." + SMEntryBatch.ibatchtype
					+ " != " + SMBatchTypes.AR_INVOICE + ")";
				}
				
				if (m_bIncludeCashBatches == false){
					SQL = SQL + " AND (" + SMEntryBatch.TableName + "." + SMEntryBatch.ibatchtype
					+ " != " + SMBatchTypes.AR_CASH + ")";
				}
				
				SQL = SQL + ")"
			+ " ORDER BY " 
			+ SMTableentries.spayeepayor
			+ ", " + SMTableentries.sdocnumber
			+ ", " + SMTableentrylines.ilinenumber;
			//System.out.println("Get_AR_Posting_Journal_SQL = " + SQL);
		
		try {
			m_rs = clsDatabaseFunctions.openResultSet(SQL, conn);
			return true;
		}catch (SQLException e){
			m_sErrorMessage = "Could not get report resultset: " + e.getMessage();
			return false;
		}
	}
	private boolean loopReport(Connection conn, PrintWriter out){
		
		if(!getReportRecords(conn)){
			
			return false;
		}
		out.println("<TABLE WIDTH = 100% CLASS=\""+ SMMasterStyleSheetDefinitions.TABLE_BASIC_WITH_BORDER_COLLAPSE + "\">" );
		int iCount =1;
		try{
			while(m_rs.next()){
				iCount =processRecord(conn, out, iCount);
				if (iCount == -1 ){
					return false;
				}
			}
			
			//After the last record, have to print the entry detail and totals:
			printEntryDetail(out, iCount);
			printDocumentTotals(out);
			printReportTotal(out);
			if (!printGLSummary(conn, out)){
				return false;
			}
		}catch (SQLException e){
			m_sErrorMessage = "Error looping through resultset: " + e.getMessage();
			return false;
		}
		return true;
	}
	private int processRecord(Connection conn, 
			PrintWriter out, 
			int iCount){
		
		int iCounter = iCount;
		String sBatchAndEntry;
		try{
			//First store this record's batch and entry number:
			sBatchAndEntry = 
				Integer.toString(m_rs.getInt(SMTableentries.ibatchnumber))
				+ "-" + Integer.toString(m_rs.getInt(SMTableentries.ientrynumber));
			
			//If it's a new batch and entry AND if it's not the first entry printed:
			if (
					(!m_sCurrentBatchAndEntry.equalsIgnoreCase(sBatchAndEntry) && (!m_sCurrentBatchAndEntry.equalsIgnoreCase("")))
				){
				    iCounter++;
					//Have to print this with values from the last record, so we load the record AFTER this . . .
					printEntryDetail(out, iCounter);
					printDocumentTotals(out);
					//Reset the totals:
					m_bdDebitTotal = new BigDecimal(0);
					m_bdCreditTotal = new BigDecimal(0);
					iCounter=0;
			}else {
				iCounter++;
			}

			m_sCustomerNumber = m_rs.getString(SMTableentries.spayeepayor);
			m_sDocumentNumber = m_rs.getString(SMTableentries.sdocnumber);
			m_sCustomerName = m_rs.getString(SMTablearcustomer.sCustomerName);
			m_sPostingDate = clsDateAndTimeConversions.TimeStampToStdString(m_rs.getTimestamp(SMEntryBatch.datpostdate));
			m_iTransactionType = m_rs.getInt(SMTableentries.idocumenttype);
			m_sTransactionDate = clsDateAndTimeConversions.utilDateToString(m_rs.getDate(SMTableentries.datdocdate),"MM/dd/yyyy");
			m_sBatchNumber = Integer.toString(m_rs.getInt(SMTableentries.ibatchnumber));
			m_sEntryNumber = Integer.toString(m_rs.getInt(SMTableentries.ientrynumber));
			m_sEntryDesc = m_rs.getString(SMTableentries.TableName + "." + SMTableentries.sdocdescription);
			m_sEntryAmount = clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(m_rs.getBigDecimal(SMTableentries.TableName + "." + SMTableentries.doriginalamount));
			m_sControlAcct = m_rs.getString(SMTableentries.scontrolacct);
			
			GLAccount glacct = new GLAccount(m_sControlAcct);
			if (!glacct.load(conn)){
				m_sErrorMessage = "Error reading GL description in ARPostingJournal.processRecord for control acct '" + m_sControlAcct + "' " + glacct.getErrorMessageString();
				return -1;
			}
			m_sControlAcctDesc = glacct.getM_sdescription();
			m_sLineApplyToDocNumber = m_rs.getString(SMTableentrylines.sdocappliedto);
			m_sLineDistributionAcct = m_rs.getString(SMTableentrylines.sglacct);

			glacct = new GLAccount(m_sLineDistributionAcct);
			if (!glacct.load(conn)){
				m_sLineDistributionAcct = "** UNKNOWN **";
				m_sDistAcctDesc = "N/A";
			}else{
				m_sDistAcctDesc = glacct.getM_sdescription();
			}
			
			BigDecimal bd = BigDecimal.ZERO;

			//Accumulate the gl totals here:
			int iIndex;
			bd = m_rs.getBigDecimal(SMTableentrylines.damount);
			
			//Process an invoice batch entry (invoice or retainage:
			if (m_rs.getInt(SMEntryBatch.ibatchtype) == SMBatchTypes.AR_INVOICE){
				//If it's a retainage entry, the line amount is always a credit against the AR account:
				if (m_iTransactionType == ARDocumentTypes.RETAINAGE){
					if (bd.compareTo(BigDecimal.ZERO) == -1){
						m_sLineDebitAmount = clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(bd.abs());
						m_bdDebitTotal = m_bdCreditTotal.add(bd.abs());
						m_bdReportDebitTotal = m_bdReportDebitTotal.add(bd.abs());
						m_sLineCreditAmount = "0.00";
						
						//Add the distribution account:
						iIndex = m_arrGLAccountArray.indexOf(m_sLineDistributionAcct);
						if (iIndex == -1){
							m_arrGLAccountArray.add(m_sLineDistributionAcct);
							m_arrGLDebitTotalArray.add(bd.abs());
							m_arrGLCreditTotalArray.add(BigDecimal.ZERO);
						}else{
							m_arrGLDebitTotalArray.set(iIndex, m_arrGLDebitTotalArray.get(iIndex).add(bd.abs()));
						}
	
					}else{
						m_sLineDebitAmount = "0.00";
						m_bdReportCreditTotal = m_bdReportDebitTotal.add(bd.abs());
						m_bdCreditTotal = m_bdCreditTotal.add(bd.abs());
						m_sLineCreditAmount = clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(bd.abs());
						
						//Add the distribution account:
						iIndex = m_arrGLAccountArray.indexOf(m_sLineDistributionAcct);
						if (iIndex == -1){
							m_arrGLAccountArray.add(m_sLineDistributionAcct);
							m_arrGLCreditTotalArray.add(bd.abs());
							m_arrGLDebitTotalArray.add(BigDecimal.ZERO);
						}else{
							m_arrGLCreditTotalArray.set(iIndex, m_arrGLCreditTotalArray.get(iIndex).add(bd.abs()));
						}
					}
				}else{
					//If it's an invoice entry, a positive amount is a debit, a negative amt is a credit:
					if (bd.compareTo(BigDecimal.ZERO) == -1){
						m_sLineCreditAmount = clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(bd.abs());
						m_bdCreditTotal = m_bdCreditTotal.add(bd.abs());
						m_bdReportCreditTotal = m_bdReportCreditTotal.add(bd.abs());
						m_sLineDebitAmount = "0.00";
						
						//Add the distribution account:
						iIndex = m_arrGLAccountArray.indexOf(m_sLineDistributionAcct);
						if (iIndex == -1){
							m_arrGLAccountArray.add(m_sLineDistributionAcct);
							m_arrGLCreditTotalArray.add(bd.abs());
							m_arrGLDebitTotalArray.add(BigDecimal.ZERO);
						}else{
							m_arrGLCreditTotalArray.set(iIndex, m_arrGLCreditTotalArray.get(iIndex).add(bd.abs()));
						}

					}else{
						//If it's an invoice entry:
						m_sLineDebitAmount = clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(bd.abs());
						m_bdReportDebitTotal = m_bdReportDebitTotal.add(bd.abs());
						m_bdDebitTotal = m_bdDebitTotal.add(bd.abs());
						m_sLineCreditAmount = "0.00";
						
						//Add the distribution account:
						iIndex = m_arrGLAccountArray.indexOf(m_sLineDistributionAcct);
						if (iIndex == -1){
							m_arrGLAccountArray.add(m_sLineDistributionAcct);
							m_arrGLDebitTotalArray.add(bd.abs());
							m_arrGLCreditTotalArray.add(BigDecimal.ZERO);
						}else{
							m_arrGLDebitTotalArray.set(iIndex, m_arrGLDebitTotalArray.get(iIndex).add(bd.abs()));
						}
					}
					
				}
			}
			
			//If it's a cash entry, a positive amount is a credit, a negative amt is a debit:
			if (m_rs.getInt(SMEntryBatch.ibatchtype) == SMBatchTypes.AR_CASH){
				if (bd.compareTo(BigDecimal.ZERO) == 1){
					m_sLineCreditAmount = clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(bd.abs());
					m_bdCreditTotal = m_bdCreditTotal.add(bd.abs());
					m_bdReportCreditTotal = m_bdReportCreditTotal.add(bd.abs());
					m_sLineDebitAmount = "0.00";
					//Add the distribution account:
					iIndex = m_arrGLAccountArray.indexOf(m_sLineDistributionAcct);
					if (iIndex == -1){
						m_arrGLAccountArray.add(m_sLineDistributionAcct);
						m_arrGLCreditTotalArray.add(bd.abs());
						m_arrGLDebitTotalArray.add(BigDecimal.ZERO);
					}else{
						m_arrGLCreditTotalArray.set(iIndex, m_arrGLCreditTotalArray.get(iIndex).add(bd.abs()));
					}

				}else{
					m_sLineDebitAmount = clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(bd.abs());
					m_bdReportDebitTotal = m_bdReportDebitTotal.add(bd.abs());
					m_bdDebitTotal = m_bdDebitTotal.add(bd.abs());
					m_sLineCreditAmount = "0.00";
					//Add the distribution account:
					iIndex = m_arrGLAccountArray.indexOf(m_sLineDistributionAcct);
					if (iIndex == -1){
						m_arrGLAccountArray.add(m_sLineDistributionAcct);
						m_arrGLDebitTotalArray.add(bd.abs());
						m_arrGLCreditTotalArray.add(BigDecimal.ZERO);
					}else{
						m_arrGLDebitTotalArray.set(iIndex, m_arrGLDebitTotalArray.get(iIndex).add(bd.abs()));
					}
				}
			}

			//If it's the first line in the entry, print the document heading:
			if(!m_sCurrentBatchAndEntry.equalsIgnoreCase(sBatchAndEntry)){
				printDocumentHeading(out);
				
				//We do all of our 'per entry' math here:
				//Put the original amount into the debit or credit amount now:
				bd = m_rs.getBigDecimal(SMTableentries.doriginalamount);

				//If it's an invoice entry, a positive amount is a debit, a negative amt is a credit:
				if (m_rs.getInt(SMEntryBatch.ibatchtype) == SMBatchTypes.AR_INVOICE){
					if (bd.compareTo(BigDecimal.ZERO) == -1){
						m_sEntryCreditAmount = clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(bd.abs());
						m_bdCreditTotal = m_bdCreditTotal.add(bd.abs());
						m_bdReportCreditTotal = m_bdReportCreditTotal.add(bd.abs());
						m_sEntryDebitAmount = "0.00";
						iIndex = m_arrGLAccountArray.indexOf(m_sControlAcct);
						if (iIndex == -1){
							m_arrGLAccountArray.add(m_sControlAcct);
							m_arrGLCreditTotalArray.add(bd.abs());
							m_arrGLDebitTotalArray.add(BigDecimal.ZERO);
						}else{
							m_arrGLCreditTotalArray.set(iIndex, m_arrGLCreditTotalArray.get(iIndex).add(bd.abs()));
						}
					}else{
						m_sEntryDebitAmount = clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(bd.abs());
						m_bdReportDebitTotal = m_bdReportDebitTotal.add(bd.abs());
						m_bdDebitTotal = m_bdDebitTotal.add(bd.abs());
						m_sEntryCreditAmount = "0.00";
						iIndex = m_arrGLAccountArray.indexOf(m_sControlAcct);
						if (iIndex == -1){
							m_arrGLAccountArray.add(m_sControlAcct);
							m_arrGLDebitTotalArray.add(bd.abs());
							m_arrGLCreditTotalArray.add(BigDecimal.ZERO);
						}else{
							m_arrGLDebitTotalArray.set(iIndex, m_arrGLDebitTotalArray.get(iIndex).add(bd.abs()));
						}
					}
				}
				
				//If it's a cash entry, a positive amount is a credit, a negative amt is a debit:
				if (m_rs.getInt(SMEntryBatch.ibatchtype) == SMBatchTypes.AR_CASH){
					if (bd.compareTo(BigDecimal.ZERO) == 1){
						m_sEntryCreditAmount = clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(bd.abs());
						m_bdCreditTotal = m_bdCreditTotal.add(bd.abs());
						m_bdReportCreditTotal = m_bdReportCreditTotal.add(bd.abs());
						m_sEntryDebitAmount = "0.00";
						iIndex = m_arrGLAccountArray.indexOf(m_sControlAcct);
						if (iIndex == -1){
							m_arrGLAccountArray.add(m_sControlAcct);
							m_arrGLCreditTotalArray.add(bd.abs());
							m_arrGLDebitTotalArray.add(BigDecimal.ZERO);
						}else{
							m_arrGLCreditTotalArray.set(iIndex, m_arrGLCreditTotalArray.get(iIndex).add(bd.abs()));
						}
					}else{
						m_sEntryDebitAmount = clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(bd.abs());
						m_bdReportDebitTotal = m_bdReportDebitTotal.add(bd.abs());
						m_bdDebitTotal = m_bdDebitTotal.add(bd.abs());
						m_sEntryCreditAmount = "0.00";
						iIndex = m_arrGLAccountArray.indexOf(m_sControlAcct);
						if (iIndex == -1){
							m_arrGLAccountArray.add(m_sControlAcct);
							m_arrGLDebitTotalArray.add(bd.abs());
							m_arrGLCreditTotalArray.add(BigDecimal.ZERO);
						}else{
							m_arrGLDebitTotalArray.set(iIndex, m_arrGLDebitTotalArray.get(iIndex).add(bd.abs()));
						}
					}
				}
				
				m_lTransactionCount++;
				if (m_rs.getInt(SMEntryBatch.ibatchtype) == AR_INVOICE){
					m_bdInvoiceTotal = m_bdInvoiceTotal.add(m_rs.getBigDecimal(SMTableentries.TableName + "." + SMTableentries.doriginalamount));
				}
				if (m_rs.getInt(SMEntryBatch.ibatchtype) == AR_CASH){
					m_bdReceiptTotal = m_bdReceiptTotal.add(m_rs.getBigDecimal(SMTableentries.TableName + "." + SMTableentries.doriginalamount));
				}
				if (m_rs.getInt(SMEntryBatch.ibatchtype) == AR_ADJUSTMENT){
					m_bdAdjustmentTotal = m_bdAdjustmentTotal.add(m_rs.getBigDecimal(SMTableentries.TableName + "." + SMTableentries.doriginalamount));
				}
			}
			//Print the detail line on every loop:
			printLineDetail(out, iCounter);
			//Update the 'new entry indicator':
			m_sCurrentBatchAndEntry = sBatchAndEntry;
			return iCounter;
		}catch (SQLException e){
			System.out.println("In ARPostingJournal - SQL error in processRecord: " + e.getMessage());
			m_sErrorMessage = "Error processing record: " + e.getMessage();
			return -1;
		}
	}
	private void printDocumentHeading(PrintWriter out){

		out.println("<TR CLASS= \"" + SMMasterStyleSheetDefinitions.TABLE_HEADING + "\">");
		out.println("<TD CLASS= \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_ALIGN_TOP + "\"><B>Customer :</B>" + m_sCustomerNumber + "  " + m_sCustomerName + "</TD>");
		out.println("<TD CLASS= \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_ALIGN_TOP + "\"><B>Transaction type: </B>" + ARDocumentTypes.Get_Document_Type_Label(m_iTransactionType) + "</TD>");
		out.println("<TD CLASS= \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_ALIGN_TOP + "\"><B>Transaction date :</B>" + m_sTransactionDate + "</TD>");
		out.println("<TD CLASS= \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_ALIGN_TOP + "\"><B>Batch-Entry: </B>" + m_sBatchNumber + "-" + m_sEntryNumber + "</TD>");
		out.println("<TD CLASS= \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_ALIGN_TOP + "\"><B>Document no: </B>" + m_sDocumentNumber + "</TD>");
		out.println("</TR>");
		
		out.println("<TR CLASS= \"" + SMMasterStyleSheetDefinitions.TABLE_HEADING + "\">");
		out.println("<TD CLASS= \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_ALIGN_TOP + "\"><B>Posting Date: </B>" + m_sPostingDate + "</TD>");
		out.println("<TD CLASS= \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_ALIGN_TOP + "\"><B>Entry Ammount: </B>" + m_sEntryAmount + "</TD>");
		out.println("<TD CLASS= \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_ALIGN_TOP + "\"><B>Entry Description: </B>" + m_sEntryDesc + "</TD>");
		out.println("<TD COLSPAN = \"2\" CLASS= \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_ALIGN_TOP +"\"> &nbsp" + "</TD>");	
		out.println("</TR>");
		
		out.println("<TR CLASS= \"" + SMMasterStyleSheetDefinitions.TABLE_HEADING + "\">");
		out.println("<TD COLSPAN = \"5\" CLASS= \"" + SMMasterStyleSheetDefinitions.TABLE_ROW_BREAK +"\"> &nbsp" + "</TD>");	
		out.println("</TR>");
		
		out.println("<TR CLASS= \"" + SMMasterStyleSheetDefinitions.TABLE_HEADING + "\">");
		out.println("<TD CLASS= \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_ALIGN_TOP + "\"><U><B>Document Number</B></U></TD>");
		out.println("<TD CLASS= \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_ALIGN_TOP + "\"><U><B>GL Account</B></U></TD>");
		out.println("<TD CLASS= \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER_ALIGN_TOP + "\"><U><B>Account Desc.</B></U></TD>");
		out.println("<TD CLASS= \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\"><U><B>Debits</B></U></TD>");
		out.println("<TD CLASS= \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\"><U><B>Credits</B></U></TD>");
		out.println("</TR>");
		
	}
	private void printDocumentTotals(PrintWriter out){
		out.println("<TR CLASS= \"" + SMMasterStyleSheetDefinitions.TABLE_SUB_HEADING + "\">");
		out.println("<TD COLSPAN = \"2\"></TD>");
		out.println("<TD CLASS=\"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\">" + "<B>TOTAL:</B></TD>");
		out.println("<TD CLASS=\""+ SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\">" + clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(m_bdDebitTotal) + "</TD>");
		out.println("<TD CLASS=\""+ SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\">" + clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(m_bdCreditTotal) + "</TD>");
		out.println("</TR>");
		out.println("<TR CLASS= \"" + SMMasterStyleSheetDefinitions.TABLE_SUB_HEADING + "\">");
		out.println("<TD COLSPAN = \"5\" CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_ROW_BREAK +"\">&nbsp</TD>");
		out.println("</TR>");
	}
	private void printLineDetail(PrintWriter out, int iCount){
		
		if(iCount %2 ==0) {
			out.println("<TR CLASS= \"" + SMMasterStyleSheetDefinitions.TABLE_ROW_ODD + "\">");
		}else {
			out.println("<TR CLASS= \"" + SMMasterStyleSheetDefinitions.TABLE_ROW_EVEN + "\">");
		}
		out.println("<TD CLASS= \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\">" + clsServletUtilities.Fill_In_Empty_String_For_HTML_Cell(m_sLineApplyToDocNumber) + "</TD>");
		out.println("<TD CLASS= \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\">" + clsServletUtilities.Fill_In_Empty_String_For_HTML_Cell(m_sLineDistributionAcct) + "</TD>");
		out.println("<TD CLASS= \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\">" + clsServletUtilities.Fill_In_Empty_String_For_HTML_Cell(m_sDistAcctDesc) + "</TD>");
		out.println("<TD CLASS= \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\">" + m_sLineDebitAmount + "</TD>");
		out.println("<TD CLASS= \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\">" + m_sLineCreditAmount + "</TD>");
		out.println("</TR>");
	}
	private void printEntryDetail(PrintWriter out, int iCount){
		
		if(iCount %2 ==0) {
			out.println("<TR CLASS= \"" + SMMasterStyleSheetDefinitions.TABLE_ROW_ODD + "\">");
		}else {
			out.println("<TR CLASS= \"" + SMMasterStyleSheetDefinitions.TABLE_ROW_EVEN + "\">");
		}

		out.println("<TD CLASS= \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\">" + m_sDocumentNumber + "</TD>");
		out.println("<TD CLASS= \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\">" + clsServletUtilities.Fill_In_Empty_String_For_HTML_Cell(m_sControlAcct) + "</TD>");
		out.println("<TD CLASS= \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\">" + clsServletUtilities.Fill_In_Empty_String_For_HTML_Cell(m_sControlAcctDesc) + "</TD>");
		out.println("<TD CLASS= \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\">" + m_sEntryDebitAmount + "</TD>");
		out.println("<TD CLASS= \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\">" + m_sEntryCreditAmount + "</TD>");
		out.println("</TR>");
		
	}
	private void printReportTotal(PrintWriter out){
		//out.println("<BR><B><U>REPORT TOTALS:</U></B><BR>");

		out.println("<TR CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_TOTAL + "\">");
		out.println("<TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\"><U><B>REPORT TOTALS: </B></U></TD>");
		out.println("<TD>&nbsp</TD>");
		out.println("<TD>&nbsp</TD>");
		out.println("<TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\"><U><B>" + "NUMBER OF TRANSACTIONS" + "</B></U></TD>");
		out.println("<TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\">" + "<U><B>" + "TOTAL TRANSACTION AMMOUNT" + "</B></U></TD>");
		out.println("</TR>");
		out.println("<TR CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_TOTAL + "\">");
		out.println("<TD>&nbsp</TD>");
		out.println("<TD>&nbsp</TD>");
		out.println("<TD>&nbsp</TD>");
		out.println("<TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\">" + Long.toString(m_lTransactionCount) + "</TD>");
		out.println("<TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\">"  + clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(m_bdReportDebitTotal) + "</TD>");
		out.println("</TR>");
		out.println("</TABLE><P><BR></P>");

	}
	private boolean printGLSummary(Connection conn, PrintWriter out){
		//out.println("<BR><B><U>GENERAL LEDGER SUMMARY:</U></B>"); 
		out.println("<TABLE WIDTH = 100% CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_BASIC_WITH_BORDER_COLLAPSE + "\">");
		out.println("<CAPTION CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_CENTER_JUSTIFIED_ARIAL_SMALL_WO_BORDER_BOLD+ "\" ><B>GENERAL LEDGER SUMMARY:<B></CAPTION>");
		out.println("<TR CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_HEADING + "\">");
		out.println("<TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL + "\"><B>" + "GL ACCOUNT" + "</B></TD>");
		out.println("<TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL + "\"><B>" + "ACCOUNT DESCRIPTION" + "</B></TD>");
		out.println("<TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL + "\"><B>" + "NET DEBITS" + "</B></TD>");
		out.println("<TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL + "\"><B>" + "NET CREDITS" + "</B></TD>");
		out.println("</TR>");
		
		BigDecimal bdDebitTotal = new BigDecimal(0);
		BigDecimal bdCreditTotal = new BigDecimal(0);
		GLAccount glacct;
	
		for (int i = 0; i <m_arrGLAccountArray.size(); i++){
			if(i%2 !=0 ) {
				out.println("<TR CLASS= \"" + SMMasterStyleSheetDefinitions.TABLE_ROW_EVEN + "\">");
			}else {
				out.println("<TR CLASS= \"" + SMMasterStyleSheetDefinitions.TABLE_ROW_ODD + "\">");
			}
			out.println("<TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\">" + m_arrGLAccountArray.get(i) + "</TD>");
			glacct = new GLAccount(m_arrGLAccountArray.get(i));
			if (!glacct.load(conn)){
				m_sErrorMessage = "Error reading GL description in ARPostingJournal.printGLSummary for '" + m_sControlAcct + "'" + glacct.getErrorMessageString();
				return false;
			}
			out.println("<TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\">" + glacct.getM_sdescription() + "</TD>");
			
			//If the debit total for this account is greater than or equal to the credit total
			if(m_arrGLDebitTotalArray.get(i).compareTo(m_arrGLCreditTotalArray.get(i)) >= 0){
				
				//Then print the debit difference:
				out.println("<TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\">" + clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(
					m_arrGLDebitTotalArray.get(i).subtract(m_arrGLCreditTotalArray.get(i))) 
					+ "</TD>");
				//And a blank for the credit:
				out.println("<TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\">" + "&nbsp;" + "</TD>");
				bdDebitTotal = bdDebitTotal.add(m_arrGLDebitTotalArray.get(i).subtract(m_arrGLCreditTotalArray.get(i)));
			}else{
				//Print a blank for the credit:
				out.println("<TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\">" + "&nbsp;" + "</TD>");

				//Then print the credit difference:
				out.println("<TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\">" + clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(
					m_arrGLCreditTotalArray.get(i).subtract(m_arrGLDebitTotalArray.get(i))) 
					+ "</TD>");
				bdCreditTotal = bdCreditTotal.add(m_arrGLCreditTotalArray.get(i).subtract(m_arrGLDebitTotalArray.get(i)));
			}
			
			out.println("</TR>");
		}
		out.println("<TR CLASS= \"" + SMMasterStyleSheetDefinitions.TABLE_TOTAL + "\">");
		out.println("<TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\">" + "&nbsp;" + "</TD>");
		out.println("<TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\"><B>" + "TOTAL:" + "</B></TD>");
		out.println("<TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\">" + clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(bdDebitTotal) + "</TD>");
		out.println("<TD CLASS = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\">" + clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(bdCreditTotal) + "</TD>");
		out.println("</TR>");
		out.println("</TABLE>");
		return true;
	}
	
	public String getErrorMessage(){
		return m_sErrorMessage;
	}
	public void setM_sBorderWidth(String borderWidth) {
	}
}