package smcontrolpanel;

import java.io.IOException;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import smar.ARCustomer;
import SMClasses.SMOrderHeader;
import SMClasses.SMTaxCertificate;
import SMDataDefinition.SMTabletaxcertificates;

public class SMEditTaxCertificatesEdit  extends HttpServlet {

	private static final long serialVersionUID = 1L;

	public void doPost(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
		
		SMTaxCertificate entry = new SMTaxCertificate(request);
		SMMasterEditEntry smedit = new SMMasterEditEntry(
				request,
				response,
				getServletContext(),
				entry.getObjectName(),
				SMUtilities.getFullClassName(this.toString()),
				"smcontrolpanel.SMEditTaxCertificatesAction",
				"smcontrolpanel.SMUserLogin",
				"Go back to user login",
				SMSystemFunctions.SMEditTaxCertificates
				);
		
		if (!smedit.processSession(getServletContext(), SMSystemFunctions.SMEditTaxCertificates)){
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
		
	    if (currentSession.getAttribute(SMTaxCertificate.ParamObjectName) != null){
	    	entry = (SMTaxCertificate) currentSession.getAttribute(SMTaxCertificate.ParamObjectName);
	    	currentSession.removeAttribute(SMTaxCertificate.ParamObjectName);
	    //But if it's NOT a 'resubmit', meaning this class was called for the first time to 
	    //edit, we'll pick up the ID or key from the request and try to load the entry:
	    }else{
	    	if (!smedit.getAddingNewEntryFlag()){
	    		try {
					entry.load(getServletContext(), smedit.getsDBID(), smedit.getUserID(), smedit.getFullUserName());
				} catch (Exception e) {
					smedit.redirectAction(e.getMessage(), "", SMTaxCertificate.Paramlid + "=" + entry.getslid());
					return;
				}
	    	}
	    }
	    smedit.printHeaderTable();
	    
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
    		smedit.redirectAction(sError, "", SMTaxCertificate.Paramlid + "=" + entry.getslid());
			return;
		}
	    return;
	}
	private String getEditHTML(SMMasterEditEntry sm, SMTaxCertificate entry) throws SQLException{

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
				s += "<TR>\n<TD ALIGN=RIGHT><B>Tax Certificate ID</B>:</TD>\n<TD>\n<B>" 
					+ sID 
					+ "<INPUT TYPE=HIDDEN NAME=\"" + SMTaxCertificate.Paramlid + "\" VALUE=\"" 
					+ sID + "\">"
					+ "</B></TD>\n</TR>\n";
		//but if it IS a new entry:
		}else{
				s += "<TR>\n<TD ALIGN=RIGHT><B>Tax Certificate ID</B>:</TD>\n<TD>\n<B>" 
					+ "(NEW)" 
					+ "<INPUT TYPE=HIDDEN NAME=\"" + SMTaxCertificate.Paramlid + "\" VALUE=\"" 
					+ entry.getslid() + "\">"
					+ "</B></TD>\n</TR>\n";
				}
		
		//Created by:
		String sCreatedBy = "N/A";
		
