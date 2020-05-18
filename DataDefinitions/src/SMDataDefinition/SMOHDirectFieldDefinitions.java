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
	
	Useful link for syntax information: https://www.odata.org/documentation/odata-version-2-0/uri-conventions/#_45_filter_system_query_option_filter_13
	
	
	*/
	//HODirect API link base
	public static final String OHDIRECT_API_LINK_BASE = "https://mingle-ionapi.inforcloudsuite.com/OHDIRECT_TRN/CPQEQ/RuntimeApi/EnterpriseQuoting/Entities/";
	
	//'Endpoint Display Names'
	public static final String ENDPOINT_QUOTE_NAME = "OHDirect Quote";
	public static final String ENDPOINT_QUOTELINE_NAME = "OHDirect Quote Line";
	public static final String ENDPOINT_QUOTELINEDETAIL_NAME = "OHDirect Quote Line Detail";
	public static final String ENDPOINT_ORDER_NAME = "OHDirect Order";
	public static final String ENDPOINT_ORDERLINE_NAME = "OHDirect Order Line";
	public static final String ENDPOINT_ORDERLINEDETAIL_NAME = "OHDirect Order Line Detail";
	
	//'Endpoint definitions':
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
	public static final String QUOTELINEDETAIL_ARRAY_NAME = "items";
	public static final String QUOTELINEDETAIL_FIELD_ID = "ID"; //Unique ID for this quote line detail
	public static final String QUOTELINEDETAIL_FIELD_QUOTELINEID = "C_QuoteLine"; // the ID of the parent quote line
	public static final String QUOTELINEDETAIL_FIELD_DESCRIPTION = "C_Description"; // the label of the detail
	public static final String QUOTELINEDETAIL_FIELD_VALUE = "C_Value"; // the actual text value of the detail
	public static final String QUOTELINEDETAIL_FIELD_SORTORDER = "C_SortOrder"; // order in which the details appear on the entry screens
	
	
	
	
	
	
}
