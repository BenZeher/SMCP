package smas;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import smcontrolpanel.SMMasterEditEntry;
import smcontrolpanel.SMSystemFunctions;
import smcontrolpanel.SMUtilities;
import SMDataDefinition.SMTablessoptions;
import ServletUtilities.clsCreateHTMLTableFormFields;

public class ASEditSSOptionsEdit  extends HttpServlet {

	private static final long serialVersionUID = 1L;
	
	public void doPost(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
		
		SSOptions entry = new SSOptions(request);
		SMMasterEditEntry smedit = new SMMasterEditEntry(
				request,
				response,
				getServletContext(),
				entry.getobjectname(),
				SMUtilities.getFullClassName(this.toString()),
				"smas.ASEditSSOptionsAction",
				"smcontrolpanel.SMUserLogin",
				"Go back to user login",
				SMSystemFunctions.ASEditSSOptions
				);
		
		if (!smedit.processSession(getServletContext(), SMSystemFunctions.ASEditSSOptions)){
			smedit.getPWOut().println("Error in process session: " + smedit.getErrorMessages());
			return;
		}
		
		//If this is a 'resubmit', meaning it's being called by the 'Action' class, then
		//the session will have an entry object in it, and that's what we'll pick up.
		HttpSession currentSession = smedit.getCurrentSession();
		
	    if (currentSession.getAttribute(entry.getobjectname()) != null){
	    	entry = (SSOptions) currentSession.getAttribute(entry.getobjectname());
	    	currentSession.removeAttribute(entry.getobjectname());
	    //But if it's NOT a 'resubmit', meaning this class was called for the first time to 
	    //edit, we'll pick up the ID or key from the request and try to load the entry:
	    }else{
    		try {
				entry.load(getServletContext(), smedit.getConfFile(), smedit.getUserName());
			} catch (Exception e) {
				response.sendRedirect(
						"" + SMUtilities.getURLLinkBase(getServletContext()) + "smas.ASMainMenu"
						+ "?Warning=" + e.getMessage()
						+ "&" + SMUtilities.REQUEST_PARAM_SESSIONTAG + "=" + smedit.getSessionTag()
					);
					return;
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
		smedit.setbIncludeDeleteButton(false);
	    try {
			smedit.createEditPage(getEditHTML(smedit, entry), "");
		} catch (Exception e) {
    		String sError = "Could not create edit page - " + e.getMessage();
			response.sendRedirect(
				"" + SMUtilities.getURLLinkBase(getServletContext()) + "" + "smas.ASMainMenu"
				+ "?Warning=Could not load " + entry.getobjectname() + " " + sError
				+ "&" + SMUtilities.REQUEST_PARAM_SESSIONTAG + "=" + smedit.getSessionTag()
			);
			return;
		}
	    return;
	}
	private String getEditHTML(SMMasterEditEntry sm, SSOptions entry) throws Exception{

		String s = "<TABLE BORDER=1>";

		//Track user location:
		s += clsCreateHTMLTableFormFields.Create_Edit_Form_Checkbox_Row(
			SMTablessoptions.itrackuserlocations, 
			Integer.parseInt(entry.getstrackuserlocation()), 
			"Track user location?", 
			"If checked, the browser will record the location of users when they activate devices, set alarms, etc."
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
