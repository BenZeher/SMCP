package smfa;

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
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsManageRequestParameters;
import smcontrolpanel.SMAuthenticate;
import smcontrolpanel.SMSystemFunctions;
import smcontrolpanel.SMUtilities;

public class FATransactionListGenerate extends HttpServlet {

	private static final long serialVersionUID = 1L;

	private SimpleDateFormat USDateformatter = new SimpleDateFormat("MM-dd-yyyy hh:mm:ss a EEE");

	private String sWarning = "";
	private String sCallingClass = "";
	private String sDBID = "";
	private String sUserID = "";
	private String sUserFirstName = "";
	private String sUserLastName = "";
	private String sCompanyName = "";
	private boolean bSelectByCategory = false;
	//private static SimpleDateFormat USTimeOnlyformatter = new SimpleDateFormat("hh:mm:ss a");

	public void doPost(HttpServletRequest request,
			HttpServletResponse response)
					throws ServletException, IOException {

		response.setContentType("text/html");
		PrintWriter out = response.getWriter();

		 if (!SMAuthenticate.authenticateSMCPCredentials(request, response, getServletContext(), SMSystemFunctions.FATransactionReport)){
		    	return;
		    }
		 
		//Get the session info:
		HttpSession CurrentSession = request.getSession(true);
		sDBID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_DATABASE_ID);
		sUserID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERID);
		sUserFirstName = (String)CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERFIRSTNAME);
		sUserLastName = (String)CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERLASTNAME);
		
		sCompanyName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_COMPANYNAME);
	   

		boolean bPrintProvisionalTransactions = false;
		boolean bPrintAdjustmentTransactions = false;
		boolean bPrintActualTransactions = false;
		boolean bShowDetails = false;

		String sStartingFY = "";
		String sStartingFP = "";
		String sEndingFY = "";
		String sEndingFP = "";


		if (request.getParameter("PRINTPROVISIONALTRANSACTION") != null){
			bPrintProvisionalTransactions = true;
		}
		if (request.getParameter("PRINTADJUSTMENTTRANSACTION") != null){
			bPrintAdjustmentTransactions = true;
		}
		if (request.getParameter("PRINTACTUALTRANSACTION") != null){
			bPrintActualTransactions = true;
		}
		if (request.getParameter("SHOWDETAILS") != null){
			bShowDetails = true;
		}

		sStartingFY = clsManageRequestParameters.get_Request_Parameter("STARTFISCALYEAR", request);
		sStartingFP = clsManageRequestParameters.get_Request_Parameter("STARTFISCALPERIOD", request);
		sEndingFY = clsManageRequestParameters.get_Request_Parameter("ENDFISCALYEAR", request);
		sEndingFP = clsManageRequestParameters.get_Request_Parameter("ENDFISCALPERIOD", request);



		String sReportTitle = "Transaction List for " + sCompanyName;

		out.println("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.0 " +
				"Transitional//EN\">" +
				"<HTML>" +
				"<HEAD><TITLE>" + sReportTitle + "</TITLE></HEAD>\n<BR>" + 
				"<BODY BGCOLOR=\"#FFFFFF\">" +
				"<TABLE BORDER=0 WIDTH=100%>" +
				"<TR><TD ALIGN=LEFT WIDTH=45%><FONT SIZE=2>" 
				+ USDateformatter.format((new Timestamp(System.currentTimeMillis()))) 
				+ " Printed by " + SMUtilities.getFullNamebyUserID(sUserID, getServletContext(), sDBID, "FATransactionListGenerate")
				+ "</FONT></TD><TD ALIGN=CENTER WIDTH=55%><FONT SIZE=2><B>" + sCompanyName + "</B></FONT></TD></TR>" +
				"<TR><TD VALIGN=BOTTOM COLSPAN=2><FONT SIZE=4><B>" + sReportTitle + "</B></FONT></TD></TR></TABLE>");

		out.println("<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) 
				+ "smcontrolpanel.SMUserLogin?" 
				+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
				+ "\" >Return to user login</A><BR>");
		out.println("<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smfa.FAMainMenu?" 
				+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
				+ "\">Return to Fixed Assets Main Menu</A><BR>");
		//log usage of this report
		SMClasses.SMLogEntry log = new SMClasses.SMLogEntry(sDBID, getServletContext());
		log.writeEntry(sUserID, SMLogEntry.LOG_OPERATION_FIXEDASSETS, "REPORT", "FATransactionList", "[1376509371]");

		//Retrieve information
		Connection conn = clsDatabaseFunctions.getConnection(
				getServletContext(), 
				sDBID, 
				"MySQL", 
				this.toString() 
				+ " - user: " 
				+ sUserID
				+ " - "
				+ sUserFirstName
				+ " "
				+ sUserLastName
				+ " [1332358799]"
				);
		if (conn == null){
			sWarning = "Unable to get data connection.";
			redirectAfterError(response);
			return;
		}

		FATransactionList list = new FATransactionList();
		if (!list.processReport(
				conn,
				sDBID,
				sUserID,
				sStartingFY,
				sStartingFP,
				sEndingFY,
				sEndingFP,
				bPrintProvisionalTransactions,
				bPrintAdjustmentTransactions,
				bPrintActualTransactions,
				bShowDetails,
				out,
				getServletContext(),
				(String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_LICENSE_MODULE_LEVEL))){
			out.println("Could not print report - " + list.getErrorMessageString());
		}
		clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1547067480]");
		out.println("</BODY></HTML>");
	}

	public void doGet(HttpServletRequest request,
			HttpServletResponse response)
					throws ServletException, IOException {

		doPost(request, response);
	}

	private void redirectAfterError(HttpServletResponse res){

		String sRedirect =
				"" + SMUtilities.getURLLinkBase(getServletContext()) + "" + sCallingClass + "?"
						+ "Warning=" + sWarning
						;

		if (bSelectByCategory){
			sRedirect += "&SelectByCategory=true";
		}

		sRedirect += "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID;
		try {
			res.sendRedirect(sRedirect);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}			
		return;
	}

}
