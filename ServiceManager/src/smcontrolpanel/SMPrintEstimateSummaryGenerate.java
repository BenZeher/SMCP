package smcontrolpanel;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import SMDataDefinition.SMTablesmestimatesummaries;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsManageRequestParameters;

public class SMPrintEstimateSummaryGenerate extends HttpServlet{

	private static final long serialVersionUID = 1L;

	public void doPost(HttpServletRequest request,
			HttpServletResponse response) throws IOException {

		response.setContentType("text/html");
		PrintWriter out = response.getWriter();
		if (!SMAuthenticate.authenticateSMCPCredentials(
				request, 
				response, 
				getServletContext(), 
				SMSystemFunctions.SMEditSMEstimates))
		{
			return;
		}

		//Get the session info:
		HttpSession CurrentSession = request.getSession(true);
		String sDBID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_DATABASE_ID);
		String sUserID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERID);
		String sUserFullName = (String)CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERFIRSTNAME) + " "
				+ (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERLASTNAME);

		String sCallingClass = "";
		//sCallingClass will look like: smcontrolpanel.ARAgedTrialBalanceReport
		sCallingClass = clsManageRequestParameters.get_Request_Parameter("CallingClass", request);


		SMEstimateSummary summary = new SMEstimateSummary(request);
		if (CurrentSession.getAttribute(SMEstimateSummary.OBJECT_NAME) != null){
			summary = (SMEstimateSummary) CurrentSession.getAttribute(SMEstimateSummary.OBJECT_NAME);
			CurrentSession.removeAttribute(SMEstimateSummary.OBJECT_NAME);
		}else {
			try {
				summary.load(getServletContext(), sDBID, sUserID);
			} catch (Exception e1) {
				response.sendRedirect(
						"" + SMUtilities.getURLLinkBase(getServletContext()) + "" + sCallingClass
						+ "?" + SMTablesmestimatesummaries.lid + "=" + summary.getslid()
						+ "&Warning=" + SMUtilities.URLEncode(e1.getMessage())
						+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
						);
			}
		}
		try {
			summary.loadEstimates(summary.getslid(), sDBID, getServletContext(), sUserFullName);
		} catch (Exception e) {
			response.sendRedirect(
					"" + SMUtilities.getURLLinkBase(getServletContext()) + "" + sCallingClass
					+ "?" + SMTablesmestimatesummaries.lid + "=" + summary.getslid()
					+ "&Warning=" + SMUtilities.URLEncode(e.getMessage())
					+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
					+ "&lid=" +  summary.getslid()
					);
			return;
		}

		//Retrieve information
		Connection conn = clsDatabaseFunctions.getConnection(
				getServletContext(), 
				sDBID, 
				"MySQL", 
				SMUtilities.getFullClassName(this.toString()) 
				+ " - user: " 
				+ sUserID
				+ " - "
				+ sUserFullName
				);
		if (conn == null){
			response.sendRedirect(
					"" + SMUtilities.getURLLinkBase(getServletContext()) + "" + sCallingClass + "?"
							+ "Warning=" + "Unable to get data connection."
							+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
							+ "&lid=" +  summary.getslid()
					);			
			return;
		}

		//TODO SMPrintEstimateSummary -> Process Report
		SMPrintEstimateSummary summaryreport = new SMPrintEstimateSummary();
		if(!summaryreport.processReport(
				conn,
				summary, 
				sDBID, 
				sUserID,
				sUserFullName, 
				out, 
				getServletContext(), 
				(String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_LICENSE_MODULE_LEVEL))
				) {
		}else {
			response.sendRedirect(
					"" + SMUtilities.getURLLinkBase(getServletContext()) + "" + sCallingClass + "?"
							+ "Warning=" + "Error Processing Report"
							+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
							+ "&lid=" +  summary.getslid()
					);			
			return;
		}
		
	}

	public void doGet(HttpServletRequest request,
			HttpServletResponse response)
					throws ServletException, IOException {

		doPost(request, response);
	}
}
