package SMClasses;

import java.math.BigDecimal;
import java.sql.Connection;
import java.util.ArrayList;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import SMDataDefinition.SMOHDirectFieldDefinitions;

/*
 Sample full request URL to obtain the Quote Line Cost Details on quote line ID: 'ea51bac1-b858-ea11-82f9-98456f859bd9'
https://mingle-ionapi.inforcloudsuite.com/OHDIRECT_TRN/CPQEQ/RuntimeApi/EnterpriseQuoting/Entities/C_DealerQuoteLineCostDetail?%24filter=C_QuoteLine%20eq%20'ea51bac1-b858-ea11-82f9-98456f859bd9'
 */

public class SMOHDirectQuoteLineCostDetailList {

	ArrayList<String> arrQuoteLineCostDetailIDs;
	ArrayList<String> arrQuoteLineIDs;
	ArrayList<BigDecimal> arrQtys;
	ArrayList<String> arrDescriptions;
	ArrayList<BigDecimal> arrListPrices;
	ArrayList<String> arrDiscountMultipliers;
	ArrayList<BigDecimal> arrBasePrices;
	ArrayList<BigDecimal> arrOptionsPrices;
	BigDecimal bdTotalBasePrices = new BigDecimal("0.00");
	BigDecimal bdTotalOptionsPrice = new BigDecimal("0.00");
	
	public SMOHDirectQuoteLineCostDetailList() {
		
	}
	
	public void getQuoteLineCostDetailList(String sRequestString, Connection conn, String sDBID, String sUserID) throws Exception{
		
		arrQuoteLineCostDetailIDs = new ArrayList<String>(0);
		arrQuoteLineIDs = new ArrayList<String>(0);
		arrQtys = new ArrayList<BigDecimal>(0);
		arrDescriptions = new ArrayList<String>(0);
		arrListPrices = new ArrayList<BigDecimal>(0);
		arrDiscountMultipliers = new ArrayList<String>(0);
		arrBasePrices = new ArrayList<BigDecimal>(0);
		arrOptionsPrices = new ArrayList<BigDecimal>(0);
		
		//Try to read the list:
		String sJSONResult = "";
		try {
			sJSONResult = ServletUtilities.clsOEAuthFunctions.requestOHDirectData(conn, sRequestString, sDBID, sUserID);
		} catch (Exception e) {
			throw new Exception("Error [202004231317] - " + e.getMessage());
		}
		
		//Try to parse the list:
		JSONParser parser = new JSONParser();
		try{
			Object obj = parser.parse(sJSONResult);
			JSONObject jo = (JSONObject) obj;
			JSONArray jarray = (JSONArray) jo.get(SMOHDirectFieldDefinitions.QUOTELINEDETAIL_ARRAY_NAME);
			
			for (int i = 0; i < jarray.size(); i++) {
				JSONObject objQuoteLine = (JSONObject)jarray.get(i);
				
				if (objQuoteLine.get(SMOHDirectFieldDefinitions.QUOTELINECOSTDETAIL_DESCRIPTION) == null) {
					arrDescriptions.add("");
				}else {
					arrDescriptions.add((String)objQuoteLine.get(SMOHDirectFieldDefinitions.QUOTELINECOSTDETAIL_DESCRIPTION));
				}
				
				if (objQuoteLine.get(SMOHDirectFieldDefinitions.QUOTELINECOSTDETAIL_ID) == null) {
					arrQuoteLineCostDetailIDs.add("");
				}else {
					arrQuoteLineCostDetailIDs.add((String)objQuoteLine.get(SMOHDirectFieldDefinitions.QUOTELINECOSTDETAIL_ID));
				}
				
				if (objQuoteLine.get(SMOHDirectFieldDefinitions.QUOTELINECOSTDETAIL_QUOTE_LINE_ID) == null) {
					arrQuoteLineIDs.add("");
				}else {
					arrQuoteLineIDs.add((String)objQuoteLine.get(SMOHDirectFieldDefinitions.QUOTELINECOSTDETAIL_QUOTE_LINE_ID));
				}
				
				if (objQuoteLine.get(SMOHDirectFieldDefinitions.QUOTELINECOSTDETAIL_QUANTITY) == null) {
					arrQtys.add(new BigDecimal("0.0000"));
				}else {
					arrQtys.add(BigDecimal.valueOf((Double)objQuoteLine.get(SMOHDirectFieldDefinitions.QUOTELINECOSTDETAIL_QUANTITY)));
				}
				
				if (objQuoteLine.get(SMOHDirectFieldDefinitions.QUOTELINECOSTDETAIL_LIST_PRICE) == null) {
					arrListPrices.add(new BigDecimal("0.0000"));
				}else {
					arrListPrices.add(BigDecimal.valueOf((Double)objQuoteLine.get(SMOHDirectFieldDefinitions.QUOTELINECOSTDETAIL_LIST_PRICE)));
				}
				
				if (objQuoteLine.get(SMOHDirectFieldDefinitions.QUOTELINECOSTDETAIL_DISCOUNT_MULTIPLIER) == null) {
					arrDiscountMultipliers.add("");
				}else {
					arrDiscountMultipliers.add((String)objQuoteLine.get(SMOHDirectFieldDefinitions.QUOTELINECOSTDETAIL_DISCOUNT_MULTIPLIER));
				}
				
				if (objQuoteLine.get(SMOHDirectFieldDefinitions.QUOTELINECOSTDETAIL_BASE_PRICE) == null) {
					arrBasePrices.add(new BigDecimal("0.0000"));
				}else {
					arrBasePrices.add(BigDecimal.valueOf((Double)objQuoteLine.get(SMOHDirectFieldDefinitions.QUOTELINECOSTDETAIL_BASE_PRICE)));
				}
				
				if (objQuoteLine.get(SMOHDirectFieldDefinitions.QUOTELINECOSTDETAIL_OPTION_PRICE) == null) {
					arrOptionsPrices.add(new BigDecimal("0.0000"));
				}else {
					arrOptionsPrices.add(BigDecimal.valueOf((Double)objQuoteLine.get(SMOHDirectFieldDefinitions.QUOTELINECOSTDETAIL_OPTION_PRICE)));
				}
			}
		}catch(Exception e) {
			throw new Exception("Error [202004232240] - " + e.getMessage());
		}
		
		//Get the totals:
		for (int i = 0; i < arrQuoteLineIDs.size(); i++) {
			bdTotalBasePrices = bdTotalBasePrices.add(arrBasePrices.get(i));
			bdTotalOptionsPrice = bdTotalOptionsPrice.add(arrOptionsPrices.get(i));
		}
		
		return;
	}
	public ArrayList<String> getQuoteLineDetailIDs(){
		return arrQuoteLineCostDetailIDs;
	}
	public ArrayList<String> getQuoteLineIDs(){
		return arrQuoteLineIDs;
	}
	public ArrayList<String> getDescriptions(){
		return arrDescriptions;
	}
	
	public ArrayList<BigDecimal> getQtys(){
		return arrQtys;
	}
	
	public ArrayList<BigDecimal> getListPrices(){
		return arrListPrices;
	}
	
	public ArrayList<String> getDiscountMultipliers(){
		return arrDiscountMultipliers;
	}
	
	public ArrayList<BigDecimal> getBasePrices(){
		return arrBasePrices;
	}
	
	public ArrayList<BigDecimal> getOptionPrices(){
		return arrOptionsPrices;
	}
	public BigDecimal getTotalBasePrice() {
		return bdTotalBasePrices;
	}
	public BigDecimal getTotalOptionsPrice() {
		return bdTotalOptionsPrice;
	}
	
}
