package smgl;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import SMDataDefinition.SMTableglexternalcompanies;
import ServletUtilities.clsDatabaseFunctions;
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

    	String sNewBatchNumber = "";
    	try {
    		sNewBatchNumber = duplicateBatch(conn, sBatchNumber, sExternalCompanyID, smaction.getFullUserName(), smaction.getUserID(), smaction.getsDBID());
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
    	
		ServletUtilities.clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1589483834]");
		smaction.redirectAction(
			"", 
			"External company batch number " + sBatchNumber + " was successfully duplicated in the current company as batch number " + sNewBatchNumber + ".",
    		""
		);
		return;
	}
	private String duplicateBatch(
		Connection conn, 
		String sBatchNumber, 
		String sExternalCompanyID,
		String sUserFullName,
		String sUserID,
		String sDBID
		) throws Exception{
		
		//Get the DB name:
		String SQL = "SELECT"
			+ " " + SMTableglexternalcompanies.sdbname
			+ ", " + SMTableglexternalcompanies.scompanyname
			+ " FROM " + SMTableglexternalcompanies.TableName
			+ " WHERE ("
				+ "(" + SMTableglexternalcompanies.lid + " = " + sExternalCompanyID + ")"
			+ ")"
		;
		String sDBName = "";
		String sCompanyName = "";
		try {
			ResultSet rsExternalCompany = clsDatabaseFunctions.openResultSet(SQL, conn);
			if (rsExternalCompany.next()) {
				sDBName = rsExternalCompany.getString(SMTableglexternalcompanies.sdbname);
				sCompanyName = rsExternalCompany.getString(SMTableglexternalcompanies.scompanyname);
			}else {
				throw new Exception("Error [202005143317] - could not read external company record for ID '" + sExternalCompanyID + "'.");
			}
			rsExternalCompany.close();
		} catch (Exception e) {
			System.out.println("[202005143404] - Error reading external company table with SQL: '" + SQL + "' - " + e.getMessage());
		}
		
		
		
		//Load the batch we're duplicating:
		GLTransactionBatch externalbatch = new GLTransactionBatch(sBatchNumber);
		try {
			externalbatch.loadExternalCompanyBatch(conn, sDBName);
		} catch (Exception e) {
			throw new Exception("Error [202005143648] - loading external company batch - " + e.getMessage());
		}
		
		//Create a new batch in our company:
		GLTransactionBatch batch = new GLTransactionBatch("-1");
		
		//Copy all the fields, entries, and lines:
		try {
			externalbatch.copyBatch(
				batch, 
				sUserFullName, 
				sUserID, 
				conn,
				getServletContext(),
				sDBID
			);
		} catch (Exception e) {
			throw new Exception("Error [202005141911] - copying from external company batch - " + e.getMessage());
		}
		
		//Save the batch:
		try {
			batch.save_with_data_transaction(getServletContext(), sDBID, sUserID, sUserFullName, false);
		} catch (Exception e) {
			throw new Exception("Error [202005142031] - saving new batch - " + e.getMessage() + ".");
		}
		
		return batch.getsbatchnumber();
		
	}
	public void doGet(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
		
		doPost(request, response);
	}
}