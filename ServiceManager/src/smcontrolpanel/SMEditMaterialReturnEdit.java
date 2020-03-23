package smcontrolpanel;

import java.io.IOException;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import SMClasses.SMMaterialReturn;
import SMDataDefinition.SMTablematerialreturns;
import ServletUtilities.clsManageRequestParameters;
import ServletUtilities.clsServletUtilities;

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

		if (currentSession.getAttribute(SMMaterialReturn.ParamObjectName) != null ){
			entry = (SMMaterialReturn) currentSession.getAttribute(SMMaterialReturn.ParamObjectName);
			currentSession.removeAttribute(SMMaterialReturn.ParamObjectName);
			//But if it's NOT a 'resubmit', meaning this class was called for the first time to 
			//edit, we'll pick up the ID or key from the request and try to load the entry:
		}else if(ServletUtilities.clsManageRequestParameters.get_Request_Parameter(SMMaterialReturn.Paramlid, request).compareToIgnoreCase("-1") == 0 ){
			//IF new entry, and clicked find vendor: don't load
		}else {
			if (!smedit.getAddingNewEntryFlag()){ 
				try {
					entry.load(getServletContext(), smedit.getsDBID(), smedit.getUserID(), smedit.getFullUserName());
				} catch (Exception e) {
					smedit.redirectAction(e.getMessage(), "", SMMaterialReturn.Paramlid + "=" + entry.getslid());
					return;
				}
			}
		}

		String sWarning = clsManageRequestParameters.get_Request_Parameter("Warning", request);
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
		if(sWarning.compareToIgnoreCase("")!=0) {
			smedit.getPWOut().println("  <script type=\"text/javascript\">\n" + 
					"    BatchEntry();\n" + 
					"    POEntry();\n" +
					"  </script>\n" + 
					"</body>");
		}
		return;
	}
	private String getEditHTML(SMMasterEditEntry sm, SMMaterialReturn entry) throws SQLException{
		String s =  "";
		s = Script(s);
		s += "<TABLE BORDER=1>";
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
			s += "<TR>\n<TD ALIGN=RIGHT><B>" + SMTablematerialreturns.sidlabel + "</B>:</TD>\n<TD>\n<B>" 
					+ sID 
					+ "<INPUT TYPE=HIDDEN NAME=\"" + SMMaterialReturn.Paramlid + "\" VALUE=\"" 
					+ sID + "\">"
					+ "</B></TD>\n</TR>\n";
			//but if it IS a new entry:
		}else{
			s += "<TR>\n<TD ALIGN=RIGHT><B>" + SMTablematerialreturns.sidlabel + "</B>:</TD>\n<TD>\n<B>" 
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
		s += "<TR>\n<TD ALIGN=RIGHT><B>" + SMTablematerialreturns.sinitiatedlabel + "<B>:</TD>\n"
				+ "<TD>\n" 
				+ sInitiatedByString 
				+ "<INPUT TYPE=HIDDEN NAME=\"" + SMMaterialReturn.Paramdatinitiated + "\" VALUE=\"" + entry.getsdatinitiated() + "\">"
				+ "<INPUT TYPE=HIDDEN NAME=\"" + SMMaterialReturn.Paramlinitiatedbyid + "\" VALUE=\"" + entry.getlinitiatedbyid() + "\">"
				+ "<INPUT TYPE=HIDDEN NAME=\"" + SMMaterialReturn.Paramsinitiatedbyfullname + "\" VALUE=\"" + entry.getsinitiatedbyfullname() + "\">"
				+ "</TD>\n"
				+ "</TR>\n"
				;

		//Work order ID:
		s += "<TR>\n<TD ALIGN=RIGHT><B>" + SMTablematerialreturns.sworkorderidlabel  + ": </B></TD>\n";
		s += "<TD ALIGN=LEFT><INPUT TYPE=TEXT NAME=\"" + SMMaterialReturn.Paramiworkorderid + "\""
				+ " VALUE=\"" + entry.getsworkorderid().replace("\"", "&quot;") + "\""
				+ "SIZE=" + "13"
				+ "MAXLENGTH=" + Integer.toString(13)
				+ "></TD>\n"
				+ "</TR>\n"
				;

		//Trimmed order number:
		s += "<TR>\n<TD ALIGN=RIGHT><B>" + SMTablematerialreturns.sordernumberlabel  + ": </B></TD>\n";
		s += "<TD ALIGN=LEFT><INPUT TYPE=TEXT NAME=\"" + SMMaterialReturn.Paramstrimmedordernumber + "\""
				+ " VALUE=\"" + entry.getstrimmedordernumber().replace("\"", "&quot;") + "\""
				+ "SIZE=" + "13"
				+ "MAXLENGTH=" + Integer.toString(SMTablematerialreturns.strimmedordernumberlength)
				+ "></TD>\n"
				+ "</TR>\n"
				;

		//Description:
		s += "<TR>\n<TD ALIGN=RIGHT><B>" + "<B>" + SMTablematerialreturns.sdescriptionlabel + ": <FONT COLOR=RED>*Required*</FONT></B>"  + " </B></TD>\n";
		s += "<TD ALIGN=LEFT><TEXTAREA NAME=\"" + SMMaterialReturn.Paramsdescription + "\""
				+"rows=\"" + "2" + "\""
				+ " style = \" width: 100%; \""
				+ ">" + entry.getsdescription().replace("\"", "&quot;") 
				+ "</TEXTAREA>"
				+ "</TD>\n"
				+ "</TR>\n"
				;

		//Comments:
		s += "<TR>\n<TD ALIGN=RIGHT VALIGN=TOP><B>" + SMTablematerialreturns.scommentslabel + "</B>:</TD>\n";
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
		s += "<TR>\n<TD ALIGN=RIGHT><B>" + SMTablematerialreturns.sresolvedlabel + "?</B></TD>\n";
		s += "<TD ALIGN=LEFT> <INPUT TYPE=CHECKBOX" + sCheckBoxChecked
				+ " NAME=\"" + SMMaterialReturn.Paramiresolved + "\" width=0.25>&nbsp;" + sResolvedBy + "</TD>\n"
				;
		s += "</TR>\n";

		//Resolution Comments:
	/*	s += "<TR>\n<TD ALIGN=RIGHT VALIGN=TOP><B>" + SMTablematerialreturns.sresolutioncommentslabel + "</B>:</TD>\n";
		s += "<TD>\n"
				+ "<TEXTAREA NAME=\"" + SMMaterialReturn.Parammresolutioncomments + "\""
				+ " rows=\"" + "3" + "\""
				+ " style = \" width: 100%; \""
				+ ">"
				+ entry.getsresolutioncomments().replace("\"", "&quot;")
				+ "</TEXTAREA>"
				+ "</TD>\n"
				+ "</TR>\n"
				;*/
		
		s += "</TABLE>";
		return s;
	}

	public String Script(String s) {
		s+= "<script>\n";
		s +=( "function BatchEntry(){\n"
				+ "\tdocument.getElementById(\"BatchEntryLink\").style.visibility = \"hidden\";\n"
				+ "}\n");

		s +=( "function POEntry(){\n"
				+ "\tdocument.getElementById(\"POLink\").style.visibility = \"hidden\";\n"
				+ "}\n");

		s+="</script>";

		return s;
	}

	public void doGet(HttpServletRequest request,
			HttpServletResponse response)
					throws ServletException, IOException {

		doPost(request, response);
	}
}
