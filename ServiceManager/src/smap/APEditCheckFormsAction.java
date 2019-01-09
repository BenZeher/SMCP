package smap;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import ConnectionPool.WebContextParameters;
import SMDataDefinition.SMTableapcheckforms;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsManageRequestParameters;
import smcontrolpanel.SMAuthenticate;
import smcontrolpanel.SMSystemFunctions;
import smcontrolpanel.SMUtilities;

public class APEditCheckFormsAction extends HttpServlet{
	
	private static final long serialVersionUID = 1L;
	private static String sObjectName = "Check Form";
	private String sDBID = "";
	private String sCompanyName = "";
	private String sUserID  = "";
	private String sUserFirstName = "";
	private String sUserLastName = "";
	//private boolean bDebugMode = true;
	@Override
	public void doPost(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
	
		PrintWriter out = response.getWriter();
		if (!SMAuthenticate.authenticateSMCPCredentials(
				request, 
				response, 
				getServletContext(),
				SMSystemFunctions.APEditCheckForms
			)
		){
			return;
		}
	    //Get the session info:
	    HttpSession CurrentSession = request.getSession(true);
	    sDBID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_DATABASE_ID);
	    sCompanyName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_COMPANYNAME);
	    sUserID = (String)CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERID);
	    sUserFirstName = (String)CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERFIRSTNAME);
	    sUserLastName = (String)CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERLASTNAME);
		long lID;
		try {
			lID = Long.parseLong(clsManageRequestParameters.get_Request_Parameter(SMTableapcheckforms.lid, request));
		} catch (NumberFormatException e1) {
			out.println("Error [1502977279] Invalid check form ID '" 
				+  clsManageRequestParameters.get_Request_Parameter(SMTableapcheckforms.lid, request) + "- click 'Back' to correct.");
			out.println("</BODY></HTML>");
			return;
		}
		String sName = request.getParameter(SMTableapcheckforms.sname).trim().replace("&quot;", "\"");
		String sDescription = request.getParameter(SMTableapcheckforms.sdescription).trim().replace("&quot;", "\"");
		String sText = request.getParameter(SMTableapcheckforms.mtext).trim().replace("&quot;", "\"");
		String sNumberOfAdviceLinesPerPage = request.getParameter(SMTableapcheckforms.inumberofadvicelinesperpage).trim().replace("&quot;", "\"");

		if (clsManageRequestParameters.get_Request_Parameter(
			APEditCheckFormsEdit.PRINT_SAMPLE_CHECKS, request).compareToIgnoreCase(APEditCheckFormsEdit.TEST_HTML_BUTTON_LABEL) == 0){
			//Call the HTML form viewer:
			String sFormViewAddress = SMUtilities.getURLLinkBase(getServletContext()) + "smap.APViewCheckForm"
				+ "?" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
				+ "&CallingClass=" + clsManageRequestParameters.get_Request_Parameter("CallingClass", request)
				+ "&" + SMTableapcheckforms.lid + "=" + request.getParameter(SMTableapcheckforms.lid)
				+ "&" + APEditCheckFormsEdit.NUMBER_OF_SAMPLE_CHECKS_TO_PRINT + "=" + clsManageRequestParameters.get_Request_Parameter(APEditCheckFormsEdit.NUMBER_OF_SAMPLE_CHECKS_TO_PRINT, request)
				+ "&" + APEditCheckFormsEdit.SAMPLE_VENDOR + "=" + clsManageRequestParameters.get_Request_Parameter(APEditCheckFormsEdit.SAMPLE_VENDOR, request)
				+ "&" + APEditCheckFormsEdit.SAMPLE_REMIT_TO + "=" + clsManageRequestParameters.get_Request_Parameter(APEditCheckFormsEdit.SAMPLE_REMIT_TO, request)
				+ "&" + APEditCheckFormsEdit.SAMPLE_NUMBER_OF_ADVICE_LINES + "=" + clsManageRequestParameters.get_Request_Parameter(APEditCheckFormsEdit.SAMPLE_NUMBER_OF_ADVICE_LINES, request)
				+ "&" + APEditCheckFormsEdit.SAMPLE_BANK_ID + "=" + clsManageRequestParameters.get_Request_Parameter(APEditCheckFormsEdit.SAMPLE_BANK_ID, request)
				+ "&" + APEditCheckFormsEdit.BUTTON_SUBMIT_ADD + "=" + clsManageRequestParameters.get_Request_Parameter(APEditCheckFormsEdit.BUTTON_SUBMIT_ADD, request)
				+ "&" + APEditCheckFormsEdit.BUTTON_SUBMIT_EDIT + "=" + clsManageRequestParameters.get_Request_Parameter(APEditCheckFormsEdit.BUTTON_SUBMIT_EDIT, request)
				+ "&" + APEditCheckFormsEdit.BUTTON_SUBMIT_DELETE + "=" + clsManageRequestParameters.get_Request_Parameter(APEditCheckFormsEdit.BUTTON_SUBMIT_DELETE, request)
			;
			redirectProcess(sFormViewAddress, response);
			return;
		}
		
		//Validate fields:
		if (sName.trim().compareToIgnoreCase("") == 0){
			out.println("Name cannot be blank - click 'Back' to correct.");
			out.println("</BODY></HTML>");
			return;
		}

		if (sText.trim().compareToIgnoreCase("") == 0){
			out.println("Text cannot be blank - click 'Back' to correct.");
			out.println("</BODY></HTML>");
			return;
		}
		
		if (sDescription.trim().compareToIgnoreCase("") == 0){
			out.println("Description cannot be blank - click 'Back' to correct.");
			out.println("</BODY></HTML>");
			return;
		}
		
		int iTest = 0;
		try {
			iTest = Integer.parseInt(sNumberOfAdviceLinesPerPage);
		} catch (NumberFormatException e1) {
			out.println("Number of advice lines per page is not valid: '" + sNumberOfAdviceLinesPerPage + "'.");
			out.println("</BODY></HTML>");
			return;
		}
		
		if (iTest < 0){
			out.println("Number of advice lines per page can't be less than zero.");
			out.println("</BODY></HTML>");
			return;
		}
		
	    String title = "Updating " + sObjectName + "'" + sName + "'";
	    String subtitle = "";
	    out.println(SMUtilities.SMCPTitleSubBGColor(title, subtitle, SMUtilities.getInitBackGroundColor(getServletContext(), sDBID), sCompanyName));
		
	    out.println("<BR><A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMUserLogin?" 
				+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
				+ "\">Return to user login</A><BR><BR>");

		//Print a link to main menu:
		out.println("<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smap.APMainMenu?" 
				+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
				+ "\">Return to Accounts Payable Main Menu</A><BR>");
		out.println("<A HREF=\"" + WebContextParameters.getdocumentationpageURL(getServletContext()) + "#" + Long.toString(SMSystemFunctions.APEditCheckForms)
				+ "\">Summary</A><BR>");
	    
	    Connection conn = clsDatabaseFunctions.getConnection(
	    		getServletContext(), 
	    		sDBID, 
	    		"MySQL", 
	    		this.toString() 
	    		+ ".doPost - user: " 
	    		+ sUserID
	    		+ " - "
	    		+ sUserFirstName
	    		+ " "
	    		+ sUserLastName
	    		);
	    if (conn == null){
	    	out.println("Error [1502977393] getting database connection");
			out.println("</BODY></HTML>");
			return;
	    }
	    
	    if (!clsDatabaseFunctions.start_data_transaction(conn)){
	    	clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1547047713]");
	    	out.println("Error [1502977394] starting database transaction");
			out.println("</BODY></HTML>");
			return;
	    }
	    
	    String sSQL = "";
	    if (lID < 0){
		    sSQL = "INSERT INTO " + SMTableapcheckforms.TableName
		    	+ " (" + SMTableapcheckforms.mtext
		    	+ ", " + SMTableapcheckforms.sdescription
		    	+ ", " + SMTableapcheckforms.sname
		    	+ ", " + SMTableapcheckforms.inumberofadvicelinesperpage
		    	+ ") VALUES ("
		    	+ " '" + clsDatabaseFunctions.FormatSQLStatement(sText) + "'"
		    	+ ", '" + clsDatabaseFunctions.FormatSQLStatement(sDescription) + "'"
		    	+ ", '" + clsDatabaseFunctions.FormatSQLStatement(sName) + "'"
		    	+ ", " + sNumberOfAdviceLinesPerPage
		    	+ ")"
		    	;
	    }else{
	    	sSQL = "UPDATE " + SMTableapcheckforms.TableName
	    		+ " SET" 
	    	    + " " + SMTableapcheckforms.mtext + " = '" + clsDatabaseFunctions.FormatSQLStatement(sText) + "'"
	    		+ ", " + SMTableapcheckforms.sdescription + " = '" + clsDatabaseFunctions.FormatSQLStatement(sDescription) + "'"
	    		+ ", " + SMTableapcheckforms.sname + " = '" + clsDatabaseFunctions.FormatSQLStatement(sName) + "'"
	    		+ ", " + SMTableapcheckforms.inumberofadvicelinesperpage + " = " + sNumberOfAdviceLinesPerPage
	    		+ " WHERE" 
	    		+ " " + SMTableapcheckforms.lid + " = " + lID 
	    		;
	    }
	    //System.out.println("[1437079668] SQL = '" + sSQL + "'");
	    try{
	    	clsDatabaseFunctions.executeSQL(sSQL, conn);
	    }catch (SQLException ex){
	    	clsDatabaseFunctions.rollback_data_transaction(conn);
	    	clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1547047714]");
	    	out.println("Error [1502977469] updating check forms with SQL: " + sSQL
	    		+ " - " + ex.getMessage());
			out.println("</BODY></HTML>");
			return;
		}
	    
	    //Now get the last insert ID:
	    if (lID < 0){
			sSQL = "SELECT LAST_INSERT_ID()";
			try {
				java.sql.ResultSet rs = clsDatabaseFunctions.openResultSet(sSQL, conn);
				if (!rs.next()){
			    	clsDatabaseFunctions.rollback_data_transaction(conn);
			    	clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1547047715]");
			    	out.println("Error [1502977493] getting last insert ID with SQL: " + sSQL);
					out.println("</BODY></HTML>");
					return;
				}else{
					lID = rs.getLong(1);
					rs.close();
				}
			} catch (SQLException e) {
		    	clsDatabaseFunctions.rollback_data_transaction(conn);
		    	clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1547047716]");
		    	out.println("Error [1502977494] getting last insert ID with SQL: " + sSQL);
				out.println("</BODY></HTML>");
				return;
			}
	    }
	    
	    if (!clsDatabaseFunctions.commit_data_transaction(conn)){
	    	clsDatabaseFunctions.rollback_data_transaction(conn);
	    	clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1547047717]");
	    	out.println("Error [1502977495] - Unable to commit data transaction.");
				out.println("</BODY></HTML>");
				return;
	    }
	    clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1547047718]");
		String sRedirectString = 
				"" + SMUtilities.getURLLinkBase(getServletContext()) + "smap.APEditCheckFormsEdit"
				+ "?" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
				+ "&" + SMTableapcheckforms.lid + "=" + Long.toString(lID)
				+ "&" + APEditCheckFormsSelect.BUTTON_SUBMIT_EDIT + "=" + "Y"
				+ "&Status=Check Form was successfully saved."
			; 
	    redirectProcess(sRedirectString, response);
	    return;
	}
	private void redirectProcess(String sRedirectString, HttpServletResponse res ){
		try {
			res.sendRedirect(sRedirectString);
		} catch (IOException e1) {
			System.out.println("Error [1502977618] in " + this.toString() + ".redirectAction - IOException error redirecting with string: "
					+ sRedirectString + " - " + e1.getMessage());
			return;
		} catch (IllegalStateException e1) {
			System.out.println("Error [1502977619] in " + this.toString() + ".redirectAction - IllegalStateException error redirecting with string: "
					+ sRedirectString + " - " + e1.getMessage());
			return;
		}
	}
	@Override
	public void doGet(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
		
		doPost(request, response);
	}
}
