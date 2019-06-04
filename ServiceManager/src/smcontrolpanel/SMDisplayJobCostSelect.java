package smcontrolpanel;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import smar.FinderResults;
import SMDataDefinition.*;
import ServletUtilities.clsServletUtilities;
import ServletUtilities.clsCreateHTMLFormFields;
import ServletUtilities.clsManageRequestParameters;

public class SMDisplayJobCostSelect  extends HttpServlet {
	//OBSOLETE? - the job cost summary is invoked from the 'View order information' screen now.
	private static final long serialVersionUID = 1L;
	public void doPost(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
	
		PrintWriter out = response.getWriter();
		if(!SMAuthenticate.authenticateSMCPCredentials(
				request, 
				response, 
				getServletContext(), 
				SMSystemFunctions.SMJobCostSummaryReport))
			{
				return;
			}

	    //Get the session info:
	    HttpSession CurrentSession = request.getSession(true);
	    String sCompanyName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_COMPANYNAME);
	    String sDBID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_DATABASE_ID);
	    String title = "Job Cost Summary";
	    String subtitle = "";
	    out.println(SMUtilities.SMCPTitleSubBGColor(title, subtitle, SMUtilities.getInitBackGroundColor(getServletContext(), sDBID), sCompanyName));
	
	    String sWarning = clsManageRequestParameters.get_Request_Parameter("Warning", request);
		if (! sWarning.equalsIgnoreCase("")){
			out.println("<B><FONT COLOR=\"RED\">WARNING: " + sWarning + "</FONT></B><BR>");
		}
	    //Print a link to the first page after login:
	    out.println("<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMUserLogin?" 
				+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
				+ "\">Return to user login</A><BR>");
	    
		out.println ("<FORM ACTION =\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMDisplayJobCostInformation\">");
		out.println("<INPUT TYPE=HIDDEN NAME='" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "' VALUE='" + sDBID + "'>");
		out.println("<INPUT TYPE=HIDDEN NAME=CallingClass VALUE=\"" + this.getClass().getName() + "\">");
		out.println("<TABLE WIDTH=100% CELLPADDING=10 BORDER=4>");
		out.println("<TR>");
		
		//Order number:
		out.println("<TD>" + "<B>Order Number:</B> " 
				+ clsCreateHTMLFormFields.TDTextBox(
					"OrderNumber", 
					clsManageRequestParameters.get_Request_Parameter("OrderNumber", request), 
					10, 
					10, 
					""
					));
		
		//Link to finder:
		out.println("&nbsp;" + "<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smar.ObjectFinder"
			+ "?ObjectName=Order"
			+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
			+ "&ResultClass=FinderResults"
			+ "&SearchingClass=" + "smcontrolpanel.SMDisplayJobCostSelect"
			+ "&ReturnField=" + "OrderNumber"
			+ "&SearchField1=" + SMTableorderheaders.sBillToName
			+ "&SearchFieldAlias1=Bill%20To%20Name"
			+ "&SearchField2=" + SMTableorderheaders.sShipToName
			+ "&SearchFieldAlias2=Ship%20To%20Name"
			+ "&SearchField3=" + SMTableorderheaders.sBillToAddressLine1
			+ "&SearchFieldAlias3=Bill%20To%20Address%20Line%201"
			+ "&SearchField4=" + SMTableorderheaders.sShipToAddress1
			+ "&SearchFieldAlias4=Ship%20To%20Address%20Line%201"
			+ "&ResultListField1="  + SMTableorderheaders.sOrderNumber
			+ "&ResultHeading1=Order%20Number"
			+ "&ResultListField2="  + SMTableorderheaders.sBillToName
			+ "&ResultHeading2=Bill%20To%20Name"
			+ "&ResultListField3="  + SMTableorderheaders.sShipToName
			+ "&ResultHeading3=Ship%20To%20Name"
			+ "&ResultListField4="
				+ clsServletUtilities.URLEncode("IF(" + SMTableorderheaders.datOrderCanceledDate + "<'1950-01-01','N/A'," 
				+ "CONCAT('<FONT COLOR=RED>', DATE_FORMAT(" + SMTableorderheaders.datOrderCanceledDate + ",'%c/%e/%Y'), '</FONT>')) AS 'CANCELEDDATE'")
			+ "&" + FinderResults.RESULT_FIELD_ALIAS + "4=CANCELEDDATE"
			+ "&ResultHeading4=Canceled"
			//+ "&ParameterString=*" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
			+ "\"> Find order</A></TD>");
		
		out.println("</TR></TABLE>");
		
		out.println("<BR>");
		out.println("<INPUT TYPE=\"SUBMIT\" VALUE=\"----View----\">");
		out.println("</FORM>");
	    	
		out.println("</BODY></HTML>");
	}
	
	public void doGet(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
		
		doPost(request, response);
	}
}
