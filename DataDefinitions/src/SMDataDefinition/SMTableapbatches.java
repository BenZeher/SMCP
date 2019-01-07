package SMDataDefinition;

public class SMTableapbatches {
	//Table Name
	public static final String TableName = "apbatches";

	//Field names:
	public static final String lbatchnumber = "lbatchnumber";
	public static final String datbatchdate = "datbatchdate";
	public static final String ibatchstatus = "ibatchstatus";
	public static final String sbatchdescription = "sbatchdescription";
	public static final String ibatchtype = "ibatchtype";
	public static final String datlasteditdate = "datlasteditdate";
	public static final String lbatchlastentry = "lbatchlastentry";
	public static final String lcreatedby = "lcreatedby";
	public static final String screatedbyfullname = "screatedbyfullname";
	public static final String llasteditedby = "llasteditedby";
	public static final String slasteditedbyfullname = "slasteditedbyfullname"; 
	public static final String datpostdate = "datpostdate";

	//Field Lengths:
	public static final int sBatchDescriptionLength = 128;
	public static final int screatedbyfullnameLength = 124;
	public static final int slasteditedbyfullnameLength = 124;
	
	public static final int AP_BATCH_TYPE_INVOICE = 0;
	public static final int AP_BATCH_TYPE_PAYMENT = 1;
	public static final int AP_BATCH_TYPE_ADJUSTMENT = 2;
	public static final int AP_BATCH_TYPE_REVERSALS = 3;
	public static final int AP_BATCH_TYPE_NUMBER_OF_TYPES = 4;
	
	public static String getBatchTypeLabel(int iAPBatchType){
		switch(iAPBatchType){
		case AP_BATCH_TYPE_INVOICE:
			return "Invoice";
		case AP_BATCH_TYPE_PAYMENT:
			return "Payment";
		case AP_BATCH_TYPE_ADJUSTMENT:
			return "Adjustment";
		case AP_BATCH_TYPE_REVERSALS:
			return "Check reversal";
		default:
			return "N/A";
		}
	}
	public static String getBatchSourceTypeLabels (int iBatchSourceType){
		switch (iBatchSourceType){
		case AP_BATCH_TYPE_INVOICE: return "IN";
		case AP_BATCH_TYPE_PAYMENT: return "PY";
		case AP_BATCH_TYPE_ADJUSTMENT: return "AD";
		case AP_BATCH_TYPE_REVERSALS: return "RC";
		default: return "IN";
		}
	}
}

