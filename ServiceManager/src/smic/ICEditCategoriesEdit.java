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
import SMDataDefinition.SMTableiccategories;
import ServletUtilities.clsCreateHTMLTableFormFields;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsManageRequestParameters;

public class ICEditCategoriesEdit extends HttpServlet {

	private static final long serialVersionUID = 1L;
	private static final String sCategoryObjectName = "Category";
	private static final String sICEditCategoriesEditCalledClassName = "ICEditCategoriesAction";

	public void doPost(HttpServletRequest request,
				HttpServletResponse response)
				throws ServletException, IOException {

		PrintWriter out = response.getWriter();
		if (!SMAuthenticate.authenticateSMCPCredentials(
				request, 
				response, 
				getServletContext(), 
				SMSystemFunctions.ICEditCategories))
		{
			return;
		}

	    //Get the session info:
	    HttpSession CurrentSession = request.getSession(true);
	    String sDBID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_DATABASE_ID);
	    String sUserID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERID);
	    String sUserFullName = (String)CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERFIRSTNAME) + " "
	    				+ (String)CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERLASTNAME);
	    String sCompanyName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_COMPANYNAME);
		//Load the input class from the request object - if it's a 'resubmit', then this will contain
	    //all the values typed from the previous screen.  If it's a 'first time' edit, then this will only
	    //contain the customer number
		ICCategory category = new ICCategory("");
		category.loadFromHTTPRequest(request);
	    //First process if it's a 'delete':
	    if(request.getParameter("SubmitDelete") != null){
		    if (request.getParameter("ConfirmDelete") == null){
				response.sendRedirect(
					"" + SMUtilities.getURLLinkBase(getServletContext()) + "smic.ICEditCategoriesSelection"
					+ "?" + ICCategory.ParamCategoryCode + "=" + category.getCategoryCode()
					+ "&Warning=You must check the 'confirming' check box to delete."
					+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
				);
				return;
		    }
		    if (category.getCategoryCode().compareToIgnoreCase("") == 0){
				response.sendRedirect(
					"" + SMUtilities.getURLLinkBase(getServletContext()) + "smic.ICEditCategoriesSelection"
					+ "?" + ICCategory.ParamCategoryCode + "=" + category.getCategoryCode()
					+ "&Warning=You must enter an category to delete."
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
		    		this.toString() + ".doPost - User: " 
		    		+ sUserID
		    		+ " - "
		    		+ sUserFullName
		    			);
		    	if(conn == null){
    				response.sendRedirect(
        					"" + SMUtilities.getURLLinkBase(getServletContext()) + "smic.ICEditCategoriesSelection"
        					+ "?" + ICCategory.ParamCategoryCode + "=" + category.getCategoryCode()
        					+ "&Warning=Error deleting category - cannot get connection."
        					+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
        				);
    						return;
		    	}
		    	clsDatabaseFunctions.start_data_transaction(conn);
			    if (!category.delete(category.getCategoryCode(), conn)){
			    	clsDatabaseFunctions.rollback_data_transaction(conn);
			    	clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1547080824]");
    				response.sendRedirect(
    					"" + SMUtilities.getURLLinkBase(getServletContext()) + "smic.ICEditCategoriesSelection"
    					+ "?" + ICCategory.ParamCategoryCode + "=" + category.getCategoryCode()
    					+ "&Warning=Error deleting category - " + category.getErrorMessageString()
    					+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
    				);
					return;
			    }else{
			    	clsDatabaseFunctions.commit_data_transaction(conn);
			    	clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1547080825]");
    				response.sendRedirect(
    					"" + SMUtilities.getURLLinkBase(getServletContext()) + "smic.ICEditCategoriesSelection"
    					+ "?" + ICCategory.ParamCategoryCode + "=" + category.getCategoryCode()
    					+ "&Status=Successfully deleted category " + category.getCategoryCode() + "."
    					+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
    				);
					return;
			    }
		    }
	    }
	    
		String title = "";
		String subtitle = "";
		
		if(request.getParameter("SubmitAdd") != null){
			category.setCategoryCode("");
			category.setNewRecord("1");
		}
		//If we are coming here for the first time to edit (i.e., it's not a re-submit), then we need to 
		//load the input class from the database if possible:
		if(request.getParameter("SubmitEdit") != null){
	    	if(!category.load(getServletContext(), sDBID)){
				response.sendRedirect(
					"" + SMUtilities.getURLLinkBase(getServletContext()) + "smic.ICEditCategoriesSelection"
					+ "?" + ICCategory.ParamCategoryCode + "=" + category.getCategoryCode()
					+ "&Warning=Could not load category " + category.getCategoryCode() + "."
					+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
				);
					return;
	    	}
		}
		
		//If we are coming here from the same screen to edit a different category, then we also need to 
		//load the input class from the database if possible:
		if(request.getParameter("SubmitEditDifferent") != null){
	    	if(!category.load(getServletContext(), sDBID)){
				response.sendRedirect(
					"" + SMUtilities.getURLLinkBase(getServletContext()) + "smic.ICEditCategoriesSelection"
					+ "?" + ICCategory.ParamCategoryCode + "=" + category.getCategoryCode()
					+ "&Warning=Could not load category " + category.getCategoryCode() + "."
					+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
				);
					return;
	    	}
		}
		
		//In any other case, such as the possibility that this is a 'resubmit', we need to edit the customer:
    	title = "Edit " + sCategoryObjectName;
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
	    out.println("<A HREF=\"" + WebContextParameters.getdocumentationpageURL(getServletContext()) + "#" + Long.toString(SMSystemFunctions.ICEditCategories) 
	    		+ "\">Summary</A>");
	    out.println("</TD>");
	    
	    //Create a form for editing a different category:
	    out.println("<TD ALIGN=RIGHT>");
	    out.println("<FORM ACTION='" + SMUtilities.getURLLinkBase(getServletContext()) + "smic.ICEditCategoriesEdit' METHOD='POST'>");
	    out.println("<INPUT TYPE=HIDDEN NAME='" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "' VALUE='" + sDBID + "'>");
	    out.println("<INPUT TYPE=TEXT NAME=\"" + ICCategory.ParamCategoryCode 
	    		+ "\" SIZE=18 MAXLENGTH=" + Integer.toString(SMTableiccategories.sCategoryCodeLength) 
	    		+ " STYLE=\"width: 1.75in; height: 0.25in\">&nbsp;");
	    out.println("<INPUT TYPE=SUBMIT NAME='SubmitEditDifferent' VALUE=\"Update different category\" STYLE='height: 0.24in'>");
	    out.println("</FORM>");
	    out.println("<TD>");
	    out.println("</TR>");
	    out.println("</TABLE>");

	    Edit_Record(category, out, sDBID, sUserFullName, sUserID);
		
		out.println("</BODY></HTML>");
	}
	
	private void Edit_Record(
			ICCategory category, 
			PrintWriter pwOut, 
			String sDBID,
			String sUserFullName,
			String sUserID
			){
		pwOut.println("<FORM NAME='MAINFORM' ACTION='" + SMUtilities.getURLLinkBase(getServletContext()) + "smic." + sICEditCategoriesEditCalledClassName + "' METHOD='POST'>");
		pwOut.println("<INPUT TYPE=HIDDEN NAME='" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "' VALUE='" + sDBID + "'>");
		if(category.getNewRecord().compareToIgnoreCase("0") == 0){
			pwOut.println("<INPUT TYPE=HIDDEN NAME=\"" + ICCategory.ParamCategoryCode 
					+ "\" VALUE=\"" + category.getCategoryCode() + "\">");
			pwOut.println("<INPUT TYPE=HIDDEN NAME=\"" + ICCategory.ParamAddingNewRecord + "\" VALUE=0>");
		}else{
			pwOut.println("<INPUT TYPE=HIDDEN NAME=\"" + ICCategory.ParamAddingNewRecord + "\" VALUE=1>");
		}
		pwOut.println("<INPUT TYPE=HIDDEN NAME=\"" + ICCategory.ParamLastMaintainedDate 
				+ "\" VALUE=\"" + category.getLastMaintainedDate() + "\">");
		pwOut.println("<INPUT TYPE=HIDDEN NAME=\"" 
				+ ICCategory.ParamLastEditUser + "\" VALUE=\"" + sUserFullName + "\">");
	    pwOut.println("Date last maintained: " + category.getLastMaintainedDate());
	    pwOut.println(" by user: " + category.getLastEditUser() + "<BR>");
	    pwOut.println("<TABLE BORDER=12 CELLSPACING=2>");
        
	    //category:
	    if(category.getNewRecord().compareToIgnoreCase("1") == 0){
	        pwOut.println(
	        		"<TR>"
	    	        + "<TD ALIGN=RIGHT><B>" + "Category code<FONT COLOR=\"RED\">*</FONT>:"  + " </B></TD>"
	    	        + "<TD ALIGN=LEFT>"
	        		+ "<INPUT TYPE=TEXT NAME=\"" + ICCategory.ParamCategoryCode + "\""
	        		+ " VALUE=\"" + category.getCategoryCode().replace("\"", "&quot;") + "\""
	        		+ " SIZE=32"
	        		+ " MAXLENGTH=" + Integer.toString(SMTableiccategories.sCategoryCodeLength)
	        		+ " STYLE=\"height: 0.25in\""
	        		+ "></TD>"
	        		+ "<TD ALIGN=LEFT>" 
	        		+ "Up to " + SMTableiccategories.sCategoryCodeLength + " characters." 
	        		+ "</TD>"
	        		+ "</TR>"
	        		);
	    	
	    }else{
	    	pwOut.println("<TD ALIGN=RIGHT><B>Category Code:</B></TD><TD>" 
	    			+ category.getCategoryCode().replace("\"", "&quot;") + "</TD><TD>&nbsp;</TD>");
	    }
	    
        //Description:
        pwOut.println(
        		"<TR>"
    	        + "<TD ALIGN=RIGHT><B>" + "Description<FONT COLOR=\"RED\">*</FONT>:"  + " </B></TD>"
    	        + "<TD ALIGN=LEFT>"
        		+ "<INPUT TYPE=TEXT NAME=\"" + ICCategory.ParamDescription + "\""
        		+ " VALUE=\"" + category.getDescription().replace("\"", "&quot;") + "\""
        		+ " SIZE=76"
        		+ " MAXLENGTH=" + Integer.toString(SMTableiccategories.sDescriptionLength)
        		+ " STYLE=\"height: 0.25in\""
        		+ "></TD>"
        		+ "<TD ALIGN=LEFT>" 
        		+ "Up to " + SMTableiccategories.sDescriptionLength + " characters." 
        		+ "</TD>"
        		+ "</TR>"
        		);

		//Active?
	    int iTrueOrFalse = 0;
	    if (category.getActive().compareToIgnoreCase("1") == 0){
	    	iTrueOrFalse = 1;
	    }else{
	    	iTrueOrFalse = 0;
	    }
	    pwOut.println(clsCreateHTMLTableFormFields.Create_Edit_Form_Checkbox_Row(
	    	ICCategory.ParamActive, 
			iTrueOrFalse, 
			"Active category?", 
			"Uncheck to de-activate this category."
			)
		);

		//Date last made inactive:
        pwOut.println(
        		"<TR>"
    	        + "<TD ALIGN=RIGHT><B>" + "Inactive date:"  + " </B></TD>"
    	        + "<TD ALIGN=LEFT>"
        		+ "<INPUT TYPE=TEXT NAME=\"" + ICCategory.ParamInactiveDate + "\""
        		+ " VALUE=\"" + category.getInactiveDate().replace("\"", "&quot;") + "\""
        		+ " SIZE=60"
        		+ " MAXLENGTH=" + "10"
        		+ " STYLE=\"height: 0.25in\"" + ">"
        		+ SMUtilities.getDatePickerString(ICCategory.datInactive, getServletContext())
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
	        	sDBID,
	        	"MySQL",
	        	this.toString() + ".Edit_Record - User: " + sUserID
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
		
	    //Cost of goods sold:
        pwOut.println(ARUtilities.Create_Edit_Form_List_Row(
        		ICCategory.ParamCostOfGoodsSoldAccount, 
        		arrValues, 
        		category.getCostOfGoodsSoldAccount().replace("\"", "&quot;"),  
        		arrDescriptions, 
        		"Cost Of Goods Sold account<FONT COLOR=\"RED\">*</FONT>:", 
        		"Select the Cost Of Goods Sold account."
        		)
        );
        
	    //Sales Acct:
        pwOut.println(ARUtilities.Create_Edit_Form_List_Row(
        		ICCategory.ParamSalesAccount, 
        		arrValues, 
        		category.getSalesAccount().replace("\"", "&quot;"),  
        		arrDescriptions, 
        		"Sales account<FONT COLOR=\"RED\">*</FONT>:", 
        		"Select the Sales account."
        		)
        );

        pwOut.println("</TABLE>");
        //pwOut.println("<BR>");
        pwOut.println("<P><INPUT TYPE=SUBMIT NAME='SubmitEdit' VALUE='Update " + sCategoryObjectName + "' STYLE='height: 0.24in'></P>");
        pwOut.println("</FORM>");
		
	}
	public void doGet(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
		
		doPost(request, response);
	}
}
