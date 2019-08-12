package smar;

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
import SMDataDefinition.SMTablearcustomer;
import ServletUtilities.clsCreateHTMLFormFields;
import ServletUtilities.clsManageRequestParameters;

public class ARDisplayCustomerSelect  extends HttpServlet {
	private static final long serialVersionUID = 1L;
	public void doPost(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
		if (!SMAuthenticate.authenticateSMCPCredentials(
				request, 
				response, 
				getServletContext(),
				SMSystemFunctions.ARDisplayCustomerInformation
			)
		){
			return;
		}	
		PrintWriter out = response.getWriter();
		
	    //Get the session info:
	    HttpSession CurrentSession = request.getSession(true);
	    String sCompanyName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_COMPANYNAME);
	    String sDBID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_DATABASE_ID);
	    String title = "View Customer Information";
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
	    out.println("<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smar.ARMainMenu?" 
				+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
				+ "\">Return to Accounts Receivable Main Menu</A><BR>");
	    out.println("<A HREF=\"" + WebContextParameters.getdocumentationpageURL(getServletContext()) + "#" + Long.toString(SMSystemFunctions.ARDisplayCustomerInformation) 
	    	+ "\">Summary</A><BR><BR>");
		out.println ("<FORM ACTION =\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smar.ARDisplayCustomerInformation\">");
		out.println("<INPUT TYPE=HIDDEN NAME='" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "' VALUE='" + sDBID + "'>");
		out.println("<INPUT TYPE=HIDDEN NAME=CallingClass VALUE=\"" + this.getClass().getName() + "\">");
		out.println("<TABLE WIDTH=100% CELLPADDING=10 border=4>");
		out.println("<TR>");
		
		//Order number:
		out.println("<TD>" + "<B>View customer:</B> " 
				+ clsCreateHTMLFormFields.TDTextBox(
					"CustomerNumber", 
					clsManageRequestParameters.get_Request_Parameter("CustomerNumber", request), 
					10, 
					10, 
					""
					));
		
		//Link to finder:
	    out.println("<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "SMClasses.ObjectFinder"
	    	+ "?" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
			+ "&ObjectName=Customer"
			+ "&ResultClass=FinderResults"
			+ "&SearchingClass=" + SMUtilities.getFullClassName(this.toString())
			+ "&ParameterString="
				+ "*CallingURL=" + request.getRequestURI().toString()
				+ "*" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
			+ "&ReturnField=CustomerNumber"
			+ "&SearchField1=" + SMTablearcustomer.sCustomerName
			+ "&SearchFieldAlias1=Name"
			+ "&SearchField2=" + SMTablearcustomer.sCustomerNumber
			+ "&SearchFieldAlias2=Customer%20Code"
			+ "&SearchField3=" + SMTablearcustomer.sAddressLine1
			+ "&SearchFieldAlias3=Address%20Line%201"
			+ "&SearchField4=" + SMTablearcustomer.sPhoneNumber
			+ "&SearchFieldAlias4=Phone"
			+ "&ResultListField1="  + SMTablearcustomer.sCustomerNumber
			+ "&ResultHeading1=Customer%20Number"
			+ "&ResultListField2="  + SMTablearcustomer.sCustomerName
			+ "&ResultHeading2=Customer%20Name"
			+ "&ResultListField3="  + SMTablearcustomer.sAddressLine1
			+ "&ResultHeading3=Address%20Line%201"
			+ "&ResultListField4="  + SMTablearcustomer.sPhoneNumber
			+ "&ResultHeading4=Phone"
			+ "&ResultListField5="  + SMTablearcustomer.iActive
			+ "&ResultHeading5=Active"
			+ "&ResultListField6="  + SMTablearcustomer.iOnHold
			+ "&ResultHeading6=On%20Hold"
			+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
			+ "\""
			//+ "target=\"_blank\""
			+ "> Find customer</A></TD></TR>"
			);
		
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
