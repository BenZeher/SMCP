package TimeCardSystem;

import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;

import ServletUtilities.clsDatabaseFunctions;

/** Servlet that inserts In-Time records into the the time entry table.*/
public class PeriodTotalSave extends HttpServlet{

	private static final long serialVersionUID = 1L;

	public void doGet(HttpServletRequest request,
				HttpServletResponse response)
				throws ServletException, IOException {
   
	    response.setContentType("text/html");
	    PrintWriter out = response.getWriter();
	    HttpSession CurrentSession = request.getSession();
		
	    String title = "Time Card System";
	    
	    try {
	    	/*
	    	+------------------+--------------+------+-----+---------------------+----------------+
	    	| Field            | Type         | Null | Key | Default             | Extra          |
	    	+------------------+--------------+------+-----+---------------------+----------------+
	    	| id               | mediumint(9) |      | PRI | NULL                | auto_increment |
	    	| sEmployeeID      | varchar(9)   | YES  |     |                     |                |
	    	| datPeriodEndDate | datetime     | YES  |     | 0000-00-00 00:00:00 |                |
	    	| dPeriodTotal     | double       | YES  |     | 0                   |                |
	    	| dPeriodRegular   | double       | YES  |     | 0                   |                |
	    	| dPeriodOverTime  | double       | YES  |     | 0                   |                |
	    	| dPeriodDouble    | double       | YES  |     | 0                   |                |
	    	| sCreatorID       | varchar(9)   | YES  |     |                     |                |
	    	+------------------+--------------+------+-----+---------------------+----------------+
	    	*/

	    	double dAT;
	    	double dAR;
	    	double dAO;
	    	double dAD;
	    	double dAL;

	    	if (request.getParameter("AdjustedTotal").length() != 0){
	    		dAT = Double.parseDouble(request.getParameter("AdjustedTotal"));
	    	}else{
	    		dAT = 0;
	    	}
	    	
	    	if (request.getParameter("AdjustedRegular").length() != 0){
	    		dAR = Double.parseDouble(request.getParameter("AdjustedRegular"));
	    	}else{
	    		dAR = 0;
	    	}
	    	
	    	if (request.getParameter("AdjustedOver").length() != 0){
	    		dAO = Double.parseDouble(request.getParameter("AdjustedOver"));
	    	}else{
	    		dAO = 0;
	    	}
	    	
	    	if (request.getParameter("AdjustedDouble").length() != 0){
	    		dAD = Double.parseDouble(request.getParameter("AdjustedDouble"));
	    	}else{
	    		dAD = 0;
	    	}
	    	
	    	if (request.getParameter("AdjustedLeave").length() != 0){
	    		dAL = Double.parseDouble(request.getParameter("AdjustedLeave"));
	    	}else{
	    		dAL = 0;
	    	}
	    	
	    	if (TimeCardUtilities.RoundHalfUp(dAR + dAO + dAD + dAL, 2) != dAT){
	    		out.println ("<HTML>\n" + 
				       			 //"<META http-equiv='Refresh' content='2;URL=" + request.getParameter("OriginalURL") + "'>" +
				            	 "<HEAD><TITLE>" + title + "</TITLE></HEAD>\n" +
				            	 "<BODY BGCOLOR=\"" + TimeCardUtilities.BACKGROUND_COLOR_FOR_ADMIN_SCREENS + "\">\n" +
				            	 "<H1>" + title + "</H1>\n");
		   		out.println("<BR><H2>The sum of regular time, over time, double time and leave time doesn't match total time.</H2><BR>");
		   		out.println("<H2>Please check you input and try again.</H2><BR>");
		   		out.println("<TABLE BORDER=1 WIDTH=60%>" +
		   					"<TR><TD WIDTH=50%><H4>Regular Time</H4></TD><TD WIDTH=50%>" + dAR + "</TD></TR>" +
		   					"<TR><TD WIDTH=50%><H4>Over Time</H4></TD><TD WIDTH=50%>" + dAO + "</TD></TR>" +
		   					"<TR><TD WIDTH=50%><H4>Double Time</H4></TD><TD WIDTH=50%>" + dAD + "</TD></TR>" +
		   					"<TR><TD WIDTH=50%><H4>Leave Time</H4></TD><TD WIDTH=50%>" + dAL + "</TD></TR>" +
		   					"<TR><TD WIDTH=50%><H4>Calculated Time</H4></TD><TD WIDTH=50%>" + TimeCardUtilities.RoundHalfUp(dAR + dAO + dAD + dAL, 2) + "</TD></TR>" +
		   					"<TR><TD WIDTH=50%><H4>Entered Total Time</H4></TD><TD WIDTH=50%>" + dAT + "</TD></TR>" +
		   					"</TABLE><BR>");
		   		out.println("<A HREF=\"" + TimeCardUtilities.URLDecode(request.getParameter("OriginalURL")) + "\"> Click here to go back</A>");
	    	}else{
		    	String sSQL = TimeCardSQLs.Get_Update_Time_Total_SQL(Integer.parseInt(request.getParameter("id")), 
		    														 dAR,
		    														 dAO,
		    														 dAD,
		    														 dAL, 
		    														 dAT);
		    	
	    		if (clsDatabaseFunctions.executeSQL(sSQL, getServletContext(), (String)CurrentSession.getAttribute(TimeCardUtilities.SESSION_ATTRIBUTE_DB))){
	    	    	
	    			out.println ("<HTML>\n" + 
			                	 "<HEAD><TITLE>" + title + "</TITLE></HEAD>\n" +
			                	 "<BODY BGCOLOR=\"#" + TimeCardUtilities.BACKGROUND_COLOR_FOR_ADMIN_SCREENS + "\">\n" +
			                	 "<H1>" + title + "</H1>\n" + 
			                	 //"Original URL= " + request.getParameter("OriginalURL")); 
			                	 "<META http-equiv='Refresh' content='0;URL=" + TimeCardUtilities.URLDecode(request.getParameter("OriginalURL")) + "'>");
	
		    	}else{
		    		out.println ("<HTML>\n" + 
				       			 //"<META http-equiv='Refresh' content='2;URL=" + request.getParameter("OriginalURL") + "'>" +
				            	 "<HEAD><TITLE>" + title + "</TITLE></HEAD>\n" +
				            	 "<BODY BGCOLOR=\"#" + TimeCardUtilities.BACKGROUND_COLOR_FOR_ADMIN_SCREENS + "\">\n" +
				            	 "<H1>" + title + "</H1>\n");
		    		out.println("<BR><H2>Error when editing period total times.</H2><BR>");
		    		out.println("<A HREF=\"" + TimeCardUtilities.URLDecode(request.getParameter("OriginalURL")) + "\"> Click here to go back to Time total list</A>");
			    	out.println ("<BR>Failed SQL statement: " + sSQL + "<BR>");
			    	
		    	}
	    	}
	    } catch (Exception ex) {
	        // handle any errors
	    	out.println("<BR><BR>Error!!<BR>");
	        out.println("SQLException: " + ex.getMessage() + "<BR>");
	    }
	
	    out.println("</BODY></HTML>");
	}
}