package smfa;

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

import smcontrolpanel.SMAuthenticate;
import smcontrolpanel.SMSystemFunctions;
import smcontrolpanel.SMUtilities;
import ConnectionPool.WebContextParameters;
import SMDataDefinition.SMTablefaclasses;
import SMDataDefinition.SMTablefadepreciationtype;
import SMDataDefinition.SMTablefalocations;
import SMDataDefinition.SMTablefamaster;
import SMDataDefinition.SMTableglaccounts;
import ServletUtilities.clsCreateHTMLTableFormFields;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsManageRequestParameters;

public class FAEditAssetsEdit extends HttpServlet {

	private static final long serialVersionUID = 1L;
	private static String sObjectName = "Asset";
	private static String sCalledClassName = "FAEditAssetsAction";
	private static String sDBID = "";
	private static String sUserName = "";
	private static String sUserID = "";
	private static String sUserFullName = "";
	private static String sCompanyName = "";
	private boolean bisNew;
	public void doPost(HttpServletRequest request,
				HttpServletResponse response)
				throws ServletException, IOException {

		PrintWriter out = response.getWriter();
	    //Get the session info:
	    HttpSession CurrentSession = request.getSession(true);
	    sDBID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_DATABASE_ID);
	    sUserName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERNAME);
	    sUserID = (String)CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERID);
	    sUserFullName = (String)CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERFIRSTNAME) 
	    				+ " " + (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERLASTNAME);
	    
	    sCompanyName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_COMPANYNAME);
	    if (!SMAuthenticate.authenticateSMCPCredentials(request, response, getServletContext(), SMSystemFunctions.FAManageAssets)){
	    	return;
	    }
		//Load the input class from the request object - if it's a 'resubmit', then this will contain
	    //all the values typed from the previous screen.  If it's a 'first time' edit, then this will only
	    //contain the item number
		FAAsset asset = new FAAsset("");
		asset.loadFromHTTPRequest(request);
	    //First process if it's a 'delete':
	    if(request.getParameter("SubmitDelete") != null){
		    if (request.getParameter("ConfirmDelete") == null){
				response.sendRedirect(
					"" + SMUtilities.getURLLinkBase(getServletContext()) + "smfa.FAEditAssetsSelect"
					+ "?" + FAAsset.ParamAssetNumber + "=" + asset.getAssetNumber()
					+ "&Warning=You must check the 'confirming' check box to delete."
					+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
				);
				return;
		    }
		    if (asset.getAssetNumber().compareToIgnoreCase("") == 0){
				response.sendRedirect(
					"" + SMUtilities.getURLLinkBase(getServletContext()) + "smfa.FAEditAssetsSelect"
					+ "?" + FAAsset.ParamAssetNumber + "=" + asset.getAssetNumber()
					+ "&Warning=You must enter an asset number to delete."
					+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
				);
				return;
		    }else{
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
        					"" + SMUtilities.getURLLinkBase(getServletContext()) + "smfa.FAEditAssetsSelect"
        					+ "?" + FAAsset.ParamAssetNumber + "=" + asset.getAssetNumber()
        					+ "&Warning=Error deleting item - cannot get connection."
        					+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
        				);
    						return;
		    	}
			    if (!asset.delete(asset.getAssetNumber(), conn)){
			    	clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1547067474]");
    				response.sendRedirect(
    					"" + SMUtilities.getURLLinkBase(getServletContext()) + "smfa.FAEditAssetsSelect"
    					+ "?" + FAAsset.ParamAssetNumber + "=" + asset.getAssetNumber()
    					+ "&Warning=Error deleting asset - " + asset.getErrorMessageString()
    					+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
    				);
					return;
			    }else{
			    	clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1547067475]");
    				response.sendRedirect(
    					"" + SMUtilities.getURLLinkBase(getServletContext()) + "smfa.FAEditAssetsSelect"
    					+ "?Status=Successfully deleted asset " + asset.getAssetNumber() + "."
    					+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
    				);
					return;
			    }
		    }
	    }
	    
		String title = "";
		String subtitle = "";
		
		//If we are ADDING a new asset:
		if(request.getParameter("SubmitAdd") != null){
			bisNew = true;
			//check to see if the asset number is taken or not.
			asset.setAssetNumber(clsManageRequestParameters.get_Request_Parameter("AssetNumber", request));
	    	if(asset.load(getServletContext(), sDBID)){
				response.sendRedirect(
					"" + SMUtilities.getURLLinkBase(getServletContext()) + "smfa.FAEditAssetsSelect"
					+ "?" + FAAsset.ParamAssetNumber + "=" + asset.getAssetNumber()
					+ "&Warning=Asset number" + asset.getAssetNumber() + " is taken. Please select another asset number."
					+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
				);
					return;
	    	}else{
	    		//the asset number is ok to use, now create a new asset for editing with the given asset number
	    		asset = new FAAsset(clsManageRequestParameters.get_Request_Parameter("AssetNumber", request));
	    	}
		}
		
		//If we are coming here for the first time to edit (i.e., it's not a re-submit), then we need to 
		//load the input class from the database if possible:
		if(request.getParameter("SubmitEdit") != null){
			bisNew = false;
	    	if(!asset.load(getServletContext(), sDBID)){
				response.sendRedirect(
					"" + SMUtilities.getURLLinkBase(getServletContext()) + "smfa.FAEditAssetsSelect"
					+ "?" + FAAsset.ParamAssetNumber + "=" + asset.getAssetNumber()
					+ "&Warning=Could not load asset " + asset.getAssetNumber() + "."
					+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID
				);
					return;
	    	}
		}
		
	    String sScreenTitle = "";
		//if(asset.getAssetNumber().compareToIgnoreCase("") != 0){
			sScreenTitle = "Editing asset number " + asset.getAssetNumber();
		//}else{
		//	sScreenTitle = "Adding a new asset";
		//}
		try {
			SMUtilities.addURLToHistory(sScreenTitle, CurrentSession, request);
		} catch (Exception e) {
		}
		
		//In any other case, such as the possibility that this is a 'resubmit', we need to edit the item:
    	title = "Edit " + sObjectName;
	    subtitle = "";
	    out.println(SMUtilities.SMCPTitleSubBGColor(title, subtitle, SMUtilities.getInitBackGroundColor(getServletContext(), sDBID), sCompanyName));
	    out.println(SMUtilities.getDatePickerIncludeString(getServletContext()));
	    
		//If there is a warning from trying to input previously, print it here:
		String sWarning = clsManageRequestParameters.get_Request_Parameter("Warning", request);
	    if (! sWarning.equalsIgnoreCase("")){
			out.println("<B><FONT COLOR=\"RED\">WARNING: " + sWarning + "</FONT></B><BR>");
		}
		String sStatus = clsManageRequestParameters.get_Request_Parameter("Status", request);
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
	    out.println("<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smfa.FAMainMenu?" 
				+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
				+ "\">Return to Fixed Assets Main Menu</A><BR>");
		out.println("<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMURLHistoryList?" 
				+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
				+ "\">Return to...</A><BR>");
	    out.println("<A HREF=\"" + WebContextParameters.getdocumentationpageURL(getServletContext()) + "#" + Long.toString(SMSystemFunctions.ICEditItems) 
	    		+ "\">Summary</A><BR>");

	    if (asset.getgdoclink().trim().compareToIgnoreCase("") != 0){
		    out.println("<A HREF=\"" + asset.getgdoclink() 
				+ "\">Google Drive folder</A>&nbsp;"
		    		);
	    }
	    
	    out.println("</TD>");
	    out.println("</TR>");
	    out.println("</TABLE>");

	    Edit_Record(asset, bisNew, out, sDBID, sUserName, request);
		
		out.println("</BODY></HTML>");
	}
	
	private void Edit_Record(
			FAAsset asset,
			boolean bisNew,
			PrintWriter pwOut, 
			String sConf,
			String sUser,
			HttpServletRequest req
			){
		pwOut.println("<FORM NAME='MAINFORM' ACTION='" + SMUtilities.getURLLinkBase(getServletContext()) + "smfa." + sCalledClassName + "' METHOD='POST'>");
		pwOut.println("<INPUT TYPE=HIDDEN NAME='" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "' VALUE='" + sDBID + "'>");
		pwOut.println("<INPUT TYPE=HIDDEN NAME='CallingClass' VALUE='" + SMUtilities.getFullClassName(this.toString()) + "'>");

	    pwOut.println("<TABLE BORDER=12 CELLSPACING=2>");
        
	    //Asset number:
	    if(bisNew){
	    	pwOut.println(clsCreateHTMLTableFormFields.Create_Edit_Form_Text_Input_Row(
	        		FAAsset.ParamAssetNumber+"NEW", 
	        		clsManageRequestParameters.get_Request_Parameter(FAAsset.ParamAssetNumber, req),  
	        		SMTablefamaster.sAssetNumberLength, 
	        		"Asset number<B><FONT COLOR=\"RED\">*</FONT></B>:", 
	        		"Up to " + SMTablefamaster.sAssetNumber + " characters.",
	        		"24"
	        	)
	        );
	    }else{
	    	pwOut.println(clsCreateHTMLTableFormFields.Create_Edit_Form_Disabled_Text_Input_Row(
	        		FAAsset.ParamAssetNumber+"EDIT", 
	        		clsManageRequestParameters.get_Request_Parameter(FAAsset.ParamAssetNumber, req),  
	        		SMTablefamaster.sAssetNumberLength, 
	        		"Asset number<B><FONT COLOR=\"RED\">*</FONT></B>:", 
	        		"Up to " + SMTablefamaster.sAssetNumber + " characters.",
	        		"24"
	        	)
	        
	        );
	    	pwOut.println("<INPUT TYPE=HIDDEN NAME=\""+FAAsset.ParamAssetNumber+"EDIT\" VALUE='" + clsManageRequestParameters.get_Request_Parameter(FAAsset.ParamAssetNumber, req) + "'>");
	    }
        
	    
        //Description:
        pwOut.println(
        		"<TR>"
    	        + "<TD ALIGN=RIGHT><B>" + "Description<FONT COLOR=\"RED\">*</FONT>:"  + " </B></TD>"
    	        + "<TD ALIGN=LEFT>"
        		+ "<INPUT TYPE=TEXT NAME=\"" + FAAsset.ParamDescription + "\""
        		+ " VALUE=\"" + asset.getDescription().replace("\"", "&quot;") + "\""
        		+ " SIZE=76"
        		+ " MAXLENGTH=" + Integer.toString(SMTablefamaster.sDescriptionLength)
        		+ " STYLE=\"height: 0.25in\""
        		+ "></TD>"
        		+ "<TD ALIGN=LEFT>" 
        		+ "Up to " + SMTablefamaster.sDescriptionLength + " characters." 
        		+ "</TD>"
        		+ "</TR>"
        		);
	    
		//Acquisition Date:
        pwOut.println(
        		"<TR>"
    	        + "<TD ALIGN=RIGHT><B>" + "Acquisition date<FONT COLOR=\"RED\">*</FONT>:"  + " </B></TD>"
    	        + "<TD ALIGN=LEFT>"
        		+ "<INPUT TYPE=TEXT NAME=\"" + FAAsset.ParamAcquisitionDate + "\""
        		+ " VALUE=\"" + asset.getAcquisitionDate().replace("\"", "&quot;") + "\""
        		+ " SIZE=60"
        		+ " MAXLENGTH=" + "10"
        		+ " STYLE=\"width:.75 in;height: 0.25in\""
        		+ ">"
        		+ SMUtilities.getDatePickerString(FAAsset.ParamAcquisitionDate, getServletContext())
        		+ "</TD>"
        		+ "<TD ALIGN=LEFT>" 
        		+ "In <B>mm/dd/yyyy</B> format."
        		+ "</TD>"
        		+ "</TR>"
        		);

        //Purchase Amt.:
        pwOut.println(
        		"<TR>"
    	        + "<TD ALIGN=RIGHT><B>" + "Purchase amount:"  + " </B></TD>"
    	        + "<TD ALIGN=LEFT>"
        		+ "<INPUT TYPE=TEXT NAME=\"" + FAAsset.ParamAcquisitionAmount + "\""
        		+ " VALUE=\"" + asset.getAcquisitionAmount() + "\""
        		+ " MAXLENGTH=" + "13"
        		+ " STYLE=\"width:1 in;height: 0.25in\""
        		+ "></TD>"
        		+ "<TD ALIGN=LEFT>" 
        		+ "" 
        		+ "</TD>"
        		+ "</TR>"
        		);

        ArrayList<String> sValues = new ArrayList<String>();
        ArrayList<String> sDescriptions = new ArrayList<String>();
        String sSQL = "";
	    try{
	        //Depreciation type
	        sSQL = "SELECT * FROM " + SMTablefadepreciationtype.TableName;
	        ResultSet rsTypes = clsDatabaseFunctions.openResultSet(
	        	sSQL, 
	        	getServletContext(), 
	        	sConf,
	        	"MySQL",
	        	this.toString() + ".Edit_Record (load decpreciation type table) - User: " + sUserID
	        	+ " - "
	        	+ sUserFullName
	        	);
	        
	        sValues.clear();
	        sDescriptions.clear();
	        //First, add a blank to make sure the user selects one:
	        sValues.add("");
	        sDescriptions.add("-- Select a type --");
	        while (rsTypes.next()){
	        	sValues.add((String) rsTypes.getString(SMTablefadepreciationtype.sDepreciationType).trim());
	        	sDescriptions.add((String) (rsTypes.getString(SMTablefadepreciationtype.sDepreciationType).trim() + " - " + 
	        								rsTypes.getString(SMTablefadepreciationtype.sCalculationType).trim() + " - " + 
	        								rsTypes.getString(SMTablefadepreciationtype.iLifeInMonths)));
	        }
	        rsTypes.close();
	        pwOut.println(clsCreateHTMLTableFormFields.Create_Edit_Form_List_Row(
	        		FAAsset.ParamDepreciationType, 
	        		sValues, 
	        		asset.getDepreciationType().replace("\"", "&quot;"), 
	        		sDescriptions, 
	        		"Depreciation type<B><FONT COLOR=\"RED\">*</FONT></B>:", 
	        		"Select the depreciation type for this asset."
	        	)
	        ); 
	        
	        //Class
	        sSQL = "SELECT * FROM " + SMTablefaclasses.TableName;
	        ResultSet rsClasses = clsDatabaseFunctions.openResultSet(
	        	sSQL, 
	        	getServletContext(), 
	        	sConf,
	        	"MySQL",
	        	this.toString() + ".Edit_Record (load class table) - User: " + sUserID
	        	+ " - "
	        	+ sUserFullName
	        	);
	        
	        sValues.clear();
	        sDescriptions.clear();
	        //First, add a blank to make sure the user selects one:
	        sValues.add("");
	        sDescriptions.add("-- Select a class --");
	        while (rsClasses.next()){
	        	sValues.add((String) rsClasses.getString(SMTablefaclasses.sClass).trim());
	        	sDescriptions.add((String) (rsClasses.getString(SMTablefaclasses.sClass).trim() + " - " + 
						        			rsClasses.getString(SMTablefaclasses.sClassDescription).trim()));
	        }
	        rsClasses.close();
	        pwOut.println(clsCreateHTMLTableFormFields.Create_Edit_Form_List_Row(
	        		FAAsset.ParamClass, 
	        		sValues, 
	        		asset.get_Class().replace("\"", "&quot;"), 
	        		sDescriptions, 
	        		"Class<B><FONT COLOR=\"RED\">*</FONT></B>:", 
	        		"Select the class for this asset."
	        	)
	        );
		}catch (SQLException ex){
			pwOut.println("<BR>Error in " + this.toString() + ".Edit_Record reading depreciation types and classes: " + ex.getMessage() + "<BR>");
		}
        
        //Serial number:
        pwOut.println(
        		"<TR>"
    	        + "<TD ALIGN=RIGHT><B>" + "Serial number:"  + " </B></TD>"
    	        + "<TD ALIGN=LEFT>"
        		+ "<INPUT TYPE=TEXT NAME=\"" + FAAsset.ParamSerialNumber + "\""
        		+ " VALUE=\"" + asset.getSerialNumber().replace("\"", "&quot;") + "\""
        		+ " SIZE=76"
        		+ " MAXLENGTH=" + Integer.toString(SMTablefamaster.sSerialNumberLength)
        		+ " STYLE=\"height: 0.25in\""
        		+ "></TD>"
        		+ "<TD ALIGN=LEFT>" 
        		+ "Up to " + SMTablefamaster.sSerialNumberLength + " characters." 
        		+ "</TD>"
        		+ "</TR>"
        		);
        
       try{
	        //Location
	        sSQL = "SELECT * FROM " + SMTablefalocations.TableName;
	        ResultSet rsLocations = clsDatabaseFunctions.openResultSet(
	        	sSQL, 
	        	getServletContext(), 
	        	sConf,
	        	"MySQL",
	        	this.toString() + ".Edit_Record (load location table) - User: " + sUserID
	        	+ " - "
	        	+ sUserFullName
	        	);
	        
	        sValues.clear();
	        sDescriptions.clear();
	        //First, add a blank to make sure the user selects one:
	        sValues.add("");
	        sDescriptions.add("-- Select a location --");
	        while (rsLocations.next()){
	        	sValues.add((String) rsLocations.getString(SMTablefalocations.sLocLocation).trim());
	        	sDescriptions.add((String) (rsLocations.getString(SMTablefalocations.sLocLocation).trim() + " - " + 
						        			rsLocations.getString(SMTablefalocations.sLocDescription).trim()));
	        }
	        rsLocations.close();
	        pwOut.println(clsCreateHTMLTableFormFields.Create_Edit_Form_List_Row(
	        		FAAsset.ParamLocation, 
	        		sValues, 
	        		asset.getLocation().replace("\"", "&quot;"), 
	        		sDescriptions, 
	        		"Location<B><FONT COLOR=\"RED\">*</FONT></B>:", 
	        		"asset location."
	        	)
	        ); 
		}catch (SQLException ex){
			pwOut.println("<BR>Error in " + this.toString() + ".Edit_Record reading locations: " + ex.getMessage() + "<BR>");
		}

        //State:
        pwOut.println(
        		"<TR>"
    	        + "<TD ALIGN=RIGHT><B>" + "State:"  + " </B></TD>"
    	        + "<TD ALIGN=LEFT>"
        		+ "<INPUT TYPE=TEXT NAME=\"" + FAAsset.ParamState + "\""
        		+ " VALUE=\"" + asset.getState().replace("\"", "&quot;") + "\""
        		+ " MAXLENGTH=" + Integer.toString(SMTablefamaster.sStateLength)
        		+ " STYLE=\"width:.75 in;height: 0.25in\""
        		+ "></TD>"
        		+ "<TD ALIGN=LEFT>" 
        		+ "Up to " + SMTablefamaster.sStateLength + " characters." 
        		+ "</TD>"
        		+ "</TR>"
        		);
		
		//Date disposed:
        String sDate = asset.getDateSold();
        if (sDate.compareTo(FAAsset.EMPTY_DATE_STRING) == 0){
        	sDate = "";
        }
        pwOut.println(
        		"<TR>"
    	        + "<TD ALIGN=RIGHT><B>" + "Date disposed:"  + " </B></TD>"
    	        + "<TD ALIGN=LEFT>"
        		+ "<INPUT TYPE=TEXT NAME=\"" + FAAsset.ParamDateSold + "\""
        		+ " VALUE=\"" + sDate.replace("\"", "&quot;") + "\""
        		+ " SIZE=60"
        		+ " MAXLENGTH=" + "10"
        		+ " STYLE=\"width:.75 in;height: 0.25in\""
        		+ ">"
        		+ SMUtilities.getDatePickerString(FAAsset.ParamDateSold, getServletContext())
        		+ "</TD>"
        		+ "<TD ALIGN=LEFT>" 
        		+ "In <B>mm/dd/yyyy</B> format."
        		+ "</TD>"
        		+ "</TR>"
        		);

        //Amt. sold for:
        pwOut.println(
        		"<TR>"
    	        + "<TD ALIGN=RIGHT><B>" + "Amount sold for:"  + " </B></TD>"
    	        + "<TD ALIGN=LEFT>"
        		+ "<INPUT TYPE=TEXT NAME=\"" + FAAsset.ParamAmountSoldFor + "\""
        		+ " VALUE=\"" + asset.getAmountSoldFor() + "\""
        		+ " MAXLENGTH=" + "13"
        		+ " STYLE=\"width:1 in;height: 0.25in\""
        		+ "></TD>"
        		+ "<TD ALIGN=LEFT>" 
        		+ "" 
        		+ "</TD>"
        		+ "</TR>"
        		);

        //Driver:
        pwOut.println(
        		"<TR>"
    	        + "<TD ALIGN=RIGHT><B>" + "Driver:"  + " </B></TD>"
    	        + "<TD ALIGN=LEFT>"
        		+ "<INPUT TYPE=TEXT NAME=\"" + FAAsset.ParamDriver + "\""
        		+ " VALUE=\"" + asset.getDriver().replace("\"", "&quot;") + "\""
        		+ " SIZE=76"
        		+ " MAXLENGTH=" + Integer.toString(SMTablefamaster.sdriverLength)
        		+ " STYLE=\"height: 0.25in\""
        		+ "></TD>"
        		+ "<TD ALIGN=LEFT>" 
        		+ "Up to " + SMTablefamaster.sdriverLength + " characters." 
        		+ "</TD>"
        		+ "</TR>"
        		);
        
        //License tag number:
        pwOut.println(
        		"<TR>"
    	        + "<TD ALIGN=RIGHT><B>" + "License tag number:"  + " </B></TD>"
    	        + "<TD ALIGN=LEFT>"
        		+ "<INPUT TYPE=TEXT NAME=\"" + FAAsset.ParamLicenseTagNumber + "\""
        		+ " VALUE=\"" + asset.getLicenseTagNumber().replace("\"", "&quot;") + "\""
        		+ " MAXLENGTH=" + Integer.toString(SMTablefamaster.sLicenseTagNumberLength)
        		+ " STYLE=\"width:1.5 in;height: 0.25in\""
        		+ "></TD>"
        		+ "<TD ALIGN=LEFT>" 
        		+ "Up to " + SMTablefamaster.sLicenseTagNumberLength + " characters." 
        		+ "</TD>"
        		+ "</TR>"
        		);

        //Garaged location:
        pwOut.println(
        		"<TR>"
    	        + "<TD ALIGN=RIGHT><B>" + "Garaged location:"  + " </B></TD>"
    	        + "<TD ALIGN=LEFT>"
        		+ "<INPUT TYPE=TEXT NAME=\"" + FAAsset.ParamGaragedLocation + "\""
        		+ " VALUE=\"" + asset.getGaragedLocation().replace("\"", "&quot;") + "\""
        		+ " MAXLENGTH=" + Integer.toString(SMTablefamaster.sGaragedLocationLength)
        		+ " STYLE=\"width:1.5 in;height: 0.25in\""
        		+ "></TD>"
        		+ "<TD ALIGN=LEFT>" 
        		+ "Up to " + SMTablefamaster.sGaragedLocationLength + " characters." 
        		+ "</TD>"
        		+ "</TR>"
        		);

        //Truck Number:
        pwOut.println(
        		"<TR>"
    	        + "<TD ALIGN=RIGHT><B>" + "Truck number:"  + " </B></TD>"
    	        + "<TD ALIGN=LEFT>"
        		+ "<INPUT TYPE=TEXT NAME=\"" + FAAsset.ParamTruckNumber + "\""
        		+ " VALUE=\"" + asset.getTruckNumber().replace("\"", "&quot;") + "\""
        		+ " MAXLENGTH=" + Integer.toString(SMTablefamaster.sTruckNumberLength)
        		+ " STYLE=\"width:1.5 in;height: 0.25in\""
        		+ "></TD>"
        		+ "<TD ALIGN=LEFT>" 
        		+ "Up to " + SMTablefamaster.sTruckNumberLength + " characters." 
        		+ "</TD>"
        		+ "</TR>"
        		);

        //Salvage value:
        pwOut.println(
        		"<TR>"
    	        + "<TD ALIGN=RIGHT><B>" + "Salvage value:"  + " </B></TD>"
    	        + "<TD ALIGN=LEFT>"
        		+ "<INPUT TYPE=TEXT NAME=\"" + FAAsset.ParamSalvageValue + "\""
        		+ " VALUE=\"" + asset.getSalvageValue() + "\""
        		+ " MAXLENGTH=" + "13"
        		+ " STYLE=\"width:1 in;height: 0.25in\""
        		+ "></TD>"
        		+ "<TD ALIGN=LEFT>" 
        		+ "" 
        		+ "</TD>"
        		+ "</TR>"
        		);

        if (bisNew){
            //Accumulated depreciation:
	        pwOut.println(
	        		"<TR>"
	    	        + "<TD ALIGN=RIGHT><B>" + "Accumulated depreciation:"  + " </B></TD>"
	    	        + "<TD ALIGN=LEFT>"
	        		+ "<INPUT TYPE=TEXT NAME=\"" + FAAsset.ParamAccumulatedDepreciation + "\""
	        		+ " VALUE=\"" + asset.getAccumulatedDepreciation() + "\""
	        		+ " MAXLENGTH=" + "13"
	        		+ " STYLE=\"width:1 in;height: 0.25in\""
	        		+ "></TD>"
	        		+ "<TD ALIGN=LEFT>" 
	        		+ "NOTE: this value can only be edited when adding a NEW asset."  
	        		+ "</TD>"
	        		+ "</TR>"
	        		);

	        //Remaining depreciation:
	        pwOut.println(
	        		"<TR>"
	    	        + "<TD ALIGN=RIGHT><B>" + "Remaining depreciation:"  + " </B></TD>"
	    	        + "<TD ALIGN=LEFT>0.00</TD>"
	        		+ "<TD ALIGN=LEFT>" 
	        		+ "</TD>"
	        		+ "</TR>"
	        		);
        }else{
            //Accumulated depreciation:
	        pwOut.println(
	        		"<TR>"
	    	        + "<TD ALIGN=RIGHT><B>" + "Accumulated depreciation:"  + " </B></TD>"
	    	        + "<TD ALIGN=LEFT>"
	        		+ asset.getAccumulatedDepreciation()
	        		+ "</TD>"
	        		+ "<TD ALIGN=LEFT>" 
	        		+ "</TD>"
	        		+ "</TR>"
	        		+ "<INPUT TYPE=HIDDEN NAME=\"" + FAAsset.ParamAccumulatedDepreciation + "\" VALUE=\"" 
	        		+ asset.getAccumulatedDepreciation() + "\">"
	        		);

	        //Remaining depreciation:
	        pwOut.println(
	        		"<TR>"
	    	        + "<TD ALIGN=RIGHT><B>" + "Remaining depreciation:"  + " </B></TD>"
	    	        + "<TD ALIGN=LEFT>"
	        		+ asset.getCurrentValue()
	        		+ "</TD>"
	        		+ "<TD ALIGN=LEFT>" 
	        		+ "</TD>"
	        		+ "</TR>"
	        		);

        }
        //Comment:
        pwOut.println(
        		"<TR>"
    	        + "<TD ALIGN=RIGHT><B>" + "Comment:"  + " </B></TD>"
    	        + "<TD ALIGN=LEFT>"
        		+ "<INPUT TYPE=TEXT NAME=\"" + FAAsset.ParamComment + "\""
        		+ " VALUE=\"" + asset.getComment().replace("\"", "&quot;") + "\""
        		+ " SIZE=76"
        		+ " MAXLENGTH=" + Integer.toString(SMTablefamaster.sCommentLength)
        		+ " STYLE=\"height: 0.25in\""
        		+ "></TD>"
        		+ "<TD ALIGN=LEFT>" 
        		+ "Up to " + SMTablefamaster.sCommentLength + " characters." 
        		+ "</TD>"
        		+ "</TR>"
        		);
        //Comment1:
        pwOut.println(
        		"<TR>"
    	        + "<TD ALIGN=RIGHT><B>" + "Comment1:"  + " </B></TD>"
    	        + "<TD ALIGN=LEFT>"
        		+ "<INPUT TYPE=TEXT NAME=\"" + FAAsset.ParamComment1 + "\""
        		+ " VALUE=\"" + asset.getComment1().replace("\"", "&quot;") + "\""
        		+ " SIZE=76"
        		+ " MAXLENGTH=" + Integer.toString(SMTablefamaster.sComment1Length)
        		+ " STYLE=\"height: 0.25in\""
        		+ "></TD>"
        		+ "<TD ALIGN=LEFT>" 
        		+ "Up to " + SMTablefamaster.sComment1Length + " characters." 
        		+ "</TD>"
        		+ "</TR>"
        		);
        //Comment2:
        pwOut.println(
        		"<TR>"
    	        + "<TD ALIGN=RIGHT><B>" + "Comment2:"  + " </B></TD>"
    	        + "<TD ALIGN=LEFT>"
        		+ "<INPUT TYPE=TEXT NAME=\"" + FAAsset.ParamComment2 + "\""
        		+ " VALUE=\"" + asset.getComment2().replace("\"", "&quot;") + "\""
        		+ " SIZE=76"
        		+ " MAXLENGTH=" + Integer.toString(SMTablefamaster.sComment2Length)
        		+ " STYLE=\"height: 0.25in\""
        		+ "></TD>"
        		+ "<TD ALIGN=LEFT>" 
        		+ "Up to " + SMTablefamaster.sComment2Length + " characters." 
        		+ "</TD>"
        		+ "</TR>"
        		);
        //Comment3:
        pwOut.println(
        		"<TR>"
    	        + "<TD ALIGN=RIGHT><B>" + "Comment3:"  + " </B></TD>"
    	        + "<TD ALIGN=LEFT>"
        		+ "<INPUT TYPE=TEXT NAME=\"" + FAAsset.ParamComment3 + "\""
        		+ " VALUE=\"" + asset.getComment3().replace("\"", "&quot;") + "\""
        		+ " SIZE=76"
        		+ " MAXLENGTH=" + Integer.toString(SMTablefamaster.sComment3Length)
        		+ " STYLE=\"height: 0.25in\""
        		+ "></TD>"
        		+ "<TD ALIGN=LEFT>" 
        		+ "Up to " + SMTablefamaster.sComment3Length + " characters." 
        		+ "</TD>"
        		+ "</TR>"
        		);
        
	    try{
	        
	        sSQL = "SELECT * FROM " + SMTableglaccounts.TableName;
	        ResultSet rsTypes = clsDatabaseFunctions.openResultSet(
	        	sSQL, 
	        	getServletContext(), 
	        	sConf,
	        	"MySQL",
	        	this.toString() + ".Edit_Record (load glaccounts table) - User: " + sUserID
	        	+ " - "
	        	+ sUserFullName
	        	);
	        
	        //create content for drop downs
	        sValues.clear();
	        sDescriptions.clear();
	        //First, add a blank to make sure the user selects one:
	        sValues.add("");
	        sDescriptions.add("-- Select an account --");
	        while (rsTypes.next()){
	        	sValues.add((String) rsTypes.getString(SMTableglaccounts.sAcctID).trim());
	        	sDescriptions.add((String) (rsTypes.getString(SMTableglaccounts.sAcctID).trim() + " - " + 
	        								rsTypes.getString(SMTableglaccounts.sDesc).trim()));
	        }
	        rsTypes.close();
		}catch (SQLException ex){
	    	System.out.println("Error in " + this.toString()+ " class!!");
	        System.out.println("SQLException: " + ex.getMessage());
	        System.out.println("SQLState: " + ex.getSQLState());
	        System.out.println("SQL: " + ex.getErrorCode());
			//return false;
		}
        //Note payable GL acccount
        pwOut.println(clsCreateHTMLTableFormFields.Create_Edit_Form_List_Row(
        		FAAsset.ParamNotePayableGLAcct, 
        		sValues, 
        		asset.getNotePayableGLAcct().replace("\"", "&quot;"), 
        		sDescriptions, 
        		"Note Payable GL Acct:", 
        		"Select the note payable GL account for this asset."
        	)
        ); 
        //Loss or Gain GL account
        pwOut.println(clsCreateHTMLTableFormFields.Create_Edit_Form_List_Row(
        		FAAsset.ParamLossOrGainGL, 
        		sValues, 
        		asset.getLossOrGainGL().replace("\"", "&quot;"), 
        		sDescriptions, 
        		"Loss or Gain GL Acct:", 
        		"Select the loss or gain GL account for this asset."
        	)
        ); 
        //Depreciation GL account
        pwOut.println(clsCreateHTMLTableFormFields.Create_Edit_Form_List_Row(
        		FAAsset.ParamDepreciationGLAcct, 
        		sValues, 
        		asset.getDepreciationGLAcct().replace("\"", "&quot;"), 
        		sDescriptions, 
        		"Depreciation GL Acct<FONT COLOR=\"RED\">*</FONT>:", 
        		"Select the depreciation GL account for this asset."
        	)
        ); 
        //Accumulated Depreciation GL account 
        pwOut.println(clsCreateHTMLTableFormFields.Create_Edit_Form_List_Row(
        		FAAsset.ParamAccumulatedDepreciationGLAcct, 
        		sValues, 
        		asset.getAccumulatedDepreciationGLAcct().replace("\"", "&quot;"), 
        		sDescriptions, 
        		"Accumulated Depreciation GL Acct<FONT COLOR=\"RED\">*</FONT>:", 
        		"Select the accumulated depreciation GL account for this asset."
        	)
        ); 
        
        pwOut.println("</TABLE>");
        
        pwOut.println("<B>Document folder link:</B>&nbsp;"
				+ "<INPUT TYPE=TEXT NAME=\"" + 
        			FAAsset.Paramgdoclink + "\""
				+ " VALUE=\"" + asset.getgdoclink().replace("\"", "&quot;") + "\""
				+ "SIZE=" + "125"
				+ " MAXLENGTH=" + Integer.toString(254)
				+ "<BR>");
			
        pwOut.println("<P><INPUT TYPE=SUBMIT NAME='SubmitEdit' VALUE='Update " + sObjectName + "' STYLE='height: 0.24in'></P>");
        pwOut.println("</FORM>");
		
	}
	public void doGet(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
		
		doPost(request, response);
	}
}
