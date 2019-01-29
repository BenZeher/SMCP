package smcontrolpanel;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Enumeration;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import SMClasses.SMOrderHeader;
import SMDataDefinition.SMTablechangeorders;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsManageRequestParameters;
import ServletUtilities.clsStringFunctions;

public class SMEditChangeOrdersAction extends HttpServlet{

	private static final long serialVersionUID = 1L;
	private ArrayList<SMChangeOrder> arrChangeOrders;

	public void doPost(HttpServletRequest request,
			HttpServletResponse response)
	throws ServletException, IOException {

		if (!SMAuthenticate.authenticateSMCPCredentials(
				request, 
				response, 
				getServletContext(), 
				SMSystemFunctions.SMEditChangeOrders)
		){
			return;
		}

		//Get the session info:
		HttpSession CurrentSession = request.getSession(true);
		String sDBID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_DATABASE_ID);
		String sUserName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERNAME);
		String sUserID = (String)CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERID);
		String sUserFullName = (String)CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERFIRSTNAME) +" " + 
						(String)CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERLASTNAME);
		String sCallingClass = clsManageRequestParameters.get_Request_Parameter("CallingClass", request);
		String sRedirectString = "";
		String sOrderNumber = "";

		//Make sure there's nothing in the session attribute - we'll read from the request instead:
		CurrentSession.removeAttribute(SMEditChangeOrdersEdit.CHANGE_ORDER_ARRAY_ATTRIBUTE);
		arrChangeOrders = new ArrayList<SMChangeOrder>(0);

		//This gives us the order number, we won't have that before this method runs:
		sOrderNumber = loadCOArray(request);

		//If it's a request to delete a Change order, process that and return:
		Enumeration <String> e = request.getParameterNames();
		String sParam = "";
		while (e.hasMoreElements()){
			sParam = e.nextElement();
			if (sParam.contains(SMEditChangeOrdersEdit.DELETE_BUTTON_NAME_PREFIX)){
				//IF it's not null:
				if (request.getParameter(sParam) != null){
					String sID = sParam.substring((SMEditChangeOrdersEdit.DELETE_BUTTON_NAME_PREFIX).length(), sParam.length());
					try {
						deleteCO(sID, sDBID, sUserName,sUserID, sUserFullName, request);
					} catch (Exception e2) {
						sRedirectString = 
								"" + SMUtilities.getURLLinkBase(getServletContext()) + sCallingClass
								+ "?" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
								+ "&" + SMOrderHeader.ParamsOrderNumber + "=" + sOrderNumber
								+ "&Warning=" + e2.getMessage()
								;	
					}
					sRedirectString = 
							"" + SMUtilities.getURLLinkBase(getServletContext()) + sCallingClass
							+ "?" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
							+ "&" + SMOrderHeader.ParamsOrderNumber + "=" + sOrderNumber
							+ "&Status=" + "Change order successfully deleted."
							;	
					try {
						response.sendRedirect(sRedirectString);
					} catch (IOException e1) {
						System.out.println("In " + this.toString() + ".redirectAction - error redirecting with string: "
								+ sRedirectString);
					}
					return;

				}
				//sRecordNumber = sParam.substring(
				//(ICEnterInvoiceEdit.FIND_EXPENSE_ACCT_PARAMETER).length(), sParam.length());
			}
		}

		try {
			saveChangeOrders(sDBID, sUserName);
		} catch (Exception e2) {
			CurrentSession.setAttribute(SMEditChangeOrdersEdit.CHANGE_ORDER_ARRAY_ATTRIBUTE, arrChangeOrders);
			sRedirectString = 
				"" + SMUtilities.getURLLinkBase(getServletContext()) + sCallingClass
				+ "?" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
				+ "&" + SMOrderHeader.ParamsOrderNumber + "=" + sOrderNumber
				+ "&Warning=" + e2.getMessage()
				;
		}
		sRedirectString = 
			"" + SMUtilities.getURLLinkBase(getServletContext()) + sCallingClass
			+ "?" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
			+ "&" + SMOrderHeader.ParamsOrderNumber + "=" + sOrderNumber
			+ "&Status=Changed orders successfully saved."
		;

		try {
			response.sendRedirect(sRedirectString);
		} catch (IOException e1) {
			System.out.println("Error [1548471253] In " + this.toString() + ".redirectAction - error redirecting with string: "
					+ sRedirectString);
			return;
		}
		return;
	}
	private void deleteCO(
		String sChangeOrderID, 
		String sDBID, 
		String sUser, 
		String sUserID, 
		String sUserFullName,  
		HttpServletRequest req) throws Exception{

		//check to see if the delete was confirmed:
		if (req.getParameter(SMEditChangeOrdersEdit.DELETE_CONFIRM_CHECKBOX_NAME + sChangeOrderID) == null){
			throw new Exception("You must check the confirming checkbox to delete a change order.");
		}

		Connection conn = clsDatabaseFunctions.getConnection(
				getServletContext(), 
				sDBID, 
				"MySQL", 
				this.toString() + ".deleteCO - user: " + sUserID + " - " + sUserFullName
		);
		if (conn == null){
			throw new Exception("Could not open data connection to delete CO.");
		}

		String SQL = "DELETE FROM " + SMTablechangeorders.TableName
		+ " WHERE ("
		+ "(" + SMTablechangeorders.iID + " = " + sChangeOrderID + ")"
		+ ")"
		;

		try {
			Statement stmt = conn.createStatement();
			stmt.execute(SQL);
		} catch (SQLException e) {
			clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1547080478]");
			throw new Exception("Error deleting change order - " + e.getMessage());
		}

		clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1547080479]");

		return;
	}
	private void saveChangeOrders(String sDBID, String sUser) throws Exception{

		//First, go through the array and format the change order numbers:
		for (int i = 0; i < arrChangeOrders.size(); i++){
			if (arrChangeOrders.get(i).getM_dChangeOrderNumber().trim().compareToIgnoreCase("") == 0){
				//Don't check the blanks - they won't be saved anyway:
			}else{
				int iTest = 0;
				try {
					iTest = Integer.parseInt(arrChangeOrders.get(i).getM_dChangeOrderNumber());
					if ((iTest <=0)){
						throw new Exception("Change order number '" + arrChangeOrders.get(i).getM_dChangeOrderNumber() 
								+ "' is invalid.");
					}
					arrChangeOrders.get(i).setM_dChangeOrderNumber(Integer.toString(iTest));
				} catch (NumberFormatException e) {
					throw new Exception("Change order number '" + arrChangeOrders.get(i).getM_dChangeOrderNumber()
							+ "' is invalid.");
				}
				arrChangeOrders.get(i).setM_dChangeOrderNumber(Integer.toString(iTest));
			}
		}

		//Make sure there are no duplicate CO numbers:
		for (int i = 0; i < arrChangeOrders.size(); i++){
			for (int j = 0; j < arrChangeOrders.size(); j++){
				//Don't compare one to itself:
				if (i == j){
				}else{
					if (arrChangeOrders.get(i).getM_dChangeOrderNumber().compareToIgnoreCase(
							arrChangeOrders.get(j).getM_dChangeOrderNumber()) == 0){
						throw new Exception("Change order number '" 
								+ arrChangeOrders.get(j).getM_dChangeOrderNumber() + "' is a duplicate.");
					}
				}
			}
		}

		Connection conn = clsDatabaseFunctions.getConnection(
				getServletContext(), 
				sDBID, 
				"MySQL", 
				this.toString() + ".saveChangeOrders - user: " + sUser
		);
		if (conn == null){
			throw new Exception("Could not get data connection to save change orders");
		}

		if (!clsDatabaseFunctions.start_data_transaction(conn)){
			clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1547080480]");
			throw new Exception("Could not start data transaction to save change orders");
		}

		for (int i = 0; i < arrChangeOrders.size(); i++){
			//Don't save change orders with a blank description and change order number - these are the 'additional
			//lines' and nothing would have been added to them if the desc or CO number is blank:
			if (
					(arrChangeOrders.get(i).getM_dChangeOrderNumber().compareToIgnoreCase("") == 0)
					&& (arrChangeOrders.get(i).getM_sDesc().compareToIgnoreCase("") == 0)
			){
			}else{
				if (!arrChangeOrders.get(i).save_without_data_transaction(conn, sUser)){
					clsDatabaseFunctions.rollback_data_transaction(conn);
					clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1547080481]");
					throw new Exception("CO " + arrChangeOrders.get(i).getM_dChangeOrderNumber() + ": " 
							+ arrChangeOrders.get(i).getErrorMessages());
				}
			}
		}
		if (!clsDatabaseFunctions.commit_data_transaction(conn)){
			clsDatabaseFunctions.rollback_data_transaction(conn);
			throw new Exception("Error [1548471144] committing data transaction.");
		}
		clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1547080482]");
		return;	
	}

	private String loadCOArray(HttpServletRequest req){

		String sOrderNumber = "";
		
		int iNumberOfLines = Integer.parseInt(
				clsManageRequestParameters.get_Request_Parameter(SMEditChangeOrdersEdit.NUMBER_OF_LINES, req));

		for (int i = 1; i <= iNumberOfLines; i++){
			SMChangeOrder co = new SMChangeOrder();
			co.setM_dAmount(clsManageRequestParameters.get_Request_Parameter(
					SMEditChangeOrdersEdit.CO_LINE_PARAM_PREFIX 
					+ SMChangeOrder.ParamdAmount + clsStringFunctions.PadLeft(Integer.toString(i), "0", 6),
					req));
			co.setM_datChangeOrderDate(clsManageRequestParameters.get_Request_Parameter(
					SMEditChangeOrdersEdit.CO_LINE_PARAM_PREFIX 
					+ SMChangeOrder.ParamdatChangeOrderDate + clsStringFunctions.PadLeft(Integer.toString(i), "0", 6),
					req));
			co.setM_dChangeOrderNumber(clsManageRequestParameters.get_Request_Parameter(
					SMEditChangeOrdersEdit.CO_LINE_PARAM_PREFIX 
					+ SMChangeOrder.ParamdChangeOrderNumber + clsStringFunctions.PadLeft(Integer.toString(i), "0", 6),
					req));
			co.setM_dTotalMarkUp(clsManageRequestParameters.get_Request_Parameter(
					SMEditChangeOrdersEdit.CO_LINE_PARAM_PREFIX 
					+ SMChangeOrder.ParamdTotalMarkUp + clsStringFunctions.PadLeft(Integer.toString(i), "0", 6),
					req));
			co.setM_dTruckDays(clsManageRequestParameters.get_Request_Parameter(
					SMEditChangeOrdersEdit.CO_LINE_PARAM_PREFIX 
					+ SMChangeOrder.ParamdTruckDays + clsStringFunctions.PadLeft(Integer.toString(i), "0", 6),
					req));
			co.setM_iID(clsManageRequestParameters.get_Request_Parameter(
					SMEditChangeOrdersEdit.CO_LINE_PARAM_PREFIX 
					+ SMChangeOrder.ParamiID + clsStringFunctions.PadLeft(Integer.toString(i), "0", 6),
					req));
			co.setM_sDesc(clsManageRequestParameters.get_Request_Parameter(
					SMEditChangeOrdersEdit.CO_LINE_PARAM_PREFIX 
					+ SMChangeOrder.ParamsDesc + clsStringFunctions.PadLeft(Integer.toString(i), "0", 6),
					req));
			co.setM_sJobNumber(clsManageRequestParameters.get_Request_Parameter(
					SMEditChangeOrdersEdit.CO_LINE_PARAM_PREFIX 
					+ SMChangeOrder.ParamsJobNumber + clsStringFunctions.PadLeft(Integer.toString(i), "0", 6),
					req));

			//If there are NO values on a line, it's just the blank line at the bottom and we can ignore it:
			if (
				(co.getM_dAmount().compareToIgnoreCase("0.00") == 0)
				&& (co.getM_datChangeOrderDate().compareToIgnoreCase(SMChangeOrder.EMPTY_DATE_STRING) == 0)
				&& (co.getM_dChangeOrderNumber().compareToIgnoreCase("") == 0)
				&& (co.getM_dTotalMarkUp().compareToIgnoreCase("0.00") == 0)
				&& (co.getM_dTruckDays().compareToIgnoreCase("0.0000") == 0)
				&& (co.getM_sDesc().compareToIgnoreCase("") == 0)
			){
				//Don't load this one . . . 
			}else{
				//Add the change order to the array:
				arrChangeOrders.add(co);
			}
			//System.out.println("for i = " + i + " co = \n" + co.read_out_debug_data());
			//Get the order number here
			sOrderNumber = co.getM_sJobNumber();
		}
		return sOrderNumber;
	}
	public void doGet(HttpServletRequest request,
			HttpServletResponse response)
	throws ServletException, IOException {

		doPost(request, response);
	}
}