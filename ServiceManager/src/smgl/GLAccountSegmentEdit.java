package smgl;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import SMDataDefinition.SMTableglaccountsegments;
import ServletUtilities.clsCreateHTMLTableFormFields;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsManageRequestParameters;
import smcontrolpanel.SMMasterEditEntry;
import smcontrolpanel.SMSystemFunctions;
import smcontrolpanel.SMUtilities;


public class GLAccountSegmentEdit extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
	private static final String OBJECT_NAME = GLAccountSegment.ParamObjectName;
	private static final String SUBMIT_UPDATE_BUTTON_NAME = "SubmitEditUpdate";
	private static final String SUBMIT_UPDATE_BUTTON_VALUE = "Update " + OBJECT_NAME;
	
	public void doPost(HttpServletRequest request,
				HttpServletResponse response)
				throws ServletException, IOException {
		
		GLAccountSegment entry = new GLAccountSegment(request);
		
		SMMasterEditEntry smedit = new SMMasterEditEntry(
				request,
				response,
				getServletContext(),
				entry.getObjectName(),
				SMUtilities.getFullClassName(this.toString()),
				"smgl.GLAccountSegmentEdit",
				"smcontrolpanel.SMUserLogin",
				"Go back to user login",
				SMSystemFunctions.GLEditAccountSegments
		);    
		
		if (!smedit.processSession(getServletContext(), SMSystemFunctions.GLEditAccountSegments, request)){
			smedit.getPWOut().println("Error in process session: " + smedit.getErrorMessages());
			return;
		}
		
	    //First process if it's a 'delete' from the Selection Screen:
	    if(request.getParameter(GLAccountSegmentSelect.SUBMIT_DELETE_BUTTON_NAME) != null){
		    if (request.getParameter(GLAccountSegmentSelect.CONFIRM_DELETE_CHECKBOX_NAME) == null){
				response.sendRedirect(
					"" + SMUtilities.getURLLinkBase(getServletContext()) + "smgl.GLAccountSegmentSelect"
					+ "?" + SMTableglaccountsegments.lid + "=" + entry.getlid()
					+ "&Warning=You must check the 'confirming' check box to delete."
				);
				return;
		    }
		    if (entry.getlid().compareToIgnoreCase("") == 0){
				response.sendRedirect(
					"" + SMUtilities.getURLLinkBase(getServletContext()) + "smgl.GLAccountSegmentSelect"
					+ "?" + SMTableglaccountsegments.lid + "=" + entry.getlid()
					+ "&Warning=You must select an " + OBJECT_NAME + " to delete."
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
        					"" + SMUtilities.getURLLinkBase(getServletContext()) + "smgl.GLAccountSegmentSelect"
        					+ "?" + SMTableglaccountsegments.lid + "=" + entry.getlid()
        					+ "&Warning=Error deleting " + OBJECT_NAME + " - cannot get connection."
        				);
    						return;
		    	}
		    	
		    	try{
		    		entry.delete(entry.getlid(), conn);
		    	}catch (Exception e){
			    	clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1547080709]");
    				response.sendRedirect(
    					"" + SMUtilities.getURLLinkBase(getServletContext()) + "smgl.GLAccountSegmentSelect"
    					+ "?" + SMTableglaccountsegments.lid + "=" + entry.getlid()
    					+ "&Warning=Error deleting " + OBJECT_NAME + " " + entry.getsdescription()
    				);
					return;
		    	}
		    	clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1547080710]");
				response.sendRedirect(
					"" + SMUtilities.getURLLinkBase(getServletContext()) + "smgl.GLAccountSegmentSelect"
					+ "?" + SMTableglaccountsegments.lid + "=" + entry.getlid()
					+ "&Status=Successfully deleted " + OBJECT_NAME + " - " + entry.getsdescription() + "."
				);
				return;
		    }
	    }
		
	    //If coming from Add button of select screen; set as new record and clear ID
		if(request.getParameter(GLAccountSegmentSelect.SUBMIT_ADD_BUTTON_NAME) != null){
			entry.setNewRecord("1");
			entry.setlid("-1");
		}
		
		//If we are coming here for the first time to edit (i.e., it's not a re-submit), then we need to 
		//load the input class from the database if possible:
		if(request.getParameter(GLAccountSegmentSelect.SUBMIT_EDIT_BUTTON_NAME) != null){
	    	try{
			entry.load(smedit.getsDBID(), getServletContext(), smedit.getUserName());
	    	}catch (Exception e){
				response.sendRedirect(
					"" + SMUtilities.getURLLinkBase(getServletContext()) + "smgl.GLAccountSegmentSelect"
					+ "?" + SMTableglaccountsegments.lid + "=" + entry.getlid()
					+ "&Warning=" + e.getMessage()
				);
					return;
	    	}
	    	
		}
		
		//NOTE: This page posts back to itself
		//If update, save the entries:
		String sSaveStatus = "";
		if(clsManageRequestParameters.get_Request_Parameter(GLAccountSegmentEdit.SUBMIT_UPDATE_BUTTON_NAME, request)
			.compareToIgnoreCase(SUBMIT_UPDATE_BUTTON_VALUE) == 0){

			try {
				entry.saveEditableFields(getServletContext(), smedit.getsDBID(), smedit.getUserName());
			} catch (Exception e) {
				//Set attribute before reloading the screen
				smedit.getCurrentSession().setAttribute(OBJECT_NAME, entry);
				//'Resubmit' the screen (without the submit button parameter)
				response.sendRedirect(
						"" + SMUtilities.getURLLinkBase(getServletContext()) + "smgl." + "GLAccountSegmentEdit" + "?"
						+ SMTableglaccountsegments.lid + "=" + entry.getlid()
						+ "&" + GLAccountSegment.ParamsNewRecord + "=" + entry.getNewRecord()
						+ "&Warning=" + "Could not save " + OBJECT_NAME + ":\n "					
						+ e.getMessage()
				);
		        return;
				
			}
			sSaveStatus = OBJECT_NAME + " '" + entry.getsdescription() + "' saved successfully.";
		}
		
		//If this is a 'resubmit', meaning it's being called by this class, then
		//the session will have an object in it, and that's what we'll pick up.
		HttpSession currentSession = smedit.getCurrentSession();
		if (currentSession.getAttribute(OBJECT_NAME) != null){
			entry = (GLAccountSegment) currentSession.getAttribute(OBJECT_NAME);
			currentSession.removeAttribute(OBJECT_NAME);
		//But if it's NOT a 'resubmit', meaning this class was called for the first time to 
		//edit OR after a previous successful edit, we'll load the entry:
		}else{
			try {
				entry.load( smedit.getsDBID(), getServletContext(), smedit.getUserName());
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
			smedit.getPWOut().println("Error [1523054243] getting session attribute - " + e1.getMessage());
			return;
		}
		
		//Build page header:
	    String title;
	    String subtitle = "";
	    title = "Edit General Ledger " + OBJECT_NAME + "s";

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
		
	    //Print a link to main menu:
		smedit.getPWOut().println("<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smgl.GLMainMenu?" 
				+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + smedit.getsDBID() 
				+ "\">Return to General Ledger Main Menu</A><BR>");
		
	try{ 
		createEditPage(
				getEditHTML(smedit, entry),
				GLAccountSegment.FORM_NAME,
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
			GLAccountSegment entry
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
	
	private String getEditHTML(SMMasterEditEntry smedit, GLAccountSegment entry) throws Exception	{
		String s = "";
		//Start the table:
		s += "<TABLE BORDER=12 CELLSPACING=2>";		
	
		//ID:
	    s +="<TR>"
	    + "<TD ALIGN=RIGHT><B>" + "ID:"  + " </B></TD>"
	    + "<TD ALIGN=LEFT>";
	      if(entry.getNewRecord().compareToIgnoreCase("1") == 0){
	         s+= "NEW";
	      }else{
	         s+= entry.getlid();
	      }
	    s+= "</TD>"
	    + "<TD ALIGN=LEFT>" 
	    + " " 
	    + "</TD>"
	    + "</TR>"
	    + "<INPUT TYPE=HIDDEN NAME=\"" + SMTableglaccountsegments.lid + "\" VALUE='" + entry.getlid() + "'>"
	    + "<INPUT TYPE=HIDDEN NAME=\"" + GLAccountSegment.ParamsNewRecord+ "\" VALUE='" + entry.getNewRecord() + "'>"
	    ;  	

	    //Description:
        s += "<TR>"
    	  + "<TD ALIGN=RIGHT><B>" + "Description:"  + " </B></TD>"
    	  + "<TD ALIGN=LEFT>"
    	  + "<INPUT TYPE=TEXT NAME=\"" + SMTableglaccountsegments.sdescription + "\""
    	  + " VALUE=\"" + entry.getsdescription().replace("\"", "&quot;") + "\""
    	  + " MAXLENGTH=" + Integer.toString(SMTableglaccountsegments.sdescriptionLength)
    	  + " STYLE=\"height: 0.25in\""
    	  + "></TD>"
    	  + "<TD ALIGN=LEFT>" 
    	  + "Every " + OBJECT_NAME + " must have a unique description. " 
    	  + "</TD>"
    	  + "</TR>"
    	  ;
	    
		//Used in closing?
	    int iTrueOrFalse = 0;
	    if (entry.getsiusedinclosing().compareToIgnoreCase("1") == 0){
	    	iTrueOrFalse = 1;
	    }else{
	    	iTrueOrFalse = 0;
	    }
	    s += clsCreateHTMLTableFormFields.Create_Edit_Form_Checkbox_Row(
	    	SMTableglaccountsegments.iuseinclosing, 
			iTrueOrFalse, 
			"Used in closing?", 
			""
			)
		;

	    //Length:
        s += "<TR>"
    	  + "<TD ALIGN=RIGHT><B>" + "Segment length:"  + " </B></TD>"
    	  + "<TD ALIGN=LEFT>"
    	  + "<INPUT TYPE=TEXT NAME=\"" + SMTableglaccountsegments.ilength + "\""
    	  + " VALUE=\"" + entry.getsilength().replace("\"", "&quot;") + "\""
    	  + " MAXLENGTH=" + Integer.toString(SMTableglaccountsegments.ilengthMAX_LENGTH)
    	  + " STYLE=\"height: 0.25in\""
    	  + "></TD>"
    	  + "<TD ALIGN=LEFT>" 
    	  + "Define the length of the segment." 
    	  + "</TD>"
    	  + "</TR>"
    	  ;
		
        s += "</TABLE>\n";
        s += "<P><INPUT TYPE=SUBMIT NAME='" + SUBMIT_UPDATE_BUTTON_NAME 
            	+ "' VALUE='" + SUBMIT_UPDATE_BUTTON_VALUE + "' STYLE='height: 0.24in'></P>";
		return s;
	}
	
	public void doGet(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
		
		doPost(request, response);
	}
}