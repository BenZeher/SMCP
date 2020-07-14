package SMDataDefinition;

public class SMOHDirectFieldDefinitions {

	//SAMPLE REQUESTS:
	/*
	REQUEST QUOTES LAST MODIFIED LATER THAN 1/9/2020:
	https://mingle-ionapi.inforcloudsuite.com/OHDIRECT_TRN/CPQEQ/RuntimeApi/EnterpriseQuoting/Entities/C_DealerQuote?%24filter=C_LastModifiedDate%20gt%20'2020-01-09'
	
	REQUEST A SINGLE QUOTE BY QUOTE NUMBER STRING:
	https://mingle-ionapi.inforcloudsuite.com/OHDIRECT_TRN/CPQEQ/RuntimeApi/EnterpriseQuoting/Entities/C_DealerQuote?%24filter=C_QuoteNumberString%20eq%20'SQAL000008-1'
	
	REQUEST ALL QUOTES WITH 'PO64107' IN THE Name field:
	https://mingle-ionapi.inforcloudsuite.com/OHDIRECT_TRN/CPQEQ/RuntimeApi/EnterpriseQuoting/Entities/C_DealerQuote?%24filter=substringof('PO64107'%2C%20C_C_Name)%20eq%20true
	
	REQUEST ALL THE LINES ON A SINGLE QUOTE:
	https://mingle-ionapi.inforcloudsuite.com/OHDIRECT_TRN/CPQEQ/RuntimeApi/EnterpriseQuoting/Entities/C_DealerQuoteLine?%24filter=C_Quote%20eq%20'00bac513-b658-ea11-82fa-d2da283a32ca'
	
	REQUEST ALL THE QUOTE LINE DETAILS FOR A QUOTE LINE:
	https://mingle-ionapi.inforcloudsuite.com/OHDIRECT_TRN/CPQEQ/RuntimeApi/EnterpriseQuoting/Entities/C_DealerQuoteLineDetail?%24filter=Id%20eq%20'ea51bac1-b858-ea11-82f9-98456f859bd9'	
	
	REQUEST ALL THE LINE COST DETAILS FOR A QUOTE LINE:
	https://mingle-ionapi.inforcloudsuite.com/OHDIRECT_TRN/CPQEQ/RuntimeApi/EnterpriseQuoting/Entities/C_DealerQuoteLineCostDetail?%24filter=C_QuoteLine%20eq%20'ea51bac1-b858-ea11-82f9-98456f859bd9'	
	
	
	Useful link for syntax information: https://www.odata.org/documentation/odata-version-2-0/uri-conventions/#_45_filter_system_query_option_filter_13
	
	
	*/
	//HODirect API link base
	public static final String OHDIRECT_API_LINK_BASE = "https://mingle-ionapi.inforcloudsuite.com/OHDIRECT_TRN/CPQEQ/RuntimeApi/EnterpriseQuoting/Entities/";
	
	//'Endpoint Display Names'
	public static final String ENDPOINT_QUOTE_NAME = "Dealer Quote";
	public static final String ENDPOINT_QUOTELINE_NAME = "Dealer Quote Line";
	public static final String ENDPOINT_QUOTELINEDETAIL_NAME = "Dealer Quote Line Detail";
	public static final String ENDPOINT_QUOTELINECOSTDETAIL_NAME = "Dealer Quote Line Cost Detail";
	public static final String ENDPOINT_ORDER_NAME = "Dealer Order";
	public static final String ENDPOINT_ORDERLINE_NAME = "Dealer Order Line";
	public static final String ENDPOINT_ORDERLINEDETAIL_NAME = "Dealer Order Line Detail";
	public static final String ENDPOINT_ORDERLINECOSTDETAIL_NAME = "Dealer Order Line Cost Detail";
	
	//'Endpoint definitions':
	public static final String ENDPOINT_QUOTE = "C_DealerQuote";
	public static final String ENDPOINT_QUOTELINE = "C_DealerQuoteLine";
	public static final String ENDPOINT_QUOTELINEDETAIL = "C_DealerQuoteLineDetail";
	public static final String ENDPOINT_QUOTELINECOSTDETAIL = "C_DealerQuoteLineCostDetail";
	public static final String ENDPOINT_ORDER = "C_DealerOrder";
	public static final String ENDPOINT_ORDERLINE = "C_DealerOrderline";
	public static final String ENDPOINT_ORDERLINEDETAIL = "C_DealerOrderlineDetail";
	public static final String ENDPOINT_ORDERLINECOSTDETAIL = "C_DealerOrderLineCostDetail";
	

