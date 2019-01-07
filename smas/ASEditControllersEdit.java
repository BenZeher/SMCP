package smas;

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
import SMDataDefinition.SMTablesscontrollers;
import ServletUtilities.clsCreateHTMLTableFormFields;

public class ASEditControllersEdit  extends HttpServlet {

	private static final long serialVersionUID = 1L;
	private static final String OBJECT_NAME = SSController.ParamObjectName;
	public void doPost(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
		
		SSController entry = new SSController(request);
		SMMasterEditEntry smedit = new SMMasterEditEntry(
				request,
				response,
				getServletContext(),
				entry.getObjectName(),
				SMUtilities.getFullClassName(this.toString()),
				"smas.ASEditControllersAction",
				"smcontrolpanel.SMUserLogin",
				"Go back to user login",
				SMSystemFunctions.ASEditControllers
				);
		
		if (!smedit.processSession(getServletContext(), SMSystemFunctions.ASEditControllers)){
			smedit.getPWOut().println("Error in process session: " + smedit.getErrorMessages());
			return;
		}
		
		//If this is a 'resubmit', meaning it's being called by the 'Action' class, then
		//the session will have a job cost entry object in it, and that's what we'll pick up.
		HttpSession currentSession = smedit.getCurrentSession();
		
	    if (currentSession.getAttribute(OBJECT_NAME) != null){
	    	entry = (SSController) currentSession.getAttribute(OBJECT_NAME);
	    	currentSession.removeAttribute(OBJECT_NAME);
	    //But if it's NOT a 'resubmit', meaning this class was called for the first time to 
	    //edit, we'll pick up the ID or key from the request and try to load the entry:
	    }else{
	    	if (!smedit.getAddingNewEntryFlag()){
	    		try {
					entry.load(getServletContext(), smedit.getConfFile(), smedit.getUserID(), smedit.getFullUserName());
				} catch (Exception e) {
					response.sendRedirect(
							"" + SMUtilities.getURLLinkBase(getServletContext()) + "smas.ASEditControllersSelect"
							+ "?" + SMTablesscontrollers.lid + "=" + entry.getslid()
							+ "&Warning=" + e.getMessage()
							+ "&" + SMUtilities.REQUEST_PARAM_SESSIONTAG + "=" + smedit.getSessionTag()
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
				"<BR><A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smas.ASMainMenu?" + SMUtilities.REQUEST_PARAM_SESSIONTAG + "=" 
					+ smedit.getSessionTag() + "\">Return to Alarm Systems Main Menu</A><BR>");
	    
		smedit.getPWOut().println("<BR>");
		
	    try {
			smedit.createEditPage(getEditHTML(smedit, entry), "");
		} catch (SQLException e) {
    		String sError = "Could not create edit page - " + e.getMessage();
			response.sendRedirect(
				"" + SMUtilities.getURLLinkBase(getServletContext()) + "" + smedit.getCallingClass()
				+ "?" + SMTablesscontrollers.lid + "=" + entry.getslid()
				+ "&Warning=Could not load " + OBJECT_NAME + " with ID: " + entry.getslid() + " - " + sError
				+ "&" + SMUtilities.REQUEST_PARAM_SESSIONTAG + "=" + smedit.getSessionTag()
			);
				return;
		}
	    return;
	}
	private String getEditHTML(SMMasterEditEntry sm, SSController entry) throws SQLException{

		String s = "<TABLE BORDER=1>";
		String sID = "NEW";
		if (
			(!sm.getAddingNewEntryFlag())
			|| (entry.getslid().compareToIgnoreCase("-1") != 0)
		){
			sID = entry.getslid();
		}
		s += "<TR><TD ALIGN=RIGHT><B>Controller</B>:</TD><TD><B>" 
			+ sID 
			+ "<INPUT TYPE=HIDDEN NAME=\"" + SMTablesscontrollers.lid + "\" VALUE=\"" 
			+ entry.getslid() + "\">"
			+ "</B></TD><TD>&nbsp;</TD></TR>";
		
		if (sm.getAddingNewEntryFlag()){
			s += clsCreateHTMLTableFormFields.Create_Edit_Form_Text_Input_Row(
					SMTablesscontrollers.scontrollername,
					entry.getscontrollername().replace("\"", "&quot;"), 
					SMTablesscontrollers.scontrollernamelength, 
					"<B>Controller name: ",
					"This name must match the one on the controller unit.",
					"40"
			);
		}else{
			s += "<TR><TD ALIGN=RIGHT><B>Controller: <FONT COLOR=RED>*Required*</FONT></B>:</TD>"
				+ "<TD>" 
				+ "<B>" + entry.getscontrollername() + "</B>" 
				+ "<INPUT TYPE=HIDDEN NAME=\"" + SMTablesscontrollers.scontrollername + "\" VALUE=\"" 
					+ entry.getscontrollername() + "\">"
				+ "</TD>"
				+ "<TD>&nbsp;</TD>"
				+ "</TR>"
				;
			
			s += "<TR><TD ALIGN=RIGHT><B>Date last maintained</B>:</TD>"
				+ "<TD>" 
				+ "<B>" + entry.getsdattimelastmaintained() + "</B>" 
				+ "<INPUT TYPE=HIDDEN NAME=\"" + SMTablesscontrollers.dattimelastmaintained + "\" VALUE=\"" 
					+ entry.getsdattimelastmaintained() + "\">"
				+ "</TD>"
				+ "<TD>&nbsp;</TD>"
				+ "</TR>"
			;

			s += "<TR><TD ALIGN=RIGHT><B>Last maintained by</B>:</TD>"
				+ "<TD>" 
				+ "<B>" + entry.getslastmaintainedbyfullname() + "</B>" 
				+ "<INPUT TYPE=HIDDEN NAME=\"" + SMTablesscontrollers.llastmaintainedbyid + "\" VALUE=\"" 
					+ entry.getllastmaintainedbyid() + "\">"
				+ "<INPUT TYPE=HIDDEN NAME=\"" + SMTablesscontrollers.slastmaintainedbyfullname + "\" VALUE=\"" 
					+ entry.getslastmaintainedbyfullname() + "\">"
				+ "</TD>"
				+ "<TD>&nbsp;</TD>"
				+ "</TR>"
			;
		}

		//Description
		s += clsCreateHTMLTableFormFields.Create_Edit_Form_Text_Input_Row(
				SMTablesscontrollers.sdescription,
				entry.getsdescription().replace("\"", "&quot;"), 
				SMTablesscontrollers.sdescriptionlength, 
				"<B>Description: <FONT COLOR=RED>*Required*</FONT></B>",
				"",
				"75"
		);

		//Active device?
		s += clsCreateHTMLTableFormFields.Create_Edit_Form_Checkbox_Row(
				SMTablesscontrollers.iactive, 
				Integer.parseInt(entry.getsactive()), 
				"Is controller active?", 
				"If unchecked, this controller and its devices won't be checked when listing devices and processing alarms."
			);
		
		//Pass code
		s += clsCreateHTMLTableFormFields.Create_Edit_Form_Text_Input_Row(
				SMTablesscontrollers.spasscode,
				entry.getspasscode().replace("\"", "&quot;"), 
				SMTablesscontrollers.spasscodelength, 
				"<B>Pass code: <FONT COLOR=RED>*Required*</FONT></B>",
				"This must match the passcode in the individual controller itself.",
				"40"
		);
		
		//Controller URL
		s += clsCreateHTMLTableFormFields.Create_Edit_Form_Text_Input_Row(
				SMTablesscontrollers.scontrollerurl,
				entry.getscontrollerurl().replace("\"", "&quot;"), 
				SMTablesscontrollers.scontrollerurllength, 
				"<B>Controller URL: <FONT COLOR=RED>*Required*</FONT></B>",
				"Enter the URL or IP address of the controller.",
				"40"
		);

		//Listening port
		s += clsCreateHTMLTableFormFields.Create_Edit_Form_Text_Input_Row(
				SMTablesscontrollers.slisteningport,
				entry.getslisteningport().replace("\"", "&quot;"), 
				SMTablesscontrollers.slisteningportlength, 
				"<B>Port to connect to controller: <FONT COLOR=RED>*Required*</FONT></B>",
				"Enter the port required to connect to the controller.  <FONT COLOR=RED><B>Keep in mind that if you are doing port"
				+ " forwarding to the controller, you enter the FORWARDING port here, not the actual port the controller is listening"
				+ " on, because then the program would not be able to connect.</B></FONT>"
				,
				"40"
		);
		
		s += "</TABLE>";
		return s;
	}

	public void doGet(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
		
		doPost(request, response);
	}
}
