package SMDataDefinition;

public class SMTableorderdetails {

	public static String TableName = "orderdetails";
	
	//Field names
	public static String dUniqueOrderID = "dUniqueOrderID"; //NOT NULL default '0',
	public static String iDetailNumber = "iDetailNumber";
	public static String mInvoiceComments = "mInvoiceComments";
	public static String mInternalComments = "minternalcomments";
	public static String mTicketComments = "mTicketComments";
	public static String iLineNumber = "iLineNumber";
	public static String sItemNumber = "sItemNumber";
	public static String sItemDesc = "sItemDesc";
	public static String sItemCategory = "sItemCategory";
	public static String sLocationCode = "sLocationCode";
	public static String datDetailExpectedShipDate = "datDetailExpectedShipDate";
	public static String iIsStockItem = "iIsStockItem";
	public static String dQtyOrdered = "dQtyOrdered";
	public static String dQtyShipped = "dQtyShipped";
	public static String dQtyShippedToDate = "dQtyShippedToDate";
	public static String dOriginalQty = "dOriginalQty";
	public static String sOrderUnitOfMeasure = "sOrderUnitOfMeasure";
	public static String dOrderUnitPrice = "dOrderUnitPrice";
	public static String dOrderUnitCost = "dOrderUnitCost";
	public static String dExtendedOrderPrice = "dExtendedOrderPrice";
	public static String dExtendedOrderCost = "dExtendedOrderCost";
	public static String iTaxable = "iTaxable";
	public static String datLineBookedDate = "datLineBookedDate";
	public static String sMechInitial = "sMechInitial";
	public static String sMechFullName = "sMechFullName";
	public static String sLabel = "sLabel";
	public static String strimmedordernumber = "strimmedordernumber";
	public static String isuppressdetailoninvoice = "isuppressdetailoninvoice";
	public static String iprintondeliveryticket = "iprintondeliveryticket";
	public static String bdEstimatedUnitCost = "bdestimatedunitcost";
	public static String imechid = "imechid";

	//Field Lengths:
	public static int sItemNumberLength = 24;
	public static int sItemDescLength = 75;
	public static int sItemCategoryLength = 6;
	public static int sLocationCodeLength = 6;
	public static int sOrderUnitOfMeasureLength = 10;
	public static int sMechInitialLength = 4;
	public static int sMechFullNameLength = 50;
	public static int sLabelLength = 50;
	public static int strimmedordernumberLength = 22;
	
	//Number scales:
	public static int dOrderUnitPriceScale = 2;
	public static int dQtyOrderedScale = 4;
	public static int dQtyShippedScale = 4;
	public static int dQtyShippedToDateScale = 4;
	public static int dOriginalQtyScale = 4;
	public static int dOrderUnitCostScale = 2;
	public static int dExtendedOrderPriceScale = 2;
	public static int dExtendedOrderCostScale = 2;
	public static int dLineTaxAmountScale = 2;
	public static int bdEstimatedUnitCostScale = 4;
	
}
