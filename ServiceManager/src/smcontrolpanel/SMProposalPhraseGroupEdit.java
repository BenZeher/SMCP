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
import ServletUtilities.clsStringFunctions;

public class SMProposalPhraseGroupEdit extends HttpServlet {

	private static final long serialVersionUID = 1L;
	private static String sObjectName = "Proposal Phrase Group";
	private static String sCalledClassName = "SMProposalPhraseGroupAction";
	private String sDBID = "";
	private String sCompanyName = "";
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
		sDBID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_DATABASE_ID);
		sCompanyName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_COMPANYNAME);
		
		String sProposalPhraseGroupID = clsStringFunctions.filter(request.getParameter(SMTableproposalphrasegroups.sid));

		String title = "";
		String subtitle = "";

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
				Edit_Record(sProposalPhraseGroupID, out, sDBID, false);
			}
		}
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
		if(request.getParameter("SubmitAdd") != null){

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
			String sConf,
			boolean bAddNew){

		pwOut.println("<FORM NAME='MAINFORM' ACTION='" 
				+ SMUtilities.getURLLinkBase(getServletContext()) 
				+ "smcontrolpanel." + sCalledClassName + "' METHOD='POST'>");
		pwOut.println("<INPUT TYPE=HIDDEN NAME='" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "' VALUE='" + sDBID + "'>");
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
					System.out.println("SQL = " + sSQL);
				}
				ResultSet rs = clsDatabaseFunctions.openResultSet(sSQL, getServletContext(), sConf);

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

		//Phrase name:
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
		pwOut.println("</TABLE><BR><P><INPUT TYPE=SUBMIT NAME='SubmitEdit' VALUE='Update " + sObjectName 
				+ "' STYLE='height: 0.24in'></P></FORM>");
	}

	private boolean Delete_Record(
			String sProposalPhraseGroupID,
			PrintWriter pwOut,
			String sConf){

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
				clsDatabaseFunctions.freeConnection(getServletContext(), conn);
				return false;
			}
			rs.close();
		} catch (SQLException e) {
			pwOut.println("<FONT COLOR=RED>Error checking for used proposal phrase groups with SQL: " + SQL 
					+ " - " + e.getMessage() + "</FONT>");
			clsDatabaseFunctions.freeConnection(getServletContext(), conn);
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
			clsDatabaseFunctions.freeConnection(getServletContext(), conn);
			return false;
		}		
		clsDatabaseFunctions.freeConnection(getServletContext(), conn);
		return true;
	}

	public void doGet(HttpServletRequest request,
			HttpServletResponse response)
	throws ServletException, IOException {
		doPost(request, response);
	}
}
