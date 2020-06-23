package smfa;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import SMClasses.SMLogEntry;
import SMDataDefinition.SMMasterStyleSheetDefinitions;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsManageRequestParameters;
import smcontrolpanel.SMAuthenticate;
import smcontrolpanel.SMSystemFunctions;
import smcontrolpanel.SMUtilities;

public class FATransactionListGenerate extends HttpServlet {

	private static final long serialVersionUID = 1L;
	private static final SimpleDateFormat USDateformatter = new SimpleDateFormat("MM-dd-yyyy hh:mm:ss a EEE");

	public void doPost(HttpServletRequest request,
			HttpServletResponse response)
					throws ServletException, IOException {
		
		String sWarning = "";
		boolean bSelectByCategory = false;

		response.setContentType("text/html");
		PrintWriter out = response.getWriter();

		 if (!SMAuthenticate.authenticateSMCPCredentials(request, response, getServletContext(), SMSystemFunctions.FATransactionReport)){
		    	return;
		    }
		 
		//Get the session info:
		HttpSession CurrentSession = request.getSession(true);
		String sDBID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_DATABASE_ID);
		String sUserID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERID);
		String sUserFirstName = (String)CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERFIRSTNAME);
		String sUserLastName = (String)CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERLASTNAME);
		String sCallingClass = clsManageRequestParameters.get_Request_Parameter("CallingClass", request);
		String sCompanyName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_COMPANYNAME);
	   

		boolean bPrintProvisionalTransactions = false;
		boolean bPrintAdjustmentTransactions = false;
		boolean bPrintActualTransactions = false;
		boolean bShowDetails = false;
		boolean bShowAllLocations = false;

		String sStartingFY = "";
		String sStartingFP = "";
		String sEndingFY = "";
		String sEndingFP = "";
		String sGroupBy = "";


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
		sGroupBy =  clsManageRequestParameters.get_Request_Parameter(FATransactionListSelect.GROUPBY_PARAMETER, request);
		bShowAllLocations = 
			clsManageRequestParameters.get_Request_Parameter(FATransactionListSelect.SHOWALLLOCATIONS_PARAMETER, request).compareToIgnoreCase("Y") == 0;
		
		ArrayList<String> arrLocations = new ArrayList<String>(0);
	    Enumeration<String> paramLocationNames = request.getParameterNames();
	    String sParamLocationName = "";
	    String sLocationMarker = FATransactionListSelect.LOCATION_PARAMETER;
	    while(paramLocationNames.hasMoreElements()) {
	    	sParamLocationName = paramLocationNames.nextElement();
		  if (sParamLocationName.contains(sLocationMarker)){
			  arrLocations.add(sParamLocationName.substring(
					  sParamLocationName.indexOf(sLocationMarker) + sLocationMarker.length()));
		  }
	    }
	    Collections.sort(arrLocations);
		
	    if (arrLocations.size() == 0){
    		sWarning += "  You must select at least one location.";
    		redirectAfterError(response, sCallingClass, sWarning, sDBID, bSelectByCategory, arrLocations);
			return;
	    }

	    String sReportTitle = "Transaction List for " + sCompanyName;
	    String sColor = SMUtilities.getInitBackGroundColor(getServletContext(), sDBID);
	    out.println(SMUtilities.getMasterStyleSheetLink());

		out.println("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.0 " +
				"Transitional//EN\">" +
				"<HTML>" +
				"<HEAD><TITLE>" + sReportTitle + "</TITLE></HEAD>\n<BR>" + 
				"<BODY BGCOLOR=\"" + SMMasterStyleSheetDefinitions.BACKGROUND_WHITE + "\">" +
				"<TABLE BORDER=0 WIDTH=100% BGCOLOR = \""+ sColor + "\">" +
				"<TR><TD ALIGN=LEFT WIDTH=45%><FONT SIZE=2>" 
				+ USDateformatter.format((new Timestamp(System.currentTimeMillis()))) 
				+ " Printed by " + SMUtilities.getFullNamebyUserID(sUserID, getServletContext(), sDBID, "FATransactionListGenerate")
				+ "</FONT></TD><TD ALIGN=CENTER WIDTH=55%><FONT SIZE=2><B>" + sCompanyName + "</B></FONT></TD></TR>" +
				"<TR><TD VALIGN=BOTTOM COLSPAN=2><FONT SIZE=4><B>" + sReportTitle + "</B></FONT></TD></TR>");

		out.println("<TR><TD><A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) 
		+ "smcontrolpanel.SMUserLogin?" 
		+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
		+ "\" >Return to user login</A></TD></TR>");
		out.println("<TR><TD><A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smfa.FAMainMenu?" 
				+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
				+ "\">Return to Fixed Assets Main Menu</A></TD></TR><BR>");
		
		String sGroupByLabel = "GL ACCOUNT";
		if (sGroupBy.compareToIgnoreCase(FATransactionListSelect.GROUPBY_VALUE_CLASS) == 0){
			sGroupByLabel = "ASSET CLASS";
		}
		out.println("  <TR>" + "\n");
		out.println("    <TD>");
		out.println("<B><I>NOTE: This report is grouped and totaled by " + sGroupByLabel + "</I></B>" + "\n");
		out.println("<TD>" + "\n");
		out.println("  <TR>" + "\n");
		
		out.println("</TABLE>\n");
		
		//log usage of this report
		SMClasses.SMLogEntry log = new SMClasses.SMLogEntry(sDBID, getServletContext());
		log.writeEntry(sUserID, SMLogEntry.LOG_OPERATION_FAFIXEDASSETS, "REPORT", "FATransactionList", "[1376509371]");

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
			redirectAfterError(response, sCallingClass, sWarning, sDBID, bSelectByCategory, arrLocations);
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
				sGroupBy,
				bPrintProvisionalTransactions,
				bPrintAdjustmentTransactions,
				bPrintActualTransactions,
				bShowDetails,
				out,
				getServletContext(),
				(String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_LICENSE_MODULE_LEVEL),
				arrLocations,
				bShowAllLocations)){
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

	private void redirectAfterError(
			HttpServletResponse res, 
			String sCallingClass, 
			String sWarning, 
			String sDBID, 
			boolean bSelectByCategory,
			ArrayList<String>arrLocations){

		String sRedirect =
				"" + SMUtilities.getURLLinkBase(getServletContext()) + "" + sCallingClass + "?"
						+ "Warning=" + sWarning
						;

		if (bSelectByCategory){
			sRedirect += "&SelectByCategory=true";
		}

		sRedirect += "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID;
		
		for (int i = 0; i < arrLocations.size(); i++){
			sRedirect += "&" + FATransactionListSelect.LOCATION_PARAMETER + arrLocations.get(i) + "=Y";
		}
		
		try {
			res.sendRedirect(sRedirect);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}			
		return;
	}

}
