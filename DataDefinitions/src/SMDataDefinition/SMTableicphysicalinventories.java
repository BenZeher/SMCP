package SMDataDefinition;

public class SMTableicphysicalinventories {
	public static final String TableName = "icphysicalinventories";
	
	//Field names:
	public static final String lid = "lid";
	public static final String sdesc = "sdesc";
	public static final String screatedbyfullname = "screatedbyfullname";
	public static final String lcreatedbyuserid = "lcreatedbyuserid";
	public static final String datcreated = "datcreated";
	public static final String istatus = "istatus";
	public static final String lbatchnumber = "lbatchnumber";
	public static final String slocation = "slocation";
	//public static final String sstartingitemnumber = "sstartingitemnumber";
	//public static final String sendingitemnumber = "sendingitemnumber";
			  
	//Field lengths:
	public static final int sdescLength = 128;
	public static final int screatedbyLength = 128;
	public static final int sstartinglocationLength = 6;
	public static final int screatedbyfullnameLength = 128;
	//public static final int sstartingitemnumberLength = 24;
	//public static final int sendingitemnumberLength = 24;
	
	public static final int STATUS_ENTERED = 0;
	public static final int STATUS_BATCHED = 1;
	public static final int STATUS_DELETED = 2;
	
	public static String getStatusDescription (int iStatus){
		switch (iStatus){
		case STATUS_ENTERED:
			return "Entered";
		case STATUS_BATCHED:
			return "Converted to batch";
		case STATUS_DELETED:
			return "Deleted";
		default:
			return "N/A";
		}
	}
}
