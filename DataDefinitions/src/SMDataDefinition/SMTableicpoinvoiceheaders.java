package SMDataDefinition;

public class SMTableicpoinvoiceheaders {

	public static final String TableName = "icpoinvoiceheaders";
	public static final String POINVOICE_SIGNATURE = "SMCP-PO";
	
	//Field names
	public static final String lid = "lid";
	public static final String lreceiptid = "lreceiptid";
	public static final String sinvoicenumber = "sinvoicenumber";
	public static final String datinvoice = "datinvoice";
	public static final String sterms = "sterms";
	public static final String datdiscount = "datdiscount";
	public static final String datdue = "datdue";
	public static final String bddiscount = "bddiscount";
	public static final String lexportsequencenumber = "lexportsequencenumber";
	public static final String bdinvoicetotal = "bdinvoicetotal";
	public static final String svendor = "svendor";
	public static final String svendorname = "svendorname";
	public static final String lpoheaderid = "lpoheaderid";
	public static final String bdreceivedamount = "bdreceivedamount";
	public static final String sdescription = "sdescription";
	public static final String staxjurisdiction = "staxjurisdiction"; //12) default NULL,
	public static final String datentered = "datentered";
	
	//TJR - added 1/29/2016:
	public static final String itaxid = "itaxid";
	public static final String bdtaxrate = "bdtaxrate";
	public static final String staxtype = "staxtype";
	public static final String icalculateonpurchaseorsale = "icalculateonpurchaseorsale";
	
	//TJR - added 3/27/2018:
	public static final String iinvoiceincludestax = "iinvoiceincludestax";
	
	//Field lengths
	public static int sinvoicenumberLength = 22;
	public static int stermsLength = SMTableicvendorterms.sTermsCodeLength;
	public static int svendorLength = SMTableicvendors.svendoracctLength;
	public static int svendornameLength = SMTableicvendors.snameLength;
	public static int sdescriptionLength = 60;
	public static int staxjurisdictionLength = 12;
	public static int staxtypeLength = 254;
	
	//Field scales
	public static int bddiscountScale = 2;
	public static int bdinvoicetotalScale = 2;
	public static int bdreceivedamountScale = 2;
	public static int bdtaxrateScale = 4;
}
