package smgl;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.util.ArrayList;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import ConnectionPool.WebContextParameters;
import SMDataDefinition.SMTableglfiscalperiods;
import ServletUtilities.clsDatabaseFunctions;
import smcontrolpanel.SMAuthenticate;
import smcontrolpanel.SMSystemFunctions;
import smcontrolpanel.SMUtilities;

public class GLCloseFiscalYearEdit extends HttpServlet {
	
	private static final long serialVersionUID = 1L;
	
	public static final String CONFIRM_PROCESS = "ConfirmProcess";
	public static String PARAM_FISCAL_YEAR_SELECTION = "FISCALYEARSELECTION";
	public static String PARAM_BATCH_DATE = "BATCHDATE";
	public static final String GL_CLOSING_SESSION_WARNING_OBJECT = "GLCLOSINGWARNING";
	
	public void doGet(HttpServletRequest request,
				HttpServletResponse response)
				throws ServletException, IOException {
	    
	    response.setContentType("text/html");
		PrintWriter out = response.getWriter();
		if (!SMAuthenticate.authenticateSMCPCredentials(
				request, 
				response, 
				getServletContext(), 
				SMSystemFunctions.GLCloseFiscalYear))
		{
			return;
		}

	    //Get the session info:
	    HttpSession CurrentSession = request.getSession(true);
	    String sDBID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_DATABASE_ID);
	    String sUserID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERID);
	    String sUserFullName = (String)CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERFIRSTNAME) + " "
	    				+ (String)CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERLASTNAME);
	    String sCompanyName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_COMPANYNAME);
	    String title = "GL Close Fiscal Year";
	    String subtitle = "";
	    
	    out.println(SMUtilities.SMCPTitleSubBGColor(title, subtitle, SMUtilities.getInitBackGroundColor(getServletContext(), sDBID), sCompanyName));
		
	    out.println(SMUtilities.getMasterStyleSheetLink());
	    out.println(SMUtilities.getDatePickerIncludeString(getServletContext()));
	    
	    //String sWarning = "";
	    String sWarning = (String)CurrentSession.getAttribute(GL_CLOSING_SESSION_WARNING_OBJECT);
	    CurrentSession.removeAttribute(GL_CLOSING_SESSION_WARNING_OBJECT);
		if (sWarning != null){
			out.println("<B><FONT COLOR=\"RED\">WARNING: " + sWarning + "</FONT></B><BR>");
		}
	    String sStatus = ServletUtilities.clsManageRequestParameters.get_Request_Parameter("Status", request);
		if (sStatus.compareToIgnoreCase("") != 0){
			out.println("<B>STATUS: " + sStatus + "</B><BR>");
		}
	    
		//Print a link to the first page after login:
	    out.println("<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMUserLogin?" 
				+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
				+ "\">Return to user login</A><BR>");
	    out.println("<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smgl.GLMainMenu?" 
				+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
				+ "\">Return to General Ledger Main Menu</A><BR>");
	    out.println("<A HREF=\"" + WebContextParameters.getdocumentationpageURL(getServletContext()) 
	    	+ "#" + Long.toString(SMSystemFunctions.GLCloseFiscalYear) 
	    	+ "\">Summary</A><BR>");
	    
	    Connection conn = null;
		try {
			conn = ServletUtilities.clsDatabaseFunctions.getConnectionWithException(
				getServletContext(), 
				sDBID, 
				"MySQL", 
				this.toString() + ".doGet - user: " + sUserFullName + " [1564153159]"
			);
		} catch (Exception e2) {
			out.println("<BR><B><FONT COLOR=RED>Error [1564153160] getting connection - " + e2.getMessage() + "</FONT></B><BR>");
			out.println("</BODY></HTML>");
			return;
		}
	    
	    GLOptions glopt = new GLOptions();
	    if (!glopt.load(conn)){
	    	ServletUtilities.clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1564153162]");
			out.println("<BR><B><FONT COLOR=RED>Error [1564153161] loading GL Options - " + glopt.getErrorMessageString() + "</FONT></B><BR>");
			out.println("</BODY></HTML>");
			return;
	    }
	    
	    out.println("<BR>This function will automatically create journal entries for the elected fiscal year"
	    		+ " to move current balances from all the income statement accounts into the single 'retained earnings'"
	    		+ " account (GL account " + glopt.getsClosingAccount() + ")."
	    		+ "<BR><BR>"
	    );
	    
    	out.println ("<FORM ACTION =\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smgl.GLCloseFiscalYearAction\">");
    	out.println("<INPUT TYPE=HIDDEN NAME='" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "' VALUE='" + sDBID + "'>\n");
    	out.println("<INPUT TYPE=HIDDEN NAME='CallingClass' VALUE='" + this.getClass().getName() + "'>\n");

		// Closable fiscal years:
		//Get a drop down of the available periods:
    	ArrayList<String> alValues = new ArrayList<String>(0);
		ArrayList<String> alOptions = new ArrayList<String>(0);
		alValues.clear();
		alOptions.clear();

		String sSQL = "SELECT"
			+ " " + SMTableglfiscalperiods.ifiscalyear
			+ " FROM " + SMTableglfiscalperiods.TableName
			+ " WHERE ("
				+ "(" + SMTableglfiscalperiods.ilockclosingperiod + " = 0)"
			+ ")"
			+ " ORDER BY " + SMTableglfiscalperiods.ifiscalyear
		;
		try {
			ResultSet rsFiscalYears = clsDatabaseFunctions.openResultSet(
				sSQL, 
				getServletContext(), 
				sDBID,
				"MySQL",
				this.toString() + ".getting fiscal year selections - User: " + sUserID
				+ " - "
				+ sUserFullName
			);
			while(rsFiscalYears.next()){
				alValues.add(Long.toString(rsFiscalYears.getLong(SMTableglfiscalperiods.ifiscalyear)));
				alOptions.add(Long.toString(rsFiscalYears.getLong(SMTableglfiscalperiods.ifiscalyear)));
			}
			rsFiscalYears.close();
		} catch (Exception e1) {
			out.println("<BR><FONT COLOR=RED><B>Error [1562701323] getting fiscal year selections - " + e1.getMessage() + "</B></FONT><BR>");
		}
		out.println("<BR>Close this fiscal year:&nbsp;");
		
		out.println("<SELECT NAME=\"" + PARAM_FISCAL_YEAR_SELECTION + "\"" 
			+ " ID = \"" + 	PARAM_FISCAL_YEAR_SELECTION + "\""
			+ "\">");
		for (int i=0;i<alValues.size();i++){
			out.println("<OPTION VALUE=\"" + alValues.get(i) + "\"> " + alOptions.get(i));
		}
		out.println("</SELECT>");
		
		out.println(
    		"<BR>Set&nbsp;closing&nbsp;batch&nbsp;date&nbsp;to:&nbsp;"
    		+ "<INPUT TYPE=TEXT NAME=\"" + PARAM_BATCH_DATE + "\""
    		+ " VALUE=\"" + ServletUtilities.clsDateAndTimeConversions.now(ServletUtilities.clsServletUtilities.DATE_FORMAT_FOR_DISPLAY) + "\""
    		+ " MAXLENGTH=" + "10"
    		+ " SIZE = " + "8"
    		+ ">"
    		+ "\n"
    	);
		out.println(SMUtilities.getDatePickerString(PARAM_BATCH_DATE, getServletContext()) + "\n");
    	
    	out.println ("<BR><INPUT TYPE=\"SUBMIT\" VALUE=\"----Close year----\">");
    	out.println("  Check to confirm process: <INPUT TYPE=CHECKBOX NAME=\"" + CONFIRM_PROCESS + "\"><BR>");
    	out.println ("</FORM>");
	    out.println("</BODY></HTML>");
	}

}
