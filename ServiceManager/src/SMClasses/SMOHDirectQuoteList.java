package SMClasses;

import java.sql.Connection;
import java.util.ArrayList;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public class SMOHDirectQuoteList {

	ArrayList<String> arrNames;
	ArrayList<String> arrQuoteNumbers;
	
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
				if (quoteitem.get(ServletUtilities.clsOEAuthFunctions.QUOTE_FIELD_QUOTENUMBER) == null) {
					arrQuoteNumbers.add("");
				}else {
					arrQuoteNumbers.add((String)quoteitem.get(ServletUtilities.clsOEAuthFunctions.QUOTE_FIELD_QUOTENUMBER));
				}
				if (quoteitem.get(ServletUtilities.clsOEAuthFunctions.QUOTE_FIELD_NAME) == null) {
					arrNames.add("");
				}else {
					arrNames.add((String)quoteitem.get(ServletUtilities.clsOEAuthFunctions.QUOTE_FIELD_NAME));
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
}
