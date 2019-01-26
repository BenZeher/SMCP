package smcontrolpanel;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import SMDataDefinition.SMTablemechanics;
import ServletUtilities.clsDatabaseFunctions;

/* Servlet to remove a selected mechanic.*/

public class SMEditMechanicsRemove extends HttpServlet{
	private static final long serialVersionUID = 1L;
	@Override
	public void doGet(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {

	    response.setContentType("text/html");

		PrintWriter out = response.getWriter();
		if (!SMAuthenticate.authenticateSMCPCredentials(
				request, 
				response, 
				getServletContext(), 
				SMSystemFunctions.SMEditMechanics))
		{
			return;
		}

	    //Get the session info:
	    HttpSession CurrentSession = request.getSession(true);
	    String sDBID = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_DATABASE_ID);
	    String sCompanyName = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_COMPANYNAME);
	    String sUser = (String) CurrentSession.getAttribute(SMUtilities.SMCP_SESSION_PARAM_USERNAME);
	    String title = "Remove Mechanics";
	    String subtitle = "";
	    out.println(SMUtilities.SMCPTitleSubBGColor(
	    	title, 
	    	subtitle, 
	    	SMUtilities.getInitBackGroundColor(getServletContext(),sDBID),
	    	sCompanyName)
	    );

	    //Print a link to the first page after login:
	    out.println("<BR><A HREF=\"" + SMUtilities.getURLLinkBase(getServletContext()) + "smcontrolpanel.SMUserLogin?" 
				+ SMUtilities.SMCP_REQUEST_PARAM_DATABASE_ID + "=" + sDBID 
				+ "\">Return to user login</A><BR>");

    	if (request.getParameter("DoubleCheck") == null){
    		//don't delete, just go back.
    		out.println("You chose to delete, but you didn't click the checkbox to confirm.");
    	}else{
    		//delete the selected entry
    		String sSQL = "DELETE FROM " + SMTablemechanics.TableName
    			+ " WHERE ("
    				+ "(" + SMTablemechanics.lid + " = " + request.getParameter("Mechanic") + ")"
    			+ ")";
    		try {
				clsDatabaseFunctions.executeSQL(
					sSQL, 
					getServletContext(), 
					sDBID, 
					"MySQL", 
					SMUtilities.getFullClassName(this.toString() + " - user: " + sUser)
				);
			} catch (SQLException e) {
				out.println("Error deleting mechanic with SQL: " + sSQL + " - " + e.getMessage() + ".<BR>");
				out.println("</BODY></HTML>");
				return;
			}
	    }
    	out.println("Successfully deleted mechanic.<BR>");
    	out.println("</BODY></HTML>");
    	return;
	}
}