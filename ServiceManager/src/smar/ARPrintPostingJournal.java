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
	private String m_sStartingBatchNumber;
	private String m_sEndingBatchNumber;
	private static boolean m_bIncludeInvoiceBatches = false;
	private static boolean m_bIncludeCashBatches = false;

	private static String sDBID = "";
	private static String sUserID = "";
	private static String sUserFullName = "";

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
	    sDBID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_DATABASE_ID);
	    sUserID = (String)CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERID);
	    sUserFullName = (String)CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERFIRSTNAME) + " "
	    				+ (String)CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERLASTNAME);
		getRequestParameters(request);

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
	private void getRequestParameters(
			HttpServletRequest req){

		m_sStartingBatchNumber = ARUtilities.get_Request_Parameter("StartingBatchNumber", req);
		m_sEndingBatchNumber = ARUtilities.get_Request_Parameter("EndingBatchNumber", req);
		if (req.getParameter("IncludeInvoiceBatches") != null){
			m_bIncludeInvoiceBatches = true;
		}else{
			m_bIncludeInvoiceBatches = false;
		}
		if (req.getParameter("IncludeCashBatches") != null){
			m_bIncludeCashBatches = true;
		}else{
			m_bIncludeCashBatches = false;
		}
		
	}
	public void doGet(HttpServletRequest request,
			HttpServletResponse response)
	throws ServletException, IOException {

		doPost(request, response);
	}
}
