package smcontrolpanel;

import java.io.IOException;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import SMClasses.SMVendorReturn;
import SMDataDefinition.SMMasterStyleSheetDefinitions;
import SMDataDefinition.SMTablevendorreturns;
import ServletUtilities.clsDateAndTimeConversions;
import ServletUtilities.clsManageRequestParameters;
import ServletUtilities.clsServletUtilities;
import ServletUtilities.clsStringFunctions;
import smap.APVendor;

public class SMEditVendorReturnEdit   extends HttpServlet {

	private static final long serialVersionUID = 1L;

	public void doPost(HttpServletRequest request,
			HttpServletResponse response)
					throws ServletException, IOException {

		SMVendorReturn entry = new SMVendorReturn(request);
		SMMasterEditEntry smedit = new SMMasterEditEntry(
				request,
				response,
				getServletContext(),
				entry.getObjectName(),
				SMUtilities.getFullClassName(this.toString()),
				"smcontrolpanel.SMEditVendorReturnAction",
				"smcontrolpanel.SMUserLogin",
				"Go back to user login",
				SMSystemFunctions.SMEditVendorReturns
				);

		if (!smedit.processSession(getServletContext(), SMSystemFunctions.SMEditVendorReturns)){
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

		if (currentSession.getAttribute(SMVendorReturn.ParamObjectName) != null ){
			entry = (SMVendorReturn) currentSession.getAttribute(SMVendorReturn.ParamObjectName);
			currentSession.removeAttribute(SMVendorReturn.ParamObjectName);
			//But if it's NOT a 'resubmit', meaning this class was called for the first time to 
			//edit, we'll pick up the ID or key from the request and try to load the entry:
		}else if(ServletUtilities.clsManageRequestParameters.get_Request_Parameter(SMVendorReturn.Paramsvendoracct, request).compareToIgnoreCase("") != 0 && ServletUtilities.clsManageRequestParameters.get_Request_Parameter(SMVendorReturn.Paramlid, request).compareToIgnoreCase("-1") == 0 ){
			//IF new entry, and clicked find vendor: don't load
		}else {
			if (!smedit.getAddingNewEntryFlag()){ 
				try {
					entry.load(getServletContext(), smedit.getsDBID(), smedit.getUserID(), smedit.getFullUserName());
				} catch (Exception e) {
					smedit.redirectAction(e.getMessage(), "", SMVendorReturn.Paramlid + "=" + entry.getslid());
					return;
				}
			}
		}

		//If there is a vendor in the request, then that might mean we are coming back from a vendor 'find', and we'll put that in the entry:
		if (ServletUtilities.clsManageRequestParameters.get_Request_Parameter(SMVendorReturn.Paramsvendoracct, request).compareToIgnoreCase("") != 0){
			entry.setsvendoracct(ServletUtilities.clsManageRequestParameters.get_Request_Parameter(SMVendorReturn.Paramsvendoracct, request));
			entry.setsponumber(ServletUtilities.clsManageRequestParameters.get_Request_Parameter(SMVendorReturn.Paramiponumber, request));
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
			smedit.redirectAction(sError, "", SMVendorReturn.Paramlid + "=" + entry.getslid());
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
	private String getEditHTML(SMMasterEditEntry sm, SMVendorReturn entry) throws SQLException{
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
			s += "<TR>\n<TD ALIGN=RIGHT><B>" + SMTablevendorreturns.sidlabel + "</B>:</TD>\n<TD>\n<B>" 
					+ sID 
					+ "<INPUT TYPE=HIDDEN NAME=\"" + SMVendorReturn.Paramlid + "\" VALUE=\"" 
					+ sID + "\">"
					+ "</B></TD>\n</TR>\n";
			//but if it IS a new entry:
		}else{
			s += "<TR>\n<TD ALIGN=RIGHT><B>" + SMTablevendorreturns.sidlabel + "</B>:</TD>\n<TD>\n<B>" 
					+ "(NEW)" 
					+ "<INPUT TYPE=HIDDEN NAME=\"" + SMVendorReturn.Paramlid + "\" VALUE=\"" 
					+ entry.getslid() + "\">"
					+ "</B></TD>\n</TR>\n";
		}

		
		//To Be Returned
		s += "<TR>\n<TD ALIGN=RIGHT VALIGN=TOP><B>" + SMTablevendorreturns.stobereturnedlabel + "? </B></TD>\n";
		s += "<TD>\n";
		String sChecked = "";
		if (entry.getstobereturned().compareToIgnoreCase(Integer.toString(1)) == 0){
			sChecked = " checked ";
		}else{
			sChecked = "";
		}
		s += "<INPUT TYPE='CHECKBOX' NAME='" + SMVendorReturn.Paramitobereturned + "'"+ sChecked + " ><BR>";

		//AP Invoice Was Put on Hold
		s += "<TR>\n<TD ALIGN=RIGHT VALIGN=TOP><B>" + SMTablevendorreturns.sinvoiceonholdlabel + " </B></TD>\n";
		s += "<TD>\n";
		sChecked = "";
		if (entry.getsinvoiceonhold().compareToIgnoreCase(Integer.toString(1)) == 0){
			sChecked = " checked ";
		}else{
			sChecked = "";
		}
		s += "<INPUT TYPE='CHECKBOX' NAME='" + SMVendorReturn.Paramiinvoiceonhold + "'"+ sChecked + " ><BR>";

		//Misc Credit Due
		s += "<TR>\n<TD ALIGN=RIGHT VALIGN=TOP><B>" + SMTablevendorreturns.sCreditDueLabel + " </B></TD>\n";
		s += "<TD>\n";
		sChecked = "";
		if (entry.getiCreditDue().compareToIgnoreCase(Integer.toString(1)) == 0){
			sChecked = " checked ";
		}else{
			sChecked = "";
		}
		s += "<INPUT TYPE='CHECKBOX' NAME='" + SMVendorReturn.Paramicreditdue + "'"+ sChecked + " ><BR>";



		//Vendor
		s += "<TR>\n<TD ALIGN=RIGHT><B>" + SMTablevendorreturns.svendoracctlabel  + ": </B></TD>\n";
		s += "<TD ALIGN=LEFT><INPUT TYPE=TEXT NAME=\"" + SMVendorReturn.Paramsvendoracct + "\""
				+ " VALUE=\"" + entry.getsvendoracct().replace("\"", "&quot;") + "\""
				+ "SIZE=" + "13"
				+ "MAXLENGTH= " + Integer.toString(SMTablevendorreturns.svendoracctlength)
				+ ">"

				//Vendor finder:
				+ "&nbsp;<A HREF=\""
				+ APVendor.getFindVendorLink(
						clsServletUtilities.getFullClassName(this.toString()), 
						SMVendorReturn.Paramsvendoracct, 
						SMVendorReturn.Paramlid + "=" + entry.getslid(),
						getServletContext(),
						sm.getsDBID()
						)
				+ "\"> Find vendor</A>"
				+ "</TD>\n"
				+ "</TR>\n"
				;
		String PO = entry.getsponumber().replace("\"", "&quot;");

		//Purchase order number:
		s += "<TR>\n<TD ALIGN=RIGHT><B>" + SMTablevendorreturns.sponumberlabel + ": </B></TD>\n";
		s += "<TD ALIGN=LEFT><INPUT ONCHANGE=\"POEntry()\" TYPE=TEXT NAME=\"" + SMVendorReturn.Paramiponumber + "\""
				+ " VALUE=\"" + PO + "\""
				+ " SIZE=" + "13"
				+ " MAXLENGTH=10"
				+ ">";
		if(PO.compareToIgnoreCase("")!=0) {
			s+= "&nbsp;<A ID=\"POLink\"  HREF=\"/sm/smic.ICEditPOEdit?lid=" + PO + "&db=" + sm.getsDBID() +"\"> View Purchase Order</A>";
		}
		s+= "";
		s+= "</TD>\n"
				+ "</TR>\n";

		//Comments:
		s += "<TR>\n<TD ALIGN=RIGHT VALIGN=TOP><B>" + SMTablevendorreturns.sVendorCommentsLabel + "</B>:</TD>\n";
		s += "<TD>\n"
				+ "<TEXTAREA NAME=\"" + SMVendorReturn.Parammvendorcomments + "\""
				+ " rows=\"" + "3" + "\""
				//+ " cols=\"" + Integer.toString(iCols) + "\""
				+ " style = \" width: 100%; \""
				+ ">"
				+ entry.getsVendorComments().replace("\"", "&quot;")
				+ "</TEXTAREA>"
				+ "</TD>\n"
				+ "</TR>\n"
				;
		

		
		//'Returned' section:
		s += "<TR class = \" " + SMMasterStyleSheetDefinitions.TABLE_HEADING + " \" ><TD ALIGN=LEFT COLSPAN=2><B>ACCOUNTING</B>:</TD>\n</TR>\n";

		String sBatchNumber = entry.getladjustedbatchnumber().replace("\"", "&quot;");
		String sEntryNumber = entry.getladjustedentrynumber().replace("\"", "&quot;");

		//Date Returned
		if(entry.getdatreturnsent().replace("\"", "&quot;").compareToIgnoreCase(clsServletUtilities.EMPTY_SQL_DATE_VALUE)==0) {
			s += "<TR><TD ALIGN=RIGHT><B>" + SMTablevendorreturns.sreturnsentlabel + ":<B></TD>"
					+ "<TD ALIGN=LEFT><INPUT TYPE=TEXT NAME=\"" + SMVendorReturn.Paramdatreturnsent + "\""
					+ " ID =\"" + SMVendorReturn.Paramdatreturnsent + "\""
					+ " VALUE=\"" + clsDateAndTimeConversions.resultsetDateStringToString(entry.getdatreturnsent().replace("\"", "&quot;")) + "\""
					+ " SIZE=" + "13"
					+ ">" + SMUtilities.getDatePickerString(SMVendorReturn.Paramdatreturnsent, getServletContext()) + "</TD>"
					+ "</TR>"
					;
		}else {
			s += "<TR><TD ALIGN=RIGHT><B>" + SMTablevendorreturns.sreturnsentlabel + ":<B></TD>"
					+ "<TD ALIGN=LEFT><INPUT TYPE=TEXT NAME=\"" + SMVendorReturn.Paramdatreturnsent + "\""
					+ " ID =\"" + SMVendorReturn.Paramdatreturnsent + "\""
					+ " VALUE=\"" + entry.getdatreturnsent().replace("\"", "&quot;") + "\""
					+ " SIZE=" + "13"
					+ ">" + SMUtilities.getDatePickerString(SMVendorReturn.Paramdatreturnsent, getServletContext()) + "</TD>"
					+ "</TR>"
					;
		}
		
		//Adjustment Batch Number
		s += "<TR><TD ALIGN=RIGHT><B>" + SMTablevendorreturns.sadjustedbatchnumberlabel  + ": </B></TD>";
		s += "<TD ALIGN=LEFT><INPUT ONCHANGE=\"BatchEntry()\" TYPE=TEXT NAME=\"" + SMVendorReturn.Paramladjustedbatchnumber + "\""
				+ " VALUE=\"" + sBatchNumber + "\""
				+ " ID =\"" + SMVendorReturn.Paramladjustedbatchnumber + "\""
				+ " SIZE=" + "13"
				+ " MAXLENGTH=" + Integer.toString(SMTablevendorreturns.ladjustedbatchnumberlength)
				+ "></TD>"
				+ "</TR>"
				;

		//Adjustment Entry Number
		s += "<TR><TD ALIGN=RIGHT><B>" + SMTablevendorreturns.sadjustedentrynumberlabel  + ": </B></TD>";
		s += "<TD ALIGN=LEFT><INPUT ONCHANGE=\"BatchEntry()\" TYPE=TEXT NAME=\"" + SMVendorReturn.Paramladjustedentrynumber + "\""
				+ " VALUE=\"" + sEntryNumber + "\""
				+ " ID =\"" + SMVendorReturn.Paramladjustedentrynumber + "\""
				+ " SIZE=" + "13"
				+ " MAXLENGTH=" + Integer.toString(SMTablevendorreturns.lentrynumberlength) + ">";
		sBatchNumber = clsStringFunctions.checkStringForNull(sBatchNumber);
		sEntryNumber = clsStringFunctions.checkStringForNull(sEntryNumber);
		if((sBatchNumber.compareToIgnoreCase("")!=0) && (sEntryNumber.compareToIgnoreCase("")!=0) && (sBatchNumber.compareToIgnoreCase("0")!=0) && (sEntryNumber.compareToIgnoreCase("0")!=0)) {
			s+= "&nbsp;<A ID=\"BatchEntryLink\"  HREF=\"/sm/smic.ICEditReceiptEntry?BatchNumber=" + sBatchNumber + "&EntryNumber=" + sEntryNumber + "&db=" + sm.getsDBID() +"\">View Adjustment Entry	</A>";
		}
		s += "</TD>"
				+ "</TR>";

		//Adjusted Credit Amount
		s += "<TR><TD ALIGN=RIGHT><B>" + SMTablevendorreturns.sadjustmentamountlabel  + ": </B></TD>";
		s += "<TD ALIGN=LEFT><INPUT TYPE=TEXT NAME=\"" + SMVendorReturn.Parambdadjustmentamount + "\""
				+ " VALUE=\"" + entry.getbdadjustmentamount().replace("\"", "&quot;") + "\""
				+ " ID =\"" + SMVendorReturn.Parambdadjustmentamount + "\""
				+ " SIZE=" + "13"
				+ " MAXLENGTH=" + Integer.toString(SMTablevendorreturns.bdadjustmentamountlength)
				+ "></TD>"
				+ "</TR>"
				;

		//Credit Memo Number
		s += "<TR><TD ALIGN=RIGHT><B>" + SMTablevendorreturns.screditmemonumberlabel  + ": </B></TD>";
		s += "<TD ALIGN=LEFT><INPUT TYPE=TEXT NAME=\"" + SMVendorReturn.Paramscreditmemonumber + "\""
				+ " VALUE=\"" + entry.getscreditmemonumber().replace("\"", "&quot;") + "\""
				+ " ID =\"" + SMVendorReturn.Paramscreditmemonumber + "\""
				+ " SIZE=" + "13"
				+ " MAXLENGTH=" + Integer.toString(SMTablevendorreturns.screditmemonumberlength)
				+ "></TD>"
				+ "</TR>"
				;

		//Date of Credit Memo:
		if(entry.getdatcreditnotedate().replace("\"", "&quot;").compareToIgnoreCase(clsServletUtilities.EMPTY_SQL_DATE_VALUE)==0) {
			s += "<TR><TD ALIGN=RIGHT><B>" + SMTablevendorreturns.screditnotedatelabel + ":<B></TD>"
					+ "<TD ALIGN=LEFT><INPUT TYPE=TEXT NAME=\"" + SMVendorReturn.Paramdatcreditnotedate + "\""
					+ " ID =\"" + SMVendorReturn.Paramdatcreditnotedate + "\""
					+ " VALUE=\"" + clsDateAndTimeConversions.resultsetDateStringToString(entry.getdatcreditnotedate().replace("\"", "&quot;")) + "\""
					+ " SIZE=" + "13"
					+ ">" + SMUtilities.getDatePickerString(SMVendorReturn.Paramdatcreditnotedate, getServletContext()) + "</TD>"
					+ "</TR>"
					;
		}else {
			s += "<TR><TD ALIGN=RIGHT><B>Date of Credit Memo:<B></TD>"
					+ "<TD ALIGN=LEFT><INPUT TYPE=TEXT NAME=\"" + SMVendorReturn.Paramdatcreditnotedate + "\""
					+ " ID =\"" + SMVendorReturn.Paramdatcreditnotedate + "\""
					+ " VALUE=\"" + entry.getdatcreditnotedate().replace("\"", "&quot;") + "\""
					+ " SIZE=" + "13"
					+ ">" + SMUtilities.getDatePickerString(SMVendorReturn.Paramdatcreditnotedate, getServletContext()) + "</TD>"
					+ "</TR>"
					;
		}

		//Credit received
		s += "<TR><TD ALIGN=RIGHT><B>" +SMTablevendorreturns.screditamtlabel +": </B></TD>";
		s += "<TD ALIGN=LEFT><INPUT TYPE=TEXT NAME=\"" + SMVendorReturn.Parambdcreditamt + "\""
				+ " VALUE=\"" + entry.getbdcreditamt().replace("\"", "&quot;") + "\""
				+ " ID =\"" + SMVendorReturn.Parambdcreditamt + "\""
				+ " SIZE=" + "13"
				+ " MAXLENGTH=" + Integer.toString(SMTablevendorreturns.bdcreditamtlength)
				+ "></TD>"
				+ "</TR>"
				;

		//Credit Not Expected
				//Credit to be expected will have a Expected Credit Amount
				//Credit received will have Credit Memo Number and Date of Credit Memo
				s += "<TR>\n<TD ALIGN=RIGHT VALIGN=TOP><B>" + SMTablevendorreturns.screditnotexpectedlabel + "</B></TD>\n";
				s += "<TD>\n";
				sChecked = "";
				if (entry.getscreditnotexpected().compareToIgnoreCase(Integer.toString(1)) == 0){
					sChecked = " checked ";
				}else{
					sChecked = "";
				}
				s += "<INPUT TYPE='CHECKBOX' NAME='" + SMVendorReturn.Paramicreditnotexpected + "' VALUE= "+ SMTablevendorreturns.STATUS_CREDITNOTEXPECTED + sChecked + " ><BR>";

		
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
