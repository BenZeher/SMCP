package TimeCardSystem;

import java.io.*;
import javax.servlet.*;
import javax.servlet.http.*;

import ServletUtilities.clsDatabaseFunctions;

import java.sql.ResultSet;

public class LeaveAdjustmentTypeSave extends HttpServlet{

	private static final long serialVersionUID = 1L;

	public void doGet(HttpServletRequest request,
				HttpServletResponse response)
				throws ServletException, IOException {
	      
	    response.setContentType("text/html");
		HttpSession CurrentSession = request.getSession();
	    PrintWriter out = response.getWriter();
		
	    String title = "Time Card System";
	    String subtitle = "";
	    
	    out.println(TimeCardUtilities.TCTitleSubBGColor(title, subtitle, TimeCardUtilities.BACKGROUND_COLOR_FOR_ADMIN_SCREENS));
	    
	    try {
	    	int iTid = Integer.parseInt(request.getParameter("Tid"));
	    	String sSQL;
    		
	    	if (iTid == 0){
	    		//new type, check to see if the supplied ID is taken or not.
		    	sSQL = TimeCardSQLs.Get_Leave_Adjustment_Type_Info_SQL(request.getParameter("TypeID"));
		    	ResultSet rsLeaveAdjustmentTypeID = clsDatabaseFunctions.openResultSet(sSQL, getServletContext(), (String)CurrentSession.getAttribute(TimeCardUtilities.SESSION_ATTRIBUTE_DB));
		    	
	    		if (rsLeaveAdjustmentTypeID.next()){
	    			//<BR>the ID is not valid, don't save.");
		    		out.println ("<BR>");
		        	out.println ("<H4>The leave adjustment type ID is taken. please go back to the previous page and choose another one.</H4><BR><BR>");	    			
	    		}else{
	    			//the selected adjustment type id is valid, save it.");
	    			Save_Leave_Adjustment_Type_Info(iTid,
					    Integer.parseInt(request.getParameter("TypeID")), 
				        request.getParameter("TypeTitle"), 
				        request.getParameter("TypeDesc"),
				        getServletContext(), 
				        (String)CurrentSession.getAttribute(TimeCardUtilities.SESSION_ATTRIBUTE_DB)
				    );
	    		}
	    		rsLeaveAdjustmentTypeID.close();
		    	
	    	}else{
	    		//existing department. proceed to save changes.
	    		
		    		Save_Leave_Adjustment_Type_Info(iTid,
					    Integer.parseInt(request.getParameter("TypeID")), 
				        request.getParameter("TypeTitle"), 
				        request.getParameter("TypeDesc"),
				        getServletContext(), 
				        (String)CurrentSession.getAttribute(TimeCardUtilities.SESSION_ATTRIBUTE_DB)
				    );
	    	}
	    } catch (Exception ex) {
	    	out.println("Error [1551905917] updating Leave Adjustment Types: " + ex.getMessage());
		    out.println("</BODY></HTML>");
		    return;
	    }
	
		//saving successful
		out.println ("<BR>");
		out.println ("<H4>Leave Adjustment Type '" + request.getParameter("TypeTitle") + " was saved successfully.</H4><BR><BR>");
    	out.println("<META http-equiv='Refresh' content='1;URL=" + ConnectionPool.WebContextParameters.getURLLinkBase(getServletContext()) + "TimeCardSystem.LeaveAdjustmentTypeList'>");
	    out.println("</BODY></HTML>");
	    return;
	}
	
	private static void Save_Leave_Adjustment_Type_Info(int iSessionTID,
       int iTypeID, 
       String sTypeTitle,
       String sTypeDesc,
       ServletContext context, 
       String conf
	   ) throws Exception{
		
		String sSQL = "";
		
		//	save department info
		try{
	        if (iSessionTID == 0){
	        	sSQL = TimeCardSQLs.Get_Insert_Leave_Adjustment_Type_SQL(iTypeID, 
				     sTypeTitle, 
				     sTypeDesc
				     );
											
	        }else{
		        sSQL = TimeCardSQLs.Get_Update_Leave_Adjustment_Type_SQL(
		        	iSessionTID, 
				     sTypeTitle, 
    		  	     sTypeDesc
				     );
		        	
	        }
	        clsDatabaseFunctions.executeSQLWithException(sSQL, conf, "MySQL", "LeaveAdjustmentTypeSave", context);
	        
		}catch (Exception ex) {
	    	throw new Exception("Error [1551906678] saving Leave Adjustment Type with SQL '" + sSQL + "' - " + ex.getMessage());
	    }
	}
}
