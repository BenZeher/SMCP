package SMDataDefinition;

public class SMCreateGoogleDriveFolderParamDefinitions {
	

	//These are used to pass parameters TO a Google Web App script that is able to create new folders in Google Drive.
	public static final String foldername = "foldername";
	public static final String parentfolderid = "parentfolderid";
	public static final String username = "username";
	public static final String recordtype = "recordtype";
	public static final String keyvalue = "keyvalue"; 
	public static final String returnURL = "returnURL";  
	public static final String folderid = "folderid";
	public static final String newfoldername = "newfoldername";
	public static final String backgroundcolor = "bkgroundcolor";
	
	
	//These are used to pass parameters BACK to SMCP from the Google Web App script to determine where to save the folder links.
	public static final String RENAMED_ORDER_FOLDER_URL_PARAM_VALUE = "rename order folder";
	public static final String ORDER_RECORD_TYPE_PARAM_VALUE = "order";
	public static final String DISPLAYED_ORDER_TYPE_PARAM_VALUE = "displayed order";
	public static final String WORK_ORDER_TYPE_PARAM_VALUE = "work order";
	public static final String SALESLEAD_RECORD_TYPE_PARAM_VALUE = "sales lead";
	public static final String PO_RECORD_TYPE_PARAM_VALUE = "PO";
	public static final String AR_CUSTOMER_RECORD_TYPE_PARAM_VALUE = "customer";
	public static final String AR_DISPLAYED_CUSTOMER_TYPE_PARAM_VALUE = "displayed customer";
	public static final String AP_VENDOR_RECORD_TYPE_PARAM_VALUE = "vendor";
	public static final String AP_DISPLAY_VENDOR_TYPE_PARAM_VALUE = "display vendor";
	public static final String SM_LABOR_BACKCHARGE_PARAM_VALUE = "labor backcharge";
}
