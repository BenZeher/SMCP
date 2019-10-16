package smgl;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.ResultSet;
import java.util.ArrayList;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import ConnectionPool.WebContextParameters;
import SMDataDefinition.SMMasterStyleSheetDefinitions;
import SMDataDefinition.SMTableglexternalcompanies;
import SMDataDefinition.SMTableglfinancialstatementdata;
import SMDataDefinition.SMTableglfiscalperiods;
import ServletUtilities.clsDatabaseFunctions;
import smcontrolpanel.SMAuthenticate;
import smcontrolpanel.SMSystemFunctions;
import smcontrolpanel.SMUtilities;

public class GLFinancialDataCheckSelect extends HttpServlet {
	
	private static final long serialVersionUID = 1L;
	
	public static final String CONFIRM_PROCESS = "ConfirmProcess";
	public static final String ADD_GL_ACCOUNTS = "AddGLAccounts";
	public static final String RADIO_BUTTONS_NAME = "RadioButtonSelect";
	public static final String TABLE_ROW_EVEN_ROW_BACKGROUND_COLOR = "#FFFFFF";
	public static final String TABLE_ROW_ODD_ROW_BACKGROUND_COLOR = "#DCDCDC";
	public static String PARAM_VALUE_DELIMITER = " - ";
	public static String PARAM_FISCAL_YEAR_SELECTION = "FISCALPERIODSELECTION";
	public static String PARAM_BATCH_DATE = "BATCHDATE";
	public static final String SESSION_WARNING_OBJECT = "GLCHECKFINANCIALWARNING";
	public static final String SESSION_RESULTS_OBJECT = "GLCHECKFINANCIALRESULTS";
	
	public void doGet(HttpServletRequest request,
				HttpServletResponse response)
				throws ServletException, IOException {
	    
	    response.setContentType("text/html");
		PrintWriter out = response.getWriter();
		if (!SMAuthenticate.authenticateSMCPCredentials(
				request, 
				response, 
				getServletContext(), 
				SMSystemFunctions.GLPullExternalDataIntoConsolidation))
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
	    String title = "GL Check Financial Data";
	    String subtitle = "";
	    
	    out.println(SMUtilities.SMCPTitleSubBGColor(title, subtitle, SMUtilities.getInitBackGroundColor(getServletContext(), sDBID), sCompanyName));
		
	    out.println(SMUtilities.getMasterStyleSheetLink());
	    out.println(SMUtilities.getDatePickerIncludeString(getServletContext()));
	    
	    String sWarning = (String)CurrentSession.getAttribute(SESSION_WARNING_OBJECT);
	    CurrentSession.removeAttribute(SESSION_WARNING_OBJECT);
		if (sWarning != null){
			out.println("<B><FONT COLOR=\"RED\">WARNING: " + sWarning + "</FONT></B><BR>");
		}
	    
	    String sResults = (String)CurrentSession.getAttribute(SESSION_RESULTS_OBJECT);
	    CurrentSession.removeAttribute(SESSION_RESULTS_OBJECT);
		if (sWarning != null){
			out.println("<B>RESULTS:<BR>" + sResults + "</B><BR>");
		}
		
		//Print a link to the first page after login:
	    out.println("<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMUserLogin?" 
				+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
				+ "\">Return to user login</A><BR>");
	    out.println("<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smgl.GLMainMenu?" 
				+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
				+ "\">Return to General Ledger Main Menu</A><BR>");
	    out.println("<A HREF=\"" + WebContextParameters.getdocumentationpageURL(getServletContext()) 
	    	+ "#" + Long.toString(SMSystemFunctions.GLCheckFinancialData) 
	    	+ "\">Summary</A><BR>");
	    
	    out.println("<BR>This function will check all the financial statement data starting at the selected fiscal"
	    		+ " against the stored fiscal set data."
	    		+ "<BR><BR>"
	    );
	    
    	out.println ("<FORM ACTION =\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smgl.GLFinancialDataCheckAction\">");
    	out.println("<INPUT TYPE=HIDDEN NAME='" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "' VALUE='" + sDBID + "'>\n");
    	out.println("<INPUT TYPE=HIDDEN NAME='CallingClass' VALUE='" + this.getClass().getName() + "'>\n");

    	out.println("<U><B>Select a GL account to check, or choose to 'Check ALL accounts':<B></U><BR>");
    	
    	
    	
		//Get a drop down of the available fiscal years:
    	ArrayList<String> alValues = new ArrayList<String>(0);
		alValues.clear();

		String sSQL = "SELECT"
			+ " " + SMTableglfiscalperiods.ifiscalyear + " FROM " + SMTableglfiscalperiods.TableName
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
				alValues.add(rsFiscalYears.getString(SMTableglfiscalperiods.ifiscalyear));
			}
			rsFiscalYears.close();
		} catch (Exception e1) {
			out.println("<BR><FONT COLOR=RED><B>Error [1571228192] getting fiscal year selections - " + e1.getMessage() + "</B></FONT><BR>");
		}
		out.println("<BR>Start the check beginning with this fiscal year:&nbsp;");
		
		out.println("<SELECT NAME=\"" + PARAM_FISCAL_YEAR_SELECTION + "\"" 
			+ " ID = \"" + 	PARAM_FISCAL_YEAR_SELECTION + "\""
			+ "\">");
		for (int i=0;i<alValues.size();i++){
			out.println("<OPTION VALUE=\"" + alValues.get(i) + "\"> " + alValues.get(i));
		}
		out.println("</SELECT>");
    	
    	out.println ("<BR><INPUT TYPE=\"SUBMIT\" VALUE=\"----Pull transactions----\">");
    	out.println("  Check to confirm process: <INPUT TYPE=CHECKBOX NAME=\"" + CONFIRM_PROCESS + "\"><BR>");
    	out.println ("</FORM>");
	    out.println("</BODY></HTML>");
	}

}
