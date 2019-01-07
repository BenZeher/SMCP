package smic;

public class ICEntryTypes {

		public static int SHIPMENT_ENTRY = 0;
		public static int RECEIPT_ENTRY = 1;
		public static int ADJUSTMENT_ENTRY = 2;
		public static int TRANSFER_ENTRY = 3;
		public static int PHYSICALCOUNT_ENTRY = 4;
		
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
		
		public static String getACCPACSourceTypes (int iType){
			switch (iType){
			case 0: return "SH";
			case 1: return "RC";
			case 2: return "AD";
			case 3: return "TF";
			case 4: return "AD";
			default: return "AD";
			}
		}
}
