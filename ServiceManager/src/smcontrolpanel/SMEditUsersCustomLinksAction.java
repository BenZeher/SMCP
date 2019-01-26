package smcontrolpanel;

import java.io.IOException;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Enumeration;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import smcontrolpanel.SMSystemFunctions;
import SMDataDefinition.*;
import ServletUtilities.clsServletUtilities;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsManageRequestParameters;

public class SMEditUsersCustomLinksAction extends HttpServlet {

	private static final long serialVersionUID = 1L;

	public void doPost(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {

		if (!SMAuthenticate.authenticateSMCPCredentials(
				request, 
				response, 
				getServletContext(), 
				SMSystemFunctions.SMEditUsersCustomLinks))
		{
			return;
		}

	    //Get the session info:
	    HttpSession CurrentSession = request.getSession(true);
	    String sDBID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_DATABASE_ID);
	    String sUserID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERID);
	    String sUserFirstName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERFIRSTNAME);
	    String sUserLastName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERLASTNAME);
	    
	    String sCallingClass = "smcontrolpanel.SMEditUsersCustomLinksEdit";
	    /**************Check Parameters**************/
	    
	    //Get the command value of this page from the request.
	  	String sCommandValue = clsManageRequestParameters.get_Request_Parameter(SMEditUsersCustomLinksEdit.COMMAND_FLAG, request);
	  		
	  	//If it's an edit, process that:
	  	if(sCommandValue.compareToIgnoreCase(SMEditUsersCustomLinksEdit.SAVE_COMMAND_VALUE) == 0){  		
	  		try {	
				save(request, getServletContext(), sDBID, sUserID, sUserFirstName, sUserLastName);
				
			  	//redirect back to edit screen.
				response.sendRedirect(
						"" + SMUtilities.getURLLinkBase(getServletContext()) + "" +  sCallingClass + "?"
						+ "Status=" + clsServletUtilities.URLEncode("Custom Link have been updated successfully." ));			
		    	return;
			} catch (Exception e) {
				response.sendRedirect(
						"" + SMUtilities.getURLLinkBase(getServletContext()) + "" + sCallingClass + "?"
						+ "Warning=" + clsServletUtilities.URLEncode("Custom Link failed to save - " + e.getMessage()));			
		    	return;
			}
	  	}
	  	//If its a delete, process that:
	  	if(sCommandValue.contains(SMEditUsersCustomLinksEdit.DELETE_COMMAND_VALUE)){  		
	  		try {	
	  			String sURLID = sCommandValue.substring(SMEditUsersCustomLinksEdit.DELETE_COMMAND_VALUE.length(), sCommandValue.length());
				
	  			delete(sURLID, request, getServletContext(), sDBID, sUserID, sUserFirstName, sUserLastName );
				
			  	//redirect back to edit screen.
				response.sendRedirect(
						"" + SMUtilities.getURLLinkBase(getServletContext()) + "" + sCallingClass + "?"
						+ "Status=" + clsServletUtilities.URLEncode("Custom Link have been updated successfully." ));			
		    	return;
			} catch (Exception e) {
				response.sendRedirect(
						"" + SMUtilities.getURLLinkBase(getServletContext()) + "" + sCallingClass + "?"
						+ "Warning=" + clsServletUtilities.URLEncode("Custom Link failed to save - " + e.getMessage()));			
		    	return;
			}
	  	}
	}
	
	private void delete(String sURLID, HttpServletRequest request, ServletContext context, String sConf, String sUserID, String sUserFirstName, String sUserLastName) throws Exception{
		// Get connection
		Connection conn = clsDatabaseFunctions.getConnection(context, sConf, "MySQL",
				SMUtilities.getFullClassName(this.toString()) 
				+ ":save - userID: " 
				+ sUserID
				+ " - "
				+ sUserFirstName
				+ " "
				+ sUserLastName);
		if (conn == null) {
			throw new Exception("Error getting data connection.");
		}	

		try {
			deleteFromUsersCustomLinksTable(sURLID, request, conn, context, sUserID);
		} catch (Exception e) {
			clsDatabaseFunctions.freeConnection(context, conn, "[1547080549]");
			throw new Exception("Error deleting scheduled users table - " + e.getMessage());
		}
		
		clsDatabaseFunctions.freeConnection(context, conn, "[1547080550]");
	}

	private void deleteFromUsersCustomLinksTable(String sURLID, HttpServletRequest request, Connection conn, ServletContext context, 
			String sUserName) throws Exception {


		String SQL = "";

				SQL = "DELETE FROM " + SMTableuserscustomlinks.TableName 
					+ " WHERE ("  
					+ "(" + SMTableuserscustomlinks.icustomlinkid + " = " + sURLID + ")"
						+ ")"
						;

				try{
					if(!clsDatabaseFunctions.executeSQL(SQL, conn)){
						throw new Exception("Error [1512152962] delete records from userscustomlinks with SQL '" + SQL + "'");
					}	
				}catch(Exception e1){
						throw new Exception("Error [1512152963] inserting records -" + e1.getMessage());
				}

				SQL = "DELETE FROM " + SMTablecustomlinks.TableName 
						+ " WHERE ("  
						+ "(" + SMTablecustomlinks.lid + " = " + sURLID + ")"
							+ ")"
							;

					try{
						if(!clsDatabaseFunctions.executeSQL(SQL, conn)){
							throw new Exception("Error [1512152962] delete records from userscustomlinks with SQL '" + SQL + "'");
						}	
					}catch(Exception e1){
							throw new Exception("Error [1512152963] inserting records -" + e1.getMessage());
					}
		
	}

	private void save(HttpServletRequest request, ServletContext context, String sConf, String sUserID, String sUserFirstName, String sUserLastName) throws Exception{
  		
		// Get connection
		Connection conn = clsDatabaseFunctions.getConnection(context, sConf, "MySQL",
				SMUtilities.getFullClassName(this.toString()) 
				+ ":save - userID: " 
				+ sUserID
				+ " - "
				+ sUserFirstName
				+ " "
				+ sUserLastName);
		if (conn == null) {
			throw new Exception("Error getting data connection.");
		}
		// Validate entries
		try {
			validateEntries(request);
		} catch (Exception e) {
			clsDatabaseFunctions.freeConnection(context, conn, "[1547080551]");
			throw new Exception("Error validating entries - " + e.getMessage());
		}
		// Now begin a data transaction to update users custom links:
		if (!clsDatabaseFunctions.start_data_transaction(conn)) {
			clsDatabaseFunctions.freeConnection(context, conn, "[1547080552]");
			throw new Exception("Error [1512153206] beginning data transaction to update users links tables");
		}

		// Update Users custom links table
		try {
			updateUsersCustomLinksTable(request, conn, context, sUserID);
		} catch (Exception e) {
			clsDatabaseFunctions.rollback_data_transaction(conn);
			clsDatabaseFunctions.freeConnection(context, conn, "[1547080553]");
			throw new Exception("Error [1530547018] updating custom links  table - " + e.getMessage());
		}

		// Commit transaction
		if (!clsDatabaseFunctions.commit_data_transaction(conn)) {
			clsDatabaseFunctions.rollback_data_transaction(conn);
			clsDatabaseFunctions.freeConnection(context, conn, "[1547080554]");
			throw new Exception(
					"Error [1512153207] commiting data transaction for custom links");
		}

		clsDatabaseFunctions.freeConnection(context, conn, "[1547080555]");
	}

	private void validateEntries(HttpServletRequest request) throws Exception {
		if(clsManageRequestParameters.get_Request_Parameter(SMTablecustomlinks.surl, request).compareToIgnoreCase("") == 0) {
			throw new Exception("URL can not be empty.");
		}
		if(clsManageRequestParameters.get_Request_Parameter(SMTablecustomlinks.surlname, request).compareToIgnoreCase("") == 0) {
			throw new Exception("Name can not be empty.");
		}

	}

	private void updateUsersCustomLinksTable(HttpServletRequest request, Connection conn, ServletContext context, String sUserID) throws Exception {
		
		//Get the Custom Link
		String sURL = clsManageRequestParameters.get_Request_Parameter(SMTablecustomlinks.surl, request);
		String sURLName = clsManageRequestParameters.get_Request_Parameter(SMTablecustomlinks.surlname, request);
		
		//collect all users to add link for
  		Enumeration <String> e = request.getParameterNames();
  		ArrayList<String> arrUsersToUpdate = new ArrayList<String> (0);
		String sParam = "";
		arrUsersToUpdate.clear();
		
		while (e.hasMoreElements()){
			sParam = (String) e.nextElement();
			String sUserToUpdate = "";
			if (sParam.contains(SMEditUsersCustomLinksEdit.USER_UPDATE_ID_MARKER)){
				sUserToUpdate = sParam.substring(SMEditUsersCustomLinksEdit.USER_UPDATE_ID_MARKER.length(), sParam.length());
			}		
			if (request.getParameter(sParam) != null && sUserToUpdate.compareToIgnoreCase("") != 0){
				arrUsersToUpdate.add(sUserToUpdate);
			}	
		}
		if(arrUsersToUpdate.isEmpty()) {
			throw new Exception("Error saving - Now Users selected. ");
		}
		String SQL = "";
			SQL = "INSERT INTO " + SMTablecustomlinks.TableName 
				+ " (" + SMTablecustomlinks.surl 
				+ ", " + SMTablecustomlinks.surlname 
				+ ") VALUES ( "
					+ "'" + sURL.trim() + "'"
    				+ ",'" + sURLName.trim() + "'"
    			+ ")"
    			;
				try{
					if(!clsDatabaseFunctions.executeSQL(SQL, conn)){
						throw new Exception("Error [1512152962] inserting into customlinks");
					}	
				}catch(Exception e1){
						throw new Exception("Error [1512152963] inserting records -" + e1.getMessage());
				}
				
				for (int i = 0; i < arrUsersToUpdate.size(); i++){	
					SQL = "INSERT INTO " + SMTableuserscustomlinks.TableName 
						+ " (" + SMTableuserscustomlinks.icustomlinkid 
						+ ", " + SMTableuserscustomlinks.luserid 
						+ ") SELECT " + SMTablecustomlinks.lid 
						+ ", " + arrUsersToUpdate.get(i) 
						+ " FROM " + SMTablecustomlinks.TableName
						+ " WHERE ("
						+ "(" +SMTablecustomlinks.surl + "='" + sURL.trim() + "')"
						+ " AND "
						+ "(" +SMTablecustomlinks.surlname + "='" + sURLName.trim() + "')"
	    				+ ")"
	    			;
					try{
						if(!clsDatabaseFunctions.executeSQL(SQL, conn)){
							throw new Exception("Error [1512152964] inserting into userscustomlinks for user '" + arrUsersToUpdate.get(i) + "'");
						}	
					}catch(Exception e1){
							throw new Exception("Error [1512152965] inserting records -" + e1.getMessage());
					}
				}
			
	}
	public void doGet(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
		
		doPost(request, response);
	}
}
