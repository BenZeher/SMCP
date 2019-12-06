package smgl;

import java.io.PrintWriter;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import javax.servlet.ServletContext;

import SMDataDefinition.SMTableglaccounts;
import SMDataDefinition.SMTableglfinancialstatementdata;
import SMDataDefinition.SMTableglfiscalperiods;
import SMDataDefinition.SMTableglfiscalsets;
import SMDataDefinition.SMTablegltransactionlines;
import ServletUtilities.clsDatabaseFunctions;
import smcontrolpanel.SMUtilities;

public class GLFinancialDataCheck extends java.lang.Object{
	
	
	public GLFinancialDataCheck(
        ) {
	}

	public String processFinancialRecords(
		String sAccount,
		String sStartingFiscalYear,
		Connection conn,
		boolean bUpdateRecords

		) throws Exception{
		
		ArrayList<clsFiscalSet>arrFiscalSets = new ArrayList<clsFiscalSet>(0);
		String sErrorMessages = "";
		String sStatusMessages = "";
		
		//Get an array list of the selected fiscal sets:
		String SQL = "SELECT"
			+ " " + SMTableglfiscalsets.TableName + ".*"
			+ ", " + SMTableglfiscalperiods.TableName + "." + SMTableglfiscalperiods.inumberofperiods
			+ " FROM " + SMTableglfiscalsets.TableName
			+ " LEFT JOIN " + SMTableglfiscalperiods.TableName
			+ " ON " + SMTableglfiscalsets.TableName + "." + SMTableglfiscalsets.ifiscalyear
			+ " = " + SMTableglfiscalperiods.TableName + "." + SMTableglfiscalperiods.ifiscalyear
			+ " WHERE ("
			+ " (1 = 1)";
		
		if (sAccount.compareToIgnoreCase("") != 0){
			SQL += " AND (" + SMTableglfiscalsets.TableName + "." + SMTableglfiscalsets.sAcctID + " = '" + sAccount + "')";
		}
		
		SQL += " AND (" + SMTableglfiscalsets.TableName + "." + SMTableglfiscalsets.ifiscalyear + " >= (" + sStartingFiscalYear + " - 2))";
		
		SQL += ")"
			+ " ORDER BY " + SMTableglfiscalsets.TableName + "." + SMTableglfiscalsets.sAcctID + ", " 
				+ SMTableglfiscalsets.TableName + "." + SMTableglfiscalsets.ifiscalyear
		;
		
		try {
			ResultSet rsFiscalYears = clsDatabaseFunctions.openResultSet(SQL, conn);
			while (rsFiscalYears.next()){
				clsFiscalSet objFiscal = new clsFiscalSet(
					rsFiscalYears.getString(SMTableglfiscalsets.TableName + "." + SMTableglfiscalsets.sAcctID),
					rsFiscalYears.getInt(SMTableglfiscalsets.TableName + "." + SMTableglfiscalsets.ifiscalyear),
					rsFiscalYears.getBigDecimal(SMTableglfiscalsets.TableName + "." + SMTableglfiscalsets.bdopeningbalance),
					rsFiscalYears.getBigDecimal(SMTableglfiscalsets.TableName + "." + SMTableglfiscalsets.bdnetchangeperiod1),
					rsFiscalYears.getBigDecimal(SMTableglfiscalsets.TableName + "." + SMTableglfiscalsets.bdnetchangeperiod2),
					rsFiscalYears.getBigDecimal(SMTableglfiscalsets.TableName + "." + SMTableglfiscalsets.bdnetchangeperiod3),
					rsFiscalYears.getBigDecimal(SMTableglfiscalsets.TableName + "." + SMTableglfiscalsets.bdnetchangeperiod4),
					rsFiscalYears.getBigDecimal(SMTableglfiscalsets.TableName + "." + SMTableglfiscalsets.bdnetchangeperiod5),
					rsFiscalYears.getBigDecimal(SMTableglfiscalsets.TableName + "." + SMTableglfiscalsets.bdnetchangeperiod6),
					rsFiscalYears.getBigDecimal(SMTableglfiscalsets.TableName + "." + SMTableglfiscalsets.bdnetchangeperiod7),
					rsFiscalYears.getBigDecimal(SMTableglfiscalsets.TableName + "." + SMTableglfiscalsets.bdnetchangeperiod8),
					rsFiscalYears.getBigDecimal(SMTableglfiscalsets.TableName + "." + SMTableglfiscalsets.bdnetchangeperiod9),
					rsFiscalYears.getBigDecimal(SMTableglfiscalsets.TableName + "." + SMTableglfiscalsets.bdnetchangeperiod10),
					rsFiscalYears.getBigDecimal(SMTableglfiscalsets.TableName + "." + SMTableglfiscalsets.bdnetchangeperiod11),
					rsFiscalYears.getBigDecimal(SMTableglfiscalsets.TableName + "." + SMTableglfiscalsets.bdnetchangeperiod12),
					rsFiscalYears.getBigDecimal(SMTableglfiscalsets.TableName + "." + SMTableglfiscalsets.bdnetchangeperiod13),
					rsFiscalYears.getBigDecimal(SMTableglfiscalsets.TableName + "." + SMTableglfiscalsets.bdnetchangeperiod14),
					rsFiscalYears.getBigDecimal(SMTableglfiscalsets.TableName + "." + SMTableglfiscalsets.bdnetchangeperiod15),
					rsFiscalYears.getInt(SMTableglfiscalperiods.TableName + "." + SMTableglfiscalperiods.inumberofperiods)
				);
				arrFiscalSets.add(objFiscal);
			}
			rsFiscalYears.close();
		} catch (Exception e) {
			throw new Exception("Error [2019287830270] " + "in rsFiscalYears loop with SQL: '" + SQL + "' - " + e.getMessage());
		}

		//If we are UPDATING records as well as checking, then first make sure we have financial statement records that correspond to ALL the
		//fiscal sets:
		if (bUpdateRecords){
			sStatusMessages += insertMissingFinancialRecords(arrFiscalSets, sStartingFiscalYear, conn);
		}
		
		//Read the financial records for each GL account that was read:
		ArrayList<String>arrListOfUniqueGLAccounts = getUniqueGLAccounts(arrFiscalSets);
		try {
			for (int i = 0; i < arrListOfUniqueGLAccounts.size(); i++){
				sErrorMessages += processSingleFiscalSet(arrListOfUniqueGLAccounts.get(i), conn, sStartingFiscalYear, arrFiscalSets, bUpdateRecords);
				sStatusMessages += "<BR><I>" + "Checking GL Account " + arrListOfUniqueGLAccounts.get(i) + "...</I>";
				//System.out.println("[2019289952298] " + "Finished checking GL Account " + arrListOfUniqueGLAccounts.get(i) + ".");
			}
		} catch (Exception e) {
			sErrorMessages += "<BR>" + e.getMessage();
		}
		
		if (sErrorMessages.compareToIgnoreCase("") != 0){
			throw new Exception(sErrorMessages);
		}
		return sStatusMessages += "<BR><BR><B>All selected financial statement data is in sync with the fiscal sets.</B><BR>";
	}

	public String checkTransactionsAgainstACCPACTransactions(
		String sAccount,
		String sStartingFiscalYear,
		Connection conn,
		Connection cnACCPAC,
		ServletContext context,
		String sDBID
		) throws Exception{
		
		String sResult = "";
		//Group the ACCPAC transactions by account:
		ArrayList<BigDecimal>arrACCPACLineSubtotals = new ArrayList<BigDecimal>(0);
		ArrayList<String>arrACCPACAcctIDs = new ArrayList<String>(0);
		ArrayList<Integer>arrACCPACFiscalYears = new ArrayList<Integer>(0);
		ArrayList<Integer>arrACCPACFiscalPeriods = new ArrayList<Integer>(0);
		
		ArrayList<BigDecimal>arrSMCPLineSubtotals = new ArrayList<BigDecimal>(0);
		ArrayList<String>arrSMCPAcctIDs = new ArrayList<String>(0);
		ArrayList<Integer>arrSMCPFiscalYears = new ArrayList<Integer>(0);
		ArrayList<Integer>arrSMCPFiscalPeriods = new ArrayList<Integer>(0);
		
		long lCounter = 0;
		
		//Load the ACCPAC array:
		String sACCPACSQL = "SELECT"
			+ " SUM(TRANSAMT) AS ACCTTOTAL"
			+ ", ACCTID"
			+ ", FISCALYR"
			+ ", FISCALPERD"
			+ " FROM GLPOST"
			+ " WHERE ("
				+ "(ACCTID = '" + sAccount + "')"
				+ " AND (FISCALYR >= " + sStartingFiscalYear + ")"
			+ ")"
			+ " GROUP BY ACCTID, FISCALYR, FISCALPERD"
		;
		try {
			Statement stmtACCPAC = cnACCPAC.createStatement();
			ResultSet rsACCPAC = stmtACCPAC.executeQuery(sACCPACSQL);
			
			while (rsACCPAC.next()){
				arrACCPACLineSubtotals.add(rsACCPAC.getBigDecimal("ACCTTOTAL"));
				arrSMCPAcctIDs.add(rsACCPAC.getString("ACCTID").trim());
				arrSMCPFiscalYears.add(rsACCPAC.getInt("FISCALYR"));
				arrACCPACFiscalPeriods.add(rsACCPAC.getInt("FISCALYR"));
				lCounter++;
			}
			rsACCPAC.close();
		} catch (Exception e) {
			throw new Exception("Error [20192941537413] " + "Error reading ACCPAC records with SQL: '" + sACCPACSQL + "' - " + e.getMessage() + ".");
		}
		
		sResult = arrACCPACLineSubtotals.size() + " records read, counter says: " + lCounter + "..., SQL = '" + sACCPACSQL + "'";
		
		return sResult;
	}
	
	public String checkFiscalSetsAgainstACCPACFiscalSets(
		String sAccount,
		String sStartingFiscalYear,
		Connection conn,
		Connection cnACCPAC,
		ServletContext context,
		String sDBID
		) throws Exception{
		
		ArrayList<clsFiscalSet>arrFiscalSets = new ArrayList<clsFiscalSet>(0);
		String sErrorMessages = "";
		String sStatusMessages = "";
		
		//Get an array list of the selected fiscal sets:
		String SQL = "SELECT"
			+ " " + SMTableglfiscalsets.TableName + ".*"
			+ ", " + SMTableglfiscalperiods.TableName + "." + SMTableglfiscalperiods.inumberofperiods
			+ " FROM " + SMTableglfiscalsets.TableName
			+ " LEFT JOIN " + SMTableglfiscalperiods.TableName
			+ " ON " + SMTableglfiscalsets.TableName + "." + SMTableglfiscalsets.ifiscalyear
			+ " = " + SMTableglfiscalperiods.TableName + "." + SMTableglfiscalperiods.ifiscalyear
			+ " WHERE ("
			+ " (1 = 1)";
		
		if (sAccount.compareToIgnoreCase("") != 0){
			SQL += " AND (" + SMTableglfiscalsets.TableName + "." + SMTableglfiscalsets.sAcctID + " = '" + sAccount + "')";
		}
		
		SQL += " AND (" + SMTableglfiscalsets.TableName + "." + SMTableglfiscalsets.ifiscalyear + " >= " + sStartingFiscalYear + ")";
		SQL += ")"
			+ " ORDER BY " + SMTableglfiscalsets.TableName + "." + SMTableglfiscalsets.sAcctID + ", " 
				+ SMTableglfiscalsets.TableName + "." + SMTableglfiscalsets.ifiscalyear
		;
		
		try {
			ResultSet rsFiscalYears = clsDatabaseFunctions.openResultSet(SQL, conn);
			while (rsFiscalYears.next()){
				clsFiscalSet objFiscal = new clsFiscalSet(
					rsFiscalYears.getString(SMTableglfiscalsets.TableName + "." + SMTableglfiscalsets.sAcctID),
					rsFiscalYears.getInt(SMTableglfiscalsets.TableName + "." + SMTableglfiscalsets.ifiscalyear),
					rsFiscalYears.getBigDecimal(SMTableglfiscalsets.TableName + "." + SMTableglfiscalsets.bdopeningbalance),
					rsFiscalYears.getBigDecimal(SMTableglfiscalsets.TableName + "." + SMTableglfiscalsets.bdnetchangeperiod1),
					rsFiscalYears.getBigDecimal(SMTableglfiscalsets.TableName + "." + SMTableglfiscalsets.bdnetchangeperiod2),
					rsFiscalYears.getBigDecimal(SMTableglfiscalsets.TableName + "." + SMTableglfiscalsets.bdnetchangeperiod3),
					rsFiscalYears.getBigDecimal(SMTableglfiscalsets.TableName + "." + SMTableglfiscalsets.bdnetchangeperiod4),
					rsFiscalYears.getBigDecimal(SMTableglfiscalsets.TableName + "." + SMTableglfiscalsets.bdnetchangeperiod5),
					rsFiscalYears.getBigDecimal(SMTableglfiscalsets.TableName + "." + SMTableglfiscalsets.bdnetchangeperiod6),
					rsFiscalYears.getBigDecimal(SMTableglfiscalsets.TableName + "." + SMTableglfiscalsets.bdnetchangeperiod7),
					rsFiscalYears.getBigDecimal(SMTableglfiscalsets.TableName + "." + SMTableglfiscalsets.bdnetchangeperiod8),
					rsFiscalYears.getBigDecimal(SMTableglfiscalsets.TableName + "." + SMTableglfiscalsets.bdnetchangeperiod9),
					rsFiscalYears.getBigDecimal(SMTableglfiscalsets.TableName + "." + SMTableglfiscalsets.bdnetchangeperiod10),
					rsFiscalYears.getBigDecimal(SMTableglfiscalsets.TableName + "." + SMTableglfiscalsets.bdnetchangeperiod11),
					rsFiscalYears.getBigDecimal(SMTableglfiscalsets.TableName + "." + SMTableglfiscalsets.bdnetchangeperiod12),
					rsFiscalYears.getBigDecimal(SMTableglfiscalsets.TableName + "." + SMTableglfiscalsets.bdnetchangeperiod13),
					rsFiscalYears.getBigDecimal(SMTableglfiscalsets.TableName + "." + SMTableglfiscalsets.bdnetchangeperiod14),
					rsFiscalYears.getBigDecimal(SMTableglfiscalsets.TableName + "." + SMTableglfiscalsets.bdnetchangeperiod15),
					rsFiscalYears.getInt(SMTableglfiscalperiods.TableName + "." + SMTableglfiscalperiods.inumberofperiods)
				);
				arrFiscalSets.add(objFiscal);
				
			}
			rsFiscalYears.close();
		} catch (Exception e) {
			throw new Exception("Error [2019287830270] " + "in rsFiscalYears loop with SQL: '" + SQL + "' - " + e.getMessage());
		}

		sStatusMessages += checkAgainstACCPACFiscalSets(arrFiscalSets, sStartingFiscalYear, sAccount, conn, cnACCPAC, context, sDBID);
		if (sErrorMessages.compareToIgnoreCase("") != 0){
			throw new Exception(sErrorMessages);
		}
		return sStatusMessages;
	}

