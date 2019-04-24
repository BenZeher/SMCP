package smgl;

import java.util.ArrayList;

import SMDataDefinition.SMTableapbatchentries;
import SMDataDefinition.SMTableaptransactions;
import smar.ARDocumentTypes;
import smic.ICEntryTypes;

public class GLSourceLedgers {

	public static final int SOURCE_LEDGER_AP = 0;
	public static final int SOURCE_LEDGER_AR = 1;
	public static final int SOURCE_LEDGER_FA = 2;
	public static final int SOURCE_LEDGER_JOURNAL_ENTRY = 3;
	public static final int SOURCE_LEDGER_IC = 4;
	public static final int SOURCE_LEDGER_PAYROLL = 5;
	
	public static final int NO_OF_SOURCELEDGERS = 6;
	
	public static final String SOURCE_LEDGER_AND_TYPE_DELIMITER = " - ";

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
	
	public static ArrayList<String>getSourceTypes(){
		
		ArrayList<String>arrSourceTypes = new ArrayList<String>(0);

		//AP
		for (int i = 0; i < SMTableaptransactions.NUMBER_OF_AP_TRANSACTION_TYPES; i++){
			arrSourceTypes.add(getSourceLedgerDescription(SOURCE_LEDGER_AP) + SOURCE_LEDGER_AND_TYPE_DELIMITER + SMTableapbatchentries.getDocType(i));	
		}

		//AR:
		for (int i = 0; i < ARDocumentTypes.NUMBER_OF_AR_DOCUMENT_TYPES; i++){
			arrSourceTypes.add(getSourceLedgerDescription(SOURCE_LEDGER_AR) + SOURCE_LEDGER_AND_TYPE_DELIMITER + ARDocumentTypes.getSourceTypes(i));	
		}
		
		//FA:
		arrSourceTypes.add("FA" + SOURCE_LEDGER_AND_TYPE_DELIMITER + "SS");
		
		//JE:
		arrSourceTypes.add("JE" + SOURCE_LEDGER_AND_TYPE_DELIMITER + "JE");
		
		//IC:
		for (int i = 0; i < ICEntryTypes.NUMBER_OF_ENTRY_TYPES; i++){
			arrSourceTypes.add(getSourceLedgerDescription(SOURCE_LEDGER_IC) + SOURCE_LEDGER_AND_TYPE_DELIMITER + ICEntryTypes.getSourceTypes(i));
		}
		
		//PR:
		arrSourceTypes.add("PR" + SOURCE_LEDGER_AND_TYPE_DELIMITER + "PR");
		
		return arrSourceTypes;
	}
	
}
