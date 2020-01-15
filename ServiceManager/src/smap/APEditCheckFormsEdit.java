package smap;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import ConnectionPool.WebContextParameters;
import SMDataDefinition.SMTableapcheckforms;
import SMDataDefinition.SMTableapvendorremittolocations;
import SMDataDefinition.SMTablebkbanks;
import SMDataDefinition.SMTableicvendors;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsManageRequestParameters;
import ServletUtilities.clsStringFunctions;
import smcontrolpanel.SMAuthenticate;
import smcontrolpanel.SMSystemFunctions;
import smcontrolpanel.SMUtilities;

public class APEditCheckFormsEdit extends HttpServlet {

	private static final long serialVersionUID = 1L;
	private static final String sObjectName = "Check Form";
	private static final String sCalledClassName = "APEditCheckFormsAction";
	public static final String PRINT_SAMPLE_CHECKS = "PRINTSAMPLECHECKS";
	public static final String NUMBER_OF_SAMPLE_CHECKS_TO_PRINT = "NUMCHECKS";
	public static final String SAMPLE_VENDOR = "SAMPLEVENDOR";
	public static final String SAMPLE_REMIT_TO = "SAMPLEREMITTO";
	public static final String SAMPLE_BANK_ID = "SAMPLEBANKID";
	public static final String SAMPLE_NUMBER_OF_ADVICE_LINES = "SAMPLENUMBEROFADVICELINES";
	public static final String SAMPLE_NUMBER_OF_ADVICE_LINES_DEFAULT = "10";
	public static final String TEST_HTML_BUTTON_LABEL = "Test HTML Form";
	public static final String BUTTON_SUBMIT_EDIT = "SubmitEdit";
	public static final String BUTTON_SUBMIT_ADD = "SubmitAdd";
	public static final String BUTTON_SUBMIT_DELETE = "SubmitDelete";
	private boolean bDebug = false;
	
