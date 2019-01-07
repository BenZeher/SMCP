package SMDataDefinition;

public class SMTableworkorderdetails {
	//Table Name
	public static final String TableName = "workorderdetails";
	
	//Field names:
	public static final String lid  = "lid";
	public static final String lworkorderid = "lworkorderid";
	public static final String idetailtype = "idetailtype";
	public static final String sitemnumber = "sitemnumber";
	public static final String sitemdesc = "sitemdesc";
	public static final String lorderdetailnumber = "lorderdetailnumber";
	public static final String llinenumber = "llinenumber";
	public static final String lworkperformedlinenumber = "lworkperformedlinenumber";
	public static final String sworkperformedcode = "sworkperformedcode";
	public static final String sworkperformed = "sworkperformed";
	public static final String bdquantity = "bdquantity";
	public static final String bdunitprice = "bdunitprice";
	public static final String sunitofmeasure = "sunitofmeasure";
	public static final String bdqtyassigned = "bdqtyassigned";
	public static final String lsetpricetozero = "lsetpricetozero";
	public static final String bdextendedprice = "bdextendedprice";
	public static final String slocationcode = "slocationcode";
	
	public static final int sitemnumberLength = 24;
	public static final int sitemdescLength = 75;
	public static final int sworkperformedcodeLength = 50;
	public static final int sworkperformedLength = 254;
	public static final int sunitofmeasureLength = 10;
	public static final int slocationcodeLength = 6;
	public static final int bdquantityDecimals = 4;
	public static final int bdunitpriceDecimals = 2;
	public static final int bdextendedpriceDecimals = 2;
	public static final int bdqtyassignedDecimals = 4;
	
	public static final int WORK_ORDER_DETAIL_TYPE_ITEM = 0;
	public static final int WORK_ORDER_DETAIL_TYPE_WORKPERFORMED = 1;
}