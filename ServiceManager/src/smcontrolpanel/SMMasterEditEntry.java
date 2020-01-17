package smcontrolpanel;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import ConnectionPool.WebContextParameters;
import ServletUtilities.clsServletUtilities;
import ServletUtilities.clsManageRequestParameters;


public class SMMasterEditEntry  extends java.lang.Object{

	public static final String SUBMIT_EDIT_BUTTON_NAME = "SubmitEdit";
	public static final String SUBMIT_DELETE_BUTTON_NAME = "SubmitDelete";
	public static final String CONFIRM_DELETE_CHECKBOX_NAME = "ConfirmDelete";
	public static final String MAIN_FORM_NAME = "MAINFORM";
	private String m_sDatabaseID;
	private String m_sUserName;
	private String m_sUserID;
	private String m_sFullUserName;
	private String m_sCompanyName;
	private String m_sCallingClass;
	private String m_sCalledClass;
	private String m_sThisClass;
	private String m_sOriginalURL;
	private String m_sCurrentCompleteURL;
	private String m_sServletPathToMainMenu;
	private String m_sMainMenuLinkText;
	private String m_sTitle;
	private long m_lSystemFunctionID;
	private boolean m_bAddingNewEntry;
	private String m_sObjectName;
	private HttpSession m_CurrentSession;
	private HttpServletRequest m_request;
	private HttpServletResponse m_response;
	private PrintWriter m_out;
	private ServletContext m_context;
	private boolean m_bIncludeUpdateButton;
	private String m_sUpdateButtonLabel;
	private boolean m_bIncludeDeleteButton;
	private boolean m_bIncludeSMCP_CSS_Script;
	private String m_onloadFunction; //Seems to be unreliable, so we set it to a blank string.
	private int m_iPageRefreshIntervalInSeconds;
	private String m_sLicenseModuleLevel;
	private boolean bDebugMode;
	private boolean bMobileView;

	private ArrayList<String>sErrorArray = new ArrayList<String>(0);

	public SMMasterEditEntry(
			HttpServletRequest request,
			HttpServletResponse response,
			ServletContext context,
			String sObjectName,
			String sThisClassName,
			String sCalledClassName,
			String sServletPathToMainMenu,
			String sMainMenuLinkText,
			long lSystemFunctionID
	) {
		m_sDatabaseID = "";
		m_sUserName = "";
		m_sFullUserName = "";
		m_sUserID = "";
		m_sCompanyName = "";
		m_sCallingClass = "";
		m_sOriginalURL = "";
		m_sCurrentCompleteURL = "";
		m_sTitle = "";
		m_CurrentSession = null;
		m_out = null;
		bDebugMode = false;
		bMobileView = false;
		m_sThisClass = sThisClassName;
		m_sCalledClass = sCalledClassName;
		m_sServletPathToMainMenu = sServletPathToMainMenu;
		m_sMainMenuLinkText = sMainMenuLinkText;
		m_lSystemFunctionID = lSystemFunctionID;
		m_sObjectName = sObjectName;
		m_bAddingNewEntry = false;
		sErrorArray = new ArrayList<String>(0);
		m_request = request;
		m_response = response;
		m_context = context;
		m_bIncludeUpdateButton = true;
		m_bIncludeDeleteButton = true;
		m_bIncludeSMCP_CSS_Script = false;
		m_sUpdateButtonLabel = "";
		m_onloadFunction = "";
		m_iPageRefreshIntervalInSeconds = 0;
		m_sLicenseModuleLevel = "0";
		//Default title:
		m_sTitle = "Edit " + m_sObjectName;

	}
	//test onload function - LTO
	public SMMasterEditEntry(
			HttpServletRequest request,
			HttpServletResponse response,
			ServletContext context,
			String sObjectName,
			String sThisClassName,
			String sCalledClassName,
			String sServletPathToMainMenu,
			String sMainMenuLinkText,
			long lSystemFunctionID,
			String sOnLoadFunction
	) {
		m_sDatabaseID = "";
		m_sUserName = "";
		m_sFullUserName = "";
		m_sUserID = "0";
		m_sCompanyName = "";
		m_sCallingClass = "";
		m_sOriginalURL = "";
		m_sCurrentCompleteURL = "";
		m_CurrentSession = null;
		m_out = null;
		bDebugMode = false;
		bMobileView = false;
		m_sThisClass = sThisClassName;
		m_sCalledClass = sCalledClassName;
		m_sServletPathToMainMenu = sServletPathToMainMenu;
		m_sMainMenuLinkText = sMainMenuLinkText;
		m_lSystemFunctionID = lSystemFunctionID;
		m_sObjectName = sObjectName;
		m_bAddingNewEntry = false;
		sErrorArray = new ArrayList<String>(0);
		m_request = request;
		m_response = response;
		m_context = context;
		m_bIncludeUpdateButton = true;
		m_bIncludeDeleteButton = true;
		m_bIncludeSMCP_CSS_Script = false;
		m_sUpdateButtonLabel = "";
		m_onloadFunction = sOnLoadFunction;
		m_iPageRefreshIntervalInSeconds = 0;
		m_sLicenseModuleLevel = "0";
		//Default title:
		m_sTitle = "Edit " + m_sObjectName;

	}

