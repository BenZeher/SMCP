package smcontrolpanel;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import ConnectionPool.WebContextParameters;
import ServletUtilities.clsManageRequestParameters;

public class SMMasterEditSelect extends java.lang.Object {

	public static final String SUBMIT_EDIT_BUTTON_NAME = "SubmitEdit";
	public static final String SUBMIT_ADD_BUTTON_NAME = "SubmitAdd";
	private String m_sDBID;
	private String m_sUserName;
	private String m_sUserID;
	private String m_sUserFullName;
	private String m_sCompanyName;
	private String m_sThisClass;
	private String m_sCallingClass;
	private String m_sCalledClass;
	private String m_sServletPathToMainMenu;
	private String m_sMainMenuLinkText;
	private long m_lSystemFunctionID;
	private String m_sObjectName;
	private HttpSession m_CurrentSession;
	private HttpServletRequest m_request;
	private HttpServletResponse m_response;
	private PrintWriter m_out;
	private ServletContext m_context;
	private ArrayList<String>sErrorArray;
	private boolean m_bAllowAdd;
	private boolean m_bAllowEdit;
	private boolean bMobileView;
	private String m_onloadFunction;
	private String m_sFormTitle;

	public SMMasterEditSelect(
			HttpServletRequest request,
			HttpServletResponse response,
			String sObjectName,
			String sThisClassName,
			String sCalledClassName,
			String sServletPathToMainMenu,
			String sMainMenuLinkText,
			long lSystemFunctionID
	) {

		m_sDBID = "";
		m_sUserName = "";
		m_sUserID = "";
		m_sUserFullName = "";
		m_sCompanyName = "";
		m_sThisClass = sThisClassName;
		m_sCallingClass = "";
		m_sCalledClass = sCalledClassName;
		m_sServletPathToMainMenu = sServletPathToMainMenu;
		m_sMainMenuLinkText = sMainMenuLinkText;
		m_lSystemFunctionID = lSystemFunctionID;
		m_sObjectName = sObjectName;
		m_CurrentSession = null;
		m_request = request;
		m_response = response;
		m_out = null;
		m_context = null;
		sErrorArray = new ArrayList<String>(0);
		m_bAllowAdd = true;
		m_bAllowEdit = true;
		bMobileView = false;
		m_onloadFunction = "";
		m_sFormTitle = "";
	}

	public boolean processSession(ServletContext context, long lFunctionID){
		
		try {
			m_out = m_response.getWriter();
		} catch (IOException e) {
			sErrorArray.add("Error getting PrintWriter object: " + e.getMessage());
			return false;
		}

		m_context = context;

		m_CurrentSession = m_request.getSession(false);
		if (m_CurrentSession == null){
			sErrorArray.add("Error getting session.");
			return false;
		}
		m_sDBID = (String) m_CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_DATABASE_ID);
		if (m_sDBID == null){
			m_sDBID = "";
		}
		if (m_sDBID.compareToIgnoreCase("") == 0){
			sErrorArray.add("sDBID parameter is empty.");
			return false;
		}

