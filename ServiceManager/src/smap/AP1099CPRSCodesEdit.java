package smap;

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
import SMDataDefinition.SMTableap1099cprscodes;
import ServletUtilities.clsCreateHTMLTableFormFields;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsManageRequestParameters;


public class AP1099CPRSCodesEdit extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final String SUBMIT_UPDATE_BUTTON_NAME = "SubmitEditUpdate";
	private static final String SUBMIT_UPDATE_BUTTON_VALUE = "Update " + AP1099CPRSCodes.ParamObjectName;
	
	public void doPost(HttpServletRequest request,
				HttpServletResponse response)
				throws ServletException, IOException {
		

		AP1099CPRSCodes entry = new AP1099CPRSCodes(request);
		
		SMMasterEditEntry smedit = new SMMasterEditEntry(
				request,
				response,
				getServletContext(),
				entry.getObjectName(),
				SMUtilities.getFullClassName(this.toString()),
				"smap.AP1099CPRSCodesEdit",
				"smcontrolpanel.SMUserLogin",
				"Go back to user login",
				SMSystemFunctions.APEdit1099CPRSCodes
		);    
		
		if (!smedit.processSession(getServletContext(), SMSystemFunctions.APEdit1099CPRSCodes, request)){
			smedit.getPWOut().println("Error in process session: " + smedit.getErrorMessages());
			return;
		}
		
	    //First process if it's a 'delete' from the Selection Screen:
	    if(request.getParameter(AP1099CPRSCodesSelection.SUBMIT_DELETE_BUTTON_NAME) != null){
		    if (request.getParameter(AP1099CPRSCodesSelection.CONFIRM_DELETE_CHECKBOX_NAME) == null){
				response.sendRedirect(
					"" + SMUtilities.getURLLinkBase(getServletContext()) + "smap.AP1099CPRSCodesSelection"
					+ "?" + AP1099CPRSCodes.Paramlid + "=" + entry.getlid()
					+ "&Warning=You must check the 'confirming' check box to delete."
				);
				return;
		    }
		    if (entry.getlid().compareToIgnoreCase("") == 0){
				response.sendRedirect(
					"" + SMUtilities.getURLLinkBase(getServletContext()) + "smap.AP1099CPRSCodesSelection"
					+ "?" + AP1099CPRSCodes.Paramlid + "=" + entry.getlid()
					+ "&Warning=You must select a 1099/CPRS Code to delete."
				);
				return;
		    }
		    
		    else{
		    	//Need a connection for the 'delete':
		    	Connection conn = clsDatabaseFunctions.getConnection(
		    		getServletContext(), 
		    		smedit.getsDBID(),
		    		"MySQL",
		    		this.toString() + ".doPost - User: " 
		    		+ smedit.getUserID()
		    		+ " - "
		    		+ smedit.getFullUserName()
		    			);
		    	if(conn == null){
    				response.sendRedirect(
        					"" + SMUtilities.getURLLinkBase(getServletContext()) + "smap.AP1099CPRSCodesSelection"
        					+ "?" + AP1099CPRSCodes.Paramlid + "=" + entry.getlid()
        					+ "&Warning=Error deleting 1099/CPRS Code - cannot get connection."
        				);
    						return;
		    	}
		    	clsDatabaseFunctions.start_data_transaction(conn);
			    if (!entry.delete(entry.getlid(), conn)){
			    	clsDatabaseFunctions.rollback_data_transaction(conn);
			    	clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1546998931]");
    				response.sendRedirect(
    					"" + SMUtilities.getURLLinkBase(getServletContext()) + "smap.AP1099CPRSCodesSelection"
    					+ "?" + AP1099CPRSCodes.Paramlid + "=" + entry.getlid()
    					+ "&Warning=Error deleting 1099/CPRS Code - " + entry.getlid()
    				);
					return;
			    }else{
			    	clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1546998932]");
    				response.sendRedirect(
    					"" + SMUtilities.getURLLinkBase(getServletContext()) + "smap.AP1099CPRSCodesSelection"
    					+ "?" + AP1099CPRSCodes.Paramlid + "=" + entry.getlid()
    					+ "&Status=Successfully deleted 1099/CPRS Code " + entry.getlid() + "."
    				);
					return;
			    }
		    }
	    }
		
	    //If coming from Add button of select screen; set as new record and clear ID
		if(request.getParameter(AP1099CPRSCodesSelection.SUBMIT_ADD_BUTTON_NAME) != null){
			entry.setNewRecord("1");
			entry.setlid("-1");
		}
		
		//If we are coming here for the first time to edit (i.e., it's not a re-submit), then we need to 
		//load the input class from the database if possible:
		if(request.getParameter(AP1099CPRSCodesSelection.SUBMIT_EDIT_BUTTON_NAME) != null){
	    	try{
			entry.load(smedit.getsDBID(), getServletContext(), smedit.getUserName());
	    	}catch (Exception e){
				response.sendRedirect(
					"" + SMUtilities.getURLLinkBase(getServletContext()) + "smap.AP1099CPRSCodesSelection"
					+ "?" + AP1099CPRSCodes.Paramlid + "=" + entry.getlid()
					+ "&Warning=" + e.getMessage()
				);
					return;
	    	}
	    	
		}
		
		//NOTE: This page posts back to itself
		//If update, save the entries:
		String sSaveStatus = "";
		if(clsManageRequestParameters.get_Request_Parameter(AP1099CPRSCodesEdit.SUBMIT_UPDATE_BUTTON_NAME, request)
				.compareToIgnoreCase(SUBMIT_UPDATE_BUTTON_VALUE) == 0){

			if (!entry.saveEditableFields(getServletContext(), smedit.getsDBID(), smedit.getUserID(), smedit.getFullUserName())){
				//Set attribute before reloading the screen
				smedit.getCurrentSession().setAttribute(AP1099CPRSCodes.ParamObjectName, entry);
				//'Resubmit' the screen (without the submit button parameter)
				response.sendRedirect(
						"" + SMUtilities.getURLLinkBase(getServletContext()) + "smap." + "AP1099CPRSCodesEdit" + "?"
						+ AP1099CPRSCodes.Paramlid + "=" + entry.getlid()
						+ "&" + AP1099CPRSCodes.ParamsNewRecord + "=" + entry.getNewRecord()
						+ "&Warning=" + "Could not save 1099/CPRS Code:\n "					
						+ entry.getErrorMessageString()
				);
		        return;
			}else{
				 sSaveStatus = "1099/CPRS Code '" + entry.getlid() + "' saved successfully.";
			}
			
		}
		
		//If this is a 'resubmit', meaning it's being called by this class, then
		//the session will have an AP1099CPRSCodes object in it, and that's what we'll pick up.
		HttpSession currentSession = smedit.getCurrentSession();
		if (currentSession.getAttribute(AP1099CPRSCodes.ParamObjectName) != null){
			entry = (AP1099CPRSCodes) currentSession.getAttribute(AP1099CPRSCodes.ParamObjectName);
			currentSession.removeAttribute(AP1099CPRSCodes.ParamObjectName);
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
			smedit.getPWOut().println("Error [1450726386] getting session attribute - " + e1.getMessage());
			return;
		}
		
		//Build page header:
	    String title;
	    String subtitle = "";
	    title = "Edit Accounts Payable 1099/CPRS Codes";

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
		smedit.getPWOut().println("<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smap.APMainMenu?" 
				+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + smedit.getsDBID() 
				+ "\">Return to Accounts Payable Main Menu</A><BR>");
		
	try{ 
		createEditPage(
				getEditHTML(smedit, entry),
				AP1099CPRSCodes.FORM_NAME,
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
			AP1099CPRSCodes entry
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
	
	private String getEditHTML(SMMasterEditEntry smedit, AP1099CPRSCodes entry) throws Exception	{
		String s = "";
		//Start the table:
		s += "<TABLE BORDER=12 CELLSPACING=2>";		
	
		//Vendor Group:
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
	    + "<INPUT TYPE=HIDDEN NAME=\"" + AP1099CPRSCodes.Paramlid+ "\" VALUE='" + entry.getlid() + "'>"
	    + "<INPUT TYPE=HIDDEN NAME=\"" + AP1099CPRSCodes.ParamsNewRecord+ "\" VALUE='" + entry.getNewRecord() + "'>"
	    ;  	

	    //Name:
        s += "<TR>"
    	  + "<TD ALIGN=RIGHT><B>" + "Class ID:"  + " </B></TD>"
    	  + "<TD ALIGN=LEFT>"
    	  + "<INPUT TYPE=TEXT NAME=\"" + AP1099CPRSCodes.Paramsclassid + "\""
    	  + " VALUE=\"" + entry.getsclassid().replace("\"", "&quot;") + "\""
    	  + " MAXLENGTH=" + Integer.toString(SMTableap1099cprscodes.sclassidlength)
    	  + " STYLE=\"height: 0.25in\""
    	  + "></TD>"
    	  + "<TD ALIGN=LEFT>" 
    	  + "Every 1099/CPRS Code must have a unique class id. " 
    	  + "</TD>"
    	  + "</TR>"
    	  ;
	    
        //Description:
        s += "<TR>"
    	  + "<TD ALIGN=RIGHT><B>" + "Description:"  + " </B></TD>"
    	  + "<TD ALIGN=LEFT>"
    	  + "<INPUT TYPE=TEXT NAME=\"" + AP1099CPRSCodes.Paramsdescription + "\""
    	  + " VALUE=\"" + entry.getsdescription().replace("\"", "&quot;") + "\""
    	  + " SIZE=" + Integer.toString(SMTableap1099cprscodes.sdescriptionlength)
    	  + " MAXLENGTH=" + Integer.toString(SMTableap1099cprscodes.sdescriptionlength)
    	  + " STYLE=\"height: 0.25in\""
    	  + "></TD>"
    	  + "<TD ALIGN=LEFT>" 
    	  + "Short description of this 1099/CPRS Code. " 
    	  + "</TD>"
    	  + "</TR>"
    	  ;

		//Active?
	    int iTrueOrFalse = 0;
	    if (entry.getiactive().compareToIgnoreCase("1") == 0){
	    	iTrueOrFalse = 1;
	    }else{
	    	iTrueOrFalse = 0;
	    }
	    s += clsCreateHTMLTableFormFields.Create_Edit_Form_Checkbox_Row(
	    	AP1099CPRSCodes.Paramiactive, 
			iTrueOrFalse, 
			"Active Code?", 
			"Uncheck to de-activate this code."
			)
		;

        //Terms Code:
        s += "<TR>"
    	  + "<TD ALIGN=RIGHT><B>" + "Minimum Reporting Amount:"  + " </B></TD>"
    	  + "<TD ALIGN=LEFT>"
    	  + "<INPUT TYPE=TEXT NAME=\"" + AP1099CPRSCodes.Parambdminimumreportingamt + "\""
    	  + " VALUE=\"" + entry.getbdminimumreportingamt().replace("\"", "&quot;") + "\""
    	  + " STYLE=\"height: 0.25in\""
    	  + "></TD>"
    	  + "<TD ALIGN=LEFT>" 
    	  + "Enter a minimum reporting amount." 
    	  + "</TD>"
    	  + "</TR>"
    	  ;
		
        s += "</TABLE>";
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