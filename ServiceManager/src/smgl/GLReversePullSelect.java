package smgl;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.ResultSet;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import ConnectionPool.WebContextParameters;
import SMDataDefinition.SMMasterStyleSheetDefinitions;
import SMDataDefinition.SMTableglexternalcompanypulls;
import smcontrolpanel.SMAuthenticate;
import smcontrolpanel.SMSystemFunctions;
import smcontrolpanel.SMUtilities;

public class GLReversePullSelect extends HttpServlet {
	
	private static final long serialVersionUID = 1L;
	
	public static final String CONFIRM_PROCESS = "ConfirmProcess";
	public static final String RADIO_BUTTONS_NAME = "RadioButtonSelect";
	public static final String TABLE_ROW_ODD_ROW_BACKGROUND_COLOR = "#DCDCDC";
	public static String PARAM_VALUE_DELIMITER = " - ";
	public static final String SESSION_WARNING_OBJECT = "REVERSEEXTERNALCOMPANIESWARNING";
	
	public void doGet(HttpServletRequest request,
				HttpServletResponse response)
				throws ServletException, IOException {
	    
	    response.setContentType("text/html");
		PrintWriter out = response.getWriter();
		if (!SMAuthenticate.authenticateSMCPCredentials(
				request, 
				response, 
				getServletContext(), 
				SMSystemFunctions.GLReverseExternalCompanyPulls))
		{
			return;
		}

	    //Get the session info:
	    HttpSession CurrentSession = request.getSession(true);
	    String sDBID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_DATABASE_ID);
	    String sUserFullName = (String)CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERFIRSTNAME) + " "
	    				+ (String)CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERLASTNAME);
	    String sCompanyName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_COMPANYNAME);
	    String title = "GL Reverse Previous Pull From External Company";
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
	    	+ "#" + Long.toString(SMSystemFunctions.GLReverseExternalCompanyPulls) 
	    	+ "\">Summary</A><BR>");
	    
	    out.println("<BR>Select a previous 'pull' from the list below to reverse it.  If it has already been reversed, it won't"
	    	+ " appear in the list, and is not eligible to be reversed."
	    	+ "<BR><BR>"
	    );
	    
    	out.println ("<FORM ACTION =\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smgl.GLReversePullAction\">");
    	out.println("<INPUT TYPE=HIDDEN NAME='" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "' VALUE='" + sDBID + "'>\n");
    	out.println("<INPUT TYPE=HIDDEN NAME='CallingClass' VALUE='" + this.getClass().getName() + "'>\n");
    	
    	try {
			out.println(buildEligiblePullList(sDBID, sUserFullName));
		} catch (Exception e) {
			out.println("<BR><B><FONT COLOR=RED>" + e.getMessage() + "</FONT></B><BR>");
		}
    	
    	//*************************************

    	out.println ("<BR><INPUT TYPE=\"SUBMIT\" VALUE=\"----Reverse selected pull----\">");
    	out.println("  Check to confirm process: <INPUT TYPE=CHECKBOX NAME=\"" + CONFIRM_PROCESS + "\"><BR>");
    	out.println ("</FORM>");
	    out.println("</BODY></HTML>");
	}
	private String buildEligiblePullList(String sDBID, String sUserFullName) throws Exception{
		String s = "";
		s += "<TABLE class = \"" + SMMasterStyleSheetDefinitions.TABLE_BASIC_WITH_BORDER + "\" >\n";
		
		//Header row:
			s += "  <TR class = \"" + SMMasterStyleSheetDefinitions.TABLE_ROW_BACKGROUNDCOLOR_LIGHTBLUE + " \" >\n";
			
			s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_RIGHT_JUSTIFIED + " \" >"
					+ "Reverse?</TD>\n";
			
			s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_RIGHT_JUSTIFIED + " \" >"
				+ "ID#</TD>\n";
			
			s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_LEFT_JUSTIFIED + " \" >"
					+ "Time</TD>\n";
			
			s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_LEFT_JUSTIFIED + " \" >"
					+ "Company name</TD>\n";

			s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_LEFT_JUSTIFIED + " \" >"
					+ "Pulled By</TD>\n";
			
			s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_RIGHT_JUSTIFIED + " \" >"
					+ "Fiscal Year</TD>\n";

			s += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_HEADING_RIGHT_JUSTIFIED + " \" >"
					+ "Fiscal Period</TD>\n";
			
		s += "  </TR>\n";
		
		String sBackgroundColor = "";
		boolean bOddRow = true;
		
		String SQL = "SELECT * FROM " + SMTableglexternalcompanypulls.TableName
			+ " WHERE ("
				+ "(" + SMTableglexternalcompanypulls.ireversed + " = 0)"
				+ " AND (" + SMTableglexternalcompanypulls.ipulltype + " = " 
					+ Integer.toString(SMTableglexternalcompanypulls.PULL_TYPE_PULL) + ")"
			+ ")"
			+ " ORDER BY " + SMTableglexternalcompanypulls.lid 
		;
		ResultSet rs = ServletUtilities.clsDatabaseFunctions.openResultSet(
			SQL, 
			getServletContext(), 
			sDBID, 
			"MySQL", 
			this.toString() + ".buildEligiblePullList - user: " + sUserFullName
		);
		
		String sLineText = "";
		boolean bFirstRecord = true;
		while (rs.next()){
			sBackgroundColor = SMMasterStyleSheetDefinitions.BACKGROUND_WHITE;
			if (bOddRow){
				sBackgroundColor = TABLE_ROW_ODD_ROW_BACKGROUND_COLOR;
			}
			
			sLineText += "  <TR style = \"  background-color:" + sBackgroundColor + ";  \""
					+ ">\n"
				;
			
			//Select:
			String sChecked = "checked";
			if (!bFirstRecord){
				sChecked = "";
			}
			String slid = Long.toString(rs.getLong(SMTableglexternalcompanypulls.lid));
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
			
			//Time
			String sTime = ServletUtilities.clsDateAndTimeConversions.resultsetDateTimeStringToFormattedString(
				rs.getString(SMTableglexternalcompanypulls.dattimepulldate), 
				ServletUtilities.clsServletUtilities.DATETIME_FORMAT_FOR_DISPLAY, 
				ServletUtilities.clsServletUtilities.EMPTY_DATETIME_VALUE
			);
			sLineText += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL + " \" >"
					+ sTime 
					+ "</TD>\n";
			
			//company name
			sLineText += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL + " \" >"
					+ rs.getString(SMTableglexternalcompanypulls.scompanyname).trim()
					+ "</TD>\n";
			
			//Pulled by
			sLineText += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL + " \" >"
					+ rs.getString(SMTableglexternalcompanypulls.sfullusername).trim()
					+ "</TD>\n";
			
			//Fiscal year
			sLineText += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL + " \" >"
					+ Integer.toString(rs.getInt(SMTableglexternalcompanypulls.ifiscalyear)).trim()
					+ "</TD>\n";
			
			//Fiscal period
			sLineText += "    <TD class = \"" + SMMasterStyleSheetDefinitions.TABLE_CELL_LEFT_JUSTIFIED_ARIAL_SMALL + " \" >"
					+ Integer.toString(rs.getInt(SMTableglexternalcompanypulls.ifiscalperiod)).trim()
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