		s	+= "<INPUT TYPE=HIDDEN NAME=\"" + SMTaxCertificate.Paramdatcreated + "\" VALUE=\"" + entry.getdatcreated() + "\">"
			+ "<INPUT TYPE=HIDDEN NAME=\"" + SMTaxCertificate.Paramlcreatedbyuserid + "\" VALUE=\"" + entry.getlcreatedbyuserid() + "\">"
			+ "<INPUT TYPE=HIDDEN NAME=\"" + SMTaxCertificate.Paramscreatedbyfullname + "\" VALUE=\"" + entry.getscreatedbyfullname() + "\">";
		//If it's not a new entry
		if(entry.getslid().compareToIgnoreCase("-1") != 0){
			sCreatedBy = entry.getscreatedbyfullname();
		}
		s += "<TR>\n<TD ALIGN=RIGHT><B>Created by<B>:</TD>\n"
			+ "<TD>\n" + sCreatedBy 
			+ "</TD>\n"
			+ "</TR>\n"
			;	
		//Customer Number:
				s += "<TR>\n<TD ALIGN=RIGHT><B>" + "Customer Number<FONT COLOR=RED>*</FONT>:"  + " </B></TD>\n";
				s += "<TD ALIGN=LEFT><INPUT TYPE=TEXT NAME=\"" + SMTaxCertificate.Paramscustomernumber + "\""
					+ " VALUE=\"" + entry.getscustomernumber().replace("\"", "&quot;") + "\""
					+ " SIZE=" + "13"
					+ " MAXLENGTH=" + Integer.toString(SMTabletaxcertificates.scustomernumberlength) + ">"				
					+ "</TD>\n"					
					+ "</TR>\n"
				;
		//Customer Name (from customer record)
				ARCustomer customer = new ARCustomer(entry.getscustomernumber().replace("\"", "&quot;"));
				if(!customer.load(getServletContext(), sm.getsDBID())){
					s += "<TR>\n<TD ALIGN=RIGHT><B>" + "Customer Name:"  + " </B></TD>\n";
					s += "<TD ALIGN=LEFT>" + "<FONT COLOR=RED>*<I>Invalid customer number<I></FONT>"
						+ "</TD>\n"
						+ "</TR>\n"
					;
				}else{
					s += "<TR>\n<TD ALIGN=RIGHT><B>" + "Customer Name:"  + " </B></TD>\n";
					s += "<TD ALIGN=LEFT>" + customer.getM_sCustomerName().replace("\"", "&quot;")
						+ "</TD>\n"
						+ "</TR>\n"
				;
				}
		//Entered Customer Name (Customer name entered manually)
		//TODO Remove this field after all tax certificate records have been saved with a valid customer number
				s += "<TR>\n<TD ALIGN=RIGHT><B>" + "Entered Customer Name:"  + " </B></TD>\n";
				s += "<TD ALIGN=LEFT><INPUT TYPE=TEXT NAME=\"" + SMTaxCertificate.Paramscustomername + "\""
						+ "VALUE=\"" + entry.getscustomername().replace("\"", "&quot;") + "\""
						+ " SIZE=" + "60"		
						//+ " readonly>"
					+ "</TD>\n"
					+ "</TR>\n"
				;

		//Job Number:
				s += "<TR>\n<TD ALIGN=RIGHT><B>" + "<B>Job Number:</B>"  + " </B></TD>\n";
				s += "<TD ALIGN=LEFT><INPUT TYPE=TEXT NAME=\"" + SMTaxCertificate.Paramsjobnumber + "\""
					+ " VALUE=\"" + entry.getsjobnumber().replace("\"", "&quot;") + "\""
					+ " SIZE=" + "13"
					+ " MAXLENGTH=" + Integer.toString(SMTabletaxcertificates.sjobnumberlength)
					+ "></TD>\n"
					+ "</TR>\n"
		;	
		//Ship to Name
				SMOrderHeader order = new SMOrderHeader();
				order.setM_strimmedordernumber(entry.getsjobnumber().replace("\"", "&quot;").trim());
				String sShiptToName = "";
				if(order.load(getServletContext(), sm.getsDBID(), sm.getUserID(), sm.getFullUserName())){
					sShiptToName = order.getM_sShipToName().replace("\"", "&quot;");
					
				}
				s += "<TR>\n<TD ALIGN=RIGHT><B>" + "Ship To Name:"  + " </B></TD>\n";
				s += "<TD ALIGN=LEFT>" + sShiptToName
					+ "</TD>\n"
					+ "</TR>\n"
				;
	
		//Project(s) Location:
				s += "<TR>\n<TD ALIGN=RIGHT><B>Project(s) Location</B>:</TD>\n";
				s += "<TD>\n"
					+ "<TEXTAREA NAME=\"" + SMTaxCertificate.Paramsprojectlocation + "\""
					+ " rows=\"" + "2" + "\""
					+ " style = \" width: 100%; \""
					+ " MAXLENGTH=" + Integer.toString(SMTabletaxcertificates.sprojectlocationlength)
					+ ">"
					+ entry.getsprojectlocation().replace("\"", "&quot;")
					+ "</TEXTAREA>"
					+ "</TD>\n"
					+ "</TR>\n"
				;		
		//Exempt Number:
				s += "<TR>\n<TD ALIGN=RIGHT><B>" + "<B>Exempt Number: </B>"  + " </B></TD>\n";
				s += "<TD ALIGN=LEFT><INPUT TYPE=TEXT NAME=\"" + SMTaxCertificate.Paramsexemptnumber + "\""
					+ " VALUE=\"" + entry.getsexemptnumber().replace("\"", "&quot;") + "\""
					+ " SIZE=" + "40"
					+ " MAXLENGTH=" + Integer.toString(SMTabletaxcertificates.sexemptnumberlength)
					+ "</TD>\n"
					+ "</TR>\n"
				;		
//		//Date Received:

