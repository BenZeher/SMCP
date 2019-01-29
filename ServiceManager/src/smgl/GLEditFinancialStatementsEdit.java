package smgl;

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
import SMDataDefinition.SMTableglstatementforms;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsManageRequestParameters;
import ServletUtilities.clsStringFunctions;
import smcontrolpanel.SMAuthenticate;
import smcontrolpanel.SMSystemFunctions;
import smcontrolpanel.SMUtilities;

public class GLEditFinancialStatementsEdit extends HttpServlet {

	private static final long serialVersionUID = 1L;
	private static String sObjectName = "Financial Statement";
	private static String sCalledClassName = "GLEditFinancialStatementsAction";
	public static String PRINT_SAMPLE_STATEMENT = "PRINTSAMPLESTATEMENT";
	public static String TEST_STATEMENT_BUTTON_LABEL = "Test Statement Form";
	public static String BUTTON_SUBMIT_EDIT = "SubmitEdit";
	public static String BUTTON_SUBMIT_ADD = "SubmitAdd";
	public static String BUTTON_SUBMIT_DELETE = "SubmitDelete";
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
				SMSystemFunctions.GLEditFinancialStatements
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
		String sStatementFormID = clsStringFunctions.filter(request.getParameter(SMTableglstatementforms.lid));

		String title = "";
		String subtitle = "";

