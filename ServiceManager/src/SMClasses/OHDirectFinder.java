package SMClasses;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import SMDataDefinition.SMOHDirectFieldDefinitions;
import ServletUtilities.clsCreateHTMLFormFields;
import ServletUtilities.clsCreateHTMLTableFormFields;
import ServletUtilities.clsDateAndTimeConversions;
import ServletUtilities.clsManageRequestParameters;
import ServletUtilities.clsServletUtilities;
import smcontrolpanel.SMAuthenticate;
import smcontrolpanel.SMUtilities;

public class OHDirectFinder extends HttpServlet {
	private static final long serialVersionUID = 1L;
	public static final int MAX_NUMBER_OF_RESULT_FIELDS = 20;
	public static final int MAX_NUMBER_OF_SEARCH_FIELDS = 20;
	public static final String CREATED_START_DATE_PARAM = "datStartCreated";
	public static final String CREATED_END_DATE_PARAM = "datEndCreated";
	public static final String LAST_MODIFIED_START_DATE_PARAM = "datEndLastModified";
	public static final String LAST_MODIFIED_END_DATE_PARAM = "datStartLastModified";
	public static final String SEARCH_JOB_TEXT_PARAM = "sSeachJobText";
	
	
	private static final boolean bDebugMode = false;
	public void doPost(HttpServletRequest request,
				HttpServletResponse response)
				throws ServletException, IOException {
		
		/*
		How to link to the finder class:
		Add a link to your page that looks like this: 
		out.println("<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "SMClasses.OHDirectFinder"
			+ "?EndpointName=SMOHDirectFieldDefinitions.ENDPOINT_QUOTE"
			+ "&SearchingClass=smcontrolpanel.SMEditSMSummaryEdit"
			+ "&ReturnField=VENDORQUOTENUMBER"
			+ "&ParameterString=Batchnumber=1&EntryNumber=3
			+ "&ResultListField1="  + SMOHDirectFieldDefinitions.QUOTE_FIELD_QUOTENUMBER
			+ "&ResultHeading1=Quote%20Number"
			+ "&ResultListField2="  + SMOHDirectFieldDefinitions.QUOTE_FIELD_QUOTENUMBER
			+ "&ResultHeading2=Quote%20Number"
			+ "&ResultListField3="  + SMOHDirectFieldDefinitions.QUOTE_FIELD_QUOTENUMBER
			+ "&ResultHeading3=Quote%20Number"
			+ "&ResultListField4="  + SMOHDirectFieldDefinitions.QUOTE_FIELD_QUOTENUMBER
			+ "&ResultHeading4=Quote%20Number"
			+ "&ResultListField5="  + SMOHDirectFieldDefinitions.QUOTE_FIELD_QUOTENUMBER
			+ "&ResultHeading5=Quote%20Number"
			+ "&ResultListField6="  + SMOHDirectFieldDefinitions.QUOTE_FIELD_QUOTENUMBER
			+ "&ResultHeading6=Quote%20Number"
			+ "\"> Find Quote</A></P>");
			
		Definitions of parameters:
		EndPointName - what you are searching for - in this case, a quote
		SearchingClass - the name of the class that is initiating the search.  This class will be re-called when the
			user finally clicks one of the search results.
		ReturnField - the name of the field in the 'SearchingClass' that will carry the resulting value when 
			the user clicks on a choice in the results list
		ResultListField1 (,2,3 . . . . up to 10) - the name of the field defined by the API that will appear in the
			list of results.
		ResultHeading1	(,2,3 . . . . up to 10) - the headings for each of the corresponding data fields displayed
			in the list of results
		 
		 local test link
		http://localhost:8080/sm/SMClasses.OHDirectFinder
		?EndpointName=C_DealerQuote
		&SearchingClass=smcontrolpanel.SMEditSMSummaryEdit
		&ReturnField=VENDORQUOTENUMBER
		&ResultListField1=C_QuoteNumberString
		&ResultListField2=C_Name
		&ResultListField3=C_CreatedDate
		&ResultListField4=C_LastModifiedDate
		&ResultHeading1=Quote%20Number
		&ResultHeading2=Job%20Name
		&ResultHeading3=Created%20Date
		&ResultHeading4=Last%20Modified%20Date
		 */
		
		if (!SMAuthenticate.authenticateSMCPCredentials(
				request, 
				response, 
				getServletContext(), 
				-1L)
		){
			return;
		}
		PrintWriter out = response.getWriter();
	    //Get the session info:
	    HttpSession CurrentSession = request.getSession(true);
	    String sCompanyName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_COMPANYNAME);

