package smcontrolpanel;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import SMDataDefinition.SMTablesavedqueries;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsManageRequestParameters;


public class SMEditSavedQueryAction extends HttpServlet {

	private static final long serialVersionUID = 1L;
	public void doPost(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
		
		//Process session
		SMMasterEditAction smaction = new SMMasterEditAction(request, response);
		if (!smaction.processSession(getServletContext(), SMSystemFunctions.SMQuerySelector)){return;}
		
		//Get parameters
		String sEditQueryID = clsManageRequestParameters.get_Request_Parameter(SMSavedQueriesSelect.EDIT_QUERY_ID_PARAM, request);
		String sQueryString = clsManageRequestParameters.get_Request_Parameter(SMQuerySelect.PARAM_QUERYSTRING, request);
	
		//Update the the Query
		try {
			String SQL = "UPDATE " + SMTablesavedqueries.TableName 
					   + " SET " + SMTablesavedqueries.ssql + " = \"" + clsDatabaseFunctions.FormatSQLStatement(sQueryString) + "\"" 
					   + " WHERE " + SMTablesavedqueries.id + " = " + sEditQueryID + "";

			if (!clsDatabaseFunctions.executeSQL(SQL, getServletContext(), smaction.getsDBID(), "MySQL",
					this.toString() + ".doGet - User: " + smaction.getFullUserName())) {
				smaction.redirectAction(
						"Failed to execute SQL: " + SQL, 
						"", 
						SMSavedQueriesSelect.EDIT_QUERY_ID_PARAM + "=" + sEditQueryID
					);
			}
		} catch (Exception e) {
			smaction.redirectAction(
					"Failed to save: " + e.getMessage(), 
					"", 
					SMSavedQueriesSelect.EDIT_QUERY_ID_PARAM + "=" + sEditQueryID
				);
		}
		
		//Redirect back to the edit screen in all cases.  
		smaction.redirectAction(
				"", 
				"Query was successfully saved.", 
				SMSavedQueriesSelect.EDIT_QUERY_ID_PARAM + "=" + sEditQueryID
			);	
	}
	public void doGet(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
		
		doPost(request, response);
	}
}
