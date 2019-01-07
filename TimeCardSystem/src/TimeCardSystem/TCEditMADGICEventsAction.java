package TimeCardSystem;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import ServletUtilities.clsManageRequestParameters;
import TCSDataDefinition.TCSTablemadgicevents;

public class TCEditMADGICEventsAction extends HttpServlet{

	private static final long serialVersionUID = 1L;
	public void doPost(HttpServletRequest request,
			HttpServletResponse response)
					throws ServletException, IOException {

		response.setContentType("text/html");
		
	    HttpSession CurrentSession = request.getSession(true);
	    String sDBID = (String) CurrentSession.getAttribute(TimeCardUtilities.SESSION_ATTRIBUTE_DB);
	    String sUser = (String) CurrentSession.getAttribute(TimeCardUtilities.SESSION_ATTRIBUTE_EID);
		String sCallingClass = clsManageRequestParameters.get_Request_Parameter("CallingClass", request);
		
		//Read the entry fields from the request object:
		TCMADGICEvent entry = new TCMADGICEvent(request);
		
		CurrentSession.removeAttribute(TCMADGICEvent.ParamObjectName);
		
		//If it's a 'delete', try to do that:
		if (clsManageRequestParameters.get_Request_Parameter(
			TCEditMADGICEventsEdit.SUBMIT_DELETE_BUTTON_NAME, request).compareToIgnoreCase(TCEditMADGICEventsEdit.SUBMIT_DELETE_BUTTON_LABEL) == 0){
			if (clsManageRequestParameters.get_Request_Parameter(TCEditMADGICEventsEdit.CONFIRM_DELETE_CHECKBOX_NAME, request).compareToIgnoreCase("") == 0){
				CurrentSession.setAttribute(TCMADGICEvent.ParamObjectName, entry);
				response.sendRedirect(
					"" + TimeCardUtilities.getURLLinkBase(getServletContext()) + sCallingClass
					+ "?" + TCSTablemadgicevents.lid + "=-1"
					+ "&CallingClass=" + "TimeCardSystem.TCEditMADGICEventsSelection"
					+ "&Warning=You must check the 'confirming' checkbox to delete."
				);
			}else{
				try {
					entry.delete(entry.get_slid(), sDBID, getServletContext(), sUser);
				} catch (Exception e) {
					CurrentSession.setAttribute(TCMADGICEvent.ParamObjectName, entry);
					response.sendRedirect(
						"" + TimeCardUtilities.getURLLinkBase(getServletContext()) + sCallingClass
						+ "?CallingClass=" + "TimeCardSystem.TCEditMADGICEventsSelection"
						+ "&Warning=" + e.getMessage()
					);
					return;
				}
				response.sendRedirect(
					"" + TimeCardUtilities.getURLLinkBase(getServletContext()) + sCallingClass
					+ "?" + TCSTablemadgicevents.lid + "=-1"
					+ "&CallingClass=" + "TimeCardSystem.TCEditMADGICEventsSelection"
					+ "&Status=" + TCMADGICEvent.ParamObjectName + " '" + entry.get_seventtypename() + "', " + entry.get_sdatevent() + " was deleted."
				);
			}
			return;
		}
		
		//If it's a 'save', try to do that:
		if (clsManageRequestParameters.get_Request_Parameter(
			TCEditMADGICEventsEdit.SUBMIT_EDIT_BUTTON_NAME, request).compareToIgnoreCase(TCEditMADGICEventsEdit.SUBMIT_EDIT_BUTTON_LABEL) == 0){
			try {
				entry.save(getServletContext(), sDBID, sUser, request);
			} catch (Exception e) {
				CurrentSession.setAttribute(TCMADGICEvent.ParamObjectName, entry);
				response.sendRedirect(
					"" + TimeCardUtilities.getURLLinkBase(getServletContext()) + sCallingClass
					+ "?CallingClass=" + "TimeCardSystem.TCEditMADGICEventsSelection"
					+ "&Warning=" + e.getMessage()
				);
				return;
			}
			//String sRedirecttring = TimeCardUtilities.getURLLinkBase(getServletContext()) + sCallingClass
			//		+ "?" + TCSTablemadgicevents.lid + "=" + entry.get_slid()
			//		+ "&CallingClass=" + "TimeCardSystem.TCEditMADGICEventsSelection"
			//		+ "&Status=" + TCMADGICEvent.ParamObjectName + " '" + entry.get_seventtypename() + "', " + entry.get_sdatevent() + " was updated.";
			response.sendRedirect(
				"" + TimeCardUtilities.getURLLinkBase(getServletContext()) + sCallingClass
				+ "?" + TCSTablemadgicevents.lid + "=" + entry.get_slid()
				+ "&CallingClass=" + "TimeCardSystem.TCEditMADGICEventsSelection"
				+ "&Status=" + TCMADGICEvent.ParamObjectName + " '" + entry.get_seventtypename() + "', " + entry.get_sdatevent() + " was updated."
			);
			return;
		}
		return;
	}
	public void doGet(HttpServletRequest request,
			HttpServletResponse response)
					throws ServletException, IOException {

		doPost(request, response);
	}
}