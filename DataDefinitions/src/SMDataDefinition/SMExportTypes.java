package SMDataDefinition;

public class SMExportTypes {
	public static final int NUMBER_OF_EXPORT_FORMATS = 3;
	public static final int EXPORT_TO_ACCPAC54 = 0;
	public static final int EXPORT_TO_ACCPAC56 = 1;
	public static final int EXPORT_TO_MAS200 = 2;
	
	public static final String EXPORT_TO_ACCPAC54_LABEL = "ACCPAC Version 5.4";
	public static final String EXPORT_TO_ACCPAC56_LABEL = "ACCPAC Version 5.6";
	public static final String EXPORT_TO_MAS200_LABEL = "MAS 200";
	
	public static String getExportFormatLabel(int iExportIndex){
		
		if (iExportIndex == EXPORT_TO_ACCPAC54){
			return EXPORT_TO_ACCPAC54_LABEL;
		}
		if (iExportIndex == EXPORT_TO_ACCPAC56){
			return EXPORT_TO_ACCPAC56_LABEL;
		}
		if (iExportIndex == EXPORT_TO_MAS200){
			return EXPORT_TO_MAS200_LABEL;
		}
		return EXPORT_TO_ACCPAC54_LABEL;
	}
}
