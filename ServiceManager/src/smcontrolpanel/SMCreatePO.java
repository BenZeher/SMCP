package smcontrolpanel;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.util.ArrayList;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import smic.ICItem;
import SMDataDefinition.SMTableicvendors;
import SMDataDefinition.SMTableorderheaders;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsDateAndTimeConversions;
import ServletUtilities.clsManageRequestParameters;
import ServletUtilities.clsStringFunctions;

public class SMCreatePO extends java.lang.Object{

	public static final String  CREATE_PO_OBJECT_NAME = "CREATEPOOBJECT";
	private static String EMPTY_DATE_STRING = "00/00/0000";
	
	private String m_sVendorCode;
	private String m_sVendorName;
	private String m_sOrderNumber;
	private String m_sDetailNumberListString;
	private String m_sExpectedShipDate;
	private ArrayList<String>arrItemNumbers = new ArrayList<String>(0);
	private ArrayList<String>arrItemDescriptions = new ArrayList<String>(0);
	private ArrayList<String>arrUnitCosts = new ArrayList<String>(0);
	private ArrayList<String>arrUnitOfMeasures = new ArrayList<String>(0);
	private ArrayList<String>arrQuantities = new ArrayList<String>(0);
	
	public SMCreatePO(
        ) {
		m_sVendorCode = "";
		m_sVendorName = "";
		m_sOrderNumber = "";
		m_sDetailNumberListString = "";
		m_sExpectedShipDate = EMPTY_DATE_STRING;
		arrItemNumbers.clear();
		arrItemDescriptions.clear();
		arrUnitCosts.clear();
		arrUnitOfMeasures.clear();
		arrQuantities.clear();
    }
	public SMCreatePO(HttpServletRequest req
	        ) {
			m_sVendorCode = clsManageRequestParameters.get_Request_Parameter(SMCreatePOFromOrderEdit.PARAM_VENDORCODE, req);
			m_sVendorName = clsManageRequestParameters.get_Request_Parameter(SMCreatePOFromOrderEdit.PARAM_VENDORNAME, req);
			m_sOrderNumber = clsManageRequestParameters.get_Request_Parameter(SMCreatePOFromOrderEdit.PARAM_ORDERNUMBER, req);
			m_sDetailNumberListString = clsManageRequestParameters.get_Request_Parameter(SMCreatePOFromOrderEdit.PARAM_DETAILNUMBERSTRING, req);
			m_sExpectedShipDate = clsManageRequestParameters.get_Request_Parameter(SMCreatePOFromOrderEdit.PARAM_EXPECTEDSHIPDATE, req);
			int iCounter = 0;
			while (clsManageRequestParameters.get_Request_Parameter(SMCreatePOFromOrderEdit.PARAM_ITEMNUMBERSTUB 
					+ clsStringFunctions.PadLeft(Integer.toString(iCounter), "0", 4), req).compareToIgnoreCase("") !=0){
				arrItemNumbers.add(clsManageRequestParameters.get_Request_Parameter(SMCreatePOFromOrderEdit.PARAM_ITEMNUMBERSTUB 
					+ clsStringFunctions.PadLeft(Integer.toString(iCounter), "0", 4), req));
				arrItemDescriptions.add(clsManageRequestParameters.get_Request_Parameter(SMCreatePOFromOrderEdit.PARAM_ITEMDESCSTUB 
					+ clsStringFunctions.PadLeft(Integer.toString(iCounter), "0", 4), req));
				arrUnitCosts.add(clsManageRequestParameters.get_Request_Parameter(SMCreatePOFromOrderEdit.PARAM_UNITCOSTSTUB
					+ clsStringFunctions.PadLeft(Integer.toString(iCounter), "0", 4), req));
				arrUnitOfMeasures.add(clsManageRequestParameters.get_Request_Parameter(SMCreatePOFromOrderEdit.PARAM_UNITOFMEASURESTUB
					+ clsStringFunctions.PadLeft(Integer.toString(iCounter), "0", 4), req));
				arrQuantities.add(clsManageRequestParameters.get_Request_Parameter(SMCreatePOFromOrderEdit.PARAM_QUANTITYSTUB
						+ clsStringFunctions.PadLeft(Integer.toString(iCounter), "0", 4), req));
				iCounter++;
			}
	    }

