package SMClasses;

import SMDataDefinition.SMMasterStyleSheetDefinitions;

public class SMBatchStatuses {
	
	public static final int ENTERED = 0;
	public static final int IMPORTED = 1;
	public static final int DELETED = 2;
	public static final int POSTED = 3;
	public static final String STATUS_COLOR_ENTERED = "#8BFF8B";
	public static final String STATUS_COLOR_IMPORTED = "#8BAAFF";
	public static final String STATUS_COLOR_DELETED = "#FCA8A8";
	public static final String STATUS_COLOR_POSTED =  SMMasterStyleSheetDefinitions.BACKGROUND_WHITE;
	
	public static String Get_Transaction_Status(int iStatus){
		
		switch (iStatus) {
		case 0:
			return "ENTERED";
		case 1:
			return "IMPORTED";
		case 2:
			return "DELETED";
		case 3:
			return "POSTED";
		default:  // optional default case
			return "ENTERED";
		}	
	}

	public static String Get_Transaction_Status_Color(int iStatus){
		
		switch (iStatus) {
		case 0:
			return STATUS_COLOR_ENTERED;
		case 1:
			return STATUS_COLOR_IMPORTED;
		case 2:
			return STATUS_COLOR_DELETED;
		case 3:
			return STATUS_COLOR_POSTED;
		default:  // optional default case
			return STATUS_COLOR_ENTERED;
		}	
	}
	
	
}
