package smgl;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.ResultSet;
import java.util.ArrayList;

import javax.servlet.ServletContext;

import SMDataDefinition.SMMasterStyleSheetDefinitions;
import SMDataDefinition.SMTableglaccounts;
import SMDataDefinition.SMTableglfinancialstatementdata;
import ServletUtilities.clsDatabaseFunctions;

public class GLTrialBalanceReport  extends java.lang.Object{

	public GLTrialBalanceReport(){
		
	}
	
	public String processReport(
		Connection conn,
		String sDBID,
		ServletContext context,
		boolean bDownloadAsHTML,
		String sStartingAccount,
		String sEndingAccount,
		String sStartingAccountGroup,
		String sEndingAccountGroup,
		String sFiscalYearAndPeriod,
		String sReportType,
		boolean bIncludeAccountsWithNoActivity,
		ArrayList<String>alStartingSegmentIDs,
		ArrayList<String>alStartingSegmentValueIDs,
		ArrayList<String>alEndingSegmentIDs,
		ArrayList<String>alEndingSegmentValueIDs
		) throws Exception{
		
		String s = "";

		
		s += printTableHeading();
		s += buildReport(
			sReportType, 
			sFiscalYearAndPeriod,
			conn,
			sDBID, 
			context
		);
		//s += printReportTotals(sPrintTransactionsInDetailOrSummary.compareToIgnoreCase(APAgedPayablesSelect.PARAM_PRINT_TRANSACTION_IN_SUMMARY_LABEL) == 0);
		
		s += printTableFooting();
		
		return s;
	}
	
	private String buildReport(
		String sReportType,
		String sFiscalYearAndPeriod,
		Connection conn,
		String sDBID, 
		ServletContext context) throws Exception{
		
		String s = "";
		
		s += printColumnHeadings(sReportType);

		String sFiscalYear = sFiscalYearAndPeriod.substring(0, sFiscalYearAndPeriod.indexOf(GLTrialBalanceSelect.PARAM_VALUE_DELIMITER));
		String sFiscalPeriod = sFiscalYearAndPeriod.replace(sFiscalYear + GLTrialBalanceSelect.PARAM_VALUE_DELIMITER, "");
		
		String sSQL = "SELECT"
			+ " " + SMTableglfinancialstatementdata.TableName + "." + SMTableglfinancialstatementdata.sacctid
			+ ", " + SMTableglfinancialstatementdata.TableName + "." + SMTableglfinancialstatementdata.bdtotalyeartodate
			+ ", " + SMTableglaccounts.TableName + "." + SMTableglaccounts.sDesc
			+ ", " + SMTableglaccounts.TableName + "." + SMTableglaccounts.inormalbalancetype
			+ " FROM " + SMTableglfinancialstatementdata.TableName
			+ " LEFT JOIN " + SMTableglaccounts.TableName 
			+ " ON " + SMTableglaccounts.TableName + "." + SMTableglaccounts.sAcctID 
			+ " = " + SMTableglfinancialstatementdata.TableName + "." + SMTableglfinancialstatementdata.sacctid

			//WHERE CLAUSE:
			+ " WHERE ("
				+ "(" + SMTableglfinancialstatementdata.TableName + "." + SMTableglfinancialstatementdata.ifiscalyear + " = " + sFiscalYear + ")"
				+ " AND (" + SMTableglfinancialstatementdata.TableName + "." + SMTableglfinancialstatementdata.ifiscalperiod + " = " + sFiscalPeriod + ")"
			+ ")"
			+ " ORDER BY " + SMTableglfinancialstatementdata.TableName + "." + SMTableglfinancialstatementdata.sacctid
		;
		
		boolean bOddRow = false;
		BigDecimal bdDebitTotal = new BigDecimal("0.00");
		BigDecimal bdCreditTotal = new BigDecimal("0.00");
		BigDecimal bdEarningsTotal = new BigDecimal("0.00");
		try {
			ResultSet rs = clsDatabaseFunctions.openResultSet(sSQL, conn);
			while(rs.next()){
				
				BigDecimal bdDebit = new BigDecimal("0.00");
				BigDecimal bdCredit = new BigDecimal("0.00");
				BigDecimal bdAmount = rs.getBigDecimal(SMTableglfinancialstatementdata.bdtotalyeartodate);
				//If the account is normally a debit balance:
				if (rs.getInt(SMTableglaccounts.TableName + "." + SMTableglaccounts.inormalbalancetype) == SMTableglaccounts.NORMAL_BALANCE_TYPE_DEBIT){
					if (bdAmount.compareTo(BigDecimal.ZERO) > 0){
						bdDebit = bdAmount;
						bdCredit = BigDecimal.ZERO;
					}else{
						bdDebit = BigDecimal.ZERO;
						bdCredit = bdAmount.negate();
					}
				// But if the account is normally a credit balance:
				}else{
					if (bdAmount.compareTo(BigDecimal.ZERO) < 0){
						bdDebit = BigDecimal.ZERO;
						bdCredit = bdAmount.negate();
					}else{
						bdDebit = bdAmount;
						bdCredit = BigDecimal.ZERO;
					}
				}
				
				bdDebitTotal = bdDebitTotal.add(bdDebit);
				bdCreditTotal = bdCreditTotal.add(bdCredit);
				s += printBalanceSheetLine(
						rs.getString(SMTableglfinancialstatementdata.TableName + "." + SMTableglfinancialstatementdata.sacctid),
						rs.getString(SMTableglaccounts.TableName + "." + SMTableglaccounts.sDesc),
						bdDebit,
						bdCredit,
						bOddRow);
				
				bOddRow = !bOddRow;
			}
			rs.close();
		} catch (Exception e1) {
			throw new Exception("<BR><FONT COLOR=RED><B>Error [1553381089] reading GL transactions with SQL: '" + sSQL + " - " + e1.getMessage() + "</B></FONT><BR>");
		}
		
		s += printBalanceReportTotals(bdDebitTotal, bdCreditTotal, bdEarningsTotal);
		
		return s;
	}
	
