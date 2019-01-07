package smap;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.ResultSet;
import java.util.ArrayList;

import javax.servlet.ServletContext;

import SMDataDefinition.SMMasterStyleSheetDefinitions;
import SMDataDefinition.SMTableapaccountsets;
import SMDataDefinition.SMTableapbatchentries;
import SMDataDefinition.SMTableapmatchinglines;
import SMDataDefinition.SMTableaptransactions;
import SMDataDefinition.SMTableicvendors;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsDateAndTimeConversions;
import ServletUtilities.clsManageBigDecimals;
import smcontrolpanel.SMUtilities;

public class APAgedPayablesReport  extends java.lang.Object{

	private final static int EARNED_DISCOUNT_TYPE = 9; //This isn't a transaction type, but it can appear as a separate line on the report
	
	private final static int AGING_CATEGORY_CURRENT = 0;
	private final static int AGING_CATEGORY_FIRST = 1;
	private final static int AGING_CATEGORY_SECOND = 2;
	private final static int AGING_CATEGORY_THIRD = 3;
	private final static int AGING_CATEGORY_FOURTH = 4;
	
	ArrayList<clsVendorSection>arrVendorSections;
	private static int m_iNumberOfColumns = 12;
	private int m_i1stAgingCategoryNumberOfDays = 0;
	private int m_i2ndAgingCategoryNumberOfDays = 0;
	private int m_i3rdAgingCategoryNumberOfDays = 0;
	private ArrayList<BigDecimal>arrReportTotals;
	
	public APAgedPayablesReport(
		String s1stAgingCategoryNumberOfDays,
		String s2ndAgingCategoryNumberOfDays,
		String s3rdAgingCategoryNumberOfDays
			){
		
		m_i1stAgingCategoryNumberOfDays = Integer.parseInt(s1stAgingCategoryNumberOfDays);
		m_i2ndAgingCategoryNumberOfDays = Integer.parseInt(s2ndAgingCategoryNumberOfDays);
		m_i3rdAgingCategoryNumberOfDays = Integer.parseInt(s3rdAgingCategoryNumberOfDays);
		arrVendorSections = new ArrayList<clsVendorSection>(0);
		arrReportTotals = new ArrayList<BigDecimal>(0);
		for(int iAgingCategory = AGING_CATEGORY_CURRENT; iAgingCategory <= AGING_CATEGORY_FOURTH; iAgingCategory++){
			arrReportTotals.add(new BigDecimal("0.00"));
		}
	}
	
	public String processReport(
		Connection conn,
		String sAgeAsOf,
		String sCutoffBy,
		String sCutOffDate,
		String sStartingVendor,
		String sEndingVendor,
		String sAccountSet,
		String sSortBy,
		String sPrintTransactionsInDetailOrSummary,
		boolean bPrintVendorswithaZeroBalance,
		boolean bIncludeAppliedDetails,
		boolean bIncludeFullyPaidTransactions,
		boolean bSortByTransactionType,
		boolean bIncludeInactiveVendors,
		boolean bIncludeTransactionsOnHold,
		boolean bDownloadAsHTML,
		boolean bIncludeLinkToTransactionInformation,
		boolean bIncludeLinkToVendorInfo,
		String sDBID,
		ServletContext context
		) throws Exception{
		
		String s = "";
		try {
			loadTransactions(
				conn,
				sAgeAsOf,
				sCutoffBy,
				sStartingVendor,
				sEndingVendor,
				sAccountSet,
				sSortBy,
				bPrintVendorswithaZeroBalance,
				bIncludeAppliedDetails,
				bIncludeFullyPaidTransactions,
				bSortByTransactionType,
				bIncludeInactiveVendors,
				bIncludeTransactionsOnHold,
				bIncludeLinkToVendorInfo,
				sDBID,
				context
			);
		} catch (Exception e) {
			throw new Exception("Error [1495721942] - " + e.getMessage());
		}

		if(bIncludeAppliedDetails){
			try {
				loadApplyingTransactions(
					conn,
					sCutoffBy,
					sStartingVendor,
					sEndingVendor,
					bIncludeInactiveVendors
					);
			} catch (Exception e) {
				throw new Exception("Error [1495815257] - " + e.getMessage());
			}
		}
		
		s += printTableHeading();
		
		s += buildReport(
			bPrintVendorswithaZeroBalance, 
			bIncludeAppliedDetails, 
			bIncludeLinkToTransactionInformation,
			bIncludeLinkToVendorInfo,
			sPrintTransactionsInDetailOrSummary.compareToIgnoreCase(APAgedPayablesSelect.PARAM_PRINT_TRANSACTION_IN_SUMMARY_LABEL) == 0,
			sDBID, 
			context
		);
		
		s += printReportTotals(sPrintTransactionsInDetailOrSummary.compareToIgnoreCase(APAgedPayablesSelect.PARAM_PRINT_TRANSACTION_IN_SUMMARY_LABEL) == 0);
		
		s += printTableFooting();
		
		s += printDocTypeLegends();
		
		return s;
	}
	
