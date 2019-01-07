package SMDataDefinition;

public class SMTablelabelprinters {
	
	//Table Name
	public static final String TableName = "labelprinters";
	
	//Field names:
	public static final String lid = "lid";
	public static final String sName = "sname";
	public static final String sDescription = "sdescription";
	public static final String sHost = "shost";
	public static final String iport = "iport";
	public static final String iTopMargin = "itopmargin";
	public static final String iLeftMargin = "ileftmargin";
	public static final String sFont = "sfont";
	public static final String iBarCodeWidth = "ibarcodewidth";
	public static final String iBarCodeHeight = "ibarcodeheight";
	public static final String iDarkness = "idarkness";
	public static final String iprinterlanguage = "iprinterlanguage";
	
	//Field Lengths:
	public static final int sNameLength = 32;
	public static final int sDescriptionLength = 254;
	public static final int sHostLength = 64;
	public static final int sFontLength = 8;
	
	public static final int PRINTER_LANGUAGE_ZPL = 1;
	public static final int PRINTER_LANGUAGE_EPL = 2;
	
	public static final String getPrinterLanguageDescription(int iLanguage){
		if (iLanguage == PRINTER_LANGUAGE_ZPL){
			return "ZPL";
		}
		if (iLanguage == PRINTER_LANGUAGE_EPL){
			return "EPL";
		}
		return "ZPL";
	}

}
