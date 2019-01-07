package smic;
import java.util.ArrayList;

import ServletUtilities.clsMasterEntry;
import smcontrolpanel.SMUtilities;

public class ICPOInvoiceLine extends clsMasterEntry{
	public static final String ParamObjectName = "Invoice line";
	
	public static final String Paramlid = "lid";
	public static final String Paramlpoinvoiceheaderid = "lpoinvoiceheaderid";
	public static final String Paramlporeceiptlineid = "lporeceiptlineid";
	public static final String Paramsinvoicenumber = "sinvoicenumber";
	public static final String Parambdreceivedcost = "bdreceivedcost";
	public static final String Parambdinvoicedcost = "bdinvoicedcost";
	public static final String Paramsexpenseaccount = "sexpenseaccount";
	public static final String Parambdqtyreceived = "bdqtyreceived";
	public static final String Paramsitemnumber = "sitemnumber";
	public static final String Paramsitemdescription = "sitemdescription";
	public static final String Paramslocation = "slocation";
	public static final String Paramsunitofmeasure = "sunitofmeasure";
	public static final String Paramlnoninventoryitem = "lnoninventoryitem";
	public static final String Paramlporeceiptid = "lporeceiptid";
	
	private String m_slid;
	private String m_slpoinvoiceheaderid;
	private String m_slporeceiptlineid;
	private String m_ssinvoicenumber;
	private String m_sbdreceivedcost;
	private String m_sbdinvoicedcost;
	private String m_ssexpenseaccount;
	private String m_sbdqtyreceived;
	private String m_ssitemnumber;
	private String m_ssitemdescription;
	private String m_sslocation;
	private String m_ssunitofmeasure;
	private String m_slnoninventoryitem;
	private String m_slporeceiptid;
	private ArrayList<String> m_sErrorMessageArray = new ArrayList<String> (0);
	
    public ICPOInvoiceLine() {
		super();
		initBidVariables();
        }
    
	public String getslid() {
		return m_slid;
	}

	public void setslid(String mSlid) {
		m_slid = mSlid;
	}

	public String getspoinvoiceheaderid() {
		return m_slpoinvoiceheaderid;
	}
	public void setspoinvoiceheaderid(String sPOInvoiceHeaderID) {
		m_slpoinvoiceheaderid = sPOInvoiceHeaderID;
	}

	public String getsporeceiptlineid() {
		return m_slporeceiptlineid;
	}
	public void setsporeceiptlineid(String sPOReceiptLineID) {
		m_slporeceiptlineid = sPOReceiptLineID;
	}
	
	public String getsporeceiptid() {
		return m_slporeceiptid;
	}
	public void setsporeceiptid(String sPOReceiptID) {
		m_slporeceiptid = sPOReceiptID;
	}

	public String getsinvoicenumber() {
		return m_ssinvoicenumber;
	}
	public void setsinvoicenumber(String sInvoiceNumber) {
		m_ssinvoicenumber = sInvoiceNumber;
	}

	public String getsreceivedcost() {
		return m_sbdreceivedcost;
	}
	public void setsreceivedcost(String sReceivedCost) {
		m_sbdreceivedcost = sReceivedCost;
	}
	
	public String getsinvoicedcost() {
		return m_sbdinvoicedcost;
	}
	public void setsinvoicedcost(String sInvoicedCost) {
		m_sbdinvoicedcost = sInvoicedCost;
	}
	
	public String getsexpenseaccount() {
		return m_ssexpenseaccount;
	}
	public void setsexpenseaccount(String sExpenseAccount) {
		m_ssexpenseaccount = sExpenseAccount;
	}
	
	public String getsqtyreceived() {
		return m_sbdqtyreceived;
	}
	public void setsqtyreceived(String sQtyReceived) {
		m_sbdqtyreceived = sQtyReceived;
	}

	public String getsitemnumber() {
		return m_ssitemnumber;
	}
	public void setsitemnumber(String sItemNumber) {
		m_ssitemnumber = sItemNumber;
	}
	
	public String getsitemdescription() {
		return m_ssitemdescription;
	}
	public void setsitemdescription(String sItemDescription) {
		m_ssitemdescription = sItemDescription;
	}
	
	public String getslocation() {
		return m_sslocation;
	}
	public void setslocation(String sLocation) {
		m_sslocation = sLocation;
	}

	public String getsunitofmeasure() {
		return m_ssunitofmeasure;
	}
	public void setsunitofmeasure(String sUnitOfMeasure) {
		m_ssunitofmeasure = sUnitOfMeasure;
	}

	public String getsnoninventoryitem() {
		return m_slnoninventoryitem;
	}
	public void setsnoninventoryitem(String sNonInventoryItem) {
		m_slnoninventoryitem = sNonInventoryItem;
	}
	
	public String read_out_debug_data(){
    	String sResult = "  ** " + SMUtilities.getFullClassName(this.toString()) + " read out: ";
    	sResult += "\nID: " + this.getslid();
    	sResult += "\nPO Invoice Header ID: " + this.getspoinvoiceheaderid();
    	sResult += "\nReceipt ID: " + this.getsporeceiptid();
    	sResult += "\nReceipt Line ID: " + this.getsporeceiptlineid();
    	sResult += "\nInvoice number: " + this.getsinvoicenumber();
    	sResult += "\nReceived cost: " + this.getsreceivedcost();
    	sResult += "\nInvoiced cost: " + this.getsinvoicedcost();
    	sResult += "\nExpense account: " + this.getsexpenseaccount();
    	sResult += "\nQty received: " + this.getsqtyreceived();
    	sResult += "\nItem number: " + this.getsitemnumber();
    	sResult += "\nItem description: " + this.getsitemdescription();
    	sResult += "\nLocation: " + this.getslocation();
    	sResult += "\nUOM: " + this.getsunitofmeasure();
    	sResult += "\nNon inv. item: " + this.getsnoninventoryitem();
    	sResult += "\nObject name: " + ParamObjectName;
    	
    	return sResult;
    }

    public void addErrorMessage(String sMsg){
    	super.addErrorMessage(sMsg);
    }

    private void initBidVariables(){
    	m_slid = "-1";
    	m_slpoinvoiceheaderid = "-1";
    	m_slporeceiptlineid = "-1";
    	m_slporeceiptid = "-1";
    	m_ssinvoicenumber = "";
    	m_sbdreceivedcost = "0.00";
    	m_sbdinvoicedcost = "0.00";
    	m_ssexpenseaccount = "";
    	m_sbdqtyreceived = "0.0000";
    	m_ssitemnumber = "";
    	m_ssitemdescription = "";
    	m_sslocation = "";
    	m_ssunitofmeasure = "";
    	m_slnoninventoryitem = "0";
    	m_sErrorMessageArray = new ArrayList<String> (0);
    	super.setObjectName(ParamObjectName);
	}
	public String getErrorMessageString(){
		String s = "";
		for (int i = 0; i < m_sErrorMessageArray.size(); i ++){
			s += "<BR>" + m_sErrorMessageArray.get(i);
		}
		return s;
	}
}
