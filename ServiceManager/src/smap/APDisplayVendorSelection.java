package smap;

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
import SMDataDefinition.SMTableicvendors;
import ServletUtilities.clsCreateHTMLFormFields;
import ServletUtilities.clsManageRequestParameters;

public class APDisplayVendorSelection  extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private String sCompanyName = "";
	private String sDBID = "";
	public void doPost(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
		if (!SMAuthenticate.authenticateSMCPCredentials(
				request, 
				response, 
				getServletContext(),
				SMSystemFunctions.APDisplayVendorInformation
			)
		){
			return;
		}	
		PrintWriter out = response.getWriter();
		
	    //Get the session info:
	    HttpSession CurrentSession = request.getSession(true);
	    sCompanyName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_COMPANYNAME);
	    sDBID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_DATABASE_ID);
	    String title = "View Vendor Information";
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
	    out.println("<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smap.APMainMenu?" 
				+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
				+ "\">Return to Accounts Payable Main Menu</A><BR>");
	    out.println("<A HREF=\"" + WebContextParameters.getdocumentationpageURL(getServletContext()) + "#" + Long.toString(SMSystemFunctions.APDisplayVendorInformation) 
	    	+ "\">Summary</A><BR><BR>");
		out.println ("<FORM ACTION =\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smap.APDisplayVendorInformation\">");
		out.println("<INPUT TYPE=HIDDEN NAME='" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "' VALUE='" + sDBID + "'>");
		out.println("<INPUT TYPE=HIDDEN NAME=CallingClass VALUE=\"" + this.getClass().getName() + "\">");
		out.println("<TABLE WIDTH=100% CELLPADDING=10 border=4>");
		out.println("<TR>");
		
		//Vendor:
		out.println("<TD>" + "<B>View Vendor:</B> " 
				+ clsCreateHTMLFormFields.TDTextBox(
					"VendorNumber", 
					clsManageRequestParameters.get_Request_Parameter("VendorNumber", request), 
					10, 
					10, 
					""
					));
		
		//Link to finder:
	    out.println("<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smar.ObjectFinder"
			+ "?ObjectName=Vendor"
			+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
			+ "&ResultClass=FinderResults"
			+ "&SearchingClass=" + SMUtilities.getFullClassName(this.toString())
			+ "&ParameterString="
				+ "*CallingURL=" + request.getRequestURI().toString()
				+ "*" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
				+ "&ReturnField=VendorNumber" 
				+ "&SearchField1=" + SMTableicvendors.svendoracct
				+ "&SearchFieldAlias1=Account"
				+ "&SearchField2=" + SMTableicvendors.sname
				+ "&SearchFieldAlias2=Name"
				+ "&SearchField3=" + SMTableicvendors.saddressline1
				+ "&SearchFieldAlias3=Address%201"
				+ "&SearchField4=" + SMTableicvendors.saddressline2
				+ "&SearchFieldAlias4=Address%202"
				+ "&SearchField5=" + SMTableicvendors.sphonenumber
			    + "&SearchFieldAlias5=Phone"
				+ "&ResultListField1="  + SMTableicvendors.svendoracct
				+ "&ResultHeading1=Vendor%20Acct."
				+ "&ResultListField2="  + SMTableicvendors.sname
				+ "&ResultHeading2=Vendor%20Name"
				+ "&ResultListField3="  + SMTableicvendors.saddressline1
				+ "&ResultHeading3=Address%201"
				+ "&ResultListField4="  + SMTableicvendors.saddressline2
				+ "&ResultHeading4=Address%202"
				+ "&ResultListField5="  + SMTableicvendors.saddressline3
				+ "&ResultHeading5=Address%203"
				+ "&ResultListField6="  + SMTableicvendors.saddressline4
				+ "&ResultHeading6=Address%204"
				+ "&ResultListField7="  + SMTableicvendors.scity
				+ "&ResultHeading7=City"
				+ "&ResultListField8="  + SMTableicvendors.sstate
				+ "&ResultHeading8=State"
				+ "&ResultListField9="  + SMTableicvendors.spostalcode
				+ "&ResultHeading9=Zip"
				+ "&ResultListField10="  + SMTableicvendors.sphonenumber
				+ "&ResultHeading10=Phone%20No."
				+ "&ResultListField11="  + SMTableicvendors.sdefaultexpenseacct
				+ "&ResultHeading11=Default%20GL"
				+ "&ResultListField12="  + "IF(" + SMTableicvendors.iactive + " = 1, 'Y', 'N')"
				+ "&ResultHeading12=Active?"
			+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
			+ "\""
			//+ "target=\"_blank\""
			+ "> Find vendor</A></TD></TR>"
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
