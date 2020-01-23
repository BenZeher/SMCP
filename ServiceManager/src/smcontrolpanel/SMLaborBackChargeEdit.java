package smcontrolpanel;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import SMClasses.SMLaborBackCharge;
import SMClasses.SMOrderHeader;
import SMDataDefinition.SMCreateGoogleDriveFolderParamDefinitions;
import SMDataDefinition.SMMasterStyleSheetDefinitions;
import SMDataDefinition.SMTablecostcenters;
import SMDataDefinition.SMTableicvendors;
import SMDataDefinition.SMTablelaborbackcharges;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsManageRequestParameters;
import ServletUtilities.clsServletUtilities;
import smap.APVendor;

public class SMLaborBackChargeEdit  extends HttpServlet {

	public static final long serialVersionUID = 1L;
	public static final String UPDATE_BUTTON_LABEL = "Update " + SMLaborBackCharge.ParamObjectName;
	public static final String DELETE_BUTTON_LABEL = "Delete " + SMLaborBackCharge.ParamObjectName;
	public static final String DELETE_COMMAND_VALUE = "UPDATE";
	public static final String UPDATE_COMMAND_VALUE = "DELETE";
	public static final String CONFIRM_DELETE_CHECKBOX_NAME = "ConfirmDelete";
	public static final String MAIN_FORM_NAME = "MAINFORM";
	public static final String CALC_TOTAL_BUTTON_LABEL = "Calculate";
	public static final String COMMAND_FLAG = "COMMANDFLAG";
	public static final String RECORDWASCHANGED_FLAG = "RECORDWASCHANGEDFLAG";
	public static final String RECORDWASCHANGED_FLAG_VALUE = "RECORDWASCHANGED";
	
	public static final String LABOR_BACKCHARGE_ID_FIELD = "LABORBACKCHARGEID";
	public static final String CREATE_UPLOAD_FOLDER_BUTTON_LABEL = "Create folder/Upload to Google Drive";
	public static final String CREATE_UPLOAD_FOLDER_COMMAND_VALUE = "CREATEUPLOADFOLDER";

