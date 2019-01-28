package smcontrolpanel;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import ConnectionPool.WebContextParameters;
import SMDataDefinition.SMTableproposals;
import ServletUtilities.clsManageRequestParameters;
import ServletUtilities.clsServletUtilities;

public class SMProposalPrintSelection extends HttpServlet {

	public static final String PRINT_BUTTON_NAME = "PRINTBUTTON";
	public static final String PRINT_BUTTON_LABEL = "Print proposal";
	public static final String EMAIL_BUTTON_NAME = "EMAILBUTTON";
	public static final String EMAIL_BUTTON_LABEL = "Email proposal";
	public static final String PRINT_LOGO_PARAM = "PRINTLOGO";
	public static final String PRINT_LOGO_LABEL = "Print logo on proposal?";
	public static final String EMAIL_TO_SELF_PARAM = "EMAILTOSELF";
	public static final String EMAIL_TO_SELF_LABEL = "Email a copy to yourself?";
	public static final String EMAIL_ADDRESSES_PARAM = "EMAILADDRESSES";
	public static final String EMAIL_ADDRESSES_LABEL = "Enter email addresses, separated by commas:";
	public static final String COMMAND_FLAG = "COMMANDFLAG";
	public static final String PRINTRECEIPTCOMMAND_VALUE = "PRINTWORKORDERRECEIPT";
	public static final String EMAILRECEIPTCOMMAND_VALUE = "EMAILWORKORDERRECEIPT";
	//TODO
	public static final String EMAIL_MESSAGE_PARAM = "EMAILMESSAGE";
	public static final String EMAIL_MESSAGE_LABEL = "Enter any message you wish to include:";
	//
	public static final String NUMBER_OF_PROPOSAL_COPIES = "NOOFPROPOSALCOPIES";

	//Colors
	//private static final String COMMON_TABLE_BG_COLOR = "#CCFFB2";
	private static final String PRINT_TABLE_BG_COLOR = "#99CCFF";
	//private static final String EMAIL_TABLE_BG_COLOR = "#FFBCA2";
	
