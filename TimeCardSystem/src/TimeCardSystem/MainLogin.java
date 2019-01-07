package TimeCardSystem;

import java.io.*;

//import java.util.Date;
import javax.servlet.*;
import javax.servlet.http.*;


import ServletUtilities.clsServletUtilities;
import ServletUtilities.clsDatabaseFunctions;
import TCSDataDefinition.TCSTablecompanyprofile;

import java.sql.*;

/** Servlet that reads pin number for validation.*/

public class MainLogin extends HttpServlet {
    //Parameters that must be included in time card links to change login screens
	public static final String ADMIN_LOGIN_PARAM = "admin";
	public static final String MILESTONES_LOGIN_PARAM = "milestones";
	
	private static final long serialVersionUID = 1L;
	public static String CLASS_NAME = "TimeCardSystem.MainLogin";
	public void doPost(HttpServletRequest request,
				HttpServletResponse response)
				throws ServletException, IOException {
		

				
	    response.setContentType("text/html");
	    
	    PrintWriter out = response.getWriter();
	    String title = ConnectionPool.WebContextParameters.getInitProgramTitle(getServletContext());
	    

	    //check for valid db name
	    if (request.getParameter(TimeCardUtilities.REQUEST_PARAMETER_DB) == null){
	    		out.println("<BR>Missing 'db' parameter - cannot log in.");
	    		out.println("</BODY></HTML>");
	    		return;
	    }
	    
		String sDb = "";
	    try {
	    	sDb = TimeCardUtilities.getDatabaseName(request, null, getServletContext());
		} catch (Exception e) {
			out.println("<BR>"+e.getMessage()+"");
			out.println("</BODY></HTML>");
			return;
		}
	    
	    	    
		String sCompanyName = "";
		String sDocLink = "";
		String sLoginClass = "UserLogin";
		String subtitle = "Login";
		String sDatabaseServer = "";
		
	    try {
	    	sDatabaseServer = TimeCardUtilities.getDatabaseServer(request, null, getServletContext());
		} catch (Exception e) {
			out.println("<BR>"+e.getMessage()+"");
			out.println("</BODY></HTML>");
			return;
		}
	    
		//Get the company information:
		String sSQL = "SELECT * FROM " + TCSTablecompanyprofile.TableName;
		try {
			ResultSet rs = clsDatabaseFunctions.openResultSet(
					sSQL, 
					getServletContext(), 
					sDb, 
					"MySQL",
					this.toString() + ".reading company name"
					);
			if (rs.next()){
				sCompanyName = rs.getString(TCSTablecompanyprofile.sCompanyName);
				sDocLink = rs.getString(TCSTablecompanyprofile.stcsloginpagedoclink);
				rs.close();
			}else{
				out.println("<BR>Could not read company name.");
				out.println("</BODY></HTML>");
				rs.close();
				return;
			}
		} catch (SQLException e) {
			out.println("<BR>Error reading read company name: " + e.getMessage()+ ".");
			out.println("</BODY></HTML>");
			return;
		}
		
		//Check login link parameters to direct user to correct screen after login.
		boolean bAdminLogin = request.getParameter(ADMIN_LOGIN_PARAM) != null;
		boolean bEditMilestonesLogin = request.getParameter(MILESTONES_LOGIN_PARAM) != null;;
		
		if (bAdminLogin){
			sLoginClass = "AdminMain";
			subtitle = "Administrative Login";
		}

		if(bEditMilestonesLogin){
			sLoginClass = "TCEditEmployeeMilestones";
			subtitle = "Edit Milestone Login";
		}
		
	    //If someone is logging into the Admin functions, set that title here:
	    out.println(TimeCardUtilities.TCTitleSubBGColor(
	    		title, 
	    		subtitle, 
	    		TimeCardUtilities.BACKGROUND_COLOR_FOR_USER_SCREENS)
	    		);
		out.println(
				"Version " + TimeCardUtilities.sProgramVersion
				+ " last updated " + TimeCardUtilities.sLastUpdated
				+ " currently running on server <B>" + clsServletUtilities.getHostName()
				+ " </B>. Using database server <B>"+sDatabaseServer+"</B> for company <B>" + sCompanyName + "</B>" + ".<BR><BR>"
		);
	    
		//Now create the form:
		out.println("<FORM NAME=MAINFORM METHOD=POST ACTION=\"/" 
				+ ConnectionPool.WebContextParameters.getInitWebAppName(getServletContext()) 
				+ "/TimeCardSystem." 
				+ sLoginClass 
				+ "\">");
		out.println("Pass Code: <INPUT TYPE=PASSWORD NAME='" + TimeCardUtilities.REQUEST_PARAMETER_PINNUMBER + "' SIZE=6 MAXLENGTH=6><BR>");
		out.println("<INPUT TYPE=HIDDEN NAME=db VALUE=\"" + sDb + "\">");
		out.println("<BR>");
		out.println("<LEFT><INPUT TYPE=\"SUBMIT\" VALUE=\"Login\"></LEFT>");
		out.println("</FORM>");
		
		//out.println("<BR><BR>");
		//out.println("<A HREF=\"https://docs.google.com/spreadsheet/ccc?key=0Au9HsBkYjPmhdDdDOS1hVnE2VWcxSk1obGdmNktPbWc#gid=0\" target=linkframe><FONT SIZE=4><B>Training Matrix</B></FONT></A><BR>");
		
		if(!bEditMilestonesLogin){
			out.println("<iframe " +
				"width=100% " +
				"height=100% " +
				"frameborder=0 " +
				"name=linkframe " +
				"src=\"" + sDocLink + "\" " +
				"target=\"_self\"></iframe>");
		}
	    out.println("</BODY></HTML>");
	}
	
	public void doGet(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
		
		doPost(request, response);
	}
}