				s += "<TR>\n<TD ALIGN=RIGHT><B>" + "Date Received:"  + " </B></TD>\n"
	    	        + "<TD ALIGN=LEFT><INPUT TYPE=TEXT NAME=\"" + SMTaxCertificate.Paramdatreceived + "\""
	        		+ " VALUE=\"" + entry.getdatreceived().replace("\"", "&quot;") + "\""
	        		+ " SIZE=10"
	        		+ " MAXLENGTH=" + "10"
	        		+ " STYLE=\"width:.75 in;height: 0.25in\""
	        		+ ">"
	        		+ SMUtilities.getDatePickerString(SMTaxCertificate.Paramdatreceived, getServletContext())
	        		+ "<i> Must be in M/D/YYYY Format. (ex: 3/1/2015)</i>"
	        		+ "</TD>\n"
	        		+ "</TR>\n"
	        		;
//		//Date Issued:

				s += "<TR>\n<TD ALIGN=RIGHT><B>" + "Date Issued:"  + " </B></TD>\n"
		 			+ "<TD ALIGN=LEFT><INPUT TYPE=TEXT NAME=\"" + SMTaxCertificate.Paramdatissued+ "\""
	        		+ " VALUE=\"" + entry.getdatissued().replace("\"", "&quot;") + "\""
	        		+ " SIZE=10"
	        		+ " MAXLENGTH=" + "10"
	        		+ " STYLE=\"width:.75 in;height: 0.25in\""
	        		+ ">"
	        		+ SMUtilities.getDatePickerString(SMTaxCertificate.Paramdatissued, getServletContext())
	        		+ "<i> Must be in M/D/YYYY Format. (ex: 3/1/2015)</i>"
	        		+ "</TD>\n"
	        		+ "</TR>\n"
	        		;
//		//Date Expired:
				s += "<TR>\n<TD ALIGN=RIGHT><B>" + "Date Expired:"  + " </B></TD>\n";
				s += "<TD ALIGN=LEFT><INPUT TYPE=TEXT NAME=\"" + SMTaxCertificate.Paramdatexpired+ "\""
	        		+ " VALUE=\"" + entry.getdatexpired().replace("\"", "&quot;") + "\""
	        		+ " SIZE=10"
	        		+ " MAXLENGTH=" + "10"
	        		+ " STYLE=\"width:.75 in;height: 0.25in\""
	        		+ ">"
	        		+ SMUtilities.getDatePickerString(SMTaxCertificate.Paramdatexpired, getServletContext())
	        		+ "<i> Must be in M/D/YYYY Format. (ex: 3/1/2015)</i>"
	        		+ "</TD>\n"
	        		+ "</TR>\n"
	        		;
		
		//Tax Jurisdiction:
				s += "<TR>\n<TD ALIGN=RIGHT><B>" + "<B>Tax Jurisdiction:</B>"  + " </B></TD>\n";
				s += "<TD ALIGN=LEFT><INPUT TYPE=TEXT NAME=\"" + SMTaxCertificate.Paramstaxjurisdiction + "\""
					+ " VALUE=\"" + entry.getstaxjurisdiction().replace("\"", "&quot;") + "\""
					+ " SIZE=" + "5"
					+ " MAXLENGTH=" + Integer.toString(SMTabletaxcertificates.staxjurisdictionlength)
					+ "></TD>\n"
					+ "</TR>\n"
		;

		//Comments:
				s += "<TR>\n<TD ALIGN=RIGHT><B>Comments</B>:</TD>\n";
				s += "<TD>\n"
					+ "<TEXTAREA NAME=\"" + SMTaxCertificate.ParammNotes + "\""
					+ " rows=\"" + "3" + "\""
					+ " style = \" width: 100%; \""
					+ ">"
					+ entry.getmnotes().replace("\"", "&quot;")
					+ "</TEXTAREA>"
					+ "</TD>\n"
					+ "</TR>\n"
		;
		
