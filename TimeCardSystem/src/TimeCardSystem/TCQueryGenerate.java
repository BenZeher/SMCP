package TimeCardSystem;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.regex.Pattern;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import SMDataDefinition.SMTablesavedqueries;
import ServletUtilities.clsServletUtilities;
import ServletUtilities.clsCreateHTMLFormFields;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsManageRequestParameters;
import ServletUtilities.clsStringFunctions;
import TCSDataDefinition.TCSTablecompanyprofile;

public class TCQueryGenerate extends HttpServlet {

	private static final long serialVersionUID = 1L;
	private static final String sQuerySaveClass = "TimeCardSystem.TCSaveQueryAction";
	public static final String SAVE_AS_PRIVATE_BUTTON = "SavePrivateQueryButton";
	public static final String SAVE_AS_PRIVATE_BUTTON_LABEL = "Save private query";
	public static final String SAVE_AS_PUBLIC_BUTTON = "SavePublicQueryButton";
	public static final String SAVE_AS_PUBLIC_BUTTON_LABEL = "Save public query";
	
	//formats
	private static SimpleDateFormat USDateformatter = new SimpleDateFormat("MM-dd-yyyy hh:mm:ss a EEE");
	
	private String sCallingClass = "";
	private boolean bDebugMode = false;

	//private static SimpleDateFormat USTimeOnlyformatter = new SimpleDateFormat("hh:mm:ss a");
	
