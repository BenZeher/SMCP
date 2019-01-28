package smic;

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
import ServletUtilities.clsCreateHTMLFormFields;
import ServletUtilities.clsManageRequestParameters;

public class ICDeleteInactiveItemsSelection extends HttpServlet {
	public static final String DELETE_DATE= "DeleteDate";

	private static final long serialVersionUID = 1L;

	public void doGet(HttpServletRequest request,
				HttpServletResponse response)
				throws ServletException, IOException {
	    
	    response.setContentType("text/html");
		PrintWriter out = response.getWriter();
		
		if (!SMAuthenticate.authenticateSMCPCredentials(
				request, 
				response, 
				getServletContext(),
				SMSystemFunctions.ICDeleteInactiveItems
			)
		){
			return;
		}
		
	    //Get the session info:
	    HttpSession CurrentSession = request.getSession(true);
	    String sCompanyName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_COMPANYNAME);
	    String sDBID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_DATABASE_ID);
	    String title = "Delete Inactive Items";
	    String subtitle = "";
	    
	    out.println(SMUtilities.SMCPTitleSubBGColor(
	    		title, 
	    		subtitle, 
	    		SMUtilities.getInitBackGroundColor(getServletContext(), sDBID), 
	    		sCompanyName));
	    out.println(SMUtilities.getDatePickerIncludeString(getServletContext()));
	    String sWarning = clsManageRequestParameters.get_Request_Parameter("Warning", request);
		if (sWarning.compareToIgnoreCase("") != 0){
			out.println("<B><FONT COLOR=\"RED\">WARNING: " + sWarning + "</FONT></B><BR>");
		}
	    
		//Print a link to the first page after login:
	    out.println("<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMUserLogin?" 
				+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
				+ "\">Return to user login</A><BR>");
	    out.println("<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smic.ICMainMenu?" 
				+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
				+ "\">Return to Inventory Main Menu</A><BR>");
	    out.println("<A HREF=\"" + WebContextParameters.getdocumentationpageURL(getServletContext()) + "#" + Long.toString(SMSystemFunctions.ICDeleteInactiveItems) 
	    		+ "\">Summary</A><BR><BR>");
	    
	    out.println("This function will PERMANENTLY delete all records for any inactive items with a last"
	    		+ " transaction date before the date selected below.  You should"
	    		+ " print the list of inactive items before deleting them to keep a record of what will be"
	    		+ " deleted.  You can set items to inactive in groups by using the 'Set inactive items'"
	    		+ " function."
	    		+ "<BR><BR>"
	    		+ "Deleting an item also means that ALL of that item's master, "
	    		+ " vendor item information, cost, location, and price records will also"
	    		+ " be deleted.  This function does NOT delete item transaction history, item statistics, "
	    		+ "orders, invoices, work orders, SpeedSearch records or"
	    		+ " previous batch entries.<BR><BR>"
	    		+ "NOTE: This process can be slow if you have a lot of inactive items, because of all the"
	    		+ " validation and multiple tables involved for each deletion.<BR><BR>"
	    );
    	out.println ("<FORM ACTION =\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smic.ICDeleteInactiveItemsGenerate\">");
    	out.println("<INPUT TYPE=HIDDEN NAME='" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "' VALUE='" + sDBID + "'>");
    	out.println("<INPUT TYPE=HIDDEN NAME='CallingClass' VALUE='" + this.getClass().getName() + "'>");
out.println("<TABLE BORDER=1>");
    	
		out.println("<TR>");
		out.println("<TD>" + "<B>Delete all inactive items with a last transaction date <B><I>before</I></B> (mm/dd/yyyy):</B></TD><TD>" 
				+ clsCreateHTMLFormFields.TDTextBox(DELETE_DATE, "1/1/2000", 10, 10, "") 
				+ SMUtilities.getDatePickerString(DELETE_DATE, getServletContext())
				+ "</TD>");
		out.println("</TR>");
    	
	    out.println("</TABLE>");
    	
    	out.println ("<INPUT TYPE=\"SUBMIT\" VALUE=\"----Delete inactives----\">");
    	out.println ("</FORM>");
	    out.println("</BODY></HTML>");
	}
}
