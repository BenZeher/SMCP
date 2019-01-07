package SMClasses;

public class SMBatchTypes {

	//AR:
	public static int AR_INVOICE = 0;
	public static int AR_CASH = 1;
	public static int AR_ADJUSTMENT = 2;
	
	public static String INVOICE_LABEL = "Invoice";
	public static String CASH_LABEL = "Cash";
	public static String ADJUSTMENT_LABEL = "Adjustment";
	
	public static String Get_Batch_Type(int iBatchType){
		
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
	
}
