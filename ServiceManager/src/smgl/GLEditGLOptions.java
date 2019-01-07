package smgl;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.ResultSet;
import java.util.ArrayList;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import SMDataDefinition.SMTableglaccounts;
import SMDataDefinition.SMTablegloptions;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsManageRequestParameters;
import ServletUtilities.clsServletUtilities;
import smcontrolpanel.SMMasterEditEntry;
import smcontrolpanel.SMSystemFunctions;
import smcontrolpanel.SMUtilities;

public class GLEditGLOptions extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final String SUBMIT_BUTTON_NAME = "SUBMIT";
	private static final String SUBMIT_BUTTON_VALUE = "Save GL Option Changes";
	
	public void doPost(HttpServletRequest request,
				HttpServletResponse response)
				throws ServletException, IOException {
		

		GLOptions entry = new GLOptions(request);
		
		SMMasterEditEntry smedit = new SMMasterEditEntry(
				request,
				response,
				getServletContext(),
				entry.getObjectName(),
				SMUtilities.getFullClassName(this.toString()),
				"smgl.GLEditGLOptions",
				"smcontrolpanel.SMUserLogin",
				"Go back to user login",
				SMSystemFunctions.GLEditGLOptions
		);    
		
		if (!smedit.processSession(getServletContext(), SMSystemFunctions.GLEditGLOptions, request)){
			smedit.getPWOut().println("Error in process session: " + smedit.getErrorMessages());
			return;
		}
		//NOTE: This page posts back to itself
		//If edit, save the entries:
		if(clsManageRequestParameters.get_Request_Parameter(GLEditGLOptions.SUBMIT_BUTTON_NAME, request)
				.compareToIgnoreCase(SUBMIT_BUTTON_VALUE) == 0){
			
			if (!entry.saveEditableFields(getServletContext(), smedit.getsDBID(),smedit.getUserID(), smedit.getFullUserName())){
				//Set attribute before reloading the screen
				smedit.getCurrentSession().setAttribute(GLOptions.ParamObjectName, entry);
				//'Resubmit' the screen (without the submit button parameter)
				response.sendRedirect(
						"" + SMUtilities.getURLLinkBase(getServletContext()) + "smgl." + "GLEditGLOptions" + "?"
						+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + smedit.getsDBID()
						+ "&Warning=" + "Could not save General Ledger options: "
						+ entry.getErrorMessageString()
				);
		        return;
			}
		}
		
		//If this is a 'resubmit', meaning it's being called by this class, then
		//the session will have an GLOptions object in it, and that's what we'll pick up.
		HttpSession currentSession = smedit.getCurrentSession();
		if (currentSession.getAttribute(GLOptions.ParamObjectName) != null){
			entry = (GLOptions) currentSession.getAttribute(GLOptions.ParamObjectName);
			currentSession.removeAttribute(GLOptions.ParamObjectName);
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
			smedit.getPWOut().println("Error [1522773235] getting session attribute - " + e1.getMessage());
			return;
		}
		
		//Build page header:
	    String title;
	    String subtitle = "";
	    title = "Edit General Ledger Options";

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
		smedit.getPWOut().println("<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smgl.GLMainMenu?" 
				+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + smedit.getsDBID() 
				+ "\">Return to General Ledger Main Menu</A><BR>");
		
	try{ 
		createEditPage(
				getEditHTML(smedit, entry),
				GLOptions.FORM_NAME,
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
	
	private String getEditHTML(SMMasterEditEntry smedit, GLOptions entry) throws Exception{
		String s = "";
		
		//Start the table:
		s += "<TABLE BORDER=1 CELLSPACING=2 style=\"font-size:80%\">\n";	
		
		//Closing account:
		s += " <TR><TD ALIGN=RIGHT><B>GL closing account</B>:</TD>"
			+ "<TD>";	
	
		ArrayList<String>arrGLAccts = new ArrayList<String>(0);
		ArrayList<String>arrGLAcctDescriptions = new ArrayList<String>(0);
		String SQL = "SELECT"
			+ " " + SMTableglaccounts.lActive
			+ ", " + SMTableglaccounts.sAcctID
			+ ", " + SMTableglaccounts.sDesc
			+ " FROM " + SMTableglaccounts.TableName
			+ " ORDER BY " + SMTableglaccounts.sAcctID
		;
		try {
			ResultSet rsGLAccts = clsDatabaseFunctions.openResultSet(
				SQL, 
				getServletContext(), 
				smedit.getsDBID(), 
				"MySQL", 
				clsServletUtilities.getFullClassName(this.toString())+ " - user: '" + smedit.getFullUserName() + "'.");
			while (rsGLAccts.next()){
				arrGLAccts.add(rsGLAccts.getString(SMTableglaccounts.sAcctID));
				String sInactive = "";
				if (rsGLAccts.getInt(SMTableglaccounts.lActive) == 0){
					sInactive = " (INACTIVE)";
				}
				arrGLAcctDescriptions.add(rsGLAccts.getString(SMTableglaccounts.sAcctID) + " - " + rsGLAccts.getString(SMTableglaccounts.sDesc) + sInactive);
			}
			rsGLAccts.close();
		} catch (Exception e) {
			throw new Exception("Error [1529350092] - could not get GL account list with SQL '" + SQL + "' - " + e.getMessage());
		}

		s += ServletUtilities.clsCreateHTMLFormFields.Create_Edit_Form_List_Field(
			GLOptions.Paramsclosingaccount, 
			arrGLAccts, 
			entry.getsClosingAccount(), 
			arrGLAcctDescriptions);
		
		s += "</TD>";
		
		s += "<TD>Select the account to be used in closing at year end.</TD></TD>"
		 + "</TR>";
			
		//Batch posting in process:
		s += "<TR><TD ALIGN=RIGHT><B>Batch posting in process?</B>:</TD>";
		if (entry.getBatchPostingInProcess().compareToIgnoreCase("1") == 0){
			s += "<TD><B>" + "YES" + "</B></TD>";
		}else{
			s += "<TD><B>" + "NO" + "</B></TD>";
		}
		s += "<TD>Indicates whether a batch posting is not complete.</TD>";
		s += "<INPUT TYPE=HIDDEN NAME=\"" + GLOptions.Parambatchpostinginprocess 
			+ "\" VALUE=\"" + entry.getBatchPostingInProcess() + "\">";
		s += "</TR>";

		//Posting user:
		s += "<TR><TD ALIGN=RIGHT><B>Currently posting user</B>:</TD>"
			+ "<TD><B>" + entry.getPostingUserFullName() + "</B></TD>"
			+ "<TD>Blank if posting processes are complete.</TD>";
		
		s += "<INPUT TYPE=HIDDEN NAME=\"" + GLOptions.Parampostinguserfullname
			+ "\" VALUE=\"" + entry.getPostingUserFullName() + "\">"
			+ "</TR>";
		
		//Posting process
		s += "<TR><TD ALIGN=RIGHT><B>Currently posting process</B>:</TD>"
			+ "<TD><B>" + entry.getPostingProcess() + "</B></TD>"
			+ "<TD>Blank if posting processes are complete.</TD>";
		
		s += "<INPUT TYPE=HIDDEN NAME=\"" + GLOptions.Parampostingprocess
				+ "\" VALUE=\"" + entry.getPostingProcess() + "\">"
				+ "</TR>";
		
		//Posting start date
		s += "<TR><TD ALIGN=RIGHT><B>Posting start time</B>:</TD>"
			+ "<TD><B>" + entry.getPostingStartDate() + "</B></TD>"
			+ "<TD>Starting date and time of any current/incomplete posting process.</TD>";
		
		s += "<INPUT TYPE=HIDDEN NAME=\"" + GLOptions.Parampostingstartdate
				+ "\" VALUE=\"" + entry.getPostingStartDate() + "\">"
				+ "</TR>";
		
		//Uses SMTP GL?  
		s += "<TR><TD ALIGN=RIGHT><B>Using SMCP GL?</B>:</TD>";
		if (entry.getUsesSMCPGL().compareToIgnoreCase("0") == 0){
			s += "<TD><INPUT TYPE=\"CHECKBOX\" NAME=\"" + GLOptions.Paramusessmcpgl 
					+ "\" ></TD>";
		}else{
			s += "<TD><INPUT TYPE=\"CHECKBOX\" NAME=\"" + GLOptions.Paramusessmcpgl 
					+ "\" CHECKED></TD>";
		}		
		s += "<TD>Check if currently using SMCP GL.</TD></TR>";
		
		//ACCPAC to GL Conversion
		s += "<TR><TD ALIGN=LEFT COLSPAN=3><B><U>ACCPAC TO GL CONVERSION:</U></B></TD></TR>";
		
		//ACCPAC Version:
		s += " <TR><TD ALIGN=RIGHT><B>ACCPAC Version</B>:</TD>"
			+ "<TD><SELECT NAME = \"" + GLOptions.Paramaccpacversion + "\">";	
	
		for (int i = 0; i < SMTablegloptions.NUMBER_OF_ACCPAC_VERSION_FORMATS; i++){
			s += "<OPTION";
			if (entry.getACCPACversion().compareToIgnoreCase(
					Long.toString(i)) == 0){
				s+= " selected=yes ";
			}
			s += " VALUE=\"" + Integer.toString(i) + "\">" 
				+ SMTablegloptions.getACCPACVersionLabel(i) + "</OPTION>";
		}
		s += "</SELECT></TD>"
		 + "<TD>Select the current accounting package.</TD></TD>"
		 + "</TR>";
		
		//ACCPAC Database Type:
		s += " <TR><TD ALIGN=RIGHT><B>ACCPAC Database Type</B>:</TD>"
			+ "<TD><SELECT NAME = \"" + GLOptions.Paramaccpacdatabasetype + "\">";	
				
		for (int i = 0; i < SMTablegloptions.NUMBER_OF_DATABASE_TYPES; i++){
			s += "<OPTION";
			if (entry.getACCPACDatabaseType().compareToIgnoreCase(
					Long.toString(i)) == 0){
				s+= " selected=yes ";
			}
			s += " VALUE=\"" + Integer.toString(i) + "\">" 
				+ SMTablegloptions.getDatabaseTypeLabel(i) + "</OPTION>";
		}
		s += "</SELECT></TD>"
		 + "<TD>Select which type of database ACCPAC is using.</TD></TD>"
		 + "</TR>";
		
	    //ACCPAC Database URL:
		s += "<TR><TD ALIGN=RIGHT><B>Database URL</B>:</TD>";
		s += "<TD><INPUT TYPE=TEXT NAME=\"" 
				+ GLOptions.Paramaccpacdatabaseurl + "\""
				+ " VALUE=\"" + entry.getACCPACDatabaseURL() + "\""
				+ "SIZE=40"
				+ "; MAXLENGTH=" + Integer.toString(SMTablegloptions.saccpacdatabaseurllength)
				+ ">"
				+ "</TD>";
		s += "<TD>This is the URL used to access the ACCPAC database.</TD></TR>";
		
	    //ACCPAC Database Name:
		s += "<TR><TD ALIGN=RIGHT><B>Database Name</B>:</TD>";
		s += "<TD><INPUT TYPE=TEXT NAME=\"" 
				+ GLOptions.Paramaccpacdatabasename + "\""
				+ " VALUE=\"" + entry.getACCPACDatabaseName() + "\""
				+ "SIZE=40"
				+ "; MAXLENGTH=" + Integer.toString(SMTablegloptions.saccpacdatabasenamelength)
				+ ">"
				+ "</TD>";
		s += "<TD>Name of the ACCPAC database.</TD></TR>";
		
	    //ACCPAC Database User:
		s += "<TR><TD ALIGN=RIGHT><B>Database User</B>:</TD>";
		s += "<TD><INPUT TYPE=TEXT NAME=\"" 
				+ GLOptions.Paramaccpacdatabaseuser + "\""
				+ " VALUE=\"" + entry.getACCPACDatabaseUser() + "\""
				+ "SIZE=40"
				+ "; MAXLENGTH=" + Integer.toString(SMTablegloptions.saccpacdatabaseuserlength)
				+ ">"
				+ "</TD>";
		s += "<TD>Username used to access the ACCPAC database.</TD></TR>";
		
	    //ACCPAC Database Password:
		s += "<TR><TD ALIGN=RIGHT><B>Database User Password</B>:</TD>";
		s += "<TD><INPUT TYPE=TEXT NAME=\"" 
				+ GLOptions.Paramaccpacdatabaseuserpw + "\""
				+ " VALUE=\"" + entry.getACCPACDatabaseUserPW() + "\""
				+ "SIZE=40"
				+ "; MAXLENGTH=" + Integer.toString(SMTablegloptions.saccpacdatabasuserpwlength)
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

