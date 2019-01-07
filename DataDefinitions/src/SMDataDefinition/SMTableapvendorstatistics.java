package SMDataDefinition;

public class SMTableapvendorstatistics {
	//Table Name
	public static final String TableName = "apvendorstatistics";
	
	//Field names:
	//In ACCPAC, these come from the 'APVSM' table:
	//The old ACCPAC name is shown in a comment to the right of each
	public static final String svendoracct = "svendoracct"; //VENDORID
	public static final String lyear = "lyear"; //CNTYR
	public static final String lmonth = "lmonth"; //CNPERD
	public static final String lnumberofinvoices = "lnumberofinvoices"; //CNTINVC
	public static final String lnumberofcredits = "lnumberofcredits"; //CNTCR
	public static final String lnumberofdebits = "lnumberofdebits"; //CNTDR
	public static final String lnumberofpayments = "lnumberofpayments"; //CNTPAYM
	public static final String lnumberofdiscountstaken = "lnumberofdiscountstaken"; //CNTDISC
	public static final String lnumberofdiscountslost = "lnumberofdiscountslost"; //CNTLOST
	public static final String lnumberofadjustments = "lnumberofadjustments"; //CNTADJ
	public static final String lnumberofinvoicespaid = "lnumberofinvoicespaid"; //CNTINVCPD
	public static final String lnumberofdaystopay = "lnumberofdaystopay"; //CNTDTOPAY
	public static final String bdamountofinvoices = "bdamountofinvoices"; //AMTINVCHC
	public static final String bdamountofcreditnotes = "bdamountofcreditnotes"; //AMTCRHC
	public static final String bdamountofdebitnotes = "bdamountofdebitnotes"; //AMTDRHC
	public static final String bdamountofpayments = "bdamountofpayments"; //AMTPAYMHC
	public static final String bdamountofdiscounts = "bdamountofdiscounts"; //AMTDISCHC
	public static final String bdamountofdiscountslost = "bdamountofdiscountslost"; //AMTLOSTHC
	public static final String bdamountofadjustments = "bdamountofadjustments"; //AMTADJHC
	//public static final String laveragedaystopay = "laveragedaystopay"; //AVGDAYSPAY
	//public static final String lnumberofpayapplicationsusedforaveraging = "lnumberofpayapplicationsusedforaveraging";
	
	//CNTPUR - number of purchases - Not used...?
	//AMTPURHC - Amount purchased, but this isn't displayed in ACCPAC, so we're not using it.
	//AMTINVPDHC - Total amt of invoices paid - ?
	
	//Field Lengths:
	public static final int svendoracctength = 12;
	public static final int bdamountofinvoicesscale = 2;
	public static final int bdamountofcreditnotesscale = 2;
	public static final int bdamountofdebitnotesscale = 2;
	public static final int bdamountofpaymentsscale = 2;
	public static final int bdamountofdiscountsscale = 2;
	public static final int bdamountofdiscountslostscale = 2;
	public static final int bdamountofadjustmentsscale = 2;
}
