package smfa;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Enumeration;

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

public class FAAssetListGenerate extends HttpServlet {

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
	public void doPost(HttpServletRequest request,
			HttpServletResponse response)
					throws ServletException, IOException {

		response.setContentType("text/html");
		PrintWriter out = response.getWriter();

		if (!SMAuthenticate.authenticateSMCPCredentials(request, response, getServletContext(), SMSystemFunctions.FAAssetList)){
			return;
		}
		//Get the session info:
		HttpSession CurrentSession = request.getSession(true);
		sDBID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_DATABASE_ID);
		sUserID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERID);
		sUserFirstName = (String)CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERFIRSTNAME);
		sUserLastName = (String)CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERLASTNAME);
		
		sCompanyName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_COMPANYNAME);


		boolean bIncludeDisposed = false;
		boolean bIncludeNonDisposed = false;
		boolean bShowDetails = false;

		if (request.getParameter("INCLUDEDISPOSED") != null){
			bIncludeDisposed = true;
		}
		if (request.getParameter("INCLUDENONDISPOSED") != null){
			bIncludeNonDisposed = true;
		}
		if (request.getParameter("SHOWDETAILS") != null){
			bShowDetails = true;
		}

		//Get the classes we wish to include:
		ArrayList<String>arrClasses = new ArrayList<String>(0);
		//Now add back in all the authorized users for this device:
		Enumeration<?> paramNames = request.getParameterNames();
		String sUserMarker = FAAssetListSelect.CLASS_CHECKBOX_PARAM;
		while(paramNames.hasMoreElements()) {
			String sParamName = (String)paramNames.nextElement();
			if (sParamName.contains(sUserMarker)){
				String sClass = (sParamName.substring(sParamName.indexOf(sUserMarker) + sUserMarker.length()));
				arrClasses.add(sClass);
			}
		}

		sCallingClass = clsManageRequestParameters.get_Request_Parameter("CALLINGCLASS", request);
		String sFiscalYear = clsManageRequestParameters.get_Request_Parameter(FAAssetListSelect.FISCALYEARSELECTION, request);
		int iFiscalYear = 0;
		if (sFiscalYear.compareToIgnoreCase(FAAssetListSelect.NO_TRANSACTIONS_AVAILABLE) == 0){
			iFiscalYear = -1;
			sFiscalYear = "(N/A)";
		}
		
		try {
			iFiscalYear = Integer.parseInt(sFiscalYear);
		} catch (NumberFormatException e) {
			sWarning = "Invalid fiscal year - '" + sFiscalYear + "'.";
			redirectAfterError(response);
		}
		String sReportTitle = "Asset List for " + sCompanyName;
		String sHeading = "<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.0 " 
				+ "Transitional//EN\">"
				+ "<HTML>"
				+ "<HEAD><TITLE>" + sReportTitle + "</TITLE></HEAD>\n<BR>" 
				+ "<BODY BGCOLOR=\"#FFFFFF\">"
				+ "<TABLE BORDER=0 WIDTH=100% style= \"font-family: Arial;\" >"
				+ "<TR><TD ALIGN=LEFT WIDTH=45%><FONT SIZE=2>" 
				+ USDateformatter.format((new Timestamp(System.currentTimeMillis()))) 
				+ " Printed by " + SMUtilities.getFullNamebyUserID(sUserID, getServletContext(), sDBID, "FAAssetListGenerate")
				+ "</FONT></TD><TD ALIGN=CENTER WIDTH=55%><FONT SIZE=2><B>" + sCompanyName + "</B></FONT></TD></TR>"
				+ "<TR><TD VALIGN=BOTTOM COLSPAN=2><FONT SIZE=3><B>" + sReportTitle 
				+ " showing YTD values for fiscal year " + sFiscalYear
				+ "</B></FONT></TD></TR>"
				;

		sHeading += "<TR>"
				+ "<TD VALIGN=BOTTOM COLSPAN=2><FONT SIZE=3>";

		if (bIncludeDisposed){
			sHeading += "<B>INCLUDING</B>";
		}else{
			sHeading += "<B>NOT INCLUDING</B>";
		}
		sHeading += " disposed assets, ";

		if (bIncludeNonDisposed){
			sHeading += "<B>INCLUDING</B>";
		}else{
			sHeading += "<B>NOT INCLUDING</B>";
		}
		sHeading += " NON-disposed assets, ";

		if (bShowDetails){
			sHeading += "<B>SHOWING</B>";
		}else{
			sHeading += "<B>NOT SHOWING</B>";
		}
		sHeading += " details.";

		sHeading += "<TR>"
				+ "<TD VALIGN=BOTTOM COLSPAN=2><FONT SIZE=3>";

		sHeading += " Including <B>ONLY</B> these classes: <B>";
		for (int i = 0; i < arrClasses.size(); i++){
			if (i == arrClasses.size() - 1){
				sHeading += arrClasses.get(i) + ".</B>";
			}else{
				sHeading += arrClasses.get(i) + ", ";
			}
		}

		sHeading += "</FONT>"
				+ "</TD>"
				+ "</TR>"
				+ "</TABLE>";
		
		out.println(sHeading);
		
		out.println("<BR><A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) 
		+ "smcontrolpanel.SMUserLogin?" 
		+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
		+ "\" >Return to user login</A><BR>");
		out.println("<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smfa.FAMainMenu?" 
				+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
				+ "\">Return to Fixed Assets Main Menu</A><BR><BR>");
		//log usage of this report
		SMClasses.SMLogEntry log = new SMClasses.SMLogEntry(sDBID, getServletContext());
		log.writeEntry(sUserID, SMLogEntry.LOG_OPERATION_FIXEDASSETS, "REPORT", "FAAssetList","[1376509370]");

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
				+ " [1332358735]"
				);
		if (conn == null){
			sWarning = "Unable to get data connection.";
			redirectAfterError(response);
			return;
		}

		FAAssetList list = new FAAssetList();
		if (!list.processReport(
				conn,
				sDBID,
				sUserID,
				bIncludeDisposed,
				bIncludeNonDisposed,
				bShowDetails,
				iFiscalYear,
				out,
				getServletContext(),
				(String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_LICENSE_MODULE_LEVEL),
				arrClasses)){
			out.println("Could not print report - " + list.getErrorMessageString());
		}
		clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1547067470]");
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