	private String printBalanceSheetLine(
			String sGLAccount,
			String sAccountDescription,
			BigDecimal bdDebitAmt,
			BigDecimal bdCreditAmt,
			boolean bOddRow
			) throws Exception{
		String s = "";
		
		if (bOddRow){
			s += "  <TR class = \"" + SMMasterStyleSheetDefinitions.TABLE_ROW_BACKGROUNDCOLOR_WHITE + " \" >\n";
		}else{
			s += "  <TR class = \"" + SMMasterStyleSheetDefinitions.TABLE_ROW_BACKGROUNDCOLOR_LIGHTBLUE + " \" >\n";
		}
		
		s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + " \" >"
			+  sGLAccount
			+ "</TD>\n"
			+ "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + " \" >"
			+  sAccountDescription
			+ "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + " \" >"
			+  ServletUtilities.clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(bdDebitAmt)
			+ "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + " \" >"
			+  ServletUtilities.clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(bdCreditAmt)
			+ "  </TR>\n"
		;
		
		return s;
	}
	
	private String printBalanceReportTotals(BigDecimal bdDebitTotal, BigDecimal bdCreditTotal, BigDecimal bdEarningsTotal){
		String s = "";

		s += "  <TR class = \"" + SMMasterStyleSheetDefinitions.TABLE_ROW_BACKGROUNDCOLOR_WHITE + " \" >\n";
		
		s += "    <TD COLSPAN=2 class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + " \" >"
			+  "<B>" + "TOTAL:" + "</B>"
			+ "</TD>\n"

			+ "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\""
				+ " style = \" border-top: 2px solid black; \""
			+ " >"
			+  "<B>" + ServletUtilities.clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(bdDebitTotal) + "</B>"
			+ "</TD>\n"
			
			+ "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\""
			+ " style = \" border-top: 2px solid black; \""
			+ " >"
			+  "<B>" + ServletUtilities.clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(bdCreditTotal) + "</B>"
			+ "</TD>\n"

			+ "  </TR>\n"
		;
		
		s += "  <TR class = \"" + SMMasterStyleSheetDefinitions.TABLE_ROW_BACKGROUNDCOLOR_WHITE + " \" >\n";
		
		s += "    <TD COLSPAN=2 class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + " \" >"
			+  "<B>" + "NET INCOME (LOSS) FOR ACCOUNTS LISTED:" + "</B>"
			+ "</TD>\n"

			+ "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + " \" >"
			+  "&nbsp;"
			+ "</TD>\n"
			
			+ "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + "\""
			+ " style = \" border-top: 2px solid black; \""
			+ " >"
			+  "<B>" + ServletUtilities.clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(bdEarningsTotal) + "</B>"
			+ "</TD>\n"

			+ "  </TR>\n"
		;
		return s;
	}
	
