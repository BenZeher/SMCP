package TimeCardSystem;

import java.io.*;
//import java.util.Date;
import javax.servlet.*;
import javax.servlet.http.*;

import ServletUtilities.clsDatabaseFunctions;

import java.sql.ResultSet;

/* Servlet that insert In-Time records into the the time entry table.*/

public class EmployeeStatusSave extends HttpServlet{

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
	    	int iSid = Integer.parseInt(request.getParameter("Sid"));
	    
	    	if (iSid == 0){
	    		//new type, check to see if the supplied ID is taken or not.
		    	String sSQL = TimeCardSQLs.Get_Employee_Status_Info_SQL(request.getParameter("StatusID"));
		    	ResultSet rsStatusID = clsDatabaseFunctions.openResultSet(sSQL, getServletContext(), (String)CurrentSession.getAttribute(TimeCardUtilities.SESSION_ATTRIBUTE_DB));
		    	
	    		if (rsStatusID.next()){
	    			//<BR>the ID is not valid, don't save.");
		    		out.println ("<BR>");
		        	out.println ("<H4>The time entry type ID is taken. please go back to the previous page and choose another one.</H4><BR><BR>");	    			
	    		}else{
	    			//the selected department id is valid, save it.");
	    			if (Save_Employee_Status(iSid,
								   			 Integer.parseInt(request.getParameter("StatusID")), 
											 request.getParameter("StatusTitle"), 
											 request.getParameter("StatusDesc"), 
											 getServletContext(), 
											 (String)CurrentSession.getAttribute(TimeCardUtilities.SESSION_ATTRIBUTE_DB))){
	    				//saving successful;
	    				out.println ("<BR>");
	    	        	out.println ("<H4>Information saved!!</H4><BR><BR>");
	    	        	out.println("<META http-equiv='Refresh' content='1;URL=" + TCWebContextParameters.getURLLinkBase(getServletContext()) + "TimeCardSystem.EmployeeStatusList'>");
	    	        	//out.println ("<A href=" + TCWebContextParameters.getURLLinkBase(getServletContext()) + "TimeCardSystem.DepartmentList>Click here to return to updated department list.</A>");
					}else{
						//saving failed;
						out.println("<BR><BR>Saving employee status information failed!!<BR>");
						out.println ("<A href=" + TCWebContextParameters.getURLLinkBase(getServletContext()) + "TimeCardSystem.EmployeeStatusList>Click here to return to employee status list.</A>");
					}
	    		}
		    	//clsoe resultset
	    		rsStatusID.close();
		    	
	    	}else{
	    		//existing department. proceed to save changes.
	    		if (Save_Employee_Status(iSid,
						   			     Integer.parseInt(request.getParameter("TypeID")), 
						   			     request.getParameter("TypeTitle"), 
						   			     request.getParameter("TypeDesc"), 
						   			     getServletContext(), 
						   			     (String)CurrentSession.getAttribute(TimeCardUtilities.SESSION_ATTRIBUTE_DB))){
					//saving successful
					out.println ("<BR>");
					out.println ("<H4>Information saved!!</H4><BR><BR>");
    	        	out.println("<META http-equiv='Refresh' content='1;URL=" + TCWebContextParameters.getURLLinkBase(getServletContext()) + "TimeCardSystem.TimeEntryTypeList'>");
					//out.println ("<A href=" + TCWebContextParameters.getURLLinkBase(getServletContext()) + "TimeCardSystem.DepartmentList>Click here to return to updated department list.</A>");
				}else{
					//saving failed
					out.println("<BR><BR>Saving time entry type information failed!!<BR>");
					out.println ("<A href=" + TCWebContextParameters.getURLLinkBase(getServletContext()) + "TimeCardSystem.TimeEntryTypeList>Click here to return to time entry type list.</A>");
				}
	    	}
	    } catch (Exception ex) {
	        // Most of the time, it is session expired.
	    	out.println("Error: " + ex.getMessage());
	    	//out.println(TimeCardUtilities.Session_Expire_Handling());
	    }
	
	    out.println("</BODY></HTML>");
	}
	
	private static boolean Save_Employee_Status(int iSessionSID,
											    int iStatusID, 
											    String sStatusTitle,
											    String sStatusDesc,
											    ServletContext context, 
											    String conf
										    	){
		
		String sSQL;
		//	save department info
		try{
	        if (iSessionSID == 0){
	        	sSQL = TimeCardSQLs.Get_Insert_Employee_Status_SQL(iStatusID, 
		        												   sStatusTitle, 
		        												   sStatusDesc);
											
	        }else{
		        sSQL = TimeCardSQLs.Get_Update_Employee_Status_SQL(iSessionSID, 
		        												   sStatusTitle, 
											        		  	   sStatusDesc);
		        	
	        }      
	        if (clsDatabaseFunctions.executeSQL(sSQL, context, conf)){ 
	        	return true;
	        }else{
	        	return false;
	        }
		}catch (Exception ex) {
	    	System.out.println("<BR><BR>[1579100890] Error in Save_Time_Entry_Type_Info!!<BR>");
	    	System.out.println("Exception: " + ex.getMessage() + "<BR>");
	    	return false;
	    }
	}
}