	public String checkFiscalSetsAgainstTransactions(
			String sAccount,
			String sStartingFiscalYear,
			Connection conn,
			boolean bUpdate) throws Exception{
		
		String sResult = "";
		if (sAccount.compareToIgnoreCase("") != 0){
			sResult =  checkFiscalSetsAgainstTransactionsForSelectedAccount(
				sAccount,
				sStartingFiscalYear,
				conn,
				bUpdate
			);
		}else{
			//Get a list of all the GL accounts, and process each, one by one:
			String SQL = "SELECT"
				+ " " + SMTableglaccounts.sAcctID
				+ " FROM " + SMTableglaccounts.TableName
				+ " ORDER BY " + SMTableglaccounts.sAcctID
			;
			try {
				ResultSet rsGLAccounts = clsDatabaseFunctions.openResultSet(SQL, conn);
				while (rsGLAccounts.next()){
					sResult += checkFiscalSetsAgainstTransactionsForSelectedAccount(
						rsGLAccounts.getString(SMTableglaccounts.sAcctID),
						sStartingFiscalYear,
						conn,
						bUpdate
					);
				}
				rsGLAccounts.close();
			} catch (Exception e) {
				throw new Exception("Error [2019328134856] " + "Error getting list of GL accounts with SQL: '" + SQL + "' - " + e.getMessage());
			}
		}
		return sResult;
	}
	private String checkFiscalSetsAgainstTransactionsForSelectedAccount(
			String sAccount,
			String sStartingFiscalYear,
			Connection conn,
			boolean bUpdate
		) throws Exception{
		String sResult = "";
		
		//First, get the list of selected and subsequent fiscal years:
		String SQL = "SELECT"
			+ " " + SMTableglfiscalperiods.ifiscalyear
			+ ", " + SMTableglfiscalperiods.inumberofperiods
			+ " FROM " + SMTableglfiscalperiods.TableName 
			+ " WHERE ("
				+ "(" + SMTableglfiscalperiods.ifiscalyear + " >= " + sStartingFiscalYear + ")"
			+ ")"
			+ " ORDER BY " + SMTableglfiscalperiods.ifiscalyear
		;
		ArrayList<String>arrFiscalYears = new ArrayList<String>(0);
		ArrayList<Integer>arrNumberOfPeriodsInFiscalYear = new ArrayList<Integer>(0);
		try {
			ResultSet rsFiscalYears = clsDatabaseFunctions.openResultSet(SQL, conn);
			while (rsFiscalYears.next()){
				arrFiscalYears.add(Long.toString(rsFiscalYears.getLong(SMTableglfiscalperiods.ifiscalyear)));
				arrNumberOfPeriodsInFiscalYear.add(rsFiscalYears.getInt(SMTableglfiscalperiods.inumberofperiods));
			}
			rsFiscalYears.close();
		} catch (Exception e) {
			throw new Exception("Error [20193281139326] " + "Error reading current and subsequent fiscal years with SQL: '" + SQL + " - " + e.getMessage());
		}
		
		for (int intFiscalYearCounter = 0; intFiscalYearCounter < arrFiscalYears.size(); intFiscalYearCounter++){
			
			//First get all the fiscal set totals into arrays:
			ArrayList<BigDecimal>arrPeriodTotals = new ArrayList<BigDecimal>(0);
			for (int iPeriodCounter = 0; iPeriodCounter < arrNumberOfPeriodsInFiscalYear.get(intFiscalYearCounter); iPeriodCounter++){
				arrPeriodTotals.add(BigDecimal.ZERO);
			}
			
			//Now add one more for the 'closing' period (period 15):
			arrPeriodTotals.add(BigDecimal.ZERO);
			
			SQL = "SELECT"
				+ " SUM(" + SMTablegltransactionlines.bdamount + ") AS PERIODTOTAL"
				+ ", " + SMTablegltransactionlines.ifiscalperiod
				+ " FROM " + SMTablegltransactionlines.TableName
				+ " WHERE (" 
					+ "(" + SMTablegltransactionlines.sacctid + " = '" + sAccount + "')"
					+ " AND (" + SMTablegltransactionlines.ifiscalyear + " = " + arrFiscalYears.get(intFiscalYearCounter) + ")"
				+ ")"
				+ " GROUP BY " + SMTablegltransactionlines.ifiscalperiod
				+ " ORDER BY " + SMTablegltransactionlines.ifiscalperiod
			;
		
			//Get a total for each period into the totals array:
			//If there are no transactions for the period, then the total will be zero:
			try {
				ResultSet rsTransactionTotals = clsDatabaseFunctions.openResultSet(SQL, conn);
				while (rsTransactionTotals.next()){
					//If it's a 'closing' period,transaction, add it to the last transaction total:
					if (rsTransactionTotals.getInt(SMTablegltransactionlines.ifiscalperiod) == 15){
						arrPeriodTotals.set(arrPeriodTotals.size() - 1, rsTransactionTotals.getBigDecimal("PERIODTOTAL"));
					}else{
						arrPeriodTotals.set(rsTransactionTotals.getInt(SMTablegltransactionlines.ifiscalperiod) - 1, rsTransactionTotals.getBigDecimal("PERIODTOTAL"));
					}
				}
				rsTransactionTotals.close();
			} catch (Exception e) {
				throw new Exception("Error [20193281143599] " + "Error reading transaction lines to check against fiscal sets with SQL: '" + SQL + " - " + e.getMessage());
			}

			//Now check those totals against what we have for the fiscal sets:
			SQL = "SELECT"
				+ " * "
				+ " FROM " + SMTableglfiscalsets.TableName
				+ " WHERE ("
					+ "(" + SMTableglfiscalsets.sAcctID + " = '" + sAccount + "')"
					+ " AND (" + SMTableglfiscalsets.ifiscalyear + " = " + arrFiscalYears.get(intFiscalYearCounter) + ")"
				+ ")"
			;
			
			ResultSet rsFiscalSet = clsDatabaseFunctions.openResultSet(SQL, conn);
			while(rsFiscalSet.next()){
				//Match up all the periods:
				//We'll check every one, but not the last one, because that's period 15, which we check below here
				for (int iPeriodCounter = 1; iPeriodCounter < arrNumberOfPeriodsInFiscalYear.get(intFiscalYearCounter); iPeriodCounter++){
					//Determine which field to read:
					String sNetChangeField = SMTableglfiscalsets.getNetChangeFieldNameForSelectedPeriod(iPeriodCounter + 1);
					BigDecimal bdTransTotal = arrPeriodTotals.get(iPeriodCounter);
					BigDecimal bdNetChangeInFiscalPeriod = rsFiscalSet.getBigDecimal(sNetChangeField);
					if (bdTransTotal.compareTo(bdNetChangeInFiscalPeriod) != 0){
						if (bUpdate){
							SQL = "UPDATE " + SMTableglfiscalsets.TableName
								+ " SET " + sNetChangeField + " = " + ServletUtilities.clsManageBigDecimals.BigDecimalTo2DecimalSQLFormat(bdTransTotal)
								+ " WHERE ("
									+ "(" + SMTableglfiscalsets.sAcctID + " = '" + sAccount + "')"
									+ " AND (" + SMTableglfiscalsets.ifiscalyear + " = " + arrFiscalYears.get(intFiscalYearCounter) + ")"
								+ ")"
							;
							try {
								ServletUtilities.clsDatabaseFunctions.executeSQLWithException(SQL, conn);
							} catch (Exception e) {
								throw new Exception("Error [201933182270] " + "updating fiscal set with SQL: '" + SQL + "' - " + e.getMessage());
							}
							sResult += "<BR>" + "Acct '" + sAccount + "', year " + arrFiscalYears.get(intFiscalYearCounter) + ", period  " + (iPeriodCounter + 1) + ": Transactions total: " 
								+ bdTransTotal + " matches fiscal set total."
							;
						}else{
						sResult += "<BR>" 
							+ "<B><FONT COLOR = RED>"
							+ "Acct '" + sAccount + "', year " + arrFiscalYears.get(intFiscalYearCounter) + ", period " + (iPeriodCounter + 1) + ": Transactions total: " 
							+ bdTransTotal + ", but fiscal set total is " + bdNetChangeInFiscalPeriod + " - difference of " 
							+ (bdTransTotal.subtract(bdNetChangeInFiscalPeriod)) + "."
							+ "</FONT></B>"
						;
						}
					}else{
						sResult += "<BR>" + "Acct '" + sAccount + "', year " + arrFiscalYears.get(intFiscalYearCounter) + ", period  " + (iPeriodCounter + 1) + ": Transactions total: " 
							+ bdTransTotal + " matches fiscal set total."
						;
					}
				}
				
				//Match up the 'closing period' (period 15):
				BigDecimal bdTransTotal = arrPeriodTotals.get(arrPeriodTotals.size() - 1);
				BigDecimal bdNetChangeInFiscalPeriod = rsFiscalSet.getBigDecimal(SMTableglfiscalsets.bdnetchangeperiod15);
				if (bdTransTotal.compareTo(bdNetChangeInFiscalPeriod) != 0){
					sResult += "<BR>" 
						+ "<B><FONT COLOR = RED>"
						+ "Acct '" + sAccount + "', year " + arrFiscalYears.get(intFiscalYearCounter) + ", period 15: Transactions total: " 
						+ bdTransTotal + ", but fiscal set total is " + bdNetChangeInFiscalPeriod + " - difference of " 
						+ (bdTransTotal.subtract(bdNetChangeInFiscalPeriod)) + "."
						+ "</FONT></B>"
					;
				}else{
					sResult += "<BR>" + "Acct '" + sAccount + "', year " + arrFiscalYears.get(intFiscalYearCounter) + ", period 15: Transactions total: " 
						+ bdTransTotal + " matches fiscal set total."
					;
				}
			}
			rsFiscalSet.close();
		}

		return sResult;
	}
	private String checkAgainstACCPACFiscalSets(
		ArrayList<clsFiscalSet>arrFiscalSets, 
		String sStartingFiscalYear, 
		String sGLAccount,
		Connection cnSMCP, 
		Connection cnACCPAC,
		ServletContext context,
		String sDBID) throws Exception{
		String sMessages = "";
		
		ArrayList<clsFiscalSet>arrACCPACFiscalSets = new ArrayList<clsFiscalSet>(0);
		
		//First, load the ACCPAC fiscal sets into the Array:
		String SQL = "SELECT * FROM GLAFS WHERE ("
			+ "(FSCSYR >= " + sStartingFiscalYear + ")"
			;
			if (sGLAccount.compareToIgnoreCase("") !=0 ){
				SQL += "AND (ACCTID = '" + sGLAccount + "')";
			}
			SQL += ")"
			+ " ORDER BY ACCTID, FSCSYR";
		Statement stmtACCPAC = cnACCPAC.createStatement();
		ResultSet rsACCPACFiscalSets = stmtACCPAC.executeQuery(SQL);
		try {
			while (rsACCPACFiscalSets.next()){
				clsFiscalSet objACCPACFiscal = new clsFiscalSet(
						rsACCPACFiscalSets.getString("ACCTID").trim(),
						rsACCPACFiscalSets.getInt("FSCSYR"),
						rsACCPACFiscalSets.getBigDecimal("OPENBAL"),
						rsACCPACFiscalSets.getBigDecimal("NETPERD1"),
						rsACCPACFiscalSets.getBigDecimal("NETPERD2"),
						rsACCPACFiscalSets.getBigDecimal("NETPERD3"),
						rsACCPACFiscalSets.getBigDecimal("NETPERD4"),
						rsACCPACFiscalSets.getBigDecimal("NETPERD5"),
						rsACCPACFiscalSets.getBigDecimal("NETPERD6"),
						rsACCPACFiscalSets.getBigDecimal("NETPERD7"),
						rsACCPACFiscalSets.getBigDecimal("NETPERD8"),
						rsACCPACFiscalSets.getBigDecimal("NETPERD9"),
						rsACCPACFiscalSets.getBigDecimal("NETPERD10"),
						rsACCPACFiscalSets.getBigDecimal("NETPERD11"),
						rsACCPACFiscalSets.getBigDecimal("NETPERD12"),
						rsACCPACFiscalSets.getBigDecimal("NETPERD13"),
						rsACCPACFiscalSets.getBigDecimal("NETPERD14"),
						rsACCPACFiscalSets.getBigDecimal("NETPERD15"),
						0
					);
					arrACCPACFiscalSets.add(objACCPACFiscal);
				
			}
			rsACCPACFiscalSets.close();
		} catch (Exception e) {
			throw new Exception("Error [2019291154385] " + "reading ACCPAC fiscal sets with SQL: '" + SQL + "' - " + e.getMessage());
		}
		
		//Next check it against the SMCP array:
		//First, see if either array has records that the other doesn't:
		for (int i = 0; i < arrACCPACFiscalSets.size(); i++){
			try {
				@SuppressWarnings("unused")
				clsFiscalSet fs = getFiscalSetByAccountAndYear(arrACCPACFiscalSets.get(i).m_sAcctID, arrACCPACFiscalSets.get(i).m_ifiscalyear, arrFiscalSets);
			} catch (Exception e) {
				sMessages += "<BR>" + e.getMessage() + " - ACCPAC has a fiscal set for account '" + arrACCPACFiscalSets.get(i).m_sAcctID + ", fiscal year " 
					+ Integer.toString(arrACCPACFiscalSets.get(i).m_ifiscalyear) + " , but SMCP does not.";
			}
		}
		
		//Now check in the other direction: are there any fiscal sets in SMCP that are not in ACCPAC?
		for (int i = 0; i < arrFiscalSets.size(); i++){
			try {
				@SuppressWarnings("unused")
				clsFiscalSet fs = getFiscalSetByAccountAndYear(arrFiscalSets.get(i).m_sAcctID, arrFiscalSets.get(i).m_ifiscalyear, arrACCPACFiscalSets);
			} catch (Exception e) {
				sMessages += "<BR>" + e.getMessage() + " - SMCP has a fiscal set for account '" + arrFiscalSets.get(i).m_sAcctID + ", fiscal year " 
					+ Integer.toString(arrFiscalSets.get(i).m_ifiscalyear) + " , but ACCPAC does not.";
			}
		}
		
		//Now check each matching fiscal set:
		
		String sLinkBase = "<A HREF=\"" + SMUtilities.getURLLinkBase(context) + "smgl.GLCheckTransactionLinesAgainstACCPAC" + "?" 
				+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
				//+ sAdditionalParameters
				//+ "\">" + sLabel + "</A>\n"
			;
		
		for (int i = 0; i < arrACCPACFiscalSets.size(); i++){
			clsFiscalSet fsACCPAC = arrACCPACFiscalSets.get(i);
			try {
				clsFiscalSet fsSMCP = getFiscalSetByAccountAndYear(fsACCPAC.m_sAcctID, fsACCPAC.m_ifiscalyear, arrFiscalSets);
				
				//Check each big decimal value:
				if (fsACCPAC.m_bdnetchangeperiod1.compareTo(fsSMCP.m_bdnetchangeperiod1) != 0){
					sMessages += "<BR>" + "ACCPAC account '" 
							+  sLinkBase 
							+ "&" + GLCheckTransactionLinesAgainstACCPAC.PARAM_FISCALPERIOD + "=1" 
							+ "&" + GLCheckTransactionLinesAgainstACCPAC.PARAM_FISCALYEAR + "=" + Integer.toString(fsACCPAC.m_ifiscalyear)
							+ "&" + GLCheckTransactionLinesAgainstACCPAC.PARAM_GLACCOUNT + "=" +  fsACCPAC.m_sAcctID
							+ "\">" + fsACCPAC.m_sAcctID + "</A>"
					+ "', fiscal year " + Integer.toString(fsACCPAC.m_ifiscalyear) 
					+ " net change, period 1 = " + fsACCPAC.m_bdnetchangeperiod1 + ", but SMCP has " + fsSMCP.m_bdnetchangeperiod1 + "."
					+ " Difference = " + fsACCPAC.m_bdnetchangeperiod1.subtract(fsSMCP.m_bdnetchangeperiod1)
					;
				}
				if (fsACCPAC.m_bdnetchangeperiod2.compareTo(fsSMCP.m_bdnetchangeperiod2) != 0){
					sMessages += "<BR>" + "ACCPAC account '" 
							+  sLinkBase 
							+ "&" + GLCheckTransactionLinesAgainstACCPAC.PARAM_FISCALPERIOD + "=2" 
							+ "&" + GLCheckTransactionLinesAgainstACCPAC.PARAM_FISCALYEAR + "=" + Integer.toString(fsACCPAC.m_ifiscalyear)
							+ "&" + GLCheckTransactionLinesAgainstACCPAC.PARAM_GLACCOUNT + "=" +  fsACCPAC.m_sAcctID
							+ "\">" + fsACCPAC.m_sAcctID + "</A>"
					+ "', fiscal year " + Integer.toString(fsACCPAC.m_ifiscalyear) 
					+ " net change, period 2 = " + fsACCPAC.m_bdnetchangeperiod2 + ", but SMCP has " + fsSMCP.m_bdnetchangeperiod2 + "."
					+ " Difference = " + fsACCPAC.m_bdnetchangeperiod2.subtract(fsSMCP.m_bdnetchangeperiod2)
					;
				}
				if (fsACCPAC.m_bdnetchangeperiod3.compareTo(fsSMCP.m_bdnetchangeperiod3) != 0){
					sMessages += "<BR>" + "ACCPAC account '" 
							+  sLinkBase 
							+ "&" + GLCheckTransactionLinesAgainstACCPAC.PARAM_FISCALPERIOD + "=3" 
							+ "&" + GLCheckTransactionLinesAgainstACCPAC.PARAM_FISCALYEAR + "=" + Integer.toString(fsACCPAC.m_ifiscalyear)
							+ "&" + GLCheckTransactionLinesAgainstACCPAC.PARAM_GLACCOUNT + "=" +  fsACCPAC.m_sAcctID
							+ "\">" + fsACCPAC.m_sAcctID + "</A>"
					+ "', fiscal year " + Integer.toString(fsACCPAC.m_ifiscalyear) 
					+ " net change, period 3 = " + fsACCPAC.m_bdnetchangeperiod3 + ", but SMCP has " + fsSMCP.m_bdnetchangeperiod3 + "."
					+ " Difference = " + fsACCPAC.m_bdnetchangeperiod3.subtract(fsSMCP.m_bdnetchangeperiod3)
					;
				}
				if (fsACCPAC.m_bdnetchangeperiod4.compareTo(fsSMCP.m_bdnetchangeperiod4) != 0){
					sMessages += "<BR>" + "ACCPAC account '" 
							+  sLinkBase 
							+ "&" + GLCheckTransactionLinesAgainstACCPAC.PARAM_FISCALPERIOD + "=4" 
							+ "&" + GLCheckTransactionLinesAgainstACCPAC.PARAM_FISCALYEAR + "=" + Integer.toString(fsACCPAC.m_ifiscalyear)
							+ "&" + GLCheckTransactionLinesAgainstACCPAC.PARAM_GLACCOUNT + "=" +  fsACCPAC.m_sAcctID
							+ "\">" + fsACCPAC.m_sAcctID + "</A>"
					+ "', fiscal year " + Integer.toString(fsACCPAC.m_ifiscalyear) 
					+ " net change, period 4 = " + fsACCPAC.m_bdnetchangeperiod4 + ", but SMCP has " + fsSMCP.m_bdnetchangeperiod4 + "."
					+ " Difference = " + fsACCPAC.m_bdnetchangeperiod4.subtract(fsSMCP.m_bdnetchangeperiod4)
					;
				}
				if (fsACCPAC.m_bdnetchangeperiod5.compareTo(fsSMCP.m_bdnetchangeperiod5) != 0){
					sMessages += "<BR>" + "ACCPAC account '" 
							+  sLinkBase 
							+ "&" + GLCheckTransactionLinesAgainstACCPAC.PARAM_FISCALPERIOD + "=5" 
							+ "&" + GLCheckTransactionLinesAgainstACCPAC.PARAM_FISCALYEAR + "=" + Integer.toString(fsACCPAC.m_ifiscalyear)
							+ "&" + GLCheckTransactionLinesAgainstACCPAC.PARAM_GLACCOUNT + "=" +  fsACCPAC.m_sAcctID
							+ "\">" + fsACCPAC.m_sAcctID + "</A>"
					+ "', fiscal year " + Integer.toString(fsACCPAC.m_ifiscalyear) 
					+ " net change, period 5 = " + fsACCPAC.m_bdnetchangeperiod5 + ", but SMCP has " + fsSMCP.m_bdnetchangeperiod5 + "."
					+ " Difference = " + fsACCPAC.m_bdnetchangeperiod5.subtract(fsSMCP.m_bdnetchangeperiod5)
					;
				}
				if (fsACCPAC.m_bdnetchangeperiod6.compareTo(fsSMCP.m_bdnetchangeperiod6) != 0){
					sMessages += "<BR>" + "ACCPAC account '" 
							+  sLinkBase 
							+ "&" + GLCheckTransactionLinesAgainstACCPAC.PARAM_FISCALPERIOD + "=6" 
							+ "&" + GLCheckTransactionLinesAgainstACCPAC.PARAM_FISCALYEAR + "=" + Integer.toString(fsACCPAC.m_ifiscalyear)
							+ "&" + GLCheckTransactionLinesAgainstACCPAC.PARAM_GLACCOUNT + "=" +  fsACCPAC.m_sAcctID
							+ "\">" + fsACCPAC.m_sAcctID + "</A>"
					+ "', fiscal year " + Integer.toString(fsACCPAC.m_ifiscalyear) 
					+ " net change, period 6 = " + fsACCPAC.m_bdnetchangeperiod6 + ", but SMCP has " + fsSMCP.m_bdnetchangeperiod6 + "."
					+ " Difference = " + fsACCPAC.m_bdnetchangeperiod6.subtract(fsSMCP.m_bdnetchangeperiod6)
					;
				}
				if (fsACCPAC.m_bdnetchangeperiod7.compareTo(fsSMCP.m_bdnetchangeperiod7) != 0){
					sMessages += "<BR>" + "ACCPAC account '" 
							+  sLinkBase 
							+ "&" + GLCheckTransactionLinesAgainstACCPAC.PARAM_FISCALPERIOD + "=7" 
							+ "&" + GLCheckTransactionLinesAgainstACCPAC.PARAM_FISCALYEAR + "=" + Integer.toString(fsACCPAC.m_ifiscalyear)
							+ "&" + GLCheckTransactionLinesAgainstACCPAC.PARAM_GLACCOUNT + "=" +  fsACCPAC.m_sAcctID
							+ "\">" + fsACCPAC.m_sAcctID + "</A>"
					+ "', fiscal year " + Integer.toString(fsACCPAC.m_ifiscalyear) 
					+ " net change, period 7 = " + fsACCPAC.m_bdnetchangeperiod7 + ", but SMCP has " + fsSMCP.m_bdnetchangeperiod7 + "."
					+ " Difference = " + fsACCPAC.m_bdnetchangeperiod7.subtract(fsSMCP.m_bdnetchangeperiod7)
					;
				}
				if (fsACCPAC.m_bdnetchangeperiod8.compareTo(fsSMCP.m_bdnetchangeperiod8) != 0){
					sMessages += "<BR>" + "ACCPAC account '" 
							+  sLinkBase 
							+ "&" + GLCheckTransactionLinesAgainstACCPAC.PARAM_FISCALPERIOD + "=8" 
							+ "&" + GLCheckTransactionLinesAgainstACCPAC.PARAM_FISCALYEAR + "=" + Integer.toString(fsACCPAC.m_ifiscalyear)
							+ "&" + GLCheckTransactionLinesAgainstACCPAC.PARAM_GLACCOUNT + "=" +  fsACCPAC.m_sAcctID
							+ "\">" + fsACCPAC.m_sAcctID + "</A>"
					+ "', fiscal year " + Integer.toString(fsACCPAC.m_ifiscalyear) 
					+ " net change, period 8 = " + fsACCPAC.m_bdnetchangeperiod8 + ", but SMCP has " + fsSMCP.m_bdnetchangeperiod8 + "."
					+ " Difference = " + fsACCPAC.m_bdnetchangeperiod8.subtract(fsSMCP.m_bdnetchangeperiod8)
					;
				}
				if (fsACCPAC.m_bdnetchangeperiod9.compareTo(fsSMCP.m_bdnetchangeperiod9) != 0){
					sMessages += "<BR>" + "ACCPAC account '" 
							+  sLinkBase 
							+ "&" + GLCheckTransactionLinesAgainstACCPAC.PARAM_FISCALPERIOD + "=9" 
							+ "&" + GLCheckTransactionLinesAgainstACCPAC.PARAM_FISCALYEAR + "=" + Integer.toString(fsACCPAC.m_ifiscalyear)
							+ "&" + GLCheckTransactionLinesAgainstACCPAC.PARAM_GLACCOUNT + "=" +  fsACCPAC.m_sAcctID
							+ "\">" + fsACCPAC.m_sAcctID + "</A>"
					+ "', fiscal year " + Integer.toString(fsACCPAC.m_ifiscalyear) 
					+ " net change, period 9 = " + fsACCPAC.m_bdnetchangeperiod9 + ", but SMCP has " + fsSMCP.m_bdnetchangeperiod9 + "."
					+ " Difference = " + fsACCPAC.m_bdnetchangeperiod9.subtract(fsSMCP.m_bdnetchangeperiod9)
					;
				}
				if (fsACCPAC.m_bdnetchangeperiod10.compareTo(fsSMCP.m_bdnetchangeperiod10) != 0){
					sMessages += "<BR>" + "ACCPAC account '" 
							+  sLinkBase 
							+ "&" +GLCheckTransactionLinesAgainstACCPAC.PARAM_FISCALPERIOD + "=10" 
							+ "&" + GLCheckTransactionLinesAgainstACCPAC.PARAM_FISCALYEAR + "=" + Integer.toString(fsACCPAC.m_ifiscalyear)
							+ "&" + GLCheckTransactionLinesAgainstACCPAC.PARAM_GLACCOUNT + "=" +  fsACCPAC.m_sAcctID
							+ "\">" + fsACCPAC.m_sAcctID + "</A>"
						+ "', fiscal year " + Integer.toString(fsACCPAC.m_ifiscalyear) 
					+ " net change, period 10 = " + fsACCPAC.m_bdnetchangeperiod10 + ", but SMCP has " + fsSMCP.m_bdnetchangeperiod10 + "."
					+ " Difference = " + fsACCPAC.m_bdnetchangeperiod10.subtract(fsSMCP.m_bdnetchangeperiod10)
					;
				}
				if (fsACCPAC.m_bdnetchangeperiod11.compareTo(fsSMCP.m_bdnetchangeperiod11) != 0){
					sMessages += "<BR>" + "ACCPAC account '" 
							+  sLinkBase 
							+ "&" + GLCheckTransactionLinesAgainstACCPAC.PARAM_FISCALPERIOD + "=11" 
							+ "&" + GLCheckTransactionLinesAgainstACCPAC.PARAM_FISCALYEAR + "=" + Integer.toString(fsACCPAC.m_ifiscalyear)
							+ "&" + GLCheckTransactionLinesAgainstACCPAC.PARAM_GLACCOUNT + "=" +  fsACCPAC.m_sAcctID
							+ "\">" + fsACCPAC.m_sAcctID + "</A>"
					+ "', fiscal year " + Integer.toString(fsACCPAC.m_ifiscalyear) 
					+ " net change, period 11 = " + fsACCPAC.m_bdnetchangeperiod11 + ", but SMCP has " + fsSMCP.m_bdnetchangeperiod11 + "."
					+ " Difference = " + fsACCPAC.m_bdnetchangeperiod11.subtract(fsSMCP.m_bdnetchangeperiod11)
					;
				}
				if (fsACCPAC.m_bdnetchangeperiod12.compareTo(fsSMCP.m_bdnetchangeperiod12) != 0){
					sMessages += "<BR>" + "ACCPAC account '" 
							+  sLinkBase 
							+ "&" + GLCheckTransactionLinesAgainstACCPAC.PARAM_FISCALPERIOD + "=12" 
							+ "&" + GLCheckTransactionLinesAgainstACCPAC.PARAM_FISCALYEAR + "=" + Integer.toString(fsACCPAC.m_ifiscalyear)
							+ "&" + GLCheckTransactionLinesAgainstACCPAC.PARAM_GLACCOUNT + "=" +  fsACCPAC.m_sAcctID
							+ "\">" + fsACCPAC.m_sAcctID + "</A>"
					+ "', fiscal year " + Integer.toString(fsACCPAC.m_ifiscalyear) 
					+ " net change, period 12 = " + fsACCPAC.m_bdnetchangeperiod12 + ", but SMCP has " + fsSMCP.m_bdnetchangeperiod12 + "."
					+ " Difference = " + fsACCPAC.m_bdnetchangeperiod12.subtract(fsSMCP.m_bdnetchangeperiod12)
					;
				}
				if (fsACCPAC.m_bdnetchangeperiod13.compareTo(fsSMCP.m_bdnetchangeperiod13) != 0){
					sMessages += "<BR>" + "ACCPAC account '" 
							+  sLinkBase 
							+ "&" + GLCheckTransactionLinesAgainstACCPAC.PARAM_FISCALPERIOD + "=13" 
							+ "&" + GLCheckTransactionLinesAgainstACCPAC.PARAM_FISCALYEAR + "=" + Integer.toString(fsACCPAC.m_ifiscalyear)
							+ "&" + GLCheckTransactionLinesAgainstACCPAC.PARAM_GLACCOUNT + "=" +  fsACCPAC.m_sAcctID
							+ "\">" + fsACCPAC.m_sAcctID + "</A>"
					+ "', fiscal year " + Integer.toString(fsACCPAC.m_ifiscalyear) 
					+ " net change, period 13 = " + fsACCPAC.m_bdnetchangeperiod13 + ", but SMCP has " + fsSMCP.m_bdnetchangeperiod13 + "."
					+ " Difference = " + fsACCPAC.m_bdnetchangeperiod13.subtract(fsSMCP.m_bdnetchangeperiod13)
					;
				}
				
				if (fsACCPAC.m_bdnetchangeperiod14.compareTo(fsSMCP.m_bdnetchangeperiod14) != 0){
					sMessages += "<BR>" + "ACCPAC account '" 
							+  sLinkBase 
							+ "&" + GLCheckTransactionLinesAgainstACCPAC.PARAM_FISCALPERIOD + "=14" 
							+ "&" + GLCheckTransactionLinesAgainstACCPAC.PARAM_FISCALYEAR + "=" + Integer.toString(fsACCPAC.m_ifiscalyear)
							+ "&" + GLCheckTransactionLinesAgainstACCPAC.PARAM_GLACCOUNT + "=" +  fsACCPAC.m_sAcctID
							+ "\">" + fsACCPAC.m_sAcctID + "</A>"
					+ "', fiscal year " + Integer.toString(fsACCPAC.m_ifiscalyear) 
					+ " net change, period 14 = " + fsACCPAC.m_bdnetchangeperiod14 + ", but SMCP has " + fsSMCP.m_bdnetchangeperiod14 + "."
					+ " Difference = " + fsACCPAC.m_bdnetchangeperiod15.subtract(fsSMCP.m_bdnetchangeperiod15)
					;
				}
				
				if (fsACCPAC.m_bdnetchangeperiod15.compareTo(fsSMCP.m_bdnetchangeperiod15) != 0){
					sMessages += "<BR>" + "ACCPAC account '" 
							+  sLinkBase 
							+ "&" + GLCheckTransactionLinesAgainstACCPAC.PARAM_FISCALPERIOD + "=15" 
							+ "&" + GLCheckTransactionLinesAgainstACCPAC.PARAM_FISCALYEAR + "=" + Integer.toString(fsACCPAC.m_ifiscalyear)
							+ "&" + GLCheckTransactionLinesAgainstACCPAC.PARAM_GLACCOUNT + "=" +  fsACCPAC.m_sAcctID
							+ "\">" + fsACCPAC.m_sAcctID + "</A>"
					+ "', fiscal year " + Integer.toString(fsACCPAC.m_ifiscalyear) 
					+ " net change, period 15 = " + fsACCPAC.m_bdnetchangeperiod15 + ", but SMCP has " + fsSMCP.m_bdnetchangeperiod15 + "."
					+ " Difference = " + fsACCPAC.m_bdnetchangeperiod15.subtract(fsSMCP.m_bdnetchangeperiod15)
					;
				}
				
				if (fsACCPAC.m_bdopeningbalance.compareTo(fsSMCP.m_bdopeningbalance) != 0){
					sMessages += "<BR>" + "ACCPAC account '" + fsACCPAC.m_sAcctID + ". fiscal year " + Integer.toString(fsACCPAC.m_ifiscalyear) 
					+ " opening balance = " + fsACCPAC.m_bdopeningbalance + ", but SMCP has " + fsSMCP.m_bdopeningbalance + "."
					+ " Difference = " + fsACCPAC.m_bdopeningbalance.subtract(fsSMCP.m_bdopeningbalance)
					;
				}
				
			} catch (Exception e) {
				sMessages += "<BR>" + "[1571430144]" + e.getMessage() + " - Could not read SMCP fiscal set for account '" + arrACCPACFiscalSets.get(i).m_sAcctID + ", fiscal year " 
					+ Integer.toString(arrACCPACFiscalSets.get(i).m_ifiscalyear) + ".";
			}
		}
		
		if (sMessages.compareToIgnoreCase("") == 0){
			sMessages = "<BR>ACCPAC and SMCP fiscal data match.";
		}
		return sMessages;
	}
	private String insertMissingFinancialRecords(ArrayList<clsFiscalSet>arrFiscalSets, String sStartingFiscalYear, Connection conn) throws Exception{
		String SQL = "";
		long lStartingNumberOfRecords = 0L;
		long lEndingNumberOfRecords = 0L;
		int iBufferSize = 0;
		final int MAX_BUFFER = 100;
		
		try {
			SQL = "SELECT COUNT(*) AS RECORDCOUNT FROM " + SMTableglfinancialstatementdata.TableName;
			ResultSet rsCount = ServletUtilities.clsDatabaseFunctions.openResultSet(SQL, conn);
			if (rsCount.next()){
				lStartingNumberOfRecords = rsCount.getLong("RECORDCOUNT");
			}
			rsCount.close();
		} catch (Exception e1) {
			throw new Exception("Error [20192891226323] " + "Could not get starting count of financial statement data records with SQL: '" 
				+ SQL + "'.");
		}
		
		//We turn this off to get faster inserts:
//		Statement stmtCommit;
//		try {
//			stmtCommit = conn.createStatement();
//			stmtCommit.execute("SET autocommit=0");
//		} catch (Exception e1) {
//			throw new Exception("Error [20192971431463] " + "setting AUTOCOMMIT to ZERO to insert glfinancial statement data - " + e1.getMessage());
//		}
		
		String sInsertBuffer = "";
		try {
			for (int iFiscalSetCounter = 0; iFiscalSetCounter < arrFiscalSets.size(); iFiscalSetCounter++){
				//We only need the fiscal sets from our selected 'starting fiscal year' forward, not the historical ones we
				//added to the array:
				if (arrFiscalSets.get(iFiscalSetCounter).m_ifiscalyear >= Integer.parseInt(sStartingFiscalYear)){
					for (int iPeriodCounter = 1; iPeriodCounter < SMTableglfiscalperiods.MAX_NUMBER_OF_EDITABLE_USER_PERIODS; iPeriodCounter++){
						if (sInsertBuffer.compareToIgnoreCase("") != 0){
							sInsertBuffer += ",";
						}
						sInsertBuffer += "(" 
							+ Integer.toString(iPeriodCounter)
							+ ", " + Integer.toString(arrFiscalSets.get(iFiscalSetCounter).m_ifiscalyear)
							+ ", '" + arrFiscalSets.get(iFiscalSetCounter).m_sAcctID + "'"
							+ ")"
						;

					}
					//Add one more for the 'closing period' (period 15):
					if (sInsertBuffer.compareToIgnoreCase("") != 0){
						sInsertBuffer += ",";
					}
					sInsertBuffer += "(" 
							+ Integer.toString(SMTableglfiscalsets.TOTAL_NUMBER_OF_GL_PERIODS)
							+ ", " + Integer.toString(arrFiscalSets.get(iFiscalSetCounter).m_ifiscalyear)
							+ ", '" + arrFiscalSets.get(iFiscalSetCounter).m_sAcctID + "'"
							+ ")"
						;
					
					if (iBufferSize >= MAX_BUFFER){
						SQL = "INSERT INTO " + SMTableglfinancialstatementdata.TableName
							+ " ("
							+ SMTableglfinancialstatementdata.ifiscalperiod
							+ ", " + SMTableglfinancialstatementdata.ifiscalyear
							+ ", " + SMTableglfinancialstatementdata.sacctid
							+ ") VALUES "
							+ sInsertBuffer
							+ " ON DUPLICATE KEY UPDATE " + SMTableglfinancialstatementdata.sacctid + " = " + SMTableglfinancialstatementdata.sacctid
						;
						try {
							Statement stmt = conn.createStatement();
							stmt.execute(SQL);
						} catch (Exception e) {
							//stmtCommit.execute("SET AUTOCOMMIT=1");
							throw new Exception("Error [2019289127429] " + "Error inserting"
								+ " financial statement record with SQL: '" 
								+ SQL + "' - " + e.getMessage());
						}
						iBufferSize = 0;
						sInsertBuffer = "";
					}
					iBufferSize++;
				}
			}
			
			//If there's anything left in the buffer, do the SQL command for it now:
			if (iBufferSize > 0){
				SQL = "INSERT INTO " + SMTableglfinancialstatementdata.TableName
						+ " ("
						+ SMTableglfinancialstatementdata.ifiscalperiod
						+ ", " + SMTableglfinancialstatementdata.ifiscalyear
						+ ", " + SMTableglfinancialstatementdata.sacctid
						+ ") VALUES "
						+ sInsertBuffer
						+ " ON DUPLICATE KEY UPDATE " + SMTableglfinancialstatementdata.sacctid + " = " + SMTableglfinancialstatementdata.sacctid
					;
					try {
						Statement stmt = conn.createStatement();
						stmt.execute(SQL);
					} catch (Exception e) {
						//stmtCommit.execute("SET AUTOCOMMIT=1");
						throw new Exception("Error [2019289127430] " + "Error inserting"
							+ " financial statement record with SQL: '" 
							+ SQL + "' - " + e.getMessage());
					}
			}
			
		} catch (Exception e) {
			//stmtCommit.execute("SET AUTOCOMMIT=1");
			throw new Exception("Error [2019289129599] " + "Error inserting financial statement records - " + e.getMessage());
		}
		
//		try {
//			stmtCommit.execute("COMMIT");
//		} catch (Exception e) {
//			throw new Exception("Error [20192971432361] " + "commiting glfinancialstatementdata Inserts - " + e.getMessage());
//		}
		
		try {
			SQL = "SELECT COUNT(*) AS RECORDCOUNT FROM " + SMTableglfinancialstatementdata.TableName;
			ResultSet rsCount = ServletUtilities.clsDatabaseFunctions.openResultSet(SQL, conn);
			if (rsCount.next()){
				lEndingNumberOfRecords = rsCount.getLong("RECORDCOUNT");
			}
			rsCount.close();
		} catch (Exception e1) {
			throw new Exception("Error [20192891226323] " + "Could not get starting count of financial statement data records with SQL: '" 
				+ SQL + "'.");
		}
		
		return "<BR>" + Long.toString(lEndingNumberOfRecords - lStartingNumberOfRecords) + " NEW financial statement data records were added.";
	}
	