	//Fields returned from the queries:
	//QUOTE:
	public static final String QUOTE_FIELD_ID = "Id";
	public static final String QUOTE_FIELD_QUOTENUMBER = "C_QuoteNumberString";
	public static final String QUOTE_FIELD_CREATEDBY = "C_CreatedBy";
	public static final String QUOTE_FIELD_CREATEDDATE = "C_CreatedDate"; // "2020-02-26T16:36:09.848058Z"
	public static final String QUOTE_FIELD_LASTMODIFIEDBY = "C_LastModifiedBy";
	public static final String QUOTE_FIELD_LASTMODIFIEDDATE = "C_LastModifiedDate";
	public static final String QUOTE_FIELD_SALESPERSON = "C_SalesPerson";
	public static final String QUOTE_FIELD_BILLTONAME = "C_BillToName";
	public static final String QUOTE_FIELD_SHIPTONAME = "C_ShipToName";
	public static final String QUOTE_FIELD_DEALERERPID = "C_DealerErpId";
	public static final String QUOTE_FIELD_NAME = "C_Name";
	public static final String QUOTE_FIELD_STATUS = "C_Status";
	public static final String QUOTE_FIELD_SOLDFROM = "C_SoldFrom";
	public static final String QUOTE_FIELD_PURCHASEDFROM = "C_PurchasedFrom";
	
	//QUOTE LINES:
	public static final String QUOTELINE_ARRAY_NAME = "items";
	public static final String QUOTELINE_FIELD_ID = "Id"; // "ea51bac1-b858-ea11-82f9-98456f859bd9"
	public static final String QUOTELINE_FIELD_QUOTENUMBER = "C_Quote"; // Actually, the ID, for example: "00bac513-b658-ea11-82fa-d2da283a32ca"
	public static final String QUOTELINE_FIELD_LINENUMBER = "C_LineNumber"; // Long integer
	public static final String QUOTELINE_FIELD_DESCRIPTION = "C_Description"; // "521 Series Commercial Aluminum Sectional Door"
	public static final String QUOTELINE_FIELD_LASTCONFIGURATIONDESCRIPTION = "C_LastConfigurationDescription"; // "521, 14'2\" x 14'1\""
	public static final String QUOTELINE_FIELD_QUANTITY = "C_Quantity"; // Long integer
	public static final String QUOTELINE_FIELD_UNITCOST = "C_UnitCost"; // Decimal
	public static final String QUOTELINE_FIELD_SELLINGPRICE = "C_SellingPrice"; // Decimal - unit sell price
	public static final String QUOTELINE_FIELD_TOTALCOST = "C_TotalCost"; // Decimal
	public static final String QUOTELINE_FIELD_TOTALSELLINGPRICE = "C_TotalSellingPrice"; // Decimal
	public static final String QUOTELINE_FIELD_LABEL = "C_Label"; // ""
	
	//QUOTE LINE DETAILS:
	public static final String QUOTELINEDETAIL_ARRAY_NAME = "items";
	public static final String QUOTELINEDETAIL_FIELD_ID = "ID"; //Unique ID for this quote line detail
	public static final String QUOTELINEDETAIL_FIELD_QUOTELINEID = "C_QuoteLine"; // the ID of the parent quote line
	public static final String QUOTELINEDETAIL_FIELD_DESCRIPTION = "C_Description"; // the label of the detail
	public static final String QUOTELINEDETAIL_FIELD_VALUE = "C_Value"; // the actual text value of the detail
	public static final String QUOTELINEDETAIL_FIELD_SORTORDER = "C_SortOrder"; // order in which the details appear on the entry screens
	
	//QUOTE LINE COST DETAILS:
	public static final String QUOTELINECOSTDETAIL_ARRAY_NAME = "items";
	public static final String QUOTELINECOSTDETAIL_ID = "ID"; //The unique Id of the line
	public static final String QUOTELINECOSTDETAIL_QUOTE_LINE_ID = "C_QuoteLine"; // The ID from C_DealerQuoteLine
	public static final String QUOTELINECOSTDETAIL_DESCRIPTION = "C_Description"; //The description from the pricing line.
	public static final String QUOTELINECOSTDETAIL_QUANTITY = "C_Quantity"; //The quantity of the priced item.
	public static final String QUOTELINECOSTDETAIL_LIST_PRICE = "C_ListPrice"; //The list price and unit of measure for the item.
	public static final String QUOTELINECOSTDETAIL_DISCOUNT_MULTIPLIER = "C_DiscountMultiplier"; //The value of either the multiplier or discount applied.
	public static final String QUOTELINECOSTDETAIL_BASE_PRICE = "C_BasePrice"; //Pricing related to the base system.
	public static final String QUOTELINECOSTDETAIL_OPTION_PRICE = "C_OptionPrice"; //Pricing related to options to the system.

