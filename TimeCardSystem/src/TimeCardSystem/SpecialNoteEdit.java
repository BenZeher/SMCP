package TimeCardSystem;

import java.io.*;
//import java.util.Date;
import javax.servlet.*;
import javax.servlet.http.*;

import ServletUtilities.clsDatabaseFunctions;

import java.sql.*;

/** Servlet that insert In-Time records into the the time entry table.*/

public class SpecialNoteEdit extends HttpServlet{

	private static final long serialVersionUID = 1L;

	public void doGet(HttpServletRequest request,
				HttpServletResponse response)
				throws ServletException, IOException {
	    
/**		String sPinNumber = TimeCardUtilities.filter(request.getParameter(TimeCardUtilities.REQUEST_PARAMETER_PINNUMBER));
	    Cookie TimeCardCookie = new Cookie("TimeCard", sPinNumber);
	    TimeCardCookie.setMaxAge(1440); //the cookie will be valid for one day
	    response.addCookie(TimeCardCookie);
*/    
	    response.setContentType("text/html");
		HttpSession CurrentSession = request.getSession();
	    PrintWriter out = response.getWriter();
		
	    String title = "Time Card System";
	    String subtitle = "Enter special note";
	    String backgroundcolor;
	    if (request.getParameter("IsAdmin") == null){
	    	backgroundcolor = TimeCardUtilities.BACKGROUND_COLOR_FOR_USER_SCREENS;
	    }else{
	    	backgroundcolor = TimeCardUtilities.BACKGROUND_COLOR_FOR_ADMIN_SCREENS;
	    }
	    out.println(TimeCardUtilities.TCTitleSubBGColor(title, subtitle, backgroundcolor));
	    
	    try {
	        	          	
	    	//get all the special note types and lay them out.
	        String sSQL = TimeCardSQLs.Retrieve_Special_Note_Types_SQL();
	        ResultSet rsNoteTypes= clsDatabaseFunctions.openResultSet(
	        		sSQL, 
	        		getServletContext(), 
	        		(String)CurrentSession.getAttribute(TimeCardUtilities.SESSION_ATTRIBUTE_DB));	

        	out.println ("<FORM ACTION =\"" + TCWebContextParameters.getURLLinkBase(getServletContext()) + "TimeCardSystem.SpecialNoteSave\">");
	        out.println("<INPUT TYPE=HIDDEN NAME=LinkID VALUE=\"" + request.getParameter("LinkID") + "\">");
		    if (request.getParameter("IsAdmin") != null){
		        out.println("<INPUT TYPE=HIDDEN NAME=IsAdmin VALUE=\"" + request.getParameter("IsAdmin") + "\">");
		        out.println("<INPUT TYPE=HIDDEN NAME=EmployeeID VALUE=\"" + request.getParameter("EmployeeID") + "\">");
		        out.println("<INPUT TYPE=HIDDEN NAME=OriginalURL VALUE=\"" + request.getParameter("OriginalURL") + "\">");
		    }
	        
		    //get available notes
		    sSQL = TimeCardSQLs.Get_Existing_Special_Notes_SQL(Integer.parseInt(request.getParameter("LinkID")));
		    ResultSet rsExistingNotes = clsDatabaseFunctions.openResultSet(sSQL, getServletContext(), (String)CurrentSession.getAttribute(TimeCardUtilities.SESSION_ATTRIBUTE_DB));
		    String sTemp="";
			while (rsNoteTypes.next()){
				if (rsNoteTypes.getInt("iTypeID") != 0){
			    	out.println("<B>" + rsNoteTypes.getString("sTypeTitle") + ": </B>("+ rsNoteTypes.getString("sTypeDesc") + ")<BR>");
			    	out.print("<TEXTAREA NAME=\"" + rsNoteTypes.getString("iTypeID") + "\" ROWS=\"8\" COLS=\"30\">");
			    	while (rsExistingNotes.next()){
			    		if (rsExistingNotes.getInt("iNoteTypeID") == rsNoteTypes.getInt("iTypeID")){
			    			sTemp = sTemp + rsExistingNotes.getString("mNote");
						}
			    	}
			    	out.print(sTemp + "</TEXTAREA><BR><BR>");
			    	rsExistingNotes.beforeFirst();
			    	sTemp = "";
				}
			}
			out.println("<INPUT TYPE=\"SUBMIT\" VALUE=\"----Save----\"></FORM><BR>");
			
			rsExistingNotes.close();
			rsNoteTypes.close();
			
			
	    } catch (Exception ex) {
	        // Most of the time, it is session expired.
	    	out.println("Exception: " + ex.getMessage() + "<BR>");
	    	out.println(TimeCardUtilities.Session_Expire_Handling(getServletContext()));
	    }
	
	    out.println("</BODY></HTML>");
	}
}