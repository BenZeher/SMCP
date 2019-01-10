package smgl;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import smcontrolpanel.SMMasterEditEntry;
import smcontrolpanel.SMSystemFunctions;
import smcontrolpanel.SMUtilities;
import SMClasses.SMTax;
import SMDataDefinition.SMTablecostcenters;
import ServletUtilities.clsCreateHTMLTableFormFields;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsManageRequestParameters;

public class GLEditCostCentersEdit extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final String SUBMIT_UPDATE_BUTTON_NAME = "SubmitEditUpdate";
	private static final String SUBMIT_UPDATE_BUTTON_VALUE = "Update " + GLCostCenter.ParamObjectName;
	private static final String REQUIRED_FIELD_FLAG = "<FONT COLOR=RED><B>*</B></FONT>";
	
	public void doPost(HttpServletRequest request,
				HttpServletResponse response)
				throws ServletException, IOException {

		GLCostCenter entry = new GLCostCenter(request);
		SMMasterEditEntry smedit = new SMMasterEditEntry(
				request,
				response,
				getServletContext(),
				SMTax.ParamObjectName,
				SMUtilities.getFullClassName(this.toString()),
				SMUtilities.getFullClassName(this.toString()),
				"smcontrolpanel.SMUserLogin",
				"Go back to user login",
				SMSystemFunctions.GLEditCostCenters
		);    
		
		if (!smedit.processSession(getServletContext(), SMSystemFunctions.GLEditCostCenters, request)){
			smedit.getPWOut().println("Error in process session: " + smedit.getErrorMessages());
			return;
		}
		
		//Get any object out of the session immediately, so we don't leave it in:
		HttpSession currentSession = smedit.getCurrentSession();
		GLCostCenter entryobjectfromsession = null;
		if (currentSession.getAttribute(GLCostCenter.ParamObjectName) != null){
			entryobjectfromsession = (GLCostCenter) currentSession.getAttribute(GLCostCenter.ParamObjectName);
			currentSession.removeAttribute(GLCostCenter.ParamObjectName);
		}
		
	    //First process if it's a 'delete' from the Selection Screen:
	    if(request.getParameter(GLEditCostCenterSelection.SUBMIT_DELETE_BUTTON_NAME) != null){
		    if (request.getParameter(GLEditCostCenterSelection.CONFIRM_DELETE_CHECKBOX_NAME) == null){
				response.sendRedirect(
					"" + SMUtilities.getURLLinkBase(getServletContext()) + "smgl.GLEditCostCenterSelection"
					+ "?" + SMTablecostcenters.lid + "=" + entry.get_slid()
					+ "&Warning=You must check the 'confirming' check box to delete."
				);
				return;
		    }
		    if (entry.get_slid().compareToIgnoreCase("") == 0){
				response.sendRedirect(
					"" + SMUtilities.getURLLinkBase(getServletContext()) + "smgl.GLEditCostCenterSelection"
					+ "?" + SMTablecostcenters.lid + "=" + entry.get_slid()
					+ "&Warning=You must select a " + GLCostCenter.ParamObjectName + " to delete."
				);
				return;
		    }
		    
		    else{
		    	//Need a connection for the 'delete':
		    	Connection conn = clsDatabaseFunctions.getConnection(
		    		getServletContext(), 
		    		smedit.getsDBID(),
		    		"MySQL",
		    		this.toString() + ".doPost - User: " + smedit.getUserID()
		    		+ " - "
		    		+ smedit.getFullUserName()
		    			);
		    	if(conn == null){
    				response.sendRedirect(
        					"" + SMUtilities.getURLLinkBase(getServletContext()) + "smgl.GLEditCostCenterSelection"
        					+ "?" + SMTablecostcenters.lid + "=" + entry.get_slid()
        					+ "&Warning=Error deleting " + GLCostCenter.ParamObjectName + " - cannot get connection."
        				);
    				return;
		    	}
		    	try {
					entry.delete(entry.get_slid(), conn);
				} catch (Exception e) {
					clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1547080739]");
    				response.sendRedirect(
        					"" + SMUtilities.getURLLinkBase(getServletContext()) + "smgl.GLEditCostCenterSelection"
        					+ "?" + SMTablecostcenters.lid + "=" + entry.get_slid()
        					+ "&Warning=Error deleting " + GLCostCenter.ParamObjectName + " - " + e.getMessage()
        				);
    				return;
		    	}
		    	clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1547080740]");
				response.sendRedirect(
					"" + SMUtilities.getURLLinkBase(getServletContext()) + "smgl.GLEditCostCenterSelection"
							+ "?" + SMTablecostcenters.lid + "=" + entry.get_slid()
					+ "&Status=Successfully deleted " + GLCostCenter.ParamObjectName + " with ID: " + entry.get_slid()
				);
				return;
			}
	    }
		
	    //If coming from Add button of select screen; set as new record and clear ID
		if(request.getParameter(GLEditCostCenterSelection.SUBMIT_ADD_BUTTON_NAME) != null){
			entry.set_snewrecord(GLCostCenter.ADDING_NEW_RECORD_PARAM_VALUE_TRUE);
			entry.set_slid("-1");
		}
		
		//If we are coming here for the first time to edit (i.e., it's not a re-submit), then we need to 
		//load the input class from the database if possible:
		if(request.getParameter(GLEditCostCenterSelection.SUBMIT_EDIT_BUTTON_NAME) != null){
	    	try{
			entry.load(smedit.getsDBID(), getServletContext(), smedit.getUserName());
	    	}catch (Exception e){
				response.sendRedirect(
					"" + SMUtilities.getURLLinkBase(getServletContext()) + "smgl.GLEditCostCenterSelection"
					+ "?" + SMTablecostcenters.lid + "=" + entry.get_slid()
					+ "&Warning=" + e.getMessage()
				);
					return;
	    	}
		}
		
		//NOTE: This page posts back to itself
		//If update, save the entries:
		String sSaveStatus = "";
		if(clsManageRequestParameters.get_Request_Parameter(SUBMIT_UPDATE_BUTTON_NAME, request)
			.compareToIgnoreCase(SUBMIT_UPDATE_BUTTON_VALUE) == 0){
			try {
				entry.save(getServletContext(), smedit.getsDBID(), smedit.getUserName());
			} catch (Exception e) {
				//Set attribute before reloading the screen
				smedit.getCurrentSession().setAttribute(GLCostCenter.ParamObjectName, entry);
				//'Resubmit' the screen (without the submit button parameter)
				response.sendRedirect(
						"" + SMUtilities.getURLLinkBase(getServletContext()) + SMUtilities.getFullClassName(this.toString()) + "?"
						+ SMTablecostcenters.lid + "=" + entry.get_slid()
						+ "&" + GLCostCenter.ParamsNewRecord + "=" + entry.get_snewrecord()
						+ "&Warning=" + "Could not save tax - " + e.getMessage()					
				);
		        return;
			}
			sSaveStatus = GLCostCenter.ParamObjectName + " '" + entry.get_scostcentername() + "' saved successfully.";
		}
		
		//If this is a 'resubmit', meaning it's being called by this class, then
		//the session will have had an appropriate object in it, and that's what we'll pick up.
		if (entryobjectfromsession != null){
			entry = entryobjectfromsession;
		//But if it's NOT a 'resubmit', meaning this class was called for the first time to 
		//edit OR after a previous successful edit, we'll load the entry fresh from the database:
		}else{
			//If it's not an 'ADD', then try to load the entry from the database:
			if (entry.get_snewrecord().compareToIgnoreCase(GLCostCenter.ADDING_NEW_RECORD_PARAM_VALUE_FALSE) == 0){
				try {
					entry.load( smedit.getsDBID(), getServletContext(), smedit.getUserName());
				} catch (Exception e) {
					smedit.redirectAction(e.getMessage(), "", "");
					return;
				}
			}
		}
		//Get company name from session
		String sCompanyName = "";
		try {
			sCompanyName = (String) currentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_COMPANYNAME);
		} catch (Exception e1) {
			smedit.getPWOut().println("Error [1455027299] getting session attribute - " + e1.getMessage());
			return;
		}
		
		//Build page header:
	    String title;
	    String subtitle = "";
	    title = "Edit " + GLCostCenter.ParamObjectName;

	    smedit.getPWOut().println(SMUtilities.SMCPTitleSubBGColor(title, subtitle, SMUtilities.getInitBackGroundColor(getServletContext(), smedit.getsDBID()), sCompanyName));
	    smedit.getPWOut().println(SMUtilities.getDatePickerIncludeString(getServletContext()));
	    
	    //If there is a warning from trying to input previously, print it here:
		String sWarning = clsManageRequestParameters.get_Request_Parameter("Warning", smedit.getRequest());
		if (sWarning.compareToIgnoreCase("") != 0){
			smedit.getPWOut().println("<B><FONT COLOR=\"RED\">WARNING: " + sWarning + "</FONT></B><BR>");
		}
		//If there is a status from trying to input previously, print it here:
		String sStatus = clsManageRequestParameters.get_Request_Parameter("Status", smedit.getRequest());
		if (sStatus.compareToIgnoreCase("") != 0 || sSaveStatus.compareToIgnoreCase("") != 0){
			smedit.getPWOut().println("<B>" + sStatus + sSaveStatus + "</B><BR>");
		}
		
	    //Print a link to main menu:
		smedit.getPWOut().println("<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMUserLogin?" 
				+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + smedit.getsDBID()
				+ "\">Return to Main Menu</A><BR>");
		
	try{ 
		createEditPage(
				getEditHTML(smedit, entry),
				GLCostCenter.EDIT_FORM_NAME,
				smedit.getPWOut(),
				smedit,
			entry);
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
			SMMasterEditEntry sm,
			GLCostCenter entry
	) throws Exception	{

		String sFormString = "<FORM ID='" + sFormClassName + "' NAME='" + sFormClassName + "' ACTION='" 
			+ SMUtilities.getURLLinkBase(getServletContext()) + sm.getCalledClass() + "'";
		
		sFormString	+= " METHOD='POST'";
		if (sFormClassName.compareToIgnoreCase("") != 0){
			sFormString += " class=" + sFormClassName + ">";
		}
		pwOut.println(sFormString);
		pwOut.println("<INPUT TYPE=HIDDEN NAME='" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "' VALUE='" + sm.getsDBID() + "'>");
		pwOut.println("<INPUT TYPE=HIDDEN NAME=\"" + "CallingClass" + "\" VALUE=\"" + SMUtilities.getFullClassName(this.toString()) + "\">");
		pwOut.println(sEditHTML);
		pwOut.println("</FORM>");

	}
	
	private String getEditHTML(SMMasterEditEntry smedit, GLCostCenter entry) throws Exception	{
		String s = "";
		//Start the table:
		s += "<TABLE BORDER=12 CELLSPACING=2>";		
	
		//Cost Center ID:
	    s +="<TR>"
	    + "<TD ALIGN=RIGHT><B>" + "ID:"  + " </B></TD>"
	    + "<TD ALIGN=LEFT>";
	      if(entry.get_snewrecord().compareToIgnoreCase(GLCostCenter.ADDING_NEW_RECORD_PARAM_VALUE_TRUE) == 0){
	         s+= "(NEW)";
	      }else{
	         s+= entry.get_slid();
	      }
	    s+= "</TD>"
	    + "<TD ALIGN=LEFT>" 
	    + " " 
	    + "</TD>"
	    + "</TR>"
	    + "<INPUT TYPE=HIDDEN NAME=\"" + SMTablecostcenters.lid + "\" VALUE='" + entry.get_slid() + "'>"
	    + "<INPUT TYPE=HIDDEN NAME=\"" + GLCostCenter.ParamsNewRecord + "\" VALUE='" + entry.get_snewrecord() + "'>"
	    ;  	

		//Active?
	    s += clsCreateHTMLTableFormFields.Create_Edit_Form_Checkbox_Row(
	    	SMTablecostcenters.iactive, 
			Integer.parseInt(entry.get_sactive()), 
			"Active?", 
			"<I>Uncheck to de-activate this cost center.</I>"
			)
		;
        
	    //Cost Center Name
        s += "<TR>"
    	  + "<TD ALIGN=RIGHT><B>" + "Name" + REQUIRED_FIELD_FLAG + ":"  + " </B></TD>"
    	  + "<TD ALIGN=LEFT>"
    	  + "<INPUT TYPE=TEXT NAME=\"" + SMTablecostcenters.scostcentername + "\""
    	  + " VALUE=\"" + entry.get_scostcentername().replace("\"", "&quot;") + "\""
    	  + " MAXLENGTH=" + Integer.toString(SMTablecostcenters.scostcenternamelength)
    	  + " STYLE=\"height: 0.25in\""
    	  + "></TD>"
    	  + "<TD ALIGN=LEFT><I>" 
    	  + "Unique name for this cost center" 
    	  + "</I></TD>"
    	  + "</TR>"
    	  ;

	    //Description
        s += "<TR>"
    	  + "<TD ALIGN=RIGHT><B>" + "Description" + REQUIRED_FIELD_FLAG + "</FONT>:"  + " </B></TD>"
    	  + "<TD ALIGN=LEFT>"
    	  + "<INPUT TYPE=TEXT NAME=\"" + SMTablecostcenters.sdescription + "\""
    	  + " VALUE=\"" + entry.get_sdescription().replace("\"", "&quot;") + "\""
    	  + " MAXLENGTH=" + Integer.toString(SMTablecostcenters.sdescriptionlength)
    	  + " STYLE=\"height: 0.25in; width: 4.00in; \""
    	  + "></TD>"
    	  + "<TD ALIGN=LEFT><I>" 
    	  + "Longer description of this particular cost center" 
    	  + "</I></TD>"
    	  + "</TR>"
    	  ;

        s += "</TABLE>";
        s += "<P><INPUT TYPE=SUBMIT NAME='" + SUBMIT_UPDATE_BUTTON_NAME 
        	+ "' VALUE='" + SUBMIT_UPDATE_BUTTON_VALUE + "' STYLE='height: 0.24in'></P>";
        
        s += "<BR>"
        	+ "&nbsp;" + REQUIRED_FIELD_FLAG + " Indicates a REQUIRED field";

		return s;
	}
	
	public void doGet(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
		
		doPost(request, response);
	}
}