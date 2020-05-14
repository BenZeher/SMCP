package smgl;

import java.io.IOException;
import java.sql.Connection;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import smcontrolpanel.SMMasterEditAction;
import smcontrolpanel.SMSystemFunctions;

public class GLDuplicateExternalBatchAction extends HttpServlet{
	
	private static final long serialVersionUID = 1L;
	public void doPost(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
	
		SMMasterEditAction smaction = new SMMasterEditAction(request, response);
		try {
			smaction.getCurrentSession().removeAttribute(GLDuplicateExternalBatchSelect.SESSION_WARNING_OBJECT);
		} catch (Exception e2) {
			//If this attribute isn't in the session, just go on without disruption....
		}
		if (!smaction.processSession(getServletContext(), SMSystemFunctions.GLDuplicateExternalCompanyBatch)){return;}
	    //Read the entry fields from the request object:
	    String sExternalCompanyID = ServletUtilities.clsManageRequestParameters.get_Request_Parameter(GLDuplicateExternalBatchSelect.RADIO_BUTTONS_NAME, request);
	    String sBatchNumber = request.getParameter(GLDuplicateExternalBatchSelect.PARAM_BATCH_NUMBER);
		
		if (request.getParameter(GLDuplicateExternalBatchSelect.CONFIRM_PROCESS) == null){
			smaction.getCurrentSession().setAttribute(GLDuplicateExternalBatchSelect.SESSION_WARNING_OBJECT, "You must check the 'Confirm' checkbox to continue.");
			smaction.redirectAction(
					"", 
					"", 
		    		""
				);
				return;
		}
		
    	Connection conn = null;
    	try {
			conn = ServletUtilities.clsDatabaseFunctions.getConnectionWithException(
				getServletContext(), 
				smaction.getsDBID(), 
				"MySQL", 
				this.toString() + ".doPost - user: " + smaction.getFullUserName()
			);
		} catch (Exception e1) {
			smaction.getCurrentSession().setAttribute(GLDuplicateExternalBatchSelect.SESSION_WARNING_OBJECT, e1.getMessage());
			smaction.redirectAction(
					"", 
					"", 
		    		""
				);
				return;
		}

    	try {
			duplicateBatch(conn, sBatchNumber, sExternalCompanyID);
		} catch (Exception e) {
			smaction.getCurrentSession().setAttribute(GLDuplicateExternalBatchSelect.SESSION_WARNING_OBJECT, e.getMessage());
			smaction.redirectAction(
					"", 
					"", 
		    		""
				);
				return;
		}
    	
		ServletUtilities.clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1589483534]");
		smaction.redirectAction(
			"", 
			"Company with ID '" + sExternalCompanyID + "' was successfully pulled into the consolidated company.",
    		""
		);
		return;
	}
	private void duplicateBatch(Connection conn, String sBatchNumber, String sExternalCompanyID) throws Exception{
		
		//Load the batch we're duplicating:
		
		
		
		//Create a new batch in our company:
			
		//Copy all the fields, entries, and lines:
		
		//Save the batch:
		
		
		return;
		
	}
	public void doGet(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
		
		doPost(request, response);
	}
}