	private void loadTransactions(
		Connection conn,
		String sAgeAsOf,
		String sCutoffBy,
		String sStartingVendor,
		String sEndingVendor,
		String sAccountSet,
		String sSortBy,
		boolean bPrintVendorswithaZeroBalance,
		boolean bIncludeAppliedDetails,
		boolean bIncludeFullyPaidTransactions,
		boolean bSortByTransactionType,
		boolean bIncludeInactiveVendors,
		boolean bIncludeTransactionsOnHold,
		boolean bIncludeLinkToVendorInfo,
		String sDBID,
		ServletContext context
	) throws Exception{
		
		//First, we load all the transactions in the range that we've asked for:
		String SQL = "SELECT"
			+ " " + SMTableaptransactions.TableName + ".*"
			+ ", " + SMTableicvendors.TableName + "." + SMTableicvendors.sname
			+ ", DATEDIFF(" 
				+ "STR_TO_DATE('" + sAgeAsOf + "', '%m/%d/%Y'), "
				+ SMTableaptransactions.TableName + "." + SMTableaptransactions.datduedate			
			+ ") AS NUMBEROFDAYSPASTDUE"
			+ " FROM " + SMTableaptransactions.TableName
			+ " LEFT JOIN " + SMTableicvendors.TableName
			+ " ON " + SMTableaptransactions.TableName + "." + SMTableaptransactions.svendor + " = " + SMTableicvendors.TableName + "." + SMTableicvendors.svendoracct
			+ " LEFT JOIN " + SMTableapaccountsets.TableName
			+ " ON " + SMTableicvendors.TableName + "." + SMTableicvendors.iapaccountset + " = " + SMTableapaccountsets.TableName + "." + SMTableapaccountsets.lid
			+ " WHERE ("
				+ "(" + SMTableaptransactions.svendor + " >= '" + sStartingVendor + "')"
				+ " AND (" + SMTableaptransactions.svendor + " <= '" + sEndingVendor + "')"

			;
		
		//Account Sets:
		if (sAccountSet.compareToIgnoreCase(APAgedPayablesSelect.PARAM_ACCOOUNT_SET_ALL_ACCOUNT_SETS) != 0){
			SQL += " AND (" + SMTableapaccountsets.TableName + "." + SMTableapaccountsets.sacctsetname + " = " + sAccountSet + ")";
		}
		
		//Include fully paid transactions:
		if(!bIncludeFullyPaidTransactions){
			SQL += " AND (" + SMTableaptransactions.TableName + "." + SMTableaptransactions.bdcurrentamt + " != 0.00)";
		}

		if(!bIncludeInactiveVendors){
			SQL += " AND (" + SMTableicvendors.TableName + "." + SMTableicvendors.iactive + " != 0)";
		}

		if(!bIncludeTransactionsOnHold){
			SQL += " AND (" + SMTableaptransactions.TableName + "." + SMTableaptransactions.ionhold + " != 1)";
		}

		//End the 'where' clause:
		SQL += ")";
		
		String sFirstSort = SMTableicvendors.TableName + "." + SMTableicvendors.sname;
		if (sSortBy.compareToIgnoreCase(APAgedPayablesSelect.PARAM_SORT_BY_ACCOUNT) == 0){
			sFirstSort = SMTableaptransactions.TableName + "." + SMTableaptransactions.svendor;
		}
		
		//We always make the second sort the transaction date:
		String sSecondSort = ", " + SMTableaptransactions.TableName + "." + SMTableaptransactions.datdocdate;
		
		String sThirdSort = "";
		if(bSortByTransactionType){
			sThirdSort = ", " + SMTableaptransactions.TableName + "." + SMTableaptransactions.idoctype;
		}
		
		SQL += " ORDER BY " + sFirstSort + sSecondSort + sThirdSort;
		//System.out.println("[1495737533] SQL = '" + SQL + "'");
		
		try {
			ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
			while (rs.next()){

				String sVendor = rs.getString(SMTableaptransactions.TableName + "." + SMTableaptransactions.svendor);
				//Try to find the 'vendor section' for this vendor:
				clsVendorSection vs = null;
				for (int i = 0; i < arrVendorSections.size(); i++){
					if (arrVendorSections.get(i).getVendor().compareToIgnoreCase(sVendor) == 0){
						vs = arrVendorSections.get(i);
					}
				}
				if (vs == null){
					arrVendorSections.add(new clsVendorSection(
						sVendor,
						rs.getString(SMTableicvendors.TableName + "." + SMTableicvendors.sname)
						)
					);
					vs = arrVendorSections.get(arrVendorSections.size() - 1);
				}
				
				//Now add the transaction info to the vendor section:
				String sDocDate = clsDateAndTimeConversions.resultsetDateStringToFormattedString(
					rs.getString(SMTableaptransactions.TableName + "." + SMTableaptransactions.datdocdate),
					SMUtilities.DATE_FORMAT_FOR_DISPLAY, SMUtilities.EMPTY_DATE_VALUE);
				String sDueDate = clsDateAndTimeConversions.resultsetDateStringToFormattedString(
						rs.getString(SMTableaptransactions.TableName + "." + SMTableaptransactions.datduedate),
						SMUtilities.DATE_FORMAT_FOR_DISPLAY, SMUtilities.EMPTY_DATE_VALUE);
				
				//If we are NOT showing the applied transactions, then we store the CURRENT AMT on the transactions
				//But if we ARE showing the applied transactions, then we store the ORIGINAL AMT on the transactions,
				//since we'll be showing the applying amts underneath each transaction:
				BigDecimal bdAmt = rs.getBigDecimal(SMTableaptransactions.TableName + "." + SMTableaptransactions.bdcurrentamt);
				if (bIncludeAppliedDetails){
					// [1510424410] - should this happen when we are including applied details??
					bdAmt = rs.getBigDecimal(SMTableaptransactions.TableName + "." + SMTableaptransactions.bdoriginalamt);
				}
				
				//if (rs.getString(SMTableaptransactions.TableName + "." + SMTableaptransactions.sdocnumber).compareToIgnoreCase("40406037") == 0){
				//	System.out.println("[1498148041] - rs.getBigDecimal(SMTableaptransactions.bdoriginalamt) = " + rs.getBigDecimal(SMTableaptransactions.TableName + "." + SMTableaptransactions.bdoriginalamt));
				//}
				
				clsTransaction trans = new clsTransaction(
					sDocDate,
					sDueDate,
					bdAmt,
					rs.getString(SMTableaptransactions.TableName + "." + SMTableaptransactions.sdocnumber),
					rs.getInt(SMTableaptransactions.TableName + "." + SMTableaptransactions.idoctype),
					rs.getInt("NUMBEROFDAYSPASTDUE"),
					rs.getLong(SMTableaptransactions.TableName + "." + SMTableaptransactions.lid),
					rs.getInt(SMTableaptransactions.TableName + "." + SMTableaptransactions.ionhold) == 1
				);
				vs.addTransaction(trans);
			}
			rs.close();
		} catch (Exception e) {
			throw new Exception("Error [1495721941] reading AP Transactions with SQL: '" + SQL + "' - " + e.getMessage());
		}
		
		return;
	}
	
