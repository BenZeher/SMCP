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
import ServletUtilities.clsDatabaseFunctions;
import smcontrolpanel.SMAuthenticate;
import smcontrolpanel.SMSystemFunctions;
import smcontrolpanel.SMUtilities;

public class GLPullIntoConsolidationSelect extends HttpServlet {
	
	private static final long serialVersionUID = 1L;
	
	public static final String CONFIRM_PROCESS = "ConfirmProcess";
	public static final String ADD_GL_ACCOUNTS = "AddGLAccounts";
	public static final String RADIO_BUTTONS_NAME = "RadioButtonSelect";
	public static final String TABLE_ROW_EVEN_ROW_BACKGROUND_COLOR = "#FFFFFF";
	public static final String TABLE_ROW_ODD_ROW_BACKGROUND_COLOR = "#DCDCDC";
	public static String PARAM_VALUE_DELIMITER = " - ";
	public static String PARAM_FISCAL_PERIOD_SELECTION = "FISCALPERIODSELECTION";
	public static String PARAM_BATCH_DATE = "BATCHDATE";
	public static final String SESSION_WARNING_OBJECT = "PULLEXTERNALCOMPANIESWARNING";
	
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
	    String title = "GL Pull Transactions Into Consolidated Company";
	    String subtitle = "";
	    
	    out.println(SMUtilities.SMCPTitleSubBGColor(title, subtitle, SMUtilities.getInitBackGroundColor(getServletContext(), sDBID), sCompanyName));
		
	    out.println(SMUtilities.getMasterStyleSheetLink());
	    out.println(SMUtilities.getDatePickerIncludeString(getServletContext()));
	    
	    //String sWarning = "";
	    String sWarning = (String)CurrentSession.getAttribute(SESSION_WARNING_OBJECT);
	    CurrentSession.removeAttribute(SESSION_WARNING_OBJECT);
		if (sWarning != null){
			out.println("<B><FONT COLOR=\"RED\">WARNING: " + sWarning + "</FONT></B><BR>");
		}
	    
		//Print a link to the first page after login:
	    out.println("<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMUserLogin?" 
				+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
				+ "\">Return to user login</A><BR>");
	    out.println("<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smgl.GLMainMenu?" 
				+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
				+ "\">Return to General Ledger Main Menu</A><BR>");
	    out.println("<A HREF=\"" + WebContextParameters.getdocumentationpageURL(getServletContext()) 
	    	+ "#" + Long.toString(SMSystemFunctions.GLPullExternalDataIntoConsolidation) 
	    	+ "\">Summary</A><BR>");
	    
	    out.println("<BR>Pulling transactions for an external company will automatically add ALL the non-zero"
	    	+ " GL transactions from that company, for the selected year and period, into THIS (consolidated) company.<BR>"
	    	+ "  There is no batch to post or any further steps required after doing the 'pull' for a selected company.<BR>"
	    	+ "<BR>Every time an individual company is 'pulled' successfully, it gets recorded in the 'glexternalcompanypulls' table."
	    	+ "  If you try to pull the same company, fiscal year and period again, the program will check that record,"
	    	+ " notify you, and prevent you from pulling it again."  
	    	+ "<BR><BR>"
	    );
	    
    	out.println ("<FORM ACTION =\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smgl.GLPullIntoConsolidationAction\">");
    	out.println("<INPUT TYPE=HIDDEN NAME='" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "' VALUE='" + sDBID + "'>\n");
    	out.println("<INPUT TYPE=HIDDEN NAME='CallingClass' VALUE='" + this.getClass().getName() + "'>\n");
    	
    	try {
			out.println(buildExternalCompanyList(sDBID, sUserFullName));
		} catch (Exception e) {
			out.println("<BR><B><FONT COLOR=RED>" + e.getMessage() + "</FONT></B><BR>");
		}
    	
    	//*************************************


		// Balance sheet Year/period
		//Get a drop down of the available periods:
    	ArrayList<String> alValues = new ArrayList<String>(0);
		ArrayList<String> alOptions = new ArrayList<String>(0);
		alValues.clear();
		alOptions.clear();
		String sLatestUnlockedYearAndPeriod = "";
		try {
			sLatestUnlockedYearAndPeriod = GLFiscalYear.getLatestUnlockedFiscalYearAndPeriod(
					getServletContext(),
					sDBID,
					this.toString(),
					sUserID,
					sUserFullName);
		} catch (Exception e) {
			out.println("<BR><FONT COLOR=RED><B>Error [1562701222] getting latest unlocked period - " 
				+ e.getMessage() + "</B></FONT><BR>");
		}
		String sSQL = "SELECT DISTINCT"
			+ " CONCAT(CAST(" + SMTableglfinancialstatementdata.ifiscalyear + " AS CHAR), '" 
				+ PARAM_VALUE_DELIMITER 
				+ "', CAST(" + SMTableglfinancialstatementdata.ifiscalperiod + " AS CHAR)) AS FISCALSELECTION"
			+ " FROM " + SMTableglfinancialstatementdata.TableName
			+ " ORDER BY " + SMTableglfinancialstatementdata.ifiscalyear + " DESC, " + SMTableglfinancialstatementdata.ifiscalperiod + " DESC"
		;
		try {
			ResultSet rsFiscalSelections = clsDatabaseFunctions.openResultSet(
				sSQL, 
				getServletContext(), 
				sDBID,
				"MySQL",
				this.toString() + ".getting period selections - User: " + sUserID
				+ " - "
				+ sUserFullName
			);
			while(rsFiscalSelections.next()){
				alValues.add(rsFiscalSelections.getString("FISCALSELECTION"));
				alOptions.add(rsFiscalSelections.getString("FISCALSELECTION"));
			}
			rsFiscalSelections.close();
		} catch (Exception e1) {
			out.println("<BR><FONT COLOR=RED><B>Error [1562701223] getting fiscal period selections - " + e1.getMessage() + "</B></FONT><BR>");
		}
		out.println("<BR>Pull transactions into this fiscal period:&nbsp;");
		
