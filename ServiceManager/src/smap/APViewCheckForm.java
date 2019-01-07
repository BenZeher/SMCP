package smap;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import SMDataDefinition.SMTableapcheckforms;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsManageRequestParameters;
import smcontrolpanel.SMAuthenticate;
import smcontrolpanel.SMMasterEditAction;
import smcontrolpanel.SMSystemFunctions;
import smcontrolpanel.SMUtilities;

public class APViewCheckForm extends HttpServlet {

	private static final long serialVersionUID = 1L;
	
	@Override
	public void doPost(HttpServletRequest request,
			HttpServletResponse response)
	throws ServletException, IOException {

		PrintWriter out = response.getWriter();
		if (!SMAuthenticate.authenticateSMCPCredentials(
				request, 
				response, 
				getServletContext(),
				SMSystemFunctions.APEditCheckForms
		)
		){
			return;
		}

		String sCheckFormID = clsManageRequestParameters.get_Request_Parameter(SMTableapcheckforms.lid, request);
		String sNumberOfChecksToPrint = clsManageRequestParameters.get_Request_Parameter(APEditCheckFormsEdit.NUMBER_OF_SAMPLE_CHECKS_TO_PRINT, request).trim();
		String sSampleVendorCode = clsManageRequestParameters.get_Request_Parameter(APEditCheckFormsEdit.SAMPLE_VENDOR, request).trim();
		String sSampleRemitToCode = clsManageRequestParameters.get_Request_Parameter(APEditCheckFormsEdit.SAMPLE_REMIT_TO, request).trim();
		String sSampleBankID = clsManageRequestParameters.get_Request_Parameter(APEditCheckFormsEdit.SAMPLE_BANK_ID, request).trim();
		String sNumberOfAdviceLinesToPrint = clsManageRequestParameters.get_Request_Parameter(APEditCheckFormsEdit.SAMPLE_NUMBER_OF_ADVICE_LINES, request).trim();
		String sCalledClassName = clsManageRequestParameters.get_Request_Parameter("CallingClass", request);
		
		SMMasterEditAction smaction = new SMMasterEditAction(request, response);
		if (!smaction.processSession(getServletContext(), -1, request)){return;}

		String sRedirectString = 
				"" + SMUtilities.getURLLinkBase(getServletContext()) + clsManageRequestParameters.get_Request_Parameter("CallingClass", request)
				+ "?" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + smaction.getsDBID()
				+ "&" + SMTableapcheckforms.lid + "=" + sCheckFormID
				+ "&CallingClass=" + clsManageRequestParameters.get_Request_Parameter("CallingClass", request)
				+ "&" + SMTableapcheckforms.lid + "=" + request.getParameter(SMTableapcheckforms.lid)
				+ "&" + APEditCheckFormsEdit.NUMBER_OF_SAMPLE_CHECKS_TO_PRINT + "=" + sNumberOfChecksToPrint
				+ "&" + APEditCheckFormsEdit.SAMPLE_BANK_ID + "=" + sSampleBankID
				+ "&" + APEditCheckFormsEdit.BUTTON_SUBMIT_ADD + "=" + clsManageRequestParameters.get_Request_Parameter(APEditCheckFormsEdit.BUTTON_SUBMIT_ADD, request)
				+ "&" + APEditCheckFormsEdit.BUTTON_SUBMIT_EDIT + "=" + clsManageRequestParameters.get_Request_Parameter(APEditCheckFormsEdit.BUTTON_SUBMIT_EDIT, request)
				+ "&" + APEditCheckFormsEdit.BUTTON_SUBMIT_DELETE + "=" + clsManageRequestParameters.get_Request_Parameter(APEditCheckFormsEdit.BUTTON_SUBMIT_DELETE, request)
			;
		
		int iNumberOfChecksToPrint = 0;
		try {
			iNumberOfChecksToPrint = Integer.parseInt(sNumberOfChecksToPrint);
		} catch (NumberFormatException e2) {
			sRedirectString += 
					"&" + "Warning" + "=" + "Error [1503670385] - invalid number of checks to print: '" + sNumberOfChecksToPrint + "'."
				;
				redirectProcess(sRedirectString, response);
				return;
		}
		
		int iNumberOfAdviceLinesToPrint = 0;
		try {
			iNumberOfAdviceLinesToPrint = Integer.parseInt(sNumberOfAdviceLinesToPrint);
		} catch (NumberFormatException e3) {
			sRedirectString += 
					"&" + "Warning" + "=" + "Error [1503670395] - invalid number of advice lines to print: '" + sNumberOfChecksToPrint + "'."
				;
				redirectProcess(sRedirectString, response);
				return;
		}
		
		if (iNumberOfAdviceLinesToPrint == 0){
			sRedirectString += 
					"&" + "Warning" + "=" + "Error [1503670495] - number of advice lines to print cannot be zero: '" + sNumberOfChecksToPrint + "'."
				;
				redirectProcess(sRedirectString, response);
				return;
		}
		
		Connection conn = null;
		try {
			conn = clsDatabaseFunctions.getConnectionWithException(getServletContext(), smaction.getsDBID(), "MySQL", SMUtilities.getFullClassName(this.toString()));
		} catch (Exception e1) {
			sRedirectString += 
				"&" + "Warning" + "=" + "Error [1503324719] getting data connection - " + e1.getMessage()
			;
			redirectProcess(sRedirectString, response);
			return;
		}
		
		out.println("<HTML><BODY>");
		
		try{
			out.println(
				APCheckFormProcessor.displayAlignmentCheckForm(
					sCheckFormID, 
					iNumberOfChecksToPrint, 
					sSampleVendorCode, 
					sSampleRemitToCode, 
					sSampleBankID, 
					iNumberOfAdviceLinesToPrint, 
					smaction.getsDBID(),
					smaction.getUserName(),
					sCalledClassName, 
					conn)
			);
		} catch (Exception e) {
			sRedirectString += 
				"&" + "Warning" + "=" + "Error [1503324720] displaying check form - " + e.getMessage()
			;
			clsDatabaseFunctions.freeConnection(getServletContext(), conn);
			redirectProcess(sRedirectString, response);
			return;
		}
		
		clsDatabaseFunctions.freeConnection(getServletContext(), conn);
		out.println("</BODY></HTML>");
		
	}
	
	private void redirectProcess(String sRedirectString, HttpServletResponse res ){
		try {
			res.sendRedirect(sRedirectString);
		} catch (IOException e1) {
			System.out.println("Error [1395238124] in " + this.toString() + ".redirectAction - IOException error redirecting with string: "
					+ sRedirectString + " - " + e1.getMessage());
			return;
		} catch (IllegalStateException e1) {
			System.out.println("Error [1395238125] in " + this.toString() + ".redirectAction - IllegalStateException error redirecting with string: "
					+ sRedirectString + " - " + e1.getMessage());
			return;
		}
	}
	public void doGet(HttpServletRequest request,
			HttpServletResponse response)
	throws ServletException, IOException {

		doPost(request, response);
	}
}
