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
import SMDataDefinition.SMTableglaccounts;
import SMDataDefinition.SMTableglfiscalsets;
import ServletUtilities.clsDatabaseFunctions;
import smcontrolpanel.SMAuthenticate;
import smcontrolpanel.SMSystemFunctions;
import smcontrolpanel.SMUtilities;

public class GLFinancialDataCheckSelect extends HttpServlet {
	
	private static final long serialVersionUID = 1L;
	
	//public static final String CONFIRM_PROCESS = "ConfirmProcess";
	public static String PARAM_VALUE_DELIMITER = " - ";
	public static String PARAM_FISCAL_YEAR_SELECTION = "FISCALYEARSELECTION";
	public static String PARAM_GL_ACCOUNTS = "GLACCOUNTS";
	public static String PARAM_UPDATE_FINANCIAL_DATA = "UPDATEFINANCIALRECORDS";
	public static String PARAM_UPDATE_FISCALSET_DATA = "UPDATEFISCALSETRECORDS";
	public static String PARAM_INSERTMISSINGFISCALANDFINANCIAL_DATA = "INSERTMISSINGFISCALSETRECORDS";
	public static String PARAM_CHECK_AGAINST_ACCPAC = "CHECKAGAINSTACCPAC";
	public static String GL_SELECT_ALL_VALUE = "";
	public static String GL_SELECT_ALL_LABEL = "** Check ALL GL Accounts **";
	public static final String SESSION_WARNING_OBJECT = "GLCHECKFINANCIALWARNING";
	public static final String SESSION_RESULTS_OBJECT = "GLCHECKFINANCIALRESULTS";
	
	public static final String RADIO_OPTIONS_GROUP = "PROCESSOPTIONSGROUP";
	public static final String CHECK_TRANSACTIONLINES_AGAINST_FISCAL_SETS = "TRANSACTIONSAGAINSTFISCALSETS";
	public static final String CHECK_FISCALSETS_AGAINST_FINANCIALSTATEMENTDATA = "FISCALSETSAGAINSTFINANCIALDATA";
	public static final String CHECK_SMCPFISCALSETS_AGAINST_ACCPACFISCALSETS = "ACCPACFISCALSETSAGAINSTSMCPFISCALSETS";
	public static final String CHECK_SMCPTRANSACTIONS_AGAINST_ACCPACTRANSACTIONS = "CHECKTRANSACTIONSAGAINSTACCPAC";
	
