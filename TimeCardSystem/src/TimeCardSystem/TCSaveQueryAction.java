package TimeCardSystem;

import java.io.IOException;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import ServletUtilities.clsServletUtilities;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsManageRequestParameters;
import TCSDataDefinition.Employees;
import TCSDataDefinition.TCTablesavedqueries;

public class TCSaveQueryAction extends HttpServlet {

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
	    String sDBID = (String) CurrentSession.getAttribute(TimeCardUtilities.SESSION_ATTRIBUTE_DB);
	    String sUserID = (String) CurrentSession.getAttribute(TimeCardUtilities.SESSION_ATTRIBUTE_EID);

	    //sCallingClass will look like: smar.ARAgedTrialBalanceReport
	    String sCallingClass = clsManageRequestParameters.get_Request_Parameter("CallingClass", request);
	    String sQueryString = clsServletUtilities.URLDecode(clsManageRequestParameters.get_Request_Parameter(TCQuerySelect.PARAM_QUERYSTRING, request));
	    String sQueryTitle = clsManageRequestParameters.get_Request_Parameter(TCQuerySelect.PARAM_QUERYTITLE, request);
	    String sQueryComment = clsManageRequestParameters.get_Request_Parameter(TCTablesavedqueries.scomment, request);
	    String sFontSize = clsManageRequestParameters.get_Request_Parameter(TCQuerySelect.PARAM_FONTSIZE, request);
		boolean bIncludeBorder = (request.getParameter(TCQuerySelect.PARAM_INCLUDEBORDER) != null);
		boolean bExportAsCommaDelimited = clsManageRequestParameters.get_Request_Parameter(
				TCQuerySelect.PARAM_EXPORTOPTIONS, 
				request).compareToIgnoreCase(TCQuerySelect.EXPORT_COMMADELIMITED_VALUE) == 0;
		boolean bExportAsHTML = clsManageRequestParameters.get_Request_Parameter(
				TCQuerySelect.PARAM_EXPORTOPTIONS, 
				request).compareToIgnoreCase(TCQuerySelect.EXPORT_HTML_VALUE) == 0;
	    boolean bAlternateRowColors = (request.getParameter(TCQuerySelect.PARAM_ALTERNATEROWCOLORS) != null);
	    boolean bTotalNumericFields = (request.getParameter(TCQuerySelect.PARAM_TOTALNUMERICFIELDS) != null);
	    boolean bShowSQLCommand = (request.getParameter(TCQuerySelect.PARAM_SHOWSQLCOMMAND) != null);
	    boolean bHideHeaderFooter = (request.getParameter(TCQuerySelect.PARAM_HIDEHEADERFOOTER) != null);
	    String sPrivateQuery = "0";
	    if (clsManageRequestParameters.get_Request_Parameter(
    		TCQueryGenerate.SAVE_AS_PRIVATE_BUTTON, request).compareToIgnoreCase(TCQueryGenerate.SAVE_AS_PRIVATE_BUTTON_LABEL) == 0){
	    	sPrivateQuery = "1";
	    }
	    //Try to insert this query into the saved queries table:
	    String SQL = "INSERT INTO " + TCTablesavedqueries.TableName + "("
    		+ TCTablesavedqueries.dattimesaved
    		+ ", " + TCTablesavedqueries.scomment
    		+ ", " + TCTablesavedqueries.sfirstname
    		+ ", " + TCTablesavedqueries.slastname
    		+ ", " + TCTablesavedqueries.ssql
    		+ ", " + TCTablesavedqueries.stitle
    		+ ", " + TCTablesavedqueries.suser
    		+ ", " + TCTablesavedqueries.iprivate
    		+ ") SELECT "
    		+ "NOW()"
    		+ ", '" + clsDatabaseFunctions.FormatSQLStatement(sQueryComment) + "'"
    		+ ", " + Employees.sEmployeeFirstName
    		+ ", " + Employees.sEmployeeLastName
    		+ ", '" + clsDatabaseFunctions.FormatSQLStatement(sQueryString) + "'"
    		+ ", '" + clsDatabaseFunctions.FormatSQLStatement(sQueryTitle) + "'"
    		+ ", '" + clsDatabaseFunctions.FormatSQLStatement(sUserID) + "'"
    		+ ", " + sPrivateQuery
    		+ " FROM " + Employees.TableName
    		+ " WHERE ("
    			+ "(" + Employees.sEmployeeID + " = '" + sUserID + "')"
    		+ ")"
	    ;
	    String sRedirect = "" + clsServletUtilities.getURLLinkBase(getServletContext()) + "" + sCallingClass
				+ "?" + TCQuerySelect.PARAM_QUERYTITLE + "=" + sQueryTitle
				+ "&" + TCQuerySelect.PARAM_FONTSIZE + "=" + sFontSize
		;
		if (bAlternateRowColors){sRedirect += "&" + TCQuerySelect.PARAM_ALTERNATEROWCOLORS + "=1";}
		if (bExportAsHTML){sRedirect += "&" + TCQuerySelect.PARAM_EXPORTOPTIONS + "=" + TCQuerySelect.EXPORT_HTML_VALUE;}
		if (bExportAsCommaDelimited){sRedirect += "&" + TCQuerySelect.PARAM_EXPORTOPTIONS + "=" + TCQuerySelect.EXPORT_COMMADELIMITED_VALUE;}
		if (bIncludeBorder){sRedirect += "&" + TCQuerySelect.PARAM_INCLUDEBORDER + "=1";}
		if (bTotalNumericFields){sRedirect += "&" + TCQuerySelect.PARAM_TOTALNUMERICFIELDS + "=1";}
		if (bShowSQLCommand){sRedirect += "&" + TCQuerySelect.PARAM_SHOWSQLCOMMAND + "=1";}
		if (bHideHeaderFooter){sRedirect += "&" + TCQuerySelect.PARAM_HIDEHEADERFOOTER + "=1";}
		//Couldn't make this reliably return back to the TCQuerySelect screen so I commented it out - TJR
		//It choked on queries with c% in them, for example
		//sRedirect += "&" + TCQuerySelect.PARAM_QUERYSTRING + "=" + sQueryString;
		try {
			clsDatabaseFunctions.executeSQL(SQL, getServletContext(), sDBID, "MySQL", this.toString() + ",insert - user: " + sUserID);
		} catch (SQLException e) {
		    String sWarning = "Error inserting query - " + e.getMessage();
		    sRedirect += "&Warning=" + sWarning;
    		response.sendRedirect(response.encodeRedirectURL(sRedirect));
    		return;
		}
		//Log the save action:
		TCLogEntry log = new TCLogEntry(sDBID, getServletContext());
		log.writeEntry(
				sUserID, 
				TCLogEntry.LOG_OPERATION_QUERY_SAVE, 
				"Title: '" + sQueryTitle + "', Private?: " + sPrivateQuery + ", Comment '" + sQueryComment + "'",
				"QueryString: " + SQL,
				"[1517847213]");
	    sRedirect += "&Status=Query: '" + sQueryTitle + "' was saved successfully";
		response.sendRedirect(response.encodeRedirectURL(sRedirect));
	    return;
	}

	public void doGet(HttpServletRequest request,
			HttpServletResponse response)
	throws ServletException, IOException {

		doPost(request, response);
	}
}
