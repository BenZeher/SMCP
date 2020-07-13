package SMClasses;

import java.math.BigDecimal;
import java.sql.Connection;
import java.util.ArrayList;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import SMDataDefinition.SMOHDirectFieldDefinitions;

/*
 Sample full request URL to obtain the Order Line Cost Details on order line ID: ' '
https://mingle-ionapi.inforcloudsuite.com/OHDIRECT_TRN/CPQEQ/RuntimeApi/EnterpriseQuoting/Entities/C_DealerOrderLineCostDetail?%24filter=C_OrderLine%20eq%20'ea51bac1-b858-ea11-82f9-98456f859bd9'
 */

public class SMOHDirectOrderLineCostDetailList {

	ArrayList<String> arrOrderLineCostDetailIDs;
	ArrayList<String> arrOrderLineIDs;
	ArrayList<BigDecimal> arrQtys;
	ArrayList<String> arrDescriptions;
	ArrayList<BigDecimal> arrListPrices;
	ArrayList<String> arrDiscountMultipliers;
	ArrayList<BigDecimal> arrBasePrices;
	ArrayList<BigDecimal> arrOptionsPrices;
	BigDecimal bdTotalBasePrices = new BigDecimal("0.00");
	BigDecimal bdTotalOptionsPrice = new BigDecimal("0.00");
	
	public SMOHDirectOrderLineCostDetailList() {
		
	}
	
	public void getQuoteLineCostDetailList(String sRequestString, Connection conn, String sDBID, String sUserID) throws Exception{
		
		arrOrderLineCostDetailIDs = new ArrayList<String>(0);
		arrOrderLineIDs = new ArrayList<String>(0);
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
			throw new Exception("Error [1594643195] - " + e.getMessage());
		}
		
		//Try to parse the list:
		JSONParser parser = new JSONParser();
		try{
			Object obj = parser.parse(sJSONResult);
			JSONObject jo = (JSONObject) obj;
			JSONArray jarray = (JSONArray) jo.get(SMOHDirectFieldDefinitions.ORDERLINEDETAIL_ARRAY_NAME);
			
			for (int i = 0; i < jarray.size(); i++) {
				JSONObject objOrderLine = (JSONObject)jarray.get(i);
				
				if (objOrderLine.get(SMOHDirectFieldDefinitions.ORDERLINECOSTDETAIL_DESCRIPTION) == null) {
					arrDescriptions.add("");
				}else {
					arrDescriptions.add((String)objOrderLine.get(SMOHDirectFieldDefinitions.ORDERLINECOSTDETAIL_DESCRIPTION));
				}
				
				if (objOrderLine.get(SMOHDirectFieldDefinitions.ORDERLINECOSTDETAIL_ID) == null) {
					arrOrderLineCostDetailIDs.add("");
				}else {
					arrOrderLineCostDetailIDs.add((String)objOrderLine.get(SMOHDirectFieldDefinitions.ORDERLINECOSTDETAIL_ID));
				}
				
				if (objOrderLine.get(SMOHDirectFieldDefinitions.ORDERLINECOSTDETAIL_ORDER_LINE_ID) == null) {
					arrOrderLineIDs.add("");
				}else {
					arrOrderLineIDs.add((String)objOrderLine.get(SMOHDirectFieldDefinitions.ORDERLINECOSTDETAIL_ORDER_LINE_ID));
				}
				
				if (objOrderLine.get(SMOHDirectFieldDefinitions.ORDERLINECOSTDETAIL_QUANTITY) == null) {
					arrQtys.add(new BigDecimal("0.0000"));
				}else {
					arrQtys.add(BigDecimal.valueOf((Double)objOrderLine.get(SMOHDirectFieldDefinitions.ORDERLINECOSTDETAIL_QUANTITY)));
				}
				
				if (objOrderLine.get(SMOHDirectFieldDefinitions.ORDERLINECOSTDETAIL_LIST_PRICE) == null) {
					arrListPrices.add(new BigDecimal("0.0000"));
				}else {
					arrListPrices.add(BigDecimal.valueOf((Double)objOrderLine.get(SMOHDirectFieldDefinitions.ORDERLINECOSTDETAIL_LIST_PRICE)));
				}
				
				if (objOrderLine.get(SMOHDirectFieldDefinitions.ORDERLINECOSTDETAIL_DISCOUNT_MULTIPLIER) == null) {
					arrDiscountMultipliers.add("");
				}else {
					arrDiscountMultipliers.add((String)objOrderLine.get(SMOHDirectFieldDefinitions.ORDERLINECOSTDETAIL_DISCOUNT_MULTIPLIER));
				}
				
				if (objOrderLine.get(SMOHDirectFieldDefinitions.ORDERLINECOSTDETAIL_BASE_PRICE) == null) {
					arrBasePrices.add(new BigDecimal("0.0000"));
				}else {
					arrBasePrices.add(BigDecimal.valueOf((Double)objOrderLine.get(SMOHDirectFieldDefinitions.ORDERLINECOSTDETAIL_BASE_PRICE)));
				}
				
				if (objOrderLine.get(SMOHDirectFieldDefinitions.ORDERLINECOSTDETAIL_OPTION_PRICE) == null) {
					arrOptionsPrices.add(new BigDecimal("0.0000"));
				}else {
					arrOptionsPrices.add(BigDecimal.valueOf((Double)objOrderLine.get(SMOHDirectFieldDefinitions.ORDERLINECOSTDETAIL_OPTION_PRICE)));
				}
			}
		}catch(Exception e) {
			throw new Exception("Error [1594643196] - " + e.getMessage());
		}
		
		//Get the totals:
		for (int i = 0; i < arrOrderLineIDs.size(); i++) {
			bdTotalBasePrices = bdTotalBasePrices.add(arrBasePrices.get(i));
			bdTotalOptionsPrice = bdTotalOptionsPrice.add(arrOptionsPrices.get(i));
		}
		
		return;
	}
	public ArrayList<String> getOrderLineDetailIDs(){
		return arrOrderLineCostDetailIDs;
	}
	public ArrayList<String> getOrderLineIDs(){
		return arrOrderLineIDs;
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
