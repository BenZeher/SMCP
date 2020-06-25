package smcontrolpanel;

import SMDataDefinition.SMMasterStyleSheetDefinitions;
import SMDataDefinition.SMTableproposals;
import SMDataDefinition.SMTablesalesperson;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsStringFunctions;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import SMClasses.MySQLs;

public class SMEditSalespersonSigEdit extends HttpServlet {

	public static final String SUBMIT_BUTTON_NAME = "SUBMIT";
	public static final String SUBMIT_BUTTON_VALUE = "Accept and save signature";
	public static final String SIGNATURE_FIELD_NAME = "SIGNATUREOUTPUT";
	private static final long serialVersionUID = 1L;
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
	    String sUserFullName = (String)CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERFIRSTNAME) + " "
	    				+ (String)CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERLASTNAME);
	    
	    if (!SMAuthenticate.authenticateSMCPCredentials(request, response, getServletContext(), SMSystemFunctions.SMEditSalespersonSignatures)){
	    	return;
	    }
	    String sEditCode = clsStringFunctions.filter(request.getParameter(SMTablesalesperson.sSalespersonCode));
		String title = "";
		String subtitle = "";
		
    	//User has chosen to edit:
		title = "Edit signature for salesperson: <B>" + sEditCode + "</B>";
	    subtitle = "";
	    
	    out.println(getHeaderString(
				title, 
				subtitle, 
				SMUtilities.getInitBackGroundColor(getServletContext(), sDBID), 
				SMUtilities.DEFAULT_FONT_FAMILY, 
				sCompanyName));
	    
	    //Print a link to the first page after login:
	    out.println("<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMUserLogin?" 
			+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
			+ "\">Return to user login</A><BR><BR>");
		
	    try {
			Edit_Record(sEditCode, out, sDBID, false);
		} catch (Exception e1) {
			out.println("<BR><FONT=RED><B>" + e1.getMessage() + "</B></FONT>");
		}
	    String sJSONSavedSignature = "";
	    try {
			sJSONSavedSignature = getStoredSignature(
				sEditCode, sDBID, (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERID),sUserFullName);
		} catch (Exception e) {
			out.println("<BR><FONT=RED><B>Could not display current signature: " + e.getMessage() + "</B></FONT>");
		}
	    
	    //Scripts for signature:
		String sSignaturePadOptions = "drawOnly:true,"
			+ " errorMessageDraw: \"\","
			+ " lineTop:" + SMTableproposals.SIGNATURE_TOP + ","
			+ " lineColour:\"" + SMMasterStyleSheetDefinitions.BACKGROUND_WHITE + "\"" //makes the line transparent
		;
	    
	    out.println("<BR><script src=\"scripts/jquery-signaturepad-min.js\"></script> <script>");
	    out.println("$(document).ready(function () {");
	    
	    //out.println("    $('.sigPad').signaturePad({drawOnly:true, errorMessageDraw: \"\", lineTop:" + SMTablesalesperson.SIG_LINE_TOP + "});");
	    out.println("    $('.sigPad').signaturePad({" + sSignaturePadOptions + "});");
	    if (sJSONSavedSignature.compareToIgnoreCase("") != 0){
		    //out.println("    $('.sigPad').signaturePad({drawOnly:true, errorMessageDraw: \"\",lineTop:" + SMTablesalesperson.SIG_LINE_TOP + "}).regenerate(" 
			//    + sJSONSavedSignature + ");");
	    	out.println("    $('.sigPad').signaturePad({" + sSignaturePadOptions + "}).regenerate("	+ sJSONSavedSignature + ");");
	    }
	    out.println("});");
	    out.println("</script>");
	    out.println("<script src=\"scripts/json2.min.js\"></script> </div>");
		out.println("</BODY></HTML>");
	}
	private String getStoredSignature(String sSalesCode, String sDBID, String sUserID, String sUserFullName) throws Exception{
		String SQL = "SELECT"
			+ " " + SMTablesalesperson.mSignature
			+ " FROM " + SMTablesalesperson.TableName
			+ " WHERE ("
				 + SMTablesalesperson.sSalespersonCode + " = '" + sSalesCode + "'"
			+ ")"
		;
		String sJSONString = "";
		try {
			ResultSet rs = clsDatabaseFunctions.openResultSet(
				SQL, 
				getServletContext(), 
				sDBID, 
				"MySQL", 
				this.toString() + ".getStoredSignature - user: " + sUserID
				+ " - "
				+ sUserFullName
			);
			if (rs.first()){
				sJSONString = rs.getString(SMTablesalesperson.mSignature);
			}
			rs.close();
		} catch (Exception e) {
			throw new Exception("Error reading signature with SQL: " + SQL + " - " + e.getMessage() + ".");
		}
		return sJSONString;
	}
	private void Edit_Record(
			String sCode, 
			PrintWriter pwOut, 
			String sDBID,
			boolean bAddNew) throws Exception{
		
	    String sOutPut = "";
	    String sSQL = "";
		try{
			//Get the record to edit:
	        sSQL = MySQLs.Get_Salesperson_By_Salescode(sCode);
	        ResultSet rs = clsDatabaseFunctions.openResultSet(sSQL, getServletContext(), sDBID);
        	
	        rs.next();
	        rs.close();
		}catch (SQLException ex){
			throw new Exception("Error [1396900892] getting salesperson record with SQL: " + sSQL + " - " + ex.getMessage());
		}
		
		//Add signature here:
        sOutPut +=
        	"\n"
        	+ "<div role=main>\n"
        	+ "<form method=post action=\"" + sCalledClassName + "\" class=sigPad>\n"
        	+ "<INPUT TYPE=HIDDEN NAME='" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "' VALUE='" + sDBID + "'>"
        	+ "<INPUT TYPE=HIDDEN NAME='" + SMTablesalesperson.sSalespersonCode + "' VALUE='" + sCode + "'>"
        	+ "<p class=drawItDesc>Draw a new signature below</p>\n"
        	+ "<ul class=sigNav>\n"
        	+ "<li class=clearButton><a href=\"#clear\">Clear signature</a></li>\n"
        	+ "</ul> <div class=\"sig sigWrapper\">\n"
        	+ "<div class=typed></div>\n"
        	+ "<canvas class=pad width=" + SMTableproposals.SIGNATURE_CANVAS_WIDTH 
        		+ " height=" + SMTableproposals.SIGNATURE_CANVAS_HEIGHT + " style=\"border:1px solid  " + SMMasterStyleSheetDefinitions.BACKGROUND_BLACK + ";\" ></canvas>\n"
        	+ "<input type=hidden name='" + SIGNATURE_FIELD_NAME + "' class=output>\n"
        	+ "</div>"
        	+ "<P><INPUT TYPE=SUBMIT NAME='" + SUBMIT_BUTTON_NAME 
        		+ "' VALUE='" + SUBMIT_BUTTON_VALUE + "' STYLE='height: 0.24in'></P>"
        	+ "</form>\n\n"
        ;
		pwOut.println(sOutPut);
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
	public void doGet(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
		
		doPost(request, response);
	}
}
