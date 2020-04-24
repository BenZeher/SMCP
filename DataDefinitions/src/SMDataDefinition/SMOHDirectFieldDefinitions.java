package SMDataDefinition;

public class SMOHDirectFieldDefinitions {

	//'Endpoints':
	public static final String ENDPOINT_QUOTE = "C_DealerQuote";
	public static final String ENDPOINT_QUOTELINE = "C_DealerQuoteLine";
	public static final String ENDPOINT_QUOTELINEDETAIL = "C_DealerQuoteLineDetail";
	public static final String ENDPOINT_ORDER = "C_DealerOrder";
	public static final String ENDPOINT_ORDERLINE = "C_DealerOrderline";
	public static final String ENDPOINT_ORDERLINEDETAIL = "C_DealerOrderlineDetail";
	
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
	public static final String QUOTELINE_FIELD_QUOTENUMBER = "C_Quote"; // "00bac513-b658-ea11-82fa-d2da283a32ca"
	public static final String QUOTELINE_FIELD_LINENUMBER = "C_LineNumber"; // Long integer
	public static final String QUOTELINE_FIELD_DESCRIPTION = "C_Description"; // "521 Series Commercial Aluminum Sectional Door"
	public static final String QUOTELINE_FIELD_LASTCONFIGURATIONDESCRIPTION = "C_LastConfigurationDescription"; // "521, 14'2\" x 14'1\""
	public static final String QUOTELINE_FIELD_QUANTITY = "C_Quantity"; // Long integer
	public static final String QUOTELINE_FIELD_UNITCOST = "C_UnitCost"; // Decimal
	public static final String QUOTELINE_FIELD_SELLINGPRICE = "C_SellingPrice"; // Decimal
	public static final String QUOTELINE_FIELD_TOTALCOST = "C_TotalCost"; // Decimal
	public static final String QUOTELINE_FIELD_TOTALSELLINGPRICE = "C_TotalSellingPrice"; // Decimal
	public static final String QUOTELINE_FIELD_LABEL = "C_Label"; // ""
	
	//QUOTE LINE DETAILS:
	public static final String QUOTELINEDETAIL_FIELD_ID = "ID";
	public static final String QUOTELINEDETAIL_FIELD_QUOTELINEID = "C_QuoteLine";
	public static final String QUOTELINEDETAIL_FIELD_DESCRIPTION = "C_Description";
	public static final String QUOTELINEDETAIL_FIELD_VALUE = "C_Value";
	public static final String QUOTELINEDETAIL_FIELD_SORTORDER = "C_SortOrder";
	
	
	
	
	
	
}
