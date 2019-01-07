package SMDataDefinition;

public class SMTableicporeceiptheaders {
	public static final String TableName = "icporeceiptheaders";
	
	public static final String lid = "lid";
	public static final String lpoheaderid = "lpoheaderid";
	public static final String datreceived = "datreceived";
	public static final String sreceiptnumber = "sreceiptnumber";
	public static final String lpostedtoic = "lpostedtoic";
	public static final String sdeletedbyfullname = "sdeletedbyfullname";
	public static final String datdeleted = "datdeleted";
	public static final String lstatus = "lstatus";
	public static final String lcreatedbyid = "lcreatedbyid";
	public static final String screatedbyfullname = "screatedbyfullname";
	public static final String llastupdateuserid= "llastupdateuserid";
	public static final String slastupdateuserfullname = "slastupdateuserfullname";
	public static final String slastupdateprocess = "slastupdateprocess";
	public static final String dattimelastupdated = "dattimelastupdated";
	
	//Field lengths:
	public static final int sreceiptnumberLength = 22;
	public static final int sdeletedbyLength = 128;
	public static final int screatedbyLength = 128;
	public static final int slastupdateuserLength = 128;
	public static final int slastupdateprocessLength = 64;
	public static final int slastupdateuserfullnameLength = 128;
	
	public static final int STATUS_ENTERED = 0;
	public static final int STATUS_DELETED = 1;
	
	public static final String UPDATE_PROCESS_FLAGASNONINVENTORY = "RECEIPT FLAGGED AS NON-INVENTORY";
	public static final String UPDATE_PROCESS_POSTINGRECEIPT = "RECEIPT POSTED";
	public static final String UPDATE_PROCESS_INSERTINGRECORD = "RECEIPT FIRST CREATED";
	public static final String UPDATE_PROCESS_UPDATINGRECORD = "RECEIPT UPDATED";
	public static final String UPDATE_PROCESS_MARKEDASDELETED = "RECEIPT MARKED AS DELETED";
	public static final String UPDATE_PROCESS_CHILDLESSRECEIPTMARKEDASDELETED = "NO RECEIPT LINES - RECEIPT AUTOMATICALLY MARKED AS DELETED";
	
	public static String getStatusDescription(int iStatus){
		switch (iStatus){
			case STATUS_ENTERED:
				return "Entered";
			case STATUS_DELETED:
				return "Deleted";
			default:
				return "N/A";
		}
	}
}
