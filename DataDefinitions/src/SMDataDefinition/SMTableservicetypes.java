package SMDataDefinition;

public class SMTableservicetypes {

	//Table Name
	public static String TableName = "servicetypes";
	
	//Field names:
    public static String sCode = "sCode";
    public static String sName = "sName";
    public static String iTypeID = "iTypeID";
    public static String mworkorderterms = "mworkorderterms";
    public static String mworeceiptcomment = "mworeceiptcomment";
    public static String id = "id";
    
    //Field lengths:
    public static int sCodeLength = 6;
    public static int sNameLength = 30;
    
    public static String getServiceTypeLabel(String sCode){
    	
    	if (sCode.compareToIgnoreCase("SH0001") == 0){
    		return "Residential Service";
    	}
    	if (sCode.compareToIgnoreCase("SH0002") == 0){
    		return "Residential Installation";
    	}
    	if (sCode.compareToIgnoreCase("SH0003") == 0){
    		return "Commercial Service";
    	}
    	if (sCode.compareToIgnoreCase("SH0004") == 0){
    		return "Commercial Installation";
    	}
    	//Use the newer versions of the code:
    	if (sCode.compareToIgnoreCase("RS") == 0){
    		return "Residential Service";
    	}
    	if (sCode.compareToIgnoreCase("RI") == 0){
    		return "Residential Installation";
    	}
    	if (sCode.compareToIgnoreCase("CS") == 0){
    		return "Commercial Service";
    	}
    	if (sCode.compareToIgnoreCase("CI") == 0){
    		return "Commercial Installation";
    	}
    	return "N/A";
    }
    public static String getServiceTypeLabelFromTypeID(String sID){
    	
    	if (sID.compareToIgnoreCase("0") == 0){
    		return "Residential Service";
    	}
    	if (sID.compareToIgnoreCase("1") == 0){
    		return "Residential Installation";
    	}
    	if (sID.compareToIgnoreCase("2") == 0){
    		return "Commercial Service";
    	}
    	if (sID.compareToIgnoreCase("3") == 0){
    		return "Commercial Installation";
    	}
    	return "N/A";
    }

    public static String getServiceTypeInitials(String sCode){
    	
    	if (sCode.compareToIgnoreCase("SH0001") == 0){
    		return "RS";
    	}
    	if (sCode.compareToIgnoreCase("SH0002") == 0){
    		return "RI";
    	}
    	if (sCode.compareToIgnoreCase("SH0003") == 0){
    		return "CS";
    	}
    	if (sCode.compareToIgnoreCase("SH0004") == 0){
    		return "CI";
    	}
    	return "N/A";
    }
    
}
