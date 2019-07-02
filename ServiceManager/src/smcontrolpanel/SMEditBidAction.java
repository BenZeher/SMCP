package smcontrolpanel;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import ConnectionPool.WebContextParameters;
import SMClasses.SMOrderHeader;
import SMDataDefinition.SMCreateGoogleDriveFolderParamDefinitions;
import SMDataDefinition.SMTablearcustomer;
import SMDataDefinition.SMTableorderheaders;
import SMDataDefinition.SMTablesalescontacts;
import ServletUtilities.clsServletUtilities;
import ServletUtilities.clsDBServerTime;
import ServletUtilities.clsManageRequestParameters;
import smar.ARCustomer;
import smar.SMOption;

public class SMEditBidAction extends HttpServlet{
	
	private static final long serialVersionUID = 1L;
	public void doPost(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
	
		SMMasterEditAction smaction = new SMMasterEditAction(request, response);
		if (!smaction.processSession(getServletContext(), SMSystemFunctions.SMEditBids)){return;}
	    //Read the entry fields from the request object:
		SMBidEntry entry = new SMBidEntry(request);
		smaction.getCurrentSession().setAttribute("BidEntry", entry);
		String sRedirectString = "";
		
		//Update another lead:
    	if (clsManageRequestParameters.get_Request_Parameter(
    		"COMMANDFLAG", request).compareToIgnoreCase(SMEditBidEntry.UPDATE_ANOTHER_COMMAND_VALUE) == 0){
    		String sLeadID = clsManageRequestParameters.get_Request_Parameter(SMEditBidEntry.UPDATE_ANOTHER_FIELD_NAME, request);
    		if (sLeadID.trim().compareToIgnoreCase("") == 0){
				smaction.getCurrentSession().setAttribute("BidEntry", entry);
				smaction.redirectAction("You chose to update another " + SMBidEntry.ParamObjectName 
					+ ", but did not enter an ID for it.", "", "");
				return;
    		}
			sRedirectString = 
				SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMEditBidEntry"
				+ "?" + SMBidEntry.ParamID + "=" + sLeadID
				+ "&CallingClass=smcontrolpanel.SMEditBidSelect"
				+ "&OriginalCallingClass=smcontrolpanel.SMEditBidSelect"
				+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + smaction.getsDBID()
			;
			//Clear the entry attribute in the session:
			smaction.getCurrentSession().removeAttribute("BidEntry");
			try {
				redirectProcess(sRedirectString, response);
			} catch (Exception e) {
				smaction.getPwOut().println("<HTML>" + e.getMessage() + "</BODY></HTML>");
			}
			return;
    	}
		
		//Clone:
    	String sCurrentDate = SMUtilities.EMPTY_DATE_VALUE;
    	clsDBServerTime st = null;
    	try {
			st = new clsDBServerTime(smaction.getsDBID(), smaction.getUserName(), getServletContext());
			sCurrentDate = st.getCurrentDateTimeInSelectedFormat("M/d/yyyy");
		} catch (Exception e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
    	if (clsManageRequestParameters.get_Request_Parameter(
        	"COMMANDFLAG", request).compareToIgnoreCase(SMEditBidEntry.CLONE_COMMAND_VALUE) == 0){
			sRedirectString = 
				SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMEditBidEntry"
				+ "?SubmitAdd=Y"
				+ "&" + SMBidEntry.Paramdatoriginationdate + "=" + clsServletUtilities.URLEncode(sCurrentDate)
				+ "&" + SMBidEntry.Paramemailaddress + "=" + clsServletUtilities.URLEncode(entry.getemailaddress())
				+ "&" + SMBidEntry.Paramisalescontactid + "=" + clsServletUtilities.URLEncode(entry.getisalescontactid())
				+ "&" + SMBidEntry.Paramsaltphonenumber + "=" + clsServletUtilities.URLEncode(entry.getsaltphonenumber())
				+ "&" + SMBidEntry.Paramscontactname + "=" + clsServletUtilities.URLEncode(entry.getscontactname())
				+ "&" + SMBidEntry.Paramscustomername + "=" + clsServletUtilities.URLEncode(entry.getscustomername())
				+ "&" + SMBidEntry.Paramsfaxnumber + "=" + clsServletUtilities.URLEncode(entry.getsfaxnumber())
				+ "&" + SMBidEntry.Paramsphonenumber + "=" + clsServletUtilities.URLEncode(entry.getsphonenumber())
				+ "&" + SMBidEntry.Paramssalespersoncode + "=" + clsServletUtilities.URLEncode(entry.getssalespersoncode())
				+ "&CallingClass=smcontrolpanel.SMEditBidEntry"
				+ "&OriginalCallingClass=smcontrolpanel.SMEditBidSelect"
				+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + smaction.getsDBID()
			;
			//Clear the entry attribute in the session:
			smaction.getCurrentSession().removeAttribute("BidEntry");
			try {
				redirectProcess(sRedirectString, response);
			} catch (Exception e) {
				smaction.getPwOut().println("<HTML>" + e.getMessage() + "</BODY></HTML>");
			}
			return;
       	}
		

		
		//Find customer
    	if (clsManageRequestParameters.get_Request_Parameter(
    			"COMMANDFLAG", request).compareToIgnoreCase(SMEditBidEntry.FIND_CUSTOMER_COMMAND_VALUE) == 0){
			sRedirectString = 
				SMUtilities.getURLLinkBase(getServletContext()) + "smar.ObjectFinder"
				+ "?" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + smaction.getsDBID()
				+ "&ObjectName=Customer"
				+ "&ResultClass=FinderResults"
				+ "&SearchingClass=smcontrolpanel.SMEditBidEntry"
				+ "&ReturnField=" + SMOrderHeader.ParamsCustomerCode
				+ "&SearchField1=" + SMTablearcustomer.sCustomerName
				+ "&SearchFieldAlias1=Name"
				+ "&SearchField2=" + SMTablearcustomer.sCustomerNumber
				+ "&SearchFieldAlias2=Customer%20Code"
				+ "&SearchField3=" + SMTablearcustomer.sAddressLine1
				+ "&SearchFieldAlias3=Address%20Line%201"
				+ "&SearchField4=" + SMTablearcustomer.sPhoneNumber
				+ "&SearchFieldAlias4=Phone"
				+ "&ResultListField1="  + SMTablearcustomer.sCustomerNumber
				+ "&ResultHeading1=Customer%20Number"
				+ "&ResultListField2="  + SMTablearcustomer.sCustomerName
				+ "&ResultHeading2=Customer%20Name"
				+ "&ResultListField3="  + SMTablearcustomer.sAddressLine1
				+ "&ResultHeading3=Address%20Line%201"
				+ "&ResultListField4="  + SMTablearcustomer.sPhoneNumber
				+ "&ResultHeading4=Phone"
				+ "&ResultListField5="  + SMTablearcustomer.iActive
				+ "&ResultHeading5=Active"
				+ "&ResultListField6="  + SMTablearcustomer.iOnHold
				+ "&ResultHeading6=On%20Hold"
				+ "&ResultListField7="  + SMTablearcustomer.sCustomerGroup
				+ "&ResultHeading7=Customer%20Group"
				//+ "&ParameterString=*" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + smaction.getsDBID() 
				+ "&ParameterString=*" + SMEditBidEntry.CUSTOMER_SEARCH + "=yes"
				+ "*" + SMBidEntry.ParamID + "=" + entry.slid()
				+ "*" + "&" + SMEditBidEntry.RECORDWASCHANGED_FLAG + "=" + clsManageRequestParameters.get_Request_Parameter(SMEditOrderEdit.RECORDWASCHANGED_FLAG, request)
				+ "*CallingClass=" + smaction.getCallingClass()
			;
			//Store the detail info we have so far in the session:
			smaction.getCurrentSession().removeAttribute("BidEntry");
			smaction.getCurrentSession().setAttribute("BidEntry", entry);
			try {
				redirectProcess(sRedirectString, response);
			} catch (Exception e) {
				smaction.getPwOut().println("<HTML>" + e.getMessage() + "</BODY></HTML>");
			}
			return;
    	}
		//Add new:
		//ADD_NEW_COMMAND_VALUE + "\";\n"
    	if (clsManageRequestParameters.get_Request_Parameter(
    			"COMMANDFLAG", request).compareToIgnoreCase(SMEditBidEntry.ADD_NEW_COMMAND_VALUE) == 0){
			sRedirectString = 
				SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMEditBidEntry"
				+ "?SubmitAdd=Y"
				+ "&CallingClass=smcontrolpanel.SMEditBidEntry"
				+ "&OriginalCallingClass=smcontrolpanel.SMEditBidSelect"
				+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + smaction.getsDBID()
			;
			//Clear the entry attribute in the session:
			smaction.getCurrentSession().removeAttribute("BidEntry");
			try {
				redirectProcess(sRedirectString, response);
			} catch (Exception e) {
				smaction.getPwOut().println("<HTML>" + e.getMessage() + "</BODY></HTML>");
			}
			return;
    	}
		//Create order:
    	if (clsManageRequestParameters.get_Request_Parameter(
    			"COMMANDFLAG", request).compareToIgnoreCase(SMEditBidEntry.CREATE_ORDER_COMMAND_VALUE) == 0){
    		String sCustomerCode = clsServletUtilities.URLEncode(clsManageRequestParameters.get_Request_Parameter(SMOrderHeader.ParamsCustomerCode, request));
    		//Check if customer is valid
    		ARCustomer cus = new ARCustomer(sCustomerCode);
    		if (!cus.load(getServletContext(), smaction.getsDBID())){
    			smaction.getCurrentSession().setAttribute("BidEntry", entry);
				smaction.redirectAction("Could not load customer '" + sCustomerCode + "' - " + cus.getErrorMessageString(), "", "");
				return;
    		}

    		sRedirectString = SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMEditOrderEdit"
				+ "?" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + smaction.getsDBID()
				+ "&" + SMMasterEditSelect.SUBMIT_ADD_BUTTON_NAME + "=Y"
				+ "&CallingClass=smcontrolpanel.SMEditOrderSelection"
				;
			//Clear the entry attribute in the session:
			smaction.getCurrentSession().removeAttribute("BidEntry");
			//Put the order header object in the session:
			try {
				smaction.getCurrentSession().setAttribute(SMOrderHeader.ParamObjectName, 
					loadNewOrderOrQuote(cus, entry, smaction.getsDBID(), SMTableorderheaders.ORDERTYPE_ACTIVE, smaction.getUserName()));
			} catch (Exception e1) {
				smaction.getCurrentSession().setAttribute("BidEntry", entry);
				smaction.redirectAction("Could not create order: " + e1.getMessage(), "", "");
				return;
			}
			try {
				redirectProcess(sRedirectString, response);
			} catch (Exception e) {
				smaction.getPwOut().println("<HTML>" + e.getMessage() + "</BODY></HTML>");
			}
			return;
    	}
		
		//Create quote:
    	if (clsManageRequestParameters.get_Request_Parameter(
    			"COMMANDFLAG", request).compareToIgnoreCase(SMEditBidEntry.CREATE_QUOTE_COMMAND_VALUE) == 0){
    		String sCustomerCode = clsServletUtilities.URLDecode(clsManageRequestParameters.get_Request_Parameter(SMOrderHeader.ParamsCustomerCode, request));
    		ARCustomer cus = new ARCustomer(sCustomerCode);
    		//Quotes don't need a customer:
    		if (sCustomerCode.compareToIgnoreCase("") != 0){
        		if (!cus.load(getServletContext(), smaction.getsDBID())){
        			smaction.getCurrentSession().setAttribute("BidEntry", entry);
    				smaction.redirectAction("Could not load customer '" + sCustomerCode + "' - " + cus.getErrorMessageString(), "", "");
    				return;
        		}
    		}

    		sRedirectString = SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMEditOrderEdit"
				+ "?" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + smaction.getsDBID()
				+ "&" + SMMasterEditSelect.SUBMIT_ADD_BUTTON_NAME + "=Y"
				+ "&CallingClass=smcontrolpanel.SMEditOrderSelection"
				;
			//Clear the entry attribute in the session:
			smaction.getCurrentSession().removeAttribute("BidEntry");
			//Put the order header object in the session:
			try {
				smaction.getCurrentSession().setAttribute(SMOrderHeader.ParamObjectName, 
					loadNewOrderOrQuote(cus, entry, smaction.getsDBID(), SMTableorderheaders.ORDERTYPE_QUOTE, smaction.getUserName()));
			} catch (Exception e1) {
				smaction.getCurrentSession().setAttribute("BidEntry", entry);
				smaction.redirectAction("Could not create order: " + e1.getMessage(), "", "");
				return;
			}
			try {
				redirectProcess(sRedirectString, response);
			} catch (Exception e) {
				smaction.getPwOut().println("<HTML>" + e.getMessage() + "</BODY></HTML>");
			}
			return;
    	}
				
		//Update:
    	if (clsManageRequestParameters.get_Request_Parameter(
    			"COMMANDFLAG", request).compareToIgnoreCase(SMEditBidEntry.UPDATE_COMMAND_VALUE) == 0){
			if(!entry.save_without_data_transaction(
					getServletContext(), 
					smaction.getsDBID(), 
					smaction.getUserID(),
					smaction.getFullUserName())){
				
				smaction.getCurrentSession().setAttribute("BidEntry", entry);
				smaction.redirectAction("Could not save: " + entry.getErrorMessages(), "", "");
				return;
			}else{
				
				//If the save succeeded, force the called function to reload it by NOT
				//putting the entry object in the current session, but by passing it
				//in the query string instead:
				smaction.getCurrentSession().removeAttribute("BidEntry");
				if (smaction.getOriginalURL().trim().compareToIgnoreCase("") != 0){
					smaction.returnToOriginalURL();
				}else{
					smaction.redirectAction(
						"", 
						"Entry ID: " + entry.slid() + " was successfully updated.",
						SMBidEntry.ParamID + "=" + entry.slid()
					);
				}
			}
	    }
	    
	    //if it is a request for look up sales contact
    	if (clsManageRequestParameters.get_Request_Parameter(
    			"COMMANDFLAG", request).compareToIgnoreCase(SMEditBidEntry.FIND_SALES_CONTACT_COMMAND_VALUE) == 0){
			sRedirectString = 
				"" + SMUtilities.getURLLinkBase(getServletContext()) + "smar.ObjectFinder"
				+ "?" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + smaction.getsDBID()
				+ "&ObjectName=SalesContact"
				+ "&ResultClass=FinderResults"
				+ "&SearchingClass=" + "smcontrolpanel.SMEditBidEntry"
				+ "&ReturnField=" + SMTablesalescontacts.id
				+ "&SearchField1=" + SMTablesalescontacts.scontactname
				+ "&SearchFieldAlias1=Sales%20Contact%20Name"
				+ "&SearchField2=" + SMTablesalescontacts.semailaddress
				+ "&SearchFieldAlias2=Email%20Address"
				+ "&SearchField3=" + SMTablesalescontacts.scustomernumber
				+ "&SearchFieldAlias3=Customer%20Account"
				+ "&SearchField4=" + SMTablesalescontacts.scustomername
				+ "&SearchFieldAlias4=Customer%20Name"
				+ "&SearchField5=" + SMTablesalescontacts.salespersoncode
				+ "&SearchFieldAlias5=Salesperson"
				+ "&ResultListField1="  + SMTablesalescontacts.id
				+ "&ResultHeading1=Sales%20Contact%20ID"
				+ "&ResultListField2="  + SMTablesalescontacts.scontactname
				+ "&ResultHeading2=Sales%20Contact%20Name"
				+ "&ResultListField3="  + SMTablesalescontacts.semailaddress
				+ "&ResultHeading3=Email%20Address"
				+ "&ResultListField4="  + SMTablesalescontacts.scustomernumber
				+ "&ResultHeading4=Customer%20Account"
				+ "&ResultListField5="  + SMTablesalescontacts.scustomername
				+ "&ResultHeading5=Customer%20Name"
				+ "&ResultListField6="  + SMTablesalescontacts.salespersoncode
				+ "&ResultHeading6=Salesperson"
				//+ "&ParameterString=*" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + smaction.getsDBID() 
				+ "&ParameterString=*" + SMEditBidEntry.SALES_CONTACT_ID_SEARCH + "=yes"
				+ "*" + SMBidEntry.ParamID + "=" + entry.slid()
				+ "*" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + smaction.getsDBID()
				+ "*" + "&" + SMEditBidEntry.RECORDWASCHANGED_FLAG + "=" + clsManageRequestParameters.get_Request_Parameter(SMEditOrderEdit.RECORDWASCHANGED_FLAG, request)
				+ "*CallingClass=" + smaction.getCallingClass()
			;
			//Store the detail info we have so far in the session:
			smaction.getCurrentSession().removeAttribute("BidEntry");
			smaction.getCurrentSession().setAttribute("BidEntry", entry);
			try {
				redirectProcess(sRedirectString, response);
			} catch (Exception e) {
				smaction.getPwOut().println("<HTML>" + e.getMessage() + "</BODY></HTML>");
			}
			return;
    	}
    	
       	//If Create And Upload Folder Button was pressed
    	if(clsManageRequestParameters.get_Request_Parameter(
    			SMEditBidEntry.COMMAND_FLAG, request).compareToIgnoreCase(SMEditBidEntry.CREATE_UPLOAD_FOLDER_COMMAND_VALUE) == 0){
    		//Need to get prefix, suffix and Web App URL
    		SMOption opt = new SMOption();
        	try {
				opt.load(smaction.getsDBID(), getServletContext(), smaction.getUserName());
			} catch (Exception e1) {
				smaction.getCurrentSession().setAttribute("BidEntry", entry);
				smaction.redirectAction("Could not create folder or upload files - SMOptions could not be loaded: " + e1.getMessage(), "", "");
				return;
			}

        	String sFolderName = opt.getgdrivesalesleadfolderprefix() + entry.slid() + opt.getgdrivesalesleadfoldersuffix();
        	//Parameters for upload folder web-app
        	//parentfolderid
        	//foldername
        	//returnURL
        	//recordtype
        	//keyvalue
			try {
		  		 sRedirectString = opt.getgdriveuploadfileurl()
		          		+ "?" + SMCreateGoogleDriveFolderParamDefinitions.parentfolderid + "=" + opt.getgdriveorderparentfolderid()
		    				+ "&" + SMCreateGoogleDriveFolderParamDefinitions.foldername + "=" + sFolderName
		          		+ "&" + SMCreateGoogleDriveFolderParamDefinitions.recordtype + "=" + SMCreateGoogleDriveFolderParamDefinitions.SALESLEAD_RECORD_TYPE_PARAM_VALUE
		          		+ "&" + SMCreateGoogleDriveFolderParamDefinitions.keyvalue + "=" + entry.slid()
		          		+ "&" + SMCreateGoogleDriveFolderParamDefinitions.backgroundcolor + "=" + opt.getBackGroundColor()
		          		+ "&" + SMCreateGoogleDriveFolderParamDefinitions.returnURL + "=" + getCreateGDriveReturnURL(request)
		              	;
				redirectProcess(sRedirectString, response);
			} catch (Exception e) {
				smaction.getPwOut().println("<HTML>" + e.getMessage() + "</BODY></HTML>");
			}
			return;
    	}
		return;
	}
	
	private String getCreateGDriveReturnURL(HttpServletRequest req) throws Exception{
		String sTemp = "";
		try {
			sTemp = clsServletUtilities.getServerURL(req, getServletContext());
		}catch(Exception e) {
			throw new Exception("Error [1542748060] "+e.getMessage());
		}
		sTemp += "/" + WebContextParameters.getInitWebAppName(getServletContext()) + "/";
		sTemp += "smcontrolpanel.SMCreateGDriveFolder";
		return sTemp;
	}
	private SMOrderHeader loadNewOrderOrQuote(ARCustomer cus, SMBidEntry saleslead, String sDBID, int iOrderType, String sUser) throws Exception{
		
		SMOrderHeader ord = new SMOrderHeader();
		String sCurrentDate = SMUtilities.EMPTY_DATE_VALUE;
		try {
			clsDBServerTime st = new clsDBServerTime(sDBID, sUser, getServletContext());
			sCurrentDate = st.getCurrentDateTimeInSelectedFormat(SMUtilities.DATE_FORMAT_FOR_DISPLAY);
		} catch (Exception e1) {
			//Nothing here - just leave it blank if something fails here
		}
		
		ord.setM_datOrderDate(sCurrentDate);
		//If the user has chosen a customer, use that customer info to fill in these fields:
		if (cus.getM_sCustomerNumber().compareToIgnoreCase("") != 0){
			ord.setM_iCustomerDiscountLevel(cus.getM_sPriceLevel());
			ord.setM_iOnHold(cus.getM_iOnHold());
			ord.setM_sBillToAddressLine1(cus.getM_sAddressLine1());
			ord.setM_sBillToAddressLine2(cus.getM_sAddressLine2());
			ord.setM_sBillToAddressLine3(cus.getM_sAddressLine3());
			ord.setM_sBillToAddressLine4(cus.getM_sAddressLine4());
			ord.setM_sBillToCity(cus.getM_sCity());
			ord.setM_sBillToCountry(cus.getM_sCountry());
			ord.setM_sBillToName(cus.getM_sCustomerName());
			ord.setM_sBillToState(cus.getM_sState());
			ord.setM_sBillToZip(cus.getM_sPostalCode());
			ord.setM_sCustomerCode(cus.getM_sCustomerNumber());
			ord.setM_sCustomerControlAcctSet(cus.getM_sAccountSet());
			ord.setM_sDefaultPriceListCode(cus.getM_sPriceListCode());
			ord.setM_sTerms(cus.getM_sTerms());
			ord.setM_sInvoicingContact(cus.getsinvoicingcontact());
			ord.setM_sInvoicingNotes(cus.getsinvoicingnotes());
			ord.setM_sInvoicingEmail(cus.getsinvoicingemail());
		//SPECIAL CASE - if the customer has NO address information, then it's probably a 'template' customer, like a resi COD account, 
		//and we'll fill in the address from the ship to:
		if (
			(cus.getM_sAddressLine1().compareToIgnoreCase("") == 0)
			&& (cus.getM_sAddressLine2().compareToIgnoreCase("") == 0)
			&& (cus.getM_sAddressLine3().compareToIgnoreCase("") == 0)
			&& (cus.getM_sAddressLine4().compareToIgnoreCase("") == 0)
			&& (cus.getM_sCity().compareToIgnoreCase("") == 0)
			&& (cus.getM_sState().compareToIgnoreCase("") == 0)
			&& (cus.getM_sPostalCode().compareToIgnoreCase("") == 0)
		){
			ord.setM_sBillToAddressLine1(saleslead.getsshiptoaddress1());
			ord.setM_sBillToAddressLine2(saleslead.getsshiptoaddress2());
			ord.setM_sBillToAddressLine3(saleslead.getsshiptoaddress3());
			ord.setM_sBillToAddressLine4(saleslead.getsshiptoaddress4());
			ord.setM_sBillToCity(saleslead.getsshiptocity());
			ord.setM_sBillToName(saleslead.getscustomername());
			ord.setM_sBillToState(saleslead.getsshiptostate());
			ord.setM_sBillToZip(saleslead.getsshiptozip());
		}
		//Otherwise, use the ship to info to fill in the address info:
		}else{
			ord.setM_sBillToAddressLine1(saleslead.getsshiptoaddress1());
			ord.setM_sBillToAddressLine2(saleslead.getsshiptoaddress2());
			ord.setM_sBillToAddressLine3(saleslead.getsshiptoaddress3());
			ord.setM_sBillToAddressLine4(saleslead.getsshiptoaddress4());
			ord.setM_sBillToCity(saleslead.getsshiptocity());
			ord.setM_sBillToName(saleslead.getscustomername());
			ord.setM_sBillToState(saleslead.getsshiptostate());
			ord.setM_sBillToZip(saleslead.getsshiptozip());
		}
		
		ord.setM_iOrderSourceID(saleslead.getiordersourceid());
		ord.setM_iOrderType(Integer.toString(iOrderType));
		ord.setM_sBilltoContact(saleslead.getscontactname());
		if(cus.getM_sFaxNumber().compareToIgnoreCase("") != 0){
			ord.setM_sBillToFax(cus.getM_sFaxNumber());
		}else{
			ord.setM_sBillToFax(saleslead.getsfaxnumber());
		}
		if (cus.getM_sPhoneNumber().compareToIgnoreCase("") != 0){
			ord.setM_sBilltoPhone(cus.getM_sPhoneNumber());
		}else{
			ord.setM_sBilltoPhone(saleslead.getsphonenumber());
		}
		if (cus.getM_sEmailAddress().compareToIgnoreCase("") != 0){
			ord.setM_sEmailAddress(cus.getM_sEmailAddress());
		}else{
			ord.setM_sEmailAddress(saleslead.getemailaddress());
		}
		ord.setM_sSalesperson(saleslead.getssalespersoncode());
		ord.setM_ssecondarybilltophone(saleslead.getsaltphonenumber());
		ord.setM_sShipToAddress1(saleslead.getsshiptoaddress1());
		ord.setM_sShipToAddress2(saleslead.getsshiptoaddress2());
		ord.setM_sShipToAddress3(saleslead.getsshiptoaddress3());
		ord.setM_sShipToAddress4(saleslead.getsshiptoaddress4());
		ord.setM_sShipToCity(saleslead.getsshiptocity());
		ord.setM_sShipToName(saleslead.getsprojectname());
		ord.setM_sShipToState(saleslead.getsshiptostate());
		ord.setM_sShipToZip(saleslead.getsshiptostate());
		ord.setM_sShipToZip(saleslead.getsshiptozip());
		ord.setsBidID(saleslead.getlid());
		ord.setM_iSalesGroup(saleslead.getlsalesgroupid());
		
		//If we need to copy the sales lead Google Drive folder URL, do that, also:
		SMOption opt = new SMOption();
		try {
			opt.load(sDBID, getServletContext(), sUser);
		} catch (Exception e) {
			throw new Exception("Error [1441741643] reading SM Options to copy Doc folder URL - " + opt.getErrorMessage());
		}
		
		if (opt.getcopysalesleadfolderurltoorder().compareToIgnoreCase("1") == 0){
			ord.setM_sGDocLink(saleslead.getsgdoclink());
		}
		return ord;
	}
	private void redirectProcess(String sRedirectString, HttpServletResponse res ) throws Exception{
		try {
			res.sendRedirect(sRedirectString);
		} catch (IOException e1) {
			throw new Exception("Error [1397679126] In " + SMUtilities.getFullClassName(this.toString()) 
					+ ".redirectAction - IOException error redirecting with string: "
					+ sRedirectString + " - " + e1.getMessage());
		} catch (IllegalStateException e1) {
			throw new Exception("Error [1397679127] In " + SMUtilities.getFullClassName(this.toString()) 
					+ ".redirectAction - IllegalStateException error redirecting with string: "
					+ sRedirectString + " - " + e1.getMessage());
		}
	}
	public void doGet(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
		
		doPost(request, response);
	}
}