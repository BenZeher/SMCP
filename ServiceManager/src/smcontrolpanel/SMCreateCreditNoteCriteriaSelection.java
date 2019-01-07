package smcontrolpanel;

import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import SMDataDefinition.*;
import ServletUtilities.clsServletUtilities;
import ServletUtilities.clsCreateHTMLFormFields;
import ServletUtilities.clsManageRequestParameters;

public class SMCreateCreditNoteCriteriaSelection extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static String sCompanyName = "";
	private static String sDBID = "";
	//private String sCallingClass = "smcontrolpanel.SMCreateMultipleInvoicesSelection";
	public void doGet(HttpServletRequest request,
				HttpServletResponse response)
				throws ServletException, IOException {
	    
		if (!SMAuthenticate.authenticateSMCPCredentials(
				request, 
				response, 
				getServletContext(), 
				SMSystemFunctions.SMCreateCreditNotes
			)
		){
			return;
		}
	    response.setContentType("text/html");
	    
		PrintWriter out = response.getWriter();
	    //Get the session info:
	    HttpSession CurrentSession = request.getSession(true);
	    sCompanyName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_COMPANYNAME);
	    sDBID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_DATABASE_ID);
	    String title = "Create credit note";
	    String subtitle = "";
	    
	    out.println(SMUtilities.SMCPTitleSubBGColor(title, subtitle, SMUtilities.getInitBackGroundColor(getServletContext(), sDBID), sCompanyName));

	    /*
    	//get current URL
    	String sCurrentURL;
    	sCurrentURL = SMUtilities.URLEncode(request.getRequestURI().toString() + "?" + request.getQueryString());
    	*/
	    out.println("<BR><A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMUserLogin?" 
				+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
				+ "\">Return to user login</A><BR><BR>");

	    //display message if there is any
    	String sMessage = clsManageRequestParameters.get_Request_Parameter("MESSAGE", request);
		if (!sMessage.equalsIgnoreCase("")){
			out.println("<B>Message: " + sMessage + "</B><BR>");
		}
	    //display warning if there is any.
	    String sWarning = clsServletUtilities.URLDecode(clsManageRequestParameters.get_Request_Parameter("Warning", request));
	    //System.out.println("2.WARNING = " + sWarning);
		if (!sWarning.equalsIgnoreCase("")){
			out.println("<B><FONT COLOR=\"RED\">WARNING: " + sWarning + "</FONT></B><BR>");
		}

    	out.println ("<FORM ACTION =\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMCreateCreditNotePreview\">");
    	out.println("<INPUT TYPE=HIDDEN NAME='" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "' VALUE='" + sDBID + "'>");
    	out.println("<INPUT TYPE=HIDDEN NAME='CallingClass' VALUE='smcontrolpanel.SMCreateCreditNoteCriteriaSelection'>");
    	out.println("<TABLE CELLPADDING=10 BORDER=1>");
    	
    	out.println("<TR><TD ALIGN=CENTER><H3>Invoice Number </H3></TD>");
    	out.println("<TD>" + clsCreateHTMLFormFields.TDTextBox(
							"INVOICENUMBER", 
							clsManageRequestParameters.get_Request_Parameter("INVOICENUMBER", request), 
							10, 
							10, 
							""
							) + "&nbsp;"

    					//Link to finder:
    				    + "<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smar.ObjectFinder"
	    				+ "?ObjectName=Invoice"
	    				+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
	    				+ "&ResultClass=FinderResults"
	    				+ "&SearchingClass=" + "smcontrolpanel.SMCreateCreditNoteCriteriaSelection"
	    				+ "&ReturnField=" + "INVOICENUMBER"
	    				+ "&SearchField1=" + SMTableinvoiceheaders.sBillToName
	    				+ "&SearchFieldAlias1=Bill%20To%20Name"
	    				+ "&SearchField2=" + SMTableinvoiceheaders.sShipToName
	    				+ "&SearchFieldAlias2=Ship%20To%20Name"
	    				+ "&SearchField3=" + SMTableinvoiceheaders.sBillToAddressLine1
	    				+ "&SearchFieldAlias3=Bill%20To%20Address%20Line%201"
	    				+ "&SearchField4=" + SMTableinvoiceheaders.sShipToAddress1
	    				+ "&SearchFieldAlias4=Ship%20To%20Address%20Line%201"
	    				+ "&ResultListField1="  + SMTableinvoiceheaders.sInvoiceNumber
	    				+ "&ResultHeading1=Invoice%20Number"
	    				+ "&ResultListField2="  + SMTableinvoiceheaders.sBillToName
	    				+ "&ResultHeading2=Bill%20To%20Name"
	    				+ "&ResultListField3="  + SMTableinvoiceheaders.sShipToName
	    				+ "&ResultHeading3=Ship%20To%20Name"
	    				//+ "&ResultListField4="  + SMTablearcustomer.sPhoneNumber
	    				//+ "&ResultHeading4=Phone"
	    				+ "&ParameterString=*" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
	    				+ "\"> Find invoice</A>" +
    	"</TD></TR>");
    	    	
    	out.println("</TABLE>");
    	
    	out.println ("<INPUT TYPE=\"SUBMIT\" VALUE=\"----Display Invoice Information----\">");
    	out.println ("</FORM>");
 
	    out.println("</BODY></HTML>");
	}
	
	public void doPost(HttpServletRequest request,
				HttpServletResponse response)
				throws ServletException, IOException {
		
		doGet(request, response);
	}
	
}