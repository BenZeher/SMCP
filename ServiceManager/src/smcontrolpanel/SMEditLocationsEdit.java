package smcontrolpanel;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import smar.ARUtilities;
import SMDataDefinition.SMTableglaccounts;
import SMDataDefinition.SMTablelocations;
import SMDataDefinition.SMTableorderdetails;
import ServletUtilities.clsCreateHTMLTableFormFields;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsStringFunctions;

public class SMEditLocationsEdit extends HttpServlet {

	private static final long serialVersionUID = 1L;
	private static String sObjectName = "Location";
	private static String sCalledClassName = "SMEditLocationsAction";
	public void doPost(HttpServletRequest request,
				HttpServletResponse response)
				throws ServletException, IOException {

		PrintWriter out = response.getWriter();
		if (!SMAuthenticate.authenticateSMCPCredentials(
				request, 
				response, 
				getServletContext(), 
				SMSystemFunctions.SMEditLocations))
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
	    String sEditCode = (String) clsStringFunctions.filter(request.getParameter(sObjectName));

		String title = "";
		String subtitle = "";
		
	    if(request.getParameter("SubmitEdit") != null){
	    	//User has chosen to edit:
			title = "Edit " + sObjectName + ": " + sEditCode;
		    subtitle = "";
		    out.println(SMUtilities.SMCPTitleSubBGColor(title, subtitle, SMUtilities.getInitBackGroundColor(getServletContext(), sDBID), sCompanyName));

		    //Print a link to the first page after login:
		    out.println("<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMUserLogin?" 
				+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
				+ "\">Return to user login</A><BR><BR>");
			
		    Edit_Record(sEditCode, out, sDBID, false, sUserID, sUserFullName);
	    }
	    if(request.getParameter("SubmitDelete") != null){
	    	//User has chosen to delete:
			title = "Delete " + sObjectName + ": " + sEditCode;
		    subtitle = "";
		    out.println(SMUtilities.SMCPTitleSubBGColor(title, subtitle, SMUtilities.getInitBackGroundColor(getServletContext(), sDBID), sCompanyName));

		    //Print a link to the first page after login:
		    out.println("<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMUserLogin"
		    	+ "?" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID	
		    	+ "\">Return to user login</A><BR><BR>");
			
		    if (request.getParameter("ConfirmDelete") == null){
		    	out.println ("You must check the 'confirming' check box to delete.");
		    }
		    else{
			    if (Delete_Record(sEditCode, out, sDBID) == false){
			    	out.println ("Error deleting " + sEditCode + ".");
			    }
			    else{
			    	out.println ("Successfully deleted " + sEditCode + ".");
			    }
		    }
	    }
	    if(request.getParameter("SubmitAdd") != null){
	    	
		    String sNewCode = clsStringFunctions.filter(request.getParameter("New" + sObjectName));
	    	//User has chosen to add a new user:
			title = "Add " + sObjectName + ": " + sNewCode;
		    subtitle = "";
		    out.println(SMUtilities.SMCPTitleSubBGColor(title, subtitle, SMUtilities.getInitBackGroundColor(getServletContext(), sDBID), sCompanyName));

		    //Print a link to the first page after login:
		    out.println("<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMUserLogin"
		    	+ "?" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID	
		    	+ "\">Return to user login</A><BR><BR>");

		    if (sNewCode == ""){
		    	out.println ("You chose to add a new " + sObjectName + ", but you did not enter a new " + sObjectName + " to add.");
		    }
		    else{
		    	Edit_Record(sNewCode, out, sDBID, true, sUserID, sUserFullName);
		    }
	    }
		
		out.println("</BODY></HTML>");
	}
	
