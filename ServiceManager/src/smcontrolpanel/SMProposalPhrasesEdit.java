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

public class SMProposalPhrasesEdit extends HttpServlet {

	private static final long serialVersionUID = 1L;
	private static String sObjectName = "Proposal Phrase";
	private static String sCalledClassName = "SMProposalPhrasesAction";
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
				SMSystemFunctions.SMEditProposalPhrases
		)
		){
			return;
		}
		//Get the session info:
		HttpSession CurrentSession = request.getSession(true);
		sDBID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_DATABASE_ID);
		sCompanyName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_COMPANYNAME);
		String sProposalPhraseID = clsStringFunctions.filter(request.getParameter(SMTableproposalphrases.sproposalphrasename));

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
					System.out.println("SQL = " + sSQL);
				}
				ResultSet rs = clsDatabaseFunctions.openResultSet(sSQL, getServletContext(), sConf);

				rs.next();
				iID = rs.getInt(SMTableproposalphrases.sid);
				sProposalPhraseName = rs.getString(SMTableproposalphrases.sproposalphrasename);
				sProposalPhrase = rs.getString(SMTableproposalphrases.mproposalphrase);
				iProposalPhraseGroupID = rs.getInt(SMTableproposalphrases.iphrasegroupid);
				iSortOrder = rs.getInt(SMTableproposalphrases.isortorder);
				rs.close();
			}catch (SQLException ex){
				pwOut.println("<BR>Error reading proposal phrase information - " + ex.getMessage());
			}
		}else{
			sProposalPhraseName = sParameter;
		}

		//Display fields:
		pwOut.println("<INPUT TYPE=HIDDEN NAME=\"" + SMTableproposalphrases.sid
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
			+ "<TD ALIGN=RIGHT><B>Name:</B>&nbsp;</TD>"
			+ "<TD ALIGN=LEFT>" 
			+ clsStringFunctions.filter(sProposalPhraseName) + "<INPUT TYPE=HIDDEN NAME=\"" + SMTableproposalphrases.sproposalphrasename
			+ "\" VALUE=\"" + sProposalPhraseName + "\">" 
			+ "</TD>"
			+ "<TD ALIGN=LEFT>Proposal phrase name</TD>"
			+ "</TR>"
		);

		//Proposal phrase:
		pwOut.println("<TR>"
			+ "<TD ALIGN=RIGHT><B>Phrase:</B>&nbsp;</TD>"
			+ "<TD ALIGN=LEFT>"
				+ "<TEXTAREA NAME=\"" + SMTableproposalphrases.mproposalphrase + "\""
				+ " rows=10"
				+ " cols=120"
				+ ">"
				+ sProposalPhrase
				+ "</TEXTAREA>"
			+ "</TD>"
			+ "<TD ALIGN=LEFT>Full phrase that will be inserted into proposals</TD>"
		+"</TR>");
		
		//Proposal phrase group:
		pwOut.println("<TR>"
				+ "<TD ALIGN=RIGHT><B>Phrase group:</B>&nbsp;</TD>"
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
        		pwOut.println ("<OPTION " + sSelected + " VALUE=\"" + rs.getString(SMTableproposalphrasegroups.sid) + "\">"
        			+ Integer.toString(rs.getInt(SMTableproposalphrasegroups.sid)) + " - "
        			+ rs.getString(SMTableproposalphrasegroups.sgroupname)
        		); 
        	}
        	rs.close();
	        	//End the drop down list:
        	pwOut.println ("</SELECT>");
		}catch (SQLException ex){
			pwOut.println("Error getting list of proposal phrase groups - " + ex.getMessage());
		}
		pwOut.println("</TD>");
		pwOut.println("<TD>Choose a group in which to include this phrase.</TD>");
		pwOut.println("</TR>");
		
		//Sort order:
		pwOut.println("<TR>"
			+ "<TD ALIGN=RIGHT><B>Sort order:</B>&nbsp;</TD>"
			+ "<TD ALIGN=LEFT>" 
			+ "<INPUT NAME=\"" + SMTableproposalphrases.isortorder
			+ "\" VALUE=\"" + Integer.toString(iSortOrder) + "\">" 
			+ "</TD>"
			+ "<TD ALIGN=LEFT>'Sorting' number which will determine where this phrase appears in the list.</TD>"
			+ "</TR>"
		);
		
		pwOut.println("</TABLE><BR><P><INPUT TYPE=SUBMIT NAME='SubmitEdit' VALUE='Update " + sObjectName 
				+ "' STYLE='height: 0.24in'></P></FORM>");
	}

	private boolean Delete_Record(
			String sProposalPhraseID,
			PrintWriter pwOut,
			String sConf){

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

		String SQL = "DELETE FROM " + SMTableproposalphrases.TableName
		+ " WHERE ("
		+ SMTableproposalphrases.sid + " = " + sProposalPhraseID
		+ ")"
		;
		try {
			Statement stmt = conn.createStatement();
			stmt.execute(SQL);
		}catch (SQLException ex){
			pwOut.println("Error deleting proposal phrase with SQL: " + SQL 
					+ " - " + ex.getMessage());
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
