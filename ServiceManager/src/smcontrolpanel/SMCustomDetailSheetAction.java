package smcontrolpanel;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import SMClasses.SMWorkOrderHeader;
import ServletUtilities.clsServletUtilities;
import ServletUtilities.clsManageRequestParameters;

public class SMCustomDetailSheetAction extends HttpServlet{
	
	private static final long serialVersionUID = 1L;
	private String sDBID = "";
	private String sUserID = "";
	private String sUserFullName = "";
	//private boolean bDebugMode = true;
	@Override
	public void doPost(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
	
		if (!SMAuthenticate.authenticateSMCPCredentials(
				request, 
				response, 
				getServletContext(),
				SMSystemFunctions.SMEditWorkOrders
			)
		){
			return;
		}
	    //Get the session info:
	    HttpSession CurrentSession = request.getSession(true);
	    sDBID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_DATABASE_ID);
	    sUserID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERID);
	    sUserFullName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERFIRSTNAME)
	    		+ " " + (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERLASTNAME);
	    String sWorkOrderID = clsManageRequestParameters.get_Request_Parameter(SMWorkOrderHeader.Paramlid, request);
	    String sViewPricingFlag = clsManageRequestParameters.get_Request_Parameter(SMWorkOrderEdit.VIEW_PRICING_FLAG, request);
	    String sDetailSheetName = clsManageRequestParameters.get_Request_Parameter(SMCustomDetailSheetEdit.PARAM_DETAIL_SHEET_NAME, request);

		String sRedirectString = 
			"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMWorkOrderEdit"
			+ "?" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
			+ "&" + SMWorkOrderHeader.Paramlid + "=" + sWorkOrderID
			+ "&" + SMWorkOrderEdit.VIEW_PRICING_FLAG + "=" + sViewPricingFlag
		; 
	    //If the user chose to cancel, then just redirect back to the work order edit screen:
	    if (clsManageRequestParameters.get_Request_Parameter(
	    	SMCustomDetailSheetEdit.CANCEL_BUTTON_NAME, request).compareToIgnoreCase(SMCustomDetailSheetEdit.CANCEL_BUTTON_LABEL) == 0){
	    	sRedirectString += "&Status=Detail sheet entry was canceled.";
	    	redirectProcess(sRedirectString, response);
	    	return;
	    }
	    
	    //If the user didn't choose 'cancel', then they must have chosen 'save', and we'll pick off the values they chose, and create new detail text for 
	    //the work order:
	    
		Enumeration <String> e = request.getParameterNames();
		ArrayList<String>arrDetailSheetParams = new ArrayList<String>(0);
		String sParam = "";
		
		while (e.hasMoreElements()){
			sParam = (String) e.nextElement();
			//Catch ONLY the parameters that are not 'predefined':
			if (
				(sParam.compareToIgnoreCase(SMWorkOrderHeader.Paramlid) != 0)
				&& (sParam.compareToIgnoreCase(SMCustomDetailSheetEdit.SAVE_BUTTON_NAME) != 0)
				&& (sParam.compareToIgnoreCase(SMCustomDetailSheetEdit.VIEW_RESULTS_BUTTON_NAME) != 0)
				&& (sParam.compareToIgnoreCase(SMCustomDetailSheetEdit.PARAM_DETAIL_SHEET_NAME) != 0)
				&& (sParam.compareToIgnoreCase(SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID) != 0)
				&& (sParam.compareToIgnoreCase(SMWorkOrderEdit.VIEW_PRICING_FLAG) != 0)
				&& (sParam.compareToIgnoreCase(SMDetailSheetEdit.DETAIL_SHEET_ID) != 0)
				&& (sParam.compareToIgnoreCase(SMDetailSheetEdit.BUTTON_SUBMIT_EDIT) != 0)
			){
				//Then it's a 'custom' parameter from the detail sheet and we'll add it:
				arrDetailSheetParams.add(sParam);
			}
		}
		//Sort the parameters to put them in the order specified by the detail sheet:
		Collections.sort(arrDetailSheetParams);

		//Now build them into a string to appear on the detail sheet:
		String sDetailSheetText = "\n\n"
			+ sDetailSheetName + "\n";
		
		for (int i = 0;i < arrDetailSheetParams.size(); i++){
			//DON'T print any parameters that have a 'blank' value:
			String sParamValue = clsManageRequestParameters.get_Request_Parameter(arrDetailSheetParams.get(i), request);
			if (sParamValue.compareToIgnoreCase("") != 0){
				sDetailSheetText += arrDetailSheetParams.get(i)
						+ ": "
						+ sParamValue
						+ "\n"
					;
			}
		}
		
		//If the user requested to see the 'test' results, then redirect to that page:
		if (clsManageRequestParameters.get_Request_Parameter(
			SMCustomDetailSheetEdit.VIEW_RESULTS_BUTTON_NAME, request).compareToIgnoreCase(SMCustomDetailSheetEdit.VIEW_RESULTS_BUTTON_LABEL) == 0){
			sRedirectString = 
				"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMTestDetailSheetResults"
				+ "?" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
				+ "&" + SMTestDetailSheetResults.DETAIL_FORM_RESULT_TEXT + "=" + clsServletUtilities.URLEncode(sDetailSheetText)
			; 
			redirectProcess(sRedirectString, response);
			return;
		}
		SMWorkOrderHeader wo = new SMWorkOrderHeader();
		wo.setlid(sWorkOrderID);;
		try {
			if (!wo.load(sDBID, sUserFullName, getServletContext())){
			   	sRedirectString += "&Warning=Error [1437073790] - Could not load work order #" + sWorkOrderID + " - " + wo.getErrorMessages();
				redirectProcess(sRedirectString, response);
				return;
			}
		} catch (Exception e1) {
		   	sRedirectString += "&Warning=Error [1437073791] loading work order #" + sWorkOrderID + " - " + e1.getMessage();
			redirectProcess(sRedirectString, response);
			return;
		}
		try {
			wo.addCustomDetailSheetText(
				sDetailSheetText, 
				getServletContext(), 
				sDBID, 
				sUserID,
				sUserFullName,
				SMWorkOrderHeader.SAVING_FROM_CUSTOM_DETAIL_TEXT_ENTRY);
		} catch (Exception e1) {
		   	sRedirectString += "&Warning=Error [1437073792] adding custom detail sheet to work order #" + sWorkOrderID + " - " + e1.getMessage();
			redirectProcess(sRedirectString, response);
			return;
		}
		
    	sRedirectString += "&Status=Detail sheet '" + clsServletUtilities.URLEncode(sDetailSheetName) + "' was saved.";
    	redirectProcess(sRedirectString, response);
    	return;
	}
	private void redirectProcess(String sRedirectString, HttpServletResponse res ){
		try {
			res.sendRedirect(sRedirectString);
		} catch (IOException e1) {
			System.out.println("Error [1395237124] in " + this.toString() + ".redirectAction - IOException error redirecting with string: "
					+ sRedirectString + " - " + e1.getMessage());
			return;
		} catch (IllegalStateException e1) {
			System.out.println("Error [1395237125] in " + this.toString() + ".redirectAction - IllegalStateException error redirecting with string: "
					+ sRedirectString + " - " + e1.getMessage());
			return;
		}
	}
	@Override
	public void doGet(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
		
		doPost(request, response);
	}
}
