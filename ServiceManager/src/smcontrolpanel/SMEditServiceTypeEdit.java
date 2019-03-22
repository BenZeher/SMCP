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
import ServletUtilities.clsManageRequestParameters;
import ServletUtilities.clsStringFunctions;

public class SMEditServiceTypeEdit extends HttpServlet {

	private static final long serialVersionUID = 1L;
	private static final String sObjectName = "Service Type";
	private static final String sCalledClassName = "smcontrolpanel.SMEditServiceTypeAction";
	private static final String sCallingClassName = "smcontrolpanel.SMEditServiceTypeEdit";
	public static final String COMMAND_FLAG = "COMMANDFLAG";
	public static final String COMMAND_VALUE_SAVE = "SaveEntry";
	public static final String COMMAND_VALUE_DELETE = "DeleteEntry";
	
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
		String sWarning = clsManageRequestParameters.get_Request_Parameter(SMMasterEditAction.WARNING_PARAMETER, request);
		String sStatus = clsManageRequestParameters.get_Request_Parameter(SMMasterEditAction.STATUS_PARAMETER, request);;
		String title = "";
		String subtitle = "";
		
		String sServiceTypeID = clsStringFunctions.filter(request.getParameter(SMTableservicetypes.id));
		
		if(request.getParameter("SubmitAdd") != null 
			|| request.getParameter(SMTableservicetypes.id).compareToIgnoreCase("-1") == 0){
			//User has chosen to edit:
			title = "Add new " + sObjectName;
			subtitle = "";
			out.println(SMUtilities.SMCPTitleSubBGColor(
					title, subtitle, SMUtilities.getInitBackGroundColor(getServletContext(), sDBID), sCompanyName));

			if(sWarning.compareToIgnoreCase("") != 0) {
				out.println("<b><font color=\"red\">" + SMMasterEditAction.WARNING_PARAMETER + ": " + sWarning + "</font></b>");
			}
			if(sStatus.compareToIgnoreCase("") != 0) {
				out.println("<b>" + SMMasterEditAction.STATUS_PARAMETER + ": " + sStatus + "</b>");
			}
			//Print a link to the first page after login:
			out.println("<BR><A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMUserLogin?" 
				+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
				+ "\">Return to user login</A><BR>");
			//Print link back to seleciton screen
			out.println("<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMEditServiceTypeSelect?" 
					+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
					+ "\">Return to selection</A><BR><BR>");
			Edit_Record(sServiceTypeID, out, sDBID, true);
			out.println("</BODY></HTML>");
			return;
		}
		