	public void doPost(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
		
		SMLaborBackCharge entry = new SMLaborBackCharge(request);
		SMMasterEditEntry smedit = new SMMasterEditEntry(
				request,
				response,
				getServletContext(),
				entry.getObjectName(),
				SMUtilities.getFullClassName(this.toString()),
				"smcontrolpanel.SMLaborBackChargeAction",
				"smcontrolpanel.SMUserLogin",
				"Go back to user login",
				SMSystemFunctions.SMEditLaborBackCharges
				);
		
		if (!smedit.processSession(getServletContext(), SMSystemFunctions.SMEditLaborBackCharges)){
			smedit.getPWOut().println("Error in process session: " + smedit.getErrorMessages());
			return;
		}
		
		if(smedit.getAddingNewEntryFlag()){
			entry.setlid("-1");
		}
		
		//If this is a 'resubmit', meaning it's being called by the 'Action' class, then
		//the session will have an entry object in it, and that's what we'll pick up.
		HttpSession currentSession = smedit.getCurrentSession();
		
	    if (currentSession.getAttribute(SMLaborBackCharge.ParamObjectName) != null){
	    	entry = (SMLaborBackCharge) currentSession.getAttribute(SMLaborBackCharge.ParamObjectName);
	    	currentSession.removeAttribute(SMLaborBackCharge.ParamObjectName);
	    //But if it's NOT a 'resubmit', meaning this class was called for the first time to 
	    //edit, we'll pick up the ID or key from the request and try to load the entry:
	    }else{
	    	if (entry.getlid().compareToIgnoreCase("-1") != 0){
	    		try {
					entry.load(getServletContext(), smedit.getsDBID(), smedit.getUserID(), smedit.getFullUserName());
				} catch (Exception e) {
					smedit.redirectAction(e.getMessage(), "", SMLaborBackCharge.Paramlid + "=" + entry.getlid());
					return;
				}
	    	 
	       }
	    }
	    
	    smedit.printHeaderTable();
		smedit.getPWOut().println(SMUtilities.getMasterStyleSheetLink());
	    
	    //Add a link to return to order:
	    smedit.getPWOut().println(
		    "<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) 
			+ "smcontrolpanel.SMDisplayOrderInformation"
			+ "?" + SMOrderHeader.ParamsOrderNumber + "=" + entry.getstrimmedordernumber() 
			+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + smedit.getsDBID() + "\">Return to Order</A><BR>");
	    
	    
		smedit.getPWOut().println("<BR>");
		smedit.getPWOut().println("<script type='text/javascript' src='scripts/gen_validatorv31.js'></script>\n"
			+ "<script type='text/javascript' src='scripts/PopupWindow.js'></script>\n");	
		
		String sFormString = "<FORM ID='" + MAIN_FORM_NAME + "' NAME='" + MAIN_FORM_NAME + "' ACTION='" 
			+ SMUtilities.getURLLinkBase(getServletContext()) + smedit.getCalledClass() + "'";	
		sFormString	+= " METHOD='POST'>";
		
		smedit.getPWOut().println(sFormString);
		smedit.getPWOut().println("<INPUT TYPE=HIDDEN NAME='" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "' VALUE='" + smedit.getsDBID() + "'>");
		smedit.getPWOut().println("<INPUT TYPE=HIDDEN NAME=\"" + "CallingClass" + "\" VALUE=\"" 
				+ SMUtilities.getFullClassName(this.toString()) + "\">");
		smedit.getPWOut().println("<INPUT TYPE=HIDDEN NAME=\"" + "OriginalURL" + "\" VALUE=\"" + smedit.getOriginalURL() + "\">");
		
		try {
			String s = sCommandScripts(entry, smedit);
			s += "<INPUT TYPE=HIDDEN NAME=\"" + COMMAND_FLAG + "\" VALUE=\"" + "" + "\"" + "id=\"" + COMMAND_FLAG + "\"" + ">";
			s += "<INPUT TYPE=HIDDEN NAME=\"" + RECORDWASCHANGED_FLAG + "\" VALUE=\"" 
				+ clsManageRequestParameters.get_Request_Parameter(RECORDWASCHANGED_FLAG, smedit.getRequest()) + "\""
				+ " id=\"" + RECORDWASCHANGED_FLAG + "\"" + ">";
			smedit.getPWOut().println(s);
			
			smedit.getPWOut().println(getEditHTML(smedit, entry));
		} catch (SQLException e) {
    		String sError = "Could not create edit page - " + e.getMessage();
    		smedit.redirectAction(sError, "", SMLaborBackCharge.Paramlid + "=" + entry.getlid());
			return;
		}
	    smedit.getPWOut().println(createUpdateButton() + createDeleteButton() );
	    smedit.getPWOut().println("</FORM>");
	    
	    smedit.getPWOut().println(getNotes());
	    return;
	}
	private String getEditHTML(SMMasterEditEntry sm, SMLaborBackCharge entry) throws SQLException{

		String s = "";
		s +=clsServletUtilities.getJQueryIncludeString();
		  boolean bUseGoogleDrivePicker = false;
			String sPickerScript = "";
				try {
				 sPickerScript = clsServletUtilities.getDrivePickerJSIncludeString(
							SMCreateGoogleDriveFolderParamDefinitions.SM_LABOR_BACKCHARGE_PARAM_VALUE,
							entry.getlid().replace("\"", "&quot;"),
							getServletContext(),
							sm.getsDBID());
				} catch (Exception e) {
					System.out.println("[1554818420] - Failed to load drivepicker.js - " + e.getMessage());
				}
		
				if(sPickerScript.compareToIgnoreCase("") != 0) {
					 s += sPickerScript;
					bUseGoogleDrivePicker = true;
				} 
		
		s += "<TABLE WIDTH=40% BORDER=1>";
		String sID = "";
		if (
			//If we are NOT adding a new labor backcharge:
			(!sm.getAddingNewEntryFlag())
			// OR if it's NOT equal to -1:
			|| (entry.getlid().compareToIgnoreCase("-1") != 0)
		){
			// Then set the value of sID to the ID of this current material return
			sID = entry.getlid();
		}
		//If it's not a new entry:
		if(entry.getlid().compareToIgnoreCase("-1")!=0){
			s += "<TR><TD ALIGN=RIGHT><B>Labor back charge ID</B>:</TD><TD><B>" 
					+ sID 
					+ "<INPUT TYPE=HIDDEN NAME=\"" + SMLaborBackCharge.Paramlid + "\" VALUE=\"" 
					+ sID + "\">"
					+ "</B>";
		//but if it IS a new entry:
		}else{
		s += "<TR><TD ALIGN=RIGHT><B>Labor back charge ID</B>:</TD><TD><B>" 
			+ "(NEW)" 
			+ "<INPUT TYPE=HIDDEN NAME=\"" + SMLaborBackCharge.Paramlid + "\" VALUE=\"" + entry.getlid() + "\">"
			+ "</B>";
		}
			s += "</TD>"
			+ "</TR>";
		
		//Initiated by:
		String sInitiatedByString = "";
		if (entry.getlinitiatedbyid().compareToIgnoreCase("") != 0){
			sInitiatedByString = entry.getsinitiatedbyfullname() + " on " + entry.getsdatinitiated() + ".";
		}
		s += "<TR><TD ALIGN=RIGHT><B>Initiated by<B>:</TD>"
			+ "<TD>" 
			+ sInitiatedByString 
			+ "<INPUT TYPE=HIDDEN NAME=\"" + SMLaborBackCharge.Paramdatinitiated + "\" VALUE=\"" + entry.getsdatinitiated() + "\">"
			+ "<INPUT TYPE=HIDDEN NAME=\"" + SMLaborBackCharge.Paramlinitiatedbyid + "\" VALUE=\"" + entry.getlinitiatedbyid() + "\">"
			+ "<INPUT TYPE=HIDDEN NAME=\"" + SMLaborBackCharge.Paramsinitiatedbyfullname + "\" VALUE=\"" + entry.getsinitiatedbyfullname() + "\">"
			+ "</TD>"
			+ "</TR>"
		;
		
		//Category:
		/*
		s += "<TR><TD ALIGN=RIGHT><B>Category<B>:<FONT COLOR=\"RED\">*</FONT></TD>"
			+ "<TD ALIGN=LEFT><SELECT NAME=\"" + SMLaborBackCharge.Paramscategorycode + "\""
			+ " ID =\"" + SMLaborBackCharge.Paramscategorycode + "\""
			+ " ONCHANGE = \"flagDirty();\""
			+ ">"
			+ "<OPTION VALUE=\"" + "" + "\">" + "*** SELECT CATEGORY ***</OPTION>";
		String SQL = "SELECT * "
				  + " FROM " + SMTableiccategories.TableName
				  + " WHERE ("
				  	+ "(" + SMTableiccategories.iActive + " = 1)"
				  + ")"
				  + " ORDER BY " + SMTableiccategories.sDescription
				;
		try {
			ResultSet rsCategory = clsDatabaseFunctions.openResultSet(
					SQL, 
					getServletContext(), 
					sm.getsDBID(), 
					"MySQL", 
					this.toString() + " [1447961188] SQL: " + SQL);
			while (rsCategory.next()){
				String sCategoryCode = rsCategory.getString(SMTableiccategories.sCategoryCode);
				s += "<OPTION";
				
				String sCurrentCategoryInfo = entry.getscategorycode();
				
				if (sCurrentCategoryInfo.compareToIgnoreCase(sCategoryCode) == 0){
					s += " selected=YES ";
				}
				s += " VALUE=\"" + sCategoryCode + "\">" 
				+ rsCategory.getString(SMTableiccategories.sCategoryCode)
				+ " - " + rsCategory.getString(SMTableiccategories.sDescription)
				+ "</OPTION>";
			}
			rsCategory.close();
		} catch (SQLException e) {
			throw new SQLException("Error loading category codes - " + e.getMessage());
		}
		s += "</SELECT>";
		s +=  "</TD>"
		+ "</TR>"
 		;
		*/
		//Cost Center:
		s += "<TR><TD ALIGN=RIGHT><B>Cost Center<B>:<FONT COLOR=\"RED\">*</FONT></TD>"
			+ "<TD ALIGN=LEFT><SELECT NAME=\"" + SMLaborBackCharge.Paramlcostcenterid + "\""
			+ " ID =\"" + SMLaborBackCharge.Paramlcostcenterid + "\""
			+ " ONCHANGE = \"flagDirty();\""
			+ ">"
			+ "<OPTION VALUE=\"" + "" + "\">" + "*** SELECT COST CENTER ***</OPTION>";
		String SQL = "SELECT * "
				  + " FROM " + SMTablecostcenters.TableName
				  + " WHERE ("
				  	+ "(" + SMTablecostcenters.iactive + " = 1)"
				  + ")"
				  + " ORDER BY " + SMTablecostcenters.lid
				;
		try {
			ResultSet rsCostCenter = clsDatabaseFunctions.openResultSet(
					SQL, 
					getServletContext(), 
					sm.getsDBID(), 
					"MySQL", 
					this.toString() + " [1447961188] SQL: " + SQL);
			while (rsCostCenter.next()){
				String sCostCenter = Long.toString(rsCostCenter.getLong(SMTablecostcenters.lid));
				s += "<OPTION";
				
				String sCostCenterInfo = entry.getlcostcenterid();
				if (sCostCenterInfo.compareToIgnoreCase(sCostCenter) == 0){
					s += " selected=YES ";
				}
				s += " VALUE=\"" + sCostCenter + "\">" 
				+ rsCostCenter.getString(SMTablecostcenters.lid)
				+ " - " + rsCostCenter.getString(SMTablecostcenters.scostcentername)
				+ "</OPTION>";
			}
			rsCostCenter.close();
		} catch (SQLException e) {
			throw new SQLException("Error loading cost centers codes - " + e.getMessage());
		}
		s += "</SELECT>";
		s +=  "</TD>"
		+ "</TR>"
 		;
		
		//Date Sent:
		s += "<TR><TD ALIGN=RIGHT><B>Date Sent<B>:</TD>"
			+ "<TD ALIGN=LEFT><INPUT TYPE=TEXT NAME=\"" + SMLaborBackCharge.Paramdatdatesent + "\""
			+ " ID =\"" + SMLaborBackCharge.Paramdatdatesent + "\""
			+ " VALUE=\"" + entry.getdatdatesent().replace("\"", "&quot;") + "\""
			+ " ONCHANGE = \"flagDirty();\""
			+ " SIZE=" + "13"
			+ ">" + SMUtilities.getDatePickerString(SMLaborBackCharge.Paramdatdatesent, getServletContext()) + "</TD>"
			+ "</TR>"
 		;
		
		//Vendor:
		s += "<TR><TD ALIGN=RIGHT><B>Vendor<B>:<FONT COLOR=\"RED\">*</FONT></TD>"
			+ "<TD ALIGN=LEFT><SELECT NAME=\"" + SMLaborBackCharge.Paramsvendoracct + "\""
			+ " ID =\"" + SMLaborBackCharge.Paramsvendoracct + "\""
			+ " ONCHANGE = \"flagDirty();\""
			//+ "style=\"width: 410px;\""
			+ ">"
			+ "<OPTION VALUE=\"" + "" + "\">" + "*** SELECT VENDOR ***</OPTION>";
		String sSQL = "SELECT * "
			+ " FROM " + SMTableicvendors.TableName
			+ " ORDER BY " + SMTableicvendors.svendoracct
				;
		try {
			ResultSet rsVendor = clsDatabaseFunctions.openResultSet(
					sSQL, 
					getServletContext(), 
					sm.getsDBID(), 
					"MySQL", 
					this.toString() + " [1448310978] SQL: " + sSQL);
			
			String sCurrentVendorAcct = entry.getsvendor();
			while (rsVendor.next()){
				String sVendorAcct = rsVendor.getString(SMTableicvendors.svendoracct);
				
				//Display all active vendors.
				if (rsVendor.getString(SMTableicvendors.iactive).compareToIgnoreCase("1") == 0){
				s += "<OPTION";
				if (sCurrentVendorAcct.compareToIgnoreCase(sVendorAcct) == 0){
					s += " selected=YES ";
				}
				s += " VALUE=\"" + sVendorAcct + "\">" 
				+ rsVendor.getString(SMTableicvendors.svendoracct)
				+ " - " + rsVendor.getString(SMTableicvendors.sname)
				+ "</OPTION>";
				}else{
					//Check to see if current vendor is inactive.
					if(sCurrentVendorAcct.compareToIgnoreCase(sVendorAcct) == 0){
						s += "<OPTION";
						s += " selected=YES ";
						s += " VALUE=\"" + sVendorAcct + "\">" 
						+ rsVendor.getString(SMTableicvendors.svendoracct)
						+ " - " + rsVendor.getString(SMTableicvendors.sname)
						+ " - " + "(Account Inactive)"
						+ "</OPTION>";
					}
				}
			}
			rsVendor.close();
		} catch (SQLException e) {
			throw new SQLException("Error loading vendors - " + e.getMessage());
		}
		s += "</SELECT>";
		s +=  "</TD>" 
		+ "</TR>"
 		;
		
		//Vendor Item Number
		s += "<TR><TD ALIGN=RIGHT><B>" + "Vendor Item Number:<FONT COLOR=\"RED\">*</FONT>"  + " </B></TD>";
		s += "<TD ALIGN=LEFT><INPUT TYPE=TEXT NAME=\"" + SMLaborBackCharge.Paramsvendoritemnumber + "\""
			+ " VALUE=\"" + entry.getsvendoritemnumber().replace("\"", "&quot;") + "\""
			+ " ONCHANGE = \"flagDirty();\""
			+ " SIZE=" + "12"
			+ " MAXLENGTH=" + Integer.toString(SMTablelaborbackcharges.svendoritemnumberlength)
			+ " ></TD>"
			+ "</TR>"
		;
		//Trimmed order number:
		s += "<TR><TD ALIGN=RIGHT><B>" + "Order number:<FONT COLOR=\"RED\">*</FONT>"  + " </B></TD>";
		s += "<TD ALIGN=LEFT><INPUT TYPE=TEXT NAME=\"" + SMLaborBackCharge.Paramstrimmedordernumber + "\""
			+ " VALUE=\"" + entry.getstrimmedordernumber().replace("\"", "&quot;") + "\""
			+ " ONCHANGE = \"flagDirty();\""
			+ " SIZE=" + "12"
			+ " MAXLENGTH=" + Integer.toString(SMTablelaborbackcharges.strimmedordernumberlength)
			+ " ></TD>"
			+ "</TR>"
		;
		
		//Bill-to/Ship-to:
		if(!((entry.getstrimmedordernumber().compareToIgnoreCase("")) == 0) 
				&& !(entry.getstrimmedordernumber().compareToIgnoreCase("-1") == 0)){ 
		SMOrderHeader orderhead = new SMOrderHeader();
		orderhead.setM_strimmedordernumber(entry.getstrimmedordernumber());
		orderhead.load(getServletContext(), sm.getsDBID(), sm.getUserID(), sm.getFullUserName());
		s += "<TR><TD ALIGN=RIGHT><B>" + "Bill-to Name:"  + " </B></TD>";
		s += "<TD ALIGN=LEFT>"
			+ " " + orderhead.getM_sBillToName().replace("\"", "&quot;") + "</TD>"
			+ "</TR>";
		s += "<TR><TD ALIGN=RIGHT><B>" + "Ship-to Name:"  + " </B></TD>";
		s += "<TD ALIGN=LEFT>"
			+ " " + orderhead.getM_sShipToName().replace("\"", "&quot;") + "</TD>"
			+ "</TR>";
		}
		
		// Vendor Back-Charge Notes
		if(entry.getsvendor().compareToIgnoreCase("")!=0 || entry.getsvendor().isEmpty()) {
			APVendor vendor = new APVendor();
			vendor.setsvendoracct(entry.getsvendor());
			vendor.load(getServletContext(), sm.getsDBID(), sm.getUserID(), sm.getUserName());
			s += "<TR><TD ALIGN=RIGHT><B>" + "Back Charge Vendor Notes:"  + " </B></TD>";
			s += "<TD ALIGN=LEFT>"
				+ " " + vendor.getmbackchargememo().replace("\"", "&quot;") + "</TD>"
				+ "</TR>";
		}
		
		
		//Description:
		s += "<TR><TD ALIGN=RIGHT><B>" + "<B>Description:</B>"  + " </B></TD>";
		s += "<TD ALIGN=LEFT><TEXTAREA NAME=\"" + SMLaborBackCharge.Paramsdescription + "\""
			+ " ONCHANGE = \"flagDirty();\""
			+ " rows=\"" + "6" + "\""
			+ " style = \" width: 100%; \""
			+ " MAXLENGTH=" + Integer.toString(SMTablelaborbackcharges.sdescriptionlength)
			+ ">" + entry.getsdescription().replace("\"", "&quot;") 
			+ "</TEXTAREA>"
			+ "</TD>"
			+ "</TR>"
		;

		//Hours:
		s += "<TR><TD ALIGN=RIGHT><B>" + "Hours:"  + " </B></TD>";
		s += "<TD ALIGN=LEFT><INPUT TYPE=TEXT NAME=\"" + SMLaborBackCharge.Parambdhours + "\""
			+ " VALUE=\"" + entry.getbdhours().replace("\"", "&quot;") + "\""
			+ " ID =\"" + SMLaborBackCharge.Parambdhours + "\""
			+ " ONCHANGE = \"flagDirty();\""
			+ " SIZE=" + "13"
			+ " MAXLENGTH=" + Integer.toString(SMTablelaborbackcharges.strimmedordernumberlength)
			+ "></TD>"
			+ "</TR>"
		;
		//Labor Rate:
		s += "<TR><TD ALIGN=RIGHT><B>" + "Labor Rate:"  + " </B></TD>";
		s += "<TD ALIGN=LEFT><INPUT TYPE=TEXT NAME=\"" + SMLaborBackCharge.Parambdlaborrate + "\""
			+ " VALUE=\"" + entry.getbdlaborrate().replace("\"", "&quot;") + "\""
			+ " ID =\"" + SMLaborBackCharge.Parambdlaborrate + "\""
			+ " ONCHANGE = \"flagDirty();\""
			+ " SIZE=" + "13"
			+ " MAXLENGTH=" + Integer.toString(SMTablelaborbackcharges.strimmedordernumberlength)
			+ "></TD>"
			+ "</TR>"
		;
		//Misc Cost:
		s += "<TR><TD ALIGN=RIGHT><B>" + "Misc Cost:"  + " </B></TD>";
		s += "<TD ALIGN=LEFT><INPUT TYPE=TEXT NAME=\"" + SMLaborBackCharge.Parambdmisccost + "\""
			+ " VALUE=\"" + entry.getbdmisccost().replace("\"", "&quot;") + "\""
			+ " ID =\"" + SMLaborBackCharge.Parambdmisccost + "\""
			+ " ONCHANGE = \"flagDirty();\""
			+ " SIZE=" + "13"
			+ " MAXLENGTH=" + Integer.toString(SMTablelaborbackcharges.strimmedordernumberlength)
			+ "></TD>"
			+ "</TR>"
		;
		
		//Credit Requested:
		s += "<TR><TD ALIGN=RIGHT><B>" + "Credit Requested:"  + " </B></TD>";
		s += "<TD ALIGN=LEFT><INPUT TYPE=TEXT NAME=\"" + SMLaborBackCharge.Parambdcreditrequested + "\""
			+ " VALUE=\"" + entry.getbdcreditrequested().replace("\"", "&quot;") + "\""
			+ " ID =\"" + SMLaborBackCharge.Parambdcreditrequested + "\""
			+ " ONCHANGE = \"calcoutstanding();flagDirty();\""
			+ " SIZE=" + "13"
			+ " MAXLENGTH=" + Integer.toString(SMTablelaborbackcharges.strimmedordernumberlength)
			+ ">&nbsp;" + createCalcTotalButton() + "</TD>"
			+ "</TR>"
		;
		
		s += "<TR class = \" " + SMMasterStyleSheetDefinitions.TABLE_HEADING + " \" ><TD ALIGN=LEFT COLSPAN=2><B>ACCOUNTING</B>:</TD>\n</TR>\n";
		
		//Credit received:
		s += "<TR><TD ALIGN=RIGHT><B>" + "Credit Received:"  + " </B></TD>";
		s += "<TD ALIGN=LEFT><INPUT TYPE=TEXT NAME=\"" + SMLaborBackCharge.Parambdcreditreceived + "\""
			+ " VALUE=\"" + entry.getbdcreditreceived().replace("\"", "&quot;") + "\""
			+ " ID =\"" + SMLaborBackCharge.Parambdcreditreceived + "\""
			+ " ONCHANGE = \"calcoutstanding();flagDirty();\""
			+ " SIZE=" + "13"
			+ " MAXLENGTH=" + Integer.toString(SMTablelaborbackcharges.strimmedordernumberlength)
			+ "></TD>"
			+ "</TR>"
		;
		
		//Credit Denied:
		s += "<TR><TD ALIGN=RIGHT><B>" + "Credit Denied<a href='#creditdenied'><sup>1</sup></a>:"  + " </B></TD>";
		s += "<TD ALIGN=LEFT><INPUT TYPE=TEXT NAME=\"" + SMLaborBackCharge.Parambdcreditdenied + "\""
			+ " VALUE=\"" + entry.getbdcreditdenied().replace("\"", "&quot;") + "\""
			+ " ID =\"" + SMLaborBackCharge.Parambdcreditdenied + "\""
			+ " ONCHANGE = \"calcoutstanding();flagDirty();\""
			+ " SIZE=" + "13"
			+ " MAXLENGTH=" + Integer.toString(SMTablelaborbackcharges.strimmedordernumberlength)
			+ "></TD>"
			+ "</TR>"
		;
		
		//Outstanding Credit:
		s += "<TR><TD ALIGN=RIGHT><B>" + "Outstanding Credit: <a href='#outstandingcredit'><sup>2</sup></a>: </B></TD>";
		s += "<TD ALIGN=LEFT><INPUT TYPE=TEXT NAME=\"" + SMLaborBackCharge.Parambdoutstandingcredits + "\""
			+ " VALUE=\"" + entry.getbdoutstandingcredits().replace("\"", "&quot;") + "\""
			+ " ID =\"" + SMLaborBackCharge.Parambdoutstandingcredits + "\""
			+ " SIZE=" + "13"
			+ " MAXLENGTH=" + Integer.toString(SMTablelaborbackcharges.strimmedordernumberlength)
			+"  readonly ></TD>"
			+ "</TR>"
		;

	
		//Credit Note Date:
		s += "<TR><TD ALIGN=RIGHT><B>" + "Date of Credit Memo:	"  + " </B></TD>";
		s += "<TD ALIGN=LEFT><INPUT TYPE=TEXT NAME=\"" + SMLaborBackCharge.Paramdatcreditnotedate + "\""
			+ " VALUE=\"" + entry.getdatcreditnotedate().replace("\"", "&quot;") + "\""
			+ " ID =\"" + SMLaborBackCharge.Paramdatcreditnotedate + "\""
			+ " ONCHANGE = \"flagDirty();\""
			+ " SIZE=" + "13"
			+ ">" + SMUtilities.getDatePickerString(SMLaborBackCharge.Paramdatcreditnotedate, getServletContext()) +"</TD>"
			+ "</TR>"
		;
		
		//Comments:
		s += "<TR><TD ALIGN=RIGHT><B>Comments</B>:</TD>";
		s += "<TD>"
			+ "<TEXTAREA NAME=\"" + SMLaborBackCharge.Paramscomments + "\""
			+ " ONCHANGE = \"flagDirty();\""
			+ " rows=\"" + "4" + "\""
			//+ " cols=\"" + Integer.toString(iCols) + "\""
			+ " style = \" width: 100%; \""
			+ " MAXLENGTH=" + Integer.toString(SMTablelaborbackcharges.scommentslength)
			+ ">"
			+ entry.getscomments().replace("\"", "&quot;")
			+ "</TEXTAREA>"
			+ "</TD>"
			+ "</TR>"
		;
		
		    s+= "</TD>"
			+ "</TR>"
		;
		s += "</TABLE>";
		
		String sCreateAndUploadButton = "";

		if (SMSystemFunctions.isFunctionPermitted(
				SMSystemFunctions.SMCreateGDriveOrderFolders, 
				sm.getUserID(), 
				getServletContext(), 
				sm.getsDBID(),
				(String) sm.getCurrentSession().getAttribute(SMUtilities.SMCP_SESSION_PARAM_LICENSE_MODULE_LEVEL))
			&& !sm.getAddingNewEntryFlag()){
			
			sCreateAndUploadButton = createAndUploadFolderButton(bUseGoogleDrivePicker);
			s+=sCreateAndUploadButton;
		}
		s+= "<BR><INPUT TYPE=TEXT NAME=\"" + SMLaborBackCharge.Paramsgdoclink + "\""
				+ " VALUE=\"" + entry.getsgdoclink().replace("\"", "&quot;") + "\""
				+ "SIZE=" + "125"
				+ " MAXLENGTH=" + SMTablelaborbackcharges.sgdoclinklength
				+ "<BR>";
		return s;
	}
	
