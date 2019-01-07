package SMDataDefinition;

public class SMTablearmonthlystatistics {
	//Table Name
	public static final String TableName = "armonthlystatistics";
	
	//Field names:
	//In ACCPAC, these come from the 'ARCSM' table:
	public static final String sCustomerNumber = "scustomernumber";//IDCUST
	public static final String sYear = "lyear"; //CNTYR
	public static final String sMonth = "lmonth"; //CNPERD
	public static final String sInvoiceTotal = "bdinvoicetotal"; //"AMTINVCHC"
	public static final String sCreditTotal = "bdcredittotal"; //"AMTCRHC"
	public static final String sPaymentTotal = "bdpaymenttotal"; //"AMTPAYMHC"
	public static final String sNumberOfInvoices = "lnumberofinvoices"; //CNTINVC
	public static final String sNumberOfCredits = "lnumberofcredits"; //CNTCR
	public static final String sNumberOfPayments = "lnumberofpayments"; //CNTPAYM
	public static final String sAverageDaysToPay = "bdaveragedaystopay"; //"AVGDAYSPAY"
	public static final String sNumberOfPaidInvoices = "lnumberofpaidinvoices"; //CNTINVCPD
	public static final String sTotalNumberOfDaysToPay = "ltotalnumberofdaystopay"; //CNTDTOPAY
	
	//Field Lengths:
	public static final int sCustomerNumberLength = 12;

}