	private void Edit_Record(
			String sCode, 
			PrintWriter pwOut, 
			String sDBID,
			boolean bAddNew,
			String sUserID,
			String sUserFullName){
	    
		//first, add the record if it's an 'Add':
		if (bAddNew == true){
			try {
				Add_Record (sCode, sDBID, pwOut);
			} catch (Exception e) {
				pwOut.println("ERROR - Could not add " + sCode + " - " + e.getMessage() + ".<BR>");
				return;
			}
				
		}
		
		pwOut.println("<FORM NAME='MAINFORM' ACTION='" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel." + sCalledClassName + "' METHOD='POST'>");
		pwOut.println("<INPUT TYPE=HIDDEN NAME='" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "' VALUE='" + sDBID + "'>");
		pwOut.println("<INPUT TYPE=HIDDEN NAME=\"EditCode\" VALUE=\"" + sCode + "\">");
	    String sOutPut = "";
	  
        sOutPut = "<TABLE BORDER=13 CELLSPACING=2>";
        
		try{
			//Get the record to edit:
	        String sSQL = SMMySQLs.Get_Location_By_Code(sCode);
	        ResultSet rs = clsDatabaseFunctions.openResultSet(sSQL, getServletContext(), sDBID);
	        
	        rs.next();
	        //Display fields:
	        //Company description:
	        sOutPut += clsCreateHTMLTableFormFields.Create_Edit_Form_Text_Input_Row(
	        		SMTablelocations.sCompanyDescription, 
	        		clsStringFunctions.filter(
	        			rs.getString(SMTablelocations.sCompanyDescription)), 
	        		SMTablelocations.sCompanyDescriptionLength, 
	        		"Company description:", 
	        		"Description of this corporate entity.",
	        		"60");

	        //Location description:
	        sOutPut += clsCreateHTMLTableFormFields.Create_Edit_Form_Text_Input_Row(
	        		SMTablelocations.sLocationDescription, 
	        		clsStringFunctions.filter(rs.getString(SMTablelocations.sLocationDescription)), 
	        		SMTablelocations.sLocationDescriptionLength, 
	        		"Location description:", 
	        		"Description of this particular location/office.",
	        		"60");

	        //Address 1:
	        sOutPut += clsCreateHTMLTableFormFields.Create_Edit_Form_Text_Input_Row(
	        		SMTablelocations.sAddress1, 
	        		clsStringFunctions.filter(rs.getString(SMTablelocations.sAddress1)), 
	        		SMTablelocations.sAddressLine1Length, 
	        		"Address line 1:", 
	        		"First line of location address.",
	        		"60");

	        //Address 2:
	        sOutPut += clsCreateHTMLTableFormFields.Create_Edit_Form_Text_Input_Row(
	        		SMTablelocations.sAddress2, 
	        		clsStringFunctions.filter(rs.getString(SMTablelocations.sAddress2)), 
	        		SMTablelocations.sAddressLine2Length, 
	        		"Line 2:", 
	        		"Second line of location address.",
	        		"60");

	        //Address 3:
	        sOutPut += clsCreateHTMLTableFormFields.Create_Edit_Form_Text_Input_Row(
	        		SMTablelocations.sAddress3, 
	        		clsStringFunctions.filter(rs.getString(SMTablelocations.sAddress3)), 
	        		SMTablelocations.sAddressLine3Length, 
	        		"Line 3:", 
	        		"Third line of location address.",
	        		"60");

	        //Address 4:
	        sOutPut += clsCreateHTMLTableFormFields.Create_Edit_Form_Text_Input_Row(
	        		SMTablelocations.sAddress4, 
	        		clsStringFunctions.filter(rs.getString(SMTablelocations.sAddress4)), 
	        		SMTablelocations.sAddressLine4Length, 
	        		"Line 4:", 
	        		"Fourth line of location address.",
	        		"60");

	        //City
	        sOutPut += clsCreateHTMLTableFormFields.Create_Edit_Form_Text_Input_Row(
	        		SMTablelocations.sCity, 
	        		clsStringFunctions.filter(rs.getString(SMTablelocations.sCity)), 
	        		SMTablelocations.sCityLength, 
	        		"City:", 
	        		"Location city.",
	        		"30");
	        
	        //State
	        sOutPut += clsCreateHTMLTableFormFields.Create_Edit_Form_Text_Input_Row(
	        		SMTablelocations.sState, 
	        		clsStringFunctions.filter(rs.getString(SMTablelocations.sState)), 
	        		SMTablelocations.sStateLength, 
	        		"State:", 
	        		"Location state (normally abbreviated, e.g. CA, MD, etc.",
	        		"12");
	        
	        //Zip
	        sOutPut += clsCreateHTMLTableFormFields.Create_Edit_Form_Text_Input_Row(
	        		SMTablelocations.sZip, 
	        		clsStringFunctions.filter(rs.getString(SMTablelocations.sZip)), 
	        		SMTablelocations.sZipCodeLength, 
	        		"Zip code:", 
	        		"Location zip.",
	        		"12");
	        
	        //Country
	        sOutPut += clsCreateHTMLTableFormFields.Create_Edit_Form_Text_Input_Row(
	        		SMTablelocations.sCountry, 
	        		clsStringFunctions.filter(rs.getString(SMTablelocations.sCountry)), 
	        		SMTablelocations.sCountryLength, 
	        		"Country:", 
	        		"Location country (usually abbreviated, e.g. USA.)",
	        		"12");
	        
	        //Phone
	        sOutPut += clsCreateHTMLTableFormFields.Create_Edit_Form_Text_Input_Row(
	        		SMTablelocations.sPhone, 
	        		clsStringFunctions.filter(rs.getString(SMTablelocations.sPhone)), 
	        		SMTablelocations.sPhoneNumberLength, 
	        		"Phone number:", 
	        		"Phone associated with location - punctuate as desired.",
	        		"15");
	        
	        //Fax
	        sOutPut += clsCreateHTMLTableFormFields.Create_Edit_Form_Text_Input_Row(
	        		SMTablelocations.sFax, 
	        		clsStringFunctions.filter(rs.getString(SMTablelocations.sFax)), 
	        		SMTablelocations.sFaxNumberLength, 
	        		"Fax number:", 
	        		"Fax associated with location - punctuate as desired.",
	        		"15");
	        
	        //Second Office Name
	        sOutPut += clsCreateHTMLTableFormFields.Create_Edit_Form_Text_Input_Row(
	        		SMTablelocations.sSecondOfficeName, 
	        		clsStringFunctions.filter(rs.getString(SMTablelocations.sSecondOfficeName)), 
	        		SMTablelocations.sSecondOfficeNameLength, 
	        		"Second office name:", 
	        		"Additional office name for this location.",
	        		"60");

	        //Second Office Phone
	        sOutPut += clsCreateHTMLTableFormFields.Create_Edit_Form_Text_Input_Row(
	        		SMTablelocations.sSecondOfficePhone, 
	        		clsStringFunctions.filter(rs.getString(SMTablelocations.sSecondOfficePhone)), 
	        		SMTablelocations.sSecondOfficePhoneLength, 
	        		"Second office phone:", 
	        		"Additional office phone - punctuate as desired.",
	        		"15");

	        //Contact
	        sOutPut += clsCreateHTMLTableFormFields.Create_Edit_Form_Text_Input_Row(
	        		SMTablelocations.sContact, 
	        		clsStringFunctions.filter(rs.getString(SMTablelocations.sContact)), 
	        		SMTablelocations.sContactLength, 
	        		"Contact name:", 
	        		"Contact associated with this location.",
	        		"60");

	        //REMIT TOs:
	        //Remit to Company Description:
	        sOutPut += clsCreateHTMLTableFormFields.Create_Edit_Form_Text_Input_Row(
	        		SMTablelocations.sRemitToCompanyDescription, 
	        		clsStringFunctions.filter(rs.getString(SMTablelocations.sRemitToCompanyDescription)), 
	        		SMTablelocations.sRemitToCompanyDescriptionLength, 
	        		"Remit to company description:", 
	        		"Description of this corporate entity.",
	        		"60");

	        //Remit to Address 1:
	        sOutPut += clsCreateHTMLTableFormFields.Create_Edit_Form_Text_Input_Row(
	        		SMTablelocations.sRemitToAddress1, 
	        		clsStringFunctions.filter(rs.getString(SMTablelocations.sRemitToAddress1)), 
	        		SMTablelocations.sRemitToAddressLine1Length, 
	        		"Remit To Address line 1:", 
	        		"First line of location remit to address.",
	        		"60");

	        //Remit To Address 2:
	        sOutPut += clsCreateHTMLTableFormFields.Create_Edit_Form_Text_Input_Row(
	        		SMTablelocations.sRemitToAddress2, 
	        		clsStringFunctions.filter(rs.getString(SMTablelocations.sRemitToAddress2)), 
	        		SMTablelocations.sRemitToAddressLine2Length, 
	        		"Remit To Line 2:", 
	        		"Second line of location remit to address.",
	        		"60");

	        //Remit To Address 3:
	        sOutPut += clsCreateHTMLTableFormFields.Create_Edit_Form_Text_Input_Row(
	        		SMTablelocations.sRemitToAddress3, 
	        		clsStringFunctions.filter(rs.getString(SMTablelocations.sRemitToAddress3)), 
	        		SMTablelocations.sRemitToAddressLine3Length, 
	        		"Remit To Line 3:", 
	        		"Third line of location remit to address.",
	        		"60");

	        //Remit to Address 4:
	        sOutPut += clsCreateHTMLTableFormFields.Create_Edit_Form_Text_Input_Row(
	        		SMTablelocations.sRemitToAddress4, 
	        		clsStringFunctions.filter(rs.getString(SMTablelocations.sRemitToAddress4)), 
	        		SMTablelocations.sRemitToAddressLine4Length, 
	        		"Remit To Line 4:", 
	        		"Fourth line of location remit to address.",
	        		"60");

	        //Remit to City
	        sOutPut += clsCreateHTMLTableFormFields.Create_Edit_Form_Text_Input_Row(
	        		SMTablelocations.sRemitToCity, 
	        		clsStringFunctions.filter(rs.getString(SMTablelocations.sRemitToCity)), 
	        		SMTablelocations.sRemitToCityLength, 
	        		"Remit To City:", 
	        		"Location remit to city.",
	        		"30");
	        
	        //Remit To State
	        sOutPut += clsCreateHTMLTableFormFields.Create_Edit_Form_Text_Input_Row(
	        		SMTablelocations.sRemitToState, 
	        		clsStringFunctions.filter(rs.getString(SMTablelocations.sRemitToState)), 
	        		SMTablelocations.sRemitToStateLength, 
	        		"Remit To State:", 
	        		"Location remit to state (normally abbreviated, e.g. CA, MD, etc.",
	        		"12");
	        
	        //Remit to Zip
	        sOutPut += clsCreateHTMLTableFormFields.Create_Edit_Form_Text_Input_Row(
	        		SMTablelocations.sRemitToZip, 
	        		clsStringFunctions.filter(rs.getString(SMTablelocations.sRemitToZip)), 
	        		SMTablelocations.sRemitToZipCodeLength, 
	        		"Remit To Zip code:", 
	        		"Location remit to zip.",
	        		"12");
	        
	        //Remit to Country
	        sOutPut += clsCreateHTMLTableFormFields.Create_Edit_Form_Text_Input_Row(
	        		SMTablelocations.sRemitToCountry, 
	        		clsStringFunctions.filter(rs.getString(SMTablelocations.sRemitToCountry)), 
	        		SMTablelocations.sRemitToCountryLength, 
	        		"Remit to Country:", 
	        		"Location remit to country (usually abbreviated, e.g. USA.",
	        		"12");
	        
	        //Remit to Phone
	        sOutPut += clsCreateHTMLTableFormFields.Create_Edit_Form_Text_Input_Row(
	        		SMTablelocations.sRemitToPhone, 
	        		clsStringFunctions.filter(rs.getString(SMTablelocations.sRemitToPhone)), 
	        		SMTablelocations.sRemitToPhoneNumberLength, 
	        		"Remit to Phone Number:", 
	        		"Remit to phone associated with location - punctuate as desired.",
	        		"15");
	        
	        //Remit to Fax
	        sOutPut += clsCreateHTMLTableFormFields.Create_Edit_Form_Text_Input_Row(
	        		SMTablelocations.sRemitToFax, 
	        		clsStringFunctions.filter(rs.getString(SMTablelocations.sRemitToFax)), 
	        		SMTablelocations.sRemitToFaxNumberLength, 
	        		"Remit To Fax Number:", 
	        		"Remit to fax associated with location - punctuate as desired.",
	        		"15");
	        
	        //Remit to Contact
	        sOutPut += clsCreateHTMLTableFormFields.Create_Edit_Form_Text_Input_Row(
	        		SMTablelocations.sRemitToContact, 
	        		clsStringFunctions.filter(rs.getString(SMTablelocations.sRemitToContact)), 
	        		SMTablelocations.sRemitToContactLength, 
	        		"Remit To Contact name:", 
	        		"Remit to contact associated with this location.",
	        		"60");
	       //WOAdditionalNotes
	        sOutPut += clsCreateHTMLTableFormFields.Create_Edit_Form_MultilineText_Input_Row(
	        		SMTablelocations.sAdditionalNotes,
	        		rs.getString(SMTablelocations.sAdditionalNotes.trim()),
	        		"Additional Notes:",
	        		"Notes",
	        		3, 
	        		45
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
		        sSQL = SMClasses.MySQLs.Get_GL_Account_List_SQL(true);
		        ResultSet rsGLAccts = clsDatabaseFunctions.openResultSet(
		        	sSQL, 
		        	getServletContext(), 
		        	sDBID,
		        	"MySQL",
		        	this.toString() + ".Edit_Record - User: " + sUserID
		        	+ " - "
		        	+ sUserFullName	);
		        
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
			
			pwOut.println(sOutPut);
	        
			//Inventory account
	        pwOut.println(ARUtilities.Create_Edit_Form_List_Row(
	        		SMTablelocations.sGLInventoryAcct, 
	        		arrValues, 
	        		clsStringFunctions.filter(rs.getString(SMTablelocations.sGLInventoryAcct)),  
	        		arrDescriptions, 
	        		"Inventory GL Account:", 
	        		"Inventory asset account associated with this location."
	        		)
	        );
	        
	        //Payables clearing:
	        pwOut.println(ARUtilities.Create_Edit_Form_List_Row(
	        		SMTablelocations.sGLPayableClearingAcct, 
	        		arrValues, 
	        		clsStringFunctions.filter(rs.getString(SMTablelocations.sGLPayableClearingAcct)),  
	        		arrDescriptions, 
	        		"Payables Clearing GL Account:", 
	        		"Clearing (receipt credit) account associated with this location."
	        		)
	        );
	        
	        //Write off account
	        pwOut.println(ARUtilities.Create_Edit_Form_List_Row(
	        		SMTablelocations.sGLWriteOffAcct, 
	        		arrValues, 
	        		clsStringFunctions.filter(rs.getString(SMTablelocations.sGLWriteOffAcct)),  
	        		arrDescriptions, 
	        		"Write Off GL Account:", 
    				"Write Off account associated with this location."
	        		)
	        );
	        
	        //Transfer clearing account
	        pwOut.println(ARUtilities.Create_Edit_Form_List_Row(
	        		SMTablelocations.sGLTransferClearingAcct, 
	        		arrValues, 
	        		clsStringFunctions.filter(rs.getString(SMTablelocations.sGLTransferClearingAcct)),  
	        		arrDescriptions, 
	        		"Transfer clearing GL Account:", 
    				"Transfer clearing account associated with this location."
	        		)
	        );

	        //Logo
	        ArrayList<String> sLogoValues = new ArrayList<String>(0);
	        sLogoValues.add((String) "YES");
	        sLogoValues.add((String) "NO");
	        ArrayList<String> sLogoDescriptions = new ArrayList<String>(0);
	        sLogoDescriptions.add((String) "Use supplied logo file");
	        sLogoDescriptions.add((String) "Don't  use supplied logo file");
	        pwOut.println(clsCreateHTMLTableFormFields.Create_Edit_Form_List_Row(
	        		SMTablelocations.sLogo, 
	        		sLogoValues, 
	        		clsStringFunctions.filter(rs.getString(SMTablelocations.sLogo)), 
	        		sLogoDescriptions, 
	        		"Use logo:", 
	        		"This determines whether the logo is printed on invoices.")
	        		);

	        //Toll Free Number
	        pwOut.println(clsCreateHTMLTableFormFields.Create_Edit_Form_Text_Input_Row(
	        		SMTablelocations.sTollFreeNumber, 
	        		clsStringFunctions.filter(rs.getString(SMTablelocations.sTollFreeNumber)), 
	        		SMTablelocations.sTollFreeNumberLength, 
	        		"Toll free number:", 
	        		"Any toll-free number associated with this location.")
	        		);
	        
	        //Web Site
	        pwOut.println(clsCreateHTMLTableFormFields.Create_Edit_Form_Text_Input_Row(
	        		SMTablelocations.sWebSite, 
	        		clsStringFunctions.filter(rs.getString(SMTablelocations.sWebSite)), 
	        		SMTablelocations.sWebSiteLength, 
	        		"Web site:", 
	        		"Web site address associate with this location.")
	        		);
	        
	        //Show in truck schedule?
		    int iTrueOrFalse = 0;
		    if (rs.getInt(SMTablelocations.ishowintruckschedule) == 1){
		    	iTrueOrFalse = 1;
		    }else{
		    	iTrueOrFalse = 0;
		    }
		    pwOut.println(clsCreateHTMLTableFormFields.Create_Edit_Form_Checkbox_Row(
		    	SMTablelocations.ishowintruckschedule, 
				iTrueOrFalse, 
				"Show in truck schedule?", 
				"Uncheck to prevent this location from appearing in the location choices when viewing a truck schedule."
				)
			);
	        rs.close();
		}catch (SQLException ex){
	    	System.out.println("Error in " + this.toString()+ " class!!");
	        System.out.println("SQLException: " + ex.getMessage());
	        System.out.println("SQLState: " + ex.getSQLState());
	        System.out.println("SQL: " + ex.getErrorCode());
			//return false;
		}
		
		//********************
		pwOut.println("</TABLE>");
		pwOut.println("<BR>");
		pwOut.println("<P><INPUT TYPE=SUBMIT NAME='SubmitEdit' VALUE='Update " + sObjectName + "' STYLE='height: 0.24in'></P>");
		pwOut.println("</FORM>");
		
	}
	
	private boolean Delete_Record(
			String sCode,
			PrintWriter pwOut,
			String sDBID){
		boolean bError = false;
		ArrayList<String> sSQLList = new ArrayList<String>(0);
		
		//if there are any unshiped items with this location do not delete it. 
		String SQL = "SELECT DISTINCT " + SMTableorderdetails.strimmedordernumber
			+ " FROM " + SMTableorderdetails.TableName
			+ " WHERE ("
			+ "(" + SMTableorderdetails.sLocationCode + " = '"  + sCode + "')"
			+ " AND (" + SMTableorderdetails.dQtyOrdered + " > 0.00)"
			+ ")"
			;
		
		try {
			ResultSet rs = clsDatabaseFunctions.openResultSet(
					SQL, 
					getServletContext(), 
					sDBID, 
					"MySQL",
					this.toString() + ".loadChangeOrders - user: "  
					);
			
			while(rs.next()) {
				pwOut.println ("Order '"+ rs.getString(SMTableorderdetails.strimmedordernumber) 
				+ "' contains item(s) with location code '" + sCode + "' that have not been shipped.<BR>");
				bError = true;
			}
		if(bError) {
			return false;
		}
			
		} catch (SQLException e) {
			pwOut.println (e.getMessage());
			return false;
		}

		//Include all the SQLs needed to delete a record:
		sSQLList.add((String) SMMySQLs.Delete_Location_SQL(sCode));
		try {
			boolean bResult = clsDatabaseFunctions.executeSQLsInTransaction(sSQLList, getServletContext(), sDBID);
			return bResult;
		}catch (SQLException ex){
	    	System.out.println("Error in " + this.toString()+ ".Delete_Record class!!");
	        System.out.println("SQLException: " + ex.getMessage());
	        System.out.println("SQLState: " + ex.getSQLState());
	        System.out.println("SQL: " + ex.getErrorCode());
			return false;
		}		
	}
	
	private void Add_Record(String sCode, String sDBID, PrintWriter pwOut) throws Exception{
		
		//First, make sure there isn't a user by this name already:
		String sSQL = SMMySQLs.Get_Location_By_Code(sCode);
		
		try{
			ResultSet rs = clsDatabaseFunctions.openResultSet(sSQL, getServletContext(), sDBID);
			if (rs.next()){
				//This record already exists, so we can't add it:
				rs.close();
				throw new Exception("The " + sObjectName + " '" + sCode + "' already exists - it cannot be added.<BR>");
			}
			rs.close();
		}catch(SQLException ex){
			throw new Exception("Error [1439993937] checking for existing location with SQL: " + sSQL + " - " + ex.getMessage());
		}
		sSQL = SMMySQLs.Add_New_Location_SQL(sCode);

		Connection conn = null;
		try {
			conn = clsDatabaseFunctions.getConnectionWithException(
					getServletContext(), 
					sDBID, 
					"MySQL", 
					SMUtilities.getFullClassName(this.toString()) + ".adding new location"
				);
		} catch (Exception e) {
			throw new Exception("Error [1439993938] getting data connection - " + e.getMessage());
		}
		
		try {
			Statement stmt = conn.createStatement();
			stmt.execute(sSQL);
			clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1547080497]");
		}catch (SQLException ex){
			throw new Exception("Error [1439993939] adding new location with SQL: " + sSQL + " - " + ex.getMessage());
		}
		clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1547080498]");
	}
	
	public void doGet(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
		
		doPost(request, response);
	}
}
