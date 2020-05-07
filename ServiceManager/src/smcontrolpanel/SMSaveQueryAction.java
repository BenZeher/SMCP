package smcontrolpanel;

import java.io.IOException;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import SMClasses.SMLogEntry;
import SMDataDefinition.*;
import ServletUtilities.clsServletUtilities;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsManageRequestParameters;

public class SMSaveQueryAction extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public void doPost(HttpServletRequest request,
				HttpServletResponse response)
				throws ServletException, IOException {

	    response.setContentType("text/html");
		//PrintWriter out = response.getWriter();
	    //Get the session info:
	    HttpSession CurrentSession = request.getSession(true);
	    String sDBID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_DATABASE_ID);
	    String sUserID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERID);
	    String sUserFullName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERFIRSTNAME)
	    		+ " " + (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERLASTNAME);

	    //sCallingClass will look like: smar.ARAgedTrialBalanceReport
	    String sCallingClass = clsManageRequestParameters.get_Request_Parameter("CallingClass", request);
	    String sQueryString = clsServletUtilities.URLDecode(clsManageRequestParameters.get_Request_Parameter(SMQuerySelect.PARAM_QUERYSTRING, request));
	    String sQueryTitle = clsManageRequestParameters.get_Request_Parameter(SMQuerySelect.PARAM_QUERYTITLE, request);
	    String sQueryID = clsManageRequestParameters.get_Request_Parameter(SMQuerySelect.PARAM_QUERYID, request);
	    String sQueryComment = clsManageRequestParameters.get_Request_Parameter(SMTablesavedqueries.scomment, request);
	    String sFontSize = clsManageRequestParameters.get_Request_Parameter(SMQuerySelect.PARAM_FONTSIZE, request);
		boolean bIncludeBorder = (request.getParameter(SMQuerySelect.PARAM_INCLUDEBORDER) != null);
		boolean bExportAsCommaDelimited = clsManageRequestParameters.get_Request_Parameter(
				SMQuerySelect.PARAM_EXPORTOPTIONS, 
				request).compareToIgnoreCase(SMQuerySelect.EXPORT_COMMADELIMITED_VALUE) == 0;
		boolean bExportAsHTML = clsManageRequestParameters.get_Request_Parameter(
				SMQuerySelect.PARAM_EXPORTOPTIONS, 
				request).compareToIgnoreCase(SMQuerySelect.EXPORT_HTML_VALUE) == 0;
	    boolean bAlternateRowColors = (request.getParameter(SMQuerySelect.PARAM_ALTERNATEROWCOLORS) != null);
	    boolean bTotalNumericFields = (request.getParameter(SMQuerySelect.PARAM_TOTALNUMERICFIELDS) != null);
	    boolean bShowSQLCommand = (request.getParameter(SMQuerySelect.PARAM_SHOWSQLCOMMAND) != null);
	    boolean bHideHeaderFooter = (request.getParameter(SMQuerySelect.PARAM_HIDEHEADERFOOTER) != null);
	    String sPrivateQuery = "0";
	    if (clsManageRequestParameters.get_Request_Parameter(
    		SMQueryGenerate.SAVE_AS_PRIVATE_BUTTON, request).compareToIgnoreCase(SMQueryGenerate.SAVE_AS_PRIVATE_BUTTON_LABEL) == 0){
	    	sPrivateQuery = "1";
	    }
	    boolean bUpdateExistingQuery = clsManageRequestParameters.get_Request_Parameter(
	    		SMQueryGenerate.UPDATE_EXISTING_QUERY_BUTTON, request).compareToIgnoreCase("") != 0;
	    
	    String SQL = "";
	    if(sQueryID.compareToIgnoreCase("") != 0 && bUpdateExistingQuery) {
	    	 SQL = "UPDATE " + SMTablesavedqueries.TableName 
	    	    + " SET " + SMTablesavedqueries.ssql + " = '" + clsDatabaseFunctions.FormatSQLStatement(sQueryString) + "'"
	    	    + ", " + SMTablesavedqueries.scomment + " = '" + clsDatabaseFunctions.FormatSQLStatement(sQueryComment) + "'"
	    	    + ", " + SMTablesavedqueries.stitle + " = '" + clsDatabaseFunctions.FormatSQLStatement(sQueryTitle) + "'"
	    	    + " WHERE (" + SMTablesavedqueries.id + " = " +  sQueryID + ")"
	    	    ;
	    	 
	    } else {
	    	//Try to insert this query into the saved queries table:
		    SQL = "INSERT INTO " + SMTablesavedqueries.TableName + "("
	    		+ SMTablesavedqueries.dattimesaved
	    		+ ", " + SMTablesavedqueries.scomment
	    		+ ", " + SMTablesavedqueries.ssql
	    		+ ", " + SMTablesavedqueries.stitle
	    		+ ", " + SMTablesavedqueries.suserfullname
	    		+ ", " + SMTablesavedqueries.luserid
	    		+ ", " + SMTablesavedqueries.iprivate
	    		+ ") VALUES ( "
	    		+ "NOW()"
	    		+ ", '" + clsDatabaseFunctions.FormatSQLStatement(sQueryComment) + "'"
	    		+ ", '" + clsDatabaseFunctions.FormatSQLStatement(sQueryString) + "'"
	    		+ ", '" + clsDatabaseFunctions.FormatSQLStatement(sQueryTitle) + "'"
	    	    + ", '" + clsDatabaseFunctions.FormatSQLStatement(sUserFullName) + "'"
	    		+ ", " + sUserID 
	    		+ ", " + sPrivateQuery
	    		+ ")"
		    ;	
	    }
	    System.out.println(SQL);
	    String sRedirect = "" + SMUtilities.getURLLinkBase(getServletContext()) + "" + sCallingClass
				+ "?" + SMQuerySelect.PARAM_QUERYTITLE + "=" + sQueryTitle
				+ "&" + SMQuerySelect.PARAM_FONTSIZE + "=" + sFontSize
				+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
		;
		if (bAlternateRowColors){sRedirect += "&" + SMQuerySelect.PARAM_ALTERNATEROWCOLORS + "=1";}
		if (bExportAsHTML){sRedirect += "&" + SMQuerySelect.PARAM_EXPORTOPTIONS + "=" + SMQuerySelect.EXPORT_HTML_VALUE;}
		if (bExportAsCommaDelimited){sRedirect += "&" + SMQuerySelect.PARAM_EXPORTOPTIONS + "=" + SMQuerySelect.EXPORT_COMMADELIMITED_VALUE;}
		if (bIncludeBorder){sRedirect += "&" + SMQuerySelect.PARAM_INCLUDEBORDER + "=1";}
		if (bTotalNumericFields){sRedirect += "&" + SMQuerySelect.PARAM_TOTALNUMERICFIELDS + "=1";}
		if (bShowSQLCommand){sRedirect += "&" + SMQuerySelect.PARAM_SHOWSQLCOMMAND + "=1";}
		if (bHideHeaderFooter){sRedirect += "&" + SMQuerySelect.PARAM_HIDEHEADERFOOTER + "=1";}
		//Couldn't make this reliably return back to the SMQuerySelect screen so I commented it out - TJR
		//It choked on queries with c% in them, for example
		//sRedirect += "&" + SMQuerySelect.PARAM_QUERYSTRING + "=" + sQueryString;
		try {
			clsDatabaseFunctions.executeSQL(SQL, getServletContext(), sDBID, "MySQL", this.toString() + ",insert - user: " + sUserFullName);
		} catch (SQLException e) {
		    String sWarning = "Error inserting query - " + e.getMessage();
		    sRedirect += "&Warning=" + sWarning;
    		response.sendRedirect(response.encodeRedirectURL(sRedirect));
    		return;
		}
		//Log the save action:
		SMClasses.SMLogEntry log = new SMClasses.SMLogEntry(sDBID, getServletContext());
		log.writeEntry(
				sUserID, 
				SMLogEntry.LOG_OPERATION_SMQUERYSAVE, 
				"Title: '" + sQueryTitle + "', Private?: " + sPrivateQuery + ", Comment '" + sQueryComment + "'",
				"QueryString: " + SQL,
				"[1376509356]");
	    sRedirect += "&Status=" + "Query: '" + sQueryTitle + "' was saved successfully";
		response.sendRedirect(response.encodeRedirectURL(sRedirect));
	    return;
	}

	public void doGet(HttpServletRequest request,
			HttpServletResponse response)
	throws ServletException, IOException {

		doPost(request, response);
	}
}
