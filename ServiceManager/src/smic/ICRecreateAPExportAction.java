package smic;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import SMDataDefinition.SMExportTypes;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsManageRequestParameters;
import smcontrolpanel.SMAuthenticate;
import smcontrolpanel.SMSystemFunctions;
import smcontrolpanel.SMUtilities;

public class ICRecreateAPExportAction extends HttpServlet{
	
	private static final long serialVersionUID = 1L;
	
	private static String sDBID = "";
	private static String sUserName = "";
	private static String sUserFullName = "";
	public void doPost(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
	
		if (!SMAuthenticate.authenticateSMCPCredentials(
				request, 
				response, 
				getServletContext(), 
				SMSystemFunctions.ICRecreateAPInvoiceExport
		)){
			return;
		}
		
	    //Get the session info:
	    HttpSession CurrentSession = request.getSession(true);
	    sDBID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_DATABASE_ID);
	    sUserName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERNAME);
	    sUserFullName = (String)CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERFIRSTNAME) + " "
	    				+ (String)CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERLASTNAME);

	    //Collect the strings here:
	    String sCallingClass = clsManageRequestParameters.get_Request_Parameter("CallingClass", request);
	    String sExportSequence = clsManageRequestParameters.get_Request_Parameter(ICRecreateAPExportEdit.RADIO_BUTTON_GROUP_NAME, request);
	    try {
			@SuppressWarnings("unused")
			long lExportSequence = Long.parseLong(sExportSequence);
		} catch (NumberFormatException e) {
			response.sendRedirect(
					"" + SMUtilities.getURLLinkBase(getServletContext()) + "" + sCallingClass
					+ "?" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
					+ "&WARNING=Invalid Export Sequence '" + sExportSequence + "'."
			);
			return;
		}
    	
		Connection conn = clsDatabaseFunctions.getConnection(
				getServletContext(), 
				sDBID,
				"MySQL",
				this.toString() + ".doPost - User: " 
				+ sUserName
				+ " - "
				+ sUserFullName
				);

		if (conn == null){
			response.sendRedirect(
					"" + SMUtilities.getURLLinkBase(getServletContext()) + "" + sCallingClass
					+ "?" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
					+ "&WARNING=Could not get data connection."
			);
			return;
		}
		
		//Now create the download and send it to the browser:
		String sDatabaseName = sDBID;
		String sSourceLedger = "IC";
		String sBatchTypeLabel = "Invoices";
		response.setContentType("text/csv");
		ICAPExport export = new ICAPExport();
        String disposition = "attachment; fileName= " + export.getExportFileName(sSourceLedger, sBatchTypeLabel, sExportSequence, sDatabaseName);
        response.setHeader("Content-Disposition", disposition);
        
        try {
			export.loadExport(conn, sExportSequence);
		} catch (Exception e1) {
			clsDatabaseFunctions.freeConnection(getServletContext(), conn);
			response.sendRedirect(
					"" + SMUtilities.getURLLinkBase(getServletContext()) + "" + sCallingClass
					+ "?" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
					+ "&WARNING=Could not load export sequence " + sExportSequence + " - " + e1.getMessage()
			);
		}
        
        //Determine the type of export:
        ICOption icopt = new ICOption();
        if(!icopt.load(conn)){
        	clsDatabaseFunctions.freeConnection(getServletContext(), conn);
			response.sendRedirect(
					"" + SMUtilities.getURLLinkBase(getServletContext()) + "" + sCallingClass
					+ "?" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
					+ "&WARNING=Could not read IC Options information - " + icopt.getErrorMessage()
			);
        };
        
        //Now write to the download:
        PrintWriter out = response.getWriter();
        try {
        	clsDatabaseFunctions.freeConnection(getServletContext(), conn);
        	if (icopt.getExportTo() == SMExportTypes.EXPORT_TO_MAS200){
        		export.writeMAS200ExportDownload(out);
        	}else{
        		export.writeACCPACExportDownload(out);
        	}
		} catch (Exception e) {
			response.sendRedirect(
					"" + SMUtilities.getURLLinkBase(getServletContext()) + "" + sCallingClass
					+ "?" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
					+ "&WARNING=Export sequence " + sExportSequence + " was not re-exported - " + e.getMessage()
			);
		}
		
		clsDatabaseFunctions.freeConnection(getServletContext(), conn);
		response.sendRedirect(
				"" + SMUtilities.getURLLinkBase(getServletContext()) + "" + sCallingClass
				+ "?" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
				+ "&STATUS=Export sequence " + sExportSequence + " was successfully re-exported."
		);
		return;
	}
	public void doGet(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
		
		doPost(request, response);
	}
}