    public String getVendorCode(){
    	return m_sVendorCode;
    }
    public void setVendorCode(String sVendorCode){
    	m_sVendorCode = sVendorCode;
    }
    public String getVendorName(){
    	return m_sVendorName;
    }
    public void setVendorName(String sVendorName){
    	m_sVendorName = sVendorName;
    }
    public String getExpectedShipDate(){
    	return m_sExpectedShipDate;
    }
    public void setExpectedShipDate(String sExpectedShipDate){
    	m_sExpectedShipDate = sExpectedShipDate;
    }
    public String getOrderNumber(){
    	return m_sOrderNumber;
    }
    public void setOrderNumber(String sOrderNumber){
    	m_sOrderNumber = sOrderNumber;
    }
    public String getDetailNumberListString(){
    	return m_sDetailNumberListString;
    }
    public void setDetailNumberListString(String sDetailNumberListString){
    	m_sDetailNumberListString = sDetailNumberListString;
    }
    public String getItemNumber(int iIndex){
    	if (iIndex > arrItemNumbers.size()){
    		return "";
    	}else{
    		return arrItemNumbers.get(iIndex);
    	}
    }
    public void addItemNumber(String sItemNumber){
    	arrItemNumbers.add(sItemNumber);
    }
    public String getItemDescription(int iIndex){
    	if (iIndex > arrItemDescriptions.size()){
    		return "";
    	}else{
    		return arrItemDescriptions.get(iIndex);
    	}
    }
    public void addItemDescription(String sItemDescription){
    	arrItemDescriptions.add(sItemDescription);
    }
    public String getUnitCost(int iIndex){
    	if (iIndex > arrUnitCosts.size()){
    		return "";
    	}else{
    		return arrUnitCosts.get(iIndex);
    	}
    }
    public void addUnitCost(String sUnitCost){
    	arrUnitCosts.add(sUnitCost);
    }
    public String getUnitOfMeasure(int iIndex){
    	if (iIndex > arrUnitOfMeasures.size()){
    		return "";
    	}else{
    		return arrUnitOfMeasures.get(iIndex);
    	}
    }
    public void addUnitOfMeasure(String sUnitOfMeasure){
    	arrUnitOfMeasures.add(sUnitOfMeasure);
    }
    public String getQuantity(int iIndex){
    	if (iIndex > arrQuantities.size()){
    		return "";
    	}else{
    		return arrQuantities.get(iIndex);
    	}
    }
    public void addQuantity(String sQuantity){
    	arrQuantities.add(sQuantity);
    }
    public int getNumberOfLines(){
    	return arrItemNumbers.size();
    }
    public void validate_entry(String sConf, ServletContext context, String sUserID, String sUserFullName) throws Exception {
    	if (m_sVendorCode.compareToIgnoreCase("") == 0){
    		throw new Exception("Vendor number cannot be blank.");
    	}
    	//check for the vendor:
    	String SQL = "SELECT"
    		+ " " + SMTableicvendors.svendoracct
    		+ " FROM " + SMTableicvendors.TableName
    		+ " WHERE ("
    			+ "(" + SMTableicvendors.svendoracct + " = '" + m_sVendorCode + "')"
    		+ ")"
    	;
    	try {
			ResultSet rsVendor = clsDatabaseFunctions.openResultSet(
					SQL,
					context,
					sConf,
					"MySQL",
					this.toString() + ".validate vendor - user: " 
					+ sUserID
					+ " - "
					+ sUserFullName
					);
			if (!rsVendor.next()){
				rsVendor.close();
				throw new Exception("Vendor '" + m_sVendorCode + "' could not be found.");
			}
			rsVendor.close();
		} catch (Exception e) {
			throw new Exception("Error checking Vendor '" + m_sVendorCode + "' - " + e.getMessage());
		}
    	
    	if (m_sOrderNumber.compareToIgnoreCase("") == 0){
    		throw new Exception("Order number cannot be blank.");
    	}
    	//check for the order:
    	SQL = "SELECT"
    		+ " " + SMTableorderheaders.strimmedordernumber
    		+ " FROM " + SMTableorderheaders.TableName
    		+ " WHERE ("
    			+ "(" + SMTableorderheaders.strimmedordernumber + " = '" + m_sOrderNumber.trim() + "')"
    			+ " AND (" + SMTableorderheaders.datOrderCanceledDate + " < '1990-01-01')"
    		+ ")"
    	;
    	try {
			ResultSet rsOrder = clsDatabaseFunctions.openResultSet(
					SQL,
					context,
					sConf,
					"MySQL",
					this.toString() + ".validate order - user: " 
					+ sUserID
					+ " - "
					+ sUserFullName
					);
			if (!rsOrder.next()){
				rsOrder.close();
				throw new Exception("Order '" + m_sOrderNumber + "' could not be found.");
			}
			rsOrder.close();
		} catch (Exception e) {
			throw new Exception("Error checking Order '" + m_sOrderNumber + "' - " + e.getMessage());
		}
    	
        if (m_sExpectedShipDate.compareTo(EMPTY_DATE_STRING) != 0){
	        if (!clsDateAndTimeConversions.IsValidDateString("M/d/yyyy", m_sExpectedShipDate)){
	        	throw new Exception("Expected date '" + m_sExpectedShipDate + "' is invalid.  ");
	        }
        }
    	
    	String sDetailNumberList[] = m_sDetailNumberListString.split(SMCreatePOFromOrderEdit.PARAM_DETAILNUMBERDELIMITER);
    	for (int i = 0; i < sDetailNumberList.length; i++){
    		try {
				Integer.parseInt(sDetailNumberList[i]);
			} catch (Exception e) {
				throw new Exception("Error in detail number list - '" + sDetailNumberList[i] + "' is not valid.");
			}
    	}
    	
    	for (int i = 0; i < arrItemNumbers.size(); i++){
    		if (arrItemNumbers.get(i).compareToIgnoreCase("") == 0){
    			throw new Exception("Item number on line " + i + 1 + " cannot be blank.");
    		}else{
    			ICItem item = new ICItem(arrItemNumbers.get(i));
    			if (!item.load(context, sConf)){
    				throw new Exception("Invalid item number ('" + arrItemNumbers.get(i) + "') on line " + i + 1 + ".");
    			}
    			if (item.getActive().compareToIgnoreCase("0") == 0){
    				throw new Exception("Item number '" +  arrItemNumbers.get(i) + "' is inactive.");
    			}
    			if (item.getNonStockItem().compareToIgnoreCase("1") == 0){
    				throw new Exception("Item number '" +  arrItemNumbers.get(i) + "' is a non-stock item.");
    			}
    		}
    	}

    	for (int i = 0; i < arrItemDescriptions.size(); i++){
    		if (arrItemDescriptions.get(i).compareToIgnoreCase("") == 0){
    			throw new Exception("Item description on line " + i + 1 + " cannot be blank.");
    		}
    	}

    	for (int i = 0; i < arrUnitOfMeasures.size(); i++){
    		if (arrUnitOfMeasures.get(i).compareToIgnoreCase("") == 0){
    			throw new Exception("Unit of measure on line " + i + 1 + " cannot be blank.");
    		}
    	}
    	
    	BigDecimal bdUnitCost = null;
    	for (int i = 0; i < arrUnitCosts.size(); i++){
    		if (arrUnitCosts.get(i).compareToIgnoreCase("") == 0){
    			throw new Exception("Unit cost on line " + i + 1 + " cannot be blank.");
    		}
    		arrUnitCosts.set(i, arrUnitCosts.get(i).replace(",", "")); 
    		try {
				bdUnitCost = new BigDecimal(arrUnitCosts.get(i));
			} catch (Exception e) {
				throw new Exception("Unit cost on line " + i + 1 + " (" + arrUnitCosts.get(i) + ") is invalid.");
			}
    		if (bdUnitCost.compareTo(BigDecimal.ZERO) < 0){
    			throw new Exception("Unit cost on line " + i + 1 + " (" + arrUnitCosts.get(i) + ") cannot be less than zero.");
    		}
    	}

    	BigDecimal bdQty = null;
    	for (int i = 0; i < arrQuantities.size(); i++){
    		if (arrQuantities.get(i).compareToIgnoreCase("") == 0){
    			throw new Exception("Unit cost on line " + i + 1 + " cannot be blank.");
    		}
    		arrQuantities.set(i, arrQuantities.get(i).replace(",", "")); 
    		try {
    			bdQty = new BigDecimal(arrQuantities.get(i));
			} catch (Exception e) {
				throw new Exception("Quantity on line " + i + 1 + " (" + arrQuantities.get(i) + ") is invalid.");
			}
    		if (bdQty.compareTo(BigDecimal.ZERO) < 0){
    			throw new Exception("Quantity on line " + i + 1 + " (" + arrQuantities.get(i) + ") cannot be less than zero.");
    		}
    	}
    }
    public String dumpObject(){
    	String s = "\n Vendor: " + this.getVendorCode()
    		+ "\n detail list string: " + this.getDetailNumberListString()
    		+ "\n number of lines: " + this.getNumberOfLines()
    		+ "\n order number: " + this.getOrderNumber()
    		+ "\n vendor name: " + this.getVendorName()
    		+ "\n expected ship date: " + this.getExpectedShipDate()
    	;
    	for (int i = 0; i < this.getNumberOfLines(); i++){
    		s += "\n item number on line " + i + 1 + ": " + this.getItemNumber(i);
    		s += "\n item description on line " + i + 1 + ": " + this.getItemDescription(i);
    		s += "\n U/M on line " + i + 1 + ": " + this.getUnitOfMeasure(i);
    		s += "\n Qty on line " + i + 1 + ": " + this.getQuantity(i);
    		s += "\n Unit cost on line " + i + 1 + ": " + this.getUnitCost(i);
    	}
    	return s;
    }
}