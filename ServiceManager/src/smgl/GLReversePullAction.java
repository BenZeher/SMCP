package smgl;

import java.io.IOException;
import java.sql.Connection;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import SMClasses.SMLogEntry;
import smcontrolpanel.SMMasterEditAction;
import smcontrolpanel.SMSystemFunctions;
import smcontrolpanel.SMUtilities;

public class GLReversePullAction extends HttpServlet{
	
	private static final long serialVersionUID = 1L;
	public void doPost(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
	
		SMMasterEditAction smaction = new SMMasterEditAction(request, response);
		try {
			smaction.getCurrentSession().removeAttribute(GLReversePullSelect.SESSION_WARNING_OBJECT);
		} catch (Exception e2) {
			//If this attribute isn't in the session, just go on without disruption....
		}
		if (!smaction.processSession(getServletContext(), SMSystemFunctions.GLReverseExternalCompanyPulls)){return;}
	    //Read the entry fields from the request object:
	    String sExternalPullID = ServletUtilities.clsManageRequestParameters.get_Request_Parameter(GLReversePullSelect.RADIO_BUTTONS_NAME, request);
	    String sDBID = ServletUtilities.clsManageRequestParameters.get_Request_Parameter(SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID, request);
	    		
	    String sUserID = (String)smaction.getCurrentSession().getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERID);
	    String sUserFullName = (String)smaction.getCurrentSession().getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERFIRSTNAME)
	    	+ " " + (String)smaction.getCurrentSession().getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERLASTNAME)
	    ;
		
	    
		if (request.getParameter(GLReversePullSelect.CONFIRM_PROCESS) == null){
			smaction.getCurrentSession().setAttribute(GLReversePullSelect.SESSION_WARNING_OBJECT, "You must check the 'Confirm' checkbox to continue.");
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
			smaction.getCurrentSession().setAttribute(GLReversePullSelect.SESSION_WARNING_OBJECT, e1.getMessage());
			smaction.redirectAction(
					"", 
					"", 
		    		""
				);
				return;
		}
    	
    	SMLogEntry log = new SMLogEntry(sDBID, getServletContext());
    	GLExternalPull pull = new GLExternalPull();
		try {
			pull.reversePreviousPull(
				conn, 
				sUserID, 
				sUserFullName, 
				sExternalPullID, 
				log);

		} catch (Exception e) {
			ServletUtilities.clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1562875914]");
			smaction.getCurrentSession().setAttribute(GLReversePullSelect.SESSION_WARNING_OBJECT, e.getMessage());
			smaction.redirectAction(
				"", 
				"", 
	    		""
			);
			return;
		}
		ServletUtilities.clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1562875915]");
		smaction.redirectAction(
			"", 
			"Previous pull with ID# " + sExternalPullID + " was successfully reversed.",
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