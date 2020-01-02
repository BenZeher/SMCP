package TimeCardSystem;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import ServletUtilities.clsDatabaseFunctions;
import TCSDataDefinition.*;

public class ManageACGroups extends HttpServlet {

	private static final long serialVersionUID = 1L;

	public void doPost(HttpServletRequest request,
				HttpServletResponse response)
				throws ServletException, IOException {

	    //Get the session info:
	    HttpSession CurrentSession = request.getSession(true);
	    String sConfFile = CurrentSession.getAttribute(TimeCardUtilities.SESSION_ATTRIBUTE_DB).toString();
	    
	    response.setContentType("text/html");
	    
		PrintWriter out = response.getWriter();
	    String title = "Manage Security Groups";
	    String subtitle = "";
	    out.println(TimeCardUtilities.TCTitleSubBGColor(title, subtitle, TimeCardUtilities.BACKGROUND_COLOR_FOR_ADMIN_SCREENS));

	    //Print a link to the first page after login:
	    out.println("<BR><A HREF=\"" + TCWebContextParameters.getURLLinkBase(getServletContext()) + "TimeCardSystem.AdminMain\">Return to main menu</A><BR><BR>");
	    
	    out.println("<FORM NAME=\"MAINFORM\" ACTION=\"" + TCWebContextParameters.getURLLinkBase(getServletContext()) + "TimeCardSystem.ManageACGroupsEdit\" METHOD=\"POST\">");
	    
	    String sOutPut = "";
	    
	    //Add drop down list
	    String sSQL = "";
		try{
	        sSQL = TimeCardSQLs.Get_Access_Control_Groups_SQL();
	        ResultSet rs = clsDatabaseFunctions.openResultSet(sSQL, getServletContext(), sConfFile);
	     	out.println ("<SELECT NAME=\"Security Groups\">" );
        	
        	while (rs.next()){
        		//flag that there are multiple entries.
        		sOutPut = "<OPTION VALUE=\"" + rs.getString(ACGroups.sGroupName) + "\">";
        		sOutPut = sOutPut + rs.getString(ACGroups.sGroupName);
	        	out.println (sOutPut);
        	}
        	rs.close();
	        	//End the drop down list:
	        out.println ("</SELECT>");
	        out.println ("<BR>");
		}catch (SQLException ex){
	    	out.println("<BR><B><FONT COLOR=RED>Error listing security groups with SQL: " + sSQL + " - " + ex.getMessage() + ".</FONT></B></BR>");
			//return false;
		}
		//Display text boxes for the new password and a confirmation:
		
		sOutPut = "<P><INPUT TYPE=SUBMIT NAME=\"SubmitEdit\" VALUE=\"Edit Selected Group\" STYLE=\"width: 2.00in; height: 0.24in\"></P>";
		sOutPut = sOutPut + "<P><INPUT TYPE=SUBMIT NAME=\"SubmitDelete\" VALUE=\"Delete Selected Group\" STYLE=\"width: 2.00in; height: 0.24in\">";
		sOutPut = sOutPut + "  Check to confirm deletion: <INPUT TYPE=CHECKBOX NAME=\"ConfirmDelete\">";
		sOutPut = sOutPut + "<P><INPUT TYPE=SUBMIT NAME=\"SubmitAdd\" VALUE=\"Add New Group\" STYLE=\"width: 2.00in; height: 0.24in\"></P>";
		sOutPut = sOutPut + "<P>New Group To Be Added: <INPUT TYPE=TEXT NAME=\"NewGroupName\" SIZE=28 MAXLENGTH=50 STYLE=\"width: 2.41in; height: 0.25in\"></P>";
		sOutPut = sOutPut + "</FORM>";
		out.println(sOutPut);
		
		out.println("</BODY></HTML>");
	}
	
	public void doGet(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
		
		doPost(request, response);
	}
}
