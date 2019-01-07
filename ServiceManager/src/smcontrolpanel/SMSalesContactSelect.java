package smcontrolpanel;

import java.io.*;

//import java.util.Date;
import javax.servlet.*;
import javax.servlet.http.*;

import SMDataDefinition.*;
import ServletUtilities.clsServletUtilities;
import ServletUtilities.clsCreateHTMLFormFields;
import ServletUtilities.clsManageRequestParameters;


public class SMSalesContactSelect extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private String sCompanyName = "";
	private String sDBID = "";
	public void doGet(HttpServletRequest request,
				HttpServletResponse response)
				throws ServletException, IOException {
	    
	    response.setContentType("text/html");
		PrintWriter out = response.getWriter();
		if (!SMAuthenticate.authenticateSMCPCredentials(
				request, 
				response, 
				getServletContext(), 
				SMSystemFunctions.SMEditSalesContacts))
		{
			return;
		}

	    //Get the session info:
	    HttpSession CurrentSession = request.getSession(true);
	    sCompanyName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_COMPANYNAME);
	    sDBID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_DATABASE_ID);
	    String title = "Manage Sales Contacts";
	    String subtitle = "";
	    
	    out.println(SMUtilities.SMCPTitleSubBGColor(title, subtitle, SMUtilities.getInitBackGroundColor(getServletContext(), sDBID), sCompanyName));

    	String sCurrentURL;
    	sCurrentURL = clsServletUtilities.URLEncode(request.getRequestURI().toString() + "?" + request.getQueryString());
		//Print a link to the first page after login:
	    out.println("<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMUserLogin?" 
				+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
				+ "\">Return to user login</A><BR><BR>");

		//If there is a warning from trying to input previously, print it here:
		String sWarning = clsManageRequestParameters.get_Request_Parameter("Warning", request);
		if (! sWarning.equalsIgnoreCase("")){
			out.println("<B><FONT COLOR=\"RED\">WARNING: " + sWarning + "</FONT></B><BR>");
		}
		
	    //print a link for creating a new sales contact
	    out.println("<TABLE BORDER=10 CELLPADDING=10><TR><TD><A HREF=\"" 
	    	+ SMUtilities.getURLLinkBase(getServletContext()) 
	    	+ "smcontrolpanel.SMSalesContactEdit?id=-1&OriginalURL=" 
	    	+ sCurrentURL + "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
	    	+ "\"><B>Create New Sales Contact</B></A></TD></TR></TABLE><BR><HR>");
	    
	    //Input box for looking up existing sales leads.
	    out.println("<FORM ACTION =\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMSalesContactEdit\">" +
		    			"<TABLE BORDER=2 CELLPADDING=10>" +
		    			"<TR>" +
			    			"<TD ALIGN=CENTER><B>Retrieve existing sales contact by id:</B></TD>" +
			    			"<TD>" + clsCreateHTMLFormFields.TDTextBox("id", clsManageRequestParameters.get_Request_Parameter("SalesContactID", request), 20, 20, "") + "&nbsp;" + 
	    					//Link to finder:
	    				    "<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smar.ObjectFinder" +
		    				"?ObjectName=SalesContact" +
		    				"&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID +
		    				"&ResultClass=FinderResults" +
		    				"&SearchingClass=" + "smcontrolpanel.SMSalesContactSelect" +
		    				"&ReturnField=" + "SalesContactID" +
		    				"&SearchField1=" + SMTablesalescontacts.scustomername +
		    				"&SearchFieldAlias1=Customer%20Name" +
		    				"&SearchField2=" + SMTablesalescontacts.scontactname +
		    				"&SearchFieldAlias2=Contact%20Name" +
		    				"&ResultListField1="  + SMTablesalescontacts.id +
		    				"&ResultHeading1=ID" +
		    				"&ResultListField2="  + SMTablesalescontacts.scustomername +
		    				"&ResultHeading2=Customer%20Name" +
		    				"&ResultListField3="  + SMTablesalescontacts.scontactname +
		    				"&ResultHeading3=Contact%20Name" +
		    				"&ResultListField4="  + SMTablesalescontacts.sphonenumber +
		    				"&ResultHeading4=Phone%20Number" +
		    				"&ParameterString=*" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID +
		    				"\"> Find sales contact</A>" +
			    			"</TD>" +
		    			"</TR>" +
		    			"</TABLE>" +
		    		"<INPUT TYPE=\"SUBMIT\" VALUE=\"----Retrieve----\">" + 
		    		//TJR - 1/27/2011 - this line caused the 'SMSalesContactEdit' form not to load in IE:
		    		//"<INPUT TYPE=HIDDEN NAME=\"OriginalURL\" VALUE=\"" + SMUtilities.URLDecode(sCurrentURL) + "\"" + 
		    		"<INPUT TYPE=HIDDEN NAME='" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "' VALUE='" + sDBID + "'>" + 
	    			"</FORM>");
	    out.println("</BODY></HTML>");
	}
}
