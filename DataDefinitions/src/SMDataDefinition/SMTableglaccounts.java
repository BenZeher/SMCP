package SMDataDefinition;

public class SMTableglaccounts {
	public static String TableName = "glaccounts";
	
	//Field names:
	public static String sAcctID = "sacctid";
	public static String sFormattedAcct = "sformattedacctid";
	public static String sDesc = "sdescription";
	public static String sAcctType = "stype";
	public static String lActive = "lactive";
	public static String iCostCenterID = "icostcenterid";
	public static String iallowaspoexpense = "iallowaspoexpense";
	public static String iaddedbyACCPACconversion = "iaddedbyACCPACconversion";
	public static String lstructureid = "lstructureid";
	public static String laccountgroupid = "laccountgroupid";
	public static String bdannualbudget = "bdannualbudget";
	public static String inormalbalancetype = "inormalbalancetype";  //ACCTBAL in ACCPAC
	//public static String iconsolidated = "iconsolidated";  //CONSLDSW in ACCPAC
	
	
	//Field lengths:
	public static int sAcctIDLength = 45;
	public static int sFormattedAcctLength = 60;
	public static int sDescLength = 60;
	public static int sAcctTypeLength = 1;
	
	public static int bdannualbudgetScale = 2;
	
	public static final int NORMAL_BALANCE_TYPE_DEBIT = 1;
	public static final int NORMAL_BALANCE_TYPE_CREDIT = 2;

	/*
	public static final int CONSOLIDATED_SWITCH_FALSE = 0;
	public static final int CONSOLIDATED_SWITCH_TRUE = 1;
	*/
	public String getNormalBalanceDescription(int iNormalBalanceType){
		
		switch (iNormalBalanceType){
		case NORMAL_BALANCE_TYPE_DEBIT:
			return "Debit";
		case NORMAL_BALANCE_TYPE_CREDIT:
			return "Credit";
		default:
			return "Debit";
		}
	}
	/*
	public String getConsolidationTypeDescription(int iConsolidationType){
		
		switch (iConsolidationType){
		case CONSOLIDATED_SWITCH_FALSE:
			return "Detail";
		case CONSOLIDATED_SWITCH_TRUE:
			return "Consolidated";
		default:
			return "Detail";
		}
	}
	*/
}
