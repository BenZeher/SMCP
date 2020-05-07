package smcontrolpanel;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
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

import ConnectionPool.WebContextParameters;
import SMDataDefinition.SMTablesavedqueries;
import ServletUtilities.clsServletUtilities;
import ServletUtilities.clsCreateHTMLFormFields;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsManageRequestParameters;
import ServletUtilities.clsStringFunctions;

public class SMQueryGenerate extends HttpServlet {

	private static final long serialVersionUID = 1L;
	private static final String sQuerySaveClass = "smcontrolpanel.SMSaveQueryAction";
	public static final String SAVE_AS_PRIVATE_BUTTON = "SavePrivateQueryButton";
	public static final String SAVE_AS_PRIVATE_BUTTON_LABEL = "Save new private query";
	public static final String SAVE_AS_PUBLIC_BUTTON = "SavePublicQueryButton";
	public static final String SAVE_AS_PUBLIC_BUTTON_LABEL = "Save new public query";
	public static final String UPDATE_EXISTING_QUERY_BUTTON = "UpdateExistingQuery";
	public static final String UPDATE_EXISTING_QUERY_BUTTON_LABEL = "Update existing query";
	
	//formats
	private static SimpleDateFormat USDateformatter = new SimpleDateFormat("MM-dd-yyyy hh:mm:ss a EEE");
	
	private boolean bDebugMode = false;

	//private static SimpleDateFormat USTimeOnlyformatter = new SimpleDateFormat("hh:mm:ss a");
	
