package TimeCardSystem;

import java.io.*;
import java.util.Enumeration;
import javax.servlet.*;
import javax.servlet.http.*;

import ServletUtilities.clsDatabaseFunctions;

import java.sql.ResultSet;
import TCSDataDefinition.*;

public class EmployeeConfidentialSave extends HttpServlet{

	private static final long serialVersionUID = 1L;

	public void doGet(HttpServletRequest request,
				HttpServletResponse response)
				throws ServletException, IOException {
	      
	    response.setContentType("text/html");
		HttpSession CurrentSession = request.getSession();
	    PrintWriter out = response.getWriter();
		
	    String title = "Time Card System";
	    String subtitle = "";
	    String bar = "Time Card System - " + CurrentSession.getAttribute(TimeCardUtilities.SESSION_ATTRIBUTE_COMPANYNAME).toString();
	    out.println(TimeCardUtilities.TCBarTitleSubBGColor(bar, title, subtitle, TimeCardUtilities.BACKGROUND_COLOR_FOR_ADMIN_SCREENS));
	    //out.println("Saving information for " + request.getParameter("EmployeeID") + "<BR>");
	    try {
	    	//first, check for the pin number availability before we start to save changes
	    	boolean bPinAvailable = false;
	    	if (request.getParameter("EmployeePinNumber").trim().length() != 0){
				String sSQL = TimeCardSQLs.Get_Employee_Info_By_Pin_SQL(request.getParameter("EmployeePinNumber"));
				ResultSet rs = clsDatabaseFunctions.openResultSet(sSQL, getServletContext(), (String)CurrentSession.getAttribute(TimeCardUtilities.SESSION_ATTRIBUTE_DB));
				if (rs.next()){
					if (rs.getString(Employees.sEmployeeID).compareTo(request.getParameter("EmployeeID")) == 0){
						bPinAvailable = true;
					}
				}else{
					bPinAvailable = true;
				}
	    	}else{
	    		//no pin number, which is allowed. save the rest of information.
	    		bPinAvailable = true;
	    	}
	    	if (bPinAvailable){
    	    	String sSQL = Get_Update_Employee_Confidential_Info_SQL(request);
    			
    			if (clsDatabaseFunctions.executeSQL(sSQL, getServletContext(), (String)CurrentSession.getAttribute(TimeCardUtilities.SESSION_ATTRIBUTE_DB))){
    				out.println("<BR>");
    	        	out.println("<H4>Information saved!!</H4><BR><BR>");
    	        	out.println("<META http-equiv='Refresh' content='1;URL=" + TimeCardUtilities.URLDecode(request.getParameter("OriginalURL")) + "'>");
    			}else{
    				out.println("<BR><BR>Saving employee information failed!!<BR>");
    		    	out.println ("<A href=\"" + TimeCardUtilities.URLDecode(request.getParameter("OriginalURL")) + "\">Click here to return to employee list.</A>");
    			}
    		}else{
    			//different employees. as employee ID is fixed, this must mean that the 
    			//user selected a new pin number for someone but collide with an existing
    			//employee, which is not allowed.
    			out.println ("<BR>");
	        	out.println ("<H4>The pin number is taken. please go back to the previous page and choose another one.</H4><BR><BR>");
    		}
	    } catch (Exception ex) {
	        // Most of the time, it is session expired.
	    	out.println("Exception : " + ex.getMessage());
	    }
	
	    out.println("</BODY></HTML>");
	}
	
	private static String Get_Update_Employee_Confidential_Info_SQL(HttpServletRequest request){

		//calculate the pay type sum and employee status sum
		Enumeration<String> paramNames = (Enumeration<String>)request.getParameterNames();
		int iPayTypesSum = 0;
		int iStatusSum = 0;
		while(paramNames.hasMoreElements()) {
			String s = paramNames.nextElement().toString();
			try{
				int iSwitch = Integer.parseInt(s.substring(1, 2));
    			switch (iSwitch) {
	                case 1: try{
                				if (iPayTypesSum >= 0){
                					iPayTypesSum = iPayTypesSum + (int)Math.pow(2, Double.parseDouble(s.substring(3)));
                				}
                				break;
                			}catch(NumberFormatException ex){
                				//all department
                				iPayTypesSum = -1;
                				break;
                			}
	                case 2: try{
		        				if (iStatusSum >= 0){
		        					iStatusSum = iStatusSum + (int)Math.pow(2, Double.parseDouble(s.substring(3)));
		        				}
		        				break;
		        			}catch(NumberFormatException ex){
		        				//all department
		        				iStatusSum = -1;
		        				break;
		        			}
    			}
			}catch(Exception ex){
				//System.out.println("Not a parameter we care about at this point.");
			}
		}
		
		return TimeCardSQLs.Get_Update_Employee_Confidential_Info_SQL(request.getParameter("EmployeeID"), 
																	  TimeCardUtilities.ConstructTimeString(request.getParameter("SelectedHour"),
																			  								request.getParameter("SelectedMinute"),
																			  								request.getParameter("SelectedAMPM")), 
																	  request.getParameter("EmployeePinNumber").trim(), 
																	  request.getParameter("EmployeeSSN"),
																	//SSNMARKSCO
																	  request.getParameter("SelectedBirthYear") + "-" +
										    						 	 request.getParameter("SelectedBirthMonth") + "-" +
										    						 	 request.getParameter("SelectedBirthDay"),
										    						  request.getParameter("SelectedHiredYear") + "-" +
										    						 	 request.getParameter("SelectedHiredMonth") + "-" +
										    						 	 request.getParameter("SelectedHiredDay"), 
										    						  iPayTypesSum,
										    						  iStatusSum,
										    						  Double.parseDouble(request.getParameter("WorkHour")),
										    						  Integer.parseInt(request.getParameter("IsActive"))
																	  );

	}
	
	public void doPost(HttpServletRequest request,
			HttpServletResponse response)
			throws ServletException, IOException {
		doGet(request, response);
	}
}