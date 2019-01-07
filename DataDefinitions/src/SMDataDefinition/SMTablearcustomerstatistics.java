package SMDataDefinition;

public class SMTablearcustomerstatistics {
	//Table Name
	public static final String TableName = "arcustomerstatistics";
	
	//Field names:
	public static final String sCustomerNumber = "scustomernumber";
	public static final String sCurrentBalance = "bdcurrentbalance";//AMTBALDUEH
	public static final String sDateOfLastInvoice = "datlastinvoice"; //"DATELASTIV"
	public static final String sDateOfLastCredit = "datlastcredit"; //"DATELASTCR"
	public static final String sDateOfLastPayment = "datlastpayment"; //"DATELASTPA"
	public static final String sAmountOfLastInvoice = "bdamountoflastinvoice"; //"AMTLASTIVH"
	public static final String sAmountOfLastCredit = "bdamountoflastcredit"; //"AMTLASTCRH"
	public static final String sAmountOfLastPayment = "bdamountoflastpayment"; //"AMTLASTPYH"
	public static final String sAmountOfHighestInvoice = "bdamountofhighestinvoice"; //"AMTINVHIH"
	public static final String sAmountOfHighestInvoiceLastYear = "bdamountofhighestinvoicelastyear"; //"AMTINVHILH"
	public static final String sHighestBalance = "bdhighestbalance"; //"AMTBALHIH"
	public static final String sHighestBalanceLastYear = "bdhighestbalancelastyear"; //"AMTBALHILH"
	//number of transactions used to calculate average
	public static final String sTotalNumberOfPaidInvoices = "ltotalnumberofpaidinvoices"; //"CNTINVPAID"
	public static final String sNumberOfOpenInvoices = "lnumberofopeninvoices";//CNTOPENINV
	public static final String sTotalDaysToPay = "ltotalnumberofdaystopay";//"DAYSTOPAY"
	
	//Field Lengths:
	public static final int sCustomerNumberLength = 12;

}
