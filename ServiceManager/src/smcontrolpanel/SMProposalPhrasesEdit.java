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

import SMDataDefinition.SMTableproposalphrasegroups;
import SMDataDefinition.SMTableproposalphrases;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsManageRequestParameters;
import ServletUtilities.clsStringFunctions;
import ServletUtilities.clsTextEditorFunctions;

public class SMProposalPhrasesEdit extends HttpServlet {

	private static final long serialVersionUID = 1L;
	private static final String sObjectName = "Proposal Phrase";
	private static final String sCalledClassName = "SMProposalPhrasesAction";
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
				SMSystemFunctions.SMEditProposalPhrases
		)
		){
			return;
		}
		//Get the session info:
		HttpSession CurrentSession = request.getSession(true);
		String sDBID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_DATABASE_ID);
		String sCompanyName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_COMPANYNAME);
		String sProposalPhraseID = clsStringFunctions.filter(request.getParameter(SMTableproposalphrases.sproposalphrasename));
		String sProposalGroupID = clsManageRequestParameters.get_Request_Parameter(SMTableproposalphrases.iphrasegroupid, request);
		if(sProposalGroupID.compareToIgnoreCase("") == 0) {
			sProposalGroupID = "0";
		}

		String title = "";
		String subtitle = "";

		if(request.getParameter("SubmitEdit") != null){
			//User has chosen to edit:
			title = "Edit " + sObjectName + " " + sProposalPhraseID;
			subtitle = "";
			out.println(SMUtilities.SMCPTitleSubBGColor(
					title, subtitle, SMUtilities.getInitBackGroundColor(getServletContext(), sDBID), sCompanyName));

			//Print a link to the first page after login:
			out.println("<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMUserLogin?" 
				+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
				+ "\">Return to user login</A><BR><BR>");
			String sWarning = clsManageRequestParameters.get_Request_Parameter("Warning", request);
			 if(sWarning.compareToIgnoreCase("") != 0) {
				 out.println("<B><FONT COLOR=RED>WARNING: " + sWarning + "</FONT></B>");
			 }
			 String sStatus = clsManageRequestParameters.get_Request_Parameter("Status", request);
			 if(sStatus.compareToIgnoreCase("") != 0) {
				 out.println("<B>STATUS: " + sStatus + "</B>");
			 }
			
			if (sProposalPhraseID == null){
				out.println("Invalid " + sObjectName + "ID. Please go back and try again.");
			}else{
				Edit_Record(sProposalPhraseID, out, sDBID, false);
			}
		}
		if(request.getParameter("SubmitDelete") != null){
			//User has chosen to delete:
			title = "Delete " + sObjectName + ": " + sProposalPhraseID;
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
				if (Delete_Record(sProposalPhraseID, out, sDBID) == false){
					out.println ("Error deleting proposal phrase: " + sProposalPhraseID + ".");
				}
				else{
					out.println ("Successfully deleted proposal phrase: " + sProposalPhraseID + ".");
				}
			}
		}
		if(request.getParameter("SubmitAdd") != null){

			String sNewCode = clsStringFunctions.filter(clsManageRequestParameters.get_Request_Parameter("New" + sObjectName, request));
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


			Edit_Record(sProposalGroupID, out, sDBID, true);
		
		}

		out.println("</BODY></HTML>");
	}

	private void Edit_Record(
			String sParameter, 
			PrintWriter pwOut, 
			String sDBID,
			boolean bAddNew){
		pwOut.println(clsTextEditorFunctions.getJavascriptTextEditToolBarFunctions());
		pwOut.println("<FORM NAME='MAINFORM' ACTION='" 
				+ SMUtilities.getURLLinkBase(getServletContext()) 
				+ "smcontrolpanel." + sCalledClassName + "' METHOD='POST'>");
		pwOut.println("<INPUT TYPE=HIDDEN NAME='" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "' VALUE='" + sDBID + "'>");
		pwOut.println("<TABLE BORDER=12 CELLSPACING=2>");

		int iID = -1;
		String sProposalPhraseName = "";
		String sProposalPhrase = "";
		int iProposalPhraseGroupID = 0;
		int iSortOrder = 0;
		if (!bAddNew){
			try{
				//Get the record to edit:
				String sSQL = "SELECT * FROM " + SMTableproposalphrases.TableName
				+ " WHERE ("
				+ "(" + SMTableproposalphrases.sid + " = '" + sParameter + "')"
				+ ")"
				;
				if (bDebug){
					System.out.println("[1579274811] SQL = " + sSQL);
				}
				ResultSet rs = clsDatabaseFunctions.openResultSet(sSQL, getServletContext(), sDBID);

				rs.next();
				iID = rs.getInt(SMTableproposalphrases.sid);
				sProposalPhraseName = clsDatabaseFunctions.getRecordsetStringValue(rs, SMTableproposalphrases.sproposalphrasename);
				sProposalPhrase = clsDatabaseFunctions.getRecordsetStringValue(rs, SMTableproposalphrases.mproposalphrase);
				iProposalPhraseGroupID = rs.getInt(SMTableproposalphrases.iphrasegroupid);
				iSortOrder = rs.getInt(SMTableproposalphrases.isortorder);
				rs.close();
			}catch (SQLException ex){
				pwOut.println("<BR>Error reading proposal phrase information - " + ex.getMessage());
			}
		}else{
			iProposalPhraseGroupID = Integer.parseInt(sParameter);
		}

		//Display fields:
		pwOut.println("<INPUT TYPE=HIDDEN NAME=\"" + SMTableproposalphrases.sid
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

		//Phrase name:
		pwOut.println("<TR>\n"
			+ "<TD ALIGN=RIGHT><B>Name:</B>&nbsp;</TD>\n"
			+ "<TD ALIGN=LEFT>" 
			+ "<INPUT TYPE=TEXT NAME=\"" + SMTableproposalphrases.sproposalphrasename
			+ "\" VALUE=\"" + clsStringFunctions.filter(sProposalPhraseName) + "\""
			+ " SIZE=\"" + Integer.toString(SMTableproposalphrases.sproposalphrasenameLength) + "\""
			+ " MAXLENGTH=\"" + Integer.toString(SMTableproposalphrases.sproposalphrasenameLength) + "\">" 
			+ "</TD>\n"
			+ "<TD ALIGN=LEFT>Proposal phrase name</TD>\n"
			+ "</TR>\n"
		);

		//Proposal phrase:
		pwOut.println(clsTextEditorFunctions.Create_Edit_Form_Editable_MultilineText_Input_Row (
				SMTableproposalphrases.mproposalphrase,
				sProposalPhrase,
				"Phrase:",
				"Full phrase that will be inserted into proposals",
				500,
				1200,
				"",
				false,
				false
		));
		
		//Proposal phrase group:
		pwOut.println("<TR>\n"
				+ "<TD ALIGN=RIGHT><B>Phrase group:</B>&nbsp;</TD>\n"
				+ "<TD ALIGN=LEFT>");
		try{
	        String sSQL = "SELECT * FROM " 
	        	+ SMTableproposalphrasegroups.TableName
	        	+ " ORDER BY " + SMTableproposalphrasegroups.sid;
	        ResultSet rs = clsDatabaseFunctions.openResultSet(sSQL, getServletContext(), sDBID);
	        pwOut.println ("<SELECT NAME=\"" + SMTableproposalphrases.iphrasegroupid + "\">" );
        	
        	while (rs.next()){
        		String sSelected = "";
        		if (iProposalPhraseGroupID == rs.getInt(SMTableproposalphrasegroups.sid)){
        			sSelected = " SELECTED ";
        		}
        		pwOut.println ("<OPTION " + sSelected + " VALUE=\"" + rs.getString(SMTableproposalphrasegroups.sid) + "\">\n"
        			+ Integer.toString(rs.getInt(SMTableproposalphrasegroups.sid)) + " - "
        			+ rs.getString(SMTableproposalphrasegroups.sgroupname) + "\n"
        		); 
        	}
        	rs.close();
	        	//End the drop down list:
        	pwOut.println ("</SELECT>");
		}catch (SQLException ex){
			pwOut.println("Error getting list of proposal phrase groups - " + ex.getMessage());
		}
		pwOut.println("</TD>\n");
		pwOut.println("<TD>\nChoose a group in which to include this phrase.</TD>\n");
		pwOut.println("</TR>\n");
		
		//Sort order:
		pwOut.println("<TR>\n"
			+ "<TD ALIGN=RIGHT><B>Sort order:</B>&nbsp;</TD>\n"
			+ "<TD ALIGN=LEFT>" 
			+ "<INPUT NAME=\"" + SMTableproposalphrases.isortorder
			+ "\" VALUE=\"" + Integer.toString(iSortOrder) + "\">" 
			+ "</TD>\n"
			+ "<TD ALIGN=LEFT>'Sorting' number which will determine where this phrase appears in the list.</TD>\n"
			+ "</TR>\n"
		);
		
		pwOut.println("</TABLE><BR><P><INPUT TYPE=SUBMIT NAME='SubmitEdit' VALUE='Update " + sObjectName + "' STYLE='height: 0.24in'>"
				+ "&nbsp;&nbsp;<INPUT TYPE=SUBMIT NAME='DeleteEdit' VALUE='Delete " + sObjectName + "' onclick=\"alert('Are you sure?');\" STYLE='height: 0.24in'>"
				+ "</P></FORM>");
	}

	private boolean Delete_Record(
			String sProposalPhraseID,
			PrintWriter pwOut,
			String sDBID){

		Connection conn = clsDatabaseFunctions.getConnection(
				getServletContext(), 
				sDBID, 
				"MySQL", 
				SMUtilities.getFullClassName(this.toString() + ".Delete_Record")
		);

		if (conn == null){
			pwOut.println("Error getting connection to delete record.");
			//System.out.println("Error getting connection to delete record.");
			return false;
		}

		String SQL = "DELETE FROM " + SMTableproposalphrases.TableName + " WHERE ("
		+ SMTableproposalphrases.sid + "=" + sProposalPhraseID + ""
		+ ")"
		;
		try {
			Statement stmt = conn.createStatement();
			stmt.execute(SQL);
		}catch (SQLException ex){
			pwOut.println("Error deleting proposal phrase with SQL: " + SQL 
					+ " - " + ex.getMessage());
			clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1547080644]");
			return false;
		}		

		clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1547080645]");

		return true;
	}

	public void doGet(HttpServletRequest request,
			HttpServletResponse response)
	throws ServletException, IOException {

		doPost(request, response);
	}
}
