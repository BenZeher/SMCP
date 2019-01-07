package smar;

public class ARBatchTypes {

	//AR:
	public static int AR_INVOICE = 0;
	public static int AR_CASH = 1;
	public static int AR_ADJUSTMENT = 2;
	
	public static String INVOICE_LABEL = "Invoice";
	public static String CASH_LABEL = "Cash";
	public static String ADJUSTMENT_LABEL = "Adjustment";
	
	public static String Get_AR_Batch_Type(int iBatchType){
		
		switch (iBatchType) {
		case 0:
			return "Invoice";
		case 1:
			return "Cash";
		case 2:
			return "Adjustment";
		default:  // optional default case
			return "Invoice";
		}
	}
	
	public static String Get_AR_Entry_Edit_Class(int iBatchType){
		
		switch (iBatchType) {
		case 0:
			return ARClassNames.INVOICE_ENTRY_EDIT;
		case 1:
			return ARClassNames.CASH_ENTRY_EDIT;
		default:  // optional default case
			return ARClassNames.INVOICE_ENTRY_EDIT;
		}
	}
	
}
