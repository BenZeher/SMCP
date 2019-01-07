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
import SMDataDefinition.SMTableapaccountsets;
import SMDataDefinition.SMTableglaccounts;
import ServletUtilities.clsCreateHTMLTableFormFields;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsManageRequestParameters;


public class APAccountSetsEdit extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final String SUBMIT_UPDATE_BUTTON_NAME = "SubmitEditUpdate";
	private static final String SUBMIT_UPDATE_BUTTON_VALUE = "Update " + APAccountSet.ParamObjectName;
	
	public void doPost(HttpServletRequest request,
				HttpServletResponse response)
				throws ServletException, IOException {
		

		APAccountSet entry = new APAccountSet();
		entry.loadFromHTTPRequest(request);
		
		SMMasterEditEntry smedit = new SMMasterEditEntry(
				request,
				response,
				getServletContext(),
				entry.getObjectName(),
				SMUtilities.getFullClassName(this.toString()),
				"smap.APAccountSetsEdit",
				"smcontrolpanel.SMUserLogin",
				"Go back to user login",
				SMSystemFunctions.APEditAccountSets
		);    
		
		if (!smedit.processSession(getServletContext(), SMSystemFunctions.APEditAccountSets, request)){
			smedit.getPWOut().println("Error in process session: " + smedit.getErrorMessages());
			return;
		}
		
	    //First process if it's a 'delete' from the Selection Screen:
	    if(request.getParameter(APAccountSetsSelection.SUBMIT_DELETE_BUTTON_NAME) != null){
		    if (request.getParameter(APAccountSetsSelection.CONFIRM_DELETE_CHECKBOX_NAME) == null){
				response.sendRedirect(
					"" + SMUtilities.getURLLinkBase(getServletContext()) + "smap.APAccountSetsSelection"
					+ "?" + APAccountSet.Paramlid + "=" + entry.getlid()
					+ "&Warning=You must check the 'confirming' check box to delete."
				);
				return;
		    }
		    if (entry.getlid().compareToIgnoreCase("") == 0){
				response.sendRedirect(
					"" + SMUtilities.getURLLinkBase(getServletContext()) + "smap.APAccountSetsSelection"
					+ "?" + APAccountSet.Paramlid + "=" + entry.getlid()
					+ "&Warning=You must enter an account set to delete."
				);
				return;
		    }
		    
		    else{
		    	//Need a connection for the 'delete':
		    	Connection conn = clsDatabaseFunctions.getConnection(
		    		getServletContext(), 
		    		smedit.getsDBID(),
		    		"MySQL",
		    		this.toString() 
		    		+ ".doPost - User: " 
		    		+ smedit.getUserID()
		    		+ " - "
		    		+ smedit.getFullUserName()
		    			);
		    	if(conn == null){
    				response.sendRedirect(
        					"" + SMUtilities.getURLLinkBase(getServletContext()) + "smap.APAccountSetsSelection"
        					+ "?" + APAccountSet.Paramlid + "=" + entry.getlid()
        					+ "&Warning=Error deleting account set - cannot get connection."
        				);
    						return;
		    	}
		    	clsDatabaseFunctions.start_data_transaction(conn);
			    if (!entry.delete(entry.getlid(), conn)){
			    	clsDatabaseFunctions.rollback_data_transaction(conn);
			    	clsDatabaseFunctions.freeConnection(getServletContext(), conn);
    				response.sendRedirect(
    					"" + SMUtilities.getURLLinkBase(getServletContext()) + "smap.APAccountSetsSelection"
    					+ "?" + APAccountSet.Paramlid + "=" + entry.getlid()
    					+ "&Warning=Error deleting account set - " + entry.getlid()
    				);
					return;
			    }else{
			    	clsDatabaseFunctions.commit_data_transaction(conn);
			    	clsDatabaseFunctions.freeConnection(getServletContext(), conn);
    				response.sendRedirect(
    					"" + SMUtilities.getURLLinkBase(getServletContext()) + "smap.APAccountSetsSelection"
    					+ "?" + APAccountSet.Paramlid + "=" + entry.getlid()
    					+ "&Status=Successfully deleted account set " + entry.getlid() + "."
    				);
					return;
			    }
		    }
	    }
		
	    //If coming from Add button of select screen; set as new record and clear ID
		if(request.getParameter(APAccountSetsSelection.SUBMIT_ADD_BUTTON_NAME) != null){
			entry.setNewRecord("1");
			entry.setlid("");
		}
		
		//If we are coming here for the first time to edit (i.e., it's not a re-submit), then we need to 
		//load the input class from the database if possible:
		if(request.getParameter(APAccountSetsSelection.SUBMIT_EDIT_BUTTON_NAME) != null){
	    	if(!entry.load(getServletContext(), smedit.getsDBID())){
				response.sendRedirect(
					"" + SMUtilities.getURLLinkBase(getServletContext()) + "smap.APAccountSetsSelection"
					+ "?" + APAccountSet.Paramlid + "=" + entry.getlid()
					+ "&Warning=Could not load account set " + entry.getlid() + "."
				);
					return;
	    	}
		}
		
		//NOTE: This page posts back to itself
		//If update, save the entries:
		String sSaveStatus = "";
		if(clsManageRequestParameters.get_Request_Parameter(APAccountSetsEdit.SUBMIT_UPDATE_BUTTON_NAME, request)
				.compareToIgnoreCase(SUBMIT_UPDATE_BUTTON_VALUE) == 0){
			//Need to get connection
			Connection conn = clsDatabaseFunctions.getConnection(
					getServletContext(), 
					smedit.getsDBID(),
					"MySQL",
					this.toString() + ".doPost - User: " + smedit.getUserName());
			if (!entry.save(conn)){
				clsDatabaseFunctions.freeConnection(getServletContext(), conn);
				//Set attribute before reloading the screen
				smedit.getCurrentSession().setAttribute(APAccountSet.ParamObjectName, entry);
				//'Resubmit' the screen (without the submit button parameter)
				response.sendRedirect(
						"" + SMUtilities.getURLLinkBase(getServletContext()) + "smap." + "APAccountSetsEdit" + "?"
						+ APAccountSet.Paramlid + "=" + entry.getlid()
						+ "&Warning=" + "Could not save Accounts Payable Account Set: "
						+ entry.getErrorMessageString()
				);
		        return;
			}else{
				clsDatabaseFunctions.freeConnection(getServletContext(), conn);
				 sSaveStatus = "Account set '" + entry.getlid() + "' saved successfully.";
			}
			
		}
		
		//If this is a 'resubmit', meaning it's being called by this class, then
		//the session will have an APAccountSet object in it, and that's what we'll pick up.
		HttpSession currentSession = smedit.getCurrentSession();
		if (currentSession.getAttribute(APAccountSet.ParamObjectName) != null){
			entry = (APAccountSet) currentSession.getAttribute(APAccountSet.ParamObjectName);
			currentSession.removeAttribute(APAccountSet.ParamObjectName);
		//But if it's NOT a 'resubmit', meaning this class was called for the first time to 
		//edit OR after a previous successful edit, we'll load the entry:
		}else{
			try {
				entry.load(getServletContext(), smedit.getsDBID());
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
	    title = "Edit Accounts Payable Account Sets";

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
				APOptions.FORM_NAME,
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
			APAccountSet entry
	) throws Exception{

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
	
	private String getEditHTML(SMMasterEditEntry smedit, APAccountSet entry) throws Exception{
		String s = "";
		
		//Start the table:
		s += "<TABLE BORDER=12 CELLSPACING=2>";		
	
		//Account Set:
	    s +="<TR>"
	    + "<TD ALIGN=RIGHT><B>" + "Account set ID:"  + " </B></TD>"
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
	    + "<INPUT TYPE=HIDDEN NAME=\"" + APAccountSet.Paramlid+ "\" VALUE='" + entry.getlid() + "'>"
	    ;  	

	    //Name:
        s += "<TR>"
    	  + "<TD ALIGN=RIGHT><B>" + "Name<FONT COLOR=\"RED\">*</FONT>:"  + " </B></TD>"
    	  + "<TD ALIGN=LEFT>"
    	  + "<INPUT TYPE=TEXT NAME=\"" + APAccountSet.Paramsacctsetname + "\""
    	  + " VALUE=\"" + entry.getsacctsetname().replace("\"", "&quot;") + "\""
    	  + " SIZE=30"
    	  + " MAXLENGTH=" + Integer.toString(SMTableapaccountsets.sacctsetnamelength)
    	  + " STYLE=\"height: 0.25in\""
    	  + "></TD>"
    	  + "<TD ALIGN=LEFT>" 
    	  + "Name for the account set. " 
    	  + "</TD>"
    	  + "</TR>"
    	  ;
	    
        //Description:
        s += "<TR>"
    	  + "<TD ALIGN=RIGHT><B>" + "Description<FONT COLOR=\"RED\">*</FONT>:"  + " </B></TD>"
    	  + "<TD ALIGN=LEFT>"
    	  + "<INPUT TYPE=TEXT NAME=\"" + APAccountSet.Paramsdescription + "\""
    	  + " VALUE=\"" + entry.getsdescription().replace("\"", "&quot;") + "\""
    	  + " SIZE=30"
    	  + " MAXLENGTH=" + Integer.toString(SMTableapaccountsets.sdescriptionlength)
    	  + " STYLE=\"height: 0.25in\""
    	  + "></TD>"
    	  + "<TD ALIGN=LEFT>" 
    	  + "Short description of the account set. " 
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
	    	APAccountSet.Paramiactive, 
			iTrueOrFalse, 
			"Active account set?", 
			"Uncheck to de-activate this account set."
			)
		;
	    
	      //Load the GL Accounts:
	    ArrayList<String> arrValues = new ArrayList<String>(0);
        ArrayList<String> arrDescriptions = new ArrayList<String>(0);
        arrValues.add("");
        arrDescriptions.add("-- Select a GL Account --");
        try{
			//Get the record to edit:
	        String sSQL = SMClasses.MySQLs.Get_GL_Account_List_SQL(true);
	        ResultSet rsGLAccts = clsDatabaseFunctions.openResultSet(
	        	sSQL, 
	        	getServletContext(), 
	        	smedit.getsDBID(),
	        	"MySQL",
	        	this.toString() + ".Edit_Record - User: " + smedit.getUserID()
	        	+ " - " + smedit.getFullUserName()
	        		);
	        
	        while (rsGLAccts.next()){
	        	arrValues.add((String) rsGLAccts.getString(SMTableglaccounts.sAcctID).trim());
	        	arrDescriptions.add((String) rsGLAccts.getString(SMTableglaccounts.sAcctID).trim() + " - " + (String) rsGLAccts.getString(SMTableglaccounts.sDesc).trim());
			}
	        rsGLAccts.close();
		}catch (SQLException ex){
			s += "<BR><FONT COLOR=RED><B>Error [1450321765] reading GL accounts - " + ex.getMessage() + ".</FONT></B><BR>";
		}
		
		//Payable Control Account
        s += clsCreateHTMLTableFormFields.Create_Edit_Form_List_Row(
        		APAccountSet.Paramspayablescontrolacct, 
        		arrValues, 
        		entry.getspayablescontrolacct().replace("\"", "&quot;"),  
        		arrDescriptions, 
        		"Payable Control Account<FONT COLOR=\"RED\">*</FONT>:", 
        		"Select the Payable Control Account."
        		);
	    
        //Purchase Discount Account
        s += clsCreateHTMLTableFormFields.Create_Edit_Form_List_Row(
        		APAccountSet.Paramspurchasediscountacct, 
        		arrValues, 
        		entry.getspurchasediscountacct().replace("\"", "&quot;"),  
        		arrDescriptions, 
        		"Purchase Discount Account<FONT COLOR=\"RED\">*</FONT>:", 
        		"Select the Purchase Discount Account."
        		);
	    
        //Prepayment Account
        s += clsCreateHTMLTableFormFields.Create_Edit_Form_List_Row(
        		APAccountSet.Paramsprepaymentacct, 
        		arrValues, 
        		entry.getsprepaymentacct().replace("\"", "&quot;"),  
        		arrDescriptions, 
        		"Prepayment Account<FONT COLOR=\"RED\">*</FONT>:", 
        		"Select the Prepayment Account."
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