	public void doGet(HttpServletRequest request,
				HttpServletResponse response)
				throws ServletException, IOException {
	    
	    response.setContentType("text/html");
		PrintWriter out = response.getWriter();
		if (!SMAuthenticate.authenticateSMCPCredentials(
				request, 
				response, 
				getServletContext(), 
				SMSystemFunctions.GLCheckFinancialData))
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
	    
		//out.println("<BR><B><div id=\"" + PROCESS_STATUS_LABEL + "\">" + "\n");
		//if (sStatus.compareToIgnoreCase("") != 0){
		//	out.println("" + sStatus + "");
		//}		
		//out.println("</div></B>" + "\n");
		
		String sMessage = request.getParameter("MESSAGE");
		if (sMessage != null){
			out.println("<BR> **** MESSAGE: " + sMessage + " ****");
		}
		
	    String sResults = (String)CurrentSession.getAttribute(SESSION_RESULTS_OBJECT);
	    CurrentSession.removeAttribute(SESSION_RESULTS_OBJECT);
		if (sResults != null){
			out.println("<B>RESULTS:</B><BR>" + sResults + "<BR>");
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
	    
	    out.println("<BR>This function will check all the financial statement data, starting at the selected fiscal"
	    		+ " year, against the stored fiscal set data."
	    		+ "<BR><BR>"
	    );
	    
    	out.println ("<FORM ACTION =\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smgl.GLFinancialDataCheckAction\">");
    	out.println("<INPUT TYPE=HIDDEN NAME='" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "' VALUE='" + sDBID + "'>\n");
    	out.println("<INPUT TYPE=HIDDEN NAME='CallingClass' VALUE='" + this.getClass().getName() + "'>\n");

    	out.println("<B>Select a GL account to check, or choose to '" + GL_SELECT_ALL_LABEL + "': <B>");
    	
    	ArrayList<String> alValues = new ArrayList<String>(0);
    	ArrayList<String> alOptions = new ArrayList<String>(0);
		//Account number range:
		alValues.clear();
		alOptions.clear();
		String sSQL = "SELECT "
			+ SMTableglaccounts.sAcctID
			+ ", " + SMTableglaccounts.sDesc
			+ " FROM " + SMTableglaccounts.TableName
			+ " ORDER BY " + SMTableglaccounts.sAcctID
		;
		try {
			ResultSet rsGLAccounts = clsDatabaseFunctions.openResultSet(
				sSQL, 
				getServletContext(), 
				sDBID,
				"MySQL",
				this.toString() + ".getting account groups - User: " + sUserID
				+ " - "
				+ sUserFullName
			);
			while(rsGLAccounts.next()){
				alValues.add(rsGLAccounts.getString(SMTableglaccounts.sAcctID));
				alOptions.add(rsGLAccounts.getString(SMTableglaccounts.sAcctID) + PARAM_VALUE_DELIMITER + rsGLAccounts.getString(SMTableglaccounts.sDesc));
			}
			rsGLAccounts.close();
		} catch (Exception e1) {
			out.println("<BR><FONT COLOR=RED><B>Error [1571228837] getting GL accounts - " + e1.getMessage() + "</B></FONT><BR>");
		}
		out.println("<SELECT NAME=\"" + PARAM_GL_ACCOUNTS + "\">");
		out.println("<OPTION VALUE=\"" + GL_SELECT_ALL_VALUE + "\"> " + GL_SELECT_ALL_LABEL);
		for (int i=0;i<alValues.size();i++){
			out.println("<OPTION VALUE=\"" + alValues.get(i) + "\"> " + alOptions.get(i));
		}
		out.println("</SELECT>");
		out.println("<BR>");
		
		//Get a drop down of the available fiscal years:
		alValues.clear();
		sSQL = "SELECT"
			+ " DISTINCT " + SMTableglfiscalsets.ifiscalyear + " FROM " + SMTableglfiscalsets.TableName
			+ " ORDER BY " + SMTableglfiscalsets.ifiscalyear
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
				alValues.add(rsFiscalYears.getString(SMTableglfiscalsets.ifiscalyear));
			}
			rsFiscalYears.close();
		} catch (Exception e1) {
			out.println("<BR><FONT COLOR=RED><B>Error [1571228192] getting fiscal year selections - " + e1.getMessage() + "</B></FONT><BR>");
		}
		out.println("Start the check beginning with this fiscal year:&nbsp;");
		
		out.println("<SELECT NAME=\"" + PARAM_FISCAL_YEAR_SELECTION + "\"" 
			+ " ID = \"" + 	PARAM_FISCAL_YEAR_SELECTION + "\""
			+ "\">");
		for (int i=0;i<alValues.size();i++){
			out.println("<OPTION VALUE=\"" + alValues.get(i) + "\"> " + alValues.get(i));
		}
		out.println("</SELECT>");
		
		//Radio buttons start here:
		out.println("<BR><BR>" + "\n");
		
		out.println("<I>To only run a check of the data, choose one of the following:</I>" + "\n");
		out.println("<TABLE BORDER = 1>" + "\n");
		
		out.println("  <TR>" + "\n");
		out.println("    <TD>"
			+ "<LABEL>"
			+ "<INPUT TYPE=\"RADIO\" NAME=\"" + RADIO_OPTIONS_GROUP 
			+ "\" VALUE='" + CHECK_TRANSACTIONLINES_AGAINST_FISCAL_SETS + "'  CHECKED >"
			+ "&nbsp;" + "Check that the fiscal set data matches the totals for the actual transactions"
			+ "</LABEL>"
			+ "</TD>" + "\n"
		);
		out.println("  </TR>" + "\n");
		