	public void updateFiscalSetsForAccount(
			Connection conn,
			String sAccount,
			int iFiscalYear,
			int iFiscalPeriod,
			BigDecimal bdNetTotalPeriodChangeForAccount
			) throws Exception{
			
			//Determine which field we'll be updating:
			String sNetChangeField = SMTableglfiscalsets.getNetChangeFieldNameForSelectedPeriod(iFiscalPeriod);
			
			//First, get the closing balance from the previous fiscal year:
			BigDecimal bdPreviousYearClosingBalance = getClosingBalanceForFiscalYear(sAccount, iFiscalYear - 1, conn);
			
			//The fiscal set record gets created or updated here:
			String SQL = "INSERT INTO " + SMTableglfiscalsets.TableName + "("
				+ sNetChangeField
				+ ", " + SMTableglfiscalsets.bdopeningbalance
				+ ", " + SMTableglfiscalsets.ifiscalyear
				+ ", " + SMTableglfiscalsets.sAcctID
				+ ") VALUES ("
				+ ServletUtilities.clsManageBigDecimals.BigDecimalTo2DecimalSQLFormat(bdNetTotalPeriodChangeForAccount)
				+ ", " + ServletUtilities.clsManageBigDecimals.BigDecimalTo2DecimalSQLFormat(bdPreviousYearClosingBalance)
				+ ", " + Integer.toString(iFiscalYear)
				+ ", '" + sAccount + "'"
				+ ") ON DUPLICATE KEY UPDATE "
				+ sNetChangeField + " = " + ServletUtilities.clsManageBigDecimals.BigDecimalTo2DecimalSQLFormat(bdNetTotalPeriodChangeForAccount)
			;
			
			try {
				Statement stmt = conn.createStatement();
				stmt.execute(SQL);
			} catch (Exception e) {
				throw new Exception(
					"Error [1555962542] updating GL fiscal set for account '" + sAccount 
					+ "', fiscal year " + Integer.toString(iFiscalYear) 
					+ ", period " + Integer.toString(iFiscalPeriod) 
					+ " with SQL: '" + SQL + "'"
					+ " - " + e.getMessage());
			}
			
			//Here we update future fiscal sets:
			
			//Allow for the possibility that this update affects an opening balance in a subsequent fiscal set:
			//Update the opening balance for any subsequent fiscal years for this account:
			//Update retained earnings for the year RATHER than the starting balance
			GLAccount glacct = new GLAccount(sAccount);
			//This flag will remember for us whether an income/expense account was involved here.  If it was, we'll
			//need to update the financial statement data for the closing account, as well as the income/expense account.
			//That may be done more than once if we loop through here more than once - but that's simpler in the logic,
			//even if it means that we spend a few redundant iterations....
			if(!glacct.load(conn)){
				throw new Exception("Error [1556569790] checking normal balance type for GL account '" 
					+ sAccount + "' - " + glacct.getErrorMessageString());
			}
			
			//If it's a balance sheet account, then we simply update all the opening balances in any subsequent
			//fiscal sets.
			
			//Income statement account changes from the previous year go to Retained Earnings (the 'closing' account).
			// Retained earnings normally carries a 'credit' balance, as do income accounts.
			// Expense accounts normally carry a debit balance.
			String sTargetAccount = sAccount;
			String sClosingAccount = "";
			if (glacct.getM_stype().compareToIgnoreCase(SMTableglaccounts.ACCOUNT_TYPE_INCOME_STATEMENT) == 0){
				GLOptions gloptions = new GLOptions();
				if (!gloptions.load(conn)){
					throw new Exception("Error [1556569791] checking GL Options closing account " 
						+ " - " + gloptions.getErrorMessageString());
				}
				//sTargetAccount = gloptions.getsClosingAccount();
				sClosingAccount = gloptions.getsClosingAccount();
			}
			
			//Determine how many subsequent fiscal years there are, and update the opening balance on each:
			SQL = "SELECT"
				+ " " + SMTableglfiscalperiods.ifiscalyear
				+ " FROM " + SMTableglfiscalperiods.TableName
				+ " WHERE ("
					+ "(" + SMTableglfiscalperiods.ifiscalyear + " > " + iFiscalYear + ")"
					//+ " AND (" + SMTableglfiscalperiods.TableName + "." + SMTableglfiscalperiods.ilockadjustmentperiod + " = 0)"
				+ ")"
			;
	    	try {
				ResultSet rsFiscalYears = ServletUtilities.clsDatabaseFunctions.openResultSet(SQL, conn);
				while (rsFiscalYears.next()){
					//First, get the closing balance of the previous year:
					bdPreviousYearClosingBalance = getClosingBalanceForFiscalYear(
						sTargetAccount, rsFiscalYears.getInt(SMTableglfiscalsets.ifiscalyear) - 1, conn);
					
					//FUTURE Fiscal set records get inserted or updated as needed:
					SQL = "INSERT INTO " + SMTableglfiscalsets.TableName + "("
						+ SMTableglfiscalsets.bdopeningbalance
						+ ", " + SMTableglfiscalsets.ifiscalyear
						+ ", " + SMTableglfiscalsets.sAcctID
						+ ") VALUES ("
						+ ServletUtilities.clsManageBigDecimals.BigDecimalTo2DecimalSQLFormat(bdPreviousYearClosingBalance)
						+ ", " + Integer.toString(rsFiscalYears.getInt(SMTableglfiscalsets.ifiscalyear))
						+ ", '" + sTargetAccount + "'"
						+ ")"
						+ " ON DUPLICATE KEY UPDATE "
						+ SMTableglfiscalsets.bdopeningbalance + " = " 
							+ ServletUtilities.clsManageBigDecimals.BigDecimalTo2DecimalSQLFormat(bdPreviousYearClosingBalance)
					;

					//System.out.println("[155603919] fiscal sets UPDATE statement = '" + SQL + "'.");
					try {
						Statement stmt = conn.createStatement();
						stmt.execute(SQL);
					} catch (Exception e) {
						throw new Exception(
							"Error [1555962592] updating opening balance of subsequent GL fiscal sets for account '" + sAccount 
							+ "', fiscal year " + Integer.toString(rsFiscalYears.getInt(SMTableglfiscalsets.ifiscalyear))
							+ " - " + e.getMessage());
					}
				}
				rsFiscalYears.close();
			} catch (Exception e) {
				throw new Exception("Error [1555958398] reading fiscal years with SQL: '" + SQL + "' - " + e.getMessage());
			}
	    	
	    	GLFinancialDataCheck dc = new GLFinancialDataCheck();
			try {
				dc.processFinancialRecords(sAccount, Integer.toString(iFiscalYear), conn, true);
			} catch (Exception e) {
				throw new Exception(e.getMessage());
			}
			
			//If there is a CLOSING account, update it, too:
			if(sClosingAccount.compareToIgnoreCase("") != 0){
				try {
					dc.processFinancialRecords(sClosingAccount, Integer.toString(iFiscalYear), conn, true);
				} catch (Exception e) {
					throw new Exception(e.getMessage());
				}	
			}
			return;
		}
	private static BigDecimal getClosingBalanceForFiscalYear(String sAcctID, int iFiscalYear, Connection conn) throws Exception{
		
		BigDecimal bdClosingBalance = new BigDecimal("0.00");
		String SQL = "SELECT"
			+ " (" + SMTableglfiscalsets.bdopeningbalance
			+ " + " + SMTableglfiscalsets.bdnetchangeperiod1
			+ " + " + SMTableglfiscalsets.bdnetchangeperiod2
			+ " + " + SMTableglfiscalsets.bdnetchangeperiod3
			+ " + " + SMTableglfiscalsets.bdnetchangeperiod4
			+ " + " + SMTableglfiscalsets.bdnetchangeperiod5
			+ " + " + SMTableglfiscalsets.bdnetchangeperiod6
			+ " + " + SMTableglfiscalsets.bdnetchangeperiod7
			+ " + " + SMTableglfiscalsets.bdnetchangeperiod8
			+ " + " + SMTableglfiscalsets.bdnetchangeperiod9
			+ " + " + SMTableglfiscalsets.bdnetchangeperiod10
			+ " + " + SMTableglfiscalsets.bdnetchangeperiod11
			+ " + " + SMTableglfiscalsets.bdnetchangeperiod12
			+ " + " + SMTableglfiscalsets.bdnetchangeperiod13
			+ " + " + SMTableglfiscalsets.bdnetchangeperiod14
			+ " + " + SMTableglfiscalsets.bdnetchangeperiod15
			+ " ) AS CLOSINGBALANCE"
			+ " FROM " + SMTableglfiscalsets.TableName
			+ " WHERE (" 
				+ "(" + SMTableglfiscalsets.sAcctID + " = '" + sAcctID + "')"
				+ " AND (" + SMTableglfiscalsets.ifiscalyear + " = " + Integer.toString(iFiscalYear) + ")"
			+ ")"
		;
		try {
			ResultSet rsClosingBalance = ServletUtilities.clsDatabaseFunctions.openResultSet(SQL, conn);
			if (rsClosingBalance.next()){
				bdClosingBalance = rsClosingBalance.getBigDecimal("CLOSINGBALANCE");
			}
			rsClosingBalance.close();
		} catch (Exception e) {
			throw new Exception("Error [20193121320230] " + "reading closing balance for GL Account '" + sAcctID + "', fiscal year " + Integer.toString(iFiscalYear)
			+ " with SQL: '" + SQL + "' - " + e.getMessage());
		}
		return bdClosingBalance;
	}
	private String processSingleFiscalSet(
		String sAccount, 
		Connection conn, 
		String sStartingFiscalYear, 
		ArrayList<clsFiscalSet>arrFiscalSets,
		boolean bUpdateRecords) throws Exception{
		String sMessages = "";

		//Get all the financial statement data that could be affected by this fiscal set:
		String SQL = "";
		SQL = "SELECT * FROM " + SMTableglfinancialstatementdata.TableName
			+ " WHERE ("
				+ "(" + SMTableglfinancialstatementdata.ifiscalyear + " >= " + sStartingFiscalYear + ")"
				+ " AND (" + SMTableglfinancialstatementdata.sacctid + " = '" + sAccount + "')"
			+ ") ORDER BY " + SMTableglfinancialstatementdata.ifiscalyear + ", " + SMTableglfinancialstatementdata.ifiscalperiod
		;
		try {
			ResultSet rsFinancials = clsDatabaseFunctions.openResultSet(SQL, conn);
			while(rsFinancials.next()){
				if (bUpdateRecords){
					sMessages += updateSingleFinancialRecord(rsFinancials, arrFiscalSets, conn);
				}else{
					sMessages += checkSingleFinancialRecord(rsFinancials, arrFiscalSets);
				}
				//System.out.println("Checked account '" + sAccount + "', year " 
				//	+ rsFinancials.getInt(SMTableglfinancialstatementdata.ifiscalyear)
				//	+ ", period " + rsFinancials.getInt(SMTableglfinancialstatementdata.ifiscalperiod));
			}
			rsFinancials.close();
		} catch (Exception e) {
			throw new Exception("Error [2019287846312] " + "reading financial data with SQL '" + SQL + "' - " + e.getMessage());
		}
		
		return sMessages;
	}
	private String checkSingleFinancialRecord(ResultSet rsFinancialData, ArrayList<clsFiscalSet>arrFiscalSets) throws Exception{
		String sMessages = "";
		
		//Here we check all the fields to see if the financial statement record is in sync with the fiscal set data:
		//Now check all the related financial statement records:
		
		//First, the opening balance:
		//Get the values from the current financial record that we're checking:
		String sAccount = rsFinancialData.getString(SMTableglfinancialstatementdata.TableName + "." + SMTableglfinancialstatementdata.sacctid);
		int iFiscalYear = rsFinancialData.getInt(SMTableglfinancialstatementdata.TableName + "." + SMTableglfinancialstatementdata.ifiscalyear);
		int iFiscalPeriod = rsFinancialData.getInt(SMTableglfinancialstatementdata.TableName + "." + SMTableglfinancialstatementdata.ifiscalperiod);
		clsFiscalSet objCurrentFiscalSet = getFiscalSetByAccountAndYear(
			sAccount,
			iFiscalYear,
			arrFiscalSets
		);
		
		//Opening balance:
		if (rsFinancialData.getBigDecimal(SMTableglfinancialstatementdata.bdopeningbalance).compareTo(objCurrentFiscalSet.m_bdopeningbalance) != 0){
			sMessages += "\n" + "Acct " + sAccount + " year " + Integer.toString(iFiscalYear) + " period " + Integer.toString(iFiscalPeriod)
				+ " opening balance shows " + rsFinancialData.getBigDecimal(
					SMTableglfinancialstatementdata.TableName + "." + SMTableglfinancialstatementdata.bdopeningbalance)
				+ " but the fiscal set shows " + objCurrentFiscalSet.m_bdopeningbalance + "."
			;
		}
		
		//Map the fields we need for the different periods:
		//Net change per period:
		if (rsFinancialData.getBigDecimal(SMTableglfinancialstatementdata.bdnetchangeforperiod).compareTo(
				objCurrentFiscalSet.getPeriodChangeByPeriod(iFiscalPeriod)) != 0){
			sMessages += "\n" + "Acct " + sAccount + " year " + Integer.toString(iFiscalYear) + " period " + Integer.toString(iFiscalPeriod)
				+ " net change for period shows " + rsFinancialData.getBigDecimal(
					SMTableglfinancialstatementdata.TableName + "." + SMTableglfinancialstatementdata.bdnetchangeforperiod)
				+ " but the fiscal set shows " + objCurrentFiscalSet.getPeriodChangeByPeriod(iFiscalPeriod) + "."
			;
		}
		
		//Total year to date:
		if (rsFinancialData.getBigDecimal(SMTableglfinancialstatementdata.bdtotalyeartodate).compareTo(
				objCurrentFiscalSet.getTotalYearToDataByPeriod(iFiscalPeriod)) != 0){
			sMessages += "\n" + "Acct " + sAccount + " year " + Integer.toString(iFiscalYear) + " period " + Integer.toString(iFiscalPeriod)
				+ " total year to date for period shows " + rsFinancialData.getBigDecimal(
					SMTableglfinancialstatementdata.TableName + "." + SMTableglfinancialstatementdata.bdtotalyeartodate)
				+ " but the fiscal set shows " + objCurrentFiscalSet.getTotalYearToDataByPeriod(iFiscalPeriod) + "."
			;
		}
		
		//Net change for same period in the previous year:
		boolean bPreviousYearFound = true;
		clsFiscalSet objPreviousFiscalSet = null;
		try {
			objPreviousFiscalSet = getFiscalSetByAccountAndYear(
				sAccount,
				iFiscalYear - 1,
				arrFiscalSets
			);
		} catch (Exception e) {
			//If we can't find a previous year, then we can't update any 'previous year' fields:
			bPreviousYearFound = false;
		}
		
		if (bPreviousYearFound){
			//Test the net change from same period, previous year:
			if (rsFinancialData.getBigDecimal(SMTableglfinancialstatementdata.bdnetchangeforperiodpreviousyear).compareTo(
					objPreviousFiscalSet.getPeriodChangeByPeriod(iFiscalPeriod)) != 0){
				sMessages += "\n" + "Acct " + sAccount + " year " + Integer.toString(iFiscalYear) + " period " + Integer.toString(iFiscalPeriod)
					+ " same period, for the previous year shows " + rsFinancialData.getBigDecimal(
						SMTableglfinancialstatementdata.TableName + "." + SMTableglfinancialstatementdata.bdnetchangeforperiodpreviousyear)
					+ " but the fiscal set shows " + objPreviousFiscalSet.getPeriodChangeByPeriod(iFiscalPeriod) + "."
				;
			}
			
			//Test the previous year's total to date:
			if (rsFinancialData.getBigDecimal(SMTableglfinancialstatementdata.bdtotalpreviousyeartodate).compareTo(
					objPreviousFiscalSet.getTotalYearToDataByPeriod(iFiscalPeriod)) != 0){
				sMessages += "\n" + "Acct " + sAccount + " year " + Integer.toString(iFiscalYear) + " period " + Integer.toString(iFiscalPeriod)
					+ " total previous year to date shows " + rsFinancialData.getBigDecimal(
						SMTableglfinancialstatementdata.TableName + "." + SMTableglfinancialstatementdata.bdtotalpreviousyeartodate)
					+ " but the fiscal set shows " + objPreviousFiscalSet.getTotalYearToDataByPeriod(iFiscalPeriod) + "."
				;
			}
			
			//Test the previous year's opening balance:
			if (rsFinancialData.getBigDecimal(SMTableglfinancialstatementdata.bdopeningbalancepreviousyear).compareTo(
					objPreviousFiscalSet.m_bdopeningbalance) != 0){
				sMessages += "\n" + "Acct " + sAccount + " year " + Integer.toString(iFiscalYear) + " period " + Integer.toString(iFiscalPeriod)
					+ " previous year opening balance shows " + rsFinancialData.getBigDecimal(
						SMTableglfinancialstatementdata.TableName + "." + SMTableglfinancialstatementdata.bdopeningbalancepreviousyear)
					+ " but the fiscal set shows " + objPreviousFiscalSet.m_bdopeningbalance + "."
				;
			}
		}
		
		//Get the net change for the previous period:
		if (iFiscalPeriod == 1){
			if (bPreviousYearFound){
				//Get the net change for the last period of the previous year
				BigDecimal bdNetChangeLastPeriodPreviousYear = objPreviousFiscalSet.getPeriodChangeByPeriod(objPreviousFiscalSet.m_numberofperiods);
				if (rsFinancialData.getBigDecimal(SMTableglfinancialstatementdata.bdnetchangeforpreviousperiod).compareTo(
						bdNetChangeLastPeriodPreviousYear) != 0){
					sMessages += "\n" + "Acct " + sAccount + " year " + Integer.toString(iFiscalYear) + " period " + Integer.toString(iFiscalPeriod)
						+ " previous period net change shows " + rsFinancialData.getBigDecimal(
							SMTableglfinancialstatementdata.TableName + "." + SMTableglfinancialstatementdata.bdnetchangeforpreviousperiod)
						+ " but the fiscal set shows " + bdNetChangeLastPeriodPreviousYear + "."
					;
				}
			}
		}else{
			if (rsFinancialData.getBigDecimal(SMTableglfinancialstatementdata.bdnetchangeforpreviousperiod).compareTo(
					objCurrentFiscalSet.getPeriodChangeByPeriod(iFiscalPeriod - 1)) != 0){
				sMessages += "\n" + "Acct " + sAccount + " year " + Integer.toString(iFiscalYear) + " period " + Integer.toString(iFiscalPeriod)
					+ " previous period net change shows " + rsFinancialData.getBigDecimal(
						SMTableglfinancialstatementdata.TableName + "." + SMTableglfinancialstatementdata.bdnetchangeforpreviousperiod)
					+ " but the fiscal set shows " + objCurrentFiscalSet.getPeriodChangeByPeriod(iFiscalPeriod - 1) + "."
				;
			}
		}
		
		//Get the net change for the previous period, in the previous year:
		if (iFiscalPeriod == 1){
			//We'll need to get the last period of the TWO years previous:
			boolean bTwoPreviousYearsFound = true;
			clsFiscalSet objPreviousTwoYearsFiscalSet = null;
			try {
				objPreviousTwoYearsFiscalSet = getFiscalSetByAccountAndYear(
					sAccount,
					iFiscalYear - 2,
					arrFiscalSets
				);
			} catch (Exception e) {
				//If we can't find a two years previous, then we can't update any 'two years previous' fields:
				bTwoPreviousYearsFound = false;
			}
			
			if (bTwoPreviousYearsFound){
				if (rsFinancialData.getBigDecimal(SMTableglfinancialstatementdata.bdnetchangeforpreviousperiodpreviousyear).compareTo(
						objPreviousTwoYearsFiscalSet.getPeriodChangeByPeriod(objPreviousTwoYearsFiscalSet.m_numberofperiods)) != 0){
					sMessages += "\n" + "Acct " + sAccount + " year " + Integer.toString(iFiscalYear) + " period " + Integer.toString(iFiscalPeriod)
						+ " previous period, previous year net change shows " + rsFinancialData.getBigDecimal(
							SMTableglfinancialstatementdata.TableName + "." + SMTableglfinancialstatementdata.bdnetchangeforpreviousperiodpreviousyear)
						+ " but the fiscal set shows " + objPreviousTwoYearsFiscalSet.getPeriodChangeByPeriod(objPreviousTwoYearsFiscalSet.m_numberofperiods) + "."
					;
				}
			}
		}else{
			if (bPreviousYearFound){
				if (rsFinancialData.getBigDecimal(SMTableglfinancialstatementdata.bdnetchangeforpreviousperiodpreviousyear).compareTo(
						objPreviousFiscalSet.getPeriodChangeByPeriod(iFiscalPeriod - 1)) != 0){
					sMessages += "\n" + "Acct " + sAccount + " year " + Integer.toString(iFiscalYear) + " period " + Integer.toString(iFiscalPeriod)
						+ " previous period, previous year net change shows " + rsFinancialData.getBigDecimal(
							SMTableglfinancialstatementdata.TableName + "." + SMTableglfinancialstatementdata.bdnetchangeforpreviousperiodpreviousyear)
						+ " but the fiscal set shows " + objPreviousFiscalSet.getPeriodChangeByPeriod(iFiscalPeriod - 1) + "."
					;
				}
			}
		}

		return sMessages;
	}
	
