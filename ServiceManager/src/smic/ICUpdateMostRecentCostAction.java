package smic;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import SMDataDefinition.SMTableicitems;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsManageBigDecimals;
import ServletUtilities.clsManageRequestParameters;
import smcontrolpanel.SMAuthenticate;
import smcontrolpanel.SMUtilities;

public class ICUpdateMostRecentCostAction extends HttpServlet {

	private static final long serialVersionUID = 1L;
	
	private static String m_sWarning = "";
	private boolean bDebugMode = false;
	public void doPost(HttpServletRequest request,
				HttpServletResponse response)
				throws ServletException, IOException {

		if (!SMAuthenticate.authenticateSMCPCredentials(
				request, 
				response, 
				getServletContext(), 
				-1L))
		{
			return;
		}

	    //Get the session info:
	    HttpSession CurrentSession = request.getSession(true);
	    String sDBID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_DATABASE_ID);
	    String sUserFullName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERFIRSTNAME) + " " 
	    					+ (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERLASTNAME) ;

	    String sItemNumber = clsManageRequestParameters.get_Request_Parameter(ICItem.ParamItemNumber, request);
	    String sMostRecentCost = clsManageRequestParameters.get_Request_Parameter(ICItem.ParamMostRecentCost, request);
	    String sCallingClass = clsManageRequestParameters.get_Request_Parameter("CallingClass", request);
	    String sPhysicalInventoryID = clsManageRequestParameters.get_Request_Parameter(
	    	ICPhysicalInventoryEntry.ParamID, request);
	    
		//Try to update the item:
		if (!updateItemCost(sItemNumber, sMostRecentCost, sDBID, sUserFullName)){
			response.sendRedirect(
				"" + SMUtilities.getURLLinkBase(getServletContext()) + "" + sCallingClass
				+ "?" + ICItem.ParamItemNumber + "=" + sItemNumber
				+ "&" + ICItem.ParamMostRecentCost + "=" + sMostRecentCost
				+ "&" + ICPhysicalInventoryEntry.ParamID + "=" + sPhysicalInventoryID
				+ "&Warning=" + m_sWarning
				+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
			);
		}else{
			response.sendRedirect(
				"" + SMUtilities.getURLLinkBase(getServletContext()) + "" + sCallingClass
				+ "?" + ICItem.ParamItemNumber + "=" + sItemNumber
				+ "&" + ICItem.ParamMostRecentCost + "=" + sMostRecentCost
				+ "&" + ICPhysicalInventoryEntry.ParamID + "=" + sPhysicalInventoryID
				+ "&Status=" + "Cost updated successfully."
				+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
			);
		}
		return;
	}
	private boolean updateItemCost(String sItem, String sMostRecentCost, String sDBID, String sUserFullName){
		
		//Make sure the most recent cost is ONLY to two decimal places:
		BigDecimal bdMRC = new BigDecimal(0);
		try {
			bdMRC = new BigDecimal(sMostRecentCost);
		} catch (Exception e1) {
			System.out.println(
				" In " + this.toString() 
				+ ".updateItemCost - error converting sMostRecentCost '" 
				+ sMostRecentCost + "' to a Big Decimal");
		}
		
		bdMRC = bdMRC.setScale(2, BigDecimal.ROUND_HALF_UP);
		
		String SQL = "UPDATE"
			+ " " + SMTableicitems.TableName
			+ " SET "
			+ SMTableicitems.bdmostrecentcost + " = " 
				+ clsManageBigDecimals.BigDecimalToScaledFormattedString(
					SMTableicitems.bdmostrecentcostScale, bdMRC).replace(",", "")
			+ " WHERE ("
				+ "(" + SMTableicitems.sItemNumber + " = '" + sItem + "')"
			+ ")"
		;

		if (bDebugMode){
			System.out.println("In " + this.toString() + ".updateItemCost - SQL = " + SQL);
		}
		try {
			if (!clsDatabaseFunctions.executeSQL(
					SQL, 
					getServletContext(), 
					sDBID, 
					"MySQL", 
					SMUtilities.getFullClassName(this.toString()) + ".updateItemCost - user: " + sUserFullName
			)){
				m_sWarning = "Error updating item number '" + sItem + "' with most recent cost " + sMostRecentCost
					+ ".";
				return false;
			}else{
				return true;
			}
		} catch (SQLException e) {
			m_sWarning = "Error updating item number '" + sItem + "' with most recent cost " + sMostRecentCost
			+ " - error = " + e.getMessage();
			return false;		
		}
	}
	public void doGet(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
		
		doPost(request, response);
	}
}
