package smcontrolpanel;

import SMDataDefinition.SMTableproposals;
import SMDataDefinition.SMTablesalesperson;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsManageRequestParameters;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

public class SMEditSalespersonSigAction extends HttpServlet{
	
	private static final long serialVersionUID = 1L;
	public static final String SUBMIT_BUTTON_NAME = "SUBMIT";
	public static final String SUBMIT_BUTTON_VALUE = "Accept and save signature";
	public static final String SIGNATURE_FIELD_NAME = "SIGNATUREOUTPUT";
	private static String sObjectName = "Salesperson Signature";
	private static String sCalledClassName = "smcontrolpanel.SMEditSalespersonSigAction";
	@Override
	public void doPost(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
	
		PrintWriter out = response.getWriter();
		
	    //Get the session info:
	    HttpSession CurrentSession = request.getSession(true);
	    String sDBID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_DATABASE_ID);
	    String sCompanyName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_COMPANYNAME);
	    if (!SMAuthenticate.authenticateSMCPCredentials(request, response, getServletContext(), SMSystemFunctions.SMEditSalespersonSignatures)){
	    	return;
	    }
		String sEditCode = clsManageRequestParameters.get_Request_Parameter(SMTablesalesperson.sSalespersonCode, request);
		
	    String title = "Updating " + sObjectName + "'" + sEditCode + "'";
	    String subtitle = "";
	    out.println(getHeaderString(
				title, 
				subtitle, 
				SMUtilities.getInitBackGroundColor(getServletContext(), sDBID), 
				SMUtilities.DEFAULT_FONT_FAMILY, 
				sCompanyName));
		
	    out.println("<BR><A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMUserLogin?" 
				+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
				+ "\">Return to user login</A><BR><BR>");
	    try {
			updateSignature(sEditCode, (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERNAME), request, sDBID);
		} catch (Exception e) {
			out.println("<BR><FONT COLOR=RED><B>Warning: " + e.getMessage() + "</B></FONT>");
		    out.println("</BODY></HTML>");
		    return;
		}
	    
		out.println("<BR><B>Successfully updated signature for salesperson '" + sEditCode + "'.</B></FONT>");
	    out.println(
			"\n"
			+ "<div role=main>\n"
			+ "<form method=post action=\"" + sCalledClassName + "\" class=sigPad>\n"
			+ "<ul class=sigNav>\n"
			+ "</ul> <div class=\"sig sigWrapper\">\n"
			+ "<div class=typed></div>\n"
			+ "<canvas class=pad width=" + SMTableproposals.SIGNATURE_CANVAS_WIDTH 
				+ " height=" + SMTableproposals.SIGNATURE_CANVAS_HEIGHT + " style=\"border:1px solid  #000000;\" ></canvas>\n"
			+ "<input type=hidden name='" + SIGNATURE_FIELD_NAME + "' class=output>\n"
			+ "</div>"
			+ "</form>\n\n"
    	);
	    //Scripts for signature:
		String sSignaturePadOptions = 
				"drawOnly:true,"
				+ " displayOnly:true,"
				+ " errorMessageDraw: \"\","
				+ " lineTop:" + SMTableproposals.SIGNATURE_TOP + ","
				+ " lineColour:\"" + SMTableproposals.SIGNATURE_LINE_COLOUR + "\"" //makes the line transparent
			;
	    out.println("<BR><script src=\"scripts/jquery-signaturepad-min.js\"></script> <script>");
	    out.println("$(document).ready(function () {");
	    out.println("    $('.sigPad').signaturePad({" + sSignaturePadOptions + "}).regenerate(" 
	    	+ (String) request.getParameter(SMEditSalespersonSigEdit.SIGNATURE_FIELD_NAME) + ");");
	    out.println("});");
	    out.println("</script>");
	    out.println("<script src=\"scripts/json2.min.js\"></script> </div>");
	    out.println("</BODY></HTML>");
	    return;
	}
	private void updateSignature(String sSalesCode, String sUser, HttpServletRequest req, String sDBID) throws Exception{
		
		String sJSONSignature = clsManageRequestParameters.get_Request_Parameter(SMEditSalespersonSigEdit.SIGNATURE_FIELD_NAME, req);
	    String SQL = "UPDATE"
	    	+ " " + SMTablesalesperson.TableName
	    	+ " SET " + SMTablesalesperson.mSignature + " = '"
	    	+ clsDatabaseFunctions.FormatSQLStatement(sJSONSignature) + "'"
	    	+ " WHERE ("
	    		+ SMTablesalesperson.sSalespersonCode + " = '" + sSalesCode + "'"
	    	+ ")"
	    	;
	    try {
			clsDatabaseFunctions.executeSQL(
				SQL, 
				getServletContext(), 
				sDBID, 
				"MySQL", 
				this.toString() + ".updateSignature - user: " + sUser
			);
		} catch (SQLException e) {
			throw new Exception("<BR><FONT COLOR=RED><B>Warning: could not update signature for salesperson '" + sSalesCode + "' with SQL: "
				+ SQL + " - " + e.getMessage() + ".</B></FONT>");
		}
	}
	private String getHeaderString(
			String title, 
			String subtitle, 
			String sbackgroundcolor, 
			String sfontfamily, 
			String scompanyname){
		String s = SMUtilities.DOCTYPE
		+ "<HTML>"
		+ "<HEAD>";
		s += "<TITLE>" + subtitle + "</TITLE>"
		+ SMUtilities.faviconLink()
		//This line should keep the font widths 'screen' wide:
		+ "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\" />"
		+ "<!--[if lt IE 9]><script src=\"scripts/flashcanvas.js\"></script><![endif]-->"
		+ "<script src=\"https://ajax.googleapis.com/ajax/libs/jquery/1.7.1/jquery.min.js\"></script>"
		+ "</HEAD>\n" 
		+ "<BODY BGCOLOR="
		+ "\"" 
		+ sbackgroundcolor
		+ "\""
		+ " style=\"font-family: " + sfontfamily + ";\""
		+ "\">"
		;
		s += "<TABLE BORDER=0>"
		+"<TR><TD VALIGN=BOTTOM><H3>" + scompanyname + ": " + title + "</H3></TD>"
		;

		if (subtitle.compareTo("") != 0){  
			s = s + "<TD VALIGN=BOTTOM><H4>&nbsp;-&nbsp;" + subtitle + "</H4></TD>";
		}

		s = s + "</TR></TABLE>";
		return s;
	}
	@Override
	public void doGet(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
		
		doPost(request, response);
	}
}
