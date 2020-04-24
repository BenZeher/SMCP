package SMClasses;

import java.math.BigDecimal;
import java.sql.Connection;
import java.util.ArrayList;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import SMDataDefinition.SMOHDirectFieldDefinitions;

/*
 Sample full request URL to obtain the Quote Lines on quote ID: '00bac513-b658-ea11-82fa-d2da283a32ca'
https://mingle-ionapi.inforcloudsuite.com/OHDIRECT_TRN/CPQEQ/RuntimeApi/EnterpriseQuoting/Entities/C_DealerQuoteLine?%24filter=C_Quote%20eq%20'00bac513-b658-ea11-82fa-d2da283a32ca'
 */

public class SMOHDirectQuoteLineList {

	ArrayList<String> arrQuoteLineIDs;
	ArrayList<String> arrQuoteNumbers;
	ArrayList<BigDecimal> arrLineNumbers;
	ArrayList<String> arrDescriptions;
	ArrayList<String> arrLastConfigurationDescriptions;
	ArrayList<BigDecimal> arrQuantities;
	ArrayList<BigDecimal> arrUnitCosts;
	ArrayList<BigDecimal> arrSellingPrices;
	ArrayList<BigDecimal> arrTotalCosts;
	ArrayList<BigDecimal> arrTotalSellingPrices;
	ArrayList<String> arrLabels;
	
	public SMOHDirectQuoteLineList() {
		
	}
	
