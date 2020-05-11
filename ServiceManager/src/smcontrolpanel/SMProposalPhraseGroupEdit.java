package smcontrolpanel;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import SMDataDefinition.SMTableproposalphrasegroups;
import SMDataDefinition.SMTableproposalphrases;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsManageRequestParameters;
import ServletUtilities.clsServletUtilities;
import ServletUtilities.clsStringFunctions;

public class SMProposalPhraseGroupEdit extends HttpServlet {

	private static final long serialVersionUID = 1L;
	private static final String sObjectName = "Proposal Phrase Group";
	private static final String sCalledClassName = "SMProposalPhraseGroupAction";
	public static final String COMMAND_FLAG = "COMMANDFLAG";
	public static final String UPDATE_COMMAND_VALUE = "UPDATEPHRASEGROUP";
	public static final String UPDATE_BUTTON_LABEL = "Update Proposal Phrase Group";
	public static final String ADD_NEW_PHRASE_LABEL = "Add New Proposal Phrase";
	public static final String SORT_LINE_COMMAND_VALUE = "SORTPHRASEORDER";
	public static final String PROPOSAL_PHRASE_BACKGROUND_HOVER = "#EBEBEB";
	
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
				SMSystemFunctions.SMEditProposalPhraseGroups
		)
		){
			return;
		}
		//Get the session info:
		HttpSession CurrentSession = request.getSession(true);
		String sDBID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_DATABASE_ID);
		String sCompanyName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_COMPANYNAME);
		
		String sProposalPhraseGroupID = clsStringFunctions.filter(request.getParameter(SMTableproposalphrasegroups.sid));

		String title = "";
		String subtitle = "";


		//If this is a request to edit
		if(request.getParameter("SubmitEdit") != null){
			//User has chosen to edit:
			title = "Edit " + sObjectName + " " + sProposalPhraseGroupID;
			subtitle = "";
			out.println(SMUtilities.SMCPTitleSubBGColor(
					title, subtitle, SMUtilities.getInitBackGroundColor(getServletContext(), sDBID), sCompanyName));

			//Print a link to the first page after login:
			out.println("<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMUserLogin?" 
				+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
				+ "\">Return to user login</A><BR><BR>");
			if (sProposalPhraseGroupID == null){
				out.println("Invalid " + sObjectName + "ID. Please go back and try again.");
			}else{
				Edit_Record(sProposalPhraseGroupID, out, sDBID, false, request);
			}
		}
		
		//If this is a request to delete
		if(request.getParameter("SubmitDelete") != null){
			//User has chosen to delete:
			title = "Delete " + sObjectName + ": " + sProposalPhraseGroupID;
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
				if (Delete_Record(sProposalPhraseGroupID, out, sDBID) == false){
				}
				else{
					out.println ("Successfully deleted proposal phrase group: " + sProposalPhraseGroupID + ".");
				}
			}
		}
		
		//If this is a request to Add
		if(request.getParameter("SubmitAdd") != null){

			String sNewCode = clsStringFunctions.filter(request.getParameter("New" + sObjectName));
			
			if (sNewCode == ""){
				String sRedirectString = 
						"" + SMUtilities.getURLLinkBase(getServletContext()) + "" + "smcontrolpanel.SMProposalPhraseGroupSelect"
						+ "?" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
						+ "&" + "Warning=" + clsServletUtilities.URLEncode(
								"You chose to add a new " + sObjectName + ", but you did not enter a new " + sObjectName + " to add.");	
				response.sendRedirect(sRedirectString);
				return;

			}else {
				String sRedirectString = 
						"" + SMUtilities.getURLLinkBase(getServletContext()) + "" + "smcontrolpanel.SMProposalPhraseGroupAction"
						+ "?" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
						+ "&" + SMTableproposalphrasegroups.sgroupname + "=" + clsServletUtilities.URLEncode(sNewCode);	
				response.sendRedirect(sRedirectString);
				return;
			}	
		}
		out.println("</BODY></HTML>");
	}


	private void Edit_Record(
			String sParameter, 
			PrintWriter pwOut, 
			String sDBID,
			boolean bAddNew,
			HttpServletRequest request){
		
		String sWarning = clsManageRequestParameters.get_Request_Parameter("Warning", request);
		 if(sWarning.compareToIgnoreCase("") != 0) {
			 pwOut.println("<B><FONT COLOR=RED>WARNING: " + sWarning + "</FONT></B>");
		 }
		 String sStatus = clsManageRequestParameters.get_Request_Parameter("Status", request);
		 if(sStatus.compareToIgnoreCase("") != 0) {
			 pwOut.println("<B>STATUS: " + sStatus + "</B>");
		 }
		
		//Add Javacript
		pwOut.println(clsServletUtilities.getJQueryIncludeString());
		pwOut.println(clsServletUtilities.getJQueryUIIncludeString());
		pwOut.println(sCommandScripts(getServletContext(), sDBID));
		pwOut.println(getStyles());
		
		pwOut.println("<FORM NAME='MAINFORM' ACTION='" 
				+ SMUtilities.getURLLinkBase(getServletContext()) 
				+ "smcontrolpanel." + sCalledClassName + "' METHOD='POST'>");
		pwOut.println("<INPUT TYPE=HIDDEN NAME='" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "' VALUE='" + sDBID + "'>");
		pwOut.println("<INPUT TYPE=HIDDEN NAME=\"" + COMMAND_FLAG + "\" VALUE=\"" + "" + "\"" + "id=\"" + COMMAND_FLAG + "\"" + "\">");
		pwOut.println("<TABLE BORDER=12 CELLSPACING=2>");

		int iID = -1;
		String sProposalPhraseGroupName = "";

		if (!bAddNew){
			try{
				//Get the record to edit:
				String sSQL = "SELECT * FROM " + SMTableproposalphrasegroups.TableName
				+ " WHERE ("
				+ "(" + SMTableproposalphrasegroups.sid + " = '" + sParameter + "')"
				+ ")"
				;
				if (bDebug){
					System.out.println("[1579274802] SQL = " + sSQL);
				}
				ResultSet rs = clsDatabaseFunctions.openResultSet(sSQL, getServletContext(), sDBID);

				rs.next();
				iID = rs.getInt(SMTableproposalphrasegroups.sid);
				sProposalPhraseGroupName = rs.getString(SMTableproposalphrasegroups.sgroupname);
				rs.close();
			}catch (SQLException ex){
				pwOut.println("<BR>Error reading proposal phrase information - " + ex.getMessage());
			}
		}else{
			sProposalPhraseGroupName = sParameter;
		}

		//Display fields:
		pwOut.println("<INPUT TYPE=HIDDEN NAME=\"" + SMTableproposalphrasegroups.sid
				+ "\" VALUE=\"" + Integer.toString(iID) + "\">");
		//ID:
		pwOut.println("<TR><TD ALIGN=RIGHT><B>ID:</B></TD>"
				+ "<TD>");
		if (iID < 0){
			pwOut.println("New");
		}else{
			pwOut.println(iID);
		}
		pwOut.println("</TD>"
				+ "<TD>&nbsp;</TD></TR>"
		);

		//Phrase group name:
		pwOut.println("<TR>"
			+ "<TD ALIGN=RIGHT><B>Group Name:</B>&nbsp;</TD>"
			+ "<TD ALIGN=LEFT>" 
			+ "<INPUT NAME=\"" + SMTableproposalphrasegroups.sgroupname
			+ "\" VALUE=\"" + sProposalPhraseGroupName + "\" MAXLENGTH=" + Integer.toString(SMTableproposalphrasegroups.sgroupnameLength)
			+ " SIZE=40" + ">" 
			+ "</TD>"
			+ "<TD ALIGN=LEFT>The name of the proposal phrase group</TD>"
			+ "</TR>"
		);
		
	   //Display all the phrases in the group
		try{
			String sSQL = "SELECT * FROM " + SMTableproposalphrases.TableName
			+ " WHERE ("
			+ "(" + SMTableproposalphrases.iphrasegroupid + " = " + iID + ")"
			+ ")"
			+ " ORDER BY " + SMTableproposalphrases.isortorder
			;

			ResultSet rsPhrase = clsDatabaseFunctions.openResultSet(sSQL, getServletContext(), sDBID);

			pwOut.println("<TR><TD COLSPAN=3>");
			pwOut.println("<B>Proposal Phrases</B>");
			pwOut.println("</TD></TR>");
			pwOut.println("<tbody id=\"sortable\" style=\"overflow-y: auto;\">");
			while(rsPhrase.next()){
				pwOut.println("<TR><TD COLSPAN=3><span class=\"handle\">"
						+ "<span class=\"ui-icon ui-icon-arrowthick-2-n-s\">" + "</span>" + "</span>"
						+ "");
				pwOut.println("<span class=\"clicktoeditproposalphrase\" id=\"" + rsPhrase.getString(SMTableproposalphrases.sid) + "\">" 
						        + rsPhrase.getString(SMTableproposalphrases.sproposalphrasename) + "</span>");
				pwOut.println("</TD>");
				pwOut.println("<INPUT TYPE=\"HIDDEN\" NAME=\"PROPOSALID" + rsPhrase.getString(SMTableproposalphrases.sid)
				              + "\" VALUE=\"" + rsPhrase.getString(SMTableproposalphrases.isortorder) + "\" >");
				pwOut.println("</TR>");
			}
			pwOut.println("</tbody>");
			rsPhrase.close();
		}catch (SQLException ex){
			System.out.println("<BR>Error reading proposal phrase information - " + ex.getMessage());
		}
		
		pwOut.println("</TABLE><BR>");
		
		pwOut.println("<P>" + createUpdateButton() 
							+ "&nbsp;&nbsp;" 
							+ createAddNewPhraseButton() 
				 + "</P>");
		pwOut.println("</FORM>");
	}


	private boolean Delete_Record(
			String sProposalPhraseGroupID,
			PrintWriter pwOut,
			String sDBID){

		Connection conn = clsDatabaseFunctions.getConnection(
				getServletContext(), 
				sDBID, 
				"MySQL", 
				SMUtilities.getFullClassName(this.toString() 
				+ ".Delete_Record"
				)
		);

		if (conn == null){
			pwOut.println("<FONT COLOR=RED>Error getting connection to delete record.</FONT>");
			//System.out.println("Error getting connection to delete record.");
			return false;
		}

		//If there are proposal phrases using this group, we cannot delete it:
		String SQL = "SELECT " + SMTableproposalphrases.sid + " FROM " + SMTableproposalphrases.TableName
				+ " WHERE " + SMTableproposalphrases.iphrasegroupid + " = " + sProposalPhraseGroupID;
		try {
			ResultSet rs = clsDatabaseFunctions.openResultSet(SQL, conn);
			if (rs.next()){
				pwOut.println("<FONT COLOR=RED>Cannot delete this proposal phrase group, there are still proposal phrases using it.</FONT>");
				rs.close();
				clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1547080632]");
				return false;
			}
			rs.close();
		} catch (SQLException e) {
			pwOut.println("<FONT COLOR=RED>Error checking for used proposal phrase groups with SQL: " + SQL 
					+ " - " + e.getMessage() + "</FONT>");
			clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1547080633]");
			return false;
		}
		SQL = "DELETE FROM " + SMTableproposalphrasegroups.TableName
		+ " WHERE ("
		+ SMTableproposalphrasegroups.sid + " = " + sProposalPhraseGroupID
		+ ")"
		;
		try {
			Statement stmt = conn.createStatement();
			stmt.execute(SQL);
		}catch (SQLException ex){
			pwOut.println("<FONT COLOR=RED>Error deleting proposal phrase group with SQL: " + SQL 
					+ " - " + ex.getMessage() + "</FONT>");
			clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1547080634]");
			return false;
		}		
		clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1547080635]");
		return true;
	}

	private String createUpdateButton(){
		return "<button type=\"button\""
			+ " value=\"" + UPDATE_BUTTON_LABEL + "\""
			+ " name=\"" + UPDATE_BUTTON_LABEL + "\""
			+ " onClick=\"update();\">"
			+ UPDATE_BUTTON_LABEL
			+ "</button>\n"
			;
	}
	
	private String createAddNewPhraseButton(){
		return "<button type=\"button\""
			+ " value=\"" + ADD_NEW_PHRASE_LABEL + "\""
			+ " name=\"" + ADD_NEW_PHRASE_LABEL + "\""
			+ " onClick=\"addnewphrase();\">"
			+ ADD_NEW_PHRASE_LABEL
			+ "</button>\n"
			;
	}
	
	private String sCommandScripts(ServletContext context,  String sDBID){
		String s = "";
		String sEditProposalPhraseLinkBase = SMUtilities.getURLLinkBase(context) 
				+ "smcontrolpanel.SMProposalPhrasesEdit"; 
		
		s += "<NOSCRIPT>\n"
			+ "    <font color=red>\n"
			+ "    <H3>This page requires that JavaScript be enabled to function properly</H3>\n"
			+ "    </font>\n"
			+ "</NOSCRIPT>\n"
		;

		s += "<script type='text/javascript'>\n";
		
		//Create drag sort line items. 
		s += "$(document).ready(\n"
				+ "   function() {\n"
				+ "     $('.handle').css('cursor', 'pointer');\n"
				+ "		$(\"tbody#sortable\").sortable({\n"
				+ "		update: function(event, ui) {  \n" 
				+ "         $('tbody#sortable tr').each(function() {\n"  
				+ "         	$(this).children('input').val($(this).index() + 1);\n" 
				+ "        	});\n"  
				+ "          document.getElementById(\"" + COMMAND_FLAG + "\").value = \"" + SORT_LINE_COMMAND_VALUE + "\";\n"
				+ "          document.forms[\"MAINFORM\"].submit();\n"
				+ "     },\n" 
				
				+ "		handle: 'td:first .handle',\n"
				+ "		cursor: 'move'\n,"
				+ "		tolerance: 'pointer',\n"
				+ "		containment: 'parent'\n"				
				+ "}); \n"
				+ " if(($(\"tbody#sortable\").children('tr').length <= 1)){\n"
				+ "    $( \"tbody#sortable\").sortable( \"disable\" );\n"
				+ "	}\n"
				+ "";
				//Click to edit proposal phrase
				s +=  "$('span.clicktoeditproposalphrase').click(function(){\n"
						+ "window.open(\"" + sEditProposalPhraseLinkBase + "?" + SMTableproposalphrases.sproposalphrasename + "=\" + $(this).attr('id') + \"" 
							+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
							+ "&SubmitEdit=Y"+ "\");\n"
						+"});\n\n";	
				s += "});\n"
				;
		
		//Update:
		s += "function update(){\n"
				+ "    document.getElementById(\"" + COMMAND_FLAG + "\").value = \"" 
					+ UPDATE_COMMAND_VALUE + "\";\n"
				+ "    document.forms[\"MAINFORM\"].submit();\n"
				+ "}\n"
				;
		
		//Add new:
		s += "function addnewphrase(){\n"
				+ "window.open(\"" + sEditProposalPhraseLinkBase + "?" + SMTableproposalphrases.sproposalphrasename + "=\" + -1 + \"" 
				+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
				+ "&SubmitAdd=Y"+ "\");\n"
				+ "}\n"
						;
		s += "</script>";
	
		return s;
	}
	
	private String getStyles() {
		String s = "";
		s += "<style>";
		
		s += ".ui-sortable-helper {\n" 
			 + " display: table;\n"
			 +"}\n";  
		
		s += "span.clicktoeditproposalphrase:hover {"
			 + " background-color: " + PROPOSAL_PHRASE_BACKGROUND_HOVER + ";"
			 + "}"	
			 ;	
		s += "</style>\n\n";
		return s;
	}
	public void doGet(HttpServletRequest request,
			HttpServletResponse response)
	throws ServletException, IOException {
		doPost(request, response);
	}
}