	private String updateSingleFinancialRecord(ResultSet rsFinancialData, ArrayList<clsFiscalSet>arrFiscalSets, Connection conn) throws Exception{
		String sMessages = "";
		
		String sAccount = rsFinancialData.getString(SMTableglfinancialstatementdata.TableName + "." + SMTableglfinancialstatementdata.sacctid);
		String sFiscalYear = Integer.toString(rsFinancialData.getInt(SMTableglfinancialstatementdata.TableName + "." + SMTableglfinancialstatementdata.ifiscalyear));
		String sFiscalPeriod = Integer.toString(rsFinancialData.getInt(SMTableglfinancialstatementdata.TableName + "." + SMTableglfinancialstatementdata.ifiscalperiod));
		BigDecimal bdopeningbalance = new BigDecimal("0.00");
		BigDecimal bdnetchangeforperiod = new BigDecimal("0.00");
		BigDecimal bdtotalyeartodate = new BigDecimal("0.00");
		BigDecimal bdnetchangeforperiodpreviousyear = new BigDecimal("0.00");
		BigDecimal bdtotalpreviousyeartodate = new BigDecimal("0.00");
		BigDecimal bdopeningbalancepreviousyear = new BigDecimal("0.00");
		BigDecimal bdnetchangeforpreviousperiod = new BigDecimal("0.00");
		BigDecimal bdnetchangeforpreviousperiodpreviousyear = new BigDecimal("0.00");
		
		//Here we check all the fields to see if the financial statement record is in sync with the fiscal set data:
		//Now check all the related financial statement records:
		
		//First, the opening balance:
		//Get the values from the current financial record that we're checking:
		int iFiscalYear = rsFinancialData.getInt(SMTableglfinancialstatementdata.TableName + "." + SMTableglfinancialstatementdata.ifiscalyear);
		int iFiscalPeriod = rsFinancialData.getInt(SMTableglfinancialstatementdata.TableName + "." + SMTableglfinancialstatementdata.ifiscalperiod);
		clsFiscalSet objCurrentFiscalSet = getFiscalSetByAccountAndYear(
			sAccount,
			iFiscalYear,
			arrFiscalSets
		);
		
		//Opening balance:
		bdopeningbalance = objCurrentFiscalSet.m_bdopeningbalance;
		
		//Net change per period:
		bdnetchangeforperiod = objCurrentFiscalSet.getPeriodChangeByPeriod(iFiscalPeriod);
		
		//Total year to date:
		bdtotalyeartodate = objCurrentFiscalSet.getTotalYearToDataByPeriod(iFiscalPeriod);
		
		//Net change for same period in the previous year:
		boolean bPreviousYearFound = true;
		clsFiscalSet objPreviousFiscalSet = null;
		try {
			objPreviousFiscalSet = getFiscalSetByAccountAndYear(
				sAccount,
				iFiscalYear - 1,
				arrFiscalSets
			);
		} catch (Exception e) {
			//If we can't find a previous year, then we can't update any 'previous year' fields:
			bPreviousYearFound = false;
		}
		
		if (bPreviousYearFound){
			//Test the net change from same period, previous year:
			bdnetchangeforperiodpreviousyear = objPreviousFiscalSet.getPeriodChangeByPeriod(iFiscalPeriod);
			
			//Test the previous year's total to date:
			bdtotalpreviousyeartodate = objPreviousFiscalSet.getTotalYearToDataByPeriod(iFiscalPeriod);
			
			//Test the previous year's opening balance:
			bdopeningbalancepreviousyear = objPreviousFiscalSet.m_bdopeningbalance;
		}
		
		//Get the net change for the previous period:
		if (iFiscalPeriod == 1){
			if (bPreviousYearFound){
				//Get the net change for the last period of the previous year
				BigDecimal bdNetChangeLastPeriodPreviousYear = objPreviousFiscalSet.getPeriodChangeByPeriod(objPreviousFiscalSet.m_numberofperiods);
				bdnetchangeforpreviousperiod = bdNetChangeLastPeriodPreviousYear;
			}
		}else{
			bdnetchangeforpreviousperiod = objCurrentFiscalSet.getPeriodChangeByPeriod(iFiscalPeriod - 1);
		}
		
		//Get the net change for the previous period, in the previous year:
		if (iFiscalPeriod == 1){
			//We'll need to get the last period of the TWO years previous:
			boolean bTwoPreviousYearsFound = true;
			clsFiscalSet objPreviousTwoYearsFiscalSet = null;
			try {
				objPreviousTwoYearsFiscalSet = getFiscalSetByAccountAndYear(
					sAccount,
					iFiscalYear - 2,
					arrFiscalSets
				);
			} catch (Exception e) {
				//If we can't find a two years previous, then we can't update any 'two years previous' fields:
				bTwoPreviousYearsFound = false;
			}
			
			if (bTwoPreviousYearsFound){
				bdnetchangeforpreviousperiodpreviousyear = objPreviousTwoYearsFiscalSet.getPeriodChangeByPeriod(objPreviousTwoYearsFiscalSet.m_numberofperiods);
			}
		}else{
			if (bPreviousYearFound){
				bdnetchangeforpreviousperiodpreviousyear = objPreviousFiscalSet.getPeriodChangeByPeriod(iFiscalPeriod - 1);
			}
		}
		String SQL = "";
		try {
			SQL = "UPDATE " + SMTableglfinancialstatementdata.TableName
				+ " SET " + SMTableglfinancialstatementdata.bdnetchangeforperiod + " = " 
					+ ServletUtilities.clsManageBigDecimals.BigDecimalTo2DecimalSQLFormat(bdnetchangeforperiod)
				+ ", " + SMTableglfinancialstatementdata.bdnetchangeforperiodpreviousyear + " = " 
					+ ServletUtilities.clsManageBigDecimals.BigDecimalTo2DecimalSQLFormat(bdnetchangeforperiodpreviousyear)	
				+ ", " + SMTableglfinancialstatementdata.bdnetchangeforpreviousperiod + " = " 
					+ ServletUtilities.clsManageBigDecimals.BigDecimalTo2DecimalSQLFormat(bdnetchangeforpreviousperiod)
				+ ", " + SMTableglfinancialstatementdata.bdnetchangeforpreviousperiodpreviousyear + " = " 
					+ ServletUtilities.clsManageBigDecimals.BigDecimalTo2DecimalSQLFormat(bdnetchangeforpreviousperiodpreviousyear)
				+ ", " + SMTableglfinancialstatementdata.bdopeningbalance + " = " 
					+ ServletUtilities.clsManageBigDecimals.BigDecimalTo2DecimalSQLFormat(bdopeningbalance)
				+ ", " + SMTableglfinancialstatementdata.bdopeningbalancepreviousyear + " = " 
					+ ServletUtilities.clsManageBigDecimals.BigDecimalTo2DecimalSQLFormat(bdopeningbalancepreviousyear)
				+ ", " + SMTableglfinancialstatementdata.bdtotalpreviousyeartodate + " = " 
					+ ServletUtilities.clsManageBigDecimals.BigDecimalTo2DecimalSQLFormat(bdtotalpreviousyeartodate)
				+ ", " + SMTableglfinancialstatementdata.bdtotalyeartodate + " = " 
					+ ServletUtilities.clsManageBigDecimals.BigDecimalTo2DecimalSQLFormat(bdtotalyeartodate)
				
				+ " WHERE ("
					+ "(" + SMTableglfinancialstatementdata.sacctid + " = '" + sAccount + "')"
					+ " AND (" + SMTableglfinancialstatementdata.ifiscalyear + " = " + sFiscalYear + ")"
					+ " AND (" + SMTableglfinancialstatementdata.ifiscalperiod + " = " + sFiscalPeriod + ")"
				+ ")"
			;
			Statement stmt = conn.createStatement();
			stmt.execute(SQL);
		} catch (Exception e) {
			throw new Exception("Error [2019289125787] " + "updating financial statement record with SQL: '" + SQL + "' - " + e.getMessage() + ".");
		}
		
		return sMessages;
	}

