package smap;

import java.io.IOException;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import smcontrolpanel.SMMasterEditEntry;
import smcontrolpanel.SMSystemFunctions;
import smcontrolpanel.SMUtilities;
import SMDataDefinition.SMTableicvendorterms;


public class APEditVendorTermsEdit  extends HttpServlet {

	private static final long serialVersionUID = 1L;
	//private boolean bDebugMode = false;

	public void doPost(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
		
		//PrintWriter debug_out = new PrintWriter(System.out);
		//SMUtilities.printRequestParameters(debug_out, request);
		
		APVendorTerms entry = new APVendorTerms(request);
		SMMasterEditEntry smedit = new SMMasterEditEntry(
				request,
				response,
				getServletContext(),
				entry.getObjectName(),
				SMUtilities.getFullClassName(this.toString()),
				"smap.APEditVendorTermsAction",
				"smcontrolpanel.SMUserLogin",
				"Go back to user login",
				SMSystemFunctions.APEditVendorTerms
				);
		
		if (!smedit.processSession(getServletContext(), SMSystemFunctions.APEditVendorTerms)){
			smedit.getPWOut().println("Error in process session: " + smedit.getErrorMessages());
			return;
		}
		
		//If this is a 'resubmit', meaning it's being called by the action class, then
		//the session will have a job cost entry object in it, and that's what we'll pick up.
		HttpSession currentSession = smedit.getCurrentSession();
		
		//Record this URL so we can return to it later:
		if (entry.getsTermsCode().compareToIgnoreCase("") != 0){
			smedit.addToURLHistory("Editing vendor terms code " + entry.getsTermsCode());
		}else{
			smedit.addToURLHistory("Adding a new vendor terms code");
		}
		
	    if (currentSession.getAttribute(APVendorTerms.ParamObjectName) != null){
	    	entry = (APVendorTerms) currentSession.getAttribute(APVendorTerms.ParamObjectName);
	    	currentSession.removeAttribute(APVendorTerms.ParamObjectName);

	    //But if it's NOT a 'resubmit', meaning this class was called for the first time to 
	    //edit, we'll pick up the ID or key from the request and try to load the entry:
	    }else{
	    	if (!smedit.getAddingNewEntryFlag()){
		    	if(!entry.load(getServletContext(), 
		    			       smedit.getsDBID(), 
		    				   smedit.getUserID(),
		    				   smedit.getFullUserName()
		    			)){
					response.sendRedirect(
						"" + SMUtilities.getURLLinkBase(getServletContext()) + "" + smedit.getCallingClass()
						+ "?" + APVendorTerms.ParamsTermsCode + "=" + entry.getsTermsCode()
						+ "&Warning=" + entry.getErrorMessages()
						+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + smedit.getsDBID()
					);
					return;
		    	}
	    	}
	    }

	    smedit.printHeaderTable();
	    
		smedit.getPWOut().println(
				"<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smap.APMainMenu?" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" 
					+ smedit.getsDBID() + "\">Return to Accounts Payable Main Menu</A>");
	    
		smedit.getPWOut().println("&nbsp;&nbsp;&nbsp;<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMURLHistoryList?" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" 
				+ smedit.getsDBID() + "\">Return to...</A>");
		
	    try {
			smedit.createEditPage(getEditHTML(smedit, entry), "");
		} catch (SQLException e) {
    		String sError = "Could not create edit page - " + e.getMessage();
			response.sendRedirect(
				"" + SMUtilities.getURLLinkBase(getServletContext()) + "" + smedit.getCallingClass()
				+ "?" + APVendorTerms.ParamsTermsCode + "=" + entry.getsTermsCode()
				+ "&Warning=Could not load Terms Code #: " + entry.getsTermsCode() + " - " + sError
				+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + smedit.getsDBID()
			);
			return;
		}
		
	    return;
	}
	private String getEditHTML(SMMasterEditEntry sm, APVendorTerms entry) throws SQLException{

		
		String s = "<TABLE style=\" border-style:solid; border-color:black; font-size:small; \">";

		s += "<TR><TD>"
			+ "<B>Last maintained:"
			+ "</TD>"
			+ "<TD>"
			+ entry.getsLastMaintainedDate()
			+ "<INPUT TYPE=HIDDEN NAME=\"" + APVendorTerms.ParamdatLastMaintained + "\" VALUE=\"" 
			+ entry.getsLastMaintainedDate() + "\">"
			+ "</TD>"
			+ "</TR>"
		;
		
		//Terms
		s += "<TR><TD style=\" text-align:right; font-weight:bold; \">Terms code:</TD>";
		if (sm.getAddingNewEntryFlag()){
			s += "<TD><INPUT TYPE=TEXT NAME=\"" + APVendorTerms.ParamsTermsCode + "\""
			+ " VALUE=\"" + entry.getsTermsCode() + "\""
			+ " SIZE=28"
			+ " MAXLENGTH=" + SMTableicvendorterms.sTermsCodeLength
			+ " STYLE=\"width: " + ".75" + " in; height: 0.25in\""
			+ ">"
			+ "</TD>"
			;
		}else{
			s += "<TD>" + entry.getsTermsCode()
				+ "<INPUT TYPE=HIDDEN NAME=\"" + APVendorTerms.ParamsTermsCode + "\" VALUE=\"" 
				+ entry.getsTermsCode() + "\">"
				+ "</TD>"
			;
		}
		s += "</TR>";

		//Description
		s += "<TR><TD  style=\" text-align:right; font-weight:bold; \">Description:</TD>"
			+ "<TD><INPUT TYPE=TEXT NAME=\"" + APVendorTerms.ParamsDescription + "\""
			+ " VALUE=\"" + entry.getsTermsDescription() + "\""
			+ " SIZE=40"
			+ " MAXLENGTH=" + SMTableicvendorterms.sDescriptionLength
			+ " STYLE=\"width: " + "1.5" + " in; height: 0.25in\""
			+ ">"
			+ "</TD>"
			+ "</TR>"
			;

		//Active?
		s += "<TR><TD  style=\" text-align:right; font-weight:bold; \">Active?:</TD>"
			+ "<TD>"
			+ "<INPUT TYPE=CHECKBOX";
		  
		if (entry.getsActive().compareToIgnoreCase("1") == 0){
			s += " checked=\"yes\" ";
		}

		s += " NAME=\"" 
			+ APVendorTerms.ParamiActive 
			+ "\" width=0.25>"
			+ "</TD>"
			+ "</TR>"
		;
			
		//Discount percent
		s += "<TR><TD  style=\" text-align:right; font-weight:bold; \">Discount percentage:</TD>"
			+ "<TD><INPUT TYPE=TEXT NAME=\"" + APVendorTerms.ParambdDiscountPercent + "\""
			+ " VALUE=\"" + entry.getsDiscountPercentage() + "\""
			+ " SIZE=20"
			+ " MAXLENGTH=" + "7"
			+ " STYLE=\"width: " + ".5" + " in; height: 0.25in\""
			+ ">"
			+ "</TD>"
			+ "</TR>"
			;		
		
		//Discount number of days
		s += "<TR><TD  style=\" text-align:right; font-weight:bold; \">Discount number of days<SUP><a href=\"#discountnumberofdays\">1</a></SUP>:</TD>"
			+ "<TD><INPUT TYPE=TEXT NAME=\"" + APVendorTerms.ParamiDiscountNumberOfDays + "\""
			+ " VALUE=\"" + entry.getsDiscountNumberOfDays() + "\""
			+ " SIZE=20"
			+ " MAXLENGTH=" + "3"
			+ " STYLE=\"width: " + ".5" + " in; height: 0.25in\""
			+ ">"
			+ "</TD>"
			+ "</TR>"
			;		
		
		//Discount day of the month
		s += "<TR><TD  style=\" text-align:right; font-weight:bold; \">Discount day of the month<SUP><a href=\"#discountdayofthemonth\">2</a></SUP>:</TD>"
			+ "<TD><INPUT TYPE=TEXT NAME=\"" + APVendorTerms.ParamiDiscountDayOfTheMonth + "\""
			+ " VALUE=\"" + entry.getsDiscountDayOfTheMonth() + "\""
			+ " SIZE=20"
			+ " MAXLENGTH=" + "2"
			+ " STYLE=\"width: " + ".5" + " in; height: 0.25in\""
			+ ">"
			+ "</TD>"
			+ "</TR>"
			;		
		
		//Minimum Days Allowed For Discount Day Of The Month:
		s += "<TR><TD  style=\" text-align:right; font-weight:bold; \">Minimum days allowed for discount day of the month<SUP><a href=\"#minimumdaysallowedfordiscount\">3</a></SUP>:</TD>"
			+ "<TD><INPUT TYPE=TEXT NAME=\"" + APVendorTerms.ParamiMinimumDaysAllowedForDiscountDueDayOfMonth + "\""
			+ " VALUE=\"" + entry.getsMinimumDaysAllowedForDiscountDueDay() + "\""
			+ " SIZE=20"
			+ " MAXLENGTH=" + "2"
			+ " STYLE=\"width: " + ".5" + " in; height: 0.25in\""
			+ ">"
			+ "</TD>"
			+ "</TR>"
			;	
		
		//Due number of days
		s += "<TR><TD  style=\" text-align:right; font-weight:bold; \">Due number of days<SUP><a href=\"#duenumberofdays\">4</a></SUP>:</TD>"
			+ "<TD><INPUT TYPE=TEXT NAME=\"" + APVendorTerms.ParamiDueNumberOfDays + "\""
			+ " VALUE=\"" + entry.getsDueNumberOfDays() + "\""
			+ " SIZE=20"
			+ " MAXLENGTH=" + "3"
			+ " STYLE=\"width: " + ".5" + " in; height: 0.25in\""
			+ ">"
			+ "</TD>"
			+ "</TR>"
			;		

		//Due day of the month
		s += "<TR><TD  style=\" text-align:right; font-weight:bold; \">Due day of the month<SUP><a href=\"#duedayofthemonth\">5</a></SUP>:</TD>"
			+ "<TD><INPUT TYPE=TEXT NAME=\"" + APVendorTerms.ParamiDueDayOfTheMonth + "\""
			+ " VALUE=\"" + entry.getsDueDayOfTheMonth() + "\""
			+ " SIZE=20"
			+ " MAXLENGTH=" + "2"
			+ " STYLE=\"width: " + ".5" + " in; height: 0.25in\""
			+ ">"
			+ "</TD>"
			+ "</TR>"
			;		

		//Minimum Days Allowed For Day Of The Month:
		s += "<TR><TD  style=\" text-align:right; font-weight:bold; \">Minimum days allowed for due day of the month<SUP><a href=\"#minimumdaysallowedforduedate\">6</a></SUP>:</TD>"
			+ "<TD><INPUT TYPE=TEXT NAME=\"" + APVendorTerms.ParamiMinimumDaysAllowedForDueDayOfMonth + "\""
			+ " VALUE=\"" + entry.getsMinimumDaysAllowedForDueDayOfMonth() + "\""
			+ " SIZE=20"
			+ " MAXLENGTH=" + "2"
			+ " STYLE=\"width: " + ".5" + " in; height: 0.25in\""
			+ ">"
			+ "</TD>"
			+ "</TR>"
			;
		
		s += "</TABLE>";
		
		//Footnotes:
		s += createFootNotes();
		
		return s;
	}
	
