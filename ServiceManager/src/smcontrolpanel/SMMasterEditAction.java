package smcontrolpanel;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import ServletUtilities.clsServletUtilities;
import ServletUtilities.clsManageRequestParameters;


public class SMMasterEditAction extends java.lang.Object{
	//public static String SUBMIT_EDIT_BUTTON_NAME = "SubmitEdit";
	//public static String SUBMIT_ADD_BUTTON_NAME = "SubmitAdd";
	
	public static String WARNING_PARAMETER = "Warning";
	public static String STATUS_PARAMETER = "Status";
	
	private String m_sDBID = "";
	private String m_sUserName = "";
	private String m_sUserID = "";
	private String m_sUserFullName = "";
	private String m_sCompanyName = "";
	private String m_sCallingClass = "";
	private String m_sOriginalURL = "";
	private HttpSession m_CurrentSession;
	private HttpServletRequest m_request;
	private HttpServletResponse m_response;
	private ServletContext m_context;
	private PrintWriter m_out;
	private ArrayList<String>sErrorArray = new ArrayList<String>(0);
	
	public SMMasterEditAction(
			HttpServletRequest request,
			HttpServletResponse response
    	) {
		
		m_sDBID = "";
		m_sUserName = "";
		m_sUserID = "";
		m_sUserFullName = "";
		m_sCompanyName = "";
		m_sCallingClass = "";
		m_sOriginalURL = "";
		m_CurrentSession = null;
		m_context = null;
		m_out = null;
		sErrorArray = new ArrayList<String>(0);
		m_request = request;
		m_response = response;
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
			m_out.println("Error in process session: " + getErrorMessages());
			return false;
	    }

	    //Remove any entry object from the session in case one's left over:
	    m_CurrentSession.removeAttribute("Entry");

