package smfa;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import SMDataDefinition.SMTablefaoptions;
import ServletUtilities.clsManageRequestParameters;
import smcontrolpanel.SMMasterEditEntry;
import smcontrolpanel.SMSystemFunctions;
import smcontrolpanel.SMUtilities;

public class FAEditOptionsEdit extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final String SUBMIT_BUTTON_NAME = "SUBMIT";
	private static final String SUBMIT_BUTTON_VALUE = "Save FA Option Changes";
	
	public void doPost(HttpServletRequest request,
				HttpServletResponse response)
				throws ServletException, IOException {
		
		FAOptions entry = new FAOptions(request);
		
		SMMasterEditEntry smedit = new SMMasterEditEntry(
				request,
				response,
				getServletContext(),
				FAOptions.ParamObjectName,
				SMUtilities.getFullClassName(this.toString()),
				"smfa.FAEditOptionsEdit",
				"smcontrolpanel.SMUserLogin",
				"Go back to user login",
				SMSystemFunctions.FAEditOptions
		);    
		
		if (!smedit.processSession(getServletContext(), SMSystemFunctions.FAEditOptions, request)){
			smedit.getPWOut().println("Error in process session: " + smedit.getErrorMessages());
			return;
		}
		//NOTE: This page posts back to itself
		//If edit, save the entries:
		if(clsManageRequestParameters.get_Request_Parameter(FAEditOptionsEdit.SUBMIT_BUTTON_NAME, request)
				.compareToIgnoreCase(SUBMIT_BUTTON_VALUE) == 0){
			
			if (!entry.saveEditableFields(getServletContext(), smedit.getsDBID(),smedit.getUserID(), smedit.getFullUserName())){
				//Set attribute before reloading the screen
				smedit.getCurrentSession().setAttribute(FAOptions.ParamObjectName, entry);
				//'Resubmit' the screen (without the submit button parameter)
				response.sendRedirect(
						"" + SMUtilities.getURLLinkBase(getServletContext()) + "smfa." + "FAEditOptionsEdit" + "?"
						+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + smedit.getsDBID()
						+ "&Warning=" + "Could not save Fixed Assets options: "
						+ entry.getErrorMessageString()
				);
		        return;
			}
		}
		
		//If this is a 'resubmit', meaning it's being called by this class, then
		//the session will have an FA Options object in it, and that's what we'll pick up.
		HttpSession currentSession = smedit.getCurrentSession();
		if (currentSession.getAttribute(FAOptions.ParamObjectName) != null){
			entry = (FAOptions) currentSession.getAttribute(FAOptions.ParamObjectName);
			currentSession.removeAttribute(FAOptions.ParamObjectName);
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
			smedit.getPWOut().println("Error [1556817219] getting session attribute - " + e1.getMessage());
			return;
		}
		
		//Build page header:
	    String title;
	    String subtitle = "";
	    title = "Edit Fixed Assets Options";

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
		smedit.getPWOut().println("<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smfa.FAMainMenu?" 
				+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + smedit.getsDBID() 
				+ "\">Return to Fixed Assets Main Menu</A><BR>");
		
	try{ 
		createEditPage(
				getEditHTML(smedit, entry),
				FAOptions.FORM_NAME,
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
	
	private String getEditHTML(SMMasterEditEntry smedit, FAOptions entry) throws Exception{
		String s = "";
		
		//Start the table:
		s += "<TABLE BORDER=1 CELLSPACING=2 style=\"font-size:80%\">\n";		
		
		//Feed GL:
		s += "  <TR>\n"
			+ "    <TD ALIGN=RIGHT><B>GL feed:</B></TD>\n";
		s += "    <TD><SELECT NAME = \"" + FAOptions.Paramifeedgl + "\">";

		s += "<OPTION";
		if (entry.getifeedgl().compareToIgnoreCase(
				Integer.toString(SMTablefaoptions.FEED_GL_EXTERNAL_GL_ONLY)) == 0){
			s +=  " selected=yes ";
		}
		s += " VALUE=\"" + Integer.toString(SMTablefaoptions.FEED_GL_EXTERNAL_GL_ONLY) + "\">";
		s += "Create GL batch export file for external GLs only";
		s += "</OPTION>";
		
		s += "<OPTION";
		if (entry.getifeedgl().compareToIgnoreCase(
				Integer.toString(SMTablefaoptions.FEED_GL_SMCP_GL_ONLY)) == 0){
			s +=  " selected=yes ";
		}
		s += " VALUE=\"" + Integer.toString(SMTablefaoptions.FEED_GL_SMCP_GL_ONLY) + "\">";
		s += "Create GL batch in SMCP GL only";
		s += "</OPTION>";
		
		s += "<OPTION";
		if (entry.getifeedgl().compareToIgnoreCase(
				Integer.toString(SMTablefaoptions.FEED_GL_BOTH_EXTERNAL_AND_SMCP_GL)) == 0){
			s +=  " selected=yes ";
		}
		s += " VALUE=\"" + Integer.toString(SMTablefaoptions.FEED_GL_BOTH_EXTERNAL_AND_SMCP_GL) + "\">";
		s += "Create external GL batch AND batch in SMCP GL (normally for testing only)";
		s += "</OPTION>";
		
		s += "</SELECT></TD>";
		s += "<TD>Determines if batch posting creates SMCP GL batches.</TD></TD>";
		s += "</TR>";
		
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

