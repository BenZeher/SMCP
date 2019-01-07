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

public class ARSelectCustomerStatistics extends HttpServlet {

	private static final long serialVersionUID = 1L;
	private static String sObjectName = "Customer";
	private static String sCalledClassName = "ARDisplayStatistics";
	
	private static String sCompanyName = "";
	private static String sDBID = "";
	public void doPost(HttpServletRequest request,
				HttpServletResponse response)
				throws ServletException, IOException {
		
		PrintWriter out = response.getWriter();
		if(!SMAuthenticate.authenticateSMCPCredentials(
				request, 
				response, 
				getServletContext(), 
				SMSystemFunctions.ARCustomerStatistics))
			{
				return;
			}

	    //Get the session info:
	    HttpSession CurrentSession = request.getSession(true);
	    sCompanyName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_COMPANYNAME);
	    sDBID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_DATABASE_ID);
	    String title = "View customer statistics";
	    String subtitle = "";
	    out.println(SMUtilities.SMCPTitleSubBGColor(title, subtitle, SMUtilities.getInitBackGroundColor(getServletContext(), sDBID), sCompanyName));

	    //Print a link to the first page after login:
	    out.println("<BR><A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMUserLogin?" 
				+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
				+ "\">Return to user login</A><BR>");
	    out.println("<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smar.ARMainMenu?" 
				+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
				+ "\">Return to Accounts Receivable Main Menu</A><BR>");
	    out.println("<A HREF=\"" + WebContextParameters.getdocumentationpageURL(getServletContext()) + "#" + Long.toString(SMSystemFunctions.ARCustomerStatistics) 
	    		+ "\">Summary</A><BR><BR>");
	    out.println("<FORM NAME='MAINFORM' ACTION='" + SMUtilities.getURLLinkBase(getServletContext()) + "smar." + sCalledClassName + "' METHOD='POST'>");
	    out.println("<INPUT TYPE=HIDDEN NAME='" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "' VALUE='" + sDBID + "'>");
	    String sOutPut = "";

	    String sEditCode = "";
	    if (request.getParameter("EditCode") != null){
	    	sEditCode = request.getParameter("EditCode");
	    }
		sOutPut = 
			"<P>Enter " + sObjectName + " Code: <INPUT TYPE=TEXT NAME=\"" 
			+ sObjectName + "\""
			+ " VALUE = \"" + sEditCode + "\""
			+ " SIZE=28 MAXLENGTH=" 
			+ Integer.toString(SMTablearcustomer.sCustomerNumberLength) 
			+ " STYLE=\"width: 2.41in; height: 0.25in\">";

		//Link to finder:
		sOutPut += "<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smar.ObjectFinder"
			+ "?ObjectName=Customer"
			+ "&ResultClass=FinderResults"
			+ "&SearchingClass=smar.ARSelectCustomerStatistics"
			+ "&ReturnField=EditCode"
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
			+ "&ParameterString=*" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
			+ "\"> Find customer</A></P>";
		
		sOutPut += "<P><INPUT TYPE=SUBMIT NAME='SubmitEdit' VALUE='View selected customer's statistics' STYLE='width: 2.00in; height: 0.24in'></P>";
		sOutPut = sOutPut + "</FORM>";
		out.println(sOutPut);
		
		out.println("</BODY></HTML>");
	}
	
	public void doGet(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
		
		doPost(request, response);
	}
}