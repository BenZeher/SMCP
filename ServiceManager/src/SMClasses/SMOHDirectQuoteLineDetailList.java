package SMClasses;

import java.math.BigDecimal;
import java.sql.Connection;
import java.util.ArrayList;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import SMDataDefinition.SMOHDirectFieldDefinitions;

/*
 Sample full request URL to obtain the Quote Line Details on quote line ID: 'ecbb8a6f-0a46-4036-b8b1-ab8700e4cb20'
https://mingle-ionapi.inforcloudsuite.com/OHDIRECT_TRN/CPQEQ/RuntimeApi/EnterpriseQuoting/Entities/C_DealerQuoteLineDetail?$filter=C_QuoteLine%20eq%20'ecbb8a6f-0a46-4036-b8b1-ab8700e4cb20'
 */

public class SMOHDirectQuoteLineDetailList {

	ArrayList<String> arrQuoteLineDetailIDs;
	ArrayList<String> arrQuoteLineIDs;
	ArrayList<String> arrDescriptions;
	ArrayList<String> arrValues;
	ArrayList<BigDecimal> arrSortOrders;
	
	public SMOHDirectQuoteLineDetailList() {
		
	}
	
	public void getQuoteLineDetailList(String sRequestString, Connection conn, String sDBID, String sUserID) throws Exception{
		arrQuoteLineIDs = new ArrayList<String>(0);
		arrQuoteLineDetailIDs = new ArrayList<String>(0);
		arrDescriptions = new ArrayList<String>(0);
		arrValues = new ArrayList<String>(0);
		arrSortOrders = new ArrayList<BigDecimal>(0);
		
		//Try to read the list:
		String sJSONResult = "";
		try {
			sJSONResult = ServletUtilities.clsOEAuthFunctions.requestOHDirectData(conn, sRequestString, sDBID, sUserID);
		} catch (Exception e) {
			throw new Exception("Error [202004231717] - " + e.getMessage());
		}
		
		//Try to parse the list:
		JSONParser parser = new JSONParser();
		try{
			Object obj = parser.parse(sJSONResult);
			JSONObject jo = (JSONObject) obj;
			JSONArray jarray = (JSONArray) jo.get(SMOHDirectFieldDefinitions.QUOTELINEDETAIL_ARRAY_NAME);
			
			for (int i = 0; i < jarray.size(); i++) {
				JSONObject objQuoteLine = (JSONObject)jarray.get(i);
				if (objQuoteLine.get(SMOHDirectFieldDefinitions.QUOTELINEDETAIL_FIELD_DESCRIPTION) == null) {
					arrDescriptions.add("");
				}else {
					arrDescriptions.add((String)objQuoteLine.get(SMOHDirectFieldDefinitions.QUOTELINEDETAIL_FIELD_DESCRIPTION));
				}
				if (objQuoteLine.get(SMOHDirectFieldDefinitions.QUOTELINEDETAIL_FIELD_ID) == null) {
					arrQuoteLineDetailIDs.add("");
				}else {
					arrQuoteLineDetailIDs.add((String)objQuoteLine.get(SMOHDirectFieldDefinitions.QUOTELINEDETAIL_FIELD_ID));
				}
				if (objQuoteLine.get(SMOHDirectFieldDefinitions.QUOTELINEDETAIL_FIELD_VALUE) == null) {
					arrValues.add("");
				}else {
					arrValues.add((String)objQuoteLine.get(SMOHDirectFieldDefinitions.QUOTELINEDETAIL_FIELD_VALUE));				}
				if (objQuoteLine.get(SMOHDirectFieldDefinitions.QUOTELINEDETAIL_FIELD_SORTORDER) == null) {
					arrSortOrders.add(new BigDecimal("0.0000"));
				}else {
					arrSortOrders.add(BigDecimal.valueOf((Double)objQuoteLine.get(SMOHDirectFieldDefinitions.QUOTELINEDETAIL_FIELD_SORTORDER)));
				}
				if (objQuoteLine.get(SMOHDirectFieldDefinitions.QUOTELINEDETAIL_FIELD_QUOTELINEID) == null) {
					arrQuoteLineIDs.add("");
				}else {
					arrQuoteLineIDs.add((String)objQuoteLine.get(SMOHDirectFieldDefinitions.QUOTELINEDETAIL_FIELD_QUOTELINEID));
				}
			}
		}catch(Exception e) {
			throw new Exception("Error [202004232840] - " + e.getMessage());
		}
		
		return;
	}
	public ArrayList<String> getQuoteLineDetailIDs(){
		return arrQuoteLineDetailIDs;
	}
	public ArrayList<String> getQuoteLineIDs(){
		return arrQuoteLineIDs;
	}
	public ArrayList<String> getDescriptions(){
		return arrDescriptions;
	}
	public ArrayList<String> getValues(){
		return arrValues;
	}
	public ArrayList<BigDecimal> getSortOrders(){
		return arrSortOrders;
	}
}
