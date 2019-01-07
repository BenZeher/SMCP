package smap;

import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import smcontrolpanel.SMMasterEditEntry;
import smcontrolpanel.SMSystemFunctions;
import smcontrolpanel.SMUtilities;
import SMDataDefinition.SMExportTypes;
import SMDataDefinition.SMTableapoptions;
import ServletUtilities.clsManageRequestParameters;

public class APEditAPOptions extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final String SUBMIT_BUTTON_NAME = "SUBMIT";
	private static final String SUBMIT_BUTTON_VALUE = "Save AP Option Changes";
	
	public void doPost(HttpServletRequest request,
				HttpServletResponse response)
				throws ServletException, IOException {
		

		APOptions entry = new APOptions(request);
		
		SMMasterEditEntry smedit = new SMMasterEditEntry(
				request,
				response,
				getServletContext(),
				entry.getObjectName(),
				SMUtilities.getFullClassName(this.toString()),
				"smap.APEditAPOptions",
				"smcontrolpanel.SMUserLogin",
				"Go back to user login",
				SMSystemFunctions.APEditAPOptions
		);    
		
		if (!smedit.processSession(getServletContext(), SMSystemFunctions.APEditAPOptions, request)){
			smedit.getPWOut().println("Error in process session: " + smedit.getErrorMessages());
			return;
		}
		//NOTE: This page posts back to itself
		//If edit, save the entries:
		if(clsManageRequestParameters.get_Request_Parameter(APEditAPOptions.SUBMIT_BUTTON_NAME, request)
				.compareToIgnoreCase(SUBMIT_BUTTON_VALUE) == 0){
			
			if (!entry.saveEditableFields(getServletContext(), smedit.getsDBID(),smedit.getUserID(), smedit.getFullUserName())){
				//Set attribute before reloading the screen
				smedit.getCurrentSession().setAttribute(APOptions.ParamObjectName, entry);
				//'Resubmit' the screen (without the submit button parameter)
				response.sendRedirect(
						"" + SMUtilities.getURLLinkBase(getServletContext()) + "smap." + "APEditAPOptions" + "?"
						+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + smedit.getsDBID()
						+ "&Warning=" + "Could not save Accounts Payable options: "
						+ entry.getErrorMessageString()
				);
		        return;
			}
		}
		
		//If this is a 'resubmit', meaning it's being called by this class, then
		//the session will have an APOptions object in it, and that's what we'll pick up.
		HttpSession currentSession = smedit.getCurrentSession();
		if (currentSession.getAttribute(APOptions.ParamObjectName) != null){
			entry = (APOptions) currentSession.getAttribute(APOptions.ParamObjectName);
			currentSession.removeAttribute(APOptions.ParamObjectName);
		//But if it's NOT a 'resubmit', meaning this class was called for the first time to 
		//edit OR after a previous successful edit, we'll load the entry:
		}else{
			try {
				entry.load(smedit.getsDBID(), getServletContext(), smedit.getUserID());
			} catch (Exception e) {
				smedit.redirectAction(e.getMessage(), "", "");
				return;
			}
			    	
		}
		//Get company name from session
		String sCompanyName = "";
		try {
			sCompanyName = (String) currentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_COMPANYNAME);
		} catch (Exception e1) {
			smedit.getPWOut().println("Error [1445280222] getting session attribute - " + e1.getMessage());
			return;
		}
		
		//Build page header:
	    String title;
	    String subtitle = "";
	    title = "Edit Accounts Payable Options";

	    smedit.getPWOut().println(SMUtilities.SMCPTitleSubBGColor(title, subtitle, SMUtilities.getInitBackGroundColor(getServletContext(), smedit.getsDBID()), sCompanyName));
		
	    //If there is a warning from trying to input previously, print it here:
		String sWarning = clsManageRequestParameters.get_Request_Parameter("Warning", smedit.getRequest());
		if (sWarning.compareToIgnoreCase("") != 0){
			smedit.getPWOut().println("<B><FONT COLOR=\"RED\">WARNING: " + sWarning + "</FONT></B><BR>");
		}
		//If there is a status from trying to input previously, print it here:
		String sStatus = clsManageRequestParameters.get_Request_Parameter("Status", smedit.getRequest());
		if (sStatus.compareToIgnoreCase("") != 0){
			smedit.getPWOut().println("<B>" + sStatus + "</B><BR>");
		}
		
	    //Print a link to main menu:
		smedit.getPWOut().println("<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMUserLogin?" 
				+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + smedit.getsDBID()
				+ "\">Return to Main Menu</A><BR>");
		
	    //Print a link to main menu:
		smedit.getPWOut().println("<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smap.APMainMenu?" 
				+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + smedit.getsDBID() 
				+ "\">Return to Accounts Payable Main Menu</A><BR>");
		
	try{ 
		createEditPage(
				getEditHTML(smedit, entry),
				APOptions.FORM_NAME,
				smedit.getPWOut(),
				smedit);
	} catch (Exception e) {
		String sError = "Could not create edit page - " + e.getMessage();
		smedit.getPWOut().println(sError);
		return;
	}

}
	
	public void createEditPage(
			String sEditHTML,
			String sFormClassName,
			PrintWriter pwOut,
			SMMasterEditEntry sm
	) throws Exception{

		String sFormString = "<FORM ID='" + sFormClassName + "' NAME='" + sFormClassName + "' ACTION='" 
			+ SMUtilities.getURLLinkBase(getServletContext()) + sm.getCalledClass() + "'";
		
		sFormString	+= " METHOD='POST'";
		if (sFormClassName.compareToIgnoreCase("") != 0){
			sFormString += " class=" + sFormClassName + ">";
		}
		pwOut.println(sFormString);
		pwOut.println("<INPUT TYPE=HIDDEN NAME='" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "' VALUE='" + sm.getsDBID() + "'>");
		pwOut.println("<INPUT TYPE=HIDDEN NAME=\"" + "CallingClass" + "\" VALUE=\"" 
				+ SMUtilities.getFullClassName(this.toString()) + "\">");
		pwOut.println(sEditHTML);
		pwOut.println("</FORM>");

	}
	
	private String getEditHTML(SMMasterEditEntry smedit, APOptions entry) throws Exception{
		String s = "";
		
		//Start the table:
		s += "<TABLE BORDER=1 CELLSPACING=2 style=\"font-size:80%\">\n";		
	
		//Export to:
		s += "  <TR>\n"
			+ "    <TD ALIGN=RIGHT><B>Export to:</B></TD>\n";
		s += "    <TD><SELECT NAME = \"" + APOptions.Paramiexportoption + "\">";

		for (int i = 0; i < SMExportTypes.NUMBER_OF_EXPORT_FORMATS; i++){
			s += "<OPTION";
			if (entry.getiexportoption().compareToIgnoreCase(
					Long.toString(i)) == 0){
				s +=  " selected=yes ";
			}
			s += " VALUE=\"" + Integer.toString(i) + "\">";
			s += SMExportTypes.getExportFormatLabel(i);
			s += "</OPTION>";
		}
		s += "</SELECT></TD>";
		s += "<TD>Select the accounting package to which you want to export AP batches.</TD></TD>";
		s += "</TR>";
		
		//Batch posting in process:
		s += "<TR><TD ALIGN=RIGHT><B>Batch posting in process?</B>:</TD>";
		if (entry.getBatchPostingInProcess().compareToIgnoreCase("1") == 0){
			s += "<TD><B>" + "YES" + "</B></TD>";
		}else{
			s += "<TD><B>" + "NO" + "</B></TD>";
		}
		s += "<TD>Indicates whether a batch posting is not complete.</TD>";
		s += "<INPUT TYPE=HIDDEN NAME=\"" + APOptions.Parambatchpostinginprocess 
			+ "\" VALUE=\"" + entry.getBatchPostingInProcess() + "\">";
		s += "</TR>";

		//Posting user:
		s += "<TR><TD ALIGN=RIGHT><B>Currently posting user</B>:</TD>"
			+ "<TD><B>" + SMUtilities.getFullNamebyUserID(entry.getPostingUserID(), getServletContext(), smedit.getsDBID(), "") + "</B></TD>"
			+ "<TD>Blank if posting processes are complete.</TD>";
		
		s += "<INPUT TYPE=HIDDEN NAME=\"" + APOptions.Parampostinguserid 
			+ "\" VALUE=\"" + entry.getPostingUserID() + "\">"
			+ "</TR>";
		
		//Posting process
		s += "<TR><TD ALIGN=RIGHT><B>Currently posting process</B>:</TD>"
			+ "<TD><B>" + entry.getPostingProcess() + "</B></TD>"
			+ "<TD>Blank if posting processes are complete.</TD>";
		
		s += "<INPUT TYPE=HIDDEN NAME=\"" + APOptions.Parampostingprocess
				+ "\" VALUE=\"" + entry.getPostingProcess() + "\">"
				+ "</TR>";
		
		//Posting start date
		s += "<TR><TD ALIGN=RIGHT><B>Posting start time</B>:</TD>"
			+ "<TD><B>" + entry.getPostingStartDate() + "</B></TD>"
			+ "<TD>Starting date and time of any current/incomplete posting process.</TD>";
		
		s += "<INPUT TYPE=HIDDEN NAME=\"" + APOptions.Parampostingstartdate
				+ "\" VALUE=\"" + entry.getPostingStartDate() + "\">"
				+ "</TR>";
		
		//Google Drive integration:
		s += "<TR><TD ALIGN=LEFT COLSPAN=3><B><U>GOOGLE DRIVE FOLDER CREATION:</U></B></TD></TR>";
		
	    //GDrive customer parent folder ID:
		s += "<TR><TD ALIGN=RIGHT><B>Google Drive VENDOR parent folder ID</B>:</TD>";
		s += "<TD><INPUT TYPE=TEXT NAME=\"" 
				+ APOptions.Paramgdrivevendorsparentfolderid + "\""
				+ " VALUE=\"" + entry.getgdrivevendorparentfolderid() + "\""
				+ "SIZE=40"
				+ "; MAXLENGTH=" + Integer.toString(SMTableapoptions.gdrivevendorparentfolderidlength)
				+ ">"
				+ "</TD>";
		s += "<TD>This is the Google Drive 'ID' of the PARENT folder in which new vendor folders will be created.</TD></TR>";

		//GDrive customer folder prefix:
		s += "<TR><TD ALIGN=RIGHT><B>VENDOR folder prefix</B>:</TD>";
		s += "<TD><INPUT TYPE=TEXT NAME=\"" 
				+ APOptions.Paramgdrivevendorsfolderprefix  + "\""
				+ " VALUE=\"" + entry.getgdrivevendorfolderprefix() + "\""
				+ "SIZE=40"
				+ "; MAXLENGTH=" + Integer.toString(SMTableapoptions.gdrivevendorfolderprefixlength)
				+ ">"
			    + "</TD>";
		s += "<TD>If you want a short prefix in front of the created vendor folder name, enter it here.</TD></TR>";
				
		//GDrive customer folder suffix:
		s += "<TR><TD ALIGN=RIGHT><B>VENDOR folder suffix</B>:</TD>";
		s += "<TD><INPUT TYPE=TEXT NAME=\"" 
				+ APOptions.Paramgdrivevendorsfoldersuffix + "\""
				+ " VALUE=\"" + entry.getgdrivevendorfoldersuffix() + "\""
				+ "SIZE=40"
				+ "; MAXLENGTH=" + Integer.toString(SMTableapoptions.gdrivevendorfoldersuffixlength)
				+ ">"
				+ "</TD>";
		s += "<TD>If you want a short suffix in front of the created vendor folder name, enter it here.</TD></TR>";
		
		//Uses SMTP AP?  
		s += "<TR><TD ALIGN=RIGHT><B>Using SMCP AP?</B>:</TD>";
		if (entry.getUsesSMCPAP().compareToIgnoreCase("0") == 0){
			s += "<TD><INPUT TYPE=\"CHECKBOX\" NAME=\"" + APOptions.Paramusessmcpap 
					+ "\" ></TD>";
		}else{
			s += "<TD><INPUT TYPE=\"CHECKBOX\" NAME=\"" + APOptions.Paramusessmcpap 
					+ "\" CHECKED></TD>";
		}		
		s += "<TD>Check if currently using SMCP AP to export.</TD></TR>";
		
		//ACCPAC to AP Conversion
		s += "<TR><TD ALIGN=LEFT COLSPAN=3><B><U>ACCPAC TO AP CONVERSION:</U></B></TD></TR>";
		
		//Export to ACCPAC Version:
		s += " <TR><TD ALIGN=RIGHT><B>ACCPAC Version</B>:</TD>"
			+ "<TD><SELECT NAME = \"" + APOptions.Paramaccpacversion + "\">";	
		
		for (int i = 0; i < SMTableapoptions.NUMBER_OF_ACCPAC_VERSION_FORMATS; i++){
			s += "<OPTION";
			if (entry.getACCPACversion().compareToIgnoreCase(
					Long.toString(i)) == 0){
				s+= " selected=yes ";
			}
			s += " VALUE=\"" + Integer.toString(i) + "\">" 
				+ SMTableapoptions.getExportFormatLabel(i) + "</OPTION>";
		}
		s += "</SELECT></TD>"
		 + "<TD>Select the current accounting package.</TD></TD>"
		 + "</TR>";
        
		//ACCPAC Database Type:
		s += " <TR><TD ALIGN=RIGHT><B>ACCPAC Database Type</B>:</TD>"
			+ "<TD><SELECT NAME = \"" + APOptions.Paramaccpacdatabasetype + "\">";	
				
		for (int i = 0; i < SMTableapoptions.NUMBER_OF_DATABASE_TYPES; i++){
			s += "<OPTION";
			if (entry.getACCPACDatabaseType().compareToIgnoreCase(
					Long.toString(i)) == 0){
				s+= " selected=yes ";
			}
			s += " VALUE=\"" + Integer.toString(i) + "\">" 
				+ SMTableapoptions.getDatabaseTypeLabel(i) + "</OPTION>";
		}
		s += "</SELECT></TD>"
		 + "<TD>Select which type of database ACCPAC is using.</TD></TD>"
		 + "</TR>";
		
	    //ACCPAC Database URL:
		s += "<TR><TD ALIGN=RIGHT><B>Database URL</B>:</TD>";
		s += "<TD><INPUT TYPE=TEXT NAME=\"" 
				+ APOptions.Paramaccpacdatabaseurl + "\""
				+ " VALUE=\"" + entry.getACCPACDatabaseURL() + "\""
				+ "SIZE=40"
				+ "; MAXLENGTH=" + Integer.toString(SMTableapoptions.saccpacdatabaseurllength)
				+ ">"
				+ "</TD>";
		s += "<TD>This is the URL used to access the ACCPAC database.</TD></TR>";
		
	    //ACCPAC Database Name:
		s += "<TR><TD ALIGN=RIGHT><B>Database Name</B>:</TD>";
		s += "<TD><INPUT TYPE=TEXT NAME=\"" 
				+ APOptions.Paramaccpacdatabasename + "\""
				+ " VALUE=\"" + entry.getACCPACDatabaseName() + "\""
				+ "SIZE=40"
				+ "; MAXLENGTH=" + Integer.toString(SMTableapoptions.saccpacdatabasenamelength)
				+ ">"
				+ "</TD>";
		s += "<TD>Name of the ACCPAC database.</TD></TR>";
		
	    //ACCPAC Database User:
		s += "<TR><TD ALIGN=RIGHT><B>Database User</B>:</TD>";
		s += "<TD><INPUT TYPE=TEXT NAME=\"" 
				+ APOptions.Paramaccpacdatabaseuser + "\""
				+ " VALUE=\"" + entry.getACCPACDatabaseUser() + "\""
				+ "SIZE=40"
				+ "; MAXLENGTH=" + Integer.toString(SMTableapoptions.saccpacdatabaseuserlength)
				+ ">"
				+ "</TD>";
		s += "<TD>Username used to access the ACCPAC database.</TD></TR>";
		
	    //ACCPAC Database Password:
		s += "<TR><TD ALIGN=RIGHT><B>Database User Password</B>:</TD>";
		s += "<TD><INPUT TYPE=TEXT NAME=\"" 
				+ APOptions.Paramaccpacdatabaseuserpw + "\""
				+ " VALUE=\"" + entry.getACCPACDatabaseUserPW() + "\""
				+ "SIZE=40"
				+ "; MAXLENGTH=" + Integer.toString(SMTableapoptions.saccpacdatabasuserpwlength)
				+ ">"
				+ "</TD>";
		s += "<TD>User password used to access the ACCPAC database.</TD></TR>";
		
		s += "</TABLE>";
				
	    s += "<BR><INPUT TYPE=SUBMIT NAME='" + SUBMIT_BUTTON_NAME + "' VALUE='" + SUBMIT_BUTTON_VALUE + "' STYLE='height: 0.24in'>";
				
	    //End the edit form:
	    s += "</FORM>";  

		return s;

	}
	
	public void doGet(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
		
		doPost(request, response);
	}
}

