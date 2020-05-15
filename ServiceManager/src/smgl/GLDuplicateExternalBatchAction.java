package smgl;

import java.io.IOException;
import java.sql.Connection;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import SMDataDefinition.SMTablegltransactionbatches;
import smcontrolpanel.SMMasterEditAction;
import smcontrolpanel.SMSystemFunctions;
import smcontrolpanel.SMUtilities;

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
	    String sExternalBatchNumber = request.getParameter(GLDuplicateExternalBatchSelect.PARAM_BATCH_NUMBER);
		
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

    	GLTransactionBatch externalbatch = new GLTransactionBatch(sExternalBatchNumber);
    	try {
			externalbatch.loadExternalCompanyBatch(conn, sExternalCompanyID, sExternalBatchNumber);
		} catch (Exception e1) {
			ServletUtilities.clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1589483934]");
			smaction.getCurrentSession().setAttribute(GLDuplicateExternalBatchSelect.SESSION_WARNING_OBJECT, e1.getMessage());
			smaction.redirectAction(
					"", 
					"", 
		    		""
				);
				return;
		}
    	GLTransactionBatch duplicatedbatch = null;
    	try {
    		duplicatedbatch = externalbatch.duplicateCurrentBatch(
    				conn, 
    				smaction.getFullUserName(), 
    				smaction.getUserID(), 
    				smaction.getsDBID(), 
    				getServletContext()
    		);
		} catch (Exception e) {
			ServletUtilities.clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1589483734]");
			smaction.getCurrentSession().setAttribute(GLDuplicateExternalBatchSelect.SESSION_WARNING_OBJECT, e.getMessage());
			smaction.redirectAction(
					"", 
					"", 
		    		""
				);
				return;
		}
    	
		//Save the batch:
		try {
			duplicatedbatch.save_with_data_transaction(getServletContext(), smaction.getsDBID(), smaction.getUserID(), smaction.getFullUserName(), false);
		} catch (Exception e) {
			ServletUtilities.clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1589483434]");
			smaction.getCurrentSession().setAttribute(GLDuplicateExternalBatchSelect.SESSION_WARNING_OBJECT, e.getMessage());
			smaction.redirectAction(
					"", 
					"", 
		    		""
				);
				return;
		}
    	
		ServletUtilities.clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1589483834]");
		String sNewBatchNumberLink = duplicatedbatch.getsbatchnumber();
		boolean bAllowGLBatchEditing = SMSystemFunctions.isFunctionPermitted(
			SMSystemFunctions.GLEditBatches, 
			smaction.getUserID(), 
			conn, 
			(String) smaction.getCurrentSession().getAttribute(SMUtilities.SMCP_SESSION_PARAM_LICENSE_MODULE_LEVEL)
		);
		
		if (bAllowGLBatchEditing) {
			sNewBatchNumberLink = "<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smgl." 
    	    		+ "GLEditBatchesEdit" 
    	    		+ "?" + SMTablegltransactionbatches.lbatchnumber + "=" + sNewBatchNumberLink
    	    		+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + smaction.getsDBID()
    	    		+ "\">"
    	    		+ sNewBatchNumberLink
    	    		+ "</A>";
		}
		
		ServletUtilities.clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1589483834]");
		
		smaction.redirectAction(
			"", 
			"External company batch number " + sExternalBatchNumber + " was successfully duplicated in the current company as batch number " + sNewBatchNumberLink + ".",
    		""
		);
		return;
	}

	public void doGet(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
		
		doPost(request, response);
	}
}