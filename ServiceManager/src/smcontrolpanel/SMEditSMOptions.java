package smcontrolpanel;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import SMClasses.SMOption;
import SMClasses.SMOptionInput;
import SMDataDefinition.SMTablesmoptions;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsManageRequestParameters;

public class SMEditSMOptions extends HttpServlet {

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
				SMSystemFunctions.SMEditSystemOptions))
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
	    title = "Edit System Options";

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
	    
		SMOptionInput m_OptionInput = (SMOptionInput)CurrentSession.getAttribute(SMOptionInput.ParamObjectName);
		CurrentSession.removeAttribute(SMOptionInput.ParamObjectName);
		if (m_OptionInput == null){
			try {
				m_OptionInput = loadSMOptionInput(m_sDBID);
			} catch (Exception e) {
				m_pwOut.println("<B><FONT COLOR=\"RED\">WARNING: " + e.getMessage() + "</FONT></B><BR>");
				return;
			}
		}
		
		try {
			createEntryScreen(m_sDBID, m_sUserFullName, m_sUserID, m_OptionInput);
		} catch (Exception e) {
			m_pwOut.println("<B><FONT COLOR=\"RED\">WARNING: " + "could not build entry screen - " + e.getMessage() + "</FONT></B><BR>");
			return;
		}
		
		//End the page:
		m_pwOut.println("</BODY></HTML>");
	}
	private SMOptionInput loadSMOptionInput(String sDBID)throws Exception{
		
		//Load the existing entry:
		SMOption option = new SMOption();
		SMOptionInput optioninput = new SMOptionInput();
		Connection conn = clsDatabaseFunctions.getConnection(getServletContext(), sDBID, "MySQL", "smcontrolpanel.SMEditSMOptions");
		if (conn == null){
	    	throw new Exception ("Error [1457450819] - Could not load smoptions record - connection = null");
		}	
		if (!option.load(conn)){
			clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1547080544]");
			throw new Exception ("Error [1457450820] - Could not load smoptions record - option.load() failed: " + option.getErrorMessage());
		}
		if (!optioninput.loadFromSMOptionClass(option)){
			clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1547080545]");
			throw new Exception ("Error [1457450820] - Could not load SM option input from SM Option record");
		}
    	clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1547080546]");
    	return optioninput;
	}
	private void createEntryScreen(String sDBID, String sUserFullName, String sUserID, SMOptionInput optionInput) throws Exception{
	    //Start the entry edit form:
		m_pwOut.println("<FORM NAME='ENTRYEDIT' ACTION='" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMOptionUpdate' METHOD='POST'>");
		m_pwOut.println("<INPUT TYPE=HIDDEN NAME=\"" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "\" VALUE=\"" + sDBID + "\">");
		m_pwOut.println("<INPUT TYPE=HIDDEN NAME=\"" + SMOptionInput.ParamLastEditUserFullName + "\" VALUE=\"" + sUserFullName + "\">");
		m_pwOut.println("<INPUT TYPE=HIDDEN NAME=\"" + SMOptionInput.ParamLastEditUserID + "\" VALUE=\"" + sUserID + "\">");
		m_pwOut.println("<INPUT TYPE=HIDDEN NAME=\"" + SMOptionInput.ParamLastEditProcess + "\" VALUE=\"" + "Edit Options" + "\">");
		
		//Start the table:
		m_pwOut.println("<TABLE BORDER=1 CELLSPACING=2 style=\"font-size:75%\">");		
	
		//Display the fields:
		//Dummy key:
		m_pwOut.println("<TR><TD ALIGN=RIGHT><B>Dummy key</B>:</TD>");
		m_pwOut.println("<TD><B>" + optionInput.getM_sdummykey() + "</B></TD>");
		m_pwOut.println("<TD>Internal key - cannot be edited</TD></TR>");

		//Next Order Uniquifier:
		m_pwOut.println("<TR><TD ALIGN=RIGHT><B>Next order uniquifier</B>:</TD>");
		m_pwOut.println("<TD><B>" + optionInput.getM_snextorderuniquifier() + "</B></TD>");
		m_pwOut.println("<TD>Used to sequence order records</TD></TR>");
		
		//Next Order Number
		m_pwOut.println("<TR><TD ALIGN=RIGHT><B>Next order number</B>:</TD>");
		m_pwOut.println("<TD><B>" + optionInput.getM_snextordernumber() + "</B></TD>");
		m_pwOut.println("<TD>Order number of next order to be created</TD></TR>");
		
		//Last Edit User
		m_pwOut.println("<TR><TD ALIGN=RIGHT><B>Last edit user</B>:</TD>");
		m_pwOut.println("<TD><B>" + optionInput.getM_slastedituserfullname() 
		+ " (" + optionInput.getM_llastedituserid() + ")" + "</B></TD>");
		m_pwOut.println("<TD>Last user to update SM options</TD></TR>");
		
		//Last edit process
		m_pwOut.println("<TR><TD ALIGN=RIGHT><B>Last edit process</B>:</TD>");
		m_pwOut.println("<TD><B>" + optionInput.getM_slasteditprocess() + "</B></TD>");
		m_pwOut.println("<TD>Last process to update SM Options</TD></TR>");
		
		//Last Edit date:
		m_pwOut.println("<TR><TD ALIGN=RIGHT><B>Last edit date</B>:</TD>");
		m_pwOut.println("<TD><B>" + optionInput.getM_slasteditdate() + "</B></TD>");
		m_pwOut.println("<TD>Date of last update to options (YYYYMMDD)</TD></TR>");
		
		//Last edit time
		m_pwOut.println("<TR><TD ALIGN=RIGHT><B>Last edit time</B>:</TD>");
		m_pwOut.println("<TD><B>" + optionInput.getM_slastedittime() + "</B></TD>");
		m_pwOut.println("<TD>Time of last update to options (milliseconds)</TD></TR>");
		
		//Next invoice number:
		m_pwOut.println("<TR><TD ALIGN=RIGHT><B>Next invoice number</B>:</TD>");
		m_pwOut.println("<TD><B>" + optionInput.getM_snextinvoicenumber() + "</B></TD>");
		m_pwOut.println("<TD>Invoice number of next invoice to be created</TD></TR>");
		
		
		//General Settings
		m_pwOut.println("<TR style=\"background-color:grey; color:white; \">"
			+ "<TD COLSPAN=3>"+ "<B>&nbsp;General </B>"+ "</TD>"+ "</TR>");
			;
			
		//background color:
		m_pwOut.println("<TR><TD ALIGN=RIGHT><B>Program background color</B>:</TD>");
		m_pwOut.println("<TD><INPUT TYPE=TEXT NAME=\"" 
			+ SMOptionInput.Paramsbackgroundcolor + "\""
			+ " VALUE=\"" + optionInput.getM_sbackgroundcolor() + "\""
			+ "SIZE=20"
			+ "; MAXLENGTH=" + Integer.toString(SMTablesmoptions.sbackgroundcolorlength)
			+ " class=\"color\""
			+ ">"
			+ "</TD>"
			//+ "&nbsp;" + SMUtilities.getColorPickerString(SMOptionInput.Paramsbackgroundcolor, getServletContext())
			);
		m_pwOut.println("<TD>The 'hex' version of the selected color (default is " + SMUtilities.DEFAULT_BK_COLOR + ")"
			+ " (you can click in the text box to see a color picker)</TD></TR>");
			
		//signature box width:
		m_pwOut.println("<TR><TD ALIGN=RIGHT><B>Work order signature box size</B>:</TD>");
		m_pwOut.println("<TD><INPUT TYPE=TEXT NAME=\"" 
			+ SMOptionInput.Paramisigantureboxwidth + "\""
			+ " VALUE=\"" + optionInput.getisignatureboxwidth() + "\""
			+ "SIZE=20"
			+ "; MAXLENGTH=" + Integer.toString(SMTablesmoptions.isignatureboxwidthlength)
			+ ">"
			+ "</TD>"
			//+ "&nbsp;" + SMUtilities.getColorPickerString(SMOptionInput.Paramsbackgroundcolor, getServletContext())
			);
		m_pwOut.println("<TD>This is the <B><I>WIDTH</I></B> in pixels of the sigature box that is displayed on Work Orders. "
				+ "The height of the box will be adjusted automatically based on the width of the box. (Default width is <B>400</B>)</TD></TR>");
		
		//Time card database:
		m_pwOut.println("<TR><TD ALIGN=RIGHT><B>Time card database</B>:</TD>");
		m_pwOut.println("<TD><INPUT TYPE=TEXT NAME=\"" 
				+ SMOptionInput.ParamTimeCardDatabase + "\""
				+ " VALUE=\"" + optionInput.getM_stimecarddatabase() + "\""
				+ "SIZE=40"
				+ "; MAXLENGTH=" + Integer.toString(SMTablesmoptions.stimecarddatabaselength)
				+ ">"
				+ "</TD>");
		m_pwOut.println("<TD>The EXACT name of the time card system database, if used</TD></TR>");
		
		//Email Settings
		m_pwOut.println("<TR style=\"background-color:grey; color:white; \">"
			+ "<TD COLSPAN=3>"+ "<B>&nbsp;System Email</B>"+ "</TD>"+ "</TR>");
			;
		
		//SMTP server:
		m_pwOut.println("<TR><TD ALIGN=RIGHT><B>SMTP Server</B>:</TD>");
		m_pwOut.println("<TD><INPUT TYPE=TEXT NAME=\"" 
				+ SMOptionInput.ParamSMTPServer + "\""
				+ " VALUE=\"" + optionInput.getM_ssmtpserver() + "\""
				+ "SIZE=40"
				+ "; MAXLENGTH=" + Integer.toString(SMTablesmoptions.ssmtpserverlength)
				+ ">"
				+ "</TD>");
		m_pwOut.println("<TD>Name or address of SMTP server to use for sending email</TD></TR>");
		
		//SMTP Port:
		m_pwOut.println("<TR><TD ALIGN=RIGHT><B>SMTP Port</B>:</TD>");
		m_pwOut.println("<TD><INPUT TYPE=TEXT NAME=\"" 
				+ SMOptionInput.ParamSMTPPort + "\""
				+ " VALUE=\"" + optionInput.getM_ssmtpport() + "\""
				+ "SIZE=10"
				+ "; MAXLENGTH=" + Integer.toString(SMTablesmoptions.ssmtpportlength)
				+ ">"
				+ "</TD>");
		m_pwOut.println("<TD>SMTP port to use for sending email</TD></TR>");
		
		//SMTP Source server name:
		m_pwOut.println("<TR><TD ALIGN=RIGHT><B>SMTP sending server name</B>:</TD>");
		m_pwOut.println("<TD><INPUT TYPE=TEXT NAME=\"" 
				+ SMOptionInput.ParamSMTPSourceServerName + "\""
				+ " VALUE=\"" + optionInput.getM_ssmtpsourceservername() + "\""
				+ "SIZE=40"
				+ "; MAXLENGTH=" + Integer.toString(SMTablesmoptions.ssmtpsourceservernamelength)
				+ ">"
				+ "</TD>");
		m_pwOut.println("<TD>Your server name when sending email</TD></TR>");

		//Uses SMTP Authentication?  (SSL)
		m_pwOut.println("<TR><TD ALIGN=RIGHT><B>Use SMTP Authentication?</B></TD>");
		if (optionInput.getM_ssmtpauthentication().compareToIgnoreCase("0") == 0){
			m_pwOut.println("<TD><INPUT TYPE=\"CHECKBOX\" NAME=\"" + SMOptionInput.Paramismtpauthentication 
					+ "\" ></TD>");
		}else{
			m_pwOut.println("<TD><INPUT TYPE=\"CHECKBOX\" NAME=\"" + SMOptionInput.Paramismtpauthentication 
					+ "\" CHECKED></TD>");
		}		m_pwOut.println("<TD>Check if mail server requires authentication (SSL)</TD></TR>");
		
		//SMTP user name:
		m_pwOut.println("<TR><TD ALIGN=RIGHT><B>SMTP user name</B>:</TD>");
		m_pwOut.println("<TD><INPUT TYPE=TEXT NAME=\"" 
				+ SMOptionInput.Paramssmtpusername + "\""
				+ " VALUE=\"" + optionInput.getM_ssmtpusername() + "\""
				+ "SIZE=40"
				+ "; MAXLENGTH=" + Integer.toString(SMTablesmoptions.ssmtpusernamelength)
				+ ">"
				+ "</TD>");
		m_pwOut.println("<TD>Username for authenticating on SMTP server (if needed).  This user will appear as sender"
				+ " on any emails sent from within the system, including: new customer and new vendor notifications,"
				+ " order cancellation notifications, work order receipts, appointments, and invoices.</TD></TR>");
		
		//SMTP password:
		m_pwOut.println("<TR><TD ALIGN=RIGHT><B>SMTP user password</B>:</TD>");
		m_pwOut.println("<TD><INPUT TYPE=TEXT NAME=\"" 
				+ SMOptionInput.Paramssmtppassword + "\""
				+ " VALUE=\"" + optionInput.getM_ssmtppassword() + "\""
				+ "SIZE=40"
				+ "; MAXLENGTH=" + Integer.toString(SMTablesmoptions.ssmtppasswordlength)
				+ ">"
				+ "</TD>");
		m_pwOut.println("<TD>Password for authenticating on SMTP server (if needed)</TD></TR>");

		//SMTP 'reply-to' address:
		m_pwOut.println("<TR><TD ALIGN=RIGHT><B>Reply-to address</B>:</TD>");
		m_pwOut.println("<TD><INPUT TYPE=TEXT NAME=\"" 
				+ SMOptionInput.Paramssmtpreplytoname + "\""
				+ " VALUE=\"" + optionInput.getM_ssmtpreplytoname() + "\""
				+ "SIZE=40"
				+ "; MAXLENGTH=" + Integer.toString(SMTablesmoptions.ssmtpreplytonamelength)
				+ ">"
				+ "</TD>");
		m_pwOut.println("<TD>This address will appear as the"
				+ " 'Reply-to' on any emails sent from within the system, including: new customer and new vendor notifications,"
				+ " order cancellation notifications, work order receipts, appointments, and invoices.</TD></TR>");
		

		
		//Export Settings
		m_pwOut.println("<TR style=\"background-color:grey; color:white; \">"
			+ "<TD COLSPAN=3>"+ "<B>&nbsp;File Export</B>"+ "</TD>"+ "</TR>");
			;
		//Order Documents FTP Url:
		m_pwOut.println("<TR><TD ALIGN=RIGHT><B>Order Documents FTP Url</B>:</TD>");
		m_pwOut.println("<TD><INPUT TYPE=TEXT NAME=\"" 
			+ SMOptionInput.ParamOrderDocsFTPUrl + "\""
			+ " VALUE=\"" + optionInput.getM_sorderdocsftpurl() + "\""
			+ "SIZE=40"
			+ "; MAXLENGTH=" + Integer.toString(SMTablesmoptions.sorderdocsftpurllength)
			+ ">"
			+ "</TD>");
		m_pwOut.println("<TD>FTP Url to parent of order docs folder</TD></TR>");
			
		//Sales Lead Documents FTP Url:
		m_pwOut.println("<TR><TD ALIGN=RIGHT><B>" + SMBidEntry.ParamObjectName + " Documents FTP Url</B>:</TD>");
		m_pwOut.println("<TD><INPUT TYPE=TEXT NAME=\"" 
			+ SMOptionInput.ParamBidDocsFTPUrl + "\""
			+ " VALUE=\"" + optionInput.getM_sbiddocsftpurl() + "\""
			+ "SIZE=40"
			+ "; MAXLENGTH=" + Integer.toString(SMTablesmoptions.sbiddocsftpurllength)
			+ ">"
			+ "</TD>");
		m_pwOut.println("<TD>FTP Url to parent of order docs folder</TD></TR>");
			
		//Export Path:
		m_pwOut.println("<TR><TD ALIGN=RIGHT><B>Export Path</B>:</TD>");
		m_pwOut.println("<TD><INPUT TYPE=TEXT NAME=\"" 
				+ SMOptionInput.ParamFileExportPath + "\""
				+ " VALUE=\"" + optionInput.getM_sfileexportpath() + "\""
				+ "SIZE=40"
				+ "; MAXLENGTH=" + Integer.toString(SMTablesmoptions.sfileexportpathlength)
				+ ">"
				+ "</TD>");
		m_pwOut.println("<TD>Path in which to create file exports. <B>NOTE: If you are uploading to FTP, "
				+ "export files will be created in this path, then deleted after they are exported.</B></TD></TR>");
		
		//FTP Export URL
		m_pwOut.println("<TR><TD ALIGN=RIGHT><B>FTP export URL</B>:</TD>");
		m_pwOut.println("<TD><INPUT TYPE=TEXT NAME=\"" 
				+ SMOptionInput.Paramsftpexporturl + "\""
				+ " VALUE=\"" + optionInput.getftpexporturl() + "\""
				+ "SIZE=40"
				+ "; MAXLENGTH=" + Integer.toString(SMTablesmoptions.sftpexporturllength)
				+ ">"
				+ "</TD>"
				);
		m_pwOut.println("<TD>The URL of the FTP server to receive export files (leave blank if FTP exports are not used)."
				+ "  If a non-standard port is used, add it to the end of the URL, separated by a colon (e.g. ftpurl.com:1234).</TD></TR>");
		
		//FTP Export User
		m_pwOut.println("<TR><TD ALIGN=RIGHT><B>FTP export user</B>:</TD>");
		m_pwOut.println("<TD><INPUT TYPE=TEXT NAME=\"" 
				+ SMOptionInput.Paramsftpexportuser + "\""
				+ " VALUE=\"" + optionInput.getftpexportuser() + "\""
				+ "SIZE=40"
				+ "; MAXLENGTH=" + Integer.toString(SMTablesmoptions.sftpexportuserlength)
				+ ">"
				+ "</TD>"
				);
		m_pwOut.println("<TD>The FTP user (leave blank if FTP exports are not used).</TD></TR>");
		
		//FTP Export password
		m_pwOut.println("<TR><TD ALIGN=RIGHT><B>FTP export password</B>:</TD>");
		m_pwOut.println("<TD><INPUT TYPE=TEXT NAME=\"" 
				+ SMOptionInput.Paramsftpexportpw + "\""
				+ " VALUE=\"" + optionInput.getftpexportpw() + "\""
				+ "SIZE=40"
				+ "; MAXLENGTH=" + Integer.toString(SMTablesmoptions.sftpexportpwlength)
				+ ">"
				+ "</TD>"
				);
		m_pwOut.println("<TD>The FTP user's password (leave blank if FTP exports are not used).</TD></TR>");
		
		//FTP Export Path
		m_pwOut.println("<TR><TD ALIGN=RIGHT><B>FTP export path</B>:</TD>");
		m_pwOut.println("<TD><INPUT TYPE=TEXT NAME=\"" 
				+ SMOptionInput.Paramsftpfileexportpath + "\""
				+ " VALUE=\"" + optionInput.getftpfileexportpath() + "\""
				+ "SIZE=40"
				+ "; MAXLENGTH=" + Integer.toString(SMTablesmoptions.sftpfileexportpathlength)
				+ ">"
				+ "</TD>"
				);
		m_pwOut.println("<TD>The directory on the FTP server into which export files should be uploaded (leave blank if FTP exports are not used)  For example: '/SMCPUploads/company1/'.</TD></TR>");
			
		//Transaction Settings
		m_pwOut.println("<TR style=\"background-color:grey; color:white; \">"
			+ "<TD COLSPAN=3>"+ "<B>&nbsp;Transaction Control</B>"+ "</TD>"+ "</TR>");
			;
		
		//Current posting period dates:
		
		//Current period start date:
		m_pwOut.println("<TR><TD ALIGN=RIGHT><B>Current posting period start date</B>:</TD>");
		m_pwOut.println("<TD><INPUT TYPE=TEXT NAME=\"" 
				+ SMOptionInput.Paramdatcurrentperiodstartdate + "\""
				+ " VALUE=\"" + optionInput.getscurrentperiodstartdate() + "\""
				+ "SIZE=10"
				+ "; MAXLENGTH=" + "10"
				+ ">"
				+ SMUtilities.getDatePickerString(SMOptionInput.Paramdatcurrentperiodstartdate, getServletContext())
				+ "</TD>"
				);
		m_pwOut.println("<TD>To enforce the posting period in transactions, enter starting and ending posting period dates.</TD></TR>");
				
		//Current period end date:
		m_pwOut.println("<TR><TD ALIGN=RIGHT><B>Current posting period end date</B>:</TD>");
		m_pwOut.println("<TD><INPUT TYPE=TEXT NAME=\"" 
				+ SMOptionInput.Paramdatcurrentperiodenddate + "\""
				+ " VALUE=\"" + optionInput.getscurrentperiodenddate() + "\""
				+ "SIZE=10"
				+ "; MAXLENGTH=" + "10"
				+ ">"
				+ SMUtilities.getDatePickerString(SMOptionInput.Paramdatcurrentperiodenddate, getServletContext())
				+ "</TD>"
				);
		m_pwOut.println("<TD>Ending posting period date.</TD></TR>");
		
		//Create bank reconciliations?
		m_pwOut.println("<TR><TD ALIGN=RIGHT><B>Automatically create bank reconciliations?</B></TD>");
		if (optionInput.getcreatebankrecexport().compareToIgnoreCase("0") == 0){
			m_pwOut.println("<TD><INPUT TYPE=\"CHECKBOX\" NAME=\"" + SMOptionInput.Paramicreatebankrecexport 
					+ "\" ></TD>");
		}else{
			m_pwOut.println("<TD><INPUT TYPE=\"CHECKBOX\" NAME=\"" + SMOptionInput.Paramicreatebankrecexport 
					+ "\" CHECKED></TD>");
		}		m_pwOut.println("<TD>Check to automatically create bank reconciliation exports when cash batches are posted.</TD></TR>");
		
		//Feed GL:
		String s = "  <TR>\n"
			+ "    <TD ALIGN=RIGHT><B>GL feed:</B></TD>\n";
		s += "    <TD><SELECT NAME = \"" + SMOptionInput.Paramifeedgl + "\">";

		s += "<OPTION";
		if (optionInput.getsfeedgl().compareToIgnoreCase(
				Integer.toString(SMTablesmoptions.FEED_GL_EXTERNAL_GL_ONLY)) == 0){
			s +=  " selected=yes ";
		}
		s += " VALUE=\"" + Integer.toString(SMTablesmoptions.FEED_GL_EXTERNAL_GL_ONLY) + "\">";
		s += "Create GL batch export file for external GLs only";
		s += "</OPTION>";
		
		s += "<OPTION";
		if (optionInput.getsfeedgl().compareToIgnoreCase(
				Integer.toString(SMTablesmoptions.FEED_GL_SMCP_GL_ONLY)) == 0){
			s +=  " selected=yes ";
		}
		s += " VALUE=\"" + Integer.toString(SMTablesmoptions.FEED_GL_SMCP_GL_ONLY) + "\">";
		s += "Create GL batch in SMCP GL only";
		s += "</OPTION>";
		
		s += "<OPTION";
		if (optionInput.getsfeedgl().compareToIgnoreCase(
				Integer.toString(SMTablesmoptions.FEED_GL_BOTH_EXTERNAL_AND_SMCP_GL)) == 0){
			s +=  " selected=yes ";
		}
		s += " VALUE=\"" + Integer.toString(SMTablesmoptions.FEED_GL_BOTH_EXTERNAL_AND_SMCP_GL) + "\">";
		s += "Create external GL batch AND batch in SMCP GL (normally for testing only)";
		s += "</OPTION>";
		
		s += "</SELECT></TD>";
		s += "<TD>Determines if batch posting creates SMCP GL batches.</TD></TD>";
		s += "</TR>";
		
		m_pwOut.println(s);
		
		//Google Integration
		m_pwOut.println("<TR style=\"background-color:grey; color:white; \">"
			+ "<TD COLSPAN=3>"+ "<B>&nbsp;Google Integration</B>"+ "</TD>"+ "</TR>");
			;
		
		//Google Drive integration:
		//m_pwOut.println("<TR><TD ALIGN=LEFT COLSPAN=3><B><U>GOOGLE DRIVE FOLDER INTEGRATION:</U></B></TD></TR>");
		
		//GDrive order parent folder ID:
		m_pwOut.println("<TR><TD ALIGN=RIGHT><B>Google Drive ORDER parent folder ID</B>:</TD>");
		m_pwOut.println("<TD><INPUT TYPE=TEXT NAME=\"" 
				+ SMOptionInput.Paramsgdriveorderparentfolderid + "\""
				+ " VALUE=\"" + optionInput.getgdriveorderparentfolderid() + "\""
				+ "SIZE=40"
				+ "; MAXLENGTH=" + Integer.toString(SMTablesmoptions.gdriveorderparentfolderidlength)
				+ ">"
				+ "</TD>");
		m_pwOut.println("<TD>This is the Google Drive 'ID' of the PARENT folder in which new order folders will be created.</TD></TR>");
		
		//GDrive order folder prefix:
		m_pwOut.println("<TR><TD ALIGN=RIGHT><B>ORDER folder prefix</B>:</TD>");
		m_pwOut.println("<TD><INPUT TYPE=TEXT NAME=\"" 
				+ SMOptionInput.Paramsgdriveorderfolderprefix + "\""
				+ " VALUE=\"" + optionInput.getgdriveorderfolderprefix() + "\""
				+ "SIZE=40"
				+ "; MAXLENGTH=" + Integer.toString(SMTablesmoptions.gdriveorderfolderprefixlength)
				+ ">"
				+ "</TD>");
		m_pwOut.println("<TD>If you want a short prefix in front of the created order folder name, enter it here.</TD></TR>");
		
		//GDrive order folder suffix:
		m_pwOut.println("<TR><TD ALIGN=RIGHT><B>ORDER folder suffix</B>:</TD>");
		m_pwOut.println("<TD><INPUT TYPE=TEXT NAME=\"" 
				+ SMOptionInput.Paramsgdriveorderfoldersuffix + "\""
				+ " VALUE=\"" + optionInput.getgdriveorderfoldersuffix() + "\""
				+ "SIZE=40"
				+ "; MAXLENGTH=" + Integer.toString(SMTablesmoptions.gdriveorderfoldersuffixlength)
				+ ">"
				+ "</TD>");
		m_pwOut.println("<TD>If you want a short suffix at the end of the created order folder name, enter it here.</TD></TR>");

		//GDrive sales lead parent folder ID:
		m_pwOut.println("<TR><TD ALIGN=RIGHT><B>Google Drive SALES LEAD parent folder ID</B>:</TD>");
		m_pwOut.println("<TD><INPUT TYPE=TEXT NAME=\"" 
				+ SMOptionInput.Paramsgdrivesalesleadparentfolderid + "\""
				+ " VALUE=\"" + optionInput.getgdrivesalesleadparentfolderid() + "\""
				+ "SIZE=40"
				+ "; MAXLENGTH=" + Integer.toString(SMTablesmoptions.gdrivesalesleadparentfolderidlength)
				+ ">"
				+ "</TD>");
		m_pwOut.println("<TD>This is the Google Drive 'ID' of the PARENT folder in which new saleslead folders will be created.</TD></TR>");
		
		//GDrive sales lead folder prefix:
		m_pwOut.println("<TR><TD ALIGN=RIGHT><B>SALES LEAD folder prefix</B>:</TD>");
		m_pwOut.println("<TD><INPUT TYPE=TEXT NAME=\"" 
				+ SMOptionInput.Paramsgdrivesalesleadfolderprefix + "\""
				+ " VALUE=\"" + optionInput.getgdrivesalesleadfolderprefix() + "\""
				+ "SIZE=40"
				+ "; MAXLENGTH=" + Integer.toString(SMTablesmoptions.gdrivesalesleadfolderprefixlength)
				+ ">"
				+ "</TD>");
		m_pwOut.println("<TD>If you want a short prefix in front of the created sales lead folder name, enter it here.</TD></TR>");
		
		//GDrive sales lead folder suffix:
		m_pwOut.println("<TR><TD ALIGN=RIGHT><B>SALES LEAD folder suffix</B>:</TD>");
		m_pwOut.println("<TD><INPUT TYPE=TEXT NAME=\"" 
				+ SMOptionInput.Paramsgdrivesalesleadfoldersuffix + "\""
				+ " VALUE=\"" + optionInput.getgdrivesalesleadfoldersuffix() + "\""
				+ "SIZE=40"
				+ "; MAXLENGTH=" + Integer.toString(SMTablesmoptions.gdrivesalesleadfoldersuffixlength)
				+ ">"
				+ "</TD>");
		m_pwOut.println("<TD>If you want a short suffix at the end of the created sales lead folder name, enter it here.</TD></TR>");
		
		//GDrive work order parent folder ID:
				m_pwOut.println("<TR><TD ALIGN=RIGHT><B>Google Drive WORK ORDER parent folder ID</B>:</TD>");
				m_pwOut.println("<TD><INPUT TYPE=TEXT NAME=\"" 
						+ SMOptionInput.Paramsgdriveworkorderparentfolderid + "\""
						+ " VALUE=\"" + optionInput.getgdriveworkorderparentfolderid() + "\""
						+ "SIZE=40"
						+ "; MAXLENGTH=" + Integer.toString(SMTablesmoptions.gdriveworkorderparentfolderidlength)
						+ ">"
						+ "</TD>");
				m_pwOut.println("<TD>This is the Google Drive 'ID' of the PARENT folder in which new work order folders will be created.</TD></TR>");
				
				//GDrive work order folder prefix:
				m_pwOut.println("<TR><TD ALIGN=RIGHT><B>WORK ORDER folder prefix</B>:</TD>");
				m_pwOut.println("<TD><INPUT TYPE=TEXT NAME=\"" 
						+ SMOptionInput.Paramsgdriveworkorderfolderprefix + "\""
						+ " VALUE=\"" + optionInput.getgdriveworkorderfolderprefix() + "\""
						+ "SIZE=40"
						+ "; MAXLENGTH=" + Integer.toString(SMTablesmoptions.gdriveworkorderfolderprefixlength)
						+ ">"
						+ "</TD>");
				m_pwOut.println("<TD>If you want a short prefix in front of the created work order folder name, enter it here.</TD></TR>");
				
				//GDrive work order folder suffix:
				m_pwOut.println("<TR><TD ALIGN=RIGHT><B>WORK ORDER folder suffix</B>:</TD>");
				m_pwOut.println("<TD><INPUT TYPE=TEXT NAME=\"" 
						+ SMOptionInput.Paramsgdriveworkorderfoldersuffix + "\""
						+ " VALUE=\"" + optionInput.getgdriveworkorderfoldersuffix() + "\""
						+ "SIZE=40"
						+ "; MAXLENGTH=" + Integer.toString(SMTablesmoptions.gdriveworkorderfoldersuffixlength)
						+ ">"
						+ "</TD>");
				m_pwOut.println("<TD>If you want a short suffix at the end of the created work order folder name, enter it here.</TD></TR>");

				
				//GDrive Labor Back-Charges parent folder ID:
				m_pwOut.println("<TR><TD ALIGN=RIGHT><B>Google Drive LABOR BACKCHARGE parent folder ID</B>:</TD>");
				m_pwOut.println("<TD><INPUT TYPE=TEXT NAME=\"" 
						+ SMOptionInput.Paramsgdrivelaborbackchargeparentfolderid + "\""
						+ " VALUE=\"" + optionInput.getgdrivelaborbackchargeparentfolderid() + "\""
						+ "SIZE=40"
						+ "; MAXLENGTH=" + Integer.toString(SMTablesmoptions.gdrivelaborbackchargeparentfolderidlength)
						+ ">"
						+ "</TD>");
				m_pwOut.println("<TD>This is the Google Drive 'ID' of the PARENT folder in which new labor backcharge folders will be created.</TD></TR>");
				
				//GDrive labor backcharges folder prefix:
				m_pwOut.println("<TR><TD ALIGN=RIGHT><B>LABOR BACKCHARGE folder prefix</B>:</TD>");
				m_pwOut.println("<TD><INPUT TYPE=TEXT NAME=\"" 
						+ SMOptionInput.Paramsgdrivelaborbackchargeprefix + "\""
						+ " VALUE=\"" + optionInput.getgdrivelaborbackchargefolderprefix() + "\""
						+ "SIZE=40"
						+ "; MAXLENGTH=" + Integer.toString(SMTablesmoptions.gdrivelaborbackchargefolderprefixlength)
						+ ">"
						+ "</TD>");
				m_pwOut.println("<TD>If you want a short prefix in front of the created labor backcharge folder name, enter it here.</TD></TR>");
								
				
		//Create upload file URL
		m_pwOut.println("<TR><TD ALIGN=RIGHT><B>Create folder/Upload file web app URL</B>:</TD>");
		m_pwOut.println("<TD><INPUT TYPE=TEXT NAME=\"" 
				+ SMOptionInput.Paramsgdriveuploadfileurl + "\""
				+ " VALUE=\"" + optionInput.getgdriveuploadfileurl() + "\""
				+ "SIZE=40"
				+ "; MAXLENGTH=" + Integer.toString(SMTablesmoptions.gdriveuploadfileurllength)
				+ ">"
				+ "</TD>");
		m_pwOut.println("<TD>This is the URL for the web app that will created folders and upload files to the appropriate folder in Google Drive.</TD></TR>");		
		
		//Create rename folder URL
		m_pwOut.println("<TR><TD ALIGN=RIGHT><B>Rename folder web app URL</B>:</TD>");
		m_pwOut.println("<TD><INPUT TYPE=TEXT NAME=\"" 
				+ SMOptionInput.Paramsgdriverenamefolderurl + "\""
				+ " VALUE=\"" + optionInput.getgdriverenamefolderurl() + "\""
				+ "SIZE=40"
				+ "; MAXLENGTH=" + Integer.toString(SMTablesmoptions.gdriverenamefolderurllength)
				+ ">"
				+ "</TD>");
		m_pwOut.println("<TD>This is the URL to the page that will update the name of a folder in Google Drive.</TD></TR>");		
		
		//Create 'COPY sales lead folder URL to order' checkbox
		m_pwOut.println("<TR><TD ALIGN=RIGHT><B>Automatically copy sales lead folder URL to order header?</B></TD>");
		if (optionInput.getcopysalesleadfolderurltoorder().compareToIgnoreCase("0") == 0){
			m_pwOut.println("<TD><INPUT TYPE=\"CHECKBOX\" NAME=\"" + SMOptionInput.Paramicopysalesleadfolderurltoorder
					+ "\" ></TD>");
		}else{
			m_pwOut.println("<TD><INPUT TYPE=\"CHECKBOX\" NAME=\"" + SMOptionInput.Paramicopysalesleadfolderurltoorder
					+ "\" CHECKED></TD>");
		}		m_pwOut.println("<TD>Check to automatically copy the sales lead folder URL to the corresponding order header.</TD></TR>");		
		
		//Google API Settings
		//Note: consider hiding this information in a json file on the server so it is not public in the html source when loading the APIs.. 
		m_pwOut.println("<TR style=\"background-color:grey; color:white; \">"
					+ "<TD COLSPAN=3>"+ "<B>&nbsp;Google API Settings</B>"+ "</TD>"+ "</TR>");
		
		//Google Account
		m_pwOut.println("<TR><TD ALIGN=RIGHT><B>Master email account</B>:</TD>");
		m_pwOut.println("<TD><INPUT TYPE=TEXT NAME=\"" 
				+ SMOptionInput.Paramsgoogledomain + "\""
				+ " VALUE=\"" + optionInput.getsgoogledomain() + "\""
				+ "SIZE=40"
				+ "; MAXLENGTH=" + Integer.toString(SMTablesmoptions.igoogleapikeylength)
				+ ">"
				+ "</TD>"
			);
		m_pwOut.println("<TD>This will restrict APIs to only be accessed by users in your domain and will be the owner of all uploaded folders and files. "
				+ "You must authoize the drive scope for your domain. "
				+ "All API credentials below can be managed from the <a href=\"https://console.developers.google.com/apis/dashboard\"> Google API Console </a>");
		
		//Google API Key:
		m_pwOut.println("<TR><TD ALIGN=RIGHT><B>Google API key</B>:</TD>");
		m_pwOut.println("<TD><INPUT TYPE=TEXT NAME=\"" 
				+ SMOptionInput.Paramsgoogleapikey + "\""
				+ " VALUE=\"" + optionInput.getsgoogleapikey() + "\""
				+ "SIZE=40"
				+ "; MAXLENGTH=" + Integer.toString(SMTablesmoptions.igoogleapikeylength)
				+ ">"
				+ "<br>"
				+ "<a href=\"" + SMGeocoder.GEOCODER_REQUEST_PREFIX_FOR_XML 
				+ "?address=USA"
				+ "&key=" + optionInput.getsgoogleapikey() + "&sensor=false" + "\">"
				//test Geocoding API
				+ "<button type=\"button\">Test geocoding API </button></a>*save before testing"
				+ "</TD>"
			);
		m_pwOut.println("<TD>This key is required to use basic geocoding function for mapping addresses. Enable Maps and Places. "
				+ ""
				+ " <a href=\"https://cloud.google.com/maps-platform/?apis=maps,places\"" + 
				 ">Get an API key</a></TD></TR>");
		
		//Use google Places API 
		m_pwOut.println("<TR><TD ALIGN=RIGHT><B>Use Places API?</B>:</TD>");
		if (optionInput.getiusegoogleplacesapi().compareToIgnoreCase("0") == 0){
			m_pwOut.println("<TD><INPUT TYPE=\"CHECKBOX\" NAME=\"" + SMOptionInput.Paramiusegoogleplacesapi 
					+ "\" ></TD>");
		}else{
			m_pwOut.println("<TD><INPUT TYPE=\"CHECKBOX\" NAME=\"" + SMOptionInput.Paramiusegoogleplacesapi 
					+ "\" CHECKED></TD>");
		}	
		m_pwOut.println("<TD>This will enable google Places API to suggest address in address fields. Places API and Maps Javascript API must"
				+ " be enabled with a valid API Key for this to function properly. "
				+ " <a href=\"https://cloud.google.com/maps-platform/places/\"" + 
				 ">Google Places API</a></TD></TR>");
		
	
	
		//API Client ID
		m_pwOut.println("<TR><TD ALIGN=RIGHT><B>Google API client ID</B>:</TD>");
		m_pwOut.println("<TD><INPUT TYPE=TEXT NAME=\"" 
				+ SMOptionInput.Paramsgoogleapiclientid + "\""
				+ " VALUE=\"" + optionInput.getsgoogleapiclientid() + "\""
				+ "SIZE=40"
				+ "; MAXLENGTH=" + Integer.toString(SMTablesmoptions.igoogleapikeylength)
				+ ">"
				+ "</TD>"
			);
		m_pwOut.println("<TD>This is required for Drive Picker API.");
		
		//API Project ID
		m_pwOut.println("<TR><TD ALIGN=RIGHT><B>Google API project ID</B>:</TD>");
		m_pwOut.println("<TD><INPUT TYPE=TEXT NAME=\"" 
				+ SMOptionInput.Paramsgoogleapiprojectid + "\""
				+ " VALUE=\"" + optionInput.getsgoogleapiprojectid() + "\""
				+ "SIZE=40"
				+ "; MAXLENGTH=" + Integer.toString(SMTablesmoptions.igoogleapikeylength)
				+ ">"
				+ "</TD>"
			);
		m_pwOut.println("<TD>This is required for certain features of the Drive Picker API.");
		
		//Use google Driver Picker API 
		m_pwOut.println("<TR><TD ALIGN=RIGHT><B>Use Drive Picker API?</B>:</TD>");
		if (optionInput.getiusegoogledrivepickerapi().compareToIgnoreCase("0") == 0){
			m_pwOut.println("<TD><INPUT TYPE=\"CHECKBOX\" NAME=\"" + SMOptionInput.Paramiusegoogledrivepickerapi 
					+ "\" ></TD>");
		}else{
			m_pwOut.println("<TD><INPUT TYPE=\"CHECKBOX\" NAME=\"" + SMOptionInput.Paramiusegoogledrivepickerapi 
					+ "\" CHECKED></TD>");
		}	
		m_pwOut.println("<TD>This will enable google Drive Picker API to upload/create files to google drive folder. "
				+ " <a href=\"https://developers.google.com/picker/\"" + 
				 ">Google Drive Picker API</a></TD></TR>");
					
		
		
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
