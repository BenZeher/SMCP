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

import SMDataDefinition.SMTableproposalterms;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsStringFunctions;

public class SMEditProposalTermsEdit extends HttpServlet {

	private static final long serialVersionUID = 1L;
	private static String sObjectName = "Proposal Terms";
	private static String sCalledClassName = "SMEditProposalTermsAction";
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
				SMSystemFunctions.SMEditProposalTerms
		)
		){
			return;
		}
		//Get the session info:
		HttpSession CurrentSession = request.getSession(true);
		sDBID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_DATABASE_ID);
		sCompanyName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_COMPANYNAME);
		String sProposalTermID = clsStringFunctions.filter(request.getParameter(SMEditProposalTermsSelect.PROPTERM_PARAM));

		String title = "";
		String subtitle = "";

		if(request.getParameter("SubmitEdit") != null){
			//User has chosen to edit:
			title = "Edit " + sObjectName + " " + sProposalTermID;
			subtitle = "";
			out.println(SMUtilities.SMCPTitleSubBGColor(
					title, subtitle, SMUtilities.getInitBackGroundColor(getServletContext(), sDBID), sCompanyName));

			//Print a link to the first page after login:
			out.println("<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMUserLogin?" 
				+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
				+ "\">Return to user login</A><BR><BR>");
			if (sProposalTermID == null){
				out.println("Invalid " + sObjectName + "ID. Please go back and try again.");
			}else{
				Edit_Record(sProposalTermID, out, sDBID, false);
			}
		}
		if(request.getParameter("SubmitDelete") != null){
			//User has chosen to delete:
			title = "Delete " + sObjectName + ": " + sProposalTermID;
			subtitle = "";
			out.println(SMUtilities.SMCPTitleSubBGColor(title, subtitle, SMUtilities.getInitBackGroundColor(getServletContext(), sDBID), sCompanyName));

			//Print a link to the first page after login:
			out.println("<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMUserLogin?" 
				+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
				+ "\">Return to user login</A><BR><BR>");

			if (request.getParameter("ConfirmDelete") == null){
				out.println ("You must check the 'confirming' check box to delete.");
			}
			else{
				if (Delete_Record(sProposalTermID, out, sDBID) == false){
					out.println ("Error deleting proposal terms: " + sProposalTermID + ".");
				}
				else{
					out.println ("Successfully deleted proposal terms: " + sProposalTermID + ".");
				}
			}
		}
		if(request.getParameter("SubmitAdd") != null){

			String sNewCode = clsStringFunctions.filter(request.getParameter("New" + sObjectName));
			//User has chosen to add a new object:
			title = "Add " + sObjectName + ": " + sNewCode;
			subtitle = "";
			out.println(SMUtilities.SMCPTitleSubBGColor(title, subtitle, SMUtilities.getInitBackGroundColor(getServletContext(), sDBID), sCompanyName));

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
		String sTermCode = "";
		String sTermDesc = "";
		String sDefaultPaymentTerms = "";
		String sDaysToAccept = "";

		if (!bAddNew){
			try{
				//Get the record to edit:
				String sSQL = "SELECT * FROM " + SMTableproposalterms.TableName
				+ " WHERE ("
				+ "(" + SMTableproposalterms.sID + " = '" + sParameter + "')"
				+ ")"
				;
				if (bDebug){
					System.out.println("SQL = " + sSQL);
				}
				ResultSet rs = clsDatabaseFunctions.openResultSet(sSQL, getServletContext(), sConf);

				rs.next();
				iID = rs.getInt(SMTableproposalterms.sID);
				sTermCode = rs.getString(SMTableproposalterms.sProposalTermCode);
				sTermDesc = rs.getString(SMTableproposalterms.mProposalTermDesc);
				sDefaultPaymentTerms = rs.getString(SMTableproposalterms.sdefaultpaymentterms);
				sDaysToAccept = rs.getString(SMTableproposalterms.sdaystoaccept);
				rs.close();
			}catch (SQLException ex){
				pwOut.println("<BR>Error reading proposal terms information - " + ex.getMessage());
				//System.out.println("Error reading proposal terms information - " + ex.getMessage());
			}
		}else{
			sTermCode = sParameter;
		}

		//Display fields:
		pwOut.println("<INPUT TYPE=HIDDEN NAME=\"" + SMTableproposalterms.sID
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

		//Terms Code:
		pwOut.println("<TR>"
			+ "<TD ALIGN=RIGHT><B>Code:</B>&nbsp;</TD>"
			+ "<TD ALIGN=LEFT>" 
			+ clsStringFunctions.filter(sTermCode) + "<INPUT TYPE=HIDDEN NAME=\"" + SMTableproposalterms.sProposalTermCode
			+ "\" VALUE=\"" + sTermCode + "\">" 
			+ "</TD>"
			+ "<TD ALIGN=LEFT>Proposal terms code</TD>"
			+ "</TR>"
		);

		//Default payment terms:
		pwOut.println("<TR>"
				+ "<TD ALIGN=RIGHT><B>Payment terms:</B>&nbsp;</TD>"
				+ "<TD ALIGN=LEFT>" 
				+ "<INPUT TYPE=TEXT NAME=\"" + SMTableproposalterms.sdefaultpaymentterms + "\""
				+ " VALUE=\"" + sDefaultPaymentTerms.replace("\"", "&quot;") + "\""
				+ " SIZE=" + "60"
				+ " MAXLENGTH=" + Integer.toString(SMTableproposalterms.sdefaultpaymenttermsLength)
				+ ">"
				+ "</TD>"
				+ "<TD ALIGN=LEFT>Default payment terms, which can be edited on proposals</TD>"
				+ "</TR>"
			);
		
		//Days to accept:
		pwOut.println("<TR>"
				+ "<TD ALIGN=RIGHT><B>Days to accept:</B>&nbsp;</TD>"
				+ "<TD ALIGN=LEFT>" 
				+ "<INPUT TYPE=TEXT NAME=\"" + SMTableproposalterms.sdaystoaccept + "\""
				+ " VALUE=\"" + sDaysToAccept.replace("\"", "&quot;") + "\""
				+ " SIZE=" + "35"
				+ " MAXLENGTH=" + Integer.toString(SMTableproposalterms.sdaystoacceptLength)
				+ ">"
				+ "</TD>"
				+ "<TD ALIGN=LEFT>Default days to accept, which can be edited on proposals</TD>"
				+ "</TR>"
			);
		
		//Terms Description:
		pwOut.println("<TR>"
						+ "<TD ALIGN=RIGHT><B>Description:</B>&nbsp;</TD>"
						+ "<TD ALIGN=LEFT>"
							+ "<TEXTAREA NAME=\"" + SMTableproposalterms.mProposalTermDesc + "\""
							+ " rows=10"
							+ " cols=120"
							+ ">"
							+ sTermDesc
							+ "</TEXTAREA>"
						+ "</TD>"
						+ "<TD ALIGN=LEFT>Proposal terms description.</TD>"
					+"</TR>");
		
		pwOut.println("</TABLE><BR><P><INPUT TYPE=SUBMIT NAME='SubmitEdit' VALUE='Update " + sObjectName 
				+ "' STYLE='height: 0.24in'></P></FORM>");
	}

	private boolean Delete_Record(
			String sProposalTermID,
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

		String SQL = "DELETE FROM " + SMTableproposalterms.TableName
		+ " WHERE ("
		+ SMTableproposalterms.sID + " = " + sProposalTermID
		+ ")"
		;
		try {
			Statement stmt = conn.createStatement();
			stmt.execute(SQL);
		}catch (SQLException ex){
			pwOut.println("Error deleting proposal terms with SQL: " + SQL 
					+ " - " + ex.getMessage());
			//System.out.println("Error deleting proposal terms with SQL: " + SQL 
			//		+ " - " + ex.getMessage());
			clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1547080533]");
			return false;
		}		

		clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1547080534]");

		return true;
	}

	public void doGet(HttpServletRequest request,
			HttpServletResponse response)
	throws ServletException, IOException {

		doPost(request, response);
	}
}
