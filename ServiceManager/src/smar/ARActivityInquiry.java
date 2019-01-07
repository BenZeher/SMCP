package smar;

import java.io.IOException;
import java.io.PrintWriter;
//import java.sql.ResultSet;
//import java.sql.SQLException;
import java.util.ArrayList;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import smcontrolpanel.SMAuthenticate;
import smcontrolpanel.SMSystemFunctions;
import smcontrolpanel.SMUtilities;
import ConnectionPool.WebContextParameters;
import SMDataDefinition.*;
import ServletUtilities.clsDateAndTimeConversions;

public class ARActivityInquiry extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static String sCompanyName = "";
	private static String sDBID = "";
	public void doPost(HttpServletRequest request,
				HttpServletResponse response)
				throws ServletException, IOException {
		
		//This page just accepts a customer number, and validates it or returns to itself . . . 
		PrintWriter out = response.getWriter();
	    if (!SMAuthenticate.authenticateSMCPCredentials(
	    		request, 
	    		response, 
	    		getServletContext(), 
	    		SMSystemFunctions.ARCustomerActivity)){
	    	return;
	    }
	    //Get the session info:
	    HttpSession CurrentSession = request.getSession(true);
	    sCompanyName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_COMPANYNAME);
	    sDBID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_DATABASE_ID);
	    
	    String sWarning = "";
	    if (request.getParameter("Warning") != null){
	    	sWarning = request.getParameter("Warning");
	    }
	    
	    String sCustomerNumber = "";
	    
	    if (request.getParameter("CustomerNumber") != null){
	    	sCustomerNumber = request.getParameter("CustomerNumber");
	    }
	    
	    String sStartingDate = "1/1/1900";
	    if (request.getParameter("StartingDate") != null){
	    	sCustomerNumber = request.getParameter("StartingDate");
	    }

	    String sEndingDate = clsDateAndTimeConversions.now("M/d/yyyy");
	    if (request.getParameter("EndingDate") != null){
	    	sCustomerNumber = request.getParameter("EndingDate");
	    }

    	boolean bSelectAllTypes = true;
    	if (ARUtilities.get_Request_Parameter("SelectAllTypes", request).compareToIgnoreCase("0") == 0){
    		bSelectAllTypes = false;
    	}
	    
	    String title = "Customer activity inquiry";
	    String subtitle = "";
	    out.println(SMUtilities.SMCPTitleSubBGColor(title, subtitle, SMUtilities.getInitBackGroundColor(getServletContext(), sDBID), sCompanyName));
	    out.println(SMUtilities.getDatePickerIncludeString(getServletContext()));
	    
	    if (sWarning.compareToIgnoreCase("") != 0){
			out.println("<B><FONT COLOR=\"RED\">WARNING: " + sWarning + "</FONT></B><BR>");
		}
	    
	    //Print a link to the first page after login:
	    out.println("<BR><A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMUserLogin?" 
				+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
				+ "\">Return to user login</A><BR>");
	    out.println("<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smar.ARMainMenu?" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID + "\">Return to Accounts Receivable Main Menu</A><BR>");
	    out.println("<A HREF=\"" + WebContextParameters.getdocumentationpageURL(getServletContext()) + "#" + Long.toString(SMSystemFunctions.ARCustomerActivity) 
	    		+ "\">Summary</A><BR><BR>");
	    out.println("<FORM NAME='MAINFORM' ACTION='" + SMUtilities.getURLLinkBase(getServletContext()) + "smar.ARActivityDisplay" 
	    		+ "' METHOD='POST'>");
	    out.println("<INPUT TYPE=HIDDEN NAME='" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "' VALUE='" + sDBID + "'>");
	    out.println("<INPUT TYPE=HIDDEN NAME='OpenTransactionsOnly' VALUE='true'>");
	    out.println("<TABLE WIDTH=100% BORDER=2>");
	    out.println(
			"<TR><TD WIDTH=20%><B>Enter customer number: </B></TD>" +
			"<TD WIDTH=80% ALIGN=LEFT><INPUT TYPE=TEXT NAME=\"" 
			+ "CustomerNumber" + "\""
			+ " VALUE = \"" + sCustomerNumber + "\""
			+ " SIZE=28 MAXLENGTH=" 
			+ Integer.toString(SMTablearcustomer.sCustomerNumberLength) 
			+ " STYLE=\"width: 2.41in; height: 0.25in\">"
			);

		//Link to finder:
	    out.println("<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smar.ObjectFinder"
	    	+ "?" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
			+ "&ObjectName=Customer"
			+ "&ResultClass=FinderResults"
			+ "&SearchingClass=smar.ARActivityInquiry"
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
	    
	    
	    //Starting date:
	    out.println(
				"<TR><TD WIDTH=20%><B>Starting with document date: </B></TD>" +
				"<TD WIDTH=80% ALIGN=LEFT><INPUT TYPE=TEXT NAME=\"" 
				+ "StartingDate" + "\""
				+ " VALUE = \"" + sStartingDate + "\""
				+ " SIZE=28 MAXLENGTH=" 
				+ "10" 
				+ " STYLE=\"width: 1.5in; height: 0.25in\">"
				+ SMUtilities.getDatePickerString("StartingDate", getServletContext())
				+ "&nbsp;(mm/dd/yyyy)</TD></TR>"
				);
	    
	    //Ending date
	    out.println(
				"<TR><TD WIDTH=20%><B>Ending with document date: </B></TD>" +
				"<TD WIDTH=80% ALIGN=LEFT><INPUT TYPE=TEXT NAME=\"" 
				+ "EndingDate" + "\""
				+ " VALUE = \"" + sEndingDate + "\""
				+ " SIZE=28 MAXLENGTH=" 
				+ "10" 
				+ " STYLE=\"width: 1.5in; height: 0.25in\">"
				+ SMUtilities.getDatePickerString("EndingDate", getServletContext())				+ "&nbsp;(mm/dd/yyyy)</TD></TR>"
				);
	    
	    //order by
	    out.println("<TR>" +
	    				"<TD ALIGN=LEFT><B>Sort By: </B></TD>" +
	    				"<TD ALIGN=LEFT><INPUT TYPE=\"RADIO\" NAME=\"OrderBy\" VALUE=\"" + SMTableartransactions.datdocdate + "\" CHECKED=\"checked\">Doc Date<BR>" +
	    							   "<INPUT TYPE=\"RADIO\" NAME=\"OrderBy\" VALUE=\"" + SMTableartransactions.idoctype + "\">Doc Type<BR>" +
									   "<INPUT TYPE=\"RADIO\" NAME=\"OrderBy\" VALUE=\"" + SMTableartransactions.sordernumber + "\">Order Number</TD>" +
					"</TR>");
	    //document types to show
	    out.println("<TR><TD ALIGN=LEFT><B>Document Types: </B><BR>");
	    if (!bSelectAllTypes){
			out.println("<P><INPUT TYPE=SUBMIT NAME='SubmitSelectAll' VALUE='Select All Types' STYLE='font-size: 8pt;width: 1.00in; height: 0.20in'></P>");
	    }else{
	    	out.println("<P><INPUT TYPE=SUBMIT NAME='SubmitClearAll' VALUE='Clear All Types' STYLE='font-size: 8pt;width: 1.00in; height: 0.20in'></P>");
	    }
	    out.println("</TD><TD ALIGN=LEFT>");
	    ArrayList<String> alDocTypes = new ArrayList<String>(0);
	    for (int i=0;i<=10;i++){
	    	if (bSelectAllTypes){
	    		alDocTypes.add("<INPUT TYPE=CHECKBOX NAME=\"" + ARDocumentTypes.Get_Document_Type_Label(i) + "\" VALUE=1 CHECKED>" + ARDocumentTypes.Get_Document_Type_Label(i) + "<BR>");
	    	}else{
	    		alDocTypes.add("<INPUT TYPE=CHECKBOX NAME=\"" + ARDocumentTypes.Get_Document_Type_Label(i) + "\" VALUE=1>" + ARDocumentTypes.Get_Document_Type_Label(i) + "<BR>");
	    	}
	    }
	    out.println(ARUtilities.Build_HTML_Table(6, alDocTypes, 0, false));
	    out.println("</TD></TR>");
	    out.println("</TABLE>");
	    out.println("<P><INPUT TYPE=SUBMIT NAME='Submit' VALUE='Inquire' STYLE='width: 2.00in; height: 0.24in'></P>");
		out.println("</FORM>");
		out.println("</BODY></HTML>");
	}
	
	public void doGet(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
		
		doPost(request, response);
	}
}