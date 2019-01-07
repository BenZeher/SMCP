package SMDataDefinition;

public class SMTableicvendors {
	
	public static final String TableName = "icvendors";
	//Field names:
	public static final String svendoracct = "svendoracct";
	public static final String sname = "sname";
	public static final String saddressline1 = "saddressline1";
	public static final String saddressline2 = "saddressline2";
	public static final String saddressline3 = "saddressline3";
	public static final String saddressline4 = "saddressline4";
	public static final String scity = "scity";
	public static final String sstate = "sstate";
	public static final String spostalcode = "spostalcode";
	public static final String scountry = "scountry";
	public static final String scontactname = "scontactname";
	public static final String sphonenumber = "sphonenumber";
	public static final String sfaxnumber = "sfaxnumber";
	public static final String sterms = "sterms";
	public static final String scompanyacctcode = "scompanyaccountcode";
	public static final String swebaddress = "swebaddress";
	public static final String datlastmaintained = "datlastmaintained";
	public static final String slasteditedbyfullname = "slasteditedbyfullname";
	public static final String llasteditedbyuserid = "llasteditedbyuserid";
	public static final String iactive = "iactive";
	public static final String ipoconfirmationrequired = "ipoconfirmationrequired";
	public static final String iapaccountset = "iapaccountset";
	public static final String ibankcode = "ibankcode";
	public static final String sdefaultexpenseacct = "sdefaultexpenseacct";
	public static final String sdefaultdistributioncode = "sdefaultdistributioncode";
	public static final String sdefaultinvoicelinedesc = "sdefaultinvoicelinedesc";
	public static final String sgdoclink = "sgdoclink";
	public static final String sprimaryremittocode = "sprimaryremittocode";
	public static final String staxidentifyingnumber = "staxidentifyingnumber";
	public static final String itaxreportingtype = "itaxreportingtype";
	public static final String i1099CPRSid = "i1099CPRSid";
	public static final String itaxidnumbertype = "itaxidnumbertype";
	public static final String igenerateseparatepaymentsforeachinvoice = "igenerateseparatepaymentsforeachinvoice";
	public static final String ivendorgroupid = "ivendorgroupid";
	
	
	//Used ONLY to convert the ACCPAC AP data - when that's all done, this field can be removed:
	public static final String iaddedbyapconversion = "iaddedbyapconversion";
	
	//Field lengths:
	public static final int svendoracctLength = 12;
	public static final int snameLength = 60;
	public static final int saddressline1Length = 60;
	public static final int saddressline2Length = 60;
	public static final int saddressline3Length = 60;
	public static final int saddressline4Length = 60;
	public static final int scityLength = 30;
	public static final int sstateLength = 30;
	public static final int spostalcodeLength = 20;
	public static final int scountryLength = 30;
	public static final int scontactnameLength = 60;
	public static final int sphonenumberLength = 30;
	public static final int sfaxnumberLength = 30;
	public static final int stermsLength = 6;
	public static final int scompanyaccountcodeLength = 64;
	public static final int swebaddressLength = 128;
	public static final int slasteditedbyLength = 128;
	public static final int sdefaultexpenseacctlength = 45;
	public static final int sgdoclinklength = 254;
	public static final int sprimaryremittocodelength = 12;
	public static final int staxidentifyingnumberlength = 32;
	public static final int sdefaultdistributioncodelength = 32;
	public static final int sdefaultinvoicelinedesclength = 96;
	
	//Tax reporting types:
	public static final int NUMBER_OF_TAX_REPORTING_TYPES = 3;
	//These match the ACCPAC values for field 'TAXRPTSW':
	public static final int TAX_REPORTING_TYPE_NONE = 0;
	public static final int TAX_REPORTING_TYPE_1099 = 1;
	public static final int TAX_REPORTING_TYPE_CPRS = 2;
	
	//Tax ID types:
	public static final int NUMBER_OF_TAX_ID_TYPES = 3;
	//These match the corresponding ACPAC Tax ID Types in field 'TAXIDTYPE'"
	public static final int TAX_ID_TYPE_UNKNOWN = 0;
	public static final int TAX_ID_TYPE_SOCIALSECURITYNUMBER = 1;
	public static final int TAX_ID_TYPE_EMPLOYER_ID_NUMBER = 2;
	
	public static String getTaxReportingTypeDescriptions(int iTaxReportingType){
		switch(iTaxReportingType){
		case TAX_REPORTING_TYPE_NONE:
			return "None";
		case TAX_REPORTING_TYPE_1099:
			return "1099";
		case TAX_REPORTING_TYPE_CPRS:
			return "CPRS";
		default:
			return "N/A";
		}
	}
	
	public static String getTaxIDTypeDescriptions(int iTaxIDType){
		switch(iTaxIDType){
		case TAX_ID_TYPE_UNKNOWN:
			return "Unknown";
		case TAX_ID_TYPE_SOCIALSECURITYNUMBER:
			return "Social Security Number";
		case TAX_ID_TYPE_EMPLOYER_ID_NUMBER:
			return "Employer ID Number";
		default:
			return "N/A";
		}
	}
}