	private String getNotes(){
		String s = "";
		s += "NOTES:"
			+ "<BR><B><a name='creditdenied'> "
			+ "<sup>1</sup>"
			+ " 'Credit Denied' </B> "
			+ "The credit denied by the vendor must be entered, "
			+ "otherwise the labor back charge will remain open indefinitely."
			+ "</a> "
			+ "<BR><B><a nam = 'outstandingcredit'>"
			+ "<sup>2</sup>"
			+ " 'Outstanding Credit' </B> "
			+ "When the outstanding credit does not equal to <B>0</B> it means the labor back charge is <B>OPEN</B>"
			+ "<BR><FONT COLOR=\"RED\"><B>*</B> Required Fields</FONT>"
				;
		
		return s;
	}
	private String createCalcTotalButton(){
		return "<button type=\"button\""
				+ " value=\"" + CALC_TOTAL_BUTTON_LABEL + "\""
				+ " onClick=\"calcRequestedCredit();\">"
				+ CALC_TOTAL_BUTTON_LABEL
				+ "</button>\n";
	}

	private String createUpdateButton(){
		return "<P><BUTTON  TYPE=\"button\""
				+ " NAME = '" + UPDATE_BUTTON_LABEL+ "'"  
				+ " VALUE='" + UPDATE_BUTTON_LABEL 
				+ "' onClick=\"update();\">"
				+ UPDATE_BUTTON_LABEL
				+ "</button>\n";
	}
	