	private String createFootNotes(){
		String s = "<BR>";
		
		s += "<a name=\"discountnumberofdays\"><SUP>1</SUP></a><B>Discount number of days</B>: "
				+ "The 'discount number of days' is the maximum number of days AFTER the invoice date for which the invoice is still eligible"
				+ " to be discounted.  So if an invoice date is the 10th, and the 'Discount number of days' is 15, then the vendor will honor"
				+ " the discount if the invoice is paid by the 25th, which is 15 days after the invoice date."
				+ "<BR><BR>"
				;

		s += "<a name=\"discountdayofthemonth\"><SUP>2</SUP></a><B>Discount day of the month</B>: "
				+ "The 'discount day of the month' is the CALENDAR DAY of the month up to which the vendor will honor the discount."
				+ "  So if the invoice date is the 10th, and the 'discount day of the month is the 25th, the invoice is eligible to be"
				+ " discounted if it's paid by the 25th.  IF the invoice date is LATER in the month than the 'discount day of the month'"
				+ " then the system will assume that the invoice is eligible for the discount up to the 'discount day' of the FOLLOWING month."
				+ "<BR><BR>"
				;

		s += "<a name=\"minimumdaysallowedfordiscount\"><SUP>3</SUP></a><B>Minimum days allowed for discount day of the month</B>: "
				+ "The 'minimum days allowed for discount day of the month' is the length, in days, of the 'grace period' that the vendor allows between"
				+ " the invoice date and the 'discount day of the month'.  If the discount day of the month follows too closely on the invoice date,"
				+ " then the discount day of the month will be moved to the following month.  So, for example, if the invoice date is the 10th,"
				+ " and the 'discount day of the month' is the 15th, that would normally mean that to get the discount, the invoice would have to be"
				+ " paid 5 days after the invoice date.  But the if the 'minimum days allowed for discount day of the month' is set to 8, for example,"
				+ " then the vendor will allow any 'discount day of the month' within 8 days of the invoice date to be shifted into the following month."
				+ " In this example, the discount day of the month would be moved to the 15th of the FOLLOWING month."
				+ "<BR><BR>"
				;
		
		s += "<a name=\"duenumberofdays\"><SUP>4</SUP></a><B>Due number of days</B>: "
				+ "The 'due number of days' is ADDED to the invoice date to calculate the DUE date of the invoice.  For example, if the invoice date is the 10th,"
				+ " and the 'due number of days' is 15, then the 'due date' for that invoice is the 25th."
				+ "<BR><BR>"
				;

		s += "<a name=\"duedayofthemonth\"><SUP>5</SUP></a><B>Due day of the month</B>: "
				+ "The 'due day of the month' is the CALENDAR DAY of the month on which the invoice will be due."
				+ "  So if the invoice date is the 10th, and the 'due day of the month is the 25th, the invoice is then due on the 25th."
				+ "  If the due day of the month is EARLIER than the invoice date, then the due date is shifted to the 'due day of the month"
				+ " of the FOLLOWING month."
				+ "<BR><BR>"
				;

		s += "<a name=\"minimumdaysallowedforduedate\"><SUP>5</SUP></a><B>Minimum days allowed for due day of the month</B>: "
				+ "The 'Minimum days allowed for due day of the month' is the 'grace period' in days that the vendor will allow"
				+ " before the the 'due day of the month' will be shifted into the following month.  If the 'due day of the month'"
				+ " places the due date too close to the invoice date, then, based on the 'minimum days allowed for due date'"
				+ ", the due date will be shifted.  So if the invoice date is the 10th, and the 'due day of the month is the 15th, the invoice"
				+ " would normally be due on the 15th of the same month.  But if the 'minimum days allowed for due date' is, say, 8,"
				+ " then because the 'due date of the month' is less than 8 days after the invoice date, the due date gets shifted"
				+ " to the following month."
				+ "<BR><BR>"
				;

		s += "<BR><B><I>NOTE:</I></B> You cannot use both a 'Discount day of the month' AND a 'Due day of the month' - at least one has to be a 'number of days'"
				+ " rather than a 'day of the month'."
				;
		
		return s;
	}
	public void doGet(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
		
		doPost(request, response);
	}
}
