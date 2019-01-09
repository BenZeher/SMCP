package smar;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import smcontrolpanel.SMAuthenticate;
import smcontrolpanel.SMSystemFunctions;
import smcontrolpanel.SMUtilities;
import SMDataDefinition.SMExportTypes;
import SMDataDefinition.SMTablearoptions;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsManageRequestParameters;

public class AREditAROptions extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private String m_sWarning;
	private String m_sDBID;
	private String m_sUserName;
	private String m_sCompanyName;
	private PrintWriter m_pwOut;
	private HttpServletRequest m_hsrRequest;
	private AROptions m_aropt = new AROptions();
	private boolean m_bInputLoaded = false;

	public void doPost(HttpServletRequest request,
				HttpServletResponse response)
				throws ServletException, IOException {
		
		m_hsrRequest = request;
	    get_request_parameters();
	    
		m_pwOut = response.getWriter();
		if (!SMAuthenticate.authenticateSMCPCredentials(
				request, 
				response, 
				getServletContext(),
				SMSystemFunctions.AREditAROptions)){
			return;
		}
		
		//Try to load an object from which to build the form:
		if (!loadAROptionInput()){
    		m_pwOut.println(
					"<META http-equiv='Refresh' content='0;URL=" 
					+ "" + SMUtilities.getURLLinkBase(getServletContext()) + "smar.AREditAROptions"
					+ "?Warning=" + m_sWarning
					+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + m_sDBID
					+ "'>"		
				);
			return;
		}
		
	    String title;
	    String subtitle = "";
	    title = "Edit Accounts Receivable Options";

	    m_pwOut.println(SMUtilities.SMCPTitleSubBGColor(title, subtitle, SMUtilities.getInitBackGroundColor(getServletContext(), m_sDBID), m_sCompanyName));
		
		//If there is a warning from trying to input previously, print it here:
		if (! m_sWarning.equalsIgnoreCase("")){
			m_pwOut.println("<B><FONT COLOR=\"RED\">WARNING: " + m_sWarning + "</FONT></B><BR>");
		}
		
	    //Print a link to main menu:
		m_pwOut.println("<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMUserLogin?" 
				+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + m_sDBID 
				+ "\">Return to Main Menu</A><BR>");
		
	    //Print a link to main menu:
		m_pwOut.println("<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smar.ARMainMenu?" 
				+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + m_sDBID 
				+ "\">Return to Accounts Receivable Main Menu</A><BR>");
	    
		if (!createEntryScreen(m_sDBID, m_sUserName)){
    		m_pwOut.println(
					"<META http-equiv='Refresh' content='0;URL=" 
					+ "" + SMUtilities.getURLLinkBase(getServletContext()) + "smar.AREditAROptions"
					+ "?Warning=" + m_sWarning
					+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + m_sDBID
					+ "'>"		
				);
			return;
		}
		
		//End the page:
		m_pwOut.println("</BODY></HTML>");
	}
	private boolean loadAROptionInput(){
		
		//If the class has been passed an AREntryInput query string, just load from that:
		if (m_bInputLoaded){
			m_aropt = new AROptions(m_hsrRequest);
		}else{
			//Have to construct the AREntryInput object here:
			m_aropt = new AROptions();
			//Load the existing entry:
			Connection conn = clsDatabaseFunctions.getConnection(
				getServletContext(), m_sDBID, "MySQL", "smar.AREditAROptions");
			if (conn == null){
		    	m_sWarning = "Could not load aroptions record - connection = null";
		    	return false;
			}	
			if (!m_aropt.load(conn)){
		    	m_sWarning = "Could not load aroptions record - option.load() failed: "
		    		+ m_aropt.getErrorMessageString();
		    	//free the connection
		    	clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1547067528]");
		    	return false;
			}
	    	clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1547067529]");
		}
		return true;
	}
	private boolean createEntryScreen(String sDBID, String sUser){
	    //Start the entry edit form:
		m_pwOut.println("<FORM NAME='ENTRYEDIT' ACTION='" + SMUtilities.getURLLinkBase(getServletContext()) + "smar.AROptionsUpdate' METHOD='POST'>");
		m_pwOut.println("<INPUT TYPE=HIDDEN NAME=\"" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "\" VALUE=\"" + m_sDBID + "\">");
		
		//Start the table:
		m_pwOut.println("<TABLE BORDER=1 CELLSPACING=2 style=\"font-size:75%\">");		
	
		//Export to:
		m_pwOut.println("<TR><TD>Export to:</TD>");
    	m_pwOut.println("<TD><SELECT NAME = \"" + AROptions.Paramiexportto + "\">");
    	
    	//add the first line as a default, so we can tell if they didn't pick one:
    	//m_pwOut.println("<OPTION");
		//m_pwOut.println(" VALUE=\"" + "" + "\">");
		//m_pwOut.println(" - Select an export format - ");
		//m_pwOut.println("</OPTION>");
		
		for (int i = 0; i < SMExportTypes.NUMBER_OF_EXPORT_FORMATS; i++){
	    	m_pwOut.println("<OPTION");
			if (m_aropt.getExportTo().compareToIgnoreCase(
					Long.toString(i)) == 0){
				m_pwOut.println( " selected=yes ");
			}
			m_pwOut.println(" VALUE=\"" + Integer.toString(i) + "\">");
			m_pwOut.println(SMExportTypes.getExportFormatLabel(i));
			m_pwOut.println("</OPTION>");
		}
        m_pwOut.println("</SELECT></TD>");
        m_pwOut.println("<TD>Select the accounting package to which you want to export.</TD></TD>");
        m_pwOut.println("</TR>");
        
		//Uses SMTP Authentication?  (SSL)
		m_pwOut.println("<TR><TD>Enforce credit limits?</TD>");
		if (m_aropt.getEnforceCreditLimit().compareToIgnoreCase("0") == 0){
			m_pwOut.println("<TD><INPUT TYPE=\"CHECKBOX\" NAME=\"" + AROptions.Paramienforcecreditlimit 
					+ "\" ></TD>");
		}else{
			m_pwOut.println("<TD><INPUT TYPE=\"CHECKBOX\" NAME=\"" + AROptions.Paramienforcecreditlimit 
					+ "\" CHECKED></TD>");
		}		m_pwOut.println("<TD>Check to prevent orders from being credit for customers at or over their credit limit</TD></TR>");
		
        
		//Display the fields:
		//Batch posting in process:
		m_pwOut.println("<TR><TD ALIGN=LEFT>Batch posting in process?:</TD>");
		if (m_aropt.getBatchPostingInProcess().compareToIgnoreCase("1") == 0){
			m_pwOut.println("<TD><B>" + "YES" + "</B></TD>");
		}else{
			m_pwOut.println("<TD><B>" + "NO" + "</B></TD>");
		}
		m_pwOut.println("<TD>Indicates whether a batch posting is not complete.</TD>");
		m_pwOut.println("<INPUT TYPE=HIDDEN NAME=\"" + AROptions.ParamBatchPostingInProcess 
			+ "\" VALUE=\"" + m_aropt.getBatchPostingInProcess() + "\">");
		m_pwOut.println("</TR>");

		//Posting user:
		m_pwOut.println("<TR><TD ALIGN=LEFT>Currently posting user:</TD>");
		m_pwOut.println("<TD><B>" + m_aropt.getPostingUserFullName() + "</B></TD>");
		m_pwOut.println("<TD>Blank if posting processes are complete.</TD>");
		m_pwOut.println("<INPUT TYPE=HIDDEN NAME=\"" + AROptions.ParamPostingUserFullName 
				+ "\" VALUE=\"" + m_aropt.getPostingUserFullName() + "\">");
			m_pwOut.println("</TR>");
		
		//Posting process
		m_pwOut.println("<TR><TD ALIGN=LEFT>Currently posting process:</TD>");
		m_pwOut.println("<TD><B>" + m_aropt.getPostingProcess() + "</B></TD>");
		m_pwOut.println("<TD>Blank if posting processes are complete.</TD>");
		m_pwOut.println("<INPUT TYPE=HIDDEN NAME=\"" + AROptions.ParamPostingProcess
				+ "\" VALUE=\"" + m_aropt.getPostingProcess() + "\">");
			m_pwOut.println("</TR>");
		
		//Posting start date
		m_pwOut.println("<TR><TD ALIGN=LEFT>Posting start time:</TD>");
		m_pwOut.println("<TD><B>" + m_aropt.getPostingStartDate() + "</B></TD>");
		m_pwOut.println("<TD>Starting date and time of any current/incomplete posting process.</TD>");
		m_pwOut.println("<INPUT TYPE=HIDDEN NAME=\"" + AROptions.ParamPostingStartDate
				+ "\" VALUE=\"" + m_aropt.getPostingStartDate() + "\">");
			m_pwOut.println("</TR>");
		
		//Google Drive integration:
		m_pwOut.println("<TR><TD ALIGN=LEFT COLSPAN=3><B><U>GOOGLE DRIVE FOLDER CREATION:</U></B></TD></TR>");
		
	    //GDrive customer parent folder ID:
		m_pwOut.println("<TR><TD ALIGN=RIGHT><B>Google Drive CUSTOMER parent folder ID</B>:</TD>");
		m_pwOut.println("<TD><INPUT TYPE=TEXT NAME=\"" 
				+ AROptions.Paramsgdrivecustomerparentfolderid + "\""
				+ " VALUE=\"" + m_aropt.getgdrivecustomerparentfolderid() + "\""
				+ "SIZE=40"
				+ "; MAXLENGTH=" + Integer.toString(SMTablearoptions.gdrivecustomerparentfolderidlength)
				+ ">"
				+ "</TD>");
		m_pwOut.println("<TD>This is the Google Drive 'ID' of the PARENT folder in which new customer folders will be created.</TD></TR>");

		//GDrive customer folder prefix:
		m_pwOut.println("<TR><TD ALIGN=RIGHT><B>CUSTOMER folder prefix</B>:</TD>");
		m_pwOut.println("<TD><INPUT TYPE=TEXT NAME=\"" 
				+ AROptions.Paramsgdrivecustomerfolderprefix  + "\""
				+ " VALUE=\"" + m_aropt.getgdrivecustomerfolderprefix() + "\""
				+ "SIZE=40"
				+ "; MAXLENGTH=" + Integer.toString(SMTablearoptions.gdrivecustomerfolderprefixlength)
				+ ">"
			    + "</TD>");
		m_pwOut.println("<TD>If you want a short prefix in front of the created customer folder name, enter it here.</TD></TR>");
				
		//GDrive customer folder suffix:
		m_pwOut.println("<TR><TD ALIGN=RIGHT><B>CUSTOMER folder suffix</B>:</TD>");
		m_pwOut.println("<TD><INPUT TYPE=TEXT NAME=\"" 
				+ AROptions.Paramsgdrivecustomerfoldersuffix + "\""
				+ " VALUE=\"" + m_aropt.getgdrivecustomerfoldersuffix() + "\""
				+ "SIZE=40"
				+ "; MAXLENGTH=" + Integer.toString(SMTablearoptions.gdrivecustomerfoldersuffixlength)
				+ ">"
				+ "</TD>");
		m_pwOut.println("<TD>If you want a short suffix in front of the created customer folder name, enter it here.</TD></TR>");
				
		m_pwOut.println("</TABLE>");
				
	    m_pwOut.println("<BR><INPUT TYPE=SUBMIT NAME='SubmitEdit' VALUE='Save changes' STYLE='height: 0.24in'>");
				
	    //End the edit form:
	    m_pwOut.println("</FORM>");  

		return true;

	}
	private void get_request_parameters(){
		
	    //Get the session info:
	    HttpSession CurrentSession = m_hsrRequest.getSession(true);
	    m_sDBID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_DATABASE_ID);
	    m_sUserName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERNAME);
	    m_sCompanyName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_COMPANYNAME);
		if (m_hsrRequest.getParameter("OptionInput") != null){
			if (clsManageRequestParameters.get_Request_Parameter("OptionInput", m_hsrRequest).equalsIgnoreCase("True")){
				m_bInputLoaded = true; 
			}else{
				m_bInputLoaded = false;
			}
		}else{
			m_bInputLoaded = false;
		}

		m_sWarning = clsManageRequestParameters.get_Request_Parameter("Warning", m_hsrRequest);
		
	}
	public void doGet(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
		
		doPost(request, response);
	}
}
