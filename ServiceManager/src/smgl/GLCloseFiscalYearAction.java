package smgl;

import java.io.IOException;
import java.sql.Connection;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import smcontrolpanel.SMMasterEditAction;
import smcontrolpanel.SMSystemFunctions;

public class GLCloseFiscalYearAction extends HttpServlet{
	
	private static final long serialVersionUID = 1L;
	public void doPost(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
	
		SMMasterEditAction smaction = new SMMasterEditAction(request, response);
		try {
			smaction.getCurrentSession().removeAttribute(GLCloseFiscalYearEdit.GL_CLOSING_SESSION_WARNING_OBJECT);
		} catch (Exception e2) {
			//If this attribute isn't in the session, just go on without disruption....
		}
		if (!smaction.processSession(getServletContext(), SMSystemFunctions.GLCloseFiscalYear)){return;}
	    //Read the entry fields from the request object:
	    String sFiscalYear = ServletUtilities.clsManageRequestParameters.get_Request_Parameter(GLCloseFiscalYearEdit.PARAM_FISCAL_YEAR_SELECTION, request);
	    String sBatchDate = ServletUtilities.clsManageRequestParameters.get_Request_Parameter(GLCloseFiscalYearEdit.PARAM_BATCH_DATE, request);
		
		if (request.getParameter(GLPullIntoConsolidationSelect.CONFIRM_PROCESS) == null){
			smaction.getCurrentSession().setAttribute(GLCloseFiscalYearEdit.GL_CLOSING_SESSION_WARNING_OBJECT, "You must check the 'Confirm' checkbox to continue.");
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
				this.toString() + ".updateCompanies - user: " + smaction.getFullUserName()
			);
		} catch (Exception e1) {
			smaction.getCurrentSession().setAttribute(GLCloseFiscalYearEdit.GL_CLOSING_SESSION_WARNING_OBJECT, e1.getMessage());
			smaction.redirectAction(
					"", 
					"", 
		    		""
				);
				return;
		}
    	
    	GLTransactionBatch glbatch = new GLTransactionBatch("-1");
    	glbatch.setlcreatedby(smaction.getUserID());
    	glbatch.setllasteditedby(smaction.getUserID());
    	glbatch.setsbatchdate(sBatchDate);
    	glbatch.setsbatchdescription("Closing Journal Entries");

    	GLTransactionBatchEntry glentry = new GLTransactionBatchEntry();
    	glentry.setsdatdocdate(sBatchDate);
    	glentry.setsdatentrydate(sBatchDate);
    	glentry.setsentrydescription("Closing Entry");
    	glentry.setsentrynumber("1");
    	glentry.setsfiscalperiod("15");
    	glentry.setsfiscalyear(sFiscalYear);
    	glentry.setssourceledger(GLSourceLedgers.getSourceLedgerDescription(GLSourceLedgers.SOURCE_LEDGER_JOURNAL_ENTRY));

    	//Now get the transaction batch lines:
    	//TODO:
    	
    	
    	try {

		} catch (Exception e) {
			ServletUtilities.clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1562875614]");
			smaction.getCurrentSession().setAttribute(GLCloseFiscalYearEdit.GL_CLOSING_SESSION_WARNING_OBJECT, e.getMessage());
			smaction.redirectAction(
				"", 
				"", 
	    		""
			);
			return;
		}
		ServletUtilities.clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1562875615]");
		smaction.redirectAction(
			"", 
			"Company with ID '" + "" + "' was successfully pulled into the consolidated company.",
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