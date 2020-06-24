package smcontrolpanel;

import java.io.IOException;
import java.sql.Connection;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import SMClasses.FinderResults;
import SMClasses.OHDirectFinderResults;
import SMClasses.SMFinderFunctions;
import SMDataDefinition.SMOHDirectFieldDefinitions;
import SMDataDefinition.SMTablesmestimates;
import SMDataDefinition.SMTablesmestimatesummaries;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsManageRequestParameters;
import smcontrolpanel.SMMasterEditAction;
import smcontrolpanel.SMSystemFunctions;

public class SMEditSMEstimateAction extends HttpServlet{
	
	private static final long serialVersionUID = 1L;
	public void doPost(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
	
		SMMasterEditAction smaction = new SMMasterEditAction(request, response);
		if (!smaction.processSession(getServletContext(), SMSystemFunctions.SMEditSMEstimates)){return;}
		
		//If there's any estimate object in the session, dump it:
		try {
			smaction.getCurrentSession().removeAttribute(SMEstimate.OBJECT_NAME);
		} catch (Exception e1) {
			//Don't choke on this...
		}
	    //Read the entry fields from the request object:
		SMEstimate estimate = new SMEstimate(request);
		
		//Get the command value from the request.
		String sCommandValue = clsManageRequestParameters.get_Request_Parameter(SMEditSMEstimateEdit.COMMAND_FLAG, request);

		Connection conn = null;
		try {
			conn = clsDatabaseFunctions.getConnectionWithException(
				getServletContext(), 
				smaction.getsDBID(), 
				"MySQL", 
				this.toString() + ".doPost - user: " + smaction.getFullUserName()
			);
		} catch (Exception e2) {
			smaction.getCurrentSession().setAttribute(SMEstimate.OBJECT_NAME, estimate);
			smaction.getCurrentSession().setAttribute(SMEditSMEstimateEdit.WARNING_OBJECT, e2.getMessage());
	    	smaction.redirectAction(
		    		"", 
		    		"", 
		    		SMTablesmestimates.lid + "=" + estimate.getslid()
		    		);
			return;
		}
		
		//If SAVE, then save the estimate:
		if(sCommandValue.compareToIgnoreCase(SMEditSMEstimateEdit.SAVE_COMMAND_VALUE) == 0){
			try {
				estimate.save_without_data_transaction(conn, smaction.getUserID(), smaction.getFullUserName());
			} catch (Exception e) {
				clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1591633340]");
				smaction.getCurrentSession().setAttribute(SMEditSMEstimateEdit.WARNING_OBJECT, e.getMessage());
				smaction.getCurrentSession().setAttribute(SMEstimate.OBJECT_NAME, estimate);
		    	smaction.redirectAction(
			    		"", 
			    		"", 
			    		SMTablesmestimates.lid + "=" + estimate.getslid()
			    		);
				return;
			}
			clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1591633341]");
			smaction.getCurrentSession().removeAttribute(SMEstimate.OBJECT_NAME);
			smaction.getCurrentSession().setAttribute(SMEditSMEstimateEdit.RESULT_STATUS_OBJECT, "Estimate #" + estimate.getslid() + " saved successfully");
	    	smaction.redirectAction(
		    		"", 
		    		"", 
		    		SMTablesmestimates.lid + "=" + estimate.getslid()
		    		);
			return;
		}
		
		//If LOOK UP ITEM data, process that:
		if(sCommandValue.compareToIgnoreCase(SMEditSMEstimateEdit.LOOKUP_ITEM_COMMAND) == 0){
			String sLineNumber = clsManageRequestParameters.get_Request_Parameter(SMEditSMEstimateEdit.PARAM_LOOKUP_ITEM_LINENUMBER, request).trim();
			try {
				estimate.lookUpItem(sLineNumber, conn);
			} catch (Exception e) {
				clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1591633390]");
				smaction.getCurrentSession().setAttribute(SMEditSMEstimateEdit.WARNING_OBJECT, e.getMessage());
				smaction.getCurrentSession().setAttribute(SMEstimate.OBJECT_NAME, estimate);
		    	smaction.redirectAction(
			    		"", 
			    		"", 
			    		SMTablesmestimates.lid + "=" + estimate.getslid()
			    		+ "&" + SMEditOrderDetailEdit.RECORDWASCHANGED_FLAG + "=" 
							+ clsManageRequestParameters.get_Request_Parameter(SMEditSMEstimateEdit.RECORDWASCHANGED_FLAG_VALUE, request)
			    		);
				return;
			}
			clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1591633391]");
			smaction.getCurrentSession().setAttribute(SMEstimate.OBJECT_NAME, estimate);
	    	smaction.redirectAction(
		    		"", 
		    		"", 
		    		SMTablesmestimates.lid + "=" + estimate.getslid()
		    		+ "&" + SMEditOrderDetailEdit.RECORDWASCHANGED_FLAG + "=" 
						+ clsManageRequestParameters.get_Request_Parameter(SMEditSMEstimateEdit.RECORDWASCHANGED_FLAG_VALUE, request)
		    		);
			return;
		}
		
		//If REFRESH ITEM, process that:
		if(sCommandValue.compareToIgnoreCase(SMEditSMEstimateEdit.REFRESH_ITEM_COMMAND) == 0){
			String sLineNumber = clsManageRequestParameters.get_Request_Parameter(SMEditSMEstimateEdit.REFRESH_ITEM_LINE_NUMBER, request).trim();
			try {
				estimate.refreshItem(sLineNumber, conn);
			} catch (Exception e) {
				clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1591633330]");
				smaction.getCurrentSession().setAttribute(SMEditSMEstimateEdit.WARNING_OBJECT, e.getMessage());
				smaction.getCurrentSession().setAttribute(SMEstimate.OBJECT_NAME, estimate);
		    	smaction.redirectAction(
			    		"", 
			    		"", 
			    		SMTablesmestimates.lid + "=" + estimate.getslid()
			    		+ "&" + SMEditOrderDetailEdit.RECORDWASCHANGED_FLAG + "=" 
							+ clsManageRequestParameters.get_Request_Parameter(SMEditSMEstimateEdit.RECORDWASCHANGED_FLAG_VALUE, request)
			    		);
				return;
			}
			clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1591633331]");
			smaction.getCurrentSession().setAttribute(SMEstimate.OBJECT_NAME, estimate);
	    	smaction.redirectAction(
		    		"", 
		    		"", 
		    		SMTablesmestimates.lid + "=" + estimate.getslid()
		    		+ "&" + SMEditOrderDetailEdit.RECORDWASCHANGED_FLAG + "=" 
						+ clsManageRequestParameters.get_Request_Parameter(SMEditSMEstimateEdit.RECORDWASCHANGED_FLAG_VALUE, request)
		    		);
			return;
		}
		
		//If FIND ITEM, process that:
		if(sCommandValue.compareToIgnoreCase(SMEditSMEstimateEdit.FIND_ITEM_COMMAND) == 0){
			String sReturnField = clsManageRequestParameters.get_Request_Parameter(SMEditSMEstimateEdit.PARAM_FIND_ITEM_RETURN_FIELD, request).trim();
			
			String sRedirectString = 
					"" + SMUtilities.getURLLinkBase(getServletContext()) + "SMClasses.ObjectFinder"
					+ "?" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + smaction.getsDBID()
					+ "&ObjectName=" + FinderResults.SEARCH_NONDEDICATEDITEMS
					+ "&ResultClass=FinderResults"
					+ "&SearchingClass=" + smaction.getCallingClass()
					+ "&ReturnField=" + sReturnField
					+ SMFinderFunctions.getStdITEMSearchAndResultString()
					+ "&ParameterString="
					+ "*" + SMTablesmestimates.lid + "=" + estimate.getslid()
					+ "*" + SMEditOrderDetailEdit.RECORDWASCHANGED_FLAG + "=" 
						+ clsManageRequestParameters.get_Request_Parameter(SMEditSMEstimateEdit.RECORDWASCHANGED_FLAG_VALUE, request)
					+ "*" + SMEditSMEstimateEdit.RETURNING_FROM_FINDER + "=T"

					+ "*CallingClass=" + smaction.getCallingClass()
			;

    		clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1591633740]");
			smaction.getCurrentSession().setAttribute(SMEstimate.OBJECT_NAME, estimate);
			try {
				redirectProcess(sRedirectString, response);
			} catch (Exception e) {
				clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1591633440]");
				smaction.getCurrentSession().setAttribute(SMEditSMEstimateEdit.WARNING_OBJECT, e.getMessage());
				smaction.getCurrentSession().setAttribute(SMEstimate.OBJECT_NAME, estimate);
		    	smaction.redirectAction(
			    		"", 
			    		"", 
			    		SMTablesmestimates.lid + "=" + estimate.getslid()		    		
			    			+ "&" + SMEditOrderDetailEdit.RECORDWASCHANGED_FLAG + "=" 
							+ clsManageRequestParameters.get_Request_Parameter(SMEditSMEstimateEdit.RECORDWASCHANGED_FLAG_VALUE, request)
			    		);
				return;
			}
		}
		
		//If REFRESH VENDOR QUOTE, process that:
		//System.out.println("[202006124811] - command was: '" + sCommandValue + "'.");
		if(sCommandValue.compareToIgnoreCase(SMEditSMEstimateEdit.REFRESH_VENDOR_QUOTE_COMMAND) == 0){
			String sResult = "";
			try {
				sResult = estimate.refreshVendorQuoteLine(conn, smaction.getsDBID(), smaction.getUserID(), smaction.getFullUserName());
			} catch (Exception e) {
				clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1591633340]");
				smaction.getCurrentSession().setAttribute(SMEditSMEstimateEdit.WARNING_OBJECT, e.getMessage());
				smaction.getCurrentSession().setAttribute(SMEstimate.OBJECT_NAME, estimate);
		    	smaction.redirectAction(
			    		"", 
			    		"", 
			    		SMTablesmestimates.lid + "=" + estimate.getslid()
			    		+ "&" + SMEditOrderDetailEdit.RECORDWASCHANGED_FLAG + "=" 
							+ clsManageRequestParameters.get_Request_Parameter(SMEditSMEstimateEdit.RECORDWASCHANGED_FLAG_VALUE, request)
			    		);
				return;
			}
			clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1591633341]");
			smaction.getCurrentSession().setAttribute(SMEditSMEstimateEdit.RESULT_STATUS_OBJECT, sResult);
			//System.out.println("[202006102717] - estimate dump = \n" + estimate.dumpData());
	    	smaction.redirectAction(
		    		"", 
		    		"", 
		    		SMTablesmestimates.lid + "=" + estimate.getslid()
		    		+ "&" + SMEditOrderDetailEdit.RECORDWASCHANGED_FLAG + "=" 
						+ clsManageRequestParameters.get_Request_Parameter(SMEditSMEstimateEdit.RECORDWASCHANGED_FLAG_VALUE, request)
		    		);
			return;
		}
		
		//If REPLACE VENDOR QUOTE, process that:
		//System.out.println("[202006124811] - command was: '" + sCommandValue + "'.");
		if(sCommandValue.compareToIgnoreCase(SMEditSMEstimateEdit.REPLACE_VENDOR_QUOTE_COMMAND) == 0){
			String sNewVendorQuoteNumber = clsManageRequestParameters.get_Request_Parameter(SMEditSMEstimateEdit.FIELD_REPLACE_QUOTE_WITH_NUMBER, request).trim();
			String sNewVendorQuoteLineNumber = clsManageRequestParameters.get_Request_Parameter(SMEditSMEstimateEdit.FIELD_REPLACE_QUOTE_LINE, request).trim();
			String sResult = "";
			try {
				sResult = estimate.replaceVendorQuoteLine(
					conn, 
					smaction.getsDBID(), 
					smaction.getUserID(), 
					smaction.getFullUserName(), 
					sNewVendorQuoteNumber, 
					sNewVendorQuoteLineNumber
				);
			} catch (Exception e) {
				clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1591633640]");
				smaction.getCurrentSession().setAttribute(SMEditSMEstimateEdit.WARNING_OBJECT, e.getMessage());
				smaction.getCurrentSession().setAttribute(SMEstimate.OBJECT_NAME, estimate);
		    	smaction.redirectAction(
			    		"", 
			    		"", 
			    		SMTablesmestimates.lid + "=" + estimate.getslid()
			    		+ "&" + SMEditOrderDetailEdit.RECORDWASCHANGED_FLAG + "=" 
							+ clsManageRequestParameters.get_Request_Parameter(SMEditSMEstimateEdit.RECORDWASCHANGED_FLAG_VALUE, request)
			    		);
				return;
			}
			clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1591633641]");
			smaction.getCurrentSession().setAttribute(SMEditSMEstimateEdit.RESULT_STATUS_OBJECT, sResult);
			//System.out.println("[202006102717] - estimate dump = \n" + estimate.dumpData());
	    	smaction.redirectAction(
		    		"", 
		    		"", 
		    		SMTablesmestimates.lid + "=" + estimate.getslid()
		    		+ "&" + SMEditOrderDetailEdit.RECORDWASCHANGED_FLAG + "=" 
						+ clsManageRequestParameters.get_Request_Parameter(SMEditSMEstimateEdit.RECORDWASCHANGED_FLAG_VALUE, request)
		    		);
			return;
		}
		
		//If FIND VENDOR QUOTE:
		if(sCommandValue.compareToIgnoreCase(SMEditSMEstimateEdit.FIND_VENDOR_QUOTE_COMMAND_VALUE) == 0){
			clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1590773259]");
    		String sRedirectString = 
				"" + SMUtilities.getURLLinkBase(getServletContext()) + "SMClasses.OHDirectFinder"
				+ "?" + "EndpointName=" + SMOHDirectFieldDefinitions.ENDPOINT_QUOTE
				+ "&" + OHDirectFinderResults.FINDER_LIST_FORMAT_PARAM + "=" + OHDirectFinderResults.FINDER_LIST_FORMAT_QUOTE_LINES_VALUE
				+ "&SearchingClass=" + "smcontrolpanel.SMEditSMEstimateEdit"
				+ "&ReturnField=" + SMEditSMEstimateEdit.FIELD_REPLACE_QUOTE_WITH_NUMBER
				+ "&ResultListField1=" + SMOHDirectFieldDefinitions.QUOTE_FIELD_QUOTENUMBER
				+ "&ResultListField2=" + SMOHDirectFieldDefinitions.QUOTE_FIELD_NAME
				+ "&ResultListField3=" + SMOHDirectFieldDefinitions.QUOTE_FIELD_CREATEDDATE
				+ "&ResultListField4=" + SMOHDirectFieldDefinitions.QUOTE_FIELD_LASTMODIFIEDDATE
				+ "&ResultHeading1=Quote%20Number"
				+ "&ResultHeading2=Job%20Name"
				+ "&ResultHeading3=Created%20Date"
				+ "&ResultHeading4=Last%20Modified%20Date"
				+ "&" + OHDirectFinderResults.FINDER_RETURN_PARAM + "="
					+ SMTablesmestimates.lid + "=" + estimate.getslid()
    		;
	    				
			try {
				redirectProcess(sRedirectString, response);
			} catch (Exception e) {
				smaction.getCurrentSession().setAttribute(SMEditSMEstimateEdit.WARNING_OBJECT, e.getMessage());
				smaction.getCurrentSession().setAttribute(SMEstimate.OBJECT_NAME, estimate);
		    	smaction.redirectAction(
			    		"", 
			    		"", 
			    		SMTablesmestimates.lid + "=" + estimate.getslid()
			    		);
				return;
			}
			return;
		}
		
		//If display common items:
		if(sCommandValue.compareToIgnoreCase(SMEditSMEstimateEdit.DISPLAY_COMMONLY_USED_ITEMS_COMMAND) == 0){
			clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1590773259]");
			smaction.getCurrentSession().removeAttribute(SMEstimate.OBJECT_NAME);
    		String sRedirectString = 
				"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMListCommonEstimateItems"
				+ "?" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + smaction.getsDBID()
				+ "&" + SMTablesmestimates.lid + "=" + estimate.getslid()
				+ "&CallingClass=" + "smcontrolpanel.SMEditSMEstimateEdit"
    		;
	    				
			try {
				redirectProcess(sRedirectString, response);
			} catch (Exception e) {
				smaction.getCurrentSession().setAttribute(SMEditSMEstimateEdit.WARNING_OBJECT, e.getMessage());
				smaction.getCurrentSession().setAttribute(SMEstimate.OBJECT_NAME, estimate);
		    	smaction.redirectAction(
			    		"", 
			    		"", 
			    		SMTablesmestimates.lid + "=" + estimate.getslid()
			    		);
				return;
			}
			return;
		}
		
		return;
	}
	private void redirectProcess(String sRedirectString, HttpServletResponse res ) throws Exception{
		try {
			res.sendRedirect(sRedirectString);
		} catch (IOException e1) {
			throw new Exception("Error [1395236124] in " + this.toString() + ".redirectAction - IOException error redirecting with string: "
					+ sRedirectString + " - " + e1.getMessage());
		} catch (IllegalStateException e1) {
			throw new Exception("Error [1395236125] in " + this.toString() + ".redirectAction - IllegalStateException error redirecting with string: "
					+ sRedirectString + " - " + e1.getMessage());
		}
	}
	public void doGet(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
		
		doPost(request, response);
	}
}