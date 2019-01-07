
package SMDataDefinition;

public class SMTableictransactions {
	//Table Name
	public static final String TableName = "ictransactions";
	
	//Field names:
	public static final String lid  = "lid";
	public static final String loriginalbatchnumber = "loriginalbatchnumber";
	public static final String loriginalentrynumber = "loriginalentrynumber";
	public static final String loriginallinenumber = "loriginallinenumber";
	public static final String sitemnumber = "sitemnumber";
	public static final String slocation = "slocation";
	public static final String ientrytype = "ientrytype";
	public static final String sdocnumber = "sdocnumber";
	public static final String sentrydescription = "sentrydescription";
	public static final String slinedescription = "slinedescription";
	public static final String datpostingdate = "datpostingdate";
	public static final String bdqty = "bdqty";
	public static final String bdcost = "bdcost";
	public static final String bdprice = "bdprice";
	public static final String scontrolacct = "scontrolacct";
	public static final String sdistributionacct = "sdistributionacct";
	//public static final String spostedby  = "spostedby";
	public static final String lpostedbyid  = "lpostedbyid";
	public static final String spostedbyfullname = "spostedbyfullname";
	public static final String llineid  = "llineid";
	public static final String sunitofmeasure  = "sunitofmeasure";
	
	//Field lengths:
	public static final int sitemnumberlength = 24;
	public static final int slocationlength = 6;
	public static final int sdocnumberlength = 75;
	public static final int sentrydescriptionlength = 128;
	public static final int slinedescriptionlength = 128;
	public static final int scontrolacctlength = 75;
	public static final int sdistributionacctlength = 75;
	public static final int spostedbylength = 128;
	public static final int sunitofmeasurelength = 10;
	public static final int spostedbyfullnameLength = 128;
}
