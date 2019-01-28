package smar;

import java.io.IOException;
//import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.ParseException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import smcontrolpanel.SMAuthenticate;
import smcontrolpanel.SMSystemFunctions;
import smcontrolpanel.SMUtilities;
import SMClasses.SMBatchStatuses;
import SMClasses.SMEntryBatch;
import SMClasses.SMModuleTypes;
import SMDataDefinition.SMTableentries;
import SMDataDefinition.SMTableentrylines;
import SMDataDefinition.SMTableglexportdetails;
import SMDataDefinition.SMTableglexportheaders;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsDateAndTimeConversions;

public class ARClearPostedBatchesAction extends HttpServlet{

	private static final long serialVersionUID = 1L;
	public void doPost(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
	
		String sCallingClass = ARUtilities.get_Request_Parameter("CallingClass", request);
	
	    if (!SMAuthenticate.authenticateSMCPCredentials(request, response, getServletContext(), SMSystemFunctions.ARClearpostedbatches)){
	    	return;
	    }

	    //Get the session info:
	    HttpSession CurrentSession = request.getSession(true);
	    String sUserID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERID);
	    String sUserFullName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERFIRSTNAME)+ " " 
	    			+	(String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERLASTNAME);
	    String sDBID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_DATABASE_ID);
	    String m_sWarning = "";
	    //Need this for a data transaction:
		Connection conn = clsDatabaseFunctions.getConnection(
				getServletContext(), 
				(String)CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_DATABASE_ID),
				"MySQL",
				this.toString() + ".doPost - User: " 
				+ sUserID
				+ " - "
				+ sUserFullName
		);
		if (conn == null){
			m_sWarning = "Unable to get data connection.";
			response.sendRedirect(
					"" + SMUtilities.getURLLinkBase(getServletContext()) + "" + sCallingClass + "?"
					+ "Warning=" + m_sWarning
					+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
			);			
	    	return;
		}
		String sClearingDate = ARUtilities.get_Request_Parameter("ClearingDate", request);

    	//Convert the date to a SQL one:
    	java.sql.Date datClearingDate = null;
		try {
			datClearingDate = clsDateAndTimeConversions.StringTojavaSQLDate("M/d/yyyy", sClearingDate);
		} catch (ParseException e1) {
			m_sWarning = "Invalid clearing date: '" + sClearingDate + "'" ;
			response.sendRedirect(
					"" + SMUtilities.getURLLinkBase(getServletContext()) + "" + sCallingClass + "?"
					+ "Warning=" + m_sWarning
					+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
			);
			return;
		}
		
	    if (request.getParameter("ConfirmClear") == null){
	    	clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1547067513]");
	    	m_sWarning = "You chose to clear, but did not check the box to confirm.";
			response.sendRedirect(
					"" + SMUtilities.getURLLinkBase(getServletContext()) + "" + sCallingClass + "?"
					+ "Warning=" + m_sWarning
					+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
			);
			return;
	    }

	    try{
	    	if(!clsDatabaseFunctions.start_data_transaction(conn)){
	    		clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1547067514]");
	    		m_sWarning = "Could not start data transaction.";
				response.sendRedirect(
						"" + SMUtilities.getURLLinkBase(getServletContext()) + "" + sCallingClass + "?"
						+ "Warning=" + m_sWarning
						+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
				);
				return;
	    	}
	    	
	    	//Execute deletes here:
	    	String SQL = "delete " 
	    		+ SMEntryBatch.TableName 
	    		+ ", " + SMTableentries.TableName
	    		+ ", " + SMTableentrylines.TableName 
	    		+ " from "
	    		+ SMEntryBatch.TableName 
	    		+ ", " + SMTableentries.TableName
	    		+ ", " + SMTableentrylines.TableName 
	    		+ " where ("
	    			+ SMEntryBatch.TableName + "." + SMEntryBatch.datpostdate + " <= '"
	    			+ clsDateAndTimeConversions.utilDateToString(datClearingDate, "yyyy-MM-dd") + " 23:59:59'"
	    			+ " AND ("
	    				+ "(" + SMEntryBatch.TableName + "." + SMEntryBatch.ibatchstatus 
	    					+ " = " + SMBatchStatuses.POSTED + ")"
	    				+ " OR (" + SMEntryBatch.TableName + "." + SMEntryBatch.ibatchstatus
	    					+ " = " + SMBatchStatuses.DELETED + ")"
	    				+ ")"
	    			+ " AND (" + SMTableentries.TableName + "." + SMTableentries.ibatchnumber 
	    				+ " = " + SMEntryBatch.TableName + "." + SMEntryBatch.ibatchnumber + ")"
	    			+ " AND (" + SMTableentrylines.TableName + "." + SMTableentrylines.ibatchnumber
	    				+ " = " + SMEntryBatch.TableName + "." + SMEntryBatch.ibatchnumber  + ")"
	    		+ ")"
	    		;
	    	//System.out.println("In " + this.toString() + " SQL = " + SQL);
	    	if(!clsDatabaseFunctions.executeSQL(SQL, conn)){
	    		clsDatabaseFunctions.rollback_data_transaction(conn);
	    		clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1547067515]");
	    		m_sWarning = "Could not execute delete statement.";
				response.sendRedirect(
						"" + SMUtilities.getURLLinkBase(getServletContext()) + "" + sCallingClass + "?"
						+ "Warning=" + m_sWarning
						+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
				);
				return;
	    	}
	    	
	    	//Now delete any batches that had no entries (the previous SQL statement would leave those):
	    	SQL = "delete from " 
	    		+ SMEntryBatch.TableName 
	    		+ " where ("
	    			+ "(" + SMEntryBatch.TableName + "." + SMEntryBatch.datpostdate + " <= '"
	    			+ clsDateAndTimeConversions.utilDateToString(datClearingDate, "yyyy-MM-dd") + " 23:59:59')"
	    			+ " AND ("
	    				+ "(" + SMEntryBatch.TableName + "." + SMEntryBatch.ibatchstatus 
	    					+ " = " + SMBatchStatuses.POSTED + ")"
	    				+ " OR (" + SMEntryBatch.TableName + "." + SMEntryBatch.ibatchstatus
	    					+ " = " + SMBatchStatuses.DELETED + ")"
	    				+ ")"
	    		+ ")"
	    		;
	    	//System.out.println("In " + this.toString() + " second SQL = " + SQL);
	    	if(!clsDatabaseFunctions.executeSQL(SQL, conn)){
	    		clsDatabaseFunctions.rollback_data_transaction(conn);
	    		clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1547067516]");
	    		m_sWarning = "Could not delete empty batches in the range.";
				response.sendRedirect(
						"" + SMUtilities.getURLLinkBase(getServletContext()) + "" + sCallingClass + "?"
						+ "Warning=" + m_sWarning
						+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
				);
				return;
	    	}
	    	
	    	//Finally clear any GL export records for any batches that no longer remain:
			SQL = "DELETE GLEXHEAD.* FROM " + SMTableglexportheaders.TableName + " GLEXHEAD"
				+ " LEFT JOIN " + SMEntryBatch.TableName + " ARBATCH ON ("
				+ "(GLEXHEAD." + SMTableglexportheaders.lbatchnumber + " = ARBATCH." + SMEntryBatch.ibatchnumber + ")"
				+ " AND (GLEXHEAD." + SMTableglexportheaders.ssourceledger + " = ARBATCH." + SMEntryBatch.smoduletype + ")"
				+ ")"
				+ " WHERE ("
					+ "(ARBATCH." + SMEntryBatch.ibatchnumber + " IS NULL)"
					+ " AND (GLEXHEAD." + SMTableglexportheaders.ssourceledger + " = '" + SMModuleTypes.AR + "')"
				+ ")"
	    	;
	    	if(!clsDatabaseFunctions.executeSQL(SQL, conn)){
	    		clsDatabaseFunctions.rollback_data_transaction(conn);
	    		clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1547067517]");
	    		m_sWarning = "Could not delete GL Export Header records in the range.";
				response.sendRedirect(
						"" + SMUtilities.getURLLinkBase(getServletContext()) + "" + sCallingClass + "?"
						+ "Warning=" + m_sWarning
						+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
				);
				return;
	    	}
	    	
			SQL = "DELETE GLEXDETAIL.* FROM " + SMTableglexportdetails.TableName + " GLEXDETAIL"
					+ " LEFT JOIN " + SMEntryBatch.TableName + " ARBATCH ON ("
					+ "(GLEXDETAIL." + SMTableglexportdetails.lbatchnumber + " = ARBATCH." + SMEntryBatch.ibatchnumber + ")"
					+ " AND (GLEXDETAIL." + SMTableglexportdetails.sdetailsourceledger + " = ARBATCH." + SMEntryBatch.smoduletype + ")"
					+ ")"
					+ " WHERE ("
						+ "(ARBATCH." + SMEntryBatch.ibatchnumber + " IS NULL)"
						+ " AND (GLEXDETAIL." + SMTableglexportdetails.sdetailsourceledger + " = '" + SMModuleTypes.AR + "')"
					+ ")"
		    	;
	    	if(!clsDatabaseFunctions.executeSQL(SQL, conn)){
	    		clsDatabaseFunctions.rollback_data_transaction(conn);
	    		clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1547067518]");
	    		m_sWarning = "Could not delete GL Export Detail records in the range.";
				response.sendRedirect(
						"" + SMUtilities.getURLLinkBase(getServletContext()) + "" + sCallingClass + "?"
						+ "Warning=" + m_sWarning
						+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
				);
				return;
	    	}
	    	
	    	if(!clsDatabaseFunctions.commit_data_transaction(conn)){
	    		clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1547067519]");
	    		m_sWarning = "Could not commit data transaction.";
				response.sendRedirect(
						"" + SMUtilities.getURLLinkBase(getServletContext()) + "" + sCallingClass + "?"
						+ "Warning=" + m_sWarning
						+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
				);
				return;
	    	}
	    	
	    }catch(SQLException e){
	    	m_sWarning = "Error deleting batches - " + e.getMessage();
	    	clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1547067520]");
			response.sendRedirect(
					"" + SMUtilities.getURLLinkBase(getServletContext()) + "" + sCallingClass + "?"
					+ "Warning=" + m_sWarning
					+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
			);
			return;
	    }
	    clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1547067521]");
		m_sWarning = "Posted and deleted batches with posting dates up to and including " + sClearingDate + " were cleared.";
		response.sendRedirect(
				"" + SMUtilities.getURLLinkBase(getServletContext()) + "" + sCallingClass + "?"
				+ "Warning=" + m_sWarning
				+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
		);
		return;
    }
	public void doGet(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
		
		doPost(request, response);
	}
}
