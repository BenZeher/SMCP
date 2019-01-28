package smic;

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

import smar.ARUtilities;
import smcontrolpanel.SMAuthenticate;
import smcontrolpanel.SMSystemFunctions;
import smcontrolpanel.SMUtilities;
import ConnectionPool.WebContextParameters;
import SMDataDefinition.SMTableglaccounts;
import SMDataDefinition.SMTableicaccountsets;
import ServletUtilities.clsCreateHTMLTableFormFields;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsManageRequestParameters;

public class ICEditAccountSetsEdit extends HttpServlet {

	private static final long serialVersionUID = 1L;
	private static final String sAccountSetsObjectName = "Account Set";
	private static String sEditAccountSetsCalledClassName = "ICEditAccountSetsAction";

	public void doPost(HttpServletRequest request,
				HttpServletResponse response)
				throws ServletException, IOException {

		PrintWriter out = response.getWriter();
		if (!SMAuthenticate.authenticateSMCPCredentials(
				request, 
				response, 
				getServletContext(), 
				SMSystemFunctions.ICEditAccountSets))
		{
			return;
		}
		 
	    //Get the session info:
	    HttpSession CurrentSession = request.getSession(true);
	    String sDBID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_DATABASE_ID);
	    String sUserID = (String)CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERID);
	    String sUserFullName = (String)CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERFIRSTNAME) + " "
	    				+ (String)CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERLASTNAME);
	    String sCompanyName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_COMPANYNAME);
		//Load the input class from the request object - if it's a 'resubmit', then this will contain
	    //all the values typed from the previous screen.  If it's a 'first time' edit, then this will only
	    //contain the account set number
		ICAccountSet acctset = new ICAccountSet("");
		acctset.loadFromHTTPRequest(request);
	    //First process if it's a 'delete':
	    if(request.getParameter("SubmitDelete") != null){
		    if (request.getParameter("ConfirmDelete") == null){
				response.sendRedirect(
					"" + SMUtilities.getURLLinkBase(getServletContext()) + "smic.ICEditAccountSetsSelection"
					+ "?" + ICAccountSet.ParamAccountSetCode + "=" + acctset.getAccountSetCode()
					+ "&Warning=You must check the 'confirming' check box to delete."
					+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
				);
				return;
		    }
		    if (acctset.getAccountSetCode().compareToIgnoreCase("") == 0){
				response.sendRedirect(
					"" + SMUtilities.getURLLinkBase(getServletContext()) + "smic.ICEditAccountSetsSelection"
					+ "?" + ICAccountSet.ParamAccountSetCode + "=" + acctset.getAccountSetCode()
					+ "&Warning=You must enter an account set to delete."
					+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
				);
				return;
		    }
		    
		    else{
		    	//Need a connection for the 'delete':
		    	Connection conn = clsDatabaseFunctions.getConnection(
		    		getServletContext(), 
		    		sDBID,
		    		"MySQL",
		    		this.toString() + ".doPost - User: " + sUserID
		    		+ " - "
		    		+ sUserFullName
		    			);
		    	if(conn == null){
    				response.sendRedirect(
        					"" + SMUtilities.getURLLinkBase(getServletContext()) + "smic.ICEditAccountSetsSelection"
        					+ "?" + ICAccountSet.ParamAccountSetCode + "=" + acctset.getAccountSetCode()
        					+ "&Warning=Error deleting account set - cannot get connection."
        					+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
        				);
    						return;
		    	}
		    	clsDatabaseFunctions.start_data_transaction(conn);
			    if (!acctset.delete(acctset.getAccountSetCode(), conn)){
			    	clsDatabaseFunctions.rollback_data_transaction(conn);
			    	clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1547080817]");
    				response.sendRedirect(
    					"" + SMUtilities.getURLLinkBase(getServletContext()) + "smic.ICEditAccountSetsSelection"
    					+ "?" + ICAccountSet.ParamAccountSetCode + "=" + acctset.getAccountSetCode()
    					+ "&Warning=Error deleting account set - " + acctset.getAccountSetCode()
    					+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
    				);
					return;
			    }else{
			    	clsDatabaseFunctions.commit_data_transaction(conn);
			    	clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1547080818]");
    				response.sendRedirect(
    					"" + SMUtilities.getURLLinkBase(getServletContext()) + "smic.ICEditAccountSetsSelection"
    					+ "?" + ICAccountSet.ParamAccountSetCode + "=" + acctset.getAccountSetCode()
    					+ "&Status=Successfully deleted account set " + acctset.getAccountSetCode() + "."
    					+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
    				);
					return;
			    }
		    }
	    }
	    
		String title = "";
		String subtitle = "";
		
		if(request.getParameter("SubmitAdd") != null){
			acctset.setAccountSetCode("");
			acctset.setNewRecord("1");
		}
		//If we are coming here for the first time to edit (i.e., it's not a re-submit), then we need to 
		//load the input class from the database if possible:
		if(request.getParameter("SubmitEdit") != null){
	    	if(!acctset.load(getServletContext(), sDBID)){
				response.sendRedirect(
					"" + SMUtilities.getURLLinkBase(getServletContext()) + "smic.ICEditAccountSetsSelection"
					+ "?" + ICAccountSet.ParamAccountSetCode + "=" + acctset.getAccountSetCode()
					+ "&Warning=Could not load account set " + acctset.getAccountSetCode() + "."
					+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
				);
					return;
	    	}
		}
		
		//If we are coming here from the same screen to edit a different account set, then we also need to 
		//load the input class from the database if possible:
		if(request.getParameter("SubmitEditDifferent") != null){
	    	if(!acctset.load(getServletContext(), sDBID)){
				response.sendRedirect(
					"" + SMUtilities.getURLLinkBase(getServletContext()) + "smic.ICEditAccountSetsSelection"
					+ "?" + ICAccountSet.ParamAccountSetCode + "=" + acctset.getAccountSetCode()
					+ "&Warning=Could not load account set " + acctset.getAccountSetCode() + "."
					+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
				);
					return;
	    	}
		}
		
		//In any other case, such as the possibility that this is a 'resubmit', we need to edit the account set:
    	title = "Edit " + sAccountSetsObjectName;
	    subtitle = "";
	    out.println(SMUtilities.SMCPTitleSubBGColor(title, subtitle, SMUtilities.getInitBackGroundColor(getServletContext(), sDBID), sCompanyName));
	    out.println(SMUtilities.getDatePickerIncludeString(getServletContext()));
	    
		//If there is a warning from trying to input previously, print it here:
		String sWarning = clsManageRequestParameters.get_Request_Parameter("Warning", request);
	    if (! sWarning.equalsIgnoreCase("")){
			out.println("<B><FONT COLOR=\"RED\">WARNING: " + sWarning + "</FONT></B><BR>");
		}
	    
	    out.println("<TABLE BORDER=0 WIDTH=100%>");
	    
	    //Print a link to the first page after login:
	    out.println("<TR>");
	    out.println("<TD>");
	    out.println("<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMUserLogin?" 
				+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
				+ "\">Return to user login</A><BR>");
	    out.println("<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smic.ICMainMenu?" 
				+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
				+ "\">Return to Inventory Control Main Menu</A><BR>");
	    out.println("<A HREF=\"" + WebContextParameters.getdocumentationpageURL(getServletContext()) + "#" + Long.toString(SMSystemFunctions.ICEditAccountSets) 
	    		+ "\">Summary</A>");
	    out.println("</TD>");
	    
	    //Create a form for editing a different account set:
	    out.println("<TD ALIGN=RIGHT>");
	    out.println("<FORM ACTION='" + SMUtilities.getURLLinkBase(getServletContext()) + "smic.ICEditAccountSetsEdit' METHOD='POST'>");
	    out.println("<INPUT TYPE=HIDDEN NAME='" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "' VALUE='" + sDBID + "'>");
	    out.println("<INPUT TYPE=TEXT NAME=\"" + ICAccountSet.ParamAccountSetCode 
	    		+ "\" SIZE=18 MAXLENGTH=" + Integer.toString(SMTableicaccountsets.sAccountSetCodeLength) 
	    		+ " STYLE=\"width: 1.75in; height: 0.25in\">&nbsp;");
	    out.println("<INPUT TYPE=SUBMIT NAME='SubmitEditDifferent' VALUE=\"Update different account set\" STYLE='height: 0.24in'>");
	    out.println("</FORM>");
	    out.println("<TD>");
	    out.println("</TR>");
	    out.println("</TABLE>");

	    Edit_Record(acctset, out, sDBID, sUserID, sUserFullName, sDBID);
		
		out.println("</BODY></HTML>");
	}
	
	private void Edit_Record(
			ICAccountSet acctset, 
			PrintWriter pwOut, 
			String sConf,
			String sUserID,
			String sUserFullName,
			String sDBID
			){
		pwOut.println("<FORM NAME='MAINFORM' ACTION='" + SMUtilities.getURLLinkBase(getServletContext()) + "smic." + sEditAccountSetsCalledClassName + "' METHOD='POST'>");
		pwOut.println("<INPUT TYPE=HIDDEN NAME='" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "' VALUE='" + sDBID + "'>");
		if(acctset.getNewRecord().compareToIgnoreCase("0") == 0){
			pwOut.println("<INPUT TYPE=HIDDEN NAME=\"" + ICAccountSet.ParamAccountSetCode 
					+ "\" VALUE=\"" + acctset.getAccountSetCode() + "\">");
			pwOut.println("<INPUT TYPE=HIDDEN NAME=\"" + ICAccountSet.ParamAddingNewRecord + "\" VALUE=0>");
		}else{
			pwOut.println("<INPUT TYPE=HIDDEN NAME=\"" + ICAccountSet.ParamAddingNewRecord + "\" VALUE=1>");
		}
		pwOut.println("<INPUT TYPE=HIDDEN NAME=\"" + ICAccountSet.ParamLastMaintainedDate 
				+ "\" VALUE=\"" + acctset.getLastMaintainedDate() + "\">");
		pwOut.println("<INPUT TYPE=HIDDEN NAME=\"" 
				+ ICAccountSet.ParamLastEditUser + "\" VALUE=\"" + sUserFullName + "\">");
	    pwOut.println("Date last maintained: " + acctset.getLastMaintainedDate());
	    pwOut.println(" by user: " + acctset.getLastEditUser() + "<BR>");
	    pwOut.println("<TABLE BORDER=12 CELLSPACING=2>");
        
	    //Account Set:
	    if(acctset.getNewRecord().compareToIgnoreCase("1") == 0){
	        pwOut.println(
	        		"<TR>"
	    	        + "<TD ALIGN=RIGHT><B>" + "Account set code<FONT COLOR=\"RED\">*</FONT>:"  + " </B></TD>"
	    	        + "<TD ALIGN=LEFT>"
	        		+ "<INPUT TYPE=TEXT NAME=\"" + ICAccountSet.ParamAccountSetCode + "\""
	        		+ " VALUE=\"" + acctset.getAccountSetCode().replace("\"", "&quot;") + "\""
	        		+ " SIZE=32"
	        		+ " MAXLENGTH=" + Integer.toString(SMTableicaccountsets.sAccountSetCodeLength)
	        		+ " STYLE=\"height: 0.25in\""
	        		+ "></TD>"
	        		+ "<TD ALIGN=LEFT>" 
	        		+ "Up to " + SMTableicaccountsets.sAccountSetCodeLength + " characters." 
	        		+ "</TD>"
	        		+ "</TR>"
	        		);
	    	
	    }else{
	    	pwOut.println("<TD ALIGN=RIGHT><B>Account Set Code:</B></TD><TD>" 
	    			+ acctset.getAccountSetCode().replace("\"", "&quot;") + "</TD><TD>&nbsp;</TD>");
	    }
	    
        //Description:
        pwOut.println(
        		"<TR>"
    	        + "<TD ALIGN=RIGHT><B>" + "Description<FONT COLOR=\"RED\">*</FONT>:"  + " </B></TD>"
    	        + "<TD ALIGN=LEFT>"
        		+ "<INPUT TYPE=TEXT NAME=\"" + ICAccountSet.ParamDescription + "\""
        		+ " VALUE=\"" + acctset.getDescription().replace("\"", "&quot;") + "\""
        		+ " SIZE=76"
        		+ " MAXLENGTH=" + Integer.toString(SMTableicaccountsets.sDescriptionLength)
        		+ " STYLE=\"height: 0.25in\""
        		+ "></TD>"
        		+ "<TD ALIGN=LEFT>" 
        		+ "Up to " + SMTableicaccountsets.sDescriptionLength + " characters." 
        		+ "</TD>"
        		+ "</TR>"
        		);

		//Active?
	    int iTrueOrFalse = 0;
	    if (acctset.getActive().compareToIgnoreCase("1") == 0){
	    	iTrueOrFalse = 1;
	    }else{
	    	iTrueOrFalse = 0;
	    }
	    pwOut.println(clsCreateHTMLTableFormFields.Create_Edit_Form_Checkbox_Row(
	    	ICAccountSet.ParamActive, 
			iTrueOrFalse, 
			"Active account set?", 
			"Uncheck to de-activate this account set."
			)
		);

		//Date last made inactive:
        pwOut.println(
        		"<TR>"
    	        + "<TD ALIGN=RIGHT><B>" + "Inactive date:"  + " </B></TD>"
    	        + "<TD ALIGN=LEFT>"
        		+ "<INPUT TYPE=TEXT NAME=\"" + ICAccountSet.ParamInactiveDate + "\""
        		+ " VALUE=\"" + acctset.getInactiveDate().replace("\"", "&quot;") + "\""
        		+ " SIZE=60"
        		+ " MAXLENGTH=" + "10"
        		+ " STYLE=\"height: 0.25in\"" + ">"
        		+ SMUtilities.getDatePickerString(ICAccountSet.ParamInactiveDate, getServletContext())
        		+ "</TD>"
        		+ "<TD ALIGN=LEFT>" 
        		+ "In <B>mm/dd/yyyy</B> format." 
        		+ "</TD>"
        		+ "</TR>"
        		); 
        
        //Load the GL Accounts:
        ArrayList<String> arrValues = new ArrayList<String>(0);
        ArrayList<String> arrDescriptions = new ArrayList<String>(0);
        arrValues.clear();
        arrDescriptions.clear();
        arrValues.add("");
        arrDescriptions.add("-- Select a GL Account --");
        try{
			//Get the record to edit:
	        String sSQL = SMClasses.MySQLs.Get_GL_Account_List_SQL(true);
	        ResultSet rsGLAccts = clsDatabaseFunctions.openResultSet(
	        	sSQL, 
	        	getServletContext(), 
	        	sConf,
	        	"MySQL",
	        	this.toString() + ".Edit_Record - User: " 
	        	+ sUserID
	        	+ " - "
	        	+ sUserFullName
	        		);
	        
	        while (rsGLAccts.next()){
	        	arrValues.add((String) rsGLAccts.getString(SMTableglaccounts.sAcctID).trim());
	        	arrDescriptions.add((String) rsGLAccts.getString(SMTableglaccounts.sAcctID).trim() + " - " + (String) rsGLAccts.getString(SMTableglaccounts.sDesc).trim());
			}
	        rsGLAccts.close();
		}catch (SQLException ex){
	    	System.out.println("Error in " + this.toString()+ " class!!");
	        System.out.println("SQLException: " + ex.getMessage());
	        System.out.println("SQLState: " + ex.getSQLState());
	        System.out.println("SQL: " + ex.getErrorCode());
			//return false;
		}
		
	    //Inventory Acct:
        pwOut.println(ARUtilities.Create_Edit_Form_List_Row(
        		ICAccountSet.ParamInventoryAccount, 
        		arrValues, 
        		acctset.getInventoryAccount().replace("\"", "&quot;"),  
        		arrDescriptions, 
        		"Inventory Account<FONT COLOR=\"RED\">*</FONT>:", 
        		"Select the Inventory Account."
        		)
        );
        
	    //Payables Clearing Acct:
        pwOut.println(ARUtilities.Create_Edit_Form_List_Row(
        		ICAccountSet.ParamPayablesClearingAccount, 
        		arrValues, 
        		acctset.getPayablesClearingAccount().replace("\"", "&quot;"),  
        		arrDescriptions, 
        		"Payables Clearing Account<FONT COLOR=\"RED\">*</FONT>:", 
        		"Select the Payables Clearing Account."
        		)
        );

	    //Adjustment Write Off Acct:
        pwOut.println(ARUtilities.Create_Edit_Form_List_Row(
        		ICAccountSet.ParamAdjustmentWriteOffAccount, 
        		arrValues, 
        		acctset.getAdjustmentWriteOffAccount().replace("\"", "&quot;"),  
        		arrDescriptions, 
        		"Adjustment/Write Off Account<FONT COLOR=\"RED\">*</FONT>:", 
        		"Select the Adjustment/Write Off Account."
        		)
        );
        
	    //Non Stock Clearing Acct:
        pwOut.println(ARUtilities.Create_Edit_Form_List_Row(
        		ICAccountSet.ParamNonStockClearingAccount, 
        		arrValues, 
        		acctset.getNonStockClearingAccount().replace("\"", "&quot;"),  
        		arrDescriptions, 
        		"Non Stock Clearing Account<FONT COLOR=\"RED\">*</FONT>:", 
        		"Select the Non Stock Clearing Account."
        		)
        );

	    //Transfer Clearing Acct:
        pwOut.println(ARUtilities.Create_Edit_Form_List_Row(
        		ICAccountSet.ParamTransferClearingAccount, 
        		arrValues, 
        		acctset.getTransferClearingAccount().replace("\"", "&quot;"),  
        		arrDescriptions, 
        		"Transfer Clearing Account<FONT COLOR=\"RED\">*</FONT>:", 
        		"Select the Transfer Clearing Account."
        		)
        );
        
        pwOut.println("</TABLE>");
        //pwOut.println("<BR>");
        pwOut.println("<P><INPUT TYPE=SUBMIT NAME='SubmitEdit' VALUE='Update " + sAccountSetsObjectName + "' STYLE='height: 0.24in'></P>");
        pwOut.println("</FORM>");
		
	}
	public void doGet(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
		
		doPost(request, response);
	}
}
