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

public class ARDeleteInactiveCustomersSelection extends HttpServlet {
	
	private static final long serialVersionUID = 1L;
	private String sCompanyName = "";
	private String sDBID = "";
	public void doGet(HttpServletRequest request,
				HttpServletResponse response)
				throws ServletException, IOException {
	    
	    response.setContentType("text/html");
		PrintWriter out = response.getWriter();
	    if (!SMAuthenticate.authenticateSMCPCredentials(request, response, getServletContext(), SMSystemFunctions.ARDeleteInactiveCustomers)){
	    	return;
	    }

	    //Get the session info:
	    HttpSession CurrentSession = request.getSession(true);
	    sCompanyName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_COMPANYNAME);
	    sDBID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_DATABASE_ID);
	    String title = "Delete Inactive Customers";
	    String subtitle = "";
	    
	    out.println(SMUtilities.SMCPTitleSubBGColor(title, subtitle, SMUtilities.getInitBackGroundColor(getServletContext(), sDBID), sCompanyName));
		
	    String sWarning = ARUtilities.get_Request_Parameter("Warning", request);
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
	    out.println("<A HREF=\"" + WebContextParameters.getdocumentationpageURL(getServletContext()) + "#" + Long.toString(SMSystemFunctions.ARDeleteInactiveCustomers) 
	    		+ "\">Summary</A><BR><BR>");
	    
	    out.println("This function will PERMANENTLY delete all records for any inactive customers.  You should"
	    		+ " print the list of inactive customers before deleting them to keep a record of what will be"
	    		+ " deleted.  You can set customers to inactive in groups by using the 'Set inactive customers'"
	    		+ " function."
	    		+ "<BR><BR>"
	    		+ "Deleting a customer means also that ALL of that customer's master, transaction, matching"
	    		+ " transaction, statistics, monthly statistics, ship-to, site location, call sheet, and"
	    		+ " sales contact records will also"
	    		+ " be deleted.  This function does NOT delete orders, invoices, SpeedSearch records or"
	    		+ " previous batch entries.<BR><BR>"
	    		+ "NOTE: This process can be slow if you have a lot of inactive customers, because of all the"
	    		+ " validation and multiple tables involved for each deletion.<BR><BR>"
	    );
    	out.println ("<FORM ACTION =\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smar.ARDeleteInactiveCustomersGenerate\">");
    	out.println("<INPUT TYPE=HIDDEN NAME='" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "' VALUE='" + sDBID + "'>");
    	out.println("<INPUT TYPE=HIDDEN NAME='CallingClass' VALUE='" + this.getClass().getName() + "'>");
    	out.println ("<INPUT TYPE=\"SUBMIT\" VALUE=\"----Delete inactives----\">");
    	out.println ("</FORM>");
	    out.println("</BODY></HTML>");
	}
	
}
