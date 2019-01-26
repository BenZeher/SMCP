package smcontrolpanel;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import SMClasses.SMWorkOrderHeader;
import SMDataDefinition.SMTableworkorderdetailsheets;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsManageRequestParameters;

public class SMDetailSheetAction extends HttpServlet{
	
	private static final long serialVersionUID = 1L;
	private static String sObjectName = "Detail Sheet";
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
				SMSystemFunctions.SMEditDetailSheets
			)
		){
			return;
		}
	    //Get the session info:
	    HttpSession CurrentSession = request.getSession(true);
	    String sDBID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_DATABASE_ID);
	    String sCompanyName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_COMPANYNAME);
	    String sUserID = (String)CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERID);
	    String sUserFirstName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERFIRSTNAME);
	    String sUserLastName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERLASTNAME);
		long lID;
		try {
			lID = Long.parseLong(clsManageRequestParameters.get_Request_Parameter(SMDetailSheetEdit.DETAIL_SHEET_ID, request));
		} catch (NumberFormatException e1) {
			out.println("Error [1437682943] Invalid detail sheet ID '" 
				+  clsManageRequestParameters.get_Request_Parameter(SMDetailSheetEdit.DETAIL_SHEET_ID, request) + "- click 'Back' to correct.");
			out.println("</BODY></HTML>");
			return;
		}
		String sName = request.getParameter(SMTableworkorderdetailsheets.sname).trim().replace("&quot;", "\"");
		String sDescription = request.getParameter(SMTableworkorderdetailsheets.sdescription).trim().replace("&quot;", "\"");
		String sText = request.getParameter(SMTableworkorderdetailsheets.mtext).trim().replace("&quot;", "\"");
		String sType = clsManageRequestParameters.get_Request_Parameter(SMTableworkorderdetailsheets.itype, request);
		
		if (clsManageRequestParameters.get_Request_Parameter(
			SMDetailSheetEdit.TEST_HTML_BUTTON_NAME, request).compareToIgnoreCase(SMDetailSheetEdit.TEST_HTML_BUTTON_LABEL) == 0){
			//Call the HTML form viewer:
			String sFormViewAddress = SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMCustomDetailSheetEdit"
				+ "?" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
				+ "&CallingClass=" + "SMDetailSheetEdit"
				+ "&" + SMWorkOrderHeader.ADD_DETAIL_SHEET_DROPDOWN_NAME + "=" + request.getParameter(SMDetailSheetEdit.DETAIL_SHEET_ID)
			;
			redirectProcess(sFormViewAddress, response);
			return;
		}
		
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
		
		if (sType.compareToIgnoreCase(Integer.toString(SMTableworkorderdetailsheets.DETAIL_SHEET_TYPE_HTML)) !=0) {
			if (sType.compareToIgnoreCase(Integer.toString(SMTableworkorderdetailsheets.DETAIL_SHEET_TYPE_TEXT)) !=0) {
				//Set this as the default:
				sType = Integer.toString(SMTableworkorderdetailsheets.DETAIL_SHEET_TYPE_TEXT);
			}
		}
		
	    String title = "Updating " + sObjectName + "'" + sName + "'";
	    String subtitle = "";
	    out.println(SMUtilities.SMCPTitleSubBGColor(title, subtitle, SMUtilities.getInitBackGroundColor(getServletContext(), sDBID), sCompanyName));
		
	    out.println("<BR><A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMUserLogin?" 
				+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
				+ "\">Return to user login</A><BR><BR>");
	
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
	    	out.println("Error getting database connection");
			out.println("</BODY></HTML>");
			return;
	    }
	    
	    if (!clsDatabaseFunctions.start_data_transaction(conn)){
	    	clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1547080433]");
	    	out.println("Error starting database transaction");
			out.println("</BODY></HTML>");
			return;
	    }
	    
	    String sSQL = "";
	    if (lID < 0){
		    sSQL = "INSERT INTO " + SMTableworkorderdetailsheets.TableName
		    	+ " (" + SMTableworkorderdetailsheets.mtext
		    	+ ", " + SMTableworkorderdetailsheets.sdescription
		    	+ ", " + SMTableworkorderdetailsheets.sname
		    	+ ", " + SMTableworkorderdetailsheets.itype
		    	+ ") VALUES ("
		    	+ " '" + clsDatabaseFunctions.FormatSQLStatement(sText) + "'"
		    	+ ", '" + clsDatabaseFunctions.FormatSQLStatement(sDescription) + "'"
		    	+ ", '" + clsDatabaseFunctions.FormatSQLStatement(sName) + "'"
		    	+ ", " + clsDatabaseFunctions.FormatSQLStatement(sType)
		    	+ ")"
		    	;
	    }else{
	    	sSQL = "UPDATE " + SMTableworkorderdetailsheets.TableName
	    		+ " SET" 
	    	    + " " + SMTableworkorderdetailsheets.mtext + " = '" + clsDatabaseFunctions.FormatSQLStatement(sText) + "'"
	    		+ ", " + SMTableworkorderdetailsheets.sdescription + " = '" + clsDatabaseFunctions.FormatSQLStatement(sDescription) + "'"
	    		+ ", " + SMTableworkorderdetailsheets.sname + " = '" + clsDatabaseFunctions.FormatSQLStatement(sName) + "'"
	    		+ ", " + SMTableworkorderdetailsheets.itype + " = '" + clsDatabaseFunctions.FormatSQLStatement(sType) + "'"
	    		+ " WHERE" 
	    		+ " " + SMTableworkorderdetailsheets.lid + " = " + lID 
	    		;
	    }
	    //System.out.println("[1437079668] SQL = '" + sSQL + "'");
	    try{
	    	clsDatabaseFunctions.executeSQL(sSQL, conn);
	    }catch (SQLException ex){
	    	clsDatabaseFunctions.rollback_data_transaction(conn);
	    	clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1547080434]");
	    	out.println("Error updating detail sheets with SQL: " + sSQL
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
			    	clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1547080435]");
			    	out.println("Error getting last insert ID with SQL: " + sSQL);
					out.println("</BODY></HTML>");
					return;
				}else{
					lID = rs.getLong(1);
					rs.close();
				}
			} catch (SQLException e) {
		    	clsDatabaseFunctions.rollback_data_transaction(conn);
		    	clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1547080436]");
		    	out.println("Error getting last insert ID with SQL: " + sSQL);
				out.println("</BODY></HTML>");
				return;
			}
	    }
	    
	    if (!clsDatabaseFunctions.commit_data_transaction(conn)){
	    	clsDatabaseFunctions.rollback_data_transaction(conn);
	    	clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1547080437]");
	    	out.println("Unable to commit data transaction.");
				out.println("</BODY></HTML>");
				return;
	    }
	    clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1547080438]");
		String sRedirectString = 
				"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMDetailSheetEdit"
				+ "?" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
				+ "&" + SMDetailSheetEdit.DETAIL_SHEET_ID + "=" + Long.toString(lID)
				+ "&" + SMDetailSheetEdit.BUTTON_SUBMIT_EDIT + "=" + "Y"
				+ "&Status=Detail sheet was successfully saved."
			; 
	    redirectProcess(sRedirectString, response);
	    return;
	}
	private void redirectProcess(String sRedirectString, HttpServletResponse res ){
		try {
			res.sendRedirect(sRedirectString);
		} catch (IOException e1) {
			System.out.println("Error [1395237124] in " + this.toString() + ".redirectAction - IOException error redirecting with string: "
					+ sRedirectString + " - " + e1.getMessage());
			return;
		} catch (IllegalStateException e1) {
			System.out.println("Error [1395237125] in " + this.toString() + ".redirectAction - IllegalStateException error redirecting with string: "
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
