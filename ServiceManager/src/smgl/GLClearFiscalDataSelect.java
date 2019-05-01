package smgl;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import ConnectionPool.WebContextParameters;
import SMDataDefinition.SMTableglfiscalsets;
import ServletUtilities.clsManageRequestParameters;
import smcontrolpanel.SMAuthenticate;
import smcontrolpanel.SMSystemFunctions;
import smcontrolpanel.SMUtilities;
public class GLClearFiscalDataSelect extends HttpServlet {
	
	public static final String CONFIRM_CLEARING_CHECKBOX = "ConfirmClearing";
	
	private static final long serialVersionUID = 1L;
	public void doPost(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
	
		PrintWriter out = response.getWriter();
	    if (!SMAuthenticate.authenticateSMCPCredentials(request, response, getServletContext(), SMSystemFunctions.GLClearFiscalData)){
	    	return;
	    }

	    //Get the session info:
	    HttpSession CurrentSession = request.getSession(true);
	    String sCompanyName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_COMPANYNAME);
	    String sDBID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_DATABASE_ID);
	    String title = "Clear GL fiscal data";
	    String subtitle = "";
	    out.println(SMUtilities.SMCPTitleSubBGColor(title, subtitle, SMUtilities.getInitBackGroundColor(getServletContext(), sDBID), sCompanyName));
	    out.println(SMUtilities.getDatePickerIncludeString(getServletContext()));
	    
	    String sWarning = clsManageRequestParameters.get_Request_Parameter("Warning", request);
		if (! sWarning.equalsIgnoreCase("")){
			out.println("<B><FONT COLOR=\"RED\">WARNING: " + sWarning + "</FONT></B><BR>");
		}
	    String sStatus = clsManageRequestParameters.get_Request_Parameter("Status", request);
		if (! sStatus.equalsIgnoreCase("")){
			out.println("<B>NOTE: " + sStatus + "</B><BR>");
		}
		
	    //Print a link to the first page after login:
	    out.println("<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMUserLogin?" 
				+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
				+ "\">Return to user login</A><BR>");
	    //Print a link to main menu:
	    out.println("<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smgl.GLMainMenu?" 
				+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
				+ "\">Return to General Ledger Main Menu</A><BR>");
	    out.println("<A HREF=\"" + WebContextParameters.getdocumentationpageURL(getServletContext()) + "#" + Long.toString(SMSystemFunctions.GLClearFiscalData) 
	    		+ "\">Summary</A><BR>");
	    
		out.println ("<FORM ACTION =\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smgl.GLClearFiscalDataAction\">");
		out.println("<INPUT TYPE=HIDDEN NAME='" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "' VALUE='" + sDBID + "'>");
		out.println("<INPUT TYPE=HIDDEN NAME=CallingClass VALUE=\"" + this.getClass().getName() + "\">");
	    out.println("<BR>NOTE: This will permanently clear all the GL fiscal data up to and including the date you select."
	    	+ "  This includes GL account balances and net change amounts, as well as financial statement data, up to and including the fiscal year you select.<BR>");

	    out.println("\n<SELECT"
	    	+ " NAME = \"" + SMTableglfiscalsets.ifiscalyear + "\""
	    	+ ">\n"
	    	+ "<OPTION VALUE=''>" + "SELECT A FISCAL YEAR" + "</OPTION>\n"	
	    );
	    String SQL = "SELECT DISTINCT"
	    	+ " " + SMTableglfiscalsets.ifiscalyear
	    	+ " FROM " + SMTableglfiscalsets.TableName
	    	+ " ORDER BY " + SMTableglfiscalsets.ifiscalyear
	    ;
	    try {
			ResultSet rs = ServletUtilities.clsDatabaseFunctions.openResultSet(SQL, getServletContext(), sDBID);
			while (rs.next()){
				out.println("<OPTION VALUE = \"" + Long.toString(rs.getLong(SMTableglfiscalsets.ifiscalyear))
					+ "\">"
					+ Long.toString(rs.getLong(SMTableglfiscalsets.ifiscalyear))
					+ "\n"
				);
			}
			rs.close();
		} catch (SQLException e) {
			out.println("<BR><B><FONT COLOR=RED>Error [1556733515] reading fiscal years - " + e.getMessage() + ".</FONT></B><BR>");
		}
	    
	    out.println("</SELECT>\n");
        
		out.println("<BR><BR>");
		
		out.println("<INPUT TYPE=\"SUBMIT\" VALUE=\"----Clear fiscal data----\">");
		out.println("  Check to confirm clearing: <INPUT TYPE=CHECKBOX NAME=\"" + CONFIRM_CLEARING_CHECKBOX + "\">");
		out.println("</FORM>");
	   
		out.println("</BODY></HTML>");
	}

	public void doGet(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
		
		doPost(request, response);
	}
}
