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
import SMDataDefinition.SMTableapvendorremittolocations;
import ServletUtilities.clsCreateHTMLTableFormFields;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsManageRequestParameters;


public class APVendorRemitToLocationEdit extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final String SUBMIT_UPDATE_BUTTON_NAME = "SubmitEditUpdate";
	private static final String SUBMIT_UPDATE_BUTTON_VALUE = "Update " + APVendorRemitToLocation.ParamObjectName;
	private static final String SUBMIT_DELETE_BUTTON_NAME = "SubmitDelete";
	//private static final String SUBMIT_DELETE_BUTTON_VALUE = "Delete " + APVendorRemitToLocation.ParamObjectName;
	private static final String CONFIRM_DELETE_CHECKBOX_NAME = "ConfirmDelete";
	
	public void doPost(HttpServletRequest request,
				HttpServletResponse response)
				throws ServletException, IOException {
		
		APVendorRemitToLocation entry = new APVendorRemitToLocation(request);
		
		SMMasterEditEntry smedit = new SMMasterEditEntry(
				request,
				response,
				getServletContext(),
				entry.getObjectName(),
				SMUtilities.getFullClassName(this.toString()),
				"smap.APVendorRemitToLocationEdit",
				"smcontrolpanel.SMUserLogin",
				"Go back to user login",
				SMSystemFunctions.APEditVendorRemitToLocations
		);    
		
		if (!smedit.processSession(getServletContext(), SMSystemFunctions.APEditVendorRemitToLocations, request)){
			smedit.getPWOut().println("Error in process session: " + smedit.getErrorMessages());
			return;
		}
		
		if(clsManageRequestParameters.get_Request_Parameter(APVendorRemitToLocationSelection.SUBMIT_EDIT_BUTTON_NAME, request)
				.compareToIgnoreCase(APVendorRemitToLocationSelection.SUBMIT_EDIT_BUTTON_VALUE) == 0
				&& request.getParameter(APVendorRemitToLocation.Paramsremittocode).compareToIgnoreCase("") == 0){
			response.sendRedirect(
					"" + SMUtilities.getURLLinkBase(getServletContext()) + "smap.APVendorRemitToLocationSelection"
					+ "?" + APVendorRemitToLocation.Paramsvendoracct + "=" + entry.getsvendoracct()				
					+ "&Warning=There are no locations to edit. Click 'Add New' to add a new location."
				);
				return;
		}

	    //First process if it's a 'delete':
	    if(request.getParameter(SUBMIT_DELETE_BUTTON_NAME) != null){
		    if (request.getParameter(CONFIRM_DELETE_CHECKBOX_NAME) == null){
				response.sendRedirect(
					"" + SMUtilities.getURLLinkBase(getServletContext()) + "smap.APVendorRemitToLocationSelection"
					+ "?" + APVendorRemitToLocation.Paramsvendoracct + "=" + entry.getsvendoracct()
					+ "&Warning=You must check the 'confirming' check box to delete."
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
        			"" + SMUtilities.getURLLinkBase(getServletContext()) + "smap.APVendorRemitToLocationSelection"
        			+ "?" + APVendorRemitToLocation.Paramsvendoracct + "=" + entry.getsvendoracct()
        			+ "&" + APVendorRemitToLocation.Paramsremittocode + "=" + entry.getsremittocode()
        			+ "&Warning=Error deleting Remit to Code - cannot get connection."
        				);
    						return;
		    	}
		    	clsDatabaseFunctions.start_data_transaction(conn);
			    if (!entry.delete(entry.getsremittocode(), conn)){
			    	clsDatabaseFunctions.rollback_data_transaction(conn);
			    	clsDatabaseFunctions.freeConnection(getServletContext(), conn);
    				response.sendRedirect(
    					"" + SMUtilities.getURLLinkBase(getServletContext()) + "smap.APVendorRemitToLocationSelection"
            			+ "?" + APVendorRemitToLocation.Paramsvendoracct + "=" + entry.getsvendoracct()
    					+ "&Warning=Error deleting remit to Code - " + entry.getsremittocode()
    				);
					return;
			    }else{
			    	clsDatabaseFunctions.commit_data_transaction(conn);
			    	clsDatabaseFunctions.freeConnection(getServletContext(), conn);
    				response.sendRedirect(
    					"" + SMUtilities.getURLLinkBase(getServletContext()) + "smap.APVendorRemitToLocationSelection"
    	            	+ "?" + APVendorRemitToLocation.Paramsvendoracct + "=" + entry.getsvendoracct()
    					+ "&Status=Successfully deleted remit to Code " + entry.getsremittocode() + "."
    				);
					return;
			    }
		    }
	    }
		
		
		//NOTE: This page posts back to itself
		//If update, save the entries:
		String sSaveStatus = "";
		if(clsManageRequestParameters.get_Request_Parameter(APVendorRemitToLocationEdit.SUBMIT_UPDATE_BUTTON_NAME, request)
				.compareToIgnoreCase(SUBMIT_UPDATE_BUTTON_VALUE) == 0){

			if (!entry.saveEditableFields(getServletContext(), smedit.getsDBID(), smedit.getUserName(), smedit.getUserID(), smedit.getFullUserName())){
				//Set attribute before reloading the screen
				smedit.getCurrentSession().setAttribute(APVendorRemitToLocation.ParamObjectName, entry);
				//'Resubmit' the screen (without the submit button parameter)
				response.sendRedirect(
					"" + SMUtilities.getURLLinkBase(getServletContext()) + "smap." + "APVendorRemitToLocationEdit" + "?"
				    + APVendorRemitToLocation.Paramsvendoracct + "=" + entry.getsvendoracct()
				    + "&" + APVendorRemitToLocation.Paramsremittocode + "=" + entry.getsremittocode()
					+ "&" + APVendorRemitToLocation.ParamsNewRecord + "=" + entry.getNewRecord()
					+ "&Warning=" + "Could not save remit to Code:\n "					
					+ entry.getErrorMessageString()
				);
		        return;
			}else{
				 sSaveStatus = "Remit To Code '" + entry.getsremittocode() + "' for vendor '" 
				+ entry.getsvendoracct() + "' saved successfully.";
			}
			
		}
		
		//If this is a 'resubmit', meaning it's being called by this class, then
		//the session will have an APVendorRemitToLocation object in it, and that's what we'll pick up.
		HttpSession currentSession = smedit.getCurrentSession();
		if (currentSession.getAttribute(APVendorRemitToLocation.ParamObjectName) != null){
			entry = (APVendorRemitToLocation) currentSession.getAttribute(APVendorRemitToLocation.ParamObjectName);
			currentSession.removeAttribute(APVendorRemitToLocation.ParamObjectName);
		//But if it's NOT a 'resubmit', meaning this class was called for the first time to 
		//edit OR after a previous successful edit, we'll load the entry:
		}else{
			try {
				if(entry.getNewRecord().compareToIgnoreCase("1") != 0){
				entry.load( smedit.getsDBID(), getServletContext(), smedit.getUserName());
				}else{
				entry.setsremittocode("");	
				}
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
			smedit.getPWOut().println("Error [1451336013] getting session attribute - " + e1.getMessage());
			return;
		}
		
		//Build page header:
	    String title;
	    String subtitle = "";
	    title = "Edit Accounts Payable Vendor Remit to Locations";

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
	
		
	    //Print a link back to vendor:
		smedit.getPWOut().println("<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smap.APEditVendorsEdit?" 
				+ APVendor.Paramsvendoracct + "=" + entry.getsvendoracct()
				+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + smedit.getsDBID()
				+ "\">Return to Edit Vendor</A><BR><BR>");
		
	try{ 
		createEditPage(
				getEditHTML(smedit, entry),
				APVendorRemitToLocation.FORM_NAME,
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
			APVendorRemitToLocation entry
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
	
	private String getEditHTML(SMMasterEditEntry smedit, APVendorRemitToLocation entry) throws Exception	{
		String s = "";
		//Start the table:
		s += "<TABLE BORDER=12 CELLSPACING=2>";		
	//    s += "<INPUT TYPE=HIDDEN NAME=\"" + APVendorRemitToLocation.ParamsNewRecord+ "\" VALUE='" + entry.getNewRecord() + "'>"; 
	    s += "<INPUT TYPE=HIDDEN NAME=\"" + APVendorRemitToLocation.Paramsvendoracct+ "\" VALUE='" + entry.getsvendoracct() + "'>"; 
		
	    //Vendor:
	    s +="<TR>"
	    + "<TD ALIGN=RIGHT><B>" + "Vendor:"  + " </B></TD>"
	    + "<TD ALIGN=LEFT>";
	    s += entry.getsvendoracct();
	    s+= "</TD>"
	    + "<TD ALIGN=LEFT>" 
	    + " " 
	    + "</TD>"
	    + "</TR>";

	    
        //Remit to code:
        s += clsCreateHTMLTableFormFields.Create_Edit_Form_Text_Input_Row(       		
        		APVendorRemitToLocation.Paramsremittocode,
        		entry.getsremittocode(),
        		SMTableapvendorremittolocations.sremittocodelength,
        		"<B>Remit to code:<B>",
        		"Enter a unique remit to code for this vendor.",
        		Integer.toString(SMTableapvendorremittolocations.sremittocodelength)
        	); 	

        //Name:
        s += clsCreateHTMLTableFormFields.Create_Edit_Form_Text_Input_Row(
        		APVendorRemitToLocation.Paramsremittoname,
        		entry.getsremittoname(),
        		SMTableapvendorremittolocations.sremittonamelength,
        		"<B>Name:<B>",
        		"",
        		"30"
        	);

		//Active?
	    int iTrueOrFalse = 0;
	    if (entry.getiactive().compareToIgnoreCase("1") == 0){
	    	iTrueOrFalse = 1;
	    }else{
	    	iTrueOrFalse = 0;
	    }
	    s += clsCreateHTMLTableFormFields.Create_Edit_Form_Checkbox_Row(
	    		APVendorRemitToLocation.Paramiactive, 
	    		iTrueOrFalse, 
	    		"Active Code?", 
	    		"Uncheck this to make the vendor remit to location inactive"
			);
        
        //Address line 1:
        s += clsCreateHTMLTableFormFields.Create_Edit_Form_Text_Input_Row(    		
        		APVendorRemitToLocation.Paramsaddressline1,
        		entry.getsaddressline1(),
        		SMTableapvendorremittolocations.saddressline1length,
        		"Address Line 1:",
        		"First line of billing address",
        		"40"
        	);
        
        //Address line 2:
        s += clsCreateHTMLTableFormFields.Create_Edit_Form_Text_Input_Row(
        		APVendorRemitToLocation.Paramsaddressline2,
        		entry.getsaddressline2(),
        		SMTableapvendorremittolocations.saddressline2length,
        		"Address Line 2:",
        		"Second line of billing address",
        		"40"      	
        	);
        
        //Address line 3:
        s += clsCreateHTMLTableFormFields.Create_Edit_Form_Text_Input_Row(
        		APVendorRemitToLocation.Paramsaddressline3,
        		entry.getsaddressline3(),
        		SMTableapvendorremittolocations.saddressline3length,
        		"Address Line 3:",
        		"Third line of billing address",
        		"40"       	
        	);
        
        //Address line 4:
        s += clsCreateHTMLTableFormFields.Create_Edit_Form_Text_Input_Row(
        		APVendorRemitToLocation.Paramsaddressline4,
        		entry.getsaddressline4(),
        		SMTableapvendorremittolocations.saddressline4length,
        		"Address Line 4:",
        		"Fourth line of billing address",
        		"40"       		
        	);
        
        //City
        s += clsCreateHTMLTableFormFields.Create_Edit_Form_Text_Input_Row(      		
        		APVendorRemitToLocation.Paramscity,
        		entry.getscity(),
        		SMTableapvendorremittolocations.scitylength,
        		"City:",
        		"",
        		"40"       	
        	);
        
        //State
        s += clsCreateHTMLTableFormFields.Create_Edit_Form_Text_Input_Row(       		
        		APVendorRemitToLocation.Paramsstate,
        		entry.getsstate(),
        		SMTableapvendorremittolocations.sstatelength,
        		"State:",
        		"",
        		"40"      		
        	);
        
        //Postal Code:
        s += clsCreateHTMLTableFormFields.Create_Edit_Form_Text_Input_Row(      		
        		APVendorRemitToLocation.Paramspostalcode,
        		entry.getspostalcode(),
        		SMTableapvendorremittolocations.spostalcodelength,
        		"Postal Code:",
        		"(No punctuation or spaces)",
        		"40"    
        	);
        
        //Country:
        s += clsCreateHTMLTableFormFields.Create_Edit_Form_Text_Input_Row(        		
        		APVendorRemitToLocation.Paramscountry,
        		entry.getscountry(),
        		SMTableapvendorremittolocations.scountrylength,
        		"Country:",
        		"",
        		"40"    		
        	);
        
        //Contact Name:
        s += clsCreateHTMLTableFormFields.Create_Edit_Form_Text_Input_Row(       		
        		APVendorRemitToLocation.Paramscontactname,
        		entry.getscontactname(),
        		SMTableapvendorremittolocations.scontactnamelength,
        		"Contact Name:",
        		"",
        		"40"      		
        	);
        
        //Phone Number:
        s += clsCreateHTMLTableFormFields.Create_Edit_Form_Text_Input_Row(        		
        		APVendorRemitToLocation.Paramsphonenumber,
        		entry.getsphonenumber(),
        		SMTableapvendorremittolocations.sphonenumberlength,
        		"Phone Number:",
        		"(No punctuation or spaces)",
        		"40"       		
        	);
        
        //Fax Number:
        s += clsCreateHTMLTableFormFields.Create_Edit_Form_Text_Input_Row(       	
        		APVendorRemitToLocation.Paramsfaxnumber,
        		entry.getsfaxnumber(),
        		SMTableapvendorremittolocations.sfaxnumberlength,
        		"Fax Number:",
        		"(No punctuation or spaces)",
        		"40"        		
        	);
        
        //Web Address:
        s += clsCreateHTMLTableFormFields.Create_Edit_Form_Text_Input_Row(       		
        		APVendorRemitToLocation.Paramswebaddress,
        		entry.getswebaddress(),
        		SMTableapvendorremittolocations.swebaddresslength,
        		"Web Address:",
        		"Example: www.somewebsite.com",
        		"40"     		
        	);
        
        //Email Address:
        s += clsCreateHTMLTableFormFields.Create_Edit_Form_Text_Input_Row(       		
        		APVendorRemitToLocation.Paramsemailaddress,
        		entry.getsemailaddress(),
        		SMTableapvendorremittolocations.semailaddresslength,
        		"Email Address:",
        		"Example: someemail@domain.com",
        		"40"
        	);
		
        s += "</TABLE>";
        s += "<P><INPUT TYPE=SUBMIT NAME='" + SUBMIT_UPDATE_BUTTON_NAME 
        	+ "' VALUE='" + SUBMIT_UPDATE_BUTTON_VALUE + "' STYLE='height: 0.24in'>&nbsp";
        
/*
       if(entry.getNewRecord().compareToIgnoreCase("1") != 0){
    	   s += "<INPUT TYPE=SUBMIT NAME='" + SUBMIT_DELETE_BUTTON_NAME 
				+ "' VALUE='" + SUBMIT_DELETE_BUTTON_VALUE + "' STYLE='height: 0.24in'>";
		
    	   s += "  Check to confirm deletion: <INPUT TYPE=CHECKBOX NAME=\"" + CONFIRM_DELETE_CHECKBOX_NAME + "\">";

        }
*/
		return s;
	}
	
	public void doGet(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
		
		doPost(request, response);
	}
}