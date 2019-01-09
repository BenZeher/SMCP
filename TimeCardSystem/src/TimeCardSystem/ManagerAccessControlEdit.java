package TimeCardSystem;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import ServletUtilities.clsDatabaseFunctions;

public class ManagerAccessControlEdit extends HttpServlet {

	private static final long serialVersionUID = 1L;

	/*
	 * This form is for editing time entries. 
	 * 1.	if there is already a date passed in, default to that date.
	 * 2.	if there is no date at all, default to today.
	 */
	public void doGet(HttpServletRequest request,
				HttpServletResponse response)
				throws ServletException, IOException {
	    
	    response.setContentType("text/html");
	    
	    PrintWriter out = response.getWriter();
	    String title = "Time Card System";
	    String subtitle = "Manager's Access Control Edit";
	    
    	HttpSession CurrentSession = request.getSession();
	    out.println(TimeCardUtilities.TCTitleSubBGColor(title, subtitle, TimeCardUtilities.BACKGROUND_COLOR_FOR_ADMIN_SCREENS));

    	out.println("<BR><A HREF=\"" + ConnectionPool.WebContextParameters.getURLLinkBase(getServletContext()) 
    		+ "TimeCardSystem.AdminMain\">Return to main menu</A><BR><BR>");
        out.println ("<FORM ACTION =\"" + ConnectionPool.WebContextParameters.getURLLinkBase(getServletContext()) 
        	+ "TimeCardSystem.ManagerAccessControlSave\">");
        Connection conn = null;
        try {
			conn = clsDatabaseFunctions.getConnectionWithException(
				getServletContext(), 
				(String)CurrentSession.getAttribute(TimeCardUtilities.SESSION_ATTRIBUTE_DB), 
				"MySQL", 
				this.toString() + ".doGet - user: " + (String)CurrentSession.getAttribute(TimeCardUtilities.SESSION_ATTRIBUTE_EID));
		} catch (Exception e2) {
			out.println("<BR><FONT COLOR=RED><B>Error [1411481002] getting data connection: " + e2.getMessage() 
					+ "</B></FONT><BR>");
	    	out.println("</TABLE>");        	
	    	out.println("</FORM>");
	        	
		    out.println("</BODY></HTML>");
		    return;
		}
        
    	if (request.getParameter("ManagerID").compareTo("0") == 0 && 
    		request.getParameter("ManagerName").compareTo("NEW") == 0){
    		//forward the parameters from previous form to the next form.
        	out.println("<INPUT TYPE=HIDDEN NAME=\"OriginalURL\" VALUE=\"" + request.getParameter("OriginalURL") + "\">");
        	String sSQL = TimeCardSQLs.Get_Lone_Manager_List_SQL();
        	try {
				ResultSet rsNotManager = clsDatabaseFunctions.openResultSet(sSQL, conn);
				if (rsNotManager.next()){
					out.println("<TABLE BORDER=2 WIDTH=60%>");
					out.println("<TR><TD><B>New Manager: </B>");
				    	out.println ("<SELECT NAME=\"ManagerID\">" );
				    	out.println ("<OPTION VALUE=" + rsNotManager.getString("sEmployeeID") + ">" + rsNotManager.getString("sEmployeeID") + " - " + rsNotManager.getString("sEmployeeFirstName") + " " + rsNotManager.getString("sEmployeeLastName"));
				    	while (rsNotManager.next()){
				    		//flag that there is multiple entries.
				        	out.println ("<OPTION VALUE=" + rsNotManager.getString("sEmployeeID") + ">" + rsNotManager.getString("sEmployeeID") + " - " + rsNotManager.getString("sEmployeeFirstName") + " " + rsNotManager.getString("sEmployeeLastName"));
				    	}
				        out.println ("</SELECT>");
				    out.println ("</TD></TR>");
				    //get all available departments
				    sSQL = TimeCardSQLs.Get_Department_List_SQL();
				    ResultSet rsDepartments = clsDatabaseFunctions.openResultSet(sSQL, conn);
				    out.println("<TR><TD><TABLE BORDER=0 WIDTH=100%>");
					while (rsDepartments.next()){
				    	out.println("<TR><TD><INPUT TYPE=CHECKBOX NAME=SELECTEDDEPT VALUE=\"" + rsDepartments.getInt("iDeptID") + "\">  " + rsDepartments.getString("sDeptDesc") + "</TD></TR>");
					}
					out.println("</TABLE>");
					rsDepartments.close();
				}else{
					//no one can be promoted to manager.
					out.println("<H3>There is no one available to be added into MANAGERS group.</H3>");
				}
				rsNotManager.close();
			} catch (SQLException e) {
				out.println("<BR><FONT COLOR=RED><B>Error [1411416079] getting list with SQL: " + sSQL + " - " + e.getMessage() 
					+ "</B></FONT><BR>");
			}
    	}else{
        	//forward the parameters from previous form to the next form.
        	out.println("<INPUT TYPE=HIDDEN NAME=\"OriginalURL\" VALUE=\"" + request.getParameter("OriginalURL") + "\">");
        	out.println("<INPUT TYPE=HIDDEN NAME=\"ManagerID\" VALUE=\"" + request.getParameter("ManagerID") + "\">");
        	String sSQL = TimeCardSQLs.Get_Temp_Manager_Access_Control_By_Individual(request.getParameter("ManagerID"));
        	try {
				clsDatabaseFunctions.executeSQL(sSQL, conn);
			} catch (SQLException e1) {
				out.println("<BR><FONT COLOR=RED><B>Error [1411416081] creating temporary table with SQL: " + sSQL + " - " 
					+ e1.getMessage() + "</B></FONT><BR>");
			}
        	try {
				sSQL = TimeCardSQLs.Get_Individual_Manager_Access_Control_Info_SQL();
				ResultSet rsManagerAC = clsDatabaseFunctions.openResultSet(sSQL, conn);
				out.println("<TABLE BORDER=2 WIDTH=60%>");
				out.println("<TR><TD><B>Manager: </B><FONT SIZE=3>" + request.getParameter("ManagerName") + "</FONT></TD></TR>");
				out.println("<TR><TD><TABLE BORDER=0 WIDTH=100%>");
				while (rsManagerAC.next()){
					out.println("<TR><TD><INPUT TYPE=CHECKBOX NAME=SELECTEDDEPT VALUE=\"" + rsManagerAC.getInt("iDeptID") + "\"");
					if (rsManagerAC.getString("iDepartmentID") != null){
						out.println(" CHECKED");
					}
					out.println(">  " + rsManagerAC.getString("sDeptDesc") + "</TD></TR>");
				}
				out.println("</TABLE>");
				rsManagerAC.close();
			} catch (SQLException e) {
				out.println("<BR><FONT COLOR=RED><B>Error [1411416080] getting list with SQL: " + sSQL + " - " + e.getMessage() 
					+ "</B></FONT><BR>");
			}
			sSQL = "DROP TABLE IndividualMAC";
			try {
				clsDatabaseFunctions.executeSQL(sSQL, conn);
			} catch (SQLException e) {
				out.println("<BR><FONT COLOR=RED><B>Error [1411416082] dropping temporary table with SQL: " + sSQL + " - " + e.getMessage()
					+ "</B></FONT><BR>");
			}
    	}
    	clsDatabaseFunctions.freeConnection(getServletContext(), conn, "[1547060116]");
    	out.println ("<TR><TD><INPUT TYPE=\"SUBMIT\" VALUE=\"----Save----\"></TD>");
    	out.println("</TABLE>");        	
    	out.println("</FORM>");
	    out.println("</BODY></HTML>");
	}
}
