package ServletUtilities;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

//import org.json.JSONArray;
//import org.json.JSONObject;

import org.json.simple.parser.JSONParser;

public class clsJSONFunctions {

	public static String parseJSONString(String sJASONString) throws Exception{
		
		String jsonString = "{"
				+ "\"_links"
				+ "\":{"
				+ "\"_self"
				+ "\":{"
				+ "\"Rel"
				+ "\":"
				+ "\"_self"
				+ "\","
				+ "\"Href"
				+ "\":"
				+ "\"https://cpqks.inforcloudsuite.com/RuntimeApi/EnterpriseQuoting/Entities/C_DealerQuote"
				+ "\","
				+ "\"Verb"
				+ "\":"
				+ "\"GET"
				+ "\"},"
				+ "\"_list"
				+ "\":{"
				+ "\"Rel"
				+ "\":"
				+ "\"_list"
				+ "\","
				+ "\"Href"
				+ "\":"
				+ "\"https://cpqks.inforcloudsuite.com/RuntimeApi/EnterpriseQuoting/Entities/C_DealerQuote"
				+ "\","
				+ "\"Verb"
				+ "\":"
				+ "\"GET"
				+ "\"}},"
				+ "\"_nonPersisted"
				+ "\":{},"
				+ "\"items"
				+ "\":[{"
				+ "\"Id"
				+ "\":"
				+ "\"00bac513-b658-ea11-82fa-d2da283a32ca"
				+ "\","
				+ "\"C_QuoteNumberString"
				+ "\":"
				+ "\"SQAL000008-1"
				+ "\","
				+ "\"C_CreatedBy"
				+ "\":"
				+ "\"Mid Atlantic Door Group"
				+ "\","
				+ "\"C_CreatedDate"
				+ "\":"
				+ "\"2020-02-26T16:36:09.848058Z"
				+ "\","
				+ "\"C_LastModifiedBy"
				+ "\":"
				+ "\"Mid Atlantic Door Group"
				+ "\","
				+ "\"C_LastModifiedDate"
				+ "\":"
				+ "\"2020-02-26T17:08:13.399255Z"
				+ "\","
				+ "\"C_SalesPerson"
				+ "\":"
				+ "\"Mid Atlantic Door Group"
				+ "\","
				+ "\"C_BillToName"
				+ "\":"
				+ "\"Base Customer"
				+ "\","
				+ "\"C_ShipToName"
				+ "\":"
				+ "\"Base Customer"
				+ "\","
				+ "\"C_DealerErpId"
				+ "\":"
				+ "\"163195"
				+ "\","
				+ "\"C_Name"
				+ "\":"
				+ "\"PO 62739 JCW"
				+ "\","
				+ "\"C_Status"
				+ "\":"
				+ "\"Ordered"
				+ "\",\"C_SoldFrom\":\"Beltsville\",\"C_PurchasedFrom\":\"Sectional Plant\",\"C_BillToExternalId\":null,\"_nonPersisted\":{},\"_links\":{\"_self\":{\"Rel\":\"_self\",\"Href\":\"https://cpqks.inforcloudsuite.com/RuntimeApi/EnterpriseQuoting/Entities/C_DealerQuote(00bac513-b658-ea11-82fa-d2da283a32ca)\",\"Verb\":\"GET\"},\"_update\":{\"Rel\":\"_update\",\"Href\":\"https://cpqks.inforcloudsuite.com/RuntimeApi/EnterpriseQuoting/Entities/C_DealerQuote(00bac513-b658-ea11-82fa-d2da283a32ca)\",\"Verb\":\"PUT\"},\"_list\":{\"Rel\":\"_list\",\"Href\":\"https://cpqks.inforcloudsuite.com/RuntimeApi/EnterpriseQuoting/Entities/C_DealerQuote\",\"Verb\":\"GET\"}},\"_meta\":{\"Id\":{\"fieldType\":\"Reference\",\"editable\":false,\"persisted\":true,\"validators\":{}},\"C_QuoteNumberString\":{\"fieldType\":\"String\",\"editable\":false,\"persisted\":true,\"validators\":{\"string\":true}},\"C_CreatedBy\":{\"fieldType\":\"String\",\"editable\":false,\"persisted\":true,\"validators\":{\"string\":true}},\"C_CreatedDate\":{\"fieldType\":\"DateTime\",\"editable\":false,\"persisted\":true,\"validators\":{}},\"C_LastModifiedBy\":{\"fieldType\":\"String\",\"editable\":false,\"persisted\":true,\"validators\":{\"string\":true}},\"C_LastModifiedDate\":{\"fieldType\":\"DateTime\",\"editable\":false,\"persisted\":true,\"validators\":{}},\"C_SalesPerson\":{\"fieldType\":\"String\",\"editable\":false,\"persisted\":true,\"validators\":{\"string\":true}},\"C_BillToName\":{\"fieldType\":\"String\",\"editable\":false,\"persisted\":true,\"validators\":{\"string\":true}},\"C_ShipToName\":{\"fieldType\":\"String\",\"editable\":false,\"persisted\":true,\"validators\":{\"string\":true}},\"C_DealerErpId\":{\"fieldType\":\"String\",\"editable\":false,\"persisted\":true,\"validators\":{\"string\":true}},\"C_Name\":{\"fieldType\":\"String\",\"editable\":false,\"persisted\":true,\"validators\":{\"string\":true}},\"C_Status\":{\"fieldType\":\"String\",\"editable\":false,\"persisted\":true,\"validators\":{\"string\":true}},\"C_SoldFrom\":{\"fieldType\":\"String\",\"editable\":false,\"persisted\":true,\"validators\":{\"string\":true}},\"C_PurchasedFrom\":{\"fieldType\":\"String\",\"editable\":false,\"persisted\":true,\"validators\":{\"string\":true}},\"C_BillToExternalId\":{\"fieldType\":\"String\",\"editable\":false,\"persisted\":true,\"validators\":{\"string\":true}}}}],\"currentPage\":1,\"itemsPerPage\":100,\"totalItems\":1,\"totalPages\":1,\"top\":100,\"skip\":0,\"_meta\":{\"Id\":{\"fieldType\":\"Reference\",\"validators\":{}},\"C_QuoteNumberString\":{\"fieldType\":\"String\",\"validators\":{\"string\":true}},\"C_CreatedBy\":{\"fieldType\":\"String\",\"validators\":{\"string\":true}},\"C_CreatedDate\":{\"fieldType\":\"DateTime\",\"validators\":{}},\"C_LastModifiedBy\":{\"fieldType\":\"String\",\"validators\":{\"string\":true}},\"C_LastModifiedDate\":{\"fieldType\":\"DateTime\",\"validators\":{}},\"C_SalesPerson\":{\"fieldType\":\"String\",\"validators\":{\"string\":true}},\"C_BillToName\":{\"fieldType\":\"String\",\"validators\":{\"string\":true}},\"C_ShipToName\":{\"fieldType\":\"String\",\"validators\":{\"string\":true}},\"C_DealerErpId\":{\"fieldType\":\"String\",\"validators\":{\"string\":true}},\"C_Name\":{\"fieldType\":\"String\",\"validators\":{\"string\":true}},\"C_Status\":{\"fieldType\":\"String\",\"validators\":{\"string\":true}},\"C_SoldFrom\":{\"fieldType\":\"String\",\"validators\":{\"string\":true}},\"C_PurchasedFrom\":{\"fieldType\":\"String\",\"validators\":{\"string\":true}},\"C_BillToExternalId\":{\"fieldType\":\"String\",\"validators\":{\"string\":true}}},\"_aggregateModifiedDateTimes\":{},\"_recordActionEvents\":[],\"_actionMessages\":[],\"_clientData\":{\"UserAgent\":\"Java/1.8.0_242\",\"IsDesktop\":true,\"IsMobile\":false,\"IsImpersonating\":false},\"_currentUser\":{\"Id\":\"3d12dc83-a187-4081-80ff-ab9d0137fbe1\",\"Name\":\"smcp-ohdcorp-wash@odcdc.com\",\"DisplayName\":\"smcp-ohdcorp-wash@odcdc.com\",\"Role\":\"DealerAdministrator\",\"IsApplicationAdministrator\":false,\"FormatCultureCode\":\"\",\"CountryCultureCode\":null,\"LanguageCultureCode\":null,\"ImpersonatedUserId\":null,\"ImpersonatedUserName\":null,\"TenantId\":\"OHDIRECT_TRN\",\"TimeZone\":\"UTC\",\"TimeZoneAbbreviation\":\"UTC\",\"TimeZoneOffset\":0}}";
		
		JSONParser parser = new JSONParser();
		//String sTestString = "[0,{\"1\":{\"2\":{\"3\":{\"4\":[5,{\"6\":7}]}}}}]";

		try{
			Object obj = parser.parse(jsonString);
			JSONObject jo = (JSONObject) obj;
			JSONArray jarray = (JSONArray) jo.get("items");
			JSONObject name = (JSONObject)jarray.get(0);
			System.out.println(name.get("C_Name"));
			
			for (int i = 0; i < jarray.size(); i++) {
				//JSONObject keyvaluepair = (JSONObject)jarray.get(i);
				//System.out.println(keyvaluepair.);
			}
			
		}catch(Exception e) {
	
			System.out.println(e.getMessage());
		}

		return "";
	}
	
}
