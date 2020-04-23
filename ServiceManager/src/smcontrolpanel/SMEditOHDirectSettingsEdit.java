package smcontrolpanel;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import SMClasses.SMOHDirectSettings;
import SMClasses.SMOption;
import SMClasses.SMOptionInput;
import SMDataDefinition.SMTableohdirectsettings;
import SMDataDefinition.SMTablesmoptions;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsManageRequestParameters;

public class SMEditOHDirectSettingsEdit  extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private PrintWriter m_pwOut;

	public void doPost(HttpServletRequest request,
				HttpServletResponse response)
				throws ServletException, IOException {
		
		m_pwOut = response.getWriter();
		if (!SMAuthenticate.authenticateSMCPCredentials(
				request, 
				response, 
				getServletContext(), 
				SMSystemFunctions.SMEditOHDirectSettings))
		{
			return;
		}

	    HttpSession CurrentSession = request.getSession(true);
	    String m_sDBID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_DATABASE_ID);
	    String m_sUserFullName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERFIRSTNAME)
	    		+ " " + (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERLASTNAME);
	    String m_sUserID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERID);
	    String m_sCompanyName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_COMPANYNAME);
		
	    String title;
	    String subtitle = "";
	    title = "Edit OHDirect Connection Settings";

	    m_pwOut.println(SMUtilities.SMCPTitleSubBGColor(title, subtitle, SMUtilities.getInitBackGroundColor(getServletContext(), m_sDBID), m_sCompanyName));
	    m_pwOut.println(SMUtilities.getColorPickerIncludeString(getServletContext()));
	    m_pwOut.println(SMUtilities.getDatePickerIncludeString(getServletContext()));
		//If there is a warning from trying to input previously, print it here:
	    String m_sWarning = clsManageRequestParameters.get_Request_Parameter("Warning", request);
		if (m_sWarning.compareToIgnoreCase("") != 0){
			m_pwOut.println("<B><FONT COLOR=\"RED\">WARNING: " + m_sWarning + "</FONT></B><BR>");
		}
		
	    //Print a link to main menu:
		m_pwOut.println("<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMUserLogin?" 
				+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + m_sDBID 
				+ "\">Return to Main Menu</A><BR>");
	    
		SMOHDirectSettings ohdobj = new SMOHDirectSettings();
		
		if (CurrentSession.getAttribute(SMOHDirectSettings.ParamObjectName) != null) {
			ohdobj = (SMOHDirectSettings)CurrentSession.getAttribute(SMOHDirectSettings.ParamObjectName);
			CurrentSession.removeAttribute(SMOHDirectSettings.ParamObjectName);
		}else {
			try {
				ohdobj.load(getServletContext(), m_sDBID, m_sUserID, m_sUserFullName);
			} catch (Exception e) {
				m_pwOut.println("<BR><B><FONT COLOR=RED>" + e.getMessage() + "</FONT></B><BR>");
			}
		}
		
		try {
			createEntryScreen(m_sDBID, m_sUserFullName, m_sUserID, ohdobj);
		} catch (Exception e) {
			m_pwOut.println("<B><FONT COLOR=\"RED\">WARNING: " + "could not build entry screen - " + e.getMessage() + "</FONT></B><BR>");
			return;
		}
		
		//End the page:
		m_pwOut.println("</BODY></HTML>");
	}

	private void createEntryScreen(String sDBID, String sUserFullName, String sUserID, SMOHDirectSettings objOHDirectSettings) throws Exception{
	    //Start the entry edit form:
		m_pwOut.println("<FORM NAME='ENTRYEDIT' ACTION='" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMEditOHDirectSettingsAction' METHOD='POST'>");
		m_pwOut.println("<INPUT TYPE=HIDDEN NAME=\"" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "\" VALUE=\"" + sDBID + "\">");
		
		//Start the table:
		m_pwOut.println("<TABLE BORDER=1 CELLSPACING=2 style=\"font-size:75%\">");		
	
		//Display the fields:
		//Client ID:
		m_pwOut.println("<TR><TD ALIGN=RIGHT><B>Client ID</B>:</TD>");
		m_pwOut.println("<TD><INPUT TYPE=TEXT NAME=\"" 
				+ SMTableohdirectsettings.sclientid + "\""
				+ " VALUE=\"" + objOHDirectSettings.getsclientid() + "\""
				+ "SIZE=120"
				+ "; MAXLENGTH=" + Integer.toString(SMTableohdirectsettings.sclientidlength)
				+ ">"
				+ "</TD>");
		m_pwOut.println("<TD>Client ID (this is the 'ci' value from the ionapi file provided by OHD Corp.)</TD></TR>");

		//Client secret:
		m_pwOut.println("<TR><TD ALIGN=RIGHT><B>Client secret</B>:</TD>");
		m_pwOut.println("<TD><INPUT TYPE=TEXT NAME=\"" 
				+ SMTableohdirectsettings.sclientsecret + "\""
				+ " VALUE=\"" + objOHDirectSettings.getsclientsecret() + "\""
				+ "SIZE=120"
				+ "; MAXLENGTH=" + Integer.toString(SMTableohdirectsettings.sclientsecretlength)
				+ ">"
				+ "</TD>");
		m_pwOut.println("<TD>Client secret (this is the 'cs' value from the ionapi file provided by OHD Corp.))</TD></TR>");
		
		//token user name
		m_pwOut.println("<TR><TD ALIGN=RIGHT><B>Token user name</B>:</TD>");
		m_pwOut.println("<TD><INPUT TYPE=TEXT NAME=\"" 
				+ SMTableohdirectsettings.stokenusername + "\""
				+ " VALUE=\"" + objOHDirectSettings.getstokenusername() + "\""
				+ "SIZE=120"
				+ "; MAXLENGTH=" + Integer.toString(SMTableohdirectsettings.stokenusernamelength)
				+ ">"
				+ "</TD>");
		m_pwOut.println("<TD>the user name used to generate the token (this is the 'saak' value from the ionapi file provided by OHD Corp.)</TD></TR>");
		
		//token user password
		m_pwOut.println("<TR><TD ALIGN=RIGHT><B>Token user password</B>:</TD>");
		m_pwOut.println("<TD><INPUT TYPE=TEXT NAME=\"" 
				+ SMTableohdirectsettings.stokenuserpassword + "\""
				+ " VALUE=\"" + objOHDirectSettings.getstokenuserpassword() + "\""
				+ "SIZE=120"
				+ "; MAXLENGTH=" + Integer.toString(SMTableohdirectsettings.stokenuserpasswordlength)
				+ ">"
				+ "</TD>");
		m_pwOut.println("<TD>the user password used to generate the token (this is the 'sask' value from the ionapi file provided by OHD Corp.)</TD></TR>");
		
		//Token URL
		m_pwOut.println("<TR><TD ALIGN=RIGHT><B>Token URL</B>:</TD>");
		m_pwOut.println("<TD><INPUT TYPE=TEXT NAME=\"" 
				+ SMTableohdirectsettings.stokenurl + "\""
				+ " VALUE=\"" + objOHDirectSettings.getstokenurl() + "\""
				+ "SIZE=120"
				+ "; MAXLENGTH=" + Integer.toString(SMTableohdirectsettings.stokenurllength)
				+ ">"
				+ "</TD>");
		m_pwOut.println("<TD>URL contacted to get token (this is created by concatenating the 'pu' and 'ot' values from the ionapi file provided by OHD Corp.)</TD></TR>");
		
		//Request URL base:
		m_pwOut.println("<TR><TD ALIGN=RIGHT><B>Request URL base</B>:</TD>");
		m_pwOut.println("<TD><INPUT TYPE=TEXT NAME=\"" 
				+ SMTableohdirectsettings.srequesturlbase + "\""
				+ " VALUE=\"" + objOHDirectSettings.getsrequesturlbase() + "\""
				+ "SIZE=120"
				+ "; MAXLENGTH=" + Integer.toString(SMTableohdirectsettings.srequesturlbaselength)
				+ ">"
				+ "</TD>");
		m_pwOut.println("<TD>The URL contacted to get quote and order information, not including the 'end point' (e.g. 'C_DealerQuote', etc.)</TD></TR>");
		
		m_pwOut.println("</TABLE>");
		
		m_pwOut.println("<BR><INPUT TYPE=SUBMIT NAME='SubmitEdit' VALUE='Save changes' STYLE='height: 0.24in'>");

	    //End the edit form:
	    m_pwOut.println("</FORM>");  
	}

	public void doGet(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
		
		doPost(request, response);
	}
}
