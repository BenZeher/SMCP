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
import SMDataDefinition.SMTableicitems;
import ServletUtilities.clsCreateHTMLFormFields;
import ServletUtilities.clsManageRequestParameters;

public class ICItemNumberMatchUpCriteriaSelection  extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
	public static final String STARTINGITEM_PARAM = "StartingItemNumber";
	public static final String ENDINGITEM_PARAM = "EndingItemNumber";
	public static final String SORTBYOURITEM_PARAM = "OurItem";
	
	public void doPost(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
	
		if (!SMAuthenticate.authenticateSMCPCredentials(
				request, 
				response, 
				getServletContext(),
				SMSystemFunctions.ICItemNumberMatchUp
			)
		){
			return;
		}
		PrintWriter out = response.getWriter();
		
	    //Get the session info:
	    HttpSession CurrentSession = request.getSession(true);
	    String sDBID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_DATABASE_ID);
	    String sCompanyName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_COMPANYNAME);
	    String title = "IC Item Number Match-Up List";
	    String subtitle = "criteria selection";
	    out.println(SMUtilities.SMCPTitleSubBGColor(title, subtitle, SMUtilities.getInitBackGroundColor(getServletContext(), sDBID), sCompanyName));
	    out.println(SMUtilities.getDatePickerIncludeString(getServletContext()));
	    String sWarning = clsManageRequestParameters.get_Request_Parameter("Warning", request);
		if (!sWarning.equalsIgnoreCase("")){
			out.println("<B><FONT COLOR=\"RED\">WARNING: " + sWarning + "</FONT></B><BR>");
		}
	    String sStatus = clsManageRequestParameters.get_Request_Parameter("Status", request);
		if (!sStatus.equalsIgnoreCase("")){
			out.println("<B>NOTE: " + sStatus + "</B><BR>");
		}
		
	    //Print a link to the first page after login:
	    out.println("<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMUserLogin?" 
				+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
				+ "\">Return to user login</A><BR>");
	    out.println("<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smic.ICMainMenu?" 
				+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
				+ "\">Return to Inventory Control Main Menu</A><BR>");
	    out.println("<A HREF=\"" + WebContextParameters.getdocumentationpageURL(getServletContext()) + "#" + Long.toString(SMSystemFunctions.ICUpdateItemPrices) 
	    	+ "\">Summary</A><BR><BR>");
		out.println ("<FORM ACTION =\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smic.ICItemNumberMatchUpGenerate\">");
		out.println("<INPUT TYPE=HIDDEN NAME='" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "' VALUE='" + sDBID + "'>");
		out.println("<INPUT TYPE=HIDDEN NAME=CallingClass VALUE=\"" 
			+ SMUtilities.getFullClassName(this.toString()) + "\">");
		out.println("<TABLE WIDTH=100% CELLPADDING=10 border=4>");
    	//Item range
		//Starting Item number:
		out.println("<TD ALIGN=RIGHT>" + "<B>Item Number Range:</B></TD>"
				+ "<TD>"
				+ "Starting with: "
				+ clsCreateHTMLFormFields.TDTextBox(
					STARTINGITEM_PARAM, 
					clsManageRequestParameters.get_Request_Parameter(STARTINGITEM_PARAM, request), 
					10, 
					SMTableicitems.sItemNumberLength, 
					""
					)
		);
		
		//Ending Item number:
		String sEndingItem = clsManageRequestParameters.get_Request_Parameter(ENDINGITEM_PARAM, request);
		if (sEndingItem.compareToIgnoreCase("") == 0){
			sEndingItem = "ZZZZZZZZZZZZZZZZ";
		}
		out.println("&nbsp;&nbsp;and ending with:"
				+ clsCreateHTMLFormFields.TDTextBox(
					ENDINGITEM_PARAM, 
					sEndingItem, 
					10, 
					SMTableicitems.sItemNumberLength, 
					""
					));
		
		out.println("</TD></TR>");
    	
		//Sorting Method
    	out.println("<TR><TD ALIGN=RIGHT><B>Sort by:</B></TD>");
		if (clsManageRequestParameters.get_Request_Parameter(SORTBYOURITEM_PARAM, request).compareTo("1") != 0){
	    	out.println("<TD>" + 
	    				"<LABEL><INPUT TYPE=\"RADIO\" NAME=\"" + SORTBYOURITEM_PARAM + "\" VALUE=0 CHECKED=\"yes\">Item Number<BR></LABEL>" + 
						"<LABEL><INPUT TYPE=\"RADIO\" NAME=\"" + SORTBYOURITEM_PARAM + "\" VALUE=1 >Vendor item number*</LABEL>");  
		}else{
	    	out.println("<TD>" + 
	    				"<LABEL><INPUT TYPE=\"RADIO\" NAME=\"" + SORTBYOURITEM_PARAM + "\" VALUE=0>Item Number<BR></LABEL>" + 
						"<LABEL><INPUT TYPE=\"RADIO\" NAME=\"" + SORTBYOURITEM_PARAM + "\" VALUE=1 CHECKED=\"yes\">Vendor item number*</LABEL>");  
		}
		out.println("</TD></TR>");
		
        out.println("</TABLE><BR><BR>");
    	out.println("<INPUT TYPE=\"SUBMIT\" VALUE=\"----List----\">");
		out.println("</FORM><BR><BR>");
		
		out.println("* When sorting by vendor item number, only items with an actual vendor item number will be listed.");
		
	    	
		out.println("</BODY></HTML>");
	}
	
	public void doGet(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
		
		doPost(request, response);
	}
}
