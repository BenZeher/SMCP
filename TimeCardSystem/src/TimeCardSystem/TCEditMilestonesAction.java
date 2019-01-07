package TimeCardSystem;

import java.io.IOException;
import java.net.URLEncoder;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import ServletUtilities.clsManageRequestParameters;
import TCSDataDefinition.TCSTableMilestones;

public class TCEditMilestonesAction extends HttpServlet{

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
		TCMilestones entry = new TCMilestones(request);
		CurrentSession.removeAttribute(TCMilestones.ParamObjectName);
		
		//If it's a 'delete', try to do that:
		if (clsManageRequestParameters.get_Request_Parameter(
			TCEditMilestonesEdit.SUBMIT_DELETE_BUTTON_NAME, request).compareToIgnoreCase(TCEditMilestonesEdit.SUBMIT_DELETE_BUTTON_LABEL) == 0){
			if (clsManageRequestParameters.get_Request_Parameter(TCEditMilestonesEdit.CONFIRM_DELETE_CHECKBOX_NAME, request).compareToIgnoreCase("") == 0){
				CurrentSession.setAttribute(TCMilestones.ParamObjectName, entry);
				response.sendRedirect(
					"" + TimeCardUtilities.getURLLinkBase(getServletContext()) + sCallingClass
					+ "?" + TCSTableMilestones.lid + "=-1"
					+ "&CallingClass=" + "TimeCardSystem.TCEditMilestonesSelection"
					+ "&Warning=" + URLEncoder.encode("You must check the 'confirming' checkbox to delete.","UTF-8")
				);
			}else{
				try {
					entry.delete(entry.get_slid(), sDBID, getServletContext(), sUser);
				} catch (Exception e) {
					CurrentSession.setAttribute(TCMilestones.ParamObjectName, entry);
					response.sendRedirect(
						"" + TimeCardUtilities.getURLLinkBase(getServletContext()) + sCallingClass
						+ "?CallingClass=" + "TimeCardSystem.TCEditMilestonesSelection"
						+ "&Warning=" + URLEncoder.encode(e.getMessage(),"UTF-8")
					);
					return;
				}
				response.sendRedirect(
					"" + TimeCardUtilities.getURLLinkBase(getServletContext()) + sCallingClass
					+ "?" + TCSTableMilestones.lid + "=-1"
					+ "&CallingClass=" + "TimeCardSystem.TCEditMilestonesSelection"
					+ "&Status=" + URLEncoder.encode(TCMilestones.ParamObjectName + " '" + entry.get_sName() + "' was deleted.","UTF-8")
				);
			}
			return;
		}
		
		//If it's a 'save', try to do that:
		if (clsManageRequestParameters.get_Request_Parameter(
			TCEditMilestonesEdit.SUBMIT_EDIT_BUTTON_NAME, request).compareToIgnoreCase(TCEditMilestonesEdit.SUBMIT_EDIT_BUTTON_LABEL) == 0){
			try {
				entry.save(getServletContext(), sDBID, sUser, request);
			} catch (Exception e) {
				CurrentSession.setAttribute(TCMilestones.ParamObjectName, entry);
				response.sendRedirect(
					"" + TimeCardUtilities.getURLLinkBase(getServletContext()) + sCallingClass
					+ "?CallingClass=" + "TimeCardSystem.TCEditMilestonesSelection"
					+ "&Warning=" + URLEncoder.encode(e.getMessage(),"UTF-8")
				);
				return;
			}
			String sRedirectString = "" + TimeCardUtilities.getURLLinkBase(getServletContext()) + sCallingClass
					+ "?" + TCSTableMilestones.lid + "=" + entry.get_slid()
					+ "&CallingClass=" + "TimeCardSystem.TCEditMilestonesSelection"
					+ "&Status=" + TCMilestones.ParamObjectName + " '" + entry.get_sName() + "' was updated.";
			System.out.println("[6486593927] sRedirectString = '" + sRedirectString + "'.");
			response.sendRedirect(
				"" + TimeCardUtilities.getURLLinkBase(getServletContext()) + sCallingClass
				+ "?" + TCSTableMilestones.lid + "=" + entry.get_slid()
				+ "&CallingClass=" + "TimeCardSystem.TCEditMilestonesSelection"
				+ "&Status=" + URLEncoder.encode(TCMilestones.ParamObjectName + " '" + entry.get_sName() + "' was updated.","UTF-8")
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