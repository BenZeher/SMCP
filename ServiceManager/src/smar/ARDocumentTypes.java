package smar;

public class ARDocumentTypes {

	//Types of documents:
	public static final int INVOICE = 0;
	public static final int CREDIT = 1;
	public static final int RECEIPT = 2;
	public static final int PREPAYMENT = 3;
	public static final int REVERSAL = 4;
	public static final int INVOICEADJUSTMENT = 5;
	public static final int MISCRECEIPT = 6;
	public static final int CASHADJUSTMENT = 7;
	public static final int CREDITADJUSTMENT = 8;
	public static final int RETAINAGE = 9;
	public static final int APPLYTO = 10;
	
	public static final String INVOICE_STRING = "0";
	public static final String CREDIT_STRING = "1";
	public static final String RECEIPT_STRING = "2";
	public static final String PREPAYMENT_STRING = "3";
	public static final String REVERSAL_STRING = "4";
	public static final String INVOICEADJUSTMENT_STRING = "5";
	public static final String MISCRECEIPT_STRING = "6";
	public static final String CASHADJUSTMENT_STRING = "7";
	public static final String CREDITADJUSTMENT_STRING = "8";
	public static final String RETAINAGE_STRING = "9";
	public static final String APPLYTO_STRING = "10";
	
	public static String Get_Document_Type_Label(int iType){
		
		switch (iType){
			case 0: return "Invoice";
			case 1: return "Credit";
			case 2: return "Receipt";
			case 3: return "Prepayment";
			case 4: return "Reversal";
			case 5: return "Invoice Adjustment";
			case 6: return "Misc Receipt";
			case 7: return "Cash Adjustment";
			case 8: return "Credit Adjustment";
			case 9: return "Retainage";
			case 10: return "Apply-To";
			default: return "Invoice";
		}
	}
	public static String getACCPACSourceTypes (int iType){
		switch (iType){
		case 0: return "IN";
		case 1: return "CN";
		case 2: return "PY";
		case 3: return "PI";
		case 4: return "AD";
		case 5: return "AD";
		case 6: return "UC";
		case 7: return "AD";
		case 8: return "AD";
		case 9: return "RT";
		case 10: return "AD";
		default: return "IN";
		}
	}
}
