package TimeCardSystem;

import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;
import java.sql.SQLException;

import ServletUtilities.*;
import TCSDataDefinition.TCSTablecompanyprofile;

public class TCSEditCompanyProfileAction extends HttpServlet{

	private static final long serialVersionUID = 1L;

	public void doGet(HttpServletRequest request,
			HttpServletResponse response)
	throws ServletException, IOException {

		response.setContentType("text/html");
		HttpSession CurrentSession = request.getSession();
		PrintWriter out = response.getWriter();

		String title = TCWebContextParameters.getInitProgramTitle(getServletContext());
		String subtitle = "";

		out.println(TimeCardUtilities.TCTitleSubBGColor(title, subtitle, TimeCardUtilities.BACKGROUND_COLOR_FOR_ADMIN_SCREENS));

		String sCompanyName = clsManageRequestParameters.get_Request_Parameter(
				TCSTablecompanyprofile.sCompanyName, request).trim().replace("&quot;", "\"");
		String sPeriodLength = clsManageRequestParameters.get_Request_Parameter(
				TCSTablecompanyprofile.sPeriodLength, request).trim().replace("&quot;", "\"");
		String sDocLink = clsManageRequestParameters.get_Request_Parameter(
				TCSTablecompanyprofile.stcsloginpagedoclink, request).trim().replace("&quot;", "\"");
		try {
			int i = Integer.parseInt(sPeriodLength);
			sPeriodLength = Integer.toString(i);
		} catch (NumberFormatException e) {
			out.println("<BR><BR>Invalid period frequency: '" + sPeriodLength + "'<BR>");
			out.println ("<A href=" 
					+ TCWebContextParameters.getURLLinkBase(getServletContext()) 
					+ "TimeCardSystem.TCSEditCompanyProfile>Click here to return to edit company profile.</A>");
			out.println("</BODY></HTML>");
			return;
			
		}
		String SQL = "UPDATE " + TCSTablecompanyprofile.TableName
					+ " SET " + TCSTablecompanyprofile.sCompanyName + " = '" + sCompanyName + "'"
					+ ", " + TCSTablecompanyprofile.sPeriodLength + " = '" + sPeriodLength + "'"
					+ ", " + TCSTablecompanyprofile.stcsloginpagedoclink + " = '" + sDocLink + "'"
					;

		try {
			if (!clsDatabaseFunctions.executeSQL(
					SQL, 
					getServletContext(), 
					(String) CurrentSession.getAttribute(TimeCardUtilities.SESSION_ATTRIBUTE_DB),
					"MySQL", 
			"TCSEditCompanyProfileAction")){

				out.println("<BR><BR>Could not save company profile.<BR>");
				out.println ("<A href=" 
						+ TCWebContextParameters.getURLLinkBase(getServletContext()) 
						+ "TimeCardSystem.TCSEditCompanyProfile>Click here to return to edit company profile.</A>");
				out.println("</BODY></HTML>");
				return;

			}
		} catch (SQLException e) {
			out.println("<BR><BR>Could not save company profile - " + e.getMessage() + ".<BR>");
			out.println ("<A href=" 
					+ TCWebContextParameters.getURLLinkBase(getServletContext()) 
					+ "TimeCardSystem.TCSEditCompanyProfile>Click here to return to edit company profile.</A>");	
			out.println("</BODY></HTML>");
			return;

		}
		out.println ("<H4>Company profile successfully saved.</H4><BR><BR>");
    	out.println("<META http-equiv='Refresh' content='3;URL=" 
    			+ TCWebContextParameters.getURLLinkBase(getServletContext()) 
    			+ "TimeCardSystem.TCSEditCompanyProfile'>");
		out.println("</BODY></HTML>");
		return;
	}
}