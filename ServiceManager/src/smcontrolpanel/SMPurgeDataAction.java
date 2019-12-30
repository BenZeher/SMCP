package smcontrolpanel;

import SMClasses.*;
import ServletUtilities.clsServletUtilities;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsDateAndTimeConversions;
import ServletUtilities.clsManageRequestParameters;

import java.io.IOException;
import java.sql.Connection;
import java.text.ParseException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

public class SMPurgeDataAction extends HttpServlet{

	private static final long serialVersionUID = 1L;
	public void doPost(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
	
		String sCallingClass = clsManageRequestParameters.get_Request_Parameter("CallingClass", request);
		String sPurgeDeadline = clsManageRequestParameters.get_Request_Parameter("PurgeDeadline", request);
		
		if (!SMAuthenticate.authenticateSMCPCredentials(
				request, 
				response, 
				getServletContext(), 
				SMSystemFunctions.SMPurgeData))
		{
			return;
		}

	    //Get the session info:
	    HttpSession CurrentSession = request.getSession(true);
	    String sDBID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_DATABASE_ID);
	    String sUserID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERID);
	    String sUserFirstName = (String)CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERFIRSTNAME);
	    String sUserLastName = (String)CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERLASTNAME);
	    String m_sWarning = "";
    	//Convert the date to a SQL one:
    	java.sql.Date datPurgeDeadline = null;
		try {
			datPurgeDeadline = clsDateAndTimeConversions.StringTojavaSQLDate("M/d/yyyy", sPurgeDeadline);
		} catch (ParseException e1) {
			m_sWarning = "Error:[1423661739] Invalid Purge Deadline: '" + datPurgeDeadline + "' - " + e1.getMessage();
			response.sendRedirect(
					"" + SMUtilities.getURLLinkBase(getServletContext()) + "" + sCallingClass + "?"
					+ "" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
					+ "&Warning=" + m_sWarning
			);
		}
	    if (request.getParameter("ConfirmPurge") == null){
	    	m_sWarning = "You chose to purge, but did not check the box to confirm.";
			response.sendRedirect(
					"" + SMUtilities.getURLLinkBase(getServletContext()) + "" + sCallingClass + "?"
					+ "" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
					+ "&Warning=" + m_sWarning
			);
			return;
	    }
	    
		//Log the process:
		SMLogEntry log = new SMLogEntry(sDBID, getServletContext());
		log.writeEntry(
			sUserID, 
			SMLogEntry.LOG_OPERATION_SMPURGEDATA, 
			"Attempting to purge data using purge deadline date " + clsDateAndTimeConversions.utilDateToString(datPurgeDeadline, "MM/dd/yyyy") + ".",
			"Purge orders: " + (request.getParameter(SMPurgeDataSelection.PURGE_ORDERS) != null)
			+ ", Purge customer call logs: " + (request.getParameter(SMPurgeDataSelection.PURGE_CUSTOMERCALLLOGS) != null)
			+ ", Purge customer " + SMBidEntry.ParamObjectName.toLowerCase() + "s: " + (request.getParameter(SMPurgeDataSelection.PURGE_BIDS) != null)
			+ ", Purge customer sales contacts: " + (request.getParameter(SMPurgeDataSelection.PURGE_SALESCONTACTS) != null)
			+ ", Purge system logs: " + (request.getParameter(SMPurgeDataSelection.PURGE_SYSTEMLOG) != null)
			+ ", Purge material returns: " + (request.getParameter(SMPurgeDataSelection.PURGE_MATERIALRETURNS) != null)
			+ ", Purge General Ledger: " + (request.getParameter(SMPurgeDataSelection.PURGE_GLDATA) != null)
			+ ", Purge security system logs: " + (request.getParameter(SMPurgeDataSelection.PURGE_SECURITYSYSTEMLOGS) != null),
			"[1376509351]"
		);
		
		Connection conn = clsDatabaseFunctions.getConnection(
				getServletContext(), 
				sDBID, 
				"MySQL", 
				SMUtilities.getFullClassName(this.toString() 
						+ ".doPost - user: " 
						+ sUserID
						+ " - "
						+ sUserFirstName
						+ " "
						+ sUserLastName
						)
			);
			
		if (conn == null){
	    	m_sWarning = "Could not open connection.";
			response.sendRedirect(
					"" + SMUtilities.getURLLinkBase(getServletContext()) + "" + sCallingClass + "?"
					+ "" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
					+ "&Warning=" + m_sWarning
			);
			return;			
		}

		long lStartTime = System.currentTimeMillis();
		try {
			SMPurgeData.purgeData(
				datPurgeDeadline, 
				request.getParameter(SMPurgeDataSelection.PURGE_ORDERS) != null, 
				request.getParameter(SMPurgeDataSelection.PURGE_CUSTOMERCALLLOGS) != null, 
				request.getParameter(SMPurgeDataSelection.PURGE_BIDS) != null, 
				request.getParameter(SMPurgeDataSelection.PURGE_SALESCONTACTS) != null,  
				request.getParameter(SMPurgeDataSelection.PURGE_SYSTEMLOG) != null,
				request.getParameter(SMPurgeDataSelection.PURGE_MATERIALRETURNS) != null,
				request.getParameter(SMPurgeDataSelection.PURGE_SECURITYSYSTEMLOGS) != null,
				request.getParameter(SMPurgeDataSelection.PURGE_GLDATA) != null,
				conn);
		} catch (Exception e) {
			clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1547080652]");
			response.sendRedirect(
					"" + SMUtilities.getURLLinkBase(getServletContext()) + "" + sCallingClass + "?"
					+ "" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
					+ "&Warning=" + clsServletUtilities.URLEncode(e.getMessage())
			);
			return;			
		}
		
		log.writeEntry(
				sUserID, 
				SMLogEntry.LOG_OPERATION_SMPURGEDATA, 
				"Successfully purged data using purge deadline date " + clsDateAndTimeConversions.utilDateToString(datPurgeDeadline, "MM/dd/yyyy") + ".",
				"Purge orders: " + (request.getParameter(SMPurgeDataSelection.PURGE_ORDERS) != null)
				+ ", Purge customer call logs: " + (request.getParameter(SMPurgeDataSelection.PURGE_CUSTOMERCALLLOGS) != null)
				+ ", Purge customer " + SMBidEntry.ParamObjectName.toLowerCase() + "s: " + (request.getParameter(SMPurgeDataSelection.PURGE_BIDS) != null)
				+ ", Purge customer sales contacts: " + (request.getParameter(SMPurgeDataSelection.PURGE_SALESCONTACTS) != null)
				+ ", Purge system logs: " + (request.getParameter(SMPurgeDataSelection.PURGE_SYSTEMLOG) != null)
				+ ", Purge material returns: " + (request.getParameter(SMPurgeDataSelection.PURGE_MATERIALRETURNS) != null)
				+ ", Purge General LEdger data: " + (request.getParameter(SMPurgeDataSelection.PURGE_GLDATA) != null)
				+ ", Purge security system logs: " + (request.getParameter(SMPurgeDataSelection.PURGE_SECURITYSYSTEMLOGS) != null),
				"[1376509352]"
			);
		
		m_sWarning = "";
		if (request.getParameter(SMPurgeDataSelection.PURGE_ORDERS) != null){
			m_sWarning += "ORDER";
		}
		if (request.getParameter(SMPurgeDataSelection.PURGE_CUSTOMERCALLLOGS) != null){
			if (m_sWarning.compareToIgnoreCase("") != 0){
				m_sWarning += ", ";
			}
			m_sWarning += "CUSTOMER CALL LOGS";
		}
		if (request.getParameter(SMPurgeDataSelection.PURGE_BIDS) != null){
			if (m_sWarning.compareToIgnoreCase("") != 0){
				m_sWarning += ", ";
			}
			m_sWarning += SMBidEntry.ParamObjectName.toUpperCase() + "s";
		}
		if (request.getParameter(SMPurgeDataSelection.PURGE_SALESCONTACTS) != null){
			if (m_sWarning.compareToIgnoreCase("") != 0){
				m_sWarning += ", ";
			}
			m_sWarning += "SALES CONTACTS";
		}
		if (request.getParameter(SMPurgeDataSelection.PURGE_SYSTEMLOG) != null){
			if (m_sWarning.compareToIgnoreCase("") != 0){
				m_sWarning += ", ";
			}
			m_sWarning += "SYSTEM LOGS";
		}
		if (request.getParameter(SMPurgeDataSelection.PURGE_MATERIALRETURNS) != null){
			if (m_sWarning.compareToIgnoreCase("") != 0){
				m_sWarning += ", ";
			}
			m_sWarning += "MATERIAL RETURNS";
		}
		if (request.getParameter(SMPurgeDataSelection.PURGE_GLDATA) != null){
			if (m_sWarning.compareToIgnoreCase("") != 0){
				m_sWarning += ", ";
			}
			m_sWarning += "GENERAL LEDGER DATA";
		}

		if (request.getParameter(SMPurgeDataSelection.PURGE_SECURITYSYSTEMLOGS) != null){
			if (m_sWarning.compareToIgnoreCase("") != 0){
				m_sWarning += ", ";
			}
			m_sWarning += "SECURITY SYSTEM LOGS";
		}

		m_sWarning += " data in this company was successfully purged using purge deadline " 
			+ clsDateAndTimeConversions.utilDateToString(datPurgeDeadline, "MM-dd-yyyy") 
			+ ", time elapsed was " + Long.toString((System.currentTimeMillis() - lStartTime) /1000) + " seconds."
			;
		clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1547080653]");
		response.sendRedirect(
				"" + SMUtilities.getURLLinkBase(getServletContext()) + "" + sCallingClass + "?"
				+ "" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
				+ "&Warning=" + m_sWarning
		);
		return;
    }

	public void doGet(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
		
		doPost(request, response);
	}
}
