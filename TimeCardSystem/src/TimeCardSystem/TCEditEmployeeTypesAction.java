package TimeCardSystem;

import java.io.IOException;
import java.net.URLEncoder;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import ServletUtilities.clsManageRequestParameters;
import TCSDataDefinition.TCSTableEmployeeTypes;

public class TCEditEmployeeTypesAction extends HttpServlet{

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
		TCEmployeeType entry = new TCEmployeeType(request);
		CurrentSession.removeAttribute(TCEmployeeType.ParamObjectName);
		
		//If it's a 'delete', try to do that:
		if (clsManageRequestParameters.get_Request_Parameter(
			TCEditEmployeeTypesEdit.SUBMIT_DELETE_BUTTON_NAME, request).compareToIgnoreCase(TCEditEmployeeTypesEdit.SUBMIT_DELETE_BUTTON_LABEL) == 0){
			if (clsManageRequestParameters.get_Request_Parameter(TCEditEmployeeTypesEdit.CONFIRM_DELETE_CHECKBOX_NAME, request).compareToIgnoreCase("") == 0){
				CurrentSession.setAttribute(TCEmployeeType.ParamObjectName, entry);
				response.sendRedirect(
					"" + TimeCardUtilities.getURLLinkBase(getServletContext()) + sCallingClass
					+ "?" + TCSTableEmployeeTypes.lid + "=-1"
					+ "&CallingClass=" + "TimeCardSystem.TCEditEmployeeTypeSelection"
					+ "&Warning=" + URLEncoder.encode("You must check the 'confirming' checkbox to delete.","UTF-8")
				);
			}else{
				try {
					entry.delete(entry.get_slid(), sDBID, getServletContext(), sUser);
				} catch (Exception e) {
					CurrentSession.setAttribute(TCEmployeeType.ParamObjectName, entry);
					response.sendRedirect(
						"" + TimeCardUtilities.getURLLinkBase(getServletContext()) + sCallingClass
						+ "?CallingClass=" + "TimeCardSystem.TCEditEmployeeTypeSelection"
						+ "&Warning=" + URLEncoder.encode(e.getMessage(),"UTF-8")
					);
					return;
				}
				response.sendRedirect(
					"" + TimeCardUtilities.getURLLinkBase(getServletContext()) + sCallingClass
					+ "?" + TCSTableEmployeeTypes.lid + "=-1"
					+ "&CallingClass=" + "TimeCardSystem.TCEditEmployeeTypeSelection"
					+ "&Status=" + URLEncoder.encode(TCEmployeeType.ParamObjectName + " '" + entry.get_sName() + "' was deleted.","UTF-8")
				);
			}
			return;
		}
		
		//If it's a 'save', try to do that:
		if (clsManageRequestParameters.get_Request_Parameter(
				TCEditEmployeeTypesEdit.SUBMIT_EDIT_BUTTON_NAME, request).compareToIgnoreCase(TCEditEmployeeTypesEdit.SUBMIT_EDIT_BUTTON_LABEL) == 0){
			try {
				//Save the employee type
				entry.save(getServletContext(), sDBID, sUser, request);
				//Update users in employee type
				entry.updateEmployeeTypeLinksTable(entry.get_slid(), getServletContext(), sDBID, request);
				//Update access users of employee type
				entry.updateEmployeeTypeAccessTable(entry.get_slid(), getServletContext(), sDBID, request);
				
			} catch (Exception e) {
				CurrentSession.setAttribute(TCEmployeeType.ParamObjectName, entry);
				response.sendRedirect(
					"" + TimeCardUtilities.getURLLinkBase(getServletContext()) + sCallingClass
					+ "?CallingClass=" + "TimeCardSystem.TCEditEmployeeTypeSelection"
					+ "&Warning=" + URLEncoder.encode(e.getMessage(),"UTF-8")
				);
				return;
			}
			response.sendRedirect(
				"" + TimeCardUtilities.getURLLinkBase(getServletContext()) + sCallingClass
				+ "?" + TCSTableEmployeeTypes.lid + "=" + entry.get_slid()
				+ "&CallingClass=" + "TimeCardSystem.TCEditEmployeeTypeSelection"
				+ "&Status=" + URLEncoder.encode(TCEmployeeType.ParamObjectName + " '" + entry.get_sName() + "' was updated.","UTF-8")
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
