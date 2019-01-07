package TimeCardSystem;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import ServletUtilities.clsManageRequestParameters;
import TCSDataDefinition.TCSTablemadgiceventtypes;

public class TCEditMADGICEventTypesAction extends HttpServlet{

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
		TCMADGICEventType entry = new TCMADGICEventType(request);
		CurrentSession.removeAttribute(TCMADGICEventType.ParamObjectName);
		
		//If it's a 'delete', try to do that:
		if (clsManageRequestParameters.get_Request_Parameter(
			TCEditMADGICEventTypesEdit.SUBMIT_DELETE_BUTTON_NAME, request).compareToIgnoreCase(TCEditMADGICEventTypesEdit.SUBMIT_DELETE_BUTTON_LABEL) == 0){
			if (clsManageRequestParameters.get_Request_Parameter(TCEditMADGICEventTypesEdit.CONFIRM_DELETE_CHECKBOX_NAME, request).compareToIgnoreCase("") == 0){
				CurrentSession.setAttribute(TCMADGICEventType.ParamObjectName, entry);
				response.sendRedirect(
					"" + TimeCardUtilities.getURLLinkBase(getServletContext()) + sCallingClass
					+ "?" + TCSTablemadgiceventtypes.lid + "=-1"
					+ "&CallingClass=" + "TimeCardSystem.TCEditMADGICEventTypesSelection"
					+ "&Warning=You must check the 'confirming' checkbox to delete."
				);
			}else{
				try {
					entry.delete(entry.get_slid(), sDBID, getServletContext(), sUser);
				} catch (Exception e) {
					CurrentSession.setAttribute(TCMADGICEventType.ParamObjectName, entry);
					response.sendRedirect(
						"" + TimeCardUtilities.getURLLinkBase(getServletContext()) + sCallingClass
						+ "?CallingClass=" + "TimeCardSystem.TCEditMADGICEventTypesSelection"
						+ "&Warning=" + e.getMessage()
					);
					return;
				}
				response.sendRedirect(
					"" + TimeCardUtilities.getURLLinkBase(getServletContext()) + sCallingClass
					+ "?" + TCSTablemadgiceventtypes.lid + "=-1"
					+ "&CallingClass=" + "TimeCardSystem.TCEditMADGICEventTypesSelection"
					+ "&Status=" + TCMADGICEventType.ParamObjectName + " '" + entry.get_sname() + "' was deleted."
				);
			}
			return;
		}
		
		//If it's a 'save', try to do that:
		if (clsManageRequestParameters.get_Request_Parameter(
			TCEditMADGICEventTypesEdit.SUBMIT_EDIT_BUTTON_NAME, request).compareToIgnoreCase(TCEditMADGICEventTypesEdit.SUBMIT_EDIT_BUTTON_LABEL) == 0){
			try {
				entry.save(getServletContext(), sDBID, sUser);
			} catch (Exception e) {
				CurrentSession.setAttribute(TCMADGICEventType.ParamObjectName, entry);
				response.sendRedirect(
					"" + TimeCardUtilities.getURLLinkBase(getServletContext()) + sCallingClass
					+ "?CallingClass=" + "TimeCardSystem.TCEditMADGICEventTypesSelection"
					+ "&Warning=" + e.getMessage()
				);
				return;
			}
			String sRedirectString = "" + TimeCardUtilities.getURLLinkBase(getServletContext()) + sCallingClass
					+ "?" + TCSTablemadgiceventtypes.lid + "=" + entry.get_slid()
					+ "&CallingClass=" + "TimeCardSystem.TCEditMADGICEventTypesSelection"
					+ "&Status=" + TCMADGICEventType.ParamObjectName + " '" + entry.get_sname() + "' was updated.";
			System.out.println("[1486593927] sRedirectString = '" + sRedirectString + "'.");
			response.sendRedirect(
				"" + TimeCardUtilities.getURLLinkBase(getServletContext()) + sCallingClass
				+ "?" + TCSTablemadgiceventtypes.lid + "=" + entry.get_slid()
				+ "&CallingClass=" + "TimeCardSystem.TCEditMADGICEventTypesSelection"
				+ "&Status=" + TCMADGICEventType.ParamObjectName + " '" + entry.get_sname() + "' was updated."
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