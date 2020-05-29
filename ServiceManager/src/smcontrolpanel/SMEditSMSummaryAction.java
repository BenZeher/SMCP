package smcontrolpanel;

import java.io.IOException;
import java.sql.Connection;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
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
	}
	
	public void doGet(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
		
		doPost(request, response);
	}
}