	private String printColumnHeadings(String sReportType){
		String s = "";
		
		if (sReportType.compareToIgnoreCase(GLTrialBalanceSelect.REPORT_TYPE_NET_CHANGES) == 0){
			s += "  <TR class = \"" + SMMasterStyleSheetDefinitions.TABLE_ROW_BACKGROUNDCOLOR_WHITE + " \" >\n";
			
			s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_LEFT_JUSTIFIED + " \" >"
				+  "&nbsp;"
				+ "</TD>"
				
				+ "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_LEFT_JUSTIFIED + " \" >"
				+  "&nbsp;"
				+ "</TD>"
				
				+"    <TD COLSPAN=2 class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_CENTER_JUSTIFIED + " \" >"
				+  "-------Opening Balance-------"
				+ "</TD>"

				+"    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_LEFT_JUSTIFIED + " \" >"
				+  "&nbsp;"
				+ "</TD>"

				+"    <TD COLSPAN=2 class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_CENTER_JUSTIFIED + " \" >"
				+  "-------Ending Balance-------"
				+ "</TD>"
			;	
			
			s += "  </TR>\n";
			
			s += "  <TR class = \"" + SMMasterStyleSheetDefinitions.TABLE_ROW_BACKGROUNDCOLOR_WHITE + " \" >\n";
			
			s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_LEFT_JUSTIFIED + " \" >"
				+  "Account"
				+ "</TD>"
				
				+ "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_LEFT_JUSTIFIED + " \" >"
				+  "Description"
				+ "</TD>"
				
				+"    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_RIGHT_JUSTIFIED + " \" >"
				+  "Debits"
				+ "</TD>"

				+"    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_RIGHT_JUSTIFIED + " \" >"
				+  "Credits"
				+ "</TD>"
				
				+"    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_RIGHT_JUSTIFIED + " \" >"
				+  "Net Changes"
				+ "</TD>"

				+"    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_RIGHT_JUSTIFIED + " \" >"
				+  "Debits"
				+ "</TD>"

				+"    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_RIGHT_JUSTIFIED + " \" >"
				+  "Credits"
				+ "</TD>"
			;	
			
			s += "  </TR>\n";
			
		}else{
			s += "  <TR class = \"" + SMMasterStyleSheetDefinitions.TABLE_ROW_BACKGROUNDCOLOR_WHITE + " \" >\n";
			
			s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_LEFT_JUSTIFIED + " \" >"
				+  "Account"
				+ "</TD>"
				
				+ "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_LEFT_JUSTIFIED + " \" >"
				+  "Description"
				+ "</TD>"
				
				+"    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_RIGHT_JUSTIFIED + " \" >"
				+  "Debits"
				+ "</TD>"

				+"    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_RIGHT_JUSTIFIED + " \" >"
				+  "Credits"
				+ "</TD>"
			;	
		}

		s += "  </TR>\n";

		return s;
	}
	
	private String printTableHeading(){
		String s = "";
		
		s += "<TABLE class = \"" + SMMasterStyleSheetDefinitions.TABLE_BASIC_WITHOUT_BORDER + "\" >\n";
		
		return s;
	}

	private String printTableFooting(){
		String s = "";
		
		s += "</TABLE>\n";
		
		return s;
	}
}
