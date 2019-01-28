package smic;

import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;

import ServletUtilities.clsManageRequestParameters;
import smcontrolpanel.SMAuthenticate;
import smcontrolpanel.SMUtilities;

public class ICConvertFromACCPACSelect extends HttpServlet {
	private static final long serialVersionUID = 1L;

	public void doGet(HttpServletRequest request,
				HttpServletResponse response)
				throws ServletException, IOException {
	    
		if(!SMAuthenticate.authenticateSMCPCredentials(
				request,	
				response,
				getServletContext(),
				-1L
			)
		){
			return;
		}
		
	    response.setContentType("text/html");
		PrintWriter out = response.getWriter();
		
	    //Get the session info:
	    HttpSession CurrentSession = request.getSession(true);
	    String sCompanyName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_COMPANYNAME);
	    String sDBID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_DATABASE_ID);
	    String title = "IC Convert Data From ACCPAC";
	    String subtitle = "";
	    
	    out.println(SMUtilities.SMCPTitleSubBGColor(title, subtitle, SMUtilities.getInitBackGroundColor(getServletContext(), sDBID), sCompanyName));
		
	    String sWarning = clsManageRequestParameters.get_Request_Parameter("Warning", request);
		if (! sWarning.equalsIgnoreCase("")){
			out.println("<B><FONT COLOR=\"RED\">WARNING: " + sWarning + "</FONT></B><BR>");
		}
	    String sStatus = clsManageRequestParameters.get_Request_Parameter("Status", request);
		if (! sStatus.equalsIgnoreCase("")){
			out.println("<B>STATUS: " + sStatus + "</B><BR>");
		}
	    
		//Print a link to the first page after login:
	    out.println("<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMUserLogin?" 
				+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
				+ "\">Return to user login</A><BR>");
	    out.println("<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smic.ICMainMenu?" 
				+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
				+ "\">Return to Inventory Main Menu</A><BR>");
	    
	    out.println("<BR>This function will delete ALL of the Service Manager IC data "
	    		+ "and create new IC tables, then read the company's ACCPAC data and "
	    		+ "convert it into a new set of Service Manager data."
	    		+ "<BR><BR>"
	    );
	    
    	out.println ("<FORM ACTION =\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smic.ICConvertFromACCPACAction\">");
    	out.println("<INPUT TYPE=HIDDEN NAME='" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "' VALUE='" + sDBID + "'>");
    	out.println("<INPUT TYPE=HIDDEN NAME='CallingClass' VALUE='" + this.getClass().getName() + "'>");
    	out.println ("<BR><INPUT TYPE=\"SUBMIT\" NAME=\"CONVERT_IC\" VALUE=\"Convert IC\">");
    	out.println("  Check to confirm IC conversion: <INPUT TYPE=CHECKBOX NAME=\"ConfirmICConversion\"><BR>");
    	out.println ("<BR><INPUT TYPE=\"SUBMIT\"  NAME=\"CONVERT_POHEADER\" VALUE=\"Convert POHead\">");
    	out.println("  Check to confirm PO Header conversion: <INPUT TYPE=CHECKBOX NAME=\"ConfirmPOHeadConversion\"><BR>");
    	out.println ("<BR><INPUT TYPE=\"SUBMIT\"  NAME=\"CONVERT_POLINE\" VALUE=\"Convert POLine\">");
    	out.println("  Check to confirm PO Line conversion: <INPUT TYPE=CHECKBOX NAME=\"ConfirmPOLineConversion\"><BR>");
    	out.println ("<BR><INPUT TYPE=\"SUBMIT\"  NAME=\"CONVERT_PORECEIPTS\" VALUE=\"Convert POReceipts\">");
    	out.println("  Check to confirm PO receipts conversion: <INPUT TYPE=CHECKBOX NAME=\"ConfirmPOReceiptsConversion\"><BR>");
    	out.println ("<BR><INPUT TYPE=\"SUBMIT\"  NAME=\"CONVERT_ALL\" VALUE=\"Convert all in one step\">");
    	out.println("  Check to confirm one step conversion: <INPUT TYPE=CHECKBOX NAME=\"ConfirmConvertAll\"><BR>");
    	out.println ("</FORM>");
	    out.println("</BODY></HTML>");
	}
}
