package TimeCardSystem;

import java.io.*;
//import java.util.Date;
import javax.servlet.*;
import javax.servlet.http.*;

import ServletUtilities.clsDatabaseFunctions;

import java.sql.ResultSet;

/* Servlet that insert In-Time records into the the time entry table.*/

public class PayTypeSave extends HttpServlet{

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
	    	int iDid = Integer.parseInt(request.getParameter("Tid"));
	    
	    	if (iDid == 0){
	    		//new type, check to see if the supplied ID is taken or not.
		    	String sSQL = TimeCardSQLs.Get_Pay_Type_Info_SQL(request.getParameter("TypeID"));
		    	ResultSet rsPayTypeID = clsDatabaseFunctions.openResultSet(sSQL, getServletContext(), (String)CurrentSession.getAttribute(TimeCardUtilities.SESSION_ATTRIBUTE_DB));
		    	
	    		if (rsPayTypeID.next()){
	    			//<BR>the ID is not valid, don't save.");
		    		out.println ("<BR>");
		        	out.println ("<H4>The pay type ID is taken. please go back to the previous page and choose another one.</H4><BR><BR>");	    			
	    		}else{
	    			//the selected department id is valid, save it.");
	    			if (Save_Pay_Type_Info(iDid,
						   			       Integer.parseInt(request.getParameter("TypeID")), 
									       request.getParameter("TypeTitle"), 
									       request.getParameter("TypeDesc"), 
									       getServletContext(), 
									       (String)CurrentSession.getAttribute(TimeCardUtilities.SESSION_ATTRIBUTE_DB))){
	    				//saving successful;
	    				out.println ("<BR>");
	    	        	out.println ("<H4>Information saved!!</H4><BR><BR>");
	    	        	out.println("<META http-equiv='Refresh' content='1;URL=" + ConnectionPool.WebContextParameters.getURLLinkBase(getServletContext()) + "TimeCardSystem.PayTypeList'>");
	    	        	//out.println ("<A href=" + ConnectionPool.WebContextParameters.getURLLinkBase(getServletContext()) + "TimeCardSystem.DepartmentList>Click here to return to updated department list.</A>");
					}else{
						//saving failed;
						out.println("<BR><BR>Saving pay type information failed!!<BR>");
						out.println ("<A href=" + ConnectionPool.WebContextParameters.getURLLinkBase(getServletContext()) + "TimeCardSystem.PayTypeList>Click here to return to pay type list.</A>");
					}
	    		}
		    	//clsoe resultset
	    		rsPayTypeID.close();
		    	
	    	}else{
	    		//existing department. proceed to save changes.
	    		if (Save_Pay_Type_Info(iDid,
				   			     	   Integer.parseInt(request.getParameter("TypeID")), 
				   			     	   request.getParameter("TypeTitle"), 
				   			     	   request.getParameter("TypeDesc"), 
				   			     	   getServletContext(), 
				   			     	   (String)CurrentSession.getAttribute(TimeCardUtilities.SESSION_ATTRIBUTE_DB))){
					//saving successful
					out.println ("<BR>");
					out.println ("<H4>Information saved!!</H4><BR><BR>");
    	        	out.println("<META http-equiv='Refresh' content='1;URL=" + ConnectionPool.WebContextParameters.getURLLinkBase(getServletContext()) + "TimeCardSystem.PayTypeList'>");
					//out.println ("<A href=" + ConnectionPool.WebContextParameters.getURLLinkBase(getServletContext()) + "TimeCardSystem.DepartmentList>Click here to return to updated department list.</A>");
				}else{
					//saving failed
					out.println("<BR><BR>Saving pay type information failed!!<BR>");
					out.println ("<A href=" + ConnectionPool.WebContextParameters.getURLLinkBase(getServletContext()) + "TimeCardSystem.PayTypeList>Click here to return to pay type list.</A>");
				}
	    	}
	    } catch (Exception ex) {
	        // Most of the time, it is session expired.
	    	out.println("Error: " + ex.getMessage());
	    	//out.println(TimeCardUtilities.Session_Expire_Handling());
	    }
	
	    out.println("</BODY></HTML>");
	}
	
	private static boolean Save_Pay_Type_Info(int iSessionTID,
										      int iTypeID, 
										      String sTypeTitle,
										      String sTypeDesc,
										      ServletContext context, 
										      String conf
									    	  ){
		
		String sSQL;
		//	save department info
		try{
	        if (iSessionTID == 0){
	        	sSQL = TimeCardSQLs.Get_Insert_Pay_Type_SQL(iTypeID, 
        												    sTypeTitle, 
        												    sTypeDesc);
											
	        }else{
		        sSQL = TimeCardSQLs.Get_Update_Pay_Type_SQL(iSessionTID, 
        												    sTypeTitle, 
									        		  	    sTypeDesc);
		        	
	        }      
	        if (clsDatabaseFunctions.executeSQL(sSQL, context, conf)){ 
	        	return true;
	        }else{
	        	return false;
	        }
		}catch (Exception ex) {
	    	System.out.println("<BR><BR>Error in Save_Pay_Type_Info!!<BR>");
	    	System.out.println("Exception: " + ex.getMessage() + "<BR>");
	    	return false;
	    }
	}
}