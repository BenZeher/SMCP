package smcontrolpanel;

import java.io.IOException;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import SMClasses.SMMaterialReturn;
import SMDataDefinition.SMMasterStyleSheetDefinitions;
import SMDataDefinition.SMTablematerialreturns;
import ServletUtilities.clsServletUtilities;
import smap.APVendor;

public class SMEditMaterialReturnEdit  extends HttpServlet {

	private static final long serialVersionUID = 1L;

	public void doPost(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
		
		SMMaterialReturn entry = new SMMaterialReturn(request);
		SMMasterEditEntry smedit = new SMMasterEditEntry(
				request,
				response,
				getServletContext(),
				entry.getObjectName(),
				SMUtilities.getFullClassName(this.toString()),
				"smcontrolpanel.SMEditMaterialReturnAction",
				"smcontrolpanel.SMUserLogin",
				"Go back to user login",
				SMSystemFunctions.SMEditMaterialReturns
				);
		
		if (!smedit.processSession(getServletContext(), SMSystemFunctions.SMEditMaterialReturns)){
			smedit.getPWOut().println("Error in process session: " + smedit.getErrorMessages());
			return;
		}
		//In case someone entered an ID number on the previous screen when they wanted to add a new record, reset it here:
		if(smedit.getAddingNewEntryFlag()){
			entry.setslid("-1");
		}
		
		//If this is a 'resubmit', meaning it's being called by the 'Action' class, then
		//the session will have an entry object in it, and that's what we'll pick up.
		HttpSession currentSession = smedit.getCurrentSession();
		
	    if (currentSession.getAttribute(SMMaterialReturn.ParamObjectName) != null){
	    	entry = (SMMaterialReturn) currentSession.getAttribute(SMMaterialReturn.ParamObjectName);
	    	currentSession.removeAttribute(SMMaterialReturn.ParamObjectName);
	    //But if it's NOT a 'resubmit', meaning this class was called for the first time to 
	    //edit, we'll pick up the ID or key from the request and try to load the entry:
	    }else{
	    	if (!smedit.getAddingNewEntryFlag()){
	    		try {
					entry.load(getServletContext(), smedit.getsDBID(), smedit.getUserID(), smedit.getFullUserName());
				} catch (Exception e) {
					smedit.redirectAction(e.getMessage(), "", SMMaterialReturn.Paramlid + "=" + entry.getslid());
					return;
				}
	    	}
	    }
	    
	    //If there is a vendor in the request, then that might mean we are coming back from a vendor 'find', and we'll put that in the entry:
	    if (ServletUtilities.clsManageRequestParameters.get_Request_Parameter(SMMaterialReturn.Paramsvendoracct, request).compareToIgnoreCase("") != 0){
	    	entry.setsvendoracct(ServletUtilities.clsManageRequestParameters.get_Request_Parameter(SMMaterialReturn.Paramsvendoracct, request));
	    }
	    
	    smedit.printHeaderTable();
	    smedit.getPWOut().println(SMUtilities.getMasterStyleSheetLink());
	    //smedit.getPWOut().println(getJavascript());
	    
	    //Add a link to return to the original URL:
	    if (smedit.getOriginalURL().trim().compareToIgnoreCase("") !=0 ){
		    smedit.getPWOut().println(
		    		"<A HREF=\"" + smedit.getOriginalURL().replace("*", "&") + "\">" 
		    		+ "Back to report" + "</A>");
	    }
	    
		smedit.getPWOut().println("<BR>");
		
	    try {
			smedit.createEditPage(getEditHTML(smedit, entry), "");
		} catch (SQLException e) {
    		String sError = "Could not create edit page - " + e.getMessage();
    		smedit.redirectAction(sError, "", SMMaterialReturn.Paramlid + "=" + entry.getslid());
			return;
		}
	    return;
	}
	private String getEditHTML(SMMasterEditEntry sm, SMMaterialReturn entry) throws SQLException{

		String s = "<TABLE BORDER=1>";
		String sID = "";
		if (
			//If we are NOT adding a new return:
			(!sm.getAddingNewEntryFlag())
			// OR if it's NOT equal to -1:
			|| (entry.getslid().compareToIgnoreCase("-1") != 0)
		){
			// Then set the value of sID to the ID of this current material return
			sID = entry.getslid();
		}
		//If it's not a new entry:
		if(entry.getslid().compareToIgnoreCase("-1")!=0){
			s += "<TR>\n<TD ALIGN=RIGHT><B>Material Return ID</B>:</TD>\n<TD>\n<B>" 
					+ sID 
					+ "<INPUT TYPE=HIDDEN NAME=\"" + SMMaterialReturn.Paramlid + "\" VALUE=\"" 
					+ sID + "\">"
					+ "</B></TD>\n</TR>\n";
		//but if it IS a new entry:
		}else{
		s += "<TR>\n<TD ALIGN=RIGHT><B>Material Return ID</B>:</TD>\n<TD>\n<B>" 
			+ "(NEW)" 
			+ "<INPUT TYPE=HIDDEN NAME=\"" + SMMaterialReturn.Paramlid + "\" VALUE=\"" 
			+ entry.getslid() + "\">"
			+ "</B></TD>\n</TR>\n";
		}
		
		//Initiated by:
		String sInitiatedByString = "";
		if (entry.getlinitiatedbyid().compareToIgnoreCase("0") != 0){
			sInitiatedByString = entry.getsinitiatedbyfullname() 
				+ " on " + entry.getsdatinitiated() + ".";
		}
		if (entry.getsinitiatedbyfullname().trim().compareToIgnoreCase("") == 0){
			entry.setsinitiatedbyfullname(sm.getFullUserName());
		}
		s += "<TR>\n<TD ALIGN=RIGHT><B>Initiated by<B>:</TD>\n"
			+ "<TD>\n" 
			+ sInitiatedByString 
			+ "<INPUT TYPE=HIDDEN NAME=\"" + SMMaterialReturn.Paramdatinitiated + "\" VALUE=\"" + entry.getsdatinitiated() + "\">"
			+ "<INPUT TYPE=HIDDEN NAME=\"" + SMMaterialReturn.Paramlinitiatedbyid + "\" VALUE=\"" + entry.getlinitiatedbyid() + "\">"
			+ "<INPUT TYPE=HIDDEN NAME=\"" + SMMaterialReturn.Paramsinitiatedbyfullname + "\" VALUE=\"" + entry.getsinitiatedbyfullname() + "\">"
			+ "</TD>\n"
			+ "</TR>\n"
		;
		
		//Work order ID:
		s += "<TR>\n<TD ALIGN=RIGHT><B>" + "Work order ID:"  + " </B></TD>\n";
		s += "<TD ALIGN=LEFT><INPUT TYPE=TEXT NAME=\"" + SMMaterialReturn.Paramiworkorderid + "\""
			+ " VALUE=\"" + entry.getsworkorderid().replace("\"", "&quot;") + "\""
			+ "SIZE=" + "13"
			+ "MAXLENGTH=" + Integer.toString(13)
			+ "></TD>\n"
			+ "</TR>\n"
		;

		//Trimmed order number:
		s += "<TR>\n<TD ALIGN=RIGHT><B>" + "Order number:"  + " </B></TD>\n";
		s += "<TD ALIGN=LEFT><INPUT TYPE=TEXT NAME=\"" + SMMaterialReturn.Paramstrimmedordernumber + "\""
			+ " VALUE=\"" + entry.getstrimmedordernumber().replace("\"", "&quot;") + "\""
			+ "SIZE=" + "13"
			+ "MAXLENGTH=" + Integer.toString(SMTablematerialreturns.strimmedordernumberlength)
			+ "></TD>\n"
			+ "</TR>\n"
		;

		//Description:
		s += "<TR>\n<TD ALIGN=RIGHT><B>" + "<B>Description: <FONT COLOR=RED>*Required*</FONT></B>"  + " </B></TD>\n";
		s += "<TD ALIGN=LEFT><TEXTAREA NAME=\"" + SMMaterialReturn.Paramsdescription + "\""
			+"rows=\"" + "2" + "\""
			+ " style = \" width: 100%; \""
			+ ">" + entry.getsdescription().replace("\"", "&quot;") 
			+ "</TEXTAREA>"
			+ "</TD>\n"
			+ "</TR>\n"
		;

		//Comments:
		s += "<TR>\n<TD ALIGN=RIGHT VALIGN=TOP><B>Comments</B>:</TD>\n";
		s += "<TD>\n"
			+ "<TEXTAREA NAME=\"" + SMMaterialReturn.Parammcomments + "\""
			+ " rows=\"" + "3" + "\""
			//+ " cols=\"" + Integer.toString(iCols) + "\""
			+ " style = \" width: 100%; \""
			+ ">"
			+ entry.getscomments().replace("\"", "&quot;")
			+ "</TEXTAREA>"
			+ "</TD>\n"
			+ "</TR>\n"
		;
        
		//Resolved?
		String sResolvedBy = 
			"<INPUT TYPE=HIDDEN NAME=\"" + SMMaterialReturn.Paramdatresolved + "\" VALUE=\"" + entry.getsdatresolved() + "\">"
			+ "<INPUT TYPE=HIDDEN NAME=\"" + SMMaterialReturn.Paramlresolvedbyid + "\" VALUE=\"" + entry.getlresolvedbyid() + "\">"
			+ "<INPUT TYPE=HIDDEN NAME=\"" + SMMaterialReturn.Paramsresolvedbyfullname + "\" VALUE=\"" + entry.getsresolvedbyfullname() + "\">"		
		;
		if (entry.getsresolved().compareToIgnoreCase("1") == 0){
			sResolvedBy += "By " + entry.getsresolvedbyfullname() + " on " + entry.getsdatresolved() + ".";
		}
		String sCheckBoxChecked = "";
		if (entry.getsresolved().compareToIgnoreCase("1") == 0){
			sCheckBoxChecked = clsServletUtilities.CHECKBOX_CHECKED_STRING;
		}
		s += "<TR>\n<TD ALIGN=RIGHT><B>" + "Resolved?" + "</B></TD>\n";
		s += "<TD ALIGN=LEFT> <INPUT TYPE=CHECKBOX" + sCheckBoxChecked
			+ " NAME=\"" + SMMaterialReturn.Paramiresolved + "\" width=0.25>&nbsp;" + sResolvedBy + "</TD>\n"
		;
		s += "</TR>\n";
		
		//Resolution Comments:
		s += "<TR>\n<TD ALIGN=RIGHT VALIGN=TOP><B>Resolution Comments</B>:</TD>\n";
		s += "<TD>\n"
			+ "<TEXTAREA NAME=\"" + SMMaterialReturn.Parammresolutioncomments + "\""
			+ " rows=\"" + "3" + "\""
			+ " style = \" width: 100%; \""
			+ ">"
			+ entry.getsresolutioncomments().replace("\"", "&quot;")
			+ "</TEXTAREA>"
			+ "</TD>\n"
			+ "</TR>\n"
		;
		//Status Options
		s += "<TR>\n<TD ALIGN=RIGHT VALIGN=TOP><B>Credit Status:</B></TD>\n";
		s += "<TD>\n";
		String sChecked = "";
		if (entry.getscreditstatus().compareToIgnoreCase(Integer.toString(SMTablematerialreturns.STATUS_CREDITNOTEXPECTED)) == 0){
			sChecked = " checked ";
		}else{
			sChecked = "";
		}
		s += "<INPUT TYPE='RADIO' NAME='" + SMMaterialReturn.Paramicreditstatus + "' VALUE= "+ SMTablematerialreturns.STATUS_CREDITNOTEXPECTED + sChecked + " >Credit Not Expected<BR>";
		if (entry.getscreditstatus().compareToIgnoreCase(Integer.toString(SMTablematerialreturns.STATUS_CREDITANTICIPATED)) == 0){
			sChecked = " checked ";
		}else{
			sChecked = "";
		}
		s += "<INPUT TYPE='RADIO' NAME='" + SMMaterialReturn.Paramicreditstatus + "' VALUE= "+ SMTablematerialreturns.STATUS_CREDITANTICIPATED + sChecked + " >Credit Anticipated<BR>";
		if (entry.getscreditstatus().compareToIgnoreCase(Integer.toString(SMTablematerialreturns.STATUS_CREDITRECEIVED)) == 0){
			sChecked = " checked ";
		}else{
			sChecked = "";
		}
		s += "<INPUT TYPE='RADIO' NAME='" + SMMaterialReturn.Paramicreditstatus + "' VALUE= "+ SMTablematerialreturns.STATUS_CREDITRECEIVED + sChecked + " >Credit Received<BR>";
		
		    s+= "</TD>\n"
		    	+ "</TR>\n"
		    ;
		    		
		//'Returned' section:
		s += "<TR class = \" " + SMMasterStyleSheetDefinitions.TABLE_HEADING + " \" ><TD ALIGN=LEFT COLSPAN=2><B>VENDOR RETURNS</B>:</TD>\n</TR>\n";
		
		if (entry.getstobereturned().compareToIgnoreCase("1") == 0){
			sCheckBoxChecked = clsServletUtilities.CHECKBOX_CHECKED_STRING;
		}else{
			sCheckBoxChecked = "";
		}
		s += "<TR>\n<TD ALIGN=RIGHT><B>" + "To be returned?" + "</B></TD>\n";
		s += "<TD ALIGN=LEFT> <INPUT TYPE=CHECKBOX" + sCheckBoxChecked
			+ " NAME=\"" + SMMaterialReturn.Paramitobereturned + "\" width=0.25></TD>\n"
			+ "</TR>\n"
		;
		
		s += "<TR>\n<TD ALIGN=RIGHT><B>" + "Vendor:"  + " </B></TD>\n";
		s += "<TD ALIGN=LEFT><INPUT TYPE=TEXT NAME=\"" + SMMaterialReturn.Paramsvendoracct + "\""
			+ " VALUE=\"" + entry.getsvendoracct().replace("\"", "&quot;") + "\""
			+ "SIZE=" + "13"
			+ "MAXLENGTH= " + Integer.toString(SMTablematerialreturns.svendoracctlength)
			+ ">"
			
			//Vendor finder:
			+ "&nbsp;<A HREF=\""
			+ APVendor.getFindVendorLink(
				clsServletUtilities.getFullClassName(this.toString()), 
				SMMaterialReturn.Paramsvendoracct, 
				SMMaterialReturn.Paramlid + "=" + entry.getslid(),
				getServletContext(),
				sm.getsDBID()
			)
			+ "\"> Find vendor</A>"
			
			+ "</TD>\n"
			+ "</TR>\n"
		;
		
		//Purchase order number:
		s += "<TR>\n<TD ALIGN=RIGHT><B>" + "PO Number:"  + " </B></TD>\n";
		s += "<TD ALIGN=LEFT><INPUT TYPE=TEXT NAME=\"" + SMMaterialReturn.Paramiponumber + "\""
			+ " VALUE=\"" + entry.getsponumber().replace("\"", "&quot;") + "\""
			+ "SIZE=" + "13"
			+ "MAXLENGTH= 10"
			+ "></TD>\n"
			+ "</TR>\n"
		;
		
		s += "</TABLE>";
		return s;
	}
	/*
	String getJavascript(){
		String s = "";
		s = "<script type= 'text/JavaScript'>\n"
				+" function invisible(){\n"
	    		+ "       document.getElementById(\"itemId\").style.display = \"none\";\n"
	    		+ "       document.getElementById(\"textBoxId\").value = \"\";\n"
	    		//+ "       document.getElementById(\"itemId\").children.value = \"\"\n;"
	    		+ " }\n"
	    		+"  $(document).ready(function(){\n"
	    		+"        if(document.getElementById(\"invoicechecked\").checked == false){\n"
	    		+ "       document.getElementById(\"itemId\").style.display = \"none\";\n"
	    		+ "       document.getElementById(\"textBoxId\").value = \"\";\n"
	    		+"         }\n   "
	    		+"       });\n"
	    		+ " function visible () {\n"
	    		+ "       document.getElementById(\"itemId\").style.display = \"block\";\n  "
	    		+ " }\n"
	    		+ " </script>\n";
		return s;
	}
	*/
	public void doGet(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
		
		doPost(request, response);
	}
}