	private void loadApplyingTransactions(
			Connection conn,
			String sCutoffBy,
			String sStartingVendor,
			String sEndingVendor,
			boolean bIncludeInactiveVendors
		) throws Exception{
		
		String SQL = "SELECT"
			+ " " + SMTableapmatchinglines.TableName + ".*"
			+ ", " + SMTableaptransactions.TableName + ".*"
			+ " FROM " + SMTableapmatchinglines.TableName
			+ " LEFT JOIN " + SMTableaptransactions.TableName
			+ " ON " + SMTableapmatchinglines.TableName + "." + SMTableapmatchinglines.ltransactionappliedfromid + " = " + SMTableaptransactions.TableName + "." + SMTableaptransactions.lid
			+ " LEFT JOIN " + SMTableicvendors.TableName
			+ " ON " + SMTableicvendors.TableName + "." + SMTableicvendors.svendoracct + " = " + SMTableapmatchinglines.TableName + "." + SMTableapmatchinglines.svendor
			+ " WHERE ("
				+ "(" + SMTableapmatchinglines.TableName + "." + SMTableapmatchinglines.svendor + " >= '" + sStartingVendor + "')"
				+ " AND (" + SMTableapmatchinglines.TableName + "." + SMTableapmatchinglines.svendor + " <= '" + sEndingVendor + "')"
			;
		
		if (!bIncludeInactiveVendors){
			SQL += " AND (" + SMTableicvendors.TableName + "." + SMTableicvendors.iactive + " = 1)";
		}
		
		SQL += ")";

		SQL += " ORDER BY " + SMTableapmatchinglines.TableName + "." + SMTableapmatchinglines.dattransactiondate;
		
		//System.out.println("[1510426478] - load applying transactions SQL = '" + SQL + "'");
		try {
			ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
			while (rs.next()){
				//System.out.println("[1510426480] - into While loop...arrVendorSections.size() = " + arrVendorSections.size());
				//If there's a corresponding transaction, add this to the 'applying transactions':
				for (int i = 0; i < arrVendorSections.size(); i++){
					//System.out.println("[1510426481] - into for loop...arrVendorSections.get(i).getVendor() = '" + arrVendorSections.get(i).getVendor() + "'");
					//System.out.println("[1510426482] - rs.getString(SMTableapmatchinglines.svendor) '" + rs.getString(SMTableapmatchinglines.TableName + "." + SMTableapmatchinglines.svendor) + "'");
					if (arrVendorSections.get(i).getVendor().compareToIgnoreCase(rs.getString(SMTableapmatchinglines.TableName + "." + SMTableapmatchinglines.svendor)) == 0){
//						System.out.println("[1510426479] - adding applying transactions for vendor '" + arrVendorSections.get(i).getVendor() + "'");
						arrVendorSections.get(i).addApplyingTransactionForVendor(
							rs.getString(SMTableapmatchinglines.TableName + "." + SMTableapmatchinglines.sappliedtodocnumber),
							clsDateAndTimeConversions.resultsetDateStringToFormattedString(
								rs.getString(SMTableapmatchinglines.TableName + "." + SMTableapmatchinglines.dattransactiondate), 
								SMUtilities.DATE_FORMAT_FOR_DISPLAY, 
								SMUtilities.EMPTY_DATE_VALUE),
							rs.getBigDecimal(SMTableapmatchinglines.TableName + "." + SMTableapmatchinglines.bdappliedamount),
							rs.getString(SMTableapmatchinglines.TableName + "." + SMTableapmatchinglines.sappliedfromdocnumber),
							rs.getInt(SMTableaptransactions.TableName + "." + SMTableaptransactions.idoctype),
							rs.getLong(SMTableapmatchinglines.TableName + "." + SMTableapmatchinglines.ltransactionappliedfromid)
						);
						
						//IF there's a 'discount amt applied', this has to appear as if it is another applying transction:
						if (rs.getBigDecimal(SMTableapmatchinglines.TableName + "." + SMTableapmatchinglines.bddiscountappliedamount).compareTo(BigDecimal.ZERO) > 0){
							arrVendorSections.get(i).addApplyingTransactionForVendor(
								rs.getString(SMTableapmatchinglines.TableName + "." + SMTableapmatchinglines.sappliedtodocnumber),
								clsDateAndTimeConversions.resultsetDateStringToFormattedString(
									rs.getString(SMTableapmatchinglines.TableName + "." + SMTableapmatchinglines.dattransactiondate), 
									SMUtilities.DATE_FORMAT_FOR_DISPLAY, 
									SMUtilities.EMPTY_DATE_VALUE),
								rs.getBigDecimal(SMTableapmatchinglines.TableName + "." + SMTableapmatchinglines.bddiscountappliedamount),
								rs.getString(SMTableapmatchinglines.TableName + "." + SMTableapmatchinglines.sappliedfromdocnumber),
								EARNED_DISCOUNT_TYPE,
								rs.getLong(SMTableapmatchinglines.TableName + "." + SMTableapmatchinglines.ltransactionappliedfromid)
							);
						}
					}
				}
			}
			rs.close();
		} catch (Exception e) {
			throw new Exception("Error [1495815036] loading applying transactions with SQL: '" + SQL + "' - " + e.getMessage());
		}
		return;
	}
	
	private String buildReport(
		boolean bPrintVendorsWithAZeroBalance, 
		boolean bIncludeAppliedDetails,
		boolean bIncludeLinkToTransactionInformation,
		boolean bIncludeLinkToVendorInformation,
		boolean bPrintInSummary,
		String sDBID, 
		ServletContext context) throws Exception{
		
		String s = "";
		
		s += printColumnHeadings(bIncludeAppliedDetails, bPrintInSummary);
		
		for (int i = 0; i < arrVendorSections.size(); i++){
			s += arrVendorSections.get(i).printVendorSection(
				bPrintVendorsWithAZeroBalance, 
				bIncludeLinkToTransactionInformation, 
				bIncludeLinkToVendorInformation,
				sDBID, 
				context, 
				bPrintInSummary);
		}
		
		return s;
	}
	
