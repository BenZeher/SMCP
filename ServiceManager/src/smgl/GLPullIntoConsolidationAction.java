package smgl;

import java.io.IOException;
import java.sql.Connection;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import smcontrolpanel.SMMasterEditAction;
import smcontrolpanel.SMSystemFunctions;

public class GLPullIntoConsolidationAction extends HttpServlet{
	
	private static final long serialVersionUID = 1L;
	public void doPost(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
	
		SMMasterEditAction smaction = new SMMasterEditAction(request, response);
		if (!smaction.processSession(getServletContext(), SMSystemFunctions.GLPullExternalDataIntoConsolidation)){return;}
	    //Read the entry fields from the request object:
	    String sExternalCompanyID = ServletUtilities.clsManageRequestParameters.get_Request_Parameter(GLPullIntoConsolidationSelect.RADIO_BUTTONS_NAME, request);
	    boolean bAddNewGLs = ServletUtilities.clsManageRequestParameters.get_Request_Parameter(
	    	GLPullIntoConsolidationSelect.ADD_GL_ACCOUNTS, request).compareToIgnoreCase("") != 0;
	    String sFiscalYearAndPeriodPeriod = request.getParameter(GLPullIntoConsolidationSelect.PARAM_FISCAL_PERIOD_SELECTION);
		String sFiscalYear = sFiscalYearAndPeriodPeriod.substring(0, sFiscalYearAndPeriodPeriod.indexOf(GLPullIntoConsolidationSelect.PARAM_VALUE_DELIMITER));
		String sFiscalPeriod = sFiscalYearAndPeriodPeriod.replace(sFiscalYear + GLTrialBalanceSelect.PARAM_VALUE_DELIMITER, "");
		try {
			pullCompany(smaction, request, sExternalCompanyID, sFiscalYear, sFiscalPeriod, bAddNewGLs);
		} catch (Exception e) {
			smaction.redirectAction(
				e.getMessage(), 
				"", 
	    		""
			);
			return;
		}
		smaction.redirectAction(
			"", 
			"Company with ID '" + sExternalCompanyID + "' was successfully pulled into the consolidated company.",
    		""
		);
		return;
	}
	private void pullCompany(
		SMMasterEditAction sm, 
		HttpServletRequest req, 
		String sCompanyID, 
		String sFiscalYear, 
		String sFiscalPeriod, 
		boolean bAddNewGLs) throws Exception{
		
		//Read the external company lines:
		String sErrorString = "";
    	
    	Connection conn = null;
    	try {
			conn = ServletUtilities.clsDatabaseFunctions.getConnectionWithException(
				getServletContext(), 
				sm.getsDBID(), 
				"MySQL", 
				this.toString() + ".updateCompanies - user: " + sm.getFullUserName()
			);
		} catch (Exception e1) {
			throw new Exception("Error [1562702050] " + "Could not get data connection - " + e1.getMessage());
		}
    	
    	//Pull the transactions into a single GL batch:
    	
    	//First confirm that the period in the consolidated company is unlocked:
    	GLFiscalYear period = new GLFiscalYear();
    	if (period.isPeriodLocked(sFiscalYear, Integer.parseInt(sFiscalPeriod), conn)){
			throw new Exception(
					"Error [20191901557102] " + "fiscal period '" + sFiscalYear + " - " + sFiscalPeriod + "' is locked.");
    	}
    	
    	if (sErrorString.compareToIgnoreCase("") != 0){
    		ServletUtilities.clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1562702052]");
    		throw new Exception("Error [1562702051] saving companies - " + sErrorString);
    	}
    	ServletUtilities.clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1562702053]");
		return;
	}

	public void doGet(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
		
		doPost(request, response);
	}
}