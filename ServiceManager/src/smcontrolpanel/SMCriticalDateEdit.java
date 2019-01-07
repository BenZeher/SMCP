package smcontrolpanel;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import SMDataDefinition.*;

import ServletUtilities.clsCreateHTMLTableFormFields;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsDateAndTimeConversions;
import ServletUtilities.clsTextEditorFunctions;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;


public class SMCriticalDateEdit  extends HttpServlet {

	private static final long serialVersionUID = 1L;
	public static final String SAVE_BUTTON_LABEL = "Save " + SMCriticalDateEntry.ParamObjectName;
	public static final String SAVE_COMMAND_VALUE = "SAVE";
	public static final String DELETE_BUTTON_LABEL = "Delete " + SMCriticalDateEntry.ParamObjectName;
	public static final String DELETE_COMMAND_VALUE = "DELETE";
	public static final String CONFIRM_DELETE_CHECKBOX = "CONFIRMDELETE";
	public static final String RECORDWASCHANGED_FLAG = "RECORDWASCHANGEDFLAG";
	public static final String RECORDWASCHANGED_FLAG_VALUE = "RECORDWASCHANGED";
	public static final String COMMAND_FLAG = "COMMANDFLAG";
	private static final String FORM_NAME = "MAINFORM";
	
	public void doPost(HttpServletRequest request,
			HttpServletResponse response)
	throws ServletException, IOException {

		//PrintWriter debug_out = new PrintWriter(System.out);
		//SMUtilities.printRequestParameters(debug_out, request);

		SMCriticalDateEntry entry = new SMCriticalDateEntry(request);
		//System.out.println("CriticalDateEntry created");
		SMMasterEditEntry smedit = new SMMasterEditEntry(request,
				response,
				getServletContext(),
				entry.getObjectName(),
				SMUtilities.getFullClassName(this.toString()),
				"smcontrolpanel.SMCriticalDateAction",
				"smcontrolpanel.SMUserLogin",
				"Go back to user login",
				SMSystemFunctions.SMEditCriticalDate
		);
		//System.out.println("Editor object created");

		if (!smedit.processSession(getServletContext(), SMSystemFunctions.SMEditCriticalDate)){
			smedit.getPWOut().println("Error in process session: " + smedit.getErrorMessages());
			return;
		}

		if (smedit.getAddingNewEntryFlag()){
			entry.setid("-1");
			//System.out.println("recognized as adding new record.");
		}

		//If this is a 'resubmit', meaning it's being called by SMCriticalDateAction, then
		//the session will have a job cost entry object in it, and that's what we'll pick up.
		HttpSession currentSession = smedit.getCurrentSession();

		if (currentSession.getAttribute(SMCriticalDateEntry.ParamObjectName) != null){
			//System.out.println("got an object from session");
			entry = (SMCriticalDateEntry) currentSession.getAttribute(SMCriticalDateEntry.ParamObjectName);
			currentSession.removeAttribute(SMCriticalDateEntry.ParamObjectName);
			//But if it's NOT a 'resubmit', meaning this class was called for the first time to 
			//edit, we'll pick up the ID or key from the request and try to load the entry:
		}else{
			//System.out.println("create a new object");
			if (!smedit.getAddingNewEntryFlag()){
				if(!entry.load(getServletContext(), smedit.getsDBID(), smedit.getUserName())){
					response.sendRedirect(
							"" + SMUtilities.getURLLinkBase(getServletContext()) + "" + smedit.getCallingClass()
							+ "?" + SMCriticalDateEntry.ParamID + "=" + entry.getid()
							+ "&Warning=" + entry.getErrorMessages()
							+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + smedit.getsDBID()
					);
					//System.out.println("entry loaded successfully.");
					return;
				}
			}
		}
		smedit.setTitle("Edit " + entry.getObjectName() 
				+ ": " + SMTablecriticaldates.getTypeDescriptions(Integer.parseInt(entry.getitype())) 
				+ " " + entry.getdocnumber());
		smedit.printHeaderTable();

		smedit.getPWOut().println(clsTextEditorFunctions.getJavascriptTextEditToolBarFunctions());
		smedit.getPWOut().println(getScripts());	
		
		//Add a link to create a new critical date:
		smedit.getPWOut().println(
				"<BR><FONT SIZE=2><a href=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMCriticalDateEdit?" +
				SMCriticalDateEntry.ParamID + "=-1" +
				"&" + SMCriticalDateEntry.ParamCriticalDate + "=" 
				+ clsDateAndTimeConversions.utilDateToString(new Date(System.currentTimeMillis()), "M/d/yyyy") +
				"&" + SMCriticalDateEntry.ParamDocNumber + "=" + entry.getdocnumber().trim() +
				"&" + SMCriticalDateEntry.ParamResponsibleUserID + "=" + entry.getresponsibleuserid().trim() +
				"&" + SMCriticalDateEntry.ParamAssignedbyUserID + "=" + entry.getassignedbyuserid().trim() +
				"&" + SMCriticalDateEntry.ParamTimeStampAudit + "=" 
				+ clsDateAndTimeConversions.utilDateToString(new Date(System.currentTimeMillis()), "yyyy-MM-dd hh:mm:ss") +
				"&" + SMCriticalDateEntry.ParamiType + "= " + entry.getitype() +
				//"&OriginalURL=" + sCurrentURL +
				"&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + smedit.getsDBID() + 
				"\">" + "Add new critical date" + "</A><BR>");


		smedit.getPWOut().println(
				"<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMURLHistoryList?" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + smedit.getsDBID() + "\">Return to...</A>");

		smedit.getPWOut().println("<BR>");

		try {
			createEditPage(getEditHTML(smedit, entry), FORM_NAME, smedit.getPWOut(), smedit);

		} catch (Exception e) {
			String sError = "Could not create edit page - " + e.getMessage();
			response.sendRedirect(
					"" + SMUtilities.getURLLinkBase(getServletContext()) + "" + smedit.getCallingClass()
					+ "?" + SMCriticalDateEntry.ParamID + "=" + entry.getid()
					+ "&Warning=Could not load entry ID: " + entry.getid() + sError
					+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + smedit.getsDBID()
			);
			return;
		}
		return;
	}