	public void doPost(HttpServletRequest request,
				HttpServletResponse response)
				throws ServletException, IOException {
		
		if (!SMAuthenticate.authenticateSMCPCredentials(
				request, 
				response, 
				getServletContext(),
				SMSystemFunctions.SMQuerySelector
			)
		){
			return;
		}
	    response.setContentType("text/html");
		PrintWriter out = response.getWriter();
		
	    //Get the session info:
	    HttpSession CurrentSession = request.getSession(true);
	    String sDBID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_DATABASE_ID);
	    String sUserName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERNAME);
	    String sUserID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERID);
	    String sUserFirstName = (String)CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERFIRSTNAME);
	    String sUserLastName = (String)CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERLASTNAME);	    
	    String sCompanyName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_COMPANYNAME);
	    
	    //sCallingClass will look like: smcontrolpanel.ARAgedTrialBalanceReport
	    String sCallingClass = clsManageRequestParameters.get_Request_Parameter("CallingClass", request);
	    String sWarning = "";
		String sQueryTitle = clsManageRequestParameters.get_Request_Parameter(SMQuerySelect.PARAM_QUERYTITLE, request);
		String sQueryID = clsManageRequestParameters.get_Request_Parameter(SMQuerySelect.PARAM_QUERYID, request);
		String sQueryString = clsServletUtilities.URLDecode(clsManageRequestParameters.get_Request_Parameter(SMQuerySelect.PARAM_QUERYSTRING, request));
		String sRawQueryString = clsServletUtilities.URLDecode(clsManageRequestParameters.get_Request_Parameter(SMQuerySelect.PARAM_RAWQUERYSTRING, request));
		String sFontSize = clsManageRequestParameters.get_Request_Parameter(SMQuerySelect.PARAM_FONTSIZE, request);
		String sComment = clsManageRequestParameters.get_Request_Parameter(SMQuerySelect.PARAM_QUERYCOMMENT, request);
		boolean bIncludeBorder = (request.getParameter(SMQuerySelect.PARAM_INCLUDEBORDER) != null);
    	String sReportTitle = "Service Manager Query: " + sQueryTitle;  
    	String sCriteria = getParameterPromptsAndValues(request);

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
	    boolean bHideColumnLabels = (request.getParameter(SMQuerySelect.PARAM_HIDECOLUMNLABELS) != null);
	    
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
		       + "<TITLE>" + sReportTitle + " - " + sCompanyName + "</TITLE></HEAD>\n<BR>" + "\n"
		       + "<BODY BGCOLOR=\"#FFFFFF\">" + "\n"
			   + "<TABLE BORDER=0 WIDTH=100%>" + "\n"
			   + "  <TR>" + "\n"
			   + "    <TD ALIGN=LEFT WIDTH=45%><FONT SIZE=2>" 
			   + USDateformatter.format((new Timestamp(System.currentTimeMillis()))) + " Printed by " + SMUtilities.getFullNamebyUserID(sUserID, getServletContext(), sDBID, sCallingClass) 
			   + "</FONT></TD>" + "\n"
			   + "    <TD ALIGN=CENTER WIDTH=55%><FONT SIZE=2><B>" + sCompanyName + "</B></FONT></TD>" + "\n"
			   + "  </TR>" + "\n"
			   + "  <TR>" + "\n"
			   + "    <TD VALIGN=BOTTOM COLSPAN=2><FONT SIZE=2><B>" + sReportTitle + "</B></FONT></TD>" + "\n"
			   + "  </TR>" + "\n"
			  );
					   
	    	out.println("  <TD><A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMUserLogin?" 
				+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
				+ "\">Return to user login</A><BR>");
		    out.println("<A HREF=\"" + WebContextParameters.getdocumentationpageURL(getServletContext()) + "#" + Long.toString(SMSystemFunctions.SMQuerySelector) 
		    		+ "\">Summary</A><BR><BR>");
			out.println("</TD>" + "\n"
		    		+ "  </TR>" + "\n"
					+ "</TABLE>");
			   }
	    }
	    
	    //Build a form to save the query here:
	    if (!bExportAsCommaDelimited && !bExportAsHTML && !bHideHeaderFooter){
			out.println ("<FORM ACTION =\"" + SMUtilities.getURLLinkBase(getServletContext()) 
					+ sQuerySaveClass + "\" METHOD='POST'>");
			    
			//Store hidden variables:
		    out.println("<INPUT TYPE=HIDDEN NAME=\"" 
				+ SMQuerySelect.PARAM_QUERYSTRING
				//TODO - test this:
				//+ "\" VALUE=\"" + SMUtilities.filter(sQueryString)
				+ "\" VALUE=\"" + clsServletUtilities.URLEncode(sQueryString)
				+ "\">" + "\n");
			
		    out.println("<INPUT TYPE=HIDDEN NAME=\"" 
				+ SMQuerySelect.PARAM_RAWQUERYSTRING
				+ "\" VALUE=\"" + clsServletUtilities.URLEncode(sRawQueryString)
				+ "\">" + "\n");
		    
			out.println("<INPUT TYPE=HIDDEN NAME=\"" 
				+ SMQuerySelect.PARAM_QUERYTITLE
				+ "\" VALUE=\"" + clsStringFunctions.filter(sQueryTitle)
				+ "\">" + "\n");
			
			out.println("<INPUT TYPE=HIDDEN NAME=\"" 
					+ SMQuerySelect.PARAM_FONTSIZE
					+ "\" VALUE=\"" + clsStringFunctions.filter(sFontSize)
					+ "\">" + "\n");
			
			if (bAlternateRowColors){
				out.println("<INPUT TYPE=HIDDEN NAME=\"" 
					+ SMQuerySelect.PARAM_ALTERNATEROWCOLORS
					+ "\" VALUE=\"" + "1"
					+ "\">" + "\n");
			}
			if (bExportAsHTML){
				out.println("<INPUT TYPE=HIDDEN NAME=\"" 
					+ SMQuerySelect.PARAM_EXPORTOPTIONS
					+ "\" VALUE=\"" + SMQuerySelect.EXPORT_HTML_VALUE
					+ "\">" + "\n");
			}
			if (bExportAsCommaDelimited){
				out.println("<INPUT TYPE=HIDDEN NAME=\"" 
					+ SMQuerySelect.PARAM_EXPORTOPTIONS
					+ "\" VALUE=\"" + SMQuerySelect.EXPORT_COMMADELIMITED_VALUE
					+ "\">" + "\n");
			}
    		if (bIncludeBorder){
				out.println("<INPUT TYPE=HIDDEN NAME=\"" 
						+ SMQuerySelect.PARAM_INCLUDEBORDER
						+ "\" VALUE=\"" + "1"
						+ "\">" + "\n");
    		}
    		if (bTotalNumericFields){
				out.println("<INPUT TYPE=HIDDEN NAME=\"" 
						+ SMQuerySelect.PARAM_TOTALNUMERICFIELDS
						+ "\" VALUE=\"" + "1"
						+ "\">" + "\n");
    		}
    		if (bShowSQLCommand){
				out.println("<INPUT TYPE=HIDDEN NAME=\"" 
						+ SMQuerySelect.PARAM_SHOWSQLCOMMAND
						+ "\" VALUE=\"" + "1"
						+ "\">" + "\n");
    		}
    		if (bHideHeaderFooter){
				out.println("<INPUT TYPE=HIDDEN NAME=\"" 
						+ SMQuerySelect.PARAM_HIDEHEADERFOOTER
						+ "\" VALUE=\"" + "1"
						+ "\">" + "\n");
    		}
    		if (bHideColumnLabels){
				out.println("<INPUT TYPE=HIDDEN NAME=\"" 
						+ SMQuerySelect.PARAM_HIDECOLUMNLABELS
						+ "\" VALUE=\"" + "1"
						+ "\">" + "\n");
    		}
        	if	(sQueryTitle.trim().compareToIgnoreCase("") == 0){
        		sQueryTitle="(Default Query Title)";
        	}
			out.println("<INPUT TYPE=HIDDEN NAME='" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "' VALUE='" + sDBID + "'>" + "\n");
			out.println("<INPUT TYPE=HIDDEN NAME=CallingClass VALUE=\"" 
					+ "smcontrolpanel.SMQuerySelect" + "\">" + "\n");
			
			if(!sQueryTitle.trim().equalsIgnoreCase("(Default Query Title)")) {
					
				out.println("<B>Query ID #:&nbsp;" + sQueryID + "</B><BR>" + "\n");
				out.println("<INPUT TYPE=HIDDEN NAME='" + SMQuerySelect.PARAM_QUERYID + "' VALUE='" + sQueryID + "'>" + "\n");
				out.println("Comment:<BR>"
					+ clsCreateHTMLFormFields.TDMemoBox(
						SMTablesavedqueries.scomment, 
						2, 
						120, 
						sComment, 
						"")
				);
				
				out.println("<INPUT TYPE=SUBMIT NAME='" + SAVE_AS_PRIVATE_BUTTON 
						+ "' VALUE='" + SAVE_AS_PRIVATE_BUTTON_LABEL + "' STYLE='height: 0.24in'>"
				);
				if (SMSystemFunctions.isFunctionPermitted(
						SMSystemFunctions.SMManagePublicQueries, 
						sUserID, 
						getServletContext(), 
						sDBID,
						(String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_LICENSE_MODULE_LEVEL))){
					out.println("<INPUT TYPE=SUBMIT NAME='" + SAVE_AS_PUBLIC_BUTTON 
							+ "' VALUE='" + SAVE_AS_PUBLIC_BUTTON_LABEL + "' STYLE='height: 0.24in'>"
					);			
					if(sQueryID.compareToIgnoreCase("") != 0) {
						out.println("<INPUT TYPE=SUBMIT NAME='" + UPDATE_EXISTING_QUERY_BUTTON 
								+ "' VALUE='" + UPDATE_EXISTING_QUERY_BUTTON_LABEL + "' STYLE='height: 0.24in'>"
						);
					}
					
				}
		    }
			out.println("</FORM>");
			
		    out.println("<TABLE BORDER=0 WIDTH=100%>\n"
			   + "  <TR>\n"
		       + "    <TD><FONT SIZE=2>" + sCriteria + "</FONT></TD>\n"
		       + "  </TR>\n"
			   + "  <TR>\n"
		       + "    <TD style = \" text-align:center; font-size: x-large; font-weight:bold;  \">" + sQueryTitle + "</TD>\n"
		       + "  </TR>\n"
			   + "</TABLE>\n"
		       + "<BR>"
		    );
	    }
	    
    	//Retrieve information
    	Connection conn = clsDatabaseFunctions.getConnection(
    			getServletContext(), 
    			sDBID, 
    			"MySQL", 
    			SMUtilities.getFullClassName(this.toString()) 
    			+ " - userID: " 
    			+ sUserID
    			+ " - "
    			+ sUserFirstName
    			+ " "
    			+ sUserLastName
    	);
    	if (conn == null){
    		sWarning = "Unable to get data connection.";
    		String sRedirect = SMUtilities.getURLLinkBase(getServletContext()) + "" + sCallingClass + "?"
    		+ "" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
			+ "&" + SMQuerySelect.PARAM_QUERYTITLE + "=" + sQueryTitle
			+ "&" + SMQuerySelect.PARAM_FONTSIZE + "=" + sFontSize
			;
    		if (bExportAsCommaDelimited){
    			sRedirect += "&" + SMQuerySelect.PARAM_EXPORTOPTIONS + "=" + SMQuerySelect.EXPORT_COMMADELIMITED_VALUE;
    		}
     		if (bExportAsHTML){
    			sRedirect += "&" + SMQuerySelect.PARAM_EXPORTOPTIONS + "=" + SMQuerySelect.EXPORT_HTML_VALUE;
    		}
    		if (bIncludeBorder){
    			sRedirect += "&" + SMQuerySelect.PARAM_INCLUDEBORDER + "=1";
    		}
    		if (bAlternateRowColors){
    			sRedirect += "&" + SMQuerySelect.PARAM_ALTERNATEROWCOLORS + "=1";
    		}
    		if (bTotalNumericFields){
    			sRedirect += "&" + SMQuerySelect.PARAM_TOTALNUMERICFIELDS + "=1";
    		}
    		if (bShowSQLCommand){
    			sRedirect += "&" + SMQuerySelect.PARAM_SHOWSQLCOMMAND + "=1";
    		}
       		if (bHideHeaderFooter){
    			sRedirect += "&" + SMQuerySelect.PARAM_HIDEHEADERFOOTER + "=1";
    		}
       		if (bHideColumnLabels){
    			sRedirect += "&" + SMQuerySelect.PARAM_HIDECOLUMNLABELS + "=1";
    		}


			sRedirect += "&Warning=" + sWarning
			;
    		
    		response.sendRedirect(sRedirect);
        	return;
    	}

    	if (
    	(sQueryString.trim().compareToIgnoreCase("") == 0)
    	){
    		clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1547080654]");
    		sWarning = "You must enter a query string.";
    		String sRedirect = SMUtilities.getURLLinkBase(getServletContext()) + "" + sCallingClass + "?"
    		+ "" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
			+ "&" + SMQuerySelect.PARAM_QUERYTITLE + "=" + sQueryTitle
			+ "&" + SMQuerySelect.PARAM_FONTSIZE + "=" + sFontSize
			;
    		if (bExportAsCommaDelimited){
    			sRedirect += "&" + SMQuerySelect.PARAM_EXPORTOPTIONS + "=" + SMQuerySelect.EXPORT_COMMADELIMITED_VALUE;
    		}
     		if (bExportAsHTML){
    			sRedirect += "&" + SMQuerySelect.PARAM_EXPORTOPTIONS + "=" + SMQuerySelect.EXPORT_HTML_VALUE;
    		}
    		if (bIncludeBorder){
    			sRedirect += "&" + SMQuerySelect.PARAM_INCLUDEBORDER + "=1";
    		}
    		if (bAlternateRowColors){
    			sRedirect += "&" + SMQuerySelect.PARAM_ALTERNATEROWCOLORS + "=1";
    		}
    		if (bTotalNumericFields){
    			sRedirect += "&" + SMQuerySelect.PARAM_TOTALNUMERICFIELDS + "=1";
    		}
    		if (bShowSQLCommand){
    			sRedirect += "&" + SMQuerySelect.PARAM_SHOWSQLCOMMAND + "=1";
    		}
    		if (bHideHeaderFooter){
    			sRedirect += "&" + SMQuerySelect.PARAM_HIDEHEADERFOOTER + "=1";
    		}
    		if (bHideColumnLabels){
    			sRedirect += "&" + SMQuerySelect.PARAM_HIDECOLUMNLABELS + "=1";
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
    	SMCustomQuery qry = new SMCustomQuery();
    	
    	//First, check permissions before even starting the query:
    	try {
			qry.checkTablePermissions(
				sUserID, 
				getServletContext(), 
				sDBID, 
				(String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_LICENSE_MODULE_LEVEL), 
				sQueryString);
		} catch (Exception e) {
			out.println("Could not print query - " + e.getMessage());
			out.println("</BODY></HTML>");
			return;
		}
    	
    	//Font sizes:
    	if (!qry.processReport(
    			conn, 
    			sDBID,
    			sQueryID,
    			sQueryTitle, 
    			sQueryString,
    			sRawQueryString,
    			sUserName,
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
    			getServletContext(),
    			(String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_LICENSE_MODULE_LEVEL)
    		)
    	){
    		
    		out.println("Could not print query '" + sQueryString + "' - " + qry.getErrorMessage());
    	}
    	clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1547080655]");
	    out.println("</BODY></HTML>");
	    return;
	}
	private String replaceQueryParameters(String sQuery, HttpServletRequest req, PrintWriter pwOut) throws Exception{
		
		String s = sQuery;
		ArrayList <String>sQueryParameterNames = new ArrayList<String>(0);
		ArrayList <String>sTextToBeReplaced = new ArrayList<String>(0);
		
		//Replace the obsolete 'SESSIONTAG' with a blank:
		// TJR - 12/21/2018
		//System.out.println("[1545410100] - " + sQuery);
		sQuery = sQuery.replaceAll(SMCustomQuery.SESSION_TAG_PARAM_VARIABLE, "");
		//System.out.println("[1545410101] - " + sQuery);
		
		loadQueryParameters(sQuery, sTextToBeReplaced, pwOut);
		
		Enumeration <String> e = req.getParameterNames();
		String sParam = "";
		while (e.hasMoreElements()){
			sParam = e.nextElement();
			//If the parameter contains EITHER the 'query parameter base' OR the query DATE PICKER parameter base, add
			//it to the list of parameter names:
			if (sParam.contains(SMQueryParameters.QUERYPARAMBASE)){
				//System.out.println("[202004294549] - sParam = '" + sParam + "'");
				sQueryParameterNames.add(sParam);
				if (bDebugMode){
					pwOut.println("<BR>sParam = '" + sParam
						+ "', parameter value = '" + clsManageRequestParameters.get_Request_Parameter(sParam, req) + "'.");
				}
			}
			if (sParam.contains(SMQueryParameters.QUERYDATEPICKERPARAMBASE)){
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
			//System.out.println("[202004294823] - sParameterValue = '" + sParameterValue + "'");
			//System.out.println("[202004294842] - sTextToReplace = '" + sTextToReplace + "'");
			if (sTextToReplace.substring(0,SMCustomQuery.DATEPICKER_PARAM_VARIABLE.length())
				.compareToIgnoreCase(SMCustomQuery.DATEPICKER_PARAM_VARIABLE) == 0){
				//s = s.replace(SMCustomQuery.STARTINGPARAMDELIMITER + sTextToReplace 
				//		+ SMCustomQuery.ENDINGPARAMDELIMITER, 
				//		"str_to_date('" + sParameterValue + "','%m/%d/%Y')");
				s = s.replace(SMCustomQuery.STARTINGPARAMDELIMITER + sTextToReplace 
						+ SMCustomQuery.ENDINGPARAMDELIMITER, 
						clsDatabaseFunctions.FormatSQLStatement(sParameterValue).trim());

			}else{
				s = s.replace(SMCustomQuery.STARTINGPARAMDELIMITER + sTextToReplace
					+ SMCustomQuery.ENDINGPARAMDELIMITER, 
					clsDatabaseFunctions.FormatSQLStatement(sParameterValue).trim());
			}
			
			if (bDebugMode){
				pwOut.println("<BR>Text to be replaced = '" + sTextToReplace
					+ "', parameter value = '" 
					+ clsDatabaseFunctions.FormatSQLStatement(sParameterValue).trim() + "'.");
			}
		}
		return s;
	}
	private void loadQueryParameters (String sQueryString, ArrayList<String> arrParams, PrintWriter out) throws Exception{
		//System.out.println("[202004294305] - sQueryString = '" + sQueryString + "'");
		try {
			Pattern p = Pattern.compile(clsStringFunctions.convertStringToRegex(SMCustomQuery.STARTINGPARAMDELIMITER));
			String[] x = p.split(sQueryString);
			for (int i=0; i<x.length; i++) {
				int iEnd = x[i].indexOf(SMCustomQuery.ENDINGPARAMDELIMITER);
				if (iEnd > -1){
					//But DON'T replace the 'SET VARIABLES' command:
					if (!x[i].contains(SMCustomQuery.SETVARIABLECOMMAND)){
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
	private String getParameterPromptsAndValues(HttpServletRequest req){
		
		String sCriteria = "";
		ArrayList<String>arrQueryParameterNames = new ArrayList<String>(0);
		Enumeration <String> e = req.getParameterNames();
		String sParam = "";
		while (e.hasMoreElements()){
			sParam = e.nextElement();
			//If the parameter contains EITHER the 'query parameter base' OR the query DATE PICKER parameter base, add
			//it to the list of parameter names:
			if (sParam.contains(SMQueryParameters.QUERYPARAMBASE)){
				//But we don't want to include the 'QUERYDROPDOWNCHOICEBASE' parameters:
				if (!sParam.contains(SMQueryParameters.QUERYDROPDOWNCHOICEBASE)){
					arrQueryParameterNames.add(sParam);
				}
			}
			if (sParam.contains(SMQueryParameters.QUERYDATEPICKERPARAMBASE)){
				if (!sParam.contains(SMQueryParameters.QUERYDROPDOWNCHOICEBASE)){
					arrQueryParameterNames.add(sParam);
				}
			}
		}
		Collections.sort(arrQueryParameterNames);
		
		//Now build the list of criteria:
		for (int i = 0; i < arrQueryParameterNames.size(); i++){
			//System.out.println("[1553031177] - arrQueryParameterNames.get(i) = '" + arrQueryParameterNames.get(i) + "'");
			//If the control on the form was NOT a drop down list, then the user's choice was simply whatever was placed in the text field:
			String sUserChoice = clsManageRequestParameters.get_Request_Parameter(arrQueryParameterNames.get(i), req);
			//But if the control was a DROP DOWN LIST, then we have to go get the actual user's choice, because it may be different than the 'value' of the control:
			
			//See if we have a parameter whose name includes the control's name AND the select value:
			String sDropDownChoiceParam = SMQueryParameters.QUERYDROPDOWNCHOICEBASE + arrQueryParameterNames.get(i) + clsManageRequestParameters.get_Request_Parameter(arrQueryParameterNames.get(i), req).trim();
			//System.out.println("[1553031178] - sDropDownChoiceParam = '" + sDropDownChoiceParam + "'");
			if (req.getParameter(sDropDownChoiceParam) != null){
				sUserChoice = clsManageRequestParameters.get_Request_Parameter(sDropDownChoiceParam, req);
				//System.out.println("[1553031179] - sUserChoice = '" + sUserChoice + "'");
			}
			
			sCriteria += clsManageRequestParameters.get_Request_Parameter(
					arrQueryParameterNames.get(i).replaceAll(
						SMQueryParameters.QUERYPARAMBASE, SMQueryParameters.QUERYPARAMPROMPTBASE).replaceAll(
							SMQueryParameters.QUERYDATEPICKERPARAMBASE, SMQueryParameters.QUERYPARAMPROMPTBASE), req)
				+ " <B>"
				+ sUserChoice
				+ "</B>"
				+ "<BR>"
			;
		}
		if (sCriteria.compareToIgnoreCase("") != 0){
			sCriteria = "<BR><I><U><B>Selection Criteria:</B></U></I><BR>" + sCriteria;
		}
		return sCriteria;
	}
	public void doGet(HttpServletRequest request,
			HttpServletResponse response)
	throws ServletException, IOException {

		doPost(request, response);
	}
}
