package SMDataDefinition;

public class SMTablecriticaldates {

	public static String TableName = "criticaldates";
	
	public static String sCriticalDate = "datcriticaldate";
	public static String sdocnumber = "sdocnumber";
	public static String sResolvedFlag = "iresolved";
	public static String sComments = "Comments";
	public static String sTimeStampAudit = "TimeStampAudit";
	public static String sId = "id";
	//public static String sResponsible = "Responsible";
	public static String lresponsibleuserid = "lresponsibleuserid";
	public static String sresponsibleuserfullname = "sresponsibleuserfullname";
	//public static String sRecType = "sRecType";
	public static String itype = "itype";
	//public static String sassignedby = "sassignedby";
	public static String lassignedbyuserid = "lassignedbyuserid";
	public static String sassignedbyuserfullname = "sassignedbyuserfullname";
	
	//public static String lcreatedbyuserid = "lcreatedbyuserid";
	//public static String screatedbyuserfullname = "screatedbyuserfullname";
	
	public static int sDocNumberLength = 11;
	public static int lResponsibleuseridLength = 11;
	public static int sResponsibleuserfullnameLength = 128;
	public static int sTypeLength = 2;
	public static int lAssignedbyuseridLength = 11;
	public static int sAssignedbyuserfullnameLength = 128;
	
	
	//Record Type Definitions
	public static final int SALES_ORDER_RECORD_TYPE = 1;
	public static final int SALES_LEAD_RECORD_TYPE = 2;
	public static final int SALES_CONTACT_RECORD_TYPE = 3;
	public static final int PURCHASE_ORDER_RECORD_TYPE = 4;
	public static final int AR_CALL_SHEET_RECORD_TYPE = 5;
	
	public static String getTypeDescriptions(int iType) {
		String s;
		switch(iType) {
		case SALES_ORDER_RECORD_TYPE:
			s = "Order";
			break;
		case SALES_LEAD_RECORD_TYPE:
			s = "Sales Lead";	
			break;
		case SALES_CONTACT_RECORD_TYPE:
			s = "Sales Contact";
			break;
		case PURCHASE_ORDER_RECORD_TYPE:
			s = "Purchase Order";
			break;
		case AR_CALL_SHEET_RECORD_TYPE:
			s = "Call Sheet";	
			break;
		default:
			s ="N/A";
		}
		return s;
	}
	
}