		//GDocLink:
				s += "<TR>\n<TD ALIGN=RIGHT><B>" + "<B>Document file link:</B>"  + " </B></TD>\n";
				s += "<TD ALIGN=LEFT><INPUT TYPE=TEXT NAME=\"" + SMTaxCertificate.Paramsgdoclink + "\""
					+ " VALUE=\"" + entry.getsgdoclink().replace("\"", "&quot;") + "\""
					+ " SIZE=" + "60"
					+ " MAXLENGTH=" + Integer.toString(SMTabletaxcertificates.sgdoclinklength)
					+ "></TD>\n"
					+ "</TR>\n"
		;

//		//GDocLink:
//		
//			s += "<TR>\n<TD ALIGN=RIGHT><B><FONT SIZE=2>Document file link:</FONT></B></TD>\n&nbsp;"
//				+ "<INPUT TYPE=TEXT NAME=\"" + SMTaxCertificate.Paramsgdoclink + "\""
//				+ " VALUE=\"" + entry.getsgdoclink().replace("\"", "&quot;") + "\""
//				+ "SIZE=" + "90"
//				+ " MAXLENGTH=" + Integer.toString(254)
//				+ "<BR>"
//				+ "</TD>\n"
//			+ "</TR>\n"
//			;
//			s += "<INPUT TYPE=HIDDEN NAME=\"" + SMTaxCertificate.Paramsgdoclink + "\" VALUE=\"" + entry.getsgdoclink() + "\">";
//	
		//Resolved?
//		String sResolvedBy = 
//			"<INPUT TYPE=HIDDEN NAME=\"" + SMTaxCertificate.Paramdatresolved + "\" VALUE=\"" + entry.getsdatresolved() + "\">"
//			+ "<INPUT TYPE=HIDDEN NAME=\"" + SMTaxCertificate.Paramsresolvedby + "\" VALUE=\"" + entry.getsresolvedby() + "\">"
//			+ "<INPUT TYPE=HIDDEN NAME=\"" + SMTaxCertificate.Paramsresolvedbyfullname + "\" VALUE=\"" + entry.getsresolvedbyfullname() + "\">"		
//		;
//		if (entry.getsresolved().compareToIgnoreCase("1") == 0){
//			sResolvedBy += "By " + entry.getsresolvedbyfullname() + " (" + entry.getsresolvedby() + ") on " + entry.getsdatresolved() + ".";
//		}
//		String sCheckBoxChecked = "";
//		if (entry.getsresolved().compareToIgnoreCase("1") == 0){
//			sCheckBoxChecked = SMUtilities.CHECKBOX_CHECKED_STRING;
//		}
//		s += "<TR>\n<TD ALIGN=RIGHT><B>" + "Resolved?" + "</B></TD>\n";
//		s += "<TD ALIGN=LEFT> <INPUT TYPE=CHECKBOX" + sCheckBoxChecked
//			+ " NAME=\"" + SMTaxCertificate.Paramiresolved + "\" width=0.25>&nbsp;" + sResolvedBy + "</TD>\n"
//		;
//		s += "</TR>\n";
		
		//Resolution Comments:
//		s += "<TR>\n<TD ALIGN=RIGHT><B>Resolution Comments</B>:</TD>\n";
//		s += "<TD>\n"
//			+ "<TEXTAREA NAME=\"" + SMTaxCertificate.Parammresolutioncomments + "\""
//			+ " rows=\"" + "3" + "\""
//			+ " style = \" width: 100%; \""
//			+ ">"
//			+ entry.getsresolutioncomments().replace("\"", "&quot;")
//			+ "</TEXTAREA>"
//			+ "</TD>\n"
//			+ "</TR>\n"
//		;
//		//Status Options
//		s += "<TR>\n<TD ALIGN=RIGHT><B>Credit Status:</B>:</TD>\n";
//		s += "<TD>\n";
//		String sChecked = "";
//		if (entry.getscreditstatus().compareToIgnoreCase(Integer.toString(SMTabletaxcertificates.STATUS_CREDITNOTEXPECTED)) == 0){
//			sChecked = " checked ";
//		}else{
//			sChecked = "";
//		}
//		s += "<INPUT TYPE='RADIO' NAME='" + SMTaxCertificate.Paramicreditstatus + "' VALUE= "+ SMTabletaxcertificates.STATUS_CREDITNOTEXPECTED + sChecked + " >Credit Not Expected<BR>";
//		if (entry.getscreditstatus().compareToIgnoreCase(Integer.toString(SMTabletaxcertificates.STATUS_CREDITANTICIPATED)) == 0){
//			sChecked = " checked ";
//		}else{
//			sChecked = "";
//		}
//		s += "<INPUT TYPE='RADIO' NAME='" + SMTaxCertificate.Paramicreditstatus + "' VALUE= "+ SMTabletaxcertificates.STATUS_CREDITANTICIPATED + sChecked + " >Credit Anticipated<BR>";
//		if (entry.getscreditstatus().compareToIgnoreCase(Integer.toString(SMTabletaxcertificates.STATUS_CREDITRECEIVED)) == 0){
//			sChecked = " checked ";
//		}else{
//			sChecked = "";
//		}
//		s += "<INPUT TYPE='RADIO' NAME='" + SMTaxCertificate.Paramicreditstatus + "' VALUE= "+ SMTabletaxcertificates.STATUS_CREDITRECEIVED + sChecked + " >Credit Received<BR>";
//		
//		
//		    s+= "</TD>\n"
//			+ "</TR>\n"
		;
		s += "</TABLE>";
		return s;
	}

	public void doGet(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
		
		doPost(request, response);
	}
}
