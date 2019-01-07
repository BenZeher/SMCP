package smap;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import ServletUtilities.clsManageRequestParameters;
import smcontrolpanel.SMUtilities;

public class APYearEndProcessingSelection  extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static String sCompanyName = "";
	private static String sDBID = "";
	public void doPost(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
	
		PrintWriter out = response.getWriter();
//		if(!SMAuthenticate.authenticateCredentials(
//				request, 
//				response, 
//				getServletContext(), 
//				SMSystemFunctions.APYearEndProcessing))
//			{
//				return;
//			}

	    //Get the session info:
	    HttpSession CurrentSession = request.getSession(true);
	    sCompanyName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_COMPANYNAME);
	    sDBID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_DATABASE_ID);
	    String title = "Year End Processing";
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
	    //Print a link to main menu:
	    out.println("<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smap.APMainMenu?" 
				+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
				+ "\">Return to Accounts Payable Main Menu</A><BR>");
	    //out.println("<A HREF=\"" + WebContextParameters.getdocumentationpageURL(getServletContext()) + "#" + Long.toString(SMSystemFunctions.APYearEndProcessing) 
	    //		+ "\">Summary</A><BR><BR>");
	    
	    out.println("NOTE: Proceeding with year end processing will update all the yearly statistics in the vendor "
	    		+ "statistics database - this cannot be reversed once it is started."
	    		+ "  After year end processing, NO transactions will update the processed year's statistics, "
	    		+ "regardless of the date of the transaction.  This cannot proceed until all batches are posted.<BR><BR>"
	    	);
	    out.println ("<FORM ACTION =\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smar.APYearEndProcessingAction\">");
	    out.println("<INPUT TYPE=HIDDEN NAME='" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "' VALUE='" + sDBID + "'>");
	    out.println("<INPUT TYPE=HIDDEN NAME=CallingClass VALUE=\"" + SMUtilities.getFullClassName(this.toString()) + "\">");
	    
		out.println("<INPUT TYPE=\"SUBMIT\" VALUE=\"Process\">");
		out.println("  Check to confirm processing: <INPUT TYPE=CHECKBOX NAME=\"ConfirmProcess\">");
		out.println("</FORM>");
	   
		out.println("</BODY></HTML>");
	}
	
	public void doGet(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
		
		doPost(request, response);
	}
}
