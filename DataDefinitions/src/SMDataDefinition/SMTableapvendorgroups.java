package SMDataDefinition;

public class SMTableapvendorgroups {
	public static final String TableName = "apvendorgroups";
	
	//Field names:
	public static final String lid = "lid";
	public static final String sgroupid = "sgroupid"; //GROUPID
	public static final String sdescription = "sdescription"; //DESCRIPTN
	public static final String iactive = "iactive"; //ACTIVESW
	public static final String iapaccountset = "iapaccountset"; //ACCTSETID
	public static final String ibankcode = "ibankcode"; //BANKID
	public static final String stermscode = "stermscode"; //TERMCODE
	public static final String iprintseparatechecks = "iprintseparatechecks"; //PRTSEPCHKS
	public static final String idistributioncodeusedfordistribution = "idistributioncodeusedfordistribution"; //DISTCODE
	public static final String sglacctusedfordistribution = "sglacctusedfordistribution"; //GLACCTID
	public static final String idistributeby = "idistributeby"; //SWDISTBY
	public static final String staxjurisdiction = "staxjurisdiction"; //TAXGRP
	public static final String itaxtype = "itaxtype"; //TAXCLASS 1 thru 5
	public static final String itaxreportingtype = "itaxreportingtype"; //TAXRPTSW
	public static final String i1099CPRScode = "i1099CPRScode"; //CLASSID
	
	//Field Lengths:
	public static final int sgroupidlength = 12;
	public static final int sdescriptionlength = 60;
	public static final int stermscodelength = 6;
	public static final int sglacctusedfordistributionlength = 45;
	public static final int staxjurisdictionlength = 12;
	
	public static final int NUMBER_OF_TAX_REPORTING_TYPES = 3;
	public static final int TAX_REPORTING_TYPE_NONE = 0;
	public static final int TAX_REPORTING_TYPE_1099 = 1;
	public static final int TAX_REPORTING_TYPE_CPRS = 2;
	
	public static String getTaxReportingType(int iTaxReportingType){
		
		switch (iTaxReportingType){
			case TAX_REPORTING_TYPE_NONE:
				return "None";
			case TAX_REPORTING_TYPE_1099:
				return "1099";
			case TAX_REPORTING_TYPE_CPRS:
				return "CPRS";
			default:
				return "None";
		}
	}
	
	//In ACCPAC these are the values:
	//Distribute by Distribution Set = 0 - we won't be using this one, so it should go to number 1:
	public static final int NUMBER_OF_DISTRIBUTE_BY_TYPES = 3;
	public static final int DISTRIBUTE_BY_TYPE_DISTRIBUTION_CODE = 1;
	public static final int DISTRIBUTE_BY_TYPE_GL_ACCT = 2;
	public static final int DISTRIBUTE_BY_TYPE_NONE = 3;
	
	public static String getDistributeByType(int iDistributeByType){
		
		switch (iDistributeByType){
			case DISTRIBUTE_BY_TYPE_NONE:
				return "None";
			case DISTRIBUTE_BY_TYPE_DISTRIBUTION_CODE:
				return "Distribution code";
			case DISTRIBUTE_BY_TYPE_GL_ACCT:
				return "GL Account";
			default:
				return "None";
		}
	}
	
}