	public void createEditPage(
			String sEditHTML,
			String sFormClassName,
			PrintWriter pwOut,
			SMMasterEditEntry sm
	) throws Exception{
		//Create HTML Form
		String sFormString = "<FORM ID='" + sFormClassName + "' NAME='" + sFormClassName + "' ACTION='" 
			+ SMUtilities.getURLLinkBase(getServletContext()) + sm.getCalledClass() + "'";
		
		sFormString	+= " METHOD='POST'";
		if (sFormClassName.compareToIgnoreCase("") != 0){
			sFormString += " class=" + sFormClassName + ">";
		}
		pwOut.println(sFormString);
		pwOut.println("<INPUT TYPE=HIDDEN NAME='" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "' VALUE='" + sm.getsDBID() + "'>");
		pwOut.println("<INPUT TYPE=HIDDEN NAME=\"" + "CallingClass" + "\" VALUE=\"" 
				+ SMUtilities.getFullClassName(this.toString()) + "\">");
		//Create HTML Fields
		try {
			pwOut.println(sEditHTML);
		} catch (Exception e) {
			throw new Exception(e.getMessage());
		}
		//Add save and delete buttons
		pwOut.println("<BR>" + createSaveButton() + "&nbsp;" +createDeleteButton());
		pwOut.println("</FORM>");
	}
	
