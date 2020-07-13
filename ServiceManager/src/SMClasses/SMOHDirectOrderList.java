package SMClasses;

import java.sql.Connection;
import java.util.ArrayList;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import SMDataDefinition.SMOHDirectFieldDefinitions;
import ServletUtilities.clsOEAuthFunctions;

/*
Sample full request URL to obtain a single Order for Order Number: 'SQAL000008-1'
https://mingle-ionapi.inforcloudsuite.com/OHDIRECT_TRN/CPQEQ/RuntimeApi/EnterpriseQuoting/Entities/C_DealerOrder?$filter=C_QuoteNumberString%20eq%20'SQAL000008-1'

Sample full request URL to obtain a list of orders modified after a selected date:
https://mingle-ionapi.inforcloudsuite.com/OHDIRECT_TRN/CPQEQ/RuntimeApi/EnterpriseQuoting/Entities/C_DealerOrder?$filter=C_LastModifiedDate%20gt%20'2020-01-09'

*/

public class SMOHDirectOrderList {

	ArrayList<String> arrOrderNumbers;
	ArrayList<String> arrOrderIDs;
	ArrayList<String> arrCreatedBys;
	ArrayList<String> arrCreatedDates;
	ArrayList<String> arrLastModifiedBys;
	ArrayList<String> arrLastModifiedDates;
	ArrayList<String> arrSalespersons;
	ArrayList<String> arrBillToNames;
	ArrayList<String> arrShipToNames;
	ArrayList<String> arrStatuses;
	
	public SMOHDirectOrderList() {
		
	}
	
	public void getOrderList(String sRequestString, Connection conn, String sDBID, String sUserID) throws Exception{
		arrOrderNumbers = new ArrayList<String>(0);
		arrOrderIDs = new ArrayList<String>(0);
		arrCreatedBys = new ArrayList<String>(0);
		arrCreatedDates = new ArrayList<String>(0);
		arrLastModifiedBys = new ArrayList<String>(0);
		arrLastModifiedDates = new ArrayList<String>(0);
		arrSalespersons = new ArrayList<String>(0);
		arrBillToNames = new ArrayList<String>(0);
		arrShipToNames = new ArrayList<String>(0);
		arrStatuses = new ArrayList<String>(0);
		//Try to read the list:
		String sJSONResult = "";
		try {
			sJSONResult = ServletUtilities.clsOEAuthFunctions.requestOHDirectData(conn, sRequestString, sDBID, sUserID);
		} catch (Exception e) {
			throw new Exception("Error [1593713962] - " + e.getMessage());
		}
		
		//System.out.println("[1593713963] - sJSONResult = '" + sJSONResult + "'.");
		
		//Try to parse the list:
		JSONParser parser = new JSONParser();
		try{
			Object obj = parser.parse(sJSONResult);
			JSONObject jo = (JSONObject) obj;
			JSONArray jarray = (JSONArray) jo.get("items");
			
			for (int i = 0; i < jarray.size(); i++) {
				JSONObject orderitem = (JSONObject)jarray.get(i);
				
				if (orderitem.get(SMOHDirectFieldDefinitions.ORDER_FIELD_ID) == null) {
					arrOrderIDs.add("");
				}else {
					arrOrderIDs.add((String)orderitem.get(SMOHDirectFieldDefinitions.ORDER_FIELD_ID));
				}
				if (orderitem.get(SMOHDirectFieldDefinitions.ORDER_FIELD_ORDERNUMBER) == null) {
					arrOrderNumbers.add("");
				}else {
					arrOrderNumbers.add((String)orderitem.get(SMOHDirectFieldDefinitions.ORDER_FIELD_ORDERNUMBER));
				}

				if (orderitem.get(SMOHDirectFieldDefinitions.ORDER_FIELD_CREATEDBY) == null) {
					arrCreatedBys.add("");
				}else {
					arrCreatedBys.add((String)orderitem.get(SMOHDirectFieldDefinitions.ORDER_FIELD_CREATEDBY));
				}
				if (orderitem.get(SMOHDirectFieldDefinitions.ORDER_FIELD_CREATEDDATE) == null) {
					arrCreatedDates.add("");
				}else {
					arrCreatedDates.add(clsOEAuthFunctions.convertOHDirectDateTimeToStd(
						(String)orderitem.get(SMOHDirectFieldDefinitions.ORDER_FIELD_CREATEDDATE)));
				}
				if (orderitem.get(SMOHDirectFieldDefinitions.ORDER_FIELD_LASTMODIFIEDBY) == null) {
					arrLastModifiedBys.add("");
				}else {
					arrLastModifiedBys.add((String)orderitem.get(SMOHDirectFieldDefinitions.ORDER_FIELD_LASTMODIFIEDBY));
				}
				if (orderitem.get(SMOHDirectFieldDefinitions.ORDER_FIELD_LASTMODIFIEDDATE) == null) {
					arrLastModifiedDates.add("");
				}else {
					arrLastModifiedDates.add(clsOEAuthFunctions.convertOHDirectDateTimeToStd(
						(String)orderitem.get(SMOHDirectFieldDefinitions.ORDER_FIELD_LASTMODIFIEDDATE)));
				}
				if (orderitem.get(SMOHDirectFieldDefinitions.ORDER_FIELD_SALESPERSON) == null) {
					arrSalespersons.add("");
				}else {
					arrSalespersons.add((String)orderitem.get(SMOHDirectFieldDefinitions.ORDER_FIELD_SALESPERSON));
				}
				if (orderitem.get(SMOHDirectFieldDefinitions.ORDER_FIELD_BILLTONAME) == null) {
					arrBillToNames.add("");
				}else {
					arrBillToNames.add((String)orderitem.get(SMOHDirectFieldDefinitions.ORDER_FIELD_BILLTONAME));
				}
				if (orderitem.get(SMOHDirectFieldDefinitions.ORDER_FIELD_SHIPTONAME) == null) {
					arrShipToNames.add("");
				}else {
					arrShipToNames.add((String)orderitem.get(SMOHDirectFieldDefinitions.ORDER_FIELD_SHIPTONAME));
				}
				if (orderitem.get(SMOHDirectFieldDefinitions.ORDER_FIELD_STATUS) == null) {
					arrStatuses.add("");
				}else {
					arrStatuses.add((String)orderitem.get(SMOHDirectFieldDefinitions.ORDER_FIELD_STATUS));
				}
				
			}
		}catch(Exception e) {
			throw new Exception("Error [1593713965] - " + e.getMessage());
		}
		
		return;
	}
	public ArrayList<String> getOrderIDs(){
		return arrOrderIDs;
	}
	public ArrayList<String> getOrderNumbers(){
		return arrOrderNumbers;
	}
	public ArrayList<String> getCreatedBys(){
		return arrCreatedBys;
	}
	public ArrayList<String> getCreatedDates(){
		return arrCreatedDates;
	}
	
	public ArrayList<String> getLastModifiedBys(){
		return arrLastModifiedBys;
	}
	public ArrayList<String> getLastModifiedDates(){
		return arrLastModifiedDates;
	}
	public ArrayList<String> getSalespersons(){
		return arrSalespersons;
	}
	public ArrayList<String> getBillToNames(){
		return arrBillToNames;
	}
	public ArrayList<String> getShipToNames(){
		return arrShipToNames;
	}
	public ArrayList<String> getStatuses(){
		return arrStatuses;
	}
}
