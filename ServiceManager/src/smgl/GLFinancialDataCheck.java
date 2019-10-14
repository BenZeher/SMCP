package smgl;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.ResultSet;
import java.util.ArrayList;

import SMDataDefinition.SMTableglfinancialstatementdata;
import SMDataDefinition.SMTableglfiscalperiods;
import SMDataDefinition.SMTableglfiscalsets;
import ServletUtilities.clsDatabaseFunctions;

public class GLFinancialDataCheck extends java.lang.Object{
	
	
	public GLFinancialDataCheck(
        ) {
	}

	public String checkRecords(
		String sAccount,
		String sStartingFiscalYear,
		Connection conn
		) throws Exception{
		
		ArrayList<clsFiscalSet>arrFiscalSets = new ArrayList<clsFiscalSet>(0);
		String sMessages = "";
		
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
			SQL += " AND (" + SMTableglfiscalsets.sAcctID + " = '" + sAccount + "')";
		}
		
		if (sStartingFiscalYear.compareToIgnoreCase("") != 0){
			SQL += " AND (" + SMTableglfiscalsets.ifiscalyear + " >= " + sStartingFiscalYear + ")";
		}
		
		SQL += ")"
			+ " ORDER BY " + SMTableglfiscalsets.sAcctID + ", " + SMTableglfiscalsets.ifiscalyear
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

		for(int i = 0; i < arrFiscalSets.size(); i++){
			sMessages += checkSingleFiscalSet(arrFiscalSets.get(i), conn, arrFiscalSets);
		}
		return sMessages;
	}

	private String checkSingleFiscalSet(clsFiscalSet objFiscalSet, Connection conn, ArrayList<clsFiscalSet>arrFiscalSets) throws Exception{
		String sMessages = "";

		//Get all the financial statement data that could be affected by this fiscal set:
		String SQL = "";
		SQL = "SELECT * FROM " + SMTableglfinancialstatementdata.TableName
			+ " WHERE ("
				+ "(" + SMTableglfinancialstatementdata.ifiscalyear + " >= " + Integer.toString(objFiscalSet.m_ifiscalyear) + ")"
				+ " AND (" + SMTableglfinancialstatementdata.sacctid + " = '" + objFiscalSet.m_sAcctID + "')"
			+ ") ORDER BY " + SMTableglfinancialstatementdata.ifiscalyear + ", " + SMTableglfinancialstatementdata.ifiscalperiod
		;
		try {
			ResultSet rsFinancials = clsDatabaseFunctions.openResultSet(SQL, conn);
			while(rsFinancials.next()){
				sMessages += checkSingleFinancialRecord(rsFinancials, arrFiscalSets);
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
		//Get the current matching fiscal set:
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
		
		/*
		public static String sacctid = "sacctid";
		public static String ifiscalyear = "ifiscalyear";
		public static String ifiscalperiod = "ifiscalperiod";
		public static String bdopeningbalance = "bdopeningbalance";
		public static String bdnetchangeforperiod = "bdnetchangeforperiod";
		public static String bdtotalyeartodate = "bdtotalyeartodate";
		public static String bdnetchangeforperiodpreviousyear = "bdnetchangeforperiodpreviousyear";
		public static String bdtotalpreviousyeartodate = "bdtotalpreviousyeartodate";
		public static String bdopeningbalancepreviousyear = "bdopeningbalancepreviousyear";
		public static String bdnetchangeforpreviousperiod = "bdnetchangeforpreviousperiod";
		public static String bdnetchangeforpreviousperiodpreviousyear = "bdnetchangeforpreviousperiodpreviousyear";
		*/
		
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
}
