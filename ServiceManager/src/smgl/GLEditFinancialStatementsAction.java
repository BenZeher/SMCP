package smgl;

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
import SMDataDefinition.SMTableglstatementforms;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsManageRequestParameters;
import smcontrolpanel.SMAuthenticate;
import smcontrolpanel.SMSystemFunctions;
import smcontrolpanel.SMUtilities;

public class GLEditFinancialStatementsAction extends HttpServlet{
	
	private static final long serialVersionUID = 1L;
	private static String sObjectName = "Financial Statement";
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
				SMSystemFunctions.GLEditFinancialStatements
			)
		){
			return;
		}
	    //Get the session info:
	    HttpSession CurrentSession = request.getSession(true);
	    String sDBID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_DATABASE_ID);
	    String sCompanyName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_COMPANYNAME);
	    String sUserID = (String)CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERID);
	    String sUserFirstName = (String)CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERFIRSTNAME);
	    String sUserLastName = (String)CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERLASTNAME);
		long lID;
		try {
			lID = Long.parseLong(clsManageRequestParameters.get_Request_Parameter(SMTableglstatementforms.lid, request));
		} catch (NumberFormatException e1) {
			out.println("Error [1534454066] Invalid financial statement form ID '" 
				+  clsManageRequestParameters.get_Request_Parameter(SMTableglstatementforms.lid, request) + "- click 'Back' to correct.");
			out.println("</BODY></HTML>");
			return;
		}
		String sName = request.getParameter(SMTableglstatementforms.sname).trim().replace("&quot;", "\"");
		String sDescription = request.getParameter(SMTableglstatementforms.sdescription).trim().replace("&quot;", "\"");
		String sText = request.getParameter(SMTableglstatementforms.mtext).trim().replace("&quot;", "\"");

		if (clsManageRequestParameters.get_Request_Parameter(
			GLEditFinancialStatementsEdit.PRINT_SAMPLE_STATEMENT, request).compareToIgnoreCase(GLEditFinancialStatementsEdit.TEST_STATEMENT_BUTTON_LABEL) == 0){
			//Call the HTML form viewer:
			String sFormViewAddress = SMUtilities.getURLLinkBase(getServletContext()) + "smgl.GLViewStatementForm"
				+ "?" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID+ "=" + sDBID
				+ "&CallingClass=" + clsManageRequestParameters.get_Request_Parameter("CallingClass", request)
				+ "&" + SMTableglstatementforms.lid + "=" + request.getParameter(SMTableglstatementforms.lid)
				+ "&" + GLEditFinancialStatementsEdit.BUTTON_SUBMIT_ADD + "=" + clsManageRequestParameters.get_Request_Parameter(GLEditFinancialStatementsEdit.BUTTON_SUBMIT_ADD, request)
				+ "&" + GLEditFinancialStatementsEdit.BUTTON_SUBMIT_EDIT + "=" + clsManageRequestParameters.get_Request_Parameter(GLEditFinancialStatementsEdit.BUTTON_SUBMIT_EDIT, request)
				+ "&" + GLEditFinancialStatementsEdit.BUTTON_SUBMIT_DELETE + "=" + clsManageRequestParameters.get_Request_Parameter(GLEditFinancialStatementsEdit.BUTTON_SUBMIT_DELETE, request)
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
		
	    String title = "Updating " + sObjectName + "'" + sName + "'";
	    String subtitle = "";
	    out.println(SMUtilities.SMCPTitleSubBGColor(title, subtitle, SMUtilities.getInitBackGroundColor(getServletContext(), sDBID), sCompanyName));
		
	    out.println("<BR><A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMUserLogin?" 
				+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
				+ "\">Return to user login</A><BR><BR>");

		//Print a link to main menu:
		out.println("<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smgl.GLMainMenu?" 
				+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
				+ "\">Return to General Ledger Main Menu</A><BR>");
		out.println("<A HREF=\"" + WebContextParameters.getdocumentationpageURL(getServletContext()) + "#" + Long.toString(SMSystemFunctions.GLEditFinancialStatements)
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
	    	out.println("Error [1534454269] getting database connection");
			out.println("</BODY></HTML>");
			return;
	    }
	    
	    if (!clsDatabaseFunctions.start_data_transaction(conn)){
	    	clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1547080741]");
	    	out.println("Error [1534454270] starting database transaction");
			out.println("</BODY></HTML>");
			return;
	    }
	    
	    String sSQL = "";
	    if (lID < 0){
		    sSQL = "INSERT INTO " + SMTableglstatementforms.TableName
		    	+ " (" + SMTableglstatementforms.mtext
		    	+ ", " + SMTableglstatementforms.sdescription
		    	+ ", " + SMTableglstatementforms.sname
		    	+ ") VALUES ("
		    	+ " '" + clsDatabaseFunctions.FormatSQLStatement(sText) + "'"
		    	+ ", '" + clsDatabaseFunctions.FormatSQLStatement(sDescription) + "'"
		    	+ ", '" + clsDatabaseFunctions.FormatSQLStatement(sName) + "'"
		    	+ ")"
		    	;
	    }else{
	    	sSQL = "UPDATE " + SMTableglstatementforms.TableName
	    		+ " SET" 
	    	    + " " + SMTableglstatementforms.mtext + " = '" + clsDatabaseFunctions.FormatSQLStatement(sText) + "'"
	    		+ ", " + SMTableglstatementforms.sdescription + " = '" + clsDatabaseFunctions.FormatSQLStatement(sDescription) + "'"
	    		+ ", " + SMTableglstatementforms.sname + " = '" + clsDatabaseFunctions.FormatSQLStatement(sName) + "'"
	    		+ " WHERE" 
	    		+ " " + SMTableglstatementforms.lid + " = " + lID 
	    		;
	    }
	    //System.out.println("[1437079668] SQL = '" + sSQL + "'");
	    try{
	    	clsDatabaseFunctions.executeSQL(sSQL, conn);
	    }catch (SQLException ex){
	    	clsDatabaseFunctions.rollback_data_transaction(conn);
	    	clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1547080742]");
	    	out.println("Error [1534454271] updating financial statement forms with SQL: " + sSQL
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
			    	clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1547080743]");
			    	out.println("Error [1534454272] getting last insert ID with SQL: " + sSQL);
					out.println("</BODY></HTML>");
					return;
				}else{
					lID = rs.getLong(1);
					rs.close();
				}
			} catch (SQLException e) {
		    	clsDatabaseFunctions.rollback_data_transaction(conn);
		    	clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1547080744]");
		    	out.println("Error [1534454273] getting last insert ID with SQL: " + sSQL);
				out.println("</BODY></HTML>");
				return;
			}
	    }
	    
	    if (!clsDatabaseFunctions.commit_data_transaction(conn)){
	    	clsDatabaseFunctions.rollback_data_transaction(conn);
	    	clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1547080745]");
	    	out.println("Error [1534454274] - Unable to commit data transaction.");
				out.println("</BODY></HTML>");
				return;
	    }
	    clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1547080746]");
		String sRedirectString = 
				"" + SMUtilities.getURLLinkBase(getServletContext()) + "smgl.GLEditFinancialStatementsEdit"
				+ "?" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
				+ "&" + SMTableglstatementforms.lid + "=" + Long.toString(lID)
				+ "&" + GLEditFinancialStatementsSelect.BUTTON_SUBMIT_EDIT + "=" + "Y"
				+ "&Status=Financial Statement Form was successfully saved."
			; 
	    redirectProcess(sRedirectString, response);
	    return;
	}
	private void redirectProcess(String sRedirectString, HttpServletResponse res ){
		try {
			res.sendRedirect(sRedirectString);
		} catch (IOException e1) {
			System.out.println("Error [1534454275] in " + this.toString() + ".redirectAction - IOException error redirecting with string: "
					+ sRedirectString + " - " + e1.getMessage());
			return;
		} catch (IllegalStateException e1) {
			System.out.println("Error [1534454276] in " + this.toString() + ".redirectAction - IllegalStateException error redirecting with string: "
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
