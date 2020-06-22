package smcontrolpanel;

import java.io.IOException;
import java.sql.Connection;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import SMDataDefinition.SMTablesmestimates;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsManageRequestParameters;
import smcontrolpanel.SMMasterEditAction;
import smcontrolpanel.SMSystemFunctions;

public class SMListCommonEstimateItemsAction extends HttpServlet{
	
	private static final long serialVersionUID = 1L;
	public void doPost(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
	
		SMMasterEditAction smaction = new SMMasterEditAction(request, response);
		if (!smaction.processSession(getServletContext(), SMSystemFunctions.SMEditSMEstimates)){return;}
		
		SMEstimate estimate = new SMEstimate(request);
	    try {
			estimate.load(getServletContext(), smaction.getsDBID(), smaction.getUserID());
		} catch (Exception e1) {
			smaction.getCurrentSession().setAttribute(SMEditSMEstimateEdit.WARNING_OBJECT, e1.getMessage());
			smaction.redirectAction(
				"", 
				"", 
				SMTablesmestimates.lid + "=" + estimate.getslid());
			return;
		}
		
		Connection conn = null;
		try {
			conn = clsDatabaseFunctions.getConnectionWithException(
				getServletContext(), 
				smaction.getsDBID(), 
				"MySQL", 
				this.toString() + ".doPost - user: " + smaction.getFullUserName()
			);
		} catch (Exception e2) {
			smaction.getCurrentSession().setAttribute(SMEditSMEstimateEdit.WARNING_OBJECT, e2.getMessage());
	    	smaction.redirectAction(
		    		"", 
		    		"", 
		    		SMTablesmestimates.lid + "=" + estimate.getslid()
		    		);
			return;
		}
		
		try {
			estimate.add_new_items(conn, smaction.getUserID(), smaction.getFullUserName(), request);
		} catch (Exception e) {
			clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1592842568]");
			smaction.getCurrentSession().setAttribute(SMEditSMEstimateEdit.WARNING_OBJECT, e.getMessage());
			smaction.getCurrentSession().setAttribute(SMEstimate.OBJECT_NAME, estimate);
	    	smaction.redirectAction(
		    		"", 
		    		"", 
		    		SMTablesmestimates.lid + "=" + estimate.getslid()
		    		);
			return;
		}
		
		//Return after adding the items:
		clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1592842768]");
    	smaction.redirectAction(
	    		"", 
	    		"", 
	    		SMTablesmestimates.lid + "=" + estimate.getslid()
	    		);
		return;

	}
	private void redirectProcess(String sRedirectString, HttpServletResponse res ) throws Exception{
		try {
			res.sendRedirect(sRedirectString);
		} catch (IOException e1) {
			throw new Exception("Error [1395236724] in " + this.toString() + ".redirectAction - IOException error redirecting with string: "
					+ sRedirectString + " - " + e1.getMessage());
		} catch (IllegalStateException e1) {
			throw new Exception("Error [1395236725] in " + this.toString() + ".redirectAction - IllegalStateException error redirecting with string: "
					+ sRedirectString + " - " + e1.getMessage());
		}
	}
	public void doGet(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
		
		doPost(request, response);
	}
}