	public void doPost(HttpServletRequest request,
				HttpServletResponse response)
				throws ServletException, IOException {
		
	    response.setContentType("text/html");
		PrintWriter out = response.getWriter();
		
		//Get the session info:
		HttpSession CurrentSession = request.getSession(true);
		String sDBID = "";
		try {
			sDBID = (String)CurrentSession.getAttribute(TimeCardUtilities.SESSION_ATTRIBUTE_DB);
		} catch (Exception e1) {
			sDBID = "";
		}
		if (sDBID == null){
			sDBID = "";
		}
		
		if (sDBID.compareToIgnoreCase("") == 0){
			out.println("<B><FONT COLOR=\"RED\">WARNING: " + "No database name found in session.</FONT></B><BR>");
			return;
		}
		
		String sUserID = "";
		try {
			sUserID = (String)CurrentSession.getAttribute(TimeCardUtilities.SESSION_ATTRIBUTE_EID);
		} catch (Exception e1) {
			sUserID = "";
		}
		if (sUserID == null){
			sUserID = "";
		}
		
		if (sUserID.compareToIgnoreCase("") == 0){
			out.println("<B><FONT COLOR=\"RED\">WARNING: " + "No employee ID found in session.</FONT></B><BR>");
			return;
		}
		
		String sCompanyName = "";
		//Get the company information:
		String sSQL = "SELECT * FROM " + TCSTablecompanyprofile.TableName;

		try {
			ResultSet rs = clsDatabaseFunctions.openResultSet(
					sSQL, 
					getServletContext(), 
					sDBID, 
					"MySQL",
					this.toString() + ".reading company name"
					);
			if (rs.next()){
				CurrentSession.setAttribute(TimeCardUtilities.SESSION_ATTRIBUTE_COMPANYNAME, rs.getString(TCSTablecompanyprofile.sCompanyName));
				sCompanyName = rs.getString(TCSTablecompanyprofile.sCompanyName);
				rs.close();
			}else{
				out.println("<BR>Could not read company name.");
				out.println("</BODY></HTML>");
				rs.close();
				return;
			}
		} catch (SQLException e) {
			out.println("<BR>Error reading read company name: " + e.getMessage()+ ".");
			out.println("</BODY></HTML>");
			return;
		}
		
		String sTitle = "Time Card System";
		String sSubtitle = "Time Card System - " + CurrentSession.getAttribute(TimeCardUtilities.SESSION_ATTRIBUTE_COMPANYNAME).toString();
		out.println(TimeCardUtilities.TCTitleSubBGColorWithFont(sTitle, sSubtitle, TimeCardUtilities.BACKGROUND_COLOR_FOR_ADMIN_SCREENS, TimeCardUtilities.BASE_FONT_FAMILY));

		String sWarning = clsManageRequestParameters.get_Request_Parameter("Warning", request);
		if (! sWarning.equalsIgnoreCase("")){
			out.println("<B><FONT COLOR=\"RED\">WARNING: " + sWarning + "</FONT></B><BR>");
		}
		String sStatus = clsManageRequestParameters.get_Request_Parameter("Status", request);
		if (! sStatus.equalsIgnoreCase("")){
			out.println("<B><FONT COLOR=\"BLACK\">STATUS: " + sStatus + "</FONT></B><BR>");
		}
		
	    //sCallingClass will look like: smcontrolpanel.ARAgedTrialBalanceReport
	    sCallingClass = clsManageRequestParameters.get_Request_Parameter("CallingClass", request);
	   
		String sQueryTitle = clsManageRequestParameters.get_Request_Parameter(TCQuerySelect.PARAM_QUERYTITLE, request);
		String sQueryID = clsManageRequestParameters.get_Request_Parameter(TCQuerySelect.PARAM_QUERYID, request);
		String sQueryString = clsServletUtilities.URLDecode(clsManageRequestParameters.get_Request_Parameter(TCQuerySelect.PARAM_QUERYSTRING, request));
		String sRawQueryString = clsServletUtilities.URLDecode(clsManageRequestParameters.get_Request_Parameter(TCQuerySelect.PARAM_RAWQUERYSTRING, request));
		String sFontSize = clsManageRequestParameters.get_Request_Parameter(TCQuerySelect.PARAM_FONTSIZE, request);
		String sComment = clsManageRequestParameters.get_Request_Parameter(SMTablesavedqueries.scomment, request);
		boolean bIncludeBorder = (request.getParameter(TCQuerySelect.PARAM_INCLUDEBORDER) != null);
    	String sReportTitle = "Service Manager Query: " + sQueryTitle;  
    	String sCriteria = "";

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
	    boolean bHideColumnLabels = (request.getParameter(TCQuerySelect.PARAM_HIDECOLUMNLABELS) != null);
	    
	    if (bExportAsCommaDelimited){
	    	 response.setContentType("text/csv");
             String disposition = "attachment; fileName= " + sQueryTitle.replace(",", "_") + ".csv";
             response.setHeader("Content-Disposition", disposition);
	    }else{
			if (bExportAsHTML){
				String disposition = "attachment; fileName= " + sQueryTitle + ".html";
				response.setHeader("Content-Disposition", disposition);
			}
	    	out.println("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.0 "
			   + "Transitional//EN\">" 
			   + "<HTML>");
			
	    if(!bHideHeaderFooter){
			out.println("<HEAD>"
			   //+ "X-XSS-Protection: 0" // This will prevent "token contains a reflected xss vector" errors if we include 
		       								//double quotes in iframes, etc.
		      //
		       + "<TITLE>" + sReportTitle + " - " + sCompanyName + "</TITLE></HEAD>\n<BR>"
		       + "<BODY BGCOLOR=\"#FFFFFF\">"
			   + "<TABLE BORDER=0 WIDTH=100%>"
			   + "<TR><TD ALIGN=LEFT WIDTH=45%><FONT SIZE=2>" 
			   + USDateformatter.format((new Timestamp(System.currentTimeMillis()))) + " Printed by " 
			   		+ TimeCardUtilities.getFullNamebyEmployeeID(sUserID, getServletContext(), sDBID, sCallingClass) 
			   + "</FONT></TD><TD ALIGN=CENTER WIDTH=55%><FONT SIZE=2><B>" + sCompanyName + "</B></FONT></TD></TR>"
			   + "<TR><TD VALIGN=BOTTOM COLSPAN=2><FONT SIZE=2><B>" + sReportTitle + "</B></FONT></TD></TR>"
			   + "<TR><TD COLSPAN=2><FONT SIZE=2>" + sCriteria + "</FONT></TD></TR>");
					   
	    	out.println("<TD><A HREF=\"" + clsServletUtilities.getURLLinkBase(getServletContext()) + "TimeCardSystem.AdminMain?" 
				+ TimeCardUtilities.SESSION_ATTRIBUTE_DB + "=" + sDBID 
				+ "&" + TimeCardUtilities.SESSION_ATTRIBUTE_PINNUMBER + " = " + sUserID
				+ "\">Return to main admin menu</A><BR>");
	    	
    		//Print a link to the main query page:
    		out.println("<BR><A HREF=\"" + clsServletUtilities.getURLLinkBase(getServletContext()) + "TimeCardSystem.TCQuerySelect" 
    				+ "\">Return to Manage Queries</A>");
			out.println("</TD></TR></TABLE>");
			   }
	    }
	    
	    //Build a form to save the query here:
	    if (!bExportAsCommaDelimited && !bExportAsHTML && !bHideHeaderFooter){
			out.println ("<FORM ACTION =\"" + clsServletUtilities.getURLLinkBase(getServletContext()) 
					+ sQuerySaveClass + "\" METHOD='POST'>");
			    
			//Store hidden variables:
		    out.println("<INPUT TYPE=HIDDEN NAME=\"" 
				+ TCQuerySelect.PARAM_QUERYSTRING
				//TODO - test this:
				//+ "\" VALUE=\"" + SMUtilities.filter(sQueryString)
				+ "\" VALUE=\"" + clsServletUtilities.URLEncode(sQueryString)
				+ "\">" + "\n");
			
		    out.println("<INPUT TYPE=HIDDEN NAME=\"" 
				+ TCQuerySelect.PARAM_RAWQUERYSTRING
				+ "\" VALUE=\"" + clsServletUtilities.URLEncode(sRawQueryString)
				+ "\">" + "\n");
		    
			out.println("<INPUT TYPE=HIDDEN NAME=\"" 
				+ TCQuerySelect.PARAM_QUERYTITLE
				+ "\" VALUE=\"" + clsStringFunctions.filter(sQueryTitle)
				+ "\">" + "\n");
			
			out.println("<INPUT TYPE=HIDDEN NAME=\"" 
					+ TCQuerySelect.PARAM_FONTSIZE
					+ "\" VALUE=\"" + clsStringFunctions.filter(sFontSize)
					+ "\">" + "\n");
			
			if (bAlternateRowColors){
				out.println("<INPUT TYPE=HIDDEN NAME=\"" 
					+ TCQuerySelect.PARAM_ALTERNATEROWCOLORS
					+ "\" VALUE=\"" + "1"
					+ "\">" + "\n");
			}
			if (bExportAsHTML){
				out.println("<INPUT TYPE=HIDDEN NAME=\"" 
					+ TCQuerySelect.PARAM_EXPORTOPTIONS
					+ "\" VALUE=\"" + TCQuerySelect.EXPORT_HTML_VALUE
					+ "\">" + "\n");
			}
			if (bExportAsCommaDelimited){
				out.println("<INPUT TYPE=HIDDEN NAME=\"" 
					+ TCQuerySelect.PARAM_EXPORTOPTIONS
					+ "\" VALUE=\"" + TCQuerySelect.EXPORT_COMMADELIMITED_VALUE
					+ "\">" + "\n");
			}
    		if (bIncludeBorder){
				out.println("<INPUT TYPE=HIDDEN NAME=\"" 
						+ TCQuerySelect.PARAM_INCLUDEBORDER
						+ "\" VALUE=\"" + "1"
						+ "\">" + "\n");
    		}
    		if (bTotalNumericFields){
				out.println("<INPUT TYPE=HIDDEN NAME=\"" 
						+ TCQuerySelect.PARAM_TOTALNUMERICFIELDS
						+ "\" VALUE=\"" + "1"
						+ "\">" + "\n");
    		}
    		if (bShowSQLCommand){
				out.println("<INPUT TYPE=HIDDEN NAME=\"" 
						+ TCQuerySelect.PARAM_SHOWSQLCOMMAND
						+ "\" VALUE=\"" + "1"
						+ "\">" + "\n");
    		}
    		if (bHideHeaderFooter){
				out.println("<INPUT TYPE=HIDDEN NAME=\"" 
						+ TCQuerySelect.PARAM_HIDEHEADERFOOTER
						+ "\" VALUE=\"" + "1"
						+ "\">" + "\n");
    		}
    		if (bHideColumnLabels){
				out.println("<INPUT TYPE=HIDDEN NAME=\"" 
						+ TCQuerySelect.PARAM_HIDECOLUMNLABELS
						+ "\" VALUE=\"" + "1"
						+ "\">" + "\n");
    		}
			out.println("<INPUT TYPE=HIDDEN NAME=CallingClass VALUE=\"" 
					+ "TimeCardSystem.TCQuerySelect" + "\">" + "\n");
			
			out.println("<B>Query ID #:&nbsp;" + sQueryID + "</B><BR>" + "\n");
			
			out.println("Comment: "
					+ clsCreateHTMLFormFields.TDTextBox(
							SMTablesavedqueries.scomment, 
							sComment, 
							80, 
							254, 
							""
					) 
			);
			
			out.println("<INPUT TYPE=SUBMIT NAME='" + SAVE_AS_PRIVATE_BUTTON 
					+ "' VALUE='" + SAVE_AS_PRIVATE_BUTTON_LABEL + "' STYLE='height: 0.24in'>"
			);
			//TODO - add authentication for this later:
			/*
			if (SMSystemFunctions.isFunctionPermitted(
					SMSystemFunctions.SMManagePublicQueries, 
					sUserID, 
					getServletContext(), 
					sDBID,
					(String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_LICENSE_MODULE_LEVEL))){
				out.println("<INPUT TYPE=SUBMIT NAME='" + SAVE_AS_PUBLIC_BUTTON 
						+ "' VALUE='" + SAVE_AS_PUBLIC_BUTTON_LABEL + "' STYLE='height: 0.24in'>"
				);
			}
			*/
			out.println("<INPUT TYPE=SUBMIT NAME='" + SAVE_AS_PUBLIC_BUTTON 
					+ "' VALUE='" + SAVE_AS_PUBLIC_BUTTON_LABEL + "' STYLE='height: 0.24in'>"
			);
			
			out.println("</FORM>");
	    }

    	//Retrieve information
    	Connection conn = clsDatabaseFunctions.getConnection(
    			getServletContext(), 
    			sDBID, 
    			"MySQL", 
    			clsServletUtilities.getFullClassName(this.toString()) + " - userID: " + sUserID
    	);
    	if (conn == null){
    		sWarning = "Unable to get data connection.";
    		String sRedirect = clsServletUtilities.getURLLinkBase(getServletContext()) + "" + sCallingClass + "?"
			+ TCQuerySelect.PARAM_QUERYTITLE + "=" + sQueryTitle
			+ "&" + TCQuerySelect.PARAM_FONTSIZE + "=" + sFontSize
			;
    		if (bExportAsCommaDelimited){
    			sRedirect += "&" + TCQuerySelect.PARAM_EXPORTOPTIONS + "=" + TCQuerySelect.EXPORT_COMMADELIMITED_VALUE;
    		}
     		if (bExportAsHTML){
    			sRedirect += "&" + TCQuerySelect.PARAM_EXPORTOPTIONS + "=" + TCQuerySelect.EXPORT_HTML_VALUE;
    		}
    		if (bIncludeBorder){
    			sRedirect += "&" + TCQuerySelect.PARAM_INCLUDEBORDER + "=1";
    		}
    		if (bAlternateRowColors){
    			sRedirect += "&" + TCQuerySelect.PARAM_ALTERNATEROWCOLORS + "=1";
    		}
    		if (bTotalNumericFields){
    			sRedirect += "&" + TCQuerySelect.PARAM_TOTALNUMERICFIELDS + "=1";
    		}
    		if (bShowSQLCommand){
    			sRedirect += "&" + TCQuerySelect.PARAM_SHOWSQLCOMMAND + "=1";
    		}
       		if (bHideHeaderFooter){
    			sRedirect += "&" + TCQuerySelect.PARAM_HIDEHEADERFOOTER + "=1";
    		}
       		if (bHideColumnLabels){
    			sRedirect += "&" + TCQuerySelect.PARAM_HIDECOLUMNLABELS + "=1";
    		}


			sRedirect += "&Warning=" + sWarning
			;
    		
    		response.sendRedirect(sRedirect);
        	return;
    	}
    	
    	if (
    			(sQueryTitle.trim().compareToIgnoreCase("") == 0)
    			|| (sQueryString.trim().compareToIgnoreCase("") == 0)
    	){
    		clsDatabaseFunctions.freeConnection(getServletContext(), conn);
    		sWarning = "You must enter a query title and a query string.";
    		String sRedirect = clsServletUtilities.getURLLinkBase(getServletContext()) + "" + sCallingClass + "?"
    		+ TCQuerySelect.PARAM_QUERYTITLE + "=" + sQueryTitle
			+ "&" + TCQuerySelect.PARAM_FONTSIZE + "=" + sFontSize
			;
    		if (bExportAsCommaDelimited){
    			sRedirect += "&" + TCQuerySelect.PARAM_EXPORTOPTIONS + "=" + TCQuerySelect.EXPORT_COMMADELIMITED_VALUE;
    		}
     		if (bExportAsHTML){
    			sRedirect += "&" + TCQuerySelect.PARAM_EXPORTOPTIONS + "=" + TCQuerySelect.EXPORT_HTML_VALUE;
    		}
    		if (bIncludeBorder){
    			sRedirect += "&" + TCQuerySelect.PARAM_INCLUDEBORDER + "=1";
    		}
    		if (bAlternateRowColors){
    			sRedirect += "&" + TCQuerySelect.PARAM_ALTERNATEROWCOLORS + "=1";
    		}
    		if (bTotalNumericFields){
    			sRedirect += "&" + TCQuerySelect.PARAM_TOTALNUMERICFIELDS + "=1";
    		}
    		if (bShowSQLCommand){
    			sRedirect += "&" + TCQuerySelect.PARAM_SHOWSQLCOMMAND + "=1";
    		}
    		if (bHideHeaderFooter){
    			sRedirect += "&" + TCQuerySelect.PARAM_HIDEHEADERFOOTER + "=1";
    		}
    		if (bHideColumnLabels){
    			sRedirect += "&" + TCQuerySelect.PARAM_HIDECOLUMNLABELS + "=1";
    		}
    		
			sRedirect += "&Warning=" + sWarning
			;
    		response.sendRedirect(sRedirect);
        	return;
    	}
    	
    	//Display the query with parameters replaced:
    	if (bDebugMode){
    		try {
    			out.println("<BR>" + replaceQueryParameters(sQueryString, request, out) + "<BR>");
    		} catch (Exception e) {
    			out.println("<BR>Error getting parameters - " + e.getMessage() + "<BR>");
    		}
    	}
    	try {
    		sQueryString = replaceQueryParameters(sQueryString, request, out);
		} catch (Exception e) {
			out.println("<BR>Error [1416325303] reading parameters - " + e.getMessage() + "<BR>");
		}
    	TCCustomQuery qry = new TCCustomQuery();
    	//Font sizes:
    	if (!qry.processReport(
    			conn, 
    			sQueryID,
    			sQueryTitle, 
    			sQueryString,
    			sRawQueryString,
    			"", 
    			sUserID, 
    			out, 
    			bExportAsCommaDelimited,
    			bIncludeBorder,
    			sFontSize,
    			bAlternateRowColors,
    			bTotalNumericFields,
    			bShowSQLCommand,
    			bHideHeaderFooter,
    			bHideColumnLabels,
    			getServletContext())){
    		
    		out.println("Could not print query '" + sQueryString + "' - " + qry.getErrorMessage());
    	}
    	clsDatabaseFunctions.freeConnection(getServletContext(), conn);
	    out.println("</BODY></HTML>");
	}
	private String replaceQueryParameters(String sQuery, HttpServletRequest req, PrintWriter pwOut) throws Exception{
		
		String s = sQuery;
		ArrayList <String>sQueryParameterNames = new ArrayList<String>(0);
		ArrayList <String>sTextToBeReplaced = new ArrayList<String>(0);
		loadQueryParameters(sQuery, sTextToBeReplaced, pwOut);
		
		Enumeration <String> e = req.getParameterNames();
		String sParam = "";
		while (e.hasMoreElements()){
			sParam = e.nextElement();
			//If the parameter contains EITHER the 'query parameter base' OR the query DATE PICKER parameter base, add
			//it to the list of parameter names:
			if (sParam.contains(TCQueryParameters.QUERYPARAMBASE)){
				sQueryParameterNames.add(sParam);
				if (bDebugMode){
					pwOut.println("<BR>sParam = '" + sParam
						+ "', parameter value = '" + clsManageRequestParameters.get_Request_Parameter(sParam, req) + "'.");
				}
			}
			if (sParam.contains(TCQueryParameters.QUERYDATEPICKERPARAMBASE)){
				sQueryParameterNames.add(sParam);
				if (bDebugMode){
					pwOut.println("<BR>sParam = '" + sParam
						+ "', parameter value = '" + clsManageRequestParameters.get_Request_Parameter(sParam, req) + "'.");
				}
			}
		}
		Collections.sort(sQueryParameterNames);
		for (int i = 0; i < sTextToBeReplaced.size(); i++){
			String sParameterValue = clsManageRequestParameters.get_Request_Parameter(sQueryParameterNames.get(i), req);
			String sTextToReplace = sTextToBeReplaced.get(i);
			if (sTextToReplace.substring(0,TCCustomQuery.DATEPICKER_PARAM_VARIABLE.length())
				.compareToIgnoreCase(TCCustomQuery.DATEPICKER_PARAM_VARIABLE) == 0){
				//s = s.replace(SMCustomQuery.STARTINGPARAMDELIMITER + sTextToReplace 
				//		+ SMCustomQuery.ENDINGPARAMDELIMITER, 
				//		"str_to_date('" + sParameterValue + "','%m/%d/%Y')");
				s = s.replace(TCCustomQuery.STARTINGPARAMDELIMITER + sTextToReplace 
						+ TCCustomQuery.ENDINGPARAMDELIMITER, 
						sParameterValue);

			}else{
				s = s.replace(TCCustomQuery.STARTINGPARAMDELIMITER + sTextToReplace
					+ TCCustomQuery.ENDINGPARAMDELIMITER, 
					sParameterValue);
			}
			if (bDebugMode){
				pwOut.println("<BR>Text to be replaced = '" + sTextToReplace
					+ "', parameter value = '" 
					+ sParameterValue + "'.");
			}
		}
		return s;
	}
	private void loadQueryParameters (String sQueryString, ArrayList<String> arrParams, PrintWriter out) throws Exception{
		
		try {
			Pattern p = Pattern.compile(clsStringFunctions.convertStringToRegex(TCCustomQuery.STARTINGPARAMDELIMITER));
			String[] x = p.split(sQueryString);
			for (int i=0; i<x.length; i++) {
				int iEnd = x[i].indexOf(TCCustomQuery.ENDINGPARAMDELIMITER);
				if (iEnd > -1){
					//But DON'T replace the 'SET VARIABLES' command:
					if (!x[i].contains(TCCustomQuery.SETVARIABLECOMMAND)){
						arrParams.add(x[i].substring(0, iEnd));
						if (bDebugMode){
							out.println("<BR>Param " + (i + 1) + " = '" + arrParams.get(arrParams.size() - 1) + "'." );
						}
					}
				}
			}
		} catch (Exception e) {
			throw e;
		}
		return;
	}
	public void doGet(HttpServletRequest request,
			HttpServletResponse response)
	throws ServletException, IOException {

		doPost(request, response);
	}
}
