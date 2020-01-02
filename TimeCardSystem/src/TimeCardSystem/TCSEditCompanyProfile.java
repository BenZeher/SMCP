package TimeCardSystem;

import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;

import java.sql.*;

import TCSDataDefinition.TCSTablecompanyprofile;
import ServletUtilities.*;

public class TCSEditCompanyProfile extends HttpServlet{

	private static final long serialVersionUID = 1L;

	public void doGet(HttpServletRequest request,
			HttpServletResponse response)
	throws ServletException, IOException {

		response.setContentType("text/html");
		HttpSession CurrentSession = request.getSession();
		PrintWriter out = response.getWriter();

		String title = TCWebContextParameters.getInitProgramTitle(getServletContext());
		String subtitle = "Edit Company Profile";

		out.println(TimeCardUtilities.TCTitleSubBGColorWithFont(
			title, 
			subtitle, 
			TimeCardUtilities.BACKGROUND_COLOR_FOR_ADMIN_SCREENS, 
			TimeCardUtilities.BASE_FONT_FAMILY)
		);

		out.println("<BR><A HREF=\"" + TCWebContextParameters.getURLLinkBase(getServletContext()) + "TimeCardSystem.AdminMain\">Return to main menu</A><BR><BR>");

		try {
				String sSQL = "SELECT * FROM " + TCSTablecompanyprofile.TableName;
				ResultSet rs = clsDatabaseFunctions.openResultSet(
						sSQL, 
						getServletContext(), 
						(String) CurrentSession.getAttribute(TimeCardUtilities.SESSION_ATTRIBUTE_DB), 
						"MySQL", 
						"TCSEditCompanyProfile - getting profile"
				);
				if (rs.next()){
					out.println ("<FORM ACTION =\"" 
							+ TCWebContextParameters.getURLLinkBase(getServletContext()) 
							+ "TimeCardSystem.TCSEditCompanyProfileAction\">");
					out.println ("<TABLE BORDER=12 CELLSPACING=2>" );
					//Companyname
					out.println ("<TR><TD><B>Company name</B></TD><TD>" + 
							"<INPUT TYPE=TEXT NAME=\"" + TCSTablecompanyprofile.sCompanyName + "\""
							+ " SIZE=50 MAXLENGTH=" + TCSTablecompanyprofile.sCompanyNameLength + " VALUE=\"" 
							+ rs.getString(TCSTablecompanyprofile.sCompanyName)+ "\"></TD></TR>");
					//PeriodLength
					out.println ("<TR><TD><B>Pay period frequency: </B></TD><TD>" + 
							"<INPUT TYPE=TEXT NAME=\"" + TCSTablecompanyprofile.sPeriodLength + "\""
							+ " SIZE=10 MAXLENGTH=" + TCSTablecompanyprofile.sPeriodLengthLength + " VALUE=\"" 
							+ rs.getString(TCSTablecompanyprofile.sPeriodLength)
							+ "\"> In weeks (integers only)</TD></TR>");
					//Link to document with links to mics online files. 
					out.println ("<TR><TD><B>Login Screen Link: </B></TD><TD>" + 
							"<INPUT TYPE=TEXT NAME=\"" + TCSTablecompanyprofile.stcsloginpagedoclink + "\""
							+ " SIZE=150 MAXLENGTH=" + TCSTablecompanyprofile.stcsloginpagedoclinkLength + " VALUE=\"" 
							+ rs.getString(TCSTablecompanyprofile.stcsloginpagedoclink)
							+ "\"></TD></TR>");

					out.println ("</TABLE>");
					
		        	out.println ("<BR>");
		        	out.println ("<INPUT TYPE=\"SUBMIT\" VALUE=\"----Save----\">");
		        	out.println("</FORM>");
		        	out.println ("<BR>");

				}else{
					out.println("Could not read company profile." + "<BR>");
				}
				rs.close();

		} catch (SQLException ex) {
			// handle any errors
			out.println("<BR><BR>Error!!<BR>");
			out.println("SQLException: " + ex.getMessage() + "<BR>");
			out.println("SQLState: " + ex.getSQLState() + "<BR>");
			out.println("SQL: " + ex.getErrorCode() + "<BR>");
		}

		out.println("</BODY></HTML>");
	}
}