package smar;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import SMClasses.MySQLs;
import smcontrolpanel.SMMasterEditEntry;
import smcontrolpanel.SMMasterEditSelect;
import smcontrolpanel.SMSystemFunctions;
import smcontrolpanel.SMUtilities;
import SMDataDefinition.SMTablecallsheets;
import SMDataDefinition.SMTablesalesperson;
import ServletUtilities.clsDatabaseFunctions;
import ServletUtilities.clsManageRequestParameters;

public class AREditCallSheetsEdit  extends HttpServlet {

	private static final long serialVersionUID = 1L;

	public static final String CALL_SHEET_OBJECT = "CallSheet";
	public static final String FIND_CUSTOMER_BUTTON = "FINDCUSTOMER";
	public static final String FIND_CUSTOMER_LABEL = "Find customer";
	public static final String CUSTOMERFINDERRETURN_FIELD = "CUSTOMERFINDERRETURN";
	public static final String UPDATEFROMORDER_BUTTON = "UPDATEFROMORDER";
	public static final String UPDATEFROMORDER_LABEL = "Update from order";
	
	
	public void doPost(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
		
		//PrintWriter debug_out = new PrintWriter(System.out);
		//SMUtilities.printRequestParameters(debug_out, request);
		
		ARCallSheet entry = new ARCallSheet(request);
		SMMasterEditEntry smedit = new SMMasterEditEntry(
				request,
				response,
				getServletContext(),
				entry.getObjectName(),
				SMUtilities.getFullClassName(this.toString()),
				"smar.AREditCallSheetsAction",
				"smcontrolpanel.SMUserLogin",
				"Go back to user login",
				SMSystemFunctions.AREditCallSheets
				);
		
		if (!smedit.processSession(getServletContext(), SMSystemFunctions.AREditCallSheets)){
			smedit.getPWOut().println("Error in process session: " + smedit.getErrorMessages());
			return;
		}
		
		if (smedit.getAddingNewEntryFlag()){
			entry.setM_siID("-1");
		}
		
		//If this is a 'resubmit', meaning it's being called by the 'Action' class, then
		//the session will have a call sheet object in it, and that's what we'll pick up.
		HttpSession currentSession = smedit.getCurrentSession();
		
	    if (currentSession.getAttribute(CALL_SHEET_OBJECT) != null){
	    	entry = (ARCallSheet) currentSession.getAttribute(CALL_SHEET_OBJECT);
	    	currentSession.removeAttribute(CALL_SHEET_OBJECT);
	    	//IF we are returning from a finder, get the customer number:
	    	if (clsManageRequestParameters.get_Request_Parameter(
	    		CUSTOMERFINDERRETURN_FIELD, request).compareToIgnoreCase("") != 0){
	    		entry.setM_sAcct(clsManageRequestParameters.get_Request_Parameter(CUSTOMERFINDERRETURN_FIELD, request));
	    	}
	    //But if it's NOT a 'resubmit', meaning this class was called for the first time to 
	    //edit, we'll pick up the ID or key from the request and try to load the entry:
	    }else{
	    	if (!smedit.getAddingNewEntryFlag()){
		    	if(!entry.load(getServletContext(), smedit.getsDBID(), smedit.getUserID(), smedit.getFullUserName())){
					response.sendRedirect(
						"" + SMUtilities.getURLLinkBase(getServletContext()) + "" + smedit.getCallingClass()
						+ "?" + ARCallSheet.ParamsID + "=" + entry.getM_siID()
						+ "&Warning=" + entry.getErrorMessages()
						+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + smedit.getsDBID()
					);
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
	    smedit.getPWOut().println(
	    	"<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) 
	    	+ "smar.ARMainMenu?" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + smedit.getsDBID() 
	    	+ "\">Return to Accounts Receivable Main Menu</A><BR>");
	    
		if (entry.getM_siID().compareToIgnoreCase("-1") != 0){
			//Also, if it's not a NEW call sheet, add a link to 'Add a new call sheet':
			currentSession.removeAttribute(CALL_SHEET_OBJECT);
			smedit.getPWOut().println("<A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) 
		    		+ SMUtilities.getFullClassName(this.toString())
		    		+ "?"
		    		+ SMMasterEditSelect.SUBMIT_ADD_BUTTON_NAME + "=Y"
		    		+ "&CallingClass=" + SMUtilities.getFullClassName(this.toString())
		    		+ "&OriginalCallingClass=" + smedit.getCallingClass()
		    		+ "&" + ARCallSheet.ParamsID + "=-1"
		    		+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + smedit.getsDBID() 
		    		+ "\">Add a new call sheet</A>"
		    		);
		}
		smedit.getPWOut().println("<BR>");
		
	    try {
			smedit.createEditPage(getEditHTML(smedit, entry), "");
		} catch (SQLException e) {
    		String sError = "Could not create edit page - " + e.getMessage();
			response.sendRedirect(
				"" + SMUtilities.getURLLinkBase(getServletContext()) + "" + smedit.getCallingClass()
				+ "?" + ARCallSheet.ParamsID + "=" + entry.getM_siID()
				+ "&Warning=Could not load entry ID: " + entry.getM_siID() + sError
				+ "&" + SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + smedit.getsDBID()
			);
				return;
		}
	    return;
	}
	private String getEditHTML(SMMasterEditEntry sm, ARCallSheet entry) throws SQLException{

		String s = "";
		
		s += "<TABLE BORDER=0 style = \" font-size:small;\" >";
		
		String sID = "";
		if (entry.getM_siID().compareToIgnoreCase("-1") == 0){
			sID = "NEW";
		}else{
			sID = entry.getM_siID();
		}
		s += "<TR><TD ALIGN=LEFT><B>Call Sheet</B>:&nbsp;"
			+ "<B>" + sID + "</B>" 
			+ "<INPUT TYPE=HIDDEN NAME=\"" + ARCallSheet.ParamsID + "\" VALUE=\"" 
				+ entry.getM_siID() + "\">"
			+ "</TD>"
			;

		s += "<TD><B>&nbsp;&nbsp;&nbsp;&nbsp;Set alert</B>:&nbsp;"
			+ "<INPUT TYPE=CHECKBOX";
		//TJR - 4/15/2011 - this is supposed to be the CORRECT way to check a checkbox,
		//according to the HTML standard (http://www.w3.org/TR/html401/interact/forms.html#checkbox)
		if (entry.getbAlertInits()){
			s += " checked=\"yes\" ";
		}
		s += " NAME=\"" + ARCallSheet.ParamsAlertInits + "\" width=0.25></TD>";
		
		//Put order number and button here:
		//Order number
		s += "<TD ALIGN=RIGHT><B>Order number:</B></TD>"
			+ "<TD><INPUT TYPE=TEXT NAME=\"" + ARCallSheet.ParamsOrderNumber + "\""
			+ " VALUE=\"" + entry.getM_sOrderNumber() + "\""
			+ " SIZE=" + "10"
			+ " MAXLENGTH=" + Integer.toString(SMTablecallsheets.sOrderNumberLength) + ">"
			//'Update' button:
			+ "<INPUT TYPE=" + "\"SUBMIT\"" 
					+ " NAME=\"" + UPDATEFROMORDER_BUTTON + "\""
					+ " VALUE=\"" + UPDATEFROMORDER_LABEL + "\">"
			+ "</TD>"
		;
		
		s += "</TR>";
		
		
		s += "<TR>";
		//Customer:
		s += "<TD ALIGN=LEFT><B>Customer:</B></TD>" 
			+ "<TD><INPUT TYPE=TEXT NAME=\"" + ARCallSheet.ParamsAcct + "\""
			+ " VALUE=\"" + entry.getM_sAcct() + "\""
			+ " SIZE=" + "10"
			+ " MAXLENGTH=" + Integer.toString(SMTablecallsheets.sAcctLength)
			+ "\">"
			//Finder:
			+ "<INPUT TYPE=" + "\"SUBMIT\"" 
					+ " NAME=\"" + FIND_CUSTOMER_BUTTON + "\""
					+ " VALUE=\"" + FIND_CUSTOMER_LABEL + "\">"
			+ "</TD>"
			;
		
		//Terms:
		s += "<TD ALIGN=LEFT><B>Terms:</B></TD>"
			+ "<TD><INPUT TYPE=TEXT NAME=\"" + ARCallSheet.ParamsAccountTerms + "\""
			+ " VALUE=\"" + entry.getM_sAccountTerms() + "\""
			+ " SIZE=" + "10"
			+ " MAXLENGTH=" + Integer.toString(SMTablecallsheets.sAccountTermsLength)
			+ "</TD>"
			+ "</TR>"
		;

		//Customer name:
		s += "<TR><TD ALIGN=LEFT><B>Customer name:</B></TD>"
			+ "<TD><INPUT TYPE=TEXT NAME=\"" + ARCallSheet.ParamsCustomerName + "\""
			+ " VALUE=\"" + entry.getM_sCustomerName() + "\""
			+ " SIZE=" + "40"
			+ " MAXLENGTH=" + Integer.toString(SMTablecallsheets.sCustomerNameLength)
			+ "</TD>"
		;

		//Collector
		s += "<TD ALIGN=RIGHT><B>Collector:</B></TD>"
			+ "<TD><INPUT TYPE=TEXT NAME=\"" + ARCallSheet.ParamsCollector + "\""
			+ " VALUE=\"" + entry.getsCollector() + "\""
			+ " SIZE=" + "10"
			+ " MAXLENGTH=" + Integer.toString(SMTablecallsheets.sCollectorLength)
			+ "</TD></TR>"
		;
		
		//Call sheet name
		s += "<TR><TD ALIGN=LEFT><B>Call sheet name:</B></TD>"
			+ "<TD><INPUT TYPE=TEXT NAME=\"" + ARCallSheet.ParamsCallSheetName + "\""
			+ " VALUE=\"" + entry.getM_sCallSheetName() + "\""
			+ " SIZE=" + "40"
			+ " MAXLENGTH=" + Integer.toString(SMTablecallsheets.sCallSheetNameLength)
			+ "</TD>"
		;
		
	    ArrayList<String> sValues = new ArrayList<String>();
	    ArrayList<String> sDescriptions = new ArrayList<String>();
    	//Select list:
        //First, add a blank to make sure the user selects one:
        sValues.add("");
        sDescriptions.add("-- Set Responsibility --");
	    try{
	        ResultSet rsSalespersons = clsDatabaseFunctions.openResultSet(
	        		MySQLs.Get_Salesperson_List_SQL(),
	        		getServletContext(),
	        		sm.getsDBID(),
	        		"MySQL",
	        		this.toString() + " reading salespersons - user: " + sm.getUserID()
	        		+ " - "
	        		+ sm.getFullUserName()
	        		);
	        while (rsSalespersons.next()){
	        	sValues.add((String) rsSalespersons.getString(SMTablesalesperson.sSalespersonCode).trim());
	        	sDescriptions.add(
	        			(String) (rsSalespersons.getString(SMTablesalesperson.sSalespersonCode).trim() 
	        			+ " - " + rsSalespersons.getString(SMTablesalesperson.sSalespersonFirstName).trim()
	        			+ " " + rsSalespersons.getString(SMTablesalesperson.sSalespersonLastName).trim())
	        	);
	        }
	        rsSalespersons.close();
	    }catch(SQLException e){
	    	throw e;
	    }
	    
		s += "<TD ALIGN=LEFT><B>" + "Responsibility:" + "</B></TD>"
			+ "<TD ALIGN=LEFT> <SELECT NAME = \"" + ARCallSheet.ParamsResponsibility + "\">"
		;
		for (int i = 0; i < sValues.size(); i++){
			s += "<OPTION";
			if (sValues.get(i).toString().compareToIgnoreCase(entry.getM_sResponsibility()) == 0){
				s += " selected=yes";
			}
			s += " VALUE=\"" + sValues.get(i).toString() + "\">" + sDescriptions.get(i).toString();
		}
		s += "</SELECT></TD>";
		s += "</TABLE>";
		
		s += "<TABLE BORDER=0 style = \" font-size:small;\" >";

		//Phone
		s += "<TD ALIGN=RIGHT><B>Phone:</B></TD>"
			+ "<TD><INPUT TYPE=TEXT NAME=\"" + ARCallSheet.ParamsPhone + "\""
			+ " VALUE=\"" + entry.getsPhone() + "\""
			+ " SIZE=" + "10"
			+ " MAXLENGTH=" + Integer.toString(SMTablecallsheets.sPhoneLength)
			+ "</TD>"
		;
		
		//Job Phone
		s += "<TD ALIGN=RIGHT><B>Job phone:</B></TD>"
			+ "<TD><INPUT TYPE=TEXT NAME=\"" + ARCallSheet.ParamsJobPhone + "\""
			+ " VALUE=\"" + entry.getM_sJobPhone() + "\""
			+ " SIZE=" + "10"
			+ " MAXLENGTH=" + Integer.toString(SMTablecallsheets.sJobPhoneLength)
			+ "</TD>"
		;
		//Last contact
		s += "<TD ALIGN=RIGHT><B>Last contact:</B></TD>"
			+ "<TD>" 
			+ createDateField(ARCallSheet.ParamdatLastContact, entry.getM_LastContactDate()) 
			+ "</TD>"
		;
		
		//Next contact
		s += "<TD ALIGN=RIGHT><B>Next contact:</B></TD>"
			+ "<TD>" 
			+ createDateField(ARCallSheet.ParamdatNextContact, entry.getM_NextContactDate()) 
			+ "</TD>"
		;

		s += "</TR>";
		s += "</TABLE>";

		s += "<scan style = \" font-size:small; \"><U><B>Notes</B></U><BR>";
		String sNotes = "";
		//System.out.println("In " + this.toString() + ". entry.getM_notes() = " + entry.getM_notes());
		sNotes = entry.getM_notes();
		//System.out.println("In " + this.toString() + ". sNotes (2) = " + sNotes);
		s += "<TEXTAREA NAME=\"" + ARCallSheet.ParammNotes + "\""
			+ " rows=\"" + "15" + "\""
			+ " cols=\"" + "110"+ "\""
			+ ">" + sNotes + "</TEXTAREA>";
		s += "</scan>";
		return s;
	}
	private String createDateField(String sParameter, String sValue){
		return 	"<INPUT TYPE=TEXT NAME=\"" + sParameter + "\""
		+ " VALUE=\"" + sValue + "\""
		+ " SIZE=28"
		+ " MAXLENGTH=10"
		+ " STYLE=\"width: " + ".75" + " in; height: 0.25in\""
		+ ">"
		+ SMUtilities.getDatePickerString(sParameter, getServletContext());
	}

	public void doGet(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
		
		doPost(request, response);
	}
}