		out.println("  <TR>" + "\n");
		out.println("    <TD>"
			+ "<LABEL>"
			+ "<INPUT TYPE=\"RADIO\" NAME=\"" + RADIO_OPTIONS_GROUP 
			+ "\" VALUE='" + CHECK_FISCALSETS_AGAINST_FINANCIALSTATEMENTDATA + "'>"
			+ "&nbsp;" + "Check that the financial statement data matches the fiscal sets"
			+ "</LABEL>"
			+ "</TD>" + "\n"
		);
		out.println("  </TR>" + "\n");
		
		out.println("  <TR>" + "\n");
		out.println("    <TD>"
			+ "<LABEL>"
			+ "<INPUT TYPE=\"RADIO\" NAME=\"" + RADIO_OPTIONS_GROUP 
			+ "\" VALUE='" + CHECK_SMCPFISCALSETS_AGAINST_ACCPACFISCALSETS + "'>"
			+ "&nbsp;" + "Check that the SMCP fiscal set data matches the ACCPAC fiscal set data"
			+ "</LABEL>"
			+ "</TD>" + "\n"
		);
		out.println("  </TR>" + "\n");
		
		out.println("  <TR>" + "\n");
		out.println("    <TD>"
			+ "<LABEL>"
			+ "<INPUT TYPE=\"RADIO\" NAME=\"" + RADIO_OPTIONS_GROUP 
			+ "\" VALUE='" + CHECK_SMCPTRANSACTIONS_AGAINST_ACCPACTRANSACTIONS + "'>"
			+ "&nbsp;" + "Check that the SMCP transaction lines match the ACCPAC transaction lines"
			+ "</LABEL>"
			+ "</TD>" + "\n"
		);
		out.println("  </TR>" + "\n");
		
		out.println("</TABLE>" + "\n");
		
		//Provide options to UPDATE the GL data:
		out.println("<BR><BR><I>To actually correct and UPDATE the data, choose one of the following:</I>" + "\n");
		out.println("<TABLE BORDER = 1>" + "\n");
		
		out.println("  <TR>" + "\n");
		out.println("    <TD>"
			+ "<LABEL>"
			+ "<INPUT TYPE=\"RADIO\" NAME=\"" + RADIO_OPTIONS_GROUP 
			+ "\" VALUE='" + PARAM_INSERTMISSINGFISCALANDFINANCIAL_DATA + "'>"
			+ "&nbsp;" + "Insert any missing fiscal sets and financial statement data with default (zero) values"
			+ "</LABEL>"
			+ "</TD>" + "\n"
		);
		out.println("  </TR>" + "\n");
		
		out.println("  <TR>" + "\n");
		out.println("    <TD>"
			+ "<LABEL>"
			+ "<INPUT TYPE=\"RADIO\" NAME=\"" + RADIO_OPTIONS_GROUP 
			+ "\" VALUE='" + PARAM_UPDATE_FISCALSET_DATA + "'>"
			+ "&nbsp;" + "Update the fiscal set data to match the totals for the actual transactions"
			+ "</LABEL>"
			+ "</TD>" + "\n"
		);
		out.println("  </TR>" + "\n");
		
		out.println("  <TR>" + "\n");
		out.println("    <TD>"
			+ "<LABEL>"
			+ "<INPUT TYPE=\"RADIO\" NAME=\"" + RADIO_OPTIONS_GROUP 
			+ "\" VALUE='" + PARAM_UPDATE_FINANCIAL_DATA + "'>"
			+ "&nbsp;" + "Update the financial statement data to match the fiscal sets"
			+ "</LABEL>"
			+ "</TD>" + "\n"
		);
		out.println("  </TR>" + "\n");
		
		out.println("</TABLE>" + "\n");
		
		//out.println("<BR>Check to UPDATE all the financial statement records: <INPUT TYPE=CHECKBOX NAME=\"" + PARAM_UPDATE_FINANCIAL_DATA + "\"><BR>");
		
    	out.println ("<BR><INPUT TYPE=\"SUBMIT\" VALUE=\"----Proceed----\">");
    	//out.println("  Check to confirm process: <INPUT TYPE=CHECKBOX NAME=\"" + CONFIRM_PROCESS + "\"><BR>");
    	out.println ("</FORM>");
	    out.println("</BODY></HTML>");
	}
}
