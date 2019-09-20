package smic;

public class ICEntryTypes {

		public static final int SHIPMENT_ENTRY = 0;
		public static final int RECEIPT_ENTRY = 1;
		public static final int ADJUSTMENT_ENTRY = 2;
		public static final int TRANSFER_ENTRY = 3;
		public static final int PHYSICALCOUNT_ENTRY = 4;
		public static final int NUMBER_OF_ENTRY_TYPES = 5;
		
		public static String SHIPMENT_LABEL = "Shipment";
		public static String RECEIPT_LABEL = "Receipt";
		public static String ADJUSTMENT_LABEL = "Adjustment";
		public static String TRANSFER_LABEL = "Transfer";
		public static String PHYSICALCOUNT_LABEL = "Location count";
		
		public static String Get_Entry_Type(int iEntryType){
			
			switch (iEntryType) {
			case 0:
				return "Shipment";
			case 1:
				return "Receipt";
			case 2:
				return "Adjustment";
			case 3:
				return "Transfer";
			case 4:
				return "Physical count";
			default:  // optional default case
				return "Shipment";
			}
		}
		
		public static String getSourceTypes (int iType){
			switch (iType){
				case 0: return "SH";
				case 1: return "RC";
				case 2: return "AD";
				case 3: return "TF";
				case 4: return "AD";
				default: return "AD";
			}
		}
		
		public static int getBatchType (int iEntryType){
			switch (iEntryType){
				case SHIPMENT_ENTRY: return ICBatchTypes.IC_SHIPMENT;
				case RECEIPT_ENTRY: return ICBatchTypes.IC_RECEIPT;
				case ADJUSTMENT_ENTRY: return ICBatchTypes.IC_ADJUSTMENT;
				case TRANSFER_ENTRY: return ICBatchTypes.IC_TRANSFER;
				case PHYSICALCOUNT_ENTRY: return ICBatchTypes.IC_PHYSICALCOUNT;
				default: return 0;
			}
		}
}
