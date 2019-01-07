package smap;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import SMClasses.SMLogEntry;
import SMDataDefinition.SMTableapbatches;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsManageRequestParameters;
import smcontrolpanel.SMAuthenticate;
import smcontrolpanel.SMSystemFunctions;
import smcontrolpanel.SMUtilities;

public class APCheckRegisterGenerate extends HttpServlet {

	private static final long serialVersionUID = 1L;

	private static SimpleDateFormat USDateformatter = new SimpleDateFormat("MM-dd-yyyy hh:mm:ss a EEE");

	private String sCallingClass = "";
	private static String sDBID = "";
	private static String sUserID = "";
	private long lStartingTime = 0;

	public void doGet(HttpServletRequest request,
			HttpServletResponse response)
	throws ServletException, IOException {

		response.setContentType("text/html");
		PrintWriter out = response.getWriter();
	    if (!SMAuthenticate.authenticateSMCPCredentials(request, response, getServletContext(), SMSystemFunctions.APEditBatches)){
	    	return;
	    }
		//Get the session info:
		HttpSession CurrentSession = request.getSession(true);
		sDBID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_DATABASE_ID);
		sUserID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERID);

		//Get parameters here:
		//sCallingClass will look like: smar.ARAgedTrialBalanceReport
		sCallingClass = clsManageRequestParameters.get_Request_Parameter("CallingClass", request);
		String sBatchNumber = request.getParameter(SMTableapbatches.lbatchnumber);
		String sBatchType = request.getParameter(SMTableapbatches.ibatchtype);

		//Customized title
		String sReportTitle = "A/P Check Register for check run batch number " + sBatchNumber;
		
		out.println("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.0 " 
			+ "Transitional//EN\">\n" 
			+ "<HTML>\n" 
			+ "  <HEAD>\n"
			+ "    <TITLE>" + sReportTitle + " - " + (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_COMPANYNAME) + "</TITLE>\n"
			+ "  </HEAD>\n"
			+ "<BR>" 
			+ "  <BODY BGCOLOR=\"#FFFFFF\">\n" 
			+ "    <TABLE BORDER=0 WIDTH=100%>\n" 
			+ "      <TR>\n"
			+ "        <TD ALIGN=LEFT WIDTH=45%><FONT SIZE=2>" + USDateformatter.format((new Timestamp(System.currentTimeMillis()))) + "</FONT></TD>\n"
			+ "        <TD ALIGN=CENTER WIDTH=55%><FONT SIZE=2><B>" + CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_COMPANYNAME).toString() + "</B></FONT></TD>\n"
			+ "      </TR>\n" 
			+ "      <TR>\n"
			+ "        <TD VALIGN=BOTTOM COLSPAN=2><FONT SIZE=2><B>" + sReportTitle + "</B></FONT></TD>\n"
			+ "      </TR>\n" 
		);
		
		out.println("  <TR>\n"
			+ "    <TD><A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMUserLogin?" 
			+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
			+ "\">Return to user login</A><BR>" 
			+ "<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smap.APMainMenu?" 
			+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
			+ "\">Return to Accounts Payable Main Menu</A></TD>\n"
			+ "  </TR>\n"
			+ "</TABLE>"
		);
		
		out.println(SMUtilities.getMasterStyleSheetLink());
		out.println("<BR>\n");
		
		//Retrieve information
		Connection conn = null;
		try {
			conn = clsDatabaseFunctions.getConnectionWithException(
					getServletContext(), 
					sDBID, 
					"MySQL", 
					this.toString() + ".doGet - UserID: " + sUserID
			);
		} catch (Exception e1) {
			response.sendRedirect(
				"" + SMUtilities.getURLLinkBase(getServletContext()) + "" + sCallingClass + "?"
	    		+ SMTableapbatches.lbatchnumber + "=" + sBatchNumber
	    		+ "&" + SMTableapbatches.ibatchtype + "=" + sBatchType
				+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
				+ "&Warning=" + e1.getMessage()
			);			
			return;
		}
		
		lStartingTime = System.currentTimeMillis();
		APCheckRegisterReport rpt = new APCheckRegisterReport();
		try {
			out.println(
				rpt.processReport(
					conn,
					sBatchNumber, 
					SMSystemFunctions.isFunctionPermitted(
						SMSystemFunctions.APViewTransactionInformation, 
						sUserID, 
						conn, 
						(String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_LICENSE_MODULE_LEVEL)),
					SMSystemFunctions.isFunctionPermitted(
						SMSystemFunctions.APDisplayVendorInformation, 
						sUserID, 
						conn, 
						(String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_LICENSE_MODULE_LEVEL)),
						sDBID,
					getServletContext()
				)
			);
			
		} catch (Exception e) {
			clsDatabaseFunctions.freeConnection(getServletContext(), conn);
			response.sendRedirect(
				"" + SMUtilities.getURLLinkBase(getServletContext()) + "" + sCallingClass + "?"
		    		+ SMTableapbatches.lbatchnumber + "=" + sBatchNumber
		    		+ "&" + SMTableapbatches.ibatchtype + "=" + sBatchType
					+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
					+ "&Warning=" + e.getMessage()
			);			
			return;
		}
		
		clsDatabaseFunctions.freeConnection(getServletContext(), conn);
		
		SMLogEntry log = new SMLogEntry(conn);
		log.writeEntry(sUserID,SMLogEntry.LOG_OPERATION_APCHECKREGISTER, "REPORT", "AP Check Register", "[1505916783]");
		
		long lEndingTime = System.currentTimeMillis();
		out.println("<BR>Processing took " + (lEndingTime - lStartingTime)/1000L + " seconds.\n");
		out.println("  </BODY>\n"
			+ "    </HTML>\n");
		return;	
	}
}
