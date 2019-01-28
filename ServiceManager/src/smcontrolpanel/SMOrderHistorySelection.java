package smcontrolpanel;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import ConnectionPool.WebContextParameters;
import ServletUtilities.clsCreateHTMLFormFields;
import ServletUtilities.clsManageRequestParameters;

public class SMOrderHistorySelection  extends HttpServlet {
	public static final String PRINT_BUTTON_LABEL = "Print order information";
	public static final String DOWNLOAD_TO_HTML = "DOWNLOAD_TO_HTML";
	public static final String ENDING_ORDER_DATE_PARAM = "ENDINGORDERDATE";
	public static final String STARTING_ORDER_DATE_PARAM = "STARTINGORDERDATE";
	
	private static final long serialVersionUID = 1L;
	public void doPost(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
		if (!SMAuthenticate.authenticateSMCPCredentials(
				request, 
				response, 
				getServletContext(),
				SMSystemFunctions.SMOrderHistory
			)
		){
			return;
		}
		PrintWriter out = response.getWriter();
		
	    //Get the session info:
	    HttpSession CurrentSession = request.getSession(true);
	    String sCompanyName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_COMPANYNAME);
	    String sDBID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_DATABASE_ID);
	    String title = "SM Order History";
	    String subtitle = "";
	    out.println(SMUtilities.SMCPTitleSubBGColor(title, subtitle, SMUtilities.getInitBackGroundColor(getServletContext(), sDBID), sCompanyName));
	    out.println(SMUtilities.getDatePickerIncludeString(getServletContext()));
	    String sWarning = clsManageRequestParameters.get_Request_Parameter("Warning", request);
		if (! sWarning.equalsIgnoreCase("")){
			out.println("<B><FONT COLOR=\"RED\">WARNING: " + sWarning + "</FONT></B><BR>");
		}
	    //Print a link to the first page after login:
	    out.println("<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMUserLogin?" 
				+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
				+ "\">Return to user login</A><BR>");
	    out.println("<A HREF=\"" + WebContextParameters.getdocumentationpageURL(getServletContext()) + "#" + Long.toString(SMSystemFunctions.SMOrderHistory) 
	    	+ "\">Summary</A><BR><BR>");
		out.println ("<FORM ACTION =\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMOrderHistoryAction\">");
		out.println("<INPUT TYPE=HIDDEN NAME='" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "' VALUE='" + sDBID + "'>");
		out.println("<INPUT TYPE=HIDDEN NAME=CallingClass VALUE=\"" 
			+ SMUtilities.getFullClassName(this.toString()) + "\">");

		out.println(
				"This function will print order information for ALL orders within the date range chosen.<BR>"
				+ "  This can be used to save order information on paper or in a file before using the 'SM Purge Data' "
				+ "function to purge orders.<BR>"
				+ "NOTE: This list can take a very long time if you include a date range with a lot of orders.  If you are only testing,"
				+ " you should use a date that will result in a small number of orders.<BR>"
			);
		
		out.println("<B>Include orders STARTING WITH this order date (mm/dd/yyyy):</B></TD><TD>" 
				+ clsCreateHTMLFormFields.TDTextBox(STARTING_ORDER_DATE_PARAM, "1/1/2000", 10, 10, "") 
				+ SMUtilities.getDatePickerString(STARTING_ORDER_DATE_PARAM, getServletContext())
				);
		out.println("<BR>");
		out.println("<B>Include orders ENDING WITH this order date (mm/dd/yyyy):</B></TD><TD>" 
				+ clsCreateHTMLFormFields.TDTextBox(ENDING_ORDER_DATE_PARAM, "1/1/2000", 10, 10, "") 
				+ SMUtilities.getDatePickerString(ENDING_ORDER_DATE_PARAM, getServletContext())
				);
		out.println("<BR>");
		
		boolean bDownLoadToHTML = (request.getParameter(DOWNLOAD_TO_HTML) != null);
		out.println(clsCreateHTMLFormFields.TDCheckBox(
			DOWNLOAD_TO_HTML, bDownLoadToHTML,
			"Download to HTML file (If this is checked, the report will automatically be downloaded to an HTML file - otherwise, it will print to the screen.)")
		); 
		
		out.println("<BR><BR><INPUT TYPE=\"SUBMIT\" VALUE=\"" + PRINT_BUTTON_LABEL + "\">");
		
		out.println("</FORM>");
	    	
		out.println("</BODY></HTML>");
	}
	
	public void doGet(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
		
		doPost(request, response);
	}
}
