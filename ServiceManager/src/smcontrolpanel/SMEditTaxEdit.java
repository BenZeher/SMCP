package smcontrolpanel;

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
import SMClasses.SMTax;
import SMDataDefinition.SMTableglaccounts;
import SMDataDefinition.SMTabletax;
import ServletUtilities.clsCreateHTMLTableFormFields;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsManageRequestParameters;

public class SMEditTaxEdit extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final String SUBMIT_UPDATE_BUTTON_NAME = "SubmitEditUpdate";
	private static final String SUBMIT_UPDATE_BUTTON_VALUE = "Update " + SMTax.ParamObjectName;
	private static final String REQUIRED_FIELD_FLAG = "<FONT COLOR=RED><B>*</B></FONT>";
	
	public void doPost(HttpServletRequest request,
				HttpServletResponse response)
				throws ServletException, IOException {

		SMTax entry = new SMTax(request);
		SMMasterEditEntry smedit = new SMMasterEditEntry(
				request,
				response,
				getServletContext(),
				SMTax.ParamObjectName,
				SMUtilities.getFullClassName(this.toString()),
				SMUtilities.getFullClassName(this.toString()),
				"smcontrolpanel.SMUserLogin",
				"Go back to user login",
				SMSystemFunctions.SMEditTaxes
		);    
		
		if (!smedit.processSession(getServletContext(), SMSystemFunctions.SMEditTaxes, request)){
			smedit.getPWOut().println("Error in process session: " + smedit.getErrorMessages());
			return;
		}
		
		//Get any object out of the session immediately, so we don't leave it in:
		HttpSession currentSession = smedit.getCurrentSession();
		SMTax entryobjectfromsession = null;
		if (currentSession.getAttribute(SMTax.ParamObjectName) != null){
			entryobjectfromsession = (SMTax) currentSession.getAttribute(SMTax.ParamObjectName);
			currentSession.removeAttribute(SMTax.ParamObjectName);
		}
		
	    //First process if it's a 'delete' from the Selection Screen:
	    if(request.getParameter(SMEditTaxSelection.SUBMIT_DELETE_BUTTON_NAME) != null){
		    if (request.getParameter(SMEditTaxSelection.CONFIRM_DELETE_CHECKBOX_NAME) == null){
				response.sendRedirect(
					"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMEditTaxSelection"
					+ "?" + SMTabletax.lid + "=" + entry.get_slid()
					+ "&Warning=You must check the 'confirming' check box to delete."
				);
				return;
		    }
		    if (entry.get_slid().compareToIgnoreCase("") == 0){
				response.sendRedirect(
					"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMEditTaxSelection"
					+ "?" + SMTabletax.lid + "=" + entry.get_slid()
					+ "&Warning=You must select a " + SMTax.ParamObjectName + " to delete."
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
        					"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMEditTaxSelection"
        					+ "?" + SMTabletax.lid + "=" + entry.get_slid()
        					+ "&Warning=Error deleting " + SMTax.ParamObjectName + " - cannot get connection."
        				);
    				return;
		    	}
		    	try {
					entry.delete(entry.get_slid(), conn);
				} catch (Exception e) {
					clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1547080547]");
    				response.sendRedirect(
        					"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMEditTaxSelection"
        					+ "?" + SMTabletax.lid + "=" + entry.get_slid()
        					+ "&Warning=Error deleting " + SMTax.ParamObjectName + " - " + e.getMessage()
        				);
    				return;
		    	}
		    	clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1547080548]");
				response.sendRedirect(
					"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMEditTaxSelection"
							+ "?" + SMTabletax.lid + "=" + entry.get_slid()
					+ "&Status=Successfully deleted " + SMTax.ParamObjectName + " with ID: " + entry.get_slid()
				);
				return;
			}
	    }
		
	    //If coming from Add button of select screen; set as new record and clear ID
		if(request.getParameter(SMEditTaxSelection.SUBMIT_ADD_BUTTON_NAME) != null){
			entry.set_snewrecord(SMTax.ADDING_NEW_RECORD_PARAM_VALUE_TRUE);
			entry.set_slid("-1");
		}
		
		//If we are coming here for the first time to edit (i.e., it's not a re-submit), then we need to 
		//load the input class from the database if possible:
		if(request.getParameter(SMEditTaxSelection.SUBMIT_EDIT_BUTTON_NAME) != null){
	    	try{
			entry.load(smedit.getsDBID(), getServletContext(), smedit.getUserName());
	    	}catch (Exception e){
				response.sendRedirect(
					"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMEditTaxSelection"
					+ "?" + SMTabletax.lid + "=" + entry.get_slid()
					+ "&Warning=" + e.getMessage()
				);
					return;
	    	}
		}
		
		//NOTE: This page posts back to itself
		//If update, save the entries:
		String sSaveStatus = "";
		if(clsManageRequestParameters.get_Request_Parameter(SMEditTaxEdit.SUBMIT_UPDATE_BUTTON_NAME, request)
			.compareToIgnoreCase(SUBMIT_UPDATE_BUTTON_VALUE) == 0){
			try {
				entry.save(getServletContext(), smedit.getsDBID(), smedit.getUserName());
			} catch (Exception e) {
				//Set attribute before reloading the screen
				smedit.getCurrentSession().setAttribute(SMTax.ParamObjectName, entry);
				//'Resubmit' the screen (without the submit button parameter)
				response.sendRedirect(
						"" + SMUtilities.getURLLinkBase(getServletContext()) + SMUtilities.getFullClassName(this.toString()) + "?"
						+ SMTabletax.lid + "=" + entry.get_slid()
						+ "&" + SMTax.ParamsNewRecord + "=" + entry.get_snewrecord()
						+ "&Warning=" + "Could not save tax - " + e.getMessage()					
				);
		        return;
			}
			sSaveStatus = "Tax '" + entry.get_staxjurisdiction() + ", " + entry.get_staxtype() + "' saved successfully.";
		}
		
		//If this is a 'resubmit', meaning it's being called by this class, then
		//the session will have had an appropriate object in it, and that's what we'll pick up.
		if (entryobjectfromsession != null){
			entry = entryobjectfromsession;
		//But if it's NOT a 'resubmit', meaning this class was called for the first time to 
		//edit OR after a previous successful edit, we'll load the entry fresh from the database:
		}else{
			//If it's not an 'ADD', then try to load the entry from the database:
			if (entry.get_snewrecord().compareToIgnoreCase(SMTax.ADDING_NEW_RECORD_PARAM_VALUE_FALSE) == 0){
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
			smedit.getPWOut().println("Error [1453845705] getting session attribute - " + e1.getMessage());
			return;
		}
		
		//Build page header:
	    String title;
	    String subtitle = "";
	    title = "Edit " + SMTax.ParamObjectName;

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
				SMTax.EDIT_FORM_NAME,
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
			SMTax entry
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
	
	private String getEditHTML(SMMasterEditEntry smedit, SMTax entry) throws Exception	{
		String s = "";
		//Start the table:
		s += "<TABLE BORDER=12 CELLSPACING=2>";		
	
		//Vendor Group:
	    s +="<TR>"
	    + "<TD ALIGN=RIGHT><B>" + "ID:"  + " </B></TD>"
	    + "<TD ALIGN=LEFT>";
	      if(entry.get_snewrecord().compareToIgnoreCase(SMTax.ADDING_NEW_RECORD_PARAM_VALUE_TRUE) == 0){
	         s+= "(NEW)";
	      }else{
	         s+= entry.get_slid();
	      }
	    s+= "</TD>"
	    + "<TD ALIGN=LEFT>" 
	    + " " 
	    + "</TD>"
	    + "</TR>"
	    + "<INPUT TYPE=HIDDEN NAME=\"" + SMTabletax.lid + "\" VALUE='" + entry.get_slid() + "'>"
	    + "<INPUT TYPE=HIDDEN NAME=\"" + SMTax.ParamsNewRecord + "\" VALUE='" + entry.get_snewrecord() + "'>"
	    ;  	

	    String sReadOnly = "readonly ";
	    if (entry.get_snewrecord().compareToIgnoreCase(SMTax.ADDING_NEW_RECORD_PARAM_VALUE_TRUE) == 0){
	    	sReadOnly = "";
	    }
	    
	    //Jurisdiction
        s += "<TR>"
    	  + "<TD ALIGN=RIGHT><B>" + "Jurisdiction" + REQUIRED_FIELD_FLAG + "</FONT>:"  + " </B></TD>"
    	  + "<TD ALIGN=LEFT>"
    	  + "<INPUT TYPE=TEXT NAME=\"" + SMTabletax.staxjurisdiction + "\""
    	  + " VALUE=\"" + entry.get_staxjurisdiction().replace("\"", "&quot;") + "\""
    	  + " MAXLENGTH=" + Integer.toString(SMTabletax.staxjurisdictionLength)
    	  + " STYLE=\"height: 0.25in\""
    	  + " " + sReadOnly
    	  + "></TD>"
    	  + "<TD ALIGN=LEFT><I>" 
    	  + "Name of the tax jurisdiction (state abbreviation, e.g.)" 
    	  + "</I></TD>"
    	  + "</TR>"
    	  ;
        
	    //Tax type
        s += "<TR>"
    	  + "<TD ALIGN=RIGHT><B>" + "Tax type" + REQUIRED_FIELD_FLAG + ":"  + " </B></TD>"
    	  + "<TD ALIGN=LEFT>"
    	  + "<INPUT TYPE=TEXT NAME=\"" + SMTabletax.staxtype + "\""
    	  + " VALUE=\"" + entry.get_staxtype().replace("\"", "&quot;") + "\""
    	  + " MAXLENGTH=" + Integer.toString(SMTabletax.staxtypeLength)
    	  + " STYLE=\"height: 0.25in\""
    	  + " " + sReadOnly
    	  + "></TD>"
    	  + "<TD ALIGN=LEFT><I>" 
    	  + "Typically 'Use', 'Retail sales', 'Exempt', e.g. - something familiar to users to help them choose the correct tax on sales oerders, etc." 
    	  + "</I></TD>"
    	  + "</TR>"
    	  ;
        
	    //Description
        s += "<TR>"
    	  + "<TD ALIGN=RIGHT><B>" + "Description" + REQUIRED_FIELD_FLAG + "</FONT>:"  + " </B></TD>"
    	  + "<TD ALIGN=LEFT>"
    	  + "<INPUT TYPE=TEXT NAME=\"" + SMTabletax.sdescription + "\""
    	  + " VALUE=\"" + entry.get_staxdescription().replace("\"", "&quot;") + "\""
    	  + " MAXLENGTH=" + Integer.toString(SMTabletax.sdescriptionLength)
    	 // + " WIDTH=120"
    	  + " STYLE=\"height: 0.25in; width: 4.00in; \""
    	  + "></TD>"
    	  + "<TD ALIGN=LEFT><I>" 
    	  + "Longer description of this particular tax" 
    	  + "</I></TD>"
    	  + "</TR>"
    	  ;
        
	    //Tax rate
        s += "<TR>"
    	  + "<TD ALIGN=RIGHT><B>" + "Tax rate (percentage)" + REQUIRED_FIELD_FLAG + "</FONT>:"  + " </B></TD>"
    	  + "<TD ALIGN=LEFT>"
    	  + "<INPUT TYPE=TEXT NAME=\"" + SMTabletax.bdtaxrate + "\""
    	  + " VALUE=\"" + entry.get_bdtaxrate().replace("\"", "&quot;") + "\""
    	  + " MAXLENGTH=10"
    	  + " STYLE=\"height: 0.25in\""
    	  + "></TD>"
    	  + "<TD ALIGN=LEFT><I>" 
    	  + "Enter the rate as a percentage amount - a 5.25% tax should be entered as '5.25'" 
    	  + "</I></TD>"
    	  + "</TR>"
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
	    			+ " - "
	    			+ smedit.getFullUserName()
	    			);

	    	while (rsGLAccts.next()){
	    		arrValues.add((String) rsGLAccts.getString(SMTableglaccounts.sAcctID).trim());
	    		arrDescriptions.add((String) rsGLAccts.getString(SMTableglaccounts.sAcctID).trim() + " - " + (String) rsGLAccts.getString(SMTableglaccounts.sDesc).trim());
	    	}
	    	rsGLAccts.close();
	    }catch (SQLException ex){
	    	s += "<BR><FONT COLOR=RED><B>Error [1454012454] reading GL accounts - " + ex.getMessage() + ".</FONT></B><BR>";
	    }
	    
        s += clsCreateHTMLTableFormFields.Create_Edit_Form_List_Row(
    		SMTabletax.sglacct, 
    		arrValues, 
    		entry.get_sglacct().replace("\"", "&quot;"),  
    		arrDescriptions, 
    		"GL Account" + REQUIRED_FIELD_FLAG + ":", 
    		"<I>The GL account associated with this tax</I>"
    	);
	    
		//Active?
	    s += clsCreateHTMLTableFormFields.Create_Edit_Form_Checkbox_Row(
	    	SMTabletax.iactive, 
			Integer.parseInt(entry.get_sactive()), 
			"Active?", 
			"<I>Uncheck to de-activate this tax</I>"
			)
		;

	    //Calculate on purchase or sale?
	    ArrayList <String >sButtonLabels = new ArrayList<String>(0);
	    ArrayList <String >sButtonValues = new ArrayList<String>(0);
	    sButtonLabels.add(SMTabletax.getCalculationTypeDescription(SMTabletax.TAX_CALCULATION_BASED_ON_PURCHASE_COST));
	    sButtonLabels.add(SMTabletax.getCalculationTypeDescription(SMTabletax.TAX_CALCULATION_BASED_ON_SELLING_PRICE));
	    sButtonValues.add(Integer.toString(SMTabletax.TAX_CALCULATION_BASED_ON_PURCHASE_COST));
	    sButtonValues.add(Integer.toString(SMTabletax.TAX_CALCULATION_BASED_ON_SELLING_PRICE));
	    s += clsCreateHTMLTableFormFields.Create_Edit_Form_RadioButton_Input_Row(
	    	SMTabletax.icalculateonpurchaseorsale, 
	    	"Calculate on purchase or sale", 
	    	"<I>Should this tax be calculated on the purchased cost or the selling price?</I>", 
	    	sButtonLabels, 
	    	sButtonValues, 
	    	entry.get_scalculateonpurchaseorsale());
	    
		//Display tax amount on customer invoice?
	    s += clsCreateHTMLTableFormFields.Create_Edit_Form_Checkbox_Row(
	    	SMTabletax.icalculatetaxoncustomerinvoice, 
			Integer.parseInt(entry.get_scalculatetaxoncustomerinvoice()), 
			"Calculate on customer invoice?", 
			"<I>Check to calculate and display this tax on customer invoices</I>"
			)
		;
	    
		//Show in Accounts Payable?
	    s += clsCreateHTMLTableFormFields.Create_Edit_Form_Checkbox_Row(
	    	SMTabletax.ishowinaccountspayable, 
			Integer.parseInt(entry.get_sshowinaccountspayable()), 
			"List this tax in Accounts Payable?", 
			"<I>Check to include this tax in the list when entering vendor's invoices</I>"
			)
		;
	    
		//Show in Order Entry?
	    s += clsCreateHTMLTableFormFields.Create_Edit_Form_Checkbox_Row(
	    	SMTabletax.ishowinorderentry, 
			Integer.parseInt(entry.get_sshowinorderentry()), 
			"List this tax in Order Entry?", 
			"<I>Check to include this tax in the list when entering sales orders</I>"
			)
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