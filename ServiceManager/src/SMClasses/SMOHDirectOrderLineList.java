package SMClasses;

import java.math.BigDecimal;
import java.sql.Connection;
import java.util.ArrayList;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import SMDataDefinition.SMOHDirectFieldDefinitions;

/*
 Sample full request URL to obtain the Order Lines on order ID: ' '
https://mingle-ionapi.inforcloudsuite.com/OHDIRECT_TRN/CPQEQ/RuntimeApi/EnterpriseQuoting/Entities/C_DealerOrderLine?$filter=C_Order%20eq%20'be44565a-a758-ea11-82fa-81f82665d436'
 */

public class SMOHDirectOrderLineList {

	ArrayList<String> arrOrderLineIDs;
	ArrayList<String> arrOrderNumbers;
	ArrayList<BigDecimal> arrLineNumbers;
	ArrayList<String> arrDescriptions;
	ArrayList<String> arrLastConfigurationDescriptions;
	ArrayList<BigDecimal> arrQuantities;
	ArrayList<BigDecimal> arrUnitSellingPrices;
	ArrayList<BigDecimal> arrTotalSellingPrices;
	ArrayList<BigDecimal> arrUnitCosts;
	ArrayList<BigDecimal> arrExtendedCosts;
	ArrayList<String> arrLabels;
	
	public SMOHDirectOrderLineList() {
		
	}
	
