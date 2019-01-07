package smbk;

import java.io.IOException;
import java.sql.Connection;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import smcontrolpanel.SMMasterEditAction;
import smcontrolpanel.SMSystemFunctions;
import smcontrolpanel.SMUtilities;
import SMDataDefinition.SMTablebkaccountentries;
import SMDataDefinition.SMTablebkpostedentries;
import SMDataDefinition.SMTablebkstatements;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsDateAndTimeConversions;
import ServletUtilities.clsManageRequestParameters;

public class BKClearEntriesAction extends HttpServlet{
	
	private static final long serialVersionUID = 1L;

	public void doPost(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
	
		SMMasterEditAction smaction = new SMMasterEditAction(request, response);
		if (!smaction.processSession(getServletContext(), SMSystemFunctions.BKClearStatements)){return;}

		//If the clearing was not confirmed, return:
		if (request.getParameter(BKClearEntriesSelect.CONFIRM_CLEAR_CHECKBOX_NAME) == null){
			smaction.redirectAction(
					"You must click the 'confirming' checkbox to clear the posted statements.", 
					"", 
					""
				);
				return;
		}
		
		//Pick up all the fields we need here:
		String sClearingDate = clsManageRequestParameters.get_Request_Parameter(BKClearEntriesSelect.CLEARING_DATE_FIELD, request);
		try {
			clear_statements(request, smaction, sClearingDate);
		} catch (Exception e) {
			smaction.redirectAction(
				"Error clearing posted statements - " + e.getMessage() + ".", 
				"", 
				""
			);
			return;
		}

		smaction.redirectAction("", "Posted statements up to " + sClearingDate + " were successfully cleared.", "");
		return;
	}

	private void clear_statements(HttpServletRequest request, SMMasterEditAction sm, String sClearingDate) throws Exception{

		//First, validate the date:
		if (!clsDateAndTimeConversions.IsValidDateString("M/d/yyyy", sClearingDate)){
			throw new Exception ("Invalid clearing date: '" + sClearingDate + "'.");
		}
		
		Connection conn;
		try {
			conn = clsDatabaseFunctions.getConnectionWithException(
				getServletContext(), 
				sm.getsDBID(), 
				"MySQL", 
				SMUtilities.getFullClassName(this.toString()) + ".clear_statements - user: " + sm.getUserName());
		} catch (Exception e1) {
			throw new Exception ("Could not get data connection - " + e1.getMessage() + ".");
		}
		
		if (!clsDatabaseFunctions.start_data_transaction(conn)){
			clsDatabaseFunctions.freeConnection(getServletContext(), conn);
			throw new Exception ("Could not start data transaction");
		}
		
		String SQL = "DELETE FROM " + SMTablebkstatements.TableName 
			+ " WHERE ("
				+ "(" + SMTablebkstatements.datstatementdate + " <= '" + clsDateAndTimeConversions.stdDateStringToSQLDateString(sClearingDate) + "')" 
				+ " AND (" + SMTablebkstatements.iposted + " = 1)"
			+ ")"
		;
		try {
			clsDatabaseFunctions.executeSQL(SQL, conn);
		} catch (Exception e) {
			clsDatabaseFunctions.rollback_data_transaction(conn);
			clsDatabaseFunctions.freeConnection(getServletContext(), conn);
			throw new Exception("Error deleting bank statements with SQL: " + SQL + " - " + e.getMessage());
		}
		
		//Now delete all the posted entries:
		SQL = "DELETE POSTEDENTRIES.* FROM " + SMTablebkpostedentries.TableName + " POSTEDENTRIES"
			+ " LEFT JOIN " + SMTablebkstatements.TableName + " STATEMENTS ON POSTEDENTRIES." + SMTablebkpostedentries.lstatementid
			+ " = STATEMENTS." + SMTablebkstatements.lid
			+ " WHERE (STATEMENTS." + SMTablebkstatements.lid + " IS NULL)"
		;
		//System.out.println("[1408478933] SQL = " + SQL);
		try {
			clsDatabaseFunctions.executeSQL(SQL, conn);
		} catch (Exception e) {
			clsDatabaseFunctions.rollback_data_transaction(conn);
			clsDatabaseFunctions.freeConnection(getServletContext(), conn);
			throw new Exception("Error deleting related posted entries with SQL: " + SQL + " - " + e.getMessage());
		}

		//Now delete all the entries that have no corresponding statements.  We do NOT want to delete entries that are not on a statement.
		//Entries that are not on a statement have a ZERO for the statement ID, so we qualify those in the where clause below:
		SQL = "DELETE ACCOUNTENTRIES.* FROM " + SMTablebkaccountentries.TableName + " ACCOUNTENTRIES"
			+ " LEFT JOIN " + SMTablebkstatements.TableName + " STATEMENTS ON ACCOUNTENTRIES." + SMTablebkaccountentries.lstatementid
			+ " = STATEMENTS." + SMTablebkstatements.lid
			+ " WHERE ("
				+ "(STATEMENTS." + SMTablebkstatements.lid + " IS NULL)"
				+ " AND (ACCOUNTENTRIES." + SMTablebkaccountentries.lstatementid + " > 0)"
			+ ")"
		;
		//System.out.println("[1408478934] SQL = " + SQL);
		try {
			clsDatabaseFunctions.executeSQL(SQL, conn);
		} catch (Exception e) {
			clsDatabaseFunctions.rollback_data_transaction(conn);
			clsDatabaseFunctions.freeConnection(getServletContext(), conn);
			throw new Exception("Error deleting related account entries with SQL: " + SQL + " - " + e.getMessage());
		}
		
		if (!clsDatabaseFunctions.commit_data_transaction(conn)){
			clsDatabaseFunctions.rollback_data_transaction(conn);
			clsDatabaseFunctions.freeConnection(getServletContext(), conn);
			throw new Exception ("Error committing data transaction.");
		}
		
		clsDatabaseFunctions.freeConnection(getServletContext(), conn);
		return;
	}
	public void doGet(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
		
		doPost(request, response);
	}
}