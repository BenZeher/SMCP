package smcontrolpanel;

import java.io.IOException;
import java.sql.Connection;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import SMClasses.FinderResults;
import SMClasses.SMFinderFunctions;
import SMDataDefinition.SMTablesmestimates;
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
					
					/*
					+ "&SearchField1=" + SMTableicitems.sItemDescription
					+ "&SearchFieldAlias1=Description"
					+ "&SearchField2=" + SMTableicitems.sItemNumber
					+ "&SearchFieldAlias2=Item%20No."
					+ "&SearchField3=" + SMTableicitems.sComment1
					+ "&SearchFieldAlias3=Comment%201"
					+ "&SearchField4=" + SMTableicitems.sComment2
					+ "&SearchFieldAlias4=Comment%202"
					+ "&ResultListField1="  + SMTableicitems.sItemNumber
					+ "&ResultHeading1=Item%20No."
					+ "&ResultListField2="  + SMTableicitems.sItemDescription
					+ "&ResultHeading2=Description"
					+ "&ResultListField3="  + SMTableicitems.sCostUnitOfMeasure
					+ "&ResultHeading3=Cost%20Unit"
					+ "&ResultListField4="  + SMTableicitems.inonstockitem
					+ "&ResultHeading4=Non-stock?"
					+ "&ResultListField5="  + SMTableicitems.sPickingSequence
					+ "&ResultHeading5=Picking%20Sequence"				+ "&ParameterString="
					*/
					
					+ "&ParameterString="
					+ "*" + SMTablesmestimates.lid + "=" + estimate.getslid()
					+ "*" + SMEditOrderDetailEdit.RECORDWASCHANGED_FLAG + "=" 
						+ clsManageRequestParameters.get_Request_Parameter(SMEditSMEstimateEdit.RECORDWASCHANGED_FLAG_VALUE, request)

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
			    		);
				return;
			}
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