	    //Get the parameters:
	    String sEndPointName = clsManageRequestParameters.get_Request_Parameter(OHDirectFinderResults.FINDER_ENDPOINT_NAME_PARAM, request);
	    String sListFormat = clsManageRequestParameters.get_Request_Parameter(OHDirectFinderResults.FINDER_LIST_FORMAT_PARAM, request);
	    String sSearchingClass = clsManageRequestParameters.get_Request_Parameter(OHDirectFinderResults.FINDER_SEARCHING_CLASS_PARAM, request);
	    String sReturnField = clsManageRequestParameters.get_Request_Parameter(OHDirectFinderResults.FINDER_RETURN_FIELD_PARAM, request);
	    String sAdditionalParameterString = clsManageRequestParameters.get_Request_Parameter(OHDirectFinderResults.FINDER_RETURN_ADDITIONAL_PARAMS, request);
	    
	    String sDBID = ServletUtilities.clsManageRequestParameters.get_Request_Parameter(SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID, request);
	    //The DBID should always be passed in by the request, so there's no conflict with an existing session.
	    //But just in case it's NOT, we'll get it from the session as a last resort...
	    if (sDBID.compareToIgnoreCase("") == 0){
	    	sDBID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_DATABASE_ID);
	    }
	    String title = "Find " + sEndPointName + ".";
	    if(sEndPointName.compareToIgnoreCase(SMOHDirectFieldDefinitions.ENDPOINT_QUOTE) == 0) {
	    	title = "Find " + SMOHDirectFieldDefinitions.ENDPOINT_QUOTE_NAME + ".";
	    }
	    if(sEndPointName.compareToIgnoreCase(SMOHDirectFieldDefinitions.ENDPOINT_ORDER) == 0) {
	    	title = "Find " + SMOHDirectFieldDefinitions.ENDPOINT_ORDER_NAME + ".";
	    }
	    String subtitle = "";
	    out.println(SMUtilities.SMCPTitleSubBGColor(title, subtitle, SMUtilities.getInitBackGroundColor(getServletContext(), sDBID), sCompanyName));
	    out.println(clsServletUtilities.getDatePickerIncludeString(getServletContext()));
	    out.println("<FORM NAME='MAINFORM' ACTION='" 
	    		+ SMUtilities.getURLLinkBase(getServletContext()) 
	    		+ "SMClasses.OHDirectFinderResults" + "' METHOD='POST'>");
	    out.println("<INPUT TYPE=HIDDEN NAME='" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "' VALUE='" + sDBID + "'>");
	    out.println("<INPUT TYPE=HIDDEN NAME=\"" + OHDirectFinderResults.FINDER_ENDPOINT_NAME_PARAM + "\" VALUE=\"" + sEndPointName + "\">");
	    out.println("<INPUT TYPE=HIDDEN NAME=\"" + OHDirectFinderResults.FINDER_SEARCHING_CLASS_PARAM + "\" VALUE=\"" + sSearchingClass + "\">");
	    out.println("<INPUT TYPE=HIDDEN NAME=\"" + OHDirectFinderResults.FINDER_RETURN_FIELD_PARAM + "\" VALUE=\"" + sReturnField + "\">");
	    out.println("<INPUT TYPE=HIDDEN NAME=\"" + OHDirectFinderResults.FINDER_RETURN_ADDITIONAL_PARAMS + "\" VALUE=\"" + sAdditionalParameterString + "\">");
	    out.println("<INPUT TYPE=HIDDEN NAME=\"" + OHDirectFinderResults.FINDER_LIST_FORMAT_PARAM + "\" VALUE=\"" + sListFormat + "\">");
	    
	    //Store the display fields and headings for the results page:
	    for (int i = 0; i < MAX_NUMBER_OF_RESULT_FIELDS; i++){
	    	if (bDebugMode){
	    		System.out.println("[1590756804] In " + this.toString() + " Result List Field for " + i + " = " 
	    				+ clsManageRequestParameters.get_Request_Parameter(OHDirectFinderResults.RESULT_LIST_FIELD + Integer.toString(i), request) );
	    	}
	    	if (request.getParameter(OHDirectFinderResults.RESULT_LIST_FIELD + Integer.toString(i)) != null){
	    		out.println("<INPUT TYPE=HIDDEN NAME=\"" + OHDirectFinderResults.RESULT_LIST_FIELD 
	    				+ Integer.toString(i) + "\" VALUE=\"" 
	    				+ request.getParameter(OHDirectFinderResults.RESULT_LIST_FIELD + Integer.toString(i)) + "\">");
	    	}
	    }

	    for (int i = 0; i < MAX_NUMBER_OF_RESULT_FIELDS; i++){
	    	if (request.getParameter(OHDirectFinderResults.RESULT_LIST_HEADING + Integer.toString(i)) != null){
	    		out.println("<INPUT TYPE=HIDDEN NAME=\"" + OHDirectFinderResults.RESULT_LIST_HEADING 
	    				+ Integer.toString(i) + "\" VALUE=\"" 
	    				+ request.getParameter(OHDirectFinderResults.RESULT_LIST_HEADING + Integer.toString(i)) + "\">");
	    	}
	    }
	    
	    String sOutPut = "";
	    sOutPut = "<TABLE BORDER=12 CELLSPACING=2>";

	    //Created date range
	    sOutPut += "<TR><TD ALIGN=RIGHT><B>Created date range:</B></TD>";
	    sOutPut += "<TD>";
	    sOutPut += clsCreateHTMLFormFields.TDTextBox(
	    		CREATED_START_DATE_PARAM, 
				"1/1/2000", 
				8, 
				10, 
				""
				) 
				+ SMUtilities.getDatePickerString(CREATED_START_DATE_PARAM, getServletContext())
				;
	    sOutPut += "&nbsp;&nbsp;To&nbsp;&nbsp;";
	    sOutPut += clsCreateHTMLFormFields.TDTextBox(
	    		CREATED_END_DATE_PARAM, 
    			clsDateAndTimeConversions.now("M/d/yyyy"), 
    			8, 
    			10, 
    			""
    			) 
    			+ SMUtilities.getDatePickerString(CREATED_END_DATE_PARAM, getServletContext())
    			;
	    sOutPut += "</TD><TD>Input as (mm/dd/yyyy)</TD></TR>";
	    
	    //Last modified date range
	    sOutPut += "<TR><TD ALIGN=RIGHT><B>Last modified date range:</B></TD>";
	    sOutPut += "<TD>";
	    sOutPut += clsCreateHTMLFormFields.TDTextBox(
	    		LAST_MODIFIED_START_DATE_PARAM, 
				"1/1/2000", 
				8, 
				10, 
				""
				) 
				+ SMUtilities.getDatePickerString(LAST_MODIFIED_START_DATE_PARAM, getServletContext())
				;
	    sOutPut += "&nbsp;&nbsp;To&nbsp;&nbsp;";
	    sOutPut += clsCreateHTMLFormFields.TDTextBox(
	    		LAST_MODIFIED_END_DATE_PARAM, 
    			clsDateAndTimeConversions.now("M/d/yyyy"), 
    			8, 
    			10, 
    			""
    			) 
    			+ SMUtilities.getDatePickerString(LAST_MODIFIED_END_DATE_PARAM, getServletContext())
    			;
	    sOutPut += "</TD><TD>Input as (mm/dd/yyyy)</TD></TR>";
	    
	    //Job search text:
	    sOutPut += clsCreateHTMLTableFormFields.Create_Edit_Form_Text_Input_Row(
	    		SEARCH_JOB_TEXT_PARAM, 
	    		"", 
	    		40, 
	    		"Search Job Name:", 
	    		"Enter the job name search string here",
	    		"30"
	    		);
	    
	    sOutPut += "</TABLE><BR>";
	    
	    sOutPut += "<P><INPUT TYPE=SUBMIT NAME='SubmitEdit' VALUE='Start search' STYLE='width: 2.00in; height: 0.24in'></P>";
	    
		sOutPut = sOutPut + "</FORM>";
		out.println(sOutPut);
		
		//Set the default focus:
		out.println("<script language=\"JavaScript\">");
		out.println("document.MAINFORM.sSearchString.focus();");
		out.println("</script>");
		
		out.println("</BODY></HTML>");
	}
	
	public void doGet(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
		
		doPost(request, response);
	}
}