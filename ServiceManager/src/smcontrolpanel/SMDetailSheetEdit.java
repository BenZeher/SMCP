package smcontrolpanel;

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

import SMDataDefinition.SMTableworkorderdetailsheets;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsManageRequestParameters;
import ServletUtilities.clsStringFunctions;

public class SMDetailSheetEdit extends HttpServlet {

	private static final long serialVersionUID = 1L;
	private static final String sObjectName = "Detail Sheet";
	private static final String sCalledClassName = "SMDetailSheetAction";
	public static final String TEST_HTML_BUTTON_NAME = "TESTHTMLBUTTONNAME";
	public static final String TEST_HTML_BUTTON_LABEL = "Test HTML Form";
	public static final String DETAIL_SHEET_ID = "DETAILSHEETID";
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
				SMSystemFunctions.SMEditDetailSheets
		)
		){
			return;
		}
		//Get the session info:
		HttpSession CurrentSession = request.getSession(true);
		String sDBID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_DATABASE_ID);
		String sCompanyName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_COMPANYNAME);
		String sUserID = (String)CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERID);
		String sUserFirstName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERFIRSTNAME);
		String sUserLastName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERLASTNAME);
		String sDetailSheetID = clsStringFunctions.filter(request.getParameter(DETAIL_SHEET_ID));

		String title = "";
		String subtitle = "";

		if(request.getParameter(BUTTON_SUBMIT_EDIT) != null){
			//User has chosen to edit:
			title = "Edit " + sObjectName + " " + sDetailSheetID;
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
			if (sDetailSheetID == null){
				out.println("Invalid " + sObjectName + " ID. Please go back and try again.");
			}else{
				Edit_Record(sDetailSheetID, out, sDBID, false);
			}
		}
		if(request.getParameter(BUTTON_SUBMIT_DELETE) != null){
			//User has chosen to delete:
			title = "Delete " + sObjectName + ": " + sDetailSheetID;
			subtitle = "";
			out.println(SMUtilities.SMCPTitleSubBGColor(title, subtitle, SMUtilities.getInitBackGroundColor(getServletContext(), sDBID), sCompanyName));

			//Print a link to the first page after login:
			out.println("<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMUserLogin?" 
				+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
				+ "\">Return to user login</A><BR><BR>");

			if (request.getParameter("ConfirmDelete") == null){
				out.println ("You must check the 'confirm' check box to delete.");
			}
			else{
				if (Delete_Record(sDetailSheetID, out, sDBID, sUserID, sUserFirstName, sUserLastName) == false){
					out.println ("Error deleting detail sheet: " + sDetailSheetID + ".");
				}
				else{
					out.println ("Successfully deleted detail sheet: " + sDetailSheetID + ".");
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

			if (sNewCode == ""){
				out.println ("You chose to add a new " + sObjectName + ", but you did not enter a new " + sObjectName + " to add.");
			}
			else{
				Edit_Record(sNewCode, out, sDBID, true);
			}
		}

		out.println("</BODY></HTML>");
	}

	private void Edit_Record(
			String sParameter, 
			PrintWriter pwOut, 
			String sDBID,
			boolean bAddNew){

		pwOut.println("<FORM NAME='MAINFORM' ACTION='" 
				+ SMUtilities.getURLLinkBase(getServletContext()) 
				+ "smcontrolpanel." + sCalledClassName + "' METHOD='POST'>");
		pwOut.println("<INPUT TYPE=HIDDEN NAME='" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "' VALUE='" + sDBID + "'>");
		pwOut.println("<INPUT TYPE=HIDDEN NAME='" + "CallingClass" + "' VALUE='" + SMUtilities.getFullClassName(this.toString()) + "'>");
		pwOut.println("<TABLE BORDER=12 CELLSPACING=2>");

		int iID = -1;
		String sDetailSheetName = "";
		String sDescription = "";
		String sText = "";
		String sType = "";
		if (!bAddNew){
			try{
				//Get the record to edit:
				String sSQL = "SELECT * FROM " + SMTableworkorderdetailsheets.TableName
				+ " WHERE ("
				+ "(" + SMTableworkorderdetailsheets.lid + " = '" + sParameter + "')"
				+ ")"
				;
				if (bDebug){
					System.out.println("SQL = " + sSQL);
				}
				ResultSet rs = clsDatabaseFunctions.openResultSet(sSQL, getServletContext(), sDBID);

				rs.next();
				iID = rs.getInt(SMTableworkorderdetailsheets.lid);
				sDetailSheetName = rs.getString(SMTableworkorderdetailsheets.sname);
				sText = rs.getString(SMTableworkorderdetailsheets.mtext);
				sDescription = rs.getString(SMTableworkorderdetailsheets.sdescription);
				sType = Long.toString(rs.getLong(SMTableworkorderdetailsheets.itype));
				rs.close();
			}catch (SQLException ex){
				pwOut.println("<BR>Error reading detail sheet information - " + ex.getMessage());
			}
		}else{
			sDetailSheetName = sParameter;
		}

		//Display fields:
		pwOut.println("<INPUT TYPE=HIDDEN NAME=\"" + DETAIL_SHEET_ID
				+ "\" VALUE=\"" + Integer.toString(iID) + "\">");
		//ID:
		pwOut.println("<TR>\n<TD ALIGN=RIGHT><B>ID:</B></TD>\n"
				+ "<TD>\n");
		if (iID < 0){
			pwOut.println("New");
		}else{
			pwOut.println(iID);
		}
		pwOut.println("</TD>\n"
				+ "<TD>\n&nbsp;</TD>\n</TR>\n"
		);

		//Name:
		pwOut.println("<TR>\n"
			+ "<TD ALIGN=RIGHT><B>Name:</B>&nbsp;</TD>\n"
			+ "<TD ALIGN=LEFT>" 
			+ clsStringFunctions.filter(sDetailSheetName) + "<INPUT TYPE=HIDDEN NAME=\"" + SMTableworkorderdetailsheets.sname
			+ "\" VALUE=\"" + sDetailSheetName + "\">"
			+ "</TD>\n"
			+ "<TD ALIGN=LEFT>Short detail sheet name</TD>\n"
			+ "</TR>\n"
		);

		//Description:
		pwOut.println("<TR>\n"
			+ "<TD ALIGN=RIGHT><B>Description:</B>&nbsp;</TD>\n"
			+ "<TD ALIGN=LEFT>" 
			+ "<INPUT NAME=\"" + SMTableworkorderdetailsheets.sdescription
			+ "\" VALUE=\"" + sDescription + "\""
			+ " SIZE= 120"
			+ " MAXLENGTH = " + Integer.toString(SMTableworkorderdetailsheets.sdescriptionlength)
			+ ">" 
			+ "</TD>\n"
			+ "<TD ALIGN=LEFT>Longer description of this detail sheet</TD>\n"
			+ "</TR>\n"
		);

		//Type of detail sheet:
		pwOut.println("<TR>\n"
			+ "<TD ALIGN=RIGHT><B>Simple text or " + SMTableworkorderdetailsheets.WEB_ENTRY_FORM_LABEL + ":</B>&nbsp;</TD>\n"
			+ "<TD ALIGN=LEFT>"
		);
		if (sType.compareToIgnoreCase(Integer.toString(SMTableworkorderdetailsheets.DETAIL_SHEET_TYPE_HTML)) == 0){
			pwOut.println("<input type=\"radio\" name=\"" + SMTableworkorderdetailsheets.itype + "\" value=\"" 
					+ Integer.toString(SMTableworkorderdetailsheets.DETAIL_SHEET_TYPE_TEXT) 
					+ "\"" + ">" 
					+ "Simple text" + "&nbsp;"
					+ "<input type=\"radio\" name=\"" + SMTableworkorderdetailsheets.itype + "\" value=\"" 
					+ Integer.toString(SMTableworkorderdetailsheets.DETAIL_SHEET_TYPE_HTML)  
					+ "\"" + " checked" + ">" 
					+ SMTableworkorderdetailsheets.WEB_ENTRY_FORM_LABEL
			);

		}else{
			pwOut.println("<input type=\"radio\" name=\"" + SMTableworkorderdetailsheets.itype + "\" value=\"" 
					+ Integer.toString(SMTableworkorderdetailsheets.DETAIL_SHEET_TYPE_TEXT) 
					+ "\"" + " checked" + ">" 
					+ "Simple text" + "&nbsp;"
					+ "<input type=\"radio\" name=\"" + SMTableworkorderdetailsheets.itype + "\" value=\"" 
					+ Integer.toString(SMTableworkorderdetailsheets.DETAIL_SHEET_TYPE_HTML)  
					+ "\"" + ">" 
					+ SMTableworkorderdetailsheets.WEB_ENTRY_FORM_LABEL
			);
		}
		pwOut.println("</TD>\n"
				+ "<TD ALIGN=LEFT>Simple text will just appear on the work order; "
				+ SMTableworkorderdetailsheets.WEB_ENTRY_FORM_LABEL + " will create a new form each time for the technician to enter detail sheet information.</TD>\n"
				+ "</TR>\n"
			);

		//Text:
		pwOut.println("<TR>\n"
			+ "<TD ALIGN=RIGHT VALIGN=TOP><B>Text:</B>&nbsp;</TD>\n"
			+ "<TD ALIGN=LEFT>"
				+ "<TEXTAREA NAME=\"" + SMTableworkorderdetailsheets.mtext + "\""
				+ " rows=20"
				+ " cols=120"
				+ ">"
				+ filterTextareaText(sText)
				//+ SMUtilities.filter(sText)
				+ "</TEXTAREA>"
			+ "</TD>\n"
			+ "<TD ALIGN=LEFT VALIGN=TOP>Full text of detail sheet</TD>\n"
		+"</TR>\n");
		
		pwOut.println("</TABLE><BR><P>");
		pwOut.println("<INPUT TYPE=SUBMIT NAME='SubmitEdit' VALUE='Update " + sObjectName 
			+ "' STYLE='height: 0.24in'>");
		if (iID != -1){
			pwOut.println("<BR><BR>\n<INPUT TYPE=SUBMIT NAME='" + TEST_HTML_BUTTON_NAME + "'"
				+ " VALUE='" + TEST_HTML_BUTTON_LABEL + "'" 
				+ " STYLE='height: 0.24in'>"
				+ "&nbsp;\nUse this to see the HTML form as it will appear for the user.  \n"
				+ "NOTE: Save any changes before testing - the 'test' function uses the <B><I>last saved version</I</B> of the detail sheet.  \n"
				+ "<BR><FONT COLOR=RED><B><I>This only works for 'HTML entry forms', not plain text detail sheets.</I></B></FONT>"
			);
		}
		pwOut.println("</P></FORM>");
	}
	private String filterTextareaText(String sDetailSheetText){
		String s = sDetailSheetText.replace("</TEXTAREA>", "&lt;/TEXTAREA>");
		clsStringFunctions.filter("");
		return s;
	}
	private boolean Delete_Record(
			String sDetailSheetID,
			PrintWriter pwOut,
			String sDBID,
			String sUserID,
			String sUserFirstName,
			String sUserLastName){

		Connection conn = clsDatabaseFunctions.getConnection(
				getServletContext(), 
				sDBID, 
				"MySQL", 
				SMUtilities.getFullClassName(this.toString() 
				+ ".Delete_Record"
				+ " - user: "
				+ sUserID
				+ " - "
				+ sUserFirstName
				+ " "
				+ sUserLastName)
		);

		if (conn == null){
			pwOut.println("Error getting connection to delete record.");
			//System.out.println("Error getting connection to delete record.");
			return false;
		}

		String SQL = "DELETE FROM " + SMTableworkorderdetailsheets.TableName
		+ " WHERE ("
		+ SMTableworkorderdetailsheets.lid + " = " + sDetailSheetID
		+ ")"
		;
		try {
			Statement stmt = conn.createStatement();
			stmt.execute(SQL);
		}catch (SQLException ex){
			pwOut.println("Error deleting detail sheet with SQL: " + SQL 
					+ " - " + ex.getMessage());
			clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1547080439]");
			return false;
		}		

		clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1547080440]");

		return true;
	}

	public void doGet(HttpServletRequest request,
			HttpServletResponse response)
	throws ServletException, IOException {

		doPost(request, response);
	}
}