	@Override
	public void doPost(HttpServletRequest request,
			HttpServletResponse response)
	throws ServletException, IOException {

		PrintWriter out = response.getWriter();
		if (!SMAuthenticate.authenticateSMCPCredentials(
				request, 
				response, 
				getServletContext(),
				SMSystemFunctions.APEditCheckForms
		)
		){
			return;
		}
		//Get the session info:
		HttpSession CurrentSession = request.getSession(true);
		String sDBID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_DATABASE_ID);
		String sCompanyName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_COMPANYNAME);
		String sUserID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERID);
		String sUserFullName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERFIRSTNAME) + " "
				+ (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERLASTNAME);
		String sCheckFormID = clsStringFunctions.filter(request.getParameter(SMTableapcheckforms.lid));

		String title = "";
		String subtitle = "";

		if(request.getParameter(BUTTON_SUBMIT_EDIT) != null){
			//User has chosen to edit:
			title = "Edit " + sObjectName + " " + sCheckFormID;
			subtitle = "";
			out.println(SMUtilities.SMCPTitleSubBGColor(
					title, subtitle, SMUtilities.getInitBackGroundColor(getServletContext(), sDBID), sCompanyName));

		    //If there is a warning from trying to input previously, print it here:
			String sWarning = clsManageRequestParameters.get_Request_Parameter("Warning", request);
			if (sWarning.compareToIgnoreCase("") != 0){
				out.println("<B><FONT COLOR=\"RED\">WARNING: " + sWarning + "</FONT></B><BR>");
			}
			//If there is a status from trying to input previously, print it here:
			String sStatus = clsManageRequestParameters.get_Request_Parameter("Status", request);
			if (sStatus.compareToIgnoreCase("") != 0){
				out.println("<B>" + sStatus + "</B><BR>");
			}		

			//Print a link to the first page after login:
			out.println("<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMUserLogin?" 
				+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
				+ "\">Return to user login</A><BR><BR>");
			
			//Print a link to main menu:
			out.println("<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smap.APMainMenu?" 
					+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
					+ "\">Return to Accounts Payable Main Menu</A><BR>");
			out.println("<A HREF=\"" + WebContextParameters.getdocumentationpageURL(getServletContext()) + "#" + Long.toString(SMSystemFunctions.APEditCheckForms)
					+ "\">Summary</A><BR>");
			
			if (sCheckFormID == null){
				out.println("Invalid " + sObjectName + " ID. Please go back and try again.");
			}else{
				Edit_Record(sCheckFormID, out, sDBID, false, sUserID, sUserFullName);
			}
		}
		if(request.getParameter(BUTTON_SUBMIT_DELETE) != null){
			//User has chosen to delete:
			title = "Delete " + sObjectName + ": " + sCheckFormID;
			subtitle = "";
			out.println(SMUtilities.SMCPTitleSubBGColor(title, subtitle, SMUtilities.getInitBackGroundColor(getServletContext(), sDBID), sCompanyName));

			//Print a link to the first page after login:
			out.println("<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMUserLogin?" 
				+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
				+ "\">Return to user login</A><BR><BR>");
			
			//Print a link to main menu:
			out.println("<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smap.APMainMenu?" 
					+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
					+ "\">Return to Accounts Payable Main Menu</A><BR>");
			out.println("<A HREF=\"" + WebContextParameters.getdocumentationpageURL(getServletContext()) + "#" + Long.toString(SMSystemFunctions.APEditCheckForms)
					+ "\">Summary</A><BR>");

			if (request.getParameter("ConfirmDelete") == null){
				out.println ("You must check the 'confirm' check box to delete.");
			}
			else{
				if (Delete_Record(sCheckFormID, out, sDBID) == false){
					out.println ("Error deleting check form: " + sCheckFormID + ".");
				}
				else{
					out.println ("Successfully deleted check form: " + sCheckFormID + ".");
				}
			}
		}
		if(request.getParameter(BUTTON_SUBMIT_ADD) != null){

			String sNewCode = clsStringFunctions.filter(request.getParameter("New" + sObjectName));
			//User has chosen to add a new object:
			title = "Add " + sObjectName + ": " + sNewCode;
			subtitle = "";
			out.println(SMUtilities.SMCPTitleSubBGColor(
					title, 
					subtitle, 
					SMUtilities.getInitBackGroundColor(getServletContext(), sDBID),
					sCompanyName));

			//Print a link to the first page after login:
			out.println("<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMUserLogin?" 
				+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
				+ "\">Return to user login</A><BR><BR>");

			//Print a link to main menu:
			out.println("<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smap.APMainMenu?" 
					+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
					+ "\">Return to Accounts Payable Main Menu</A><BR>");
			out.println("<A HREF=\"" + WebContextParameters.getdocumentationpageURL(getServletContext()) + "#" + Long.toString(SMSystemFunctions.APEditCheckForms)
					+ "\">Summary</A><BR>");
			
			if (sNewCode == ""){
				out.println ("You chose to add a new " + sObjectName + ", but you did not enter a new " + sObjectName + " to add.");
			}
			else{
				Edit_Record(sNewCode, out, sDBID, true, sUserID, sUserFullName);
			}
		}

		out.println("</BODY></HTML>");
	}

	private void Edit_Record(
			String sParameter, 
			PrintWriter pwOut, 
			String sDBID,
			boolean bAddNew,
			String sUserID,
			String sUserFullName
			){

		pwOut.println("<FORM NAME='MAINFORM' ACTION='" 
				+ SMUtilities.getURLLinkBase(getServletContext()) 
				+ "smap." + sCalledClassName + "' METHOD='POST'>\n");
		pwOut.println("<INPUT TYPE=HIDDEN NAME='" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "' VALUE='" + sDBID + "'>\n");
		pwOut.println("<INPUT TYPE=HIDDEN NAME='" + "CallingClass" + "' VALUE='" + SMUtilities.getFullClassName(this.toString()) + "'>\n");
		pwOut.println("<TABLE BORDER=12 CELLSPACING=2>\n");

		int iID = -1;
		String sCheckFormName = "";
		String sDescription = "";
		String sText = "";
		String sMaxNumberOfAdviceLinesPerPage = "0";
		if (!bAddNew){
			try{
				//Get the record to edit:
				String sSQL = "SELECT * FROM " + SMTableapcheckforms.TableName
				+ " WHERE ("
				+ "(" + SMTableapcheckforms.lid + " = '" + sParameter + "')"
				+ ")"
				;
				if (bDebug){
					System.out.println("[1579114156] SQL = " + sSQL);
				}
				ResultSet rs = clsDatabaseFunctions.openResultSet(sSQL, getServletContext(), sDBID);

				rs.next();
				iID = rs.getInt(SMTableapcheckforms.lid);
				sCheckFormName = rs.getString(SMTableapcheckforms.sname);
				sText = rs.getString(SMTableapcheckforms.mtext);
				sDescription = rs.getString(SMTableapcheckforms.sdescription);
				sMaxNumberOfAdviceLinesPerPage = Integer.toString(rs.getInt(SMTableapcheckforms.inumberofadvicelinesperpage));
				rs.close();
			}catch (SQLException ex){
				pwOut.println("<BR>Error [1502976602] reading check form information - " + ex.getMessage());
			}
		}else{
			sCheckFormName = sParameter;
		}

		//Display fields:
		pwOut.println("<INPUT TYPE=HIDDEN NAME=\"" + SMTableapcheckforms.lid
				+ "\" VALUE=\"" + Integer.toString(iID) + "\">\n");
		//ID:
		pwOut.println("  <TR>"
			+ "    <TD ALIGN=RIGHT><B>ID:</B></TD>\n"
			+ "    <TD>");
		if (iID < 0){
			pwOut.println("New");
		}else{
			pwOut.println(iID);
		}
		pwOut.println("</TD>\n"
			+ "    <TD>&nbsp;</TD>\n"
			+ "  </TR>\n"
		);

		//Name:
		pwOut.println("  <TR>\n"
			+ "    <TD ALIGN=RIGHT><B>Name:</B>&nbsp;</TD>\n"
			+ "    <TD ALIGN=LEFT>" 
			+ clsStringFunctions.filter(sCheckFormName) + "<INPUT TYPE=HIDDEN NAME=\"" + SMTableapcheckforms.sname
			+ "\" VALUE=\"" + sCheckFormName + "\">"
			+ "</TD>\n"
			+ "    <TD ALIGN=LEFT>Short check form name</TD>\n"
			+ "  </TR>\n"
		);

		//Description:
		pwOut.println("  <TR>\n"
			+ "    <TD ALIGN=RIGHT><B>Description:</B>&nbsp;</TD>\n"
			+ "    <TD ALIGN=LEFT>" 
			+ "<INPUT NAME=\"" + SMTableapcheckforms.sdescription
			+ "\" VALUE=\"" + sDescription + "\""
			+ " SIZE= 120"
			+ " MAXLENGTH = " + Integer.toString(SMTableapcheckforms.sdescriptionlength)
			+ ">" 
			+ "</TD>\n"
			+ "    <TD ALIGN=LEFT>Longer description of this check form</TD>\n"
			+ "  </TR>\n"
		);

		//Max number of advice lines per page:
		pwOut.println("  <TR>\n"
			+ "    <TD ALIGN=RIGHT><B>Max number of advice lines per page:</B>&nbsp;</TD>\n"
			+ "    <TD ALIGN=LEFT>" 
			+ "<INPUT NAME=\"" + SMTableapcheckforms.inumberofadvicelinesperpage
			+ "\" VALUE=\"" + sMaxNumberOfAdviceLinesPerPage + "\""
			+ " SIZE= 10"
			+ " MAXLENGTH = " + "2"
			+ ">" 
			+ "</TD>\n"
			+ "    <TD ALIGN=LEFT>This is the MAXIMUM number of advice lines you want printed per page.  If there are"
				+ " more advice lines on a check than this maximum number, they will print on a second page.</TD>\n"
			+ "</TR>\n"
		);
		
		//Text:
		pwOut.println("  <TR>\n"
			+ "    <TD ALIGN=RIGHT VALIGN=TOP><B>Text:</B>&nbsp;</TD>\n"
			+ "    <TD ALIGN=LEFT>\n"
				+ "      <TEXTAREA NAME=\"" + SMTableapcheckforms.mtext + "\""
				+ " rows=20"
				+ " cols=120"
				+ ">\n"
				+ filterTextareaText(sText) + "\n"
				//+ SMUtilities.filter(sText)
				+ "      </TEXTAREA>\n"
			+ "    </TD>\n"
			+ "    <TD ALIGN=LEFT VALIGN=TOP>Full formatting text of check form</TD>\n"
		+"  </TR>\n");
		
		pwOut.println("</TABLE>\n<BR><P>");
		pwOut.println("<INPUT TYPE=SUBMIT NAME='SubmitEdit' VALUE='Update " + sObjectName 
			+ "' STYLE='height: 0.24in'>\n");

		if (iID != -1){
			pwOut.println("<BR><BR>");
			pwOut.println("<B><I><U>PRINT SAMPLE CHECKS</U></I></B><BR>\n"
				+ "Use this to see the check form as it will appear when the check is printed.  "
				+ "NOTE: Save any changes before testing - the 'test' function uses the <B><I>last saved version</I></B> of the check form."	
			);
			pwOut.println("<TABLE BORDER=1 >\n");

			pwOut.println("  <TR>\n"
					+ "    <TD ALIGN=RIGHT >\n"
					+ "How many sample checks do you wish to print?"
					+ "</TD>\n"
					+ "    <TD ALIGN=LEFT >\n"
						+ "<INPUT NAME=\"" + NUMBER_OF_SAMPLE_CHECKS_TO_PRINT
						+ "\" VALUE=\"" + "1" + "\""
						+ " SIZE= 5"
						+ " MAXLENGTH = " + "2"
						+ ">"
					+ "</TD>\n"
					
					+ "  </TR>\n"
				);
			
			pwOut.println("  <TR>\n"
					+ "    <TD ALIGN=RIGHT >\n"
					+ "Print samples for this vendor:"
					+ "</TD>\n"
					+ "    <TD ALIGN=LEFT >\n"
					+ "<INPUT NAME=\"" + SAMPLE_VENDOR
					+ "\" VALUE=\"" + "" + "\""
					+ " SIZE= 10"
					+ " MAXLENGTH = " + SMTableicvendors.svendoracctLength
					+ ">"
					+ "</TD>\n"
					
					+ "  </TR>\n"
				);
			
			pwOut.println("  <TR>\n"
					+ "    <TD ALIGN=RIGHT >\n"
					+ "Using this remit-to code:"
					+ "</TD>\n"
					+ "    <TD ALIGN=LEFT >\n"
					+ "<INPUT NAME=\"" + SAMPLE_REMIT_TO
					+ "\" VALUE=\"" + "" + "\""
					+ " SIZE= 10"
					+ " MAXLENGTH = " + SMTableapvendorremittolocations.sremittocodelength
					+ ">"
					+ "</TD>\n"
					
					+ "  </TR>\n"
				);
			
			//Bank:
			String SQL = "SELECT"
				+ " " + SMTablebkbanks.lid
				+ ", " + SMTablebkbanks.sshortname
				+ " FROM " + SMTablebkbanks.TableName
				+ " ORDER BY " + SMTablebkbanks.sshortname
			;
			
			String s = "  <TR>\n"
					+ "    <TD ALIGN=RIGHT >\n"
					+ "Use this bank:"
					+ "</TD>\n"
					+ "    <TD ALIGN=LEFT >\n"
					+ "<SELECT"
						+ " NAME='" + SAMPLE_BANK_ID + "'"
						+ "ID = '" + SAMPLE_BANK_ID + "'"
						+ " <OPTION VALUE='0'>*** SELECT A BANK FOR THE SAMPLE ***</OPTION>"
					;
			
			try {
				ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, getServletContext(), sDBID, "MySQL", SMUtilities.getFullClassName(this.toString()) + ".Edit_Record - user: " 
			    + sUserID
			    + " - "
			    + sUserFullName
						
						);
				while (rs.next()){
					s += "<OPTION VALUE='" + Long.toString(rs.getLong(SMTablebkbanks.lid)) + "'>"
						+ rs.getString(SMTablebkbanks.sshortname)
						+ "</OPTION>"
					;
				}
				rs.close();
			} catch (SQLException e) {
				pwOut.println("<BR>Error [1504011872] reading bank information with SQL '" + SQL + "' - " + e.getMessage());
			}
			
			s += "</SELECT>"
				+ "</TD>\n"
				+ "  </TR>"
			;
			
			pwOut.println(s);
			
			
			pwOut.println("  <TR>\n"
					+ "    <TD ALIGN=RIGHT >\n"
					+ "How many sample advice lines?"
					+ "</TD>\n"
					+ "    <TD ALIGN=LEFT >\n"
					+ "<INPUT NAME=\"" + SAMPLE_NUMBER_OF_ADVICE_LINES
					+ "\" VALUE=\"" + SAMPLE_NUMBER_OF_ADVICE_LINES_DEFAULT + "\""
					+ " SIZE= 5"
					+ " MAXLENGTH = " + "3"
					+ ">"
					+ "</TD>\n"
					
					+ "  </TR>\n"
				);
			
			pwOut.println("  <TR>\n"
					+ "    <TD ALIGN=CENTER COLSPAN=2 >\n"
						+ "<INPUT TYPE=SUBMIT NAME='" + PRINT_SAMPLE_CHECKS + "'"
						+ " VALUE='" + TEST_HTML_BUTTON_LABEL + "'" 
						+ " STYLE='height: 0.24in'>"
					+ "</TD>\n"
					
					+ "  </TR>\n"
				);
			
			pwOut.println("</TABLE>\n");
			
		}
		
		//Print the variable names and descriptions:
		pwOut.println(getCheckVariableTable());
		
		pwOut.println("</P></FORM>");
	}
	private String getCheckVariableTable(){
		String s = "";
		
		s += "<BR><BR><I><B>The following variables can be used as placeholders for the run time check and advice values:</B></I>";
		
		s += "<TABLE>\n";
		
		APCheckFormProcessor proc = new APCheckFormProcessor();
		
		for (int i = 0; i < proc.getVariableNames().size(); i++){
			s += "  <TR>\n"
				+ "    <TD>"
				+ "<B>" + proc.getVariableNames().get(i) + "</B>"
				+ "</TD>\n"
				+ "    <TD>"
				+ proc.getVariableDescriptions().get(i)
				+ "</TD>\n"
				+ "  </TR>"
			;
		}
		
		s += "</TABLE>\n";
		return s;
		
	}
	private String filterTextareaText(String sCheckFormText){
		String s = sCheckFormText.replace("</TEXTAREA>", "&lt;/TEXTAREA>");
		s = sCheckFormText.replace("&nbsp;", "&amp;nbsp;");
		return s;
	}
	private boolean Delete_Record(
			String sCheckFormID,
			PrintWriter pwOut,
			String sDBID){

		Connection conn = clsDatabaseFunctions.getConnection(
				getServletContext(), 
				sDBID, 
				"MySQL", 
				SMUtilities.getFullClassName(this.toString() + ".Delete_Record")
		);

		if (conn == null){
			pwOut.println("Error [1502976809] getting connection to delete record.");
			//System.out.println("Error getting connection to delete record.");
			return false;
		}

		String SQL = "DELETE FROM " + SMTableapcheckforms.TableName
		+ " WHERE ("
		+ SMTableapcheckforms.lid + " = " + sCheckFormID
		+ ")"
		;
		try {
			Statement stmt = conn.createStatement();
			stmt.execute(SQL);
		}catch (SQLException ex){
			pwOut.println("Error [1502976881] deleting check form with SQL: " + SQL 
					+ " - " + ex.getMessage());
			clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1547059457]");
			return false;
		}		

		clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1547059458]");

		return true;
	}

	public void doGet(HttpServletRequest request,
			HttpServletResponse response)
	throws ServletException, IOException {

		doPost(request, response);
	}
}
