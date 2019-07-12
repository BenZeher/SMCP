package smar;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsDateAndTimeConversions;
import ServletUtilities.clsManageRequestParameters;
import ServletUtilities.clsServletUtilities;
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
		
		String m_sStartingBatchNumber = clsManageRequestParameters.get_Request_Parameter("StartingBatchNumber", request);
		String m_sEndingBatchNumber = clsManageRequestParameters.get_Request_Parameter("EndingBatchNumber", request);
		String sCompanyName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_COMPANYNAME);
		String sReportTitle = "AR Posting Journal";
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
		

		out.println(clsServletUtilities.DOCTYPE +
	        "<HTML>" +
	        "<HEAD><TITLE>" + sReportTitle + " - " + sCompanyName + "</TITLE></HEAD>\n<BR>\n"  
			+ "<BODY BGCOLOR=\"#FFFFFF\">");
		String sColor = SMUtilities.getInitBackGroundColor(getServletContext(), conn);
		out.println("<TABLE BORDER=0 WIDTH=100% BGCOLOR=\"" + sColor + "\">\n" +
				"<TR><TD ALIGN=CENTER WIDTH=55%><FONT SIZE=2><B>" + sCompanyName + "</B></FONT></TD></TR>\n" +
				"<TR><TD VALIGN=BOTTOM COLSPAN=2><FONT SIZE=4 ><B>" + sReportTitle + "</B><BR><BR></FONT></TD></TR>\n"
				+ "<TR><TD COLSPAN=2><FONT SIZE=2><B>Starting with batch number: </B>" + m_sStartingBatchNumber + "</FONT></TD></TR>\n"
				+ "<TR><TD COLSPAN=2><FONT SIZE=2><B>Ending with batch number: </B>" + m_sEndingBatchNumber + "</FONT></TD></TR>\n");
		if(m_bIncludeInvoiceBatches){
			out.println("<TR><TD COLSPAN=2><FONT SIZE=2>INCLUDING invoice batches.<BR></FONT></TD></TR>\n");
		}
		if(m_bIncludeCashBatches){
			out.println("<TR><TD COLSPAN=2><FONT SIZE=2>INCLUDING cash batches.<BR></FONT></TD></TR>\n");
		}
		out.println("<TR><TD COLSPAN=2><FONT SIZE=2><B>Printed by </B>" + sUserFullName + 
				"<B> on </B>" + clsDateAndTimeConversions.nowStdFormat() + 
				"</FONT></TD></TR>\n");
		out.println("</TABLE><BR>");
		out.println(SMUtilities.getMasterStyleSheetLink());
		
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
		if (!pj.processReport(conn, out)){
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
