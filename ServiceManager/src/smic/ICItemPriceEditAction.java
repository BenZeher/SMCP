package smic;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import SMDataDefinition.SMTableicitemprices;
import SMDataDefinition.SMTableicitems;
import SMDataDefinition.SMTablepricelistcodes;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsManageBigDecimals;
import ServletUtilities.clsManageRequestParameters;
import smcontrolpanel.SMAuthenticate;
import smcontrolpanel.SMPriceLevelLabels;
import smcontrolpanel.SMSystemFunctions;
import smcontrolpanel.SMUtilities;

public class ICItemPriceEditAction extends HttpServlet{
	private static final long serialVersionUID = 1L;
	
	public void doPost(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
	
		if (!SMAuthenticate.authenticateSMCPCredentials(
				request, 
				response, 
				getServletContext(), 
				SMSystemFunctions.ICEditItemPricing))
		{
			return;
		}

	    //Get the session info:
	    HttpSession CurrentSession = request.getSession(true);
	    String sDBID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_DATABASE_ID);
	    String sUserID = "0";
	    	   sUserID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERID);
	    String sUserFullName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERFIRSTNAME) + " "
	    		      + (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERLASTNAME);
	    
	    String sCallingClass = clsManageRequestParameters.get_Request_Parameter("CallingClass", request);
		String sErrorMessage = "";
		
	    //First get the input fields:
	    String sItemNumber = clsManageRequestParameters.get_Request_Parameter("ItemNumber", request).trim();
	    String sPriceListCode = clsManageRequestParameters.get_Request_Parameter("PriceListCode", request).trim();
	    String sPriceListCodeDesc = clsManageRequestParameters.get_Request_Parameter("PriceListCodeDesc", request).trim();
	    String sBasePrice = clsManageRequestParameters.get_Request_Parameter("BasePrice", request).trim().replace(",", "");
	    String sPriceLevel1 = clsManageRequestParameters.get_Request_Parameter("PriceLevel1", request).trim().replace(",", "");
	    String sPriceLevel2 = clsManageRequestParameters.get_Request_Parameter("PriceLevel2", request).trim().replace(",", "");
	    String sPriceLevel3 = clsManageRequestParameters.get_Request_Parameter("PriceLevel3", request).trim().replace(",", "");
	    String sPriceLevel4 = clsManageRequestParameters.get_Request_Parameter("PriceLevel4", request).trim().replace(",", "");
	    String sPriceLevel5 = clsManageRequestParameters.get_Request_Parameter("PriceLevel5", request).trim().replace(",", "");

	    
	    try{
	    	saveItemPrice(
		    		sItemNumber,
		    		sPriceListCode,
		    		sUserFullName,
		    		sUserID,
		    		sDBID,
		    		sBasePrice,
		    		sPriceLevel1,
		    		sPriceLevel2,
		    		sPriceLevel3,
		    		sPriceLevel4,
		    		sPriceLevel5)
	    		;
	    	response.sendRedirect(
					"" + SMUtilities.getURLLinkBase(getServletContext()) + "" + sCallingClass
					+ "?ItemNumber=" + sItemNumber
					+ "&PriceListCode=" + sPriceListCode
					+ "&Status=Item price saved successfully"
					+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
			);
	    	
	    }catch(Exception e) {
	    	response.sendRedirect(
					"" + SMUtilities.getURLLinkBase(getServletContext()) + "" + sCallingClass
					+ "?ItemNumber=" + sItemNumber
					+ "&PriceListCode=" + sPriceListCode
					+ "&PriceListCodeDesc=" + sPriceListCodeDesc
					+ "&BasePrice=" + sBasePrice
					+ "&PriceLevel1=" + sPriceLevel1
					+ "&PriceLevel2=" + sPriceLevel2
					+ "&PriceLevel3=" + sPriceLevel3
					+ "&PriceLevel4=" + sPriceLevel4
					+ "&PriceLevel5=" + sPriceLevel5
					+ "&Reprocess=true"
					+ "&Warning=Could not save: " + sErrorMessage
					+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
			);
	    }
		return;
	}
	private void saveItemPrice(
			String sItemNumber, 
			String sPriceListCode, 
			String sUserFullName, 
			String sUserID, 
			String sDBID,
			String sBasePrice,
			String sPriceLevel1,
			String sPriceLevel2,
			String sPriceLevel3,
			String sPriceLevel4,
			String sPriceLevel5) throws Exception{
		
		Connection conn;
		try {
			conn = ServletUtilities.clsDatabaseFunctions.getConnectionWithException(
				getServletContext(), 
				sDBID, 
				"MySQL", 
				this.toString() + ".doPost - user: " + sUserID);
		} catch (Exception e) {
			throw new Exception("Error [2020351654243] " + "getting connection - " + e.getMessage());
		}
		
		try {
			validateFields(
					sItemNumber, 
					sPriceListCode, 
					sDBID, 
					sUserFullName,
					sBasePrice,
					sPriceLevel1,
					sPriceLevel2,
					sPriceLevel3,
					sPriceLevel4,
					sPriceLevel5,
					conn
					);
		}catch (Exception e) {
			ServletUtilities.clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1580910619]");
			throw new Exception("Error in validation - " + e.getMessage());
		}
		ServletUtilities.clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1580910620]");
		
		String SQL = "INSERT INTO " + SMTableicitemprices.TableName
			+ " ("
			+ SMTableicitemprices.bdBasePrice
			+ ", " + SMTableicitemprices.bdLevel1Price
			+ ", " + SMTableicitemprices.bdLevel2Price
			+ ", " + SMTableicitemprices.bdLevel3Price
			+ ", " + SMTableicitemprices.bdLevel4Price
			+ ", " + SMTableicitemprices.bdLevel5Price
			+ ", " + SMTableicitemprices.datLastMaintained
			+ ", " + SMTableicitemprices.sItemNumber
			+ ", " + SMTableicitemprices.sLastEditUserFullName
			+ ", " + SMTableicitemprices.lLastEditUserID
			+ ", " + SMTableicitemprices.sPriceListCode
			+ ") VALUES ("
			+ sBasePrice
			+ ", " + sPriceLevel1
			+ ", " + sPriceLevel2
			+ ", " + sPriceLevel3
			+ ", " + sPriceLevel4
			+ ", " + sPriceLevel5
			+ ", NOW()"
			+ ", '" + sItemNumber + "'"
			+ ", '" + clsDatabaseFunctions.FormatSQLStatement(sUserFullName) + "'"
			+ ", " + sUserID + ""
			+ ", '" + sPriceListCode + "'"
			+ ")"
			+ " ON DUPLICATE KEY UPDATE"
			+ " " + SMTableicitemprices.bdBasePrice + " = " + sBasePrice
			+ ", " + SMTableicitemprices.bdLevel1Price + " = " + sPriceLevel1
			+ ", " + SMTableicitemprices.bdLevel2Price + " = " + sPriceLevel2
			+ ", " + SMTableicitemprices.bdLevel3Price + " = " + sPriceLevel3
			+ ", " + SMTableicitemprices.bdLevel4Price + " = " + sPriceLevel4
			+ ", " + SMTableicitemprices.bdLevel5Price + " = " + sPriceLevel5
			+ ", " + SMTableicitemprices.datLastMaintained + " = NOW()"
			+ ", " + SMTableicitemprices.sLastEditUserFullName + " = '" + clsDatabaseFunctions.FormatSQLStatement(sUserFullName) + "'"
			+ ", " + SMTableicitemprices.lLastEditUserID + " = " + sUserID + ""
			;
		
		//System.out.println("In " + SMUtilities.getFullClassName(this.toString()) + ":saveItem, SQL = " + SQL);
		try {
			if (!clsDatabaseFunctions.executeSQL(
					SQL, 
					getServletContext(), 
					sDBID, 
					"MySQL", 
					SMUtilities.getFullClassName(this.toString()) + ".saveItemPrice - user: " + sUserFullName)){
				throw new Exception("Could not update/insert item price record.");
			}
		} catch (SQLException e) {
			throw new Exception("Could not update/insert item price record - " + e.getMessage());
		}
	}
	private void validateFields(
    		String sItemNumber,
    		String sPriceListCode,
    		String sDBID,
    		String sUserFullName,
    		String sBasePrice,
			String sPriceLevel1,
			String sPriceLevel2,
			String sPriceLevel3,
			String sPriceLevel4,
			String sPriceLevel5,
			Connection conn
    	)throws Exception{
		
		boolean bResult = true;
		String sErrorMessage = "";
		
		//Validate the item number
		if (!validateItem(sItemNumber, sDBID, sUserFullName)){
			sErrorMessage += "<BR>Could not validate item number '" + sItemNumber + "'.";
			bResult = false;
		}
		//Validate the price list code
		if (!validatePriceCode(sPriceListCode, sDBID, sUserFullName)){
			sErrorMessage += "<BR>Could not validate price list code '" + sPriceListCode + "'.";
			bResult = false;
		}
		
		SMPriceLevelLabels pricelevellabels = new SMPriceLevelLabels();
		try {
			pricelevellabels.load(conn);
		} catch (Exception e1) {
			throw new Exception("Error [1580852368] reading price level labels: " + e1.getMessage());
		}
		
		//Validate the dollar values:
		if (sBasePrice.compareToIgnoreCase("") == 0){
			sBasePrice = "0.00";
		}else{
			if (!isValidAmount(sBasePrice, 2)){
				sErrorMessage += "<BR>Invalid " + pricelevellabels.get_sbaselabel() + ": '" + sBasePrice + "'.";
				bResult = false;
			}
		}
		
		if (sPriceLevel1.compareToIgnoreCase("") == 0){
			sPriceLevel1 = "0.00";
		}else{
			if (!isValidAmount(sPriceLevel1, 2)){
				sErrorMessage += "<BR>Invalid " + pricelevellabels.get_slevel1label() + " price: '" + sPriceLevel1 + "'.";
				bResult = false;
			}
		}
		
		if (sPriceLevel2.compareToIgnoreCase("") == 0){
			sPriceLevel2 = "0.00";
		}else{
			if (!isValidAmount(sPriceLevel2, 2)){
				sErrorMessage += "<BR>Invalid " + pricelevellabels.get_slevel2label() + " price: '" + sPriceLevel2 + "'.";
				bResult = false;
			}
		}

		if (sPriceLevel3.compareToIgnoreCase("") == 0){
			sPriceLevel3 = "0.00";
		}else{
			if (!isValidAmount(sPriceLevel3, 2)){
				sErrorMessage += "<BR>Invalid " + pricelevellabels.get_slevel3label() + " price: '" + sPriceLevel3 + "'.";
				bResult = false;
			}
		}

		if (sPriceLevel4.compareToIgnoreCase("") == 0){
			sPriceLevel4 = "0.00";
		}else{
			if (!isValidAmount(sPriceLevel4, 2)){
				sErrorMessage += "<BR>Invalid " + pricelevellabels.get_slevel4label() + " price: '" + sPriceLevel4 + "'.";
				bResult = false;
			}
		}

		if (sPriceLevel5.compareToIgnoreCase("") == 0){
			sPriceLevel5 = "0.00";
		}else{
			if (!isValidAmount(sPriceLevel5, 2)){
				sErrorMessage += "<BR>Invalid " + pricelevellabels.get_slevel5label() + " price: '" + sPriceLevel5 + "'.";
				bResult = false;
			}
		}
		if(!bResult) {
			throw new Exception(sErrorMessage);
		}
	}
	private boolean isValidAmount(String sAmt, int iScale){
		
    	try{
    		sAmt = sAmt.replace(",", "");
    		BigDecimal bd = new BigDecimal(sAmt);
    		bd = bd.setScale(iScale, BigDecimal.ROUND_HALF_UP);
    		sAmt =  clsManageBigDecimals.BigDecimalTo2DecimalSTDFormat(bd);
    		return true;
    	}catch (NumberFormatException e){
    		return false;
    	}
	}
	private boolean validateItem(String sItem, String sDBID, String sUserFullName){
		
		String SQL = "SELECT"
			+ " " + SMTableicitems.sItemNumber
			+ " FROM " + SMTableicitems.TableName
			+ " WHERE ("
				+ SMTableicitems.sItemNumber + " = '" + sItem + "'"
			+ ")"
			;
		//System.out.println(SQL);
		try {
			ResultSet rs = clsDatabaseFunctions.openResultSet(
					SQL, 
					getServletContext(), 
					sDBID, 
					"MySQL", 
					SMUtilities.getFullClassName(this.toString()) + ".validateItem - user: " + sUserFullName
					);
			if (rs.next()){
				rs.close();
				return true;
			}else{
				rs.close();
				return false;
			}
		} catch (SQLException e) {
			return false;
		}
	}
	private boolean validatePriceCode(String sPriceCode, String sDBID, String sUserFullName){
		
		String SQL = "SELECT"
			+ " " + SMTablepricelistcodes.spricelistcode
			+ " FROM " + SMTablepricelistcodes.TableName
			+ " WHERE ("
				+ SMTablepricelistcodes.spricelistcode + " = '" + sPriceCode + "'"
			+ ")"
			;
		
		try {
			ResultSet rs = clsDatabaseFunctions.openResultSet(
					SQL, 
					getServletContext(), 
					sDBID, 
					"MySQL", 
					SMUtilities.getFullClassName(this.toString()) + ".validatePriceCode - user: " + sUserFullName
					);
			if (rs.next()){
				rs.close();
				return true;
			}else{
				rs.close();
				return false;
			}
		} catch (SQLException e) {
			return false;
		}
	}

	public void doGet(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
		
		doPost(request, response);
	}
}