	private clsFiscalSet getFiscalSetByAccountAndYear(String sAccount, int iFiscalYear, ArrayList<clsFiscalSet> arrFiscalSets) throws Exception{
		for (int i = 0; i < arrFiscalSets.size(); i++){
			if (
				(arrFiscalSets.get(i).m_sAcctID.compareToIgnoreCase(sAccount) == 0)
				&& (arrFiscalSets.get(i).m_ifiscalyear == iFiscalYear)
			){
				return arrFiscalSets.get(i);
			}
		}
		throw new Exception("Error [20192871120523] " + "Could not find loaded fiscal set for account '" 
			+ sAccount + "', fiscal year '" + Integer.toString(iFiscalYear) + ".");
	}
	private ArrayList<String>getUniqueGLAccounts(ArrayList<clsFiscalSet> arrFiscalSets){
		ArrayList<String>arrUniqueAccounts = new ArrayList<String>(0);
		String sLastAccountRead = "";
		for (int i = 0; i < arrFiscalSets.size(); i++){
			if (arrFiscalSets.get(i).m_sAcctID.compareToIgnoreCase(sLastAccountRead) != 0){
				arrUniqueAccounts.add(arrFiscalSets.get(i).m_sAcctID);
			}
			sLastAccountRead = arrFiscalSets.get(i).m_sAcctID;
		}
		
		return arrUniqueAccounts;
	}
	
