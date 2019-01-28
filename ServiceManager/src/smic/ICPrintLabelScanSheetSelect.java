package smic;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import smcontrolpanel.SMAuthenticate;
import smcontrolpanel.SMSystemFunctions;
import smcontrolpanel.SMUtilities;
import ConnectionPool.WebContextParameters;
import ServletUtilities.clsManageRequestParameters;

public class ICPrintLabelScanSheetSelect extends HttpServlet {

	public static final String PARAM_STARTINGITEM = "STARTINGITEM";
	public static final String PARAM_ENDINGITEM = "ENDINGITEM";
	public static final String PRINT_BUTTON = "PRINTBUTTON";
	public static final String PRINT_BUTTON_LABEL = "Print scan sheet";
	
	private static final long serialVersionUID = 1L;
	private static String sCalledClassName = "ICPrintLabelScanSheetAction";

	public void doPost(HttpServletRequest request,
				HttpServletResponse response)
				throws ServletException, IOException {
		if(!SMAuthenticate.authenticateSMCPCredentials(
				request,	
				response,
				getServletContext(),
				SMSystemFunctions.ICPrintLabelScanSheets
			)
		){
			return;
		}
		PrintWriter out = response.getWriter();
	    //Get the session info:
	    HttpSession CurrentSession = request.getSession(true);
	    String sCompanyName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_COMPANYNAME);
	    String sDBID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_DATABASE_ID);
	    String title = "Print Barcode Label Scan Sheets.";
	    String subtitle = "";
	    out.println(SMUtilities.SMCPTitleSubBGColor(title, subtitle, SMUtilities.getInitBackGroundColor(getServletContext(), sDBID), sCompanyName));

		//If there is a warning from trying to input previously, print it here:
		String sWarning = clsManageRequestParameters.get_Request_Parameter("Warning", request);
	    if (sWarning.compareToIgnoreCase("") != 0){
			out.println("<B><FONT COLOR=\"RED\">WARNING: " + sWarning + "</FONT></B><BR>");
		}
		//If there is a status from trying to input previously, print it here:
		String sStatus = clsManageRequestParameters.get_Request_Parameter("Status", request);
		if (sStatus.compareToIgnoreCase("") != 0){
			out.println("<B>" + sStatus + "</B><BR>");
		}
	    
	    //Print a link to the first page after login:
	    out.println("<BR><A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMUserLogin?" 
				+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
				+ "\">Return to user login</A><BR>");
	    out.println("<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smic.ICMainMenu?" 
				+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
				+ "\">Return to Inventory Control Main Menu</A><BR>");
	    out.println("<A HREF=\"" + WebContextParameters.getdocumentationpageURL(getServletContext()) + "#" + Long.toString(SMSystemFunctions.ICPrintLabelScanSheets) 
	    		+ "\">Summary</A><BR>");
	    out.println("<FORM NAME='MAINFORM' ACTION='" + SMUtilities.getURLLinkBase(getServletContext()) 
	    		+ "smic." + sCalledClassName + "' METHOD='POST'>");
	    out.println("<INPUT TYPE=HIDDEN NAME='" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "' VALUE='" + sDBID + "'>");
	    out.println("<INPUT TYPE=HIDDEN NAME=\"" + "CallingClass" + "\" VALUE=\"" + "ICPrintLabelScanSheetSelect" + "\">");
	    	    
	    out.println("<BR>Print scannable barcode sheets for items:<BR>");
	    out.println("<B>STARTING</B> with item number: "
	    	+ "<INPUT TYPE=TEXT NAME=\"" + PARAM_STARTINGITEM 
	    	+ "\" VALUE=\"" + clsManageRequestParameters.get_Request_Parameter(PARAM_STARTINGITEM, request)
	    	+ "\"SIZE=24 MAXLENGTH=24 STYLE="
	    	+ "\"width: 1.2in; height: 0.25in\">"
	    );
	    
	    out.println("&nbsp;And <B>ENDING</B> with item number: "
		    + "<INPUT TYPE=TEXT NAME=\"" + PARAM_ENDINGITEM 
		    + "\" VALUE=\"" + clsManageRequestParameters.get_Request_Parameter(PARAM_ENDINGITEM, request)
		    + "\"SIZE=24 MAXLENGTH=24 STYLE="
		    + "\"width: 1.2in; height: 0.25in\">"
		    + "<BR>"
		);
	    
				
		out.println("<INPUT TYPE=SUBMIT NAME='" 
			+ PRINT_BUTTON 
			+ "' VALUE='" + PRINT_BUTTON_LABEL 
			+ "' STYLE='width: 2.00in; height: 0.24in'>"
		);
		out.println("</FORM>");
		out.println("</BODY></HTML>");
	}
	
	public void doGet(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
		
		doPost(request, response);
	}
}