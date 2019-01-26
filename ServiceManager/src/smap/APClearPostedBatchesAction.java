package smap;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.ParseException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import SMClasses.SMBatchStatuses;
import SMClasses.SMModuleTypes;
import SMDataDefinition.SMTableapbatchentries;
import SMDataDefinition.SMTableapbatchentrylines;
import SMDataDefinition.SMTableapbatches;
import SMDataDefinition.SMTableglexportdetails;
import SMDataDefinition.SMTableglexportheaders;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsDateAndTimeConversions;
import ServletUtilities.clsManageRequestParameters;
import smcontrolpanel.SMAuthenticate;
import smcontrolpanel.SMSystemFunctions;
import smcontrolpanel.SMUtilities;

public class APClearPostedBatchesAction extends HttpServlet{

	private static final long serialVersionUID = 1L;
	public void doPost(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
	
		String sCallingClass = clsManageRequestParameters.get_Request_Parameter("CallingClass", request);
	
		if (!SMAuthenticate.authenticateSMCPCredentials(
				request, 
				response, 
				getServletContext(), 
				SMSystemFunctions.APClearPostedAndDeletedBatches))
		{
			return;
		}

	    //Get the session info:
	    HttpSession CurrentSession = request.getSession(true);
	    String sUserID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERID);
	    String sUserFirstName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERFIRSTNAME);
	    String sUserLastName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERLASTNAME);
	    String sDBID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_DATABASE_ID);
	    String m_sWarning = "";
	    
	    //Need this for a data transaction:
		Connection conn = clsDatabaseFunctions.getConnection(
				getServletContext(), 
				sDBID,
				"MySQL",
				this.toString() 
				+ ".doPost - User: " 
				+ sUserID
				+ " - "
				+ sUserFirstName
				+ " "
				+ sUserLastName
		);
		if (conn == null){
			m_sWarning = "Error [1489596340] - Unable to get data connection.";
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
			clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1546998964]");
	    	m_sWarning = "Error [1489596341] - Invalid clearing date: '" + sClearingDate + "' - " + e1.getMessage();
			response.sendRedirect(
					"" + SMUtilities.getURLLinkBase(getServletContext()) + "" + sCallingClass + "?"
					+ "" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
					+ "&Warning=" + m_sWarning
			);
			return;
		}
		
	    if (request.getParameter("ConfirmClear") == null){
	    	clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1546998965]");
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
	    		clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1546998966]");
	    		m_sWarning = "Error [1489596342] - Could not start data transaction.";
				response.sendRedirect(
						"" + SMUtilities.getURLLinkBase(getServletContext()) + "" + sCallingClass + "?"
						+ "Warning=" + m_sWarning
						+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
				);
				return;
	    	}
	    	
	    	//Execute deletes here:
	    	String SQL = "DELETE " 
	    		+ SMTableapbatches.TableName 
	    		+ ", " + SMTableapbatchentries.TableName
	    		+ ", " + SMTableapbatchentrylines.TableName
	    		+ " FROM "
	    		+ SMTableapbatches.TableName 
	    		+ ", " + SMTableapbatchentries.TableName
	    		+ ", " + SMTableapbatchentrylines.TableName 
	    		+ " WHERE ("

	    			//Link the entries and lines to the batches:
	    			+ " (" + SMTableapbatchentries.TableName + "." + SMTableapbatchentries.lbatchnumber 
	    				+ " = " + SMTableapbatches.TableName + "." + SMTableapbatches.lbatchnumber + ")"
	    			+ " AND (" + SMTableapbatchentrylines.TableName + "." + SMTableapbatchentrylines.lbatchnumber
	    				+ " = " + SMTableapbatches.TableName + "." + SMTableapbatches.lbatchnumber  + ")"
	    		
	    			//Now qualify by status and date:
	    			+ " AND ("
	    		
		    			//Pick up the POSTED batches:
		    			+ "("
		    				+ "(" + SMTableapbatches.TableName + "." + SMTableapbatches.datpostdate + " <= '"
			    				+ clsDateAndTimeConversions.utilDateToString(datClearingDate, "yyyy-MM-dd") + " 23:59:59')"
			    			+ " AND (" + SMTableapbatches.TableName + "." + SMTableapbatches.ibatchstatus + " = " + SMBatchStatuses.POSTED + ")"
		    			+ ")"
		    			
		    			//Pick up the deleted batches
		    			+ " OR ("
	    					+ "(" + SMTableapbatches.TableName + "." + SMTableapbatches.datbatchdate + " <= '"
	    					+ clsDateAndTimeConversions.utilDateToString(datClearingDate, "yyyy-MM-dd") + "')"
	    					+ " AND (" + SMTableapbatches.TableName + "." + SMTableapbatches.ibatchstatus + " = " + SMBatchStatuses.DELETED + ")"
		    			+ ")"
    					
    				+ ")"	

	    		+ ")"
	    		;
	    	//System.out.println("[1489678049] " + this.toString() + " SQL = " + SQL);
	    	if(!clsDatabaseFunctions.executeSQL(SQL, conn)){
	    		clsDatabaseFunctions.rollback_data_transaction(conn);
	    		clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1546998967]");
	    		m_sWarning = "Error [1489596767] - Could not execute delete statement with SQL: " + SQL + ".";
				response.sendRedirect(
						"" + SMUtilities.getURLLinkBase(getServletContext()) + "" + sCallingClass + "?"
						+ "Warning=" + m_sWarning
						+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
				);
				return;
	    	}
	    	
	    	//Now delete any batches that had no entries (the previous SQL statement would leave those):
	    	SQL = "DELETE FROM " 
	    		+ SMTableapbatches.TableName 
	    		+ " WHERE ("
	    			+ "(" + SMTableapbatches.TableName + "." + SMTableapbatches.datpostdate + " <= '"
	    			+ clsDateAndTimeConversions.utilDateToString(datClearingDate, "yyyy-MM-dd") + " 23:59:59')"
	    			+ " AND ("
	    				+ "(" + SMTableapbatches.TableName + "." + SMTableapbatches.ibatchstatus 
	    					+ " = " + SMBatchStatuses.POSTED + ")"
	    				+ " OR (" + SMTableapbatches.TableName + "." + SMTableapbatches.ibatchstatus
	    					+ " = " + SMBatchStatuses.DELETED + ")"
	    				+ ")"
	    		+ ")"
	    		;
	    	//System.out.println("In " + this.toString() + " second SQL = " + SQL);
	    	if(!clsDatabaseFunctions.executeSQL(SQL, conn)){
	    		clsDatabaseFunctions.rollback_data_transaction(conn);
	    		clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1546998968]");
	    		m_sWarning = "Error [1489596768] - Could not delete empty batches in the range.";
				response.sendRedirect(
						"" + SMUtilities.getURLLinkBase(getServletContext()) + "" + sCallingClass + "?"
						+ "Warning=" + m_sWarning
						+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
				);
				return;
	    	}
	    	
	    	//Delete any GL export header or detail records for these batches:
			SQL = "DELETE GLEXHEAD.* FROM " + SMTableglexportheaders.TableName + " GLEXHEAD"
					+ " LEFT JOIN " + SMTableapbatches.TableName + " APBATCH ON ("
					+ "(GLEXHEAD." + SMTableglexportheaders.lbatchnumber + " = APBATCH." + SMTableapbatches.lbatchnumber + ")"
					+ " AND (GLEXHEAD." + SMTableglexportheaders.ssourceledger + " = '" + SMModuleTypes.AP + "')"
					+ ")"
					+ " WHERE ("
						+ "(APBATCH." + SMTableapbatches.lbatchnumber + " IS NULL)"
						+ " AND (GLEXHEAD." + SMTableglexportheaders.ssourceledger + " = '" + SMModuleTypes.AP + "')"
					+ ")"
		    	;
		    	if(!clsDatabaseFunctions.executeSQL(SQL, conn)){
		    		clsDatabaseFunctions.rollback_data_transaction(conn);
		    		clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1546998969]");
		    		m_sWarning = "Error [1489596926] - Could not delete GL Export Header records in the range.";
					response.sendRedirect(
							"" + SMUtilities.getURLLinkBase(getServletContext()) + "" + sCallingClass + "?"
							+ "Warning=" + m_sWarning
							+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
					);
					return;
		    	}
		    	
			SQL = "DELETE GLEXDETAIL.* FROM " + SMTableglexportdetails.TableName + " GLEXDETAIL"
					+ " LEFT JOIN " + SMTableapbatches.TableName + " APBATCH ON ("
					+ "(GLEXDETAIL." + SMTableglexportdetails.lbatchnumber + " = APBATCH." + SMTableapbatches.lbatchnumber + ")"
					+ " AND (GLEXDETAIL." + SMTableglexportdetails.sdetailsourceledger + " = '" + SMModuleTypes.AP + "')"
					+ ")"
					+ " WHERE ("
						+ "(APBATCH." + SMTableapbatches.lbatchnumber + " IS NULL)"
						+ " AND (GLEXDETAIL." + SMTableglexportdetails.sdetailsourceledger + " = '" + SMModuleTypes.AP + "')"
					+ ")"
		    	;
	    	if(!clsDatabaseFunctions.executeSQL(SQL, conn)){
	    		clsDatabaseFunctions.rollback_data_transaction(conn);
	    		clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1546998970]");
	    		m_sWarning = "Error [1489597008] - Could not delete GL Export Detail records in the range.";
				response.sendRedirect(
						"" + SMUtilities.getURLLinkBase(getServletContext()) + "" + sCallingClass + "?"
						+ "Warning=" + m_sWarning
						+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
				);
				return;
	    	}
	    	
	    	if(!clsDatabaseFunctions.commit_data_transaction(conn)){
	    		clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1546998971]");
	    		m_sWarning = "Error [1489597009] - Could not commit data transaction.";
				response.sendRedirect(
						"" + SMUtilities.getURLLinkBase(getServletContext()) + "" + sCallingClass + "?"
						+ "Warning=" + m_sWarning
						+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
				);
				return;
	    	}
	    	
	    }catch(SQLException e){
	    	m_sWarning = "Error [1489597010] - deleting batches - " + e.getMessage();
	    	clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1546998972]");
			response.sendRedirect(
					"" + SMUtilities.getURLLinkBase(getServletContext()) + "" + sCallingClass + "?"
					+ "Warning=" + m_sWarning
					+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
			);
			return;
	    }
	    clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1546998973]");
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
