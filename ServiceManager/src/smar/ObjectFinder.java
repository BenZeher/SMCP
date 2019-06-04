package smar;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import ServletUtilities.clsManageRequestParameters;
import smcontrolpanel.SMAuthenticate;
import smcontrolpanel.SMUtilities;

public class ObjectFinder extends HttpServlet {
	private static final long serialVersionUID = 1L;
	public static final int MAX_NUMBER_OF_RESULT_FIELDS = 20;
	public static final int MAX_NUMBER_OF_SEARCH_FIELDS = 20;
	public static final String DO_NOT_SHOW_MENU_LINK = "DONOTSHOWMENULINK";
	private static final boolean bDebugMode = false;
	public void doPost(HttpServletRequest request,
				HttpServletResponse response)
				throws ServletException, IOException {
		
		/*
		How to link to the finder class:
		Add a link to your page that looks like this: 
		out.println("<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smar.ObjectFinder"
			+ "?ObjectName=Customer"
			+ "&ResultClass=FinderResults"
			+ "&SearchingClass=smar.AREditCustomers"
			+ "&ReturnField=EditCode"
			+ "&ParameterString=Batchnumber=1&EntryNumber=3
			+ "&SearchField1=" + SMTablearcustomers.sCustomerName
			+ "&SearchFieldAlias1=Name"
			+ "&SearchField2=" + SMTablearcustomers.sCustomerNumber
			+ "&SearchFieldAlias2=Customer%20Code"
			+ "&SearchField3=" + SMTablearcustomers.sAddressLine1
			+ "&SearchFieldAlias3=Address%20Line%201"
			+ "&SearchField4=" + SMTablearcustomers.sPhoneNumber
			+ "&SearchFieldAlias4=Phone"
			+ "&ResultListField1="  + SMTablearcustomers.sCustomerNumber
			+ "&ResultHeading1=Customer%20Number"
			+ "&ResultListField2="  + SMTablearcustomers.sCustomerName
			+ "&ResultHeading2=Customer%20Name"
			+ "&ResultListField3="  + SMTablearcustomers.sAddressLine1
			+ "&ResultHeading3=Address%20Line%201"
			+ "&ResultListField4="  + SMTablearcustomers.sPhoneNumber
			+ "&ResultHeading4=Phone"
			+ "&ResultListField5="  + SMTablearcustomer.iActive
			+ "&ResultHeading5=Active"
			+ "&ResultListField6="  + SMTablearcustomer.iOnHold
			+ "&ResultHeading6=On%20Hold"
			+ "\"> Find customer</A></P>");
			
		Definitions of parameters:
		ObjectName - what you are searching for - in this case, customers
		ResultClass - the name of the result page you want to display after searching (typically, 'FinderResults')
		SearchingClass - the name of the class that is initiating the search.  This class will be re-called when the
			user finally clicks one of the search results.
		ReturnField - the name of the field in the 'SearchingClass' that will carry the resulting value when 
			the user clicks on a choice in the results list
		SearchField1 (,2,3 . . . . up to 10) - the name of the field(s) (in the database) that the user can use
			to search on.
		SearchFieldAlias1 (,2,3 . . . . up to 10) - the user-recognizable name that the user will see when selecting
			a field to search on (for example 'Customer name' for the 'sCustomerName' field.)
		ResultListField1 (,2,3 . . . . up to 10) - the name of the field in the database that will appear in the
			list of results.
		ResultHeading1	(,2,3 . . . . up to 10) - the headings for each of the corresponding data fields displayed
			in the list of results
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
	    String sObjectName = clsManageRequestParameters.get_Request_Parameter(FinderResults.FINDER_OBJECT_NAME_PARAM, request);
	    String sResultClass = clsManageRequestParameters.get_Request_Parameter(FinderResults.FINDER_RESULT_CLASS_PARAM, request);
	    String sDocumentType = clsManageRequestParameters.get_Request_Parameter(FinderResults.FINDER_DOC_TYPE_PARAM, request);
	    String sSearchingClass = clsManageRequestParameters.get_Request_Parameter(FinderResults.FINDER_SEARCHING_CLASS_PARAM, request);
	    String sReturnField = clsManageRequestParameters.get_Request_Parameter(FinderResults.FINDER_RETURN_FIELD_PARAM, request);
	    String sParameterString = "";
	    if (request.getParameter(FinderResults.FINDER_PARAMETER_STRING_PARAM) != null){
	    	sParameterString = request.getParameter(FinderResults.FINDER_PARAMETER_STRING_PARAM);
	    }
	    
	    String sDBID = ServletUtilities.clsManageRequestParameters.get_Request_Parameter(SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID, request);
	    //The DBID should always be passed in by the request, so there's no conflict with an existing session.
	    //But just in case it's NOT, we'll get it from the session as a last resort...
	    if (sDBID.compareToIgnoreCase("") == 0){
	    	sDBID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_DATABASE_ID);
	    }
	    
	    String title = "Find " + sObjectName + ".";
	    String subtitle = "";
	    out.println(SMUtilities.SMCPTitleSubBGColor(title, subtitle, SMUtilities.getInitBackGroundColor(getServletContext(), sDBID), sCompanyName));

	    //Print a link to the first page after login:
	    if (clsManageRequestParameters.get_Request_Parameter(ObjectFinder.DO_NOT_SHOW_MENU_LINK, request).compareToIgnoreCase("") == 0){
	    	out.println("<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMUserLogin?" 
				+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
				+ "\">Return to user login</A><BR>");
	    }
	    out.println("<FORM NAME='MAINFORM' ACTION='" 
	    		+ SMUtilities.getURLLinkBase(getServletContext()) 
	    		+ "smar." + sResultClass + "' METHOD='POST'>");
	    out.println("<INPUT TYPE=HIDDEN NAME='" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "' VALUE='" + sDBID + "'>");
	    out.println("<INPUT TYPE=HIDDEN NAME=\"ObjectName\" VALUE=\"" + sObjectName + "\">");
	    out.println("<INPUT TYPE=HIDDEN NAME=\"SearchingClass\" VALUE=\"" + sSearchingClass + "\">");
	    out.println("<INPUT TYPE=HIDDEN NAME=\"DocumentType\" VALUE=\"" + sDocumentType + "\">");
	    out.println("<INPUT TYPE=HIDDEN NAME=\"ReturnField\" VALUE=\"" + sReturnField + "\">");
	    out.println("<INPUT TYPE=HIDDEN NAME=\"ParameterString\" VALUE=\"" + sParameterString + "\">");
	    out.println("<INPUT TYPE=HIDDEN NAME=\"" + FinderResults.ADDITIONAL_WHERE_CLAUSE_PARAMETER + "\" VALUE=\"" 
	    	+ clsManageRequestParameters.get_Request_Parameter(FinderResults.ADDITIONAL_WHERE_CLAUSE_PARAMETER, request) 
	    	+ "\">");
	    out.println("<INPUT TYPE=HIDDEN NAME=\"" + FinderResults.FINDER_BOX_TITLE + "\" VALUE=\"" 
	    	+ clsManageRequestParameters.get_Request_Parameter(FinderResults.FINDER_BOX_TITLE, request) 
	    	+ "\">");
	    
	    out.println("<INPUT TYPE=HIDDEN NAME=\"" + DO_NOT_SHOW_MENU_LINK + "\" VALUE=\"" + clsManageRequestParameters.get_Request_Parameter(DO_NOT_SHOW_MENU_LINK, request) + "\">");
	    
	    //Store the display fields and headings for the results page:
	    for (int i = 0; i < MAX_NUMBER_OF_RESULT_FIELDS; i++){
	    	if (bDebugMode){
	    		System.out.println("[1352741895] In " + this.toString() + " Result List Field for " + i + " = " 
	    				+ clsManageRequestParameters.get_Request_Parameter(FinderResults.RESULT_LIST_FIELD + Integer.toString(i), request) );
	    	}
	    	if (request.getParameter(FinderResults.RESULT_LIST_FIELD + Integer.toString(i)) != null){
	    		out.println("<INPUT TYPE=HIDDEN NAME=\"" + FinderResults.RESULT_LIST_FIELD 
	    				+ Integer.toString(i) + "\" VALUE=\"" 
	    				+ request.getParameter(FinderResults.RESULT_LIST_FIELD + Integer.toString(i)) + "\">");
	    	}
	    }
	    for (int i = 0; i < MAX_NUMBER_OF_RESULT_FIELDS; i++){
	    	if (bDebugMode){
	    		System.out.println("[1352741896] In " + this.toString() + " Result Field Alias for " + i + " = " 
	    				+ clsManageRequestParameters.get_Request_Parameter(FinderResults.RESULT_FIELD_ALIAS + Integer.toString(i), request) );
	    	}
	    	if (request.getParameter(FinderResults.RESULT_FIELD_ALIAS + Integer.toString(i)) != null){
	    		out.println("<INPUT TYPE=HIDDEN NAME=\"" + FinderResults.RESULT_FIELD_ALIAS
	    				+ Integer.toString(i) + "\" VALUE=\"" 
	    				+ request.getParameter(FinderResults.RESULT_FIELD_ALIAS + Integer.toString(i)) + "\">");
	    	}
	    }
	    for (int i = 0; i < MAX_NUMBER_OF_RESULT_FIELDS; i++){
	    	if (request.getParameter(FinderResults.RESULT_LIST_HEADING + Integer.toString(i)) != null){
	    		out.println("<INPUT TYPE=HIDDEN NAME=\"" + FinderResults.RESULT_LIST_HEADING 
	    				+ Integer.toString(i) + "\" VALUE=\"" 
	    				+ request.getParameter(FinderResults.RESULT_LIST_HEADING + Integer.toString(i)) + "\">");
	    	}
	    }
	    String sOutPut = "";
	    
		//If there is a 'special title' for the finder box, print it here:
		if (clsManageRequestParameters.get_Request_Parameter(FinderResults.FINDER_BOX_TITLE, request).compareToIgnoreCase("") != 0){
			out.println("<BR><B><I>" + clsManageRequestParameters.get_Request_Parameter(FinderResults.FINDER_BOX_TITLE, request) + "</I></B>");
		}
	    
	    sOutPut = "<TABLE BORDER=12 CELLSPACING=2>";

	    //First, set up the search fields:
	    ArrayList<String> sSearchFields = new ArrayList<String>();
	    
	    for (int i = 1; i<=MAX_NUMBER_OF_SEARCH_FIELDS; i++){
	    	if (request.getParameter("SearchField" + Integer.toString(i)) != null){
	    		sSearchFields.add((String) request.getParameter("SearchField" + Integer.toString(i)));
	    	}
	    }

	    //Set up the search field aliases:
	    ArrayList<String> sSearchFieldAliases = new ArrayList<String>();
	    
	    for (int i = 1; i<=MAX_NUMBER_OF_SEARCH_FIELDS; i++){
	    	if (request.getParameter("SearchFieldAlias" + Integer.toString(i)) != null){
	    		sSearchFieldAliases.add((String) request.getParameter("SearchFieldAlias" + Integer.toString(i)));
	    	}
	    }
	    
	    sOutPut += ARUtilities.Create_Edit_Form_RadioButton_Row(
	    		"sSearchField", 
	    		sSearchFields, 
	    		//Default to the first value:
	    		(String) sSearchFields.get(0), 
	    		sSearchFieldAliases, 
	    		"Search in:", 
	    		"Choose field to use for search"
	    		);
	    
	    
	    //Next, set up the types of search:
	    ArrayList<String> sSearchTypes = new ArrayList<String>();
	    
	    sSearchTypes.add((String) "Beginning with");
	    sSearchTypes.add((String) "Containing");
	    sSearchTypes.add((String) "Exactly matching");

	    ArrayList<String> sSearchTypeDescriptions = new ArrayList<String>();
	    
	    sSearchTypeDescriptions.add((String) "Beginning with");
	    sSearchTypeDescriptions.add((String) "Containing");
	    sSearchTypeDescriptions.add((String) "Exactly matching");

	    String sDefaultType = "Containing";
	    
	    sOutPut += ARUtilities.Create_Edit_Form_RadioButton_Row(
	    		"sSearchType", 
	    		sSearchTypes, 
	    		sDefaultType, 
	    		sSearchTypeDescriptions, 
	    		"Find any " + sObjectName + " ", 
	    		" the text below."
	    		);

	    //The actual search text:
	    sOutPut += ARUtilities.Create_Edit_Form_Text_Input_Row(
	    		"sSearchString", 
	    		"", 
	    		50, 
	    		"Search for:", 
	    		"Enter the search string here"
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