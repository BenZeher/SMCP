package SMDataDefinition;

public class SMTableinvoicedetails {
	public static final String TableName = "invoicedetails";
	
	public static final String iIsStockItem = "iIsStockItem";
	public static final String dExtendedPrice = "dExtendedPrice";
	public static final String dQtyShipped = "dQtyShipped";
	public static final String dUnitPrice = "dUnitPrice";
	public static final String iDetailNumber = "iDetailNumber";
	public static final String iTaxable = "iTaxable";
	public static final String iLineNumber = "iLineNumber";
	public static final String mDetailInvoiceComment = "mDetailInvoiceComment";
	public static final String sDesc = "sDesc";
	public static final String sInvoiceNumber = "sInvoiceNumber";
	public static final String sItemCategory = "sItemCategory";
	public static final String sItemNumber = "sItemNumber";
	public static final String sLocationCode = "sLocationCode";
	public static final String sUnitOfMeasure = "sUnitOfMeasure";
	public static final String sInventoryGLAcct = "sInventoryGLAcct";
	public static final String sExpenseGLAcct = "sExpenseGLAcct";
	public static final String sRevenueGLAcct = "sRevenueGLAcct";
	public static final String dExtendedCost = "dExtendedCost";
	public static final String dExtendedPriceAfterDiscount = "dExtendedPriceAfterDiscount";  //KEEP THIS - WE NEED IT!
	public static final String bdlinesalestaxamount = "bdlinesalestaxamount";  //TJR - 1/21/2016 - changed this name and type - also changed name again on 2/4/2016
	public static final String iMatchingInvoiceLineNumber = "iMatchingInvoiceLineNumber";
	public static final String sMechInitial = "sMechInitial";
	public static final String sMechFullName = "sMechFullName";
	public static final String sLabel = "sLabel";
	public static final String lictransactionid = "lictransactionid";
	public static final String isuppressdetailoninvoice = "isuppressdetailoninvoice";
	public static final String imechid = "imechid";
	public static final String ilaboritem = "ilaboritem";

	//Field lengths:
	public static final int sDesclength = 75;
	public static final int sInvoiceNumberlength = 15;
	public static final int sItemCategorylength = 6;
	public static final int sItemNumberlength = 24;
	public static final int sLocationCodelength = 6;
	public static final int sUnitOfMeasurelength = 10;
	public static final int sInventoryGLAcctlength = 45;
	public static final int sExpenseGLAcctlength = 45;
	public static final int sRevenueGLAcctlength = 45;
	public static final int sMechInitiallength = 4;
	public static final int sMechFullNamelength = 50;
	public static final int sLabellength = 50;
	public static final int bdlinetaxamountscale = 2;
	

}
