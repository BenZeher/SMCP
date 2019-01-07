package smic;

public class ICBatchTypes {

		public static int IC_SHIPMENT = 0;
		public static int IC_RECEIPT = 1;
		public static int IC_ADJUSTMENT = 2;
		public static int IC_TRANSFER = 3;
		public static int IC_PHYSICALCOUNT = 4;
		
		public static String SHIPMENT_LABEL = "Shipment";
		public static String RECEIPT_LABEL = "Receipt";
		public static String ADJUSTMENT_LABEL = "Adjustment";
		public static String TRANSFER_LABEL = "Transfer";
		public static String PHYSICALCOUNT_LABEL = "Physical count";
		
		public static String Get_Batch_Type(int iBatchType){
			
			switch (iBatchType) {
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
				return "";
			}
		}
		
}
