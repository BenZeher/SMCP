package smcontrolpanel;

import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import SMDataDefinition.SMTableorderheaders;
import ServletUtilities.clsManageRequestParameters;
import ServletUtilities.clsStringFunctions;

public class SMIncorporateSummaryAction extends HttpServlet{
	
	private static final long serialVersionUID = 1L;
	private boolean bDebugMode = false;
	
	public void doPost(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
	
		SMMasterEditAction smaction = new SMMasterEditAction(request, response);
		if (!smaction.processSession(getServletContext(), SMSystemFunctions.SMDirectItemEntry)){return;}

		//If there is an object in the session, destroy it:
		smaction.getCurrentSession().removeAttribute(SMDirectOrderDetailEntry.ParamObjectName);
		
		if (bDebugMode){
			System.out.println("[1579269679] In " + this.toString() + " - PRINTING PARAMS\n");
			PrintWriter debugout = new PrintWriter(System.out);
			clsManageRequestParameters.printRequestParameters(debugout, request);
		}
		//Read the entry fields from the request:
		SMDirectOrderDetailEntry entry = new SMDirectOrderDetailEntry(request);

		if (bDebugMode){
			//PrintWriter debugout = new PrintWriter(System.out);
			System.out.println("[1579269684] In " + this.toString() + " - entry= \n" + entry.read_out_debug_data());
			//SMUtilities.printRequestParameters(debugout, request);
		}
		
		//Special cases:
		//********************************
       	//If it's a request to add a new blank line:
    	if (clsManageRequestParameters.get_Request_Parameter(
    			SMEditDirectOrderDetailEntry.ADDBLANKLINE_BUTTON_NAME, request).compareToIgnoreCase(
    					SMEditDirectOrderDetailEntry.ADDBLANKLINE_BUTTON_LABEL) == 0){
    		//Add a new line:
    		entry.addNewLine();
    		//Save the entry in a session object:
    		smaction.getCurrentSession().setAttribute(SMDirectOrderDetailEntry.ParamObjectName, entry);
    		//Now return to the edit screen:
			String sRedirectString = SMUtilities.getURLLinkBase(getServletContext())
				+ "smcontrolpanel.SMEditDirectOrderDetailEntry"
				+ "?" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + smaction.getsDBID()
				+ "&CallingClass=" + SMUtilities.getFullClassName(this.toString())
				+ "&" + SMEditDirectOrderDetailEntry.CHOOSE_CATEGORY_METHOD_PARAM + "=" 
					+ clsManageRequestParameters.get_Request_Parameter(SMEditDirectOrderDetailEntry.CHOOSE_CATEGORY_METHOD_PARAM, request)
			;
			redirectProcess(sRedirectString, response);
			return;
    	}
    	
       	//If it's a request to calculate the billing values:
    	if (clsManageRequestParameters.get_Request_Parameter(
    			SMEditDirectOrderDetailEntry.CALCULATE_BUTTON_NAME, request).compareToIgnoreCase(
    					SMEditDirectOrderDetailEntry.CALCULATE_BUTTON_LABEL) == 0){
    		String sRedirectString = "";

    		try {
				entry.calculateBillingValues(getServletContext(), 
											smaction.getsDBID(), 
											smaction.getUserName(),
											smaction.getUserID(),
											smaction.getFullUserName()
						);
			} catch (Exception e) {
    			smaction.getCurrentSession().setAttribute(SMDirectOrderDetailEntry.ParamObjectName, entry);
    			sRedirectString = SMUtilities.getURLLinkBase(getServletContext())
    					+ "smcontrolpanel.SMEditDirectOrderDetailEntry"
    					+ "?" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + smaction.getsDBID()
    					+ "&CallingClass=" + SMUtilities.getFullClassName(this.toString())
    					+ "&" + SMEditDirectOrderDetailEntry.CHOOSE_CATEGORY_METHOD_PARAM + "=" 
    						+ clsManageRequestParameters.get_Request_Parameter(SMEditDirectOrderDetailEntry.CHOOSE_CATEGORY_METHOD_PARAM, request)
    					+ "&Warning=Error: " + clsStringFunctions.filter(e.getMessage())
    				;
    			redirectProcess(sRedirectString, response);
    			return;
			}
    		//Save the entry in a session object:
    		smaction.getCurrentSession().setAttribute(SMDirectOrderDetailEntry.ParamObjectName, entry);
    		//Now return to the edit screen:
			sRedirectString = SMUtilities.getURLLinkBase(getServletContext())
				+ "smcontrolpanel.SMEditDirectOrderDetailEntry"
				+ "?" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + smaction.getsDBID()
				+ "&CallingClass=" + SMUtilities.getFullClassName(this.toString())
				+ "&" + SMEditDirectOrderDetailEntry.CHOOSE_CATEGORY_METHOD_PARAM + "=" 
					+ clsManageRequestParameters.get_Request_Parameter(SMEditDirectOrderDetailEntry.CHOOSE_CATEGORY_METHOD_PARAM, request)
			;
			redirectProcess(sRedirectString, response);
			return;
    	}
		
    	//If it's a request to create the items and place them on the order:
    	if (clsManageRequestParameters.get_Request_Parameter(
    			SMEditDirectOrderDetailEntry.ADDITEMSTOORDER_BUTTON_NAME, request).compareToIgnoreCase(
    					SMEditDirectOrderDetailEntry.ADDITEMSTOORDER_BUTTON_LABEL) == 0){
    		String sRedirectString = "";

    		try {
    			entry.createItemsAndAddToOrder(smaction.getsDBID(), 
    											getServletContext(), 
    											smaction.getUserName(),
    											smaction.getUserID(),
    											smaction.getFullUserName());
			} catch (Exception e) {
    			smaction.getCurrentSession().setAttribute(SMDirectOrderDetailEntry.ParamObjectName, entry);
    			sRedirectString = SMUtilities.getURLLinkBase(getServletContext())
    					+ "smcontrolpanel.SMEditDirectOrderDetailEntry"
    					+ "?" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + smaction.getsDBID()
    					+ "&CallingClass=" + SMUtilities.getFullClassName(this.toString())
    					+ "&" + SMEditDirectOrderDetailEntry.CHOOSE_CATEGORY_METHOD_PARAM + "=" 
    						+ clsManageRequestParameters.get_Request_Parameter(SMEditDirectOrderDetailEntry.CHOOSE_CATEGORY_METHOD_PARAM, request)
    					+ "&Warning=Error: " + SMUtilities.URLEncode(e.getMessage())
    				;
    			redirectProcess(sRedirectString, response);
    			return;
			}
    		//If it was successful, return to the order details screen:
    		//Now return to the edit screen:
			sRedirectString = SMUtilities.getURLLinkBase(getServletContext())
				+ "smcontrolpanel.SMOrderDetailList"
				+ "?" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + smaction.getsDBID()
				+ "&" + SMTableorderheaders.strimmedordernumber + "=" + entry.getM_ordernumber()
				+ "&CallingClass=" + SMUtilities.getFullClassName(this.toString())
				+ "&" + SMEditDirectOrderDetailEntry.CHOOSE_CATEGORY_METHOD_PARAM + "=" 
					+ clsManageRequestParameters.get_Request_Parameter(SMEditDirectOrderDetailEntry.CHOOSE_CATEGORY_METHOD_PARAM, request)
			;
			redirectProcess(sRedirectString, response);
			return;
    	}
    	
       	//If it's a request to change the category selection method:
    	if (clsManageRequestParameters.get_Request_Parameter(
    			SMEditDirectOrderDetailEntry.CHOOSE_CATEGORY_BY_LINE_BUTTON_NAME, request).compareToIgnoreCase(
    					SMEditDirectOrderDetailEntry.CHOOSE_CATEGORY_BY_LINE_BUTTON_LABEL) == 0){
    		//Save the entry in a session object:
    		smaction.getCurrentSession().setAttribute(SMDirectOrderDetailEntry.ParamObjectName, entry);
    		//Now return to the edit screen:
			String sRedirectString = SMUtilities.getURLLinkBase(getServletContext())
				+ "smcontrolpanel.SMEditDirectOrderDetailEntry"
				+ "?" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + smaction.getsDBID()
				+ "&CallingClass=" + SMUtilities.getFullClassName(this.toString())
				+ "&" + SMEditDirectOrderDetailEntry.CHOOSE_CATEGORY_METHOD_PARAM + "=" + SMEditDirectOrderDetailEntry.CHOOSE_CATEGORY_BY_LINE_BUTTON_LABEL
			;
			redirectProcess(sRedirectString, response);
			return;
    	}
    	
       	//If it's a request to change the category selection method:
    	if (clsManageRequestParameters.get_Request_Parameter(
    			SMEditDirectOrderDetailEntry.CHOOSE_CATEGORY_BY_HEADER_BUTTON_NAME, request).compareToIgnoreCase(
    					SMEditDirectOrderDetailEntry.CHOOSE_CATEGORY_BY_HEADER_BUTTON_LABEL) == 0){
    		//Save the entry in a session object:
    		smaction.getCurrentSession().setAttribute(SMDirectOrderDetailEntry.ParamObjectName, entry);
    		//Now return to the edit screen:
			String sRedirectString = SMUtilities.getURLLinkBase(getServletContext())
				+ "smcontrolpanel.SMEditDirectOrderDetailEntry"
				+ "?" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + smaction.getsDBID()
				+ "&CallingClass=" + SMUtilities.getFullClassName(this.toString())
				+ "&" + SMEditDirectOrderDetailEntry.CHOOSE_CATEGORY_METHOD_PARAM + "=" + SMEditDirectOrderDetailEntry.CHOOSE_CATEGORY_BY_HEADER_BUTTON_LABEL
			;
			redirectProcess(sRedirectString, response);
			return;
    	}
		//********************************
		return;
	}

	private void redirectProcess(String sRedirectString, HttpServletResponse res ){
		try {
			res.sendRedirect(sRedirectString);
		} catch (IOException e1) {
			System.out.println("[1579269690] In " + this.toString() + ".redirectAction - error redirecting with string: "
					+ sRedirectString);
			return;
		}
	}
	public void doGet(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
		
		doPost(request, response);
	}
}