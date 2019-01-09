package smap;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import smcontrolpanel.SMMasterEditEntry;
import smcontrolpanel.SMSystemFunctions;
import smcontrolpanel.SMUtilities;
import SMDataDefinition.SMTableapdistributioncodes;
import SMDataDefinition.SMTableglaccounts;
import ServletUtilities.clsCreateHTMLTableFormFields;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsManageRequestParameters;


public class APDistributionCodesEdit extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final String SUBMIT_UPDATE_BUTTON_NAME = "SubmitEditUpdate";
	private static final String SUBMIT_UPDATE_BUTTON_VALUE = "Update " + APDistributionCode.ParamObjectName;
	
	public void doPost(HttpServletRequest request,
				HttpServletResponse response)
				throws ServletException, IOException {
		

		APDistributionCode entry = new APDistributionCode(request);
		
		SMMasterEditEntry smedit = new SMMasterEditEntry(
				request,
				response,
				getServletContext(),
				entry.getObjectName(),
				SMUtilities.getFullClassName(this.toString()),
				"smap.APDistributionCodesEdit",
				"smcontrolpanel.SMUserLogin",
				"Go back to user login",
				SMSystemFunctions.APEditDistributionCodes
		);    
		
		if (!smedit.processSession(getServletContext(), SMSystemFunctions.APEditDistributionCodes, request)){
			smedit.getPWOut().println("Error in process session: " + smedit.getErrorMessages());
			return;
		}
		
	    //First process if it's a 'delete' from the Selection Screen:
	    if(request.getParameter(APDistributionCodesSelection.SUBMIT_DELETE_BUTTON_NAME) != null){
		    if (request.getParameter(APDistributionCodesSelection.CONFIRM_DELETE_CHECKBOX_NAME) == null){
				response.sendRedirect(
					"" + SMUtilities.getURLLinkBase(getServletContext()) + "smap.APDistributionCodesSelection"
					+ "?" + APDistributionCode.Paramlid + "=" + entry.getlid()
					+ "&Warning=You must check the 'confirming' check box to delete."
				);
				return;
		    }
		    if (entry.getlid().compareToIgnoreCase("") == 0){
				response.sendRedirect(
					"" + SMUtilities.getURLLinkBase(getServletContext()) + "smap.APDistributionCodesSelection"
					+ "?" + APDistributionCode.Paramlid + "=" + entry.getlid()
					+ "&Warning=You must select a Distribution Code to delete."
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
		    		+ smedit.getFullUserName());
		    	if(conn == null){
    				response.sendRedirect(
        					"" + SMUtilities.getURLLinkBase(getServletContext()) + "smap.APDistributionCodesSelection"
        					+ "?" + APDistributionCode.Paramlid + "=" + entry.getlid()
        					+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + smedit.getsDBID() 
        					+ "&Warning=Error deleting Distribution Code - cannot get connection."
        				);
    						return;
		    	}
		    	
			    if (!entry.delete(entry.getlid(), conn)){
			    	clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1547047710]");
    				response.sendRedirect(
    					"" + SMUtilities.getURLLinkBase(getServletContext()) + "smap.APDistributionCodesSelection"
    					+ "?" + APDistributionCode.Paramlid + "=" + entry.getlid()
    					+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + smedit.getsDBID() 
    					+ "&Warning=Error deleting Distribution Code - " + entry.getlid()
    				);
					return;
			    }else{
			    	clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1547047711]");
    				response.sendRedirect(
    					"" + SMUtilities.getURLLinkBase(getServletContext()) + "smap.APDistributionCodesSelection"
    					+ "?" + APDistributionCode.Paramlid + "=" + entry.getlid()
    					+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + smedit.getsDBID() 
    					+ "&Status=Successfully deleted Distribution Code " + entry.getlid() + "."
    				);
					return;
			    }
		    }
	    }
		
	    //If coming from Add button of select screen; set as new record and clear ID
		if(request.getParameter(APDistributionCodesSelection.SUBMIT_ADD_BUTTON_NAME) != null){
			entry.setNewRecord("1");
			entry.setlid("-1");
		}
		
		//If we are coming here for the first time to edit (i.e., it's not a re-submit), then we need to 
		//load the input class from the database if possible:
		if(request.getParameter(APDistributionCodesSelection.SUBMIT_EDIT_BUTTON_NAME) != null){
	    	try{
			entry.load(smedit.getsDBID(), getServletContext(), smedit.getUserName());
	    	}catch (Exception e){
				response.sendRedirect(
					"" + SMUtilities.getURLLinkBase(getServletContext()) + "smap.APDistributionCodesSelection"
					+ "?" + APDistributionCode.Paramlid + "=" + entry.getlid()
					+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + smedit.getsDBID() 
					+ "&Warning=" + e.getMessage()
				);
					return;
	    	}
	    	
		}
		
		//NOTE: This page posts back to itself
		//If update, save the entries:
		String sSaveStatus = "";
		if(clsManageRequestParameters.get_Request_Parameter(APDistributionCodesEdit.SUBMIT_UPDATE_BUTTON_NAME, request)
				.compareToIgnoreCase(SUBMIT_UPDATE_BUTTON_VALUE) == 0){

			if (!entry.saveEditableFields(getServletContext(), smedit.getsDBID(), smedit.getUserID(), smedit.getFullUserName())){
				//Set attribute before reloading the screen
				smedit.getCurrentSession().setAttribute(APDistributionCode.ParamObjectName, entry);
				//'Resubmit' the screen (without the submit button parameter)
				response.sendRedirect(
						"" + SMUtilities.getURLLinkBase(getServletContext()) + "smap." + "APDistributionCodesEdit" + "?"
						+ APDistributionCode.Paramlid + "=" + entry.getlid()
						+ "&" + APDistributionCode.ParamsNewRecord + "=" + entry.getNewRecord()
						+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + smedit.getsDBID() 
						+ "&Warning=" + "Could not save Distribution Code:\n "					
						+ entry.getErrorMessageString()
				);
		        return;
			}else{
				 sSaveStatus = "Distribution Code '" + entry.getlid() + "' saved successfully.";
			}
			
		}
		
		//If this is a 'resubmit', meaning it's being called by this class, then
		//the session will have an object in it, and that's what we'll pick up.
		HttpSession currentSession = smedit.getCurrentSession();
		if (currentSession.getAttribute(APDistributionCode.ParamObjectName) != null){
			entry = (APDistributionCode) currentSession.getAttribute(APDistributionCode.ParamObjectName);
			currentSession.removeAttribute(APDistributionCode.ParamObjectName);
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
			smedit.getPWOut().println("Error [1450811453] getting session attribute - " + e1.getMessage());
			return;
		}
		
		//Build page header:
	    String title;
	    String subtitle = "";
	    title = "Edit Accounts Payable Distribution Codes";

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
				APDistributionCode.FORM_NAME,
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
			APDistributionCode entry
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
	
	private String getEditHTML(SMMasterEditEntry smedit, APDistributionCode entry) throws Exception	{
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
	    + "<INPUT TYPE=HIDDEN NAME=\"" + APDistributionCode.Paramlid+ "\" VALUE='" + entry.getlid() + "'>"
	    + "<INPUT TYPE=HIDDEN NAME=\"" + APDistributionCode.ParamsNewRecord+ "\" VALUE='" + entry.getNewRecord() + "'>"
	    ;  	

	    //Name:
        s += "<TR>"
    	  + "<TD ALIGN=RIGHT><B>" + "Name:"  + " </B></TD>"
    	  + "<TD ALIGN=LEFT>"
    	  + "<INPUT TYPE=TEXT NAME=\"" + APDistributionCode.Paramsdistcodename + "\""
    	  + " VALUE=\"" + entry.getsdistcodename().replace("\"", "&quot;") + "\""
    	  + " MAXLENGTH=" + Integer.toString(SMTableapdistributioncodes.sdistcodenamelength)
    	  + " STYLE=\"height: 0.25in\""
    	  + "></TD>"
    	  + "<TD ALIGN=LEFT>" 
    	  + "Every Distribution Code must have a unique name. " 
    	  + "</TD>"
    	  + "</TR>"
    	  ;
	    
        //Description:
        s += "<TR>"
    	  + "<TD ALIGN=RIGHT><B>" + "Description:"  + " </B></TD>"
    	  + "<TD ALIGN=LEFT>"
    	  + "<INPUT TYPE=TEXT NAME=\"" + APDistributionCode.Paramsdescription + "\""
    	  + " VALUE=\"" + entry.getsdescription().replace("\"", "&quot;") + "\""
    	  + " SIZE=" + Integer.toString(SMTableapdistributioncodes.sdescriptionlength)
    	  + " MAXLENGTH=" + Integer.toString(SMTableapdistributioncodes.sdescriptionlength)
    	  + " STYLE=\"height: 0.25in\""
    	  + "></TD>"
    	  + "<TD ALIGN=LEFT>" 
    	  + "Short description of this Distribution Code. " 
    	  + "</TD>"
    	  + "</TR>"
    	  ;

		//Active?
	    int iTrueOrFalse = 0;
	    if (entry.getidiscountable().compareToIgnoreCase("1") == 0){
	    	iTrueOrFalse = 1;
	    }else{
	    	iTrueOrFalse = 0;
	    }
	    s += clsCreateHTMLTableFormFields.Create_Edit_Form_Checkbox_Row(
	    	APDistributionCode.Paramidiscountable, 
			iTrueOrFalse, 
			"Discountable?", 
			"Uncheck to de-activate discounts."
			)
		;

	    //Load the GL Accounts for GL account used for distribution:
	    ArrayList<String> arrValues = new ArrayList<String>(0);
        ArrayList<String> arrDescriptions = new ArrayList<String>(0);
        arrValues.add("0");
        arrDescriptions.add("-- Select GL Account --");
        try{
			//Get the record to edit:
          String sInactive = "";
	      String sSQL = "SELECT * FROM " + SMTableglaccounts.TableName 
	    		   + " ORDER BY " + SMTableglaccounts.sAcctID;
	        ResultSet rsGLAccts = clsDatabaseFunctions.openResultSet(
	        	sSQL, 
	        	getServletContext(), 
	        	smedit.getsDBID(),
	        	"MySQL",
	        	this.toString() + ".Edit_Record - User: " + smedit.getUserID()
	        	+ " - "
	        	+ smedit.getFullUserName()
	        		);
	        
	        while (rsGLAccts.next()){
	        	arrValues.add(rsGLAccts.getString(SMTableglaccounts.sAcctID).trim());
	        	sInactive = "";
	        	if(rsGLAccts.getInt(SMTableglaccounts.lActive) == 0){
	        		sInactive = "(Inactive)";
	        	}
	        	arrDescriptions.add(rsGLAccts.getString(SMTableglaccounts.sAcctID).trim()
	        			+ " - " + rsGLAccts.getString(SMTableglaccounts.sDesc).trim()
	        			+ " " + sInactive);
			}
	        rsGLAccts.close();
		}catch (SQLException ex){
			s += "<BR><FONT COLOR=RED><B>Error [1450812029] reading GL accounts - " + ex.getMessage() + ".</FONT></B><BR>";
		}
		
		//GL account used for distribution

        s += clsCreateHTMLTableFormFields.Create_Edit_Form_List_Row(
        		APDistributionCode.Paramsglacct, 
        		arrValues, 
        		entry.getsglacct().replace("\"", "&quot;"),  
        		arrDescriptions, 
        		"GL Account:", 
        		"Select GL account used for distribution."
        		);
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