package SMDataDefinition;

public class SMModuleListing {

	// These values are 2^n:
	public static final long MODULE_BASE = 1; // n = 0
	public static final long MODULE_ACCOUNTSRECEIVABLE = 2; // n = 1
	public static final long MODULE_ORDERENTRY = 4; // n = 2
	public static final long MODULE_SALESMANAGEMENT = 8; // n = 3
	public static final long MODULE_INVENTORYCONTROL = 16; // n = 4
	public static final long MODULE_ACCOUNTSPAYABLE = 32; // n = 5
	public static final long MODULE_FIXEDASSETS = 64; // n = 6
	public static final long MODULE_BANKFUNCTIONS = 128; // n = 7
	public static final long MODULE_GENERALLEDGER = 256; // n = 8
	public static final long MODULE_ALARMSYSTEM = 512; // n = 9
	public static final long MODULE_PAYROLL = 1024; // n = 10

	//A company's license file includes a license module level, which is just the sum of all the modules they've purchased.
	//So, for example, if a company ONLY purchased the BASE (1), ACCOUNTS RECEIVABLE (2), and ORDER ENTRY (4), their 
	// license module level would be 7 (1 + 2 + 4)
	//If they later purchased the alarm system, which is 512, then their license module level would be:
	// 519 (1 + 2 + 4 + 512)

	//All packages together total 2047

	//Security functions each carry a 'module level sum' - it is the sum of all the module levels which require that function.
	//So if the 'SM Edit Orders' function only needs to be in the 'Order Entry' module, which is module number 4,
	// then the 'SM Edit Orders' function would carry module level sum 4.  But the 'IC Edit Items function might need to be
	// in Order Entry (4) AND Inventory (16), so the function would carry module level sum 20.
	
	public static String getModuleName (long lModuleLevel){
		if (lModuleLevel == MODULE_BASE){
			return "Base Module";
		}
		if (lModuleLevel == MODULE_ACCOUNTSRECEIVABLE){
			return "Accounts Receivable";
		}
		if (lModuleLevel == MODULE_ORDERENTRY){
			return "Order Entry";
		}
		if (lModuleLevel == MODULE_SALESMANAGEMENT){
			return "Sales Management";
		}
		if (lModuleLevel == MODULE_INVENTORYCONTROL){
			return "Inventory";
		}
		if (lModuleLevel == MODULE_ACCOUNTSPAYABLE){
			return "Accounts Payable";
		}
		if (lModuleLevel == MODULE_FIXEDASSETS){
			return "Fixed Assets";
		}
		if (lModuleLevel == MODULE_BANKFUNCTIONS){
			return "Bank Functions";
		}
		if (lModuleLevel == MODULE_GENERALLEDGER){
			return "General Ledger";
		}
		if (lModuleLevel == MODULE_ALARMSYSTEM){
			return "Alarm System";
		}
		if (lModuleLevel == MODULE_PAYROLL){
			return "Payroll";
		}
		return "N/A";
	}
	
	public static String getModuleList(long lLicenseLevel){
		String s = "";
		
		if ((lLicenseLevel & MODULE_BASE) > 0){
			if (s.compareToIgnoreCase("") != 0){
				s += ", ";
			}
			s += "Base Module";
		}
		if ((lLicenseLevel & MODULE_ACCOUNTSRECEIVABLE) > 0){
			if (s.compareToIgnoreCase("") != 0){
				s += ", ";
			}
			s += "Accounts Receivable";
		}
		if ((lLicenseLevel & MODULE_ORDERENTRY) > 0){
			if (s.compareToIgnoreCase("") != 0){
				s += ", ";
			}
			s += "Order Entry";
		}
		if ((lLicenseLevel & MODULE_SALESMANAGEMENT) > 0){
			if (s.compareToIgnoreCase("") != 0){
				s += ", ";
			}
			s += "Sales Management";
		}
		if ((lLicenseLevel & MODULE_INVENTORYCONTROL) > 0){
			if (s.compareToIgnoreCase("") != 0){
				s += ", ";
			}
			s += "Inventory";
		}
		if ((lLicenseLevel & MODULE_ACCOUNTSPAYABLE) > 0){
			if (s.compareToIgnoreCase("") != 0){
				s += ", ";
			}
			s += "Accounts Payable";
		}
		if ((lLicenseLevel & MODULE_FIXEDASSETS) > 0){
			if (s.compareToIgnoreCase("") != 0){
				s += ", ";
			}
			s += "Fixed Assets";
		}
		if ((lLicenseLevel & MODULE_BANKFUNCTIONS) > 0){
			if (s.compareToIgnoreCase("") != 0){
				s += ", ";
			}
			s += "Bank Functions";
		}
		if ((lLicenseLevel & MODULE_GENERALLEDGER) > 0){
			if (s.compareToIgnoreCase("") != 0){
				s += ", ";
			}
			s += "General Ledger";
		}
		if ((lLicenseLevel & MODULE_ALARMSYSTEM) > 0){
			if (s.compareToIgnoreCase("") != 0){
				s += ", ";
			}
			s += "Alarm System";
		}
		if ((lLicenseLevel & MODULE_PAYROLL) > 0){
			if (s.compareToIgnoreCase("") != 0){
				s += ", ";
			}
			s += "Payroll Functions";
		}

		return s;
	}
}