		out.println("<SELECT NAME=\"" + PARAM_FISCAL_PERIOD_SELECTION + "\"" 
			+ " ID = \"" + 	PARAM_FISCAL_PERIOD_SELECTION + "\""
			+ "\">");
		for (int i=0;i<alValues.size();i++){
			//System.out.println("[1559924487] sLatestUnlockedYearAndPeriod = '" + sLatestUnlockedYearAndPeriod + "', alValues.get(i) = '" + alValues.get(i) + "'.");
			if (alValues.get(i).compareToIgnoreCase(sLatestUnlockedYearAndPeriod) == 0){
				out.println("<OPTION selected=yes VALUE=\"" + alValues.get(i) + "\"> " + alOptions.get(i));
			}else{
				out.println("<OPTION VALUE=\"" + alValues.get(i) + "\"> " + alOptions.get(i));
			}
		}
		out.println("</SELECT>");
    	
    	out.println ("<BR><INPUT TYPE=\"SUBMIT\" VALUE=\"----Pull transactions----\">");
    	out.println("  Check to confirm process: <INPUT TYPE=CHECKBOX NAME=\"" + CONFIRM_PROCESS + "\"><BR>");
    	out.println ("</FORM>");
	    out.println("</BODY></HTML>");
	}
	private String buildExternalCompanyList(String sDBID, String sUserFullName) throws Exception{
		String s = "";
		s += "<TABLE class = \"" + SMMasterStyleSheetDefinitions.TABLE_BASIC_WITH_BORDER + "\" >\n";
		
		//Header row:
			s += "  <TR class = \"" + SMMasterStyleSheetDefinitions.TABLE_ROW_BACKGROUNDCOLOR_LIGHTBLUE + " \" >\n";
			
			s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_RIGHT_JUSTIFIED + " \" >"
					+ "Select?</TD>\n";
			
			s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_RIGHT_JUSTIFIED + " \" >"
				+ "ID#</TD>\n";
			
			s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_LEFT_JUSTIFIED + " \" >"
					+ "Company name</TD>\n";
			
		s += "  </TR>\n";
		
		String sBackgroundColor = "";
		boolean bOddRow = true;
		
		String SQL = "SELECT * FROM " + SMTableglexternalcompanies.TableName;
		ResultSet rs = ServletUtilities.clsDatabaseFunctions.openResultSet(
			SQL, 
			getServletContext(), 
			sDBID, 
			"MySQL", 
			this.toString() + ".buildExternalCompanyList - user: " + sUserFullName
		);
		
		String sLineText = "";
		boolean bFirstRecord = true;
		while (rs.next()){
			sBackgroundColor = TABLE_ROW_EVEN_ROW_BACKGROUND_COLOR;
			if (bOddRow){
				sBackgroundColor = TABLE_ROW_ODD_ROW_BACKGROUND_COLOR;
			}
			
			String slid = Long.toString(rs.getLong(SMTableglexternalcompanies.lid));
			sLineText += "  <TR style = \"  background-color:" + sBackgroundColor + ";  \""
				+ ">\n"
			;
			
			//Select:
			String sChecked = "checked";
			if (!bFirstRecord){
				sChecked = "";
			}
			
			sLineText += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_CENTER_JUSTIFIED_ARIAL_SMALL + " \" >" 
				+ "<LABEL>&nbsp;&nbsp;&nbsp;"
				+ "<input type=\"radio\" name=\"" + RADIO_BUTTONS_NAME + "\" value=\"" 
				+ slid + "\"" + " " + sChecked + " " + ">" 
				+ "&nbsp;&nbsp;&nbsp;</LABEL>"
				+ "</TD>\n"
			;
			
			//lid
			sLineText += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL + " \" >"
					+ slid 
					+ "</TD>\n";
			
			//company name
			sLineText += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL + " \" >"
					+ rs.getString(SMTableglexternalcompanies.scompanyname).trim()
					+ "</TD>\n";
			
			sLineText += "  </TR>\n";
			bOddRow = !bOddRow;
			bFirstRecord = false;
		}
		rs.close();
		
		//Add the buffer into the main string:
		s += sLineText;
		
		s += "</TABLE>\n";
		
		return s;
	}
}