	public boolean processSession(ServletContext context, long lFunctionID){

		try {
			m_out = m_response.getWriter();
		} catch (IOException e) {
			sErrorArray.add("Error [1424104607] Error getting PrintWriter object: " + e.getMessage());
			return false;
		}
		
		if (!SMAuthenticate.authenticateSMCPCredentials(m_request, m_response, context, lFunctionID)){
			return false;
		}

		m_context = context;

		m_CurrentSession = m_request.getSession(false);
		if (m_CurrentSession == null){
			sErrorArray.add("Error [1424104603] getting session.");
			return false;
		}
		m_sDatabaseID = clsServletUtilities.getStringAttributeFromSession(m_CurrentSession, SMUtilities.SMCP_SESSION_PARAM_DATABASE_ID);
		if (m_sDatabaseID.compareTo("") == 0){
			sErrorArray.add("Error [1424104602] Database ID parameter is empty.");
			return false;
		}
		m_sUserName = clsServletUtilities.getStringAttributeFromSession(m_CurrentSession, SMUtilities.SMCP_SESSION_PARAM_USERNAME);
		if (m_sUserName.compareToIgnoreCase("") == 0){
			sErrorArray.add("Error [1424104605] UserName parameter is empty.");
			return false;
		}
		m_sFullUserName = (clsServletUtilities.getStringAttributeFromSession(m_CurrentSession, SMUtilities.SMCP_SESSION_PARAM_USERFIRSTNAME) 
			+ " " 
			+ clsServletUtilities.getStringAttributeFromSession(m_CurrentSession, SMUtilities.SMCP_SESSION_PARAM_USERLASTNAME)).trim();
		if (m_sFullUserName.compareToIgnoreCase("") == 0){
			sErrorArray.add("Error [1524602947] Full User Name parameter is empty.");
			return false;
		}
		m_sUserID = clsServletUtilities.getStringAttributeFromSession(m_CurrentSession, SMUtilities.SMCP_SESSION_PARAM_USERID);
		if (m_sUserID.compareToIgnoreCase("") == 0){
			sErrorArray.add("Error [1824154685] UserID parameter is empty.");
			return false;
		}
		m_sCompanyName = clsServletUtilities.getStringAttributeFromSession(m_CurrentSession, SMUtilities.SMCP_SESSION_PARAM_COMPANYNAME);
		if (m_sCompanyName.compareToIgnoreCase("") == 0){
			sErrorArray.add("Error [1424104606] CompanyName parameter is empty.");
			return false;
		}

		m_sCallingClass = clsManageRequestParameters.get_Request_Parameter("CallingClass", m_request);
		m_sOriginalURL = clsServletUtilities.URLDecode(
				clsManageRequestParameters.get_Request_Parameter("OriginalURL", m_request)).replace("&", "*");
		//System.out.println("In " + this.toString() + " - m_sOriginalURL = " + m_sOriginalURL);

		try {
			m_sCurrentCompleteURL = clsServletUtilities.URLEncode(m_request.getRequestURI().toString()
				+ clsManageRequestParameters.getQueryStringFromPost(m_request)
			);
		} catch (Exception e1) {
			sErrorArray.add("Error [1543602455] Error getting request URL - " + e1.getMessage());
			return false;
		}
		
		if (bDebugMode){
			System.out.println("[1579274269] In " + this.toString() 
					+ ".processSession - m_sCurrentCompleteURL = " + m_sCurrentCompleteURL);
		}
		m_bAddingNewEntry = clsManageRequestParameters.get_Request_Parameter(
				SMMasterEditSelect.SUBMIT_ADD_BUTTON_NAME, m_request).compareTo("") != 0;
		bMobileView = false;
		try {
			if (m_CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_MOBILE) != null){
				String sMobile = (String)m_CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_MOBILE);
				if ((sMobile.compareToIgnoreCase("Y") == 0)){
					bMobileView = true;
				}
			}
		} catch (Exception e) {
			System.out.println("Error [1415807184] - " + e.getMessage());
		}
		
		m_sLicenseModuleLevel = (String) m_CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_LICENSE_MODULE_LEVEL);
		return true;
	}
	public boolean processSession(ServletContext context, long lFunctionID, HttpServletRequest req){

		try {
			m_out = m_response.getWriter();
		} catch (IOException e) {
			sErrorArray.add("Error [1424104607] Error getting PrintWriter object: " + e.getMessage());
			return false;
		}
		
		if (!SMAuthenticate.authenticateSMCPCredentials(req, m_response, context, lFunctionID)){
			return false;
		}

		m_context = context;

		m_CurrentSession = req.getSession(false);
		if (m_CurrentSession == null){
			sErrorArray.add("Error [1424104603] getting session.");
			return false;
		}
		m_sDatabaseID = clsServletUtilities.getStringAttributeFromSession(m_CurrentSession, SMUtilities.SMCP_SESSION_PARAM_DATABASE_ID);
		if (m_sDatabaseID.compareToIgnoreCase("") == 0){
			sErrorArray.add("Error [1424104602] Database ID parameter is empty.");
			return false;
		}
		m_sUserName = clsServletUtilities.getStringAttributeFromSession(m_CurrentSession, SMUtilities.SMCP_SESSION_PARAM_USERNAME);
		if (m_sUserName.compareToIgnoreCase("") == 0){
			sErrorArray.add("Error [1424104605] UserName parameter is empty.");
			return false;
		}
		m_sFullUserName = (clsServletUtilities.getStringAttributeFromSession(m_CurrentSession, SMUtilities.SMCP_SESSION_PARAM_USERFIRSTNAME) 
			+ " " 
			+ clsServletUtilities.getStringAttributeFromSession(m_CurrentSession, SMUtilities.SMCP_SESSION_PARAM_USERLASTNAME)).trim();
		if (m_sFullUserName.compareToIgnoreCase("") == 0){
			sErrorArray.add("Error [1524602948] Full User Name parameter is empty.");
			return false;
		}
		m_sUserID = clsServletUtilities.getStringAttributeFromSession(m_CurrentSession, SMUtilities.SMCP_SESSION_PARAM_USERID);
		if (m_sUserID.compareToIgnoreCase("") == 0){
			sErrorArray.add("Error [148410465] UserID parameter is empty.");
			return false;
		}
		m_sCompanyName = clsServletUtilities.getStringAttributeFromSession(m_CurrentSession, SMUtilities.SMCP_SESSION_PARAM_COMPANYNAME);
		if (m_sCompanyName.compareToIgnoreCase("") == 0){
			sErrorArray.add("Error [1424104606] CompanyName parameter is empty.");
			return false;
		}

		m_sCallingClass = clsManageRequestParameters.get_Request_Parameter("CallingClass", req);
		m_sOriginalURL = clsServletUtilities.URLDecode(
				clsManageRequestParameters.get_Request_Parameter("OriginalURL", req)).replace("&", "*");
		//System.out.println("In " + this.toString() + " - m_sOriginalURL = " + m_sOriginalURL);

		m_sCurrentCompleteURL = clsServletUtilities.URLEncode(req.getRequestURI().toString() 
				+ clsManageRequestParameters.getQueryStringFromPost(req));
		if (bDebugMode){
			System.out.println("[1579274276] In " + this.toString() 
					+ ".processSession - m_sCurrentCompleteURL = " + m_sCurrentCompleteURL);
		}
		m_bAddingNewEntry = clsManageRequestParameters.get_Request_Parameter(
				SMMasterEditSelect.SUBMIT_ADD_BUTTON_NAME, req).compareTo("") != 0;
		bMobileView = false;
		try {
			if (m_CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_MOBILE) != null){
				String sMobile = (String)m_CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_MOBILE);
				if ((sMobile.compareToIgnoreCase("Y") == 0)){
					bMobileView = true;
				}
			}
		} catch (Exception e) {
			System.out.println("Error [1415807184] - " + e.getMessage());
		}
		
		m_sLicenseModuleLevel = (String) m_CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_LICENSE_MODULE_LEVEL);
		return true;
	}
	public void printHeaderTable(
	){

		String subtitle = "";

		m_out.println(SMUtilities.SMCPTitleSubBGColorWithAutoRefresh(
				m_sTitle, 
				subtitle, 
				SMUtilities.getInitBackGroundColor(m_context, m_sDatabaseID), 
				SMUtilities.DEFAULT_FONT_FAMILY, 
				m_sCompanyName,
				bMobileView,
				m_onloadFunction,
				m_iPageRefreshIntervalInSeconds)
		);
		m_out.println(SMUtilities.getDatePickerIncludeString(m_context));
		m_out.println();
		if (m_bIncludeSMCP_CSS_Script){
			m_out.println(SMUtilities.getSMCP_CSSIncludeString(m_context));
		}

		//If there is a warning from trying to input previously, print it here:
		String sWarning = clsManageRequestParameters.get_Request_Parameter("Warning", m_request);
		if (! sWarning.equalsIgnoreCase("")){
			m_out.println("<B><FONT COLOR=\"RED\">WARNING: " + clsServletUtilities.URLDecode(sWarning) + "</FONT></B><BR>");
		}
		//If there is a status from trying to input previously, print it here:
		String sStatus = clsManageRequestParameters.get_Request_Parameter("Status", m_request);
		if (! sStatus.equalsIgnoreCase("")){
			m_out.println("<B>" + clsServletUtilities.URLDecode(sStatus) + "</B><BR>");
		}
		m_out.println("<TABLE BORDER=0 WIDTH=100%>");

		//Print a link to the first page after login:
		m_out.println("<TR>");
		m_out.println("<TD>");

		if (m_sServletPathToMainMenu.compareToIgnoreCase("") != 0){
			m_out.println("<A HREF=\"" + SMUtilities.getURLLinkBase(m_context) + "" + m_sServletPathToMainMenu + "?" 
				+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + m_sDatabaseID 
				+ "\">" 
					+ m_sMainMenuLinkText + "</A><BR>");
		}
		m_out.println("<A HREF=\"" + WebContextParameters.getdocumentationpageURL(m_context) + "#" + Long.toString(m_lSystemFunctionID) 
				+ "\">Summary</A>");
		m_out.println("</TD>");

		m_out.println("</TR>");
		m_out.println("</TABLE>");

	}
	public void printLowProfileHeaderTable(
	){

		String subtitle = "";

		m_out.println(SMUtilities.lowProfileSMCPTitle(
				m_sTitle, 
				subtitle, 
				SMUtilities.getInitBackGroundColor(m_context, m_sDatabaseID), 
				SMUtilities.DEFAULT_FONT_FAMILY, 
				m_sCompanyName,
				bMobileView,
				m_onloadFunction)
		);
		m_out.println(SMUtilities.getDatePickerIncludeString(m_context));

		m_out.println("<BR>");
		
		//If there is a warning from trying to input previously, print it here:
		String sWarning = clsManageRequestParameters.get_Request_Parameter("Warning", m_request);
		if (! sWarning.equalsIgnoreCase("")){
			m_out.println("<B><FONT COLOR=\"RED\">WARNING: " + sWarning + "</FONT></B><BR>");
		}
		//If there is a warning from trying to input previously, print it here:
		String sStatus = clsManageRequestParameters.get_Request_Parameter("Status", m_request);
		if (! sStatus.equalsIgnoreCase("")){
			m_out.println("<B>" + sStatus + "</B><BR>");
		}

		if (m_sServletPathToMainMenu.compareToIgnoreCase("") != 0){
			m_out.println("<A HREF=\"" + SMUtilities.getURLLinkBase(m_context) + "" + m_sServletPathToMainMenu + "?" 
				+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + m_sDatabaseID 
				+ "\">" 
					+ m_sMainMenuLinkText + "</A>&nbsp;");
		}
		m_out.println("<A HREF=\"" + WebContextParameters.getdocumentationpageURL(m_context) + "#" + Long.toString(m_lSystemFunctionID) 
				+ "\">Summary</A>");

	}
	public void createEditPage(
			String sEditHTML,
			String sAfterSaveAndDeleteButtons
	){

		String sFormString = "<FORM ID='" + MAIN_FORM_NAME + "' NAME='" + MAIN_FORM_NAME + "' ACTION='" 
			+ SMUtilities.getURLLinkBase(m_context) + m_sCalledClass + "'";
		
		sFormString	+= " METHOD='POST'>";
		m_out.println(sFormString);
		m_out.println("<INPUT TYPE=HIDDEN NAME='" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "' VALUE='" + m_sDatabaseID + "'>");
		m_out.println("<INPUT TYPE=HIDDEN NAME=\"" + "CallingClass" + "\" VALUE=\"" 
				+ m_sThisClass + "\">");
		m_out.println("<INPUT TYPE=HIDDEN NAME=\"" + "OriginalURL" + "\" VALUE=\"" 
				+ m_sOriginalURL + "\">");

		m_out.println(sEditHTML);

		if (m_bIncludeUpdateButton){
			String sButtonLabel = m_sUpdateButtonLabel;
			if (sButtonLabel.compareToIgnoreCase("") == 0){
				sButtonLabel = "Update " + m_sObjectName ;
			}
			m_out.println("<P><INPUT TYPE=SUBMIT NAME='" + SUBMIT_EDIT_BUTTON_NAME 
					+ "' VALUE='" + sButtonLabel + "' STYLE='height: 0.24in'>");
		}
		if (m_bIncludeDeleteButton){
			m_out.println("<INPUT TYPE=SUBMIT NAME='" + SUBMIT_DELETE_BUTTON_NAME + "' VALUE='Delete " + m_sObjectName 
					+ "' STYLE='height: 0.24in'>");
			m_out.println("  Check to confirm deletion: <INPUT TYPE=CHECKBOX NAME=\"" 
					+ CONFIRM_DELETE_CHECKBOX_NAME + "\"></P>");
		}
		//This is for any HTML needed after the update and delete buttons, that we still need
		//within the form:
		if (sAfterSaveAndDeleteButtons.compareToIgnoreCase("") != 0){
			m_out.println(sAfterSaveAndDeleteButtons);
		}
		m_out.println("</FORM>");

	}

	/*
	private String getEditHTML(){
		String sEdit = "";

		//SAMPLE CODE FOR ALL TYPES OF FIELDS:
		sEdit += "<INPUT TYPE=HIDDEN NAME=\"" + SMJobCostEntry.ParamID + "\" VALUE=\"" 
				+ entry.slid() + "\">";
		sEdit += "<INPUT TYPE=HIDDEN NAME=\"" + SMJobCostEntry.ParamDateTimeLastEdit 
				+ "\" VALUE=\"" + entry.sDateTimeLastEdit() + "\">";
		sEdit +="<INPUT TYPE=HIDDEN NAME=\"" + SMJobCostEntry.ParamLastEditedBy + "\" VALUE=\"" 
				+ sUser + "\">");

		sEdit +="Date last maintained: " + entry.sDateTimeLastEdit();
	    sEdit +=" by user: " + entry.sLastEditedBy() + "<BR>";
	    sEdit +="<TABLE BORDER=1 CELLSPACING=2>";

	    //Entry ID:
		sEdit +="<TD ALIGN=RIGHT><B>Entry ID:</B></TD><TD>" 
				+ entry.slid().replace("\"", "&quot;") + "</TD><TD>&nbsp;</TD>";

		//Fixed length string:
		sEdit += SMUtilities.Create_Edit_Form_Text_Input_Row(
				SMJobCostEntry.ParamJobNumber, //field name
				entry.sJobNumber().replace("\"", "&quot;"), //value 
				SMTablejobcost.sJobNumberLength,	//Maximum field length 
				"Order Number:", //label
				"Max of 8 characters",	//Remark to the right of the field
				"10" //width of text box
				);

		//Multi-line text:
		sEdit += SMUtilities.Create_Edit_Form_MultilineText_Input_Row(
				SMJobCostEntry.ParamDesc, //Field name 
				entry.sDescription().replace("\"", "&quot;"), 	//field default value
				"Work description:", //field label 
				"Full description of work", //remark to the right of the field
				2,	//number of rows for the textarea
				60	//number of columns for the textarea
				);

		//Regular Date; //Always stored as MM/dd/yyyy
		sEdit += SMUtilities.Create_Edit_Form_Date_Input_Row(
				SMJobCostEntry.ParamDate,	//Field name
				entry.sEntryDate().replace("\"", "&quot;"),	//date value as M/d/yyyy string
				"Entry date:",	//Label for field
				"In <B>mm/dd/yyyy</B> format." //Remark to the right of the field
				);

		//Date and time (M/d/yyyy 00:00 PM)
		sEdit += SMUtilities.Create_Edit_Form_DateTime_Input_Row(
				SMBidEntry.Paramdattimebiddate, //Field name
				entry.getdattimebiddate(), //value
				"Test label:", //Label
				"Test remark"); //Remark to the right of the field


		//Phone number
		sEdit += SMUtilities.Create_Edit_Form_PhoneNumber_Input_Row(
				SMBidEntry.Paramsphonenumber, //Field name
				entry.getsphonenumber().replace("\"", "&quot;"), //Value
				"<B>Phone number:</B>", //Label
				"" // Remark to the right of the field
				);

		//Phone number with extension
		sEdit += SMUtilities.Create_Edit_Form_PhoneWithExt_Input_Row(
				SMBidEntry.Paramsphonenumber,  //Phone number field name 
				entry.getsphonenumber().replace("\"", "&quot;"),  //Phone number value
				SMBidEntry.Paramsextension,  //Extension field name
				entry.getsextension().replace("\"", "&quot;"),  //Extension value
				"<B>Phone number:</B>",  //Label
				""  //Remark to the right of the field
				);


	    //Checkbox:
	    //TODO

	    //Radio buttons:
		sEdit += SMUtilities.Create_Edit_Form_RadioButton_Input_Row(
				SMBidEntry.ParamHasBidDate, //Field name
				"<B>Date/Time:</B>",  //Field label
				"In mm/dd/yy format",  //Remarks to the right of the field 
				sDescriptions, //Array of button/option descriptions
				sValues,  //Array <String> of values for each button
				sDefaultValue //Default button value - i.e., which button is selected
				);

	    //Select list loaded from database:
	    ArrayList<String> sValues = new ArrayList<String>();
	    ArrayList<String> sDescriptions = new ArrayList<String>();
	    try{
	        //Categories
	        String sSQL = "SELECT * FROM " + SMTablemechanics.TableName
	        	+ " ORDER BY " + SMTablemechanics.sMechInitial
	        	;
	        ResultSet rsMechanics = SMUtilities.openResultSet(
	        	sSQL, 
	        	getServletContext(), 
	        	sDBID,
	        	"MySQL",
	        	this.toString() + ".Edit_Record (2) - User: " + sUserName);

	        sValues.clear();
	        sDescriptions.clear();
	        //First, add a blank to make sure the user selects one:
	        sValues.add("");
	        sDescriptions.add("-- Select a mechanic --");
	        while (rsMechanics.next()){
	        	sValues.add((String) rsMechanics.getString(SMTablemechanics.sMechInitial.trim()));
	        	sDescriptions.add((String) (rsMechanics.getString(SMTablemechanics.sMechInitial).trim() 
	        			+ " - " + rsMechanics.getString(SMTablemechanics.sMechFullName).trim()));
	        }
	        rsMechanics.close();
	        sEdit +=SMUtilities.Create_Edit_Form_List_Row(
	        		SMJobCostEntry.ParamMechanic, 
	        		sValues, 
	        		entry.sMechanic().replace("\"", "&quot;"), 
	        		sDescriptions, 
	        		"Mechanic:", 
	        		""
	        	)
	        ;

		}catch (SQLException ex){
	    	System.out.println("Error in " + this.toString()+ " class!!");
	        System.out.println("SQLException: " + ex.getMessage());
	        System.out.println("SQLState: " + ex.getSQLState());
	        System.out.println("SQL: " + ex.getErrorCode());
			//return false;
		}

	    sEdit +="</TABLE>";
	    sEdit +="<FONT COLOR=RED>*</FONT> Asterisked items are <B>required</B> fields.";
	    //pwOut.println("<BR>");
		return sEdit;
	}
	 */
	public void redirectAction(String sWarning, String sStatus, String sOtherParameters){
		
		String sRedirectString = 
			"" + SMUtilities.getURLLinkBase(m_context) + "" + m_sCallingClass
			+ "?" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + m_sDatabaseID
			;
		
		if (m_sOriginalURL != null || m_sOriginalURL.compareToIgnoreCase("") != 0){
			sRedirectString = sRedirectString + "&OriginalURL=" + m_sOriginalURL;
		}
		
		if (sWarning.trim().compareToIgnoreCase("") != 0){
			sRedirectString = sRedirectString + "&Warning=" + clsServletUtilities.URLEncode(sWarning);
		}
		if (sStatus.trim().compareToIgnoreCase("") != 0){
			sRedirectString = sRedirectString + "&Status=" + clsServletUtilities.URLEncode(sStatus);
		}
		if (sOtherParameters.trim().compareToIgnoreCase("") != 0){
			//In case the '&' is duplicated, filter that out:
			sRedirectString = (sRedirectString + "&" + sOtherParameters).replace("&&", "&");
		}
		

		//System.out.println("In " + this.toString() + ".redirectAction: " + sRedirectString);
		try {
			m_response.sendRedirect(sRedirectString);
		} catch (IOException e) {
			System.out.println("[1579274285] In " + this.toString() 
					+ ".redirectAction - IOException error redirecting with string: "
					+ sRedirectString + " - " + e.getMessage()
			);
			m_out.println("In " + this.toString() 
					+ ".redirectAction - IOException error redirecting with string: "
					+ clsServletUtilities.URLEncode(sRedirectString + " - " + e.getMessage())
			);
		} catch (IllegalStateException e) {
			System.out.println("[1579274289] In " + this.toString() 
					+ ".redirectAction - IllegalStateException error redirecting with string: "
					+ sRedirectString + " - " + e.getMessage()
			);
			m_out.println("In " + this.toString() 
					+ ".redirectAction - IllegalStateException error redirecting with string: "
					+ clsServletUtilities.URLEncode(sRedirectString + " - " + e.getMessage())
			);
		}
	}
	public HttpSession getCurrentSession(){
		return m_CurrentSession;
	}
	public String getObjectName(){
		return m_sObjectName;
	}
	public PrintWriter getPWOut(){
		return m_out;
	}
	public String getsDBID(){
		return m_sDatabaseID;
	}
	public String getUserName(){
		return m_sUserName;
	}
	public String getFullUserName(){
		return m_sFullUserName;
	}
	public String getUserID(){
		return m_sUserID;
	}
	public String getCallingClass(){
		return m_sCallingClass;
	}
	public String getCalledClass(){
		return m_sCalledClass;
	}
	public String getOriginalURL(){
		return m_sOriginalURL;
	}
	public boolean getAddingNewEntryFlag(){
		return m_bAddingNewEntry;
	}
	public String getLicenseModuleLevel(){
		return m_sLicenseModuleLevel;
	}
	public HttpServletRequest getRequest(){
		return m_request;
	}
	public void addToURLHistory(String sTitle){
		m_CurrentSession.setAttribute(SMUtilities.SMCP_SESSION_PARAM_URLHISTORY,
				SMUtilities.updateURLHistory(sTitle, 
						m_sCurrentCompleteURL.replace("&", "*"), 
						m_CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_URLHISTORY),
						m_CurrentSession.getAttribute("URLMaxSize"))
		);
	}
	public boolean isbIncludeUpdateButton() {
		return m_bIncludeUpdateButton;
	}
	public void setbIncludeUpdateButton(boolean bIncUpdateButton) {
		m_bIncludeUpdateButton = bIncUpdateButton;
	}
	public boolean isbIncludeDeleteButton() {
		return m_bIncludeDeleteButton;
	}
	public void setbIncludeDeleteButton(boolean bIncDeleteButton) {
		m_bIncludeDeleteButton = bIncDeleteButton;
	}
	public void setIncludeSMCP_CSS_Script(boolean bIncludeSMCP_CSS_Script){
		m_bIncludeSMCP_CSS_Script = bIncludeSMCP_CSS_Script;
	}
	public void setTitle(String sTitle){
		m_sTitle = sTitle;
	}
	public void setUpdateButtonLabel(String sUpdateButtonLabel){
		m_sUpdateButtonLabel = sUpdateButtonLabel;
	}
	public String getSubmitEditButtonName(){
		return SUBMIT_EDIT_BUTTON_NAME;
	}
	public String getSubmitDeleteButtonName(){
		return SUBMIT_DELETE_BUTTON_NAME;
	}
	public void setPageRefreshIntervalInSeconds(int iPageRefreshIntervalInSeconds){
		m_iPageRefreshIntervalInSeconds = iPageRefreshIntervalInSeconds;
	}
	public int getPageRefreshIntervalInSeconds(){
		return m_iPageRefreshIntervalInSeconds;
	}
	
	public String getErrorMessages(){
		String s = "";
		for (int i = 0; i < sErrorArray.size(); i++){
			if (i == 0){
				s+= sErrorArray.get(i);
			}else{
				s+= "\n" + sErrorArray.get(i);
			}
		}
		return s;
	}
}