	private String getEditHTML(SMMasterEditEntry sm, SMCriticalDateEntry entry) throws SQLException{

		String s = "";
		
//		s += "<INPUT TYPE=HIDDEN NAME=\"" + RECORDWASCHANGED_FLAG + "\""
//				+ " VALUE=\"" + clsManageRequestParameters.get_Request_Parameter(RECORDWASCHANGED_FLAG, sm.getRequest()) + "\""
//				+ " id=\"" + RECORDWASCHANGED_FLAG + "\""+ ">";
		
		s += "<INPUT TYPE=HIDDEN NAME=\"" + COMMAND_FLAG + "\""
				+ " VALUE=\"" + "" + "\""+ " "
				+ " ID=\"" + COMMAND_FLAG + "\""+ "\">";
		
		s += "<TABLE BORDER=1>";

		String sID = "";
		if (entry.getid().compareToIgnoreCase("-1") == 0){
			sID = "NEW";
		}else{
			sID = entry.getid();
		}
		s += "<TR><TD ALIGN=RIGHT><B> ID</B>:</TD>"
			+ "<TD>" 
			+ "<B>" + sID + "</B>" 
			+ "<INPUT TYPE=HIDDEN NAME=\"" + SMCriticalDateEntry.ParamID + "\" VALUE=\"" 
			+ entry.getid() + "\">"
			+ "</TD>"
			+ "</TR>"
			;

		//Critical date
		String sDefaultDate = "";
		//System.out.println("entry.getdatoriginationdate() = " + entry.getdatoriginationdate().replace("\"", "&quot;"));
		if (entry.getcriticaldate().replace("\"", "&quot;").compareToIgnoreCase(
				SMCriticalDateEntry.EMPTY_DATE_STRING) == 0){
			sDefaultDate = clsDateAndTimeConversions.now("M/d/yyyy");
		}else{
			//System.out.println("entry.getdatoriginationdate() = " + entry.getdatoriginationdate());
			sDefaultDate = entry.getcriticaldate().replace("\"", "&quot;");
			//System.out.println("sDefaultDate = " + sDefaultDate);
		}
		s += clsCreateHTMLTableFormFields.Create_Edit_Form_Date_Input_Row(
				SMCriticalDateEntry.ParamCriticalDate,
				sDefaultDate,
				"<B>Critical Date: <FONT COLOR=RED>*Required*</FONT></B>",
				"", //Remark to the right of the field
				getServletContext()
		);

		//Resolved flag
		int iResolved = Integer.parseInt(entry.getresolvedflag());
		//System.out.println("iResolved = " + iResolved);
		s += clsCreateHTMLTableFormFields.Create_Edit_Form_Checkbox_Row(
				SMCriticalDateEntry.ParamResolvedFlag, 
				iResolved, 
				"<B>Resolved? </B>",
				"" //Remark to the right of the field
		);

		//Comments	
		s += clsTextEditorFunctions.Create_Edit_Form_Editable_MultilineText_Input_Row(
				SMCriticalDateEntry.ParamComments, 
				entry.getcomments().replace("\"", "&quot;"), 
				"<B>Comments: </B>",
				"", 
				150, 
				350, 
				"", 
				false,
				true);

		s += "<INPUT TYPE=HIDDEN NAME=\"" + SMCriticalDateEntry.ParamTimeStampAudit + "\" VALUE=\"" + entry.gettimestampaudit() + "\">";
		s += "<INPUT TYPE=HIDDEN NAME=\"" + SMCriticalDateEntry.ParamDocNumber + "\" VALUE=\"" + entry.getdocnumber() + "\">";

		//Additional Information
		int iType = Integer.parseInt(entry.getitype());
		//If the record type is not zero display the record information
		if(iType != 0) {
			
			String sSQL = "";

			if(iType == SMTablecriticaldates.SALES_ORDER_RECORD_TYPE) {
				sSQL = "SELECT "
					+ SMTableorderheaders.TableName + "." + SMTableorderheaders.sBillToName  
					+ ", " + SMTableorderheaders.TableName + "." + SMTableorderheaders.sShipToName 
					+ ", " + SMTableorderheaders.TableName + "." + SMTableorderheaders.sBillToContact
					+ ", " + SMTableorderheaders.TableName + "." + SMTableorderheaders.sShipToContact
					+ ", " + SMTableorderheaders.TableName + "." + SMTableorderheaders.sBillToPhone 
					+ ", " + SMTableorderheaders.TableName + "." + SMTableorderheaders.sShipToPhone
					+ ", " + SMTableorderheaders.TableName + "." + SMTableorderheaders.sOrderNumber
					+ " FROM " + SMTableorderheaders.TableName
					+ " WHERE " +  SMTableorderheaders.TableName + "." + SMTableorderheaders.strimmedordernumber + " = '" + entry.getdocnumber() + "'";
			}else if(iType == SMTablecriticaldates.PURCHASE_ORDER_RECORD_TYPE) {
				sSQL = "SELECT "
					+ " " + SMTableicpoheaders.TableName + "." + SMTableicpoheaders.svendor
					+ ", " + SMTableicpoheaders.TableName + "." + SMTableicpoheaders.lid
					+ ", " + SMTableicpoheaders.TableName + "." + SMTableicpoheaders.svendorname
					+ ", " + SMTableicpoheaders.TableName + "." + SMTableicpoheaders.datexpecteddate
					+ ", " + SMTableicpoheaders.TableName + "." + SMTableicpoheaders.lstatus
					+ " FROM " + SMTableicpoheaders.TableName
					+ " WHERE " +  SMTableicpoheaders.TableName + "." + SMTableicpoheaders.lid + " = '" + entry.getdocnumber() + "'";
			}else if(iType == SMTablecriticaldates.SALES_LEAD_RECORD_TYPE) {
				sSQL = "SELECT "
					+  SMTablebids.TableName + "." + SMTablebids.scontactname
					+ ", " + SMTablebids.TableName + "." + SMTablebids.sphonenumber  
					+ ", " + SMTablebids.TableName + "." + SMTablebids.lid  
					+ ", " + SMTablebids.TableName + "." + SMTablebids.sprojectname
					+ ", " + SMTablebids.TableName + "." + SMTablebids.scustomername
					+ " FROM " + SMTablebids.TableName
					+ " WHERE " +  SMTablebids.TableName + "." + SMTablebids.sphonenumber + " = '" + entry.getdocnumber() + "'";
			}else if(iType == SMTablecriticaldates.SALES_CONTACT_RECORD_TYPE) {
				sSQL = "";
			}else if(iType == SMTablecriticaldates.AR_CALL_SHEET_RECORD_TYPE) {
				sSQL = "";
			}else{
				sSQL = "";
			}

		
			try{
				ResultSet rsInfo = clsDatabaseFunctions.openResultSet(
					sSQL,
					getServletContext(),
					sm.getsDBID(),
					"MySQL",
					this.toString() + " reading additional information in critical dates edit - user: " + sm.getUserID()
					+ " - "
					+ sm.getFullUserName()
					);
				if (rsInfo.next()){
					if( iType == SMTablecriticaldates.SALES_ORDER_RECORD_TYPE) {
						s += "<TR><TD ALIGN=RIGHT><b>Ship To Name: </B></TD>";
						s += "<TD ALIGN=LEFT>" + rsInfo.getString((SMTableorderheaders.TableName + "." + SMTableorderheaders.sShipToName).replace("`", "")).trim() + "</TD></TR>"; 
						s +="<TR><TD ALIGN=RIGHT><b>Bill To Name: </b></TD><TD ALIGN=LEFT>" + rsInfo.getString((SMTableorderheaders.TableName + "." + SMTableorderheaders.sBillToName).replace("`", "")).trim() + "</TD></TR>"; 
						s +="<TR><TD ALIGN=RIGHT><b>Contact: </b></TD><TD ALIGN=LEFT>" + rsInfo.getString((SMTableorderheaders.TableName + "." + SMTableorderheaders.sBillToContact).replace("`", "")).trim() + "</TD></TR>"; 
						s +="<TR><TD ALIGN=RIGHT><b>Phone: </b></TD><TD ALIGN=LEFT>" + rsInfo.getString((SMTableorderheaders.TableName + "." + SMTableorderheaders.sBillToPhone).replace("`", "")).trim() + "</TD></TR>"; 
						s +="<TR><TD ALIGN=RIGHT><b>Order Number:</b></TD><TD ALIGN=LEFT>" + "<A HREF=\"" 
								+ SMUtilities.getURLLinkBase(getServletContext()) 
								+ "smcontrolpanel.SMDisplayOrderInformation?OrderNumber=" + rsInfo.getString(SMTableorderheaders.sOrderNumber).replace("`", "")
								+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sm.getsDBID()
								+ "#CriticalDatesFooter\">" 
								+ rsInfo.getString((SMTableorderheaders.TableName + "." 
								+ SMTableorderheaders.sOrderNumber).replace("`", "")) 
							+ "</A></TD></TR>"; 
					}
					
					if(iType == SMTablecriticaldates.SALES_LEAD_RECORD_TYPE) {
						s +="<TR><TD ALIGN=RIGHT><b>Ship To Name: </b></TD><TD ALIGN=LEFT>" + rsInfo.getString((SMTablebids.TableName + "." + SMTablebids.scustomername).replace("`", "")).trim() + "</TD></TR>"; 
						s +="<TR><TD ALIGN=RIGHT><b>Bill To Name: </b></TD><TD ALIGN=LEFT>" + rsInfo.getString((SMTablebids.TableName + "." + SMTablebids.sprojectname).replace("`", "")).trim() + "</TD></TR>"; 
						s +="<TR><TD ALIGN=RIGHT><b>Contact: </b></TD><TD ALIGN=LEFT>" + rsInfo.getString((SMTablebids.TableName + "." + SMTablebids.scontactname).replace("`", "")).trim() + "</TD></TR>"; 
						s +="<TR><TD ALIGN=RIGHT><b>Phone: </b></TD><TD ALIGN=LEFT>" + rsInfo.getString((SMTablebids.TableName + "." + SMTablebids.sphonenumber).replace("`", "")).trim() + "</TD></TR>"; 
						s +="<TR><TD ALIGN=RIGHT><b>Sales Lead: </b></TD><TD ALIGN=LEFT>" + "<A HREF=\"" 
							+ SMUtilities.getURLLinkBase(getServletContext()) 
							+ "smcontrolpanel.SMEditBidEntry?lid=" + Integer.toString(rsInfo.getInt((SMTablebids.TableName + "." + SMTablebids.lid).replace("`", ""))) 
							+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sm.getsDBID()
							+ "\">" 
							+ Integer.toString(rsInfo.getInt((SMTablebids.TableName + "." 
							+ SMTablebids.lid).replace("`", "")))  
							+ "</A></TD></TR>";	
					}
					
					if( iType == SMTablecriticaldates.PURCHASE_ORDER_RECORD_TYPE) {
						s +="<TR><TD ALIGN=RIGHT><b>Name: </b></TD><TD ALIGN=LEFT>" + rsInfo.getString((SMTableicpoheaders.TableName + "." + SMTableicpoheaders.svendorname).replace("`", "")).trim() + "</TD></TR>"; 
						s +="<TR><TD ALIGN=RIGHT><b>Number: </b></TD><TD ALIGN=LEFT>" + rsInfo.getString((SMTableicpoheaders.TableName + "." + SMTableicpoheaders.svendor).replace("`", "")).trim() + "</TD></TR>"; 
						s +="<TR><TD ALIGN=RIGHT><b>Status: </b></TD><TD ALIGN=LEFT>" + SMTableicpoheaders.getStatusDescription(rsInfo.getInt((SMTableicpoheaders.TableName + "." + SMTableicpoheaders.lstatus))) + "</TD></TR>"; 
						s +="<TR><TD ALIGN=RIGHT><b>Expected: </b></TD><TD ALIGN=LEFT>" + clsDateAndTimeConversions.utilDateToString(rsInfo.getDate((SMTableicpoheaders.TableName + "." + SMTableicpoheaders.datexpecteddate)), "M/d/yyyy") + "</TD></TR>"; 
						s +="<TR><TD ALIGN=RIGHT><b>PO Number: </b></TD><TD ALIGN=LEFT>" + "<A HREF=\"" 
							+ SMUtilities.getURLLinkBase(getServletContext()) 
							+ "smic.ICEditPOEdit?lid=" 
							+ Long.toString(rsInfo.getLong((SMTableicpoheaders.TableName + "." + SMTableicpoheaders.lid))) 
							+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sm.getsDBID()
							+ "#CriticalDatesFooter\">" 
							+ Long.toString(rsInfo.getLong((SMTableicpoheaders.TableName + "." + SMTableicpoheaders.lid))) 
							+ "</A></TD></TR>";	
					}
				}
				rsInfo.close();
			}catch(SQLException e){
				throw e;
			}
		}
		
		//Responsible
		ArrayList<String> sValues = new ArrayList<String>();
		ArrayList<String> sDescriptions = new ArrayList<String>();

		//Select list:
		//First, add a blank to make sure the user selects one:
		sValues.add("");
		sDescriptions.add("-- Select a user --");

		try{
			ResultSet rsUsers = clsDatabaseFunctions.openResultSet(
					SMMySQLs.Get_User_List_SQL(false),
					getServletContext(),
					sm.getsDBID(),
					"MySQL",
					this.toString() + " reading users table - user: " + sm.getUserID()
					+ " - "
					+ sm.getFullUserName()
			);
			while (rsUsers.next()){
				sValues.add((String) rsUsers.getString(SMTableusers.lid).trim());
				sDescriptions.add(
						(String) rsUsers.getString(SMTableusers.sUserFirstName).trim()
								+ " " + rsUsers.getString(SMTableusers.sUserLastName).trim());
			}
			rsUsers.close();
		}catch(SQLException e){
			throw e;
		}

		s+= clsCreateHTMLTableFormFields.Create_Edit_Form_List_Row(
				SMCriticalDateEntry.ParamResponsibleUserID, 
				sValues, 
				entry.getresponsibleuserid().replace("\"", "&quot;"), 
				sDescriptions, 
				"<B>Responsible:</B>", 
				""
		);
		s += "<INPUT TYPE=HIDDEN NAME=\"" + SMCriticalDateEntry.ParamResponsibleUserFullName + "\" ID=\"" + SMCriticalDateEntry.ParamResponsibleUserFullName + "\" VALUE=\"" + entry.getresponsibleuserfullname() + "\">";
		
		s+= clsCreateHTMLTableFormFields.Create_Edit_Form_List_Row(
				SMCriticalDateEntry.ParamAssignedbyUserID, 
				sValues, 
				entry.getassignedbyuserid().replace("\"", "&quot;"), 
				sDescriptions, 
				"<B>Assigned by:</B>", 
				""
		);
		s += "<INPUT TYPE=HIDDEN NAME=\"" + SMCriticalDateEntry.ParamAssignedbyUserFullName + "\" ID=\"" + SMCriticalDateEntry.ParamAssignedbyUserFullName + "\" VALUE=\"" + entry.getassignedbyuserid() + "\">";
		
		s += "<INPUT TYPE=HIDDEN NAME=\"" + SMCriticalDateEntry.ParamiType + "\" VALUE=\"" + entry.getitype() + "\">";
		s += "</TABLE>";
		return s;
	}
	
