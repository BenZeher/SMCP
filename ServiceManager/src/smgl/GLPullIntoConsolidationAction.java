package smgl;

import java.io.IOException;
import java.sql.Connection;

import javax.servlet.ServletContext;
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
		try {
			smaction.getCurrentSession().removeAttribute(GLPullIntoConsolidationSelect.SESSION_WARNING_OBJECT);
		} catch (Exception e2) {
			//If this attribute isn't in the session, just go on without disruption....
		}
		if (!smaction.processSession(getServletContext(), SMSystemFunctions.GLPullExternalDataIntoConsolidation)){return;}
	    //Read the entry fields from the request object:
	    String sExternalCompanyID = ServletUtilities.clsManageRequestParameters.get_Request_Parameter(GLPullIntoConsolidationSelect.RADIO_BUTTONS_NAME, request);
	    boolean bAddNewGLs = ServletUtilities.clsManageRequestParameters.get_Request_Parameter(
	    	GLPullIntoConsolidationSelect.ADD_GL_ACCOUNTS, request).compareToIgnoreCase("") != 0;
	    String sFiscalYearAndPeriodPeriod = request.getParameter(GLPullIntoConsolidationSelect.PARAM_FISCAL_PERIOD_SELECTION);
		String sFiscalYear = sFiscalYearAndPeriodPeriod.substring(0, sFiscalYearAndPeriodPeriod.indexOf(GLPullIntoConsolidationSelect.PARAM_VALUE_DELIMITER));
		String sFiscalPeriod = sFiscalYearAndPeriodPeriod.replace(sFiscalYear + GLTrialBalanceSelect.PARAM_VALUE_DELIMITER, "");
		
		if (request.getParameter(GLPullIntoConsolidationSelect.CONFIRM_PROCESS) == null){
			smaction.getCurrentSession().setAttribute(GLPullIntoConsolidationSelect.SESSION_WARNING_OBJECT, "You must check the 'Confirm' checkbox to continue.");
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
			smaction.getCurrentSession().setAttribute(GLPullIntoConsolidationSelect.SESSION_WARNING_OBJECT, e1.getMessage());
			smaction.redirectAction(
					"", 
					"", 
		    		""
				);
				return;
		}
    	
    	GLExternalPull pull = new GLExternalPull();
		try {
			pull.pullCompany(
				smaction.getsDBID(),
				smaction.getUserID(),
				smaction.getFullUserName(),
				sExternalCompanyID, 
				sFiscalYear, 
				sFiscalPeriod, 
				conn, 
				getServletContext()
			);
		} catch (Exception e) {
			ServletUtilities.clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1562875614]");
			smaction.getCurrentSession().setAttribute(GLPullIntoConsolidationSelect.SESSION_WARNING_OBJECT, e.getMessage());
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
			"Company with ID '" + sExternalCompanyID + "' was successfully pulled into the consolidated company.",
    		""
		);
		return;
	}

	/* TJR - 7/12/2019 - probably can't use this because the account structures may not be the same in the SOURCE and TARGET companies....
	private void addNewGLAccounts(Connection conn, String sDBName, String sFiscalYear, String sFiscalPeriod) throws Exception{
		
		String SQL = "SELECT DISTINCT " + SMTablegltransactionlines.TableName + "." + SMTablegltransactionlines.sacctid
			+ ",  " + SMTableglaccounts.TableName + "." + SMTableglaccounts.bdannualbudget
			+ ",  " + SMTableglaccounts.TableName + "." + SMTableglaccounts.iallowaspoexpense
			+ ",  " + SMTableglaccounts.TableName + "." + SMTableglaccounts.iCostCenterID
			+ ",  " + SMTableglaccounts.TableName + "." + SMTableglaccounts.inormalbalancetype
			+ ",  " + SMTableglaccounts.TableName + "." + SMTableglaccounts.laccountgroupid
			+ ",  " + SMTableglaccounts.TableName + "." + SMTableglaccounts.lActive
			+ ",  " + SMTableglaccounts.TableName + "." + SMTableglaccounts.lstructureid
			+ ",  " + SMTableglaccounts.TableName + "." + SMTableglaccounts.sAcctID
			+ ",  " + SMTableglaccounts.TableName + "." + SMTableglaccounts.sAcctType
			+ ",  " + SMTableglaccounts.TableName + "." + SMTableglaccounts.sDesc
			+ ",  " + SMTableglaccounts.TableName + "." + SMTableglaccounts.sFormattedAcct
			+ " FROM " + sDBName + "." + SMTablegltransactionlines.TableName + " LEFT JOIN " + sDBName + "." + SMTableglaccounts.TableName + " ON "
			+ sDBName + "." + SMTablegltransactionlines.TableName + "." + SMTablegltransactionlines.sacctid + " = " 
			+ sDBName + "." + SMTableglaccounts.TableName + "." + SMTableglaccounts.sAcctID
			+ " WHERE ("
			    + "(" + SMTablegltransactionlines.ifiscalperiod + " = " + sFiscalPeriod + ")"
    			+ " AND (" + SMTablegltransactionlines.ifiscalyear + " = " + sFiscalYear + ")"
			+ ")"
		;
		
		//TODO - go through the accounts and add them if needed:
		
		
		return;
	}
	*/

	public void doGet(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
		
		doPost(request, response);
	}
}