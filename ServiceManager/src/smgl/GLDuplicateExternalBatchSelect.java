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
import SMDataDefinition.SMTableglexternalcompanies;
import smcontrolpanel.SMAuthenticate;
import smcontrolpanel.SMMasterEditAction;
import smcontrolpanel.SMSystemFunctions;
import smcontrolpanel.SMUtilities;

public class GLDuplicateExternalBatchSelect extends HttpServlet {
	
	private static final long serialVersionUID = 1L;
	
	public static final String CONFIRM_PROCESS = "ConfirmProcess";
	public static final String RADIO_BUTTONS_NAME = "RadioButtonSelect";
	public static final String TABLE_ROW_ODD_ROW_BACKGROUND_COLOR = "#DCDCDC";
	public static String PARAM_BATCH_NUMBER = "BATCHNUMBER";
	public static final String SESSION_WARNING_OBJECT = "DUPLICATEEXTERNALBATCHWARNING";
	
	public void doGet(HttpServletRequest request,
				HttpServletResponse response)
				throws ServletException, IOException {
	    
	    response.setContentType("text/html");
		PrintWriter out = response.getWriter();
		if (!SMAuthenticate.authenticateSMCPCredentials(
				request, 
				response, 
				getServletContext(), 
				SMSystemFunctions.GLDuplicateExternalCompanyBatch))
		{
			return;
		}

	    //Get the session info:
	    HttpSession CurrentSession = request.getSession(true);
	    String sDBID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_DATABASE_ID);
	    String sUserFullName = (String)CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERFIRSTNAME) + " "
	    				+ (String)CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERLASTNAME);
	    String sCompanyName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_COMPANYNAME);
	    String title = "GL External Company Batches";
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
	    
		String sStatus = ServletUtilities.clsManageRequestParameters.get_Request_Parameter(SMMasterEditAction.STATUS_PARAMETER, request);
		if (sStatus.compareToIgnoreCase("") != 0){
			out.println("<B><FONT COLOR=\"GREEN\">RESULT: " + sStatus + "</FONT></B><BR>");
		}
		
		//Print a link to the first page after login:
	    out.println("<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMUserLogin?" 
				+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
				+ "\">Return to user login</A><BR>");
	    out.println("<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smgl.GLMainMenu?" 
				+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
				+ "\">Return to General Ledger Main Menu</A><BR>");
	    out.println("<A HREF=\"" + WebContextParameters.getdocumentationpageURL(getServletContext()) 
	    	+ "#" + Long.toString(SMSystemFunctions.GLDuplicateExternalCompanyBatch) 
	    	+ "\">Summary</A><BR>");
	    
	    out.println("<BR>This will allow you to duplicate a POSTED batch from the company you select, and re-create that batch"
	    	+ " as an UNPOSTED batch in the current company.  From there you can edit or post that batch as needed."
	    	+ "<BR><BR>"
	    );
	    
    	out.println ("<FORM ACTION =\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smgl.GLDuplicateExternalBatchAction\">");
    	out.println("<INPUT TYPE=HIDDEN NAME='" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "' VALUE='" + sDBID + "'>\n");
    	out.println("<INPUT TYPE=HIDDEN NAME='CallingClass' VALUE='" + this.getClass().getName() + "'>\n");
    	
    	try {
			out.println(buildExternalCompanyList(sDBID, sUserFullName));
		} catch (Exception e) {
			out.println("<BR><B><FONT COLOR=RED>" + e.getMessage() + "</FONT></B><BR>");
		}
    	
    	//*************************************

		out.println("<BR>Duplicate posted batch number:&nbsp;");
		
		out.println("<INPUT TYPE=TEXT NAME = '" + PARAM_BATCH_NUMBER + "' SIZE = 10>");
    	
    	out.println ("<BR><INPUT TYPE=\"SUBMIT\" VALUE=\"----Duplicate batch----\">");
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
			sBackgroundColor = SMMasterStyleSheetDefinitions.BACKGROUND_WHITE;
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