	//ORDER:
	public static final String ORDER_FIELD_ID = "Id";
	public static final String ORDER_FIELD_QUOTEID = "C_QuoteID";
	public static final String ORDER_FIELD_ORDERNUMBER = "C_OrderNumberString";
	public static final String ORDER_FIELD_CREATEDBY = "C_CreatedBy";
	public static final String ORDER_FIELD_CREATEDDATE = "C_CreatedDate"; // "2020-02-26T16:36:09.848058Z"
	public static final String ORDER_FIELD_LASTMODIFIEDBY = "C_LastModifiedBy";
	public static final String ORDER_FIELD_LASTMODIFIEDDATE = "C_LastModifiedDate";
	public static final String ORDER_FIELD_SALESPERSON = "C_SalesPerson";
	public static final String ORDER_FIELD_BILLTONAME = "C_BillToName";
	public static final String ORDER_FIELD_SHIPTONAME = "C_ShipToName";
	public static final String ORDER_FIELD_DEALERERPID = "C_DealerErpId";
	public static final String ORDER_FIELD_STATUS = "C_Status";
	public static final String ORDER_FIELD_BILLTOEXTERNALID = "C_BillToExternalID";
	public static final String ORDER_FIELD_SOLDFROM = "C_SoldFrom";
	public static final String ORDER_FIELD_PURCHASEDFROM = "C_PurchasedFrom";
	
	//ORDER LINES:
	public static final String ORDERLINE_ARRAY_NAME = "items";
	public static final String ORDERLINE_FIELD_ID = "Id"; 
	public static final String ORDERLINE_FIELD_ORDER = "C_Order"; 
	public static final String ORDERLINE_FIELD_LINENUMBER = "C_LineNumber"; // Long integer
	public static final String ORDERLINE_FIELD_DESCRIPTION = "C_Description"; // "521 Series Commercial Aluminum Sectional Door"
	public static final String ORDERLINE_FIELD_LASTCONFIGURATIONDESCRIPTION = "C_LastConfigurationDescription"; // "521, 14'2\" x 14'1\""
	public static final String ORDERLINE_FIELD_QUANTITY = "C_Quantity"; // Long integer
	public static final String ORDERLINE_FIELD_UNITCOST = "C_UnitCost"; // Decimal
	public static final String ORDERLINE_FIELD_TOTALCOST = "C_TotalCost"; // Decimal
	public static final String ORDERLINE_FIELD_LABEL = "C_Label"; // ""
	
	//ORDER LINE DETAILS:
	public static final String ORDERLINEDETAIL_ARRAY_NAME = "items";
	public static final String ORDERLINEDETAIL_FIELD_ID = "Id"; //Unique ID for this order line detail
	public static final String ORDERLINEDETAIL_FIELD_ORDERLINEID = "C_OrderLine"; // the ID of the parent order line
	public static final String ORDERLINEDETAIL_FIELD_DESCRIPTION = "C_Description"; // the label of the detail
	public static final String ORDERLINEDETAIL_FIELD_VALUE = "C_Value"; // the actual text value of the detail
	public static final String ORDERLINEDETAIL_FIELD_SORTORDER = "C_SortOrder"; // order in which the details appear on the entry screens
	
	//ORDER LINE COST DETAILS:
	public static final String ORDERLINECOSTDETAIL_ARRAY_NAME = "items";
	public static final String ORDERLINECOSTDETAIL_ID = "Id"; //The unique Id of the line
	public static final String ORDERLINECOSTDETAIL_ORDER_LINE_ID = "C_OrderLine"; // The ID from C_DealerOrderLine
	public static final String ORDERLINECOSTDETAIL_DESCRIPTION = "C_Description"; //The description from the pricing line.
	public static final String ORDERLINECOSTDETAIL_QUANTITY = "C_Quantity"; //The quantity of the priced item.
	public static final String ORDERLINECOSTDETAIL_LIST_PRICE = "C_ListPrice"; //The list price and unit of measure for the item.
	public static final String ORDERLINECOSTDETAIL_DISCOUNT_MULTIPLIER = "C_DiscountMultiplier"; //The value of either the multiplier or discount applied.
	public static final String ORDERLINECOSTDETAIL_BASE_PRICE = "C_BasePrice"; //Pricing related to the base system.
	public static final String ORDERLINECOSTDETAIL_OPTION_PRICE = "C_OptionPrice"; //Pricing related to options to the system.
}
