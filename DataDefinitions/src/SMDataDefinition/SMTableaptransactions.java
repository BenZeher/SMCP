package SMDataDefinition;

public class SMTableaptransactions {
	//Table Name
	public static final String TableName = "aptransactions";
	public static final String OBJECT_NAME = "AP Transaction";
	
	//Field names:
	public static final String lid  = "lid";
	public static final String loriginalbatchnumber = "loriginalbatchnumber";
	public static final String loriginalentrynumber = "loriginalentrynumber";
	public static final String svendor = "svendor";
	public static final String sdocnumber = "sdocnumber";
	//AP transaction doc types are:
	//	0: "Invoice";
	//	1: "Payment";
	//	2: "Adjustment";
	public static final String idoctype = "idoctype";
	public static final String datdocdate = "datdocdate";
	public static final String datduedate = "datduedate";
	public static final String bdoriginalamt = "bdoriginalamt";
	public static final String bdcurrentamt = "bdcurrentamt";
	public static final String sdocdescription = "sdocdescription";
	public static final String scontrolacct = "scontrolacct";
	public static final String datdiscountdate = "datdiscountdate";
	
	//ONLY invoices can have a 'discount available' - original or current.
	//And when the current amt of the invoice transaction drops to zero, regardless of whether there's still
	// a 'current discount available', that available discount gets set to zero.
	public static final String bdoriginaldiscountavailable = "bdoriginaldiscountavailable";
	public static final String bdcurrentdiscountavailable = "bdcurrentdiscountavailable";
	public static final String ionhold = "ionhold";
	
	//These are used to carry the apply to information for pre-pay transactions, so the system knows how to apply them automatically:
	public static final String lapplytopurchaseorderid = "lapplytopurchaseorderid";
	public static final String lapplytosalesorderid = "lapplytosalesorderid";
	public static final String sapplytoinvoicenumber = "sapplytoinvoicenumber";
	
	public static final String schecknumber = "schecknumber";
	
	public static final String lbatchentryid = "lbatchentryid";
	
	public static final String iinvoiceincludestax = "iinvoiceincludestax";
	
	//Tax info:
	public static final String staxjurisdiction = "staxjurisdiction";
	public static final String itaxid = "itaxid";
	public static final String bdtaxrate = "bdtaxrate";
	public static final String staxtype = "staxtype";
	public static final String icalculateonpurchaseorsale = "icalculateonpurchaseorsale";
	
	//Field lengths:
	public static final int svendorlength = 12;
	public static final int sdocnumberlength = 75;
	public static final int sdocdescriptionlength = 128;
	public static final int scontrolacctlength = 75;
	public static final int schecknumberlength = 12;
	
	public static final int bdoriginalamtScale = 2;
	public static final int bdcurrentamtScale = 2;
	public static final int bdoriginaldiscountavailabletScale = 2;
	public static final int bdcurrentdiscountavailableScale = 2;
	
	public static final int staxjurisdictionLength = 12;
	public static final int staxtypeLength = 254;
	public static final int bdtaxrateScale = 4;
	
	public static final int AP_TRANSACTION_TYPE_INVOICE_INVOICE = SMTableapbatchentries.ENTRY_TYPE_INV_INVOICE;
	public static final int AP_TRANSACTION_TYPE_INVOICE_CREDITNOTE = SMTableapbatchentries.ENTRY_TYPE_INV_CREDITNOTE;
	public static final int AP_TRANSACTION_TYPE_INVOICE_DEBITNOTE = SMTableapbatchentries.ENTRY_TYPE_INV_DEBITNOTE;
	//public static final int AP_TRANSACTION_TYPE_INVOICE_INTEREST = SMTableapbatchentries.ENTRY_TYPE_INV_INTEREST;
	
	public static final int AP_TRANSACTION_TYPE_PAYMENT_PAYMENT = SMTableapbatchentries.ENTRY_TYPE_PAYMENT_PAYMENT;
	public static final int AP_TRANSACTION_TYPE_PAYMENT_PREPAYMENT = SMTableapbatchentries.ENTRY_TYPE_PAYMENT_PREPAYMENT;
	public static final int AP_TRANSACTION_TYPE_PAYMENT_APPLYTO = SMTableapbatchentries.ENTRY_TYPE_PAYMENT_APPLYTO;
	public static final int AP_TRANSACTION_TYPE_PAYMENT_MISCPAYMENT = SMTableapbatchentries.ENTRY_TYPE_PAYMENT_MISCPAYMENT;
	
	public static final int AP_TRANSACTION_TYPE_REVERSAL = SMTableapbatchentries.ENTRY_TYPE_REVERSAL;
	
	public static final int NUMBER_OF_AP_TRANSACTION_TYPES = 8;
	
	public static final int NO_ADDITIONAL_TAX_LIABILITY = 1;
	public static final int ADDITIONAL_TAX_LIABILITY = 0;
	
	private static final String NO_ADDITIONAL_TAX_LIABILITY_LABEL = "Invoice does NOT have additional tax liability";
	private static final String ADDITIONAL_TAX_LIABILITY_LABEL = "Invoice DOES have additional tax liability";
	
	public static final String getInvoiceTaxLiabilityLabel(int iLiabilityDecision){
		
		switch(iLiabilityDecision){
		
		case NO_ADDITIONAL_TAX_LIABILITY:
			return NO_ADDITIONAL_TAX_LIABILITY_LABEL;
		case ADDITIONAL_TAX_LIABILITY:
			return ADDITIONAL_TAX_LIABILITY_LABEL;
		default:
			return ADDITIONAL_TAX_LIABILITY_LABEL;
		}
	}
	
	public static final String getInvoiceTaxLiabilityFootnote(){
		String s = "";
		
		s += "<a name=\"TAXLIABILITYNOTE\">"
			+ "<SUP>1</SUP><B><I>Additional tax liability:</I></B>  This is used to determine whether all taxes due are being paid on this invoice, OR if additional tax will be due"
			+ " to be paid later.<BR><BR>"
			+ "Choose <B>'" + NO_ADDITIONAL_TAX_LIABILITY_LABEL + "'</B> if the invoice:<BR>"
			+ "  1) Is not SUBJECT to any tax<BR>"
			+ "  OR 2) has the tax already included in the lines themselves<BR>"
			+ "  OR 3) has one or more lines explicitly detailing a tax amount.<BR>"
			+ "<BR>"
			+ " Choose <B>'" + ADDITIONAL_TAX_LIABILITY_LABEL + "'</B> if the invoice has NO tax included in it already AND <BR>"
			+"  you may be liable to pay the tax on it later."
		;
		return s;
	}
	
	//public static final int AP_TRANSACTION_TYPE_ADJUSTMENT = SMTableapbatchentries.ENTRY_TYPE_ADJUSTMENT;
	
}
