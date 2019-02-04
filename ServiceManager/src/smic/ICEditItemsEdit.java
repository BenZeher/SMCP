package smic;

import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import smcontrolpanel.SMAuthenticate;
import smcontrolpanel.SMSystemFunctions;
import smcontrolpanel.SMUtilities;
import ConnectionPool.WebContextParameters;
import SMDataDefinition.SMTableiccategories;
import SMDataDefinition.SMTableicitemlocations;
import SMDataDefinition.SMTableicitems;
import SMDataDefinition.SMTablelocations;
import SMDataDefinition.SMTablepricelistcodes;
import ServletUtilities.clsCreateHTMLTableFormFields;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsManageRequestParameters;

public class ICEditItemsEdit extends HttpServlet {

	private static final long serialVersionUID = 1L;
	private static final String sItemObjectName = "Item";
	private static final String sICEditItemsEditCalledClassName = "ICEditItemsAction";

	private boolean bCreatingAnItemFromPO;
	public void doPost(HttpServletRequest request,
				HttpServletResponse response)
				throws ServletException, IOException {

		bCreatingAnItemFromPO = false;
		PrintWriter out = response.getWriter();
		if (!SMAuthenticate.authenticateSMCPCredentials(
				request, 
				response, 
				getServletContext(), 
				SMSystemFunctions.ICEditItems))
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
	    //contain the item number
		ICItem item = new ICItem("");
		item.loadFromHTTPRequest(request);
	    //First process if it's a 'delete':
		if(request.getParameter("SubmitDelete") != null){
		    if (request.getParameter("ConfirmDelete") == null){
		    	response.sendRedirect(
					"" + SMUtilities.getURLLinkBase(getServletContext()) + "smic.ICEditItemsSelection"
					+ "?" + ICItem.ParamItemNumber + "=" + item.getItemNumber()
					+ "&Warning=You must check the 'confirming' check box to delete."
					+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
				);
				return;
		    }
		    if (item.getItemNumber().compareToIgnoreCase("") == 0){
				response.sendRedirect(
					"" + SMUtilities.getURLLinkBase(getServletContext()) + "smic.ICEditItemsSelection"
					+ "?" + ICItem.ParamItemNumber + "=" + item.getItemNumber()
					+ "&Warning=You must enter an item number to delete."
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
		    		this.toString() 
		    		+ ".doPost - User: " 
		    		+ sUserID
		    		+ " - "
		    		+ sUserFullName
		    		);
		    	if(conn == null){
    				response.sendRedirect(
        					"" + SMUtilities.getURLLinkBase(getServletContext()) + "smic.ICEditItemsSelection"
        					+ "?" + ICItem.ParamItemNumber + "=" + item.getItemNumber()
        					+ "&Warning=Error deleting item - cannot get connection."
        					+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
        				);
    						return;
		    	}
		    	clsDatabaseFunctions.start_data_transaction(conn);
			    if (!item.delete(item.getItemNumber(), conn)){
			    	clsDatabaseFunctions.rollback_data_transaction(conn);
			    	clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1547080837]");
    				response.sendRedirect(
    					"" + SMUtilities.getURLLinkBase(getServletContext()) + "smic.ICEditItemsSelection"
    					+ "?" + ICItem.ParamItemNumber + "=" + item.getItemNumber()
    					+ "&Warning=" + "Error deleting item - " + item.getErrorMessageString()
    					+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
    				);
					return;
			    }else{
			    	clsDatabaseFunctions.commit_data_transaction(conn);
			    	clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1547080838]");
    				
			    	response.sendRedirect(
    					"" + SMUtilities.getURLLinkBase(getServletContext()) + "smic.ICEditItemsSelection"
    					+ "?" + ICItem.ParamItemNumber + "=" + item.getItemNumber()
    					+ "&Status=" + "Successfully deleted item " + item.getItemNumber() + "." 
    			 		+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
    				);
					return;
			    }
		    }
	    }
	    
		String title = "";
		String subtitle = "";
		
		//If we are ADDING a new item:
		if(request.getParameter("SubmitAdd") != null){
			item.setItemNumber("");
			item.setNewRecord("1");
			//Set defaults for an 'add':
			item.setActive("1");
			item.setTaxable("1");
		}
		
		//But if we are coming from the PO system to CREATE A NEW ITEM:
		if(request.getParameter(ICEditPOLineEdit.CREATE_ITEM_BUTTON) != null){
			bCreatingAnItemFromPO = true;
			item.setNewRecord("1");
			//Set defaults for an item added from the PO system:
			item.setActive("1");
			item.setTaxable("1");
		}
		
		//If we are coming here for the first time to edit (i.e., it's not a re-submit), then we need to 
		//load the input class from the database if possible:
		if(request.getParameter("SubmitEdit") != null){
	    	if(!item.load(getServletContext(), sDBID)){
				response.sendRedirect(
					"" + SMUtilities.getURLLinkBase(getServletContext()) + "smic.ICEditItemsSelection"
					+ "?" + ICItem.ParamItemNumber + "=" + item.getItemNumber()
					+ "&Warning=Could not load item " + item.getItemNumber() + "."
					+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
				);
					return;
	    	}
		}
		
		//If we are coming here from the same screen to edit a different item, then we also need to 
		//load the input class from the database if possible:
		if(request.getParameter("SubmitEditDifferent") != null){
	    	if(!item.load(getServletContext(), sDBID)){
				response.sendRedirect(
					"" + SMUtilities.getURLLinkBase(getServletContext()) + "smic.ICEditItemsSelection"
					+ "?" + ICItem.ParamItemNumber + "=" + item.getItemNumber()
					+ "&Warning=Could not load item " + item.getItemNumber() + "."
					+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
				);
					return;
	    	}
		}
		
	    String sScreenTitle = "";
		if(item.getItemNumber().compareToIgnoreCase("") != 0){
			sScreenTitle = "Editing item number " + item.getItemNumber();
		}else{
			sScreenTitle = "Adding a new item";
		}
		try {
			SMUtilities.addURLToHistory(sScreenTitle, CurrentSession, request);
		} catch (Exception e) {
		}
		
		//In any other case, such as the possibility that this is a 'resubmit', we need to edit the item:
    	title = "Edit " + sItemObjectName;
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
		out.println("<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMURLHistoryList?" 
				+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
				+ "\">Return to...</A><BR>");
	    out.println("<A HREF=\"" + WebContextParameters.getdocumentationpageURL(getServletContext()) + "#" + Long.toString(SMSystemFunctions.ICEditItems) 
	    		+ "\">Summary</A>");
	    
	    //If it's an existing item, add a link to edit the vendor items:
	    if(item.getItemNumber().compareToIgnoreCase("") != 0){
	    	//If the user has rights to it:
	    	boolean bAllowVendorItemEdit = SMSystemFunctions.isFunctionPermitted(
	    			SMSystemFunctions.ICEditVendorItems, 
	    			sUserID, 
	    			getServletContext(), 
	    			sDBID,
	    			(String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_LICENSE_MODULE_LEVEL));
	    	if (bAllowVendorItemEdit && !bCreatingAnItemFromPO){
	    		out.println("<BR><A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smic.ICEditVendorItems?" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" 
	    			+ sDBID 
	    			+ "&" + ICEditVendorItems.ITEMNUMBER + "=" + item.getItemNumber()
	    			+ "\">Edit vendor item information</A><BR>");
	    	}
	    	boolean bAllowPricingEdit = SMSystemFunctions.isFunctionPermitted(
	    			SMSystemFunctions.ICEditItemPricing, 
	    			sUserID, 
	    			getServletContext(), 
	    			sDBID,
	    			(String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_LICENSE_MODULE_LEVEL));
	    	if (bAllowPricingEdit && !bCreatingAnItemFromPO){
	    		out.println("<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) 
		    		+ "smic.ICViewItemPricingGenerate?" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
		    		+ "&StartingItemNumber=" + item.getItemNumber()
		    		+ "&EndingItemNumber=" + item.getItemNumber()
		    		+ "&StartingPriceCode="
		    		+ "&EndingPriceCode=ZZZZZZZZZZZ"
		    		+ "&CallingClass=" + SMUtilities.getFullClassName(this.toString()) 
		    		+ "\">Edit item prices</A>");
	    	}
	    }
	    out.println("</TD>");
	    
	    //Create a form for editing a different item:
	    out.println("<TD ALIGN=RIGHT>");
	    out.println("<FORM ACTION='" + SMUtilities.getURLLinkBase(getServletContext()) + "smic.ICEditItemsEdit' METHOD='POST'>");
	    out.println("<INPUT TYPE=HIDDEN NAME='" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "' VALUE='" + sDBID + "'>");
	    if (!bCreatingAnItemFromPO){
	    	out.println("<INPUT TYPE=TEXT NAME=\"" + ICItem.ParamItemNumber + "\" SIZE=18 MAXLENGTH=" + Integer.toString(SMTableicitems.sItemNumberLength) + " STYLE=\"width: 1.75in; height: 0.25in\">&nbsp;");
	    	out.println("<INPUT TYPE=SUBMIT NAME='SubmitEditDifferent' VALUE=\"Update different item\" STYLE='height: 0.24in'>");
	    }
	    out.println("</FORM>");
	    out.println("<TD>");
	    out.println("</TR>");
	    out.println("</TABLE>");

	    Edit_Record(item, out, sDBID, sUserFullName, bCreatingAnItemFromPO, request, sUserID);
		
		out.println("</BODY></HTML>");
	}
	
	private void Edit_Record(
			ICItem item, 
			PrintWriter pwOut, 
			String sDBID,
			String sUserFullName,
			boolean bCreatingItemFromPO,
			HttpServletRequest req,
			String sUserID
			){
		pwOut.println("<FORM NAME='MAINFORM' ACTION='" + SMUtilities.getURLLinkBase(getServletContext()) + "smic." + sICEditItemsEditCalledClassName + "' METHOD='POST'>");
		pwOut.println("<INPUT TYPE=HIDDEN NAME='" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "' VALUE='" + sDBID + "'>");
		if(item.getNewRecord().compareToIgnoreCase("0") == 0){
			pwOut.println("<INPUT TYPE=HIDDEN NAME=\"" + ICItem.ParamItemNumber + "\" VALUE=\"" + item.getItemNumber() + "\">");
			pwOut.println("<INPUT TYPE=HIDDEN NAME=\"" + ICItem.ParamAddingNewRecord + "\" VALUE=0>");
		}else{
			pwOut.println("<INPUT TYPE=HIDDEN NAME=\"" + ICItem.ParamAddingNewRecord + "\" VALUE=1>");
		}
		if (bCreatingItemFromPO){
			pwOut.println("<INPUT TYPE=HIDDEN NAME=\"" + ICEditPOLineEdit.CREATE_ITEM_BUTTON + "\" VALUE=Y>");
			pwOut.println("<INPUT TYPE=HIDDEN NAME=\"" + ICPOLine.Paramlid + "\" VALUE=" 
				+ clsManageRequestParameters.get_Request_Parameter(ICPOLine.Paramlid, req) + ">");
			pwOut.println("<INPUT TYPE=HIDDEN NAME=\"" + ICPOLine.Paramlpoheaderid + "\" VALUE=" 
					+ clsManageRequestParameters.get_Request_Parameter(ICPOLine.Paramlpoheaderid, req) + ">");
		}
		pwOut.println("<INPUT TYPE=HIDDEN NAME=\"" + ICItem.ParamLastMaintainedDate + "\" VALUE=\"" + item.getLastMaintainedDate() + "\">");
		pwOut.println("<INPUT TYPE=HIDDEN NAME=\"" + ICItem.ParamLastEditUserFullName + "\" VALUE=\"" + sUserFullName + "\">");
	    pwOut.println("Date last maintained: " + item.getLastMaintainedDate());
	    pwOut.println(" by user: " + item.getLastEditUserFullName() + "<BR>");
	    pwOut.println("<TABLE BORDER=12 CELLSPACING=2>");
        
	    //Item number:
	    if(item.getNewRecord().compareToIgnoreCase("1") == 0){
	        pwOut.println(clsCreateHTMLTableFormFields.Create_Edit_Form_Text_Input_Row(
	        		ICItem.ParamItemNumber, 
	        		item.getItemNumber().replace("\"", "&quot;"),  
	        		SMTableicitems.sItemNumberLength, 
	        		"Item number<B><FONT COLOR=\"RED\">*</FONT></B>:", 
	        		"Up to " + SMTableicitems.sItemNumberLength + " characters.",
	        		"24"
	        	)
	        );
	    	
	    }else{
	    	pwOut.println("<TD ALIGN=RIGHT><B>Item number:</B></TD><TD>" 
	    			+ item.getItemNumber().replace("\"", "&quot;") + "</TD><TD>&nbsp;</TD>");
	    }
	    
        //Description:
        pwOut.println(
        		"<TR>"
    	        + "<TD ALIGN=RIGHT><B>" + "Description<FONT COLOR=\"RED\">*</FONT>:"  + " </B></TD>"
    	        + "<TD ALIGN=LEFT>"
        		+ "<INPUT TYPE=TEXT NAME=\"" + ICItem.ParamItemDescription + "\""
        		+ " VALUE=\"" + item.getItemDescription().replace("\"", "&quot;") + "\""
        		+ " SIZE=76"
        		+ " MAXLENGTH=" + Integer.toString(SMTableicitems.sItemDescriptionLength)
        		+ " STYLE=\"height: 0.25in\""
        		+ "></TD>"
        		+ "<TD ALIGN=LEFT>" 
        		+ "Up to " + SMTableicitems.sItemDescriptionLength + " characters." 
        		+ "</TD>"
        		+ "</TR>"
        		);

		//Active?
	    int iTrueOrFalse = 0;
	    if (item.getActive().compareToIgnoreCase("1") == 0){
	    	//System.out.println("In " + this + " - item.getActive == '1'");
	    	iTrueOrFalse = 1;
	    }else{
	    	//System.out.println("In " + this + " - item.getActive != '1'");
	    	iTrueOrFalse = 0;
	    }
	    pwOut.println(clsCreateHTMLTableFormFields.Create_Edit_Form_Checkbox_Row(
	    	ICItem.ParamActive, 
			iTrueOrFalse, 
			"Active item?", 
			"Uncheck to de-activate this item."
			)
		);
	    
		//Date last made inactive:
        pwOut.println(
        		"<TR>"
    	        + "<TD ALIGN=RIGHT><B>" + "Inactive date:"  + " </B></TD>"
    	        + "<TD ALIGN=LEFT>"
        		+ "<INPUT TYPE=TEXT NAME=\"" + ICItem.ParamInactiveDate + "\""
        		+ " VALUE=\"" + item.getInactiveDate().replace("\"", "&quot;") + "\""
        		+ " SIZE=60"
        		+ " MAXLENGTH=" + "10"
        		+ " STYLE=\"height: 0.25in\""
        		+ ">"
        		+ SMUtilities.getDatePickerString(ICItem.ParamInactiveDate, getServletContext())
        		+ "</TD>"
        		+ "<TD ALIGN=LEFT>" 
        		+ "In <B>mm/dd/yyyy</B> format."
        		+ "</TD>"
        		+ "</TR>"
        		);
	    
		//Labor item?
	    iTrueOrFalse = 0;
	    if (item.getLaborItem().compareToIgnoreCase("1") == 0){
	    	iTrueOrFalse = 1;
	    }else{
	    	iTrueOrFalse = 0;
	    }
	    pwOut.println(clsCreateHTMLTableFormFields.Create_Edit_Form_Checkbox_Row(
	    	ICItem.ParamLaborItem, 
			iTrueOrFalse, 
			"Labor item?", 
			"Check if this item represents labor."
			)
		);
	    
		//Non stock item?
	    iTrueOrFalse = 0;
	    if (item.getNonStockItem().compareToIgnoreCase("1") == 0){
	    	iTrueOrFalse = 1;
	    }else{
	    	iTrueOrFalse = 0;
	    }
	    pwOut.println(clsCreateHTMLTableFormFields.Create_Edit_Form_Checkbox_Row(
	    	ICItem.ParamNonStockItem, 
			iTrueOrFalse, 
			"Non stock item?", 
			"Non stock items do not get processed in inventory*."
			)
		);
        
		//Taxable?
	    if (item.getTaxable().compareToIgnoreCase("1") == 0){
	    	iTrueOrFalse = 1;
	    }else{
	    	iTrueOrFalse = 0;
	    }
	    pwOut.println(clsCreateHTMLTableFormFields.Create_Edit_Form_Checkbox_Row(
	    	ICItem.ParamTaxable, 
			iTrueOrFalse, 
			"Taxable item?", 
			"Uncheck to make item ALWAYS non-taxable. "
			)
		);
	    
		//Cannot be Purchased?
	    if (item.getCannotBePurchasedFlag().compareToIgnoreCase("1") == 0){
	    	iTrueOrFalse = 1;
	    }else{
	    	iTrueOrFalse = 0;
	    }
	    pwOut.println(clsCreateHTMLTableFormFields.Create_Edit_Form_Checkbox_Row(
	    	ICItem.Paramicannotbepurchased, 
			iTrueOrFalse, 
			"Prevent purchasing?", 
			"If checked, the PO system will NOT allow this item to be purchased."
			)
		);

		//Cannot be Sold?
	    if (item.getCannotBeSoldFlag().compareToIgnoreCase("1") == 0){
	    	iTrueOrFalse = 1;
	    }else{
	    	iTrueOrFalse = 0;
	    }
	    pwOut.println(clsCreateHTMLTableFormFields.Create_Edit_Form_Checkbox_Row(
	    	ICItem.Paramicannotbesold, 
			iTrueOrFalse, 
			"Prevent selling?", 
			"If checked, this item will not be allowed to be placed on orders or work orders."
			)
		);
	    
		//Suppress item qty lookup?
	    iTrueOrFalse = 0;
	    if (item.getSuppressItemQtyLookup().compareToIgnoreCase("1") == 0){
	    	iTrueOrFalse = 1;
	    }else{
	    	iTrueOrFalse = 0;
	    }
	    pwOut.println(clsCreateHTMLTableFormFields.Create_Edit_Form_Checkbox_Row(
	    	ICItem.ParamSuppressItemQtyLookup, 
			iTrueOrFalse, 
			"Suppress item qty lookup?", 
			"If unchecked, warnings will be displayed in order editing when there are insufficient qtys of this item on hand."
			)
		);

		//Hide on invoice?
	    iTrueOrFalse = 0;
	    if (item.getHideOnInvoiceDefault().compareToIgnoreCase("1") == 0){
	    	iTrueOrFalse = 1;
	    }else{
	    	iTrueOrFalse = 0;
	    }
	    pwOut.println(clsCreateHTMLTableFormFields.Create_Edit_Form_Checkbox_Row(
	    	ICItem.ParamHideOnInvoiceDefault, 
			iTrueOrFalse, 
			"Default to hide on invoice?", 
			"If checked, item will initialized as NOT to be displayed on invoice (DNP)."
			)
		);
	    
        ArrayList<String> sValues = new ArrayList<String>();
        ArrayList<String> sDescriptions = new ArrayList<String>();
        String sSQL = "";
	    try{
	        //Categories
	        sSQL = "SELECT * FROM " + SMTableiccategories.TableName;
	        ResultSet rsCategories = clsDatabaseFunctions.openResultSet(
	        	sSQL, 
	        	getServletContext(), 
	        	sDBID,
	        	"MySQL",
	        	this.toString() + ".Edit_Record (2) - User: " 
	        	+ sUserID
	        	+ " - "
	        	+ sUserFullName
	        		);
	        
	        sValues.clear();
	        sDescriptions.clear();
	        //First, add a blank to make sure the user selects one:
	        sValues.add("");
	        sDescriptions.add("-- Select a category --");
	        while (rsCategories.next()){
	        	sValues.add((String) rsCategories.getString(SMTableiccategories.sCategoryCode).trim());
	        	sDescriptions.add((String) (rsCategories.getString(SMTableiccategories.sCategoryCode).trim() + " - " + rsCategories.getString(SMTableiccategories.sDescription).trim()));
	        }
	        rsCategories.close();
	        pwOut.println(clsCreateHTMLTableFormFields.Create_Edit_Form_List_Row(
	        		ICItem.ParamCategoryCode, 
	        		sValues, 
	        		item.getCategoryCode().replace("\"", "&quot;"), 
	        		sDescriptions, 
	        		"Category<B><FONT COLOR=\"RED\">*</FONT></B>:", 
	        		"Select the default category for this item."
	        	)
	        );
	        
	        //Price list code
	        sSQL = "SELECT * FROM " + SMTablepricelistcodes.TableName
	        	+ " ORDER BY " + SMTablepricelistcodes.spricelistcode
	        	;
	        ResultSet rsPriceListCodes = clsDatabaseFunctions.openResultSet(
	        	sSQL, 
	        	getServletContext(), 
	        	sDBID,
	        	"MySQL",
	        	this.toString() + ".Edit_Record (3) - User: " 
	        	+ sUserID
	        	+ " - "
	        	+ sUserFullName
	        		);
	        
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
	        pwOut.println(clsCreateHTMLTableFormFields.Create_Edit_Form_List_Row(
	        		ICItem.ParamDefaultPriceListCode, 
	        		sValues, 
	        		item.getDefaultPriceListCode().replace("\"", "&quot;"), 
	        		sDescriptions, 
	        		"<B>Price list code:<FONT COLOR=\"RED\">*</FONT></B>", 
	        		"Select the default price list code for this item."
	        	)
	        );
	        
		}catch (SQLException ex){
			pwOut.println(
				"<BR>Error [1390853959] in " + SMUtilities.getFullClassName(this.toString()) 
				+ " with SQL - " + sSQL + " - " + ex.getMessage() + ".<BR>"); 
		}
        
        //Cost unit of measure:
        pwOut.println(
        		"<TR>"
    	        + "<TD ALIGN=RIGHT><B>" + "Cost unit of measure<FONT COLOR=\"RED\">*</FONT>:"  + " </B></TD>"
    	        + "<TD ALIGN=LEFT>"
        		+ "<INPUT TYPE=TEXT NAME=\"" + ICItem.ParamCostUnitOfMeasure + "\""
        		+ " VALUE=\"" + item.getCostUnitOfMeasure().replace("\"", "&quot;") + "\""
        		+ " SIZE=76"
        		+ " MAXLENGTH=" + Integer.toString(SMTableicitems.sCostUnitOfMeasureLength)
        		+ " STYLE=\"height: 0.25in\""
        		+ "></TD>"
        		+ "<TD ALIGN=LEFT>" 
        		+ "Up to " + SMTableicitems.sCostUnitOfMeasureLength + " characters." 
        		+ "</TD>"
        		+ "</TR>"
        		);

        //Picking sequence:
        pwOut.println(
        		"<TR>"
    	        + "<TD ALIGN=RIGHT><B>" + "Picking sequence:"  + " </B></TD>"
    	        + "<TD ALIGN=LEFT>"
        		+ "<INPUT TYPE=TEXT NAME=\"" + ICItem.ParamPickingSequence + "\""
        		+ " VALUE=\"" + item.getPickingSequence().replace("\"", "&quot;") + "\""
        		+ " SIZE=76"
        		+ " MAXLENGTH=" + Integer.toString(SMTableicitems.sPickingSequenceLength)
        		+ " STYLE=\"height: 0.25in\""
        		+ "></TD>"
        		+ "<TD ALIGN=LEFT>" 
        		+ "Up to " + SMTableicitems.sPickingSequenceLength + " characters." 
        		+ "</TD>"
        		+ "</TR>"
        		);
        
        //Most recent cost:
        pwOut.println(
        		"<TR>"
    	        + "<TD ALIGN=RIGHT><B>" + "Most recent cost:"  + " </B></TD>"
    	        + "<TD ALIGN=LEFT>"
        		+ "<INPUT TYPE=TEXT NAME=\"" + ICItem.ParamMostRecentCost + "\""
        		+ " VALUE=\"" + item.getMostRecentCost().replace("\"", "&quot;") + "\""
        		+ " SIZE=20"
        		+ " MAXLENGTH=" + "10"
        		+ " STYLE=\"height: 0.25in\""
        		+ "></TD>"
        		+ "<TD ALIGN=LEFT>" 
        		+ "Normally set by PO receiving." 
        		+ "</TD>"
        		+ "</TR>"
        		);
        
        //Number of labels:
        pwOut.println(
        		"<TR>"
    	        + "<TD ALIGN=RIGHT><B>" + "Number of labels:"  + " </B></TD>"
    	        + "<TD ALIGN=LEFT>"
        		+ "<INPUT TYPE=TEXT NAME=\"" + ICItem.ParamNumberOfLabels + "\""
        		+ " VALUE=\"" + item.getNumberOfLabels().replace("\"", "&quot;") + "\""
        		+ " SIZE=20"
        		+ " MAXLENGTH=" + "10"
        		+ " STYLE=\"height: 0.25in\""
        		+ "></TD>"
        		+ "<TD ALIGN=LEFT>" 
        		+ "Number of labels to print for each unit." 
        		+ "</TD>"
        		+ "</TR>"
        		);
        
        //Dedicated to order number:
        pwOut.println(
        		"<TR>"
    	        + "<TD ALIGN=RIGHT><B>" + "Dedicated to order number:"  + " </B></TD>"
    	        + "<TD ALIGN=LEFT>"
        		+ "<INPUT TYPE=TEXT NAME=\"" + ICItem.ParamDedicatedToOrderNumber + "\""
        		+ " VALUE=\"" + item.getDedicatedToOrderNumber().replace("\"", "&quot;") + "\""
        		+ " SIZE=76"
        		+ " MAXLENGTH=" + Integer.toString(SMTableicitems.sDedicatedToOrderNumberLength)
        		+ " STYLE=\"height: 0.25in\""
        		+ "></TD>"
        		+ "<TD ALIGN=LEFT>" 
        		+ "Up to " + SMTableicitems.sDedicatedToOrderNumberLength + " characters.  (This prints on item labels.)" 
        		+ "</TD>"
        		+ "</TR>"
        		);

        //Comment 1:
        pwOut.println(
        		"<TR>"
    	        + "<TD ALIGN=RIGHT><B>" + "Comment 1:"  + " </B></TD>"
    	        + "<TD ALIGN=LEFT>"
        		+ "<INPUT TYPE=TEXT NAME=\"" + ICItem.ParamComment1 + "\""
        		+ " VALUE=\"" + item.getComment1().replace("\"", "&quot;") + "\""
        		+ " SIZE=76"
        		+ " MAXLENGTH=" + Integer.toString(SMTableicitems.sComment1Length)
        		+ " STYLE=\"height: 0.25in\""
        		+ "></TD>"
        		+ "<TD ALIGN=LEFT>" 
        		+ "Up to " + SMTableicitems.sComment1Length + " characters." 
        		+ "</TD>"
        		+ "</TR>"
        		);

        //Comment 2:
        pwOut.println(
        		"<TR>"
    	        + "<TD ALIGN=RIGHT><B>" + "Comment 2:"  + " </B></TD>"
    	        + "<TD ALIGN=LEFT>"
        		+ "<INPUT TYPE=TEXT NAME=\"" + ICItem.ParamComment2 + "\""
        		+ " VALUE=\"" + item.getComment2().replace("\"", "&quot;") + "\""
        		+ " SIZE=76"
        		+ " MAXLENGTH=" + Integer.toString(SMTableicitems.sComment2Length)
        		+ " STYLE=\"height: 0.25in\""
        		+ "></TD>"
        		+ "<TD ALIGN=LEFT>" 
        		+ "Up to " + SMTableicitems.sComment2Length + " characters." 
        		+ "</TD>"
        		+ "</TR>"
        		);

        //Comment 3:
        pwOut.println(
        		"<TR>"
    	        + "<TD ALIGN=RIGHT><B>" + "Comment 3:"  + " </B></TD>"
    	        + "<TD ALIGN=LEFT>"
        		+ "<INPUT TYPE=TEXT NAME=\"" + ICItem.ParamComment3 + "\""
        		+ " VALUE=\"" + item.getComment3().replace("\"", "&quot;") + "\""
        		+ " SIZE=76"
        		+ " MAXLENGTH=" + Integer.toString(SMTableicitems.sComment3Length)
        		+ " STYLE=\"height: 0.25in\""
        		+ "></TD>"
        		+ "<TD ALIGN=LEFT>" 
        		+ "Up to " + SMTableicitems.sComment3Length + " characters." 
        		+ "</TD>"
        		+ "</TR>"
        		);

        //Comment 4:
        pwOut.println(
        		"<TR>"
    	        + "<TD ALIGN=RIGHT><B>" + "Comment 4:"  + " </B></TD>"
    	        + "<TD ALIGN=LEFT>"
        		+ "<INPUT TYPE=TEXT NAME=\"" + ICItem.ParamComment4 + "\""
        		+ " VALUE=\"" + item.getComment4().replace("\"", "&quot;") + "\""
        		+ " SIZE=76"
        		+ " MAXLENGTH=" + Integer.toString(SMTableicitems.sComment4Length)
        		+ " STYLE=\"height: 0.25in\""
        		+ "></TD>"
        		+ "<TD ALIGN=LEFT>" 
        		+ "Up to " + SMTableicitems.sComment4Length + " characters." 
        		+ "</TD>"
        		+ "</TR>"
        		);

        //Report Group 1:
        pwOut.println(
        		"<TR>"
    	        + "<TD ALIGN=RIGHT><B>" + "Report Group 1:"  + " </B></TD>"
    	        + "<TD ALIGN=LEFT>"
        		+ "<INPUT TYPE=TEXT NAME=\"" + ICItem.ParamReportGroup1 + "\""
        		+ " VALUE=\"" + item.getReportGroup1().replace("\"", "&quot;") + "\""
        		+ " SIZE=76"
        		+ " MAXLENGTH=" + Integer.toString(SMTableicitems.sreportgroup1Length)
        		+ " STYLE=\"height: 0.25in\""
        		+ "></TD>"
        		+ "<TD ALIGN=LEFT>" 
        		+ "Up to " + SMTableicitems.sreportgroup1Length + " characters.  (This prints on item labels.)" 
        		+ "</TD>"
        		+ "</TR>"
        		);
        
        //Report Group 2:
        pwOut.println(
        		"<TR>"
    	        + "<TD ALIGN=RIGHT><B>" + "Report Group 2:"  + " </B></TD>"
    	        + "<TD ALIGN=LEFT>"
        		+ "<INPUT TYPE=TEXT NAME=\"" + ICItem.ParamReportGroup2 + "\""
        		+ " VALUE=\"" + item.getReportGroup2().replace("\"", "&quot;") + "\""
        		+ " SIZE=76"
        		+ " MAXLENGTH=" + Integer.toString(SMTableicitems.sreportgroup2Length)
        		+ " STYLE=\"height: 0.25in\""
        		+ "></TD>"
        		+ "<TD ALIGN=LEFT>" 
        		+ "Up to " + SMTableicitems.sreportgroup2Length + " characters." 
        		+ "</TD>"
        		+ "</TR>"
        		);
        
        //Report Group 3:
        pwOut.println(
        		"<TR>"
    	        + "<TD ALIGN=RIGHT><B>" + "Report Group 3:"  + " </B></TD>"
    	        + "<TD ALIGN=LEFT>"
        		+ "<INPUT TYPE=TEXT NAME=\"" + ICItem.ParamReportGroup3 + "\""
        		+ " VALUE=\"" + item.getReportGroup3().replace("\"", "&quot;") + "\""
        		+ " SIZE=76"
        		+ " MAXLENGTH=" + Integer.toString(SMTableicitems.sreportgroup3Length)
        		+ " STYLE=\"height: 0.25in\""
        		+ "></TD>"
        		+ "<TD ALIGN=LEFT>" 
        		+ "Up to " + SMTableicitems.sreportgroup3Length + " characters." 
        		+ "</TD>"
        		+ "</TR>"
        		);
        
        //Report Group 4:
        pwOut.println(
        		"<TR>"
    	        + "<TD ALIGN=RIGHT><B>" + "Report Group 4:"  + " </B></TD>"
    	        + "<TD ALIGN=LEFT>"
        		+ "<INPUT TYPE=TEXT NAME=\"" + ICItem.ParamReportGroup4 + "\""
        		+ " VALUE=\"" + item.getReportGroup4().replace("\"", "&quot;") + "\""
        		+ " SIZE=76"
        		+ " MAXLENGTH=" + Integer.toString(SMTableicitems.sreportgroup4Length)
        		+ " STYLE=\"height: 0.25in\""
        		+ "></TD>"
        		+ "<TD ALIGN=LEFT>" 
        		+ "Up to " + SMTableicitems.sreportgroup4Length + " characters." 
        		+ "</TD>"
        		+ "</TR>"
        		);
        
        //Report Group 5:
        pwOut.println(
        		"<TR>"
    	        + "<TD ALIGN=RIGHT><B>" + "Report Group 5:"  + " </B></TD>"
    	        + "<TD ALIGN=LEFT>"
        		+ "<INPUT TYPE=TEXT NAME=\"" + ICItem.ParamReportGroup5 + "\""
        		+ " VALUE=\"" + item.getReportGroup5().replace("\"", "&quot;") + "\""
        		+ " SIZE=76"
        		+ " MAXLENGTH=" + Integer.toString(SMTableicitems.sreportgroup5Length)
        		+ " STYLE=\"height: 0.25in\""
        		+ "></TD>"
        		+ "<TD ALIGN=LEFT>" 
        		+ "Up to " + SMTableicitems.sreportgroup5Length + " characters." 
        		+ "</TD>"
        		+ "</TR>"
        		);
        
        //Work order item comment:
        pwOut.println(
        		"<TR>"
    	        + "<TD ALIGN=RIGHT><B>" + "Work order item comment:"  + " </B></TD>"
    	        + "<TD ALIGN=LEFT COLSPAN=2>"
    	        + "<TEXTAREA NAME=\"" + ICItem.Paramsworkordercomment + "\" rows=\"3\" cols=\"110\">"
    	        + item.getworkordercomment().replace("\"", "&quot;")
    	        + "</TEXTAREA>"
        		+ "</TD>"
        		+ "</TR>"
        		);
        
        //starting here, edit item minimum on-hand quantity
        pwOut.println(
        		"<TR><TD COLSPAN=3><HR></TD></TR>" +
        			"<TD ALIGN=RIGHT><B>" + "Minimum On-Hand Qty by Location:"  + " </B></TD><TD>"
        		);
        BigDecimal bdMinQtyOnHand = BigDecimal.ZERO;
        //get item minimum quantity info
        try{
        	sSQL = "SELECT * FROM " + SMTablelocations.TableName + " LEFT JOIN" + 
				" (SELECT * FROM " + SMTableicitemlocations.TableName +
					" WHERE" + 
						" " + SMTableicitemlocations.TableName + "." + SMTableicitemlocations.sItemNumber + "='" + item.getItemNumber() + "')" +
				" AS TEMP" + 
				" ON" + 
				" TEMP." + SMTableicitemlocations.sLocation + " = " + SMTablelocations.TableName + "." + SMTablelocations.sLocation;
        	//System.out.println("[1382549750] SQL = " + sSQL);
	        ResultSet rsQtyAtLocations = clsDatabaseFunctions.openResultSet(sSQL, 
			  getServletContext(), 
			  sDBID,
			  "MySQL",
			  this.toString() + ".Edit_Record (3) - User: " 
			  + sUserID
			  + " - "
			  + sUserFullName
	   		);
	        pwOut.println("<TABLE WIDTH=100%>");
	        while (rsQtyAtLocations.next()){
	        	
        		pwOut.println("<TR><TD>" + rsQtyAtLocations.getString(SMTablelocations.TableName + "." + SMTableicitemlocations.sLocation) + " - " +
        							       rsQtyAtLocations.getString(SMTablelocations.TableName + "." + SMTablelocations.sLocationDescription)+ ":</TD>");
        		if (rsQtyAtLocations.getBigDecimal(SMTableicitemlocations.sMinQtyOnHand) != null ){
        			bdMinQtyOnHand = rsQtyAtLocations.getBigDecimal(SMTableicitemlocations.sMinQtyOnHand);
        		}
        		pwOut.println("<TD ALIGN=LEFT>"
	                		+ "<INPUT TYPE=TEXT NAME=\"LOC" + rsQtyAtLocations.getString(SMTablelocations.TableName + "." + SMTablelocations.sLocation) + "\""
	                		+ " VALUE=\"" + bdMinQtyOnHand + "\""
	                		+ " SIZE=20"
	                		+ " MAXLENGTH=20"
	                		+ " STYLE=\"height: 0.25in\""
	                		+ "></TD></TR>");
	        	pwOut.println("</TR>");
	        }
	        rsQtyAtLocations.close();
	        pwOut.println("</TABLE>");
	        
        }catch (SQLException ex){
			pwOut.println(
					"<BR>Error [1390853960] in " + SMUtilities.getFullClassName(this.toString()) 
					+ " with SQL - " + sSQL + " - " + ex.getMessage() + ".<BR>"); 
			//return false;
        }
        pwOut.println("</TD><TD ALIGN=LEFT>If no minimum quantity is designated, leave it 0.</TD></TR>");
        
        pwOut.println("</TABLE>");
        
        pwOut.println("*Non-stock items cannot be received, shipped, transferred, ordered on PO's, and so on."
        		+ "  They do not create IC transactions and are typically used for labor, expensed items, etc."
        		+ "  If you change an item from a stock item to a non-stock item,"
        		+ " all of the item statistics from its time as a stock item will be cleared,"
        		+ " but the transaction history will remain.<BR>");
        //pwOut.println("<BR>");
        pwOut.println("<P><INPUT TYPE=SUBMIT NAME='SubmitEdit' VALUE='Update " + sItemObjectName + "' STYLE='height: 0.24in'></P>");
        pwOut.println("</FORM>");
		
	}
	public void doGet(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
		
		doPost(request, response);
	}
}