	public void getOrderLineList(String sRequestString, Connection conn, String sDBID, String sUserID) throws Exception{
		
		arrOrderLineIDs = new ArrayList<String>(0);
		arrOrderNumbers = new ArrayList<String>(0);
		arrLineNumbers = new ArrayList<BigDecimal>(0);
		arrDescriptions = new ArrayList<String>(0);
		arrLastConfigurationDescriptions = new ArrayList<String>(0);
		arrQuantities = new ArrayList<BigDecimal>(0);
		arrUnitSellingPrices = new ArrayList<BigDecimal>(0);
		arrTotalSellingPrices = new ArrayList<BigDecimal>(0);
		arrUnitCosts = new ArrayList<BigDecimal>(0);
		arrExtendedCosts = new ArrayList<BigDecimal>(0);
		arrLabels = new ArrayList<String>(0);
		
		//Try to read the list:
		String sJSONResult = "";
		try {
			sJSONResult = ServletUtilities.clsOEAuthFunctions.requestOHDirectData(conn, sRequestString, sDBID, sUserID);
		} catch (Exception e) {
			throw new Exception("Error [1594641627] - " + e.getMessage());
		}
		
		//Try to parse the list:
		JSONParser parser = new JSONParser();
		try{
			Object obj = parser.parse(sJSONResult);
			JSONObject jo = (JSONObject) obj;
			JSONArray jarray = (JSONArray) jo.get(SMOHDirectFieldDefinitions.ORDERLINE_ARRAY_NAME);
			
			for (int i = 0; i < jarray.size(); i++) {
				JSONObject objOrderLine = (JSONObject)jarray.get(i);
				if (objOrderLine.get(SMOHDirectFieldDefinitions.ORDERLINE_FIELD_DESCRIPTION) == null) {
					arrDescriptions.add("");
				}else {
					arrDescriptions.add((String)objOrderLine.get(SMOHDirectFieldDefinitions.ORDERLINE_FIELD_DESCRIPTION));
				}
				if (objOrderLine.get(SMOHDirectFieldDefinitions.ORDERLINE_FIELD_ID) == null) {
					arrOrderLineIDs.add("");
				}else {
					arrOrderLineIDs.add((String)objOrderLine.get(SMOHDirectFieldDefinitions.ORDERLINE_FIELD_ID));
				}
				if (objOrderLine.get(SMOHDirectFieldDefinitions.ORDERLINE_FIELD_LABEL) == null) {
					arrLabels.add("");
				}else {
					arrLabels.add((String)objOrderLine.get(SMOHDirectFieldDefinitions.ORDERLINE_FIELD_LABEL));
				}
				if (objOrderLine.get(SMOHDirectFieldDefinitions.ORDERLINE_FIELD_LASTCONFIGURATIONDESCRIPTION) == null) {
					arrLastConfigurationDescriptions.add("");
				}else {
					arrLastConfigurationDescriptions.add((String)objOrderLine.get(SMOHDirectFieldDefinitions.ORDERLINE_FIELD_LASTCONFIGURATIONDESCRIPTION));
				}
				if (objOrderLine.get(SMOHDirectFieldDefinitions.ORDERLINE_FIELD_LINENUMBER) == null) {
					arrLineNumbers.add(new BigDecimal("0.00"));
				}else {
					arrLineNumbers.add(new BigDecimal((Double)objOrderLine.get(SMOHDirectFieldDefinitions.ORDERLINE_FIELD_LINENUMBER)));
				}
				if (objOrderLine.get(SMOHDirectFieldDefinitions.ORDERLINE_FIELD_QUANTITY) == null) {
					arrQuantities.add(new BigDecimal("0.0000"));
				}else {
					arrQuantities.add(BigDecimal.valueOf((Double)objOrderLine.get(SMOHDirectFieldDefinitions.ORDERLINE_FIELD_QUANTITY)));
				}
				if (objOrderLine.get(SMOHDirectFieldDefinitions.ORDERLINE_FIELD_ORDER) == null) {
					arrOrderNumbers.add("");
				}else {
					arrOrderNumbers.add((String)objOrderLine.get(SMOHDirectFieldDefinitions.ORDERLINE_FIELD_ORDER));
				}
				if (objOrderLine.get(SMOHDirectFieldDefinitions.ORDERLINE_FIELD_EXTENDEDCOST) == null) {
					arrExtendedCosts.add(new BigDecimal("0.00"));
				}else {
					arrExtendedCosts.add(BigDecimal.valueOf((Double)objOrderLine.get(SMOHDirectFieldDefinitions.ORDERLINE_FIELD_EXTENDEDCOST)));
				}
				if (objOrderLine.get(SMOHDirectFieldDefinitions.ORDERLINE_FIELD_UNITCOST) == null) {
					arrUnitCosts.add(new BigDecimal("0.00"));
				}else {
					arrUnitCosts.add(BigDecimal.valueOf((Double)objOrderLine.get(SMOHDirectFieldDefinitions.ORDERLINE_FIELD_UNITCOST)));
				}
				if (objOrderLine.get(SMOHDirectFieldDefinitions.ORDERLINE_FIELD_SELLINGPRICE) == null) {
					arrUnitSellingPrices.add(new BigDecimal("0.00"));
				}else {
					arrUnitSellingPrices.add(BigDecimal.valueOf((Double)objOrderLine.get(SMOHDirectFieldDefinitions.ORDERLINE_FIELD_SELLINGPRICE)));
				}
				if (objOrderLine.get(SMOHDirectFieldDefinitions.ORDERLINE_FIELD_TOTALSELLINGPRICE) == null) {
					arrTotalSellingPrices.add(new BigDecimal("0.00"));
				}else {
					arrTotalSellingPrices.add(BigDecimal.valueOf((Double)objOrderLine.get(SMOHDirectFieldDefinitions.ORDERLINE_FIELD_TOTALSELLINGPRICE)));
				}
	
			}
		}catch(Exception e) {
			throw new Exception("Error [1594641628] - " + e.getMessage());
		}
		
		return;
	}
	public ArrayList<String> getOrderLineIDs(){
		return arrOrderLineIDs;
	}
	public ArrayList<String> getOrderNumbers(){
		return arrOrderNumbers;
	}
	public ArrayList<BigDecimal> getLineNumbers(){
		return arrLineNumbers;
	}
	public ArrayList<String> getDescriptions(){
		return arrDescriptions;
	}
	public ArrayList<String> getLastConfigurationDescriptions(){
		return arrLastConfigurationDescriptions;
	}
	public ArrayList<BigDecimal> getQuantities(){
		return arrQuantities;
	}
	public ArrayList<BigDecimal> getUnitSellingPrices(){
		return arrUnitSellingPrices;
	}
	public ArrayList<BigDecimal> getTotalSellingPrices(){
		return arrTotalSellingPrices;
	}
	public ArrayList<BigDecimal> getUnitCosts(){
		return arrUnitCosts;
	}
	public ArrayList<BigDecimal> getExtendedCosts(){
		return arrExtendedCosts;
	}
	public ArrayList<String> getLabels(){
		return arrLabels;
	}
}
