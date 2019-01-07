package smcontrolpanel;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import ConnectionPool.WebContextParameters;
import ServletUtilities.clsManageRequestParameters;

public class SMRecalibrateCounters extends HttpServlet {
	
	private static final long serialVersionUID = 1L;
	private String sDBID = "";
	private String sCompanyName = "";
	public void doGet(HttpServletRequest request,
				HttpServletResponse response)
				throws ServletException, IOException {
	    
	    response.setContentType("text/html");
		PrintWriter out = response.getWriter();
		
	    //Get the session info:
	    HttpSession CurrentSession = request.getSession(true);
	    sDBID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_DATABASE_ID);
	    sCompanyName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_COMPANYNAME);
		if (!SMAuthenticate.authenticateSMCPCredentials(
				request, 
				response, 
				getServletContext(), 
				SMSystemFunctions.SMRecalibrateOrderAndInvoiceCounters)){
			return;
		}
	    String title = "SM Recalibrate Order And Invoice Number Counters";
	    String subtitle = "";
	    
	    out.println(SMUtilities.SMCPTitleSubBGColor(
	    	title, 
	    	subtitle, 
	    	SMUtilities.getInitBackGroundColor(getServletContext(), sDBID),
	    	sCompanyName));
		
	    String sWarning = clsManageRequestParameters.get_Request_Parameter("Warning", request);
		if (! sWarning.equalsIgnoreCase("")){
			out.println("<B><FONT COLOR=\"RED\">WARNING: " + sWarning + "</FONT></B><BR>");
		}
	    
		//Print a link to the first page after login:
	    out.println("<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMUserLogin?" 
				+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
				+ "\">Return to user login</A><BR>");
	    out.println("<A HREF=\"" + WebContextParameters.getdocumentationpageURL(getServletContext()) + "#" + Long.toString(SMSystemFunctions.SMRecalibrateOrderAndInvoiceCounters) + "\">Summary</A><BR>");
	    
	    out.println("<BR>This function will reset the system's 'Next Order Number', and 'Next Invoice Number'"
	    		+ " based on the last invoice number and order number that it actually finds in the data.<BR><BR>"
	    );
	    
    	out.println ("<FORM ACTION =\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMRecalibrateCountersAction\">");
    	out.println("<INPUT TYPE=HIDDEN NAME='" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "' VALUE='" + sDBID + "'>");
    	out.println("<INPUT TYPE=HIDDEN NAME='CallingClass' VALUE='" + this.getClass().getName() + "'>");
    	out.println ("<BR><INPUT TYPE=\"SUBMIT\" VALUE=\"----Recalibrate counters----\">");
    	out.println("  Check to confirm recalibration: <INPUT TYPE=CHECKBOX NAME=\"Confirm\"><BR>");
    	out.println ("</FORM>");
	    out.println("</BODY></HTML>");
	}
}
