package SMDataDefinition;

public class SMTablelabortypes {

	public static String  TableName = "labortypes";
	
	//Field definitions:
	public static String  sID = "ID"; //` int(6) NOT NULL default '0',
	public static String sLaborName = "sLaborName";//` varchar(255) default '',
	public static String dMarkupAmount = "dMarkupAmount";//` double default '0',
	public static String sItemNumber = "sItemNumber";//` varchar(50) default '',
	public static String sCategory = "sCategory";//` varchar(6) default '',
	
	//Field lengths:
	public static int sLaborNameLength = 255;
	public static int sItemNumberLength = 50;
	public static int sCategoryLength = 6;
}
