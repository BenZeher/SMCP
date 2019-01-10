package smic;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Enumeration;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import SMDataDefinition.SMTableicitemlocations;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsManageRequestParameters;
import smcontrolpanel.SMAuthenticate;
import smcontrolpanel.SMSystemFunctions;
import smcontrolpanel.SMUtilities;

public class ICEditItemsAction extends HttpServlet{
	private static final long serialVersionUID = 1L;
	private static String sDBID = "";
	private static String sUserID = "";
	private static String sUserFullName = "";
	public void doPost(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
	
		if (!SMAuthenticate.authenticateSMCPCredentials(
				request, 
				response, 
				getServletContext(), 
				SMSystemFunctions.ICEditItems))
		{
			return;
		}

	    //Get the session info:
	    HttpSession CurrentSession = request.getSession(true);
	    sDBID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_DATABASE_ID);
	    sUserID = (String)CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERID);
	    sUserFullName = (String)CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERFIRSTNAME) + " "
	    				+ (String)CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERLASTNAME);
		ICItem item = new ICItem("");
		item.loadFromHTTPRequest(request);
		Connection conn = clsDatabaseFunctions.getConnection(
			getServletContext(), 
			sDBID,
			"MySQL",
			this.toString() + ".doPost - User: " + sUserID
			+ " - "
			+ sUserFullName
				);
		
		//Need a connection here because it involves a data transaction:
		if(!item.save(sUserFullName, sUserID, conn)){
			clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1547080832]");
			response.sendRedirect(
					"" + SMUtilities.getURLLinkBase(getServletContext()) + "smic.ICEditItemsEdit"
					+ "?" + item.getQueryString()
					+ "&Warning=Could not save: " + item.getErrorMessageString()
					+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
			);
			return;
		}
		
		//process minimum qty on hand
		ArrayList<String> alLocations = new ArrayList<String> (0);
		ArrayList<BigDecimal> alMinQty = new ArrayList<BigDecimal> (0);
		Enumeration<String> enumParameters = request.getParameterNames();
		while(enumParameters.hasMoreElements()) {
			String sParamName = (String) enumParameters.nextElement();
			
			//If the parameter key is LESS than 3 characters, then don't even try this:
			if (sParamName.length() >= "LOC".length()){
				if (sParamName.substring(0,3).compareTo("LOC") == 0){
					alLocations.add(sParamName.substring(3));
					try{
						alMinQty.add(new BigDecimal(request.getParameter(sParamName)));
					}catch(NumberFormatException e){
						clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1547080833]");
						response.sendRedirect(
							"" + SMUtilities.getURLLinkBase(getServletContext()) + "smic.ICEditItemsEdit"
							+ "?" + item.getQueryString()
							+ "&Warning=[1393511066] Error parsing minimum quantity entered for location " + sParamName.substring(3)
							+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID);
						return;
					}
				}
			}
		}
		//Start a data connection
		if (!clsDatabaseFunctions.start_data_transaction(conn)){
			clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1547080834]");
			return;
		}
		try{
			for (int i = 0; i < alLocations.size(); i ++){
				//System.out.println("[1382549750] i = " + i);
				String sSQL = "INSERT INTO " + SMTableicitemlocations.TableName + "("
						+ SMTableicitemlocations.sLocation
						+ ", " + SMTableicitemlocations.sItemNumber
						+ ", " + SMTableicitemlocations.sMinQtyOnHand
						+ ", " + SMTableicitemlocations.sQtyOnHand
						+ ", " + SMTableicitemlocations.sTotalCost
						+ ") VALUES ("
						+ "'" + (String) alLocations.get(i) + "'"
						+ ", '" + item.getItemNumber() + "'"
						+ ", '" +(BigDecimal) alMinQty.get(i) + "'"
						+ ", '0'"
						+ ", '0'"
						+ ") ON DUPLICATE KEY UPDATE "
						+ SMTableicitemlocations.sMinQtyOnHand + " = " + (BigDecimal) alMinQty.get(i)
					;
				//System.out.println(sSQL);

				clsDatabaseFunctions.executeSQL(sSQL, conn);
			}
		}catch (SQLException ex){
			System.out.println("ICEditItemsAction Error: error updating record.");
		}
			
		if (!clsDatabaseFunctions.commit_data_transaction(conn)){
			clsDatabaseFunctions.rollback_data_transaction(conn);
			clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1547080835]");
			return;
		}
		
		clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1547080836]");
		
		//If we are returning from creating an item through the PO system, we'll return to editing the PO:
		if (clsManageRequestParameters.get_Request_Parameter(ICEditPOLineEdit.CREATE_ITEM_BUTTON, request).compareToIgnoreCase("") != 0){
			response.sendRedirect(
				"" + SMUtilities.getURLLinkBase(getServletContext()) + "smic.ICEditPOLineEdit"
				+ "?" + "" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
				+ "&" + ICPOLine.Paramlid + "=" + clsManageRequestParameters.get_Request_Parameter(ICPOLine.Paramlid, request)
				+ "&" + ICPOLine.Paramlpoheaderid + "=" 
					+ clsManageRequestParameters.get_Request_Parameter(ICPOLine.Paramlpoheaderid, request)
			);

		}else{
			//If we are just returning to 'ICEditItemsEdit', we'll do that now:
			response.sendRedirect(
					"" + SMUtilities.getURLLinkBase(getServletContext()) + "smic.ICEditItemsEdit"
					+ "?" + item.getQueryString()
					+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
			);
		}
		
		return;
	}
	public void doGet(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
		
		doPost(request, response);
	}
}
