package smap;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import SMDataDefinition.SMTableapbatchentries;
import SMDataDefinition.SMTableapbatches;
import SMDataDefinition.SMTableapchecks;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsManageRequestParameters;
import smcontrolpanel.SMAuthenticate;
import smcontrolpanel.SMSystemFunctions;
import smcontrolpanel.SMUtilities;

public class APConfirmCheckPrintAction extends HttpServlet{
	
	public static final String BUTTON_CHECKS_PRINTED_VALUE = "DIDCHECKSPRINTVALUE";
	public static final String BUTTON_CHECKS_NOT_PRINTED_SUCCESSFULLY = "NOTSUCCESSFUL";
	public static final String BUTTON_CHECKS_PRINTED_SUCCESSFULLY = "SUCCESSFUL";
	public static final String BUTTON_CHECKS_NOT_PRINTED_SUCCESSFULLY_LABEL = "NO";
	public static final String BUTTON_CHECKS_PRINTED_SUCCESSFULLY_LABEL = "YES";
	public static final String CHECKBOX_CONFIRM = "CONFIRM";
	public static final String COMMADELIMITED_LIST_OF_FINALIZED_CHECK_BATCHENTRYIDS = "FINALIZEDENTRIES";
	
	private static final long serialVersionUID = 1L;
	private static String sUserName = "";
	private static String sDBID = "";
	public void doPost(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
	
	if (!SMAuthenticate.authenticateSMCPCredentials(
			request, 
			response, 
			getServletContext(), 
			SMSystemFunctions.APPrintChecks))
	{
		return;
	}

    //Get the session info:
    HttpSession CurrentSession = request.getSession(true);
    
    sUserName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERNAME);
    sDBID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_DATABASE_ID);
    String sBatchNumber = request.getParameter(SMTableapbatches.lbatchnumber);
    
    //String title = "Updating check status";
    //String subtitle = "";
	
    //If the user says the checks are successfully printed, then we need to update the entries' checks as finalized and return
    //to the batch edit screen:
    if (clsManageRequestParameters.get_Request_Parameter(BUTTON_CHECKS_PRINTED_VALUE, request).compareToIgnoreCase(BUTTON_CHECKS_PRINTED_SUCCESSFULLY_LABEL) == 0){
    	//We get a list of which entries' checks were 'FINALIZED', and we need to update those entries to flag them as having 'finalized' checks:
    	String sFinalizedEntryIDList = clsManageRequestParameters.get_Request_Parameter(COMMADELIMITED_LIST_OF_FINALIZED_CHECK_BATCHENTRYIDS, request);
    	
    	try {
			setBatchEntryCheckStatusToFinalizedAndUpdateCheckNumberOnBatchEntry(
				sBatchNumber, 
				sFinalizedEntryIDList, 
				getServletContext(), 
				sDBID, 
				sUserName
			);
		} catch (Exception e) {
			response.sendRedirect(
					SMUtilities.getURLLinkBase(getServletContext()) + "smap.APPrintChecksEdit" 
					+ "?" + SMTableapbatches.lbatchnumber + "=" + sBatchNumber
					+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
		    	    + "&" + "Warning=" + e.getMessage()
					);
				return;
		}
    	
		response.sendRedirect(
			SMUtilities.getURLLinkBase(getServletContext()) + "smap.APEditBatchesEdit"
			+ "?" + SMTableapbatches.lbatchnumber + "=" + sBatchNumber
			+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
			+ "&" + SMTableapbatches.ibatchtype + "=" + Integer.toString(SMTableapbatches.AP_BATCH_TYPE_PAYMENT)
			);
		return;
    }else{
		response.sendRedirect(
			SMUtilities.getURLLinkBase(getServletContext()) + "smap.APPrintChecksEdit" 
			+ "?" + SMTableapbatches.lbatchnumber + "=" + sBatchNumber
			+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
			);
		return;
    }
}
	
	private void setBatchEntryCheckStatusToFinalizedAndUpdateCheckNumberOnBatchEntry(
    	String sBatchNumber, 
    	String sFinalizedEntryIDList,
    	ServletContext context, 
    	String sDBID, 
    	String sUserName
    	) throws Exception{
		
		Connection conn;
		try {
			conn = clsDatabaseFunctions.getConnectionWithException(
				context, 
				sDBID, 
				"MySQL", 
				SMUtilities.getFullClassName(
					this.toString()) + ".setBatchEntryCheckStatusToFinalizedAndUpdateCheckNumberOnBatchEntry - user: '" + sUserName);
		} catch (Exception e1) {
			throw new Exception("Error [1513214550] - couldn't get data connection - " + e1.getMessage());
		}
		
		String arrFinalizedEntryIDs[] = sFinalizedEntryIDList.split(",");
		
		if (!clsDatabaseFunctions.start_data_transaction(conn)){
			clsDatabaseFunctions.freeConnection(context, conn, "[]1547047438");
			throw new Exception("Error [1513214551] - couldn't start data transaction.");
		}
		
		for (int i = 0; i < arrFinalizedEntryIDs.length; i++){
			String sCheckNumber = "";
			String SQL = "SELECT"
				+ " " + SMTableapchecks.schecknumber
				+ " FROM " + SMTableapchecks.TableName
				+ " WHERE ("
					+ "(" + SMTableapchecks.lbatchentryid + " = " + arrFinalizedEntryIDs[i] + ")"
					+ " AND (" + SMTableapchecks.ilastpage + " = 1)"  //Get the very last check page, to get the correct check number
				+ ")"
			;
			ResultSet rsChecks = clsDatabaseFunctions.openResultSet(SQL, conn);
			if (rsChecks.next()){
				sCheckNumber = rsChecks.getString(SMTableapchecks.schecknumber);
			}
			rsChecks.close();
			if (sCheckNumber.compareToIgnoreCase("") == 0){
				clsDatabaseFunctions.rollback_data_transaction(conn);
				clsDatabaseFunctions.freeConnection(context, conn, "[]1547047439");
				throw new Exception("Error [1513214540] - Check number for last check page linked to batch entry ID " + arrFinalizedEntryIDs[i] + " was not found with SQL '" + SQL + "'.");
			}
			
			//Now go and update the batch entry with the finalized check number of the LAST check page:
			SQL = "UPDATE"
				+ " " + SMTableapbatchentries.TableName
				+ " SET " + SMTableapbatchentries.iprintingfinalized + " = 1"
				+ ", " + SMTableapbatchentries.schecknumber + " = '" + sCheckNumber + "'"
				+ " WHERE ("
					+ "(" + SMTableapbatchentries.lid + " = " + arrFinalizedEntryIDs[i] + ")"
				+ ")"
			;
			try {
				clsDatabaseFunctions.executeSQL(
					SQL, 
					context, 
					sDBID, 
					"MySQL", 
					SMUtilities.getFullClassName(this.toString()) + ".setBatchEntryCheckStatusToFinalized - user: " + sUserName
				);
			} catch (Exception e) {
				clsDatabaseFunctions.rollback_data_transaction(conn);
				clsDatabaseFunctions.freeConnection(context, conn, "[]1547047440");
				throw new Exception("Error [1510770200] updating finalized batch entries with SQL '" + SQL + "' - " + e.getMessage());
			}
		}
		if(!clsDatabaseFunctions.commit_data_transaction(conn)){
			clsDatabaseFunctions.rollback_data_transaction(conn);
			clsDatabaseFunctions.freeConnection(context, conn, "[]1547047441");
			throw new Exception("Error [1513214553] - couldn't commit data transaction.");
			
		}
		clsDatabaseFunctions.freeConnection(context, conn, "[]1547047442");
		return;
		
	}
	public void doGet(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
		
		doPost(request, response);
	}
}