		if(request.getParameter("SubmitEdit") != null
				|| request.getParameter(SMTableservicetypes.id).compareToIgnoreCase("-1") != 0){
			//User has chosen to edit:
			title = "Edit " + sObjectName;
			subtitle = "";
			out.println(SMUtilities.SMCPTitleSubBGColor(
					title, subtitle, SMUtilities.getInitBackGroundColor(getServletContext(), sDBID), sCompanyName));

			if(sWarning.compareToIgnoreCase("") != 0) {
				out.println("<b><font color=\"red\">" + SMMasterEditAction.WARNING_PARAMETER + ": " + sWarning + "</font></b>");
			}
			if(sStatus.compareToIgnoreCase("") != 0) {
				out.println("<b>" + SMMasterEditAction.STATUS_PARAMETER + ": " + sStatus + "</b>");
			}
			
			//Print a link to the first page after login:
			out.println("<BR><A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMUserLogin?" 
				+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
				+ "\">Return to user login</A><BR>");
			//Print link back to seleciton screen
			out.println("<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMEditServiceTypeSelect?" 
					+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
					+ "\">Return to selection</A><BR><BR>");
			if (sServiceTypeID == null){
				out.println("Invalid " + sObjectName + "ID. Please go back and try again.");
			}else{
				Edit_Record(sServiceTypeID, out, sDBID, false);
			}
			out.println("</BODY></HTML>");
			return;
		}
		return;
	}

	private void Edit_Record(
			String sParameter, 
			PrintWriter pwOut, 
			String sDBID,
			boolean bAddNew){

		pwOut.println("<FORM NAME=\"" + SMMasterEditEntry.MAIN_FORM_NAME + "\" ACTION='" 
				+ SMUtilities.getURLLinkBase(getServletContext()) 
				 + sCalledClassName + "' METHOD='POST'>");
		pwOut.println("<INPUT TYPE=HIDDEN NAME='" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "' VALUE='" + sDBID + "'>");
		pwOut.println("<INPUT TYPE=HIDDEN NAME=\"" + COMMAND_FLAG + "\" VALUE=\"" + "" + "\"" + " id=\"" + COMMAND_FLAG + "\"" + ">");
		pwOut.println("<INPUT TYPE=HIDDEN NAME=\"" + "CallingClass" + "\" VALUE=\"" + sCallingClassName + "\">");
		pwOut.println(getJavascript());
		
		pwOut.println("<TABLE BORDER=12 CELLSPACING=2>");
		int iID = -1;
		
		String sServiceTypeCode = "";
		String sWorkOrderTerms = "";
		String sServiceTypeName = "";
		String sWorkOrderReceiptComment = "";
		if(!bAddNew) {
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
				sServiceTypeCode = rs.getString(SMTableservicetypes.sCode);
				sWorkOrderTerms = rs.getString(SMTableservicetypes.mworkorderterms);
				sServiceTypeName = rs.getString(SMTableservicetypes.sName);
				sWorkOrderReceiptComment = rs.getString(SMTableservicetypes.mworeceiptcomment);
				rs.close();
			}catch (SQLException ex){
				pwOut.println("<BR>Error reading service type information - " + ex.getMessage());
			}
		}
		
		
		//Display fields
		String sRequired = "<font color=\"red\">*</font>";
		//ID
		pwOut.println("<INPUT TYPE=HIDDEN NAME=\"" + SMTableservicetypes.id + "\" VALUE=\"" + Integer.toString(iID) + "\">");
		
		//Code
		if(!bAddNew) {
			pwOut.println("<TR><TD ALIGN=RIGHT><B>Code" + ":&nbsp;</B></TD>");
			pwOut.println("<TD>" + sServiceTypeCode + "</TD>");
			pwOut.println("<INPUT TYPE=HIDDEN NAME=\"" + SMTableservicetypes.sCode + "\" VALUE=\"" + sServiceTypeCode + "\">");
			pwOut.println("<TD>Unique code that represents this service type. (i.e. SH0005) </TD></TR>");
		}else {
			pwOut.println("<TR><TD ALIGN=RIGHT><B>Code" + sRequired + ":&nbsp;</B></TD>");
			pwOut.println("<TD><INPUT TYPE=\"TEXT\" NAME=\"" + SMTableservicetypes.sCode + "\" VALUE=\"" + sServiceTypeCode +  "\">" + "</TD>");
			pwOut.println("<TD>Unique code that represents this service type. (i.e. SH0005)</TD></TR>");
		}
		
		//Name
		if(!bAddNew) {
			pwOut.println("<TR><TD ALIGN=RIGHT><B>Name" + ":&nbsp;</B></TD>");
			pwOut.println("<TD><INPUT TYPE=\"TEXT\" NAME=\"" + SMTableservicetypes.sName + "\" VALUE=\"" + sServiceTypeName +  "\">" + "</TD>");
			pwOut.println("<TD>Unique name that represents this service type. (i.e. Commercial Install)</TD></TR>");
		}else {
			pwOut.println("<TR><TD ALIGN=RIGHT><B>Name" + sRequired + ":&nbsp;</B></TD>");
			pwOut.println("<TD><INPUT TYPE=\"TEXT\" NAME=\"" + SMTableservicetypes.sName + "\" VALUE=\"" + sServiceTypeName +  "\">" + "</TD>");
			pwOut.println("<TD>Unique name that represents this service type. (i.e. Commercial Install)</TD></TR>");
		}

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
		
		pwOut.println("</TABLE><BR>");
		pwOut.println("<P>");
		pwOut.println("<button onclick=\"saveEntry()\" style='height: 0.24in'>" + "Update "	+ sObjectName + "</button>" + "&nbsp;");
		if(!bAddNew) {
			pwOut.println("<button onclick=\"deleteEntry()\" style='height: 0.24in'>" + "Delete "	+ sObjectName + "</button> ");
		}
		pwOut.println("</P>");
		pwOut.println("</FORM>");
	}
	
	private String getJavascript() {
		String s = "<script type='text/javascript'>\n";
		
		s += "function saveEntry(){\n"
				+ "    document.getElementById(\"" + COMMAND_FLAG + "\").value = \"" + COMMAND_VALUE_SAVE + "\";\n"
				+ "    document.forms[\"" + SMMasterEditEntry.MAIN_FORM_NAME + "\"].submit();\n"
				+ "}\n"
				;
		s += "function deleteEntry(){\n"
				+ "var bresult = confirm(\"This service type will not be deleted from historical data. Are you sure you want to delete this master record? \");\n"
				+ "if (bresult) {\n"
				+ "    document.getElementById(\"" + COMMAND_FLAG + "\").value = \"" + COMMAND_VALUE_DELETE + "\";\n"
				+ "    document.forms[\"" + SMMasterEditEntry.MAIN_FORM_NAME + "\"].submit();\n"
				+ "}\n"
		
				+ "}\n"
				;
		s +=	"</script>";
		return s;
	}
	public void doGet(HttpServletRequest request,
			HttpServletResponse response)
	throws ServletException, IOException {

		doPost(request, response);
	}
}
