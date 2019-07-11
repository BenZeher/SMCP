package smar;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import smcontrolpanel.SMAuthenticate;
import smcontrolpanel.SMMySQLs;
import smcontrolpanel.SMSystemFunctions;
import smcontrolpanel.SMUtilities;
import ConnectionPool.WebContextParameters;
import SMDataDefinition.SMCreateGoogleDriveFolderParamDefinitions;
import SMDataDefinition.SMTablearacctset;
import SMDataDefinition.SMTablearcustomer;
import SMDataDefinition.SMTablearcustomergroups;
import SMDataDefinition.SMTablearterms;
import SMDataDefinition.SMTabledefaultsalesgroupsalesperson;
import SMDataDefinition.SMTablepricelistcodes;
import SMDataDefinition.SMTablesalesgroups;
import SMDataDefinition.SMTablesalesperson;
import SMDataDefinition.SMTabletax;
import ServletUtilities.clsServletUtilities;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsManageRequestParameters;

public class AREditCustomersEdit extends HttpServlet {

	private static final long serialVersionUID = 1L;
	private static final String sObjectName = "Customer";
	private static final String sCalledClassName = "AREditCustomersAction";
	
	public static final String CUSTOMER_SALESGROUP_SALESPERSON_FIELD = "CUSTOMERSALESGROUPSALESPERSON";
	public static final String CREATE_UPLOAD_FOLDER_BUTTON_LABEL = "Create folder/Upload to Google Drive";
	public static final String CREATE_UPLOAD_FOLDER_COMMAND_VALUE = "CREATEUPLOADFOLDER";
	
	public static final boolean bDebugMode = false;
	public static final String COMMAND_FLAG = "COMMANDFLAG";
	public static final String RECORDWASCHANGED_FLAG = "RECORDWASCHANGEDFLAG";
	public static final String RECORDWASCHANGED_FLAG_VALUE = "RECORDWASCHANGED";
	public static final String UPDATE_COMMAND_VALUE = "UPDATECUSTOMER";
	public static final String UPDATE_BUTTON_LABEL = "Update Customer";
	public static final String Paramlastsaveddate = "lastsavedstartdate";
	
