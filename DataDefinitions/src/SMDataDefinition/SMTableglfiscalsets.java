package SMDataDefinition;

public class SMTableglfiscalsets {
	public static String TableName = "glfiscalsets";
	public static int CLOSING_PERIOD = 15;
	
	//Field names:
	public static String sAcctID = "sacctid";
	public static String ifiscalyear = "ifiscalyear";
	public static String bdopeningbalance = "bdopeningbalance";
	public static String bdnetchangeperiod1 = "bdnetchangeperiod1";
	public static String bdnetchangeperiod2 = "bdnetchangeperiod2";
	public static String bdnetchangeperiod3 = "bdnetchangeperiod3";
	public static String bdnetchangeperiod4 = "bdnetchangeperiod4";
	public static String bdnetchangeperiod5 = "bdnetchangeperiod5";
	public static String bdnetchangeperiod6 = "bdnetchangeperiod6";
	public static String bdnetchangeperiod7 = "bdnetchangeperiod7";
	public static String bdnetchangeperiod8 = "bdnetchangeperiod8";
	public static String bdnetchangeperiod9 = "bdnetchangeperiod9";
	public static String bdnetchangeperiod10 = "bdnetchangeperiod10";
	public static String bdnetchangeperiod11 = "bdnetchangeperiod11";
	public static String bdnetchangeperiod12 = "bdnetchangeperiod12";
	public static String bdnetchangeperiod13 = "bdnetchangeperiod13";
	public static String bdnetchangeperiod14 = "bdnetchangeperiod14";
	public static String bdnetchangeperiod15 = "bdnetchangeperiod15";
	
	public static int bdopeningbalanceScale = 2;
	public static int bdnetchangeperiodScale = 2;
	
	//Field lengths:
	public static int sAcctIDLength = 45;
	
	public static String getNetChangeFieldNameForSelectedPeriod(int iFiscalPeriod){
		switch(iFiscalPeriod){
		case 1:
			return bdnetchangeperiod1;
		case 2:
			return bdnetchangeperiod2;
		case 3:
			return bdnetchangeperiod3;
		case 4:
			return bdnetchangeperiod4;
		case 5:
			return bdnetchangeperiod5;
		case 6:
			return bdnetchangeperiod6;
		case 7:
			return bdnetchangeperiod7;
		case 8:
			return bdnetchangeperiod8;
		case 9:
			return bdnetchangeperiod9;
		case 10:
			return bdnetchangeperiod10;
		case 11:
			return bdnetchangeperiod11;
		case 12:
			return bdnetchangeperiod12;
		case 13:
			return bdnetchangeperiod13;
		case 14:
			return bdnetchangeperiod14;
		case 15:
			return bdnetchangeperiod15;

		default:
			return "";
		}
	}
}
