package SMDataDefinition;

public class SMTableglbatches {
	//Table Name
	public static final String TableName = "glbatches";

	//Field names:
	public static final String lbatchnumber = "lbatchnumber";
	public static final String datbatchdate = "datbatchdate";
	public static final String ibatchstatus = "ibatchstatus";
	public static final String sbatchdescription = "sbatchdescription";
	public static final String ibatchtype = "ibatchtype";
	public static final String datlasteditdate = "datlasteditdate";
	public static final String lbatchlastentry = "lbatchlastentry";
	public static final String screatedby = "screatedby";
	public static final String slasteditedby = "slasteditedby"; 
	public static final String datpostdate = "datpostdate";

	//Field Lengths:
	public static final int sBatchDescriptionLength = 128;
	public static final int sCreatedByLength = 128;
	public static final int sLastEditUserLength = 128; 
	
	public static final int GL_BATCH_TYPE_AP_INVOICE = 0;
	public static final int GL_BATCH_TYPE_AP_PAYMENT = 1;
	public static final int GL_BATCH_TYPE_AP_REVERSAL = 2;
	public static final int GL_BATCH_TYPE_AR_INVOICE = 3;
	public static final int GL_BATCH_TYPE_AR_CASH = 4;
	public static final int GL_BATCH_TYPE_IC_SHIPMENT = 5;
	public static final int GL_BATCH_TYPE_IC_RECEIPT = 6;
	public static final int GL_BATCH_TYPE_IC_ADJUSTMENT = 7;
	public static final int GL_BATCH_TYPE_IC_TRANSFER = 8;
	public static final int GL_BATCH_TYPE_IC_PHYSICALCOUNT = 9;
	public static final int GL_BATCH_TYPE_FA_DEPRECIATION = 10;
	
	public static final int GL_NUMBER_OF_BATCH_TYPES = 11;
	
	public static String getBatchTypeLabel(int iAPBatchType){
		switch(iAPBatchType){
		case GL_BATCH_TYPE_AP_INVOICE:
			return "AP Invoice";
		case GL_BATCH_TYPE_AP_PAYMENT:
			return "AP Payment";
		case GL_BATCH_TYPE_AP_REVERSAL:
			return "AP Cheeck Reversal";
		case GL_BATCH_TYPE_AR_INVOICE:
			return "AR Invoice";
		case GL_BATCH_TYPE_AR_CASH:
			return "AR Cash";
		case GL_BATCH_TYPE_IC_SHIPMENT:
			return "IC Shipment";
		case GL_BATCH_TYPE_IC_RECEIPT:
			return "IC Receipt";
		case GL_BATCH_TYPE_IC_ADJUSTMENT:
			return "IC Adjustment";
		case GL_BATCH_TYPE_IC_TRANSFER:
			return "IC Transfer";
		case GL_BATCH_TYPE_IC_PHYSICALCOUNT:
			return "IC Physical Count";
		case GL_BATCH_TYPE_FA_DEPRECIATION:
			return "FA Depreciation";

		default:
			return "N/A";
		}
	}
}