	public void getQuoteLineList(String sRequestString, Connection conn) throws Exception{
		arrQuoteNumbers = new ArrayList<String>(0);
		arrQuoteLineIDs = new ArrayList<String>(0);
		arrLineNumbers = new ArrayList<BigDecimal>(0);
		arrDescriptions = new ArrayList<String>(0);
		arrLastConfigurationDescriptions = new ArrayList<String>(0);
		arrQuantities = new ArrayList<BigDecimal>(0);
		arrUnitCosts = new ArrayList<BigDecimal>(0);
		arrSellingPrices = new ArrayList<BigDecimal>(0);
		arrTotalCosts = new ArrayList<BigDecimal>(0);
		arrTotalSellingPrices = new ArrayList<BigDecimal>(0);
		arrLabels = new ArrayList<String>(0);
		
		//Try to read the list:
		String sJSONResult = "";
		try {
			sJSONResult = ServletUtilities.clsOEAuthFunctions.requestOHDirectData(conn, sRequestString);
		} catch (Exception e) {
			throw new Exception("Error [202004231617] - " + e.getMessage());
		}
		
		//Try to parse the list:
		JSONParser parser = new JSONParser();
		try{
			Object obj = parser.parse(sJSONResult);
			JSONObject jo = (JSONObject) obj;
			JSONArray jarray = (JSONArray) jo.get(SMOHDirectFieldDefinitions.QUOTELINE_ARRAY_NAME);
			
			for (int i = 0; i < jarray.size(); i++) {
				JSONObject objQuoteLine = (JSONObject)jarray.get(i);
				if (objQuoteLine.get(SMOHDirectFieldDefinitions.QUOTELINE_FIELD_DESCRIPTION) == null) {
					arrDescriptions.add("");
				}else {
					arrDescriptions.add((String)objQuoteLine.get(SMOHDirectFieldDefinitions.QUOTELINE_FIELD_DESCRIPTION));
				}
				if (objQuoteLine.get(SMOHDirectFieldDefinitions.QUOTELINE_FIELD_ID) == null) {
					arrQuoteLineIDs.add("");
				}else {
					arrQuoteLineIDs.add((String)objQuoteLine.get(SMOHDirectFieldDefinitions.QUOTELINE_FIELD_ID));
				}
				if (objQuoteLine.get(SMOHDirectFieldDefinitions.QUOTELINE_FIELD_LABEL) == null) {
					arrLabels.add("");
				}else {
					arrLabels.add((String)objQuoteLine.get(SMOHDirectFieldDefinitions.QUOTELINE_FIELD_LABEL));
				}
				if (objQuoteLine.get(SMOHDirectFieldDefinitions.QUOTELINE_FIELD_LASTCONFIGURATIONDESCRIPTION) == null) {
					arrLastConfigurationDescriptions.add("");
				}else {
					arrLastConfigurationDescriptions.add((String)objQuoteLine.get(SMOHDirectFieldDefinitions.QUOTELINE_FIELD_LASTCONFIGURATIONDESCRIPTION));
				}
				if (objQuoteLine.get(SMOHDirectFieldDefinitions.QUOTELINE_FIELD_LINENUMBER) == null) {
					arrLineNumbers.add(new BigDecimal("0.00"));
				}else {
					arrLineNumbers.add(new BigDecimal((Double)objQuoteLine.get(SMOHDirectFieldDefinitions.QUOTELINE_FIELD_LINENUMBER)));
				}
				if (objQuoteLine.get(SMOHDirectFieldDefinitions.QUOTELINE_FIELD_QUANTITY) == null) {
					arrQuantities.add(new BigDecimal("0.0000"));
				}else {
					arrQuantities.add(BigDecimal.valueOf((Double)objQuoteLine.get(SMOHDirectFieldDefinitions.QUOTELINE_FIELD_QUANTITY)));
				}
				if (objQuoteLine.get(SMOHDirectFieldDefinitions.QUOTELINE_FIELD_QUOTENUMBER) == null) {
					arrQuoteNumbers.add("");
				}else {
					arrQuoteNumbers.add((String)objQuoteLine.get(SMOHDirectFieldDefinitions.QUOTELINE_FIELD_QUOTENUMBER));
				}
				if (objQuoteLine.get(SMOHDirectFieldDefinitions.QUOTELINE_FIELD_SELLINGPRICE) == null) {
					arrSellingPrices.add(new BigDecimal("0.00"));
				}else {
					arrSellingPrices.add(BigDecimal.valueOf((Double)objQuoteLine.get(SMOHDirectFieldDefinitions.QUOTELINE_FIELD_SELLINGPRICE)));
				}
				if (objQuoteLine.get(SMOHDirectFieldDefinitions.QUOTELINE_FIELD_TOTALCOST) == null) {
					arrTotalCosts.add(new BigDecimal("0.00"));
				}else {
					arrTotalCosts.add(BigDecimal.valueOf((Double)objQuoteLine.get(SMOHDirectFieldDefinitions.QUOTELINE_FIELD_TOTALCOST)));
				}
				if (objQuoteLine.get(SMOHDirectFieldDefinitions.QUOTELINE_FIELD_TOTALSELLINGPRICE) == null) {
					arrTotalSellingPrices.add(new BigDecimal("0.00"));
				}else {
					arrTotalSellingPrices.add(BigDecimal.valueOf((Double)objQuoteLine.get(SMOHDirectFieldDefinitions.QUOTELINE_FIELD_TOTALSELLINGPRICE)));
				}
				if (objQuoteLine.get(SMOHDirectFieldDefinitions.QUOTELINE_FIELD_UNITCOST) == null) {
					arrUnitCosts.add(new BigDecimal("0.00"));
				}else {
					arrUnitCosts.add(BigDecimal.valueOf((Double)objQuoteLine.get(SMOHDirectFieldDefinitions.QUOTELINE_FIELD_UNITCOST)));
				}
			}
		}catch(Exception e) {
			throw new Exception("Error [202004232740] - " + e.getMessage());
		}
		
		return;
	}
	public ArrayList<String> getQuoteLineIDs(){
		return arrQuoteLineIDs;
	}
	public ArrayList<String> getQuoteNumbers(){
		return arrQuoteNumbers;
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
	public ArrayList<BigDecimal> getUnitCosts(){
		return arrUnitCosts;
	}
	public ArrayList<BigDecimal> getSellingPrices(){
		return arrSellingPrices;
	}
	public ArrayList<BigDecimal> getTotalCosts(){
		return arrTotalCosts;
	}
	public ArrayList<BigDecimal> getTotalSellingPrices(){
		return arrTotalSellingPrices;
	}
	public ArrayList<String> getLabels(){
		return arrLabels;
	}
}
