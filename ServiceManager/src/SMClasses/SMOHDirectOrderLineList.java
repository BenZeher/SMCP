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
https://mingle-ionapi.inforcloudsuite.com/OHDIRECT_TRN/CPQEQ/RuntimeApi/EnterpriseQuoting/Entities/C_DealerOrderLine?$filter=C_Order%20eq%20'00bac513-b658-ea11-82fa-d2da283a32ca'
 */

public class SMOHDirectOrderLineList {

	ArrayList<String> arrOrderLineIDs;
	ArrayList<String> arrOrderNumbers;
	ArrayList<BigDecimal> arrLineNumbers;
	ArrayList<String> arrDescriptions;
	ArrayList<String> arrLastConfigurationDescriptions;
	ArrayList<BigDecimal> arrQuantities;
	ArrayList<BigDecimal> arrUnitCosts;
	ArrayList<BigDecimal> arrTotalCosts;
	ArrayList<String> arrLabels;
	
	public SMOHDirectOrderLineList() {
		
	}
	
	public void getOrderLineList(String sRequestString, Connection conn, String sDBID, String sUserID) throws Exception{
		arrOrderNumbers = new ArrayList<String>(0);
		arrOrderLineIDs = new ArrayList<String>(0);
		arrLineNumbers = new ArrayList<BigDecimal>(0);
		arrDescriptions = new ArrayList<String>(0);
		arrLastConfigurationDescriptions = new ArrayList<String>(0);
		arrQuantities = new ArrayList<BigDecimal>(0);
		arrUnitCosts = new ArrayList<BigDecimal>(0);
		arrTotalCosts = new ArrayList<BigDecimal>(0);
		arrLabels = new ArrayList<String>(0);
		
		//Try to read the list:
		String sJSONResult = "";
		try {
			sJSONResult = ServletUtilities.clsOEAuthFunctions.requestOHDirectData(conn, sRequestString, sDBID, sUserID);
		} catch (Exception e) {
			throw new Exception("Error [202004232617] - " + e.getMessage());
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
				if (objOrderLine.get(SMOHDirectFieldDefinitions.ORDERLINE_FIELD_TOTALCOST) == null) {
					arrTotalCosts.add(new BigDecimal("0.00"));
				}else {
					arrTotalCosts.add(BigDecimal.valueOf((Double)objOrderLine.get(SMOHDirectFieldDefinitions.ORDERLINE_FIELD_TOTALCOST)));
				}
				if (objOrderLine.get(SMOHDirectFieldDefinitions.ORDERLINE_FIELD_UNITCOST) == null) {
					arrUnitCosts.add(new BigDecimal("0.00"));
				}else {
					arrUnitCosts.add(BigDecimal.valueOf((Double)objOrderLine.get(SMOHDirectFieldDefinitions.ORDERLINE_FIELD_UNITCOST)));
				}
			}
		}catch(Exception e) {
			throw new Exception("Error [202004233740] - " + e.getMessage());
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
	public ArrayList<BigDecimal> getUnitCosts(){
		return arrUnitCosts;
	}
	public ArrayList<BigDecimal> getTotalCosts(){
		return arrTotalCosts;
	}
	public ArrayList<String> getLabels(){
		return arrLabels;
	}
}
