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
import SMDataDefinition.SMTableap1099cprscodes;
import SMDataDefinition.SMTableapaccountsets;
import SMDataDefinition.SMTableapdistributioncodes;
import SMDataDefinition.SMTableapvendorgroups;
import SMDataDefinition.SMTablebkbanks;
import SMDataDefinition.SMTableglaccounts;
import ServletUtilities.clsCreateHTMLTableFormFields;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsManageRequestParameters;


public class APVendorGroupEdit extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final String SUBMIT_UPDATE_BUTTON_NAME = "SubmitEditUpdate";
	private static final String SUBMIT_UPDATE_BUTTON_VALUE = "Update " + APVendorGroup.ParamObjectName;
	
	public void doPost(HttpServletRequest request,
				HttpServletResponse response)
				throws ServletException, IOException {
		

		APVendorGroup entry = new APVendorGroup(request);
		
		SMMasterEditEntry smedit = new SMMasterEditEntry(
				request,
				response,
				getServletContext(),
				entry.getObjectName(),
				SMUtilities.getFullClassName(this.toString()),
				"smap.APVendorGroupEdit",
				"smcontrolpanel.SMUserLogin",
				"Go back to user login",
				SMSystemFunctions.APEditVendorGroups
		);    
		
		if (!smedit.processSession(getServletContext(), SMSystemFunctions.APEditVendorGroups, request)){
			smedit.getPWOut().println("Error in process session: " + smedit.getErrorMessages());
			return;
		}
		
	    //First process if it's a 'delete' from the Selection Screen:
	    if(request.getParameter(APVendorGroupSelection.SUBMIT_DELETE_BUTTON_NAME) != null){
		    if (request.getParameter(APVendorGroupSelection.CONFIRM_DELETE_CHECKBOX_NAME) == null){
				response.sendRedirect(
					"" + SMUtilities.getURLLinkBase(getServletContext()) + "smap.APVendorGroupSelection"
					+ "?" + APVendorGroup.Paramlid + "=" + entry.getlid()
					+ "&Warning=You must check the 'confirming' check box to delete."
				);
				return;
		    }
		    if (entry.getlid().compareToIgnoreCase("") == 0){
				response.sendRedirect(
					"" + SMUtilities.getURLLinkBase(getServletContext()) + "smap.APVendorGroupSelection"
					+ "?" + APVendorGroup.Paramlid + "=" + entry.getlid()
					+ "&Warning=You must select a vendor group to delete."
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
        					"" + SMUtilities.getURLLinkBase(getServletContext()) + "smap.APVendorGroupSelection"
        					+ "?" + APVendorGroup.Paramlid + "=" + entry.getlid()
        					+ "&Warning=Error deleting vendor group - cannot get connection."
        				);
    						return;
		    	}
		    	clsDatabaseFunctions.start_data_transaction(conn);
			    if (!entry.delete(entry.getlid(), conn)){
			    	clsDatabaseFunctions.rollback_data_transaction(conn);
			    	clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1547059494]");
    				response.sendRedirect(
    					"" + SMUtilities.getURLLinkBase(getServletContext()) + "smap.APVendorGroupSelection"
    					+ "?" + APVendorGroup.Paramlid + "=" + entry.getlid()
    					+ "&Warning=Error deleting vendor group - " + entry.getlid()
    				);
					return;
			    }else{
			    	clsDatabaseFunctions.commit_data_transaction(conn);
			    	clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1547059495]");
    				response.sendRedirect(
    					"" + SMUtilities.getURLLinkBase(getServletContext()) + "smap.APVendorGroupSelection"
    					+ "?" + APVendorGroup.Paramlid + "=" + entry.getlid()
    					+ "&Status=Successfully deleted vendor group " + entry.getlid() + "."
    				);
					return;
			    }
		    }
	    }
		
	    //If coming from Add button of select screen; set as new record and clear ID
		if(request.getParameter(APVendorGroupSelection.SUBMIT_ADD_BUTTON_NAME) != null){
			entry.setNewRecord("1");
			entry.setlid("-1");
		}
		
		//If we are coming here for the first time to edit (i.e., it's not a re-submit), then we need to 
		//load the input class from the database if possible:
		if(request.getParameter(APVendorGroupSelection.SUBMIT_EDIT_BUTTON_NAME) != null){
	    	try{
			entry.load(smedit.getsDBID(), getServletContext(), smedit.getUserName());
	    	}catch (Exception e){
				response.sendRedirect(
					"" + SMUtilities.getURLLinkBase(getServletContext()) + "smap.APVendorGroupSelection"
					+ "?" + APVendorGroup.Paramlid + "=" + entry.getlid()
					+ "&Warning=" + e.getMessage()
				);
					return;
	    	}
	    	
		}
		
		//NOTE: This page posts back to itself
		//If update, save the entries:
		String sSaveStatus = "";
		if(clsManageRequestParameters.get_Request_Parameter(APVendorGroupEdit.SUBMIT_UPDATE_BUTTON_NAME, request)
				.compareToIgnoreCase(SUBMIT_UPDATE_BUTTON_VALUE) == 0){

			if (!entry.saveEditableFields(getServletContext(), smedit.getsDBID(), smedit.getUserID(), smedit.getFullUserName())){
				//Set attribute before reloading the screen
				smedit.getCurrentSession().setAttribute(APVendorGroup.ParamObjectName, entry);
				//'Resubmit' the screen (without the submit button parameter)
				response.sendRedirect(
						"" + SMUtilities.getURLLinkBase(getServletContext()) + "smap." + "APVendorGroupEdit" + "?"
						+ APVendorGroup.Paramlid + "=" + entry.getlid()
						+ "&" + APVendorGroup.ParamsNewRecord + "=" + entry.getNewRecord()
						+ "&Warning=" + "Could not save vendor group:\n "					
						+ entry.getErrorMessageString()
				);
		        return;
			}else{
				 sSaveStatus = "Vendor group '" + entry.getlid() + "' saved successfully.";
			}
			
		}
		
		//If this is a 'resubmit', meaning it's being called by this class, then
		//the session will have an APVendorGroup object in it, and that's what we'll pick up.
		HttpSession currentSession = smedit.getCurrentSession();
		if (currentSession.getAttribute(APVendorGroup.ParamObjectName) != null){
			entry = (APVendorGroup) currentSession.getAttribute(APVendorGroup.ParamObjectName);
			currentSession.removeAttribute(APVendorGroup.ParamObjectName);
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
			smedit.getPWOut().println("Error [1445280222] getting session attribute - " + e1.getMessage());
			return;
		}
		
		//Build page header:
	    String title;
	    String subtitle = "";
	    title = "Edit Accounts Payable Vendor Groups";

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
				APVendorGroup.FORM_NAME,
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
			APVendorGroup entry
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
	
	private String getEditHTML(SMMasterEditEntry smedit, APVendorGroup entry) throws Exception	{
		String s = "";
		String sInactive = "";
		//Start the table:
		s += "<TABLE BORDER=12 CELLSPACING=2>";		
	
		//Vendor Group:
	    s +="<TR>"
	    + "<TD ALIGN=RIGHT><B>" + "Vendor Group ID:"  + " </B></TD>"
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
	    + "<INPUT TYPE=HIDDEN NAME=\"" + APVendorGroup.Paramlid+ "\" VALUE='" + entry.getlid() + "'>"
	    + "<INPUT TYPE=HIDDEN NAME=\"" + APVendorGroup.ParamsNewRecord+ "\" VALUE='" + entry.getNewRecord() + "'>"
	    ;  	

	    //Name:
        s += "<TR>"
    	  + "<TD ALIGN=RIGHT><B>" + "Group Name<FONT COLOR=\"RED\">*</FONT>:"  + " </B></TD>"
    	  + "<TD ALIGN=LEFT>"
    	  + "<INPUT TYPE=TEXT NAME=\"" + APVendorGroup.Paramsgroupid + "\""
    	  + " VALUE=\"" + entry.getsgroupid().replace("\"", "&quot;") + "\""
    	  + " MAXLENGTH=" + Integer.toString(SMTableapvendorgroups.sgroupidlength)
    	  + " STYLE=\"height: 0.25in\""
    	  + "></TD>"
    	  + "<TD ALIGN=LEFT>" 
    	  + "Every vendor group must have a unique name. " 
    	  + "</TD>"
    	  + "</TR>"
    	  ;
	    
        //Description:
        s += "<TR>"
    	  + "<TD ALIGN=RIGHT><B>" + "Description<FONT COLOR=\"RED\">*</FONT>:"  + " </B></TD>"
    	  + "<TD ALIGN=LEFT>"
    	  + "<INPUT TYPE=TEXT NAME=\"" + APVendorGroup.Paramsdescription + "\""
    	  + " VALUE=\"" + entry.getsdescription().replace("\"", "&quot;") + "\""
    	  + " SIZE=" + Integer.toString(SMTableapvendorgroups.sdescriptionlength)
    	  + " MAXLENGTH=" + Integer.toString(SMTableapvendorgroups.sdescriptionlength)
    	  + " STYLE=\"height: 0.25in\""
    	  + "></TD>"
    	  + "<TD ALIGN=LEFT>" 
    	  + "Short description of this vendor group. " 
    	  + "</TD>"
    	  + "</TR>"
    	  ;
	      
        //Terms Code:
        s += "<TR>"
    	  + "<TD ALIGN=RIGHT><B>" + "Terms Code:"  + " </B></TD>"
    	  + "<TD ALIGN=LEFT>"
    	  + "<INPUT TYPE=TEXT NAME=\"" + APVendorGroup.Paramstermscode + "\""
    	  + " VALUE=\"" + entry.getstermscode().replace("\"", "&quot;") + "\""
    	  + " MAXLENGTH=" + Integer.toString(SMTableapvendorgroups.stermscodelength)
    	  + " STYLE=\"height: 0.25in\""
    	  + "></TD>"
    	  + "<TD ALIGN=LEFT>" 
    	  + "Enter a terms code for vendors in this group." 
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
	    	APVendorGroup.Paramiactive, 
			iTrueOrFalse, 
			"Active vendor group?", 
			"Uncheck to de-activate this vendor group."
			)
		;	    
        
		//Print separate checks
	    iTrueOrFalse = 0;
	    if (entry.getiprintseparatechecks().compareToIgnoreCase("1") == 0){
	    	iTrueOrFalse = 1;
	    }else{
	    	iTrueOrFalse = 0;
	    }
	    s += clsCreateHTMLTableFormFields.Create_Edit_Form_Checkbox_Row(
	    	APVendorGroup.Paramiprintseparatechecks, 
			iTrueOrFalse, 
			"Print separate checks?", 
			"Uncheck to de-activate printing separate checks for vendors in this group."
			)
		;    

	    ArrayList<String> arrValues = new ArrayList<String>(0);
        ArrayList<String> arrDescriptions = new ArrayList<String>(0);
        //Load AP account sets
        arrValues.add("0");
        arrDescriptions.add("-- Select Account Set --");

			//Get the record to edit:
	       String sSQL = "SELECT * FROM " + SMTableapaccountsets.TableName
	        		+ " ORDER BY " + SMTableapaccountsets.sacctsetname;
	       try{
	        ResultSet rsAcctSets = clsDatabaseFunctions.openResultSet(
	        	sSQL, 
	        	getServletContext(), 
	        	smedit.getsDBID(),
	        	"MySQL",
	        	this.toString() + ".Edit_Record - User: " 
	        	+ smedit.getUserID()
	        	+ " - "
	        	+ smedit.getFullUserName()
	        		);
	        
	        while (rsAcctSets.next()){
	        	arrValues.add((String) rsAcctSets.getString(SMTableapaccountsets.lid).trim());
	        	sInactive = "";
	        	if(rsAcctSets.getInt(SMTableapaccountsets.iactive) == 0){
	        		sInactive = "(Inactive)";
	        	}
	        	arrDescriptions.add((String) rsAcctSets.getString(SMTableapaccountsets.sacctsetname).trim() 
	        			+ " - " + (String) rsAcctSets.getString(SMTableapaccountsets.sdescription).trim() 
	        			+ " " + sInactive);
			}
	        rsAcctSets.close();
		}catch (SQLException ex){
			s += "<BR><FONT COLOR=RED><B>Error [1450450650] reading account sets - " + ex.getMessage() + ".</FONT></B><BR>";
		}
      //Account sets
        s += clsCreateHTMLTableFormFields.Create_Edit_Form_List_Row(
        		APVendorGroup.Paramiapaccountset, 
        		arrValues, 
        		entry.getiapaccountset().replace("\"", "&quot;"),  
        		arrDescriptions, 
        		"Account Set:", 
        		"Default General ledger associated with vendor's transactions."
        		);
      
        //Load Banks
         sSQL = "SELECT"
    			+ " " + SMTablebkbanks.lid
    			+ ", " + SMTablebkbanks.saccountname
    			+ " FROM " + SMTablebkbanks.TableName
    			+ " ORDER BY " + SMTablebkbanks.lid 
    		;
    	//First, add a bank account so we can be sure the user chose one:
        arrValues.clear();
        arrDescriptions.clear();
        arrValues.add("0");
        arrDescriptions.add("-- Select bank account --");
    				
    		try {
    			ResultSet rs = clsDatabaseFunctions.openResultSet(sSQL, getServletContext(),
    					smedit.getsDBID(), "MySQL", SMUtilities.getFullClassName(this.toString())
    					+ ".getEditHTML - user: " + smedit.getUserID()
    					+ " - "
    					+ smedit.getFullUserName()
    					);
    			while (rs.next()) {
    				arrValues.add(rs.getString(SMTablebkbanks.lid));
    				arrDescriptions.add(rs.getString(SMTablebkbanks.saccountname)
    				);
    			}
    			rs.close();
    		} catch (SQLException e) {
    			System.out.println("Error reading bank accounts - " + e.getMessage());
    			s += "<B>Error reading bank account codes.</B><BR>";
    		}			
    		s += clsCreateHTMLTableFormFields.Create_Edit_Form_List_Row(
    				APVendor.Paramibankcode, 
    				arrValues, 
    				entry.getibankcode(), 
    				arrDescriptions, 
    				"Bank Account:", 
    				"Bank from which checks for this vendor will be drawn."
    			);
	    
	    //Load distribution codes used for distribution
        arrValues.clear();
        arrDescriptions.clear();
        arrValues.add("0");
        arrDescriptions.add("-- Select Distribution Code --");
        try{
			//Get the record to edit:
	        sSQL ="SELECT * FROM " + SMTableapdistributioncodes.TableName;
	        ResultSet rsDisCode = clsDatabaseFunctions.openResultSet(
	        	sSQL, 
	        	getServletContext(), 
	        	smedit.getsDBID(),
	        	"MySQL",
	        	this.toString() + ".Edit_Record - User: " + smedit.getUserID()
	        	+ " - "
	        	+ smedit.getFullUserName()
	        		);
	        while (rsDisCode.next()){
	        	arrValues.add((String) rsDisCode.getString(SMTableapdistributioncodes.lid).trim());
	        	arrDescriptions.add((String) rsDisCode.getString(SMTableapdistributioncodes.sdistcodename).trim() + " - " + (String) rsDisCode.getString(SMTableapdistributioncodes.sdescription).trim());
			}
	        rsDisCode.close();
		}catch (SQLException ex){
			s += "<BR><FONT COLOR=RED><B>Error [1450452212] reading distribution codes - " + ex.getMessage() + ".</FONT></B><BR>";
		}
		
		//distribution codes used for distribution
        s += clsCreateHTMLTableFormFields.Create_Edit_Form_List_Row(
        		APVendorGroup.Paramidistributioncodeusedfordistribution, 
        		arrValues, 
        		entry.getidistributioncodeusedfordistribution().replace("\"", "&quot;"),  
        		arrDescriptions, 
        		"Distribution code:", 
        		"Select distribution codes used for distribution."
        		);
        
	   
	    //Load the GL Accounts for GL account used for distribution:
        arrValues.clear();
        arrDescriptions.clear();
        arrValues.add("0");
        arrDescriptions.add("-- Select GL Account --");
        try{
			//Get the record to edit:
	       sSQL = "SELECT * FROM " + SMTableglaccounts.TableName 
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
			s += "<BR><FONT COLOR=RED><B>Error [1450450651] reading GL accounts - " + ex.getMessage() + ".</FONT></B><BR>";
		}
		
		//GL account used for distribution

        s += clsCreateHTMLTableFormFields.Create_Edit_Form_List_Row(
        		APVendorGroup.Paramsglacctusedfordistribution, 
        		arrValues, 
        		entry.getsglacctusedfordistribution().replace("\"", "&quot;"),  
        		arrDescriptions, 
        		"GL Account:", 
        		"Select GL account used for distribution."
        		);

      //Distributed by:
		s += " <TR><TD ALIGN=RIGHT><B>Distributed by</B>:</TD>"
			+ "<TD><SELECT NAME = \"" + APVendorGroup.Paramidistributeby + "\">";	
		
		for (int i = 0; i < SMTableapvendorgroups.NUMBER_OF_DISTRIBUTE_BY_TYPES; i++){
			s += "<OPTION";
			if (entry.getidistributeby().compareToIgnoreCase(
					Long.toString(i)) == 0){
				s+= " selected=yes ";
			}
			s += " VALUE=\"" + Integer.toString(i) + "\">" 
				+ SMTableapvendorgroups.getDistributeByType(i) + "</OPTION>";
		}
		s += "</SELECT></TD>"
		 + "<TD>Distributed by</TD></TD>"
		 + "</TR>";
		
	     //Tax reporting type:
			s += " <TR><TD ALIGN=RIGHT><B>Tax Reporting Type</B>:</TD>"
				+ "<TD><SELECT NAME = \"" + APVendorGroup.Paramitaxreportingtype + "\">";	
			
			for (int i = 0; i < SMTableapvendorgroups.NUMBER_OF_TAX_REPORTING_TYPES; i++){
				s += "<OPTION";
				if (entry.getitaxreportingtype().compareToIgnoreCase(
						Long.toString(i)) == 0){
					s+= " selected=yes ";
				}
				s += " VALUE=\"" + Integer.toString(i) + "\">" 
					+ SMTableapvendorgroups.getTaxReportingType(i) + "</OPTION>";
			}
			s += "</SELECT></TD>"
			 + "<TD>Tax reporting type</TD></TD>"
			 + "</TR>";
			
		//Load 1099 CPRS codes
	     arrValues.clear();
	     arrDescriptions.clear();
	     arrValues.add("");
	     arrDescriptions.add("-- Select 1099 CPRS code --");
	        try{
				//Get the record to edit:
		         sSQL = "SELECT * FROM " + SMTableap1099cprscodes.TableName;
		        ResultSet rs1099Code = clsDatabaseFunctions.openResultSet(
		        	sSQL, 
		        	getServletContext(), 
		        	smedit.getsDBID(),
		        	"MySQL",
		        	this.toString() + ".Edit_Record - User: " + smedit.getUserID()
		        	+ " - "
		        	+ smedit.getFullUserName()
		        		);
		        
		        while (rs1099Code.next()){
		        	arrValues.add((String) rs1099Code.getString(SMTableap1099cprscodes.lid).trim());
		        	 sInactive = "";
		        	if(rs1099Code.getInt(SMTableap1099cprscodes.iactive) == 0){
		        		sInactive = "(Inactive)";
		        	}
		        	arrDescriptions.add((String) rs1099Code.getString(SMTableap1099cprscodes.sclassid).trim() 
		        			+ " - " + (String) rs1099Code.getString(SMTableap1099cprscodes.sdescription).trim()
		        			+ " " + sInactive);
				}
		        rs1099Code.close();
			}catch (SQLException ex){
				s += "<BR><FONT COLOR=RED><B>Error [1450450661] reading 1099 codes - " + ex.getMessage() + ".</FONT></B><BR>";
			}
			
			//GL account used for distribution
	        s += clsCreateHTMLTableFormFields.Create_Edit_Form_List_Row(
	        		APVendorGroup.Parami1099CPRScode, 
	        		arrValues, 
	        		entry.geti1099CPRScode().replace("\"", "&quot;"),  
	        		arrDescriptions, 
	        		"1099 CPRS Code:", 
	        		"Select a 1099 CPRS Code."
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