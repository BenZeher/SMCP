package smcontrolpanel;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import SMDataDefinition.SMTableservicetypes;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsStringFunctions;

public class SMEditServiceTypeEdit extends HttpServlet {

	private static final long serialVersionUID = 1L;
	private static String sObjectName = "Service Type";
	private static String sCalledClassName = "SMEditServiceTypeAction";
	//private boolean bDebug = false;
	
	@Override
	public void doPost(HttpServletRequest request,
			HttpServletResponse response)
	throws ServletException, IOException {

		PrintWriter out = response.getWriter();
		if (!SMAuthenticate.authenticateSMCPCredentials(
				request, 
				response, 
				getServletContext(),
				SMSystemFunctions.SMEditServiceTypes
		)
		){
			return;
		}
		//Get the session info:
		HttpSession CurrentSession = request.getSession(true);
		String sDBID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_DATABASE_ID);
		String sCompanyName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_COMPANYNAME);
		String sServiceTypeID = clsStringFunctions.filter(request.getParameter(SMTableservicetypes.id));

		String title = "";
		String subtitle = "";

		if(request.getParameter("SubmitEdit") != null){
			//User has chosen to edit:
			title = "Edit " + sObjectName;
			subtitle = "";
			out.println(SMUtilities.SMCPTitleSubBGColor(
					title, subtitle, SMUtilities.getInitBackGroundColor(getServletContext(), sDBID), sCompanyName));

			//Print a link to the first page after login:
			out.println("<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMUserLogin?" 
				+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
				+ "\">Return to user login</A><BR><BR>");
			if (sServiceTypeID == null){
				out.println("Invalid " + sObjectName + "ID. Please go back and try again.");
			}else{
				Edit_Record(sServiceTypeID, out, sDBID, false);
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
		pwOut.println("<TABLE BORDER=12 CELLSPACING=2>");

		int iID = -1;
		String sWorkOrderTerms = "";
		String sServiceTypeDescription = "";
		String sWorkOrderReceiptComment = "";
		try{
			//Get the record to edit:
			String sSQL = "SELECT * FROM " + SMTableservicetypes.TableName
			+ " WHERE ("
			+ "(" + SMTableservicetypes.id + " = " + sParameter + ")"
			+ ")"
			;
			ResultSet rs = clsDatabaseFunctions.openResultSet(sSQL, getServletContext(), sDBID);

			rs.next();
			iID = rs.getInt(SMTableservicetypes.id);
			sWorkOrderTerms = rs.getString(SMTableservicetypes.mworkorderterms);
			sServiceTypeDescription = SMTableservicetypes.getServiceTypeLabelFromTypeID(Integer.toString(rs.getInt(SMTableservicetypes.iTypeID)));
			sWorkOrderReceiptComment = rs.getString(SMTableservicetypes.mworeceiptcomment);
			rs.close();
		}catch (SQLException ex){
			pwOut.println("<BR>Error reading service type information - " + ex.getMessage());
		}

		//Display fields:
		pwOut.println("<INPUT TYPE=HIDDEN NAME=\"" + SMTableservicetypes.id
				+ "\" VALUE=\"" + Integer.toString(iID) + "\">");
		//ID:
		pwOut.println("<TR><TD ALIGN=RIGHT><B>Type:</B></TD>"
				+ "<TD>");
		pwOut.println(sServiceTypeDescription);
		pwOut.println("</TD>"
				+ "<TD>&nbsp;</TD></TR>"
		);

		//Work order terms:
		pwOut.println("<TR>"
			+ "<TD ALIGN=RIGHT VALIGN=TOP><B>Work order terms and conditions:</B>&nbsp;</TD>"
			+ "<TD ALIGN=LEFT>"
				+ "<TEXTAREA NAME=\"" + SMTableservicetypes.mworkorderterms + "\""
				+ " rows=15"
				+ " cols=100"
				+ ">"
				+ sWorkOrderTerms
				+ "</TEXTAREA>"
			+ "</TD>"
			+ "<TD ALIGN=LEFT VALIGN=TOP>Terms and conditions that will appear on work orders of this type</TD>"
		+"</TR>");
		
		//Work order receipt comment:
		pwOut.println("<TR>"
			+ "<TD ALIGN=RIGHT VALIGN=TOP><B> Work order receipt comment:</B>&nbsp;</TD>"
			+ "<TD ALIGN=LEFT>"
				+ "<TEXTAREA NAME=\"" + SMTableservicetypes.mworeceiptcomment + "\""
				+ " rows=5"
				+ " cols=100"
				+ ">"
				+ sWorkOrderReceiptComment
				+ "</TEXTAREA>"
			+ "</TD>"
			+ "<TD ALIGN=LEFT VALIGN=TOP>Comment that will appear on work orders of this service type</TD>"
		+"</TR>");
		
		pwOut.println("</TABLE><BR><P><INPUT TYPE=SUBMIT NAME='SubmitEdit' VALUE='Update " + sObjectName 
				+ "' STYLE='height: 0.24in'></P></FORM>");
	}
	public void doGet(HttpServletRequest request,
			HttpServletResponse response)
	throws ServletException, IOException {

		doPost(request, response);
	}
}
