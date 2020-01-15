package TimeCardSystem;

import java.io.*;
//import java.util.Date;
import javax.servlet.*;
import javax.servlet.http.*;

import ServletUtilities.clsDatabaseFunctions;

import java.sql.ResultSet;



/* Servlet that insert In-Time records into the the time entry table.*/

public class DepartmentInfoSave extends HttpServlet{

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
	    	int iDid = Integer.parseInt(request.getParameter("Did"));
	    
	    	if (iDid == 0){
	    		//new department, check to see if the supplied ID is taken or not.
		    	String sSQL = TimeCardSQLs.Get_Department_Info_SQL(Integer.parseInt(request.getParameter("DepartmentID")));
		    	ResultSet rsDepartmentID = clsDatabaseFunctions.openResultSet(sSQL, getServletContext(), (String)CurrentSession.getAttribute(TimeCardUtilities.SESSION_ATTRIBUTE_DB));
		    	
	    		if (rsDepartmentID.next()){
	    			//<BR>the department is not valid, don't save.");
		    		out.println ("<BR>");
		        	out.println ("<H4>The department ID is taken. please go back to the previous page and choose another one.</H4><BR><BR>");	    			
	    		}else{
	    			//the selected department id is valid, save it.");
	    			if (Save_Department_Info(iDid,
							   			     Integer.parseInt(request.getParameter("DepartmentID")), 
										     request.getParameter("DepartmentName"), 
										     Double.parseDouble(request.getParameter("DepartmentRate")), 
										     Double.parseDouble(request.getParameter("LateGracePeriodLength")),
										     getServletContext(), 
										     (String)CurrentSession.getAttribute(TimeCardUtilities.SESSION_ATTRIBUTE_DB))){
	    				//saving successful;
	    				out.println ("<BR>");
	    	        	out.println ("<H4>Information saved!!</H4><BR><BR>");
	    	        	out.println("<META http-equiv='Refresh' content='1;URL=" + TCWebContextParameters.getURLLinkBase(getServletContext()) + "TimeCardSystem.DepartmentList'>");
	    	        	//out.println ("<A href=" + ConnectionPool.WebContextParameters.getURLLinkBase(getServletContext()) + "TimeCardSystem.DepartmentList>Click here to return to updated department list.</A>");
					}else{
						//saving failed;
						out.println("<BR><BR>Saving department information failed!!<BR>");
						out.println ("<A href=" + TCWebContextParameters.getURLLinkBase(getServletContext()) + "TimeCardSystem.DepartmentList>Click here to return to department list.</A>");
					}
	    		}
		    	//clsoe resultset
		    	rsDepartmentID.close();
		    	
	    	}else{
	    		//existing department. proceed to save changes.
	    		if (Save_Department_Info(iDid,
						   			   	 Integer.parseInt(request.getParameter("DepartmentID")), 
						   			   	 request.getParameter("DepartmentName"), 
						   			   	 Double.parseDouble(request.getParameter("DepartmentRate")),
						   			   	 Double.parseDouble(request.getParameter("LateGracePeriodLength")), 
						   			   	 getServletContext(), 
						   			   	 (String)CurrentSession.getAttribute(TimeCardUtilities.SESSION_ATTRIBUTE_DB))){
					//saving successful
					out.println ("<BR>");
					out.println ("<H4>Information saved!!</H4><BR><BR>");
    	        	out.println("<META http-equiv='Refresh' content='1;URL=" + TCWebContextParameters.getURLLinkBase(getServletContext()) + "TimeCardSystem.DepartmentList'>");
					//out.println ("<A href=" + ConnectionPool.WebContextParameters.getURLLinkBase(getServletContext()) + "TimeCardSystem.DepartmentList>Click here to return to updated department list.</A>");
				}else{
					//saving failed
					out.println("<BR><BR>Saving department information failed!!<BR>");
					out.println ("<A href=" + TCWebContextParameters.getURLLinkBase(getServletContext()) + "TimeCardSystem.DepartmentList>Click here to return to department list.</A>");
				}
	    	}
	    } catch (Exception ex) {
	        // Most of the time, it is session expired.
	    	out.println("Error: " + ex.getMessage());
	    	//out.println(TimeCardUtilities.Session_Expire_Handling());
	    }
	
	    out.println("</BODY></HTML>");
	}
	
	private static boolean Save_Department_Info(int iSessionDID,
											    int iDepartmentID, 
											    String sDepartmentName,
											    double dDepartmentRate,
											    double dLateGracePeriod,
											    ServletContext context, 
											    String conf
											    ){
		
		String sSQL;
		//	save department info
		try{
	        if (iSessionDID == 0){
	        	sSQL = TimeCardSQLs.Get_Insert_Department_SQL(iDepartmentID, 
											        		  sDepartmentName, 
											        		  dDepartmentRate, 
											        		  dLateGracePeriod);
											
	        }else{
		        sSQL = TimeCardSQLs.Get_Update_Department_SQL(iSessionDID, 
										        		      sDepartmentName, 
										        		      dDepartmentRate,
										        		      dLateGracePeriod);
		        	
	        }      
	        if (clsDatabaseFunctions.executeSQL(sSQL, context, conf)){ 
	        	return true;
	        }else{
	        	return false;
	        }
		}catch (Exception ex) {
	    	System.out.println("<BR><BR>[1579099048] Error in Save_Department_Info!!<BR>");
	    	System.out.println("Exception: " + ex.getMessage() + "<BR>");
	    	return false;
	    }
	}
}