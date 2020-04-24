package SMClasses;

import java.sql.Connection;
import java.util.ArrayList;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import SMDataDefinition.SMOHDirectFieldDefinitions;

/*
Sample full request URL to obtain a single Quote for Quote Number: 'SQAL000008-1'
https://mingle-ionapi.inforcloudsuite.com/OHDIRECT_TRN/CPQEQ/RuntimeApi/EnterpriseQuoting/Entities/C_DealerQuote?%24filter=C_QuoteNumberString%20eq%20'SQAL000008-1'

Sample full request URL to obtain a list of quotes modified after a selected date:
https://mingle-ionapi.inforcloudsuite.com/OHDIRECT_TRN/CPQEQ/RuntimeApi/EnterpriseQuoting/Entities/C_DealerQuote?%24filter=C_LastModifiedDate%20gt%20'2020-01-09'

*/

public class SMOHDirectQuoteList {

	ArrayList<String> arrNames;
	ArrayList<String> arrQuoteNumbers;
	ArrayList<String> arrCreatedBys;
	ArrayList<String> arrCreatedDates;
	ArrayList<String> arrLastModifiedBys;
	ArrayList<String> arrLastModifiedDates;
	ArrayList<String> arrSalespersons;
	ArrayList<String> arrBillToNames;
	ArrayList<String> arrShipToNames;
	ArrayList<String> arrStatuses;
	
	public SMOHDirectQuoteList() {
		
	}
	
	public void getQuoteList(String sRequestString, Connection conn) throws Exception{
		arrQuoteNumbers = new ArrayList<String>(0);
		arrNames = new ArrayList<String>(0);
		
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
			JSONArray jarray = (JSONArray) jo.get("items");
			
			for (int i = 0; i < jarray.size(); i++) {
				JSONObject quoteitem = (JSONObject)jarray.get(i);
				if (quoteitem.get(SMOHDirectFieldDefinitions.QUOTE_FIELD_QUOTENUMBER) == null) {
					arrQuoteNumbers.add("");
				}else {
					arrQuoteNumbers.add((String)quoteitem.get(SMOHDirectFieldDefinitions.QUOTE_FIELD_QUOTENUMBER));
				}
				if (quoteitem.get(SMOHDirectFieldDefinitions.QUOTE_FIELD_NAME) == null) {
					arrNames.add("");
				}else {
					arrNames.add((String)quoteitem.get(SMOHDirectFieldDefinitions.QUOTE_FIELD_NAME));
				}
				if (quoteitem.get(SMOHDirectFieldDefinitions.QUOTE_FIELD_CREATEDBY) == null) {
					arrCreatedBys.add("");
				}else {
					arrCreatedBys.add((String)quoteitem.get(SMOHDirectFieldDefinitions.QUOTE_FIELD_CREATEDBY));
				}
				if (quoteitem.get(SMOHDirectFieldDefinitions.QUOTE_FIELD_CREATEDDATE) == null) {
					arrCreatedDates.add("");
				}else {
					arrCreatedDates.add((String)quoteitem.get(SMOHDirectFieldDefinitions.QUOTE_FIELD_CREATEDDATE));
				}
				if (quoteitem.get(SMOHDirectFieldDefinitions.QUOTE_FIELD_LASTMODIFIEDBY) == null) {
					arrLastModifiedBys.add("");
				}else {
					arrLastModifiedBys.add((String)quoteitem.get(SMOHDirectFieldDefinitions.QUOTE_FIELD_LASTMODIFIEDBY));
				}
				if (quoteitem.get(SMOHDirectFieldDefinitions.QUOTE_FIELD_LASTMODIFIEDDATE) == null) {
					arrLastModifiedDates.add("");
				}else {
					arrLastModifiedDates.add((String)quoteitem.get(SMOHDirectFieldDefinitions.QUOTE_FIELD_LASTMODIFIEDDATE));
				}
				if (quoteitem.get(SMOHDirectFieldDefinitions.QUOTE_FIELD_SALESPERSON) == null) {
					arrSalespersons.add("");
				}else {
					arrSalespersons.add((String)quoteitem.get(SMOHDirectFieldDefinitions.QUOTE_FIELD_SALESPERSON));
				}
				if (quoteitem.get(SMOHDirectFieldDefinitions.QUOTE_FIELD_BILLTONAME) == null) {
					arrBillToNames.add("");
				}else {
					arrBillToNames.add((String)quoteitem.get(SMOHDirectFieldDefinitions.QUOTE_FIELD_BILLTONAME));
				}
				if (quoteitem.get(SMOHDirectFieldDefinitions.QUOTE_FIELD_SHIPTONAME) == null) {
					arrShipToNames.add("");
				}else {
					arrShipToNames.add((String)quoteitem.get(SMOHDirectFieldDefinitions.QUOTE_FIELD_SHIPTONAME));
				}
				if (quoteitem.get(SMOHDirectFieldDefinitions.QUOTE_FIELD_STATUS) == null) {
					arrStatuses.add("");
				}else {
					arrStatuses.add((String)quoteitem.get(SMOHDirectFieldDefinitions.QUOTE_FIELD_STATUS));
				}
				
			}
		}catch(Exception e) {
			throw new Exception("Error [202004232740] - " + e.getMessage());
		}
		
		return;
	}
	public ArrayList<String> getQuoteNames(){
		return arrNames;
	}
	public ArrayList<String> getQuoteNumbers(){
		return arrQuoteNumbers;
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
