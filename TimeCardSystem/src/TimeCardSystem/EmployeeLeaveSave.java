package TimeCardSystem;

import java.io.*;
//import java.util.Date;
import javax.servlet.*;
import javax.servlet.http.*;

import ServletUtilities.clsDatabaseFunctions;

/** Servlet that inserts In-Time records into the the time entry table.*/

public class EmployeeLeaveSave extends HttpServlet{

	private static final long serialVersionUID = 1L;

	public void doGet(HttpServletRequest request,
				HttpServletResponse response)
				throws ServletException, IOException {
   
	    response.setContentType("text/html");
	    PrintWriter out = response.getWriter();
	    HttpSession CurrentSession = request.getSession();
	    String sDBID = (String) CurrentSession.getAttribute(TimeCardUtilities.SESSION_ATTRIBUTE_DB);
	    String sUserID = (String) CurrentSession.getAttribute(TimeCardUtilities.SESSION_ATTRIBUTE_EID);
	    String sEmployeeID = request.getParameter("EmployeeID");
		
	    String title = "Time Card System";
	    
	    try {
	    	int iType = Integer.parseInt(request.getParameter("Type"));
	    	int iSpecialAdj = 0;

	    	//construct dates
	    	String sStartDateString = request.getParameter("SelectedStartYear") + "-" +
				  request.getParameter("SelectedStartMonth") + "-" +
				  request.getParameter("SelectedStartDay");
	    	String sEndDateString = request.getParameter("SelectedEndYear") + "-" +
				request.getParameter("SelectedEndMonth") + "-" +
				request.getParameter("SelectedEndDay");
			
	    	if (request.getParameter("SpecialAdjustmentCheck") != null){
	    		//Early Start
	    		iSpecialAdj = 1;
			}
	    	
	    	String sSQL;
			
	    	boolean isNewEntry = false;
	    	if (Integer.parseInt(request.getParameter("id")) < 0){
	    		//INSERT NEW LEAVE ADJUSTMENT
	    		isNewEntry = true;
	    		sSQL = TimeCardSQLs.Get_Insert_Leave_Adjustment_SQL(sEmployeeID, 
				    iType,
				    sStartDateString,
				    sEndDateString,
				    Double.parseDouble(request.getParameter("Duration")),
				    iSpecialAdj,
				    request.getParameter("Note"));
	    	}else{
	    		//UPDATE OLD LEAVE ADJUSTMENT
		    		sSQL = TimeCardSQLs.Get_Update_Leave_Adjustment_SQL(Integer.parseInt(request.getParameter("id")),
		    			sEmployeeID,
						iType,
						sStartDateString, 
						sEndDateString,
						Double.parseDouble(request.getParameter("Duration")), 
						iSpecialAdj,
						request.getParameter("Note"));
	    	}
    		if (clsDatabaseFunctions.executeSQL(sSQL, getServletContext(), (String)CurrentSession.getAttribute(TimeCardUtilities.SESSION_ATTRIBUTE_DB))){
	    		out.println ("<HTML>\n" + 
            	 "<HEAD><TITLE>" + title + "</TITLE></HEAD>\n" +
            	 "<BODY BGCOLOR=\"" + TimeCardUtilities.BACKGROUND_COLOR_FOR_ADMIN_SCREENS + "\">\n" +
            	 "<H1>" + title + "</H1>\n" + 
            	 "<META http-equiv='Refresh' content='1;URL=" + request.getParameter("OriginalURL") + "#" + sEmployeeID + "'>");
	    		//out.println ("<BR><H2>You've modified the time entry sucessfully.</H2>");
	    	}else{
	    		out.println ("<HTML>\n" + 
       			 //"<META http-equiv='Refresh' content='2;URL=" + request.getParameter("OriginalURL") + "'>" +
            	 "<HEAD><TITLE>" + title + "</TITLE></HEAD>\n" +
            	 "<BODY BGCOLOR=\"" + TimeCardUtilities.BACKGROUND_COLOR_FOR_ADMIN_SCREENS + "\">\n" +
            	 "<H1>" + title + "</H1>\n");
	    		out.println("<BR><H2>Failed in saving leave adjustment process.</H2><BR>");
	    	}
    		
    		String sProcess = "Updated current leave entry";
    		if (isNewEntry){
    			sProcess = "Inserted new leave entry";
    		}
    		TCLogEntry log = new TCLogEntry(sDBID, getServletContext());
    		log.writeEntry(
    			sUserID, 
    			TCLogEntry.LOG_OPERATION_ADMIN_SAVED_LEAVE_ADJUSTMENT, 
    			sProcess + " for employee '" + sEmployeeID + "',"
    				+ " start date: '" + sStartDateString + "',"
    				+ " end date: '" + sEndDateString + "',"
    				+ " duration: '" + request.getParameter("Duration") + "',"
    				+ " note: '" + request.getParameter("Note") + "'." 
    			, 
    			sSQL, 
    			"[1518037319]"
    		);
    		
	    } catch (Exception ex) {
	        // handle any errors
	    	out.println("<BR><BR>Error in EmployeeLeaveSave!!<BR>");
	        out.println("Exception: " + ex.getMessage() + "<BR>");
	    }
	
	    out.println("</BODY></HTML>");
	}
}