	private static final long serialVersionUID = 1L;
	private static final String sCalledClass = "smcontrolpanel.SMProposalPrintAction";
	public void doPost(HttpServletRequest request,
			HttpServletResponse response)
	throws ServletException, IOException {

		if (!SMAuthenticate.authenticateSMCPCredentials(
				request, 
				response, 
				getServletContext(),
				SMSystemFunctions.SMEditProposals
		)
		){
			return;
		}

		PrintWriter out = response.getWriter();

		//Get the session info:
		HttpSession CurrentSession = request.getSession(true);
		String sCompanyName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_COMPANYNAME);
		String sDBID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_DATABASE_ID);
		String sUserName =  (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERNAME);

		String title = "Print Proposals";
		String subtitle = "";

		out.println(SMUtilities.SMCPTitleSubBGColor(
			title, subtitle, SMUtilities.getInitBackGroundColor(getServletContext(), sDBID), sCompanyName));

		String sWarning = clsManageRequestParameters.get_Request_Parameter("Warning", request);
		String sStatus = clsManageRequestParameters.get_Request_Parameter("Status", request);
		if (! sWarning.equalsIgnoreCase("")){
			out.println("<B><FONT COLOR=\"RED\">WARNING: " + sWarning + "</FONT></B><BR>");
		}
		if (! sStatus.equalsIgnoreCase("")){
			out.println("<B>" + sStatus + "</B><BR>");
		}
		
		//Print a link to the first page after login:
		out.println("<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) 
				+ "smcontrolpanel.SMUserLogin?" 
				+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
				+ "\">Return to user login</A><BR>");
		out.println("<A HREF=\"" + WebContextParameters.getdocumentationpageURL(getServletContext()) + "#" + Long.toString(SMSystemFunctions.SMEditProposals) 
				+ "\">Summary</A><BR><BR>");
		out.println ("<FORM ID='MAINFORM' NAME='MAINFORM' ACTION =\"" 
				+ SMUtilities.getURLLinkBase(getServletContext()) 
				+ sCalledClass + "\" METHOD='POST'>");
		String sTrimmedOrderNumber = clsManageRequestParameters.get_Request_Parameter(SMTableproposals.strimmedordernumber, request);
		out.println("<INPUT TYPE=HIDDEN NAME='" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "' VALUE='" + sDBID + "'>");
		out.println("<INPUT TYPE=HIDDEN NAME=CallingClass VALUE=\"" + this.getClass().getName() + "\">");
		out.println("<INPUT TYPE=HIDDEN NAME='" + SMTableproposals.strimmedordernumber + "' VALUE='" + sTrimmedOrderNumber + "'>");
		out.println("<INPUT TYPE=HIDDEN NAME='" + NUMBER_OF_PROPOSAL_COPIES + "' VALUE='" + "1" + "'>");
		out.println("<script type='text/javascript' src='scripts/gen_validatorv31.js'></script>\n");
		out.println(sStyleScripts());
		out.println(sCommandScripts());
		//Store which command button the user has chosen:
		out.println("<INPUT TYPE=HIDDEN NAME=\"" + COMMAND_FLAG + "\" VALUE=\"" + "" + "\""
				+ " id=\"" + COMMAND_FLAG + "\""
				+ "\">");

		String s = "";
		//Start the outer table here:
		//s += "<TABLE style=\" title:ParentTable; border-style:solid; border-color:black; font-size:small; font-family:Arial; width:100%\">\n";		
		
		//Common table:
		//try {
		//	s += createCommonTable() + "\n";
		//} catch (Exception e) {
		//	s += "Error printing common table - " + e.getMessage();
		//}
		
		try {
			s += createPrintTable(request, sTrimmedOrderNumber, sUserName);
		} catch (Exception e) {
			s += "Error printing print options table - " + e.getMessage();
		}
		
		//Close the parent table:
		//s += "</TABLE style=\" title:ENDParentTable; \">";
		
		out.println(s);
		out.println("</FORM>");
		out.println("</BODY></HTML>");
	}
	
	private String createPrintTable(HttpServletRequest req,
		String sTrimmedOrderNumber,
		String sUser) throws Exception{
		String sNumberOfColumns = "1";
		String s = "";
		s += "<TABLE  class = \"innermost\" \" style=\" title:PrintTable; padding: 0px; background-color:" + PRINT_TABLE_BG_COLOR + "; \" width=100% >\n";

		//Header:
		s += "<TR><TD COLSPAN=" + sNumberOfColumns + ">"
			+ "<U><B>PRINT OPTIONS</B></U>"
			+ "</TD>"
			+ "</TR>"
		;
		
		/*
		s += "<TR>";
		s += "<TD>" + "<B>Number of copies:</B>" + "&nbsp;"
   			+ " <SELECT "
			+ " NAME = \"" + NUMBER_OF_PROPOSAL_COPIES + "\""
			+ " ID = \"" + NUMBER_OF_PROPOSAL_COPIES + "\""
			+ ">";
		
		long lNumberOfProposalCopies;
		try {
			lNumberOfProposalCopies = Long.parseLong(SMUtilities.get_Request_Parameter(NUMBER_OF_PROPOSAL_COPIES, req));
		} catch (Exception e) {
			lNumberOfProposalCopies = 1;
		}
		for (long l = 1; l <= 6; l++){
			if (l== lNumberOfProposalCopies){
				s += "<OPTION SELECTED VALUE = \"" + Long.toString(l) + "\""
					+ ">" + Long.toString(l)
					+ "</OPTION>";
			}else{
				s += "<OPTION VALUE = \"" + Long.toString(l) + "\""
				+ ">" + Long.toString(l)
				+ "</OPTION>";
			}
		}
		s += "</SELECT><BR>";
		
		s += "</TR>";
		*/
		
		//Print logo?
		s += "<TR>";
		s += "<TD>" + "<B>" + PRINT_LOGO_LABEL + "</B>&nbsp;"
			+ "<INPUT TYPE=CHECKBOX "
			+ clsServletUtilities.CHECKBOX_CHECKED_STRING
			+ " NAME=\"" + PRINT_LOGO_PARAM + "\""
			+ " id = \"" + PRINT_LOGO_PARAM + "\""
			+ " width=0.25>"
			+ "</TD>"
		;
		s += "</TR>";
		
		s += "<TR>";
		s += "<TD><button type=\"button\""
				+ " value=\"" + PRINT_BUTTON_LABEL + "\""
				+ " name=\"" + PRINT_BUTTON_LABEL + "\""
				+ " onClick=\"print();\">"
				+ PRINT_BUTTON_LABEL
				+ "</button>\n";
		/* - TJR turned off until we get this working - 4/7/2014
		s += "&nbsp;&nbsp;&nbsp;&nbsp;<button type=\"button\""
				+ " value=\"" + EMAIL_BUTTON_LABEL + "\""
				+ " name=\"" + EMAIL_BUTTON_LABEL + "\""
				+ " onClick=\"email();\">"
				+ EMAIL_BUTTON_LABEL
				+ "</button>\n"
				+ "&nbsp;To:&nbsp;"
				
				+ "<INPUT TYPE=TEXT"
				+ " NAME=\"" + EMAIL_ADDRESSES_PARAM + "\""
				+ " id = \"" + EMAIL_ADDRESSES_PARAM + "\""
				//+ " VALUE=\"" + "" + "\""
				+ " SIZE=" + "40"
				+ " MAXLENGTH=128"
				+ ">" + EMAIL_ADDRESSES_LABEL + "</TD>";
		*/
		s += "</TR>";
		
		//Close the table:
		s += "</TABLE style = \" title:PrintTable; \">\n";
		return s;
	}

	private String sStyleScripts(){
		String s = "";
		String sBorderSize = "1";
		String sRowHeight = "22px";
		s += "<style type=\"text/css\">\n";
		
		//Layout table:
		s +=
			"table.innermost {"
			+ "border-width: " + sBorderSize + "px; "
			+ "border-spacing: 2px; "
			+ "border-style: solid; "
			//+ "border-style: none; "
			+ "border-color: black; "
			+ "border-collapse: separate; "
			+ "width: 100%; "
			+ "font-size: " + "small" + "; "
			+ "font-family : Arial; "
			+ "color: black; "
			//+ "background-color: white; "
			+ "}"
			+ "\n"
			;
		
		//This is the def for a left aligned field:
		s +=
			"td.fieldleftaligned {"
			+ "height: " + sRowHeight + "; "
			+ "font-weight: bold; "
			+ "text-align: left; "
			+ "}"
			+ "\n"
			;
		
		//This is the def for a right aligned field:
		s +=
			"td.fieldrightaligned {"
			+ "height: " + sRowHeight + "; "
			+ "font-weight: bold; "
			+ "text-align: right; "
			+ "}"
			+ "\n"
			;
		
		//This is the def for a label field:
		s +=
			"td.fieldlabel {"
			+ "height: " + sRowHeight + "; "
			//+ "border-width: " + sBorderSize + "px; "
			//+ "padding: 2px; "
			//+ "border-style: none; "
			//+ "border-color: white; "
			//+ "vertical-align: text-middle;"
			//+ "background-color: black; "
			+ "font-weight: bold; "
			+ "text-align: right; "
			//+ "color: black; "
			//+ "height: 50px; "
			+ "}"
			+ "\n"
			;
		
		//This is the def for a read-only field:
		s +=
			"td.readonlyfield {"
			+ "height: " + sRowHeight + "; "
			//+ "border-width: " + sBorderSize + "px; "
			//+ "padding: 2px; "
			//+ "border-style: none; "
			//+ "border-color: white; "
			//+ "vertical-align: text-middle;"
			//+ "background-color: black; "
			+ "font-weight: normal; "
			+ "text-align: left; "
			//+ "color: black; "
			//+ "height: 50px; "
			+ "}"
			+ "\n"
			;

		//This is the def for a control on the screen:
		s +=
			"td.fieldcontrol {"
			+ "height: " + sRowHeight + "; "
			//+ "border-width: " + sBorderSize + "px; "
			//+ "padding: 2px; "
			//+ "border-style: none; "
			//+ "border-color: white; "
			//+ "vertical-align: text-middle;"
			//+ "background-color: black; "
			+ "text-align: left; "
			//+ "color: black; "
			//+ "height: 50px; "
			+ "}"
			+ "\n"
			;
		
		//This is the def for an underlined heading on the screen:
		s +=
			"td.fieldheading {"
			+ "height: " + sRowHeight + "; "
			+ "font-weight: bold; "
			+ "text-align: left; "
			+ "text-decoration:underline; "
			+ "}"
			+ "\n"
			;

		s += "</style>"
			+ "\n"
			;

		return s;
	}

	private String sCommandScripts(){
		String s = "";
		
		s += "<NOSCRIPT>\n"
			+ "    <font color=red>\n"
			+ "    <H3>This page requires that JavaScript be enabled to function properly</H3>\n"
			+ "    </font>\n"
			+ "</NOSCRIPT>\n"
		;

		s += "<script type='text/javascript'>\n";

		//***********************
		//Email
		s += "function email(){\n"
			+ "    document.getElementById(\"" + COMMAND_FLAG + "\").value = \"" 
					 + EMAILRECEIPTCOMMAND_VALUE + "\";\n"
			+ "    document.forms[\"MAINFORM\"].submit();\n"
			+ "}\n"
		;

		//Print
		s += "function print(){\n"
			+ "    document.getElementById(\"" + COMMAND_FLAG + "\").value = \"" 
					 + PRINTRECEIPTCOMMAND_VALUE + "\";\n"
			+ "    document.forms[\"MAINFORM\"].submit();\n"
			+ "}\n"
		;

		s += "</script>\n";
		return s;
	}
	
	public void doGet(HttpServletRequest request,
			HttpServletResponse response)
	throws ServletException, IOException {

		doPost(request, response);
	}
}