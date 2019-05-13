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
		
		//IC:
		for (int i = 0; i < ICEntryTypes.NUMBER_OF_ENTRY_TYPES; i++){
			arrSourceTypes.add(getSourceLedgerDescription(SOURCE_LEDGER_IC) + SOURCE_LEDGER_AND_TYPE_DELIMITER + ICEntryTypes.getSourceTypes(i));
		}
		
		//JE:
		arrSourceTypes.add("JE" + SOURCE_LEDGER_AND_TYPE_DELIMITER + "JE");
		
		//PR:
		arrSourceTypes.add("PR" + SOURCE_LEDGER_AND_TYPE_DELIMITER + "PR");
		
		return arrSourceTypes;
	}
	
	public static ArrayList<String>getSourceTypeDescriptions(){
		ArrayList<String>arrSourceTypeDescriptions = new ArrayList<String>(0);
		
		//AP
		for (int i = 0; i < SMTableaptransactions.NUMBER_OF_AP_TRANSACTION_TYPES; i++){
			arrSourceTypeDescriptions.add("<B>" + getSourceLedgerDescription(SOURCE_LEDGER_AP) + SOURCE_LEDGER_AND_TYPE_DELIMITER + SMTableapbatchentries.getDocType(i) + "</B>"
				+ " = <I>" + getSourceLedgerDescription(SOURCE_LEDGER_AP) + " " + SMTableapbatchentries.getDocumentTypeLabel(i) + "</I>"
			);	
		}
		
		//AR
		for (int i = 0; i < ARDocumentTypes.NUMBER_OF_AR_DOCUMENT_TYPES; i++){
			arrSourceTypeDescriptions.add("<B>" + getSourceLedgerDescription(SOURCE_LEDGER_AR) + SOURCE_LEDGER_AND_TYPE_DELIMITER + ARDocumentTypes.getSourceTypes(i) + "</B>"
				+ " = <I>" + getSourceLedgerDescription(SOURCE_LEDGER_AR) + " " + ARDocumentTypes.Get_Document_Type_Label(i) + "</I>"
			);	
		}
		
		//FA
		arrSourceTypeDescriptions.add("<B>" + "FA" + SOURCE_LEDGER_AND_TYPE_DELIMITER + "SS" + "</B>"
			+ " = <I>" + "FA Periodic Depreciation" + "</I>"
		);
		
		//IC
		for (int i = 0; i < ICEntryTypes.NUMBER_OF_ENTRY_TYPES; i++){
			arrSourceTypeDescriptions.add("<B>" + getSourceLedgerDescription(SOURCE_LEDGER_IC) + SOURCE_LEDGER_AND_TYPE_DELIMITER + ICEntryTypes.getSourceTypes(i) + "</B>"
				+ " = <I>" + getSourceLedgerDescription(SOURCE_LEDGER_IC) + " " + ICEntryTypes.Get_Entry_Type(i) + "</I>"
			);	
		}

		//JE
		arrSourceTypeDescriptions.add("<B>" + "JE" + SOURCE_LEDGER_AND_TYPE_DELIMITER + "JE" + "</B>"
			+ " = <I>" + "JE Journal Entries" + "</I>"
		);

		
		//PR
		arrSourceTypeDescriptions.add("<B>" + "PR" + SOURCE_LEDGER_AND_TYPE_DELIMITER + "PR" + "</B>"
			+ " = <I>" + "PR Payroll Entries" + "</I>"
		);
		
		return arrSourceTypeDescriptions;
	}
	
}