	private String printReportTotals(boolean bPrintInSummary){
		String s = "";
		
		BigDecimal bdTotalPayables = 
			arrReportTotals.get(AGING_CATEGORY_CURRENT).add(
			arrReportTotals.get(AGING_CATEGORY_FIRST)).add(
			arrReportTotals.get(AGING_CATEGORY_SECOND)).add(
			arrReportTotals.get(AGING_CATEGORY_THIRD)).add(
			arrReportTotals.get(AGING_CATEGORY_FOURTH))
		;
		BigDecimal bdTotalOverdue = 
			arrReportTotals.get(AGING_CATEGORY_FIRST).add(
			arrReportTotals.get(AGING_CATEGORY_SECOND)).add(
			arrReportTotals.get(AGING_CATEGORY_THIRD)).add(
			arrReportTotals.get(AGING_CATEGORY_FOURTH))
		;
		s += "  <TR class = \"" + SMMasterStyleSheetDefinitions.TABLE_ROW_BACKGROUNDCOLOR_WHITE + " \" >\n";
		
		if (bPrintInSummary){
			s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_FIELDCONTROL_RIGHT_JUSTIFIED + " \" COLSPAN=2 >"
				+ "<B>Report totals:</B>"
				+ "</TD>\n"
			;
		}else{
			s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_FIELDCONTROL_RIGHT_JUSTIFIED + " \" COLSPAN=" + Integer.toString(m_iNumberOfColumns - 7) + " >"
				+ "<B>Report totals:</B>"
				+ "</TD>\n"
			;	
		}
		
		s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + " \" >"
			+  "<B>" + clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(arrReportTotals.get(AGING_CATEGORY_CURRENT)) + "</B>"
			+ "</TD>\n"

			+ "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + " \" >"
			+  "<B>" + clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(arrReportTotals.get(AGING_CATEGORY_FIRST)) + "</B>"
			+ "</TD>\n"
			
			+ "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + " \" >"
			+  "<B>" + clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(arrReportTotals.get(AGING_CATEGORY_SECOND)) + "</B>"
			+ "</TD>\n"

			+ "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + " \" >"
			+  "<B>" + clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(arrReportTotals.get(AGING_CATEGORY_THIRD)) + "</B>"
			+ "</TD>\n"

			+ "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + " \" >"
			+  "<B>" + clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(arrReportTotals.get(AGING_CATEGORY_FOURTH)) + "</B>"
			+ "</TD>\n"

			+ "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + " \" >"
			+  "<B>" 
			+ clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(bdTotalOverdue)
			+ "</B>"
			+ "</TD>\n"
			
			+ "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + " \" >"
			+  "<B>" 
			+ clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(bdTotalPayables)
			+ "</B>"
			+ "</TD>\n"
			
			+ "  </TR>\n"
		;
		
		//Percentages:
		BigDecimal bdCurrentPct = new BigDecimal("0.00");
		BigDecimal bd1stPct = new BigDecimal("0.00");
		BigDecimal bd2ndPct = new BigDecimal("0.00");
		BigDecimal bd3rdPct = new BigDecimal("0.00");
		BigDecimal bd4thPct = new BigDecimal("0.00");
		BigDecimal bdOverduePct = new BigDecimal("0.00");
		BigDecimal bdTotalPct = new BigDecimal("0.00");
		
		if (bdTotalPayables.compareTo(BigDecimal.ZERO) != 0){
			bdCurrentPct = (arrReportTotals.get(AGING_CATEGORY_CURRENT).divide(bdTotalPayables, 2, BigDecimal.ROUND_HALF_UP)).multiply(new BigDecimal("100.00"));
			bd1stPct = (arrReportTotals.get(AGING_CATEGORY_FIRST).divide(bdTotalPayables, 2, BigDecimal.ROUND_HALF_UP)).multiply(new BigDecimal("100.00"));
			bd2ndPct = (arrReportTotals.get(AGING_CATEGORY_SECOND).divide(bdTotalPayables, 2, BigDecimal.ROUND_HALF_UP)).multiply(new BigDecimal("100.00"));
			bd3rdPct = (arrReportTotals.get(AGING_CATEGORY_THIRD).divide(bdTotalPayables, 2, BigDecimal.ROUND_HALF_UP)).multiply(new BigDecimal("100.00"));
			bd4thPct = (arrReportTotals.get(AGING_CATEGORY_FOURTH).divide(bdTotalPayables, 2, BigDecimal.ROUND_HALF_UP)).multiply(new BigDecimal("100.00"));
			bdOverduePct = (bdTotalOverdue.divide(bdTotalPayables, 2, BigDecimal.ROUND_HALF_UP)).multiply(new BigDecimal("100.00"));
			bdTotalPct = (bdTotalPayables.divide(bdTotalPayables, 2, BigDecimal.ROUND_HALF_UP)).multiply(new BigDecimal("100.00"));
		}
		
		s += "  <TR class = \"" + SMMasterStyleSheetDefinitions.TABLE_ROW_BACKGROUNDCOLOR_WHITE + " \" >\n";
		
		if (bPrintInSummary){
			s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_FIELDCONTROL_RIGHT_JUSTIFIED + " \" COLSPAN=2 >"
				+ "<B>As Percentages:</B>"
				+ "</TD>\n"
			;
		}else{
			s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_FIELDCONTROL_RIGHT_JUSTIFIED + " \" COLSPAN=" + Integer.toString(m_iNumberOfColumns - 7) + " >"
				+ "<B>As Percentages:</B>"
				+ "</TD>\n"
			;	
		}
			
		s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + " \" >"
			+  "<B>" + clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(bdCurrentPct) + "%</B>"
			+ "</TD>\n"

			+ "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + " \" >"
			+  "<B>" + clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(bd1stPct) + "%</B>"
			+ "</TD>\n"
			
			+ "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + " \" >"
			+  "<B>" + clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(bd2ndPct) + "%</B>"
			+ "</TD>\n"

			+ "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + " \" >"
			+  "<B>" + clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(bd3rdPct) + "%</B>"
			+ "</TD>\n"

			+ "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + " \" >"
			+  "<B>" + clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(bd4thPct) + "%</B>"
			+ "</TD>\n"

			+ "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + " \" >"
			+  "<B>" 
			+ clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(bdOverduePct) + "%</B>"
			+ "</TD>\n"
			
			+ "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + " \" >"
			+  "<B>" 
			+ clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(bdTotalPct) + "%</B>"
			+ "</TD>\n"
			
			+ "  </TR>\n"
		;
		return s;
	}
	