	private class clsFiscalSet extends Object{
		
		public String m_sAcctID;
		public int m_ifiscalyear;
		public BigDecimal m_bdopeningbalance;
		public BigDecimal m_bdnetchangeperiod1;
		public BigDecimal m_bdnetchangeperiod2;
		public BigDecimal m_bdnetchangeperiod3;
		public BigDecimal m_bdnetchangeperiod4;
		public BigDecimal m_bdnetchangeperiod5;
		public BigDecimal m_bdnetchangeperiod6;
		public BigDecimal m_bdnetchangeperiod7;
		public BigDecimal m_bdnetchangeperiod8;
		public BigDecimal m_bdnetchangeperiod9;
		public BigDecimal m_bdnetchangeperiod10;
		public BigDecimal m_bdnetchangeperiod11;
		public BigDecimal m_bdnetchangeperiod12;
		public BigDecimal m_bdnetchangeperiod13;
		public BigDecimal m_bdnetchangeperiod14;
		public BigDecimal m_bdnetchangeperiod15;
		public int m_numberofperiods;
		
		private clsFiscalSet(){
			
		}
		
		private clsFiscalSet(
			String sAccount,
			int iFiscalYear,
			BigDecimal bdOpeningBalance,
			BigDecimal bdNetChangePeriod1,
			BigDecimal bdNetChangePeriod2,
			BigDecimal bdNetChangePeriod3,
			BigDecimal bdNetChangePeriod4,
			BigDecimal bdNetChangePeriod5,
			BigDecimal bdNetChangePeriod6,
			BigDecimal bdNetChangePeriod7,
			BigDecimal bdNetChangePeriod8,
			BigDecimal bdNetChangePeriod9,
			BigDecimal bdNetChangePeriod10,
			BigDecimal bdNetChangePeriod11,
			BigDecimal bdNetChangePeriod12,
			BigDecimal bdNetChangePeriod13,
			BigDecimal bdNetChangePeriod14,
			BigDecimal bdNetChangePeriod15,
			int iNumberOfPeriods
			){
			
			m_sAcctID = sAccount;
			m_ifiscalyear = iFiscalYear;
			m_bdopeningbalance = bdOpeningBalance;
			m_bdnetchangeperiod1 = bdNetChangePeriod1;
			m_bdnetchangeperiod2 = bdNetChangePeriod2;
			m_bdnetchangeperiod3 = bdNetChangePeriod3;
			m_bdnetchangeperiod4 = bdNetChangePeriod4;
			m_bdnetchangeperiod5 = bdNetChangePeriod5;
			m_bdnetchangeperiod6 = bdNetChangePeriod6;
			m_bdnetchangeperiod7 = bdNetChangePeriod7;
			m_bdnetchangeperiod8 = bdNetChangePeriod8;
			m_bdnetchangeperiod9 = bdNetChangePeriod9;
			m_bdnetchangeperiod10 = bdNetChangePeriod10;
			m_bdnetchangeperiod11 = bdNetChangePeriod11;
			m_bdnetchangeperiod12 = bdNetChangePeriod12;
			m_bdnetchangeperiod13 = bdNetChangePeriod13;
			m_bdnetchangeperiod14 = bdNetChangePeriod14;
			m_bdnetchangeperiod15 = bdNetChangePeriod15;
			m_numberofperiods = iNumberOfPeriods;
		}
		public BigDecimal getPeriodChangeByPeriod(int iPeriod) throws Exception{
			switch (iPeriod){
			case 1: return m_bdnetchangeperiod1;
			case 2: return m_bdnetchangeperiod2;
			case 3: return m_bdnetchangeperiod3;
			case 4: return m_bdnetchangeperiod4;
			case 5: return m_bdnetchangeperiod5;
			case 6: return m_bdnetchangeperiod6;
			case 7: return m_bdnetchangeperiod7;
			case 8: return m_bdnetchangeperiod8;
			case 9: return m_bdnetchangeperiod9;
			case 10: return m_bdnetchangeperiod10;
			case 11: return m_bdnetchangeperiod11;
			case 12: return m_bdnetchangeperiod12;
			case 13: return m_bdnetchangeperiod13;
			case 14: return m_bdnetchangeperiod14;
			case 15: return m_bdnetchangeperiod15;
			default: throw new Exception("Error [20192871143237] " + "no fiscal set field for period " + Integer.toString(iPeriod));
			}
		}
	
