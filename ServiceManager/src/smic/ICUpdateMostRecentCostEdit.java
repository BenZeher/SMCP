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

public class ICUpdateMostRecentCostEdit extends HttpServlet {

	private static final long serialVersionUID = 1L;
	private static String sObjectName = "Most Recent Cost";
	private static String sCalledClassName = "ICUpdateMostRecentCostAction";

	public void doPost(HttpServletRequest request,
				HttpServletResponse response)
				throws ServletException, IOException {

		PrintWriter out = response.getWriter();
		if (!SMAuthenticate.authenticateSMCPCredentials(
				request, 
				response, 
				getServletContext(), 
				-1L))
		{
			return;
		}

	    //Get the session info:
	    HttpSession CurrentSession = request.getSession(true);
	    String sCompanyName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_COMPANYNAME);
	    String sDBID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_DATABASE_ID);

	    String sItemNumber = clsManageRequestParameters.get_Request_Parameter(ICItem.ParamItemNumber, request);
	    String sMostRecentCost = clsManageRequestParameters.get_Request_Parameter(ICItem.ParamMostRecentCost, request);
	    String sPhysicalInventoryID = clsManageRequestParameters.get_Request_Parameter(ICPhysicalInventoryEntry.ParamID, request);
	    
		String title = "";
		String subtitle = "";
		
		//In any other case, such as the possibility that this is a 'resubmit', we need to edit the item:
    	title = "Edit " + sObjectName + " for item " + sItemNumber;
	    subtitle = "";
	    out.println(SMUtilities.SMCPTitleSubBGColor(title, subtitle, SMUtilities.getInitBackGroundColor(getServletContext(), sDBID), sCompanyName));
	    
		//If there is a warning from trying to input previously, print it here:
		String sWarning = clsManageRequestParameters.get_Request_Parameter("Warning", request);
	    if (! sWarning.equalsIgnoreCase("")){
			out.println("<B><FONT COLOR=\"RED\">WARNING: " + sWarning + "</FONT></B><BR>");
		}
		//If there is a status from trying to input previously, print it here:
		String sStatus = clsManageRequestParameters.get_Request_Parameter("Status", request);
	    if (! sStatus.equalsIgnoreCase("")){
			out.println("<B>Status: " + sStatus + "</B><BR>");
		}
	    out.println("<TABLE BORDER=0 WIDTH=100%>");
	    
	    //Print a link to the first page after login:
	    out.println("<TR>");
	    out.println("<TD>");
	    out.println("<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMUserLogin?" 
				+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
				+ "\">Return to user login</A><BR>");
	    out.println("<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smic.ICMainMenu?" 
				+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
				+ "\">Return to Inventory Control Main Menu</A><BR>");
	    out.println("<A HREF=\"" + WebContextParameters.getdocumentationpageURL(getServletContext()) + "#" + Long.toString(SMSystemFunctions.ICEditPhysicalInventory) 
	    		+ "\">Summary</A>");
	    
	    out.println("<BR>");
	    
	    //Create links to re-print the variance report:
		out.println(
	    		"<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smic.ICPrintPhysicalInventoryVarianceReport"
	    		+ "?CallingClass=" + "smic.ICEditPhysicalInventory"
	    		+ "&" + ICPhysicalInventoryEntry.ParamID + "=" + sPhysicalInventoryID
	    		+ "&OnlyShowVariances=no"
	    		+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
	    		+ "\">Print variance report showing all items</A>"		
			);
			
		out.println(
	    		"&nbsp;&nbsp;<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smic.ICPrintPhysicalInventoryVarianceReport"
	    		+ "?CallingClass=" + "smic.ICEditPhysicalInventory"
	    		+ "&" + ICPhysicalInventoryEntry.ParamID + "=" + sPhysicalInventoryID
	    		+ "&OnlyShowVariances=yes"
	    		+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
	    		+ "\">Print variance report only showing counts that vary</A><BR>"		
			);
	    
	    out.println("</TD>");
	    out.println("</TR>");
	    
	    out.println("NOTE: Editing a most recent cost here updates the most recent cost in the item master file.<BR>");
	    
	    //Create a form for updating the most recent cost:
	    out.println("<TR>");
	    out.println("<TD>");
	    out.println("<FORM ACTION='" + SMUtilities.getURLLinkBase(getServletContext()) 
	    		+ "smic." + sCalledClassName + "' METHOD='POST'>");
	    out.println("<INPUT TYPE=HIDDEN NAME='" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "' VALUE='" + sDBID + "'>");
	    out.println("<INPUT TYPE=HIDDEN NAME='" + ICPhysicalInventoryEntry.ParamID 
	    		+ "' VALUE='" + sPhysicalInventoryID + "'>");
	    out.println("<INPUT TYPE=HIDDEN NAME='CallingClass' VALUE='" 
	    	+ SMUtilities.getFullClassName(this.toString()) + "'>");
	    
	    out.println("<INPUT TYPE=HIDDEN NAME='" + ICItem.ParamItemNumber + "' VALUE='" + sItemNumber + "'>");
	    out.println("<INPUT TYPE=TEXT NAME=\"" + ICItem.ParamMostRecentCost
	    		+ "\" VALUE=\"" + sMostRecentCost + "\""
	    		+ "\" SIZE=18 MAXLENGTH=15 STYLE=\"width: 1.75in; height: 0.25in\">&nbsp;");
	    out.println("<INPUT TYPE=SUBMIT NAME='SubmitCost' "
	    		+ "VALUE=\"Update most recent cost\" STYLE='height: 0.24in'>");
	    out.println("</FORM>");
	    out.println("<TD>");
	    out.println("</TR>");
	    out.println("</TABLE>");
		
		out.println("</BODY></HTML>");
	}
	
	public void doGet(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
		
		doPost(request, response);
	}
}
