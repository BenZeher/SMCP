package smcontrolpanel;

import java.io.IOException;
import java.sql.Connection;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import SMClasses.OHDirectFinderResults;
import SMDataDefinition.SMOHDirectFieldDefinitions;
import SMDataDefinition.SMTablesmestimates;
import SMDataDefinition.SMTablesmestimatesummaries;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsManageRequestParameters;
import smcontrolpanel.SMMasterEditAction;
import smcontrolpanel.SMSystemFunctions;

public class SMEditSMSummaryAction extends HttpServlet{
	
	private static final long serialVersionUID = 1L;
	public void doPost(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
	
		SMMasterEditAction smaction = new SMMasterEditAction(request, response);
		if (!smaction.processSession(getServletContext(), SMSystemFunctions.SMEditSMEstimates)){return;}
		
		//If there's any summary object in the session, dump it:
		try {
			smaction.getCurrentSession().removeAttribute(SMEstimateSummary.OBJECT_NAME);
		} catch (Exception e1) {
			//Don't choke on this...
		}
	    //Read the entry fields from the request object:
		SMEstimateSummary summary = new SMEstimateSummary(request);
		
		//Make sure to load the estimates as well:
		try {
			summary.loadEstimates(summary.getslid(), smaction.getsDBID(), getServletContext(), smaction.getFullUserName());
		} catch (Exception e1) {
			smaction.getCurrentSession().setAttribute(SMEstimateSummary.OBJECT_NAME, summary);
			smaction.getCurrentSession().setAttribute(SMEditSMSummaryEdit.WARNING_OBJECT, e1.getMessage());
	    	smaction.redirectAction(
		    		"", 
		    		"", 
		    		SMTablesmestimatesummaries.lid + "=" + summary.getslid()
		    		);
			return;
		}
		
		//Get the command value from the request.
		String sCommandValue = clsManageRequestParameters.get_Request_Parameter(SMEditSMSummaryEdit.COMMAND_FLAG, request);

		Connection conn = null;
		try {
			conn = clsDatabaseFunctions.getConnectionWithException(
				getServletContext(), 
				smaction.getsDBID(), 
				"MySQL", 
				this.toString() + ".doPost - user: " + smaction.getFullUserName()
			);
		} catch (Exception e2) {
			smaction.getCurrentSession().setAttribute(SMEstimateSummary.OBJECT_NAME, summary);
			smaction.getCurrentSession().setAttribute(SMEditSMSummaryEdit.WARNING_OBJECT, e2.getMessage());
	    	smaction.redirectAction(
		    		"", 
		    		"", 
		    		SMTablesmestimatesummaries.lid + "=" + summary.getslid()
		    		);
			return;
		}
		
		//If DELETE button, process that:
		if(sCommandValue.compareToIgnoreCase(SMEditSMSummaryEdit.DELETE_COMMAND_VALUE) == 0){
			try {
				summary.deleteSummary(conn);
			} catch (Exception e) {
				clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1590689958]");
				smaction.getCurrentSession().setAttribute(SMEstimateSummary.OBJECT_NAME, summary);
				smaction.getCurrentSession().setAttribute(SMEditSMSummaryEdit.WARNING_OBJECT, e.getMessage());
		    	smaction.redirectAction(
			    		"", 
			    		"", 
			    		SMTablesmestimatesummaries.lid + "=" + summary.getslid()
			    		);
				return;
			}
			
			//If it deletes successfully:
			clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1590689571]");
			smaction.getCurrentSession().removeAttribute(SMEstimateSummary.OBJECT_NAME);
			smaction.getCurrentSession().setAttribute(SMEditSMSummaryEdit.RESULT_STATUS_OBJECT, "Estimate Summary #" + summary.getslid() + " deleted successfully");
	    	smaction.redirectAction(
		    		"", 
		    		"", 
		    		SMTablesmestimatesummaries.lid + "=" + summary.getslid()
		    		);
			return;
		}
		
		//If SAVE, then save the summary:
		if(sCommandValue.compareToIgnoreCase(SMEditSMSummaryEdit.SAVE_COMMAND_VALUE) == 0){
			try {
				summary.save_without_data_transaction(conn, smaction.getUserID(), smaction.getFullUserName());
			} catch (Exception e) {
				clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1590689471]");
				smaction.getCurrentSession().setAttribute(SMEditSMSummaryEdit.WARNING_OBJECT, e.getMessage());
				smaction.getCurrentSession().setAttribute(SMEstimateSummary.OBJECT_NAME, summary);
		    	smaction.redirectAction(
			    		"", 
			    		"", 
			    		SMTablesmestimatesummaries.lid + "=" + summary.getslid()
			    		);
				return;
			}
			clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1590689771]");
			smaction.getCurrentSession().removeAttribute(SMEstimateSummary.OBJECT_NAME);
			smaction.getCurrentSession().setAttribute(SMEditSMSummaryEdit.RESULT_STATUS_OBJECT, "Estimate Summary #" + summary.getslid() + " saved successfully");
	    	smaction.redirectAction(
		    		"", 
		    		"", 
		    		SMTablesmestimatesummaries.lid + "=" + summary.getslid()
		    		);
			return;
		}
		
		//If REMOVE ESTIMATE, then:
		if(sCommandValue.compareToIgnoreCase(SMEditSMSummaryEdit.REMOVE_ESTIMATE_COMMAND) == 0){
			String sSummaryLineNumber = clsManageRequestParameters.get_Request_Parameter(
				SMEditSMSummaryEdit.PARAM_SUMMARY_LINE_NUMBER_TO_BE_REMOVED, request);
			try {
				summary.deleteEstimateByLineNumber(conn, sSummaryLineNumber, summary.getslid(), smaction.getUserID(), smaction.getFullUserName());
			} catch (Exception e) {
				clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1590773159]");
				smaction.getCurrentSession().setAttribute(SMEditSMSummaryEdit.WARNING_OBJECT, e.getMessage());
				smaction.getCurrentSession().setAttribute(SMEstimateSummary.OBJECT_NAME, summary);
		    	smaction.redirectAction(
			    		"", 
			    		"", 
			    		SMTablesmestimatesummaries.lid + "=" + summary.getslid()
			    		);
				return;
			}
			clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1590689771]");
			smaction.getCurrentSession().removeAttribute(SMEstimateSummary.OBJECT_NAME);
			smaction.getCurrentSession().setAttribute(SMEditSMSummaryEdit.RESULT_STATUS_OBJECT, "Estimate on Summary line number " + sSummaryLineNumber + " removed successfully");
	    	smaction.redirectAction(
		    		"", 
		    		"", 
		    		SMTablesmestimatesummaries.lid + "=" + summary.getslid()
		    		);
			return;
		}
		
		//If FIND VENDOR QUOTE, process that:
		if(sCommandValue.compareToIgnoreCase(SMEditSMSummaryEdit.FIND_VENDOR_QUOTE_COMMAND_VALUE) == 0){
			clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1590773659]");
    		String sRedirectString = 
				"" + SMUtilities.getURLLinkBase(getServletContext()) + "SMClasses.OHDirectFinder"
				+ "?" + "EndpointName=" + SMOHDirectFieldDefinitions.ENDPOINT_QUOTE
				+ "&SearchingClass=" + "smcontrolpanel.SMEditSMSummaryEdit"
				+ "&ReturnField=" + SMEditSMSummaryEdit.FIELD_VENDOR_QUOTE
				+ "&ResultListField1=" + SMOHDirectFieldDefinitions.QUOTE_FIELD_QUOTENUMBER
				+ "&ResultListField2=" + SMOHDirectFieldDefinitions.QUOTE_FIELD_NAME
				+ "&ResultListField3=" + SMOHDirectFieldDefinitions.QUOTE_FIELD_CREATEDDATE
				+ "&ResultListField4=" + SMOHDirectFieldDefinitions.QUOTE_FIELD_LASTMODIFIEDDATE
				+ "&ResultHeading1=Quote%20Number"
				+ "&ResultHeading2=Job%20Name"
				+ "&ResultHeading3=Created%20Date"
				+ "&ResultHeading4=Last%20Modified%20Date"
				+ "&" + OHDirectFinderResults.FINDER_RETURN_PARAM + "="
					+ SMTablesmestimatesummaries.lid + "=" + summary.getslid()
    		;
	    				
			try {
				redirectProcess(sRedirectString, response);
			} catch (Exception e) {
				smaction.getCurrentSession().setAttribute(SMEditSMSummaryEdit.WARNING_OBJECT, e.getMessage());
				smaction.getCurrentSession().setAttribute(SMEstimateSummary.OBJECT_NAME, summary);
		    	smaction.redirectAction(
			    		"", 
			    		"", 
			    		SMTablesmestimatesummaries.lid + "=" + summary.getslid()
			    		);
				return;
			}
			return;
		}
		
		//If ADD NEW ESTIMATE, then:
		if(sCommandValue.compareToIgnoreCase(SMEditSMSummaryEdit.ADD_MANUAL_ESTIMATE_COMMAND) == 0){
			clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1590773619]");
    		String sRedirectString = 
				"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMEditSMEstimateEdit"
				+ "?" + SMTablesmestimates.lid + "=-1"
				+ "&" + SMTablesmestimates.lsummarylid + "=" + summary.getslid()
				+ "&" + SMMasterEditSelect.SUBMIT_ADD_BUTTON_NAME + "=Y"
				+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + smaction.getsDBID()
				+ "&" + "CallingClass=" + "smcontrolpanel.SMEditSMSummaryEdit"
				;
	    	//System.out.println("[202006044427] - sRedirectString = '" + sRedirectString + "'");			
			try {
				redirectProcess(sRedirectString, response);
			} catch (Exception e) {
				smaction.getCurrentSession().setAttribute(SMEditSMSummaryEdit.WARNING_OBJECT, e.getMessage());
				smaction.getCurrentSession().setAttribute(SMEstimateSummary.OBJECT_NAME, summary);
		    	smaction.redirectAction(
			    		"", 
			    		"", 
			    		SMTablesmestimatesummaries.lid + "=" + summary.getslid()
			    		);
				return;
			}
			return;
		}
		
		//If ADD VENDOR QUOTE, then:
		if(sCommandValue.compareToIgnoreCase(SMEditSMSummaryEdit.ADD_VENDOR_QUOTE_COMMAND) == 0){
			String sVendorQuoteNumber = clsManageRequestParameters.get_Request_Parameter(
					SMEditSMSummaryEdit.FIELD_VENDOR_QUOTE, request);
			try {
				summary.createEstimatesFromVendorQuote(
						conn, 
						sVendorQuoteNumber, 
						smaction.getsDBID(), 
						smaction.getUserID(), 
						smaction.getFullUserName(), 
						getServletContext()
				);
			} catch (Exception e) {
				clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1590773959]");
				smaction.getCurrentSession().setAttribute(SMEditSMSummaryEdit.WARNING_OBJECT, e.getMessage());
				smaction.getCurrentSession().setAttribute(SMEstimateSummary.OBJECT_NAME, summary);
		    	smaction.redirectAction(
			    		"", 
			    		"", 
			    		SMTablesmestimatesummaries.lid + "=" + summary.getslid()
			    		);
				return;
			}
			clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1590689971]");
			smaction.getCurrentSession().removeAttribute(SMEstimateSummary.OBJECT_NAME);
			smaction.getCurrentSession().setAttribute(SMEditSMSummaryEdit.RESULT_STATUS_OBJECT, "Estimates added from vendor quote number " 
					+ sVendorQuoteNumber + " added successfully");
	    	smaction.redirectAction(
		    		"", 
		    		"", 
		    		SMTablesmestimatesummaries.lid + "=" + summary.getslid()
		    		);
			return;
		}
		
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