package smic;

import java.io.IOException;
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
import SMClasses.SMModuleTypes;
import SMDataDefinition.SMTableglexportdetails;
import SMDataDefinition.SMTableglexportheaders;
import SMDataDefinition.SMTableicbatchentries;
import SMDataDefinition.SMTableicentrylines;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsDateAndTimeConversions;
import ServletUtilities.clsManageRequestParameters;
import ServletUtilities.clsServletUtilities;

public class ICClearPostedBatchesAction extends HttpServlet{

	private static final long serialVersionUID = 1L;
	private static String m_sWarning = "";
	private static String sUserID = "";
	private static String sUserFullName = "";
	private static String sDBID = "";
	public void doPost(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
	
		String sCallingClass = clsManageRequestParameters.get_Request_Parameter("CallingClass", request);
	
		if (!SMAuthenticate.authenticateSMCPCredentials(
				request, 
				response, 
				getServletContext(), 
				SMSystemFunctions.ICClearPostedBatches))
		{
			return;
		}

	    //Get the session info:
	    HttpSession CurrentSession = request.getSession(true);
	    sUserID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERID);
	    sUserFullName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERFIRSTNAME) + " "
	    		+ (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERLASTNAME);
	    sDBID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_DATABASE_ID);
	    //Need this for a data transaction:
		Connection conn = clsDatabaseFunctions.getConnection(
				getServletContext(), 
				(String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_DATABASE_ID),
				"MySQL",
				this.toString() + ".doPost - User: " + sUserID
				+ " - "
				+ sUserFullName
		);
		if (conn == null){
			m_sWarning = clsServletUtilities.URLEncode("Unable to get data connection.");
			response.sendRedirect(
					"" + SMUtilities.getURLLinkBase(getServletContext()) + "" + sCallingClass + "?"
					+ "Warning=" + m_sWarning
					+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
			);			
	    	return;
		}
		String sClearingDate = clsManageRequestParameters.get_Request_Parameter("ClearingDate", request);
	 
    	//Convert the date to a SQL one:
    	java.sql.Date datClearingDate = null;
		try {
			datClearingDate = clsDateAndTimeConversions.StringTojavaSQLDate("M/d/yyyy", sClearingDate);
		} catch (ParseException e1) {
			clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1547080786]");
	    	m_sWarning = clsServletUtilities.URLEncode("Error:[1423766702] Invalid clearing date: '" + sClearingDate + "' - " + e1.getMessage());
			response.sendRedirect(
					"" + SMUtilities.getURLLinkBase(getServletContext()) + "" + sCallingClass + "?"
					+ "" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
					+ "&Warning=" + m_sWarning
			);
			return;
		}
		
	    if (request.getParameter("ConfirmClear") == null){
	    	clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1547080787]");
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
	    		clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1547080788]");
	    		m_sWarning = clsServletUtilities.URLEncode("Could not start data transaction.");
				response.sendRedirect(
						"" + SMUtilities.getURLLinkBase(getServletContext()) + "" + sCallingClass + "?"
						+ "Warning=" + m_sWarning
						+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
				);
				return;
	    	}
	    	
	    	//Execute deletes here:
	    	String SQL = "delete " 
	    		+ ICEntryBatch.TableName 
	    		+ ", " + SMTableicbatchentries.TableName
	    		+ ", " + SMTableicentrylines.TableName
	    		+ " from "
	    		+ ICEntryBatch.TableName 
	    		+ ", " + SMTableicbatchentries.TableName
	    		+ ", " + SMTableicentrylines.TableName 
	    		+ " where ("
	    			+ ICEntryBatch.TableName + "." + ICEntryBatch.datpostdate + " <= '"
	    			+ clsDateAndTimeConversions.utilDateToString(datClearingDate, "yyyy-MM-dd") + " 23:59:59'"
	    			+ " AND ("
	    				+ "(" + ICEntryBatch.TableName + "." + ICEntryBatch.ibatchstatus 
	    					+ " = " + SMBatchStatuses.POSTED + ")"
	    				+ " OR (" + ICEntryBatch.TableName + "." + ICEntryBatch.ibatchstatus
	    					+ " = " + SMBatchStatuses.DELETED + ")"
	    				+ ")"
	    			+ " AND (" + SMTableicbatchentries.TableName + "." + SMTableicbatchentries.lbatchnumber 
	    				+ " = " + ICEntryBatch.TableName + "." + ICEntryBatch.lbatchnumber + ")"
	    			+ " AND (" + SMTableicentrylines.TableName + "." + SMTableicentrylines.lbatchnumber
	    				+ " = " + ICEntryBatch.TableName + "." + ICEntryBatch.lbatchnumber  + ")"
	    		+ ")"
	    		;
	    	//System.out.println("In " + this.toString() + " SQL = " + SQL);
	    	if(!clsDatabaseFunctions.executeSQL(SQL, conn)){
	    		clsDatabaseFunctions.rollback_data_transaction(conn);
	    		clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1547080789]");
	    		m_sWarning = clsServletUtilities.URLEncode("Could not execute delete statement.");
				response.sendRedirect(
						"" + SMUtilities.getURLLinkBase(getServletContext()) + "" + sCallingClass + "?"
						+ "Warning=" + m_sWarning
						+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
				);
				return;
	    	}
	    	
	    	//Now delete any batches that had no entries (the previous SQL statement would leave those):
	    	SQL = "delete from " 
	    		+ ICEntryBatch.TableName 
	    		+ " where ("
	    			+ "(" + ICEntryBatch.TableName + "." + ICEntryBatch.datpostdate + " <= '"
	    			+ clsDateAndTimeConversions.utilDateToString(datClearingDate, "yyyy-MM-dd") + " 23:59:59')"
	    			+ " AND ("
	    				+ "(" + ICEntryBatch.TableName + "." + ICEntryBatch.ibatchstatus 
	    					+ " = " + SMBatchStatuses.POSTED + ")"
	    				+ " OR (" + ICEntryBatch.TableName + "." + ICEntryBatch.ibatchstatus
	    					+ " = " + SMBatchStatuses.DELETED + ")"
	    				+ ")"
	    		+ ")"
	    		;
	    	//System.out.println("In " + this.toString() + " second SQL = " + SQL);
	    	if(!clsDatabaseFunctions.executeSQL(SQL, conn)){
	    		clsDatabaseFunctions.rollback_data_transaction(conn);
	    		clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1547080790]");
	    		m_sWarning = clsServletUtilities.URLEncode("Could not delete empty batches in the range.");
				response.sendRedirect(
						"" + SMUtilities.getURLLinkBase(getServletContext()) + "" + sCallingClass + "?"
						+ "Warning=" + m_sWarning
						+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
				);
				return;
	    	}
	    	
	    	//Delete any GL export header or detail records for these batches:
			SQL = "DELETE GLEXHEAD.* FROM " + SMTableglexportheaders.TableName + " GLEXHEAD"
					+ " LEFT JOIN " + ICEntryBatch.TableName + " ICBATCH ON ("
					+ "(GLEXHEAD." + SMTableglexportheaders.lbatchnumber + " = ICBATCH." + ICEntryBatch.lbatchnumber + ")"
					+ " AND (GLEXHEAD." + SMTableglexportheaders.ssourceledger + " = ICBATCH." + ICEntryBatch.smoduletype + ")"
					+ ")"
					+ " WHERE ("
						+ "(ICBATCH." + ICEntryBatch.lbatchnumber + " IS NULL)"
						+ " AND (GLEXHEAD." + SMTableglexportheaders.ssourceledger + " = '" + SMModuleTypes.IC + "')"
					+ ")"
		    	;
		    	if(!clsDatabaseFunctions.executeSQL(SQL, conn)){
		    		clsDatabaseFunctions.rollback_data_transaction(conn);
		    		clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1547080791]");
		    		m_sWarning = clsServletUtilities.URLEncode("Could not delete GL Export Header records in the range.");
					response.sendRedirect(
							"" + SMUtilities.getURLLinkBase(getServletContext()) + "" + sCallingClass + "?"
							+ "Warning=" + m_sWarning
							+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
					);
					return;
		    	}
		    	
			SQL = "DELETE GLEXDETAIL.* FROM " + SMTableglexportdetails.TableName + " GLEXDETAIL"
					+ " LEFT JOIN " + ICEntryBatch.TableName + " ICBATCH ON ("
					+ "(GLEXDETAIL." + SMTableglexportdetails.lbatchnumber + " = ICBATCH." + ICEntryBatch.lbatchnumber + ")"
					+ " AND (GLEXDETAIL." + SMTableglexportdetails.sdetailsourceledger + " = ICBATCH." + ICEntryBatch.smoduletype + ")"
					+ ")"
					+ " WHERE ("
						+ "(ICBATCH." + ICEntryBatch.lbatchnumber + " IS NULL)"
						+ " AND (GLEXDETAIL." + SMTableglexportdetails.sdetailsourceledger + " = '" + SMModuleTypes.IC + "')"
					+ ")"

		    	;
	    	if(!clsDatabaseFunctions.executeSQL(SQL, conn)){
	    		clsDatabaseFunctions.rollback_data_transaction(conn);
	    		clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1547080792]");
	    		m_sWarning = clsServletUtilities.URLEncode("Could not delete GL Export Detail records in the range.");
				response.sendRedirect(
						"" + SMUtilities.getURLLinkBase(getServletContext()) + "" + sCallingClass + "?"
						+ "Warning=" + m_sWarning
						+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
				);
				return;
	    	}
	    	
	    	if(!clsDatabaseFunctions.commit_data_transaction(conn)){
	    		clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1547080793]");
	    		m_sWarning = clsServletUtilities.URLEncode("Could not commit data transaction.");
				response.sendRedirect(
						"" + SMUtilities.getURLLinkBase(getServletContext()) + "" + sCallingClass + "?"
						+ "Warning=" + m_sWarning
						+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
				);
				return;
	    	}
	    	
	    }catch(SQLException e){
	    	m_sWarning = clsServletUtilities.URLEncode("Error deleting batches - " + e.getMessage());
	    	clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1547080794]");
			response.sendRedirect(
					"" + SMUtilities.getURLLinkBase(getServletContext()) + "" + sCallingClass + "?"
					+ "Warning=" + m_sWarning
					+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
			);
			return;
	    }
	    clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1547080795]");
		m_sWarning = clsServletUtilities.URLEncode("Posted and deleted batches with posting dates up to and including " + sClearingDate + " were cleared.");
		response.sendRedirect(
				"" + SMUtilities.getURLLinkBase(getServletContext()) + "" + sCallingClass + "?"
				+ "Status=" + m_sWarning
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