		m_sUserName = (String) m_CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERNAME);
		if (m_sUserName == null){
			m_sUserName = "";
		}
		if (m_sUserName.compareToIgnoreCase("") == 0){
			sErrorArray.add("UserName parameter is empty.");
			return false;
		}
		m_sUserID = (String) m_CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERID);
		if (m_sUserID == null){
			m_sUserID = "";
		}
		if (m_sUserID.compareToIgnoreCase("") == 0){
			sErrorArray.add("UserID parameter is empty.");
			return false;
		}
		m_sUserFullName = (String) m_CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERFIRSTNAME) + " "
						  + (String)m_CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERLASTNAME);
		if(m_sUserFullName == null){
			m_sUserFullName = "";
		}
		if(m_sUserFullName.compareToIgnoreCase("") == 0){
			sErrorArray.add("UserFullName parameter is empty");
		}

		m_sCompanyName = (String) m_CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_COMPANYNAME);
		if (m_sCompanyName == null){
			m_sCompanyName = "";
		}
		if (m_sCompanyName.compareToIgnoreCase("") == 0){
			sErrorArray.add("CompanyName parameter is empty.");
			return false;
		}

		m_sCallingClass = clsManageRequestParameters.get_Request_Parameter("CallingClass", m_request);

		bMobileView = false;
		if (m_CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_MOBILE) != null){
			String sMobile = (String)m_CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_MOBILE);
			if ((sMobile.compareToIgnoreCase("Y") == 0)){
				bMobileView = true;
			}
		}
		if (!SMAuthenticate.authenticateSMCPCredentials(m_request, m_response, context, lFunctionID)){
			return false;
		}
		return true;
	}

	public void printHeaderTable(
	){
		if (m_sFormTitle.compareToIgnoreCase("") == 0){
			m_sFormTitle = "Manage " + m_sObjectName + ".";
		}
		String subtitle = "";

		m_out.println(SMUtilities.SMCPTitleSubBGColor(
				m_sFormTitle, 
				subtitle, 
				SMUtilities.getInitBackGroundColor(m_context, m_sDBID), 
				SMUtilities.DEFAULT_FONT_FAMILY, 
				m_sCompanyName,
				bMobileView,
				m_onloadFunction)
		);
		m_out.println(SMUtilities.getDatePickerIncludeString(m_context));
		//If there is a warning from trying to input previously, print it here:
		String sWarning = clsManageRequestParameters.get_Request_Parameter("Warning", m_request);
		if (! sWarning.equalsIgnoreCase("")){
			m_out.println("<B><FONT COLOR=\"RED\">WARNING: " + sWarning + "</FONT></B><BR>");
		}
		String sStatus = clsManageRequestParameters.get_Request_Parameter("Status", m_request);
		if (! sStatus.equalsIgnoreCase("")){
			m_out.println("<B>STATUS: " + sStatus + "</B><BR>");
		}

		//Print a link to the first page after login:
		m_out.println("<A HREF=\"" + SMUtilities.getURLLinkBase(m_context) + "" + m_sServletPathToMainMenu + "?" 
				+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + m_sDBID 
				+ "\">" 
				+ m_sMainMenuLinkText + "</A><BR>");

		m_out.println("<A HREF=\"" + WebContextParameters.getdocumentationpageURL(m_context) + "#" + Long.toString(m_lSystemFunctionID) 
				+ "\">Summary</A>");

		m_out.println("</TD>");

		m_out.println("</TR>");
		m_out.println("</TABLE>");
	}
	public void createEditForm(
			String sEditHTML
	){

		m_out.println("<FORM NAME='MAINFORM' ACTION='" + SMUtilities.getURLLinkBase(m_context) + "" + m_sCalledClass + "' METHOD='POST'>");
		m_out.println("<INPUT TYPE=HIDDEN NAME='" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "' VALUE='" + m_sDBID + "'>");
		m_out.println("<INPUT TYPE=HIDDEN NAME=\"" + "CallingClass" + "\" VALUE=\"" 
				+ m_sThisClass + "\">");

		m_out.println(sEditHTML);

		if (m_bAllowEdit){
			m_out.println("<P><INPUT TYPE=SUBMIT NAME='" + SMMasterEditSelect.SUBMIT_EDIT_BUTTON_NAME 
				+ "' VALUE='Edit Selected " + m_sObjectName
				+ "' STYLE='height: 0.24in'>&nbsp;&nbsp;");
		}
		if (m_bAllowAdd){
			m_out.println("<INPUT TYPE=SUBMIT NAME='" + SMMasterEditSelect.SUBMIT_ADD_BUTTON_NAME 
					+ "' VALUE='Add New " + m_sObjectName 
					+ "' STYLE='height: 0.24in'></P>");
		}
		m_out.println("</FORM>");
		m_out.println("</BODY></HTML>");

		/*Sample sEditHTML code:
		String s = "";
	    String sEditCode = "";
	    if (req.getParameter(SMBidEntry.ParamID) != null){
	    	sEditCode = req.getParameter(SMBidEntry.ParamID);
	    }

		s+= 
			"<P>Enter " + SMBidEntry.ParamObjectName + " Number: <INPUT TYPE=TEXT NAME=\"" 
			+ SMBidEntry.ParamID + "\""
			+ " VALUE = \"" + sEditCode + "\""
			+ " SIZE=32 MAXLENGTH=" 
			+ "8"
			+ " STYLE=\"width: 2.41in; height: 0.25in\">&nbsp;";

		return s;

		 */

	}
	public void createEditFormWithJavaScript(
			String sEditHTML,
			String sJavaScriptFunction
	){

		m_out.println("<FORM NAME='MAINFORM' ACTION='" + SMUtilities.getURLLinkBase(m_context) + "" + m_sCalledClass + "' METHOD='POST' "+sJavaScriptFunction+">");
		m_out.println("<INPUT TYPE=HIDDEN NAME='" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "' VALUE='" + m_sDBID + "'>");
		m_out.println("<INPUT TYPE=HIDDEN NAME=\"" + "CallingClass" + "\" VALUE=\"" 
				+ m_sThisClass + "\">");

		m_out.println(sEditHTML);

		if (m_bAllowEdit){
			m_out.println("<P><INPUT TYPE=SUBMIT NAME='" + SMMasterEditSelect.SUBMIT_EDIT_BUTTON_NAME 
				+ "' VALUE='Edit Selected " + m_sObjectName
				+ "' STYLE='height: 0.24in'>&nbsp;&nbsp;");
		}
		if (m_bAllowAdd){
			m_out.println("<INPUT TYPE=SUBMIT NAME='" + SMMasterEditSelect.SUBMIT_ADD_BUTTON_NAME 
					+ "' VALUE='Add New " + m_sObjectName 
					+ "' STYLE='height: 0.24in'></P>");
		}
		m_out.println("</FORM>");
		m_out.println("</BODY></HTML>");

		/*Sample sEditHTML code:
		String s = "";
	    String sEditCode = "";
	    if (req.getParameter(SMBidEntry.ParamID) != null){
	    	sEditCode = req.getParameter(SMBidEntry.ParamID);
	    }

		s+= 
			"<P>Enter " + SMBidEntry.ParamObjectName + " Number: <INPUT TYPE=TEXT NAME=\"" 
			+ SMBidEntry.ParamID + "\""
			+ " VALUE = \"" + sEditCode + "\""
			+ " SIZE=32 MAXLENGTH=" 
			+ "8"
			+ " STYLE=\"width: 2.41in; height: 0.25in\">&nbsp;";

		return s;

		 */

	}

	public String getCallingClass(){
		return m_sCallingClass;
	}
	public PrintWriter getPrintWriter(){
		return m_out;
	}
	public String getsDBID(){
		return m_sDBID;
	}
	public String getUser(){
		return m_sUserName;
	}
	public String getUserID(){
		return m_sUserID;
	}
	public String getFullUserName (){
		return m_sUserFullName;
	}
	public HttpSession getSession(){
		return m_CurrentSession;
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
	public void showAddNewButton(boolean bShowAddNew){
		m_bAllowAdd = bShowAddNew;
	}
	public void showEditButton(boolean bShowEdit){
		m_bAllowEdit = bShowEdit;
	}
	public void setOnLoadFunction(String sOnLoadFunction){
		m_onloadFunction = sOnLoadFunction;
	}
	public void setFormTitle(String sFormTitle){
		m_sFormTitle = sFormTitle;
	}
}