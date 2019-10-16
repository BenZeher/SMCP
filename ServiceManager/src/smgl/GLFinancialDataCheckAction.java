package smgl;

import java.io.IOException;
import java.sql.Connection;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import smcontrolpanel.SMMasterEditAction;
import smcontrolpanel.SMSystemFunctions;

public class GLFinancialDataCheckAction extends HttpServlet{
	
	private static final long serialVersionUID = 1L;
	public void doPost(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
	
		SMMasterEditAction smaction = new SMMasterEditAction(request, response);
		try {
			smaction.getCurrentSession().removeAttribute(GLFinancialDataCheckSelect.SESSION_RESULTS_OBJECT);
			smaction.getCurrentSession().removeAttribute(GLFinancialDataCheckSelect.SESSION_WARNING_OBJECT);
		} catch (Exception e2) {
			//If these attributes aren't in the session, just go on without disruption....
		}
		if (!smaction.processSession(getServletContext(), SMSystemFunctions.GLCheckFinancialData)){return;}
	    //Read the entry fields from the request object:
		String sFiscalYear = request.getParameter(GLFinancialDataCheckSelect.PARAM_FISCAL_YEAR_SELECTION);
		String sGLAccount = request.getParameter(GLFinancialDataCheckSelect.PARAM_GL_ACCOUNTS);
		
		if (request.getParameter(GLFinancialDataCheckSelect.CONFIRM_PROCESS) == null){
			smaction.getCurrentSession().setAttribute(GLFinancialDataCheckSelect.SESSION_WARNING_OBJECT, "You must check the 'Confirm' checkbox to continue.");
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
			smaction.getCurrentSession().setAttribute(GLFinancialDataCheckSelect.SESSION_WARNING_OBJECT, e1.getMessage());
			smaction.redirectAction(
					"", 
					"", 
		    		""
				);
				return;
		}
    	
    	GLFinancialDataCheck dc = new GLFinancialDataCheck();
    	String sResults = "";
    	//System.out.println("[2019289938156] " + "sGLAccount = '" + sGLAccount + "'");
    	//System.out.println("[2019289938346] " + "sFiscalYear = '" + sFiscalYear + "'");
    	
    	boolean bCheckRecordsOnly = true;
    	try {
			sResults = dc.processFinancialRecords(sGLAccount, sFiscalYear, conn, bCheckRecordsOnly);
		} catch (Exception e) {
			
			//System.out.println("[2019289941251] " + "financial check error: '" + e.getMessage() + "'.");
			smaction.getCurrentSession().setAttribute(GLFinancialDataCheckSelect.SESSION_WARNING_OBJECT, e.getMessage());
			smaction.redirectAction(
					"", 
					"", 
		    		""
				);
				return;
		}
    	
    	//return after successful processing:
    	//System.out.println("[2019289940487] " + "sResults = '" + sResults + "'.");
		smaction.getCurrentSession().setAttribute(GLFinancialDataCheckSelect.SESSION_RESULTS_OBJECT, sResults);
		smaction.redirectAction(
				"", 
				"", 
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