	    m_sDBID = (String) m_CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_DATABASE_ID) + "";
	    if (m_sDBID.compareToIgnoreCase("") == 0){
	    	sErrorArray.add("sDBID parameter is empty.");
	    	m_out.println("Error in process session: " + getErrorMessages());
			return false;
	    }
	    
	    m_sUserName = (String) m_CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERNAME);
	    if (m_sUserName == null || m_sUserName.compareToIgnoreCase("") == 0){
	    	sErrorArray.add("UserName parameter is empty.");
	    	m_out.println("Error in process session: " + getErrorMessages());
			return false;
	    }
	    
	    m_sUserID = (String) m_CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERID);
	    if (m_sUserID == null || m_sUserID.compareToIgnoreCase("") == 0){
	    	sErrorArray.add("UserID parameter is empty.");
	    	m_out.println("Error in process session: " + getErrorMessages());
			return false;
	    }
	    
	    m_sUserFullName = (String) m_CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERFIRSTNAME) + " "
	    					+ (String)m_CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERLASTNAME);
	    if(m_sUserFullName == null || m_sUserFullName.compareToIgnoreCase("") == 0){
	    	sErrorArray.add("UserFirstName and UserLastName parameter is empty.");
	    	m_out.println("Error in process session: "+ getErrorMessages());
	    	return false;
	    }

	    m_sCompanyName = (String) m_CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_COMPANYNAME);
	    if (m_sCompanyName == null || m_sCompanyName.compareToIgnoreCase("") == 0){
	    	sErrorArray.add("CompanyName parameter is empty.");
	    	m_out.println("Error in process session: " + getErrorMessages());
	    	return false;
	    }
	    
	    m_sCallingClass = clsManageRequestParameters.get_Request_Parameter("CallingClass", m_request);
	    m_sOriginalURL = clsServletUtilities.URLDecode(
		    	clsManageRequestParameters.get_Request_Parameter("OriginalURL", m_request)).replace("&", "*");

		if (!SMAuthenticate.authenticateSMCPCredentials(m_request, m_response, context, lFunctionID)){
			return false;
		}
	    return true;
	}
	public boolean processSession(ServletContext context, long lFunctionID, HttpServletRequest req) {
		
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
			m_out.println("Error in process session: " + getErrorMessages());
			return false;
	    }

	    //Remove any entry object from the session in case one's left over:
	    m_CurrentSession.removeAttribute("Entry");

	    m_sDBID = (String) m_CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_DATABASE_ID) + "";
	    if (m_sDBID.compareToIgnoreCase("") == 0){
	    	sErrorArray.add("sDBID parameter is empty.");
	    	m_out.println("Error in process session: " + getErrorMessages());
			return false;
	    }
	    
	    m_sUserName = (String) m_CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERNAME);
	    if (m_sUserName == null || m_sUserName.compareToIgnoreCase("") == 0){
	    	sErrorArray.add("UserName parameter is empty.");
	    	m_out.println("Error in process session: " + getErrorMessages());
			return false;
	    }
	    
	    m_sUserFullName = (String) m_CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERFIRSTNAME) + " "
				+ (String)m_CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERLASTNAME);
	    if(m_sUserFullName == null || m_sUserFullName.compareToIgnoreCase("") == 0){
	    	sErrorArray.add("UserFirstName and UserLastName parameter is empty.");
	    	m_out.println("Error in process session: "+ getErrorMessages());
	    	return false;
	    }

	    m_sUserID = (String) m_CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERID);
	    if (m_sUserID == null || m_sUserID.compareToIgnoreCase("") == 0){
	    	sErrorArray.add("UserID parameter is empty.");
	    	m_out.println("Error in process session: " + getErrorMessages());
			return false;
	    }

	    m_sCompanyName = (String) m_CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_COMPANYNAME);
	    if (m_sCompanyName == null || m_sCompanyName.compareToIgnoreCase("") == 0){
	    	sErrorArray.add("CompanyName parameter is empty.");
	    	m_out.println("Error in process session: " + getErrorMessages());
	    	return false;
	    }
	    
	    m_sCallingClass = clsManageRequestParameters.get_Request_Parameter("CallingClass", req);
	    m_sOriginalURL = clsServletUtilities.URLDecode(
		    	clsManageRequestParameters.get_Request_Parameter("OriginalURL", req)).replace("&", "*");

		if (!SMAuthenticate.authenticateSMCPCredentials(req, m_response, context, lFunctionID)){
			return false;
		}

	    return true;
	}
	
	public boolean isDeleteRequested(){
		if(m_request.getParameter(SMMasterEditEntry.SUBMIT_DELETE_BUTTON_NAME) != null){
	    	return true;
		}else{
			return false;
		}
	}
	public boolean isEditRequested(){
		if(m_request.getParameter(SMMasterEditEntry.SUBMIT_EDIT_BUTTON_NAME) != null){
	    	return true;
		}else{
			return false;
		}
	}

	public boolean isDeleteConfirmed(){

	    if (m_request.getParameter(SMMasterEditEntry.CONFIRM_DELETE_CHECKBOX_NAME) == null){
			try {
				m_response.sendRedirect(
					"" + SMUtilities.getURLLinkBase(m_context) + "" + m_sCallingClass
					+ "?Warning=You must check the 'confirming' check box to delete."
					+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + m_sDBID
				);
			} catch (IOException e) {
				return false;
			}
			return false;
	    }else{
	    	return true;
	    }
	}
	public void redirectAction(String sWarning, String sStatus, String sOtherParameters){
		
		String sRedirectString = 
			"" + SMUtilities.getURLLinkBase(m_context) + "" + m_sCallingClass
			+ "?" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + m_sDBID
			;
		redirectPage(sRedirectString, sWarning, sStatus, sOtherParameters);
	}
	public void redirectActionWithBookmark(String sWarning, String sStatus, String sOtherParameters, String sBookmark){
		String sRedirectString = 
				"" + SMUtilities.getURLLinkBase(m_context) + "" + m_sCallingClass
				+ "#" + sBookmark
				+ "?" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + m_sDBID
				;
			redirectPage(sRedirectString, sWarning, sStatus, sOtherParameters);
	}
	private void redirectPage(String sRedirectString, String sWarning, String sStatus, String sOtherParameters){
		if (m_sOriginalURL != null || m_sOriginalURL.compareToIgnoreCase("") != 0){
			sRedirectString = sRedirectString + "&OriginalURL=" + m_sOriginalURL;
		}
		
		if (sWarning.trim().compareToIgnoreCase("") != 0){
			sRedirectString = sRedirectString + "&" + WARNING_PARAMETER + "=" + clsServletUtilities.URLEncode(sWarning);
		}
		if (sStatus.trim().compareToIgnoreCase("") != 0){
			sRedirectString = sRedirectString + "&" + STATUS_PARAMETER + "=" + clsServletUtilities.URLEncode(sStatus);
		}
		if (sOtherParameters.trim().compareToIgnoreCase("") != 0){
			//In case the '&' is duplicated, filter that out:
			sRedirectString = (sRedirectString + "&" + sOtherParameters).replace("&&", "&");
		}
		//System.out.println("In " + this.toString() + ".redirectAction: " + sRedirectString);
		try {
			//System.out.println("[1463764295] sRedirectString = " + sRedirectString);
			m_response.sendRedirect(sRedirectString);
		} catch (IOException e) {
			//System.out.println("In " + this.toString() 
			//		+ ".redirectAction - IOException error redirecting with string: "
			//		+ sRedirectString + " - " + e.getMessage()
			//);
			m_out.println("In " + this.toString() 
					+ ".redirectAction - IOException error redirecting with string: "
					+ clsServletUtilities.URLEncode(sRedirectString + " - " + e.getMessage())
			);
		} catch (IllegalStateException e) {
			//System.out.println("In " + this.toString() 
			//		+ ".redirectAction - IllegalStateException error redirecting with string: "
			//		+ sRedirectString + " - " + e.getMessage()
			//);
			m_out.println("In " + this.toString() 
					+ ".redirectAction - IllegalStateException error redirecting with string: "
					+ clsServletUtilities.URLEncode(sRedirectString + " - " + e.getMessage())
			);
		}
	}
	public void returnToOriginalURL(){
		
		//Remove this from the session, since it won't be needed if we are returning to the original URL:
		m_CurrentSession.removeAttribute("Entry");
		try {
			m_response.sendRedirect(m_sOriginalURL.replace("*", "&"));
		}catch (IOException e) {
			//System.out.println("In " + this.toString() + ".returnToOriginalURL - error redirecting with string: "
			//		+ m_sOriginalURL
			//);
		}
	}
	public HttpSession getCurrentSession(){
		return m_CurrentSession;
	}
	public String getsDBID(){
		return m_sDBID;
	}
	public String getUserName(){
		return m_sUserName;
	}
	public String getUserID(){
		return m_sUserID;
	}
	public String getFullUserName (){
		return m_sUserFullName;
	}
	public String getCallingClass(){
		return m_sCallingClass;
	}
	public void setCallingClass(String sCallingClass){
		m_sCallingClass = sCallingClass;
	}
	public String getOriginalURL(){
		return m_sOriginalURL;
	}
	public PrintWriter getPwOut(){
		return m_out;
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