	private String createDeleteButton(){
		return "&nbsp;<BUTTON  TYPE=\"button\""
				+ " NAME = '" + DELETE_BUTTON_LABEL + "'"  
				+ " VALUE='" + DELETE_BUTTON_LABEL 
				+ "' onClick=\"deletelbc();\">"
				+ DELETE_BUTTON_LABEL 
				+ "</button>\n"
				+ "  Check to confirm deletion: <INPUT TYPE=CHECKBOX NAME=\"" 
					+ CONFIRM_DELETE_CHECKBOX_NAME + "\"></P>";
	}
	
	private String sCommandScripts(
			SMLaborBackCharge laborbackcharge, 
			SMMasterEditEntry smmaster
			) throws SQLException{
			String s = "";
			
			s += "<NOSCRIPT>\n"
				+ "    <font color=red>\n"
				+ "    <H3>This page requires that JavaScript be enabled to function properly</H3>\n"
				+ "    </font>\n"
				+ "</NOSCRIPT>\n"
			;

			s += "<script type='text/javascript'>\n";
			
			s += "window.onbeforeunload = prompttosave;\n"
			   + "window.onload = calcoutstanding;\n";
			
			s += "function prompttosave(){\n"
				//Don't prompt on updates and deletes
				+ "    if (document.getElementById(\"" + COMMAND_FLAG + "\").value == \"" + UPDATE_COMMAND_VALUE + "\" ){\n"
				+ "        return;\n"
				+ "    }\n"
				
				+ "    if (document.getElementById(\"" + COMMAND_FLAG + "\").value == \"" + DELETE_COMMAND_VALUE + "\" ){\n"
				+ "        return;\n"
				+ "    }\n"
		
				+ "    if (document.getElementById(\"" + RECORDWASCHANGED_FLAG + "\").value == \"" 
				+ RECORDWASCHANGED_FLAG_VALUE + "\" ){\n"
				+ "       return 'You have unsaved changes - are you sure you want to leave this page?';\n"
				+ "    }\n"
				+ "}\n\n"
				;
			
			s += "function flagDirty() {\n"
					+ "    document.getElementById(\"" + RECORDWASCHANGED_FLAG + "\").value = \"" 
						 + RECORDWASCHANGED_FLAG_VALUE + "\";\n"
					+ "}\n";
			//Delete
			s += "function deletelbc(){\n"
					+ "    document.getElementById(\"" + COMMAND_FLAG + "\").value = \"" 
							 + DELETE_COMMAND_VALUE + "\";\n"
					+ "    document.forms[\"MAINFORM\"].submit();\n"
					+ "}\n";
			
			//Update		
			s += "function update(){\n"
				+ "    document.getElementById(\"" + COMMAND_FLAG + "\").value = \"" 
						 + UPDATE_COMMAND_VALUE + "\";\n"
				+ "    document.forms[\"MAINFORM\"].submit();\n"
				+ "}\n";
			
			//Calc Total:
			s += "function calcRequestedCredit(){\n"			
				+ " var laborrate = Number(document.getElementById(\"" + SMLaborBackCharge.Parambdlaborrate + "\").value);\n"
				+ " var hours = Number(document.getElementById(\"" + SMLaborBackCharge.Parambdhours + "\").value);\n"
				+ " var misccost = Number(document.getElementById(\"" + SMLaborBackCharge.Parambdmisccost + "\").value);\n"
				+ " var requested = (laborrate * hours) + misccost;\n"
				+ " document.getElementById(\"" + SMLaborBackCharge.Parambdcreditrequested + "\").value = requested.toFixed(2);\n"
				+ "calcoutstanding();\n"
				+ "}\n"
			;
			//Calc Outstanding:
			s += "function calcoutstanding(){\n"				
				+ " var requested = parseFloat(document.getElementById(\"" + SMLaborBackCharge.Parambdcreditrequested + "\").value.replace(/,/g, ''));\n"
				+ "  requested = requested || 0;\n"
				+ "  document.getElementById(\"" + SMLaborBackCharge.Parambdcreditrequested + "\").value = requested.toFixed(2);\n\n"
				+ " var received = parseFloat(document.getElementById(\"" + SMLaborBackCharge.Parambdcreditreceived + "\").value.replace(/,/g, ''));\n"
				+  " received = received || 0;\n"
				+ "  document.getElementById(\"" + SMLaborBackCharge.Parambdcreditreceived + "\").value = received.toFixed(2);\n\n"
				+ " var denied = parseFloat(document.getElementById(\"" + SMLaborBackCharge.Parambdcreditdenied + "\").value.replace(/,/g, ''));\n"
				+  " denied = denied || 0;\n"
				+ "  document.getElementById(\"" + SMLaborBackCharge.Parambdcreditdenied + "\").value = denied.toFixed(2);\n\n"
				+ " var outstanding = (requested - received) - denied;\n"
				//convert outstanding from any falsey value to 0.
				+ " outstanding = outstanding || 0;\n"
				+ " document.getElementById(\"" + SMLaborBackCharge.Parambdoutstandingcredits + "\").value = outstanding.toFixed(2);\n"
				+ "}\n"
			;

			s += "function createanduploadfolder(){\n"
					+ "    document.getElementById(\"" + COMMAND_FLAG + "\").value = \"" 
						+ CREATE_UPLOAD_FOLDER_COMMAND_VALUE + "\";\n"
					+ "    document.forms[\"MAINFORM\"].submit();\n"
					+ "}\n";
			
			s += "</script>\n";
			return s;
		}
	
	private String createAndUploadFolderButton(boolean bUseGoogleDrivePicker){
		String sOnClickFunction = "createanduploadfolder()";
		if(bUseGoogleDrivePicker) {
			sOnClickFunction = "loadPicker()";
		}
		
		return "<button type=\"button\""
			+ " value=\"" + CREATE_UPLOAD_FOLDER_BUTTON_LABEL + "\""
			+ " name=\"" + CREATE_UPLOAD_FOLDER_BUTTON_LABEL + "\""
			+ " onClick=\"" + sOnClickFunction + "\">\n"
			+ CREATE_UPLOAD_FOLDER_BUTTON_LABEL
			+ "</button>\n"
			;
	}
	
	public void doGet(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
		
		doPost(request, response);
	}
}