	private String printColumnHeadings(boolean bIncludeAppliedDetails, boolean bPrintInSummary){
		String s = "";

		s += "  <TR class = \"" + SMMasterStyleSheetDefinitions.TABLE_ROW_BACKGROUNDCOLOR_WHITE + " \" >\n";
		
		if (!bPrintInSummary){
			s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_LEFT_JUSTIFIED + " \" >"
				+  "Doc Date"
				+ "</TD>"
				
				+ "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_LEFT_JUSTIFIED + " \" >"
				+  "Type"
				+ "</TD>"
				
				+"    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_LEFT_JUSTIFIED + " \" >"
				+  "Doc #"
				+ "</TD>"
	
				+"    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_LEFT_JUSTIFIED + " \" >"
				+  "Due Date"
				+ "</TD>"
	
				+"    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_LEFT_JUSTIFIED + " \" >"
				+  "On Hold?"
				+ "</TD>"
			;	
		}else{
			s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_LEFT_JUSTIFIED + " \" >"
				+  "Vendor"
				+ "</TD>"
				
				+ "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_LEFT_JUSTIFIED + " \" >"
				+  "Name"
				+ "</TD>"
			;	
		}
		s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_RIGHT_JUSTIFIED + " \" >"
			+  "Current"
			+ "</TD>"

			+"    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_RIGHT_JUSTIFIED + " \" >"
			+  "1 to " + Integer.toString(m_i1stAgingCategoryNumberOfDays)
			+ "</TD>"
			
			+"    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_RIGHT_JUSTIFIED + " \" >"
			+  Integer.toString(m_i1stAgingCategoryNumberOfDays + 1) + " to " + Integer.toString(m_i2ndAgingCategoryNumberOfDays)
			+ "</TD>"

			+"    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_RIGHT_JUSTIFIED + " \" >"
			+  Integer.toString(m_i2ndAgingCategoryNumberOfDays + 1) + " to " + Integer.toString(m_i3rdAgingCategoryNumberOfDays)
			+ "</TD>"

			+"    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_RIGHT_JUSTIFIED + " \" >"
			+  "Over " + Integer.toString(m_i3rdAgingCategoryNumberOfDays)
			+ "</TD>"
			
			+"    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_RIGHT_JUSTIFIED + " \" >"
			+  "Total<BR>Overdue"
			+ "</TD>"

			+"    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_RIGHT_JUSTIFIED + " \" >"
			+  "Total<BR>Payable"
			+ "</TD>"

		;
		
		s += "  </TR>\n";
		
		if (bIncludeAppliedDetails && !bPrintInSummary){
			String sIndent = "&nbsp;&nbsp;";
			s += "  <TR class = \"" + SMMasterStyleSheetDefinitions.TABLE_ROW_BACKGROUNDCOLOR_LIGHTGREY + " \" >\n";
			s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_LEFT_JUSTIFIED + " \" >"
				+  sIndent + "Apply Date"
				+ "</TD>"
				
				+ "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_LEFT_JUSTIFIED + " \" >"
				+  "Type"
				+ "</TD>"
				
				+"    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_LEFT_JUSTIFIED + " \" >"
				+  "Doc #"
				+ "</TD>"
				
				+"    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_LEFT_JUSTIFIED + " \" >"
				+  ""
				+ "</TD>"

				+"    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_LEFT_JUSTIFIED + " \" >"
				+  ""
				+ "</TD>"

				+"    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_RIGHT_JUSTIFIED + " \" >"
				+  "Applied Amt"
				+ "</TD>"

				+"    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_RIGHT_JUSTIFIED + " \" >"
				+  ""
				+ "</TD>"
				
				+"    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_RIGHT_JUSTIFIED + " \" >"
				+  ""
				+ "</TD>"

				+"    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_RIGHT_JUSTIFIED + " \" >"
				+  ""
				+ "</TD>"

				+"    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_RIGHT_JUSTIFIED + " \" >"
				+  ""
				+ "</TD>"
				
				+"    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_RIGHT_JUSTIFIED + " \" >"
				+  ""
				+ "</TD>"

				+"    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_RIGHT_JUSTIFIED + " \" >"
				+  "Net<BR>Applied"
				+ "</TD>"

			;
			s += "  </TR>\n";
		}
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

	private class clsApplyingTransaction extends Object{
		private String m_sdatapplyingdate;
		private BigDecimal m_bdappliedamt;
		private String m_sdocnumber;
		private int m_idoctype;
		private long m_ltransactionid;
		
		private clsApplyingTransaction(
			String sApplyingDate,
			BigDecimal bdApplyingAmt,
			String sDocNumber,
			int iDocType,
			long lTransactionID
		){
			m_sdatapplyingdate = sApplyingDate;
			m_bdappliedamt = bdApplyingAmt;
			m_sdocnumber = sDocNumber;
			m_idoctype = iDocType;
			m_ltransactionid = lTransactionID;
		}
		
		private String printApplyingLine(boolean bIncludeLinkToTransaction, String sDBID, ServletContext context){
			String s = "";
			//System.out.println("[1510426475]");
			String sIndent = "&nbsp;&nbsp;";
			
			String sDocNumber = m_sdocnumber;
			if (bIncludeLinkToTransaction){
				sDocNumber =
					"<A HREF=\"" + SMUtilities.getURLLinkBase(context) + "smap.APViewTransactionInformation?"
					+ SMTableaptransactions.lid + "=" + m_ltransactionid
					+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
					+ "\">" + sDocNumber + "</A>"
				;
			}
			
			s += "  <TR class = \"" + SMMasterStyleSheetDefinitions.TABLE_ROW_BACKGROUNDCOLOR_LIGHTGREY + " \" >\n";
			s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + " \" >"
				+  sIndent + "<I>" + m_sdatapplyingdate + "</I>"
				+ "</TD>\n"
				+ "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + " \" >"
				+ "<I>" + getDocType(m_idoctype) + "</I>"
				+ "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + " \" >"
				+ "<I>" + sDocNumber + "</I>"
				+ "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + " \" >"
				+  "&nbsp;"
				+ "</TD>\n"
				+ "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + " \" >"
				+  "&nbsp;"
				+ "</TD>\n"
				
				//Print the aging columns:
				+ "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + " \" >"
				+ "<I>" + clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(m_bdappliedamt.negate()) + "</I>"  //The applying lines sign matches the apply-to, but the report needs to show the opposite
				+ "</TD>\n"
				
				+ "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + " \" >"
				+  "&nbsp;"
				+ "</TD>\n"
				
				+ "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + " \" >"
				+  "&nbsp;"
				+ "</TD>\n"
				
				+ "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + " \" >"
				+  "&nbsp;"
				+ "</TD>\n"
				
				+ "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + " \" >"
				+  "&nbsp;"
				+ "</TD>\n"
				
				+ "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + " \" >"
				+  "&nbsp;"
				+ "</TD>\n"
				
				+ "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + " \" >"
				+ "<I>" + clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(m_bdappliedamt.negate()) + "</I>" //The applying lines sign matches the apply-to, but the report needs to show the opposite
				+ "</TD>\n"
				
				+ "  </TR>\n"
			;
			
			//System.out.println("[1510426476] 'SMUtilities.BigDecimalTo2DecimalSTDFormat(m_bdappliedamt.negate()) = '" + SMUtilities.BigDecimalTo2DecimalSTDFormat(m_bdappliedamt.negate()));
			
			return s;
		}
	}
	
	private class clsTransaction extends Object{
		private String m_sdatdocdate;
		private String m_sdatduedate;
		private BigDecimal m_bdamt;
		private String m_sdocnumber;
		private int m_idoctype;
		private int m_inumberofdayspastdue;
		private long m_ltransactionid;
		private boolean m_bOnHold;
		private ArrayList<clsApplyingTransaction>arrApplyingTransactionsForEachTransaction;
		private ArrayList<BigDecimal>arrAgingValuesForEachTransaction;
		
		private clsTransaction(
			String sDocDate,
			String sDueDate,
			BigDecimal bdTransactionAmt,
			String sDocNumber,
			int iDocType,
			int iNumberOfDaysPastDue,
			long lTransactionID,
			boolean bOnHold
		){
			m_sdatdocdate = sDocDate;
			m_sdatduedate = sDueDate;
			m_bdamt = bdTransactionAmt;
			m_sdocnumber = sDocNumber;
			m_idoctype = iDocType;
			m_inumberofdayspastdue = iNumberOfDaysPastDue;
			m_ltransactionid = lTransactionID;
			m_bOnHold = bOnHold;
			
			arrApplyingTransactionsForEachTransaction = new ArrayList<clsApplyingTransaction>(0);
			arrAgingValuesForEachTransaction = new ArrayList<BigDecimal>(0);
			
			for (int iAgingCategory = AGING_CATEGORY_CURRENT; iAgingCategory <= AGING_CATEGORY_FOURTH; iAgingCategory++){
				arrAgingValuesForEachTransaction.add(new BigDecimal("0.00"));
			}
			
			if (m_inumberofdayspastdue <= 0){
				arrAgingValuesForEachTransaction.set(AGING_CATEGORY_CURRENT, m_bdamt);
			}
			
			if ((m_inumberofdayspastdue > 0)&& (m_inumberofdayspastdue <= m_i1stAgingCategoryNumberOfDays)){
				arrAgingValuesForEachTransaction.set(AGING_CATEGORY_FIRST, m_bdamt);
			}
			
			if ((m_inumberofdayspastdue > m_i1stAgingCategoryNumberOfDays) && (m_inumberofdayspastdue <= m_i2ndAgingCategoryNumberOfDays)){
				arrAgingValuesForEachTransaction.set(AGING_CATEGORY_SECOND, m_bdamt);
			}
			
			if ((m_inumberofdayspastdue > m_i2ndAgingCategoryNumberOfDays) && (m_inumberofdayspastdue <= m_i3rdAgingCategoryNumberOfDays)){
				arrAgingValuesForEachTransaction.set(AGING_CATEGORY_THIRD, m_bdamt);
			}
			
			if (m_inumberofdayspastdue > m_i3rdAgingCategoryNumberOfDays){
				arrAgingValuesForEachTransaction.set(AGING_CATEGORY_FOURTH, m_bdamt);
			}
		}
		private String getDocDate(){
			return m_sdatdocdate;
		}
		private String getDueDate(){
			return m_sdatduedate;
		}
		private String getOnHoldFlag(){
			if (m_bOnHold){
				return "<FONT COLOR=RED><B>Y</<B></FONT>";
			}else{
				return "&nbsp;";
			}
		}
		private String getDocNumber(){
			return m_sdocnumber;
		}
		private String getAmtByAgingCategory(int iAgingCategory){
			if (arrAgingValuesForEachTransaction.get(iAgingCategory).compareTo(BigDecimal.ZERO) == 0){
				return "";
			}else{
				return clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(arrAgingValuesForEachTransaction.get(iAgingCategory));
			}
		}
		private String getTotalOverDueAmt(){
			return clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat( arrAgingValuesForEachTransaction.get(AGING_CATEGORY_FIRST).add(
				arrAgingValuesForEachTransaction.get(AGING_CATEGORY_SECOND)).add(
				arrAgingValuesForEachTransaction.get(AGING_CATEGORY_THIRD)).add(
				arrAgingValuesForEachTransaction.get(AGING_CATEGORY_FOURTH)));
		}
		private String getTotalAmt(){
			return clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat( arrAgingValuesForEachTransaction.get(AGING_CATEGORY_CURRENT).add(
				arrAgingValuesForEachTransaction.get(AGING_CATEGORY_FIRST)).add(
				arrAgingValuesForEachTransaction.get(AGING_CATEGORY_SECOND)).add(
				arrAgingValuesForEachTransaction.get(AGING_CATEGORY_THIRD)).add(
				arrAgingValuesForEachTransaction.get(AGING_CATEGORY_FOURTH)));
		}
		private BigDecimal getAmountByAgingCategory(int iAgingCategory){
			return arrAgingValuesForEachTransaction.get(iAgingCategory);
		}
		
		private void addApplyingTransactionForDocument(
			String sApplyingDate,
			BigDecimal bdApplyingAmt,
			String sDocNumber,
			int iDocType,
			long lTransactionID
			){

			//System.out.println("[1510426484] - sApplyingDate = '" + sApplyingDate + "'"
			//	+ ", bdApplyingAmt = " + bdApplyingAmt
			//	+ ", sDocNumber = '" + sDocNumber + "'"
			//	+ ", iDocType = " + iDocType
			//	+ ", lTransactionID = " + lTransactionID
			//);
			clsApplyingTransaction applyingtrans = new clsApplyingTransaction(
				sApplyingDate,
				bdApplyingAmt,
				sDocNumber,
				iDocType,
				lTransactionID
			);
			arrApplyingTransactionsForEachTransaction.add(applyingtrans);
			
			//TJR - 6/22/2017 - this function ONLY runs if the user has chosen to show 'applied details', and in that case
			//we want to show the FULL original amt of the transaction, not the current amt.  So we don't need this:
			//arrAgingValuesForEachTransaction.set(AGING_CATEGORY_CURRENT, arrAgingValuesForEachTransaction.get(AGING_CATEGORY_CURRENT).subtract(bdApplyingAmt));
		}
		
		private String printTransactionLine(boolean bIncludeLinkToTransactionInformation, String sDBID, ServletContext context){
			
			String s = "";
			
			String sDocNumber = getDocNumber();
			if (bIncludeLinkToTransactionInformation){
				sDocNumber =
					"<A HREF=\"" + SMUtilities.getURLLinkBase(context) + "smap.APViewTransactionInformation?"
					+ SMTableaptransactions.lid + "=" + m_ltransactionid
					+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
					+ "\">" + sDocNumber + "</A>"
				;
			}
			
			s += "  <TR class = \"" + SMMasterStyleSheetDefinitions.TABLE_ROW_BACKGROUNDCOLOR_WHITE + " \" >\n";
			s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + " \" >"
				+  getDocDate()
				+ "</TD>\n"
				+ "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + " \" >"
				+  getDocType(m_idoctype)
				+ "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + " \" >"
				+  sDocNumber
				+ "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + " \" >"
				+  getDueDate()
				+ "</TD>\n"
				+ "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + " \" >"
				+  getOnHoldFlag()
				+ "</TD>\n"
				
				//Print the aging columns:
				+ "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + " \" >"
				+  getAmtByAgingCategory(AGING_CATEGORY_CURRENT)
				+ "</TD>\n"
				
				+ "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + " \" >"
				+  getAmtByAgingCategory(AGING_CATEGORY_FIRST)
				+ "</TD>\n"
				
				+ "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + " \" >"
				+  getAmtByAgingCategory(AGING_CATEGORY_SECOND)
				+ "</TD>\n"
				
				+ "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + " \" >"
				+  getAmtByAgingCategory(AGING_CATEGORY_THIRD)
				+ "</TD>\n"
				
				+ "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + " \" >"
				+  getAmtByAgingCategory(AGING_CATEGORY_FOURTH)
				+ "</TD>\n"
				
				+ "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + " \" >"
				+  getTotalOverDueAmt()
				+ "</TD>\n"
				
				+ "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + " \" >"
				+  getTotalAmt()
				+ "</TD>\n"
				
				//+ "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + " \" >"
				//+  getNumberOfdaysPastDue()
				//+ "</TD>\n"
				
				+ "  </TR>\n"
			;
			
			//System.out.println("[1510426474] arrApplyingTransactionsForEachTransaction.size() = " + arrApplyingTransactionsForEachTransaction.size());
			for (int iApplyingTransactionCounter = 0; iApplyingTransactionCounter < arrApplyingTransactionsForEachTransaction.size(); iApplyingTransactionCounter++){
				//System.out.println("[1510426477]");
				s += arrApplyingTransactionsForEachTransaction.get(iApplyingTransactionCounter).printApplyingLine(bIncludeLinkToTransactionInformation, sDBID, context);
			}
			
			return s;
		}
	}
	
	private class clsVendorSection extends Object{
		private String m_svendoracct;
		private String m_svendorname;
		private ArrayList<clsTransaction>arrTransactions;
		
		private clsVendorSection(
			String sVendorAcct,
			String sVendorName
		){
			m_svendoracct = sVendorAcct;
			m_svendorname = sVendorName;
			arrTransactions = new ArrayList<clsTransaction>(0);
		}
		
		private void addTransaction(clsTransaction trans){
			arrTransactions.add(trans);
		}
		public String getVendor(){
			return m_svendoracct;
		}
		
		private String printVendorSection(
			boolean bPrintVendorsWithAZeroBalance, 
			boolean bIncludeLinkToTransactionInformation,
			boolean bIncludeLinkToVendorInformation,
			String sDBID, 
			ServletContext context,
			boolean bPrintInSummary
			){
			String s = "";
			
			BigDecimal bdVendorTotal = new BigDecimal("0.00");
			for (int iTransactionCounter = 0; iTransactionCounter < arrTransactions.size(); iTransactionCounter++){
				bdVendorTotal = bdVendorTotal.add(arrTransactions.get(iTransactionCounter).m_bdamt);
			}
			
			if (!bPrintVendorsWithAZeroBalance && bdVendorTotal.compareTo(BigDecimal.ZERO) == 0){
				//Don't print the vendor
			}else{
				if (!bPrintInSummary){
					s += printVendorHeading(bIncludeLinkToVendorInformation, context, sDBID);
					
					s += printVendorLines(bIncludeLinkToTransactionInformation, sDBID, context);
				}
				s += printVendorFooting(bPrintInSummary);
			}
			return s;
		}
		private String printVendorHeading(boolean bIncludeLinkToVendorInformation, ServletContext context, String sDBID){
			String s = "";
			
			//Print a link to the 'Display vendor info' screen, if the user has the permission:
			String sVendorLink = m_svendoracct;
			if (bIncludeLinkToVendorInformation){
        		sVendorLink = "<A HREF=\"" + SMUtilities.getURLLinkBase(context) + "smap." 
    	    		+ "APDisplayVendorInformation" 
    	    		+ "?" + "VendorNumber" + "=" + m_svendoracct
    	    		+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
    	    		+ "\">"
    	    		+ m_svendoracct
    	    		+ "</A>";
			}
			
			s += "  <TR class = \"" + SMMasterStyleSheetDefinitions.TABLE_ROW_BACKGROUNDCOLOR_LIGHTBLUE + " \" >\n";
			s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_FIELDCONTROL_LEFT_JUSTIFIED + " \""
				+ " COLSPAN = " + Integer.toString(m_iNumberOfColumns) + ">"
				+ "<B>Vendor account:</B>&nbsp;" + sVendorLink
				+ "&nbsp;&nbsp;&nbsp;<B>Vendor name:</B>&nbsp;" + m_svendorname
				+ "</TD>\n"
				+ "  </TR>\n"
			;
			
			return s;
		}
		
		public BigDecimal bdGetVendorTotalsByAgingCategory(int iAgingCategory){
			BigDecimal bdTotal = new BigDecimal("0.00");
			for (int iTransactionCounter = 0; iTransactionCounter < arrTransactions.size(); iTransactionCounter++){
				bdTotal = bdTotal.add(arrTransactions.get(iTransactionCounter).getAmountByAgingCategory(iAgingCategory));
				//The applying transactions don't get 'aged', so they are all considered 'current'.
				//So if we are calculating CURRENT values, we'll also include the APPLYING transactions to the total:
				if (iAgingCategory == AGING_CATEGORY_CURRENT){
					for (int iApplyingTransactionCounter = 0; iApplyingTransactionCounter < arrTransactions.get(iTransactionCounter).arrApplyingTransactionsForEachTransaction.size(); iApplyingTransactionCounter++){
						bdTotal = bdTotal.subtract(arrTransactions.get(iTransactionCounter).arrApplyingTransactionsForEachTransaction.get(iApplyingTransactionCounter).m_bdappliedamt);
					}
				}
			}
			return bdTotal;
		}
		
		private String printVendorLines(boolean bIncludeLinkToTransactionInformation, String sDBID, ServletContext context){
			String s = "";
			
			for (int iTransactionCounter = 0; iTransactionCounter < arrTransactions.size(); iTransactionCounter++){
				s += arrTransactions.get(iTransactionCounter).printTransactionLine(bIncludeLinkToTransactionInformation, sDBID, context);
			}
			
			return s;
		}
		
		private String getTotalsByAgingCategoryForVendor(int iAgingCategory){
			return clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(bdGetVendorTotalsByAgingCategory(iAgingCategory));
		}
		
		private String getTotalOverdueForVendor(){
			
			BigDecimal bdTotalOverdue = new BigDecimal("0.00");
			for (int iAgingCategory = AGING_CATEGORY_FIRST; iAgingCategory <= AGING_CATEGORY_FOURTH; iAgingCategory++){
				bdTotalOverdue = bdTotalOverdue.add(bdGetVendorTotalsByAgingCategory(iAgingCategory));
			}
			
			return clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(bdTotalOverdue);
		}
		
		private String getTotalForVendor(){
			
			BigDecimal bdTotalForVendor = new BigDecimal("0.00");
			for (int iAgingCategory = AGING_CATEGORY_CURRENT; iAgingCategory <= AGING_CATEGORY_FOURTH; iAgingCategory++){
				bdTotalForVendor = bdTotalForVendor.add(bdGetVendorTotalsByAgingCategory(iAgingCategory));
				//Add to the report totals:
				arrReportTotals.set(iAgingCategory, arrReportTotals.get(iAgingCategory).add(bdGetVendorTotalsByAgingCategory(iAgingCategory)));
				
			}
			
			return clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(bdTotalForVendor);
		}
		
		private void addApplyingTransactionForVendor(
				String sApplyToDocNumber,
				String sApplyingDate,
				BigDecimal bdAppliedAmt,
				String sDocNumber,
				int iDocType,
				long lTransactionID
			){
			//System.out.println("[1510426482] arrTransactions.size() = " + arrTransactions.size());
			for (int iTransactionIndex = 0; iTransactionIndex < arrTransactions.size(); iTransactionIndex++){
				//System.out.println("[1510426483] arrTransactions.get(iTransactionIndex).getDocNumber() = '" + arrTransactions.get(iTransactionIndex).getDocNumber() + "'"
				//	+ ", sApplyToDocNumber = '" + sApplyToDocNumber + "'"
				//);
				if (arrTransactions.get(iTransactionIndex).getDocNumber().compareToIgnoreCase(sApplyToDocNumber) == 0){
					arrTransactions.get(iTransactionIndex).addApplyingTransactionForDocument(sApplyingDate, bdAppliedAmt, sDocNumber, iDocType, lTransactionID);
				}
			}
		}
		
		private String printVendorFooting(boolean bPrintInSummary){
			String s = "";
			
			s += "  <TR class = \"" + SMMasterStyleSheetDefinitions.TABLE_ROW_BACKGROUNDCOLOR_LIGHTBLUE + " \" >\n";
				
			;
			if (!bPrintInSummary){
				s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_FIELDCONTROL_RIGHT_JUSTIFIED + " \" COLSPAN = " + Integer.toString(m_iNumberOfColumns - 7) + " >"
					+ "<B>Totals for " + m_svendoracct + " - " + m_svendorname + ":</B>"
					+ "</TD>\n"

					+ "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + " \" >"
					+  "<B>" + getTotalsByAgingCategoryForVendor(AGING_CATEGORY_CURRENT) + "</B>"
					+ "</TD>\n"

					+ "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + " \" >"
					+  "<B>" + getTotalsByAgingCategoryForVendor(AGING_CATEGORY_FIRST) + "</B>"
					+ "</TD>\n"
					
					+ "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + " \" >"
					+  "<B>" + getTotalsByAgingCategoryForVendor(AGING_CATEGORY_SECOND) + "</B>"
					+ "</TD>\n"

					+ "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + " \" >"
					+  "<B>" + getTotalsByAgingCategoryForVendor(AGING_CATEGORY_THIRD) + "</B>"
					+ "</TD>\n"

					+ "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + " \" >"
					+  "<B>" + getTotalsByAgingCategoryForVendor(AGING_CATEGORY_FOURTH) + "</B>"
					+ "</TD>\n"

					+ "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + " \" >"
					+  "<B>" + getTotalOverdueForVendor() + "</B>"
					+ "</TD>\n"
					
					+ "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + " \" >"
					+  "<B>" + getTotalForVendor() + "</B>"
					+ "</TD>\n"
				;
				s += "  </TR>\n";
				
				s += "  <TR class = \"" + SMMasterStyleSheetDefinitions.TABLE_ROW_BACKGROUNDCOLOR_BLACK + " \" >\n"
					+ "    <TD COLSPAN = " + Integer.toString(m_iNumberOfColumns) + ">&nbsp;</TD>\n"
					+ "  </TR>\n"
				;
			}else{
				s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_FIELDCONTROL_LEFT_JUSTIFIED + " \" >" //COLSPAN = " + Integer.toString(m_iNumberOfColumns - 7)
					+ "<B>" + m_svendoracct + "</B>"
					+ "</TD>\n"
					
					+ "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_FIELDCONTROL_LEFT_JUSTIFIED + " \" >" //COLSPAN = " + Integer.toString(m_iNumberOfColumns - 7)
					+ "<B>" + m_svendorname + "</B>"
					+ "</TD>\n"

					+ "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + " \" >"
					+  "<B>" + getTotalsByAgingCategoryForVendor(AGING_CATEGORY_CURRENT) + "</B>"
					+ "</TD>\n"

					+ "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + " \" >"
					+  "<B>" + getTotalsByAgingCategoryForVendor(AGING_CATEGORY_FIRST) + "</B>"
					+ "</TD>\n"
					
					+ "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + " \" >"
					+  "<B>" + getTotalsByAgingCategoryForVendor(AGING_CATEGORY_SECOND) + "</B>"
					+ "</TD>\n"

					+ "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + " \" >"
					+  "<B>" + getTotalsByAgingCategoryForVendor(AGING_CATEGORY_THIRD) + "</B>"
					+ "</TD>\n"

					+ "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + " \" >"
					+  "<B>" + getTotalsByAgingCategoryForVendor(AGING_CATEGORY_FOURTH) + "</B>"
					+ "</TD>\n"

					+ "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + " \" >"
					+  "<B>" + getTotalOverdueForVendor() + "</B>"
					+ "</TD>\n"
					
					+ "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_RIGHT_JUSTIFIED_ARIAL_SMALL_WO_BORDER + " \" >"
					+  "<B>" + getTotalForVendor() + "</B>"
					+ "</TD>\n"
				;
				s += "  </TR>\n";
			}
			
			s += "  </TR>\n";
			
			return s;
		}
		
	}
	
	private String getDocType(int iDocType){
		switch(iDocType){
		case SMTableaptransactions.AP_TRANSACTION_TYPE_INVOICE_CREDITNOTE:
			return "CN";
		case SMTableaptransactions.AP_TRANSACTION_TYPE_INVOICE_DEBITNOTE:
			return "DN";
		case SMTableaptransactions.AP_TRANSACTION_TYPE_INVOICE_INVOICE:
			return "IN";
		case SMTableaptransactions.AP_TRANSACTION_TYPE_PAYMENT_APPLYTO:
			return "AT";
		case SMTableaptransactions.AP_TRANSACTION_TYPE_PAYMENT_MISCPAYMENT:
			return "MI";
		case SMTableaptransactions.AP_TRANSACTION_TYPE_PAYMENT_PAYMENT:
			return "PY";
		case SMTableaptransactions.AP_TRANSACTION_TYPE_PAYMENT_PREPAYMENT:
			return "PP";
		case SMTableaptransactions.AP_TRANSACTION_TYPE_REVERSAL:
			return "RV";
		case EARNED_DISCOUNT_TYPE:
			return "ED";
		default:
			return "NA";
		}
	}
	
	//Print the legends:
	private String printDocTypeLegends(){
		String s = "";
		
		s += "<BR><TABLE BORDER=0>\n";
		s += "  <TR>\n";
		for (int i = SMTableaptransactions.AP_TRANSACTION_TYPE_INVOICE_INVOICE;i <= 7; i++){
			if (getDocType(i).compareToIgnoreCase("NA") != 0){
				s += "    <TD><FONT SIZE=2><I>" + SMTableapbatchentries.getDocumentTypeLabel(i) + " = " + getDocType(i) + "</I></FONT></TD>\n";
			}
		}
		s += "  </TR>\n";
		s += "</TABLE>\n";
		
		return s;
	}


}
