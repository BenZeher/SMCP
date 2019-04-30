package smgl;

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
import SMClasses.SMLogEntry;
import SMDataDefinition.SMTablegltransactionbatchentries;
import SMDataDefinition.SMTablegltransactionbatchlines;
import SMDataDefinition.SMTablegltransactionbatches;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsDateAndTimeConversions;
import ServletUtilities.clsManageRequestParameters;
import smcontrolpanel.SMAuthenticate;
import smcontrolpanel.SMSystemFunctions;
import smcontrolpanel.SMUtilities;

public class GLClearPostedBatchesAction extends HttpServlet{

	private static final long serialVersionUID = 1L;
	public void doPost(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
	
		String sCallingClass = clsManageRequestParameters.get_Request_Parameter("CallingClass", request);
	
		if (!SMAuthenticate.authenticateSMCPCredentials(
				request, 
				response, 
				getServletContext(), 
				SMSystemFunctions.GLClearPostedAndDeletedBatches))
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
			m_sWarning = "Error [1556648232] - Unable to get data connection.";
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
			clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1556648234]");
	    	m_sWarning = "Error [1556648233] - Invalid clearing date: '" + sClearingDate + "' - " + e1.getMessage();
			response.sendRedirect(
					"" + SMUtilities.getURLLinkBase(getServletContext()) + "" + sCallingClass + "?"
					+ "" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
					+ "&Warning=" + m_sWarning
			);
			return;
		}
		
	    if (request.getParameter("ConfirmClear") == null){
	    	clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1556648235]");
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
	    		clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1556648236]");
	    		m_sWarning = "Error [1556648237] - Could not start data transaction.";
				response.sendRedirect(
						"" + SMUtilities.getURLLinkBase(getServletContext()) + "" + sCallingClass + "?"
						+ "Warning=" + m_sWarning
						+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
				);
				return;
	    	}
	    	
	    	//Execute deletes here:
	    	String SQL = "DELETE " 
	    		+ SMTablegltransactionbatches.TableName 
	    		+ ", " + SMTablegltransactionbatchentries.TableName
	    		+ ", " + SMTablegltransactionbatchlines.TableName
	    		+ " FROM "
	    		+ SMTablegltransactionbatches.TableName 
	    		+ ", " + SMTablegltransactionbatchentries.TableName
	    		+ ", " + SMTablegltransactionbatchlines.TableName 
	    		+ " WHERE ("

	    			//Link the entries and lines to the batches:
	    			+ " (" + SMTablegltransactionbatchentries.TableName + "." + SMTablegltransactionbatchentries.lbatchnumber 
	    				+ " = " + SMTablegltransactionbatches.TableName + "." + SMTablegltransactionbatches.lbatchnumber + ")"
	    			+ " AND (" + SMTablegltransactionbatchlines.TableName + "." + SMTablegltransactionbatchlines.lbatchnumber
	    				+ " = " + SMTablegltransactionbatches.TableName + "." + SMTablegltransactionbatches.lbatchnumber  + ")"
	    		
	    			//Now qualify by status and date:
	    			+ " AND ("
	    		
		    			//Pick up the POSTED batches:
		    			+ "("
		    				+ "(" + SMTablegltransactionbatches.TableName + "." + SMTablegltransactionbatches.datpostdate + " <= '"
			    				+ clsDateAndTimeConversions.utilDateToString(datClearingDate, "yyyy-MM-dd") + " 23:59:59')"
			    			+ " AND (" + SMTablegltransactionbatches.TableName + "." + SMTablegltransactionbatches.ibatchstatus + " = " + SMBatchStatuses.POSTED + ")"
		    			+ ")"
		    			
		    			//Pick up the deleted batches
		    			+ " OR ("
	    					+ "(" + SMTablegltransactionbatches.TableName + "." + SMTablegltransactionbatches.datbatchdate + " <= '"
	    					+ clsDateAndTimeConversions.utilDateToString(datClearingDate, "yyyy-MM-dd") + "')"
	    					+ " AND (" + SMTablegltransactionbatches.TableName + "." + SMTablegltransactionbatches.ibatchstatus + " = " + SMBatchStatuses.DELETED + ")"
		    			+ ")"
    					
    				+ ")"	

	    		+ ")"
	    		;
	    	//System.out.println("[1489678049] " + this.toString() + " SQL = " + SQL);
	    	if(!clsDatabaseFunctions.executeSQL(SQL, conn)){
	    		clsDatabaseFunctions.rollback_data_transaction(conn);
	    		clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1556648238]");
	    		m_sWarning = "Error [1556648239] - Could not execute delete statement with SQL: " + SQL + ".";
				response.sendRedirect(
						"" + SMUtilities.getURLLinkBase(getServletContext()) + "" + sCallingClass + "?"
						+ "Warning=" + m_sWarning
						+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
				);
				return;
	    	}
	    	
	    	//Now delete any batches that had no entries (the previous SQL statement would leave those):
	    	SQL = "DELETE FROM " 
	    		+ SMTablegltransactionbatches.TableName 
	    		+ " WHERE ("
	    			+ "(" + SMTablegltransactionbatches.TableName + "." + SMTablegltransactionbatches.datpostdate + " <= '"
	    			+ clsDateAndTimeConversions.utilDateToString(datClearingDate, "yyyy-MM-dd") + " 23:59:59')"
	    			+ " AND ("
	    				+ "(" + SMTablegltransactionbatches.TableName + "." + SMTablegltransactionbatches.ibatchstatus 
	    					+ " = " + SMBatchStatuses.POSTED + ")"
	    				+ " OR (" + SMTablegltransactionbatches.TableName + "." + SMTablegltransactionbatches.ibatchstatus
	    					+ " = " + SMBatchStatuses.DELETED + ")"
	    				+ ")"
	    		+ ")"
	    		;
	    	//System.out.println("In " + this.toString() + " second SQL = " + SQL);
	    	if(!clsDatabaseFunctions.executeSQL(SQL, conn)){
	    		clsDatabaseFunctions.rollback_data_transaction(conn);
	    		clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1556648240]");
	    		m_sWarning = "Error [1556648241] - Could not delete empty batches in the range.";
				response.sendRedirect(
						"" + SMUtilities.getURLLinkBase(getServletContext()) + "" + sCallingClass + "?"
						+ "Warning=" + m_sWarning
						+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
				);
				return;
	    	}
	    	
	    	if(!clsDatabaseFunctions.commit_data_transaction(conn)){
	    		clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1556648246]");
	    		m_sWarning = "Error [1556648247] - Could not commit data transaction.";
				response.sendRedirect(
						"" + SMUtilities.getURLLinkBase(getServletContext()) + "" + sCallingClass + "?"
						+ "Warning=" + m_sWarning
						+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
				);
				return;
	    	}
	    	
	    }catch(SQLException e){
	    	m_sWarning = "Error [1556648248] - deleting batches - " + e.getMessage();
	    	clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1556648249]");
			response.sendRedirect(
					"" + SMUtilities.getURLLinkBase(getServletContext()) + "" + sCallingClass + "?"
					+ "Warning=" + m_sWarning
					+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
			);
			return;
	    }
	    clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1556648250]");
	    SMLogEntry log = new SMLogEntry(sDBID, getServletContext());
	    log.writeEntry(
	    	sUserID, 
	    	SMLogEntry.LOG_OPERATION_GLCLEARPOSTEDBATCHES,
	    	"Successfully cleared posted and deleted GL batches",
	    	"Clearing date: '" + datClearingDate + "'",
	    	"[1556648251]");
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
