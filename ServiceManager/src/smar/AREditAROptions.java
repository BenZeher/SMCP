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

	public void doPost(HttpServletRequest request,
				HttpServletResponse response)
				throws ServletException, IOException {
		
	    PrintWriter pwOut = response.getWriter();
		if (!SMAuthenticate.authenticateSMCPCredentials(
				request, 
				response, 
				getServletContext(),
				SMSystemFunctions.AREditAROptions)){
			return;
		}
		
	    //Get the session info:
	    HttpSession CurrentSession = request.getSession(true);
	    String sDBID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_DATABASE_ID);
	    String sUserName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERNAME);
	    String sCompanyName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_COMPANYNAME);
	    String sWarning = "";
	    boolean m_bInputLoaded = false;
		if (request.getParameter("OptionInput") != null){
			if (clsManageRequestParameters.get_Request_Parameter("OptionInput", request).equalsIgnoreCase("True")){
				m_bInputLoaded = true; 
			}else{
				m_bInputLoaded = false;
			}
		}else{
			m_bInputLoaded = false;
		}

		sWarning = clsManageRequestParameters.get_Request_Parameter("Warning", request);
		
		//Try to load an object from which to build the form:
		AROptions aropt = new AROptions();
		try {
			aropt = loadAROptionInput(request, sDBID, m_bInputLoaded);
		} catch (Exception e) {
			pwOut.println(
					"<META http-equiv='Refresh' content='0;URL=" 
					+ "" + SMUtilities.getURLLinkBase(getServletContext()) + "smar.AREditAROptions"
					+ "?Warning=" + e.getMessage()
					+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
					+ "'>"		
				);
			return;

		}
		
	    String title;
	    String subtitle = "";
	    title = "Edit Accounts Receivable Options";

	    pwOut.println(SMUtilities.SMCPTitleSubBGColor(title, subtitle, SMUtilities.getInitBackGroundColor(getServletContext(), sDBID), sCompanyName));
		
		//If there is a warning from trying to input previously, print it here:
		if (! sWarning.equalsIgnoreCase("")){
			pwOut.println("<B><FONT COLOR=\"RED\">WARNING: " + sWarning + "</FONT></B><BR>");
		}
		
	    //Print a link to main menu:
		pwOut.println("<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMUserLogin?" 
				+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
				+ "\">Return to Main Menu</A><BR>");
		
	    //Print a link to main menu:
		pwOut.println("<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smar.ARMainMenu?" 
				+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
				+ "\">Return to Accounts Receivable Main Menu</A><BR>");
	    
		if (!createEntryScreen(sDBID, sUserName, pwOut, aropt)){
			pwOut.println(
					"<META http-equiv='Refresh' content='0;URL=" 
					+ "" + SMUtilities.getURLLinkBase(getServletContext()) + "smar.AREditAROptions"
					+ "?Warning=" + sWarning
					+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
					+ "'>"		
				);
			return;
		}
		
		//End the page:
		pwOut.println("</BODY></HTML>");
	}
	private AROptions loadAROptionInput(HttpServletRequest req, String sDBID, boolean bInputLoaded) throws Exception{
		
		AROptions m_aropt = new AROptions();
		
		//If the class has been passed an AREntryInput query string, just load from that:
		if (bInputLoaded){
			m_aropt = new AROptions(req);
		}else{
			//Have to construct the AREntryInput object here:
			m_aropt = new AROptions();
			//Load the existing entry:
			Connection conn = clsDatabaseFunctions.getConnection(
				getServletContext(), sDBID, "MySQL", "smar.AREditAROptions");
			if (conn == null){
		    	throw new Exception("Could not load aroptions record - connection = null");
			}	
			if (!m_aropt.load(conn)){
				clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1547067528]");
		    	throw new Exception("Could not load aroptions record - option.load() failed: "
		    		+ m_aropt.getErrorMessageString());
			}
	    	clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1547067529]");
		}
		return m_aropt;
	}
	private boolean createEntryScreen(String sDBID, String sUser, PrintWriter pwOut, AROptions aropt ){
	    //Start the entry edit form:
		pwOut.println("<FORM NAME='ENTRYEDIT' ACTION='" + SMUtilities.getURLLinkBase(getServletContext()) + "smar.AROptionsUpdate' METHOD='POST'>");
		pwOut.println("<INPUT TYPE=HIDDEN NAME=\"" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "\" VALUE=\"" + sDBID + "\">");
		
		//Start the table:
		pwOut.println("<TABLE BORDER=1 CELLSPACING=2 style=\"font-size:75%\">");		
	
		//Export to:
		pwOut.println("<TR><TD>Export to:</TD>");
    	pwOut.println("<TD><SELECT NAME = \"" + AROptions.Paramiexportto + "\">");
    	
    	//add the first line as a default, so we can tell if they didn't pick one:
    	//m_pwOut.println("<OPTION");
		//m_pwOut.println(" VALUE=\"" + "" + "\">");
		//m_pwOut.println(" - Select an export format - ");
		//m_pwOut.println("</OPTION>");
		
		for (int i = 0; i < SMExportTypes.NUMBER_OF_EXPORT_FORMATS; i++){
	    	pwOut.println("<OPTION");
			if (aropt.getExportTo().compareToIgnoreCase(
					Long.toString(i)) == 0){
				pwOut.println( " selected=yes ");
			}
			pwOut.println(" VALUE=\"" + Integer.toString(i) + "\">");
			pwOut.println(SMExportTypes.getExportFormatLabel(i));
			pwOut.println("</OPTION>");
		}
        pwOut.println("</SELECT></TD>");
        pwOut.println("<TD>Select the accounting package to which you want to export.</TD></TD>");
        pwOut.println("</TR>");
        
		//Uses SMTP Authentication?  (SSL)
		pwOut.println("<TR><TD>Enforce credit limits?</TD>");
		if (aropt.getEnforceCreditLimit().compareToIgnoreCase("0") == 0){
			pwOut.println("<TD><INPUT TYPE=\"CHECKBOX\" NAME=\"" + AROptions.Paramienforcecreditlimit 
					+ "\" ></TD>");
		}else{
			pwOut.println("<TD><INPUT TYPE=\"CHECKBOX\" NAME=\"" + AROptions.Paramienforcecreditlimit 
					+ "\" CHECKED></TD>");
		}		pwOut.println("<TD>Check to prevent orders from being credit for customers at or over their credit limit</TD></TR>");
		
        
		//Display the fields:
		//Batch posting in process:
		pwOut.println("<TR><TD ALIGN=LEFT>Batch posting in process?:</TD>");
		if (aropt.getBatchPostingInProcess().compareToIgnoreCase("1") == 0){
			pwOut.println("<TD><B>" + "YES" + "</B></TD>");
		}else{
			pwOut.println("<TD><B>" + "NO" + "</B></TD>");
		}
		pwOut.println("<TD>Indicates whether a batch posting is not complete.</TD>");
		pwOut.println("<INPUT TYPE=HIDDEN NAME=\"" + AROptions.ParamBatchPostingInProcess 
			+ "\" VALUE=\"" + aropt.getBatchPostingInProcess() + "\">");
		pwOut.println("</TR>");

		//Posting user:
		pwOut.println("<TR><TD ALIGN=LEFT>Currently posting user:</TD>");
		pwOut.println("<TD><B>" + aropt.getPostingUserFullName() + "</B></TD>");
		pwOut.println("<TD>Blank if posting processes are complete.</TD>");
		pwOut.println("<INPUT TYPE=HIDDEN NAME=\"" + AROptions.ParamPostingUserFullName 
				+ "\" VALUE=\"" + aropt.getPostingUserFullName() + "\">");
			pwOut.println("</TR>");
		
		//Posting process
		pwOut.println("<TR><TD ALIGN=LEFT>Currently posting process:</TD>");
		pwOut.println("<TD><B>" + aropt.getPostingProcess() + "</B></TD>");
		pwOut.println("<TD>Blank if posting processes are complete.</TD>");
		pwOut.println("<INPUT TYPE=HIDDEN NAME=\"" + AROptions.ParamPostingProcess
				+ "\" VALUE=\"" + aropt.getPostingProcess() + "\">");
			pwOut.println("</TR>");
		
		//Posting start date
		pwOut.println("<TR><TD ALIGN=LEFT>Posting start time:</TD>");
		pwOut.println("<TD><B>" + aropt.getPostingStartDate() + "</B></TD>");
		pwOut.println("<TD>Starting date and time of any current/incomplete posting process.</TD>");
		pwOut.println("<INPUT TYPE=HIDDEN NAME=\"" + AROptions.ParamPostingStartDate
				+ "\" VALUE=\"" + aropt.getPostingStartDate() + "\">");
			pwOut.println("</TR>");
		
		//Google Drive integration:
		pwOut.println("<TR><TD ALIGN=LEFT COLSPAN=3><B><U>GOOGLE DRIVE FOLDER CREATION:</U></B></TD></TR>");
		
	    //GDrive customer parent folder ID:
		pwOut.println("<TR><TD ALIGN=RIGHT><B>Google Drive CUSTOMER parent folder ID</B>:</TD>");
		pwOut.println("<TD><INPUT TYPE=TEXT NAME=\"" 
				+ AROptions.Paramsgdrivecustomerparentfolderid + "\""
				+ " VALUE=\"" + aropt.getgdrivecustomerparentfolderid() + "\""
				+ "SIZE=40"
				+ "; MAXLENGTH=" + Integer.toString(SMTablearoptions.gdrivecustomerparentfolderidlength)
				+ ">"
				+ "</TD>");
		pwOut.println("<TD>This is the Google Drive 'ID' of the PARENT folder in which new customer folders will be created.</TD></TR>");

		//GDrive customer folder prefix:
		pwOut.println("<TR><TD ALIGN=RIGHT><B>CUSTOMER folder prefix</B>:</TD>");
		pwOut.println("<TD><INPUT TYPE=TEXT NAME=\"" 
				+ AROptions.Paramsgdrivecustomerfolderprefix  + "\""
				+ " VALUE=\"" + aropt.getgdrivecustomerfolderprefix() + "\""
				+ "SIZE=40"
				+ "; MAXLENGTH=" + Integer.toString(SMTablearoptions.gdrivecustomerfolderprefixlength)
				+ ">"
			    + "</TD>");
		pwOut.println("<TD>If you want a short prefix in front of the created customer folder name, enter it here.</TD></TR>");
				
		//GDrive customer folder suffix:
		pwOut.println("<TR><TD ALIGN=RIGHT><B>CUSTOMER folder suffix</B>:</TD>");
		pwOut.println("<TD><INPUT TYPE=TEXT NAME=\"" 
				+ AROptions.Paramsgdrivecustomerfoldersuffix + "\""
				+ " VALUE=\"" + aropt.getgdrivecustomerfoldersuffix() + "\""
				+ "SIZE=40"
				+ "; MAXLENGTH=" + Integer.toString(SMTablearoptions.gdrivecustomerfoldersuffixlength)
				+ ">"
				+ "</TD>");
		pwOut.println("<TD>If you want a short suffix in front of the created customer folder name, enter it here.</TD></TR>");
				
		//Feed GL:
		String s = "  <TR>\n"
			+ "    <TD ALIGN=RIGHT><B>GL feed:</B></TD>\n";
		s += "    <TD><SELECT NAME = \"" + AROptions.Paramifeedgl + "\">";

		s += "<OPTION";
		if (aropt.getFeedGl().compareToIgnoreCase(
				Integer.toString(SMTablearoptions.FEED_GL_EXTERNAL_GL_ONLY)) == 0){
			s +=  " selected=yes ";
		}
		s += " VALUE=\"" + Integer.toString(SMTablearoptions.FEED_GL_EXTERNAL_GL_ONLY) + "\">";
		s += "Create GL batch export file for external GLs only";
		s += "</OPTION>";
		
		s += "<OPTION";
		if (aropt.getFeedGl().compareToIgnoreCase(
				Integer.toString(SMTablearoptions.FEED_GL_SMCP_GL_ONLY)) == 0){
			s +=  " selected=yes ";
		}
		s += " VALUE=\"" + Integer.toString(SMTablearoptions.FEED_GL_SMCP_GL_ONLY) + "\">";
		s += "Create GL batch in SMCP GL only";
		s += "</OPTION>";
		
		s += "<OPTION";
		if (aropt.getFeedGl().compareToIgnoreCase(
				Integer.toString(SMTablearoptions.FEED_GL_BOTH_EXTERNAL_AND_SMCP_GL)) == 0){
			s +=  " selected=yes ";
		}
		s += " VALUE=\"" + Integer.toString(SMTablearoptions.FEED_GL_BOTH_EXTERNAL_AND_SMCP_GL) + "\">";
		s += "Create external GL batch AND batch in SMCP GL (normally for testing only)";
		s += "</OPTION>";
		
		s += "</SELECT></TD>";
		s += "<TD>Determines if batch posting creates SMCP GL batches.</TD></TD>";
		s += "</TR>";
		pwOut.println(s);
		
		pwOut.println("</TABLE>");
				
	    pwOut.println("<BR><INPUT TYPE=SUBMIT NAME='SubmitEdit' VALUE='Save changes' STYLE='height: 0.24in'>");
				
	    //End the edit form:
	    pwOut.println("</FORM>");  

		return true;

	}

	public void doGet(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
		
		doPost(request, response);
	}
}