		public BigDecimal getTotalYearToDataByPeriod(int iPeriod) throws Exception{
			switch (iPeriod){
			case 1: return m_bdnetchangeperiod1;
			case 2: return m_bdnetchangeperiod1.add(m_bdnetchangeperiod2);
			case 3: return m_bdnetchangeperiod1.add(m_bdnetchangeperiod2).add(m_bdnetchangeperiod3);
			case 4: return m_bdnetchangeperiod1.add(m_bdnetchangeperiod2).add(m_bdnetchangeperiod3).add(m_bdnetchangeperiod4);
			case 5: return m_bdnetchangeperiod1.add(m_bdnetchangeperiod2).add(m_bdnetchangeperiod3).add(m_bdnetchangeperiod4).add(m_bdnetchangeperiod5);
			case 6: return m_bdnetchangeperiod1.add(m_bdnetchangeperiod2).add(m_bdnetchangeperiod3).add(m_bdnetchangeperiod4).add(m_bdnetchangeperiod5).add(
				m_bdnetchangeperiod6);
			case 7: return m_bdnetchangeperiod1.add(m_bdnetchangeperiod2).add(m_bdnetchangeperiod3).add(m_bdnetchangeperiod4).add(m_bdnetchangeperiod5).add(
					m_bdnetchangeperiod6).add(m_bdnetchangeperiod7);
			case 8: return m_bdnetchangeperiod1.add(m_bdnetchangeperiod2).add(m_bdnetchangeperiod3).add(m_bdnetchangeperiod4).add(m_bdnetchangeperiod5).add(
					m_bdnetchangeperiod6).add(m_bdnetchangeperiod7).add(m_bdnetchangeperiod8);
			case 9: return m_bdnetchangeperiod1.add(m_bdnetchangeperiod2).add(m_bdnetchangeperiod3).add(m_bdnetchangeperiod4).add(m_bdnetchangeperiod5).add(
					m_bdnetchangeperiod6).add(m_bdnetchangeperiod7).add(m_bdnetchangeperiod8).add(m_bdnetchangeperiod9);
			case 10: return m_bdnetchangeperiod1.add(m_bdnetchangeperiod2).add(m_bdnetchangeperiod3).add(m_bdnetchangeperiod4).add(m_bdnetchangeperiod5).add(
					m_bdnetchangeperiod6).add(m_bdnetchangeperiod7).add(m_bdnetchangeperiod8).add(m_bdnetchangeperiod9).add(m_bdnetchangeperiod10);
			case 11: return m_bdnetchangeperiod1.add(m_bdnetchangeperiod2).add(m_bdnetchangeperiod3).add(m_bdnetchangeperiod4).add(m_bdnetchangeperiod5).add(
					m_bdnetchangeperiod6).add(m_bdnetchangeperiod7).add(m_bdnetchangeperiod8).add(m_bdnetchangeperiod9).add(m_bdnetchangeperiod10).add(
					m_bdnetchangeperiod11);
			case 12: return m_bdnetchangeperiod1.add(m_bdnetchangeperiod2).add(m_bdnetchangeperiod3).add(m_bdnetchangeperiod4).add(m_bdnetchangeperiod5).add(
					m_bdnetchangeperiod6).add(m_bdnetchangeperiod7).add(m_bdnetchangeperiod8).add(m_bdnetchangeperiod9).add(m_bdnetchangeperiod10).add(
					m_bdnetchangeperiod11).add(m_bdnetchangeperiod12);
			case 13: return m_bdnetchangeperiod1.add(m_bdnetchangeperiod2).add(m_bdnetchangeperiod3).add(m_bdnetchangeperiod4).add(m_bdnetchangeperiod5).add(
					m_bdnetchangeperiod6).add(m_bdnetchangeperiod7).add(m_bdnetchangeperiod8).add(m_bdnetchangeperiod9).add(m_bdnetchangeperiod10).add(
					m_bdnetchangeperiod11).add(m_bdnetchangeperiod12).add(m_bdnetchangeperiod13);
			case 14: return m_bdnetchangeperiod1.add(m_bdnetchangeperiod2).add(m_bdnetchangeperiod3).add(m_bdnetchangeperiod4).add(m_bdnetchangeperiod5).add(
					m_bdnetchangeperiod6).add(m_bdnetchangeperiod7).add(m_bdnetchangeperiod8).add(m_bdnetchangeperiod9).add(m_bdnetchangeperiod10).add(
					m_bdnetchangeperiod11).add(m_bdnetchangeperiod12).add(m_bdnetchangeperiod13).add(m_bdnetchangeperiod14);
			case 15: return m_bdnetchangeperiod1.add(m_bdnetchangeperiod2).add(m_bdnetchangeperiod3).add(m_bdnetchangeperiod4).add(m_bdnetchangeperiod5).add(
					m_bdnetchangeperiod6).add(m_bdnetchangeperiod7).add(m_bdnetchangeperiod8).add(m_bdnetchangeperiod9).add(m_bdnetchangeperiod10).add(
					m_bdnetchangeperiod11).add(m_bdnetchangeperiod12).add(m_bdnetchangeperiod13).add(m_bdnetchangeperiod14).add(m_bdnetchangeperiod15);
			default: throw new Exception("Error [20192871143238] " + "no fiscal set field for period " + Integer.toString(iPeriod));
			}
		}
	}
	
	public void readMatchingRecordsets(
			Connection conn, 
			Connection cnACCPAC, 
			String sFiscalYear, 
			String sFiscalPeriod, 
			String sAccount,
			PrintWriter out,
			ServletContext context,
			String sDBID
			) throws Exception{
			
			ArrayList<clsTransactionLine>arrACCPACLines = new ArrayList<clsTransactionLine>(0);
			ArrayList<clsTransactionLine>arrSMCPLines = new ArrayList<clsTransactionLine>(0);
			
			//Load the ACCPAC array:
			String sACCPACSQL = "SELECT * FROM GLPOST"
				+ " WHERE ("
					+ "(ACCTID = '" + sAccount + "')"
					+ " AND (FISCALYR = " + sFiscalYear + ")"
					+ " AND (FISCALPERD = " + sFiscalPeriod + ")"
				+ ")"
				+ " ORDER BY TRANSAMT, DOCDATE"
			;
			try {
				Statement stmtACCPAC = cnACCPAC.createStatement();
				ResultSet rsACCPAC = stmtACCPAC.executeQuery(sACCPACSQL);
				while (rsACCPAC.next()){
					clsTransactionLine tl = new clsTransactionLine(
						rsACCPAC.getString("ACCTID").trim(),
						rsACCPAC.getInt("FISCALYR"),
						rsACCPAC.getInt("FISCALPERD"),
						rsACCPAC.getInt("BATCHNBR"),
						rsACCPAC.getInt("ENTRYNBR"),
						rsACCPAC.getInt("TRANSNBR"),
						convertACCPACLongDateToString(rsACCPAC.getLong("DOCDATE"), false),
						FormatSQLStatement(rsACCPAC.getString("SRCELEDGER").trim()),
						rsACCPAC.getBigDecimal("TRANSAMT"),
						FormatSQLStatement(rsACCPAC.getString("JNLDTLDESC").trim()),
						convertACCPACLongDateToString(rsACCPAC.getLong("JRNLDATE"), false),
						""
					);
					arrACCPACLines.add(tl);
				}
				rsACCPAC.close();
			} catch (Exception e) {
				throw new Exception("Error [20192941534413] " + "Error reading ACCPAC records with SQL: '" + sACCPACSQL + "' - " + e.getMessage() + ".");
			}
				
			//Next load the SMCP array:
			String sSMCPSQL = "SELECT * FROM " + SMTablegltransactionlines.TableName
				+ " WHERE ("
					+ "(" + SMTablegltransactionlines.sacctid + " = '" + sAccount + "')"
					+ " AND (" + SMTablegltransactionlines.ifiscalyear + " = " + sFiscalYear + ")"
					+ " AND (" + SMTablegltransactionlines.ifiscalperiod + " = " + sFiscalPeriod + ")"
				+ ")"
				+ " ORDER BY " + SMTablegltransactionlines.bdamount + ", " + SMTablegltransactionlines.dattransactiondate 
			;
			try {
				ResultSet rsSMCP = ServletUtilities.clsDatabaseFunctions.openResultSet(sSMCPSQL, conn);
				while (rsSMCP.next()){
					clsTransactionLine tl = new clsTransactionLine(
						FormatSQLStatement(rsSMCP.getString(SMTablegltransactionlines.sacctid)),
						rsSMCP.getInt(SMTablegltransactionlines.ifiscalyear),
						rsSMCP.getInt(SMTablegltransactionlines.ifiscalperiod),
						rsSMCP.getLong(SMTablegltransactionlines.loriginalbatchnumber),
						rsSMCP.getLong(SMTablegltransactionlines.loriginalentrynumber),
						rsSMCP.getLong(SMTablegltransactionlines.loriginallinenumber),
						ServletUtilities.clsDateAndTimeConversions.resultsetDateStringToFormattedString(
							rsSMCP.getString(SMTablegltransactionlines.dattransactiondate), SMUtilities.DATE_FORMAT_FOR_DISPLAY, "00/00/0000"),
						FormatSQLStatement(rsSMCP.getString(SMTablegltransactionlines.ssourceledger)),
						rsSMCP.getBigDecimal(SMTablegltransactionlines.bdamount),
						FormatSQLStatement(rsSMCP.getString(SMTablegltransactionlines.sdescription)),
						ServletUtilities.clsDateAndTimeConversions.resultsetDateStringToFormattedString(
							rsSMCP.getString(SMTablegltransactionlines.datpostingdate), SMUtilities.DATE_FORMAT_FOR_DISPLAY, "00/00/0000"),
						FormatSQLStatement(rsSMCP.getString(SMTablegltransactionlines.sSourceledgertransactionlink))
					);
					arrSMCPLines.add(tl);
				}
				rsSMCP.close();
			} catch (Exception e) {
				throw new Exception("Error [20192941534414] " + "Error reading SMCP records with SQL: '" + sSMCPSQL + "' - " + e.getMessage() + ".");
			}
			
			//Now, try to match up the lines, and send them to be printed:
			
			//First, start the overall table:
			out.println("<TABLE WIDTH=100 style = \""
				+ " border: 1px solid black; border-collapse:collapse; font-size:small; font-family:Arial; color:black;"
				+ " \" >" + "\n");
			
			out.println("  <TR>" + "\n");
			out.println("    <TD ALIGN=CENTER COLSPAN=9 style = \" color:white; background-color:black; \" >" + " <B>ACCPAC TRANSACTIONS</B>"+ "</TD>" + "\n");
			out.println("    <TD ALIGN=CENTER COLSPAN=8 style = \" color:white; background-color:black; \" >" + " <B>SMCP TRANSACTIONS</B>"+ "</TD>" + "\n");
			out.println("  </TR>" + "\n");
			
			//Headings:
			String s = "";
			s += "  <TR style = \" background-color: #C2E0FF; \" >" + "\n";

			s += "    <TD ALIGN=RIGHT>" + "<B>Row</B>" + "</TD>" + "\n";
			
			s += "    <TD ALIGN=RIGHT>" + "<B>Batch</B>" + "</TD>" + "\n";
			s += "    <TD ALIGN=RIGHT>" + "<B>Entry</B>" + "</TD>" + "\n";
			s += "    <TD ALIGN=RIGHT>" + "<B>Line</B>" + "</TD>" + "\n";
			s += "    <TD >" + "<B>Src Ledger</B>" + "</TD>" + "\n";
			s += "    <TD >" + "<B>Description</B>" + "</TD>" + "\n";
			s += "    <TD >" + "<B>Trans Date</B>" + "</TD>" + "\n";
			s += "    <TD >" + "<B>Post Date</B>" + "</TD>" + "\n";
			s += "    <TD ALIGN=RIGHT style = \" border-right-style: solid; padding: 4px; \">" + "<B>Amt</B>" + "</TD>" + "\n";
			s += "    <TD ALIGN=RIGHT>" + "<B>Batch</B>" + "</TD>" + "\n";
			s += "    <TD ALIGN=RIGHT>" + "<B>Entry</B>" + "</TD>" + "\n";
			s += "    <TD ALIGN=RIGHT>" + "<B>Line</B>" + "</TD>" + "\n";
			s += "    <TD >" + "<B>Src Ledger</B>" + "</TD>" + "\n";
			s += "    <TD >" + "<B>Description</B>" + "</TD>" + "\n";
			s += "    <TD >" + "<B>Trans Date</B>" + "</TD>" + "\n";
			s += "    <TD >" + "<B>Post Date</B>" + "</TD>" + "\n";
			s += "    <TD ALIGN=RIGHT>" + "<B>Amt</B>" + "</TD>" + "\n";
			
			s += "  </TR>" + "\n";
			out.println(s);
			
			displayTransactionLines(arrACCPACLines, arrSMCPLines, out, context, sDBID);
			
			//Display totals:
			BigDecimal bdACCPACTotal = new BigDecimal("0.00");
			BigDecimal bdSMCPTotal = new BigDecimal("0.00");
			
			for (int i = 0; i < arrACCPACLines.size(); i++){
				bdACCPACTotal = bdACCPACTotal.add(arrACCPACLines.get(i).m_bdamt);
			}
			for (int i = 0; i < arrSMCPLines.size(); i++){
				bdSMCPTotal = bdSMCPTotal.add(arrSMCPLines.get(i).m_bdamt);
			}
			
			s = "";
			s += "  <TR style = \" background-color: #C2E0FF; \" >" + "\n";
			s += "    <TD ALIGN=RIGHT COLSPAN=9 style = \" border-right-style: solid; padding: 4px; \">" + "<B>"
				+ ServletUtilities.clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(bdACCPACTotal) + "</B>" + "</TD>" + "\n";
			s += "    <TD ALIGN=RIGHT COLSPAN=8>" + "<B>"
				+ ServletUtilities.clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(bdSMCPTotal) + "</B>" + "</TD>" + "\n";
			s += "  </TR>" + "\n";
			out.println(s);
			
			//Close the parent table:
			out.println("</TABLE>" + "\n");
			
			return;
			
		}

