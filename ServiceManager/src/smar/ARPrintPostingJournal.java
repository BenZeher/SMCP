package smar;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import ServletUtilities.clsDatabaseFunctions;
import smcontrolpanel.SMAuthenticate;
import smcontrolpanel.SMSystemFunctions;
import smcontrolpanel.SMUtilities;

import java.sql.Connection;

public class ARPrintPostingJournal extends HttpServlet{

	private static final long serialVersionUID = 1L;

	public void doPost(HttpServletRequest request,
			HttpServletResponse response)
	throws ServletException, IOException {

		PrintWriter out = response.getWriter();
		if(!SMAuthenticate.authenticateSMCPCredentials(
				request, 
				response, 
				getServletContext(), 
				SMSystemFunctions.ARPostingJournal))
			{
				return;
			}

	    //Get the session info:
	    HttpSession CurrentSession = request.getSession(true);
	    String sDBID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_DATABASE_ID);
	    String sUserID = (String)CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERID);
	    String sUserFullName = (String)CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERFIRSTNAME) + " "
	    				+ (String)CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERLASTNAME);
		
		String m_sStartingBatchNumber = ARUtilities.get_Request_Parameter("StartingBatchNumber", request);
		String m_sEndingBatchNumber = ARUtilities.get_Request_Parameter("EndingBatchNumber", request);
		boolean m_bIncludeInvoiceBatches = false;
		if (request.getParameter("IncludeInvoiceBatches") != null){
			m_bIncludeInvoiceBatches = true;
		}
		boolean m_bIncludeCashBatches = false;
		if (request.getParameter("IncludeCashBatches") != null){
			m_bIncludeCashBatches = true;
		}

		ARPostingJournal pj = new ARPostingJournal(
				m_sStartingBatchNumber, 
				m_sEndingBatchNumber, 
				m_bIncludeInvoiceBatches,
				m_bIncludeCashBatches,
				sUserID,
				sUserFullName);
		pj.setM_sBorderWidth("1");
		Connection conn = clsDatabaseFunctions.getConnection(
			getServletContext(), 
			sDBID,
			"MySQL",
			this.toString() + ".doPost - User: " 
			+ sUserID
			+ " - "
			+ sUserFullName
				);
		
		if (conn == null){
			response.sendRedirect(
					"" + SMUtilities.getURLLinkBase(getServletContext()) + "smar." + "ARSelectForPostingJournal" + "?"
					+ "StartingBatchNumber=" + m_sStartingBatchNumber
					+ "&EndingBatchNumber=" + m_sEndingBatchNumber
					+ "&Warning=Could not open database connection"
					+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
			);
					
					return;
		}
		if (!pj.processReport(conn, out, getServletContext())){
			response.sendRedirect(
					"" + SMUtilities.getURLLinkBase(getServletContext()) + "smar." + "ARSelectForPostingJournal" + "?"
					+ "StartingBatchNumber=" + m_sStartingBatchNumber
					+ "&EndingBatchNumber=" + m_sEndingBatchNumber
					+ "&Warning=Could not open process report: " + pj.getErrorMessage()
					+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID		
			);
					clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1547067568]");
					return;
		}
		clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1547067569]");
		return;

	}

	public void doGet(HttpServletRequest request,
			HttpServletResponse response)
	throws ServletException, IOException {

		doPost(request, response);
	}
}
