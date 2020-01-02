package TimeCardSystem;

import java.io.*;
//import java.util.Date;
import javax.servlet.*;
import javax.servlet.http.*;

import ServletUtilities.clsDatabaseFunctions;

import java.sql.*;

/** Servlet that insert In-Time records into the the time entry table.*/

public class SpecialNoteSave extends HttpServlet{

	private static final long serialVersionUID = 1L;

	public void doGet(HttpServletRequest request,
				HttpServletResponse response)
				throws ServletException, IOException {
	    
	    response.setContentType("text/html");
	    PrintWriter out = response.getWriter();
	    HttpSession CurrentSession = request.getSession();
		
	    String title = "Time Card System";
	    String subtitle = "";
	    String backgroundcolor;
	    if (request.getParameter("IsAdmin") == null){
	    	backgroundcolor = TimeCardUtilities.BACKGROUND_COLOR_FOR_USER_SCREENS;
	    }else{
	    	backgroundcolor = TimeCardUtilities.BACKGROUND_COLOR_FOR_ADMIN_SCREENS;
	    }
	    out.println(TimeCardUtilities.TCTitleSubBGColor(title, subtitle, backgroundcolor));

	    if (request.getParameter("IsAdmin") == null){
	    	out.println("<META http-equiv='Refresh' content='2;URL=" 
	    			+ TCWebContextParameters.getURLLinkBase(getServletContext())
	    			+ MainLogin.CLASS_NAME
	    			+ "?db=" + (String)CurrentSession.getAttribute(TimeCardUtilities.SESSION_ATTRIBUTE_DB) + "'>");
	    }else{
	    	out.println("<META http-equiv='Refresh' content='1;URL=" + request.getParameter("OriginalURL") + "#" + request.getParameter("EmployeeID") + "'>");
	    }
	  
	    try {
/**	        Notes;
	        +-------------+--------------+------+-----+---------+----------------+
			| Field       | Type         | Null | Key | Default | Extra          |
			+-------------+--------------+------+-----+---------+----------------+
			| id          | mediumint(9) |      | PRI | NULL    | auto_increment |
			| iLinkID     | mediumint(9) |      |     | 0       |                |
			| mNote       | text         | YES  |     | NULL    |                |
			| iNoteTypeID | int(11)      |      |     | 0       |                |
			+-------------+--------------+------+-----+---------+----------------+
*/ 
	    	
//	    	for each kind of special notes if there is any, pass them on to the next form
        	String sSQL = TimeCardSQLs.Retrieve_Special_Note_Types_SQL();
        	//out.println("<BR>" + sSQL + "<BR>");

        	ResultSet rsSpecialTypes = clsDatabaseFunctions.openResultSet(sSQL, getServletContext(), (String)CurrentSession.getAttribute(TimeCardUtilities.SESSION_ATTRIBUTE_DB));

        	//for everyone of special types, check to see if there is any notes.
        	while (rsSpecialTypes.next()){
        		if (request.getParameter(String.valueOf(rsSpecialTypes.getInt("iTypeID"))) != null){
        			//out.println("<BR>" + rsSpecialTypes.getInt("iTypeID") + "<BR>");
        			if (request.getParameter(String.valueOf(rsSpecialTypes.getInt("iTypeID"))).trim().compareTo("") != 0){
        				//check to see if there is already this kind of note for this time entry or not
        			    sSQL = TimeCardSQLs.Get_Existing_Special_Notes_SQL(Integer.parseInt(request.getParameter("LinkID")), 
        			    												   rsSpecialTypes.getInt("iTypeID"));
        	        	//out.println("<BR>" + sSQL + "<BR>");
        			    ResultSet rsExistingNotes = clsDatabaseFunctions.openResultSet(sSQL, getServletContext(), (String)CurrentSession.getAttribute(TimeCardUtilities.SESSION_ATTRIBUTE_DB));
        			    if (rsExistingNotes.next()){
        			    	sSQL = TimeCardSQLs.Get_Update_Special_Note_SQL(Integer.parseInt(request.getParameter("LinkID")),
																			rsSpecialTypes.getInt("iTypeID"), 
																			request.getParameter(String.valueOf(rsSpecialTypes.getInt("iTypeID"))));
        			    }else{
        			    	sSQL = TimeCardSQLs.Get_Insert_Special_Note_SQL(Integer.parseInt(request.getParameter("LinkID")),
        																	rsSpecialTypes.getInt("iTypeID"), 
        																	request.getParameter(String.valueOf(rsSpecialTypes.getInt("iTypeID"))));
        			    }
        			    rsExistingNotes.close();
        			    
        				//out.println ("<BR>sSQL = " + sSQL);			
        				clsDatabaseFunctions.executeSQL(sSQL, getServletContext(), (String)CurrentSession.getAttribute(TimeCardUtilities.SESSION_ATTRIBUTE_DB));
        			}else{
        				//remove empty notes
        				sSQL = TimeCardSQLs.Remove_Special_Note_SQL(Integer.parseInt(request.getParameter("LinkID")), 
								   									rsSpecialTypes.getInt("iTypeID"));
        				clsDatabaseFunctions.executeSQL(sSQL, getServletContext(), (String)CurrentSession.getAttribute(TimeCardUtilities.SESSION_ATTRIBUTE_DB));
        			}
        		}
        	}
    		out.println ("<H2>Notes saved.</H2");
    		
    		rsSpecialTypes.close();
    		
	    } catch (Exception ex) {
	        // Most of the time, it is session expired.
	    	out.println(TimeCardUtilities.Session_Expire_Handling(getServletContext()));
	    }
	    out.println("</BODY></HTML>");
	}
}