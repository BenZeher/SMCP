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
		smaction.getCurrentSession().removeAttribute(SMIncorporateSummary.ParamObjectName);
		
		if (bDebugMode){
			System.out.println("[1579269679] In " + this.toString() + " - PRINTING PARAMS\n");
			PrintWriter debugout = new PrintWriter(System.out);
			clsManageRequestParameters.printRequestParameters(debugout, request);
		}
		//Read the entry fields from the request:
		SMIncorporateSummary entry = new SMIncorporateSummary(request);

		if (bDebugMode){
			//PrintWriter debugout = new PrintWriter(System.out);
			System.out.println("[1579269684] In " + this.toString() + " - entry= \n" + entry.read_out_debug_data());
			//SMUtilities.printRequestParameters(debugout, request);
		}
		
		//Special cases:
		//********************************
    	
		/*
       	//If it's a request to calculate the billing values:
    	if (clsManageRequestParameters.get_Request_Parameter(
    			SMIncorporateSummaryEdit.CALCULATE_BUTTON_NAME, request).compareToIgnoreCase(
    					SMIncorporateSummaryEdit.CALCULATE_BUTTON_LABEL) == 0){
    		String sRedirectString = "";

    		try {
				entry.calculateBillingValues(getServletContext(), 
											smaction.getsDBID(), 
											smaction.getUserName(),
											smaction.getUserID(),
											smaction.getFullUserName()
						);
			} catch (Exception e) {
    			smaction.getCurrentSession().setAttribute(SMIncorporateSummary.ParamObjectName, entry);
    			sRedirectString = SMUtilities.getURLLinkBase(getServletContext())
    					+ "smcontrolpanel.SMIncorporateSummaryEdit"
    					+ "?" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + smaction.getsDBID()
    					+ "&CallingClass=" + SMUtilities.getFullClassName(this.toString())
    					+ "&" + SMIncorporateSummaryEdit.CHOOSE_CATEGORY_METHOD_PARAM + "=" 
    						+ clsManageRequestParameters.get_Request_Parameter(SMIncorporateSummaryEdit.CHOOSE_CATEGORY_METHOD_PARAM, request)
    					+ "&Warning=Error: " + clsStringFunctions.filter(e.getMessage())
    				;
    			redirectProcess(sRedirectString, response);
    			return;
			}
    		//Save the entry in a session object:
    		smaction.getCurrentSession().setAttribute(SMIncorporateSummary.ParamObjectName, entry);
    		//Now return to the edit screen:
			sRedirectString = SMUtilities.getURLLinkBase(getServletContext())
				+ "smcontrolpanel.SMIncorporateSummaryEdit"
				+ "?" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + smaction.getsDBID()
				+ "&CallingClass=" + SMUtilities.getFullClassName(this.toString())
				+ "&" + SMIncorporateSummaryEdit.CHOOSE_CATEGORY_METHOD_PARAM + "=" 
					+ clsManageRequestParameters.get_Request_Parameter(SMIncorporateSummaryEdit.CHOOSE_CATEGORY_METHOD_PARAM, request)
			;
			redirectProcess(sRedirectString, response);
			return;
    	}
		*/
		
    	//If it's a request to create the items and place them on the order:
    	if (clsManageRequestParameters.get_Request_Parameter(
    			SMIncorporateSummaryEdit.ADDITEMSTOORDER_BUTTON_NAME, request).compareToIgnoreCase(
    					SMIncorporateSummaryEdit.ADDITEMSTOORDER_BUTTON_LABEL) == 0){
    		String sRedirectString = "";

			sRedirectString = SMUtilities.getURLLinkBase(getServletContext())
					+ "smcontrolpanel.SMIncorporateSummaryEdit"
					+ "?" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + smaction.getsDBID()
					+ "&CallingClass=" + SMUtilities.getFullClassName(this.toString())
					+ "&" + SMIncorporateSummaryEdit.CHOOSE_CATEGORY_METHOD_PARAM + "=" 
						+ clsManageRequestParameters.get_Request_Parameter(SMIncorporateSummaryEdit.CHOOSE_CATEGORY_METHOD_PARAM, request)
					+ "&Warning=Error: " + SMUtilities.URLEncode("This function isn't working yet.")
				;
			redirectProcess(sRedirectString, response);
			return;
    		/*
    		try {
    			entry.createItemsAndAddToOrder(smaction.getsDBID(), 
    											getServletContext(), 
    											smaction.getUserName(),
    											smaction.getUserID(),
    											smaction.getFullUserName());
			} catch (Exception e) {
    			smaction.getCurrentSession().setAttribute(SMIncorporateSummary.ParamObjectName, entry);
    			sRedirectString = SMUtilities.getURLLinkBase(getServletContext())
    					+ "smcontrolpanel.SMIncorporateSummaryEdit"
    					+ "?" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + smaction.getsDBID()
    					+ "&CallingClass=" + SMUtilities.getFullClassName(this.toString())
    					+ "&" + SMIncorporateSummaryEdit.CHOOSE_CATEGORY_METHOD_PARAM + "=" 
    						+ clsManageRequestParameters.get_Request_Parameter(SMIncorporateSummaryEdit.CHOOSE_CATEGORY_METHOD_PARAM, request)
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
				+ "&" + SMIncorporateSummaryEdit.CHOOSE_CATEGORY_METHOD_PARAM + "=" 
					+ clsManageRequestParameters.get_Request_Parameter(SMIncorporateSummaryEdit.CHOOSE_CATEGORY_METHOD_PARAM, request)
			;
			redirectProcess(sRedirectString, response);
			return;
			*/
    	}
    	
       	//If it's a request to change the category selection method:
    	if (clsManageRequestParameters.get_Request_Parameter(
    			SMIncorporateSummaryEdit.CHOOSE_CATEGORY_BY_LINE_BUTTON_NAME, request).compareToIgnoreCase(
    					SMIncorporateSummaryEdit.CHOOSE_CATEGORY_BY_LINE_BUTTON_LABEL) == 0){
    		//Save the entry in a session object:
    		smaction.getCurrentSession().setAttribute(SMIncorporateSummary.ParamObjectName, entry);
    		//Now return to the edit screen:
			String sRedirectString = SMUtilities.getURLLinkBase(getServletContext())
				+ "smcontrolpanel.SMIncorporateSummaryEdit"
				+ "?" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + smaction.getsDBID()
				+ "&CallingClass=" + SMUtilities.getFullClassName(this.toString())
				+ "&" + SMIncorporateSummaryEdit.CHOOSE_CATEGORY_METHOD_PARAM + "=" + SMIncorporateSummaryEdit.CHOOSE_CATEGORY_BY_LINE_BUTTON_LABEL
			;
			redirectProcess(sRedirectString, response);
			return;
    	}
    	
       	//If it's a request to change the category selection method:
    	if (clsManageRequestParameters.get_Request_Parameter(
    			SMIncorporateSummaryEdit.CHOOSE_CATEGORY_BY_HEADER_BUTTON_NAME, request).compareToIgnoreCase(
    					SMIncorporateSummaryEdit.CHOOSE_CATEGORY_BY_HEADER_BUTTON_LABEL) == 0){
    		//Save the entry in a session object:
    		smaction.getCurrentSession().setAttribute(SMIncorporateSummary.ParamObjectName, entry);
    		//Now return to the edit screen:
			String sRedirectString = SMUtilities.getURLLinkBase(getServletContext())
				+ "smcontrolpanel.SMIncorporateSummaryEdit"
				+ "?" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + smaction.getsDBID()
				+ "&CallingClass=" + SMUtilities.getFullClassName(this.toString())
				+ "&" + SMIncorporateSummaryEdit.CHOOSE_CATEGORY_METHOD_PARAM + "=" + SMIncorporateSummaryEdit.CHOOSE_CATEGORY_BY_HEADER_BUTTON_LABEL
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