		if(request.getParameter(BUTTON_SUBMIT_EDIT) != null){
			//User has chosen to edit:
			title = "Edit " + sObjectName + " " + sStatementFormID;
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
			out.println("<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smgl.GLMainMenu?" 
					+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
					+ "\">Return to General Ledger Main Menu</A><BR>");
			out.println("<A HREF=\"" + WebContextParameters.getdocumentationpageURL(getServletContext()) + "#" + Long.toString(SMSystemFunctions.GLEditFinancialStatements)
					+ "\">Summary</A><BR>");
			
			if (sStatementFormID == null){
				out.println("Invalid " + sObjectName + " ID. Please go back and try again.");
			}else{
				Edit_Record(sStatementFormID, out, sDBID, false, sUserID, sUserFullName, sDBID);
			}
		}
		if(request.getParameter(BUTTON_SUBMIT_DELETE) != null){
			//User has chosen to delete:
			title = "Delete " + sObjectName + ": " + sStatementFormID;
			subtitle = "";
			out.println(SMUtilities.SMCPTitleSubBGColor(title, subtitle, SMUtilities.getInitBackGroundColor(getServletContext(), sDBID), sCompanyName));

			//Print a link to the first page after login:
			out.println("<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMUserLogin?" 
				+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
				+ "\">Return to user login</A><BR><BR>");
			
			//Print a link to main menu:
			out.println("<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smgl.GLMainMenu?" 
					+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
					+ "\">Return to General Ledger Main Menu</A><BR>");
			out.println("<A HREF=\"" + WebContextParameters.getdocumentationpageURL(getServletContext()) + "#" + Long.toString(SMSystemFunctions.GLEditFinancialStatements)
					+ "\">Summary</A><BR>");

			if (request.getParameter("ConfirmDelete") == null){
				out.println ("You must check the 'confirm' check box to delete.");
			}
			else{
				if (Delete_Record(sStatementFormID, out, sDBID) == false){
					out.println ("Error deleting financial statement form: " + sStatementFormID + ".");
				}
				else{
					out.println ("Successfully deleted financial statement form: " + sStatementFormID + ".");
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
			out.println("<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smgl.GLMainMenu?" 
					+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
					+ "\">Return to General Ledger Main Menu</A><BR>");
			out.println("<A HREF=\"" + WebContextParameters.getdocumentationpageURL(getServletContext()) + "#" + Long.toString(SMSystemFunctions.GLEditFinancialStatements)
					+ "\">Summary</A><BR>");
			
			if (sNewCode == ""){
				out.println ("You chose to add a new " + sObjectName + ", but you did not enter a new " + sObjectName + " to add.");
			}
			else{
				Edit_Record(sNewCode, out, sDBID, true, sUserID, sUserFullName, sDBID);
			}
		}

		out.println("</BODY></HTML>");
	}

	private void Edit_Record(
			String sParameter, 
			PrintWriter pwOut, 
			String sDBIB,
			boolean bAddNew,
			String sUserID,
			String sUserFullName,
			String sDBID){

		pwOut.println("<FORM NAME='MAINFORM' ACTION='" 
				+ SMUtilities.getURLLinkBase(getServletContext()) 
				+ "smgl." + sCalledClassName + "' METHOD='POST'>\n");
		pwOut.println("<INPUT TYPE=HIDDEN NAME='" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "' VALUE='" + sDBID + "'>\n");
		pwOut.println("<INPUT TYPE=HIDDEN NAME='" + "CallingClass" + "' VALUE='" + SMUtilities.getFullClassName(this.toString()) + "'>\n");
		pwOut.println("<TABLE BORDER=12 CELLSPACING=2>\n");

		int iID = -1;
		String sFinancialStatementName = "";
		String sDescription = "";
		String sText = "";
		if (!bAddNew){
			try{
				//Get the record to edit:
				String sSQL = "SELECT * FROM " + SMTableglstatementforms.TableName
				+ " WHERE ("
				+ "(" + SMTableglstatementforms.lid + " = '" + sParameter + "')"
				+ ")"
				;
				if (bDebug){
					System.out.println("[1534453447] SQL = " + sSQL);
				}
				ResultSet rs = clsDatabaseFunctions.openResultSet(sSQL, getServletContext(), sDBIB);

				rs.next();
				iID = rs.getInt(SMTableglstatementforms.lid);
				sFinancialStatementName = rs.getString(SMTableglstatementforms.sname);
				sText = rs.getString(SMTableglstatementforms.mtext);
				sDescription = rs.getString(SMTableglstatementforms.sdescription);
				rs.close();
			}catch (SQLException ex){
				pwOut.println("<BR>Error [1534453448] reading GL financial statement form information - " + ex.getMessage());
			}
		}else{
			sFinancialStatementName = sParameter;
		}

		//Display fields:
		pwOut.println("<INPUT TYPE=HIDDEN NAME=\"" + SMTableglstatementforms.lid
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
			+ clsStringFunctions.filter(sFinancialStatementName) + "<INPUT TYPE=HIDDEN NAME=\"" + SMTableglstatementforms.sname
			+ "\" VALUE=\"" + sFinancialStatementName + "\">"
			+ "</TD>\n"
			+ "    <TD ALIGN=LEFT>Short financial statement form name</TD>\n"
			+ "  </TR>\n"
		);

		//Description:
		pwOut.println("  <TR>\n"
			+ "    <TD ALIGN=RIGHT><B>Description:</B>&nbsp;</TD>\n"
			+ "    <TD ALIGN=LEFT>" 
			+ "<INPUT NAME=\"" + SMTableglstatementforms.sdescription
			+ "\" VALUE=\"" + sDescription + "\""
			+ " SIZE= 120"
			+ " MAXLENGTH = " + Integer.toString(SMTableglstatementforms.sdescriptionlength)
			+ ">" 
			+ "</TD>\n"
			+ "    <TD ALIGN=LEFT>Longer description of this check form</TD>\n"
			+ "  </TR>\n"
		);
		
		//Text:
		pwOut.println("  <TR>\n"
			+ "    <TD ALIGN=RIGHT VALIGN=TOP><B>Text:</B>&nbsp;</TD>\n"
			+ "    <TD ALIGN=LEFT>\n"
				+ "      <TEXTAREA NAME=\"" + SMTableglstatementforms.mtext + "\""
				+ " rows=20"
				+ " cols=120"
				+ ">\n"
				+ filterTextareaText(sText)
				//+ SMUtilities.filter(sText)
				+ "</TEXTAREA>\n"
			+ "    </TD>\n"
			+ "    <TD ALIGN=LEFT VALIGN=TOP>Full formatting text of financial statement form</TD>\n"
		+"  </TR>\n");
		
		pwOut.println("</TABLE>\n<BR><P>");
		pwOut.println("<INPUT TYPE=SUBMIT NAME='SubmitEdit' VALUE='Update " + sObjectName 
			+ "' STYLE='height: 0.24in'>\n");

		if (iID != -1){
			pwOut.println("<BR><BR>");
			pwOut.println("<B><I><U>PRINT SAMPLE FORM</U></I></B><BR>\n"
				+ "Use this to see the financial statement as it will appear when it is printed.  "
				+ "NOTE: Save any changes before testing - the 'test' function uses the <B><I>last saved version</I></B> of the financial statement form."	
			);
			pwOut.println("<TABLE BORDER=1 >\n");
			pwOut.println("  <TR>\n"
					+ "    <TD ALIGN=CENTER COLSPAN=2 >\n"
						+ "<INPUT TYPE=SUBMIT NAME='" + PRINT_SAMPLE_STATEMENT + "'"
						+ " VALUE='" + TEST_STATEMENT_BUTTON_LABEL + "'" 
						+ " STYLE='height: 0.24in'>"
					+ "</TD>\n"
					
					+ "  </TR>\n"
				);
			
			pwOut.println("</TABLE>\n");
			
		}
		
		//Print the variable names and descriptions:
		pwOut.println(getStatementVariableTable());
		
		pwOut.println("</P></FORM>");
	}
	private String getStatementVariableTable(){
		String s = "";
		
		s += "<BR><BR><I><B>The following variables and functions can be used as when creating financial statement forms:</B></I>";
		
		s += "<TABLE>\n";
		
		GLStatementFormProcessor proc = new GLStatementFormProcessor();
		
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
	private String filterTextareaText(String sFinancialFormText){
		String s = sFinancialFormText.replace("</TEXTAREA>", "&lt;/TEXTAREA>");
		s = sFinancialFormText.replace("&nbsp;", "&amp;nbsp;");
		return s;
	}
	private boolean Delete_Record(
			String sFinancialFormID,
			PrintWriter pwOut,
			String sDBID){

		Connection conn = clsDatabaseFunctions.getConnection(
				getServletContext(), 
				sDBID, 
				"MySQL", 
				SMUtilities.getFullClassName(this.toString() + ".Delete_Record")
		);

		if (conn == null){
			pwOut.println("Error [1534453825] getting connection to delete record.");
			//System.out.println("Error getting connection to delete record.");
			return false;
		}

		String SQL = "DELETE FROM " + SMTableglstatementforms.TableName
		+ " WHERE ("
		+ SMTableglstatementforms.lid + " = " + sFinancialFormID
		+ ")"
		;
		try {
			Statement stmt = conn.createStatement();
			stmt.execute(SQL);
		}catch (SQLException ex){
			pwOut.println("Error [1534453826] deleting financial form with SQL: " + SQL 
					+ " - " + ex.getMessage());
			clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1547080747]");
			return false;
		}		

		clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1547080748]");

		return true;
	}

	public void doGet(HttpServletRequest request,
			HttpServletResponse response)
	throws ServletException, IOException {

		doPost(request, response);
	}
}