	private String createSaveButton(){
		return "<button type=\"button\""
				+ " value=\"" + SAVE_BUTTON_LABEL + "\""
				+ " name=\"" + SAVE_BUTTON_LABEL + "\""
				+ " onClick=\"save();\">"
				+ SAVE_BUTTON_LABEL
				+ "</button>\n";
	}
	
	private String createDeleteButton(){
		String s = "";
		
		s = "<button type=\"button\""
			+ " value=\"" + DELETE_BUTTON_LABEL + "\""
			+ " name=\"" + DELETE_BUTTON_LABEL + "\""
			+ " onClick=\"isdelete();\">"
			+ DELETE_BUTTON_LABEL
			+ "</button>\n";
		
		s += "<INPUT TYPE='CHECKBOX' NAME='" + CONFIRM_DELETE_CHECKBOX 
			+ "' VALUE='" + CONFIRM_DELETE_CHECKBOX + "' > Check to confirm before deleting";
		return s;
	}
	
	private String getScripts() {
		String s ="";
		
		s += "<NOSCRIPT>\n"
				+ "    <font color=red>\n"
				+ "    <H3>This page requires that JavaScript be enabled to function properly</H3>\n"
				+ "    </font>\n"
				+ "</NOSCRIPT>\n"
			;
 
			s += "<script type='text/javascript'>\n";								
			//Delete:
			s += "function isdelete(){\n"
				+ "        document.getElementById(\"" + COMMAND_FLAG + "\").value = \"" + DELETE_COMMAND_VALUE + "\";\n"
				+ "        document.forms[\"" +FORM_NAME + "\"].submit();\n"
				+ "}\n"
			;
			//Save
			s += "function save(){\n"
				+ "    var selectedResponsible = document.getElementsByName(\"" + SMCriticalDateEntry.ParamResponsibleUserID + "\")[0];\n"
				+ "    var selectedAssigned = document.getElementsByName(\"" + SMCriticalDateEntry.ParamAssignedbyUserID + "\")[0];\n"
				+ "	   document.getElementById(\"" + SMCriticalDateEntry.ParamResponsibleUserFullName + "\").value = selectedResponsible.options[selectedResponsible.selectedIndex].text;\n"
				+ "	   document.getElementById(\"" + SMCriticalDateEntry.ParamAssignedbyUserFullName + "\").value = selectedAssigned.options[selectedAssigned.selectedIndex].text;\n"
				+ "    document.getElementById(\"" + COMMAND_FLAG + "\").value = \"" + SAVE_COMMAND_VALUE + "\";\n"
				+ "    document.getElementById(\"" + FORM_NAME + "\").submit();\n"
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
