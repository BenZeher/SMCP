package smgl;

public class GLSourceLedgers {

	public static final int SOURCE_LEDGER_AR = 0;
	public static final int SOURCE_LEDGER_AP = 1;
	public static final int SOURCE_LEDGER_JOURNAL_ENTRY = 2;
	public static final int SOURCE_LEDGER_IC = 3;
	public static final int SOURCE_LEDGER_FA = 4;
	public static final int SOURCE_LEDGER_PAYROLL = 5;
	
	public static final int NO_OF_SOURCELEDGERS = 6;

	public static String getSourceLedgerDescription(int iSourceLedger){
		
		switch(iSourceLedger){
		case SOURCE_LEDGER_AR:
			return "AR";
		case SOURCE_LEDGER_AP:
			return "AP";
		case SOURCE_LEDGER_JOURNAL_ENTRY:
			return "JE";
		case SOURCE_LEDGER_IC:
			return "IC";
		case SOURCE_LEDGER_FA:
			return "FA";
		case SOURCE_LEDGER_PAYROLL:
			return "PR";
		default:
			return "NA";
		}
	}
	
}