	public void doPost(HttpServletRequest request,
				HttpServletResponse response)
				throws ServletException, IOException {

		PrintWriter out = response.getWriter();
		if(!SMAuthenticate.authenticateSMCPCredentials(
				request, 
				response, 
				getServletContext(), 
				SMSystemFunctions.AREditCustomers))
			{
				return;
			}
	    //Get the session info:
	    HttpSession CurrentSession = request.getSession(true);
	    String sDBID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_DATABASE_ID);
	    String sUserID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERID);
	    String sUserFullName = (String)CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERFIRSTNAME) + " "
	    		+ (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERLASTNAME);
	    String sCompanyName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_COMPANYNAME);
	    String sLicenseModuleLevel = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_LICENSE_MODULE_LEVEL);
		//Load the input class from the request object - if it's a 'resubmit', then this will contain
	    //all the values typed from the previous screen.  If it's a 'first time' edit, then this will only
	    //contain the customer number
		ARCustomer cust = new ARCustomer("");
		cust.loadFromHTTPRequest(request);
		//System.out.println("In " + this.toString() + " after load from HTTP request, cust.querystring = " + cust.getQueryString());
	    //First process if it's a 'delete':
	    if(request.getParameter("SubmitDelete") != null){
		    if (request.getParameter("ConfirmDelete") == null){
				response.sendRedirect(
					"" + SMUtilities.getURLLinkBase(getServletContext()) + "smar.AREditCustomers"
					+ "?" + ARCustomer.ParamsCustomerNumber + "=" + cust.getM_sCustomerNumber()
					+ "&Warning=You must check the 'confirming' check box to delete."
					+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
				);
				return;
		    }
		    if (cust.getM_sCustomerNumber().compareToIgnoreCase("") == 0){
				response.sendRedirect(
					"" + SMUtilities.getURLLinkBase(getServletContext()) + "smar.AREditCustomers"
					+ "?" + ARCustomer.ParamsCustomerNumber + "=" + cust.getM_sCustomerNumber()
					+ "&Warning=You must enter a customer code to delete."
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
		    		this.toString() + ".doPost - UserID: " 
		    		+ sUserID
		    		+ " - "
		    		+ sUserFullName
		    			);
		    	if(conn == null){
    				response.sendRedirect(
        					"" + SMUtilities.getURLLinkBase(getServletContext()) + "smar.AREditCustomers"
        					+ "?" + ARCustomer.ParamsCustomerNumber + "=" + cust.getM_sCustomerNumber()
        					+ "&Warning=Error deleting customer."
        					+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
        				);
    						return;
		    	}
		    	clsDatabaseFunctions.start_data_transaction(conn);
			    if (!cust.delete(cust.getM_sCustomerNumber(), conn)){
			    	clsDatabaseFunctions.rollback_data_transaction(conn);
			    	clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1547067536]");
    				response.sendRedirect(
    					"" + SMUtilities.getURLLinkBase(getServletContext()) + "smar.AREditCustomers"
    					+ "?" + ARCustomer.ParamsCustomerNumber + "=" + cust.getM_sCustomerNumber()
    					+ "&Warning=Error deleting customer - " + cust.getErrorMessageString()
    					+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
    				);
					return;
			    }else{
			    	clsDatabaseFunctions.commit_data_transaction(conn);
			    	clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1547067537]");
    				response.sendRedirect(
    					"" + SMUtilities.getURLLinkBase(getServletContext()) + "smar.AREditCustomers"
    					+ "?" + ARCustomer.ParamsCustomerNumber + "=" + cust.getM_sCustomerNumber()
    					+ "&Status=Successfully deleted customer " + cust.getM_sCustomerNumber() + "."
    					+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
    				);
					return;
			    }
		    }
	    }
	    
		String title = "";
		String subtitle = "";
		
		if(request.getParameter("SubmitAdd") != null){
			cust.setM_sCustomerNumber("");
			cust.setM_bNewRecord("1");
		}
		//If we are coming here for the first time to edit (i.e., it's not a re-submit), then we need to 
		//load the input class from the database if possible:
		if(request.getParameter("SubmitEdit") != null){
	    	if(!cust.load(getServletContext(), sDBID)){
				response.sendRedirect(
					"" + SMUtilities.getURLLinkBase(getServletContext()) + "smar.AREditCustomers"
					+ "?" + ARCustomer.ParamsCustomerNumber + "=" + cust.getM_sCustomerNumber()
					+ "&Warning=Could not load customer " + cust.getM_sCustomerNumber() + "."
					+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
				);
					return;
	    	}
		}
		
		//If we are coming here from the same screen to edit a different customer, then we also need to 
		//load the input class from the database if possible:
		if(request.getParameter("SubmitEditDifferent") != null){
	    	if(!cust.load(getServletContext(), sDBID)){
				response.sendRedirect(
					"" + SMUtilities.getURLLinkBase(getServletContext()) + "smar.AREditCustomers"
					+ "?" + ARCustomer.ParamsCustomerNumber + "=" + cust.getM_sCustomerNumber()
					+ "&Warning=Could not load customer " + cust.getM_sCustomerNumber() + "."
					+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
				);
					return;
	    	}
		}
		
		//In any other case, such as the possibility that this is a 'resubmit', we need to edit the customer:
    	title = "Edit " + sObjectName;
	    subtitle = "";
	    out.println(SMUtilities.SMCPTitleSubBGColor(title, subtitle, SMUtilities.getInitBackGroundColor(getServletContext(), sDBID), sCompanyName));
	    out.println(SMUtilities.getDatePickerIncludeString(getServletContext()));
	    
		//If there is a warning from trying to input previously, print it here:
		String sWarning = ARUtilities.get_Request_Parameter("Warning", request);
	    if (! sWarning.equalsIgnoreCase("")){
			out.println("<B><FONT COLOR=\"RED\">WARNING: " + sWarning + "</FONT></B><BR>");
		}
		String sStatus = ARUtilities.get_Request_Parameter("Status", request);
	    if (! sStatus.equalsIgnoreCase("")){
			out.println("<B>STATUS: " + sStatus + "</B><BR>");
		}
	    out.println("<TABLE BORDER=0 WIDTH=100%>");
	    
	    //Print a link to the first page after login:
	    out.println("<TR>");
	    out.println("<TD>");
	    out.println("<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMUserLogin?" 
				+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
				+ "\">Return to user login</A><BR>");
	    out.println("<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smar.ARMainMenu?" 
				+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
				+ "\">Return to Accounts Receivable Main Menu</A><BR>");
	    if (SMSystemFunctions.isFunctionPermitted(
	    	SMSystemFunctions.SMEditSalesContacts, 
	    	sUserID, 
	    	getServletContext(), 
	    	sDBID,
	    	sLicenseModuleLevel)){
		    out.println("<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMSalesContactEdit?id=-1"
		    	+ "&" + "SelectedCustomer=" + clsServletUtilities.URLEncode(cust.getM_sCustomerNumber())
				+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
				+ "\">Create a new sales contact for this customer</A><BR>");
	    }
	    out.println("<A HREF=\"" + WebContextParameters.getdocumentationpageURL(getServletContext()) + "#" + Long.toString(SMSystemFunctions.AREditCustomers) + "\">Summary</A>");
	    out.println("</TD>");
	    
	    //Create a form for editing a different customer:
	    out.println("<TD ALIGN=RIGHT>");
	    out.println("<FORM ACTION='" + SMUtilities.getURLLinkBase(getServletContext()) + "smar.AREditCustomersEdit' METHOD='POST'>");
	    out.println("<INPUT TYPE=HIDDEN NAME='" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "' VALUE='" + sDBID + "'>");
	    out.println("<INPUT TYPE=TEXT NAME=\"" + ARCustomer.ParamsCustomerNumber + "\" SIZE=14 MAXLENGTH=" + Integer.toString(SMTablearcustomer.sCustomerNumberLength) + " STYLE=\"width: 1.75in; height: 0.25in\">&nbsp;");
	    out.println("<INPUT TYPE=SUBMIT NAME='SubmitEditDifferent' VALUE=\"Update different Customer\" STYLE='height: 0.24in'>");
	    out.println("</FORM>");
	    out.println("<TD>");
	    out.println("</TR>");
	    out.println("</TABLE>");

	    //System.out.println("In " + this.toString() + " before edit_record, cust.querystring = " + cust.getQueryString());
	    Edit_Record(request, cust, out, sDBID, sUserFullName, sUserID, sLicenseModuleLevel);
		
		out.println("</BODY></HTML>");
	}
	
	private void Edit_Record(
			HttpServletRequest request,
			ARCustomer cust, 
			PrintWriter pwOut, 
			String sDBID,
			String sUserFullName,
			String sUserID,
			String sLicenseModLevel
			){
		
		//We'll use these arrays for the salesperson list, which we'll need more than once:
        ArrayList<String> sSalespersonCodes = new ArrayList<String>();
        ArrayList<String> sSalespersonDescriptions = new ArrayList<String>();
        
		pwOut.println("<FORM NAME='MAINFORM' ACTION='" + SMUtilities.getURLLinkBase(getServletContext()) + "smar." + sCalledClassName + "' METHOD='POST'>");
		pwOut.println("<INPUT TYPE=HIDDEN NAME='" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "' VALUE='" + sDBID + "'>");
		if(cust.getM_iNewRecord().compareToIgnoreCase("0") == 0){
			pwOut.println("<INPUT TYPE=HIDDEN NAME=\"" + ARCustomer.ParamsCustomerNumber + "\" VALUE=\"" + cust.getM_sCustomerNumber() + "\">");
			pwOut.println("<INPUT TYPE=HIDDEN NAME=\"" + ARCustomer.ParamsAddingNewRecord + "\" VALUE=0>");
		}else{
			pwOut.println("<INPUT TYPE=HIDDEN NAME=\"" + ARCustomer.ParamsAddingNewRecord + "\" VALUE=1>");
		}
		pwOut.println("<INPUT TYPE=HIDDEN NAME=\"" + ARCustomer.ParamdatLastMaintained + "\" VALUE=\"" + cust.getM_datLastMaintained() + "\">");
		pwOut.println("<INPUT TYPE=HIDDEN NAME=\"" + ARCustomer.ParamsLastEditUserFullName + "\" VALUE=\"" + sUserFullName + "\">");
		pwOut.println("<INPUT TYPE=HIDDEN NAME=\"" + ARCustomer.ParamsLastEditUserID + "\" VALUE=\"" + sUserID + "\">");
		pwOut.println("<INPUT TYPE=HIDDEN NAME=\"" + Paramlastsaveddate + "\" VALUE=\"" + cust.getM_datStartDate() + "\""
				+ " id=\"" + Paramlastsaveddate + "\""
				+ "\">");
		pwOut.println("<INPUT TYPE=HIDDEN NAME=\"" + COMMAND_FLAG + "\" VALUE=\"" + "" + "\"" + "id=\"" + COMMAND_FLAG + "\"" + "\">");
		
		//Store whether or not the record has been changed:
		pwOut.println("<INPUT TYPE=HIDDEN NAME=\"" + RECORDWASCHANGED_FLAG + "\" VALUE=\"" 
				+ clsManageRequestParameters.get_Request_Parameter(RECORDWASCHANGED_FLAG, request ) + "\""
				+ " id=\"" + RECORDWASCHANGED_FLAG + "\"" + ">");
	   
	    if (cust.getsgdoclink().compareToIgnoreCase("") != 0){
	    	pwOut.println("<A HREF=\"" + cust.getsgdoclink() 
				+ "\">Google Drive folder</A>&nbsp;&nbsp;"
		    );
	    }
		pwOut.println("Date last maintained: " + cust.getM_datLastMaintained());
	    pwOut.println(" by user: " + cust.getM_sLastEditUserFullName() + "<BR>");
	    
	    //Add javaScript
	    pwOut.println(sCommandScripts());
	    
	    boolean bUseGoogleDrivePicker = false;
		String sPickerScript = "";
			try {
			 sPickerScript = clsServletUtilities.getDrivePickerJSIncludeString(
						SMCreateGoogleDriveFolderParamDefinitions.AR_CUSTOMER_RECORD_TYPE_PARAM_VALUE,
						cust.getM_sCustomerNumber().replace("\"", "&quot;"),
						getServletContext(),
						sDBID);
			} catch (Exception e) {
				System.out.println("[1554818420] - Failed to load drivepicker.js - " + e.getMessage());
			}
	
			if(sPickerScript.compareToIgnoreCase("") != 0) {
				 pwOut.println(sPickerScript);
				bUseGoogleDrivePicker = true;
			} 
	    
	    pwOut.println("<TABLE BORDER=12 CELLSPACING=2>");
        //Customer number:
	    if(cust.getM_iNewRecord().compareToIgnoreCase("1") == 0){
	        pwOut.println(ARUtilities.Create_Edit_Form_Text_Input_Row(
	        		ARCustomer.ParamsCustomerNumber + "\" ID=\"" + ARCustomer.ParamsCustomerNumber + "\" ONCHANGE=\"flagDirty();", 
	        		cust.getM_sCustomerNumber().replace("\"", "&quot;"),
	        		SMTablearcustomer.sCustomerNumberLength, 
	        		"Customer number<B><FONT COLOR=\"RED\">*</FONT></B>:", 
	        		"Up to " + SMTablearcustomer.sCustomerNumberLength + " characters.",
	        		"1.6"
	        	)
	        );
	    }else{
	    	pwOut.println("<TD ALIGN=RIGHT><B>Customer number:</B></TD><TD>" 
	    			+ cust.getM_sCustomerNumber().replace("\"", "&quot;") + "</TD><TD>&nbsp;</TD>");
	    }
		//Active?
	    int iTrueOrFalse = 1;
	    if (cust.getM_iActive().compareToIgnoreCase("1") == 0){
	    	iTrueOrFalse = 1;
	    }else{
	    	iTrueOrFalse = 0;
	    }
	    pwOut.println(ARUtilities.Create_Edit_Form_Checkbox_Row(
			ARCustomer.ParamiActive + "\" ID=\"" + ARCustomer.ParamiActive + "\" ONCHANGE=\"flagDirty();", 
			iTrueOrFalse,
			"Active customer?", 
			"Uncheck to de-activate this customer."
			)
		);
		//On hold?
	    if (cust.getM_iOnHold().compareToIgnoreCase("1") == 0){
	    	iTrueOrFalse = 1;
	    }else{
	    	iTrueOrFalse = 0;
	    }
	    pwOut.println(ARUtilities.Create_Edit_Form_Checkbox_Row(
    		ARCustomer.ParamiOnHold + "\" ID=\"" + ARCustomer.ParamiOnHold + "\" ONCHANGE=\"flagDirty();", 
    		iTrueOrFalse,
			"On hold?", 
			"Check to put this customer on hold."
			)
		);
	    
		//Uses electronic deposit?
	    if (cust.getM_sUsesElectronicDeposit().compareToIgnoreCase("1") == 0){
	    	iTrueOrFalse = 1;
	    }else{
	    	iTrueOrFalse = 0;
	    }
	    pwOut.println(ARUtilities.Create_Edit_Form_Checkbox_Row(
    		ARCustomer.Paramiuseselectronicdeposit + "\" ID=\"" + ARCustomer.Paramiuseselectronicdeposit + "\" ONCHANGE=\"flagDirty();", 
    		iTrueOrFalse, 
			"Uses electronic deposit?", 
			"Check to indicate that this customer uses electronic deposits."
			)
		);
	    
		//Require statements?
	    if (cust.getM_sRequiresStatements().compareToIgnoreCase("1") == 0){
	    	iTrueOrFalse = 1;
	    }else{
	    	iTrueOrFalse = 0;
	    }
	    pwOut.println(ARUtilities.Create_Edit_Form_Checkbox_Row(
    		ARCustomer.Paramirequiresstatements + "\" ID=\"" + ARCustomer.Paramirequiresstatements + "\" ONCHANGE=\"flagDirty();", 
    		iTrueOrFalse,
			"Requires statements?", 
			"Check to indicate that this customer uses requires statements."
			)
		);
	    
		//Require PO?
	    if (cust.getM_sRequiresPO().compareToIgnoreCase("1") == 0){
	    	iTrueOrFalse = 1;
	    }else{
	    	iTrueOrFalse = 0;
	    }
	    pwOut.println(ARUtilities.Create_Edit_Form_Checkbox_Row(
    		ARCustomer.Paramirequirespo + "\" ID=\"" + ARCustomer.Paramirequirespo + "\" ONCHANGE=\"flagDirty();", 
    		iTrueOrFalse,
			"Requires purchase order?", 
			"Check to indicate that this customer requires purchase orders."
			)
		);
        ArrayList<String> sValues = new ArrayList<String>();
        ArrayList<String> sDescriptions = new ArrayList<String>();
        String sSQL = "";
	    try{
	        //Terms
	        sSQL =  "SELECT " 
	        		+ SMTablearterms.sTermsCode + ", "
	        		+ SMTablearterms.sDescription
	        		+ " FROM " + SMTablearterms.TableName
	        		+ " ORDER BY " + SMTablearterms.sTermsCode;
	        ResultSet rsTerms = clsDatabaseFunctions.openResultSet(
	        	sSQL, 
	        	getServletContext(), 
	        	sDBID,
	        	"MySQL",
	        	this.toString() + ".Edit_Record (1) - User: " + sUserID
	        	+ " - "
	        	+ sUserFullName
	        		);
	        
	        //First, add a blank to make sure the user selects one:
	        sValues.add("");
	        sDescriptions.add("-- Select a set of terms --");
	        while (rsTerms.next()){
	        	sValues.add((String) rsTerms.getString(SMTablearterms.sTermsCode).trim());
	        	sDescriptions.add((String) (rsTerms.getString(SMTablearterms.sTermsCode).trim() + " - " + rsTerms.getString(SMTablearterms.sDescription).trim()));
	        }
	        rsTerms.close();
	        
	        pwOut.println(ARUtilities.Create_Edit_Form_List_Row(
	        		ARCustomer.ParamsTerms + "\" ID=\"" + ARCustomer.ParamsTerms + "\" ONCHANGE=\"flagDirty();", 
	        		sValues, 
	        		cust.getM_sTerms().replace("\"", "&quot;"),  
	        		sDescriptions, 
	        		"Terms<B><FONT COLOR=\"RED\">*</FONT></B>:",
	        		"Set the default terms for this customer."
	        		)
	        );
	        //Customer group
	        sSQL = "SELECT " 
	        		+ SMTablearcustomergroups.sGroupCode + ", "
	        		+ SMTablearcustomergroups.sDescription
	        		+ " FROM " + SMTablearcustomergroups.TableName
	        		+ " ORDER BY " + SMTablearcustomergroups.sGroupCode;
	        ResultSet rsCustomerGroups = clsDatabaseFunctions.openResultSet(
	        	sSQL, 
	        	getServletContext(), 
	        	sDBID,
	        	"MySQL",
	        	this.toString() + ".Edit_Record (2) UserID: " + sUserID);
	        
	        sValues.clear();
	        sDescriptions.clear();
	        //First, add a blank to make sure the user selects one:
	        //sValues.add("");
	        //sDescriptions.add("-- Select a customer group --");
	        while (rsCustomerGroups.next()){
	        	sValues.add((String) rsCustomerGroups.getString(SMTablearcustomergroups.sGroupCode).trim());
	        	sDescriptions.add((String) (rsCustomerGroups.getString(SMTablearcustomergroups.sGroupCode).trim() + " - " + rsCustomerGroups.getString(SMTablearcustomergroups.sDescription).trim()));
	        }
	        rsCustomerGroups.close();

	        pwOut.println(ARUtilities.Create_Edit_Form_List_Row(
	        		ARCustomer.ParamsCustomerGroup + "\" ID=\"" + ARCustomer.ParamsCustomerGroup + "\" ONCHANGE=\"flagDirty();", 
	        		sValues, 
	        		cust.getM_sCustomerGroup().replace("\"", "&quot;"), 
	        		sDescriptions, 
	        		"Customer group<B><FONT COLOR=\"RED\">*</FONT></B>:",
	        		"Select a group for this customer."
	        	)
	        );
	        //Account set
	        sSQL =  "SELECT *" 
	        		+ " FROM " + SMTablearacctset.TableName
	        		+ " ORDER BY " + SMTablearacctset.sAcctSetCode;
	        ResultSet rsAcctSets = clsDatabaseFunctions.openResultSet(
	        	sSQL, 
	        	getServletContext(), 
	        	sDBID,
	        	"MySQL",
	        	this.toString() + ".Edit_Record (3) - UserID: " + sUserID);
	        
	        sValues.clear();
	        sDescriptions.clear();
	        //First, add a blank to make sure the user selects one:
	        sValues.add("");
	        sDescriptions.add("-- Select an account set --");
	        while (rsAcctSets.next()){
	        	sValues.add((String) rsAcctSets.getString(SMTablearacctset.sAcctSetCode).trim());
	        	sDescriptions.add((String) (rsAcctSets.getString(SMTablearacctset.sAcctSetCode).trim() + " - " + rsAcctSets.getString(SMTablearacctset.sDescription).trim()));
	        }
	        rsAcctSets.close();
	        pwOut.println(ARUtilities.Create_Edit_Form_List_Row(
	        		ARCustomer.ParamsAccountSet + "\" ID=\"" + ARCustomer.ParamsAccountSet + "\" ONCHANGE=\"flagDirty();", 
	        		sValues, 
	        		cust.getM_sAccountSet().replace("\"", "&quot;"), 
	        		sDescriptions, 
	        		"Account set<B><FONT COLOR=\"RED\">*</FONT></B>:",
	        		"Set the account set for this customer."
	        	)
	        );
	        //Price list code
	        sSQL = "SELECT *" 
	        		+ " FROM " + SMTablepricelistcodes.TableName
	        		+ " ORDER BY " + SMTablepricelistcodes.spricelistcode;
	        ResultSet rsPriceListCodes = clsDatabaseFunctions.openResultSet(
	        	sSQL, 
	        	getServletContext(), 
	        	sDBID,
	        	"MySQL",
	        	this.toString() + ".Edit_Record (4) - UserID: " + sUserID);
	        
	        sValues.clear();
	        sDescriptions.clear();
	        //First, add a blank to make sure the user selects one:
	        sValues.add("");
	        sDescriptions.add("-- Select a price list code --");
	        while (rsPriceListCodes.next()){
	        	sValues.add((String) rsPriceListCodes.getString(SMTablepricelistcodes.spricelistcode).trim());
	        	sDescriptions.add((String) (rsPriceListCodes.getString(SMTablepricelistcodes.spricelistcode).trim() 
	        			+ " - " + rsPriceListCodes.getString(SMTablepricelistcodes.sdescription).trim()));
	        }
	        rsPriceListCodes.close();
		}catch (SQLException ex){
			pwOut.println("<BR><FONT COLOR=RED>Error [1454085134] with SQL: '" + sSQL + "' - " + ex.getMessage() + "</FONT><BR>");
		}
        pwOut.println(ARUtilities.Create_Edit_Form_List_Row(
        		ARCustomer.Paramspricelistcode + "\" ID=\"" + ARCustomer.Paramspricelistcode + "\" ONCHANGE=\"flagDirty();", 
        		sValues, 
        		cust.getM_sPriceListCode().replace("\"", "&quot;"), 
        		sDescriptions, 
        		"Price list code<B><FONT COLOR=\"RED\">*</FONT></B>:",
        		"Set the price list code for this customer."
        	)
        );
        
        //Price level
        sValues.clear();
        sDescriptions.clear();
        //First, add a blank to make sure the user selects one:
        //sValues.add("");
        //sDescriptions.add("-- Select a price level --");
        for(int i = 0; i <= 5; i++){
        	sValues.add(Integer.toString(i));
        	if(i == 0){
        		sDescriptions.add("Base Price Level");
        	}else{
        		sDescriptions.add("Price Level " + Integer.toString(i));
        	}
        }
        pwOut.println(ARUtilities.Create_Edit_Form_List_Row(
        		ARCustomer.Paramspricelevel + "\" ID=\"" + ARCustomer.Paramspricelevel + "\" ONCHANGE=\"flagDirty();", 
        		sValues, 
        		cust.getM_sPriceLevel().replace("\"", "&quot;"), 
        		sDescriptions, 
        		"Price level:",
        		"Set the pricing level for this customer."
        	)
        );
        
        //ACTIVE Tax jurisdictions
		sSQL = "SELECT DISTINCT"
				+ " " + SMTabletax.staxjurisdiction
				+ ", " + SMTabletax.staxtype
				+ ", " + SMTabletax.lid
				+ " FROM "
				+ SMTabletax.TableName
				+ " WHERE ("
					+ "(" + SMTabletax.iactive + " = 1)"
				+ ")"
				+ " ORDER BY " + SMTabletax.staxjurisdiction + ", " + SMTabletax.staxtype
				;
		
        try {
			ResultSet rsTaxes = clsDatabaseFunctions.openResultSet(
				sSQL, 
				getServletContext(), 
				sDBID,
				"MySQL",
				this.toString() + ".Edit_Record (5) - UserID: " + sUserID);
			
			sValues.clear();
			sDescriptions.clear();
			//First, add a blank to make sure the user selects one:
			sValues.add("");
			sDescriptions.add("-- Select a default tax --");
			while (rsTaxes.next()){
				sValues.add((String) Long.toString(rsTaxes.getLong(SMTabletax.lid)));
				sDescriptions.add((String) (rsTaxes.getString(SMTabletax.staxjurisdiction).trim() 
						+ " - " + rsTaxes.getString(SMTabletax.staxtype).trim()));
			}
			rsTaxes.close();
		} catch (Exception e) {
			pwOut.println("<BR><FONT COLOR=RED>Error [1454085133] reading tax information with SQL: '" + sSQL + "' - " + e.getMessage() + "</FONT><BR>");
		}
        pwOut.println(ARUtilities.Create_Edit_Form_List_Row(
        		ARCustomer.Paramitaxid + "\" ID=\"" + ARCustomer.Paramitaxid + "\" ONCHANGE=\"flagDirty();", 
        		sValues, 
        		cust.getstaxid().replace("\"", "&quot;"), 
        		sDescriptions, 
        		"Tax type<B><FONT COLOR=\"RED\">*</FONT></B>:",
        		"Set the default tax type for this customer."
        	)
        );

        //Customer name:
		pwOut.println(ARUtilities.Create_Edit_Form_Text_Input_Row(
        		ARCustomer.ParamsCustomerName + "\" ID=\"" + ARCustomer.ParamsCustomerName + "\" ONCHANGE=\"flagDirty();", 
        		cust.getM_sCustomerName().replace("\"", "&quot;"), 
        		SMTablearcustomer.sCustomerNameLength, 
        		"Customer name<B><FONT COLOR=\"RED\">*</FONT></B>:",
        		"Normally the company name, up to " + SMTablearcustomer.sCustomerNameLength + " characters.",
        		"6.4"
        	)
        );
        //Address Line 1:
        pwOut.println(ARUtilities.Create_Edit_Form_Text_Input_Row(
        		ARCustomer.ParamsAddressLine1 + "\" ID=\"" + ARCustomer.ParamsAddressLine1 + "\" ONCHANGE=\"flagDirty();", 
        		cust.getM_sAddressLine1().replace("\"", "&quot;"), 
        		SMTablearcustomer.sAddressLine1Length,
        		"First line of the address, up to " + SMTablearcustomer.sAddressLine1Length + " characters.",
        		"6.4"
        		)
        );
        //Address Line 2:
        pwOut.println(ARUtilities.Create_Edit_Form_Text_Input_Row(
        		ARCustomer.ParamsAddressLine2 + "\" ID=\"" + ARCustomer.ParamsAddressLine2 + "\" ONCHANGE=\"flagDirty();", 
        		cust.getM_sAddressLine2().replace("\"", "&quot;"), 
        		SMTablearcustomer.sAddressLine2Length, 
        		"Address Line 2:",
        		"Second line of the address, up to " + SMTablearcustomer.sAddressLine2Length + " characters.",
        		"6.4"
        		)
        );
        //Address Line 3:
        pwOut.println(ARUtilities.Create_Edit_Form_Text_Input_Row(
        		ARCustomer.ParamsAddressLine3 + "\" ID=\"" + ARCustomer.ParamsAddressLine3 + "\" ONCHANGE=\"flagDirty();", 
        		cust.getM_sAddressLine3().replace("\"", "&quot;"), 
        		SMTablearcustomer.sAddressLine3Length, 
        		"Address Line 3:",
        		"Third line of the address, up to " + SMTablearcustomer.sAddressLine3Length + " characters.",
        		"6.4"
        		)
        );
        //Address Line 4:
        pwOut.println(ARUtilities.Create_Edit_Form_Text_Input_Row(
        		ARCustomer.ParamsAddressLine4 + "\" ID=\"" + ARCustomer.ParamsAddressLine4 + "\" ONCHANGE=\"flagDirty();", 
        		cust.getM_sAddressLine4().replace("\"", "&quot;"), 
        		SMTablearcustomer.sAddressLine4Length, 
        		"Address Line 4:",
        		"Fourth line of the address, up to " + SMTablearcustomer.sAddressLine4Length + " characters.",
        		"6.4"
        		)
        );
        //City:
        pwOut.println(ARUtilities.Create_Edit_Form_Text_Input_Row(
        		ARCustomer.ParamsCity + "\" ID=\"" + ARCustomer.ParamsCity + "\" ONCHANGE=\"flagDirty();", 
        		cust.getM_sCity().replace("\"", "&quot;"),  
        		SMTablearcustomer.sCityLength, 
        		"City:",
        		"Name of city, up to " + SMTablearcustomer.sCityLength + " characters.",
        		"6.4"
        	)
		);
        //State:
        pwOut.println(ARUtilities.Create_Edit_Form_Text_Input_Row(
        		ARCustomer.ParamsState + "\" ID=\"" + ARCustomer.ParamsState + "\" ONCHANGE=\"flagDirty();", 
        		cust.getM_sState().replace("\"", "&quot;"), 
        		SMTablearcustomer.sStateLength, 
        		"State Or Province:",
        		"Up to " + SMTablearcustomer.sStateLength + " characters.",
        		"3.2"
        	)
		);
        //Country:
        pwOut.println(ARUtilities.Create_Edit_Form_Text_Input_Row(
        		ARCustomer.ParamsCountry + "\" ID=\"" + ARCustomer.ParamsCountry + "\" ONCHANGE=\"flagDirty();", 
        		cust.getM_sCountry().replace("\"", "&quot;"), 
        		SMTablearcustomer.sCountryLength, 
        		"Country:",
        		"Up to " + SMTablearcustomer.sCountryLength + " characters.",
        		"6.4"
        	)
		);
        //Postal code:
        pwOut.println(ARUtilities.Create_Edit_Form_Text_Input_Row(
        		ARCustomer.ParamsPostalCode + "\" ID=\"" + ARCustomer.ParamsPostalCode + "\" ONCHANGE=\"flagDirty();", 
        		cust.getM_sPostalCode().replace("\"", "&quot;"), 
        		SMTablearcustomer.sPostalCodeLength, 
        		"Zip Or Postal Code:",
        		"Up to " + SMTablearcustomer.sPostalCodeLength + " characters.",
        		"3.2"
        	)
		);
        //Contact name:
        pwOut.println(ARUtilities.Create_Edit_Form_Text_Input_Row(
        		ARCustomer.ParamsContactName + "\" ID=\"" + ARCustomer.ParamsContactName + "\" ONCHANGE=\"flagDirty();", 
        		cust.getM_sContactName().replace("\"", "&quot;"),  
        		SMTablearcustomer.sContactNameLength, 
        		"Contact name:",
        		"Up to " + SMTablearcustomer.sContactNameLength + " characters.",
        		"6.4"
        	)
		);
        //Phone:
        pwOut.println(ARUtilities.Create_Edit_Form_Text_Input_Row(
        		ARCustomer.ParamsPhoneNumber + "\" ID=\"" + ARCustomer.ParamsPhoneNumber + "\" ONCHANGE=\"flagDirty();", 
        		cust.getM_sPhoneNumber().replace("\"", "&quot;"), 
        		SMTablearcustomer.sPhoneNumberLength, 
        		"Phone<B><FONT COLOR=\"RED\">*</FONT></B>:",
        		"Up to " + SMTablearcustomer.sPhoneNumberLength + " characters.",
        		"3.2"
        	)
		);
        //Fax:
        pwOut.println(ARUtilities.Create_Edit_Form_Text_Input_Row(
        		ARCustomer.ParamsFaxNumber + "\" ID=\"" + ARCustomer.ParamsFaxNumber + "\" ONCHANGE=\"flagDirty();", 
        		cust.getM_sFaxNumber().replace("\"", "&quot;"),  
        		SMTablearcustomer.sFaxNumberLength, 
        		"Fax:",
        		"Up to " + SMTablearcustomer.sFaxNumberLength + " characters.",
        		"3.2"
        	)
		);
        //Email address:
        pwOut.println(ARUtilities.Create_Edit_Form_Text_Input_Row(
        		ARCustomer.ParamsEmailAddress + "\" ID=\"" + ARCustomer.ParamsEmailAddress + "\" ONCHANGE=\"flagDirty();", 
        		cust.getM_sEmailAddress().replace("\"", "&quot;"), 
        		SMTablearcustomer.sEmailAddressLength, 
        		"Email:",
        		"The company email address, up to " + SMTablearcustomer.sEmailAddressLength + " characters.",
        		"6.4"
        	)
       	);
        //Web address:
        pwOut.println(ARUtilities.Create_Edit_Form_Text_Input_Row(
        		ARCustomer.ParamsWebAddress + "\" ID=\"" + ARCustomer.ParamsWebAddress + "\" ONCHANGE=\"flagDirty();", 
        		cust.getM_sWebAddress().replace("\"", "&quot;"), 
        		SMTablearcustomer.sWebAddressLength, 
        		"Web address:",
        		"The company web address, up to " + SMTablearcustomer.sWebAddressLength + " characters.",
        		"6.4"
        	)
       	);
        //Credit limit:
        pwOut.println(ARUtilities.Create_Edit_Form_Text_Input_Row(
        		ARCustomer.ParamdCreditLimit + "\" ID=\"" + ARCustomer.ParamdCreditLimit + "\" ONCHANGE=\"flagDirty();", 
        		cust.getM_dCreditLimit().replace("\"", "&quot;"), 
        		18, 
        		"Credit limit:",
        		"Maximum credit allowed.",
        		"3.2"
        	)
		);

        //Accounting Notes:
        pwOut.println(ARUtilities.Create_Edit_Form_MultilineText_Input_Row(
        		ARCustomer.ParammAccountingNotes + "\" ID=\"" + ARCustomer.ParammAccountingNotes + "\" ONCHANGE=\"flagDirty();", 
        		cust.getM_mAccountingNotes().replace("\"", "&quot;"),
        		"Accounting notes:", 
        		"Does not print - used for internal notes", 
        		3, 
        		55
        	)
       	);
        //Customer comments:
        pwOut.println(ARUtilities.Create_Edit_Form_MultilineText_Input_Row(
        		ARCustomer.ParammCustomerComments + "\" ID=\"" + ARCustomer.ParammCustomerComments + "\" ONCHANGE=\"flagDirty();",
        		cust.getM_mCustomerComments().replace("\"", "&quot;"),
        		"Customer comments:", 
        		"Typically used for A/R or Collection remarks", 
        		3, 
        		55
        	)
       	);
        //Start Date:
        pwOut.println(Create_Edit_Form_DateText_Input_Row(
        		ARCustomer.ParamdatStartDate,
        		ARCustomer.ParamdatStartDate + "\" ID=\"" + ARCustomer.ParamdatStartDate + "\" ONCHANGE=\"flagDirty();", 
        		cust.getM_datStartDate().replace("\"", "&quot;"),  
        		10, 
        		"Start date:", 
        		"Date customer started (in mm/dd/yyyy format).",
        		"1",
        		getServletContext()
        	)
		);
        
        
        //Invoicing Contact
        pwOut.println(ARUtilities.Create_Edit_Form_Text_Input_Row(
        		ARCustomer.Paramsinvoicingcontact + "\" ID=\"" + ARCustomer.Paramsinvoicingcontact + "\" ONCHANGE=\"flagDirty();", 
        		cust.getsinvoicingcontact().replace("\"", "&quot;"),  
        		SMTablearcustomer.sInvoicingContactLength, 
        		"Invoicing contact name:",
        		"Up to " + SMTablearcustomer.sInvoicingContactLength + " characters.",
        		"6.4"
        	)
		);
        
        //Invoicing Email
        pwOut.println(ARUtilities.Create_Edit_Form_Text_Input_Row(
        		ARCustomer.Paramsinvoicingemail + "\" ID=\"" + ARCustomer.Paramsinvoicingemail + "\" ONCHANGE=\"flagDirty();", 
        		cust.getsinvoicingemail().replace("\"", "&quot;"), 
        		SMTablearcustomer.sInvoicingEmailLength, 
        		"Invoicing email:",
        		"The invoicing email address, up to " + SMTablearcustomer.sInvoicingEmailLength + " characters.",
        		"6.4"
        	)
       	);
        
        //Invoicing Instructions
        pwOut.println(ARUtilities.Create_Edit_Form_MultilineText_Input_Row(
        		ARCustomer.Paramsinvoicingnotes + "\" ID=\"" + ARCustomer.Paramsinvoicingnotes + "\" ONCHANGE=\"flagDirty();", 
        		cust.getsinvoicingnotes().replace("\"", "&quot;"),
        		"Invoicing instructions:", 
        		"Does not print - used for internal invoicing notes", 
        		3, 
        		55
        	)
       	);
        
        //Create a list for the default sales group salesperson:
        //Salesperson
        sSQL = SMMySQLs.Get_Salesperson_List_SQL();
        try {
			ResultSet rsSalespersons = clsDatabaseFunctions.openResultSet(
				sSQL, 
				getServletContext(), 
				sDBID,
				"MySQL",
				this.toString() + ".Edit_Record (7) - UserID: " + sUserID);

			sSalespersonCodes.clear();
			sSalespersonDescriptions.clear();
			//First, add a blank to make sure the user selects one:
			sSalespersonCodes.add("");
			sSalespersonDescriptions.add("Not assigned (N/A)");
			while (rsSalespersons.next()){
				sSalespersonCodes.add((String) rsSalespersons.getString(SMTablesalesperson.sSalespersonCode).trim());
				sSalespersonDescriptions.add(
						(String) 
						(rsSalespersons.getString(SMTablesalesperson.sSalespersonCode).trim() 
								+ " - " + rsSalespersons.getString(SMTablesalesperson.sSalespersonFirstName).trim() 
								+ " " + rsSalespersons.getString(SMTablesalesperson.sSalespersonLastName).trim()));
			}
			rsSalespersons.close();
		} catch (SQLException e1) {
			pwOut.println("<FONT COLOR=RED>Error reading salespersons using SQL: " + sSQL + " - " + e1.getMessage());
		}
        
        pwOut.println("<TR><TD><B>Default salespersons by sales group:</B></TD><TD COLSPAN=2>");
        
        //Get a list of salesgroups here:
        /* 
        String SQL = "SELECT"
        	+ " * FROM " + SMTablesalesgroups.TableName
        	+ " LEFT JOIN " + SMTabledefaultsalesgroupsalesperson.TableName
        	+ " ON " + SMTablesalesgroups.TableName + "." + SMTablesalesgroups.iSalesGroupId
        	+ " = " + SMTabledefaultsalesgroupsalesperson.TableName + "." + SMTabledefaultsalesgroupsalesperson.ssalesgroupid
        	+ " WHERE ("
        		+ "(" + SMTabledefaultsalesgroupsalesperson.TableName + "." + SMTabledefaultsalesgroupsalesperson.scustomercode
        		+ " = '" + cust.getM_sCustomerNumber() + "')"
        		+ " OR (" + SMTabledefaultsalesgroupsalesperson.TableName + "." + SMTabledefaultsalesgroupsalesperson.scustomercode
        		+ " IS NULL)"
       		+ ")"
        	+ " ORDER BY " + SMTablesalesgroups.TableName + "." + SMTablesalesgroups.sSalesGroupCode
        ;
         */
        String SQL = "SELECT"
            	+ " * FROM " + SMTablesalesgroups.TableName
            	+ " ORDER BY " + SMTablesalesgroups.TableName + "." + SMTablesalesgroups.sSalesGroupCode
            ;

        if (bDebugMode){
        	System.out.println("In " + this.toString() + ".getsalesgroups - SQL: " + SQL); 
         }
        try {
			ResultSet rsSalesGroups = clsDatabaseFunctions.openResultSet(
			    	SQL, 
			    	getServletContext(), 
			    	sDBID,
			    	"MySQL",
			    	this.toString() + ".Edit_Record.get_salesgroups - UserID: " + sUserID
			    	+ " - "
			    	+ sUserFullName
					);
			while (rsSalesGroups.next()){
				pwOut.println("Salesperson for the " + rsSalesGroups.getString(SMTablesalesgroups.TableName 
						+ "." + SMTablesalesgroups.sSalesGroupDesc) + " sales group: ");
				String sDefaultSalesperson = "";
				if (cust.getM_sCustomerNumber().compareToIgnoreCase("") != 0){
					sSQL = "SELECT * FROM " + SMTabledefaultsalesgroupsalesperson.TableName
						+ "  WHERE ("
							+ "(" + SMTabledefaultsalesgroupsalesperson.scustomercode + " = '" + cust.getM_sCustomerNumber() + "')"
							+ " AND (" + SMTabledefaultsalesgroupsalesperson.lsalesgroupid + " = " 
							+ Long.toString(rsSalesGroups.getLong(SMTablesalesgroups.iSalesGroupId)) + ")"
						+ ")"
					;
					try {
						ResultSet rsDefaultSalesperson = clsDatabaseFunctions.openResultSet(
								sSQL, 
						    	getServletContext(), 
						    	sDBID,
						    	"MySQL",
						    	this.toString() + ".Edit_Record.get_default_salespersons - UserID: " + sUserID
						    	+ " - "
						    	+ sUserFullName
								);
						
						if (rsDefaultSalesperson.next()){
							sDefaultSalesperson = rsDefaultSalesperson.getString(SMTabledefaultsalesgroupsalesperson.TableName 
								+ "." + SMTabledefaultsalesgroupsalesperson.ssalespersoncode);
							if (sDefaultSalesperson == null){
								sDefaultSalesperson = "";
							}
						}
						rsDefaultSalesperson.close();
					} catch (Exception e) {
						pwOut.println("<FONT COLOR=RED>Error reading default salesperson using SQL: " + sSQL + " - " + e.getMessage());
					}
				}
				pwOut.println(
					ARUtilities.Create_Edit_Form_List_Field(
						CUSTOMER_SALESGROUP_SALESPERSON_FIELD  
						+ (Long.toString(rsSalesGroups.getLong(SMTablesalesgroups.TableName + "." + SMTablesalesgroups.iSalesGroupId))
						+ "\" ID=\"" + CUSTOMER_SALESGROUP_SALESPERSON_FIELD  
						+ (Long.toString(rsSalesGroups.getLong(SMTablesalesgroups.TableName + "." + SMTablesalesgroups.iSalesGroupId))) 
						+ "\" ONCHANGE=\"flagDirty();"), 
						sSalespersonCodes, 
						sDefaultSalesperson, 
						sSalespersonDescriptions)
						+ "<BR>"
				);
			}
        } catch (SQLException e) {
			pwOut.println("<FONT COLOR=RED>Error reading salesgroups using SQL: " + SQL + " - " + e.getMessage());
		} 

        pwOut.println("</TD></TR>");
        
        pwOut.println("</TABLE>");
        
        String sCreateAndUploadButton = "";
		if (
			SMSystemFunctions.isFunctionPermitted(
				SMSystemFunctions.SMCreateGDriveARFolders, 
				sUserID, 
				getServletContext(), 
				sDBID,
				sLicenseModLevel
			)
			&& (cust.getM_iNewRecord().compareToIgnoreCase("1") != 0)
		){
			sCreateAndUploadButton = createAndUploadFolderButton(bUseGoogleDrivePicker);
		}
		
		pwOut.println("<B>Document folder link:</B>&nbsp;"
				+ sCreateAndUploadButton + "<BR>"
				+ "<INPUT TYPE=TEXT NAME=\"" + ARCustomer.Paramsgdoclink + "\""
				+ " VALUE=\"" + cust.getsgdoclink().replace("\"", "&quot;") + "\""
				+ "SIZE=" + "125"
				+ " MAXLENGTH=" + Integer.toString(254)
				+ "<BR>"
			);
        pwOut.println("<P>"+ createUpdateButton() + "</P>");
        pwOut.println("</FORM>");
		
	}
	
	private String Create_Edit_Form_DateText_Input_Row (
			  String sFieldName,
			  String sID,
			  String sValue,
			  int iFieldLength,
			  String sLabel,
			  String sRemark,
			  String sTextBoxWidth,
			  ServletContext context
			  ){
				
		        String sRow = "<TR>";
		        sRow += "<TD ALIGN=RIGHT><B>" + sLabel  + " </B></TD>";
		        
		        sRow += "<TD ALIGN=LEFT>";
		        sRow += "<INPUT TYPE=TEXT NAME=\"" + sID + "\"";
		        if (sValue != null){
		        	sRow += " VALUE=\"" + sValue + "\"";
		        }
		        else{
		        	sRow += " VALUE=\"\"";
		        }
		        sRow += "SIZE=28";
		        sRow += " MAXLENGTH=" + Integer.toString(iFieldLength);
		        sRow += " STYLE=\"width: " + sTextBoxWidth + " in; height: 0.25in\"";
		        sRow += ">";
		        sRow += SMUtilities.getDatePickerString(sFieldName, context);
		        sRow += "</TD>";
		  		
		        sRow += "<TD ALIGN=LEFT>" + sRemark + "</TD>";
		        sRow += "</TR>";
		  		return sRow;
		  		
			  }  
	private String createUpdateButton(){
		return "<button type=\"button\""
			+ " value=\"" + UPDATE_BUTTON_LABEL + "\""
			+ " name=\"" + UPDATE_BUTTON_LABEL + "\""
			+ " onClick=\"update();\">"
			+ UPDATE_BUTTON_LABEL
			+ "</button>\n"
			;
	}
	
	private String createAndUploadFolderButton(boolean bUseGoogleDrivePicker){
		String sOnClickFunction = "createanduploadfolder()";
		if(bUseGoogleDrivePicker) {
			sOnClickFunction = "loadPicker()";
		}
		
		return "<button type=\"button\""
			+ " value=\"" + CREATE_UPLOAD_FOLDER_BUTTON_LABEL + "\""
			+ " name=\"" + CREATE_UPLOAD_FOLDER_BUTTON_LABEL + "\""
			+ " onClick=\"" + sOnClickFunction + "\">"
			+ CREATE_UPLOAD_FOLDER_BUTTON_LABEL
			+ "</button>\n"
			;
	}
	
	private String sCommandScripts(){
		String s = "";
		
		s += "<NOSCRIPT>\n"
				+ "    <font color=red>\n"
				+ "    <H3>This page requires that JavaScript be enabled to function properly</H3>\n"
				+ "    </font>\n"
				+ "</NOSCRIPT>\n";
		
		s += "<script type='text/javascript'>\n";
		
		s += "window.onbeforeunload = prompttosave;\n";
		
		s += "function prompttosave(){\n"
		//Check to see if the date field was changed and flag the record was changed field.
		+ "   if (document.getElementById(\"" + Paramlastsaveddate + "\").value != " 
			+ "document.getElementById(\"" + ARCustomer.ParamdatStartDate + "\").value){\n"
		+ "        flagDirty();\n"
		+ "    }\n  "
		//Don't prompt on updates
		+ "    if (document.getElementById(\"" + COMMAND_FLAG + "\").value == \"" +  UPDATE_COMMAND_VALUE + "\" ){\n"
		+ "        return;\n"
		+ "    }\n"
		
		+ "    if (document.getElementById(\"" + RECORDWASCHANGED_FLAG + "\").value == \"" 
		+ RECORDWASCHANGED_FLAG_VALUE + "\" ){\n"
		+ "       return 'You have unsaved changes - are you sure you want to leave this page?';\n"
		+ "    }\n"			
		+ "}\n\n";
		
		s += "function flagDirty() {\n"
				+ "    document.getElementById(\"" + RECORDWASCHANGED_FLAG + "\").value = \"" 
					 + RECORDWASCHANGED_FLAG_VALUE + "\";\n"
				+ "}\n";
		
		//Update:
		s += "function update(){\n"
			+ "    document.getElementById(\"" + COMMAND_FLAG + "\").value = \"" 
				 + UPDATE_COMMAND_VALUE + "\";\n"
			+ "    document.forms[\"MAINFORM\"].submit();\n"
			+ "}\n";
		

		//Create and/or upload files:
		//Create folder and/or upload file
		s += "function createanduploadfolder(){\n"
			+ "    document.getElementById(\"" + COMMAND_FLAG + "\").value = \"" 
				+ CREATE_UPLOAD_FOLDER_COMMAND_VALUE + "\";\n"
			+ "    document.forms[\"MAINFORM\"].submit();\n"
			+ "}\n"
		;
		
		s += "</script>\n";
		
		return s;
	}
	
	public void doGet(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
		
		doPost(request, response);
	}
}