	private void displayTransactionLines(
		ArrayList<clsTransactionLine>arrACCPAC
		, ArrayList<clsTransactionLine>arrSMCP
		, PrintWriter out
		, ServletContext context
		, String sDBID
		) throws Exception{
		
		int iACCPACCounter = 0;
		int iSMCPCounter = 0;
		int iRowCounter = 0;
		
		//Print the line with the lowest amount, then move to the next.
		//If the two lines match, then print both:
		while (true){
			
			//If we've printed all the ACCPAC lines, then print any remaining SMCP lines:
			if (iACCPACCounter >= arrACCPAC.size()){
				iRowCounter++;
				if(iSMCPCounter < arrSMCP.size()){
					printPairedLines(null, arrSMCP.get(iSMCPCounter), out, iRowCounter, context, sDBID);
					iSMCPCounter++;
					continue;
				}else{
					break;
				}
			}
			
			//If we've printed all the SMCP lines, then print any remaining ACCPAC lines:
			if (iSMCPCounter >= arrSMCP.size()){
				iRowCounter++;
				if (iACCPACCounter < arrACCPAC.size()){
					printPairedLines(arrACCPAC.get(iACCPACCounter), null, out, iRowCounter, context, sDBID);
					iACCPACCounter++;
					continue;
				}else{
					break;
				}
			}
			if ((iACCPACCounter >= arrACCPAC.size()) && (iSMCPCounter >= arrSMCP.size())){
				break;
			}
			
			//But if we still have ACCPAC lines AND SMCP lines left, work through them:
			clsTransactionLine lineACCPAC = arrACCPAC.get(iACCPACCounter);
			clsTransactionLine lineSMCP = arrSMCP.get(iSMCPCounter);
			
			//If the lines are equal, print both:
			if (lineACCPAC.m_bdamt.compareTo(lineSMCP.m_bdamt) == 0){
				iRowCounter++;
				printPairedLines(lineACCPAC, lineSMCP, out, iRowCounter, context, sDBID);
				iACCPACCounter++;
				iSMCPCounter++;
			}
			//If the ACCPAC line is less than the SMCP line, print it:
			if (lineACCPAC.m_bdamt.compareTo(lineSMCP.m_bdamt) < 0){
				iRowCounter++;
				printPairedLines(lineACCPAC, null, out, iRowCounter, context, sDBID);
				iACCPACCounter++;
			}
			//If the SMCP line is less than the ACCPAC line, print it:
			if (lineSMCP.m_bdamt.compareTo(lineACCPAC.m_bdamt) < 0){
				iRowCounter++;
				printPairedLines(null, lineSMCP, out, iRowCounter, context, sDBID);
				iSMCPCounter++;
			}
			if ((iACCPACCounter >= arrACCPAC.size()) && (iSMCPCounter >= arrSMCP.size())){
				break;
			}
		}
	}
	
	private void printPairedLines(
		clsTransactionLine ACCPACLine
		, clsTransactionLine SMCPLine
		, PrintWriter out
		, int iRowNumber
		, ServletContext context
		, String sDBID
		){
		String s = "";
		String sBackgroundColor = "#DCDCDC";
		if ((iRowNumber % 2) == 0){
			sBackgroundColor = "#FFFFFF";
		}
		s += "  <TR style = \" background-color:" + sBackgroundColor + "; \" >" + "\n";
		
		if (ACCPACLine == null){
			s += "    <TD ALIGN=RIGHT >" + Integer.toString(iRowNumber) + "</TD>" + "\n";
			s += "    <TD ALIGN=RIGHT style = \" background-color:red; \" >" + "&nbsp;" + "</TD>" + "\n";
			s += "    <TD ALIGN=RIGHT style = \" background-color:red; \" >" + "&nbsp;"  + "</TD>" + "\n";
			s += "    <TD ALIGN=RIGHT style = \" background-color:red; \" >" + "&nbsp;"  + "</TD>" + "\n";
			s += "    <TD style = \" background-color:red; \" >" + "&nbsp;"  + "</TD>" + "\n";
			s += "    <TD style = \" background-color:red; \"  >" + "&nbsp;"  + "</TD>" + "\n";
			s += "    <TD style = \" background-color:red; \" >" + "&nbsp;"  + "</TD>" + "\n";
			s += "    <TD style = \" background-color:red; \" >" + "&nbsp;"  + "</TD>" + "\n";
			s += "    <TD ALIGN=RIGHT style = \" background-color:red; border-right-style: solid; padding: 4px; \" >" + "&nbsp;"  + "</TD>" + "\n";
		}else{
			s += "    <TD ALIGN=RIGHT>" + Integer.toString(iRowNumber) + "</TD>" + "\n";
			s += "    <TD ALIGN=RIGHT>" + Long.toString(ACCPACLine.m_loriginalbatchnumber) + "</TD>" + "\n";
			s += "    <TD ALIGN=RIGHT>" + Long.toString(ACCPACLine.m_loriginalentrynumber) + "</TD>" + "\n";
			s += "    <TD ALIGN=RIGHT>" + Long.toString(ACCPACLine.m_loriginallinenumber) + "</TD>" + "\n";
			s += "    <TD >" + ACCPACLine.m_ssourceledger + "</TD>" + "\n";
			s += "    <TD >" + ACCPACLine.m_sdescription + "</TD>" + "\n";
			s += "    <TD >" + ACCPACLine.m_stransactiondate + "</TD>" + "\n";
			s += "    <TD >" + ACCPACLine.m_spostingdate + "</TD>" + "\n";
			s += "    <TD ALIGN=RIGHT style = \" border-right-style: solid; padding: 4px; \" >" + ServletUtilities.clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(ACCPACLine.m_bdamt) + "</TD>" + "\n";
		}
		
		if (SMCPLine == null){
			s += "    <TD ALIGN=RIGHT style = \" background-color:red; \" >" + "&nbsp;" + "</TD>" + "\n";
			s += "    <TD ALIGN=RIGHT style = \" background-color:red; \" >" + "&nbsp;"  + "</TD>" + "\n";
			s += "    <TD ALIGN=RIGHT style = \" background-color:red; \" >" + "&nbsp;"  + "</TD>" + "\n";
			s += "    <TD style = \" background-color:red; \" >" + "&nbsp;"  + "</TD>" + "\n";
			s += "    <TD style = \" background-color:red; \" >" + "&nbsp;"  + "</TD>" + "\n";
			s += "    <TD style = \" background-color:red; \" >" + "&nbsp;"  + "</TD>" + "\n";
			s += "    <TD style = \" background-color:red; \" >" + "&nbsp;"  + "</TD>" + "\n";
			s += "    <TD ALIGN=RIGHT style = \" background-color:red; \" >" + "&nbsp;"  + "</TD>" + "\n";
		}else{
			String sTransactionLink;
			try {
				sTransactionLink = GLTransactionLinks.getSubledgerTransactionLink(
					SMCPLine.m_ssourceledger, 
					SMCPLine.m_ssourceledgertransactionID, 
					context, 
					sDBID, 
					ServletUtilities.clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(SMCPLine.m_bdamt)
				);
			} catch (Exception e) {
				//Just skip the link in case of error:
				sTransactionLink = ServletUtilities.clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(SMCPLine.m_bdamt);
			}
			String sBatchnumber = Long.toString(SMCPLine.m_loriginalbatchnumber);
			String sEntrynumber = Long.toString(SMCPLine.m_loriginalentrynumber);
			String sLinenumber = Long.toString(SMCPLine.m_loriginallinenumber);
			if (SMCPLine.m_loriginalbatchnumber < 0L){
				sBatchnumber = "0";
				sEntrynumber = "0";
				sLinenumber = "0";
				sTransactionLink = ServletUtilities.clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(SMCPLine.m_bdamt);
			}
			s += "    <TD ALIGN=RIGHT>" + sBatchnumber + "</TD>" + "\n";
			s += "    <TD ALIGN=RIGHT>" + sEntrynumber + "</TD>" + "\n";
			s += "    <TD ALIGN=RIGHT>" + sLinenumber + "</TD>" + "\n";
			s += "    <TD >" + SMCPLine.m_ssourceledger + "</TD>" + "\n";
			s += "    <TD >" + SMCPLine.m_sdescription + "</TD>" + "\n";
			s += "    <TD >" + SMCPLine.m_stransactiondate + "</TD>" + "\n";
			s += "    <TD >" + SMCPLine.m_spostingdate + "</TD>" + "\n";
			s += "    <TD ALIGN=RIGHT>" + sTransactionLink + "</TD>" + "\n";
		}
		
		s += "  </TR>" + "\n";
		out.println(s);
		
		
	}
	
	private class clsTransactionLine extends Object{
		
		@SuppressWarnings("unused")
		public String m_sAcctID;
		@SuppressWarnings("unused")
		public int m_ifiscalyear;
		@SuppressWarnings("unused")
		public int m_ifiscalperiod;
		public long m_loriginalbatchnumber;
		public long m_loriginalentrynumber;
		public long m_loriginallinenumber;
		public String m_stransactiondate;
		public String m_ssourceledger;
		public BigDecimal m_bdamt;
		public String m_sdescription;
		public String m_spostingdate;
		public String m_ssourceledgertransactionID;
		
		private clsTransactionLine(){
			
		}
		
		private clsTransactionLine(
			String sAccount,
			int iFiscalYear,
			int iFiscalPeriod,
			long lOriginalBatchNumber,
			long lOriginalEntryNumber,
			long lOriginalLineNumber,
			String sTransactionDate,
			String sSourceLedger,
			BigDecimal bdAmount,
			String sDescription,
			String sPostingDate,
			String sSourceLedgerTransactionID
			){
			
			m_sAcctID = sAccount;
			m_ifiscalyear = iFiscalYear;
			m_ifiscalperiod = iFiscalPeriod;
			m_loriginalbatchnumber = lOriginalBatchNumber;
			m_loriginalentrynumber = lOriginalEntryNumber;
			m_loriginallinenumber = lOriginalLineNumber;
			m_stransactiondate = sTransactionDate;
			m_ssourceledger = sSourceLedger;
			m_bdamt = bdAmount;
			m_sdescription = sDescription;
			m_spostingdate = sPostingDate;
			m_ssourceledgertransactionID = sSourceLedgerTransactionID;
		}

	}
	private static String convertACCPACLongDateToString(long lDate, boolean bUseNowForNulls){

		if (lDate == 0L){
			if (bUseNowForNulls){
				return now("MM/dd/yyyy");
			}else{
				return "00/00/0000";
			}
		}

		String sDate = Long.toString(lDate);
		return sDate.substring(4, 6) + "/" + sDate.substring(6, 8) + "/" + sDate.substring(0, 4);
	}
	public static String FormatSQLStatement(String s) {

		if (s != null){
			s = s.replace("'", "''");
			s = s.replace("\\", "\\\\");
			//s = s.replace("\"", "\"\"");
		}

		return s;
	}
	public static String now(String sDateFormat) {
		Calendar cal = Calendar.getInstance();
		SimpleDateFormat sdf = new SimpleDateFormat(sDateFormat);
		return sdf.format(cal.getTime());

		/*
		 Samples:
		 System.out.println(DateUtils.now("dd MMMMM yyyy"));
		 System.out.println(DateUtils.now("yyyyMMdd"));
		 System.out.println(DateUtils.now("dd.MM.yy"));
		 System.out.println(DateUtils.now("MM/dd/yy"));
		 System.out.println(DateUtils.now("yyyy.MM.dd G 'at' hh:mm:ss z"));
		 System.out.println(DateUtils.now("EEE, MMM d, ''yy"));
		 System.out.println(DateUtils.now("h:mm a"));
		 System.out.println(DateUtils.now("H:mm:ss:SSS"));
		 System.out.println(DateUtils.now("K:mm a,z"));
		 System.out.println(DateUtils.now("yyyy.MMMMM.dd GGG hh:mm aaa"));
		 */
	}
}
